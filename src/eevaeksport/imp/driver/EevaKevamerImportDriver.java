package eevaeksport.imp.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

import eevaeksport.Customer;
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

public class EevaKevamerImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(EevaKevamerImportDriver.class.getName());

  public EevaKevamerImportDriver(ImportPanel parent) {
	  this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    //String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
     // Class.forName(databaseDriverName).newInstance();
     
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
    return id;
  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
      return prodID;
  }


  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    int invoiceID = getNextID();
      addInvoiceHeader(invoiceID);
	   addInvoiceLines(invoiceID);   
   
  }

private int getNextID() throws SQLException{
 
	    String sql =  " select nextval('firma34.dok_dokumnr_seq') as seeralgus";
	    Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("seeralgus");
	    }
	    rs.close();
	    st.close();
	    con.close();
	    return intNr;
}
  
  private double getPrice (String prod ) {
	  double res = 0;
	  String cmd = "SELECT toode, nimetus, tykke, myygihind, yhik  ,liik" +
        		",ribakood ,seisund  FROM firma34.toode WHERE " +
            "  length(trim(toode)) >2 and length(trim(nimetus)) >1 and grupp ='L' and  toode = '"+prod+"'";
try{
	

	Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");

	 Statement stmt = con.createStatement();
                  ResultSet  rs = stmt.executeQuery(cmd);

                    if (rs.next()) {
                    	return rs.getDouble("myygihind");
                    }
                       rs.close();
                       stmt.close();
                       con.close();
                        
                    }catch(Exception r) {
                     r.printStackTrace();	
                    }


        return res ;
  }

  private boolean addInvoiceLines(int invID) throws SQLException{
	    
	     boolean result = false;
	  
	    Enumeration enumr = order.getOrderLines().elements();
	    int count = 1;
	    while(enumr.hasMoreElements()){
	      
	    	OrderLine line = (OrderLine)enumr.nextElement();
	      String toode = line.getToode();
	  
	      double kastis = 1;
	      if(line.getUnit().equals("DPA")){
	    	  kastis = line.getBonus();
	      }
	      if(order.getStatus() == 1){
	    	kastis = kastis * -1;  
	      }
	      double price  = 0;
	     
	      double prhinnak = getPrice(toode);
	      double disc = line.getDiscount();
	      if( disc !=  0 ){
	    	  price = prhinnak * (100-line.getDiscount())/100;
	      }
	      
	      if(disc == -2 && prhinnak == line.getPrice()) {
	    	  price = 0;
	    	  disc = 100;
	      }
	      
	     
	      
	      String sql = "INSERT INTO firma34.rid( " +
	              "id, reanr, dokumnr, nimetus, hind, kogus, toode, partii, myygikood, " +
	              "hinnak, kaubasumma, yhik, kulukonto, kuluobjekt, rid2obj, reakuupaev, " +
	              "kogpak, kulum, baasostu, ostuale, rid3obj, rid4obj, rid5obj, " +
	              "rid6obj, rid7obj, rid8obj, rid9obj, kaskogus, aktsiis, kulutoode, " +
	              "kulupartii, inpdoktyyp, inpdokumnr, rtaitkogus, fifoexpens, calculrow, " +
	              "laosumma, laoraha, variant, taitmata, iseteha, rtellimus, reakaal, " +
	              "paritoluri, tellreanr, statvaartu, statiskood) " +
	     " VALUES (nextval('firma34.rid_id_seq'),nextval('firma34.rid_reanr_seq'), ?,'" 
	              + line.getTooteNimi().replaceAll("'", " ") + "' , " + price +", " + line.getKogus()*kastis 
	     + ", '" + toode + "', null, null, " +
	       "       null, 0, null, null, null, null, now(), " +
	        "      0, 0, 0, " + disc + ", null, null, null, " +
	         "     null, null, null, null, 0, 0, null, " +
	         "     null, null, null, 0, false, false, " +
	          "    0, null, false, 0, false, null, " + line.getKogus()*kastis + ", " +
	           "   null, " + count+ ", null,   null)";
	      
	    
	    System.out.println(invID + " " + sql);
	    Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
	      PreparedStatement st = con.prepareStatement(sql);
	      st.setInt(1, invID);
	  	  st.executeUpdate();
	    	  result = true;
	     
	      
	      count++;

	      st.close(); 
	      con.close();
	      setNumAddedLines(getNumAddedLines() + 1);
	    }

    
  return result;
  }
	  

  private Customer getPerson(String ID) {
	  Customer cust = null;
	  String  cmd = "SELECT ko.nimi, ko.klient, ko.telefon, ko.email, ko.guid FROM firma34.kaardika ko where timestamp = '" + ID + "'";
	  try {
		  Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
		    Statement st = con.createStatement();
		    ResultSet rs = st.executeQuery(cmd);
		 
		    while(rs.next()){

		    	
		    	   String name =  parent.make(rs.getString("nimi"));
		              String klient = parent.make(rs.getString("klient"));
		              String phone = parent.make(rs.getString("telefon"));
		              String mail =  parent.make(rs.getString("email"));
		              String idd = parent.make(rs.getString("guid"));
		              cust = new Customer(idd);
		              cust.setName(name);
		              cust.setPersonalData(phone);
		              cust.setInfoEmail(mail);
		    	//   " null, null, null, 0, 0 ,'" + contact.getName() + "','" + contact.getPersonalData() + "'" +
		    	  //         ",'" + contact.getInfoEmail() + "'" + ",'" + contact.restStreet + "'" + ",'" + contact.restCity + "'" +
		    	        
		    	
		    	
		    }
		    rs.close();
		    st.close();
		    con.close(); 
	  }catch(Exception e) {
		  e.printStackTrace();
	  }
	  
	  return cust ;
  }
	  
  private String getPayer(String ID) {
	  String res = null;
	  String  cmd = "SELECT maksja  FROM firma34.klient ko where kood = '" + ID + "'";
	  try {
		  Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
		    Statement st = con.createStatement();
		    ResultSet rs = st.executeQuery(cmd);
		 
		    while(rs.next()){

		    	   res = (rs.getString("maksja").trim());
		      
		    }
		    rs.close();
		    st.close();
		    con.close(); 
	  }catch(Exception e) {
		 // e.printStackTrace();
	  }
	  
	  return res ;
  }
  
  
 


  private void addInvoiceHeader(int invID) throws SQLException{
	  String ladu = "1";

	  Customer contact = null;
		if( order.getContactID() != null && order.getContactID().trim().length() > 0 ){
			String cc = order.getContactID().trim();
			if(cc.indexOf(":") != -1) {
				cc = cc.substring(0,cc.indexOf(":"));
			}
			contact= getPerson(cc) ;
		}
		
	    
	  String agent = order.getAgent();
	  String ordernumber = null;
	    if(order.getStatus() > 10){
	    	ordernumber = order.getDokNr();
	    }
	  
	   String cname = "";
	   String ckood = null;
	   String cpers = "";
	   String cmail = "";
	   if(contact != null) {
		   cname =  contact.getName();
		   cpers =   contact.getPersonalData();
		   cmail =   contact.getInfoEmail();
		   ckood =  "'" + contact.getID() + "'";
		   
	   }
	  Calendar cal = Calendar.getInstance();
	  
	  String hour = cal.get(Calendar.HOUR_OF_DAY)+"";
	  if (hour.length() == 1) {
		  hour = "0"+ hour;
	  }
	  String minu = cal.get(Calendar.MINUTE)+"";
	  if (minu.length() == 1) {
		  minu = "0"+ minu;
	  }
	   String time = hour + " " + minu;
	   
	    
	  String sql = "INSERT INTO firma34.dok( doktyyp, dokumnr, kuupaev, oper, klient, laonr, raha, tasudok, " +
           " knr, tasukuup, yksus, sihtyksus, pais2obj, saaja, krdokumnr, " +
           " eimuuda, kasutaja, username, kellaaeg, arvekonto, maksetin, exchrate, " +
          " ratefound, tekst1, viitenr, objrealt, arvenumber, pais3obj, pais4obj, " +
           " pais5obj, pais6obj, pais7obj, pais8obj, pais9obj, masin, tegmasin, " +
           " guid, doksumma, kinnitatud, tasumata, sularaha, kaardimaks, kalkliik, " +
           " kalktoode, inventuur, algus, lopp, taidetud, kaal, \"timestamp\", " +
           " vmnr, tellimus, volitaisik, postitatud, liikmesrii, tehingulii, " +
           " tarneklaus, statprots, deliverypa, kurss, eekexchara, customer ,phonenum , emailaddre" +
           " ) VALUES (" +
           " 'T', ?, now(), 'LT1', ?, 1, 'EUR', '" + invID + "', " +
           " '1311', now(), '"+ladu+"', null, ?, ?, null, " +
           " false, 'RIINAPUKK','RIINAPUKK', '"+time+"', '1112', null, 1 ," +
           " now(), ?, '', false, null, null, null, " +
           " null, null, null, null, null, 'mobil', null, " +
           " '" + Calendar.getInstance().getTimeInMillis() + "', 0, true, 0, 0, 0, '', " +
           " null, false, now(), ?, false, 0, '20151027111600', " +
           " null, '" + ordernumber + "', " + ckood + ", now(), null, null, " +
           " null, null, null, 0, 0 ,'" + cname + "','" + cpers + "'" +
           ",'" + cmail + "'"  +
           " )" ;
	  

   
	  System.out.println("oo "+sql);
    log.info("addInvoiceHeader2: " + sql);
    Connection con = DriverManager.getConnection(url,"odbcmerisa","pMerisiil49");
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    String payer = getPayer(order.getCompany());
    if(payer == null) {
    	payer = order.getCompany();
    }
    stmt.setString(2, payer);
    stmt.setString(3, agent);
    stmt.setString(4, order.getCompany());
    String info = order.getInfo();
    if(info == null){
    	info = " ";
    }
    stmt.setString(5, info);
    stmt.setTimestamp(6,new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
    stmt.executeUpdate();
    stmt.close();
    con.close();
  }

  public String getDriverVersion(){
    return "EevaKevamer";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private ImportPanel parent; 
  private String url = "jdbc:postgresql://erp.eetasoft.ee/sba";
 // private String	  url = "jdbc:odbc:PostgreKEV";
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
