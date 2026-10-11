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
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
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
public class Boom5DBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater, Discounter{
	
	/**
	 * Constructor declaration
	 *
	 *
	 * @param parent
	 *
	 * @see
	 */
	public Boom5DBDriver(EksportPanel parent) {
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
	
	/**
	 *
	 */
	public void connectionUp(){
	}
	public String getSaveDebtInfo() {
	   	return null;
	   }
	   
	public String getAssortmentStr(Agent agent){
		return null;
	}
	
	private Connection getNewCon() throws SQLException{
		return DriverManager.getConnection(url, "SYSDBA", "kuu2pool");
	}
	
	
	public String getDealerCustomerCSV(String agent){
		return null;
	}
	
	public String  getCustProdDiscounts(Agent agent){
		return null;
	}
	
	
	 public String getSaveStockInfo(){
		  

		  
		  String  cmd = " select artno as toode , sum(volume) as stock  from fifo l where stockno = 5 group by artno having  sum(volume) > 0 ";


		  

	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
	    	   
	    	   Connection con = this.getNewCon();

	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	           if(seis > 0){
	               products.append(kood +  ";");
	               products.append(" ;");
	               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	             }
	           }
	           rs.close();
	           st.close();
	           con.close();
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
	
	
	public TreeMap getProdGroupsSecond() throws SQLException {
		TreeMap   tm = new TreeMap();
		return tm;
	}
	
	
	public TreeMap getProdGroups() throws SQLException {
		Connection con = getNewCon();
		TreeMap   tm = new TreeMap();
		String    sql = "SELECT  g.groupno AS liik , g.groupname AS nimetus FROM artgrp g,artmain  t " +
		" WHERE t.groupno=g.groupno AND t.passive = 'F' AND g.metagroupno >0 GROUP BY g.groupname,g.metagroupno,g.groupno ";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
		
		while (rs.next()) {
			tm.put(rs.getString("liik").trim(),
					rs.getString("nimetus").trim());
		}
		rs.close();
		st.close();
		con.close();
		System.out.println("GROUPS2");
		return tm;
	}
	
	public TreeMap getAgents() {
		String  cmd = "  SELECT sellercode AS yksus,sellerno||' '|| sellername AS nimetus,1 AS ladu FROM sellers ";
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
				if (agent.getID().equals("54")) {
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
		String cmd = "SELECT custcode AS kood, custname AS nimi, address AS aadress,  " +
		"   sellerno AS agent  , CONTACTPERSON as info ,  PHONE , EMAIL , PAYPERIOD as term ,ownerno as payer "
		+ " FROM cumain WHERE (sellerno)>0  AND passive='F' AND  typeno=1 ORDER BY custname ";
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
				
				String agent1 = rs.getString("agent") ;
				
				if(agent1 != null){
					cust.addAgent(new Agent(agent1.trim())) ;
				
				
				String agent3 = "0";
				if(agent1.trim().equals("1") || agent1.trim().equals("9") || agent1.trim().equals("18")){
					agent3= "11";//Ylle Arno
					cust.addAgent(new Agent(agent3));
				}
				
				}

				cust.addAgent(new Agent("19"));
				
				String info = rs.getString("info");
				
		        if (info != null && info.trim().length()>2) {
		        	
		        	if(info.trim().startsWith(",")){
		        		info = info.trim().substring(1).trim();
		        	}
		        	
		        	if(info.trim().startsWith(",")){
		        		info = info.trim().substring(1).trim();
		        	}
		          cust.setPersonalData(info.trim());
		        }
		        
		        String PHONE = rs.getString("PHONE");
		        
                  if (PHONE != null && PHONE.trim().length()>2) {
		        	
		        	if(PHONE.trim().startsWith(",")){
		        		PHONE = PHONE.trim().substring(1).trim();
		        	}
		        	
		        	if(PHONE.trim().startsWith(",")){
		        		PHONE = PHONE.trim().substring(1).trim();
		        	}
		        	if(cust.getPersonalData().trim().length() > 0){
		        		cust.setPersonalData(cust.getPersonalData() + " Tel:" + PHONE.trim());
		        	}else{
		        	    cust.setPersonalData("Tel:" + PHONE.trim());	
		        	}
		      
		        }
				String EMAIL = rs.getString("EMAIL");
				  
                if (EMAIL != null && EMAIL.trim().length()>2) {
		        	
		        	if(EMAIL.trim().startsWith(",")){
		        		EMAIL = EMAIL.trim().substring(1).trim();
		        	}
		        	if(EMAIL.trim().startsWith(",")){
		        		EMAIL = EMAIL.trim().substring(1).trim();
		        	}
		        	if(cust.getPersonalData().trim().length() > 0){
		        		cust.setPersonalData(cust.getPersonalData() + " " + EMAIL.trim());
		        	}else{
		        	    cust.setPersonalData( EMAIL.trim());	
		        	}
		      
		        }
		        
		        
		        String term = rs.getString("term");
		        if(term != null && term.trim().length() >0){
		        	cust.setPaymTerm(term);	
		        }
				
		        String payer = rs.getString("payer");
		        if(payer != null && payer.trim().length() >0){
		        	cust.setPayer("m"+payer.trim());
		        	cust.setGroup("p"+payer.trim());
		        }
				
		        
		        
				customers.put(cust.getName(), cust);
				
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

		System.out.println(cmd);
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
		
		String cmd = "SELECT  t.artno AS toode, trim(t.artname) || ' ' || trim(t.artcode)   AS nimetus,u.salesprice/1.2 AS myygihind ,u.unitname AS unit, " + 
		 " packagevolume AS tykke,b.barcode AS ean,t.groupno AS grupp ,l.volume AS stock " +
		 " FROM artmain t,unit u ,barart b,fifo l, stgrpmap m WHERE t.artno=u.artno AND t.artno=b.artno  and m.artno=t.artno " +
		 " AND l.stockno = 5  AND t.artno=l.artno and m.stockgroupno= 1 AND t.passive='F' ORDER BY t.artname ,b.isdefault ";
		
		if (isDebug()) {
			System.out.println(cmd);
		}
		
		TreeMap products = new TreeMap();
		try {
			Connection con = getNewCon();
			Statement st = con.createStatement();
			ResultSet	      rs = st.executeQuery(cmd);
		
			while (rs.next()) {
				String kood = rs.getString("toode").trim();
				String nimetus = rs.getString("nimetus").trim();

				//String nimetus2 = rs.getString("nimetus2");
				//if (nimetus2 != null) nimetus = nimetus + " " + nimetus2.trim();
				Product prod = new Product(kood);
				prod.setName(nimetus);
				String hind = rs.getString("myygihind");
				if(hind != null){
					prod.setPrice(parseDouble(hind.trim()));	
				}
				
				
				String unit = rs.getString("unit"); 
				if (unit != null && unit.trim().length()>0){
					prod.setUnit(unit.trim() );
						
				}
				
				
				double kast = rs.getDouble("tykke");
					if (kast > 0 ) {
						prod.setInCases(kast);
					}

				
				String ean = rs.getString("ean");
				
				if (ean != null && ean.trim().length()>5) {
					if (ean.trim().substring(0,2).equals("00")){
						ean = ean.trim().substring(1,ean.length());
					}
					
					prod.setEAN(ean.trim());
				}
				double sto = 0;
				
			//	prod.setGroupSecond(rs.getString("grupp2").trim());
				prod.setGroup(rs.getString("grupp").trim());

					String srt = rs.getString("stock").trim();
					if(srt != null && srt.trim().length()>0){
						 sto = parseDouble(srt.replace(',','.'));
						prod.setStock(sto);		
				   if(products.get(nimetus) != null){
				   	Product old = (Product)products.get(nimetus);
				   	prod.setStock(prod.getStock()+old.getStock());
				   }
					products.put(nimetus, prod);
				
			}
			}
			rs.close();
			
			
		} catch (Exception e) {
		e.printStackTrace();
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
		
		String sql = " SELECT r.artno AS toode,t.custcode AS klient ,r.volume AS kogus ,d.invdate AS lastdate,max ((r.amount-r.vatamount) /r.volume) as hind FROM outitems r, outbill d, cumain t " +
		" WHERE d.invdate>current_date-250  AND r.invoiceno = d.invoiceno  AND d.custno>0 and t.custno=d.custno " +
		" GROUP BY r.artno,t.custcode, r.volume,d.invdate ORDER BY t.custcode, r.artno";
		
		if (isDebug()) {
			System.out.println(sql);
		}
		
		Vector custProds = new Vector();
		
		try {
			Connection con = getNewCon();
			PreparedStatement stmt = con.prepareStatement(sql);
			
			ResultSet	      rs = stmt.executeQuery();
			
			while (rs.next()) {
				String kliendikood = rs.getString("klient").trim();
				String tootekood = rs.getString("toode").trim();
				String lastdate =  rs.getString("lastdate").trim();
				double lastPrice = rs.getDouble("hind");
				lastdate = lastdate.substring(5,10);
				CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()) ,0 ,0,lastdate,lastPrice,0,0," ");
				custProds.add(cp);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		System.out.println("CPREADY");
		return custProds;
	}
	
	public String getPilotDayData(Agent agent){
		return null;
	}
	
	
	public String getNonMobileOrders(Agent agent){
		return null;
		/*
		 connectionUp(url);
		 
		 String sql = "SELECT trim(SL01002) AS nimi, ST02009 AS number, OR20024 AS summa FROM ST020100 " +
		 "LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001 " +
		 "LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
		 "AND LEN(trim(OR20056))=0 GROUP BY ST02009,OR20024,SL01002,SL14003";
		 
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
	
		String cmd = "  select emplno as agent, sum(amount) sales  from invoice inv  " +
				  " where extract(month from invdate)= extract(month from current_timestamp) "
				  + " AND extract(year from invdate)= extract(year from current_timestamp) " +
				  " group by emplno ";
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
	public static final String DRIVER_VERSION = "Boom5";
	//private String             url = "jdbc:odbc:scalaDB";
	private String url = "jdbc:odbc:boom";
	//"jdbc:freetds:sqlserver://NAVI-AJALUGU/TestNavi";
	private String url2 = "jdbc:odbc:report";
	private String subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();
	private TreeMap dealers = new TreeMap();
	
	
	 public Vector getPriceMatrixData() {
         Vector lines = new Vector();
         
        String sql = "  SELECT  '' as klient,  custno AS clgroup,  t.artno AS prodid,    '' AS prodgroup,0 AS discount,price/1.2 as hind " +  
                "   FROM  prices r INNER JOIN unit u on u.unitno = r.unitno INNER JOIN  pricelist l on l.pricelistno=r.pricelistno  " +
				"  INNER JOIN artmain t On t.artno=u.artno   order by custno "; //l.pricegroupno see oli grupis

         try {

                 Connection ss = this.getNewCon();
       	      Statement stmt = ss.createStatement();
                 ResultSet rs = stmt.executeQuery(sql);

                 while (rs.next()) {

                         String klient = rs.getString("klient");
                         String clGroup = rs.getString("clgroup");
                         String prodID = rs.getString("prodid");
                         String prodGroup = rs.getString("prodgroup");
                         double discount = rs.getDouble("discount");
                         double price = rs.getDouble("hind");
                         MatrixLine mline = new MatrixLine();
                     
                         if (klient != null && klient.trim().length() > 0) {
                            // mline.setCustID(klient.trim());
                               }   
                       
                         if ( clGroup != null
                                         && clGroup.trim().length() > 0) {
                                 mline.setCliGroup("p" +clGroup.trim());
                         

                         if (prodID != null && prodID.trim().length() > 0) {
                                 mline.setProdID(prodID.trim());
                         }
                         /*if (mline.getProdID() == null && prodGroup != null
                                         && prodGroup.trim().length() > 0) {
                                 mline.setProdGroup(prodGroup);
                         }*/
                        // mline.setDiscount(discount);

                         if (price != 0) {
                                 mline.setPrice(price);
                         }
                         lines.add(mline);
                         
                         }
                 }
                 
                 rs.close();
                 stmt.close();
                 ss.close();

         } catch (SQLException ex) {
         ex.printStackTrace();
         }
         System.out.println("pmatrix " + lines.size());
         
         return lines;
 }

	@Override
	public Vector getFixedAssortmentData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
}
