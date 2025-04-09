package eevaeksport.driver;

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
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
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
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BC365DBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,WebReporter {

  public BC365DBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
      company = parent.getProps().getProperty("company");
      secret = parent.getProps().getProperty("365.secret"); //"I~c8Q~fgLOHhFnFcIe2RHUSX6HWlPHXN8fon9b4Q"; 
	  tenantID = parent.getProps().getProperty("365.tenantID"); //"bcf61030-52c2-4048-9e18-042d8a0c856f";
	  clientID =  parent.getProps().getProperty("365.clientID"); //"075984fb-65fc-45e1-9a26-168935061542";
	  companyID =  parent.getProps().getProperty("365.companyID");
	  user =  getParent().getProps().getProperty("rest.user");
      pass = getParent().getProps().getProperty("rest.pass");
      rcompany = getParent().getProps().getProperty("rest.companyx");
     
      
      
      
     
    
  
    //  token = getMSToken();
    
      System.out.println("Token " + token);

      //this.downloadData("salesPrices");
  //  this.downloadData(null);
    // this.getDebt(null);
    // this.getCustomerProductsList(null);
     //this.setBulkSales(null);
   

     }
  
	  String user =  null;
	  String pass = null;
	  String rcompany = null;
	  String secret = null;
      String tenantID = null;
      String companyID = null;
	  String clientID =  null;
	  String token =  null;
  
		 
		 
		 
		 
 
	  private void testConn() {
		  String url = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/environments";

		  url = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/LIVE/api/v2.0/"
		  		+ "companies(3b92aaf0-963a-ef11-8409-000d3ab2813a)/items";
		  
		  try (CloseableHttpClient client = HttpClients.createDefault()) {
			  HttpGet request = new HttpGet(url);
			  request.setHeader("Authorization", "Bearer " + token);

			  HttpResponse response = client.execute(request);
			  HttpEntity entity = response.getEntity();
			  if (entity != null) {
			  String result = EntityUtils.toString(entity);
			  Converter.saveFile("test.json", result);
			 // System.out.println("Query result " + result);
			  
			  /* {
      "@odata.etag": "W/\"JzIwOzEwNDU0MjEwNDEyNDg0NjMxODY3MTswMDsn\"",
      "id": "e81e48a3-ae33-ef11-8409-0022489f6722",
      "number": "47SISI000603",
      "displayName": "SISI Relax 40 den nero M SP",
      "displayName2": "SUKKP\u00dcKSID Relax",
      "type": "Inventory",
      "itemCategoryId": "040414c1-b354-ef11-bfe2-000d3adbde7b",
      "itemCategoryCode": "SUKKPX SISI",
      "blocked": false,
      "gtin": "",
      "inventory": 0,
      "unitPrice": 0,
      "priceIncludesTax": false,
      "unitCost": 0,
      "taxGroupId": "00000000-0000-0000-0000-000000000000",
      "taxGroupCode": "",
      "baseUnitOfMeasureId": "e087a81b-7829-ef11-9f88-000d3ab16a11",
      "baseUnitOfMeasureCode": "TK",
      "generalProductPostingGroupId": "dd6282ee-041c-ef11-9f88-000d3a69189e",
      "generalProductPostingGroupCode": "KAUP",
      "inventoryPostingGroupId": "08dea46d-751e-ef11-9f88-000d3a69189e",
      "inventoryPostingGroupCode": "KAUBAD",
      "lastModifiedDateTime": "2024-09-17T15:49:54.307Z"
    }*/
			  }
			  } catch (IOException e) {
			  e.printStackTrace();
			  }
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
  
  private String company = null;
  
  
  public String getSaveDebtInfo() {
   	return null;
   }
   
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
  
	return lines;

   }

 //  public Vector getFixedAssortmentData() {
      // return fixedLines ;
 //}
   
   
   
   public Vector getFixedAssortmentData() {
	return null;
	}

   
   
   
  
   
 

   public String SortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }

 

   public Vector getDebt(String agentCode){
			  Vector tm = new Vector();
				org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesInvoices.json");
		  	
				org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
				
				  int lenn = values.size();
				try {
				  for (int i = 0; i < lenn; i++) {
						org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
		        		String number = (String) cob.get("number");
			        	String date =(String) cob.get("invoiceDate");
			        	String duedate = (String) cob.get("dueDate");
			        	Double volg = Double.parseDouble(cob.get("remainingAmount") + "");
			        	String payer = (String) cob.get("billToCustomerNumber");
			        	String custid = (String) cob.get("customerNumber");
					  


       		     Debt dd = new Debt();
				  dd.setCustID(custid);
				  dd.setPayerID(payer);
				  dd.setDoknr(number);
				  dd.setDate(getDate(date));
				  dd.setDuedate(getDate(duedate));
				  
				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
				  dd.setDue(ff + "");
				  dd.setSum(volg);
				//	System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	

				 tm.add(dd);

		        		
		          	}
				  
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		System.out.println("Debtsize " + tm.size());

		  

	     return tm;
	   }

   public void connectionDown(){
   }
   public void connectionUp(){
   }

   public String getDealerCustomerCSV(String agent){
       return null;
        }

       public String  getCustProdDiscounts(Agent agent){
                     return null;
            }
  private TreeMap   prGroups= new TreeMap();
  
 
  
  
  
  
  
  
  
   public TreeMap getProdGroups() throws SQLException {
	   TreeMap tm = new TreeMap();
	   
	   
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//items.json");
	  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
			  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
          
           String code = (String)cob.get("manufacturerCode");
           if(tm.get(code) == null) {
           String name = (String)cob.get("manufacturerName");
           if (name != null && name.trim().length() > 0) {
        	    tm.put(code,name);
        	   }
              }
         	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
System.out.println("ProdGroup size :  " + tm.size());
	  
	return tm;
   

}
	   
	   
	   
	   /*   {
      "@odata.etag": "W/\"JzIwOzE0NTcxMzI1NDExNjY1NDUyMDYwMTswMDsn\"",
      "id": "2a7525a9-b354-ef11-bfe2-000d3adbde7b",
      "code": "AINEDISP",
      "displayName": "puhastusaine dispenser",
      "lastModifiedDateTime": "2024-08-07T11:53:31.527Z"
    },
    */
	   
   

   public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
        }



   private String enCode(String res ){
	 
	   return Converter.enCodeEst(res);
	  }
	  
   
   public static String deCode(String res ){
	   
 	return Converter.deCode(res);
 }
   
   
   public TreeMap getAgents(){
		  TreeMap tm = new TreeMap();
			org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesPersons.json");
	  	
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
			
			  int lenn = values.size();
			try {
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	            String salesCode = (String)cob.get("code");
	            String salesName = (String)cob.get("name");
	            if (salesCode != null && salesCode.trim().length() > 0) {
	            	Agent agent = new Agent(salesCode);
	            	agent.setName(salesName);
	            	  tm.put(agent.getName(), agent);
	            }

	        
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	System.out.println("Managersize " + tm.size());

	  

     return tm;
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
        	  
        	  
       public String getDistribution() {
         return null;
       }
private void saveFile (String dataType) {
	  String folderPath = "Data"; // Update this path as needed

      // Create a File object
      File folder = new File(folderPath);

      // Check if the folder already exists
      if (!folder.exists()) {
          // Try to create the folder
          if (folder.mkdirs()) {
              System.out.println("Folder created successfully at: " + folderPath);
          } else {
              System.out.println("Failed to create the folder at: " + folderPath);
          }
      } 
      String urlBase = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/LIVE/api/itera/kp/v2.0/"
	          + "companies(" + companyID + ")/";
      

	      String url = urlBase + dataType;
	      
	      System.out.println( "url " + url);
	      
	      try (CloseableHttpClient client = HttpClients.createDefault()) {
	        HttpGet request = new HttpGet(url);
	        request.setHeader("Authorization", "Bearer " + token);

	        HttpResponse response = (HttpResponse) client.execute(request);
	        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	        if (entity != null) {
	        String result = EntityUtils.toString(entity);
	        Converter.saveFile("data//" + dataType + ".json", result);
	        System.out.println(dataType + " tehtud");
	        }} catch (IOException e) {
	            e.printStackTrace();
	            }
	       // System.out.println("Query result " + result);

      
      
}

  private void downloadData(String dataType) {
	  if (token == null) {
		  token = getMSToken();
	  }

	  //Folderi olemasolu kontroll-loomine
	   // Specify the path where you want to create the folder
      String folderPath = "Data"; // Update this path as needed

      // Create a File object
      File folder = new File(folderPath);

      // Check if the folder already exists
      if (!folder.exists()) {
          // Try to create the folder
          if (folder.mkdirs()) {
              System.out.println("Folder created successfully at: " + folderPath);
          } else {
              System.out.println("Failed to create the folder at: " + folderPath);
          }
      } 

      if (dataType == null || dataType.equals("salesPersons")) {
    	  this.saveFile("salesPersons");
    }

      
      if (dataType == null || dataType.equals("salesPrices")) {
    	  this.saveFile("salesPrices");
         }
      
      
      if (dataType == null || dataType.equals("salesInvoices")) {
    	  this.saveFile("salesInvoices");
  		

  		  File dir = new File("Data//lines");
  		  if (!dir.exists()) {
  			  dir.mkdir();
  			  }

			org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesInvoices.json");
		  	
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
			
			  int lenn = values.size();
			try {
			  for (int i = 0; i < lenn; i++) {
					org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
	        		String number = (String) cob.get("number");
		        	String id = (String) cob.get("id");
		        	
		        	
		        	File f = new File("data//lines//"+number+".json");
		        	if (!f.exists()) {
		        		
		        	
		        	


		        	      String urlBase = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/LIVE/api/itera/kp/v2.0/"
		        		          + "companies(" + companyID + ")/";
  		  
  		  
	     String url = urlBase + "salesInvoices("+ id +")/salesInvoiceLines";
	      
	      
	      try (CloseableHttpClient client = HttpClients.createDefault()) {
	        HttpGet request = new HttpGet(url);
	        request.setHeader("Authorization", "Bearer " + token);

	        HttpResponse response = (HttpResponse) client.execute(request);
	        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	        if (entity != null) {
	        String result = EntityUtils.toString(entity);
	        Converter.saveFile("data//lines//"+number+".json", result);
	        System.out.println("salesInvoiceLines tehtud");
	        }} catch (IOException e) {
	            e.printStackTrace();
	            }
		        	}
	       // System.out.println("Query result " + result);
}
			} catch (Exception e) {
	            e.printStackTrace();
	            }
      }
      
	  ///agedAccountsReceivables
	  //toodete json
	     
      
if (dataType == null || dataType.equals("agedAcc")) {
	  this.saveFile("agedAccountsReceivables");
}
      
      
if (dataType == null || dataType.equals("items")) {
	 this.saveFile("items");
}
	  
if (dataType == null || dataType.equals("customers")) {
	this.saveFile("customers");
}

if (dataType == null || dataType.equals("itemCategories")) {
	this.saveFile("itemCategories");
}






	  
  }
       
       
       
       
       
   public TreeMap getCustomers(Agent agent){
	   
	   downloadData(null);
	   
	   TreeMap<String, Double>volad = new TreeMap<String, Double>();
		org.json.simple.JSONObject cc1 = 	Converter.readJSONFile("data//agedAccountsReceivables.json");
 	
		org.json.simple.JSONArray values1 = (org.json.simple.JSONArray) cc1.get("value");
		
		  int lenn1 = values1.size();
		try {
		  for (int i = 0; i < lenn1; i++) {
			  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values1.get(i);

         	String number = (String)cob.get("customerNumber");
         	Double balDue = Double.parseDouble( cob.get("balanceDue")+"");
         	if (balDue != 0 && number != null && number.trim().length() > 0) {
             	volad.put(number, balDue+0.0);
         	}

          
		  }
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Võlad"+ volad.toString());
	   
	   
	   
	   
	   
		  TreeMap tm = new TreeMap();
			org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//customers.json");
	  	
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
			
			  int lenn = values.size();
			  
			try {
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	          	String number = (String)cob.get("number");
	            Customer cust = new Customer(number);
	            
	       
	            
	            
	            
	            String displayName = (String)cob.get("displayName");
	            cust.setName(displayName);
	            
	            String billToCustomerNo = (String)cob.get("billToCustomerNo");
	            cust.setPayer(billToCustomerNo);
	            
	            String address1 = (String)cob.get("addressLine1");
	            String address2 = (String)cob.get("addressLine2");
	            if (address2 != null && address2.trim().length() > 1) {
	            	address2 = ", " + address2.trim();
	            
	            }else {
	            	address2 = "";
	            }
	            if(address2.contains(address1)) {
	            	address2 = "";
	            }
	            
	            String city = (String)cob.get("city");
	            if (city != null && city.trim().length() > 1) {
	            	city = ", " + city.trim();
	            
	            }else {
	            	city = "";
	            }
	            String state = (String)cob.get("state");
	            if (state != null && state.trim().length() > 1) {
	            	state = ", " + state.trim();
	            
	            }else {
	            	state = "";
	            }
	            String country = (String)cob.get("country");
	            if (country != null && country.trim().length() > 1) {
	            	country = ", " + country.trim();
	            
	            }else {
	            	country = "";
	            }
	            String postalCode = (String)cob.get("postalCode");
	            if (postalCode != null && postalCode.trim().length() > 1) {
	            	postalCode = ", " + postalCode.trim();
	            
	            }else {
	            	postalCode = "";
	            }
	            if (city.contains(state)) {
	            	state = "";
	            }
	        //    System.out.println("klinfo " + address1 + "'" +address2 + "'" + city  + "'" + state+ "'" + country+ "'" + postalCode);
	            cust.setAddress((address1 +  address2 + city  + state + country + postalCode).trim());
	            
	            
	            String phone = (String)cob.get("phoneNumber");
	            cust.setPersonalData("Tel:" + phone);
	            
	            
	            
	            String email = (String)cob.get("email");
	            cust.setInfoEmail(email);
	            
	            
	            String salesCode = (String)cob.get("salespersonCode");
	            cust.addAgent(new Agent (salesCode));

	            String salesCode2 = (String)cob.get("salespersonCode2");
	            if(salesCode2 != null) {
	              cust.addAgent(new Agent (salesCode2));
	            }
	            
	           // String balanceDue = cob.get("balanceDue")+ "";
	            
	            String creditLimit = cob.get("creditLimit")+"";
	            cust.setCredit(creditLimit);
	            
	            
	            if (volad.get(cust.getID() ) != null) {
	            	cust.setDebt(volad.get(cust.getID()));
	            }
	            if (volad.get(cust.getPayer() ) != null) {
	            	cust.setDebt(volad.get(cust.getPayer()));
	            }
	            
	            
	            //String blocked = (String)cob.get("blocked");
	            
	          	
	          tm.put(cust.getName(), cust);
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	System.out.println("Custsize " + tm.size());
		  
		return tm;
	    
		 /*
		  * {
	      "@odata.etag": "W/\"JzE5OzE5NTUyMzgxOTY2ODM5NTk2NzExOzAwOyc=\"",
	      "id": "a2ea5579-8933-ef11-8409-0022489f6722",
	      "number": "10001",
	    1  "displayName": "Selver Anne",
	      "type": "Company",
	    1  "addressLine1": "Kalda tee 43",
	     1 "addressLine2": "",
	     1 "city": "Tartu, Tartu",
	     1 "state": "",
	    1  "country": "EE",
	     1 "postalCode": "50707",
	    1  "phoneNumber": "7461604",
	    1  "email": "selver.kaubakategooriajuhtanne@selver.ee",
	      "website": "",
	    1  "salespersonCode": "IRMAL",
	    1  "balanceDue": 0,
	    1  "creditLimit": 1,
	      "taxLiable": false,
	      "taxAreaId": "cdafba67-db1c-ef11-9f88-000d3a69189e",
	      "taxAreaDisplayName": "Kodumaised kliendid ja hankijad",
	      "taxRegistrationNumber": "",
	      "currencyId": "00000000-0000-0000-0000-000000000000",
	      "currencyCode": "EUR",
	      "paymentTermsId": "597a462b-5d1e-ef11-9f88-000d3a69189e",
	      "shipmentMethodId": "a834a542-b82f-ef11-8409-000d3a6922b3",
	      "paymentMethodId": "cf59ca74-6729-ef11-9f88-000d3ab16a11",
	   1   "blocked": "_x0020_",
	      "lastModifiedDateTime": "2024-11-05T11:39:49.14Z"
	    },
		  */

     
   }
   
   
  

   private boolean isOkDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(6, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(9, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.MONTH, -6);
		
		if(cal.after(ca)){
			return true;
		}else{
			return false;
		}
		
	  }
   
   private Date getDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
	    return date;
		
	  }
  
   
   private boolean isDueDate(String mmddyy){
	  	
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.DAY_OF_YEAR, -1);
		
		if(cal.before(ca)){
			return true;
		}else{
			return false;
		}
		
	  }

   private Vector getCP(String custid , String number, String date ){

	   Vector result = new Vector();
	   try{
			String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice_row&get=1&number=" + number;
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = this.enCode(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		int ind =  line.indexOf("item=") ;
		        		String toode = "";
			        	if(ind != -1){
			        		toode = line.substring(ind + 6);
			        		toode = toode.substring(0,toode.indexOf("\""));
			        	
			        	}
		        
			        	
			        	ind =  line.indexOf("quantity=") ;
		        		double kogus = 0;
		        		String kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 10);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		
			        		 kogus = Double.parseDouble(kkk);
			        	}
			        	
			        	ind =  line.indexOf("total=") ;
			        	double hind = 0;
		        		 kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 7);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		
			        		 hind = Double.parseDouble(kkk)/kogus;
			        		// System.out.println("toode " + toode + " " + hind);
			        	}
			        	
		        
		        result.add(new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," "));
		         }
		        	
		        }
		        in.close();
		      //  System.out.println("buffer " + result);
		        
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	   return result;
   }

   Hashtable prodCodes = null;
   
   
   public TreeMap s(Agent agent){

	   
     TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
    
     prodCodes = new Hashtable();
     Vector noGrups = new Vector();
     Vector codeEnds = new Vector();
     Vector noTexts = new Vector();
     Vector yesClasses = new Vector();
     Vector noBrands = new Vector();
     String groupIndent = null;
         TreeMap brands = new TreeMap();
         boolean sortBR = false;
         boolean useBrandAsType = false;
         if(getParent().getProps().getProperty("usebrand") != null){
        	 useBrandAsType = true;
         }
     
     if(getParent().getProps().getProperty("codeends") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("codeends"),",");
     	while (st.hasMoreElements()){
     		codeEnds.addElement(st.nextToken());
     	}
     }
     
     if(getParent().getProps().getProperty("yesclasses") != null){
      	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesclasses"),",");
      	while (st.hasMoreElements()){
      		yesClasses.addElement(st.nextToken());
      	}
      }
     
     if(getParent().getProps().getProperty("usedataforgroups") != null){
         groupIndent = getParent().getProps().getProperty("usedataforgroups");
      	prGroups = new TreeMap();
      }
     
     
     
     if(getParent().getProps().getProperty("nogroups") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogroups"),",");
     	while (st.hasMoreElements()){
     		noGrups.addElement(st.nextToken());
     	}
     }
     
     
     if(getParent().getProps().getProperty("notexts") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("notexts"),",");
     	while (st.hasMoreElements()){
     		noTexts.addElement(st.nextToken().toLowerCase());
     	}
     }
     
     Hashtable prices = null;
     
     if(getParent().getProps().getProperty("usepricelist") != null){
     	String pr = getParent().getProps().getProperty("usepricelist");
     //	prices = getPricelists(pr);
     }
     
    
     if("true".equals(getParent().getProps().getProperty("sortbybrands"))){
     	try {
 			brands = this.getProdGroupsSecond();
 			sortBR = true;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     if(getParent().getProps().getProperty("nobrands") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nobrands"),",");
     	while (st.hasMoreElements()){
     		noBrands.addElement(st.nextToken());
     	}
     }
    
     
     try {
 
    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
           BufferedReader reader = new BufferedReader(fr);
         String line = null;

         //line = reader.readLine();
         
         System.out.println("suurus " +prodsInUse.size());
          while((line = reader.readLine()) != null  ){
        	  
        	  if((line.trim().length()) > 50 ){
        		  
        	  int len = line.indexOf("code=");
        	  String id   = line.substring(len +6);
        	  id = id.substring(0,id.indexOf("\""));
      	     Product prod = new Product(id);
      	   
      	      len = line.indexOf("default_name=");
      	    String namec  = line.substring(len + 14);
      	     	 namec = namec.substring(0,namec.indexOf("\""));
      
      	    
      	 
      			String add = "";
      	    len = line.indexOf("stripe_code=");
      	    if(len != -1){
      	    	 String ean   = line.substring(len + 13);
      	    	 ean = ean.substring(0,ean.indexOf("\""));
          	     prod.setEAN(ean);
          	     
          	   if("true".equals(getParent().getProps().getProperty("add.ean"))){
          			add = ean.substring(ean.length()-3); 
          		 }
      	    }
      	   
      	
      	     
    	    
        	  if("true".equals(getParent().getProps().getProperty("name.code.front"))){
     			 prod.setName(id + " " + Converter.enCodeEst(namec) + " " +  add); 
     		 }else{
     			 prod.setName(Converter.enCodeEst(namec) + " " + id + " " + add); 
     		 }
        	    
      	    
      	     
      	     
      	   len = line.indexOf("web_price1=");
      	 len = line.indexOf("base_price=");
      	   if(len == -1){
      		 len = line.indexOf("base_price=");
      		String price   =  line.substring(len + 12);  
 	    	// System.out.println(" siin " +  prod.getName() + " " + price);
 	    	 try{

 	      	    prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))) ); 
 	    	 }catch (Exception e){
 	    		prod.setPrice(1);
 	    	 }
      	   }else{
      		 String price   =  line.substring(len + 12);  
  	    	// System.out.println(" siin " +  prod.getName() + " " + price);
       	    prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))));
       	    
      	   }
      	    	
           	 len = line.indexOf("unit=");
           	 if(len != -1){
           	   String unit   =  line.substring(len + 6);  
 	          prod.setUnit(unit.substring(0,unit.indexOf("\"")));
   	   		 
           	 }
      	  boolean goes = true;
      	if(noTexts.size() > 0){
      		
      		for(int u = 0 ; u < noTexts.size(); u++){
      			String aa = noTexts.elementAt(u) + "";
      			if(namec.toLowerCase().contains(aa)){
          			goes = false;	
      			}	
      		}
      	}
	 	    
      	
	if(codeEnds.size() > 0){
      		
      		for(int u = 0 ; u < codeEnds.size(); u++){
      			String aa = codeEnds.elementAt(u) + "";
      			if(id.toLowerCase().endsWith(aa.toLowerCase())){
          			goes = false;	
      			}	
      		}
      	}
	 	     
      	  
      		 len = line.indexOf("class=");
      	 	 if(len != -1){
      	 	     String grupp   = line.substring(len + 7);
      	 	     grupp =   grupp.substring(0,grupp.indexOf("\""));
      	 	    // System.out.println("grupp" + grupp + " " + yesClasses.toString());
      	 	     if(!yesClasses.contains(grupp)){
      	 	    	goes = false; 
      	 	     }
      	 	  prod.setGroup(grupp);
      	 	  
      	 	  if( groupIndent !=  null){
            		
           		 len = line.indexOf("<data code=\"" +groupIndent + "\"");
           		 int lll = ("<data code=\"" +groupIndent + "\"").length();
            	    if(len != -1){
            	    	 String  gr   = line.substring(len + lll + 10);
            	    	 
            	    	 gr = gr.substring(0,gr.indexOf("\""));
            	    	 prod.setGroup(gr);
                	   this.prGroups.put(gr, gr);  
                	 }
           		
           	}
      	 	 }
        	
      	 	 String areto = "";
      	 	 
      	 	 len = line.indexOf("data code=\"ALK_REG\""); 
      	 	 if(len != -1){
      	 	      areto =  line.substring(len + 29); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                // System.out.println("areto " + areto );

      	 	 }
      	 	 
      	 	 len = line.indexOf("data code=\"BRAND\""); 
      	 	 if(len != -1){
      	 	      areto =  line.substring(len + 27); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                 System.out.println("areto " + prod.getID() + " " + areto );
                if(useBrandAsType) {
                	prod.setType(areto);
                }

      	 	 }

      	 	 double mahto = 0;
      	 	 String maht = "";
      	 	 len = line.indexOf("data code=\"KOGUS\""); 
      	 	 if(len != -1){
      	 	      maht =  line.substring(len + 27); 
      	 	      maht = maht.substring(0,maht.indexOf("\""));
                 //  System.out.println("maht " + maht );
                   try{
                	   mahto = Double.parseDouble(maht.replace(',', '.'));
                   }catch(Exception e){
                	   
                   }
      	 	 }
      	 	 
      	 	 len = line.indexOf("stock_level_total=");
      	 	 if(len != -1){
      	 	     String stock   = line.substring(len + 19);  
     	          prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
     	          
      	 	 }
      	 	 
      	
        	  
      	  if(line.indexOf("ARTIKKEL_KAMP") != -1){
      	    	prod.setStatusDiscount("1");
      	    }
      	    	  
      	    
      	  if(namec.startsWith("*") && prod.getStock() < 1){
    	    	//goes = false;
    	    }
      	    
      	  if(namec.startsWith("**")){
    	    //	goes = false;
    	    }
      	  
      	  
      	if(!prodsInUse.contains(prod.getID())){
      	//  goes = false;
      	}
      	 
      	    if(goes){
      	         prodHash.put(prod.getID(), prod);
      	         tm.put(prod.getName(), prod);	
      	       if("true".equals(parent.getProps().getProperty("logistika.pluss"))){
      	            addLPProduct(prod,areto,mahto); 	
      	            }
      	        }
      	    
              }
          }
    
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   
   private void addLPProduct(Product prod,String areto ,double maht){
	   
	   //lisame toote LP-sse
	   	double nkaal = 0;
		double bkaal = 0;
		//double maht =  0;
		double alusel = 0;
		double protsent  = 0;
		String pandikood = "" ;
		String maa = "";
		String ekn = "";
		//String areto = "";
		String tootjaKood = "";
		String tootjaNimi = "";
		String grupinimi = "";
	 
	 
	    
	   
	      
	        
	      }
	  

	   
   

   
   public TreeMap s2(){

	    TreeMap tm = new TreeMap();
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		 // System.out.println(line);
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		      	      Product prod = new Product(id.substring(0,id.indexOf("\"")).trim());
		      	      len = line.indexOf("default_name=");
		      	     String name   = line.substring(len + 14);
		      	     prod.setName( prod.getID() + " " + deCode(name.substring(0,name.indexOf("\""))));
		      	     tm.put(prod.getID(), prod); 
	        	  }
	        	
	          }	    	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     return tm;
	     
	   }
   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   private Hashtable dueSums = new Hashtable();
   private Hashtable usedCredit = new Hashtable();

   public Vector makeDebtlist(){
	   //1.teeme klientide arvete nimekirja .
	   Vector debts = new Vector();
	   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		
		   try{
			 	String httpsURL = "https://login.directo.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice&get=1&customer=" + custid;
		      // System.out.println(httpsURL);
			 	Converter.doTrustToCertificates();
			 	URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = this.enCode(inputLine);
		        //	System.out.println(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		String number = "";
			        	String date = "";
			        	String duedate = "";
			        	String volg = "";
			        	String payer = "";
		        		
			        	int ind =  line.indexOf("invoice number=") ;
		        		if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		 number = number.substring(0,number.indexOf("\""));
			        	}
			        	
			        	ind =  line.indexOf("date=") ;
				        	if(ind != -1){
				        		 date = line.substring(ind + 6);
				        		 date = date.substring(0,date.indexOf("\""));
				        	}
				        		
				        	
			        	 ind =  line.indexOf("due_date=") ;
			        	if(ind != -1){
			        		 duedate = line.substring(ind + 10);
			        		duedate = duedate.substring(0,duedate.indexOf("\""));
			        	 }
			        	
			        	 ind =  line.indexOf("regno=") ;
				        	if(ind != -1){
				        		 payer = line.substring(ind + 7);
				        		 payer = payer.substring(0,payer.indexOf("\""));
				        	 }
				        	
			        	
			        	ind =  line.indexOf("balance=") ;
				        	if(ind != -1){
				        		 volg = line.substring(ind + 9);
				        		 volg = volg.substring(0,volg.indexOf("\""));
				        	 }
				        //	System.out.println(custid  + " " + number + " " + date  + " " + duedate  + " " + volg );	
				       
			        	try{
			        		 double vv = Double.parseDouble(volg);
			        		 if(vv != 0){
			        			 if(isDueDate(duedate)){
						        		     Debt dd = new Debt();
						     				  dd.setCustID(custid);
						     				  
						     				  dd.setDoknr(number);
						     				  dd.setDate(getDate(date));
						     				  dd.setDuedate(getDate(duedate));
						     				  
						     				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
						     				  dd.setDue(ff + "");
						     				  dd.setSum(vv);
						     				//	System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	
								        			  
						     				  if(dueSums.get(custid) != null){
						     					  double ss = Double.parseDouble(dueSums.get(custid) + "")+ vv ;
						     					 dueSums.put(custid, ss);    
						     				  }else{
						     					 dueSums.put(custid, volg); 
						     				  }
						     				 debts.add(dd);

									        		
						        	 }
			        			 

			     				  if(usedCredit.get(custid) != null){
			     					  double ss = Double.parseDouble(usedCredit.get(custid) + "")+ vv ;
			     					 usedCredit.put(custid, ss);    
			     				  }else{
			     					 usedCredit.put(custid, volg); 
			     				  }
			        			 
			        		 }
				        	
				        	  
			        	}catch(Exception e){
			        		//e.printStackTrace();
			        	}
				        
			        	 
		        	}           	
		        }
		      //  System.out.println(inputLineBuf);
		        in.close();
		        
		       
		   } catch (Exception a){
			   a.printStackTrace();
		   }
		   
	   }
	   
	     System.out.println("debts size" + debts.size());
	   
	   return debts;
   }

   
   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
	   
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesInvoices.json");
	  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
       		String number = (String) cob.get("number");
	        	
	        	String customerNumber = (String) cob.get("customerNumber");
	        	
	        	
	        	
	        	File f = new File("data//lines//"+number+".json");
	        	if (f.exists()) {
	        		org.json.simple.JSONObject ccL = 	Converter.readJSONFile("data//lines//"+number+".json");
	        	  	
	        		org.json.simple.JSONArray valuesL = (org.json.simple.JSONArray) ccL.get("value");
	        		
	      		  int lennL = valuesL.size();
	      		  
	    		  for (int ii = 0; ii < lennL; ii++) {
	  				org.json.simple.JSONObject line = (org.json.simple.JSONObject) valuesL.get(ii);
	         		String prodId = (String) line.get("lineObjectNumber");
	         		String shipDate = (String) line.get("shipmentDate");
	         		shipDate = shipDate.substring(5);
	         		Double netAmnt = 0d;
	         		try {
	         			netAmnt = Double.parseDouble(line.get("netAmount")+"");
	         			
	         		}catch(Exception E) {
	         			
	         		}
	         		
	         		Double qty = 0d;
	         		
	         		try {
	         			qty = Double.parseDouble(line.get("quantity")+"");;
	         			
	         		}catch(Exception E) {
	         			
	         		}
	         		
	         				
	         		Double price = 0d;
	         		
	         		try {
	         			price = netAmnt / qty;;;
	         			
	         		}catch(Exception E) {
	         			
	         		}
	         		  
	         		  CustProd cp = new CustProd(customerNumber, prodId, qty ,0 ,0 ,shipDate,price,0,0," ");
	         		  cp.setAbiKood(number);
	         		  custProds.add(cp);
	         		
	    		  }

	        	}
       
}
		} catch (Exception e) {
           e.printStackTrace();
           }
		System.out.println("Query result " + custProds.size());
 
	   return custProds;
   }


   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Directo";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
	   TreeMap<String, Double> ags = new TreeMap<String, Double>();
	   
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesInvoices.json");
	  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
			  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
      		String dateInv = (String) cob.get("invoiceDate");
      		String salespers = (String) cob.get("salesperson");
      		Double amnt = Double.parseDouble(cob.get("totalAmountExcludingTax")+"");
      		
      		
      		Date dateSInv = this.getDate(dateInv);
      		Calendar cal = Calendar.getInstance();
      		cal.set(Calendar.DAY_OF_MONTH, 1);
      		cal.set(Calendar.HOUR_OF_DAY, 0);
      		java.util.Date d = cal.getTime();
      		
      		//if (d.before(dateSInv)) {
      			Double sum = ags.get(salespers);
      			if (sum == null) {
      				ags.put(salespers, amnt);
      			}
      			else {
      				ags.put(salespers, sum+amnt);
      			}
      			
      			
     // 		}
		  }
		}catch(Exception E){
	    	
	    }
	        	
	  System.out.println(ags.toString());
	  
	  Iterator itera = ags.keySet().iterator();
	  while (itera.hasNext()) {
		  String agentID = (String) itera.next();
		  Agent agent = (Agent)agents.get(agentID);
	      if (agent != null){

	        agent.setMonthSales(ags.get(agentID).intValue());
	             		
		     }
		  
	  }

	  
	  
	  
  }

@Override
public String getSaveStockInfo() {

	
	return null;
}

@Override
public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}

@Override
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
		

          	String itemCatCode = (String)cob.get("manufacturerCode");
        	if (itemCatCode != null && itemCatCode.length() > 0) {
        		prod.setGroup(itemCatCode);
        	}
        	
          	Boolean blocked = (Boolean) cob.get("blocked");
          	
          	String add = "";
          	String gtin = (String)cob.get("barcode");
          	if (gtin != null && gtin.length() > 5) {
             	prod.setEAN(gtin);
             	add = " " + gtin.substring(gtin.length() -5);
          	}
          	

          	String kastis = cob.get("packageQty")+"";
          	if(kastis != null) {
          		try {
          		prod.setInCases(Double.parseDouble(kastis));
          		}catch(Exception e) {
          			
          		}
          	}
          	
          	String name = (String)cob.get("displayName");
          	String name2 = (String)cob.get("displayName2");
          	prod.setName(name2.trim() + " " + number + add);
          	
        
        	String unit = (String)cob.get("baseUnitOfMeasureCode");
        	if (unit != null) {
        		prod.setUnit(unit);
        	}
        	
          	String price = cob.get("currentSalesPrice")+ "";
          	if (price != null) {
          		prod.setPrice(Double.parseDouble(price));
          	}
          	
          	String inventory = cob.get("inventory") +"";
          	if (inventory != null) {
          		prod.setStock(Double.parseDouble(inventory));
          	}
          	  	
             
            if(!blocked) {
            	  tm.put(prod.getName(), prod);	
            }
          
          	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
System.out.println("Prodsize " + tm.size());
	  
	return tm;
}

@Override
public void WebReporter() {


	  ArrayList<String> filenames = new ArrayList<String>();
	     // teeme kataloogi salvestamiseks
	     		File f = new File("svdata");
	     		if(!f.exists()) {
	     			f.mkdir();
	     		}
	     		
	     		Calendar cal = Calendar.getInstance();
	     		String hh = cal.getTime().getHours()+"";
	     		if(hh.length() == 1) {
	     			hh = "0" + hh;
	     		}
	     		String mm = cal.getTime().getMinutes()+"";
	     		if(mm.length() == 1) {
	     			mm = "0" + mm;
	     		}
	     		String time2 = "_" + hh +"-" +mm;
	     	// loeme signatuurid sisse kui on olemas	
	     		HashSet done = null;
	     		try {
	     			done = (HashSet) Converter.readObject("signadone.obj");
	     		}catch(Exception e) {
	     			e.printStackTrace();
	     		}
	     		
	     		if(done == null) {
	     			 done = new HashSet();
	     		}
	     	
	
	     		
	         	
	     		Calendar algus = Calendar.getInstance();  
	     	  	Calendar lopp = Calendar.getInstance();  	
	     	  	Integer days = 8;
	     	  if(	parent.getProps().getProperty("webreport.days") != null) {
	     		  try {
	     			  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
	     		  }catch(Exception e) {
	     			  
	     		  }
	     	
	     	  };

	     	  	algus.add(Calendar.DATE, -1 * days);
	     	  	algus.set(Calendar.HOUR, 0);
	     	  	lopp.set(Calendar.HOUR, 23);
	     	  	lopp.set(Calendar.MINUTE, 59);
	     	  	lopp.add(Calendar.DATE, +2);
	     		

    			String exceldir = this.getExcelDir() + "reports/";
           		File ff = new File( exceldir );
           		if(!ff.exists()) {
           			ff.mkdir();
           		}
	     		
           		

      		  JSONArray dayreportarray = Converter.getWebDayReport(algus.getTimeInMillis(), lopp.getTimeInMillis(), rcompany, user, pass);
      		Converter.saveFileInUTF8(f.getPath() + "/currDRjson.json", dayreportarray.toString());
      		
      		

            String url = "http://reporting-files.merisoft.eu/gallery/klp/";
      		
      		TreeMap newCusts = new TreeMap();
      		
      		org.json.simple.JSONArray cc = 	Converter.readJSONArrFile(f.getPath() + "/currDRjson.json");
      		
      		
  		  int lenn = cc.size();
  		try {
  		  for (int i = 0; i < lenn; i++) {
  			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) cc.get(i);
            	String key = (String)cob.get("custid");
            	if(key.equals("NAUJAS")) {
            	try {
            	String comm = (String)cob.get("comment");
            	if(comm.contains("\"custname\"")) {
            		comm = comm.substring(comm.indexOf("{"));
            		comm = comm.substring(0,comm.lastIndexOf("}") + 1);
            		comm = comm.replaceAll("\"\"", "\"");

              		//System.out.println("comm " + comm);
            		newCusts.put(i , comm);
            	}
            	
            	}catch(Exception e){
            	}

            	}
  		  }
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		
  	//	System.out.println("newCusts " + newCusts.toString());
  		
  		
  		
  		
           		
	  try {
    	   // Siin siis uue kliendi info kogumine
    	  	String fnameNewCust = this.getExcelDir() + "ReportNewCustomer.xlsx";
    		FileInputStream excelifailNew = new FileInputStream(fnameNewCust);
    		XSSFWorkbook  wbNEW = new XSSFWorkbook(excelifailNew);
    		XSSFSheet worksheetNEW = wbNEW.getSheetAt(0);
    	     
    	  	CreationHelper createHelper2 = wbNEW.getCreationHelper();
    		
    		 XSSFCellStyle hlinkstyle2 = wbNEW.createCellStyle();
    	      XSSFFont hlinkfont2 = wbNEW.createFont();
    	      hlinkfont2.setUnderline(XSSFFont.U_SINGLE);
    	      hlinkfont2.setColor(IndexedColors.BLUE.getIndex());
    	      hlinkstyle2.setFont(hlinkfont2);
    		
    	      
    	      Iterator itera  = newCusts.values().iterator();
    	      int i = 1;
    	      while(itera.hasNext()) {
    	      String com = (String)itera.next();
    	      JSONParser p = new org.json.simple.parser.JSONParser();
    	      org.json.simple.JSONObject ob = (org.json.simple.JSONObject) p.parse(com);
    	      
    	    	String name = (String)ob.get("custname") ;
    	    	String contactemail = (String)ob.get("contactemail") ;
    	    	String contactename = (String)ob.get("contactname") ;
    	    	String agentid = (String)ob.get("agentid") ;
    	    	String street = (String)ob.get("street") ;
    	    	String contactphone = (String)ob.get("contactphone") ;
    	    	String location = (String)ob.get("location") ;
    	    	String city = (String)ob.get("city") ;
    	    	String zipcode = (String)ob.get("zipcode") ;
    	    	String regnum = (String)ob.get("regnum") ;
    	    	Long timestamp = (Long)ob.get("timestamp") ;
    	    	String agentname = (String)ob.get("agentname") ;

    	    	String pictures = (String)ob.get("pictures") ;
    	    	
    	    	
    	    	String foto1 = null;
    	    	String foto2 = null;
    	    	String foto3 = null;
    	    	
    	    	if(pictures != null) {
    	    		StringTokenizer st = new StringTokenizer(pictures,",");
    	    		foto1 = st.nextToken();
    	    		if(st.hasMoreTokens()) {
    	    			foto2 = st.nextToken();
    	    		}
    	    		if(st.hasMoreTokens()) {
    	    			foto3 = st.nextToken();
    	    		}
    	    	}
    	    	
    	    	String contact = "";
    	    	if(contactename != null) {
    	    		contact = contactename;
    	    	}
    	    	if(contactphone != null) {
    	    		contact = contact + "," + contactphone ;
    	    	}
    	    	if(contactemail != null) {
    	    		contact = contact + "," + contactemail ;
    	    	}
    	    	
    	    	
    	    	String aadress = "";
    	    	if(street != null) {
    	    		aadress = street;
    	    	}
    	    	if(city != null) {
    	    		aadress = aadress + "," + city ;
    	    	}
    	    	if(zipcode != null) {
    	    		aadress = aadress + "," + zipcode ;
    	    	}
    	    	
    	    	System.out.println(name + " " + contactemail + " " + contactename + " " + contactphone+ " " + street
    	    			+ " " + location +" " + zipcode + " " + regnum + " " +timestamp+ " " + agentname + "\n"
    	    			+ URLEncoder.encode("https://maps.google.com/maps?q=" + location, StandardCharsets.UTF_8.toString()));
    	    	
    	    	Calendar cal1 = Calendar.getInstance();
  	  			
  	  			try {
  	  				cal1.setTimeInMillis(timestamp);
  	  			}catch(Exception e) {
  	  			}
  	  			
  	  			String dateOnly = Converter.getYYYYmMMmDDmString(cal1.getTime());
  	  			

 	  			String signa = dateOnly + "" + name + "" + agentid ;
    	    	
 	  			if(!done.contains(signa)) {
 	  				worksheetNEW.createRow(i);
 	  				worksheetNEW.getRow(i).createCell(0).setCellValue(dateOnly);
    	    	worksheetNEW.getRow(i).createCell(1).setCellValue(name);
    	    	worksheetNEW.getRow(i).createCell(2).setCellValue(aadress);
    	    	worksheetNEW.getRow(i).createCell(3).setCellValue(regnum);
    	    	worksheetNEW.getRow(i).createCell(4).setCellValue(contact);

    	    	worksheetNEW.getRow(i).createCell(5).setCellValue(location);
    	    	if(location != null) {
	  		         Cell  cell = worksheetNEW.getRow(i).createCell(6);
	  		         cell.setCellValue("Maps link");
	  		         XSSFHyperlink link = (XSSFHyperlink)createHelper2.createHyperlink(Hyperlink.LINK_URL);
	  		         link.setAddress(URLEncoder.encode("https://maps.google.com/maps?q=" + location, StandardCharsets.UTF_8.toString()));
	  		         cell.setHyperlink((XSSFHyperlink) link);
	  		         cell.setCellStyle(hlinkstyle2);
	  				}
   	    	
    	    	
    	    	worksheetNEW.getRow(i).createCell(7).setCellValue(agentid);
    	    	
    	    	if(foto1 != null) {
	  		         Cell  cell = worksheetNEW.getRow(i).createCell(8);
	  		         cell.setCellValue("Photo Link 1");
	  		         XSSFHyperlink link = (XSSFHyperlink)createHelper2.createHyperlink(Hyperlink.LINK_URL);
	  		         link.setAddress(URLEncoder.encode(url + foto1, StandardCharsets.UTF_8.toString()));
	  		         cell.setHyperlink((XSSFHyperlink) link);
	  		         cell.setCellStyle(hlinkstyle2);
	  				}
    	    	

    	    	if(foto2 != null) {
	  		         Cell  cell = worksheetNEW.getRow(i).createCell(9);
	  		         cell.setCellValue("Photo Link 2");
	  		         XSSFHyperlink link = (XSSFHyperlink)createHelper2.createHyperlink(Hyperlink.LINK_URL);
	  		         link.setAddress(URLEncoder.encode(url + foto2, StandardCharsets.UTF_8.toString()));
	  		         cell.setHyperlink((XSSFHyperlink) link);
	  		         cell.setCellStyle(hlinkstyle2);
	  				}

    	    	if(foto3 != null) {
	  		         Cell  cell = worksheetNEW.getRow(i).createCell(10);
	  		         cell.setCellValue("Photo Link 3");
	  		         XSSFHyperlink link = (XSSFHyperlink)createHelper2.createHyperlink(Hyperlink.LINK_URL);
	  		         link.setAddress(URLEncoder.encode(url + foto3, StandardCharsets.UTF_8.toString()));
	  		         cell.setHyperlink((XSSFHyperlink) link);
	  		         cell.setCellStyle(hlinkstyle2);
	  				}
    	    	
 	  			 done.add(signa);
 	  			}
    	    	
    	    	
    	    	i++;
    	      }
    	     
    	      if(i > 1) {

         		FileOutputStream output_file_oos =new FileOutputStream(exceldir +
         				"newCustomer-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + time2 + ".xlsx");
         		filenames.add(exceldir + "newCustomer-" + Converter.getYYYY_MM_DD_String(new java.util.Date())+ time2 + ".xlsx");
         		wbNEW.write(output_file_oos); 
         		output_file_oos.close();  
    	      
         		try {
         			Converter.saveObject(done, "signadone.obj");
         		} catch (IOException e) {
         			e.printStackTrace();
         		}
         		
    	      }	
    	 } catch (Exception e) {
 			e.printStackTrace();
 		 }
 	  

	     System.exit(0);
	
}


}
