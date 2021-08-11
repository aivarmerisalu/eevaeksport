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
import eevaeksport.MatrixLine;
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
public class MotoralDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public MotoralDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        //String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
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

    public Vector getFixedAssortmentData() {
      return null;
    }

        public String getSaveStockInfo(){
        return null;
        }

        public String getProductSortingData(){
           	return null;
           }
                public String getDealerCustomerCSV(String agent){
                 return null;
                 }

            	public Vector getLastThreeData() {
            		// TODO Auto-generated method stub
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



        public String getDistribution() {
          return null;
        }



    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "toni", "toni");
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
    String    sql = "SELECT tpr_koodi as liik ,tpr_nimi as nimi FROM tuotepaaryhma " ;
     System.out.println(sql);
     setTreeGroup(true);
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimi").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
    }
    public TreeMap getProdGroupsSecond() throws SQLException {

       Connection con = getNewCon();
       TreeMap   tm = new TreeMap();
       String    sql = " SELECT rtrim(tur_osasto_yleinen )+tur_koodi AS liik , tur_nimi AS nimi FROM tuotryhm where length( rtrim(tur_osasto_yleinen )+tur_koodi)>0 ";
       Statement st = con.createStatement();
       ResultSet rs = st.executeQuery(sql);

       while (rs.next()) {
           tm.put(rs.getString("liik").trim(),
                  rs.getString("nimi").trim());
       }
       rs.close();
       st.close();
       con.close();

       return tm;
   }


    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = "  SELECT asa_asiakasnro AS custid,asa_asiakryhmkoodi AS clgroup,asa_tuotekoodi AS prodid,asa_tuoteryhmkoodi " +
		" AS prodgroup,asa_alepros AS discount,asa_kmyyntihinta  AS price FROM asiakale ";
          Statement st = null;
          try {
            Connection con = getNewCon();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

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
              if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup);
              }
                mline.setDiscount(discount);

              if (price != 0) {
                mline.setPrice(price);
              }
             lines.add(mline);
            }

            rs.close();
            st.close();
            con.close();
          }
          catch (SQLException ex) {
          }
        return lines;
      }

    public TreeMap getAgents() {
      String  cmd = "SELECT ary_koodi as kood, ary_nimi as nimi FROM asiakryh WHERE length((ary_koodi))>2 ";
        TreeMap agents = new TreeMap();
        System.out.println(cmd);
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

    	String cmd = " SELECT ap_asiakasnro AS kood, ap_nimi AS nimi, rtrim(rtrim(ap_katuosoite) +' '+ rtrim(ap_postinumero) +' '+ rtrim(ap_postitoimipaikka)) AS aadress, " +
        " asr_asiakryhmkoodi AS agent,'' AS payer  ,ap_luottoraja AS credit,av_avoinlasksaldo as debt FROM asiakper " +
        " LEFT JOIN asiakkaanryhm on asr_asiakasnro=ap_asiakasnro    " +
        " LEFT JOIN asiakvai on av_asiakasnro=asr_asiakasnro "+
        " WHERE  asr_asiakryhmkoodi in ('10','1','2','3','5') ";

          log(cmd);
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
         System.out.println(cmd);
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
              Customer cust = new Customer(rs.getString("kood").trim());
                String nimi = rs.getString("nimi").trim();
                cust.setName(nimi);
                String aadress = rs.getString("aadress");
                if (aadress != null){
                cust.setAddress(aadress.trim());
                }
                cust.addAgent(new Agent("104"));
                String credit = rs.getString("credit");
                if (credit != null){
                cust.setCredit(credit.trim());
                }
                cust.setDebt(rs.getDouble("debt"));
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
      String sql = "SELECT alr_asiakasnro AS klient,alr_asiakasnro AS maksja,alr_asiakasnro AS number, alr_erapvm AS kuupaev,alr_avoinna_nyt AS summa " +
       " FROM avlaskut WHERE alr_avoinna_nyt!=0 ORDER BY alr_asiakasnro " ;
      Connection con = getNewCon();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);
       System.out.println(sql);
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
        debt.setSum(sum);
        list.addElement(debt);
      }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }



    public TreeMap getProducts(Agent agent){

    String xx = "''";
    if(parent.getProps().getProperty("list") != null && parent.getProps().getProperty("list").trim().length()>0
       ){
    xx = parent.getProps().getProperty("list");
    }


      String cmd = " SELECT  tp_tuotekoodi AS toode,rtrim(tp_nimi) +' ' + tp_tuotekoodi AS nimi,tp_myyntihinta/1.20 AS myygihind,'tk' AS unit, " +
                  " tur_osasto_yleinen AS grupp,rtrim(tur_osasto_yleinen )+ tp_ryhmakoodi AS grupp2,tv_varastossa AS stock, (case when tp_myyntihinta=0 " +
				 " or tp_ostohinta=0 then 0 else round((1-tp_ostohinta/tp_myyntihinta)*100,2) end) AS kate " +
                  " FROM tuotper  LEFT JOIN tuotvaih ON tv_tuotekoodi=tp_tuotekoodi LEFT JOIN tuotryhm ON tur_koodi=tp_ryhmakoodi " +
				  " WHERE tp_myyntihinta!=0 AND tv_toimittajanro=1 AND tp_aktiivinen=1 AND tp_ryhmakoodi !='196' AND tp_ryhmakoodi !=(case when tv_varastossa>0 then '     ' else '2820' end) " +
				  " AND tp_ryhmakoodi !=(case when tv_varastossa>0 then '     ' else '2220' end) AND (tp_takuukk)>-1 " +
				" AND tp_ryhmakoodi !='0734' AND tp_ryhmakoodi !='0730' AND tp_ryhmakoodi !='0720' AND tp_ryhmakoodi !='0715' " +
				  " AND tp_ryhmakoodi !='0710' AND tp_ryhmakoodi !='2830' AND tp_ryhmakoodi !='2835' AND tp_ryhmakoodi !='2840' "+
				  " AND tp_ryhmakoodi !='2845' AND tur_osasto_yleinen != (case when tv_varastossa>0 then '     ' else '3' end) " +
                                "  AND tp_ryhmakoodi not in ('2130','2150','2411','2413','2415','2418','2422','2425','2427','2428','2429','2438' " +
				" ,'2604','2620','2625','2630','2645','2646','2650','2660','2665') AND tp_ryhmakoodi not in (" + xx + ") " +
                                  " AND tp_ryhmakoodi !='2845' AND tur_osasto_yleinen != (case when tv_varastossa>0 then '     ' else '4' end) ";

                   System.out.println(cmd);

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
                if(unit.length()>0 && unit.trim().length()>0){
                  prod.setUnit(unit.trim());
                }
                String grupp = rs.getString("grupp");
                if( grupp!= null && grupp.trim().length()>0 ){
                  prod.setGroup(grupp.trim());
                }
                String grupp2 = rs.getString("grupp2");
                if( grupp2!= null && grupp2.trim().length()>0 ){
                  prod.setGroupSecond(grupp2.trim());
                }
                prod.setStock(parseDouble(rs.getString("stock").trim()));
                prod.setMargin(rs.getString("kate").trim());
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

      String sql1 ="SELECT myp_asiakasnro AS klient,rtrim(myr_tuotekoodi) AS toode,myr_maara AS kogus,myr_rivisumma/myr_maara/1.20 AS lastprice FROM " +
	   " myyntper inner join myyntriv on myr_myyntinro=myp_myyntinro " +
      " WHERE  myp_pvm>'" + getStartingTime() + "' AND length(myr_tuotekoodi)>0 AND myr_maara!=0 " +
      "  GROUP BY myr_tuotekoodi, myp_asiakasnro, myp_pvm ,myr_maara,myr_rivisumma ORDER BY myp_asiakasnro,myr_tuotekoodi,myp_pvm asc";

        System.out.println(sql1);

        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql1);
          ResultSet	      rs = stmt.executeQuery();
          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            //System.out.println(kliendikood);
            String tootekood = rs.getString("toode").trim();
            double kogus = Double.parseDouble(rs.getString("kogus").trim());
            double lastprice = Double.parseDouble(rs.getString("lastprice").trim());
            Customer cust = (Customer)kliendid.get(kliendikood);
            if (cust == null){
              cust = new Customer(kliendikood);
            }
            Product prod = new Product(tootekood);
            cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0," ",lastprice,0,0," "),false);
            kliendid.put(kliendikood, cust);
            if (kliendikood.length() > 0) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0," ",lastprice,0,0," ");
                  custProds.add(cp);
                }

          }

          rs.close();
          stmt.close();
          con.close();
        } catch (Exception e) {
          getParent().showError(e);
        }
        connectionDown();
       // System.out.println(custProds.size());
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
      String cmd = " SELECT ROUND(SUM(myr_rivisumma),0) AS sales,asr_asiakryhmkoodi AS agent  FROM " +
		 "  myyntper inner join myyntriv on myr_myyntinro=myp_myyntinro " +
		  " LEFT JOIN asiakkaanryhm on asr_asiakasnro=myp_asiakasnro " +
          " WHERE  YEAR(myp_pvm)=YEAR(curdate()) AND MONTH(myp_pvm)=MONTH(curdate())" +
           " AND asr_asiakryhmkoodi!='' AND length(asr_asiakryhmkoodi)>2 GROUP BY asr_asiakryhmkoodi ORDER BY asr_asiakryhmkoodi";
        log(cmd);
         System.out.println(cmd);

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
        Calendar cal = Calendar.getInstance();

        // cal.add(Calendar.MONTH, -6);
        cal.add(Calendar.MONTH,
                -Integer.parseInt(getParent().getProps().getProperty("months")));

        int month = cal.get(Calendar.MONTH) + 1;
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String sqlDate = ""+cal.get(Calendar.YEAR) + "-" + month + "-"
                         +  day ;
        return sqlDate;
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
    public static final String DRIVER_VERSION = "Motoral";
    //private String             url = "jdbc:odbc:scalaDB";
    private String url = "jdbc:odbc:ShMem AFP";
        //"jdbc:freetds:sqlserver://NAVI-AJALUGU/TestNavi";
    private String             url2 = "jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

