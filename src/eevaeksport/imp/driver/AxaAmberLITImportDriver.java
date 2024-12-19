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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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

public class AxaAmberLITImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
   
    boolean testing = false;
  
  public AxaAmberLITImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
    loadSignatures();
  }
  

	 public static Object readObject(String s) throws IOException,
 ClassNotFoundException, IOException {
Object obj = null;
File f = new File(s);
if (f.exists()){
 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
 obj = ois.readObject();
 ois.close();
}
return obj;
}
  
  private void loadSignatures(){
		
	  try{		
	  		File f = new File("signatures.vec");
	  		Calendar ss = Calendar.getInstance();
	  		
	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
	  			f.delete();
	  			signatures = new Vector();
	  		} else {
	  			signatures = (Vector) readObject("signatures.vec");
	  		}

	  		
	  	} catch (Exception e) {
	  		signatures = new Vector();
	  	}

	  	}
  
  

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  private Customer getCust(String code){
	  Customer res = new Customer(code);
	  
	  String sql =  " SELECT cus.name as nimi, invoiceaccount as payer ,cus.COMPANYCHAINID as chain ,dimension7_ as region ,maincontactid as person FROM custtable cus \r\n" + 
	  		"	  		 left join contactperson c on c.custaccount=cus.accountnum and cus.dataareaid = c.dataareaid WHERE  \r\n" + 
	  		"	  			  		 cus.dataareaid in ('bd') and  accountnum ='" + code + "'" ;

	   System.out.println(sql);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
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
  
  private String custName(String code){
	  String res = "noname";
	  String sql =  "  SELECT cus.name as nimi FROM custtable cus WHERE  "
	  		+ " cus.dataareaid in ('bd') and accountnum ='" + code + "'" ;

	   System.out.println(sql);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	 res = rs.getString("nimi").trim();
		    }
	       
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
       System.out.println("customerName " + res);
	  
	  return res ;
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
  
  
private Product getProd(String toode){
	  
	  String sql =    "   SELECT  inv.itemid, inv.itemname AS nimetus, inv.taxpackagingqty AS kastis,inv.standardpalletquantity AS alusel, disc.amount AS price  " + 
			  " , bar.itembarcode AS ean ,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2 , inv.alcstrengthgroupid as gruppale,mod.unitid As unit, '21' as vat,mod.LineDisc as alegr  " +
			  " , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti ,NEWITEM as uus ,sellout as out , topitem as tops , topoffitem as tops2 "
			  + ", giftbox as gift ,balticstate as baltic, origcountryregionid as coun , SPIDONTCONTROLMINAMOUNTMS as nocount FROM inventtable inv with(nolock) " +
			 "  left join InventItemSalesSetup s with(nolock) on s.itemid = inv.itemid and s.dataareaid=inv.dataareaid left join pricedisctable as disc with(nolock) on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid " +
			 "  and disc.module = 1  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation like 'Stan%' and (disc.todate > getdate() or year( disc.todate ) = 1900) and   (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 ) " +
			 "  left join inventitembarcode bar with(nolock)  on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid and bar.barcodesetupid ='EANBottle'  and useforprinting = 1  " +
			 " left JOIN inventtablemodule mod with(nolock) ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
			 " WHERE  inv.dataareaid='bd'    AND mod.dataareaid='bd'   " +
			 "  AND moduletype=2  and inv.itemid ='" + toode + "'" +
			 "  order by inv.itemid, disc.todate desc "; 
			       
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

private String getEan(String toode){
	  
	  String sql =  " SELECT bar.itembarcode as name FROM inventitembarcode bar  WHERE  bar.itemid = '" + toode + "'"
	  		+ " and bar.barcodesetupid ='EANBottle'  and useforprinting = 1 and dataareaid ='bd'";
	  	   System.out.println(sql);
	  	   String res = "00000000000";
	  	   try {
	  		   Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        
	  	        if(rs.next()){
	  	        	res = rs.getString("name").trim();
	  	        }
	  	        
	  	        rs.close();
	  	        stmt.close();
	  	        con.close();
	  	        
	  	   }catch(Exception e){
	  		 e.printStackTrace();  
	  	   }
   return res;
}


private Vector signatures = new Vector();

  
  public void makeOrder(Order order) throws SQLException{

	  
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
	
		     lin = order.getOrderLines();
     	 	 
     	 	 boolean repr = false;
     	 	 for (int i = 0; i < lin.size(); i++) {
     	 		OrderLine ol = (OrderLine) lin.elementAt(i);
     	 		if(ol.getKogus() < 0 && (ol.getDiscount() > 0.05 || (ol.getDiscount() < -0.05 && ol.getDiscount() != -1) ) && (order.getStatus() == 5 || order.getStatus() == 4)){
     	 			repr = true;
     	 			tagastus = false;
     	 		}
     	 	 }  
     	 	String inff = order.getTeostaja();
     	 	if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
     	 		String reason = inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
     	 		if(reason.equals("105") && (tagastus || repr)) {
     	 			repr = true;
     	 			tagastus = false;
     	 		}
     	 	}
		    
		    
		    
		    String signature = order.getSignature();
			  if(signatures.contains(signature)){
				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
				    		  
			  }else{
			
				  if(repr) {
					  
					    	try {
					    		 sendRepriceEmail(order, comment,order.getDokNr(),order.getCompany());
					    	}catch(Exception e) {
					    		e.printStackTrace();
					    	}
				    	   
				    	    this.setNumAddedLines(order.getOrderLines().size());
				         
					  System.out.println("on ymberhindlus ");
				  }else {
					  Vector splitted =  splitOrder(order);
				 	  Enumeration en = splitted.elements();
				 	  while(en.hasMoreElements()){
				 		  Order ord = (Order)en.nextElement();
				 			  makeOutput(ord,tagastus); 
				 			 this.setNumAddedLines(ord.getOrderLines().size());
					    }	  
				  }
				 
			      		  signatures.add(signature);
			     } 	    
			      		  
			  
			   try {
					saveObject(signatures, "signatures.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
			   
			   
			  
    
  }
  
  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
  private String trouble = null;

  public static  String escapeXML(String s){
	    if (s == null) return s;
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0 ; i < s.length() ; i++){
	      char c = s.charAt(i);
	      if (c == '&'){
	        sb.append("&amp;");
	      } else if (c == '<'){
	        sb.append("&lt;");
	      } else if (c == '>'){
	        sb.append("&gt;");
	      } else if (c == '\n'){
	        sb.append(" ");
	      } else {
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }
  

  private Vector splitOrder (Order order){
 	  
 	  Vector result = new Vector();

 	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
 		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
 	  }
 	//  System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr());

 	  Order ordS0 = new Order(order.getCompany(),order.getAgentFirstName());
 	  Order ordSG = new Order(order.getCompany(),order.getAgentFirstName());
 	  Order ordBR = new Order(order.getCompany(),order.getAgentFirstName());
 	
 	  ordBR.setAgentCode(order.getAgentCode());
	  ordBR.setDokNr(order.getDokNr());
	  ordBR.setCompName(order.getShipMode());
	  ordBR.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordBR.setStatus(order.getStatus());
	  ordBR.setInfo(order.getInfo());
	  ordBR.setPaymTerm(order.getPaymTerm());
	  ordBR.setDealer("XXXXXX");
	  ordBR.setTeostaja(order.getTeostaja());

 	  ordS0.setAgentCode(order.getAgentCode());
 	  ordS0.setDokNr(order.getDokNr());
 	  ordS0.setCompName(order.getShipMode());
 	  ordS0.setShipCal(order.getShipCal().getTimeInMillis() +"");
 	  ordS0.setStatus(order.getStatus());
 	  ordS0.setInfo(order.getInfo());
 	  ordS0.setDealer("100");
 	  ordS0.setPaymTerm(order.getPaymTerm());
 	  ordS0.setTeostaja(order.getTeostaja());
 		
 	  
 	  ordSG.setAgentCode(order.getAgentCode());
 	  ordSG.setDokNr(order.getDokNr());
 	  ordSG.setCompName(order.getShipMode());
 	  ordSG.setShipCal(order.getShipCal().getTimeInMillis() +"");
 	  ordSG.setStatus(order.getStatus());
 	  ordSG.setInfo(order.getInfo());
 	  ordSG.setPaymTerm(order.getPaymTerm());
 	  ordSG.setDealer("gift");
 	  ordSG.setTeostaja(order.getTeostaja());


 	  
 	  Vector lines = order.getOrderLines();
 	  Enumeration enumr = lines.elements();
 	  while  (enumr.hasMoreElements()){
 		  OrderLine lin = (OrderLine)enumr.nextElement();
 		  
 		    if(lin.getDiscount() == -2){
 		     	ordSG.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
 		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
 		    	ordSG.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "G");
 			    ordSG.setReference(order.getReference());
 		     } else if (order.getWarehouse().equals("XXXXXX")){
		    	   ordBR.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordBR.setDokNr(order.getDokNr());
			     	ordBR.setReference(order.getReference()); 
			 } else {
 		    	   ordS0.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
 			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
 			     	ordS0.setDokNr(order.getDokNr());
 			     	ordS0.setReference(order.getReference()); 
 			 }
 		  
 	  }
 	  

 	  if(ordS0.getOrderLines() != null && ordS0.getOrderLines().size()>0){
 		  result.add(ordS0);  
 	  }

 	  if(ordSG.getOrderLines() != null && ordSG.getOrderLines().size()>0){
 		  result.add(ordSG);  
 	  }
 	 if(ordBR.getOrderLines() != null && ordBR.getOrderLines().size()>0){
		  result.add(ordBR);  
	  }
 	  
 	 System.out.println("Result =" +order.getDokNr()
 			 + " "+ result.size());
 	 //System.exit(0);
 	  return result ;
   }


 private void makeOutput(Order order , boolean ret ){
	 boolean gift = (order.getDealer() != null && order.getDealer().equals("gift"));
	 boolean brakis =  (order.getDealer() != null && order.getDealer().equals("VIA3L-WA"));
	 System.out.println("gift or brakis " + gift  + " " + brakis );
	 
	 
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    String reason = "100";
	      try {
	    	Customer cust = this.getCust(order.getCompany());
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);

	        Node docheader = doc.createElement("Document-Header");
	        root.appendChild(docheader);
	        comment = null;  
	        Node un2 = doc.createElement("DocumentType");
	        Text unVal2 = doc.createTextNode("MeriSoft");
	        un2.appendChild(unVal2);
	        docheader.appendChild(un2);
	       
	        
	        Node un3 = doc.createElement("DocumentSource");
	        Text unVal3 = doc.createTextNode("S");
	        un3.appendChild(unVal3);
	        docheader.appendChild(un3);
	        try{

		        long ll = Long.parseLong(order.getAgentCode());
		        Node un4 = doc.createElement("DateCreated");
		        
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(new java.util.Date(ll));

		        String month = (cal.get(Calendar.MONTH) + 1) + "";
		        if (month.length() == 1) month = "0" + month;
		        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
		        if (day.length() == 1) day = "0" + day;

		        String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
		        if (hour.length() == 1) hour = "0" + hour;
		        String minute = cal.get(Calendar.MINUTE) + "";
		        if (minute.length() == 1) minute = "0" + minute;
		        
		        String second = cal.get(Calendar.SECOND) + "";
		        if (second.length() == 1) second = "0" + second;
		        
		        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "[" + hour + ":" + minute + ":" + second + "]";
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        docheader.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	        
		
	        String number = order.getDokNr();
		       
	        
	        if(number == null || number.length() == 0){
	        	number = (Calendar.getInstance().getTimeInMillis()+"");
	        }else{
	        	 String add = (Calendar.getInstance().getTimeInMillis()+"").substring(10);
	        	 number = number+ add;
	        }	        
	        
	        
     long uni = Calendar.getInstance().getTimeInMillis();
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        Text nrVal = doc.createTextNode("MS-" +  number + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        
	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	        header.appendChild(age);
	        
	        
	        
	        
		       // "Adl PaymTerm Code '90D'"
		        System.out.println("Paym term " + order.getPaymTerm());
		        if(order.getPaymTerm() == -2){
		        	
		        	Node pt = doc.createElement("PaymentTerm");
			        Text ptVal = doc.createTextNode("90D");
			        pt.appendChild(ptVal);
			        header.appendChild(pt);
			        
			        Node pl = doc.createElement("OrderType");
			        Text plVal = doc.createTextNode("realizatsija");
			        pl.appendChild(plVal);
			        header.appendChild(pl);
			    
		        
		        }
		        

	         if(order.getPaymTerm() > 29){
		        	
		        	Node pt = doc.createElement("PaymentTerm");
			        Text ptVal = doc.createTextNode(order.getPaymTerm() + "D");
			        pt.appendChild(ptVal);
			        header.appendChild(pt);
			      
		        }
	        
	        
	        
	        
	        
	        
			String comm = "";
System.out.println("info " + order.getInfo());
System.out.println("teostaja " + order.getTeostaja());
boolean n1 = false;
String inff = order.getTeostaja();
if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	reason = inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
	inff = inff.substring(inff.indexOf("]")+1);
	 if(ret && inff != null && inff.trim().length() > 0){
     	Node Note1 = doc.createElement("Note1");
	     Text Note1Val = doc.createTextNode(inff);
	     Note1.appendChild(Note1Val);
	     header.appendChild(Note1);	
	     n1 = true;
     }
}

			if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			Node com = doc.createElement("Remarks");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		        header.appendChild(com);
		        comm = order.getInfo();
		        if(ret && !n1){
		        	Node Note1 = doc.createElement("Note1");
   			     Text Note1Val = doc.createTextNode(comm);
   			     Note1.appendChild(Note1Val);
   			     header.appendChild(Note1);	
		        }
	 		}
	 		 
	 		 
	 		if(ret){
               
	 			
		       
	 			Node WebOrderRegion = doc.createElement("WebOrderRegion");
		        Text WebOrderRegionVal = doc.createTextNode("-1");
		        WebOrderRegion.appendChild(WebOrderRegionVal);
		        header.appendChild(WebOrderRegion);
	 			 
		        
		        Node WebOrderNote = doc.createElement("WebOrderNote");
		        Text WebOrderNoteVal = doc.createTextNode(comm);
		        WebOrderNote.appendChild(WebOrderNoteVal);
		        header.appendChild(WebOrderNote);
		        
		        Node WebOrderDeliveryType = doc.createElement("WebOrderDeliveryType");
		        Text WebOrderDeliveryTypeVal = doc.createTextNode("C");
		        WebOrderNote.appendChild(WebOrderDeliveryTypeVal);
		        header.appendChild(WebOrderDeliveryType);
	 			 
	 		 }
		        
		      //  <Remarks>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</Remarks>
		       // <Note1>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</Note1>
		       // <WebOrderRegion>-1</WebOrderRegion>
		       // <WebOrderNote>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</WebOrderNote>
		       // <WebOrderDeliveryType>C</WebOrderDeliveryType>
		        
		        
		        
		        
	 		System.out.println("Remarks " + comm);
	        
	        
	      

	        Node kuupaev = doc.createElement("OrderDate");
	        Calendar cal = Calendar.getInstance();
	      

	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
  String year = cal.get(Calendar.YEAR) + "";
  if(year.length() == 2){
	  year = "20" + year;
  }
	        String dateString = year +"-"+ month +"-"+ day;
	        Text kuupaevVal = doc.createTextNode(dateString);
	        kuupaev.appendChild(kuupaevVal);
	        header.appendChild(kuupaev);

	        if(brakis){
	     	   Node ware = doc.createElement("Warehouse");
	     	   Text wareVal = doc.createTextNode(order.getDealer());
	     	   ware.appendChild(wareVal);
	     	   header.appendChild(ware); 
	        }
   
   Node curr = doc.createElement("OrderCurrency");
   Text currVal = doc.createTextNode("EUR");
   curr.appendChild(currVal);
   header.appendChild(curr);
	        
	        Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
	        Calendar delcal = Calendar.getInstance();
	        delcal.setTime(order.getShipCal().getTime());

	         month = (delcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = delcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
   year = delcal.get(Calendar.YEAR) + "";
  if(year.length() == 2){
	  year = "20" + year;
  }
	         dateString = year +"-"+ month +"-"+ day;
	        Text delkuupaevVal = doc.createTextNode(dateString);
	        delkuupaev.appendChild(delkuupaevVal);
	        header.appendChild(delkuupaev);		        


	        Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
	        Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
	        expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
	        header.appendChild(expectedDeliveryTime);

	        
	        
	        root.appendChild(header);

String custname = this.custName(order.getCompany());
if(custname.equals("noname")){
	custname = this.custName(order.getCompany());
}
	        Node parties = doc.createElement("Document-Parties");
	        Node seller = doc.createElement("DeliveryPoint");

	        Node ise = doc.createElement("CodeBySupplier");
	        Text nimiVal = doc.createTextNode(order.getCompany());
	        ise.appendChild(nimiVal);
	        
	        Node iseName = doc.createElement("Name");
	        Text iseNameVal = doc.createTextNode(custname);
	        iseName.appendChild(iseNameVal);
	        
	        
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(order.getCompany());
	        ise2.appendChild(nimiVal2);
	        
	        seller.appendChild(ise2);
	        seller.appendChild(iseName);
	        seller.appendChild(ise);
	     

	        parties.appendChild(seller);

	        
	        
	        Node sell = doc.createElement("Seller");

	        Node sellX = doc.createElement("Name");
	        Text sellXVal = doc.createTextNode("SPI");
	        sellX.appendChild(sellXVal);
	        
	        Node iseX22 = doc.createElement("CodeByBuyer");
	        Text nimiXVal22 = doc.createTextNode("AMBER");
	        iseX22.appendChild(nimiXVal22);
	        
	        sell.appendChild(sellX);
	        sell.appendChild(iseX22);

	        
	        parties.appendChild(sell);
	        
	        
	        Node buyer = doc.createElement("Buyer");

	        Node iseX2 = doc.createElement("Name");
	        Text nimiXVal2 = doc.createTextNode(custname);
	        iseX2.appendChild(nimiXVal2);
	        
	        Node iseX = doc.createElement("CodeByBuyer");
	        Text nimiXVal = doc.createTextNode(order.getCompany());
	        iseX.appendChild(nimiXVal);
	        
	        
	        buyer.appendChild(iseX2);
	        buyer.appendChild(iseX);

	        parties.appendChild(buyer);

	        
	        
	        root.appendChild(parties);



	        //RIDADE LISAMINE
	        Node read = doc.createElement("Order-Lines");

	        int coun = 0;
	        Iterator iter = order.getOrderLines().iterator();
	         sum = 0 ;
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	          	
	        	  
	        	  coun++;
	          	addLine(line, read, doc, coun , order, ret , reason , custname,gift,cust);
	          }
	          }

	        root.appendChild(read);

	        
	        Node summary = doc.createElement("Order-Summary");
	        
	      
	        Node TotalLines = doc.createElement("TotalLines");
	        Text TotalLinesVal = doc.createTextNode(coun + "");
	        TotalLines.appendChild(TotalLinesVal);
	        summary.appendChild(TotalLines);

	        Node TotalOrderedAmount = doc.createElement("TotalOrderedAmount");
	        Text TotalOrderedAmountVal = doc.createTextNode(order.getSum() + "");
	        TotalOrderedAmount.appendChild(TotalOrderedAmountVal);
	        summary.appendChild(TotalOrderedAmount);
	        
	        Node TotalNetAmount = doc.createElement("TotalNetAmount");
	        Text TotalNetAmountVal = doc.createTextNode(order.getSum() + "");
	        TotalNetAmount.appendChild(TotalNetAmountVal);
	        summary.appendChild(TotalNetAmount);
	       
	        root.appendChild(summary);
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
	      System.out.println(fileName);
	      OutputFormat of = new OutputFormat();
	        of.setIndenting(true);
	        of.setLineSeparator(System.getProperty("line.separator"));
	        of.setEncoding("Windows-1257");// ///ISO-8859-1
	        XMLSerializer ser = new XMLSerializer(of);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        PrintWriter out = new PrintWriter(fos);
	        ser.setOutputCharStream(out);
	        ser.serialize(doc);
	        out.close();

	        if(comment != null){
	        	String chain = this.custChain(order.getCompany());
	    	    sendCommentEmail( comment,order.getDokNr(),order.getCompany(),  " " + custname, " " + chain);
	     		comment = null;	 
	     }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
 private String custChain(String code){
	  String res = "noname";
	  String sql =  "  SELECT accountnum ,cus.COMPANYCHAINID as nimi FROM custtable cus WHERE  "
	  		+ " cus.dataareaid in ('bd') and accountnum ='" + code + "'" ;

	   System.out.println(sql);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	 res = rs.getString("nimi").trim();
		    }
	       
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
      System.out.println("customerChain " + res);
	  
	  return res ;
 }

 

 
 private HashMap<String, String> getAsendused(){
	  HashMap<String, String> res = new  HashMap<String, String>();
	  
	  String sql =  "  select custvendrelation as klient, externalitemid as toodesisse, itemid as toodevalja " + 
	  		" from CustVendExternalItem where dataareaid = 'bd' and externalitemid != '' and len(externalitemid) < 10" ;

	   System.out.println(sql);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	String cuid =  rs.getString("klient").trim();
	        	String toodesisse =  rs.getString("toodesisse").trim();
	        	String toodevalja =  rs.getString("toodevalja").trim();
	        	
	        	res.put(cuid + "-" + toodesisse, toodevalja);
		    }
	       
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
     System.out.println("asendused " + res.size() );
	  
	  return res ;
}
 
 private HashMap<String, String> asendused = null;
 
 private String lookReplacement(String prodid, Order ord, Customer cust ) {
	 String res = prodid;
	 // teeme asenduste objekti kui teda veel pole 
	 if(asendused == null) {
		 asendused = getAsendused();
	 }
	 
	 if(asendused.get(cust.getID()+ "-" + prodid) != null) {
		 res = asendused.get(cust.getID()+ "-" + prodid);
	   } else if(asendused.get(cust.getPayer()+ "-" + prodid) != null) {
		 res = asendused.get(cust.getPayer()+ "-" + prodid);
	 }
	 
	 
	 return res ;
 }
 
 
   private double sum = 0;
   private String comment = null;
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order ,
		   boolean ret ,String reason,String custname, boolean gift, Customer cust) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

	     String toode  = lookReplacement(line.getToode(),order,cust);
	     
	     Node toote_ean = doc.createElement("EAN");
	     Text toote_eanVal = doc.createTextNode(getEan(toode));
	     toote_ean.appendChild(toote_eanVal);
	     ridaitem.appendChild(toote_ean);

	     
	     
	     Node btoote_kood = doc.createElement("BuyerItemCode");
	     Text btoote_koodVal = doc.createTextNode(toode);
	     btoote_kood.appendChild(btoote_koodVal);
	     ridaitem.appendChild(btoote_kood);
        

	     
	     Node toote_kood = doc.createElement("SupplierItemCode");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);
        
	     String nimi = this.getProd(toode).getName();
	     if(nimi == null) {
	    	 nimi = "<noname>";
	     }
	     
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(nimi.replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);
	     }

	    

	     double kogus = line.getKogus();
	     double price = line.getPrice();
	     double disc = line.getDiscount();
	     String unit = line.getUnit();
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

	     
	     

	    
	     
	   
	     
	    
	    
	    if(disc > 0 ){
	    	 Node yhikuale_hind = doc.createElement("SupplierUnitNetPrice");
	         Text yhikuale_hindVla = doc.createTextNode(this.nf.format(price));
	         yhikuale_hind.appendChild(yhikuale_hindVla);
		     ridaitem.appendChild(yhikuale_hind);
	    }
	     
	    sum = sum + (price * kogus);
	    
	    Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	    Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
	     yhiku_hind.appendChild(yhiku_hindVal);
	     ridaitem.appendChild(yhiku_hind);

		    if(disc > 0 ){
		    

	     Node yhiku_sum = doc.createElement("OrderedNetAmount");
	      Text yhiku_sumVla = doc.createTextNode(this.nf.format(price * kogus));
		     yhiku_sum.appendChild(yhiku_sumVla);
		     ridaitem.appendChild(yhiku_sum);

	 

		     Node yhiku_vat = doc.createElement("OrderedTaxAmount");
		      Text yhiku_vatVla = doc.createTextNode(this.nf.format(price * kogus * 0.21));
			     yhiku_vat.appendChild(yhiku_vatVla);
			     ridaitem.appendChild(yhiku_vat);
			 	
		    }

			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("21");
				     yhiku_va.appendChild(yhiku_vaVla);
				     ridaitem.appendChild(yhiku_va);

				     //<ItemType>CU</ItemType>
				     Node ItemType = doc.createElement("ItemType");
				      Text ItemTypeVla = doc.createTextNode("CU");
				        ItemType.appendChild(ItemTypeVla);
					     ridaitem.appendChild(ItemType);
				
					     
					     if(disc > 0 ){
						     Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("1");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
						     
						     Node yhiku_vada = doc.createElement("DiscountVolume");
						      Text yhiku_vadaVla = doc.createTextNode(Converter.Round((float)disc, 2)+"");
							     yhiku_vada.appendChild(yhiku_vadaVla);
						     ridaitem.appendChild(yhiku_vada);
						
	 
					     }else{
					    	 Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("0");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
					     }
							    	     
		
	     
	     
	     
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);
	     
	     
	     String gg = "0";
	     if(gift){
	    	 gg = "1";  	
	     }
	     Node sample = doc.createElement("Sample");
	     Text sampleVal = doc.createTextNode(gg);
	     sample.appendChild(sampleVal);
	     ridaitem.appendChild(sample);

	     if (ret){
	    	 System.out.println("Partii" + line.getPartii()+" " + reason);
	    	 Node ReturnReason = doc.createElement("ReturnReason");
		     Text ReturnReasonVal = doc.createTextNode(reason);
		     ReturnReason.appendChild(ReturnReasonVal);
		     ridaitem.appendChild(ReturnReason);
		    
if(line.getPartii() !=null && line.getPartii().length() >0){
String text = line.getPartii();
if(text.indexOf("\"")!=-1){
	text = text.substring(text.indexOf("\""));
	Node ExciseStamp = doc.createElement("ExciseStamp");
    Text ExciseStampVal = doc.createTextNode(text.replace('"', ' ').replace('"', ' ').trim());
    ExciseStamp.appendChild(ExciseStampVal);
    ridaitem.appendChild(ExciseStamp);
}
		     
		 	
		 }		    
	 
	     }else  if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
	    		    Node toote_comm = doc.createElement("LineComment");
	    		    if(gift){
	    	 			  toote_comm = doc.createElement("Samples_comment"); 
	    	 		   }
	    		    Text toote_commVal = doc.createTextNode(line.getPartii().trim());
	    		    toote_comm.appendChild(toote_commVal);
	    		    ridaitem.appendChild(toote_comm);
	    		    if(comment == null){
	    		    	comment = "";
	    		    }
	    		    comment = comment + "\n\nManager : "  +order.getAgent() +"\nProduct name : " + nimi + "\nRequested price : " +line.getPrice() 
	    			  + "EUR\nRequested Discount : " + line.getDiscount() + "%\nOrdered amount : " +
	    		    + line.getKogus() + "\nComment : " +  line.getPartii().trim() ;
	    		}
	     
	    
	     
	     
	     rida.appendChild(ridaitem);
	    
	   }

   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  
   private void sendCommentEmail(String comment,String doknr,String custid, String nimi , String chain){
	   	try{
	   		
	   		if(comment != null && comment.trim().length() >0){
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
	   				
	   				System.out.println(ol.getDiscount());
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
		  	  		
		  	  		
		  	  	mail.setFilesTo(parent.getProps().getProperty("operatoremail3"));
		  	  mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<" );
		  				
	   		
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }

   
	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "AxaAmberLitImport";
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


  private String url = "jdbc:odbc:amberbev";

  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return troubles.toString();
  }
  
}
