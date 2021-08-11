package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
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

public class VipexKTKDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter {
  public VipexKTKDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  /**
   *
   */
  public void connectionUp(){
  }




   public Vector getFixedAssortmentData() {
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();

          String sql = " SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, " +
				   "(case when accountcode = 1 then accountrelation else ''end)  AS clgroup, "+
				   "(case when itemcode = 0 then itemrelation else ''end) AS prodid, " +
				   "(case when itemcode = 1 then itemrelation else ''end) AS prodgroup,percent1 AS discount,amount " +
                   " AS price FROM pricedisctable where" +
				   " (fromdate<getdate() and todate>getdate()) " +
				   " or (fromdate=('1900-01-01 00:00:00.00') and todate=('1900-01-01 00:00:00.00')) ";

          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                          String custID = rs.getString("custid");
                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();

                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCustID(custID.trim());
                          }
                          if (mline.getCustID() == null && clGroup != null
                                          && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                          if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0) {
                                  mline.setProdGroup(prodGroup);
                          }
                          mline.setDiscount(discount);

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          lines.add(mline);
                  }

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
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
  public void connectionDown(){
  }

  public TreeMap getAgents(){
    String cmd = "SELECT num, description FROM dimensions WHERE dataareaid='dat' " +
           " AND dimensioncode=1";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("num").trim());
          String name = rs.getString("description").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agents.put(name, agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = " SELECT cus.name, cus.accountnum, (case when len(cus.invoiceaccount)>0 then cus.invoiceaccount else cus.accountnum end) AS payer, " +
             " '' AS debt, cus.linedisc , " +
             " cus.address ,salesgroup AS agent , " +
            " paymtermid  AS term,creditmax  AS limiit ,mjforall as koik " +
            " FROM custtable cus   WHERE  cus.dataareaid='dat' and cus.accountnum = 'z99999'" +
             " AND blocked=0  ORDER BY cus.accountnum    ";


 String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
                    "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid " +
                    "WHERE co.dataareaid='dat' AND a.dataareaid='dat' ORDER BY a.accountnum,co.transdate ";


       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);

  TreeMap customers = new TreeMap();
    HashMap custMap = new HashMap();

    try {
    	//Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = rs.getString("name").trim();
        String accountnum = rs.getString("accountnum").trim();


        Customer cust = new Customer(accountnum);
        cust.setName(name);
        cust.setPayer(rs.getString("payer"));
        String debtStr = rs.getString("debt");
        if (debtStr != null && debtStr.trim().length()>0 ){
          cust.setDebt(Double.parseDouble(debtStr));
        }

        if (accountnum.equals("") || accountnum.equals("")) {
        Enumeration enums = getParent().getAgentsHash().elements();
          while (enums.hasMoreElements()) {
            Agent a = (Agent) enums.nextElement();
            cust.addAgent(a);
            }
          }


        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(aadr.trim().replace('\n',' '));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }


        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
          cust.addAgent(new Agent("KTK"));
        }


        String term = rs.getString("term");
        if (term != null) {
        	if(term.trim().equals("E")){
        		term = "-1";
        	}
          cust.setPaymTerm(term.trim());
        }

        double limiit = rs.getDouble("limiit");
        if(limiit >0){
        	cust.setCredit((limiit +"").substring(0,(limiit +"").indexOf(".")));
        }

        boolean koik = rs.getBoolean("koik");
        if(koik){
        	 Enumeration enums = getParent().getAgentsHash().elements();
             while (enums.hasMoreElements()) {
               Agent a = (Agent) enums.nextElement();
               cust.addAgent(a);
             }
        }

        /*String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='dat' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
        ResultSet rs1 = st.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();*/
        customers.put(name, cust);
        custMap.put(cust.getID(), cust);
     //   System.out.println(cust.getName());
      }


      String sss = "SELECT custaccount AS klient , emplid AS agent,  visit FROM  mobilejavainfo";
      rs = stmt.executeQuery(sss);
      while(rs.next()){
      	String id = rs.getString("klient").trim();
      	Customer cust = (Customer)custMap.get(id);
      	if(cust != null){
            String ag = rs.getString("agent");
            Agent age = new Agent(ag);
          	String visit = rs.getString("visit");
            if (visit != null && visit.trim().length()>0) {
              age.setVisit(visit.trim());
              cust.setVisit(visit.trim());
            }
      		cust.addAgent(age);
      	}
      }



      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }

    } catch (Exception e){
      getParent().showError(e);
    }
    System.out.println("custdone");
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT itemgroupid AS liik , name AS nimetus FROM  inventitemgroup " +
                " WHERE dataareaid='dat'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
 //   SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='dat' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    TreeMap   tm = new TreeMap();
    /*String    sql = " SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='dat' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
    Connection con = getNewConnection();
    while (rs.next()) {
      String liik = rs.getString("liik").trim();
       // tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();*/

    return tm;
  }

  public TreeMap getProducts(Agent agent){

    String cmd =" SELECT   inv.itemid AS kood,inv.itemname AS nimetus, factor AS tykke, '' AS stock, " +
   "  price AS hind,inv.itemgroupid AS grupp, '' AS onpallet , " +
   " itembarcode AS ean , left(taxitemgroupid,2) AS vat ,unitid AS unit " +
	"  FROM inventtable inv " +
   " LEFT JOIN inventtablemodule mod ON mod.itemid = inv.itemid " +
   " LEFT JOIN inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid  " +
	"  LEFT JOIN inventitembarcode bc ON bc.itemid=inv.itemid  " +
	"  LEFT JOIN unitconvert uc ON uc.itemid=inv.itemid   " +
  " WHERE inv.dataareaid='dat' AND gr.dataareaid='dat' AND mod.dataareaid='dat'  AND moduletype=2 " +
    " AND len(inv.itemname)>0  AND left(inv.itemid,1)!='2' AND blocked = 0 "  ;


    //unitconvert
             String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +

                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid " +
                 "WHERE summ.DATAAREAID='dat'and dim.DATAAREAID='dat' and inv.DATAAREAID='dat' AND CLOSED = 0 AND  moduletype=0 " +
                 "AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid ) GROUP BY summ.itemid  ORDER BY summ.itemid ";


    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();

      System.out.println("prodstart");
      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = kood + " " + rs.getString("nimetus").trim();

        prod.setInCases(rs.getDouble("tykke"));
        prod.setPrice(rs.getDouble("hind"));

        String grupp = rs.getString("grupp");
        if(grupp!=null && grupp.trim().length()>0){
         prod.setGroup(grupp);
       // nimetus = (grupp + " " + nimetus).trim() ;
      }

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }

            prod.setName(nimetus);
            String vat = rs.getString("vat");
            if(vat!=null && vat.trim().length()>0){
             prod.setUnit(vat);
          }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                prod.setUnit(unit);
             }


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

    System.out.println("proddone");
    return products;
  }

  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    //if (true) return custProds ;

   String cmd = "SELECT jou.orderaccount,tra.itemid ,jou.invoicedate AS lastdate,tra.qty AS lastamount ,tra.lineamount/tra.qty AS lastprice " +
       " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
      " tra.invoiceid=jou.invoiceid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid " +
      " WHERE  jou.invoicedate >getdate()-180 AND jou.dataareaid ='dat' AND tra.dataareaid ='dat' " +
	  " AND jou.dataareaid ='dat' AND cus.dataareaid ='dat'" +
	  " GROUP BY jou.orderaccount,tra.itemid,jou.invoicedate,tra.qty ,tra.lineamount" +
      " ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate";


    if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kliendikood = rs.getString("orderaccount").trim();
        String tootekood = rs.getString("itemid").trim();
        String lastdate = rs.getString("lastdate").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                 double lastamount = rs.getDouble("lastamount");
                 double lastprice = rs.getDouble("lastprice");
        CustProd cp = new CustProd(kliendikood, tootekood, lastamount ,0 ,0,lastdate,lastprice,0,0," ");
        custProds.add(cp);
      }
    } catch (Exception e){
      getParent().showError(e);
    }
    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();

  /*tring cmd = "  SELECT cus.name, cus.accountnum, cus.invoiceaccount AS payer, " +
             " '' AS debt, cus.linedisc , " +
             " cus.address, abi.visit AS visit ,commissiongroup AS agent , " +
             " paymtermid  AS term,creditmax  AS limiit ,mjforall as koik, emplid AS abiagent " +
             " FROM custtable cus left join mobilejavainfo abi ON abi.custaccount=cus.accountnum  WHERE  cus.dataareaid='dat'" +
             " AND blocked=0 and  len(commissiongroup)>0 ORDER BY cus.name */
      String cmd =
          "   SELECT salesgroup AS agent, sum(lineamountmst) AS summa from  custinvoicetrans sal WHERE " +
	            "   MONTH(sal.invoicedate) = MONTH(getdate()) AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
                 "  AND sal.dataareaid='dat' GROUP BY salesgroup ";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa"));
                   // System.out.println(agent.getMonthSales());
                  }
                }
                rs.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }
          }

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,enddate AS kuupaev ,docsum AS summa,linecount AS ridu,text AS tekst " +
                   "FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc";
       this.connectionUp();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "  SELECT accountnum AS kood, 0 AS aastamyyk,0 AS kuumyyk,e.name AS agendinimi,' ' AS kontaktisik " +
                      ",k.phone AS telefon, (case when left(paymtermid,1)='N' then right(paymtermid,len(paymtermid)-1)else paymtermid end )" +
                      " AS maksetin,0 AS ale " +
                       "FROM custtable k INNER JOIN empltable e ON emplid=talsalesman " +
  	                  "WHERE e.dataareaid='dat' AND k.dataareaid='dat' ORDER BY accountnum";

      // siin tuleks leida kuu ja aasta myyk veel eraldi ja COTACT PERSON tabeli  asi ka t��le panna

      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim().substring(0,10) + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        if (tekst == null) tekst = " ";
        else tekst = tekst.trim();
        sb.append(ensureLen(tekst," ") + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
         sb.append(ensureLen(rs2.getString("maksetin").replaceAll("SR","1"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");

      }
        con.close();
        con2.close();
      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = " SELECT k.accountnum AS klient,max(d.talsalesman) AS agent,max(e.name) AS agentname " +
	   " FROM custtable k INNER JOIN empltable e ON emplid=k.talsalesman INNER JOIN custinvoicejour d ON k.accountnum=d.orderaccount " +
	   " and k.talsalesman=d.talsalesman " +
	   " WHERE e.dataareaid='dat' AND k.dataareaid='dat' AND d.dataareaid = 'dat' AND blocked = 0 AND d.invoicedate > getdate()-30 " +
	   " GROUP BY k.accountnum  ORDER BY accountnum ";
       Connection  con3 = this.getNewConnection();
      Statement ps = con3.createStatement();
      rs = ps.executeQuery(sql);
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
        if (agentName == null) agentName = "";
        bf2.append(agentName + "\n");
      }

      md.setClientAgentInfo(bf2.toString());
      con.close();
      return md;
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
      System.out.println("nr:" + nr + ", total:" + total);
      return total;*/
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "Vipex";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){

  	    String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa " +
       " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON " +
       " sal.invoiceid=jou.invoiceid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE len(talinventstatistics1num) >0 AND " +
       " inv.dataareaid='dat' AND sal.dataareaid='dat' AND cus.dataareaid='dat' AND cus.talsalesman='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())" +
       " GROUP BY talinventstatistics1num  ORDER BY talinventstatistics1num ";
  	  StringBuffer sb = new StringBuffer();

  	  System.out.println(sql);
  	 try{

    	Statement stmt =  this.getNewConnection().createStatement();
  	    ResultSet	      rs = stmt.executeQuery(sql);

  	          sb.append("GrupiNimi     kilodes ja EUR\n");
  	          sb.append("---------------------------------------------------\n");

  	          double totalKokku = 0d;
                    double totalSumma = 0d;

  	          while (rs.next()) {
  	            sb.append(rs.getString("grupp").trim() + "\n");
  	          	int kogus = rs.getInt("kogus");
                            int summa = rs.getInt("summa");
  	            totalSumma = round(totalSumma + summa, 2);
  	            totalKokku = round(totalKokku + kogus, 2);
  	            sb.append("\t" + kogus + "\t" + summa + " EUR\n\n");
  	           }

  	          sb.append("---------------------------------------------------\n");
  	          sb.append("\tKuu m��k kokku: " + totalKokku + " Kg." + totalSumma + " EUR");


  	                  } catch (Exception e) {
  	                      getParent().showError(e);
  	                  }


  	      return sb.toString();
  	  }






       public NumberFormat getNF(){
         return NF;
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

/*	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){

        String sql = "INSERT into supervisor values ("
          + "?, "   //   agent	C(50),
          + "?,"   //   agentname	C(200),
          + "?,"   //   clientid	C(50),
          + "?,"   //   clientname	C(200),
          + "?,"   //	prodid	C(50),
          + "?,"   //   prodname	C(200),
          + "getdate() ,"   //	sentdate	D,
          + " ' ' ,"   //	receivedTime	D,
          + "?,"   // amoun	N,
          + "?,"   // grouptotal	N,
          + "?,"   // cattotal	N,
          + "?,"   // pertotcat	I,
          + "?,"   // percolcat	I,
          + "?,"   // perpalcat	I,
          + "?,"   // pershecat	I,
          + "?,"   // perrescat	I,
          + "?,"   // isaction	L,
          + "?,"   // ispartner	L,
          + "?,"   // isbrand	L,
          + "?,"   // isgroup	L
          + "?,"   // actionaeva	N,
          + "?,"   // actionbeva N,
          + "?,"   // actiontota	N,
          + "?,"   // custeval	N,
          + "?,"   // custcomme	C(200),
          + "?,"   // comment	C(200),
          + "?,"   // custagent	C(200),
          + "?,"   // custagentname	C(200),
          + "?,"   // mustbe	I,
          + "?,"   // ideal	I,
          + "?,"   // resstr1	C(200),
          + "?,"   // resstr2	C(200),
          + "?,"   // resstr3	C(200),
          + "?,"   // resint1	I,
          + "?,"   // resint2	I,
          + "?,"   //resint3	N,
	      + "?,'dat',?"
		  +  ")" ; //	normalprice

          try {	Connection c = getNewConnection();
              PreparedStatement ps = c.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
                ps.setString(1, wr.getInfo().getAgentID());
                ps.setString(2, wr.getInfo().getAgentName());
                ps.setString(3, lw.getLine().getCustID());
                ps.setString(4, lw.getLine().getCustName());
                ps.setString(5, lw.getLine().getProdID());
                ps.setString(6, lw.getLine().getProdName());
                ps.setDouble(8, lw.getLine().getAmount());
                ps.setDouble(7, 0);
                ps.setDouble(9, 0);
                ps.setDouble(10, lw.getLine().getPercentageFromTotalCategory());
                ps.setDouble(11, lw.getLine().getPercentageFromColdCategory());
                ps.setDouble(12, lw.getLine().getPercentageFromPallCategory());
                ps.setDouble(13, lw.getLine().getPercentageFromShelfCategory());
                ps.setDouble(14, lw.getLine().getPercentageFromRestCategory());
                System.out.println(lw.getLine().isAction()
                		+"" +lw.getLine().isGroup()
+"" +lw.getLine().isBrand()
+"" +lw.getLine().isPartner());
                int act = 0;
                int par = 0;
                int bra= 0;
                int grp = 0;
                if(lw.getLine().isAction())act = 1;
                if(lw.getLine().isGroup())grp = 1;
                if(lw.getLine().isPartner())par = 1;
                if(lw.getLine().isBrand())bra = 1;
                ps.setInt(15, act);
                ps.setInt(16, par);
                ps.setInt(17, bra);
                ps.setInt(18, grp);
                ps.setDouble(19, lw.getLine().getAction1Eval());
                ps.setDouble(20, lw.getLine().getAction2Eval());
                ps.setDouble(21, lw.getLine().getActionTotalEval());
                ps.setDouble(22, lw.getLine().getCustEval());
                String comm = lw.getLine().getCustComment();
                if(comm == null){
                	comm = " ";
                }
                ps.setString(23, comm);
                comm = lw.getLine().getComment();
                if(comm == null){
                	comm = " ";
                }
                comm = lw.getLine().getCustAgentId();
                if(comm == null){
                	comm = " ";
                }
                ps.setString(24, comm);
                ps.setString(25, comm);
                comm = lw.getLine().getCustAgentName();
                if(comm == null){
                	comm = " ";
                }
                ps.setString(26,comm);

                ps.setDouble(27, lw.getLine().getDealNr());
                ps.setDouble(28, lw.getLine().getIdealNr());
                comm = lw.getLine().getResStr1();
                if(comm == null){
                	comm = " ";
                }
                ps.setString(29, comm);
                comm = lw.getLine().getResStr2();
                if(comm == null){
                	comm = " ";
                }
                ps.setString(30, comm);
                comm = lw.getLine().getResStr3();
                if(comm == null){
                	comm = " ";
                }
                ps.setString(31, comm);
                ps.setDouble(32, lw.getLine().getResInt1());
                ps.setDouble(33, lw.getLine().getResInt2());
                ps.setDouble(34, lw.getLine().getResInt3());
                ps.setDouble(35, lw.getLine().getNormalPrice());
                ps.setInt(36, Integer.parseInt((Calendar.getInstance().getTimeInMillis()+"").substring(6,12)));
                ps.executeUpdate();
              }

              ps.close();
          } catch (SQLException e) {
              e.printStackTrace();
          }
        }


       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:BMSDSN";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
