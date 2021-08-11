package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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

public class S1DBDriver extends AbstractDBDriver {
  public S1DBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
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

  public TreeMap getProdGroups() throws SQLException {
    TreeMap   tm = new TreeMap();
    return tm;
  }
  public String getSaveDebtInfo() {
   	return null;
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
    String cmd = "SELECT id,descr FROM sc2240";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("id").trim());
          String name = rs.getString("descr").trim();
          agent.setName(name);
          agents.put(name, agent);
        }
        rs.close();
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getCustomers(Agent agent){
    String cmd = "SELECT MAX(aja.date) AS maxdate, kl.id AS kood,kl.descr AS nimi,kl.sp4301 AS address," +
                 "kl.SP4691 AS visit " +
                 "FROM sc4056 kl INNER JOIN 1sconst aja ON aja.objid=kl.id WHERE " +
              "ltrim(rtrim(aja.value)) = ? ORDER BY  kl.descr,aja.date ASC GROUP BY descr";

  if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    try {
      pstmt = con.prepareStatement(cmd);
      pstmt.setString(1, agent.getID());
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()){
        String name = rs.getString("nimi").trim();
        String accountnum = rs.getString("kood").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(name);

        cust.setAddress(rs.getString("address").trim());
        cust.setVisit(rs.getString("visit").trim());

        customers.put(name, cust);
        set.put(cust.getID(), cust);
      }

      rs.close();
      pstmt.close();

      String sql = "SELECT aja.value,MAX(aja.date) FROM 1sconst aja WHERE ltrim(rtrim(aja.id))='34K' AND ltrim(rtrim(aja.objid))=?";
      pstmt = con.prepareStatement(sql);

      Iterator keys = customers.values().iterator();
      while(keys.hasNext()){
        Customer cust = (Customer)keys.next();
        pstmt.setString(1, cust.getID());
        rs = pstmt.executeQuery();
        while(rs.next()){
          cust.setDiscount(rs.getDouble("value"));
        }
      }
      rs.close();
      pstmt.close();


 sql = "SELECT sp3938 AS klid,d.docno AS doknr,sp3762 AS maksepaev,-sp3941 AS debt FROM RG3942 alg " +
    "left JOIN 1sjourn d ON rtrim(RIGHT(alg.sp3940,7))= rtrim(ltrim(d.iddoc)) " +
    "left JOIN dh3773 info ON info.iddoc=d.iddoc " +
    "left JOIN  dt3773 abi ON abi.iddoc=info.iddoc left JOIN sc4056 k ON k.id = sp3755 " +
    "WHERE (ltrim(rtrim(sp4263)) = ? AND MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) AND k.descr!='') AND sp3941<>0 " +
    "ORDER BY k.descr,sp3762 GROUP BY d.docno ";
      pstmt = con.prepareStatement(sql);
      pstmt.setString(1, agent.getID());
      rs = pstmt.executeQuery();

      this.debts = new Vector();
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klid").trim());
        debt.setDoknr(rs.getString("doknr").trim());
        debt.setDuedate(rs.getDate("maksepaev"));
        debt.setSum(rs.getDouble("debt"));

        Customer cust = (Customer)set.get(debt.getCustID());
        if (cust != null){
          java.util.Date maksep = debt.getDuedate();

          if (maksep.getTime() < today.getTimeInMillis()){
            cust.setDebt(cust.getDebt() + debt.getSum());
          }
        }

        debts.addElement(debt);
      }
      rs.close();
      pstmt.close();

    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }


  public Vector getDebt(String agentCode) throws SQLException{
    return this.debts;
  }

  public TreeMap getProducts(Agent agent){

    TreeMap products = new TreeMap();


      String sql = "SELECT sp3920 AS seis,sp3918 AS toode FROM rg3927  WHERE   MONTH(period)=MONTH(date()) " +
                  " AND YEAR(period)=YEAR(date()) ORDER BY sp3918";
          try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Hashtable tooted = new Hashtable();
            while(rs.next()){
              String id = rs.getString("toode").trim();
              Toode toode = (Toode)tooted.get(id);
              if (toode == null){
                toode = new Toode(id);
                tooted.put(id, toode);
              }
              toode.setKogus(toode.getKogus() + rs.getDouble("seis"));
              //System.out.println("1. " + toode);
            }
            rs.close();
            stmt.close();



          String cmd = " SELECT pr.id AS kood,pr.descr AS nimi,pr.sp3602 AS hind,pr.sp4250 AS kastis, " +
                "0 AS stock  FROM sc3613 pr WHERE pr.sp3602 <> 0 ORDER BY pr.descr ";






    if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      rs = stmt.executeQuery(cmd);
      while(rs.next()){
        // if ( kood.length() > 2 ){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi").trim();
        prod.setName(nimetus);
        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("hind"));

        Toode toode = (Toode)tooted.get(kood);
               double kogus;
               if (toode == null){
                 //System.out.println(kood);
                 kogus = 0;
               } else {
                  kogus = round(toode.getKogus(), 2);
               }

          prod.setStock(kogus);

        products.put(nimetus, prod);
        //}
      }
      rs.close();
      stmt.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    return products;
  }

  public Vector getCustomerProductsList(String agentCode){

    String cmd = " SELECT  sp3765 AS toode,sp3755 AS klient FROM dh3773 info INNER JOIN 1sjourn pais ON info.iddoc=pais.iddoc " +
                 "INNER JOIN  dt3773 abi ON abi.iddoc=info.iddoc INNER JOIN sc3613 tt ON tt.id=sp3765  INNER JOIN sc3613 pr ON pr.id=sp3765 " +
                 "WHERE ltrim(rtrim(sp4263)) = ? AND pr.sp3602 <> 0 AND sp3758 > ? GROUP BY sp3755, tt.descr ORDER BY sp3755, tt.descr";

    if (isDebug()) System.out.println(cmd);
    Vector custProds = new Vector();
    try {
      pstmt = con.prepareStatement(cmd);
      pstmt.setString(1, agentCode);

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -Integer.parseInt(getParent().getProps().getProperty("months")));
      pstmt.setDate(2, new java.sql.Date(cal.getTime().getTime()));

      ResultSet rs = pstmt.executeQuery();
      while(rs.next()){
        String kliendikood = rs.getString("klient").trim();
        String tootekood = rs.getString("toode").trim();
        CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0," ",0,0,0," ");
        custProds.add(cp);
      }
      rs.close();
      pstmt.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    return custProds;
  }


  public double getSalary(String agentCode) {
    /*connectionUp();*/
      double sales = 0;
      String cmd = "SELECT SUM(abi.sp3770) AS sales FROM dh3773 info INNER JOIN 1sjourn pais ON info.iddoc=pais.iddoc " +
                   "INNER JOIN  dt3773 abi ON abi.iddoc=info.iddoc INNER JOIN sc3613 tt ON tt.id=sp3765  INNER JOIN sc3613 pr ON pr.id=sp3765 " +
                  "WHERE ltrim(rtrim(sp4263)) = ? AND YEAR(sp3758)=YEAR(date()) AND MONTH(sp3758) = MONTH(DATE()) order BY sp3755";

      try {
          pstmt = con.prepareStatement(cmd);
          pstmt.setString(1, agentCode);
          ResultSet rs = pstmt.executeQuery();

          if(rs.next()) {
            sales = rs.getDouble("sales");
            setSales(sales);
          }
      } catch (Exception e) {
          getParent().showError(e);
      }

      return 0;
    }

  public String getStartingTime(){
    return null;
  }

  public String getDriverVersion(){
    return "1S";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){
    return null;
  }



  private String url = "jdbc:odbc:baas";
  private Connection con;
  private Statement stmt;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;
}
