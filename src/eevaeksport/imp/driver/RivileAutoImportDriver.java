
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

public class RivileAutoImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileAutoImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "b2b","dba","sql");

    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	 
  
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	    sql1 = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_EIL_NR) is null then '1' else trim(str(N33_EIL_NR)) end) AS kood " +
	  " FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
	  " where right(rtrim(N33_PAV),3) =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood1").trim() + "-" +rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyCustomerName(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_PAV AS aadress FROM n08_klij   WHERE" +
	  	 		"  trim(N08_KODAS_KS) = '" + id +"'"; 
	  	
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("aadress").replace("\"", "-").trim();

	    }
	    rs1.close();
	    st1.close();
	    return acc;
	  }
  
  public String getLadu(String ag) throws SQLException {
	  	 String	sql1 = "SELECT N15_PASTABOS AS objekt  FROM  n15_men where N15_KODAS_MS = '" + ag +"'"; 
	  	
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("objekt").trim();

	    }
	    rs1.close();
	    st1.close();
	    return acc;
	  }


  
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  trim(N08_KODAS_KS)||'-'|| N33_kodas_ak = '" + id +"'"; 
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
 	 String	sql1 = "SELECT N33_SHIP_VIA AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
 	 		"  trim(N08_KODAS_KS)||'-'|| N33_kodas_ak = '" + id +"'"; 
 	
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
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
    
    String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
     " FROM n17_prod a LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps " +
   	"  WHERE N17_KOD_T = '" + ean.trim() + "'";

    String sql2 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
    " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps " +
   	"  WHERE  N17_KODAS_PS = '" + prodID.trim() + "'";

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

  private void setOrder(Order order){
	     this.order = order;
	   }
  private Order order = null;
  
  public void makeOrder(Order order) throws SQLException{
 
	  setOrder(order);
      makeOutput(order);
  }
  
  
  
  

  public String getPartii(String toode , String ladu , double kogus) throws SQLException {
	 	 String	sql1 = " SELECT  I17_KODAS_OS AS partii " +
 " from i17_vpro where I17_KODAS_IS in ('" + ladu + "') and I17_KIEKIS >= " + kogus + " and I17_KODAS_PS = '" + toode +"'"; 
	 	
	   System.out.println(sql1);
	   String acc = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	     acc = rs1.getString("partii").trim();

	   }
	   rs1.close();
	   st1.close();

	   return acc;

	 }
	  
	

 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
    
 	String ls = System.getProperty("line.separator");
 	try {
 	  int cou = 1;
      sb.append(getLineNr(cou));
      sb.append("       <I06>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_OP_TIP>55</I06_OP_TIP>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_PVM_TIP>1</I06_PVM_TIP>" + ls);
      cou++;
      String cust = order.getCompany();

      
      String agent = order.getAgent();
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_MS>" + escape(agent) + "</I06_KODAS_MS>" + ls);
      cou++;
      String name =  null;
  	try {
  		
  		
  		name = identifyCustomerName(cust.substring(0,cust.lastIndexOf("-")));
  		if(name == null){
  			name ="nonam";
  		}else{
  			name = name.replaceAll("UAB", " ").trim();
  	  		if(name.length()>5){
  	  			name = name.substring(0,5);
  	  		}	
  		}
  		if(name.startsWith("-")){
  			name = name.substring(1);
  		}
  		
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}

      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_NR>U" +  escape(agent) +  ( Calendar.getInstance().getTimeInMillis()+"").substring(6,13)  + "</I06_DOK_NR>" + ls);
      cou++;
      

      sb.append(getLineNr(cou));
      sb.append("          <I06_OP_DATA>" +  getDateString(order.getShipCal()) + "</I06_OP_DATA>" + ls);
      cou++;
      
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_DOK_DATA>" + ls);
      cou++;
  


	sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_KS>" + escape(cust.substring(0,cust.lastIndexOf("-"))) + "</I06_KODAS_KS>" + ls);
      cou++;
      

      sb.append("          <I06_KODAS_AK>" + escape(cust.substring(cust.lastIndexOf("-")+1)) + "</I06_KODAS_AK>" + ls);
      cou++;
      String addr =  null;
	try {
		addr = identifyCustomerADDR(cust);
	} catch (SQLException e1) {
		e1.printStackTrace();
	}
	if(addr != null){
		sb.append(getLineNr(cou));
	      sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
	      cou++;	
	}
	
	  String lisa = null;
      if(order.getInfo() != null ){
  		 lisa = order.getInfo(); 
  	  }
	
	  if(lisa != null){
    	  sb.append(getLineNr(cou));
          sb.append("          <I06_PASTABOS>" + escape(lisa) + "</I06_PASTABOS>" + ls);
          cou++;  
      }
      
	  java.util.Collections.sort(order.getOrderLines(), new java.util.Comparator(){
			 

	        public boolean equals(Object obj) {
	          return false;
	        }

	     
	        public int compare(Object o1, Object o2) {
	          OrderLine ol1 = (OrderLine)o1;
	          OrderLine ol2 = (OrderLine)o2;
	          return ol1.getToode().compareTo(ol2.getToode());
	        }
	      });
     
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
      	 sb.append(getLineNr(cou));
         sb.append("          <I07>" + ls);
         cou++;
        
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          double ale = line.getDiscount();
          if (ale == -1 ){
          ale = 0 ;
              }
          sb.append(getLineNr(cou));
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
          cou++;
         
          String ladu = getLadu(agent);
          if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0 ){
        	  ladu = order.getWarehouse().trim();
          }
          
      if(ladu.trim().length()!=0){
	
          sb.append(getLineNr(cou));
          sb.append("               <I07_KODAS_IS>" + ladu  + "</I07_KODAS_IS>" + ls);
          cou++;

       }      


          sb.append(getLineNr(cou));
          sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          cou++;
          
          try{
        	  String partii = this.getPartii(toode, ladu, line.getKogus());
              if(partii != null){
              	  sb.append(getLineNr(cou));
                    sb.append("               <I07_KODAS_OS>" +  escape(partii) + "</I07_KODAS_OS>" + ls);
                    cou++;
              }
          }catch(Exception e){
        	  
          }
          
          
          sb.append(getLineNr(cou));
          sb.append("               <I07_KIEKIS>" + escape(nf.format(line.getKogus())) + "</I07_KIEKIS>" + ls);
          cou++;
         
       //   sb.append(getLineNr(cou));
       //   sb.append("               <I07_KAINA_BE>" + escape(getConc().getNF().format(price)) + "</I07_KAINA_BE>" + ls);
       //   cou++;
  if (ale != 0 ){
  	
          sb.append(getLineNr(cou));
          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          cou++;

  }     
         // sb.append(getLineNr(cou));
         // sb.append("               <I07_MOKESTIS_P>" + prod.getVat() + "</I07_MOKESTIS_P>" + ls);
         // cou++;
          
         // sb.append(getLineNr(cou));
         // sb.append("               <I07_SUMA>" + escape(getConc().getNF().format((line.getAmount()* price)*(100-ale)/100)
          //		)+ "</I07_SUMA>" + ls);
          //cou++;
          

  addNumLine();

          sb.append(getLineNr(cou));
          sb.append("          </I07>" + ls);
          cou++;
      }

      sb.append(getLineNr(cou));
      sb.append("          </I06>" + ls);
      cou++;

 	
      String fileName = escape(parent.getProps().getProperty("conv.dir") + order.getAgent()) + "-" +
      name + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "RivileAutoImport";
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

  
  private String getLineNr(int i){
  	String res = i + "";
  	if (res.length() == 1){
  		res = "00000" + res;
  	} else if (res.length() == 2){
  		res = "0000" + res;
  	} else if (res.length() == 3){
  		res = "000" + res;
  	} else if (res.length() == 4){
  		res = "00" + res;
  	} else if (res.length() == 5){
  		res = "0" + res;
  	}
  	return res;
  }
  
  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    sb.append(cal.get(Calendar.YEAR) + ".");
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month + ".");
//2005.09.05 00:00
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day );

    return sb.toString();
  }

  private String url = "jdbc:odbc:";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
