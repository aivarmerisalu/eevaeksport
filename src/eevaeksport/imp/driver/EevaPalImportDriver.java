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
import java.util.Iterator;
import java.util.TreeMap;

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

public class EevaPalImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public EevaPalImportDriver(ImportPanel parent) {
    
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eeva");
    }
    catch (Exception e) {
      parent.showError(e);
      System.exit(0);
    }
  }

  
  private Connection getCon(){
	  try{
		  return   DriverManager.getConnection(url + "eeva"); 
	  }catch(Exception r){
		  System.exit(0);
	  }
	  return null;
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
    Connection con = getCon();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim();

    }
    rs1.close();
    st1.close();
    con.close();
   // System.out.println("Klient: " + acc);
    return acc;

  }

  public double getPrice(String prodID,String custID) throws SQLException{


    String sql1 = " SELECT rid.hind , toode.myygihind FROM dok INNER " +
	   " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 " +
	    "  AND dok.kuupaev >= date()-90 AND  dok.doktyyp = 'G'  AND rid.doktyyp= 'G' and dok.saaja='" 
		+ custID + "' AND rid.Toode='" + prodID + "' " +
	    "  ORDER BY dok.saaja, toode.pesakood,dok.kuupaev GROUP BY dok.saaja, rid.toode";

    
    
    double hind = 0;
    Connection con = getCon();
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
    con.close();
    
    return hind;
  }

  
  public String getToode(String ean) throws SQLException{


	    String sql1 = " SELECT t.toode FROM  toode t where  t.ribakood='" + ean + "' AND left(t.toode,1)='�' " ;
		    

	    
	    
	    String  toode = null;
	    Connection con = getCon();
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	
	      toode = rs1.getString("toode");
	      System.out.println("Saime tagastustoote " + toode);
	      
	    }
	    rs1.close();
	    st1.close();
	    con.close();
	    if (toode == null && ean != null ){
	    	toode = "�T0000" + ean.substring(ean.length()-3);
	    }
	   
	    return toode;
	  }

	  
  
  
  public String getDoknr(String prodID,String custID) throws SQLException{


    String sql1 = " SELECT tasudok FROM dok INNER " +
	   " JOIN rid ON dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
	    " len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 2 " +
	    "  AND dok.kuupaev >= date()-90 AND  dok.doktyyp = 'G'  AND rid.doktyyp= 'G' and dok.saaja='" 
		+ custID + "' AND rid.Toode='" + prodID + "' " +
	    "  ORDER BY dok.saaja, toode.pesakood,dok.kuupaev GROUP BY dok.tasudok";
   
    String  res = "" ;
    Connection con = getCon();
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    System.out.println(sql1);
    while (rs1.next()) {
      res = rs1.getString("tasudok").trim() + " ";
   }
    rs1.close();
    st1.close();
    con.close();
    
    return res;
  }

  
  
  
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM toode where left(ribakood,13)='" + ean + "'";


    String sql2 = "SELECT toode FROM toode where toode='" + prodID + "'";
    String sql3 = "SELECT toode FROM toode where pesakood='" + prodID + "' and aktiivne = 1 ";

    String prod = null;
    Connection con = getCon();
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

    System.out.println("prod1" + prod);
    rs1.close();
    st1.close();
    con.close();
    return prod;
  }


  public void makeOrder(Order order) throws SQLException{

    setOrder(order);
    int invoiceID =0;
    try{
     invoiceID = getNextID();
     if(exists(invoiceID)){
    	 invoiceID = getNextID();
     }
    }catch (Exception r){
     System.exit(0);	
    }
    
    addInvoiceHeader(invoiceID);
    addInvoiceLines(invoiceID);
  }
  
  private boolean exists (int i) throws SQLException{
      boolean res = false;
	  String sql = "select dokumnr from dok where doktyyp = 'T' and dokumnr =" + i;
	    Connection con = getCon();
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	
	    if(rs.next()){
	      res = true;
	      System.out.println(sql);
	    }
	    //System.out.println(sql + " " + res);
	  
	    rs.close();
	    st.close();
	    con.close();
	    //System.exit(0);
      return res;
  }

  private int getNextID() throws SQLException{
	  long asi  = Calendar.getInstance().get(Calendar.MILLISECOND);
	  
	  try {
		//	wait(asi);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    String sql = "select seeralgus from alusdok where alusdok='LT'";
    Connection con = getCon();
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
    con.close();

System.out.println("Number " + intNr);
    return intNr;
  }
  
  private String getTagastus() throws SQLException{
  	
  	String result = "";
  	
  	 Enumeration enume = order.getOrderLines().elements();
  	 TreeMap tr = new TreeMap();
     while(enume.hasMoreElements()){    
       OrderLine line = (OrderLine)enume.nextElement();
       if(line.getKogus() < 0 ){
        tr.put(getDoknr(line.getToode(), order.getCompany()),"");
       }     
     }
     
     if(tr.size()>0){
     	Iterator iter = tr.keySet().iterator();
     	boolean first = true;
     	while(iter.hasNext()){
     		String res = (String)iter.next();
     		if(first){
     			result = "Tagastused p�rinevad Arvetelelt :";
     		}
     		
     		result = result + res + ",";
     	}
     }


    return result;
  }

  private void addInvoiceLines(int invID) throws SQLException{
    String sql = "INSERT INTO rid VALUES('T',?, ?, ?, ?, ?,'','','',0,'','','','',date()," +
                 "0,0,0,0,'','','','','','','',0,0,'','','','',0,.F.,.F.,0,0,.F.,'',0,'','',0 ,'',.F.,'',0)";
    Connection con = null;
   try{
	   con = getCon(); 
   }catch(Exception r){
	   try{
		   con = getCon(); 
	   }catch(Exception rw){
		   try{
			   con = getCon(); 
		   }catch(Exception rr){
			   try{
				   con = getCon(); 
			   }catch(Exception rt){
				  
			   }  
		   }  
	   }  
   }
   
    PreparedStatement st = con.prepareStatement(sql );

    Enumeration enumr = order.getOrderLines().elements();
    while(enumr.hasMoreElements()){
      OrderLine line = (OrderLine)enumr.nextElement();
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
    con.close();
  }

  private void addInvoiceHeader(int invID) throws SQLException{
  	String tagastus = getTagastus();
  	
  	
    String sql = "INSERT INTO dok VALUES ('T',?, ?, 'LT1',?,1, 'EUR',?,'', date(),'TALLINN', '1', ?,?,0, .F., '','ADMIN', " +
         "'23 59', '', '1', 1,date(),?,' ', .F.,'', '', '', '', '', '','','','','mobilejava','', 0, .F.,0,0,0, '', '', .F., " +
        "date(),date(),.F.,.F., 0, '2000000000',0, '' ,'',date(),date(),'','','','','',0,.F.,.F.,0) ";
    String info = order.getInfo();
    System.out.println("Info " +info);
    
    if(info == null){
    	info = " ";
    }
    if(tagastus.trim().length()>0){
    	info = tagastus;
    }
    log.info("addInvoiceHeader2: " + sql);
    Connection con = getCon();
    PreparedStatement stmt = con.prepareStatement(sql);
    stmt.setInt(1, invID);
    stmt.setDate(2,new Date(order.getShipCal().getTimeInMillis()));
    stmt.setString(3, order.getCompany());
    stmt.setString(4, String.valueOf(invID));
    stmt.setString(5, order.getAgent());
    stmt.setString(6, order.getCompany());
    stmt.setString(7, info);
   // stmt.setString(8, tagastus);

    stmt.executeUpdate();
    stmt.close();
    con.close();
  }

  public String getDriverVersion(){
    return "EevaPal";
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
