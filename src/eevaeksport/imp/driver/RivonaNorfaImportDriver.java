
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
import java.util.Enumeration;
import java.util.HashMap;
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

public class RivonaNorfaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivonaNorfaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "norfa");

    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  private Vector splitOrder (Order order){
	  Vector result = new Vector();
	 String doknr = getNextDoknr();
	 if(doknr == null){
		 doknr = getNextDoknr();
	 }
	 if(doknr == null){
		 doknr = getNextDoknr();
	 }
	 if(doknr == null){
		 doknr = getNextDoknr();
	 }
	 
	 if(doknr.length() == 2){
		 doknr = "0000" + doknr;
	 }
	 if(doknr.length() == 3){
		 doknr = "000" + doknr;
	 }
	 if(doknr.length() == 4){
		 doknr = "00" + doknr;
	 }
	 if(doknr.length() == 5){
		 doknr = "0" + doknr;
	 }
	  Order ord01 = new Order(order.getCompany(),order.getAgentFirstName());
	  Order ord05 = new Order(order.getCompany(),order.getAgentFirstName());	
		 
	  
	  ord01.setAgentCode(order.getAgentCode());
	  ord01.setDokNr("UZSVY1" +doknr);
	  ord01.setInfo(order.getInfo());
	  ord01.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord01.setStatus(order.getStatus());
	  ord01.setPaymTerm(order.getPaymTerm());
	  ord01.setChanged(true); // et teaksime et on tegemita tubakaga T t�he lisamiseks
	  ord01.setWarehouse("VY000001");
	  ord01.setTeostaja(order.getDokNr());
	  
	  ord05.setAgentCode(order.getAgentCode());
	  ord05.setDokNr("UZSND1" +doknr);
	  ord05.setInfo(order.getInfo());
	  ord05.setPaymTerm(order.getPaymTerm());
	  ord05.setShipCal(order.getShipCal().getTimeInMillis() +"");
	  ord05.setStatus(order.getStatus());
	  ord05.setWarehouse("ND000001");
	  ord05.setTeostaja(order.getDokNr());
		  
	  	  Vector lines = order.getOrderLines();
	  Enumeration enumr = lines.elements();
	  while  (enumr.hasMoreElements()){
		  OrderLine lin = (OrderLine)enumr.nextElement();
	
		     String itemid = lin.getToode();
		     
		    String ware =  identifyWare(itemid,lin.getKogus());
		    
		    System.out.println("Splitter " + ware );
		    
		     if(ware.equals("VY000001") ){ 		     	    
		    	 	  ord01.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);			 
		     } else if (ware.equals("ND000001")){
		    		 ord05.add(lin.getToode().substring(3), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
		    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
		      }
			     			
			     }
               





	  if(ord01.getOrderLines() != null && ord01.getOrderLines().size()>0){
		  result.add(ord01);  
	  }

	  if(ord05.getOrderLines() != null && ord05.getOrderLines().size()>0){
		  result.add(ord05);  
	  }
	
	 
	 System.out.println("Result =" +order.getDokNr()
			 + " "+ result.size());
	 //System.exit(0);
	  return result ;
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	    sql1 = "SELECT  trim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else trim(str(N33_kodas_ak)) end) AS kood " +
	  " FROM n08_klij  JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS " +
	  " where N08_KODAS_LS_4 = '" + id + "'";	
  	  
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

  public String identifyWare(String id , double kogus)  {
	  	 String res = "VY000001";

		if(id.startsWith("AL-")){
			res = "ND000001";

		}
	   // System.out.println("Klient: " + acc);
	    return res;

	  }
  
  private String  getNextDoknr(){
	  String res = null;
	  	String   sql1 = "select order_nr as numbe from dbo.MR1_DOK_NR where dev_id = 'ANDROID'";	
  	  
    System.out.println(sql1);
  
	try {
		Connection  con = DriverManager.getConnection(url + "norfa");
		Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	res = rs1.getString("numbe");
	    }
	    rs1.close();
	    sql1 = " update  dbo.MR1_DOK_NR set order_nr = order_nr +1,last_update = getdate() where dev_id = 'ANDROID' ";	
	    st1.executeUpdate(sql1);

	   
	    st1.close();
	    con.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return res ;
  }
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
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
 	 String	sql1 = "SELECT N33_KODAS_LS_1 AS aadress FROM n08_klij  JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE " +
 	 		" trim(N08_KODAS_KS)||'-'|| N33_kodas_ak ='" + id +"'"; 
 	
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
  	
    
    String sql1 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean FROM n17_prod a   " +
   	"  WHERE  N17_KODAS_PS = '" + ean.trim().substring(0,ean.trim().length()-1) + "'";

    String sql2 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean " +
    " FROM n17_prod a  ,  n27_pak " +
   	"  WHERE  n27_kodas_ps=n17_kodas_ps  and  N17_KODAS_PS = '" + prodID.trim() + "'";

    
    String sql3 = " SELECT N17_KODAS_PS AS toode ,N17_KOD_T AS ean FROM n17_prod a   " +
   	"  WHERE  N17_KODAS_PS = '" + ean + "'";

    String prod = null;

    try {
    	
    System.out.println(sql1);
    Statement st1 = con.createStatement();  
    ResultSet rs1 = st1.executeQuery(sql1);
  
    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
   System.out.println(ean + " prod0 " + prod);
   

    if(prod == null){

    	   Statement st2 = con.createStatement();
    	   ResultSet rs2 = st2.executeQuery(sql2);
      while (rs2.next()) {
        prod = rs2.getString("toode").trim();
      }
      rs2.close();
      st2.close();
      
    }
    
    
    if(prod == null){

        Statement st3 = con.createStatement();
        ResultSet rs3 = st3.executeQuery(sql3);
        while (rs3.next()) {
          prod = rs3.getString("toode").trim();
        }
        rs3.close();
        st3.close();
    }

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();
    }catch (Exception e){
    	e.printStackTrace();
    }

    return prod;
  }
  private HashMap bcodes =  null;

  public void makeOrder(Order order) throws SQLException{
	    String bc = "select N37_KODAS_PS as prod ,N37_BAR_KODAS as ean  from n37_pmat where N37_BAR_KODAS != N37_KODAS_PS " +
		"and N37_BAR_KODAS!='000000000000' and '0'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS and '00'+ltrim(N37_KODAS_PS)!=N37_BAR_KODAS";
	    bcodes =  new HashMap();
Statement stm = con.createStatement();
 ResultSet rst = stm.executeQuery(bc);

  while (rst.next()) {
    String toode = rst.getString("prod").trim();
    String bar = rst.getString("ean").trim();        
    bcodes.put(toode,bar);	
}
rst.close();
stm.close();

	  
	  Vector splitted =  splitOrder(order);
   Enumeration en = splitted.elements();
	  while(en.hasMoreElements()){
		  Order ord = (Order)en.nextElement();
		    makeOutput(ord);
	  }
	  
  }

 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
    boolean alco = false;
 	if(order.getWarehouse().equals("ND000001")){
 		alco = true;
 	}
nf4.setMaximumFractionDigits(3);
nf4.setGroupingUsed(false);
 	
 	String ls = System.getProperty("line.separator");
 	try {
 	 
      sb.append("       <I06>" + ls);

      int status =  order.getStatus();
    String type = "55";
   
      if(status == 0){
    	  sb.append("          <I06_OP_TIP>" + type + "</I06_OP_TIP>" + ls); 
      }else{
    	  sb.append("          <I06_OP_TIP>"  + type + "</I06_OP_TIP>" + ls);  
      }
      String cust = order.getCompany();
      sb.append("          <I06_DOK_NR>" + escape(order.getDokNr())  + "</I06_DOK_NR>" + ls);
      sb.append("          <I06_KODAS_PO>" + order.getDokNr() + "</I06_KODAS_PO>" + ls);
      sb.append("          <I06_DOK_DATA>" +  getDateString(order.getShipCal()) + "</I06_DOK_DATA>" + ls);
      sb.append("          <I06_OP_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_OP_DATA>" + ls);
      sb.append("          <I06_KODAS_LS_4>" + escape(order.getAgent()) + "</I06_KODAS_LS_4>" + ls);
      sb.append("          <I06_KODAS_MS>" + escape(order.getAgent()) + "</I06_KODAS_MS>" + ls);
      sb.append("          <I06_KODAS_KS>" + escape(cust.substring(0,cust.lastIndexOf("-"))) + "</I06_KODAS_KS>" + ls);
      sb.append("          <I06_KODAS_AK>" + escape(cust.substring(cust.lastIndexOf("-")+1)) + "</I06_KODAS_AK>" + ls);
      sb.append("          <i06_DOK_REG>" + escape(order.getDokNr()) + "</i06_DOK_REG>" + ls);

      String addr =  null;
  	try {
  		addr = identifyCustomerADDR(cust);
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}
       sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
           
     
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
         sb.append("          <I07>" + ls);
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          if(this.bcodes.get(toode) != null ){
        	  toode = (String)bcodes.get(toode) ;
          }
          
          double ale = line.getDiscount();
          if (ale == -1 ){
             ale = 0 ;
              }
          
          String kog  = escape(nf4.format(line.getKogus()));
          double kogus = line.getKogus();
          boolean inte = true;
          if((int)kogus - kogus != 0){
        	  kog = escape(nf4.format(line.getKogus()));
        	  inte = false;
          }
          
          sb.append("               <I07_KODAS_PO>" + order.getDokNr() + "</I07_KODAS_PO>" + ls); 
          sb.append("               <I07_EIL_NR>" + i + "</I07_EIL_NR>" + ls);
     //     sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          sb.append("               <DI07_BAR_KODAS>" +  escape(toode) + "</DI07_BAR_KODAS>" + ls);
          if(inte){
        	//  sb.append("               <I07_KIEKIS>" + kog + "</I07_KIEKIS>" + ls);  
          }else{
        	  //sb.append("               <T_KIEKIS>" + kog + "</T_KIEKIS>" + ls);
          }
         
          sb.append("               <KIEKIS>" + kog + "</KIEKIS>" + ls);
        //  sb.append("               <I07_ALT_KIEKIS>" + kog + "</I07_ALT_KIEKIS>" + ls);
          sb.append("               <I07_KODAS_IS>" + order.getWarehouse() + "</I07_KODAS_IS>" + ls);    
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
       if (ale != 0 ){
          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          }     
       
       
         // sb.append("               <I07_MOKESTIS_P>" + prod.getVat() + "</I07_MOKESTIS_P>" + ls);
         // sb.append("               <I07_SUMA>" + escape(getConc().getNF().format((line.getAmount()* price)*(100-ale)/100)
          //		)+ "</I07_SUMA>" + ls);
          
  /*<I07>

  <I07_KAINA_SU>1.0900</I07_KAINA_SU>
  <I07_NUOLAIDA>0.00</I07_NUOLAIDA>
*/
  

  addNumLine();
          sb.append("          </I07>" + ls);
      }

      sb.append("          </I06>" + ls);
 
 	String num =  "KS" + getNextDoknr();
 	
 	String pdanum = order.getTeostaja();
 	if(pdanum == null || pdanum.length() == 0){
 		pdanum = "";	
 	}
 	
 	 String	sql1 = "   insert into dbo.B00_IMP( B00_KODAS_IM,B00_PAV,B00_MODUL,B00_DATE,B00_OPERACIJA,B00_LOGAS,B00_PERKELTA,B00_ADDUSR,B00_USERIS,B00_R_DATE)" +
 	 		" values( '"+num+"','Importas is Android " + pdanum + " ','RO',current date,'" + sb.toString()+ "'," +
 	 				"'',1,'MASTER','MASTER',DATEFORMAT(getdate(*),'yyyy-mm-dd hh:nn:ss.000'))"; 

System.out.println(sql1);
if(alco){
	  con = DriverManager.getConnection(urlAlco);
}else{
	  con = DriverManager.getConnection(url + "norfa");
}
Statement st1 = con.createStatement();
  st1.executeUpdate(sql1);
  st1.close();

      
   
      
      
      String fileName = escape(parent.getProps().getProperty("conv.dir") + num +"-"+ order.getTeostaja() + "-" +  order.getDokNr() + "-" + order.getAgent())  + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
 public String getImportFile(){
	    return parent.getProps().getProperty("imp.dir");
	  }
 
  public String getDriverVersion(){
    return "RivonaNorfaImport";
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

  
  private String getLineNr2(int i){
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
  private String	       urlAlco = "jdbc:odbc:norfa_nd";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private NumberFormat nf4 = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
