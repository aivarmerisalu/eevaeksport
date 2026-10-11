package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;


/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class IceAxaResDBDriver extends AbstractDBDriver {
  public IceAxaResDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
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
              public String getSaveDebtInfo() {
               	return null;
               }
               

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }

  /**
   *
   */
  public void connectionUp(){
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, "bmssa", "bmssa_pwd");
  }

  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();

    String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
        "co.transdate AS kuupaev FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid " +
        "WHERE  a.dimension2_ = '" + agentCode.trim() + "'  " +
        " AND co.dataareaid='bal' AND a.dataareaid='bal' ORDER BY a.accountnum,co.transdate ";

    
    Connection ccc = getNewConnection();
    Statement stm = ccc.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    while(rs.next()){
      Debt debt = new Debt();
      debt.setCustID(rs.getString("klient").trim());
      String number = rs.getString("number").trim();
      debt.setDoknr(number);
      debt.setDuedate(rs.getDate("maksepaev"));
      debt.setSum(rs.getDouble("tasumata"));
      debt.setDate(rs.getDate("kuupaev"));
    //  list.addElement(debt);
    }
    rs.close();
    stm.close();
    ccc.close();
    System.out.println("V�LAD");
    return list;
  }


  /**
   *
   */
  public void connectionDown(){
  }

  public TreeMap getAgents(){
    String cmd = "SELECT num, description,inventlocation,lendur FROM dimensions WHERE dataareaid='bal' " +
           " AND dimensioncode=1  ";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("num").trim());
          String name = rs.getString("description").trim();
          String objekt = rs.getString("inventlocation").trim();
          agent.setName(name);
          agent.setObject(objekt);
          if(rs.getBoolean("lendur") == true){
           // agent.setIsPilot(true);
          }
          agents.put(name, agent);
        }
 
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }




      public String getDistribution() {
        return null;
      }



  public TreeMap getProdGroups() throws SQLException {


     TreeMap   tm = new TreeMap();
     String    sql = "SELECT right(left(itemgroupid,4),2) AS liik ,right(left(itemgroupid,4),2)+ ' ' + name AS nimetus FROM  inventitemgroup " +
                " WHERE dataareaid='bal' AND right(itemgroupid,2) = '00'  " +
                "   AND right(left(itemgroupid,4),2)!='19' AND (left(itemgroupid,1))>0 AND (left(itemgroupid,1))<8 AND right(left(itemgroupid,4),2)!='00'  " +
                "  ORDER BY right(left(itemgroupid,4),2)";

     
     Connection ccc = getNewConnection();
     Statement stm = ccc.createStatement();
     ResultSet rs = stm.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimetus").trim();
       if (liik != ""){
       tm.put(liik,nimetus);
       }
     }
     rs.close();
     stm.close();
     ccc.close();
     return tm;
 }

  public TreeMap getCustomers(Agent agent){
    String cmd = "SELECT cus.name, cus.accountnum," +
                 "(SELECT SUM(amountmst) FROM custtransopen co WHERE co.duedate<getdate()" +
                 " AND co.accountnum = cus.accountnum) AS debt, cus.linedisc , " +
                 "cus.address, cus.DayOfVisit AS visit ,cus.PilotEAN AS useean ,cus.Pilotdepot AS mituladu " +
            " FROM custtable cus  WHERE cus.dimension2_ ='" + agent.getID() + "'  AND cus.dataareaid='bal' AND blocked=0 ";



  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
           //HashMap custHash = new HashMap();
    try {
    	
    	 Connection ccc1 = getNewConnection();
 	    Statement stm1 = ccc1.createStatement();
      ResultSet rs = stm1.executeQuery(cmd);
      Connection ccc = getNewConnection();
      Statement stm = ccc.createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(name);
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }

        String aleke = rs.getString("linedisc");
        if(aleke == null){
        aleke = "000" ;
        } else {
        aleke = aleke.trim();
        }

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(aadr.trim());
        }
        String visit = rs.getString("visit");
        if (visit != null) {
          cust.setVisit(visit.trim());
        }

        String printean = rs.getString("useean");
        if (printean.equalsIgnoreCase("T")) {
          cust.setPrintean("T");
        }
        String mituladu = rs.getString("mituladu");
               if (mituladu.equalsIgnoreCase("T")) {
                 cust.setBlockingDealer("T");
               }

        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='bal' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
        ResultSet rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
        customers.put(name, cust);
      }
      stm1.close();
      ccc1.close();
      
      stm.close();
      ccc.close();

    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }

  public TreeMap getProducts(Agent agent){

    String obj =  "'KM','KS'";
    

    String cmd = "SELECT inv.itemid,(right(left(inv.itemgroupid,4),2) + ' ' + left(gr.name,4)+ ' ' + inv.itemname) AS nimetus " +
    		" ,'' as lisa , '' as nimi2, inv.itemqtyinabox AS kastis,  disc.amount AS price " +
               ", NAMEALIAS AS ean ,right(left(inv.itemgroupid,4),2) AS grupp,itemqtyonpallet AS pallet FROM inventtable inv  " +
                 "INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid " +
                 " INNER JOIN inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid  " +
                 " INNER JOIN pricedisctable disc ON inv.itemid=disc.itemrelation   and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') ) " +
                 " WHERE  disc.dataareaid='bal' AND disc.accountrelation='Pood' AND inv.dataareaid='bal'AND gr.dataareaid='bal' AND mod.dataareaid='bal' AND moduletype=2  " +
                 " AND balsenditem=1 ORDER BY right(left(inv.itemgroupid,4),2) ";
if(agent.getID().equals("111")){
	cmd = "SELECT  inv.itemid,right(left(inv.itemgroupid,4),2) + ' ' + inv.itemid  AS nimetus, " +
			 "  txt.txt AS lisa,inv.itemname AS nimi2, inv.itemqtyinabox AS kastis,  disc.amount AS price  " +
              " , NAMEALIAS AS ean ,right(left(inv.itemgroupid,4),2) AS grupp,itemqtyonpallet AS pallet FROM inventtable inv " + 
                "  INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid " +
                 "  INNER JOIN inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid " +  
                 " INNER JOIN pricedisctable disc ON inv.itemid=disc.itemrelation and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') )" +
				 " LEFT JOIN InventTxt txt ON txt.itemid=inv.itemid  And txt.languageid = 'en-us' " +
                 " WHERE  disc.dataareaid='bal' AND disc.accountrelation='Margota' AND inv.dataareaid='bal'" +
                 " AND gr.dataareaid='bal' AND mod.dataareaid='bal' AND moduletype=2 " + 
                 " AND balsenditem=1 ORDER BY right(left(inv.itemgroupid,4),2)  ";
    
}
    
             String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid " +
                 "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=0 " +
                 "AND dim.inventlocationid IN (" + obj + ") GROUP BY summ.itemid  ORDER BY summ.itemid ";


    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    try {
    	Connection ccc = this.getNewConnection();
    	 Statement stm = ccc.createStatement();
      ResultSet rs = stm.executeQuery(cmd);
       HashMap prodHash = new HashMap();
      while(rs.next()){
        // if ( kood.length() > 2 ){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim();
        if(agent.getID().equals("113")){
        
        String lisa = rs.getString("lisa");
        
        if(lisa == null){
        	lisa = "";
        }else{
        	lisa = lisa.trim();
        }
        
        String nimi2 = rs.getString("nimi2").trim();
        prod.setName((nimetus + " " + lisa).trim() );
        if(lisa.length()== 0){
        	prod.setName((prod.getName()+ " " + nimi2).trim());
        }	
        
        System.out.println("nimi " + prod.getName());
        }else{
        	prod.setName(nimetus);
        }
        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));
        prod.setEAN(rs.getString("ean"));
        prod.setGroup(rs.getString("grupp"));
        double pallet = rs.getDouble("pallet");
        if (pallet >0 && prod.getInCases()>0){
          prod.setOnPallet(pallet/prod.getInCases());
        }
        prod.setStock(0);
        products.put(nimetus, prod);
        prodHash.put(prod.getID(), prod);
        //}
      }
      rs.close();
      stm.close();
      ccc.close();
      
      ccc = this.getNewConnection();
      stm = ccc.createStatement();
  
      ResultSet rs1 = stm.executeQuery(cmd2);
                            while(rs1.next()){
                              String id = rs1.getString("toode").trim();
                              Product prod1 = (Product)prodHash.get(id);
                              if (prod1 != null){
                                prod1.setStock(rs1.getDouble("stock"));
                              }
      
                            }
                            stm.close();
                            ccc.close();
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }


  public String getPilotDayData(Agent agent){


    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
"inv.dataareaid='bal' AND sal.dimension2_='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";

       String sql2 =  "SELECT sal.salesid AS arve, sal.invoicedate AS kuupaev,cus.salesname AS klient, SUM(sal.lineamountmst) AS summa " +
    " FROM custinvoicetrans sal INNER JOIN salestable cus ON sal.salesid=cus.salesid " +
     "WHERE sal.dataareaid='bal' AND cus.dataareaid='bal' AND cus.dimension2_ ='" + agent.getID() + "' AND " +
    " sal.invoicedate > getdate()-4 GROUP BY sal.salesid,cus.salesname ,sal.invoicedate ORDER BY sal.invoicedate";

    StringBuffer sb = new StringBuffer();
    sb.append("\n\n\n Vormistatud tellimused:\n\n");

      try {
                  ResultSet	      rs2 = stmt.executeQuery(sql2);
                  while (rs2.next()) {
                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Summa: " + rs2.getDouble("summa") + "\n");
                  }

        //stmt = con.prepareStatement(sql);
        //stmt.sets(1, agent.getObject());
          //stmt.setString(2, agent.getObject());
          ResultSet	      rs = stmt.executeQuery(sql);
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


                  } catch (Exception e) {
                      getParent().showError(e);
                  }


      return sb.toString();
  }



  public Vector getCustomerProductsList(String agentCode){


    String cmd = " SELECT jou.orderaccount,tra.itemid,tra.qty AS kogus ,jou.invoicedate AS lastdate " +
    "   FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
    "  tra.invoiceid=jou.invoiceid INNER JOIN custtable cust ON jou.orderaccount = cust.accountnum " +
    "  INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid " +
    "  WHERE  jou.invoicedate > getdate()-30 AND cus.dataareaid= 'BAL' and tra.dataareaid= 'BAL' and cus.dataareaid= 'BAL' and jou.dataareaid= 'BAL'" +
    " and cust.dataareaid= 'BAL' and " +
        "cust.dimension2_ = '" + agentCode + "'   GROUP BY jou.orderaccount,tra.itemid,tra.qty,jou.invoicedate " +
    "  ORDER BY jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate  " ;


    if (isDebug()) System.out.println(cmd);
    Vector custProds = new Vector();
    try {
    	   Connection ccc = getNewConnection();
    	    Statement stm = ccc.createStatement();
      ResultSet rs = stm.executeQuery(cmd);
      while(rs.next()){
      	 String kliendikood = rs.getString("orderaccount").trim();
         String tootekood = rs.getString("itemid").trim();
         double kogus = rs.getDouble("kogus");
         String lastdate = rs.getString("lastdate").substring(0,10);
                  lastdate = lastdate.substring(5,10);
             
         CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");

         custProds.add(cp);
      }
      rs.close();
      stm.close();
      ccc.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    return custProds;
  }


  public double getSalary(String agentCode) {

      String cmd = "SELECT sum(lineamountmst) AS salary from  custinvoicetrans sal WHERE " +
	" MONTH(sal.invoicedate) = MONTH(getdate()) AND YEAR(sal.invoicedate) = YEAR(getdate()) "
        + "AND sal.dataareaid='bal' AND sal.dimension2_= ? " ;

      try {
    	  Connection ccc = this.getNewConnection();
        PreparedStatement pst = ccc.prepareStatement(cmd);
          pst.setString(1, agentCode);
          ResultSet rs = pst.executeQuery();

          if(rs.next()) {
          }
          
          rs.close();
          pst.close();
          ccc.close();
      } catch (Exception e) {
             getParent().showError(e);
      }

     // setSales(sales);
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
    return "IceAxaRes";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }


  private String url = "jdbc:odbc:AXA";
  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
}
