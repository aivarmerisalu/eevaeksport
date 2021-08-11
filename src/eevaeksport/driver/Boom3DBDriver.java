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
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;





public class Boom3DBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater{


	
	public Boom3DBDriver(EksportPanel parent) {
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

    
	
	public void connectionUp(){
    }
    
	public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, "kaire", " ");
    }


            public String getDealerCustomerCSV(String agent){
             return null;
             }

                                                public String  getCustProdDiscounts(Agent agent){
                                                 return null;
                                                 }


    public String getSaveStockInfo(){
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

        public String getDistribution() {
          return null;
        }


    public TreeMap getProdGroups() throws SQLException {
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "Select metagroupno AS liik ,name AS nimetus from metagrup " ;

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
    setTreeGroup(true);
    return tm;
    }


     public TreeMap getProdGroupsSecond() throws SQLException {
      Connection con = getNewCon();
    TreeMap   tm = new TreeMap();
    String    sql = "SELECT (trim(str(g.metagroupno))+trim(str(g.groupno))) AS liik , g.name AS nimetus FROM artgrp g,artmain  t " +
        "WHERE t.groupno=g.groupno AND t.passive=0 GROUP BY g.name,(trim(str(g.metagroupno))+trim(str(g.groupno))) ";
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
      String  cmd = "  SELECT emplno AS yksus,(str(emplno)+' '+ name) AS nimetus,1 AS ladu FROM employee WHERE validdate>date()  ORDER BY name ";
        TreeMap agents = new TreeMap();
System.out.println(cmd);
        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);


            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              String objekt = rs.getString("ladu");
              if(agent.getID().equals("21")){
                agent.addProdGroupList("1");
                agent.addProdGroupList("4");
              }
              if (agent.getID().equals("32")) {
                agent.addProdGroupList("3");
                System.out.println(agent.toString());
              }

              if (agent.getID().equals("8")) {
                agent.addProdGroupList("1");
                agent.addProdGroupList("4");
                System.out.println(agent.toString());
              }

              agent.setName(nimi);
              if (objekt != null){
                agent.setObject(objekt.trim());
              }
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
      String cmd = "SELECT custno AS kood, name AS nimi, address AS aadress, '' AS visit, " +
                 "   custgroupno AS agent FROM cumain WHERE LEN(custgroupno)>0  AND passive=0 AND  typeno=1 ORDER BY name";
        log(cmd);

        TreeMap customers = new TreeMap();

        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;

                Customer cust = new Customer(rs.getString("kood").trim());
                String nimi = rs.getString("nimi").trim();
                cust.setName(nimi);
                String add = rs.getString("aadress");
                if (add != null){
                  cust.setAddress(add.trim());
                } else cust.setAddress(" ");
                String visit =rs.getString("visit");
                    if (visit != null){
                cust.setVisit(visit);
                    } else {
                    cust.setVisit(" ");
                  }
                String agent1 = rs.getString("agent").trim() ;
                String agent3 = "0";
                if(agent1.trim().equals("2") || agent1.trim().equals("9") || agent1.trim().equals("18")){
                agent3= "21";//Ylle Arno
                cust.addAgent(new Agent(agent3));
                }
                if(agent1.trim().equals("12") || agent1.trim().equals("9") || agent1.trim().equals("4") || agent1.trim().equals("8") || agent1.trim().equals("16") || agent1.trim().equals("21")){
                agent3= "32";
                cust.addAgent(new Agent(agent3));//Marek pollman

                 }
                if(agent1.trim().equals("6") || agent1.trim().equals("12")) {
                  agent3= "8";
                  cust.addAgent(new Agent(agent3));//Kaire Lohk


                 }
                //if  (payer != null) cust.setPayer(payer.trim());
                customers.put(cust.getName(), cust);
                //custMap.put(cust.getPayer(), cust);

            }
            rs.close();

            /*String sql = "SELECT a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " +
                  " AS kuupaev,a.[MD Summa] AS summa,SUM(tas.[Tasutud Summa])AS tasutud " +
                   "FROM TVMDKP a LEFT JOIN TVMDKK tas on tas.[Keha pvoti]=a.[Keha voti] " +
                   "WHERE " +
                   "a.[MD summa]<>0 AND a.kuup�ev>date()-300 AND (a.kuup�ev+a.[Viivise valem])< date() " +
                   "GROUP BY a.number,a.[Maksja/Saaja],a.[MD Summa],(a.kuup�ev+a.[Viivise valem]) "+
                   "ORDER BY a.[Maksja/Saaja] ";


            rs = stmt.executeQuery(sql);
            while(rs.next()){
              String id = rs.getString("klient").trim();
              double debt = Double.parseDouble(rs.getString("summa").trim());
              double paid = 0;//
              try {
                paid = Double.parseDouble(rs.getString("tasutud").trim());
              }
              catch (Exception e) {}
              Customer cust = (Customer) custMap.get(id);
              if (cust != null) {
                cust.setDebt(cust.getDebt() + debt - paid);
              }
            }
            rs.close();
            stmt.close();
            con.close();*/
        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      /*String sql = "SELECT a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " +
                  " AS kuupaev,a.[MD Summa] AS summa,SUM(tas.[Tasutud Summa])AS tasutud " +
                   "FROM TVMDKP a LEFT JOIN TVMDKK tas on tas.[Keha pvoti]=a.[Keha voti] " +
                   "WHERE " +
                   "a.[MD summa]<>0 AND a.kuup�ev>date()-300 AND (a.kuup�ev+a.[Viivise valem])< date() " +
                   "GROUP BY a.number,a.[Maksja/Saaja],a.[MD Summa],(a.kuup�ev+a.[Viivise valem]) "+
                   "ORDER BY a.[Maksja/Saaja] ";
               //((a.[MD Summa])<>(tas.[Tasutud Summa]) OR a.[Keha voti]=0) AND
      Connection con = getNewCon();
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery(sql);

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        double paid = 0;
        debt.setDuedate(rs.getDate("kuupaev"));
        double sum = Double.parseDouble(rs.getString("summa").trim());
        try {
          String Paid = rs.getString("tasutud");
          if (Paid != null) {
            paid = Double.parseDouble(Paid.trim());
          }
        }
        catch (NumberFormatException e) {
        }

        debt.setSum(sum - paid);
        //debt.setDue(rs.getString("yle"));
        if (sum-paid != 0) {
          list.addElement(debt);
        }
      }
      rs.close();
      stmt.close();
      con.close();
      //return list;*/
      return list;
    }

    public TreeMap getProducts(Agent agent){

      String cmd = "SELECT  t.artno AS toode,trim(t.name) + ' <' + trim(t.artcode) +'>'  AS nimetus,u.salesprice AS myygihind ,u.unitname AS unit, " +
				"  t.info AS tykke,b.barno AS ean,(trim(str(g.metagroupno))+trim(str(g.groupno))) AS grupp2,g.metagroupno AS grupp ,l.volume AS stock " +
                 " FROM artmain t,unit u ,barart b,artgrp g,stock l WHERE t.artno=u.artno AND t.artno=b.artno AND t.groupno=g.groupno " +
                 " AND l.stockno = 1 AND t.artno=l.artno AND t.passive=0 ORDER BY t.name";

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
                String nimetus = rs.getString("nimetus").trim();
                //String nimetus2 = rs.getString("nimetus2");
                //if (nimetus2 != null) nimetus = nimetus + " " + nimetus2.trim();
                Product prod = new Product(kood);
                prod.setName(nimetus);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
                prod.setUnit(rs.getString("unit").trim() );
                try{
                  String kast = rs.getString("tykke");
                  if (kast != null) {
                    prod.setInCases(parseDouble(kast.trim()));
                  }
                }catch (Exception e){}
                String ean = rs.getString("ean");
                if (ean != null) {
                  if (ean.trim().substring(0,2).equals("00")){
                  ean = ean.trim().substring(1,ean.length());
                  }
                prod.setEAN(ean.trim());
                }
                prod.setGroupSecond(rs.getString("grupp2").trim());
                prod.setGroup(rs.getString("grupp").trim());
               double sto = parseDouble(rs.getString("stock").trim());
                prod.setStock(sto);
                //prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                if(sto == 0 && nimetus.startsWith("*")){
                 //System.out.println(nimetus);
                }else {
                  products.put(nimetus, prod);
                  prodHash.put(prod.getID(), prod);
                }
            }
            rs.close();


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

    public DealerProduct getDealerProductsList() {

     return null;
    }


    public Vector getCustomerProductsList(String agentCode) {

      String sql = "SELECT r.artno AS toode,d.custno AS klient ,r.volume AS kogus ,d.invdate AS lastdate FROM outitems r, outbill d " +
				" WHERE d.invdate>?  AND r.invoiceno = d.invoiceno  AND len(d.custno)>0 " +
				" GROUP BY r.artno, d.custno, r.volume,d.invdate ORDER BY d.custno, r.artno";

        if (isDebug()) {
            System.out.println(sql);
        }

        Vector custProds = new Vector();

        try {
          Connection con = getNewCon();
          PreparedStatement stmt = con.prepareStatement(sql);
          stmt.setDate(1, getStartingDate());;
         ResultSet	      rs = stmt.executeQuery();

          while (rs.next()) {
            String kliendikood = rs.getString("klient").trim();
            String tootekood = rs.getString("toode").trim();
            String lastdate =  rs.getString("lastdate").trim();
            lastdate = lastdate.substring(0,10);
            CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()) ,0 ,0," ",0,0,0," ");
            custProds.add(cp);
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
    	boolean ok = true;
      if (ok)return;
      String cmd = "SELECT round(SUM(d.amount-d.vatamount),0) AS summa,emplno AS agent  FROM  outbill d " +
      " WHERE  month(invdate)= month(date()) " +
      " AND year(d.invdate)= year(date()) AND len(emplno)>0 GROUP BY emplno ORDER BY emplno";
      log(cmd);

        try {
          Connection con = getNewCon();
            Statement stmt = con.createStatement();

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent").trim();
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("sales"));
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
    	boolean ok = true;
      if (ok) return 0;
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
    public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1) month = "0" + month;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1) day = "0" + day;
    return day + month + cal.get(Calendar.YEAR);
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
   
       private double parseDouble(String s) {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < s.length(); i++) {
           char ch = s.charAt(i);
           if (isNumeric(ch)) {
             sb.append(ch);
           }
           else if ( (ch == '.') || (ch == ',')) {
             sb.append('.');
           }
           else if ( (ch == ' ')) {
             sb.append("");
           }

         }
         return new Double(sb.toString()).doubleValue();
       }

       private static boolean isNumeric(char ch) {
         return ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' ||
             ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
       }


       public TreeMap getDealers() {
         return dealers;
       }

       public void setDealers(TreeMap aDealers) {
         dealers = aDealers;
       }

       // private String url = "jdbc:DBF:///c://work/avallone/dbf";
       public static final String DRIVER_VERSION = "Boom3";
       //private String             url = "jdbc:odbc:scalaDB";
       private String url = "jdbc:odbc:boom";
       //"jdbc:freetds:sqlserver://NAVI-AJALUGU/TestNavi";
       private String url2 = "jdbc:odbc:report";
       private String subjectFilesTo = null;
       private NumberFormat NF = NumberFormat.getInstance();
       private TreeMap dealers = new TreeMap();
     }
