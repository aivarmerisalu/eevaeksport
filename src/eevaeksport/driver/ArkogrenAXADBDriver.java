package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
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
 * @author Tarmo Tubro
 * @version 1.0
 */

public class ArkogrenAXADBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter {
  public ArkogrenAXADBDriver(EksportPanel parent){
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
  }

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
     return null;
   }

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          String sql = " SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, " +
				   "(case when accountcode = 1 then accountrelation else ''end)  AS clgroup, "+
				   "(case when itemcode = 0 then itemrelation else ''end) AS prodid, " +
				   "(case when itemcode = 1 then itemrelation else ''end) AS prodgroup,percent1 AS discount,amount " +
                   " AS price FROM pricedisctable where" +
				   " fromdate<getdate() and todate>getdate() AND " +
				  " fromdate!=('1900-01-01 00:00:00.00') and dataareaid = 'ark'";

          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                          String custID = rs.getString("custid");
                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();

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

  public TreeMap getAgents(){
    String cmd = "SELECT e.emplid AS kood ,e.del_name as name ,e.arkmobilejavawarehouse AS ladu FROM custtable k INNER JOIN empltable e" +
    		" ON emplid=arksalesadministrator and  k.dataareaid= e.dataareaid" +
	" WHERE e.dataareaid='ark' AND k.dataareaid='ark'  group by e.del_name ,e.emplid,arkmobilejavawarehouse " +
    " order by arkmobilejavawarehouse,e.del_name,e.emplid";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name");
          if(name == null || name.length() == 0){
        	  name = agent.getID();
          }
          agent.setName(name);
          agent.setIsPilot(true);
          agent.setObject(rs.getString("ladu").trim());
          agents.put(name, agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("KART");
    aa.setName("KART");
    agents.put("KART", aa);
    
    
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = " SELECT cus.name,cus.namealias,cus.accountnum,(case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end )AS payer, " +
             " (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate() and co.dataareaid='ark' " +
             "   AND co.accountnum = cus.accountnum) AS debt, cus.linedisc ,  " +
              "    cus.address, '' AS visit ,arksalesadministrator AS agent ,  numofdays AS term, blocked AS block , " +
			  " (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount) " +
			  "  as payerblock ,phone + ' '  + telefax as info FROM custtable cus LEFT JOIN paymterm  pay on cus.paymtermid=pay.paymtermid and cus.dataareaid=pay.dataareaid "+
			"	 WHERE   (cus.dataareaid='ark' and  pay.dataareaid='ark' AND blocked != 2 and  len(arksalesadministrator)>0 and arksendtopda =1 )  " +
			"    order by cus.name" ;
 
           String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
                    "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid " +
                    " and co.dataareaid = a.dataareaid WHERE co.dataareaid='ark' AND a.dataareaid='ark' ORDER BY a.accountnum,co.transdate ";


       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
        String alias = rs.getString("namealias");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name = name + " " + alias;
        }
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(Converter.enCodeEst(name));
        cust.setPayer(rs.getString("payer"));
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Estonia"));
        }
        
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
          if(agen.trim().equals("K�RT")){
        	  cust.addAgent(new Agent("KART"));
          }
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
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='ark' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
        ResultSet rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
        customers.put(name, cust);
        custMap.put(cust.getID(),cust);
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
        String    sql = " SELECT ve.accountnum AS liik, ve.name AS nimetus  FROM vendtable ve INNER JOIN inventtable t " +
        		"ON t.primaryvendorid=ve.accountnum  and  t.dataareaid= ve.dataareaid WHERE ve.dataareaid='ark' and  t.dataareaid='ark'  group by ve.accountnum, ve.name ";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String liik = rs.getString("liik").trim();
            tm.put(liik, Converter.enCodeEst(liik.substring(0,3) + " " + rs.getString("nimetus").trim()));
        }
        rs.close();
        st.close();
        con.close();

        return tm;
    }


      public TreeMap getProdGroupsSecond() throws SQLException {
    	  

    		    Connection con = getNewConnection();
    		    TreeMap   tm = new TreeMap();
    		    String    sql = "select category1+category2 as liik , category1+category2 +' '+ name as nimetus from arkitemcategory2table where dataareaid ='ark' and name !='' ";
    		    Statement st = con.createStatement();
    		    ResultSet rs = st.executeQuery(sql);

    		    while (rs.next()) {
    		      String liik = rs.getString("liik").trim();
    		        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
    		    }
    		    rs.close();
    		    st.close();
    		    con.close();

    		    return tm;
      }

  public TreeMap getProducts(Agent agent){

	    //if (true) return custProds ;
	  int counter = 1;
	    String cmd4 = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate " +
	       " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
	       " tra.invoiceid=jou.invoiceid and jou.dataareaid=tra.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid  and cus.dataareaid=tra.dataareaid" +
	       " WHERE  jou.invoicedate > getdate()-450 AND tra.dataareaid ='ark'  AND jou.dataareaid = 'ark'  AND cus.dataareaid = 'ark' " +
	       " GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	       " ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " ;


	     if (isDebug()) System.out.println(cmd4);

	     try {

	     	Statement stmt =  this.getNewConnection().createStatement();
	       ResultSet rs = stmt.executeQuery(cmd4);
	       counter++;
	       while(rs.next()){
	         String kliendikood = rs.getString("orderaccount").trim();
	         String tootekood = rs.getString("itemid").trim();
	         double kogus = rs.getDouble("qty");
	         String lastdate = rs.getString("lastdate").substring(0,10);
	                  lastdate = lastdate.substring(5,10);
	                  
	         CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");

	         //cp.setRedline(rs.getString("date"));
	         custProds2.add(tootekood);
	         custProds.add(cp);
	       }
	     } catch (Exception e){
	       getParent().showError(e);
	       if(counter< 4){
	       	try{
	         	 ResultSet rs = stmt.executeQuery(cmd4);
	              counter++;
	              while(rs.next()){
	                String kliendikood = rs.getString("orderaccount").trim();
	                String tootekood = rs.getString("itemid").trim();
	                String lastdate = rs.getString("lastdate").substring(0,10);
	                         lastdate = lastdate.substring(5,10);
	                CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

	                cp.setRedline(rs.getString("date"));
	                custProds.add(cp);
	                custProds2.add(tootekood);
	              }
	       	}catch (Exception ee){

	       	}

	       }
	     }
	  
	  
	  
	  
    String cmd = " SELECT inv.itemid,(inv.itemname) AS nimetus, arkboxqty AS kastis,  mod.price AS price  "+
              " , arkitembarcode AS ean,primaryvendorid AS grupp ,arkitemcategory1 + arkitemcategory2 AS grupp2,mod.unitid AS unit " +
			  "   FROM inventtable inv  " +
              "   INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid and inv.dataareaid=mod.dataareaid " +
              "   WHERE   inv.dataareaid='ark' AND mod.dataareaid='ark'  AND moduletype=2  "+
			  "    AND len(inv.itemname)>0   AND del_blocked = 0 " +
              "  order by inv.itemid  "  ;

             String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,max(case when postedqty > 0 then postedvalue/postedqty else 0 end) as cost FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
                 "WHERE summ.DATAAREAID='ark'and dim.DATAAREAID='ark' and inv.DATAAREAID='ark' AND CLOSED = 0 AND  moduletype=0 " +
                 "AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid ) GROUP BY summ.itemid  ORDER BY summ.itemid ";


    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap stocks = new TreeMap();
    TreeMap costs = new TreeMap();
    TreeMap stocks2 = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
    	
    	   ResultSet rs1 = stmt.executeQuery(cmd2);
           while(rs1.next()){
             String id = rs1.getString("toode").trim();
             stocks.put(id,rs1.getDouble("stock")+ "");
             costs.put(id, rs1.getDouble("cost")+ "");
           }
    	



           String cmd3 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,inventlocationid as ladu ,summ.DATAAREAID as area FROM INVENTSUM summ  " +
                       "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.DATAAREAID = dim.DATAAREAID " +
                       "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.DATAAREAID = inv.DATAAREAID " +
                       "  WHERE summ.DATAAREAID in ('ark','lv') and dim.DATAAREAID in ('ark','lv') and inv.DATAAREAID in ('ark','lv')AND CLOSED = 0 AND  moduletype=0 " +
                       "  AND  rtrim(dim.inventlocationid)  IN ('20','02','01' ) " +
        			   " GROUP BY summ.DATAAREAID,inventlocationid,summ.itemid   HAVING SUM(AVAILPHYSICAL) > 0 ORDER BY summ.itemid ";

           
    	    rs1 = stmt.executeQuery(cmd3);
           while(rs1.next()){
             String id = rs1.getString("toode").trim();
             String kog = rs1.getInt("stock") + "";
             String asi = rs1.getString("ladu").trim();
            String asi2 = rs1.getString("area").trim().substring(0,2);
            
            if(stocks2.get(id) != null){
            	stocks2.put(id,(String)stocks2.get(id) + " " + asi2 + asi + ":" +kog);
            }else{
            	stocks2.put(id,asi2 + asi + ":" +kog);
            }        
           }
    	

           System.out.println("stocks2 size " + stocks2.size());
    	
    	
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim();

        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));
        if(costs.get(kood) != null){
        	double cost = Double.parseDouble((String) costs.get(kood));
        	int asi = (int)((1-cost/prod.getPrice())*100);
        	//round((1-tp_ostohinta/tp_myyntihinta)*100,2)
        	if(asi > 0){
        	 	 prod.setMargin( asi + "");
            	// System.out.println("cost " + kood + " " + prod.getMargin());	
        	}
       
        }
       
        String ean = rs.getString("ean");
        String eanx = "";
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean.trim());
        if(ean.trim().length() > 4){
            eanx = " " +  ean.trim().substring(ean.trim().length()-4);                 	
        }
      }
        
  boolean nostock = true;
        
        if(stocks2.get(kood) != null){
        	nimetus = nimetus + " <" +stocks2.get(kood) +">";
        	nostock = false;
        }
        
        if(stocks.get(kood) != null){
        	prod.setStock(Double.parseDouble((String) stocks.get(kood)));
        }
        
        
      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
      
     }
       nimetus = kood.trim().substring(0,4) + " " + nimetus + " " + kood + eanx;
      prod.setName(Converter.enCodeEst(nimetus));


     String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>0){
            // prod.setGroupSecond(grupp2);
          }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                prod.setUnit(unit);
             }

if(custProds2.contains(prod.getID()) || prod.getStock()>0 || !nostock){
    products.put(nimetus, prod);
    prodHash.put(prod.getID(), prod);	
}

      }
      rs.close();
   

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }





  public Vector getCustomerProductsList(String agentCode){
   

    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
      
      String cmd ="SELECT  cus.arksalesadministrator as agent,round(SUM(lineamount),0) AS summa " +
        " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid and inv.dataareaid=sal.dataareaid INNER JOIN custinvoicejour jou ON " +
        " sal.invoiceid=jou.invoiceid  and jou.dataareaid=sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount " +
        " and jou.dataareaid=cus.dataareaid  WHERE  " +
        " inv.dataareaid='ark' AND sal.dataareaid='ark' AND cus.dataareaid='ark'  AND jou.dataareaid='ark' AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
        " AND MONTH(sal.invoicedate) = MONTH(getdate()) " +
        " GROUP BY cus.arksalesadministrator";

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
    return "Tallegg";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){

  	    String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa " +
       " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid and inv.dataareaid=sal.dataareaid " +
       "INNER JOIN custinvoicejour jou ON " +
       " sal.invoiceid=jou.invoiceid and jou.dataareaid=sal.dataareaid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount" +
       " and jou.dataareaid=cus.dataareaid WHERE len(talinventstatistics1num) >0 AND " +
       " inv.dataareaid='tal' AND sal.dataareaid='tal' AND cus.dataareaid='tal' AND cus.talsalesman='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())" +
       " GROUP BY talinventstatistics1num  ORDER BY talinventstatistics1num ";
  	  StringBuffer sb = new StringBuffer();

  	  System.out.println(sql);
  	 try{

    	Statement stmt =  this.getNewConnection().createStatement();
  	    ResultSet	      rs = stmt.executeQuery(sql);

  	          sb.append("GrupiNimi     kilodes ja EUR\n");
  	          sb.append("---------------------------------------------------\n");

  	          double totalKokku = 0d;
                    double totalSumma = 0d;

  	          while (rs.next()) {
  	            sb.append(rs.getString("grupp").trim() + "\n");
  	          	int kogus = rs.getInt("kogus");
                            int summa = rs.getInt("summa");
  	            totalSumma = round(totalSumma + summa, 2);
  	            totalKokku = round(totalKokku + kogus, 2);
  	            sb.append("\t" + kogus + "\t" + summa + " EUR\n\n");
  	           }

  	          sb.append("---------------------------------------------------\n");
  	          sb.append("\tKuu m��k kokku: " + totalKokku + " Kg." + totalSumma + " EUR");


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
  private Vector custProds = new Vector();
  private HashSet custProds2 = new HashSet();
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
