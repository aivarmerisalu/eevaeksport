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
import eevaeksport.DealerProduct;
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

public class EstoDBDriver extends AbstractDBDriver {
  public EstoDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
      con2 = DriverManager.getConnection(url2);
      stmt = con.createStatement();
      stmt2 = con2.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

                                     public String  getCustProdDiscounts(Agent agent){
                                      return null;
                                      }

          public String getDealerCustomerCSV(String agent){
           return null;
           }

          public String getAssortmentStr(Agent agent){
          	return null;
          }
          public String getSaveDebtInfo() {
           	return null;
           }
            /**
   *
   */
  public void connectionUp(){
  }

  public TreeMap getProdGroups() throws SQLException {
    TreeMap   tm = new TreeMap();
     stmt = con.createStatement();
  String  sql = " SELECT prodgrid,prodgr FROM prodgroups GROUP BY prodgrid,prodgr " ;
   try {
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
         tm.put(rs.getString("prodgrid").trim(),
           rs.getString("prodgr").trim());
         }
          rs.close();
          } catch (Exception e){
          getParent().showError(e);
        }


  return tm;
  }


      public String getSaveStockInfo(){
        String cmd = "   SELECT kaup,nimi,jaak AS stock,100 AS stockcode " +
           "  from KAUBAD k, kaup_m90 a   WHERE kaup_id=k.kaup AND jaak>0  ";

          if (isDebug()) {
              System.out.println(cmd);
          }

          StringBuffer products = new StringBuffer();
          try {
            Statement st = con.createStatement();
              ResultSet	      rs = st.executeQuery(cmd);
              while (rs.next()) {
                  String kood = rs.getString("kaup").trim();
                  String nimetus = rs.getString("nimi").trim();
                  nimetus = nimetus.replace('.',' ');
                  products.append(kood +  ";");
                  products.append(" ;");
                  products.append(getParent().getNF().format(rs.getDouble("stock")) +  "\n");
              }
              rs.close();
          } catch (Exception e) {
              getParent().showError(e);
          }
          return products.toString();
      }


 

  /**
   *
   */
  public void connectionDown(){
  }

  public TreeMap getAgents(){
    String cmd = "select kood,tootajanimi from tootajad where len(ltrim(kood))>0";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("tootajanimi").trim();
          agent.setName(name);
          agents.put(name, agent);
        }
        rs.close();
    } catch (Exception e){
        getParent().showError(e);
      }

    return agents;
  }


  public void setDealers(TreeMap aDealers) {
    dealers = aDealers;
  }



  public TreeMap getCustomers(Agent agent) {
    String cmd = "SELECT klient AS kood, nimi AS nimetus, firma AS payer,aadress, klallahind*100 AS ale,grupp2 AS agent,visit " +
		  " FROM kliendid k WHERE aktiivne=1 AND grupp2 = '" + agent.getID()  +  "' AND  len(ltrim(grupp2))>0";

  if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    try {
      pstmt = con.prepareStatement(cmd);
      //pstmt.setString(1, agent.getID());
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()){
        String accountnum = rs.getString("kood").trim();
        String name = rs.getString("nimetus").trim().substring(5);
        Customer cust = new Customer(accountnum);
        String payer = rs.getString("payer");
        if (payer != null) {
          cust.setPayer(payer.trim());
        }
        String aadress = rs.getString("aadress");
        if(aadress != null){
          cust.setAddress(aadress.trim());
        }
        cust.setDiscount(rs.getDouble("ale"));
        cust.addAgent(new Agent(rs.getString("agent").trim()));
        String visit =rs.getString("visit");
        if(visit != null){
        cust.setVisit(visit);}
        cust.setName(name);


        customers.put(name, cust);
        set.put(cust.getID(), cust);
      }

      rs.close();
      pstmt.close();



      String sql = "SELECT d.doknr AS dok,k.firma AS klient,d.klient AS saaja,d.tasutud AS makse, SUM(round((kogus*int(((((hind*(1-allahind)) * 20 + 0.99) / 20 * 100)/5))*5/100)*1.20,2)) AS tasumata ,mkuup AS maksepaev,date()-mkuup AS yle " +
          " FROM slehed d,slread r,kliendid k " +
          " WHERE mkuup<date()+5 and d.slehenr=r.slehenr and k.klient=d.klient AND k.grupp2='" +
          agent.getID() + "' " +
          " GROUP BY d.doknr,k.firma,d.klient,mkuup,mkuup-date(),d.tasutud ORDER BY d.klient ";
      pstmt = con.prepareStatement(sql);
      if (isDebug()) System.out.println(sql);
      //pstmt.setString(1, agent.getID());
      rs = pstmt.executeQuery();

      this.debts = new Vector();
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while(rs.next()){
        Debt debt = new Debt();

        debt.setDoknr(rs.getString("dok").trim());
        debt.setCustID(rs.getString("klient").trim());
        debt.setSum(rs.getDouble("tasumata") - rs.getDouble("makse"));
        debt.setDuedate(rs.getDate("maksepaev"));
        debt.setDue(rs.getString("yle"));

        Customer cust = (Customer)set.get(rs.getString("saaja").trim());
        if(debt.getSum()>1 || debt.getSum()<-1){
          if (cust != null) {
            java.util.Date maksep = debt.getDuedate();

            if (maksep.getTime() < today.getTimeInMillis()) {
              cust.setDebt(cust.getDebt() + debt.getSum());
            }
          }

          debts.addElement(debt);
        }
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

    String cmd = " SELECT realistaeg,k.kaup,kaup16,mhind AS myygihind,yhik,jaak AS stock,prodgrid AS grupp,ean_kood AS ean,tk_kastis AS kastis " +
         " from KAUBAD k, kaup_m90 a  , prodgroups pr ,realistaeg par WHERE par.kaup=k.kaup AND kaup_id=k.kaup AND pr.hinnakiri_id=k.hinnakiri_id " +
        " ORDER BY kaup16,realistaeg";

         if (isDebug()) {
             System.out.println(cmd);
         }

         TreeMap products = new TreeMap();
         try {
           Statement st = con.createStatement();
             ResultSet	      rs = st.executeQuery(cmd);
             HashMap prodHash = new HashMap();
             while (rs.next()) {
               String partii = rs.getString("realistaeg");
                 String kood = rs.getString("kaup").trim();
                 String nimetus = rs.getString("kaup16").trim();
                 nimetus = nimetus.replace('.',' ');

                 Product prod = new Product(kood);
                 prod.setPrice((rs.getDouble("myygihind")));
                 prod.setUnit(rs.getString("yhik").trim() );
                 prod.setStock(rs.getDouble("stock"));
                 String grupp = rs.getString("grupp").trim();
                 prod.setGroup(grupp);
                 if (partii != null && partii.trim().length()>0){
                 prod.setPartii(partii.trim().substring(0,10));
                 }

                 prod.setName(grupp + " " + nimetus.trim());
                 prod.setEAN(rs.getString("ean"));
                 prod.setInCases(rs.getDouble("kastis"));
                 //prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                 products.put(nimetus, prod);
                 prodHash.put(prod.getID(), prod);
             }
             rs.close();
         } catch (Exception e) {
             getParent().showError(e);
         }

         return products;
     }




      public String getDistribution() {
        return null;
      }


  public Vector getCustomerProductsList(String agentCode){

    String cmd =
        " SELECT d.klient, pr.prodgrid ,r.kaup,iif(d.kuup>date()-30,d.kuup,'') AS kuupa ,r.kogus , " +
        " iif(d.kuup>date()-30,iif(kogus>0,kogus,0),0) AS pluss  " +
        " , iif(d.kuup>date()-30,iif(kogus>0,0,kogus),0) AS minus " +
        "  ,d.kuup , khind,hind*(1-allahind) AS hind2,phind " +
        " FROM slehed d,slread r ,kliendid k,prodgroups pr ,kaubad t WHERE t.kaup=r.kaup and kuup>date()-60 AND k.grupp2 = '" + agentCode  +  "'  AND k.klient= d.klient " +
        "AND d.slehenr=r.slehenr AND r.kogus > 0 AND pr.hinnakiri_id=t.hinnakiri_id GROUP BY d.klient,r.kaup,iif(d.kuup>date()-30,d.kuup,''),d.kuup,kogus,iif(d.kuup>date()-30,iif(kogus>0,kogus,0),0)," +
        " iif(d.kuup>date()-30,iif(kogus>0,0,kogus),0),khind,hind*(1-allahind),phind,pr.prodgrid ORDER BY d.klient,pr.prodgrid,r.kaup,d.kuup";


    if (isDebug()) System.out.println(cmd);
    Vector custProds = new Vector();
    try {
      pstmt = con.prepareStatement(cmd);
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()){
        String kliendikood = rs.getString("klient").trim();
        String tootekood = rs.getString("kaup").trim();
        double kogus = rs.getDouble("kogus");
        double plusskogus = rs.getDouble("pluss");
        double miinuskogus = rs.getDouble("minus");
        String lastDate =  rs.getString("kuup").substring(5,10);
         double lastPrice = 0;
         double maarprice = 0;
         try{
          lastPrice = rs.getDouble("khind");
           maarprice = rs.getDouble("phind");
        } catch (Exception e){}
        double fixprice = rs.getDouble("hind2");


        if (lastPrice != 0){
        fixprice = lastPrice;
        }

        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,plusskogus ,miinuskogus,lastDate,fixprice,0,maarprice," ");
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

       double sales = 0;

       String oh = "SELECT kaup,keskm_omahind AS cost FROM keskm_hind ";

       String cmd = "SELECT r.kaup,SUM(r.hind*(1-r.allahind)) AS myygisumm,SUM(kogus) AS kogu ,a.tasu FROM slehed d,slread r ,kliendid k,tootajad a " +
       " WHERE month(kuup)=month(date()) AND YEAR(kuup)=YEAR(date()) AND k.grupp2 ='A' AND a.kood=k.grupp2 AND k.klient= d.klient " +
        " AND d.slehenr=r.slehenr GROUP BY r.kaup,a.tasu";

       TreeMap prods = new TreeMap();
       try {

        ResultSet rs1 = stmt2.executeQuery(oh);
        while (rs1.next()) {
          String kaup = rs1.getString("kaup");
          Product prod = new Product(kaup);
          prod.setMaxDiscount(rs1.getDouble("cost"));
          prods.put(kaup , prod);
        }

         rs1.close();

           ResultSet rs = stmt.executeQuery(cmd);
           while(rs.next()) {
              String toode = rs.getString("kaup");
              double myygisumm = rs.getDouble("myygisumm");
              double kogus = rs.getDouble("kogu");
              double tasu = rs.getDouble("tasu");
              Product prod = (Product)prods.get(toode);
              double cost = 0;
              if (prod != null ) {
              cost = prod.getMaxDiscount();
              System.out.println(prod.getID());
              }

              sales = sales +  ((myygisumm - (kogus* cost))*(tasu));
           }
       } catch (Exception e) {
              getParent().showError(e);
       }

       setSales(sales);
       return sales;
     }


  public DealerProduct getDealerProductsList() {
     return null;
          }

  public String getStartingTime(){
    return null;
  }

  public String getDriverVersion(){
    return "Esto";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){
    return null;
  }



      public TreeMap getDealers() {
        return dealers;
      }




  private String url = "jdbc:odbc:esto";
  private String url2 = "jdbc:odbc:kasum";
  private Connection con;
  private Statement stmt;
  private Connection con2;
  private Statement stmt2;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;
  private TreeMap  dealers = new TreeMap();
}
