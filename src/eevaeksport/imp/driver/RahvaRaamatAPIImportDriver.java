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

  TreeMap eans = null;

  HashMap poeStock = null;
  
  
  public void makeOrder(Order order){
    
    if(custs == null) {
    	custs = getCustomers();
    }
    
    if(poeStock == null) {
    	poeStock = poeStock();
    }
    
    if(names == null) {
    	names = getProductNames();
    }
    String komm = "";
    if (order.getInfo() != null) {
         komm =  "Kommentaar : " + order.getInfo();
    }
    
    
    HashMap st = (HashMap)poeStock.get(order.getCompany());
	
    
    String agnimi = ((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent())).getName();
    String name = getKliendiNimi(order.getCompany());
    	String text = " " + name +  "|Tellimuse number: " + order.getDokNr() +  "\nKlient : " + name + " " + order.getCompany() +
    			 "\nAgent: " + agnimi + "\n\n" 
    			 + getOrderText(order,names,eans,st) + "\n" + komm ;   
    	
    	Converter.sendReportEmail(text, null, parent.getEksportFrame().getEksportPanel());
    	this.setNumAddedLines(order.getOrderLines().size());
 }
  
  
  private  String getOrderText(Order order, TreeMap<String, String> names,TreeMap<String, String> eans, HashMap st) {
	    StringBuilder res = new StringBuilder();
     
	    
	    // Start HTML table
	    res.append("<table border='1' cellspacing='0' cellpadding='5'>");
	    res.append("<tr>")
	       .append("<th>Kood</th>")
	       .append("<th>Ean</th>")
	       .append("<th>Nimetus</th>")
	       .append("<th>Hind</th>")
	       .append("<th>Laoseis</th>")
	       .append("<th>Tagastusse</th>")
	       .append("<th>Kadunud</th>") 
	       .append("<th>Kahjustunud</th>")
	       .append("</tr>");

	    Iterator<OrderLine> iter = order.getOrderLines().iterator();
	    while (iter.hasNext()) {
	        OrderLine line = iter.next();
	        String toode = line.getToode();
	        if (toode != null) {
	            toode = toode.trim();
	        } else {
	            toode = "";
	        }
          String lseis = "";
	        if(st != null) {
				Toode stT = (Toode)st.get(toode);
				if(stT != null) {
					lseis = (int)stT.getKogus() + "";
				}
			}
	        
	        
	        String name = line.getTooteNimi();
	        if (name == null || name.trim().isEmpty()) {
	            name = toode;
	        }

	        if (names != null && names.get(toode) != null) {
	            name = names.get(toode);
	        }
	        
	        String ean = "";
	        if (eans != null && eans.get(toode) != null) {
	            ean = eans.get(toode);
	        }

	        if (line.getKogus() != 0 && !toode.isEmpty()) {
	            res.append("<tr>")
	               .append("<td>").append(toode).append("</td>")
	               .append("<td>").append(ean).append("</td>")
	               .append("<td>").append(name).append("</td>")
	               .append("<td>").append(line.getPrice()).append("</td>")
	               .append("<td>").append(lseis).append("</td>")
	               .append("<td>").append((int)line.getKogus()).append("</td>")
	               .append("<td>").append("").append("</td>")
	               .append("<td>").append("").append("</td>")
	               .append("</tr>");
	        }
	    }

	    res.append("</table>");
	    return res.toString();
	}
  
  public TreeMap getProductNames() {
	  TreeMap tm = new TreeMap();
	  eans = new TreeMap();
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
	      	
	       	  String gtin = (String)cob.get("EAN");
	          	if (gtin != null && gtin.length() > 5) {
		       	   eans.put((String)cob.get("product_id"), gtin);
	          	}
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
		  
		  locations = new HashMap<String, String>();
		  
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
	        	String locCode = (String)cob.get("location_code");
	          	locations.put(locCode, number);
	                  
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


  private HashMap<String, String> locations = null; 
  private HashMap poeStock() {

 	 HashMap res = new HashMap();
 	 Integer total = getPagination("getCustomerStockAndSales");
      
	 	for (int a = 0 ; a < total; a++) {
	 		String aa = "";
	 	    if(a > 0) {
	 	    	aa = a + "";
	 	    }
	 	  try {	
  	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getCustomerStockAndSales" + aa +".json");
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		
		  for (int i = 0; i < lenn; i++) {
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
      		String customerNumber = (String) cob.get("location");
      		String cuCode = locations.get(customerNumber);
      		if(cuCode != null) {
      			try {

      	        	String prodId = ((String) cob.get("product_id"));
      				if(prodId == null) {
      					prodId = ((String) cob.get("item_id"));
      				}
      				prodId = prodId.trim();
	        	
	         		Toode tt = new Toode(prodId);
			  if(cob.get("quantity") != null) {
			 	 tt.addKogus(Double.parseDouble(cob.get("quantity") + "")); 
			  }
  
 	         		HashMap sisu = new HashMap();
	         		if(res.get(cuCode) != null) {
	         			sisu = (HashMap)res.get(cuCode);
	         		}
	         		
	         		sisu.put(prodId, tt);
	         		res.put(cuCode, sisu);
      			}catch(Exception e) {
      			System.out.println(cob.toJSONString());
      				e.printStackTrace();
      			}
	         		
      		}
	    		  }
     

		   } catch (Exception e) {
            e.printStackTrace();
         }
  	}

	 	
	 	System.out.println("getCustomerStockAndSales size " + res.size());

 	 
 	 return res;
  }

  
    public String getDriverVersion(){
    return "RahvaRaamatAPI";
  }

  
  
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}