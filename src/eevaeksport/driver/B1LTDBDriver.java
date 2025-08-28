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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.util.EntityUtils;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.Quest;
import eevaeksport.QuestLine;
import eevaeksport.util.Converter;
import eevaeksport.util.ReportingService;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class B1LTDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,WebReporter {

  public B1LTDBDriver(EksportPanel parent) {
    super(parent);
    
    String folderPath = "Data"; 
    File folder = new File(folderPath);

    if (!folder.exists()) {
           folder.mkdirs(); 
    } 
    folderPath = "Data/lines"; 
    folder = new File(folderPath);

    if (!folder.exists()) {
           folder.mkdirs(); 
    } 
    
    warehouse = parent.getProps().getProperty("warehouse");
    this.subjectFilesTo = parent.getString("filemsg");
      apikey = parent.getProps().getProperty("b1.apikey");
      secret = parent.getProps().getProperty("b1.secret"); 
	 
      
	  user =  getParent().getProps().getProperty("rest.user");
      pass = getParent().getProps().getProperty("rest.pass");
      companyx = getParent().getProps().getProperty("rest.companyx");
      

	  if(getParent().getProps().getProperty("nogroups") != null){
	    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogroups"),",");
	    	while (st.hasMoreElements()){
	    		nogroups.add(st.nextToken());
	    		
	    	}
	    }
	  
	//  System.out.println(nogroups.toString());
	  if(getParent().getProps().getProperty("yesgroups") != null){
	    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesgroups"),",");
	    	while (st.hasMoreElements()){
	    	 	yesGrups.add(st.nextToken());
	    	}
	    }

    
     // getPriceMatrixData();
  //downloadData(null);
       
  }
  
  
     String warehouse  =  "6";
    
	  String user =  null;
	  String pass = null;
	  String companyx = null;
	  String secret = null;
	  String apikey = null;
      String tenantID = null;
      String companyID = null;
	  String clientID =  null;
	  String token =  null;
      HashSet nogroups = new HashSet();
      HashSet yesGrups = new HashSet();
  	
		 
		 private HttpPost getPost(String url, String data) {
			 HttpPost request = new HttpPost(url);
			  request.setHeader("B1-Api-Key", apikey);
			  request.setHeader("Content-Type", "application/json" );
			  request.setEntity(new StringEntity(data, ContentType.APPLICATION_FORM_URLENCODED));
			 return request;
		 }
		 
 
	  
	  private void downloadData(String dataType) {
	
		
		
			  if (dataType == null || dataType.equals("vat")) {
				  this.saveFile("vat", "https://www.b1.lt/api/reference-book/vat-classifications/list");
			    }
			  
			  if (dataType == null || dataType.equals("salesPersons")) {
			  this.saveFile("salesPersons", "https://www.b1.lt/api/personnel/employees/list");
		        }
		  
		  if (dataType == null || dataType.equals("customers")) {
				this.saveFile("customers","https://www.b1.lt/api/clients/list");
			   } 
		  
		  if (dataType == null || dataType.equals("custaddr")) {
				this.saveFile("custaddr","https://www.b1.lt/api/client/client-addresses/list");
			   } 
	
	      
	    		if (dataType == null || dataType.equals("items")) {
	    			 this.saveFile("items" , "https://www.b1.lt/api/reference-book/items/list");
	    		}
	
	    		if (dataType == null || dataType.equals("itemCategories")) {
	    			this.saveFile("itemCategories","https://www.b1.lt/api/reference-book/item-groups/list");
	    		}
	    		
	    		if (dataType == null || dataType.equals("sales")) {
	    			
	    			Calendar cal = Calendar.getInstance();
	    			cal.add(Calendar.MONTH, -7);
	    			String date = Converter.getYYYY_MM_DDpString(cal);

	    			org.json.simple.JSONObject filter = new org.json.simple.JSONObject();
	    			filter.put("field", "saleDate");
	    			filter.put("op", "gt");
	    			filter.put("data", date);
	    		
	    			this.saveFileWithFilter("sales","https://www.b1.lt/api/warehouse/sales/list", filter);
	    		}
	    		
	    		
	    		
	    		if (dataType == null || dataType.equals("stocks")) {
	    			this.saveFile("stocks","https://www.b1.lt/api/warehouse/stock/list");
	    		}
	    		
	    		
	    		if (dataType == null || dataType.equals("pricelist")) {
	    			org.json.simple.JSONObject filter = new org.json.simple.JSONObject();
	    			filter.put("field", "clientPrice");
	    			filter.put("op", "gt");
	    			filter.put("data", "0");
	    		
	    			this.saveFileWithFilter("pricelist","https://www.b1.lt/api/client/item-prices/list", filter);
	    		}
	  
		  
	  }

	  
	 
	 public String getAuthToken(String tanantId,String clientId,String client_secret) throws ClientProtocolException, IOException {
		    return null;
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
  
   	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//pricelist.json");
	
	int pages =  Integer.parseInt(cc.get("pages")+"");
		  System.out.println("pages prod " + pages);
		for (int u = 0; pages > u ;u++) {
			//System.out.println( "ii " + u);
			 String fname ="pricelist.json";
			 if(u > 0) {
				 fname ="pricelist" + (u+1) + ".json";
			 }
		
			cc = 	Converter.readJSONFile("data//" + fname);

	//System.out.println("fname " + fname);
	
	org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
	
	  int lenn = values.size();
	  
	try {
	  for (int i = 0; i < lenn; i++) {
	  
		org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

		
      	String client = cob.get("clientBaseId") +"";
      	String prodID = cob.get("itemId")+"";
        Double price = Double.parseDouble(cob.get("clientPrice")+"");
      	
        MatrixLine mline = new MatrixLine();
        mline.setCliGroup(client + "pr");
		  
		  if (prodID != null && prodID.length() > 0) {
		    mline.setProdID(prodID);
		  }
		
		  if (price != 0) {
		      mline.setPrice(price);
		  }

		  lines.add(mline); 
	  }
	}catch(Exception e) {
		e.printStackTrace();
	}
		}
   	
   	
   	
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
			/*	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesInvoices.json");
		  	
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

		  */

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
	   
	   
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//itemCategories.json");
	  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
			  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
           String id = cob.get("id")+"";
           String name = (String)cob.get("name");
           
         boolean goes = true;
           if (name != null && name.trim().length() > 0) {

//System.out.println("ProdGroup size :  " + nogroups.contains(name)  + " '" + name+ "'");
        	   
        	   if(nogroups.contains(name)) {
        		   if(!nogroups.contains(id)) {
        		   nogroups.add(id);
        		   }
        		   goes = false;
        	   }
        	   
        	   if(goes) {
        	      tm.put(id,Converter.enCodeEst(name));   
        	   }
        	   
           }
         	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
System.out.println("ProdGroup size :  " + tm.size());
	  
	return tm;
   

}
	   
	   
	
   
   

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
	  	if(cc != null) {
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
			
			  int lenn = values.size();
			try {
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	            String salesCode = (Long)cob.get("id") + "";
	            String salesName = (String)cob.get("name") + " " + (String)cob.get("lastName");
	            if (salesCode != null && salesCode.trim().length() > 0) {
	            	Agent agent = new Agent(salesCode);
	            	agent.setName(salesName);
	            	  tm.put(agent.getName(), agent);
	             }
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
	  	}
	  System.out.println("Managersize " + tm.size());

     return tm;
   }




        	  
        	  
       public String getDistribution() {
         return null;
       }

       
       private void saveFileWithFilter (String filename, String url ,org.json.simple.JSONObject filter) {
 	      
    		  org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
    		  obj.put("rows", 500);
    		  obj.put("page", 1);

    		  org.json.simple.JSONObject fil = new org.json.simple.JSONObject();  
    		  fil.put("groupOp", "AND");

    		  org.json.simple.JSONArray rules = new org.json.simple.JSONArray(); 
    		  rules.add(filter);
    		  
    		  fil.put("rules", rules);
    		  
    		  obj.put("filters", fil);
     		  
    		      try (CloseableHttpClient client = HttpClients.createDefault()) {
    		        HttpPost request = this.getPost(url, obj.toJSONString());
    		        HttpResponse response = (HttpResponse) client.execute(request);
    		        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
    		        if (entity != null) {
    		        String result = EntityUtils.toString(entity);
    		        Converter.saveFile("data//" + filename + ".json", result);
    		        System.out.println(filename + " tehtud");
    		        }} catch (IOException e) {
    		            e.printStackTrace();
    		            }
    		      
    		    	  //lähene laeme lõpuni sisse kui on rohkem kui 1 lk stuffi.
    		    	  
    		  	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//" + filename + ".json");
    		  	
    			int pages = 0;
    			try {
    			pages = Integer.parseInt(cc.get("pages")+"");
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			if(pages > 0) {
    				 // System.out.println("pages " + pages);
    				for (int i = 2; pages > i-1 ;i++) {
    				//	System.out.println( "ii " + i);
    					 obj.put("page", i);
    					 try (CloseableHttpClient client = HttpClients.createDefault()) {
    					        HttpPost request = this.getPost(url, obj.toJSONString());
    					        HttpResponse response = (HttpResponse) client.execute(request);
    					        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
    					        if (entity != null) {
    					        String result = EntityUtils.toString(entity);
    					        Converter.saveFile("data//" + filename + i +  ".json", result);
    					        System.out.println(filename + i + " tehtud");
    					        }} catch (IOException e) {
    					            e.printStackTrace();
    					         }
    					     
    				}
    			}
    		
    		            
    	}

       
private void saveFile (String filename, String url ) {
	      
	  org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
	  obj.put("rows", 500);
	  obj.put("page", 1);
	      try (CloseableHttpClient client = HttpClients.createDefault()) {
	        HttpPost request = this.getPost(url, obj.toJSONString());
	        HttpResponse response = (HttpResponse) client.execute(request);
	        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	        if (entity != null) {
	        String result = EntityUtils.toString(entity);
	        Converter.saveFile("data//" + filename + ".json", result);
	        System.out.println(filename + " tehtud");
	        }} catch (IOException e) {
	            e.printStackTrace();
	            }
	      
	    	  //lähene laeme lõpuni sisse kui on rohkem kui 1 lk stuffi.
	    	  
	  	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//" + filename + ".json");
	  	
		int pages =  Integer.parseInt(cc.get("pages")+"");
		
		if(pages > 1) {
			  System.out.println("pages " + pages);
			for (int i = 2; pages > i-1 ;i++) {
				//System.out.println( "ii " + i);
				 obj.put("page", i);
				 try (CloseableHttpClient client = HttpClients.createDefault()) {
				        HttpPost request = this.getPost(url, obj.toJSONString());
				        HttpResponse response = (HttpResponse) client.execute(request);
				        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
				        if (entity != null) {
				        String result = EntityUtils.toString(entity);
				        Converter.saveFile("data//" + filename + i +  ".json", result);
				        System.out.println(filename + i + " tehtud");
				        }} catch (IOException e) {
				            e.printStackTrace();
				         }
				     
			}
		}
	
	            
}

        
       
       private HashSet<String> activeCust = null;
       
       
   public TreeMap getCustomers(Agent agent){
	   
	   if(!"true".equals(getParent().getProps().getProperty("testing"))) {
	       downloadData(null);
	   }
	    
	   activeCust = new HashSet<String>();
	    addr = new HashMap<String, ArrayList>();
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//custaddr.json");
	  	
		int pages =  Integer.parseInt(cc.get("pages")+"");
			  System.out.println("pages " + pages);
			for (int u = 0; pages > u ;u++) {
				//System.out.println( "ii " + u);
				 String fname ="custaddr.json";
				 if(u > 0) {
					 fname ="custaddr" + (u+1) + ".json";
				 }
			
				cc = 	Converter.readJSONFile("data//" + fname);
	
	//	System.out.println("fname " + fname);
		
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
	          	Customer cu = new Customer(clientId + "_" + id);
	          	 cu.setAddress(Converter.enCodeEst(fullAddress));
	          	 cu.setPayer(clientId);
	          	 cu.setGroup(clientId+"pr");
	          	 
	          	if(employeeId != null) {
	          		Agent ag = new Agent(employeeId + "");
	          		cu.addAgent(ag);
	          	}
	          	
	          	if(getParent().getProps().getProperty("agents") != null) {
	          		StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("agents"),",");
	          		while(st.hasMoreElements()) {
	          			cu.addAgent(new Agent(st.nextToken()));	
	          		}
	          	}else {
	          		//romo case
	          		cu.addAgent(new Agent("61"));
		          	cu.addAgent(new Agent("39"));
		           	cu.addAgent(new Agent("64"));
		          	
	          	}
	          	
	          	ss.add(cu);
	          	addr.put(clientId, ss);
	          	
	          	
		  }
	
		}catch(Exception e) {
			e.printStackTrace();
		}
	   
			}
	  
			System.out.println("addr suurus " + addr.size());
	  // System.exit(0);
			
			boolean usecustfilter = true;
			
			if("false".equals(getParent().getProps().getProperty("usecustfilter"))) {
				usecustfilter = false;
			}
	   
		  TreeMap tm = new TreeMap();
			 cc = 	Converter.readJSONFile("data//customers.json");
	  		  pages =  Integer.parseInt(cc.get("pages")+"");
				  System.out.println("pages " + pages);
				for (int u = 0; pages > u ;u++) {
				//	System.out.println( "ii " + u);
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
	            Boolean isPotential = (Boolean)cob.get("isPotentialClient");
	            if(juridical && isPotential || !usecustfilter) {
	          	String number = cob.get("id") +"";
	          	Customer cust = new Customer(number);
	          	activeCust.add(number);
	          	cust.setGroup(number + "pr");
	            
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
	            
	            
	            String phone = (String)cob.get("phoneNumber");
	            if(phone != null) {
	             cust.setPersonalData("Tel:" + phone);
	            }
	            
	            String email = (String)cob.get("email");
	            if(email != null) {
	             cust.setInfoEmail(email);
	            }
	            
	            if(getParent().getProps().getProperty("agents") != null) {
	          		StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("agents"),",");
	          		while(st.hasMoreElements()) {
	          			cust.addAgent(new Agent(st.nextToken()));	
	          		}
	          	}else {
	          		//romo case
	          		cust.addAgent(new Agent("61"));
		          	cust.addAgent(new Agent("39"));
		           	cust.addAgent(new Agent("64"));
		          	
	          	}
	          	

	            
	            
	           if(addr.get(number) != null) {
	        	   ArrayList arr = (ArrayList)addr.get(number);
	        	   
	        	   for(int y = 0; y < arr.size();y++) {
	        		   Customer ccc = (Customer)arr.get(y);
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
	        		   tm.put(displayName + ccc.getAddress(), ccc);
	        		   
	        	   }
	        	   
	           } else {
	        	  // kui aadresse pole siis läheb pea klient
	            tm.put(displayName, cust);
	           // System.out.println("Peaklient only " +  cust.getID());
	            }
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
   
   
  

   
    Hashtable prodCodes = null;
   
   public String getAssortmentStr(Agent agent){
    return null;
  }

  private  HashMap<String, ArrayList> addr = null;
   

   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
	   
	   
	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//sales.json");
	  	
		int pages =  Integer.parseInt(cc.get("pages")+"");
			  System.out.println("pages " + pages);
			for (int u = 0; pages > u ;u++) {
				//System.out.println( "ii " + u);
				 String fname ="sales.json";
				 if(u > 0) {
					 fname ="sales" + (u+1) + ".json";
				 }
			
				cc = 	Converter.readJSONFile("data//" + fname);
	
		//System.out.println("fname " + fname);
		
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		  
		try {
		  
			for (int i = 0; i < lenn; i++) {
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
	          	
	          	Object aadr = cob.get("unloadAddressId") ;

	          	Integer id = Integer.parseInt(cob.get("id")+"");
	          	String date = cob.get("saleDate") +"";
	          	String dnumber = cob.get("series") + "" + cob.get("number");
          		String clientId = cob.get("clientId") +"";

	        	File f = new File("data//lines//"+id+"-saleItems.json");
               if(activeCust.contains(clientId)) {
        		if(!f.exists()) {
	    			org.json.simple.JSONObject filter = new org.json.simple.JSONObject();
	    			filter.put("field", "saleId");
	    			filter.put("op", "eq");
	    			filter.put("data", id);
	    			 saveFileWithFilter("lines/" + id + "-saleItems","https://www.b1.lt/api/warehouse/sale-items/list", filter);
	    			}
        	
	          	
	        	if (f.exists()) {
	        		org.json.simple.JSONObject ccL = 	Converter.readJSONFile("data//lines//"+id+"-saleItems.json");
	        	  	
	        		org.json.simple.JSONArray valuesL = (org.json.simple.JSONArray) ccL.get("data");
	        		
	      		  int lennL = valuesL.size();
	      		  
	    		  for (int ii = 0; ii < lennL; ii++) {
	  				org.json.simple.JSONObject line = (org.json.simple.JSONObject) valuesL.get(ii);
	         		String prodId = line.get("itemId") + "";
	         	
	         		
	         		Double netAmnt = 0d;
	         		try {
	         			netAmnt = Double.parseDouble(line.get("priceWithoutVatWithDiscount")+"");
	         		}catch(Exception E) {
	         		}
	         		
	         		
	         		Double qty = 0d;
	         		try {
	         			qty = Double.parseDouble(line.get("quantity")+"");;
	         		}catch(Exception E) {
	         		}
	         		
	         				
	         		Double price = 0d;
	         		try {
	         			price = netAmnt / qty;
	         		}catch(Exception E) {
	         		}
	         		String customerNumber = clientId;
	         		if(aadr != null) {
		          		 customerNumber = clientId + "_" + aadr;
	         		  System.out.println("CP " + customerNumber + " " + prodId + " " + qty + " " + price + " " + date);
	         		  CustProd cp = new CustProd(customerNumber, prodId, qty ,0 ,0 ,Converter.turn(date).substring(0,5),price,0,0," ");
	         		  cp.setAbiKood(dnumber);
	         		  custProds.add(cp);
	         		} else {
	         		    if(addr.get(clientId) != null) {
	         		       ArrayList<Customer> arr = addr.get(clientId);
	         		       for(Customer custAdr : arr) {
	         			  CustProd cp = new CustProd(custAdr.getID(), prodId, qty ,0 ,0 ,Converter.turn(date).substring(0,5),price,0,0," ");
		         		  cp.setAbiKood(dnumber);
		         		  custProds.add(cp);
		         		  }
		         	    }
	         		  }
	    		  
	        	
	          	}		
	        	}
	          	}
			}
		  
		  
		} catch (Exception e) {
	           e.printStackTrace();
	    }
			
			}
	   
	/*	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//salesInvoices.json");
	  	
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
		System.out.println("Query result " + custProds.toString());
 */
	   return custProds;
   }


   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "B1LT";
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
	 /*  TreeMap<String, Double> ags = new TreeMap<String, Double>();
	   
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

	  
	  */
	  
  }

@Override
public String getSaveStockInfo() {

	
	downloadData("stocks");
	
	
	
	  HashMap<String,Double> stock = new HashMap<String,Double>();
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//stocks.json");
		int pages =  Integer.parseInt(cc.get("pages")+"");
			  System.out.println("pages prod " + pages);
			for (int u = 0; pages > u ;u++) {
			//	System.out.println( "ii " + u);
				 String fname ="stocks.json";
				 if(u > 0) {
					 fname ="stocks" + (u+1) + ".json";
				 }
				cc = 	Converter.readJSONFile("data//" + fname);
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		  
		try {
		  for (int i = 0; i < lenn; i++) {
		  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
        	String warehouseId = cob.get("warehouseId")+"";
        	if(warehouseId.equals(warehouse)) {
            	String number = cob.get("id") + "";
            	try {
            	Double st = Double.parseDouble(cob.get("stock")+"");
            	if(stock.get(number) != null) {
            		stock.put(number,stock.get(number)+ st);
            	} else {
            		stock.put(number, st);
                }
            	
            	}catch(Exception e) {
            		
            	}
        	}
         }
		}catch(Exception r) {
			r.printStackTrace();
		}
	  
			}

		      StringBuffer products = new StringBuffer();	
	
		      
		      Iterator itera = stock.keySet().iterator();
		      while (itera.hasNext()) {
		    	  String kood = itera.next()+"";
		    	  products.append(kood +  ";");
	              products.append(" ;");
	              products.append(stock.get(kood) + ";");
	              products.append(" ;");
	              products.append(" ; \n");
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

	  try {
		this.getProdGroups();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
  
	//  System.out.println("nogr " + nogroups.toString());

	  
	  //loeme sisse jäägid 
	  HashMap<String,Double> stock = new HashMap<String,Double>();
	  
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//stocks.json");
		
		int pages =  Integer.parseInt(cc.get("pages")+"");
			  System.out.println("pages prod " + pages);
			for (int u = 0; pages > u ;u++) {
				//System.out.println( "ii " + u);
				 String fname ="stocks.json";
				 if(u > 0) {
					 fname ="stocks" + (u+1) + ".json";
				 }
			
				cc = 	Converter.readJSONFile("data//" + fname);
	
		//System.out.println("fname " + fname);
		
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		  
		try {
		  for (int i = 0; i < lenn; i++) {
		  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

          	String warehouseId = cob.get("warehouseId")+"";
          	if(warehouseId.equals(warehouse)) {

              	String number = cob.get("id") + "";
              	try {
              	Double st = Double.parseDouble(cob.get("stock")+"");
              	if(stock.get(number) != null) {
              		stock.put(number,stock.get(number)+ st);
                    	
              	}else {
              		stock.put(number, st);
               }
              	}catch(Exception e) {
              		
              	}
          	}
          	
          	
		  }
		}catch(Exception r) {
			r.printStackTrace();
		}
	  
			}
			
			
			
			
		 cc = 	Converter.readJSONFile("data//items.json");
		
		 pages =  Integer.parseInt(cc.get("pages")+"");
			  System.out.println("pages prod " + pages);
			for (int u = 0; pages > u ;u++) {
			//	System.out.println( "ii " + u);
				 String fname ="items.json";
				 if(u > 0) {
					 fname ="items" + (u+1) + ".json";
				 }
			
				cc = 	Converter.readJSONFile("data//" + fname);
	
		//System.out.println("fname " + fname);
		
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		  
		try {
		  for (int i = 0; i < lenn; i++) {
		  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

          	String number = cob.get("id") + "";

          	String code = (String)cob.get("code");
			Product prod = new Product(number);
			
			boolean goes = false;
		
          	String itemCatCode = cob.get("groupId") + "";
        	if (itemCatCode != null && itemCatCode.length() > 0) {
        		prod.setGroup(itemCatCode);
        		if(yesGrups.contains(itemCatCode)) {
        			goes = true;
        		}else if(nogroups.size() > 0 && !nogroups.contains(itemCatCode)) {
        			goes = true;
        		}
        			
        	}
          	
        	if(yesGrups.size() == 0 && nogroups.size() == 0) {
        		goes = true;
        	}
        	
        	
        	
        	if(!"true".equals(parent.getProps().getProperty("nobarcode"))) {
          	String gtin = (String)cob.get("barcode");
          	if (gtin != null && gtin.length() > 5) {
             	prod.setEAN(gtin);
             //	System.out.println("ean ok ");
          	}}
          	
          	String name = (String)cob.get("name");
          	name = name.replaceAll("\"", " ");
          	name = name.replace('(', ' ' ) ;
         	name = name.replace('(', ' ' ) ;
         	name = name.replace(')', ' ' ) ; 	
         	name = name.replace(')', ' ' ) ;
          	
          	if(name != null && code != null && name.contains(code)) {
          		name = name.replaceAll(code, " ") ;
          	}
          	name = name.replaceAll("   ", " ") ;
          	name = name.replaceAll("  ", " ") ;
          	name = name.replaceAll("  ", " ") ;
          	name = name.replaceAll("  ", " ") ;
          	prod.setName(name.trim() + " " + code);
          	
        
        	String unit = (String)cob.get("measurementUnitName");
        	if (unit != null) {
        		prod.setUnit(unit);
        	}
        	
          	String price = cob.get("priceWithoutVat")+ "";
          	if (price != null && !price.equals("null")) {
          		prod.setPrice(Double.parseDouble(price));
          	}
          	
          		prod.setVat("21");
          	 
          	
          	String inventory = cob.get("stock")+"";
          	if (inventory != null) {
          		try {
          		//prod.setStock(Double.parseDouble(inventory));
          		}catch(Exception e) {
          			
          		}
          	}
          	prod.setStock(0);
          	if(stock.get(prod.getID()) != null) {
          		  prod.setStock(stock.get(prod.getID()));
          	 	}
          	 
          	if(goes) {
            	  tm.put(prod.getName(), prod);	
          	}
          	
          
          	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
			}
System.out.println("Prodsize " + tm.size());
	  
	return tm;
}




	  

@Override
public void WebReporter() {


	Converter.uploadQuestsAndAnswers(companyx,user,pass,parent.getProps(),this.getExcelDir());
   
	     System.exit(0);
	
}

}
