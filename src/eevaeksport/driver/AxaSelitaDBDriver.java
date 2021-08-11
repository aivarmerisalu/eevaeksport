package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
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

public class AxaSelitaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaSelitaDBDriver(EksportPanel parent){
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
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;


  /**
   *
   */
  public void connectionUp(){
  }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getFixedAssortmentData() {
     return null;
   }

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
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
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
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

         Statement st = con.createStatement();
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

        String  sql = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu FROM INVENTSUM summ " +
               "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
               "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  "+
               "  WHERE summ.DATAAREAID='sel'and dim.DATAAREAID='sel' and inv.DATAAREAID='sel' AND CLOSED = 0 AND  moduletype=0 " +
               "    and dim.inventlocationid like'%PROD%' " +
               "  GROUP BY dim.inventlocationid,summ.itemid  ORDER BY dim.inventlocationid,summ.itemid ";

           if (isDebug()) System.out.println(sql);
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(sql);
                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  dealerProds.add(ladu, toode,seis+""," "," " );
              }
              rst.close();
              stm.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;
     }

  
  private TreeMap agent1 = new TreeMap();
  
  
  public TreeMap getAgents(){
    String cmd = "select num as kood , description AS name ,companygroup as ladu" +
    		"  from dimensions where  num like 'AG%' and dataareaid='sel'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
	
	  
    Properties propse =  new Properties();
    try{
    	propse =  EksportFrame.loadProps("emails.txt");
  	  
    }catch(Exception e){
    	e.printStackTrace();
    }

    agent1 = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agent.setObject(rs.getString("ladu").trim());
          
          agent.setLanguage("LIT");
          agent.setCountry("lt");
          agent.setPassWord("0000");
          String email = " ";
          
          if(propse.get(agent.getID()) != null){
        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
          }
             
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
          if(email.length() > 0){
        	  agent.setActive(true);
          }
        
          
          
          agents.put(name, agent);
          agent1.put(agent.getID(), agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = "  SELECT cus.name,cus.namealias,cus.accountnum,(case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end )AS payer, " +
            " (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate() "+
             "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end ) and co.dataareaid='sel') AS debt, cus.linedisc ,  " + 
             "  cus.address, '' AS visit ,cus.dimension4_ AS agent ,  numofdays AS term," +
             " (case when cus.CustGroup='LT-S' OR cus.CustGroup='LT-K' then  MandatoryCreditLimit else 0 end )  AS block , "+ 
			// " (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount) " +
			  "  0 as payerblock ,phone + ' '  + telefax as info,blocked as plokk FROM custtable cus LEFT JOIN paymterm  pay on cus.paymtermid=pay.paymtermid  "+
			 "	and  pay.dataareaid='sel'  WHERE  (cus.dataareaid='sel' AND blocked != 2 and  dimension4_ like  '%AG%' and len(cus.dimension)>0  )  " +
			  " order by cus.name " ;
 
           String sql = "SELECT  a.orderaccount AS klient, a.accountnum as payer,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
                    "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid " +
                    "WHERE co.dataareaid='sel' AND a.dataareaid='sel' and a.invoice != '' ORDER BY a.accountnum,co.transdate ";



  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
        
        if(name.indexOf("Kauno rajono centrinis vartotoj� kooperatyvas") >-1){
        	name = name.substring(0,name.indexOf("Kauno rajono centrinis vartotoj� kooperatyvas")) + "Kau.raj. cv koop." +
        	name.substring(name.indexOf("Kauno rajono centrinis vartotoj� kooperatyvas")+45) ;
        }
        String alias = rs.getString("namealias");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name = name + " " + alias;
        }
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(Converter.enCodeEst(name));
        cust.setPayer(rs.getString("payer"));
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
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
          cust.setAddress(Converter.enCodeEst(Converter.adressLink(aadr.trim().replace('\n',' '),"Lithuania")));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
        }

        int block = rs.getInt("block");
        if (block == 1) {
          cust.setSalesBlock(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if ( payerblock == 2 || payerblock == 1) {
          cust.setSalesBlock(true);
        }

    
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim());
        }
        
        int plokk = rs.getInt("plokk");
        if ( plokk == 2 || plokk == 1) {
          cust.setSalesBlock(true);
        }
        
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='sel' AND len(disc.itemrelation)=0  AND fromdate<=getdate()+1 and todate >= getdate()-1";
        ResultSet rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
        customers.put(name, cust);
        custMap.put(cust.getID(),cust);
      }

      
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim()); 
                        String payerId = rs.getString("payer").trim();
                        debt.setPayerID(payerId);
                        String number = rs.getString("number").trim();
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        number = number + "-" + getDateDDpMMStr( debt.getDate());
                        debt.setDoknr(number);
                        if(debt.getSum() > 0.01 || debt.getSum() < -0.01){
                            this.debt.addElement(debt);	
                        }
                      }


    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }

	public static String getDateDDpMMStr(Date ddd){
		Calendar cal = Calendar.getInstance();
		cal.setTime(ddd);
		StringBuffer sb = new StringBuffer();
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day +".");
		sb.append(month);		
	
		return sb.toString();
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
    String    sql = "   SELECT brandsubgroupid as liik,subgroupname as nimi FROM colinventbrandsubgroup  where dataareaid ='sel' ";
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

    //Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  /*  String    sql = " SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM inventtable WHERE dataareaid='tal' AND talROUTETYPEID!='' GROUP BY talROUTETYPEID";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
*/
    return tm;
  }

  public TreeMap getProducts(Agent agent){

    String cmd = "SELECT inv.itemid, inv.itemname AS nimetus, taxpackagingqty AS kastis,palletquantity_col AS alusel, disc.amount AS price " +
", bar.itembarcode AS ean ,brandsubgroupid_col AS grupp,mod.unitid As unit, taxitemgroupid as vat FROM pricedisctable disc " +
"left JOIN inventtable inv ON inv.dataareaid='sel' and " +
"inv.itemid=disc.itemrelation and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') ) and relation = 4 " +
"left JOIN inventtablemodule mod ON mod.itemid = inv.itemid AND mod.dataareaid='sel' LEFT JOIN inventitembarcode bar ON bar.itemid = inv.itemid AND " +
"bar.dataareaid='sel' WHERE disc.dataareaid='sel' and disc.accountrelation='MAZI' AND moduletype=2";
    
  
   String cmd2 = " SELECT   (case when itemcode = 0 then itemrelation else ''end) AS prodid,amount  AS price FROM pricedisctable where " +
				  "  ((fromdate<=getdate() and getdate()-1<= todate) OR  " +
				  " todate=('1900-01-01 00:00:00.00') ) and  relation= 5  and (case when accountcode = 1 then accountrelation else ''end)='MAZI' " +
                  " order by (case when itemcode = 0 then itemrelation else ''end)" ;

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        prod.setStock(0);
        String nimetus = kood.substring(1,3) + " " +rs.getString("nimetus").trim();
       if(nimetus.indexOf("mineraliz. geriam. vand." ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("mineraliz. geriam. vand." )) +
    	   nimetus.substring(nimetus.indexOf("mineraliz. geriam. vand." ) + 25);
    	   
       }
       
       if(nimetus.indexOf("mineralinis vanduo" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("mineralinis vanduo" )) +
    	   nimetus.substring(nimetus.indexOf("mineralinis vanduo" ) + 18);
    	   
       }
       
       
       
       if(nimetus.indexOf("geriamasis vanduo" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("geriamasis vanduo" )) +
    	   nimetus.substring(nimetus.indexOf("geriamasis vanduo" ) + 17);
    	   
       }
       
        
       
       if(nimetus.indexOf("Sausainiai" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Sausainiai" )) + "Saus" +
    	   nimetus.substring(nimetus.indexOf("Sausainiai" ) + 10);
    	   
       }
       
       
       
       if(nimetus.indexOf("Skalbimo milteliai spalv. dr." ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Skalbimo milteliai spalv. dr." )) + "Skalb.milt" +
    	   nimetus.substring(nimetus.indexOf("Skalbimo milteliai spalv. dr." ) + 30);   
       }
       
       

       if(nimetus.indexOf("nat�ralus" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("nat�ralus" )) + "Nat." +
    	   nimetus.substring(nimetus.indexOf("nat�ralus" ) + 9);   
       }
       
       
       

       if(nimetus.indexOf("Sirupas" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Sirupas" )) + "Srp." +
    	   nimetus.substring(nimetus.indexOf("Sirupas" ) + 7);   
       }
       
       if(nimetus.indexOf("�okoladas" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("�okoladas" )) + "�ok." +
    	   nimetus.substring(nimetus.indexOf("�okoladas" ) + 9);   
       }
       
       
       
       if(nimetus.indexOf("saldainiai" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("saldainiai" )) + "Sald" +
    	   nimetus.substring(nimetus.indexOf("saldainiai" ) + 10);   
       }
       
       
       
       if(nimetus.indexOf("Meduoliai" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Meduoliai" )) + "Meduo" +
    	   nimetus.substring(nimetus.indexOf("Meduoliai" ) + 9);   
       } 
       
       
       

       if(nimetus.indexOf("Majonezas" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Majonezas" )) + "Majon" +
    	   nimetus.substring(nimetus.indexOf("Majonezas" ) + 9);   
       } 
       
       nimetus = nimetus.replaceAll("\"", "");
       if(nimetus.indexOf("Makaronai" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Makaronai" )) + "Makar" +
    	   nimetus.substring(nimetus.indexOf("Makaronai" ) + 9);   
       } 
       
       if(nimetus.indexOf("Prieskoni� mi�inys" ) >-1){
    	   nimetus= nimetus.substring(0,nimetus.indexOf("Prieskoni� mi�inys" )) + "Piesk.Mi�" +
    	   nimetus.substring(nimetus.indexOf("Prieskoni� mi�inys" ) + 18);   
       }
       
        prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));
        
        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
        nimetus =  "  " + nimetus  +  " ";
     }

      prod.setName(Converter.enCodeEst(nimetus));

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat(vat.trim());
    }
      
    /* String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>0){
            // prod.setGroupSecond(grupp2);
          }

          

          //   prod.setInUnit(rs.getDouble("yhikukogus"));
*/
        products.put(nimetus, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();


      rs = stmt.executeQuery(cmd2);
      while(rs.next()){
    	  String kood = rs.getString("prodid");
    	  double info = rs.getDouble("price");
    	  System.out.println(" siin " + kood + " " + info );
    	  
    	  if(prodHash.get(kood.trim()) != null){
    		  System.out.println(" siin2 " + kood + " " + info );
    		  Product prod = (Product) prodHash.get(kood.trim());
    		  prod.setStatusDiscount("1");
    		  prod.setName(prod.getName() + " %" + parent.getNF().format(info) + "%");
    	  }
    	  
      }
      
    //  System.exit(0);
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    //if (true) return custProds ;
 int counter = 1;
   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate " +
      " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
      " tra.invoiceid=jou.invoiceid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid " +
      " WHERE  jou.invoicedate > getdate()-180 AND tra.dataareaid ='sel'  AND jou.dataareaid = 'sel'  AND cus.dataareaid = 'sel' " +
      " GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
      " ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " ;


    if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
        String kliendikood = rs.getString("orderaccount").trim();
        String tootekood = rs.getString("itemid").trim();
        double kogus = rs.getDouble("qty");
        String lastdate = rs.getString("lastdate").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                 
        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");

        //cp.setRedline(rs.getString("date"));
        custProds.add(cp);
      }
    } catch (Exception e){
      getParent().showError(e);
      if(counter< 4){
      	try{
        	 ResultSet rs = stmt.executeQuery(cmd);
             counter++;
             while(rs.next()){
               String kliendikood = rs.getString("orderaccount").trim();
               String tootekood = rs.getString("itemid").trim();
               String lastdate = rs.getString("lastdate").substring(0,10);
                        lastdate = lastdate.substring(5,10);
               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

               cp.setRedline(rs.getString("date"));
               custProds.add(cp);
             }
      	}catch (Exception ee){

      	}

      }
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
      
      String cmd ="SELECT  jou.dimension4_ as agent,round(sum(lineamount),0) AS summa FROM salestable jou inner join salesline  l " +
      		"on l.salesid=jou.salesid  " +
  " WHERE  jou.dataareaid='sel'  and l.dataareaid='sel' AND YEAR(jou.deliverydate) = YEAR(getdate())  " +
  "     AND MONTH(jou.deliverydate) = MONTH(getdate())  GROUP BY jou.dimension4_";
      
      cmd = " SELECT  sal.dimension4_ as agent,round(SUM(lineamount),0) AS summa " + 
      		"   FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid  INNER JOIN custinvoicejour jou with (nolock) ON   " + 
      		"   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus with (nolock) ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
      		"   inv.dataareaid='sel'    AND YEAR(sal.invoicedate) = YEAR(getdate())  " + 
      		"    AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY sal.dimension4_";
      
      
      cmd = "\r\n" + 
      		"SELECT  sal.dimension4_ as agent,round(SUM(lineamount),0) AS summa \r\n" + 
      		"   FROM custinvoicetrans sal \r\n" + 
      		"INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid  \r\n" + 
      		"INNER JOIN inventDim ind with (nolock) ON sal.inventDimid=ind.inventdimid \r\n" + 
      		"									  and ind.dataareaid= sal.dataareaid  \r\n" + 
      		"									  and ind.inventLocationId <> ''  \r\n" + 
      		"INNER JOIN custinvoicejour jou with (nolock) ON   \r\n" + 
      		"   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid \r\n" + 
      		"INNER JOIN custtable cus with (nolock) ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE \r\n" + 
      		"   inv.dataareaid='sel'    AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
      		"    AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY sal.dimension4_";

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
    return "AxaSelita";
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





       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:AXA";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
