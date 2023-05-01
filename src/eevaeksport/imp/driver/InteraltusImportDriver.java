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

import eevaeksport.Agent;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class InteraltusImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(InteraltusImportDriver.class.getName());

  public InteraltusImportDriver(ImportPanel parent) {
	  this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eeva");
 
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
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
    String sql3 = "SELECT toode FROM firma1.toode where toode='" + ean + "'";

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
    
    if(prod == null){

        rs1 = st1.executeQuery(sql3);

        while (rs1.next()) {
          prod = rs1.getString("toode").trim();
        }
      }

    //System.out.println("prod1" + prod);
    rs1.close();
    st1.close();

    return prod;
  }


  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    int invoiceID = getNextID();
  
   
    boolean hasinter = hasInter(order);
   if(hasinter){
	   
	   
	   String comment = order.getInfo();
	      String comm2 = comment;
	          if(comment == null || comment.trim().length() == 0 ){
	         	comment = "";
	         	comm2= "";
	          }
	          
	         if(order.isShipDateUsed()) {
	        	 comm2 = comm2 + "<br> Tarnekuupäev valitud : " + Converter.getDDpMMpYYYYpString(order.getShipCal());
	         }
	          
	   addInvoiceHeader(invoiceID, order);
	   addInvoiceLines(invoiceID);  
	   
	   if((comm2 != null) && order.getStatus() < 5){
		   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(order.getAgent());
	    Converter.sendCommentEmail( comm2,null,order.getDokNr(),order.getCompany(),  " " + getCustName(order.getCompany()) , " ",false,ag,parent,null);
	 }
   }
   
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
  
  private String getKiiliKlient(String custid) throws SQLException{
	    String sql = "select infomail as klient from firma1.klient where kood ='" + custid + "'";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    
	    if(rs.next()){
	     return rs.getString("klient");	     
	    }

	    return null;
	  }
  
  private String getCustName(String custid) throws SQLException{
	    String sql = "select nimi  from firma1.klient where kood ='" + custid + "'";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    
	    if(rs.next()){
	     return rs.getString("nimi");	     
	    }

	    return null;
	  }
  
  
  
  private double getPrice(String prodid, double disc) throws SQLException{
	  String cmd = "SELECT myygihind FROM firma1.toode WHERE " +
            "   trim(toode) = '" + prodid+"'  ORDER BY toode";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(cmd);
	    
	    if(rs.next()){
	     return rs.getDouble("myygihind") * (100-disc)/100;	     
	    }

	    return 0;
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
	      if(line.getDiscount() > 0){
	    	  price = getPrice(toode, line.getDiscount());
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
	              + line.getTooteNimi().replaceAll("'", " ") + "' , " + price +", " + line.getKogus()*kastis 
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
	 
	      if(toode != null && toode.trim().length() > 0 && !toode.startsWith("KI-")){
	    	  st.executeUpdate();
	    	  result = true;
	      }
	      
	      count++;

	      st.close(); 
	      setNumAddedLines(getNumAddedLines() + 1);
	    }

    
  return result;
  }
	  

  private boolean hasInter(Order order) throws SQLException{
	    
	  String kiiliklient = getKiiliKlient(order.getCompany());
	     boolean result = false;
	  StringBuffer sb = new StringBuffer(); 
	    Enumeration enumr = order.getOrderLines().elements();
	
	    while(enumr.hasMoreElements()){
	      OrderLine line = (OrderLine)enumr.nextElement();
	      String toode = line.getToode();
	      if (toode == null){
	      toode = " ";
	    }

	      if(!toode.startsWith("KI-")){
	    	  result = true;
	      }else{
	          if(kiiliklient!= null){
	        		sb.append(kiiliklient.trim()+ ";" + line.getToode().substring(3) +  ";"  + line.getKogus()  + ";0; ;0;" + line.getDiscount()+ ";0; ;" + line.getPrice() +";0; ;0; ; ; \n");
	        		System.out.println("KiiliKlient " + kiiliklient + " toode " + line.getKogus()+  " " + line.getToode().substring(3)  );
	          }	  
	       }
	    }


	    if(sb.length() > 2){
	    	String uniq = (Calendar.getInstance().getTimeInMillis() + "").substring(5);
	        File f = new File(getDir() + "31." + uniq + ".txt");
	        if (f.exists())f.delete();
	          try {
	    		PrintWriter os = new PrintWriter(new FileWriter(
	    				getDir() + "31." + uniq + ".txt", true));
	    		os.write(sb.toString());
	    		os.close();
	    	  }catch (IOException ex) {
	    		ex.printStackTrace();
	    	  }
	     }
    
   return result;
  }
	  



  private String getDir(){
	    String dir = parent.getProps().getProperty("output.dir");
	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	    return dir;
	  }
  
  
  


  private void addInvoiceHeader(int invID, Order orde) throws SQLException{
	  String ladu = "1";

	  try
	  {
	      Thread.sleep(11);
	  }
	  catch(InterruptedException ex)
	  {
	      Thread.currentThread().interrupt();
	  }
	    
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
           " now(), ?, '', false, null, null, null, " +
           " null, null, null, null, null, 'mobil', null, " +
           " '" + Calendar.getInstance().getTimeInMillis() +""+ order.getOrderLines().size() + "', 0, false, 0, 0, 0, '', " +
           " null, false, now(), ?, false, 0, '20151027111600', " +
           " null, '" + ordernumber + "', null, now(), null, null, " +
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
    stmt.setTimestamp(6,new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
   // stmt.setString(7, ordernumber);

    stmt.executeUpdate();
    stmt.close();
  }

 

  public String getDriverVersion(){
    return "Interaltus";
  }

  private void setOrder(Order order){
    this.order = order;
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
