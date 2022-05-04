package eevaeksport.imp.driver;

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
import java.util.Locale;

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

public class NaviVilandraImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public NaviVilandraImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    nf.setGroupingUsed(false);
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "navi"
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));

    } catch (Exception e){
       parent.showError(e);
    }
  }


  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
 }

  public String identifyCustomer(String id,String regnr) throws SQLException {
	  Connection con3 = DriverManager.getConnection(url + "navi"
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
    String acc = null;
    if(id != null && id.trim().length()>0){

    String sql2 = "SELECT No_  AS kood FROM [UAB Vilandra$Customer] where 'MA' + subdivision ='" + id + "'";
    System.out.println(sql2);
    


    Statement st2 = con3.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      acc = rs2.getString("kood").trim();

    }
    rs2.close();
    st2.close();

    }


    if(regnr == null && acc == null){

        String sql2 = " SELECT No_  AS kood FROM [UAB Vilandra$Customer] where No_ = '" + id + "' ";
        System.out.println(sql2);

        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();

        }
        rs2.close();
        st2.close();

        }

    if(regnr == null && acc == null){

        String sql2 = "SELECT No_  AS kood FROM [UAB Vilandra$Customer] where subdivision ='" + id + "'";
        System.out.println(sql2);

        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();

        }
        rs2.close();
        st2.close();

        }
    

   // System.out.println("Klient: " + acc);
    return acc;

  }

  
  
  public String getCustomerAgent(String custid) throws SQLException{

    String sql2 = "SELECT [Salesperson Code] AS agent FROM [UAB Vilandra$Customer]   WHERE  No_='" + custid + "'";
  System.out.println("Cust Agent " + sql2);
    String ag = null;
    Connection con3 = DriverManager.getConnection(url + "navi"
            , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
    Statement  st1 = con3.createStatement();

	   
    ResultSet    rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    if (rs1.next()) {
	      ag = rs1.getString("agent").trim();
	  	
	    } 
      rs1.close();
      st1.close();
      con3.close();
  
    return ag;
  }


  

  
  public String identifyProduct(String ean, String prodID) throws SQLException{
	  
	  
	  if(ean != null && ean.startsWith("0")){
		  while(ean.startsWith("0")){
			  ean= ean.substring(1);
		  }
	  }

    String sql2 = "SELECT No_ AS toode FROM [UAB Vilandra$Item]   WHERE  No_='" + prodID + "'";
System.out.println(sql2);
    String prod = null;
    ResultSet rs1 = null;
    Statement  st1 = con.createStatement();
    if(prodID != null && ean != null && ean.equals(prodID)){
    	prodID = null;
    }
  if(prodID != null ){
	   
	    rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  	  System.out.println("prod1 " + prod );
	    } 
  }else{
	  
	  
	  sql2 = "SELECT No_ AS toode FROM [UAB Vilandra$Item]    WHERE   CrossReferenceNo = '" + ean + "'";
	  rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  	  System.out.println("prod1 " + prod );
	    } 
  }
 

  

   System.out.println("prod" + prod +" "+ ean+prodID);
   if(prod == null){
   	prod = prodID;
   }
   if(prod == null || prod.trim().length() == 0){
	   prod = "XXX";
   }
    return prod;
  }



  public void makeOrder(Order order) throws SQLException{

    setOrder(order);
 
    	  String salesid = getAndUpdateRecID() + "";
    	    addInvoiceHeader(salesid);
    	    addInvoiceLines(salesid);	
  
  /*  String sql4 = "update  Mobilejava.SalesHeader set ReadyForImport=1 where SalesNo = " + salesid;
   
    PreparedStatement st2 = con5.prepareStatement(sql4);
    st2.executeUpdate();
    st2.close();*/
  //  makeOutput(order);
  }
  public static String getYYYYpMMpDDpTimeNiceString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("." + month  + ".");
		sb.append(day +" ");
	    
	    return sb.toString();
	  }

  
  
 

  private void addInvoiceLines(String salesid) throws SQLException{
      Connection con2 = DriverManager.getConnection(url + "navi"
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
       double kogus = orderLine.getKogus();
       double discount = 0;
       if(orderLine.getDiscount() > 0 && (order.getStatus() < 3 || order.getStatus() == 96)){
    	   discount = orderLine.getDiscount();
       }
          String sql =" INSERT into [UAB Vilandra$EDI Order Line] ([Entry No_], [Line No_], [Order No_] ,[Source Line No_],[Item No_],[Quantity] ,[Discount %], " +
 " [Unit Price With Disc_(no Act_)],[EAN],[Customer Item No_],[Item Description],[Unit of Measure],[Customer Unit of Measure],[Unit Price], " +
 " [EDI Pack Size],[Blocked],[Comment],[Modification User],[Modification Date],[Modification Time],[Action Discount %],[Additional Fee]  " +
 " ,[Action Discount Amount], [Shipped Quantity],[Amount],[VAT Amount],[VAT %],[Amount Including VAT],[Dimension (PADALINYS)],[Dimension (KODUKAI)],[Dimension (TINKLAS)],[Dimension (ZENKLAS)]"
 + ",[Dimension (OBJEKTAS)])   " +
 "  values("+salesid+","+ counter + ",'" +  order.getDokNr() + "',"+ counter + ",'"+itemid+"'," +
             		+kogus+"," + discount + ",0.0,'','','','','',0.0, 0,'','',''," +
             				"cast ('1753-01-01 00:00:00.000' as datetime),cast('1754-01-01 00:00:00.000' as datetime),0,'',0,0,0,0,0,0,'','','','','')";
  
              log.info("SALES_LINE_SQL: " + sql);
              System.out.println(sql);
              Statement st2 = con2.createStatement();
              String aiai = parent.getProps().getProperty("testing");
              if(aiai != null && aiai.equals("true")){
            	  System.out.println("lines " + itemid + " kog " + kogus);
              }else{
            	  st2.executeUpdate(sql);
              	
              }
              
             
              addNumLine();
              st2.close();

              }
    con2.close();
  }

  private void addInvoiceHeader(String salesid) throws SQLException{

         String agent = order.getAgent();
       
String perfix = "MS";
     String tekst = " ";

      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
      	tekst = order.getInfo().trim();
      }

      if(order.getStatus() == 82) {
    	  tekst = " ";
          perfix = "MO";
      }
      
       String dokNr = order.getDokNr();
       if(dokNr == null || dokNr.trim().length() == 0){
    	  Calendar cal = Calendar.getInstance();
    	   dokNr = (cal.getTimeInMillis() + "").substring(5);
       }
      
      String type = "1";
      if(order.getStatus() == 2){
    	 type = "2"; 
      }
      
      if(agent.equals("aaa") || agent.equals("SUP3") || agent.equals("SUP2") || agent.equals("SUP1")){
    	agent =   getCustomerAgent(order.getCompany());
    	if(agent == null){
    		  agent = parent.getProps().getProperty("defaultagent");
    	}
    	
      }
   
      String time = "00:00:00.000";
      
      Date dd = new Date ();
      String hour = dd.getHours() + "";
      if(hour.length() == 1){
    	  hour = "0"+ hour;
      }
      String minute = dd.getMinutes() + "";
      if(minute.length() == 1){
    	  minute = "0"+ minute;
      }
      time = hour + ":" + minute + ":00.000";
      
      System.out.println(order.getCompany() +"|"+ agent + "|"+ tekst+"|" + order.getStatus());
      String comp =  order.getCompany();
      if(tekst != null && tekst.length() > 50){
    	  tekst = tekst.substring(0,50);
      }
   
     String sql = " INSERT INTO [UAB Vilandra$EDI Order] ( [Entry No_],[Type],[Status],[Order No_],[Document Type],[Order Date],[Customer No_], " +
    " [Discount %],[Salesperson],[User],[Record Date],[Record Time] ,[Delivery Comment] ,[Delivery Date],[Buyer ILN],[Buyer Name],[Customer ILN],[Customer Name], " +
    " [Delivery Point Name],[Delivery Point by Buyer],[Delivery Point ILN],[Location],[Currency],[Comment],[Internal Document No_],[Blocked],[Duration (ms)] " +
    " ,[Status User],[Status Date],[Status Time],[Order Status],[Payment Status],[TVS Customer No_],[Customer Address], "+
    " [Customer City],[Customer Post Code],[Customer Phone No_],[Customer E-mail],[Delivery Address],[Delivery City],[Delivery Post Code],"
    + " [Posting No_],[Receiving Advice Number],[Amount],[Due Date],[Vendor No_], [Vendor Name],[Vendor Registration No_],[Vendor Account No_],[Customer Registration No_]) " + 
    " VALUES (" + salesid + ",2,0,'" + perfix + dokNr + "'," + type + ", cast(cast(getdate() as date) as datetime),'" + comp +"',0,'" + agent + "','MERISOFT'," +
    		"  cast(cast(getdate() as date) as datetime),cast('1754-01-01 " + time + "' as datetime)" + // cast(substring('1754-01-01 ' + cast(cast(getdate() as time) as char),0,24) as datetime)
    ",'" + tekst +"', cast(cast(? as date) as datetime),'','','','','','','','','','" + tekst +"','','','',''," +
    		" cast ('1753-01-01 00:00:00.000' as datetime),cast('1754-01-01 00:00:00.000' as datetime),'','','','','','','','','','','','',''"
    		+ ",0,cast('1754-01-01 00:00:00.000' as datetime),'','','','','')" ; 
         
          log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        Connection con2 = DriverManager.getConnection(url + "navi"
                , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
            
         if(order.getStatus() == 82){
        	 dat.setTime(order.getShipCal().getTimeInMillis());
         }
       // stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
      //  stmt2.setString(2, ref);
            String aiai = parent.getProps().getProperty("testing");
            if(aiai != null && aiai.equals("true")){
             	System.out.println("sc " + comp + " ref " + tekst + " status " + order.getStatus() + " date " + dat.toLocaleString() );	
            }else{
            	stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
            	stmt2.executeUpdate();
           
            }
            
        
        stmt2.close();
        con2.close();
    
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
  }



  private long getAndUpdateRecID() throws SQLException{
    long nextRecID = 0;
    String sql = "SELECT max ([Entry No_]) as ID from [UAB Vilandra$EDI Order] where [Entry No_] < 190000000";
    log.info("getAndUpdateRecID(): " + sql);
    Connection con2 = DriverManager.getConnection(url + "navi"
            , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      Statement st = con2.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        nextRecID = rs.getLong("ID") + 1;
      }
      rs.close();
      st.close();
      con2.close();
     System.out.println("returning recid " + nextRecID);
    return nextRecID;
  }


  public String getDriverVersion(){
    return "NaviVilandraImport";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private void addLog(String line){
    parent.addLog(line);
  }
  public boolean isAmount(){
          return isAmount;
  }

 


  public NumberFormat getNF() {
		return nf;
	}
	/**
	 * @param nf the nf to set
	 */
	public void setNF(NumberFormat nf) {
		this.nf = nf;
	}
  
  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }

  private String url = "jdbc:odbc:";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private Connection con;

  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
