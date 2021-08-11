package eevaeksport.imp.driver;

import java.sql.Connection;
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

public class EevaPalLatImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public EevaPalLatImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "eevalat");
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	if(regnr==  null){
  	    sql1 = "SELECT kood FROM firma1.klient where   fil= '" + id + "'";	
  	}else{

  	     sql1 = "SELECT kood FROM firma1.klient where  (regnr)= '" + regnr + "' AND fil= '" + id + "'";	
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
  public String identifyAgent(String id) throws SQLException {
	  	String sql1 = null;
	  	    sql1 = "SELECT objekt2 FROM firma1.klient where   kood= '" + id + "'";	
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("objekt2").trim();

	    }
	    rs1.close();
	    st1.close();

	    return acc;

	  }
  
  public double getPrice(String prodID,String custID) throws SQLException{


    String sql1 = " SELECT rid.hind , toode.myygihind FROM firma1.dok INNER " +
	   " JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode WHERE " +
	    " length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 2 " +
	    "  AND dok.kuupaev >= current_date -90 AND  dok.doktyyp = 'G'  and dok.klient='" 
		+ custID + "' AND rid.Toode='" + prodID + "' " +
	    "  ORDER BY dok.klient, toode.pesakood,dok.kuupaev GROUP BY dok.klient, rid.toode,rid.myygihind";

    
    
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
    	
    	String sql2 = " SELECT myygihind FROM firma1.toode WHERE toode = '" + prodID + "'";
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

  
  
	  
  
  
  public String getDoknr(String prodID,String custID) throws SQLException{


    String sql1 = " SELECT d.tasudok FROM firma1.dok d INNER " +
	   " JOIN firma1.rid  r ON d.dokumnr = r.dokumnr INNER JOIN firma1.toode t on r.toode = t.toode WHERE " +
	    " length(trim(d.klient)) > 1 AND length(trim(r.toode)) > 2 " +
	    "  AND d.kuupaev >= current_date -90 AND  d.doktyyp = 'G'  and d.klient='" 
		+ custID + "' AND r.Toode='" + prodID + "' " +
	    "  ORDER BY d.klient, t.pesakood,d.kuupaev GROUP BY d.tasudok";
   
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

  
  
  
  
  public String identifyProduct(String ean, String prodID) throws SQLException{
    String sql1 = "SELECT toode FROM firma1.toode where left(ribak,13)='" + ean + "'";


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
    addInvoiceHeader(invoiceID);
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
  
  private int getDokNr() throws SQLException{
	    String sql =  " select nextval('firma1.dok_t_seq') as seeralgus";

	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);
	    int intNr = -1;
	    while(rs.next()){
	      intNr = rs.getInt("seeralgus");
	    }
	    return intNr;
	  }

  
  private String getTagastus() throws SQLException{
  	
  	String result = "";
  	
  	 Enumeration enums = order.getOrderLines().elements();
  	 TreeMap tr = new TreeMap();
     while(enums.hasMoreElements()){    
       OrderLine line = (OrderLine)enums.nextElement();
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
	    String sql = "INSERT INTO firma1.rid VALUES(nextval('firma1.rid_id_seq'),nextval('firma1.rid_reanr_seq'), " +
	        "  ?, ?, ?, ?, ?,'','','',0,null,null,null,null,now(), " +
	              "   0,0,0,0)";
	    PreparedStatement st = con.prepareStatement(sql);

	    Enumeration enums= order.getOrderLines().elements();
	    while(enums.hasMoreElements()){
	      OrderLine line = (OrderLine)enums.nextElement();
	      st.setInt(1, invID);
	      String toode = line.getToode();
	      if (toode == null){
	          toode = " ";
	        }
	          if (toode.length()>10){
	        	  
	          }
	 
	      st.setString(2, line.getTooteNimi());

	      if(line.getKogus() < 0 ){
	      	 st.setDouble(3, getPrice(line.getToode(), order.getCompany()));
	      }else{
	      	 st.setDouble(3, 0);
	      }
	      st.setDouble(4, line.getKogus());
	        st.setString(5, toode.trim());

	      st.executeUpdate();
	      setNumAddedLines(getNumAddedLines() + 1);
	    }
	    st.close();
	  }
    
    private void addInvoiceHeader(int invID) throws SQLException{

    	
      	String tagastus = getTagastus();
        String info = order.getInfo();
     
        
        if(info == null){
        	info = " ";
        }
        if(tagastus.trim().length()>0){
        	info = tagastus;
        }
    	
   String doktyyp = "'T'";

  String konto = "113";
  String ladu = "RIGA";//tallinn 2 on riga
  
  String doknr = getDokNr()+"";
  if(order.getTeostaja() != null){
	  doknr = order.getTeostaja();
  }
  if(order.getDokNr() != null && order.getDokNr().trim().length() > 0){
	  doknr = order.getDokNr();
	  
  }
  
  if(doknr.startsWith("|")){
	  doknr = doknr.substring(1);
  }
  if(doknr.indexOf("|") != -1){
	  doknr = doknr.substring(0,doknr.indexOf("|"));         		 
	 }
  
  System.out.println("Doknr " + doknr);
//ss
	String agentIdent =identifyAgent(order.getCompany());
	
	if(agentIdent == null){
		agentIdent = order.getAgent();
	}
  
    String sql =   " insert into firma1.dok values" +
   " (" + doktyyp +",?,?,'LVV',?,1,'EUR','" + doknr + "'," +
   " '" + konto +"',NOW(),'" + ladu  + "',null,?,?, null,false,'ADMIN','ADMIN','23 59','1'," +
   " NULL,NULL ,null,NULL ,'" + info.replaceAll("'", "") + "','',false,'',null,null,null,null,null,null,null,'lvlv',''," +
   " 'mobile"  + Calendar.getInstance().getTimeInMillis() +"') " ;

System.out.println(sql);
    log.info("addInvoiceHeader2: " + sql);
    PreparedStatement stmt = con.prepareStatement(sql);

    stmt.setInt(1, invID);
    stmt.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
    stmt.setString(3, order.getCompany());  
    stmt.setString(4, agentIdent);
    stmt.setString(5, order.getCompany());
    stmt.executeUpdate();
    stmt.close();
  }
  
  
 

  public String getDriverVersion(){
    return "EevaPalLat";
  }
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
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
