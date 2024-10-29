package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaKaupmeesDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaKaupmeesDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
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

  

  /**
   *
   */
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
	   
	   Vector lines = new Vector();
/*
	      try {
	     	  String cmd = " SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode  " + 
   "  FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " + 
   "  WHERE PRI.DATAAREAID='km' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0 and PRI.AMOUNT > 0.01and PRI.CAMPAIGNCODES = 0 and " +
   " (GETDATE()>=PRI.FROMDATE AND GETDATE()<=PRI.TODATE+1)  " +
   "  GROUP BY ITEMCODE, ACCOUNTCODE, ITEMRELATION, ACCOUNTRELATION ORDER BY PRI.ACCOUNTRELATION,max(inv.mleitemsubgroup),max(inv.itemname) ";
	   
//kliendi grupi p�hine hinnakiri 
	     	  /*
	     	   * 
		SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode  
      FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation 
	   WHERE PRI.DATAAREAID='km' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0 and  PRI.CAMPAIGNCODES = 0 and
       (GETDATE()>=PRI.FROMDATE AND GETDATE()<=PRI.TODATE+1) 
       GROUP BY ITEMCODE, ACCOUNTCODE, ITEMRELATION, ACCOUNTRELATION ORDER BY PRI.ACCOUNTRELATION,max(inv.mleitemsubgroup),max(inv.itemname)
			*/

/*
	     	
	     	System.out.println(cmd);
	     	 Connection cc = getNewConnection();
	    	        Statement stmt = cc.createStatement();
	    	        ResultSet rs = stmt.executeQuery(cmd);
	    	        while(rs.next()){
	                       String custID = " ";
	                       String clGroup = " ";
	                       String prodID = " ";
	                       String prodGroup = " ";
	                       MatrixLine mline = new MatrixLine();

	                       clGroup = rs.getString("grupp");
	                       prodID = rs.getString("toode");

	                       if (custID != null && custID.trim().length() > 0) {
	                         mline.setCustID(custID.trim());
	                       }
	                       if (clGroup != null
	                           && clGroup.trim().length() > 0) {
	                         mline.setCliGroup(clGroup.trim());
	                       }

	                       if (prodID != null && prodID.trim().length() > 0) {
	                         mline.setProdID(prodID.trim());
	                       }
	                       if (mline.getProdID() == null && prodGroup != null
	                           && prodGroup.trim().length() > 0) {
	                         mline.setProdGroup(prodGroup);
	                       }

	                       lines.add(mline);
	                     }

	    	       rs.close();
	    	       con.close();
	    	       cc.close();
	                    } catch (Exception e) {
	                        getParent().showError(e);
	                    }
	*/                 
	      return lines;
	}
	 
   

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         String sql = " SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, Max(PRICEDISCTABLE.AMOUNT) AS price  " +
      "  FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='km' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND  " +
	"	 PRICEDISCTABLE.itemcode=0 and  PRICEDISCTABLE.CAMPAIGNCODES = 0 and PRICEDISCTABLE.AMOUNT > 0.01 and  " +
    "   (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1)  " +
     "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION " +
	"	order by ACCOUNTCODE,ITEMCODE";

          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String prodID = rs.getString("prodid");
                          String custID = rs.getString("custid");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();
                    
                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCustID(custID.trim());
                          }
                       
                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                     
                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                        
                          lines.add(mline);
                  }
                  rs.close();
                  
                  
                  String sql2 = "SELECT PRICEDISCTABLE.ACCOUNTRELATION as clgroup, " +
           "  PRICEDISCTABLE.ITEMRELATION as prodid, MIN(PRICEDISCTABLE.AMOUNT) AS price " +
           "  FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='km' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND " +
		"	  PRICEDISCTABLE.itemcode=0 and  PRICEDISCTABLE.CAMPAIGNCODES = 0 and PRICEDISCTABLE.AMOUNT > 0.01 and " +
         "    (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) " +
          "   GROUP BY ITEMCODE, ACCOUNTCODE, ITEMRELATION, ACCOUNTRELATION " +
	        " order by ACCOUNTCODE,ITEMCODE" ;
                  
                  rs = stmt.executeQuery(sql2);

                  while (rs.next()) {

                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();
                          
                      
                          if (clGroup != null && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                          
                          mline.setPrice(price);
                 
                          lines.add(mline);
                  }
                  
                  rs.close();
                  
                  //kampaaniad 
            
                  String sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus" +
                  		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp " +
     " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
	 "  WHERE PRI.DATAAREAID='km' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0 and  PRI.CAMPAIGNCODES = 20 and " +
     "  (GETDATE()>=PRI.FROMDATE AND GETDATE()<=PRI.TODATE+1)  and pri.AMOUNT > 0.01 " +
     "  ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                         
                         rs = stmt.executeQuery(sql4);

                         while (rs.next()) {

                                 String clGroup = rs.getString("grupp");
                                 String prodID = rs.getString("toode");
                                 double price = rs.getDouble("hind");

                                 String algus = rs.getString("algus").substring(0,10);
                                 String lopp = rs.getString("lopp").substring(0,10);
                                 int kuu = rs.getInt("kamp");
                                 MatrixLine mline = new MatrixLine();
                                 
                             
                                 if (clGroup != null && clGroup.trim().length() > 0) {
                                         mline.setCliGroup(clGroup.trim());
                                 }

                                 if (prodID != null && prodID.trim().length() > 0) {
                                         mline.setProdID(prodID.trim());
                                 }
                                 
                                  if (algus  != null && algus.trim().length() > 0) {
                                         mline.setStartDate(algus.trim());
                                 }

                                 if (lopp  != null && lopp.trim().length() > 0) {
                                         mline.setEndDate(lopp.trim());
                                 }
                                 
                                 if(kuu > 2){
                                	 mline.setProdLevel("pr");
                                 }
                                 
                                 mline.setPrice(price);
                        
                                 lines.add(mline);
                         }
                         
                         rs.close();
        
                       sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus" +
                         		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp " +
            " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
       	 "  WHERE PRI.DATAAREAID='km' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0 and  PRI.CAMPAIGNCODES = 20 and " +
            "  (GETDATE()>=PRI.FROMDATE AND GETDATE()<=PRI.TODATE+1)  and pri.AMOUNT > 0.01 " +
            "  ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                                
                                rs = stmt.executeQuery(sql4);

                                while (rs.next()) {

                                        String custID = rs.getString("grupp");
                                        String prodID = rs.getString("toode");
                                        double price = rs.getDouble("hind");

                                        String algus = rs.getString("algus").substring(0,10);
                                        String lopp = rs.getString("lopp").substring(0,10);
                                        int kuu = rs.getInt("kamp");
                                        MatrixLine mline = new MatrixLine();
                                        
                                    
                                        if (custID != null && custID.trim().length() > 0) {
                                                mline.setCliGroup(custID.trim());
                                        }

                                        if (prodID != null && prodID.trim().length() > 0) {
                                                mline.setProdID(prodID.trim());
                                        }
                                        
                                         if (algus  != null && algus.trim().length() > 0) {
                                                mline.setStartDate(algus.trim());
                                        }

                                        if (lopp  != null && lopp.trim().length() > 0) {
                                                mline.setEndDate(lopp.trim());
                                        }
                                        
                                        if(kuu > 2){
                                       	 mline.setProdLevel("pr");
                                        }
                                        
                                        mline.setPrice(price);
                               
                                        lines.add(mline);
                                }
                                
                                rs.close();
               
                         

                                sql4 = " select itemrelation as toode , amount as price from PRICEDISCTABLE where  " +
                              "  ITEMCODE=0  and ACCOUNTCODE=2  and FROMDATE<=GETDATE() and TODATE >getdate() " +
                              "  and RELATION=4 and mlepricetype=15 and CAMPAIGNCODES=1 and DATAAREAID='km'" ;
                                         
                                         rs = stmt.executeQuery(sql4);

                                         while (rs.next()) {

                                                 String prodID = rs.getString("toode");
                                                 double price = rs.getDouble("price");

                                               
                                                 MatrixLine mline = new MatrixLine();
                                            

                                                 if (prodID != null && prodID.trim().length() > 0) {
                                                         mline.setProdID(prodID.trim());
                                                 }
                                                 
                                                 mline.setPrice(price);
                                               lines.add(mline);
                                         }
                                         
                                         rs.close();
                               
                         
                  
                  

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
  
  private TreeMap kogused = null;

 public String getSaveStockInfo(){
	  
   kogused = new TreeMap();
	  
   String  cmd = "  SELECT  summ.itemid AS toode , (AVAILPHYSICAL) AS stock ,summ.inventlocationid as ladu FROM kaup_onhanddaily summ  " +  
			                   "  where  ONHANDDATE=(select MAX(onhanddate) from kaup_onhanddaily) and " +
                " (AVAILPHYSICAL) != 0 ORDER BY summ.inventlocationid,summ.itemid  ";


       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {

         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
           String dealer = rs.getString("ladu");
           if(seis > 0){
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis) + ";" + dealer  +  "\n"); // + dealer +  "\n");
             kogused.put(kood, seis + "");
           }
           }
           rs.close();
       } catch (Exception e) {
           getParent().showError(e);
       }
       return products.toString();
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

  
      DealerProduct dealerProds = new DealerProduct();
      try {

        String  sql = "    SELECT summ.itemid AS toode , sum(AVAILPHYSICAL) AS stock ,summ.inventlocationid as ladu FROM kaup_onhanddaily summ " +               
               "  WHERE summ.DATAAREAID='km' and  ONHANDDATE=(select MAX(onhanddate) from kaup_onhanddaily) " +                
               "  GROUP BY summ.inventlocationid,summ.itemid  ORDER BY summ.inventlocationid ";

           if (isDebug()) System.out.println(sql);
           Connection cc = getNewConnection();
              Statement stm = cc.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  if(seis > 0){
                	  dealerProds.add(ladu, toode,seis+""," "," " );  
                  }
                 
              }
              rst.close();
              stm.close();
              cc.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;
     }

  
  private TreeMap agentDim = new TreeMap();
  TreeMap agents1 = new TreeMap();
  public TreeMap getAgents(){
	  
    String cmd = "select emp.emplid as kood  , dir.name as name , max(inventlocationid) as ladu  from mlewarehousesalesman sal INNER JOIN empltable emp on emp.dataareaid = " +
   "sal.dataareaid and sal.emplid=emp.emplid  " +
    "  inner join dirpartytable dir on  dir.partyid=emp.partyid " + 
    "  where sal.dataareaid = 'km' and emp.dataareaid = 'km' group by emp.emplid,dir.name";
    
    
    String cmd2 = "select inventlocationid as ladu, emp.emplid as kood  from mlewarehousesalesman sal INNER JOIN empltable emp on emp.dataareaid = sal.dataareaid and sal.emplid=emp.emplid " +
    " inner join dirpartytable dir on  dir.partyid=emp.partyid  " +
      "  where sal.dataareaid = 'km' and emp.dataareaid = 'km' ";
    if (isDebug()) System.out.println(cmd);
    
    
    Properties propse =  new Properties();
    try{
    	propse =  EksportFrame.loadProps("emails.txt");
  	  
    }catch(Exception e){
    	e.printStackTrace();
    }
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("kood").trim()));       
          String name = rs.getString("name").trim();
          String ladu = rs.getString("ladu").trim();
          
          agent.setName(name);
          agent.setIsPilot(true);
          agent.setObject(ladu);
          
          agent.setLanguage("EST");
          agent.setCountry("ee");
          agent.setPassWord("0000");
          String email = " ";
          
          if(propse.get(agent.getID()) != null){
        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
          }
          
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
        
          if(email.length() > 0){
        	  agent.setActive(true);
          }
          
          
          agents.put(name, agent);
          agents1.put(agent.getID(), agent);
        }
        
         rs = stmt.executeQuery(cmd2);
        while(rs.next()){
          String ladu  = rs.getString("ladu").trim();           
          String ag = Converter.cleanLitLatEst(rs.getString("kood").trim());
           if(agentDim.get(ladu) != null){
        	   Vector vv =(Vector)agentDim.get(ladu) ;
        	   vv.add(ag);
        	   agentDim.put(ladu, vv); 
           }else{
        	   Vector vv = new Vector();
        	   vv.add(ag);
        	   agentDim.put(ladu, vv);
           }
          
        }
    } catch (Exception e){
        getParent().showError(e);
      }

    System.out.println(agentDim.toString());
    return agents;
  }

  public static float Round(float Rval, int Rpl) {
	   float p = (float)Math.pow(10,Rpl);
	   Rval = Rval * p;
	   float tmp = Math.round(Rval);
	   return (float)tmp/p;
	   }
  
  public TreeMap getCustomers(Agent agent){

	  try{
		   Converter.getManagersReplacements(agents1, rcompany, user, pass, false); 
	}catch(Exception e){

	}
	  
 String cmd = "  SELECT   cus.name,cus.custclassificationid as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " + 
             " (SELECT sum(amountmst ) FROM custtransopen co WHERE co.duedate<getdate()  and co.dataareaid = 'km'  " +
             "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  " +
 	         " cus.invoiceaccount end )) AS debt, cus.linedisc ,   " +
             "    cus.address, '' AS visit ,maincontactid  AS agent ,  (select top 1 numofdays from paymterm pt where pt.paymtermid=	" +		 
			"	 (select top 1 paymtermid from custtable where invoiceaccount = cus.invoiceaccount and dataareaid in ('kon')  ) and pt.dataareaid in ('kon','km'))    AS term, blocked AS block , " +
			 " (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = 'km') " +
			 "   as payerblock ,phone + ' , ' + telefax as info , PriceGroup as grupp  FROM custtable cus left outer JOIN paymterm  " +
			" pay on cus.paymtermid=pay.paymtermid and  pay.dataareaid='km'  " +
			"	 WHERE   cus.dataareaid in ('kon','km') AND blocked != 2   and cus.name not like'#%' " +
			 "  order by cus.name " ;
 
 
 String cont = "select custaccount as klient,name  + (case when phone != '' then ' Tel. '+ phone else '' end )  " +
 " + (case when cellularphone != '' then ' Mob. '+ cellularphone else '' end ) " +
 "+ (case when telefax != '' then ' Fax. '+ telefax else '' end ) " +
 "+ (case when email != '' then '|  '+ email else '' end )" +
 "as asi1  from contactperson  where dataareaid='kon' and name != ''";
 
 
 String ccc = "select invoiceaccount from custtable where dataareaid='kon' group by invoiceaccount";
 
           String sql = "SELECT  a.accountnum,a.orderaccount AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata,  " +
                "    co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle  FROM custtransopen co  " +
				" INNER JOIN custtrans a ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
                 "   WHERE co.dataareaid='km' AND a.dataareaid='km'  and co.duedate < getdate()+5 and a.invoice != '' " +
				"	and a.accountnum not in ('Comarket','KEILA T�','Maxima','Selver','Prisma') ORDER BY a.accountnum,co.transdate ";

           
           String salessql = " SELECT  jou.orderaccount as klient,( ltrim(str(YEAR(sal.invoicedate)) +''+ case when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
        "  round(SUM(lineamount),0) AS summa " +
	    "  FROM custinvoicetrans sal INNER JOIN custinvoicejour jou ON  " +
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
		" WHERE sal.dataareaid='km'   AND jou.dataareaid='km' AND sal.invoicedate > getdate()-400 " +
        " GROUP BY jou.orderaccount ,(YEAR(sal.invoicedate)),(month(sal.invoicedate))  order by   jou.orderaccount ,YEAR(sal.invoicedate), month(sal.invoicedate) ";

                 
           
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();
    try {
 
     	Connection conh = this.getNewConnection();
    	Statement stmh = conh.createStatement();
    	TreeMap contacts = new TreeMap();
        ResultSet rs1h = stmh.executeQuery(cont);
        while(rs1h.next()){
        	String id = rs1h.getString("klient").trim();
        	String contact = rs1h.getString("asi1").trim();
         	if(contacts.get(id) != null){
        		contacts.put(id, contacts.get(id)+"|"+contact);  
        	}else{
        		contacts.put(id, contact);  	
        	}
        }
        
       rs1h.close();
       stmh.close();
       conh.close();
    	
    	
    	
    	Statement stmr = this.getNewConnection().createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(salessql);
        while(rs1.next()){
        	String klient = rs1.getString("klient").trim();
        	String kuu = rs1.getString("kuu");
        	int summa = (int)rs1.getDouble("summa");
          if(salesres.get(klient) == null){
        	  salesres.put(klient,"   " + kuu + " : " + summa + " �" );
          }else{
        	  String asi = (String)salesres.get(klient);     	  
        	  salesres.put(klient,"   " + kuu + " : " + summa +" �|" + asi);
          }
        }
       rs1.close();
       stmr.close();
       
   	 stmr = this.getNewConnection().createStatement();
	
     rs1 = stmr.executeQuery(ccc);
    while(rs1.next()){
   payers.add(rs1.getString("invoiceaccount"));
      
    }
   rs1.close();
   stmr.close();
    	
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name = " " + alias + " " + name ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(round(Double.parseDouble(debtStr),2));
        }

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
           // cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String aadr = rs.getString("address");
        if (aadr != null) {
        	
        	
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',',')),"Estonia"));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {

            cust.addAgent(new Agent(agen.trim()));
            if(agen.trim().equals("abormann") || agen.trim().equals("jalepik")){
            	cust.addAgent(new Agent("hkynnapu"));
            }
            
            if(agen.trim().equals("kviiburg") ){
            	cust.addAgent(new Agent("msaluste"));
            	cust.addAgent(new Agent("kkuusmaa"));
            }
            
            if(agen.trim().equals("m�nd")){
            	cust.addAgent(new Agent("mand"));
            }
            
        	cust.setDealer(new Dealer(agen.trim()));
        	if(agentDim.get(agen.trim()) != null){
        		Vector vv = (Vector)agentDim.get(agen.trim());
        		for (int i = 0;i<vv.size();i++){
        			 //(new Agent((String)vv.elementAt(i)));  
        		}
        	}
        	
        	
        	 if (agen != null && agents1.get(agen.trim()) != null){
    				Agent aa = (Agent)agents1.get(agen.trim());
    			
    				Vector vec = aa.getDubbler();
    				if(vec!= null && vec.size() >0){
    					Enumeration en = vec.elements();
    				//	System.out.println("Duubel " + age.trim() + " " + vec.toString());
    					while(en.hasMoreElements()){
    						String ag = (String) en.nextElement();
    						cust.addAgent(new Agent(ag));
    					}
    				}
    			}
                 
        }
        
        if(contacts.get(cust.getID()) != null){       	
        	// cust.setPersonalData( Converter.enCodeEst(cust.getPersonalData() + "|" + contacts.get(cust.getID())) );
        }
        
        String term = rs.getString("term");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
        
        
        int block = rs.getInt("block");
        if (block == 1 || block == 2) {
          cust.setSalesBlock(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim());
        }
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART L�bim��gid :|Kuu ja aasta|m��k �|" + salesres.get(accountnum) );
        }
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
          cust.setGroup(grupp.trim());
          cust.addFixSortCode(grupp.trim());
        }
        String cmd2 =  "  SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
             "  disc.DATAAREAID='km' AND disc.RELATION=5 AND"+
            "   disc.ACCOUNTCODE=1 AND disc.ITEMCODE=1 AND disc.CURRENCY='EUR'";
             rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }

      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(round(rs.getDouble("tasumata"),2));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }


    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    this.setTreeGroup(true);
    String    sql = "  select mlemaingroup as liik , description as nimi from MLEMAINGRPTABLE where dataareaid ='km'  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik,  Converter.enCodeEst(rs.getString("nimi").trim().toLowerCase()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {
/*
  TreeMap   tm = new TreeMap();
	  Connection con = getNewConnection();
	    
	  String    sql = "  select mleitemsubgroup as liik , description as nimi from MLESUBGRPTABLE where dataareaid ='km'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik,Converter.enCodeEst( rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();
*/
    return null;
  }

  public TreeMap getProducts(Agent agent){

    String cmd = "   SELECT  disc.todate,inv.itemid, inv.itemname AS nimetus, mleblockqty AS kastis,mlepalletqty AS alusel, disc.amount AS price   " +
 ", bar.itembarcode AS ean ,inv.mleitemsubgroup AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr , " +
 " (case when lower(mlecountingunit) in('kg','l') and MLECCOUNTINGINSALES > 0 then  round(disc.amount/MLECCOUNTINGINSALES,2)    else 0 end) as unithind ," +
 " ' �/' +lower(mlecountingunit) as unitu,mlerecmargin as margin  FROM inventtable inv  " +
 "  left join pricedisctable as disc on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid " +
 " and disc.inventdimid='Axapta'and disc.itemcode=0 and disc.accountcode=2 and disc.relation=4 and disc.todate > getdate()  " +
 " left join inventitembarcode bar   on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid and bar.inventdimid ='Axapta'  and useforprinting =1 " + 
" left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
" LEFT JOIN mleitemwarehousdata u ON inv.ITEMID=u.ITEMID AND u.DATAAREAID=inv.dataareaid  AND u.inventlocationid = 'VRT' " +
" WHERE  inv.dataareaid='km'    AND mod.dataareaid='km'   " +
"  AND moduletype=2 and blocked = 0 AND inv.itemname not like '%DIETA %' " +
"  order by inv.itemid, disc.todate desc ";
    
    
    TreeMap products = new TreeMap();
    
    try { 

    	
    	
    	
    	
    	HashSet   pohisort = new HashSet();
    String    sql = "  select distinct  itemid  from mleassortmanagement where mleassortsign1 in('PS','RK') " +
    		" and dataareaid = 'km' and inventlocationid in  ('1LL','1KAD')";
    Statement st = this.getNewConnection().createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String item = rs.getString("itemid").trim();
      pohisort.add(item);
    }
    rs.close();
    st.close();
    
    
    
    HashSet   seisud = new HashSet();
    sql = " SELECT distinct summ.itemid AS toode  FROM kaup_onhanddaily summ   " + 
			           "  where   ONHANDDATE=(select MAX(onhanddate) from kaup_onhanddaily) and (AVAILPHYSICAL) != 0";
  st = this.getNewConnection().createStatement();
     rs = st.executeQuery(sql);

 while (rs.next()) {
   String item = rs.getString("toode").trim();
   seisud.add(item );
 }
 rs.close();
 st.close();
 
    
 
 
 
 HashMap   laoseis = new HashMap();
 sql = " SELECT  summ.itemid AS toode , max(case when inventlocationid ='1LL' then (AVAILPHYSICAL)else 0 end ) AS seis1 " +
							"	 ,max(case when inventlocationid ='1KA' then (AVAILPHYSICAL)else 0 end) AS seis2 " +
						    "  FROM kaup_onhanddaily summ   " +
			                "     where  ONHANDDATE=(select MAX(onhanddate) from kaup_onhanddaily) and inventlocationid in ('1LL','1KA') and " +
                            "   (AVAILPHYSICAL) != 0 " +
							"	GROUP BY summ.itemid  ";
st = this.getNewConnection().createStatement();
  rs = st.executeQuery(sql);

while (rs.next()) {
String item = rs.getString("toode").trim();
Toode tt = new Toode(item);
tt.setKogus(rs.getDouble("seis1"));
tt.setKastis(rs.getDouble("seis2"));	


laoseis.put(item, tt );
}
rs.close();
st.close();

    
    HashSet   ettesort = new HashSet();
       sql = "  select distinct  itemid  from mleassortmanagement where mleassortsign1 in('ET') " +
    		" and dataareaid = 'km' and inventlocationid in  ('1LL','1KAD')";
     st = this.getNewConnection().createStatement();
        rs = st.executeQuery(sql);

    while (rs.next()) {
      String item = rs.getString("itemid").trim();
      ettesort.add(item);
    }
    rs.close();
    st.close();
    
    
    HashSet   lisasort = new HashSet();
    sql = "  select distinct  itemid  from mleassortmanagement where mleassortsign1 in('KK','HK','SV') " +
 		" and dataareaid = 'km' and inventlocationid in  ('1LL','1KAD')";
  st = this.getNewConnection().createStatement();
     rs = st.executeQuery(sql);

 while (rs.next()) {
   String item = rs.getString("itemid").trim();
   if(seisud.contains(item)){
	   lisasort.add(item);   
   }
 
 }
 rs.close();
 st.close();
    
    if (isDebug()) System.out.println(cmd);
 

    	Statement stmt =  this.getNewConnection().createStatement();
      rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus =  " " +rs.getString("nimetus").trim() + " " + kood ;
     nimetus =  Converter.enCodeEst(nimetus);
   
       prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        if(ean.length() >4){
            add = " " +  ean.trim().substring(ll-4);	
        }
 
      }
        
        boolean goes = false;
        if(ettesort.contains(kood)){
     	//   nimetus = ".ET " + nimetus;
     	   goes = true;
        }
      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
     
    	   prod.setGroup(grupp.trim().substring(0,4));
          // prod.setGroupSecond(grupp.trim());
    
     }
       
       
   
  
       
       if(laoseis.get(kood) != null){
    	   Toode tt = (Toode)laoseis.get(kood);
    	   double lseis = tt.getKogus();
    	   double kseis = tt.getKastis();
    	   
    	   if(lseis > 0){
    		   prod.setStock((int)lseis);
    		   prod.setPartii( " Kiv: " + (int)lseis);
    	   }
    	   if(kseis >0 ){
    		 prod.setPartii(prod.getPartii() + " Kad: " + (int)kseis);
    	   }
       }
       
       //lisa kilo hind 
       
      prod.setName(Converter.enCodeEst(nimetus + add));

     
      
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("22");
    }
  
      String alegr = rs.getString("alegr");
      if(alegr!=null && alegr.trim().length()>0){
       prod.setAlegrupp(alegr);
    }
   
      double uh = rs.getDouble("unithind");
      if(uh > 0){
    	  String uu = rs.getString("unitu");
    	  prod.setPartii(prod.getPartii() + "| BH :" + NF.format(uh) + " " + uu);
      }
      
      double margin = rs.getDouble("margin");
      if(margin > 0){   	  
    	  prod.setMargin(margin + "");
      }
      
      if(lisasort.contains(kood)){
    	  goes = true;
      }
      
      if(prod.getGroup().indexOf("A041") != -1){
          goes = true;	  
      }

      
      
      if(pohisort.contains(prod.getID()) || goes){
    	  if(!prod.getGroup().equals("A032")){
        	  products.put(nimetus, prod);
              prodHash.put(prod.getID(), prod);   
          }
     
      }
       

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();

	 int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok  " +
	   "    FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid   " + 
	   "    WHERE  jou.invoicedate > getdate()-60 AND tra.dataareaid ='km'  AND jou.dataareaid = 'km'   " +
	   "    and tra.qty !=0 and tra.itemid != '' " +
	   "    GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	    "   ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty" ;

	//   and blocked = 0 
	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Statement stmt =  this.getNewConnection().createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("qty");
	  
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	     	        String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	        //cp.setAbiKood(bill);

	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	      }
	    } catch (Exception e){
	      getParent().showError(e);
	      if(counter< 4){
	      	try{
	        	 ResultSet rs = stmt.executeQuery(cmd);
	             counter++;
	             while(rs.next()){
	               String kliendikood = rs.getString("orderaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               double kogus = rs.getDouble("qty");
	         	  
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	                        double hind = rs.getDouble("hind");
	    	     	        String bill = rs.getString("dok");
	    	     	       CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	    	   	        cp.setAbiKood(bill);

	               custProds.add(cp);
	             }
	      	}catch (Exception ee){

	      	}

	      }
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
    
      
      
      String cmd ="   SELECT  cus.maincontactid as agent,  " +
     "  round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) then lineamount else 0  end),0) AS summa, " +
	 "	 round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate())-1 then lineamount else 0  end),0) AS summalast " +  
     "    FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON  " +
     "    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount " +
	 "	 WHERE inv.dataareaid='km' AND sal.dataareaid='km' AND cus.dataareaid='kon'  AND jou.dataareaid='km' AND sal.invoicedate > getdate()-400 " +
     "      and  cus.inventlocation != ''  GROUP BY cus.maincontactid ";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa"));
                   int summalast = (int)rs.getDouble("summalast");
                   agent.setStart("Eelmise aasta sama kuu :|" + summalast + " �||" );            
                  }
                }
                rs.close();
                
                cmd ="  SELECT  cus.maincontactid as agent, max(datepart(qq,sal.invoicedate)) as kvartal, " +
      "  round(SUM(case when datepart(qq,sal.invoicedate) = datepart(qq,getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) then lineamount else 0  end),0) AS summa, " +
	  "	 round(SUM(case when datepart(qq,sal.invoicedate) = datepart(qq,getdate()) and YEAR(sal.invoicedate) = YEAR(getdate())-1 then lineamount else 0  end),0) AS summalast " +  
      "  FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON   " +
      "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  " +
	  "	 WHERE inv.dataareaid='km' AND sal.dataareaid='km' AND cus.dataareaid='kon'  AND jou.dataareaid='km' AND sal.invoicedate > getdate()-400 " + 
      "  and  cus.inventlocation != ''  GROUP BY cus.maincontactid";
     
                rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                	  String kv = rs.getString("kvartal");
                	  int summa = (int)rs.getDouble("summa");
                   int summalast = (int)rs.getDouble("summalast");
                   agent.setStart(agent.getStart() + "Selle aasta " + kv  + " kvartal:|" + summa + " �||" +
                   		"Eelmise aasta " + kv + " kvartal :|" + summalast );            
                  }
                }
                rs.close();
                
                
                
                
                
                
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }
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
    return "AxaKaupmees";
    
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid  WHERE sal.dataareaid='km' AND cus.dataareaid='km'  AND rid.dataareaid='km' AND cus.salesrep ='" + agent.getID() + "' AND " +
	    " sal.ORDERDATE > getdate()-0.8  and referenceno = '' GROUP BY sal.orderid, sal.orderdate , cus.name ORDER BY sal.orderdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Vormistatud tellimused:\n\n");

	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }

	        //stmt = con.prepareStatement(sql);
	        //stmt.sets(1, agent.getObject());
	          //stmt.setString(2, agent.getObject());
	                  /* 	    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
	"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
	"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
	"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
	"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
	"inv.dataareaid='bal' AND sal.dimension='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
	"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";
   ResultSet	      rs = stmt.executeQuery(sql);
	          ResultSetMetaData md = rs.getMetaData();
	          sb.append("Nimi  \t\tLadu\t\tM��dud\t\tSumma\n");
	          sb.append("---------------------------------------------------\n");

	          double totalKokku = 0d;

	          while (rs.next()) {
	            int ladu = rs.getInt("ladu");
	            int kogus = rs.getInt("kogus");
	            double hind = round(rs.getDouble("myygihind"), 2);
	            double kokku = round(kogus*hind, 2);
	            totalKokku = round(totalKokku + kokku, 2);

	            sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

	            sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
	              kokku + "\n\n");
	           }

	          sb.append("---------------------------------------------------\n");
	          sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");

*/
	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }


	      return sb.toString();
	  }





       public NumberFormat getNF(){
         return NF;
       }






       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:AXA";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
