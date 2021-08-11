
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;


public class SAFWiruBillDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater{
  public SAFWiruBillDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }

      public String getDealerCustomerCSV(String agent){
       return null;
       }

	  public void connectionUp(){
	  }
	
	
	  public String getAssortmentStr(Agent agent){
	  	return null;
	  }

      public String getSaveStockInfo(){
      return null;
      }
      public String getSaveDebtInfo() {
       	return null;
       }
       
 	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
	
	  public void connectionDown(){
	  }

  
    public TreeMap getAgents(){
  	
    String cmd = "SELECT id AS agent, name AS nimetus, code as abikood  " +
       " FROM  grcompanycat WHERE Category='myygimees'";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    HashMap aHash = new HashMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          agent.setName(name);
          agent.setObject("607");
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent("565");
    aa.setName("HALJALA");
    aa.setObject("607");
    agents.put(aa.getName(), aa);

    return agents;
  }



      public String getDistribution() {
/*
        StringBuffer sb = new StringBuffer();

        try {
          Connection con = getNewConnection();
          Statement st = con.createStatement();
          String cmd = "select mmitno as kood ,mmitds AS Nimetus , mmdim1 from mvxjdta.mitmas where MMCFI5 = 'Y'";

          if (isDebug())
          System.out.println(cmd);
          ResultSet rs = st.executeQuery(cmd);
         while(rs.next()){
           sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
         }
         st.close();
         con.close();
       } catch (Exception e){}
         System.out.println(sb.toString());
        return sb.toString();*/
    return null;
      }




  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address,U_kylastus AS visit ,creditlimit AS limiit,cat.id AS agent, " +
          " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone,i_ladu  AS ladu "+
          " FROM grcompany kl INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id INNER JOIN grcompanycat cat " +
          " ON cat.id=rel.idgrcompanycat WHERE cat.category='myygimees' AND ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          cust.setName(name);
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(aadress1);
          String visit = rs.getString("visit");
          if(visit != null){
           // cust.setVisit(visit.trim());
          }
          //String info = rs.getString("info");
          //if (info != null){cust.setInfo(info.trim());}
         String limiit = rs.getString("limiit");
         // System.out.println(limiit);
          if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	}
          	}
          String ag = rs.getString("agent").trim();
          cust.addAgent(new Agent(ag));
       /*   if (ag.equals("3")){
          	cust.addAgent(new Agent("4"));
          }
          if (ag.equals("4")){
          	cust.addAgent(new Agent("3"));
          }*/
         // System.out.println("agents" +cust.getAgents());
          cust.setPayer(rs.getString("payer").trim());
          double ale  = rs.getDouble("ale");
          if (ale>0){cust.setDiscount(ale);}
         // cust.setDebt(rs.getDouble("debt"));
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }
          
          String ladu = rs.getString("ladu");
          if(ladu != null && ladu .trim().length()>0){
          	cust.setWare(ladu.trim());
          }
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


    String cmd3 = "SELECT d.idpartner AS maksja,k.code AS saaja, sum(d.totalamount) +sum(-m.amount) AS SUMMA  FROM sdinvoice d " +
    		" INNER JOIN grcompany k  on k.id = d.idgrcompany " +
          " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id WHERE docstate !='F' AND exchangerate=1  GROUP BY  d.idpartner,k.code ";

     if (isDebug()) System.out.println(cmd3);
        ResultSet rs1 = st.executeQuery(cmd3);
         while (rs1.next()) {
           String id = rs1.getString("saaja").trim();
           double debt =rs1.getDouble("SUMMA");
          // System.out.println(id + "  " + debt);
           Customer cust = (Customer) custMap.get(id);
           if (cust != null ) {
             cust.setDebt(debt);
            //  System.out.println(id + "  " + debt);
           }
         }

         st.close();
         con.close();


    } catch (SQLException e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='PDA'  ORDER BY name ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
  }

    



  public TreeMap getProducts(Agent agent){

    String cmd = "  SELECT pr.code AS kood,  rtrim(pr.name)  + ' ' + rtrim(pr.code) AS nimi," +
    		"hi.price AS hind,U_ean_teg AS EAN ,cat.idgrarticlecat AS grupp, " +
       "  pr.contentquantity AS tykke ,y.name AS yhik " +
       "  FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " + 
       "  hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
       "  RIGHT JOIN grunit y ON y.id=pr.idgrunit   " +
       "  WHERE hi.idgrpricelist=23 AND arca.category='PDA'  AND (articletype='G' OR articletype='P') AND right(pr.code,1)!='T' ";
    
    String cmd2 = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss " +
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore!=6 AND quantity!=0 " +
	        " GROUP BY  pr.code " ;

    //siin on k�sa kas k�ik laod liidame kokku 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode");
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          toode.setStock(sto);
          //toode.setMeasure(ost);
          tooted.put(id,toode);
        }

      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();
         Product prod = new Product(kood);
         String nimetus = rs.getString("nimi").trim();
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         prod.setName(nimetus);
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 7) {
           prod.setEAN(ean.trim());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }
         
         if(prod.getName().indexOf("alus") >0 || prod.getName().indexOf("1/2") >0  || prod.getName().indexOf("1/2") >0){
            prod.setDiffColor(true);
            }

         prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");

        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
        }
        products.put(nimetus, prod);
        //}
      }
      rs.close();
      st.close();
      con.close();

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }



  public Vector getCustomerProductsList(String agentCode){

    String sql1 = 	"SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice  " +
   " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc "+
   " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
   " WHERE r.docdate>getdate()-90 AND len(ltrim(d.idgrcompany))>0 " +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.code,r.quantity,r.docdate,r.unitprice ORDER BY k.code,t.code,r.docdate";

    
    String sql2 = " SELECT k.code AS klient,t.code AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
      "WHERE r.docdate>getdate()-90 AND len(ltrim(d.idgrcompany))>0 " +
      "AND (r.articletype='P' OR r.articletype='G') GROUP BY t.code,k.code ORDER BY t.code,k.code ";

           if (isDebug()) {
                System.out.println(sql1);
            }

            Vector custProds = new Vector();

            try {
              Connection con = getNewConnection();
              PreparedStatement stmt = con.prepareStatement(sql1);

              //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
              ResultSet	      rs = stmt.executeQuery();

              HashMap kliendid = new HashMap();
              while (rs.next()) {
                String kliendikood = rs.getString("klient").trim();
                String tootekood = rs.getString("toode").trim();
                double kogus = Double.parseDouble(rs.getString("kogus").trim());
                String lastdate = rs.getString("kuupaev").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," "),false);
                kliendid.put(kliendikood, cust);
                //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
                //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
                //CustProd cp = new CustProd(kliendikood, tootekood, kogus ,pluskogus ,minuskogus);
                //custProds.add(cp);
              }

              rs.close();
              stmt.close();

              stmt = con.prepareStatement(sql2);
              rs = stmt.executeQuery();
              while(rs.next()){
                String klient = rs.getString(1).trim();
                String toode = rs.getString(2).trim();
                double pkogus = rs.getDouble(3);
                double mkogus = rs.getDouble(4);
                Customer c = (Customer)kliendid.get(klient);
                if (c != null){
                  ArrayList al = (ArrayList)c.getProducts().get(toode);
                  if (al != null){
                    CustProd cp = (CustProd)al.get(1);
                    if (cp != null){
                      cp.setMovedStockPluss(pkogus);
                      cp.setMovedStockMinus(mkogus);
                      custProds.add(cp);
                      //System.out.println(cp.getProdID());
                    }
                  }
                }
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

  public void setBulkSales(Map agents){

    String cmd = "SELECT  cat.id AS agent ,SUM(r.amount)  AS summa, SUM(t.u_maht*r.quantity)AS summavol " +
    " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
   " INNER JOIN eremployee w ON w.id= d.idworker  " +
   " INNER JOIN grcompanycat cat  ON cat.code=w.code " +
   " INNER JOIN grarticle t ON t.id= r.idgrarticle" +
   " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND r.idgrunit!=1 " +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY cat.id";

       if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summa"));
                  agent.setMonthSalesVol((int)rs.getDouble("summavol"));
                 // System.out.println(agent.getMonthSales());
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
        }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {


       DealerProduct dealerProds = new DealerProduct();
       try {


          String  sql = " SELECT pr.code AS toode,(quantity) AS stock , ss.iddrstore AS ladu FROM dcurbalancestore ss " + 
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in ('601','600','607') AND quantity!=0 " +
	        " order BY  pr.code" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                  	double amo = dealerProds.getDealerProductStock(ladu,toode);
                   if(amo>0){
                   	dealerProds.remDealerProduct(ladu,toode);
                    dealerProds.add(ladu, toode,(amo+seis)+"" ,"","");
                   }else{
                    dealerProds.add(ladu, toode,seis+"" ,"","");     	
                   }
                  }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }

        return dealerProds;
        //return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = " SELECT d.idpartner AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
        " d.totalamount AS SUMMA ,sum(m.amount) AS minus ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
        " FROM sdinvoice d " +
        " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
        " WHERE docstate !='F' AND exchangerate=1 AND d.promptdate < getdate() +5 " +
        "  group by  d.sourcedocno,d.idpartner,d.promptdate,d.totalamount ORDER BY d.idpartner " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);

       while(rs.next()){
         Debt debt = new Debt();
         debt.setCustID(rs.getString("maksja").trim());
         debt.setDoknr(rs.getString("arve").trim());
         debt.setDuedate(rs.getDate("kuupaev"));
         
         double summa = rs.getDouble("summa");
         double minus = -rs.getDouble("minus");
         debt.setDue(rs.getString("paevi"));
         
         if( summa+minus > 1 || summa+minus < -1){
              debt.setSum(summa+minus);
           list.addElement(debt);
         }
         
       }
       rs.close();
       stmt.close();
       con.close();
       return list;
    }

    public ManagerData getManagerData() throws SQLException{
       ManagerData md = new ManagerData();
       return md;
     }


    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }
    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }

    public String getStartingTime() {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH,
              -Integer.parseInt(getParent().getProps().getProperty("months")));
      int month = cal.get(Calendar.MONTH) + 1;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      String sqlDate = cal.get(Calendar.YEAR) + month + day;
      return sqlDate;
    }

    public String getDriverVersion() {
      return "SAFWiru";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }



    public String getPilotDayData(Agent agent){
        return null;
    }

    public void insertDefective(DefectiveWrapper wr){

    }


    public void insertDistribution(DistEvalListWrapper wr){
    }

  

    public TreeMap getDealers() {
      return dealers;
    }

    public void setDealers(TreeMap aDealers) {
      dealers = aDealers;
    }

    public NumberFormat getNF() {
      return NF;
    }

    public void setNF(NumberFormat aNF) {
      NF = aNF;
    }


  private String url = "jdbc:odbc:SAF";
  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
