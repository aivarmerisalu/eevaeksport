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

public class EevaMGKassaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public EevaMGKassaImportDriver(ImportPanel parent) {
    PropertyConfigurator.configure("log4j.properties");
    this.parent = parent ;
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
  	    sql1 = "SELECT kood FROM klient where   fil= '" + id + "'";	
  	}else{

  	     sql1 = "SELECT kood FROM klient where  (regnr)= '" + regnr + "' AND fil= '" + id + "'";	
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

   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM toode where left(ribak,13)='" + ean + "'";


    String sql2 = "SELECT toode FROM toode where toode='" + prodID + "'";

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
  
  
  public String getNimi( String prodID) throws SQLException{

	    String sql2 = "SELECT nimetus FROM toode where toode='" + prodID + "'";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql2);


	      rs1 = st1.executeQuery(sql2);

	      while (rs1.next()) {
	        prod = rs1.getString("nimetus").trim();
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
    addInvoiceHeader(invoiceID,ladu);
    addInvoiceLines(invoiceID);
  }

  private int getNextID() throws SQLException{
   
	    String sql2 = "update alusdok set seeralgus=seeralgus + 1 where alusdok='LG'";
	    Statement st1 = con.createStatement();
	    st1.executeUpdate(sql2);
	    st1.close();
	    
	  
	  String sql = "select seeralgus from alusdok where alusdok='LG'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    int intNr = -1;
    while(rs.next()){
      intNr = rs.getInt("seeralgus");
      
    }
    st.close();


    return intNr-1;
  }

  private void addInvoiceLines(int invID) throws SQLException{
    String sql = "INSERT INTO rid VALUES('G',?, ?, ?, ?, ?,'','2007','',0,'','','','',date(), " +
                 "0,0,0,0,'','','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0)";
    PreparedStatement st = con.prepareStatement(sql);

    Enumeration enumr = order.getOrderLines().elements();
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
      st.setInt(1, invID);
      String toode = line.getToode();
      if (toode == null){
      toode = " ";
    }
      String nimi = getNimi(toode);
     
      st.setString(2, nimi);
      st.setDouble(3, line.getPrice());
      st.setDouble(4, line.getKogus());
      st.setString(5, toode);

      st.executeUpdate();
      setNumAddedLines(getNumAddedLines() + 1);
    }
    st.close();
  }

  private void addInvoiceHeader(int invID , String ladu ) throws SQLException{
	  if(ladu== null || ladu.trim().length() == 0){
		  ladu = "N�IDIS";
	  }
	  String ting = "SK";
	  if(-1 > order.getPaymTerm()){
		  ting = "SD";
	  }
	  
    String sql = "INSERT INTO dok VALUES ('G',?, date(), 'ASL',?,1, 'EUR','','13111', date(),'"+ladu+"', '1', ?,?,0, .F., '','ADMIN', " +
         "'23 59', '', '" + ting + "', 1,date(), 0,?,' ', .F.,'', '', '', '', '', '','','','mobilejava','','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),date(),.F., 0, '2000000000',0, '','','','','','',0) ";

	

    


    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(3, "MARIKA");
    stmt.setString(4, order.getCompany());
    String info = order.getInfo();
    
    if(info == null){
    	info = " ";
    }
    stmt.setString(5, info);

 //   stmt.setString(6, info);
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "EevaMGKassa";
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
