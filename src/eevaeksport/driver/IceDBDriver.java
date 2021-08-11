/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Hashtable;
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
public class IceDBDriver extends AbstractDBDriver {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public IceDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }

    public String getAssortmentStr(Agent agent){
    	return null;
    }
    public String getSaveDebtInfo() {
       	return null;
       }
       
            public String getDealerCustomerCSV(String agent){
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
    public void connectionUp(){
      connectionUp(this.url);
    }

    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }

           con = DriverManager.getConnection(url);
      } catch (Exception e) {
	    getParent().showError(e);
      }

    }

    public TreeMap getProdGroups() throws SQLException {

           TreeMap   tm = new TreeMap();
           String    sql = "select grupp AS liik,grupi_nimi as nimi from artikkel group by grupp";
           Statement st = con.createStatement();
           ResultSet rs = st.executeQuery(sql);

           while (rs.next()) {
             String liik=rs.getString("liik").trim();
             String nimetus=rs.getString("nimi").trim();
             if (liik != ""){
             tm.put(liik,nimetus);
             }
           }
           rs.close();
           st.close();


           return tm;

    }



        public String getDistribution() {
          return null;
        }


    /**
     *
     */
    public void connectionDown(){
        try {
           con.close();
           con = null;
	} catch (Exception e) {
	    getParent().showError(e);
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
    public TreeMap getAgents() {
        connectionUp(url2);
	String  cmd = "SELECT agent AS yksus, nimi AS nimetus ,objekt from agent ";
        TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();
	   
	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setObject(rs.getString("objekt").trim());
              agent.setName(nimi);
              agents.put(nimi, agent);
	    }
            stmt.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }



    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT yksus AS kood, nimetus AS nimi,  aadress,siffer as ale ,makseting AS makse" +
              " FROM yksus WHERE ltrim(str( mm)) = ?  AND pmviitenr != 1 ORDER BY nimetus";
	log(cmd);

	TreeMap customers = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agent.getID());
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		Customer cust = new Customer(rs.getString("kood").trim());
		String nimi = rs.getString("nimi").trim();
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());
                cust.setPrintean("T");
                cust.setDiscount(rs.getDouble("ale"));
                cust.setPaymTerm(rs.getString("makse").trim());
                //cust.setDiscount(-rs.getDouble("ale"));
               // cust.setVisit(rs.getString("visit").trim());
               // String printean = rs.getString("printean");
                //if (printean != null){
                  //cust.setPrintean(printean.trim());
                //}
		customers.put(nimi, cust);
	    }
            stmt.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return customers;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);

      connectionUp();
   TreeMap products = new TreeMap();

   String sql = "SELECT aeg as alguspaev ,sum(kogus) AS kogus, artikkel AS toode FROM ladu/laosaldo ls,aeg "+
       "  WHERE (ls.yksus = ? OR ls.yksus = ?) GROUP BY artikkel ORDER BY algus";
   int obj = Integer.parseInt(agent.getObject());
   int obj2 = Integer.parseInt(agent.getObject());
   int obj3 = Integer.parseInt(agent.getObject());
   if (obj != 300) {
     obj2 = 100;
     obj3 = 800;
   }

   try {
     PreparedStatement pstmt = con.prepareStatement(sql);
     pstmt.setInt(1, obj2);
     pstmt.setInt(2, obj3);
     ResultSet rs = pstmt.executeQuery();
     Hashtable tooted = new Hashtable();
     Date kuupaev = null;
     while(rs.next()){
       kuupaev = rs.getDate("alguspaev");
       String id = rs.getString("toode").trim();
       Toode toode = new Toode(id);
       toode.setAlguspaev(kuupaev);
       toode.setKogus(rs.getDouble("kogus"));
       tooted.put(id, toode);
       //System.out.println("1. " + toode);
     }
     rs.close();
     pstmt.close();


     sql = "      SELECT r.artikkel AS toode,SUM(IIF(d.SYSOPERATS=('A') OR d.sysoperats=('L'),r.kogus,0)) AS sisse " +
            " FROM ladu/ladu d  " +
            " INNER JOIN ladu/lstl r ON r.viit=d.viit LEFT JOIN " +
            " ladu/laosaldo ls ON (ls.artikkel = r.artikkel AND (ls.yksus = ? OR ls.yksus = ?)) " +
            " WHERE d.kuupaev=>(select aeg from aeg) AND (d.yksus=? OR d.yksus=?) AND d.tellimlipp!='P'  GROUP BY r.ARTIKKEL";

     pstmt = con.prepareStatement(sql);
     pstmt.setInt(1, obj2);
     pstmt.setInt(2, obj3);
     pstmt.setInt(3, obj2);
     pstmt.setInt(4, obj3);

     rs = pstmt.executeQuery();
     while(rs.next()){
       String id = rs.getString("toode").trim();
       Toode toode = (Toode)tooted.get(id);
       if (toode == null){
         toode = new Toode(id);
         toode.addKogus(rs.getDouble("sisse"));
         tooted.put(id, toode);
       } else {
         toode.addKogus(rs.getDouble("sisse"));
         //System.out.println("2. " + toode);
       }
     }
     rs.close();
     pstmt.close();

     //System.out.println("1. tooted suurus: " + tooted.size());

     sql = "SELECT r.artikkel AS toode,SUM(IIF(d.SYSOPERATS=('V') OR d.sysoperats=('L'),r.kogus,0)) AS valja " +
            " FROM ladu/ladu d  " +
            " INNER JOIN ladu/lstl r ON r.viit=d.viit LEFT JOIN " +
            " ladu/laosaldo ls ON (ls.artikkel = r.artikkel AND (ls.yksus = ? OR ls.yksus = ?)) "+
            " WHERE d.kuupaev=>(select aeg from aeg) AND (d.firma=? OR d.firma=?) AND d.tellimlipp!='P'  GROUP BY r.ARTIKKEL";

     pstmt = con.prepareStatement(sql);
     pstmt.setInt(1, obj2);
     pstmt.setInt(2, obj3);
     pstmt.setInt(3, obj2);
     pstmt.setInt(4, obj3);

     rs = pstmt.executeQuery();
     while(rs.next()){
       String id = rs.getString("toode").trim();
       Toode toode = (Toode)tooted.get(id);
       if (toode == null){
         toode = new Toode(id);
         toode.addKogus(-rs.getDouble("valja"));
         tooted.put(id, toode);
       } else {
         toode.addKogus(-rs.getDouble("valja"));
         //System.out.println("3. " + toode);
       }
     }
     rs.close();
     pstmt.close();

    /*
     System.out.println("2. tooted suurus: " + tooted.size());
     Enumeration enum = tooted.elements();
     while (enum.hasMoreElements()){
       Toode toode = (Toode)enum.nextElement();
       //System.out.println(toode);
     }*/

     // String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
     String cmd = "SELECT  l.toodlipp,a.artikkel AS toode, a.nimetus AS nimetus, a.grupp  AS grupp,l.sisukogus AS tykke, l.realhind AS myygihind, ls.kogus, l.ribakood AS ean " +
                "FROM artikkel a LEFT JOIN ladu/laduart l ON a.artikkel = l.artikkel " +
                "LEFT JOIN ladu/laosaldo ls ON (ls.artikkel = a.artikkel AND (ls.yksus = ? OR ls.yksus = ?)) " +
                "WHERE len(ltrim(str(a.artikkel))) >= 5 AND len(ltrim(temp))=0 " +
                "AND(a.artikkel BETWEEN 200000 AND 899999) and len(trim(a.nimetus))!=0 group by a.artikkel,a.nimetus";

            if (isDebug()) {
                System.out.println(cmd);
            }

                stmt = con.prepareStatement(cmd);
                stmt.setInt(1, obj2);
                stmt.setInt(2, obj3);

                 rs = stmt.executeQuery();

                 while (rs.next()) {

                   String toodlipp = rs.getString("toodlipp");
                     String kood = rs.getString("toode").trim();
                     Toode toode = (Toode)tooted.get(kood);
                     double kogus;
                     if (toode == null){
                       //System.out.println(kood);
                       kogus = 0;
                     } else {
                       kogus = round(toode.getKogus(), 2);
                       //System.out.println(kogus);
                     }
                     String nimetus =rs.getString("nimetus").trim();
                     if ((toodlipp != null) && toodlipp.equalsIgnoreCase("t")){
                 nimetus = " " + nimetus;
               }

                     Product prod = new Product(kood);
                     prod.setName(nimetus);
                     prod.setPrice(rs.getDouble("myygihind"));
                     String grupp = rs.getString("grupp");

                     if(grupp != null && grupp.trim().length()>0){
                     prod.setGroup(grupp.trim());}
                     prod.setInCases(rs.getDouble("tykke"));
                     String ean = rs.getString("ean");
                     if (ean != null && ean.trim().length()>0){
                       prod.setEAN(ean.trim());
                     }
                     prod.setStock(kogus);
                     products.put(nimetus, prod);

                 }

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
        connectionUp(url3);
	/*
	 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
	 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */

        String cmd = "SELECT l.yksus AS klient, s.artikkel AS toode " +
            "FROM ladu/ladu l INNER JOIN ladu/lstl s ON l.viit = s.viit INNER JOIN artikkel a ON a.artikkel=s.artikkel " +
            "INNER JOIN ladu/laduart la ON la.artikkel=a.artikkel INNER JOIN yksus y ON y.yksus=l.yksus " +
            "WHERE len(ltrim(str(a.artikkel))) >= 5 AND(a.artikkel BETWEEN 200000 AND 899999) and len(trim(a.nimetus))!=0 " +
            "AND l.kuupaev >= ? AND ltrim(str(y.mm))=? GROUP BY l.yksus, s.artikkel ORDER BY l.yksus,a.nimetus";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(2, agentCode);
            stmt.setDate(1, getStartingDate());
	    ResultSet	      rs = stmt.executeQuery();
	   
	    while (rs.next()) {
		String kliendikood = rs.getString("klient").trim();
		String tootekood = rs.getString("toode").trim();

		if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0," ",0,0,0," ");
                  custProds.add(cp);
		}
	    }
            rs.close();
            stmt.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }

    public String getPilotDayData(Agent agent){


      String sql = "SELECT LEFT(grupinimi, 4) + ' ' + a.nimetus AS nimetus, a.sisu AS tykke, " +
          " l.hk_pood AS myygihind, ls.kogus AS ladu , SUM(lstl.kogus) AS kogus" +
          "           FROM artikkel a INNER JOIN ladu/laduart l ON a.artikkel = l.artikkel LEFT JOIN ladu/laosaldo ls ON  " +
          " (ls.artikkel = a.artikkel AND ls.yksus = ?) INNER JOIN ladu/lstl lstl ON lstl.artikkel = a.artikkel " +
	"	   WHERE len(ltrim(str(a.artikkel))) >= 5 AND lstl.yksus2 = ? AND lstl.kuupaev = date()" +
        " GROUP BY lstl.artikkel";

    String sql2 = "SELECT d.doknr AS arve,k.nimetus AS klient , d.kuupaev,SUM(r.realsumma) AS summa FROM  ladu/ladu d INNER JOIN yksus k ON k.yksus=d.yksus " +
   " INNER JOIN ladu/lstl r ON  r.viit=d.viit " +
   " WHERE  d.firma = ?  AND d.kuupaev > date ()-4  AND left(konto,3)='150' GROUP BY d.doknr ORDER BY d.doknr ";


    String sql3 = "SELECT d.doknr AS arve,k.nimetus AS klient , d.kuupaev,SUM(r.realsumma) AS summa FROM  ladu/ladu d INNER JOIN yksus k ON k.yksus=d.yksus " +
   " INNER JOIN ladu/lstl r ON  r.viit=d.viit " +
   " WHERE  d.firma = ?  AND d.kuupaev > date ()-3 AND left(konto,3)='150' GROUP BY d.doknr ORDER BY d.doknr ";

      StringBuffer sb = new StringBuffer();
      sb.append("\n\n\n Vormistatud tellimused:\n\n");

	try {
          connectionUp(url3);
          try {
            stmt = con.prepareStatement(sql3);
            stmt.setString(1, agent.getObject());

            ResultSet rs3 = stmt.executeQuery();
  
            while (rs3.next()) {
              sb.append("(" + rs3.getString("arve") + ") [" +
                        rs3.getString("kuupaev") + "] - " + rs3.getString("klient") + "  Summa: " + rs3.getDouble("summa") +
                        "\n");

            }
            stmt.close();
          }
          catch (Exception e) {
            getParent().showError(e);
          }
          connectionDown();



          connectionUp(url2);
                    stmt = con.prepareStatement(sql2);
                    stmt.setString(1, agent.getObject());

                    ResultSet	      rs2 = stmt.executeQuery();
                    while (rs2.next()) {
                    sb.append("(" + rs2.getString("arve") + ") [" + rs2.getString("kuupaev") + "] - " + rs2.getString("klient") + "  Summa: " + rs2.getDouble("summa") + "\n");
                    }

          stmt = con.prepareStatement(sql);
            stmt.setString(1,  agent.getObject());
            stmt.setString(2, agent.getObject());
	    ResultSet	      rs = stmt.executeQuery();
	        sb.append("Nimi  \t\t\t\tLadu\t\tM��dud\t\tSumma\n");
            sb.append("--------------------------------------------------------------------------------------\n");

            double totalKokku = 0d;

	    while (rs.next()) {
              int kogus = rs.getInt("kogus");
              double hind = round(rs.getDouble("myygihind"), 2);
              double kokku = round(kogus*hind, 2);
              totalKokku = round(totalKokku + kokku, 2);

              sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke"));

              sb.append("\t\t" + rs.getInt("ladu") + "\t\t" + kogus + "\t\t" +
                kokku + "\n\n");
 	    }

            sb.append("--------------------------------------------------------------------------------------\n");
            sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");


                    } catch (Exception e) {
                        getParent().showError(e);
                    }

                    connectionDown();





	return sb.toString();
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
      connectionUp(url3);
      double sales = 0;
      String cmd = "SELECT  SUM(realsumma) AS salary FROM ladu/ladu l " +
          "INNER JOIN ladu/lstl s ON l.viit = s.viit INNER JOIN yksus y ON y.yksus=l.yksus " +
          "WHERE ltrim(str(y.mm))=? AND month(s.kuupaev) = month(date()) AND YEAR(s.kuupaev)=YEAR(date())";
      log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);
            ResultSet	      rs = stmt.executeQuery();
        
            if(rs.next()) {
              sales = rs.getDouble("salary");
            }
            stmt.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
        return 0;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT sp.saaja AS klient,sp.doknr AS number ,sp.summa,ar.laeksumma,sp.kuupaev ,ll.makseting " +
          "FROM pank/arved a INNER JOIN pank/stlpank sp ON a.viit=sp.viitstl " +
          "INNER JOIN ladu/ladu ll ON ll.viit=sp.dokviit INNER JOIN yksus y ON y.yksus=sp.saaja " +
          "left JOIN pank/arveldus ar ON ar.viit=a.viit WHERE a.tyyp='D' " +
          "AND len(trim(a.oklipp))=0 AND ltrim(str(y.mm)) = ? ORDER BY sp.saaja ";

      connectionUp(url3);
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());

        java.util.Date dt = new java.util.Date(rs.getDate("kuupaev").getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.DAY_OF_MONTH, rs.getInt("makseting"));

        debt.setDuedate(cal.getTime());

        debt.setSum(rs.getDouble("summa") - rs.getDouble("laeksumma"));
        debt.setDate(dt);
        list.addElement(debt);
      }
      st.close();
      return list;
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
	String sqlDate = "{^" + day + '-' + month + '-'
			 + cal.get(Calendar.YEAR) + '}';

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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Ice";
    private String	       url = "jdbc:odbc:jooksev";
    private String             url2 = "jdbc:odbc:kiire";
    private String	       url3 = "jdbc:odbc:baas";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

