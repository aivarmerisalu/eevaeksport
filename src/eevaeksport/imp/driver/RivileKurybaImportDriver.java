
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
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
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
  
  
  private void addInvoiceLines(String salesid) throws SQLException{

	     Enumeration enums = order.getOrderLines().elements();
	     int counter = 0;
	     while (enums.hasMoreElements()){
	       counter++;
	       log.info("ORDER_LINE_COUNTER:" + counter);
	       OrderLine orderLine = (OrderLine)enums.nextElement();
	       String itemid = orderLine.getToode();
	         double kogus = orderLine.getKogus();
    	     double ale = orderLine.getDiscount();
	         double hind = orderLine.getPrice();
	         String kood = orderLine.getToode();
	         String nimi = orderLine.getTooteNimi();
	        
	         
	       String  sql = "INSERT INTO DBA.i_mobili_uzsakymaid (uzsakymaid_kodas,uzsakymaid_kodas_ps,uzsakymaid_kodas_us,uzsakymaid_kodas_os" +
	       		",uzsakymaid_serija,uzsakymaid_kiekis,uzsakymaid_kaina,uzsakymaid_nuolaida," +
	       		"uzsakymaid_frakcija,uzsakymaid_ivykdyta,uzsakymaid_pasiulymas)" +
	         " VALUES( " +
	           salesid + "," +
	           kood + ",'" +
	           itemid + "'," +
	           kogus + "," +
	           hind +", ?)";

	           log.info("SALES_LINE_SQL: " + sql);
	           PreparedStatement st2 = con.prepareStatement(sql);
	           st2.setString(1, nimi);
	           st2.executeUpdate();
	          
	           addNumLine();
	           st2.close();
	        }  
	     }


	   private void addInvoiceHeader(String salesid) throws SQLException{
	   	
	   	System.out.println("doknr " + order.getDokNr());
	   	String agent = order.getAgent();
	   	System.out.println("agent " +  agent);
    	   String  ladu = order.getWarehouse();
	    if(ladu == null || ladu.equals(" ")){
	    	ladu = "1";
	     }
	  
	   
	   Agent ag = (Agent)parent.getEksportFrame().getEksportPanel().getAgentsHash().get(agent);
	   
      
	          String text = order.getEmailBody();
	          System.out.println("text " + text);
	   		if (text != null && text.trim().length()>0){
	   			text = " " + text.trim().replaceAll("\'", " ");
	   		}
	   		
	   		if(text == null){
	   			text = "";
	   		}
	   		
	   		
	   		String text2 = order.getInfo();
	   		if (text2 != null && text2.trim().length()>0){
	   			text2 = " " + text2.trim().replaceAll("\'", " ");
	   		}
	   		
	   		if(text2 == null){
	   			text2 = "";
	   		}
	   	
	   		
	   		System.out.println("info sisselugemisel " + order.getInfo());
	                 		
	                 		String ref = order.getDokNr();
	                 		if(ref == null || ref.trim().length()==0){
	                 			ref =  "";
	                 		}
	  /*CREATE TABLE DBA.i_mobili_uzsakymai(
	uzsakymai_kodas_im   char(12) NOT NULL PRIMARY KEY,
	uzsakymai_klientas   char(12) NOT NULL,
	uzsakymai_sutartis   char(12) NOT NULL,
	uzsakymai_padalinys  char(12) NOT NULL,
	uzsakymai_r_date     timestamp NOT NULL
)

)*/               		
	                 		

	         String sql = "INSERT INTO i_mobili_uzsakymai (uzsakymai_kodas_im,uzsakymaid_kodas_ps,uzsakymai_sutartis,uzsakymai_padalinys" +
	         		",uzsakymai_r_date ) " +
	              "VALUES (' " + order.getInfo() + "','" + order.getCompany() + "',?,'" + agent + "',?)";

	     log.info("addInvoiceHeader2: " + sql);
	     System.out.println(sql);
	
	     PreparedStatement stmt2 = con.prepareStatement(sql);
	         stmt2.setTimestamp(1, new java.sql.Timestamp(order.getShipCal().getTimeInMillis())); //lahtine mis see on 
	         stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
	         stmt2.executeUpdate();
	         stmt2.close();
	         addLog("lisatud arve p�is -> " + order.getCompany() + "\n");
	
	   }

	   private void addLog(String line){
		     parent.addLog(line);
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
      
      
  	sb.append(getLineNr(cou));
    sb.append("          <I06_KODAS_AK>" + escape(cust.substring(cust.lastIndexOf("-")+1)) + "</I06_KODAS_AK>" + ls);
    cou++;
    
    
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

    if(order.getShipMode() != null && order.getShipMode().trim().length() > 0){
    	
    	String cod = "";
    	String shipm = order.getShipMode();
    	if(shipm.contains("|")) {
    		cod = "-COD";
    		shipm = shipm.substring(0,shipm.indexOf("|"));
    	}
    	
    	sb.append(getLineNr(cou));
        sb.append("          <I06_PASTABOS>" + order.getAgent() + "-" + order.getDokNr() + "-"+ shipm + cod+  "</I06_PASTABOS>" + ls);
        cou++;
        sb.append(getLineNr(cou));
        sb.append("          <I06_DOK_PR>" + order.getDokNr() + cod + "</II06_DOK_PR>" + ls);
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
