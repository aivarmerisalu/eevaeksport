package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.Customer;
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

public class HorizonSmilImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public HorizonSmilImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
   if(parent.getProps().getProperty("wsdl.url") != null){
	   weburl = parent.getProps().getProperty("wsdl.url");
   }
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
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
    return null;
  }


  
  public String identifyProduct(String ean, String prodID) throws SQLException{ 	  
    return null;
}

  
  
  public void makeOrder(Order order) throws SQLException{
       counter = 0;
       
       if (signatures == null){
    	   signatures = new Vector();
       }
       		  String signature = order.getSignature();
    			  if(signatures.contains(signature)){
    				  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" 
    			  + Converter.deCode(order.getCompany()) ;
    			  }else{
    				  makeOutput(order);  
        			 // signatures.add(signature);
        			  this.trouble = null;
        		  }
    	
       try {
		Converter.saveObject(signatures, "signatures.vec");
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
  
  private String weburl = null;
		
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
	  
	 
	   sb.append(  year + "-");
	    if (month.length() == 1){
		     sb.append("0");
		   }
		     sb.append(month  + "-");
	    
	    if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	     if (hour.length() == 1){
		      hour = "0" + hour ;
		    }
	    return sb.toString();
	  }
  
  
  private String getWeb(String req, String req2) throws ParseException, IOException{
	  HttpResponse response = null;
	  try{
		  System.out.println("weburl " + weburl);
		  DefaultHttpClient httpClient = new DefaultHttpClient();
		 // Credentials defaultcreds = new UsernamePasswordCredentials("restmappost", "R");
		  Credentials defaultcreds = new UsernamePasswordCredentials(parent.getProps().getProperty("wsdl.user"),  parent.getProps().getProperty("wsdl.pass"));
		  
		  httpClient.getCredentialsProvider().setCredentials(
                   AuthScope.ANY, defaultcreds);
		  HttpPost postRequest = new HttpPost(weburl);
		  StringEntity input = new StringEntity(req);
		  input.setContentType("application/xml");
		  postRequest.setEntity(input);
		  response = httpClient.execute(postRequest);	  
	  }catch(Exception e){
		  trouble = "NOT IMPORTED ORDER \n" + req2;
		  e.printStackTrace();
	  }
	  return  EntityUtils.toString(response.getEntity());
  }
	  
  
 private boolean isBeforeDay(Calendar cal , Calendar ship){
	  
	  Calendar cc = Calendar.getInstance();
	  cc.setTimeInMillis(cal.getTimeInMillis());
	  cc.set(Calendar.HOUR, 1);
	  cc.set(Calendar.MINUTE, 1);
	  cc.set(Calendar.SECOND, 1);
	  cc.set(Calendar.MILLISECOND, 1);
	  
	  Calendar ss = Calendar.getInstance();
	  ss.setTimeInMillis(ship.getTimeInMillis());
	  ss.set(Calendar.HOUR, 1);
	  ss.set(Calendar.MINUTE, 1);
	  ss.set(Calendar.SECOND, 1);
	  ss.set(Calendar.MILLISECOND, 1);
	  if(cc.getTime().before(ss.getTime())){
		  return true;
	  }
	  return false;
  }
	

 private boolean isToday(Calendar cal){
	  Calendar cc = Calendar.getInstance();
	  if(cc.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)){
		  return true;
	  }
	  return false;
 }
 
 
 private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(parent.getProps().getProperty("dsn.url"), 
	    		parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

 
 public Customer getCustId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	  	
	    String cmd = "SELECT  K.PK_KLIENTS AS id, K.KODS AS kood, K.NOSAUK AS nimi FROM   APV07062.DKLIENTS K " +
	  			" WHERE K.KODS = '" +kood+"'" ;  

	  	Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    Customer liik = null;
	    if (rs.next()) {
	        liik = new Customer(rs.getString("id").trim());
	        String name = rs.getString("nimi").trim();
	        liik.setName(name);
	      }
	    rs.close();
	    st.close();
	    con.close();
	    return liik;
	}

 public String getProdId(String kood) throws SQLException {

	    Connection con = getNewConnection();
	    

	  	
	    String cmd = "SELECT PK_NOM AS id FROM   APV07062.NNOM t " +
	  			" WHERE t.KODS = '" +Converter.deCode(kood)+"'" ;  

	  	Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    String liik = null;
	    if (rs.next()) {
	      liik = rs.getString("id").trim();
	      }
	    rs.close();
	    st.close();
	    con.close();
	    return liik;
	}
  
  
  private void makeOutput(Order order){
	
	 	try {
	 	
	 		String comm = "";
	 		if(order.getInfo() != null && order.getInfo().trim().length() >0){
	 			comm =  order.getInfo() ;
	 		}
			
	 		String doknr = order.getDokNr();
	 		 Calendar  shipDate = order.getShipCal();

	 		  Calendar cal = Calendar.getInstance();
	            if(isToday(shipDate)){
	            	 	if(shipDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
	   	            		shipDate.add(Calendar.DATE, 2);
	   	            	}else{
	   	            		shipDate.add(Calendar.DATE, 1);
	   	            	}
	   	             	
	            }
	         
		           if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
		        	   cal.add(Calendar.DATE , 2);
		           }else{
		        	   cal.add(Calendar.DATE , 1);
		           }
		           
		           if (shipDate != null && isBeforeDay(cal,shipDate) ) {			        	
		               cal = shipDate;
		               }        	    
		           if(order.isShipDateUsed()) {
		        	   cal = order.getShipCal();
		           }
		           
		        String cukood =  Converter.deCode(order.getCompany());
		        String adid = null;
		        if(cukood.indexOf("_")!= -1){
		        	adid =  escape(cukood.substring(cukood.lastIndexOf("_")+1));
		        	cukood = escape(cukood.substring(0,cukood.lastIndexOf("_")));
			    }
		           
		           Customer cuid = getCustId(cukood);
		
		           String aadr = "<NdmPvzAddition>";
		           if(adid != null){
		        	   aadr =  " <NdmPvzAddition>";
		           }
		         
		           String agg = order.getAgent();
		        
		           String  order_xml = "<resource> <description>Merisoft Order</description>" +
		           "<entity xsi:schemaLocation=\"TNdmPvzSale.xsd TNdmPvzSale.xsd\" xmlns=\"TNdmPvzSale.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
	 		    "   <PK_DOKT> " +
		           //rest/TLgPiepDBL/template
        " <href>/rest/TLgPiepDBL/template/361</href> " +
        " <title>NPMERI : Order Merisoft</title> " +
        " </PK_DOKT>" +
        " <PAMV_ID>2711</PAMV_ID> "  + 
        " <PK_IESTADE><href>/rest/TdmDIestadeBL/1</href></PK_IESTADE>" +
       // " <PK_PVNK><href>/rest/TdmPvnKat/1</href></PK_PVNK>" +
        " <PK_R_ES><href>/rest/TDdmSaviRek/7092</href></PK_R_ES> " + 
        " <PK_KLIENTS><href>/rest/TDdmCustomer/" + cuid.getID() + "</href></PK_KLIENTS> "+   
        " <DAT_DOK>" + getDateNiceString(Calendar.getInstance()) + "</DAT_DOK>" + //getDateNiceString(Calendar.getInstance())
        " <NEED_DATE>" + getDateNiceString(cal) + "</NEED_DATE>" + 
        " <PK_PARD><href>/rest/TNdmPard/" + agg + "</href></PK_PARD>" + 
        " <PK_VAL> <href>/rest/TsdmValName/3</href><title>EUR</title></PK_VAL><PAMAT>TextOnAccount</PAMAT><TIPS>1</TIPS>" +
        " <PK_PROJ><href>/rest/TdmProject/1</href><title>V : Visi projekti</title></PK_PROJ>" +
        "<PK_ADRESE><href>/rest/TNdmAdr/" + adid + "</href></PK_ADRESE>" +
       aadr +   
        " <PVN_TIPS>0</PVN_TIPS> " +
		" <PK_NDARAPR><href>/rest/TdmNDarAprBL/1</href></PK_NDARAPR></NdmPvzAddition><NOTES>" + comm+ "</NOTES>" ; 
      
      
      
      order_xml = order_xml + "<LgPiepRows>";

	     	 Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		    	  OrderLine line = (OrderLine) lines.elementAt(i);
		    	  String prid = getProdId(line.getToode());
		         order_xml = order_xml + "<row>";
		         order_xml = order_xml +  "<RN_VEIDS>0</RN_VEIDS> ";
		         order_xml = order_xml +  "<PK_NOM>" + prid + "</PK_NOM>";
		          //  <!--KODS>113</KODS-->
		          // <!--BAR_KODS>4751012090010</BAR_KODS-->
		         order_xml = order_xml +  "<DAUDZ>" + line.getKogus() + "</DAUDZ>";
		         //   <TEKSTS>Rindā Piezīmes</TEKSTS>
		         //  <TEMPERAT>Rindā raksturojums</TEMPERAT>
			      order_xml = order_xml +  "<V_KODS>" + line.getUnit() + "</V_KODS>";
			      if(line.getDiscount() == -2) {
			    	  order_xml = order_xml +"<CENA>0.01</CENA>";
                      order_xml = order_xml +"<ATLAIDE>99.99</ATLAIDE>";
			      }else  if(line.getDiscount() > 0 || line.getDiscount() < -1){
                      order_xml = order_xml +"<CENA>"+ line.getPrice() + "</CENA>";
                      order_xml = order_xml +"<ATLAIDE>"+ line.getDiscount() + "</ATLAIDE>";
			      }
			      order_xml = order_xml + "</row>";
			     counter++;
	
		      }
		      
		
	 	order_xml = order_xml + "</LgPiepRows>";
	 	order_xml = order_xml + "</entity>";
	 	order_xml = order_xml + "</resource>";
	
	 	System.out.println("output " + order_xml); 
	 	
	 		
	 	try {
	 		
	 		  String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      
		      String fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-" + fn  +  ".xml";
		      System.out.println(fileName);
		       
		      Converter.saveFileInUTF8(fileName, order_xml);

		      fileName = escape(parent.getProps().getProperty("conv.dir")) +  "t-"  + fn  +  "-response.xml";
		      System.out.println(fileName);
		
		   //   trouble = "IMPORTED ORDER \n" + order_xml;
		      
		      if(parent.getProps().getProperty("smtphost") != null) {
				     Agent ag = new Agent(agg);
				     System.setProperty("mail.smtps.ssl.protocols", "TLSv1.2");
				      ag.setName(agg);
				    //   Converter.sendCommentEmail("New Order " + comm, null, doknr, cukood, cuid.getName(), null, false, ag, parent, null);
				     }
		      if("true".equals(parent.getProps().getProperty("testing"))){
			 		System.out.println("Testing ");
		    	  System.exit(0);
		      }
			 	
	 		String xml = getWeb(order_xml , "DokNr:" + order.getDokNr() + " Company:" + cukood);		
	 	//	System.out.println("Tulemus " + xml);
		     Converter.saveFileInUTF8(fileName, xml);
		     
	      
	      setNumAddedLines(counter); 
	      
	    
	 	
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 		
	 	  trouble = "NOT IMPORTED ORDER \n" + order_xml;
	 	//  System.exit(0);
	 	}

	 	
		} catch (Exception e) {
			e.printStackTrace();

		 	  trouble = "NOT IMPORTED ORDER \n" + order.getDokNr();
		 //	  System.exit(0);
		}
		
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

 
  public String getDriverVersion(){
    return "HorizonSmilImport";
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

  private String trouble = null;
  private int counter = 0;
  private ImportPanel parent;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return trouble;
}
  
}
