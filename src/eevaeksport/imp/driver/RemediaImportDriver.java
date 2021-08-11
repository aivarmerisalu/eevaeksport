/*
 * Created on 29.08.2007
 */
package eevaeksport.imp.driver;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public class RemediaImportDriver extends AbstractImportDriver {

	static Category log = Category.getInstance(RemediaImportDriver.class.getName());
	

	  private ImportPanel parent;
	
	  public RemediaImportDriver(ImportPanel parent) {
		  this.parent = parent;
	    PropertyConfigurator.configure("log4j.properties");
	    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	      con = DriverManager.getConnection(url + "baas");
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
	  }
	
	 public String identifyCustomer(String id,String regnr) throws SQLException {
	    String sql1 = "select code as kood from SC69 where  SP3911 = '" + id + "' and sp71 = '" + regnr + "'";
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim();

	    }
	    rs1.close();
	    st1.close();

	    System.out.println("Klient: " + acc);
	    return acc;

	  }

	  public String identifyProduct(String ean, String prodID) throws SQLException{
	    String sql1 = "SELECT pr.code AS toode  FROM SC322 pr INNER JOIN SC85 y ON y.id = pr.sp619  " +
       "  WHERE  isfolder=2  AND parentid in (  " +
	    " SELECT id FROM SC322 WHERE isfolder=1 and  ltrim(rtrim(parentid)) in ('5','PC')) " +
		" and (ltrim(rtrim(SP2807)) ='2EO' or  ltrim(rtrim(SP2807)) ='25S' or  ltrim(rtrim(SP2807)) ='2XV' ) and  " +
	    " pr.descr not like '% EKS'  and pr.descr not like '% PL'  and SP1877= '" + ean + "' and SP1877 !=''";


	    String sql2  = "SELECT pr.code AS toode  FROM SC322 pr INNER JOIN SC85 y ON y.id = pr.sp619  " +
	       "  WHERE  isfolder=2  AND parentid in (  " +
		    " SELECT id FROM SC322 WHERE isfolder=1 and  ltrim(rtrim(parentid)) in ('5','PC')) " +
			" and (ltrim(rtrim(SP2807)) ='2EO' or  ltrim(rtrim(SP2807)) ='25S' or  ltrim(rtrim(SP2807)) ='2XV' ) and  " +
		    " pr.descr not like '% EKS'  and pr.descr not like '% PL'  and pr.code= '" + prodID + "' ";

	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      prod = rs1.getString("toode").trim();
	    }
 System.out.println("prod0" + prod + " " + sql1);
	    if(prod == null){

	      rs1 = st1.executeQuery(sql2);

	      while (rs1.next()) {
	        prod = rs1.getString("toode").trim();
	      }
	    }

	    System.out.println("prod1" + prod);
	    rs1.close();
	    st1.close();

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
		              sb.append(separator + getDate(cal)); //päev 
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
	  public String getAgent(String kood) throws SQLException {

		    String acc = null;
		    String sql2 = " SELECT kl.SP3123 AS kood  FROM sc69 kl WHERE ltrim(rtrim(kl.code)) = '" + kood + "'";
		    System.out.println(sql2);
		    Statement st2 = con.createStatement();
		    ResultSet rs2 = st2.executeQuery(sql2);

		    while (rs2.next()) {
		      acc = rs2.getString("kood").trim();
		    }
		    
		    rs2.close();
		    st2.close();
		    
		    return acc;
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

	  
	  public String getImportFile(){
		    return parent.getProps().getProperty("imp.read");
		  }
		  
	  private String url = "jdbc:odbc:";
	  private Connection con;

	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
