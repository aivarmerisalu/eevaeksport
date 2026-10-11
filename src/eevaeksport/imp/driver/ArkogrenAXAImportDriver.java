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

public class ArkogrenAXAImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceAxaImportDriver.class.getName());

  public ArkogrenAXAImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
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
      
      con5 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
  con6 = DriverManager.getConnection(url + parent.getProps().getProperty("dsn")
            , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
      //stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {

    String acc = null;
    if(regnr != null && regnr.trim().length()>0){

    String sql2 = "SELECT accountnum  AS kood FROM custtable where  (regnum_w)= '" + regnr + "' AND arkspartnercode = '" + id + "' and dataareaid= 'ark'";
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

  	String sql1 = "SELECT accountnum  AS kood FROM custtable where  (regnum_w)= '" + id + "' OR arkspartnercode = '" + id + "'  and dataareaid= 'ark'";
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
    String sql1 = "SELECT inv.itemid AS toode FROM inventtable inv inner join inventtablemodule b " +
    		" on inv.itemid = b.itemid where moduletype=2  AND blocked = 0 AND arkitembarcode='" + ean + "'  and inv.dataareaid= 'ark'  and b.dataareaid= 'ark'";


    String sql2 = "SELECT itemid AS toode FROM inventtable where  itemid='" + prodID + "'  and dataareaid= 'ark'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql2);

  	System.out.println(sql2);
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
  	System.out.println("prod1 " + prod );
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

  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }


  public void makeOrder(Order order) throws SQLException{

    setOrder(order);
    String salesid =  (Calendar.getInstance().getTimeInMillis()+"") ;
    addInvoiceHeader(salesid);
    addInvoiceLines(salesid);
    String sql4 = "update MJORDER set ready=1 where ltrim(orderid) = ?";
    PreparedStatement st2 = con6.prepareStatement(sql4);
    st2.setString(1, salesid.trim());
    st2.executeUpdate();

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


      String sql0 = "SELECT inv.itemid,(inv.itemname) AS nimetus, unitid AS unit FROM inventtable inv " +
                " INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid WHERE  mod.dataareaid='ark' AND inv.dataareaid='ark' AND moduletype=2 " +
                " AND len(inv.itemname)>0 AND inv.itemid = '" + itemid + "' ";
    log.info("SALES_LINE_SQL: " + sql0);

    Statement st = con5.createStatement();
    ResultSet rs = st.executeQuery(sql0);
   boolean olemas =false;
    String unit = " ";

    while(rs.next()){
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
  	if(itemid.length()>20){
  		itemid = itemid.substring(0,19);
  	}
  }
  
  double discount = 0;
  if(orderLine.getDiscount()>0){
	  discount = orderLine.getDiscount();
  }
          String agent = order.getAgent();
          Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
          if(ag == null){
         	ag = new Agent(agent);
         }


          String sql ="INSERT into  MJORDERLINE  (ORDERID, ITEMID, QTY, UNITID ,DATAAREAID, DISCOUNT ) " +
            " values('"+salesid+"','"+itemid+"',"+kogus+",'" + unit + " ','ark'," + discount + ")";

              log.info("SALES_LINE_SQL: " + sql);
              PreparedStatement st2 = con2.prepareStatement(sql);
              st2.executeUpdate();
              addNumLine();
              st2.close();

    }
  }

  private void addInvoiceHeader(String salesid) throws SQLException{


         String agent = order.getAgent();
         if(agent.equals("KART")){
        	 agent = "K�RT";
         }
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);

         if(ag == null){
         	ag = new Agent(agent);
         }
         
         

     String tekst = " ";

      if (order.getEmailBody()!= null){
         tekst = order.getEmailBody().replaceAll("\n"," ");
       }
      String ref = " ";
      if(order.getInfo() != null && order.getInfo().trim().length()>0 ){
      	ref = order.getInfo().trim();
      }

      if(order.getReference() != null && order.getReference().trim().length()>0 ){
      	ref = order.getReference().trim();
      }

      System.out.println(salesid + order.getCompany() +ag.getID()+tekst);
     String sql = "INSERT INTO mjorder ( " +
            " ORDERID,ORDERACCOUNT,SALESTAKER,ORDERDATE,NOTES,SALESORIGINID,ready,reference,deliverydate,DATAAREAID) " +
            " VALUES ('"+salesid+"','" + order.getCompany() +"','"+ag.getID()+"',getdate(),'"+tekst+"','MJPDA','0','" + ref + "',?,'ark') " ;
 //    " ORDERID,ORDERACCOUNT,SALESTAKER,ORDERDATE,NOTES,SALESORIGINID,DATAAREAID,ready,recid) " +
   //  " VALUES ('"+salesid+"','" + order.getCompany() +"','"+ag.getID()+"',getdate(),'"+tekst+"','MJPDA','ark','0',"+recID+") " ;

        log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con2.prepareStatement(sql);



        java.util.Date dat = new java.util.Date();

        dat.setTime(dat.getTime()+86400000);

        if(dat.before(order.getShipCal().getTime())){
            dat.setTime(order.getShipCal().getTimeInMillis());
        }
        stmt2.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
      try{
      	stmt2.executeUpdate();

      }catch(Exception e){

      	 sql = "INSERT INTO mjorder ( " +
        " ORDERID,ORDERACCOUNT,SALESTAKER,ORDERDATE,NOTES,SALESORIGINID,ready,reference,deliverydate,DATAAREAID) " +
        " VALUES ('"+salesid+"','" + order.getCompany() +"','"+ag.getID()+"',getdate(),'"+tekst+"','MJPDA','0','" + ref + "',?,'ark') " ;
      	 PreparedStatement stmt3 = con3.prepareStatement(sql);
      	 stmt3.setTimestamp(1, new java.sql.Timestamp(dat.getTime()));
     	 stmt3.executeUpdate();
     	 stmt3.close();

      }
        stmt2.close();
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
  }



  public String getDriverVersion(){
    return "ArkogrenAXA";
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
  private Connection con5;
  private Connection con6;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}

}
