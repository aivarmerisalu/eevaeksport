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
import eevaeksport.MatrixLine;
import eevaeksport.Product;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class VikstalDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {
  public VikstalDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url);
      con2 = DriverManager.getConnection(url);
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  public String getAssortmentStr(Agent agent){
  	return null;
  }

                                     public String  getCustProdDiscounts(Agent agent){
                                      return null;
                                      }
                                     public String getSaveDebtInfo() {
                                       	return null;
                                       }
                                       
          public String getDealerCustomerCSV(String agent){
           return null;
           }

  /**
   *
   */
  public void connectionUp(){
  }

  public TreeMap getProdGroups() throws SQLException {
    TreeMap   tm = new TreeMap();
     stmt = con.createStatement();
  String  sql = "SELECT id AS liik, descr AS nimetus FROM sc113 WHERE isfolder=1 ORDER BY descr " ;
   try {
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
         tm.put(rs.getString("liik").trim(),
           rs.getString("nimetus").trim());
         }
          rs.close();
          } catch (Exception e){
          getParent().showError(e);
        }


  return tm;
  }



      public String getSaveStockInfo(){
      return null;
      }


  /**
   *
   */
  public void connectionDown(){
  }

  public TreeMap getAgents(){
    String cmd = "SELECT id,trim(descr) + ' ' + code as nimi FROM sc96 where ltrim(parentid)='1YX'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("id").trim());
          String name = rs.getString("nimi").trim();
          agent.setName(name);
          agents.put(name, agent);
        }
        rs.close();
    } catch (Exception e){
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
    dealers = aDealers;
  }




  public TreeMap getCustomers(Agent agent) {
    String cmd = " SELECT kl.code AS kood,kl.descr AS nimi,rtrim(ltrim(sp82)) AS address,'' AS visit,abi.sp726 AS ale " +
    		" ,sp539 AS blokk ,'' AS toall,kl.sp873 AS payer,  ltrim(rtrim(kl.sp715)) as agent " +
        "  FROM sc96 kl left JOIN 1sconst c ON c.objid=kl.id and ltrim(c.id)='2J' LEFT JOIN  sc724 abi ON  abi.id=c.value	" +
        "        WHERE   kl.verstamp != 'E' ORDER BY kl.descr ASC GROUP BY kl.descr ";
    /*and ltrim(rtrim(fieldid))='5YG' selle v�tsin �rah
         a. maksetahtaeg - failis 1sconst ID=2G (paevade kogus)
         b. allahindlus - failis 1sconst ID=2K - objekt failist SC724
         c. krediidilimiit - failis 1sconst ID=2F
     */
    if (isDebug()) System.out.println(cmd);

    TreeMap customers = new TreeMap();
    HashMap set = new HashMap();
    try {
      pstmt = con.prepareStatement(cmd);
      //pstmt.setString(1, agent.getID());
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        String name = rs.getString("nimi").trim();
        String accountnum = rs.getString("kood").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(name);
        cust.addAgent(agent);
        cust.setAddress(rs.getString("address").trim());
        cust.setVisit(rs.getString("visit").trim());
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
          Enumeration enums = getParent().getAgentsHash().elements();
          while (enums.hasMoreElements()) {
            Agent a = (Agent) enums.nextElement();
            cust.addAgent(a);
          }
        }

        String payer = rs.getString("payer");
        if (payer != null && payer.trim().length() > 0) {
          String sql1 = "SELECT code FROM sc96  " +
              " WHERE  ltrim(rtrim(id)) = '" + payer.trim() + "'";
          Statement st = con2.createStatement();
          ResultSet rs2 = st.executeQuery(sql1);
          while (rs2.next()) {
          //  cust.setPayer(rs2.getString("code").trim());
          }
          rs2.close();
          st.close();
        }

        
        String ag = rs.getString("agent");
        if(ag != null && ag.trim().length() > 0){
        	cust.addAgent(new Agent(ag.trim()));
        }
        
        customers.put(name, cust);
        set.put(cust.getID(), cust);
      }

      rs.close();
      pstmt.close();


      String sql = "SELECT k.code AS klid,d.docno AS doknr,iif(sp261>date()-1000,sp261,d.date) AS maksepaev,-sp440 AS debt,date()-iif(sp261>date()-1000,sp261,d.date) AS yle FROM RG441 alg " +
          "  left JOIN 1sjourn d ON ltrim(RIGHT(alg.sp439,8)) = rtrim(ltrim(d.iddoc)) left JOIN dh272 info ON ltrim(rtrim(info.iddoc))=ltrim(rtrim(d.iddoc)) " +
          " left JOIN dt272 abi ON abi.iddoc=info.iddoc left JOIN  sc96 k ON k.id = sp437 " +
          " WHERE (  MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period) " +
          " ) AND sp440<>0 ORDER BY k.descr,sp261 GROUP BY d.docno ";

 //
      pstmt = con.prepareStatement(sql);
      if (isDebug()) System.out.println(sql);
     // pstmt.setString(1, agent.getID());
      //pstmt.setString(2, agent.getID());
      rs = pstmt.executeQuery();

      this.debts = new Vector();
      Calendar today = Calendar.getInstance();
      today.set(Calendar.HOUR_OF_DAY, 1);

      while (rs.next()) {
        Debt debt = new Debt();
        String klid = rs.getString("klid").trim();
        
          debt.setCustID(klid);
        
        debt.setDoknr(rs.getString("doknr").trim());
        Date dat = rs.getDate("maksepaev");
        if (dat != null) {
          debt.setDuedate(dat);
        }
        debt.setSum(rs.getDouble("debt"));
        debt.setDue(rs.getString("yle"));

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

      System.out.println("Debts size " + debts.size());
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
  
  
  
  public Vector getPriceMatrixData() {
      Vector lines = new Vector();
     String sql = "select pr.code,sp1072 as custgr ,sp1080 AS price ,max(d.sp1086) , SP1895 as kamp from DT1070 r inner join " +
	" DH1070 d on r.IDDOC = d.IDDOC left join sc113 pr on rtrim(pr.id) = rtrim(r.sp1073) " +
	" group by  pr.code , sp1072 order by sp1072, code";
 TreeMap vahe = new TreeMap();
      try {

      	Connection con = DriverManager.getConnection(url);
          	Statement stmt = con.createStatement();
              ResultSet rs = stmt.executeQuery(sql);

              while (rs.next()) {
            	  String prodID = rs.getString("prodid");
                      String custGR = rs.getString("custgr");
                      double price = rs.getDouble("price");
                  
                      String camp = rs.getString("camp").trim();
                    
                     //  String algus = rs.getString("algus");
                     // String lopp = rs.getString("lopp");
                      
                      
                      MatrixLine mline = new MatrixLine();
                      
                
                      if (custGR != null && custGR.trim().length() > 0) {
                              mline.setCliGroup(custGR.trim());
                      }
                   
                      if (prodID != null && prodID.trim().length() > 0) {
                              mline.setProdID(prodID.trim());
                      }
                      
                      if(camp.length() > 1){
                    	  mline.setLineinfo("pr");
                      }
                 

                      if (price != 0) {
                              mline.setPrice(price);
                      }
                      
                      /*if (algus != null && algus.trim().length() > 0) {
                          mline.setStartDate(algus.trim().substring(0,10));
                  }
                      if (lopp != null && lopp.trim().length() > 0) {
                          mline.setEndDate(lopp.trim().substring(0,10));
                  } */
                      
                      vahe.put(custGR.trim()+ "-" + prodID.trim(), mline);
                      
                      
              }
              rs.close();
              stmt.close();
              con.close();
              
      } catch (SQLException ex) {
      ex.printStackTrace();
      }

      Iterator itera = vahe.keySet().iterator();
      while(itera.hasNext()){
      	 lines.add(vahe.get(itera.next()));
      }
      
     

      
      
      return lines;
}

  
  
  

  public TreeMap getProducts(Agent agent){

    TreeMap products = new TreeMap();


    String sql = " SELECT pr.code AS toode, sum(sp418) AS seis,sp119 AS kastis  FROM sc113 pr inner JOIN rg426 alg ON alg.sp416=pr.id " + 
       " LEFT JOIN sc121 yh ON yh.parentext=pr.id  AND  ltrim(sp118)= 'D' " +
       " WHERE MONTH(date())=MONTH(alg.period) AND YEAR(date())=YEAR(alg.period)order by pr.code group by pr.code ";
    
    //AND sp1424 = .F.
    try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Hashtable tooted = new Hashtable();
            while(rs.next()){
              String id = rs.getString("toode").trim();
              Toode toode = (Toode)tooted.get(id);
              if (toode == null){
                toode = new Toode(id);
                tooted.put(id, toode);
              }
              toode.setKogus(toode.getKogus() + rs.getDouble("seis"));
              toode.setKastis(rs.getDouble("kastis"));

            }
            rs.close();

String cmd = "  SELECT  (date),pr.id,pr.code AS kood,pr.descr AS nimi,(c.value) AS hind,sp510 AS EAN , yh.descr AS yhik  " +
    " ,parentid AS grupp FROM sc113 pr  "+
   "  LEFT JOIN sc41 yh ON yh.id=sp106 LEFT JOIN SC506 hi on hi.parentext=pr.id  LEFT JOIN 1sconst c ON (c.objid) = (hi.id) LEFT " +
   "join sc504 abi on abi.id=hi.sp508 " +
  "  WHERE  ((ltrim(c.id)='E5' AND isfolder=2 AND ltrim(c.value)!='0.000'  and ltrim(sp508)='I' )  OR  pr.descr  like'%pda%') and date<=date() " +
	"  ORDER BY pr.descr, date ";


    if (isDebug()) System.out.println(cmd);
      stmt = con.createStatement();

      rs = stmt.executeQuery(cmd);
      while(rs.next()){
        // if ( kood.length() > 2 ){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi").trim();
        prod.setName(kood + " " + nimetus  );
        prod.setPrice(rs.getDouble("hind"));
        String ean =rs.getString("EAN");
        if(ean != null && ean.trim().length()>0){
          prod.setEAN(ean.trim());
        }
        String yhik = rs.getString("yhik");
        if (yhik!= null && yhik.trim().length()>0){
          prod.setUnit(yhik.trim());
        }
        prod.setVat("24");
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length() > 0) {
            prod.setGroup(grupp.trim());
        }

        Toode toode = (Toode) tooted.get(kood);
        double kogus;
               double kastis;
               if (toode == null){
                 kogus = 0;
                 kastis =0;
               } else {
                  kogus = round(toode.getKogus(), 2);
                  kastis = round(toode.getKastis(), 2);
               }

          prod.setStock(kogus);
          prod.setInCases(kastis);

        products.put(nimetus, prod);

      }
      rs.close();
      //stmt.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    return products;
  }




      public String getDistribution() {
        return null;
      }


  public Vector getCustomerProductsList(String agentCode){

    String cmd = " SELECT tt.code AS toode,kk.code AS klient,sp265 AS kogus,sp261 AS lastdate,  SUM(iif(sp265>0,sp265,0)) AS Plusskogus,SUM(iif(sp265<0,sp265,0)) AS miinuskogus " +
        " FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc INNER JOIN dh272 abi ON abi.iddoc=info.iddoc INNER JOIN sc113 tt ON tt.id=sp264 " +
        " INNER JOIN sc96 kk ON kk.id=sp254  WHERE  sp261 > ?  GROUP BY sp254, tt.descr ORDER BY sp254, tt.descr " ;

    if (isDebug()) System.out.println(cmd);
    Vector custProds = new Vector();
    try {
      pstmt = con.prepareStatement(cmd);
     // pstmt.setString(1, agentCode);

      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -Integer.parseInt(getParent().getProps().getProperty("months")));
      pstmt.setDate(1, new java.sql.Date(cal.getTime().getTime()));

      ResultSet rs = pstmt.executeQuery();
      while(rs.next()){
        String kliendikood = rs.getString("klient").trim();
        String tootekood = rs.getString("toode").trim();
        double kogus = rs.getDouble("kogus");
         double plusskogus = rs.getDouble("plusskogus");
         double miinuskogus = rs.getDouble("miinuskogus");
         String lastDate =  rs.getString("lastdate").substring(5,10);

        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,plusskogus ,miinuskogus,lastDate,0,0,0," ");
        custProds.add(cp);
      }
      rs.close();
      pstmt.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    return custProds;
  }


  public double getSalary(String agentCode) {
    /*connectionUp();*/
    double sales = 0;
    String cmd = " SELECT (abi.sp269) AS sales  FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
       " INNER JOIN dh272 abi ON abi.iddoc=info.iddoc INNER JOIN sc113 tt ON tt.id=sp264 " +
        " WHERE ltrim(rtrim(sp640)) =? AND YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) " +
                  " GROUP BY docno";

      try {
          pstmt = con.prepareStatement(cmd);
          pstmt.setString(1, agentCode);
          ResultSet rs = pstmt.executeQuery();

          while(rs.next()) {
            sales = sales + rs.getDouble("sales");
          }
          setSales(sales);
      } catch (Exception e) {
          getParent().showError(e);
      }

      return 0;
    }

      public DealerProduct getDealerProductsList() {
           DealerProduct dealerProds = new DealerProduct();
            return dealerProds;
          }


  public String getStartingTime(){
    return null;
  }

  public String getDriverVersion(){
    return "Vikstal";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){
    return null;
  }



      public TreeMap getDealers() {
        return dealers;
      }




  private String url = "jdbc:odbc:baas";
  private Connection con;
  private Connection con2;
  private Statement stmt;
  private PreparedStatement pstmt;
  private String subjectFilesTo = null;
  private Vector debts = null;
  private TreeMap  dealers = new TreeMap();
@Override
public void setBulkSales(Map agents) {
	 
	    String cmd = " SELECT sum(info.sp269) AS sales , sp640 as agent FROM dt272 info INNER JOIN  1sjourn pais ON info.iddoc=pais.iddoc " +
	       " INNER JOIN dh272 abi ON abi.iddoc=info.iddoc INNER JOIN sc113 tt ON tt.id=sp264 " +
	        " WHERE  YEAR(date)=YEAR(date()) AND MONTH(date) = MONTH(DATE()) " +
	                  " GROUP BY sp640 ";

	      try {
	          pstmt = con.prepareStatement(cmd);
	         // pstmt.setString(1, agentCode);
	          ResultSet rs = pstmt.executeQuery();

	          while(rs.next()) {
	           double  sales =  rs.getDouble("sales");
	            String ag = rs.getString("agent");
	           if(ag != null && ag.trim().length() > 0){
	        	   Agent agent = (Agent)agents.get(ag.trim());
	  				if (agent != null){
	  					agent.setMonthSales((int)sales);
	  				}   
	           }
	            
	          }
	      } catch (Exception e) {
	          getParent().showError(e);
	      }

	
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
