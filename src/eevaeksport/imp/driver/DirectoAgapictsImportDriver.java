package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
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
import eevaeksport.EksportFrame;
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

public class DirectoAgapictsImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(DirectoAgapictsImportDriver.class.getName());

  public DirectoAgapictsImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    eans = new TreeMap();
    company = parent.getProps().getProperty("company");
    kliendid = getCustomers(null);
    tooted = this.getProducts();
    mlines = getFixedAssortmentData();
    grupid = this.getProdGroups();  
    loadSignatures();
    try {
    	  konverter = EksportFrame.loadProps("abifailid/konverter.txt");
    	 // System.out.println("konverter " + konverter.toString());
    }catch (Exception e) {
    	e.printStackTrace();
    }
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
  
  }

    boolean testing = false;
	private Vector signatures = new Vector();

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
        	  
	          reader.close();
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
	  
	  private HashMap custFixedProds = null;
	  
  
	  public Vector getFixedAssortmentData() {
	       Vector lines = new Vector();
	       custFixedProds = new HashMap();
	       try {
	   		  File f = new File (getDir() + "hinnakiri.xml");
	   		  System.out.println("siin matix");
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
	                     String start = " ";
	                     String end = " ";
	                     MatrixLine mline = new MatrixLine();
	                    try{
	                    	 try{
	                    	     clGroup = rida.getAttributes().getNamedItem("code").getNodeValue();
	                           }catch(Exception aa){}
	                    	 try{
		                         prodID =  rida.getAttributes().getNamedItem("datafieldvalue").getNodeValue();  
	                    	   }catch(Exception aa){ }
	                    	 try{
	                             prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
	                    	  }catch(Exception aa){ }
	                    	  try{
		                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
		                    	  }catch(Exception aa){ }
		                    	  try{
			                             end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
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
	                         
	                         if(start != " " && end != " "){
	                        	 mline.setStartDate(start);
	                        	 mline.setEndDate(end);
	                             Date alg = getCalXML(start); 
	                             Date lopp = getCalXML(end); 
	                             
	                             if(alg.before(new Date()) && lopp.after(new Date())){
	                            	
	                            	 lines.add(mline);	
	                            	 
	                            	 HashSet hash = (HashSet)custFixedProds.get(clGroup);
	  	                           if(hash == null) {
	  	                        	   hash = new HashSet();
	  	                           }

		                           if(prodID != null) {
	  	                           hash.add(prodID.trim());
	  	                           custFixedProds.put(clGroup, hash);
		                           }
	                             }
	                           
	                         }else{
	                        	 lines.add(mline);
	                        	 HashSet hash = (HashSet)custFixedProds.get(clGroup);
		                           if(hash == null) {
		                        	   hash = new HashSet();
		                           }
		                           if(prodID != null) {
		                           hash.add(prodID.trim());
		                           custFixedProds.put(clGroup, hash);
		                           }
	     	                 }
	                    
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
	       
	    System.out.println("siin custFixedProds" + custFixedProds.size());
	       return lines;
	 }
	  
	  
	  
	  private Date getCalXML(String ss){
		  Calendar cc = Calendar.getInstance();
		  	
		  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
		  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
		  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
		     
		  	return cc.getTime() ;
		  		
	  }
	  
	  
  
  public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     impCodes =new TreeMap();
	   
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
	                 //System.out.println(rida.getChildNodes().getLength());
	                 
	                 for(int ii = 0;ii<rida.getChildNodes().getLength();ii++){
	                	 Node nn = rida.getChildNodes().item(ii);
	                	 if(nn.getNodeName().equals("data") && nn.getAttributes().getNamedItem("code").getNodeValue().equals("IIMP")){
	                	  	
	       	                impCodes.put( nn.getAttributes().getNamedItem("content").getNodeValue(),customer.getID());		 
	                	 }
	                 }
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
	         		      
	         		     tm.put(customer.getID(), customer);
	                      
	         	  }catch(Exception e){
	         		  
	         	  }
	              
	         }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}   
	 	System.out.println("ImpCodes " + impCodes.toString());
	 	
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
	  
	  if(regnr != null && kliendid.get(regnr) != null){
		  return regnr;
	  }
	  

	  
    return null;
  }

  Properties konverter = null;
  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	  
	
	  if(konverter != null && konverter.getProperty(ean) != null) {
		  ean = (String)konverter.getProperty(ean);
	  }
	  
	  
	  if(eans.get(ean) != null){
		  return eans.get(ean) + "";
	  }
    return null;
}
  
  private StringBuffer troubles= new StringBuffer();

  public void makeOrder(Order order) throws SQLException{
       counter = 0;
       String signature = order.getSignature();
       
       if(signatures == null) {
    	   signatures = new Vector();
       }
       
 	 if(signatures.contains(signature)){
 		  this.troubles.append( "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany()) ;
 	   } else {
 		   
 		   
 		   addToMemory(order);
 		   
 		  Vector splitted =  splitOrder(order);
	 	  Enumeration en = splitted.elements();
	 	  while(en.hasMoreElements()){
	 		  Order ord = (Order)en.nextElement();
	 			  makeOutput(ord);  
		    }
 		 
 		  signatures.add(signature);
 		  this.troubles = new StringBuffer();
 	  }	  
		
 	 try {
			Converter.saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
  
  private void addToMemory(Order order) {
		HashMap mem = null;  
	try {
		File f = new File("ordermemory.vec");
		if(f.exists()) {
		    mem =  (HashMap) Converter.readObject("ordermemory.vec");
		}else {
			mem = new HashMap();
		}
		mem.put(order.getCompany(), order.getAgent()+ "|"+ Calendar.getInstance().getTimeInMillis() );
	
		Converter.saveObject(mem, "ordermemory.vec");
		
	} catch (ClassNotFoundException | IOException e) {
		e.printStackTrace();
	}
  }
  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));


	 
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
  
  
  private double getPriceAction(String toode ,String firma, Product ppp){
	
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
  

  
  public static String protectSpecialCharacters(String originalUnprotectedString) {
	    if (originalUnprotectedString == null) {
	        return null;
	    }
	    boolean anyCharactersProtected = false;

	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < originalUnprotectedString.length(); i++) {
	        char ch = originalUnprotectedString.charAt(i);

	        boolean controlCharacter = ch < 32;
	        boolean unicodeButNotAscii = ch > 126;
	        boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

	        if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
	           // stringBuffer.append("&#" + (int) ch + ";");
	            anyCharactersProtected = true;
	        } else {
	            stringBuffer.append(ch);
	        }
	    }
	    if (anyCharactersProtected == false) {
	        return originalUnprotectedString;
	    }

	    return stringBuffer.toString();
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
	
  
  void saveProps(Properties props, String filename, String header)
		throws IOException {
			FileOutputStream fos = new FileOutputStream(filename);
			props.save(fos, header);
			fos.close();
		}
  
  
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
      
	 System.out.println("Enne suurus splitti" + order.getOrderLines().size());
	 
	 // Vaatame mitu erinevat partiid on olemas
	 
	  Vector<String> splits = new Vector<String>() ;

	  Vector lines = order.getOrderLines();
	  Enumeration enums = lines.elements();
	  while  (enums.hasMoreElements()){
		  OrderLine lin = (OrderLine)enums.nextElement();
			
		     String partii = lin.getPartii();
		     if(partii != null && partii.trim().length() > 1 && !splits.contains(partii)) {
		    	 splits.add(partii);
		     }
		 //    System.out.println("partii " + partii);
	  }
		  
	  System.out.println("splits " + splits.toString());
	// System.exit(0);
	  
	  if(splits.size() == 0) {
		  result.add(order);
		  return result;
	  }
	  
	  HashMap ords = new HashMap();
	  for(String split : splits ) {
		  // teeme kehad valmis enne kui ridu paneme sisse
		  Order ord23 = new Order(order.getCompany(),order.getAgentFirstName());
		  ord23.setAgentCode(order.getAgentCode());
		  ord23.setDokNr(order.getDokNr());
		  ord23.setInfo(order.getInfo());
		  ord23.setReference(split);
		  ord23.setTeostaja(split);
		  ord23.setChanged(order.isChanged());
		  ord23.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ord23.setStatus(order.getStatus());
		  ord23.setShipTime(order.getShipTime());
		  ord23.setShipDateUsed(order.isShipDateUsed());
		  ord23.setWarehouse(order.getWarehouse());
		  lines = order.getOrderLines();
		   enums = lines.elements();
		  while  (enums.hasMoreElements()){
			  OrderLine lin = (OrderLine)enums.nextElement();
			
			     String part = lin.getPartii();
			     if(part.equals(split)) {
			    	   ord23.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     		 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,lin.docType);
			     }
		  }
		ords.put(split, ord23);
	  }
	  
	  for(String split : splits ) {
		  result.add(ords.get(split));
	  }
	
	//  System.out.println("Splitin Warehouse " + order.getWarehouse());
	

	 System.out.println("Result =" +order.getDokNr()
		 + " "+ result.size());
//	 System.exit(0);
	  return result ;
  }

  
  
  private void makeOutput(Order order){
	 	try {
	 	 
	 		Customer ccc = (Customer)kliendid.get(order.getCompany());
	 
	 		boolean sortOK = true;
	 		HashSet hh = null;
	 		Customer cust = (Customer) kliendid.get(order.getCompany());
	 		if(cust != null && cust.getGroup().trim().length() > 1) {
		 	 hh = (HashSet)	custFixedProds.get(cust.getGroup());
		 	  Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  Product ppp = (Product)tooted.get(line.getToode());
		    	  if(ppp != null && hh != null && !hh.contains(ppp.getEAN())) {
		    		  sortOK = false;  
		    	  }
		      }
	 		}
	 		
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm =  order.getInfo();
	 			 comm = comm.replace('"', ' ');
		 		 comm = comm.replace('"', ' ');
		 		 comm = comm.replace('"', ' ');
	 			
	 			String sort = "";
		 		if(!sortOK) {
		 			sort = " EkstraToodeTellil";
		 		}
		 		comm = "comment=\""+comm + sort +"\" "  ;
	 			if(order.getStatus() > 5){
		 			comm = "cutomer_orderno=\""+ order.getInfo()+ "\" " ;
		 		}
	 		}else {
	 			
		 		if(!sortOK) {
		 			comm = "comment=\"EkstraToodeTellil\" "  ;
		 		}
		 	}
	 		
	 		
	 		 comm = protectSpecialCharacters(comm);
	 	
	 		System.out.println("comm " + comm);
	 		String gr = "";
	 		if(ccc != null && ccc.getGroup()!= null){
	 			gr = ccc.getGroup();
	 		}
	 		String adr = "";
	 		if(ccc != null && ccc.getAddress()!= null){
	 			adr = deCode(ccc.getAddress());
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
	 		order.setSum(0);
	 		String ordnum = getNum()+ "";
	 		
	 		 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
			 		if(line.getUnit().length() > 3) {
			 			ordnum = line.getUnit();
			 		}
		      }
		      String teo = "";
	 		if(order.getTeostaja() != null && order.getTeostaja().trim().length() > 1) {
	 			 teo = order.getTeostaja();
	 			if(teo.indexOf("|") != -1) {
		 			teo = teo.replace('|', ' ').replace('|', ' ').trim();
		 		}
	 			if(!company.equals("agapics_lv")) {
	 				ordnum = teo;
	 			}
	 			teo = "cutomer_orderno=\""+ teo + "\" " ;
	 			
	 		}

	 		String shdate = "";
	 		if(order.isShipDateUsed()) {
	 			
	 			shdate = "deliverydate=\""+ getDateNiceString(order.getShipCal()) + "\" " ;
	 		}
	 		
	 		if(ordnum.indexOf("|") != -1) {
	 			ordnum = ordnum.replace('|', ' ').replace('|', ' ').trim();
	 		}
	 	    
	 		String hooaeg = "";
	 		try{
	        	if(order.getShipMode() != null  && order.getShipMode().equals("-3")){
	        		hooaeg = "datafield3=\"Hooajaline\" " ;
	 	        }	
	        }catch(Exception e){
	        	
	        }
	 		
	 		
	 		
	 		
	 		String  order_xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
	          "<transport>" +
	          "<order " + 
	         "webid=\"" + ordnum + "\" " + //order.getDokNr()+
	         "webdate=\"" + getDateNiceString(Calendar.getInstance())+ "\" " +
	         "ccode=\"" +deCode( order.getCompany()) + "\" " +
	         "send_mode=\"POST\" " + 
	         "send_type=\"ALL\" " +
	         "object=\"4\" " +
             comm + teo + shdate + hooaeg + 
	         "amount=\"" + order.getSum() + "\" " +
	         "amount_vat=\"" + order.getSum()* 1.2 + "\" " +
	         "currency=\"EUR\" status=\"KOMPLEKTEERI\" language=\"EST\">" +
	         
	         "<pricelist>" + gr + "</pricelist>" +
	         "<send_address>" +
	         "<street>" + adr + "</street>" +
	         "<county>" + city + "</county>" +
	         "<country>0</country>" +
	         "</send_address>" +
	         "<contact>" +
	         "<name>" + nam + "</name>\r\n" +
	         "<phone>" + pers +"</phone>" +
	         "</contact>" +
	         "<lines>" ;
	       
	     	  lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  Product ppp = (Product)tooted.get(line.getToode());
		    	  
		    	  order_xml = order_xml + "<line ";
		    	  order_xml = order_xml + "quantity=\"" + line.getKogus() + "\" ";
			      order_xml = order_xml +"pcode=\""+ Converter.deCode(line.getToode()) + "\" ";
		    	 // order_xml = order_xml + "description=\"" + ppp.getName() + "\" " ;
		    	  double hind = 0;
		    	  
		    	  
		    	  try{
		    		  hind =  this.getPriceAction(line.getToode(),order.getCompany(),ppp)+ 0.0005 ;
				  }catch(Exception e){
		    	  }
				 // System.out.println("HindAction " + line.getToode() + " " + hind);
			    	
				  if(hind < 0.0006){
                      try{
			    		  hind =  getPrice(line.getToode(),order.getCompany(),ppp)+ 0.0005 ;
					  }catch(Exception e){
			    	  }
			     }
		    	  
		    	//System.out.println("Hind "Muudatus " " + hind);
		    	  
                  if(line.getDiscount() > 0) {
                	  hind = line.getPrice();
                	  order_xml = order_xml +"discount=\""+ line.getDiscount() +"\" ";
                	  order_xml = order_xml +"price=\""+ Converter.Round((float)hind, 2) +"\" ";
    		      }
			    
		    	  order_xml = order_xml + "/>";
			 counter++;
	      }
	 
		      
	 	order_xml = order_xml + "</lines>";
	 	order_xml = order_xml + "</order>";
	 	order_xml = order_xml + "</transport>";
	    System.out.println("output " +order_xml); 
	 	
	 		//System.exit(0);
	    if(!testing) {
	    String asi = "";
	 	try {
	 	    // Construct data
	 	    String data = URLEncoder.encode("put", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8");
	 	    data += "&" + URLEncoder.encode("what", "UTF-8") + "=" + URLEncoder.encode("order", "UTF-8");
	 	    data += "&" + URLEncoder.encode("xmldata", "UTF-8") + "=" + URLEncoder.encode(order_xml, "UTF-8");
	 	    // Send data
	 	   Converter.doTrustToCertificates();
	 	    URL url = new URL("https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp");
	 	    URLConnection conn = url.openConnection();
	 	    conn.setDoOutput(true);
	 	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	 	    wr.write(data);
	 	    wr.flush();
try {
	 	    // Get the response
	 	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	 	    String line;
	 	   
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
		      if(asi.contains("error code") || asi.contains("Imported 0 document")){
		    	  trouble = "ORDER Was not Imported :" + asi;
		      }else{
		    	  trouble = null;
		      }
	 	    
		      
			 	} catch (Exception e) {
			 		e.printStackTrace();
			 		log.error(e);
			 	}
		      
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 		log.error(e);
	 		System.exit(0);
	 	}
       
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
	    

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
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
  private TreeMap grupid = null;
  private TreeMap eans = null;
  private TreeMap impCodes = null;
  
  
  private TreeMap tooted = null;
  private Vector mlines = null;
  private boolean isAmount = false;

  public String getTrouble() {
		return trouble;
	}

  private String trouble = null;

  
}
