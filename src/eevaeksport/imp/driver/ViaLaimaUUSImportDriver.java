package eevaeksport.imp.driver;




import java.io.BufferedReader;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

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

public class ViaLaimaUUSImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public ViaLaimaUUSImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");

    
    loadSignatures();
  }
  
	private Vector signatures = new Vector();

	private void loadSignatures(){
		
try{		
		File f = new File("signatures.vec");
		Calendar ss = Calendar.getInstance();
		
		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 3600000) {// kui on yle tunni vana siis maha 
			f.delete();
			signatures = new Vector();
		} else {
			signatures = (Vector) Converter.readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}

  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

   
  
  public String identifyCustomer(String id,String regnr)  {
    String acc = null;
    return acc;
  }

  private CloseableHttpClient client;
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }
  

  
  static HttpResponse restPostRequest(String uri, String json, HttpClient client) throws Exception {

		 HttpPost httpPost = new HttpPost(uri);
		 if(json != null){
			 httpPost.setEntity(new StringEntity(json,"utf-8"));
		        httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
		 }
	        return client.execute(httpPost);

	}

	public void closeClient(){
		if(client != null){
			
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    client = null;
		}		
	}
  
	
  public String postJson(String json, String filename) throws Exception{
	  Converter.trustAllHttpsURLConnection();
	  String urls = "https://84.50.148.253/edimap2/index.php?loc=tellimiskeskus/json/createorderclientver1.php";
	  // vana info2.via3l.ee:8084
	  URL url = new URL (urls);
	 	
	 	HttpURLConnection con = (HttpURLConnection)url.openConnection();
	 	
	 	con.setRequestMethod("POST");
	 	con.setRequestProperty("Content-Type", "application/json; utf-8");
	 	con.setRequestProperty("Accept", "application/json");
	 	con.setDoOutput(true);
	 	con.getOutputStream();
	 	
	  try {
		  
		  OutputStream os = con.getOutputStream();
	 	    byte[] input = json.getBytes("utf-8");
	 	    os.write(input, 0, input.length);           
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
	  String aaa = null;
	  try{
		  BufferedReader br = new BufferedReader(
				  new InputStreamReader(con.getInputStream(), "utf-8"));
		  
					    StringBuilder response = new StringBuilder();
					    String responseLine = null;
					    while ((responseLine = br.readLine()) != null) {
					        response.append(responseLine.trim());
					    }
					    aaa = response.toString();
					    Converter.saveFile(filename, aaa);
					    System.out.println("aaa " + aaa);
		  
	    }catch(Exception u) {
		  u.printStackTrace();
	  }
			
			
    	return aaa;
	}
  
  public void makeOrder(Order order) throws SQLException{
	  counter = 0;
      if (signatures == null){
   	   signatures = new Vector();
      }
      
      
      String signature = order.getSignature();
      
      
	  if(false && signature != null && signatures.contains(signature)){
		  this.troubles.append( "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany()) ;
	  }else{
		  makeOutput(order);  
		  signatures.add(signature);
		  this.troubles = null;
	  }	    
	  
	   try {
			Converter.saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
  }
  
  
  
 private void makeOutput(Order order){
	  
	  
	    
	      try {
	 
	    	  long uni = Calendar.getInstance().getTimeInMillis();
	    	  
JSONObject ord = new JSONObject();
	  // ord.put("trackid", "1384c1b6f32e31353630353132393339");
	   String trackid = null;
	  if( parent.getProps().getProperty("trackid") != null) {
		  trackid  = parent.getProps().getProperty("trackid");
	  }else {
		  System.out.println("NO TRACK ID");
		  System.exit(0);
	  }
	   ord.put("trackid", trackid);
		    	  
 JSONObject viajson = new JSONObject();
	   ord.put("viajson", viajson);
	   
	   
	JSONObject document = new JSONObject();
	   viajson.put("Document", document);
	   
	   JSONObject header = new JSONObject();
	   
	   header.put( "DocType", "SalesQuotation");
 
	   header.put( "MMF", order.getAgent().substring(order.getAgent().length()-2) );
	   
	   header.put( "PartnerOrder", order.getAgent() + "-" + order.getDokNr());

	   header.put( "OrderAccount",Converter.deCode(order.getCompany()));
	   
	  if(order.isShipDateUsed()) {
		  header.put("DeliveryDate",Converter.getYYYY_MM_DDpString(order.getShipCal()));
	  }
	   
	   header.put( "DocCreatorName", order.getAgent());
	   
	   header.put( "DocCreatorId", order.getAgent());

	   
	   if(order.getWarehouse() != null && order.getWarehouse().trim().equals("L6") ) {
		   header.put( "InventLocation", order.getWarehouse());
	   }
	 
	   String shipmode = order.getShipMode();
	   
	   if(shipmode != null && shipmode.trim().length()>0){
		      String ID = "";
		      String CARR = "";
		      String NAME = "";
		      String COMPANY = "";
			  String PHONE = "";
			  String EMAIL = "";
			  String STREET = "";
			  String CITY = "";
			  String INDEX = "";
			  String TYPE = "";
			  String PRIV = "";
			  String ORDNUM = "";
			  String COMM = "";
			  
			  
			  StringTokenizer st = new StringTokenizer(shipmode,"|");
			  while(st.hasMoreElements()) {
				  
				//  ky:EE90105|re:10092256|na:Aivar Merisalu|co: Akadeemia Konsum Etk9_1|te:+37256248034|em:Aivar@mobilejava.ee|tn:|ci:|in:|ty:box|pp:false
				   ID = st.nextToken().substring(3);
				   CARR = st.nextToken().substring(3);
			       NAME = st.nextToken().substring(3);
			       COMPANY = st.nextToken().substring(3);
				   PHONE = st.nextToken().substring(3);
				   EMAIL = st.nextToken().substring(3);
				   STREET = st.nextToken().substring(3);
				   CITY = st.nextToken().substring(3);
				   INDEX = st.nextToken().substring(3);
				   TYPE = st.nextToken().substring(3);
				   PRIV = st.nextToken().substring(3).toUpperCase();
				   if(st.hasMoreTokens())
				   ORDNUM = st.nextToken().substring(3);
				   if(st.hasMoreTokens())
				   COMM = st.nextToken().substring(3);
				   
				   /*<DeliveryCompanyName>OC Demo</DeliveryCompanyName>         <!--mandatory-->
		<DeliveryPersonName>Elmo Tamm</DeliveryPersonName>         <!--mandatory-->
		<DeliveryContactEmail>demo@demo.ee</DeliveryContactEmail>  <!--optional-->
		<DeliveryContactPhone>+37256565656</DeliveryContactPhone>  <!--mandatory. Notification will be sent to this number. Must contain country code and no empty spaces -->
		<DeliveryServiceType>ParcelLocker</DeliveryServiceType>    <!--mandatory Courier or ParcelLocker -->
		<DeliveryServiceCarrier>1034324</DeliveryServiceCarrier>   <!--mandatory DPD or other partner registernumber. Can be taken from Via3L parcelmachinelist API-->
		<TerminalId>EE90186</TerminalId>                           <!--mandatory Parcel Locker code. Can be taken from Via3L parcelmachinelist API -->*/
				   if( COMPANY.trim().length() > 0) {
					   header.put( "DeliveryCompanyName", COMPANY);
				   }
				   if( NAME.trim().length() > 0) {
					   header.put( "DeliveryPersonName", NAME);
				   }
				   if( EMAIL.trim().length() > 0) {
					   header.put( "DeliveryContactEmail", EMAIL);
				   }
				   if( PHONE.trim().length() > 0) {
					   header.put( "DeliveryContactPhone", PHONE);
				   }
				   if( TYPE.trim().length() > 0) {
					   if(TYPE.equals("box")) {
						   header.put( "DeliveryServiceType", "ParcelLocker");
					   } else {
						   header.put( "DeliveryServiceType", "Courier"); 
						   header.put( "Country", "EE"); 
						 
					   }
				   }
				   if( CARR.trim().length() > 0) {
					   header.put( "DeliveryServiceCarrier", CARR);
				   }
				   if( !ID.equals("kul1") && ID.trim().length() > 0) {
					   header.put( "TerminalId", ID);
				   }
				   if( PRIV.trim().length() > 0) {
					   header.put( "DeliveryIsToPrivatePerson", PRIV);
				   }
				   if( ORDNUM.trim().length() > 0) {
					   header.put( "OrigOrder", ORDNUM);
				   }
				   
				   /*<DeliveryAddressDetail>
			<Country>EE</Country> <!--mandatory-->
			<Postalcode>45451</Postalcode> <!--mandatory-->
			<City>Tallinn</City> <!--mandatory-->
			<AddressRow1>TC$he 7-1</AddressRow1> <!--* 2 possibilities: This field must be filled or Street,StreetNumber,PostBox fields must be filled-->
			<Street>TC$he</Street> <!--*-->
			<StreetNumber>7</StreetNumber><!--*-->
			<PostBox>1</PostBox><!--*-->
		</DeliveryAddressDetail>*/
				   if( STREET.trim().length() > 0) {
					   JSONObject aadress = new JSONObject();
					   aadress.put("AddressRow1", STREET);
					   if( INDEX.trim().length() > 0) {
						   aadress.put("Postalcode", INDEX);
					   }
					   if( CITY.trim().length() > 0) {
						   aadress.put("City", CITY);
					   }
					   if(!TYPE.equals("box")) {
						   aadress.put( "Country", "EE"); 
					   }
					   
					   if(COMM.length()>1) {
							  
						   aadress.put("AddressRow2", COMM);
						   
					   }
					   header.put( "DeliveryAddressDetail", aadress);
					   
					   
					
					 
				   }
				   
				   
			  }
			  
			  
		   
	   }else {
		   System.out.println("Reference " + order.getTeostaja());
		   
		   if(order.getTeostaja() != null && order.getTeostaja().trim().length() != 0){
			   header.put( "OrigOrder", order.getTeostaja());
	 		}
		 
	   }
	   
	   String com = (order.getInfo());
	   
	   if(com != null && com.trim().length()>0){
			
		
            String KT = "";
			String KOMPL = "";
			String ARVE = "";
		    

			JSONObject comm = new JSONObject();
			
			if(com.indexOf("KT:")!=-1 && com.indexOf("KM:") != -1){
			      	KT = com.substring(com.indexOf("KT:"),com.indexOf("KM:"));
			} else if(com.indexOf("KT:")!=-1 && com.indexOf("AR:") != -1){
				    KT = com.substring(com.indexOf("KT:"),com.indexOf("AR:"));
			} else if(com.indexOf("KT:")!=-1){
				    KT = com.substring(com.indexOf("KT:"),com.length());
			}

			if(com.indexOf("AR:")!=-1 && com.indexOf("KM:") != -1){  
				KOMPL = com.substring(com.indexOf("KM:"),com.indexOf("AR:")); 	
			  }else if(com.indexOf("KM:") != -1){
			  	KOMPL = com.substring(com.indexOf("KM:"),com.length()); 	
			  }    

			if(com.indexOf("AR:")!=-1 ){  
				ARVE = com.substring(com.indexOf("AR:"),com.length());
			  }

			
			
			if(ARVE.length() > 3){
				ARVE = ARVE.substring(3);
				if(ARVE.length() > 250){
					ARVE = ARVE.substring(0,250);
				}
				String test =  JSONObject.escape(ARVE.trim());
				comm.put("Invoice", test);
			}
			
			if(KT.length() > 3){
				KT = KT.substring(3);
				if(KT.length() > 250){
					KT = KT.substring(0,250);
				}
				String test =  JSONObject.escape(KT.trim());
				comm.put("Block", test);
			}
			
			if(KOMPL.length() > 3){
				KOMPL = KOMPL.substring(3);
				if(KOMPL.length() > 250){
					KOMPL = KOMPL.substring(0,250);
				}
				String test =  JSONObject.escape(KOMPL.trim());
				comm.put("Pick", test);
			}
			
	
			System.out.println(com + "\nKT:" + KT + "KOMPL:" + KOMPL + "ARVE:" + ARVE);
     	   
			header.put( "Comment", comm);
			   	
			
		}

		
   document.put("Header", header);
	   

	        //RIDADE LISAMINE

	   
	   JSONArray linex = new JSONArray();
   	   document.put("Line", linex);
   	   
	        int coun = 0;
	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	    	  coun++;
	        	  counter++;
	        	  
	        JSONObject ll = new JSONObject();
	       	 ll.put("LineNum", coun);
	       	 ll.put("ProductId", toode);
	       	 ll.put("Unit", "ea");
	       	 ll.put("Qty", line.getKogus());
	       	 
	       	 if(line.getDiscount() > 0) {
	       		 ll.put("Price", line.getPrice());
	       		 ll.put("Discount", line.getDiscount());
	       		 ll.put("SumBeforeVAT", line.getPrice() * line.getKogus());
		     }
	       	 
	       	 
	      // 	 if(order.getWarehouse() != null && order.getWarehouse().trim().equals("L6") ) {
	       	//	 JSONObject la = new JSONObject();
	       		// la.put("InventLocation", order.getWarehouse());
	  		   //ll.put( "Detail", la);
	  	   //}
	       	 
	       	    linex.add(ll);
	       	 
	          }
	    	   
	       	   JSONObject footer = new JSONObject();
	       	   footer.put("LineCount", order.getOrderLines().size());
	       	   document.put("Footer", footer);

	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim() +  ".json";
	      String fileName2 = parent.getProps().getProperty("conv.dir2") + order.getDokNr().trim() + (uni + "").trim() +  "res.txt";
		    
	      // System.out.println(fileName);
	      boolean testing = false;
	      Converter.saveFile(fileName, ord.toString());
	      
	      if("true".equals(parent.getProps().getProperty("testing"))) {
	    	  testing = true;
	      }

	      System.out.println("counter " + counter);
		   setNumAddedLines(counter);
		   
		   if(!testing) {
			   postJson(ord.toString(), fileName2);
		   }else {
			   System.out.println("json " + ord.toString());
		   }
	     
	      
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


 private int counter = 0;  

 
  public String getDriverVersion(){
    return "ViaLaimaUUSImport";
  }

 
 public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

 private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

 


   
  private String url = "jdbc:odbc:mynowaco";
  private Connection con;
  private ImportPanel parent;
 
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
  	// TODO Auto-generated method stub
	  if(troubles != null) {
		  return troubles.toString();
	  }
  	return null;
  }
  
}
