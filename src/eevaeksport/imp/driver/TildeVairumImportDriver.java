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

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


public class TildeVairumImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public TildeVairumImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    logurl = parent.getProps().getProperty("API.url"); 
    secret = parent.getProps().getProperty("API.secret"); 
	user =  parent.getProps().getProperty("rest.user");
    pass = parent.getProps().getProperty("rest.pass");
    rcompany = parent.getProps().getProperty("rest.companyx");
    System.out.println("Tooted " + getData("/products/list",""));
   
  }
  
  String logurl = null;
  String user =  null;
  String pass = null;
  String rcompany = null;
  String secret = null;

	  
  
  public static String deCode(String res ){
	return Converter.deCode(res);
}

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public String identifyCustomer(String id,String regnr)  {
    return id;
  }

	   

	 
  public String identifyProduct(String ean, String prodID) throws SQLException{ 
	 return prodID;
}
  
  
    public void makeOrder(Order order) throws SQLException{
    	
    	
        makeOutput(order); 
  }
  

  TreeMap paymTerms = null;
  TreeMap products = null;
  
  
  private String postData( String json){
	  
	    String res = null;
	    String url = logurl;
	    
	    try (CloseableHttpClient client = HttpClients.createDefault()) {
	      HttpPost request = new HttpPost(url);
	    //  auth_key: 12345678-1234-1234-1234-12345678901
	      request.setHeader("auth_key",  secret);
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
  
  private String getData(String service , String json) {
      String url = logurl;

      try (CloseableHttpClient client = HttpClients.createDefault()) {
          HttpGet request = new HttpGet(url + service);

          request.setHeader("auth_key", secret);
          request.setHeader("Content-Type", "application/json");
          request.setHeader("Accept", "application/json");

          StringEntity jsonEntity = new StringEntity(
              json,
              org.apache.http.entity.ContentType.APPLICATION_JSON
          );
         // request.setEntity(jsonEntity);

          try (CloseableHttpResponse response = client.execute(request)) {
              HttpEntity entity = response.getEntity();

              if (entity != null) {
                  return EntityUtils.toString(entity, "UTF-8");
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }

      return null;
  }
  

  
  private void makeOutput(Order order){
	  
	  
	  
	  
	  String comm = null;
		if(order.getInfo() != null && order.getInfo().trim().length() >0){
			comm = order.getInfo();
		}
	
		  JSONObject ordMain = new JSONObject();
		  JSONArray ordArray = new JSONArray();
	      JSONObject ord = new JSONObject();
	  String num = order.getAgent() + order.getDokNr();
	  ord.put("OrderNumber",num.substring(num.length()-10));
	  ord.put("OrderCustomerDeliveryAddress",  order.getCompany());
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
	  
	  try {
		JSONObject res2 = (JSONObject) parser.parse(res);
		if(res2.get("ExceptionType") != null) {
			this.trouble = res;
		}
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
