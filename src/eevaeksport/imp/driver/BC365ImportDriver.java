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
	  String urlBase = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/DEV/api/v2.0/"
	          + "companies(" + companyID + ")/";
		

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
	  
	  JSONObject ord = new JSONObject();
	  ord.put("salesperson", order.getAgent());
	  ord.put("orderDate", Converter.jsonDateOnly(new Date()));
	  ord.put("customerNumber", order.getCompany());
	  ord.put("currencyCode", "EUR");
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
	 	Calendar cal = order.getShipCal();
	 	lin.put("shipmentDate", Converter.jsonDateOnly(cal.getTime()));
	 	
	 	
	 	lin.put("quantity", line.getKogus());
	 	
	 	lin.put("invoiceQuantity", line.getKogus());
	 	
	 	lin.put("shipQuantity", line.getKogus());
	 	
	 	  
	 	  
	    lin.put("lineType", "Item");
	    
	    
	   // lin.put("taxCode", "STANDARD");
	    
	    Double disc = line.getDiscount();
	    if (disc > 0) {
	    	lin.put("discountPercent", disc);
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
	 	try {
	 	
	 		
	 		
	 		
	 		
	 		
	 		
	 		
	 		
	 		
	 		Customer ccc = (Customer)kliendid.get(order.getCompany());
	 
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm = "comment=\""+ (order.getInfo())+ "\" " ;
	 		}
	 		//System.out.println("Comment " + comm);
			
	 	    String  order_xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
	          "<transport>" +
	          "<order " + 
	         "webid=\"" + "1" + "\" " + //order.getDokNr()+
	         "webdate=\"" + (Calendar.getInstance())+ "\" " +
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
	 	   String fileName = parent.getProps().getProperty("conv.dir") +  "t-" + fn  +  ".txt";
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
	    
	      
	      String fileName = parent.getProps().getProperty("conv.dir") +  "t-" + fn  +  ".xml";
	      System.out.println(fileName);
	       
	      Converter.saveFileInUTF8(fileName, order_xml);
	    
	      
	      if(order.getStatus() < 5  ) {
	    	
	    	  
	    		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
	    		 
	    	 //  sendCommentEmail( comm ,null,doknr,order.getCompany(),  " " + ccc.getName() , " ",false,ag,parent);
	    	 
	    }

		} catch (Exception e) {
			e.printStackTrace();
		}
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
