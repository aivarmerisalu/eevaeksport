package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

public class RahvaRaamatAPIDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker {

  public RahvaRaamatAPIDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    
      secret = parent.getProps().getProperty("api.secret");  
	  clientID =  parent.getProps().getProperty("api.clientID"); 
	  
	  user =  getParent().getProps().getProperty("rest.user");
      pass = getParent().getProps().getProperty("rest.pass");
      rcompany = getParent().getProps().getProperty("rest.companyx");
     
      getParent().setIsLastDate(true);
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
  //this.getRouting();
     // getProducts(null);
   //   token = getMSToken();
   
   //   getPriceMatrixData();
     // getCustomerNewProducts();
    // this.downloadData("getCustomerOrders");
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
		Vector result = new Vector();
		try {
		
			 Integer total = getPagination("getCustomerOrders");
	         
		 	 	for (int a = 0 ; a < total; a++) {
		 	 		String aa = "";
		 	 	    if(a > 0) {
		 	 	    	aa = a + "";
		 	 	    }
		 	 	    
		 	 	  try {	
		     	org.json.simple.JSONObject cc = Converter.readJSONFile("data//getCustomerOrders" + aa +".json");
		 		org.json.simple.JSONArray values = (org.json.simple.JSONArray) cc.get("data");
		 		
		 		  int lenn = values.size();
		 		
		 		  for (int i = 0; i < lenn; i++) {
		 			  
		 				org.json.simple.JSONObject cob = (org.json.simple.JSONObject) values.get(i);
		         		String customerNumber = (String) cob.get("location_code");
		         		String cuCode = locations.get(customerNumber);
		         		
		         		if(cuCode != null) {
		         			try {

		         				/*
		         			      "location_code": "10ETKTIK",
		         			      "product_id": "R822691",
		         			      "quantity": "2",
		         			      "date": "2026-07-20T00:00:00.000Z"
		         			     */
		         				
		         	        	String prodId = ((String) cob.get("product_id"));
		         	            String startDate = (String)cob.get("date");		

							 	MatrixLine ml = new MatrixLine();
								 ml.setCustID(cuCode);
								 ml.setProdID(prodId);
								
							     if(cob.get("quantity") != null) {
									ml.setPrice(Double.parseDouble((String)cob.get("quantity")));// see on sassis ja vajab erilähenemist	
							     }
							     
							     ml.setStartDate(startDate.substring(5,10));
							    
							    
							     /*ml.setDiscount(rs3.getDouble("hind"));
								
								double kmin = rs3.getDouble("kogusmin");
								
								ml.setMinAm(kmin);
								ml.setLineinfo(rs3.getString("doknr").trim());
								
								int color = rs3.getInt("color");
								if(color == 1) {
									ml.setCliGroup("green");
								}
		                      
								if(kmin < 0) {
									ml.setCliGroup("red");
								}
							    */
								result.add(ml);
	     
		         			}catch(Exception e) {e.printStackTrace();}
		         		}
		 		     }
		 	 	  }catch(Exception e) {e.printStackTrace();}
		 	 	}

			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("LastThree " + result.size());

		return result;

	}
	
	private String getDateX(String input) {
		if(input == null) {
			return null;
		}
		   DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        LocalDateTime dateTime = LocalDateTime.parse(input, inputFormat);
	        LocalDateTime previousDay = dateTime.minusDays(1);
	        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("d_MMM");
	        return previousDay.format(outputFormat);
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
           String startDateA = getDateX((String)cob.get("campaign_start"));
           String endDateA = getDateX((String)cob.get("campaign_end"));
           String startDateR = getDateX((String)cob.get("campaign_real_start_date"));
           String endDateR = getDateX((String)cob.get("campaign_real_end_date"));
           /* "campaign_start": "2025-08-12 00:00:00",
      "campaign_end": "2025-10-31 00:00:00",
      "campaign_name": "Selver HH 12 2025",
      "campaign_pricestick_price": 1.19,
      "campaign_real_start_date": "2025-08-12 00:00:00",
      "campaign_real_end_date": "2025-09-08 00:00:00",*/
           
 		   Date std = getDate(startDate);
	       Date end = getDate(endDate);
	     
	      if(now.after(std) && now.before(end)) {
			String custid = (String)cob.get("customer_code");
	      	String custgr = (String)cob.get("customer_group");
	      	String payer = (String)cob.get("payer");
	      	String prodid = (String)cob.get("product_code");
	      	Double sPrice = null ;
	      	try {
	      	sPrice = Double.parseDouble("" +cob.get("campaign_pricestick_price"));
	      	} catch(Exception e) {
	      		
	      	}
	      	
	      	
	      	Double price = null;
	      	try {
	      price = 	Double.parseDouble("" +cob.get("campaign_sales_price"));
	      	
} catch(Exception e) {
	      		
	      	}
	    	String cName = (String)cob.get("campaign_name");
	      
	      	Long isCamp = (Long)cob.get("is_campaign");
	      	
	      	MatrixLine mline = new MatrixLine();
	      	
	      	if(custgr != null) {
      	        mline.setCliGroup(custgr);
      	    } else if(payer != null) {
        		mline.setCliGroup(payer);
        	} else if(custid != null) {
      		    mline.setCustID(custid);
      	    }
      	
      	    mline.setProdID(prodid);
      	    if(isCamp == 1) {
      	    	String add = "";
      	    	if(startDateR != null) {
      	    		add = " K:[" +startDateR;
      	    	}
      	    	if(endDateR != null) {
      	    		add = add+ "->" + endDateR +"]";
      	    	}
      	    mline.setStartDate(startDateA);
      	    mline.setEndDate(endDateA + add );
      	    String ww = "";
      	    if(sPrice != null) {
      	    	ww = " (" + sPrice + "€)";
      	    }
      	    mline.setLineinfo(cName + ww);
      	    if(price != null) {
      	    mline.setPrice(price);
      	    
      	    lines.add(mline);
      	    }
      	     } else {
      	    	//mline.setDiscount(0);
      	    	//lines.add(mline);
      	     }
      	    
      	 //   System.out.println(mline.getStartDate() + " " + mline.getEndDate() + " " + mline.getCliGroup() );
      	    
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
	   
	   Vector res = new Vector();
	   if(returnList == null) {
		   this.getStockAndSales();
	   }
	  // System.out.println("Fix listis " + this.returnList.size());
	
	   Iterator itera =  returnList2.keySet().iterator();
		  while(itera.hasNext()) {
			  String custID = (String)itera.next();
			  HashMap sisu = (HashMap)returnList2.get(custID);
		
			  Iterator iterax =  sisu.keySet().iterator();
			  while(iterax.hasNext()) {
				  String prodid = (String) iterax.next();
				  if(prodid.contains("|")) {
					 prodid = prodid.substring(prodid.indexOf("|")+1);  
				  MatrixLine mline = new MatrixLine();
				  mline.setProdID(prodid);
				  mline.setCliGroup(custID);
				  res.add(mline);
				  }
			  }
			
			  
		  }
	   
	   itera =  returnList.keySet().iterator();
	  while(itera.hasNext()) {
		  String custID = (String)itera.next();
		  TreeMap sisu = (TreeMap)returnList.get(custID);
	
		  Iterator iterax =  sisu.keySet().iterator();
		  while(iterax.hasNext()) {
			  String prodid = ((String) iterax.next());
			//  Paneme ainult sorditud kaasa 
			  if(prodid.contains("|")) {
				  prodid = prodid.substring(prodid.indexOf("|")+1);  
				  MatrixLine mline = new MatrixLine();
				  mline.setProdID(prodid);
				  mline.setCliGroup(custID);
				  res.add(mline);
			  }
		  }
		
		  
	  }
	   
	  System.out.println("Fixed lõpetan");
	   
	return res;
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
   
   private TreeMap agent1 = new TreeMap();
   
   
   public TreeMap getAgents(){
		  TreeMap tm = new TreeMap();
		    agent1 = new TreeMap();
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
	 				 agent1.put(agent.getID(), agent);
	              }
	          	}
			  
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Agent agent0 = new Agent("ANIKEN");
        	agent0.setName("ANIKEN");
            tm.put(agent0.getName(), agent0);
			agent1.put(agent0.getID(), agent0);
			
			Agent agent = new Agent("NELEK");
        	agent.setName("NELEK");
            tm.put(agent.getName(), agent);
			agent1.put(agent.getID(), agent);
			
			
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
	        		      //  System.out.println(dataType + " tehtud");
	        		        
	        		        
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
      

      if (dataType == null || dataType.equals("getCustomerOrders")) {
  		this.saveFile("getCustomerOrders",true);
  	 }
	  
  }
       
       
       private HashMap<String, String> locations = null; 
       
   public TreeMap getCustomers(Agent agent){
	   
	   locations = new HashMap<String, String>();
	   
	   
	   if(this.loadData ) {
	    downloadData(null);
	   }
	   
	//   TreeMap routes = getRouting();
	   
	 //  System.exit(0);
	   HashMap lastDeliveries = getLastDeliveryDates();
	 //  System.out.println("ld : " + lastDeliveries.toString());
	   
		 TreeMap custagents =  Converter.getWebCustomersAgents(rcompany, user, pass);
	   
		 
		 try{
				if(rcompany != null) {
				   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
				}
			}catch(Exception e){
			}
		 
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
	            cust.addFixSortCode(number);
	            
	          //  if(routes.get(number)!= null) {
	            //	cust.setVisit(routes.get(number)+"");
	            	//System.out.println(cust.getVisit() + " " + cust.getID());
	            //}
	                  
	            cust.setName((String)cob.get("name"));
	            
	            JSONArray group = (JSONArray)cob.get("customer_group");
	            if(group != null) {
	            for (int r = 0; r < group.size(); r++) {
	                String gr = (String) group.get(r);
	                if(!gr.equals("HULGI")) {
	                	if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
		              		cust.setGroup(cust.getGroup() + "|" + Converter.cleanLitLatEst(gr.trim()));
		              	}else{
		              		cust.setGroup(Converter.cleanLitLatEst(gr.trim()));
		              	}
	                	}
	              }
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
	            
	            if(lastDeliveries.get(locCode) != null) {
	            	cust.setCity("VT: " + lastDeliveries.get(locCode));
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

		          TreeMap ags = (TreeMap) cust.getAgents().clone();
	            try {
	           Iterator itera = ags.keySet().iterator();
	          while (itera.hasNext()) {
	        	  String a1 = (String) itera.next();
	            if (a1 != null && agent1.get(a1.trim()) != null){
					Agent aab = (Agent)agent1.get(a1.trim());
					Vector vec = aab.getDubbler();
					if(vec!= null && vec.size() >0){
						Enumeration en = vec.elements();
						while(en.hasMoreElements()){
							String age = (String) en.nextElement();
							if(!cust.hasAgent(age)) {
							cust.addAgent(new Agent(age));
						 }
					   }
					}
				}
	          }
	            }catch(Exception e) {
	            	//System.out.println("ags " + ags.toString());
	            	e.printStackTrace();
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
	//System.out.println("Custsize " + tm.size());
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
     

     private HashMap returnList = null;

     private HashMap returnList2 = null;
     
     private HashMap getStockAndSales() {

    	 HashMap res = new HashMap();
    	 returnList = new HashMap();
    	 returnList2 = new HashMap();
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
 	        	
         				
         				boolean gg = false;		
         				
	         		Toode tt = new Toode(prodId);
     if(cob.get("quantity") != null) {
    	 tt.addKogus(Double.parseDouble(cob.get("quantity") + "")); 
    	 if(tt.getKogus() > 0) {
    	   gg = true;
    	 }
     }
     
     if(cob.get("sales30d") != null) {
    	 tt.addDonecur(Double.parseDouble(cob.get("sales30d") + "")); 
    	 if(tt.getDonecur() > 0 ) {
    	 gg = true;
    	 }
     }
     
     if(cob.get("sales60d") != null) {
    	 tt.addDonevol(Double.parseDouble(cob.get("sales60d") + "")); 
    	 if(tt.getDonevol() > 0 ) {
        	 gg = true;
        	 }
     }
     
     if(cob.get("sales90d") != null) {
    	 tt.addPlancur(Double.parseDouble(cob.get("sales90d") + "")); 
    	 if(tt.getPlancur() > 0 ) {
        	 gg = true;
        	 }
     }
     String dateSort = "";
   
     if(cob.get("last_arrival") != null) {
    	 dateSort = cob.get("last_arrival")+"" ; 
     }
     
     
    // if(cuCode.equals("K802755")) { //10ETKKOKOS
		// System.out.println("ABCD välja " + prodId + " " + cuCode + " " + gg ); 
	 //}

     
     if( gg && !prodsInUse.contains(prodId)) {
				prodsInUse.add(prodId);
     }
     
     if(cob.get("ABCD") != null) {
    	 
    	 tt.comment = (String) cob.get("ABCD");
    	 String inSort = (String) cob.get("in_sortiment");
    	 
    	 boolean seis = false;
    	 String stat = tt.comment;
    	 
    	 if(tt.getKogus() > 0) {
    		 seis = true;
    	 }
    	 
    	 

    	// if(cuCode.equals("K800393") && prodId.equals("R844011")) {
    	  //      System.out.println("Seisev välja0 " + customerNumber + " " + prodId + " "+ tt.getKogus() + " " + stat + " " + gg + " " + aa);
    		// }
    	 if((stat.startsWith("A") || stat.equals("B") || stat.equals("C") || stat.equals("D")) && inSort.equals("0") && gg) {

	        	String date  = (String) cob.get("last_arrival");
    		 HashMap sisu = new HashMap();
       		if(returnList2.get(cuCode) != null) {
       			sisu = (HashMap)returnList2.get(cuCode);
       			}
      		sisu.put(date + "|" + prodId, tt);
      		returnList2.put(cuCode, sisu);
      		
      		

        } else  if(stat.equals("D") && seis ) {
    		
	        	String date  = (String) cob.get("first_arrival");
	        	/* if(cuCode.equals("K800393") && prodId.equals("R844011")) {
	        	        System.out.println("Seisev välja " + customerNumber + " " + prodId + " "+ tt.getKogus() + " ");
	        		 }*/
   		 TreeMap sisu = new TreeMap();
      		if(returnList.get(cuCode) != null) {
      			sisu = (TreeMap)returnList.get(cuCode);
      			}
     		sisu.put(prodId, tt);
     		
     		if(date != null && date.trim().length() > 2) {
     			sisu.put(date + "|" + prodId, tt);
         	}
     		returnList.put(cuCode, sisu);
       } 
     }
     
             		HashMap sisu = new HashMap();
 	         		 if(res.get(cuCode) != null) {
 	         			sisu = (HashMap)res.get(cuCode);
 	         		 } 
 	         		
 	         		 if(dateSort.length() > 0) {
 	         			 tt.comment2 = dateSort;
 	         		 }
 	         		sisu.put( prodId, tt);
 	         		
 	         		res.put(cuCode, sisu);
         			}catch(Exception e) {
         			//System.out.println(cob.toJSONString());
         				e.printStackTrace();
         			}
 	         		
         		}
 	    		  }
        

 		   } catch (Exception e) {
               e.printStackTrace();
            }
     	}

 	 	
 	 	System.out.println("getCustomerStockAndSales size " + res.size());
 	 	System.out.println("ReturnList  size " + returnList.size() + " " + total);
  
    	 
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
    	 if(tt.comment != null && tt.comment.length() > 0) {
    		 res = "@" + tt.comment + " ";
    	 }
    	 if(tt.getKogus() > 0) {
    		 res = res + "Laos:" + (int)tt.getKogus() + " ";
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
     

     
     private HashMap getLastDeliveryDates() {
    	 Integer total = getPagination("getCustomerProducts");
    	   DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

         
    	   HashMap res = new HashMap();
    	   
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
    		String cuCode = customerLoc;
    		
    			
          			String prodId =(String) cob.get("item_no");
    			
		    			
		    	 String last_date = ((String) cob.get("date"));
		    	 String oldD = (String)res.get(cuCode);
		    	 if(oldD != null) {
		    		  LocalDate dateL = LocalDate.parse(last_date, fmt);
		              LocalDate dateD = LocalDate.parse(oldD, fmt);
		              if(dateL.isBefore(dateD)) {
		            	  res.put(cuCode, oldD);
		              }else {
		            	  res.put(cuCode, last_date);	  
		              }
		    	 } else {
		    		 res.put(cuCode, last_date);	 
		    	 }
             
    		
	  }
	} catch (Exception e) {
		e.printStackTrace();
	}
 	 	}
   return res;
     }
     
     private HashMap getLastDates() {
    	 Integer total = getPagination("getCustomerProducts");

    	   HashMap res = new HashMap();
    	   
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
    			if(prodsInUse.contains(prodId)) {
		    			
		    	String last_date = ((String) cob.get("date"));
		    	Double qty = Double.parseDouble(cob.get("qty") + "");
		    	String number = (String) cob.get("invoice_no");
		    	Double price = 0d ;
		    	HashMap prodd = (HashMap)res.get(cuCode);
                 if(prodd == null) {
                	 prodd = new HashMap();
                 }
                 prodd.put(prodId, last_date);
                 res.put(cuCode, prodd);
                 
    			}
    		}
    		}
	  }
	} catch (Exception e) {
		e.printStackTrace();
	}
 	 	}
   return res;
     }
    
     public Vector getCustomerProductsList(String agentCode){
   	  
  	   loadData = false;
  	   if(locations == null) {
  		   getCustomers(null);
  	   }
  	   
  	   
  	   Vector custProds = new Vector();
  	   TreeMap<String, TreeMap<String, CustProd>> sortedCP = new TreeMap<String, TreeMap<String, CustProd>>();
  	   
  	   HashMap stock = getStockAndSales();

  	   HashMap lastd = getLastDates();
  	   HashMap newProds = getCustomerNewProducts();
     
      Iterator itera = stock.keySet().iterator();
      while (itera.hasNext()) {
			String cuCode = (String) itera.next();
			HashMap prods = (HashMap)stock.get(cuCode);
			 Iterator iteraw = prods.keySet().iterator();
		      while (iteraw.hasNext()) {
					String prodId = (String)iteraw.next();
					Toode tt = (Toode)prods.get(prodId);
					
					String lineInff = null;
    				
    				HashMap st = (HashMap)stock.get(cuCode);
        			if(st != null) {
        				Toode stT = (Toode)st.get(prodId);
        				if(stT != null) {
        				 lineInff = getLineInff(stT);
        				}
        			}
        			
      	String last_date = "";
      	if(lastd.get(cuCode) != null) {
      		HashMap prodd = (HashMap)lastd.get(cuCode);
      		if(prodd.get(prodId) != null) {
      			last_date = (String) prodd.get(prodId);	
      		}
      	}
      	
      	if(lineInff != null) {
  			last_date = last_date + " " + lineInff;
  		  }
      	Double qty = tt.getKogus() ;
      
      	Double price = 0d ;
      	

       		  CustProd cp = new CustProd(cuCode, prodId, qty ,0 ,0 ,last_date,price,0,0," ");
       	
       		  
       		  if(newProds != null && newProds.get(cuCode) != null) {
       			  HashMap hh = (HashMap)newProds.get(cuCode);
       			  if(hh.get(prodId) != null) {
       				  String ttx = (String)hh.get(prodId);
  	         		  cp.setAbiKood("|tUus: " + ttx);
  	         	  }
       		  }
       		
       		  if(returnList != null && returnList.get(cuCode) != null) {
       			  TreeMap hh = (TreeMap)returnList.get(cuCode);
       			 Iterator iterx =  hh.keySet().iterator();
       			 while(iterx.hasNext()) {
       				String prodid = (String) iterx.next();
  				  if(prodid.contains("|")) {
  					 prodid = prodid.substring(prodid.indexOf("|")+1);
          			  if(prodid.equals(prodId)) {
     	         		  cp.setAbiKood("|v");
     	         	  } 
  				  }
       			 }
       			  
       		  }
       		  
       		  if(returnList2 != null && returnList2.get(cuCode) != null) {
       			  HashMap hh = (HashMap)returnList2.get(cuCode);
       			 Iterator iterx =  hh.keySet().iterator();
       			 while(iterx.hasNext()) {
       				String prodid = (String) iterx.next();
  				  if(prodid.contains("|")) {
  					 prodid = prodid.substring(prodid.indexOf("|")+1);
          			  if(prodid.equals(prodId)) {
     	         		  cp.setAbiKood("|z");
     	         	  } 
  				  }
       			 }
       			
       		  }
       		  
       		TreeMap<String, CustProd> tr = sortedCP.get(cuCode);
       		if(tr == null ) {
       			 tr = new TreeMap<String, CustProd>();
       		}
       		tr.put(last_date + prodId, cp);
       		  sortedCP.put(cuCode, tr);
       		 
      }
  	   
      }
      
     Iterator iterax =  sortedCP.keySet().iterator();
     while(iterax.hasNext()) {
    	 String cuid = (String) iterax.next();
    	 TreeMap<String, CustProd> tr = sortedCP.get(cuid);
    	 Iterator ite =  tr.keySet().iterator();
         while(ite.hasNext()) {
        	 CustProd ccp = tr.get(ite.next());
        	// System.out.println(cuid +" "+ ccp.getProdID() + " " + ccp.getLastAmountDate());
        	  custProds.add(ccp);    	 
         }
        	 
     }
      

	    Collections.reverse(custProds);
      System.out.println("custProds Size " + custProds.size());
  	   
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
private HashMap pictsLink = new HashMap();
private HashSet allProdIds = new HashSet();



@Override
public TreeMap getProducts(Agent agent) {
	  TreeMap tm = new TreeMap();
	  String weblink = parent.getProps().getProperty("weblink");
	  if(allProdIds.size() == 0) {
	   getStockAndSales();
	  }
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
          	if(prodsInUse.contains(number)) {
			Product prod = new Product(number);
			
			 if(stocks.get(number) != null) {
				 prod.setStock((Double)stocks.get(number));
			 }
			 
			String price = (String)cob.get("wholesale_price");
          	if (price != null) {
          		prod.setPrice(Double.parseDouble(price));
          	}
			
          	prod.setVat("24");
          	
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
          	prod.setName(name.replace('*', '\'').replace('*', '\'') + " " + number + add);
          	
        
        	String unit = (String)cob.get("unit");
        	if (unit != null) {
        		prod.setUnit("tk");
        	}
        	
          	
          	String inventory = (String)cob.get("inventory") ;
          	if (inventory != null) {
          		prod.setStock(Double.parseDouble(inventory));
          	}
          	
          	String fname = (String)cob.get("image_filename") ;
          	if(fname != null && fname.trim().length() > 4) {
          		pictsLink.put(number , fname);
          		
          	} 
          	 	allProdIds.add(number);
          	
          		
            
           	  tm.put(prod.getName(), prod);	
         
          	}
		  }
		} catch (Exception e) {
			//System.out.println("cob "+  cob.toString());
			e.printStackTrace();
		}
	 	}
	 	
System.out.println("Prodsize " + tm.size());
System.out.println("Imagelinks " + pictsLink.size());
	
   return tm;
}

private void addWebShopLinks(TreeMap prods) {
	
	
	
}


@Override
public void PictureMaker() {
		
		String pictssou = parent.getProps().getProperty("pict.dir");
		    String pictspda = parent.getProps().getProperty("pictpda.dir");
	   
		    String link = "http://kontor.rahvaraamat.ee/pildid_backup/";

		     loadData = false;
		  	   if(locations == null || locations.size() == 0) {
		  		   getCustomers(null);
		  	   }
		  	   
		    
		     TreeMap prods = this.getProducts(null);
		     
		     StringBuffer buf = new StringBuffer();
		    
		 try {
	 	  
	      File f = new File(pictspda +"list.txt");
	      if (f.exists())f.delete();
	      //Vaatame kas on vaja allalaadida
	      
	      HashSet pictExists = new HashSet();
 // pictsLink = new HashMap();
	     	
	    /*   Iterator enume2 = pictsLink.keySet().iterator();
	   	      while (enume2.hasNext()) {
	   	         String toode = (String)enume2.next();
	   		     String fname  = (String)pictsLink.get(toode);
	   	         File gg = new File (pictssou + fname);
	   	         boolean isJpg = true;
	             if(!gg.exists()) {
	            	 System.out.println(gg.getName());
	            	 Converter.downloadFile(link + fname, pictssou + fname);
	            	 if(!isRealJpeg(gg)) {
	            		 gg.delete();
	            		 isJpg = false;
	            	 }
	              }
	            
	            		File dest =  new File(pictspda + fname);
	            		if(!dest.exists() && isJpg) {
	            			Converter.shrinkAndCopyFile(gg,dest,200);
	            		}
	            		if(dest.length() > 0) {
	            			pictExists.add(toode);
	            		if(buf.toString().indexOf(fname +";"  + dest.length()) == -1){
	                		buf.append(fname + ";"  + dest.length() + "\n");
	                	}
	            		}
	           }*/
	   	      
	   	      // siin on otsesysteemi pildid laetud alla.
	   	      //nüüd lisame webipoets puuduvad.
	   	      
	      //System.out.println("allProdIds " + allProdIds.size());
      	
	      
	   	   Iterator itera = allProdIds.iterator();
	   	      while(itera.hasNext()) {
	   	    	  String prodID = (String)itera.next();
	   	    	  if(!pictExists.contains(prodID)) {
	   	    	File dest =  new File(pictspda + prodID+ ".jpg");
	   	    
	   	    	
        		if(!dest.exists()) {
        			  try {
        			String url = "https://web.rahvaraamat.ee/product/images?nav_product_code=" + prodID;
        			 String xml = fetchXml(url);
        			 String ur = parseImagesXml(xml);
        			 
        			// System.out.println("ur " + ur);
        			 if(ur != null) {
        			  Converter.downloadFile(ur, pictssou + prodID+ ".jpg");
        			 
        			  if(!dest.exists() ) {
        					File gg =  new File(pictssou + prodID+ ".jpg");
	            			Converter.shrinkAndCopyFile(gg,dest,200);
	            		}
        			  }
        			  } catch(Exception e) {
        				  e.printStackTrace();
        			  }
        		}
	   	      
	   	       if(dest.exists()) {
	   	    	if(dest.length() > 0) {
        			pictExists.add(prodID);
        		if(buf.toString().indexOf(prodID+ ".jpg" +";"  + dest.length()) == -1){
            		buf.append(prodID+ ".jpg" + ";"  + dest.length() + "\n");
            	}
        		} 
	   	       }
	   	      // System.exit(0);
	   	    	  }
	   	      }
	   	      
	    
	     try {
	 		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
	 		os.write(buf.toString());
	 		os.close();
	 	  }catch (Exception ex) {
	 		ex.printStackTrace();
	 	  }
		 
		 } catch (Exception e) {
		    getParent().showError(e);
		}
		 
	System.exit(0);
	
}


 
public String parseImagesXml(String xml) throws Exception {
	//System.out.println(xml);
	String res = null;
     JSONParser parser = new JSONParser();
    JSONObject ob = (JSONObject) parser.parse(xml);
     JSONArray ims = (JSONArray)ob.get("images");
 	int lenn = ims.size();
 	  
 	try {
 	  for (int i = 0; i < lenn; i++) {
 		org.json.simple.JSONObject cob = (org.json.simple.JSONObject) ims.get(i);
 		String type = (String) cob.get("type");
 		if(type.equals("product_image")){
 			JSONObject urls = (JSONObject) cob.get("urls");
 			
 			res = (String)urls.get("main");
 			
 		}
 	  }
 	} catch(Exception e) {
 		e.printStackTrace();
 	}
 	//System.out.println(res);
   
    return res;
}

public String fetchXml(String urlStr) throws Exception {
    URL url = new URL(urlStr);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(10_000);
    conn.setReadTimeout(10_000);

    int status = conn.getResponseCode();
    if (status != HttpsURLConnection.HTTP_OK) {
        throw new RuntimeException("Failed to fetch XML: HTTP status " + status);
    }

    try (InputStream in = conn.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    } finally {
        conn.disconnect();
    }
}


public static boolean isRealJpeg(File file) {
    if (!file.exists() || !file.isFile()) return false;

    try (FileInputStream fis = new FileInputStream(file)) {
        byte[] start = new byte[2];
        byte[] end = new byte[2];
        long length = file.length();

        if (length < 4) return false;

        // Read first 2 bytes
        if (fis.read(start) != 2) return false;

        // Read last 2 bytes
        fis.getChannel().position(length - 2);
        if (fis.read(end) != 2) return false;

        // Check JPEG magic bytes
        boolean hasJpegMarkers = 
            (start[0] & 0xFF) == 0xFF && (start[1] & 0xFF) == 0xD8 &&
            (end[0] & 0xFF) == 0xFF && (end[1] & 0xFF) == 0xD9;

        return hasJpegMarkers;
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}


}
