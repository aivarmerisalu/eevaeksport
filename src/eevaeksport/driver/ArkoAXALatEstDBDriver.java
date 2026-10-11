package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
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

public class ArkoAXALatEstDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter {
  public ArkoAXALatEstDBDriver(EksportPanel parent){
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

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
	  makePictureCat();
	  System.exit(0);
   	return null;
   }

  
  private void makePictureCat(){
	     
	//  makeCustprod();
	  getProducts(null);
System.out.println("prdcodes suurus "  + prodcodes.size());
    	
    	
    	String picts = parent.getProps().getProperty("pict.dir");
  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
  	 

    	try {

 HashSet tooted = new HashSet();
         StringBuffer buf = new StringBuffer();
         StringBuffer buf0 = new StringBuffer();
         int a = 0;
         int b = 0;
         int c = 0;
         System.out.println("Piltide leidmine ");
         
    	   for  (int i = 0 ; i<prodcodes.size();i++) {
    	    	a++;
    	    	c++;
    	    	if(c == 100){
    	    		  c =0;
    	    		  System.out.println("Piltide leidmine "  + a);	
    	    	}
    	     
                  String toode = prodcodes.elementAt(i) + "";  
                  toode = toode.replace('/', '_');
                  toode = toode.replace('\\', '_');
                
                  File f = new File(picts + toode+".jpg");
                  String len = "0";
                  if(f.exists()){
                    len =	f.length() + "";
                  }
          if(len.equals("0")){
            	//System.out.println(toode+".jpg;"  + len);
            	buf0.append(toode+".jpg\n");
            }else{
            	tooted.add((toode+".jpg").toLowerCase());
            	buf.append(toode+".jpg;"  + len + "\n");
      	    	b++;
            }
          
	   }
      
    	
    	    //oleasoleva kataloogi kustutamine
    	  System.out.println("Kataloogi kustutamine "  + a);	    
                File f = new File("list.txt");
                if (f.exists())f.delete();
                
                  try {
            		PrintWriter os = new PrintWriter(new FileWriter(
            				  "list.txt", true));
            		os.write(buf.toString());
            		os.close();
            	
            	  }catch (Exception ex) {
            		ex.printStackTrace();
            	  }
            	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
            	  File f0 = new File("list0.txt");
            	
            	  	
                     if (f0.exists())f0.delete();
                  
                    try {
              		PrintWriter os1 = new PrintWriter(new FileWriter(
              				  "list0.txt", true));
              		os1.write(buf0.toString());
              		os1.close();
              		//System.out.println( buf.toString());
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }

    //Kataloogi sisude kustutamine
              	  
   
           	 
           	 
         	 File res2 = new File (pictspda);
             String[] ff2 = res2.list();
         	 for(int i = 0;ff2.length>i;i++){
             //	System.out.println("fff " + ff2[i]);
             	(new File ( pictspda + ff2[i])).delete(); 
          
             }
//         	 L�ppes kustutamine 
         	 
           System.out.println("Piltide kopeerimine ");	
           //Failide kopeerimine  
             String[] gg = new File(picts).list();
          	  //System.out.println("gg " + gg[0] + " " + gg.length);
             
              for(int i = 0;gg.length>i;i++){
              	String fn = gg[i];
              	File in = new File ( picts + fn); 
              	if(in.isFile() && tooted.contains(fn.toLowerCase())){
              	
              	File inpda = new File ( pictspda +  toUpper( fn )); 
                    	Converter.shrinkAndCopyFile(in,inpda);
              	}
              }
              File fff =  new File (pictspda +  "list.txt");
              Converter.copyFile(f,fff);  	
        	  File fff0 =  new File (pictspda +  "list0.txt");
        	  Converter.copyFile(f0,fff0);  	
    	} catch (Exception e) {
    	    getParent().showError(e);
    	}
    
}
  
  private String toUpper(String asi ){
	   
	   asi = asi.replace('\\', '_');
	   asi = asi.replace('\\', '_');
	   asi = asi.replace('/', '_');
	   asi = asi.replace('/', '_');
	   asi = asi.substring(0,asi.indexOf('.'))+ asi.substring(asi.indexOf('.')).toLowerCase();
	   return asi ;
  }
  



   public Vector getFixedAssortmentData() {
     return null;
   }

   public Vector getPriceMatrixData() {
       Vector lines = new Vector();
      String sql = " SELECT  cus.invoiceaccount as custid , max(accountnum) as klient,  '' AS clgroup, " +
				 "  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid,  " +
				 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount) " +  
              "   AS price  FROM pricedisctable pd left join custtable cus on cus.accountnum = pd.accountrelation  and  cus.dataareaid= pd.dataareaid where  " +
				 "   pd.dataareaid = 'lv'   and cus.dataareaid = 'lv'  " +		   
				 " and  pd.fromdate<getdate() and pd.todate>getdate() " +
				 " and  pd.relation in (4,5)  AND pd.fromdate!=('1900-01-01 00:00:00.00') " +
				 "  group by cus.invoiceaccount, pd.itemrelation,pd.itemcode  order by  cus.invoiceaccount,pd.itemrelation";

       try {


     	Statement stmt = this.getNewConnection().createStatement();
               ResultSet rs = stmt.executeQuery(sql);

               while (rs.next()) {

                       String custID = rs.getString("custid");
                       String klient = rs.getString("klient");
                       String clGroup = rs.getString("clgroup");
                       String prodID = rs.getString("prodid");
                       String prodGroup = rs.getString("prodgroup");
                       double discount = rs.getDouble("discount");
                       double price = rs.getDouble("price");
                       MatrixLine mline = new MatrixLine();
                       
                      
                       if (klient != null && klient.trim().length() > 0) {
                           mline.setCustID(klient.trim());
                   }
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
               rs.close();
               
               
               String sql2 = "   select '' AS clgroup,  " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
				  "   pd.fromdate as algus , pd.todate as lopp from  " +
				  "   pricedisctable pd where   pd.fromdate<getdate()-3 and pd.todate>=getdate() and pd.dataareaid = 'lv' and accountrelation = '3%'  and percent1 != 3 " +
				  "  order by  pd.itemrelation " ;
               
               rs = stmt.executeQuery(sql2);

               while (rs.next()) {

                       String clGroup = rs.getString("clgroup");
                       String prodID = rs.getString("prodid");
                       String prodGroup = rs.getString("prodgroup");
                       double discount = rs.getDouble("discount");
                       String fromdate = rs.getString("algus");
                       String todate = rs.getString("lopp");
                       MatrixLine mline = new MatrixLine();
                       
                   
                       if (clGroup != null && clGroup.trim().length() > 0) {
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
                       mline.setStartDate(fromdate.substring(0,10));
                       mline.setEndDate(todate.substring(0,10));
                  
                     
                       lines.add(mline);
               }
               
               
               

       } catch (SQLException ex) {
       ex.printStackTrace();
       }
       return lines;}
   
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

  private void makeCustprod(){
	  int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate,max(tra.lineamount/case when tra.qty != 0 then tra.qty else 1 end) as lastprice " +
	      " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
	      " tra.invoiceid=jou.invoiceid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid " +
	      "   and  tra.dataareaid= cus.dataareaid  WHERE  jou.invoicedate > getdate()-300 AND tra.dataareaid ='lv'  AND jou.dataareaid = 'lv'  AND cus.dataareaid = 'lv' " +
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
	                 double lastprice = rs.getDouble("lastprice");
	                 
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,lastprice,0,0," ");

	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	        prods.addElement(tootekood);
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
	                        double lastprice = rs.getDouble("lastprice");
	               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,lastprice,0,0," ");
	               custProds.add(cp);
	             }
	      	}catch (Exception ee){

	      	}

	      }
	    }
  }
  
  public TreeMap getAgents(){
    String cmd = "SELECT e.groupid AS kood ,e.name  FROM custtable k INNER JOIN commissionsalesgroup e " +
    		" ON e.groupid=salesgroup  and  k.dataareaid= e.dataareaid " +
	" WHERE e.dataareaid='lv' AND k.dataareaid='lv'  group by e.name ,e.groupid  order by e.name";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
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
	  makeCustprod();
 String cmd = " SELECT cus.name,cus.namealias,cus.accountnum,(case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end )AS payer,  " +
              "(SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate()  " +
              "  AND co.accountnum = cus.accountnum) AS debt, cus.linedisc ,   " +
               "   aad.address, '' AS visit ,salesgroup AS agent ,  numofdays AS term, blocked AS block , " +
			  " (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount)  "+
			  " as payerblock ,aad.phone + ' '  + aad.telefax as info  FROM custtable cus LEFT JOIN paymterm  pay on cus.paymtermid=pay.paymtermid and  cus.dataareaid= pay.dataareaid  " +
			  " LEFT JOIN ADDRESS aad ON aad.addrrecid=cus.recid and aad.dataareaid= 'lv'   and  cus.dataareaid= aad.dataareaid" +
			"	 WHERE   (cus.dataareaid='lv'and  pay.dataareaid='lv' AND blocked != 2 and  len(salesgroup)>0 and arksendtopda =1 )  " + 
			 "   order by cus.name" ;
 
           String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
                    "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid  and  co.dataareaid= a.dataareaid " +
                    "WHERE co.dataareaid='lv' AND a.dataareaid='lv' ORDER BY a.accountnum,co.transdate ";


       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        String alias = Converter.enCodeEst(rs.getString("namealias"));
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name = name + " " + alias;
        }
        
        String accountnum = rs.getString("accountnum").trim();
        name = name +  " " + accountnum ;
        Customer cust = new Customer(accountnum);
        cust.setName(name );
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')), "Latvia"));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
        }
        if(cust.getID().startsWith("EE")) {
        	 cust.addAgent(new Agent("56"));
        }

        int block = rs.getInt("block");
        if (block == 1 || block == 2) {
          cust.setSalesBlock(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData(info.trim());
        }
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='lv' AND len(disc.itemrelation)=0  AND fromdate<=getdate() and todate >= getdate()";
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
    String    sql = " SELECT ve.accountnum AS liik, ve.name AS nimetus  FROM vendtable ve INNER JOIN inventtable t " +
    		"ON t.primaryvendorid=ve.accountnum  and  t.dataareaid= ve.dataareaid WHERE ve.dataareaid='lv' and  t.dataareaid='lv'  group by ve.accountnum, ve.name ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {
	  

		    Connection con = getNewConnection();
		    TreeMap   tm = new TreeMap();
		    String    sql = "select category1+category2 as liik , category1+category2 +' '+ name as nimetus from arkitemcategory2table where dataareaid ='vrt' and name !='' ";
		    Statement st = con.createStatement();
		    ResultSet rs = st.executeQuery(sql);

		    while (rs.next()) {
		      String liik = rs.getString("liik").trim();
		        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
		    }
		    rs.close();
		    st.close();
		    con.close();

		    return tm;
  }

  private Vector prodcodes = null;
  
  public TreeMap getProducts(Agent agent){
	  prodcodes = new Vector();
    String cmd = "  SELECT inv.itemid,(inv.itemname) AS nimetus2,tc.txt as nimetus, arkboxqty AS kastis,  mod.price AS price , pdt.AMOUNT as hulgihind  " +
             " , itembarcode AS ean,primaryvendorid AS grupp ,arkitemcategory1 + arkitemcategory2 AS grupp2,mod.unitid AS unit " +
             "   FROM inventtable inv  " +
             "    INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid  and  mod.dataareaid= inv.dataareaid " +
             "   left join inventtxt tc ON tc.itemid = inv.itemid and tc.languageid ='ET' " +   
             "    LEFT JOIN inventitembarcode bar ON bar.itemid = inv.itemid  and  bar.dataareaid= inv.dataareaid and bar.barcodesetupid='EAN13' AND bar.dataareaid='lv' " + 
                    "   CROSS APPLY ( select top 1 Amount from PRICEDISCTABLE  " +
                     "                      JOIN INVENTPARAMETERS  ON inventparameters.PRICEGROUPVT = PRICEDISCTABLE.ACCOUNTRELATION " +
                     "                       and inventParameters.DATAAREAID = PRICEDISCTABLE.DATAAREAID   " +
                     "                     where PRICEDISCTABLE.RELATION = 4 and PRICEDISCTABLE.ITEMCODE = 0 " +
                      "                      and PRICEDISCTABLE.ITEMRELATION = inv.ITEMID and PRICEDISCTABLE.DATAAREAID = inv.DATAAREAID " +
                      "                      and PRICEDISCTABLE.ACCOUNTCODE = 1 and PRICEDISCTABLE.FROMDATE <= GETDATE() " +
                      "                     and ( PRICEDISCTABLE.TODATE >= GETDATE() or PRICEDISCTABLE.TODATE <= 1900-01-02)) pdt " +
                  " WHERE   inv.dataareaid='lv' AND mod.dataareaid='lv'   AND moduletype=2   " +      
                    "        AND len(inv.itemname)>0   AND del_blocked = 0 " +
                     "           order by inv.itemid "  ;

             String cmd2 = " SELECT summ.itemid AS toode , sum(AVAILPHYSICAL) AS stock ,max(price) as cost FROM INVENTSUM summ " +
               "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid  and  summ.dataareaid= dim.dataareaid  " +
                " INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  and  inv.dataareaid= summ.dataareaid " +
                " WHERE summ.DATAAREAID='lv'and dim.DATAAREAID='lv' and inv.DATAAREAID='lv' AND CLOSED = 0 AND  moduletype=0  " +
                 " AND  rtrim(dim.inventlocationid)  IN ('01','02') GROUP BY summ.itemid ORDER BY summ.itemid";
        
             String cmd3 = "select itemid ,itembarcode as code from inventitembarcode where dataareaid= 'lv'  and barcodesetupid='EAN13' "+	
		"	";

    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap stocks = new TreeMap();
    TreeMap costs = new TreeMap();
    
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
    	
        ResultSet rs1 = stmt.executeQuery(cmd2);
        while(rs1.next()){
          String id = rs1.getString("toode").trim();     
            stocks.put(id,rs1.getDouble("stock")+"");
            costs.put(id, rs1.getDouble("cost")+"");
          }
    	
    	
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus2").trim();
        String nim  = rs.getString("nimetus");
        if(nim != null){
        	nimetus = nim;
        }
       nimetus = Converter.enCodeEst(nimetus) + " " +kood;
        
   
    

        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("hulgihind"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        //prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
        nimetus =  "    " + nimetus ;
     }

      prod.setName(nimetus.trim());


     String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>0){
             prod.setGroupSecond(grupp2);
          }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                prod.setUnit(unit);
             }

               if(stocks.get(prod.getID()) != null){
            	   prod.setStock(Double.parseDouble((String)stocks.get(prod.getID())));
               }
               
               double oh = 0;
               
               if(costs.get(prod.getID()) != null){
            	   oh =(Double.parseDouble((String)costs.get(prod.getID())));
               }
               
             	
               
            	if(oh>0){
            		double kate = (1-oh/prod.getPrice())*100;
                	if(kate>0){
                		String kat = (kate + "");
                        prod.setMargin(kat.substring(0,kat.indexOf(".")));   	
                	//	System.out.println(prod.getName() + "'" + prod.getMargin() + "'");
                	}	
            	}

               
               
               prod.setVat("21");
   if(prod.getStock() > 1 || (prods.contains(prod.getID()) && prod.getStock() >0 && prod.getStock() < 3) ){
    products.put(nimetus, prod);
    prodHash.put(prod.getID(), prod);
    prodcodes.add(prod.getID());
   }

      }
      rs.close();

      
      rs1 = stmt.executeQuery(cmd3);
      while(rs1.next()){
        String id = rs1.getString("itemid").trim();     
        String code = rs1.getString("code").trim();
         Product prod1 = (Product)prodHash.get(id);
         if (prod1 != null){
            if(prod1.getEAN()== null || prod1.getEAN().trim().length()== 0){
            	prod1.setEAN(code);
            	if(code.length()> 5){
                	prod1.setName(prod1.getName() + " " +code.substring(code.length()-5));	
            	}
    //            prod1.addBarcode(code);
            }else{
                prod1.addBarcode(code);
             }
           }
      }
    
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
   
      String cmd ="SELECT  cus.arksalesadministrator as agent,round(SUM(lineamount),0) AS summa " +
        " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  and  sal.dataareaid= inv.dataareaid INNER JOIN custinvoicejour jou ON " +
        " sal.invoiceid=jou.invoiceid  and  sal.dataareaid = jou.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  and  cus.dataareaid= jou.dataareaid WHERE  " +
        " inv.dataareaid='lv' AND sal.dataareaid='lv' AND cus.dataareaid='lv'  AND jou.dataareaid='lv' AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
        " AND MONTH(sal.invoicedate) = MONTH(getdate()) " +
        " GROUP BY cus.arksalesadministrator";

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
    return "ArkoAXALat";
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
  private Vector custProds = new Vector();
  private String subjectFilesTo = null;
  private Vector prods = new Vector();
    private NumberFormat NF = NumberFormat.getInstance();
}
