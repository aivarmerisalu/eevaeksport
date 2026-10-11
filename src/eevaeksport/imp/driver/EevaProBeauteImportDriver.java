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

public class EevaProBeauteImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public EevaProBeauteImportDriver(ImportPanel parent) {
    PropertyConfigurator.configure("log4j.properties");
    this.parent = parent;
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eeva" ,"postgres","eeta8joma");
    }
    catch (Exception e) {
      parent.showError(e);
      System.exit(0);
    }
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	if(regnr==  null){
  	    sql1 = "SELECT kood FROM firma2.klient where  tulemus= '" + id + "'";	
  	}else{
  	     sql1 = "SELECT kood FROM firma2.klient where  (regnr)= '" + regnr + "' AND tulemus= '" + id + "'";	
  	}
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();

    System.out.println("Klient: " + acc);
    return acc;

  }

  public String getAgent(String custid) throws SQLException{
	    String sql1 = "SELECT objekt2 FROM firma2.klient where kood = '" + custid + "'";


	    String res  = "EST";

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      res = rs1.getString("objekt2").trim();
	    }
	//System.out.println("prod0" + prod);
	  
	    rs1.close();
	    st1.close();

	    return res;
	  }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
	  
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM firma2.toode where left(ribakood,13)='" + ean + "'";
    String sql3 = "SELECT toode FROM firma2.toode where left(ribakood,13)='" + ean.substring(1) + "'";

    String sql2 = "SELECT toode FROM firma2.toode where toode='" + prodID + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
    
    if(prod == null){

        rs1 = st1.executeQuery(sql3);

        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        }
      }

    
//System.out.println("prod0" + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    System.out.println("prod1" + prod);
    rs1.close();
    st1.close();

    return prod;
  }

  

  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    int invoiceID = getNextID();
    String ladu = order.getWarehouse();
    addInvoiceHeader(invoiceID,ladu);
    addInvoiceLines(invoiceID);
  }


  
  private int getDokNr() throws SQLException{
	    String sql =  " select nextval('firma2.dok_t_seq') as seeralgus";

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("seeralgus");
	    }
	    return intNr;
	  }
  
  private int getNextID() throws SQLException{
   
	    String sql =  " select nextval('firma2.dok_dokumnr_seq') as seeralgus";

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("seeralgus");
	    }
	    return intNr;
  }

  private void addInvoiceLines(int invID) throws SQLException{
    
  
    
    Enumeration enumr = order.getOrderLines().elements();
    int count = 1;
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
      
      
      String toode = line.getToode();
      if (toode == null){
      toode = " ";
    }
      double kastis = 1;
      if(line.getUnit().equals("DPA")){
    	  kastis = line.getBonus();
      }
      
      if(order.getStatus() == 1){
    	kastis = kastis * -1;  
      }
      
      
      String nimi = line.getTooteNimi();
      if(nimi.length() > 50){
    	  nimi = nimi.substring(0,50);
      }
      
      String sqlo = "INSERT INTO firma2.rid VALUES('T',?, ?, 0, ?, ?,'','','',0,'','','','',current_date, " +
              "0,0,0,?,'','','','','','','',0,0,'','','',0,0,false,false,0,'',false,0,false,'',?,'','',0,'',current_date)";
   
      
      String sql = "INSERT INTO firma2.rid( " +
              "id, reanr, dokumnr, nimetus, hind, kogus, toode, partii, myygikood, " +
              "hinnak, kaubasumma, yhik, kulukonto, kuluobjekt, rid2obj, reakuupaev, " +
              "kogpak, kulum, baasostu, ostuale, rid3obj, rid4obj, rid5obj, " +
              "rid6obj, rid7obj, rid8obj, rid9obj, kaskogus, aktsiis, kulutoode, " +
              "kulupartii, inpdoktyyp, inpdokumnr, rtaitkogus, fifoexpens, calculrow, " +
              "laosumma, laoraha, variant, taitmata, iseteha, rtellimus, reakaal, " +
              "paritoluri, tellreanr) " +
     " VALUES (nextval('firma2.rid_id_seq'),nextval('firma2.rid_reanr_seq'), ?,'" 
              + nimi + "' , 0, " + line.getKogus()*kastis 
     + ", '" + toode + "', null, null, " +
       "       null, 0, null, null, null, null, now(), " +
        "      0, 0, 0, " + line.getDiscount() + ", null, null, null, " +
         "     null, null, null, null, 0, 0, null, " +
         "     null, null, null, 0, false, false, " +
          "    0, null, false, 0, false, null, " + line.getKogus()*kastis + ", " +
           "   null, " + count+ ")";
      
    
    System.out.println(invID + " " + sql);

      PreparedStatement st = con.prepareStatement(sql);
      
      st.setInt(1, invID);
     //  st.setString(2, );
      //st.setDouble(3, );
      //st.setString(4, toode);
     // st.setDouble(5, line.getDiscount()); //ostuale v�lja paneme allahindluse v��rtuse
     // st.setDouble(6, line.getKogus()*kastis);
      st.executeUpdate();
   
      count++;

      st.close(); 
      setNumAddedLines(getNumAddedLines() + 1);
    }
  }
  
  

  private void addInvoiceHeader(int invID , String ladu ) throws SQLException{
	  if(ladu== null || ladu.trim().length() == 0){
		  ladu = "LADU";
	  }

	  
	    String sqlo = "INSERT INTO firma2.dok VALUES ('T',?, current_date, 'LT1',?,1, 'EUR','" + invID + "','',"
	    		+ " current_date,'"+ladu+"', '1', ?,?,0, false, '','ADMIN', " +
	         "'23 59', '', '1', 1,current_date, 0,?,' ', false,'', '', '', '', '', '','','','mobilejava','','', 0, false,0,0,0, '', '', false, " +
	        "current_date,?,false, 0, '2000000000',0, '', ?,current_date,'','','','','',0) ";
	    
	  String agent = order.getAgent();
	  if(agent.equals("MARIKA") || agent.equals("MEELI") || agent.equals("RUTA") || agent.equals("TAIMI")){
		  agent =  getAgent(order.getCompany());
	  }
	  
	  String sql = "INSERT INTO firma2.dok( doktyyp, dokumnr, kuupaev, oper, klient, laonr, raha, tasudok, " +
           " knr, tasukuup, yksus, sihtyksus, pais2obj, saaja, krdokumnr, " +
           " eimuuda, kasutaja, username, kellaaeg, arvekonto, maksetin, exchrate, " +
          " ratefound, tekst1, viitenr, objrealt, arvenumber, pais3obj, pais4obj, " +
           " pais5obj, pais6obj, pais7obj, pais8obj, pais9obj, masin, tegmasin, " +
           " guid, doksumma, kinnitatud, tasumata, sularaha, kaardimaks, kalkliik, " +
           " kalktoode, inventuur, algus, lopp, taidetud, kaal, \"timestamp\", " +
           
           " vmnr, tellimus, volitaisik, postitatud, liikmesrii, tehingulii, " +
           " tarneklaus, statprots, deliverypa, kurss, eekexchara " +
           " ) VALUES (" +
           " 'T', ?, now(), 'LT1', ?, 1, 'EUR', '" + invID + "', " +
           " '13111', now(), '"+ladu+"', null, ?, ?, null, " +
           " false, 'ANU','ADMIN', '23 59', '112', null, 1 ," +
           " now(), ?, '', false, null, null, null, " +
           " null, null, null, null, null, 'mobil', null, " +
           " '" + Calendar.getInstance().getTimeInMillis() + "', 0, false, 0, 0, 0, '', " +
           " null, false, now(), ?, false, 0, '20151027111600', " +
           " null, '', null, now(), null, null, " +
           " null, null, null, 0, 0 " +
           " )" ;
	  

    String ordernumber = order.getDokNr();
	  System.out.println("oo "+sql);
    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    stmt.setString(3, agent);
    stmt.setString(4, order.getCompany());
    String info = order.getInfo();
    if(info == null){
    	info = " ";
    }
    stmt.setString(5, info);
    stmt.setTimestamp(6,new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
   // stmt.setString(7, ordernumber);

    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "EevaProBeaute";
  }

  private void setOrder(Order order){
    this.order = order;
  }


  private String url = "jdbc:odbc:";
  private Connection con;
  private Order order = null;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
