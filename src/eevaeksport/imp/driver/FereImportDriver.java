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

public class FereImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public FereImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
        Class.forName(databaseDriverName).newInstance();
      }
      catch (Exception e) {
        parent.showError(e);
      }
     
    }
  
  HashMap customers = null;
  TreeMap prodNames = null;
  
  private String url = "jdbc:odbc:kiire";
  
  private HashMap getCustomers() {
	  HashMap res = new HashMap();
	  try {
	  String cmd = "Select kood, nimi  from getCustomers where nimi != '' ";
	   cmd = "SELECT  f.kood, left(f.kood,5)+ ' ' + f.nimi AS nimi FROM osmy/firma f " +
	        "LEFT JOIN osmy/my_tell tt ON f.kood=tt.saaja  " +
	        "WHERE   (f.agent)!=0  GROUP BY kood ";
	 	Connection  con = getNewConnection();
        Statement   stmt = con.createStatement();        
        ResultSet  rs = stmt.executeQuery(cmd);
        while (rs.next()) {
           
        	String kood = rs.getString("kood").trim();
          Customer cust = new Customer(kood);
          String nimi = rs.getString("nimi").trim();
         cust.setName(nimi);

        	  res.put(kood, cust);
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
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  public String identifyCustomer(String id,String regnr)  {
    return id;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    return prodID;
  }
	
  
  public void makeOrder(Order order) throws SQLException{
	  counter = 0;
	  System.out.println("tellimust tegema");
	  
    	  customers = this.getCustomers();
    	  prodNames = this.getProducts(null);
    	  System.out.println("tellimust tegema2 " + customers.size() + " " + prodNames.size());
    	  
		  makeOutput(order); 
		  

	      setNumAddedLines(counter);
	  
  }
  
  
  
 private void makeOutput(Order order){
	   counter = order.getOrderLines().size();
        
	      try {
	   
	   String com = order.getInfo();

	      System.out.println("counter " + counter + " " + com);
		  	   
	   if(com != null && com.trim().length()>0){
		   	
	     

		   
	     
		    	  String linesTXT = Converter.getOrderText(order, prodNames);
		    	  String comm = "";
		    	  if(order.getInfo() != null) {
		    		  comm = order.getInfo();
		    	  }

			      System.out.println("lähen saatma " );
				  
		    	  Converter.sendCommentEmail("order " + comm + "\n\n" + linesTXT, null, order.getAgent() + "-" + order.getDokNr(), order.getCompany(),
		    			  ((Customer)customers.get(order.getCompany())).getName(), null, false, null, parent, null);
		    	 
		    	  System.out.println("Shell email sent");
		    		
		   }      
		} catch (Exception e) {
			e.printStackTrace();
		}
	      

	 }

 
 
 private TreeMap getProducts(Agent agent){

	    String  cmd = " SELECT a.siffer AS toode,trim(a.siffer) + ' ' + iif(right(a.uhik,5)='1---T','+++','') + a.nimi AS nimetus," +
	       		"  right(left(a.uhik,4),3) AS tykke, a.mghind AS myygihind, " +
	       " (kak+tulek-minek) AS kogus, left (allgrupp,1) AS grupp,left(allgrupp,2) AS grupp2 , grupp AS alegrupp,a.kassak as ean " +
	       " FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood LEFT JOIN ladu/ll010000 ll " +
	       " ON ll.siffer=a.siffer WHERE (a.uhik not in ('------') OR (right(ltrim(str(tarnaeg)),2)=right(str(year(date())),2) AND " +
	        " left(ltrim(str(tarnaeg)),2)>iif(len(ltrim(str(month(date()))))=1,'0'+ltrim(str(month(date()))),ltrim(str(month(date()))))))" +
	        " ORDER BY a.siffer " ;
	    
	    TreeMap products = new TreeMap();
	    try {
   Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      HashMap prodHash = new HashMap();
	      while(rs.next()){
	        String kood = rs.getString("toode").trim();
	        String nimetus = rs.getString("nimetus").trim() ;
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
	        
	        
 
 private int counter = 0;  

 
  public String getDriverVersion(){
    return "Fere";
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
