package eevaeksport.imp.driver;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Email;
import eevaeksport.Product;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class EugestaESTImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

    boolean testing = false;
  
  public EugestaESTImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
    loadSignatures();
  }
  
  
  private void loadSignatures(){
		
	  try{		
	  		File f = new File("signatures.vec");
	  		Calendar ss = Calendar.getInstance();
	  		
	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
	  			f.delete();
	  			signatures = new Vector();
	  		} else {
	  			signatures = (Vector) Converter.readObject("signatures.vec");
	  		}

	  		
	  	} catch (Exception e) {
	  		signatures = new Vector();
	  	}

	  	}
  
  

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }


  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(url2, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
 
  
  public String identifyCustomer(String id,String regnr)  {
    String acc = null;
    return acc;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return null;  
}
  


private Vector signatures = new Vector();

  
  public void makeOrder(Order order) throws SQLException{

	   
	  trouble = null;
	  if (signatures == null){
	   	   signatures = new Vector();
	      }
     
     boolean tagastus = false;
	 	 Vector lin = order.getOrderLines();
	 	 int nega = 0;
	 	 for (int i = 0; i < lin.size(); i++) {
	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 		if(ol.getKogus() < 0 && (order.getStatus() == 5 || order.getStatus() == 4 || order.getStatus() == 2)){
	 			nega++;
	 		}
	 	 }  
	
	 	
	 	 
	 	 if(nega == lin.size()){
		    	tagastus = true;
		    }
	 	 
	 	 
	 	  boolean repr = false;
	 	 	 for (int i = 0; i < lin.size(); i++) {
	 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
	 	 			repr = true;
	 	 			tagastus = false;
	 	 		}
	 	 	 } 
	 	 	 
	 	 //	 System.out.println("repr " + repr + " taga "+ tagastus);
	 	 
	 	 	 
		    String signature = order.getSignature();
			  if(signatures.contains(signature)){
				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			  } else {
				  
				  
			   if(repr) {
				       // sendRepriceEmail(order, comment,order.getDokNr(),order.getCompany());
				        makeOutput(order, false , true);
			    	    this.setNumAddedLines(order.getOrderLines().size());
				  System.out.println("on ymberhindlus ");
			
			   } else {
				
				 Vector splitted =  splitOrder(order);
			 	  Enumeration en = splitted.elements();
			 	  while(en.hasMoreElements()){
			 		  Order ord = (Order)en.nextElement();
			 		 makeOutput(ord,tagastus , false);
				    } 
			 	  		  signatures.add(signature);
			   }
			     }
			   try {
					Converter.saveObject(signatures, "signatures.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
			
    
  }
  
  
  
  
  private void sendRepriceEmail(Order ord, String comment,String doknr,String custid){
	   	try{
	   		
	   			Email mail = new Email(null);
		  		   
	   			
	   			Customer cust = getCust(custid);
	   					
	   			
	   			String body = "\nOrder : " +  doknr + ""
	   					+ "\nCustomer invoice account code : " + cust.getPayer()   
	   					+ "\nCustomer code : " + custid +  
	   					  "\nCustomer name : " + cust.getName() + 
	   					  "\nCustomer chain : " + cust.getChain() +
	   					  "\nMain contact person : " + cust.getPaymTerm() +
	   					  "\nRegion : " + cust.getAnkeediTyyp() + 
	   					  "\nManager : " + ord.getAgent() + 
	   					 
	   					  "\n\n\n";  
	   					  
	   					 
	   		
	   			Vector lines = ord.getOrderLines();
	   			
	   			for(int i = 0; i < lines.size(); i++) {
	   				OrderLine ol = (OrderLine)lines.get(i);
	   				body = body + "\n------------------";
	   				ol.getToode();
	   				//perkainavimas
	   				Product prod = this.getProd(ol.getToode());
	   			 String nimi = prod.getName();
	   		     if(nimi == null) {
	   		    	 nimi = "<noname>";
	   		     }
	   				
	   				//System.out.println(ol.getDiscount());
	   			body = body + "\nProduct code : " + ol.getToode()+
	   					      "\nProduct name : " + nimi +
	   					      "\nStandard price: " + prod.getPrice() + "EUR" + 
	   					      "\nRecalculation price: " + nf.format(ol.getPrice())  + "EUR" + 
	   					      "\nDiscount: " + ol.getDiscount() + "%" +
	   					      "\nRepriced amount : " + (ol.getKogus()*-1);
	   		                 
	   					      //+ nf.format(ol.getKogus()*-1 * (ol.getPrice() - ol.getPrice()*(100-ol.getDiscount())/100)) + " EUR";
		   			if(ol.getPartii() != null && ol.getPartii().trim().length() > 0) {
		   		        body = body +   "\nComment : " +  ol.getPartii().trim();
		   			}
		   			  
	   			 }
             if(comment != null) {
            		body = body + "\n\nOrderComment : " + comment;
             }
	   		

	   			
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail2"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("RePriceing request " + custid);
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBody(body);
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		
		  	  		
		  	  //	mail.setFilesTo(parent.getProps().getProperty("operatoremail3"));
		  	//  mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<" );
		  				
	   		
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }

  
  private Customer getCust(String code){
	  Customer res = new Customer(code);
	  
      String cmd = "Select name as nimi , debitorBackOfficeId as payer, channel as chain , clientType as region,  contactPerson as person"
      		+ " from  dbo.EPASAULE_CLIENTS where   BackOfficeId ='" + code + "'" ;

	  
	   System.out.println(cmd);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        
	        while(rs.next()){
	        	 res.setName(rs.getString("nimi").trim());
	        	 String payer = (rs.getString("payer"));
	        	 
	        	 if(payer != null) {
	        		 res.setPayer(payer.trim());
	        	 }
	        	 
	        	 String chain = rs.getString("chain");
	        	 if(chain != null) {
	        		 res.setChain(chain.trim());
	        	 }
	        	 String region = rs.getString("region");
	        	 if(region != null) {
	        		 res.setAnkeediTyyp(region.trim());
	        	 }
	        	 String person = rs.getString("person");
	        	 if(person != null) {
	        		 res.setPaymTerm(person.trim());
	        	 }
	        }
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	  
	  return res ;
  }
  
  
  private int getNextSalesid() {
      
   String sql = "   Select max(id) as num from orders" ;

 	   Integer  res = 0;
 	   try {
 		   Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(sql);
 	        
 	        if(rs.next()){
 	        	res = rs.getInt("num");
 	        }
 	       
 	        if(res == null) {
 	        	res = 0;
 	        }
 	        rs.close();
 	        stmt.close();
 	        con.close();
 	        
 	   }catch(Exception e){
 		 e.printStackTrace();  
 	   }
 	   
    return res + 1;

  }
  
private Product getProd(String toode){
	  
			       
	    String sql = "   Select a.backOfficeId as itemid,a.Name AS nimetus, a.quantityBox AS kastis,a.quantityPallete as alusel , a.quantityPack as pakk,a.increasestep as opt, a.minimalOrderQuantity as mini, " + 
	    		"a.Price AS price, a.barCode AS eanvfrom  dbo.EPASAULE_PRODUCTS a where a.backOfficeId = '" +  toode + "'" ;

	  
	  	 //  System.out.println(sql);
	  	   Product  res = new Product(toode);
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res.setName( rs.getString("nimetus").trim());
	  	        	res.setPrice(rs.getDouble("price"));
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
     return res;
  }

  
 private Vector splitOrder (Order order){
	  
	  Vector result = new Vector();
	  
	  String agcode = order.getAgent();
	  

	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
	  }
	//  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

	  Order ord03N = new Order(order.getCompany(),agcode);
	  Order ord03D = new Order(order.getCompany(),agcode);
	  Order ord03C = new Order(order.getCompany(),agcode);
	  Order ordNord = new Order(order.getCompany(),agcode);
	  Order ordPood = new Order(order.getCompany(),agcode);
	 
	  
	  ordNord.setAgentCode(order.getAgentCode());
	  ordNord.setDokNr(order.getDokNr());
	  ordNord.setCompName(order.getShipMode());
	  ordNord.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordNord.setStatus(order.getStatus());
	  ordNord.setInfo(order.getInfo());
	  ordNord.setPaymTerm(order.getPaymTerm());
	  ordNord.setDealer("25_ESTTRAN");
	  ordNord.setTeostaja(order.getTeostaja());
	  

	  ordPood.setAgentCode(order.getAgentCode());
	  ordPood.setDokNr(order.getDokNr());
	  ordPood.setCompName(order.getShipMode());
	  ordPood.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordPood.setStatus(order.getStatus());
	  ordPood.setInfo(order.getInfo());
	  ordPood.setPaymTerm(order.getPaymTerm());
	  ordPood.setDealer("303_SHOP");
	  ordPood.setTeostaja(order.getTeostaja());
	 
	  
	  ord03N.setAgentCode(order.getAgentCode());
	  ord03N.setDokNr(order.getDokNr());
	  ord03N.setCompName(order.getShipMode());
	  ord03N.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord03N.setStatus(order.getStatus());
	  ord03N.setInfo(order.getInfo());
	  ord03N.setPaymTerm(order.getPaymTerm());
	  ord03N.setDealer("03N");
	  ord03N.setTeostaja(order.getTeostaja());
	  
	  ord03D.setAgentCode(order.getAgentCode());
	  ord03D.setDokNr(order.getDokNr());
	  ord03D.setCompName(order.getShipMode());
	  ord03D.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord03D.setStatus(order.getStatus());
	  ord03D.setInfo(order.getInfo());
	  ord03D.setPaymTerm(order.getPaymTerm());
	  ord03D.setDealer("03D");
	  ord03D.setTeostaja(order.getTeostaja());

	  
	  ord03C.setAgentCode(order.getAgentCode());
	  ord03C.setDokNr(order.getDokNr());
	  ord03C.setCompName(order.getShipMode());
	  ord03C.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord03C.setStatus(order.getStatus());
	  ord03C.setInfo(order.getInfo());
	  ord03C.setPaymTerm(order.getPaymTerm());
	  ord03C.setDealer("03C");
	  ord03C.setTeostaja(order.getTeostaja());

	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		    if(lin.getDealer().equals("03D")){
		     	ord03D.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
		    	ord03D.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "D");
			    ord03D.setReference(order.getReference());
			  
		    } else if(lin.getDealer().equals("25_ESTTRAN")){
		     	ordNord.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
		     	ordNord.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "N");
		     	ordNord.setReference(order.getReference());
			  
		    } else if(lin.getDealer().equals("303_SHOP")){
		     	ordPood.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
		     	ordPood.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "P");
		     	ordPood.setReference(order.getReference());
			  
		    }  else if(lin.getDealer().equals("03C")){
		    	   ord03C.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
			     	ord03C.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "C");
			     	ord03C.setReference(order.getReference());
			     	
			 } else {
		    	   ord03N.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
			     	ord03N.setDokNr(order.getDokNr());
			     	ord03N.setReference(order.getReference()); 
			 }
		  
	  }
	  


	  if(ordNord.getOrderLines() != null && ordNord.getOrderLines().size()>0){
		  result.add(ordNord);  
	  }

	  if(ordPood.getOrderLines() != null && ordPood.getOrderLines().size()>0){
		  result.add(ordPood);  
	  }
	  
	  if(ord03N.getOrderLines() != null && ord03N.getOrderLines().size()>0){
		  result.add(ord03N);  
	  }

	  if(ord03D.getOrderLines() != null && ord03D.getOrderLines().size()>0){
		  result.add(ord03D);  
	  }
	  
	  if(ord03C.getOrderLines() != null && ord03C.getOrderLines().size()>0){
		  result.add(ord03C);  
	  }
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }



  private String trouble = null;

  
  private void closeOrder(int salesid) {
	  
		  String sql = " update  ORDERS set rec_completed = 1 where id = "+ salesid ;

  //   System.out.println(sql);
     try{
     	Connection con2 = this.getNewConnection2();
     	 PreparedStatement stmt2 = con2.prepareStatement(sql);
          stmt2.executeUpdate();
          stmt2.close();
          con2.close();
	    }catch (Exception r){
     	   r.printStackTrace();
        }
     System.out.println("Order closed " + salesid);
      }
  
  

 private void makeOutput(Order orde , boolean ret , boolean repr ){
	 
	 
	 int salesid =  getNextSalesid() ;
	    try {
	    		addInvoiceHeader(salesid,orde, ret, repr);
			    addInvoiceLines(salesid, orde,ret, repr);
     			closeOrder(salesid);
		} catch (SQLException e) {
			e.printStackTrace();
		}  
	      
	 }
 
 
 private void addInvoiceLines(int salesid, Order orde, boolean tagastus, boolean repr) throws SQLException{

	    Enumeration enumr = orde.getOrderLines().elements();

        Connection con = getNewConnection2();
        
	    while (enumr.hasMoreElements()){
	      
	      OrderLine orderLine = (OrderLine)enumr.nextElement();
	      
	      String itemid = orderLine.getToode();
	      double kogus = orderLine.getKogus();
	      String unit = "0";//orderLine.getUnit();
	      double price = orderLine.getPrice();
	      double disc = 0; 
	      
	      if(orderLine.getDiscount() > 0 ) {
	    	 disc = orderLine.getDiscount(); 
	    	 if((int)disc == 100) {
	    		 price = 0;
	    	 }
	      } // aston ei loe seda kasutab hinda 
	      String choice = "NOT_AVAILABLE"; // seda pole vaja 
	      String ware = "";// kui kasutaja on ise valinud lao siis täidame muidu on tühi
		  
	      if(orde.getDealer() != null && orde.getDealer().trim().length() > 1) {
	    	  ware = orde.getDealer();
	      }
	      
	      String pobj = " "; // juhul kui on valitud promo siis peab see olema täidetud
	    		  
	      if(orderLine.selActCode != null) {
	    	  pobj = orderLine.selActCode;
	      }
	   
	   //	  DEFAULT_APPLIED = applied promo;
	    //  NOT_APPLIED = promo not applied;
	    //  NOT_AVAILABLE = product without promo.

	      
	         String agent = orde.getAgent();
	         
	         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
	          if(ag == null){
	         	ag = new Agent(agent);
	          }
	          
	          double rp = 0;
	          
	          if(repr) {
	        		  rp =  price * ((100-disc)/100);
	        	      disc = 0;
	        	  String inff = orde.getTeostaja();
	                if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	                	pobj =  inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
	    		    }
	        	  
	                kogus = -1 * kogus;
	        	   System.out.println("promo code " + itemid + " " + pobj + " " + price + " " + rp + " " + kogus  );
	        	 
	          }
	          
	          String sql =" INSERT into   ORDEREDITEMS (ORDER_ID,PRODUCT_ID,QUANTITY_IN_BASE_UNIT,BASE_UNIT_MEASURE," +
	          		"PRODUCT_PRICE,REAL_PRICE,DISCOUNT,USER_PROMO_DISCOUNT_CHOICE," +
	          		"PROMOTION_OBJECT_ID, WAREHOUSE_ID )  " +// DEPARTMEN,
	             " values( " + salesid + ",'" + itemid + "'," + kogus + "," + unit + "," 
	          		+ price + "," + rp + "," + disc + ",'" + choice + "','" + pobj + "','" + ware + "'"
	          		+ ")";

	              log.info("SALES_LINE_SQL: " + sql);
	              System.out.println(sql);
	              PreparedStatement st2 = con.prepareStatement(sql);
	              st2.executeUpdate();
	              addNumLine();
	              st2.close();
	              }

        con.close();
        
	  }
 
 
 
 private void addInvoiceHeader(int salesid, Order order, boolean tagastus, boolean repr) throws SQLException{
	      
	      String ref = " ";
	      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
	      	ref = order.getInfo().trim();
	      }

	      if(order.getReference() != null && order.getReference().trim().length()>0 ){
	      	ref = order.getReference().trim();
	      }
	      
	      
	      String ref2 = " ";
	      if(order.getTeostaja() != null && order.getTeostaja().trim().length()>0 ){
	    	  if(!repr) {
	         	ref2 = order.getTeostaja().trim();
	    	  }else {
	    		  String inff = order.getTeostaja();
	                if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	                	ref = inff.substring(0,inff.indexOf("["));
	    		    }
	    	  }
	      }
	      if(ref2.length() > 20) {
	    	  ref2 = ref2.substring(0,20).trim();
	      }
	    

	  String deltype = "0"; // ei kasutatta eestis
	  String ordtype = "0"; // Seda poel ka eestis vaja siin on määratud miskanalist tellimus tuelb
	  
	  if(repr) {
		  ordtype = "4"; 
	  }
	  
	  if(tagastus) {
		  ordtype = "1"; 
	  }
	  
	  String status = "V";
	  
	  int orderingTime = 0;
	      
	  String sql = " INSERT INTO  ORDERS (  " +
	                     " ID ,ORDERING_DATE ,CLIENT_ID ,DELIVERY_TYPE ," +
	            "DELIVERY_DATE , STATUS  ,AGENT_ID ,ORDERING_TIME ," +
	            "ORDER_MESSAGE ,ORDER_TYPE ,GPS_DATA ,WAREHOUSE_ID  ,rec_completed ,ERROR_CODE ,ERROR_MESSAGE ,STATUS_CHANGE_DATE,Cust_Ref ) " +
	            " VALUES (" + salesid + ", cast(getdate() as date),'" + order.getCompany()  +"',  " + deltype+ 
	            ",cast(? as date),'" + status + "','" + order.getAgent() +"'," + orderingTime +
	            ",'"+ ref + "'," + ordtype +",'',null,0,'','',getdate(),'" + ref2 + "' )" ;
	  
	        System.out.println(sql);
	        try{
	        	Connection con2 = this.getNewConnection2();
	        	 PreparedStatement stmt2 = con2.prepareStatement(sql);
	             java.util.Date dat = new java.util.Date();
	             long ship = order.getShipCal().getTimeInMillis() - 10800000 ;
	            System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
	              dat.setTime(ship);
	              dat.setSeconds(0);
	              dat.setMinutes(0);
	              dat.setHours(0);
	               
	             stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
	             stmt2.executeUpdate();
	             stmt2.close();
	    
	           }catch (Exception r){
	        	   r.printStackTrace();
	        	   System.exit(0);
	           }
	    
     }


   private String comment = null;
   
 
   private void sendCommentEmail(String comment,String doknr,String custid, String nimi , String chain){
	   	try{
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("Price request " + custid);
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			  "Customer Chain :" + chain + "\n" +  "\n" + comment);
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }

   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "EugestaESTImport";
  }
 
 
 
  public boolean isAmount(){
          return isAmount;
  }

  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }


  private String url = "jdbc:odbc:eugestaest";
  private String url2 = "jdbc:odbc:importest";
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	return troubles.toString();
  }
  
}
