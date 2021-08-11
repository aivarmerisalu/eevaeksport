
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

public class RivileMomentinImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileMomentinImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "rivile2","","");

    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
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
	     
	     String salesid = null ;
	     
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
   
	Hashtable pandikas =  new Hashtable();
	
	 Vector lines = order.getOrderLines();
     for (int i = 0; i < lines.size(); i++) {
    	    
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          String pant = getPandiPakend(toode);
          if(pant != null ){
        	  double aaa = line.getKogus();
         	  if(pandikas.get(pant) == null){
         		  pandikas.put(pant,aaa+"");
               } else {
               	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+(aaa))+"");
               }
            }
         }
  
     
	
	
	
 	
 	String ls = System.getProperty("line.separator");
 	try {
 	  int cou = 1;
      sb.append(getLineNr(cou));
      sb.append("       <I06>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_OP_TIP>51</I06_OP_TIP>" + ls);
      cou++;

      sb.append(getLineNr(cou));
      sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);
      cou++;
      String cust = order.getCompany();

      
      String agent = order.getAgent();
      
      sb.append(getLineNr(cou));
      sb.append("          <I06_KODAS_MS>" + escape(agent) + "</I06_KODAS_MS>" + ls);
      cou++;
      String name =  null;
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

      sb.append(getLineNr(cou));
      sb.append("          <I06_DOK_NR>U" +  escape(agent)+ "-" + name + "-"  +  escape(order.getDokNr())  + "</I06_DOK_NR>" + ls);
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
      
     
    lines = order.getOrderLines();
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

      
      if( pandikas.size()>0){
      	Enumeration enumpant = pandikas.keys();
       while(enumpant.hasMoreElements()){
       	
       	String toode = (String)enumpant.nextElement();
      	double pand = new Double((String)pandikas.get(toode)).doubleValue();
    	  
   	 sb.append(getLineNr(cou));
     sb.append("          <I07>" + ls);
     cou++;
    
      sb.append(getLineNr(cou));
      sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
      cou++;

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
      sb.append("               <I07_KIEKIS>" + escape(nf.format(pand)) + "</I07_KIEKIS>" + ls);
      cou++;
     
      sb.append(getLineNr(cou));
      sb.append("          </I07>" + ls);
      cou++;
   	  }
      }
      
      sb.append(getLineNr(cou));
      sb.append("          </I06>" + ls);
      cou++;

 	
      String fileName = escape(parent.getProps().getProperty("conv.dir") + order.getAgent()) + "-" + name + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

        String fname = createXMLFile(order,tag, pandikas ,addr);
        
        System.out.println("XML created " + fname);
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
 String cmd = " SELECT N17_KODAS_PS AS toode,ltrim(N17_PAV)  AS nimetus,dbo.papn(L11_LAUKAS , 8, 'c')  AS tykke,dbo.papn(L11_LAUKAS , 9, 'c')  AS alusel, 0 AS myygihind," +
 		"N37_BAR_KODAS AS ean, "+
 " 0 AS kogus,n17_kodas_ls_5 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
 " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
 " FROM n17_prod a LEFT JOIN  n37_pmat  ON n37_kodas_ps=n17_kodas_ps  and N37_KODAS_US = N17_KODAS_US LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1 where  N17_POZ_DATE !=1" +
 "  and  N17_KODAS_PS = '" + toode +"'" ;
   
          System.out.println(cmd);

      try {
      	
    	  Statement st1 = con.createStatement();
    	    ResultSet rs = st1.executeQuery(cmd);

          while (rs.next()) {
            String kood = rs.getString("toode").trim();
            prod = new Product(kood);
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

  

  private void sendOrderEmail(Order ord , boolean ret , Hashtable pandikas){
	  
			          
			        try {
	    	 
	  String address = parent.getProps().getProperty("logistmail");//aivar@mobilejava.ee" ;//"mediato.clients@edi.sis.lt" ;//agent.getEmail();
	//  address =(props.get(ord.getAgent())+"");"
//	  System.out.println("määratud email " + emai + " " + props.toString());
	  String cusname = this.identifyCustomerName(ord.getCompany());
	  
	  if(address != null ){
		  Email email = new Email("Order from Momentin: " + order.getCompany() + " " + cusname);
          email.setFrom(parent.getProps().getProperty("from"));
          email.setSMTP(parent.getProps().getProperty("smtphost"));
          email.setUser(parent.getProps().getProperty("smtpuser"));
          email.setPass(parent.getProps().getProperty("smtppass"));
          email.setPort(parent.getProps().getProperty("smtpport"));
          email.setSubjectFilesTo("Order info : " + cusname);
          
          email.setEncoding("iso-8859-1");
          try {
        	  
        	email.setBodyHtml("Order from Momentin");
        	String fname = createXMLFile(ord,ret, pandikas,null);
            email.addAttach(parent.getProps().getProperty("exceldir") + fname);
            StringTokenizer st =  new StringTokenizer(address,";");
            String aadr = null;
            while(st.hasMoreTokens()){
            	 aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              email.send();
              }
                		
            
            File ff = new File(parent.getProps().getProperty("exceldir") + fname);
            if(ff.exists()) {
         //   	File f1  = new File(parent.getProps().getProperty("exceldir")+ "sent/"+ fname);
           // boolean renamed	= ff.renameTo(f1);
          //  System.out.println("ff " + renamed + " " + ff.getAbsolutePath() + " " + f1.getAbsolutePath()  );
           // log.error("sent renamed "  + renamed + " " + ff.getAbsolutePath() + " " + f1.getAbsolutePath());
            
            }
            
            if(ret) {
            	
          
            	 email = new Email("RETURN from Momentin: " + order.getCompany() + " " + cusname);
                 email.setFrom(parent.getProps().getProperty("from"));
                 email.setSMTP(parent.getProps().getProperty("smtphost"));
                 email.setUser(parent.getProps().getProperty("smtpuser"));
                 email.setPass(parent.getProps().getProperty("smtppass"));
                 email.setPort(parent.getProps().getProperty("smtpport"));
                 email.setSubjectFilesTo("Order info : " + cusname);
                 email.setEncoding("iso-8859-1");
                 
                 
                 String text = "\nReturn :" +  ord.getDokNr() + " \nCustomer code :" + ord.getCompany() 
                     +  "\nCustomer Name :" + this.identifyCustomerName(ord.getCompany()) + "\n\n" ;
                 
                Vector lines = ord.getOrderLines();
                for(int i = 0;i <lines.size();i++) {
                	OrderLine line = (OrderLine)lines.elementAt(0);
                	 Product prod = this.getProduct(line.getToode());
                  String lin = "[" + line.getKogus() + "] " +  prod.getID() + " " + prod.getName() + " " + prod.getEAN() + "\n";	
                  text = text + lin;
                }
                 
                 
                email.setBody(text);
		  	  	email.setSendNotice(false);
                aadr = new InternetAddress("droid.lit@momentingroup.com").toString();
                email.setFilesTo(aadr);
                email.send();
            
            
            
            }
            
            
            
            
		} catch (AddressException e) {
		log.error(e);
			e.printStackTrace();
			System.exit(0);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e);
			System.exit(0);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e);
			System.exit(0);
		}
		  		  
	  }
			    } catch (Exception e) {
					e.printStackTrace();
					log.error(e);
					System.exit(0);
			    }
				
  }
  


  private String createXMLFile(Order order , boolean ret, Hashtable pandikas , String addr){
      
		 boolean gift = (order.getDealer() != null && order.getDealer().equals("gift"));
		 boolean brakis =  (order.getDealer() != null && order.getDealer().equals("GPP-VT-BR"));
		 System.out.println("gift or brakis " + gift  + " " + brakis );
		 long uni = Calendar.getInstance().getTimeInMillis();
		 	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    String reason = "100";
		      try {
		    	
		        DocumentBuilder db = dbf.newDocumentBuilder();
		        Document doc = db.newDocument();
		        Element root = doc.createElement("Document-Order");
		        doc.appendChild(root);

		  //      String filiaalStr = null;
		    //    String tarnijaKoodStr = null;

		        boolean shipmodeown  = false ;
                boolean noinvoice = false; 
		        
		        if(order.getShipMode() != null && order.getShipMode().trim().length() > 0) {
		        	String uu =  order.getShipMode();
		        	
		        	if(uu.contains("-3")) {
		        		noinvoice = true;
		        	}
		        	
		        	if( uu.contains("M")) {
		        		shipmodeown = true;
		        	}
		        	
		        }
		        
		        Node docheader = doc.createElement("Document-Header");
		        root.appendChild(docheader);
		        
		        Node un2 = doc.createElement("DocumentType");
		        Text unVal2 = doc.createTextNode("MeriSoft");
		        un2.appendChild(unVal2);
		        docheader.appendChild(un2);
		       
		        
		        Node un3 = doc.createElement("DocumentSource");
		        Text unVal3 = doc.createTextNode("S");
		        un3.appendChild(unVal3);
		        docheader.appendChild(un3);
		        try{

			        long ll = Long.parseLong(order.getAgentCode());
			        Node un4 = doc.createElement("DateCreated");
			        
			        Calendar cal = Calendar.getInstance();
			        cal.setTime(new java.util.Date(ll));

			        String month = (cal.get(Calendar.MONTH) + 1) + "";
			        if (month.length() == 1) month = "0" + month;
			        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
			        if (day.length() == 1) day = "0" + day;

			        String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
			        if (hour.length() == 1) hour = "0" + hour;
			        String minute = cal.get(Calendar.MINUTE) + "";
			        if (minute.length() == 1) minute = "0" + minute;
			        
			        String second = cal.get(Calendar.SECOND) + "";
			        if (second.length() == 1) second = "0" + second;
			        
			        //[ HH:MM:SS]
			        
			        String dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day + "[" + hour + ":" + minute + ":" + second + "]";
			        
			        Text unVal4 = doc.createTextNode(dateString);
			        un4.appendChild(unVal4);
			        docheader.appendChild(un4);
			        
		        }catch(Exception e){
		        	
		        }
		        
			

		        String number = order.getDokNr();
		       
		        
		        if(number == null || number.length() == 0){
		        	number = (Calendar.getInstance().getTimeInMillis()+"");
		        }else{
		        	 String add = (Calendar.getInstance().getTimeInMillis()+"").substring(10);
		        	 number = number+ add;
		        }
		        
		        
		        
	    
	     Node header = doc.createElement("Order-Header");
		        
		        
		        Node nr = doc.createElement("OrderNumber");
		        Text nrVal = doc.createTextNode("MS-" +  number + "");
		        nr.appendChild(nrVal);
		        header.appendChild(nr);
		        
		        Node age = doc.createElement("SalesAgent");
		        Text ageVal = doc.createTextNode(order.getAgent());
		        age.appendChild(ageVal);
		        header.appendChild(age);
		        
		        

		        
		       // "Adl PaymTerm Code '90D'"
		        System.out.println("Paym term " + order.getPaymTerm());
		        if(order.getPaymTerm() == -2){
		        	
		        	Node pt = doc.createElement("PaymentTerm");
			        Text ptVal = doc.createTextNode("90D");
			        pt.appendChild(ptVal);
			        header.appendChild(pt);
			        
			        Node pl = doc.createElement("OrderType");
			        Text plVal = doc.createTextNode("realizatsija");
			        pl.appendChild(plVal);
			        header.appendChild(pl);
			    
		        
		        }
		        
		        if(ret) {
		            Node pl = doc.createElement("OrderType");
			        Text plVal = doc.createTextNode("return");
			        pl.appendChild(plVal);
			        header.appendChild(pl);    	
		        }else {
		            Node pl = doc.createElement("OrderType");
			        Text plVal = doc.createTextNode("order");
			        pl.appendChild(plVal);
			        header.appendChild(pl);
		        }
		    
		        
		       System.out.println("is Gift Sampli jaoks " + gift + " " + ((OrderLine)order.getOrderLines().get(0)).docType); 
		        
		        if(gift){
		        	String samtype = ((OrderLine)order.getOrderLines().get(0)).docType;
		        	if(samtype == null || samtype.trim().length() == 0 || samtype.trim().equals("0")){
		        		samtype = "PB_SALmerisoft";
		        	}
		        	Node samType = doc.createElement("SampleType");
			        Text samTypeVal = doc.createTextNode(samtype);
			        samType.appendChild(samTypeVal);
			        header.appendChild(samType);	
		        }
		        
		        
		        
	         if(order.getPaymTerm() > 29){
		        	
		        	Node pt = doc.createElement("PaymentTerm");
			        Text ptVal = doc.createTextNode(order.getPaymTerm() + "D");
			        pt.appendChild(ptVal);
			        header.appendChild(pt);
			      
		        }
		        
				String comm = "";
	System.out.println("info " + order.getInfo());
	System.out.println("teostaja " + order.getTeostaja());
				if(order.getInfo() != null && order.getInfo().trim().length() >0){
		 			Node com = doc.createElement("Remarks");
			        Text comVal = doc.createTextNode(order.getInfo());
			        com.appendChild(comVal);
			        header.appendChild(com);
			        comm = order.getInfo().replaceAll("\\r\\n|\\r|\\n", " ");
			        if(ret){
			        	Node Note1 = doc.createElement("Note1");
	   			     Text Note1Val = doc.createTextNode(comm);
	   			     Note1.appendChild(Note1Val);
	   			     header.appendChild(Note1);	
			        }
		 		}
		 		 
		 		 
		 		if(ret){
	                String inff = order.getTeostaja();
	                if (inff != null && inff.trim().length()>0 && inff.indexOf("[") !=-1){
	                	reason = inff.substring(inff.indexOf("[")+1, inff.indexOf("]"));
	    		    
	                }
		 			
			       
		 			Node WebOrderRegion = doc.createElement("WebOrderRegion");
			        Text WebOrderRegionVal = doc.createTextNode("-1");
			        WebOrderRegion.appendChild(WebOrderRegionVal);
			        header.appendChild(WebOrderRegion);
		 			 
			        
			        Node WebOrderNote = doc.createElement("WebOrderNote");
			        Text WebOrderNoteVal = doc.createTextNode(comm);
			        WebOrderNote.appendChild(WebOrderNoteVal);
			        header.appendChild(WebOrderNote);
			        
			        Node WebOrderDeliveryType = doc.createElement("WebOrderDeliveryType");
			        Text WebOrderDeliveryTypeVal = doc.createTextNode("C");
			        WebOrderNote.appendChild(WebOrderDeliveryTypeVal);
			        header.appendChild(WebOrderDeliveryType);
		 			 
		 		 }
			        
			      //  <Remarks>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</Remarks>
			       // <Note1>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</Note1>
			       // <WebOrderRegion>-1</WebOrderRegion>
			       // <WebOrderNote>preču atgriešana. jāaizbrauc ar loģistiku pakaļ!</WebOrderNote>
			       // <WebOrderDeliveryType>C</WebOrderDeliveryType>
			        
			        
			        
			        
		 		System.out.println("Remarks " + comm);
		        
		        
		      

		        Node kuupaev = doc.createElement("OrderDate");
		        Calendar cal = Calendar.getInstance();
		      

		        String month = (cal.get(Calendar.MONTH) + 1) + "";
		        if (month.length() == 1) month = "0" + month;
		        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
		        if (day.length() == 1) day = "0" + day;
	  String year = cal.get(Calendar.YEAR) + "";
	  if(year.length() == 2){
		  year = "20" + year;
	  }
		        String dateString = year +"-"+ month +"-"+ day;
		        Text kuupaevVal = doc.createTextNode(dateString);
		        kuupaev.appendChild(kuupaevVal);
		        header.appendChild(kuupaev);

	   if(brakis){
		   Node ware = doc.createElement("Warehouse");
		   Text wareVal = doc.createTextNode(order.getDealer());
		   ware.appendChild(wareVal);
		   header.appendChild(ware); 
	   }
	   
	   Node curr = doc.createElement("OrderCurrency");
	   Text currVal = doc.createTextNode("EUR");
	   curr.appendChild(currVal);
	   header.appendChild(curr);
		        
		        Node delkuupaev = doc.createElement("ExpectedDeliveryDate");
		        Calendar delcal = Calendar.getInstance();
		        delcal.setTime(order.getShipCal().getTime());

		         month = (delcal.get(Calendar.MONTH) + 1) + "";
		        if (month.length() == 1) month = "0" + month;
		         day = delcal.get(Calendar.DAY_OF_MONTH) + "";
		        if (day.length() == 1) day = "0" + day;
	   year = delcal.get(Calendar.YEAR) + "";
	  if(year.length() == 2){
		  year = "20" + year;
	  }
		         dateString = year +"-"+ month +"-"+ day;
		        Text delkuupaevVal = doc.createTextNode(dateString);
		        delkuupaev.appendChild(delkuupaevVal);
		        header.appendChild(delkuupaev);		        


		        Node expectedDeliveryTime = doc.createElement("ExpectedDeliveryTime");
		        Text expectedDeliveryTimeVal = doc.createTextNode("00:00");
		        expectedDeliveryTime.appendChild(expectedDeliveryTimeVal);
		        header.appendChild(expectedDeliveryTime);

		        
		        String trykk = "false";
		        if(noinvoice ) { // || ret
		        	trykk = "true";
		        }
		        
		        Node specialInstructions = doc.createElement("SpecialInstructions");
		        Text specialInstructionsVal = doc.createTextNode(trykk);
		        specialInstructions.appendChild(specialInstructionsVal);
		        header.appendChild(specialInstructions);
		       
		        
		        String shm = "carrier";
		        if(shipmodeown) {
		        	shm = "private";
		        }
		        Node orderTransport = doc.createElement("Order-Transport");
		        
		        Node modeOfTransport = doc.createElement("ModeOfTransport");
		        Text modeOfTransportVal = doc.createTextNode(shm);
		        modeOfTransport.appendChild(modeOfTransportVal);
		        orderTransport.appendChild(modeOfTransport);
		        
		        header.appendChild(orderTransport);
		         

		        
		        
		        root.appendChild(header);

	String custname = this.identifyCustomerName(order.getCompany());
		        Node parties = doc.createElement("Document-Parties");
		        Node seller = doc.createElement("DeliveryPoint");

		        Node ise = doc.createElement("CodeBySupplier");
		        Text nimiVal = doc.createTextNode(escape(order.getCompany().substring(order.getCompany().lastIndexOf("-")+1)));
		        ise.appendChild(nimiVal);
		        
		        Node iseName = doc.createElement("Name");
		        Text iseNameVal = doc.createTextNode(custname);
		        iseName.appendChild(iseNameVal);
		        
		        
		        Node ise2 = doc.createElement("ILN");
		        Text nimiVal2 = doc.createTextNode(escape(order.getCompany().substring(order.getCompany().lastIndexOf("-")+1)));
		        ise2.appendChild(nimiVal2);
		        
		        Node add  = doc.createElement("StreetAndNumber");
		        if(addr != null) {
		        	Text addVal2 = doc.createTextNode(escape(addr));
			        add.appendChild(addVal2);
			    }
		        
		        seller.appendChild(ise2);
		        seller.appendChild(iseName);
		        seller.appendChild(ise);
		        seller.appendChild(add);
			        parties.appendChild(seller);

		        
		        
		        Node sell = doc.createElement("Seller");
		        Node sellX = doc.createElement("Name");
		        Text sellXVal = doc.createTextNode("Momentin LT");
		        sellX.appendChild(sellXVal);
		        
		        Node iseX22 = doc.createElement("CodeByBuyer");
		        Text nimiXVal22 = doc.createTextNode("MOMENTIN");
		        iseX22.appendChild(nimiXVal22);
		        sell.appendChild(sellX);
		        sell.appendChild(iseX22);

		        parties.appendChild(sell);
		        
		        Node buyer = doc.createElement("Buyer");

		        Node iseX2 = doc.createElement("Name");
		        Text nimiXVal2 = doc.createTextNode(custname);
		        iseX2.appendChild(nimiXVal2);
		        
		        Node iseX = doc.createElement("CodeByBuyer");
		        Text nimiXVal = doc.createTextNode(escape(order.getCompany().substring(order.getCompany().lastIndexOf("-")+1)));
		        iseX.appendChild(nimiXVal);
		        Node ilnX = doc.createElement("ILN");
		        Text ilnXVal = doc.createTextNode(escape(order.getCompany().substring(0,order.getCompany().lastIndexOf("-"))));
		        ilnX.appendChild(ilnXVal);
		        
		        buyer.appendChild(iseX2);
		        buyer.appendChild(iseX);
		        buyer.appendChild(ilnX);

		        parties.appendChild(buyer);
		        
		        root.appendChild(parties);



		        //RIDADE LISAMINE
		        Node read = doc.createElement("Order-Lines");
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
		              coun++;
		        	  addLine(line, read, doc, coun , order, ret , reason , custname,gift);
		          }
		          }
		        
		        
		        if( pandikas.size()>0){
		          	Enumeration enumpant = pandikas.keys();
		           while(enumpant.hasMoreElements()){
		           	
		           	String toode = (String)enumpant.nextElement();
		          	double pand = new Double((String)pandikas.get(toode)).doubleValue();
		          	 coun++;
		        	  addLine2(toode, pand, read, doc, coun , order, ret , reason , custname,gift);
		          }
		           }
		        

		        root.appendChild(read);

		        
		        Node summary = doc.createElement("Order-Summary");
		        
		      
		        Node TotalLines = doc.createElement("TotalLines");
		        Text TotalLinesVal = doc.createTextNode(coun + "");
		        TotalLines.appendChild(TotalLinesVal);
		        summary.appendChild(TotalLines);

		        Node TotalOrderedAmount = doc.createElement("TotalOrderedAmount");
		        Text TotalOrderedAmountVal = doc.createTextNode(order.getSum() + "");
		        TotalOrderedAmount.appendChild(TotalOrderedAmountVal);
		        summary.appendChild(TotalOrderedAmount);
		        
		        Node TotalNetAmount = doc.createElement("TotalNetAmount");
		        Text TotalNetAmountVal = doc.createTextNode(order.getSum() + "");
		        TotalNetAmount.appendChild(TotalNetAmountVal);
		        summary.appendChild(TotalNetAmount);
		       
		        root.appendChild(summary);
		        //new File(parent.getProps().getProperty("exceldir")+ "sent/"+
		      String fileName =  parent.getProps().getProperty("outdir") + order.getDokNr().trim() + (uni + "").trim() +  ".xml";
		      System.out.println(fileName);
		      OutputFormat of = new OutputFormat();
		        of.setIndenting(true);
		        of.setLineSeparator(System.getProperty("line.separator"));
		        of.setEncoding("ISO-8859-1");
		        XMLSerializer ser = new XMLSerializer(of);
		        FileOutputStream fos = new FileOutputStream(fileName);
		        PrintWriter out = new PrintWriter(fos);
		        ser.setOutputCharStream(out);
		        ser.serialize(doc);
		        out.close();
		      
			
		      } catch (Exception e) {
				e.printStackTrace();
			}
		      
		      return  order.getDokNr().trim() + (uni + "").trim() +  ".xml";
		 }

  

  
  private void addLine(OrderLine line, Node read, Document doc,int counter ,Order order , boolean ret 
		   ,String reason,String custname, boolean gift) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

	     String toode  = line.getToode();
	     
	     Product prod = this.getProduct(toode);
	
	     if(prod== null) {
	    	 System.out.println("Prod null " + toode);
	    	// System.exit(0);
	    	 prod = new Product(toode);
	    	 prod.setEAN(toode);
	    	 prod.setName(toode);
	     }
	     try {
			    Node toote_ean = doc.createElement("EAN");
			     Text toote_eanVal = doc.createTextNode(prod.getEAN());
			     toote_ean.appendChild(toote_eanVal);
			     ridaitem.appendChild(toote_ean);	 
		 }catch(Exception e) {
			 
		 }
	 

	     
	     
	     Node btoote_kood = doc.createElement("BuyerItemCode");
	     Text btoote_koodVal = doc.createTextNode(toode);
	     btoote_kood.appendChild(btoote_koodVal);
	     ridaitem.appendChild(btoote_kood);
       

	     
	     Node toote_kood = doc.createElement("SupplierItemCode");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);
       
	     
	     String nimi = (prod.getName());  
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(Converter.deCode(nimi).replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);
	     }

	    

	     double kogus = line.getKogus();
	     double price = line.getPrice();
	     double disc = line.getDiscount();
	     String unit = line.getUnit();
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

	     
	     

	    
	     
	   
	     
	    
	    
	    if(disc > 0 || disc == -3){
	    	 Node yhikuale_hind = doc.createElement("SupplierUnitNetPrice");
	         Text yhikuale_hindVla = doc.createTextNode(this.nf.format(price));
	         yhikuale_hind.appendChild(yhikuale_hindVla);
		     ridaitem.appendChild(yhikuale_hind);
	    }
	     
	    
	    Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	    Text yhiku_hindVal = doc.createTextNode(this.nf.format(price));
	     yhiku_hind.appendChild(yhiku_hindVal);
	   //  ridaitem.appendChild(yhiku_hind);

		    if(disc > 0 || disc == -3 ){
		    

	     Node yhiku_sum = doc.createElement("OrderedNetAmount");
	      Text yhiku_sumVla = doc.createTextNode(this.nf.format(price * kogus));
		     yhiku_sum.appendChild(yhiku_sumVla);
		     ridaitem.appendChild(yhiku_sum);

	 

		     Node yhiku_vat = doc.createElement("OrderedTaxAmount");
		      Text yhiku_vatVla = doc.createTextNode(this.nf.format(price * kogus * 0.21));
			     yhiku_vat.appendChild(yhiku_vatVla);
			     ridaitem.appendChild(yhiku_vat);
			 	
		    }

			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("21");
				     yhiku_va.appendChild(yhiku_vaVla);
				     ridaitem.appendChild(yhiku_va);

				     //<ItemType>CU</ItemType>
				     Node ItemType = doc.createElement("ItemType");
				      Text ItemTypeVla = doc.createTextNode("CU");
				        ItemType.appendChild(ItemTypeVla);
					     ridaitem.appendChild(ItemType);
				
					     
					     if(disc > 0 || disc == -3){
						     Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("1");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
						     String hhh = disc + "";
						     if(disc == -3){
						    	 hhh = "0";
						     }
						     Node yhiku_vada = doc.createElement("DiscountVolume");
						      Text yhiku_vadaVla = doc.createTextNode(hhh);
							     yhiku_vada.appendChild(yhiku_vadaVla);
						     ridaitem.appendChild(yhiku_vada);
						
	 
					     }else{
					    	 Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("0");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
					     }
							    	     
		
	     
	     
	     
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);
String gg = "0";
	     if(gift){
	    	 gg = "1";  	
	     }
	     Node sample = doc.createElement("Sample");
	     Text sampleVal = doc.createTextNode(gg);
	     sample.appendChild(sampleVal);
	     ridaitem.appendChild(sample);
	     
	     if (ret){
	    	 System.out.println("Partii" + line.getPartii()+" " + reason);
	    	 Node ReturnReason = doc.createElement("ReturnReason");
		     Text ReturnReasonVal = doc.createTextNode(reason);
		     ReturnReason.appendChild(ReturnReasonVal);
		     ridaitem.appendChild(ReturnReason);
		    
if(line.getPartii() !=null && line.getPartii().length() >0){
String text = line.getPartii();
if(text.indexOf("\"")!=-1){
	text = text.substring(text.indexOf("\""));
	Node ExciseStamp = doc.createElement("ExciseStamp");
   Text ExciseStampVal = doc.createTextNode(text.replace('"', ' ').replace('"', ' ').trim());
   ExciseStamp.appendChild(ExciseStampVal);
   ridaitem.appendChild(ExciseStamp);
}
		 	
		 }		    
	 
	     }else  if(line.getPartii() != null && line.getPartii().trim().length() > 0 ){
		    Node toote_comm = doc.createElement("LineComment");
		   if(gift){
			  toote_comm = doc.createElement("Samples_comment"); 
		   }
		    Text toote_commVal = doc.createTextNode(line.getPartii().trim());
		    toote_comm.appendChild(toote_commVal);
		    ridaitem.appendChild(toote_comm);
		    
		}
   
	     rida.appendChild(ridaitem);
	    
	   }

  private void addLine2(String toode  , double kogus, Node read, Document doc,int counter ,Order order , boolean ret 
		   ,String reason,String custname, boolean gift) throws SQLException{
	   

	
	     Node rida = doc.createElement("Line");
	     read.appendChild(rida);

	     Node ridaitem = doc.createElement("Line-Item");


	     Node linenum = doc.createElement("LineNumber");
	     Text linnum = doc.createTextNode(counter+"");
	     linenum.appendChild(linnum);
	     ridaitem.appendChild(linenum);

	    
	     
	     Product prod = this.getProduct(toode);
		 try {
			    Node toote_ean = doc.createElement("EAN");
			     Text toote_eanVal = doc.createTextNode(prod.getEAN());
			     toote_ean.appendChild(toote_eanVal);
			     ridaitem.appendChild(toote_ean);	 
		 }catch(Exception e) {
			 
		 }
	 

	     
	     
	     Node btoote_kood = doc.createElement("BuyerItemCode");
	     Text btoote_koodVal = doc.createTextNode(toode);
	     btoote_kood.appendChild(btoote_koodVal);
	     ridaitem.appendChild(btoote_kood);
      

	     
	     Node toote_kood = doc.createElement("SupplierItemCode");
	     Text toote_koodVal = doc.createTextNode(toode);
	     toote_kood.appendChild(toote_koodVal);
	     ridaitem.appendChild(toote_kood);
      
	     
	     String nimi = (prod.getName());  
	     if(nimi != null){
	    	 Text nimetusVal = doc.createTextNode(Converter.deCode(nimi).replace('`',' ').replace('´',' '));
		     Node nimetus = doc.createElement("ItemDescription");
		     nimetus.appendChild(nimetusVal);
		     ridaitem.appendChild(nimetus);
	     }

	    

	     
	     String unit = prod.getUnit();
	     
	     Node koGus = doc.createElement("OrderedQuantity");
	     
	     Text kogusVal = doc.createTextNode(getNF().format(kogus));
	     koGus.appendChild(kogusVal);
	     ridaitem.appendChild(koGus);

	     
	     

	   
	    
	    Node yhiku_hind = doc.createElement("OrderedUnitNetPrice");
	    Text yhiku_hindVal = doc.createTextNode(this.nf.format(prod.getPrice()));
	     yhiku_hind.appendChild(yhiku_hindVal);
	     ridaitem.appendChild(yhiku_hind);

		

			     Node yhiku_va = doc.createElement("TaxRate");
			      Text yhiku_vaVla = doc.createTextNode("21");
				     yhiku_va.appendChild(yhiku_vaVla);
				     ridaitem.appendChild(yhiku_va);

				     //<ItemType>CU</ItemType>
				     Node ItemType = doc.createElement("ItemType");
				      Text ItemTypeVla = doc.createTextNode("CU");
				        ItemType.appendChild(ItemTypeVla);
					     ridaitem.appendChild(ItemType);
				
					     
					    	 Node yhiku_vad = doc.createElement("Discount");
						      Text yhiku_vadVla = doc.createTextNode("0");
							     yhiku_vad.appendChild(yhiku_vadVla);
						     ridaitem.appendChild(yhiku_vad);
					    	    	     
		
	     
	     
	     
	     
	     Node yhik = doc.createElement("UnitOfMeasure");
	     Text yhikVal = doc.createTextNode(unit);
	     yhik.appendChild(yhikVal);
	     ridaitem.appendChild(yhik);
String gg = "0";
	     if(gift){
	    	 gg = "1";  	
	     }
	     Node sample = doc.createElement("Sample");
	     Text sampleVal = doc.createTextNode(gg);
	     sample.appendChild(sampleVal);
	     ridaitem.appendChild(sample);
	     
	    
	   
  
	     rida.appendChild(ridaitem);
	    
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
