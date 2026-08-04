package eevaeksport.driver;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.simple.parser.JSONParser;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class BC365DBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,WebReporter,FixLoader {

  public BC365DBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
      secret = parent.getProps().getProperty("365.secret"); //"I~c8Q~fgLOHhFnFcIe2RHUSX6HWlPHXN8fon9b4Q"; 
	  tenantID = parent.getProps().getProperty("365.tenantID"); //"bcf61030-52c2-4048-9e18-042d8a0c856f";
	  clientID =  parent.getProps().getProperty("365.clientID"); //"075984fb-65fc-45e1-9a26-168935061542";
	  companyID =  parent.getProps().getProperty("365.companyID");
	  user =  getParent().getProps().getProperty("rest.user");
      pass = getParent().getProps().getProperty("rest.pass");
      rcompany = getParent().getProps().getProperty("rest.companyx");
     
      
      
      
      String node = getParent().getProps().getProperty("nodebt");
      if(node != null) {
    	  nodebt = new HashSet();
    	  StringTokenizer st = new StringTokenizer(node,",");
    	  while(st.hasMoreTokens()) {
    		  String nn = (String)st.nextToken();
    		  if(nn.trim().length() > 0) {
    		  nodebt.add(nn.trim());
    		  }
    	  }
      }
     
    
  
   //  token = getMSToken();
    
      System.out.println("Token " + token);

		//saveFileInvoices();

      //this.downloadData("salesPrices");
  //  this.downloadData(null);
    // this.getDebt(null);
    // this.getCustomerProductsList(null);
     //this.setBulkSales(null);
   

     }
  
     HashSet nodebt =  null;
  
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
				 int mm = 6;
			  	   if(getParent().getProps().getProperty("months") != null){
			  		   mm = Integer.parseInt(getParent().getProps().getProperty("months"));
			  	   }
			  	   
		  		 for(int x=mm; x>-1; x--) {
		 	  	    Calendar dds = Calendar.getInstance();
		   	  	       dds.set(Calendar.DAY_OF_MONTH, 1);
		 	      	   dds.add(Calendar.MONTH, - x);

		 	      	   SimpleDateFormat yymmFormat = new SimpleDateFormat("yy-MM");
		 	         		  
					org.json.simple.JSONObject cc = Converter.readJSONFile("data//" + yymmFormat.format(dds.getTime()) + "salesInvoices.json");

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
		            if(!nodebt.contains(payer)) { 
				       tm.add(dd);
		            }
		          	}
				  
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			try {
			org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesPersons.json");
	  		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
			
			  int lenn = values.size();
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

   

        	  
        	  
       public String getDistribution() {
         return null;
       }
       
       
       

        	  private void saveFileInvoices () {
        		  String folderPath = "Data";
        	      File folder = new File(folderPath);
        	      if (!folder.exists()) {
        	          if (folder.mkdirs()) {
        	              System.out.println("Folder created successfully at: " + folderPath);
        	          } else {
        	              System.out.println("Failed to create the folder at: " + folderPath);
        	          }
        	      } 
        	      String urlBase = "https://api.businesscentral.dynamics.com/v2.0/" + tenantID + "/LIVE/api/itera/kp/v2.0/"
        		          + "companies(" + companyID + ")/";
        	 
        	      
        	  	   int mm = 6;
        	  	   if(getParent().getProps().getProperty("months") != null){
        	  		   mm = Integer.parseInt(getParent().getProps().getProperty("months"));
        	  	   }
        	  	   
        	  	   
        	  	   for(int x=mm; x>-1; x--) {
        	  	    Calendar dds = Calendar.getInstance();
        	  	    int day = dds.get(Calendar.DAY_OF_MONTH);
          	  	       dds.set(Calendar.DAY_OF_MONTH, 1);
        	      	   dds.add(Calendar.MONTH, - x);

             	    Calendar dde = Calendar.getInstance();
        	      	   dde.set(Calendar.DAY_OF_MONTH, 1);
        	      	   dde.add(Calendar.MONTH, - x);
        	      	   dde.set(Calendar.DAY_OF_MONTH, dde.getActualMaximum(Calendar.DAY_OF_MONTH));
        	      	   SimpleDateFormat yymmFormat = new SimpleDateFormat("yy-MM");
        	       
        	          String fileName = yymmFormat.format(dds.getTime()) + "salesInvoices.json";
        	          File filef = new File("data//"  + fileName);
        	          
        	          if(!filef.exists() || x == 0 || (x == 1 && day < 5)) { //jooksvat kuud teeb igastahes ja eelmist ka kuni 5 kuupäevani
        	          
        		      String url = urlBase + "salesInvoices?$filter=" + URLEncoder.encode("postingDate ge " + Converter.getYYYYmMMmDDmString(dds.getTime()) +
        		    		  " and postingDate le " +  Converter.getYYYYmMMmDDmString(dde.getTime())) ;
        		      
        		      System.out.println( "url " + url);
        		      
        		      try (CloseableHttpClient client = HttpClients.createDefault()) {
        		        HttpGet request = new HttpGet(url);
        		        request.setHeader("Authorization", "Bearer " + token);

        		        HttpResponse response = (HttpResponse) client.execute(request);
        		        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
        		        if (entity != null) {
        		        String result = EntityUtils.toString(entity);
        		        Converter.saveFile("data//" +  fileName, result);
        		        System.out.println( "salesInvoices" + fileName + " tehtud");
        		        }} catch (IOException e) {
        		            e.printStackTrace();
        		            }
        		       // System.out.println("Query result " + result);
        	          }
        	  	   }
        	}

        	  
       
       
private void saveFile (String dataType) {
	  String folderPath = "Data"; 
	  
      File folder = new File(folderPath);
      if (!folder.exists()) {
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
    	  this.saveFileInvoices();
  		
  		  File dir = new File("Data//lines");
  		  if (!dir.exists()) {
  			  dir.mkdir();
  			  }
  		 int mm = 6;
	  	   if(getParent().getProps().getProperty("months") != null){
	  		   mm = Integer.parseInt(getParent().getProps().getProperty("months"));
	  	   }
	  	   
  		 for(int x=mm; x>-1; x--) {
 	  	    Calendar dds = Calendar.getInstance();
   	  	       dds.set(Calendar.DAY_OF_MONTH, 1);
 	      	   dds.add(Calendar.MONTH, - x);

 	      	   SimpleDateFormat yymmFormat = new SimpleDateFormat("yy-MM");
 	         		  
			org.json.simple.JSONObject cc = Converter.readJSONFile("data//" + yymmFormat.format(dds.getTime()) + "salesInvoices.json");
		  	
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
      }
      
	  ///agedAccountsReceivables
	  //toodete json
	     
      
if (dataType == null || dataType.equals("agedAcc")) {
	  this.saveFile("agedAccountsReceivables");
}
      
      
if (dataType == null || dataType.equals("items")) {
	 this.saveFile("items");
}

if (dataType == null || dataType.equals("stock")) {
	 this.saveFile("itemInventories");
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
		//System.out.println("Võlad"+ volad.toString());
	   
	   
	   
	   
	   
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
	            
	           // String creditLimit = cob.get("creditLimit")+"";
	           // cust.setCredit(creditLimit);
	            
	            if(!nodebt.contains(cust.getPayer())) {
	            if (volad.get(cust.getID() ) != null) {
	            	cust.setDebt(volad.get(cust.getID()));
	            }
	            if (volad.get(cust.getPayer() ) != null) {
	            	cust.setDebt(volad.get(cust.getPayer()));
	            }
	            }
	            
	            
	            //String blocked = (String)cob.get("blocked");
	            
	          	
	          tm.put(cust.getName() + cust.getID(), cust);
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	System.out.println("Custsize " + tm.size());
		  
		return tm;
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
  
   
  
   Hashtable prodCodes = null;
  
     public String getAssortmentStr(Agent agent){
       return null;
      }

     
   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
	   
		 int mm = 6;
	  	   if(getParent().getProps().getProperty("months") != null){
	  		   mm = Integer.parseInt(getParent().getProps().getProperty("months"));
	  	   }
	  	   
		 for(int x=mm; x>-1; x--) {
	  	    Calendar dds = Calendar.getInstance();
 	  	       dds.set(Calendar.DAY_OF_MONTH, 1);
	      	   dds.add(Calendar.MONTH, - x);

	      	   SimpleDateFormat yymmFormat = new SimpleDateFormat("yy-MM");
	         		  
			org.json.simple.JSONObject cc = Converter.readJSONFile("data//" + yymmFormat.format(dds.getTime()) + "salesInvoices.json");

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



   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
	   TreeMap<String, Double> ags = new TreeMap<String, Double>();
	   
	   
	   int mm = 6;
  	   if(getParent().getProps().getProperty("months") != null){
  		   mm = Integer.parseInt(getParent().getProps().getProperty("months"));
  	   }
  	   
		 for(int x=mm; x>-1; x--) {
	  	    Calendar dds = Calendar.getInstance();
	  	       dds.set(Calendar.DAY_OF_MONTH, 1);
	      	   dds.add(Calendar.MONTH, - x);

	      	   SimpleDateFormat yymmFormat = new SimpleDateFormat("yy-MM");
	         		  
		org.json.simple.JSONObject cc = Converter.readJSONFile("data//" + yymmFormat.format(dds.getTime()) + "salesInvoices.json");

	  	
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
		 }	
	 // System.out.println(ags.toString());
	  
	  Iterator itera = ags.keySet().iterator();
	  while (itera.hasNext()) {
		  String agentID = (String) itera.next();
		  Agent agent = (Agent)agents.get(agentID);
	      if (agent != null){

	        agent.setMonthSales(ags.get(agentID).intValue());
	             		
		     }
		  
	  }

	  
	  
	  
  }

  
  private HashMap<String,Double> stocks = null;
  
@Override
public String getSaveStockInfo() {
	
	  StringBuffer products = new StringBuffer();
	if(stocks == null) {
		stocks = new HashMap<String,Double>();
	}
	
	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//itemInventories.json");
  	org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("value");
	
	  int lenn = values.size();
	try {
	  for (int i = 0; i < lenn; i++) {
		  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
	//		"ItemId":"Z-SFP-0002","PhysicalInvent":150000,"InventLocationId":"3PL1-STD","expDate":"2026-07-02T12:00:00Z"	
			String prodid = (String) cob.get("itemNo");
			String ware = (String) cob.get("location");
     		Double amnt = Double.parseDouble(cob.get("inventory")+"");
  		
  		if(amnt > 0 && ware.equals("PL")) {
  			
  	         products.append(prodid +  ";");
             products.append(" ;");
             products.append(getParent().getNF().format(amnt) + "; "  +  "\n"); // + dealer +  "\n");
  				stocks.put(prodid, amnt);
  				//partii.put(prodid, part.substring(0,10));
  				
  		}
	  }
	}catch(Exception E){
    	
    }
        	
  
	
	return products.toString();
}


@Override
public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public TreeMap getProducts(Agent agent) {
	  TreeMap tm = new TreeMap();
	  
	  
      this.getSaveStockInfo();	  
	  
	  String weblink = parent.getProps().getProperty("weblink") ;
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
          	

          	Boolean mjprod = (Boolean) cob.get("mobelJava");
          	
          	String add = "";
          	String gtin = (String)cob.get("barcode");
          	if (gtin != null && gtin.length() > 5) {
             	prod.setEAN(gtin);
             	add = " " + gtin.substring(gtin.length() -5);
             	if(eanprods == null) {
                   	eanprods =new TreeMap();
                   }
                   eanprods.put(gtin.trim(), prod.getID());
                
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
          	//	prod.setStock(Double.parseDouble(inventory));
          	}
          	
          	if(stocks != null && stocks.get(prod.getID()) != null) {
          		prod.setStock(stocks.get(prod.getID()));
          	}else {
          		prod.setStock(0);
          	}
          	  	
          	if(weblink != null) {
          		prod.weblink = weblink + prod.getID();
          	}
             
          	
          	prod.setVat("24");
          	
            if(!blocked && mjprod) {
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
      		
      		

            String url = "https://reporting-files.mobitrek.eu/gallery/klp/";
      		
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
    	    	
    	    //	System.out.println(name + " " + contactemail + " " + contactename + " " + contactphone+ " " + street
    	    	//		+ " " + location +" " + zipcode + " " + regnum + " " +timestamp+ " " + agentname + "\n"
    	    		//	+ URLEncoder.encode("https://maps.google.com/maps?q=" + location, StandardCharsets.UTF_8.toString()));
    	    	
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


private TreeMap eanprods = null;

@Override
		public void FixLoader() {
		   
		  boolean hasFile = Converter.hasFixFromExcels("excelsort",parent);
		   if(hasFile) {
		   eanprods =  Converter.loadMore();
			  if(eanprods == null) {
				  eanprods = new TreeMap();
			  }
			this.getProducts(null);
			
			TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
			
		//	System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
			}
			
		     } else {
			   System.out.println("No sortiments to read");
		   }
			System.exit(0);
		}


}
