package eevaeksport.imp.driver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import eevaeksport.Debt;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;


public class TildeVairumImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public TildeVairumImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    logurl = parent.getProps().getProperty("API.urlimport"); 
    secret = parent.getProps().getProperty("API.secret"); 
	user =  parent.getProps().getProperty("rest.user");
    pass = parent.getProps().getProperty("rest.pass");
    url = "jdbc:odbc:" + parent.getProps().getProperty("dsn.url");
   
  }
  
  String logurl = null;
  String user =  null;
  String pass = null;
  String secret = null;
  String url = null;
	  
  
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
  
    	adressGlns = getAddressGlns();
        makeOutput(order); 
  
    }
  

  HashMap adressGlns = null;
  
  
  private HashMap getAddressGlns(){
	  HashMap list = new HashMap();
      
      String sql = "select a.AddressID as saaja ,a.GLNCode as gln from partneraddress a where a.GLNCode != '' ";
try {
      Connection con = DriverManager.getConnection(url);
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
      while(rs.next()){
        String adID = rs.getString("saaja");
        String gln = rs.getString("gln").trim();
        list.put(adID, gln);
      }
      rs.close();
      stmt.close();
      con.close();
       }catch(Exception e) {e.printStackTrace();}
      return list;
    }

  
  
  private String postData( String json){
	  
	    String res = null;
	    String url = logurl;
	    
	    try (CloseableHttpClient client = HttpClients.createDefault()) {
	      HttpPost request = new HttpPost(url);
	    //  auth_key: 12345678-1234-1234-1234-12345678901
	      request.setHeader("auth_key",  secret);
	      request.setHeader("Content-Type", "application/json");
	      request.setHeader("Accept", "application/json");
	      
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
  
   
  private void makeOutput(Order order){
	  
	  String comm = null;
		if(order.getInfo() != null && order.getInfo().trim().length() >0){
			comm = order.getInfo();
		}
		
	  JSONObject ord = new JSONObject();
	  ord.put("type_id",21);
	  
	  String num = order.getAgent() + "-" + order.getDokNr();
	 
	  ord.put("number",num);
	  String cukood =  Converter.deCode(order.getCompany());
      String adid = null;
      if(cukood.indexOf("_")!= -1){
      	adid =  cukood.substring(cukood.lastIndexOf("_")+1);
      	cukood = cukood.substring(0,cukood.lastIndexOf("_"));
	    }
	   ord.put("customer_id",  cukood);
	  if(adid != null) {
	    ord.put("billing_address_id",  adid);
	    String gln = adid;
	    if(this.adressGlns.get(adid) != null) {
	    	gln = (String)this.adressGlns.get(adid); 
	    }
	    
	    if(gln != null && gln.length() > 0 ) {
	      ord.put("billing_address_gln_code",  gln);
	    }
	    
	  }
	  
	   ord.put("date_created", Converter.jsonDate(Calendar.getInstance().getTime()));
	   ord.put("agent_id", order.getAgent());

	  if (comm != null) {
		  ord.put("note", Converter.cleanLitLatEst(comm));
	  }
	  
	 	if(order.isShipDateUsed()) {
	 	  ord.put("date_delivery", Converter.jsonDate(order.getShipCal().getTime()));
	 	}
	 	
	  Vector lines2 = order.getOrderLines();
		 JSONArray linesA = new JSONArray();
		 
	     for (int i = 0; i < lines2.size(); i++) {
	      JSONObject lin = new JSONObject();
	   	  OrderLine line = (OrderLine) lines2.elementAt(i);
	   	  
	   	  lin.put("product_id", line.getToode());
	   	  lin.put("quantity",  line.getKogus());
	 	  lin.put("unit_name", line.getUnit());
	 	  
	 	  if(line.getDiscount() > 0) {
//	 		 lin.put("discount_percent", line.getUnit());
	 		 lin.put("price", line.getPrice());
	 	  }
	 	  
	     linesA.add(lin);
	    
	     this.addNumLine();
	     }
	     
	  ord.put("line_items", linesA);
	  
	  Converter.saveFile("orders/response/" + order.getDokNr() + "request.json" , ord.toJSONString() );
	  String res = postData( ord.toJSONString());
	  System.out.println("res " + res);
	  Converter.saveFile("orders/response/" + order.getDokNr() + "reply.json" , res );
	
   
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
