
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class RivileHonestasImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileHonestasImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "rivile","","");

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
	  	 String	sql1 = "SELECT N08_PAV AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
	  	 		"  trim(N08_KODAS_KS)||'-'|| N33_kodas_ak = '" + id +"'"; 
	  	
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
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  rtrim(N08_KODAS_KS) + '-' + N33_kodas_ak = '" + id +"'"; 
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
 	 String	sql1 = "SELECT n08_kodas_ms_p AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
 	 		"  rtrim(N08_KODAS_KS) + '-' + N33_kodas_ak = '" + id +"'"; 
 	
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
  
  private String removeFirstZero(String res){
	  int ind = res.indexOf("0"); 
	  if(ind != -1){
		  res = res.substring(0,ind) + res.substring(ind+1);
	  }
	  return res ;
  }

  public String getPartii(String toode , String ladu , double kogus) throws SQLException {
	 	 String	sql1 = " SELECT  I17_SERIJA AS partii " +
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
  
  
	   private void addLog(String line){
		     parent.addLog(line);
		   }
  

 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	StringBuffer sb2 = new StringBuffer();
    
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
      sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);
      cou++;
      String cust = Converter.deCode(order.getCompany());

      
      String agent = order.getAgent();
      String ag2 = agent;
      
      try{
    	  ag2 =  identifyCustomerAGENT(cust);
      }catch(Exception e){
    	  
      }
     
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_MS>" + escape(ag2) + "</I06_KODAS_MS>" + ls);
      cou++;
  
      
 //  if(HNS4000656)
	   String doknr = order.getDokNr();
	   if(doknr.length() > 9){
		   doknr = removeFirstZero(order.getDokNr());
	   }

      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_NR>" + escape(doknr)  + "</I06_DOK_NR>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_REG>" + escape(doknr)  + "</I06_DOK_REG>" + ls);
      cou++;
      
      String auto = agent;
      if(auto.equals("05")){
    	  auto = "BFS 587";
      }
      if(auto.equals("04")){
    	  auto = "COR 389";
      }
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_AU>" +  escape(auto)   + "</I06_KODAS_AU>" + ls);
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
      
      String addr =  null;
	try {
		addr = identifyCustomerADDR(cust);
	} catch (SQLException e1) {
		e1.printStackTrace();
	}
	sb.append(getLineNr(cou));
      sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
      cou++;
    
    
     
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
          /*
          Fillial 01   -  agent 007
Fillial 02   -  agent 020
Fillial 03    - agent 016*/
          String ladu = agent;
          
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
              if(partii != null && partii.trim().length() > 0){
              	  sb.append(getLineNr(cou));
                    sb.append("               <I07_SERIJA>" +  escape(partii.trim()) + "</I07_SERIJA>" + ls);
                    cou++;
                    System.out.println("Seeria " + partii);
              }
              
          }catch(Exception e){
        	  
          }
        

          
          sb.append(getLineNr(cou));
          sb.append("               <I07_KIEKIS>" + escape(nf.format(line.getKogus())) + "</I07_KIEKIS>" + ls);
          cou++;
         
         sb.append(getLineNr(cou));
          sb.append("               <I07_KAINA_BE>" + escape(nf.format(line.getPrice())) + "</I07_KAINA_BE>" + ls);         
          cou++;
  if (ale != 0 ){
  	
          sb.append(getLineNr(cou));
          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          cou++;

  }   
  

  sb2.append(ag2 + ";" + agent + ";" + escape(order.getDokNr()) + ";" + getDateString(Calendar.getInstance()) 
  		  + ";" + escape(cust.substring(0,cust.lastIndexOf("-"))) +";" + escape(addr)  +";" + ladu +";" + toode +";" + (line.getKogus()+ "").replace('.', ',') +";" + (line.getPrice()+ "").replace('.', ',') +"\n");

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

 	
      String fileName = escape(parent.getProps().getProperty("conv.dir") + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(4,13)   +  ".EIP";
        if(order.getDokNr().toLowerCase().startsWith("f")){
        	fileName = escape(parent.getProps().getProperty("conv.dir2") + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(4,13)   +  ".txt";
        	 FileOutputStream fos = new FileOutputStream(fileName);
             fos.write(sb2.toString().getBytes());
             fos.close();
        }else{
        	 FileOutputStream fos = new FileOutputStream(fileName);
             fos.write(sb.toString().getBytes());
             fos.close();
        }
      System.out.println(fileName);
       

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "RivileSalcioImport";
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
