package eevaeksport.driver;


import java.io.File;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
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
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaNooDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker,FixLoader {
  public AxaNooDBDriver(EksportPanel parent){
    super(parent);
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
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
  public void connectionUp(){
  }
	public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		

	String sq3 = "  SELECT jou.orderaccount as klient, jou.invoicedate  as kuup , tra.itemid as toode ,\r\n" + 
			"									 sum(case when tra.qty > 0 then tra.qty else 0 end) as kogus,\r\n" + 
			"							 sum(case when tra.qty < 0 then tra.qty else 0 end) as kogusmin, max (jou.salesid) as doknr , \r\n" + 
			"						 case when  jou.invoicedate > CAST(GETDATE() AS DATE)then 1 else 0 end as color FROM NLTCustInvoiceTrans  tra INNER JOIN custinvoicejour jou ON \r\n" + 
			"						 tra.salesid=jou.salesid  and  tra.dataareaid= jou.dataareaid  WHERE  jou.invoicedate in   \r\n" + 
			"						(select top 5 kuupaev  from ( 	 SELECT top 10 jou2.invoicedate as kuupaev FROM  custinvoicejour jou2  where ltrim(jou2.orderaccount) = ltrim(jou.orderaccount)  and jou2.dataareaid= 'nlt'  \r\n" + 
			"						 and jou2.invoicedate > getdate()-14 and jou2.invoicedate < getdate()+4 group by jou2.orderaccount , jou2.invoicedate order by jou2.invoicedate desc) as dd order by kuupaev)  \r\n" + 
			"						 AND tra.dataareaid ='nlt' \r\n" + 
			"				 GROUP BY jou.orderaccount,jou.invoicedate , tra.itemid ORDER BY jou.orderaccount,jou.invoicedate desc, max(jou.salesid) desc, tra.itemid " ;

	     Connection con3 = getNewConnection();
					Statement st3 = con3.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(rs3.getString("kuup").substring(5,10));
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						double kmin = rs3.getDouble("kogusmin");
						
						ml.setMinAm(kmin);
						ml.setLineinfo(rs3.getString("doknr").trim());
						
						int color = rs3.getInt("color");
						if(color == 1) {
							ml.setCliGroup("green");
						}
                        
						if(kmin < 0) {
							ml.setCliGroup("red");
						}
					    
						result.add(ml);
					}
					rs3.close();
					st3.close();
					con3.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Last three " + result.size());

		return result;
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
      String sql = "SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, max(PRICEDISCTABLE.AMOUNT) AS price , max(percent1) as perc " + 
      		"    FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='nlt' AND PRICEDISCTABLE.RELATION in (4,5) AND PRICEDISCTABLE.ACCOUNTCODE=0 AND  " + 
      		"	 PRICEDISCTABLE.itemcode=0  and (PRICEDISCTABLE.AMOUNT > 0.01 or PRICEDISCTABLE.percent1 > 0.01) and  " + 
      		"  ((GETDATE() between PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE+1) " + 
      		"	   or(GETDATE()>=PRICEDISCTABLE.FROMDATE AND year(PRICEDISCTABLE.toDATE) =1900) " + 
      		"	   or(year(PRICEDISCTABLE.FROMDATE) =1900 and year(PRICEDISCTABLE.toDATE) =1900) " + 
      		"	  or( GETDATE()<=PRICEDISCTABLE.TODATE+1) )   " + 
      		"	    GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION " + 
      		"		order by ACCOUNTCODE,ITEMCODE	";

       try {

Connection con = this.getNewConnection();
     	Statement stmt = con.createStatement();
               ResultSet rs = stmt.executeQuery(sql);

               while (rs.next()) {
             	  String prodID = rs.getString("prodid");
                       String custID = rs.getString("custid");
                       double price = rs.getDouble("price");
                       double perc = rs.getDouble("perc");
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
                       if (perc != 0) {
                           mline.setDiscount(perc);
                   }
                     
                       lines.add(mline);
               }
               rs.close();
               
               
               //kampaaniad 
               
           

               String sql4 = " SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind,percent1 as disc  ,PRI.FROMDATE as algus " + 
               		"               		              		 ,PRI.TODATE as lopp,DATEDIFF(DAY ,PRI.FROMDATE, PRI.TODATE)   as kamp ,PRI.RELATION  FROM PRICEDISCTABLE pri  " + 
               		"               			   WHERE PRI.DATAAREAID='nlt' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " + 
               		"               		    ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " + 
               		"               			   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " + 
               		"               			   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " + 
               		"               			   or( GETDATE()<=PRI.TODATE+1) and year(PRI.FROMDATE) =1900 )  and PRI.ACCOUNTRELATION not like 'BAAS10_22%' " + 
               		"               		       ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                                    
                       //and percent1 > 0.1
               
                      rs = stmt.executeQuery(sql4);

                      while (rs.next()) {

                              String clGroup = Converter.cleanLitLatEst(rs.getString("grupp"));
                              String prodID = rs.getString("toode");
                              double price = rs.getDouble("hind");
                              double disc = rs.getDouble("disc");

                              String algus = rs.getString("algus").substring(0,10);
                              String lopp = rs.getString("lopp").substring(0,10);
                              int kuu = rs.getInt("kamp");
                              
                              MatrixLine mline = new MatrixLine();
                              
                          
                              if (clGroup != null && clGroup.trim().length() > 0) {
                                      mline.setCliGroup(clGroup.trim());
                              }

                              if (prodID != null && prodID.trim().length() > 0) {
                                      mline.setProdID(prodID.trim());
                              }
                              
                              if( algus.indexOf("1900")== -1 &&  lopp.indexOf("1900")== -1 ){
                             	
                               if (algus  != null && algus.trim().length() > 0 && algus.indexOf("1900")== -1) {
                                     mline.setStartDate(algus.trim());
                              }

                              if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                                      mline.setEndDate(lopp.trim());
                              }
                              
                              if(kuu > 65  && algus.indexOf("1900")== -1 && lopp.indexOf("1900")== -1){
                             	 mline.setProdLevel("pr");
                              }
                              
                              }
                            
                             	
                             	 mline.setPrice(price);
                                  if(price > 0){
                                      lines.add(mline);
                                  }else if(disc > 0){ 
                                	   mline.setDiscount(disc);
                                	   lines.add(mline);
                                  }
                               
                              
                              
                      }
                      
                      rs.close();
     
                    sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,pri.percent1 as disc ,PRI.FROMDATE as algus " +
                      		" ,PRI.TODATE as lopp,DATEDIFF(DAY ,PRI.FROMDATE, PRI.TODATE)   as kamp " +
          " FROM PRICEDISCTABLE pri " +
    	  " WHERE PRI.DATAAREAID='nlt' AND  PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0  and " +
        "   ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) "+
	  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	  " or( GETDATE()<=PRI.TODATE+1) )  and PRI.ACCOUNTRELATION not like 'BAAS10_22%' " +
        "    ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                             
                             rs = stmt.executeQuery(sql4);

                             while (rs.next()) {

                                     String custID = Converter.cleanLitLatEst(rs.getString("grupp"));
                                     String prodID = rs.getString("toode");
                                     double price = rs.getDouble("hind");
                                     double disc = rs.getDouble("disc");

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
                                     
                                     if ( kuu < 65 && algus.indexOf("1900")== -1  && lopp.indexOf("1900")== -1){
                                         if (algus  != null && algus.trim().length() > 0  && algus.indexOf("1900")== -1) {
                                            mline.setStartDate(algus.trim());
                                     }

                                     if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                                             mline.setEndDate(lopp.trim());
                                     }
                                                                        	
                                     }

                                     if(kuu > 65 ){
                                    	 mline.setProdLevel("pr");
                                     }     
                               
                                     
                                     
                                      //   mline.setPrice(price);
                                         mline.setDiscount(disc);
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
	     " WHERE  summ.DATAAREAID='nlt' and CLOSED = 0  and dim.inventlocationid in ('36','61') " +
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
       
       
       makeActionFile();
       return products.toString();
   }
 
 
 public HashMap getBatch(){
	  
	 HashMap res = new HashMap();
		  
	   String cmd = " select   summ.itemid  AS toode ,sum(AVAILPHYSICAL) AS partii   FROM INVENTSUM summ  with(nolock)\r\n" + 
	   		"	         INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid \r\n" + 
	   		"			 INNER JOIN INVENTBATCH bat on bat.inventbatchid = dim.inventbatchid\r\n" + 
	   		"	      WHERE  summ.DATAAREAID='nlt' and CLOSED = 0  and dim.inventlocationid in ('36','61')\r\n" + 
	   		"		  \r\n" + 
	   		"		  \r\n" + 
	   		"			GROUP BY  summ.itemid  having sum(availphysical) >0";


	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
	    	   Connection con = this.getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           String partii = rs.getString("partii");
	            res.put(kood, partii);
	            
	           }
	           rs.close();
	           st.close();
	           con.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       
	       
	    
	       return res;
	   }


 
 private void  makeActionFile() {
	String cmd = "select lastmodified , id as toode , quantity as kogus , discount , basepricewithdiscount  as hind , batchid as partii,expirationdate as bestbefore,units   from [Tehnika].[dbo].[sales]  ";
	 
		StringBuffer buf = new StringBuffer();
	  
		try {
	
			
			String aaa = "select  NLTITEMBATCH as partii, sum(l.qty) as kogus, max(unitid) as unit from NLTSORDERHEADER d  with(nolock)  INNER JOIN NLTSORDERLINES l on l.orderid = d.orderid and d.dataareaid =l.dataareaid " + 
					"where createddate + CONVERT(DECIMAL(10, 0), createdtime)/ 86400 > (select max(lastmodified)from [Tehnika].[dbo].[sales]) and NLTITEMBATCH != ''"
					+ "and d.dataareaid = 'nlt'   group by NLTITEMBATCH " ;
			
			HashMap laks = new HashMap();

			Connection con = this.getNewConnection();
			Statement  stmt =  con.createStatement();
			ResultSet  rs = stmt.executeQuery(aaa);

	           while(rs.next()){
	        	        
	                  	String  partii = rs.getString("partii").trim();
	                  		double  kogus =  rs.getDouble("kogus");
	                  	
	                  		laks.put(partii, kogus)	;
		              		
	                  
	                  }	
	           rs.close();
	           stmt.close();
	           con.close();
			
			
			 con = this.getNewConnection();
			  stmt =  con.createStatement();
			  rs = stmt.executeQuery(cmd);

	           while(rs.next()){
	        	        
	                  	String  toodex = rs.getString("toode").trim();
	                  	try{
	                  		double  kogus =  rs.getDouble("kogus");
	                  		
		                  	double hl = rs.getDouble("hind");
		                  	String partii =   rs.getString("partii").trim();
		                  	
		                  	if(laks.get(partii) != null) {
		                  		kogus = kogus - Double.parseDouble(laks.get(partii)+"");
		                  	}
		                  	if(kogus > 0) {
		                  		
		                  		String bb = rs.getString("bestbefore").trim();
		                  		String units =   rs.getString("units").trim();
		                  
		                  		String nimetus = "(" + NF.format(kogus) + " " + units + ") " +  bb;
		                  			  buf.append(toodex+ ";" + partii + ";" + nimetus + ";" + NF.format(hl).replace(',', '.') + "; ;0;"  );
	                          buf.append("\n");	
		                  	}
		                  	    
		              		
	                  	}catch(Exception e){
	                  		e.printStackTrace();
	                  	}
	                  }	
	           rs.close();
	           stmt.close();
	           con.close();
		}catch (Exception e ){
			e.printStackTrace();
		}
    String path = getParent().getProps().getProperty("stockrefdir");
      
      if(buf.length() > 2){
    	  
          File f = new File(path + "prodactions.txt");
          if (f.exists())f.delete();
            try {
      		PrintWriter os = new PrintWriter(new FileWriter(path +
      				 "prodactions.txt", true));
      		os.write(buf.toString());
      		os.close();
      	  }catch (IOException ex) {
      		ex.printStackTrace();
      	  }
       }else{
          File f = new File(path +"prodactions.txt");
          if (f.exists())f.delete();
            try {
      		PrintWriter os = new PrintWriter(new FileWriter(
      				path +"prodactions.txt", true));
      		os.write("");
      		os.close();
      	  }catch (IOException ex) {
      		ex.printStackTrace();
      	  }
       }
      
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
	  
    String cmd = " Select custgroup as kood, name from custgroup where dataareaid = 'nlt'" ; 
    		
    
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
    	
    	Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
        Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("kood").trim()));       
          String name = rs.getString("name").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);
          
          agent.setLanguage("EST");
          agent.setCountry("ee");
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
        rs.close();
        stmt.close();
        con.close();
      } catch (Exception e){
        getParent().showError(e);
      }
    
    return agents;
  }

  
  public static float Round(float Rval, int Rpl) {
	   float p = (float)Math.pow(10,Rpl);
	   Rval = Rval * p;
	   float tmp = Math.round(Rval);
	   return (float)tmp/p;
	   }
  
  
  private HashSet horeca = null;
  
  
  
  private TreeMap unsold = null;
  
  
  
  
  public TreeMap getCustomers(Agent agent){
	  horeca = new HashSet();
	  
	  
	

 String cmd = "  SELECT   cus.name,cus.accountnum,cus.invoiceaccount AS payer,  (SELECT sum(co.amountmst ) FROM custtransopen co  with(nolock) inner join CUSTTRANS ct   " + 
 		" 	            	                     on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE   co.duedate<getdate()  and co.dataareaid = 'nlt'     " + 
 		" 		               AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else    " + 
 		" 		 	          cus.invoiceaccount end )) AS debt,  (SELECT sum(co.amountmst ) FROM custtransopen co  with(nolock) inner join CUSTTRANS ct    " + 
 		" 		 	          	                     on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE    co.dataareaid = 'nlt'   " + 
 		" 		              AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else   cus.invoiceaccount end )) AS debta, creditmax  as limit2, " + 
 		" 					  (select max (creditmax) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'nlt')   " + 
 		" 					    as limit ,mandatorycreditlimit as limitgo  ,  cus.linedisc ,   cus.address, '' AS visit ,  custgroup as agent , '' as teles,  (select top 1 numofdays from paymterm pt where pt.paymtermid=	  " + 
 		" 					 (select top 1 paymtermid from custtable where ltrim(accountnum) = ltrim(cus.invoiceaccount) and dataareaid in ('nlt')  ) and pt.dataareaid in ('nlt') ) AS term, " + 
 		"					 (select top 1 numofdays from paymterm pt where pt.paymtermid=	cus.paymtermid and pt.dataareaid in ('nlt') ) as term2 , cus.paymtermid as term3," + 
 		"							 (select top 1 paymtermid from custtable where ltrim(accountnum) = ltrim(cus.invoiceaccount) and dataareaid in ('nlt')) as term4," + 
 		" 						  blocked AS block ,  (select max (blocked) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'nlt')   " + 
 		" 					    as payerblock 	,phone  as info , PriceGroup as grupp , (Select top 1 ccc.name  from custtable ccc  where ccc.accountnum = cus.TALCustChainAccount and dataareaid = cus.dataareaid) "
 		+ " as chain ,  0 as maxallow , '' as ette ,'' as cuinfo ,"
 		+ "   50 as minam , (Select top 1 custaccount  from NLTAbiINactiveClients  where custaccount = cus.accountnum and dataareaid = cus.dataareaid) as ei , cus.email as mail ,  contactpersonid as pers " + 
 		" 			  FROM custtable cus  WHERE   cus.dataareaid in ('nlt')  " ;
 
 
           String ccc = " select invoiceaccount , count (invoiceaccount) from custtable where dataareaid='nlt' group by invoiceaccount having count (invoiceaccount) > 1 ";
 
           String sql = " SELECT  a.accountnum,a.orderaccount AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata,  " + 
           		"                    co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle ,a.POSTINGPROFILE FROM custtransopen co with(nolock)  " + 
           		"				INNER JOIN custtrans a with(nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " + 
           		"                    WHERE co.dataareaid='nlt' AND a.dataareaid='nlt'  and co.duedate < getdate()+5 and a.invoice != '' " + 
           		"					and a.accountnum not in ('1147','1080','2132', '1141') " + 
           		"				 ORDER BY a.accountnum,co.transdate " ;
			  
           
           String salessql = " SELECT  jou.orderaccount as klient,( ltrim(case when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
        "  round(SUM(lineamount),0) AS summa " +
	    "  FROM custinvoicetrans sal with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON  " +
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
		" WHERE sal.dataareaid='nlt'   AND jou.dataareaid='nlt' AND sal.invoicedate > getdate()-200 " +
        " GROUP BY jou.orderaccount ,(YEAR(sal.invoicedate)),(month(sal.invoicedate))  "
        + " order by   jou.orderaccount ,YEAR(sal.invoicedate), month(sal.invoicedate) ";

           try{
        	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
        		
        }catch(Exception e){
        	e.printStackTrace();
        }
          // System.out.println("oioiooi");
       //    tal_visitationdays ,talspartnercode,

           String rout = "select l.custaccount as klient, routeday as route ,v.name from talrouteline l inner join talroutetable t on t.routeid=l.routeid and t.dataareaid=l.dataareaid \r\n" + 
           		" left join vendtable v on v.accountnum = transportcompany and v.dataareaid=l.dataareaid \r\n" + 
           		"           		where len(l.custaccount) > 0 and t.dataareaid='nlt'\r\n" + 
           		"           		order by l.custaccount,routeday";
           
  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();
    HashMap visits = new HashMap();
    HashMap transa = new HashMap();
    HashMap transaname = new HashMap();
    HashMap promogroups = new HashMap();
    HashMap cpersons = new HashMap();
    HashMap tarned = new HashMap();
    
    if(fivePercCust == null) {
    	  Calendar cc = Calendar.getInstance();
    	  System.out.println("tund " + cc.get(Calendar.HOUR_OF_DAY));
    	  
    	if(fivePerc == null) {
    		if(cc.get(Calendar.HOUR_OF_DAY) < 8 ) {
    			fivePerc = getFivePerc();
    				try {
    					Converter.saveObject(fivePerc, "fiveperc.trm");
    					Converter.saveObject(fivePercCust, "fiveperccust.trm");
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			
    		}else {
    			try {
    				fivePerc = (TreeMap)Converter.readObject( "fiveperc.trm");
    				fivePercCust = (TreeMap)Converter.readObject( "fiveperccust.trm");
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (ClassNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
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
	
     rs1 = stmr.executeQuery(rout);
    while(rs1.next()){
    	String klient = rs1.getString("klient").trim();
    	String rrr = rs1.getString("route");
    	String trname = rs1.getString("name");
    	
    	if( trname != null) {
    		transaname.put(klient, trname.trim());
    	}
    	
    	
      if(tarned.get(klient) == null){
    	  tarned.put(klient,"Tarne: " + rrr  );
      }else{
    	  String asi = (String)tarned.get(klient);     	  
    	  tarned.put(klient,asi + "-" +rrr);
      }
    }
   rs1.close();
   stmr.close();

     /*  
    	 stmr = con.createStatement();
    		
         rs1 = stmr.executeQuery(cont);
        while(rs1.next()){
         cpersons.put(rs1.getString("klient").trim(),rs1.getString("asi1").trim());
          
        }
       rs1.close();
       stmr.close();
       */

       
   	 stmr = con.createStatement();
	
     rs1 = stmr.executeQuery(ccc);
    while(rs1.next()){
   payers.add(rs1.getString("invoiceaccount").trim());
      
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
        if(payer == null || payer.trim().length() == 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        	payer = payer.trim();
        }
        
        Boolean addFinance = true;
        //'1147','1080','2132', '1141'
        if(payer != null && (payer.trim().equals("1147")  || payer.trim().equals("1080")  || payer.trim().equals("2132") || payer.trim().equals("1141"))) {
        	     addFinance = false;
        }
      
        if(accountnum.equals("1147")  || accountnum.equals("1080")  || accountnum.equals("2132") || accountnum.equals("1141")) {
   	             addFinance = false;
        }
   
        
        if(addFinance) {
        String debtStr = rs.getString("debt");
        if (debtStr != null){
        	double dd = Double.parseDouble(debtStr);
        	if(dd > 0.01 || dd < -0.01) {
        		 cust.setDebt(round(dd,2));
        	}
         
        }
        String debtStra = rs.getString("debta");
        

        double limit2 = rs.getDouble("limit2");
        
        String limit = rs.getString("limit");
        if (limit != null){
        	try {
        		if(Double.parseDouble(limit.trim()) > 0) {
                    cust.setCredit(limit);	
            	}	
        	}catch(Exception e) {
        	}
        }
        
        int limitgo = rs.getInt("limitgo");
        if(limitgo == 1) {
        	if (debtStra != null){
                cust.setUsedCredit(round(Double.parseDouble(debtStra),2));
              }
        	
        	 if (limit != null){
        		 try {
        			 Double ll = Double.parseDouble(limit);
        			 if(ll > 0) {
        				 cust.setCredit(limit); 
        			 }
        		 }catch(Exception e) {
        		 }
               }
        	 
        	 if(limit2 > 0) {
        		  cust.setCredit((int)limit2 + "");
        	 }
        	 
        }
        
        
    	}
        
        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
           cust.setGroup(Converter.cleanLitLatEst(aleke));
           if(aleke.equals("Horeca")) {
        	   horeca.add(cust.getID());
           }
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
        	age = age.trim();
         cust.addAgent(new Agent(age));
        // cust.setMainAgent(age);
         cust.addAgent(new Agent("310"));
         cust.addAgent(new Agent("311"));
         cust.addAgent(new Agent("312"));
         cust.addAgent(new Agent("313"));

         if(age.equals("760")) {
        	    cust.addAgent(new Agent("400"));
         }
         
         if (age != null && agent1.get(age) != null){
				Agent aa = (Agent)agent1.get(age);
			
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
    
        
        if(accountnum.equals("1301")  || accountnum.equals("2372")) {
        	Iterator itera = agent1.keySet().iterator();
        	while (itera.hasNext()) {
        		cust.addAgent(new Agent(itera.next()+""));
        	}
         }
        
        
        
        String teles = rs.getString("teles");
        if (teles != null && teles.trim().length() > 0) {
         cust.addAgent(new Agent(teles.trim()));
       //  cust.setCity("Tele: " + teles.trim());
        }
    
        if(fivePercCust != null && fivePercCust.get(accountnum) != null ) {
        	cust.setInfo(fivePercCust.get(accountnum) + "" );
    
        }
        
        
        
        String term = rs.getString("term");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
        
        String term2 = rs.getString("term2");
        if(term2 != null && term2.trim().length() >0){
        	cust.setPaymTerm(term2);	
        }
        
        
        String term3 = rs.getString("term3");
        if(term3 != null && term3.trim().length() >0){
        	if(term3.trim().equals("ETTE")) {
        		cust.setSalesBlockPrepay(true);
        	}
        	if(term3.trim().equals("N0")) {
        		cust.setPaymTerm("-5");
        	}	
        }
        
        String term4 = rs.getString("term4");
        if(term4 != null && term4.trim().length() >0){
        	if(term4.trim().equals("ETTE")) {
        		cust.setSalesBlockPrepay(true);
        	}

        	if(term4.trim().equals("N0")) {
        		cust.setPaymTerm("-5");
        	}	}
        
        
        
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
           // goes = false;
          }

        
        String info = rs.getString("info");
       
        if(salesres.get(accountnum) != null){
        	cust.setInfo(Converter.enCodeEst("CHART Müük :|Kuu aasta|müük |" )+ salesres.get(accountnum) );
        }
        if(tarned.get(accountnum) != null){
        	cust.setCity( tarned.get(accountnum)+"" );
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
        
        
        if(transaname.get(cust.getID())!= null){
        	String ppp = (String)transaname.get(cust.getID());
        	if(ppp.startsWith(",")){
        		ppp = ppp.substring(1);
        	}
        	if(cust.getCity().length()>1){
        	 	cust.setCity(cust.getCity() + "|" +Converter.enCodeEst("Vedaja : ") + ppp);
        	}else{
        	 	cust.setCity(Converter.enCodeEst("Vedaja : ") + ppp );
        	}
        }
        
        
        
        if(visits.get(cust.getID())!= null){
        	cust.setVisit(visits.get(cust.getID())+"");
        }
        
        
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0 && grupp.indexOf("BAAS10_22") == -1) {
        	grupp = Converter.cleanLitLatEst(grupp);
        	if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
        		   cust.setGroup(cust.getGroup() + "|" + grupp.trim());
        	          cust.setCustType(grupp.trim());	
        	}else {
        		   cust.setGroup(grupp.trim());
        	          cust.setCustType(grupp.trim());	
        	}
       
        }
       
        if(promogroups.get(cust.getID()) != null){
        	
        	grupp = Converter.cleanLitLatEst((String)promogroups.get(cust.getID()));
        	if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
        		cust.setGroup(cust.getGroup() + "|" + grupp.trim());
        	}else{
        		cust.setGroup(grupp.trim());
        	}
        	//System.out.println("grupp " + cust.getGroup());
        }
        
        String chain = rs.getString("chain");
                    if (chain != null && chain.trim().length()>0) {
                    	chain = chain.trim();
                    	
                    	if(chain.endsWith(" AS") || chain.endsWith(" TÜ")) {
                    		chain = chain.substring(0,chain.length()-3);
                    	}
                    	
                    	if(chain.contains("RIMI")) {
                    		chain = "RIMI";
                    	}
                    	
                    	if(chain.contains("COOP")) {
                    		chain = "COOP";
                    	}
                    	
                    	if(chain.contains("KAUPM")) {
                    		chain = "KAUPMEES";
                    	}
                    	
                    	if(chain.contains("PRISMA")) {
                    		chain = "PRISMA";
                    	}
                    	
                    	
                    	if(chain.contains("HORECA")) {
                    		chain = " ";
                    	}
                    	
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
                 	 //  cust.setMinsum(minam);
                    }
                    
                    String ei = rs.getString("ei");
                    if (ei != null){
                    	goes = false;
                    }
                    String mail  = rs.getString("mail");
                    if (mail != null && mail.trim().length() > 0 ){
                    	cust.setPersonalData(mail);
                    	mail = " " + mail.trim();
                    	
                    }else {
                    	mail = "";
                    }
                    String pers  = rs.getString("pers");
                    if (pers != null && pers.trim().length() > 0 ){
                    	String pp = (String)cpersons.get(pers.trim());
                    	if(pp != null) {
                    		cust.setPersonalData(pp + mail);
                    	}
                    	
                    }
                    
                    if (info != null && info.trim().length()>2) {
                    	if(info.trim().startsWith(",")){
                    		info = info.trim().substring(1);
                    	}
                    	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 2) {
                    	     cust.setPersonalData(cust.getPersonalData() +  "|Tel:" + info.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
                             
                    	}else {
                    	     cust.setPersonalData( "Tel:" + info.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
                                		
                    	}
                     }
                    
                    
                    
                   if(payers.contains(cust.getID())) {
                	   goes= false;
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

private TreeMap groups = null;

      public TreeMap getProdGroups() throws SQLException {

    	  if(groups == null){
    		  
    		  teised = new TreeMap();
    	    Connection con = getNewConnection();
    	    TreeMap   tm = new TreeMap();
    	    HashSet kasut = new HashSet();
    	    
    	   // this.setTreeGroup(true);
    	    String    sql = " select NLTSTATGROUP4ID as gr2 , NLTMAINGROUP as gr1 from NLTStatGroup4 where NLTSTATGROUP4ID in " + 
    	    		" (select NLTSTATGROUP4ID  from INVENTTABLE  where dataareaid ='nlt' and len(NLTSTATGROUP4ID) > 0  group by NLTSTATGROUP4ID) and len (NLTMAINGROUP) > 0";
    	    		
    	    Statement st = con.createStatement();
    	    ResultSet rs = st.executeQuery(sql);
    	    
    	    while (rs.next()) {
    	    
    	      String gr2 = rs.getString("gr2").trim();
    	      String gr1 = rs.getString("gr1").trim();
    	      
    	      if(gr1.length() < 6) {
    	    	  gr1 = gr1 + "000000";
    	      }
    	      
    	      String liik1 = gr1.substring(0, 6);

    	      teisedAlus.put(gr2, Converter.cleanLitLatEst(liik1));
    	      
    	      String nimi1 = gr1.trim();
    	      if(liik1.equals("Grillt")) {
    	    	  nimi1 = "Suvi";
    	      }
    	      
    	      if(liik1.equals("Hakkli")) {
    	    	  nimi1 = "Hakkli";
    	      }
    	      if(liik1.equals("Hakkto")) {
    	    	  nimi1 = "Pihvjm";
    	      }
    	      if(liik1.equals("Jahuta")) {
    	    	  nimi1 = "j.liha";
    	      }
    	      if(liik1.equals("Kulmut")) {
    	    	  nimi1 = "k.liha";
    	      }
    	      if(liik1.equals("Keedu-")) {
    	    	  nimi1 = "Salami";
    	      }
    	      if(liik1.equals("Keeduv")) {
    	    	  nimi1 = "k/vorst";
    	      }
    	      
    	      if(liik1.equals("Keeduv")) {
    	    	  nimi1 = "k/vorst";
    	      }
    	   
    	      if(liik1.equals("M/S000")) {
    	    	  nimi1 = "M/S";
    	      }
    	      if(liik1.equals("Maksap")) {
    	    	  nimi1 = "pastet";
    	      }
    	      if(liik1.equals("Muud s")) {
    	    	  nimi1 = "muu.st";
    	      }
    	      if(liik1.equals("Muud t")) {
    	    	  nimi1 = "muud";
    	      }
    	      if(liik1.equals("Suldid")) {
    	    	  nimi1 = Converter.enCodeEst("Sült");
    	      }
    	      if(liik1.equals("Singid")) {
    	    	  nimi1 = "sink";
    	      }
    	      if(liik1.equals("Snakkt")) {
    	    	  nimi1 =Converter.enCodeEst( "Snäkk");
    	      }
    	      if(liik1.equals("PuitsuP")) {
    	    	  nimi1 = "P/S";
    	      }
    	      if(liik1.equals("PuitsuT")) {
    	    	  nimi1 = "T/S";
    	      }
    	      if(liik1.equals("Talvet")) {
    	    	  nimi1 = "Talv";
    	      }
    	      if(liik1.equals("Viiner")) {
    	    	  nimi1 = "Viiner";
    	      }
    	   

    	        tm.put(Converter.cleanLitLatEst(liik1),  Converter.enCodeEst(nimi1.toLowerCase()));

    	        
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

      
      TreeMap teised = new TreeMap();
      TreeMap teisedAlus = new TreeMap();
      
  public TreeMap getProdGroupsSecond() throws SQLException {
        return teised;
  }

  

  
  
  
  
  public TreeMap getProducts(Agent agent){
 try {
	this.getProdGroups();
} catch (SQLException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
 
 StringBuffer buf = new StringBuffer();
HashMap partiid = getBatch();
 
 HashSet topProds = getTop10();
 
    String cmd = "       SELECT inv.itemid, inv.itemname AS nimetus, inv.iteitemqtyinbox AS kastis, disc.amount AS price   " + 
    		"    		 , bar.itembarcode AS ean ,inv.NLTSTATGROUP4ID AS grupp ,mod.unitid As unit, '20' as vat, mod.LineDisc as alegr  " + 
    		"    		  , '' as type ,0 as mini , 0 as opti, 0 as uus ,0 as out , 0 as tops , 0 as gift , 0 as nocount ,  storageinfo as temp , bestbeforedays as red,  iteitemingredients  as info FROM inventtable inv   " + 
    		"    		   inner join pricedisctable as disc on ltrim(inv.itemid)=ltrim(disc.itemrelation) and disc.dataareaid=inv.dataareaid  " + 
    		"    		  and disc.module = 1  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation like 'BAAS10_22%' and (disc.todate > getdate() or year( disc.todate ) = 1900) and  (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 ) " + 
    		"    		  inner join inventitembarcode bar   on bar.dataareaid = inv.dataareaid  and  ltrim(bar.itemid) = ltrim(inv.itemid) and bar.barcodesetupid ='NLT1' and ltrim(bar.inventdimid) = 'Axapta'   " + 
    		"    		 left JOIN inventtablemodule mod ON ltrim(mod.itemid) = ltrim(inv.itemid) and mod.dataareaid=inv.dataareaid  " + 
    		"    		 WHERE  inv.dataareaid='nlt'    AND mod.dataareaid='nlt'    " + 
    		"    		  AND mod.moduletype=2 and len(inv.NLTSTATGROUP4ID) > 0  and inv.NLTSTATGROUP4ID != 'Taara' and mod.blocked = 0 order by inv.itemid, disc.todate desc ";
    
    HashMap prodHash = new HashMap();
    TreeMap products = new TreeMap();
    
    try { 

    	
    	
    	
    	
 StringBuffer sb = new StringBuffer();
 HashMap   laoseis = new HashMap();

 
String sql = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  with(nolock) " +
	     "    INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " + 
	     "  WHERE  summ.DATAAREAID='nlt' and CLOSED = 0  and dim.inventlocationid in ('36','61') " +
		 "	GROUP BY  summ.itemid  having sum(availphysical) > 0 ";


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


HashMap aleprods = new HashMap();

		 sql = "select id as toode , name , basepricewithdiscount  as hind , baseprice  from [Tehnika].[dbo].[sales]";


		 st = con.createStatement();
		  rs = st.executeQuery(sql);

		while (rs.next()) {
		String item = rs.getString("toode").trim();
		String name = rs.getString("name").trim();
		double hind = rs.getDouble("hind");
		double hindb = rs.getDouble("baseprice");
		Toode tt = new Toode(item);
		tt.setBrand(name);
		tt.setKastis(hind);
		tt.setKogus(hindb);
		aleprods.put(item, tt );
		}
		rs.close();
		st.close();


    
     if (isDebug()) System.out.println(cmd);
 

    	Statement stmt =  con.createStatement();
      rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        
        if(aleprods.get(kood) != null) {
        	aleprods.remove(kood);
        }
        Product prod = new Product(kood);
        String nimetus =  rs.getString("nimetus").trim() ;
       
        if(kood.length() == 5 && (kood.startsWith("2") || kood.startsWith("3"))) {
        	prod.setDiffColor(true);
        }
        
        prod.setInCases(rs.getDouble("kastis"));
       
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        	ean = ean.trim();
        	int len = ean.length();
        	if(len > 12) {
        		  prod.setEAN(ean.substring(2));
        	}else {
        		if(ean.startsWith("0823")) {
        			 prod.setEAN(ean.substring(4));
        		}
        	}
        	
        	if(eanprods == null) {
        		eanprods= new TreeMap();
        	}
      
        eanprods.put(prod.getEAN(), prod.getID());
       }
        
        boolean goes = false;
        
        
        
        if(laoseis.get(kood) != null){
     	   Toode tt = (Toode)laoseis.get(kood);
     	   double lseis = tt.getKogus();
     	   if(lseis > 0){
     		   prod.setStock((int)lseis);
     	   }else {
     		   prod.setStock(0);
     	   }
     	   }
          String grupp = rs.getString("grupp");
        
        if(grupp!=null && grupp.trim().length()>0){
        //	System.out.println("grupp " + grupp + " "+ teisedAlus.toString());
        	if(teisedAlus.get(grupp.trim()) != null) {
        		   prod.setGroup(Converter.cleanLitLatEst(teisedAlus.get(grupp.trim())+""));
        		   if("Jahuta".equals(Converter.cleanLitLatEst(teisedAlus.get(grupp.trim())+""))){
        			   prod.setStock(-1);
        		   }
        	}
        	
    	 
    	    goes = true;
    	}
     
       
     
       if(topProds.contains(kood)) {
    	   prod.setStatusTop("1");
       }
  
      
      String unit = rs.getString("unit");
      String un = "";
      
      if(unit!=null && unit.trim().length()>0){
        prod.setUnit(unit.trim());
        un = unit.trim();
       }
        prod.setName(Converter.enCodeEst(kood +" "+ nimetus + " " + un.toUpperCase()));
        prod.setNameSorting(Converter.enCodeEst(nimetus));
      
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("20");
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
    /*  java.sql.Date uus  = rs.getDate("uus");
      if(uus!=null && uus.after(new java.sql.Date((new java.util.Date()).getTime()))){
       prod.setStatusNew("1");
    }*/
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
      
      String temp = rs.getString("temp");
      if(temp != null) {
    	  
    	  String days = "";
    	  try {
    		   days = rs.getInt("red") + "";
    	    	 
        	  if(days.trim().length() > 0) {
        		  days = " MaxPäevi: " + days ;
        	  }else {
        		  days = "";
        	  }	  
    	  }catch(Exception t) {
    		  
    	  }
    	 
    	  prod.setPartii(temp.trim() + days);
    	  
      }
      
      if(partiid.containsKey(prod.getID())) {
    	  if(prod.getPartii() != null && prod.getPartii().trim().length() > 0) {
    		  prod.setPartii(prod.getPartii() + "|Partii :" + partiid.get(prod.getID()));
    	  }else {
    		  prod.setPartii("Partii :" + partiid.get(prod.getID()) );
    	  }
      }
      
      
 	 String info =rs.getString("info");
	 if(info != null && info.trim().length() > 10){
		 info = info.trim().replaceAll("\\r\\n|\\r|\\n", "|"); 
		   sb.append(prod.getID() + ";" + info + "\n");
	 }




      
      
      if(goes){
     	  products.put(nimetus + " " + kood, prod);
          prodHash.put(prod.getID(), prod);  
    
      }
       

      }
      rs.close();
      stmt.close();
      con.close();
      Converter.saveFileInUTF8("texts.txt", sb);
      
      if(aleprods.keySet().size() > 0) {
    	Iterator itera =  aleprods.keySet().iterator();
    	while(itera.hasNext()) {
    		String key = itera.next()+"";
    		Toode tt   = (Toode)aleprods.get(key);
    		
    		Product prod = new Product(key);
    		prod.setName(tt.getBrand());
    		prod.setPrice(tt.getKogus());
    		  products.put(tt.getBrand()+key, prod);
              prodHash.put(prod.getID(), prod);  
    	}
      }
      
      makeActionFile();
    			
    } catch (Exception e){
      getParent().showError(e);
    }
    
     
    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();

	 int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,tra.lineamount/case when tra.qty = 0 then 1 else tra.qty end as hind ,max(jou.salesid) as dok  , " + 
	   		"	   			   		 (case when jou.invoicedate+bestbeforedays>getdate() then 0 else 1 end)as dat,max(case when jou.invoicedate > getdate()-4 then talsalesinitialqty else 0 end) as lastamount  " + 
	   		"	   			   			      FROM NLTCustInvoiceTrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid    " + 
	   		"	   			   				  INNER JOIN  INVENTTABLE inv ON inv.itemid = tra.itemid and inv.dataareaid = tra.dataareaid " + 
	   		"	   							   WHERE  jou.invoicedate > getdate()-45 AND tra.dataareaid ='nlt'  AND jou.dataareaid = 'nlt'  " + 
	   		"	   			   			        and tra.itemid != ''  " + 
	   		"	   			   			       GROUP BY jou.orderaccount,tra.itemid,bestbeforedays ,jou.invoicedate ,tra.qty ,(tra.lineamount/case when tra.qty = 0 then 1 else tra.qty end) " + 
	   		"	   			   			          ORDER BY jou.orderaccount,tra.itemid ,bestbeforedays,jou.invoicedate desc,tra.qty " ;

	//   and blocked = 0 
	    if (isDebug()) System.out.println(cmd);

	    try {
	    	Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      HashSet olemas = new HashSet();
	      
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("qty");
	  
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	                String bill = rs.getString("dok"); 
	     	         if(bill != null) {
	     	        	bill = bill.trim();
	     	          }
	     	       
	     	        String rline = rs.getString("dat");

	                double open = 0;      
	     	        double lastamount = rs.getDouble("lastamount");
                   
                    
                    
                    
                    double percent = 0;
                    
                    if(kogus>0){
                    	percent =  kogus / lastamount * 100;
                    }
               
                    if(kogus == 0 && lastamount >0){
                    	open = lastamount;
                    }else if (percent < 70){
                    	open = lastamount - kogus ;
                    }
	     	      
                	   if(horeca.contains(kliendikood)) {
                			open = 0;
                		}

	     	     if(open > 0 || kogus != 0 ) {
            	  
            	  if(olemas.contains(kliendikood+tootekood)) {
            		  CustProd cp0 =   (CustProd) custProds.get(custProds.size()-1);
            		  if(!nf.format(hind).equals(nf.format(cp0.getLastPrice()))) {
            			  cp0.setAbiKood("lp1:" + nf.format(cp0.getLastPrice() )+ " lp2:" +nf.format(hind) );
            		  }
            		  
            	  } else {
            		  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,open,0," ");
                	  
            	        cp.setAbiKood(bill);
            	        cp.setRedline(rline);
            	        custProds.add(cp); 
            	        olemas.add(kliendikood+tootekood);
                        
            	  }
            	
      	   }
                    
	       
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
	    	     	        if(bill != null) {
	    	     	        	bill = bill.trim();
	    	     	        }
	    	     	        
	    	     	        
	    	     	        String rline = rs.getString("dat");

	    		               double open = 0;      
	    		     	       double lastamount = rs.getDouble("lastamount");
	    	                 
	    		     	       if(kogus>0) {
	    	                   }else {
	    	                	   open = lastamount;
	    	                   }
	    	                   	
	    	              
	    	                    
	    		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,open,0," ");
	    		        cp.setAbiKood(bill);
	    		        cp.setRedline(rline);

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

  
  private TreeMap getUnSold() {
	
		  TreeMap result = new TreeMap();
				
				String cmd = " SELECT jou.custgroup as agent, jou.deliveryname, tra.name, (jou.invoicedate) AS lastdate , talsalesinitialqty  as lastamount " + 
						"			      FROM NLTCustInvoiceTrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON "
						+ "  tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid     " + 
						"			  INNER JOIN  INVENTTABLE inv ON inv.itemid = tra.itemid and inv.dataareaid = tra.dataareaid  " + 
						"			   WHERE  jou.invoicedate > getdate()-3 AND tra.dataareaid ='nlt'  AND jou.dataareaid = 'nlt'   " + 
						"			        and tra.itemid != '' and tra.qty = 0 and itemgroupid ='LÕPPTOODE' and deliveryname not like '%egust%' " + 
						"		  ORDER BY jou.custgroup, jou.deliveryname ";

				

				
				   try {
			         	Connection con = this.getNewConnection();
			             Statement stmt =  con.createStatement();
			             ResultSet	      rs = stmt.executeQuery(cmd);
			             int agcou = 0;
			             String oldag = null;
			             while(rs.next()) {
			            	
			               String agentID = rs.getString("agent");
			               if(agentID != null) {
			            	   
			               agentID = agentID.trim();
			               if(oldag == null) {
			          		 oldag = agentID;
			          	   }
			            
			              
			               if(agcou < 20) {
			            	   String klnimi = Converter.enCodeEst(rs.getString("deliveryname").trim());
			            	   String tnimi = Converter.enCodeEst(rs.getString("name").trim());
			            	   String ldate = rs.getString("lastdate").trim().substring(5, 10);
			            	   int telle = rs.getInt("lastamount");
			            	   
			            	   if(result.get(agentID) != null) {
			            	       result.put(agentID, result.get(agentID) + "||" + klnimi +  "|   [" + telle + "] " + tnimi + " ("  + ldate +")" );
			            	     } else {
			            		   result.put( agentID, klnimi +  "|[" + telle + "] " + tnimi + " ("  + ldate + ")" );
			            	   }
			                  
			               }
			               agcou++;
			               
			               if(!oldag.equals(agentID)) {
			            	   agcou = 0;
			            	   oldag = agentID;
			               }
			               }
			               }
			         
			             rs.close();
			             stmt.close();
			             con.close();              
			             
			         } catch (Exception e) {
			             getParent().showError(e);
			          }
			       
				return result ;
  }
  
private TreeMap getAlla50() {
	TreeMap result = new TreeMap();
	
	String cmd = " select custgroup as agent , count (jou.deliveryname) as telle , deliveryname from custinvoicejour jou where jou.invoicedate > getdate()-30 \r\n" + 
			"												  AND jou.dataareaid = 'nlt'  and  invoiceamount < 50  and  invoiceamount > 0 group by custgroup,deliveryname\r\n" + 
			"												  order by custgroup,count (jou.deliveryname)desc";

	
	   try {
         	Connection con = this.getNewConnection();
             Statement stmt =  con.createStatement();
             ResultSet	      rs = stmt.executeQuery(cmd);
             int agcou = 0;
             String oldag = null;
             while(rs.next()) {
            	
               String agentID = rs.getString("agent");
               if(agentID != null) {
            	   
               agentID = agentID.trim();
               if(oldag == null) {
          		 oldag = agentID;
          	   }
               int telle = rs.getInt("telle");
               String klnimi = rs.getString("deliveryname").trim();
               if(agcou < 5) {
               
            	   if(result.get(agentID) != null) {
            	       result.put(agentID, result.get(agentID) + "|" + "[" + telle + "] " + klnimi );
            	   }else {
            		   result.put( agentID, "[" + telle + "] " + klnimi );
            	   }
                  
               }
               agcou++;
               
               if(!oldag.equals(agentID)) {
            	   agcou = 0;
            	   oldag = agentID;
               }
               }
               }
         
             rs.close();
             stmt.close();
             con.close();              
             
         } catch (Exception e) {
             getParent().showError(e);
          }
       
	return result ;
}


private HashSet  getTop10() {
	HashSet result = new HashSet();
	
	String cmd = " SELECT top 10 inv.itemid as prod, round(SUM(lineamount),0) AS summa " + 
			"   		  				    FROM custinvoicetrans sal with (nolock) INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid " + 
			"   		 INNER JOIN custinvoicejour jou with (nolock) ON " + 
			"   		   				    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus with (nolock) " + 
			"   		 ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
			"   		   				    inv.dataareaid='nlt'    AND YEAR(sal.invoicedate) = YEAR(getdate()-30) " + 
			"   		   				     AND MONTH(sal.invoicedate) = MONTH(getdate()-30) GROUP BY inv.itemid order by round(SUM(lineamount),0) desc ";

	
	   try {
         	Connection con = this.getNewConnection();
             Statement stmt =  con.createStatement();
             ResultSet	      rs = stmt.executeQuery(cmd);
             int prodcou = 0;
            
             while(rs.next()) {
            
            String prod = rs.getString("prod").trim();
               if(prodcou < 10) {
                 result.add(prod);
            	}
                prodcou++;
               
             }
         
             rs.close();
             stmt.close();
             con.close();              
             
         } catch (Exception e) {
             getParent().showError(e);
         }
       
	return result ;
}

  
  private TreeMap fivePerc = null;
  private TreeMap fivePercCust = null;
  
  private TreeMap  getFivePerc() {
	  TreeMap result = new TreeMap();
	
	  fivePercCust = new TreeMap();
	  
		String cmd = "  SELECT  cus.custgroup as agent, deliveryname as klient , orderaccount  as kood , " + 
				"  SUM(case when YEAR(sal.invoicedate) = YEAR(getdate())  " + 
				"   		   				     AND MONTH(sal.invoicedate) = MONTH(getdate()) then " + 
				"  round(((case when  salesunit = 'tk' then inv.netweight else 1 end )* sal.qty),0) else 0 end )as koguskg1 , " + 
				"  SUM (case when YEAR(sal.invoicedate) = YEAR(getdate()-365)  " + 
				"   		   				     AND MONTH(sal.invoicedate) = MONTH(getdate()-365) AND DAY(sal.invoicedate) <= DAY(getdate()) then " + 
				"  round(((case when  salesunit = 'tk' then inv.netweight else 1 end )* sal.qty),0) else 0 end )as koguskg2 " + 
				"   		  				    FROM custinvoicetrans sal with (nolock) INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid " + 
				"   		 INNER JOIN custinvoicejour jou with (nolock) ON " + 
				"   		   				    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus with (nolock) " + 
				"   		 ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
				"   		   				    inv.dataareaid='nlt'    AND (sal.invoicedate) > getdate()-365  " + 
				"   		   				     GROUP BY cus.custgroup, deliveryname,orderaccount order by 1,3 desc";

		
		   try {
	         	Connection con = this.getNewConnection();
	             Statement stmt =  con.createStatement();
	             ResultSet	      rs = stmt.executeQuery(cmd);
	             int agcou = 0;
	             String oldag = null;
	             while(rs.next()) {
	            	
	               String agentID = rs.getString("agent");
	               if(agentID != null) {
	            	   
	               agentID = agentID.trim();
	               if(oldag == null) {
	          		 oldag = agentID;
	          	   }
	               String klnimi = rs.getString("klient").trim();
	               String kl = rs.getString("kood").trim();
	               double kog1 = rs.getDouble("koguskg1");
	               double kog2 = rs.getDouble("koguskg2");
	               
                   if(kog1 > 0 && kog2 > 0 && kog2-kog1 > 100) {
                	double perc = 100 -( kog1 / kog2 * 100 );
                	
                	if(perc > 5 ) {
                	
	                if(agcou < 5) {
	               
	                	fivePercCust.put(kl, "-" + (int) perc + " % vs eelmine aasta");
	            	   if (result.get(agentID) != null) {
	            	       result.put(agentID, result.get(agentID) + "|" + "-" + (int) perc + " %  " + klnimi );
	            	   } else {
	            		   result.put( agentID, "-" + (int) perc + " % " + klnimi );
	            	   }
	            	   
                   }  
	               }
	               agcou++;
                   }
	               
	               if(!oldag.equals(agentID)) {
	            	   agcou = 0;
	            	   oldag = agentID;
	               }
	               }
	               }
	         
	             rs.close();
	             stmt.close();
	             con.close();              
	             
	         } catch (Exception e) {
	             getParent().showError(e);
	          }
	       
	       
		return result ;
	}

  
  
  public void setBulkSales(Map agents){
    this.connectionUp();
  Calendar cc = Calendar.getInstance();
  System.out.println("tund " + cc.get(Calendar.HOUR_OF_DAY));
  
if(fivePerc == null) {
	if(cc.get(Calendar.HOUR_OF_DAY) < 8 ) {
		fivePerc = getFivePerc();
		try {
			Converter.saveObject(fivePerc, "fiveperc.trm");
			Converter.saveObject(fivePercCust, "fiveperccust.trm");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}else {
		try {
			fivePerc = (TreeMap)Converter.readObject( "fiveperc.trm");
			fivePercCust = (TreeMap)Converter.readObject( "fiveperccust.trm");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

if(unsold == null) {
	  unsold = getUnSold();
}
    
TreeMap telle50 = this.getAlla50();
    
   String cmd =" SELECT  cus.custgroup as agent,round(SUM(lineamount),0) AS summa ,round(SUM((case when  salesunit = 'tk' then inv.netweight else 1 end )* sal.qty),0) as kogus " + 
   		"   				    FROM custinvoicetrans sal with (nolock) INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid "
   		+ " INNER JOIN custinvoicejour jou with (nolock) ON " + 
   		"   				    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus with (nolock) "
   		+ " ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
   		"   				    inv.dataareaid='nlt'    AND YEAR(sal.invoicedate) = YEAR(getdate()) " + 
   		"   				     AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY cus.custgroup " ;

       if (isDebug()) System.out.println(cmd);
          try {
          	Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                
                if (agent != null){
              	  int sales = (int)rs.getDouble("summa");
              	  agent.setMonthSales(sales);
              	int kogus = (int)rs.getDouble("kogus");
            	  agent.setKgSales(kogus);
       		
            	  if (telle50.get(agentID) != null) {
            		 agent.setStartText("viim.30 päeva Alla 50€ tellide arv |" + telle50.get(agentID)); 
            	  }
            	  
            	  if(fivePerc != null && fivePerc.get(agentID) != null) {
            		 if(agent.getStart() != null) {
            			 agent.setStartText(agent.getStartText() + "|| %5 kehvem kui eelmine aasta |" + fivePerc.get(agentID)); 
            		 }else {
            			 agent.setStartText("%5 kehvem kui eelmine aasta |" + fivePerc.get(agentID)); 
            		 }
            	  }
            		
            	  
            	  
            	  if(unsold.get(agentID) != null) {
             		 if(agent.getStart() != null) {
             			 agent.setStartText(agent.getStartText() + "|| Tarnimatta kaubad |" + unsold.get(agentID)); 
             		 }else {
             			 agent.setStartText(" Tarnimatta kaubad |" + unsold.get(agentID)); 
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
    return "AxaNoo";
    
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }




  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
    
   public String getPilotDayData(Agent agent){

	   String   sql2 = " select sal.salesid as number , max(salesname) as nimi,count (salesname) as lines ,sum(lineamount) as summa , max(deliverydate) as kuup from salestable sal  " +
	        " with(nolock)   INNER JOIN salesline ll on ll.salesid = sal.salesid and sal.dataareaid = ll.dataareaid where  sal.dataareaid='nlt' AND sal.custgroup ='"  + agent.getID() + "' and " +  
             "   sal.createddate > getdate()-1  and purchorderformnum like '__0-%'  group by sal.salesid ";
	   
 StringBuffer sb = new StringBuffer();
 this.connectionUp();
try {
	
  Connection con = this.getNewConnection();
PreparedStatement stmt2 = con.prepareStatement(sql2);
//stmt2.setString(1, agent.getID());
String sep= System.getProperty("line.separator");
   ResultSet  rs = stmt2.executeQuery();
double droidsum = 0;
double muusum = 0;
boolean droid = true;

while(rs.next()){
	  String num = rs.getString("number");
	  String nimi = rs.getString("nimi");
	  int lines = rs.getInt("lines");
	  double sales = rs.getDouble("summa");
     
	  if (nimi == null){
       nimi = " ";
        } else {nimi = nimi.trim();}

	sb.append(nimi + "\n");
	sb.append(rs.getString("kuup").trim().substring(0,10));
	sb.append("  Nr." + num.trim() + "\t");

	
	String sum = getNF().format(sales) ;
    sb.append("[" + lines + "] "+sum + " eur\n\n" + sep);
    
}

rs.close();
stmt2.close();
con.close(); 	 


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
  private String url = "jdbc:odbc:axa";
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
	             	if( ff2[i].startsWith(toode + "_")){
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
    
    
}
