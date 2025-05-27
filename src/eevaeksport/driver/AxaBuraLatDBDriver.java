package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaBuraLatDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaBuraLatDBDriver(EksportPanel parent){
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
    if(parent.getProps().getProperty("rate") != null){
    	 try {
    	    rate = Double.parseDouble(parent.getProps().getProperty("rate"));
    	    } catch (Exception e){
    	       parent.showError(e);
    	    }
    }
    
  }

 // private double  rate = 0.702804;
  private double  rate = 1;
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


  public Vector getFixedAssortmentData() {
      Vector lines = new Vector();

      try {
     	  String cmd = "  select  assortgroupid as grupp,assortitemid as toode  from assorttable where dataareaid ='bb' " +
     	  		" and assortcondition =1 order by assortgroupid,assortitemid ";
     	  boolean insertassort = false;
      	
     	  

     	
     	System.out.println(cmd);
     	 Connection cc = getNewConnection();
    	        Statement stmt = cc.createStatement();
    	        ResultSet rs = stmt.executeQuery(cmd);
    	        while(rs.next()){
                       String custID = " ";
                       String clGroup = " ";
                       String prodID = " ";
                       String prodGroup = " ";
                       MatrixLine mline = new MatrixLine();

                       clGroup = rs.getString("grupp");
                       prodID = rs.getString("toode");

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

                     
                       
                       if(insertassort){
         //               insertSortLine(mline , prodGroup);   
                       }
                       lines.add(mline);
                     }

    	       rs.close();
    	       con.close();
    	       cc.close();
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
                 
      return lines;
}
  
  private void  insertSortLine(MatrixLine mline ,String tootegrupp ){
	  try{
			
		      String sql = "INSERT INTO mj_assortment_lat values (" +
		         "'" + mline.getCustID() + "'," + 
		         "getdate()," + 
		         "'" + mline.getCliGroup() + "'," + 
		         "'" + mline.getProdID() + "'," + 
		         "0," + 
		         "0,'" + tootegrupp + "')" ;
		  
		  	     Connection  con2 = getNewConnection2();      
			     Statement stmt2 = con2.createStatement();
			     stmt2.executeUpdate(sql);
			     stmt2.close();
			     con2.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }

	  
  }
  

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into supervisor_lat values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
	       + " '' ,"   //	receivedTime	D,
	       + "?,"   //normalpr	N,
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
		      + "?)" ; //	resint3	L

	       try {
	       	System.out.println("jam");
	        Connection con = getNewConnection2();
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
	             ps.setDouble(9, 0);
	             ps.setDouble(10, 0);
	             ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	             ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	             ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	             ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	             ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	             ps.setBoolean(16, lw.getLine().isAction());
	             ps.setBoolean(17, lw.getLine().isPartner());
	             ps.setBoolean(18, lw.getLine().isBrand());
	             ps.setBoolean(19, lw.getLine().isGroup());
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
	             String cc = Converter.deCode(lw.getLine().getCustComment());
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = Converter.deCode(lw.getLine().getComment());
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = Converter.deCode(lw.getLine().getCustAgentName());
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
	             ps.setInt(28, lw.getLine().getDealNr());
	             ps.setInt(29, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(30, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, lw.getLine().getResInt3()); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }



  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          HashSet kordus = new HashSet();
          
          String cmd = "SELECT     inv.itemid,  round(  mod.price,4)  as price2 " +
           "FROM   inventtable inv   left join inventitembarcode bar on bar.itemid = inv.itemid and " +
            "           bar.dataareaid = inv.dataareaid and useforprinting = 1            inner join inventtablemodule mod on " +
             "          mod.itemid = inv.itemid and   inv.dataareaid = mod.dataareaid and mod.moduletype = 2 " +
             "    inner join InventItemSalesSetup iss on iss.itemid = inv.itemid and   iss.dataareaid = inv.dataareaid " +
                " WHERE   inv.dataareaid = 'bb' and  mod.dataareaid = 'bb' and iss.stopped = 0 and   inv.bbinventsuni9 ='NNY' ";
         

          try {
          Connection cc = getNewConnection();
      	Statement stmt = cc.createStatement();
                ResultSet rs = stmt.executeQuery(cmd);
   Hashtable<String,Double> hinnad = new Hashtable<String, Double>();
                while (rs.next()) {
                 String itemid = rs.getString("itemid").trim();
                 double hind = rs.getDouble("price2");
                 hinnad.put(itemid, hind );
                 
                }
                
                rs.close();
                stmt.close();

          
          
          
         String sql = "  select '' as custid ,PDT.AccountRelation as clgroup ,prodid = ITM.ItemID,price = Convert(Numeric(28, 12),SubString(Max(Convert(Char(1), " +
	        " PDT.SPMPromotion) +Convert(VarChar(8), PDT.FromDate, 112) +Convert(VarChar, PDT.Amount)),10,28)) " +
      " from InventTable ITM inner join PriceDiscTable as PDT with (nolock) on PDT.DataAreaID = 'BB' and " +
    " PDT.ItemRelation = ITM.ItemID where	PDT.Module = 1 and	PDT.AccountCode = 1 and PDT.ItemCode = 0 and " +
    "	PDT.FromDate <= GetDate() + 1 and (PDT.ToDate = '19000101' or PDT.ToDate >= CONVERT(date, GETDATE()))  " + //  and pdt.accountrelation !='_1'
    "	and  PDT.CURRENCY = 'EUR' and pdt.spmpromotion != 1 group by PDT.AccountRelation,	ITM.ItemID ,ITM.namealias order by PDT.AccountRelation,ITM.namealias,prodid";


                  stmt = cc.createStatement();
                   rs = stmt.executeQuery(sql);

                  while (rs.next()) {

                        //  String custID = rs.getString("custid");
                        //  String klient = rs.getString("klient");
                          String clGroup = Converter.cleanLitLatEst(rs.getString("clgroup"));
                          String prodID = rs.getString("prodid");
                         // String prodGroup = rs.getString("prodgroup");
                         // double discount = rs.getDouble("discount");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();
                          
                         
                          /*if (klient != null && klient.trim().length() > 0) {
                              mline.setCustID(klient.trim());
                      }
                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCustID(custID.trim());
                          }*/
                          if (mline.getCustID() == null && clGroup != null
                                          && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }
                          boolean goes = true;

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                                  if(hinnad.get(prodID.trim())!= null){
                                	  if(price == hinnad.get(prodID.trim())){
                                		  goes = false;
                                	  }
                                  }else{
                                	  goes = false;
                                  }
                          }
                          /*if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0) {
                                  mline.setProdGroup(prodGroup);
                          }*/
                          //mline.setDiscount(discount);

                          if (price != 0 && price > 0.01) {
                                  mline.setPrice(price);
                                
                          }
                         
                        
                        if(goes){
                            lines.add(mline);	
                        }
                     
                  }
                  rs.close();
                  
                  
                  String sql2 = "  select  pd.accountrelation AS clgroup,  " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " + 
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
				  "   pd.fromdate as algus , pd.todate as lopp ,amount as price from  " +
				  "   pricedisctable pd where  pd.dataareaid = 'bb' and  pd.fromdate<=CONVERT(date, GETDATE()) and pd.todate>=CONVERT(date, GETDATE()) and pd.accountcode=1  " +				 
				"	 and pd.spmpromotion = 1 and  PD.CURRENCY = 'EUR' "+
				 "    order by pd.accountrelation , pd.itemrelation  " ;
                  
                  rs = stmt.executeQuery(sql2);

                  while (rs.next()) {

                          String clGroup = Converter.cleanLitLatEst(rs.getString("clgroup"));
                          String prodID = rs.getString("prodid");
                          String prodGroup = rs.getString("prodgroup");
                          
                          String fromdate = rs.getString("algus");
                          String todate = rs.getString("lopp");
                          double price = rs.getDouble("price");
                          MatrixLine mline = new MatrixLine();
                          
                      
                          if (clGroup != null && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }
boolean goes = true;
                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                                  if(hinnad.get(prodID.trim())!= null){
                                	  if(price == hinnad.get(prodID.trim())){
                                		  goes = false;
                                	  }
                                  }else{
                                	  goes = false;
                                  }
                          }
                          if (mline.getProdID() == null && prodGroup != null
                                          && prodGroup.trim().length() > 0) {
                                  mline.setProdGroup(prodGroup);
                          }
                          mline.setPrice(price);
                          mline.setStartDate(fromdate.substring(0,10));
                          mline.setEndDate(todate.substring(0,10));
                     
                        if(goes){
                        	lines.add(mline);
                        }
                          
                  }
                  rs.close();
                  stmt.close();
                  cc.close();
                  
                  

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
		StringBuffer sb = new StringBuffer();
	
	      
        String cmd = "SELECT     inv.itemid  " +
         "FROM   inventtable inv   left join inventitembarcode bar on bar.itemid = inv.itemid and " +
          "           bar.dataareaid = inv.dataareaid and useforprinting = 1            inner join inventtablemodule mod on " +
           "          mod.itemid = inv.itemid and   inv.dataareaid = mod.dataareaid and mod.moduletype = 2 " +
           "    inner join InventItemSalesSetup iss on iss.itemid = inv.itemid and   iss.dataareaid = inv.dataareaid " +
              " WHERE   inv.dataareaid = 'bb' and  mod.dataareaid = 'bb' and iss.stopped = 0 and   inv.bbinventsuni9 ='NNY' ";
       

        try {
        Connection cc = getNewConnection();
    	Statement stmt = cc.createStatement();
              ResultSet rs = stmt.executeQuery(cmd);
 
              
 HashSet<String> check = new HashSet<String>();
 while (rs.next()) {
               String itemid = rs.getString("itemid").trim();
             
               check.add(itemid );
               
              }
              
              rs.close();
              stmt.close();

        
		
		
		
		 cmd = "select  PDT.AccountRelation as grupp ,toode = ITM.ItemID from InventTable ITM " +
      " inner join Axdb2009_bb..PriceDiscTable as PDT with (nolock)  on PDT.DataAreaID in ('BB') and PDT.ItemRelation = ITM.ItemID " +
      " where      PDT.Module = 1 and  PDT.AccountCode = 1 and  PDT.ItemCode = 0 and PDT.FromDate <= GetDate() + 1 and " +
      " (PDT.ToDate = '19000101' or PDT.ToDate >= GetDate()) group by   PDT.AccountRelation, ITM.ItemID ,ITM.namealias " +
	  " order by Accountrelation,ITM.namealias,ITM.ItemId";
	
			System.out.println("groupsAG suurus " + groupsAG.size());
			System.out.println(cmd);
			 stmt = cc.createStatement();
			 rs = stmt.executeQuery(cmd);
			 String ag = " ";
			while (rs.next()) {
				String type = Converter.cleanLitLatEst(rs.getString("grupp").trim());
				String id = rs.getString("toode").trim();	
				if(check.contains(id)){
					 ag = " ";
					if(groupsAG.get(type) != null){
						ag = groupsAG.get(type) + "";
					}
					sb.append(" ;" + type + "; ;" + id + "; ;" + ag + "\n");	
					//sb.append(custid + ";" + Converter.cleanLitLatEst(type + "; ;" + abi + "; ;" + prgrupp + "; ;" + color + ";," + ag + ",")
					//+ "\n");
				}
			}

			rs.close();
			stmt.close();
			cc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
  }

  
  public String getSaveStockInfo(){
	  	  
	  String  cmd =  "SELECT  IST.ITEMID as toode, SUM(IST.AVAILPHYSICAL) as stock FROM INVENTSUM IST with (nolock) " +
            "  INNER JOIN INVENTDIM ID with (nolock) ON IST.INVENTDIMID=ID.INVENTDIMID and ID.DATAAREAID=IST.DATAAREAID " +
            "  INNER JOIN INVENTLOCATION ILT with (nolock) ON  ID.INVENTLOCATIONID = ILT.INVENTLOCATIONID and ID.DATAAREAID = ILT.DATAAREAID" +
            "  INNER JOIN INVENTTABLE IT with (nolock) on  IT.ITEMID=IST.ITEMID and   IT.DATAAREAID=IST.DATAAREAID WHERE IST.DATAAREAID='bb'and IST.CLOSED = 0 AND " +
      " ID.INVENTLOCATIONID in (select INVENTLOCATIONID from INVENTLOCATION where DATAAREAID = 'bb' and STOCKEXPORT = 1) and IT.BBINVENTSUNI9 ='NNY' GROUP BY  IST.itemid" +
" HAVING SUM(IST.AVAILPHYSICAL) > 0 union all select itemid as toode, sum(qty) as stock from [Axdb_dt].dbo.[DTInventSum]" +
" where TplPartnerId in (select DTPartner from [Axdb_dt].dbo.[DTPartnerTable] where StockExportDT = 1) and   qty != 0 GROUP BY itemid order by  toode ";

	  
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
	            //   kogused.put(kood, seis + "");
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
/*
  
      DealerProduct dealerProds = new DealerProduct();
      try {

        String  sql = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu FROM INVENTSUM summ " +
               "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid " +
               "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  "+
               "  WHERE summ.DATAAREAID='bb'and dim.DATAAREAID='bb' and inv.DATAAREAID='bb' AND CLOSED = 0 AND  moduletype=0 " +
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

       return dealerProds;*/
	  return null;
     }

  
  
  
  public TreeMap getAgents(){
	  TreeMap agents = new TreeMap(); 
	
	 
	  try {
		  // leiame agentside kaubagrupid 
		  String cmd0 = "select emp.emplid as kood , ITEMSUBGROUPID  as grupp from TraAgentItemGroupTable tra Inner join TraAgentsTable emp  " +
         " on emp.dataareaid= tra.dataareaid and ltrim(rtrim(tra.agent))= ltrim(rtrim(emp.agentsid)) and tra.DataAreaId = 'bb' ";
		      if (isDebug()) System.out.println(cmd0);
		     
		      TreeMap gr = new TreeMap();
		        ResultSet rs = stmt.executeQuery(cmd0);
		          while(rs.next()){
		            String agent = rs.getString("kood").trim();
		            String gru = rs.getString("grupp").trim();
		          
		            Vector ss = (Vector)gr.get(agent);
		            
		              if(ss != null){
		                  ss.add(gru);
		                  gr.put(agent, ss);
		              }else{
		            	  ss = new Vector();
		            	  ss.add(gru);
		            	  gr.put(agent, ss);
		              }		            
		          } 
		          
		  //leiame asendajad 
		          String cmd1 = " select (select emplid from TraAgentsTable  x where x.dataareaid = 'bb' and x.agentsid = a.agent) as agent , " +
				" (select emplid from TraAgentsTable  x where x.dataareaid = 'bb' and x.agentsid = a.agentsubstitute) as asendaja " +
				" from  TraAgentSubstituteTable a where a.dataareaid = 'bb' and getdate() between a.startdate and a.enddate ";
		 		      if (isDebug()) System.out.println(cmd0);
		 		     
		 		      TreeMap repl = new TreeMap();
		 		      rs = stmt.executeQuery(cmd1);
		 		          while(rs.next()){
		 		            String agent = rs.getString("agent").trim();
		 		            String gru = rs.getString("asendaja").trim();
		 		          
		 		            Vector ss = (Vector)repl.get(agent);		            
		 		              if(ss != null){
		 		                  ss.add(gru);
		 		                 repl.put(agent, ss);
		 		              }else{
		 		            	  ss = new Vector();
		 		            	  ss.add(gru);
		 		            	 repl.put(agent, ss);
		 		              }
		 		            
		 		          } 
		          
	  
    String cmd = "select emp.emplid as kood ,del_name  + ' ' + agentsid as nimi from  TraAgentsTable tra inner join empltable emp " + 
         " on emp.dataareaid= tra.dataareaid and ltrim(rtrim(tra.emplid))= ltrim(rtrim(emp.emplid)) where  " +
         " emp.dataareaid='bb' and  tra.dataareaid ='bb'";
    if (isDebug()) System.out.println(cmd);
   
    
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("nimi").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          // sätime gruppid kylge 
        if(gr.get(agent.getID())!= null){
        	Vector vv = (Vector)gr.get(agent.getID());
        	for (int i = 0; i<vv.size();i++){
        		agent.addProdGroupList((String)vv.elementAt(i));
        	}
        }
          // sätime asendajad kylge 
          if(repl.get(agent.getID()) != null){
        		Vector vv = (Vector)repl.get(agent.getID());
            	for (int i = 0; i<vv.size();i++){
            		agent.addDubbler((String)vv.elementAt(i));
            	} 
          }
          agents.put(name, agent);
          agents1.put(agent.getID(), agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
 
    
    Agent aa = new Agent ("HOUSE");
    aa.setName("HOUSE");
    agents.put("HOUSE", aa);
    
    Agent aa1 = new Agent ("HOUSE1");
    aa1.setName("HOUSE1");
    agents.put("HOUSE1", aa1);
    
    Agent aa2 = new Agent ("HOUSE2");
    aa2.setName("HOUSE2");
    agents.put("HOUSE2", aa2);
    
    Agent aa3 = new Agent ("HOUSE3");
    aa3.setName("HOUSE3");
    agents.put("HOUSE3", aa3);
    
    
    // System.out.println("Agendid " + agents.toString());
    
    return agents;
  }
  
  private TreeMap fixAsendused = null;
  
  private void makePictureCat(){
	     
      	String  cmd2 = " SELECT  inv.itemid as toode FROM inventtable inv WHERE  inv.dataareaid='bb'   and bbinventsuni9 ='NNY'" ;
      	
      	
      	String picts = parent.getProps().getProperty("pict.dir");
    	    String pictspda = parent.getProps().getProperty("pictpda.dir");
    	 

      	try {
      		Connection con = this.getNewConnection();
      		Statement stmt = con.createStatement();
      	    ResultSet	      rs = stmt.executeQuery(cmd2);

   
           StringBuffer buf = new StringBuffer();
           StringBuffer buf0 = new StringBuffer();
           int a = 0;
           int b = 0;
           int c = 0;
           System.out.println("Piltide leidmine ");
      	    while (rs.next()) {
      	    	a++;
      	    	c++;
      	    	if(c == 100){
      	    		  c =0;
      	    		  System.out.println("Piltide leidmine "  + a);	
      	    	}
      	     
                    String toode = rs.getString("toode").trim();               
                    File f = new File(picts +"B"+ toode+".jpg");
                    String len = "0";
                    if(f.exists()){
                      len =	f.length() + "";
                    }
                    if(len.equals("0")){
                    	//System.out.println(toode+".jpg;"  + len);
                    	buf0.append(toode+".jpg\n");
                    }else{
                    	buf.append(toode+".jpg;"  + len + "\n");
              	    	b++;
                    }
                    }
      	    rs.close();
      	    stmt.close();
      	    con.close();
      	
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
//           	 Lõppes kustutamine 
           	 
             System.out.println("Piltide kopeerimine ");	
             //Failide kopeerimine  
               String[] gg = new File(picts).list();
            	  //System.out.println("gg " + gg[0] + " " + gg.length);
               
                for(int i = 0;gg.length>i;i++){
                	
                	File in = new File ( picts + gg[i]); 
                	if(in.isFile()){
                	
                	File inpda = new File ( pictspda + gg[i].substring(1)); 
               
                	
                	
                      Converter.shrinkAndCopyFile(in,inpda);
                      if(gg[i].substring(1).indexOf("038659771") != -1){
                  		System.out.println("Olemas fail " + inpda.exists() + in.exists());
                  	}
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
    private String getLong(String value){
		    Calendar cal = Calendar.getInstance();
		    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(value.substring(0, 2)));
		    cal.set(Calendar.MONTH, Integer.parseInt(value.substring(3, 5))-1);
		    cal.set(Calendar.YEAR, Integer.parseInt(value.substring(6, 10)));
		    return (cal.getTime().getTime()) + "";
  }
  
  
  public TreeMap getAgents1() {
		return agents1;
	}

private TreeMap agents1 = new TreeMap();


private TreeMap makeCustomerDeliveryFile(){
	TreeMap res = new TreeMap();
	try{

      
String sql3 = " select   D.ID, C.ACCOUNTNUM as klient ,  C.NAME,  D.DELIVERY_DAYS as paev " +
 " from [bb-ax01].[BO_TABLES].[epasaule].[bo_clients_deliveries] D " +
 " inner join [bb-ax01].[Axdb2009_bb].[dbo].[CustTable] C on " +
 "  D.ID COLLATE DATABASE_DEFAULT = C.TRAADDRESSID and C.DATAAREAID = 'bb'";

Connection con = this.getNewConnection();
	Statement stmt = con.createStatement();

ResultSet rs3 = stmt.executeQuery(sql3);
while(rs3.next()){
String klient = rs3.getString("klient").trim();
String days = rs3.getString("paev").replace(";", " ").trim();

StringTokenizer st = new StringTokenizer(days,",");
if(st.hasMoreTokens()){
	if(res.get(klient) == null){
		res.put(klient+"xxx", getLong(st.nextToken().trim()));
	}else{
		String olemas = (String)res.get(klient);
		olemas = olemas + "," +  getLong(st.nextToken().trim());
		res.put(klient, olemas);
	}
}


}
rs3.close();
stmt.close();
con.close();
 
  }catch (Exception e){
	  e.printStackTrace();
    //getParent().showError(e);
  }
  return res;
}

private HashSet specList(){
	HashSet res = new HashSet();
	try{

String sql3 = "select ITM.ItemID as kood from InventTable ITM inner join " +
 " Axdb2009_bb..PriceDiscTable as PDT with (nolock) on PDT.DataAreaID in ('BB') and PDT.ItemRelation = ITM.ItemID " +
 " where   PDT.Module = 1 and PDT.AccountCode = 1 and PDT.ItemCode = 0 and PDT.FromDate <= GetDate() + 1 and " +
 " (PDT.ToDate = '19000101' or PDT.ToDate >= GetDate()) and " +
 "    PDT.ACCOUNTRELATION in ('ATF', 'ATN') ";


Connection con = this.getNewConnection();
Statement stmt = con.createStatement();

ResultSet rs3 = stmt.executeQuery(sql3);
while(rs3.next()){
res.add(rs3.getString("kood").trim());
}
rs3.close();
stmt.close();
con.close();

  }catch (Exception e){
	  e.printStackTrace();
    
  }
  

  return res;
}

private TreeMap groupsAG = null;


  public TreeMap getCustomers(Agent agent){
System.out.println("Alustan kliendid ");
  this.groupsAG = new TreeMap();
//Teeme kysimuste failid valmis  p02831

 

  TreeMap deldays =  makeCustomerDeliveryFile();
  

//klientide osa algab 

  String sqlall = "select CTR.ACCOUNTNUM AS maksja,CTR.ORDERACCOUNT AS klient, CTR.TransDate AS kuupaev, CTR.Invoice AS number,   Sum(CTR.AmountMst - CTR.SettleAmountMst)  AS tasumata, CTR.DueDate AS maksepaev, datediff(day,CTR.DUEDATE,getdate()) AS yle " +
  " from CustTrans as CTR with (nolock) left join CustInvoiceJour as CIJ with (nolock) on CIJ.DataAreaID = CTR.DataAreaID and" +
  " CIJ.InvoiceAccount = CTR.AccountNum and CIJ.LedgerVoucher = CTR.Voucher and CIJ.InvoiceDate = CTR.TransDate " +
  " inner join CustTable as CTB with (nolock)     on CTB.DataAreaID = CTR.DataAreaID" +
  " and CTB.AccountNum = IsNull(CIJ.OrderAccount, CTR.AccountNum) " +
  " where  CTB.DataAreaID = 'BB' and CTB.Name not like '%sl[e]gts%' and CTB.TraAddressID <> '' and CTR.AmountMst > 0 and CTR.DueDate < GetDate() + 5 " + 
  " group by  CTR.ACCOUNTNUM,  CTR.ORDERACCOUNT, CTR.TransDate,  CTR.Invoice,  CTR.DueDate, CTR.POSTINGPROFILE " +
  " having Sum(CTR.AmountMst - CTR.SettleAmountMst) > 0 and CTR.POSTINGPROFILE = 'STD'";


  
String volg = "select CTR.ACCOUNTNUM AS maksja,   Sum(CTR.AmountMst - CTR.SettleAmountMst) AS tasumata " +
" from CustTrans as CTR with (nolock)  left join CustInvoiceJour as CIJ with (nolock) on   CIJ.DataAreaID = CTR.DataAreaID and " +
"  CIJ.InvoiceAccount = CTR.AccountNum and CIJ.LedgerVoucher = CTR.Voucher and CIJ.InvoiceDate = CTR.TransDate  " +
" inner join CustTable as CTB with (nolock) on    CTB.DataAreaID = CTR.DataAreaID and " +
 "           CTB.AccountNum = IsNull(CIJ.OrderAccount, CTR.AccountNum)  " +
" where CTB.DataAreaID = 'BB' and CTB.Name not like '%sl[e]gts%' and CTB.TraAddressID <> '' and CTR.AmountMst > 0 and  CTR.DueDate < GetDate() " + 
" group by   CTR.ACCOUNTNUM,    CTR.POSTINGPROFILE " +
" having Sum(CTR.AmountMst - CTR.SettleAmountMst) > 0 and CTR.POSTINGPROFILE = 'STD'" ;
  
  


 String cmd = " SELECT (cus.deliveryfilter + cus.name + case when cus.bblagentcomment != '' then ' // ' + cus.bblagentcomment else '' end)  as name ,traaddressid as traid,'' as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
             " (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate() and co.dataareaid = 'bb'  " +
              "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " + 
 	         " cus.invoiceaccount end )) AS debt, cus.linedisc ,  " +
             "     cus.address, '' AS visit ,  numofdays AS term, blocked AS block ,  " +
			 " (select (blocked) from custtable cc where  cc.dataareaid='bb' and cc.accountnum=cus.invoiceaccount) " + 
			 "   as payerblock ,phone + ' ' + telefax as info , pricegroup as grupp , " +
			" (select(creditmax) from custtable cc where  cc.dataareaid='bb' and cc.accountnum=cus.invoiceaccount) as credit " +
			" , bbcustgroupid as grupp3 , PAYMMODE as ette FROM custtable cus left outer JOIN paymterm  " +
			 " pay on cus.paymtermid=pay.paymtermid and pay.dataareaid = cus.dataareaid and  pay.dataareaid='bb' " +
			"	 WHERE   cus.dataareaid='bb'  and traaddressid != '' and cus.traaddressid!= 'PA0000' " +
			"    order by cus.name " ;
 
 String ccc = "select invoiceaccount from custtable where dataareaid='bb' and invoiceaccount != ''  group by invoiceaccount";

 String cont = "select custaccount as klient,name  + (case when phone != '' then ' Tel. '+ phone else '' end )  " +
" + (case when cellularphone != '' then ' Mob. '+ cellularphone else '' end ) " +
"+ (case when telefax != '' then ' Fax. '+ telefax else '' end ) " +
"+ (case when email != '' then '|  '+ email else '' end )" +
"as asi1  from contactperson  where dataareaid='bb' and name != ''";
 
 String qqq = "select tra.emplid as agent , custaccount as klient , day , periodtype ,'(' +tra.emplid +')' + agentsid + ' ' +del_name as nam ,Tsmanager as tyyp from TraAgentCustTable cus inner join TraAgentsTable tra on cus.dataareaid= tra.dataareaid " +  
				" and cus.agent = tra.agentsid INNER JOIN EMPLTABLE e ON e.dataareaid = tra.dataareaid  and cus.dataareaid = tra.dataareaid and e.emplid=tra.emplid " +
				" where cus.dataareaid = 'bb' and tra.dataareaid = 'bb' and e.dataareaid = 'bb'";
 
      
           String ddd = "select (accountnum) as klient,max(assortgroupid) as asi1, min(assortgroupid) as asi2 from assortgroups_cust where   dataareaid = 'bb' " +
                     " group by accountnum having  count (assortgroupid ) >0 ";


  if (isDebug()) System.out.println(cmd);
  
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap custAgents = new TreeMap();
      fixAsendused = new TreeMap();
    TreeMap tempFix = new TreeMap();
    Vector payers =  new Vector();
    try {
   	
        String sql2 = "select clid AS kood ,sentdate as kuup, agentnam AS agent , mestext " +
	  		"AS mess from mj_crm where sentdate > getdate()-60 order by clid,sentdate";
	 Connection con2 = this.getNewConnection2();
          Statement stt = con2.createStatement();
	  ResultSet rr = stt.executeQuery(sql2);
	  StringBuffer sb = new StringBuffer ();
	  
	  while (rr.next()){
	  	sb.append(rr.getString("kood").trim() + ";" + rr.getString("kuup").trim() + " " + rr.getString("agent").trim()
	  			+ "|" + rr.getString("mess").trim() + "\n");
	  }
	  rr.close();
	  stt.close();
	  con2.close();
	  
	  if(sb.length()==0){
		  sb.append("kood;meaasge|message\n");
	  }
	      File f = new File( "customercrm.txt");
	      if (f.exists())f.delete();
	        try {
	  		PrintWriter os = new PrintWriter(new FileWriter(
	  				"customercrm.txt", true));
	  		os.write(sb.toString());
	  		os.close();
	  	  }catch (IOException ex) {
	  		ex.printStackTrace();
	  	  }
    	
    	TreeMap debtover = new TreeMap();
    	
	  	Connection congd = this.getNewConnection();
    	Statement stmgd = congd.createStatement();
    	
        ResultSet rs1gd = stmgd.executeQuery(volg);
        while(rs1gd.next()){
        	String kood = rs1gd.getString("maksja").trim();
        	double yle = rs1gd.getDouble("tasumata");
        	debtover.put(kood, yle+"");
        }
        System.out.println("Debtover" +debtover.size() );
       rs1gd.close();
       stmgd.close();
       congd.close();
    	
    	
    	
    	Connection cong = this.getNewConnection();
    	Statement stmg = cong.createStatement();
    	
        ResultSet rs1g = stmg.executeQuery(ddd);
        while(rs1g.next()){
        	String kood = rs1g.getString("klient").trim();
        	String sort1 = rs1g.getString("asi1").trim();
        	String sort2 = rs1g.getString("asi2").trim();
        	
        	if(!sort1.equals(sort2)){
        		tempFix.put(kood, sort1+"|"+sort2);  
        	}else{
        		tempFix.put(kood, sort1);	
        	}
        	
        	       
        }
        System.out.println("Suurused" +tempFix.size() );
       rs1g.close();
       stmg.close();
       cong.close();
    	
    	
    	Connection conh = this.getNewConnection();
    	Statement stmh = conh.createStatement();
    	TreeMap contacts = new TreeMap();
        ResultSet rs1h = stmh.executeQuery(cont);
        while(rs1h.next()){
        	String id = rs1h.getString("klient").trim();
        	String contact = rs1h.getString("asi1").trim();
        	
        	
        	if(contacts.get(id) != null){
        		contacts.put(id, contacts.get(id)+"|"+contact);  
        	}else{
        		contacts.put(id, contact);  	
        	}
        	
        	       
        }
        System.out.println("Suurused" +tempFix.size() );
       rs1h.close();
       stmh.close();
       conh.close();
    	
  
       
    	Connection conr = this.getNewConnection();
    	Statement stmr = conr.createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
       conr.close();
    	
       Connection cone = this.getNewConnection();
       Vector kliendid = new Vector();
   	Statement stme = cone.createStatement();
   	HashMap custagVisit = new HashMap();
   	HashMap custagName = new HashMap();
    HashMap tara = new HashMap();
   	HashMap agendiTyyp = new HashMap();
   	HashMap custVisit = new HashMap();
   	
   	String fgy ="Select accountnum as klient ,ta.itemid as kood , it.itemname as nimi , ta.qty as kogus "+
  " from BBTaraBalance ta inner join inventtable it  on it.itemid = ta.itemid  "+
  " and  ta.dataareaid = it.dataareaid and ta.itemid not in ('165000001') and ta.qty <> 0 where ta.dataareaid ='bb' ";
   	
    ResultSet rsex = stme.executeQuery(fgy);
    while(rsex.next()){
    	
   	    String klient = rsex.getString("klient").trim();
   	 String taraid = rsex.getString("kood").trim();
   	    String taraname = rsex.getString("nimi"); 
   	    int kogus = rsex.getInt("kogus");
   	 String tt= "";
   	    if(tara.get(klient)!= null){
   	    	 tt = (String)tara.get(klient);
   	    	tt = tt + "|[" + kogus + "] " + taraname;
   	    }else{
   	    	tt =  "Uz esoðo adresi|[" + kogus+ "] " + taraname;
   	    }
   	   
   	    tara.put(klient, tt);
    }
      rsex.close();
    
    fgy =" Select cu.invoiceaccount as klient ,ta.itemid as kood , it.itemname as nimi , sum(ta.qty) as kogus " +
 " from BBTaraBalance ta inner join inventtable it  on it.itemid = ta.itemid  "+
  "  and  ta.dataareaid = it.dataareaid  "+
	" inner join  custtable cu on  cu.accountnum=ta.accountnum   and  ta.dataareaid = cu.dataareaid  " +
  " where ta.dataareaid ='bb' and ta.itemid not in ('165000001') and ta.qty <> 0 and invoiceaccount!='' group by cu.invoiceaccount,ta.itemid,it.itemname";   	
     rsex = stme.executeQuery(fgy);
    while(rsex.next()){
    	
   	    String klient = rsex.getString("klient").trim();
   	 String taraid = rsex.getString("kood").trim();
   	    String taraname = rsex.getString("nimi"); 
   	    int kogus = rsex.getInt("kogus");
   	 String tt= "";
   	if(tara.get(klient)!= null){
	    	 tt = (String)tara.get(klient);
	    	tt = tt + "|[" + kogus + "] " + taraname;
	    }else{
	    	tt =  "Uz maksâtâju |[" + kogus+ "] " + taraname;
	    }
   	   
   	    tara.put(klient, tt);
    }
	   
   	
   	
       ResultSet rse = stme.executeQuery(qqq);
       while(rse.next()){
   	   
    		String age = rse.getString("agent").trim();
       	    String klient = rse.getString("klient").trim();
       	    String day = rse.getString("day");
       	    String period = rse.getString("periodtype");
       	    String name = rse.getString("nam");
       	    String tyyp = rse.getString("tyyp");
      
       	    if(tyyp != null && tyyp.trim().equals("1")){
       	 	 agendiTyyp.put(age, tyyp);
       	    }
       	    String old = (String)custagVisit.get(age + klient);
       	 
       	   String visit = this.getMJDay(day, period , old );
       	   custagVisit.put(age + klient, visit);
           custagName.put(age + klient, name);
           custVisit.put(klient, visit);
       	 kliendid.add(klient);
       	 
       	 
       if(custAgents.get(klient) != null){
    	   Vector vv = (Vector)custAgents.get(klient) ;
    	   vv.add(age);
    	   custAgents.put(klient, vv);
    	  
       }else{
    	   Vector vv = new Vector() ;
    	   vv.add(age);
    	   custAgents.put(klient, vv);
       }
    	
       }
      rse.close();
      stme.close();
      cone.close();
       
      System.out.println("agent klient tehtud ");
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").trim();
      
        String traid = rs.getString("traid").trim();
        String panenim = name + traid;
        String alias = rs.getString("namealias");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	//if(!name.trim().startsWith(alias.trim()))
        	//  name = " " + alias  ;
        }
       
        
        
        String accountnum = rs.getString("accountnum").trim();
        if(kliendid.contains(accountnum)){
        
        Customer cust = new Customer(accountnum);

     
        
    
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          //cust.setDebt(Double.parseDouble(debtStr));
        }
        
        
        if(tara.get(cust.getID()) != null){
        	cust.addToCustDataVec(cust.getID()+ ";tara;data;" + "Taras Bilance:|" + tara.get(cust.getID()));
        }
        if(!cust.getID().equals(cust.getPayer()) && tara.get(cust.getPayer()) != null){
        	cust.addToCustDataVec(cust.getPayer()+ ";tara;data;" + "Taras Bilance:|" + tara.get(cust.getPayer()));
        }
     	
        
        if(debtover.get(cust.getPayer()) != null){
        	  cust.setDebt(Double.parseDouble((String) debtover.get(cust.getPayer())));
  	
        }
        if(debtover.get(cust.getID()) != null){
      	  cust.setDebt(Double.parseDouble((String) debtover.get(cust.getID())));
	
      }
        
      
        if(tempFix.get(cust.getID()) != null){
        	cust.addFixSortCode((String) tempFix.get(cust.getID()));
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replaceAll("\n"," ").replaceAll("\r", " ")),"Latvia"));
          name = name + ": " + Converter.enCodeEst(aadr.trim().replaceAll("\n"," ").replaceAll("\r", " "));
        }
      
        cust.setName(traid + " " + Converter.enCodeEst(name));
        
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
   
        if(deldays.get(cust.getID())  != null){
        	cust.setDelDays((String)deldays.get(cust.getID()));
        }
       
        
        if(custAgents.get(cust.getID()) != null){
        	Vector vv = (Vector) custAgents.get(cust.getID());
        	for(int i = 0; i < vv.size();i++){
        		
        		Agent aa = new Agent((String) vv.elementAt(i));
        		String visi = (String)custagVisit.get(aa.getID() + cust.getID());
        		if(visi != null && visi.trim().length() > 0){
        			aa.setVisit(visi);	
        		}
        		
        		String nam = (String)custagName.get(aa.getID() + cust.getID());
        		if(nam != null && nam.trim().length() > 0 && agendiTyyp.get(cust.getID())!= null ){
        			cust.setCity("TELE: " + Converter.enCodeEst(nam) + "|");
        		}
        		 cust.addAgent(aa);	
        		 
        		  if (aa != null ){
      				Agent agg = ((Agent)this.getAgents1().get(aa.getID()));
      				
      				if(agg != null && agg.getDubbler().size()>0){
      					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
      					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
      						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
      						if(custVisit.get(cust.getID()) != null){
      							dub.setVisit(custVisit.get(cust.getID()) + "");
      						}
      						cust.addAgent(dub);
      					//	System.out.println(cust.getName() + cust.getAgents());
      					}
      				}
      			}
        		
        	}
        	
        }

        cust.addAgent(new Agent("HOUSE1"));
        
        int block = rs.getInt("block");
        if (block == 1 ) {
          cust.setSalesBlock(true);
        }
        
        if (block == 2 ) {
            cust.setSalesBlockT(true);
          }
        
        if (block == 3 ) {
            cust.setInfo("Cash Only");
          }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }
        
        if (payerblock == 2) {
            cust.setSalesBlockT(true);
          }
        if (payerblock == 3) {
        	  cust.setInfo("Cash Only");
          }
   

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData( Converter.enCodeEst(info.trim())); 
        }
        
        if(contacts.get(cust.getID()) != null){       	
        	 cust.setPersonalData( Converter.enCodeEst(cust.getPersonalData() + "|" + contacts.get(cust.getID())) );
        }
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
          String gr = Converter.cleanLitLatEst(grupp.trim());
        	cust.setGroup(gr);
        	if(custAgents.get(cust.getID()) != null){
        		
        		Vector vv = (Vector) custAgents.get(cust.getID());
            	for(int i = 0; i < vv.size();i++){
            		
            		String aa = vv.elementAt(i)+ ",";
        		
              if(groupsAG.get(gr) == null){
            	  
              	  groupsAG.put(gr, "," +aa);
                }else{
                 String uu = groupsAG.get(gr)+"";
                 if(!uu.contains(aa)){
                	 groupsAG.put(gr, uu + aa);
                  }
                }	
        	}
        }}
        
        
        double credit = rs.getDouble("credit");
        if(credit > 1){
        	cust.setCredit(credit + "");
        }
        String grupp3 = rs.getString("grupp3");
        if (grupp3 != null && grupp3.trim().length()>0) {
        
          cust.setCity(cust.getCity() + "Klienta grupa: " + Converter.enCodeEst(grupp3.trim()));
        
          }
   
        String ette = rs.getString("ette");
        if (ette != null && ette.trim().equals("PrePayment")) {
        	//cust.setSalesBlockPrepay(true);
        if(cust.getInfo() == null || cust.getInfo().trim().length() == 0){
      	  cust.setInfo("Apmaksas veids: prieksapmaksa");
        }else{
        	 cust.setInfo(cust.getInfo() + "\nApmaksas veids: prieksapmaksa");
        }
        }
        
        String cmd2 = "SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
               " disc.dataareaid='bb' AND len(disc.itemrelation)=0  AND fromdate<=getdate()+1 and todate >= getdate() + 1";
             rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    if(laks){
                        customers.put(panenim, cust);
                        custMap.put(cust.getID(),cust);	
                    }
        }
              
      }
System.out.println("Kliendi tehtud alustan võla listiga ");

String sql = "select CTR.ACCOUNTNUM AS maksja,CTR.ORDERACCOUNT AS klient, CTR.TransDate AS kuupaev, CTR.Invoice AS number,   Sum(CTR.AmountMst - CTR.SettleAmountMst)  AS tasumata, CTR.DueDate AS maksepaev, datediff(day,CTR.DUEDATE,getdate()) AS yle " +
" from CustTrans as CTR with (nolock) left join CustInvoiceJour as CIJ with (nolock) on CIJ.DataAreaID = CTR.DataAreaID and CIJ.InvoiceAccount = CTR.AccountNum and CIJ.LedgerVoucher = CTR.Voucher and CIJ.InvoiceDate = CTR.TransDate " +
" inner join CustTable as CTB with (nolock)     on CTB.DataAreaID = CTR.DataAreaID and CTB.AccountNum = IsNull(CIJ.OrderAccount, CTR.AccountNum) " +
" where  CTB.DataAreaID = 'BB' and CTB.Name not like '%sl[e]gts%' and CTB.TraAddressID <> '' and   CTR.AmountMst > 0 and CTR.DueDate < GetDate() + 5 " + 
" group by  CTR.ACCOUNTNUM,  CTR.ORDERACCOUNT, CTR.TransDate,  CTR.Invoice,  CTR.DueDate, CTR.POSTINGPROFILE " +
" having Sum(CTR.AmountMst - CTR.SettleAmountMst) > 0 and CTR.POSTINGPROFILE = 'STD'";



      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        debt.setDate(rs.getDate("kuupaev"));
                        String number = rs.getString("number").trim();
                        if(number.length() != 0){
                        	  debt.setDoknr(number);
                              debt.setSum(rs.getDouble("tasumata"));
                              debt.setDuedate(rs.getDate("maksepaev"));
                              debt.setDue(rs.getString("yle"));
                              this.debt.addElement(debt);                             	
                          }
                      }

        

    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    System.out.println("Groups AG  " + groupsAG.toString());  
    return customers;
  }

  
  
  private void sendExceptionEmail(String buf){
	  String address = parent.getProps().getProperty("exceptionemail");
	  if(address != null ){
		  Email email = new Email(parent.getString("importerror"));
         
          email.setFrom(parent.getProps().getProperty("from"));
          email.setSMTP(parent.getProps().getProperty("smtphost"));
          email.setUser(parent.getProps().getProperty("smtpuser"));
          email.setPass(parent.getProps().getProperty("smtppass"));
          email.setSubjectFilesTo(parent.getString("importerror")  );
          if(parent.getProps().getProperty("language").equals("Russian")){
            email.setEncoding("KOI8-R");
          }
          
          email.setEncoding("iso-8859-1");
          try {
        	  
        	email.setBody(buf.toString());

            StringTokenizer st =  new StringTokenizer(address,";");
            while(st.hasMoreTokens()){
            	String aadr = new InternetAddress(st.nextToken()).toString();
              email.setFilesTo(aadr);
              email.send();
              }
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  		  
	  }
  }


  private Connection getNewConnection() throws SQLException{
	  Connection con = null;
	  try{
		  con = DriverManager.getConnection(url 
			        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	    } catch (Exception e){
	    	 try{
	    	  con = DriverManager.getConnection(url 
				        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	    	 } catch (Exception u){
	    		 try{
	   	    	  con = DriverManager.getConnection(url 
	   				        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	   	    	 } catch (Exception x){
	   	    		 this.sendExceptionEmail(x.toString());
	   	    	 } 
	    	 }
	       parent.showError(e);
	    }
	
	return con;
  }
  
  private Connection getNewConnection2() throws SQLException{
	  Connection con = null;
	  try{
		  con = DriverManager.getConnection(url2 
			        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	    } catch (Exception e){
	    	 try{
	    	  con = DriverManager.getConnection(url2 
				        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	    	 } catch (Exception u){
	    		 try{
	   	    	  con = DriverManager.getConnection(url2
	   				        , parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	   	    	 } catch (Exception x){
	   	    		 this.sendExceptionEmail(x.toString()); 
	   	    	 } 
	    	 }
	       parent.showError(e);
	    }
	
	return con;  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "   select groupid as liik , name as nimi from BBLItemSubGroupTable where dataareaid = 'bb'  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      String nimi = Converter.enCodeEst(rs.getString("nimi").trim());
      
      int aa = nimi.indexOf("11122xx");
      if(aa != -1){
    	 	nimi = nimi.substring(0,aa) + "Z" + nimi.substring(aa+7,nimi.length()); 
      }
      
      aa = nimi.indexOf("11127xx");
      if(aa != -1){
    	 	nimi = nimi.substring(0,aa) + "C" + nimi.substring(aa+7,nimi.length()); 
      }
      
	    tm.put(liik, nimi);
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "  select trabrandid as liik , description as nimetus from TraBrandtable where dataareaid = 'bb' ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    if(groups.size() > 0){
    	Iterator itera = groups.keySet().iterator();
    	while (itera.hasNext()){
    		String key = (String) itera.next();
    		String  val = (String)groups.get(key);
    		tm.put(val + key, tm.get(val)+ " " + tm.get(key));
    		
    	}
    	
    }
  //  System.out.println(tm.toString());
    return tm;
  }

  private String getMJDay(String day, String period, String old){
	  String res = "";
	  
	  if(day.equals("0")){
		  res = " ";
	  }

	  
	  if(day.equals("1")){
		  res = "E";
	  }
	  if(day.equals("2")){
		  res = "T";
	  }

	  if(day.equals("3")){
		  res = "K";
	  }

	  if(day.equals("4")){
		  res = "N";
	  }

	  if(day.equals("5")){
		  res = "R";
	  }

	  if(day.equals("6")){
		  res = "L";
	  }

	  if(day.equals("7")){
		  res = "P";
	  }

	  if(day.equals("8")){
		  res = "ETKNR";
	  }
	  
	  if(period.equals("2")){
		  res = res + "2";
	  }

	  if(period.equals("3")){
		  res = res + "1";
	  }
	  if(old != null){
		  res = old + res;
	  }
	  return res;
	  
  }
private HashMap groups = new HashMap();
  
  public TreeMap getProducts(Agent agent){
	  
	  HashSet specList = specList();
  	 
	  String  cmd2 =  "SELECT  IST.ITEMID as toode, SUM(IST.AVAILPHYSICAL) as stock FROM INVENTSUM IST with (nolock) " +
	            "  INNER JOIN INVENTDIM ID with (nolock) ON IST.INVENTDIMID=ID.INVENTDIMID and ID.DATAAREAID=IST.DATAAREAID " +
	            "  INNER JOIN INVENTLOCATION ILT with (nolock) ON  ID.INVENTLOCATIONID = ILT.INVENTLOCATIONID and ID.DATAAREAID = ILT.DATAAREAID" +
	            "  INNER JOIN INVENTTABLE IT with (nolock) on  IT.ITEMID=IST.ITEMID and   IT.DATAAREAID=IST.DATAAREAID WHERE IST.DATAAREAID='bb'and IST.CLOSED = 0 AND " +
	      " ID.INVENTLOCATIONID in (select INVENTLOCATIONID from INVENTLOCATION where DATAAREAID = 'bb' and STOCKEXPORT = 1) and IT.BBINVENTSUNI9 ='NNY' GROUP BY  IST.itemid" +
	" HAVING SUM(IST.AVAILPHYSICAL) > 0 union all select itemid as toode, sum(qty) as stock from [Axdb_dt].dbo.[DTInventSum]" +
	" where TplPartnerId in (select DTPartner from [Axdb_dt].dbo.[DTPartnerTable] where StockExportDT = 1) and   qty != 0 GROUP BY itemid order by  toode ";

		  
	  

	  
	  String  cmduus =  " SELECT TRJL.ItemId as toode, sum(TRJL.QtyInitial - TRJL.QtyUsed) as stock FROM TRARESGROUPTABLE TRGT " +
          "  inner join TraResJournalTable TRJT on  TRGT.GroupCode = TRJT.ResGroupCode and " +
           "       TRGT.DataAreaId = TRJT.DataAreaId left join TraResJournalLine TRJL on " +
            "      TRJT.ResGroupCode = TRJL.ResGroupCode and TRJT.JournalId = TRJL.JournalId and TRJT.DataAreaId = TRJL.DataAreaId " +
   " inner join InventTable ITB on TRJL.ItemId = ITB.ItemId and  TRJL.DataAreaId = ITB.DataAreaId " +
     "       inner join InventItemBarcode BAR on  ITB.ItemId = BAR.ItemId and  ITB.DataAreaId = BAR.DataAreaId and  BAR.USEFORPRINTING = 1 " +
    " WHERE  TRGT.DataAreaId = 'bb' GROUP BY TRJL.ItemId,  BAR.ItemBarcode having   sum(TRJL.QtyInitial - TRJL.QtyUsed) > 0";

	  

    String cmd = "select inv.itemid, inv.itemgroupid as tyyp, " +
     "  isnull(car.alias,'') + ' ' + case when len(inv.namealias) > 50 then left(inv.namealias,50) + '...' else inv.namealias end as nimetus, " +
     "  inv.tracrateqty as kastis, inv.standardpalletquantity as alusel, " +
     "  mod.price as price2, isnull(bar.itembarcode,'') as ean, " +
      " inv.bblitemsubgroupid as grupp, inv.trabrandid as grupp2, inv.trabrandhn as  grupp3," +
     "  mod.unitid as unit, inv.itemgroupid as vat,  iss.multipleqty as optimum,  " +
       " iss.lowestqty as minimum,  inv.abctype as abc from  inventtable inv with (nolock)" +
      "        left join inventitembarcode bar with (nolock) on bar.itemid = inv.itemid and  bar.dataareaid = inv.dataareaid and  useforprinting = 1" +
     "         inner join inventtablemodule mod with (nolock) on  mod.itemid = inv.itemid and inv.dataareaid = mod.dataareaid and  mod.moduletype = 2 " +
    "          inner join InventItemSalesSetup iss with (nolock) on  iss.itemid = inv.itemid and iss.dataareaid = inv.dataareaid" +
   "           left join INVENTDIM idim with (nolock) on  iss.INVENTDIMID = idim.INVENTDIMID and  iss.DATAAREAID = idim.DATAAREAID" +
  "            left join INVENTLOCATION iloc with (nolock) on idim.INVENTLOCATIONID = iloc.INVENTLOCATIONID and idim.DATAAREAID = iloc.DATAAREAID" +
 "             left join TRACARRIERTABLE car with (nolock) on iloc.TRACARRIERID = car.CARRIERID and iloc.DATAAREAID = car.DATAAREAID" +
" where inv.dataareaid = 'bb' and  mod.dataareaid = 'bb' and iss.stopped = 0 and inv.bbinventsuni9 ='NNY' ";
    
  

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    TreeMap stocks = new TreeMap();
    TreeMap stocksNimi = new TreeMap();
    try {
        Connection ccc = this.getNewConnection();
    	Statement stmt =  ccc.createStatement();
        ResultSet rs = stmt.executeQuery(cmd2);
       
        while(rs.next()){
        	String toode= rs.getString("toode").trim();
        	double kogus = rs.getDouble("stock");
        	stocks.put(toode, kogus + "");
        }
        rs.close();
        stmt.close();
        ccc.close();
        
      ccc = this.getNewConnection();
    	 stmt =  ccc.createStatement();
        rs = stmt.executeQuery(cmduus);
       
        while(rs.next()){
        	String toode= rs.getString("toode").trim();
        	double kogus = rs.getDouble("stock");
        	stocksNimi.put(toode, ((int)kogus) + "");
        }
        rs.close();
        stmt.close();
        ccc.close();
        
        
        ccc = this.getNewConnection();
        stmt = ccc.createStatement();
        rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
    	  
    	  
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
       String type = rs.getString("tyyp");
        if(type!=null && type.trim().length()>0){
     	    prod.setType2(type.substring(0,1));    	   
        }
        String nimi = rs.getString("nimetus").trim();
        
        String nimetus = nimi + ": " + kood ;
        
        String panenim = nimi + kood;
        if(prod.getType().equals("N")){
        	 nimetus = kood.substring(3) +  " " + nimi + " " + kood.substring(kood.length()-3) ;
        	 panenim = kood ;
        }
        
        if(specList.contains(kood)){
        	prod.setStatusNew("1");
        }
        
        if(stocks.get(kood) != null){
        	prod.setStock(Double.parseDouble((String)stocks.get(kood)));
        }else{
        	prod.setStock(0);
        }
        
        prod.setInCases(rs.getDouble("kastis"));
        if(!prod.getType().equals("N")){
        	// prod.setOnPallet(rs.getDouble("alusel"));	
        }
       
       double  price = rs.getDouble("price2");
       
        prod.setPrice(price);

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
           prod.setEAN(ean.trim());
           if(prod.getType().equals("N")){
        	   if(ean.length() > 4){
        		 if(!ean.trim().substring(ean.length()-4).equals(kood.trim().substring(kood.length()-4))){
        		   nimetus = nimetus + " @" +ean.trim().substring(ean.length()-4);    
        	   }   
        	   }
        	 
           }else{
        	   if(ean.length() > 4){
        		   nimetus = nimetus + " @" +ean.trim().substring(ean.length()-4);   
        	   }
        	}
         }

     String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
     }

      String grupp2 = rs.getString("grupp2");
       if(grupp2!=null && grupp2.trim().length()>0){
        prod.setGroupSecond(grupp2.trim());
     }
       
       String grupp3 = rs.getString("grupp3");
       if(grupp3 != null && grupp3.trim().length()>0){
    	   groups.put(grupp3.trim(), prod.getGroupSecond());
        prod.setGroupSecond(prod.getGroupSecond() + grupp3.trim());
      
     }
       
       if(stocksNimi.get(kood)!= null){
       	nimetus = nimetus + " |s:" + stocksNimi.get(kood) +"|"; 
       }
    
       
      prod.setName(Converter.enCodeEst(nimetus));

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat(vat.trim().substring(1));
    }
      double optim = rs.getDouble("optimum");
       if(optim >0){
         prod.setOptAmountInUnits(optim);
       }
  
       double minamount = rs.getDouble("minimum");
       if(minamount > 0){
         prod.setMinAmountInUnits(minamount);
       }
  
       
       String type2 = rs.getString("abc");
       String nameadd = "";
       if(type2!=null && type2.trim().length()>0){
    	   if(type2.trim().equals("1")){
    		   prod.setType("A");
    		   nameadd = " A";
    	   }
    	   if(type2.trim().equals("2")){
    		   prod.setType("AB");
    		   nameadd = " B";
    	   }
    	   if(type2.trim().equals("3")){
    		   prod.setType("ABC");
    		   nameadd = " C";
    	   }  
    	   if(type2.trim().equals("4")){
    		   prod.setType("ABCD");
    		   nameadd = " D";
    	   }
    	   prod.setName(prod.getName() + nameadd);
     }
       
      
        products.put(panenim, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    //if (true) return custProds ;
 int counter = 1;
 
 String cmd = "select  CIJ.ORDERACCOUNT as customer,  CIT.ITEMID,  sum(CIT.QTY) as qty,  CIJ.INVOICEDATE as lastdate, " +
     " CIJ.INVOICEID as arve,  SL.TRAORIGINALQTY as orig from (select  ITR.DataAreaID, ITR.InventTransID," +
     " ITR.ItemID, ITR.DateStatus,  ITR.InvoiceID, IST.MaxTransDate, CostAmountPosted = Sum(CostAmountPosted),CostAmountAdjustment = Sum(CostAmountAdjustment)" +
" from  InventTrans as ITR with (nolock)  left join ( select DataAreaID, TransRecID, MaxTransDate = Max(TransDate) " +
           " from InventSettlement with (nolock)   where  Cancelled = 0 group by    DataAreaID,  TransRecID ) as IST " +
    "  on ITR.DataAreaID = IST.DataAreaID and ITR.RecID = IST.TransRecID where  ITR.TransType = 0 " +
" group by ITR.DataAreaID,  ITR.InventTransID,  ITR.ItemID, ITR.DateStatus, ITR.InvoiceID," +
    "  IST.MaxTransDate having  Sum(ITR.Qty) != 0) as ITR inner join InventTable as iTable with (nolock) on " +
       "     iTable.DataAreaID = ITR.DataAreaID and   iTable.ItemID = ITR.ItemID left join CustInvoiceTrans as CIT with (nolock) on " +
       "     CIT.DataAreaID = ITR.DataAreaID and    CIT.InventTransID = ITR.InventTransID and   CIT.InvoiceID = ITR.InvoiceID" +
     " left join CustInvoiceJour as CIJ with (nolock) on  CIJ.DataAreaID = CIT.DataAreaID and  CIJ.InvoiceID = CIT.InvoiceID and " +
          "  CIJ.InvoiceDate = CIT.InvoiceDate and   CIJ.SalesID = CIT.SalesID and   CIJ.NumberSequenceGroup = CIT.NumberSequenceGroup " +
     " inner join SalesLine as SL with (nolock) on  SL.DataAreaID = ITR.DataAreaID and  SL.InventTransID = ITR.InventTransID " +
" where  ITR.DataAreaID = 'bb' and CIJ.InvoiceDate >= getdate()-100 " +
" group by CIJ.ORDERACCOUNT,   CIT.ITEMID, CIJ.INVOICEDATE,   CIJ.INVOICEID,   SL.TRAORIGINALQTY";
 
 
   String cmd2 = "SELECT jou.orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,jou.INVOICEID as arve,sal.traoriginalqty as orig" +
" FROM salesline sal with(nolock) left join custinvoicetrans tra with(nolock) on sal.dataareaid=tra.dataareaid   " +
	"   and sal.salesid = tra.salesid  and sal.itemid= tra.itemid " +
    "   INNER JOIN custinvoicejour jou with(nolock) ON  " +
    "   tra.invoiceid=jou.invoiceid and  tra.dataareaid= jou.dataareaid   " +
	"   INNER JOIN inventTABLE cus with(nolock) ON cus.itemid=tra.itemid and  tra.dataareaid = cus.dataareaid  and jou.dataareaid = cus.dataareaid  " +
    "   WHERE  jou.invoicedate > getdate()-100 AND tra.dataareaid = 'bb'  AND jou.dataareaid  = 'bb' AND tra.dataareaid  = 'bb' and cus.dataareaid='bb'  "+   
    "   GROUP BY jou.orderaccount,tra.itemid ,cus.itemname,jou.invoicedate ,sal.traoriginalqty,tra.qty,jou.INVOICEID " +
	"   ORDER BY jou.orderaccount,cus.itemname " ;
   
   if (isDebug()) System.out.println(cmd);

    try {
  Connection con1 = getNewConnection();
    	Statement stmt =  con1.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
    	  
        String kliendikood = rs.getString("customer").trim();
        String tootekood = rs.getString("itemid").trim();
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		      //  String lastdoc = rs.getString("arve").trim();
		      
		        double orig =  0;
		        if(kogus > 0 ){
		        	orig = rs.getDouble("orig")-kogus;
		        }
		        if(orig < 0){
		        	orig = 0;
		        }
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,orig,0," ");
		       //  cp.setAbiKood(lastdoc);
		         custProds.add(cp);
        
      }
      rs.close();
      stmt.close();
      con1.close();      
      
    } catch (Exception e){
      getParent().showError(e);
      if(counter< 4){
      	try{
      	  counter++;
      		Connection con1 = getNewConnection();
      		Statement stmt =  con1.createStatement();
            ResultSet rs = stmt.executeQuery(cmd);
           
             while(rs.next()){
            	    String kliendikood = rs.getString("customer").trim();
                    String tootekood = rs.getString("itemid").trim();
            		        double kogus = rs.getDouble("qty");
            		        String lastdate = rs.getString("lastdate").substring(0,10);
            		       // String lastdoc = rs.getString("arve").trim();
            		        double orig = rs.getDouble("orig")-kogus;
            		     
            		        if(orig < 0){
            		        	orig = 0;
            		        }
            		        lastdate = lastdate.substring(0,10);
            		        custs.put(kliendikood + tootekood,lastdate); 
            		        lastdate = lastdate.substring(5,10); 
            		                 
            		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,orig,0," ");
            		     //    cp.setAbiKood(lastdoc);
            		         custProds.add(cp);
             }
             
             rs.close();
             stmt.close();
             con1.close();      
             
             
      	}catch (Exception ee){
          ee.printStackTrace();
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
    
      
      
      String cmd =" SELECT  cus.emplid as agent,round(SUM(lineamount),0) AS summa  " +
      "   FROM custinvoicetrans sal  INNER JOIN custinvoicejour jou ON " +
      "   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN TraAgentsTable cus ON cus.agentsid = " +
      "jou.traagentsid and jou.dataareaid= cus.dataareaid  WHERE " +
	  "	  sal.dataareaid='bb' AND cus.dataareaid='bb'  AND jou.dataareaid='bb' AND YEAR(sal.invoicedate) = YEAR(getdate()) " +
      "    AND MONTH(sal.invoicedate) = MONTH(getdate())     GROUP BY cus.emplid ";

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
          
    
            
            String cmd2 =" SELECT cus.emplid as agent,jou.TraSalesTypeId as type , round(SUM(lineamount),0) AS summa  FROM custinvoicetrans sal  with (nolock) " +
			" INNER JOIN custinvoicejour jou with (nolock) ON sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  " +
			" INNER JOIN TraAgentsTable cus with (nolock) ON 	cus.agentsid = jou.traagentsid and jou.dataareaid= cus.dataareaid " +  
			" WHERE sal.dataareaid='bb' AND cus.dataareaid='bb' AND jou.dataareaid='bb' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())  " +
			" GROUP BY jou.TraSalesTypeId, cus.emplid ";

               if (isDebug()) System.out.println(cmd2);
                  try {
                      Statement stmt =  this.getNewConnection().createStatement();
                      ResultSet	      rs = stmt.executeQuery(cmd2);
                      while(rs.next()) {
                    	
                        String agentID = rs.getString("agent").trim();
                        Agent agent = (Agent)agents.get(agentID);
                        if (agent != null){
                        	  String type = rs.getString("type").trim();
                        	     int summa  = (int)rs.getDouble("summa");
                        	 
                         agent.setStart(agent.getStart() + getText(type) + " : " + summa + "|"  );
                         // System.out.println(agent.getMonthSales());
                        }
                      }
                      rs.close();
                      con.close();
                  } catch (Exception e) {
                      getParent().showError(e);
                   }
            
    
                   cmd2 =" SELECT cus.EmplId as agent , jou.TraSalesTypeId as type , round(SUM(lineamount),0) AS summa  " +
			 " FROM SalesLine sal  with (nolock) INNER JOIN SalesTable jou with (nolock) ON " +
			   "      	sal.SalesId=jou.SalesId and jou.dataareaid= sal.dataareaid   " +
			" INNER JOIN TraAgentsTable cus with (nolock) ON cus.agentsid = jou.traagentsid and jou.dataareaid= cus.dataareaid  " +
			" WHERE sal.dataareaid='bb' AND cus.dataareaid='bb' AND jou.dataareaid='bb' AND YEAR(jou.CreatedDateTime) = YEAR(getdate()) AND  " +
			"	MONTH(jou.CreatedDateTime) = MONTH(getdate()) and jou.TraSalesTypeId in ( 'STD','A') GROUP BY 	jou.TraSalesTypeId,cus.emplid ORDER BY jou.TraSalesTypeId desc ";

                     if (isDebug()) System.out.println(cmd2);
                        try {
                            Statement stmt =  this.getNewConnection().createStatement();
                            ResultSet	      rs = stmt.executeQuery(cmd2);
                            while(rs.next()) {
                          	
                              String agentID = rs.getString("agent").trim();
                              Agent agent = (Agent)agents.get(agentID);
                              if (agent != null){
                            	  String type = rs.getString("type").trim();
                         	     int summa  = (int)rs.getDouble("summa");
                         	 
                               agent.setStart(agent.getStart() + getText2(type) + " : " + summa + "|"  );
                          // System.out.println(agent.getMonthSales());
                              }
                            }
                            rs.close();
                            con.close();
                        } catch (Exception e) {
                            getParent().showError(e);
                         }
                  
    }
    
    private String getText2(String asi ){
    	if(asi.equals("A")){
    		return Converter.enCodeEst("Atgrieðana (pasûtîjums)");
    	}
    	if(asi.equals("STD")){
    		return Converter.enCodeEst("Pârdoðana (pasûtîjums) kopâ");
    	}
    	return asi;
    }
    
    private String getText(String asi ){
    	if(asi.equals("A")){
    		return Converter.enCodeEst("Atgrieðana");
    	}
    	if(asi.equals("STD")){
    		return Converter.enCodeEst("Pârdoðana");
    	}
    	if(asi.equals("Kplus")){
    		return Converter.enCodeEst("Korekcija(+)");
    	}
    	if(asi.equals("K")){
    		return Converter.enCodeEst("Korekcija(-)");
    	}
    	return asi;
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
    return "AxaBuraLat";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }
private String getStatuses(String code){
	String asi = code;
	if(code.equals("1")){
		return "Pienemts";
	}
  if(code.equals("10")){
		return " Logistika (AKC)";
	}
	 if(code.equals("7")){
			return "WMS";
		}
	 if(code.equals("8")){
			return "Logistika";
		}
	 if(code.equals("9")){
			return "DirectLogistics";
		}
	 if(code.equals("4")){
			return "Prieksapmaksa";
		}
	 if(code.equals("5")){
			return "Kredita kontrole";
		}
	 if(code.equals("2")){
			return "Neaktivs";
		}
	 if(code.equals("3")){
			return "Aktivs";
		}
	 if(code.equals("6")){
			return "Slegts";
		}
	return asi ;
}
  
  public String getPilotDayData(Agent agent){



	       String sql2 =  " SELECT sal.salesid AS arve, sal.deliverydate AS kuupaev,DeliveryDate as tarne, salesname  AS klient, " +
	       		"linecount AS summa ,  TraSalesStatus as status  " +
	   "  FROM salestable sal WHERE sal.dataareaid='bb' AND " +
	"	  (select emplid from TraAgentsTable  x where x.dataareaid = 'bb' and x.agentsid = sal.traagentsid) ='" + agent.getID()+ "' AND  " +
	 "    sal.createddatetime > getdate()-0.8   and trasalestypeid ='STD' ORDER BY sal.salesid";

	    StringBuffer sb = new StringBuffer();
	   
boolean first = true;
int num = 1;
	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                		if(first){

	                            sb.append("Saņemtā nocenošana\n"  );
	                            sb.append("-------------------------------------------\n" );
	                            first = false;
	                        	}
	                  sb.append( num + ". (" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + " -> " + 
	                		    rs2.getString("tarne").substring(0,10) + "] - " + rs2.getString("klient") +
	                		    "\n  Status:" + getStatuses(rs2.getString("status")) + "\n");
	                  sb.append("--------------------\n" );
	                  num++ ;
	                  }

	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }
System.out.println("Sisse "  + sql2 + sb.toString());
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
  private String url2 = "jdbc:odbc:report";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
