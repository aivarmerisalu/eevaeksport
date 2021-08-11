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
public class SaareDBDriver extends AbstractDBDriver {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public SaareDBDriver(EksportPanel parent) {
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
    public void connectionUp(){
      connectionUp(this.url);
    }

    public Vector getDebt(String agentCode){
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



        public String getDistribution() {
          return null;
        }


    public TreeMap getProdGroups() throws SQLException {
   connectionUp(url2);
  TreeMap   tm = new TreeMap();
  String    sql = "SELECT k.klkood AS liik , k.nimi AS nimetus FROM osmy/lm_mat a " +
      "INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood WHERE klkood !=299 AND klkood != 292 " +
      " AND klkood !=291  AND klkood !=291  AND klkood !=200  AND klkood !=286 GROUP BY k.klkood " ;

             try {
               stmt = con.prepareStatement(sql);
               ResultSet rs = stmt.executeQuery();

               while (rs.next()) {
                 tm.put(rs.getString("liik").trim(),
                        rs.getString("nimetus").trim());
               }
           } catch (Exception e) {
          getParent().showError(e);
      }
    connectionDown();
    return tm;
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
	String  cmd = "SELECT kood AS yksus,trim(enimi)+' '+ trim(pnimi)" +
                       " as nimetus, '1' AS objekt FROM osmy/isik  ORDER BY enimi";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              agent.setObject(rs.getString("objekt").trim());
              String name = rs.getString("nimetus").trim();
              agent.setName(name);
              agents.put(name, agent);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public TreeMap getCustomers(Agent agent){
        connectionUp(url2);
        String cmd = "SELECT  f.kood, f.nimi, SUM(aa.summa-aa.makstud) AS tasumata, (trim(aadr1)+' '+trim(aadr2)) AS aadress, '0' AS ale , ryhm AS visit FROM osmy/firma f " +
        "LEFT JOIN osmy/my_tell tt ON f.kood=tt.saaja  " +
        "LEFT JOIN osmy/my_arve aa ON aa.nr=tt.arvenr " +
        "WHERE vkuup < date() AND vkuup > date()-300 AND tt.osmy=? GROUP BY kood ORDER BY kood";
	log(cmd);

	TreeMap customers = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agent.getID()));
            //stmt.setDate(2, getStartingDate());
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              String name = rs.getString("nimi").trim();
              String accountnum = rs.getString("kood").trim();
              Customer cust = new Customer(accountnum);
              cust.setName(name);
              String debtStr = rs.getString("tasumata");
              if (debtStr != null){
                cust.setDebt(Double.parseDouble(debtStr));
              }

              cust.setAddress(rs.getString("aadress").trim());
              cust.setVisit(rs.getString("visit"));

              String discStr = rs.getString("ale");
              if (discStr != null){
                cust.setDiscount(Double.parseDouble(discStr));
              }

              customers.put(name, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return customers;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url2);
	/*
	 * String cmd = "SELECT toode.kastis, rid.toode, rid.nimetus FROM rid " +
	 * "INNER JOIN omrid ON rid.dokumnr = omrid.tasudjrk INNER JOIN toode " +
	 * "ON rid.toode = toode.toode WHERE (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND " +
	 * "omrid.reakuupaev >= " + getStartingTime() + " GROUP BY rid.toode";
	 */

	// String cmd = "SELECT toode, nimetus, yhik, myygihind FROM toode WHERE (len(trim(toode)) > 3) AND (trim(toode.grupp) = 'L')";
        String cmd = "SELECT a.siffer AS toode, a.nimi AS nimetus, a.karp AS tykke, " +
        "a.mghind AS myygihind, (kak+tulek-minek) AS kogus,k.klkood AS grupp FROM osmy/lm_mat a INNER JOIN osmy/lm_kla k ON a.grupp = k.klkood " +
        "LEFT JOIN ladu/ll010000 ll ON ll.siffer=a.siffer " +
        "WHERE klkood!=299 ORDER BY a.siffer";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              String kood = rs.getString("toode").trim();
              Product prod = new Product(kood);
              String nimetus = new String((rs.getString("nimetus").trim() + ' ' + kood).getBytes(), "Cp850");
              prod.setName(nimetus);
              prod.setInCases(rs.getDouble("tykke"));
              prod.setPrice(rs.getDouble("myygihind"));
              String laoseis = rs.getString("kogus");
              if (laoseis != null){
                prod.setStock(Double.parseDouble(laoseis));
              }
              prod.setGroup(rs.getString("grupp"));
              products.put(nimetus, prod);

		// }
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
	String cmd =
            "SELECT te.saaja  AS klient,pr.siffer AS toode FROM osmy/my_toode pr INNER JOIN osmy/my_tell te ON te.nr=pr.recnr WHERE te.osmy=? " +
            "AND regaeg >? " +
            "GROUP BY te.saaja,pr.siffer ORDER BY te.saaja ";
	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agentCode));
            stmt.setDate(2, getStartingDate());
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String kliendikood = rs.getString("klient").trim();
		String tootekood = rs.getString("toode").trim();
                CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0," ",0,0,0," ");
                custProds.add(cp);
	    }
            rs.close();
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
        String cmd = "SELECT SUM(summa) AS salary " +
                     "FROM osmy/my_tell te  WHERE te.osmy=? " +
                     "AND month(regaeg)=month(date()) AND year(regaeg)=year(date())";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setInt(1, Integer.parseInt(agentCode));
            ResultSet	      rs = stmt.executeQuery();

            if(rs.next()) {
              sales = rs.getDouble("salary")/1.20;

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
    public static final String DRIVER_VERSION = "Saare";
    private String             url = "jdbc:odbc:kiire";
    private String	       url2 = "jdbc:odbc:baas";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

