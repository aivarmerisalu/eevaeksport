
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;





import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
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

public class MovexLITImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public MovexLITImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection("jdbc:oracle:thin:@10.10.10.55:1522:MVXEE002", parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));;
      
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }


  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010100 where SL01106  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  trim(N08_KODAS_KS)||'-'|| N33_EIL_NR = '" + id +"'"; 
  	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();

    return acc;

  }
  
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010100 left JOIN SY240100  ON SL01036=SY24002 " +
 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
 	
   System.out.println(sql1);
   String agent = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();

   return agent;

 }
 
  
  public String getPandiPakend(String toode) throws SQLException{
 	 
    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
    String prod = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("pant").trim();
    }
       System.out.println(toode + " prod1 " + prod);
    rs1.close();
    st1.close();
    return prod;
 }
 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + ean.trim() 
                              + "' or RTRIM(SC01003) = '" + ean.trim() + "' and SC01015 != ''";

    String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
   System.out.println(ean + " prod0 " + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();

    return prod;
  }


  
  public void makeOrder(Order order) throws SQLException{

    makeOutput(order);
  }

  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	 
 	try {
 	  int cou = 0;
      
		String agent = order.getAgent();
		Calendar cal = order.getShipCal();
		
		System.out.println("Tuvastatud agent :" + agent );
	
		String ref = order.getReference();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		String ware = order.getWarehouse();
		if ( ware == null ){
			ware = " ";
		}
		 String doknr = order.getDokNr();
         if(doknr.length()>6){
         	// doknr = doknr.substring(doknr.length()-6,doknr.length());
         }
         if(doknr.trim().length() == 00){
        
        	
         }

         String ls = System.getProperty("line.separator");
         
         sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
         sb.append("<Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:env=\"http://www.intentia.com/MBM_Envelope_1\" xmlns=\"http://www.intentia.com/schemas/mec\">" + ls);
         sb.append(" <Header>" + ls);
         sb.append("  <env:delivery>" + ls);
         sb.append("  <env:to>" + ls);
         sb.append("    <env:address>ragutis</env:address> " + ls);
         sb.append("  </env:to>" + ls);
         sb.append("  <env:from>" + ls);
         sb.append("    <env:address>MobileJava</env:address> " + ls);
         sb.append("  </env:from>" + ls);
         sb.append("  </env:delivery>" + ls);
         sb.append("  <env:manifest>" + ls);
         sb.append("    <env:reference uri=\"#ORDERS@1\">" + ls);
         sb.append("      <description /> " + ls);
         sb.append("    </env:reference>" + ls);
         sb.append("  </env:manifest>" + ls);
         sb.append(" </Header>" + ls);
         sb.append("<Body>" + ls);
         sb.append("<ORDERS>" + ls);
         sb.append("   <BGM>" + ls);
         sb.append("      <cmp01>" + ls);
         
         String ordnr = order.getDokNr();
         
         sb.append("        <e01_1001>615</e01_1001>" + ls);
         sb.append("      </cmp01>" + ls);
         sb.append("     <e01_1004>" + ordnr + "</e01_1004>" + ls);
         sb.append("   </BGM>" + ls);
         sb.append("      <DTM>" + ls);
         sb.append("          <cmp01>" + ls);
         sb.append("            <e01_2005>2</e01_2005>" + ls);
         sb.append("            <e02_2380>" + escape(getDateString(cal)) + "</e02_2380>" + ls);
         sb.append("            <e03_2379>101</e03_2379> " + ls);
         sb.append("         </cmp01>" + ls);
         sb.append("     </DTM>" + ls);
         sb.append("   <g002>" + ls);
         sb.append("     <NAD>" + ls);
         sb.append("     <e01_3035>BY</e01_3035>" + ls);
         sb.append("      <cmp01>" + ls);
         sb.append("         <e01_3039>" + escape(order.getCompany()) + "</e01_3039>");
         sb.append("      </cmp01>" + ls);
         sb.append("     </NAD>" + ls);
         sb.append("   </g002>" + ls);
         
         
         
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
            double disc = line.getDiscount();
            if(disc > 0){
            	System.out.println("on ikka discounti kah ");
            	addDBLine(order,line.getToode(),disc);
            }

            nf.setMaximumFractionDigits(4);
 
       	
  
     
     sb.append("   <g025>" + ls);
     sb.append("      <LIN>" + ls);
     sb.append("         <e01_1082>1</e01_1082> " + ls);
     sb.append("          <cmp01>" + ls);
     sb.append("             <e01_7140>" + escape(line.getToode()) + "</e01_7140>" + ls);
     sb.append("             <e02_7143>SA</e02_7143> " + ls);
     sb.append("          </cmp01>" + ls);
     sb.append("      </LIN>" + ls);

  
     
     sb.append("       <QTY>" + ls);
     sb.append("          <cmp01>" + ls);
     sb.append("          <e01_6063>21</e01_6063>" + ls);
     double amount = line.getKogus();
     
 //    System.out.println(line.getProduct()+ " " + amount + " " + prod.getSortCode());
     
     sb.append("            <e02_6060>" + amount + "</e02_6060> " + ls);
     sb.append("          </cmp01>" + ls);
     sb.append("      </QTY>" + ls);
     sb.append("      <DTM>" + ls);
     sb.append("          <cmp01>" + ls);
     sb.append("            <e01_2005>2</e01_2005>" + ls);
     sb.append("            <e02_2380>"  + escape(getDateString(cal))  +  "</e02_2380>" + ls);

     sb.append("            <e03_2379>101</e03_2379> " + ls);
     sb.append("         </cmp01>" + ls);
     sb.append("     </DTM>" + ls);
     sb.append("   </g025>" + ls);

        
       cou++;
      }
      
      
      
      sb.append("</ORDERS>" + ls);
      sb.append("</Body>" + ls);
      sb.append("</Envelope>" + ls);
      
      
      
      
      
      
    	
      setNumAddedLines(cou);
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  +  ".xml";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  

 
 private void addDBLine(Order order, String prodid ,double disc) {
	  
	 String	 sql = "INSERT INTO discount VALUES (200,'600',6,'" + getToday() + "',' 2',0,'" + order.getCompany() + "','" + prodid + "',' '," +
	 		"1,0," + disc +",0,' ',' ',0,0,' ',' ',' ','" + getToday() + "','" + getTime() + "','" + getToday() + "',1,'AIVARAS')";
	    Statement st;
		try {
			st = con.createStatement();
		    st.executeUpdate(sql);
	        st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
 
 private String getToday() {
 	
     Calendar cal = Calendar.getInstance();
     String month = (cal.get(Calendar.MONTH)+1) +"";
     if(month.length() == 1){month = "0" + month;}
     String day = cal.get(Calendar.DAY_OF_MONTH)+"";
     if(day.length()==1){
     	day = "0" + day;
     }
     return cal.get(Calendar.YEAR) + "" + month + "" + day;
   }
 private String getTime() {
	 	
     Calendar cal = Calendar.getInstance();
     String hour = (cal.get(Calendar.HOUR_OF_DAY)) +"";
     if(hour.length() == 1){hour = "0" + hour;}
     String min = cal.get(Calendar.MINUTE)+"";
     if(min.length()==1){
    	 min = "0" + min;
     }
     String sec = cal.get(Calendar.SECOND)+"";
     if(sec.length()==1){
    	 sec = "0" + sec;
     }
     
     return hour + "" + min + "" + sec;
   }
 
 
 
  public String getDriverVersion(){
    return "MovexLITImport";
  }



  private String escape(String s){
    if (s == null) return s;
    StringBuffer sb = new StringBuffer();
    for (int i = 0 ; i < s.length() ; i++){
      char c = s.charAt(i);
      if (c == '&'){
        sb.append("&amp;");
      } else if (c == '<'){
        sb.append("&lt;");
      } else if (c == '>'){
        sb.append("&gt;");
      } else if (c == '\n'){
        sb.append(" ");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  
  
 
  public static final String addSpaces(String s, int totalLen){
    int len = s.length();
    StringBuffer sb = new StringBuffer(s);
    for (int i=len; i < totalLen ; i++){
      sb.append(' ');
    }
    if( len > totalLen){
    	return s.substring(0,totalLen);
    }
    return sb.toString();
  }
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();


  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    String year =   cal.get(Calendar.YEAR) + "";
    sb.append(year.substring(2,4));
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month);

    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day);
    return sb.toString();
  }

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
