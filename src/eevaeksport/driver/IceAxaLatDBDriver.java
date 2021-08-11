package eevaeksport.driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.util.Converter;



public class IceAxaLatDBDriver extends AbstractDBDriver  {
  public IceAxaLatDBDriver(EksportPanel parent){
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

  
	
		public String getSaveStockInfo() {
			connectionUp();
	       String cmd = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
                 "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=0 " +
                 " AND dim.inventlocationid IN ('KM')  GROUP BY summ.itemid  ORDER BY summ.itemid ";

			if (isDebug())
				System.out.println(cmd);
			StringBuffer products = new StringBuffer();
			try {

				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(cmd);

				while (rs.next()) {

					String kood = rs.getString("toode").trim();
					double seis = rs.getDouble("stock");
					if (seis > 0) {
						products.append(kood + ";");
						products.append(" ;");
						products.append(getParent().getNF().format(seis) + "; "
								+ "\n"); // + dealer + "\n");
					}
				}
				rs.close();
				st.close();
			} catch (Exception e) {
				getParent().showError(e);
			}
			return products.toString();
		}



              public String getDealerCustomerCSV(String agent){
                return null;
               }

          	  public String getSaveDebtInfo() {      		
                return null;
        	   }

               
                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }

                                                  public String getAssortmentStr(Agent agent){
                                                  	return null;
                                                  }
  /**
   *
   */
  public void connectionUp(){
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, "bmssa", "bmssa_pwd");
  }

  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();

   
    System.out.println("V�LAD");
    return list;
  }


  /**
   *
   */
  public void connectionDown(){
  }

  public TreeMap getAgents(){
    String cmd = " select e.dimension2_  as id , e.name + ' ' + e.dimension2_ as nimi, " + 
				 " (select top 1 inventlocation from custtable c where c.dataareaid='bal' and  e.dimension2_=c.dimension2_)as obj" +
				 " from empltable e " +	  
    		     "   WHERE e.dataareaid='bal' and e.training != '' and dimension2_ != ''";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
        	
        	String id = rs.getString("id").trim();        
          String name = rs.getString("nimi").trim();
         
          Agent agent = new Agent(id);
          agent.setName(name);
          if(id.equals("106")){
        	    agent.setObject("L5");  
          }
          if(id.equals("108")){
      	    agent.setObject("L6");  
        }
          if(id.equals("109")){
      	    agent.setObject("LA");  
        }
          agent.setIsPilot(true);
          agents.put(name, agent);
          
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    return agents;
  }




      public String getDistribution() {
        return null;
      }



  public TreeMap getProdGroups() throws SQLException {


     TreeMap   tm = new TreeMap();
     String    sql = "SELECT ItemBuyerGroupId AS liik , ItemBuyerGroupId AS nimetus FROM  inventtable " +
                " WHERE dataareaid='bal' and ItemBuyerGroupId != '' Group by ItemBuyerGroupId";
    Statement stmt = this.getNewConnection().createStatement();
     ResultSet rs = stmt.executeQuery(sql);

     while (rs.next()) {
       String liik=rs.getString("liik").trim();
       String nimetus=rs.getString("nimetus").trim();
       if (liik != ""){
       tm.put(liik,Converter.enCodeEst(nimetus));
       }
     }
     rs.close();
     return tm;
 }

  public TreeMap getCustomers(Agent agent){
    String cmd = "SELECT cus.name, cus.accountnum," +
                 "(SELECT SUM(amountcur) FROM custtransopen co WHERE co.duedate<getdate()" +
                 " AND co.accountnum = cus.accountnum) AS debt, cus.linedisc ,cus.dimension2_ as agent " +
            " FROM custtable cus  WHERE  cus.dataareaid='bal' and blocked!=2 and closedcustomer=0  " +
            "and len(cus.dimension2_) >0 and name like '%j�tsi%'  and cus.dimension2_='" + agent.getID() + "'";



  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
           //HashMap custHash = new HashMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = rs.getString("name").trim();
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        cust.setName(Converter.enCodeEst(name));
        String debtStr = rs.getString("debt");
        if (debtStr != null){
//          cust.setDebt(Double.parseDouble(debtStr));
        }

        String ag = rs.getString("agent").trim();
		   cust.addAgent(new Agent(ag));   
      
          customers.put(name, cust);	                  
      }


    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }

  public TreeMap getProducts(Agent agent){

    String obj = agent.getObject(); //selle kaudu anname laokoodi edasi 
    
    
    String cmd = "SELECT inv.itemid, left(ItemBuyerGroupId,4) + ' ' + inv.itemname AS nimetus,inv.itemname" +
    		" as kontroll, inv.itemqtyinabox AS kastis,  disc.amount AS price " +
               ", NAMEALIAS AS ean ,ItemBuyerGroupId AS grupp ,MINAVERAGESETTLE as maxdisc FROM inventtable inv  " +
                 "INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid" +
                 " INNER JOIN inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid and inv.dataareaid=gr.dataareaid " +
                 " INNER JOIN pricedisctable disc ON inv.itemid=disc.itemrelation and inv.dataareaid=disc.dataareaid " +
                 "and (getdate()>=disc.fromdate OR disc.fromdate=('1900-01-01 00:00:00.00') )   and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') ) " +
                 " WHERE  disc.dataareaid='bal' AND disc.accountrelation='J_Abi' AND inv.dataareaid='bal'AND gr.dataareaid='bal' AND mod.dataareaid='bal' " +
                 "AND moduletype=2  " ;
                 
             String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
                 "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
                 "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and summ.dataareaid=inv.dataareaid " +
                 "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=0 " +
                 "AND dim.inventlocationid ='" + obj + "' GROUP BY summ.itemid  ORDER BY summ.itemid ";

             String cmd3 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ " +
             "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid " +
             "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=summ.dataareaid " +
             "WHERE summ.DATAAREAID='bal'and dim.DATAAREAID='bal' and inv.DATAAREAID='bal' AND CLOSED = 0 AND  moduletype=0 " +
             " AND dim.inventlocationid IN ('KS') GROUP BY summ.itemid  ORDER BY summ.itemid ";

    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    try {
    	
    	Connection  con2 = getNewConnection();
    	 Statement  stmta = con2.createStatement();
      ResultSet rs = stmta.executeQuery(cmd);
       HashMap prodHash = new HashMap();
      while(rs.next()){
        // if ( kood.length() > 2 ){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus =  rs.getString("nimetus").trim();
        if(kood.startsWith("X")){
        	nimetus = "x " + nimetus;
        }
        boolean goes = true ;
        if (nimetus.indexOf("***") != -1){
        	goes = false;
        }
        prod.setName(Converter.enCodeEst(nimetus));
        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price") / 1.2);
        prod.setEAN(rs.getString("ean"));
        prod.setGroup(parent.make(rs.getString("grupp")));
       
        
       // prod.setStock(0);
        prod.setVat("20");
        if(goes){
        	 products.put(nimetus, prod);
             prodHash.put(prod.getID(), prod); 	
        }
       
      }
      rs.close();
      stmta.close();
      
      Statement  stmtb = con2.createStatement();
      ResultSet rs1 = stmtb.executeQuery(cmd2);
                            while(rs1.next()){
                              String id = rs1.getString("toode").trim();
                              Product prod1 = (Product)prodHash.get(id);
                              if (prod1 != null){
                                prod1.setStock(rs1.getDouble("stock"));
                              }
                            }
                            rs1.close();
                            
                            rs1 = stmtb.executeQuery(cmd3);
                            while(rs1.next()){
                              String id = rs1.getString("toode").trim();
                              Product prod1 = (Product)prodHash.get(id);
                              if (prod1 != null){
                            	  double kog = rs1.getDouble("stock") ;
                            	  if(kog>0){
                                      prod1.setName(prod1.getName() + " |" + kog + "|");  
                            	  }
                                }
                            }
                            rs1.close();
                            stmtb.close();
                            con2.close();

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }


  public String getPilotDayData(Agent agent){


    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and summ.dataareaid=dim.dataareaid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
"AND CLOSED = 0  " +
"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
"inv.dataareaid='bal' AND sal.dimension2_='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";

       String sql2 =  "SELECT cus.salesid AS arve, cus.createddate AS kuupaev,cus.salesname AS klient  FROM  salestable cus " +
    " WHERE cus.dataareaid='bal' AND cus.dimension2_ ='" + agent.getID() + "' AND   cus.createddate > cast(getdate()-1 as datetime) " + 
   "  ORDER BY cus.createddate ";

    StringBuffer sb = new StringBuffer();
    sb.append("\n\n\n Vormistatud tellimused:\n\n");

      try {
                  ResultSet	      rs2 = stmt.executeQuery(sql2);
                  while (rs2.next()) {
                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") +  "\n");
                  }

        //stmt = con.prepareStatement(sql);
        //stmt.sets(1, agent.getObject());
          //stmt.setString(2, agent.getObject());
          ResultSet	      rs = stmt.executeQuery(sql);
//          sb.append("Nimi  \t\tLadu\t\tM��dud\t\tSumma\n");
          sb.append("---------------------------------------------------\n");

          double totalKokku = 0d;

          while (rs.next()) {
            int ladu = rs.getInt("ladu");
            int kogus = rs.getInt("kogus");
            double hind = round(rs.getDouble("myygihind"), 2);
            double kokku = round(kogus*hind, 2);
            totalKokku = round(totalKokku + kokku, 2);

            //sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

            //sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
              //kokku + "\n\n");
           }

          sb.append("---------------------------------------------------\n");
          sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");


                  } catch (Exception e) {
                      getParent().showError(e);
                  }


      return sb.toString();
  }



  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    if (true) return custProds ;

   String cmd = " SELECT jou.orderaccount,tra.itemid,tra.qty AS kogus ,jou.invoicedate AS lastdate " +
     "   FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
     "  tra.invoiceid=jou.invoiceid and tra.dataareaid=jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid=tra.dataareaid " +
     "  WHERE  jou.invoicedate > getdate()-30  GROUP BY jou.orderaccount,tra.itemid,tra.qty,jou.invoicedate " +
     "  ORDER BY jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate  " ;


    if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
       ResultSet rs = stmt.executeQuery(cmd);
   
      while(rs.next()){
        String kliendikood = rs.getString("orderaccount").trim();
        String tootekood = rs.getString("itemid").trim();
        double kogus = rs.getDouble("kogus");
        String lastdate = rs.getString("lastdate").substring(0,10);
                 lastdate = lastdate.substring(5,10);
            
        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");

        custProds.add(cp);
      }
    } catch (Exception e){
      getParent().showError(e);

    }
    return custProds;
  }





  public double getSalary(String agentCode) {

      double sales = 0;
      String cmd = "SELECT sum(lineamountmst) AS salary ,sal.dimension2_ as agent from  custinvoicetrans sal WHERE " +
	  " MONTH(sal.invoicedate) = MONTH(getdate()) AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
      " AND sal.dataareaid='bal' and sal.dimension2_ = ? group by sal.dimension2_ " ;

      try {
        PreparedStatement pst = con.prepareStatement(cmd);
          pst.setString(1, agentCode);
          ResultSet rs = pst.executeQuery();

          if(rs.next()) {
            sales = rs.getDouble("salary");
          }
      } catch (Exception e) {
             getParent().showError(e);
      }

      setSales(sales);
      return 0;
    }


  public void setBulkSales(Map agents){
this.connectionUp();
    String cmd =
        " SELECT sal.dimension2_ as agent ,sum(lineamountmst) AS summa from  custinvoicetrans sal WHERE " +
	  " MONTH(sal.invoicedate) = MONTH(getdate()) AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
      "  AND sal.dataareaid='bal' group by sal.dimension2_";
    
    
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

  


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "IceAxaLat";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }


  private String url = "jdbc:odbc:AXA";
  private Connection con;
  private Statement stmt;
  private String subjectFilesTo = null;
}
