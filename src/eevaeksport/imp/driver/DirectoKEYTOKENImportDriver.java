package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.w3c.dom.NodeList;

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

public class DirectoKEYTOKENImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public DirectoKEYTOKENImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    eans = new TreeMap();
    company = parent.getProps().getProperty("company");
    kliendid = getCustomers(null);
    tooted = this.getProducts(null);
    grupid = this.getProdGroups(); 
    mlines = getPriceMatrixData();
    companytoken = parent.getProps().getProperty("companytoken");
    
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
	   		
	   		
	   		if(comment == null) {
	   			comment = "";
	   		}
	   			
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
		  				
	   		
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
  
  public TreeMap getProducts(Agent agent){
		 
    TreeMap tm = new TreeMap();
    
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
	        	  try{
	        		  if(rida.getNodeName().equals("item")){
	        			 
	        			  boolean goes = true;
	        			  
	        			 String id = rida.getAttributes().getNamedItem("code").getNodeValue();
	        			 
	        			 Node nd = rida.getAttributes().getNamedItem("name");
	        			 if(nd != null) {
	        			 String name = rida.getAttributes().getNamedItem("name").getNodeValue();
	        			
	        			    Product prod = new Product(id);
	        			    prod.setName(name);
	        			    
	        	      	    	
		        			 try{
			        			 prod.setUnit(rida.getAttributes().getNamedItem("unit").getNodeValue());
		                 	    } catch(Exception aa){}	   
		        			 
		        			 
		        			 String add = "";
	        			 try{
	        				
	        			 String ean = rida.getAttributes().getNamedItem("barcode").getNodeValue();
	                 	     prod.setEAN(ean);
	                  	     
	                    	   if("true".equals(parent.getProps().getProperty("add.ean"))){
	                    		   try{ add = ean.substring(ean.length()-4); 
	                    	       }catch(Exception e ){}
	                    		 }
	                       }catch(Exception aa){}


      		        	  if("true".equals(parent.getProps().getProperty("name.code.front"))){
      		     			 prod.setName((id + " " + name + " " +  add).trim()); 
      		     		 }else{
      		     			 prod.setName((name + " " + id + " " + add).trim()); 
      		     		 }
	        			

      		        	  if(parent.getProps().getProperty("vat") != null){
      		        		  prod.setVat(parent.getProps().getProperty("vat"));
      		        	  }
      		      	    
      		      	     prod.setPrice(1);
      		      	     
      		      	    try{
	        			 prod.setPrice(Double.parseDouble(rida.getAttributes().getNamedItem("salesprice").getNodeValue()));
                	    } catch(Exception aa){}	 
      		      	    
      		      	 try{
	        			 prod.setInCases(Double.parseDouble(rida.getAttributes().getNamedItem("package1").getNodeValue()));
                	    } catch(Exception aa){}	 
      		      	    
      		      try{
	        			 prod.setGroup(rida.getAttributes().getNamedItem("class").getNodeValue());
	        			 
             	    } catch(Exception aa){}	   
    			 
   		      		         
      		         	    if(goes ){
      		         	    	 tm.put(prod.getID(), prod);	
      		         	        } 
	               
      		        	  
	        }}
	        	  }catch (Exception e) {
	        		  e.printStackTrace();
	        	  }
	        }

   
    } catch (Exception e){
      e.printStackTrace();
    }
   
    
      System.out.println("tooted tehtud " + tm.size());
    
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
	  
	  
  
		public Vector getPriceMatrixData() {
			   	Vector lines = new Vector();
			    
			    try {
					  File f = new File (getDir() + "hinnakiri.xml");
					//  System.out.println("siin matix");
				     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				     DocumentBuilder db;
					db = dbf.newDocumentBuilder();      	
			    	FileInputStream in = getCleanInputStream(f);
			    	String handler = null;
			        Document doc = db.parse(in,handler);
			        Node root = getNode(doc, "transport");    
			     
			        Calendar cc = Calendar.getInstance();
			        cc.add(Calendar.DATE, 1);
			        Date all = cc.getTime();
			       
			    	Node read = getNode(root, "priceformularows");
			        for (int i = 0; i < read.getChildNodes().getLength(); i++){
			        	  Node rida = read.getChildNodes().item(i);
			        	  try{
			        		  if(rida.getNodeName().equals("priceformularow")){
			        			
			        			 String custID = " ";
			                  String clGroup = " ";
			                  String prodID = " ";
			                  String prodGroup = " ";
			                  String start = " ";
			                  String end = " ";
			                  String currency = "";
			                  MatrixLine mline = new MatrixLine();
			                 try{
			                 	 try{
			                 	     clGroup = ensureLen(rida.getAttributes().getNamedItem("code").getNodeValue());
			                        }catch(Exception aa){}
			                 	 try{
				                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
			                 	   }catch(Exception aa){ }
			                 	 try{
			                          prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
			                 	  }catch(Exception aa){ }
			                 	  try{
				                             start =  rida.getAttributes().getNamedItem("date1").getNodeValue();
				                    	  }catch(Exception aa){ }
				                    	  try{
					                             end =  rida.getAttributes().getNamedItem("date2").getNodeValue();
					                    	  }catch(Exception aa){ }
				                    	  try{
					                             currency =  rida.getAttributes().getNamedItem("currency").getNodeValue();
					                    	  }catch(Exception aa){ }
				                    	  String mode = null;  
				                    	  try{  	  
			                       mode = rida.getAttributes().getNamedItem("type").getNodeValue();
				                    	  }catch(Exception aa){ }
			                      try{
			                     double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
			                     if(mode != null && mode.equals("8")){
			                    	 mline.setPrice(value);
			                     }else{
			                         mline.setDiscount(value);
			                     }
			                      }catch(Exception aa){ }
			                  
			                      
			                      mline.setProdLevel(mode);
			                    
			                      
			                      if (custID != null && custID.trim().length() > 0) {
			                        mline.setCustID(custID.trim());
			                      }
			                      if (currency != null && currency.trim().length() > 0) {
			                          mline.setCurrency(currency.trim());
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
			                      
			                      if(!clGroup.equals("00001")) {
			                      if(start != " " && end != " "){
			                    	  Date alg = getCalXML(start); 
			                          Date lopp = getCalXML(end); 
			                          
			                    	  start = start.substring(0,10).substring(5);
			                    	  end = end.substring(0,10).substring(5);
			                    	  mline.setStartDate(start);
			                     	  mline.setEndDate(end);
			                          
			                          if(alg.before(all) && lopp.after(new Date())){
			                         	 lines.add(mline);	
			                          }
			                          
			                      } else {
			                     	 lines.add(mline);	
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
			    
			 System.out.println("siin matrix" + lines.size());
			    return lines;


			   }
		 private String ensureLen(String in ){
			 
			 if(in.length() == 4){
				 in = "0" + in ;
			 }
			 if(in.length() == 3){
				 in = "00" + in ;
			 }
			 if(in.length() == 2){
				 in = "000" + in ;
			 }

			 if(in.length() == 1){
				 in = "0000" + in ;
			 }
			 return in;
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
	 
	   try {
	 		  File f = new File (getDir() + "kliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport");      
	     	Node read = getNode(root, "customers");
	     	
	     	HashMap<String, Customer> payersDebt = new HashMap<String, Customer>();

	     	HashMap<String, String> payersPRLists = new HashMap<String, String>();
	     	
	     	  for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  if(rida.getNodeName().equals("customer")){
	         		 String name = null;
	         		boolean addaadr = true;
		         	  try{
		         		
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		  Customer cust = new Customer(id);
		         		 String vatregno = "";
			         		try{ 
			         			vatregno =  rida.getAttributes().getNamedItem("vatregno").getNodeValue(); 
			 	         		cust.setVatnr(vatregno);
			 	         		cust.setPayer(vatregno);
			 	         		
			 	         		if(rida.getAttributes().getNamedItem("specialpriceformula") != null) {
			 	         	      String price_list =  rida.getAttributes().getNamedItem("specialpriceformula").getNodeValue();

		 	         	    	  String old = payersPRLists.get(vatregno);
			 	         	      if(old != null) {
			 	         	    	  if(!old.contains(price_list+ ",")) {
			 	         	    	     payersPRLists.put(vatregno, old +  price_list + "," );
			 	         	    	  }
			 	         	       }else {
			 	         	         payersPRLists.put(vatregno, price_list + ",");
			 	         	      }
			 	         		}
			 	         		
				         		Double limit = 0.0;
				         		try{ 
				         			limit =  Double.parseDouble(rida.getAttributes().getNamedItem("creditlimit").getNodeValue()); 
				         			if(limit > 0)
				 	         		cust.setCredit(limit+"");
				 	         	 }catch(Exception ee){
				         		 }
		                         
				         	
				         		Double debt = null;
				         		try{ 
				         			debt =  Double.parseDouble(rida.getAttributes().getNamedItem("balance").getNodeValue()); 
				         			if(debt > 0) {
				 	         		cust.setUsedCredit(debt);
				         			payersDebt.put(vatregno, cust);
				         			}
				 	         	 }catch(Exception ee){
				         		 }
			 	         		
			 	         	 }catch(Exception ee){
			         		 }
	                         
		         	  }catch(Exception e) {
		         		  
		         	  }
	         	  }
	     	  }
	     	  
	     	 read = getNode(root, "customers");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("customer")){
	         		 String name = null;
	         		boolean addaadr = true;
		         	  try{
		         		   name =  rida.getAttributes().getNamedItem("name").getNodeValue();
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		  Customer customer = new Customer(id);
		         		  customer.restName = name;
		         		  String kkood = id;
		         		  try{
		         			  String payer = rida.getAttributes().getNamedItem("payer").getNodeValue();
			         		  
			         		  if(payer != null && payer.trim().length() > 0 ){
			         			  customer.setPayer(payer.trim());
			         		  }
	  
		         		  }catch(Exception e){
		         			  
		         		  }
		         		
		         	
		         		
		         		 String country = "Estonia";
		       		  if(parent.getProps().getProperty("country") != null){
		       			  country = parent.getProps().getProperty("country");
		       		  }
		       		//deliveryaddress1="Klonėnų vs. 1" deliveryaddress2="19156 Širvintų r" deliveryaddress3="Leedu" country="LT"
		         		 String kname = name;
		         		 String sendaadr = ""; 
		         		 
		         		try { 
		 	         		  sendaadr =  rida.getAttributes().getNamedItem("deliveryaddress1").getNodeValue();
		 	         		  String linn = "";
		 	         		try {
				 	         	    linn =  rida.getAttributes().getNamedItem("deliveryaddress2").getNodeValue().replaceAll(";", ","); 
				 	         	    sendaadr = sendaadr + " " + linn;
				 	         		customer.restCity = linn;
				 	         		}catch(Exception eed){
					         		 }
		 	         		  customer.restStreet = sendaadr.replaceAll(";", ",");
		 	         		  name = name + " " + sendaadr;
		 	         		addaadr = false;
		 	         		
		 	         		String maa = "";
		 	         		try{
		 	         	      maa =  rida.getAttributes().getNamedItem("deliveryaddress3").getNodeValue().replaceAll(";", ","); 
		 	         		  country = maa;

		 	         		  if("true".equals(parent.getProps().getProperty("use.country"))){
		 	         			name = name + " " + maa;	  
		 	         		  }
		 	         		
		 	         		} catch(Exception eed){
			         		 
		 	         		}
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	
		         		
		         		} catch (Exception ee){
		         		 
		         		}
			         	
		         		
		         		String asula = "";
		         		try{ 
		 	         		  asula =  rida.getAttributes().getNamedItem("deliveryaddress2").getNodeValue().replaceAll(";", ","); 
		 	         		sendaadr = sendaadr + " " + asula;
		 	         		customer.restCity = asula; 
		 	         	
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("deliveryaddress3").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	 }catch(Exception ee){
		         		 }
		         		

                        
		         		String vatregno = "";
		         		try{ 
		         			vatregno =  rida.getAttributes().getNamedItem("vatregno").getNodeValue(); 
		 	         		customer.setVatnr(vatregno);
		 	         		customer.setPayer(vatregno);
		 	         	 }catch(Exception ee){
		         		 }
                        
		         		
		         

		         		try{ 
		         			double discount  =  Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
		         			if(discount > 0){
		         		 		customer.setDiscount(discount);
		    		 	  }
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		
		         		
		         		 
		         		 
		         		  customer.setName(name);
		         		 
		         		
		         		 //contact="Justyna Marciniak-Ashtiani" phone="+358503235307" email="justyna.marciniak-ashtiani@edenred.com"
		         		
		         		
		         		  
		         		  try{
		         			 String price_list =  ensureLen(rida.getAttributes().getNamedItem("priceformula").getNodeValue());         	
			         		   customer.setGroup(Converter.deCode(price_list));
			         		  if("true".equals(parent.getProps().getProperty("fixed.frompricelist"))){
			       			   customer.addFixSortCode(Converter.deCode(price_list)); 
			       		       }
		         		  }catch(Exception e){
		         		  }
		         		  
		         		  
		         		  
		
		         		  try{
		         			  String	 price_list =  rida.getAttributes().getNamedItem("specialpriceformula").getNodeValue() ;
		         			  
		         			  StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         				  
                                  String as = ensureLen(ss.nextToken());
			         			  
                                 if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
                               	  if(!customer.getGroup().contains(as+"|")) {
			         				    customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
                               	  }
					         	    }else{
			         				 customer.setGroup(Converter.deCode(as));
					         	   }
					         		
                                  if("true".equals(parent.getProps().getProperty("fixed.frompricelist"))){
					       			   customer.addFixSortCode(Converter.deCode(as)); 
					       		   }  
		         			  }
		         			  
			         		  }catch(Exception e){
			         		  }
		         		  
		         		if( vatregno != null &&  payersPRLists.get(vatregno) != null) {
		         			String price_list = payersPRLists.get(vatregno);
		         			StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         			     String as = ensureLen(ss.nextToken());
			         		      if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
			         		    	 if(!customer.getGroup().contains(as+"|")) {
				         			 customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
			         		    	 }
			         		    	 } else {
			         				 customer.setGroup(Converter.deCode(as));
					         	    }
					         		
                                if("true".equals(parent.getProps().getProperty("fixed.frompricelist"))){
					       			   customer.addFixSortCode(Converter.deCode(as)); 
					       		   }  
		         			  }
		         		}
		 	         		
		         		  
	        		      
	         		        tm.put(customer.getID(), customer);
	                  
    		
    		   	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	              
	         	  }}
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	
	   System.out.println("cust size " + tm.size());
		   
    return tm;
  }

  
  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  
  public String identifyCustomer(String id,String regnr)  {
	  
	  if(regnr != null && impCodes.get(regnr.trim()) != null){
		  return impCodes.get(regnr.trim()) + "";
	  }  

	  if(id != null && impCodes.get(id.trim()) != null){
		  return impCodes.get(id.trim()) + "";
	  }
	  
	  if(kliendid.get(id) != null){
		  return id;
	  }
	  
	  if(regnr != null && kliendid.get(regnr) != null){
		  return regnr;
	  }
	  
	 // System.out.println("imp '" + id + "' '" + regnr + "' " + impCodes.get(id.trim()) );
	//  System.exit(0);

	  
    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	  
	  if(eans.get(ean) != null){
		  return eans.get(ean) + "";
	  }
	  
	  if(ean != null && eans.get(ean.trim()) != null){
		  return eans.get(ean.trim()) + "";
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
	 
	   sb.append(  year + "-");
	    if (month.length() == 1){
		     sb.append("0");
		   }
		     sb.append(month  + "-");
	    
	    if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	   
	     if (hour.length() == 1){
		      hour = "0" + hour ;
		    }
	     
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
	  if(ccc != null){
		    kgrupp = ccc.getGroup();		 
	      }
	    
	  if(ppp != null){
			tgrupp = ppp.getGroup();		 
		  }
	  
	  Enumeration enumr = mlines.elements();
	  
	//  System.out.println("sss " + kgrupp + " tootegr " + tgrupp+ " tooteM1gr " + tm1grupp + " tooteM2gr " + tm2grupp );
	  
	  while (enumr.hasMoreElements()){
		  MatrixLine mm = (MatrixLine)enumr.nextElement();

		  if(mm.getStartDate()!= null && mm.getStartDate().trim().length() > 1){
			//if(mm.getCliGroup() != null && mm.getCliGroup().indexOf("Elva") != -1) {
			//  System.out.println("Hinnakiri "  + mm.getCliGroup() + " " + kgrupp + " " + mm.getStartDate());
	//}
		  if(mm.getCliGroup()!= null && kgrupp.contains(mm.getCliGroup()) ){
			  //leidsime rea mis on hinnakirja oma
			//  System.out.println("Hinnakiri leitud "  + mm.getCliGroup());
			  if (mm.getProdID() != null && mm.getProdID().equals(ppp.getID())){
				  //leidsime ka toote 

				   String mode = mm.getProdLevel();
				   double value = mm.getDiscount();
				   double price = mm.getPrice();

				//	  System.out.println("Toode leitud "  + mm.getProdLevel() + " " + value + " " + price);
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
					   return price ;
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
					   return mm.getPrice() ;
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
  void saveProps(Properties props, String filename, String header)
		throws IOException {
			FileOutputStream fos = new FileOutputStream(filename);
			props.save(fos, header);
			fos.close();
		}

  private String companytoken = null;

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
  
  private void makeOutput(Order order){
	  boolean sendcomm = false;
	  if("true".equals(parent.getProps().getProperty("sendemail.allorders"))) {
		  sendcomm = true;
	  }
	 	try {
	 		String ordnum = getNum()+ "";
	 		Customer ccc = (Customer)kliendid.get(order.getCompany());
	 
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm = "text1=\""+ escape(order.getInfo())+ "\" " ;
	 			if(order.getStatus() > 5 && order.getStatus() != 1000){
		 			comm = "customerordernumber=\""+ escape(order.getInfo())+ "\" " ;
		 		}
	 		}
	 		if(order.getStatus() == 1000){
	 			comm = comm+"customerordernumber=\""+ order.getDokNr()+ "\" " ;
	 		}
	 		// comm = protectSpecialCharacters(comm);
	 		System.out.println("comm " + comm);
	   
	 		String gr = "";
	 		if(ccc != null && ccc.getGroup()!= null){
	 			gr = ccc.getGroup();
	 		}
	 		String adr = "";
	 		if(ccc != null && ccc.restStreet!= null){
	 			adr = deCode(ccc.restStreet);
	 		} 
	 		
	 		String city = "";
	 		if(ccc != null && ccc.restCity!= null){
	 			city = deCode(ccc.restCity);
	 		}
	 		
	 		String maa = "";
	 		if(ccc != null && ccc.restZipCode!= null){
	 			maa = deCode(ccc.restZipCode);
	 		}
	 		String nam = "";
	 		if(ccc != null && ccc.getName()!= null){
	 			nam = deCode(ccc.getName());
	 		}
	 		
	 		double vat = 1;
	 		if(ccc != null && ccc.getVAT() == 0){
	 			vat = 0;
	 		}
	 		String lan ="EST";
	 		if(order.getAgent().equals("DOVILE")) {
	 			lan = "LT";
	 		}
	 		
	 		
	 		System.out.println("Comment " + comm);
	 		order.setSum(0);
	 		
	 		String ladu = "";
	 		 if("true".equals(parent.getProps().getProperty("order.setware"))) {
	 			 ladu =  "object=\"" + parent.getProps().getProperty("stockcode") +  "\" "  ; 
	 		  }
			
	 		 if(order.getWarehouse() != null && order.getWarehouse().trim().length() >0) {
	 			 ladu =  "object=\"" + order.getWarehouse() +  "\" "  ; 
	 		 }
	 		 String priceList = "<pricelist>" + gr + "</pricelist>";
	 		 if (order.getStatus() == 1000) {
	 			 priceList = "";
	 		 }
	 		 
	 		 
	 	
	 		 
	 	    String  order_xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
	          "<orders>" +
	          "<order " + 
	         "number=\"" + ordnum + "\" " + //order.getDokNr()+
	         "date=\"" + getDateNiceString(Calendar.getInstance())+ "\" " +
	         "customercode=\"" +deCode( order.getCompany()) + "\" " +
	         "deliveryname=\"" + nam + "\" " +
	         "deliveryaddress1=\"" + adr + "\" " +
	         "deliveryaddress2=\"" + city + "\" " +
	         "deliveryaddress3=\"" + maa + "\" " +
	         
	          comm + 
              ladu +
	         "currency=\"EUR\" language=\"" + lan 
	         + "\">" +
	         
	         "<rows>" ;
	       
	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  Product ppp = (Product)tooted.get(line.getToode());
		    	 // <row item="0001" price="37.5" quantity="1" sum="37.5" rn="1" rr="1"/>
		    	  order_xml = order_xml + "<row ";
		    	  order_xml = order_xml + "quantity=\"" + line.getKogus() + "\" ";
			      order_xml = order_xml +"item=\""+ Converter.deCode(line.getToode()) + "\" ";
		    	 
			      if(line.getPartii() != null && line.getPartii().trim().length() > 0) {
			    	  order_xml = order_xml + "comment=\"" + line.getPartii() + "\" " ;
				  }
			      // order_xml = order_xml + "description=\"" + ppp.getName() + "\" " ;
		    	  double hind = line.getPrice();
		    	  
		    	  try{
		    		  hind =  this.getPriceAction(line.getToode(),order.getCompany(),ppp)+ 0.0005 ;
				  }catch(Exception e){
					  e.printStackTrace();
		    	  }
				//  System.out.println("HindAction " + line.getToode() + " " + hind);
			    	
				  if(hind < 0.0006){
                      try{
			    		  hind =  getPrice(line.getToode(),order.getCompany(),ppp)+ 0.0005 ;
					  }catch(Exception e){
			    	  }
			     }
		    	  
		    //	System.out.println("Hind " + line.getToode() + " " + hind + " " + line.getPrice());
		    	if(hind < 0.0006){
	               hind = line.getPrice();
		    	} 
                  if(line.getDiscount() > 0 ) {
                	  if (order.getStatus() != 1000) {
                		  order_xml = order_xml +"discount=\""+ line.getDiscount() +"\" ";
                	  }else {
                		  order_xml = order_xml +"discount=\"0\" ";
                	  }
                  }
                
                 // if(line.getDiscount() > 0 && order.getStatus() == 0) {
    			      order_xml = order_xml +"price=\""+ Converter.Round((float)hind, 2) +"\" ";  
                   //}
		    	 
                  order_xml = order_xml + "/>";
			 counter++;
	      }
	 
		      
	 	order_xml = order_xml + "</rows>";
	 	order_xml = order_xml + "</order>";
	 	order_xml = order_xml + "</orders>";
	    System.out.println("output " + order_xml); 
	 	
	    
	   // System.exit(0);
	    StringBuilder response = new StringBuilder();
		  
	 		
	 	try {
	 	    // Construct data
	 		
	 	    
	        URL url = new URL("https://login.directo.ee/xmlcore/cap_xml_direct/xmlcore.asp?");
	        Converter.trustAllHttpsURLConnection();
	        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setDoOutput(true);
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


	 	    String data = "&"  + URLEncoder.encode("put", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8");
	 	    data += "&" + URLEncoder.encode("what", "UTF-8") + "=" + URLEncoder.encode("order", "UTF-8");
	 	    data += "&" + URLEncoder.encode("xmldata", "UTF-8") + "=" + URLEncoder.encode(order_xml, "UTF-8");
	        
	        String params = "token=" + URLEncoder.encode(this.companytoken, StandardCharsets.UTF_8.toString()) + data ;
	        

	        try (OutputStream os = connection.getOutputStream()) {
	            byte[] input = params.getBytes(StandardCharsets.UTF_8);
	            os.write(input, 0, input.length);
	        }

	        int responseCode = connection.getResponseCode();
	        System.out.println("Response Code: " + responseCode);

	        InputStream responseStream =
	                responseCode >= 200 && responseCode < 300
	                        ? connection.getInputStream()
	                        : connection.getErrorStream();

	        try (BufferedReader br = new BufferedReader(
	                new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {

	            String line;
	            while ((line = br.readLine()) != null) {
	                response.append(line);
	            }

	            System.out.println("Response Body:" + response.toString());
	           //.out.println(response.toString());
	        }

	        connection.disconnect();
	 	
	 	   } catch (Exception e) {
	 	       e.printStackTrace();
	 	   }
	 	   
	 	   
	 	   if(response.toString().length() > 2){
	        

		 	   String doknr = order.getDokNr();
			      String fn = doknr;
			      if(doknr.trim().length()== 0){
			    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
			      }
		 	   String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".txt";
			      System.out.println(fileName);
			       
			      Converter.saveFileInUTF8(fileName, response.toString());
			      if(response.toString().contains("error code") || response.toString().contains("credential is expired")){
			    	  trouble = "ORDER Was not Imported :" + response.toString();
			      }else{
			    	  trouble = null;
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

		      if( sendcomm) {
		    	
		    	  
		    		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
		    		   System.out.println("Saadan emaili sisse");
		    	   sendCommentEmail( comm ,null,doknr,order.getCompany(),  " " + ccc.getName() , " ",false,ag,parent);
		    	 System.out.println("Saadan emaili tehstud");
		    }

	 		
	 	 
	 	    

	 	    
		      
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 	}
       

	      
	 	
		//System.exit(0);
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
