package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

public class PMImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

  public PMImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con2 = DriverManager.getConnection(url, "sa", "");
      con3 = DriverManager.getConnection(url, "sa", "");
      con4 = DriverManager.getConnection(url2, "sa", "");
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr) {
    return null;
  }

  public int getSalesid() throws SQLException {

    if (order.isIsBill()) {
      int res = Integer.parseInt(order.getDokNr());
      return res;
    }

    String cmd = "SELECT MAX(sernr)+1 AS id from pda_tellimused";
    PreparedStatement st2 = con2.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
    int result = 0;
    while (rs.next()) {
      result = rs.getInt("id");
    }
    return result;
  }

  public String identifyProduct(String ean, String prodID) {
    return null;
  }

  public void makeOrder(Order order) throws SQLException {
    setOrder(order);
    salesid = getSalesid() + "";
    String agent = order.getAgent();
    Agent ag = (Agent) parent.getEksportFrame().getEksportPanel().getAgentsHash().
        get(agent);
    if (ag.isIspilot() && ag.getObject().equals(ag.getObject())) {
      order.setIsBill(true);
    }

  

    addInvoiceHeader(salesid);
    addInvoiceLines(salesid);
    String sql4 = "update pda_arved set status=1 ";
    PreparedStatement st2 = con2.prepareStatement(sql4);
    //st2.setString(1, salesid.trim());
    st2.executeUpdate();
    //String sql5 = "update " + tablename +" set status=1 ";
    String sql5 = "update pda_tellimused  set status=1 ";
        PreparedStatement st3 = con2.prepareStatement(sql5);
        //st2.setString(1, salesid.trim());
        st3.executeUpdate();


  }

  private void addInvoiceLines(String salesid) throws SQLException {

    String agent = order.getAgent();
    Agent ag = (Agent) parent.getEksportFrame().getEksportPanel().getAgentsHash().
        get(agent);
     order.setIsBill(false);
    System.out.println("Laokood Failist '" + order.getDealer() + "'");

    System.out.println("Agendi Staatus '" + ag.getID() + " " +ag.isIspilot() + "'");
    if (ag.isIspilot() && !order.getDealer().trim().equals("HULGI")) {

    System.out.println("See On ARVE !!! '" + ag.getName() + "'");
      order.setIsBill(true);
    }else{

    System.out.println("See On Tellimus !!! '" + ag.getName() + "'");
    }

    String paycode = null;
    String payname = null;
    String paym = null;
    String comcode = order.getCompany();
    String comname = null;

    String sql5 =
        "SELECT delcode,kl.name,kl.deladdr0, custcode AS payer,paydeal " +
        " FROM deladdrvc kl  " +
        " INNER JOIN cuvc mak ON custcode=code LEFT JOIN rebvc ale ON rebcode=ale.code WHERE ltrim(custcode)+ltrim(delcode)='" +
        comcode + "' ";
    Statement st5 = con4.createStatement();
    ResultSet rs5 = st5.executeQuery(sql5);
    while (rs5.next()) {
      comcode = rs5.getString("delcode");
      payname = rs5.getString("name");
      comname = rs5.getString("deladdr0");
      paycode = rs5.getString("payer");
      paym = rs5.getString("paydeal");
    }
    rs5.close();
    st5.close();

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()) {
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine) enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      double ale = orderLine.getDiscount();
      String sql = "";
      double hind = 0;

      sql = "SELECT pr.code AS kood,pr.name AS nimi,pr.uprice1 AS hind,pr.barcode AS EAN ,pr.rdbgroup AS grupp, pr.conversion2 AS tykke ,pr.unittext AS yhik " +
          " FROM invc pr WHERE code='" + itemid + "'";

      log.info("SALES_LINE_SQL: " + sql);

      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String sep = System.getProperty("line.separator");

      while (rs.next()) {
        hind = rs.getDouble("hind");
        String payment = "8";
//8null0000
        if (order.isIsBill()) {
          salesid = order.getDokNr();
          int payme = order.getPaymTerm();
          if(payme==-1){
          	paym = "S";
          }else if(payme==-2){
          	paym = "99";
          }else{
          	paym = payme +"";
          }
          
          
          if(paym.equals("S")){
          	payment = "7";
          }
          
          
       if (salesid.length() == 1) {
         salesid = "000" + salesid;
       }
       if (salesid.length() == 2) {
         salesid = "00" + salesid;
       }
       if (salesid.length() == 3) {
         salesid = "0" + salesid;
       }


          String doknr = payment + ag.getCode() + salesid;
          salesid = doknr;
          sql = "INSERT INTO pda_arved (sernr,invdate,custcode,location,paydeal ,salesman,artcode,quant,price,vrebate,status,deladdrcode)" +
              " VALUES (" + doknr + ",getdate(),'" + paycode + "','" +
              ag.getObject() + "',' " + paym + "','"
              + ag.getID() + "','" + itemid + "'," + kogus + "," + hind + "," +
              ale +
              ",1 ,'" + comcode + "')";
        }
        else {
          String body = " ";
          if(order.getEmailBody() != null){
          body = order.getEmailBody().replaceAll("\n"," ");
          body = order.getEmailBody().replaceAll(sep," ");
          }


          String ob = ag.getObject();

          if (ag.isIspilot()){
          ob = "HULGI";
          }
       System.out.println("hhh" + body);
          sql = "INSERT INTO pda_tellimused (sernr,orddate,custcode,addr0,DELADDRCODE,SHIPADDR0,location,paydeal ,salesman,artcode,quant,price,vrebate,status,spec)" +
              " VALUES (" + salesid + ",getdate(),'" + paycode + "','" +
              payname + "','" + comcode + "','" + comname + "','" +
              ob + "','" + paym + "','"
              + ag.getID() + "','" + itemid + "'," + kogus + "," + hind + "," +
              ale +
             // ",0,'" + body + "')";
          ",0,' ')";

        }

        log.info("SALES_LINE_SQL: " + sql);
        PreparedStatement st2 = con3.prepareStatement(sql);
        st2.executeUpdate();
        setAmount(false);
        addNumLine();
        st2.close();
      }
    }
  }

  private void addInvoiceHeader(String salesid) throws SQLException {

  }

  public String getDriverVersion() {
    return "PM";
  }

  private void setOrder(Order order) {
    this.order = order;
  }

  public boolean isAmount() {
    return isAmount;
  }

  /**
   *
   * @param aUseEAN - the new value for useEAN
   */
  public void setAmount(boolean aAmount) {
    isAmount = aAmount;
  }

  private String url = "jdbc:odbc:hansaimport";

  private String url2 = "jdbc:odbc:HANSA";
  private Connection con2;
  private Connection con3;
  private Connection con4;
  private ImportPanel parent;
  private String salesid = null;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
