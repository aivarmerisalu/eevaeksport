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
import eevaeksport.imp.POP3Reader;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class DeaNewImportDriver extends AbstractImportDriver{

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public DeaNewImportDriver(ImportPanel parent) {
 
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eeva");

      POP3Reader pop3 = new POP3Reader(parent.getEksportFrame().getString("moborder"));
      pop3.setPop3host(parent.getProps().getProperty("pop3.host"));
      pop3.setUser(parent.getProps().getProperty("pop3.user"));
      pop3.setPassword(parent.getProps().getProperty("pop3.password"));
      pop3.setSaveDir(getImportFile());
      pop3.readFiles(true);


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

  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    int invoiceID = getNextID();
    addInvoiceHeader(invoiceID);
    addInvoiceLines(invoiceID);
  }

  private int getNextID() throws SQLException{
    String sql = "SELECT MAX(dokumnr) AS doknr FROM dok";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    int intNr = -1;
    while(rs.next()){
      intNr = rs.getInt("doknr");
      intNr++;
    }

    return intNr;
  }

  private void addInvoiceLines(int invID) throws SQLException{


    String sql = "INSERT INTO rid VALUES('G',?, '', 0, ?, ?,'','K','',0,'','','','',date()," +
                "0,?,?,0,'','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',0,0,.F.,0,'',0)";
    PreparedStatement st = con.prepareStatement(sql);

    Enumeration enumr = order.getOrderLines().elements();
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
      st.setInt(1, invID);
      st.setDouble(2, line.getKogus());
      st.setString(3, line.getToode());
      double disc = line.getDiscount();
      if (disc == -1) disc = 0;
      st.setDouble(4, disc);
       double bonus = 0 ;
      if (line.getBonus() != -1){
          bonus = line.getBonus() ;
              }
      st.setDouble(5, bonus);

      st.executeUpdate();
      setNumAddedLines(getNumAddedLines() + 1);
    }
    st.close();
  }

  private void addInvoiceHeader(int invID) throws SQLException{
      String sql = "INSERT INTO dok VALUES ('G',?, date(), 'ARV',?,1, 'EUR',?," +
                 "'', date(),?, '1', ?,?,0, .F., 'ADMIN', " +
        "'23 59', '', '1', 1,date(), 0,?,'', .T., '', '', '', '', '','','','epoc','','', 0, .F.,0, '', '', .F., " +
        "date(),date(),.F., 0, '2000000000',0, '','','','','','') ";


    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setDate(2,new java.sql.Date(order.getShipCal().getTime().getTime()));
    stmt.setString(3, order.getCompany());
    stmt.setString(4, String.valueOf(invID));
    stmt.setString(5, order.getDealer());
    stmt.setString(6, order.getAgent());
    stmt.setString(7, order.getCompany());
    stmt.setString(8, order.getEmailBody());
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "DeaNew";
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