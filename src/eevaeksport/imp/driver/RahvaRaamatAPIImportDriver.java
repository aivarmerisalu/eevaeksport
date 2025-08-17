package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

public class RahvaRaamatAPIImportDriver extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RahvaRaamatAPIImportDriver(ImportPanel parent) {
    this.parent = parent;
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }

  private String getKliendiNimi(String kood) {
	  String res = null;
	  
	   if(custs.get(kood) != null) {
		   res = ((Customer)custs.get(kood)).getName(); 
	   }
	   
	  return res;
  }
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
 
  TreeMap custs = null;
  TreeMap names = null;
  
  
  public void makeOrder(Order order){
    
    if(custs == null) {
    	custs = getCustomers();
    }
    
    if(names == null) {
    	names = getProductNames();
    }
    String komm = "";
    if (order.getInfo() != null) {
         komm =  "Kommentaar : " + order.getInfo();
    }
    
    String agnimi = ((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent())).getName();
    String name = getKliendiNimi(order.getCompany());
    	String text = " " + name +  "|Tellimuse number: " + order.getDokNr() +  "\nKlient : " + name + " " + order.getCompany() +
    			 "\nAgent: " + agnimi + "\n\n" 
    			 + Converter.getOrderText(order,names) + "\n" + komm ;    			  ;
    	
    	Converter.sendReportEmail(text, null, parent.getEksportFrame().getEksportPanel());
    	this.setNumAddedLines(order.getOrderLines().size());
 }
  
  
  public TreeMap getProductNames() {
	  TreeMap tm = new TreeMap();
	  
	  Integer total = getPagination("getItems");
      
	 	for (int a = 0 ; a < total; a++) {
	 		String aa = "";
	 	    if(a > 0) {
	 	    	aa = a + "";
	 	    }
	 		
  	    org.json.simple.JSONObject cc = Converter.readJSONFile("data//getItems" + aa +".json");
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		org.json.simple.JSONObject cob = null;
	
		try {
		  int lenn = values.size();
		  
		  for (int i = 0; i < lenn; i++) {
			  cob = (org.json.simple.JSONObject) values.get(i);
	       	  tm.put((String)cob.get("product_id"), (String)cob.get("description"));	
          	}
		  
		} catch (Exception e) {
			System.out.println("cob "+  cob.toString());
			e.printStackTrace();
		}
	 	}
	 	
     System.out.println("Prodsize " + tm.size());
	  
	return tm;
}

  
  private Integer getPagination(String method) {
	     
      org.json.simple.JSONObject cc0 = Converter.readJSONFile("data//" + method + ".json");
	    org.json.simple.JSONObject cc1 = (org.json.simple.JSONObject)cc0.get("pagination");
	 if(cc1 == null) {
		 return 1;
	 }
	     return Integer.parseInt(cc1.get("totalPages") + "");
	   }
  
  public TreeMap getCustomers(){
	   
		  TreeMap tm = new TreeMap();
		  Integer total = getPagination("getCustomers");
	       
		 	for (int a = 0 ; a < total; a++) {
		 		String aa = "";
		 	    if(a > 0) {
		 	    	aa = a + "";
		 	    }
		 		
	    	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getCustomers" + aa +".json");
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
	
			  int lenn = values.size();
			  
			try {
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	          	String number = (String)cob.get("code");
	            Customer cust = new Customer(number);
	                  
	            cust.setName((String)cob.get("name"));

	            String payer = (String)cob.get("payer");
	            cust.setPayer(payer);
	            
	            String address1 = (String)cob.get("address");
	            cust.restStreet = address1;
	            
	            
	            
	            String city = (String)cob.get("city");
	            if (city != null && city.trim().length() > 1) {
	            	cust.restCity = city;
	            	city = ", " + city.trim();
	            }else {
	            	city = "";
	            }
	            
	            String postalCode = (String)cob.get("post_code");
	            if (postalCode != null && postalCode.trim().length() > 3) {
		            cust.restZipCode = postalCode;
	            	postalCode = ", " + postalCode.trim();
	            }else {
	            	postalCode = "";
	            }
	            
	            cust.setAddress((address1  + city + postalCode).trim());
	           
	            String cper = (String)cob.get("contact");
	            if(cper.length() > 0) {
	              cust.adPersonalData("|" + cper);
	              cust.restPerson = cper;
	            }
	           
	            String salesCode = (String)cob.get("salesperson_code");
	            cust.addAgent(new Agent (salesCode));
          
	            String payment_term = cob.get("payment_term")+"";
	          try {
	            	if(payment_term.length() < 4) {
	            	   cust.setPaymTerm(payment_term.substring(0,payment_term.length()-1));
	            	}
	 	        }catch(Exception e) {
	            }
                   tm.put(cust.getID(), cust);
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
		 	}
       System.out.println("Custsize " + tm.size());
		return tm;
  }

    public String getDriverVersion(){
    return "RahvaRaamatAPI";
  }

  
  
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}