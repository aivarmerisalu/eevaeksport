package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Product;

/**
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0
 */
public class AvalloneKassaDBDriver extends AbstractDBDriver implements Discounter {

	/**
	 * Constructor declaration
	 * 
	 * 
	 * @param parent
	 * 
	 * @see
	 */
	public AvalloneKassaDBDriver(EksportPanel parent) {
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

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSaveStockInfo() {
		StringBuffer products = new StringBuffer();
		return products.toString();
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	/**
     *
     */
	public void connectionUp() {
		if (con == null) {
			try {
				con = DriverManager.getConnection(url);
				stmt = con.createStatement();
			} catch (Exception e) {
				getParent().showError(e);
			}
		}
	}

	public String getDistribution() {
		return null;
	}

	public TreeMap getProdGroups() throws SQLException {
		connectionUp();
		TreeMap tm = new TreeMap();
		String sql = "SELECT liik AS liik , artlnimi AS nimi  FROM artliik t order by artlnimi";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			String liik = rs.getString("liik").trim();
			String nimetus = rs.getString("nimi").trim();
			if (liik != "") {
				tm.put(liik, nimetus + " " + liik);
			}
		}
		rs.close();
		st.close();

		return tm;
	}

	/**
     *
     */
	public void connectionDown() {
		try {
			stmt.close();
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
		connectionUp();
		String cmd = "SELECT yksus,nimetus FROM yksus WHERE klassif=2";
		TreeMap agents = new TreeMap();

		try {
			ResultSet rs = stmt.executeQuery(cmd);

			while (rs.next()) {
				Agent agent = new Agent(rs.getString("yksus").trim());
				String nimi = rs.getString("nimetus").trim();
			
				agent.setName(nimi);
				agents.put(nimi, agent);
			}
			Agent aa = new Agent("KASSA");
			aa.setName("KASSA");
			agents.put("KASSA", aa);
		} catch (Exception e) {
			getParent().showError(e);
		}
		connectionDown();
		return agents;
	}

	public TreeMap getCustomers(Agent agent) {
		connectionUp();
   TreeMap customers = new TreeMap();
		Customer cus = new Customer("160");
		cus.setName("ERAKLIENT");
		customers.put("ERAKLIENT", cus);
		Customer cusr = new Customer("2825");
		cusr.setName("ERAKLIENT A");
		customers.put("ERAKLIENT A", cusr);

		return customers;
	}

	public TreeMap getProducts(Agent agent) {
		connectionUp();

		TreeMap products = new TreeMap();
		try {

			String ladu = "LADU";
			if (agent.getObject() != null
					&& agent.getObject().trim().length() > 0) {
				ladu = agent.getObject();
			}

			System.out.println("agendi pealadu :" + ladu);

			String cmd = " SELECT toode.toode, toode.nimetus, toode.tykke, toode.myygihind, taarakaal as jaehind ,toode.norm,ribakood AS ean, "
					+ " yhik,liik AS liik FROM toode  toode  WHERE  len(trim(toode.toode))>2 AND  ryhm != 'POS' and grupp='L' AND trim(seisund) !='A' and len(trim(toode.nimetus))>2 "
					+ " GROUP BY toode.toode ORDER BY toode.nimetus  ";
			// kood AS liik , nimetus AS nimi FROM artryhm t
			if (isDebug()) {
				System.out.println(cmd);
			}
			Statement sst = con.createStatement();
			ResultSet rs = sst.executeQuery(cmd);

			while (rs.next()) {
				String kood = rs.getString("toode").trim();
				String name = rs.getString("nimetus").trim();

				// if ( kood.length() > 2 ){
				Product prod = new Product(kood);
				name = name.substring(0, 1)
						+ (name.substring(1, name.length())).toLowerCase();
				prod.setName(name + " " + kood);
				prod.setInCases(rs.getDouble("tykke"));
				prod.setPrice(rs.getDouble("myygihind"));
				double jaehind = rs.getDouble("jaehind");
				if (jaehind > 0) {
					//prod.setPrice(jaehind / 1.2);
				}
				prod.setEAN(parent.make(rs.getString("ean")));
				String yh = rs.getString("yhik");

				if (yh != null && yh.trim().length() > 0) {
					prod.setUnit(yh.trim());
				}
				prod.setGroup(parent.make(rs.getString("liik")));
				products.put(name, prod);
			}
			rs.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();

		return lines;
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
		connectionUp();

		Vector custProds = new Vector();

		return custProds;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
		return list;
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
		double salary = 0;
		return salary;
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

	public String getPilotDayData(Agent agent) {
		return null;
	}

	public void setBulkSales(Map agents) {

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

	public static final String DRIVER_VERSION = "AvalloneKassa";
	private String url = "jdbc:odbc:eeva";
	private Connection con = null;
	private Statement stmt;
	private String subjectFilesTo = null;

	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}

/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

