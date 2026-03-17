
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportFrame;
import eevaeksport.Email;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class RivileSB3ImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileSB3ImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + parent.getProps().getProperty("dns.host"),parent.getProps().getProperty("dns.user"),parent.getProps().getProperty("dns.pass"));
    }
    catch (Exception e) {
      parent.showError(e);
    }
    
   
    logurl = "https://api.manorivile.lt/client/v2";
    if( parent.getProps().getProperty("logurl") != null){
    	  logurl = parent.getProps().getProperty("logurl");
    }
    if( parent.getProps().getProperty("apikey") != null){
    	APIKEY = parent.getProps().getProperty("apikey");
    }
  }
  
  
  private String logurl = null;
  private String APIKEY = null;
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

private HashMap pants = null;

  public String getPandiPakend(String toode) {
	
	  if(toode.equals("405340094252")) {
		  return "087";
	  }
	  
	  if(pants == null) {
		  pants = new HashMap();
		    String sql1 = " SELECT N26_KODAS_PS as toode , RTRIM(N26_KODAS_PS_K) AS pant FROM N26_KOMP  where N26_KODAS_PS_K not like 'MI%' ";
		   
		    try {
		    	
		    	Statement st1 = con.createStatement();
		   	    ResultSet rs1 = st1.executeQuery(sql1);
		   	    while (rs1.next()) {
		   	    	
		   	     String tood  = rs1.getString("toode").trim();
		   	     String pant  = rs1.getString("pant").trim();
		   	         pants.put(tood, pant);
		   	    }
		   	       
		   	    rs1.close();
		   	    st1.close();	

		    }catch(Exception e) {
		    	e.printStackTrace();
		    } 
	  }
	  
	  
	 
	    return (String)pants.get(toode);
	 }
	  
  
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	    sql1 = "SELECT  ltrim(N08_KODAS_KS) AS kood1, (case when (N33_EIL_NR) is null then '1' else ltrim(str(N33_EIL_NR)) end) AS kood " +
	  " FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
	  " where right(rtrim(N33_PAV),3) =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood1").trim() + "-" +rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }
  public boolean isKeg1Euro(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_aprasymas1 as asi FROM n08_klij k  WHERE rtrim(N08_KODAS_KS) = '" + id +"'"; 
	  	
	    System.out.println(sql1);
	    boolean acc = false;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	String ss = rs1.getString("asi");
	    	if(ss != null && ss.trim().length() >0 ) {
	    		if(ss.toLowerCase().trim().equals("sta1eur")) {
	    			 acc = true;
	    		}
	    	}
	     
	    }
	    rs1.close();
	    st1.close();

	    return acc;

	  }

  public boolean isKeg0Euro(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_aprasymas1 as asi FROM n08_klij k  WHERE rtrim(N08_KODAS_KS) = '" + id +"'"; 
	  	
	    System.out.println(sql1);
	    boolean acc = false;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	String ss = rs1.getString("asi");
	    	if(ss != null && ss.trim().length() >0 ) {
	    		if(ss.toLowerCase().trim().equals("sta0eur")) {
	    			 acc = true;
	    		}
	    	}
	     
	    }
	    rs1.close();
	    st1.close();

	    return acc;

	  }
  
  public String identifyCustomerName(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_PAV AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
	  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
	  	
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("aadress").replace("\"", "-").trim();

	    }
	    rs1.close();
	    st1.close();

	    return acc;

	  }
  
 private HashMap<String, String> pnames = null;
  
  private String identifyProductName(String id) throws SQLException {
	  String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_PAV AS nimi " +
			     " FROM n17_prod a where N17_POZ_DATE !=1  and N17_MEN_PAV !='IG' and len(ltrim(N17_PAV_K1))  >0 "; 
	  	
	  	 if(pnames != null) {
	  		 return pnames.get(id);
	  	 }
	  	 
	    System.out.println(sql1);
	   

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	if(pnames == null) {
	    		pnames = new HashMap<String, String>();
	    	}
	    	
	      String toode  = rs1.getString("toode").trim();

	      String nimi  = rs1.getString("nimi");
	      if(nimi != null)
	      pnames.put(toode, nimi);

	    }
	    rs1.close();
	    st1.close();

	    if(pnames != null) {
	  		 return pnames.get(id);
	  	 }
	    
	    return null;

	  }
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
    System.out.println(sql1);
    String acc = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();
    return acc;
  }
  
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT N33_SHIP_VIA AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
 	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
 	
   System.out.println(sql1);
   String acc = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
     acc = rs1.getString("aadress").trim();

   }
   rs1.close();
   st1.close();

   return acc;

 }
  
  public String getAgentCar(String age) throws SQLException {
	 	 String	sql1 = "SELECT N72_KODAS_AU AS carplate FROM N72_mas  WHERE N72_KODAS_MS = '" + age +"'"; 
	 	
	   System.out.println(sql1);
	   String acc = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	     acc = rs1.getString("carplate").trim();

	   }
	   rs1.close();
	   st1.close();

	   return acc;

	 }
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
     " FROM n17_prod a LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps " +
   	"  WHERE N17_KOD_T = '" + ean.trim() + "'";

    String sql2 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
    " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps " +
   	"  WHERE  N17_KODAS_PS = '" + prodID.trim() + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
 //  System.out.println(ean + " prod0 " + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

   // System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();

    return prod;
  }

  private void setOrder(Order order){
	     this.order = order;
	   }
  private Order order = null;
  
  
  
  private double getProductPrice(String toode) {
	  double res = 0;
	  
	  String    sql = "select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI "
	    		+ " where I33_KODAS_iS = '03' and  I33_KODAS_PS = '" + toode + "'  GROUP BY I33_KODAS_PS order by I33_KODAS_PS " ;
	            System.out.println("hind");
	       try {
	    	   
	      
	            Statement st1 = con.createStatement();
	            ResultSet rst = st1.executeQuery(sql);
	            
	       
	               while (rst.next()) {
	                 res = rst.getDouble("hind");
	                }
	             rst.close();
	             st1.close();
	             
           }catch(Exception e ) {
	    	   
	       }
	  return res;
			  
  }
  
  
private HashSet stats = null;
  
  private Vector splitOrder (Order order){
	  
	   stats = getStats();
	  Vector result = new Vector();
	  Vector lines = order.getOrderLines();
	  
	  Enumeration enumr = lines.elements();
	  
	   
	  if (order.getDokNr() == null || order.getDokNr().trim().length() == 0){
		  order.setDokNr((Calendar.getInstance().getTimeInMillis() + "").substring(3));
	  }
	  
	
	  Order ordorkla = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordorklaret = new Order(order.getCompany(),order.getAgentFirstName());
	 	  
	 	  ordorkla.setAgentCode(order.getAgentCode());
	  ordorkla.setDokNr(order.getDokNr());
	  ordorkla.setCompName(order.getShipMode());
	  ordorkla.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordorkla.setStatus(order.getStatus()); 
	  ordorkla.setInfo(order.getInfo());
	  ordorkla.setReference(order.getReference());
	  ordorkla.setTeostaja(order.getTeostaja());
	  ordorkla.setWarehouse(order.getWarehouse());
	  ordorkla.setDealer(order.getDealer());
	  ordorkla.setShipTime(order.getShipTime());
	 	 

	  ordorklaret.setAgentCode(order.getAgentCode());
	  ordorklaret.setDokNr(order.getDokNr()+"R");
	  ordorklaret.setCompName(order.getShipMode());
	  ordorklaret.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ordorklaret.setStatus(order.getStatus()); 
	  ordorklaret.setInfo(order.getInfo());
	  ordorklaret.setReference(order.getReference());
	  ordorklaret.setTeostaja(order.getTeostaja());
	  ordorklaret.setWarehouse(order.getWarehouse());
	  ordorklaret.setDealer(order.getDealer());
	  ordorklaret.setShipTime(order.getShipTime());
	  
	   lines = order.getOrderLines();
	   enumr = lines.elements();

	   while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		
		   if (stats.contains(lin.getToode())) {
		  		double kord = 1;
		  		if(lin.getKogus() < 0) {
		  			kord = -1;
		  		}
		    		ordorklaret.add(lin.getToode(), lin.getKogus()*kord, lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordorklaret.setDokNr(order.getDokNr()+"R");
			     	ordorklaret.setReference(order.getReference());
		    	}else {
		    		ordorkla.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	ordorkla.setDokNr(order.getDokNr());
			     	ordorkla.setReference(order.getReference());
		    	}
		  
	  }
	  
	  if(ordorkla.getOrderLines() != null && ordorkla.getOrderLines().size()>0){
		  result.add(ordorkla);  
	  }

	  if(ordorklaret.getOrderLines() != null && ordorklaret.getOrderLines().size()>0){
		  result.add(ordorklaret);  
	  }
	  
	
	  
	 System.out.println("Result = " +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }

  
  public void makeOrder(Order order) throws SQLException{
 
	  setOrder(order);
	  
	  
	 Vector vv = this.splitOrder(order);
    
	  for(int k = 0; k<vv.size();k++) {
     	  Order ord = (Order)vv.elementAt(k);
	 
	  boolean tag = false;
	  if(ord.getDokNr().endsWith("R")) {
		  tag = true;
	  }
	  
	   Vector lines = ord.getOrderLines();
	   
	    pandikas = new HashMap();
	     for (int i = 0; i < lines.size(); i++) {
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	         String pant = getPandiPakend(toode);
	          if(pant != null ){
	        	  double aaa = line.getKogus();
	        	  if(toode.equals("405340094252")) {
	        		  aaa = aaa * 5;
	        	  }
	         	  if(pandikas.get(pant) == null){
	         		  pandikas.put(pant,aaa+"");
	               } else {
	               	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+(aaa))+"");
	               }
	            }
	         }
	  
	  
	  System.out.println("On Tagastus " + tag );
    makeOutput(ord,tag );
	  }
  }
  
 private HashMap pandikas = null;

 private void makeOutput(Order order,boolean tag){
 	StringBuffer sb = new StringBuffer();
	
  
 	
 	String ls = System.getProperty("line.separator");
 	try {
 	  int cou = 1;
     // sb.append(getLineNr(cou));
      sb.append("       <I06>" + ls);
      cou++;

      String type = "51"; //tellimus
      if(tag) {
    	  type = "52";
      }
      //sb.append(getLineNr(cou));
      sb.append("          <I06_OP_TIP>" + type + "</I06_OP_TIP>" + ls);
      cou++;

      //sb.append(getLineNr(cou));
      sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);
      cou++;
      String cust = order.getCompany();

      
      String agent = order.getAgent();
      String ov21 = "";
      
      String comm = "";
      if(order.getInfo() != null && order.getInfo().trim().length() >0){
	        comm = order.getInfo().replaceAll("\\r\\n|\\r|\\n", " ");
      }
      
      sb.append("          <I06_KODAS_MS>" + escape(agent) + "</I06_KODAS_MS>" + ls);
      cou++;
      
      if(tag) {
          sb.append("          <I06_ISAF>5</I06_ISAF>" + ls);
          cou++;  
      }
     
      
      
      String name =  null;
   try {
  		name = identifyCustomerName(cust);
  		if(name == null){
  			name ="nonam";
  		}else{
  			name = name.replaceAll("UAB", " ").trim();
  	  		if(name.length()>5){
  	  			name = name.substring(0,5);
  	  		}	
  		}
  		
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}

          sb.append("          <I06_OP_DATA>" +  getDateString(order.getShipCal()) + "</I06_OP_DATA>" + ls);
      cou++;
      
      sb.append("          <I06_DOK_DATA>" +  getDateString(order.getShipCal()) + "</I06_DOK_DATA>" + ls);
      cou++;
   String cuid = cust.substring(0,cust.lastIndexOf("-"));
   if(cuid.length() > 8) {
	   String s = cuid.substring(cuid.length()-1);
	   if(!Converter.isNumeric(s)) {
		   cuid = cuid.substring(0,cuid.length()-1);
	   }
	   s = cuid.substring(cuid.length()-1);
	   if(!Converter.isNumeric(s)) {
		   cuid = cuid.substring(0,cuid.length()-1);
	   }
	   s = cuid.substring(cuid.length()-1);
	   if(!Converter.isNumeric(s)) {
		   cuid = cuid.substring(0,cuid.length()-1);
	   }
   }
      
      sb.append("          <I06_KODAS_KS>" + escape(cuid) + "</I06_KODAS_KS>" + ls);
      cou++;
      
      sb.append("          <I06_KODAS_AK>" + escape(cust.substring(cust.lastIndexOf("-")+1)) + "</I06_KODAS_AK>" + ls);
      cou++;
      
      boolean sta1Eur = isKeg1Euro(cuid);
      boolean sta0Eur = false;
      if(!sta1Eur) {
        sta0Eur = isKeg0Euro(cuid);
      }

      System.out.println("sta1Eur " + sta1Eur);
      System.out.println("sta0Eur " + sta0Eur);
      
      String addr =  null;
		try {
			addr = identifyCustomerADDR(cust);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
      cou++;
      
      String aeg = null;
      if(order.getShipTime() != null && order.getShipTime().trim().length() > 0) {
    	  aeg =   order.getShipTime().trim();
    	  sb.append("          <I06_ADR3>" + escape(aeg) + "</I06_ADR3>" + ls);
          cou++;
      }
      
      String lisa = null;
      if(order.getInfo() != null && order.getInfo().trim().length() > 0) {
    	  lisa = order.getInfo().trim();
      }
      
      
      if(lisa != null){
          sb.append("          <I06_PASTABOS>" + escape(ov21 + lisa) + "</I06_PASTABOS>" + ls);
          cou++;  
      }
      
      sb.append("          <PAP_3>1</PAP_3>" + ls);
      cou++; 
     
    Vector  lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
         sb.append("          <I07>" + ls);
         cou++;
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          
          double ale = line.getDiscount();
          if (ale == -1 ){
          ale = 0 ;
              }
          if(ale == -1 ){
        	  ale = 100;
          }
          
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
          cou++;


  if (tag) {
	  if(toode.length() == 3) {
		 String obj =  this.getVaatObjekt(toode);
		  sb.append("               <I07_KODAS_OS>" +  escape(obj) + "</I07_KODAS_OS>" + ls);
          cou++;
		  toode = changeVaat(toode);
	  }
  }

          sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          cou++;

          double kord = 1;
          if(tag && line.getKogus() < 0) {
        	  kord = -1;
          }
          if(tag) {
          sb.append("               <I07_KIEKIS>" + (int)(line.getKogus() * kord) + "</I07_KIEKIS>" + ls);
          cou++;
          }else {
        	  sb.append("               <T_KIEKIS>" + (int)(line.getKogus() * kord) + "</T_KIEKIS>" + ls);
              cou++;  
          }

          if (!tag) {
        	  String unit = line.getUnit() ;
        	  if(toode.equals("102010") || toode.equals("506063373053") || toode.equals("506063373010")  || 
        			  toode.equals("506063373005") || toode.equals("506063373006") || toode.equals("506063373009") 
        			  || toode.equals("31320001") || toode.equals("31220001")  ) {
        		  unit = "PET20.0";
        	  }
        	  
          sb.append("               <I07_KODAS_US>" + unit + "</I07_KODAS_US>" + ls);
          cou++;
          }
    
  if (ale != 0 ){
	 //    sb.append("               <I07_KAINA_BE>" + escape(nf.format(line.getPrice())) + "</I07_KAINA_BE>" + ls);
       //  cou++;
  	
         sb.append("               <I07_NUOLAIDA>" + escape(getNF().format(ale)) + "</I07_NUOLAIDA>" + ls);
          cou++;
    } else if(tag) {
    	
    	
    	
	  sb.append("               <I07_KAINA_BE>" + getKegPrice(line.getToode(),sta1Eur , sta0Eur) + "</I07_KAINA_BE>" + ls);
      cou++;
    }
  
  addNumLine();
          sb.append("          </I07>" + ls);
          cou++;
      }
      
      
        if( pandikas.size()>0){
    	Iterator enumpant = pandikas.keySet().iterator();
     while(enumpant.hasNext()){
     	
     	String toode = (String)enumpant.next();
    	double pand = new Double((String)pandikas.get(toode)).doubleValue();
  	  
   sb.append("          <I07>" + ls);
   cou++;
  
    sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
    cou++;

    sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
    cou++;
    double kord = 1;
    /*094-deposits of bottle
096-deposits of can
095 deposits of pet
*/
    String price = "0.10";
    if(stats.contains(toode)) {
    	price = getKegPrice(toode,sta1Eur,sta0Eur);
    }
    
    sb.append("               <I07_KAINA_BE>" + price + "</I07_KAINA_BE>" + ls);
    cou++;
    sb.append("               <I07_KAINA_SU>" + price + "</I07_KAINA_SU>" + ls);
    cou++;
    sb.append("               <I07_KIEKIS>" + escape(getNF().format(pand * kord)) + "</I07_KIEKIS>" + ls);
    cou++;
   
    sb.append("          </I07>" + ls);
    cou++;
 	  }
    }

     
      sb.append("          </I06>" + ls);
      cou++;

      String cat = parent.getProps().getProperty("conv.dir");
      if(tag) {
    	 cat = parent.getProps().getProperty("conv.dirret");
      }
 	
      String fileName = escape(cat + order.getAgent()) + "-" + name + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
      
      Converter.saveFile(fileName, sb.toString());
      
      if(APIKEY != null) {
          String data = "	<body><method>EDIT_I06_FULL</method><data>" + sb.toString() + "</data>" + 
          		"</body>";
          System.out.println(data);
          
          HttpResponse aaa = restPostRequest(data);
         
          String sbb = EntityUtils.toString(aaa.getEntity());
          System.out.println("Response " + sbb);
       //  StringBuffer sbb = postData(data);
       //  System.out.println("Response " + sbb);
         
          
          fileName = escape(cat + order.getAgent()) + "-" + name + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  "response.txt";
         System.out.println(fileName);
         FileOutputStream  fos = new FileOutputStream(fileName);
           fos.write(sbb.toString().getBytes());
           fos.close();
           if(sbb.contains("<h1>Error: Server Error") || sbb.contains("Connection refused") ||  sbb.contains("connect timed out") ||  sbb.contains("errorMessage")) {
              
          	   this.sendOrderEmail(order, tag, sbb);
          	      
          	 System.exit(0);	
           }
           if(sbb.contains("<errorMessage></errorMessage>") ) {

        	   this.sendOrderEmail(order, tag, sbb);
               System.exit(0);	
            }
          }
   
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
 
 private void sendOrderEmail(Order ord , boolean ret , String error){
	  
     
     try {

String address = parent.getProps().getProperty("errormail");

String cusname = this.identifyCustomerName(ord.getCompany());

if(address != null ){
Email email = new Email("Order Import error 3BS: " + order.getCompany() + " " + cusname);
email.setFrom(parent.getProps().getProperty("from"));
email.setSMTP(parent.getProps().getProperty("smtphost"));
email.setUser(parent.getProps().getProperty("smtpuser"));
email.setPass(parent.getProps().getProperty("smtppass"));
email.setPort(parent.getProps().getProperty("smtpport"));
email.setSubjectFilesTo("Order Import error : " + cusname);

email.setEncoding("iso-8859-1");
try {

email.setBody("Order Import error 3BS \n " + error );
StringTokenizer st =  new StringTokenizer(address,";");
String aadr = null;
while(st.hasMoreTokens()){
	 aadr = new InternetAddress(st.nextToken()).toString();
email.setFilesTo(aadr);
email.send();
}
System.out.println("mail saatmisel " );

} catch (AddressException e) {
log.error(e);
e.printStackTrace();
System.exit(0);
} catch (MessagingException e) {
// TODO Auto-generated catch block
e.printStackTrace();
log.error(e);
System.exit(0);
}
	  
}
 } catch (Exception e) {
		e.printStackTrace();
		log.error(e);
		System.exit(0);
 }
     
     System.out.println("mail saadetud");
	
}

  
 private HttpResponse restPostRequest(String data) throws Exception {

	 HttpPost httpPost = new HttpPost(this.logurl);
	 if(data != null){
		 httpPost.setEntity(new StringEntity(data,"utf-8"));
		  httpPost.setHeader("ApiKey", APIKEY);
	        httpPost.setHeader("Accept", "application/xml");
	        httpPost.setHeader("Content-type", "application/xml;charset=utf-8");
	 }
        return HttpClients.createDefault().execute(httpPost);

}





 
 private String getKegPrice(String toode, boolean sta1Eur, boolean sta0Eur ) {
	 String res = "30.00";

	 /*031 Statinė Westmalle 20L
 	030 Statinė Haacht 20L
 	029 Statinė Haacht 15L
 	032 Statinė Brewery Trade 15L       
    */
	 if(toode.equals("031") || toode.equals("030") || toode.equals("029") || toode.equals("032") ) {
		 res = "75.00";
	 }
	 
	 if(sta1Eur) {
			res = "1.00";	
		} 

	 if(sta0Eur) {
			res = "0.00";	
		} 
	 
	 return res;
 }
  public String getDriverVersion(){
    return "RivileSB3Import";
  }
  
   private String  getVaatObjekt(String toode) {
	   String res = null;
	   if(toode.equals("022")) {
		   res = "04";
	   }
	   if(toode.equals("023")) {
		   res = "06";
	   }
	   if(toode.equals("024")) {
		   res = "09";
	   }
	   if(toode.equals("025")) {
		   res = "52";
	   }
	   if(toode.equals("026")) {
		   res = "33v";
	   }
	   if(toode.equals("027")) {
		   res = "33o";
	   }
	   if(toode.equals("028")) {
		   res = "33g";
	   }
	   
	   if(toode.equals("030")) {
		   res = "11";
	   }
	   if(toode.equals("031")) {
		   res = "44";
	   }
	   if(toode.equals("029")) {
		   res = "11";
	   }
	   if(toode.equals("032")) {
		   res = "06";
	   }
	   /*


		   04 - barrel 20L:
		   it must be 11 („objektas“)-haacht statinė            -> barcode from invoice 030
		   it must be 44 („objektas“)-westmalle statinė    -> barcode from invoice 031

		   08 - barrel 15L:
		   it must be 11 („objektas“)-haacht statinė             -> barcode from invoice 029
		   it must be 06 („objektas“) -k.brewery statinė      -> barcode from invoice 032
		   			*/																																																	
	   
	   
	   return res;
   }
   private String changeVaat(String toode) {
	   
	   String res  = toode;
	   if(toode.equals("022") || toode.equals("023") ||  toode.equals("024") || toode.equals("025") || toode.equals("026") || toode.equals("027") || toode.equals("028")) {
		   res = "02";
	   }
	   if(toode.equals("030") || toode.equals("031") ) {
		   res = "04";
	   }
	   if(toode.equals("029") || toode.equals("032") ) {
		   res = "08";
	   }

	   return res;
   }
  
  private void sendConfirmEmail(String comment, Order ord , String cuname , String delaadr, String ov21, boolean overDisc){
	   	try{
	   		
	   			
	   			System.out.println(comment);
	   			Email mail = new Email(null);
	   			String ll = "";
	   			if(overDisc) {
	   				ll = "OVERDISCOUNTED ";
	   			}
		  		   String sub = ll + ov21 + "Order by : " + ord.getAgent() + " " + ord.getCompany() + " " + cuname + " " + delaadr;
		  	
		  		   if(ov21.length() > 1 || overDisc) {
		  			   mail.setImportance(Email.IMPORTANCE_HIGH);
		  		   }
		            mail.setSMTP(parent.getProps().getProperty("smtphost"));
		            mail.setUser(parent.getProps().getProperty("smtpuser"));
		            mail.setPass(parent.getProps().getProperty("smtppass"));
		            mail.setPort(parent.getProps().getProperty("smtpport"));
		  	  		mail.setFilesTo(parent.getProps().getProperty("confemail"));//pasutijumi@fazer.com
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setSubjectFilesTo(sub);
		  	  		mail.setEncoding("utf-8");
		  	    	mail.setBodyHtml(getOOSEmailText(ord, ord.getAgent() , cuname , delaadr , overDisc ,ov21 ));
		  	  		mail.setSendNotice(false);
		  	  		mail.send();
		  	  
		  	  		System.out.println("Request Sent");
		  	
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
  

  
  private String getOOSEmailText( Order ord, String agent, String custname, String addr, boolean overDisc, String ov21) throws SQLException {
		int i = 0;
		int j = 0;
		 String text = order.getInfo();
		  
		   if(text != null && text.trim().length() > 0){
			   text = text.trim();
		   }else{
			   text = "";	
			   }
		
		StringBuffer sb = new StringBuffer();
	    sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>Klientas:</b> " + custname + " " + addr +  "</font> ") ;
	    sb.append("<font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;" + "" + " </font>") ;
		sb.append("</font>");
	    sb.append("<font face=\"Arial\" size=\"2\" color=\"#E42217\"><span style=\"float: right\">  Prist. data "
				+ Converter.getDDpMMpYYYYpString(ord.getShipCal()) + "</span></font><br> ") ;
	    	
	
		sb.append("<font face=\"Arial\" size=\"2\" color=\"#000000\"><span style=\"float: right\"> <b>" 
				+ ("Agents ") + ":</b> " +" " +  ord.getAgent()+ "</span></font> <br>") ;
 
		if(text.length() > 0) {
		 sb.append("<font face=\"Arial\" size=\"3\"  color=\"#382D2C\"><b> Komentaras : " + text + " " + ov21 + " </b>" );
		}
		sb.append("<br><br>" );
														
		sb.append("	<style type=\"text/css\">");
		sb.append("table.tb-table {");
		sb.append("border: 1px inset #98bf21; font-family: Arial, Helvetica, sans-serif");
		sb.append("font-size: 12px;} ");
		sb.append(".tb-table td {");
		sb.append("	padding: 4px;");
		sb.append("	margin: 3px;");
		sb.append("		border: 1px inset #FF3399;}");
		sb.append(".tb-table th {");
		sb.append("	background-color: #FFF; ");
		sb.append("color: #00000;");
		sb.append("		font-weight: bold;}");
		sb.append("</style><table class=\"tb-table\">" );
		sb.append("<tr><th>Nr.</th><th>Kodas</th><th>Pavad</th><th>Kiekis</th><th>Nuol.</th><th>Kaina €</th></tr>" );

		
			for (; i < ord.getOrderLines().size(); i++) {
				OrderLine line = (OrderLine) ord.getOrderLines().elementAt(i);
				String discS = "";
				 Double price = line.getPrice();
				 if(line.getDiscount() > 0 ) {
					// price = price * (100-line.getDiscount())/100;
					 discS = getNF().format(line.getDiscount()) + "%";
				 }
				
				 
				String priceS = getNF().format(price) ;
				
				
				
				String col = "";
				String part = "";
				if(line.getPartii() != null && line.getPartii().trim().length() > 0) {
					part = "<br>" +line.getPartii().trim();
					col = "color: red; font-weight: bold;";
				}
				
				
					sb.append("<tr><td>" + (i+1) + ".</td><td>" + line.getToode() + 
							"</td><td><div style=\"min-width: 250px\" >" + identifyProductName(line.getToode()) + part + "</div></td>"
									+ "<td><div style=\"min-width: 150px\" >" + line.getKogus() + "</div></td>"
											+ "<td><div  style=\"min-width: 200px; "+col+"\" >" + discS + "</div></td>"
													+ "<td><div style=\"min-width: 200px\" >" + priceS + "</div></td></tr>" );
				j++;
			}
			
		String ordSum =	getNF().format(getCalcSum(ord.getOrderLines())) + " €";
		sb.append("</table><br><font face=\"Arial\" size=\"3\" color=\"#000000\"><span style=\"float: right\"> <b>" 
				+ "Suma  " + ":</b> " +" " +  ordSum+ "</span></font> <br>") ;
		System.out.println("ordSum " + ordSum);
		return sb.toString();
	}


 
  private double getCalcSum(Vector orderLines){
	  double res = 0 ;
	 Enumeration enume =  orderLines.elements();
	 while(enume.hasMoreElements()){
		 OrderLine ol  = (OrderLine)enume.nextElement();
		 Double price = ol.getPrice();
		 if(ol.getDiscount() > 0 ) {
			// price = price * (100-ol.getDiscount())/100;
		 }
		 res = res + (price * ol.getKogus());
	 }
	  return res;
  }



  private String escape(String s){
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

  
  
  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    sb.append(cal.get(Calendar.YEAR) + ".");
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month + ".");
//2005.09.05 00:00
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day );

    return sb.toString();
  }

  
  public Product getProduct(String toode){
   Product prod = null;
 String cmd = " SELECT N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus,dbo.papn(L11_LAUKAS , 8, 'c')  AS tykke,dbo.papn(L11_LAUKAS , 9, 'c')  AS alusel, 0 AS myygihind," +
 		"N17_KODAS_PS AS ean, "+
 " 0 AS kogus,n17_kodas_ls_5 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
 " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
 " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1 where  N17_POZ_DATE !=1" +
 "  and  N17_KODAS_PS = '" + toode +"'" ;
   
          System.out.println(cmd);

      try {
      	
    	  Statement st1 = con.createStatement();
    	    ResultSet rs = st1.executeQuery(cmd);

          while (rs.next()) {
            String kood = rs.getString("toode").trim();
            prod = new Product(kood);
            prod.setUnit("VNT");
            String nimetus = rs.getString("nimetus").trim();
            prod.setName(Converter.enCodeEst(nimetus));
            try{
            prod.setInCases(rs.getDouble("tykke"));
          } catch (Exception e){}
            
            try{
                prod.setOnPallet(rs.getDouble("alusel"));
              } catch (Exception e){}
     
            String ean = rs.getString("ean");
            if(ean != null) {
            	
            if( ean.trim().length()>11 && !ean.startsWith("0")){
          	  try{
          		  EAN13CheckDigit ee = new EAN13CheckDigit();
          		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                    prod.setEAN(ean2);	
          		  prod.setName(prod.getName() + " " + ean2.substring(ean2.length()-4));
          	  }catch(Exception y){
          		  
          	  }
            }
            }
        
            String grupp = rs.getString("grupp");
           if (grupp != null && grupp.trim().length()>0){
             prod.setGroup(grupp.trim());
           }
         

            
             prod.setVat("21");
             
            

          }
          rs.close();
          st1.close();
      } catch (Exception e) {
          e.printStackTrace();
          }

      return prod;
  }

  private HashSet getStats(){
	   HashSet res = new HashSet();
	   String cmd = " SELECT N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus, N37_KODAS_US as unit ,0  AS tykke,0  AS alusel, 0 AS myygihind,\r\n" + 
		   		"   		  		N37_BAR_KODAS AS ean, \r\n" + 
		   		"   		    0 AS kogus,n17_kodas_gs AS grupp2, N17_KODAS_VS_T AS grupp,  N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status ,  N35_PAV_K3 as maks,\r\n" + 
		   		"   		    case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus , ltrim(N17_KODAS_LS_8) as sellout \r\n" + 
		   		"   		  , N17_KODAS_LS_8 as maxdisc ,a.* FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1\r\n" + 
		   		"   		  LEFT JOIN  n37_pmat g ON n37_kodas_ps=n17_kodas_ps  and N37_KODAS_US != 'L' \r\n" + 
		   		"   	 left join L02_FILD on L02_kodas_fil = N17_KODAS_PS left join N35_LGST on N35_KODAS_LS= N17_KODAS_LS_5   where  N17_POZ_DATE !=1  " + 
		   		"   ";
	   
	          System.out.println(cmd);

	      try {
	      	
	    	  Statement st1 = con.createStatement();
	    	    ResultSet rs = st1.executeQuery(cmd);

	          while (rs.next()) {
	            String kood = rs.getString("toode").trim();
	            String grupp = rs.getString("grupp2");
	            if(grupp != null  && grupp.trim().equals("STATINES") && kood.length() == 3) {
	            	res.add(kood);
	               }

	          }
	          rs.close();
	          st1.close();
	      } catch (Exception e) {
	          e.printStackTrace();
	          }
	      System.out.println("stats "+ res.toString());
	      return res;
	  }


  
  private String url = "jdbc:odbc:";
  private Connection con;
  private ImportPanel parent;

  public NumberFormat getNF() {
	 NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		return nf;
	}
  
  
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
