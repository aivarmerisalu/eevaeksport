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

public class MantingaViaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MantingaViaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eeva" ,"mobilejava","mobilejava");
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
    String sql1 = "SELECT kood FROM firma1.klient where  tulemus = '" + id + "'";
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

  public String identifyProduct(String ean, String prodID) throws SQLException{
	    
	  String sql1 = "SELECT toode FROM firma1.toode where ribakood=='" + ean + "'";
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


 
  public void makeOrder(Order order) throws SQLException{
	  
		  setOrder(order);
		    int invoiceID = getNextID();
		    String ladu = order.getWarehouse();
		   // ladu = getLadu(ladu, order);
		   // System.out.println("ladu ;" +  ladu);
		    addInvoiceHeader(invoiceID,ladu);
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
  
  private int getTerm(String custid) throws SQLException{
	    String sql =  "SELECT maksetin FROM firma1.klient where  kood = '" + custid + "'";


	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("maksetin");
	    }
	    return intNr;
	  }
 
  
  private String getProdName(String prodid) throws SQLException{
	    String sql =  "SELECT nimetus FROM firma1.toode where  toode = '" + prodid + "'";

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    String intNr = " ";
	    while(rs.next()){
	      intNr = rs.getString("nimetus");
	    }
	    return intNr;
	  }
 
 
  
 
  
  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }
  
  private void addInvoiceLines(int invID) throws SQLException{
	   String sql = "INSERT INTO firma1.rid VALUES(nextval('firma1.rid_id_seq'),nextval('firma1.rid_reanr_seq'), " +
       "  ?, ?, ?, ?, ?,'','22',null,0,null,null,null,null,now(), " +
             "   0,0,0,?)";
   PreparedStatement st = con.prepareStatement(sql);

   Enumeration enumr = order.getOrderLines().elements();
   while(enumr.hasMoreElements()){
     OrderLine line = (OrderLine)enumr.nextElement();
     st.setInt(1, invID);
     String toode = line.getToode();
     if (toode == null){
     toode = " ";
   }
     double kassa = 1;
     if ( order.getPaymTerm() <1){
   	    kassa = 1.20;
   	  }
     String nimi = getProdName(toode);
     if(nimi != null ){
    	 if(nimi.trim().length()>49){
    		 nimi = nimi.trim().substring(0,48); 
    	 }else{
    		 nimi = nimi.trim(); 
    	 }
         
     }else {
    	 nimi = " ";
     }

     
     st.setString(2, nimi);
     double disc = line.getDiscount();
     double price = line.getPrice();
     if(disc>0){
   	 price = price *(1 - disc/100);  
     }
     st.setDouble(3, price * kassa);
     st.setDouble(4, line.getKogus());
       st.setString(5, toode);
       System.out.println("toode " + toode + "  " + line.getKogus());
     st.setDouble(6,disc ); //ostuale v�lja paneme allahindluse v��rtuse

     st.executeUpdate();
     setNumAddedLines(getNumAddedLines() + 1);
   }
   st.close();
  }

  
  
  
  
  private void addInvoiceHeader(int invID,String ladu) throws SQLException{
     
        int paymterm = 0;
	   String doktyyp = "'G'";
	   if ( order.getPaymTerm() >0){
	    paymterm = order.getPaymTerm();
	  }else{
		 
	  }
	   paymterm = getTerm(order.getCompany());
	   
	  String konto = "1311EUR";

	  String doknr =  order.getDokNr();
String custordnum = order.getCompName();
if(custordnum == null || custordnum.trim().length() == 0)
	custordnum = "";
	    String sql =   " insert into firma1.dok values" +
	   " (" + doktyyp +",?,?,'ASL',(select  case when maksja is null then kood else maksja end  from firma1.klient where kood='" + 
	   order.getCompany()+"'),1,'EUR','" + doknr + "'," +
	   " '" + konto +"',NOW() + interval '" + paymterm + " day','NORD',null,?,'" + order.getCompany() + "', null,false,'ADMIN','ADMIN','23 59','1121EUR'," +
	   " NULL,NULL ,null,NULL ,'" + order.getInfo() + "','',false,'" + order.getCompName() + "',null,null,null,null,null,null,null,'',''," +
	   " 'MJDOK-" + Calendar.getInstance().getTimeInMillis() + "') " ;
	   //, " +
	   //" ,false, ,NULL ,NULL ,'','',false,'','',false,0.000,'20090310142835', NULL,'','','','','','') " ;

	  //  select  case when maksja is null then kood else maksja end as asi,kood from firma1.klient where nimi like '%MAX%'

	    log.info("addInvoiceHeader2: " + sql);
	    PreparedStatement stmt = con.prepareStatement(sql);


	    stmt.setInt(1, invID);
	    stmt.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
	   // stmt.setString(3, order.getCompany());
	    
	    //stmt.setString(3, String.valueOf(invID));
	    stmt.setString(3, "VIKTO");
	    //stmt.setString(5, order.getCompany());
	    String info = order.getInfo();

	    if(info == null){
	    	info = " ";
	    }
	  //  stmt.setString(5, info);

	 //   stmt.setString(6, info);
	    stmt.executeUpdate();
	    stmt.close();
  }

  public String getDriverVersion(){
    return "MantingaVia";
  }

  private void setOrder(Order order){
    this.order = order;
  }

 

  private String url = "jdbc:odbc:";
  private Connection con;
  private ImportPanel parent;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
