
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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

public class RivileKurybaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileKurybaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "kuryba","sa","sql");

    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  
  public String identifyCustomerName(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_PAV AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
	  	 		"  rtrim(ltrim(N08_KODAS_KS))= '" + id +"'"; 
	  	
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

  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  rtrim(N08_KODAS_KS)+'-'+ ltrim(rtrim(N33_kodas_ak)) = '" + id +"'"; 
  	
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
  
  
  private Double getMinSum()  throws SQLException {
	   
	  
	  String	sql1 = "  	SELECT [N31_MINIMUM] as summa FROM [N31_NUOD] where"
	  		+ " N31_KODAS_NS = 'nuol  nuol' and N31_NUOL_PROC = 0 "; 
	  	
	    System.out.println(sql1);
	    Double acc = 0d;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    if (rs1.next()) {
	    	
	    	Double summa = rs1.getDouble("summa");
	    	
	    	if(summa > 0) {
	    		 acc = summa; 
	    	}
	   	    }
	    
	    rs1.close();
	    st1.close();
	    System.out.println("minSum0 " + acc);
	    return acc;

}
  
  private TreeMap<String , Double> getTransaprice()  throws SQLException {
	   
		  
		  String	sql1 = "SELECT [N35_KODAS_LS] as kood ,[N35_PAV_K1] as summa " + 
		  		"		  FROM [N35_LGST]  where LEFT(n35_tevas,2) = '01'"; 
		  	
		    System.out.println(sql1);
		    TreeMap<String , Double> acc = new TreeMap<String , Double>();

		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	String kood = rs1.getString("kood").trim();
		    	Double summa = rs1.getDouble("summa");
		    	
		    	if(summa > 0) {
		    		 acc.put(kood, summa ); 
		    	}
		     

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
    if(this.minSum < 1) {
    	minSum = getMinSum();
       }
    System.out.println("minSum " + minSum );
	  setOrder(order);
	  makeOutput(order);
  }
  
 


	

	   private void addLog(String line){
		     parent.addLog(line);
		   }
  
	   private double minSum = -1; 

 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	TreeMap<String , Double> trprices = null;
 	boolean less = false;
 	System.out.println("minsum " + minSum + " " + order.getCalcSum());
 	if(order.getCalcSum() *1.21 < minSum ) {
 		less = true;
 		try {
 			 trprices = getTransaprice();
 		} catch (SQLException e2) {
 			e2.printStackTrace();
 		}
 	}
 	
 	String ls = System.getProperty("line.separator");
 	try {
 	  int cou = 1;
      sb.append(getLineNr(cou));
      sb.append("       <I06>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_OP_TIP>53</I06_OP_TIP>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);
      cou++;
      String cust = order.getCompany();

      
      String agent = order.getAgent();
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_ATSTOVAS>" + escape(agent) + "</I06_ATSTOVAS>" + ls);
      cou++;


      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_NR>U-" +  escape(agent)+ "-" +  escape(order.getDokNr())  + "</I06_DOK_NR>" + ls);
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
      
      
      String acode = escape(cust.substring(cust.lastIndexOf("-")+1));
      
      if(!acode.equals("1")) {
    	  sb.append(getLineNr(cou));
    	    sb.append("          <I06_KODAS_AK>" + acode + "</I06_KODAS_AK>" + ls);
    	    cou++;
    	     
      }
  	
    
      String addr =  null;
	try {
		addr = identifyCustomerADDR(cust);
		if(addr != null){
			//sb.append(getLineNr(cou));
		      //sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
		      //cou++;	
		}
	
	} catch (SQLException e1) {
		e1.printStackTrace();
	}
	
     System.out.println("shipmode " + order.getShipMode());
     String shipm = null;
     if(order.getShipMode() != null && order.getShipMode().trim().length() > 0){
    	
    	String cod = "";
    	shipm = order.getShipMode();
    	String money = "";
    	
    	if(shipm.contains("|")) {
    		cod = "-COD";
    		money = shipm.substring(shipm.indexOf("|")+4);
    		shipm = shipm.substring(0,shipm.indexOf("|"));
    	}
    	  System.out.println("shipmode2 " + money);
    	  
    	sb.append(getLineNr(cou));
        sb.append("          <I06_PASTABOS>" + order.getAgent() + "-" + order.getDokNr() + "-"+ shipm + cod+  "</I06_PASTABOS>" + ls);
        cou++;
        sb.append(getLineNr(cou));
        sb.append("          <I06_DOK_PR>" + order.getDokNr() + cod + "</I06_DOK_PR>" + ls);
        cou++;
        sb.append(getLineNr(cou));
        sb.append("          <I06_KODAS_LS_1>" + shipm + "</I06_KODAS_LS_1>" + ls);
        cou++;
        sb.append(getLineNr(cou));
        sb.append("          <I06_APRASYMAS3>" + money + "</I06_APRASYMAS3>" + ls);
        cou++;
    }
	
	/*
	<I06_PASTABOS>[DATA1]</I06_PASTABOS>
	<I06_DOK_PR>[DATA2]</I06_DOK_PR>

	Specifics of those data fields are different:
	DATA1 - char[40] (syntax: OrderNo-Carrier-COD)
	DATA2 - char[20] (syntax: OrderNo-COD)

	Examples with COD:
	DATA1 = vadyba2-123456-venipak-COD
	DATA2 = 123456-COD

	Examples without COD:
	DATA1 = vadyba2-123456-venipak
	DATA2 = 123456 
*/
	
	
     
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
          String ladu = "";
          
if(ladu.trim().length()!=0){
	
          sb.append(getLineNr(cou));
          sb.append("               <I07_KODAS_IS>" + ladu  + "</I07_KODAS_IS>" + ls);
          cou++;

}      



          sb.append(getLineNr(cou));
          sb.append("               <I07_KODAS>" +  escape(toode) + "</I07_KODAS>" + ls);
          cou++;
          
          sb.append(getLineNr(cou));
          sb.append("               <I07_KIEKIS>" + escape(nf.format(line.getKogus())) + "</I07_KIEKIS>" + ls);
          cou++;
         
        
  if (ale != 0 ){
  	
	  
	  if(ale == -2){
		  sb.append(getLineNr(cou));
          sb.append("               <I07_KAINA_BE>" + escape(nf.format(0.01)) + "</I07_KAINA_BE>" + ls);
          cou++;
    	  sb.append(getLineNr(cou));
          sb.append("               <i07_aprasymas2>akcija</i07_aprasymas2>" + ls);
          cou++;
          
          
          
	  } else if(ale == -4){
		  sb.append(getLineNr(cou));
          sb.append("               <I07_NUOLAIDA>100</I07_NUOLAIDA>" + ls);
          cou++; 
          sb.append(getLineNr(cou));
          sb.append("               <i07_aprasymas2>" + line.getPrice() + "</i07_aprasymas2>" + ls);
          cou++; 
          
         // <i07_aprasymas2>product price before 100% discount</i07_aprasymas2>
         // <i07_nuolaida>100</i07_nuolaida>

          
	  }else{
		   sb.append(getLineNr(cou));
	          sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
	          cou++;  
	  }
       

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

      System.out.println("minsun " +  less + " " + minSum + " " + shipm);
      
      if(less && shipm != null && trprices != null && trprices.get(shipm) != null) {
    	 sb.append(getLineNr(cou));
    	 sb.append("<I07>" + ls);
    	 cou++;
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_TIPAS>2</I07_TIPAS>" + ls);
    	 cou++;
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_KODAS>5001</I07_KODAS>" + ls);
    	 cou++;
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_KIEKIS>1</I07_KIEKIS>" + ls);
    	 cou++;
    	 
    	 double price = Converter.Round((trprices.get(shipm)).floatValue() /1.21f, 4);
    	 double vatprice  = Converter.Round((trprices.get(shipm)).floatValue() , 4);
    	 double vat = vatprice-price;
    	 double lineSum = Converter.Round((trprices.get(shipm)).floatValue() /1.21f + 0.001f, 2);
    	 
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_KAINA_BE>" +   Converter.Round((float)price ,4) + "</I07_KAINA_BE>"+ ls);
    	 cou++;
    	 
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_KAINA_SU>" +  Converter.Round((float)vatprice ,4) + "</I07_KAINA_SU>"+ ls);
    	 cou++;
    	 
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_PVM>" +  Converter.Round((float)vat ,2)+ "</I07_PVM>"+ ls);
    	 cou++;
    	 
    	 sb.append(getLineNr(cou));
    	 sb.append(" <I07_SUMA>" + Converter.Round((float)(price+0.001) ,2)  + "</I07_SUMA>"+ ls);
    	 cou++;
    	
  
    	 
    	 sb.append(getLineNr(cou));
    	 sb.append("</I07>" + ls);
    	 cou++;
      }
      
      
      
      sb.append(getLineNr(cou));
      sb.append("          </I06>" + ls);
      cou++;

      String name =  null;
    	try {
    		name = identifyCustomerName(cust.substring(0,cust.lastIndexOf("-")));
    		name = name.replaceAll("UAB", " ").trim();
    		if(name.length()>8){
    			name = name.substring(0,8);
    		}
    	} catch (SQLException e1) {
    		e1.printStackTrace();
    	}
      
 	
      String fileName = escape(parent.getProps().getProperty("conv.dir") + order.getAgent()) + "-"+ name + "-" +( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "RivileCeryxImport";
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
