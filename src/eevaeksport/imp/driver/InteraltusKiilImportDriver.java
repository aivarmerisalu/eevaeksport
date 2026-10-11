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

public class InteraltusKiilImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public InteraltusKiilImportDriver(ImportPanel parent) {
   
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    this.parent = parent;
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eevakiil");
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	if(regnr==  null){
  	    sql1 = "SELECT kood FROM firma1.klient where   fil= '" + id + "'";	
  	}else{

  	     sql1 = "SELECT kood FROM firma1.klient where  (regnr)= '" + regnr + "' AND fil= '" + id + "'";	
  	}
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    
    if(acc != null){
    	return acc ;
    }
    sql1 = "SELECT kood FROM firma1.klient where   kood= '" + id + "'";	
    rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();
    }
    
    
    
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM firma1.toode where ribakood='" + ean + "'";


    String sql2 = "SELECT toode FROM firma1.toode where toode='" + prodID + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
//System.out.println("prod0" + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();

    return prod;
  }

  
  public String getProductName(String prodID) throws SQLException{


	    String sql2 = "SELECT nimetus FROM firma1.toode where toode='" + prodID + "'";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql2);

	    while (rs1.next()) {
	      prod = rs1.getString("nimetus").trim();
	    }

	    rs1.close();
	    st1.close();

	    return prod;
	  }


  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    int invoiceID = getNextID();
    addInvoiceHeader(invoiceID);
    addInvoiceLines(invoiceID);
  }

  private int getNextID() throws SQLException{
	  
	    String sql =  " select nextval('firma1.dok_dokumnr_seq') as seeralgus";

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("seeralgus");
	    }
	    return intNr;
}

  
  
  
  


  private void addInvoiceHeader(int invID) throws SQLException{
	  String ladu = "1";

	  
	    String sqlo = "INSERT INTO firma1.dok VALUES ('T',?, current_date, 'LT1',?,1, 'EUR','" + invID + "','',"
	    		+ " current_date,'"+ladu+"', '1', ?,?,0, false, '','ADMIN', " +
	         "'23 59', '', '1', 1,current_date, 0,?,' ', false,'', '', '', '', '', '','','','mobilejava','','', 0, false,0,0,0, '', '', false, " +
	        "current_date,?,false, 0, '2000000000',0, '', ?,current_date,'','','','','',0) ";
	    
	  String agent = order.getAgent();
	  String ordernumber = null;
	    if(order.getStatus() > 10){
	    	ordernumber = order.getDokNr();
	    }
	  
	  String sql = "INSERT INTO firma1.dok( doktyyp, dokumnr, kuupaev, oper, klient, laonr, raha, tasudok, " +
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
           " '1311', now(), '"+ladu+"', null, ?, ?, null, " +
           " false, '','ADMIN', '23 59', '112', null, 1 ," +
           " now(), ?, ?, false, null, null, null, " +
           " null, null, null, null, null, 'mobil', null, " +
           " '" + Calendar.getInstance().getTimeInMillis() + "', 0, false, 0, 0, 0, '', " +
           " null, false, now(), ?, false, 0, '20151027111600', " +
           " null, '', null, now(), null, null, " +
           " null, null, null, 0, 0 " +
           " )" ;
	  

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
    stmt.setString(6, ordernumber);
    stmt.setTimestamp(7,new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
   //

    stmt.executeUpdate();
    stmt.close();
  }
  
  
  
  
  

  
  private boolean addInvoiceLines(int invID) throws SQLException{
	    
	     boolean result = false;
	  StringBuffer sb = new StringBuffer(); 
	        
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
	      
	     
	      
	      String sql = "INSERT INTO firma1.rid( " +
	              "id, reanr, dokumnr, nimetus, hind, kogus, toode, partii, myygikood, " +
	              "hinnak, kaubasumma, yhik, kulukonto, kuluobjekt, rid2obj, reakuupaev, " +
	              "kogpak, kulum, baasostu, ostuale, rid3obj, rid4obj, rid5obj, " +
	              "rid6obj, rid7obj, rid8obj, rid9obj, kaskogus, aktsiis, kulutoode, " +
	              "kulupartii, inpdoktyyp, inpdokumnr, rtaitkogus, fifoexpens, calculrow, " +
	              "laosumma, laoraha, variant, taitmata, iseteha, rtellimus, reakaal, " +
	              "paritoluri, tellreanr, statvaartu, parimenne, statiskood) " +
	     " VALUES (nextval('firma1.rid_id_seq'),nextval('firma1.rid_reanr_seq'), ?,'" 
	              + line.getTooteNimi().replaceAll("'", " ") + "' , 0, " + line.getKogus()*kastis 
	     + ", '" + toode + "', null, null, " +
	       "       null, 0, null, null, null, null, now(), " +
	        "      0, 0, 0, " + line.getDiscount() + ", null, null, null, " +
	         "     null, null, null, null, 0, 0, null, " +
	         "     null, null, null, 0, false, false, " +
	          "    0, null, false, 0, false, null, " + line.getKogus()*kastis + ", " +
	           "   null, " + count+ ", null, null,  null)";
	      
	    
	    System.out.println(invID + " " + sql);

	      PreparedStatement st = con.prepareStatement(sql);
	      st.setInt(1, invID);
	      st.executeUpdate();
	      count++;
          st.close(); 
	      setNumAddedLines(getNumAddedLines() + 1);
	    }

return result;
}

  
  private void addInvoiceHeaderold(int invID) throws SQLException{
    String sql = "INSERT INTO dok VALUES ('T',?, date(), 'LT1',?,1, 'EUR','','', date(),'1', '1', ?,?,0, .F., '','ADMIN', " +
         "'23 59', '', '1', 1,date(), 0,' ',?, .F.,'', '', '', '', '', '','','','epoc','','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),date(),.F., 0, '2000000000',0, '','',date(),'','','','','','',.F.,.F.,.F.,0,0) ";




    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(3, order.getAgent());
    stmt.setString(4, order.getCompany());
    String info = order.getInfo();
    
    if(info == null){
    	info = " ";
      } else {
    	info = info.trim();   	
    }
    
    String ref = order.getReference();
    if(ref != null && !ref.trim().equals(info)){
      info= ref + " " + info;	
    }
    stmt.setString(5, info);
System.out.println("inf " + info);
 //   stmt.setString(6, info);
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "Interaltus";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
  private ImportPanel parent;

  private String url = "jdbc:odbc:";
  private Connection con;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
