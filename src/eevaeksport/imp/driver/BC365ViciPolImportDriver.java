package eevaeksport.imp.driver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
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

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


public class BC365ViciPolImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public BC365ViciPolImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    secret = parent.getProps().getProperty("365.secret"); 
	  tenantID = parent.getProps().getProperty("365.tenantID"); 
	  clientID =  parent.getProps().getProperty("365.clientID"); 
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

	  
  
  public static String deCode(String res ){
	return Converter.deCode(res);
}

  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  

  
  public String identifyCustomer(String id,String regnr)  {
    return id;
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
		    HttpPost loginPost = new HttpPost("https://login.microsoftonline.com/" + tanantId + "/oauth2/token");
		    String encodedBody = "client_id=" + clientId  + "&client_secret=" + client_secret
		            + "&grant_type=client_credentials&resource=https://vic-2-uat.sandbox.operations.eu.dynamics.com";
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
	 return prodID;
}
  
  
    public void makeOrder(Order order) throws SQLException{
    	
    	
    	 
		  TreeMap tm = new TreeMap();
		  this.delCodes = new HashMap<String,String>();
			org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//getCustomers.json");
	  	
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
			
			  int lenn = values.size();
			  
			try {
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	          	String number = (String)cob.get("CustomerAccount");
	            String delid = (String)cob.get("DeliveryAddressLocationId");
	            delCodes.put(number, delid);
	            
			  }
			  } catch( Exception e){
				  e.printStackTrace();
			  }
    	
    	
    	
        makeOutput(order); 
  }
  

  TreeMap paymTerms = null;
  TreeMap products = null;
  
  
  private String postData( String json){
	  if (token == null) {
		  token = this.getMSToken();
	  }
	    String res = null;
	 // System.out.println("imp token " + token);
	    String urlBase = "https://vic-2-uat.sandbox.operations.eu.dynamics.com//api/services/AMGVICMerisoftServiceGroup/AMGVICMerisoftImportSalesOrderServicePL/ImportSalesOrdersMerisoft";

	    String url = urlBase ;
	    
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
	      }} catch (IOException e) {
	          e.printStackTrace();
	          }
	    
	    
	    return res;
	 
	  
  }
  
  private HashMap<String,String> delCodes = null;
  
  private void makeOutput(Order order){
	  
	  
	  
	  
	  String comm = null;
		if(order.getInfo() != null && order.getInfo().trim().length() >0){
			comm = order.getInfo();
		}
	
		  JSONObject ordMain = new JSONObject();
		  JSONArray ordArray = new JSONArray();
	      JSONObject ord = new JSONObject();
	  
	  ord.put("OrderNumber",order.getAgent() + "-" + order.getDokNr());
	  ord.put("OrderCustomerDeliveryAddress", delCodes.get( order.getCompany()));
	  ord.put("OrderDate", Converter.jsonDate(Calendar.getInstance().getTime()));
	  ord.put("OrderSalesperson", order.getAgent());

	  if (comm != null) {
		  ord.put("OrderComment", Converter.cleanLitLatEst(comm));
	  }
	  
	 	if(order.isShipDateUsed()) {
	 	  ord.put("OrderDeliveryDate", Converter.jsonDate(order.getShipCal().getTime()));
	 	}
	 	
	 
	  Vector lines2 = order.getOrderLines();
		 JSONArray linesA = new JSONArray();
		 
	     for (int i = 0; i < lines2.size(); i++) {
	      JSONObject lin = new JSONObject();
	   	  OrderLine line = (OrderLine) lines2.elementAt(i);
	   	  
	   	  lin.put("ItemId", line.getToode() );
	   	  lin.put("Qty",  line.getKogus());
	 	  lin.put("Unit", line.getUnit());
	 	  
	     linesA.add(lin);
	    
	     this.addNumLine();
	     }
	     
	  ord.put("LINES", linesA);
	  
	  
	  ordArray.add(ord);
	  ordMain.put("ORDERS", ordArray);
	  
	  Converter.saveFile("orders/response/" + order.getDokNr() + "request.json" , ordMain.toJSONString() );
	  String res = postData( ordMain.toJSONString());
	  System.out.println("res " + res);
	  JSONParser parser = new JSONParser();
	  String ordId = null;
	  try {
		JSONObject res2 = (JSONObject) parser.parse(res);
		ordId = (String) res2.get("id");
		System.out.println(res2.toJSONString());
		  Converter.saveFile("orders/response/" + order.getDokNr() + "reply.json" , res2.toJSONString() );
	} catch (ParseException e) {
		e.printStackTrace();
	}
   
//System.exit(0);
	 }
  
  public String getDriverVersion(){
    return "BC365ViciPol";
  }
  
  public String getTrouble() {
		return trouble;
	}

  private String trouble = null;
  
  public boolean isAmount(){
          return isAmount;
  }

  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }


  private ImportPanel parent;
  private boolean isAmount = false;

  
}
