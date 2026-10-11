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

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class SireImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public SireImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "kiire");
      con2 = DriverManager.getConnection(url + "kiire");
    } catch (Exception e){
       parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr){
    return null;
  }

  public String identifyProduct(String ean, String prodID){
    return null;
  }

  public void makeOrder(Order order){
    //if (true) return;
    setOrder(order);
    String invoiceID = getNextID();
    addInvoiceHeader(invoiceID);
    addInvoiceLines(invoiceID);
    //String salesid = updateMyygNumberSequence();
    //addInvoiceLines(salesid);
  }

  private String getNextID(){

    String s = null;
    String result = null;
    try {
      Statement st = con2.createStatement();
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
    PreparedStatement pst = con2.prepareStatement(sql);
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

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      String sql = "SELECT ?*summa AS laosumma ,kogus FROM ladu/laosaldo WHERE yksus=? AND artikkel=? ";

      PreparedStatement st = con2.prepareStatement(sql);
      st.setDouble(1, kogus);
      int objekt = Integer.parseInt(((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent())).getObject());
      st.setInt(2, objekt);
      st.setInt(3, Integer.parseInt(itemid));
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


      sql = "SELECT hk_ FROM yksus WHERE yksus = ? AND hk_ <>''";
      PreparedStatement pst = con.prepareStatement(sql);
       pst.setInt(1, Integer.parseInt(order.getCompany()));
      ResultSet rst = pst.executeQuery();
      String hk = "hulgi";
   while (rst.next()){
        hk = rst.getString("hk_").trim();
      }
      rst.close();
      pst.close();

      sql = "SELECT  l.hk_" + hk + " AS price FROM ladu/laduart l INNER JOIN artikkel a ON l.artikkel=a.artikkel WHERE l.artikkel=?";
      //sql = "SELECT  l. AS price,a.grupp FROM ladu/laduart l INNER JOIN artikkel a ON l.artikkel=a.artikkel WHERE l.artikkel=?";

      st = con2.prepareStatement(sql);
      st.setInt(1, Integer.parseInt(itemid));
      rs = st.executeQuery();
      double price = 0;
      String grupp = "";
      if (rs.next()){
        price = rs.getDouble("price");
        //grupp = rs.getString("grupp");
      }
      rs.close();
      st.close();

      sql = "SELECT hk_prots AS percent FROM yksus WHERE yksus=? ";
       //sql = "SELECT (hkg_" + grupp + " + hk_prots) AS percent FROM yksus WHERE yksus=?";
      log.info(sql + "\n" + "hkg_" + grupp + "\n" + order.getCompany());
      st = con2.prepareStatement(sql);
      //st.setString(1, "hkg_" + grupp);
      st.setInt(1, Integer.parseInt(order.getCompany()));
      rs = st.executeQuery();
      double percent = 0;
      if (rs.next()){
        percent = Double.parseDouble(rs.getString("percent").replace(',','.'));
      }
      rs.close();
      st.close();
      double pr = getPrice(kogus, -percent, price);

      sql = "INSERT INTO ladu/lstl VALUES ( " +
          "?," +
          Integer.parseInt(itemid) + "," +
           kogus +"," +
          laosumma +"," +
          pr +"," +
          "''," +
          "0," +
          "0," +
          "''," +
          "0," +
          "100," +
          "date()," +
          "''," +
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

     PreparedStatement stm = con2.prepareStatement(sql);
          stm.setString(1, invID);
          order.setSum(order.getSum() + pr);
          stm.executeUpdate();
          setNumAddedLines(getNumAddedLines() + 1);
    }

    } catch (Exception e){
       parent.showError(e);
    }
  }

  private void addInvoiceHeader(String invID){
    String sql = "SELECT hk_,makseting FROM yksus WHERE yksus=?";
    //String sql = "SELECT makseting FROM yksus WHERE yksus=?";

  log.info("addInvoiceHeader: " + sql);

    try {
      PreparedStatement st = con2.prepareStatement(sql);
      st.setInt(1, Integer.parseInt(order.getCompany()));
      ResultSet rs = st.executeQuery();
      while(rs.next()){

        int ting = 0;
        try {
        ting = rs.getInt("makseting");
        }catch(Exception e){}
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
        "'A'," +
        "'P'," +
       "date(1899,12,30)," +
        "0," +
        "''," +
     //   "'"+ rs.getString("hk_") + "'," +
         "'Hulgi'," +
        "?," +
        "0,"  +
        "'411" + order.getAgent()+ "'," +
        "'311" + order.getAgent()+ "'," +
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
        PreparedStatement stmt2 = con2.prepareStatement(sql);
        stmt2.setInt(1, Integer.parseInt(order.getCompany()));
        stmt2.setInt(2,
          Integer.parseInt(((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent())).getObject()));
        stmt2.setString(3, "112" + order.getAgent() );
        String dokNr = order.getDokNr();
        if (dokNr == null){
          dokNr = String.valueOf(updateNumberSequence(SEQUENCE_NVALJA));
        }
        stmt2.setInt(4, Integer.parseInt(dokNr));
        stmt2.setInt(5, ting);
        //stmt2.setString(6, rs.getString("hk_vaik"));
        stmt2.setString(6, invID);
        //stmt2.setInt(8, updateNumberSequence(SEQUENCE_IDVALJA));
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
      PreparedStatement st = con2.prepareStatement(sql);
      st.setString(1, idtag);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        //System.out.println("number:" + rs.getString("NEXTREC"));
        id = rs.getInt("nextrec") + 1;
      //log.info("updateNumberSequence()3: " + id);
      }
      st.close();

      sql = "UPDATE ladu/tagladu SET idnum=? WHERE tagladu.idtag=?";
      log.info("updateNumberSequence(): " + sql);
      st = con2.prepareStatement(sql);
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

  public static void main(String [] args){
    //System.out.println(getPrice(32, -6.5, 1.96));
    System.exit(0);
  }

  public static double getPrice(double kogus, double percent, double price){
    //System.out.println("kogus: " + kogus + " percent: " + percent + "price: " + price);
    double value = kogus * price*((100+percent)/100);
    value = (Math.round((value*100d)))/100d;
    return value;
  }

  public String getDriverVersion(){
    return "Sire";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private void addLog(String line){
    parent.addLog(line);
  }

  //private String url = "jdbc:DBF:///c://work/avallone/dbf";
  private String url = "jdbc:odbc:";
  private Connection con;
  private Connection con2;
  private ImportPanel parent;
  private Order order = null;
  public static final String SEQUENCE_NVALJA = "NVALJA";
  public static final String SEQUENCE_IDVALJA = "IDVALJA";

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
