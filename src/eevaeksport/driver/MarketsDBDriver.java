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

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class MarketsDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public MarketsDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
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
        /*  String sql = " SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, " +
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
          return null;
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
	  	  
	  
	  String  cmd = " SELECT summ.itemid AS toode ,dim.inventlocationid as ladu , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
      "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
      "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  "+
      "  WHERE summ.DATAAREAID='sel'and dim.DATAAREAID='sel' and inv.DATAAREAID='sel' AND CLOSED = 0 AND  moduletype=0 " +
      "    and dim.inventlocationid like'%PROD%' " +
      "  GROUP BY dim.inventlocationid,summ.itemid  ORDER BY dim.inventlocationid,summ.itemid ";


	  

       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
         Connection cc = this.getNewConnection();
         Statement st = cc.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
           if(seis > 0){
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
             }
           }
           rs.close();
           st.close();
           cc.close();
       } catch (Exception e) {
           getParent().showError(e);
       }
       return products.toString();
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

  
  public DealerProduct getDealerProductsList() {

  
      DealerProduct dealerProds = new DealerProduct();
      try {

        String  sql = " SELECT barcode13 AS toode ,(AVAILABLE) AS stock ,6 as ladu FROM stock_in_series_stores st  " +
                " INNER JOIN goods pr ON pr.CD_GOOD=st.LK_GOOD  where lk_store in (6,12) and AVAILABLE >0";

           if (isDebug()) System.out.println(sql);
              Connection cc = this.getNewConnection();
              Statement stm = cc.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  dealerProds.add(ladu, toode,seis+""," "," " );
              }
              rst.close();
              stm.close();
              cc.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;
     }

  
  
  
  public TreeMap getAgents(){
    String cmd = " select car_name as kood , name AS name from delivery_agents where car_name != '' ";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
    	
    	Connection cc = this.getNewConnection();
        Statement stm = cc.createStatement();
        ResultSet rs = stm.executeQuery(cmd);
     
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agent.setObject("6");
          agents.put(name, agent);
        }
        rs.close();
        stm.close();
        cc.close();
        
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = " SELECT  per.company as name , per.buyer_card_no as kood, per.lk_gl_person AS payer, " + 
           "  0 AS debt, per.address , ag.car_name AS agent , per.buyer_pay_days AS term,  " +
	    "	  I_SELL as block ,per.busnessno as info  from persons per left join  delivery_agents " +
	    " ag on ag.CD_AGENT=per.lk_delivery_agent where per.LK_PERSON_GROUP in (2,3,79, 80, 81,82,83,84) " +
	    " and per.buyer_card_no not like '%NOKL.NR%' " ;


 

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	Connection cc = this.getNewConnection();
    	Statement stmt = cc.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
     
      while(rs.next()){
        String name = rs.getString("name").trim();
        
    
        String accountnum = rs.getString("kood").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer != null ){
        	 cust.setPayer(payer.trim());
        }
       
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }


        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Latvia"));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        String agen = rs.getString("agent");
        if (agen != null && agen.trim().length()>0) {
          cust.addAgent(new Agent(agen.trim()));
        } else {
        	Enumeration enums = getParent().getAgentsHash().elements();
       	  while (enums.hasMoreElements()) {
       	    Agent a = (Agent) enums.nextElement();
       	    cust.addAgent(a);
       	   }
        }
     int term = rs.getInt("term");
   
     if(term >0){
    	 cust.setPaymTerm(term + "");
     }
     
        int payerblock = rs.getInt("block");
        if ( payerblock == 0 ) {
          cust.setSalesBlockT(true);
        }

        
    
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim());
        }
        
        String sql3 = " SELECT   cdue_total AS tasumata FROM gl_documents d INNER JOIN persons per ON per.CD_PERSON=d.LK_PERSON  " + 
			"	WHERE    D.DOC_TYPE like '%ins pirc%' and  D.cdue_total!= 0.00 and  D.STORNED = 0 and doc_date> current_timestamp-180  " +
		"		and due_date <= current_timestamp and per.buyer_card_no = '" + cust.getID() + "'";
        Connection ccm = this.getNewConnection();
        Statement stm = ccm.createStatement();
        
        ResultSet rs1 = stm.executeQuery(sql3);
        while(rs1.next()){
          double ale = rs1.getDouble("tasumata");
          cust.setDebt(cust.getDebt() + ale);
        }
       rs1.close();
       stm.close();
       ccm.close();
        
   if(cust.getID().trim().length() >0){
       customers.put(name, cust);
       custMap.put(cust.getID(),cust);   
   }
 
      }
      rs.close();
      
      String sql = "	  SELECT  per.buyer_card_no AS klient,stockgenref AS number,due_date AS maksepaev, cdue_total AS tasumata, " +
                  "  doc_date AS kuupaev , datediff(day, due_date , current_date) AS yle FROM gl_documents d INNER JOIN persons per ON per.CD_PERSON=d.LK_PERSON   " +
			"	WHERE    D.DOC_TYPE like '%ins pirc%' and  D.cdue_total!= 0.00 and  D.STORNED = 0 and doc_date> current_timestamp-180 ";

            rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("number");
                        if(number!= null){
                        	 debt.setDoknr(number);	
                        }
                       
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue((int)rs.getDouble("yle") + "");
                        if(number!= null && (debt.getSum() > 0.1 || debt.getSum() < -0.1)){
                        	 this.debt.addElement(debt);
                        }
                       
                      }
                      stmt.close();
                      cc.close();
                      

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
	  
  isTreeGroup();
  setTreeGroup(true);
    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "  SELECT code as liik,description as nimi FROM good_sec_groups where PARENT_NODE =-1 ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, Converter.enCodeEst(rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

	   Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = "  SELECT code as liik,description as nimi FROM good_sec_groups where PARENT_NODE !=-1 ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	        tm.put(liik, rs.getString("nimi").trim());
	    }
	    rs.close();
	    st.close();
	    con.close();

    return tm;
  }

  public TreeMap getProducts(Agent agent){

    String cmd = "SELECT pr.barcode13 as itemid, pr.description AS nimetus, pr.units_in_pack AS kastis, pr.price1 as price  " +
   " , pr.price2 as price2  " +
   " , pr.price3 as price3  " +
   " , pr.price4 as price4  " +
   " , pr.price5 as price5  " +
   " , pr.price6 as price6  " +
   " , pr.price7 as price7  " +
   " , pr.price8 as price8  " +
                   " , pr.barcode13 AS ean ,gr.code AS grupp,pr.msr_units As unit, pr.tax_rate as vat ,COMPLECTATION_CLASS  as optimum , MIN_SALE_AMOUNT as minimum FROM  " +
                 " goods pr left join good_sec_groups gr on gr.cd_GOOD_SEC_group=pr.lk_good_sec_group where (not_liquid is null or not_liquid ='0')  " + 
				 " and SUBSTRING(gr.code from 1 for 3) not in  ('ALK','ZZZ','CIG','VEI') and pr.barcode13 is not null ";
    
  

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
   Connection cc = this.getNewConnection();
    	Statement stmt =  cc.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim();
           if(nimetus.indexOf("akcija") != -1){
        	   prod.setStatusDiscount("1");
           }
         
        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));
        double price2 = rs.getDouble("price2");
        double price3 = rs.getDouble("price3");
        double price4 = rs.getDouble("price4");
        double price5 = rs.getDouble("price5");
        double price6 = rs.getDouble("price6");
        double price7 = rs.getDouble("price7");
        double price8 = rs.getDouble("price8"); 
        prod.setPartii(" ");
        if(price2 > 0 ){
        	prod.setPartii("P2 : " + this.getNF().format(price2) + " €|");
        }

        if(price3 > 0 ){
        	prod.setPartii(prod.getPartii() + "P3 : " + this.getNF().format(price3) + " €|");
        }
       
        if(price4 > 0 ){
        	prod.setPartii(prod.getPartii() + "P4 : " + this.getNF().format(price4) + " €|");
        }
        if(price5 > 0 ){
        	prod.setPartii(prod.getPartii() + "P5 : " + this.getNF().format(price5) + " €|");
        }
        if(price6 > 0 ){
        	prod.setPartii(prod.getPartii() + "P6 : " + this.getNF().format(price6) + " €|");
        }
        if(price7 > 0 ){
        	prod.setPartii(prod.getPartii() + "P7 : " + this.getNF().format(price7) + " €|");
        }
        if(price8 > 0 ){
        	prod.setPartii(prod.getPartii() + "P8 : " + this.getNF().format(price8) + " €|");
        }
       
        
        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
      String gr = "";
       if(grupp!=null && grupp.trim().length()>2){
        prod.setGroup(grupp.trim().substring(0,3));
        gr = grupp.trim().substring(0,2);
        if(grupp.trim().length()>3){
        	prod.setGroupSecond(grupp.trim());
        }
       
     }
       nimetus = "     "  + gr + " " + nimetus.replaceAll("\"", "") + " " + kood;
      prod.setName(Converter.enCodeEst(nimetus));

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit.trim());
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat(vat.trim());
    }
      
      
      String optimum = rs.getString("optimum");
      if(optimum!=null && optimum.trim().length()>0){
    	  prod.setOptAmountInUnits(Double.parseDouble(optimum.trim()));
      }

      
      String minimum = rs.getString("minimum");
      if(minimum!=null && minimum.trim().length()>0){
       prod.setMinAmountInUnits(Double.parseDouble(minimum.trim()));
      }
        products.put(nimetus, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      stmt.close();
      cc.close();


      
      
      String  sql = " SELECT barcode13 AS toode ,(AVAILABLE) AS stock ,6 as ladu FROM stock_in_series_stores st  " +
      " INNER JOIN goods pr ON pr.CD_GOOD=st.LK_GOOD  where lk_store in (6,12) and AVAILABLE >0";

 if (isDebug()) System.out.println(sql);
   Connection cd = this.getNewConnection();
    Statement stm = cd.createStatement();
    ResultSet rst = stm.executeQuery(sql);

      while (rst.next()) {
        String toode = rst.getString("toode").trim();
        int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
       if( prodHash.get(toode) != null){
    	   ((Product)prodHash.get(toode)).setStock(seis);
          } 
       } 
    rst.close();
    stm.close();
    cd.close();

    
  
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }



  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
 
   String cmd = "SELECT per.buyer_card_no as klient,pro.barcode13 as toode ,rid.amount,dok.invoice_date  AS lastdate " +
      " FROM invoices dok  INNER JOIN invoice_items rid ON " +
      " dok.CD_INVOICE=rid.LK_INVOICE INNER JOIN goods pro ON pro.CD_GOOD=rid.LK_GOOD " +
	  " INNER JOIN persons per ON dok.LK_PERSON=per.CD_person  " +
      " WHERE  dok.invoice_date > current_timestamp-180 and per.buyer_card_no not like '%NOKL.NR%' " +
	  " and SUBSTRING(pro.grp_code from 1 for 3) not in  ('ALK','ZZZ','CIG','VEI') and pro.barcode13 is not null " +
      " GROUP BY per.buyer_card_no,pro.barcode13 ,dok.invoice_date ,rid.amount " +
      " ORDER BY per.buyer_card_no,pro.barcode13,dok.invoice_date ,rid.amount " ;


    if (isDebug()) System.out.println(cmd);

    try {
  Connection cc =  this.getNewConnection();
    	Statement stmt = cc.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
  
      while(rs.next()){
        String kliendikood = rs.getString("klient").trim();
        String tootekood = rs.getString("toode").trim();
        double kogus = rs.getDouble("amount");
        String lastdate = rs.getString("lastdate").substring(0,10);
                 lastdate = lastdate.substring(5,10);
        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
        custProds.add(cp);
      }
      rs.close();
      stmt.close();
      cc.close();
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
    
      /*SELECT  jou.dimension4_ as agent,round(sum(invoiceamount),0) AS summa
        FROM custinvoicejour jou 
  WHERE  jou.dataareaid='sel' 
  AND YEAR(jou.invoicedate) = YEAR(getdate()) 
        AND MONTH(jou.invoicedate) = MONTH(getdate()) 
         GROUP BY jou.dimension4_*/
      
      String cmd =" select car_name as agent , sum(g_taxable) as summa  from invoices inv inner join  delivery_agents ag on ag.CD_AGENT=inv.lk_delivery_agent " +
	  " where car_name is not null and extract(month from invoice_date)= extract(month from current_timestamp)  AND extract(year from invoice_date)= extract(year from current_timestamp) " +
	  " group by car_name ";

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
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
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
    return "Markets";
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
  	          sb.append("\tKuu müük kokku: " + totalKokku + " Kg." + totalSumma + " EUR");


  	                  } catch (Exception e) {
  	                      getParent().showError(e);
  	                  }


  	      return sb.toString();
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
  private String url = "jdbc:odbc:MARKETS";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
