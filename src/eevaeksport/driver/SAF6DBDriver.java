package eevaeksport.driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
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

public class SAF6DBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater{
  public SAF6DBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
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

          public String getDealerCustomerCSV(String agent){
           return null;
           }

  /**
   *
   */
  public void connectionUp(){
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



                                         public String  getCustProdDiscounts(Agent agent){
                                          return null;
                                          }


  /**
   *
   */
  public void connectionDown(){
  }

  public TreeMap getAgents(){
    String cmd = "SELECT id AS agent, name AS nimetus, code as abikood ,i_teostaja as Teostaja " +
       " FROM  grcompanycat WHERE Category='myygimees'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    HashMap aHash = new HashMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          agent.setName(name);
          agent.setObject("1");
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
     /* String  cmd2 = "SELECT MWWHLO AS yksus ,MWWHNM AS nimetus FROM mvxjdta.MITWHL WHERE " +
		  " MWCONO='200' AND (MWWHLO = '200' OR MWWHLO = '205' OR MWWHLO = '215')";

       try {
         Connection con = getNewConnection();
           Statement stmt = con.createStatement();
           ResultSet	      rs = stmt.executeQuery(cmd2);


           while (rs.next()) {
             Dealer dealer = new Dealer(rs.getString("yksus").trim());
              dealer.setName(rs.getString("nimetus").trim());
                    addDealer(dealer);
           }
           rs.close();
           stmt.close();
           con.close();

       } catch (Exception e) {
           getParent().showError(e);
        }*/

    return agents;
  }



      public String getDistribution() {
/*
        StringBuffer sb = new StringBuffer();

        try {
          Connection con = getNewConnection();
          Statement st = con.createStatement();
          String cmd = "select mmitno as kood ,mmitds AS Nimetus , mmdim1 from mvxjdta.mitmas where MMCFI5 = 'Y'";

          if (isDebug())
          System.out.println(cmd);
          ResultSet rs = st.executeQuery(cmd);
         while(rs.next()){
           sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
         }
         st.close();
         con.close();
       } catch (Exception e){}
         System.out.println(sb.toString());
        return sb.toString();*/
    return null;
      }




  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = " SELECT kl.id AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address,' ' AS visit ,cat.id AS agent, " +
          " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale"+
          " FROM grcompany kl INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id INNER JOIN grcompanycat cat " +
          " ON cat.id=rel.idgrcompanycat WHERE cat.category='myygimees' AND ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          cust.setName(name);
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(aadress1);
          String visit = rs.getString("visit");
          if(visit != null){
           // cust.setVisit(visit.trim());
          }
          //String info = rs.getString("info");
          //if (info != null){cust.setInfo(info.trim());}
         // String limiit = rs.getString("limiit");
         // System.out.println(limiit);
          //if (limiit != null){cust.setCredit(info.trim());}
          String agent1 = rs.getString("agent").trim();
          cust.addAgent(new Agent(agent1));
          cust.setPayer(rs.getString("payer").trim());
          double ale  = rs.getDouble("ale");
          if (ale>0){cust.setDiscount(ale);}
         // cust.setDebt(rs.getDouble("debt"));
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


    String cmd3 = "SELECT d.idpartner AS maksja,d.idgrcompany AS saaja, sum(d.totalamount) +sum(-m.amount) AS SUMMA  FROM sdinvoice d " +
          " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id WHERE docstate !='F' AND exchangerate=1  GROUP BY  d.idpartner,d.idgrcompany ";

     if (isDebug()) System.out.println(cmd3);
        ResultSet rs1 = st.executeQuery(cmd3);
         while (rs1.next()) {
           String id = rs1.getString("saaja").trim();
           double debt =rs1.getDouble("SUMMA");
          // System.out.println(id + "  " + debt);
           Customer cust = (Customer) custMap.get(id);
           if (cust != null ) {
             cust.setDebt(debt);
            //  System.out.println(id + "  " + debt);
           }
         }

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

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='2tase' ORDER BY name";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
  }




  public TreeMap getProducts(Agent agent){

    String cmd = "SELECT pr.id AS kood,pr.name AS nimi,hi.price AS hind,pr.u_ean  AS EAN ,cat.idgrarticlecat AS grupp, " +
        " pr.contentquantity AS tykke ,y.name AS yhik " +
        " FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " +
        " hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
        " RIGHT JOIN grunit y ON y.id=pr.idgrunit  " +
        " WHERE hi.idgrpricelist=4 AND arca.category='2tase' AND articletype='G' ";
    String cmd2 = "SELECT idgrarticle AS toode,SUM(quantity) AS seis,round(SUM(amountbase)/SUM(quantity),3) AS ost FROM dcurbalancestore WHERE iddrstore!=6 AND quantity!=0 " +
	 " GROUP BY idgrarticle" ;

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode");
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          double ost = rs2.getDouble("ost");
          toode.setStock(sto);
          toode.setMeasure(ost);
          tooted.put(id,toode);
        }

      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product toode = (Product)tooted.get(kood);
        double kogus;
        double ost;
        if (toode == null) {
          kogus = 0;
          ost = 0;
        }
        else {
          kogus = round(toode.getStock(), 2);
           ost = round(toode.getMeasure(), 2);
         }

         Product prod = new Product(kood);
         String nimetus = rs.getString("nimi").trim();
         prod.setStock(kogus);
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         double margin = (1 - (ost / hind)) * 100;
         int margi = 0;
         try {
           margi = (int)margin;
         }

         catch (Exception e) {}
          //System.out.println("margi"+margi+"   "+margin);
         prod.setName(nimetus);
         if (margi != 0 || margi != 100) { prod.setMargin(margi + "");
         }

         prod.setName(nimetus);
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 7) {
           prod.setEAN(ean.trim());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }

         prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");

        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
        }
        products.put(nimetus, prod);
        //}
      }
      rs.close();
      st.close();
      con.close();

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }



  public Vector getCustomerProductsList(String agentCode){

    String sql1 = "SELECT d.idgrcompany AS klient,r.idgrarticle AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice " +
        "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
        "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
        "WHERE r.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0 " +
        "AND r.articletype='G' GROUP BY d.idgrcompany,r.idgrarticle,r.quantity,r.docdate,r.unitprice ORDER BY d.idgrcompany,r.idgrarticle,r.docdate";

    String sql2 = " SELECT d.idgrcompany AS klient,idgrarticle AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
      "WHERE r.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0 " +
      "AND r.articletype='G' GROUP BY r.idgrarticle,d.idgrcompany ORDER BY d.idgrcompany,idgrarticle ";

           if (isDebug()) {
                System.out.println(sql1);
            }

            Vector custProds = new Vector();

            try {
              Connection con = getNewConnection();
              PreparedStatement stmt = con.prepareStatement(sql1);

              //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
              ResultSet	      rs = stmt.executeQuery();

              HashMap kliendid = new HashMap();
              while (rs.next()) {
                String kliendikood = rs.getString("klient").trim();
                String tootekood = rs.getString("toode").trim();
                double kogus = Double.parseDouble(rs.getString("kogus").trim());
                String lastdate = rs.getString("kuupaev").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," "),false);
                kliendid.put(kliendikood, cust);
                //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
                //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
                //CustProd cp = new CustProd(kliendikood, tootekood, kogus ,pluskogus ,minuskogus);
                //custProds.add(cp);
              }

              rs.close();
              stmt.close();

              stmt = con.prepareStatement(sql2);
              rs = stmt.executeQuery();
              while(rs.next()){
                String klient = rs.getString(1).trim();
                String toode = rs.getString(2).trim();
                double pkogus = rs.getDouble(3);
                double mkogus = rs.getDouble(4);
                Customer c = (Customer)kliendid.get(klient);
                if (c != null){
                  ArrayList al = (ArrayList)c.getProducts().get(toode);
                  if (al != null){
                    CustProd cp = (CustProd)al.get(1);
                    if (cp != null){
                      cp.setMovedStockPluss(pkogus);
                      cp.setMovedStockMinus(mkogus);
                      custProds.add(cp);
                      //System.out.println(cp.getProdID());
                    }
                  }
                }
              }

              rs.close();
              stmt.close();
              con.close();
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

  public void setBulkSales(Map agents){

    String cmd =
        "SELECT cat.id AS agent ,SUM(d.totalamount)  AS summa  FROM  sdinvoice d  " +
        " INNER JOIN grcompanycatrel rel ON rel.idgrcompany=d.idgrcompany INNER JOIN grcompanycat cat  " +
        " ON cat.id=rel.idgrcompanycat " +
        " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) " +
        " AND cat.category='myygimees' GROUP BY cat.id";

       if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
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
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
        }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {


       /*try {


          String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
		"from mvxjdta.MITBAL l WHERE MBWHLO='200' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                   dealerProds.add(ladu, toode,seis+"" );
                   //System.out.println(ladu+" "+toode+" "+seis+"");
               }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }*/

        //return dealerProds;
        return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = " SELECT d.idpartner AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
        " d.totalamount AS SUMMA ,sum(m.amount) AS minus ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
        " FROM sdinvoice d " +
        " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
        " WHERE docstate !='F' AND exchangerate=1  " +
        "  group by  d.sourcedocno,d.idpartner,d.promptdate,d.totalamount ORDER BY d.idpartner " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);

       while(rs.next()){
         Debt debt = new Debt();
         debt.setCustID(rs.getString("maksja").trim());
         debt.setDoknr(rs.getString("arve").trim());
          debt.setDuedate(rs.getDate("kuupaev"));

         double summa = rs.getDouble("summa");
         double minus = rs.getDouble("minus");
         debt.setDue(rs.getString("paevi"));
        // if(duedate.getTime()<now.getTime()){
           if( summa+minus > 1 || summa+minus < -1){
              debt.setSum(summa+minus);
           list.addElement(debt);
         }
       }
       rs.close();
       stmt.close();
       con.close();
       return list;
    }

    public ManagerData getManagerData() throws SQLException{
       ManagerData md = new ManagerData();
       return md;
     }


    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }
    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }

    public String getStartingTime() {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH,
              -Integer.parseInt(getParent().getProps().getProperty("months")));
      int month = cal.get(Calendar.MONTH) + 1;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      String sqlDate = cal.get(Calendar.YEAR) + month + day;
      return sqlDate;
    }

    public String getDriverVersion() {
      return "SAF6";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }



    public String getPilotDayData(Agent agent){
        return null;
    }

    public void insertDefective(DefectiveWrapper wr){

    }


    public void insertDistribution(DistEvalListWrapper wr){
    }

   


    public TreeMap getDealers() {
      return dealers;
    }

    public void setDealers(TreeMap aDealers) {
      dealers = aDealers;
    }

    public NumberFormat getNF() {
      return NF;
    }

    public void setNF(NumberFormat aNF) {
      NF = aNF;
    }


  private String url = "jdbc:odbc:SAF";
  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
