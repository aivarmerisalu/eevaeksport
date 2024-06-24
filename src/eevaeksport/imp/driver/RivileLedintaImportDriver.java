
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.Debt;
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

public class RivileLedintaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public RivileLedintaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");

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
		    	 con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
		    	
		    	 Statement st1 = con.createStatement();
		   	    ResultSet rs1 = st1.executeQuery(sql1);
		   	    while (rs1.next()) {
		   	    	
		   	     String tood  = rs1.getString("toode").trim();
		   	     String pant  = rs1.getString("pant").trim();
		   	         pants.put(tood, pant);
		   	  
		   	    }
		   	       
		   	    rs1.close();
		   	    st1.close();	
                con.close();
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
    con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood1").trim() + "-" +rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();
   con.close();
   // System.out.println("Klient: " + acc);
    return acc;

  }

  public String identifyCustomerName(String id) throws SQLException {
	  	 String	sql1 = "SELECT N08_PAV AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
	  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
	  	
	    System.out.println(sql1);
	    String acc = null;
	    con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("aadress").replace("\"", "-").trim();

	    }
	    rs1.close();
	    st1.close();
        con.close();
	    return acc;

	  }
  
  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  rtrim(rtrim(N08_KODAS_KS))+'-'+ rtrim(ltrim(N33_kodas_ak)) = '" + id +"'"; 
    System.out.println(sql1);
    String acc = null;
    con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();
    con.close();
    return acc;
  }
  
  

  
  
  
  
  
  public String getAgentCar(String age) throws SQLException {
	 	 String	sql1 = "SELECT N72_KODAS_AU AS carplate FROM N72_mas  WHERE N72_KODAS_MS = '" + age +"'"; 
	 	
	   System.out.println(sql1);
	   String acc = null;
	   con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	     acc = rs1.getString("carplate").trim();

	   }
	   rs1.close();
	   st1.close();
       con.close();
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
    con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
 //  System.out.println(ean + " prod0 " + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

   // System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();
   con.close();
    return prod;
  }

  private void setOrder(Order order){
	     this.order = order;
	   }
  private Order order = null;
  
  
  
  private double getProductPrice(String toode) {
	  double res = 0;
	  
	  String    sql = "select I33_KODAS_PS AS toode, max(I33_KAINA) AS hind from I33_PKAI "
	    		+ " where I33_KODAS_iS = 'SAND1' and  I33_KODAS_PS = '" + toode + "'  GROUP BY I33_KODAS_PS order by I33_KODAS_PS " ;
	            System.out.println("hind");
	       try {
	    	   
	    	   con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
	            Statement st1 = con.createStatement();
	            ResultSet rst = st1.executeQuery(sql);
	            
	       
	               while (rst.next()) {
	                 res = rst.getDouble("hind");
	                }
	             rst.close();
	             st1.close();
	             con.close();
           }catch(Exception e ) {
	    	   
	       }
	  return res;
			  
  }
  
	
	 private Vector d30 = new Vector();
	 private HashMap<String, Double> usedCr = new HashMap<String, Double>();
	 

	 public Vector getDebt(String agentCode) throws SQLException{
		 return this.debtV;
	 }
	
	 private Vector debtV = new Vector();
	
	 
public Vector getDebt() throws SQLException{
	
   debtV = new Vector();
   usedCr = new HashMap<String, Double>();
   
 String sql = "SELECT I44_KODAS_KS AS klient,I44_DOK_NR AS number ,max(I44_DATA_MOK) AS maksepaev , " +
     " sum(i44_suma_db-i44_suma_cr) AS tasumata ,min(I44_DATA_DOK) AS kuupaev , max(datediff(day,I44_DATA_MOK,getdate())) as yle FROM i44_skol,n08_klij WHERE " +
     " i44_skol.i44_kodas_ks=n08_klij.n08_kodas_ks and " +
     " exists(select i44_kodas_ks,i44_dok_nr,sum(i44_suma_db-i44_suma_cr) as suma from i44_skol i44 where i44.i44_kodas_ks=i44_skol.i44_kodas_ks " +
     " and i44.i44_dok_nr=i44_skol.i44_dok_nr and i44_tipas<>11  group by i44.i44_kodas_ks,i44.i44_dok_nr " + //and i44.i44_data_dok <= getdate()
    " having sum(i44.i44_suma_db-i44.i44_suma_cr)<>0) group by I44_DOK_NR,I44_KODAS_KS order by I44_KODAS_KS,i44_dok_nr ";

 
 String sql2 = "select l11_kodas1 as klient , dbo.pap(l11_laukas,11,1) as paevi from l11_pap1 where l11_kodas_nr='KSN08' ";
 Connection con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
  PreparedStatement st = con.prepareStatement(sql2);
  ResultSet rs = st.executeQuery();
  TreeMap<String, Integer> paeviT = new TreeMap<String, Integer> ();
  while(rs.next()){
	 String kl = rs.getString("klient").trim();
	 String  pv = rs.getString("paevi");
	 
	 if(pv != null && pv.trim().length() > 0) {
		 Integer paevi = Integer.parseInt(pv.trim());
   	  if(paevi != null) {
   		  paeviT.put(kl, paevi);
   	  } 
	 }
    }
  
  rs.close();
  st.close();
 
  
  st = con.prepareStatement(sql);
  rs = st.executeQuery();

   while(rs.next()){
   int days = 30;
    String kl = rs.getString("klient").trim();
    if(paeviT.get(kl) != null) {
    	days = paeviT.get(kl);
    }
    if(!kl.equals("140")) { // maximat pole vaja 
   	 Debt debt = new Debt();
     debt.setCustID(kl);
     String number = rs.getString("number").trim();
     debt.setPayerID(debt.getCustID());
     debt.setDoknr(number);
     debt.setDuedate(rs.getDate("maksepaev"));
     debt.setSum(rs.getDouble("tasumata"));
     debt.setDate(rs.getDate("kuupaev"));
     int yle = rs.getInt("yle");
     debt.setDue(yle+"");
     if(yle > days && !d30.contains(kl) && debt.getSum() > 0) {// muutsin 30 peale
    	 d30.add(kl);
     }
     double ss = 0;
     if(usedCr.get(kl) != null) {
    	 ss = usedCr.get(kl);
     }
     usedCr.put(kl, ss +debt.getSum());
     debtV.addElement(debt);
    
    }
  }
  rs.close();
  st.close();
  con.close();
  
  return debtV;
}
  
  public TreeMap getCustomers(){
  	try {
  		getDebt();
  	  }catch(Exception e) {
  		e.printStackTrace();
  	 }
  	
  	System.out.println("d30.size() " + d30.size());


      String cmd = "SELECT  ltrim(N08_KODAS_KS) AS kood1, (case when (N33_kodas_ak) is null then '1' else ltrim((N33_kodas_ak)) end) AS kood, " +
      		" N08_PAV as nimetus, N33_kodas_ak AS nimetus2, N08_CREDIT_LIM as cred, " +
				" N33_ADRESAS AS aadress, ltrim(N08_NUOL_GR)   AS ale , " +
				" ' ' AS visit ,N08_KODAS_KS AS payer , N08_KODAS_LS_8 as konsig , " +
				" (CASE WHEN LEN(LTRIM(N33_KODAS_MS))=0 or N33_KODAS_MS  IS NULL THEN  N08_KODAS_MS_P ELSE N33_KODAS_MS  END) AS agent ,  " +
				" (case when N08_KODAS_TS_P in ('REAL','SEKAN','ISANKSTINIS','NER') then '0' else ltrim(N08_KODAS_TS_P) end) as paymterm , N33_TEL as phone , N33_SHIP_FOB  as deld " +
				"  FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS where N08_PAV is not null order by N08_PAV ";
     
      TreeMap customers = new TreeMap();
    

      try {


   

    	  con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
           PreparedStatement   stmt = con.prepareStatement(cmd);
           ResultSet	      rs = stmt.executeQuery();

          while (rs.next()) {
            String accountnum = rs.getString("kood1").trim();
            String maks = accountnum;
            String acc = rs.getString("kood");
        
            if (acc != null && acc.trim().length()>2){
            	accountnum = accountnum + "-" + acc.trim();
            }
            String name = rs.getString("nimetus").trim() ;
          		 String nim2 = rs.getString("nimetus2");
            if(nim2 != null) {
          	  name = name + " " + nim2.trim();
            }
          		  
           
          	name = name + " "  + accountnum;
          
          Customer cust = new Customer(accountnum);
          //  System.out.println("Name " + name);
         
          
          String cred = rs.getString("cred");
          if (cred != null && cred.trim().length() >0 ){
        		     cust.setCredit(cred.trim());
          }
            
         
          
            String aadr = rs.getString("aadress");
            if(aadr != null && aadr.trim().length()>0){
              cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim()),"Lithuania"));
              name = name + " " + cust.getAddress();
            }
            cust.setName(Converter.enCodeEst(name).replaceAll("  ", " ").replaceAll("  ", " "));

            String discStr = rs.getString("ale");
            if (discStr != null && discStr.trim().length() >0 ){
          		     cust.setGroup(discStr.trim());
            }
            
            String visit = rs.getString("visit");
            if (visit != null && visit.trim().length()>0){
              cust.setVisit(visit.trim());
            }
            
            
            String payer =rs.getString("payer");
          
            if(payer != null && payer.trim().length()> 0){
              cust.setPayer(payer.trim());
                         }
            
            if(usedCr.get(cust.getPayer()) != null) {
  	          cust.setUsedCredit(usedCr.get(cust.getPayer()));
             } else if(usedCr.get(accountnum) != null) {
    	          cust.setUsedCredit(usedCr.get(accountnum));
             }
          
            if(d30.contains(cust.getID()) || d30.contains(maks)) {
          	  cust.setSalesBlockT(true);
          //	System.out.println("Blokk peale " + cust.getID());
            }
         
            
            String konsig = rs.getString("konsig");
            if(cust.isSalesBlockT() && konsig != null && konsig.trim().equals("ATSISK_PO_RE")){
          	  cust.setSalesBlockT(false);
          	 
            }
            
         
                customers.put(cust.getID(), cust);
         
          }
          rs.close();

          stmt.close();
          con.close();
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        return customers;
  }
  
  TreeMap custs = null;
  HashSet statProds = null;
  
  public HashSet getStatProds(){
	  
    HashSet products = new HashSet();
    
      	try {
      		
      	   String cmd = " SELECT N17_KODAS_PS AS toode  FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1\r\n" + 
      	   		"   left join L02_FILD on L02_kodas_fil = N17_KODAS_PS left join N35_LGST on N35_KODAS_LS= N17_KODAS_LS_5   where  N17_POZ_DATE !=1 and "
      	   		+ "(L02_KODAS_FS is null or L02_KODAS_FS!='NP_O') and N17_MEN_PAV !='IG' and len(ltrim(N17_PAV_K1))  >0 and N17_kodas_gs = 'STAT'";
      		 con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
             PreparedStatement   stmt = con.prepareStatement(cmd);
             ResultSet	      rs = stmt.executeQuery();

          
          while (rs.next()) {
        	  products.add(rs.getString("toode").trim());
          }
         
          rs.close();
          stmt.close();
          con.close();
          
      } catch (Exception e) {
         e.printStackTrace();
      }

      return products;
  }

  
  
  private boolean onlyStat(Order order) {
	  boolean res = true;
		
		 Vector lines = order.getOrderLines();
	     for (int i = 0; i < lines.size(); i++) {
	    	    
	      	OrderLine line = (OrderLine)lines.elementAt(i);
	          String toode = line.getToode();
	           if(!statProds.contains(toode)) {
	        	   res = false;
	           }
	         }
	  
	  return res;
  }
  
  public void makeOrder(Order order) throws SQLException{
	  getNF();
	  setOrder(order);
	     
	  if(custs == null) {
		custs =   this.getCustomers();
	  }
	
	  
	  boolean kaup = false;
	
	  
	  boolean tag = false;
	  
	  OrderLine ol = (OrderLine)order.getOrderLines().get(0);
	  if(ol.getKogus() < 0) {
		  tag = true;
		  if("true".equals(parent.getProps().getProperty("return.goods"))){
			 if(!ol.getToode().startsWith("MED")) {
				 kaup = true;
			 }
		  }
	  }
	  
	 
	  boolean stat = false;
	  if(tag) {
		  if(statProds == null) {
			  statProds = this.getStatProds();
		  }
		  
	   stat = onlyStat(order);
	  }
	  
	  System.out.println("On Tagastus/Võlgu " + tag  + " " + kaup + " statProds " + stat);
	  
      makeOutput(order, tag, false, kaup, false , stat);
    
    
 
 
  }
 

  
 

 private void makeOutput(Order order,boolean tag, boolean volgu , boolean kaup , boolean yle, boolean stat){
 	StringBuffer sb = new StringBuffer();
   
	
 
    
 	String ls = System.getProperty("line.separator");
 	try {
 	  int cou = 1;
     // sb.append(getLineNr(cou));
 
     sb.append(getLineNr(cou));
     sb.append("       <I06>" + ls);
     cou++;
    // 51-Važtaraštis, 52-Grąžinimas, 53-Užsakymas, 54-Rezervavimas, 55-Pasiūlymas
     
     String type = "55";
     if(parent.getProps().getProperty("doc.type") != null) {
    	 type = parent.getProps().getProperty("doc.type") ;
     }
     sb.append(getLineNr(cou));
     sb.append("          <I06_OP_TIP>"+ type+ "</I06_OP_TIP>" + ls);
     cou++;

     sb.append(getLineNr(cou));
     sb.append("          <I06_PVM_TIP>0</I06_PVM_TIP>" + ls);
     cou++;
     String cust = Converter.deCode(order.getCompany());
     String addr =  null;
     String err = "";
 	try {
 		addr = identifyCustomerADDR(cust);
 	} catch (Exception e1) {
 		err = e1.toString();
 		try {
 			addr = identifyCustomerADDR(cust);
 		}catch(Exception k) {
 			err = k.toString();
 			try {
 	  			addr = identifyCustomerADDR(cust);
 	  		}catch(Exception u) {
 	  		err = u.toString();
 	  		try {
 	  			addr = identifyCustomerADDR(cust);
 	  		}catch(Exception s) {
 	  		err = s.toString();
 	  			parent.addLog(s.toString());
 	  		}
 	  		}
 		}
 		
 		
 	}
 	
 	
     
     String agent = order.getAgent();
    
     
	   String doknr = order.getDokNr();
	   if(doknr.length() > 13){
		  // doknr = removeFirstZero(order.getDokNr());
	   }

     sb.append(getLineNr(cou));
     sb.append("          <I06_DOK_NR>" + escape(doknr)  + "</I06_DOK_NR>" + ls);
     cou++;

     sb.append(getLineNr(cou));
     sb.append("          <I06_DOK_REG>" + escape(doknr)  + "</I06_DOK_REG>" + ls);
     cou++;
     
     String auto = agent;
     if(auto.equals("05")){
   	  //auto = "BFS 587";
     }
     if(auto.equals("04")){
   	 // auto = "COR 389";
     }
     
   /*  sb.append(getLineNr(cou));
     sb.append("          <I06_KODAS_AU>" +  escape(auto)   + "</I06_KODAS_AU>" + ls);
     cou++;*/
     

     sb.append(getLineNr(cou));
     sb.append("          <I06_OP_DATA>" +  getDateString(order.getShipCal()) + "</I06_OP_DATA>" + ls);
     cou++;
     
     
     sb.append(getLineNr(cou));
     sb.append("          <I06_DOK_DATA>" +  getDateString(Calendar.getInstance()) + "</I06_DOK_DATA>" + ls);
     cou++;
 
     

   
	sb.append(getLineNr(cou));
     sb.append("          <I06_KODAS_KS>" + escape(cust.substring(0,cust.lastIndexOf("-"))) + "</I06_KODAS_KS>" + ls);
     cou++;
     System.out.println("aadress "+ addr);
   if(addr != null) {
   	sb.append(getLineNr(cou));
       sb.append("          <I06_ADR2>" + escape(addr) + "</I06_ADR2>" + ls);
       cou++;
     	
   }
	double addtoVat = 0;
   //vaatame kas on vaja lisada km-le midagi.
   Vector lines = order.getOrderLines();
   double sum = 0;
   double sumVat = 0;
   for (int i = 0; i < lines.size(); i++) {
   	   
   	OrderLine line = (OrderLine)lines.elementAt(i);
       double ale = line.getDiscount();
       double linesum = 0;
       double linevat = 0;
       if (ale != 0 ){
    	 	  
         linesum =   Double.parseDouble(nf.format ((line.getKogus() * line.getPrice())*(100-ale)/100));
         linevat =   Double.parseDouble(nf.format((line.getKogus() * line.getPrice() * 0.21)*(100-ale)/100)); 	

   } else {
	   linesum =   Double.parseDouble(nf.format((line.getKogus() * line.getPrice() ))); 
       linevat = Double.parseDouble(nf.format((line.getKogus() * line.getPrice() * 0.21)));  
    		   //Converter.Round((float) (line.getKogus() * line.getPrice() * 0.21),2); 
   }
       sum = sum + linesum;
       sumVat = sumVat + linevat;
   }
    
   double baseVat =  Double.parseDouble(nf.format( Double.parseDouble(nf.format((sum))) * 0.21));
   
   addtoVat = baseVat - sumVat ;
   
 //  System.out.println("vats " + addtoVat + " " + baseVat + " " + sumVat + " " + sum);
   

   
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
         sb.append(getLineNr(cou));
         sb.append("               <I07_TIPAS>1</I07_TIPAS>" + ls);
         cou++;
         /*
         Fillial 01   -  agent 007
Fillial 02   -  agent 020
Fillial 03    - agent 016*/
         String ladu = "KAUNAS";
         if(agent.equals("003")) {
        	 ladu = "KLAIPEDA";
         }
         if(agent.equals("004")) {
        	 ladu = "UTENA";
         }
         if(agent.equals("007")) {
        	// ladu = "PALANGA";
         }
         if(doknr.startsWith("LED7")) {
        	 ladu = "PALANGA";
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
             if(partii != null && partii.trim().length() > 0){
             	  sb.append(getLineNr(cou));
                   sb.append("               <I07_KODAS_OS>" +  escape(partii.trim()) + "</I07_KODAS_OS>" + ls);
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
         
         double addv = 0;
         if(i == 0) {
        	 addv = addtoVat;
         }
 if (ale != 0 ){
 	
         sb.append(getLineNr(cou));
         sb.append("               <I07_NUOLAIDA>" + escape(nf.format(ale)) + "</I07_NUOLAIDA>" + ls);
         cou++;
         sb.append(getLineNr(cou));
         
         sb.append("               <I07_SUMA>" + escape(nf.format((line.getKogus() * line.getPrice())*(100-ale)/100)
         	)+ "</I07_SUMA>" + ls);
         cou++;

         sb.append(getLineNr(cou));
         sb.append("               <I07_PVM>" + escape(nf.format((line.getKogus() * line.getPrice()*(100-ale)/100 * 0.21)+addv))+ "</I07_PVM>" + ls);
         cou++;

 } else {
	 
	 sb.append(getLineNr(cou));
     sb.append("               <I07_SUMA>" + escape(nf.format((line.getKogus() * line.getPrice())))+ "</I07_SUMA>" + ls);
     cou++;
     
     sb.append(getLineNr(cou));
     sb.append("               <I07_PVM>" + escape(nf.format((line.getKogus() * line.getPrice() * 0.21)+addv))+ "</I07_PVM>" + ls);
     cou++;
     
 }
 
 sb.append(getLineNr(cou));
 sb.append("               <I07_MOKESTIS_P>21</I07_MOKESTIS_P>" + ls);
 cou++;
        
         
    
         

 addNumLine();

         sb.append(getLineNr(cou));
         sb.append("          </I07>" + ls);
         cou++;
     }

     sb.append(getLineNr(cou));
     sb.append("          </I06>" + ls);
     cou++;
      
      
      
      
      String cat = parent.getProps().getProperty("conv.dir");
      if(tag) {
    	 cat = parent.getProps().getProperty("conv.dirret");
      }
      if(order.getDokNr().toLowerCase().startsWith("f") || order.getDokNr().toLowerCase().startsWith("z")) {
    	  cat = parent.getProps().getProperty("conv.dir2");
      }
 	
      String fileName = escape(cat + order.getAgent()) + "-"  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".EIP";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

        if(!order.getDokNr().toLowerCase().startsWith("f") &&  !order.getDokNr().toLowerCase().startsWith("z")) {
    	try {
        


        String data = "	<body><method>EDIT_I06_FULL</method><data>" + sb.toString() + "</data>" + 
        		"</body>";
        System.out.println(data);
        
        HttpResponse aaa = restPostRequest(data);
       
        String sbb = EntityUtils.toString(aaa.getEntity());
        System.out.println("Response " + sbb);
     //  StringBuffer sbb = postData(data);
     //  System.out.println("Response " + sbb);
       
        
        fileName = escape(cat + order.getAgent())  + "-" + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  "response.txt";
       System.out.println(fileName);
          fos = new FileOutputStream(fileName);
         fos.write(sbb.toString().getBytes());
         fos.close();
         if(sbb.contains("<h1>Error: Server Error") || sbb.contains("Connection refused") ||  sbb.contains("connect timed out") ||  sbb.contains("errorMessage")) {
        	 order.setDokNr(order.getDokNr()+"r");
        	 this.makeOrder(order);
        	 coun++;
        	 if(coun > 2)
            System.exit(0);	
         }
       
         
    	} catch (Exception o) {
    		o.printStackTrace();
    		this.log.info(o);
    	
    		System.exit(0);
    	}
        
        }
	} catch (Exception e) {
		e.printStackTrace();
		
	}
 }
 
 private int coun = 0;
  
  public String getDriverVersion(){
    return "RivileLedinta";
  }
  public String getPartii(String toode , String ladu , double kogus) throws SQLException {
	 	 String	sql1 = "SELECT  N06_KODAS_OS AS partii  from i17_vpro LEFT OUTER JOIN N06_OBJ AS b ON I17_KODAS_OS = N06_KODAS_OS " +
	 	 		" where I17_KODAS_IS in ('" + ladu + "') and I17_KIEKIS >= " + kogus + " and I17_KODAS_PS = '" + toode +"'"; 
	 	
	   System.out.println(sql1);
	   String acc = null;
	 Connection  con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	     acc = rs1.getString("partii").trim();

	   }
	   rs1.close();
	   st1.close();
    con.close();
	   
    return acc;

	 }
	  

  
  private String getLineNr(int i){
	  /*	String res = i + "";
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
	  	}*/
	  	return "";
	  }
  
  private HttpResponse restPostRequest(String data) throws Exception {

		 HttpPost httpPost = new HttpPost("https://api.manorivile.lt/client/v2");
		 if(data != null){
			 httpPost.setEntity(new StringEntity(data,"utf-8"));
			  httpPost.setHeader("ApiKey", "AO17LO7.wesg3cdPJ1REMkgxJ5IuXK9FYin7a4ZSTJvAwO8Q");
		        httpPost.setHeader("Accept", "application/xml");
		        httpPost.setHeader("Content-type", "application/xml;charset=utf-8");
		 }
	        return HttpClients.createDefault().execute(httpPost);

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
 		"N17_KODAS_PS AS ean, "+
 " 0 AS kogus,n17_kodas_ls_5 AS grupp, N17_KODAS_GS AS grupp2 ,N17_MEN_PAV AS minkogus ,N17_KODAS_LS_4 AS status , N17_MAX_NUOL as maks," +
 " case when N17_DATA_NAUJA>getdate() AND N17_AR_NAUJA=1 then 1 else 0 end AS uus " +
 " FROM n17_prod a  LEFT JOIN  n27_pak ON n27_kodas_ps=n17_kodas_ps left join L11_PAP1 b on b.L11_KODAS_NR = 'PSN17' and N17_KODAS_PS = L11_KODAS1 where  N17_POZ_DATE !=1" +
 "  and  N17_KODAS_PS = '" + toode +"'" ;
   
          System.out.println(cmd);

      try {
    	  con = DriverManager.getConnection(url + "meri","LEDIN_API","P455W0rd_*1");
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
          con.close();
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
		nf.setMaximumFractionDigits(2);
		return this.nf;
	}
  
  
public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
