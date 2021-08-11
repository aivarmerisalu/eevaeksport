package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class JungentDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater {
  public JungentDBDriver(EksportPanel parent){
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

  /**
   *
   */
  public void connectionUp(){
  }

  public Vector getDebt(String agentCode){
  Vector vec = new Vector() ;
  return vec;
  }

  public String getAssortmentStr(Agent agent){
  	return null;
  }
  public String getSaveDebtInfo() {
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
    String cmd = "SELECT groupid, name FROM commissionsalesgroup WHERE dataareaid='dat' AND dimension = '99'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("groupid").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agents.put(name, agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }

  public TreeMap getCustomers(Agent agent){
    String cmd = "SELECT  saaja , agent FROM JungentiAgendiKlient" ;

    if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();

    try {
      Map tempCusts = new HashMap();
      //PreparedStatement pst = con.prepareStatement(cmd);
     //pst.setString(1, agent.getID());
      ////pst.setTimestamp(2, new Timestamp(getStartingDate().getTime()));
      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs1 = st.executeQuery(cmd);

      while(rs1.next()){
        String custID = rs1.getString("saaja").trim();
        String agId = rs1.getString("agent").trim();
        Customer c = (Customer)tempCusts.get(custID);
        if (c == null){
          c = new Customer(custID);
          tempCusts.put(custID, c);
        }
        c.addAgent(new Agent(agId));
      }
      rs1.close();

      String cmd2 = "SELECT cus.accountnum ,cus.name, " +
                    "(SELECT SUM(amountmst) FROM custtransopen co WHERE co.duedate<getdate()" +
                    "AND co.accountnum = cus.accountnum) AS debt," +
                    "cus.address,cus.telex FROM custtable cus  WHERE " +
                    "cus.dataareaid='dat' ";

      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = stmt.executeQuery(cmd2);
      //int i = 0 ;
      while(rs.next()){
        String accountnum = rs.getString("accountnum").trim();
        if (tempCusts.containsKey(accountnum)){
          String name = rs.getString("name").trim();
          Customer cust = (Customer)tempCusts.get(accountnum);
          cust.setName(name);
          //String debtStr = rs.getString("debt");
          //if (debtStr != null){
          //  cust.setDebt(Double.parseDouble(debtStr.trim()));
          //}
          cust.setDebt(rs.getDouble("debt"));
          cust.setAddress(rs.getString("address"));
          cust.setVisit(rs.getString("telex"));
          customers.put(cust.getName(), cust);
          //i = i + 1;
          //System.out.println(" Nii mitu klienti :" + i  + "Agent" + cust.getAgents().toString());
        }

      }

      rs.close();
      st.close();
      con.close();
    } catch (SQLException e){
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
    String    sql = "SELECT IIPG.jungentipihukakood AS liik,IIPG.jungentiselgitus AS nimetus, " +
         " salesgroup AS agent FROM inventitemproductgroup IIPG, AgendiTooteGrupp ATG " +
        "WHERE IIPG.dimension='99' AND IIPG.JungentiPihukaKood = ATG.JungentiPihukaKood " +
         " GROUP BY IIPG.jungentipihukakood,IIPG.jungentiselgitus,salesgroup ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());

        String ag = rs.getString("agent").trim();
        Agent agent = (Agent)getParent().getAgentsHash().get(ag);
        if (agent != null){
          agent.addProdGroupList(liik);
        }

    }
    rs.close();
    st.close();
    con.close();

    return tm;
}




  public TreeMap getProducts(Agent agent){

    String cmd =  "SELECT inv.itemid, inv.itemname, inv.caseqty, mod.price,(SELECT SUM(PHYSICALINVENT - RESERVPHYSICAL) " +
                  "FROM INVENTSUM WHERE DATAAREAID='dat' AND itemid=inv.itemid AND " +
                  "CLOSED = 0 AND dimension ='99' GROUP BY itemid)AS Stock ,i.jungentipihukakood AS prodgroup FROM inventtable inv " +
                  " INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid INNER JOIN inventitemproductgroup i " +
                  " ON inv.productgroup=i.productgroup INNER JOIN Agenditootegrupp a ON a.jungentipihukakood=i.jungentipihukakood WHERE inv.dataareaid='dat' AND inv.dimension='99' " +
                  " AND moduletype=1 AND i.dimension='99' AND LEN(i.jungentipihukakood)>0 AND mod.blocked=0  " +
                  "    GROUP BY inv.itemid, inv.itemname, mod.price, inv.caseqty, i.jungentipihukakood " ;



    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        // if ( kood.length() > 2 ){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("itemname").trim();
        prod.setName(nimetus);
        prod.setInCases((rs.getDouble("caseqty")));
        prod.setPrice((rs.getDouble("price")));
        prod.setStock((rs.getDouble("stock"))) ;
        prod.setGroup(rs.getString("prodgroup"));
        products.put(nimetus, prod);
        //}
      }
    } catch (Exception e){
      getParent().showError(e);
    }
    return products;
  }

  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
   // if (true) return custProds ;

    String cmd = "SELECT jou.orderaccount,tra.itemid  FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
    "   tra.invoiceid=jou.invoiceid  " +
    " WHERE  jou.invoicedate >'" + getStartingTime() + "'  GROUP BY jou.orderaccount,tra.itemid" +
    "  ORDER BY jou.orderaccount,tra.itemid " ;


    if (isDebug()) System.out.println(cmd);

    try {
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kliendikood = rs.getString("orderaccount").trim();
        String tootekood = rs.getString("itemid").trim();
        CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0," ",0,0,0," ");
        custProds.add(cp);
      }
    } catch (Exception e){
      getParent().showError(e);
    }
    return custProds;
  }


  public double getSalary(String agentCode) {
    connectionUp();
      double sales = 0;
      String cmd = "SELECT SUM(tra.lineamount) AS salary  FROM custinvoicetrans  tra " +
                   "WHERE tra.salesgroup = ?  AND MONTH(invoicedate) >= MONTH(getdate()) AND " +
                   "YEAR(invoicedate) >= YEAR(getdate())" ;

      try {
        PreparedStatement pst = con.prepareStatement(cmd);
          pst.setString(1, agentCode);
          ResultSet rs = pst.executeQuery();

          if(rs.next()) {
            sales = rs.getDouble("salary");
          }
      } catch (Exception e) {
          getParent().showError(e);
      }

      setSales(sales);
      return 0;
    }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Connection con = getNewConnection();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        if (tekst == null) tekst = " ";
        else tekst = tekst.trim();
        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = "";
        bf2.append(agentName + "\n");
      }

      md.setClientAgentInfo(bf2.toString());

      return md;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
      /*String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
      System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }


    public void setBulkSales(Map agents){
      if (true)return;
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
    return "Jungent";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){
    return null;
  }



       public NumberFormat getNF(){
         return NF;
       }

       private boolean scrollTo(String id, ResultSet rs) throws SQLException{
       rs.beforeFirst();
       while(rs.next()){
         if (id.equals(rs.getString("kood").trim())){
           return true;
         }
       }
       return false;
     }

     private String ensureLen(String str, String replace){
       if (str == null) str = "";
       else str = str.trim();
       if (str.length() == 0){
         return replace;
       } else {
         return str;
       }
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
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}