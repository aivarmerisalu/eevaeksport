package eevaeksport.imp.driver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

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

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.Customer;
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

public class S1XMLRadexImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public S1XMLRadexImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");

  }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  private HashMap prodNames() {
	  HashMap res = new HashMap();
	  
	 	try{
	 	    String sql2 = " SELECT [No_] AS toode,([Search Description]) AS nimi  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure] " + 
	 	    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) ";
	 	    System.out.println(sql2);

	 	   Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	 	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

	 	    Statement st2 = con.createStatement();
	 	    ResultSet rs2 = st2.executeQuery(sql2);

	 	    while (rs2.next()) {
	 	    	String kood = rs2.getString("toode");
	 	    	String nimi = rs2.getString("nimi");
	 	    	if(kood != null && nimi != null && kood.trim().length() > 0 && kood.trim().length() > 0) {
	 	    		  res.put(kood.trim(), nimi.trim());
	 	    	}
	 	    

	 	    }
	 	    rs2.close();
	 	    st2.close();
	 	    con.close();
	 	    
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
		  
		  return res;
  }

  public String identifyCustomer(String id,String regnr)  {

    String acc = null;

    	
    	try{
    String sql2 = " select [Customer No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer Cross]  = '" + id.trim() + "'";
    System.out.println(sql2);

   Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("klient").trim();

    }
    rs2.close();
    st2.close();
    con.close();

    


  	if (acc == null){

  	String sql1 = "select [Customer No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer No_]  = '" + id + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}
  	
  	
  	
  	
  	if (acc == null){

  	String sql1 = "select [No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer]  where [EDI ILN]  = '" + id + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}

  	if (acc == null && regnr != null && regnr.trim().length() > 0){

  	String sql1 = "select [No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer]  where [EDI ILN]  = '" + regnr + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}  	
  
    	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
  	if(acc == null){
  		acc = id;
  	}
  	
    System.out.println("Klient: " + acc);
    return acc;

  }


  public String identifyAgent(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Salesperson Code] AS agent " +
	      " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);

	    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));


	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	    con3.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identKett(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Tinklas] AS agent " +
	      " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);

	    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	    con3.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identifyProduct2(String ean, String prodID) throws SQLException{

    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "' order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai ir Ko$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

   
    String prod = null;
    Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);

  	System.out.println(sql2);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	  //System.out.println("prod1 " + prod );
    }
    
	System.out.println("prod2 " +sql1);
    if(prod == null){	
      rs1 = st1.executeQuery(sql1);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      //	System.out.println("prod2 " + prod  + sql1);
      }
    }
    st1.close();
    rs1.close();
   con.close();


    System.out.println("prodFINAL " + prod + " " + ean + " " +prodID);

    return prod;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	  String tyyp = "";
	  if(prodID.indexOf("|") != -1){
		  tyyp = "and [Cross-Reference Type No_] ='" + prodID.substring( prodID.indexOf("|")+1) + "'";
		  prodID = prodID.substring(0, prodID.indexOf("|"));
	  }
	  
    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "'  order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai ir Ko$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

    
    String sql3 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] "
    		+ "where [Cross-Reference No_]='" + prodID + "' and [Cross-Reference No_] !='' " + tyyp ;
    String sql6 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] "
    		+ "where [Cross-Reference No_]='" + ean + "' and [Cross-Reference No_] !='' " + tyyp ;
 String id = null;
    if(prodID != null){
	    id = prodID.replace('.', ',');
   }
   

    String sql4 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] where [Cross-Reference No_]='" + id + "'";
   
    String prod = null;
    Connection con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql3);

  	//System.out.println(sql3);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	//System.out.println("prod1 " + prod );
    }
    
    if(prod == null){	
        rs1 = st1.executeQuery(sql6);
        if (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	//System.out.println("prod2 " + prod  + sql1);
        }
      }
    
	//System.out.println("prod2 " +sql1);
    if(prod == null){	
      rs1 = st1.executeQuery(sql1);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      	//System.out.println("prod2 " + prod  + sql1);
      }
    }
	//System.out.println("prod2 " + prod  + sql1);
    if(prod == null ){
    	
        rs1 = st1.executeQuery(sql2);
        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	System.out.println("prod3 " + prod  + sql3);
        }
      }
    //System.out.println("prod4 " + prod  + sql4);
    if(prod == null && prodID != null){
    	
        rs1 = st1.executeQuery(sql4);
        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	//System.out.println("prod4 " + prod  + sql4);
        }
      }
   // st1.close();
    rs1.close();
   


if(prod == null){
	 String eanabi = ean;
	    if(eanabi != null && eanabi.length()>7){
	    	eanabi = eanabi.substring(0,7);
	    }
	    
	    String sql5 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
		"  WHERE left([Brūkšninis kodas],10) ='" + eanabi + "...' and [Blocked]=0 ";
	   // System.out.println("prod5 " + prod  + sql5);
    rs1 = st1.executeQuery(sql5);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
    	//System.out.println("prod5 " + prod  + sql5);
    }
  }

System.out.println("prod" + prod +" "+ ean+ " " + prodID);

    st1.close();
    con3.close();
   if(prod == null){
   	prod = prodID;
   }
   
    return prod;
  }



  private HashMap getCustILNs() {
	  HashMap res = new HashMap();
	  
	 	try{
	 	    String sql2 = "SELECT k.kood as kood1 ,k.[Ship-to Address ILN] as kood2,k.[Customer ILN] as kood3 "
	 	    		+ " from  dbo.v_VIK_Customers k where k.[Customer ILN] != '' ";
	 	    System.out.println(sql2);

	 	   Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	 	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

	 	    Statement st2 = con.createStatement();
	 	    ResultSet rs2 = st2.executeQuery(sql2);

	 	    while (rs2.next()) {
	 	    	String id = rs2.getString("kood1").trim();
				String id2 = rs2.getString("kood2");
				String id3 = rs2.getString("kood3");
				if(id2 != null && id2.trim().length() > 0) {
					id = id + "_" + id2.trim();
				}
	 	    	if(id != null && id2 != null && id.trim().length() > 0 && id2.trim().length() > 0) {
	 	    		  res.put(id, id3);
	 	    	}
	 	    

	 	    }
	 	    rs2.close();
	 	    st2.close();
	 	    con.close();
	 	    
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
		  
		  return res;
  }
  
  private HashMap getILNs() {
	  HashMap res = new HashMap();
	  
	 	try{
	 	    String sql2 = "SELECT k.kood as kood1 ,k.[Ship-to Address ILN] as kood2 "
	 	    		+ " from  dbo.v_VIK_Customers k where k.[Ship-to Address ILN] != '' ";
	 	    System.out.println(sql2);

	 	   Connection con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
	 	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

	 	    Statement st2 = con.createStatement();
	 	    ResultSet rs2 = st2.executeQuery(sql2);

	 	    while (rs2.next()) {
	 	    	String id = rs2.getString("kood1").trim();
				String id2 = rs2.getString("kood2");
				if(id2 != null && id2.trim().length() > 0) {
					id = id + "_" + id2.trim();
				}
	 	    	if(id != null && id2 != null && id.trim().length() > 0 && id2.trim().length() > 0) {
	 	    		  res.put(id, id2);
	 	    	}
	 	    

	 	    }
	 	    rs2.close();
	 	    st2.close();
	 	    con.close();
	 	    
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
		  
		  return res;
  }
  
  public void makeOrder(Order order) throws SQLException{

    
        setOrder(order);
        makeOutput(order);
     
  }

  
  
  private Node getNode(Node rt, String s){
	    return GenericDOM.getNode(rt, s);
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
  
  private String getDir(){
	     String dir = parent.getProps().getProperty("readin");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   }
  
  
  
  
  
  
  
  public void getAgents(){
	   
	     agentGUID = new HashMap<String, String>(); //key=GUID, value=code
	     TreeMap tm = new TreeMap();
	     //
	     //getFixedAssortmentData();
	  //  makeData();
	     
	     // loene võimalikud agendid sisse 
	   
	 	try {
	 		  File f = new File (getDir() + "Agents.xml");
	 	     
	 	       // System.out.println(getDir() + "Agents.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "AgentsList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	 if(rida.getNodeName().equals("Agent")){
	         	  try{
	         		 String salesman   = getNode(rida,"Code").getFirstChild().getNodeValue();
	         		 String salesmanN   = getNode(rida,"Name").getFirstChild().getNodeValue();
	         		 salesman = Converter.deCode(salesman);
	         		 Agent sm = new Agent (salesman);
	         		 sm.setName(salesmanN);
	                 tm.put(salesman, sm); 
	               //System.out.println(salesman + " " + sm);
	                 String id =  getNode(rida,"GUID").getFirstChild().getNodeValue();
	                 agentGUID.put(salesman, id);
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	     //this.getCustomers(null);
	     //this.getProducts(null);
	     //return tm;
	   }
  
  
  
  
  
  
  Hashtable custAgents = null;
  private HashMap <String, String> agentGUID = null;


  public void getCustomers(){
	     customerstm = new TreeMap();
	     custAgents = new Hashtable();
	
		
 	 
 	 try {
	 		  File f = new File (getDir() + "Clients.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "CustomersList"); 
	         
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	  if(rida.getNodeName().equals("Customer")){
	         		  

	         		 String CustGUID   = null;
	         		 String CustName   = null;		
	         		 String CustCode   = null;
	         		 String DAddressGUID   = null;
	         		String DAddressCode   = null;
	         		String DAddress   = null;
	         		String DAddressAgentGUID   = null;
	         		  
	         		  try{
		         		 CustGUID   = getNode(rida,"CustomerGUID").getFirstChild().getNodeValue();
		         		 
		         		try {
		         			CustName   = getNode(rida,"CustomerName").getFirstChild().getNodeValue();		
		         			}catch(Exception k) {}
		         		
		         		 CustCode   = getNode(rida,"CustomerCode").getFirstChild().getNodeValue();
		         		 CustName = Converter.deCode(CustName);
		         		
		         		 //System.out.println(CustGUID + " " +CustName);
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         	  
	         		  Customer customer = new Customer(CustGUID);
	      		         if(DAddressGUID != null ) {
	      		        	 customer.setPayer(DAddressGUID);
	      		         }
	      		         customer.setName(Converter.enCodeEst(CustName));
	      		         
      		  	Node addressNode = getNode(rida, "DeliveryAddressList");
      		  	if(addressNode != null){
	      			for (int j = 0; j < addressNode.getChildNodes().getLength(); j++){
	      				Node ridaAddress = addressNode.getChildNodes().item(j);
	      				
	      				if(ridaAddress.getNodeName().equals("DeliveryAddress")){
	      				
	      				
	      				try {
	      					DAddressGUID   = getNode(ridaAddress,"AddressGUID").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      				try {
	      					DAddressCode   = getNode(ridaAddress,"AddressCode").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      				try {
	      					DAddress   = getNode(ridaAddress,"Address").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      				try {
	      					DAddressAgentGUID   = getNode(ridaAddress,"AgentGUID").getFirstChild().getNodeValue();
	      				}catch(Exception k) {}
	      				
	      				
	      				//System.out.println("Addressinfo: "+DAddressGUID + " " + DAddressCode + " " + DAddress + " " + DAddressAgentGUID);
	      				
	      		       
	      		         if(DAddressGUID != null ) {
	      		        	 customer.setPayer(DAddressGUID);
	      		         }
	      		         customer.setName(Converter.enCodeEst(CustName) + "_" + DAddressCode);
	      		         customer.setAddress(Converter.enCodeEst(Converter.adressLink(DAddress,"Latvia")));
	      		         
	      		         //customer.addAgent(new Agent(agentGUID.get(DAddressAgentGUID)));
	      		         
	      		         customerstm.put(CustCode + "_" + DAddressCode, customer); 
	      		       
	      		         
	      		         //customer.getID()_kliendiguid_addressiguid
	      				
	      				}
	      			}
      		  	} else {
      		  	 customerstm.put(CustCode + "_0" , customer); 
      		  	}
			       }
	         	 
	         	}
	         
	         //customercode_addresscode
	         
	
          in.close();
          
 	 }catch(Exception k ){
				k.printStackTrace();
			}
 	 
 	 
 	 //System.out.println(tm.toString());
 	 
 	 //System.exit(0);
       
 	 
 	 
 	 
     
      //return tm;
    }
  
  
  TreeMap customerstm = null;
  
  
  
  
  private Hashtable prodNames = null;
  TreeMap prodtm = null;
  

  public void getProducts(){
    prodtm = new TreeMap();
    
    //Productgroups.xml
    


    
		//Product.xml
    String ProdGUID=null;
	 String ProdCode=null;   
		
		try {
			  File f = new File (getDir() + "Product.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
			FileInputStream in = getCleanInputStream(f);
			String handler = null;
		    Document doc = db.parse(in,handler);
		    Node root = getNode(doc, "ProductsList"); 
		    
		    for (int i = 0; i < root.getChildNodes().getLength(); i++){
		    	  Node rida = root.getChildNodes().item(i);
		    	  
		    	  //System.out.println(rida.getNodeName() + " " );
		    	  if(rida.getNodeName().equals("Product")){
		    		  try{
		        		  ProdGUID   = getNode(rida,"GUID").getFirstChild().getNodeValue();
		        		  ProdCode   = getNode(rida,"Code").getFirstChild().getNodeValue();
		        	  }catch(Exception e){
		        		  e.printStackTrace();
		        	  }
		    		

					 prodtm.put(ProdCode, ProdGUID);

			       }
		    	}
		    
		    in.close();
		}catch(Exception k ){
				k.printStackTrace();
		}
		

  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  HashMap names = null;
  HashMap ilns = null;
  HashMap ilnsC = null;

	private void makeOutput(Order order) {

	  if(customerstm == null) {
		  getCustomers();
	  }
	  if(agentGUID == null) {
		  getAgents();
	  }
	  
	  if(prodtm == null) {
		  getProducts();
	  }
	  
	  

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();



			// String filiaalStr = null;
			// String tarnijaKoodStr = null;

			Node docheader = doc.createElement("Order");
			doc.appendChild(docheader);

			
			String number = order.getDokNr();
			if (number == null || number.length() == 0) {
				number = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
			}
			Node un2 = doc.createElement("Number");
			Text unVal2 = doc.createTextNode(number);
			un2.appendChild(unVal2);
			docheader.appendChild(un2);

			try {
				

				//long ll = Long.parseLong(order.getAgentCode());
				Calendar cal = Calendar.getInstance();

				String month = (cal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				String day = cal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;
				String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
				if (hour.length() == 1)
					hour = "0" + hour;
				String minute = cal.get(Calendar.MINUTE) + "";
				if (minute.length() == 1)
					minute = "0" + minute;

				String second = cal.get(Calendar.SECOND) + "";
				if (second.length() == 1)
					second = "0" + second;
				String year = cal.get(Calendar.YEAR) + "";
				if (year.length() == 2) {
					year = "20" + year;
				}
				String dateString = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "T" + hour + ":" + minute + ":"
						+ second;
				
				Node un4 = doc.createElement("Date");
				Text unVal4 = doc.createTextNode(dateString);
				un4.appendChild(unVal4);
				docheader.appendChild(un4);
				
				Node delkuupaev = doc.createElement("DeliveryDate");
				Calendar delcal = Calendar.getInstance();
				delcal.setTime(order.getShipCal().getTime());

				
				month = (delcal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				day = delcal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;
				year = delcal.get(Calendar.YEAR) + "";
				if (year.length() == 2) {
					year = "20" + year;
				}
				dateString = year + "-" + month + "-" + day + "T00:00:00";
				
				Node un5 = doc.createElement("DeliveryDate");
				Text unVal5 = doc.createTextNode(dateString);
				un5.appendChild(unVal5);
				docheader.appendChild(un5);
				

				Node age = doc.createElement("AgentGUID");
				Text ageVal = doc.createTextNode(agentGUID.get(order.getAgentCode()));
				age.appendChild(ageVal);
				docheader.appendChild(age);

				
				/*Node pricetypeGUID = doc.createElement("PriceTypeGUID");
				Text pricetypeGUIDVal = doc.createTextNode("hardcode-f2f8-11e8-8430-42358369fa142");
				pricetypeGUID.appendChild(pricetypeGUIDVal);
				docheader.appendChild(pricetypeGUID);*/
				
				/*Node sum = doc.createElement("Sum");
				Text sumVal = doc.createTextNode("");
				sum.appendChild(sumVal);
				docheader.appendChild(sum);*/
				
				Node currency = doc.createElement("Currency");
				Text currencyVal = doc.createTextNode("EUR");
				currency.appendChild(currencyVal);
				docheader.appendChild(currency);

				if(order.getInfo() != null && order.getInfo().trim().length() > 0 ) {
					Node comment = doc.createElement("Comment");
					Text commentVal = doc.createTextNode(order.getInfo());
					comment.appendChild(commentVal);
					docheader.appendChild(comment);
				}
				
				Customer cust = ((Customer)customerstm.get( order.getCompany()));
				Node customer = doc.createElement("Customer");
				Node custGUID = doc.createElement("CustomerGUID");
				Text custGUIDVal = doc.createTextNode(cust.getID()+"");
				custGUID.appendChild(custGUIDVal);
				customer.appendChild(custGUID);
				docheader.appendChild(customer);
				
				Node custName = doc.createElement("Name");
				Text custNameVal = doc.createTextNode(Converter.deCode(cust.getName()));
				custName.appendChild(custNameVal);
				customer.appendChild(custName);
				
				Node custRegNr = doc.createElement("RegNumber");
				Text custRegNrVal = doc.createTextNode(cust.getRegnr());
				custRegNr.appendChild(custRegNrVal);
				customer.appendChild(custRegNr);
				
				Node custDelAddr = doc.createElement("DeliveryAddress");
				Text custDelAddrVal = doc.createTextNode(Converter.deCode(cust.getAddress()));
				custDelAddr.appendChild(custDelAddrVal);
				customer.appendChild(custDelAddr);
				

				Node DelAddrGUID = doc.createElement("DeliveryAddressGUID");
				Text DelAddrGUIDVal = doc.createTextNode(cust.getPayer()+"");
				DelAddrGUID.appendChild(DelAddrGUIDVal);
				customer.appendChild(DelAddrGUID);


				Node products = doc.createElement("Products");
				docheader.appendChild(products);
				
				for(int x=0; x<order.getOrderLines().size(); x++) {
					OrderLine ll = (OrderLine) order.getOrderLines().get(x);
					Node line = doc.createElement("Line");
					products.appendChild(line);
					
					Node ProdNrNode = doc.createElement("Nr");
					Text ProdNr = doc.createTextNode((x + 1) + "");
					
					Node ProdGUIDNode = doc.createElement("ProductGUID");
					Text ProdGUID = doc.createTextNode(prodtm.get(ll.getToode())+"");
					
					Node ProdCodeNode = doc.createElement("ProductCode");
					Text ProdCode = doc.createTextNode(ll.getToode());
					
					Node ProdQtyNode = doc.createElement("Qty");
					Text ProdQty = doc.createTextNode(ll.getKogus()+"");
					
					
					
					Node ProdVatNode = doc.createElement("VatPercent");
					Text ProdVat = doc.createTextNode("21");
					

					
					ProdNrNode.appendChild(ProdNr);
					ProdGUIDNode.appendChild(ProdGUID);
					ProdCodeNode.appendChild(ProdCode);
					ProdQtyNode.appendChild(ProdQty);
					
					ProdVatNode.appendChild(ProdVat);
					//ProdSumNode.appendChild(ProdSum);
					
					line.appendChild(ProdNrNode);
					line.appendChild(ProdGUIDNode);
					line.appendChild(ProdCodeNode);
					line.appendChild(ProdQtyNode);
					if(ll.getDiscount()>0) {
						Node ProdPriceNode = doc.createElement("Discount");
						Text ProdPrice = doc.createTextNode(ll.getDiscount()+"");
						
						ProdPriceNode.appendChild(ProdPrice);
						line.appendChild(ProdPriceNode);
					}
					line.appendChild(ProdVatNode);
					//line.appendChild(ProdSumNode);
					addNumLine();
				}
				
				
				
				
				
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			

			String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + ".xml";
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

		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.exit(0);
	}


	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

	private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);





  

  public String getDriverVersion(){
    return "S1XMLRadexImport";
  }

  private void setOrder(Order order){
    this.order = order;
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

  private String url = "jdbc:odbc:";

  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
}
