package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.Date;
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

public class AvalloneKassaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AvalloneKassaImportDriver(ImportPanel parent) {
	  this.parent = parent ;
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

  public String identifyCustomer(String id,String regnr) throws SQLException {
    String sql1 = "SELECT kood FROM klient where  (regnr)= '" + id + "' OR fil= '" + id + "'";
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();

    if(id.indexOf("ogistikakeskus") != -1){
    	acc = "KESKOEESTIAS";
    }
    
   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM toode where ribakood='" + ean + "' AND (ribakood!='' OR ribakood!=' ')";

    String prod = null;

    
    System.out.println(sql1);
    
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
    
    if(prod == null && prodID != null && prodID.trim().length()>0){
    	 sql1 = "SELECT toode FROM toode where " +
        "  kaubakood='" + prodID + "' OR toode='" + prodID + "'";
    	
    	 rs1 = st1.executeQuery(sql1);

    	    while (rs1.next()) {
    	      prod = rs1.getString("toode").trim();
    	    }
    }
    
    
    rs1.close();
    st1.close();

    return prod;
  }

  public double getKastis(String prodID) throws SQLException{
	    String sql1 = "SELECT kastis FROM toode where toode='" + prodID + "'";

	    double kastis = 1;
  System.out.println(sql1);
	    	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	   if (rs1.next()) {
	      kastis = rs1.getDouble("kastis");
	    }
	    
	 
	    
	    rs1.close();
	    st1.close();

	    return kastis;
	  }

 
  
  public void makeOrder(Order order) throws SQLException{
	  
	  

		  
		  setOrder(order);
		    int invoiceID = getNextID();
		    invoiceID =  checkForID (invoiceID);
		   
		    addInvoiceHeader(invoiceID,"1");
		    addInvoiceLines(invoiceID);
	
  }

  
  
  private int checkForID(int iii) throws SQLException {
  	String sql = "select dokumnr from dok where doktyyp='G' and dokumnr= "+iii;
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    if(rs.next()){
    	iii = getNextID();
    }
    
  	st.close();
  	
  	return iii;
  }
  
  private int getNextID() throws SQLException{
    String sql = "select seeralgus from alusdok where alusdok='LG'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    int intNr = -1;
    while(rs.next()){
      intNr = rs.getInt("seeralgus");
      intNr++;
    }
    String sql2 = "update alusdok set seeralgus=" + intNr + "where alusdok='LG'";
    Statement st1 = con.createStatement();
    st1.executeUpdate(sql2);
    st1.close();
    st.close();


    return intNr;
  }
  
  private int getNextNum() throws SQLException{
	    String sql = "select jargdok from alusdok where alusdok='LG'";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("jargdok");
	      intNr++;
	    }
	    String sql2 = "update alusdok set jargdok=" + intNr + "where alusdok='LG'";
	    Statement st1 = con.createStatement();
	    st1.executeUpdate(sql2);
	    st1.close();
	    st.close();


	    return intNr;
	  }

  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }
 
  private void addInvoiceLines(int invID) throws SQLException{
    String sql = " INSERT INTO rid VALUES('G',?, ?, 0, ?, ?,'','','',0,'','','','',date()," +
                 "0,0,0,?,'','','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0,0,'')";
    PreparedStatement st = con.prepareStatement(sql);

    Enumeration enumr = order.getOrderLines().elements();
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
      st.setInt(1, invID);
      String toode = line.getToode();
      if (toode == null){
      toode = " ";
    }
      double kastis = 1;
      if(line.getUnit().equals("DPA")){
    	  kastis = getKastis(line.getToode());
      }
      st.setString(4, toode);
      st.setString(2, line.getTooteNimi());
      st.setDouble(3, line.getKogus()*kastis);
     
      st.setDouble(5, line.getDiscount());
    	 st.executeUpdate();	
      setNumAddedLines(getNumAddedLines() + 1);
    }
    st.close();
  }

  private void addInvoiceHeader(int invID,String ladu) throws SQLException{
	  String ordernumber = order.getDokNr();
	  if(ordernumber == null){
		  ordernumber = order.getInfo();
	  }
	  System.out.println("oo "+order.getInfo() + " " + order.getReference());
	  
	  if(ordernumber == null || ordernumber.length() < 5){
		  ordernumber = getNextNum() + "";
	  }
	  java.sql.Date dd = new Date(Calendar.getInstance().getTimeInMillis());
	  if(order.getShipCal() != null){
		  dd = new Date(order.getShipCal().getTimeInMillis());
	  }
	  
	  
    String sql = "INSERT INTO dok VALUES ('G',?, date(), 'ASL',?,1, 'EUR','" + ordernumber + "','', date(),'"+ ladu +"', '1', ?,?,0, .F., 'ADMIN','ADMIN', " +
         "'23 59', '', '1', 1,date(), 0,'Mobiilne Tellimus','', .F., '','', '', '', '', '','','','epoc','','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),?,.T., 0, '2000000000',0, '','" + ordernumber + "','','','','','',0)  ";



    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(3, order.getAgent());
    stmt.setString(4, order.getCompany());
    stmt.setDate(5, dd);
    stmt.executeUpdate();
    
    stmt.close();
  }

  public String getDriverVersion(){
    return "AvalloneKassa";
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
