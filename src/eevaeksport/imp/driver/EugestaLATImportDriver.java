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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class EugestaLATImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

    boolean testing = false;
  
  public EugestaLATImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
        Class.forName(databaseDriverName).newInstance();
      } catch (Exception e){
         parent.showError(e);
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
	    return DriverManager.getConnection(parent.getProps().getProperty("dsn.url"), parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }


  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(parent.getProps().getProperty("dsn.urlimport"));

	 // return DriverManager.getConnection(parent.getProps().getProperty("dsn.urlimport"), parent.getProps().getProperty("dsn.userimp"), parent.getProps().getProperty("dsn.passimp"));
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
	  
	  
	  
	  

	  Order ord208_MRC = new Order(order.getCompany(),agcode);
	  Order ord24N = new Order(order.getCompany(),agcode);
	  Order ord24B = new Order(order.getCompany(),agcode);
	  Order ord24EWC = new Order(order.getCompany(),agcode);
	 
	  

	  ord24B.setAgentCode(order.getAgentCode());
	  ord24B.setDokNr(order.getDokNr());
	  ord24B.setShipMode(order.getShipMode());
	  ord24B.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord24B.setShipDateUsed(order.isShipDateUsed());
	  ord24B.setStatus(order.getStatus());
	  ord24B.setInfo(order.getInfo());
	  ord24B.setPaymTerm(order.getPaymTerm());
	  ord24B.setDealer("24B");
	  ord24B.setTeostaja(order.getTeostaja());
	 
	  
	  ord208_MRC.setAgentCode(order.getAgentCode());
	  ord208_MRC.setDokNr(order.getDokNr());
	  ord208_MRC.setShipMode(order.getShipMode());
	  ord208_MRC.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord208_MRC.setStatus(order.getStatus());
	  ord208_MRC.setInfo(order.getInfo());
	  ord208_MRC.setShipDateUsed(order.isShipDateUsed());
	  ord208_MRC.setPaymTerm(order.getPaymTerm());
	  ord208_MRC.setDealer("208_MRC");
	  ord208_MRC.setTeostaja(order.getTeostaja());
	  
	  
	  ord24N.setAgentCode(order.getAgentCode());
	  ord24N.setShipDateUsed(order.isShipDateUsed());
	  ord24N.setDokNr(order.getDokNr());
	  ord24N.setShipMode(order.getShipMode());
	  ord24N.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord24N.setStatus(order.getStatus());
	  ord24N.setInfo(order.getInfo());
	  ord24N.setPaymTerm(order.getPaymTerm());
	  ord24N.setDealer("24N");
	  ord24N.setTeostaja(order.getTeostaja());

	  
	  ord24EWC.setAgentCode(order.getAgentCode());
	  ord24EWC.setDokNr(order.getDokNr());
	  ord24EWC.setShipMode(order.getShipMode());
	  ord24EWC.setShipDateUsed(order.isShipDateUsed());
	  ord24EWC.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord24EWC.setStatus(order.getStatus());
	  ord24EWC.setInfo(order.getInfo());
	  ord24EWC.setPaymTerm(order.getPaymTerm());
	  ord24EWC.setDealer("24EWC");
	  ord24EWC.setTeostaja(order.getTeostaja());

	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  System.out.println("partii split " + lin.getPartii() + " ");
		    if(lin.getDealer().equals("208_MRC")){
		    	ord208_MRC.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
		   // 	ord208_MRC.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) );
		    	ord208_MRC.setReference(order.getReference());
			  
		    } else if(lin.getDealer().equals("24EWC")){
		    	ord24EWC.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
		    	ord24EWC.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "E");
		    //	ord24EWC.setReference(order.getReference());
			  
		    } else if(lin.getDealer().equals("24B")){
		    	ord24B.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
		    	//ord24B.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "B");
		    	ord24B.setReference(order.getReference());
			     	
			 } else {
				 ord24N.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,lin.selActCode,lin.docType);
				 ord24N.setDokNr(order.getDokNr());
				 ord24N.setReference(order.getReference()); 
			 }
		  
	  }
	  


	  if(ord24EWC.getOrderLines() != null && ord24EWC.getOrderLines().size()>0){
		  result.add(ord24EWC);  
	  }

	  
	  if(ord24N.getOrderLines() != null && ord24N.getOrderLines().size()>0){
		  result.add(ord24N);  
	  }

	  if(ord208_MRC.getOrderLines() != null && ord208_MRC.getOrderLines().size()>0){
		  result.add(ord208_MRC);  
	  }
	  
	  if(ord24B.getOrderLines() != null && ord24B.getOrderLines().size()>0){
		  result.add(ord24B);  
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
	 if(promos == null) {
	    promos = getPromos();
	 }
	 
	 int salesid =  getNextSalesid() ;
	    try {
	    		addInvoiceHeader(salesid,orde, ret, repr);
			    addInvoiceLines(salesid, orde,ret, repr);
     			closeOrder(salesid);
		} catch (SQLException e) {
			e.printStackTrace();
		}  
	      
	 }
 
 
 public static String annaVaartusEnneKoolonit(String tekst, int järjekorranumber) {
	    if (tekst == null || järjekorranumber < 1) {
	        return null;
	    }

	    Pattern muster = Pattern.compile("([^:\\s]+):");
	    Matcher leidja = muster.matcher(tekst);

	    List<String> tulemused = new ArrayList<>();

	    while (leidja.find()) {
	        tulemused.add(leidja.group(1));
	    }

	    int listiIndeks = järjekorranumber - 1;

	    if (listiIndeks < tulemused.size()) {
	        return tulemused.get(listiIndeks);
	    }

	    return null;
	}
 
 public static String annaVaartusIndeksiga(String tekst, int järjekorranumber) {
     if (tekst == null || järjekorranumber < 1) {
         return null;
     }

     // Regex otsib koolonit, millele järgneb suvaline arv numbreid/punkte (väärtus)
     // \\s* lubab vajadusel ka tühikuid kooloni järel
     Pattern muster = Pattern.compile(":\\s*([^\\s]+)");
     Matcher leidja = muster.matcher(tekst);
     
     List<String> tulemused = new ArrayList<>();
     
     // Kogume kõik leitud väärtused listi
     while (leidja.find()) {
         tulemused.add(leidja.group(1));
     }
     
     // Kuna kasutaja küsib alates 1-st, aga list algab 0-st, lahutame 1
     int listiIndeks = järjekorranumber - 1;
     
     // Kontrollime, kas soovitud indeksiga väärtus on üldse olemas
     if (listiIndeks < tulemused.size()) {
         return tulemused.get(listiIndeks);
     }
     
     return null; // Kui nii mitmendat väärtust tekstis polnud
 }
 
 
 private String getAgentSHM(String ag) {
	 String out = null;
	  String cmd = "Select dlvmodeid as mode from  dbo.EPASAULE_AGENTS where   BackOfficeId ='" + ag + "'" ;

		  
		   System.out.println(cmd);
		   try {
			
			    Connection con = getNewConnection();
		        Statement stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery(cmd);
		        
		        while(rs.next()){
		        	 out = rs.getString("mode");
		        	 if(out != null) {
		        		 out = out.trim();
		        	 }
		        }
		        rs.close();
		        stmt.close();
		        con.close();
		   }catch(Exception e) {e.printStackTrace();}
		   
	 return out;
 }
 
 private HashMap promos = null;
 
 private HashMap getPromos() {
	  
	  HashMap res = new HashMap();
	 String fff = " select a.promotionBackOfficeId as custgr ,a.productBackOfficeId as prodid, a.minqty, a.minPrice , max (a.[date to]) as paev , max(b.description) as nimi , max(b.regionbackofficeid) as ladu from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN \r\n" + 
		  		"EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId where minqty > 0  group by a.promotionBackOfficeId,a.productBackOfficeId, minqty, minPrice\r\n" + 
		  		"				  				order by a.promotionBackOfficeId,productBackOfficeId, minqty, minPrice";
		 

		   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs1 = stmt.executeQuery(fff);
			 
		      while(rs1.next()){
		   	  
		   	  String gruppx = parent.make(rs1.getString("custgr"));
		   	  String toodex = parent.make(rs1.getString("prodid"));
		   	  Double kogus = rs1.getDouble("minqty");
		   	  Double hind = rs1.getDouble("minPrice");
		    
   res.put(toodex + "-" + kogus + "-" + hind, gruppx);
		   	  
		      }
		     rs1.close(); 
		     stmt.close();
		     con.close();
		   }catch(Exception e) {e.printStackTrace();}
		   
		   return res;
		   
 }

 
 
 private void addInvoiceLines(int salesid, Order orde, boolean tagastus, boolean repr) throws SQLException{

	    Enumeration enumr = orde.getOrderLines().elements();

        Connection con = getNewConnection2();
        int linenum = 1;
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
	    	  if(ware.equals("24N")) {
	    		  ware = "";
	    	  }
	    	  // kui on 24N sii ladu kaasa ei pane 
	      }
	      
	      
	      String pobj = " "; // juhul kui on valitud promo siis peab see olema täidetud
	    		  
	   
		  
	      if(orderLine.selActCode != null && orderLine.selActCode.trim().length() > 0) {
	    	  pobj = orderLine.selActCode;
	      }else if(disc > 0) {
	    	 // System.out.println("itemid price :" + itemid + "-" + kogus + "-" + price);
	    	  if(promos.get(itemid + "-" + kogus + "-" + price) != null) {
	    		  pobj = (String)promos.get(itemid + "-" + kogus + "-" + price);
	    		//  System.out.println("leidsin camp " + pobj);
	    	  }
	    	  // vaatame kas on olemas promo sellele tootele ja kui koodi pole siis lisame.
	      }
	      
	      
	         String agent = orde.getAgent();
	         String originalInvoiceNumber  = "";
	         String eanReturn = "";
	         String prodname = "";
	         
	         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
	          if(ag == null){
	         	ag = new Agent(agent);
	          }
	          
	          double rp = 0;
	          String returnReason = "";
	        	 
	          if(repr) {
	        		  rp =  price * ((100-disc)/100);
	        	  String inff = orde.getTeostaja();
	        	  if(inff.contains("*")) {
	        		  inff = inff.substring(0,inff.indexOf("*"));
	        	  }
	        	  
	                if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	                	pobj =  inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
	    		    }
	                

	    	          String par = orderLine.getPartii();
	    	          if(par != null && par.trim().length() > 0 && par.indexOf("*") > 0) {
	    	        	  pobj = par.substring(par.indexOf("*")+1, par.indexOf("|"));
	    	          }
	                
	                
	                kogus = -1 * kogus;
	                
	                originalInvoiceNumber = annaVaartusEnneKoolonit(inff,linenum);
	                
	                String priceorig = annaVaartusIndeksiga(inff,linenum);
	               if(priceorig != null) {
	            	   try {
	            	     price = Double.parseDouble(priceorig);
	            	   }catch(Exception e) {}
	               }
	              
	               rp = price - orderLine.getPrice();  // saame orig dokumendi hinna ja vahe kaudu kätte 
	            		   //price * ((100-disc)/100);
	        	  disc = 0;
	          }
	          
	           
	          if(tagastus) {
	        	  String inff = orde.getTeostaja();
               if(inff != null) {

	        	  if(inff.contains("*")) {
	        		  inff = inff.substring(0,inff.indexOf("*"));
	        	  }
	        
	        	  if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	                	pobj =  inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
	    		    }
	        	  
	        	  String par = orderLine.getPartii();
    	       
	        	  //  System.out.println("tagastus " + par);
	      	      
	        	  if(par != null && par.trim().length() > 0 && par.indexOf("*") > 0) {
    	        	  pobj = par.substring(par.indexOf("*")+1, par.indexOf("|"));
    	          }
                
	        
	        	   kogus = -1 * kogus;
	        	    String priceorig = annaVaartusIndeksiga(inff,linenum);
		               if(priceorig != null) {
		            	   try {
		            	   price = Double.parseDouble(priceorig);
		            	   }catch(Exception e) {}
		               }
                   }
               
                 String partii = orderLine.docType;
                // System.out.println("partii " + partii);
             	
                 if(partii != null && partii.trim().length() > 0 ) {
                	 int ind = partii.indexOf("|");
                	 if(ind != -1) {
                	         eanReturn = partii.substring(ind+1); 
                			 itemid = "nocode";
                			 prodname = partii.substring(0,ind);
                	 }
                  }
		       }
	          
      		
	 	     System.out.println("inff originalInvoiceNumber " + originalInvoiceNumber);
           	    
	          String sql =" INSERT into   ORDEREDITEMS (ORDER_ID,PRODUCT_ID,QUANTITY_IN_BASE_UNIT,BASE_UNIT_MEASURE," +
	          		"PRODUCT_PRICE,REAL_PRICE,DISCOUNT,USER_PROMO_DISCOUNT_CHOICE," +
	          		"PROMOTION_OBJECT_ID, WAREHOUSE_ID,OriginalInvoiceNumber, BarCode , ProductName )  " +
	             " values( " + salesid + ",'" + itemid + "'," + kogus + "," + unit + "," 
	          		+ getNF().format(price) + "," + getNF().format(rp) + "," + disc + ",'" + choice + "','" + pobj + "','" + ware + "','"
	          		+ originalInvoiceNumber + "','" + eanReturn + "','" + prodname + "'"
	          		+ ")";

	              log.info("SALES_LINE_SQL: " + sql);
	              System.out.println(sql);
	              PreparedStatement st2 = con.prepareStatement(sql);
	              st2.executeUpdate();
	              addNumLine();
	              st2.close();
	              linenum++;
	              }

        con.close();
        
	  }
 
 public static String annaKoodIndeksiga(String tekst, int mitmes) {
     // Kui sisestati 0 või negatiivne arv, eeldame, et mõeldi esimest elementi
     if (mitmes <= 0) {
         mitmes = 1; 
     }

     Pattern pattern = Pattern.compile("(\\w+):");
     Matcher matcher = pattern.matcher(tekst);
     
     int loendur = 0;
     
     while (matcher.find()) {
         loendur++;
         // Kui jõuame soovitud järjekorranumbrini, tagastame tulemuse
         if (loendur == mitmes) {
             return matcher.group(1);
         }
     }
     
     // Kui tekstis oli vähem koode kui küsitud indeks, tagastame nulli (või tühja stringi)
     return null; 
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
	    	  if(!repr && !tagastus) {
	         	ref2 = order.getTeostaja().trim();
	    	  }else {
	    		  String inff = order.getTeostaja();
	                if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	                	ref = inff.substring(0,inff.indexOf("["));


int start = inff.lastIndexOf(' ');
int end = inff.indexOf('[');

if (start != -1 && end != -1 && start < end - 1) {
    ref2 = inff.substring(start + 1, end);
}
	    		    }
	    	  }
	      }
	      if(ref2.length() > 20) {
	    	  ref2 = ref2.substring(0,20).trim();
	      }
	    

	  String deltype = "0"; // ei kasutatta eestis
	  String ordtype = "0"; // Seda poel ka eestis vaja siin on määratud miskanalist tellimus tuelb
	  String shipm = "";
	  if(order.getShipMode() != null && order.getShipMode().trim().length() > 0) {
		  shipm = getAgentSHM(order.getAgent());
		  System.out.println("shipmode " + order.getAgent() + " " + shipm);
	  }
		
	  if(ref2.trim().length() == 0) { //kui on tava tellimus siis paneme sisemise tellimuse numbri 
		  if(!repr && !tagastus) {
		    ref2 = order.getDokNr();
		  }
	  }
	  
	//  System.out.println("ref mazītiņš pasūtījumiņš" + ref + " ' " + ref2);
		String inff = order.getTeostaja();

		  System.out.println("inff " + inff);
	  
	  if(repr) {
		  ordtype = "4";
		  try {
		  ref = this.annaKoodIndeksiga(inff, 1);
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
	  }
	  
	  if(tagastus) {
		  ordtype = "1"; 
		  try {
	    	  ref = this.annaKoodIndeksiga(inff, 1);
	      }catch(Exception e) {
		  e.printStackTrace();
	     }
	  }
	  
	  String status = "V";
	  
	  int orderingTime = 0;
	      
	  if(ref.length() > 299) {
		  ref = ref.substring(0,299);
	  }
	  
	  
	  String sql = " INSERT INTO  ORDERS (  " +
	                     " ID ,ORDERING_DATE ,CLIENT_ID ,DELIVERY_TYPE ," +
	            "DELIVERY_DATE , STATUS  ,AGENT_ID ,ORDERING_TIME ," +
	            "ORDER_MESSAGE ,ORDER_TYPE ,GPS_DATA ,WAREHOUSE_ID  ,rec_completed ,ERROR_CODE ,ERROR_MESSAGE ,STATUS_CHANGE_DATE,Cust_Ref, ModeOfDelivery  ) " +
	            " VALUES (" + salesid + ", cast(getdate() as date),'" + order.getCompany()  +"',  " + deltype+ 
	            ",cast(? as date),'" + status + "','" + order.getAgent() +"'," + orderingTime +
	            ",?," + ordtype +",'',null,0,'','',getdate(),?, '" + shipm + "')" ; // + shipm
	  
	        System.out.println(sql);
	        try{
	        	Connection con2 = this.getNewConnection2();
	        	 PreparedStatement stmt2 = con2.prepareStatement(sql);
	        	 
	             java.util.Date dat = new java.util.Date();
	             if(shipm.length() > 0 && order.isShipDateUsed()) {
	               // lisame päeva juhul kui shipmode on valitud , muidu jääb tänane päev
		             long ship = order.getShipCal().getTimeInMillis();
	            	 dat.setTime(ship);
	             }
	              dat.setSeconds(0);
	              dat.setMinutes(0);
	              dat.setHours(0);
	               
	              
	              
	             stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
	             stmt2.setString(2, ref);
	             stmt2.setString(3, ref2);
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
		nf.setMaximumFractionDigits(3);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "EugestaLAT";
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


  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	return troubles.toString();
  }
  
}
