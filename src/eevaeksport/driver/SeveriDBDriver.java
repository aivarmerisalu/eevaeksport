/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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
public class SeveriDBDriver extends AbstractDBDriver {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public SeveriDBDriver(EksportPanel parent) {
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

    public String getSaveDebtInfo() {
       	return null;
       }
       
    public Vector getDebt(String agentCode){
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

                                                    public String getCustProdDiscounts(Agent agent){
                                                     return null;
                                                     }


    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }

    public TreeMap getProdGroups() throws SQLException {
      TreeMap   tm = new TreeMap();
      return tm;
    }

        public String getDistribution() {
          return null;
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
	String  cmd = "SELECT projekt AS yksus, nimetus, objekt AS objekt, paev FROM projekt" +
              " WHERE len(trim(projekt)) >= 1 ORDER BY nimetus";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();
	
	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setName(nimi);
              agent.setObject(rs.getString("objekt").trim());
              agent.setDays(rs.getString("paev").trim());
              agents.put(nimi, agent);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT y.yksus AS kood, y.nimetus AS nimi, y.yle AS tasumata," +
                     "y.aadress,y.hk_prots AS ale, y.paev AS visit FROM yksus y LEFT JOIN " +
                     "ladu/ladu l " +
                     "ON y.yksus=l.yksus LEFT JOIN ladu/projladu p ON p.viit=l.projviit where " +
                     "(p.p2 = ? OR y.p2= ? ) AND l.kuupaev <= ? group by y.yksus " +
                     "ORDER BY nimetus";
	log(cmd);

	TreeMap customers = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agent.getID());
            stmt.setDate(3, new java.sql.Date(new java.util.Date().getTime()));
            stmt.setString(2, agent.getID());
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
		cust.setDebt(rs.getDouble("tasumata"));
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());
                cust.setDiscount(rs.getDouble("ale"));
                cust.setVisit(rs.getString("visit").trim());
		customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return customers;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);
	/*
	 * String cmd = "SELECT toode.kastis, rid.toode, rid.nimetus FROM rid " +
	 * "INNER JOIN omrid ON rid.dokumnr = omrid.tasudjrk INNER JOIN toode " +
	 * "ON rid.toode = toode.toode WHERE (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND " +
	 * "omrid.reakuupaev >= " + getStartingTime() + " GROUP BY rid.toode";
	 */

	// String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
        String cmd = "SELECT a.artikkel AS toode, LEFT(grupp, 4) + ' ' + a.nimetus AS nimetus, l.sisukogus AS tykke, " +
          "l.realhind AS myygihind, ls.kogus " +
           "FROM artikkel a INNER JOIN ladu/laduart l ON a.artikkel = l.artikkel  " +
                    "LEFT JOIN ladu/laosaldo ls ON (ls.artikkel = a.artikkel ) where  " +
                    " len(trim(a.nimetus))>0 AND a.alp=.T. AND ls.yksus=? group by a.artikkel";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agent.getObject()));
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimetus").trim()
				 + " (" + rs.getString("tykke").trim()
				 + ") [" + rs.getString("kogus") + "]";

                Product prod = new Product(kood);
                prod.setName(nimetus);
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setInCases(rs.getDouble("tykke"));
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
        connectionUp(url2);
	/*
	 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
	 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
	String cmd =
            "SELECT l.yksus AS klient, s.artikkel AS toode FROM ladu/ladu l INNER JOIN ladu/lstl s ON " +
            "l.viit = s.viit INNER JOIN ladu/projladu p ON p.viit=l.projviit WHERE p.p2 = ? AND l.kuupaev >= ? GROUP BY l.yksus, s.artikkel";
	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);
            stmt.setDate(2, getStartingDate());
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String kliendikood = rs.getString("klient").trim();
		String tootekood = rs.getString("toode").trim();

		if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0," ",0,0,0," ");
                  custProds.add(cp);
		}
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }

    public String getPilotDayData(Agent agent){
      return null;
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
      connectionUp(url2);
	double sales = 0;
        String cmd = "SELECT SUM(realsumma) AS salary FROM ladu/ladu l INNER JOIN ladu/lstl s ON " +
                    "l.viit = s.viit INNER JOIN ladu/projladu p ON p.viit=l.projviit  WHERE p.p2 = ? AND month(s.kuupaev) = month(date()) AND YEAR(s.kuupaev)=YEAR(date())";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);
            ResultSet	      rs = stmt.executeQuery();

            
            if(rs.next()) {
              sales = rs.getDouble("salary");
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);
	return 0;
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
    public static final String DRIVER_VERSION = "Severi";
    private String             url = "jdbc:odbc:kiire";
    private String	       url2 = "jdbc:odbc:baas";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

