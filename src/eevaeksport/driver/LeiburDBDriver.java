package eevaeksport.driver;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0
 */

public class LeiburDBDriver extends AbstractDBDriver implements Manager, Promo,
		BulkUpdater, Discounter {
	public LeiburDBDriver(EksportPanel parent) {
		super(parent);
		if (parent.getParentFrame().isDaySales()) {
			this.subjectFilesTo = getParent().getString("senddatamsg");
		} else {
			this.subjectFilesTo = parent.getString("filemsg");
		}
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(databaseDriverName).newInstance();
			con = DriverManager.getConnection(url, "PDA", getParent().getProps().getProperty("dsn.pass"));
			stmt = con.createStatement();
		} catch (Exception e) {
			parent.showError(e);
		}
		
		  user =  getParent().getProps().getProperty("rest.user");
		    pass = getParent().getProps().getProperty("rest.pass");
		    rcompany = getParent().getProps().getProperty("rest.company");
		    rcompanyx = getParent().getProps().getProperty("rest.companyx");
			  
	}

	 String user =  null;
	  String pass = null;
	  String rcompany = null;
	  String rcompanyx = null;
	
	
	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public Vector getLastThreeData() {

		Vector result = new Vector();
		// Siin loeme igale kliendile kylge viimase kolem korra ostud
		try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select * from kliendid_baas";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {
				String accountnum = rs.getString("kood").trim();
				
			//	paneme tuleviku siia ette 
				String sqlt = "select ItemNumber as toode ,OrderedAmount as kogus , RequestedDeliveryDate as kuup ,Tarne "
						+ " from futureOrders_view  where CustomerCode = '"
						+ accountnum + "'  order by toode";

				Connection con3 = getNewConnection();
				Statement st3 = con3.createStatement();
				ResultSet rs3 = st3.executeQuery(sqlt);
				boolean hasTom = false;
				while (rs3.next()) {
					hasTom = true;
					MatrixLine ml = new MatrixLine();
					ml.setCustID(accountnum);
					ml.setProdID(rs3.getString("toode").trim());
					ml.setPrice(rs3.getDouble("kogus"));
					ml.setStartDate(rs3.getString("kuup").substring(4).replaceAll("-", "")
							+ "-" + Converter.cleanLitLatEst(rs3.getString("Tarne")));
					ml.setCliGroup("green");
					result.add(ml);
				}
				rs3.close();
				st3.close();
				String asi ="5";
				if(hasTom){
					asi = "4";
				}

				String sq1 = "select top "+asi+" kuupaev from  tempkaive3viimast where klient ='"
						+ accountnum
						+ "' group by klient ,kuupaev  order by klient,kuupaev desc";
				Connection con2 = getNewConnection();
				Statement st2 = con2.createStatement();

				ResultSet rs2 = st2.executeQuery(sq1);
				while (rs2.next()) {
					String kuup = rs2.getInt("kuupaev")+ "";
					String sq3 = "select toode , kogus from  tempkaive3viimast where klient ='"
							+ accountnum
							+ "' and kuupaev ="
							+ kuup
							+ " order by toode";
					  con3 = getNewConnection();
					 st3 = con3.createStatement();
					 rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(accountnum);
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						ml.setStartDate(kuup.substring(4));
						result.add(ml);
					}
					rs3.close();
					st3.close();
					con3.close();
				}
				rs2.close();
				st2.close();
				con2.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
   *
   */
	public void connectionUp() {
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public void insertSuperVisorSQL(SupervisorInfoWrapper wr) {}

	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																	// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ " '' ," // receivedTime D,
				+ "?," // normalpr N,
				+ "?," // amoun N,
				+ "?," // grouptotal N,
				+ "?," // cattotal N,
				+ "?," // pertotcat I,
				+ "?," // percolcat I,
				+ "?," // perpalcat I,
				+ "?," // pershecat I,
				+ "?," // perrescat I,
				+ "?," // isaction L,
				+ "?," // ispartner L,
				+ "?," // isbrand L,
				+ "?," // isgroup L
				+ "?," // actionaeva N,
				+ "?," // actionbeva N,
				+ "?," // actiontota N,
				+ "?," // custeval N,
				+ "?," // custcomme C(200),
				+ "?," // comment C(200),
				+ "?," // custagent C(200),
				+ "?," // custagentname C(200),
				+ "?," // mustbe I,
				+ "?," // ideal I,
				+ "?," // resstr1 C(200),
				+ "?," // resstr2 C(200),
				+ "?," // resstr3 C(200),
				+ "?," // resint1 I,
				+ "?," // resint2 I,
				+ "?)"; // resint3 L

		try {
			System.out.println("jam");
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, wr.getInfo().getAgentName());
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4, lw.getLine().getCustName());
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6, lw.getLine().getProdName());
				ps.setDouble(7, lw.getLine().getNormalPrice() * 100);
				ps.setDouble(8, lw.getLine().getAmount() * 100);
				ps.setDouble(9, 0);
				ps.setDouble(10, 0);
				ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
				ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
				ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
				ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
				ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
				ps.setBoolean(16, lw.getLine().isAction());
				ps.setBoolean(17, lw.getLine().isPartner());
				ps.setBoolean(18, lw.getLine().isBrand());
				ps.setBoolean(19, lw.getLine().isGroup());
				ps.setDouble(20, lw.getLine().getAction1Eval());
				ps.setDouble(21, lw.getLine().getAction2Eval());
				ps.setDouble(22, lw.getLine().getActionTotalEval());
				ps.setDouble(23, lw.getLine().getCustEval());
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24, cc);

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25, cd);

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27, cf);
				ps.setInt(28, lw.getLine().getDealNr());
				ps.setInt(29, lw.getLine().getIdealNr());

				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
				ps.setString(30, cr1);

				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}

				Calendar cal = Calendar.getInstance();
				try {
					cal.setTimeInMillis(Long.parseLong(cr3));
				} catch (Exception e) {
				//	System.out.println("cr3 '" + cr3 + "'");

					// e.printStackTrace();

				}

				ps.setString(32, getDateStr(cal));
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				ps.executeUpdate();
				/*
				 * Connection cco = this.getNewConnection(); Statement st =
				 * cco.createStatement(); // st.executeQuery("exportsari");
				 * st.close(); cco.close();
				 */
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public String getSaveDebtInfo() {
		return null;
	}

	public String getSaveStockInfo() {

		String cmd = "laosaldo";

		if (isDebug())
			System.out.println(cmd);

		StringBuffer products = new StringBuffer();
		try {

			Connection con1 = getNewConnection();
			Statement st = con1.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
				double kogus = rs.getDouble("stock");
				String dealer = rs.getString("LADU").trim();

				products.append(kood + ";");
				products.append(" ;");
				products.append(getParent().getNF().format(kogus) + ";"
						+ dealer + "\n");
				// products.append(getParent().getNF().format(rs.getDouble("stock"))
				// + "; ; \n");
			}
			rs.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		return products.toString();
	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	/**
   *
   */
	public void connectionDown() {
	}

	public TreeMap getAgents() {

		TreeMap agents = new TreeMap();
		aHash = new HashMap();
		agents1 = new TreeMap();
		  agent1 = new TreeMap();
		 Properties propse =  new Properties();
		    try{
		    	propse =  EksportFrame.loadProps("emails.txt");
		  	  
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		try {
			Connection con1 = getNewConnection();
			Statement stmt = con1.createStatement();
			ResultSet rs2 = stmt.executeQuery("haldur_asendaja");
			while (rs2.next()) {
				String id = rs2.getString("asendatav");
				String asendaja = rs2.getString("asendaja");
				if(agents1.get(id) == null){
					agents1.put(id, asendaja);
							
				}else{
					agents1.put(id, agents1.get(id) + "," + asendaja);
					
				}
			}

			rs2.close();

			ResultSet rs = stmt.executeQuery("Select * from halduritebaas");
			while (rs.next()) {
				String kood = rs.getString("HaldurID").trim();
				Agent agent = new Agent(kood);
				String name = rs.getString("Haldur").trim();
				agent.setName(name);
				agent.setLanguage("EST");
				agent.setCountry("ee");
				agent.setPassWord("0000");
                
                String email = " ";
                
                if(propse.get(agent.getID()) != null){
              	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
                }
                
                
                if(email.length() > 3){
              	  agent.setEmail(email);
                }
                if(email.length() > 1){
              	  agent.setActive(true);
                }
				
				Iterator enums = agents1.keySet().iterator();
				while (enums.hasNext()) {
					String a = (String) enums.next();

					if (a.equals(agent.getID())) {
						StringTokenizer st = new StringTokenizer((String) agents1.get(a),",");
						while (st.hasMoreTokens()){
							agent.addDubbler(st.nextToken());	
						}
									}
				}

				agents.put(name, agent);
				aHash.put(agent.getID(), agent);
				agent1.put(agent.getID(), agent);
			}
			rs.close();
			stmt.close();
			con1.close();

		} catch (Exception e) {
			getParent().showError(e);
		}
	
		return agents;
	}

	public String getDistribution() {
		return null;
	}
	private TreeMap agent1 = new TreeMap();
	public TreeMap getCustomers(Agent agent) {
		TreeMap customers = new TreeMap();
		try{
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass, false); 
				
		}catch(Exception e){
			
		}
		  Converter ccd = new Converter();
		String cmd = "   select klient,toode,max(kuupaev)AS kuupaev,max(arvkogus) AS arvkogus, sum (arvkogus) as pluss "
				+ ", Arve  from tempklienditoode group by  klient,toode,Arve ORDER BY klient,toode ";

		if (isDebug())
			System.out.println(cmd);

		custProds = new Vector();
		HashMap kliendid = new HashMap();

		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
		
			while (rs.next()) {
				
				String kliendikood = rs.getString("klient").trim();
				String tootekood = rs.getString("toode").trim();
				String kuup = rs.getString("kuupaev");
				double kogus = rs.getDouble("arvkogus");
				double pluss = rs.getDouble("pluss");
				String doknr = rs.getString("Arve").trim();

				CustProd cp = new CustProd(kliendikood, tootekood, kogus,
						pluss, 0, kuup, 0, 0, 0, " ");
				if(doknr.trim().length() > 0){
					cp.setAbiKood(doknr);
				}
				
				custProds.add(cp);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		try {

			String cmd2 = "select * from tempkliendimyyk";
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

				String kliendikood = rs.getString("klient").trim();
				double rsumma = rs.getDouble("summa");
				double kilod = rs.getDouble("kilod");

				kliendid.put(kliendikood, rsumma + "|" + kilod);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}
TreeMap groups = new TreeMap();
		try {

			String cmd2 = "select KL_kood as klient , KL_grupp  as grupp from  [MobileJava].[dbo].[Grupid]";
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd2);

			while (rs.next()) {

				String kliendikood = rs.getString("klient").trim();
				String grupp = rs.getString("grupp").trim();
				

				groups.put(kliendikood,grupp);

			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}
		
		
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();

			String cmd2 = "select * from kliendid_baas";

			if (isDebug())
				System.out.println(cmd2);
			ResultSet rs = st.executeQuery(cmd2);
			HashMap custMap = new HashMap();

			while (rs.next()) {

				String klgrupp = rs.getString("klgrupp");
				if (klgrupp != null) {
					klgrupp = klgrupp.trim();
				} else {
					klgrupp = "";
				}
				String accountnum = rs.getString("kood").trim();
				Customer cust = new Customer(accountnum);

				if(groups.get(accountnum) != null){
					cust.addFixSortCode("ss" +Converter.enCodeEst(groups.get(accountnum)+""));
				}
				String name = rs.getString("nimi").trim();
				name = name.replaceAll("TARBIJATE ÜHISTU", "TÜ");
				name = name.replaceAll("KAUPLUS", "Kpl.");
				name = name.replaceAll("KAUBAMAJA", "KM");
				name = name.replaceAll("TULUNDUSÜHISTU", "TulÜ");
				cust.setName(Converter.enCodeEst(name));
				String aadress1 = rs.getString("aadress1");
				if (aadress1 != null) {
					aadress1 = aadress1.trim();
				} else {
					aadress1 = " ";
				}
				String aadress2 = rs.getString("aadress2");
				if (aadress2 != null) {
					aadress2 = aadress2.trim();
				} else {
					aadress2 = " ";
				}
				cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1 + " " + aadress2),"Estonia"));
				String visit = rs.getString("visit");
				if (visit != null) {
					cust.setVisit(visit.trim());
				}
				if (kliendid.get(cust.getID()) != null) {
					String sss = ((String) kliendid.get(cust.getID()));
					sss = sss.substring(0, sss.indexOf("|"));
					cust.setSold(Double.parseDouble(sss));
				}
				String info = rs.getString("info");
				if (info != null) {
					cust.setInfo(info.trim());
				}
				String ag = rs.getString("agent").trim();
				cust.addAgent(new Agent("130"));
				if (ag != null) {

					cust.addAgent(new Agent(ag));
					Agent agg = ((Agent) this.aHash.get(ag.trim()));
					if (agg != null && agg.getDubbler().size() > 0) {
						for (int i = 0; i < agg.getDubbler().size(); i++) {
							cust.addAgent(new Agent(
									(String) ((Agent) this.aHash.get(ag.trim()))
											.getDubbler().elementAt(i)));
						}
					}
					
					
				    if (ag != null && agent1.get(ag.trim()) != null){
						Agent aa = (Agent)agent1.get(ag.trim());
					
						Vector vec = aa.getDubbler();
						if(vec!= null && vec.size() >0){
							Enumeration en = vec.elements();
							while(en.hasMoreElements()){
								String age = (String) en.nextElement();
								cust.addAgent(new Agent(age));
							}
						}
					}
					

				}

				cust.setPayer(rs.getString("payer").trim());

				String maksjanimi = rs.getString("payername");
				if (maksjanimi != null && maksjanimi.trim().length() > 0) {
					maksjanimi = maksjanimi.trim();
					maksjanimi = maksjanimi.replaceAll(
							"EESTI TARBIJATE�HISTUTE KESKÜHISTU", "ETK");
					maksjanimi = maksjanimi
							.replaceAll("TARBIJATE �HISTU", "TÜ");
					maksjanimi = maksjanimi.replaceAll("MAJANDUS�HISTU", "MÜ"); // TARBIJATE
																				// KOOPERATIIV
					maksjanimi = maksjanimi.replaceAll("TARBIJATE KOOPERATIIV",
							"TK");
				}

				String blokk = rs.getString("blok2");

				if (!blokk.trim().equals("0")) {
					cust.setSalesBlock(true);
				}
				String kett = rs.getString("kett").trim();
				if (kett.equals("99")) {
					cust.setCity("99");
					
				}
				//cust.setChain(kett);

				String ketinimi = rs.getString("ketinimi");
				if (ketinimi != null && ketinimi.trim().length() > 0) {
					ketinimi = ketinimi.trim();
					if (!ketinimi.equals("Mitte kett")
							&& !ketinimi.equals("ETK")) {
						name = ketinimi.substring(0, 3) + " " + name;
						cust.setName(name);
						cust.setChain(ketinimi);
					}

					if (ketinimi.equals("ETK")) {
						name = maksjanimi + " " + name;
						cust.setName(name);
					}

				}

				String kett2 = rs.getString("kett2");
				if (kett2 != null && kett2.trim().length() > 0) {
					cust.setGroup(kett2.trim());
				}

				String sortiment = rs.getString("sortiment");
				if (sortiment != null && sortiment.trim().length() > 0
						&& !sortiment.trim().equals("1000")) {
					// cust.setGroup(sortiment.trim());
				}

				String jelp = rs.getString("asendus");
				if (jelp != null && jelp.trim().length() > 0) {
					cust.addAgent(new Agent(jelp.trim()));
				}

				String inff = rs.getString("personalinfo");
				if (inff != null && inff.trim().length() > 0) {
					cust.setPersonalData(inff.trim());
				}

				// ja kui on vaja kuvada siis paneme peale all olev on
				// ajam��rang
				//cust.setSupervisormustfill(true);
				// ccd.addQuestiosToCustomer(cust,kyssad);
				// cust.setDebt(rs.getDouble("debt"));
				customers.put(name, cust);
				custMap.put(cust.getID(), cust);
			}


			Customer cc = new Customer("YYYYY");
			cc.setName("Määramatta Klient");
			cc.setSalesBlockT(true);

			Enumeration enums = getParent().getAgentsHash().elements();
			while (enums.hasMoreElements()) {
				Agent a = (Agent) enums.nextElement();
				cc.addAgent(a);
			}

			customers.put("Määramata Klient", cc);
			custMap.put(cc.getID(), cc);

			String cmd3 = "volg ";

			if (isDebug())
				System.out.println(cmd3);
			
			
			ResultSet rs1 = st.executeQuery(cmd3);
			while (rs1.next()) {
				String id = rs1.getString("saaja").trim();
				double debt = Double.parseDouble(rs1.getString("SUMMA").trim());
				Customer cust = (Customer) custMap.get(id);
				if (cust != null) {
					cust.setDebt(cust.getDebt() + debt);
				}
			}

		} catch (SQLException e) {
			getParent().showError(e);
		}
		
		
		return customers;
	}

	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	public TreeMap getProdGroups() throws SQLException {

		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = "tootegrupp";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			tm.put(rs.getString("tootegrupp").trim(), Converter.enCodeEst(rs.getString("grupinimi").trim()));
		}
		rs.close();
		st.close();
		con.close();

		return tm;
	}

	public TreeMap getProdGroupsSecond() throws SQLException {
		TreeMap tm = new TreeMap();
		return tm;
	}

	public TreeMap getProducts(Agent agent) {

		Hashtable stocks = new Hashtable();

		// Leiame Laoseisud
		try {

			String sql = " laosaldo";

			if (isDebug())
				System.out.println(sql);
			Connection con1 = getNewConnection();
			Statement stm = con1.createStatement();
			ResultSet rst = stm.executeQuery(sql);

			while (rst.next()) {
				String toode = rst.getString("toode").trim();
				int seis = (int) Double.parseDouble(rst.getString("stock")
						.replace(',', '.'));
				String ladu = rst.getString("ladu").trim();
				if (ladu.equals("100") && seis > 0) {
					stocks.put(toode, seis + "");
				}

			}
			rst.close();
			stm.close();
			con1.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		// Leiame soodused

		String cmd = "select * from tooted_baas";

		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("kood").trim();
				Product prod = new Product(kood);
				String nimetus = rs.getString("nimetus").trim();
				try {
					if (nimetus.indexOf("(") != -1) {
						nimetus = nimetus.replace('(', '<');
					}
				} catch (Exception ew) {

				}
				try {
					if (nimetus.indexOf(")") != -1) {
						nimetus = nimetus.replace(')', '>');
					}
				} catch (Exception ew) {

				}

				prod.setInCases((rs.getDouble("tykke")));
				prod.setPrice((rs.getDouble("hind")));

				String grupp = rs.getString("grupp");
				if (grupp != null && grupp.trim().length() > 0) {
					prod.setGroup(grupp.trim());
				}

				String into = rs.getString("inunit");
				String lisa = "";
				if (into == null || into.trim().length() == 0) {
					prod.setInUnit(prod.getInCases());

				} else if (into.trim().equals("1") || into.trim().equals("4")) {
					prod.setInUnit(prod.getInCases() / 2);
					lisa = " " + (int) prod.getInCases() + "/"
							+ (int) prod.getInCases() / 2;
				} else if (into.trim().equals("3") || into.trim().equals("5")) {
					prod.setInUnit(prod.getInCases() / 4);
					lisa = " " + (int) prod.getInCases() + "/"
							+ (int) prod.getInCases() / 2;
				}

				prod.setName("    " +Converter.enCodeEst( prod.getGroup() + " " + nimetus + lisa));
			

				String ean = rs.getString("ean");
				if (ean != null && ean.trim().length() > 7) {
					prod.setEAN(ean.trim());
				}

				String unit = rs.getString("unit");

				if (unit != null && unit.trim().length() > 0) {
					unit = unit.trim();
					prod.setUnit(unit);

				}

				if (stocks.get(kood) != null) {
					prod.setStock(Double.parseDouble((String) stocks.get(kood)));
				}
  prod.setVat("22");
				products.put(prod.getName(), prod);
				// }
			}
			rs.close();
			st.close();
			con.close();

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getCustomerProductsList(String agentCode) {
		return custProds;

	}

	public void setBulkSales(Map agents) {
		  
	      int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
	      int yy = Calendar.getInstance().get(Calendar.YEAR);
	      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	      int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	     
	      
		String cmd = "select * from temphaldurmyyk";
   TreeMap<String,Toode> eesmark = new TreeMap<String,Toode>();
   TreeMap<String,Toode> eile = new TreeMap<String,Toode>();
		if (isDebug())
			System.out.println(cmd);
		try {
			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			
			String cmd2 = "select haldurID as haldur ,eesmark as plaan from eesmargid_view" +
					" where month(kuu) = month(getdate()) and year(kuu) = year(getdate())";
		try{
ResultSet rs2 = stmt.executeQuery(cmd2);
			
			while (rs2.next()) {
				String agentID = rs2.getString("haldur").trim();
				double donecur = rs2.getDouble("plaan");
				Toode tt = new Toode("");
				tt.setDonecur(donecur);
				eesmark.put(agentID, tt);
				
			}
			rs2.close();
			//stmt.close();	
		}catch (Exception e){
			e.printStackTrace();
		}
			
		
	  String   cmd3 = "select haldurid,kg,summa from eilnemyyk";
try{
	//stmt = con.createStatement();
ResultSet rs2 = stmt.executeQuery(cmd3);

while (rs2.next()) {
	String agentID = rs2.getString("haldurid").trim();
	double doneYkg = rs2.getDouble("kg");
	double doneYsum = rs2.getDouble("summa");
	Toode tt = new Toode("");
	tt.setDonecur(doneYsum);
	tt.setDonevol(doneYkg);
	eile.put(agentID, tt);	
}

 rs2.close();
 stmt.close();	
 }catch (Exception e){
   e.printStackTrace();
 }
	System.out.println("Eile "  + eile);	
		
				
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			
			while (rs.next()) {
				String agentID = rs.getString("haldur").trim();
				
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					agent.setMonthSales((int) rs.getDouble("summa"));
					agent.setKgSales((int) rs.getDouble("kilod"));
					System.out.println("agent" + agent.getName() + " "
							+ agent.getMonthSales() + " "
							+ agent.getMonthSalesVol());
					
				
					
					
						}
					}
			
			
			Iterator itera = agents.keySet().iterator();
			while (itera.hasNext()){
				String agentID = (String) itera.next();
				
				
				Agent agent = (Agent) agents.get(agentID);
				
				
				
				if(eesmark.get(agentID) != null){
					 Toode tt = eesmark.get(agentID);
					 
					 double psales = 0;
               	  
               	  try{
               		  psales = tt.getDonecur();
               	  }catch(Exception e){
               		  
               	  }
                     
               	  double  tehtudsalperc = agent.getMonthSales()/psales;
               	  double plaanitavMyykSal = d2 * agent.getMonthSales()/dd;
               	  double  prognosalperc = plaanitavMyykSal/psales;
               	  String asi = "";
                     if(dd > 2){
                   	  asi = asi + "Plaan/prognoos (�):";
                         asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                     }
                   
                     asi = asi + "M��gi eesm�rk : " +(int)psales + " �||";
					          agent.setStart(asi);
				          }	
				
				
			
				
				
				if(eile.get(agentID) != null){
					 Toode tt = eile.get(agentID);
					 
					 double psales = 0;
					 double pvol = 0;
	           	  
	           	  try{
	           		  psales = tt.getDonecur();
	           	  }catch(Exception e){
	           		  
	           	  }
	                 
	           	  try{
	           		  pvol = tt.getDonevol();
	           	  }catch(Exception e){
	           		  
	           	  }
	           	  String asi = "";
	                
	               	  asi = asi + "Eile : " +NF.format(psales) + " �   " + NF.format(pvol)  + " kg ||";  
	               
	                        agent.setStart(agent.getStart() + asi);
	                        System.out.println(agent.getStart());
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

		/*
		 * 
		 * DealerProduct dealerProds = new DealerProduct(); try {
		 * 
		 * 
		 * String sql =
		 * " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
		 * "from mvxjdta.MITBAL l WHERE MBWHLO='100' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO"
		 * ;
		 * 
		 * if (isDebug()) System.out.println(sql); Connection con1 =
		 * getNewConnection(); Statement stm = con1.createStatement(); ResultSet
		 * rst = stm.executeQuery(sql);
		 * 
		 * while (rst.next()) { String toode = rst.getString("toode").trim();
		 * int seis =
		 * (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
		 * String ladu = rst.getString("ladu").trim(); dealerProds.add(ladu,
		 * toode,seis+""," "," " );
		 * //System.out.println(ladu+" "+toode+" "+seis+""); } rst.close();
		 * stm.close(); con1.close();
		 * 
		 * } catch (Exception e) { getParent().showError(e); }
		 * 
		 * return dealerProds;
		 */
		return null;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		Vector list = new Vector();
		String sql = "volalist";

		if (isDebug())
			System.out.println(sql);
		Connection con = getNewConnection();

		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		Calendar cal = Calendar.getInstance();
		Date now = new Date(cal.getTimeInMillis());

		while (rs.next()) {
			Debt debt = new Debt();
			debt.setCustID(rs.getString("maksja").trim());
			debt.setDoknr(rs.getString("arve").trim());
			Date duedate = getDate(rs.getString("kuupaev").trim());
			debt.setDuedate(duedate);

			debt.setSum(rs.getDouble("summa"));
			debt.setDue(rs.getString("paevi"));
			if (duedate.getTime() < now.getTime()) {
				list.addElement(debt);
			}
		}
		rs.close();
		stmt.close();
		con.close();
		return list;
	}

	public ManagerData getManagerData() throws SQLException {
		ManagerData md = new ManagerData();
		return md;
	}

	public double getFoodSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getAlcoSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getPromoSales(String agentCode, int nr) throws SQLException {
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
		return "Leibur";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public String getDaySalesData(Agent agent) {
		String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa "
				+ "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO "
				+ "AND substr(UCIVDT,0,6) = '"
				+ getYearMonth()
				+ "' "
				+ "AND UCSMCD='"
				+ agent.getID()
				+ "' AND UCITTY = '01' AND M9FACI=200 GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";
		System.out.println(sql);
		StringBuffer sb = new StringBuffer();

		sb.append("\n " + getParent().getString("string5") + "\n\t"
				+ getParent().getString("string6") + "\n\n");
		sb.append(getParent().getString("string4") + "\n");
		NumberFormat nn = getNF();
		nn.setGroupingUsed(false);
		try {
			Connection con = getNewConnection();
			stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery(sql);
			while (rs2.next()) {
				sb.append(" " + rs2.getString("grupp") + "\n                 "
						+ nn.format(rs2.getDouble("summa"))
						+ " EUR\n-------------------------------------\n");
			}
			con.close();
			sb.append("\n\n" + getParent().getString("string3") + "\n\n");
			sb.append(getParent().getString("string2") + "\n\n");
		} catch (Exception E) {
		}

		return sb.toString();
	}

	public String getPilotDayData(Agent agent) {

		String sql = "SELECT t.MMspe2 AS grupp,SUM(UCSAAM) AS summa "
				+ "FROM mvxjdta.OSBSTD d ,mvxjdta.MITMAS t WHERE t.MMITNO=d.UCITNO "
				+ "AND substr(UCIVDT,0,6) = '" + getYearMonth() + "' "
				+ "AND UCSMCD='" + agent.getID()
				+ "' AND UCITTY = '01'  GROUP BY t.MMspe2 ORDER BY t.MMspe2 ";

		String sql2 = "SELECT t.MMITDS As nimetus ,dxdisp AS ale ,dxlimt AS tase FROM  mvxjdta.ogdmtx ah, mvxjdta.MITMAS t WHERE  t.MMITNO=ah.dxobv1 "
				+ "AND substr(dxfvdt,0,6) = '"
				+ getYearMonth()
				+ "' AND dxdipo=1 AND dxdisy=100 "
				+ "    AND dxdisp>0 GROUP BY t.MMITDS,dxdisp,dxlimt ";

		String sql3 = " SELECT d8obv1,t.MMITDS AS nimetus,d8disp AS ale,d8limt AS tase from mvxjdta.ogcmtx, mvxjdta.MITMAS t "
				+ "WHERE d8disy = 100 AND d8dipo=1 AND d8cono=200 AND  d8obv1=mmitno AND substr(d8fvd1,0,6) = '"
				+ getYearMonth() + "' ";

		String sql4 = "SELECT ucivno as nr,okcunm AS nimi FROM mvxjdta.OSBSTD d ,mvxjdta.OCUSMA k "
				+ " WHERE uccuno=OKCUNO AND to_char(to_date(to_char(d.UCIVDT),'YYYYMMDD')) = (to_char(sysdate)) "
				+ " AND UCSMCD='"
				+ agent.getID()
				+ "' AND UCITTY = '01' GROUP BY ucivno,okcunm ORDER BY ucivno,okcunm";

		System.out.println(sql);
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("\n " + getParent().getString("todaysbills") + "\n\n");

			Connection con4 = getNewConnection();
			stmt = con4.createStatement();
			ResultSet rs5 = stmt.executeQuery(sql4);
			while (rs5.next()) {
				sb.append(rs5.getString("nr") + " " + rs5.getString("nimi")
						+ "\n\n");
			}
			rs5.close();
			con4.close();

			sb.append("\n\n " + getParent().getString("string5") + "\n\t"
					+ getParent().getString("string6") + "\n\n");
			sb.append(getParent().getString("string4") + "\n");
			NumberFormat nn = getNF();
			nn.setGroupingUsed(false);

			Connection con = getNewConnection();
			stmt = con.createStatement();
			ResultSet rs2 = stmt.executeQuery(sql);
			while (rs2.next()) {

				sb.append(" " + rs2.getString("grupp") + "\n                 "
						+ nn.format(rs2.getDouble("summa")) +

						" EUR.\n-------------------------------------\n");
			}
		//	System.out.println(sb.toString());
			con.close();
			sb.append("\n\n" + getParent().getString("string3") + "\n\n");
			sb.append(getParent().getString("string2") + "\n\n");
			Connection con2 = getNewConnection();
			String nimetus = null;
			String ale = null;
			String kogus = null;
			Statement st = con2.createStatement();
			ResultSet rs3 = st.executeQuery(sql2);
			System.out.println(sql2);
			while (rs3.next()) {
				nimetus = rs3.getString("nimetus").trim();
				ale = rs3.getString("ale");
				kogus = rs3.getString("tase").trim();
				sb.append(nimetus + "\n");
				sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
			}

			Connection con3 = getNewConnection();
			Statement stv = con3.createStatement();
			ResultSet rs4 = st.executeQuery(sql3);
			System.out.println(sql3);
			while (rs4.next()) {
				nimetus = rs4.getString("nimetus").trim();
				ale = rs4.getString("ale");
				kogus = rs4.getString("tase").trim();
				sb.append(nimetus + "\n");
				sb.append("\t\t" + ale + " %\t" + kogus + "\n\n");
			}
			con2.close();
			st.close();
			con3.close();
			stv.close();
		} catch (Exception e) {
			getParent().showError(e);
		}

		return sb.toString();
	}

	public void insertDefective(DefectiveWrapper wr) {}

	public void insertDistribution(DistEvalListWrapper wr) {}

	public Vector getFixedAssortmentData() {
		Vector lines = new Vector();

		try {
			String cmd = " select PR_grupp as clGroup ,ProductCode as TooteKood from [MobileJava].[dbo].[Tootegrp]";

			System.out.println(cmd);

			con = getNewConnection();

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String custID = " ";
				String clGroup = " ";
				String prodID = " ";
				String prodGroup = " ";
				MatrixLine mline = new MatrixLine();

				clGroup = "ss" + rs.getString("clGroup");
				prodID = rs.getString("TooteKood");

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
				if (clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(Converter.enCodeEst(clGroup.trim()));
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				if (mline.getProdID() == null && prodGroup != null
						&& prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup);
				}

				lines.add(mline);

			}

			rs.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		//System.out.println("Mlines " + lines);
		

		return lines;
	}

	private String setZero(int i) {
		String x = i + "";
		if (x.length() == 1) {
			x = "0" + x;
		}
		return x;
	}

	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(5, 7))-1;
		int DAY = Integer.parseInt(kuup.substring(8, 10));
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}

	public String getStartingDateStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,
				-(Integer.parseInt(parent.getParentFrame().getProps()
						.getProperty("months")) + 1));
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	public String getDateStr() {
		Calendar cal = Calendar.getInstance();
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	public String getDateStr(Calendar cal) {

		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	public String get30AgoStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -30);
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}

	private String getYearMonth() {
		Calendar cal = Calendar.getInstance();
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		return cal.get(Calendar.YEAR) + "" + month;
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

	// private String url = "jdbc:oracle:thin:@10.10.10.55:1521:MVXEE001";
	private String url = "jdbc:odbc:mj_leibur";
	private Connection con;
	private Vector custProds = null;
	private Statement stmt;
	private String subjectFilesTo = null;
	private TreeMap agents1 = new TreeMap();
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	HashMap aHash = new HashMap();

	public Vector getPriceMatrixData() {

		String cmd = "select * from tooted_baas";

		if (isDebug())
			System.out.println(cmd);
		TreeMap products = new TreeMap();
		try {

			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("kood").trim();
			
				String hind = rs.getDouble("hind") + "";

				products.put(kood, hind);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Vector lines = new Vector();
		String sql = "kampaaniad";
		Vector check = new Vector();

		try {
			con = getNewConnection();

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String clGroup = rs.getString("MaksjaKood");
				String klient = rs.getString("Klient").trim();
				String algDate = rs.getString("AlgusPaev").substring(0, 10);
				String endDate = rs.getString("LoppPaev").substring(0, 10);
				String prodID = rs.getString("TooteKood");

				double discount = rs.getDouble("Allahindlus");
				double price = rs.getDouble("Hind");

				if (products.get(prodID.trim()) != null) {
					try {

						if (price == 0) {
							price = Double.parseDouble((String) products
									.get(prodID.trim()))
									* (100 - discount)
									/ 100;
							discount = 0;
						} else {
							price = price / 100;
						}

						// System.out.println("arvutan hinda " + prodID.trim() +
						// " " + price + " " +
						// (String)products.get(prodID.trim()));
					} catch (Exception a) {
						a.printStackTrace();
					}
				}

				MatrixLine mline = new MatrixLine();

				mline.setCustID(klient);
				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}

				if (endDate != null && endDate.trim().length() > 0) {
					mline.setEndDate(endDate);
				}
				if (algDate != null && algDate.trim().length() > 0) {
					mline.setStartDate(algDate);
				}

				mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				if (!check.contains(klient.trim() + prodID.trim())) {
					lines.add(mline);
				}

				check.addElement(clGroup.trim() + prodID.trim());
			}

			rs.close();
			this.connectionDown();
		} catch (SQLException ex) {
		}
		return lines;
	}

	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}
}
