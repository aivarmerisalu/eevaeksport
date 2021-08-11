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
import eevaeksport.imp.POP3Reader;

public class VipexLITImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(VipexImportDriver.class.getName());

  public VipexLITImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con2 = DriverManager.getConnection(url, "pda", "pda");
      con3 = DriverManager.getConnection(url, "pda", "pda");

      con4 = DriverManager.getConnection(url2, "pda", "pda");

      POP3Reader pop3 = new POP3Reader(parent.getEksportFrame().getString(
          "moborder"));
      pop3.setPop3host(parent.getProps().getProperty("pop3.host"));
      pop3.setUser(parent.getProps().getProperty("pop3.user"));
      pop3.setPassword(parent.getProps().getProperty("pop3.password"));
      pop3.setSaveDir(getImportFile());
      pop3.readFiles(true);

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
    String doknr = order.getDokNr();

    String salesid = getSalesid() + "";
    String tablename = "pda_tellimused";
    String agent = order.getAgent();
    Agent ag = (Agent) parent.getEksportFrame().getEksportPanel().getAgentsHash().
        get(agent);
    if (ag.isIspilot() && ag.getObject().equals(ag.getObject())) {
      order.setIsBill(true);
    }

    if (order.isIsBill()) {
      tablename = "pda_arved";
      if (doknr != null && doknr.trim().length() > 0) {
        salesid = order.getDokNr();
      }
    }

    addInvoiceHeader(salesid);
    addInvoiceLines(salesid);
    String sql4 = "update " + tablename + " set status=1 where sernr = ?";
    PreparedStatement st2 = con2.prepareStatement(sql4);
    st2.setString(1, salesid.trim());
    st2.executeUpdate();
  }

  private void addInvoiceLines(String salesid) throws SQLException {

    String agent = order.getAgent();
    Agent ag = (Agent) parent.getEksportFrame().getEksportPanel().getAgentsHash().
        get(agent);

    String paycode = null;
    String payname = null;
    String paym = null;
    String comcode = order.getCompany();
    String comname = null;

    String sql5 ="SELECT (case a.shiptoid when 'DEFAULT' then a.custid else a.shiptoid end) AS saaja,a.custid AS kood,a.name AS nimi,terms AS paydeal " +
                   " FROM customer k INNER JOIN soaddress a ON k.custid=a.custid LEFT JOIN snote s ON k.noteid=s.nid   " +
                   " WHERE  k.status='A' AND LEN(a.name)>0 AND (case a.shiptoid when 'DEFAULT' then a.custid else rtrim(a.custid) + a.shiptoid end)= '" +comcode + "' ";
    Statement st5 = con4.createStatement();
    ResultSet rs5 = st5.executeQuery(sql5);
    while (rs5.next()) {
      comcode = rs5.getString("saaja");
      paycode = rs5.getString("kood");
      String nimi = rs5.getString("nimi");
      payname = nimi;
      comname = nimi;
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

      sql = "   SELECT t.invtid AS kood,t.descr AS nimi,t.stkbaseprc AS hind,t.upccode AS ean , " +
               "     priceclassid AS grupp,c.cnvfact AS tykke,t.dfltpounit AS unit FROM INVENTORY t " +
                "   left JOIN inunit c ON  t.invtid=c.invtid left JOIN inventoryadg g ON t.invtid=g.invtid  " +
                 " WHERE t.transtatuscode='AC' AND t.invtid='" + itemid + "'";

      log.info("SALES_LINE_SQL: " + sql);

      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);

      while (rs.next()) {
        hind = rs.getDouble("hind");
   //     SERNR ORDDATE  CUSTCODE  ADDR0  DELADDRCODE SHIPADDR0 LOCATION  PAYDEAL SALESMAN ARTCODE SPEC QUANT PRICE VREBATE STATUS

          sql = "INSERT INTO pda_tellimused (sernr,orddate,custcode,addr0,DELADDRCODE,SHIPADDR0,location,paydeal ,salesman,artcode,quant,price,vrebate,status,spec)" +
              " VALUES (" + salesid + ",getdate(),'" + paycode + "','" +
              payname + "','" + comcode + "','" + comname + "','" +
              ag.getObject() + "','" + paym + "','"
              + ag.getID() + "','" + itemid + "'," + kogus + "," + hind + "," +
              ale +
              ",0,'" + order.getEmailBody() + "')";



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
    return "VipexLIT";
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

  private String url = "jdbc:odbc:BMSDSN";
  private String url2 = "jdbc:odbc:BMSDSN";
  private Connection con2;
  private Connection con3;
  private Connection con4;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}

}
