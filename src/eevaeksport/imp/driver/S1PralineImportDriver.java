package eevaeksport.imp.driver;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

public class S1PralineImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(VipexImportDriver.class.getName());

  public S1PralineImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    nf.setGroupingUsed(false);
    try {
      Class.forName(databaseDriverName).newInstance();
      con2 = DriverManager.getConnection(url, "pda", "pda");
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  public String identifyCustomer(String id,String regnr) throws SQLException {

	    String acc = null;
	    String sql2 = " SELECT kl.code AS kood  FROM sc96 kl WHERE descr like '%" + id + "'";
	    System.out.println(sql2);
	    Statement st2 = con2.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);

	    while (rs2.next()) {
	      acc = rs2.getString("kood").trim();
	    }
	    
	    rs2.close();
	    st2.close();

	    

	    return acc;
	  }

  public String getAgent(String kood) throws SQLException {

	    String acc = null;
	    String sql2 = " SELECT kl.sp715 AS kood  FROM sc96 kl WHERE ltrim(rtrim(kl.code)) = '" + kood + "'";
	    System.out.println(sql2);
	    Statement st2 = con2.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);

	    while (rs2.next()) {
	      acc = rs2.getString("kood").trim();
	    }
	    
	    rs2.close();
	    st2.close();

	    String sql1 = " SELECT kl.code AS kood  FROM sc96 kl WHERE ltrim(rtrim(kl.code)) = '" + acc + "'";
	    st2 = con2.createStatement();
	     rs2 = st2.executeQuery(sql1);

	    while (rs2.next()) {
	      acc = rs2.getString("kood").trim();
	    }
	    
	    rs2.close();
	    st2.close();
	    
	    return acc;
	  }

  

  public String identifyProduct(String ean, String prodID) throws SQLException{
	    String sql1 = "SELECT  (date),SP1463 as ok,pr.id,pr.code AS kood,pr.descr AS nimi,c.value AS hind,sp510 AS EAN , yh.descr AS yhik  " +
           " ,parentid AS grupp FROM sc113 pr  " +
           " INNER JOIN sc41 yh ON yh.id=sp106 INNER JOIN SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) " +
		   " inner join sc504 abi on abi.id=hi.sp508 " +
           " WHERE ltrim(c.id)='E5' AND isfolder=2 AND ltrim(c.value)!='0.000' and ltrim(sp508)='2'  and sp510= '" + ean + "' " ;

	    String sql2 = "SELECT  (date),SP1463 as ok,pr.id,pr.code AS kood,pr.descr AS nimi,c.value AS hind,sp510 AS EAN , yh.descr AS yhik  " +
        " ,parentid AS grupp FROM sc113 pr  " +
        " INNER JOIN sc41 yh ON yh.id=sp106 INNER JOIN SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) " +
		   " inner join sc504 abi on abi.id=hi.sp508 " +
        " WHERE ltrim(c.id)='E5' AND isfolder=2 AND ltrim(c.value)!='0.000' and ltrim(sp508)='2'  and pr.code = '" + prodID + "' " ;

	    
	    String prod = null; 
	    Statement  st1 = con2.createStatement();
        ResultSet  rs1 = st1.executeQuery(sql1);
	      while (rs1.next()) {
	        prod = rs1.getString("kood").trim();
	      	System.out.println("prod2 " + prod );
	      }
	    rs1.close();
	    st1.close();
	    System.out.println(sql1);
	    
	   System.out.println("prod" + prod +" "+ ean+prodID);
	   if(prod == null){
		   st1 = con2.createStatement();
		   rs1 = st1.executeQuery(sql2);
		      while (rs1.next()) {
		        prod = rs1.getString("kood").trim();
		      	System.out.println("prod2 " + prod );
		      }
		    rs1.close();
		    st1.close();
		   
	   	prod = prodID;
	   }
	    return prod;
	  }



  public void makeOrder(Order order) throws SQLException {
	  makeOutput(order);
  }

  
  

  private void makeOutput(Order order){
	    
	  StringBuffer sb = new StringBuffer();
	  String firma = order.getCompany();
	  String reference  = order.getDokNr();
      Calendar cal = Calendar.getInstance();
      cal.setTime(order.getShipCal().getTime());


      
      
	    
	      try {

   String separator  = ";";
	        int coun = 0;

	        Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	          if(line.getKogus()!= 0 && !toode.equals("")){
	        	 
	        	  sb.append( firma + separator
	                      + line.getToode() + separator
	                      + line.getKogus() );
	        	  sb.append(separator + "0");//ale
	              sb.append(separator + " ");// dealer
	              sb.append(separator + getAgent(firma));// agent
	              sb.append(separator + getDate(cal)); //p�ev 
	              sb.append(separator + " "); //kommetaar
	              sb.append(separator + " ");//action kood
	              sb.append(separator   + " ");// action hind 
	              sb.append(separator   + reference);// tellimuse number
	              
	             // System.out.println(sb.toString());
	              sb.append(System.getProperty("line.separator"));
	        	  coun++;
	          }
	          }

	        setNumAddedLines(coun); 
	        long uni = Calendar.getInstance().getTimeInMillis();
	 	
	      String fileName = parent.getProps().getProperty("conv.dir") + order.getDokNr() + uni +  ".txt";
	      System.out.println(fileName);
	      FileOutputStream fos = new FileOutputStream(fileName);
	        fos.write(sb.toString().getBytes());
	        fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
  
  
  
  
  
  private ImportPanel parent;

  public String getDriverVersion() {
    return "InterAlco1S";
  }

 

  public boolean isAmount() {
    return isAmount;
  }

  /**
   *
   * @param aUseEAN - the new value for useEAN
   */
  public void setAmount(boolean aAmount) {
    isAmount = aAmount;
  }
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

  private String url = "jdbc:odbc:Praline";
  private Connection con2;
  private boolean isAmount = false;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
/**
 * @return the nf
 */
public NumberFormat getNF() {
	return nf;
}

private String getDate(Calendar cal){
  StringBuffer sb = new StringBuffer();
  String year =   cal.get(Calendar.YEAR) + "";
  String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
  String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
   if (day.length() == 1){
    sb.append("0");
  }
   sb.append(day);
   sb.append(".");
  if (month.length() == 1){
   sb.append("0");
 }
   sb.append(month );
  sb.append( "." + year);
  return sb.toString();
}

/**
 * @param nf the nf to set
 */
public void setNF(NumberFormat nf) {
	this.nf = nf;
}

}
