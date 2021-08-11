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

public class AvalloneViaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AvalloneViaImportDriver(ImportPanel parent) {
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
    String sql1 = "SELECT kood FROM klient where  raamat = '" + id + "'";
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
    String sql1 = "SELECT toode FROM toode where toode='" + prodID + "'";

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
		    invoiceID =  checkForID (invoiceID);
		    String ladu = order.getWarehouse();
		    ladu = getLadu(ladu, order);
		    System.out.println("ladu ;" +  ladu);
		    addInvoiceHeader(invoiceID,ladu);
		    addInvoiceLines(invoiceID);
   
  }

  
  
  private String getLadu(String ladu ,Order ord ) throws SQLException {
	  	String sql = "select parool from klient where kood='" + ord.getCompany() + "'";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    if(rs.next()){
	    	ladu = rs.getString("parool").trim();
	    	if(ladu.equals("2")){
	    		return "2";
	    	} else if (ladu.equals("3")){
	    		return "3";
	    	} else {
	    	
	    		return "1";
	    	}
	    }
	    
	  	st.close();
	  	
	  	return ladu;
	  }
	  
  
  private String getAgent(Order ord ) throws SQLException {
	  	String sql = "select objekt2 from klient where kood='" + ord.getCompany() + "'";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    String agent = "";
	    if(rs.next()){
	    	agent = rs.getString("objekt2").trim();
	    
	    }
	    
	  	st.close();
	  	
	  	return agent;
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
  
  public String getImportFile(){
	   return parent.getProps().getProperty("imp.read");
	 }
  
  private void addInvoiceLines(int invID) throws SQLException{
    String sql = " INSERT INTO rid VALUES('G',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
                 "0,0,0,0,'','','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0,0,'')";
    PreparedStatement st = con.prepareStatement(sql);

    Enumeration enumr = order.getOrderLines().elements();
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
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

  private void addInvoiceHeader(int invID,String ladu) throws SQLException{
   
	  
	  
	  
	  
      int paevi = 0;
      String sql1 = "Select t.maksetin ,tahtaeg1 from klient k inner join " +
      		" ../andmed/maksetin  t on t.maksetin=k.maksetin where k.kood ='"+order.getCompany()+"'";

          Statement st1 = con.createStatement();
          ResultSet rs1 = st1.executeQuery(sql1);

          while (rs1.next()) {
            paevi = rs1.getInt("tahtaeg1");
          }
          rs1.close();
          st1.close();
          int tingimus = 0;
          if(order.getPaymTerm()!=0){
            tingimus = order.getPaymTerm();
          }else{
          	tingimus = paevi;
          }
	  
	  
	  String sql = "INSERT INTO dok VALUES ('G',?, ?, 'ASL',?,1, 'EUR','"+ order.getDokNr() +"','13111', date() + " + tingimus + ",'"+ ladu +"', '1', ?,?,0, .T., 'ADMIN','ADMIN', " +
         "'23 59', '', '1', 1,date(), 0,'','', .F., '','', '', '', '', '','','','epoc','','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),date(),.F., 0, '2000000000',0, '" + order.getInfo().replaceAll("'", " ").trim()+ "','','','','','','',0)  ";
  


    log.info("addInvoiceHeader2: " + sql);
    System.out.println(sql +  " \n" + order.getDokNr() + " info:" + order.getInfo() );
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setDate(2, new java.sql.Date(order.getShipCal().getTimeInMillis()));
    stmt.setString(3, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(4, getAgent(order));
    stmt.setString(5, order.getCompany());
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "AvalloneVia";
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
