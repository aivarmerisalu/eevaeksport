
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

public class ElfaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public ElfaImportDriver(ImportPanel parent) {
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

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	if(regnr==  null){
  	    sql1 = "SELECT kood FROM klient where   nimi like '" + id + "'";	
  	}else{

  	     sql1 = "SELECT kood FROM klient where  (regnr)= '" + regnr + "' AND fil= '" + id + "'";	
  	}
  
  	if(id.length() >20){
  		id = id.substring(0,20);
  	}
  	
  	String  sql2 = "SELECT kood FROM klient where  (parool) = '" + id + "' ";	
    System.out.println(sql1);
    System.out.println(sql2);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    
    if(acc == null){
    	rs1 = st1.executeQuery(sql2);

        while (rs1.next()) {
          acc = rs1.getString("kood").trim();

        }
    }
    
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }


  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM ../andmed/toode where ribakood='" + ean + "'";


    String sql2 = "SELECT toode FROM ../andmed/toode where toode='" + prodID + "'";

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

  
  private int getNextNum() throws SQLException{
    String sql = "select jargdok from alusdok where alusdok='LT'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    int intNr = -1;
    while(rs.next()){
      intNr = rs.getInt("jargdok");
      intNr++;
    }
    String sql2 = "update alusdok set jargdok=" + intNr + "where alusdok='LT'";
    Statement st1 = con.createStatement();
    st1.executeUpdate(sql2);
    st1.close();
    st.close();


    return intNr;
  }

  
  private void addInvoiceLines(int invID) throws SQLException{
    String sql = "INSERT INTO rid VALUES('T',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
                 "0,0,0,0,'','','','','','','',0,0,'','','','',0,.F.,.F.,0,0,.F.,'',0,'','',0,'',.F.,'',0,0,0,0)";
    PreparedStatement st = con.prepareStatement(sql);

    Enumeration enums = order.getOrderLines().elements();
    while(enums.hasMoreElements()){
      OrderLine line = (OrderLine)enums.nextElement();
      st.setInt(1, invID);
      String toode = line.getToode();
      if (toode == null){
      toode = " ";
    }
      st.setString(5, toode);
      st.setString(2, line.getTooteNimi());
      st.setDouble(4, line.getKogus());
      st.setDouble(3, line.getPrice());

      st.executeUpdate();
      setNumAddedLines(getNumAddedLines() + 1);
    }
    st.close();
  }

  private void addInvoiceHeader(int invID) throws SQLException{
  	String doknr = null ;
  	
  	if(doknr == null){
  		doknr = this.getNextNum()+"";
  	}else if (doknr.trim().length()<2){
  		doknr = this.getNextNum()+"";
  	}

    String info = order.getReference();
    System.out.println("fff"+info);
    if(info == null){
    	info = " ";
    }
    
  	String orignum  = order.getDokNr();
  	if(orignum == null){
  		orignum = " ";
  	}
  	
    String sql = "INSERT INTO dok VALUES ('T',?, date(), 'LT1',?,1, 'EUR','" + doknr + "','', date(),'EFD', '', ?,?,0, .F., '','ADMIN', " +
         "'23 59', '112', '', 0,date(),?,' ', .F.,'', '', '', '', '', '','','','epoc','','', 0, .F.,0,0,0, '', '', .F., " +
       " date(),date(),.F., 0, '2000000000',0,'','',date(), '','','','','',0,'" + info + "','',.F.,0,'') ";

    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(3, "KAIRI");
    stmt.setString(4, order.getCompany());

    if( order.getCompany() != null && order.getCompany().equals("WEB")){
    	stmt.setString(5,  info);
    }else{
        stmt.setString(5, "MJ " + info);	
    }

 //   stmt.setString(6, info);
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "Elfa";
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
