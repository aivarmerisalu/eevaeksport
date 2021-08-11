package eevaeksport.imp.driver;

import java.sql.Connection;
import java.sql.Date;
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

public class EevaPalWareImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public EevaPalWareImportDriver(ImportPanel parent) {
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


  
  
  public String getDoknr(String prodID,String custID) throws SQLException{


    String sql1 = " SELECT tasudok FROM dok INNER " +
	   " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 " +
	    "  AND dok.kuupaev >= date()-90 AND  dok.doktyyp = 'G'  AND rid.doktyyp= 'G' and dok.saaja='" 
		+ custID + "' AND rid.Toode='" + prodID + "' " +
	    "  ORDER BY dok.saaja, toode.pesakood,dok.kuupaev GROUP BY dok.tasudok";
   
    String  res = "" ;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    System.out.println(sql1);
    while (rs1.next()) {
      res = rs1.getString("tasudok").trim() + " ";
   }
    rs1.close();
    st1.close();

    return res;
  }

  
  public String getToode(String ean) throws SQLException{


	    String sql1 = " SELECT t.toode FROM  toode t where  t.ribakood='" + ean + "' AND left(t.toode,1)='�' " ;
	    String  toode = null;
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      toode = rs1.getString("toode");
	      System.out.println("Saime tagastustoote " + toode);	      
	    }
	    rs1.close();
	    st1.close();
	    if (toode == null && ean != null ){
	    	toode = "�T0000" + ean.substring(ean.length()-3);
	    }
	   
	    return toode;
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

  private void addInvoiceLines(int invID) throws SQLException{
	    String sql = "INSERT INTO rid VALUES('T',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
	                 "0,0,0,0,'','','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0)";
	    PreparedStatement st = con.prepareStatement(sql );

	    Enumeration enums = order.getOrderLines().elements();
	    while(enums.hasMoreElements()){
	      OrderLine line = (OrderLine)enums.nextElement();
	      st.setInt(1, invID);
	      String toode = line.getToode();
	     
	      
	      if (toode == null){
	      toode = " ";
	    }
	      if (toode.length()>10){
	    	  toode = getToode(toode);
	      }
	      
	      if(toode.indexOf(".")!= -1){
	    	toode =   this.identifyProduct(null, toode);
	      }
	      
	      String nimi = line.getTooteNimi();
	      if (toode == null){
	          toode = " ";
	          nimi = line.getToode();
	        }
	      
	      
	      
	      st.setString(2, nimi);
	     
	      if(line.getKogus() < 0 ){
	      	 st.setDouble(3, getPrice(line.getToode(), order.getCompany()));
	      }else{
	      	 st.setDouble(3, 0);
	      }
	     
	      st.setDouble(4, line.getKogus());
	      st.setString(5, toode);
	      st.executeUpdate();
	      setNumAddedLines(getNumAddedLines() + 1);
	    }
	    st.close();
	  }

	  private void addInvoiceHeader(int invID) throws SQLException{
	  
	  	
	  	
	    String sql = "INSERT INTO dok VALUES ('T',?, ?, 'LT1',?,1, 'EUR',?,'', date(),'TALLINN', '1', ?,?,0, .F., '','ADMIN', " +
	         "'23 59', '', '1', 1,date(), 0,?,' ', .F.,'', '', '', '', '', '','','','','mobilejava','', 0, .F.,0,0,0, '', '', .F., " +
	        "date(),date(),.F., 0, '2000000000',0, '','','','','','',.F.,.F.,0) ";
	    String info = order.getInfo();
	    System.out.println("Info " +info);
	    
	    if(info == null){
	    	info = " ";
	    }
	  
	    log.info("addInvoiceHeader2: " + sql);
	    PreparedStatement stmt = con.prepareStatement(sql);
	    stmt.setInt(1, invID);
	    stmt.setDate(2,new Date(order.getShipCal().getTimeInMillis()));
	    stmt.setString(3, "AAATAGASTUS");
	    stmt.setString(4, String.valueOf(invID));
	    stmt.setString(5, order.getAgent());
	    stmt.setString(6, "AAATAGASTUS");
	    stmt.setString(7, info);
	   // stmt.setString(8, tagastus);

	    stmt.executeUpdate();
	    stmt.close();
	  }
  
  
  
  public void makeOrder(Order order) throws SQLException{

    setOrder(order);
    boolean tagastus = false;
    
    if(((OrderLine)order.getOrderLines().get(0)).getKogus() <0  ){
    	tagastus = true;
    }
    
    if(tagastus){
     int invoiceID = getNextID();
    	    addInvoiceHeader(invoiceID);
    	    addInvoiceLines(invoiceID);
    }else{
        changeOrderContent(order.getCompany());	
    }
  
    
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
 
  
  private void changeOrderContent(String invnr) throws SQLException{
 
  //	1.otsime doknri ylesse 
  	
  	String sqldok = "Select dokumnr from dok where tasudok='" + invnr + "' and dok.doktyyp='T'";
  	
  	int kanne = 0;
  	
  	Statement sm = con.createStatement();
  	ResultSet rr = sm.executeQuery(sqldok);
  	if(rr.next()){
  		kanne = rr.getInt("dokumnr");
  	}
  	rr.close();
  	sm.close();
  	
  	
  	
  	//2. K�ime l�bi read ja vaatame kas seal dokumendil on selline rida  
  	
				
				Enumeration enums = order.getOrderLines().elements();
				while(enums.hasMoreElements()){
				OrderLine line = (OrderLine)enums.nextElement();
				
				
				String sqlotsi = "select toode from rid where dokumnr="+ kanne + " and doktyyp='T' and toode='" + line.getToode()+"'";
				System.out.println("sqlotsi" + sqlotsi);
				Statement so = con.createStatement();
				ResultSet sorr = so.executeQuery(sqlotsi);
				if(sorr.next()){
					System.out.println("Toode olemas UPDATE ");
					String sql2 = "UPDATE rid set kogus=" + line.getKogus() + " where dokumnr = "+ kanne + " and doktyyp ='T' and toode='" + line.getToode()+"'";
					Statement aa = con.createStatement();
					aa.executeUpdate(sql2);
					aa.close();

				      setNumAddedLines(getNumAddedLines() + 1);
					
				} else {
					System.out.println("Toote lisamine INSERT");
					
					String sql = "INSERT INTO rid VALUES('T',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
				    "0,0,0,0,'','','','','','','',0,0,'','','',0,0,.F.,.F.,0,'',.F.,0,.F.,'',0,'',0,0)";
								PreparedStatement st = con.prepareStatement(sql);	
					st.setInt(1, kanne);
					String toode = line.getToode();
					if (toode == null){
					toode = " ";
					}
					
					st.setString(2, line.getTooteNimi());
					
					if(line.getKogus() < 0 ){
					st.setDouble(3, getPrice(line.getToode(), order.getCompany()));
					}else{
					st.setDouble(3, 0);
					}
					
					st.setDouble(4, line.getKogus());
					st.setString(5, toode);

				      setNumAddedLines(getNumAddedLines() + 1);
				      
					st.executeUpdate();
					st.close();
					}
					
				}
				
				

			    
				  	
		
  }

  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
 
  
  
  public double getPrice(String prodID,String custID) throws SQLException{


    String sql1 = " SELECT rid.hind , toode.myygihind FROM dok INNER " +
	   " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 " +
	    "  AND dok.kuupaev >= date()-90 AND  dok.doktyyp = 'G'  AND rid.doktyyp= 'G' and dok.saaja='" 
		+ custID + "' AND rid.Toode='" + prodID + "' " +
	    "  ORDER BY dok.saaja, toode.pesakood,dok.kuupaev GROUP BY dok.saaja, rid.toode";

    
    
    double hind = 0;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      hind = rs1.getDouble("hind");
      System.out.println("Saime Erihinna" + custID + " " + prodID + " " +hind);
      
    }

    rs1.close();
    st1.close();
    
    
    if(hind == 0){
    	
    	String sql2 = " SELECT myygihind FROM toode WHERE toode = '" + prodID + "'";
    Statement st2 = con.createStatement();
    ResultSet rs2 = st2.executeQuery(sql2);

    while (rs2.next()) {
      hind = rs2.getDouble("myygihind");
    }

    rs2.close();
    st2.close();
    }
    
    return hind;
  }

  
  public String getDriverVersion(){
    return "EevaPalWare";
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
