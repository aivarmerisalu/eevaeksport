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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
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

public class VikstalImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());
    boolean testing = false;
  
  public VikstalImportDriver(ImportPanel parent) {
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
  
  TreeMap customers = null;
  TreeMap prodNames = null;
  
  private String url = "jdbc:odbc:baas";
  
  public TreeMap getCustomers() {
	    String cmd = " SELECT kl.code AS kood,kl.descr AS nimi,rtrim(ltrim(sp82)) AS address,'' AS visit,abi.sp726 AS ale " +
	    		" ,sp539 AS blokk ,'' AS toall,kl.sp873 AS payer,  ltrim(rtrim(kl.sp715)) as agent " +
	        "  FROM sc96 kl left JOIN 1sconst c ON c.objid=kl.id and ltrim(c.id)='2J' LEFT JOIN  sc724 abi ON  abi.id=c.value	" +
	        "        WHERE   kl.verstamp != 'E' ORDER BY kl.descr ASC GROUP BY kl.descr ";

	    TreeMap customers = new TreeMap();
	    
	    try {
	    	Connection con = DriverManager.getConnection(url);
	    	PreparedStatement   pstmt = con.prepareStatement(cmd);
	      //pstmt.setString(1, agent.getID());
	      ResultSet rs = pstmt.executeQuery();
	      while (rs.next()) {
	        String name = rs.getString("nimi").trim();
	        String accountnum = rs.getString("kood").trim();
	        Customer cust = new Customer(accountnum);
	        cust.setName(name);
	        cust.setAddress(rs.getString("address").trim());
	        cust.setVisit(rs.getString("visit").trim());
	        String ale = rs.getString("ale");
	        if (ale != null && ale.trim().length() > 0) {
	          cust.setDiscount(Double.parseDouble(ale));
	        }
	        boolean blokk = rs.getBoolean("blokk");
	        if (blokk) {
	          cust.setSalesBlock(blokk);
	        }
	        customers.put(cust.getID(), cust);
	      }

	      rs.close();
	      pstmt.close();
	      con.close();
	    
	    }
	    catch (Exception e) {
	      e.printStackTrace();;
	    }
	    return customers;
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
    	  prodNames = this.getProducts();
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
				  
		    	  Converter.sendCommentEmail("order " + comm + "\n\n" + linesTXT, null, order.getAgent() , order.getCompany(),
		    			  ((Customer)customers.get(order.getCompany())).getName(), null, false, null, parent, null);
		    	 
		    	  System.out.println("Vikstal email sent");
		    		
		   }      
		} catch (Exception e) {
			e.printStackTrace();
		}
	      

	 }

 
 public TreeMap getProducts(){

	    TreeMap products = new TreeMap();


	    try {
	    	Connection con = DriverManager.getConnection(url);
		    

	String cmd = "  SELECT  (date),pr.id,pr.code AS kood,pr.descr AS nimi,(c.value) AS hind,sp510 AS EAN , yh.descr AS yhik  " +
	    " ,parentid AS grupp FROM sc113 pr  "+
	   "  LEFT JOIN sc41 yh ON yh.id=sp106 LEFT JOIN SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) LEFT " +
	   "join sc504 abi on abi.id=hi.sp508 " +
	  "  WHERE  ((ltrim(c.id)='E5' AND isfolder=2 AND ltrim(c.value)!='0.000'  and ltrim(sp508)='I' )  OR  pr.descr  like'%pda%') and date<=date() " +
		"  ORDER BY pr.descr, date ";


	     Statement stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      while(rs.next()){
	        // if ( kood.length() > 2 ){
	        String kood = rs.getString("kood").trim();
	        Product prod = new Product(kood);
	        String nimetus = rs.getString("nimi").trim();
	        prod.setName(kood + " " + nimetus  );
	        prod.setPrice(rs.getDouble("hind"));
	        String ean =rs.getString("EAN");
	        if(ean != null && ean.trim().length()>0){
	          prod.setEAN(ean.trim());
	        }
	        String yhik = rs.getString("yhik");
	        if (yhik!= null && yhik.trim().length()>0){
	          prod.setUnit(yhik.trim());
	        }
	        prod.setVat("22");
	        String grupp = rs.getString("grupp");
	        if (grupp != null && grupp.trim().length() > 0) {
	            prod.setGroup(grupp.trim());
	        }


	        products.put(prod.getID(), prod.getName());

	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	     e.printStackTrace();;
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
