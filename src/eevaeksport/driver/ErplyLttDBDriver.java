package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.methods.HttpGet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
import eevaeksport.util.FileListFilter;
import eevaeksport.util.ReportingService;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ErplyLttDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public ErplyLttDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
   String url = getParent().getProps().getProperty("erply.url");
   ownGLN = getParent().getProps().getProperty("owngln");
   if(url == null){
	   url = "https://www.erply.net/api/";
   }
	try {
		myurl = new URL(url);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
	
   username = getParent().getProps().getProperty("erply.user");	
   company = getParent().getProps().getProperty("erply.company");
   password = getParent().getProps().getProperty("erply.pass");
   apiversion = getParent().getProps().getProperty("erply.apiversion");
   if(getParent().getProps().getProperty("erply.onpage") != null){
	   try{
		   onpage = Integer.parseInt(getParent().getProps().getProperty("erply.onpage"));
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	  
   }
   if(apiversion == null){
	   apiversion = "1.0";
   }
   returntype = getParent().getProps().getProperty("erply.returntype");
   if(returntype == null){
	   returntype = "XML";
   }
    passString =  userstuff = "&version=" + apiversion + "&responseType=" + returntype + "&username=" + username 
      + "&password=" + password;
   
   session = getSessionId(passString);
   try {
		myurlcdn = new URL("https://cdn.erply.com/session");
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
   sessioncdn = getSessionIdCDN("jwt=" +jwttoken);
   
   try {
		// getImagesCDN(sessioncdn);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	while(session.indexOf("%") != -1){
	   session = getSessionId(passString);
   }
    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
    System.out.println(session + userstuff ) ;
   
    
    if(getParent().getProps().getProperty("laoid") != null){        	   
    	   laoID = getParent().getProps().getProperty("laoid");
       }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
    
  public String getSaveDebtInfo() {
	   
           	   int mm = 1;
           	   if(getParent().getProps().getProperty("months") != null){        
           		    mm = Integer.parseInt(getParent().getProps().getProperty("months"));
           	   }
           	   
              Calendar dds = Calendar.getInstance();
              dds.add(Calendar.MONTH, - mm);
              
              Calendar dde = Calendar.getInstance();
              
              
              System.out.println("kliendisortimendid alustan"  + mm + " kuud" +  Converter.getYYYYmMMmDDmString(dds.getTime()) +"  "+  Converter.getYYYYmMMmDDmString(dde.getTime()));
               
              StringBuffer inputLineBuf = null;
        
              try {
          		inputLineBuf = postData("request=getReportSalesRows" + "&dateStart="+ Converter.getYYYYmMMmDDmString(dds.getTime())
   				+"&dateEnd=" + Converter.getYYYYmMMmDDmString(dde.getTime()) + userstuff);
          	
          		
          	} catch (Exception e1) {
          		e1.printStackTrace();
          	}
                 if(inputLineBuf.length() > 2 && inputLineBuf.indexOf("<errorCode>") == -1 ){          
                       try {
                     	  File f = new File (getDir() + "klienditoodeErp.xml");
                     	  if(f.exists())f.delete();
                     	  Converter.saveFileInUTF8(getDir() + "klienditoodeErp.xml", Converter.deCode(inputLineBuf.toString()));
                 	 }catch (Exception ex) {
                 		ex.printStackTrace();
                 	  }
                  }
              
              System.out.println("kliendisortimendid tehtud" );
              
              
             System.exit(0);
   		
	  
   	return null;
   }
  
  public static String getYYYYpMMpDDpString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day );
		
		sb.append("." + month  + ".");
		sb.append("" + cal.get(Calendar.YEAR));
		
	    return sb.toString();
	  }







  private String getSessionIdCDN(String asi ){
	  
	  try {
	String res = (postDataCDN(asi)).toString();
//	System.out.println("res " + res);
	JSONParser parser = new JSONParser();  
	JSONObject json = (JSONObject) parser.parse(res);  
	sessioncdn = (String)json.get("sessionKey");
	
	} catch (Exception e) {
		e.printStackTrace();
	}
	//  System.out.println("res " + sessioncdn);
	  return sessioncdn;
  }
  
  
String jwttoken = null;
  
  private String getSessionId(String asi ){

	  String data = "clientCode=" + company + "&request=verifyUser" + asi ;
	//  System.out.println(data);
	  
	  try {
	String res = (postData(data)).toString();
//	System.out.println("res " + res);
	session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));
	jwttoken = res.substring(res.indexOf("<token") + 7 ,res.indexOf("</token"));
	//System.out.println("res " + jwttoken);
	} catch (Exception e) {
		e.printStackTrace();
	}
	 
	  return session;
  }
  
  
  
  private StringBuffer postData(String data) throws Exception {
  HttpURLConnection urlc = null;

 	Converter.doTrustToCertificates();
  StringBuffer inputLineBuf = new StringBuffer();
  try{
  urlc = (HttpURLConnection) myurl.openConnection();
  try{
  urlc.setRequestMethod( "POST" );
  } catch (ProtocolException e) {
    throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
  }
  urlc.setDoOutput(true);
  urlc.setDoInput(true);
  urlc.setUseCaches(false);
  urlc.setAllowUserInteraction(false);
  OutputStream out = urlc.getOutputStream();
  try {
	out.write( data.getBytes() );
  } catch (IOException e){
  throw new Exception("IOException while posting data", e);
  } finally {
  if (out != null)
  out.close();
  }
  InputStream in = urlc.getInputStream();
  try{
  Reader reader = new InputStreamReader(in);
    InputStreamReader isr = new InputStreamReader(in,"UTF-8");
    BufferedReader inr = new BufferedReader(isr);
    String inputLine;

    while ((inputLine = inr.readLine()) != null){
        inputLine = enCode(inputLine);	// enCode(inputLine);	
    	inputLineBuf.append(inputLine.replaceAll("/>","/>\n").replace((char)0x1c, ' ').replace((char)0x1b, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ')
    			.replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0xb, ' ').replace((char)0xe, ' ')
    			.replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
    			 .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
     			.replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ')
    			);
  }

     inr.close();
  
  
  reader.close();
  } catch (IOException e)
  {
  throw new Exception("IOException while reading response", e);
  } finally
  {
  if (in != null)
  in.close();
  }
  } catch (IOException e)
  {
  throw new Exception("Connection error (is server running at " + myurl + " ?): " + e);
  } finally
  {
  if (urlc != null)
  urlc.disconnect();
  }
  return inputLineBuf;
  }
  
  
  private StringBuffer postDataCDN(String data) throws Exception {
	  HttpURLConnection urlc = null;

	  
	/*  String data = "data=Hello+World!";
      URL url = new URL("http://localhost:8084/WebListenerServer/webListener");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.getOutputStream().write(data.getBytes("UTF-8"));
      con.getInputStream();
*/
	  
	 	Converter.doTrustToCertificates();
	  StringBuffer inputLineBuf = new StringBuffer();
	  try{
		//System.out.println("data " + data);
	  urlc = (HttpURLConnection) myurlcdn.openConnection();
	  try{
	  urlc.setRequestMethod( "POST" );
	  } catch (ProtocolException e) {
	    throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
	  }
	  urlc.setDoOutput(true);
	  urlc.setDoInput(true);
	  urlc.setUseCaches(false);
	  urlc.setAllowUserInteraction(false);
	  OutputStream out = urlc.getOutputStream();
	  try {
		out.write( data.getBytes() );
	  } catch (IOException e){
	  throw new Exception("IOException while posting data", e);
	  } finally {
	  if (out != null)
	  out.close();
	  }
	  
	  
	  InputStream in = urlc.getInputStream();
	  try{
	  Reader reader = new InputStreamReader(in);
	    InputStreamReader isr = new InputStreamReader(in,"UTF-8");
	    BufferedReader inr = new BufferedReader(isr);
	    String inputLine;

	    while ((inputLine = inr.readLine()) != null){
	        inputLine = enCode(inputLine);	// enCode(inputLine);	
	    	inputLineBuf.append(inputLine.replaceAll("/>","/>\n").replace((char)0x1c, ' ').replace((char)0x1b, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ')
	    			.replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0xb, ' ').replace((char)0xe, ' ')
	    			.replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
	    			 .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
	     			.replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
	     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ')
	    			);
	  }

	     inr.close();
	  
	  
	  reader.close();
	  
	  
	  } catch (IOException e) {
	  throw new Exception("IOException while reading response", e);
	  } finally { if (in != null)
	  in.close();
	  }
	  } catch (IOException e){
	  throw new Exception("Connection error (is server running at " + myurlcdn + " ?): " + e);
	  } finally {
	  if (urlc != null)
	  urlc.disconnect();
	  }
	  return inputLineBuf;
	  }
  
  private StringBuffer getData(String data) throws Exception {
     	String httpsURL = myurl + "?" + data;
        URL myurl1 = new URL(httpsURL);
        HttpsURLConnection con = (HttpsURLConnection)myurl1.openConnection();
        InputStream ins = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
        BufferedReader in = new BufferedReader(isr);

        String inputLine;
        StringBuffer inputLineBuf = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
        	inputLine = this.enCode(inputLine);
        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
       }
        return inputLineBuf;
	  }
	  
	  
  private HashMap getImagesCDN(String apikey) throws Exception {
	  HashMap pildid = new HashMap();
	  
   	String httpsURL = "https://cdn.erply.com/images?page=1";
      URL myurl1 = new URL(httpsURL);
      
      HttpsURLConnection con = (HttpsURLConnection)myurl1.openConnection();
       con.setRequestProperty("API_KEY", apikey);
      InputStream ins = con.getInputStream();
      InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
      BufferedReader in = new BufferedReader(isr);

      String inputLine;
      int totalRec = 0;
     
      while ((inputLine = in.readLine()) != null){
    	 
    	  JSONParser parser = new JSONParser();  
    		JSONObject json = (JSONObject) parser.parse(inputLine); 
    		JSONArray arr =(JSONArray)json.get("images");
    		System.out.println(inputLine);
    		for(int i = 0; i < arr.size(); i++) {
    			JSONObject aa = (JSONObject)arr.get(i);
    			
    			// System.out.println("aa.get(\"productId\")" + aa.get("productId")+ " "+ aa.get("key") + " " + aa.get("size"));
    			 String toode = (aa.get("productId"))+"";
	    			pildid.put(toode, aa.get("key"));
    			
    		}
    		totalRec = Integer.parseInt( ((Long) json.get("totalRecords")) +"");
     }
      in.close();
      isr.close();
      con.disconnect();
      
      int pages = (int) (totalRec/100);
      
   
      for(int w = 2; w < pages + 2; w++) {
    		 httpsURL = "https://cdn.erply.com/images?page=" + (w);
    	       myurl1 = new URL(httpsURL);
    	      
    	     con = (HttpsURLConnection)myurl1.openConnection();
    	       con.setRequestProperty("API_KEY", apikey);
    	      ins = con.getInputStream();
    	      isr = new InputStreamReader(ins,"UTF-8");
    	      in = new BufferedReader(isr);

    	     
    	      while ((inputLine = in.readLine()) != null){
    	    	//  System.out.println(inputLine);
    	    	  JSONParser parser = new JSONParser();  
    	    		JSONObject json = (JSONObject) parser.parse(inputLine); 
    	    		JSONArray arr =(JSONArray)json.get("images");
    	    		
    	    		for(int i = 0; i < arr.size(); i++) {
    	    			JSONObject aa = (JSONObject)arr.get(i);
    	    			String toode = (aa.get("productId"))+"";
    	    			pildid.put(toode, aa.get("key"));
    	    			
    	    			
    	    		}
    	    		
    	     }
    	      in.close();
    	      isr.close();
    	      con.disconnect();
      }
      
      System.out.println("suurus " + pildid.size());
    //  System.exit(0);
      return pildid;
	  }
	  
  
  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
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
	
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	
	   
	  	try {
	  		
	  		 FileListFilter fil = new FileListFilter("prlisterp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int ee = 0; ee < ll.length; ee++) {
	    		
	    		
	    		
	    		 String name = ll[ee];
	    		// System.out.println(name);
	 		  File f = new File (getDir() + name);
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	      	
	        Node root = getNode(doc, "document");      
	         // System.out.println(root);
	       	Node read = getNode(root, "records");
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("item")){
	          	  try{
	          		 String clGroupName  = getNode(rida,"name").getFirstChild().getNodeValue();
	          		 
	          		 boolean camp = false;
	          		 boolean over = false;
	          		 if("true".equals(getParent().getProps().getProperty("erply.parenthis"))){
	          			 if(clGroupName.contains("(") && clGroupName.contains(")")) {
		          			 camp = true;
		          		 }	 
	          		 }
	          		
	               		 String clGroup  = getNode(rida,"pricelistID").getFirstChild().getNodeValue();
	               		String startDate  = getNode(rida,"startDate").getFirstChild().getNodeValue();
	               		String endDate  = getNode(rida,"endDate").getFirstChild().getNodeValue();
	              
	               	   // System.out.println("out " + clGroupName + " " + startDate + " " + endDate);
	               			Node read2 = getNode(rida, "pricelistRules");
	               	    	if(read2 != null){
	               	    	
	               			for (int u = 0; u < read2.getChildNodes().getLength(); u++){
	               			 Node rida2 = read2.getChildNodes().item(u);
	               			
	               		    if(rida2 != null && rida2.getNodeName() != null && rida2.getNodeName().equals("item")){
	               		    		
	               		    	//System.out.println(group + " " + getNode(rida2,"id").getFirstChild().getNodeValue());
	               		    	//	System.out.println(group + " " + getNode(rida2,"price").getFirstChild().getNodeValue());
	               			 
	             			MatrixLine mline = new MatrixLine();
	            			if (mline.getCustID() == null && clGroup != null
	            					&& clGroup.trim().length() > 0) {
	            				mline.setCliGroup(clGroup.trim());
	            			}
	            			
                           String prodID = getNode(rida2,"id").getFirstChild().getNodeValue();
	            			if (prodID != null && prodID.trim().length() > 0) {
	            				mline.setProdID(prodID.trim());
	            			}
	            			
	            			
	            			try{
		            			if(!startDate.equals("0000-00-00") && !endDate.equals("0000-00-00")){
		            		 	    Date hetk = new Date();
		    	               		Date alg = getDate(startDate);
		    	               	    Date lopp = getDate(endDate);
		    	               	    if(lopp.after(hetk) && alg.before(hetk)){
		    	               	    	mline.setStartDate(startDate);
		    	               	    	mline.setEndDate(endDate);
		    	               	    }else {
		    	               	    	over = true;
		    	               	    }
		    	               	    
		            			}
		                		
		            			}catch(Exception k ){
		            				k.printStackTrace();
		            			}
		            			if (camp) {
		            				mline.setStartDate(startDate);
	    	               	    //	mline.setEndDate(endDate);
		            			}
	            			
	            			
	            			try{
	            				 double price = Double.parseDouble(getNode(rida2,"price").getFirstChild().getNodeValue());
	 	            			if (price != 0) {
	 	            				mline.setPrice(price);
	 	            			}
	 	            			if(!over) {
	 	            				lines.add(mline);
	 	            			}
	 	            			
	 	               		    		
	            			}catch(Exception k ){
	            				
	            			}
	            			
	            			
	            			
	               		    }
	               			}
	               			
	               	    	}
	          	  }catch(Exception r){
	    	  		r.printStackTrace();
	    	  	  }
	          	  }
	           	  }
	           
	    	 } 	
	    	 }catch(Exception e){
	    	  		e.printStackTrace();
	    	  	} 

      return lines;

   }

   private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection("jdbc:odbc:report", "mobile", "mobile");
	  }
   
   
   
   private TreeMap getGrupid(){
	      File fff = new File(this.getExcelDir() + "Sortiment.xls" );
	      File fff3 = new File(this.getExcelDir() + "Sortiment.xlsx" );
	      TreeMap groups = new TreeMap(); 
	  	try{
	  		
	  		
	  		
	  		
			Workbook workbook = null;
			Sheet sheet = null;
			if(fff.exists()) {
				FileInputStream ff = new FileInputStream(fff);
				 workbook = new HSSFWorkbook((ff));
				 sheet = (HSSFSheet) workbook.getSheetAt(1);	
			}else {
				FileInputStream ff = new FileInputStream(fff3);
				 workbook = new XSSFWorkbook((ff));
				 sheet = (XSSFSheet) workbook.getSheetAt(1);	
			}
			
			
			
			Row row = null;
	  		
	  		row = sheet.getRow(0);
	  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	  		
	  		//System.out.println("rownum " + row.getLastCellNum());
	  		
	  		
	  		for(int a = 1 ; a < sheet.getLastRowNum();a++){
	  			try{
	  			//	Loeme sisse rea
	  				row = sheet.getRow(a);
	  				if (row.getCell((short) 0) != null) {
	  				


	  	    			   String kood = null;
	  	    			  if(row.getCell((short)0) != null){
		    	    			  	try{
		    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(kood.indexOf(".")!= -1){
		    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
		    	    			  	 }
		    	    			  	 String sort = null;
		    	    			  	try{
		    	    			  		sort = row.getCell((short)1).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
			    	    	    		} 
			    	    			  	 if(sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
			    	    			  	 }
			    	    			  	 sort = sort.replace(',', '|').replace(',', '|').replace(',', '|').replace(',', '|');
		    	    			  	 groups.put(kood,Converter.cleanLitLatEst(sort));
	  	    			  
	  	    				
	  				}	   
	  				}
	 			}catch (Exception e){
	  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
	  			}
	  		}
	  		
	  		
	  
	  	 }catch (Exception e){
	  		//e.printStackTrace();
	  	}
	  	 
	  	 System.out.println("fix grupid suurus " + groups.size() + " " + groups.toString() );
	      
	     return groups; 
	  	 
	  }

   
   
   public void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
	   useOldData = true;
	  	  boolean oosmarge = true;
	  	  if("true".equals(getParent().getProps().getProperty("distribution.insmarking"))){
	  		oosmarge = false; 
	  	  }
	  	  
	  	  
	  	  
	  	  
	  	 //=10379733,10379733
	  	  //distribution.email.10379733=aivar@mobilejava.ee
	  	  
	    	getProducts(null);
	    	getCustomers(null);
	    	 
	    	  //1.Fikseeritud sortimendi salvestamine baasi juhul kui tava andmetega pole tehtud 
	    	 this.getFixedAssortmentData();
	    	   
	  	  String saajad = getParent().getProps().getProperty("distribution.reporters");
	  	  
	  	  StringTokenizer sto = new StringTokenizer(saajad,",");
	  	  while (sto.hasMoreElements()){
	  		
	  	  String add = "";
	  	  String regno = sto.nextToken();
	  	  String toThem = getParent().getProps().getProperty("distribution.email." + regno);
	  	  
	  	  if(ccc != null){
	  		  Calendar cal = Calendar.getInstance();
	  		  long diff = cal.getTimeInMillis() -ccc.getTimeInMillis();
	  		  cal.setTimeInMillis(diff);
	  		  add = "-" + cal.get(Calendar.DAY_OF_YEAR);
	  	  }
	  	  //2. t�naste lugemiste 
	  	  // M��rame t�nased kehtivad sortimendid nii suurused kui ka sisu
	  	  
	  	   String sql1 = "select sortgroup, count(prodid) as number from mj_assortment where  " +
	         "  year(sortdate) =  year (getdate()" + add + ") " +
	           "   and month(sortdate) =  month (getdate()" + add + ")" + 
	             "		and day(sortdate) =  day (getdate()" + add + ")  group by sortgroup";
	  	   
	  	   String sql2 = "select sortgroup ,prodid  from mj_assortment " +
	  	   	"	where year(sortdate) =  year (getdate()" + add + " " +
	        "  ) and month(sortdate) =  month (getdate()" + add + ") " + 
			"  and day(sortdate) =  day (getdate()" + add + ")  group by sortgroup,prodid";  // t�nase sortimendi leidmine
	  	 
	  	   
	  	   HashMap sortLen = new HashMap();
	  	   HashMap sortIse = new HashMap();
	  	   HashMap tnimed = new HashMap();
	  	   
	  	   try{
	  		   Connection con1 = this.getNewConnection();
	      	   Statement st = con1.createStatement();
	      	   ResultSet rs = st.executeQuery(sql1);
	      	   
	      	   while(rs.next()){
	      		   sortLen.put(Converter.cleanLitLatEst(rs.getString("sortgroup").trim()),rs.getString("number").trim());
	      	   }
	      	   
	            rs = st.executeQuery(sql2);
	      	   
	      	   while(rs.next()){
	      		   Vector sort = new Vector();
	      		   String sortim = Converter.cleanLitLatEst(rs.getString("sortgroup").trim());
	      		   String prodid = rs.getString("prodid").trim();
	      		   Product pr = ((Product) prodHash.get(prodid));
	      		   
	      		 //  System.out.println("toode" + prodid + "+" + pr);
	      		   if(pr == null){
	      			   pr = new Product(prodid);
	      			   pr.setEAN(prodid);
	      			   pr.setName(prodid + " nimipuudub");
	      		   }
	      		   String ean = pr.getEAN();
	      		   if(pr.getEAN() == null)ean = "";
	      		   String prname = pr.getName();
	      		   if(prname == null) prname = "Tundmatu toode";
	      		   String prodname = ean + " " + prname;
	      		   tnimed.put(prodid, prodname);	
	      		   if(sortIse.get(sortim) != null){
	      			   sort = (Vector)sortIse.get(sortim);
	      			   sort.add(prodid);
	      		   }else{
	      			   sort.add(prodid);
	      			   sortIse.put(sortim,sort);  
	      		   }
	      	   }
	      	   
	      int counter = 0;	   
	      	   //L�pes sortimentide sisselugemine
	      	   
	      	   // vaja leida mitu toodet oli m�rgitud kontrolli ajal kohale 
	      	   //ja ,m��rame kes k�is mis kaupluses ja kellaaeg
	      	   
	      Calendar ccw = Calendar.getInstance();
	      ccw.add(Calendar.DAY_OF_MONTH, -7);
	String sql3 = "select prodid ,sup.custid,sup.custname, sup.sentdate as date," +
	     	" rtrim(sup.agentid) as agid, rtrim(sup.agentname) as agent ,sup.resstr3 as time ,resint3 as oosins  " +
	      " from supervisor sup  " +
	      " where   sup.sentdate > getdate()-7 and resint3 = 888 " +// mis on myygil sup.resstr3 >'" + ccw.getTimeInMillis() + "'" + //
	      		"order by sup.sentdate desc";
	      	   HashMap visited = new HashMap();
	      	   HashMap names = new HashMap();
	      	   HashMap agen = new HashMap();
	      	   HashMap sorti = new HashMap();
	      	   HashMap times = new HashMap();
	      	   HashMap visitedcoun = new HashMap();
	      	   Vector days = new Vector();
	             HashMap puuduvad = new HashMap(); 
	             rs = st.executeQuery(sql3);
	             
	           Vector kliendid = (Vector)ketid.get(regno);
	          // System.out.println("kliendid " + kliendid);

	while(rs.next()){
		   Vector sort = new Vector();
		   String prodid = rs.getString("prodid").trim();
		
		   String custid = rs.getString("custid").trim();
		   String sortim = (String)sortimendid.get(custid);
		   String custname = Converter.deCode(rs.getString("custname").trim());
		   String date = rs.getString("date").trim().substring(0,10);
		   String agid =  rs.getString("agid").trim();
		   String agent = Converter.deCode(rs.getString("agent").trim());
		   String time = rs.getString("time");
		  // System.out.println("ahaa " + custid );
		 if(kliendid.contains(custid)){
			
		   if(time != null && time.trim().length() != 0){
			   long lll = Long.parseLong(time.trim());
			   Calendar cc = Calendar.getInstance();
			   cc.setTimeInMillis(lll);
			   time = getYYYYpMMpDDpTimeNiceString(cc.getTime());
			   date = time.substring(0,10);
			   if(!days.contains(date)){
					  days.addElement(date);
				  }
		   }else{
			   if(!days.contains(date)){
					  days.addElement(date);
				  }
			   time = date + " 00:00:00";
		   }
		   	
		   agen.put(custid, agid + " " + agent);
		   names.put(custid, custname);
		   sorti.put(custid, sortim);
		   times.put(custid+date, time);

		   if(visitedcoun.get(custid) != null){
			   Vector dddd = (Vector)visitedcoun.get(custid);
			   if(!dddd.contains(date))dddd.add(date);
			   visitedcoun.put(custid,dddd);  
		   }else{
			   Vector dddd = new Vector();
			   dddd.addElement(date);
			   visitedcoun.put(custid,dddd);  
		   }	   

		//Loeme kokku mis olid pudu - m�rgivad puuduvaid tooteid    
		   if(sortIse.get(sortim) != null){
			  
				 Vector res = (Vector)sortIse.get(sortim);
				 if(oosmarge){
					 if(puuduvad.get(custid + date) == null){
						   counter++;
		            Vector puud = new Vector();
		                 puud.addElement(prodid);
						   puuduvad.put(custid + date,puud);    
					   }else{
						   Vector puud = (Vector) puuduvad.get(custid + date);
				           puud.addElement(prodid);
						   puuduvad.put(custid + date,puud);   
					   } 
				 }else{
					 
					   if(puuduvad.get(custid + date) == null){
						   counter++;
		              Vector puud = (Vector)res.clone();
		           //   System.out.println("Kloonin " + puud.size() + " " + custid );
		                   puud.removeElement(prodid);
						   puuduvad.put(custid + date,puud);    
					   }else{
						   Vector puud = (Vector) puuduvad.get(custid + date);
				           puud.removeElement(prodid);
						   puuduvad.put(custid + date,puud);   
					   }
				 }
			    
			  
		   }
		   if(visited.get(custid+date) != null){
			   sort = (Vector)visited.get(custid+date);
			   sort.add(prodid);
			   visited.put(custid+date,sort);  
		   }else{
			   sort.add(prodid);
			   visited.put(custid+date,sort);  
		   }	   
		   
		 }
		 } 


	StringBuffer sb = new StringBuffer();
	StringBuffer sbf = new StringBuffer();

	Iterator ite = names.keySet().iterator();
	int total = 0;
	double perc = 0;
	while (ite.hasNext()){

		String custid = (String)ite.next();
		
		String sortcode = (String)sorti.get(custid);
		sb.append("Klient :\t" + names.get(custid) +  "\n");
		sb.append("Kontrollis:\t" + agen.get(custid) +  "\n");
		
		sbf.append("Klient :," + names.get(custid) +  "\n");
		sbf.append("Kontrollis:," + agen.get(custid) +  "\n");
		
		
		Enumeration ed =  ((Vector)visitedcoun.get(custid)).elements();
		while(ed.hasMoreElements()){
			total++;
			String date = (String)ed.nextElement();
			sb.append("Aeg :\t\t" + times.get(custid + date).toString().substring(0,19) +  "\n");	
			sbf.append("Aeg :," + times.get(custid + date).toString().substring(0,19) +  "\n");	
		

			double sss = Double.parseDouble((String)sortLen.get(sortcode));
			
			double mmm = Double.parseDouble(((Vector)visited.get(custid+date)).size()+ "");
			
		//	System.out.println("OOS " + sss + " " + mmm);
			if(oosmarge){
				mmm = sss - mmm;
							
			}
		  // kui m�rgivad OOS siis on myygil vahe .
			       
			double nnn = mmm/sss*100d;	
			int per = (int)(nnn);
			perc = perc + nnn;	
			sb.append("Sortimendis " + (int)sss + " / M��gil " + (int)mmm + " / " + per + "% \n\n");
			sbf.append("Sortimendis, M��gil ,% \n");
			sbf.append((int)sss + "," + (int)mmm + "," + per + "% \n\n\n\n");
		}

		sb.append("\n\n");

	}


	//Sortimendi v�rdlus  puuduolevad tooted ja v�ljaspool olevad tooted


	StringBuffer lisa = new StringBuffer();
	TreeMap tootedPoed = new TreeMap();
	StringBuffer lisaf = new StringBuffer();
	//System.out.println("puuduvad " + puuduvad.keySet());
	Iterator itera = puuduvad.keySet().iterator();
	boolean first = true ;
	while (itera.hasNext()){
		String id = (String)itera.next();
	//	System.out.println(id);
		String paev = id.substring(id.length() - 10);
	   String custid = id.substring(0,id.length() - 10);
	   //System.out.println(custid + "ee" + paev);
	   if(first){
			lisa.append("Lisainfo Puuduste kohta\n");
			lisaf.append("Lisainfo Puuduste kohta\n");
			first = false;
		}
		Vector vvv = (Vector)puuduvad.get(id);
		lisa.append(names.get(custid) + " " + paev + "\nPUUDU: " + vvv.size() + " toodet\n");
		lisaf.append(names.get(custid) + "," + paev + "\nPUUDU:," + vvv.size() + "\n");
		Enumeration enu = vvv.elements();
		
		while(enu.hasMoreElements()){
			
			String prodid = (String) enu.nextElement();
			lisa.append("\t" + prodid + " " + tnimed.get(prodid) + "\n");	
			lisaf.append(prodid + "," + tnimed.get(prodid) + "\n");
			if(tootedPoed.get(prodid + " " +  tnimed.get(prodid)) != null){
				Vector kli= (Vector)tootedPoed.get(prodid + " " +  tnimed.get(prodid));
				kli.addElement(names.get(custid));
				tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);
			}else{
				Vector kli= new Vector();
				kli.addElement(names.get(custid));
				tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);	
			}
		}
		lisa.append("\n\n");
		lisaf.append("\n\n");
	}
		

	StringBuffer toodePood = new StringBuffer();

	Iterator iteri = tootedPoed.keySet().iterator();
	while (iteri.hasNext()){
		String toode = (String)iteri.next();
		toodePood.append(toode + "\n");
		
		Vector vvv = (Vector) 	tootedPoed.get(toode);
		toodePood.append("puudub " + vvv.size() + " poes\n");
		
	 Enumeration enumi = 	vvv.elements();
	 while (enumi.hasMoreElements()){
		 toodePood.append("\t"  + enumi.nextElement() + "\n");
	 }
	 toodePood.append("\n\n");
	}


	if(total == 0 ){
		total = 1;
	}
	String text = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
	+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";
	String textf = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
	+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";

//	System.out.println(text + "\n" + sb.toString() + lisa.toString()+ "\n" + toodePood );


	Calendar ctc = Calendar.getInstance();

	String filename = "dreports/" +"DR-Erply-" + getYYYYpMMpDDpTimeNiceString(ctc.getTime()).substring(0, 10) + ".csv";  

	   File f = new File(filename);
	   if (f.exists())f.delete();
	     try {
			PrintWriter os = new PrintWriter(new FileWriter(
					filename, true));
			os.write(textf + "\n" + sbf.toString() + "\n" + lisaf.toString());
			os.close();
		  }catch (IOException ex) {
			ex.printStackTrace();
		  }

	Email email = new Email(getParent().getString("datasent"));

	//email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
	email.setFrom(getParent().getProps().getProperty("from"));
	email.setSMTP(getParent().getProps().getProperty("smtphost"));
	email.setUser(getParent().getProps().getProperty("smtpuser"));
	email.setPass(getParent().getProps().getProperty("smtppass"));
	email.setPort(getParent().getProps().getProperty("smtpport"));
	email.setSubjectFilesTo("Distributsiooni aruanne - Ltt AS klient " + regno);
	email.setBody(Converter.deCode(text + "\n" + sb.toString() + "\n" + lisa.toString() + "\n"+ toodePood.toString()));
	email.addAttach(filename);
	//System.out.println("Saadan Emaili");
	//iseendale saatmine
	String toOwn = getParent().getProps().getProperty("distribution.ownemail");
	
if(sb.length() >10){
	email.setFilesTo(toOwn);
	email.send();
	email.setFilesTo(toThem);
	email.send();
}
	
	      	   
	  	   }catch(Exception e){
	  		   e.printStackTrace();
	  	   }
	  	   
	  	   
	  	  }
	  	  System.exit(0);
	    }

  
   private HashMap prodHash = new HashMap();
   
	    private String getYYYYpMMpDDpTimeNiceString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
		    
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("-" + month  + "-");
			sb.append(day +" ");
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		     sb.append("_");
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     sb.append("_");
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    return sb.toString();
		  }


   private Connection con2 = null;
   
   Hashtable prodCodes = null;
   
  
  
   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		
		if("true".equals(getParent().getProps().getProperty("usepricematrix.fixed"))){
			result = getPriceMatrixData();
		}else{
		
		File f = new File(this.getExcelDir() + "Sortiment.xls");
		File f2 = new File(this.getExcelDir() + "Sortiment.xlsx");
		
		 if( prodCodes == null){
			 this.getProducts(null);
		 }
		
		  boolean insertassort = true;
         
         try {
       	  con2 = this.getNewConnection();  
      	   String sql = "SELECT sortdate from mj_assortment where  year(sortdate) = year(getdate()) and" +
      	   		" month(sortdate) = month(getdate()) and day(sortdate) = day(getdate()) ";
       	 
       	    Connection cons = this.getNewConnection();
   	        Statement stmts = cons.createStatement();
   	        ResultSet rss = stmts.executeQuery(sql);
   	       
   	        if(rss.next()){
   	        	insertassort = false;
   	        }   
   	        
   	        rss.close();
   	        stmts.close();
   	        cons.close();
   	        
         }catch(Exception d){
      	  // d.printStackTrace();
         }
		
	//	System.out.println("asoortment juurde " + insertassort);
		
		
		try {
			
			Workbook workbook = null;
			Sheet sheet = null;
			if(f.exists()) {
				FileInputStream ff = new FileInputStream(f);
				 workbook = new HSSFWorkbook((ff));
				 sheet = (HSSFSheet) workbook.getSheetAt(0);	
			}else {
				FileInputStream ff = new FileInputStream(f2);
				 workbook = new XSSFWorkbook((ff));
				 sheet = (XSSFSheet) workbook.getSheetAt(0);	
			}
			
			
			
			Row row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(1);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			System.out.println(groups.toString());

			for (int i = 2; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						
						prodID = (row.getCell((short) 0).getStringCellValue());
					} catch (Exception e) {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					}
					
                   if(!prodID.equals("")){
                	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";
							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.trim().toLowerCase().equals("1")) {
                                  if(a > 10){
                                 //	 System.out.println( a + " " + prodID + " " +
             						//		 clGroup + " " + go); 
                                  }
								 

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									if(prodCodes != null){
										String prodX = (String)prodCodes.get(prodID.trim());
										if(prodX != null){
											mline.setProdID(prodX.trim());		
										}else{
											//System.out.println("mingi jama toodet pole " + prodID );
										}
										
										
									}
									
									
								}
								
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								  if(insertassort){
									  try{
										  insertSortLine(mline); 
									  }catch(Exception s){
										  
									  }
								         
		                           }
								result.add(mline);
							}
						}
					}
					   
                   }
				}
			}
			if(con2 != null)
			con2.close();
		} catch (Exception e) {
		//	e.printStackTrace();
		}
		
		}
		// System.exit(0);
   System.out.println("fixed size ddddd " + result.size());
		return result;

	}

   
   
   public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr ){
	     String sql = "INSERT into dbo.supervisor values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
	       + " '' ,"   //	receivedTime	D,
	       + "?,"   //normalpr	N,
	       + "?,"   // amoun	N,
	       + "?,"   // grouptotal	N,
	       + "?,"   // cattotal	N,
	       + "?,"   // pertotcat	I,
	       + "?,"   // percolcat	I,
	       + "?,"   // perpalcat	I,
	       + "?,"   // pershecat	I,
	       + "?,"   // perrescat	I,
	       + "?,"   // isaction	L,
	       + "?,"   // ispartner	L,
	       + "?,"   // isbrand	L,
	       + "?,"   // isgroup	L
	       + "?,"   // actionaeva	N,
	       + "?,"   // actionbeva N,
	       + "?,"   // actiontota	N,
	       + "?,"   // custeval	N,
	       + "?,"   // custcomme	C(200),
	       + "?,"   // comment	C(200),
	       + "?,"   // custagent	C(200),
	       + "?,"   // custagentname	C(200),
	       + "?,"   // mustbe	I,
	       + "?,"   // ideal	I,
	       + "?,"   // resstr1	C(200),
	       + "?,"   // resstr2	C(200),
	       + "?,"   // resstr3	C(200),
	       + "?,"   // resint1	I,
	       + "?,"   // resint2	I,
		      + "?)" ; //	resint3	L

	       try {
	       	System.out.println("jam");
	          this.connectionUp();
	          con2 = this.getNewConnection(); 
	          
	          PreparedStatement ps = con2.prepareStatement(sql);

	           
	           for (Iterator it = wr.getInfo().getLines().iterator(); it.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new  eevaeksport.report.SuperVisorLineWrapperMeri(it.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             String agName =  wr.getInfo().getAgentName();
	             if(agName == null){
	            	 agName = (String)this.agents.get(wr.getInfo().getAgentID());
	             }
	             ps.setString(2, agName);
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             ps.setDouble(7, lw.getLine().getNormalPrice()*100);
	             ps.setDouble(8, lw.getLine().getAmount()*100);
	             ps.setDouble(9, 0);
	             ps.setDouble(10, 0);
	             ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	             ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	             ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	             ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	             ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	             ps.setBoolean(16, lw.getLine().isAction());
	             ps.setBoolean(17, lw.getLine().isPartner());
	             ps.setBoolean(18, lw.getLine().isBrand());
	             ps.setBoolean(19, lw.getLine().isGroup());
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
	             ps.setInt(28, lw.getLine().getDealNr());
	             ps.setInt(29, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(30, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, lw.getLine().getResInt3());
	             ps.executeUpdate();
	             System.out.println("lopp real");
	           }

	           ps.close();
	           con2.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	       this.connectionDown();
	     }


   
   
   private void  insertSortLine(MatrixLine mline){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'')" ;
			  
			  	        
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     
				    // System.out.println(sql);
		  }catch(Exception e){
			 // e.printStackTrace();
		  }		  
	  }

   
   
   public Vector getFixedAssortmentData1() {
       Vector lines = new Vector();
       
       try {
   		  File f = new File (getDir() + "hinnakiri.xml");
   	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	     DocumentBuilder db;
   		db = dbf.newDocumentBuilder();      	
       	FileInputStream in = getCleanInputStream(f);
       	String handler = null;
           Document doc = db.parse(in,handler);
           Node root = getNode(doc, "transport");      
      
       	Node read = getNode(root, "priceformulas");
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  try{
           		  if(rida.getNodeName().equals("priceformula")){
           			 String custID = " ";
                     String clGroup = " ";
                     String prodID = " ";
                     String prodGroup = " ";
                     MatrixLine mline = new MatrixLine();
                    try{
                    	 clGroup = rida.getAttributes().getNamedItem("code").getNodeValue(); 
                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue(); 
                         if (custID != null && custID.trim().length() > 0) {
                           mline.setCustID(custID.trim());
                         }
                         if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                         }

                         if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                         }
                         
                         if (mline.getProdID() == null && prodGroup != null
                             && prodGroup.trim().length() > 0) {
                           mline.setProdGroup(prodGroup);
                         }

                        
                        	 
                        	if(!prodsInUse.contains(prodID.trim())){
                           	  this.prodsInUse.add(prodID.trim());	
                        	}
                        	 
                            lines.add(mline);	
                          
                    
                       
                    }catch(Exception e){
                    }
                    
                   }
                	
           	  }catch(Exception e){
           		  e.printStackTrace();
           	  }
                
           }
           
   	}catch(Exception e){
   		e.printStackTrace();
   	}
  //     System.out.println(prodsInUse.size() );
       
       return lines;
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	        Vector vec = new Vector();
	       // System.out.println("Prod22 " + products.size());
	        for (int i = 0 ; i < inVec.size() ; i++){
	          vec.addElement(inVec.elementAt(i));
	        }
	        Vector sorted = new Vector(vec.size());
	        while(vec.size() > 0){
	          MatrixLine smallest = null;
	          Product smProd = null;
	          for (Enumeration enumr = vec.elements(); enumr.hasMoreElements() ; ){
	        	  MatrixLine ol = (MatrixLine)enumr.nextElement();
	            Product prod = (Product)products.get(ol.getProdID());
           //   System.out.println("Prod " + prod + "  " + ol.getProdID());
	            if ( (smallest == null && smProd == null )
	            		|| ( prod.getName().compareTo(smProd.getName()) < 0 )) {
	              smallest = ol;
	              smProd = (Product)products.get(smallest.getProdID());
	            }
	          }
	          vec.removeElement(smallest);
	          sorted.addElement(smallest);
	        }
	        return sorted;
	      }
	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }


   public String getSaveStockInfo(){
		  

	          StringBuffer products = new StringBuffer();

	          
	          try {
	      		StringBuffer inputLineBuf = postData("request=getProductStock&warehouseID=" + laoID + userstuff); //&getStockInfo=1
	      		  	
	      		
	      	  if(inputLineBuf.length() > 2){
	              
	              try {
	            	  File f = new File (getDir() + "laoseis.xml");
	            	  if(f.exists())f.delete();
	            	   Writer os = new BufferedWriter(new OutputStreamWriter( 
	     			    new FileOutputStream(getDir() + "laoseis.xml"), "UTF-8"));
	     		        os.write(inputLineBuf.toString().replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' '));
	     		        os.close();
	            	  }catch (IOException ex) {
	        		ex.printStackTrace();
	        	  }
	             }
	         	} catch (Exception e1) {
	      		e1.printStackTrace();
	      	}
	          
	          try {

	        	  File f = new File (getDir() + "laoseis.xml");
	      	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	      	     DocumentBuilder db;
	      		db = dbf.newDocumentBuilder();      	
	          	FileInputStream in = getCleanInputStream(f);
	          	String handler = null;
	              Document doc = db.parse(in,handler);
	              Node root = getNode(doc, "document");      
	           //   System.out.println(root);
	          	Node read = getNode(root, "records");
	         // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	              for (int i = 0; i < read.getChildNodes().getLength(); i++){
	              	  Node rida = read.getChildNodes().item(i);
	              	  if(rida.getNodeName().equals("item")){
	              	  

	                try{
	            
	              String kood  =  getNode(rida, "productID").getFirstChild().getNodeValue(); 
	             // System.out.println("jj " + kood);
			     double seis = Double.parseDouble(getNode(rida, "amountInStock").getFirstChild().getNodeValue()); 
		    	            
			             if(seis > 0){
		    	                  products.append(kood +  ";");
		    	                  products.append(" ;");
		    	                  products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		    	              }
	                  		 
	                  	  }catch(Exception k){
                                k.printStackTrace();
	                  	  }
	              	    }
	              	  }	  
	              
	         
	          } catch (Exception e) {
	             e.printStackTrace();
	          }
	        //  System.out.println(" "+ products.toString());
	          return products.toString();
	      }

   public Vector getDebt(String agentCode){
        return this.debtList;
   }
   
   private TreeMap<String,Double> getWarehouseStock (String laoid){
	   TreeMap<String,Double> res = new TreeMap<String,Double>();
	   
	   try {
     		StringBuffer inputLineBuf = postData("request=getProductStock&warehouseID=" + laoid + userstuff); //&getStockInfo=1
     		  	
     		
     	  if(inputLineBuf.length() > 2){
             
             try {
           	  File f = new File (getDir() + "laoseis" + laoid +  ".xml");
           	  if(f.exists())f.delete();
           	   Writer os = new BufferedWriter(new OutputStreamWriter( 
    			    new FileOutputStream(getDir() + "laoseis" + laoid +  ".xml"), "UTF-8"));
    		        os.write(inputLineBuf.toString().replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' '));
    		        os.close();
           	  }catch (IOException ex) {
       		ex.printStackTrace();
       	  }
            }
        	} catch (Exception e1) {
     		e1.printStackTrace();
     	}
         
         try {

       	  File f = new File (getDir() + "laoseis" + laoid +  ".xml");
     	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     	     DocumentBuilder db;
     		db = dbf.newDocumentBuilder();      	
         	FileInputStream in = getCleanInputStream(f);
         	String handler = null;
             Document doc = db.parse(in,handler);
             Node root = getNode(doc, "document");      
          //   System.out.println(root);
         	Node read = getNode(root, "records");
        // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
             for (int i = 0; i < read.getChildNodes().getLength(); i++){
             	  Node rida = read.getChildNodes().item(i);
             	  if(rida.getNodeName().equals("item")){
             	  

               try{
           
             String kood  =  getNode(rida, "productID").getFirstChild().getNodeValue(); 
            // System.out.println("jj " + kood);
		     double seis = Double.parseDouble(getNode(rida, "amountInStock").getFirstChild().getNodeValue()); 
	    	            
		             if(seis > 0){
	    	         res.put(kood, seis)  ; 
		             }
                 		 
                 	  }catch(Exception k){
                           k.printStackTrace();
                 	  }
             	    }
             	  }	  
             
        
         } catch (Exception e) {
            e.printStackTrace();
         }
	 
	   
	   
	   return res ;
   }
   
   private HashMap<String,Double> overDue = null;
   
   public Vector getDebt2(){
	  
	   
	   debtList = new Vector();
	   overDue = new HashMap<String,Double>();
	   
		  String mm = getMonthc().substring(0,7);
	      FileListFilter fil = new FileListFilter("doklist","xml");
	 	 String[] ll = new File (getDir()).list(fil);
	 	 for (int u = 0; u < ll.length; u++) {
	 		 String name = ll[u];
		    	//	System.out.println(name);
		 		  File f = new File (getDir() + name);
		 		  
		 		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     DocumentBuilder db;
		  	     try{
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		          Document doc = db.parse(in,handler);
		        
		        Node root = getNode(doc, "document");      
		       	Node read = getNode(root, "records");
		       for (int i = 0; i < read.getChildNodes().getLength(); i++){
		           	  Node rida = read.getChildNodes().item(i);
		           	  if(rida.getNodeName().equals("item")){
		          	  try{
					
					String number = getNode(rida, "number").getFirstChild().getNodeValue();
					String date =  getNode(rida, "date").getFirstChild().getNodeValue();
					String cust = getNode(rida, "clientID").getFirstChild().getNodeValue();
					String paymentStatus = getNode(rida, "paymentStatus").getFirstChild().getNodeValue();
					String type = getNode(rida, "type").getFirstChild().getNodeValue();
					String type2 = null;
					String invoiceState = null;
					
				
					try{
						invoiceState = getNode(rida, "invoiceState").getFirstChild().getNodeValue();
						
					}catch(Exception r) {
						
					}
					try{
						type2 = getNode(rida, "followUpDocuments").getFirstChild().getNodeValue();
						
					}catch(Exception r) {
						
					}
				
					
					if( !paymentStatus.equals("PAID") && (type.equals("INVWAYBILL") || type2.equals("INVOICE") )){
					
						
					
					
					int paevi = 0 ;
					try{
						paevi = Integer.parseInt(getNode(rida, "paymentDays").getFirstChild().getNodeValue());
					 }catch(Exception e){
						e.printStackTrace();
					 }
					
					
					double total = Double.parseDouble(getNode(rida, "total").getFirstChild().getNodeValue());
			        double paid = 0;
			        try{
			        	paid = Double.parseDouble(getNode(rida, "paid").getFirstChild().getNodeValue());
			        }catch(Exception e){
			        	
			        }
			    
			    
			        
			        if(total - paid  > 0 ){
			    	 Date dd = getDate(date); //v�ljastamise kuup�ev 
			    	  Calendar cal = Calendar.getInstance();
			    	  cal.setTime(dd);
			    	  cal.add(Calendar.DAY_OF_YEAR, paevi);
			    	 // cal on maksekuup�ev
			    
			    	  Calendar now  = Calendar.getInstance();
			    	int due =(int)daysBetween(cal,now);
			    	if(due > 0){
			    		if(overDue.get(cust) != null){
			    			overDue.put(cust,total-paid + overDue.get(cust));
			    		}else{
			    			overDue.put(cust,total-paid );
			    		}
			    	}
			    	
			    
			    	
		        Debt debt = new Debt();
			        debt.setCustID(cust);
			        debt.setDoknr(number);
			        debt.setDuedate(cal.getTime());
			        debt.setSum(total-paid);
			        debt.setDate(this.getDate(date));
			        debt.setDue(due + "");
			        debtList.addElement(debt);
			      
			      }
					
			      }
		          	  }catch(Exception e){
		          		  e.printStackTrace();
		          	  }
		           	}     
		          }
		           
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	 	 }
	
	      return this.debtList;
   
   }
   
   
   public void getAfterOrders(){
	   if("true".equals(getParent().getProps().getProperty("after.orders"))){
		   afterOrders = new TreeMap();
	     
		  String mm = getMonthc().substring(0,7);
	      FileListFilter fil = new FileListFilter("ordlist","xml");
	 	 String[] ll = new File (getDir()).list(fil);
	 	 for (int u = 0; u < ll.length; u++) {
	 		 String name = ll[u];
		    	//	System.out.println(name);
		 		  File f = new File (getDir() + name);
		 		  
		 		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     DocumentBuilder db;
		  	     try{
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		          Document doc = db.parse(in,handler);
		        
		        Node root = getNode(doc, "document");      
		       	Node read = getNode(root, "records");
		       for (int i = 0; i < read.getChildNodes().getLength(); i++){
		           	  Node rida = read.getChildNodes().item(i);
		           	  if(rida.getNodeName().equals("item")){
		          	  try{
					
					String number = getNode(rida, "number").getFirstChild().getNodeValue();
					String date =  getNode(rida, "date").getFirstChild().getNodeValue();
					String cust = getNode(rida, "clientID").getFirstChild().getNodeValue();
					String paymentStatus = getNode(rida, "paymentStatus").getFirstChild().getNodeValue();
					String type = getNode(rida, "type").getFirstChild().getNodeValue();
					String type2 = null;
					String invoiceState = null;
					
				
					try{
						invoiceState = getNode(rida, "invoiceState").getFirstChild().getNodeValue();
						
					}catch(Exception r) {
						
					}
				
					
					if(afterOrders != null && (number.endsWith("-1") || number.endsWith("-2") || number.endsWith("-3") || number.endsWith("-4") || number.endsWith("-5"))) {
					//	System.out.println("number " + number + " klient " + cust  + " type " + type + " " + invoiceState);
						
						  Node tooted = getNode(rida, "rows");
						  
						  for (int p = 0; p < tooted.getChildNodes().getLength(); p++){
							  Node tt = tooted.getChildNodes().item(p);
				           	  if(tt.getNodeName().equals("item")){
				           		  
				           		  String prod = getNode(tt, "productID").getFirstChild().getNodeValue();
				           		 String am = getNode(tt, "amount").getFirstChild().getNodeValue();
				           	
				           		String price = getNode(tt, "finalNetPrice").getFirstChild().getNodeValue();
				           		
				           		 Toode t = new Toode(prod);
				           		 t.setBrand(date);
				           		 try {
				           			t.setKogus(Double.parseDouble(am));
					              }catch(Exception r) {
				           			 r.printStackTrace();
				           		 }
				           		 try {
					           			t.setPlancur(Double.parseDouble(price));
						              }catch(Exception r) {
					           			 r.printStackTrace();
					           		 }
				           		 //    CustProd cp = new CustProd(klient, t.getID(),0,0,0,t.getBrand(),t.getPlancur(),t.getKogus(),0," ");
				           		 
				           		 ArrayList arr  = new ArrayList();
				           		 if(afterOrders.get(cust) != null) {
				           			 arr = (ArrayList)afterOrders.get(cust);
				           		 }
				           		 arr.add(t);
				           		afterOrders.put(cust, arr);
				           		 
				           		//System.out.println("number " + number + " klient " + cust  + " type " + type + " " + invoiceState + " " + prod +" "+ am);
				           		  
				           	  }
						  }
						  
					}
				
		          	  }catch(Exception e){
		          		  e.printStackTrace();
		          	  }
		           	}     
		          }
		           
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	 	 }    
	 	 }
	 	
   
   }
   
   
   public static long daysBetween(Calendar startDate, Calendar endDate) {
	    long end = endDate.getTimeInMillis();
	    long start = startDate.getTimeInMillis();
	    return TimeUnit.MILLISECONDS.toDays((end - start));
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
       
       
       private Hashtable getPricelists(String id){
    	
    	   Hashtable prices = new Hashtable();
    	   
    	  	try {

   	  		 FileListFilter fil = new FileListFilter("prlisterp","xml");
   	    	 String[] ll = new File (getDir()).list(fil);
   	    	 for (int ee = 0; ee < ll.length; ee++) {
   	    		
   	    		
   	    		
   	    		
   	    		 String name = ll[ee];
   	    		File f = new File (getDir() + name);
    	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	  	     DocumentBuilder db;
    	  		db = dbf.newDocumentBuilder();      	
    	      	FileInputStream in = getCleanInputStream(f);
    	      	String handler = null;
    	          Document doc = db.parse(in,handler);
    	      	
    	        Node root = getNode(doc, "document");      
    	         // System.out.println(root);
    	       	Node read = getNode(root, "records");
    	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
    	           	  Node rida = read.getChildNodes().item(i);
    	           	  if(rida.getNodeName().equals("item")){
    	          	  try{
    	          		 
    	               		 String group  = getNode(rida,"pricelistID").getFirstChild().getNodeValue();
    	               		if(group.equals(id)){
    	               		    
    	               			Node read2 = getNode(rida, "pricelistRules");
    	               			//System.out.println("ridu  " + read2.getChildNodes().getLength() + read2.toString());
        	              	    	
    	               			for (int u = 0; u < read2.getChildNodes().getLength(); u++){
    	               			 Node rida2 = read2.getChildNodes().item(u);
    	               			
    	               		    if(rida2 != null && rida2.getNodeName() != null && rida2.getNodeName().equals("item")){
    	               		    		
    	               		    	//System.out.println(group + " " + getNode(rida2,"id").getFirstChild().getNodeValue());
    	               		    	//	System.out.println(group + " " + getNode(rida2,"price").getFirstChild().getNodeValue());
    	               			 prices.put(getNode(rida2,"id").getFirstChild().getNodeValue(),getNode(rida2,"price").getFirstChild().getNodeValue());
    	               			}
    	               			}
    	          	         
    	               		}
    	          	  }catch(Exception r){
      	    	  		r.printStackTrace();
      	    	  	  }
    	          	  }
    	           	  }
    	           
   	    	 }   
    	           
    	          	  }catch(Exception e){
    	    	  		e.printStackTrace();
    	    	  	} 

                  //    System.out.println(prices.toString());
    	          	  return prices;
       }

       
       public   String  addLeadingZero(String aaaa){
		   String res = aaaa.trim();
		  	  int len = aaaa.trim().length();
		  	 while (len < 4){
		  		  res = "0" + res ;
		  		  len = res.length();
		  	  }
		  	  return res;
		   }
       
      private HashSet okGrupid = null;
      private HashMap<String,Double> mdGrupid = null;
       
   public TreeMap getProdGroups()  {
     TreeMap   tm = new TreeMap();
     
     if("true".equals(getParent().getProps().getProperty("group.webok"))){
    	 okGrupid = new HashSet();
     }
     
     if("true".equals(getParent().getProps().getProperty("group.maxdisc"))){
    	 mdGrupid = new HashMap<String,Double>();
     }
     
     groupList = new TreeMap();
     groupListSec = new TreeMap();
     boolean subgr = false ;
     if("true".equals(getParent().getProps().getProperty("group.usesub"))){
    	 subgr = true;
    	 this.setTreeGroup(true);
     }
  	try {
  		  File f = new File (getDir() + "tootegrupiderp.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
      	
        Node root = getNode(doc, "document");      
         // System.out.println(root);
       	Node read = getNode(root, "records");
      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  if(rida.getNodeName().equals("item")){
          	  try{
          		String web = null;
          		boolean web0 = false;
          		boolean peaOK = false;
               		 String group  = getNode(rida,"productGroupID").getFirstChild().getNodeValue();
               		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
               		 if(okGrupid != null){
               			try{ 
               			 web  = getNode(rida,"showInWebshop").getFirstChild().getNodeValue();
                    	if(web.equals("1")){
                    		okGrupid.add(group);
                    		peaOK = true;
                    	} else {
                    		web0 = true;
                    	}
                    	
               		    }catch(Exception e){
           			    }
               		 }
               		String web2 = null ;
               		if(mdGrupid != null){
               			try{ 
               			 web2  = getNode(rida,"nameRUS").getFirstChild().getNodeValue();
                    	if(web2 != null && web2.contains("MD")){
                    		mdGrupid.put(group,Double.parseDouble(web2.substring(2)));	
                    	}
               		    }catch(Exception e){
           			    }
               		 }
               		 
               	
               		
               		 name.replaceAll("Sibullill", "Sl");
               		 group = deCode(group);
              
               		 if(subgr){
               			 group = addLeadingZero(group); 
               		 }
               		 name  =  deCode(name);
                        tm.put(group,Converter.enCodeEst(name));  
                        groupList.put(group, group);
                    	Node alamad = getNode(rida, "subGroups");
                    	if(alamad != null){
                            for (int u = 0; u < alamad.getChildNodes().getLength(); u++){
                           	  Node alam = alamad.getChildNodes().item(u);
                           	 if(alam.getNodeName().equals("item")){
                           		 String agroup  = getNode(alam,"productGroupID").getFirstChild().getNodeValue();
                           		  String aname  =  "noname " + agroup;
                           		 if(okGrupid != null){
                           			 try{
                           			 web  = getNode(alam,"showInWebshop").getFirstChild().getNodeValue();
                                     	if(web.equals("1") && web0){
                                     		okGrupid.add(agroup);	
                                     	} else if(peaOK){
                                     		okGrupid.add(agroup);	
                                     	}
                                     	
                                     	if(web.equals("0")){
                                    		web0 = true;
                                    	}	 
                           			 }catch(Exception e){
                           			 }
                           		 }
                           		String web3 = null;
                           		if(mdGrupid != null){
                           			try{ 
                           			 web3  = getNode(alam,"nameRUS").getFirstChild().getNodeValue();
                                	
                           			if(web3 != null && web3.contains("MD")){
                                		mdGrupid.put(agroup,Double.parseDouble(web3.substring(2)));	
                                	}else if(web2 != null){
                                		mdGrupid.put(agroup,Double.parseDouble(web2.substring(2)));	
                                    }
                                	
                           		    }catch(Exception e){
                       			    }
                           		 }
                           	//	System.out.println("ridu  " + agroup );
                           		  try{
                           			  aname = getNode(alam,"name").getFirstChild().getNodeValue();
                                  }catch(Exception t ){
                           		  }
                           		 agroup = deCode(agroup);
                           		 if(subgr){
                           			 agroup = addLeadingZero(agroup); 
                           		 }
                           		  aname  =  deCode(aname);
                           		 if(!subgr){
                           			tm.put(group + "." + agroup, Converter.enCodeEst(aname)); 
                                     
                           		 }
                           		    groupList.put(agroup, group + "." + agroup);
                                    groupListSec.put(group + "." + agroup, Converter.enCodeEst(aname));
                                    
                                    
                                    Node alamad2 = getNode(alam, "subGroups");
                                 	if(alamad2 != null){
                                        for (int w = 0; w < alamad2.getChildNodes().getLength(); w++){
                                       	  Node alam2 = alamad2.getChildNodes().item(w);
                                       	 if(alam2.getNodeName().equals("item")){
                                       		String a2group  = getNode(alam2,"productGroupID").getFirstChild().getNodeValue();
                                        
                                       	 if(okGrupid != null){
                                   			 try{
                                   			 web  = getNode(alam2,"showInWebshop").getFirstChild().getNodeValue();
                                             	if(web.equals("1") && web0){
                                             		okGrupid.add(a2group);	
                                             	}else if(peaOK){
                                             		okGrupid.add(a2group);
                                             	}


                                             	if(web.equals("0")){
                                            		web0 = true;
                                            	}	 
                                             	
                                   			 }catch(Exception e){
                                   			 }
                                   		 }
                                       	 if(a2group.startsWith("42")) {
                                       		System.out.println("ridu2  " + a2group + " " + web); 
                                       	 }
                                     	
                                       		String a2name  = "noname " + a2group;
                                     		  try{
                                     			a2name = getNode(alam2,"name").getFirstChild().getNodeValue();
                                     		  }catch(Exception t){
                                     			  
                                     		  }
                                     		 
                                     		 if(subgr){
                                       			 a2group = addLeadingZero(a2group); 
                                       		 }
                                     		  a2group = deCode(a2group);
                                     		  a2name  =  deCode(a2name);
                                     		 if(!subgr){
                                     			tm.put(group + "." + agroup + "." + a2group, Converter.enCodeEst(a2name)); 
                                              }	
                                     		  groupList.put(a2group, group + "." + agroup + "." + a2group);
                                              groupListSec.put(group + "." + agroup + "." + a2group, Converter.enCodeEst(a2name));
                                                          
                                              Node alamad3 = getNode(alam2, "subGroups");
                                        //   	System.out.println("ridu2  " + read.getChildNodes().getLength() + read.toString());
                                      	if(alamad3 != null){
                                              for (int y = 0; y < alamad3.getChildNodes().getLength(); y++){
                                             	  Node alam3 = alamad3.getChildNodes().item(y);
                                             	 if(alam3.getNodeName().equals("item")){
                                             		String a3group  = getNode(alam3,"productGroupID").getFirstChild().getNodeValue();
                                           		  String a3name  =  getNode(alam3,"name").getFirstChild().getNodeValue();
                                           	//	System.out.println("ridu3  " + a3group );
                                           		 if(okGrupid != null){
                                           			 try{
                                           				 web  = getNode(alam3,"showInWebshop").getFirstChild().getNodeValue();
                                                     	if(web.equals("1") ){// && web0
                                                     		okGrupid.add(a3group);	
                                                     	}	
                                                     	
                                                     	if(web.equals("0")){
                                                    		web0 = true;
                                                    	}
                                                     	
                                           			 }catch(Exception e){
                                           			 }
                                           		 }
                                           		
                                           		 
                                           		String web4 = null;
                                           		if(mdGrupid != null){
                                           			try{ 
                                           			 web4  = getNode(alam,"nameRUS").getFirstChild().getNodeValue();
                                                	
                                           			if(web4 != null && web4.contains("MD")){
                                                		mdGrupid.put(a3group,Double.parseDouble(web4.substring(2)));	
                                                	}else if(web3 != null){
                                                		mdGrupid.put(a3group,Double.parseDouble(web3.substring(2)));	
                                                    }else if(web2 != null){
                                                		mdGrupid.put(a3group,Double.parseDouble(web2.substring(2)));	
                                                    }
                                                	
                                           		    }catch(Exception e){
                                       			    }
                                           		 }
                                           		 
                                           		 
                                           		 
                                           		  
                                           		  if(subgr){
                                           			 a3group = addLeadingZero(a3group); 
                                           		 }
                                           		 a3group = deCode(a3group);
                                           		  a3name  =  deCode(a3name);
                                            
                                           		 if(!subgr){
                                           			tm.put(group + "." + agroup + "." + a2group + "." + a3group, Converter.enCodeEst(a3name)); 
                                                 }
                                           		 
                                          			groupList.put(a3group, group + "." + agroup + "." + a2group +"." + a3group);
                                                    groupListSec.put(group + "." + agroup + "." + a2group +"." + a3group
                                                    		, Converter.enCodeEst(a3name));
                                                  
                                                    Node alamad4 = getNode(alam3, "subGroups");
                                                    //   	System.out.println("ridu2  " + read.getChildNodes().getLength() + read.toString());
                                                  	if(alamad4 != null){
                                                          for (int q = 0; q < alamad4.getChildNodes().getLength(); q++){
                                                         	  Node alam4 = alamad4.getChildNodes().item(q);
                                                         	 if(alam4.getNodeName().equals("item")){
                                                         		String a4group  = getNode(alam4,"productGroupID").getFirstChild().getNodeValue();
                                                       		  String a4name  =  getNode(alam4,"name").getFirstChild().getNodeValue();
                                                       		 if(okGrupid != null){
                                                       			 try{
                                                       				 web  = getNode(alam4,"showInWebshop").getFirstChild().getNodeValue();
                                                                 	if(web.equals("1") && web0){
                                                                 		okGrupid.add(a4group);	
                                                                 	}	
                                                                 	
                                                                 	if(web.equals("0")){
                                                                		web0 = true;
                                                                	}
                                                                 	
                                                       			 }catch(Exception e){
                                                       			 }
                                                       		 }
                                                       		
                                                       		 if(a4group.startsWith("45")) {
                                                            		System.out.println("ridu4  " + a4group + " " + web); 
                                                            	 } 
                                                       		 
                                                        		String web5 = null;
                                                        		if(mdGrupid != null){
                                                        			try{ 
                                                        			 web5  = getNode(alam,"nameRUS").getFirstChild().getNodeValue();
                                                             	
                                                        			if(web5 != null && web5.contains("MD")){
                                                             		mdGrupid.put(a4group,Double.parseDouble(web5.substring(2)));	
                                                             	}else if(web4 != null){
                                                             		mdGrupid.put(a4group,Double.parseDouble(web4.substring(2)));	
                                                                 }else if(web3 != null){
                                                             		mdGrupid.put(a4group,Double.parseDouble(web3.substring(2)));	
                                                                 }else if(web2 != null){
                                                             		mdGrupid.put(a4group,Double.parseDouble(web2.substring(2)));	
                                                                 }
                                                             	
                                                        		    }catch(Exception e){
                                                    			    }
                                                        		 }
                                                       		  if(subgr){
                                                       			 a4group = addLeadingZero(a4group); 
                                                       		 }
                                                    //   		System.out.println("ridu4  " + a4group );
                                                       		 a4group = deCode(a4group);
                                                       		  a4name  =  deCode(a4name);
                                                        
                                                       		 if(!subgr){
                                                       			tm.put(group + "." + agroup + "." + a2group + "." + a3group+ "." + a4group, Converter.enCodeEst(a4name)); 
                                                             }
                                                       		 
                                                      			groupList.put(a4group, group + "." + agroup + "." + a2group +"." + a3group + "." + a4group);
                                                                groupListSec.put(group + "." + agroup + "." + a2group + "." + a3group + "." + a4group
                                                                		, Converter.enCodeEst(a4name));
                                                                
                                                         	 } 
                                                            } 
                                                    	  }
                                                    
                                             	 } 
                                                }
                                              
                                              
                                        	  }
                                            } 
                                      	 } 
                                  	 }
                                 }
                            }	
                    	}
                             	
                  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 
          	  }
           	  }     
       	  
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
 	
  //	System.out.println("ok " + okGrupid.toS );
    
  	System.out.println("tm " + tm.size());
         return tm;
   }

   
   public TreeMap getProdGroupsSecond() throws SQLException {
	   
	   if("true".equals(getParent().getProps().getProperty("brands.group"))){
		   TreeMap   tm = new TreeMap();
		  	try {
		  		  File f = new File (getDir() + "brandserp.xml");
		  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     DocumentBuilder db;
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		          Document doc = db.parse(in,handler);
		      	
		        Node root = getNode(doc, "document");      
		         // System.out.println(root);
		       	Node read = getNode(root, "records");
		      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
		           for (int i = 0; i < read.getChildNodes().getLength(); i++){
		           	  Node rida = read.getChildNodes().item(i);
		           	  if(rida.getNodeName().equals("item")){
		          	  try{
		          		 String group  = getNode(rida,"brandID").getFirstChild().getNodeValue();
	               		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
	               		 group = deCode(group);
	               		  name  =  deCode(name);
	                        tm.put("2." + group,Converter.enCodeEst(name));  
		          	  }catch (Exception r){
		          		  r.printStackTrace();
		          	  }
		          	  }             	  
	                 }
		           }catch (Exception r){
		          		r.printStackTrace();  
		          	  }
		           return tm;
	   
	   } else if("true".equals(getParent().getProps().getProperty("supplier.group"))){
		   
		   TreeMap   tm = suppliersTM;
		  	           return tm;	   
	   } else if("true".equals(getParent().getProps().getProperty("group.usesub"))){
		   return this.groupListSec;
		     	   
	   }else{
		   return null;
	   }
	   
	   
        }


   private TreeMap suppliersTM = null;

   private String enCode(String res ){
	  	 return Converter.enCodeEst(res);
	  }
	  
   
   public static String deCode(String res ){
	  return Converter.deCode(res);
 }

   TreeMap agents = null;
   
   private void sendAsi(){
	   eevaeksport.util.ReportingService serv = new ReportingService();
	   
		serv.user =  getParent().getProps().getProperty("rest.user");
		serv.pass = getParent().getProps().getProperty("rest.pass");
		serv.company = getParent().getProps().getProperty("rest.company");
		

		Vector lines = new Vector();
		Product pp = new Product("kast");
		pp.setInCases(33);
		lines.add(pp);

		Product pp1 = new Product("kast");
		pp1.setInCases(55);
		lines.add(pp1);
		
	  //saadame tagastus dokumnendi 
	
				try {
				serv.addNewShipDoc(null, "ida",
						null, null, null,"SHIPIT",lines, "magi" , new Date(),null,null);
				}catch(Exception r){
					r.printStackTrace();
				}
			
		serv.closeClient();	

   }
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // this.getFixedAssortmentData();
    // plans = this.makePlans("Budget.xlsx");
     
     agent1 = new TreeMap();
    // sendAsi();
     agents = new TreeMap();
    getProdGroups();
     StringBuffer inputLineBuf = new StringBuffer();
	try {
		inputLineBuf = postData("request=getEmployees&recordsOnPage=" + onpage + userstuff);
	} catch (Exception e1) {
		e1.printStackTrace();
	}
     if(inputLineBuf.length() > 2){
         
         try {
       	  File f = new File (getDir() + "tootajad.xml");
       	  if(f.exists())f.delete();
       	      Writer os = new BufferedWriter(new OutputStreamWriter( 
			    new FileOutputStream(getDir() + "tootajad.xml"), "UTF-8"));
		        os.write(inputLineBuf.toString().replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' '));
		        os.close();
       	  }catch (IOException ex) {
   		ex.printStackTrace();
   	  }
    }
   
     // loene v�imalikud agendid sisse 
   
     try {
 		inputLineBuf = postData("request=getCustomerGroups&recordsOnPage=" + onpage + userstuff);
 	} catch (Exception e1) {
 		e1.printStackTrace();
 	}
      if(inputLineBuf.length() > 2){
          
          try {
        	  File f = new File (getDir() + "kliendigrupid.xml");
        	  if(f.exists())f.delete();
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() + "kliendigrupid.xml", true));
    		os.write(inputLineBuf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    
     
     
 	try {
 		  File f = new File (getDir() + "tootajad.xml");
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "document");      
      //   System.out.println(root);
     	Node read = getNode(root, "records");
    // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
         for (int i = 0; i < read.getChildNodes().getLength(); i++){
         	  Node rida = read.getChildNodes().item(i);
         	  if(rida.getNodeName().equals("item")){

             	  try{
             		  
             		//	System.out.println("rida  " + rida.getNodeName() );
             		// getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
             		 String salesman  =  getNode(rida, "employeeID").getFirstChild().getNodeValue(); 
             		
             		 Agent sm = new Agent (salesman);
             		 String name  = deCode(getNode(rida, "employeeName").getFirstChild().getNodeValue());        		
             		 sm.setName(name);
                     tm.put(name, sm);
                     agent1.put(sm.getID(), sm);
                     agents.put(salesman, name);	 
             	  }catch(Exception e){
             		  e.printStackTrace();
             	  }  
         	  }
              
         }
         
 	}catch(Exception e){
 		e.printStackTrace();
 	}
     
     System.out.println("Agendilist tehtud" );

//     System.out.println("Hinnakirju kasutusel "  + pricelists.size() +  " " + pricelists.toString() );

  //   System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());

    // fixedLines = getFixedAssortmentData1();
            
     
     Agent sm = new Agent ("HOUSE");
		       		
		 sm.setName("OFFICE");
     tm.put("HOUSE", sm); 
    // getAfterOrders();
    // setBulkSales(tm);
     return tm;
   }

   
   private StringBuffer removeTags(String input , String tag){
	   StringBuffer res  = new StringBuffer();
	   int len = tag.length();
	   while (input.indexOf("<" + tag + ">") != -1){
		   int a = input.indexOf("<" + tag + ">");
		   res.append(input.substring(0,a));
		   input = input.substring(a + len + 1);
		  // System.out.println("vahe " + input.substring(0,100));
		   if(input.indexOf("</" +tag + ">") != -1){
			   input = input.substring(input.indexOf("</" +tag + ">") + len + 3);
		   }
		   //System.out.println("vahe2 " + input.substring(0,100));
	   }
	   
	  res.append(input);
	   return res ;
   }
   

private String getMonth6(){
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.MONTH, -6);

	String res = cal.get(Calendar.YEAR)+ "-";
	String mm = (cal.get(Calendar.MONTH )+1) +"";
	if(mm.length() == 1 )
		mm = "0" + mm;
	
	return res + mm + "-01";
}
  
private String getMonthc(){
	Calendar cal = Calendar.getInstance();
	
	String res = cal.get(Calendar.YEAR)+ "-";
	String mm = (cal.get(Calendar.MONTH )+1) +"";
	if(mm.length() == 1 )
		mm = "0" + mm;
	
	return res + mm + "-01";
}
   
private String getMonthe(){
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.MONTH, -1);
	String res = cal.get(Calendar.YEAR)+ "-";
	String mm = (cal.get(Calendar.MONTH )+1) +"";
	if(mm.length() == 1 )
		mm = "0" + mm;
	
	return res + mm + "-01";
}

private String laoID = "2";


public static String getDataDirStr(){
	StringBuffer sb = new StringBuffer();

	Calendar	cal = Calendar.getInstance();
	
	
	String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	if (day.length() == 1) {day = "0" + day;}

	String minute = String.valueOf(cal.get(Calendar.MINUTE));
	if (minute.length() == 1) {minute = "0" + minute;}

	String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
	if (month.length() == 1){month = ("0") + month;}

	sb.append(("" + cal.get(Calendar.YEAR)).substring(2,4));
	sb.append("" + month);
	sb.append(day +"");
	sb.append(minute +"");
	return sb.toString();
}


   private void makeData(){
	   
	   System.out.println("copy data dir " + getParent().getProps().getProperty("copydatadir"));
	   if("true".equals(getParent().getProps().getProperty("copydatadir"))){        	   
    	 File ff = new File(getDir());
    	 File fs = new File(getDir() + getDataDirStr() );
    	 if(!fs.exists()){
    		 fs.mkdir();
    	 }
    	 
    	
         File[] listOfFiles = ff.listFiles();

         for (int i = 0; i < listOfFiles.length; i++) {
        	// System.out.println("copy data dir " + listOfFiles[i].getName());
             if (listOfFiles[i].isFile()) {
                  File f = new File(getDir() +listOfFiles[i].getName()); 
                    f.renameTo(new File(fs.getAbsolutePath()+"/"+ listOfFiles[i].getName()));
             }
         }
    	 
       }
	    
	   
	   
       try{
           StringBuffer inputLineBuf = new StringBuffer();
       	try {
       		
       		
       		
       		
    		inputLineBuf = postData("request=getProductGroups&getAllLanguages=1" + userstuff);
    	//	inputLineBuf = getData("request=getCustomers&customerID=430&recordsOnPage=" + onpage + "&getAddresses=1&getContactPersons=1&getBalanceInfo=1&responseMode=detail" + userstuff);
           	
    	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
           if(inputLineBuf.length() > 2){          
                 try {
               	  File f = new File (getDir() + "tootegrupiderp.xml");
               	  if(f.exists())f.delete();
           	
				 Converter.saveFileInUTF8(getDir() + "tootegrupiderp.xml", inputLineBuf.toString());
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
           
           
       
       //   System.exit(0);
           
           
                inputLineBuf = new StringBuffer();
           	try {
        		inputLineBuf = postData("request=getProductPriorityGroups" + userstuff);
        	} catch (Exception e1) {
        		e1.printStackTrace();
        	}
               if(inputLineBuf.length() > 2){          
                     try {
                   	  File f = new File (getDir() + "productPriorityGroups.xml");
                   	  if(f.exists())f.delete();
               	
    				 Converter.saveFileInUTF8(getDir() + "productPriorityGroups.xml", inputLineBuf.toString());
               	  }catch (Exception ex) {
               		ex.printStackTrace();
               	  }
                }
       
               System.out.println("Kliendigrupid alustan" );
             	try {
          		inputLineBuf = postData("request=getCustomerGroups" + userstuff);
          	} catch (Exception e1) {
          		e1.printStackTrace();
          	}
                 if(inputLineBuf.length() > 2){          
                       try {
                     	  File f = new File (getDir() + "klgrupperp.xml");
                     	  if(f.exists())f.delete();
                     	 Converter.saveFileInUTF8(getDir() + "klgrupperp.xml", inputLineBuf.toString());
                 	
                 	  }catch (Exception ex) {
                 		ex.printStackTrace();
                 	  }
                  }
                
                  //  System.exit(0); 
               
               
           try {
       		inputLineBuf = postData("request=getBrands" + userstuff);
       	} catch (Exception e1) {
       		e1.printStackTrace();
       	}
              if(inputLineBuf.length() > 2){          
                    try {
                  	  File f = new File (getDir() + "brandserp.xml");
                  	  if(f.exists())f.delete();
              	
   				 Converter.saveFileInUTF8(getDir() + "brandserp.xml", inputLineBuf.toString());
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }
               }

              if("true".equals(getParent().getProps().getProperty("deltypes"))){   
            	
              System.out.println("Teen delType listi" );   
              
            	try {
         		inputLineBuf = postData("request=getDeliveryTypes" + userstuff);
         	} catch (Exception e1) {
         		e1.printStackTrace();
         	}
                if(inputLineBuf.length() > 2){          
                      try {
                    	  File f = new File (getDir() + "deltype.xml");
                    	  if(f.exists())f.delete();
                    	 Converter.saveFileInUTF8(getDir() + "deltype.xml", inputLineBuf.toString());
                	
                	  }catch (Exception ex) {
                		ex.printStackTrace();
                	  }
                 }
              
             //   System.exit(0);
              }
                
                FileListFilter fil = new FileListFilter("prlisterp","xml");
 	    	 String[] ll = new File (getDir()).list(fil);
 	    	 for (int i = 0; i < ll.length; i++) {
 	    		 String name = ll[i];
 		    		// System.out.println(name);
 		 		  File f = new File (getDir() + name);
 	           if(f.exists()){
 	        	   f.delete();
 	           }
 	    	 }
              
              try {
             		inputLineBuf = postData("request=getPriceLists&recordsOnPage=" + onpage  + userstuff);
             	} catch (Exception e1) {
             		e1.printStackTrace();
             	}
                                       
                    int tsykkel = 0;
            		if (inputLineBuf.indexOf("recordsTotal") != -1) {
            			int lines = inputLineBuf.indexOf("recordsTotal");
            			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
            			// System.out.println(inputLineBuf);
            			try {
            				tsykkel = Integer.parseInt(inputLineBuf.substring(
            						lines + 13, end - 2));
            			} catch (Exception e) {

            			}
            		}
            		if (inputLineBuf.length() > 2) {
            			try {
            				File f = new File(getDir() + "prlisterp1.xml");
            				if (f.exists())
            					f.delete();
            				 String c = (char)0x19 + "";
            				 Converter.saveFileInUTF8(getDir() + "prlisterp1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
            						 .replace((char)0x19, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0x19, ' ')
            						 .replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
            						 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
            						 .replace((char)0x1b, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0x1b, ' '));
            		       
            			} catch (Exception ex) {
            				ex.printStackTrace();
            			}
            		}

            		if (tsykkel > onpage) {
            			int counter = 2;
            			for (; tsykkel > onpage; tsykkel = tsykkel - onpage) {
            				try {
            					inputLineBuf = postData("request=getPriceLists&recordsOnPage=" + onpage + "&pageNo="
            							+ counter + userstuff);
            				} catch (Exception e1) {
            					e1.printStackTrace();
            				}

            				if (inputLineBuf.length() > 2) {
            					try {
            						File f = new File(getDir() + "prlisterp" + counter
            								+ ".xml");
            						if (f.exists())
            							f.delete();
            						
            						 Converter.saveFileInUTF8(getDir() + "prlisterp" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
            								 .replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0x19, ' ')
            								 .replace((char)0xe, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
            								 .replace((char)0x1c, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
            								 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')

            					     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' '));
            					
            					} catch (Exception ex) {
            						ex.printStackTrace();
            					}
            				}

            				counter++;
            			}

            		}

/*
                	boolean soldsortnew = true;
                  	 if("true".equals(getParent().getProps().getProperty("nosoldsortnew"))){        	   
                  		soldsortnew = false;
                         }

                          if(soldsortnew){
                       	
                          System.out.println("kliendisortimendiduus alustan" );
                          Calendar dds = Calendar.getInstance();
                          dds.set(Calendar.DAY_OF_MONTH, 1);
                          Calendar dde = Calendar.getInstance();
                          
                          
                          try {

                        		inputLineBuf = postData("request=getSalesReport&reportType=SALES_BY_ALL_PRODUCTS&"
                        				+ "dateStart="+ Converter.getYYYYmMMmDDmString(dds.getTime())
                        				+"&dateEnd=" + Converter.getYYYYmMMmDDmString(dde.getTime()) + userstuff);
                          
                        		if(inputLineBuf.indexOf("<errorCode>") != -1 ){

                        			inputLineBuf = postData("request=getSalesReport&reportType=SALES_BY_ALL_PRODUCTS&"
                            				+ "dateStart="+ Converter.getYYYYmMMmDDmString(dds.getTime())
                            				+"&dateEnd=" + Converter.getYYYYmMMmDDmString(dde.getTime()) + userstuff);
                         }
                      	 if(inputLineBuf.indexOf("<errorCode>") != -1 ){
                      		inputLineBuf = postData("request=getSalesReport&reportType=SALES_BY_ALL_PRODUCTS&"
                    				+ "dateStart="+ Converter.getYYYYmMMmDDmString(dds.getTime())
                    				+"&dateEnd=" + Converter.getYYYYmMMmDDmString(dde.getTime()) + userstuff);
                         }
                      		
                      	} catch (Exception e1) {
                      		e1.printStackTrace();
                      	}
                             if(inputLineBuf.length() > 2){          
                                   try {
                                 	  File f = new File (getDir() + "klienditoodeErpUus.xml");
                                 	  if(f.exists())f.delete();
                                 	  Converter.saveFileInUTF8(getDir() + "klienditoodeErpUus.xml", Converter.deCode(inputLineBuf.toString()));
                             	 }catch (Exception ex) {
                             		ex.printStackTrace();
                             	  }
                              }
                          
                          System.out.println("kliendisortimendid UUs tehtud" );
                          //algab Hinnakirja faili moodustamine
                          
                          }*/
              
            	      
                   	boolean soldsort = true;
               	 if("true".equals(getParent().getProps().getProperty("nosoldsort"))){        	   
               		soldsort = false;
                      }
               	 
               	 if("true".equals(getParent().getProps().getProperty("soldindebt"))){        	   
                		soldsort = false;
                       }


                       if(soldsort){
                    	   int mm = 1;
                    	   if(getParent().getProps().getProperty("months") != null){        
                    		    mm = Integer.parseInt(getParent().getProps().getProperty("months"));
                    	   }
                    	   
                       Calendar dds = Calendar.getInstance();
                       dds.add(Calendar.MONTH, - mm);
                       
                       Calendar dde = Calendar.getInstance();
                       
                       
                       System.out.println("kliendisortimendid alustan"  + mm + " kuud" +  Converter.getYYYYmMMmDDmString(dds.getTime()) +"  "+  Converter.getYYYYmMMmDDmString(dde.getTime()));
                        

                 
                       try {
                   		inputLineBuf = postData("request=getReportSalesRows" + "&dateStart="+ Converter.getYYYYmMMmDDmString(dds.getTime())
            				+"&dateEnd=" + Converter.getYYYYmMMmDDmString(dde.getTime()) + userstuff);
                   	
                   		
                   	} catch (Exception e1) {
                   		e1.printStackTrace();
                   	}
                          if(inputLineBuf.length() > 2 && inputLineBuf.indexOf("<errorCode>") == -1 ){          
                                try {
                              	  File f = new File (getDir() + "klienditoodeErp.xml");
                              	  if(f.exists())f.delete();
                              	  Converter.saveFileInUTF8(getDir() + "klienditoodeErp.xml", Converter.deCode(inputLineBuf.toString()));
                          	 }catch (Exception ex) {
                          		ex.printStackTrace();
                          	  }
                           }
                       
                       System.out.println("kliendisortimendid tehtud" );
                       
                       
                       }
            		
            		
                  //  System.exit(0);
                    
                    
           //l�peb tootegruppidega tegelemine 
          // System.out.println("Tootegrupid tehtud" );

                    

                       
                       
                       System.out.println("Teen Order listi" );   
           
           FileListFilter fil2 = new FileListFilter("ordlist","xml");
	    	 String[] ll2 = new File (getDir()).list(fil2);
	    	 for (int i = 0; i < ll2.length; i++) {
	    		 String name = ll2[i];
		    		// System.out.println(name);
		 		  File f = new File (getDir() + name);
	           if(f.exists()){
	        	   f.delete();
	           }
	    	 }
	    		
	    		boolean doclist = true;
	    	 if("true".equals(getParent().getProps().getProperty("nodoclist"))){        	   
	    		 doclist = false;
	           }

           if(doclist){
        	
         String  data = "request=getSalesDocuments&recordsOnPage=" + this.onpage + "&dateFrom=" + getMonth6() +  "&getRowsForAllInvoices=1&types=ORDER&invoiceState=READY" //"&types=INVWAYBILL,WAYBILL,INVOICE"
				+ userstuff;// &dateFrom=2010-12-01
		// System.out.println(data);

		inputLineBuf = postData(data);
		// System.out.println(inputLineBuf.toString());

		 tsykkel = 0;
		if (inputLineBuf.indexOf("recordsTotal") != -1) {
			int lines = inputLineBuf.indexOf("recordsTotal");
			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
			// System.out.println(inputLineBuf);
			try {
				tsykkel = Integer.parseInt(inputLineBuf.substring(
						lines + 13, end - 2));
			} catch (Exception e) {

			}
		}
		if (inputLineBuf.length() > 2) {
			try {
				File f = new File(getDir() + "ordlist1.xml");
				if (f.exists())
					f.delete();
				 String c = (char)0x19 + "";
				 Converter.saveFileInUTF8(getDir() + "ordlist1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
						 .replace((char)0x19, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0x19, ' ')
						 .replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
						 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
						 .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
			     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' '));
		       
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (tsykkel > onpage) {
			int counter = 2;
			for (; tsykkel > onpage; tsykkel = tsykkel - onpage) {
				try {
					inputLineBuf = postData("request=getSalesDocuments&recordsOnPage=" + onpage + "&getRowsForAllInvoices=1&types=ORDER&invoiceState=READY&dateFrom=" + getMonth6() + "&pageNo="
							+ counter + userstuff);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				if (inputLineBuf.length() > 2) {
					try {
						File f = new File(getDir() + "ordlist" + counter
								+ ".xml");
						if (f.exists())
							f.delete();
						
						 Converter.saveFileInUTF8(getDir() + "ordlist" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
								 .replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0x19, ' ')
								 .replace((char)0xe, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
								 .replace((char)0x1c, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
								 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
					     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' '));
					
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				counter++;
			}

		}
           
		   
           }
           
                              

                       
                       
                       System.out.println("Teen Dokyumendi listi" );   
           
           FileListFilter fil23 = new FileListFilter("doklist","xml");
	    	 String[] ll23 = new File (getDir()).list(fil23);
	    	 for (int i = 0; i < ll23.length; i++) {
	    		 String name = ll23[i];
		    		// System.out.println(name);
		 		  File f = new File (getDir() + name);
	           if(f.exists()){
	        	   f.delete();
	           }
	    	 }
	    		
	    	
	    	 if("true".equals(getParent().getProps().getProperty("nodoclist"))){        	   
	    		 doclist = false;
	           }

           if(doclist){
        	
         String  data = "request=getSalesDocuments&recordsOnPage=" + this.onpage + "&dateFrom=" + getMonth6() +  "&types=INVWAYBILL,WAYBILL,INVOICE"
				+ userstuff;// &dateFrom=2010-12-01
		// System.out.println(data);

		inputLineBuf = postData(data);
		// System.out.println(inputLineBuf.toString());

		 tsykkel = 0;
		if (inputLineBuf.indexOf("recordsTotal") != -1) {
			int lines = inputLineBuf.indexOf("recordsTotal");
			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
			// System.out.println(inputLineBuf);
			try {
				tsykkel = Integer.parseInt(inputLineBuf.substring(
						lines + 13, end - 2));
			} catch (Exception e) {

			}
		}
		if (inputLineBuf.length() > 2) {
			try {
				File f = new File(getDir() + "doklist1.xml");
				if (f.exists())
					f.delete();
				 String c = (char)0x19 + "";
				 Converter.saveFileInUTF8(getDir() + "doklist1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
						 .replace((char)0x19, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0x19, ' ')
						 .replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
						 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
						 .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
			     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' '));
		       
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (tsykkel > onpage) {
			int counter = 2;
			for (; tsykkel > onpage; tsykkel = tsykkel - onpage) {
				try {
					inputLineBuf = postData("request=getSalesDocuments&recordsOnPage=" + onpage + "&types=INVWAYBILL,WAYBILL,INVOICE&dateFrom=" + getMonth6() + "&pageNo="
							+ counter + userstuff);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				if (inputLineBuf.length() > 2) {
					try {
						File f = new File(getDir() + "doklist" + counter
								+ ".xml");
						if (f.exists())
							f.delete();
						
						 Converter.saveFileInUTF8(getDir() + "doklist" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
								 .replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0x19, ' ')
								 .replace((char)0xe, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
								 .replace((char)0x1c, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ') .replace((char)0x1b, ' ')
								 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
					     			.replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' ').replace((char)0xb, ' '));
					
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				counter++;
			}

		}
           
		   
           }
           
  //   System.exit(0);  
           System.out.println("Laod alustan" );
       	try {
    		inputLineBuf = postData("request=getWarehouses" + userstuff);
    	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
           if(inputLineBuf.length() > 2){          
                 try {
               	  File f = new File (getDir() + "laoderp.xml");
               	  if(f.exists())f.delete();
               	 Converter.saveFileInUTF8(getDir() + "laoderp.xml", inputLineBuf.toString());
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
          
                
          
     
           
           System.out.println("Algab toodete moodustamine" );
            tsykkel = 0;
           
           inputLineBuf = new StringBuffer();
           boolean cut = true;
           if("true".equals(getParent().getProps().getProperty("nocut"))){        	   
        	   cut = false;
           }
           String luba = "";
           if(getParent().getProps().getProperty("okgrups") != null){
                luba =  "&groupIDsWithSubgroups=" + getParent().getProps().getProperty("okgrups") ;  
           }
          
           if(getParent().getProps().getProperty("okstatus") != null){
               luba = luba + "&status=" + getParent().getProps().getProperty("okstatus") ;  
          }
           
        	
       	try {
    		inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage +
    				"&getPackageInfo=1&getStockInfo=1&getContainerInfo=1" + luba + userstuff); //&getStockInfo=1
    		//inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getStockInfo=1&getContainerInfo=1&warehouseID="+ laoID + userstuff); //&getStockInfo=1
    		
    	//	System.out.println("joru1 " + inputLineBuf.substring(0,100));
    		if(cut){
    				inputLineBuf = removeTags(inputLineBuf.toString(),"manufacturerName");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"brandName");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"packages");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"description");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"longdesc");	  
    		  }
    		//System.out.println("joru2 " + inputLineBuf.substring(0,100));
        	
       	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
        
           
         if(inputLineBuf.indexOf("recordsTotal") != -1 ){
        	 int lines = inputLineBuf.indexOf("recordsTotal");
        	 int end = inputLineBuf.indexOf("recordsTotal",lines+12);
        	
        	 try{
        		 tsykkel = Integer.parseInt(inputLineBuf.substring(lines+13,end-2));
        	
        		 //kuion olemas siis vanad failid maha.
        		 if(tsykkel != 0){
        			 fil = new FileListFilter("tootederp","xml");
         	    	 ll = new File (getDir()).list(fil);
         	    	 for (int i = 0; i < ll.length; i++) {
         	    		 String name = ll[i];
         		    		// System.out.println(name);
         		 		  File f = new File (getDir() + name);
         	           if(f.exists()){
         	        	   f.delete();
         	           }
         	    	 }	 
        		 }
        		 
        	 }catch(Exception e){
        		 
        	 }
         }
        // System.out.println("Tooted Tsykkel " + tsykkel );
           if(inputLineBuf.length() > 2){        
               try {
             	  File f = new File (getDir() + "tootederp1.xml");
             	  if(f.exists())f.delete();
         		 Converter.saveFileInUTF8(getDir() + "tootederp1.xml", inputLineBuf.toString().replace((char)0x1d, ' ').replace((char)0x1d, ' '));
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
           
           if(tsykkel > onpage ){
        	  int counter = 2;
        	   for(;tsykkel > onpage;tsykkel = tsykkel-onpage){
        		   try {
        	    		inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&pageNo=" + counter + luba + userstuff); //&getStockInfo=1
        	    		//inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&warehouseID=" + laoID + "&pageNo=" + counter + userstuff); //&getStockInfo=1
        	    		if(inputLineBuf.toString().indexOf("1054") != -1 && inputLineBuf.toString().indexOf("error") !=-1){
        	    			 session = getSessionId(passString);
        	    				System.out.println("uus session " + session);
        	    			   while(session.indexOf("%") != -1){
        	    				   session = getSessionId(passString);
        	    			   }
        	    			    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
        	    			    inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&pageNo=" + counter  + luba + userstuff); //&getStockInfo=1
                	    			    
        	    		}
        	    		
        	    		if(cut){
        	    				inputLineBuf = removeTags(inputLineBuf.toString(),"manufacturerName");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"brandName");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"packages");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"description");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"longdesc");	 
        	    		 }
        	    	
        		   } catch (Exception e1) {
        	    		e1.printStackTrace();
        	    	}  
        	    	if(inputLineBuf.length() > 2){        
        	               try {
        	             	  File f = new File (getDir() + "tootederp" + counter + ".xml");
        	             	  if(f.exists())f.delete();
        	         
        	       	    	 Converter.saveFileInUTF8(getDir() + "tootederp" + counter + ".xml", inputLineBuf.toString());
        	               }catch (Exception ex) {
        	         		ex.printStackTrace();
        	         	  }
        	          }

        	    	counter++;
        	   }
        	   
           }
         
           
           System.out.println("l�petatud toodete moodustamine" );
           System.out.println("algab klientide moodustamine" ); 
           inputLineBuf = new StringBuffer();
           
        
           String custgrupp = "";
           try {
        	 
        	   if(getParent().getProps().getProperty("customer.group") != null){
        		   custgrupp =  "&groupIDWithSubgroups=" + getParent().getProps().getProperty("customer.group");  
        	   }
       		inputLineBuf = getData("request=getCustomers&recordsOnPage=" + onpage + "&getAddresses=1&getContactPersons=1&getBalanceInfo=1&responseMode=detail"+ custgrupp + userstuff);
       	
       	System.out.println("buff" + inputLineBuf );
       	   } catch (Exception e1) {
       		e1.printStackTrace();
       	   }
       
       	   
       	   tsykkel = 0;
        if(inputLineBuf.indexOf("recordsTotal") != -1 ){
       	 int lines = inputLineBuf.indexOf("recordsTotal");
       	 int end = inputLineBuf.indexOf("recordsTotal",lines+12);
       	
       	 try{
       		 tsykkel = Integer.parseInt(inputLineBuf.substring(lines+13,end-2));
       	//kui �nnestub siis kustutame maha
       		 if(tsykkel != 0){
       			fil = new FileListFilter("kliendiderp","xml");
    	    	 ll = new File (getDir()).list(fil);
    	    	 for (int i = 0; i < ll.length; i++) {
    	    		 String name = ll[i];
    		    		// System.out.println(name);
    		 		  File f = new File (getDir() + name);
    	           if(f.exists()){
    	        	   f.delete();
    	           }
    	    	 } 
    	    	   if(inputLineBuf.length() > 2){
    	        	   try {
    	              	  File f = new File (getDir() + "kliendiderp1.xml");
    	              	  if(f.exists())f.delete();
    	          	
    	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp1.xml", inputLineBuf.toString().replace((char)0x1f, ' ').replace((char)0x1f, ' '));
         	            
    	          	  }catch (Exception ex) {
    	          		ex.printStackTrace();
    	          	  }
    	            }
    	    	 
       		 }
        		
       		 
       	 }catch(Exception e){
       		 
       	 }
       	 System.out.println("Klientide tsykkel " + tsykkel);
       	 
        }
        
         
           
           if(tsykkel > onpage ){
         	  int counter = 2;
         	   for(;tsykkel > onpage;tsykkel = tsykkel-onpage){
         		  try {
         			//  System.out.println("request=getCustomers&recordsOnPage=100&responseMode=detail&pageNo=" + counter + userstuff);
         	       		inputLineBuf = getData("request=getCustomers&recordsOnPage=" + onpage + "&getAddresses=1&getContactPersons=1&getBalanceInfo=1&responseMode=detail&pageNo=" + counter + custgrupp+ userstuff);
         	       	   } catch (Exception e1) {
         	       		e1.printStackTrace();
         	       	   }
         	        
         		 if(inputLineBuf.toString().contains("errorCode>1054")){
	    			 session = getSessionId(passString);
	    				System.out.println(session);
	    			   while(session.indexOf("%") != -1){
	    				   session = getSessionId(passString);
	    			   }
	    			    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
	    			    inputLineBuf = getData("request=getCustomers&recordsOnPage=" + onpage + "&getAddresses=1&responseMode=detail&pageNo=" + counter + userstuff);
	         	       	   	    
	    		}
         	    	if(inputLineBuf.length() > 2){        
         	               try {
         	             	  File f = new File (getDir() + "kliendiderp" + counter + ".xml");
         	             	  if(f.exists())f.delete();
         	         	
       	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp" + counter + ".xml", inputLineBuf.toString().replace((char)0xe, ' ')
       	          			.toString().replace((char)0xe, ' ')
       	          		.replace((char)0x1f, ' ')
       	          	.replace((char)0x1f, ' '));
            	        
         	         	  }catch (Exception ex) {
         	         		ex.printStackTrace();
         	         	  }
         	          }

         	    	counter++;
         	   }
         	   
            }
           
           
           
           System.out.println("l�petatud klientide moodustamine" );

       
       }catch(Exception e){
       	e.printStackTrace();
       }
//System.exit(0);
	   
   }

   private  Node getNode(Node node, String name){
	    for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	      Node nd = node.getChildNodes().item(i);
	  //    System.out.println(nd.getNodeName());
	      if (nd.getNodeName().equals(name)){
	        return nd;
	      }
	    }
	    return null;
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

private boolean useOldData = false;
private TreeMap agent1 = new TreeMap();



public TreeMap getGrupidPayer(){
    TreeMap tm = new TreeMap();
    
  try {
	   
	   FileListFilter fil = new FileListFilter("kliendiderp","xml");
   	 String[] ll = new File (getDir()).list(fil);
   	 for (int i = 0; i < ll.length; i++) {
   		
   		
   		 String name = ll[i];
   		// System.out.println(name);
		  File f = new File (getDir() + name);
	   
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "document");      
    	Node read = getNode(root, "records");
    	 for (int s = 0; s < read.getChildNodes().getLength(); s++){
        	  Node rida = read.getChildNodes().item(s);
        	  try{
        		 if(rida.getNodeName().equals("item")){
        			boolean goes = true;
        		  String id =  deCode(getNode(rida,"id").getFirstChild().getNodeValue());
        		
	                 String prlist = null;
	                 try{ 
	 	         		 prlist =   getNode(rida,"priceListID").getFirstChild().getNodeValue();
	 	         		 if(prlist.equals("0")){
	 	         			prlist = null; 
	 	         		 }
	 	        	 }catch(Exception ee){
		        	 } 
	                 if(prlist == null){
	                	 prlist = "";
	                 }else{
	                	 prlist = prlist + "|";
	                 }
	                 
	                 String prlist2 = null;
	                 try{ 
	 	         		 prlist2 =   getNode(rida,"priceListID2").getFirstChild().getNodeValue();
	 	         		if(prlist2.equals("0")){
	 	         			prlist2 = null; 
	 	         		 }
	 	        	 }catch(Exception ee){
		        	 } 
	                 if(prlist2 != null){
	                	 prlist = prlist + prlist2 + "|";
	                 }
	                 
	                 String prlist3 = null;
	                 try{ 
	 	         		 prlist3 =   getNode(rida,"priceListID3").getFirstChild().getNodeValue();
	 	        	 }catch(Exception ee){
		        	 } 
	                 if(prlist3.equals("0")){
 	         			prlist3 = null; 
 	         		 }
	                
	                 if(prlist3 != null){
	                	 prlist = prlist + prlist3 + "|" ;
	                 }
	                 
	                 String prlist4 = null;
	                 try{ 
	 	         		 prlist4 =   getNode(rida,"priceListID4").getFirstChild().getNodeValue();
	 	        	 }catch(Exception ee){
		        	 } 
	                 if(prlist4 != null && prlist4.equals("0")){
 	         			prlist4 = null; 
 	         		 }
	                 
	                 if(prlist4 != null){
	                	 prlist = prlist + prlist4 + "|" ;
	                 }
	                
	                 
	                 String prlist5 = null;
	                 try{ 
	 	         		 prlist5 =   getNode(rida,"priceListID5").getFirstChild().getNodeValue();
	 	        	 }catch(Exception ee){
		        	 } 
	                 if(prlist5!= null && prlist5.equals("0")){
 	         			prlist5 = null; 
 	         		 }
	                 
	                 if(prlist5 != null){
	                	 prlist = prlist + prlist5 ;
	                 }
	                 
	                
	                 if(prlist.endsWith("|")){
                   	 prlist = prlist.substring(0, prlist.length()-1);
                    }
	                 
                    if(prlist.length() >0 ){
                   	 tm.put(id, prlist);
                   	
                    }
	          
        		 
        			 
	                   
        		 }
        	  }catch(Exception e){
        		  e.printStackTrace();
        	  }
        
   	  }    
        }
	}catch(Exception e){
		e.printStackTrace();
	}
   
 

  
  
return tm;
}

private TreeMap afterOrders = null;

   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     boolean makedata = true;
	     String repManas = null;
	     
	     
	     
	     
	     
	     try {
	  		  File f = new File (getDir() + "deltype.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	      	StringBuffer sb = new StringBuffer();
	        Node root = getNode(doc, "document");      
	         // System.out.println(root);
	    //    sb.append("select; Vali tarneviis\n");
	       	Node read = getNode(root, "records");
	      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("item")){
	          	  try{
	          		 String group  = getNode(rida,"deliveryTypeID").getFirstChild().getNodeValue();
              		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
              		 group = deCode(group);
              		  name  =  deCode(name);
              		sb.append(group + ";" + name + "\n");
                       
	          	  }catch (Exception r){
	          		  r.printStackTrace();
	          	  }
	          	  }             	  
                }
	    
	           try{
	               
	            	  Writer out = new BufferedWriter(new OutputStreamWriter(
	       			    new FileOutputStream("shipmode.txt"), "UTF-8"));
	       			try {
	       			    out.write(sb.toString());
	       			} finally {
	       			    out.close();
	       			}
	             }catch(Exception r){
	             	
	             }
	     
	     }catch (Exception r){
	          		r.printStackTrace();  
	          	  }
	    
	     
	
	    
	     if( getParent().getProps().getProperty("replace.managers") != null){
	    	 repManas = getParent().getProps().getProperty("replace.managers") ;
	     }
	     
		 if("true".equals(getParent().getProps().getProperty("use.olddata")) || useOldData){
			 makedata = false;
		 }
		 
		 if(rcompany != null){
			Converter.getManagersReplacements(agent1, rcompany, user , pass);
		 }
		 
	     if(makedata){
	    	 makeData();
	 	 }
	     
	     
	     getDebt2();
	     StringBuffer sb2 = new StringBuffer();
	   
	     TreeMap groups = this.getGrupid();
	     TreeMap groupsPayer = this.getGrupidPayer();
	     sortimendid = new HashMap();
		  ketid = new HashMap();
	     
	     this.customers = new ArrayList();
	   
	   try {
		   
		   FileListFilter fil = new FileListFilter("kliendiderp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String name = ll[i];
	    		// System.out.println(name);
	 		  File f = new File (getDir() + name);
		   
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "document");      
	     	Node read = getNode(root, "records");
	     	 for (int s = 0; s < read.getChildNodes().getLength(); s++){
	         	  Node rida = read.getChildNodes().item(s);
	         	  try{
	         		 if(rida.getNodeName().equals("item")){
	         			boolean goes = true;
	         		  String id =  deCode(getNode(rida,"customerID").getFirstChild().getNodeValue());
	         		  Customer customer = new Customer(id);
	         		  try{
	         			 String kett = deCode(getNode(rida,"code").getFirstChild().getNodeValue());;
		                 if(kett != null && kett.trim().length()>0){
		              	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
		                 		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett));
		                 		vv.add(customer.getID());
		                 		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
		                 	}else{
		                 		Vector vv = new Vector();
		                 		vv.add(customer.getID());
		                 		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
		                 	}
		                 }
	                    	  
	         		  }catch(Exception e){
	        
	         		  }
	         		
	         		  try{
	         		  String namec =   deCode(getNode(rida,"fullName").getFirstChild().getNodeValue());
	         		  customer.setName(Converter.enCodeEst(namec));
	         		  
                         }catch(Exception ee){
	                     customer.setName(id); 
                         }
                        
	         		  if(this.overDue.get(customer.getID()) != null){
	         			  customer.setDebt(overDue.get(customer.getID()));
	         			//System.out.println("Siin on yle olemas "  + overDue.get(customer.getID()) + " " + customer.getID() );
  	                 	
	         		  }
	         		  
                         String grp = (String)groups.get(id);
    	                 if(grp != null){
    	                 	//System.out.println("Siin on grupp olemas "  + grp + " " + customer.getID() );
    	                 	Vector vv = new Vector();
    	                 	StringTokenizer st = new StringTokenizer(grp,"|");
    	                 	while (st.hasMoreElements()){
    	                 		String gg = st.nextToken();
    	                 		vv.add(Converter.cleanLitLatEst(gg));
    	                 		sortimendid.put(customer.getID(), Converter.cleanLitLatEst(gg));
    	                 	}
    	                 	customer.setFixSortCode(vv);
    	                 //	System.out.println(customer.getName() + " " + vv.toString());
    	                 //	cust.setGroup(grp);
    	                 }
    	                 String prlist = null;
    	                 try{ 
    	 	         		 prlist =   getNode(rida,"priceListID").getFirstChild().getNodeValue();
    	 	         		 if(prlist.equals("0")){
    	 	         			prlist = null; 
    	 	         		 }
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist == null){
    	                	 prlist = "";
    	                 }else{
    	                	 prlist = prlist + "|";
    	                 }
    	                 
    	                 String prlist2 = null;
    	                 try{ 
    	 	         		 prlist2 =   getNode(rida,"priceListID2").getFirstChild().getNodeValue();
    	 	         		if(prlist2.equals("0")){
    	 	         			prlist2 = null; 
    	 	         		 }
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist2 != null){
    	                	 prlist = prlist + prlist2 + "|";
    	                 }
    	                 
    	                 String prlist3 = null;
    	                 try{ 
    	 	         		 prlist3 =   getNode(rida,"priceListID3").getFirstChild().getNodeValue();
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist3.equals("0")){
 	 	         			prlist3 = null; 
 	 	         		 }
    	                
    	                 if(prlist3 != null){
    	                	 prlist = prlist + prlist3 + "|" ;
    	                 }
    	                 
    	                 String prlist4 = null;
    	                 try{ 
    	 	         		 prlist4 =   getNode(rida,"priceListID4").getFirstChild().getNodeValue();
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist4 != null && prlist4.equals("0")){
 	 	         			prlist4 = null; 
 	 	         		 }
    	                 
    	                 if(prlist4 != null){
    	                	 prlist = prlist + prlist4 + "|" ;
    	                 }
    	                
    	                 
    	                 String prlist5 = null;
    	                 try{ 
    	 	         		 prlist5 =   getNode(rida,"priceListID5").getFirstChild().getNodeValue();
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist5!= null && prlist5.equals("0")){
 	 	         			prlist5 = null; 
 	 	         		 }
    	                 
    	                 if(prlist5 != null){
    	                	 prlist = prlist + prlist5 ;
    	                 }
    	                 
    	                 if(prlist.endsWith("|")){
                        	 prlist = prlist.substring(0, prlist.length()-1);
                         }
    	                 
    	                 try{
 		  	         		String payer  =  getNode(rida,"payerID").getFirstChild().getNodeValue();
 		         		            customer.setPayer(payer);
 		         		          if(groupsPayer.get(payer) != null) {
 		         		        	  if(prlist == null) {
 		         		        		  prlist = (String)groupsPayer.get(payer) ;
 		         		        	  }else {
 		         		        		  
 		         		        		 StringTokenizer st = new StringTokenizer((String)groupsPayer.get(payer), "|"); 
 		         		        		  while(st.hasMoreTokens()) {
 		         		        			  String ss = st.nextToken();
 		         		        			  if(!prlist.contains(ss)) {
 		         		        				 prlist = prlist + "|" + ss ; 
 		         		        			  }
 		         		        		
 		         		        		  }
 		         		        		
 		         		        	  
 		         		        	  }
 		         		        
 		         		          }
 		  	         		 }catch(Exception ee){
 		  		        	 }
 		  	         
    	                 
    	                 
    	                
                     
    	                 
                         if(prlist.length() >0 ){
                        	 customer.setGroup(prlist);
                        	 if("true".equals(getParent().getProps().getProperty("usepricematrix.fixed"))){
                        		 StringTokenizer st = new StringTokenizer(prlist,"|");
                        		 while(st.hasMoreTokens()){
                        			 customer.addFixSortCode(st.nextToken()); 
                        		 }
                     			
                     		}
                        	
                         }
    	                 
	         		  try{ 
	 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
	 	         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(deCode(aadr)),"Estonia"));
	         		 }catch(Exception ee){
		         		 customer.setAddress(""); 
	         		 }
	         		
	         		 try{ 
	 	         		 String mobile =   getNode(rida,"mobile").getFirstChild().getNodeValue();
	 	         		 if (mobile != null && mobile.trim().length() > 0){
	 	         			 customer.restPhone = mobile;
	 	         		 }
	 	         	 }catch(Exception ee){
		         		 
	         		 }
	         		try{ 
	 	         		 String vatNumber =   getNode(rida,"vatNumber").getFirstChild().getNodeValue();
	 	         		 if (vatNumber != null && vatNumber.trim().length() > 0){
	 	         			 customer.setVatnr(vatNumber);
	 	         		 }
	 	         	 }catch(Exception ee){
		         		 
	         		 }
	         		
	         		try{ 
	 	         		 String street =   getNode(rida,"street").getFirstChild().getNodeValue();
	 	         		 if (street != null && street.trim().length() > 0){
	 	         			 customer.restStreet = (street);
	 	         		 }
	 	         	 }catch(Exception ee){
		         		 
	         		 }

	         		try{ 
	 	         		 String city =   getNode(rida,"city").getFirstChild().getNodeValue();
	 	         		 if (city != null && city.trim().length() > 0){
	 	         			 customer.restCity = (city);
	 	         		 }
	 	         	 }catch(Exception ee){
		         		 
	         		 }try{ 
	 	         		 String postalCode =   getNode(rida,"postalCode").getFirstChild().getNodeValue();
	 	         		 if (postalCode != null && postalCode.trim().length() > 0){
	 	         			 customer.restZipCode = (postalCode);
	 	         		 }
	 	         	 }catch(Exception ee){
		         		 
	         		 }
	         		
	         		//<country>Eesti</country>
		         		

		         		  try{ 
			 	         		 Node aadr =   getNode(rida,"addresses");
			 	         		
			 	         		 for (int ss = 0; ss < aadr.getChildNodes().getLength(); ss++){
			 	         			
			 	  	         	  Node uuu = aadr.getChildNodes().item(ss);
			 	  	         		 if(uuu.getNodeName().equals("item")){
			 	  	         		  String addrID =  deCode(getNode(uuu,"addressID").getFirstChild().getNodeValue());
					 	        	//  System.out.println("aadrID " + addrID);
				         			 }
			 	  	         	
				         		 
			         		 }
			 	  	          
			 	         		 
		         		 }catch(Exception ee){}
		         		 
	         	    

		         		  try{ 
			 	         		 Node conta =   getNode(rida,"contactPersons");
			 	         		
			 	         		 for (int ss = 0; ss < conta.getChildNodes().getLength(); ss++){
			 	         			
			 	  	         	  Node uuu = conta.getChildNodes().item(ss);
			 	  	         		 if(uuu.getNodeName().equals("item")){
			 	  	         		  String namec =  deCode(getNode(uuu,"fullName").getFirstChild().getNodeValue());
			 	  	         		  if(namec != null){
			 	  	         			  
			 	  	         			  if(namec.startsWith(",")) {
			 	  	         				  namec = namec.substring(1).trim();
			 	  	         			  }

			 	  	         			  if(namec.endsWith(",")) {
			 	  	         				  namec = namec.substring(0,namec.length()-1).trim();
			 	  	         			  }
			 	  	         		  String cid =  deCode(getNode(uuu,"contactPersonID").getFirstChild().getNodeValue());
			 	  	         		 String phonec1 =  null ;
			 	  	         		 try{
			 	  	         		    phonec1 = deCode(getNode(uuu,"phone").getFirstChild().getNodeValue());
			 	  	         		 }catch(Exception e){
			 	  	         			 
			 	  	         		 }
			 	  	         		 
			 	  	         		String phonec2 =  null;
			 	  	         	 try{
				 	  	         		phonec2 = deCode(getNode(uuu,"mobile").getFirstChild().getNodeValue());
				 	  	         		 }catch(Exception e){
				 	  	         			 
				 	  	         		 }
			 	  	         		String add = Converter.enCodeEst(namec);
			 	  	         		
			 	  	         		if(phonec1 != null ){
			 	  	         			add = add +"|Tel: " + phonec1;
			 	  	         		}
			 	  	         	if(phonec2 != null ){
		 	  	         			add = add +"|Tel: " + phonec2;
		 	  	         		}
			 	  	         	Date synna = null;
	 	                	   String nimi = namec;
	 	                	   String synn = deCode(getNode(uuu,"birthday").getFirstChild().getNodeValue());
	 	                	   if(synn == null || synn.indexOf("0000") != -1){
	 	                		  synna = new java.sql.Date(0);
	 	                	   }else {
	 	                		  
	 	                		 synna = getDate(synn);
	 	                	   }
	 	                	   
	 	                	  String phone = phonec1;
	 	                	  if(phone == null) {
	 	                		  phone = " ";
	 	                	  }
	 	              //	  String cell = parent.make(rs1.getString("cellularphone"));
	 	                	  
	 	                	  
	 	                	 String mail =  null;
		 	  	         	 try{
			 	  	         		mail = deCode(getNode(uuu,"email").getFirstChild().getNodeValue());
			 	  	         		 }catch(Exception e){
			 	  	         			 
			 	  	         		 }
		 	  	         	 if(mail == null || mail.length() < 5) {
		 	  	         		 mail = " ";
		 	  	         	 }
	 	                	  
	 	                	  
	 	                    sb2.append(customer.getID()+";"+ nimi + ";" + synna.getTime() + "; ;" + phone  + ";" + mail + ";" + cid + "\n");
	 	                  
			 	  	         		  
			 	  	         		  
			 	  	         		if(customer.getPersonalData().trim().length() > 2 ){
			 	  	         		  customer.setPersonalData(customer.getPersonalData() + "|" + add);
			 	  	         		}else{
			 	  	         		  customer.setPersonalData(add);	
			 	  	         		}
			 	  	         		  }
			 	  	         		  //  System.out.println("aadrID " + addrID);
				         			 }
			 	  	         	
				         		 
			         		 }

			 	                 
			 	                 	

			 	         		 
		         		 }catch(Exception ee){}
		         		  
		         		  
		         		  
	         		  try{ 
	 	         		 String type =   getNode(rida,"customerType").getFirstChild().getNodeValue();
	 	         		  if(!type.equals("COMPANY")){
	 	         			  goes = false;
	 	         		  }
	         		 }catch(Exception ee){
		         	 
	         		 }
	         		  try{
	         		   String salesman  = deCode( getNode(rida,"customerManagerID").getFirstChild().getNodeValue());
       		            customer.addAgent(new Agent(salesman));
       		            this.custtsUsed.add(id);
       		            
       		       
       		        if (salesman != null) {
       		        	//customer.setMainAgent( salesman.trim());
       		        	if(repManas != null){
       		        		StringTokenizer st = new StringTokenizer(repManas,",");
       		        		while (st.hasMoreTokens()){
       		        			customer.addAgent(new Agent(st.nextToken()));	
       		        		}
       		        	}
       		        	
       		         if ( salesman != null && agent1.get( salesman.trim()) != null){
       		                      Agent aa = (Agent) agent1.get( salesman.trim());
       		                       Vector vec = aa.getDubbler();
       		                       if( vec!= null && vec.size() > 0 ){
       		                            Enumeration en = vec.elements();
       		                            while(en.hasMoreElements()){
       		                                 String ag = (String) en.nextElement();
       		                              customer.addAgent( new Agent( ag));
       		                           }
       		                      }
       		                }
       		        }

       		            
       		            
	         		   }catch(Exception ee){
		         		 
	         		   }
	         		  customer.addAgent(new Agent("HOUSE"));
	         		 if("true".equals(getParent().getProps().getProperty("all.customers"))){
	         			Enumeration enume = getParent().getAgentsHash().elements();
	         				while(enume.hasMoreElements()){
	         					Agent a = (Agent)enume.nextElement();
	         					customer.addAgent(a);
	         				}
	         		}
	         		 
	         		 
	         		 try{
		         		  String email =   deCode(getNode(rida,"email").getFirstChild().getNodeValue());
		         		  if(email.length() > 0 ){
		         			 if(getParent().getProps().getProperty("confirm.email.domain") != null){
			         			 String ems = getParent().getProps().getProperty("confirm.email.domain");
			         			 
				         			StringTokenizer st = new StringTokenizer(ems,",");
				         			while (st.hasMoreTokens()){
				         				if(email.contains(st.nextToken())){
				         					customer.setConfirmEmail(email);	
				         					//customer.setConfirmEmail("aivar@mobilejava.ee");	
				         				}
				         			}
				         		 }  
		         		  }
		         		
		         		  
	                         }catch(Exception ee){
		                    
	                         }
	         		 
	         		 
	         		 
	         		 if(getParent().getProps().getProperty("all.customers.list") != null){
	         			 String ags = getParent().getProps().getProperty("all.customers.list");
	         			 
		         			StringTokenizer st = new StringTokenizer(ags,",");
		         			while (st.hasMoreTokens()){
		         				Agent a = new Agent(st.nextToken());
		         					customer.addAgent(a);
		         			}
		         			
		         		 }
	         		  
	         		  try{
	  	         		String paym  =  getNode(rida,"paymentDays").getFirstChild().getNodeValue();
	         		            customer.setPaymTerm(paym);
	  	         		 }catch(Exception ee){
	  		        	 }
	  	
	         		  
	          		 try{
		         			Node attr = getNode(rida, "attributes");
		         			if(attr != null){
		         			
		         			//	System.out.println("ridu  " + attr.getChildNodes().getLength() + attr.toString());
			        	         for (int u = 0; u < attr.getChildNodes().getLength(); u++){
			        	        	 try{
			        	        	 	 Node item = attr.getChildNodes().item(u);
				        	      //   System.out.println("name" + item.getNodeName());
				        	        //	System.out.println("value " + getNode(item,"attributeName").getFirstChild().getNodeValue());
				        	        	
				        	            String glnkood  =  getNode(item,"attributeName").getFirstChild().getNodeValue();
				    		            if(glnkood.equals("clattr_gln")){
				    		            	glnkood  =  getNode(item,"attributeValue").getFirstChild().getNodeValue();
				    		            	// this.ilns.put(glnkood.trim(), id);
				    		            	customer.GLNCode = glnkood;
				    		            	// System.out.println("olemas " + glnkood + " " + id);
				    		            }
				        	        	
				        	        	
				        	        	
			        	        	 }catch(Exception e){
			        	        		//e.printStackTrace(); 
			        	        	 }
			        	        }
			        	         	 
			         			 
				 
			    		           
		         				
		         				
		         			}
		        	     
			 	         		
			         		   }catch(Exception ee){
				         		 
			         		   }
		         		 
	         		  
	         		  
	         		  
	         		  
	         		  
	          		 
	          		 
	          		
	          		 try{
		  	         		double actualBalance  =  Double.parseDouble(getNode(rida,"actualBalance").getFirstChild().getNodeValue());
		         		           // customer.setUsedCredit(actualBalance);
		  	         		 }catch(Exception ee){
		  		        	 }
	          		
	          
	         		  
	         		
		  	         		
		  	         		  if(getParent().getProps().getProperty("country") != null){
		  	         			 try{
		 		  	         		String country  =  getNode(rida,"country").getFirstChild().getNodeValue();
		 		  	         	
		 		  	         	if(country != null && country.trim().length() > 0 ){
		 		  	         	  customer.setCountry(country);
		 		  	         	
	 		  	         	    }
		 		  	         		     if(country != null && country.trim().length() > 0 && 
		 		         		        		 getParent().getProps().getProperty("country").indexOf(country) == -1){
		 		         		        	 goes = false;
		 		         		         }
		 		  	         		 }catch(Exception ee){
		 		  	         			 
		 		  		        	 }  
		  	         		  }
		  	         		  
		  	         		
	                        	 if("true".equals(getParent().getProps().getProperty("chain.fromgroup"))){
	                        		 try {
	                        				String groupName  =  getNode(rida,"groupName").getFirstChild().getNodeValue();
	    	 		 		  	         		 customer.setChain(groupName.trim());
	                        		 } catch(Exception t) {
	                        			 
	                        		 }
	 		 		  	         	 
	                     		}
	                        	
	                         
		  	         		  
		  	         		  
		  	         		  
		  	         		 
	         		 if(goes){
	         			  tm.put(customer.getName(), customer);
		                     this.customers.add(customer.getID()); 
	         		 }
	         		   
	                     
	         		 }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	    	  }    
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
	   try {
	    	 getOutDir();
	 		Converter.makeTelemaFiles(this.getOutDir(), null, null, tm, ownGLN);
	 	} catch (Exception e) {
	 		
	 		e.printStackTrace();
	 	}
		 
	   
	   if(sb2.length() > 2){
    		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
    	}

	   
	   
     return tm;
   }

 
   public static  String makeNumeric(String s){
	    if (s == null) return s;
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0 ; i < s.length() ; i++){
	      char c = s.charAt(i);
	      if (Converter.isNumeric(c)){
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }
 
   
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
       prodCodes = new Hashtable();
    Vector noGrups = new Vector();
    Vector noBrands = new Vector();
        prodHash = new HashMap();
        TreeMap brands = new TreeMap();
        boolean sortBR = false;
        String status = null;
        String statusnot = null;
        boolean soldGoes = false;
        boolean secgr = false;
        boolean groupmustbe = false;
        boolean masterKapp = false;
        HashSet kappProds = new HashSet();
        HashMap cdnPictures = null;
        
        boolean addpicts = false;
        String pictType = "";
        if(getParent().getProps().getProperty("usepictures") != null){
     	   addpicts = true;
     	   pictType = getParent().getProps().getProperty("usepictures");
        }
        
        if("true".equals(getParent().getProps().getProperty("cdn.pictures"))){
        	   sessioncdn = getSessionIdCDN("jwt=" +jwttoken);
            	System.out.println(sessioncdn);
            	
            	try {
					cdnPictures = getImagesCDN(sessioncdn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
        }

     
        
        if( getParent().getProps().getProperty("masterdir") != null){
        	masterKapp = true;
        }
        
        getSoldprods();  
      
        if("true".equals(getParent().getProps().getProperty("group.usesub"))){
        	secgr = true;
        }
        
        if("true".equals(getParent().getProps().getProperty("soldgoes"))){
        	soldGoes = true;
        }
        
        if("true".equals(getParent().getProps().getProperty("groupmustbe"))){
        	groupmustbe = true;
        }
        
    if(getParent().getProps().getProperty("nogrups") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogrups"),",");
    	while (st.hasMoreElements()){
    		String aa = st.nextToken();
    		if(secgr){
    			aa= this.addLeadingZero(aa);
    		}
    		noGrups.addElement(aa);
    	}
    }
    
    String priorLadu = null;
    if(getParent().getProps().getProperty("priorLadu") != null){
    	priorLadu = getParent().getProps().getProperty("priorLadu");
    }
  
    String priorLopp = null;
    if(getParent().getProps().getProperty("priorLopp") != null){
    	priorLopp = getParent().getProps().getProperty("priorLopp");
    }
  
    String priorEtte = null;
    if(getParent().getProps().getProperty("priorEtte") != null){
    	priorEtte = getParent().getProps().getProperty("priorEtte");
    }
    
  
	   
   
    if(getParent().getProps().getProperty("prodstatus") != null){
    	 status = getParent().getProps().getProperty("prodstatus");
    	
    }
    if(getParent().getProps().getProperty("prodstatusnot") != null){
   	 statusnot = getParent().getProps().getProperty("prodstatusnot");
   	
   }
    boolean stockZero = false ;
    if("true".equals(getParent().getProps().getProperty("stockZero"))){
    	stockZero = true;
    }
    
    Hashtable prices = null;
    if(getParent().getProps().getProperty("usepricelist") != null){
    	String pr = getParent().getProps().getProperty("usepricelist");
    	prices = getPricelists(pr);
    }

    boolean prbase = false;
    
    String pictroot = null; 
    
    if(getParent().getProps().getProperty("usepricelist2") != null && prices != null){
    	prbase = true;
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
   
    
    if("true".equals(getParent().getProps().getProperty("supplier.group"))){
        suppliersTM = new TreeMap();
    }
     
     StringBuffer code8 = new StringBuffer();
     
     HashSet prodsInWares = new HashSet();
    // System.out.println("masterKapp "+ masterKapp);
    if(masterKapp){
    	try {
    		
    		  File f = new File (getDir() + "laoderp.xml");
    	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	     DocumentBuilder db;
    		db = dbf.newDocumentBuilder();      	
        	FileInputStream in = getCleanInputStream(f);
        	String handler = null;
            Document doc = db.parse(in,handler);
        	StringBuffer lll = new StringBuffer();
          Node root = getNode(doc, "document");      
           // System.out.println(root);
         	Node read = getNode(root, "records");
        // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
             for (int i = 0; i < read.getChildNodes().getLength(); i++){
             	  Node rida = read.getChildNodes().item(i);
             	  if(rida.getNodeName().equals("item")){
            	  
             		    
                 		 String ladu  = getNode(rida,"warehouseID").getFirstChild().getNodeValue();
                 		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
            	  lll.append(ladu + "\t" + Converter.deCode(name) + "\r\n");
             	  }
             }
            	 
             
            // System.out.println("masterKapplll "+ lll);
         	
            if(lll.length() > 0){
           	  Converter.saveFileInUTF8(getMasterDir() + "locations.csv", lll.toString());
            }
            	  }catch(Exception e){
              		e.printStackTrace();
              	}
    	
    	  if(getParent().getProps().getProperty("kapid") != null){
            StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("kapid"),",");
              while(st.hasMoreTokens()){
            	  String ladu = st.nextToken();
            	 TreeMap<String,Double> la =  this.getWarehouseStock(ladu);
            	 
            	// System.out.println("la suurus " + la.size());
            	Iterator itera = la.keySet().iterator();
            	while(itera.hasNext()){
            		String kood = (String)itera.next();
            		if(!prodsInWares.contains(kood)){
            			prodsInWares.add(kood);
            		}
            	}
            	  
              }
    	    }
              	      
    }
    
    
     try {
    	 StringBuffer lllitem = new StringBuffer();
    	 StringBuffer lllean = new StringBuffer();
    	  	  FileListFilter fil = new FileListFilter("tootederp","xml");
    	 String[] ll = new File (getDir()).list(fil);
    	 for (int i = 0; i < ll.length; i++) {
    		
    		 String name = ll[i];
 		  File f = new File (getDir() + name);
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
     	
        Node root = getNode(doc, "document");      

      	Node read = getNode(root, "records");
        if(read != null) {
        
        for (int s = 0; s < read.getChildNodes().getLength(); s++){
          	  Node rida = read.getChildNodes().item(s);
          	  if(rida.getNodeName().equals("item")){
         	  try{
         		  
         		 
              		 String code  = deCode(getNode(rida,"productID").getFirstChild().getNodeValue());
              		 Product prod = new Product(code);
              		 boolean goes = true ;
              		 String kood = "";
              		 try{
              			 kood = deCode(getNode(rida,"code").getFirstChild().getNodeValue()); 
              		
              		 }catch (Exception e){
              			 
              		 }
              		try{
             			String kood8 = deCode(getNode(rida,"code8").getFirstChild().getNodeValue());
             			if(kood8 != null && kood8.trim().length() > 0) {
             				StringTokenizer st = new StringTokenizer(kood8,",");
             				code8.append(code);
             				while(st.hasMoreTokens()) {
             					String hh = st.nextToken();
             					hh = hh.replaceAll("\\s","");
             					code8.append(";"+ hh);
             				}
             				code8.append("\n");
             			}
             		
             		 } catch (Exception e){
             			 
             		 }
              		
              		String ean  = null;
              		String add = code;
              		 if("true".equals(getParent().getProps().getProperty("name.code.none"))){
              			 add = "";
              		 }
              		
              		 try{
               		 ean = getNode(rida,"code2").getFirstChild().getNodeValue();
                   		if(ean != null){
                   			prod.setEAN(ean.replaceAll(";", ","));
                   		 if("true".equals(getParent().getProps().getProperty("add.ean"))){
                   			add = ean.substring(ean.length()-4); 
                   		 }
                   		
                   		}	 
               		 }catch(Exception r){
               			 
               		 }
               	String pre = "";
            
               	boolean noway = false ;
               	
               	
              		  try{
          				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
          			
          				if((prod.getStock() < 1 && sta.equals("NO_LONGER_ORDERED")) || sta.equals("NOT_FOR_SALE") ){
          					goes = false;
          					noway = true;
          				}
          				if( sta.equals("NO_LONGER_ORDERED") ){
              					prod.setStatusSellout("1");
          				}
          				if( sta.equals("NOT_FOR_SALE") ){
          					pre = "xPRE "; 
      				}
          				
          				
                    }catch(Exception e){
          				
          			}
                	
              		
              		if(addpicts){
              			
              			
              			
              			if(cdnPictures != null && cdnPictures.get(code) != null) {
              				String fileN = (String)cdnPictures.get(code);
              				String url = "https://cdn.erply.com/images/" + company + "/" + fileN ;//+"?width=200&height=200";
              						//5153/3D-Agnesi-pasta-Casarecce-500g.jpg?width=200&height=200
              				
              				System.out.println("sees " + url);
              				prod.setPhotoid("phurl:" +url); 
              			} else {
              			
              			try {
              				 Node picts  =  getNode(rida,"images");
                          
              				
              				for (int e = 0; e < picts.getChildNodes().getLength(); e++){
              	          	  Node rr = picts.getChildNodes().item(e);
              	          	  if(rr.getNodeName().equals("item")){
              	          		for (int fs = 0; fs < rr.getChildNodes().getLength(); fs++){
                  	          	   Node rr0 = rr.getChildNodes().item(fs);
              	          			if(rr0.getNodeName().indexOf(pictType) != -1){
                        	          	String pictname  =  rr0.getFirstChild().getNodeValue();
                          	 	       int last = pictname.lastIndexOf("/");
                          	 	    	   //http://www.erply.net/files/20346/pictures/web/product_pict/prd_47548247oz.jpg

                          	 	   //    System.out.println(rr0.getNodeName() + "  " +pictname );
                          	 	    pictroot = pictname.substring(0,last+1);
                          	 	    pictname = pictname.substring(last +1);
                          	 	    int lp = pictname.lastIndexOf(".");
                                    prod.setPhotoid(pictname.substring(0,lp) + "|" +pictname.substring(lp));
                       	 	     //  System.out.println(pictroot + "  " +pictname );
                	          		}
                	          }
                      		   }
              				}
              			}catch(Exception e){
              			 //	e.printStackTrace();
              			}
              			
              			}
              			
              		}
              		
              		String price  =  getNode(rida,"price").getFirstChild().getNodeValue();
              		if(price != null){
              			prod.setPrice(Double.parseDouble(price));	
              		}
              		
              		if(prices != null && prices.get(prod.getID()) != null){
              			prod.setPrice(Double.parseDouble((String) prices.get(prod.getID())));	
              		}
              		
              		String vat  =  getNode(rida,"vatrate").getFirstChild().getNodeValue();
              		if(vat != null){
              			prod.setVat(vat);	
              		}
              		
              		
              		if(status != null){
              			try{
              				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
              				if(!sta.equals(status)){
              					goes = false;
              				}
                        }catch(Exception e){
              				
              			}
              		}
              		
              		
              		if(statusnot != null){
              			try{
              				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
              				if(sta.equals(statusnot)){
              					goes = false;
              				}
                        }catch(Exception e){
              				
              			}
              		}
              		
              		
              		
              		String group  =  getNode(rida,"groupID").getFirstChild().getNodeValue();
              		
              		String grori = group;
              		
              		if(group.trim().length() == 0 && groupmustbe){
              			goes = false;
              		}
              		 if(secgr){
               			 group = addLeadingZero(group); 
               		 }
              		 
              		
              		 
              		if(group != null && groupList != null){
              			String ghh = (String)groupList.get(group);
              			
              				
              				if(ghh != null){
              				prod.setGroup(ghh);	
                  			if(noGrups.contains(group)){
                  				goes = false;
                  			}	
                  			

                  			if(secgr && ghh.contains(".") ){//kui on subgroup kasutuesl
                  				StringTokenizer st = new StringTokenizer(ghh,".");
                  				prod.setGroup(st.nextToken());
                  				prod.setGroupSecond(ghh); 
                  	
                  				 if(getParent().getProps().getProperty("search.group") != null){
                          			 String aaaa = getParent().getProps().getProperty("search.group");
                          			if(prod.getGroup() != null && prod.getGroup().equals(aaaa)){
                          				add = makeNumeric(kood);
                          			//	System.out.println("add "  + add  + " " + prod.getID());
                                  		
                          			}
                          		 }
                  				// ainult viimase grupi panema 
                  			}
              				
              				}
              			
              		}
              		
              		 	if(mdGrupid != null && mdGrupid.get(grori) != null){
              		 		prod.setMaxDiscount(mdGrupid.get(grori));
              		 	//	System.out.println("oioi " );
              		 		
              		 	}
             
              		
              		try{
              			String unitName  =  getNode(rida,"unitName").getFirstChild().getNodeValue();
                  		if(unitName != null){
                  			prod.setUnit(deCode(unitName));	
                  		}	
              		}catch(Exception d){
              			
              		}
              		
              		

	
              		String active  =  getNode(rida,"active").getFirstChild().getNodeValue();
              	
              		if(active != null && !active.equals("1")){
              				goes = false;
              		}
              	
              		try{
              			
              			StringTokenizer st = new StringTokenizer(laoID,",");
              			while(st.hasMoreElements()){
              				String lll = st.nextToken();
                  			Node wares  =  getNode(rida,"warehouses");
                        	for (int e = 0; e < wares.getChildNodes().getLength(); e++){
              	          	  Node rr = wares.getChildNodes().item(e);
              	          	  if(rr.getNodeName().equals("item")){
              	          		for (int fs = 0; fs < rr.getChildNodes().getLength(); fs++){
                  	          	   Node rr0 = rr.getChildNodes().item(fs);
              	          			if(rr0.getNodeName().indexOf("warehouseID") != -1){
                        	          	String ladu   =  rr0.getFirstChild().getNodeValue();
                          	 	         if(ladu.equals(lll)){
                                       	String stock  =  getNode(rr,"totalInStock").getFirstChild().getNodeValue();
                                       	
                                       		if(stock != null ){
                                       			double seis = Double.parseDouble(stock);
                                       			if(seis > 0){
                                       				if(prod.getStock() > 0){
                                           				prod.setStock(prod.getStock() + seis);	
                                                 	}else{
                                           				prod.setStock(seis);	
                                                    }	
                                       			}
                                       			
                                       		}
                                       //	 System.out.println(rr0.getNodeName() + "  " + prod.getStock() ); 
                          	 	         }
                          	 	     
                          	 	    }
                	          }
                      		   }
              				}              				
              			}

              			
              		
              			
              			String stock  =  getNode(getNode(getNode(rida,"warehouses"),"item"),"totalInStock").getFirstChild().getNodeValue();
                  		if(stock != null){
                  			prod.setStock(Double.parseDouble(stock));	
                  		}	
              		}catch(Exception e){
              			
              		}
              		
              		if(stockZero){
              			if(prod.getStock()< 1){
              				prod.setStock(-1);
              			}
              		}
              		
              		try{
              			String kastis  =  getNode(getNode(getNode(rida,"packages"),"kast"),"amount").getFirstChild().getNodeValue();
                  		if(kastis != null){
                  			prod.setInCases(Double.parseDouble(kastis));	
                  		}	
              		}catch(Exception e){
              			
              		}
             		  
              		/*
              		try{
              			String karbis  =  getNode(getNode(getNode(rida,"packages"),"karp"),"amount").getFirstChild().getNodeValue();
                  		if(karbis != null){
                  			prod.setInCases(Double.parseDouble(karbis));	
                  		}	
              		}catch(Exception e){
              			
              		}*/
     
              		

              	    if("true".equals(getParent().getProps().getProperty("supplier.group"))){
              	         
              	      try{
                			String suppName  =  getNode(rida,"supplierName").getFirstChild().getNodeValue();
                    		if(suppName != null){
                    			prod.setGroupSecond(deCode(suppName));	
                    			suppliersTM.put(suppName, suppName);
                    		}	
                		}catch(Exception d){
                			
                		}
              	    }

              	    if(soldGoes && prod.getStock() < 0.1){
              	    	 goes = false;
              	    	if( soldProds.contains(kood) && !noway){
              	    		goes = true;
              	    		//System.out.println(kood);
              	         }
              	    }
              	    
              	    //kui status on l�pumyyk siis vaatame laoj��ki 

              		try{
          				String sta  =  getNode(rida,"priorityGroupID").getFirstChild().getNodeValue();
          			
          				if(priorLadu != null){
          					
          					goes = false;
          					
          					
          					if( sta.equals(priorLadu)){
            					  goes = true;
            				}
          					if(priorLopp != null && (prod.getStock() > 1 && sta.equals(priorLopp))){
              					  goes = true;
              				}
              				if( priorLopp != null  && sta.equals(priorLopp) ){
                  					pre = "xOUT "; 
                  						prod.setStatusSellout("1");
                  			}
              				if( priorEtte != null && sta.equals(priorEtte) ){
              					    pre = "xPRE "; 
              					    goes = true;
          				     }
              			}
          				
          				
          				
          				
                    }catch(Exception e){
          				
          			}
              	
     
             		
             		 String namec = null;
             		 try{
             		  namec  =  (getNode(rida,"name").getFirstChild().getNodeValue());
             		 if("true".equals(getParent().getProps().getProperty("name.code.front"))){
             			 prod.setName(pre + kood + " " + (namec) + " " +  add); //Converter.enCodeEst
             		 }else{
             			 prod.setName(pre + (namec) + " " + kood + " " + add); //Converter.enCodeEst
             		 }
             		
                     }catch(Exception r){
             			// r.printStackTrace();
             			 prod.setName("noname " + code);
             		 }
             		
             		String brand  =  getNode(rida,"brandID").getFirstChild().getNodeValue();
             		if(brand != null){
             		if(noBrands.contains(brand.trim())){
             					goes = false;
                 			}
             		
             		 if("true".equals(getParent().getProps().getProperty("brands.group"))){
             			 prod.setGroupSecond("2." + brand.trim());
             			 if(brands != null && namec != null &&!namec.startsWith("TST") && sortBR && brands.get("2."+ brand.trim()) != null){
             				 prod.setNameSorting((String)brands.get("2."+ brand.trim()) + prod.getName());
             			 }else{
             				 prod.setNameSorting("xxxx" + prod.getName()); 
             			 }
             		  }
             		}
             		//System.out.println(prod.getNameSorting());
             		
             	
             		if(prices != null && namec != null && prbase && namec.indexOf("TST") == -1 && prices.get(prod.getID()) == null){
             				goes = false;
             		}
             		
             		if(kood.indexOf("28188") != -1) {
             			System.out.println(prod.getName() + " 12 " + goes + " "+ grori);
             		}
             		
             		if(okGrupid != null && !okGrupid.contains(grori)){
             			goes = false;
             		  }
             		
             		
             		if(masterKapp && prodsInWares.contains(prod.getID()) ){ //&& kappProds.contains(prod.getID())
             			lllitem.append(prod.getID()+ "\t" + prod.getID()+  "\t" + prod.getName() + "\t" + prod.getEAN()
                           + "\t0\t0\r\n");
             			if(prod.getEAN() != null && prod.getEAN().length() > 4){
             				lllean.append(prod.getEAN()+ "\t" + prod.getID()+  "\t1\ttk.\t0\t"+ "\r\n");
             	    	}
             	 	}
             	
             	
             		
               if(goes){
         			 String sorting = prod.getID();
         			if(getParent().getProps().getProperty("sorting") != null){
         				if(getParent().getProps().getProperty("sorting").equals("nimi")){
         					sorting = prod.getName();
         				}
         				if(getParent().getProps().getProperty("sorting").equals("gruppnimi")){
         					sorting = prod.getGroup() + prod.getName();
         				}
         				if(getParent().getProps().getProperty("sorting").equals("gruppkood")){
         					sorting = prod.getGroup() + prod.getID();
         				}
         			}
         			  tm.put(sorting , prod); 
         			  prodHash.put(prod.getID(), prod);
         				prodCodes.put(kood.trim(), code);
         	
         			  
         		  }
                      
         	  }catch(Exception e){
         		  e.printStackTrace();
         	  }
          	  }     
         }
    	
        }
          
          
    	 }	 
    	 if(lllitem.length() > 0){
   	   	  Converter.saveFileInUTF8(getMasterDir() + "items.csv", Converter.deCode(lllitem.toString()));
   	    }
    	 if(lllean.length() > 0){
      	   	  Converter.saveFileInUTF8(getMasterDir() + "itembarcodes.csv", lllean.toString());
        }
    	 
    	 if("true".equals(getParent().getProps().getProperty("makeanalogs")) && code8.length() > 0) {
    		 Converter.saveFileInUTF8("prodanalogs.txt", code8.toString());
    	 }
     } catch (Exception e){
       e.printStackTrace();
     }
     
     
     
     try {
    
    	 
    
    	 getOutDir();
 		//Converter.makeTelemaFiles(this.getOutDir(), tm, this.getProdGroups(), null, ownGLN);
 	} catch (Exception e) {
 		
 		e.printStackTrace();
 	}

 	
     return tm;
   }

   private String ownGLN = null;
   private String getOutDir(){
       String dir = getParent().getProps().getProperty("readout");
       if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
       return dir;
     }

   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   
   private String getKlient(String kli){
	   String klient = null ;
	   return klient ;
   }

   public void getSoldprods(){
	  
	   soldProds = new HashSet();

		try {
	  		  File f = new File (getDir() + "klienditoodeErp.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "document");      
	       	Node read = getNode(root, "records");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("CSV")){
	          	  String csv = rida.getFirstChild().getNodeValue();
	          	  csv = csv.replaceAll("\"\"", "|");
	          
	          	  StringTokenizer st = new StringTokenizer(csv,"|");
	          	  st.nextToken();
	          	  while (st.hasMoreElements()){
	          		  String line = st.nextToken();
	          		 line = line.replaceAll("\";\"", "|");
	          		 line = line.replaceAll("\"", " ").trim();
	          		
	      //    		System.out.println("line  " + line);
	          	//	line  6276|8|Aurusti eGo Sigma|2013-06-29|6.6667|8|1
	          		 StringTokenizer stv = new StringTokenizer(line,"|");
	          		  while (stv.hasMoreElements()){
	          			
	          				 String toode =  " "; 
	 	          			 try{
		          				  toode = stv.nextToken().trim();
			          		  if(!soldProds.contains(toode)){
			          			soldProds.add(toode);
			          		  }
	          			  		 }catch(Exception e){
	          			 } } } }     
	              }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  
	  	   System.out.println("SoldProds size " + soldProds.size());
	  
   
   }
   
   private  Vector custProds = new Vector();
 
   public Vector getCustomerProductsList(String agentCode){
	 
	   getAfterOrders();
	   
	   HashSet afterOlemas = new HashSet();
	   
	   custProds = new Vector();
	   soldProds = new HashSet();

		try {
	  		  File f = new File (getDir() + "klienditoodeErp.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "document");      
	       	Node read = getNode(root, "records");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("CSV")){
	          	  String csv = rida.getFirstChild().getNodeValue();
	          	  csv = csv.replaceAll("\"\"", "|");
	          
	          	  StringTokenizer st = new StringTokenizer(csv,"|");
	          	  st.nextToken();
	          	  while (st.hasMoreElements()){
	          		  String line = st.nextToken();
	          		 line = line.replaceAll("\";\"", "|");
	          		 line = line.replaceAll("\"", " ").trim();
	          		
	      //    		System.out.println("line  " + line);
	          	//	line  6276|8|Aurusti eGo Sigma|2013-06-29|6.6667|8|1
	          		 StringTokenizer stv = new StringTokenizer(line,"|");
	          		  while (stv.hasMoreElements()){
	          			  String klient = (String)docCust.get(stv.nextToken().trim());
	          			  if (klient != null){
	          				 String toode =  " "; 
	 	          			
		          			 String kuupaev = " ";
		          			 double kog = 1;
		          			 try{
		          				  toode = stv.nextToken().trim();
			          			   stv.nextToken().trim();//String nimi =
		          			   kuupaev = stv.nextToken().trim().substring(5);
		          			   double lastpr = 0;
		          			   try{
		          				   lastpr = Double.parseDouble( stv.nextToken().trim());
		          			   }catch(Exception e){
		          				   
		          			   }
		          			double oamount = 0;
		          			   if(this.afterOrders != null && afterOrders.get(klient) != null) {
		          				// System.out.println("oamount olemas "  + klient + " " + toode + " " + oamount );
		          				   ArrayList<Toode> prods = (ArrayList<Toode>)afterOrders.get(klient);
		          				   for(Toode tt : prods) {
		          					   if(tt.getID().equals(toode)) {
		          						   oamount = tt.getKogus();
		          						 afterOlemas.add(klient+toode+oamount);
		          						 
		          						//   System.out.println("oamount olemas "  + klient + " " + toode + " " + oamount );
		          					   }
		          				   }
		          			   }
		          			   
		          			 stv.nextToken().trim();
		          			  
		          			 String kogus = stv.nextToken().trim();
		          			  kog = Double.parseDouble(kogus); 
		          			 
		          			  CustProd cp = new CustProd(klient, toode,kog,0,0,kuupaev,lastpr,oamount,0," ");
			          		   custProds.add(cp);
			          		   if(!soldProds.contains(toode)){
			          			   soldProds.add(toode);
			          		    } 
	  
	          			  		 }catch(Exception e){
	          			  			// e.printStackTrace();
	          			 }  } } } }     
	          }
	        
	        if(this.afterOrders != null) {
	        	Iterator itera = afterOrders.keySet().iterator();
	        	while (itera.hasNext()) {
	        		String klient = (String)itera.next();
	        	
	        		ArrayList<Toode> arr = (ArrayList<Toode>)afterOrders.get(klient);
   for(Toode t : arr) {
	   
	   CustProd cp = new CustProd(klient, t.getID(),0,0,0,t.getBrand(),t.getPlancur(),t.getKogus(),0," ");
	   cp.setAbiKood("BackOrder");
	   custProds.add(cp);
   }
    			 
        		   
	        	}
	        }
	        
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  
	  	   System.out.println("Cp size " + custProds.size());
	   return custProds;
   }

   private HashSet soldProds = null;
   
   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "ErplyLtt";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }


  private String getMasterDir(){
    String dir = getParent().getProps().getProperty("masterdir");
    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
    return dir;
  }
   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }
   
   private HashMap ketid = null;
   private HashMap sortimendid = null;
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private TreeMap groupList = new TreeMap();
   private TreeMap groupListSec = new TreeMap();
    
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private String session = "";
   private String sessioncdn = "";
   private String passString = ""; 
   private String username = null;
   private int onpage = 100;
   private String company = null;
   private String password = null;
   private String apiversion = null;
   private String returntype = null;
   private String userstuff = null;
     private URL myurl = null;
     private URL myurlcdn = null;

  /**
   * setBulkSales
   * 
   *
   * @param agents Map
   */
     double totalPlanSum = 0;
     
     private TreeMap makePlans(String fname){
   	   TreeMap res = new TreeMap();
   	      totalPlanSum = 0;
   	   try{
   		
   	   Calendar cal = Calendar.getInstance();
   	   int a = 0;
   	   File f = new File (getExcelDir() + fname);
   		  FileInputStream	 ff = new FileInputStream(f);
   		DataFormatter formatter = new DataFormatter();
		
   		XSSFWorkbook workbook = new XSSFWorkbook((ff));
   		XSSFSheet sheet = workbook.getSheetAt(0);
   		XSSFRow row = null; 
   				ff.close();	
   				if(sheet != null){
   					
   					String gr1 = null;
   					
   					for(int i = 1 ; i < sheet.getLastRowNum() + 1;i++){ // 9ndast reas hakkame scrollima nii kilosid kui ka summat
   						 row = sheet.getRow(i);
   						String agent = null;
   						double g1 = 0;
   						
   						try {
   							agent = formatter.formatCellValue(row.getCell((short) 1));
   						} catch (Exception e) {
      					//    	 System.out.println("agent ei  " + agent);
      						//  e.printStackTrace();
      					      }
   				//	  System.out.println("agent jah  " + agent + "  " +g1);
   						try {
   						g1 = Double.parseDouble(row.getCell((short) (2 + cal.get(Calendar.MONTH))).getNumericCellValue()+ "");
   					//  System.out.println("agent jah0  " + agent +" "+ g1);
   						} catch (Exception e) {
   	   					 e.printStackTrace();	
    					    }
   						
   					
   						if(agent != null) {
   							    row = sheet.getRow(i);
   						TreeMap asi= new TreeMap();
   				        Toode t1 = new Toode(gr1);
  						       t1.setPlancur(g1);
  						    //   System.out.println("agenthh " + agent + " " + g1 + " " );
  					    res.put(agent, t1);
   					  }
   					}
   						
   					}
   				
   				
           		   
   	   }catch (Exception e){
   		   e.printStackTrace();
   	   }
   	   
   	   System.out.println("totalPlanSum "+ res.toString() );
   	   return res;
      }
      
     
     private Hashtable docCust = null;
     private TreeMap plans = null; 
     
  public void setBulkSales(Map agents) {
	  TreeMap myygid = new TreeMap();
	  TreeMap myygideelm = new TreeMap();
	  docCust = new Hashtable();
	  
	  plans = this.makePlans("Budget.xlsx");
	  
	  int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	  
	  String mm = getMonthc().substring(0,7);
	  String mme = getMonthe().substring(0,7);
      FileListFilter fil = new FileListFilter("doklist","xml");
 	 String[] ll = new File (getDir()).list(fil);
 	 for (int u = 0; u < ll.length; u++) {
 		 String name = ll[u];
	    		//System.out.println(name);
	 		  File f = new File (getDir() + name);
	 		  
	 		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  	     try{
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        
	        Node root = getNode(doc, "document");      
	        //   System.out.println(root);
	       	Node read = getNode(root, "records");
	      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("item")){
	          
	          try{
				
	        String agent = getNode(rida, "employeeID").getFirstChild().getNodeValue();
				String date =  getNode(rida, "date").getFirstChild().getNodeValue();
				double total = Double.parseDouble(getNode(rida, "netTotal").getFirstChild().getNodeValue());
		     boolean confirmed = false ;
		     String con =  getNode(rida, "confirmed").getFirstChild().getNodeValue();
				if(con.equals("1")){
					confirmed = true;
				  }
				
				  String nimi =  getNode(rida, "clientName").getFirstChild().getNodeValue();
				  String number =  getNode(rida, "number").getFirstChild().getNodeValue();
				  String type =  getNode(rida, "type").getFirstChild().getNodeValue();
				  if(type.equals("INVOICE")) {
					  confirmed = false;
				  }
					
				
				if(date.startsWith(mm) && confirmed ){
		    	if(myygid.get(agent) != null){
		        	double hetk = Double.parseDouble((String)myygid.get(agent));
		        	myygid.put(agent,(total+ hetk)+"");
		        }else{
		        	myygid.put(agent,total+"");
		        }
		    	  if (agent != null && plans.get(agent) != null){
	                   Toode tt = (Toode)plans.get(agent);
	                   tt.setDonecur(tt.getDonecur() + total);
	                   }
			    }
				
				if(date.startsWith(mme) && confirmed ){
			    	if(myygideelm.get(agent) != null){
			        	double hetk = Double.parseDouble((String)myygideelm.get(agent));
			        	myygideelm.put(agent,(total+ hetk)+"");
			        }else{
			        	myygideelm.put(agent,total+"");
			        }
			    	
				    }
				
				if(!type.equals("INVOICE")) {
					 docCust.put(getNode(rida, "id").getFirstChild().getNodeValue(), 
				        		getNode(rida, "clientID").getFirstChild().getNodeValue());
				     	  }
				
		      
		          }catch(Exception e){
	          		  e.printStackTrace();
	          	  }
	           	}     
	          }
	           
	           
         	  }catch(Exception e){
         		  e.printStackTrace();
         		
         	  }
 	 }
         //	  System.out.println("myygid" + myygid.size() + " Doccust " + docCust.size());
 	         
	 		  Iterator itera = myygid.keySet().iterator();
	 		  while(itera.hasNext()){
	 			  String code = (String)itera.next();
	 			 Agent agent = (Agent)agents.get(code);
	 			Toode tt =   (Toode)plans.get(code);
	             if (agent != null){
	            	 
	            	 double teh = Double.parseDouble((String)myygid.get(code));
	            	   agent.setMonthSales((int) teh); 
	            	 String  asi = "";
	            	
	            	 if(tt != null) {
	            		 asi = (((int)(teh/tt.getPlancur()*100))+ "% Plan.: " +(int) tt.getPlancur() + " €  Sales.: " + (int) teh + " €||");
	     	     	 }
	            	 
	            	   if(myygideelm.get(code) != null && day < 6 ) {
	            		  asi = asi + ("|Last Month: " + (int) Double.parseDouble((String)myygideelm.get(code)));  
	            	   }
	            	   
	            	   System.out.println(agent.getID() + " " + asi);
	            	   agent.setStart(asi);
	             }
	          }
         }
    }
