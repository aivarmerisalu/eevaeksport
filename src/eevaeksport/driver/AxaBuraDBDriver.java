package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaBuraDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaBuraDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  
  
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
         String sql = " SELECT  cus.invoiceaccount as custid , max(accountnum) as klient,  '' AS clgroup, " +
				 "  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid,  " +
				 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount) " +  
                 "   AS price  FROM pricedisctable pd left join custtable cus on cus.accountnum = pd.accountrelation and cus.dataareaid=pd.dataareaid where  " +
				 "   pd.dataareaid = 'pre'   and cus.dataareaid = 'pre'  " +		   
				 " and  pd.fromdate<getdate() and pd.todate>getdate() " +
				 " and  pd.relation in (4,5)  AND pd.fromdate!=('1900-01-01 00:00:00.00') " +
				 "  group by cus.invoiceaccount, pd.itemrelation,pd.itemcode  order by  cus.invoiceaccount,pd.itemrelation";

          try {


        	Statement stmt = this.getNewConnection().createStatement();
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
                          }
                          mline.setDiscount(discount);

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          
                        
                          lines.add(mline);
                  }
                  rs.close();
                  
                  
                  String sql2 = " select pd.accountrelation AS clgroup, " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +  
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
				  "   pd.fromdate as algus , pd.todate as lopp from  " +
				  "   pricedisctable pd where   pd.fromdate<getdate()+3 and pd.todate>=getdate() and pd.accountcode=1 and percent1 > 0 " +
				  "   order by pd.accountrelation , pd.itemrelation " ;
                  
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

  
      DealerProduct dealerProds = new DealerProduct();
      try {

        String  sql = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu FROM INVENTSUM summ " +
               "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
               "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  "+
               "  WHERE summ.DATAAREAID='lp2'and dim.DATAAREAID='lp2' and inv.DATAAREAID='lp2' AND CLOSED = 0 AND  moduletype=0 " +
               "    and dim.inventlocationid like'%PROD%' " +
               "  GROUP BY dim.inventlocationid,summ.itemid  ORDER BY dim.inventlocationid,summ.itemid ";

           if (isDebug()) System.out.println(sql);
         
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  dealerProds.add(ladu, toode,seis+""," "," " );
              }
              rst.close();
              stm.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;
     }

  
  
  
  public TreeMap getAgents(){
    String cmd = "select emplid as kood , name AS name " +
    		"  from empltable  where (dimension ='ADM' or emplid ='10034P') and dataareaid = 'pre'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agents.put(name, agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = " SELECT cus.name,cus.custclassificationid as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
             " (SELECT sum(amountmst /( case when year(transdate) >2010 then 1 else 15.64666 end )) FROM custtransopen co WHERE co.duedate<getdate()  " +
             "   AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " +
 	         " cus.invoiceaccount end )) AS debt, cus.linedisc ,  " +
             "     cus.address, '' AS visit ,salesrep AS agent ,  numofdays AS term, blocked AS block ,  " +
			 " (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount)  " +
			 "   as payerblock ,phone + ' ' + telefax as info , linedisc as grupp FROM custtable cus left outer JOIN paymterm " +
			 " pay on cus.paymtermid=pay.paymtermid and pay.dataareaid = cus.dataareaid and  pay.dataareaid='pre' " +
			"	 WHERE   cus.dataareaid='pre' AND blocked != 2  " +
			"    order by cus.name " ;
 
 String ccc = "select invoiceaccount from custtable where dataareaid='pre' and invoiceaccount != ''  group by invoiceaccount";
 
           String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst/( case when year(co.transdate) >2010 then 1 else 15.64666 end ) AS tasumata, " +
                    "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid and  co.dataareaid=a.dataareaid" +
                    " WHERE co.dataareaid='pre' AND a.dataareaid='pre' and a.invoice != '' ORDER BY a.accountnum,co.transdate ";


       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    try {
    	Statement stmr = this.getNewConnection().createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
    	
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
        
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name = " " + alias + " " + name ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        cust.setName(name);
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(aadr.trim().replace('\n',' '));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
          
        }
        cust.addAgent(new Agent("HOUSE1"));
        
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
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='pre' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
             rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	laks = false;
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
                        debt.setSum(rs.getDouble("tasumata"));
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
    String    sql = "    SELECT itemgroupid as liik,name as nimi FROM inventitemgroup  where dataareaid ='pre'  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimi").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    //Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  /*  String    sql = " SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='tal' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
*/
    return tm;
  }

  public TreeMap getProducts(Agent agent){

    String cmd = "SELECT inv.itemid, inv.itemname AS nimetus, qtyinbox AS kastis,'' AS alusel, disc.amount AS price  " +
", inv.eancode AS ean ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat  FROM inventtable inv  " +
"left JOIN pricedisctable disc ON inv.itemid=disc.itemrelation and inv.dataareaid=disc.dataareaid " +
"left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and inv.dataareaid=mod.dataareaid" +
" WHERE disc.dataareaid='pre'   and inv.dataareaid='pre'  AND mod.dataareaid='pre'  " +
" and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') ) "+
" AND moduletype=2 and blocked = 0 " +
"  and disc.accountrelation='UUS'";
    
  

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus ="     " + kood.substring(0,2) + " " +rs.getString("nimetus").trim() + " " + kood.substring(2) ;
   
  
        prod.setInCases(rs.getDouble("kastis"));
       // prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
   
     }

      prod.setName(nimetus);

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("20");
    }
      
    /* String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>0){
            // prod.setGroupSecond(grupp2);
          }

          

          //   prod.setInUnit(rs.getDouble("yhikukogus"));
*/
        products.put(nimetus, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    //if (true) return custProds ;
 int counter = 1;
   String cmd = "SELECT max(storagelife) as life ,custaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ," +
   		"(case when jou.invoicedate " +
    " < getdate()-max(storagelife)/24 then 1 else 0 end ) as date " +
     "  FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
     "  tra.invoiceid=jou.invoiceid and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and  tra.dataareaid= cus.dataareaid " +
     "  WHERE  jou.invoicedate > getdate()-30 AND tra.dataareaid in ('pre')  AND jou.dataareaid  in ('pre')  AND cus.dataareaid  in ('pre') " +	
     "  GROUP BY custaccount,tra.itemid ,jou.invoicedate ,tra.qty  union " +
     " SELECT max(storagelife) as life ,jou.orderaccount as customer,tra.itemid ,tra.qty,jou.deliverydate AS lastdate , " +
	 "    (case when jou.deliverydate < getdate()-max(storagelife)/24 then 1 else 0 end ) as date  " +
    " FROM custpackingsliptrans  tra INNER JOIN custpackingslipjour jou ON jou.packingslipid=tra.packingslipid and tra.dataareaid= jou.dataareaid " +
  	" INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and tra.dataareaid= cus.dataareaid " +
    " WHERE  jou.deliverydate > getdate()-30 AND tra.dataareaid in ('pre')  AND jou.dataareaid  in ('pre')  AND cus.dataareaid  in ('pre')  " +
   "  GROUP BY jou.orderaccount,tra.itemid ,jou.deliverydate ,tra.qty  " +
   "  ORDER BY customer,tra.itemid  ,lastdate desc,tra.qty " ;
  
   if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
    	  double life = rs.getDouble("life");
    	  
        String kliendikood = rs.getString("customer").trim();

        String tootekood = rs.getString("itemid").trim();
        if(!custs.keySet().contains(kliendikood+ tootekood)){
		           	
		       
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
		        if(life>0){
		            cp.setRedline(rs.getString("date"));	
		        }
		        custProds.add(cp);
        }
        
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
               String lastdate = rs.getString("lastdate").substring(0,10);
                        lastdate = lastdate.substring(5,10);
               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

               cp.setRedline(rs.getString("date"));
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
    
      
      
      String cmd =" SELECT  cus.salesrep as agent,round(SUM(lineamount),0) AS summa  " +
       "  FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON  " +
       "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid  WHERE " + 
	  "	 inv.dataareaid='pre' AND sal.dataareaid='pre' AND cus.dataareaid='pre'  AND jou.dataareaid='pre' AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
      "    AND MONTH(sal.invoicedate) = MONTH(getdate())  and AssortmentGroup != 6 " +
      "  GROUP BY cus.salesrep ";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa"));
                   // System.out.println(agent.getMonthSales());
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
    return "AxaBura";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid and sal.dataareaid=rid.dataareaid  WHERE sal.dataareaid='pre' AND cus.dataareaid='pre'  AND rid.dataareaid='pre' AND cus.salesrep ='" + agent.getID() + "' AND " +
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
