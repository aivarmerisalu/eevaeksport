package eevaeksport.imp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

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

public class SAF5NikoImportDriver extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public SAF5NikoImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "saf");
    } catch (Exception e){
       parent.showError(e);
       
    }
    initConversions();
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1  = "SELECT yksus AS kood FROM yksus where nimetus like  '%\"" + id + "\"%'";	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{

  	String sql1 = "SELECT artikkel FROM artikkel where ribakood='" + ean + "' order by artikkel group by nimetus";
    if(prodID == null || prodID.trim().length()==0){
    	prodID = "0";
    }
  	String sql2 = "SELECT artikkel FROM artikkel where artikkel=" + prodID + " order by artikkel group by nimetus";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("artikkel").trim();
    }
//System.out.println("prod0" + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("artikkel").trim();
      }
    }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();

    return prod;
  }



  public void makeOrder(Order order){
 
	 //Vector splitted =  splitOrder(order);
	 // Enumeration en = splitted.elements();
	  //while(en.hasMoreElements()){
		//  Order ord = (Order)en.nextElement();
    
	  setOrder(order);
    String invoiceID = getNextID();
    addInvoiceHeader(invoiceID);
    addInvoiceLines(invoiceID);
    
	  //}
  }
  
  private void initConversions(){
	  try{
		  File f = new File("custsafaxa.txt");
		    if (f.exists()){
		      InputStreamReader is = new FileReader(f);
		      BufferedReader br = new BufferedReader(is);
		      String line = null;
		      while( ( line = br.readLine()) != null){
		      	
		        StringTokenizer st = new StringTokenizer(line, ";");
		        if (st.hasMoreTokens()){
		        	try{
		        	this.custsafaxa.put(st.nextToken(), st.nextToken());
		        	 }catch (Exception e){
					    	
					    }
		        }	    	
		        }
	  } 
		    f = new File("prodsafaxa.txt");
		    if (f.exists()){
			      InputStreamReader is = new FileReader(f);
			      BufferedReader br = new BufferedReader(is);
			      String line = null;
			      while( ( line = br.readLine()) != null){
			      	
			        StringTokenizer st = new StringTokenizer(line, ";");
			        if (st.hasMoreTokens()){
			    try{
			    	this.prodsafaxa.put(st.nextToken(), st.nextToken());
			    }catch (Exception e){
			    	
			    }
			        }	    	
			        }
		  }
		    f = new File("agentsafaxa.txt");
		    if (f.exists()){
			      InputStreamReader is = new FileReader(f);
			      BufferedReader br = new BufferedReader(is);
			      String line = null;
			      while( ( line = br.readLine()) != null){
			      	
			        StringTokenizer st = new StringTokenizer(line, ";");
			        if (st.hasMoreTokens()){
			        	try{
			        	this.agentssafaxa.put(st.nextToken(), st.nextToken());
			        	 }catch (Exception e){
						    	
						    }
			        	
			        }	    	
			        }
			      System.out.println(agentssafaxa.toString());
		  }
		    
		    
	  }catch (Exception e){
		  e.printStackTrace();
	  }
	   
  }

  private String getNextID(){

    String s = null;
    String result = null;
    try {
      Statement st = con.createStatement();
      String sql = "SELECT idviit FROM ladu/tagladu WHERE idtag ='VLADU'";
      ResultSet rs = st.executeQuery(sql);
      if (rs.next()){
        s = rs.getString("idviit");
      }

    byte[] bytes = s.getBytes();
    int systeem = 128;
    int decimal = bytes[0]*(int)Math.pow(systeem, 2) + bytes[1]*(int)Math.pow(systeem, 1) +
      bytes[2];
    decimal++;
    int reminder = decimal/systeem;
    int a = decimal - reminder*systeem;
    int reminder2 = reminder/systeem;
    int b = reminder-reminder2*systeem;
    int reminder3 = reminder2/systeem;
    int c = reminder2 - reminder3*systeem;

    String aa = new String(new byte[] {(byte)a});
    String bb = new String(new byte[] {(byte)b});
    String cc = new String(new byte[] {(byte)c});

    result = new String(cc + bb + aa);

    sql = "UPDATE ladu/tagladu SET idviit=? WHERE idtag='VLADU'";
    PreparedStatement pst = con.prepareStatement(sql);
    pst.setString(1, result);
    pst.executeUpdate();
    pst.close();
    }
    catch (SQLException e){
      e.printStackTrace();
    }

    return result;
  }

  private void addInvoiceLines(String invID){
    try{
     StringBuffer buf = new StringBuffer();
     String timefull = System.currentTimeMillis() + "";
     
   
     String cust = (String)this.custsafaxa.get(order.getCompany());
     if (order.getDokNr()!= null && order.getDokNr().trim().length() >0){
    	 timefull = timefull.substring(10,13) +  order.getDokNr();
    	 
     }
     String filename = order.getAgent()+ "." + timefull;
   
     String agg = (String)this.agentssafaxa.get(order.getAgent());
     if(agg == null){
    	 agg = order.getAgent() ;
     }
     Calendar cal = Calendar.getInstance();
     if(order.getShipCal() != null){
    	 cal.setTime(order.getShipCal().getTime());	 
     }
    
     String shipdate = getDateNiceString(cal);

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      String sql = "SELECT ?*summa AS laosumma ,kogus,yksus FROM ladu/laosaldo WHERE  artikkel=? ";

      String prod = (String)this.prodsafaxa.get(itemid);
      PreparedStatement st = con.prepareStatement(sql);
      st.setDouble(1, kogus);
      st.setInt(2, Integer.parseInt(itemid));
      ResultSet rs = st.executeQuery();
      double laosumma = 0;
      if (rs.next()){
        laosumma = rs.getDouble("laosumma");
        double rskogus = rs.getDouble("kogus");
        if (rskogus != 0){
          laosumma = laosumma/rskogus;
        } else {
          laosumma = 0;
        }
       
      }
      rs.close();
      st.close();


      sql = "SELECT hk_vaikimi FROM yksus WHERE  hk_vaikimi <>'' AND yksus =?";
      PreparedStatement pst = con.prepareStatement(sql);
       pst.setInt(1, Integer.parseInt(order.getCompany()));
      ResultSet rst = pst.executeQuery();

      while (rst.next()){
      }
     rst.close();
      pst.close();

      //sql = "SELECT  l.hk_" + hk + " AS price,a.grupp FROM ladu/laduart l INNER JOIN artikkel a ON l.artikkel=a.artikkel WHERE l.artikkel=?";
      sql = "SELECT  l.realhind AS price,maksulipp FROM ladu/laduart l INNER JOIN artikkel a ON l.artikkel=a.artikkel WHERE l.artikkel=?";

      st = con.prepareStatement(sql);
      st.setInt(1, Integer.parseInt(itemid));
      rs = st.executeQuery();
      String maksulipp = "K";
      double price = 0;
      if (rs.next()){
        price = rs.getDouble("price");
        maksulipp = rs.getString("maksulipp");
      }
      rs.close();
      st.close();

      sql = "SELECT (siffer) AS percent FROM yksus WHERE yksus=?";
       //sql = "SELECT (hkg_" + grupp + " + hk_prots) AS percent FROM yksus WHERE yksus=?";
      //System.out.println(sql + "\n" + "hkg_" + grupp + "\n" + order.getCompany());
      st = con.prepareStatement(sql);
      //st.setString(1, "hkg_" + grupp);
      st.setInt(1, Integer.parseInt(order.getCompany()));
      rs = st.executeQuery();
      double percent = 0;
      percent = orderLine.getDiscount();
      if (percent <=0){
    	  percent = 0;
      }
      if (rs.next()){
        //percent = rs.getDouble("percent");
      }
      rs.close();
      st.close();

      sql = "INSERT INTO ladu/lstl VALUES(" +
          "?," +
          "?," +
          "?," +
          "?," +
          "?," +
          "'" + maksulipp + "'," +
          "0," +
          "0," +
          "''," +
          "0," +
          "?," +
          "date()," +
          "'P'," +
          "''," +
          "0," +
          "0," +
          "''," +
          "0," +
          "''," +
          "0," +
          "''" +
          ")";

          log.info("SALES_LINE_SQL: " + sql);

          st = con.prepareStatement(sql);
          st.setString(1, invID);
          st.setInt(2, Integer.parseInt(itemid));
          st.setDouble(3, kogus);
          st.setDouble(4, laosumma);

          double pr = getPrice(kogus, percent, price);
          order.setSum(order.getSum() + pr);
          
          st.setDouble(5, pr);
          int yksus;
          if(Integer.parseInt(itemid)>300000 && Integer.parseInt(itemid)<350000){
          	yksus = 0;
          }else{
          	yksus = 0;
          }
          st.setInt(6, yksus);
          st.executeUpdate();
          st.close();
         if(cust==null){
        	 cust = order.getCompany()+ "xxx";
         }
         if(prod==null){
        	 prod = orderLine.getToode() + "xxx";
         }
        buf.append(cust + ";" + prod + ";" + kogus+ ";0; ;0;0;" + timefull+ ";0;" + pr + ";0;" + agg + ";0;" + orderLine.getUnit() + ";"
        		   + shipdate + "; ; ; ;" + timefull +  System.getProperty("line.separator"));
          setNumAddedLines(getNumAddedLines() + 1);
    }

    
    
    if(buf.length() > 2){
        File f = new File(getDir() + filename +".txt");
        if (f.exists()){
        	filename = filename + "x";
        }
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() + filename + ".txt", true));
    		os.write(buf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    
    } catch (Exception e){
       parent.showError(e);
    }
  }

  private void addInvoiceHeader(String invID){
    

  	try {
     int yksus ;
     String itemid = ((OrderLine)order.getOrderLines().get(0)).getToode();
     if(Integer.parseInt(itemid)>300000 && Integer.parseInt(itemid)<350000){
     	yksus = 600;
     }else{
     	yksus = 500;
     }
    String sql = "SELECT makseting FROM yksus WHERE yksus=?";

    log.info("addInvoiceHeader: " + sql);

    sql = "SELECT hk_vaikimi, makseting FROM yksus WHERE  hk_vaikimi <>'' AND yksus =?";
    PreparedStatement pst = con.prepareStatement(sql);
     pst.setInt(1, Integer.parseInt(order.getCompany()));
    ResultSet rst = pst.executeQuery();
    String hk = "apteek";
    while (rst.next()){
      hk = rst.getString("hk_vaikimi").trim();
    }
   rst.close();
    pst.close();
      PreparedStatement st = con.prepareStatement(sql);
      st.setInt(1, Integer.parseInt(order.getCompany()));
      ResultSet rs = st.executeQuery();
      while(rs.next()){

      sql = "INSERT INTO ladu/ladu VALUES (" +
        "'V'," +
        "date()," +
        "date()," +
        "date()," +
        "?," +
        "?," +
        "?," +
        "0," +
        "?," +
        "''," +
        "'MobileJava'," +
        "?," +
        "0," +
        "''," +
        "'P'," +
        "date(1899,12,30)," +
        "0," +
        "'','" +
        hk + "'," +
        "?," +
        "0,"  +
        "''," +
        "''," +
        "0," +
        "0," +
        "''," +
        "''," +
        "0," +
        "''," +
        "''," +
        "0," +
        "''," +
        "date(1899,12,30)," +
        "0," +
        "'')";
        log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con.prepareStatement(sql);
        String idd= "NVALJA";
        if(parent.getProps().getProperty("idd") !=null){
        	idd = parent.getProps().getProperty("idd");
        }
        stmt2.setInt(1, Integer.parseInt(order.getCompany()));
        stmt2.setInt(2, yksus);
        stmt2.setString(3, order.getAgent() );
        String dokNr = null;//order.getDokNr();
        if (dokNr == null){
          dokNr = String.valueOf(updateNumberSequence(idd));
        }
        stmt2.setInt(4, Integer.parseInt(dokNr));
        stmt2.setInt(5, rs.getInt("makseting"));
        stmt2.setString(6, invID);
        stmt2.executeUpdate();
        stmt2.close();
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
      }
      st.close();
    } catch(SQLException e){
      parent.showError(e);
    }

  }

  private int updateNumberSequence(String idtag){
    int id = -1;
    try {

      String sql = "SELECT tagladu.idnum AS nextrec FROM ladu/tagladu WHERE tagladu.idtag=?";
      log.info("updateNumberSequence()2: " + sql);
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, idtag);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        
        id = rs.getInt("nextrec") + 1;
      }
      st.close();

      sql = "UPDATE ladu/tagladu SET idnum=? WHERE tagladu.idtag=?";
      log.info("updateNumberSequence(): " + sql);
      st = con.prepareStatement(sql);
      st.setInt(1, id);
      st.setString(2, idtag);
      int i = st.executeUpdate();
      log.info("updateNumberSequence(): " + i + " rows updated");

      st.close();
    } catch(SQLException e){
      parent.showError(e);
    }
    return id;
  }



  public static double getPrice(double kogus, double percent, double price){
    //System.out.println("kogus: " + kogus + " percent: " + percent + "price: " + price);
    double value = kogus * price*((100+percent)/100);
    value = (Math.round((value*100d)))/100d;
    return value;
  }

  public String getDriverVersion(){
    return "SAF6Niko";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private String getDir(){
	    String dir = parent.getProps().getProperty("writeout");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }

  
  private String getDateNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	     if (day.length() == 1){
	      sb.append("0");
	    }
	     sb.append(day);
	     sb.append(".");
	    if (month.length() == 1){
	     sb.append("0");
	   }
	     sb.append(month );
	    sb.append( "." + year);
	    return sb.toString();
	  }
	
  
  
  private void addLog(String line){
    parent.addLog(line);
  }

  private String url = "jdbc:odbc:";
  private Connection con;
  private HashMap custsafaxa =  new HashMap();
  private HashMap prodsafaxa =  new HashMap();
  private HashMap agentssafaxa =  new HashMap();
  private ImportPanel parent;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
 
}