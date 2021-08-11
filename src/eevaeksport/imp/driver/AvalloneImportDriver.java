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
import java.util.Vector;

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

public class AvalloneImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AvalloneImportDriver(ImportPanel parent) {
  
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
    
    if(prod == null && prodID != null && prodID.trim().length()>0){
   	 sql1 = "SELECT toode FROM toode where ribakood='" + prodID + "' AND (ribakood!='' OR ribakood!=' ')";
   	
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

  private Vector splitOrder (Order order){
	  Vector result = new Vector();
	  Order ordNorm = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ordRR = new Order(order.getCompany(),order.getAgentFirstName());
	     
	  ordNorm.setAgentCode(order.getAgentCode());
	  ordNorm.setDokNr(order.getDokNr());
	  ordNorm.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  
	  

	  ordRR.setAgentCode(order.getAgentCode());
	  ordRR.setDokNr(order.getDokNr());
	  ordRR.setShipCal(order.getShipCal().getTimeInMillis() +"");


	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
		  
		     String itemid = lin.getToode();
		     if(itemid.startsWith("RR")){
		     	
		     	ordRR.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	ordRR.setWarehouse("RM");
		     	System.out.println("ordRR " + lin.getToode());
		     }else {
		     	ordNorm.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		     	ordNorm.setWarehouse("1");
		     	System.out.println("ordNorm " + lin.getToode());
		     
	  }
		     }
	  

	//  iif(a.artikkel>10000 AND a.artikkel<20000,600,  " +
		//		"   iif(a.artikkel>100000 AND a.artikkel<499999,500,700)) 
	  
	  if(ordRR.getOrderLines() != null && ordRR.getOrderLines().size()>0){
		  result.add(ordRR);  
	  }
	  if(ordNorm.getOrderLines() != null && ordNorm.getOrderLines().size()>0){
		  result.add(ordNorm);  
	  }
	  
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }
  
  public void makeOrder(Order order) throws SQLException{
	  
	  
	  Vector splitted =  splitOrder(order);
	  Enumeration en = splitted.elements();
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();
		  setOrder(ord);
		    int invoiceID = getNextID();
		    invoiceID =  checkForID (invoiceID);
		    String ladu = ord.getWarehouse();
		    ladu = getLadu(ladu, ord);
		    System.out.println("ladu ;" +  ladu);
		    addInvoiceHeader(invoiceID,ladu);
		    addInvoiceLines(invoiceID);
	  }
		 
	  
	  
   
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
	  
  
  private int checkForID(int iii) throws SQLException {
  	String sql = "select dokumnr from dok where doktyyp='T' and dokumnr= "+iii;
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    if(rs.next()){
    	iii = getNextID();
    }
    
  	st.close();
  	
  	return iii;
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
  
  public String getImportFile(){
    return "C:/Mobile Tellimused/";
  }
  
  private void addInvoiceLines(int invID) throws SQLException{
    String sql = " INSERT INTO rid VALUES('T',?, ?, 0, ?, ?,'','','',0,'','','','',date()," +
                 "0,0,0,?,'','','','','','','',0,0,'','','','',0,.F.,.F.,0,0,.F.,'',0,'','',0,'',.F.,'',0)";
    
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
     // st.setDouble(6, line.getPrice());
	
      if(line.getEAN() == null ){
		 st.executeUpdate();	
	}else if( !line.getEAN().startsWith("205000003") ){
	 st.executeUpdate();
    }
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
	  
	  
    String sqlold = "INSERT INTO dok VALUES ('T',?, date(), 'LT1',?,1, 'EUR','" + ordernumber + "','', date(),'"+ ladu +"', '1', ?,?,0, .F., 'ADMIN','ADMIN', " +
         "'23 59', '', '1', 1,date(), 0,'Mobiilne Tellimus','', .F., '','', '', '', '', '','','','epoc','','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),?,.T., 0, '2000000000',0, '','" + ordernumber + "','','','','','',0)  ";


    String sql = "INSERT INTO dok VALUES ('T',?, date(), 'LT1',?,1, 'EUR','" + ordernumber + "','', date(),'"+ ladu +"', '1', ?,?,0, .F., 'ADMIN','ADMIN', " +
         "'23 59', '', '1', 1,date(), 'Mobiilne Tellimus','', .F., '','', '', '', '', '','','','epoc','','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),?,.T., 0, '2000000000',0, '','" + ordernumber + "',date(),'','','','','',0,'',0)  ";
    
    
  String sql2 = "Update rid set taitmata =0  where doktyyp = 'T' and reakuupaev > date()-10";


    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setString(2, order.getCompany());
    //stmt.setString(3, String.valueOf(invID));
    stmt.setString(3, order.getAgent());
    stmt.setString(4, order.getCompany());
    stmt.setDate(5, dd);
    stmt.executeUpdate();
    
    stmt = con.prepareStatement(sql2);
    stmt.executeUpdate();
    stmt.close();
  }

  public String getDriverVersion(){
    return "Avallone";
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
