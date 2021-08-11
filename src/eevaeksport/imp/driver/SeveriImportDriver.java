package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

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

public class SeveriImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public SeveriImportDriver(ImportPanel parent) {
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
    try {
      String invoiceID = getNextID(SEQUENCE_VLADU, getSQLLaduSerial(), getSQLLaduUpdate());
      this.viit = getNextID(SEQUENCE_VPROJEKT, getSQLLaduSerial(), getSQLLaduUpdate());
      addInvoiceHeader(invoiceID);
      addInvoiceLines(invoiceID);
      addProjectLine(invoiceID);

      addPankHeader(invoiceID);
      addPankLines(invoiceID);
      addPankProjectLine();
      //String salesid = updateMyygNumberSequence();
      //addInvoiceLines(salesid);
    } catch (SQLException e){
      parent.showError(e);
    }
  }

  private String getNextID(String id, String selectSQL, String updateSQL) throws SQLException{

    String s = null;
    String result = null;
      //String sql = "SELECT idviit FROM ladu/tagladu WHERE idtag = ?";
      PreparedStatement st = con.prepareStatement(selectSQL);
      st.setString(1, id);
      ResultSet rs = st.executeQuery();
      if (rs.next()){
        s = rs.getString("idviit");
      }
      rs.close();
      st.close();

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

    PreparedStatement pst = con2.prepareStatement(updateSQL);
    pst.setString(1, result);
    pst.setString(2, id);
    pst.executeUpdate();
    pst.close();

    return result;
  }

  private void addPankLines(String invID) throws SQLException{

    Enumeration enumr = order.getOrderLines().elements();
    while (enumr.hasMoreElements()){
      String sql = "INSERT INTO pank/stlrpank VALUES (?,?,?,?,'',0,?,'P')";
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, getVstlpank());
      st.setInt(2, Integer.parseInt(itemid));
      st.setDouble(3, kogus);
      st.setDouble(4, 0); //reasummat Aivar ei viitsinud panna pidi sanatooriumisse minema
      st.setString(5, getPankprojekt());
      st.executeUpdate();
      st.close();
    }
    /*
    1. getAndUpdate(VSTLPANK)
    2. artikli kood
    3. kogus
    4. reasumma
    5. getAndUpdate(VPROJEKT)    */
  }

  private void addInvoiceLines(String invID) throws SQLException{

    Enumeration enumr = order.getOrderLines().elements();
    int counter = 0;
    while (enumr.hasMoreElements()){
      counter++;
      log.info("ORDER_LINE_COUNTER:" + counter);
      OrderLine orderLine = (OrderLine)enumr.nextElement();
      String itemid = orderLine.getToode();
      double kogus = orderLine.getKogus();
      String sql = "SELECT ?*summa AS laosumma ,kogus FROM ladu/laosaldo WHERE yksus=? AND artikkel=?";

      PreparedStatement st = con2.prepareStatement(sql);
      st.setDouble(1, kogus);
      st.setInt(2,
        Integer.parseInt(((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent())).getObject()));
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

      doClientInfo();

      sql = "SELECT  hk_" + hinnakiri + " AS price FROM ladu/laduart WHERE artikkel=?";
      st = con2.prepareStatement(sql);
      st.setInt(1, Integer.parseInt(itemid));
      rs = st.executeQuery();
      double price = 0;
      if (rs.next()){
        price = rs.getDouble("price");
      }
      rs.close();
      st.close();



      sql = "INSERT INTO ladu/lstl VALUES(" +
          "?," +
          "?," +
          "?," +
          "?," +
          "?," +
          "''," +
          "0," +
          "0," +
          "''," +
          "0," +
          "?," +
          "?," +
          "''," +
          "''," +
          "0," +
          "0," +
          "?," +
          "0," +
          "''," +
          "0," +
          "''," +
           "0," +
           "0" +
          ")";

          log.info("SALES_LINE_SQL: " + sql);

          st = con2.prepareStatement(sql);
          st.setString(1, invID);
          st.setInt(2, Integer.parseInt(itemid));
          st.setDouble(3, kogus);
          st.setDouble(4, laosumma);
          st.setDouble(5, getPrice(kogus, percent, price));
          st.setInt(6, 0);
          st.setDate(7, new java.sql.Date(getDocDate(((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(area)).getDays()).getTime()));
          //st.setDate(7, new java.sql.Date(getDocDate(area).getTime()));
          this.viit = getNextID(SEQUENCE_VPROJEKT, getSQLLaduSerial(), getSQLLaduUpdate());
          st.setString(8, this.viit);
          st.executeUpdate();
          st.close();
          setNumAddedLines(getNumAddedLines() + 1);
    }
  }

  private void doClientInfo() throws SQLException{
    String sql = "SELECT hk_prots AS percent,hk_ AS hinnakiri, p1 AS area FROM yksus WHERE yksus=?";
    //System.out.println(sql + "\n" + "hkg_" + grupp + "\n" + order.getCompany());
    PreparedStatement st = con2.prepareStatement(sql);
    //st.setString(1, "hkg_" + grupp);
    st.setInt(1, Integer.parseInt(order.getCompany()));
    ResultSet rs = st.executeQuery();
    if (rs.next()){
      percent = rs.getDouble("percent");
      hinnakiri = rs.getString("hinnakiri");
      area = rs.getString("area").trim();
      if (area.equals("")){
        area = "PU";
      }
    }
    rs.close();
    st.close();
  }

  private void addProjectLine(String invID) throws SQLException{

    String sql = "INSERT INTO ladu/projladu VALUES(?,?,?)";
    PreparedStatement st = con.prepareStatement(sql);
    st.setString(1, viit);
    st.setString(2, area);
    st.setString(3, order.getAgent());
    st.executeUpdate();
    st.close();
  }

  private void addPankProjectLine() throws SQLException{

    String sql = "INSERT INTO pank/projpank VALUES(?,?,?)";
    PreparedStatement st = con.prepareStatement(sql);
    st.setString(1, getPankprojekt());
    st.setString(2, area);
    st.setString(3, order.getAgent());
    st.executeUpdate();
    st.close();
  }


  private void addInvoiceHeader(String invID) throws SQLException{
    //String sql = "SELECT makseting, hk_vaik FROM yksus WHERE yksus=?";
    String sql = "SELECT makseting,hk_,hk_prots FROM yksus WHERE yksus=?";

  log.info("addInvoiceHeader: " + sql);

      PreparedStatement st = con2.prepareStatement(sql);
      st.setInt(1, Integer.parseInt(order.getCompany()));
      ResultSet rs = st.executeQuery();
      while(rs.next()){

      sql = "INSERT INTO ladu/ladu VALUES (" +
        "'V'," +
        "?," +
        "?," +
        "date()," + //date
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
        "date()," + //date
        "0," +
        "''," +
        "?," +
        "?," +
        "?,"  +
        "''," +
        "''," +
        "0," +
        "0," +
        "''," +
        "''," +
        "0," +
        "'P'," +
        "?," +
        "?," +
        "''," +
        "date()," + //date
        "0," +
        "''," +
        "''," +
        "'')";
      System.out.println(sql);
        log.info("addInvoiceHeader2: " + sql);
        PreparedStatement stmt2 = con2.prepareStatement(sql);

        doClientInfo();

        java.sql.Date dt = new java.sql.Date(getDocDate(((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(area)).getDays()).getTime());
        //java.sql.Date dt = new java.sql.Date(getDocDate(area).getTime());

        stmt2.setDate(1, dt);
        stmt2.setDate(2, dt);
        stmt2.setInt(3, Integer.parseInt(order.getCompany()));
        String obj = ((Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent())).getObject();
        System.out.print("obj=" + obj);
        stmt2.setInt(4, Integer.parseInt(obj));
        stmt2.setString(5, "62");
      //  String dokNr = order.getDokNr();
      //  if (dokNr == null){
          String dokNr = String.valueOf(updateNumberSequence(SEQUENCE_NVALJA));
          order.setDokNr(dokNr);
       // }
        stmt2.setString(8, rs.getString("hk_"));
        stmt2.setInt(6, Integer.parseInt(dokNr));
        setMakseTingimus(rs.getInt("makseting"));
        stmt2.setInt(7, getMakseTingimus());
        //stmt2.setString(6, rs.getString("hk_vaik"));
        stmt2.setString(9, invID);
        setSaateleheNr(updateNumberSequence(SEQUENCE_UIDVALJA));
        stmt2.setInt(10, getSaateleheNr());

         this.viit = getNextID(SEQUENCE_VPROJEKT, getSQLLaduSerial(), getSQLLaduUpdate());

        addProjectLine(invID);
        stmt2.setString(11, this.viit);
        stmt2.setInt(12, rs.getInt("hk_prots"));
        stmt2.executeUpdate();
        addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
        stmt2.close();
      }
      st.close();
  }

  private void addPankHeader(String invID) throws SQLException{
    String sql = "INSERT INTO pank/stlpank VALUES (?,?,'7','',?,?,300,?,0,0,'A',DATE(),'V',0,0,0,'',?,'','','',date(),0,?,'P','P','') ";
    PreparedStatement st = con.prepareStatement(sql);
    setVstlpank(getNextID(SEQUENCE_VSTLPANK, getSQLPankSerial(), getSQLPankUpdate()));
    st.setString(1, getVstlpank());
    st.setString(2, invID);
    st.setInt(3, getSaateleheNr());
    st.setInt(4, Integer.parseInt(order.getCompany()));
    st.setInt(5, getMakseTingimus());

    setVarveldus(getNextID(SEQUENCE_VARVELDUS, getSQLPankSerial(), getSQLPankUpdate()));
    st.setString(6, getVarveldus());

    setPankprojekt(getNextID(SEQUENCE_VPROJEKT, getSQLPankSerial(), getSQLPankUpdate()));
    st.setString(7, getPankprojekt());

    /*1. getAndUpdate(VSTLPANK)
    2. tellimuse viit
    3. doknr
    4. saaja
    5. makset�htaeg
    6. getAndUpdate(VARVELDUS)
    7. getAndUpdate(VPROJEKT)
    */
    st.executeUpdate();
    st.close();
  }

  private int updateNumberSequence(String idtag) throws SQLException{
    int id = -1;

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
    return id;
  }

  public static double getPrice(double kogus, double percent, double price){
    System.out.println("kogus: " + kogus + " percent: " + percent + "price: " + price);
    double value = kogus * price*((100+percent)/100);
    value = (Math.round((value*100d)))/100d;
    value = Math.round(value*20d)/20d;

    return value;
  }

  public static java.util.Date getDocDate(String days){
    days = days.toUpperCase();
    Calendar cal = Calendar.getInstance();
    Vector vec = new Vector();
    for (int i = 0 ; i < days.length() ; i++){
      char c = days.charAt(i);
      if ( c == 'E'){
        vec.addElement(new Integer(Calendar.MONDAY));
      } else if (c == 'T'){
        vec.addElement(new Integer(Calendar.TUESDAY));
      } else if (c == 'K'){
        vec.addElement(new Integer(Calendar.WEDNESDAY));
      } else if (c == 'N'){
        vec.addElement(new Integer(Calendar.THURSDAY));
      } else if (c == 'R'){
        vec.addElement(new Integer(Calendar.FRIDAY));
      } else if (c == 'L'){
        vec.addElement(new Integer(Calendar.SATURDAY));
      } else if (c == 'P'){
        vec.addElement(new Integer(Calendar.SUNDAY));
      }
    }

    if (vec.size() == 0){
      if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
        cal.add(Calendar.DAY_OF_WEEK, 3);
      } else {
        cal.add(Calendar.DAY_OF_WEEK, 1);
      }
    } else {
      for (int i = 1 ; i < 8 ; i++){
        cal.add(Calendar.DAY_OF_WEEK, 1);
        if (vec.contains(new Integer(cal.get(Calendar.DAY_OF_WEEK)))){
          break;
        }
      }
    }

    return cal.getTime();
  }

  public String getDriverVersion(){
    return "Severi";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private void addLog(String line){
    parent.addLog(line);
  }

  public String getImportFile(){
    return "R:/Mobile Tellimused/";
  }

  private String getSQLLaduSerial(){
    return "SELECT idviit FROM ladu/tagladu WHERE idtag = ?";
  }

  private String getSQLPankSerial(){
    return "SELECT idviit FROM pank/tagpank WHERE idtag = ?";
  }

  private String getSQLLaduUpdate(){
    return "UPDATE ladu/tagladu SET idviit=? WHERE idtag=?";
  }

  private String getSQLPankUpdate(){
    return "UPDATE pank/tagpank SET idviit=? WHERE idtag=?";
  }
  /**
   *
   * @return the int value of saateleheNr.
   */
  public int getSaateleheNr(){
    return saateleheNr;
  }

  /**
   *
   * @param aSaateleheNr - the new value for saateleheNr
   */
  public void setSaateleheNr(int aSaateleheNr){
    saateleheNr = aSaateleheNr;
  }

  /**
  *
  * @return the int value of makseTingimus.
  */
  public int getMakseTingimus(){
          return makseTingimus;
  }

  /**
  *
  * @param aMakseTingimus - the new value for makseTingimus
  */
  public void setMakseTingimus(int aMakseTingimus){
          makseTingimus = aMakseTingimus;
  }

  /**
  *
  * @return the String value of varveldus.
  */
  public String getVarveldus(){
          return varveldus;
  }

  /**
  *
  * @param aVarveldus - the new value for varveldus
  */
  public void setVarveldus(String aVarveldus){
          varveldus = aVarveldus;
  }


  /**
  *
  * @return the String value of vstlpank.
  */
  public String getVstlpank(){
          return vstlpank;
  }

  /**
  *
  * @param aVstlpank - the new value for vstlpank
  */
  public void setVstlpank(String aVstlpank){
          vstlpank = aVstlpank;
  }


  /**
  *
  * @return the String value of pankprojekt.
  */
  public String getPankprojekt(){
          return pankprojekt;
  }

  /**
  *
  * @param aPankprojekt - the new value for pankprojekt
  */
  public void setPankprojekt(String aPankprojekt){
          pankprojekt = aPankprojekt;
  }
  //private String url = "jdbc:DBF:///c://work/avallone/dbf";
  private String url = "jdbc:odbc:";
  private Connection con;
  private Connection con2;
  private ImportPanel parent;
  private Order order = null;

  public static final String SEQUENCE_NVALJA = "UNTELL";
  public static final String SEQUENCE_IDVALJA = "IDVALJA";
  public static final String SEQUENCE_VLADU = "VLADU";
  public static final String SEQUENCE_VPROJEKT = "VPROJEKT";
  public static final String SEQUENCE_UIDVALJA = "UIDVALJA";
  public static final String SEQUENCE_VARVELDUS = "VARVELDUS";
  public static final String SEQUENCE_VSTLPANK = "VSTLPANK";
  private String viit = null;
  private String area = "PU";
  private double percent = 0;
  private String hinnakiri = null;
  private int saateleheNr = -1;
  private int makseTingimus = -1;
  private String varveldus = null;
  private String vstlpank = null;
  private String pankprojekt = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}