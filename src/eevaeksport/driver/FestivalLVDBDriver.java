
package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
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

public class FestivalLVDBDriver  extends AbstractDBDriver implements BulkUpdater,Discounter {
  public FestivalLVDBDriver(EksportPanel parent) {
  	
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
      stmt = con.createStatement();
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String getCustProdDiscounts(Agent agent) {
    return null;
  }

  public String getDealerCustomerCSV(String agent) {
    return null;
  }
  public String getSaveDebtInfo() {
   	return null;
   }
   
  public String getAssortmentStr(Agent agent){
  	return null;
  }
  
  
  public void connectionUp() {
  }

  public TreeMap getProdGroups() throws SQLException {
    TreeMap tm = new TreeMap();
    String sql = "SELECT id AS liik, descr AS nimetus FROM sc33 WHERE  isfolder =1  " +
" and parentid in ('    D8   ') ";
    try {
    Statement	stmt = DriverManager.getConnection(url).createStatement();
        ResultSet rs = stmt.executeQuery(sql);
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
private HashMap abigr = null;
  
  public TreeMap getProdGroupsSecond() throws SQLException {

      TreeMap tm = new TreeMap();
      abigr = new HashMap();
      String cmd = "SELECT id AS liik, descr AS nimetus,parentid FROM sc33 WHERE  isfolder =1 " +
      " and ( parentid in ('    D7   ','   17X   ','   17W   ','    CP   ','    YU   ','   1BA   ','   1E2   ')" +
      " or id in ('    D7   ','   17X   ','   17W   ','    CP   ','    YU   ','   1BA   ','   1E2   ') )  ";
      try {
    	Statement  stmt =  DriverManager.getConnection(url).createStatement();
        ResultSet rs = stmt.executeQuery(cmd);

        while (rs.next()) {
          String liik = rs.getString("liik").trim();
          String nimetus = rs.getString("nimetus").trim();
          String paren = rs.getString("parentid").trim();
          if (liik != "") {
            tm.put(liik, Converter.enCodeEst(nimetus));
            abigr.put(liik,paren );
          }
        }
        rs.close();
        stmt.close();
        
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

  public String getPilotDayData(Agent agent){

  	 String sql2 =  "SELECT kl.code AS klient,kl.descr AS nimi,docno AS  arve, sum(info.sp428) AS summa , date() As kuupaev " +
			" FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
          " INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc46 kl on kl.id=sp413 INNER JOIN SC5230 b ON b.parentext=kl.id " +
          " WHERE   (sp416) = date()   AND sp5228 like '%" + agent.getID() + "%'  group by docno ORDER BY docno ";
    
      StringBuffer sb = new StringBuffer();
      sb.append("\n\n\nT�na Vormistatud tehingud:\n\n");
        this.getCustomers(null);
        try {

                 Statement st = DriverManager.getConnection(url).createStatement();
               
                ResultSet rs2 = st.executeQuery(sql2);

                    while (rs2.next()) {
                    sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + 
                    		rs2.getString("nimi")+ "  Summa: " + rs2.getDouble("summa") + "\n");
                    }
                    
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
                    if(sb.length()== 0){
                    	return null;
                    }else{
                        return sb.toString();
                                         	
                    }
   
  }

  
  public TreeMap getAgents() {
    String cmd = "select id,code,descr from SC5200 where parentext like '%7%' or  parentext like '%8%'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try{
    	   Statement	stmt = DriverManager.getConnection(url).createStatement();
           ResultSet rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        	String id = rs.getString("id").trim();
        	String code = rs.getString("code").trim();
        	String name = rs.getString("descr").trim() ;
        	
            Agent agent = new Agent(id);
            agent.setIsPilot(true);
            agent.setCode(code);
            agent.setName(name + " " + code);
           String agentAGENTS = getParent().getProps().getProperty(agent.getID());
            if(agentAGENTS != null){
 	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	  	       while(stag.hasMoreTokens()){
                agent.addDubbler(stag.nextToken());
    	       }
            }
            
            agents.put(name , agent);
        }
    }catch(Exception e){
    	e.printStackTrace();
    }
   return agents;
  }

  
  
  public void setDealers(TreeMap aDealers) {
    dealers = aDealers;
  }

 private TreeMap getKomments(){
	 TreeMap res = new TreeMap();
		try {
	  File f = new File( "Sortiment/lepingud.csv");
	  System.out.println(f.getAbsolutePath());
	    if (f.exists()){
	    	
		 InputStreamReader is = new FileReader(f);	
	      BufferedReader br = new BufferedReader(is);
	      String line = null;
	      while( ( line = br.readLine()) != null){
	    	  line = line.replaceAll("\",\"", "@");
	    	  line = line.replaceAll("\"", " ").trim();
	      	String id = null;
	      	String name = null;
	        StringTokenizer st = new StringTokenizer(line, "@");
	        if (st.hasMoreTokens()){
	        	id = st.nextToken().trim();
	        	 if (st.hasMoreTokens()){
	              id = id + "_" + st.nextToken().trim();
	               if (st.hasMoreTokens()){
	            	   name = st.nextToken(); 
	               }
	          res.put(id,name);
	        }
	      }
	    }
	      is.close();
	      br.close();
	    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	// System.out.println("asi  "+ res.toString());
	 return res;
 }
  
  public TreeMap getCustomers(Agent agent) {

	  TreeMap kommentaarid = getKomments();
	  
  	String cmd = "SELECT trim(kl.code) + '_' + ltrim(a.code) AS kood ,trim(kl.code) as kood2,rtrim(kl.descr) + ' ' " +
  		"	+ a.sp6354 AS nimi,a.sp6088 AS address,c.value " +
  	" AS days,'' AS visit ,ag.id AS agent, rtrim(sp6189) + ' ' + rtrim(kon.descr) AS kontakt " +
	 "   FROM  SC3724 a INNER JOIN sc46 kl ON kl.id=a.parentext " +
	  "  INNER JOIN SC6536 agab ON agab.parentext=a.id and rtrim(ltrim(sp6537))='8'  " +
	  "  INNER JOIN SC5200 ag ON ag.id=agab.sp6538 " +
 " LEFT JOIN 1sblob ko ON " +
 " ko.objid=kl.id  AND ko.fieldid =  ' 2X5 '   left join SC6194 kon ON kon.id=sp6087 " +
 " left join 1sconst c ON c.objid=kl.id and trim(c.id)like '%P3%' " +
 " WHERE kl.isfolder=2  and (ltrim(rtrim(kl.parentid)) ='11B' or ltrim(rtrim(kl.parentid)) ='5') " +
 " and ag.descr like 'LV%' ORDER BY kl.descr " ;
  	
  	
  	
  	/*    3G   	00001	Aleksandr Herodes                                                               
    3I   	00002	German Tuisk                                                                    
*/

    if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    try {
    	
    	  String     sql = " SELECT k.code AS klid,d.docno AS doknr,sp909 AS maksepaev,sp171 AS debt,date()-sp909 AS yle FROM RG169 alg  " +
          " left JOIN 1sjourn d ON  ltrim(RIGHT(alg.sp405,7))= rtrim(ltrim(d.iddoc)) left JOIN dh410 info ON info.iddoc=d.iddoc  " +
          " left JOIN dt410 abi ON abi.iddoc=info.iddoc left JOIN  sc46 k ON k.id = sp413 " +
          " WHERE  MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) " +
          "  AND k.descr!='' AND (sp171<-0.05 OR sp171>0.05) AND sp909< date()+5 ORDER BY k.descr,sp909 GROUP BY d.docno  ";
      	stmt = DriverManager.getConnection(url).createStatement();
     ResultSet  rs = stmt.executeQuery(sql);

          if (isDebug()) System.out.println(sql);

          this.debts = new Vector();
          Calendar today = Calendar.getInstance();
          today.set(Calendar.HOUR_OF_DAY, 1);

          while (rs.next()) {
            Debt debt = new Debt();
            debt.setCustID(rs.getString("klid").trim());
            debt.setDoknr(rs.getString("doknr").trim());
            debt.setDuedate(rs.getDate("maksepaev"));
            debt.setSum(rs.getDouble("debt"));
            debt.setDue(rs.getString("yle"));
  
              java.util.Date maksep = debt.getDuedate();             
              if (maksep.getTime() < today.getTimeInMillis()) {
            	  String paYer = (String)set.get(debt.getCustID());
            	  if(paYer == null ){
            		  set.put(debt.getCustID(), debt.getSum() + "");   	  
            	  }else{
            		  set.put(debt.getCustID(),(Double.parseDouble(paYer) +  debt.getSum()) + ""); 
            	  }
              }

            debts.addElement(debt);
            
          }
          rs.close();
    	
    	
    	   Statement	stmt = DriverManager.getConnection(url).createStatement();
            rs = stmt.executeQuery(cmd);
         while (rs.next()) {
        String name = rs.getString("nimi").trim().replaceAll("\\r\\n|\\r|\\n", " ");
        
        String accountnum = rs.getString("kood").trim();
        String payer = rs.getString("kood2").trim();
        Customer cust = new Customer(accountnum);
        cust.setPayer(payer);
        
        name = name +  " " +accountnum;
        cust.setName(Converter.enCodeEst(name) );
  //      String agen = rs.getString("agent").trim();
      //  cust.addAgent(new Agent(agen));
        
        String agen = rs.getString("agent").trim();
        cust.addAgent(new Agent(agen));
         if(agen.equals("J7")){
       	  cust.addAgent(new Agent("J5"));
         }else if(agen.equals("J5")){
          	  cust.addAgent(new Agent("J7"));
            }
       // cust.addAgent(new Agent("2I")); Katarina soovil v�lja
      //  cust.addAgent(new Agent("40"));
        
        String agentAGENTS = getParent().getProps().getProperty(agen);

        if(agentAGENTS != null){
	         StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
  	       while(stag.hasMoreTokens()){
            cust.addAgent(new Agent(stag.nextToken()));
	       }
        }
      
        
         String deb =(String) set.get(payer);
         if(deb != null ){
        	 cust.setDebt(Double.parseDouble(deb));
         }
        cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("address").trim().replaceAll("\\r\\n|\\r|\\n", " ")),"Latvia"));
        if(kommentaarid.get(accountnum) != null){
        	cust.setInfo(((String)kommentaarid.get(accountnum)).replaceAll("\\r\\n|\\r|\\n", "|"));
        }
        
    //    cust.setVisit(rs.getString("visit").trim());
        String days = rs.getString("days");
        if(days != null && days.trim().length()>0){
        	cust.setPaymTerm(days.trim());        	
        }
 
        customers.put(name, cust);
        
      }

      rs.close();

      String sql3 = " select rtrim(sp6189) + ' ' + rtrim(kon.descr) + ' ' + rtrim(sp6191) + ' ' + sp6192  as kontakt from SC6223 al" +
      		" inner join SC6194 kon on sp6220 = kon.id " +
    " INNER JOIN  SC3724 a on a.id=al.parentext INNER JOIN sc46 kl ON kl.id=a.parentext where  " +
    " trim(kl.code) + '_' + ltrim(a.code) = ?";
      pstmt = con.prepareStatement(sql3);

      Iterator keys = customers.values().iterator();
      while (keys.hasNext()) {
        Customer cust = (Customer) keys.next();
        pstmt.setString(1, cust.getID());
        rs = pstmt.executeQuery();
        while (rs.next()) {
            String kontakt = rs.getString("kontakt");
            if(kontakt != null && kontakt.trim().length()>0){
            	cust.setPersonalData(cust.getPersonalData().replaceAll("\\r\\n|\\r|\\n", " ")  + kontakt.trim().replaceAll("\\r\\n|\\r|\\n", " ")+ "|");
            }
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
    
    TreeMap groups = new TreeMap();
   try{
	   groups = getProdGroupsSecond();
	       
   }catch(Exception e){
	   e.printStackTrace();
   }
    
    

    String sql = " select t.code AS toode,t.descr, ks.sp134 AS kastis,(sp102) AS kogus from rg99 l INNER JOIN  sc33 t on l.sp101=t.id AND RTRIM(LTRIM(SP3603))='2' " +
	      " LEFT JOIN sc131 ks ON ks.parentext = t.id and sp3748 like'%2%'  where year(period) = year(date()) and month(period) = month(date())" +
	      " and RTRIM(LTRIM(sp100))='J' ORDER BY T.DESCR  "; 
  
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
        toode.setKogus(toode.getKogus() + rs.getDouble("kogus"));
        toode.setKastis(rs.getDouble("kastis"));
     
      }
      rs.close();


      
      
      
      
      
      String sql2 = " select t.code AS toode,t.descr, '' AS kastis,(sp102) AS kogus from rg99 l INNER JOIN  sc33 t on l.sp101=t.id AND RTRIM(LTRIM(SP3603))='1' " +
      " where year(period) = year(date()) and month(period) = month(date()) and RTRIM(LTRIM(sp100))='G' ORDER BY T.DESCR "; //and rtrim(sp3603)like '%0' and sp100 like '%1'

      
      
      stmt = con.createStatement();
      ResultSet rs2 = stmt.executeQuery(sql2);
      Hashtable tooted2 = new Hashtable();
      while (rs2.next()) {
        String id = rs2.getString("toode").trim();
        Toode toode = (Toode) tooted2.get(id);
        if (toode == null) {
          toode = new Toode(id);
          tooted2.put(id, toode);
        }
        toode.setKogus(toode.getKogus() + rs2.getDouble("kogus"));
   //   toode.setKastis(rs.getDouble("kastis"));
     
      }
      rs2.close();
      
      
      
      
      
      
      String cmd = "  SELECT  (date),pr.id,pr.code AS kood,nim.descr AS nimi,c.value AS hind,pr.sp3559 AS EAN , yh.descr AS yhik  " +
      "  ,pr.parentid AS grupp ,pr.sp3752 AS abikood   FROM sc33 pr left join  sc33 pr2 on pr2.id = pr.parentid left join  sc33 pr3 on pr3.id = pr2.parentid   " +
       " LEFT JOIN sc131 yh ON yh.id=pr.sp3757 LEFT JOIN  SC3772 hi on hi.parentext=pr.id and  hi.descr='LV Price' LEFT JOIN " +
      " 1sconst c ON (c.objid) = (hi.id) INNER JOIN SC5973 nim on nim.parentext=pr.id and nim.code = '00003' " +
      "  WHERE pr.isfolder=2 AND ltrim(c.value)!='0.000' AND trim(c.id) like '%2WV%' and ( pr.parentid  like  '%D8%' or pr2.parentid  like  '%D8%' or  pr3.parentid like  '%D8%') " +
      "	ORDER BY pr.descr,date ";
      
   

      if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      rs = stmt.executeQuery(cmd);
      while (rs.next()) {
        // if ( kood.length() > 2 ){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi").trim();
        prod.setName(Converter.cleanLitLatEst(nimetus));
        double price = rs.getDouble("hind"); 
        if(price == 0 ){
          	price = 1;
        }
     	 prod.setPrice(price);
        String ean = rs.getString("EAN");
        if (ean != null && ean.trim().length() > 0) {
        	if(ean.startsWith("-")){
        		ean = ean.substring(1);
        	}
          prod.setEAN(ean.trim());
        }
        String yhi = rs.getString("yhik");
        if(yhi != null && yhi.trim().length()>0){
        	prod.setUnit(yhi.trim());
        }
        String grupp = rs.getString("grupp");
        /*if (grupp != null && grupp.trim().length() > 0) {
          String ste = (String) grup.get(grupp.trim());
          if (ste != null) {
            prod.setGroup(ste);
          }
        }*/
        
        Toode toode = (Toode) tooted2.get(kood);
        double kogus;
        if (toode == null) {
          //System.out.println(kood);
          kogus = 0;
        }
        else {
          kogus = round(toode.getKogus(), 2);
        }
        if(kogus >0 ){
        	prod.setName(prod.getName() + "|" + kogus + "|"); //aktsiisilao seisu lisamine
        }
        
        
        if (grupp != null && grupp.trim().length() > 0) {
              prod.setGroupSecond(grupp.trim());
              prod.setGroup(abigr.get(grupp.trim()) + "");
          }
        
        String abi = rs.getString("abikood");
        if(abi != null && abi.trim().length()>0){
        	//prod.setSorting(abi.trim()+"16");
        }

         toode = (Toode) tooted.get(kood);
     
        double kastis;
        if (toode == null) {
          //System.out.println(kood);
          kogus = 0;
          kastis = 0;
        }
        else {
          kogus = round(toode.getKogus(), 2);
          kastis = round(toode.getKastis(), 2);
        }

         prod.setStock(kogus);
         prod.setInCases(kastis);
         
     if(  groups.get(prod.getGroupSecond()) != null){
         products.put(nimetus, prod); 
     }
      }
      
      rs.close();
      
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

	    String cmd = "SELECT tt.code AS toode,kk.code AS klient,a.code AS kood,info.sp424 AS kogus,sp416 AS lastdate, " +
	       " SUM(iif(info.sp424>0,info.sp424,0)) AS Plusskogus,SUM(iif(info.sp424<0,info.sp424,0)) AS miinuskogus " +
	       "  FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
	       "  INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc33 tt ON tt.id=SP423 " +
		   "	 INNER JOIN SC3724 a ON  a.id=sp3802 "+
	       "    INNER JOIN sc46 kk ON kk.id=sp413  WHERE  sp416 > ?  GROUP BY kk.code,sp3802, tt.code ORDER BY kk.code,sp3802, tt.code" ;
	    if (isDebug()) System.out.println(cmd);
	    Vector custProds = new Vector();
	    try {
	        con = DriverManager.getConnection(url);
	      pstmt = con.prepareStatement(cmd);

	      Calendar cal = Calendar.getInstance();
	      cal.add(Calendar.MONTH,
	              -Integer.parseInt(getParent().getProps().getProperty("months")));
	      pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));

	      ResultSet rs = pstmt.executeQuery();
	      while (rs.next()) {
	        String kliendikood = rs.getString("klient").trim()+"_"+rs.getString("kood").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("kogus");
	        double plusskogus = rs.getDouble("plusskogus");
	        double miinuskogus = rs.getDouble("miinuskogus");
	        String lastDate = rs.getString("lastdate");

	        CustProd cp = new CustProd(kliendikood, tootekood, kogus, plusskogus,
	                                   miinuskogus, lastDate, 0, 0, 0, " ");
	        custProds.add(cp);
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
    String cmd = "SELECT SUM(info.sp428) AS sales,sp5228 AS agent  FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
       "  INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc46 kl on kl.id=sp413 INNER JOIN SC5230 b ON b.parentext=kl.id " +
       "  WHERE  YEAR(sp416)=YEAR(date()) AND MONTH(sp416) = MONTH(DATE()) group by sp5228 ";

    try {
      pstmt = con.prepareStatement(cmd);
      pstmt.setString(1, agentCode);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        sales = rs.getDouble("sales");
        setSales(sales);
      }
    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return 0;
  }
 
  public void setBulkSales(Map agents){

    String cmd = "SELECT SUM(info.sp428) AS summa,sp5228 AS agent  FROM dt410 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
    "  INNER JOIN dh410 abi ON abi.iddoc=info.iddoc INNER JOIN sc46 kl on kl.id=sp413 INNER JOIN SC5230 b ON b.parentext=kl.id " +
    "  WHERE  YEAR(sp416)=YEAR(date()) AND MONTH(sp416) = MONTH(DATE()) group by sp5228" ;
       if (isDebug()) System.out.println(cmd);
          try {
              Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("summa"));
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
        }


  
  public DealerProduct getDealerProductsList() {

  /*  try {

      String sql = " SELECT t.code AS toode,SUM(sp411) AS STOCK , l.code AS LADU from rg405 s INNER JOIN sc55 l ON l.id=s.sp418 " +
          " inner join  sc84 t on sp408=t.id WHERE MONTH(period)=MONTH(date()) AND YEAR(period)=YEAR(date()) GROUP BY t.code,l.code ORDER BY l.code ";

      if (isDebug()) System.out.println(sql);

      Statement stm = con.createStatement();
      ResultSet rst = stm.executeQuery(sql);

      while (rst.next()) {
        String toode = rst.getString("toode").trim();
        int seis = (int) Double.parseDouble(rst.getString("stock").replace(',',
            '.'));
        String ladu = rst.getString("ladu").trim();

        dealerProds.add(ladu, toode, seis + "", " ", " ");

        //System.out.println(ladu+" "+toode+" "+seis+"");
      }
      rst.close();

    }
    catch (Exception e) {
      getParent().showError(e);
    }

    return dealerProds;
*/
    return null;
    }

  public String getStartingTime() {
    return null;
  }

  public String getDriverVersion() {
    return "Festival";
  }

  public String getSubjectFilesTo() {
    return this.subjectFilesTo;
  }


  public TreeMap getDealers() {
    return dealers;
  }

  private String url = "jdbc:odbc:baas";
  private Connection con;
  private Statement stmt;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;
  private TreeMap dealers = new TreeMap();

	  public Vector getFixedAssortmentData() {
	        Vector lines = new Vector();
String cmd = "select t.code aS toode,k.code+'_'+LTRIM(L.CODE) as klient,K.DESCR,T.DESCR from SC6768 sor inner join sc33 t on  sor.SP6770=t.id " +
       " inner join SC3724 l on l.id = sor.parentext inner join SC46 k on k.id=l.parentext ORDER BY k.code,L.CODE,T.descr ";
	        	 try {
	        		 
	        		Connection con = DriverManager.getConnection(url);
	                Statement  stmt = con.createStatement();
	                 ResultSet	      rs = stmt.executeQuery(cmd);
	                 while(rs.next()) {
	             
	                   String custID = " ";
                       String clGroup = " ";
                       String prodID = " ";
                       String prodGroup = " ";
                       MatrixLine mline = new MatrixLine();
                       
                       prodID = rs.getString("toode").trim();
                       custID =  rs.getString("klient").trim();
                    
                    //    System.out.println("Mline " + custID + "  " +  prodID);
                        if (custID != null && custID.trim().length() > 0) {
                          mline.setCustID(custID.trim());
                        }
                       if (clGroup != null
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
                         
                       lines.add(mline);
	                   
	                 }
	                 rs.close();
	                 stmt.close();
	                 con.close();
	             } catch (Exception e) {
	                 getParent().showError(e);
	              }
	           
	        	
	                       
	            
	        return lines;
	}


public Vector getLastThreeData() {
	// TODO Auto-generated method stub
	return null;
}

public Vector getPriceMatrixData() {
	// TODO Auto-generated method stub
	return null;
}

public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}
}

