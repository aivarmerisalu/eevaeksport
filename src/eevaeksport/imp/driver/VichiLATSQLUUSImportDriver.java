package eevaeksport.imp.driver;

import java.io.FileOutputStream;
import java.io.PrintWriter;
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
import java.util.Iterator;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class VichiLATSQLUUSImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public VichiLATSQLUUSImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    
    if(parent.getProps().getProperty("view.url") != null) {
    	urlV = parent.getProps().getProperty("view.url");
    }
    
    if("true".equals(parent.getProps().getProperty("use.view"))) {

	    view = true;	
    }
    try {
      Class.forName(databaseDriverName).newInstance();
      
     
    } catch (Exception e){
       parent.showError(e);
    }
  }
private boolean view = false;
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  private HashMap prodNames() {
	  HashMap res = new HashMap();
	  
	 	try{
	 	    String  sql2 =  " select * from  MobilejavaVIK.v_LAT_GetProducts ";
	 	    if(view) {
	 	    	sql2 = "select * from MobileJavaLAT.v_LAT_GetProducts";
	 	    }
	 	    System.out.println(sql2);

	 	   Connection con = DriverManager.getConnection(parent.getProps().getProperty("view.url")   , parent.getProps().getProperty("view.user"), parent.getProps().getProperty("view.pass"));

	 	    Statement st2 = con.createStatement();
	 	    ResultSet rs2 = st2.executeQuery(sql2);

	 	    while (rs2.next()) {
	 	    	String kood = rs2.getString("toode");
	 	    	String nimi = rs2.getString("nimi");
	 	    	if(kood != null && nimi != null && kood.trim().length() > 0 && kood.trim().length() > 0) {
	 	    		  res.put(kood.trim(), nimi.trim());
	 	    	}
	 	    

	 	    }
	 	    rs2.close();
	 	    st2.close();
	 	    con.close();
	 	    
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
		  System.out.println("nimed " + res.size());
		  return res;
  }

  public String identifyCustomer(String id,String regnr)  {

    String acc = null;

    	
    	try{
    String sql2 = " select [Customer No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer Cross]  = '" + id.trim() + "'";
    System.out.println(sql2);

   Connection con = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("klient").trim();

    }
    rs2.close();
    st2.close();
    con.close();

    


  	if (acc == null){

  	String sql1 = "select [Customer No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer Cross Reference]  where [Customer No_]  = '" + id + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}
  	
  	
  	
  	
  	if (acc == null){

  	String sql1 = "select [No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer]  where [EDI ILN]  = '" + id + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}

  	if (acc == null && regnr != null && regnr.trim().length() > 0){

  	String sql1 = "select [No_] AS klient " +
      " FROM [Vičiūnai ir Ko$Customer]  where [EDI ILN]  = '" + regnr + "'";
    System.out.println(sql1);
    Connection con3 = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("klient").trim();

    }    
    rs1.close();
    st1.close();
    con3.close();
  	}  	
  
    	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
  	if(acc == null){
  		acc = id;
  	}
  	
    System.out.println("Klient: " + acc);
    return acc;

  }


  public String identifyAgent(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Salesperson Code] AS agent " +
	      " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);

	    Connection con3 = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));


	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	    con3.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identKett(String customer)  {

	    String agent = null;

	    	
	    	try{

	  	String sql1 = "select [Tinklas] AS agent " +
	      " FROM [Vičiūnai ir Ko$Customer]  where [No_]   = '" + customer + "'";
	    System.out.println(sql1);

	    Connection con3 = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
	            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      agent = rs1.getString("agent").trim();

	    }    
	    rs1.close();
	    st1.close();
	    con3.close();
	  	
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  	
	    return agent;

	  }


  
  public String identifyProduct2(String ean, String prodID) throws SQLException{

    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "' order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai ir Ko$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

   
    String prod = null;
    Connection con = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);

  	System.out.println(sql2);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	  //System.out.println("prod1 " + prod );
    }
    
	System.out.println("prod2 " +sql1);
    if(prod == null){	
      rs1 = st1.executeQuery(sql1);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      //	System.out.println("prod2 " + prod  + sql1);
      }
    }
    st1.close();
    rs1.close();
   con.close();


    System.out.println("prodFINAL " + prod + " " + ean + " " +prodID);

    return prod;
  }

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	  String tyyp = "";
	  if(prodID.indexOf("|") != -1){
		  tyyp = "and [Cross-Reference Type No_] ='" + prodID.substring( prodID.indexOf("|")+1) + "'";
		  prodID = prodID.substring(0, prodID.indexOf("|"));
	  }
	  
    String sql1 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
    		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
			"  WHERE [Brūkšninis kodas] ='" + ean + "'  order by [Blocked] "; // and [Blocked]=0


    String sql2 = " SELECT [No_] AS toode FROM [Vičiūnai ir Ko$Item] " +
			"  WHERE [No_] = '" + prodID + "' "; // and [Blocked]=0 

    
    String sql3 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] "
    		+ "where [Cross-Reference No_]='" + prodID + "' and [Cross-Reference No_] !='' " + tyyp ;
    String sql6 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] "
    		+ "where [Cross-Reference No_]='" + ean + "' and [Cross-Reference No_] !='' " + tyyp ;
 String id = null;
    if(prodID != null){
	    id = prodID.replace('.', ',');
   }
   

    String sql4 = "select [Item No_] as toode from [Vičiūnai ir Ko$Item Cross Reference] where [Cross-Reference No_]='" + id + "'";
   
    String prod = null;
    Connection con3 = DriverManager.getConnection( parent.getProps().getProperty("dsna.url")
            , parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));

    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql3);

  	//System.out.println(sql3);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	//System.out.println("prod1 " + prod );
    }
    
    if(prod == null){	
        rs1 = st1.executeQuery(sql6);
        if (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	//System.out.println("prod2 " + prod  + sql1);
        }
      }
    
	//System.out.println("prod2 " +sql1);
    if(prod == null){	
      rs1 = st1.executeQuery(sql1);
      if (rs1.next()) {
        prod = rs1.getString("toode").trim();
      	//System.out.println("prod2 " + prod  + sql1);
      }
    }
	//System.out.println("prod2 " + prod  + sql1);
    if(prod == null ){
    	
        rs1 = st1.executeQuery(sql2);
        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	System.out.println("prod3 " + prod  + sql3);
        }
      }
    //System.out.println("prod4 " + prod  + sql4);
    if(prod == null && prodID != null){
    	
        rs1 = st1.executeQuery(sql4);
        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        	//System.out.println("prod4 " + prod  + sql4);
        }
      }
   // st1.close();
    rs1.close();
   


if(prod == null){
	 String eanabi = ean;
	    if(eanabi != null && eanabi.length()>7){
	    	eanabi = eanabi.substring(0,7);
	    }
	    
	    String sql5 = "SELECT [No_] AS toode  FROM [Vičiūnai ir Ko$Item] LEFT JOIN [Vičiūnai ir Ko$Item Unit of Measure]" +
		" vat ON (vat.[Item No_]=[No_] and vat.[Code]='DĖŽ.' ) " +
		"  WHERE left([Brūkšninis kodas],10) ='" + eanabi + "...' and [Blocked]=0 ";
	   // System.out.println("prod5 " + prod  + sql5);
    rs1 = st1.executeQuery(sql5);
    if (rs1.next()) {
      prod = rs1.getString("toode").trim();
    	//System.out.println("prod5 " + prod  + sql5);
    }
  }

System.out.println("prod" + prod +" "+ ean+ " " + prodID);

    st1.close();
    con3.close();
   if(prod == null){
   	prod = prodID;
   }
   
    return prod;
  }

HashMap paILNs = null;

  private HashMap getCustILNs() {
	  HashMap res = new HashMap();
	  
	  paILNs = new HashMap();
	 	try{
	 	    String sql2 = " SELECT k.kood as kood1 ,k. [Ship-to-address code] as kood2, k.[Ship-to Address ILN] as kood3 , k.[Customer ILN] as kood4 " + 
	 	    		"	 	    		from  MobileJavaLAT.v_LAT_Customers k where k.[Customer ILN] != '' ";
	 	    System.out.println(sql2);

	 	   Connection con = DriverManager.getConnection(urlV   , parent.getProps().getProperty("view.user"), parent.getProps().getProperty("view.pass"));

	 	    Statement st2 = con.createStatement();
	 	    ResultSet rs2 = st2.executeQuery(sql2);

	 	    while (rs2.next()) {
	 	    	String id = rs2.getString("kood1").trim();
				String id2 = rs2.getString("kood2");
				String id3 = rs2.getString("kood3");
				String id4 = rs2.getString("kood4");
				if(id2 != null && id2.trim().length() > 0) {
					id = id + "_" + id2.trim();
				}
	 	    	if(id != null && id2 != null && id.trim().length() > 0 && id2.trim().length() > 0) {
	 	    		  res.put(id, id3);
	 	    		  if(id4 != null && id4.trim().length() > 0) {
	 	    		    paILNs.put(id, id4.trim());
	 	    		  }
	 	    	}
	 	    

	 	    }
	 	    rs2.close();
	 	    st2.close();
	 	    con.close();
	 	    
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
		  
		  return res;
  }
  
  private HashMap getILNs() {
	  HashMap res = new HashMap();
	  
	 	try{
	 	    String sql2 = "SELECT k.kood as kood1 ,k.[Ship-to Address ILN] as kood2 "
	 	    		+ " from  MobileJavaLAT.v_LAT_Customers k where k.[Ship-to Address ILN] != '' ";
	 	    System.out.println(sql2);

	 	    
	 	   Connection con = DriverManager.getConnection(urlV   , parent.getProps().getProperty("view.user"), parent.getProps().getProperty("view.pass"));

	 	    Statement st2 = con.createStatement();
	 	    ResultSet rs2 = st2.executeQuery(sql2);

	 	    while (rs2.next()) {
	 	    	String id = rs2.getString("kood1").trim();
				String id2 = rs2.getString("kood2");
				if(id2 != null && id2.trim().length() > 0) {
					id = id + "_" + id2.trim();
				}
	 	    	if(id != null && id2 != null && id.trim().length() > 0 && id2.trim().length() > 0) {
	 	    		  res.put(id, id2);
	 	    	}
	 	    

	 	    }
	 	    rs2.close();
	 	    st2.close();
	 	    con.close();
	 	    
	 	}catch(Exception e) {
	 		e.printStackTrace();
	 	}
		  
		  return res;
  }
  
  public void makeOrder(Order order) throws SQLException{

    
        setOrder(order);
        makeOutput(order);
     
  }

  
  HashMap names = null;
  HashMap ilns = null;
  HashMap ilnsC = null;

	private void makeOutput(Order order) {

	  if(names == null) {
		  names = prodNames();
	  }
	  if(ilns == null) {
		  ilns = getILNs();
	  }
	  
	  if(ilnsC == null) {
		  ilnsC = getCustILNs();
	  }
	  
	  

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element root = doc.createElement("Document-Order");
			doc.appendChild(root);

			// String filiaalStr = null;
			// String tarnijaKoodStr = null;

			Node docheader = doc.createElement("Document-Header");
			root.appendChild(docheader);

			Node un2 = doc.createElement("DocumentType");
			Text unVal2 = doc.createTextNode("MeriSoft");
			un2.appendChild(unVal2);
			docheader.appendChild(un2);

			Node un3 = doc.createElement("DocumentSource");
			Text unVal3 = doc.createTextNode("S");
			un3.appendChild(unVal3);
			docheader.appendChild(un3);
			try {

				long ll = Long.parseLong(order.getAgentCode());
				Node un4 = doc.createElement("DateCreated");

				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date(ll));

				String month = (cal.get(Calendar.MONTH) + 1) + "";
				if (month.length() == 1)
					month = "0" + month;
				String day = cal.get(Calendar.DAY_OF_MONTH) + "";
				if (day.length() == 1)
					day = "0" + day;

				String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
				if (hour.length() == 1)
					hour = "0" + hour;
				String minute = cal.get(Calendar.MINUTE) + "";
				if (minute.length() == 1)
					minute = "0" + minute;

				String second = cal.get(Calendar.SECOND) + "";
				if (second.length() == 1)
					second = "0" + second;

				// [ HH:MM:SS]

				String dateString = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "[" + hour + ":" + minute + ":"
						+ second + "]";

				Text unVal4 = doc.createTextNode(dateString);
				un4.appendChild(unVal4);
				docheader.appendChild(un4);

			} catch (Exception e) {

			}

			String add =  (Calendar.getInstance().getTimeInMillis() + "").substring(10);
			String number = order.getDokNr();
			if (number == null || number.length() == 0) {
				number = (Calendar.getInstance().getTimeInMillis() + "").substring(4);
			}else {
				number = number+add;
			}

			long uni = Calendar.getInstance().getTimeInMillis();
			Node header = doc.createElement("Order-Header");

			Node nr = doc.createElement("OrderNumber");
			Text nrVal = doc.createTextNode("MS-" + number + "");
			nr.appendChild(nrVal);
			header.appendChild(nr);

			Node age = doc.createElement("SalesAgent");
			Text ageVal = doc.createTextNode( order.getAgent());
			age.appendChild(ageVal);
			header.appendChild(age);

			String comm = "";
			if (order.getInfo() != null && order.getInfo().trim().length() > 0) {

				Node com = doc.createElement("Remarks");
				Text comVal = doc.createTextNode(order.getInfo());
				com.appendChild(comVal);
				header.appendChild(com);
				comm = order.getInfo();

				Node com2 = doc.createElement("comment");
				Text comVal2 = doc.createTextNode(order.getInfo());
				com2.appendChild(comVal2);
				header.appendChild(com2);

			}
			System.out.println("Remarks " + comm);

			Node kuupaev = doc.createElement("OrderDate");
			Calendar cal = Calendar.getInstance();

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			String year = cal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			String dateString = year + "-" + month + "-" + day;
			Text kuupaevVal = doc.createTextNode(dateString);
			kuupaev.appendChild(kuupaevVal);
			header.appendChild(kuupaev);

			Node curr = doc.createElement("OrderCurrency");
			Text currVal = doc.createTextNode("EUR");
			curr.appendChild(currVal);
			header.appendChild(curr);

			Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
			Calendar delcal = Calendar.getInstance();
			delcal.setTime(order.getShipCal().getTime());

			month = (delcal.get(Calendar.MONTH) + 1) + "";
			if (month.length() == 1)
				month = "0" + month;
			day = delcal.get(Calendar.DAY_OF_MONTH) + "";
			if (day.length() == 1)
				day = "0" + day;
			year = delcal.get(Calendar.YEAR) + "";
			if (year.length() == 2) {
				year = "20" + year;
			}
			dateString = year + "-" + month + "-" + day;
			Text delkuupaevVal = doc.createTextNode(dateString);
			delkuupaev.appendChild(delkuupaevVal);
			header.appendChild(delkuupaev);

			Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
			Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
			expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
			header.appendChild(expectedDeliveryTime);

			root.appendChild(header);
			
			String cust = order.getCompany();
			 String kood = cust;
			 String ILN = (String)ilns.get(kood);
			 String ILNC = (String)ilnsC.get(kood);
			 if(ILNC == null) {
				 ILNC = "";
			 }
			 String ILNP = (String)paILNs.get(kood);
			 if(ILNP == null) {
				 ILNP = "";
			 }
			 System.out.println("ilnc " + ILNC);
			 
			 if(kood.indexOf("_") != -1) {
				 kood = cust.substring(0,cust.indexOf("_"));
			 }
           
			String lisa = "";
			if(!cust.endsWith("_")) {
				lisa = cust.substring(cust.indexOf("_")+1);
			}else {
				lisa = kood;
			}
			
			Node parties = doc.createElement("Document-Parties");
			Node seller = doc.createElement("DeliveryPoint");
			Node ise = doc.createElement("CodeBySupplier");
			Text nimiVal = doc.createTextNode(kood);
			 ise.appendChild(nimiVal);
			Node ise2 = doc.createElement("ILN");
			Text nimiVal2 = doc.createTextNode(ILNC);
			
			 ise2.appendChild(nimiVal2);
			Node ise3 = doc.createElement("DeliveryILN");
			Text nimiVal3 = doc.createTextNode(ILNC);
			
			Node ise3R = doc.createElement("SenderILN");
			if(ILNP != null) {
		    	Text nimiVal3R = doc.createTextNode(ILNP);
			    ise3R.appendChild(nimiVal3R);
			}
			
			ise3.appendChild(nimiVal3);
			seller.appendChild(ise);
			seller.appendChild(ise2);
			seller.appendChild(ise3);
			seller.appendChild(ise3R);
			parties.appendChild(seller);
			

			Node receiver = doc.createElement("Receiver");
			Node iseR = doc.createElement("CodeBySupplier");
			Text nimiRVal = doc.createTextNode(kood);
			iseR.appendChild(nimiRVal);
			Node ise2R = doc.createElement("ILN");
			if(ILN != null) {
			Text nimiVal2R = doc.createTextNode(ILNC);
			ise2R.appendChild(nimiVal2R);
			}
			receiver.appendChild(iseR);
			receiver.appendChild(ise2R);
			parties.appendChild(receiver);

		

			//parties.appendChild(buyer);

			root.appendChild(parties);

			// RIDADE LISAMINE
			Node read = doc.createElement("Order-Lines");

			int coun = 0;
			Iterator iter = order.getOrderLines().iterator();
			while (iter.hasNext()) {
				OrderLine line = (OrderLine) iter.next();
				String toode = line.getToode();
				if (toode != null) {
					toode = toode.trim();
				} else {
					toode = "";
				}
				if (line.getKogus() != 0 && !toode.equals("")) {

					coun++;
					addLine(line, read, doc, coun, order);
				}
			}

			root.appendChild(read);

			String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr().trim() + (uni + "").trim()
					+ ".xml";
			System.out.println(fileName);
			OutputFormat of = new OutputFormat();
			of.setIndenting(true);
			of.setLineSeparator(System.getProperty("line.separator"));
			of.setEncoding("ISO-8859-1");
			XMLSerializer ser = new XMLSerializer(of);
			FileOutputStream fos = new FileOutputStream(fileName);
			PrintWriter out = new PrintWriter(fos);
			ser.setOutputCharStream(out);
			ser.serialize(doc);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addLine(OrderLine line, Node read, Document doc, int counter, Order order) throws SQLException {

		Node rida = doc.createElement("Line");
		read.appendChild(rida);

		Node ridaitem = doc.createElement("Line-Item");

		Node linenum = doc.createElement("LineNumber");
		Text linnum = doc.createTextNode(counter + "");
		linenum.appendChild(linnum);
		ridaitem.appendChild(linenum);

		/*
		 * Node toote_ean = doc.createElement("EAN"); Text toote_eanVal =
		 * doc.createTextNode(line.getEAN());
		 * toote_ean.appendChild(toote_eanVal); ridaitem.appendChild(toote_ean);
		 */

		String toode = line.getToode();

		Node btoote_kood = doc.createElement("BuyerItemCode");
		Text btoote_koodVal = doc.createTextNode(toode);
		btoote_kood.appendChild(btoote_koodVal);
		ridaitem.appendChild(btoote_kood);

		Node toote_kood = doc.createElement("SupplierItemCode");
		Text toote_koodVal = doc.createTextNode(toode);
		toote_kood.appendChild(toote_koodVal);
		ridaitem.appendChild(toote_kood);

		String nimi = (toode);
		if(names.get(toode) != null) {
			nimi = (String)names.get(toode);
		}
		if (nimi != null) {
			Text nimetusVal = doc.createTextNode(nimi.replace('`', ' ').replace('´', ' '));
			Node nimetus = doc.createElement("ItemDescription");
			nimetus.appendChild(nimetusVal);
			ridaitem.appendChild(nimetus);
		}

		double kogus = line.getKogus();
		double price = line.getPrice();
		double disc = line.getDiscount();
		String unit = line.getUnit();

		Node koGus = doc.createElement("OrderedQuantity");

		Text kogusVal = doc.createTextNode(getNF().format(kogus));
		koGus.appendChild(kogusVal);
		ridaitem.appendChild(koGus);

		if (disc > 0) {
			// price = price * (100-disc)/100;
		/*	Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
			Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
			yhiku_hind.appendChild(yhiku_hindVal);
			ridaitem.appendChild(yhiku_hind);
			*/
			
			
			Node yhiku_ale = doc.createElement("LineDiscount");
			Text yhiku_aleVal = doc.createTextNode(disc+"");
			yhiku_ale.appendChild(yhiku_aleVal);
			ridaitem.appendChild(yhiku_ale);
		}

		Node yhiku_va = doc.createElement("TaxRate");
		Text yhiku_vaVla = doc.createTextNode("21");
		yhiku_va.appendChild(yhiku_vaVla);
		ridaitem.appendChild(yhiku_va);

		Node yhik = doc.createElement("UnitOfMeasure");
		Text yhikVal = doc.createTextNode(unit);
		yhik.appendChild(yhikVal);
		ridaitem.appendChild(yhik);

		rida.appendChild(ridaitem);
		addNumLine();
	}

	public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}

	private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);





  

  public String getDriverVersion(){
    return "VichiLITSQL";
  }

  private void setOrder(Order order){
    this.order = order;
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

  private String url = "jdbc:odbc:";
  private String urlV = "jdbc:odbc:naviltview";

  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
  
}
