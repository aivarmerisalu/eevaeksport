package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaKosmelitaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter, FixReporter ,WebReporter {
  public AxaKosmelitaDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    NF.setMaximumFractionDigits(2);
    NF.setMaximumFractionDigits(2);
    NF.setGroupingUsed(false);
    NF.setMinimumFractionDigits(2);
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
 
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
   
  
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	  public String getSaveDebtInfo() {
	
   	  
   	    return null;
   	  
       }
	  
	  
 

  public Vector getFixedAssortmentData() {
    return null;
  }


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         String sql = " SELECT (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) as custid, (accountnum) as klient,  '' AS clgroup, " + 
				 "  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +  
				 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount)  " +
                 "  AS price FROM pricedisctable pd " +
                 "   left join custtable cus on cus.accountnum = pd.accountrelation  " +
                 "    and cus.DATAAREAID=pd.DATAAREAID where  " +
				 "  cus.DATAAREAID = 'kosm'   and pd.DATAAREAID = 'kosm'   " +
				 "  and  pd.fromdate<getdate() and pd.todate>getdate()" +
				 "  and  pd.relation in (4,5) and quantityamountfrom = 1 " +
				 "  group by (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) " +
                 "  ,cus.accountnum, pd.itemrelation,pd.itemcode  order by " +
				 "  (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end),pd.itemrelation ";

          try {

                  Connection ss = this.getNewConnection();
        	      Statement stmt = ss.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                          String custID = rs.getString("custid");
                          String klient = rs.getString("klient");
                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();
                      
                          if (klient != null && klient.trim().length() > 0) {
                              mline.setCustID(klient.trim());
                                }   
                          if (custID != null && custID.trim().length() > 0) {
                              mline.setCustID(custID.trim());
                               }
                          
                     
                          if (mline.getCustID() == null && clGroup != null
                                          && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                          if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0) {
                                  mline.setProdGroup(prodGroup);
                                  mline.setDiscGroup(prodGroup);
                          }
                          mline.setDiscount(discount);

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          
                        
                          lines.add(mline);
                  }
                  rs.close();
                  
                  
                  String sql2 = "  select pd.accountrelation AS clgroup,  " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
				  "   pd.fromdate as algus , pd.todate as lopp from  " +
				  "  pricedisctable pd where   pd.fromdate<getdate()+3 and pd.todate>=getdate() and pd.accountcode=1 and percent1 > 0 and pd.dataareaid ='kosm' " +
				  "   order by pd.accountrelation , pd.itemrelation" ;
                  
                  rs = stmt.executeQuery(sql2);

                  while (rs.next()) {

                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          double discount = rs.getDouble("discount");
                          String fromdate = rs.getString("algus");
                          String todate = rs.getString("lopp");
                          MatrixLine mline = new MatrixLine();
                          
                      
                          if (clGroup != null && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                          if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0) {
                                  mline.setProdGroup(prodGroup);
                          }
                          mline.setDiscount(discount);
                          mline.setStartDate(fromdate.substring(0,10));
                          mline.setEndDate(todate.substring(0,10));
                     
                        
                          lines.add(mline);
                  }
                  
                  
                  rs.close();
                  stmt.close();
                  ss.close();

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }
  
 


  public String getAssortmentStr(Agent agent){
  	return null;
  }

      public String getSaveStockInfo(){
      return null;
      }


              public String getDealerCustomerCSV(String agent){
               return null;
               }

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }


  /**
   *
   */
  public void connectionDown(){
  }

  
  public DealerProduct getDealerProductsList() {

  return null;
     }

  
  TreeMap agnames = null;
  
  public TreeMap getAgents(){

    String cmd = " select w.value as kood , w.name as name from  DIMATTRIBUTEHCMWORKER w   inner join HCMWORKER h on h.personnelnumber = w.value \r\n" + 
    		"			 inner join custtable c on c.MAINCONTACTWORKER=h.recid where w.value like 'G%' group by  w.value  , w.name";// +
    
    Properties propse =  new Properties();
    try{
    	propse =  EksportFrame.loadProps("emails.txt");
    }catch(Exception e){
    	e.printStackTrace();
    }
	
    
    Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    StringBuffer sb2 = new StringBuffer();
    agnames  = new TreeMap();
    try {
   	 Connection con = getNewConnection();
     Statement   stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);


     		String agentAGENTS = props.getProperty(agent.getID());
   		if(agentAGENTS != null){
              StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
   	    while(stag.hasMoreTokens()){  	
   	    	agent.addDubbler(stag.nextToken());
            System.out.println("Lisan Duubleid " + agent.getID() + " " + agent.getDubbler().toString());
   	       }
            }

        agent.setLanguage("LIT");
        agent.setCountry("lt");
        agent.setPassWord("0000");
String email = " ";
        
        if(propse.get(agent.getID()) != null){
      	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
        }
        
        
        if(email.length() > 3){
      	  agent.setEmail(email);
        }
        if(email.length() > 3){
      	  agent.setActive(true);
        }
        
        
        
          
          
          agents.put(name + agent.getID(), agent);
          agents2.put(agent.getID(), agent);
          if(props.containsKey(agent.getID())){
        	  agnames.put(agent.getID(), name);
              sb2.append(agent.getID() + ";" + name + "\n");  
          }
          
        }
        rs.close();
        stmt.close();
        con.close();
        
    } catch (Exception e){
        getParent().showError(e);
      }
  
  
    if(rcompany != null){
    	Converter.sendAgentJson(agents, rcompany, false);
    }
    
    return agents;
  }
 
  
  
  
  private  TreeMap agents2 = new TreeMap(); 
  

 


  

 
  public TreeMap getCustomers(Agent agent){

	
	  System.out.println("Alustan Kliendid");
	  

 String ccc = "select invoiceaccount from  custtable where dataareaid='kosm' and invoiceaccount is not null  group by invoiceaccount";
 
           String sql = " SELECT  a.ORDERACCOUNT AS klient,a.invoice AS numbe,co.duedate AS maksepaev,sum(co.amountmst) AS tasumata, " +
                 "   co.transdate AS kuupaev , Datediff (dayofyear, co.duedate, getdate()) AS yle FROM  custtransopen co with(nolock) " +
				 "   INNER JOIN  custtrans a ON co.REFRECID=a.recid and  co.DATAAREAID =a.DATAAREAID    " +
                 "    WHERE co.dataareaid='kosm' AND a.dataareaid='kosm'	 " +
				 " group by a.invoice, a.ORDERACCOUNT,co.duedate, co.transdate  ORDER BY co.transdate,a.invoice, a.ORDERACCOUNT,co.duedate ";

           
           String salessql = "    SELECT  jou.orderaccount as klient,round(SUM(lineamount),0) AS summa,MONTH(sal.invoicedate) as kuu " +  
        " FROM  custinvoicetrans sal  with(nolock) INNER JOIN  custinvoicejour jou ON  sal.invoiceid  =jou.invoiceid   and " + 
       "  jou.DATAAREAID =sal.DATAAREAID  WHERE " + 
	   " sal.dataareaid='kosm' AND sal.invoicedate > getdate() -390  GROUP BY jou.orderaccount,MONTH(sal.invoicedate) " +
	   " ORDER BY jou.orderaccount,MONTH(sal.invoicedate)" ;
               
           
           
           
           String sss = "SELECT  accountnum, sum(case when co.duedate<getdate() then amountmst else 0 end) as asi ," +
           		"sum(amountmst) as asi2,0 as tara FROM  custtransopen co " + // sum(case when DebtType ='4'  then amountmst else 0 end)
           		"WHERE co.dataareaid='kosm'   " +
           "  group by  accountnum";
           
           
          

           
           
           String cmd2 = " SELECT disc.accountrelation as klient ,disc.percent1 AS discount FROM  pricedisctable disc  WHERE " +
                 "   disc.dataareaid='kosm' AND  ltrim(disc.itemrelation)='0'  AND fromdate<=getdate() and todate >= getdate() ";
          
           TreeMap aled = new TreeMap();
           Connection con = null;
           try{
                 con = this.getNewConnection();
        	   Statement stm = con.createStatement();
        	  ResultSet rs1 = stm.executeQuery(cmd2);
               while(rs1.next()){
            	   String klient = rs1.getString("klient").trim();
                 double ale = rs1.getDouble("discount");
                aled.put(klient, ale + "");
               }
              rs1.close();  
              stm.close();
            
           }catch (Exception r){
        	   System.out.println(cmd2);
        	   r.printStackTrace();
           }
           

     	  System.out.println("Alustan Kliendid 1");
           try{
               
               String fff = " select custaccount as klient,name as nimi ,  " +
  " case when birthyear  > 1930 then CONVERT(date, ( CAST(birthyear AS varchar) +'-'+ CAST(birthmonth AS varchar)  +'-'+ CAST(birthday AS varchar) ), 126) else null end " +
  "   as synna , (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactemail)  as email, " +
  " (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone)  as phone " +
  "  from  Contactperson co inner join DirPartyTable dp ON co.partY=dp.REcid where dataareaid='kosm'";
              

                   StringBuffer sb2 = new StringBuffer();
                
            	   Statement stm = con.createStatement();
            	  ResultSet rs1 = stm.executeQuery(fff);
                   while(rs1.next()){
                	   String klient = rs1.getString("klient").trim();
                	   String nimi = rs1.getString("nimi").trim();
                	   java.sql.Date synna = rs1.getDate("synna");
                	   if(synna == null){
                		   synna = new java.sql.Date(0);
                	   }
                	  String email = parent.make(rs1.getString("email"));
                	  String phone = parent.make(rs1.getString("phone"));
              //	  String cell = parent.make(rs1.getString("cellularphone"));
                    sb2.append(klient+";"+ nimi + ";" + synna.getTime() + "; ;" + phone  + ";" + email +  "\n");
                   }
                  rs1.close();  
                  stm.close();
                  if(sb2.length() > 2){
                     		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
                     	}
                  

              
           }catch (Exception r){
        	   System.out.println(cmd2);
        	   r.printStackTrace();
           }

        
     	  System.out.println("Alustan Kliendid 2");
           
       //    tal_visitationdays ,talspartnercode,

     	 String cmd = "   SELECT dp.name,street as nimelisa,dp.namealias,cus.accountnum, \r\n" + 
     	 		"             adr.street as tanav,adr.city as linn, adr.county as regioon,adr.zipcode as indeks ,cus.invoiceaccount AS payer, \r\n" + 
     	 		"             0 AS debt, cus.linedisc , \r\n" + 
     	 		"             adr.address ,(Select h.personnelnumber from HCMWORKER h WITH (NOLOCK) where MAINCONTACTWORKER = h.recid) AS agent ,  numofdays AS term, cus.blocked AS block , \r\n" + 
     	 		"               (select max (blocked) from  custtable cc  WITH (NOLOCK) where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = cus.dataareaid ) \r\n" + 
     	 		"             as payerblock ,(select l.locator  from  LOGISTICSELECTRONICADDRESS l WITH (NOLOCK)  where l.recid = dp.primarycontactphone)  \r\n" + 
     	 		"			  as info , linedisc as grupp, 0 as minord , \r\n" + 
     	 		"               '' as transa,'' as discount ,'' as samm , '' as tyyp ,'' as tyyp2   \r\n" + 
     	 		"             FROM  custtable cus with(nolock) inner join DirPartyTable dp with(nolock) ON cus.partY=dp.REcid  inner join \r\n" + 
     	 		"	 		 LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO left outer JOIN  paymterm \r\n" + 
     	 		"            pay on cus.paymtermid = pay.paymtermid  and pay.DATAAREAID  = cus.DATAAREAID  \r\n" + 
     	 		"           WHERE   (cus.dataareaid='kosm'  and (Select h.personnelnumber from HCMWORKER h WITH (NOLOCK) where MAINCONTACTWORKER = h.recid) like 'G%') ";

     	  
     	  
     	  
  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap custDebt = new TreeMap();
  
    TreeMap totalDebt = new TreeMap();
   
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();
    try {
    	ResultSet rs1 ;
    	Statement stmr ;
    	try{
    		 stmr = con.createStatement();
        	 rs1 = stmr.executeQuery(salessql);
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim();
            	
            	int summa = (int)rs1.getDouble("summa");
            	String kuu = rs1.getString("kuu");
              if(salesres.get(klient) == null){
            	  salesres.put(klient,"   " + kuu + " : " + summa + " €" );
              }else{
            	  String asi = (String)salesres.get(klient);     	  
            	  salesres.put(klient,"   " + kuu + " : " + summa +" €|" + asi);
              }
            }
           rs1.close();
           stmr.close();
     	}catch(Exception s){
    		
    	}
    	    	
    	

  	  System.out.println("Alustan Kliendid 3");
  	  try {
  	   Statement stmu = con.createStatement();
    	
         rs1 = stmu.executeQuery(sss);
        while(rs1.next()){
        	
       String ee = rs1.getString("accountnum").trim();
       double debti = rs1.getDouble("asi");
       if(debti != 0){
    	   custDebt.put(ee, debti + "");
       }
       double alld = rs1.getDouble("asi2");
       if(alld != 0){
    	   totalDebt.put(ee, alld + "");
       }
    
          
        }
       rs1.close();
       stmu.close();
  	  }catch(Exception k) {
  		  k.printStackTrace();
  	  }

	//  System.out.println("Alustan Kliendid 4");
    	
        stmr = con.createStatement();
    	 rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
    	

 	  System.out.println("Alustan Kliendid 5");
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = rs.getString("name").replaceAll("UAB", " ").trim();
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name =  name + " " + alias ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
       	
        
        String tanav = rs.getString("tanav");
        if (tanav != null && tanav.length() > 0){
          cust.restStreet = tanav.trim();
        }
        String linn = rs.getString("linn");
        if (linn != null && linn.length() > 0){
          cust.restCity = linn.trim();
        }
        String regioon = rs.getString("regioon");
        if (regioon != null && regioon.length() > 0){
        	cust.restCity = (cust.restCity + " " + regioon.trim()).trim();
        }

        String indeks = rs.getString("indeks");
        if (indeks != null && indeks.length() > 0){
          cust.restZipCode = indeks.trim();
        }
      
        name = name.replaceAll("\\r\\n|\\r|\\n", " ");
        name = name.replaceAll("'", " ").replaceAll("\"", " ").replaceAll("  ", " ");
      
        cust.restName = name;
        cust.setCountry("Lithuania");


        name = name + " " + accountnum;
        cust.setName( Converter.enCodeEst(name.trim()));
        
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
     
        cust.setPayer(payer);
        String debtStr = (String)custDebt.get(payer);
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }
        String totalStr = (String)totalDebt.get(payer);
        if (totalStr != null){
          cust.setUsedCredit(Double.parseDouble(totalStr));
        }
        
        
      

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } 
        
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Pardavimai :|Menuo ir metai|Pardavimai €|" + salesres.get(accountnum) );
        }
        

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Lithuania").replaceAll("\\r\\n|\\r|\\n", " "));
        }
     
        String agen = rs.getString("agent");
      
        if (agen != null && agen.trim().length() >0) {
          cust.addAgent(new Agent(agen.trim()));
    
       
          cust.setMainAgent(agen.trim());
          
          
          Agent agg = ((Agent)this.agents2.get(agen.trim()));
			
			if(agg != null && agg.getDubbler().size()>0){
				for (int e = 0 ; e < agg.getDubbler().size() ; e++){
					cust.addAgent(new Agent((String)agg.getDubbler().elementAt(e)));
				}
          	}
        }
        
      
        
        
       
        int term = rs.getInt("term");
        if (term > 0) {
          cust.setPaymTerm(term + "");
        }
        int block = rs.getInt("block");
        if (block == 1 || block == 2) {
          cust.setSalesBlockT(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1 || payerblock == 2) {
          cust.setSalesBlockT(true);
        }
        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim().replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        double  minord  = rs.getDouble("minord");
        if (minord > 0 ) {
          cust.setMinsum(minord);
        }
        
        String transa = rs.getString("transa");
        if (transa != null && transa.trim().length()>0) {
          cust.setCity("Marsruto numeris: " +transa.trim());
          
        }
        String rounding = rs.getString("samm");
        if (rounding != null && rounding.trim().equals("2")) {
          cust.setMakeAmountRounding(true);        
        }
        
        if(aled.get(cust.getID())!= null){
        	cust.setDiscount(Double.parseDouble((String)aled.get(cust.getID())));
        }
        
        String tyyp = rs.getString("tyyp");
        if (tyyp != null && tyyp.trim().length() > 0) {
        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("Tipas :" + tyyp.trim());
        	}else{
        		cust.setCity(cust.getCity() + "| Tipas :" + tyyp.trim());
        	}
          cust.setMakeAmountRounding(true);        
        }
        
        String tyyp2 = rs.getString("tyyp2");
        if (tyyp2 != null && tyyp2.trim().length() > 0) {

        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("Apyv.lygis :" + tyyp2.trim());
        	}else{
        		cust.setCity(cust.getCity() + "| Apyv.lygis :" + tyyp2.trim());
        	}     
        }
        
      
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    
      }
      rs.close();

	  System.out.println("Alustan Kliendid 6");
try {
	  rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        
                        String number = rs.getString("numbe").trim();
                        if(number != null && number.trim().length() > 0){
                            debt.setDoknr(number.replace(';',','));
                         }else{
                        	debt.setDoknr("NoNumber");
                        }
                        
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt); 	
                      }
                      rs.close();
}catch(Exception r) {
	r.printStackTrace();
}
                      stmt.close();
                      con.close();

                	  System.out.println("Alustan Kliendid 7 ");

    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    if(rcompany != null){
    // 	Converter.sendCustomerJson(customers, rcompany,false,user,pass);
     }
    System.out.println("Kliendid " + customers.size());
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  //
  

  
      public String getDistribution() {
        return null;
      }

     
    
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  //  String    sql = "   SELECT PRODUCTLINEID as liik,name as nimi FROM  inventproductline  where dataareaid ='kosm'  ";
 
    String    sql = " 	Select e.PARENTCATEGORY as liik ,  (Select name from ECORESCATEGORY a where a.RECID = e.PARENTCATEGORY) as nimi from ECORESPRODUCTCATEGORY g \r\n" + 
    		"	 inner join ECORESCATEGORY e on e.RECID = g.CATEGORY where  g.categoryhierarchy in ('5637146827','5637146826') group by e.recid , e.name ,PARENTCATEGORY ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    HashSet names = new HashSet();
    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      String nim = rs.getString("nimi").trim();
      if(nim.indexOf(" ") != -1){
    	  nim = nim.substring(nim.indexOf(" ")).trim();
      }
      if(names.contains(nim)) {
    	  nim = nim + " .";
      }else {
    	  names.add(nim);
      }
        tm.put(liik,  Converter.enCodeEst(nim));
    }
    rs.close();
    st.close();
    con.close();
 
    return tm;
}


  
  public TreeMap getProdGroupsSecond() throws SQLException {
	  
	    Connection con = getNewConnection();
	    TreeMap tm = new TreeMap();
	    
	    String sql = "Select e.recid as liik , e.PARENTCATEGORY as liik2, e.name as nimi from  ECORESPRODUCTCATEGORY g \r\n" + 
	    		"	 inner join ECORESCATEGORY e on e.RECID = g.CATEGORY where  g.categoryhierarchy in ('5637146827','5637146826') group by e.recid ,PARENTCATEGORY, e.name";
	   
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String liik2 =  rs.getString("liik2").trim();
	        tm.put(liik2+liik,  Converter.enCodeEst(rs.getString("nimi").trim()));
	    }
	    rs.close();
	    st.close();
	    con.close();

	    return tm;
  }
  

  
  

  public TreeMap getProducts(Agent agent){
	   HashMap prodHash = new HashMap();
	   HashMap<String, ArrayList<String>> lineDiscs = new HashMap<String, ArrayList<String>>();
	   
    String cmd = " SELECT inv.itemid, inv. namealias AS nimetus, DISPLAYVALUE  AS kastis,'' AS alusel, disc.amount AS price \r\n" + 
    		"        ,CATEGORY AS grupp2, PARENTCATEGORY  AS grupp , \r\n" + 
    		"   ww.displayvalue as grkood, 0 as grid,mod.unitid As unit, 20 as vat , \r\n" + 
    		"     0  as samm ,sortcode as sorting  ,  0 as nosales ,linedisc as prgrupp FROM  inventtable inv \r\n" + 
    		"	 INNER JOIN  ECORESPRODUCT i  on i.DISPLAYPRODUCTNUMBER = inv.ITEMID\r\n" + 
    		"	 INNER JOIN ECORESPRODUCTCATEGORY g on g.product = i.RECID and g.categoryhierarchy in ('5637146827','5637146826') \r\n" + 
    		"	 inner join ECORESCATEGORY e on e.RECID = g.CATEGORY\r\n" + 
    		"         left  JOIN  pricedisctable disc ON inv.itemid = disc.itemrelation and accountcode = 0  and \r\n" + 
    		"     disc.amount != 0  and (disc.todate>getdate()  or   YEAR(disc.todate) =1900)   and disc.currency = 'EUR' and disc.dataareaid='kosm' \r\n" + 
    		"       left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid and inv.DATAAREAID =mod.DATAAREAID  \r\n" + 
    		"   left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = inv.defaultdimension  \r\n" + 
    		"		 and  dimensionattribute = 5637145336 \r\n" + 
    		"    WHERE   moduletype=2  and  inv.dataareaid = 'kosm' ";
    //z p algusega
  
    
    String sss = "select bb.itemid, itembarcode from inventitembarcode  bb \r\n" + 
    		"    		 left join inventdim dd on bb.inventdimid = dd.inventdimid and dd.DATAAREAID=bb.DATAAREAID\r\n" + 
    		"    		 left join inventtable tbl on bb.itemid = tbl.itemid and tbl.DATAAREAID=bb.DATAAREAID\r\n" + 
    		"    		 where bb.dataareaid = 'kosm' and tbl.standardconfigid = dd.configid and tbl.DATAAREAID='kosm'";

 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
     Connection ccc = getNewConnection();
    	Statement stmt =  ccc.createStatement();
      ResultSet rs = stmt.executeQuery(sss);
      
      
      HashMap eans = new HashMap();
      while(rs.next()){
    	  String kood = rs.getString("itemid").trim();
    	  String ean = rs.getString("itembarcode").trim();
    	  eans.put(kood, ean);
      }
      rs.close();
      
      rs = stmt.executeQuery(cmd);
   
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst( rs.getString("nimetus").trim() + " " + kood) ;
   
  
        prod.setInCases(rs.getDouble("kastis"));
       // prod.setOnPallet(rs.getDouble("alusel"));
        Double price = rs.getDouble("price");
        if(price != null) {
        	 prod.setPrice(price);
        }else {
        	prod.setPrice(1);
        }
       
       
         if(eans.get(kood) != null) {
    	  prod.setEAN(eans.get(kood)+"");
    	 
        }
         String add = "";
         String gruppc = rs.getString("grupp2");
      
         
   
      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
     }
       if(gruppc!=null && gruppc.trim().length()>0){
           gruppc = grupp + gruppc.trim();
           prod.setGroupSecond(gruppc);
        }
   
       
      prod.setName((nimetus.replaceAll("'", " ").replaceAll("\"", " ").trim()).replaceAll("\n", " ").trim());

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("21");
    }

     
      
      String top = rs.getString("sorting");
      if(top != null && top.trim().length()>0 && top.trim().equals("TOP")){
       prod.setStatusTop("1");
    }
      
      String nosales = rs.getString("nosales");
      if(nosales != null && nosales.trim().length()>0 && nosales.trim().equals("1")){
       prod.noOrder = true;
    }
      
      
      
      String prgrupp = rs.getString("prgrupp");
      if(prgrupp != null && prgrupp.trim().length()>0 ){
         prod.setDiscgrupp(prgrupp.trim());
         ArrayList<String> arr = lineDiscs.get(prgrupp.trim());
         if(arr == null) {
        	 arr = new ArrayList<String>();
         }
         arr.add(prod.getID());
         lineDiscs.put(prgrupp.trim(), arr);
       }
        products.put(add + nimetus, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      stmt.close();
      ccc.close();
    } catch (Exception e){
    	e.printStackTrace();
      getParent().log(e.toString());
      
    	//System.exit(0);
    }

    
    
	  cmd = " SELECT (case when pd.itemcode = 1 then pd.itemrelation else ''end) as grupp , accountrelation  as klient ,amount AS hind, quantityamountfrom as kogus\r\n" + 
	  		"				   ,(percent1) AS perc, (case when pd.itemcode = 1 then pd.itemrelation else ''end) as linedisc  FROM pricedisctable pd \r\n" + 
	  		"                    left join custtable cus on cus.accountnum = pd.accountrelation  \r\n" + 
	  		"                     and cus.DATAAREAID=pd.DATAAREAID where  \r\n" + 
	  		"				   cus.DATAAREAID = 'kosm'   and pd.DATAAREAID = 'kosm'   \r\n" + 
	  		"				   and  pd.fromdate<getdate() and pd.todate>getdate()\r\n" + 
	  		"				   and  pd.relation in (4,5) and quantityamountfrom > 1  ";
	 
		
	    StringBuffer buf = new StringBuffer();
		try {
			
		Connection	 con = this.getNewConnection();
		 Statement    stmt =  con.createStatement();
		  ResultSet    rs = stmt.executeQuery(cmd);

	           while(rs.next()){
	        	        String  gruppx = rs.getString("grupp").trim();
	        	        String  klient = rs.getString("klient").trim();
	        	        if(lineDiscs.get(gruppx) != null) {
	        	        ArrayList<String> arr = lineDiscs.get(gruppx);	
	        	     	double  kogus = rs.getDouble("kogus");
	                  	double  perc  = rs.getDouble("perc");
	        	        for (String toodex : arr) {
	                 
	                  		
	                  		Product pp = (Product)prodHash.get(toodex);
	                  		if(pp != null) {
	                  		
	                  		double  hind  = pp.getPrice();
		                 
		                  	double  lined = 0;
		                  	
		                  	if(kogus == 0){
		                  		kogus = 1;
		                  	}
		                  	
		                  	double hl = (hind -lined) *(100-perc)/100;
		                  	
		                  	// siia teha number format 
		                  	
		                  	      buf.append(toodex+ "; ; ;" + NF.format(hl).replace(',', '.') + ";" + gruppx + ";" + kogus + ";-1; ;-1;" +klient  );
		                          buf.append("\n");	
		                      	
	                  		}
	                  		}
	        	        }
	                  }	
	           rs.close();
	           stmt.close();
	           con.close();
		}catch (Exception e ){
			e.printStackTrace();
		}
    
    if(buf.length() > 2){
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write(Converter.cleanLitLatEst(buf.toString()));
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }else{
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write("");
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    
    
    System.out.println("PRODUCTS " + products.size());
    return products;
  }



  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    //if (true) return custProds ;
    
System.out.println("CustProdlisti tegemine ");
 int counter = 1;
   String cmdu = " SELECT /*+ leading(tra,jou) use_hash(tra,jou) */orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate " +
     "  FROM  custinvoicetrans  tra INNER JOIN  custinvoicejour jou ON " +
     "  tra.invoiceid =  jou.invoiceid  and  tra.DATAAREAID = jou.DATAAREAID  " +
     " and tra.SALESID  = jou.SALESID   WHERE  jou.invoicedate > getdate()-180 AND tra.DATAAREAID  = ('kosm')  AND jou.DATAAREAID   = ('kosm')  " +
     "  GROUP BY orderaccount,tra.itemid,tra.name ,jou.invoicedate ,tra.qty  Order by orderaccount,tra.name,jou.invoicedate desc " ;
  

   if (isDebug()) System.out.println(cmdu);

    try {

    	Connection ccc = this.getNewConnection();
    	Statement stmt =  ccc.createStatement();
      ResultSet rs = stmt.executeQuery(cmdu);
      counter++;
      while(rs.next()){
    	
        String kliendikood = rs.getString("customer").trim();
        String tootekood = rs.getString("itemid").trim();
        if(!custs.keySet().contains(kliendikood+ tootekood)){
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(4);
		        custs.put(kliendikood + tootekood,lastdate); 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
		        custProds.add(cp);
        }
        
      }
      rs.close();
      stmt.close();
      ccc.close();
     
 
      
      
    } catch (Exception e){
      getParent().showError(e);
    }
  
  System.out.println("CustProdlisti tegemine TEHTUD ");
    return custProds;
  }

 
  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  

    public void setBulkSales(Map agents){
  
          }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "AxaZpienas";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private HashMap brandplans = null; 
  private HashMap typeplans = null;  
  private HashMap typebrand = null; 
  

  public String getPilotDayData(Agent agent){


	      return null;
	  }





       public NumberFormat getNF(){
         return NF;
       }



       @Override
       public void WebReporter() {
     	 
     	  System.exit(0);
       }
       
       

       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:kosmelitaaxa";

  private   Vector debt = new Vector();

  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
	@Override
	public void FixReporter() {
	
	}
	
	
}
