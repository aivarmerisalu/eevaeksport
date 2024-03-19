package eevaeksport.util;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import eevaeksport.Product;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.EksportPanel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;


public class WebShopPiguLT {
	private String url = null;
	private String username = null;
	private String password = null;
	private CloseableHttpClient client;
	private String token = null;
	private String sellerID = null;
	private HashMap eanProds = null;
	private String kaup24Klient = null;
	private String piguLTKlient = null;
	private String lati220Klient = null;
	
	
	public WebShopPiguLT(HashMap eanProds) {
		this.eanProds = eanProds;
		// TODO Auto-generated constructor stub
	}





	public boolean login(EksportPanel parent) {
		
		url = parent.getProps().getProperty("piguURL");
		password = parent.getProps().getProperty("piguPassword");
		username = parent.getProps().getProperty("piguUsername");
		kaup24Klient = parent.getProps().getProperty("kaup24");
		 piguLTKlient = parent.getProps().getProperty("piguLT");
		 lati220Klient = parent.getProps().getProperty("220LV");
		
		File fileToFind = new File("WebShopper");
		if (!fileToFind.exists()) {
			fileToFind.mkdir();
		}
		
		
        // Specify the path to the text file
        String filePath = "WebShopper/piguToken.txt";
        // Create a Path object for the specified file
        Path path = Paths.get(filePath);
        // Check if the file exists
        if (Files.exists(path)) {
            try {
                // Get the last modified timestamp of the file
                long lastModifiedMillis = Files.getLastModifiedTime(path).toMillis();
                // Get the current time
                Instant currentTime = Instant.now();
                // Convert the last modified timestamp to Instant
                Instant lastModifiedTime = Instant.ofEpochMilli(lastModifiedMillis);
                // Calculate the duration between the last modified time and the current time
                Duration duration = Duration.between(lastModifiedTime, currentTime);
                // Get the age of the file in seconds
                long ageInSeconds = duration.getSeconds();
                // Print the age of the file
                token = (String) Converter.readJSONFile(filePath).get("token");
                if (ageInSeconds <   2160000) {

                    return true;
        		}
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File does not exist.");
        }
        String jsonBody = "{\r\n"
        		+ "   \"username\": \""+ username +"\",\r\n"
        		+ "   \"password\": \""+ password +"\"\r\n"
        		+ "}";
        try {
         	HttpClient cli = getClient();
    		HttpResponse resp = restPostRequest(url+"v3/login", jsonBody, cli);
    		String aaa = EntityUtils.toString(resp.getEntity());
            Converter.saveFile("WebShopper/piguToken.txt", aaa);
            token = (String) Converter.readJSONFile(filePath).get("token");
        } catch(Exception e) {
        	e.printStackTrace();
        }
		return false;
	}
	
	
	
	
	
	private HttpClient getClient() throws Exception{
		if (client == null){
			client =  HttpClients.createDefault();
			
		}
		return client;
	}
	
	
	
	private HttpResponse restPostRequest(String uri, String json, HttpClient client) throws Exception {

		 HttpPost httpPost = new HttpPost(uri);
		 if(json != null){
			 httpPost.setEntity(new StringEntity(json,"utf-8"));
		        httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
		 }
	        return client.execute(httpPost);
	        

	}
	
	
	private HttpResponse restGetRequest(String uri, HttpClient client) throws Exception {
		 HttpGet httpPost = new HttpGet(uri);
		 System.out.println(uri);
			// httpPost.setEntity(new StringEntity(json,"utf-8"));
		       // httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Authorization", "Pigu-mp "+token);
	        return client.execute(httpPost);

	}
	

public static String getCurrentDateTimeMinusTwoWeeks() {

    LocalDateTime currentTime = LocalDateTime.now();
    LocalDateTime twoWeeksAgo = currentTime.minusWeeks(2);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return twoWeeksAgo.format(formatter);
    
}
	public boolean getSellerID() {
		 String filePath = "WebShopper/piguCompany.txt";
		 File f = new File(filePath);
		 if (!f.exists()) {
	    try {
         	HttpClient cli = getClient();
    		HttpResponse resp = restGetRequest(url+"v3/sellers/me", cli);
    		String aaa = EntityUtils.toString(resp.getEntity());
            Converter.saveFile(filePath, aaa);
            sellerID = ((Long) Converter.readJSONFile(filePath).get("id"))+"";
            //System.out.println(sellerID);
        } catch(Exception e) {
        	e.printStackTrace();
        }
		}else {
			sellerID = ((Long) Converter.readJSONFile(filePath).get("id"))+"";
		}
		
		return false;
	}
	
// <>
	public ArrayList<Order> getOrderList() {
		ArrayList<Order> orders = new ArrayList<Order>();
		
	
	        String directoryPath = "WebShopper";
	        String filePrefix = "piguOrderList";
	        File directory = new File(directoryPath);
	        
	      
	        
	        if (directory.exists() && directory.isDirectory()) {
	            File[] files = directory.listFiles();
	            if (files != null) {
	                for (File file : files) {
	                    if (file.getName().startsWith(filePrefix)) {
	                        if (file.delete()) {
	                            System.out.println("Deleted file: " + file.getName());
	                        } else {
	                            System.out.println("Failed to delete file: " + file.getName());
	                        }
	                    }
	                }
	            }
	        } else {
	            System.out.println("Directory does not exist or is not a directory: " + directoryPath);
	        }

	        
	int co = 0;
	int offset = 0;
	String twoWeeks = getCurrentDateTimeMinusTwoWeeks();
	String date_from = "created_at_from="+URLEncoder.encode(twoWeeks);
 	String status = "&order_status=submitted";
	while (true) {
	    try {
        	HttpClient cli = getClient();
        	
        	String offS = "";
        	if (offset > 0) {
        		offS = "&offset="+offset;
        	}
       
        
   		HttpResponse resp = restGetRequest(url + "v3/sellers/" + sellerID + "/orders?" + date_from + offS + status, cli);
   		//HttpResponse resp = restGetRequest(url + "v3/sellers/" + sellerID + "/orders?" + date_from + offS , cli);
   		String aaa = EntityUtils.toString(resp.getEntity());
           Converter.saveFile(directoryPath+ "/" + filePrefix + co + ".txt", aaa);
           JSONObject ordersJ = Converter.readJSONFile(directoryPath+ "/" + filePrefix + co + ".txt");
           JSONArray ordArr = (JSONArray) ordersJ.get("orders");
           if (ordArr.size() == 20) {
        	   offset = offset + 20;
           }else {
        	  break; 
           }
       } catch(Exception e) {
       	e.printStackTrace();
       }
       
		co++;
	}
	
	

       
	        
	        if (directory.exists() && directory.isDirectory()) {
	            File[] files = directory.listFiles();
	            if (files != null) {
	                for (File file : files) {
	                    if (file.getName().startsWith(filePrefix)) {
	                            System.out.println("Detected OrderList: " + file.getName());
	                            
	                            
	                            
	                    		JSONObject ordersJ = Converter.readJSONFile(directory + "/" + file.getName());
	                    		JSONArray ordArr = (JSONArray) ordersJ.get("orders");
	                    		System.out.println("Array sisaldab  " + ordArr.size());
	                    		for (int i = 0; i<ordArr.size(); i++) {
	                    			JSONObject order = (JSONObject) ordArr.get(i);
	                    			  SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//"2024-02-14T12:40:53+02:00"
	                    		        jsonDateFormat.setLenient(false); // Set lenient to false to enforce strict parsing
	                    		    	Date ordDate = null;
	                    		        try {
	                    				ordDate = jsonDateFormat.parse((String)order.get("created_at"));
	                    				} catch (ParseException e) {
	                    					
	                    					e.printStackTrace();
	                    				}

	                    		        String orderID = ((Long) order.get("id"))+"";
	                    		        String orderNum = ((Long) order.get("external_id"))+"";
	                    		        String orderSite = (String) order.get("app_name");
	                    		        
	                    		    System.out.println(ordDate+"  "+ orderSite);
	                    			
	                    			
	                    			
	                    			JSONObject shippingAddress = (JSONObject)order.get("shipping_address");
	                    			String city = (String) shippingAddress.get("city");
	                    			String address = (String) shippingAddress.get("address");
	                    			String phone = (String) shippingAddress.get("phone");
	                    			String post_code = (String) shippingAddress.get("post_code");
	                    			String receiver_first_name = (String) shippingAddress.get("receiver_first_name");
	                    			String receiver_last_name = (String) shippingAddress.get("receiver_last_name");
	                    			System.out.println(receiver_first_name + " " + receiver_last_name+ " " + orderSite);
	                    			String orderSiteCode = null;
	                    		
	                    			if (orderSite.equals("220.lv")) {
	                    				orderSiteCode = this.lati220Klient;
	                    				
	                    			}
	                    				
	                    			else if (orderSite.equals("kaup24.ee")) {
	                    				orderSiteCode = this.kaup24Klient;
	                    				
	                    			}
	                    			else if (orderSite.equals("pigu.lt")){
	                    				orderSiteCode = this.piguLTKlient;
	                    				
	                    			}
	                    			Order ord = new Order(orderSiteCode, "WebiPood");
	                    			ord.setDokNr(orderNum);
	                    			ord.setStatus(1000);
	                    			ord.setShipCal(ordDate.getTime()+ "");
	                    			ord.setInfo(receiver_first_name + " " + receiver_last_name +" "+ orderNum);
	                    			Vector lines = new Vector();
	                    		
	                    			
	                    			
	                    			JSONArray ordProd = (JSONArray) order.get("products");
	                    			for (int e = 0; e<ordProd.size(); e++) {
	                    				JSONObject productLine = (JSONObject)  ordProd.get(e);
	                    				JSONObject modification = (JSONObject)productLine.get("modification");
	                    				
	                    				String prodName = (String) modification.get("title");
	                    				String prodEan = (String) modification.get("ean");
	                    				
	                    				Double prodPriceVAT = (Double) productLine.get("price");
	                    				Double prodPrice = Double.parseDouble((String) productLine.get("price_without_vat"));
	                    				
	                    				Long prodAmnt = (Long) productLine.get("amount");
	                    				
	                    			System.out.println(orderSiteCode + " " + ((Product)eanProds.get(prodEan)).getID()+"  "+ prodEan + "  " + prodPrice + "  " + prodAmnt);
	                    			
	                    				OrderLine ol = new OrderLine();
	                    				ol.setKogus(prodAmnt);
	                    				ol.setEAN(prodEan);
	                    				ol.setTooteNimi(prodName);
	                    				ol.setPrice(prodPrice);
	                    				//ol.setDiscount(10);
	                    				
	                    				if(eanProds.get(prodEan) != null) {
	                    					ol.setToode(((Product)eanProds.get(prodEan)).getID() + "");
	                    				}
	                    				//System.out.println(((Product)eanProds.get(prodEan)).getID()+"  "+ prodEan + "  " + prodPrice + "  " + prodAmnt);
	                    			lines.add(ol);
	                    			
	                    				
	                  
	                    			}
	                    			ord.setOrderLines(lines);
	                    			if (ord.getInfo().contains("Ketlin Muttel"))
	                    			orders.add(ord);


	                        
	                    }
	                }
	            }
	        } else {
	            System.out.println("Directory does not exist or is not a directory: " + directoryPath);
	        }

		
		}
		
		
		
		/*



	     */
		
		
		
		return orders;
	}
	
		

	

}
