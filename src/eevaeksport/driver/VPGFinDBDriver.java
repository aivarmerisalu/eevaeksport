package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.OrderFormReader;
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

public class VPGFinDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter {
  public VPGFinDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
    }
  }


  public void connectionUp(){
  }


  public String getSaveDebtInfo() {
   	return null;
   }

  public String getProductSortingData(Vector done){
   	StringBuffer buf = new StringBuffer();
   	
   	String filename =  getParent().getProps().getProperty("tilauslomake");
   	//System.out.println("SorditudTooted" + filename);
		try {
			 OrderFormReader ofr = new OrderFormReader(filename);
			 
			 Vector itemNumbers = ofr.getItemNumbers();
			 Enumeration enums =  itemNumbers.elements();
			  while (enums.hasMoreElements()){
			  	String prodID = (String) enums.nextElement();	
			  	if(prodID.trim().length()>0 && prodID.trim().length()<8 && !prodID.equals("KOIRAT")){
					//System.out.println("SorditudTootedins " + prodID);
			  		buf.append("so;" + prodID.trim() + "\n");	
				}				
			  }
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
   	
		
		
		
	System.out.println(buf.toString());	
   	return buf.toString();
   }
   
  
  
   public Vector getFixedAssortmentData() {
     return null;
   }

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          String sql = " SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, " +
				 "  (case when accountcode = 1 then accountrelation else ''end)  AS clgroup, "+
				 "  (case when itemcode = 0 then itemrelation else ''end) AS prodid, " +
				 "  (case when itemcode = 1 then itemrelation else ''end) AS prodgroup,percent1 AS discount,amount*100 " +
                  "  AS price FROM pricedisctable where (case when accountcode = 1 then accountrelation else ''end) not in ('intern') AND " +
				  "  fromdate<=sysdate and( todate>=sysdate or to_char(todate) = '01.01.1900') and dataareaid='vsf' " +
				  "order by (case when accountcode = 1 then accountrelation else ''end) " +
				"	,(case when accountcode = 0 then accountrelation else ''end) " ;


          try {


        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                          String custID = rs.getString("custid");
                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price")/100;
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
  	//return null;
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
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
    String cmd = " select groupid,name from bmssa.commissionsalesgroup where dataareaid = 'vsf'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("groupid").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          agents.put(name, agent);
          agents1.put(agent.getID(), agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }

	String agentStr = getParent().getProps().getProperty("menekinedist�j�");

	if(agentStr != null){

	    StringTokenizer st = new StringTokenizer(agentStr, ",");
	       while(st.hasMoreTokens()){
	       	Agent agent = new Agent(st.nextToken());

			String agentAGENTS = getParent().getProps().getProperty(agent.getID());
     	 if(agentAGENTS != null){

			StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
		    agent.setName(stag.nextToken());
			//agent.setManager(true);

		       while(stag.hasMoreTokens()){
		       	Agent aa = (Agent)agents1.get(stag.nextToken());
                aa.addDubbler(agent.getID());
                aa.addDubbler(aa.getID());
		       }
				agents.put(agent.getName(), agent);
				agents1.put(agent.getID(), agent);
	       }

	     	 }
	}


    return agents;
  }

  public TreeMap getCustomers(Agent agent){

 String cmd = " SELECT cus.name, cus.accountnum,(case when length(cus.invoiceaccount)<2 then cus.accountnum else cus.invoiceaccount end )AS payer, " +
            " (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<sysdate " +
             "   AND co.accountnum = cus.accountnum  AND co.dataareaid='vsf') AS debt, cus.linedisc ,  " +
              "    cus.address, '' AS visit ,salesgroup AS agent ,  payment AS term,    blocked AS block " +
               "  , pricegroup AS grupp FROM bmssa.custtable cus  WHERE  cus.dataareaid='vsf' AND blocked != 2  and length(salesgroup)>0" +
               " and length(trim(cus.accountnum))>4 order by name " ;

           String sql = "SELECT  a.accountnum AS klient,a.invoice AS num,co.duedate AS maksepaev, co.amountmst AS tasumata, " +
                 "   co.transdate AS kuupaev , co.duedate-sysdate AS yle FROM bmssa.custtransopen co ,  bmssa.custtrans a " +
                  "  WHERE  co.REFRECID=a.recid  AND co.duedate < sysdate -5 AND co.duedate > sysdate -300  AND co.dataareaid='vsf'  AND a.dataareaid='vsf'  and a.accountnum !='     " +
                  "    1' and  (co.amountmst>1 or  co.amountmst<-1) "+
               " ORDER BY a.accountnum,co.transdate ";

           String cmd3 ="  SELECT  cus.accountnum as klient,round(SUM(lineamount),0)  -round(SUM(-costamountposted),0) AS summa  " +
  		  "FROM bmssa.custinvoicetrans sal INNER JOIN  bmssa.inventtable inv ON sal.itemid=inv.itemid INNER JOIN  bmssa.custinvoicejour jou ON " +
         "  sal.invoiceid=jou.invoiceid  INNER JOIN  bmssa.custtable cus ON cus.accountnum = jou.orderaccount  INNER JOIN inventtrans idi ON " +
         "sal.inventtransid = idi.inventtransid   WHERE  " +
         "  inv.dataareaid='vsf' AND sal.dataareaid='vsf'  AND cus.dataareaid='vsf'  AND  " +
         "  idi.dataareaid='vsf'  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'YYYY')) " +
         " = TO_NUMBER(TO_CHAR(sysdate, 'YYYY')) " +
         "  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'MM'))= TO_NUMBER(TO_CHAR(sysdate, 'MM')) " +
         " GROUP BY cus.accountnum ";


           String cmd4 =
            "  SELECT  cus.accountnum as klient,round(SUM(lineamount),0)  -round(SUM(-costamountposted),0) AS summa  " +
  		  "FROM bmssa.custinvoicetrans sal INNER JOIN  bmssa.inventtable inv ON sal.itemid=inv.itemid INNER JOIN  bmssa.custinvoicejour jou ON " +
         "  sal.invoiceid=jou.invoiceid  INNER JOIN  bmssa.custtable cus ON cus.accountnum = jou.orderaccount  INNER JOIN inventtrans idi ON " +
         " sal.inventtransid = idi.inventtransid   WHERE  " +
         "  inv.dataareaid='vsf' AND sal.dataareaid='vsf'  AND cus.dataareaid='vsf'  AND  " +
         "  idi.dataareaid='vsf'  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'YYYY')) " +
         " = TO_NUMBER(TO_CHAR(sysdate, 'YYYY'))-1 " +
         "  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'MM'))= TO_NUMBER(TO_CHAR(sysdate, 'MM')) " +
         " GROUP BY cus.accountnum ";
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = rs.getString("name").trim();
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
        cust.setSold(0);
        cust.setName(name + " " + accountnum );
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length()>0){
        	cust.setPayer(payer.trim());
        }

        String debtStr = rs.getString("debt");
        if (debtStr != null){
        	double xx = Double.parseDouble(debtStr.replace(',','.'));
        	if(xx>1 || xx<-1){
                cust.setDebt(xx);
        	}
        }

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
           // cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(aadr.trim().replace('\n',' '));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String ag = rs.getString("agent");

    	if (ag != null && ag.trim().length()>0){
    		cust.addAgent(new Agent(ag.trim().replaceAll("\n"," ").toLowerCase()));
			Agent agg = ((Agent)this.getAgents1().get(ag.trim()));

			if(agg != null && agg.getDubbler().size()>0){
				for (int i = 0 ; i < agg.getDubbler().size() ; i++){
					cust.addAgent(new Agent((String)((Agent)this.getAgents1().get(ag.trim().replaceAll("\n"," ").toLowerCase())).getDubbler().elementAt(i)));
				}

			}
		}

        if(cust.getID().equals("newcust")){
            Iterator iter = getAgents().values().iterator();
            while (iter.hasNext()){
                  Agent a = (Agent) iter.next();
                cust.addAgent(a);
                }

            }
        int block = rs.getInt("block");
        if (block != 0) {
          cust.setSalesBlock(true);
        }

        String gr = rs.getString("grupp");
        if (gr != null && gr.trim().length()>1) {
          cust.setGroup(gr.trim().toLowerCase());
        }
        if(cust.getID().length()>5){ // pasi soov dets 2007
            customers.put(cust.getName(), cust);
            custMap.put(cust.getID(),cust);	
        }
      }

                      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("num").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        String yle = rs.getString("yle");
                        if(yle.indexOf(",")!= -1){
                        	debt.setDue(yle.substring(0,yle.indexOf(",")));

                        }
                        if(debt.getSum()> 1 || debt.getSum()< -1){
                        	 this.debt.addElement(debt);

                        }
                        }


                      rs = stmt.executeQuery(cmd3);
                      while(rs.next()){
                      	Customer cust = (Customer) custMap.get(rs.getString("klient").trim());
                      	if(cust != null){
                      		cust.setSold(rs.getDouble("summa"));
                      	}
                      }

                      rs = stmt.executeQuery(cmd4);
                      while(rs.next()){
                      	Customer cust = (Customer) custMap.get(rs.getString("klient").trim());
                      	if(cust != null){
                      		cust.setBudget(rs.getDouble("summa"));
                      	}
                      }



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



    TreeMap   tm = new TreeMap();
    //setTreeGroup(true);
    try {
       FileReader fr = new FileReader("ryhmat.txt");
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   while(((line = reader.readLine())!= null) ){
      StringTokenizer st = new StringTokenizer(line, ";");
       if (st.hasMoreTokens()){
         String id = (st.nextToken());
               if (st.hasMoreTokens()){
                String name = (st.nextToken());
                tm.put(id,id.substring(0,1)+ " " + name);

           }
         }
       }
     } catch (IOException e){
       e.printStackTrace();
     }


  	/*
    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql2 = "SELECT itemgroupid AS liik, TXT AS nimetus FROM bmssa.vpg_inventitemgrouptxt" +
    		" gr WHERE dataareaid='vsf' and languageid = 'fi'";
    String    sql = " SELECT itemgroupid AS liik, name AS nimetus FROM bmssa.inventitemgroup gr WHERE dataareaid='vsf'";

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      if(liik.length()>3){
      	liik = liik.substring(0,3);
      }
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
*/
    tm.put("xxxx", "Muut");
    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT brandid AS liik, name AS nimetus FROM bmssa.vpg_brandtable gr WHERE dataareaid='vsf'";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();
    tm.put("YYYY", "Muut");
    return tm;
  }

  public TreeMap getProducts(Agent agent){
   TreeMap tmg = new TreeMap();
   Vector mm = new Vector();
   Vector done = new Vector(); 
  	try{
    FileReader fr = new FileReader("ryhmat.txt");
    BufferedReader reader = new BufferedReader(fr);
     String line = null;

  while((line = reader.readLine()) != null ){
     StringTokenizer st = new StringTokenizer(line, ";");
      if (st.hasMoreTokens()){
        String id = (st.nextToken());
              if (st.hasMoreTokens()){
         
               while(st.hasMoreTokens()){
               	tmg.put(st.nextToken(),id);
               }

          }
        }
      }


  FileReader fr2 = new FileReader("mm.txt");
  BufferedReader reader2 = new BufferedReader(fr2);
   String line2 = null;

while((line2 = reader2.readLine()) != null ){
   StringTokenizer st = new StringTokenizer(line2, ";");
    if (st.hasMoreTokens()){
    	mm.addElement(st.nextToken().trim());
    }
}

    } catch (IOException e){
      e.printStackTrace();
    }



    String param = parent.getProps().getProperty("days");
    if(param == null ){
    	param = "120";
    }


    String sold = "SELECT  tra.itemid AS toode  FROM bmssa.custinvoicetrans tra INNER JOIN bmssa.custinvoicejour jou ON " +
     "  tra.invoiceid=jou.invoiceid " +
     " WHERE  jou.invoicedate >sysdate-" + param + "   AND tra.dataareaid='vsf' and jou.dataareaid='vsf' " +
     " GROUP BY tra.itemid " ;



  	Vector sol = new Vector();


    String cmd = "SELECT inv.itemid,(inv.itemname) AS nimetus, '' AS kastis,  mod.price AS price " +
              ", inv.barcode AS ean,itemgroupid AS grupp ,vpg_brandid AS grupp2,bomunitid AS unit, '' AS yhikukogus," +
              "substr(taxitemgroupid,4,5) AS vat,quantity AS minunit FROM bmssa.inventtable inv  " +
              "   INNER JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid " +
              "    INNER JOIN bmssa.inventitemgroup gr ON inv.itemgroupid= gr.itemgroupid  " +
              "   WHERE inv.dataareaid='vsf' AND gr.dataareaid='vsf' AND mod.dataareaid='vsf' " +
              "   AND moduletype=2  " +
              "   AND length(inv.itemname)>0  AND blocked = 0  ";
             String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,SUM(postedvalue) AS laohind  FROM  bmssa.INVENTSUM summ " +
                " INNER JOIN bmssa.INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
                " INNER JOIN  bmssa.inventtablemodule inv ON inv.itemid=summ.itemid "+
                " WHERE summ.DATAAREAID='vsf'and dim.DATAAREAID='vsf' and inv.DATAAREAID='vsf' AND CLOSED = 0 AND  moduletype=0 "+
                " AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid )" +
                " GROUP BY summ.itemid  ORDER BY summ.itemid " ;






             if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    try {

   TreeMap tooted2 = new TreeMap();
        ResultSet rs1 = stmt.executeQuery(cmd2);
        while(rs1.next()){
          String id = rs1.getString("toode").trim();
           Toode tt = new Toode(id);
            tt.setKogus(rs1.getDouble("stock"));
            tt.setKastis(rs1.getDouble("laohind"));
         tooted2.put(id,tt);

        }





    	Statement stmt4 =  this.getNewConnection().createStatement();
      ResultSet rs4 = stmt4.executeQuery(sold);
      while(rs4.next()){
      	sol.addElement(rs4.getString("toode").trim());
      }

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);



        String nimetus = rs.getString("nimetus").trim();

        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));

        Toode ss = (Toode) tooted2.get(kood);
        if (ss == null){
        	prod.setStock(0);
        }else{
        	prod.setStock(ss.getKogus());
            double ccc = ss.getKastis();
            if(ccc != 0){
            	ccc = ccc/ss.getKogus();
            	int ii = (int)((1-ccc/prod.getPrice())*100);
           if(ii >0 && ii <101){
	         
	         prod.setMargin(ii+"");
             }
              }
           }

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
       if(tmg.get(grupp.trim()) != null){
        prod.setGroup((String)tmg.get(grupp.trim()));
       }else{
     	prod.setGroup("xxxx");
     }
       }


       if(mm.contains(prod.getID())){
        nimetus = "!MM! " + kood  + " " + nimetus ;
       }else{
        nimetus = "   " + kood  + " " + nimetus ;
       }

      prod.setName(nimetus);


     String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>1){
            prod.setGroupSecond(grupp2);
          }else{
          	 prod.setGroupSecond("yyyy");
          }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                prod.setUnit(unit);
             }

          //   prod.setInUnit(rs.getDouble("yhikukogus"));

               String vat = rs.getString("vat");
               if(vat!=null && vat.trim().length()>0){
                prod.setVat(vat);
             }
               double minunit = rs.getDouble("minunit");
               if(minunit > 1){
                prod.setOptAmountInUnits(minunit);
             }
               if(minunit != 0){
               	prod.setInCases(minunit);
               }

               if((prod.getStock()>0 || sol.contains(kood)) && minunit > 0 && prod.getID().length()>4 && prod.getID().indexOf("-") == -1){
               	// pasi soov dets 2007 viimased kaks tingimust
                products.put(nimetus, prod);
                done.addElement(prod.getID());
               }

      }
      rs.close();

     
    /*  
      String buf =   getProductSortingData(done);
       	 System.out.println("Sorteeritud Tootek ALGUS");
               
               if(buf.length() > 2){        
           	 System.out.println("Sorteeritud Tootek ALGUS222 ");
                   File f = new File( "prodsort.txt");
                   if (f.exists())f.delete();
                     try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				 "prodsort.txt", true));
        
               		System.out.println("Sorteeritud Tootek OK !!!!");
               		os.write(buf);
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }	
                }*/ 
      
      
      
      
      

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }





  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    //if (true) return custProds ;
 int counter = 1;
   String cmd = "SELECT  jou.orderaccount,tra.itemid ,jou.invoicedate AS lastdate ,tra.qty AS lastamount FROM bmssa.custinvoicetrans" +
   		"  tra INNER JOIN bmssa.custinvoicejour jou ON " +
     "  tra.invoiceid=jou.invoiceid INNER JOIN bmssa.inventTABLE cus ON cus.itemid=tra.itemid " +
      " WHERE  jou.invoicedate >sysdate-120   AND tra.dataareaid='vsf' and jou.dataareaid='vsf' and " +
      "cus.dataareaid='vsf' and length(trim(jou.orderaccount))>4 GROUP BY jou.orderaccount,tra.itemid,jou.invoicedate ,tra.qty " +
       "ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate,tra.qty " ;


    if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
        String kliendikood = rs.getString("orderaccount").trim();
        String tootekood = rs.getString("itemid").trim();
        String lastdate = rs.getString("lastdate").substring(0,10);
                 lastdate = lastdate.substring(5,10);
        CustProd cp = new CustProd(kliendikood, tootekood, rs.getDouble("lastamount") ,0 ,0,lastdate,0,0,0," ");

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
      String cmd =
          "  SELECT  lower(cus.salesgroup) as agent,round(SUM(lineamount),0) AS summa,round(SUM(lineamount),0)  -round(SUM(-costamountposted),0) AS summa2  " +
		  "FROM bmssa.custinvoicetrans sal INNER JOIN  bmssa.inventtable inv ON sal.itemid=inv.itemid INNER JOIN  bmssa.custinvoicejour jou ON " +
       "  sal.invoiceid=jou.invoiceid  INNER JOIN  bmssa.custtable cus ON cus.accountnum = jou.orderaccount  INNER JOIN inventtrans idi ON " +
       "sal.inventtransid = idi.inventtransid   WHERE  " +
       "  inv.dataareaid='vsf' AND sal.dataareaid='vsf'  AND cus.dataareaid='vsf'  AND  " +
       "  idi.dataareaid='vsf'  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'YYYY')) " +
       " = TO_NUMBER(TO_CHAR(sysdate, 'YYYY')) " +
       "  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'MM'))= TO_NUMBER(TO_CHAR(sysdate, 'MM')) " +
       " GROUP BY lower(cus.salesgroup) ";

      String cmd2 =
        "  SELECT  lower(cus.salesgroup) as agent,round(SUM(lineamount),0) AS summa,round(SUM(lineamount),0)  -round(SUM(-costamountposted),0) AS summa2  " +
		" FROM bmssa.custinvoicetrans sal INNER JOIN  bmssa.inventtable inv ON sal.itemid=inv.itemid INNER JOIN  bmssa.custinvoicejour jou ON " +
     "  sal.invoiceid=jou.invoiceid  INNER JOIN  bmssa.custtable cus ON cus.accountnum = jou.orderaccount  INNER JOIN inventtrans idi ON " +
     "sal.inventtransid = idi.inventtransid   WHERE  " +
     "  inv.dataareaid='vsf' AND sal.dataareaid='vsf'  AND cus.dataareaid='vsf'  AND  " +
     "  idi.dataareaid='vsf'  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'YYYY')) " +
     " = TO_NUMBER(TO_CHAR(sysdate, 'YYYY'))-1 " +
     "  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'MM'))= TO_NUMBER(TO_CHAR(sysdate, 'MM')) " +
     "  AND TO_NUMBER(TO_CHAR(sal.invoicedate, 'DD')) <= TO_NUMBER(TO_CHAR(sysdate, 'DD')) " +
     " GROUP BY lower(cus.salesgroup) ";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa2"));
                   // agent.setMonthBudget(agent.getMonthSales() - (int)rs.getDouble("summa2"));

                   // System.out.println(agent.getMonthSales());
                  }
                }
                rs.close();
              //  con.close();
                Statement stmt2 =  this.getNewConnection().createStatement();
                ResultSet	      rs2 = stmt2.executeQuery(cmd2);
                while(rs2.next()) {
                  String agentID = rs2.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthBudget((int)rs2.getDouble("summa2"));
                   // agent.setMonthBudget(agent.getMonthSales() - (int)rs.getDouble("summa2"));

                   // System.out.println(agent.getMonthSales());
                  }
                }
                rs2.close();
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
    return "VPGFin";
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
        String sql2 = "INSERT INTO TAL_ANSWERS (TAL_QUESTIONID,TAL_ANSWER,CUSTACCOUNT," +
        		"DATAAREAID,RECID,TAL_ANSWERDATE) values (?,?,?,'tal',?,?)";
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
                	double anse = 0;
                	double tt = lw.getLine().getResInt1();
                	if(tt == 1){
                		anse = 1;
                	}else if (tt == 0){
                		anse = 0;
                	}else if (tt == 2){
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
                pp.setInt(4 , getAndUpdateRecID());
                Date ddd = new Date();
                ddd.setHours(0);
                ddd.setMinutes(0);
                ddd.setSeconds(0);
                String dd = ddd.getTime() + "";
                dd = dd.substring(0,dd.length()-3) + "000";
     //  lw.getLine().getDate();
                pp.setTimestamp(5, new Timestamp(Long.parseLong(dd)));
                pp.executeUpdate();
   /*             ps.setString(1, wr.getInfo().getAgentID());
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

               // ps.executeUpdate();

              }

              ps.close();
          } catch (SQLException e) {
              e.printStackTrace();
              log.info(e);
          }
        }
*/


       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:oracle:thin:@orasrv.vpg.dk:1521:AXDB";

  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private TreeMap agents1 = new TreeMap();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
/**
 * @return Returns the agents1.
 */
public TreeMap getAgents1() {
	return agents1;
}
/**
 * @param agents1 The agents1 to set.
 */
public void setAgents1(TreeMap agents1) {
	this.agents1 = agents1;
}
}
