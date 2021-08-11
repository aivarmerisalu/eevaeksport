/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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
public class MeritDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public MeritDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
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


        public String getDealerCustomerCSV(String agent){
         return null;
         }





    public void connectionUp(){

    }



        public String getDistribution() {
          return null;
        }



    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "mobilejava", "mobilejava");
    }

    /**
     *
     */
    public void connectionDown(){
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    public TreeMap getProdGroups() throws SQLException {
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT kood AS liik, nimetus FROM kgrupid1 WHERE kood in ('000001','000002','000010','000004','000020')" ;

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

    public TreeMap getAgents() {
      String  cmd = "SELECT tunnus_c3 AS kood,tunnus_c3 AS nimi FROM ../RP97/KLIENDID   group BY tunnus_c3  WHERE tunnus_c3 !='' ";
        TreeMap agents = new TreeMap();

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);


            while (rs.next()) {
              Agent agent = new Agent(rs.getString("kood").trim());

              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agents.put(nimi, agent);
            }
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }
        return agents;
    }

    public TreeMap getCustomers(Agent agent){
      String cmd = "SELECT nimetus AS nimi,kood,aadress, hinnakateg AS ale,arve3 AS visit,'' AS info,tunnus_c3 AS agent,maksja AS payer " +
	 " , www as kinnitus FROM ../RP97/kliendid  WHERE tunnus_c3 !=''  ORDER BY NIMETUS" ;
          log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            Iterator iter = getAgents().values().iterator();
             final Vector agents = new Vector();
             while (iter.hasNext()) {
             Agent agent1 = (Agent) iter.next();
             String agentCode = agent1.getID();
              agents.addElement(agentCode);}

            while (rs.next()) {
              counter++;
                String nimi = rs.getString("nimi").trim();
                String kood = rs.getString("kood").trim();
                Customer cust = new Customer(kood);
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());


                String visit=rs.getString("visit");
                if (visit!=null){
                  cust.setVisit(visit.trim());
                }

                String agenT = rs.getString("agent").trim();
                 //System.out.println(":"+agenT+":");
                if(agenT.equals("MITU")){
                    for (int i = 0 ; i < agents.size() ; i++){
                    String agentCode = (String) agents.get(i);
                    cust.addAgent(new Agent(agentCode));
                    //System.out.println(agentCode);
                    }

                } else {
                  cust.addAgent(new Agent(agenT));
                }
                String maksja = rs.getString("payer").trim();
                if(maksja.length()>0){
                cust.setPayer(maksja);
                } else {
                cust.setPayer(kood);
                }

                String confirm = rs.getString("kinnitus");
                if(confirm != null && confirm.trim().length()>0){
                  cust.setConfirmEmail(confirm.trim());
                }
                customers.put(cust.getName(), cust);
                custMap.put(cust.getPayer(), cust);

            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT d.klient,d.maksja,d.doknumber AS number, d.kuupaev,kreedit AS summa,deebet AS laekumine, " +
           "date()-d.tahtaeg AS yle from ../RP97/arveldus a "+
           " right JOIN lvalh_t d on d.doknumber=a.doknumber WHERE " +
           "(kreedit-a.deebet<-0.09 OR kreedit-a.deebet>0.09)  AND d.dokliik='M' AND d.tahtaeg=a.tahtaeg " +
           " ORDER BY d.klient " ;

      Connection con = getNewCon();
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        String klient = rs.getString("klient").trim();
            String maksja =rs.getString("maksja").trim();
            if(maksja != null && maksja.length()>1){
              klient = maksja.trim();
            }
        debt.setCustID(klient);
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("kuupaev"));
        double sum = Double.parseDouble(rs.getString("summa").trim());
        String laek =rs.getString("laekumine");
        if(laek != null) {
          double jaak = Double.parseDouble(laek.trim());
          sum = sum - jaak;
        }


        debt.setSum(sum);
        debt.setDue(rs.getString("yle"));
        list.addElement(debt);
      }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }

    public TreeMap getProducts(Agent agent){

      String cmd = "SELECT t.matkood AS toode,t.nimetus AS nimi,t.hind_valja AS myygihind,t.mootyhik AS unit,t.yhkoef2 AS tykke,t.grupp1 AS grupp, " +
	  " t.triipkood AS ean , s.kogus AS stock, e.tunnus0  AS separate FROM kaubad t RIGHT JOIN lseis_t s ON s.matkood=t.matkood  " +
          " LEFT JOIN kaubatun e ON e.matkood=t.matkood   " +
         " WHERE t.matliik!='TAAR' AND t.matkood!=''  AND t.grupp1 in ('000001','000002','000010','000004','000020') AND t.nimetus not like '%!!!%' ORDER BY t.matkood ";

        if (isDebug()) {
            System.out.println(cmd);
        }

        TreeMap products = new TreeMap();
        try {
          Connection con = getNewCon();
          Statement st = con.createStatement();
            ResultSet	      rs = st.executeQuery(cmd);
            HashMap prodHash = new HashMap();
            while (rs.next()) {
                String kood = rs.getString("toode").trim();
                String nimetus = rs.getString("nimi").trim();
                Product prod = new Product(kood);
                prod.setName(nimetus);
                String price =rs.getString("myygihind");
               if(price.length()>0){
                  prod.setPrice(parseDouble(price.trim()));
                }
                String unit = rs.getString("unit");
                if(unit.length()>0){
                  prod.setUnit(unit.trim());
                }
                String tykke = rs.getString("tykke");

                   try { prod.setInCases(parseDouble(tykke.trim()));}
                   catch (Exception e) {}

                prod.setGroup(rs.getString("grupp").trim());
                String ean = rs.getString("ean").trim();
                if(ean.length()>0){
                  prod.setEAN(ean);
                }
                 prod.setStock(parseDouble(rs.getString("stock").trim()));
                //prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                String separate = rs.getString("separate");
                if(separate != null && separate.length()>0){
                  if (separate.trim().equals("LIHA")) {
                    prod.setSendSeparate("tellimine.liha@slpt.ee");
                  }

                  if (separate.trim().equals("JUUST")) {
                    prod.setSendSeparate("tellimine.piim@slpt.ee");
                  }

                }



                products.put(nimetus, prod);
                prodHash.put(prod.getID(), prod);
            }

            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return products;
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public Vector getCustomerProductsList(String agentCode) {

      String sql1 =
          "SELECT d.klient,r.matkood AS toode, r.kogus, r.kuupaev FROM lvald_t r " +
          " INNER JOIN lvalh_t d ON d.doknumber=r.doknumber " +
          " WHERE d.dokliik = 'M' AND r.kuupaev>date()-30 AND d.kuupaev=r.kuupaev "+
          " GROUP BY r.matkood,d.klient ORDER BY d.klient,r.matkood,r.kuupaev desc ";

      String sql2 = "SELECT d.klient,r.matkood,SUM(r.kogus) AS plusskogus  FROM lvald_t r INNER JOIN lvalh_t d " +
          " ON d.doknumber=r.doknumber WHERE r.kuupaev>date()-30 AND r.kogus>0 AND d.dokliik = 'M' AND d.kuupaev=r.kuupaev  " +
          " GROUP BY r.matkood,d.klient ORDER BY d.klient,r.matkood";

      String sql3 = " select klient,kaup,kaubakood2 from klikaup where kaubakood2 != ''";


      if (isDebug()) {
        System.out.println(sql1);

      }

        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);

          //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            double kogus = Double.parseDouble(rs.getString("kogus").trim());
        
            Customer cust = (Customer)kliendid.get(kliendikood);
            if (cust == null){
              cust = new Customer(kliendikood);
            }
            Product prod = new Product(tootekood);
            CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0," ",0,0,0," ");
            
          
            cust.addProduct(prod,cp ,false);
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
            String klient = rs.getString("klient").trim();
            String toode = rs.getString("matkood").trim();
            double pkogus = rs.getDouble("plusskogus");
            Customer c = (Customer)kliendid.get(klient);
            if (c != null){
              ArrayList al = (ArrayList)c.getProducts().get(toode);
              if (al != null){
                CustProd cp = (CustProd)al.get(1);
                if (cp != null){
                  cp.setMovedStockPluss(pkogus);
                  custProds.add(cp);
                }
              }
            }
          }

          rs.close();
          stmt.close();
          stmt = con.prepareStatement(sql3);
        rs = stmt.executeQuery();
        while(rs.next()){
          String klient = rs.getString("klient").trim();
          String toode = rs.getString("kaup").trim();
          String  abikoo = rs.getString("kaubakood2");
          Customer c = (Customer)kliendid.get(klient);
          if (c != null){
            ArrayList al = (ArrayList)c.getProducts().get(toode);
            if (al != null){
              CustProd cp = (CustProd)al.get(1);
              if (cp != null){
                cp.setAbiKood(abikoo);
                custProds.add(cp);
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

    public String getPilotDayData(Agent agent){
      return null;
    }


    public String getNonMobileOrders(Agent agent){
      return null;
    }

    public void setBulkSales(Map agents){
      String cmd = "SELECT tunnus_c3 AS agent , round(sum(kogus*hind_valja),0) AS sales FROM lvald_t r " +
           "INNER JOIN lvalh_t d ON d.doknumber=r.doknumber " +
          " INNER JOIN ../RP97/kliendid k ON k.kood=d.klient " +
          " WHERE month(d.kuupaev)=month(date()) " +
          " AND YEAR(date())=YEAR(d.kuupaev) AND tunnus_c3 !='' GROUP BY tunnus_c3 ";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent");
              Agent agent = (Agent)agents.get(agentID.trim());
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("sales"));
                //System.out.println(agent.toString() + " " + agent.getMonthSales());
              }
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {

        return 0;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Connection con = getNewCon();
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
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getStartingTime() {
      return null;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getDriverVersion() {
        return DRIVER_VERSION;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getSubjectFilesTo() {
        return this.subjectFilesTo;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void log(String s) {
        getParent().log(s);
    }

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
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

    public static final String DRIVER_VERSION = "Merit";
    private String url = "jdbc:odbc:merit";
    private String             url2 = "jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

