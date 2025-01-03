package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class BC365ImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public BC365ImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    company = parent.getProps().getProperty("company");
    secret = parent.getProps().getProperty("365.secret"); //"I~c8Q~fgLOHhFnFcIe2RHUSX6HWlPHXN8fon9b4Q"; 
	  tenantID = parent.getProps().getProperty("365.tenantID"); //"bcf61030-52c2-4048-9e18-042d8a0c856f";
	  clientID =  parent.getProps().getProperty("365.clientID"); //"075984fb-65fc-45e1-9a26-168935061542";
	  companyID =  parent.getProps().getProperty("365.companyID");
	  user =  parent.getProps().getProperty("rest.user");
    pass = parent.getProps().getProperty("rest.pass");
    rcompany = parent.getProps().getProperty("rest.companyx");
   

  }
  String logurl = null;
  
  String user =  null;
  String pass = null;
  String rcompany = null;

  String secret = null;
  String tenantID = null;
  String companyID = null;
  String clientID =  null;
  String token =  null;

 
  private String company = null;

	  
  
  public static String deCode(String res ){
	return Converter.deCode(res);
}
  

	  
  
  
  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
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

  private String  getMSToken() {
		 String res = null;
		 
		
		
	  try {
		return getAuthToken(tenantID,clientID,secret);
	   } catch (ClientProtocolException e2) {
		e2.printStackTrace();
	   } catch (IOException e2) {
		e2.printStackTrace();
	   }
		 
		 return res;
	 }
	   
	   
	 
	 public String getAuthToken(String tanantId,String clientId,String client_secret) throws ClientProtocolException, IOException {
		    CloseableHttpClient client = HttpClients.createDefault();
		    HttpPost loginPost = new HttpPost("https://login.microsoftonline.com/" + tanantId + "/oauth2/v2.0/token");
		   
		    String scopes = "https://api.businesscentral.dynamics.com/.default";
		    String encodedBody = "client_id=" + clientId + "&scope=" + scopes + "&client_secret=" + client_secret
		            + "&grant_type=client_credentials";
		    loginPost.setEntity(new StringEntity(encodedBody, ContentType.APPLICATION_FORM_URLENCODED));
		    loginPost.addHeader(new BasicHeader("cache-control", "no-cache"));
		    CloseableHttpResponse loginResponse = client.execute(loginPost);
		    InputStream inputStream = loginResponse.getEntity().getContent();
		    byte[] response = IOUtils.toByteArray(inputStream);
		    ObjectMapper objectMapper = new ObjectMapper();
		    JavaType type = objectMapper.constructType(
		            objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, String.class));
		    Map<String, String> parsed = new ObjectMapper().readValue(response, type);
		    return parsed.get("access_token");
		}
  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	  
	  if(eans.get(ean) != null){
		  return eans.get(ean) + "";
	  }
    return null;
}
  
  private TreeMap getPaymTerm() {
	  TreeMap s = new TreeMap();
	  
	  
	  org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//customers.json");
	  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
			  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

        	String number = (String)cob.get("number");
        	String paymTermId =  (String)cob.get("paymentTermsId");
        	s.put(number, paymTermId);
        	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
	  
	  
	  return s;
	  
  }

  public void makeOrder(Order order) throws SQLException{
       counter = 0;
       if (token == null) {
    	  // token = getMSToken();
       }
       if(paymTerms == null) {
    	   paymTerms = getPaymTerm();
       } if(products == null) {
    	   products = getProducts(null);
       }
		 makeOutput(order); 
    
  }
  

  TreeMap paymTerms = null;
  TreeMap products = null;
  
  
  private String postData(String type, String json){
	  if (token == null) {
		  token = this.getMSToken();
	  }
	  String res = null;
	  String urlBase = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/DEV/api/itera/kp/v2.0/"
	          + "companies(" + companyID + ")/";
		// https://api.businesscentral.dynamics.com/v2.0/%7B%7Bkodupaber%7D%7D/DEV/api/itera/kp/v2.0/companies(%7B%7BIT-TEST%7D%7D)/salesPersons

	    String url = urlBase + type;
	    
	    try (CloseableHttpClient client = HttpClients.createDefault()) {
	      HttpPost request = new HttpPost(url);
	      request.setHeader("Authorization", "Bearer " + token);
	      request.setHeader("Content-Type", "application/json");
	      
	      StringEntity jsonEntity = new StringEntity(json);
	        request.setEntity(jsonEntity);
	     
	      HttpResponse response = (HttpResponse) client.execute(request);
	      HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	      if (entity != null) {
	      String result = EntityUtils.toString(entity);
	      res = result;
	      System.out.println("kliendid tehtud");
	      }} catch (IOException e) {
	          e.printStackTrace();
	          }
	    
	    
	    return res;
	 
	  
  }
  
  private void makeOutput(Order order){
	  String paymTerm = (String)paymTerms.get(order.getCompany());
	  String comm = null;
		if(order.getInfo() != null && order.getInfo().trim().length() >0){
			comm = order.getInfo();
		}
	  JSONObject ord = new JSONObject();
	  ord.put("salesperson", order.getAgent());
	  
	  Calendar cal =  Calendar.getInstance();;
	 	if(order.isShipDateUsed()) {
	 		cal = order.getShipCal();
	 	}
	  ord.put("orderingDate", Converter.jsonDateOnly(cal.getTime()));
	  ord.put("customerNumber", order.getCompany());
	  ord.put("currencyCode", "EUR");
	  if (comm != null) {
		  ord.put("comment", comm);
	  }
	  
	  if (paymTerm != null) {
		  
		  ord.put("paymentTermsId", paymTerm);  
	  }
	  
	  Vector lines2 = order.getOrderLines();
		 JSONArray linesA = new JSONArray();
		 
	     for (int i = 0; i < lines2.size(); i++) {
	      JSONObject lin = new JSONObject();
	   	  OrderLine line = (OrderLine) lines2.elementAt(i);
	   	  String toode = line.getToode();
	   	  Product prod = ((Product) products.get(toode));
	   	  String tID = prod.getAgentGroup();
	   	  
	   	  if (tID != null) {	  
	   	  lin.put("itemId", tID );
	     }
	   	  
	   	  String itemVarID = prod.getAlegrupp();
	   	  if (itemVarID != null) {	  
	   	  lin.put("itemVariantId", itemVarID );
	     }
	   	  
	   	String locID = prod.getGroupSecond();
	   	  if (locID != null) {
	   	  lin.put("locationId", locID );
	     }
	   	  
	 	String name = prod.getName();
	 	  if (name != null) {
	 	  lin.put("description", name );
	   }
	 	  

	 	String unitMeasureC = prod.getUnit();
	 	  if (unitMeasureC != null) {
	 	  lin.put("unitOfMeasureCode", unitMeasureC );
	   }
	 	  
	 	Double price = prod.getPrice();
	 	  if (price != null) {
	 	  lin.put("unitPrice", price );
	   }
	 	  
	 	String unitMeasureId = prod.getDefectiveList();
	 	  if (unitMeasureId != null) {
	 	  lin.put("unitOfMeasureId", unitMeasureId );
	   }
	 	
	 	
	 	lin.put("quantity", line.getKogus());
	 	
	 	lin.put("invoiceQuantity", line.getKogus());
	 	
	 	lin.put("shipQuantity", line.getKogus());
	 	
	 	  
	 	  
	    lin.put("lineType", "Item");
	    
	    
	   // lin.put("taxCode", "STANDARD");
	    
	    Double disc = line.getDiscount();
	    if (disc > 0) {
	    	lin.put("specialDiscount", disc);
	    }
	    
	    lin.put("lineObjectNumber", toode);
	    linesA.add(lin);
	     }
	  ord.put("salesOrderLines", linesA);
	  
	  String res = postData("salesOrders", ord.toJSONString());
	  JSONParser parser = new JSONParser();
	  String ordId = null;
	  try {
		JSONObject res2 = (JSONObject) parser.parse(res);
		ordId = (String) res2.get("id");
		System.out.println(res2.toJSONString());
	} catch (ParseException e) {
		e.printStackTrace();
	}
   

	  
	  
	System.exit(0); 


	      

	 }
 
  public TreeMap getProducts(Agent agent) {
	  TreeMap tm = new TreeMap();
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//items.json");
  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
			  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

          	String number = (String)cob.get("number");
			Product prod = new Product(number);
			String id = (String)cob.get("id");
			prod.setAgentGroup(id);

          	String itemCatCode = (String)cob.get("itemCategoryCode");
        	if (itemCatCode != null && itemCatCode.length() > 0) {
        		prod.setGroup(itemCatCode);
        	}
        	
          	Boolean blocked = (Boolean) cob.get("blocked");
          	
          	
          	String gtin = (String)cob.get("gtin");
          	if (gtin != null && gtin.length() > 5) {
             	prod.setEAN(gtin);
          	}
          	
          	String name = (String)cob.get("displayName");
          	String name2 = (String)cob.get("displayName2");
          	prod.setName((name + " " + name2).trim());
          	
        
        	String unit = (String)cob.get("baseUnitOfMeasureCode");
        	if (unit != null) {
        		prod.setUnit(unit);
        	}
        	String unitID = (String)cob.get("baseUnitOfMeasureId");
        	if (unitID != null) {
        		prod.setDefectiveList(unitID);
        	}
        	String itemVariantId = "00000000-0000-0000-0000-000000000000";
        	prod.setAlegrupp(itemVariantId);
        	String locationId = "00000000-0000-0000-0000-000000000000";
        	prod.setGroupSecond(locationId);
        	
        	
      
        	
          	String price = cob.get("unitPrice")+ "";
          	if (price != null) {
          		prod.setPrice(Double.parseDouble(price));
          	}
          	
          	Long inventory = (Long)cob.get("inventory");
          	if (inventory != null) {
          		prod.setStock(inventory);
          	}
          	  	
            
           
            
            
            
            if(!blocked) {
            	  tm.put(prod.getID(), prod);	
            }

          	
          
          	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
System.out.println("Prodsize " + tm.size());
	  
	return tm;
}

 
  public String getDriverVersion(){
    return "BC365";
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
