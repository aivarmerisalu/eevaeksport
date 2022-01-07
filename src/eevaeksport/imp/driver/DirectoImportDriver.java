package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
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

public class DirectoImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public DirectoImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    eans = new TreeMap();
    company = parent.getProps().getProperty("company");
    kliendid = getCustomers(null);
    tooted = this.getProducts();
    mlines = getFixedAssortmentData();
    grupid = this.getProdGroups();
   
    logurl = "https://directo.gate.ee/xmlcore/";
    if( parent.getProps().getProperty("logurl") != null){
    	  logurl = parent.getProps().getProperty("logurl");
    }

    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
  }
  String logurl = null;
  
  public TreeMap getProducts(){

	    TreeMap tm = new TreeMap();
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	  if((line.trim().length()) > 50){
	        		  
	        	  
	        	  int len = line.indexOf("code=");
	        	  String id   = line.substring(len + 6);
	      	      Product prod = new Product(id.substring(0,id.indexOf("\"")));
	      	      len = line.indexOf("default_name=");
	      	     String name   = line.substring(len + 14);
	      	    prod.setName(deCode(name.substring(0,name.indexOf("\""))));
	      	  len = line.indexOf("base_price=");
	      	   if(len != -1){
	      		 String price   =  line.substring(len + 12);  
	       	    prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))));//hulgihind      	    
	      	   }
	      	    
	      	 
	      	   
	      	    
	    	 len = line.indexOf("purchase_price=");
	      	 if(len != -1){
		      		String price   =  line.substring(len + 16);  
		      	    prod.setInCases(Double.parseDouble(price.substring(0,price.indexOf("\""))));// ostuhind
	      	 }
	      	    
	      	 len = line.indexOf("class=");
      	 	 if(len != -1){
      	 	     String grupp   = line.substring(len + 7);  
     	          prod.setGroup(grupp.substring(0,grupp.indexOf("\"")));
      	 	 }    
	      	 
      	 	 
      	 	 len = line.indexOf("stripe_code=");
       	    if(len != -1){
       	    	 String ean   = line.substring(len + 13);
       	    	 ean = ean.substring(0,ean.indexOf("\""));
           	     prod.setEAN(ean);
           	     eans.put(ean, prod.getID());
           	}
       	   
	      	 
	      	     tm.put(prod.getID(),prod);	
	          }	   
	          }
        	  
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     return tm;
	   }
  
  private String company = null;
  
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
	  
	  
  
	  public Vector getFixedAssortmentData() {
	       Vector lines = new Vector();
	       
	       try {
	   		  File f = new File (getDir() + "hinnakiri.xml");
	   		  //System.out.println("siin matix");
	   	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   	     DocumentBuilder db;
	   		db = dbf.newDocumentBuilder();      	
	       	FileInputStream in = getCleanInputStream(f);
	       	String handler = null;
	           Document doc = db.parse(in,handler);
	           Node root = getNode(doc, "transport");      
	      
	       	Node read = getNode(root, "priceformulas");
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  try{
	           		  if(rida.getNodeName().equals("priceformula")){
	           			
	           			 String custID = " ";
	                     String clGroup = " ";
	                     String prodID = " ";
	                     String prodGroup = " ";
	                     MatrixLine mline = new MatrixLine();
	                    try{
	                    	 try{
	                    	 clGroup = rida.getAttributes().getNamedItem("code").getNodeValue();
	                           }catch(Exception aa){}
	                    	 try{
		                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
	                    	   }catch(Exception aa){ }
	                    	 try{
	                         prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
	                    	  }catch(Exception aa){ }
	                         String mode = rida.getAttributes().getNamedItem("what").getNodeValue();
	                        
	                         try{
	                        double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
	                        mline.setDiscount(value);
	                         }catch(Exception aa){ }
	                         mline.setProdLevel(mode);
	                       
	                         
	                         if (custID != null && custID.trim().length() > 0) {
	                           mline.setCustID(custID.trim());
	                         }
	                         if (clGroup != null
	                             && clGroup.trim().length() > 0) {
	                           mline.setCliGroup(clGroup.trim());
	                         }

	                         if (prodID != null && prodID.trim().length() > 0) {
	                           mline.setProdID(prodID.trim());
	                         }
	                         
	                         if (mline.getProdID() == null && prodGroup != null
	                             && prodGroup.trim().length() > 0) {
	                           mline.setProdGroup(prodGroup);
	                         }   
	                           lines.add(mline);	
	                    
	                    }catch(Exception e){
	                    	e.printStackTrace();
	                    }
	                    
	                   }
	                	
	           	  }catch(Exception e){
	           		  e.printStackTrace();
	           	  }
	                
	           }
	           
	   	}catch(Exception e){
	   		e.printStackTrace();
	   	}
	       
	   // System.out.println("siin matix" + lines.size());
	       return lines;
	 }
	  
	  
  
  public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	       impCodes = new TreeMap();
	   
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
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  try{
	         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
	         		  Customer customer = new Customer(id);
	         		  String name =  rida.getAttributes().getNamedItem("name").getNodeValue();
	         		  customer.setName(deCode(name));

	         		  try{ 
	         		   String aadr =  rida.getAttributes().getNamedItem("send_address_street").getNodeValue(); 
	         		     customer.setAddress(deCode(aadr));
	         	          }catch(Exception e){
	         		  try{ 
	 	         		 String aadr =  rida.getAttributes().getNamedItem("address_street").getNodeValue(); 
	 	         		  customer.setAddress(deCode(aadr));
	         		 }catch(Exception ee){

		         		 customer.setAddress(""); 
	         		 }
         		  }
	         		  try{
		         			 String city =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue() ;         	
			         		  customer.setCity(city);
		         		  }catch(Exception e){
		         			 try{
			         			 String city =  rida.getAttributes().getNamedItem("address_county").getNodeValue() ;         	
				         		  customer.setCity(city);
		         			}catch(Exception es){

			         			  customer.setCity("");	
		         			}
		         		  }
	         		
	         		  
	         		  
	         		  try{
	         			 String price_list =  rida.getAttributes().getNamedItem("price_list").getNodeValue() ;         	
		         		  customer.setGroup(deCode(price_list));
	         	  
	         		  }catch(Exception e){
	         			  
	         		  }
	         		
	         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
	         		      customer.addAgent(new Agent(deCode(salesman)));
	                 
	         		      try{
	         		    	Node contact = getNode(rida,"contact");
	         		    	String contact_name  = contact.getAttributes().getNamedItem("name").getNodeValue();
	         		    	customer.setInfo(contact_name);
	         		    	
	         		    	String contact_phone  = contact.getAttributes().getNamedItem("telephone").getNodeValue();
	         		    	customer.setPersonalData(contact_phone);
	         		    	
	         		      }catch (Exception e){
	         		    	// e.printStackTrace();
	         		    	 customer.setInfo("");
	         		      }

	         		      
	         		      try{
	         		    	 // System.out.println("Rida " + rida.getChildNodes().toString());
		         		    	Node contact = getNode(rida,"data");
		         		    	String contact_name  = contact.getAttributes().getNamedItem("name").getNodeValue();
		         		    	customer.setInfo(contact_name);
		         		    	
		         		    	String contact_phone  = contact.getAttributes().getNamedItem("telephone").getNodeValue();
		         		    	customer.setPersonalData(contact_phone);
		         		    	
		         		      }catch (Exception e){
		         		    	// e.printStackTrace();
		         		    	 customer.setInfo("");
		         		      }

	         		      
	         		      
	         		      
	         		     tm.put(customer.getID(), customer);
	                      
	         	  }catch(Exception e){
	         		  
	         	  }
	              
	         }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}   
  return tm;
}

  
  
  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public TreeMap getList(){
	  TreeMap companies = new TreeMap();
	    File file = new File("klient" + File.separator + "klient.txt");
	    try{
	      BufferedReader reader = new BufferedReader(new FileReader(file));
	      String s = null;
	      while((s = reader.readLine()) != null){
	        StringTokenizer st = new StringTokenizer(s, ";");
	        if (st.hasMoreTokens()){
	          String compID = st.nextToken();
	          if (compID.trim().equals("")){
	          compID =null;
	          }
	          String name = null;
	          if (compID!= null){
	            name = st.nextToken();
	            companies.put(compID, name);
	          }
	         }
	      }
	    } catch (IOException e){
	      parent.showError(e);
	    }
	    return companies;
	  }
  
  
  public String identifyCustomer(String id,String regnr)  {

	  if(regnr != null && impCodes.get(regnr) != null){
		  return impCodes.get(regnr) + "";
	  }

	  if(id != null && impCodes.get(id) != null){
		  return impCodes.get(id) + "";
	  }
	  
	  if(kliendid.get(id) != null){
		  return id;
	  }
	  
	  if(kliendid.get(regnr) != null){
		  return kliendid.get(regnr) + "";
	  }


	  
    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	  
	  if(eans.get(ean) != null){
		  return eans.get(ean) + "";
	  }
    return null;
}
  
  

  public void makeOrder(Order order) throws SQLException{
       counter = 0;
		 makeOutput(order); 
    
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
  
  
  private double getPrice(String toode ,String firma, Product ppp){
	
	  Customer ccc = (Customer) kliendid.get(firma);
	
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
	  
	//  System.out.println("sss " + kgrupp + " tootegr " + tgrupp+ " tooteM1gr " + tm1grupp + " tooteM2gr " + tm2grupp );
	  
	  while (enumr.hasMoreElements()){
		  MatrixLine mm = (MatrixLine)enumr.nextElement();
		  if(mm.getCliGroup()!= null && mm.getCliGroup().equals(kgrupp) ){
			  //leidsime rea mis on hinnakirja oma
			//  System.out.println("Hinnakiri leitud "  + mm.getCliGroup() + " tr " + mm.getProdGroup() + " tt " + mm.getProdID());
			  if (mm.getProdID() != null && mm.getProdID().equals(ppp.getID())){
				  //leidsime ka toote 

				 // System.out.println("Toode leitud "  + mm.getProdLevel());
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
				 // System.out.println("Tootegrupp  leitud " + mm.getProdLevel());
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
				//  System.out.println("Tootegrupp master 1 leitud " + mm.getProdLevel());
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
				 // System.out.println("Tootegrupp master 2 leitud " + mm.getProdLevel());
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
	//	System.exit(0);    
	  
	  return ppp.getInUnit();
  }
  
  private void makeOutput(Order order){
	  boolean sendcomm = false;
	  if("true".equals(parent.getProps().getProperty("sendemail.allorders"))) {
		  sendcomm = true;
	  }
	 	try {
	 	 
	 		Customer ccc = (Customer)kliendid.get(order.getCompany());
	 
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm = "comment=\""+ order.getInfo()+ "\" " ;
	 		}
	 		//System.out.println("Comment " + comm);
			
	 	    String  order_xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
	          "<transport>" +
	          "<order " + 
	         "webid=\"" + "1" + "\" " + //order.getDokNr()+
	         "webdate=\"" + getDateNiceString(Calendar.getInstance())+ "\" " +
	         "ccode=\"" +deCode( order.getCompany()) + "\" " +
	       //  "send_mode=\"POST\" " + 
	         "send_type=\"ALL\" " +
	         "object=\"4\" " +
             comm + 
	         "amount=\"" + order.getSum() + "\" " +
	         "amount_vat=\"" + order.getSum()* 1.2 + "\" " +
	         "currency=\"EUR\" language=\"EST\">" +
	         "<pricelist>" + ccc.getGroup() + "</pricelist>" +
	         "<send_address>" +
	         "<street>" + deCode(ccc.getAddress()) + "</street>" +
	         "<county>" + deCode(ccc.getCity()) + "</county>" +
	         "<country>0</country>" +
	         "</send_address>" +
	         "<contact>" +
	         "<name>" + deCode(ccc.getInfo()) + "</name>\r\n" +
	         "<phone>" + ccc.getPersonalData() +"</phone>" +
	         "</contact>" +
	         "<lines>" ;
	       
	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  Product ppp = (Product)tooted.get(line.getToode());
		    	  
		    	  order_xml = order_xml + "<line ";
		    	  order_xml = order_xml + "quantity=\"" + line.getKogus() + "\" ";
			      order_xml = order_xml +"pcode=\""+ line.getToode() + "\" ";
		    	 // order_xml = order_xml + "description=\"" + ppp.getName() + "\" " ;
		    	  double hind = 0;
		    	  try{
		    		  hind = ppp.getPrice() ;//getPrice(line.getToode(),order.getCompany(),ppp)+ 0.0005 ;
				    	 	  
		    	  }catch(Exception e){
		    		  
		    	  }
		    	  
                  if(line.getDiscount() > 0) {
                	  //hind = line.getPrice();
                	  order_xml = order_xml +"discount=\""+ line.getDiscount() +"\" ";
    		    	  
                  }
                  
			      order_xml = order_xml +"price=\""+ hind +"\" ";
		    	  order_xml = order_xml + "/>";
			 counter++;
	      }
	 
		  
		      
	 	
	 	
	 	
	 	order_xml = order_xml + "</lines>";
	 	order_xml = order_xml + "</order>";
	 	order_xml = order_xml + "</transport>";
	    System.out.println("output " +order_xml); 
	 	
	 		//System.exit(0);
	 		
	 	try {
	 	    // Construct data
	 	    String data = URLEncoder.encode("put", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8");
	 	    data += "&" + URLEncoder.encode("what", "UTF-8") + "=" + URLEncoder.encode("order", "UTF-8");
	 	    data += "&" + URLEncoder.encode("xmldata", "UTF-8") + "=" + URLEncoder.encode(order_xml, "UTF-8");
	 	    // Send data
	 	   Converter.doTrustToCertificates();
	 	    URL url = new URL( logurl + company + "/xmlcore.asp");
	 	    URLConnection conn = url.openConnection();
	 	    conn.setDoOutput(true);
	 	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	 	    wr.write(data);
	 	    wr.flush();

	 	    // Get the response
	 	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	 	    String line;
	 	    String asi = "";
	 	    while ((line = rd.readLine()) != null) {
	 	        System.out.println(line);
	 	        asi = asi + "\n" + line ;
	 	    }
	 	    wr.close();
	 	    rd.close();
	 	    
	 	   String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
	 	   String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".txt";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, asi);
		      if(asi.contains("error code")){
		    	  trouble = "ORDER Was not Imported :" + asi;
		      }else{
		    	  trouble = null;
		      }
	 	    
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 	}
       

	      
	      setNumAddedLines(counter); 
	      
	    String doknr = order.getDokNr();
	      String fn = doknr;
	      if(doknr.trim().length()== 0){
	    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
	      }
	    
	      
	      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
	      System.out.println(fileName);
	       
	      Converter.saveFileInUTF8(fileName, order_xml);
	    
	      
	      if(order.getStatus() < 5 && sendcomm) {
	    	
	    	  
	    		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
	    		 
	    	   sendCommentEmail( comm ,null,doknr,order.getCompany(),  " " + ccc.getName() , " ",false,ag,parent);
	    	 
	    }

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
  private void sendCommentEmail(String comment1 ,String comment2,String doknr,String custid, String nimi , String chain, boolean price, Agent ag ,ImportPanel parent){
	   	try{
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment = "Comment : " + comment1 + "\n\n" ;
	   		}
	   		
	   		if(comment2 != null && comment2.trim().length() > 0) {
	   			if(comment == null) {
	   				comment = "";
	   			}
	   			
	   			comment = comment + "Price info : " + comment2 + "\n" ;
	   		}
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	                
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		String agent = "";
		  	  	if(ag != null) {
		  	  		agent = ag.getName() ;
		  	  	}
		  	  		mail.setSubjectFilesTo("Order info : "+ agent + " " + custid + " " + nimi);
		  	  		String lisa = "";
		  	  		
		  	  		mail.setEncoding("utf-8");
		  	  		
		  	  		
		  	  		mail.setBodyHtml( "Customer code : " + custid +  "<br>Customer Name : " + nimi + "<br>" +
		  	  			    "Manager : " + agent + "<br><br>" + comment.replaceAll("\n", "<br>") + lisa  + "<br><br>");
		  	  		//mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			//    "Manager :" + agent + "\n" +  "\n" + lisa + comment + "\n\n.");
		  	  		mail.setSendNotice(false);
		  	  		
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + lisa + custid + " "+ nimi + " " + agent + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
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
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

 
  public String getDriverVersion(){
    return "Directo";
  }
  
  public String getTrouble() {
		return trouble;
	}

  private String trouble = null;
  
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


  
  public TreeMap getProdGroups()  {
	     TreeMap   tm = new TreeMap();
	     
	  	try {
	  		  File f = new File (getDir() + "tootegrupid.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "transport");      
	       //   System.out.println(root);
	      	Node read = getNode(root, "itemclasses");
	      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	          for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          	  Node rida = read.getChildNodes().item(i);
	          	  try{
	          		  if(rida.getNodeName().equals("itemclass")){
	               		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
	               		 try{
	               			 
	               		 
	               		 String name  = rida.getAttributes().getNamedItem("default_name").getNodeValue(); 
	               		 group = deCode(group);
	               		  name  =  deCode(name);
	               		  Toode tt = new Toode (group);
	               		  
	               		 String gr  = rida.getAttributes().getNamedItem("masters").getNodeValue(); 
	               		 if(gr.indexOf(",") !=-1){
	               			//System.out.println("on rohkem " + gr);
	               			 String gr1 = gr.substring(0,gr.indexOf(","));
	               			tt.setGr1(gr1);
	               		//	System.out.println("on rohkem1 " + gr1 + " " + gr.substring(gr1.length()));
	               			if(gr.substring(gr1.length()+1).indexOf(",") != 0){
	               				String www = gr.substring(gr1.length()+1).replaceAll(",", "");
	               				if(www.trim().length() != 0){
		               				tt.setGr2(www);	
		               				//System.out.println("on rohkem2 " + tt.getGr2());
	               				}
	               				
	               				
	               			}
	               		 }else{
	               			// System.out.println("ei ole rohkem " + gr);
	               			 tt.setGr1(gr);
	               		 }
	               		 String gr2  = rida.getAttributes().getNamedItem("default_name").getNodeValue(); 
	               		
	               		 
	               		  tt.setGr1(gr2);
	                        tm.put(group, tt); 
	               		}catch (Exception ee){
	              			 
	              		 }
	          		  } 
	          		
	          	  }catch(Exception e){
	          		  e.printStackTrace();
	          		 // System.exit(0);
	          	  }
	               
	          }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	         return tm;
	   }

  
  
  private int counter = 0;
  private ImportPanel parent;
  private TreeMap kliendid = null;
  private TreeMap impCodes = null;
  
  private TreeMap grupid = null;
  private TreeMap eans = null;
  
  
  private TreeMap tooted = null;
  private Vector mlines = null;
  private boolean isAmount = false;


  
}
