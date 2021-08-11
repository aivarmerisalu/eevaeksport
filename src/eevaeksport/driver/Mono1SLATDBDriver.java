/*
 * Created on 3.09.2006
 */
package eevaeksport.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

 import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
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

 public class Mono1SLATDBDriver
     extends AbstractDBDriver implements BulkUpdater {
   public Mono1SLATDBDriver(EksportPanel parent) {
     super(parent);
     this.subjectFilesTo = parent.getString("filemsg");
     //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
     String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
     try {
       Class.forName(databaseDriverName).newInstance();
       con = DriverManager.getConnection(url);
       con2 = DriverManager.getConnection(url);
       stmt = con.createStatement();
     }
     catch (Exception e) {
       parent.showError(e);
     }
   }
   public String getSaveDebtInfo() {
   	return null;
   }
   
   public String getCustProdDiscounts(Agent agent) {
     return null;
   }

   public String getAssortmentStr(Agent agent){
   	return null;
   }
   public String getDealerCustomerCSV(String agent) {
     return null;
   }

   /**
    *
    */
   public void connectionUp() {
   }

   public TreeMap getProdGroups() throws SQLException {
     TreeMap tm = new TreeMap();
     Statement stmt1 = con2.createStatement();
     String sql = "SELECT id AS liik, descr AS nimetus FROM sc33 WHERE isfolder=1 ORDER BY descr ";
     try {
       ResultSet rs = stmt1.executeQuery(sql);
       while (rs.next()) {
         tm.put(rs.getString("liik").trim(),
                rs.getString("nimetus").trim());
       }
       rs.close();
     }
     catch (Exception e) {
       getParent().showError(e);
     }

     return tm;
   }


   public String getSaveStockInfo() {
     return null;
   }

   /**
    *
    */
   public void connectionDown() {
   }

   public TreeMap getAgents() {
     String cmd = "SELECT id,descr,sp3214 as allclients FROM SC838 ";
     if (isDebug()) System.out.println(cmd);
     TreeMap agents = new TreeMap();
     try {
       ResultSet rs = stmt.executeQuery(cmd);
       while (rs.next()) {
         Agent agent = new Agent(rs.getString("id").trim());
         String name = rs.getString("descr").trim();
         String allcl = rs.getString("allclients");
         if (!allcl.trim().equals("0")) {
           System.out.println("allcl " + allcl);
         	agent.setSednAllClients(true);
         }
         agent.setName(name);
         agents.put(name, agent);
       }
       rs.close();
     }
     catch (Exception e) {
       getParent().showError(e);
     }
     /*   String cmd2 = "SELECT  code AS yksus ,descr AS nimetus FROM sc55";
        try {

          stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery(cmd2);

          while (rs.next()) {
            Dealer dealer = new Dealer(rs.getString("yksus").trim());
            dealer.setName(rs.getString("nimetus").trim());
            addDealer(dealer);
          }
          rs.close();



        }
        catch (Exception e) {
          getParent().showError(e);
        }
      */
     return agents;
   }
   
   private String clear (String ss){
	//	System.out.println(ss);
	String ret =   ss.replace(',','.');
	ret =  ret.replace(',','.');
	ret =  ret.replace(',','.');
	ret =  ret.replace(',','.');
	ret =  ret.replace('"',' ').trim();
	ret =  ret.replace('"',' ').trim();
	ret =  ret.replace('"',' ').trim();

    ret = ret.replaceAll("�","11132xx");//
	ret = ret.replaceAll("�","11129xx");//
	ret = ret.replaceAll("�","11128xx");//
//	ret = ret.replaceAll("","11111xx");//
	ret = ret.replaceAll("�","11112xx");//
	ret = ret.replaceAll("�","11113xx");//
	ret = ret.replaceAll("�","11114xx");//
	ret = ret.replaceAll("�","11115xx");//
	ret = ret.replaceAll("�","11116xx");//
	ret = ret.replaceAll("�","11117xx");//
	ret = ret.replaceAll("�","11118xx");
	ret = ret.replaceAll("�","11119xx");//
	ret = ret.replaceAll("�","11120xx");//
	ret = ret.replaceAll("�","11121xx");//
	ret = ret.replaceAll("�","11122xx");//
	ret = ret.replaceAll("�","11123xx");//
	ret = ret.replaceAll("�","11124xx");
	//ret = ret.replaceAll("","11125xx");//
	ret = ret.replaceAll("�","11126xx");//
	ret = ret.replaceAll("�","11127xx");//
	//ret = ret.replaceAll("?","11130xx");//

	
	
	ret = ret.replaceAll("11111xx","�");
	ret = ret.replaceAll("11112xx","�");
	ret = ret.replaceAll("11113xx","�");
	ret = ret.replaceAll("11114xx","�");
	ret = ret.replaceAll("11116xx","�");
	ret = ret.replaceAll("11116xx","�");
	ret = ret.replaceAll("11117xx","�");
	ret = ret.replaceAll("11118xx","�");	
	ret = ret.replaceAll("11120xx","�");
	ret = ret.replaceAll("11121xx","�");
	ret = ret.replaceAll("11122xx","�");
	ret = ret.replaceAll("11123xx","�");
	ret = ret.replaceAll("11124xx","�");
	ret = ret.replaceAll("11125xx","�");
	ret = ret.replaceAll("11126xx","�");
	ret = ret.replaceAll("11127xx","�");
	ret = ret.replaceAll("11128xx","�");
	ret = ret.replaceAll("11129xx","�");
	ret = ret.replaceAll("11130xx","�");
	ret = ret.replaceAll("11131xx","�");
	ret = ret.replaceAll("11115xx","�");
	ret = ret.replaceAll("11119xx","�");
	ret = ret.replaceAll("11132xx","�");
	//ret = ret.replaceAll("?","�");
	
//	System.out.println(ret);
	if(ret == null || ret.trim().length()==0){
		return " ";
	}else {
		return ret;
	}
}



   public TreeMap getCustomers(Agent agent) {
     String cmd = " SELECT ab.code AS kood,kl.descr AS nimi,rtrim(ltrim(sp1653)) AS address,'' AS visit,'' AS ale  ,.F. AS blokk , " +
         " SP3212 AS toall,kl.code AS payer,ab.SP2487 AS agent ,trim(SP48)+' '+trim(SP49) AS info " +
         " FROM SC1652 ab left join sc46 kl on kl.id=ab.parentext  " +
         " WHERE  ltrim(rtrim(kl.code)) != '' and ltrim(rtrim(ab.SP2487)) != '' and ltrim(rtrim(ab.SP2487)) != '0'  ORDER BY kl.descr ASC ";
     if (isDebug()) System.out.println(cmd);

     TreeMap customers = new TreeMap();
     HashMap set = new HashMap();
     try {
       pstmt = con.prepareStatement(cmd);
       ResultSet rs = pstmt.executeQuery();
       while (rs.next()) {
         String name = rs.getString("nimi").trim();
         String accountnum = rs.getString("kood").trim();
         if(name.indexOf(".Novij Klient") != -1){
         	name = name + " " + accountnum;
         }
         agent = new Agent(rs.getString("agent").trim());
         Customer cust = new Customer(accountnum);
         
         cust.addAgent(agent);
         cust.setAddress(clear(rs.getString("address").trim()));
         cust.setName(clear(name + " " + cust.getAddress()));
         String visit = rs.getString("visit");
         if(visit!= null & visit.trim().length()>0){
           cust.setVisit(visit.trim());
         }
         String ale = rs.getString("ale");
         if (ale != null && ale.trim().length() > 0) {
           cust.setDiscount(Double.parseDouble(ale));
         }
         boolean blokk = rs.getBoolean("blokk");
         if (blokk) {
           cust.setSalesBlock(blokk);
         }

         String allagents = rs.getString("toall");
         if (allagents.trim().equals("3D")) {
           Enumeration enume = getParent().getAgentsHash().elements();
           while (enume.hasMoreElements()) {
             Agent a = (Agent) enume.nextElement();
             cust.addAgent(a);
           }
         }

         Enumeration enume = getParent().getAgentsHash().elements();
         while (enume.hasMoreElements()) {
           Agent a = (Agent) enume.nextElement();
           if(a.isSednAllClients()){
             cust.addAgent(a);
           }
         }


         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length() > 0) {
           cust.setPayer(payer.trim());
         }

         String info = rs.getString("info");
         if (info != null && info.trim().length() > 0) {
           cust.setPersonalData(info.trim());
         }
         cust.setShowfullpricelist(true);
         customers.put(cust.getName(), cust);
         set.put(cust.getID(), cust);
       }

       rs.close();
       pstmt.close();

       String sql = "SELECT k.code AS klid,d.docno AS doknr,iif(sp909>date()-1000,sp909,d.date) AS maksepaev,-sp270 AS debt," +
       		"date()-iif(sp909>date()-1000,sp909,d.date) " +
			"	  AS yle FROM RG267 alg " +
          " left JOIN 1sjourn d ON  ltrim(RIGHT(alg.sp1356,7)) = rtrim(ltrim(d.iddoc)) left JOIN DH410 info ON " +
          "ltrim(rtrim(info.iddoc))=ltrim(rtrim(d.iddoc)) " +
          "  left JOIN DT410 abi ON abi.iddoc=info.iddoc left JOIN  sc46 k ON k.id = sp413 " +
          "  WHERE MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) " +
          "  AND k.descr!='' AND sp270<>0 ORDER BY k.descr,sp909 GROUP BY d.docno ";

       String sqle =" SELECT k.descr,k.code AS klid,right(ltrim(left(sp1008,23)),9) AS doknr,SP654  as kuup,sp1753 AS maksepaev,-sp270 AS debt, " + 
		"	 date()-iif(sp1753>date()-1000,sp1753,SP654 )   AS yle " +
	"		  FROM RG267 alg " +
     "      left JOIN DH649 d ON  ltrim(RIGHT(alg.sp1356,7)) = rtrim(ltrim(d.iddoc)) " +
      "       left JOIN  sc46 k ON k.id = sp651 " +
       "     WHERE MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) " +
        "    AND k.descr!='' AND sp270<>0 ORDER BY k.descr,sp1008 GROUP BY d.sp1008 ";
       
       this.debts = new Vector();
       Calendar today = Calendar.getInstance();
       today.set(Calendar.HOUR_OF_DAY, 1);

       pstmt = con.prepareStatement(sql);
       if (isDebug()) System.out.println(sql);
    
       rs = pstmt.executeQuery();

       while (rs.next()) {
         Debt debt = new Debt();
         String klid = rs.getString("klid").trim();
         debt.setPayerID(klid);
         debt.setCustID(klid);
        
         debt.setDoknr(rs.getString("doknr").trim());
         Date dat = rs.getDate("maksepaev");
         if (dat != null) {
           debt.setDuedate(dat);
         }
         debt.setSum(rs.getDouble("debt"));
         String yle = rs.getString("yle");

         if(yle != null && yle.trim().length()>0){
           debt.setDue(yle);
         }

         Iterator iter = set.values().iterator();
         while (iter.hasNext()) {
           Customer cust = (Customer) iter.next();
           if (cust != null && cust.getPayer().equals(klid)) {
             java.util.Date maksep = debt.getDuedate();
             if (maksep != null && maksep.getTime() < today.getTimeInMillis()) {
               cust.setDebt(cust.getDebt() + debt.getSum());
             }
           }
         }

           debts.addElement(debt);
         }
       rs.close();
       pstmt.close();

       pstmt = con.prepareStatement(sqle);
       if (isDebug()) System.out.println(sqle);
    
       rs = pstmt.executeQuery();

       while (rs.next()) {
         Debt debt = new Debt();
         String klid = rs.getString("klid").trim();
         if (debt.getCustID() == null) {
           debt.setCustID(klid);
         }

         debt.setDoknr(rs.getString("doknr").trim());
         Date dat = rs.getDate("maksepaev");
         if (dat != null) {
           debt.setDuedate(dat);
         }
         debt.setSum(rs.getDouble("debt"));
         String yle = rs.getString("yle");

         if(yle != null && yle.trim().length()>0){
           debt.setDue(yle);
         }

         Iterator iter = set.values().iterator();
         while (iter.hasNext()) {
           Customer cust = (Customer) iter.next();
           if (cust != null && cust.getPayer().equals(klid)) {
             java.util.Date maksep = debt.getDuedate();
             if (maksep != null && maksep.getTime() < today.getTimeInMillis()) {
               cust.setDebt(cust.getDebt() + debt.getSum());
             }
           }
         }

           debts.addElement(debt);
         }


       
         rs.close();
         pstmt.close();


     }
     catch (Exception e) {
       getParent().showError(e);
     }
     return customers;
   }

   public Vector getDebt(String agentCode) throws SQLException {
     return this.debts;
   }

   public TreeMap getProducts(Agent agent) {

     TreeMap products = new TreeMap();

     String sql = "  SELECT pr.code AS toode, sum(sp102) AS seis  FROM sc33 pr inner JOIN rg99 alg ON   alg.sp101=pr.id  " +
        " WHERE MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) AND ltrim(sp100)='B'  order by pr.code group by pr.code ";
     try {
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);
       Hashtable tooted = new Hashtable();
       while (rs.next()) {
         String id = rs.getString("toode").trim();
         Toode toode = (Toode) tooted.get(id);
         if (toode == null) {
           toode = new Toode(id);
           tooted.put(id, toode);
         }
         toode.setKogus(toode.getKogus() + rs.getDouble("seis"));
        // toode.setKastis(rs.getDouble("kastis"));
       }
       rs.close();
       //  stmt.close();
       /*
                 a. kastis olevate uhikute arv - failis SC121 on koik uhikud. Igal toodel on kak uhikut -
                 uks on samasugune, kui uhik failist SC113 - kas SP106 voi SP1234 (need on meil uhesugused, failist SC41)
                 ja teine ongi KAST ja selles failis (SC121) SP119 ongi kogus kastis. Ja veel: failis SC121 SP118
                 on failist SC41
                 b. vaata punkt a.: uhik failist SC113 - kas SP106 voi SP1234 (need on meil uhesugused, failist SC41)
                 c. kauba hind - failis SC506 on hinnade tuubid. SP508 - failist SC504, SP517- failist  SC41. Ja hind on tegelikult failis 1sconst ID=E5
        */

       String cmd = "  SELECT pr.id,pr.code AS kood,pr.descr AS nimi,pr.sp37 AS hind,bar.code AS EAN , yh.descr AS yhik " + 
           " ,parentid AS grupp ,SP3388 as kaal FROM sc33 pr  " +
           "  left JOIN sc131 yh ON yh.id=sp74  left join SC1764 bar on bar.sp1765 = pr.id " +
           " WHERE  isfolder=2   ORDER BY pr.descr " ;
       if (isDebug()) System.out.println(cmd);
       stmt = con.createStatement();

       rs = stmt.executeQuery(cmd);
       while (rs.next()) {
         // if ( kood.length() > 2 ){
        
         String kood = rs.getString("kood").trim();
         Product prod = new Product(kood);
         String nimetus = rs.getString("nimi").trim();
         prod.setName(nimetus);
         prod.setPrice(rs.getDouble("hind"));
         String ean = rs.getString("EAN");
         if (ean != null && ean.trim().length() > 0) {
           prod.setEAN(ean.trim());
         }
         String yhik = rs.getString("yhik");
         if (yhik != null && yhik.trim().length() > 0) {
           prod.setUnit(yhik.trim());
         }
         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }
         double kaal = rs.getDouble("kaal");
         if (kaal  > 0) {
           prod.setMeasure(kaal);
         }

         
         Toode toode = (Toode) tooted.get(kood);
         double kogus = -1;
         if (toode != null) {
         
           kogus = round(toode.getKogus(), 2);
         }

         prod.setStock(kogus);
       //  prod.setInCases(kastis);
         
         prod.setVat("21");
if(!nimetus.startsWith("x")){
    products.put(nimetus, prod);
    
}
         

       }
       rs.close();
       //stmt.close();
       
       
       String  cmd2 = "select  pr.code AS toode,li.descr as kood,pr.descr AS nimi,c.value AS hind,li.descr as actnimetus " + 
    	" from sc33 pr    inner join SC2497 li  on pr.id = li.parentext inner join SC2491 ss on ss.id=li.sp2492  " +
   	" LEFT JOIN 1sconst c ON (c.objid) = (li.id)  WHERE ltrim(c.id)='1X9' " +
       " and ss.descr like '%Extra%' and left(pr.descr,1) != 'x' and date <= date()  order by pr.code,li.descr,date desc";
        StringBuffer buf = new StringBuffer();
    	try {
    		Statement stmt = con.createStatement();
    		ResultSet	      rs2 = stmt.executeQuery(cmd2);
    	

    Vector check = new Vector();
    while(rs2.next()){
        double hind = rs2.getDouble("hind");
      	String toodex = rs2.getString("toode").trim();
      	String koodx = rs2.getString("kood").trim();
     if(hind>0){
      	if(!check.contains(toodex+koodx)){
              buf.append(toodex+ ";" + koodx
              		+ ";" + rs2.getString("actnimetus").trim()+ ";" + hind);
              buf.append("\n");	
      	}
      }
    check.add(toodex+koodx);
  }	
    		
    		
    	}catch (Exception e ){
    		e.printStackTrace();
    	}
    		
    	System.out.println(buf.toString());
    	if(buf.length() > 2){
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write(buf.toString());
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }else{
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write("");
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }
        
            

       
       
       
       
       
       
     }
     catch (Exception e) {
       getParent().showError(e);
     }
     return products;
   }

   public String getDistribution() {
     return null;
   }

   public Vector getCustomerProductsList(String agentCode) {

     String cmd = " SELECT tt.code AS toode,kk.code AS klient,sp424 AS kogus,date AS lastdate,  SUM(iif(sp424>0,sp424,0)) AS Plusskogus,SUM(iif(sp424<0,sp424,0)) AS miinuskogus " +
         " FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc33 tt ON tt.id=sp423  " +
         " INNER JOIN SC1652 kk ON kk.id=sp2300  WHERE   sp909 > ? GROUP BY kk.code , tt.code,date " +
         " ORDER BY sp2300, tt.descr , date desc ";

     if (isDebug()) System.out.println(cmd);
     Vector custProds = new Vector();
     try {
    	  con = DriverManager.getConnection(url);
     PreparedStatement  pstmt = con.prepareStatement(cmd);
      // pstmt.setString(1, agentCode);
   HashSet done = new HashSet();
       Calendar cal = Calendar.getInstance();
       cal.add(Calendar.MONTH,
               -Integer.parseInt(getParent().getProps().getProperty("months")));
       pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));

       ResultSet rs = pstmt.executeQuery();
       while (rs.next()) {
         String kliendikood = rs.getString("klient").trim();
         String tootekood = rs.getString("toode").trim();
         double kogus = rs.getDouble("kogus");
         double plusskogus = rs.getDouble("plusskogus");
         double miinuskogus = rs.getDouble("miinuskogus");
         String lastDate = rs.getString("lastdate").substring(5, 10);
        String aaa = kliendikood + tootekood;
       if(!done.contains(aaa)){
    	     CustProd cp = new CustProd(kliendikood, tootekood, kogus, plusskogus,
    	    		 miinuskogus, lastDate, 0, 0, 0, " ");             
custProds.add(cp);
    	     done.add(aaa)   		    	   ;
       }
       
       }
       rs.close();
       pstmt.close();
     }
     catch (Exception e) {
       getParent().showError(e);
     }
     return custProds;
   }

   public double getSalary(String agentCode) {
     /*connectionUp();*/
     double sales = 0;
     String cmd = " SELECT (abi.sp269) AS sales,  ltrim(rtrim(sp640)) AS agent  FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc  " +
         " INNER JOIN dh272 abi ON abi.iddoc=info.iddoc INNER JOIN sc113 tt ON tt.id=sp264  " +
         " WHERE len(ltrim(pais.ismark))=0 AND YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) " +
         " GROUP BY  sp640 ";

     try {
     PreparedStatement  pstmt = con.prepareStatement(cmd);
       pstmt.setString(1, agentCode);
       ResultSet rs = pstmt.executeQuery();

       while (rs.next()) {
         sales = sales + rs.getDouble("sales");
       }
          rs.close();
       pstmt.close();
     }
     catch (Exception e) {
       getParent().showError(e);
     }

     return 0;
   }

   public DealerProduct getDealerProductsList() {
     return null;
   }

   public String getStartingTime() {
     return null;
   }

   public String getDriverVersion() {
     return "Mono1s";
   }

   public String getSubjectFilesTo() {
     return this.subjectFilesTo;
   }

   public String getPilotDayData(Agent agent) {
     return null;
   }


   public void setBulkSales(Map agents){

     String cmd = " SELECT (abi.SP428) AS sales,  ltrim(rtrim(SP2487)) AS agent  FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " + 
           " INNER JOIN dh410 abi ON abi.iddoc=info.iddoc  INNER JOIN SC1652 kk ON kk.parentext=sp413 " +
           " WHERE  YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) ORDER BY SP2487 " +
           " GROUP BY  docno";

        try {
            ResultSet	      rs = stmt.executeQuery(cmd);
            while(rs.next()) {
              String agentID = rs.getString("agent").trim();
              Agent agent = (Agent)agents.get(agentID);
              if (agent != null){
                agent.setMonthSales((int)rs.getDouble("sales"));
              //  System.out.println(agent.getMonthSales());
              }
            }
            rs.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

    }


   public TreeMap getDealers() {
     return null;
   }

   private String url = "jdbc:odbc:baaslat";
   private Connection con;
   private Connection con2;
   private Statement stmt;
   private PreparedStatement pstmt;
   private String subjectFilesTo = null;
   private Vector debts = null;
 }
