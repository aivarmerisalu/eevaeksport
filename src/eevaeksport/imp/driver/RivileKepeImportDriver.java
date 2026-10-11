 
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportFrame;
import eevaeksport.Email;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class RivileKepeImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileKepeImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "lk","","");

    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }

  
  
  
  private String  getNextDoknr(){
	  String res = null;
	  	String   sql1 = "select N49_BEG_N+1 as number from N49_SKDK  where N49_BEG_S = 'UZS'";	
  	  
    System.out.println(sql1);
  
	try {
		Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	res = rs1.getString("number");
	    }
	    rs1.close();
	    
	    sql1 = " update N49_SKDK set N49_BEG_N = N49_BEG_N +1  where N49_BEG_S = 'UZS' ";	
	    st1.executeUpdate(sql1);

	    st1.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}
	  return "UZS00" + res ;
  }
  
HashMap pants = null;

  public String getPandiPakend(String toode) {
	
	  
	  if(pants == null) {
		  pants = new HashMap();
		    String sql1 = " SELECT N26_KODAS_PS as toode , RTRIM(N26_KODAS_PS_K) AS pant FROM N26_KOMP  where N26_KODAS_PS_K not like 'MI%' ";
		   
		    try {
		    	
		    	Statement st1 = con.createStatement();
		   	    ResultSet rs1 = st1.executeQuery(sql1);
		   	    while (rs1.next()) {
		   	    	
		   	     String tood  = rs1.getString("toode").trim();
		   	     String pant  = rs1.getString("pant").trim();
		   	         pants.put(tood, pant);
		   	  
		   	    }
		   	       
		   	    rs1.close();
		   	    st1.close();	

		    }catch(Exception e) {
		    	e.printStackTrace();
		    } 
	  }
	  
	  
	 
	    return (String)pants.get(toode);
	 }
	  
  
  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	    sql1 = "SELECT  ltrim(N08_KODAS_KS) AS kood1, (case when (N33_EIL_NR) is null then '1' else ltrim(str(N33_EIL_NR)) end) AS kood " +
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
  
  public String identifyCustomerVS(String id) throws SQLException {
	  	 String	sql1 = "SELECT n33_kodas_vs AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
	  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
	  	
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

  public String identifyCustomerName(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_PAV AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
	  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
	  	
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
  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
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
 	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
 	
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
	     
	   
	     
	  boolean tag = false;
	  OrderLine ol = (OrderLine)order.getOrderLines().get(0);
	  if(ol.getKogus() < 0) {
		  tag = true;
	  }
	  
	  System.out.println("On Tagastus " + tag );
    makeOutput(order,tag);
  }
  
  



 private void makeOutput(Order order,boolean tag){
 	StringBuffer sb = new StringBuffer();
   
	 Vector lines = order.getOrderLines();
    
 	boolean norm = true;
	 	 	OrderLine line = (OrderLine)lines.elementAt(0);
            Product prod = getProduct(line.getToode());
            if(prod != null && (prod.getGroup().equals("BAKEOFF") || prod.getGroup().equals("RETAIL"))) {
          	 norm = false;
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
      sb.append("          <I06_KODAS_MS>" + escape(agent) + "</I06_KODAS_MS>" + ls);
      cou++;
      
      if(norm) {
    	  sb.append(getLineNr(cou));
          sb.append("          <I06_PASTABOS>" + escape(agent) + "</I06_PASTABOS>" + ls);
          cou++;	  
      }
    
      String name =  null;
      String vskodas =  null;
  	try {
  		
  		
  		name = identifyCustomerName(cust);
  		if(name == null){
  			name ="nonam";
  		}else{
  			name = name.replaceAll("UAB", " ").trim();
  	  		if(name.length()>5){
  	  			name = name.substring(0,5);
  	  		}	
  		}
  		
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}
  	
	try {
  		
  		
  		vskodas = identifyCustomerVS(cust);
  		
  		
  	} catch (SQLException e1) {
  		e1.printStackTrace();
  	}
	
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.DAY_OF_YEAR, 1);
	if(order.isShipDateUsed()) {
		cal.setTimeInMillis(order.getShipCal().getTimeInMillis());
	}

      sb.append(getLineNr(cou));
     // sb.append("          <I06_DOK_NR>U" +  escape(agent)+ "-" + name + "-"  +  escape(order.getDokNr())  + "</I06_DOK_NR>" + ls);
      sb.append("          <I06_DOK_NR>" + this.getNextDoknr() + "</I06_DOK_NR>" + ls);
    
      cou++;
      

      sb.append(getLineNr(cou));
      sb.append("          <I06_OP_DATA>" +  getDateString(cal) + "</I06_OP_DATA>" + ls);
      cou++;
      
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_DATA>" +  getDateString(cal) + "</I06_DOK_DATA>" + ls);
      cou++;
  
      sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_KS>" + escape(cust.substring(0,cust.lastIndexOf("-"))) + "</I06_KODAS_KS>" + ls);
      cou++;

	sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_AK>" + escape(cust.substring(cust.lastIndexOf("-")+1)) + "</I06_KODAS_AK>" + ls);
      cou++;
      
      
      if(vskodas != null) {
    	  sb.append(getLineNr(cou));
          sb.append("          <I06_KODAS_VS>" + vskodas + "</I06_KODAS_VS>" + ls);
          cou++;  
      }
      
      
      
      
      String addr =  null;
	try {
		addr = identifyCustomerADDR(cust);
	} catch (SQLException e1) {
		e1.printStackTrace();
	}
	sb.append(getLineNr(cou));
      sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
      cou++;
      
     
    lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
      	 sb.append(getLineNr(cou));
         sb.append("          <I07>" + ls);
         cou++;
        
          
       line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
           prod = getProduct(line.getToode());
          
          double ale = line.getDiscount();
          if (ale == -1 ){
          ale = 0 ;
              }
          if(ale == -1 ){
        	  ale = 100;
          }
          
          sb.append(getLineNr(cou));
          sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
          cou++;
          /*
          Fillial 01   -  agent 007
Fillial 02   -  agent 020
Fillial 03    - agent 016*/
          String ladu = "";
          
          if(prod != null && (prod.getGroup().equals("BAKEOFF") || prod.getGroup().equals("RETAIL"))) {
        	  ladu = "920";
          }
          
          if(prod != null && prod.getMainWarehouseStock() != null && 
        		  prod.getMainWarehouseStock().trim().length() > 2) {
        	  ladu = prod.getMainWarehouseStock().trim();
        	  
        	  if(order.getWarehouse() != null && order.getWarehouse().trim().length() > 0) {
            	  ladu = order.getWarehouse();
              }
          }
          
         
          
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
	  
	  sb.append(getLineNr(cou));
         sb.append("               <I07_KAINA_BE>" + escape(nf.format(line.getPrice())) + "</I07_KAINA_BE>" + ls);
         cou++;
  	
          //sb.append(getLineNr(cou));
          //sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
          //cou++;

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

 	
      String fileName = escape(parent.getProps().getProperty("conv.dir") + order.getAgent()) + "-" + order.getCompany() + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

       // String fname = createXMLFile(order,tag, pandikas ,addr);
        
      // System.out.println("XML created " + fname);
      //  this.sendOrderEmail(order, tag, pandikas);
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "RivileBalmerk";
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

  
  public Product getProduct(String toode){
   Product prod = null;
 String cmd = " SELECT N17_KODAS_MS as ladu, N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus,1  AS tykke,1  AS alusel, 0 AS myygihind," +
 		"N37_BAR_KODAS AS ean, "+
 " 0 AS kogus,n17_kodas_ls_1 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
 " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
 " FROM n17_prod a LEFT JOIN  n37_pmat  ON n37_kodas_ps=n17_kodas_ps  and N37_KODAS_US = N17_KODAS_US LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1 where  N17_POZ_DATE !=1" +
 "  and  N17_KODAS_PS = '" + toode +"'" ;
   
          System.out.println(cmd);

      try {
      	
    	  Statement st1 = con.createStatement();
    	    ResultSet rs = st1.executeQuery(cmd);

          while (rs.next()) {
        	  String ladu = rs.getString("ladu");
            String kood = rs.getString("toode").trim();
            prod = new Product(kood);
            if(ladu != null && ladu.trim().length() > 0) {
            	prod.setMainWarehouseStock(ladu);
            }
            prod.setUnit("VNT");
            String nimetus = rs.getString("nimetus").trim();
            prod.setName(Converter.enCodeEst(nimetus));
            try{
            prod.setInCases(rs.getDouble("tykke"));
          } catch (Exception e){}
            
            try{
                prod.setOnPallet(rs.getDouble("alusel"));
              } catch (Exception e){}
     
            String ean = rs.getString("ean");
            if(ean != null) {
            	
            if( ean.trim().length()>11 && !ean.startsWith("0")){
          	  try{
          		  EAN13CheckDigit ee = new EAN13CheckDigit();
          		  String ean2 = ean.trim() + ee.calculate(ean.trim());
                    prod.setEAN(ean2);	
          		  prod.setName(prod.getName() + " " + ean2.substring(ean2.length()-4));
          	  }catch(Exception y){
          		  
          	  }
            }
            }
        
            String grupp = rs.getString("grupp");
           if (grupp != null && grupp.trim().length()>0){
             prod.setGroup(grupp.trim());
           }
         

            
             prod.setVat("21");
             
            

          }
          rs.close();
          st1.close();
      } catch (Exception e) {
          e.printStackTrace();
          }

      return prod;
  }

  

 



  private String url = "jdbc:odbc:";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

  public NumberFormat getNF() {
		nf.setGroupingUsed(false);
		return this.nf;
	}
  
  
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
