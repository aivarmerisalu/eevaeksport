package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class AxaBuraImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public AxaBuraImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      //con.setAutoCommit(false);
      //con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
      con2 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con3 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con4 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
              , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }


  public String getImportFile(){
   return parent.getProps().getProperty("imp.read");
 }

  public String identifyCustomer(String id,String regnr) throws SQLException {

    String acc = null;
    if(regnr != null && regnr.trim().length()>0){

    String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='pre' and  (REGNUM_W)= '" + regnr + "' AND ouraccountnum = '" + id + "'";
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

        String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='pre' and   accountnum = '" + id + "'";
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

        String sql2 = " SELECT accountnum  AS kood FROM custtable where dataareaid='pre' and   namealias = '" + id + "' ";
        System.out.println(sql2);



        Statement st2 = con3.createStatement();
        ResultSet rs2 = st2.executeQuery(sql2);

        while (rs2.next()) {
          acc = rs2.getString("kood").trim();
        }
        
        rs2.close();
        st2.close();

        }
    
  	if (acc == null){

  	String sql1 = " SELECT accountnum  AS kood FROM custtable where dataareaid='pre' and  (REGNUM_W)= '" + id + "' AND ouraccountnum = '" + id + "'";
    System.out.println(sql1);



    Statement st1 = con3.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();

  	}
   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT inv.itemid AS toode ,eancode FROM inventtable inv left JOIN inventtablemodule mod ON mod.itemid = inv.itemid where blocked = 0 and moduletype = 2 and  mod.dataareaid = 'pre' and inv.dataareaid='pre' and inv.telema='1'" +
    		" AND inv.eancode='" + ean + "' order by inv.itemid " ;


    String sql2 = "SELECT inv.itemid AS toode FROM inventtable inv left JOIN inventtablemodule mod ON mod.itemid = inv.itemid where blocked = 0  and moduletype = 2 and  inv.dataareaid = 'pre' and mod.dataareaid = 'pre' and inv.itemid='" + prodID + "' ";

    String prod = null;
    ResultSet rs1 = null;
    Statement  st1 = con.createStatement();
  if(prodID != null){
	   
	     rs1 = st1.executeQuery(sql2);
		System.out.println(sql2);
	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	  	System.out.println("prod1 " + prod );
	    } 
  }
 

  

    if(prod == null){
      rs1 = st1.executeQuery(sql1);
      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      	System.out.println("prod2 " + prod );
      }
    }
    rs1.close();
    st1.close();
   System.out.println("prod" + prod +" "+ ean+prodID);
   if(prod == null){
   	prod = prodID;
   }
    return prod;
  }



  public void makeOrder(Order order) throws SQLException{

    setOrder(order);
    String salesid =  (Calendar.getInstance().getTimeInMillis()+"").substring(3) ;
    addInvoiceHeader(salesid);
    addInvoiceLines(salesid);
  //  String sql4 = "update talsorderheader set ready=1 where ltrim(orderid) = ?";
  //  PreparedStatement st2 = con6.prepareStatement(sql4);
  //  st2.setString(1, salesid.trim());
  //  st2.executeUpdate();

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

  
  
  public String identifyProductSecond( String custID,String ean) throws SQLException{
	    String sql1 = "select sal.salesstatus,lin.itemid  as toode  from salestable sal inner join salesline lin  on lin.salesid=sal.salesid where  " + 
     " ExternalItemId  != '' and  sal.dataareaid='pre' and lin.dataareaid='pre' and sal.salesstatus=1 " +
	" and sal.custaccount='" + custID + "' and lin.ExternalItemId = '" + ean + "' " ;



	    String prod = null;
	    ResultSet rs1 = null;
	    Statement  st1 = con.createStatement();
	  if(custID != null){
		   
		     rs1 = st1.executeQuery(sql1);
			System.out.println(sql1);
		    while (rs1.next()) {
		      prod = rs1.getString("toode").trim();
		  	   System.out.println("Teine tuvastamine prod1 " + prod );
		    } 
	  }
	 

	  
	    rs1.close();
	    st1.close();
	   System.out.println("Teine tuvastamine prod " + prod + " " + ean + " " + custID);
	   
	   
	    return prod;
	  }

  
  

  private void addInvoiceLines(String salesid) throws SQLException{

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();

      

      String sql0 = "SELECT inv.itemid,(inv.itemname) AS nimetus,unitid AS unit FROM inventtable inv " +
               " INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid WHERE  mod.dataareaid='pre'" +
               " AND inv.dataareaid='pre' AND moduletype=2  " +
               "  AND len(inv.itemname)>0 AND inv.itemid ='" + itemid + "' ";
      
    log.info("SALES_LINE_SQL: " + sql0);
    System.out.println(sql0);
    Statement st = con5.createStatement();
    ResultSet rs = st.executeQuery(sql0);
   boolean olemas = false;
    String unit = " ";
    String name = "";
    while(rs.next()){
       name = rs.getString("nimetus"); 
    	unit = rs.getString("unit");

      if (unit !=null && unit.trim().length()>0){
      	unit = unit.trim();
      	olemas = true;
      }else {
      	unit = " ";
      }
    }
  if(orderLine.getUnit().trim().length()>0){
    unit = orderLine.getUnit();
  }
  System.out.println("olemas " + olemas);
  if(!olemas){
  	itemid = orderLine.getTooteNimi();
  	name = orderLine.getEAN() + " " + orderLine.getTooteNimi();
  	if(itemid.length()>20){
  		itemid = itemid.substring(0,19);
  	}
  }
          String agent = order.getAgent();
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
          if(ag == null){
         	ag = new Agent(agent);
         }
          
          int time = 0;
          Calendar cal = Calendar.getInstance() ;        
          time = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND);
     
        /*  if(ean != null && ean.trim().length()>0){
        	  try{
        		  //siin toimub lisa kontroll kas tootel on sortimenduis kood olemas 
        		  String lisatoode = identifyProductSecond(order.getCompany(),ean);
                  if(lisatoode != null){
                	  itemid = lisatoode;
                	  
                	  System.out.println("lisatoode l�heb k�iku " + lisatoode);
                  }  
        	  }catch(Exception e){
        		 e.printStackTrace(); 
        	  }
             
          }*/


          String sql =" INSERT into  perinttelemaline (ORDERID, ITEMID, QTY, UNITID ,DATAAREAID, RECID,CREATEDDATE, CREATEDTIME,  ITEMNAME )  " +
             " values('"+salesid+"','"+itemid+"',"+kogus+",'" + unit + " ','pre'," +getAndUpdateRecID() + ",getdate()," + time + ",'" + name + "')";

              log.info("SALES_LINE_SQL: " + sql);
              System.out.println(sql);
              PreparedStatement st2 = con2.prepareStatement(sql);
              st2.executeUpdate();
              addNumLine();
              st2.close();

              }
  }

  private void addInvoiceHeader(String salesid) throws SQLException{

    long recID = getAndUpdateRecID();

         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }

     String tekst = " ";

      if (order.getEmailBody()!= null){
         tekst = order.getEmailBody().replaceAll("\n"," ");
       }
      

      if (order.getShipTime()!= null){
         tekst =  order.getShipTime().replaceAll("\n"," ") + "  P�ev :" + getYYYYpMMpDDpTimeNiceString(order.getShipCal());
       }
      
      
      String ref = " ";
      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
      	ref = order.getInfo().trim();
      }

      if(order.getReference() != null && order.getReference().trim().length()>0 ){
      	ref = order.getReference().trim();
      }
      
     

      int time = 0;
      Calendar cal = Calendar.getInstance() ;
      
      time = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND);
      
      String custname = "";
      if( order.getCompName() != null){
    	  custname = order.getCompName();
      }
      
      if (order.getShipTime()!= null){
         custname  =  order.getShipTime().replaceAll("\n"," ") + " " + getYYYYpMMpDDpTimeNiceString(order.getShipCal()) + " " + custname;
        }
      System.out.println(salesid + "|"+ order.getCompany() +"|"+ ag.getID()+ "|"+ tekst+"|"+recID);
     String sql = " INSERT INTO perinttelematable (  " +
            " ORDERID,CUSTACCOUNT,SALESPERSON,ORDERDATE,NOTES, DATAAREAID,recid,REFERENCENO,CREATEDDATE,CREATEDTIME,CURRENCYCODE,DELIVERYDATE ,CUSTNAME) " +
            " VALUES ('"+salesid+"','" + order.getCompany() +"','"+ag.getID()+"',?,'"+tekst+
            "','pre',"+recID+",'" + ref + "',getdate()," + time + ",'EUR', ?, '" +custname + "')" ;
        log.info("addInvoiceHeader2: " + sql);
        System.out.println(sql);
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        java.util.Date dat = new java.util.Date();
       // System.out.println(dat.toString() + "  sc " + order.getShipCal().getTime().toString() + " ref " + ref);
            dat.setTime(order.getShipCal().getTimeInMillis());
        Calendar cc = Calendar.getInstance();
        stmt2.setTimestamp(1, new java.sql.Timestamp(cc.getTimeInMillis()));
        stmt2.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
        stmt2.executeUpdate();
        stmt2.close();
    
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
  }



  private long getAndUpdateRecID() throws SQLException{
    long nextRecID = 0;
    String sql = "SELECT NEXTVAL FROM SYSTEMSEQUENCES WHERE  NAME='SEQNO' and tabid=65531";
    log.info("getAndUpdateRecID(): " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      while(rs.next()){
        counter++;
        log.info("COUNTER:" + counter);
        //System.out.println("number:" + rs.getString("NEXTREC"));
        nextRecID = rs.getLong("NEXTVAL");
        log.info("NEXTRECID = " + nextRecID);
        sql = "UPDATE SYSTEMSEQUENCES SET NEXTVAL=" + (nextRecID+1) +
        " WHERE  NAME='SEQNO' and tabid=65531";
        log.info("getAndUpdateRecID(): " + sql);
        Statement st2 = con4.createStatement();
        int i = st2.executeUpdate(sql);
        st2.close();
        log.info("UPDATED ROWS = " + i);
      }
      rs.close();
      st.close();

    return nextRecID;
  }


  public String getDriverVersion(){
    return "AxaPere";
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

  /**
  *
  * @param aUseEAN - the new value for useEAN
  */
  public void setAmount(boolean aAmount){
           isAmount= aAmount;
  }

  private String url = "jdbc:odbc:";
  private Connection con;
  private Connection con2;
  private Connection con3;
  private Connection con4;
  private Connection con5;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;
  private int counter = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
