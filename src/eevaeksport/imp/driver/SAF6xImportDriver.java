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


public class SAF6xImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

  public SAF6xImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url +
                                        parent.getProps().getProperty("dsn")
                                        , "sa", "sql");
      con2 = DriverManager.getConnection(url +
                                         parent.getProps().getProperty("dsn")
                                         , "sa", "sql");
      con3 = DriverManager.getConnection(url +
                                         parent.getProps().getProperty("dsn")
                                         , "sa", "sql");
      con4 = DriverManager.getConnection(url +
            parent.getProps().getProperty("dsn")
            , "sa", "sql");
      con5 = DriverManager.getConnection(url +
               parent.getProps().getProperty("dsn")
               , "sa", "sql");

      con6 = DriverManager.getConnection(url +
            parent.getProps().getProperty("dsn")
            , "sa", "sql");


    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr) {
    return null;
  }

  public int getSalesid() throws SQLException{
    String cmd = "SELECT MAX(tp_id)+1 AS id from tp ";
    PreparedStatement st2 = con2.prepareStatement(cmd);
    ResultSet rs = st2.executeQuery();
    int result = 0 ;
    while (rs.next()){
    result =  rs.getInt("id");
    }
     return result;
  }

  public String identifyProduct(String ean, String prodID){
    return null;
  }

  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    String salesid = getSalesid() + "";
    addInvoiceHeader(salesid);
    addInvoiceLines(salesid);
    String sql4 = "update tp set staatus=1 where ltrim(tellnr) = ?";
    PreparedStatement st2 = con.prepareStatement(sql4);
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
      double ale = orderLine.getDiscount();
      int tpid = 0;
      double hind = 0;
          String sql3 = "SELECT MAX(tp_id)AS idx FROM tp ";
          Statement st3 = con3.createStatement();
          ResultSet rs3 = st3.executeQuery(sql3);
          while (rs3.next()) {
          tpid = rs3.getInt("idx");
          }

          String sql = "SELECT pr.code AS kood,hi.price AS hind FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id " +
              " RIGHT JOIN grpricelistrel hi ON hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
              " RIGHT JOIN grunit y ON y.id=pr.idgrunit " +
              " WHERE hi.idgrpricelist=26 AND  articletype='G' AND pr.id='" + itemid + "'";
        log.info("SALES_LINE_SQL: " + sql);

        Statement st = con5.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (ale != 0){
          hind = rs.getDouble("hind")*(1-ale)*kogus;
        }

        while(rs.next()){
          sql = "INSERT INTO ts (tp_id,kaubaID,kaubakood,kogus,hind)" +
            " VALUES( " +
              tpid + "," +
              itemid + ",'" +
              rs.getString("kood") + "'," +
              kogus + "," +
              hind +")";

              log.info("SALES_LINE_SQL: " + sql);
              PreparedStatement st2 = con3.prepareStatement(sql);
              st2.executeUpdate();
              setAmount(false);
              addNumLine();
              st2.close();
          }
       }
    }




  private void addInvoiceHeader(String salesid) throws SQLException{
    String sql = "SELECT code FROM grcompany WHERE LTRIM(id)='" + order.getCompany() + "'";

      log.info("addInvoiceHeader: " + sql);
      Statement st = con4.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String text = order.getEmailBody();
      if (text == null){
            text = "";
             }

      while(rs.next()){
        String custCode = rs.getString("code").trim();
         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
/*
                   20   PL		Peter Lindeberg
          21 VE	 	Veronika Ello


         9   KO		Katrin Ots
         11 Tax Free	Tax Free - selleks saab kunagi Jaanus K�rva, praegu aktiivset m��ki ei ole
         12 K		Kontor ---vist ei l�he vaja
         20 ML	 	Merle Laidro
 2	0	K�lli	Rebane		Kylli
3	0	�lle	Mootse		Ylle
4	0	Kersti	Valter		Kersti
5	0	Karmen	Kadakas		Karmen
6	0	Timo	Tammsaar		Timo
7	0	Peter	Lindeberg		PL
8	0	Jaanus	Lajal		JL
9	0	Katrin	Ots		KO
10	0	Laili	Taru		LT
11	0		Tax Free		Tax Free
12	0		Kontor		K
18	0	Jaanus	K�rva		Jaanus
19	0	Leelet	Nellis		Leelet
20	0	Merle	Laidro		ML
21	0	Helena	Kuris		Helena
22	0	Eva	Narro		Eva
23	0	Triinu	Kipper		Triinu
24	0	Aigar	Sakk		AS
25	0	Veronika	Ello		VE

          */
         String agentID = " ";
         String agentid = " ";
         if (ag.getID().equals("20")) {
           agentID = "PL";
           agentid = "7";
         }
         else if (ag.getID().equals("21")) {
           agentID = "VE";
           agentid = "25";
         }
         else if (ag.getID().equals("49")) {
           agentID = "ML";
           agentid = "20";
         }
         else if (ag.getID().equals("23")) {
           agentID = "Tax Free";
           agentid = " ";
         }
         else if (ag.getID().equals("24")) {
           agentID = "K";
           agentid = "12";
         }
         else if (ag.getID().equals("25")) {
           agentID = "ML";
           agentid = "20";
         }



         int o = 1;
         if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==6){
             o = 3;
         }


         sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
             "  ,sisestajaID,sisestajakood,sisestaja,lisainfo) " +
             "VALUES (' " + salesid + "',getdate()+1+" + o + ",getdate()+1" +
             "," + order.getCompany() + ",'" + custCode +
             "','" + agentid + "','" + agentID + "','" + ag.getName() +
             "','MJ')";
 			// "','MJ " + text + "')";

    log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con6.prepareStatement(sql);
        stmt2.executeUpdate();
        stmt2.close();
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
      }
      st.close();

  }


  public String getDriverVersion(){
    return "SAF6x";
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
  private Connection con6;
  private Connection con2;
  private Connection con3;
  private Connection con4;
  private Connection con5;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}

}
