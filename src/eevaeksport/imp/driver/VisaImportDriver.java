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

public class VisaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(VisaImportDriver.class.getName());

  public VisaImportDriver(ImportPanel parent) {
    PropertyConfigurator.configure("log4j.properties");
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
    String sql1 = "SELECT kood FROM klient where kood='" + id + "' OR str(regnr)= '" + id + "' OR fil= '" + id + "'";
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
    String sql1 = "SELECT toode FROM toode where (ribakood='" + ean + "' AND (ribakood!='' OR ribakood!=' '))" +
        " OR kaubakood='" + prodID + "' OR toode='" + prodID + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
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
    String sql = "select seeralgus from alusdok where alusdok='LT'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    int intNr = -1;
    while(rs.next()){
      intNr = rs.getInt("seeralgus");
      intNr++;
    }
    String sql2 = "update alusdok set seeralgus=" + intNr + "where alusdok='LT'";
    Statement st1 = con.createStatement();
    st1.executeUpdate(sql2);
    st1.close();
    st.close();


    return intNr;
  }

  private void addInvoiceLines(int invID) throws SQLException{
    String sql = "INSERT INTO rid VALUES('T',?, '', 0, ?, ?,'','','',0,'','','','',date()," +
                 "0,0,0,0,'','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',0,0,.F.,0,'',0)";
    PreparedStatement st = con.prepareStatement(sql);

    Enumeration enumr = order.getOrderLines().elements();
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
      st.setInt(1, invID);
      st.setDouble(2, line.getKogus());
      st.setString(3, line.getToode());
      st.executeUpdate();
      setNumAddedLines(getNumAddedLines() + 1);
    }
    st.close();
  }

  private void addInvoiceHeader(int invID) throws SQLException{
    String sql = "INSERT INTO dok VALUES ('T',?, date(), 'LT1',?,1, 'EUR','" + order.getDokNr()+ "','', date(),'1', '1', ?,?,0, .F., 'ADMIN', " +
         "'23 59', '', '1', 1,date(), 0,'MJ tellimus','', .F., '', '', '', '', '','','','','','', 0, .F.,0, '', '', .F., " +
        "date(),date(),.F., 0, '2000000000',0, '','','','','') ";




    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(3, order.getAgent());
    stmt.setString(4, order.getCompany());
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "Visa";
  }

  private void setOrder(Order order){
    this.order = order;
  }

  private String url = "jdbc:odbc:";
  private Connection con;
  private Order order = null;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}