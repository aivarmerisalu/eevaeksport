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
import java.util.HashMap;
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
public class PragmaDBDriver extends AbstractDBDriver implements BulkUpdater {

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public PragmaDBDriver(EksportPanel parent) {
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
        	   this.connectionUp();
        	     TreeMap   tm = new TreeMap();
        	     String    sql = "select grupesid as liik,grupespav as nimi from grupes";
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

        	     
        	     connectionDown();
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
	String  cmd = "select tabelionr as yksus ,vardas + ' ' + pavarde as nimi from darbuotojai where xkred='PMOBILE' order by vardas";
	TreeMap agents = new TreeMap();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);
	    Agent agent1 = new Agent("ALL");
	    agents.put("K�ik", agent1);
	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
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

        TreeMap volad = new TreeMap();
    	TreeMap customers = new TreeMap();
    	try {
        String sql = " SELECT DISTINCTROW firmos.firmaid as klient,sum( Vvaztarastis) AS alus,sum(Su) as makstud1,sum(Gr) AS makstud2 " +
" FROM (((((SkolosAPMSel LEFT JOIN SkolosApmG ON SkolosAPMSel.VazNr = SkolosApmG.RysNr) LEFT JOIN FirmosSel " +
" ON SkolosAPMSel.KlFirma = FirmosSel.Firma) LEFT JOIN SvFirmosSel ON SkolosAPMSel.SvFirma = SvFirmosSel.Firma) " +
" LEFT JOIN StandK ON SkolosAPMSel.Tipas = StandK.Tipas) LEFT JOIN SkolosApmM_Paj ON SkolosAPMSel.VazNr = SkolosApmM_Paj.VazNr) " +
" INNER JOIN firmos on firmos.firma=SkolosAPMSel.klfirma " +
" WHERE (((SkolosAPMSel.ApmData) Between now()-365 And Now()) AND" + 
"((SkolosAPMSel.VazData)>=now()-365) AND ((SkolosAPMSel.Pastaba)<>'<<>>')" +
 " AND ((SkolosAPMSel.VazTipas)=2) AND ((StandK.Sel)=True) AND ((FirmosSel.Sel)=True) AND  " +
 "((SvFirmosSel.Sel)=True) AND ((SkolosAPMSel.Stat)='+')) OR " +
 " (((SkolosAPMSel.ApmData)<=Now()) AND ((SkolosAPMSel.Pastaba)='<<>>') AND ((SkolosAPMSel.VazTipas)=2) AND ((StandK.Sel)=True) AND " +
  " ((FirmosSel.Sel)=True) AND ((SvFirmosSel.Sel)=True) AND ((SkolosAPMSel.Stat)='+')  " +
   " AND ((SkolosAPMSel.SData)=now()-365)) group by firmos.firmaid " +
" ORDER BY max(SkolosAPMSel.ApmData)";


                      //jee
             con = DriverManager.getConnection(url);
             Statement stmt = con.createStatement();
             ResultSet	      rs = stmt.executeQuery(sql);
             while(rs.next()){
              String klient = rs.getString("klient").trim();

               double volg = rs.getDouble("alus"); 
               double tasu1 = rs.getDouble("makstud1");
               double tasu2 = rs.getDouble("makstud2");
               if(tasu1 !=0){
               	volg = volg-tasu1;
               }
               if(tasu2 !=0){
                  	volg = volg-tasu2;
                  }
               if(Math.abs(volg)> 0.02){
            	   volad.put(klient, volg +"");
                      //System.out.println("klient " +  klient + " v�lg" + volg);
               }
              
         
             }
        
        
        
        
        
        
        
        
        
        String cmd =
	    " select f.filialasid as kood, f.filialas as nimi ,f.adresas AS aadress,f.TelFax AS kontakt, k.apmterminas AS makse," +
	    " k.nuolaida as ale,k.kreditmax AS limit, k.firmaid AS maksja, " +
	    " tabelionr AS agent " +
        " from filialai f , firmos k , darbuotojai ag  " +
        " WHERE   f.firma=k.firma and ag.vardas=f.svfirma  and ag.xkred='PMOBILE' and k.INFKODAS='PMOBILE' " +
	    "      order by f.filialas  ";


	log(cmd);



	  	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
            String kood = rs.getString("kood").trim();
		Customer cust = new Customer(kood);
		String nimi = rs.getString("nimi").trim();
                cust.setName(nimi);
               // cust.setDebt(rs.getDouble("tasumata"));
                //cust.setVisit(rs.getString("visit"));
                	cust.setAddress(parent.make(rs.getString("aadress")));
                	cust.setPersonalData(parent.make(rs.getString("kontakt")).replaceAll(";", ":"));
                	cust.setPaymTerm(parent.make(rs.getString("makse")));
                    cust.setDiscount(rs.getDouble("ale"));
                
                
                double limit = rs.getDouble("limit");
                if(limit>0){
                 cust.setCredit(limit + "");	
                }
             /*
                String over = rs.getString("contrend");
                if(over.trim().length()>0){
                 cust.setCredit(over);	
                }
               */ 
                String payer = rs.getString("maksja");
                if(payer != null && payer.trim().length()>1){
                  cust.setPayer(payer.trim());
                } else {cust.setPayer(kood);}
                
                String ag = rs.getString("agent");
                if (ag != null && ag.trim().length()>0){
                	cust.addAgent(new Agent(ag.trim()));
                	cust.addAgent(new Agent("ALL"));
                }
                
                if(volad.containsKey(kood)){
                	double vv = Double.parseDouble((String)volad.get(kood));
                	cust.setDebt(vv);
                }
                
                if(volad.containsKey(payer.trim())){
                	double vv = Double.parseDouble((String)volad.get(payer.trim()));
                	cust.setDebt(vv);
                }
                
                customers.put(nimi, cust);
	    }
	} catch (Exception e) {
	    getParent().showError(e);
	}
	return customers;
    }

    public TreeMap getProducts(Agent agent){

        String cmd = "  select prekeid as kood,(preke + ' ' + nomnr ) AS nimetus, pak AS kastis,  pardkaina AS price "+
                 "  , par1 AS ean,grupe AS grupp ,matas AS unit ,pvmproc AS km from prekes where wuzs=1 " +
				 "  order by prekeid "  ;

                 String cmd2 = "select t.prekeid as toode,kiekis AS stock from ((_PrekiuLikuciai ls inner join " +
                 		" prekes t on t.nomnr=ls.nomnr) inner join sandeliai l on l.sandelys= ls.sandelis) " +
                         " where l.sandkodas in ('PMOBILE')  order by prekeid";


        if (isDebug()) System.out.println(cmd);
           if (isDebug()) System.out.println(cmd2);
        TreeMap products = new TreeMap();
        try {
       this.connectionUp();
          ResultSet rs = stmt.executeQuery(cmd);
          HashMap prodHash = new HashMap();
          while(rs.next()){
            String kood = rs.getString("kood").trim();
            Product prod = new Product(kood);
            String nimetus = rs.getString("nimetus").trim();

            prod.setInCases(rs.getDouble("kastis"));
            prod.setPrice(rs.getDouble("price"));

            String ean = rs.getString("ean");
            if(ean!=null && ean.trim().length()>0){
            prod.setEAN(ean);
          }

          String grupp = rs.getString("grupp");
           if(grupp!=null && grupp.trim().length()>0){
            prod.setGroup(grupp.trim());
            nimetus = grupp.substring(0,2)  + " " + nimetus ;
         }

          prod.setName(nimetus);

/*
         String grupp2 = rs.getString("grupp2");
          if(grupp2 != null && grupp2.trim().length()>0){
                 prod.setGroupSecond(grupp2);
              }
*/
              String unit = rs.getString("unit");
                   if(unit!=null && unit.trim().length()>0){
                    prod.setUnit(unit);
                 }

                 prod.setVat(rs.getString("km"));

            products.put(nimetus, prod);
            prodHash.put(prod.getID(), prod);

          }
          rs.close();
          ResultSet rs1 = stmt.executeQuery(cmd2);
                                while(rs1.next()){
                                  String id = rs1.getString("toode").trim();
                                  Product prod1 = (Product)prodHash.get(id);
                                  if (prod1 != null){
                                    prod1.setStock(rs1.getDouble("stock"));
                                  }
                                }

            

        } catch (Exception e){
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
	    "select f.filialasid as klient,t.prekeid as toode,max(r.kiekis) as kogus,max(vazdata) as kuup" +
	    " from vazpirkimo d, filialai f ,vazpirkprekes r,prekes t  " +
" where d.vazdata > date()-180 and d.klfilialas=f.filialas and r.pirkvazid=d.pirkvazid and t.nomnr=r.nomnr " +
" group by f.filialasid,t.prekeid,t.preke,t.grupe  order by f.filialasid,t.grupe,t.preke,max(vazdata) desc ";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("klient").trim();
		String tootekood = rs.getString("toode").trim();
		double kogus = rs.getDouble("kogus");
     String kuup = rs.getString("kuup");
		
                if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuup.substring(5,10),0,0,0," ");
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
      String sql = "SELECT DISTINCTROW  firmos.firmaid as klient, SkolosAPMSel.KLFilialas, SkolosAPMSel.VazNr AS numb, " +
      		"SkolosAPMSel.ApmData as maksepaev," +
     " SkolosAPMSel.vVaztarastis AS Summa, " +
     " date() - SkolosAPMSel.ApmData AS yle, Su as tasu1, gr AS tasu2, SkolosAPMSel.SvFirma" +
    " FROM (((((SkolosAPMSel LEFT JOIN SkolosApmG ON SkolosAPMSel.VazNr = SkolosApmG.RysNr) LEFT JOIN FirmosSel " +
    " ON SkolosAPMSel.KlFirma = FirmosSel.Firma) LEFT JOIN SvFirmosSel ON SkolosAPMSel.SvFirma = SvFirmosSel.Firma) " +
    " LEFT JOIN StandK ON SkolosAPMSel.Tipas = StandK.Tipas) LEFT JOIN SkolosApmM_Paj ON SkolosAPMSel.VazNr = SkolosApmM_Paj.VazNr)" +
    " INNER JOIN firmos on firmos.firma=SkolosAPMSel.klfirma " +
    " WHERE (((SkolosAPMSel.ApmData) Between now()-365 And Now()) AND" + 
    " ((SkolosAPMSel.VazData)>=now()-365) AND ((SkolosAPMSel.Pastaba)<>'<<>>')" +
    "  AND ((SkolosAPMSel.VazTipas)=2) AND ((StandK.Sel)=True) AND ((FirmosSel.Sel)=True) AND" + 
     " ((SvFirmosSel.Sel)=True) AND ((SkolosAPMSel.Stat)='+')) OR" +
     "  (((SkolosAPMSel.ApmData)<=Now()) AND ((SkolosAPMSel.Pastaba)='<<>>') AND ((SkolosAPMSel.VazTipas)=2)" +
     " AND ((StandK.Sel)=True) AND " +
      "  ((FirmosSel.Sel)=True) AND ((SvFirmosSel.Sel)=True) AND ((SkolosAPMSel.Stat)='+') " +
       "  AND ((SkolosAPMSel.SData)=now()-365)) " +
    " ORDER BY (SkolosAPMSel.ApmData) ";

               //jee
      con = DriverManager.getConnection(url);
      Statement stmt = con.createStatement();
      ResultSet	      rs = stmt.executeQuery(sql);
      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("numb").trim());
        debt.setDuedate(rs.getDate("maksepaev"));

        double volg = rs.getDouble("summa"); 
        debt.setDue(rs.getString("yle"));
        double tasu1 = rs.getDouble("tasu1");
        double tasu2 = rs.getDouble("tasu2");
        if(tasu1 !=0){
        	volg = volg-tasu1;
        }
        
        if(tasu2 !=0){
        	volg = volg-tasu2;
        }
        debt.setSum(volg);
       // debt.setDate(rs.getDate("kuupaev"));
    if(Math.abs(volg) >0.02){
        list.addElement(debt);	
    }
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
        String  sql = "SELECT (SUM(o.doksumma)/1.19*(y.tasu/100) + y.palk) AS palk FROM omdok o INNER " +
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

          String cmd =   " select  tabelionr AS agent , sum(d.vmedzsuma) as sales " +
 " from vazpirkimo d , darbuotojai ag  where month(d.vazdata)=month(date()) and year(d.vazdata)=year(date())  " +
  " and d.svfirma=ag.vardas group by tabelionr";
      	
      	            log(cmd);
      	          connectionUp();
          try {
              Statement stmt = con.createStatement();

              Calendar cal = Calendar.getInstance();
              cal.set(Calendar.DAY_OF_MONTH, 1);

              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("sales"));
                  //System.out.println(agent.toString() + " " + agent.getMonthSales());
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
    public static final String DRIVER_VERSION = "Pragma";
    private String	       url = "jdbc:odbc:pragma";
    private Connection	       con = null;
    private Statement	       stmt;
    private String	       subjectFilesTo = null;
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

