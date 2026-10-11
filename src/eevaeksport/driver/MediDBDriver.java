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
public class MediDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater{


    public MediDBDriver(EksportPanel parent) {
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
       
    public void connectionUp(){
    }

    public String getAssortmentStr(Agent agent){
    	return null;
    }
    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "mobilejava", "mobilejava");
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
    String    sql = "SELECT matliik AS liik, nimetus FROM matliik WHERE matliik not in "+
        "('FURN','REKL','AUTO','VAAT','ALUS','KALV','KOKT','LAJO','VEIN')" ;

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




        public String getDistribution() {
          return null;
        }



    public TreeMap getAgents() {
      String  cmd = "  SELECT  kood,nimetus AS nimi FROM isikud  WHERE kood !='' AND len(trim(kood))=1 ";
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
      String cmd = " SELECT k.nimetus,k.kood,k.aadress, hinnakateg AS ale,k.tunnus_c1 AS visit, '' AS info,d.isik1 AS agent,k.maksja AS payer " +
       "  FROM kliendid k  right JOIN lvalh_m d ON k.kood=d.klient WHERE isik1!='' AND d.kuupaev>date()-300 AND trim(k.kood)!='' AND left(kood,1)!='M' "+
        "  GROUP BY k.kood ORDER BY NIMETUS " ;
          log(cmd);

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;
                String nimi = rs.getString("nimetus").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());

                String visit=rs.getString("visit");
                if (visit!=null){
                  cust.setVisit(visit.trim());
                }
                double ale = 0;
                String aLe = rs.getString("ale");
               if (aLe != null) {

                if (aLe.equals("B")) {
                    ale = 3;
                 }
                 else if (aLe.equals("C")) {
                    ale = 5;
                 }
                 else if (aLe.equals("D")) {
                    ale = 7;
                 }
                 else if (aLe.equals("E")) {
                    ale = 10;
                 }
                 else if (aLe.equals("F")) {
                    ale = 6;
                 }

               }
               cust.setDiscount(ale);

                //cust.setInfo(rs.getString("info").trim());
                cust.addAgent(new Agent(rs.getString("agent").trim()));
                cust.setPayer(rs.getString("payer").trim());
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
           "date()-d.tahtaeg AS yle from ../MERIT/aktiva/arveldus a "+
           " right JOIN lvalh_m d on d.doknumber=a.doknumber WHERE " +
           "(kreedit-a.deebet<-0.09 OR kreedit-a.deebet>0.09) AND tasuliik!='F' AND d.dokliik='M' AND d.tahtaeg=a.tahtaeg " +
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

      String cmd = "SELECT alltrim(t.matkood) AS toode,trim(t.nimetus) + ' ' + t.mootyhik AS nimi,t.hind_valja AS myygihind,t.grupp2 " +
     " AS unit,yhkoef2 AS tykke,t.matliik AS grupp,  t.triipkood AS ean , (s.kogus) AS stock, sum(e.kogus) AS stock2,kaaskaup1 AS pandipakend " +
     "  FROM kaubad t LEFT JOIN lseis_m s ON s.matkood = t.matkood  LEFT JOIN lseis_e e ON e.matkood = t.matkood WHERE " +
      " t.matliik!='TAAR' AND t.nimetus not like '%!!!%' AND t.matkood!= '' AND " +
	  " t.matkood not in('PANT1','PANT2','PANT3','PANT4') AND len(trim(t.grupp2))>0 " + 
	 "  group by nimetus ,t.mootyhik  ORDER BY t.nimetus desc,t.matkood " ;
       
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
                if(kood.indexOf("/") >  -1){
                	kood = kood.substring(0,kood.indexOf("/"));
                }
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
                 prod.setStock(rs.getDouble("stock"));
                 prod.setStock(prod.getStock()+ rs.getDouble("stock2"));
                 String pant = rs.getString("pandipakend");
                 if(pant != null && pant.trim().length()>0){
                 	prod.setPandPakendiKood(pant.trim());
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
          "SELECT d.klient,r.matkood AS toode, r.kogus, r.kuupaev FROM lvald_m r " +
          " INNER JOIN lvalh_m d ON d.doknumber=r.doknumber WHERE d.dokliik = 'M' AND r.kuupaev>date()-120 AND d.kuupaev=r.kuupaev " +
          " GROUP BY r.matkood,klient ORDER BY klient,matkood,r.kuupaev desc ";

      String sql11 =
        "SELECT d.klient,r.matkood AS toode, r.kogus, r.kuupaev FROM lvald_e r " +
        " INNER JOIN lvalh_e d ON d.doknumber=r.doknumber WHERE d.dokliik = 'M' AND r.kuupaev>date()-120 AND d.kuupaev=r.kuupaev " +
        " GROUP BY r.matkood,klient ORDER BY klient,matkood,r.kuupaev desc ";
      
      String sql2 = "SELECT d.klient,r.matkood,SUM(r.kogus) AS plusskogus  FROM lvald_m r INNER JOIN lvalh_m d " +
          " ON d.doknumber=r.doknumber WHERE r.kuupaev>date()-120 AND r.kogus>0 AND d.dokliik = 'M' AND d.kuupaev=r.kuupaev " +
          " GROUP BY r.matkood,d.klient ORDER BY d.klient,r.matkood";

      String sql3 = "SELECT d.klient,r.matkood,SUM(r.kogus)  AS minuskogus  FROM lvald_m r INNER JOIN lvalh_m d " +
          " ON d.doknumber=r.doknumber WHERE r.kuupaev>date()-120 AND r.kogus<0 AND d.dokliik = 'M' AND d.kuupaev=r.kuupaev " +
          " GROUP BY r.matkood,d.klient ORDER BY d.klient,r.matkood";


      if (isDebug()) {
        System.out.println(sql1);

      }

        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            if(tootekood.indexOf("/") >  -1){
            	tootekood = tootekood.substring(0,tootekood.indexOf("/"));
            }
            double kogus = Double.parseDouble(rs.getString("kogus").trim());
            Customer cust = (Customer)kliendid.get(kliendikood);
            if (cust == null){
              cust = new Customer(kliendikood);
            }
            Product prod = new Product(tootekood);
            cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0," ",0,0,0," "),false);
            kliendid.put(kliendikood, cust);
            //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
            //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
            CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0," ",0,0,0," ");
            custProds.add(cp);
          }

          rs.close();
          stmt.close();
           stmt = con.prepareStatement(sql11);
             rs = stmt.executeQuery();

          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            double kogus = Double.parseDouble(rs.getString("kogus").trim());
            Customer cust = (Customer)kliendid.get(kliendikood);
            if (cust == null){
              cust = new Customer(kliendikood);
            }
            if(tootekood.indexOf("/") >  -1){
            	tootekood = tootekood.substring(0,tootekood.indexOf("/"));
            }
            Product prod = new Product(tootekood);
            cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0," ",0,0,0," "),false);
            kliendid.put(kliendikood, cust);
            //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
            //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
            CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0," ",0,0,0," ");
            custProds.add(cp);
          }
          rs.close();
          stmt.close();
                    
          stmt = con.prepareStatement(sql2);
          rs = stmt.executeQuery();
          while(rs.next()){
            String klient = rs.getString("klient").trim();
            String toode = rs.getString("matkood").trim();
            if(toode.indexOf("/") >  -1){
            	toode = toode.substring(0,toode.indexOf("/"));
            }
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
          String toode = rs.getString("matkood").trim();
          if(toode.indexOf("/") >  -1){
        	toode = toode.substring(0,toode.indexOf("/"));
        }
          double mkogus = rs.getDouble("minuskogus");
          Customer c = (Customer)kliendid.get(klient);
          if (c != null){
            ArrayList al = (ArrayList)c.getProducts().get(toode);
            if (al != null){
              CustProd cp = (CustProd)al.get(1);
              if (cp != null){
                cp.setMovedStockMinus(mkogus);
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
      /*
      connectionUp(url);

      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, OR20024 AS summa FROM ST020100 " +
                   "LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001 " +
                   "LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
                  "AND LEN(RTRIM(OR20056))=0 GROUP BY ST02009,OR20024,SL01002,SL14003";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            sb.append(getParent().getString("name") + "  \t\t" + getParent().getString("number") +
                      "\t" + getParent().getString("sum") + sep);
            sb.append("-------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();*/
    }

    public void setBulkSales(Map agents){
      String cmd = "SELECT isik1 AS agent , round(sum(summakmta),0) AS sales FROM  lvalh_m d " +
                     " WHERE month(d.kuupaev)=month(date()) " +
              " AND YEAR(date())=YEAR(d.kuupaev) AND isik1 !=''AND dokliik='M' AND (tunnus like  isik1 OR left(tunnus,1) like isik1 OR right(left(tunnus,2),1) like isik1) GROUP BY isik1 ";
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
      if (agentCode == null) return 0;
      String cmd = "SELECT SUM(OR20024) AS sales ,OR20019 AS agent FROM OR200100 WHERE MONTH(OR20015)=" +
                   "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
        log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return 0;
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
     // System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Medi";
    //private String             url = "jdbc:odbc:scalaDB";
    private String url = "jdbc:odbc:merit";
        //"jdbc:freetds:sqlserver://NAVI-AJALUGU/TestNavi";
    private String             url2 = "jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

