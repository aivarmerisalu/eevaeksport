package eevaeksport.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
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

public class RahvaRaamatAPIDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public RahvaRaamatAPIDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
      secret = parent.getProps().getProperty("api.secret"); //"I~c8Q~fgLOHhFnFcIe2RHUSX6HWlPHXN8fon9b4Q"; 
	  clientID =  parent.getProps().getProperty("api.clientID"); //"075984fb-65fc-45e1-9a26-168935061542";
	  
	  user =  getParent().getProps().getProperty("rest.user");
      pass = getParent().getProps().getProperty("rest.pass");
      rcompany = getParent().getProps().getProperty("rest.companyx");
     

	  nodebt = new HashSet();
      String node = getParent().getProps().getProperty("nodebt");
      if(node != null) {
    	  StringTokenizer st = new StringTokenizer(node,",");
    	  while(st.hasMoreTokens()) {
    		  String nn = (String)st.nextToken();
    		  if(nn.trim().length() > 0) {
    		  nodebt.add(nn.trim());
    		  }
    	  }
      }
     
  
   //   token = getMSToken();
    
      
    //  this.downloadData("getCustomers");
    //  getCustomers(null);
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
	  String clientID =  null;
	  String token =  null;
  

	  
  private String  getMSToken() {
		 String res = null;
	  try {
		return getAuthToken(clientID,secret);
	   } catch (ClientProtocolException e2) {
		e2.printStackTrace();
	   } catch (IOException e2) {
		e2.printStackTrace();
	   }

 	 return res;
	 }
	   
	   
	 
	 public String getAuthToken(String clientId,String clientSecret) throws ClientProtocolException, IOException {
		 String url = "https://api.rahvaraamat.ee/oauth/token";
          
          String token = null;
         try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
             
        	 HttpPost post = new HttpPost(url);
             post.setHeader("Content-Type", "application/x-www-form-urlencoded");
             List<NameValuePair> params = new ArrayList<>();
             params.add(new BasicNameValuePair("grant_type", "client_credentials"));
             params.add(new BasicNameValuePair("client_id", clientId));
             params.add(new BasicNameValuePair("client_secret", clientSecret));
             post.setEntity(new UrlEncodedFormEntity(params));

             try (CloseableHttpResponse response = httpClient.execute(post)) {
                 String responseBody = EntityUtils.toString(response.getEntity());
                  token = (new org.json.JSONObject(responseBody)).getString("access_token");
             }

         } catch (Exception e) {
             e.printStackTrace();
         }
         return token;
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
				
	        	int total = this.getPagination("getDebtList");
	        	
	        	for (int a = 0 ; a < total; a++) {
	        	
	        		String aa = "";
	        	    if(a > 0) {
	        	    	aa = a + "";
	        	    }
	        		
	        	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getDebtList" + aa +".json");
				org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
				
				  int lenn = values.size();
				try {
				  for (int i = 0; i < lenn; i++) {
						org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
		        		String number = (String) cob.get("document_no");
			        	String date =(String) cob.get("document_date");
			        	String duedate = (String) cob.get("pay_date");
			        	Double volg = Double.parseDouble(cob.get("open_amount") + "");
			        	String payer = (String) cob.get("payer_code");
			        	String custid = (String) cob.get("customer_code");
			        	

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
	   
	   
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//getProductGroups.json");
	  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
			  
			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
          
           String code = (String)cob.get("code");
           if(tm.get(code) == null) {
           String name = (String)cob.get("description");
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
			org.json.simple.JSONObject cc = Converter.readJSONFile("data//getAgents.json");
	  	
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
			
			  int lenn = values.size();
		
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	            String salesCode = (String)cob.get("code");
	            String salesName = (String)cob.get("name");
	            String salesCur = (String)cob.get("sales_cur");
	            if (salesCode != null && salesCode.trim().length() > 0) {
	            	Agent agent = new Agent(salesCode);
	            	agent.setName(salesName);
	            	try {
	            	  agent.setMonthSales((int)Double.parseDouble(salesCur));
	            	} catch(Exception e) {
	            		e.printStackTrace();
	            	}
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
 
       
     private void saveFile (String dataType, boolean pagination) {
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
      String urlBase = "https://api.rahvaraamat.ee/wholesale/";
      

	      String url = urlBase + dataType;
	      
	      System.out.println( "url " + url);
	      
	      try (CloseableHttpClient client = HttpClients.createDefault()) {
	        HttpPost request = new HttpPost(url);
	        request.setHeader("Authorization", "Bearer " + token);
	        request.setHeader("Content-Type", "application/json");
	        
	        String json = "{\"paged\": false }";
	        if(pagination) {
	         json = "{\"page\": 1 ,\"pageSize\": 1000 }";
	        }
     
	        
	        StringEntity enti = new StringEntity(json, "UTF-8");
	        request.setEntity(enti);

	        HttpResponse response = (HttpResponse) client.execute(request);
	        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	        if (entity != null) {
	        String result = EntityUtils.toString(entity);
	        Converter.saveFile("data//" + dataType + ".json", result);
	        System.out.println(dataType + " tehtud");
	        
	        
	        }} catch (IOException e) {
	            e.printStackTrace();
	            }
	       

	        if(pagination) {
	        	org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//" + dataType+ ".json");
	        	org.json.simple.JSONObject cc1 = (org.json.simple.JSONObject)cc.get("pagination");
	        	
	        	int total = Integer.parseInt(cc1.get("totalPages") + "");
	        	for (int i = 1 ; i < total; i++) {
	        		
	        		 try (CloseableHttpClient client = HttpClients.createDefault()) {
	        		        HttpPost request = new HttpPost(url);
	        		        request.setHeader("Authorization", "Bearer " + token);
	        		        request.setHeader("Content-Type", "application/json");
	        		        
	        		        String json = "{\"page\": " + (i+1) + " ,\"pageSize\": 1000 }";
	        		        
	        		        
	        		        StringEntity enti = new StringEntity(json, "UTF-8");
	        		        request.setEntity(enti);

	        		        HttpResponse response = (HttpResponse) client.execute(request);
	        		        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
	        		        if (entity != null) {
	        		        String result = EntityUtils.toString(entity);
	        		        Converter.saveFile("data//" + dataType +"" + i+ ".json", result);
	        		        System.out.println(dataType + " tehtud");
	        		        
	        		        
	        		        }} catch (IOException e) {
	        		            e.printStackTrace();
	        		            }
	        		
	        	}
	        	
	        }
	      
	      // System.out.println("Query result " + result);
      
}

  private void downloadData(String dataType) {
	  
	  if (token == null) {
		  token = getMSToken();
	  }

      String folderPath = "Data"; 
      File folder = new File(folderPath);
      if (!folder.exists()) {
          if (folder.mkdirs()) {
              System.out.println("Folder created successfully at: " + folderPath);
          } else {
              System.out.println("Failed to create the folder at: " + folderPath);
          }
      } 

      if (dataType == null || dataType.equals("getAgents")) {
    	  this.saveFile("getAgents", false);
         }

      
      if (dataType == null || dataType.equals("getCustomers")) {
    		this.saveFile("getCustomers",true);
    	 }

      
      if (dataType == null || dataType.equals("getItems")) {
    		 this.saveFile("getItems", true);
    	 }
    		  

      if (dataType == null || dataType.equals("getProductGroups")) {
      	this.saveFile("getProductGroups", false);
         }
      
      
      if (dataType == null || dataType.equals("getPriceMatrix")) {
    	  this.saveFile("getPriceMatrix", true);
         }

      
      if (dataType == null || dataType.equals("getDebtList")) {
    	  this.saveFile("getDebtList", true);
         }
      
      
      if (dataType == null || dataType.equals("getCustomerProducts")) {
    	  this.saveFile("getCustomerProducts", false);
  			            }
      




	  
  }
       
       
       
       
       
   public TreeMap getCustomers(Agent agent){
	   
	 //  downloadData(null);

	   
		  TreeMap tm = new TreeMap();
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
	            
	            cust.setName((String)cob.get("name"));
	            
	            String payer = (String)cob.get("payer");
	            cust.setPayer(payer);
	            
	            String address1 = (String)cob.get("address");
	            
	            
	            String city = (String)cob.get("city");
	            if (city != null && city.trim().length() > 1) {
	            	city = ", " + city.trim();
	            }else {
	            	city = "";
	            }
	            
	            String postalCode = (String)cob.get("post_code");
	            if (postalCode != null && postalCode.trim().length() > 1) {
	            	postalCode = ", " + postalCode.trim();
	            }else {
	            	postalCode = "";
	            }
	            
	        //    System.out.println("klinfo " + address1 + "'" +address2 + "'" + city  + "'" + state+ "'" + country+ "'" + postalCode);
	            cust.setAddress((address1  + city + postalCode).trim());
	            cust.restStreet = address1;
	            cust.restCity = city;
	            cust.restZipCode = postalCode;

	            String phone = (String)cob.get("phone");
	            if(phone.length() > 0) {
	              cust.setPersonalData("Tel:" + phone);
	              cust.restPhone = phone;
	            }
	           
	            String cper = (String)cob.get("contact");
	            if(cper.length() > 0) {
	              cust.adPersonalData("|" + cper);
	              cust.restPerson = cper;
	            }
	           
	            String salesCode = (String)cob.get("salesperson_code");
	            cust.addAgent(new Agent (salesCode));

	            
	         	Double used_credit = Double.parseDouble( (String)cob.get("used_credit") );
            	if(used_credit != 0) {
            	  cust.setUsedCredit(used_credit);
            	}
           
	            String payment_term = cob.get("payment_term")+"";
	            try {
	            	if(payment_term.length() < 4) {
	                   cust.setPaymTerm(payment_term.substring(0,payment_term.length()-1));
	            	//System.out.println("jj " + payment_term);
	            	}
	 	           
	            }catch(Exception e) {
	            }
	            String creditLimit = cob.get("credit_limit")+"";
	            if(!creditLimit.equals("0")) {
	               cust.setCredit(creditLimit);
	            }
	            
	            if(!nodebt.contains(cust.getPayer())) {
	            	Double debt = Double.parseDouble( (String)cob.get("debt_due") );
	            	if(debt != 0) {
	            	  cust.setDebt(debt);
	            	}
	            }
	           

	            String blocked = cob.get("blocked")+"";
	            if(!blocked.equals("0")) {
	               cust.setSalesBlock(true);
	            }
	            
	          	if(cust.getID().length() < 8 && cust.getName().length() > 2 ) {
	              tm.put(cust.getName(), cust);
	          	}
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
		 	}
	System.out.println("Custsize " + tm.size());
	System.exit(0);
		  
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

     private Integer getPagination(String method) {
     
        org.json.simple.JSONObject cc0 = Converter.readJSONFile("data//" + method + ".json");
	    org.json.simple.JSONObject cc1 = (org.json.simple.JSONObject)cc0.get("pagination");
 	 if(cc1 == null) {
 		 return 1;
 	 }
 	     return Integer.parseInt(cc1.get("totalPages") + "");
 	   }
     
     
   public Vector getCustomerProductsList(String agentCode){
	  
	   Vector custProds = new Vector();
	   
       Integer total = getPagination("getCustomerProducts");
       
	 	for (int a = 0 ; a < total; a++) {
	 		String aa = "";
	 	    if(a > 0) {
	 	    	aa = a + "";
	 	    }
	 		
    	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getCustomerProducts" + aa +".json");
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		
		  int lenn = values.size();
		try {
		  for (int i = 0; i < lenn; i++) {
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
        		String customerNumber = (String) cob.get("code");
	        	String prodId =(String) cob.get("product_code");
	        	String last_date = (String) cob.get("last_date");
	        	Double qty = Double.parseDouble(cob.get("qty") + "");
	        	String number = (String) cob.get("invoice_no");
	        	Double price = Double.parseDouble(cob.get("last_price") + "");
	
	         		  
	         		  CustProd cp = new CustProd(customerNumber, prodId, qty ,0 ,0 ,last_date,price,0,0," ");
	         		  cp.setAbiKood(number);
	         		  custProds.add(cp);
	         		
	    		  }
       

		   } catch (Exception e) {
              e.printStackTrace();
           }
    	}
		System.out.println("CustProds size " + custProds.size());
 
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



   private String subjectFilesTo = null;
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

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
	  String weblink = parent.getProps().getProperty("weblink") ;
	  
	  
		org.json.simple.JSONObject cc = 	Converter.readJSONFile("data//getItems.json");
  	
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");

		try {
		  int lenn = values.size();
		  
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
          	  	
          	if(weblink != null) {
          		prod.weblink = weblink + prod.getID();
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




}
