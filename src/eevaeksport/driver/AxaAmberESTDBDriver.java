package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;
import sun.util.logging.resources.logging;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaAmberESTDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, 
Discounter,PictureMaker,FixLoader {
  public AxaAmberESTDBDriver(EksportPanel parent){
    super(parent);
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
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

  
	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
  /**
   *
   */
	private TreeMap eanprods = null;
	@Override
	public void FixLoader() {
		
	eanprods =  Converter.loadMore();
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
		this.getProducts(null);
		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
		System.out.println("Contracts " + contracts.toString());
		Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
		System.exit(0);
	}

	
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
	   
	   Vector lines = new Vector();
	      return lines;
	}
	 
   

   public Vector getPriceMatrixData() {
       Vector lines = new Vector();
      String sql = " SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, max(PRICEDISCTABLE.AMOUNT) AS price " +  
 "   FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ade' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=0 AND  " +
	" PRICEDISCTABLE.itemcode=0  and PRICEDISCTABLE.AMOUNT > 0.01 and  " +
 "   ((GETDATE() between PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE+1) " +
	"   or(GETDATE()>=PRICEDISCTABLE.FROMDATE AND year(PRICEDISCTABLE.toDATE) =1900) "+
	"   or(year(PRICEDISCTABLE.FROMDATE) =1900 and year(PRICEDISCTABLE.toDATE) =1900) " +
	"  or( GETDATE()<=PRICEDISCTABLE.TODATE+1) )   " +
	"    GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION " +
	"	order by ACCOUNTCODE,ITEMCODE	";

       try {

Connection con = this.getNewConnection();
     	Statement stmt = con.createStatement();
               ResultSet rs = stmt.executeQuery(sql);

               while (rs.next()) {
             	  String prodID = rs.getString("prodid");
                       String custID = rs.getString("custid");
                       double price = rs.getDouble("price");
                       MatrixLine mline = new MatrixLine();
                 
                       if (custID != null && custID.trim().length() > 0) {
                               mline.setCustID(custID.trim());
                       }
                    
                       if (prodID != null && prodID.trim().length() > 0) {
                               mline.setProdID(prodID.trim());
                       }
                  
                       if (price != 0) {
                               mline.setPrice(price);
                       }
                     
                       lines.add(mline);
               }
               rs.close();
               
               
               //kampaaniad 
               
               String sthind = "SELECT PRI.ITEMRELATION as toode ,pri.AMOUNT as hind  " +
 "   FROM PRICEDISCTABLE pri " +
	"   WHERE PRI.DATAAREAID='ade' AND PRI.RELATION in (4) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and " + 
 "     ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
	"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) "+
	"   or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION   like '%Stan%' " +
	"    ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation";
               
         
               
               TreeMap hinake = new TreeMap();
               
               rs = stmt.executeQuery(sthind);

               while (rs.next()) {
                     hinake.put(rs.getString("toode").trim(), rs.getDouble("hind"));
               }
               rs.close();

               String sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus " +
               		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp ,PRI.RELATION " +
   " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
	 "  WHERE PRI.DATAAREAID='ade' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
  "    ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
	 "  or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	 "  or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	 "  or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION not like 'Stan%'" +
   "   ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                                    
                       
               
                      rs = stmt.executeQuery(sql4);

                      while (rs.next()) {

                              String clGroup = Converter.cleanLitLatEst(rs.getString("grupp"));
                              String prodID = rs.getString("toode");
                              double price = rs.getDouble("hind");

                              String algus = rs.getString("algus").substring(0,10);
                              String lopp = rs.getString("lopp").substring(0,10);
                              int kuu = rs.getInt("kamp");
                              int relation = rs.getInt("RELATION");
                              
                              MatrixLine mline = new MatrixLine();
                              
                          
                              if (clGroup != null && clGroup.trim().length() > 0) {
                                      mline.setCliGroup(clGroup.trim());
                              }

                              if (prodID != null && prodID.trim().length() > 0) {
                                      mline.setProdID(prodID.trim());
                              }
                              
                              if( algus.indexOf("1900")== -1 &&  lopp.indexOf("1900")== -1 ){
                             	
                               if (algus  != null && algus.trim().length() > 0 && algus.indexOf("1900")== -1) {
                                    //  mline.setStartDate(algus.trim());
                              }

                              if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                                     // mline.setEndDate(lopp.trim());
                              }
                              
                              if(kuu > 2  && algus.indexOf("1900")== -1 && lopp.indexOf("1900")== -1){
                             	 //mline.setProdLevel("pr");
                              }
                              
                              }
                              boolean goes = true;
                              if(hinake.get(prodID) !=null){
                             	double dd = Double.parseDouble(hinake.get(prodID)+"");
                             	if(dd == price){
                             		goes = false;
                             	}
                              }
                              
                              if(goes){
                             	
                              if(relation == 5 ){
                             	 if(hinake.get(prodID) !=null){
                             	 price = Double.parseDouble(hinake.get(prodID)+"")-price;
                             	 mline.setPrice(price);
                                      lines.add(mline);
                                  }
                             	 
                              }else{
                             	 mline.setPrice(price);
                                  if(price > 0){
                                      lines.add(mline);
                                  }
                              }
                              
                              }
                      }
                      
                      rs.close();
     
                    sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus " +
                      		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp " +
          " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
    	  " WHERE PRI.DATAAREAID='ade' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0  and " +
        "   ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) "+
	  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	  " or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION not like 'Stan%'" +
        "    ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                             
                             rs = stmt.executeQuery(sql4);

                             while (rs.next()) {

                                     String custID = Converter.cleanLitLatEst(rs.getString("grupp"));
                                     String prodID = rs.getString("toode");
                                     double price = rs.getDouble("hind");

                                     String algus = rs.getString("algus").substring(0,10);
                                     String lopp = rs.getString("lopp").substring(0,10);
                                     int kuu = rs.getInt("kamp");
                                     MatrixLine mline = new MatrixLine();
                                     
                                 
                                     if (custID != null && custID.trim().length() > 0) {
                                             mline.setCliGroup(custID.trim());
                                     }

                                     if (prodID != null && prodID.trim().length() > 0) {
                                             mline.setProdID(prodID.trim());
                                     }
                                     
                                     if ( kuu < 3 && algus.indexOf("1900")== -1  && lopp.indexOf("1900")== -1){
                                         if (algus  != null && algus.trim().length() > 0  && algus.indexOf("1900")== -1) {
                                           //  mline.setStartDate(algus.trim());
                                     }

                                     if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                                             //mline.setEndDate(lopp.trim());
                                     }
                                     
                                     if(kuu > 3){
                                    	 mline.setProdLevel("pr");
                                     }                                        	
                                     }

                                     boolean goes = true;
                                     if(hinake.get(prodID) !=null){
                                    	double dd = Double.parseDouble(hinake.get(prodID)+"");
                                    	if(dd == price){
                                    		goes = false;
                                    	}
                                     }
                                     
                                     if (goes){
                                         mline.setPrice(price);
                                         lines.add(mline);
                                     }
                            
                             }
                             
rs.close();
                             
                             
                             
                             
                             
                             
                             
                             sql4 = "select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind ,PRI.FROMDATE as algus " +
             "   	 ,PRI.TODATE as lopp,DATEDIFF(week, PRI.FROMDATE, PRI.TODATE)  as kamp,PRIORITY as prior " +
" from astPromoGMLine l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum " +
" where l.dataareaid = 'ade' and ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
	"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
	"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	"   or( GETDATE()<=PRI.TODATE+1) ) and accepted = 2  and fromqty = 0 and promostatus in (1,4)  " ;
                                      
                                      rs = stmt.executeQuery(sql4);

                                      while (rs.next()) {

                                              String custID = Converter.cleanLitLatEst(rs.getString("grupp"));
                                              String prodID = rs.getString("toode");
                                              double price = rs.getDouble("hind");

                                              String algus = rs.getString("algus").substring(0,10);
                                              String lopp = rs.getString("lopp").substring(0,10);
                                              int kuu = rs.getInt("kamp");
                                              String pri = rs.getInt("prior")+"";
                                              if(pri.length() == 1){
                                             	 pri = "000"+ pri;
                                              }
                                              if(pri.length() == 2){
                                             	 pri = "00"+ pri;
                                              }

                                              if(pri.length() == 3){
                                             	 pri = "01"+ pri;
                                              }
                                              MatrixLine mline = new MatrixLine();
                                              
                                          
                                              if (custID != null && custID.trim().length() > 0) {
                                                      mline.setCliGroup(custID.trim());
                                              }

                                              if (prodID != null && prodID.trim().length() > 0) {
                                                      mline.setProdID(prodID.trim());
                                              }
                                              

                                              
                                              if(kuu > 42 ){
                                             //	 mline.setProdLevel("pr");
                                                }else{
                                                mline.setStartDate(algus.trim());
                                                mline.setEndDate(lopp.trim());
                                              }
                                              

                                            //  mline.setProdLevel(pri); 
                                              mline.setPrice(price);
                                     
                                              lines.add(mline);
                                      }
                                      
                                      rs.close();
                                      stmt.close();
                                      con.close();

                             
                             
                             
                             
                             
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


  public String getAssortmentStr(Agent agent){
  	return null;
  }
  
  private TreeMap kogused = null;

 public String getSaveStockInfo(){
	  
   kogused = new TreeMap();
	  
   String cmd = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  with(nolock) " +
	     "    INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " + 
	     " WHERE  summ.DATAAREAID='ade' and CLOSED = 0  and dim.inventlocationid in ('VIA3L-OP','VIA3L-EXC','VIA3L-EXC') " +
		"	GROUP BY  summ.itemid  having sum(availphysical) >0 ";


       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
    	   Connection con = this.getNewConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
          // String dealer = rs.getString("ladu");
           if(seis > 0){
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  "\n"); // + dealer +  "\n");
             kogused.put(kood, seis + "");
           }
           }
           rs.close();
           st.close();
           con.close();
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

  
     
       return null;
     }

  
  private TreeMap agentDim = new TreeMap();
  private TreeMap agent1 = new TreeMap();
  public TreeMap getAgents(){
	  
    String cmd = " SELECT  a.ACCOUNTID as kood , d.NAME FROM  EAgentTable a inner join  EMPLTABLE  e  on e.EMPLID = a.EMPLID and e.DATAAREAID = a.DATAAREAID INNER JOIN " +
                       "  DIRPARTYTABLE AS d ON d.DATAAREAID = 'v02' AND e.PARTYID = d.PARTYID " +
               " WHERE   (e.DATAAREAID = 'ade') ";
    
   // Converter.loadVisitLines("plan", rcompany, user, pass);
   // Converter.loadVisitLines("done", rcompany, user, pass);
   // Converter.loadVisitLines("extra", rcompany, user, pass);
    
   
    
   
 
    
    
    
   // makePlans();
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
    	agent1 = new TreeMap();
    	
    	
    	Connection con = null;
    	try{
    		con = getNewConnection();
    	}catch(Exception e){
    		System.out.println("OutBabe no connection");
    		System.exit(0);
    	}
        Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("kood").trim()));       
          String name = rs.getString("name").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);
          agents.put(name, agent);
          agent1.put(agent.getID(), agent);
        }
        rs.close();
        stmt.close();
        con.close();
      } catch (Exception e){
        getParent().showError(e);
      }

    
    Agent agenta = new Agent("16");       
    String name = "Andrei";
    agenta.setName(name + " " + agenta.getID());
    agenta.setIsPilot(true);
    agents.put(name, agenta);
    agent1.put(agenta.getID(), agenta);
    
    
    
	try{
		String file2 = "replacer.txt";
		File ff = new File(getExcelDir() +  file2);
		if(ff.exists()){		
			FileReader fr = new FileReader(getExcelDir()  + file2);
		 BufferedReader reader = new BufferedReader(fr);
	 String line = null;
		while ((line = reader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, ";");
			if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
					Agent agent = (Agent)agent1.get(id);
					if(agent != null){
						while (st.hasMoreTokens()) {
							String dubler = st.nextToken().trim();
							agent.addDubbler(dubler);
							System.out.println("Lisan Dublanti " + id + " " + dubler );
						}
					}	
				}
			}
		}
		
	
	}catch(Exception e){
	e.printStackTrace();	
	}
	    
    
	//setBulkSales(agent1);
    
    
    return agents;
  }
  
  
  private String getStatusText(String aa){
	  
	  if(aa.equals("10")){
		  return "Open";
	  }
	  if(aa.equals("20")){
		  return "Prepayment";
	  }
	  if(aa.equals("25")){
		  return "HigherPromo";
	  }
	  
	  if(aa.equals("30")){
		  return "CreditControl";
	  }
	  if(aa.equals("40")){
		  return "LowCost";
	  }
	  if(aa.equals("50")){
		  return "Confirmed";
	  }
	  if(aa.equals("51")){
		  return "Execution";
	  }
	  if(aa.equals("52")){
		  return "Completed";
	  }
	  if(aa.equals("53")){
		  return "Invoiced";
	  }
	  if(aa.equals("54")){
		  return "OnTheWay";
	  }
		 
	  if(aa.equals("55")){
		  return "Canceled";
	  }
		 
	  if(aa.equals("56")){
		  return "PartlyCompleted";
	  }
		 
	  if(aa.equals("57")){
		  return "Returned";
	  }
	  if(aa.equals("58")){
		  return "UnConfirmed";
	  }
		 
	  if(aa.equals("59")){
		  return "Verified";
	  }
	  if(aa.equals("71")){
		  return "EESent2Whs";
	  }
	  if(aa.equals("72")){
		  return "EEWhsConfirmError";
	  }
	  if(aa.equals("73")){
		  return "EEWhsConfirmation";
	  }
	  if(aa.equals("74")){
		  return "EEWhsInvoiceError";
	  }
	  if(aa.equals("75")){
		  return "EEWhsInvoice";
	  }
	  if(aa.equals("76")){
		  return "EEInvoicedAndSent";
	  }
	  if(aa.equals("59")){
		  return "Verified";
	  }
	  if(aa.equals("59")){
		  return "Verified";
	  }
	  if(aa.equals("80")){
		  return "InProduction";
	  }
	  if(aa.equals("90")){
		  return "LoadApproved";
	  }
	  if(aa.equals("91")){
		  return "IncorrectItem";
	  }
	  if(aa.equals("92")){
		  return "IncorrectPrices";
	  }
	  if(aa.equals("93")){
		  return "NotGive";
	  }
	  if(aa.equals("94")){
		  return "Samples";
	  }
	  if(aa.equals("95")){
		  return "Realization";
	  }
	return aa;
	  
  }
  

  
  private TreeMap  getOpenOrders(){
		TreeMap res = new TreeMap();
		

		   String sql2 =  "select  sal.salesid AS id,case when ediordernumber like 'MS-%' then 1 else 0 end as abi, sal.custaccount AS kkood , "+ 
"sal.salesid as number ,SPISalesOrderStatus  as status,sal.createddatetime AS kuup, estimate AS summa "+
"from SalesTable sal with (nolock) "+
"WHERE sal.dataareaid='ade' AND sal.createddatetime > getdate()-1 "+
"ORDER BY sal.createddatetime " ;
		   
	
	 this.connectionUp();
	try {
	   nf.setMinimumFractionDigits(2);
	   Connection con = this.getNewConnection();
	   PreparedStatement stmt2 = con.prepareStatement(sql2);
	   ResultSet  rs = stmt2.executeQuery();


	while(rs.next()){
		StringBuffer sb = new StringBuffer();
		String id = rs.getString("id");
		 int abi = Integer.parseInt(rs.getString("abi"));
		 String text = "edi";
		 if(abi == 1){
			 text = "merisoft";
		 }
	    String nimi = rs.getString("kkood");
	    if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim() + " ('" +getStatusText(rs.getString("status").trim()) +"' ");
		sb.append(rs.getString("kuup").trim().substring(0,10) + " " + text + ") ");
		

		double summa = Double.parseDouble(rs.getString("summa").trim());
		if(summa > 0 ){
			String sum = getNF().format(summa) ;
		    sb.append(sum + " eur KM-ga|" );	
		}else{
			 sb.append("|" );	
		}
		
	  
	    String sql3 = "select qtyordered as amount,  salesunit as unit  , name from  " +
				   " SalesLine rid where rid.dataareaid='spil' and  salesid =  '" + id + "'";
	    Connection cc = this.getNewConnection();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
		 
	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    }
	   
		    rs2.close();
	       stmt3.close();
	       cc.close();
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	       
	       }else{
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	    	   
	       }
	   	
	}
	
	
	// siin lisame Realizatsiooni asjad juurde 
	
	sql2 = "		select  sal.salesid AS id,case when ediordernumber like 'MS-%' then 1 else 0 end as abi,  " +
		   "  sal.custaccount AS kkood , sal.salesid as number ,sal.createddatetime AS kuup , " +
		   "	 estimate AS summa from SalesTable sal  WHERE sal.dataareaid='ade'  AND  " +
		   "  sal.createddatetime > getdate()-360  and sal.salesid like 'REALI%' ORDER BY sal.createddatetime";
	
       stmt2 = con.prepareStatement(sql2);
	     rs = stmt2.executeQuery();


	while(rs.next()){

		StringBuffer sb = new StringBuffer();
		String id = rs.getString("id");
		 int abi = Integer.parseInt(rs.getString("abi"));
		 String text = "edi";
		 if(abi == 1){
			 text = "merisoft";
		 }
	    String nimi = rs.getString("kkood");
	    if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim()+ " Realization");
		sb.append(rs.getString("kuup").trim().substring(0,10) + " " + text + ") ");
		

		double summa = Double.parseDouble(rs.getString("summa").trim());
		if(summa > 0 ){
			String sum = getNF().format(summa) ;
		    sb.append(sum + " eur KM-ga|" );	
		}else{
			 sb.append("|" );	
		}
		
	  
	    String sql3 = "select qtyordered as amount,  salesunit as unit  , name from  " +
				   " SalesLine rid where rid.dataareaid='ade' and  salesid =  '" + id + "'";
	    Connection cc = this.getNewConnection();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
		 
	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    }
	   
		    rs2.close();
	       stmt3.close();
	       cc.close();
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }else{
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }
	   	
	}
	
	
	
	rs.close();
	stmt2.close();
	con.close(); 	 
	

	
	
	
	
	
	} catch (Exception e) {
	  getParent().showError(e);
	}

	connectionDown();

		
		return res;
	}
	


  
  public static float Round(float Rval, int Rpl) {
	   float p = (float)Math.pow(10,Rpl);
	   Rval = Rval * p;
	   float tmp = Math.round(Rval);
	   return (float)tmp/p;
	   }
  
  public TreeMap getCustomers(Agent agent){
	  
	   TreeMap openOrders = this.getOpenOrders();
	   
	  // System.out.println("openOrders " + openOrders.toString());
		

 String cmd = "  SELECT   cus.name,cus.accountnum,cus.invoiceaccount AS payer, " +
            "  (SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct " +
            " 	                     on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and "  + 
            " 	                     ct.storno = 0 AND co.duedate<getdate()  and co.dataareaid = 'ade'   " +
             "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else   " +
 	         " cus.invoiceaccount end )) AS debt,  (SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct " + 
 	         " 	                     on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and " + 
 	         " 	                     ct.storno = 0  and  co.dataareaid = 'ade' " +  
              " AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else   " +
 	         " cus.invoiceaccount end )) AS debta, " +
			 " (select max (creditmax) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ade') " + 
			 "   as limit ,"
 	         + " cus.linedisc ,   " +
              "   cus.address, '' AS visit ,  SPIEAGENTACCOUNTID as agent , SPITELESALESAGENTGROUPID as teles,  (select top 1 numofdays from paymterm pt where pt.paymtermid=	" +
			"	 (select top 1 paymtermid from custtable where accountnum = cus.invoiceaccount and dataareaid in ('ade')  ) and pt.dataareaid in ('v02')) AS term, " +
			"	  blocked AS block , " +
			 " (select max (blocked) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ade') " +
			  "  as payerblock 	,phone + ' , ' + telefax as info , PriceGroup as grupp , COMPANYCHAINID as chain ,  erpagentdiscedit as maxallow , custclassificationid as ette ,memo as cuinfo , "
			  + "   (select spiminorderamount from smmBusRelChainGroup e where e.dataareaid= cus.dataareaid and e.chainid = COMPANYCHAINID ) as minam , cus.exportformssamples  as sample  "
			
			+ "  FROM custtable cus with(nolock) left outer JOIN paymterm " +
			" pay with(nolock) on cus.paymtermid=pay.paymtermid and  pay.dataareaid='ade'  " +
			"	 WHERE   cus.dataareaid in ('ade') and blocked != 2  and (SPIEAGENTACCOUNTID != '' or SPITELESALESAGENTGROUPID != '') " +
			 "  and erpactive = 1   " ;
 
 String tra = "select  accountnum as cuid , monday as mon , tuesday as tue , wednesday as wed, thursday as thu , friday as fri , saturday as sat, sunday as sun " +
" from custtable cus inner join TRDDeliveryPlaces tr on tr.dataareaid = cus.dataareaid and  cus.trdoutletid = tr.outletid where cus.dataareaid = 'ade'";
 
 
 String ccc = "select invoiceaccount from custtable where dataareaid='ade' group by invoiceaccount";
 String promo = "select custaccount as custid , l.promonum as grupp   from astPromoJournal pri  " + 
			" INNER JOIN astPromoCustomers l on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  where " +
			" l.dataareaid = 'ade' and ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
		  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
		  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
		  " or( GETDATE()<=PRI.TODATE+1 AND year(PRI.fromDATE) =1900) ) and promostatus in (1,4)";
 
           String sql = "  SELECT  a.accountnum,a.orderaccount AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata,  " +
                  "  co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle  FROM custtransopen co with(nolock)  " +
				" INNER JOIN custtrans a with(nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
                 "   WHERE co.dataareaid='ade' AND a.dataareaid='ade'  and co.duedate < getdate()+5 and a.invoice != '' " +
				"	and a.storno !=1 and a.accountnum not in ('DB0001334','DB0000867','DB0000010', 'DB0001529', 'DB0001186' , 'DB0001049', 'DB0001439') "
				+ "and a.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') ORDER BY a.accountnum,co.transdate " ;
			  
           
           String salessql = " SELECT  jou.orderaccount as klient,( ltrim(case when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
        "  round(SUM(lineamount),0) AS summa " +
	    "  FROM custinvoicetrans sal with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON  " +
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
		" WHERE sal.dataareaid='ade'   AND jou.dataareaid='ade' AND sal.invoicedate > getdate()-200 " +
        " GROUP BY jou.orderaccount ,(YEAR(sal.invoicedate)),(month(sal.invoicedate))  "
        + " order by   jou.orderaccount ,YEAR(sal.invoicedate), month(sal.invoicedate) ";

           try{
        	   Converter.getManagersReplacements(agent1, rcompany, user, pass,true); 
        		
        }catch(Exception e){
        	
        }
           
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();
    HashMap visits = new HashMap();
    HashMap transa = new HashMap();
    HashMap promogroups = new HashMap();
    
    try {
 
    	
    	Connection con = this.getNewConnection();
    	Statement stmr = con.createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(salessql);
        while(rs1.next()){
        	String klient = rs1.getString("klient").trim();
        	String kuu = rs1.getString("kuu");
        	int summa = (int)rs1.getDouble("summa");
          if(salesres.get(klient) == null){
        	  salesres.put(klient,"   " + kuu + " : " + summa + " " );
          }else{
        	  String asi = (String)salesres.get(klient);     	  
        	  salesres.put(klient,"   " + kuu + " : " + summa +" |" + asi);
          }
        }
       rs1.close();
       stmr.close();
    
       stmr = con.createStatement();
   	
       rs1 = stmr.executeQuery(tra);
      while(rs1.next()){
    	  String cuid = rs1.getString("cuid");
    	  int mon = rs1.getInt("mon");
    	  int tue = rs1.getInt("tue");
    	  int wed = rs1.getInt("wed");
    	  int thu = rs1.getInt("thu");
    	  int fri = rs1.getInt("fri");
    	  int sat = rs1.getInt("sat");
    	  int sun = rs1.getInt("sun");
    	  
    	String vis = "";
    	String ttt = "";
    	if(mon == 1){
    		vis = "R";
    		ttt = "1";
    	}
    	if(tue == 1){
    		vis = vis + "E";
    		ttt = ttt+ ",2";
    	}
    	if(wed == 1){
    		vis = vis + "T";
    		ttt = ttt+ ",3";
    	}
    	if(thu == 1){
    		vis = vis + "K";
    		ttt = ttt + ",4";
    	}
    	if(fri == 1){
    		vis = vis + "N";
    		ttt = ttt+ ",5";
    	}
    	if(sat == 1){
    		vis = vis + "R";
    		ttt = ttt+ ",6";
    	}
    	if(sun == 1){
    		vis = vis + "L";
    		ttt = ttt+ ",7";
    	}
    	
    	if(vis.length() >0){
    		visits.put(cuid, vis);
    		transa.put(cuid, ttt);
        }
  
    	
    	
      }
     rs1.close();
     stmr.close();
      
 // teeme return reason file valmis 
     
     stmr = con.createStatement();
 	StringBuffer sb = new StringBuffer();
     rs1 = stmr.executeQuery("select reasoncodeid as id, description as nimi from ReturnReasonCode where dataareaid ='ade'");
    while(rs1.next()){
    	String id = rs1.getString("id");
    	String name = rs1.getString("nimi");
    	if(name != null && name.trim().length() > 0){
    	      sb.append( id+ ";" +Converter.enCodeEst(name.trim()) + "\n");	
    	}
    }
   rs1.close();
   stmr.close();
  // System.out.println("REASON " + sb.toString());
   if(sb.length() > 0){
	   Converter.saveFile("returnreason.txt", sb.toString());
   }
       
       
   	 stmr = con.createStatement();
	
     rs1 = stmr.executeQuery(ccc);
    while(rs1.next()){
   payers.add(rs1.getString("invoiceaccount"));
      
    }
   rs1.close();
   stmr.close();
   
   Statement stm = getNewConnection().createStatement();
   
   
   stmr = con.createStatement();
	
   rs1 = stmr.executeQuery(promo);
 
   while(rs1.next()){
	   String ij = rs1.getString("custid").trim();
	   String gr = Converter.cleanLitLatEst(rs1.getString("grupp").trim()) ;
	   if( promogroups.get(ij) == null){
		   promogroups.put(ij,gr);
	   }else{
		   promogroups.put(ij,promogroups.get(ij)+ "|"+ gr);
	   }
	 }
    rs1.close();
    stmr.close();
   
   
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim()).replace('*', ' ').trim();;
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        
        if(payer != "DB0001334" && payer != "DB0000867" && payer != "DB0000010"&& payer != "DB0001529" && 
        		payer != "DB0001186" && payer != "DB0001049" && payer != "DB0001439"){
        
			  
        
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt(round(Double.parseDouble(debtStr),2));
        }
        String debtStra = rs.getString("debta");
        if (debtStra != null){
          cust.setUsedCredit(round(Double.parseDouble(debtStra),2));
        }

        String limit = rs.getString("limit");
        if (limit != null){
          cust.setCredit(limit);
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',',')),"Estonia"));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String age = rs.getString("agent");
        if (age != null) {
         cust.addAgent(new Agent(age.trim()));
         cust.setMainAgent(age.trim());
        
         if (age != null && agent1.get(age.trim()) != null){
				Agent aa = (Agent)agent1.get(age.trim());
			
				Vector vec = aa.getDubbler();
				if(vec!= null && vec.size() >0){
					Enumeration en = vec.elements();
					while(en.hasMoreElements()){
						String ag = (String) en.nextElement();
						cust.addAgent(new Agent(ag));
					}
				}
			}
        }
    
        if(openOrders.get(cust.getID()) != null){
        	cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Avatud Tellimused |"   +  openOrders.get(cust.getID()));
        }
        
        
        String teles = rs.getString("teles");
        if (teles != null && teles.trim().length() > 0) {
         cust.addAgent(new Agent(teles.trim()));
         cust.setCity("Tele: " + teles.trim());
        }
    
        
        String term = rs.getString("term");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
        
        
        int block = rs.getInt("block");
        if (block == 1 ) {
          cust.setSalesBlock(true);
        }
        if (block == 2 ) {
            cust.setSalesBlockT(true);
          }
       boolean goes = true;
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }
        if (payerblock == 2) {
            cust.setSalesBlockT(true);
            goes = false;
          }

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>2) {
        	if(info.trim().startsWith(",")){
        		info = info.trim().substring(1);
        	}
          cust.setPersonalData(info.trim().replaceAll("\\r\\n|\\r|\\n", " "));
        }
        if(salesres.get(accountnum) != null){
        	cust.setInfo(Converter.enCodeEst("CHART Müük :|Kuu aasta|müük |" )+ salesres.get(accountnum) );
        }
        
        if(transa.get(cust.getID())!= null){
        	String ppp = (String)transa.get(cust.getID());
        	if(ppp.startsWith(",")){
        		ppp = ppp.substring(1);
        	}
        	if(cust.getCity().length()>1){
        	 	cust.setCity(cust.getCity() + "|" +Converter.enCodeEst("Veopäev : ") + ppp);
        	}else{
        	 	cust.setCity(Converter.enCodeEst("Veopäev : ") + ppp );
        	}
        }
        
        
        if(visits.get(cust.getID())!= null){
        	cust.setVisit(visits.get(cust.getID())+"");
        }
        
        
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0 && grupp.indexOf("Stand") == -1) {
          cust.setGroup(grupp.trim());
          cust.setCustType(grupp.trim());
        }
       
        if(promogroups.get(cust.getID()) != null){
        	if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
        		cust.setGroup(cust.getGroup() + "|" + (String)promogroups.get(cust.getID()));
        	}else{
        		cust.setGroup((String)promogroups.get(cust.getID()));
        	}
        	//System.out.println("grupp " + cust.getGroup());
        }
        
        String chain = rs.getString("chain");
                    if (chain != null && chain.trim().length()>0) {
                      cust.setChain(chain.trim());
                    }
                    
                    int maxallow = rs.getInt("maxallow");
                    
                    if(maxallow == 1){
                    	cust.setMaxdisc(1);
                    }
                    
                    String ette = rs.getString("ette");
                    if (ette != null && ette.trim().equals("PRAPM")) {
                     	cust.isSalesBlockPrepay();
                    }
                    

                    String cuinfo = rs.getString("cuinfo");
                    if (cuinfo != null && cuinfo.trim().length() > 2) {
                     	cust.setInfo(cuinfo.trim().replaceAll("\\r\\n|\\r|\\n", " ").replaceAll(";", ":"));
                    }
                    
                    double minam = rs.getDouble("minam");
                    if(minam > 0 ){
                 	   cust.setMinsum(minam);
                    }
                    
                    String cmd3 = " SELECT     TOP (100) PERCENT ASSETID + ' ' + NAME AS Kylmik ,ASSETID as idd FROM " +  
                            " ASSETTABLE  WHERE (DATAAREAID = 'ade') and  LOCATION='" + accountnum + "'";

                          ResultSet rs11 = stm.executeQuery(cmd3);
                           boolean first = true;
                                 while(rs11.next()){
                               	  if(first){
                               		  cust.setCity( cust.getCity() + "|asset:");
                               	  }
                               	  first = false;
                                   String inff = rs11.getString("Kylmik");
                                   cust.setCity(cust.getCity() + "|" + inff );
                              //   System.out.println(cust.getCity());
                                 }
                                rs11.close();
                                

                                int sample = rs.getInt("sample");
                                if(sample == 1){
                             	   cust.addAgent(new Agent("SAM_1"));
                                }
                    
                    
                    if(goes){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }

      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(round(rs.getDouble("tasumata"),2));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt);
                      }
  rs.close();
  stmt.close();
  con.close();
  stm.close();
  

    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() {
    try {
		return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	} catch (SQLException e) {
		parent.logerror(e);
		e.printStackTrace();
		System.exit(0);
	}
    return null;
  }


      public String getDistribution() {
        return null;
      }

private TreeMap groups = null;

      public TreeMap getProdGroups() throws SQLException {

    	  if(groups == null){
    		  
    	  
    	    Connection con = getNewConnection();
    	    TreeMap   tm = new TreeMap();
    	    this.setTreeGroup(true);
    	    String    sql = "   select ITEMSUBGROUPID as liik , name as nimi from INVENTITEMSUBGROUP where dataareaid ='ade' ";
    	    Statement st = con.createStatement();
    	    ResultSet rs = st.executeQuery(sql);

    	    while (rs.next()) {
    	    	   String abi = rs.getString("liik").trim();
    	    	 if(abi.length() > 4){
    	    		 abi = abi.substring(0,4);
    	    	 }else{
    	    		 if(abi.length() != 4){
    	    			 abi = abi + "0";	 
    	    		 }
    	    		 if(abi.length() != 4){
    	    			 abi = abi + "0";	 
    	    		 }
    	    	 }
    	      String liik = abi;
    	      String nimi = rs.getString("nimi").trim();
    	     // int count = nimi.length() - nimi.replace(" ", "").length();
    	     // while(count > 3){
    	    //	  nimi = nimi.substring(0, nimi.lastIndexOf(" "));
    	    //	 count = nimi.length() - nimi.replace(" ", "").length();
    	     // }
    	      if(liik.equals("WINE")){
    	    	  nimi = "Wine";
    	      } 
    	      
    	      if(liik.equals("SPAR")){
    	    	  nimi = "Sparkling";
    	      }
    	      
    	        tm.put(liik,  Converter.enCodeEst(nimi.toLowerCase()));

    	      //  tm.put("bs" + liik,  Converter.enCodeEst(nimi.toLowerCase() + " BS") );
    	    }
    	    rs.close();
    	    st.close();
    	    con.close();
           groups = tm;
    	    return tm;
    	  }else{
     		 return groups; 
     	  }
    	}


  public TreeMap getProdGroupsSecond() throws SQLException {

  TreeMap   tm = new TreeMap();
	  Connection con = getNewConnection();
	    
	  String    sql = "  select  inv.ITEMSUBGROUPID as abi , BRANDID as liik , name as nimi from SPIBRANDTABLE sp inner join inventtable inv " + 
       " on sp.BRANDID = inv.SPIBRANDID and sp.dataareaid=inv.dataareaid "
       + " where sp.dataareaid ='ade' group by BRANDID,inv.ITEMSUBGROUPID, sp.name " +
       "  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	 String abi = rs.getString("abi").trim();
    	 if(abi.length() > 4){
    		 abi = abi.substring(0, 4);
    	 }else{
    		 if(abi.length() != 4){
    			 abi = abi + "0";	 
    		 }
    		 if(abi.length() != 4){
    			 abi = abi + "0";	 
    		 }
    	 }
      String liik = rs.getString("liik").trim();
      tm.put(abi+"."+liik,Converter.enCodeEst( rs.getString("nimi").trim()+ " " + abi.substring(0,1) + liik));
         }
    rs.close();
    st.close();
    con.close();

    return tm;
  }

  public TreeMap getProducts(Agent agent){

    String cmd = "   SELECT inv.itemid, inv.itemname AS nimetus, inv.taxpackagingqty AS kastis,inv.standardpalletquantity AS alusel, disc.amount AS price  " + 
" , bar.itembarcode AS ean ,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2,mod.unitid As unit, '22' as vat,mod.LineDisc as alegr  " +
 " , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti, NEWITEM as uus ,sellout as out , topitem as tops , giftbox as gift , SPIDONTCONTROLMINAMOUNTMS as nocount FROM inventtable inv  " +
"  left join InventItemSalesSetup s on s.itemid = inv.itemid and s.dataareaid=inv.dataareaid left join pricedisctable as disc on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid " +
"  and disc.module = 1  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation = 'Standard' and (disc.todate > getdate() or year( disc.todate ) = 1900) and  (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 )" +
"  left join inventitembarcode bar   on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid and bar.barcodesetupid ='EANBottle'  and useforprinting = 1  " +
" left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
" WHERE  inv.dataareaid='ade'    AND mod.dataareaid='ade'   " +
"  AND moduletype=2 and active = 1  and inv.ITEMID not like  'AM%' and inv.ITEMID not like  'NE%' and inv.ITEMGROUPID not like  'Taara1' " +
"  order by inv.itemid, disc.todate desc ";
    
    HashMap prodHash = new HashMap();
    TreeMap products = new TreeMap();
    
    try { 

    	
 
 HashMap   laoseis = new HashMap();

String sql = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  " +
        "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
        "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid        " +
     " WHERE  inv.DATAAREAID='ade' and CLOSED = 0 AND moduletype=0 and dim.inventlocationid in ('VIA3L-OP','VIA3L-EXC','VIA-EXC') " +
	"	GROUP BY  summ.itemid  having sum(availphysical) >0 ";
   
sql = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  with(nolock) " +
	     "    INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " + 
	     " WHERE  summ.DATAAREAID='ade' and CLOSED = 0  and dim.inventlocationid in ('VIA3L-OP','VIA3L-EXC','VIA-EXC') " +
		"	GROUP BY  summ.itemid  having sum(availphysical) >0 ";
Connection con = this.getNewConnection();
Statement st = con.createStatement();
 ResultSet  rs = st.executeQuery(sql);

while (rs.next()) {
String item = rs.getString("toode").trim();
Toode tt = new Toode(item);
tt.setKogus(rs.getDouble("stock"));
laoseis.put(item, tt );
}
rs.close();
st.close();


HashMap   braak = new HashMap();
 sql = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  " +
       "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
       "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid        " +
    " WHERE  inv.DATAAREAID='ade' and CLOSED = 0 AND moduletype=0 and dim.inventlocationid in ('VIA3L-WA') " +
	"	GROUP BY  summ.itemid  having sum(availphysical) >0 ";
 sql = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  with(nolock) " +
	     "    INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " + 
	     " WHERE  summ.DATAAREAID='ade' and CLOSED = 0  and dim.inventlocationid in ('VIA3L-WA') " +
		"	GROUP BY  summ.itemid  having sum(availphysical) >0 ";
 st = con.createStatement();
  rs = st.executeQuery(sql);

while (rs.next()) {
String item = rs.getString("toode").trim();
Toode tt = new Toode(item);
tt.setKogus(rs.getDouble("stock"));
braak.put(item, tt );
}
rs.close();
st.close();


    
     if (isDebug()) System.out.println(cmd);
 

    	Statement stmt =  con.createStatement();
      rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus =  rs.getString("nimetus").trim() ;
        nimetus =  Converter.enCodeEst(nimetus.replaceAll("bottle", "bot.").replaceAll("Reserve", "Res.")
        		.replaceAll("Vodka", "Vod.").replaceAll("Cosmopilitan", "Cosmo.").replaceAll("Premium", "Prem.").replaceAll("Chardonnay", "Char.").replaceAll("Cabernet", "Cab.").replaceAll("Sauvignon", "Sauv.").replaceAll("Rouge", "Rou.").replaceAll("Blanc", "Bla."));
   
        
        
         
        prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        	if(ean.startsWith("0")) {
        		ean = ean.substring(1);
        	}
        prod.setEAN(ean.trim());
        
        if(eanprods == null) {
    		eanprods = new TreeMap();
    	}
  
    eanprods.put(prod.getEAN(), prod.getID());
        int ll  = ean.trim().length();
        if(ean.length() >4){
            add = " " +  ean.trim().substring(ll-4);	
        }
       }
        
        boolean goes = true;
  

        String taara = "";
        String grupp = rs.getString("grupp");
        String abi = grupp;
       if(grupp!=null && grupp.trim().length()>0){
    	    if(abi.length() > 4){
          		 abi = abi.substring(0, 4);
          	 }else{
          		 if(abi.length() != 4){
          			 abi = abi + "0";	 
          		 }
          		 if(abi.length() != 4){
          			 abi = abi + "0";	 
          		 }
          	 }
            
    	    prod.setGroup(abi);  
    		
        	 String grupp2 = rs.getString("grupp2");
             if(grupp2!=null && grupp2.trim().length()>0){
          	   prod.setGroupSecond(abi + "." + grupp2.trim());
             }
        	
     }
       
      
       if(braak.get(kood) != null){
    	   Toode tt = (Toode)braak.get(kood);
    	   double lseis = tt.getKogus();
    	   if(lseis > 0){
    		   prod.setPartii("Def.:" + (int)lseis);
    		   nimetus = nimetus + " @BR:" +(int)lseis;
    	    }
    	   }
  
       
       if(laoseis.get(kood) != null){
    	   Toode tt = (Toode)laoseis.get(kood);
    	   double lseis = tt.getKogus();
    	   if(lseis > 0){
    		   prod.setStock((int)lseis);
    	   }
    	   }else if(prod.getID().startsWith("32")){
    		   goes = false;
    	   }
       
       
       //lisa kilo hind 
       if(prod.getID().startsWith("32")){
    	   taara = "x ";
       }
      
      prod.setName(Converter.enCodeEst(taara + nimetus + add));

     
      
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("24");
    }
  
      String alegr = rs.getString("alegr");
      if(alegr!=null && alegr.trim().length()>0){
       prod.setAlegrupp(alegr);
    }
   
      String type = rs.getString("type");
      if(type!=null && type.trim().length()>0){
      // prod.setType(type);
      }

      int mini = rs.getInt("mini");
      int opti = rs.getInt("opti");
      if(opti != mini && mini > 1){
    	  prod.setMinAmountInUnits(mini);
      }else if(opti > 1){
    	  prod.setOptAmountInUnits(opti);
      }
      // NEWITEM as uus ,sellout as out , topitem as tops
      java.sql.Date uus  = rs.getDate("uus");
      if(uus!=null && uus.after(new java.sql.Date((new java.util.Date()).getTime()))){
       prod.setStatusNew("1");
    }
      int out = rs.getInt("out");
      if(out == 1){
    	  prod.setStatusSellout("1");
      }

      int topitem = rs.getInt("tops");
      if(topitem == 1){
    	  prod.setStatusTop("1");
      }

      int giftitem = rs.getInt("gift");
      if(giftitem == 1){
    	  prod.setStatusGiftPack("1");
      }

      int  nocount = rs.getInt("nocount");
      if(nocount == 1){
    	 prod.sumNoCount = true;
      }
      
      if(goes){
     	  products.put(taara + nimetus, prod);
          prodHash.put(prod.getID(), prod);  
    
      }
       

      }
      rs.close();
      stmt.close();
      con.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }
// see on vana 
	  cmd = "  select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind , fromqty as kogus " +
      "  from astPromoGMLine l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum " + 
      "  where l.dataareaid = 'ade' and (GETDATE() between PRI.FROMDATE-1 AND PRI.TODATE-1) " +
	  "  and fromqty > 0  and accepted=2 and promostatus in (1,4)  ";
	  //See on uus 
	String  cmdu =  " select PRI.PROMONUM as grupp,l.ITEMID as toode ,astfinalprice as hind , fromqty as kogus ,astpromopercent as perc " +
			     " from astPromoItems l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum   " +
			     " where l.dataareaid = 'ade' and (GETDATE() between PRI.FROMDATE-1 AND PRI.TODATE-1) " +
			     " and promostatus in (1,4) and maxdiscountpromo = 1 ";
		
	    StringBuffer buf = new StringBuffer();
		try {
			
			Connection con = this.getNewConnection();
		    Statement stmt =  con.createStatement();
			ResultSet	      rs = stmt.executeQuery(cmd);

		    while(rs.next()){
    	        String  gruppx = rs.getString("grupp").trim();
              	String  toodex = rs.getString("toode").trim();
              	try{
              		double  hind = ((Product)prodHash.get(toodex)).getPrice();
                  	double  kogus = rs.getDouble("kogus");
                  	double  perc = rs.getDouble("perc");
                  	double  lined = 0;//rs.getDouble("linedisc");
                  	
                  	if(kogus == 0){
                  		kogus = 1;
                  	}
                  	double hl = (hind -lined) *(100-perc)/100;
                  	// siia teha number format 
                  	
                  	      buf.append(toodex+ "; ; ;" + NF.format(hl).replace(',', '.') + ";" + gruppx + ";" + kogus + ";"  );
                          buf.append("\n");	
              		
              	}catch(Exception e){
              		
              	}
              }	
		    
	           rs.close();
	           stmt.close();
	           con.close();
		}catch (Exception e ){
			e.printStackTrace();
		}
		

    
    if(buf.length() > 2){
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write(buf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }else{
        File f = new File("prodactions.txt");
        if (f.exists())f.delete();
          try {
    		PrintWriter os = new PrintWriter(new FileWriter(
    				 "prodactions.txt", true));
    		os.write("");
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    
    
    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();

	 int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok  " +
	   "    FROM custinvoicetrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid   " + 
	   "    WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid ='ade'  AND jou.dataareaid = 'ade'   " +
	   "    and tra.qty !=0 and tra.itemid != '' " +
	   "    GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	    "   ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty" ;

	//   and blocked = 0 
	    if (isDebug()) System.out.println(cmd);

	    try {
	    	Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("qty");
	  
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	     	        String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	        //cp.setAbiKood(bill);

	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	      if(counter< 4){
	      	try{
	      		Connection con = this.getNewConnection();
		    	Statement stmt =  con.createStatement();
	        	 ResultSet rs = stmt.executeQuery(cmd);
	             counter++;
	             while(rs.next()){
	               String kliendikood = rs.getString("orderaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               double kogus = rs.getDouble("qty");
	         	  
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	                        double hind = rs.getDouble("hind");
	    	     	        String bill = rs.getString("dok");
	    	     	       CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	    	   	        cp.setAbiKood(bill);

	               custProds.add(cp);
	             }
	             rs.close();
	             stmt.close();
	             con.close();
	             
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

  


  private TreeMap makePlans(){
	   TreeMap res = new TreeMap();
	  

	   
					    
			    String cmd =" select eagentaccountid as agent , itemsubgroup as grupp , salesamount as planx,focussubgroup "
			    		+ " from ASTSalesAgentForecast where year(transdate)=year(getdate())"
			    		+ " and  month(transdate) = month(getdate()) and dataareaid = 'ade' and modelid = 'B_2024'";

			       if (isDebug()) System.out.println(cmd);
			          try {
			        	  Connection con = this.getNewConnection();
			              Statement stmt =  con.createStatement();
			              ResultSet	rs = stmt.executeQuery(cmd);
			              while(rs.next()) {
			                  String agentID = rs.getString("agent").trim();
			                  String grupp = rs.getString("grupp").trim();
				               double plan = rs.getDouble("planx");
				               int focussubgroup = rs.getInt("focussubgroup");
				               
				               if(focussubgroup == 1){
				            	   res.put(agentID+"|"+grupp,plan);
					           }
			                  
			                  if(res.get(agentID) != null){
			                	  double pl = Double.parseDouble(res.get(agentID)+"");
			                	  res.put(agentID,plan+pl);
			                	  
			                  }else{
			                	  res.put(agentID,plan);
			                  }
			                  
			                  
			                  
			              }
			              rs.close();
			              stmt.close();
			              con.close();
			          
				          
			          }catch(Exception e){
			        	  e.printStackTrace();
			          }
			

		   		System.out.println("jooksevplaan " + res.toString());
		
	   return res;
}



 
  
  
  
  public void setBulkSales(Map agents){
this.connectionUp();
  
    TreeMap plans = makePlans();
   // System.out.println("plans " + plans.toString());
	 int yy = Calendar.getInstance().get(Calendar.YEAR);
   int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
   int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

    
    String cmd ="   SELECT  cus.SPIEAGENTACCOUNTID as agent,  " +
   "  round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) then lineamount else 0  end),0) AS summa, " +
	 "	 round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate())-1 then lineamount else 0  end),0) AS summalast " +  
   "    FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
   "    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
	 "	 WHERE inv.dataareaid='ade' AND sal.dataareaid='ade' AND cus.dataareaid='ade'  AND jou.dataareaid='ade' AND ((sal.invoicedate >= DATEADD(month, DATEDIFF(month, 0, getdate()), 0) \r\n" + 
	 "     AND sal.invoicedate <  DATEADD(month, DATEDIFF(month, 0, getdate())+1, 0) )\r\n" + 
	 "       OR (sal.invoicedate >= DATEADD(month, DATEDIFF(month, 0, getdate())-12, 0)  \r\n" + 
	 "    AND sal.invoicedate <  DATEADD(month, DATEDIFF(month, 0, getdate())-12+1, 0) ))            \r\n" + 

   "      and  cus.inventlocation != '' and inv.ITEMGROUPID not in  ('TARA','POS') and cus.SPIEAGENTACCOUNTID != '' GROUP BY cus.SPIEAGENTACCOUNTID ";

       if (isDebug()) System.out.println(cmd);
          try {
          	Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
              
                
                
                Agent agent = (Agent)agents.get(agentID);
                
                if (agent != null){
              	  double sales = rs.getDouble("summa");
                 // agent.setMonthSales((int)sales);
                 int summalast = (int)rs.getDouble("summalast");
                 agent.setStart(Converter.enCodeEst("Sama kuu, eelmine aasta :|") + summalast + "€||" );  
                 
                 agent.buffer = (int)(sales);

           	  double msales = agent.buffer;
              String asi = Converter.enCodeEst("Müük: ") + (int)msales + " €|";

           	  
           	  
           	  double psales = 0;
       		try{
       	
       			psales = Double.parseDouble(plans.get(agent.getID())+"");
       		  double  tehtudsalperc = msales/psales;
           	  double plaanitavMyykSal = d2 * msales/dd;
           	  double  prognosalperc = plaanitavMyykSal/psales;
          
             asi = asi + Converter.enCodeEst("Müügi plaan: ") + (int)psales + " €|";    
             if(dd > 6){
           	  asi = asi + Converter.enCodeEst("Plaan tehtud/Prognoos :");
           	  if(tehtudsalperc > 3){
           		tehtudsalperc = 0;
           	  }
           	if(prognosalperc > 3){
           		prognosalperc = 0;
           	  }
           	  
                 asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %|";  
             }
             
              agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
               
       		}catch(Exception e){
       		 // e.printStackTrace();
       	  }
       	 System.out.println(agentID + "' " + asi  );
         
       		
       	     }
                }
              rs.close();
              stmt.close();
              
              
              
               cmd ="     SELECT  cus.SPIEAGENTACCOUNTID as agent,  SUM(lineamount ) AS summa, inv.ITEMSUBGROUPID as grupp , ITEMGROUPID as type " +
        	     "  FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
               "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
        	 " WHERE inv.dataareaid='ade' AND sal.dataareaid='ade' AND cus.dataareaid='ade'  AND jou.dataareaid='ade' AND "
        	 + " MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) " +
            "      and  cus.inventlocation != '' and inv.ITEMGROUPID not in  ('TARA','POS')and inv.ITEMSUBGROUPID != ''and inv.ITEMGROUPID != '' and cus.SPIEAGENTACCOUNTID != '' GROUP BY cus.SPIEAGENTACCOUNTID ,ITEMSUBGROUPID,ITEMGROUPID";

                 if (isDebug()) System.out.println(cmd);
                    
                    	        stmt =  con.createStatement();
                        	      rs = stmt.executeQuery(cmd);
                        while(rs.next()) {
                          String agentID = rs.getString("agent").trim();
                          Agent agent = (Agent)agents.get(agentID);
                          if (agent != null){
                        	  double sales = rs.getDouble("summa");
                        	String grupp = rs.getString("grupp").trim();
                        	String type = rs.getString("type").trim();
                           agent.buffer = (int)(sales);

        //K2|RTD=47953.568154633176, K2|SPIRIT_DR=28015.803217184315, 
 //K2|VODKA|LB=43991.97816010396, R11=324834.90552250843, R11|RTD=61743.06907171655,
  //R11|SPIRIT_DR=41825.6858004074, R11|VODKA|LB=48906.82130825486}
                     	  double msales = agent.buffer;
                 		  Double psaless = null;
                 		try{
                 		  psaless = Double.parseDouble(plans.get(agent.getID()+"|"+ grupp+"|"+type)+"");
                 		}catch(Exception e){
                   		 // e.printStackTrace();
                   	  }
                 		 if(psaless != null){
                 			double psales = psaless;
                 		  double  tehtudsalperc = msales/psales;
                     	  double plaanitavMyykSal = d2 * msales/dd;
                     	  double  prognosalperc = plaanitavMyykSal/psales;
                       String asi =  " | " +grupp +"-"+type + " " + Converter.enCodeEst("müük: ") + (int)msales + " €|";
                       asi = asi +grupp +"-"+type + " " + Converter.enCodeEst("plaan: ") + (int)psales + " €|";    
                       
                       if(dd > 6){
                     	  asi = asi + Converter.enCodeEst("Plaanist tehtud/Prognoos :");
                           asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                       }
                       
                        agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
                        
                 		 }
                 	
                 	     }
                          }
                        rs.close();


                        
                        cmd ="     SELECT  cus.SPIEAGENTACCOUNTID as agent,  SUM(lineamount ) AS summa, inv.ITEMSUBGROUPID as grupp  " +
                 	     "  FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
                        "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
                 	 " WHERE inv.dataareaid='ade' AND sal.dataareaid='ade' AND cus.dataareaid='ade'  AND jou.dataareaid='ade' AND "
                 	 + " MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) " +
                     "      and  cus.inventlocation != '' and inv.ITEMGROUPID not in  ('TARA','POS') and inv.ITEMSUBGROUPID != '' and cus.SPIEAGENTACCOUNTID != '' GROUP BY cus.SPIEAGENTACCOUNTID ,ITEMSUBGROUPID";

                          if (isDebug()) System.out.println(cmd);
                             
                             	        stmt =  con.createStatement();
                                 	      rs = stmt.executeQuery(cmd);
                                 while(rs.next()) {
                                   String agentID = rs.getString("agent").trim();
                                   Agent agent = (Agent)agents.get(agentID);
                                   if (agent != null){
                                 	  double sales = rs.getDouble("summa");
                                 	String grupp = rs.getString("grupp").trim();
                                    agent.buffer = (int)(sales);

                 //K2|RTD=47953.568154633176, K2|SPIRIT_DR=28015.803217184315, 
          //K2|VODKA|LB=43991.97816010396, R11=324834.90552250843, R11|RTD=61743.06907171655,
           //R11|SPIRIT_DR=41825.6858004074, R11|VODKA|LB=48906.82130825486}
                              	  double msales = agent.buffer;
                          		  double psales = 0;
                          		  
                          //		  System.out.println("Plans " +agent.getID()+"|"+ grupp + " '"+ plans.get(agent.getID()+"|"+ grupp)+"");
                          		  
                          		try{
                          		  psales = Double.parseDouble(plans.get(agent.getID()+"|"+ grupp)+"");
                          		}catch(Exception e){
                              		//  e.printStackTrace();
                              	  }
                          		 if(psales > 0){
                          			
                          		  double  tehtudsalperc = msales/psales;
                              	  double plaanitavMyykSal = d2 * msales/dd;
                              	  double  prognosalperc = plaanitavMyykSal/psales;
                                String asi =  " | " +grupp  + " " + Converter.enCodeEst("müük: ") + (int)msales + " €|";
                                 asi = asi + grupp + " " + Converter.enCodeEst("plaan: ") + (int)psales + " €|";    
                                
                                if(dd > 6){
                              	    asi = asi + Converter.enCodeEst("Plaanist tehtud/Prognoos :");
                                    asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                                }
                                
                                 agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
                                 
                          		 }
                          	
                          	     }
                                   }
                                 rs.close();
                                 stmt.close();
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
    return "AxaAmber";
    
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }




  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
    
   public String getPilotDayData(Agent agent){

	   String sql2 =  "SELECT sal.recid AS id,case when eorderid like 'MS-%' then 1 else 0 end as abi,cus.name AS nimi ,sal.date AS kuup,eorderid as number , " +
	   	" linecount AS summa FROM ESalesOrderTable sal INNER JOIN custtable cus ON sal.deliverycode=cus.accountnum  and cus.dataareaid= sal.dataareaid  " +
	     "      WHERE sal.dataareaid='ade' AND cus.dataareaid='ade' AND cus.SPIEAGENTACCOUNTID ='" + agent.getID()+ "' AND  " +
	      "     sal.date > getdate()-0.8    ORDER BY sal.date " ;
StringBuffer sb = new StringBuffer();
 this.connectionUp();
try {
	 nf.setMinimumFractionDigits(2);
  Connection con = this.getNewConnection();
PreparedStatement stmt2 = con.prepareStatement(sql2);
//stmt2.setString(1, agent.getID());
String sep= System.getProperty("line.separator");
   ResultSet  rs = stmt2.executeQuery();
double droidsum = 0;
double muusum = 0;
boolean droid = true;

while(rs.next()){
	 String id = rs.getString("id");
	 int abi = Integer.parseInt(rs.getString("abi"));
    String nimi = rs.getString("nimi");
    if (nimi == null){
    nimi = " ";
    } else {nimi = nimi.trim();}

	sb.append(nimi + "\n");
	sb.append(rs.getString("kuup").trim().substring(0,10));
	sb.append("  Nr." + rs.getString("number").trim() + "\t");
	double summa = Double.parseDouble(rs.getString("summa").trim());

	
	String sum = getNF().format(summa) ;
    sb.append(sum + " eur\n" + sep);
  
    String sql3 = "select orderedqty as amount,  unitofmeasure as unit  ,itemdescription as name from " +
			   " ESalesOrderLines rid where rid.dataareaid='ade' and  refrecid = " + id;
    Connection cc = this.getNewConnection();
    PreparedStatement stmt3 = cc.prepareStatement(sql3);
	 
       ResultSet  rs2 = stmt3.executeQuery();
   
	    while(rs2.next()){
	    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
	       + rs2.getString("unit").trim()+ " " + rs2.getString("name").trim() + "\n");
	    }
   
	    rs2.close();
       stmt3.close();
       cc.close();
    	sb.append( "\n\n");
	 	  
    
	if(abi == 0){
 		droidsum = droidsum + summa;
 	}else{
 		muusum = muusum + summa;
 		if(droid && droidsum > 0 ){
 			droid = false;
 			sb.append("------------------------------\n");
 			sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "ridu\n\n");
 		}
 	}
}
rs.close();
stmt2.close();
con.close(); 	 
	if(droid && droidsum > 0 ){
			droid = false;
		//	sb.append("------------------------------\n");
		//	sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
		}

if(muusum > 0){
		//sb.append("------------------------------\n");
		//sb.append("E-tellimused Kokku :" + getNF().format(muusum)  + "eur");
		 
}

} catch (Exception e) {
  getParent().showError(e);
}

connectionDown();

Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent.getID() + "report.txt", sb.toString());
return "";
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
  private String url = "jdbc:odbc:amberbev";
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
    
    
	
	  private void makePictureCat(){
	  	    
	    	
	    	String pictssou = parent.getProps().getProperty("pict.dir");
	  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	       
	  	 try {
       	  
            File f = new File(pictspda +"list.txt");
            if (f.exists())f.delete();
            //Kataloogi sisude kustutamine
        	 	 File res2 = new File (pictspda);
	             String[] ff2 = res2.list();
	         	 for(int i = 0;ff2.length>i;i++){
	             	(new File ( pictspda + ff2[i])).delete(); 
	             }
//	         	 Lõppes kustutamine 
	  		 
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	    	    
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(toode + ".")){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
              	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                    	}
              	  	}   }
	                 }
	               }
	             }
	       }
	         try {
       		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
       		os.write(buf.toString());
       		os.close();
       	  }catch (Exception ex) {
       		ex.printStackTrace();
       	  }
	  	 
	  	 } catch (Exception e) {
	    	    getParent().showError(e);
	    	}
	     
	  }
    
    
}
