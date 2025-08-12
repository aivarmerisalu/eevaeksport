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
import java.util.HashMap;
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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
     
     // getProducts(null);
   //   token = getMSToken();
   
   //   getPriceMatrixData();
     // getCustomerNewProducts();
    // this.downloadData("getStockAndSales");
     // getCustomers(null);
        //downloadData(null);
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
		return null;
	}
	
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	
   	//loeme sisse ja paneme kampaaniad ainult arvele.
   	
    Integer total = getPagination("getPriceMatrix");
    
 	for (int a = 0 ; a < total; a++) {
 		String aa = "";
 	    if(a > 0) {
 	    	aa = a + "";
 	    }
 		
	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getPriceMatrix" + aa +".json");
	org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");

	int lenn = values.size();

    Date now  = new Date(Calendar.getInstance().getTimeInMillis());
	  
	try {
	  for (int i = 0; i < lenn; i++) {
		  
		org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
           String startDate = (String)cob.get("start_day");
           String endDate = (String)cob.get("end_day");
 		   Date std = getDate(startDate);
	       Date end = getDate(endDate);
	     
	      if(now.after(std) && now.before(end)) {
			String custid = (String)cob.get("customer_code");
	      	String custgr = (String)cob.get("customer_group");
	      	String payer = (String)cob.get("payer");
	      	String prodid = (String)cob.get("product_code");
	      	Double price = Double.parseDouble("" +cob.get("price"));
	      	int disc = Integer.parseInt( (String)cob.get("discount"));
	      	
	      	MatrixLine mline = new MatrixLine();
	      	
	      	if(custgr != null) {
      	        mline.setCliGroup(custgr);
      	    } else if(payer != null) {
        		mline.setCliGroup(payer);
        	} else if(custid != null) {
      		    mline.setCustID(custid);
      	    }
      	
      	    mline.setProdID(prodid);
      	    mline.setStartDate(startDate);
      	    mline.setEndDate(endDate);
      	    mline.setDiscount(1);
      	    System.out.println(mline.getStartDate() + " " + mline.getEndDate() + " " + mline.getCliGroup() );
      	    lines.add(mline);
	     }
      }
	}catch(Exception e) {
		e.printStackTrace();
	}
 	}
  
 	System.out.println("matrix size " + lines.size());
	return lines;

   }

   
   
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
           String code = (String)cob.get("item_category_code");
           if(tm.get(code) == null ) {
           String name =  (String)cob.get("category_description");
           if (name != null && name.trim().length() > 0) {
        	  if( grinUse.contains(code)) {
        	    tm.put(code,name);
        	  }
        	   }
              }
         	}
		  
		} catch (Exception e) {
			e.printStackTrace();
		}
		  
     System.out.println("ProdGroup size :  " + tm.size() );
	return tm;
   }
	   
	 

   public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
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


   public TreeMap getStock(){
		  TreeMap tm = new TreeMap();
			try {
				
			org.json.simple.JSONObject cc = Converter.readJSONFile("data//getKeskInventory.json");
	  	
			org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
			
			  int lenn = values.size();
		
			  for (int i = 0; i < lenn; i++) {
				  
				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);

	            String prod = (String)cob.get("product_id");
	            Double kogus = Double.parseDouble((String)cob.get("quantity"));
	            
  tm.put(prod, kogus);
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
	       try {

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
	       }catch(Exception e) {
	    	   e.printStackTrace();
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


      if (dataType == null || dataType.equals("getKeskInventory")) {
    	  this.saveFile("getKeskInventory", false);
         }
      
      if (dataType == null || dataType.equals("getCustomers")) {
    		this.saveFile("getCustomers",true);
    	 }

       
      if (dataType == null || dataType.equals("getCustomerNewProducts")) {
    		this.saveFile("getCustomerNewProducts",true);
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

      
      
      if (dataType == null || dataType.equals("getCustomerProducts")) {
    	  this.saveFile("getCustomerProducts", false);
  			            }
      

      if (dataType == null || dataType.equals("getStockAndSales")) {
  		this.saveFile("getCustomerStockAndSales",true);
  	 }
      
	  
  }
       
       private TreeMap getRouting() {
    	  
    	   TreeMap rr = new TreeMap();
    	   File f = new File(this.getExcelDir() + "Contactplan.xlsx");
		
		try {
			
	       if(f.exists()){
			
	    	   Calendar cal = Calendar.getInstance();
	    	   cal.setFirstDayOfWeek(Calendar.MONDAY);
	           cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	    	   
	    	   String calKuu = "Jaanuar";
	    	   if(cal.get(Calendar.MONTH) == 1) {
	    		   calKuu = "Veebruar";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 2) {
	    		   calKuu = "Märts";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 3) {
	    		   calKuu = "Aprill";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 4) {
	    		   calKuu = "Mai";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 5) {
	    		   calKuu = "Juuni";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 6) {
	    		   calKuu = "Juuli";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 7) {
	    		   calKuu = "August";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 8) {
	    		   calKuu = "September";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 9) {
	    		   calKuu = "Oktoober";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 10) {
	    		   calKuu = "November";
	    	   }
	    	   if(cal.get(Calendar.MONTH) == 11) {
	    		   calKuu = "Detsember";
	    	   }
	    	   
	    	   
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			int jooksevKuu = 0;
			int jooksevPaev = 0;
			
			XSSFRow row = sheet.getRow(0);
			for (int i = 0; i < row.getLastCellNum(); i++) {
				
				String kuu = null;
				try {
					kuu = formatter.formatCellValue(row.getCell((short) i));
					if(kuu != null && kuu.trim().length() > 0) {
						jooksevKuu = i;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			
			//otsime päevasid
			row = sheet.getRow(1);
				
				String paev = null;
				try {
					paev = formatter.formatCellValue(row.getCell((short) jooksevKuu));
					
					int day = Integer.parseInt(paev);
					int vahe = - cal.get(Calendar.DAY_OF_MONTH) + day;
					
							
					jooksevPaev = jooksevKuu - vahe;

							System.out.println(paev + " " + vahe + " " + jooksevPaev );
				} catch (Exception e) {
					e.printStackTrace();
				}

				
				// nüüd on vaja leida selle nädala esmaspäev ja siis sealt alustada 5 välja kaupa scannimist.
				

				for (int i = 2; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					if(row != null) {
					String custID = formatter.formatCellValue(row.getCell((short) 0));
					
					for (int u = jooksevPaev;  u < jooksevPaev + 5  ; u++) {
					String pluss = formatter.formatCellValue(row.getCell((short) u)).trim();
					if(pluss.equals("+")) {
					  int pp = u-jooksevPaev;
					  String aa = "E";
					  if(pp == 1) {  aa = "T";}
					  if(pp == 2) {  aa = "K";}
					  if(pp == 3) {  aa = "N";}
					  if(pp == 4) {  aa = "R";}
					  if(rr.get(custID) != null) {
						  rr.put(custID, rr.get(custID) + aa);
					    } else {
						  rr.put(custID, aa);
					  }
					  
					}
					}
					}
				}
	     }
	 
		} catch(Exception e) {
			e.printStackTrace();
		}
    	   System.out.println(rr.toString());
    	   return rr;
       }
       
       private HashMap<String, String> locations = null; 
       
   public TreeMap getCustomers(Agent agent){
	   
	   locations = new HashMap<String, String>();
	   if(this.loadData) {
	    // downloadData(null);
	   }
	   
	   TreeMap routes = getRouting();
	   
	 //  System.exit(0);
	   
		 TreeMap custagents =  Converter.getWebCustomersAgents(rcompany, user, pass);
	   
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
	          	String locCode = (String)cob.get("location_code");
	          	locations.put(locCode, number);
	            Customer cust = new Customer(number);
	            
	            if(routes.get(number)!= null) {
	            	cust.setVisit(routes.get(number)+"");
	            	System.out.println(cust.getVisit() + " " + cust.getID());
	            }
	                  
	            cust.setName((String)cob.get("name"));
	            
	            if(cust.getName().toLowerCase().contains("maxima")) {
	            	cust.setGroup("MAXIMA");
	            }
	            if(cust.getName().toLowerCase().contains("selver")) {
	            	cust.setGroup("SELVER");
	            }
	            if(cust.getName().toLowerCase().contains("rimi ")) {
	            	cust.setGroup("RIMI");
	            }
	            
	            String payer = (String)cob.get("payer");
	            cust.setPayer(payer);
	            
	            String address1 = (String)cob.get("address");
	            cust.restStreet = address1;
	            
	            
	            
	            String city = (String)cob.get("city");
	            if (city != null && city.trim().length() > 1) {
	            	cust.restCity = city;
	            	city = ", " + city.trim();
	            }else {
	            	city = "";
	            }
	            
	            String postalCode = (String)cob.get("post_code");
	            if (postalCode != null && postalCode.trim().length() > 3) {

		            cust.restZipCode = postalCode;
	            	postalCode = ", " + postalCode.trim();
	            }else {
	            	postalCode = "";
	            }
	            
	        //    System.out.println("klinfo " + address1 + "'" +address2 + "'" + city  + "'" + state+ "'" + country+ "'" + postalCode);
	            cust.setAddress((address1  + city + postalCode).trim());
	            

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
	            if(custagents.get(cust.getID()) != null) {
	            	ArrayList ags = (ArrayList) custagents.get(cust.getID());
	            	for(int e = 0 ; e < ags.size(); e++) {
	            		cust.addAgent(new Agent(ags.get(e)+""));
	            	}
	            }
	            custInUse.add(cust.getID());
	            
	         /*	Double used_credit = Double.parseDouble( (String)cob.get("used_credit") );
            	if(used_credit != 0) {
            	  cust.setUsedCredit(used_credit);
            	}*/
           
	            String payment_term = cob.get("payment_term")+"";
	            try {
	            	if(payment_term.length() < 4) {
	                   cust.setPaymTerm(payment_term.substring(0,payment_term.length()-1));
	            	//System.out.println("jj " + payment_term);
	            	}
	 	           
	            }catch(Exception e) {
	            }
	         /*   String creditLimit = cob.get("credit_limit")+"";
	            if(!creditLimit.equals("0")) {
	               cust.setCredit(creditLimit);
	            }*/
	            
	         /*   if(!nodebt.contains(cust.getPayer())) {
	            	Double debt = Double.parseDouble( (String)cob.get("debt_due") );
	            	if(debt != 0) {
	            	  cust.setDebt(debt);
	            	}
	            }*/
	           

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
	//System.exit(0);
		  
		return tm;
   }
   
   
   
   private Date getDate(String yyyymmdd){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( yyyymmdd.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(yyyymmdd.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(yyyymmdd.substring(8, 10)));
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
     
     
     private HashMap getStockAndSales() {

    	 HashMap res = new HashMap();
    	 Integer total = getPagination("getCustomerStockAndSales");
         
 	 	for (int a = 0 ; a < total; a++) {
 	 		String aa = "";
 	 	    if(a > 0) {
 	 	    	aa = a + "";
 	 	    }
 	 	  try {	
     	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getCustomerStockAndSales" + aa +".json");
 		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
 		
 		  int lenn = values.size();
 		
 		  for (int i = 0; i < lenn; i++) {
 				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
         		String customerNumber = (String) cob.get("location");
         		String cuCode = locations.get(customerNumber);
         		if(cuCode != null) {
         			try {

         	        	String prodId = ((String) cob.get("product_id"));
         				if(prodId == null) {
         					prodId = ((String) cob.get("item_id"));
         				}
         				prodId = prodId.trim();
 	        	
	         		Toode tt = new Toode(prodId);
     if(cob.get("quantity") != null) {
    	 tt.addKogus(Double.parseDouble(cob.get("quantity") + "")); 
     }
     
     if(cob.get("sales30d") != null) {
    	 tt.addDonecur(Double.parseDouble(cob.get("sales30d") + "")); 
     }
     
     if(cob.get("sales60d") != null) {
    	 tt.addDonevol(Double.parseDouble(cob.get("sales60d") + "")); 
     }
     
     if(cob.get("sales90d") != null) {
    	 tt.addPlancur(Double.parseDouble(cob.get("sales90d") + "")); 
     }
	    
 	         		
 	         		HashMap sisu = new HashMap();
 	         		if(res.get(cuCode) != null) {
 	         			sisu = (HashMap)res.get(cuCode);
 	         		}
 	         		
 	         		sisu.put(prodId, tt);
 	         		res.put(cuCode, sisu);
         			}catch(Exception e) {
         			System.out.println(cob.toJSONString());
         				e.printStackTrace();
         			}
 	         		
         		}
 	    		  }
        

 		   } catch (Exception e) {
               e.printStackTrace();
            }
     	}

 	 	
 	 	System.out.println("getCustomerStockAndSales size " + res.size());
  
    	 
    	 return res;
     }

     
     private HashMap getCustomerNewProducts() {

    	 HashMap res = new HashMap();
    	 Integer total = getPagination("getCustomerNewProducts");
         
 	 	for (int a = 0 ; a < total; a++) {
 	 		String aa = "";
 	 	    if(a > 0) {
 	 	    	aa = a + "";
 	 	    }
 	 		
     	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getCustomerNewProducts" + aa +".json");
 		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
 		
 		  int lenn = values.size();
 		try {
 		  for (int i = 0; i < lenn; i++) {
 				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
         		String customerNumber = (String) cob.get("customer");
         		
         		String cuCode = locations.get(customerNumber);
         		if(cuCode != null) {
 	        	
 	        	String prodId = (String) cob.get("item_no");
 	        	String date = (String) cob.get("date");
 	 	         
 	         		
 	         		HashMap sisu = new HashMap();
 	         		if(res.get(cuCode) != null) {
 	         			sisu = (HashMap)res.get(cuCode);
 	         		}
 	         		
 	         		sisu.put(prodId, date);
 	         		res.put(cuCode, sisu);
 	         		
         		}
 	    		  }
        
 		   } catch (Exception e) {
               e.printStackTrace();
            }
     	}

 	 	
 	 	System.out.println("getCustomerNewProducts size " + res.size());
  
    	 
    	 return res;
     }
     

     private boolean loadData = true;
     
    
     private String getLineInff(Toode tt) {
    	 String res = "";
    	 if(tt.getKogus() > 0) {
    		 res = "Laos:" + (int)tt.getKogus() + " ";
    	 }
    	 if(tt.getDonecur() > 0) {
    		 res = res +  "M30:" + (int)tt.getDonecur() + " ";
    	 }
    	 if(tt.getDonevol() > 0) {
    		 res = res + "M60:" + (int)tt.getDonevol() + " ";
    	 }
    	 if(tt.getPlancur() > 0) {
    		 res = res + "M90:" + (int)tt.getPlancur();
    	 }
    	 return res.trim();
     }
     
   public Vector getCustomerProductsList(String agentCode){
	  
	   loadData = false;
	   if(locations == null) {
		   getCustomers(null);
	   }
	   
	   
	   Vector custProds = new Vector();
	   HashMap stock = getStockAndSales();
	   HashMap newProds = getCustomerNewProducts();
	   
	   
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
        		String customerLoc = (String) cob.get("customer");
        		String cuCode = locations.get(customerLoc);
        		if(cuCode != null) {
        			
        		if(custInUse.contains(cuCode)) {
        			
              			String prodId =(String) cob.get("item_no");

        //			System.out.println("Kl kood olemas ja kasutusel " + cuCode + " " + prodsInUse.size() + " " + prodsInUse.contains(prodId));        			
	      
        			
        			if(prodsInUse.contains(prodId)) {
        				
        				String lineInff = null;
        				
        				HashMap st = (HashMap)stock.get(cuCode);
            			if(st != null) {
            				Toode stT = (Toode)st.get(prodId);
            				if(stT != null) {
            				 lineInff = getLineInff(stT);
            				}
            			}
            			 
        				
        				
	        	String last_date = ((String) cob.get("date")).substring(5);
	        	Double qty = Double.parseDouble(cob.get("qty") + "");
	        	String number = (String) cob.get("invoice_no");
	        	Double price = 0d ;
	        	try {
	        	price = Double.parseDouble((String)cob.get("unit_price"));
	        	}catch(Exception e) {
	        		
	        	}
	
	        	if(lineInff != null) {
       			 // cp.setAbiKood(lineInff);
	        		last_date = last_date + " " + lineInff;
       		  }
	         		  CustProd cp = new CustProd(cuCode, prodId, qty ,0 ,0 ,last_date,price,0,0," ");
	         	
	         		  
	         		  if(newProds.get(cuCode) != null) {
	         			  HashMap hh = (HashMap)newProds.get(cuCode);
	         			  if(hh.get(prodId) != null) {
	         				  String tt = (String)hh.get(prodId);
	    	         		  cp.setAbiKood("|t" + tt);
	    	         		//  System.out.println("uus toode " + cuCode + " " + prodId);
	         			  }
	         		  }
	         		  
	         		  custProds.add(cp);
	         		
	    		  }
		  }
        		}
       
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
     return "RahvaRaamat";
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

private HashSet grinUse = new HashSet();
private HashSet prodsInUse = new HashSet();
private HashSet custInUse = new HashSet();



@Override
public TreeMap getProducts(Agent agent) {
	  TreeMap tm = new TreeMap();
	  String weblink = parent.getProps().getProperty("weblink") ;
	  
	  TreeMap stocks =  getStock() ;
	  
	  Integer total = getPagination("getItems");
      
	 	for (int a = 0 ; a < total; a++) {
	 		String aa = "";
	 	    if(a > 0) {
	 	    	aa = a + "";
	 	    }
	 		
  	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getItems" + aa +".json");
		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");

		org.json.simple.JSONObject cob = null;
		try {
		  int lenn = values.size();
		  
		  for (int i = 0; i < lenn; i++) {
			  
			 cob = (org.json.simple.JSONObject) values.get(i);
	

          	String number = (String)cob.get("product_id");
			Product prod = new Product(number);
			 prodsInUse.add(number);
			
			 if(stocks.get(number) != null) {
				 prod.setStock((Double)stocks.get(number));
			 }
			 
			String price = (String)cob.get("wholesale_price");
          	if (price != null) {
          		prod.setPrice(Double.parseDouble(price));
          	}
			
          	String itemDivCode = (String)cob.get("division_code");
        	
          	String itemCatCode = (String)cob.get("item_category_code");
        	if (itemCatCode != null && itemCatCode.length() > 0) {
        		prod.setGroup(itemCatCode);
        		if(!grinUse.contains(itemCatCode)) {
        			grinUse.add(itemCatCode);
        		}
        	}
         	
        	String itemGrCode = (String)cob.get("product_group_code");
        	if (itemGrCode != null && itemGrCode.length() > 0) {
        		prod.setGroupSecond(itemGrCode);
        	}
        	
          	String add = "";
          	String gtin = (String)cob.get("EAN");
          	if (gtin != null && gtin.length() > 5) {
             	prod.setEAN(gtin);
             	add = " " + gtin.substring(gtin.length() -5);
          	}
        
          	
          	
          	String name = (String)cob.get("description");
          	prod.setName(name + " " + number + add);
          	
        
        	String unit = (String)cob.get("unit");
        	if (unit != null) {
        		prod.setUnit("tk");
        	}
        	
          	
          	String inventory = (String)cob.get("inventory") ;
          	if (inventory != null) {
          		prod.setStock(Double.parseDouble(inventory));
          	}
          	
            
           	  tm.put(prod.getName(), prod);	
            
          	}
		  
		} catch (Exception e) {
			System.out.println("cob "+  cob.toString());
			e.printStackTrace();
		}
	 	}
System.out.println("Prodsize " + tm.size());
	  
	return tm;
}




}
