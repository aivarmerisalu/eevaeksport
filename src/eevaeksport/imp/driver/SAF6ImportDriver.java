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




public class SAF6ImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(SAF6ImportDriver.class.getName());

  public SAF6ImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();

      con2 = DriverManager.getConnection(url +
                                         parent.getProps().getProperty("dsn")
                                         , "sa", "sql");
      con3 = DriverManager.getConnection(url +
                                         parent.getProps().getProperty("dsn")
                                         , "sa", "sql");

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

  public String identifyCustomer(String id,String regnr){
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
    PreparedStatement st2 = con2.prepareStatement(sql4);
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
              " WHERE hi.idgrpricelist=4 AND arca.category='2tase' AND articletype='G' AND pr.id='" + itemid + "'";
        log.info("SALES_LINE_SQL: " + sql);

        Statement st = con2.createStatement();
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
      Statement st = con2.createStatement();
      ResultSet rs = st.executeQuery(sql);
      String text = order.getEmailBody();
      if (text == null){
            text = "";
             }
/*
                           4	0	Madis	Laansalu		Madis
             5	0	Andrus	M�letj�rv	37104180221	Andrus
             6	0	�lo	Sarap		�lo
             7	0	Marina	Butenko	47908050392	Marina
             8	0	Igor	Igumenov	36202030215	Igor
             9	0	Heli	Uustal	46410250242	Heli
             10	0	Evelin	Vainu	46806230288	Evelin
             11	0	Tatjana	Morozova	47505030242	Tatjana
             12	0	Ain	Laansalu		Ain
             13	0	Heiki	Press		Heiki
             14	0	K�lliki	Lepik	46809160296	K�lliki
             15	0	Oleg	Fjodorov	35510202223	Oleg
             16	0		Arnis		1
             17	0		Madis		2
             18	0		Evelin		3
             19	0		Ain		4
             20	0		Mati		5
             21	0		Viktor		6
             22	0		Martin		7
             23	0		Heiki		8
             24	0		Heli		9
             25	0		Valeri		10
             26	0	Andres	Tabor		Andres Tabor
             27	0	Marina	Fjodorova		Marina Fjodorova
             28	0	Mati	Vainu	36411110294	Mati
             29	0	Viktor	Rudi	35309180259	Viktor
             30	0	Andrus	Maldre	36908100327	Maldre
             31	0	Valeri 	Filatov	36907080339	Valeri
             32	0	Martin	Lokk	38106060290	Martin
             33	0	Valentina	Linter	43711200324	Valentina
             34	0	Kalev	Ventsli	36011132720	Kalev
             35	0	Igor 	Dunajev	36406270237	asendust��taja
             36	0	Aleksandr	Kurdasov	36401100245	asendust��taja2
             37	0	Andrus	M�letj�rv		M�letj�rv
             38	0	Maret	Pulver	45006090215	Maret
             40	0	Indrek	V�li	37306200253	Indrek

              */
      while(rs.next()){
        String custCode = rs.getString("code").trim();
         String agent = order.getAgent();
         Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
         
         String agentid = " ";
         if (ag.getID().equals("9")) {
          
           agentid = "8";
         }
         else if (ag.getID().equals("21")) {
        
           agentid = "25";
         }
         else if (ag.getID().equals("22")) {
       
           agentid = "9";
         }
         else if (ag.getID().equals("23")) {
          
           agentid = " ";
         }
         else if (ag.getID().equals("24")) {
        
           agentid = "12";
         }
         else if (ag.getID().equals("25")) {
         
           agentid = "20";
         }

         sql = "INSERT INTO tp (tellnr,kuupaev,tahtaeg,kliendiID,kliendikood" +
             "  ,sisestajaID,sisestaja,lisainfo) " +
             "VALUES (' " + salesid + "',getdate(),getdate()" +
             "," + order.getCompany() + ",'" + custCode +
             "'," + agentid + ",'" + ag.getName() +
             "','MJ " + text.replaceAll("\n"," ") + "')";

    log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        stmt2.executeUpdate();
        stmt2.close();
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
      }
      st.close();

  }




  public String getDriverVersion(){
    return "SAF6";
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
  
  private Connection con2;
  private Connection con3;
  private ImportPanel parent;
  private Order order = null;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}

}
