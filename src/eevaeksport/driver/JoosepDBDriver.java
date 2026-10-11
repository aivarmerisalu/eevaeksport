
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
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
public class JoosepDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public JoosepDBDriver(EksportPanel parent) {
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
       

    public String getSaveStockInfo(){
      connectionUp();

          StringBuffer products = new StringBuffer();

       String sql = "SELECT kuupaev as alguspaev ,sum(kogus) AS kokku, toode FROM algsa WHERE "+
                   "(kuupaev)=(select max(kuupaev) from algsa) and osak='1' GROUP BY toode ORDER BY kuupaev desc";
       if (isDebug()) {
           System.out.println(sql);
       }

       try {
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery();
         Hashtable tooted = new Hashtable();
         Date kuupaev = null;
         while(rs.next()){
           kuupaev = rs.getDate("alguspaev");
           String id = rs.getString("toode").trim();
           Toode toode = new Toode(id);
           toode.setAlguspaev(kuupaev);
           toode.setKogus(rs.getDouble("kokku"));
           tooted.put(id, toode);
         }
         rs.close();
         pstmt.close();


         sql = "select  sum(r.kogus) AS kogus,r.toode from toode t inner join rid r on t.toode=r.toode" +
               " inner join dok d on r.dokumnr=d.dokumnr where d.kuupaev >= ? " +
               "and d.doktyyp in('O','S','I') AND trim(sihtyksus)='1' group by r.toode";

         pstmt = con.prepareStatement(sql);
         pstmt.setDate(1, kuupaev);
         //pstmt.setString(2, agent.getObject());
         rs = pstmt.executeQuery();
         while(rs.next()){
           String id = rs.getString("toode").trim();
           Toode toode = (Toode)tooted.get(id);
           if (toode == null){
             toode = new Toode(id);
             tooted.put(id, toode);
           } else {
             toode.addKogus(rs.getDouble("kogus"));
           }
         }
         rs.close();
         pstmt.close();

         System.out.println("1. tooted suurus: " + tooted.size());

         sql = "select  sum(r.kogus) AS kogus,r.toode from toode t inner join rid r on t.toode=r.toode" +
               " inner join dok d on r.dokumnr=d.dokumnr where d.kuupaev >= ? " +
               " and d.doktyyp in('G','Y','K','V') AND trim(yksus)='1' group by r.toode";

         pstmt = con.prepareStatement(sql);
         pstmt.setDate(1, kuupaev);
         rs = pstmt.executeQuery();
         while(rs.next()){
           String id = rs.getString("toode").trim();
           Toode toode = (Toode)tooted.get(id);
           if (toode == null){
             toode = new Toode(id);
             tooted.put(id, toode);
           } else {
             toode.addKogus(-rs.getDouble("kogus"));
           }
         }
         rs.close();
         pstmt.close();

         sql = "select  sum(r.kogus) AS kogus,r.toode from toode t inner join rid r on t.toode=r.toode" +
                        " inner join dok d on r.dokumnr=d.dokumnr where d.kuupaev >= ? " +
                        " and d.doktyyp in ('T') AND trim(yksus)='1' AND taidetud = 0  group by r.toode";

                  pstmt = con.prepareStatement(sql);
                  pstmt.setDate(1, kuupaev);
                  rs = pstmt.executeQuery();
                  while(rs.next()){
                    String id = rs.getString("toode").trim();
                    Toode toode = (Toode)tooted.get(id);
                    if (toode == null){
                      toode = new Toode(id);
                      tooted.put(id, toode);
                    } else {
                      toode.addKogus(-rs.getDouble("kogus"));
                    }
                  }
                  rs.close();
                  pstmt.close();

        Enumeration enums = tooted.keys();
        while (enums.hasMoreElements()) {

        String  kood = (String)enums.nextElement();
                  double kogus = ((Toode)tooted.get(kood)).getKogus();
                  products.append(kood +  ";");
                  products.append(" ;");
                  products.append(getParent().getNF().format(kogus));
                  products.append("; ;");
                  products.append(" \n");
        }

          } catch (Exception e) {
              getParent().showError(e);
          }
          return products.toString();
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

    /**
     *
     */
    public void connectionUp(){
      if (con == null){
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
        TreeMap   tm = new TreeMap();
        String    sql = "SELECT grupinr AS liik , grupitxt AS nimi  FROM kgrupp  order by grupitxt";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String liik=rs.getString("liik").trim();
          String nimetus=rs.getString("nimi");
          if (liik != "" && nimetus != null
        	 ){
          tm.put(liik,liik + " " + nimetus.trim());
          }
        }
        rs.close();
        st.close();


        return tm;
    }

    /**
     *
     */
    public void connectionDown(){
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
	String  cmd = "select usernr as agent ,username as nimi from kasutaja";
	TreeMap agents = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("agent").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
		agents.put(nimi, agent);
	    }
	    
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public TreeMap getCustomers(Agent agent) {
        connectionUp();

        String cmd = " SELECT kl_kood AS kood, kl_nimi as nimi,  trim(kl_aadr1)+ ' '+kl_aadr2 AS aadress,trim(kl_isik) +' ' +kl_tel1 AS kontakt,  " +
	   " ''AS makse,0 as ale,sum(kokkusum) AS tasumata,0 AS limit, " +
	   " 0 as contrend, '' AS visit,kl_maksja as maksja,kl_myyja AS agent ,kl_regnr as reg FROM klient k left join dokument d  " +
	"	 on  kl_maksja = d.hankija and lipuke !=2 and tyyp =23 and tasupaev > date()  " +
    "    WHERE  kl_myyja>0 group by kl_kood order by kl_kood";


	log(cmd);

	TreeMap customers = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = rs.getString("nimi").trim();
                cust.setName(nimi);
                cust.setDebt(rs.getDouble("tasumata"));
                cust.setVisit(rs.getString("visit"));
                	cust.setAddress(parent.make(rs.getString("aadress")));
                	cust.setPersonalData(parent.make(rs.getString("kontakt")));
                	cust.setPaymTerm(parent.make(rs.getString("makse")));
                    cust.setDiscount(rs.getDouble("ale"));
                
                
                
                String limit = rs.getString("limit");
                if(limit.trim().length()>0){
                 cust.setCredit(limit.trim());	
                }
             
                String over = rs.getString("contrend");
                if(over.trim().length()>0){
                 cust.setCredit(over);	
                }
                
                String payer = rs.getString("maksja");
                if(payer != null && payer.trim().length()>1){
                  cust.setPayer(payer.trim());
                } else {cust.setPayer(kood);}
                
                String ag = rs.getString("agent");
                if (ag != null && ag.trim().length()>0){
                	cust.addAgent(new Agent(ag.trim()));
                }
                
                String reg = rs.getString("reg");
                if (reg != null && reg.trim().length()>0){
                	cust.setRegnr(reg.trim());
                }
                
                customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
	return customers;
    }

    public TreeMap getProducts(Agent agent) {
        connectionUp();

    	TreeMap products = new TreeMap();
    	try {
        
   
    
         

          
           String cmd = "  SELECT kkood as toode, knimi as nimetus,kogpakis as tykke, kmhind as myygihind , barcode AS ean, " +
           	 "   kyhik as yhik,kgrupp AS liik FROM kaup  WHERE kmhind>0  " +
             "    AND kgrupp!=''  ORDER BY knimi  ";

           
			if (isDebug()) {
			    System.out.println(cmd);
			}

           ResultSet  rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String name = rs.getString("nimetus").trim();

		// if ( kood.length() > 2 ){
		Product prod = new Product(kood);
		        name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
                prod.setName("    "  + name);
                String sql = "  select lkaup , sum(iif(sissevalja=0 ,lkogus,-lkogus)) as kokku from liikumin where lladu=1 " +
                		"  AND lkaup="+kood+" group by lkaup " ;
	           
              
                try{
    PreparedStatement pstm = con.prepareStatement(sql);
    ResultSet rs6 = pstm.executeQuery();
  
   if(rs6.next()){

    prod.setStock(rs6.getDouble("kokku"));
      
    }
}catch (Exception e){
 e.printStackTrace();	
}
                   
                          
                          
                prod.setInCases(rs.getDouble("tykke"));
                prod.setPrice(rs.getDouble("myygihind"));
                prod.setEAN(parent.make(rs.getString("ean")));
                if(prod.getEAN().trim().length() >4){
                	prod.setName(prod.getName()+ " " + prod.getEAN().substring(prod.getEAN().length()-4) );
                }
                String yh = rs.getString("yhik");
                
                if(yh != null && yh.trim().length()>0){
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

	String cmd =
	    "select inf.lisa_saaja as saaja,r.lkaup as toode,d.dok_kuup as kuupaev, r.lkogus AS kogus	" +
	    "FROM dokument d inner join lisainf inf on " +
 "inf.lisa_dok=d.dok_kood  INNER JOIN liikumin r   ON d.dok_kood=r.ldok_kood  " +
 "INNER JOIN klient k ON k.kl_kood = inf.lisa_saaja  where d.tyyp=23  AND  r.lmhind!=0 and d.dok_kuup >= date()-60" +
 " and r.lkaup >100 " +
 "GROUP BY inf.lisa_saaja, r.lkaup ORDER BY inf.lisa_saaja";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		String tootekood = rs.getString("toode").trim();
		 String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
     

                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),0,0,0," ");
                  custProds.add(cp);
                }
	    }

            rs.close();

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }

    public Vector getDebt(String agentCode) throws SQLException{
        Vector list = new Vector();
        String sql = "SELECT hankija as klient,dok_nr AS number,tasupaev AS maksepaev, kokkusum AS tasumata, dok_kuup AS kuupaev ,tasupaev-date() as yle FROM " +
                    " dokument WHERE  lipuke !=2 and tyyp =23 and kokkusum != 0  and tasupaev < date() ORDER BY hankija";


        con = DriverManager.getConnection(url);
        Statement stmt = con.createStatement();
        ResultSet	      rs = stmt.executeQuery(sql);

        while(rs.next()){
          Debt debt = new Debt();
          debt.setCustID(rs.getString("klient").trim());
          debt.setDoknr(rs.getString("number").trim());
          debt.setDuedate(rs.getDate("maksepaev"));

          debt.setSum(rs.getDouble("tasumata"));
          debt.setDate(rs.getDate("kuupaev"));
          debt.setDue(rs.getString("yle"));
          list.addElement(debt);
        }
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
        connectionUp();
	double  salary = 0;
        String  sql = "SELECT (SUM(o.doksumma)/1.20*(y.tasu/100) + y.palk) AS palk FROM omdok o INNER " +
                      "JOIN klient k ON k.kood=o.klient INNER JOIN yksus y ON y.yksus=k.objekt2 WHERE " +
                      "YEAR(o.kuupaev)=YEAR(date()) AND MONTH(o.kuupaev)=MONTH(date()) AND " +
                      "k.objekt2=?";

	if (isDebug()) {
	    System.out.println(sql);
	}

      try {
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, agentCode);
        ResultSet rs = st.executeQuery();
        while(rs.next()){
          salary = rs.getDouble("palk");
        }
        rs.close();
        st.close();

        sql = "SELECT SUM(rid.hind*rid.kogus*toode.tasu/100) AS palk FROM dok INNER JOIN rid ON " +
              "dok.dokumnr = rid.dokumnr INNER JOIN toode on rid.toode = toode.toode WHERE " +
              "len(trim(dok.klient)) > 1 AND len(trim(rid.toode)) > 3  AND dok.pais3obj = ? AND " +
              "YEAR(dok.kuupaev)=YEAR(date()) AND MONTH(dok.kuupaev)=MONTH(date()) AND toode.tasu>0 " +
              "AND dok.doktyyp = 'G' AND rid.doktyyp ='G' AND rid.toode not like 'SU%' GROUP BY dok.raha";
        st = con.prepareStatement(sql);
        st.setString(1, agentCode);
        rs = st.executeQuery();
        while(rs.next()){
          salary += rs.getDouble("palk");
        }
        rs.close();
        st.close();



        sql = "SELECT sum(rid.kogus*(rid.hind-rid.kaubasumma))AS sales,sum(rid.kogus*(rid.hind))AS sales,sum(rid.kogus*(rid.kaubasumma))AS" +
        		" sal,plaan, dok.pais3obj  AS haldur FROM dok " +
              "INNER JOIN rid ON dok.doktyyp+STR(dok.dokumnr,7)=rid.doktyyp+STR(rid.dokumnr,7)  INNER JOIN yksus y ON y.yksus = dok.pais3obj " + 
              " WHERE  len(trim(dok.klient)) > 1  AND dok.pais3obj=? AND YEAR(dok.kuupaev)=YEAR(date()) " +
              " AND MONTH(dok.kuupaev)=MONTH(date()) AND y.klassif=3 AND (dok.doktyyp = 'G' OR dok.doktyyp = 'Y')" +
              " AND (rid.doktyyp ='G' or rid.doktyyp ='Y') " +
			  "  AND dok.yksus='HULGI' AND len(trim(rid.toode))>2 AND rid.toode not like 'SU%' GROUP BY haldur " ;
        st = con.prepareStatement(sql);
        	st.setString(1, agentCode);
        rs = st.executeQuery();
        double sales = 0;
        double plan = 0;
        while(rs.next()){
           sales = rs.getDouble("sales");
           plan = rs.getDouble("plaan");
        }
        setSales(sales);
        setBudget(plan);
        rs.close();
        st.close();


      } catch (SQLException e){
        getParent().showError(e);
      }

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

    public String getPilotDayData(Agent agent){
      return null;
    }

    public void setBulkSales(Map agents){

          String cmd =   " SELECT dok.myyja AS agent ,SUM(kokkusum) AS sales FROM dokument dok inner join lisainf inf on " +
  " inf.lisa_dok=dok.dok_kood  WHERE   YEAR(dok.dok_kuup)=YEAR(date()) AND MONTH(dok.dok_kuup)=MONTH(date()) " +
       "   GROUP BY dok.myyja ";
      	
         
    
          
      	          log(cmd);
      	          connectionUp();
          try {
              Statement stmt = con.createStatement();

              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("sales"));
                  System.out.println(agent.toString() + " " + agent.getMonthSales());
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
     * @param s
     *
     * @see
     */
    public void log(String s) {
	getParent().log(s);
    }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Joosep";
    private String	       url = "jdbc:odbc:joosep";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

