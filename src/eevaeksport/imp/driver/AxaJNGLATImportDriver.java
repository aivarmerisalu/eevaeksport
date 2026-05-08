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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportFrame;
import eevaeksport.Product;
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

public class AxaJNGLATImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public AxaJNGLATImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
        Class.forName(databaseDriverName).newInstance();
       
      }
      catch (Exception e) {
        parent.showError(e);
      }
    
   
    loadSignatures();
    instance = parent.getProps().getProperty("company");
    if(instance == null) {
  	  instance = "";
       }
     
    }
  
  HashMap customers = null;
  TreeMap prodNames = null;
  String instance = null;
  private String url = "jdbc:odbc:axa";
  
  private HashMap getCustomers() {
	  HashMap res = new HashMap();
	  try {
	  String cmd = "Select kood, nimi  from getCustomers where nimi != '' ";
	 	Connection  con = getNewConnection();
        Statement   stmt = con.createStatement();        
        ResultSet  rs = stmt.executeQuery(cmd);
        while (rs.next()) {
           
        	String kood = rs.getString("kood").trim();
          Customer cust = new Customer(kood);
          String nimi = rs.getString("nimi").trim();
         cust.setName(nimi);

          if(!nimi.endsWith("#")){
        	  res.put(kood, cust);
          }
          }
        rs.close();
        stmt.close();
        con.close();
          
	  }catch (Exception e) {
		  e.printStackTrace();
	  }
          
        	  
        	  return res;
  }
  
  
  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
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
			signatures = (Vector) readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}

	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
  
		
	   public static Object readObject(String s) throws IOException,
     ClassNotFoundException, IOException {
   Object obj = null;
   File f = new File(s);
   if (f.exists()){
     ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
     obj = ois.readObject();
     ois.close();
   }
   return obj;
 }


  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

   
  
  public String identifyCustomer(String id,String regnr)  {

    return id;

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
	  String urls = "https://info2.via3l.ee:8084/edimap2/index.php?loc=tellimiskeskus/json/createorderclientver1.php";
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
      
      if( (instance.equals("shell") || order.getAgent().startsWith("SH")) && this.customers == null) {
    	  customers = this.getCustomers();
       }
      if( (instance.equals("shell") || order.getAgent().startsWith("SH")) && this.prodNames == null) {
    	  prodNames = this.getProducts(null);
       }
      
      String signature = order.getSignature();
	  if(signatures.contains(signature)){
		  this.troubles.append( "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany()) ;
		    		  
	  }else{
		  makeOutput(order);  
		  signatures.add(signature);
		  this.troubles = null;
		  
	  }	    
	  
	   try {
			saveObject(signatures, "signatures.vec");
		} catch (IOException e) {
			e.printStackTrace();
		}
	  
  }
  
  
  
 private void makeOutput(Order order){
	  
	 Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("emailret.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	      try {
	 
	    	  long uni = Calendar.getInstance().getTimeInMillis();
	    	  
JSONObject ord = new JSONObject();
	  // ord.put("trackid", "1384c1b6f32e31353630353132393339");
	   String trackid = "1384c1b6f32e31353630353132393339";
	  if( parent.getProps().getProperty("trackid") != null) {
		  trackid  = parent.getProps().getProperty("trackid");
	  }
	   ord.put("trackid", trackid);
		    	  
 JSONObject viajson = new JSONObject();
	   ord.put("viajson", viajson);
	   
	   
	JSONObject document = new JSONObject();
	   viajson.put("Document", document);
	   
	   JSONObject header = new JSONObject();
	   
	   header.put( "DocType", "SalesQuotation");
 
	   header.put( "DocReceiverId", "JNGLV" );
	   
	   header.put( "PartnerOrder", order.getAgent() + "-" + order.getDokNr());

	   header.put( "OrderAccount",Converter.deCode(order.getCompany()));
	   
	  if(order.isShipDateUsed()) {
		  header.put("DeliveryDate",Converter.getYYYY_MM_DDpString(order.getShipCal()));
	  }
	   
	  if(order.getShipMode() != null && order.getShipMode().equals("EXW")) {
		  header.put("DeliveryTerm","EXW");
	  }
	   header.put( "DocCreatorName", order.getAgent());
	   
	   header.put( "DocCreatorId", order.getAgent());

	   
	   if(order.getWarehouse() != null && order.getWarehouse().trim().equals("L6") ) {
		   header.put( "InventLocation", order.getWarehouse());
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
				if(ARVE.length() > 100){
					ARVE = ARVE.substring(0,100);
				}
				String test =  JSONObject.escape(ARVE.trim());
				comm.put("Invoice", test);
			}
			
			if(KT.length() > 3){
				KT = KT.substring(3);
				if(KT.length() > 100){
					KT = KT.substring(0,100);
				}
				String test =  JSONObject.escape(KT.trim());
				comm.put("Block", test);
			}
			
			if(KOMPL.length() > 3){
				KOMPL = KOMPL.substring(3);
				if(KOMPL.length() > 100){
					KOMPL = KOMPL.substring(0,100);
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
	       	 ll.put("Unit", line.getUnit());
	       	 ll.put("Qty", line.getKogus());
	       	 
	       	 if(line.getDiscount() > 0) {
	       		 ll.put("Price", line.getPrice());
	       		 ll.put("Discount", line.getDiscount());
	       		 ll.put("SumBeforeVAT", line.getPrice() * line.getKogus());
		     }
	       	if(line.getDiscount() == -2) {
	       		 ll.put("Price", 0.01);
	       		// ll.put("Discount", 100);
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
			  addToDB(order.getAgent() + "-" +order.getDokNr().trim(),ord.toJSONString());
			   // postJson(ord.toString(), fileName2);
		   }else {
			   System.out.println("json " + ord.toString());
		   }
	     
		   
		      if(instance.equals("shell") || (order.getAgent().startsWith("SH"))) {// shell
		    	  Agent ag = new Agent(order.getAgent());
		    	  ag.setEmail(props.getProperty(order.getAgent()));
		    	  ag.setName(props.getProperty(order.getAgent()));
		    	//  System.out.println(prodNames);
		    	
		    	  String linesTXT = Converter.getOrderText(order, prodNames);
		    	  String comm = "";
		    	  if(order.getInfo() != null) {
		    		  comm = order.getInfo();
		    	  }
		    	  Converter.sendCommentEmail("order " + comm + "\n\n" + linesTXT, null, order.getAgent() + "-" + order.getDokNr(), order.getCompany(),
		    			  ((Customer)customers.get(order.getCompany())).getName(), null, false, ag, parent, fileName);
		    	 
		    	  System.out.println("Shell email sent");
		      }
		      
		} catch (Exception e) {
			e.printStackTrace();
		}
	      
	     
	 }

 
 
 private TreeMap getProducts(Agent agent){

	    String cmd = " select * from getproducts where nimi1 != ''";
	    
	    TreeMap products = new TreeMap();
	    try {
   Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      HashMap prodHash = new HashMap();
	      while(rs.next()){
	        String kood = rs.getString("toode").trim();
	        String nimetus = rs.getString("nimi1").trim() ;
	         products.put(kood, nimetus);
	        
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	    return products;
 }
	        
	        
 
 private void addToDB(String number,String json) throws SQLException{


   String sql = "INSERT INTO [SALESORDER] (STATUS,DOCUMENTID,DOCDATA) VALUES (1,'" + number + "', '" +json +"') ";
   
   
   log.info("addInvoiceHeader2: " + sql);
   Connection con = getNewConnection();
   
   PreparedStatement stmt = con.prepareStatement(sql);
   stmt = con.prepareStatement(sql);
   int aa =  stmt.executeUpdate();
  System.out.println("Vastus " + aa);
   stmt.close();
   con.close();
 
 }

 private int counter = 0;  

 
  public String getDriverVersion(){
    return "AxaJNGLAT";
  }

 
 public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private ImportPanel parent;
  private StringBuffer troubles= new StringBuffer();

  public String getTrouble() {
	  if(troubles != null) {
		  return troubles.toString();
	  }
  	return null;
  }
  
}
