package eevaeksport.driver;

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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class Mono1sDBDriver
    extends AbstractDBDriver implements BulkUpdater {
  public Mono1sDBDriver(EksportPanel parent) {
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

  public String getCustProdDiscounts(Agent agent) {
    return null;
  }

  public String getAssortmentStr(Agent agent){
  	return null;
  }
  public String getDealerCustomerCSV(String agent) {
    return null;
  }
  public String getSaveDebtInfo() {
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
    String sql = "SELECT id AS liik, descr AS nimetus FROM sc113 WHERE isfolder=1 ORDER BY descr ";
    try {
      ResultSet rs = stmt1.executeQuery(sql);
      while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
        		Converter.enCodeEst(rs.getString("nimetus").trim()));
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
    String cmd = "SELECT id,descr,SP1725 as allclients FROM sc96 where ltrim(parentid)='3QC'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        Agent agent = new Agent(rs.getString("id").trim());
        String name = rs.getString("descr").trim();
        String allcl = rs.getString("allclients");
        if (allcl.trim().equals("1")) {
       //   System.out.println("allcl " + allcl);
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

  public void setDealers(TreeMap aDealers) {
   
  }

  public TreeMap getCustomers(Agent agent) {
    String cmd = " SELECT kl.code AS kood,kl.descr AS nimi,rtrim(ltrim(sp82)) AS address,SP1724 AS visit,abi.sp726 AS ale  ,sp539 AS blokk ,SP1725 AS toall,kl.sp873 AS payer,kl.sp715 AS agent  " +
        "  FROM sc96 kl left JOIN 1sconst c ON c.objid=kl.id and ltrim(c.id)='2J' LEFT JOIN  sc724 abi ON  abi.id=c.value	" +
        "        WHERE ltrim(rtrim(kl.sp715)) !='' AND ltrim(rtrim(kl.code)) != '' AND ltrim(rtrim(kl.sp715)) != '0' AND sp1473=1 ORDER BY kl.descr ASC GROUP BY kl.descr ";
    if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    try {
      pstmt = con.prepareStatement(cmd);
    //  pstmt.setString(1, agent.getID());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        String name = rs.getString("nimi").trim();
        String accountnum = rs.getString("kood").trim();
        agent = new Agent(rs.getString("agent").trim());
        Customer cust = new Customer(accountnum);
        cust.setName(Converter.enCodeEst(name));
        cust.addAgent(agent);
        cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("address").trim()),"Estonia"));
        String visit =rs.getString("visit");
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
        if (allagents.trim().equals("1")) {
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
         //   System.out.println("added" + a.getName());
          }
        }


        String payer = rs.getString("payer");
        if (payer != null && payer.trim().length() > 0) {
          String sql1 = "SELECT code FROM sc96  " +
              " WHERE  ltrim(rtrim(id)) = '" + payer.trim() + "'";
          Statement st = con2.createStatement();
          ResultSet rs2 = st.executeQuery(sql1);
          while (rs2.next()) {
            cust.setPayer(rs2.getString("code").trim());
          }
          rs2.close();
          st.close();
        }

        customers.put(name, cust);
        set.put(cust.getID(), cust);
      }

      rs.close();
      pstmt.close();

      String sql = "SELECT k.code AS klid,d.docno AS doknr,iif(sp261>date()-1000,sp261,d.date) AS maksepaev,-sp440 AS debt,date()-iif(sp261>date()-1000,sp261,d.date) AS yle,sp1425 AS rece FROM RG441 alg " +
          "  left JOIN 1sjourn d ON  ltrim(RIGHT(alg.sp439,9)) = rtrim(ltrim(d.iddoc)) left JOIN dh272 info ON ltrim(rtrim(info.iddoc))=ltrim(rtrim(d.iddoc)) " +
          " left JOIN dt272 abi ON abi.iddoc=info.iddoc left JOIN  sc96 k ON k.id = sp437 " +
          " WHERE ((ltrim(rtrim(sp715)) != '' OR ltrim(rtrim(sp640)) != '' ) AND MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) " +
          " AND k.descr!='') AND sp440<>0 ORDER BY k.descr,sp261 GROUP BY d.docno ";

      pstmt = con.prepareStatement(sql);
      if (isDebug()) System.out.println(sql);
     // pstmt.setString(1, agent.getID());
     // pstmt.setString(2, agent.getID());
      rs = pstmt.executeQuery();

      this.debts = new Vector();
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while (rs.next()) {
        Debt debt = new Debt();
        String rece = rs.getString("rece");
        String klid = rs.getString("klid").trim();
        debt.setPayerID(klid);
        if (rece != null && rece.trim().length() > 0) {
          String sql1 = "SELECT code FROM sc96  " +
              " WHERE  ltrim(rtrim(id)) = '" + rece.trim() + "'";
          Statement st = con2.createStatement();
          ResultSet rs2 = st.executeQuery(sql1);
          while (rs2.next()) {
           // debt.setCustID(rs2.getString("code").trim());
          }
          rs2.close();
          st.close();
        }
        if (debt.getCustID() == null) {
          debt.setCustID(klid);
        }
  String doknr = rs.getString("doknr");
  if(doknr != null) {
        debt.setDoknr(doknr.trim());
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
    Vector active = new Vector();

    String sql = "     SELECT pr.code AS toode, sum(sp418) AS seis,sp1476 AS kastis  FROM sc113 pr inner JOIN rg426 alg ON alg.sp416=pr.id LEFT JOIN sc121 yh ON yh.parentext=pr.id " +
        " WHERE MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) AND   sp417 like '%7' order by pr.code group by pr.code ";
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
        toode.setKastis(rs.getDouble("kastis"));
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

      String cmd = "  SELECT  (date),SP1463 as ok,pr.id,pr.code AS kood,pr.descr AS nimi,c.value AS hind,sp510 AS EAN , yh.descr AS yhik  " +
           " ,parentid AS grupp ,SP2010 as kaal FROM sc113 pr  " +
           " INNER JOIN sc41 yh ON yh.id=sp106 INNER JOIN SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) " +
		   " inner join sc504 abi on abi.id=hi.sp508 " +
           " WHERE ltrim(c.id)='E5' AND isfolder=2 AND ltrim(c.value)!='0.000' and ltrim(sp508)='2LG' ORDER BY pr.descr,date  ";

      if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        // if ( kood.length() > 2 ){
        int ok = rs.getInt("ok");
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi").trim();
        prod.setName(Converter.enCodeEst(nimetus));
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
        double kastis = 0;
        if (toode != null) {
        
          kogus = round(toode.getKogus(), 2);
          kastis = round(toode.getKastis(), 2);
        }

        prod.setStock(kogus);
        prod.setInCases(kastis);
        prod.setVat("22");

        if (ok == 0){
          products.put(nimetus, prod);
          active.addElement(prod.getID());
        } else if (kogus > 0) {
          products.put(nimetus, prod);
          active.addElement(prod.getID());
        }

      }
      rs.close();
      //stmt.close();
    }
    catch (Exception e) {
      getParent().showError(e);
    }
    
	
	String  cmd = "select pr.code AS toode,li.code as kood,pr.descr AS nimi,c.value AS hind,li.descr as actnimetus " +
		"	from sc113 pr left join  SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) inner join SC504 li " +
	"		on li.id = sp508  inner join Dh1070 ae on c.docid=ae.iddoc WHERE ltrim(c.id)='E5'  " +
"			and li.descr like '%Extra%'  and date <= date()  order by pr.code,li.descr,date desc ";
	
    StringBuffer buf = new StringBuffer();
	try {
		Statement stmt = con.createStatement();
		ResultSet	      rs = stmt.executeQuery(cmd);
	

		 Vector check = new Vector();
           while(rs.next()){
        	        double hind = rs.getDouble("hind");
                  	String toodex = rs.getString("toode").trim();
                  	String koodx = rs.getString("kood").trim();
                 if(hind>0 && active.contains(toodex)){
                  	if(!check.contains(toodex+koodx)){
                          buf.append(toodex+ ";" + koodx
                          		+ ";" + rs.getString("actnimetus").trim()+ ";" + hind);
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
    
    
    return products;
  }

  public String getDistribution() {
    return null;
  }

  public Vector getCustomerProductsList(String agentCode) {

    String cmd = " SELECT tt.code AS toode,kk.code AS klient,sp265 AS kogus,date AS lastdate,  SUM(iif(sp265>0,sp265,0)) AS Plusskogus," +
    		"SUM(iif(sp265<0,sp265,0)) AS miinuskogus , max(dtos(date)+'|' + ltrim(str(sp265)) ) as abi" +
        " FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc INNER JOIN dh272 abi ON abi.iddoc=info.iddoc INNER JOIN sc113 tt ON tt.id=sp264 " +
        " INNER JOIN sc96 kk ON kk.id=sp1425  WHERE ltrim(rtrim(sp715)) != '' AND sp261 > ?  GROUP BY sp1425, tt.descr ORDER BY sp1425, tt.descr ";

    if (isDebug()) System.out.println(cmd);
    Vector custProds = new Vector();
    try {
    	Connection con = DriverManager.getConnection(url);
     PreparedStatement pstmt = con.prepareStatement(cmd);
     // pstmt.setString(1, agentCode);

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
    //    String lastDate = rs.getString("lastdate").substring(5, 10);
        String abi  = rs.getString("abi").trim();
        String lastDate = abi.substring(0, 8);

        try{
        	kogus = Double.parseDouble(abi.substring(9));
        }catch(Exception e){
        	System.out.println(abi.substring(9));
        	e.printStackTrace();
        }
        CustProd cp = new CustProd(kliendikood, tootekood, kogus, plusskogus,
                                   miinuskogus, lastDate, 0, 0, 0, " ");
        custProds.add(cp);
      }
      rs.close();
      pstmt.close();
      con.close();
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
      pstmt = con.prepareStatement(cmd);
      pstmt.setString(1, agentCode);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        sales = sales + rs.getDouble("sales");
      }
    //  setSales(sales);
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

    String cmd = " SELECT sum(info.sp269) AS sales,  ltrim(rtrim(sp640)) AS agent  FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +  
        " INNER JOIN dh272 abi ON abi.iddoc=info.iddoc   " +
        " WHERE len(ltrim(pais.ismark))=0 AND YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) " +
        " GROUP BY  sp640 ";

       try {
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
           con.close();
       } catch (Exception e) {
           getParent().showError(e);
       }

   }


  public TreeMap getDealers() {
    return null;
  }

  private String url = "jdbc:odbc:baas";
  private Connection con;
  private Connection con2;
  private Statement stmt;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;
}
