/*
 * Created on 17.09.2006
 */
package eevaeksport.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Date;
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class GuttaDBDriver extends AbstractDBDriver implements Manager,Promo, BulkUpdater,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public GuttaDBDriver(EksportPanel parent) {
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

    public String getProductSortingData(){
       	return null;
       }
    public Vector getFixedAssortmentData() {
      return null;
    }

   public Vector getPriceMatrixData() {
      return null;
   }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

    public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
 
        }

    public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
        String sql = "INSERT into supervisor values ("
          + "?, "   //   agent	C(50),
          + "?,"   //   agentname	C(200),
          + "?,"   //   clientid	C(50),
          + "?,"   //   clientname	C(200),
          + "?,"   //	prodid	C(50),
          + "?,"   //   prodname	C(200),agentid agentname custid custname prodid prodname
          + "date() ,"   //	sentdate	D,
         + " '' ,"   //	receivedTime	D,
          + "?,"   //normalpr	N,
          + "?,"   // amoun	N,
          + "?,"   // grouptotal	N,
          + "?,"   // cattotal	N,
          + "?,"   // pertotcat	I,sentdate normalpr amount grouptotal cattotal pertotcat
          + "?,"   // percolcat	I,
          + "?,"   // perpalcat	I,
          + "?,"   // pershecat	I,
          + "?,"   // perrescat	I,
          + "?,"   // isaction	L,
          + "?,"   // ispartner	L,
          + "?,"   // isbrand	L,
          + "?,"   // isgroup	L
          + "?,"   // actionaeva	N,percolcat perpalcat pershecat perrescat isaction ispartner isbrand isgroup actionaeva
          + "?,"   // actionbeva N,
          + "?,"   // actiontota	N,
          + "?,"   // custeval	N,
          + "?,"   // custcomme	C(200),
          + "?,"   // comment	C(200),
          + "?,"   // custagent	C(200),
          + "?,"   // custagentname	C(200),actionbeva actiontota custeval custcomme comment
          + "?,"   // mustbe	I,
          + "?,"   // ideal	I,
          + "?,"   // resstr1	C(200),
          + "?,"   // resstr2	C(200),
          + "?,"   // resstr3	C(200),
          + "?,"   // resint1	I,
          + "?,"   // resint2	I,
	      + "?)" ; //	resint3	L
        //   resstr1 resstr2 resstr3 resint1 resint2 resint3

          try {	Connection c = DriverManager.getConnection(url2);
              PreparedStatement ps = c.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
            	  eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
                ps.setString(1, wr.getInfo().getAgentID());
                ps.setString(2, wr.getInfo().getAgentName());
                ps.setString(3, lw.getLine().getCustID());
                ps.setString(4, lw.getLine().getCustName());
                ps.setString(5, lw.getLine().getProdID());
                ps.setString(6, lw.getLine().getProdName());
                ps.setDouble(7, lw.getLine().getNormalPrice()*100);
                ps.setDouble(8, lw.getLine().getAmount()*100);
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
                ps.setDouble(20, lw.getLine().getAction1Eval()*100);
                ps.setDouble(21, lw.getLine().getAction2Eval()*100);
                ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
                ps.setDouble(23, lw.getLine().getCustEval()*100);
                ps.setString(24, lw.getLine().getCustComment());
                ps.setString(25, lw.getLine().getComment());
                ps.setString(26, lw.getLine().getCustAgentId());
                ps.setString(27, lw.getLine().getCustAgentName());
                ps.setInt(28, lw.getLine().getDealNr());
                ps.setInt(29, lw.getLine().getIdealNr());
                ps.setString(30, lw.getLine().getResStr1());
                ps.setString(31, lw.getLine().getResStr2());
                ps.setString(32, lw.getLine().getResStr3());
                ps.setInt(33, lw.getLine().getResInt1());
                ps.setInt(34, lw.getLine().getResInt2());
                ps.setInt(35, lw.getLine().getResInt3());
                ps.executeUpdate();
              }

              ps.close();
          } catch (SQLException e) {
              e.printStackTrace();
          }
        }




    public String getAssortmentStr(Agent agent){
    	return null;
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
    public void connectionUp(){
    }

    private Connection getNewCon() throws SQLException{
      return DriverManager.getConnection(url, " ", " ");
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
    String    sql = "SELECT siffer AS liik , [Nimetus eesti keeles] AS nimetus FROM tvkaubad WHERE " +
                   " nivoo=2 AND aktiivne ='J' AND klass='P' " ;

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      String nimi = liik + " " + rs.getString("nimetus").trim();
        tm.put(liik,
               nimi);
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
        TreeMap agents = new TreeMap();
        try{


        FileReader fr = new FileReader("c:/mjgutta/agent.txt");
        BufferedReader reader = new BufferedReader(fr);
      String line = reader.readLine();
    //  System.out.println(line);
       while(((line = reader.readLine()).trim().length()) > 0 ){
       	StringTokenizer st = new StringTokenizer(line, ";");
        System.out.println(line);
         if (st.hasMoreTokens()) {
           String id = st.nextToken();
           String name = st.nextToken();
           Agent agent = new Agent(id);
           agent.setIsPilot(true);
           agents.put(name, agent);
         }
       }
       }catch(Exception e){
    	e.printStackTrace();
       }
	       return agents;
	    }

	    public TreeMap getCustomers(Agent agent){
	      String cmd = "SELECT siffer AS kood, [NimetusEst] AS nimi,L�him�rkus AS nimi2, aadress, Tellimispaevad AS visit," +
	                   " agent,internetiparool,nivoosiffer3 AS payer FROM tvfirmad " +
	                   "WHERE len([NimetusEst])>0 AND LEN(agent)>0  ORDER BY NimetusEst";
        log(cmd);
        Agent sukad = null;
        if(parent.getProps().getProperty("sukad") != null && parent.getProps().getProperty("sukad").length()>0){
        	 sukad = new Agent(parent.getProps().getProperty("sukad"));
        }

        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

        try {
          Connection con = getNewCon();

          Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);
            int counter = 0;
            while (rs.next()) {
              counter++;

                Customer cust = new Customer(rs.getString("kood").trim());
                String nimi = rs.getString("nimi").trim();
                String nimi2 = rs.getString("nimi2");
                if (nimi2 != null) nimi = nimi + " " + nimi2.trim();
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
                //cust.setInfo(rs.getString("info").trim());
                cust.addAgent(new Agent(rs.getString("agent").trim()));
                String agent2 = rs.getString("internetiparool");
                if(agent2 != null && agent2.trim().length()>0){
                cust.addAgent(new Agent(agent2.trim()));
                }
                cust.addAgent(new Agent("MARKO"));
                if(sukad != null){
                cust.addAgent(sukad);
                }

                String payer = rs.getString("payer");
                if  (payer != null) cust.setPayer(payer.trim());
                customers.put(cust.getName(), cust);
                custMap.put(cust.getPayer(), cust);

            }
            rs.close();

            String sql = "SELECT a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " +
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
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

        return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT a.[Maksja/Saaja] AS klient,a.number,(a.kuup�ev+a.[Viivise valem]) " +
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
      return list;
    }

    public TreeMap getProducts(Agent agent){

      String cmd = "SELECT  siffer AS toode, [Nimetus eesti keeles] AS nimetus,[Teine Kaup] AS nimetus2 , " +
         " h.hind AS myygihind ,�hik AS unit,[Lisatunnus 2] AS alus, kohamaht AS tykke,v��tkood AS ean,nivoosiffer2 AS grupp " +
          " FROM tvkaubad t,tvmhind h WHERE nivoo=4 AND aktiivne='J' AND internetikaup=1 AND " +
          "         h.hinnakiri=1 AND h.alateskuup<=date() AND h.kunikuup>=date() AND h.kaup=siffer " +
         "GROUP BY siffer,[Nimetus eesti keeles],[Teine Kaup],h.hind ,�hik,[Lisatunnus 2], kohamaht,v��tkood,nivoosiffer2 "   +
          "        ORDER BY [Nimetus eesti keeles]";

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
                String nimetus2 = rs.getString("nimetus2");
                if (nimetus2 != null) nimetus = nimetus + " " + nimetus2.trim();
                Product prod = new Product(kood);
                prod.setName(nimetus);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
                prod.setUnit(rs.getString("unit").trim() );
                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                String ean = rs.getString("ean");
                prod.setGroup(rs.getString("grupp").trim());

                       if(ean != null && ean.trim().length()>0){
                         prod.setEAN(ean.trim());
                       }

                //prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                products.put(nimetus, prod);
                prodHash.put(prod.getID(), prod);
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



      String cmd = "SELECT siffer AS yksus FROM TVFIRMAD k " +
                       "WHERE nivoo=2 AND klass = 'E' AND len(siffer)<3 AND l�him�rkus='LADU' AND left(nivoosiffer2,1)='L'";


         //System.out.println(cmd);


     DealerProduct dealerProds = new DealerProduct();

     try {
        Connection con = getNewCon();
        Statement st = con.createStatement();
        ResultSet	      rs = st.executeQuery(cmd);



        while (rs.next()) {
             String      yksus = rs.getString("yksus");
             String     sql = " SELECT LaoSiffer AS yksus,[Kaubasiffer] AS toode, SUM([SaldoPraegu]-reserveeritud)" +
                     "AS stock FROM TVLADU,TVKAUBAD WHERE siffer=kaubasiffer AND aktiivne='J' AND internetikaup=1 AND laosiffer='" + yksus + "' AND " +
                    "int(Aasta) = YEAR(date()) AND len(kaubasiffer)>0 AND len(laosiffer)<3 AND len(kaubahind)>0  "+
                                 "GROUP BY kaubasiffer,laosiffer,[Nimetus eesti keeles] ORDER BY [Nimetus eesti keeles]";


             //System.out.println(sql);
             Statement stm = con.createStatement();
             ResultSet rst = stm.executeQuery(sql);

             while (rst.next()) {
                 dealerProds.add(yksus, rst.getString("toode").trim(),rst.getString("stock")," "," " );
             }


         }
         rs.close();
         st.close();


     } catch (Exception e) {
         getParent().showError(e);
     }

     connectionDown();

     return dealerProds;
    }

    public Vector getCustomerProductsList(String agentCode) {

      String sql = "SELECT kaup AS toode,ostja AS klient ,[Kauba kogus] AS kogus FROM tvedkk r INNER JOIN tvedkp d ON r.[Keha pvoti] = d.[Keha voti] " +
 " WHERE [ED kuup�ev]>?  AND len(kaup)>0 GROUP BY kaup,ostja,[Kauba kogus] ORDER BY ostja, kaup";

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

    	String lastMonthVisit = parent.getProps().getProperty("lastreport");
    	long last = System.currentTimeMillis();
    	if(lastMonthVisit != null){
    		last = new Long(lastMonthVisit).longValue();
    	}
    	System.out.println(last + " " + new Date(last) + " " + agent.getID());
      String sql = " SELECT custid,custname , prodname,sentdate,amoun/100 AS kogus,pertotcat AS kamp , perpalcat AS hinnang, " +
      		" percolcat AS protsent,comment,custcomme " +
            " FROM supervisor WHERE len(trim(prodid))<3  and sentdate >=?  AND rtrim(custagent) = ? ORDER BY custname";


      StringBuffer sb = new StringBuffer();
      String header = "\n\n SUPERVISORI t�helepanekud:";
//System.out.println(header);
      boolean first = true;
      boolean groups = true;
      boolean groups2 = true;
      boolean cl = true;
      boolean c2 = true;
      try {
          Connection con2 = getNewCon();

                PreparedStatement stmt = con2.prepareStatement(sql);
                stmt.setDate(1, new Date(last));
                stmt.setString(2, agent.getID());
                ResultSet rs2 = stmt.executeQuery();
                String oldname =  "";
                    while (rs2.next()) {
                    	 String custID = rs2.getString("custid");
                    	if(first){
                    		String date = rs2.getString("sentdate");
                    		sb.append(header + " " + date + "\n\n");
                    		first = false;
                    	}
                    	String name = rs2.getString("custname").trim();
                    	//System.out.println("nnn"+name);
                        if(!name.equals(oldname)){
                        	cl = true;
                        	c2 =true;
                        	groups2=true;
                        }
                        //System.out.println("yyy"+oldname);
                        if(cl){
                        	sb.append("\n\n " +name + "\n");
                        	String comment2 = rs2.getString("custcomme");
                            if(comment2 != null && comment2.trim().length()>0){
                            	sb.append("\tKliendi Kommentaar: " + comment2.trim() + "\n");
                            }
                            cl = false;
                        }

                        if(c2){

                            String sql2 =  " SELECT custname, prodname,resint1,comment,custcomme " +
        		            " FROM supervisor WHERE len(trim(prodid))>3  and sentdate >= ? AND custid = ? ORDER BY custname,resint1";

                          Connection con3 = getNewCon();
                          PreparedStatement stmt2 = con3.prepareStatement(sql2);
                          stmt2.setDate(1, new Date(last));
                          stmt2.setString(2, custID);
                          ResultSet rs3 = stmt2.executeQuery();



                      	  groups = true;
                      	  boolean f1 = true;
                      	  boolean f2 = true;
                      	  while (rs3.next()) {
                            	if(groups){
                            		sb.append("\n   PUUDUVAD TOOTED & OUTofSTOCK \n");
                            		groups = false;
                            	}

                            String res = rs3.getString("resint1");


                            if(res != null && !res.trim().equals("2") && f1){
                            	sb.append("  Lisada Sortimentti :\n");
                            	f1 = false;
                            }
                            if(res != null && res.trim().equals("2") && f2){
                            	sb.append("  O.O.S. : \n" );
                            	f2 = false;
                            }
                            sb.append("\t" + rs3.getString("prodname").trim()+"\n");

                          String   comment = rs3.getString("comment");
                            if(comment != null && comment.trim().length()>0){
                            	sb.append("\tKommentaar: " + comment.trim() + "\n");
                            }
                          }
                      	  c2 = false;
                         }
                        //System.out.println("gr2 " +groups2);
                        if(groups2){
                    		sb.append("\n   TOOTEGRUPID \n");
                    		groups2 = false;
                    	}



                 sb.append("\t" + rs2.getString("prodname").trim() + "\tVP Hinnang : " + rs2.getString("hinnang") +" \n");
                  //  sb.append("\t" + rs2.getString("prodname").trim() +" \n");
                    String comment = rs2.getString("comment");
                    if(comment != null && comment.trim().length()>0){
                    	sb.append("\tKommentaar: " + comment.trim() + "\n");

                    }




                    oldname = name;

             }
        }catch (Exception e){
        	e.printStackTrace();
        }
      // System.out.println(sb.toString());
        if(sb.toString().trim().length()>0){
        return sb.toString();
       }else {
       	return null;
       }

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
      String cmd = "SELECT SUM([Kauba kogus]*m��gihind*kohamaht*(1- mahahindlus/100)) AS summa,agent  FROM tvedkk r ,tvedkp d,tvfirmad f " +
      "WHERE r.[Keha pvoti] = d.[Keha voti] AND f.siffer=d.ostja AND month([ED kuup�ev])= month(date()) " +
      " AND year([ED kuup�ev])= year(date()) AND len(agent)>0 GROUP BY agent ORDER BY agent";
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


       public TreeMap getDealers() {
           return dealers;
       }

       public void setDealers(TreeMap aDealers) {
           dealers = aDealers;
    }
    public static final String DRIVER_VERSION = "Gutta";
    private String url = "jdbc:odbc:report";
    private String             url2 = "jdbc:odbc:report";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private TreeMap	       dealers = new TreeMap();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

