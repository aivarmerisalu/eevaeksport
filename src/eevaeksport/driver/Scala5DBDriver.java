/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
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
public class Scala5DBDriver extends AbstractDBDriver implements Manager, Promo{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public Scala5DBDriver(EksportPanel parent) {
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
       
    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }


            public String getDealerCustomerCSV(String agent){
             return null;
             }

        public String getSaveStockInfo(){
        return null;
        }


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
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

    public TreeMap getProdGroups() throws SQLException {
      connectionUp(url);
     TreeMap   tm = new TreeMap();
     String    sql = "SELECT \"Ext  Product Group\" AS liik ,\"Ext  Product Group\" AS nimetus FROM sc01  WHERE " +
                                 " \"Ext  Product Group\" not in ('AROMATISEERITUD VEIN', 'KONJAK & BR�NDI' , 'RUMM & DZHINN' ,'') " +
                                "  GROUP BY \"Ext  Product Group\" ";
     Statement st = con.createStatement();
     ResultSet rs = st.executeQuery(sql);

     while (rs.next()) {
         tm.put(rs.getString("liik").trim(),
                rs.getString("nimetus").trim());
     }
     rs.close();
     st.close();
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
        connectionUp(url);
        String  cmd = "SELECT \"Key\" AS yksus,\"Text Description\" AS nimetus FROM SY24 WHERE \"Code Type\"='BK'";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();
	  
	    
	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimetus").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agents.put(nimi, agent);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        String cmd = "SELECT \"sl01001\" AS kood, RTRIM(\"sl01002\") AS nimi2, RTRIM(\"Customer Name\") AS nimi,\"sl01003\" AS aadress,\"sl01009\" AS visit," +
                     "\"sl01036\" AS ale,((\"sl01052\")+' '+(\"sl01053\")) AS info FROM sl01 LEFT JOIN sl14 ON \"sl01001\"=\"Customer Code\" WHERE length(sl01035)>0";
        if (agent != null){
          cmd += " AND sl01035=?";
        }

        cmd += " ORDER BY sl01002";
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

	try {
            stmt = con.prepareStatement(cmd);
            if (agent != null){
              stmt.setString(1, agent.getID());
            }
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi");
                if (nimi == null){
                  nimi = "";
                }
                String nimi2 = rs.getString("nimi2");
                if (nimi2 == null){
                  nimi2 = "";
                }
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setName(nimi2 + " " + nimi);
                cust.setAddress(rs.getString("aadress").trim());
                String ale = rs.getString("ale").trim();
                if (ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale));
                }
                cust.setVisit(rs.getString("visit").trim());
                cust.setInfo(rs.getString("info").trim());
		customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();


            String sql = "SELECT \"Customer code\" AS id,\"Invoice Amount Local\" AS debt FROM sl03 WHERE " +
                         "NOT(\"Invoice Amount Local\"=\"Tot Payed LCU\") " +
                         "AND \"due date\"< ? AND LENGTH(LTRIM(\"Invoice Amount Local\"))>0";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, getDateString(new java.util.Date()));

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = rs.getString("id").trim();
              double debt = Double.parseDouble(rs.getString("debt").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.setDebt(cust.getDebt() + debt);
              }
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

	return customers;
    }

    private String getDateString(java.util.Date date){
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      String result = cal.get(Calendar.YEAR) + "";
      String month = (cal.get(Calendar.MONTH) + 1) + "";
      if (month.length() == 1) month = "0" + month;
      String day = cal.get(Calendar.DAY_OF_MONTH) + "";
      if (day.length() == 1) day = "0" + day;
      result = result + month + day;
      return result;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT \"Customer code\" AS klient, \"Invoice Number\" AS number, \"Due Date\"" +
                   "AS kuupaev,\"Invoice Amount Local\" AS summa FROM sl03 WHERE " +
                   "NOT(\"Invoice Amount Local\"=\"Tot Payed LCU\") AND LENGTH(LTRIM(\"Invoice Amount Local\"))>0 " +
                   " AND \"due date\" < ?";
      if (agentCode != null){
        sql += " AND \"Sales Person\" = ?";
      }
      sql += " ORDER BY \"Customer code\"";

      connectionUp(url);
      stmt = con.prepareStatement(sql);

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, 5);

      stmt.setString(1, getDateString(cal.getTime()));

      if (agentCode != null){
        stmt.setString(2, agentCode);
      }

      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(getDate(rs.getString("kuupaev")));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        list.addElement(debt);
      }
      rs.close();
      stmt.close();
      return list;
    }

    private static java.util.Date getDate(String date){
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
      cal.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
      cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(6, 8)));
      return cal.getTime();
    }

    private String getYesterday(){
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, -1);
      return getDateString(cal.getTime());
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
        //String cmd = "SELECT \"Stock code\" AS toode, RTRIM(\"Product Group\") AS pg1, RTRIM(\"Description 1\") AS pg2, \"Description 2\" AS nimetus" +
          //           ",\"Price local Curr\" AS myygihind,\"User Defined 1\" AS tykke,\"stock balance\" AS stock, \"User Defined 2\" AS maxale  FROM sc01 " +
            //         "WHERE LENGTH(LTRIM(\"Product Group\")) > 0 ORDER BY \"description 1\"";
        String cmd="SELECT s.\"Stock code\" AS toode,RTRIM(s.\"product group\") AS pg1,RTRIM(s.\"Description 1\") AS pg2,s.\"Description 2\" AS nimetus,s.\"Price local Curr\" AS myygihind " +
                  " ,s.\"User defined 1\" AS tykke,s.\"User Defined 2\" AS maxale, " +
                  " l.\"stock balance\" AS stock , \"Ext  Product Group\" AS grupp FROM sc01 s,sc03 l WHERE l.\"Location id\"='01' AND l.\"Stock code\"=s.\"Stock code\" AND " +
                  "  length(LTRIM(s.\"product group\"))>0 ORDER BY s.\"description 1\" " ;
	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
                String pg1 = rs.getString("pg1").trim();
                String pg2 = rs.getString("pg2").trim();
		String nimetus = pg1 + " " + pg2 + " " + rs.getString("nimetus").trim();

                Product prod = new Product(kood);
                prod.setName(nimetus);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                prod.setStock(parseDouble(rs.getString("stock").trim()));
                prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
               String grupp = rs.getString("grupp") ;
                   if(grupp != null){
                     prod.setGroup(grupp.trim());
                   }
		products.put(nimetus, prod);

	    }
            rs.close();
            st.close();
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
        connectionUp(url);


        String cmd2 = "SELECT \"Cust  Code Invoicing\" AS klient ,\"Stock Code\" AS toode, 0 AS kogus FROM " +
                      "or20 p INNER JOIN or21 r ON p.\"Order Number\"=r.\"Order Number\" WHERE " +
              " \"Order Date\" > ? AND LENGTH(\"Salesman Number\")>0";

        if (agentCode != null){
          cmd2 += " AND \"Salesman Number\"=?";
        }
        cmd2 += " GROUP BY \"Cust  Code Invoicing\",\"Stock Code\" ORDER BY \"Cust  Code Invoicing\",\"Stock Code\"," +
               "\"Order Date\"";
        if (isDebug()) {
            System.out.println(cmd2);
	}

        Vector custProds = getFromRs(agentCode, cmd2);


        String cmd = "SELECT \"Customer Code\" AS klient ,\"Stock Code\" AS toode, Qty AS kogus FROM st02 WHERE " +
              " \"Invoice Order Date\" > ? AND LENGTH(\"Salesman\")>0";

        if (agentCode != null){
          cmd += " AND \"Salesman\"=?";
        }
        cmd += " GROUP BY \"Customer Code\",\"Stock Code\", Qty ORDER BY \"Customer Code\",\"Stock Code\"," +
               "\"Invoice order date\"";

	if (isDebug()) {
	    System.out.println(cmd);
	}

        custProds.addAll(getFromRs(agentCode, cmd));

        connectionDown();
	return custProds;
    }

    private Vector getFromRs(String agentCode, String sql){
      Vector custProds = new Vector();
      try {
        stmt = con.prepareStatement(sql);
          stmt.setString(1, getStartingTime());
          if (agentCode != null){
            stmt.setString(2, agentCode);
          }
          ResultSet	      rs = stmt.executeQuery();
          while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()),0 ,0," ",0,0,0," ");
              custProds.add(cp);
          }
          rs.close();
          stmt.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
      return custProds;
    }

    public String getPilotDayData(Agent agent){
      return null;
    }

    public String getNonMobileOrders(Agent agent){
      connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      */
      String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi, " +
                   "i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON o.\"Order Number\"=" +
                   "i.\"Order Number\" INNER JOIN sl14 k ON i.\"Customer Code\"=k.\"Customer Code\" " +
                   "INNER JOIN sl01 l ON \"sl01001\"= k.\"Customer Code\" WHERE length(\"Shipment Mark\")" +
                 "=0 AND \"Salesman\"=? AND \"Order Date\"=? GROUP BY" +
      " i.\"order Number\", \"sl01002\",\"Order value\",\"Customer name\" ORDER BY i.\"order Number\"";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            String sep= System.getProperty("line.separator");
            stmt.setString(1, agent.getID());
            stmt.setString(2, getYesterday());
            ResultSet	      rs = stmt.executeQuery();
            sb.append("Nimi  \t\tNumber\tSumma" + sep);
            sb.append("-------------------------------------------" + sep);
            while(rs.next()){
              sb.append(rs.getString("nimi").trim() + "\t");
              sb.append(rs.getString("number").trim() + "\t");
              sb.append(rs.getString("summa").trim() + sep);




            }
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
      if (agentCode == null) return 0;
      connectionUp(url);
	double sales = 0;
        String cmd = "SELECT \"Price LCU\" AS hind, \"Qty\" AS kogus FROM st02 WHERE \"Salesman\" = ?" +
                     " AND \"Invoice Order Date\" >= ?";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            stmt.setString(2, getDateString(cal.getTime()));
            ResultSet	      rs = stmt.executeQuery();

            while(rs.next()) {
              sales += parseDouble(rs.getString("hind").trim()) * parseDouble(rs.getString("kogus").trim());
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

        setSales(sales);

	return 0;
    }

    public double getPromoSales(String agentCode, int nr) throws SQLException{
      String sql = "SELECT \"Qty\" AS kogus ,\"Volume\" AS sisu FROM st02 r INNER JOIN sc01 t ON r.\"Stock Code\"=t.\"Stock Code\" WHERE \"Salesman\"=? "  +
      " AND \"Invoice order date\">= ? AND \"User defined 3\" = ?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      pst.setString(2, getDateString(cal.getTime()));
      pst.setString(3, String.valueOf(nr));
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

      return total;
    }

    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT \"sl01001\" AS kood,(\"sl01002\") AS nimi,\"sl01024\" AS maksetin," +
                    "\"sl01036\" AS ale,\"sl01040\" AS aastamyyk,\"sl01041\" AS kuumyyk, " +
                    " \"Text Description\" AS agendinimi , \"sl01006\" AS kontaktisik , \"sl01011\" AS telefon FROM sl01 LEFT JOIN sy24 ON \"Key\"=\"sl01035\" ORDER BY \"sl01002\"";
      connectionUp();
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
        String tekst = ensureLen(rs.getString("tekst")," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
        if (tekst == null) tekst = " ";
        
        sb.append(tekst + sep);


        boolean has = scrollTo(cliID, rs2);
        if (has){
          sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
          sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
          sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
          sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
          String tel =  rs2.getString("telefon") ;
          tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
          sb.append(ensureLen(tel, " ") + sep);
          sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
          sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT \"Cust  Code Invoicing\" AS klient,\"Salesman Number\" AS agent,\"Our Reference\" AS " +
            "agentname FROM or20 WHERE \"Order Date\">? GROUP BY \"Cust  Code Invoicing\",\"Salesman " +
            "Number\",\"Our Reference\"";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setString(1, getStartingTime());
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname").trim();
        if (agentName == null) agentName = " ";
        bf2.append(agentName + "\n");
      }

      md.setClientAgentInfo(bf2.toString());

      return md;
    }

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
      if (str.length() == 0){
        return replace;
      } else {
        return str;
      }
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
        if (day.length() == 1) day = "0" + day;

        String monthStr = String.valueOf(month);
        if (monthStr.length() == 1) monthStr = "0" + monthStr;

	String sqlDate = String.valueOf(cal.get(Calendar.YEAR)) + monthStr + day;

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


    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Severi";
    private String             url = "jdbc:odbc:scaladb";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

