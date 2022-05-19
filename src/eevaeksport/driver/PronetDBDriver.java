package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
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
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

public class PronetDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public PronetDBDriver(EksportPanel parent) {
    super(parent);
    

    this.subjectFilesTo = parent.getString("filemsg");
    url = getParent().getProps().getProperty("pronet.url");
  
   if(url == null){
	   url = "https://analytics.program.pronet-kr.si/api/rest/BP5";
   }
	try {
		myurl = new URL(url);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
	
   username = getParent().getProps().getProperty("pronet.user");	
 
   password = getParent().getProps().getProperty("pronet.pass");
  
   if(getParent().getProps().getProperty("pronet.onpage") != null){
	   try{
		   onpage = Integer.parseInt(getParent().getProps().getProperty("pronet.onpage"));
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	  
   }
   if(apiversion == null){
	   apiversion = "1.0";
   }
   returntype = getParent().getProps().getProperty("pronet.returntype");
   if(returntype == null){
	   returntype = "JSON";
   }
 

    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
    
    
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
  String url = null;
  
    
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





  private String getSessionId(String asi ){

	  String data = "clientCode=" + company + "&request=verifyUser" + asi ;
	//  System.out.println(data);
	  
	  try {
	String res = (postData(data)).toString();
//	System.out.println("res " + res);
	session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));

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
  private StringBuffer postProNetIds(String function, String method) throws Exception {
	  HttpURLConnection urlc = null;
	  
	
	  JSONObject body=new JSONObject();
	  Calendar cal = Calendar.getInstance();
	  String reqID = cal.getTimeInMillis()+"";
	  body.put("RequestID", reqID.substring(3));
	  body.put("Function", function);
	  JSONObject times= new JSONObject();
	  cal.add(Calendar.DATE, -4000);
	  times.put("addedBefore", Converter.jsonDate(new Date()));
	  times.put("modifiedBefore", Converter.jsonDate(new Date()));
	  times.put("modifiedAfter", Converter.jsonDate(cal.getTime()));
	  times.put("addedAfter", Converter.jsonDate(cal.getTime()));
	  
	  
	  
	  body.put("Parameters", times);
	  System.out.println(body.toJSONString());
	 
	  
	  
	  
	 	Converter.doTrustToCertificates();
	  StringBuffer inputLineBuf = new StringBuffer();
	  try {
			myurl = new URL(url+"/"+method);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	  try{
	  urlc = (HttpURLConnection) myurl.openConnection();
	  String encoded = Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));  //Java 8
	  urlc.setRequestProperty("Authorization", "Basic "+encoded);
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
		out.write( body.toJSONString().getBytes() );
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
  private StringBuffer postProNetData(String function, String method, JSONArray id) throws Exception {
	  HttpURLConnection urlc = null;
	  
	
	  JSONObject body=new JSONObject();
	  Calendar cal = Calendar.getInstance();
	  String reqID = cal.getTimeInMillis()+"";
	  body.put("RequestID", reqID.substring(3));
	  if (function != null) {
		  body.put("Function", function);
	  }
	  JSONObject params= new JSONObject();
	  if(id != null) {
		  params.put("ids", id );
	  }
	 if (id == null) {
		 Calendar a = Calendar.getInstance();
		 a.add(Calendar.YEAR, -1);
		 Calendar b = Calendar.getInstance();
		 b.add(Calendar.DATE, 3);
		 
		 params.put("startDate",  Converter.jsonDate(a.getTime()));
		 params.put("endDate", Converter.jsonDate(b.getTime()));
	 }
	  body.put("Parameters", params);
	  System.out.println(body.toJSONString());
	 
	  
	  
	  
	 	Converter.doTrustToCertificates();
	  StringBuffer inputLineBuf = new StringBuffer();
	  try {
			myurl = new URL(url+"/"+method);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	  try{
	  urlc = (HttpURLConnection) myurl.openConnection();
	  String encoded = Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));  //Java 8
	  urlc.setRequestProperty("Authorization", "Basic "+encoded);
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
		out.write( body.toJSONString().getBytes() );
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
  
  
  /*  "" : "2727",
  "productId" : "",
  "dateOfStock" : "" */
  
  private StringBuffer postProNetStockData(String function, String method, String id) throws Exception {
	  HttpURLConnection urlc = null;
	  
	
	  JSONObject body=new JSONObject();
	  Calendar cal = Calendar.getInstance();
	  String reqID = cal.getTimeInMillis()+"";
	  body.put("RequestID", reqID.substring(3));
	  body.put("Function", function);
	  JSONObject params= new JSONObject();
	  params.put("warehouseId", id );
	  params.put("productId", "" );
	  params.put("dateOfStock", "" );
	  
	  
	  
	  
	  body.put("Parameters", params);
	  System.out.println(body.toJSONString());
	 
	  
	  
	  
	 	Converter.doTrustToCertificates();
	  StringBuffer inputLineBuf = new StringBuffer();
	  try {
			myurl = new URL(url+"/"+method);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	  try{
	  urlc = (HttpURLConnection) myurl.openConnection();
	  String encoded = Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));  //Java 8
	  urlc.setRequestProperty("Authorization", "Basic "+encoded);
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
		out.write( body.toJSONString().getBytes() );
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
     
     try {
		   
		   FileListFilter fil = new FileListFilter("G1Toode","txt");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String fname = ll[i];
	    		//System.out.println(name);
	 		  File f = new File (getDir() + fname);
	 		  JSONObject obj = (JSONObject) this.readJsonFile(f);
	 		  JSONArray pGroups1 = (JSONArray) obj.get("ProductGroup1Data");
	 		 for(Object pgo: pGroups1) {
	 			 JSONObject pg = (JSONObject) pgo;
	 			 String id = (String) pg.get("id");
	 			String name = (String) pg.get("name");
	 			tm.put(id, name);
	 			 } 
	    	 }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
   
  	System.out.println("tm " + tm.size());
         return tm;
   }

   
   public TreeMap getProdGroupsSecond() throws SQLException {
	   
	   
	   TreeMap   tm = new TreeMap();
	     
	     try {
			   
			   FileListFilter fil = new FileListFilter("G2Toode","txt");
		    	 String[] ll = new File (getDir()).list(fil);
		    	 for (int i = 0; i < ll.length; i++) {
		    		
		    		
		    		 String fname = ll[i];
		    		//System.out.println(name);
		 		  File f = new File (getDir() + fname);
		 		  JSONObject obj = (JSONObject) this.readJsonFile(f);
		 		  JSONArray pGroups1 = (JSONArray) obj.get("ProductGroup2Data");
		 		 for(Object pgo: pGroups1) {
		 			 JSONObject pg = (JSONObject) pgo;
		 			 String id = (String) pg.get("id");
		 			String name = (String) pg.get("name");
		 			tm.put(id, name);
		 			 } 
		    	 }
		 	}catch(Exception e){
		 		e.printStackTrace();
		 	}
	   
	  	System.out.println("tm " + tm.size());
	         return tm;
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
   private Vector<JSONArray> getSplit(JSONArray ids, int split) {
	   Vector<JSONArray> vec = new Vector<JSONArray>();
	   int count = ids.size();
	   JSONArray out = new JSONArray();
	   int counter = 0;
	   for (Object obj : ids) {
		   out.add(obj);
		   counter = counter + 1;
		   if (counter == split) {
			   vec.add(out);
			   counter = 0;
			   System.out.println("Out sisu " + out.toString());
			   out = new JSONArray();
		   }

	   }
	   if (out.size() > 0) {
		   vec.add(out);
	   }
	   return vec;
   }
   private void saveWebFiles() {
	     try {
	    	
	    			JSONParser parser = new JSONParser();
	    			
	    			
	    				String asi =this.postProNetData(null ,"invoiceLines", null).toString();
	    				 parser = new JSONParser();
	    				 JSONObject json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi);
	    				 Converter.saveFile("Data/invoiceLines.txt", asi);
	    				
	    				 
	    					 asi =this.postProNetData(null ,"unpaidInvoices", null).toString();
		    				 parser = new JSONParser();
		    				 json = (JSONObject) parser.parse(asi);
		    				 System.out.println(asi);
		    				 Converter.saveFile("Data/debts.txt", asi);
		    				
	    				 
	    				 
	    			
	    		
	    			//kliendid
	    			asi =this.postProNetIds("getClientIds" ,"getIDs").toString();
	    			 json = (JSONObject) parser.parse(asi);
	    			JSONArray ids = (JSONArray) json.get("ClientIds");
	    			Vector<JSONArray> vec = getSplit(ids, 2000);
	    			int counter = 1;
	    		
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getClientData" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 3000));
	    				 Converter.saveFile("Data/klient"+ counter +".txt", asi);
	    				 if (counter == 3){
	    					// System.exit(0);
	    				 }
	    				 counter++;
	    			}
	    			
	    			//laod
	    			asi =this.postProNetIds("getWarehouseIds" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("WarehouseIds");
	    		     vec = getSplit(ids, 500);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getWarehouseData" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 500));
	    				 Converter.saveFile("Data/Ladu"+ counter +".txt", asi);
	    				 if (counter == 3){
	    				
	    				 }
	    				 counter++;
	    			}
	    			
	    			
	    			//agendilist
	    			asi =this.postProNetIds("getClientManagerListIds" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("ClientManagerListIds");
	    		     vec = getSplit(ids, 500);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getClientManagerListData" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi);
	    				 Converter.saveFile("Data/agent.txt", asi);

	    				
	    				 
	    				 counter++;
	    			}
	    			
	    			
	    			
	    			//getstock
	    			
	    			asi =this.postProNetIds("getWarehouseIds" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("WarehouseIds");
	    		
	    			 vec = getSplit(ids, 500);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetStockData("getStockData" ,"getStock", "7571").toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 500));
	    				 Converter.saveFile("Data/laoseis"+ counter +".txt", asi);
	    				 if (counter == 3){
	    				
	    				 }
	    				 counter++;
	    		}
	    			
	    			
	    			
	    			
	    			//ProductGroup2
	    			asi =this.postProNetIds("getProductGroup2Ids" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("ProductGroup2Ids");
	    		
	    			 vec = getSplit(ids, 500);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getProductGroup2Data" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 3000));
	    				 Converter.saveFile("Data/G2Toode"+ counter +".txt", asi);
	    				 if (counter == 3){
	    				
	    				 }
	    				 counter++;
	    		}
	    			
	    			
	    			
	    			
	    			
	    			//WholesalePriceList
	    			asi =this.postProNetIds("getRetailPricelistIds" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 System.out.println(asi.substring(0,500));
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("RetailPriceListIds");
	    		
	    			 vec = getSplit(ids, 1000);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getRetailPricelistData" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 3000));
	    				 Converter.saveFile("Data/KliendiHind"+ counter +".txt", asi);
	    				 if (counter == 3){
	    				
	    				 }
	    				 counter++;
	    			}
	    			
	    			
	    		
	    			
	    			
	    			
	    			//tooted
	    			 asi =this.postProNetIds("getProductIds" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("ProductIds");
	    			 vec = getSplit(ids, 1000);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getProductData" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 3000));
	    				 Converter.saveFile("Data/Toode"+ counter +".txt", asi);
	    				 if (counter == 3){
	    					// System.exit(0);
	    				 }
	    				 counter++;
	    			}
	    		
	    			
	    			
	    			
	    			
	    			
	    			//getProductGroup1Data
	    			asi =this.postProNetIds("getProductGroup1Ids" ,"getIDs").toString();
	    			 parser = new JSONParser();
	    			 json = (JSONObject) parser.parse(asi);
	    			 ids = (JSONArray) json.get("ProductGroup1Ids");
	    			 vec = getSplit(ids, 500);
	    			 counter = 1;
	    			for(JSONArray arr: vec) {
	    				asi =this.postProNetData("getProductGroup1Data" ,"getData", arr).toString();
	    				 parser = new JSONParser();
	    				 System.out.println(asi);
	    				 json = (JSONObject) parser.parse(asi);
	    				 System.out.println(asi.substring(0, 3000));
	    				 Converter.saveFile("Data/G1Toode"+ counter +".txt", asi);
	    				 if (counter == 3){
	    					// System.exit(0);
	    				 }
	    				 counter++;
	    		}
	     }
	    			catch (Exception e2) {
	    			// TODO Auto-generated catch block
	    			e2.printStackTrace();
	    		}
	  
	   
}
   
   
   
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // this.saveWebFiles();
 
    
     
     
     agent1 = new TreeMap();
     agents = new TreeMap();
     
    // this.getProducts(null);

     
 
     
     Agent sm = new Agent ("TEST");
		       		
		 sm.setName("ERIK");
     tm.put("TEST", sm); 
     
     
     try {
		   
		   FileListFilter fil = new FileListFilter("klient","txt");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String fname = ll[i];
	    		//System.out.println(name);
	 		  File f = new File (getDir() + fname);
	 		  JSONObject obj = (JSONObject) this.readJsonFile(f);
	 		  JSONArray customers = (JSONArray) obj.get("ClientData");
	 		 for(Object custo: customers) {
	 			 JSONObject cust = (JSONObject) custo;
	 			String manager = null;
	 			String managern = null;
	 			JSONObject man = (JSONObject) cust.get("clientManager");
	 			if(man != null) {
	 				manager = man.get("id").toString();
	 				managern = man.get("name").toString();
	 				 Agent aa = new Agent (manager);
			       		
	 				 aa.setName(managern);
	 		     tm.put(manager, aa); 


	 			}
	 			
	 			
	 		 } 
	    	 }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    System.out.println("tmsuurus " + tm.size());
     
     
     
     return tm;
   }

   
   private void saveAssetFile() {
	org.json.JSONArray data = Converter.getAssetsJson(user, pass, rcompany);
	//System.out.println(data.toString().substring(0, 3000));
	
	
	     String sisu = "InventoryCode;InventoryName;FactoryId;NFC;CustomerCode;CustomerName"
	     		+ ";LastCheck;LastCheckManagerCode;LastCheckManagerName;Status;PictureLink;PictureDate\n";
	  int i = data.length();
	 for(int a = 0; a < i;a++) {
		 String nimi = null;
		 String knimi = null;
		 String kkood = null;
		 String kood = null;
		 String mnimi = null;
		 String mkood = null;
		 String nfc = null;
		 String fnimi = null;
		 String dnimi = null;
		 Long viimaneKin = null;
		 String fId = null;
		 Long modified = null;
		 String status = null;
		 Long fcreated = null;
		 
		 

		 
	 try {
		 org.json.JSONObject asset =(org.json.JSONObject) data.get(a);
		nimi = (String) asset.get("name");
		kood = (String) asset.get("code");
		

		
	try {
		org.json.JSONArray fileArr =(org.json.JSONArray) asset.get("files");
		if(fileArr != null) {
			int e = fileArr.length();
			 for(int u = 0; u < e;u++) {
				 org.json.JSONObject fileO =(org.json.JSONObject) fileArr.get(u);
				 fnimi = (String) fileO.get("filename");
				 fcreated = (Long) fileO.get("created");
				 }
		}
	} catch (JSONException e) {
	}	
	
	try {
		viimaneKin = (Long) asset.get("lastConfirmed");
		//System.out.println(Converter.getDDsMMMsYYYYString(new Date(viimaneKin)));
	} catch (JSONException e) {
	}	
	
	
	try {
		status = (String) asset.get("status");
	}catch (JSONException e) {
	}	
	
	
	
	try {
		modified = (Long) asset.get("modified");
		
	} catch (JSONException e) {
	}
	
	
	try {
		fId = (String) asset.get("factoryId");
	} catch (JSONException e) {
	}	
	
	
	
	try {
		org.json.JSONArray dims =(org.json.JSONArray) asset.get("dimensions");
		if(dims != null) {
			int e = dims.length();
			 for(int u = 0; u < e;u++) {
				 org.json.JSONObject dimO =(org.json.JSONObject) dims.get(u);
				 dnimi = (String) dimO.get("name");
				}
		}
	} catch (JSONException e) {
	}	
		
		
		try {
			nfc = (String) asset.get("nfcTag");
		} catch (JSONException e) {
		}	
		
		
		
		try {
			 org.json.JSONObject manager =(org.json.JSONObject) asset.get("changeManager");
			 if(manager != null) {
				 mnimi = (String) manager.get("name");
				 mkood = (String) manager.get("code");
			 }
			
		} catch (JSONException e) {
		}	
		
		
		try {
			
			 org.json.JSONObject customer =(org.json.JSONObject) asset.get("customer");
			 if(customer != null) {
				 knimi = (String) customer.get("name");
				 kkood = (String) customer.get("code");

			 }
		} catch (JSONException e) {
		}	

		
		//"InventoryCode;InventoryName;FactoryId;NFC;CustomerCode;CustomerName"
 		//+ ";LastCheck;LastCheckManagerCode;LastCheckManagerName;Status;PictureLink;PictureDate\n";
		
		sisu = sisu + kood + ";" + nimi + ";" + nonull(fId) + ";" + nonull(nfc) + ";" + nonull(kkood) + ";" + nonull(knimi) + ";" + lnonull(viimaneKin)
		+ ";" + nonull(mkood) + ";" + nonull(mnimi) + ";" + nonull(status) + ";" + nonull(fnimi) + ";" + lnonull(fcreated);
		sisu = sisu + "\n";
	} catch (JSONException e) {
		e.printStackTrace();
	}
		//System.out.println(kood + " ja " + nimi + " ja " +  knimi + " ja " + kkood);
		//System.out.println(  " ja " + nfc +   " ja " +  mnimi + " ja " + mkood);
		//System.out.println( dnimi +" "+ kood);
	 }
	 Converter.saveFileInUTF8("Inventorydata.txt", sisu);
	
}
   private String lnonull(Long a) {
	   if(a == null) {
		   return " ";
		   
	   }
	   Calendar cal = Calendar.getInstance();
	   cal.setTimeInMillis(a);
	   return Converter.getDDpMMpYYYYpString(cal);
   }
   
   
   
   private String nonull(String a) {
	   if(a == null) {
		   return " ";
		   
	   }
	   return a;
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


private Object readJsonFile(File f){
	 JSONParser parser = new JSONParser();  
		 Object obj = null;
		try {
			obj = parser.parse(new FileReader(getDir() + f.getName()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	return obj;
}



   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     boolean makedata = true;
	     String repManas = null;
	     
	     
	     
	     
	     
	     
		 if("true".equals(getParent().getProps().getProperty("use.olddata")) || useOldData){
			 makedata = false;
		 }
		 
		 if(rcompany != null){
			Converter.getManagersReplacements(agent1, rcompany, user , pass);
		 }
		 
	     if(makedata){
	    	 this.saveWebFiles();
	 	 }
	     
	     
	    
	   try {
		   
		   FileListFilter fil = new FileListFilter("klient","txt");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String fname = ll[i];
	    		//System.out.println(name);
	 		  File f = new File (getDir() + fname);
	 		  JSONObject obj = (JSONObject) this.readJsonFile(f);
	 		  JSONArray customers = (JSONArray) obj.get("ClientData");
	 		 for(Object custo: customers) {
	 			 JSONObject cust = (JSONObject) custo;
	 			 String code = (String) cust.get("code");
	 			String name = (String) cust.get("name");
	 			String address = (String) cust.get("address");
	 			String billingClientId = (String) cust.get("billingClientId");
	 			String active = (String) cust.get("active");
	 			String id = (String) cust.get("id");
	 			String manager = null;
	 			JSONObject man = (JSONObject) cust.get("clientManager");
	 			if(man != null) {
	 				manager = man.get("id").toString();

	 			}
	 			
	 			Customer cc = new Customer(id);
	 			cc.setName(name + " " + code);
	 			cc.addAgent(new Agent("TEST"));
	 			if(manager != null) {
	 				cc.addAgent(new Agent(manager));
	 			}
	 			if(address != null) {
		 			cc.setAddress(address);
	 			}
	 			if(billingClientId != null) {
	 				cc.setPayer(billingClientId);
	 			}

	 			if(active.equals("1")) {
	 			tm.put(name, cc);
	 			}
	 			
	 			
	 		 } 
	    	 }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    System.out.println("tmsuurus " + tm.size());
	  
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
    TreeMap<String, Integer> stocks = new TreeMap<String, Integer>();
    
    try {
		   
		   FileListFilter fil = new FileListFilter("laoseis","txt");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String fname = ll[i];
	    		//System.out.println(name);
	 		  File f = new File (getDir() + fname);
	 		  JSONObject obj = (JSONObject) this.readJsonFile(f);
	 		  JSONObject sto = (JSONObject) obj.get("StockData");
	 		  JSONArray stock = (JSONArray) sto.get("Items");
	 		  
	 		 for(Object prodo: stock) {
	 			 JSONObject lseis = (JSONObject) prodo;
	 			 JSONObject lprod = (JSONObject) lseis.get("product");
	 			 String id = (String) lprod.get("id");
	 			 JSONObject lware = (JSONObject) lseis.get("warehouse");
	 			 String wareId = (String) lware.get("id");
	 			 String kog = (String) lseis.get("amountInStock");
	 			 Integer kogus = Integer.parseInt(kog);
	 			 if (kogus != 0){
	 				 if (stocks.get(id) != null){
	 					 kogus = kogus + stocks.get(id);
	 				 }
	 				stocks.put(id, kogus);
	 			
	 			 }
	 			 

	 			
	 			
	 			
	 		 } 
	    	 }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    System.out.println("lssuurus " + stocks.toString());

 
    
    try {
		   
		   FileListFilter fil = new FileListFilter("Toode","txt");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String fname = ll[i];
	    		//System.out.println(name);
	 		  File f = new File (getDir() + fname);
	 		  JSONObject obj = (JSONObject) this.readJsonFile(f);
	 		  JSONArray products = (JSONArray) obj.get("ProductData");
	 		 for(Object prodo: products) {
	 			 JSONObject prod = (JSONObject) prodo;
	 			 String code = (String) prod.get("code");
	 			 String id = (String) prod.get("id");
	 			String name = (String) prod.get("name");
	 			String active = (String) prod.get("active");
	 			String barcode = (String) prod.get("barcode");
	 			JSONObject group1 = (JSONObject) prod.get("group1");
	 			String gr1ID = null;
	 			if(group1 != null) {
	 				 gr1ID = (String) group1.get("id");
	 			}
	 			
	 			JSONObject group2 = (JSONObject) prod.get("group2");
	 			String gr2ID = null;
	 			if(group2 != null) {
	 			 gr2ID = (String) group2.get("id");
	 			}
	 			String pri = (String) prod.get("wholesalePrice");
	 			JSONObject unito = (JSONObject) prod.get("unit");
	 			String unit = (String) unito.get("name");
	 			JSONObject vt = (JSONObject) prod.get("vat");
	 			String vat = (String) vt.get("rate");
	 		
	 			
	 			Product pp = new Product(id);
	 			if(barcode != null){
	 				pp.setEAN(barcode);
	 			}
	 			
	 			pp.setVat(vat);
	 			pp.setName(name + " " + code);
	 			if(gr1ID != null) {
	 				pp.setGroup(gr1ID);
	 			}
	 			if(gr2ID != null) {
	 				pp.setGroupSecond(gr2ID);
	 			}
	 			if(pri != null) {
	 				pri = pri.replace(',', '.');
		 			double price = Double.valueOf(pri);
		 			pp.setPrice(price);	
	 			}
	 			pp.setUnit(unit);
	 			if(active.equals("1") && name != null) {
	 			tm.put(name, pp);
	 			}
	 			
	 			
	 			
	 		 } 
	    	 }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    System.out.println("tmsuurus " + tm.size());
    
    
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
     
    
      
     
     private Hashtable docCust = null;
     private TreeMap plans = null; 
     
  public void setBulkSales(Map agents) {
	  TreeMap myygid = new TreeMap();
	  TreeMap myygideelm = new TreeMap();
	  docCust = new Hashtable();
	  
	 
	  
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
