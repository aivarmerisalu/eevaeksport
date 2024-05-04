/*
f  * Created on 31.07.2006
 */
package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


 public class NaviTobisDirectoImportDriver extends AbstractImportDriver{

   static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

   public NaviTobisDirectoImportDriver(ImportPanel parent) {
     this.parent = parent;
     PropertyConfigurator.configure("log4j.properties");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
     
       con2 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con3 = DriverManager.getConnection(url + "NAV40"
            , "", "");
       con4 = DriverManager.getConnection(url + "NAV40"
            , "", "");

       con6 = DriverManager.getConnection(url + "NAV40"
            , "", "");

     
             
     String sql =  "  SELECT k.[No_] AS kood " +
	 " FROM [2015_TOBIS$Customer] k " +
	 " WHERE k.[Salesperson Code] != ''  AND Blocked != 3 " ;

System.out.println(sql);


     Statement stmt = con2.createStatement();
     ResultSet rs = stmt.executeQuery(sql);
     while(rs.next()){
    		String kood = rs.getString("kood").trim(); 
    		String asi = Converter.enCodeEst(kood);
    		custcodes.put(asi, kood);
     }
     rs.close();
     stmt.close();
    
     }
     catch (Exception e) {
       parent.showError(e);
     }
     loadSignatures();
     
     eans = new TreeMap();
     company = parent.getProps().getProperty("company");
     comphelp = parent.getProps().getProperty("comphelp");
   
     tooted = this.getProducts();
  //   mlines = new Vector();//getFixedAssortmentData();
    // grupid = this.pggroups; 
     
     logurl = "https://directo.gate.ee/xmlcore/";
     if( parent.getProps().getProperty("logurl") != null){
     	  logurl = parent.getProps().getProperty("logurl");
     }
  }
   
   TreeMap naviCustomers = null;
   HashSet directoOnly = null;
   HashSet naviOnly = null;
    
   private TreeMap getDirNaviCustomers() {
	   TreeMap res = new TreeMap();
	   try {
		
		   String sql =  " SELECT k.[No_] AS kood, [Code in DIRECTO LT] as dire,k.[Name] AS nimi, k.[Address]+' '+k.[Address 2]+' '+k.[City] as addr1, a.[Address]+' '+a.[Address 2]+' '+a.[City] as addr2"
		   		+ ", [Order only to DIRECTO] as dirord FROM [2015_Tobis$Customer] k LEFT join [2015_Tobis$Ship-to Address] a on  a.[Customer No_]=k.[No_] \r\n" + 
                   " and a.Code=k.[Ship-to Code]   WHERE k.[Salesperson Code] != ''  AND Blocked != 3 ";
   
		   naviCustomers = new TreeMap();
		   directoOnly = new HashSet();
		   naviOnly = new HashSet();
		   
   System.out.println(sql);

   Connection con = DriverManager.getConnection(url + "NAV40" , "", "");
   
   Statement stmt = con.createStatement();
   ResultSet rs = stmt.executeQuery(sql);
   while(rs.next()){
  		String kood = rs.getString("kood").trim(); 
  		String dire = rs.getString("dire"); 
  		String name = rs.getString("nimi").trim();
  		String aadress1 =  rs.getString("addr1");
  		String aadress2 =  rs.getString("addr2");
  		Integer dirord = rs.getInt("dirord");
  		String addr = "";
  		if(aadress2 != null) {
  			addr = aadress2.trim();
  		}else if(aadress1 != null) {
  			addr = aadress1.trim();
  		}
  	
  		
  		naviCustomers.put(kood, escape(name+ " " + addr));
  		if(dire != null) {
  			res.put(kood, dire.trim());
  			if(dirord == 1) {
  			//	System.out.println("directo Only " + kood);
  			directoOnly.add(kood);
  			}
  			if(dirord == 2) {
  	  			//	System.out.println("directo Only " + kood);
  	  			naviOnly.add(kood);
  	  			}
  		}
  		
   }
   rs.close();
   stmt.close();
   con.close();
   
	   }catch(Exception e) {
		   e.printStackTrace();
	   }
       
	   res.put("fake", "5556GETZ");
	   
	   return res;
   }
   
   
   
   public void getDirCustomers(){
	
	   dirKliendid = new TreeMap();
		   
		   try {
		 		  File f = new File (getDir() + "kliendid.xml");
		 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 	     DocumentBuilder db;
		 		db = dbf.newDocumentBuilder();      	
		     	FileInputStream in = getCleanInputStream(f);
		     	String handler = null;
		         Document doc = db.parse(in,handler);
		         Node root = getNode(doc, "transport");      
		      //   System.out.println(root);
		     	Node read = getNode(root, "customers");
		     	 for (int i = 0; i < read.getChildNodes().getLength(); i++){
		         	  Node rida = read.getChildNodes().item(i);
		         	  
		         	 if(rida.getNodeName().equals("bi_customer")){
		        
		         		//System.out.println("ridu  " + rida.toString());
		         		 String name = null;
		         		 try{
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		
		         		  Customer customer = new Customer(id);
		         		// System.out.println("id  " + id);

		         		   name =  rida.getAttributes().getNamedItem("name").getNodeValue();
		         		  customer.setName(deCode(name));
		         		  
		         		  
		         		   //del_address1="E-600 Prekybos centras  Palangos16,Sudmant11146xx km," del_address2="Klaipeda" del_address3="92499"
		         		  try{ 
		         			
		         		 String aadr =  rida.getAttributes().getNamedItem("del_address1").getNodeValue(); 
		         		  customer.setAddress(deCode(aadr));
		         	      }catch(Exception e){
		         		  
		         		  try{ 
		 	         		 String aadr =  rida.getAttributes().getNamedItem("address1").getNodeValue(); 
		 	         		  customer.setAddress(deCode(aadr));
		         		 }catch(Exception ee){

			         		 customer.setAddress(""); 
		         		 }
	         		  }
		         		  try{
			         			 String city =  rida.getAttributes().getNamedItem("del_address2").getNodeValue() ;         	
				         		  customer.setCity(city);
			         		  }catch(Exception e){
			         			 try{
				         			 String city =  rida.getAttributes().getNamedItem("address2").getNodeValue() ;         	
					         		  customer.setCity(city);
			         			}catch(Exception es){

				         			  customer.setCity("");	
			         			}
			         		  }
		         		
		         		 try{
		         			 String zip =  rida.getAttributes().getNamedItem("del_address3").getNodeValue() ;         	
			         		  customer.setZipcode(zip);
		         		  }catch(Exception e){
		         			 try{
			         			 String city =  rida.getAttributes().getNamedItem("address3").getNodeValue() ;         	
				         		  customer.setCity(city);
		         			}catch(Exception es){

			         			  customer.setCity("");	
		         			}
		         		  }
		         		  
		         		  try{
		         			 String price_list =  rida.getAttributes().getNamedItem("priceformula").getNodeValue() ;         	
			         		  customer.setGroup(price_list);
		         	  
		         		  }catch(Exception e){
		         			  
		         		  }
		         		
		         		   // System.out.println(id);
		         		 dirKliendid.put(id, customer);
		                      
		         	  }catch(Exception e){
		         		 e.printStackTrace(); 
		         	  }
		         	 }
		         }
		         
		 	}catch(Exception e){
		 		e.printStackTrace();
		 	}   
		 	//System.out.println("ImpCodes " + impCodes.toString());
		 	
	

   }

   
    private TreeMap dirNaviCust = null;

    private TreeMap dirNaviAgs = null;
    
   private void makeOutputDirecto(Order ord){
	   
	   if(dirNaviCust == null) {
		   dirNaviCust = getDirNaviCustomers();
	   }

	   if(dirKliendid == null) {
		getDirCustomers();
	   }
		  boolean sendcomm = false;
		  if("true".equals(parent.getProps().getProperty("sendemail.allorders"))) {
			  sendcomm = true;
		  }
		 	try {
		 		String ordnum = getNum()+ "";
		 		String comm = "";
		 		if(ord.getInfo() != null && ord.getInfo().trim().length() >0){
		 			comm = "comment=\""+ escape(ord.getInfo()) + "\" " ;
		 			if(ord.getStatus() > 5){
			 			comm = "cutomer_orderno=\""+ escape( ord.getInfo()) + "\" " ;
			 		}
		 		}
		 		

		 	   	String custid = (ord.getCompany());
		 	   	System.out.println("enne " + custid );
		 	     custid =   (String) this.custcodes.get(custid);
		 	     if(custid == null) {
		 	    	 custid = (ord.getCompany());
		 	     }
		 		
		 		
		 		String dirCuID = (String)dirNaviCust.get(custid);
		 		if(dirCuID == null) {
		 			dirCuID = (String)dirNaviCust.get("fake");
		 		}
		 		Customer ccc = (Customer)dirKliendid.get(dirCuID);
		 		System.out.println("dirKliendid " + dirKliendid.size());
		        
		 		if(ccc == null) {
		 			ccc = new Customer("5556GETZ" );
		 			ccc.setName("Uzsakymai TOBIS");
		 			String nameNav = custid + " " + (String)naviCustomers.get(custid);
		 			if(ord.getInfo() != null && ord.getInfo().trim().length() >0){
			 			comm = "comment=\""+ ord.getAgent() + " " + ord.getInfo()+ " " + nameNav + "\" " ;
			 			
			 		}else {
			 			comm = "comment=\""+ ord.getAgent() + " " + nameNav + "\" " ;
			 		}
		 			
		 			
		 		}
		 		
		 		
		        
		        System.out.println("ccc " + ccc.getName());
		 		
		 		
		 		 comm = protectSpecialCharacters(comm);
		 		System.out.println("comm " + comm + " ccc " +  ccc.getGroup());
		 		String gr = "";
		 		if(ccc != null && ccc.getGroup()!= null){
		 			gr = ccc.getGroup();
		 		}
		 		String str = "";
		 		if(ccc != null && ccc.getAddress()!= null){
		 			str = deCode(ccc.getAddress());
		 		}
		 		String zip = "";
		 		if(ccc != null && ccc.getZipcode()!= null){
		 			zip = deCode(ccc.getZipcode());
		 		}
		 		
		 		String city = "";
		 		if(ccc != null && ccc.getCity()!= null){
		 			city = deCode(ccc.getCity());
		 		}
		 		
		 		String nam = "";
		 		if(ccc != null && ccc.getInfo()!= null){
		 			nam = deCode(ccc.getInfo());
		 		}
		 		String pers = "";
		 		if(ccc != null && ccc.getPersonalData()!= null){
		 			pers = deCode(ccc.getPersonalData());
		 		}
		 		System.out.println("Comment " + comm);
		 		ord.setSum(0);
		 		
		 		String ladu = "";
		 		 if("true".equals(parent.getProps().getProperty("order.setware"))) {
		 			 ladu =  "stock=\"" + parent.getProps().getProperty("stockcode") +  "\" "  ; 
		 		  }
				
		 		 if(ord.getWarehouse() != null && ord.getWarehouse().trim().length() >0) {
		 			 ladu =  "stock=\"" + ord.getWarehouse() +  "\" "  ; 
		 		 }
		 		 
		 	    String  order_xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
		          "<transport>" +
		          "<order " + 
		         "webid=\"" + ordnum + "\" " + //order.getDokNr()+
		         "webdate=\"" + getDateNiceString(Calendar.getInstance())+ "\" " +
		         "ccode=\"" +deCode( ccc.getID()) + "\" " +
		         "send_mode=\"POST\" " + 
		         "send_type=\"ALL\" " +
		         "object=\"4\" " +
	             comm + 
	              ladu +
		         "amount=\"" + ord.getSum() + "\" " +
		         "amount_vat=\"" + ord.getSum()* 1.21 + "\" " +
		         "currency=\"EUR\" language=\"LIT\">" +
		         "<pricelist>" + gr + "</pricelist>" +
		       //del_address1="E-600 Prekybos centras  Palangos16,Sudmant11146xx km," del_address2="Klaipeda" del_address3="92499"
		         "<send_address>" +
		         "<street>" + str + "</street>" +
		         "<county>" + city + "</county>" +
		         "<country>0</country>" +
		         "</send_address>" +
		         "<contact>" +
		         "<name>" + nam + "</name>\r\n" +
		         "<phone>" + pers +"</phone>" +
		         "</contact>" +
		         "<lines>" ;
		       
		     	 Vector lines = ord.getOrderLines();
			      for (int i = 0; i < lines.size(); i++) {
			    	  OrderLine line = (OrderLine) lines.elementAt(i);
			    	  Product ppp = (Product)tooted.get(line.getToode());
			    	  
			    	  order_xml = order_xml + "<line ";
			    	  order_xml = order_xml + "quantity=\"" + line.getKogus() + "\" ";
				      order_xml = order_xml +"pcode=\""+ Converter.deCode(line.getToode()) + "\" ";
			    	 // order_xml = order_xml + "description=\"" + ppp.getName() + "\" " ;
			    	  double hind = 0;
			    	  
			    	  try{
			    		  hind =  this.getPriceAction(line.getToode(),custid,ppp)+ 0.0005 ;
					  }catch(Exception e){
			    	  }
					 // System.out.println("HindAction " + line.getToode() + " " + hind);
				    	
					  if(hind < 0.0006){
	                      try{
				    		  hind =  getPrice(line.getToode(),custid,ppp)+ 0.0005 ;
						  }catch(Exception e){
				    	  }
				     }
			    	  
			    	System.out.println("Hind " + line.getToode() + " " + hind);
			    	  
	                  if(line.getDiscount() > 0) {
	                	  hind = line.getPrice();
	                	  order_xml = order_xml +"discount=\""+ line.getDiscount() +"\" ";
					    //  order_xml = order_xml +"price=\""+ Converter.Round((float)hind, 2) +"\"";
	                  }
			    	  order_xml = order_xml + "/>";
				 counter++;
				 addNumLine();
		      }
			      
			      if(ord.docType != null && ord.docType.equals("less")) {
			    	  
			    	
			    			  
			    				String transa = "TRANSP75";
			    	   	
			    	   	if(parent.getProps().getProperty("directotransportcode") != null ) {
			    	   		transa =  parent.getProps().getProperty("directotransportcode");
			    	    }
			    	  order_xml = order_xml + "<line ";
			    	  order_xml = order_xml + "quantity=\"1\" ";
				      order_xml = order_xml +"pcode=\""+ transa +"\" ";
			          order_xml = order_xml +"price=\"0\"";
			    	  order_xml = order_xml + "/>";
				 
			      }
		 
			      
		 	order_xml = order_xml + "</lines>";
		 	order_xml = order_xml + "</order>";
		 	order_xml = order_xml + "</transport>";
		    System.out.println("output " +order_xml); 
		 	
		 		//System.exit(0);
		 		
		 	try {
		 	    // Construct data
		 		String key = parent.getProps().getProperty("keyOrder");
		 		
		 	    String data = ("key=" + key + "&put=1&what=order&xmldata=" + URLEncoder.encode(order_xml,"UTF-8") );
		 	  
		 	   
		    Converter.doTrustToCertificates();
		 	    URL url = new URL(logurl + company +"/"+ comphelp + "/xmlcore.asp");
		 	    System.out.println(logurl + company +"/"+ comphelp + "/xmlcore.asp");
		 	    
		 	   byte[] postData = data.getBytes( StandardCharsets.UTF_8 );
	           int postDataLength = postData.length;
	           HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
	           con.setRequestMethod("POST");
	           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
	           con.setRequestProperty("charset", "utf-8");
	           con.setRequestProperty("Content-Length", Integer.toString( postDataLength ));
	           con.setUseCaches(false);
	           con.setDoOutput(true);
	           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
	              wr.write( postData );
	              wr.close();
	           }
	           
		 	    

		 	    // Get the response
		 	    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		 	    String line;
		 	    String asi = "";
		 	    while ((line = rd.readLine()) != null) {
		 	        System.out.println(line);
		 	        asi = asi + "\n" + line ;
		 	    }
		 	
		 	    rd.close();
		 	    
		 	    
		 	    
		 	   String doknr = order.getDokNr();
			      String fn = doknr;
			      if(doknr.trim().length()== 0){
			    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
			      }
		 	   String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".txt";
			      System.out.println(fileName);
			     //  String asi = data;
			      Converter.saveFileInUTF8(fileName, asi);
			      if(asi.contains("error code")){
			    	  trouble = "ORDER Was not Imported :" + asi;
			      }else{
			    	  trouble = null;
			      }
		 	    
			      
		 	} catch (Exception e) {
		 		e.printStackTrace();
		 	}
	       

		      
		 	
		      
		    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		    
		      
		      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, order_xml);

		      if( sendcomm) {
		    	
		    	  
		    		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
		    		   System.out.println("Saadan emaili sisse");
		    	//   sendCommentEmail( comm ,null,doknr,order.getCompany(),  " " + ccc.getName() , " ",false,ag,parent);
		    	 System.out.println("Saadan emaili tehstud");
		    }

			} catch (Exception e) {
				e.printStackTrace();
			}
		 	//System.exit(0);
		 }


   private int counter = 0;

   private double getPrice(String toode ,String firma, Product ppp){
		
		  Customer ccc = (Customer) dirKliendid.get(firma);
		
		  String kgrupp = null;
		  String tgrupp = null;
		  String tm1grupp = null;
		  String tm2grupp = null;
		  if(ccc != null){
			    kgrupp = ccc.getGroup();		 
		      }
		    
		  if(ppp != null){
				tgrupp = ppp.getGroup();		 
			  }
		  
		  if(grupid.get(tgrupp) != null){
			  Toode tt = (Toode)grupid.get(tgrupp);
			  if(tt.getGr1() != null){
				  tm1grupp = tt.getGr1();
			  }
			  if(tt.getGr2() != null){
				  tm2grupp = tt.getGr2();
			  }
			  
		  }
		  Enumeration enumr = mlines.elements();
		  
		//  System.out.println("sss " + kgrupp + " tootegr " + tgrupp+ " tooteM1gr " + tm1grupp + " tooteM2gr " + tm2grupp  + " " + mlines.size());
		  
		  while (enumr.hasMoreElements()){
			  MatrixLine mm = (MatrixLine)enumr.nextElement();
			//  System.out.println("Hinnakiri leitud "  + mm.getCliGroup());
			  if(mm.getCliGroup()!= null && mm.getCliGroup().equals(kgrupp) ){
				  //leidsime rea mis on hinnakirja oma
				 // System.out.println("Hinnakiri leitud "  + mm.getCliGroup() + " tr " + mm.getProdGroup() + " tt " + mm.getProdID());
				  if (mm.getProdID() != null && mm.getProdID().equals(ppp.getID())){
					  //leidsime ka toote 

					//  System.out.println("Toode leitud "  + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }
					  
				  }
				  
				
				  if (mm.getProdGroup() != null && mm.getProdGroup().equals(tgrupp)){
					  //leidsime ka tootegrupi
					  System.out.println("Tootegrupp  leitud " + mm.getProdLevel() 
					  + " " + mm.getDiscount());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }  
				  }	 
				  
				  if (tm1grupp != null && mm.getProdGroup() != null && mm.getProdGroup().equals(tm1grupp)){
					  //leidsime ka tootegrupi
					  System.out.println("Tootegrupp master 1 leitud " + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }  
				  }
				  

				  if (tm2grupp != null && mm.getProdGroup() != null && mm.getProdGroup().equals(tm2grupp)){
					  //leidsime ka tootegrupi
					  System.out.println("Tootegrupp master 2 leitud " + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }  
				  }
				  
			  }
		  }
			   
		  
		  return ppp.getInUnit();
	  }
	  

   private Vector mlines = null;
	  
	  private double getPriceAction(String toode ,String firma, Product ppp){
		
		  Customer ccc = (Customer) dirKliendid.get(firma);
		
		  String kgrupp = null;
		  String tgrupp = null;
		  String tm1grupp = null;
		  String tm2grupp = null;
		  if(ccc != null){
			    kgrupp = ccc.getGroup();		 
		      }
		    
		  if(ppp != null){
				tgrupp = ppp.getGroup();		 
			  }
		  if(grupid.get(tgrupp) != null){
			  Toode tt = (Toode)grupid.get(tgrupp);
			  if(tt.getGr1() != null){
				  tm1grupp = tt.getGr1();
			  }
			  if(tt.getGr2() != null){
				  tm2grupp = tt.getGr2();
			  }
			  
		  }
		  Enumeration enumr = mlines.elements();
		  
		 /// System.out.println("sss " + kgrupp + " tootegr " + tgrupp+ " tooteM1gr " + tm1grupp + " tooteM2gr " + tm2grupp );
		  
		  while (enumr.hasMoreElements()){
			  MatrixLine mm = (MatrixLine)enumr.nextElement();
			  if(mm.getStartDate()!= null && mm.getStartDate().trim().length() > 1){
				
			  if(mm.getCliGroup()!= null && mm.getCliGroup().equals(kgrupp) ){
				  //leidsime rea mis on hinnakirja oma
				  System.out.println("Hinnakiri leitud "  + mm.getCliGroup() + " tr " + mm.getProdGroup() + " tt " + mm.getProdID());
				  if (mm.getProdID() != null && mm.getProdID().equals(ppp.getID())){
					  //leidsime ka toote 

					  System.out.println("Toode leitud "  + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }
					  
				  }
				  
				
				  if (mm.getProdGroup() != null && mm.getProdGroup().equals(tgrupp)){
					  //leidsime ka tootegrupi
					  System.out.println("Tootegrupp  leitud " + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }  
				  }	 
				  
				  if (tm1grupp != null && mm.getProdGroup() != null && mm.getProdGroup().equals(tm1grupp)){
					  //leidsime ka tootegrupi
					  System.out.println("Tootegrupp master 1 leitud " + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }  
				  }
				  

				  if (tm2grupp != null && mm.getProdGroup() != null && mm.getProdGroup().equals(tm2grupp)){
					  //leidsime ka tootegrupi
					  System.out.println("Tootegrupp master 2 leitud " + mm.getProdLevel());
					   String mode = mm.getProdLevel();
					   double value = mm.getDiscount();
					   
					   if(mode.equals("1")){
						   return ppp.getInCases()* (100 - value)/ 100;
					   }
					   if(mode.equals("2")){
						   return ppp.getInUnit()* (100 - value)/ 100;
					   }			   
					   if(mode.equals("3") && ppp.getPrice() > 0){
						   return ppp.getPrice()* (100 - value)/ 100;
					   }
					   if(mode.equals("8") ){
						   return value ;
					   }  
				  }
				  
			  }
			  }
		  }
			//System.exit(0);    
		  
		  return 0;
	  }
	  
	  private String getDateNiceString(Calendar cal){
		    StringBuffer sb = new StringBuffer();
		    String year =   cal.get(Calendar.YEAR) + "";
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

		    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

		    String minute = String.valueOf(cal.get(Calendar.MINUTE));

		    String second = String.valueOf(cal.get(Calendar.SECOND));
		 
		   sb.append(  year + "-");
		    if (month.length() == 1){
			     sb.append("0");
			   }
			     sb.append(month  + "-");
		    
		    if (day.length() == 1){
		      sb.append("0");
		    }
		     sb.append(day);
		  //28.06.2010 20:00:33Y-m-d\TH:i:s n2002-05-30T09:00:00
		   
		     if (hour.length() == 1){
			      hour = "0" + hour ;
			    }
		    /* sb.append("T" +hour + ":");

		     if (minute.length() == 1){
			      sb.append("0");
			    }
		     sb.append(minute + ":");
		     if (second.length() == 1){
			      sb.append("0");
			    }
		     sb.append(second);*/
		     
		    return sb.toString();
		  }

	  
	  public static String protectSpecialCharacters(String originalUnprotectedString) {
		    if (originalUnprotectedString == null) {
		        return null;
		    }
		    boolean anyCharactersProtected = false;

		    StringBuffer stringBuffer = new StringBuffer();
		    for (int i = 0; i < originalUnprotectedString.length(); i++) {
		        char ch = originalUnprotectedString.charAt(i);

		        boolean controlCharacter = ch < 32;
		        boolean unicodeButNotAscii = ch > 256;
		        boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

		        if (characterWithSpecialMeaningInXML  || controlCharacter) { // || unicodeButNotAscii
		           // stringBuffer.append("&#" + (int) ch + ";");
		            anyCharactersProtected = true;
		        } else {
		            stringBuffer.append(ch);
		        }
		    }
		    if (anyCharactersProtected == false) {
		        return originalUnprotectedString;
		    }

		    return stringBuffer.toString().replaceAll("\"", "'");
		}
	  void saveProps(Properties props, String filename, String header)
			throws IOException {
				FileOutputStream fos = new FileOutputStream(filename);
				props.save(fos, header);
				fos.close();
			}
	  

	  private int getNum() {
		  Properties props = new Properties();
		    try {
		      FileInputStream in = new FileInputStream("serial.txt");
		      props.load(in);
		      in.close();
		    } catch (IOException e){
		      
		      } 
		    if(props.get("doknr") == null ) {
		    	props.put("doknr", "5000001");
		    	
		    	 try {
					saveProps(props,"serial.txt","value");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		    	 return 5000000;
		    }else {
		    	int i = Integer.parseInt(props.get("doknr")+"");
		    	i++;
		    	
		    	 props.put("doknr", i+"");
			    	
		    	 try {
					saveProps(props,"serial.txt","value");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		    	 return i;
		    }
		    	
		    
		 
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
	      } else if (c == '"'){
	          sb.append("&quot;");
	      } else if (c == '\''){
	          sb.append("&apos;");
	      } else {
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }

private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}

	   return idx;
   }

private HashMap getNaviProds() {
	HashMap res = new HashMap();
	try {
		 String lubaSQl = " SELECT [No_] as toode from [2015_Tobis$Item] where  [Show in mob_ device] = 1";
    	 HashSet luba = new HashSet();
    	 
    	 Connection con =  DriverManager.getConnection(url + "NAV40", "", "");
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(lubaSQl);
         while(rs.next()){
        	  String id = rs.getString("toode").trim();
        	  luba.add(id);
         }
         System.out.println("luba suurus " + luba.size());
           rs.close();
           stmt.close();
           con.close();
        
   	 String cmd = "  SELECT  t.[No_] AS toode,(t.[Description] + ' ' +   t.[Description 2] ) AS nimi, t.[Unit Price] AS myygihind,"
   	 		+ " '' AS grupp2,t.[Global Dimension 2 Code] AS grupp " +
			" ,t.[Base Unit of Measure] AS unit,[No_ 2] AS ean,'' AS status ,0 AS maxdiscount, " +
			 " '' AS type, [Units per Parcel] AS kastis,[Item Disc_ Group] as alegr ,  '' AS kaal, "
			 + "[VAT Prod_ Posting Group] AS vat ,[Units per Pallet] as pallet " +
			 " ,[Unit List Price] as listprice,[New until] as newprod ,[Nelikvidas]  as status2 , [Minimum Unit Price]  as jaeh FROM [2015_Tobis$Item] t  WHERE t.[Blocked]=0    ORDER BY t.[No_] ";
     
   	 System.out.println(cmd);
    	 con =  DriverManager.getConnection(url + "NAV40", "", "");
         stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
         
       while(rs.next()){
           String id = rs.getString("toode").trim();
           String weight ="";
           Product prod = new Product(id);
        	String name = Converter.enCodeEst(id + " " + rs.getString("nimi").trim()).replaceAll("11153xx", "x2");
        	
            double price = rs.getDouble("myygihind");
               prod.setPrice(price);
              String grupp = cleardots(rs.getString("grupp"));
       		if(grupp != null && grupp.trim().length() >0){
       			prod.setGroup(Converter.cleanLitLatEst(grupp.trim()));	
       		}
       	       prod.setType("TOBIS");
       		
               prod.setUnit(rs.getString("unit"));
             String ean = (rs.getString("ean"));
                if (ean != null && ean.trim().length()>0){
                  prod.setEAN(ean);
                }
                   
                double	cases = rs.getDouble("kastis");
               	if(cases > 0){
                      prod.setInCases(cases);
               	}
               
               	String alegrupp = rs.getString("alegr");
               	if(alegrupp != null && alegrupp.trim().length() > 0){
               		prod.setAlegrupp(Converter.cleanLitLatEst(alegrupp));	
               	}
                


         	 String vat = rs.getString("vat");
         	 
                  if(vat != null && vat.trim().length()>0){
               	 	prod.setVat(vat.trim());
                  }
              
                  double pallet = rs.getDouble("pallet");
                  if(pallet >0){
               	   prod.setOnPallet(pallet);
                  }
                  
                  double listprice = rs.getDouble("listprice");
                  if(listprice >0){
               	   prod.setPriceSecond(Converter.Round((float)(listprice * 1.21), 2));
                  }
                  
                  Date newprod = rs.getDate("newprod");
                  if(newprod.after(new Date())){
               	//   prod.setStatusNew("1");
               	   prod.setMustBe("1");
                  }
                  try {
               	   int status = rs.getInt("status2");
                      if(status > 0) {
                   	   prod.setStatusSellout("1");
                      }   
                  }catch(Exception e) {
               	   
                  }
                
                  
                  
                  if(luba.contains(prod.getID()) && !name.equals("")){
               	   prod.setName(name);
               	   res.put(prod.getID(),prod);
                      
                  }
         
       }
 
     
       rs.close();
       stmt.close();
       con.close();

	}catch(Exception e) {
		e.printStackTrace();
	}
	
	System.out.println("naviTooted suurus " + res.size());
	return res;
}

private HashMap naviProds = null;

   private Vector splitOrder (Order order){
		  
		  Vector result = new Vector();
		  

		   if(dirNaviCust == null) {
			   dirNaviCust = getDirNaviCustomers();
		   }
		  
		  if(naviProds == null) {
			  naviProds = getNaviProds();
		  }

		  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
			  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
		  }
		 System.out.println("Infokene " + order.getInfo() + " " + order.getDokNr() + " " + order.docType);

		  Order ord = new Order(order.getCompany(),order.getAgentFirstName());
		  Order orddirecto = new Order(order.getCompany(),order.getAgentFirstName());
		 
		  
		  
		  ord.setAgentCode(order.getAgentCode());
		  ord.setDokNr(order.getDokNr());
		  ord.setCompName(order.getShipMode());
		  ord.setShipMode(order.getShipMode());
		  ord.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ord.setStatus(order.getStatus()); 
		  ord.setInfo(order.getInfo());
		  ord.setReference(order.getReference());
		  ord.setTeostaja(order.getTeostaja());
		  ord.setWarehouse(order.getWarehouse());
		  ord.setDealer(order.getDealer());
		  ord.setCurrency("tobis");
		  ord.docType = order.docType;

		  
		  orddirecto.setAgentCode(order.getAgentCode());
		  orddirecto.setDokNr(order.getDokNr()+"B");
		  orddirecto.setCompName(order.getShipMode());
		  orddirecto.setShipMode(order.getShipMode());
		  orddirecto.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  orddirecto.setStatus(order.getStatus()); 
		  orddirecto.setInfo(order.getInfo());
		  orddirecto.setReference(order.getReference());
		  orddirecto.setTeostaja(order.getTeostaja());
		  orddirecto.setWarehouse(order.getWarehouse());
		  orddirecto.setDealer(order.getDealer());
          
		  orddirecto.setCurrency("directo");

		  boolean dironly = false;
		  boolean navonly = false;
		  String custid = ord.getCompany();
		  
		     custid =   (String) this.custcodes.get(custid);
		     if(custid == null) {
		    	 custid = (ord.getCompany());
		     }
		     
		     if(directoOnly.contains(custid) || directoOnly.contains(ord.getCompany())) {
		    	 dironly = true;
		     }
		    

		     if(naviOnly.contains(custid) || naviOnly.contains(ord.getCompany())) {
		    	 navonly = true;
		     }
		    
		     
	     System.out.println("dironly " + dironly + " " + navonly);
		  
		  Vector lines = order.getOrderLines();
		  
		  
		  Enumeration enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			  if(lin.docType.equals("less")) {
				  ord.docType = "less";
			  }
			    	
			    	if((naviProds.containsKey(lin.getToode()) && !dironly) || navonly) {
			    		ord.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     	ord.setDokNr(order.getDokNr());
				     	ord.setReference(order.getReference());
			    		
			    	} else {

			    		orddirecto.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			    		orddirecto.setDokNr(order.getDokNr());
			    		orddirecto.setReference(order.getReference());
				     		
			    	}
			  
		  }
		  boolean hasNavi = false;
		  if(ord.getOrderLines() != null && ord.getOrderLines().size()>0){
			  result.add(ord);  
			  hasNavi = true;
		  }
		  
		  if(orddirecto.getOrderLines() != null && orddirecto.getOrderLines().size()>0){
			  if(!hasNavi) {
				  orddirecto.docType = "less"; 
			  }
			  result.add(orddirecto);  
		  }
	      
		  
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }

   
   TreeMap pggroups = null;
   
   public TreeMap getProdGroups()  {
 	   
 	         return pggroups;
 	   }

   
   private String getDir(){
 	     String dir = parent.getProps().getProperty("readin");
 	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
 	     return dir;
 	   }

   private Node getNode(Node rt, String s){
 	    return GenericDOM.getNode(rt, s);
 	  }
 	  
   
   public static String deCode(String res ){
 	return Converter.deCode(res);
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
 	  
 	  

   
   
   public TreeMap getProducts(){
	   System.out.println("tooted a");
	   
	TreeMap tm = new TreeMap();
    
    String fixedurl = null;
    
    boolean proddatafile = false;
    
    StringBuffer sb = new StringBuffer();
    try {
 
   	 File f = new File (getDir() + "tooted.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
   	FileInputStream in = getCleanInputStream(f);
   	String handler = null;
       Document doc = db.parse(in,handler);
       Node root = getNode(doc, "transport");      
  
   	Node read = getNode(root, "items");
       for (int i = 0; i < read.getChildNodes().getLength(); i++){
       	  Node rida = read.getChildNodes().item(i);
       	
       		  if(rida.getNodeName().equals("bi_item")){
       		
      
       		String id =  rida.getAttributes().getNamedItem("code").getNodeValue();  
       		
       	
     	     Product prod = new Product(id);
     	   
     		boolean goes = true;
     	
  		
     	    	  
     	 try { 
      	    String clo =  rida.getAttributes().getNamedItem("closed").getNodeValue(); 
      	 	 if(clo.equals("1")) {
      	 		 goes = false;
      	 	 }
            }catch(Exception e) {}
     	 
     	 
     	 try { 
       	    String gr =  rida.getAttributes().getNamedItem("class").getNodeValue(); 
       	    String grn =  rida.getAttributes().getNamedItem("classname").getNodeValue(); 
       	 	prod.setGroup(gr);
       	    pggroups.put(gr, grn);
       	     }catch(Exception e) {}
     	  
          	
     			String add = "";
     	 try {
     	    	 String ean =  rida.getAttributes().getNamedItem("barcode").getNodeValue();    
     	    
     	    	ean =  ean.replaceAll(";", ":");
     	    	if(ean.contains("&")) {
     	    		ean = ean.substring(0,ean.indexOf("&"));
     	    	}
     	    	ean =  ean.replaceAll(";", ":");
         	     prod.setEAN(ean);
         	     
         	  
     	 } catch (Exception e) {
     		 
     	 }
     	 
     
     	 try {
     	  String namec =  rida.getAttributes().getNamedItem("name").getNodeValue(); 
   	  //  System.out.println("prod " + id + " + " + namec);
   	
     	  namec = namec.replace("´", "'");
     	  String addid = "";
     	
    			 prod.setName(( Converter.enCodeEst(namec) + " " + id + " " + add ).trim()); 
    	
       	    
     	   }catch(Exception e) {
     		   goes = false;
     	   }
     	     prod.setPrice(1);
     	     
     	     
  	    	 try{
     		 String pri =  rida.getAttributes().getNamedItem("price").getNodeValue(); 
     	 	      	    prod.setPrice(Double.parseDouble(pri) ); 
	    	 }catch (Exception e){
	    
	    	 }
     	
     	 
     	
     	try {
     	 String stop =  rida.getAttributes().getNamedItem("ART_STOPPED").getNodeValue();
     	if(stop.equals("Yes")){
     		goes = false;
     	}
     	stop =  rida.getAttributes().getNamedItem("E_AGENT").getNodeValue();
     	if(stop.equals("N")){
     		goes = false;
     	}
     	}catch(Exception e) {
     		
     	}
    
     	    if(goes ){
     	         tm.put(prod.getID(), prod);	
     	        }
     	      }
       }
    }catch(Exception e) {
   	 e.printStackTrace();
    }
  

    
    return tm;
  }
  


   private TreeMap tooted = null;

   String logurl = null;
   private TreeMap dirKliendid = null;
   private TreeMap grupid = null;
   private TreeMap eans = null;
   private String company = null;
   private String comphelp = null;
   
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
  

   HashMap custcodes = new HashMap();
   
   
   public String identifyCustomer(String id,String regnr)throws SQLException {
    String custid = null;
    
    String cmd = "SELECT kl.code FROM grcompany kl WHERE U_ImpordiFiliaal = '" + id + "'";   
    PreparedStatement st2 = con4.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
 
    while (rs.next()){
    	custid =  rs.getString("code");
     }
    
    rs.close();
    st2.close();
    
    if(custid == null){
    	   String cmd2 = "SELECT kl.code FROM grcompany kl WHERE  id ='" + id + "'";   
    	    PreparedStatement st22 = con4.prepareStatement(cmd2);
    	    ResultSet rs2 = st22.executeQuery();	 
    	    while (rs2.next()){
    	    	custid =  rs2.getString("code");
    	     }
    	    rs2.close();
    	    st22.close();
    }
    
   	return custid;
   }

   public int getSalesid() throws SQLException{
     String cmd = "SELECT MAX(Nr)+1 AS id from [2015_Tobis$MOFF Uzsakymu krepselis] where Merisoft= 1  ";   
     PreparedStatement st2 = con2.prepareStatement(cmd);
     ResultSet rs = st2.executeQuery();
     int result = 0 ;
     while (rs.next()){
     result =  rs.getInt("id");
      }
     rs.close();
     st2.close();
     if(result  < 7000000){
    	 result = 7000001;
     }
    
     return result;
   }

   public String identifyProduct(String ean, String prodID) throws SQLException{

    return prodID;
   }
   
   private String trouble = null;
   public String getTrouble() {
		// TODO Auto-generated method stub
		return trouble;
	}

   public void makeOrder(Order order) throws SQLException{
	   
	   if (signatures == null){
    	   signatures = new Vector();
       }
	   
	   
	   String signature = order.getSignature();
	   
	     if(signatures.contains(signature) ){ 
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			    		  
		  }else{
			   
			     setOrder(order);
			     System.out.println("out1");
			     Vector vv = this.splitOrder(order);
			     
				  for(int k = 0; k < vv.size(); k++) {
			    	   
				 	  Order ord = (Order)vv.elementAt(k);
				    if (ord.getCurrency().equals("tobis")) {
					     String salesid = this.getSalesid() + "";

					     addInvoiceHeader(salesid, ord);
					     addInvoiceLines(salesid, ord );
					     for(int i = 0 ; i < ord.getOrderLines().size();i++) {
					    // addNumLine();
					     }
					     signatures.add(signature);
						  this.trouble = null;					    	
				    }else {
				    	makeOutputDirecto(ord);
				    	 System.out.println("out3 " + ord.getOrderLines().size());
				    }
			     

				  }
			  
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
 	


   private void addInvoiceLines(String salesid, Order ord) throws SQLException{

     Enumeration enumr = ord.getOrderLines().elements();
     int counter = 0;
     while (enumr.hasMoreElements()){
       counter++;
       log.info("ORDER_LINE_COUNTER:" + counter);
       OrderLine orderLine = (OrderLine)enumr.nextElement();
       String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double ale = orderLine.getDiscount();
    
       String ccc = "";
       if(orderLine.selActCode != null && orderLine.selActCode.trim().length() > 0) {
    	   ccc = orderLine.selActCode;
       }
    
       String agent = order.getAgent();
         int alex = 0;
         if(ale == -2){
        	alex = 1; 
         }
     
         String	sql = "INSERT INTO [2015_Tobis$MOFF Prekiu krepselio eilute] ([Krepselio Nr],[Eilutes Nr], [Prietaiso Nr],[Prekes Nr]," +
         		"    [Kiekis],[Dovana],[Nuolaidos proc_],[Unit Of Measure Code],[Ret_ From ILE No_],[End Customer Discount %],"
         		+ "[End Customer price],[Uzs_Eil_Nr_],[KS prekes Nr],[Suma],[Suma su PVM],[Aprasas],[Kaina],[Campaign No],"
         		+ "[Item_code2],[Item_code15],[Item_code5],[Item_code3],[Item_code16],[can_be_equal],[RetFromInvoiceNo],[RetFromInvoiceDate],[Unrecognized])" +
             " VALUES( " +
               salesid + "," +
               counter + ",'MS-" +
               agent + "',?," +
               kogus + "," +
               alex +"," + ale + ",'',0,0,0,0,'',0,0,'',0,'" + ccc + "','','','','','',0,'', '1753-01-01 00:00:00',0)";
System.out.println(sql);
               log.info("SALES_LINE_SQL: " + sql);
               PreparedStatement st2 = con3.prepareStatement(sql);
               st2.setString(1,   Converter.deCode(itemid).trim() );
               String aiai = parent.getProps().getProperty("testing");
               if(aiai != null && aiai.equals("true")){
            	   
               }else{
            	   st2.executeUpdate();   
               }
              
               setAmount(false);
               addNumLine();
               st2.close();
           
        }
   //  System.exit(0);
     }

   private boolean isBeforeOneDay(Calendar cal , Calendar ship){
		  
		  Calendar cc = Calendar.getInstance();
		  cc.setTimeInMillis(cal.getTimeInMillis());
		  cc.set(Calendar.HOUR, 2);
		  cc.set(Calendar.MINUTE, 1);
		  cc.set(Calendar.SECOND, 1);
		  cc.set(Calendar.MILLISECOND, 1);
		  cc.add(Calendar.DAY_OF_YEAR, 1);
		  
		  Calendar ss = Calendar.getInstance();
		  ss.setTimeInMillis(ship.getTimeInMillis());
		  ss.set(Calendar.HOUR, 1);
		  ss.set(Calendar.MINUTE, 1);
		  ss.set(Calendar.SECOND, 1);
		  ss.set(Calendar.MILLISECOND, 1);
		  
		  if(cc.getTime().before(ss.getTime())){
			  return true;
		  }
		  
		  return false;
	  }

   

  private String returnMode (Order ord){
	  String res = "2";
		     Enumeration enumr = order.getOrderLines().elements();
	  
	     while (enumr.hasMoreElements()){
	    	  OrderLine orderLine = (OrderLine)enumr.nextElement();
		       double kogus = orderLine.getKogus();
		       if(kogus > 0){
		    	   return "0";
		       }
	     }
	 
	  return res ;
  }

   private void addInvoiceHeader(String salesid, Order ord ) throws SQLException{
   	

   	
   	String agent = ord.getAgent();
    
   	String custid = (ord.getCompany());
   	System.out.println("enne " + custid );
     custid =   (String) this.custcodes.get(custid);
     if(custid == null) {
    	 custid = (ord.getCompany());
     }

   	String transa = "0";
   	
   	if(ord.docType != null && ord.docType.equals("less") ) {
   		transa =  parent.getProps().getProperty("navitransportcost");
    }
   	System.out.println( ord.docType + " " + transa);
    String tekst = "";

    if (ord.getEmailBody()!= null){
      // tekst = order.getEmailBody().replaceAll("\n"," ");
     }
          
    
    if(ord.getInfo() != null && ord.getInfo().trim().length()>0 ){
    	tekst = ord.getInfo().trim();
    }

    if(tekst.length() > 250){
    	tekst = tekst.substring(0, 250);
    }

		String reference  = ord.getTeostaja();
		
		String ref = "";
		if ( reference != null && reference.trim().length() > 4 ){
		  ref = reference.trim().toUpperCase();
		}
		
	     Calendar cal = Calendar.getInstance();
	     boolean change = false;
	     Calendar  shipDate = order.getShipCal();
         if(shipDate != null && cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY &&
         		shipDate.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY && 
         		shipDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
        	 change = true;
         }
         
         if(shipDate != null && isBeforeOneDay( cal,shipDate ) ){
         	 change = true;         	
          }
	        
	          if(change){
	        	  cal = shipDate;
	          }
	        String status = "0";
	        if(ord.getStatus() == 1){
	        	status = "1";
	        }
	        if(ord.getStatus() == 3){
	        	status = "3";
	        }
	        
	        if(ord.getStatus() == 2){
	        	status = returnMode(order); //return
	        }
	        
	        if(ord.getStatus() == 6){
	        	status = "6";
	        }
		String ladu = "CS";
		
		String smode = "";
		if(ord.getShipMode() != null && ord.getShipMode().trim().length()>0){
			smode = ord.getShipMode();
		}

        String  sql = "INSERT INTO [2015_Tobis$MOFF Uzsakymu krepselis] ([Nr],[Prietaiso Nr],[Pirkejo Nr],[Registravimo data],[Merisoft]," +
        		" [Nuoroda],[External Document No_],[Sandelys],[Document Type],[ShipMode],[SalesPersoncode],[Order source],[Order Date],[Shipment Date],[Invoice To Payer],[Suma]"
        		+ " ,[Suma su PVM], [Importuota], [Uzsakymo Nr], [Uzsakymo Sukurimo data],[Uzsakymo Sukurimo laikas],[Naujas],[WEB B2B],[End Cust_ Due Date],[Payment Method Code],"
        		+ " [Prices include VAT],[Your Reference],[Coupon Code],[Coupon Amount],[ShippingCost],[Direct Delivery via Payer],"
        		+ "[Payer No_],[End_Cust_ Paym_Method],[Ship-To Name],[Ship-To Name 2],[Ship-To Address],\r\n" + 
        		"[Ship-To Address 2],[Ship-To City],[Ship-To Post Code],[Ship-To Country],[Ship-To VAT Reg_ No_],\r\n" + 
        		"[Ship-To Phone],[Ship-To e-mail],[Bill-To Name],[Bill-To Name 2],[Bill-To Address],\r\n" + 
        		"[Bill-To Address 2],[Bill-To City],[Bill-To Post Code],[Bill-To Country],[Bill-To Reg_ No_],\r\n" + 
        		"[Bill-To VAT Reg_ No_],[Reply-To email],[Ship-To Info Modified],[Payer Info Modified],[StockMgt Order],[DeliveryDate],[Priority],[Customer No_],[Locker Owner], [Locker Code], [Prices From File]) " +
                " VALUES ( " + salesid + ",'MS-" + agent + "','" + custid + "',cast(? as date),1,'"+ tekst.replaceAll("'", " ")+"','" +  
              ref + "','" + ladu  + "'," + status + ",'" + smode + "','" + ord.getAgent() + "','MOB',(CAST('1753-01-01 00:00:00.000' AS DateTime)),"
              		+ "(CAST('1753-01-01 00:00:00.000' AS DateTime)),0,0,0,'','',(CAST('1753-01-01 00:00:00.000' AS DateTime)),(CAST('1753-01-01 00:00:00.000' AS DateTime)),''"
              		+ ",0,(CAST('1753-01-01 00:00:00.000' AS DateTime)),'',0,'','',0,"+ transa+ ",0,"
              		+ " '','','','','','','','','','','','','','','','','','','','','','',0,0,0,cast(? as date),0,'','','',0)";

     log.info("addInvoiceHeader2: " + sql);
     System.out.println("addInvoiceHeader2: " + sql);
         PreparedStatement stmt2 = con6.prepareStatement(sql);
         stmt2.setTimestamp(1, new java.sql.Timestamp(cal.getTimeInMillis()));
         stmt2.setTimestamp(2, new java.sql.Timestamp(cal.getTimeInMillis()));

         String aiai = parent.getProps().getProperty("testing");
         if(aiai != null && aiai.equals("true")){
      	   
         }else{
        	 stmt2.executeUpdate();   
         }
         stmt2.close();
         addLog("lisatud arve pais -> " + ord.getCompany() + "\n");
     //  System.exit(0);
   }


   public String getDriverVersion(){
     return "NaviTobis";
   }

   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
 
   
   private void setOrder(Order order){
     this.order = order;
   }

   private void addLog(String line){
     parent.addLog(line);
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
   
   private Connection con6;
   private Connection con2;
   private Connection con3;
   private Connection con4;
   private ImportPanel parent;
   private Order order = null;
 
   private boolean isAmount = false;


  
 }

