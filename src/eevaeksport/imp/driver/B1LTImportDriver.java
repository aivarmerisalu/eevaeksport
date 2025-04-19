package eevaeksport.imp.driver;




import java.io.BufferedReader;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import eevaeksport.Agent;
import eevaeksport.Customer;
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

public class B1LTImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public B1LTImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    apikey = parent.getProps().getProperty("b1.apikey");
    secret = parent.getProps().getProperty("b1.secret"); 
    
    loadSignatures();
  }
  
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

  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

   
  
  public String identifyCustomer(String id,String regnr)  {
    String acc = null;
    return acc;
  }

  private CloseableHttpClient client;
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }
  

  
  String apikey = null;
  String secret = null;
	 
	 private HttpPost getPost(String url, String data) {
		 HttpPost request = new HttpPost(url);
		  request.setHeader("B1-Api-Key", apikey);
		  request.setHeader("Content-Type", "application/json" );
		  request.setEntity(new StringEntity(data, ContentType.APPLICATION_FORM_URLENCODED));
		 return request;
	 }
  

	public void closeClient(){
		if(client != null){
			
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    client = null;
		}		
	}
  
	
  public String postJson(String json, String filename , String url) throws Exception{
	
	  String result = null;
	  
	  
	  try (CloseableHttpClient client = HttpClients.createDefault()) {
	        HttpPost request = this.getPost(url,json);
	        HttpResponse response = (HttpResponse) client.execute(request);
	        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	        if (entity != null) {
	            result = EntityUtils.toString(entity);
	            if(filename != null) {
	        Converter.saveFile( filename , result);
	        System.out.println(filename + " tehtud");
	            }
	        }} catch (IOException e) {
	            e.printStackTrace();
	            }
			
    	return result;
	}
  
  public void makeOrder(Order order) throws SQLException{
	  counter = 0;
      if (signatures == null){
   	   signatures = new Vector();
      }
      
      if(customers == null) {
    	  customers = this.getCustomers(null);
      }
      
      String signature = order.getSignature();
      
      
	  if(false && signature != null && signatures.contains(signature)){
		  this.troubles.append( "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany()) ;
	  }else{
		  makeOutput(order);  
		  signatures.add(signature);
		  this.troubles = new StringBuffer();
	  }	    
	  
	   try {
			Converter.saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
  }
  
  
  
 private void makeOutput(Order order){
	  
	  
	    
	      try {
	 
	    	  long uni = Calendar.getInstance().getTimeInMillis();
	    	  
	    	  
	    	   String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".json";
	 	      String fileName2 = parent.getProps().getProperty("conv.dir2") + order.getDokNr().trim() + (uni + "").trim() +  "res.txt";
	    	  
	    	  String headerurl = "https://www.b1.lt/api/warehouse/sales/create";
	    	  

JSONObject ordsave = new JSONObject();
JSONObject ord = new JSONObject();
int u = order.getCompany().indexOf("_");
String custid = order.getCompany().substring(0,u);
String addrid = order.getCompany().substring(u+1);


ord.put("saleDate", Converter.getYYYY_MM_DDpString(Calendar.getInstance()));
ord.put("autoGenerateNumberAndSeries", false);


ord.put("series", "MOBI");
ord.put("number", order.getDokNr());

ord.put("warehouseId", "6");
ord.put("clientId", custid);
ord.put("operationTypeId", "2");
ord.put("employeeId", order.getAgent());

String comm = order.getInfo();
if(comm != null && comm.trim().length() > 0) {
	ord.put("notes", comm.trim());
}

ord.put("unloadAddressId", addrid);


System.out.println(ord);




	String data =  this.postJson(ord.toJSONString(), fileName2, headerurl);

System.out.println("ord " + data);
	//System.exit(0);
	
	
	JSONParser pp = new JSONParser();
	JSONObject dd = (JSONObject)pp.parse(data);
	
	JSONObject dat  = (JSONObject)dd.get("data");
	
	
	String id = dat.get("id") + "";

System.out.println("ord " + id);
	   // System.exit(0);
	    
ordsave.put("header", ord);

	        //RIDADE LISAMINE
String urlLine = "https://www.b1.lt/api/warehouse/sale-items/create-simple";
	   
	  
	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	    	
	        	  
	        JSONObject ll = new JSONObject();
	      //   ll.put("calculationMode", "1");
	       	 ll.put("saleId", id);
	       	 ll.put("itemId", toode);
	        // ll.put("purchaseItemId", toode);
	       	 ll.put("quantity", line.getKogus());
	       	 ll.put("priceWithoutVat", line.getPrice());
	       	 //ll.put("warehouseId", "6");
	       	 //ll.put("vatClassificationId", "68");

              ordsave.put("line" + counter, ll);
            //  System.out.println("line " + ll.toJSONString());
              
              String data2 =  this.postJson(ll.toJSONString(), null, urlLine);
              System.out.println("line enter " + data2);
              if(!data2.contains("Klaida")) {
               counter++;
              }else {
            	  this.troubles = this.troubles.append(data2);
              }
          	
	          }
	      
	      Converter.saveFile(fileName, ordsave.toString());
	      
	      System.out.println("counter " + counter);
		   setNumAddedLines(counter);
		   
	      
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


 private int counter = 0;  

 
  public String getDriverVersion(){
    return "B1LTImport";
  }

 
 public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

 private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
 private TreeMap customers = null;

 public TreeMap getCustomers(Agent agent){
	
	   
	   HashMap<String, ArrayList> addr = new HashMap<String, ArrayList>();
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//custaddr.json");
	  	
		int pages =  Integer.parseInt(cc.get("pages")+"");
			  System.out.println("pages " + pages);
			for (int u = 0; pages > u ;u++) {
				System.out.println( "ii " + u);
				 String fname ="custaddr.json";
				 if(u > 0) {
					 fname ="custaddr" + (u+1) + ".json";
				 }
			
				cc = 	Converter.readJSONFile("data//" + fname);
	

		
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		  
		try {
		  for (int i = 0; i < lenn; i++) {
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
	          	String clientId = cob.get("clientId") +"";
	          	String id = cob.get("id") +"";
	          	String fullAddress = cob.get("fullAddress") +"";
	          	Long employeeId = (Long)cob.get("employeeId") ;
	          	ArrayList ss = new ArrayList();
	          	if(addr.get(clientId) != null ) {
	          		ss = (ArrayList)addr.get(clientId);
	          	}
	          	Customer cu = new Customer(clientId+ "_" + id);
	          	 cu.setAddress(Converter.enCodeEst(fullAddress));
	          	 cu.setPayer(clientId);
	          	if(employeeId != null) {
	          		Agent ag = new Agent(employeeId + "");
	          		cu.addAgent(ag);
	          		
	          	}
	          	cu.addAgent(new Agent("61"));
	          	cu.addAgent(new Agent("39"));
	          	
	          	ss.add(cu);
	          	addr.put(clientId, ss);
	          	
	          	
		  }
	
		}catch(Exception e) {
			e.printStackTrace();
		}
	   
			}
	  
			System.out.println("addr suurus " + addr.size());
	  // System.exit(0);
	   
		  TreeMap tm = new TreeMap();
			 cc = 	Converter.readJSONFile("data//customers.json");
	  		  pages =  Integer.parseInt(cc.get("pages")+"");
				  System.out.println("pages " + pages);
				for (int u = 0; pages > u ;u++) {
					System.out.println( "ii " + u);
					 String fname ="customers.json";
					 if(u > 0) {
						 fname ="customers" + (u+1) + ".json";
					 }
				
					cc = 	Converter.readJSONFile("data//" + fname);
		
			System.out.println("fname " + fname);
			
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
			
			  int lenn = values.size();
			  
			try {
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	            Boolean juridical = (Boolean)cob.get("isJuridical");
	            if(juridical) {
	          	String number = cob.get("id") +"";
	          	Customer cust = new Customer(number);
	            
	          	String code = (String)cob.get("code") ;
	          	if(code != null && code.trim().length() > 1) {
	            	cust.setRegnr(code.trim());
	            }
	            
	          	String vatCode = (String) cob.get("vatCode");
	            if(vatCode != null && vatCode.trim().length() > 1) {
	            	cust.setVatnr(vatCode.trim());
	            }
	            
	            
	            String displayName = (String)cob.get("name");
	            if(displayName != null) {
	            cust.setName(Converter.enCodeEst(displayName));
	            
	            cust.setPayer(number);
	            String address1 = (String)cob.get("registrationAddress");
	            
	            String city = (String)cob.get("registrationCity");
	            if (city != null && city.trim().length() > 1) {
	            	city = ", " + city.trim();
	            } else {
	            	city = "";
	            }
	            
	            String country = (String)cob.get("registrationCountry");
	            if (country != null && country.trim().length() > 1) {
	            	country = ", " + country.trim();
	            } else {
	            	country = "";
	            }
	            
	            String postalCode = (String)cob.get("registrationZipCode");
	            if (postalCode != null && postalCode.trim().length() > 1) {
	            	postalCode = ", " + postalCode.trim();
	            
	            }else {
	            	postalCode = "";
	            }
	            
	        //    System.out.println("klinfo " + address1 + "'" +address2 + "'" + city  + "'" + state+ "'" + country+ "'" + postalCode);
	            cust.setAddress(Converter.enCodeEst((address1 + city + country + postalCode).trim()));
	            
	            Agent aa = new Agent("60");
	            cust.addAgent(aa);
	            
	            String phone = (String)cob.get("phoneNumber");
	            if(phone != null) {
	             cust.setPersonalData("Tel:" + phone);
	            }
	            
	            String email = (String)cob.get("email");
	            if(email != null) {
	             cust.setInfoEmail(email);
	            }
	            
	           if(addr.get(number) != null) {
	        	   ArrayList arr = (ArrayList)addr.get(number);
	        	   for(int y = 0; y < arr.size();y++) {
	        		   Customer ccc = (Customer)arr.get(y);
	        		  
	        		   ccc.addAgent(aa);
	        		   ccc.setName(displayName);
	        		   if(code != null && code.trim().length() > 1) {
	   	            	ccc.setRegnr(code.trim());
	   	            }
	   	            
	   	            if(vatCode != null && vatCode.trim().length() > 1) {
	   	            	ccc.setVatnr(vatCode.trim());
	   	            }
	   	            if(phone != null) {
	   	            	if(phone.startsWith("370")) {
	   	            		phone = "+" + phone;
	   	            	}
	   	             ccc.setPersonalData("Tel:" + phone);
	   	            }
	   	            
	   	            if(email != null) {
	   	             ccc.setInfoEmail(email);
	   	            }
	        		   tm.put(ccc.getID(), ccc);
	        		   
	        	   }
	        	   
	           }else {
	            
	            
	          /*  if (volad.get(cust.getID() ) != null) {
	            	cust.setDebt(volad.get(cust.getID()));
	            }
	            if (volad.get(cust.getPayer() ) != null) {
	            	cust.setDebt(volad.get(cust.getPayer()));
	            }
	            */
	            
	            //String blocked = (String)cob.get("blocked");
	            
	          	
	            }
	           tm.put(cust.getID(), cust);
	           
	          	}
			  } 
			  }
						
			} catch (Exception e) {
				e.printStackTrace();
			}
		     
					}
			
	System.out.println("Custsize " + tm.size());
	
	//System.exit(0);
	
	
		return tm;
	    
		
     
   }

  private ImportPanel parent;
 
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
	  if(troubles != null) {
		  return troubles.toString();
	  }
  	return null;
  }
  
}
