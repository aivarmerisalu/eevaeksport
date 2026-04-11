package eevaeksport.imp.driver;




import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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

public class BC365ValdoImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public BC365ValdoImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
  
    if("true".equals( parent.getProps().getProperty("testing"))){
    	testing = true;
    }
  }
  
  
  

  /* =========================
     CORE NTLM SOAP CALL
     ========================= */

  public String callOperation(String operation, String json) throws Exception {

      String serviceUrl = parent.getProps().getProperty("api.SERVICE_URL");
      String namespace  = parent.getProps().getProperty("api.NAMESPACE");
      String domain     = parent.getProps().getProperty("api.DOMAIN");
      String username   = parent.getProps().getProperty("api.USERNAME");
      String password   = parent.getProps().getProperty("api.PASSWORD");

      URL url = new URL(serviceUrl);

      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
              new AuthScope(url.getHost(), url.getPort()),
              new NTCredentials(username, password, null, domain)
      );

      try (CloseableHttpClient client = HttpClients.custom()
              .setDefaultCredentialsProvider(credsProvider)
              .build()) {

          HttpPost post = new HttpPost(serviceUrl);
          post.setHeader("Content-Type", "text/xml; charset=utf-8");
          post.setHeader("SOAPAction", namespace + ":" + operation);

          String soapEnvelope = buildSoapEnvelope(namespace, operation, json);
          System.out.println("soapEnvelope " +soapEnvelope + " \n" + serviceUrl);
          post.setEntity(new StringEntity(soapEnvelope, StandardCharsets.UTF_8));


try (CloseableHttpResponse response = client.execute(post)) {
    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
}
      }
  }

  
  private String buildSoapEnvelope(String namespace, String operation, String json) {
	    return 
	        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
	        "xmlns:mer=\"" + namespace + "\">" +
	        "<soapenv:Header/>" +
	        "<soapenv:Body>" +
	        "<mer:ImportOrder>" +
	        "<mer:jsonRequest>" + json + "</mer:jsonRequest>" +
	        "</mer:ImportOrder>" +
	        "</soapenv:Body>" +
	        "</soapenv:Envelope>";
	}


  private String extractReturnValue(InputStream xmlStream) throws Exception {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(xmlStream);

      NodeList nodes = document.getElementsByTagName("return_value");

      if (nodes.getLength() == 0) {
          throw new RuntimeException("SOAP response does not contain <return_value>");
      }

      StringBuilder sb = new StringBuilder();
      NodeList children = nodes.item(0).getChildNodes();

      for (int i = 0; i < children.getLength(); i++) {
          if (children.item(i).getNodeValue() != null) {
              sb.append(children.item(i).getNodeValue());
          }
      }
      return sb.toString().trim();
  }

  


  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  
  
  public String identifyCustomer(String id,String regnr)  {

    String acc = null;
    return acc;

  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	
  return prodID;
  
}
  



  
  public void makeOrder(Order order) throws SQLException{

     makeOutput(order); 
     
     
    
  }
  

  public static  String escapeXML(String s){
	    if (s == null) return s;
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0 ; i < s.length() ; i++){
	      char c = s.charAt(i);
	      if (c == '&'){
	        sb.append("&amp;");
	      } else if (c == '<'){
	        sb.append("&lt;");
	      } else if (c == '>'){
	        sb.append("&gt;");
	      } else if (c == '\n'){
	        sb.append(" ");
	      } else {
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }
  
  
  private void makeOutput(Order order){
	
	  //JSONArray orders = new JSONArray();
	  JSONObject jOrder = new JSONObject();
	 // orders.add(jOrder);
	  
	  JSONObject jHeader = new JSONObject();
	
	 	
	    
	      try {
	    
	        String retemail = null; 

              double kordaja = 1;

	        boolean tagastus = false;
	   	 	 Vector lin = order.getOrderLines();
	   	 	 HashSet signas = new HashSet();
	   	 	 int nega = 0;
	   	 	 for (int i = 0; i < lin.size(); i++) {
	   	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	   	 		if(!signas.contains(ol.getToode() + " " + ol.getKogus())) {
	   	 			signas.add(ol.getToode() + " " + ol.getKogus());
	   	 		}
	   	 		if(ol.getKogus() < 0 && (order.getStatus() == 5 || order.getStatus() == 4 || order.getStatus() == 2)){
	   	 			nega++;
	   	 		}
	   	 		
	   	 	 if(ol.getPartii() != null && ol.getPartii().trim().length() >0) {
		    	  if(ol.getPartii().contains("++")) {
		    		  retemail = ol.getPartii().substring(ol.getPartii().indexOf("++")+2);
		    	  }
		     }
	   	 	 }  
	   	
	   	 	
	   	 	 
	   	 	 if(nega == lin.size()){
	   		    	tagastus = true;
	   		    	kordaja =-1;
	   		    }
	   	 	
	   	 	 
	   	 	  boolean repr = false;
	   	 	 	 for (int i = 0; i < lin.size(); i++) {
	   	 	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	   	 	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0.1 && (order.getStatus() == 5 || order.getStatus() == 4)){
	   	 	 			repr = true;
	   	 	 			tagastus = false;
	   	 	 		    kordaja =-1;
	   	 	 		}
	   	 
	   	 	 		
	   	 	 	 } 
	        
	 
	     
	        
		

	        String number = order.getDokNr();
	        if(number == null || number.length() == 0){
	        	number = (Calendar.getInstance().getTimeInMillis()+"").substring(3);
	        }
	           
     long uni = Calendar.getInstance().getTimeInMillis();
 
	       
	        if(number.length() > 20) {
	        	number = number.substring(0,20);
	        }
	        
	         jHeader.put("OrderNumber", number);
	   	     jHeader.put("SalesAgent", order.getAgent());
	  	     jHeader.put("CustomerDeliveryCode", order.getCompany());
	  	
	 
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			 jHeader.put("Comment", order.getInfo());
	 		}
	 	//	System.out.println("Comment " + comm + " " + order.getTeostaja() + " " + order.getShipMode());
	        
	 		if(order.getTeostaja() != null && order.getTeostaja().trim().length() >0 &&
	 				!order.getTeostaja().trim().startsWith("VLD")){
                 	String num = order.getTeostaja();
	 			 if(num.length() > 20) {
	 	        	num = num.substring(0,20);
	 	        }
	 		    jHeader.put("CustomerOrderNumber", num);
	 		}

	 		
	 		if(order.getShipMode() != null && order.getShipMode().trim().length() >0){

	 			String sm = order.getShipMode();
	 			if(sm.indexOf("Pieg") != -1) {
	 				sm = "Piegade";
	 			}
	 			 jHeader.put("ShipMode", sm);
		   
	 		}
	 
	       
	        Calendar cal = Calendar.getInstance();
	        String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
			  String year = cal.get(Calendar.YEAR) + "";
			  if(year.length() == 2){
				  year = "20" + year;
			  }
	      	  jHeader.put("OrderDate", year +"-"+ month +"-"+ day);
		  	
   
	        
	        Calendar delcal = Calendar.getInstance();
	        delcal.setTime(order.getShipCal().getTime());

	         month = (delcal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	         day = delcal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;
			   year = delcal.get(Calendar.YEAR) + "";
			  if(year.length() == 2){
				  year = "20" + year;
			  }
			  jHeader.put("ExpectedDeliveryDate", year +"-"+ month +"-"+ day);
	        		        

	        boolean invoice = false;
	          
	        System.out.println("Seisund " + repr + " " + tagastus );
	        
		     String func = "ORDER";
		      if(repr) {
		    	  func = "REPRICE";
		      }
		      
		      if(tagastus) {
		    	  func = "RETURN";
		      }
		        
	        if(retemail != null) {
	        	  jHeader.put("RepriceEmail", retemail);
	        }
	        
	    	 
		  	  jHeader.put("DocumentType", func);
		  	
		  	  jOrder.put("Header", jHeader);
	 		
	        
		  	  JSONArray orderLines = new JSONArray();
		  	  jOrder.put("Orderlines", orderLines);
		  	  
		  	  
		  	
	       

	        int coun = 0;
            double kog = 0;
            double sum = 0;

            Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	          	
	        	  kog = kog + line.getKogus();
	        	  sum = sum + (line.getKogus()* line.getPrice());
	        	     double kogus = line.getKogus() * kordaja;
	        	     double price = line.getPrice();
	        	     double disc = line.getDiscount();
	        	     
     	   
	        	  if(signas.contains(toode+ " " + line.getKogus())) {
	        		  coun++;
	        		  
	        		  JSONObject jsonline = new JSONObject();
	        		  jsonline.put("orderline", coun);
	        		  jsonline.put("ProductCode", toode);
	        		  if(!repr && !tagastus) {
	        		  jsonline.put("OrderedAmount", line.getKogus());
	        		  }
	        		  jsonline.put("UnitCode", line.getUnit());
	        		  
	        		  
	        		   if(disc > 0  && !repr){
	        			   jsonline.put("PriceGiven", this.nf.format(price));
	  	        	  
	  	        		     String par = line.getPartii();
	  	        		     if(par != null && par.trim().length() >0) {
	  	        		    	    if(par.contains("++")) {
	  	        			    		  par = par.substring(0,par.indexOf("++"));
	  	        			    	  }
	  	        		    	  jsonline.put("OriginalDocNumber", par);
	  	        		     }
	  	        	    }

		        	    
	  	        	 
		        	    if(repr){
		        	    	 jsonline.put("Qty", kogus);
		        	    	 jsonline.put("PriceOld", price);
		        	    	 jsonline.put("PriceNew", (price * (100-disc)/100));
		        	    	 String par = line.getPartii();
	  	        		     if(par != null && par.trim().length() >0) {
	  	        		    	    if(par.contains("++")) {
	  	        			    		  par = par.substring(0,par.indexOf("++"));
	  	        			    	  }
	  	        		    	  jsonline.put("OriginalDocNumber", par);
	  	        		     } 
		        	        double compsum = (price - (price * (100-disc)/100)) * kogus  ;
		        		         jsonline.put("CompensationSum", this.nf.format(compsum));
		        		         
		        		    }else if(disc > 0 ){
		        						   jsonline.put("Discount", this.nf.format(disc));
		        		    	     }
		        				     orderLines.add(jsonline);
	        		  
	        		  addNumLine(); 
	        	  }
	          	  signas.remove(toode+ " " + line.getKogus()); // topelt ridade vastu tehtud ...
	          }
	          }


	        
       String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + "-" + (uni + "").trim() +  ".json";
	      System.out.println(fileName);

		     Converter.saveFile(fileName,jOrder.toJSONString()); 
		     
	   String resp =   callOperation("ImportOrder", jOrder.toJSONString());
	     fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + "-" + (uni + "").trim() +  "response.json";
	     Converter.saveFile(fileName, resp); 
	   
	     
	     
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


   
   
   public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  


	   private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
	   

 
  public String getDriverVersion(){
    return "BC365ValdoImport";
  }
 
 
 
  public boolean isAmount(){
          return isAmount;
  }

  
  
  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }


  
  private ImportPanel parent;
  private boolean isAmount = false;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	return troubles.toString();
  }
  
}
