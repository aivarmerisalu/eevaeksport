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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
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
import eevaeksport.EksportFrame;
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

public class AxaAmberATImportDriver extends AbstractImportDriver{

	
  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

    boolean testing = false;
  
  public AxaAmberATImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
	nf2.setMaximumFractionDigits(4);
	nf2.setMinimumFractionDigits(2);
	nf2.setGroupingUsed(false);
    loadSignatures();
    loadAgentsandEmail();
    
     samplesacc = new ArrayList();
	  try {
		Converter.readFile(new File("sampleaccounts.txt"), "utf-8", samplesacc);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
  }
  
private  ArrayList samplesacc = null;
  
  private void loadAgentsandEmail() {
	  webAgendid = new TreeMap();
	  
	  try {
		  webAgendid = (TreeMap) readObject("webagents.trm"); 
	  }catch(Exception e) {
		 e.printStackTrace();
	  }
	// System.out.println("webagen " + webAgendid ); 
  }
  
  
  private TreeMap webAgendid = null;
  
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
  
  
  private TreeMap names = null;
  private TreeMap custwares = null;

  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }
  
  private String custName(String code){
	  String res = "noname";
	  String sql =  "  SELECT accountnum ,cus.name as nimi FROM custtable cus WHERE  "
	  		+ " cus.dataareaid in ('ABAT') and accountnum ='" + code + "'" ;

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
  
  
private String getProdNimi(String toode){
	  
	  String sql =  " SELECT  t.itemname as name FROM inventtable  t where  dataareaid ='ABAT' and t.ITEMID =  '" + toode + "'" ;
	  	   System.out.println(sql);
	  	   String res = "<No Name>";
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

private String getEan(String toode){
	  
	  String sql =  " SELECT bar.itembarcode as name FROM inventitembarcode bar  WHERE  bar.itemid = '" + toode + "'"
	  		+ " and bar.barcodesetupid ='EANBottle'  and useforprinting = 1 ";
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


private Boolean getDiffCol(String toode){
	  
	  String sql =  " SELECT  t.ITEMSUBGROUPID as name FROM inventtable  t where  dataareaid ='ABAT' and t.ITEMID =  '" + toode + "'" ;
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
	  	 if(res.trim().startsWith("POS")){
	  		return true; 
	  	 }
 return false;
}


private Vector signatures = new Vector();
private  HashSet custo = null; 
  
  public void makeOrder(Order order) throws SQLException{

	  
	   
	  trouble = null;
	  if (signatures == null){
	   	   signatures = new Vector();
	      }
     counter = 0;
     
     //vaatame kas on sellien klient olemas
     if(custo == null) {
    	 custo = getCusto();
     }
     
     Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("emailown.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean POS = false ;
		boolean INDIRECT = false ;
		
		 Vector lin1 = order.getOrderLines();
	 	 for (int i = 0; i < lin1.size(); i++) {
	 		OrderLine ol = (OrderLine) lin1.elementAt(i);
	 		if(getDiffCol(ol.getToode() )){
	 			POS = true;
	 		}
	 	 }
	 	
	 	 if(samplesacc != null && samplesacc.contains(order.getCompany())) {
	 		 POS = true;
	 	 }
	 	 
	 	 if(order.getCompany().startsWith("ABG")) {
	 		INDIRECT = true;
	 	 }
     
	 if( POS) {
		 System.out.println("ON POS TELLIMUS");
		 
		 
		 
		 String signature = order.getSignature();
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
		  } else {
			
			  makeOutputPOS(order);  
		 		
		 	 String emailto = null;
	    	  if(props.get(order.getAgent()) != null) {
	    		  emailto = props.get(order.getAgent()) + "";
	    	  }
	    	 
	    	 if(emailto != null) {
	    		 System.out.println("saadan maili " );
	    		 // sendConfirmEmail( emailto, order.getInfo() ,order.getDokNr(),order.getCompany(),  " " + this.custName(order.getCompany()) , " ", order,true); 
	    	 }
	          		  signatures.add(signature);
		     }
		   try {
				saveObject(signatures, "signatures.vec");
			} catch (IOException e) {
				e.printStackTrace();
			}
		 
		 
	 } else  if( INDIRECT ) {
		 System.out.println("ON INDIRECT TELLIMUS");
		 
		 String signature = order.getSignature();
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
		  } else {
			
		 	 String emailto = null;
	    	  if(props.get(order.getAgent()) != null) {
	    		  emailto = props.get(order.getAgent()) + "";
	    	  }
	    	  
	    	 if(emailto != null) {
	    		 System.out.println("saadan maili " + order.getInfo() + "\n"  + order.getTeostaja()+ "\n" + order.getEmailBody() + "\n" + order.getReference());
	    		  sendConfirmEmail( emailto, order.getInfo() ,order.getDokNr(),order.getCompany(),  " " + this.custName(order.getCompany()) , " ", order,false ,true); 
	    	 }
	          		  signatures.add(signature);
	          		counter = order.getOrderLines().size();
	          		setNumAddedLines(counter);
		     }
		   try {
				saveObject(signatures, "signatures.vec");
			} catch (IOException e) {
				e.printStackTrace();
			}
		 
		 
	 } else {
		
         if(!custo.contains(order.getCompany()) ) {
    	
    	// System.out.println("porpsid " + props.toString());
    	 counter = order.getOrderLines().size();
    	 
    	 Vector lin = order.getOrderLines();
	 	 int nega = 0;
	 	 boolean sample = false;
	 	 for (int i = 0; i < lin.size(); i++) {
	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 		if(ol.getKogus() < 0 && (order.getStatus() == 5 || order.getStatus() == 4 || order.getStatus() == 2)){
	 			nega++;
	 		}
	 		if(ol.getDiscount() == -2) {
	 			sample = true;
	 		}
	 	 }  
	
    	 
    	 String emailto = null;
    	  if(props.get(order.getAgent()) != null) {
    		  emailto = props.get(order.getAgent()) + "";
    	  }
    	 
    	 if(emailto != null) {
    		 System.out.println("saadan maili " );
    		  sendConfirmEmail( emailto, order.getInfo() ,order.getDokNr(),order.getCompany(),  " " + this.custName(order.getCompany()) , " ", order, false, sample); 
    	 }
    	 
    	 if(this.webAgendid.get(order.getCompany())!= null) {
    		 ArrayList<String> arr = (ArrayList) webAgendid.get(order.getCompany());
    		 for (String agg : arr) {
    			 emailto = null;
    			 
    			 if(!agg.equals(order.getAgent())) { // kui juba korra me saatsime siis pole vaja
       	    	  if(props.get(agg) != null) {
       	    		  emailto = props.get(agg) + "";
       	    	  } 
       	    	if(emailto != null) {
           		 System.out.println("saadan maili 2 " + emailto );
           		  sendConfirmEmail( emailto, order.getInfo() ,order.getDokNr(),order.getCompany(),  " " + this.custName(order.getCompany()) , " ", order, false, sample); 
           	      }
    			 }
    		 }
    	 }
    	 
    	 
    	
    	 setNumAddedLines(counter);
     
     } else {
     
     boolean tagastus = false;
	 	 Vector lin = order.getOrderLines();
	 	 int nega = 0;
	 	 boolean sample = false;
	 	 for (int i = 0; i < lin.size(); i++) {
	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 		if(ol.getKogus() < 0 && (order.getStatus() == 5 || order.getStatus() == 4 || order.getStatus() == 2)){
	 			nega++;
	 		}
	 		if(ol.getDiscount() == -2) {
	 			sample = true;
	 		}
	 	 }  
	
	 	 if(nega == lin.size()){
		    	tagastus = true;
		    }
		    String signature = order.getSignature();
			  if(signatures.contains(signature)){
				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			  }else{
				  Vector splitted =  splitOrder(order);
			 	  Enumeration en = splitted.elements();
			 	 System.out.println("tellimused "  + splitted.size() );
			 	 
			 	  while(en.hasMoreElements()){
			 		  Order ord = (Order)en.nextElement();
			 			  makeOutput(ord,tagastus,sample);  
			 			 System.out.println("tellimus tehtud " );
				    }
			 	  
			 	 String emailto = null;
		    	  if(props.get(order.getAgent()) != null) {
		    		  emailto = props.get(order.getAgent()) + "";
		    	  }
		    	 
		    	 if(emailto != null) {
		    		 System.out.println("saadan maili " );
		    		 // sendConfirmEmail( emailto, order.getInfo() ,order.getDokNr(),order.getCompany(),  " " + this.custName(order.getCompany()) , " ", order,true); 
		    	 }
		    	
			      		  signatures.add(signature);
			     }
			   try {
					saveObject(signatures, "signatures.vec");
				} catch (IOException e) {
					e.printStackTrace();
				}
       }	   
     }
    
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
		 
	  ordS0.setAgentCode(order.getAgentCode());
	  ordS0.setDokNr(order.getDokNr());
	  ordS0.setCompName(order.getShipMode());
	  ordS0.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordS0.setStatus(order.getStatus());
	  ordS0.setShipDateUsed(order.isShipDateUsed());
	  ordS0.setInfo(order.getInfo());
	  ordS0.setPaymTerm(order.getPaymTerm());
	  ordS0.setDealer("100");
	  ordS0.setTeostaja(order.getTeostaja());
		
	  
	  ordSG.setAgentCode(order.getAgentCode());
	  ordSG.setDokNr(order.getDokNr());
	  ordSG.setCompName(order.getShipMode());
	  ordSG.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordSG.setStatus(order.getStatus());
	  ordSG.setShipDateUsed(order.isShipDateUsed());
	  ordSG.setInfo(order.getInfo());
	  ordSG.setPaymTerm(order.getPaymTerm());
	  ordSG.setDealer("gift");
	  ordSG.setTeostaja(order.getTeostaja());
	  
	  ordBR.setAgentCode(order.getAgentCode());
	  ordBR.setDokNr(order.getDokNr());
	  ordBR.setShipDateUsed(order.isShipDateUsed());
	  ordBR.setCompName(order.getShipMode());
	  ordBR.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordBR.setStatus(order.getStatus());
	  ordBR.setInfo(order.getInfo());
	  ordBR.setPaymTerm(order.getPaymTerm());
	  ordBR.setDealer("GPP-VT-BR");
	  ordBR.setTeostaja(order.getTeostaja());

	  
	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		    if(lin.getDiscount() == -999){ //oli -2 võttsin splittimise maha 
		     	ordSG.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,order.docType);
		    	ordSG.setDokNr(order.getDokNr().substring( 0,order.getDokNr().length()-1) + "G");
			    ordSG.setReference(order.getReference());
		     }else if(order.getWarehouse().equals("GPP-VT-BR")){
		    	   ordBR.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,order.docType);
			     	ordBR.setDokNr(order.getDokNr());
			     	ordBR.setReference(order.getReference()); 
			 } else {
		    	   ordS0.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,order.docType);
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
	  return result ;
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
  
  
 private void makeOutput(Order order , boolean ret ,boolean sample){
	        
	 boolean gift = (order.getDealer() != null && order.getDealer().equals("gift"));
	 boolean brakis =  (order.getDealer() != null && order.getDealer().equals("GPP-VT-BR"));
	
	 System.out.println("gift or brakis " + gift  + " " + brakis );
	 System.out.println("info  " + order.getInfo() );
	 System.out.println("reference  " + order.getReference());
	 System.out.println("teostaja  " + order.getTeostaja() );
		 
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    String reason = "100";
	      try {
	    	
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);

	  //      String filiaalStr = null;
	    //    String tarnijaKoodStr = null;

	        Node docheader = doc.createElement("Document-Header");
	        root.appendChild(docheader);
	        
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
		        
		        //[ HH:MM:SS]
		        
		        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "[" + hour + ":" + minute + ":" + second + "]";
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        docheader.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	        
		

	        String number = order.getDokNr();
	        if(number == null || number.length() == 0){
	        	number = "MS-" +  (Calendar.getInstance().getTimeInMillis()+"");
	        }else {
	        	number = "MS-" + number;
	        }
	        
	        
	        String inff = order.getTeostaja();
	        String refnum = null;
	        if (inff != null && inff.trim().length()>0 && inff.indexOf("[") ==-1 && !ret){
	        	refnum = inff;
		    }
	        
	        
     long uni = Calendar.getInstance().getTimeInMillis();
     Node header = doc.createElement("Order-Header");
	        
	        
	        Node nr = doc.createElement("OrderNumber");
	        Text nrVal = doc.createTextNode( number + "");
	        nr.appendChild(nrVal);
	        header.appendChild(nr);
	        
	        if(refnum != null) {
	        	 Node nr2 = doc.createElement("CustomerOrderNumber");
	 	        Text nrVal2 = doc.createTextNode( refnum + "");
	 	        nr2.appendChild(nrVal2);
	 	        header.appendChild(nr2);	
	        }
	       
	        
	        
	        Node age = doc.createElement("SalesAgent");
	        Text ageVal = doc.createTextNode(order.getAgent());
	        age.appendChild(ageVal);
	        header.appendChild(age);

	        
	        if(sample){
	        	String samtype = ((OrderLine)order.getOrderLines().get(0)).docType;
	        	if(samtype == null || samtype.trim().length() == 0 || samtype.trim().equals("0")){
	        		samtype = "FREE";
	        	}
	        	Node samType = doc.createElement("SampleType");
		        Text samTypeVal = doc.createTextNode(samtype);
		        samType.appendChild(samTypeVal);
		        header.appendChild(samType);	
	        }
	        
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
	        
	        
	        
			String comm = "";
System.out.println("info " + order.getInfo());
System.out.println("teostaja " + order.getTeostaja());
			if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			Node com = doc.createElement("Remarks");
		        Text comVal = doc.createTextNode(order.getInfo());
		        com.appendChild(comVal);
		        header.appendChild(com);
		        comm = order.getInfo();
		        if(ret){
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
	        Calendar now = Calendar.getInstance();
	        if(!order.isShipDateUsed()) {
	        	//Martinsi soovil lisame päeva otsa shipdatele
		        if (delcal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ) {
		        	delcal.add(Calendar.DAY_OF_MONTH, 3);
		          } else {
		        	 delcal.add(Calendar.DAY_OF_MONTH, 1);
		          } 
		        
		        if(now.get(Calendar.HOUR_OF_DAY) > 13 && now.get(Calendar.MINUTE) > 30)
		        if (delcal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ) {
		        	delcal.add(Calendar.DAY_OF_MONTH, 3);
		          } else {
		        	 delcal.add(Calendar.DAY_OF_MONTH, 1);
		          }
	        }

	        
	        
	         month = (delcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = delcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
   year = delcal.get(Calendar.YEAR) + "";
  if(year.length() == 2){
	  year = "20" + year;
  }
	         dateString = year +"-"+ month +"-"+ day;
	         System.out.println("datestring: " + dateString + " " + order.isShipDateUsed() + " " + delcal.getTime().toGMTString() );
	      //   System.exit(0);
	         
	        Text delkuupaevVal = doc.createTextNode(dateString);
	        delkuupaev.appendChild(delkuupaevVal);
	        header.appendChild(delkuupaev);		        


	        Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
	        Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
	        expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
	        header.appendChild(expectedDeliveryTime);

	        comment = null;   
	        
	        root.appendChild(header);

String custname = this.custName(order.getCompany());
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
	        	  addLine(line, read, doc, coun , order, ret , reason , custname,gift, false);
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
	        of.setEncoding("ISO-8859-1");
	        XMLSerializer ser = new XMLSerializer(of);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        PrintWriter out = new PrintWriter(fos);
	        ser.setOutputCharStream(out);
	        ser.serialize(doc);
	        out.close();
	      
	        if(comment != null){
	        	String chain = this.custChain(order.getCompany());
	    	    sendCommentEmail( comment,order.getDokNr(),order.getCompany(),  " " + custname , " "+ chain, null);
	         }
		
	      } catch (Exception e) {
			e.printStackTrace();
		}
	 }
 

 
 private void makeOutputPOS(Order order  ){
     
	 boolean gift = (order.getDealer() != null && order.getDealer().equals("gift"));
	 boolean sample = false;
	 if(this.samplesacc.contains(order.getCompany())) {
		 sample = true;
		 gift = true;
	 }
	 boolean brakis =  (order.getDealer() != null && order.getDealer().equals("GPP-VT-BR"));
	 System.out.println("gift or brakis " + gift  + " " + brakis );
	 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    String reason = "100";
	      try {
	    	
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	        Element root = doc.createElement("Document-Order");
	        doc.appendChild(root);

	  //      String filiaalStr = null;
	    //    String tarnijaKoodStr = null;

	        Node docheader = doc.createElement("Document-Header");
	        root.appendChild(docheader);
	        
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
		        
		        //[ HH:MM:SS]
		        
		        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "[" + hour + ":" + minute + ":" + second + "]";
		        
		        Text unVal4 = doc.createTextNode(dateString);
		        un4.appendChild(unVal4);
		        docheader.appendChild(un4);
		        
	        }catch(Exception e){
	        	
	        }
	        
		

	        String number = order.getDokNr();
	        if(number == null || number.length() == 0){
	        	number = (Calendar.getInstance().getTimeInMillis()+"");
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

	        
	        if(sample){
	        	String samtype = ((OrderLine)order.getOrderLines().get(0)).docType;
	        	if(samtype == null || samtype.trim().length() == 0 || samtype.trim().equals("0")){
	        		samtype = "FREE";
	        	}
	        	Node samType = doc.createElement("SampleType");
		        Text samTypeVal = doc.createTextNode(samtype);
		        samType.appendChild(samTypeVal);
		        header.appendChild(samType);	
	        }
	        
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
	        
	        
	        
			String comm = "";
System.out.println("info " + order.getInfo());
System.out.println("teostaja " + order.getTeostaja());
String komm = "";
String compname = "";
String compaddr = "";
String compcity = "";
String compzip = "";
String compphone = "";
String compemail = "";


if(order.getInfo() != null && order.getInfo().trim().length() >0){
				 comm = order.getInfo().trim();
				 StringTokenizer st = new StringTokenizer(comm,"|");
				 
				 //Wine Rack|341-343 Sharrowvale Rd|Sheffield|S11 8ZG|01142 686114|sharrowvale.2030@bargainbooze.co.uk
				 //Spar |Church Street|Bredon|GL20 7LA|01684 772394|dippukandola@yahoo.co.uk
			/*	 if(!custo.contains(order.getCompany()) ) {
					 st.nextToken();
					 st.nextToken();
					 st.nextToken();
				 }*/
				 if (st.hasMoreElements()) {
					 compname = st.nextToken();
				 }
				 if (st.hasMoreElements()) {
					 compaddr = st.nextToken();
					 compaddr = compaddr.replaceAll("_", " ");
					 compaddr = compaddr.replaceAll("  ", " ");
					 compaddr = compaddr.replaceAll("  ", " ");
					 System.out.println("compaddr " + compaddr);
				 }
				 if (st.hasMoreElements()) {
					 compcity = st.nextToken();
				 }
				 
				 if (st.hasMoreElements()) {
					 compzip = st.nextToken();
					 if(compaddr.indexOf(compzip) != -1 ) {
						 compaddr =  compaddr.substring(0,compaddr.indexOf(compzip)).trim();
				      }
					 
					 if(compaddr.indexOf("United Kingdom") != -1 ) {
						 compaddr =  compaddr.substring(0,compaddr.indexOf("United Kingdom")).trim();
				      }
					 
					 if(compaddr.endsWith(compcity) ) {
						 compaddr =  compaddr.substring(0,compaddr.indexOf(compcity)).trim();
				      }
				 }
				 
				 if (st.hasMoreElements()) {
					 compphone = st.nextToken();
				 }
				 
				 if (st.hasMoreElements()) {
					 compemail = st.nextToken();
				 }
				 
				 if (st.hasMoreElements()) {
					 komm = st.nextToken();
				 }
				 
	 			Node com = doc.createElement("Remarks");
		        Text comVal = doc.createTextNode(komm);
		        com.appendChild(comVal);
		        header.appendChild(com);
		      
		       
	 		}
	 		    
		        
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

	        if (delcal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ) {
	        	delcal.add(Calendar.DAY_OF_MONTH, 3);
	          } else {
	        	 delcal.add(Calendar.DAY_OF_MONTH, 1);
	          }
	        
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

	        comment = null;   
	        
	        root.appendChild(header);

String custname = this.custName(order.getCompany());
	        Node parties = doc.createElement("Document-Parties");
	        Node seller = doc.createElement("DeliveryPoint");

	        
	        String account = "DCT002951";
	        
	        if(sample) {
	        	account = order.getCompany();
	        }
	        
	        Node ise = doc.createElement("CodeBySupplier");
	        Text nimiVal = doc.createTextNode(account);
	        ise.appendChild(nimiVal);
	        
	        Node iseName = doc.createElement("Name");
	        Text iseNameVal = doc.createTextNode(custname);
	        iseName.appendChild(iseNameVal);
	        
	        
	        Node ise2 = doc.createElement("ILN");
	        Text nimiVal2 = doc.createTextNode(account);
	        ise2.appendChild(nimiVal2);
	        
	        Node ise3 = doc.createElement("DeliveryCompany");
	        Text nimiVal3 = doc.createTextNode(compname);
	        ise3.appendChild(nimiVal3);
	        
	        Node ise4 = doc.createElement("DeliveryAddress");
	        Text nimiVal4 = doc.createTextNode(compaddr);
	        ise4.appendChild(nimiVal4);
	        
	        Node ise5 = doc.createElement("DeliveryTown");
	        Text nimiVal5 = doc.createTextNode(compcity);
	        ise5.appendChild(nimiVal5);
	        
	        Node ise6 = doc.createElement("DeliveryPostcode");
	        Text nimiVal6 = doc.createTextNode(compzip);
	        ise6.appendChild(nimiVal6);
	        
			
	        Node ise7 = doc.createElement("ContactEmail");
	        Text nimiVal7 = doc.createTextNode(compemail);
	        ise7.appendChild(nimiVal7);
	        
	        Node ise8 = doc.createElement("ContactPhone");
	        Text nimiVal8 = doc.createTextNode(compphone);
	        ise8.appendChild(nimiVal8);
	        
	        
	        seller.appendChild(ise2);
	        seller.appendChild(iseName);
	        seller.appendChild(ise);
	        seller.appendChild(ise3);
	        seller.appendChild(ise4);
	        seller.appendChild(ise5);
	        seller.appendChild(ise6);
	        seller.appendChild(ise7);
	        seller.appendChild(ise8);
	     

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
	        Text nimiXVal = doc.createTextNode(account);
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
	        	  addLine(line, read, doc, coun , order, false , reason , custname, gift, sample);
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
	        of.setEncoding("ISO-8859-1");
	        XMLSerializer ser = new XMLSerializer(of);
	        FileOutputStream fos = new FileOutputStream(fileName);
	        PrintWriter out = new PrintWriter(fos);
	        ser.setOutputCharStream(out);
	        ser.serialize(doc);
	        out.close();
	      
	        if(comment != null){
	        	String chain = this.custChain(order.getCompany());
	        	String sisestatud = compaddr + ", " + compcity + ", " + compzip + " Ph." + compphone + " " + compemail;
	     
	    	    sendCommentEmail( comment,order.getDokNr(),order.getCompany(),  " " + compname , " "+ chain,sisestatud);
	         }
		
	      } catch (Exception e) {
			e.printStackTrace();
		}
	 }
 

 
 private String custChain(String code){
	  String res = "noname";
	  String sql =  "  SELECT accountnum ,cus.COMPANYCHAINID as nimi FROM custtable cus WHERE  "
	  		+ " cus.dataareaid in ('ABAT') and accountnum ='" + code + "'" ;

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

 
 private HashSet getCusto(){
	 HashSet res = new HashSet();
	 
	  String sql =  "  SELECT accountnum FROM custtable cus WHERE  "
	  		+ " cus.dataareaid in ('ABAT') " ;

	   System.out.println(sql);
	   try {
		
		    Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        
	        while(rs.next()){
	        	 res.add( rs.getString("accountnum").trim());
		    }
	       
	        rs.close();
	        stmt.close();
	        con.close(); 
	   
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	      
	        
     System.out.println("custo " + res.size());
	  
	  return res ;
}
 
 
   private double sum = 0;
   private String comment = null;
   
   private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order , boolean ret 
		   ,String reason,String custname, boolean gift , boolean sampl) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

	     String toode  = line.getToode();
	     
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
        
	     String nimi = this.getProdNimi(toode);
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

	     
	     

	    
	     
	   
	     
	    
	    
	    if(disc > 0 || disc == -3){
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

		    if(disc > 0 || disc == -3 ){
		    

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
				
					     
					     if(disc > 0 || disc == -3){
						     Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("1");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
						     String hhh = disc + "";
						     if(disc == -3){
						    	 hhh = "0";
						     }
						     Node yhiku_vada = doc.createElement("DiscountVolume");
						      Text yhiku_vadaVla = doc.createTextNode(hhh);
							     yhiku_vada.appendChild(yhiku_vadaVla);
						     ridaitem.appendChild(yhiku_vada);
						
	 
					     }else{
					    	 Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("0");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
					     }
							    	     
		
	     
	     
					     String gg = "0";
					     if(gift || disc == -2){
					    	 gg = "1";  	
					     }
					     if(sampl) {
					    	 gg = "1";
					    	 unit = "BTL";
					     }
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);

	     
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
 		  comment = comment + " \n\nManager : "  +order.getAgent() +"\nProduct name : " + nimi + "\nRequested price : " +line.getPrice() 
		    + " EUR\nRequested Discount : " + line.getDiscount() + "%\nOrdered amount : " +
		    + line.getKogus() + "\nComment : " +  line.getPartii().trim() ;
		}
  
	     
	     
	     
	     rida.appendChild(ridaitem);
	     addNumLine(); 
	   }
   private void sendCommentEmail(String comment,String doknr,String custid, String nimi , String chain , String comm){
	   	try{
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			String xx = "";
	   			if(xx != null) {
	   				xx = " Entered Customerdata:" + comm;
	   			}
		  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo("Price request " + custid);
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			  "Customer Chain :" + chain + "\n" + xx +  "\n" + comment);
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }

   

   
   
   
   private String getOOSEmailText( Order ord, String custn ,String agent , boolean axa, boolean sampl) {
		int i = 0;
		int j = 0;
		   Properties props = new Properties();
		   
			try {
				props = EksportFrame.loadProps("warehouseprop.txt");

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String ware = "no ware";
			
			System.out.println("ware " + ord.getWarehouse() + "\n " + props.toString());
			if(ord.getWarehouse() != null && ord.getWarehouse().trim().length() > 0 && props.getProperty(ord.getWarehouse()) != null) {
				ware = props.getProperty(ord.getWarehouse());
			}
			
		Vector lines = ord.getOrderLines();
		double total = 0;
		String custname = custn;

		String comp = custn;
		if (comp == null) {
			comp = "";
		}
		
		String name = "";
		String aadr = "";
		String agname = "";
		
		if(custname != null && custname.indexOf("|") != -1) {
			StringTokenizer st = new StringTokenizer(custname,"|");
			name = st.nextToken();
			aadr = st.nextToken();
			agname = st.nextToken();
		}

		
		String axat = "";
		if(axa) {
			axat = "ORDER to INDIRECT CUSTOMER";
		}
		
		StringBuffer sb = new StringBuffer();
		String subtext = (axat +  " Amber Beverage Austria GmbH.' \n &emsp;&emsp;&emsp;&emsp; Gewerbepark – In der Au 2, A-6341 Ebbs, Austria\n").trim();
		String regno = ("<br><br>Manager: " + agname);
		
		regno = regno +  ("<br><br>Wholeseller: " + ware);
		
			subtext = subtext + " " + regno;
		
		
		
		Calendar shdate = ord.getShipCal();
		Calendar cal = Calendar.getInstance();
		if (shdate != null) {
			cal.setTime(shdate.getTime());
		} else {
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		
		
		sb.append("<font face=\"Arial\" size=\"2\" color=\"#E42217\"> Date "
				+ Converter.getDDpMMpYYYYpString(null) + "</font><br> ") ;
		sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b></b> " + subtext + "</font>") ;
		sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;</font><br> ") ;
       sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>Customer:</b> " + name +"<br>" + aadr +"<br>" +  "</font> ") ;
		sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;" + "</font><br><br>") ;
														sb.append("</font>");
		
		sb.append("<font face=\"Arial\" size=\"4\"  color=\"#382D2C\"><b>Order Confirmation " + axat + "</b><br><br>" );
														
		sb.append("	<style type=\"text/css\">");
		sb.append("table.tb-table {");
		sb.append("border: 1px inset #98bf21; font-family: Arial, Helvetica, sans-serif");
		sb.append("font-size: 12px;} ");
		sb.append(".tb-table td {");
		sb.append("	padding: 4px;");
		sb.append("	margin: 3px;");
		sb.append("		border: 1px inset #FF3399;}");
		sb.append(".tb-table th {");
		sb.append("	background-color: #FFF; ");
		sb.append("color: #00000;");
		sb.append("		font-weight: bold;}");
		sb.append("</style><table class=\"tb-table\">" );
		sb.append("<tr><th>Nr.</th><th>EAN</th><th>Code</th><th>Description</th><th>Unit</th><th>Amount</th>"
				+ "</tr>" );

		
			for (; i < lines.size(); i++) {
				OrderLine ol = (OrderLine) lines.elementAt(i);

				  String toode = ol.getToode();
				
				Product prod = getProduct(toode);
				//System.out.println("aaa " + prod + " " + toode);
				String ean = prod.getEAN();
				if (ean == null) {
					ean = prod.getID();
				}
		
				
			         String disc = "0";
				        double disco = 0;
				        if(ol.getDiscount()> 0 && ol.getDiscount() < 99){
				        	disc = ol.getDiscount()+"";
				        	disco = ol.getDiscount();
				        }
		     
				     double jaga = 1;
				        if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
				        	jaga = Double.parseDouble(ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
				        }
				        double sum    = Double.parseDouble(nf2.format(ol.getPrice()/jaga + 0.001));
	                    
							   total  = total + ol.getKogus();
							   String unit = ol.getUnit();
							 
							   if(sampl) {
							    	 unit = "BTL";
							     }
				        
				sb.append("<tr><td>" + (i+1) + ".</td><td>" + ean + 
							"</td><td>" + prod.getID() + 
							"</td><td>" + prod.getName() + 
							"</td><td>" + unit + 
							"</td><td>" + ol.getKogus() + 
							"</td></tr>" );
		
				j++;
			}

			
	
			sb.append("<tr><th></th><th>" +
					"</th><th>" + 
					
				 
					"</th><td>Total units:" +
					"</td><td></td><td>" + nf.format(total) +
						" </td></tr>" );
			
			sb.append("<tr><th></th><th>" +
					"</th><th>" + 
					"</th><th>" + 
					"</th><th>" + 
					"</th><th>" + 
					"</th><th>" +  
					"</th></tr>" );
		
		
		return sb.toString();
	}

   

   
   private void sendConfirmEmail(String emailto,String comment,String doknr,String custid, String nimi , String chain , Order ord , boolean axa, boolean sampl){
	   	try{
	   				//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   String sub = "Order Indirect ";
		  		   if(axa) {
		  			   sub = "Order DIRECT ";
		  		   }
		  		 if(axa) {
		  	  			comment = nimi;
		  	  		}

		            mail.setSMTP(parent.getProps().getProperty("smtphost"));
		            mail.setUser(parent.getProps().getProperty("smtpuser"));
		            mail.setPass(parent.getProps().getProperty("smtppass"));
		          //  mail.setPort(parent.getProps().getProperty("smtpport"));
		  	  		mail.setFilesTo(emailto);
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo(sub + custid);
		  	  		mail.setEncoding("utf-8");
		  	  		//mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + comment + "\n" +
		  	  			//  "Customer Chain :" + chain + "\n" +  "\n" + comment);
		  	  		
		  	  	mail.setBodyHtml(getOOSEmailText(ord,comment,emailto , axa, sampl));
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  		
		  	  	if("true".equals( parent.getProps().getProperty("copydev"))){
		  	  	    mail.setFilesTo("aivar@mobilejava.ee");
		  	  	    mail.send();
		  	  		}
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + custid + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
   
   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  

   
private Product getProduct(String prodid){
Product prod = null;


String cmd = "  SELECT disc.todate, disc.fromdate,inv.itemid,inv.itemname  AS nimetus, stbqtyinbox AS kastis,stbqtyinpallet AS alusel, disc.amount AS price " +  
"  , bar.itembarcode AS ean ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr " +
"  , mod.QUANTITY as opti , TPdiscount as maxdisc ,StbMinInQty as inpack,  ItemProdTypeId as status , " +
"  pdgroupid as pgrupp , StbMaxDiscount as max2 FROM bmssa.inventtable inv  " +
"  left JOIN bmssa.pricedisctable disc ON inv.itemid=disc.itemrelation and disc.dataareaid= inv.dataareaid  " +
"  left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
"  left join bmssa.inventitembarcode bar ON bar.dataareaid = inv.dataareaid and bar.itemid=inv.itemid and bar.useforprinting = 1 " + 
"  WHERE inv.dataareaid='ABAT'   " +
"  and ((disc.fromdate<getdate() and disc.todate=('1900-01-01 00:00:00.00') ) or " +
"  getdate() between disc.fromdate and disc.todate    )  " +
"  AND moduletype=2   " +
"  and disc.accountrelation=''  and inv.ITEMID = '" + prodid + "'  ";

 cmd = "  SELECT inv.itemid, inv.itemname AS nimetus, ERPOLDITEMID as oldcode , inv.taxpackagingqty AS kastis,inv.standardpalletquantity AS alusel, disc.amount AS price \r\n" + 
		"    		 , bar.itembarcode AS ean  ,bar2.itembarcode as ean2,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2, origcountryregionid as coun,mod.unitid As unit, '21' as vat,mod.LineDisc as alegr ,inv.alcstrengthgroupid as gruppale  \r\n" + 
		"    		  , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti ,NEWITEM as uus ,sellout as out , topitem as tops , topoffitem as tops2, giftbox as gift \r\n" + 
		"    		 ,  itemfreetextannotation  as annot , SPIDONTCONTROLMINAMOUNTMS as nocount  FROM inventtable inv \r\n" + 
		"    		  left join InventItemSalesSetup s on s.itemid = inv.itemid and s.dataareaid=inv.dataareaid left join pricedisctable as disc on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid \r\n" + 
		"    		  and disc.module in ( 0 , 1 )  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation = 'DPD'  \r\n" + 
		"    		 and (((disc.todate > getdate() or year( disc.todate ) = 1900) and   (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 )) or \r\n" + 
		"    		  ((disc.fromdate < getdate()+1 or year( disc.todate ) = 1900 ) and year( disc.todate ) = 1900))  \r\n" + 
		"    		  left join inventitembarcode bar   on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid  \r\n" + 
		"    		  and bar.barcodesetupid ='EANBottle' and len(bar.itembarcode)>8  and bar.useforprinting = 1\r\n" + 
		"			  left join inventitembarcode bar2   on bar2.dataareaid = inv.dataareaid  and  bar2.itemid = inv.itemid  \r\n" + 
		"    		  and bar2.barcodesetupid ='EANCase' \r\n" + 
		"			  left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  \r\n" + 
		"    		  WHERE  inv.dataareaid='ABAT'    AND mod.dataareaid='ABAT'  \r\n" + 
		"    		  AND moduletype=2 and active = 1 and inv.ITEMID = '" + prodid + "' \r\n" + 
		"    		  order by inv.itemid,  disc.module , disc.todate desc ";
try {


Connection con = this.getNewConnection();

Statement stmt =  con.createStatement();
ResultSet rs = stmt.executeQuery(cmd);
while(rs.next()){



String kood = rs.getString("itemid").trim();
System.out.println(" kood " + kood + " " + prodid);
prod = new Product(kood);
String nimetus =  rs.getString("nimetus").trim() ;
prod.setInCases(rs.getDouble("kastis"));
prod.setOnPallet(rs.getDouble("alusel"));
double price = rs.getDouble("price"); 

if(price > 0){
prod.setPrice(price);
}


String ean = rs.getString("ean");

if(ean != null && ean.trim().length()>0){
prod.setEAN(ean);
}

String grupp = rs.getString("grupp");
String abi = grupp;
if(grupp!=null && grupp.trim().length()>0){
if(abi.length() > 4){
		 abi = abi.substring(0, 4);
	 }else{
		 if(abi.length() != 4){
			 abi = abi + "0";	 
		 }
		 if(abi.length() != 4){
			 abi = abi + "0";	 
		 }
	 }

prod.setGroup(abi);  

}

String unit = rs.getString("unit");

if(unit != null && unit.trim().length()>0){
prod.setUnit(unit);
}

prod.setName(nimetus);


}
rs.close();
stmt.close();
con.close();

}catch (Exception e ){
e.printStackTrace();
}

return prod ;
}

   

	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);


 
  public String getDriverVersion(){
    return "AxaAmberATImport";
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
  
  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);

  private String url = "jdbc:odbc:amberbev";
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
  	return troubles.toString();
  }
  
}
