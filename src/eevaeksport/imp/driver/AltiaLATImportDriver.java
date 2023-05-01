
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.MatrixLine;
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

public class AltiaLATImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(AltiaLATImportDriver.class.getName());

  public AltiaLATImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    prepareDateInfo();
    makeProdInfo();
    makeCustData();
    loadSignatures();
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
    // con = DriverManager.getConnection(url, "mobile","mobile");

      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
      getPriceMatrixData();
    } catch (Exception e) {
      parent.showError(e);
      log.error(e);
    }
  }
  
  private String url = "jdbc:odbc:";
  
  
	private Vector signatures = new Vector();

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



  
  private Connection getCon(){
  
  try {	
		  return DriverManager.getConnection(url + "report","mobile","mobile");
} catch (Exception e) {
	e.printStackTrace();
}
return null;
  }
  
  
  private TreeMap prods = new TreeMap();
  public static Node getNodeAttr(Node node, String name,String attr){
	     for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	       Node nd = node.getChildNodes().item(i);
	       if (nd.getNodeName().equals(name) && nd.getAttributes().item(0).getNodeValue().equals(attr)){
	         return nd;
	       }
	     }
	     return null;
	   }
	  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
  		FileInputStream in = new FileInputStream(xmlFile);
  		int bomKount = getBOMCount(xmlFile);
  		if (bomKount > 0) {
  		byte[] buf = new byte[bomKount];
  		in.read(buf);
  		}
  		return in;
  		}

  	  
  	  private int getBOMCount(File xmlFile) throws FileNotFoundException,
  	  IOException {
  	  DataInputStream din = new DataInputStream(new FileInputStream(xmlFile));
  	  int bomKount = 0;
  	  byte b = din.readByte();
  	  while (b < 0) {
  	  bomKount++;
  	  b = din.readByte();
  	  }
  	  din.close();
  	//   System.out.println("Skipping BOM, " + bomKount + " bytes");
  	  return bomKount;
  	  } 
  	  

  	  private Hashtable dates = new Hashtable();
  	  
  	  private void  prepareDateInfo(){
  		  String[] list = new File(getDir()).list();
  		  //System.out.println(list.toString());
  		  
  		  //meil on 4 tyypi faile ja on vaja määrata nende kuupäevad mis on kõige värskem 
  		  for(int i = 0 ; i < list.length ; i++){
  			  String name = list[i];
  			//  System.out.println(list[i]);
  			  
  			  if(name.indexOf("PARTNER")!= -1){
  				  String date = name.substring(name.indexOf("PARTNER")+9,name.indexOf("PARTNER")+17);
  				//  System.out.println(date);
  				  
  				  String ddd = (String) dates.get("PARTNER");
  				  if(ddd == null){
  					  dates.put("PARTNER", date); 
  				  }else{
  					  if(ddd.compareTo(date) <= -1){
  						  dates.put("PARTNER", date); 
  					  }
  				  }
  			  }
  			  
  			  if(name.indexOf("PRICAT")!= -1){
  				  String date = name.substring(name.indexOf("PRICAT")+8,name.indexOf("PRICAT")+16);
  				//  System.out.println(date);
  				  
  				  String ddd = (String) dates.get("PRICAT");
  				  if(ddd == null){
  					  dates.put("PRICAT", date); 
  				  }else{
  					  if(ddd.compareTo(date) <= -1){
  						  dates.put("PRICAT", date); 
  					  }
  				  }
  			  }
  			  
  			  
  			  if(name.indexOf("PRODAT")!= -1){
  				  String date = name.substring(name.indexOf("PRODAT")+8,name.indexOf("PRODAT")+16);
  				  //System.out.println(date);
  				  
  				  String ddd = (String) dates.get("PRODAT");
  				  if(ddd == null){
  					  dates.put("PRODAT", date); 
  				  }else{
  					  if(ddd.compareTo(date) <= -1){
  						  dates.put("PRODAT", date); 
  					  }
  				  }
  			  }

  			  if(name.indexOf("OPENINV")!= -1){
  				  String date = name.substring(name.indexOf("ICESMS")+6,name.indexOf("ICESMS")+14);
  				  //System.out.println(date);
  				  
  				  String ddd = (String) dates.get("OPENINV");
  				  if(ddd == null){
  					  dates.put("OPENINV", date); 
  				  }else{
  					  if(ddd.compareTo(date) <= -1){
  						  dates.put("OPENINV", date); 
  					  }
  				  }
  			  }
  			  
  			  if(name.indexOf("INVRPT")!= -1){
  				  String date = name.substring(name.indexOf("INVRPT")+8,name.indexOf("INVRPT")+16);
  				 // System.out.println(date);
  				  
  				  String ddd = (String) dates.get("INVRPT");
  				  if(ddd == null){
  					  dates.put("INVRPT", date); 
  				  }else{
  					  if(ddd.compareTo(date) <= -1){
  						  dates.put("INVRPT", date); 
  					  }
  				  }
  			  }		  
  		  }
  		//  System.out.println(dates.toString());
  	  }
  	   

  	   public static Node getNode(Node node, String name){
  	     for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
  	       Node nd = node.getChildNodes().item(i);
  	       if (nd.getNodeName().equals(name)){
  	         return nd;
  	       }
  	     }
  	     return null;
  	   }
  	   
  	   
  private String getDir(){
    String dir = parent.getProps().getProperty("readin");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    return dir;
  }
  
  private HashMap nimed = new HashMap();
  
  private HashMap payers = new HashMap();
  
  public void makeCustData(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Kliendid alustan");
	     nimed = new HashMap();
	   
	   try {
		   String[] list = new File(getDir()).list();
	 		  //System.out.println(list.toString());
	 		  
	 		  //meil on 4 tyypi faile ja on vaja määrata nende kuupäevad mis on kõige värskem 
	 		  for(int u = 0 ; u < list.length ; u++){
	 			
	 			  String fname = list[u];
	 		
	 		if(fname.indexOf("PARTNER") != -1 && fname.indexOf((String)dates.get("PARTNER")) != -1){
	 		
	 		  File f = new File (getDir() + fname);
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node rr = getNode(doc, "E-Document");     
	          Node rr1 = getNode(rr, "Document"); 
	          Node rr2 = getNode(rr1, "DocumentInfo"); 
	          Node root = getNode(rr2, "DocumentItem");   
      //  System.out.println(root);
	     
           for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	         
	         	  try{
	         		  String id =  getNode(rida,"PartnerCode").getFirstChild().getNodeValue();
	         		  Customer customer = new Customer(id);
	         		  String name =  getNode(rida,"Name").getFirstChild().getNodeValue();
	         		 nimed.put(id, name);
	         		 customer.setName((name + " " + id));
	         		  try{
	         			 String aadr =  getNode(getNode(rida,"PartnerAddress"),"Address1").getFirstChild().getNodeValue();
	         			 try{
		         			 String post =   getNode(getNode(rida,"PartnerAddress"),"PostalCode").getFirstChild().getNodeValue(); 
		         			String linn =   getNode(getNode(rida,"PartnerAddress"),"City").getFirstChild().getNodeValue();  
			         		 aadr = aadr + " " +((post + "," +linn ));  	  
		         		  }catch(Exception ea){
		         		  }
	         			 
		         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr), "Estonia"));
		         		  
	         		  }catch(Exception e){
	         			  
	         		  }
	         		
	         	     try{
  		    	 	 String contacname =  getNode(rida, "ContactFirstName").getFirstChild().getNodeValue()   ;
	         			 if(contacname.trim().length() >0){
			         		  customer.setPersonalData((contacname)); 
	         			 }  	  
	         		  }catch(Exception e){}
	         		  
	         		   try{
		         			 String contacname =  getNode(rida, "ContactLastName").getFirstChild().getNodeValue()   ;
		         			 if(contacname.trim().length() >0){
		         				 if(customer.getPersonalData() != null){
					         		  customer.setPersonalData(customer.getPersonalData() + " " + (contacname));  
			         			 }else{
					         		  customer.setPersonalData((contacname)); 
			         			 } 	  
		         			 }  	  
		         		  }catch(Exception e){ }
		         		  
	         		     try{
		         			 String phone =  getNode(rida,"PhoneNumber").getFirstChild().getNodeValue(); 
		         			 if(customer.getPersonalData() != null){
				         		  customer.setPersonalData(customer.getPersonalData() + " " + (phone));  
		         			 }else{
				         		  customer.setPersonalData((phone)); 
		         			 } 	  
		         		  }catch(Exception e){
		         		  }
	         		  

		         		
		         		    
	         		 try{  String paym =  getNode(getNode(rida,"ContractInfo"),"PaymentTerm").getFirstChild().getNodeValue();
	         		  customer.setPaymTerm((paym));
                      }catch(Exception e){ }
                     
                      
                  try{ String crlim =  getNode(getNode(rida,"ContractInfo"),"CreditLimit").getFirstChild().getNodeValue();
     	         		  customer.setCredit(crlim);
                              }catch(Exception e){  } 
                      
                              
                  try{ String salesman  =  getNode(rida,"UserID").getFirstChild().getNodeValue();
	         		      salesman = salesman.substring(0,salesman.indexOf(":"));
	         		      customer.addAgent(new Agent((salesman)));
                      }catch(Exception e){ }
	         	   
	           		      
	                  try{ String payer  =  getNode(rida,"PayerPartnerCode").getFirstChild().getNodeValue();
	                       payers.put(customer.getID(), payer);
		         		      customer.setPayer((payer));	 
		         		     customer.setGroup((payer));	
		         	   }catch(Exception e){ }
		         	   
		         	//  -<PartnerBalanceInfo> <OutstandingAmount>333.72</OutstandingAmount> <OverdueAmount>333.72</OverdueAmount>
		         	   
		         	   try{ String over  =  getNode(getNode(rida,"PartnerBalanceInfo"),"OverdueAmount").getFirstChild().getNodeValue();
			         		      customer.setDebt(Double.parseDouble(over));
			         	   }catch(Exception e){}
			         	   
			          try{ String used  =  getNode(getNode(rida,"PartnerBalanceInfo"),"OutstandingAmount").getFirstChild().getNodeValue();
	         		      customer.setUsedCredit(Double.parseDouble(used));
	         	         }catch(Exception e){}	   
			        
	         	         
	         	         boolean goes = true;
			          try{ String blocked  =  getNode(rida,"Blocked").getFirstChild().getNodeValue();
				         		
				         		 if(blocked.equals("1")){
				         		      customer.setSalesBlock(true); 
				         		      goes = false;
				         		  }
				         	   }catch(Exception e){}
				         	   
	         	   if(goes){
	         		  tm.put(customer.getName(), customer);
	                   
	         	   }
	         		      
	         	  }catch(Exception e){
	         		  
	         	  }
	         	    
	         }    
	         }    
		         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   
	    
	 	
	 	
	 
	    System.out.println("Kliendid tehtud");
	    

}
  
  
  private void makeProdInfo(){
      try {
          
   	   String[] list = new File(getDir()).list();
 		  //System.out.println(list.toString());
 		  
 		  //meil on 4 tyypi faile ja on vaja määrata nende kuupäevad mis on kõige värskem 
 		  for(int u = 0 ; u < list.length ; u++){
 			
 			  String fname = list[u];
 		
 		if(fname.indexOf("PRODAT") != -1 && fname.indexOf((String)dates.get("PRODAT")) != -1){
 		
 		  File f = new File (getDir() + fname);
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream inx = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(inx,handler);
         inx.close();
         
         Node rr = getNode(doc, "E-Document");     
         Node rr1 = getNode(rr, "Document"); 
         Node root = getNode(rr1, "DocumentItem");
               
       if(root.getChildNodes().getLength() > 0 ){
     	     for (int i = 0; i < root.getChildNodes().getLength(); i++){
            	  Node rida = root.getChildNodes().item(i);
            	  
            	if(rida.getNodeName().equals("ItemEntry")){
            	
           		 boolean goes = true;
           	     String id = getNode(rida,"SellerItemCode").getFirstChild().getNodeValue();
           		   Product prod = new Product(id);
           		  
           		 try{
        	    		 String grupp = getNode(rida, "ItemGroupCode").getFirstChild().getNodeValue();
        	    		 //this.prGroups.put(grupp, grupp);
        	    	     prod.setGroup(grupp.substring(0,8));
        	    	     prod.setGroupSecond(grupp);
        	    	 // prod.setGroupSecond(grupp.substring(0,1) + grupp);
        	    	  	   
        	    	 }catch (Exception e){
        	    		 
        	   	 }
           		      		   
           		 
        	      try{
           		 String ean   = getNode(rida,"GTIN").getFirstChild().getNodeValue();
           	     prod.setEAN(ean);
       	      }catch(Exception e ){}
       	      
       	      
        	    	 
         	      try{
         	    	  String name   = getNodeAttr(rida,"ItemDescription","LV").getFirstChild().getNodeValue();
         	          prod.setName((name) + " " + prod.getID()); 
     
          	   
         	      }catch(Exception e ){     	    	
         	    	  System.out.println( " id " + prod.getID());
         	    	  try{
         	    		 String name   = getNodeAttr(rida,"ItemDescription","EN").getFirstChild().getNodeValue();
            	          prod.setName("    " + (name) + " " +  prod.getID());   
         	    	  }catch(Exception h){
         	    		  
         	    	  }
         	    	  
         	      }              
         	 
    	 
     	      //    prod.setStock(Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
         	 	 
         	    if(goes && prod.getName()!= null){
         	         prods.put(prod.getID(), prod);	
         	       
         	    }
     	     }
             } 
       }
    	 
    	 
    	 
    }}
     } catch (Exception e){
       e.printStackTrace();
     }
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi Määramine: " );
  	 
  	 if(id.length() > 10){
  		 id = id.substring(id.length()-10);
  	 }
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010200 where SL01106  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

 
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010200 left JOIN SY240200  ON SL01036=SY24002 " +
 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
 	
   System.out.println(sql1);
   String agent = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();

   return agent;

 }
 
  
  

 
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }


  
  public void makeOrder(Order order) throws SQLException{

	  if (signatures == null){
   	   signatures = new Vector();
      }
	  counter = 0;
	   trouble = "Imoprdin tellimust " + order.getAgent() + "." + order.getDokNr() + ".txt aga SAP faili ei tekkinud ";
		
	 Order ord =  order;	
	 try{
		  java.util.Collections.sort(ord.getOrderLines(), new java.util.Comparator(){
				 
		        public boolean equals(Object obj) {
		          return false;
		        }

		       public int compare(Object o1, Object o2) {
		          OrderLine ol1 = (OrderLine)o1;
		          OrderLine ol2 = (OrderLine)o2;
		          return ol1.getTooteNimi().compareTo(ol2.getTooteNimi());
		        }
		           }); 
	 }catch(Exception r ){
		 log.error(r);
	 }
		
   		   
     			   
     			  String signature = order.getSignature();
     			  if(signatures.contains(signature)){
     				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
     				    		  
     			  }else{
     			        ord.setTeostaja(order.getTeostaja());
     			        
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
     			        
          			   makeOutput(ord,tagastus);
          			   addToDB(ord,(String)nimed.get(ord.getCompany()));
     	    	   signatures.add(ord.getSignature());
     			  }
     	    	 
     			   try {
     					saveObject(signatures, "signatures.vec");
     				} catch (IOException e) {
     					e.printStackTrace();
     				}
     			   
     			   
     			   
      trouble = null;
      setNumAddedLines(counter);
  }
  

	private void  addToDB(Order ord , String name ){


 
		    String sql1 = "   insert into orderheaders (agentid,custid,custname,madedate,lineamount,summa) values " +
	 "  ('" +ord.getAgent() + "','" + ord.getCompany()+ "','" +name + "',getdate()," + ord.getOrderLines().size() + ",0)";
		  
		    Statement st1;
		    Connection con = this.getCon();
			try {
				st1 = con.createStatement();
				st1.executeUpdate(sql1);
				st1.close();
				con.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
}

  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	

  
  
  
	

  private String getNimi(String aa) throws SQLException{
  	String res = "noname";
   if(prods.get(aa) != null){
	   res = ((Product)prods.get(aa)).getName();
   }
  	return res ;
  }
  
  private String getEan(String aa) throws SQLException{
	  	String res = "0000000000000";
	  	 if(prods.get(aa) != null){
	  	   res = ((Product)prods.get(aa)).getEAN();
	     }
	  	return res ;
	  }
  
  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01015 + SC01001 AS sort FROM SC010200 pr  where pr.SC01001='" + prodID + "'";
	  log.error("1_LINE_SQL: " + sql1);

	    String sort = "000";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	sort = rs1.getString("sort");
	    //	System.out.println("sort käes 1 ;" + sort );
	    	if(sort == null || sort.trim().length() == 0){
	    		sort = "xxx";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// System.out.println("Sort käes 2 ;" + sort );
	    return sort;
	  }
  
  private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.YEAR)+"").substring(2));
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day);

	    return sb.toString();
	  }
	  
  private String getStampString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.YEAR)+""));
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append(month + "");

	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day + "0000");

	    return sb.toString();
	  }
  private String getTimeString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    sb.append((cal.get(Calendar.HOUR_OF_DAY)+""));
	    String min = String.valueOf(cal.get(Calendar.MINUTE));
	    if (min.length() == 1){
	      sb.append("0");
	    }
	    sb.append(min + "");
	    return sb.toString();
	  }
	  
  
 private void makeOutput(Order order, boolean tagastus){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
String regnum = "10232094";

if( parent.getProps().getProperty("regnum")!= null){
	regnum = parent.getProps().getProperty("regnum");//"40003244865";
}

 	try {
 	  int cou = 0;
		String ref = order.getReference();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getInfo();
		if ( com == null ){
			com =  " ";
		}		
	
		
		 String doknr = order.getDokNr();
		  System.out.println("Doknr0" + doknr);
		 
		 if(order.getTeostaja() != null && order.getTeostaja().contains("|") ){
			  doknr = order.getTeostaja();
		  }
		 
		  if(doknr.startsWith("|")){
			  doknr = doknr.substring(1);         		 
			 }
		  
		  if(doknr.indexOf("|") != -1){
			  doknr = doknr.substring(0,doknr.indexOf("|"));         		 
			 }
		  
		   System.out.println("Info " +order.getInfo());
	        System.out.println("Teostaja " +order.getTeostaja());
		  System.out.println("Doknr " + doknr);
		 

         String ls = System.getProperty("line.separator");
         String fixagent = order.getAgent();       
     
         String custID = order.getCompany();
         String ladu = null;
             Calendar  shipDate = order.getShipCal();
             
             String code = order.getWarehouse();        
             if (code != null){
               ladu = code;
             }
           
           if (ladu == null) {
             ladu = "";
           }
  
           String agent = order.getAgent();
           
           if(fixagent != null){
         	  agent = fixagent;
           }
    
           Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
           if(ag != null){
          	int aa = ag.getName().indexOf(" ");
        	  agent = ag.getName().substring(0,aa).trim();
          }
         agent = agent.toLowerCase();
         agent = Converter.cleanLitLatEst(agent);
         if(agent.equals("null")){
        	 agent = "agentX";
         }
         
           Calendar cal = Calendar.getInstance();
           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
        	   cal.add(Calendar.DAY_OF_YEAR, 3);  
           }else{
        	   cal.add(Calendar.DAY_OF_YEAR, 1);  
           }
           
          
           if (shipDate != null) {
                 cal = shipDate;
               }
         
           sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + ls);
           sb.append("<EDIFACT xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"H:\\ORDERS_D96A.xsd\">" + ls);
         
      
           sb.append("<UNB>" + ls);
             sb.append("<UNB01-SyntaxIdentifier> " + ls +
		      "<UNB0101-SyntaxIdentifier>UNOC</UNB0101-SyntaxIdentifier>" + ls +
		      "<UNB0102-SyntaxVersionNumber>1</UNB0102-SyntaxVersionNumber>" + ls +
		      "</UNB01-SyntaxIdentifier>" + ls +
		      "<UNB02-InterchangeSender>" + ls +
		      "<UNB0201-SenderIdentification>10584597</UNB0201-SenderIdentification>" + ls +
		      "<UNB0202-PartnerIdentificationCodeQualifier>91</UNB0202-PartnerIdentificationCodeQualifier>" + ls +
		      "</UNB02-InterchangeSender>"); 
		     sb.append(" <UNB03-InterchangeRecipient><UNB0301-RecipientIdentification>" + regnum + "</UNB0301-RecipientIdentification>" + ls +
              "<UNB0302-PartnerIdentificationCodeQualifier>91</UNB0302-PartnerIdentificationCodeQualifier>" + ls+
              "</UNB03-InterchangeRecipient>" + ls);
             sb.append("<UNB04-DateTimeOfPreparation>" + ls +
           		   "<UNB0401-DateOfPreparation>"+ escape(getDateString(Calendar.getInstance())) +"</UNB0401-DateOfPreparation>" + ls +
           		   "<UNB0402-TimeOfPreparation>" + escape(getTimeString(Calendar.getInstance())) + "</UNB0402-TimeOfPreparation>" + ls +
           		   "</UNB04-DateTimeOfPreparation>" + ls);  
             String alus =  Calendar.getInstance().getTimeInMillis() + "";
             sb.append("<UNB05-InterchangeControlReference>" + alus + "</UNB05-InterchangeControlReference>" + ls); 	
           sb.append("</UNB>" + ls);
           
           
           
           
           sb.append("<ORDERS>" + ls + "<UNH>" + ls);
           
           sb.append("<UNH01-MessageReferenceNumber>" + alus + "</UNH01-MessageReferenceNumber>" + ls);
           sb.append("<UNH02-MessageIdentifier>" + ls);
           sb.append(  "<UNH0201-MessageType>ORDERS</UNH0201-MessageType>" + ls +
			           "<UNH0202-MessageVersionNumber>D</UNH0202-MessageVersionNumber>" + ls +
			           "<UNH0203-MessageReleaseNumber>96A</UNH0203-MessageReleaseNumber>" + ls +
			           "<UNH0204-ControllingAgency>UN</UNH0204-ControllingAgency>" + ls +
			           "<UNH0205-AssociationAssignedCode>EAN008</UNH0205-AssociationAssignedCode>" + ls );
		   sb.append("</UNH02-MessageIdentifier>" + ls);
           sb.append("</UNH>" + ls);
           
           sb.append("<BGM>" + ls);
           sb.append("<BGM01-DocumentMessageName>" + ls);
           String aa = "220";
           if(tagastus) {
        	   aa = "22R";
           }
           sb.append("<BGM0101-DocumentMessageNameCoded>" + aa + "</BGM0101-DocumentMessageNameCoded>" + ls);
           sb.append("<BGM0103-CodeListResponsibleAgencyCoded>ZZZ</BGM0103-CodeListResponsibleAgencyCoded>" + ls);
           sb.append("</BGM01-DocumentMessageName>" + ls);
           if(agent.length() > 5){
        	   agent = agent.substring(0,5);
           }
           String nummer = "MS" + agent + "" + (Calendar.getInstance().getTimeInMillis()+ "").substring(5);
           if(nummer.length()>20){
        	   nummer = nummer.substring(0,20);
           }
           
  boolean has = true;
           if(order.getTeostaja() != null && order.getTeostaja().trim().length() != 0 && 
        		   !order.getTeostaja().trim().equals("null") && order.getTeostaja().indexOf("|") != -1 ){
        	   nummer =  doknr;
        	   has = false;
        	   if(nummer.length()>20){
            	  nummer = nummer.substring(0,20);
               }       	   
           }
           
           if(order.getDokNr() != null && order.getDokNr().trim().length() != 0 && has){
        	   if(!order.getDokNr().trim().equals("null") ){
        		   nummer = "MS" + agent + "" + order.getDokNr() + (Calendar.getInstance().get(Calendar.MILLISECOND)+"");
            	   if(nummer.length()>20){
                	   nummer = nummer.substring(0,20);
                   }      
        	   }
           }
           
           System.out.println("nummer " + nummer);
           
           if(nummer.indexOf("null") != -1){
        	   nummer = "MS" + agent + "" + (Calendar.getInstance().getTimeInMillis()+ "").substring(5); 
           }
           
           sb.append("<BGM02-DocumentMessageNumber>" + nummer + "</BGM02-DocumentMessageNumber>" + ls);
           sb.append("</BGM>" + ls);
     
           
           sb.append("<DTM>" + ls);
           sb.append("<DTM01-DateTimePeriod>" + ls);
           sb.append("  <DTM0101-DateTimePeriodQualifier>137</DTM0101-DateTimePeriodQualifier>" + ls);
           sb.append(" <DTM0102-DateTimePeriod>" + getStampString(Calendar.getInstance())+ "</DTM0102-DateTimePeriod>" + ls);
           sb.append("  <DTM0103-DateTimePeriodFormatQualifier>203</DTM0103-DateTimePeriodFormatQualifier>" + ls);
           sb.append(" </DTM01-DateTimePeriod>" + ls);
           sb.append("</DTM>" + ls);
           sb.append("<DTM>" + ls);
           sb.append(" <DTM01-DateTimePeriod>" + ls);
           sb.append("  <DTM0101-DateTimePeriodQualifier>4</DTM0101-DateTimePeriodQualifier>" + ls);
           sb.append("  <DTM0102-DateTimePeriod>"+ getStampString(Calendar.getInstance())+"</DTM0102-DateTimePeriod>" + ls);
           sb.append("  <DTM0103-DateTimePeriodFormatQualifier>203</DTM0103-DateTimePeriodFormatQualifier>" + ls);
           sb.append("</DTM01-DateTimePeriod>" + ls);
           sb.append("</DTM>" + ls);
           sb.append("<DTM>" + ls);
           sb.append(" <DTM01-DateTimePeriod>"+ ls);
           sb.append("  <DTM0101-DateTimePeriodQualifier>2</DTM0101-DateTimePeriodQualifier>" + ls);
           sb.append("<DTM0102-DateTimePeriod>"+ getStampString(cal)+"</DTM0102-DateTimePeriod>" + ls);
           sb.append(" <DTM0103-DateTimePeriodFormatQualifier>203</DTM0103-DateTimePeriodFormatQualifier>" + ls);
           sb.append(" </DTM01-DateTimePeriod>" + ls);
           sb.append("</DTM>" + ls);
        
           
           if(com.trim().length() > 0 ){
        	   sb.append("<FTX>" + ls);
               sb.append(" <FTX01-TextSubjectQualifier>AAI</FTX01-TextSubjectQualifier>"+ ls);
               sb.append("  <FTX04-TextLiteral>" + ls);
               sb.append("  <FTX0401-FreeText>" + escape(com.replaceAll("&", " and ")) + "</FTX0401-FreeText>"+ ls);
               sb.append("</FTX04-TextLiteral>" + ls);  
               sb.append("</FTX>" + ls);  
           }
           
        
          
      
           String payer = (String)payers.get(custID);
           if(payer == null){
        	   payer = custID;
           }
           
           sb.append("<GROUP_2>" + ls);
           sb.append(" <NAD>" + ls);
           sb.append("  <NAD01-PartyQualifier>BY</NAD01-PartyQualifier>" + ls);
           sb.append("  <NAD02-PartyIdentificationDetails>" + ls);
        //   sb.append("    <NAD0201-PartyIdIdentification>" + payer + "</NAD0201-PartyIdIdentification>" + ls);
           sb.append("    <NAD0201-PartyIdIdentification>" + custID + "</NAD0201-PartyIdIdentification>" + ls);
           
           sb.append("    <NAD0203-CodeListResponsibleAgencyCoded>91</NAD0203-CodeListResponsibleAgencyCoded>" + ls);
           sb.append("  </NAD02-PartyIdentificationDetails>" + ls);
           sb.append("</NAD>" + ls);
           sb.append("</GROUP_2>" + ls);
           sb.append("<GROUP_2>" + ls);
           sb.append("<NAD>" + ls);
           sb.append(" <NAD01-PartyQualifier>SU</NAD01-PartyQualifier>" + ls);
           sb.append("<NAD02-PartyIdentificationDetails>" + ls);
           sb.append(" <NAD0201-PartyIdIdentification>"+regnum + "</NAD0201-PartyIdIdentification>" + ls);
           sb.append("  <NAD0203-CodeListResponsibleAgencyCoded>91</NAD0203-CodeListResponsibleAgencyCoded>" + ls);
           sb.append(" </NAD02-PartyIdentificationDetails>" + ls);
           sb.append("  <NAD04-PartyName>" + ls);
           sb.append("    <NAD0401-PartyName>ALTIA EESTI AS</NAD0401-PartyName>" + ls);
           sb.append(" </NAD04-PartyName>" + ls);
           sb.append(" </NAD>" + ls);
          /* sb.append("<GROUP_5>" + ls);
           sb.append(" <CTA>" + ls);
           sb.append("    <CTA01-ContactFunctionCoded>SR</CTA01-ContactFunctionCoded>" + ls);
           sb.append("    <CTA02-DepartmentOrEmployeeDetails>" + ls);
           sb.append("      <CTA0201-DepartmentOrEmployeeIdentification>" + agent + "@altia</CTA0201-DepartmentOrEmployeeIdentification>" + ls);
           sb.append("    </CTA02-DepartmentOrEmployeeDetails>" + ls);
           sb.append("  </CTA>" + ls);
           sb.append("</GROUP_5>" + ls);*/
           sb.append("</GROUP_2>" + ls);
       
           
           
           
           
           sb.append("   <GROUP_2>" + ls);
           sb.append("  <NAD>" + ls);
           sb.append("    <NAD01-PartyQualifier>DP</NAD01-PartyQualifier>" + ls);
           sb.append("    <NAD02-PartyIdentificationDetails>" + ls);
           sb.append("      <NAD0201-PartyIdIdentification>" + custID + "</NAD0201-PartyIdIdentification>" + ls);
           sb.append("      <NAD0203-CodeListResponsibleAgencyCoded>91</NAD0203-CodeListResponsibleAgencyCoded>" + ls);
           sb.append("    </NAD02-PartyIdentificationDetails>" + ls);
           sb.append("    <NAD04-PartyName>" + ls);
           sb.append("     <NAD0401-PartyName>name not needed</NAD0401-PartyName>" + ls);
           sb.append("   </NAD04-PartyName>" + ls);
           sb.append("  </NAD>" + ls);
        /*   sb.append("  <GROUP_5>" + ls);
           sb.append("   <CTA>" + ls);
           sb.append("     <CTA01-ContactFunctionCoded>EB</CTA01-ContactFunctionCoded>" + ls);
           sb.append("    <CTA02-DepartmentOrEmployeeDetails>" + ls);
           sb.append("      <CTA0201-DepartmentOrEmployeeIdentification>" + agent + "@altia</CTA0201-DepartmentOrEmployeeIdentification>" + ls);
                 sb.append("    </CTA02-DepartmentOrEmployeeDetails>" + ls);
                 sb.append("  </CTA>" + ls);
                 sb.append(" </GROUP_5>" + ls);*/
                 sb.append(" </GROUP_2>" + ls);
         
           
    
       /*    if(ref != null && ref.trim().length() > 0){
         	   sb.append("<RefInfo>" + ls);
               sb.append("<SourceDocument type=\"order\">" + ls);
               sb.append("    <SourceDocumentNum>" + escape(ref) + "</SourceDocumentNum>" + ls );
               sb.append("</SourceDocument>" + ls);
               sb.append("</RefInfo>" + ls); 
           }*/

           

           
           Vector sortedLines = order.getOrderLines();
           for (int i = 0; i < sortedLines.size(); i++) {
               OrderLine line = (OrderLine)sortedLines.elementAt(i);
               double ale = line.getDiscount();     
               counter++;
               String ean = line.getEAN();
               String yhik = line.getUnit();
               if (ale == -1 ){
                   ale = 0 ;
                   }
               
               
               if( ean==null || ean.length() < 6 ) {
                   ean = getEan(line.getToode());
                  }
             
               sb.append(" <GROUP_25>" + ls);
               sb.append(" <LIN>" + ls);
               sb.append("   <LIN01-LineItemNumber>"  + (i+1) + "</LIN01-LineItemNumber>" + ls);
               sb.append("  <LIN03-ItemNumberIdentification>" + ls);
               sb.append("    <LIN0301-ItemNumber>" + ean + "</LIN0301-ItemNumber>" + ls);
               sb.append("   <LIN0302-ItemNumberTypeCoded>EN</LIN0302-ItemNumberTypeCoded>" + ls);
               sb.append("  </LIN03-ItemNumberIdentification>" + ls);
               sb.append(" </LIN>" + ls);
               sb.append(" <PIA>" + ls);
               sb.append("  <PIA01-ProductIdFunctionQualifier>5</PIA01-ProductIdFunctionQualifier>" + ls);
               sb.append("  <PIA02-ItemNumberIdentification>" + ls);
               sb.append("    <PIA0201-ItemNumber>" + line.getToode() + "</PIA0201-ItemNumber>" + ls);
               sb.append("    <PIA0202-ItemNumberTypeCoded>SA</PIA0202-ItemNumberTypeCoded>" + ls);
               sb.append("    <PIA0204-CodeListResponsibleAgencyCoded>91</PIA0204-CodeListResponsibleAgencyCoded>" + ls);
               sb.append("  </PIA02-ItemNumberIdentification>" + ls);
               sb.append(" </PIA>" + ls);
               sb.append(" <IMD>" + ls);
               sb.append("  <IMD01-ItemDescriptionTypeCoded>F</IMD01-ItemDescriptionTypeCoded>" + ls);
               sb.append("  <IMD02-ItemCharacteristicCoded>8</IMD02-ItemCharacteristicCoded>" + ls);
               sb.append("   <IMD03-ItemDescription>" + ls);
               sb.append("   <IMD0304-ItemDescription>" +cleanLitLatEst(escape(getNimi(line.getToode()))) + "</IMD0304-ItemDescription>" + ls);
               sb.append(" </IMD03-ItemDescription>" + ls);
               sb.append("</IMD>" + ls);
               sb.append(" <QTY>" + ls);
               sb.append("   <QTY01-QuantityDetails>" + ls);
               sb.append("    <QTY0101-QuantityQualifier>21</QTY0101-QuantityQualifier>" + ls);
               sb.append("    <QTY0102-Quantity>" + escape(getNF().format(line.getKogus())) + "</QTY0102-Quantity>" + ls);
               if(yhik.equals("PCS")){
            	   sb.append("   <QTY0103-MeasureUnitQualifier>PCE</QTY0103-MeasureUnitQualifier>" + ls);
            	 //  sb.append("   <QTY0103-MeasureUnitQualifier>" + escape(yhik) + "</QTY0103-MeasureUnitQualifier>" + ls);
               }
              
               sb.append("  </QTY01-QuantityDetails>" + ls);
               sb.append("</QTY>" + ls);
               
               if(tagastus){
            	   sb.append("<FTX>" + ls);
                   sb.append(" <FTX01-TextSubjectQualifier>ZZZ</FTX01-TextSubjectQualifier>"+ ls);
                   sb.append("  <FTX04-TextLiteral>" + ls);
                   sb.append("  <FTX0401-FreeText>503</FTX0401-FreeText>"+ ls);
                   sb.append("</FTX04-TextLiteral>" + ls);  
                   sb.append("</FTX>" + ls);  
               }
               
               sb.append(" <GROUP_28>" + ls);
               sb.append("  <PRI>" + ls);
               sb.append("   <PRI01-PriceInformation>" + ls);
               sb.append("      <PRI0101-PriceQualifier>AAF</PRI0101-PriceQualifier>" + ls);
               double price = line.getPrice();
               if(line.getDiscount() > 0){
            	   price = price /((100-line.getDiscount())/100);
               }
               if(hinnad.get(line.getToode()) != null){
            	   price = Double.parseDouble((String)hinnad.get(line.getToode()));
               }
              
               
               sb.append("     <PRI0102-Price>" + nf.format(price) + "</PRI0102-Price>" + ls);
               sb.append("    </PRI01-PriceInformation>" + ls);
               sb.append("  </PRI>" + ls);
               sb.append("  <PRI>" + ls);
               sb.append("    <PRI01-PriceInformation>" + ls);
               String dim = "AAA";
               if(line.getDiscount() > 0){         	 
              	  dim = "AAE";
                }   
               if(hinnad.get(line.getToode()) != null){
            	   price = Double.parseDouble((String)hinnad.get(line.getToode()));
            	   price = price * (100-line.getDiscount())/100;
               }
              
               sb.append("      <PRI0101-PriceQualifier>" + dim + "</PRI0101-PriceQualifier>" + ls); //AAE
             
               sb.append("     <PRI0102-Price>" + nf.format(price) + "</PRI0102-Price>" + ls);
               sb.append("   </PRI01-PriceInformation>" + ls);
               sb.append("  </PRI>" + ls);
               sb.append("</GROUP_28>" + ls);
               sb.append("</GROUP_25>" + ls);
                
                
           }
                
     	    String comm = order.getInfo();
             if(comm != null && comm.length()>100){
             	comm = comm.substring(0,100);
             }
             
             
           sb.append("</ORDERS>" + ls);
           sb.append("</EDIFACT>" + ls);
 
      counter = counter + cou;
    	//
      String fn = doknr;
      if(fn.equals("null") || doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String country = parent.getProps().getProperty("country");
      //läti tarbeks pne parameeter LVORDERS_CUMS 
      String ette = "ESORDERS_CUMS";
      if(country != null){
    	  ette = country;
      }
      
      String fileName = escape(parent.getProps().getProperty("conv.dir") + ette + agent)  + "." + fn  +  ".xml";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();
        File f = new File(fileName);
        if(f.exists()){
        	log.error(fn  + " tellimus failis " + fileName);
        }else{
        	log.error(fn  + " tellimust POLE failis " + fileName);
        }
       
	} catch (Exception e) {
		e.printStackTrace();
		
	log.error(e);
	}
 }
 
 
	public NumberFormat getNF() {
		return this.nf;
	}
	
	private HashMap	hinnad = new HashMap();
	
	   public Vector getPriceMatrixData() {

		   	Vector lines = new Vector();

		  
		   	
			
		 	try {

			   	   String[] list = new File(getDir()).list();
					
			      	hinnad = new HashMap();
					  for(int u = 0 ; u < list.length ; u++){
						
						  String fname = list[u];
						
					if(fname.indexOf("PRICAT") != -1 && fname.indexOf((String)dates.get("PRICAT")) != -1){
						//  System.out.println("File" + getDir() + fname);
					
					  File f = new File (getDir() + fname);
			 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			 	     DocumentBuilder db;
			 		db = dbf.newDocumentBuilder();      	
			     	FileInputStream in = getCleanInputStream(f);
			     	String handler = null;
			         Document doc = db.parse(in,handler);
			         Node root  = getNode(doc, "n0:PDA_Prices");     
			      //   System.out.println(root);
			     //	System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
			         for (int i = 0; i < root.getChildNodes().getLength(); i++){

			         	  Node rida = root.getChildNodes().item(i); 
			       //  	 System.out.println("ridu  " + rida.getNodeName());
			         	  if(rida.getNodeName().equals("PDA_Price")){
			         		 
			         			 
				         				MatrixLine mline = new MatrixLine();

				         			    double price = 0;
				         				String custID = null;
				         				String clGroup = null;
				         				String prodID = null;
				         				String discGroup = null;
				         				String prodGroup = null;

				         				double discount = 0;
				         				String start = null;
				         				String end = null;
				         				 Node item = getNode(rida,"Material");
				         				
				         				try{
				         					prodID  = item.getFirstChild().getNodeValue();	         					
				         				}catch(Exception e){
				         					//e.printStackTrace();
				         				}
				         				try{
				         					prodGroup  = getNode(rida,"ProductGroupCode").getFirstChild().getNodeValue();
				         				   if(prodGroup.equals("01001004B")){
				         					  prodGroup = "0100104B";
				         		    	   }
				         		    	   
				         		    	   
				         		    	   if(prodGroup.startsWith("01003002")){
				         		    		  prodGroup = prodGroup.replaceAll("01003002", "01003001");
				         		    		  
				         		   		   }
				         		   		 if(prodGroup.startsWith("01003003")){
				         		   			prodGroup = prodGroup.replaceAll("01003003", "01003001");
				         		   			
				         				   }
				         		   		
				         		    	
				         				}catch(Exception e){
				         					//e.printStackTrace();
				         				}
				         				
				         				try{
				         					end  = getNode(rida,"PriceEndDate").getFirstChild().getNodeValue();	         					
				         				}catch(Exception e){
				         					
				         					e.printStackTrace();
				         				}
				         				try{
				         					start  = getNode(rida,"PriceStartDate").getFirstChild().getNodeValue();	         					
				         				}catch(Exception e){
				         					
				         					e.printStackTrace();
				         				}
				         				try{
				         					 price  = Double.parseDouble(getNode(rida,"Price").getFirstChild().getNodeValue());		         					
				         				}catch(Exception e){
				         					//System.out.println("id " +prodID);
				         					//e.printStackTrace();
				         				}
				         				
				         				try{
				         					 discount  = Double.parseDouble(getNode(rida,"Discount").getFirstChild().getNodeValue());		         					
				         				}catch(Exception e){
				         					//e.printStackTrace();
				         				}
				         				boolean goes = true ;
				         				try{
				         					clGroup  = getNode(rida,"Payer").getFirstChild().getNodeValue();		         					
				         				}catch(Exception e){
				         					hinnad.put(prodID.trim(), price + "");
				         					goes = false;
				         					//System.out.println(prodID  + " " + price);
				         					//e.printStackTrace();
				         				}
				         				
				         				  if(prodID == null && prodGroup == null){
				         					
				         					  goes = false;
				         				  }
				         				
				         				if (custID != null && custID.trim().length() > 0) {
				         					mline.setCustID(custID.trim());
				         				}
				         				if (mline.getCustID() == null && clGroup != null
				         						&& clGroup.trim().length() > 0) {
				         					mline.setCliGroup(clGroup.trim());
				         				}

				         				if (prodID != null && prodID.trim().length() > 0) {
				         					mline.setProdID(prodID.trim());
				         				}
				         				if (mline.getProdID() == null && discGroup != null
				         						&& discGroup.trim().length() > 0) {
				         					mline.setDiscGroup(discGroup);
				         					
				         				}
				         				
				         				if (mline.getProdID() == null && prodGroup != null
				         						&& prodGroup.trim().length() > 0) {
				         					mline.setProdGroup(prodGroup);
				         				}
				         				
				         				if(start != null){
				         					mline.setStartDate(start);
				         				}
		                                if(end != null && end.indexOf("9999") == -1){
		                                	mline.setEndDate(end);
				         				}
		                                if(end.indexOf("9999") != -1){
		                              
		                                }
		                                
				         				if (price != 0) {
				         					mline.setPrice(price);
				         				}
				         				
				         				
				         				if(discount >0){
				         					mline.setDiscount(discount);
				         					mline.setPrice(0);
				         				}
				         			

				                    if(goes)     				
				         				lines.add(mline);				         			
				         				  
			         		  }
			                   }}  
			         	  }
			         }catch(Exception t){
			        	 t.printStackTrace();
			         }

		 //System.out.println(this.hinnad);
			return lines;

		   }

 
 
  public String getDriverVersion(){
    return "AltiaLATImport";
  }



  private String escape(String s){
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



  public static final String addSpaces(String s, int totalLen){
    int len = s.length();
    StringBuffer sb = new StringBuffer(s);
    for (int i=len; i < totalLen ; i++){
      sb.append(' ');
    }
    if( len > totalLen){
    	return s.substring(0,totalLen);
    }
    return sb.toString();
  }
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();

  public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	      
	        ret = ret.replaceAll("ė","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');
	        ret = ret.replaceAll("Į","I");
	        ret = ret.replaceAll("ų","u");
	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("ž","z");
	        ret = ret.replaceAll("š","s");
	        ret = ret.replaceAll("Š","S");
	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("Į","I");
	        ret = ret.replaceAll("ū","u");
	        ret = ret.replaceAll("į","i");
	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("Ž","Z");
	        ret = ret.replaceAll("Ė","E");
	        ret = ret.replaceAll("Ų","U");
	        ret = ret.replaceAll("Ū","U");
	        ret = ret.replaceAll("§","Pg.");
	        
	        ret = ret.replaceAll("Ž","Z");
	        ret = ret.replaceAll("Ā","A");
	        ret = ret.replaceAll("ā","a");
	        ret = ret.replaceAll("Č","C");

	        ret = ret.replaceAll("č","c");
	        ret = ret.replaceAll("Ē","E");
	        ret = ret.replaceAll("ē","e");
	        ret = ret.replaceAll("Ģ","G");
	        ret = ret.replaceAll("ģ","g");
	        ret = ret.replaceAll("Ī","I");
	        ret = ret.replaceAll("ī","i");
	        ret = ret.replaceAll("Ķ","K");
	        ret = ret.replaceAll("ķ","k");
	        ret = ret.replaceAll("Ļ","L");
	        ret = ret.replaceAll("ļ","l");
	        ret = ret.replaceAll("Ņ","N");
	        ret = ret.replaceAll("ņ","n");
	        
	        ret = ret.replaceAll("õ","o");
	        ret = ret.replaceAll("ä","a");
	        ret = ret.replaceAll("ö","o");
	        ret = ret.replaceAll("ü","u");
	       
	        ret = ret.replaceAll("Õ","O");
	        ret = ret.replaceAll("Ä","A");
	        ret = ret.replaceAll("Ö","O");
	        ret = ret.replaceAll("Ü","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	System.out.println(ret);
	        return ret;
	        }        
	      }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private int counter = 0;
  private ImportPanel parent;
  private String trouble = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
}
