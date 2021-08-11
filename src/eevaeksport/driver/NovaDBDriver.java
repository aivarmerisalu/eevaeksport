package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

public class NovaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter {
  public NovaDBDriver(EksportPanel parent){
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


  public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getFixedAssortmentData() {
     return null;
   }

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         /* String sql = " SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, " +
				   "(case when accountcode = 1 then accountrelation else ''end)  AS clgroup, "+
				   "(case when itemcode = 0 then itemrelation else ''end) AS prodid, " +
				   "(case when itemcode = 1 then itemrelation else ''end) AS prodgroup,percent1 AS discount,amount " +
                   " AS price FROM pricedisctable where" +
				   " fromdate<getdate() and todate>getdate() AND " +
				  " fromdate!=('1900-01-01 00:00:00.00')";

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
          }*/
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
    String cmd = "select kdi,teksti from v_myyja where teksti!=''";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kdi").trim());
          //String name = rs.getString("name").trim();
          agent.setName(rs.getString("teksti").trim());
          //agent.setIsPilot(true);
          agents.put(agent.getID(), agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = " select  cus.nimi1 , cus.nimi2, cus.numero AS kood,(case when len(cus.laskutus)=0 or " +
		 " cus.laskutus = 0  then cus.numero else cus.laskutus end) AS payer,  cus.avoinna AS debt, " +
		 "  cus.alepros AS linedisc ,cus.losoite,cus.osoite,  " +
         " hen.selite AS visit ,cus.myyja AS agent ,'' AS term,1 AS block ,cus.puhelin AS info " +
		 "  from  asiakas cus LEFT JOIN ASHENKILO hen ON hen.numero = cus.numero  WHERE len(nimi1)>0 AND myyja in ('11','301','9','2','1','22') order by nimi1" ;


 String sql = "SELECT anumero AS klient,numero AS number,(case when len((nettopvm))>0 then pvm else nettopvm end) AS maksepaev,summa AS tasumata,suorituksia AS makstud, " +
                   " pvm AS kuupaev , datediff(day,nettopvm,getdate()) AS yle " +
                   " from lasku where tila <> 8 and len(str(anumero))>0 order by nimi1 ";


       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    HashMap custMap = new HashMap();
    try {
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("nimi1").trim();
        String name2 = rs.getString("nimi2");
        if(name2!=null && name2.trim().length()>0){
           name = name + " " + name2;
        }

        String accountnum = rs.getString("kood").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(name);
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length()>0){
        	cust.setPayer(payer);
        }
        String debtStr = rs.getString("debt");

        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }

        double aleke = rs.getDouble("linedisc");
            cust.setDiscount(aleke);

        String aadr = rs.getString("losoite");//losoite,osoite,
        if (aadr != null && aadr.trim().length()>0) {
        	aadr = aadr.trim();
          cust.setAddress(aadr.replace('\n',' ').trim());
        }
        String aadr2 = rs.getString("osoite");//losoite,osoite,
        if (aadr2 != null && aadr2.trim().length()>0) {
        	aadr2 = aadr2.trim();
          cust.setAddress((cust.getAddress() +  " " + aadr2.replace('\n',' ')).trim());
        }



        String visit = rs.getString("visit");

        if(cust.getID().equals("235012")){
        Iterator iter = getAgents().values().iterator();
        while (iter.hasNext()){
              Agent a = (Agent) iter.next();
            cust.addAgent(a);
            }

        }

        String agen = rs.getString("agent");
        if (agen != null) {
        	Agent aa = new Agent(agen.trim());
        	if (visit != null && visit.trim().length()>0) {
        		aa.setVisit(visit.trim());
            }
           cust.addAgent(aa);

           if(agen.equals("22")){
           	Agent ab = new Agent ("11");
           	if (visit != null && visit.trim().length()>0) {
        		ab.setVisit(visit.trim());
            }
             cust.addAgent(ab);
           }else if(!agen.equals("11") && !agen.equals("0")){
             cust.addAgent(new Agent("11"));
           }

        }

        int block = rs.getInt("block");
        if (block == 1) {
         // cust.setSalesBlock(true);
        }
        String info  = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info);
        }
        customers.put(name, cust);
        custMap.put(cust.getID(), cust);
   //     System.out.println(name);
      }





		      String sql3 = "  SELECT max(pvm),d.numero,d.anumero AS  klient,r.koodi AS prodid, r.kpl AS amount,r.mk AS price " +
			  		" from mt d inner join mtrivi r on r.numero=d.numero where d.laji=1 AND r.koodi !='' AND anumero!=0 " +
			  		" AND pvm>getdate()-365 AND r.kpl > 0 AND r.mk > 0 group by d.numero,d.anumero,r.koodi,r.kpl,r.mk ";

		rs = stmt.executeQuery(sql3);
		StringBuffer buf = new StringBuffer();
		while(rs.next()){
		 String id = rs.getString("klient").trim();
		 Customer cust = (Customer)custMap.get(id);
		 if (cust != null){
		 	String pr = rs.getString("prodid");
		 	double am =  rs.getDouble("amount");
		 	double hind = rs.getDouble("price");
		 	String hi = hind+"";
		 	int len  = (hi.substring(hi.indexOf("."),hi.length())).length();
		 	if(len > 2){
		 		hi = hi.substring(0,hi.indexOf(".")+3);
		 	}else{
		 		hi = hi.substring(0,hi.indexOf(".")+2);
		 	}

		 	buf.append(id + ";" + pr + ";" + (am +"").substring(0,(am +"").indexOf(".")) + ";" + hi	+ "\n");
		 }
		}

		if(buf.length() > 2){
		    File f = new File(  "offer.txt");
		    if (f.exists())f.delete();
		      try {
				PrintWriter os = new PrintWriter(new FileWriter(
						 "offer.txt", true));
				os.write(buf.toString());
				os.close();
			  }catch (IOException ex) {
				ex.printStackTrace();
			  }
		 }



      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        Date dd = rs.getDate("maksepaev");
                        debt.setSum(rs.getDouble("tasumata")-rs.getDouble("makstud"));
                        Date ddd = rs.getDate("kuupaev");
                        if(dd != null){
                            debt.setDuedate(dd);
                        }else {
                        	debt.setDate(ddd);
                        }
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }

      Connection newCon = stm.getConnection();
      newCon.close();
      stm.close();

    } catch (Exception e){
      getParent().showError(e);
    }
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
       String    sql = " select kdi,teksti from  v_paatuoteryhma g  where teksti!='' order by kdi";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);
 //   SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='tal' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID

    while (rs.next()) {
      String liik = rs.getString("kdi").trim();
      String liik2 = "";
      if (liik.trim().length()==1){
      	liik2 = "000" + liik;
      }
      if (liik.trim().length()==2){
      	liik2 = "00" + liik;
      }
      if (liik.trim().length()==3){
      	liik2 = "0" + liik;
      }
        tm.put(liik,liik2+ " "  + rs.getString("teksti").trim());
    }
    rs.close();
    st.close();
    con.close();
    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    TreeMap   tm = new TreeMap();
   /* String    sql = " select kdi,teksti from  v_niryhma g  where teksti!='' order by kdi,ltrim(teksti) ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("kdi").trim();
        tm.put(liik, rs.getString("teksti").trim());
    }
    rs.close();
    st.close();
    con.close();
*/
    return tm;
  }

  public TreeMap getProducts(Agent agent){
    Vector prods = new Vector();


    String cmd = " SELECT d.tanumero,r.koodi ,d.pvm AS lastdate ,r. veroton/r.kpl AS lastprice, kpl AS kogus FROM lasku  d INNER JOIN laskurivi r ON  r.numero=d.numero  " +
       			  " WHERE  d.pvm >getdate()-500 AND len(koodi)>0 AND len(d.tanumero)>0 and r.kpl<>0 GROUP BY   d.tanumero,r.koodi,d.pvm,r.kpl ,r.veroton " +
        			  " ORDER BY d.tanumero,r.koodi,d.pvm,r.kpl,r.veroton" ;


     if (isDebug()) System.out.println(cmd);

     try {

     	Statement stmt =  this.getNewConnection().createStatement();
       ResultSet rs = stmt.executeQuery(cmd);
       while(rs.next()){
         String kliendikood = rs.getString("tanumero").trim();
         String tootekood = rs.getString("koodi").trim();
         String lastdate = rs.getString("lastdate").substring(0,10);
                  lastdate = lastdate.substring(5,10);
         double lastprice = rs.getDouble("lastprice");
         CustProd cp = new CustProd(kliendikood, tootekood, rs.getDouble("kogus") ,0 ,0,lastdate,lastprice,0,0," ");
         custProds.add(cp);
         prods.add(tootekood);
       }
     } catch (Exception e){
       getParent().showError(e);
     }



     cmd = "SELECT v.koodi AS toode ,v.nimike AS nimi,v.nimike2 AS nimi2,v.myksikko as yhik,v.OVH AS price," +
    		"v.tr as grupp2,substring(ltrim(rtrim(str(v.tr))),0,len(ltrim(rtrim(str(v.tr))))-1) AS grupp,varastossa-menossa AS  stock  FROM" +
    		" VARASTO v LEFT JOIN paikka p ON p.koodi = v.koodi where len(v.nimike) >0 AND v.abcryhma!='E'" +
    		"and vp=1  ";


    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = rs.getString("toode").trim();
        Product prod = new Product(kood);
        String nimetus = kood + " " + rs.getString("nimi").trim();
       // String nimetus = rs.getString("nimi").trim();

       // prod.setInCases(rs.getDouble("kastis"));


        String unit = rs.getString("yhik");
        if(unit!=null && unit.trim().length()>0){
         prod.setUnit(unit);
      }
        double price = rs.getDouble("price");
        if(price == 0){
        	price = 0.01;
        }

    	prod.setPrice(price);

      /*  String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }*/
        String grupp = rs.getString("grupp2");
        if(grupp!=null && grupp.trim().length()>0){
         prod.setGroup(grupp);
      }


      String grupp2 = rs.getString("grupp");
       if(grupp2 != null && grupp2.trim().length()>0){
         prod.setGroupSecond(grupp2);
      }
       double stock = rs.getDouble("stock");
       if(stock > 0){
       	prod.setStock(stock);
       }


      prod.setName(nimetus);
     // if (prod.getStock() > 0 || prods.contains(prod.getID()) ){
        products.put(nimetus, prod);
     // }

      }
      rs.close();




    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }





  public Vector getCustomerProductsList(String agentCode){
    return this.custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
      String cmd =
          " SELECT  d.myyja as agent,round(sum(mk),0) AS summa " +
          " FROM lasku  d INNER JOIN laskurivi r ON  r.numero=d.numero " +
      	  " WHERE  YEAR(d.pvm) = YEAR(getdate()) " +
          " AND MONTH(d.pvm) = MONTH(getdate()) and len(koodi)>0 GROUP BY d.myyja  ";

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
  	                  "WHERE e.dataareaid='tal' AND k.dataareaid='tal' ORDER BY accountnum";

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
	   " WHERE e.dataareaid='tal' AND k.dataareaid='tal' AND d.dataareaid = 'tal' AND blocked = 0 AND d.invoicedate > getdate()-30 " +
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
    return "Nova";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){

  	    String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa " +
       " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON " +
       " sal.invoiceid=jou.invoiceid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE len(talinventstatistics1num) >0 AND " +
       " inv.dataareaid='tal' AND sal.dataareaid='tal' AND cus.dataareaid='tal' AND cus.talsalesman='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())" +
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
	      + "?,'tal',?"
		  +  ")" ; //	normalprice
     //   TAL_QUESTIONID TAL_ANSWER                     TAL_ANSWERDATE          CUSTACCOUNT DATAAREAID RECID
        String sql2 = "INSERT INTO TAL_ANSWERS values (?,?,getdate(),?,'tal',?)";
        String ttt = "Select tal_answertype AS vas from TAL_questions WHERE TAL_questionid = ? ";
          try {	Connection c = getNewConnection();
              PreparedStatement ps = c.prepareStatement(sql);
              PreparedStatement pp = c.prepareStatement(sql2);
              PreparedStatement pr = c.prepareStatement(ttt);
              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
                pr.setString(1, lw.getLine().getProdID());
                ResultSet rs = pr.executeQuery();
                String ans = "";
                if(rs.next()){
                	 ans  = rs.getString("vas").trim();
                }

                pp.setString(1, lw.getLine().getProdID());
                if(ans.equals("1")){
                	double anse = lw.getLine().getResInt1();
                	if(anse == 1){
                		anse = 1;
                	}else if (anse == 0){
                		anse = 1;
                	}else if (anse == 2){
                		anse = 0.5;
                	}

                    pp.setDouble(2,anse);
                }else if(ans.equals("2")){
                    pp.setDouble(2, lw.getLine().getAmount());
                }else if(ans.equals("3")){

                	double anse = lw.getLine().getResInt1();
                	if(anse == 0){
                		anse = 1;
                	}else if (anse == 1){
                		anse = 0;
                	}
                    pp.setDouble(2, anse);
                }else if(ans.equals("4")){
                	double anse = lw.getLine().getResInt3();
                	if(anse == 0){
                		anse = 0;
                	}else {
                		anse = anse/100;
                	}
                    pp.setDouble(2, anse);
                }
				pp.setString(3, lw.getLine().getCustID());
                pp.setInt(4 , 10000);

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
                pp.executeUpdate();
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
  private String url = "jdbc:odbc:MQIS";
  private Connection con;
  private Vector custProds = new Vector();
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
