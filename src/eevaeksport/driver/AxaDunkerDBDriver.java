package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
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

public class AxaDunkerDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter, FixLoader, PictureMaker {
  public AxaDunkerDBDriver(EksportPanel parent){
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
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;

  

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	  
	     }

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into supervisor values ("
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
	        Connection con = getNewConnection(url2);
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, wr.getInfo().getAgentName());
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
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
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
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
	             ps.setInt(35, 111); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
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
   	return null;
   }

  private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
  
  
  private TreeMap getRimi(){
	  
	    String cmd = "select cvei.ItemId as dunkood, cvei.CustVendRelation as klientgrupp, cvei.ExternalItemId as klientkood " +
       " from CustVendExternalItem cvei where cvei.dataareaid='vir' and cvei.moduletype = 6";
	    if (isDebug()) System.out.println(cmd);
	    TreeMap rimi = new TreeMap();
	    
	    
	    try {	
	    	ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	        String kood = rs.getString("dunkood").trim();
	        String rimkood = rs.getString("klientkood").trim();
	        rimi.put(kood, rimkood);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

	        return rimi;
	        
  }

	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getDir() + "/xls/sortimendid.xls");
		TreeMap rimi = getRimi(); 
		
		try {
			
			/*
			  String sql = "SELECT sortdate from mj_assortment where  " + tananepaev;
        	  boolean insertassort = true;
        	  Connection con = DriverManager.getConnection(url2,"aivarm", "mobilejava");
  	        Statement stmt = con.createStatement();
  	        
  	        ResultSet rs = stmt.executeQuery(sql);
  	        if(rs.next()){
  	        	insertassort = false;
  	        	//kontrollime kas on vaja insertida 
  	        }
  	        System.out.println("Sisestame sortimenti " + insertassort );
			*/
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Tooted");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 5; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";
//System.out.println("rida " + i);
				if (row != null &&  row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 4; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.equals("1")) {

							//	 System.out.println( a + " " + prodID + " " +
								// clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
									if(clGroup.trim().startsWith("RIM") && prodID != null
											&& rimi.get(prodID.trim()) != null){
										mline.setLineinfo("RimiKood:" + rimi.get(prodID.trim()));
									
									}
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
					
								result.add(mline);
							}
						}
					}
					   
                       }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		 System.out.println("fix suurus " + result.size());

		return result;

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          String sql = " SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " +
         " MIN(PRICEDISCTABLE.AMOUNT) AS price   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp ," +
		" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
         "  FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='VIR' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=0 " + 
         "  AND  (GETDATE()>=PRICEDISCTABLE.FROMDATE " + 
         " AND (GETDATE()<=PRICEDISCTABLE.TODATE+1 OR PRICEDISCTABLE.TODATE=('1900-01-01 00:00:00.00'))) " + 
         "  GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

          try {

        	Statement stmt = this.getNewConnection().createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String prodID = rs.getString("prodid");
                          String custID = rs.getString("custid");
                          double price = rs.getDouble("price");
                      
                          String algus = rs.getString("algus");
                          String lopp = rs.getString("lopp");
                          int paevi = rs.getInt("paevi");
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
                          
                          // if(paevi < 47){
                        	   if (algus != null && algus.trim().length() > 0) {
                                   mline.setStartDate(algus.trim().substring(0,10));
                           }
                               if (lopp != null && lopp.trim().length() > 0) {
                                   mline.setEndDate(lopp.trim().substring(0,10));
                           }  	  
                          //}else{
                        //	  mline.setProdLevel("pr");
                         // }
                       
                          lines.add(mline);
                  }
                  rs.close();
                  
                  
                  String sql2 = " SELECT PRICEDISCTABLE.ACCOUNTRELATION as clgroup," +
                  " PRICEDISCTABLE.ITEMRELATION as prodid, MIN(PRICEDISCTABLE.AMOUNT) AS price  , min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp" +
              	  " , datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
                  " FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='VIR' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND " +
                  " (GETDATE()>=PRICEDISCTABLE.FROMDATE " + 
                  " AND (GETDATE()<=PRICEDISCTABLE.TODATE+1 OR PRICEDISCTABLE.TODATE=('1900-01-01 00:00:00.00'))) " + 
                  " GROUP BY ITEMCODE, ACCOUNTCODE, ITEMRELATION, ACCOUNTRELATION";
                  
                  rs = stmt.executeQuery(sql2);

                  while (rs.next()) {

                          String clGroup = rs.getString("clgroup");
                          String prodID = rs.getString("prodid");
                          double price = rs.getDouble("price");
                          String algus = rs.getString("algus");
                          String lopp = rs.getString("lopp");
                      int paevi = rs.getInt("paevi");
                          
                          MatrixLine mline = new MatrixLine();
                          
                      
                          if (clGroup != null && clGroup.trim().length() > 0) {
                                  mline.setCliGroup(clGroup.trim());
                          }

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                        // if(paevi < 47){
                        	   if (algus != null && algus.trim().length() > 0) {
                                   mline.setStartDate(algus.trim().substring(0,10));
                           }
                               if (lopp != null && lopp.trim().length() > 0) {
                                   mline.setEndDate(lopp.trim().substring(0,10));
                           } 	 
                         //}else{
                       	 // mline.setProdLevel("pr");
                         //}
                       
                          mline.setPrice(price);
                 
                          lines.add(mline);
                  }
                  
                  rs.close();
                  
        
        
                  
                  

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
	  
String  cmd = " SELECT summ.itemid AS toode ,' ' as ladu ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ " +
          "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " +
          "  INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid='vir' " +
          "  INNER JOIN INVENTTABLE itt  ON itt.itemid=summ.itemid and " +
          "           itt.dataareaid='vir' and itt.ssordereraccount='DUNKER' " +
          "  INNER JOIN WMSPALLET pal  ON dim.wmspalletid = pal.wmspalletid and " +
          "           pal.dataareaid=dim.dataareaid and   pal.sssticker=1 " +
"WHERE summ.DATAAREAID='vin' and dim.DATAAREAID='vin' and inv.DATAAREAID='vir' and " +
"CLOSED = 0 AND moduletype=0 and dim.inventlocationid in ('LK_AKTSIIS','LK_TAVA','ITELLA') and " +
"itt.itemgroupid < 80 GROUP BY summ.itemid having sum(availphysical) >0 order by summ.itemid ";


	  

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
             kogused.put(kood, seis + "");
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
               "  WHERE summ.DATAAREAID='lp2'and dim.DATAAREAID='lp2' and inv.DATAAREAID='lp2' AND CLOSED = 0 AND  moduletype=0 " +
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

  
  
  
  public TreeMap getAgents(){
    String cmd = "select agent from custtable where dataareaid = 'vir' and Agent != '' group by agent";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
    
    try {
    
    	Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("asendajad.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = agent.getID();
          agent.setName(name);
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
        
          
          
  		String agentAGENTS = props.getProperty("dub." +agent.getID());
		if(agentAGENTS != null){
           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	    while(stag.hasMoreTokens()){  	
         agent.addDubbler(stag.nextToken());
         //System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
	       }
         }
		
		 agents1.put(agent.getID(), agent);
          agents.put(name, agent);
        }
    } catch (Exception e){
        getParent().showError(e);
      }
  /*  Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    */
    
    if(rcompany != null){
    	//Converter.sendAgentJson(agents, rcompany, false);
    }
    
    return agents;
  }
  

  @Override
	public void FixLoader() {
	  
	  eanprods =  Converter.loadMore();
	  
	  if(eanprods == null) {
		  eanprods = new TreeMap();
	  }
		this.getProducts(null);
		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
		System.out.println("Contracts Fixx " + contracts.keySet().size() + " " + contracts.keySet().toString());
		if(contracts.size() > 0) {
			Converter.sendContractJson(contracts ,  rcompany,  user ,  pass, "FIXED");
		}
		contracts = Converter.loadSuggFromExcels("excelsugg",eanprods,parent);
		System.out.println("Contracts Sugg " + contracts.keySet().size() + " " + contracts.keySet().toString());
		if(contracts.size() > 0) {
			Converter.sendContractJson(contracts ,  rcompany,  user ,  pass, "SUGGESTED");
		}
		
		
		System.exit(0);
	}
  
  private HashMap sortimendid = null;
  public TreeMap getCustomers(Agent agent){

	  sortimendid = new HashMap();
      
      File fff = new File(this.getDir() + "/xls/sortimendid.xls" );
      TreeMap groups = new TreeMap(); 
  	try{
          FileInputStream ff = new FileInputStream(fff);
  		HSSFWorkbook workbook = new HSSFWorkbook((ff));
  		HSSFSheet sheet = workbook.getSheet("Kauplused");
  		HSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  	//	System.out.println("rownum " + row.getLastCellNum());
  		
  		
  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
  			try{
  			//	Loeme sisse rea
  				row = sheet.getRow(a);
  				if (row.getCell((short) 0) != null) {
  				


  	    			   String kood = null;
  	    			  if(row.getCell((short)0) != null){
	    	    			  	try{
	    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
	    	    			  	 }catch (Exception e){
	    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
	    	    	    		} 
	    	    			  	 if(kood.indexOf(".")!= -1){
	    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
	    	    			  	 }
	    	    			  	 String sort = null;
	    	    			  	try{
	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
		    	    	    		} 
		    	    			  	 if(sort.indexOf(".")!= -1){
		    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
		    	    			  	 }
	    	    			  	 groups.put(kood,sort);
  	    			  
  	    				
  				}	   
  				}
 			}catch (Exception e){
  			//	System.out.println(row.getCell((short)a).getNumericCellValue());
  			}
  		}
  		
  		//System.out.println(groups.toString());
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}

    String cmd = "SELECT cus.name,cus.custclassificationid as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer,  " +
            " (SELECT sum(amountmst ) FROM custtransopen co WHERE co.duedate<(getdate()-1)  and (co.dataareaid = 'du2') " +
            " AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " +
            " cus.invoiceaccount end )) AS debt,(SELECT sum(amountmst ) FROM custtransopen co WHERE (co.dataareaid = 'du2') " +
            " AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  " +
            " cus.invoiceaccount end )) AS used, cus.linedisc ,  " +
            " cus.address, DunVisitDay AS visit ,agent AS agent , numofdays AS term, blocked AS block ,  " +
            " (select max (blocked) from custtable cc where cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = 'vir')  " +
            " as payerblock ,phone + ', ' + CellularPhone as info , PriceGroup as grupp ,dunorderinfomail  as confmail, '' as abiagent, ''" +
            " as abipaev ,  (select top 1 abi.CreditMax  from DunCustContractTable abi where (case when len(cus.invoiceaccount)=0 then cus.accountnum else " + 
            " cus.invoiceaccount end )= abi.CustAccount " +
            " AND abi.Status=1 and abi.dataareaid='du2') as credit ,cus.affiliatedgroup as chain" +
            " FROM custtable cus left outer JOIN paymterm " +
            " pay on cus.paymtermid=pay.paymtermid and pay.dataareaid='du2' " +
            " WHERE cus.dataareaid='vir' AND blocked != 2 AND (cus.ssordereraccount='DUNKER' or cus.ssordereraccount='') " +
            " order by cus.name" ;


 
 String ccc = "select invoiceaccount from custtable where dataareaid='vir' group by invoiceaccount";
 
 String sql = "	SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata,  " +
 " co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co  " +
 " INNER JOIN custtrans a ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
 " WHERE (co.dataareaid='du2' or co.dataareaid='dun') AND (co.dataareaid='du2' or co.dataareaid='dun') " +
 " and a.invoice != '' AND co.duedate < getdate()+5 ORDER BY a.accountnum,co.transdate  ";

 
           
           
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    try {
    	Statement stmr = this.getNewConnection().createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
         payers.add(rs1.getString("invoiceaccount"));
        }
  
        rs1.close();
        stmr.close();
    	
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	//if(!name.trim().startsWith(alias.trim()))
        	//name = " " + alias + " " + name ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        cust.setName(name);
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	cust.setPayer(payer.trim());
        }else{
        	cust.setPayer(accountnum);
        }
 
        String debtStr = rs.getString("debt");
        if (debtStr != null){
          cust.setDebt((int)Double.parseDouble(debtStr));
        }
        
        String usedStr = rs.getString("used");
        if (usedStr != null){
          cust.setUsedCredit((int)Double.parseDouble(usedStr));
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
        	
        	aadr = "." + aadr + ",Estonia.";
          cust.setAddress(Converter.enCodeEst(aadr.trim().replace('\n',',')));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));   
         // cust.setMainAgent(agen.trim());
          if (agen != null && agen.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
				
				if(agg != null && agg.getDubbler().size()>0){
					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
					//	System.out.println(cust.getName() + cust.getAgents());
					}
				}
			}
          
        }
        
        if(cust.getID().equals("ERAISIK") || cust.getID().equals("PAKKUMINE") || 
        		cust.getID().equals("MUUD") ){
        	Enumeration enume = getParent().getAgentsHash().elements();
            while (enume.hasMoreElements()) {
              Agent a = (Agent) enume.nextElement();
              cust.addAgent(a);
            }
        }
       
      
        
        
        String term = rs.getString("term");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
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
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
          cust.setGroup(grupp.trim());
        }
        
        String gro = (String) groups.get(cust.getID());
        if( gro != null && gro.trim().length() > 0){
        	cust.addFixSortCode(gro);
        //	System.out.println("Settin fixsorti " + gro);
        }
        
        String confmail = rs.getString("confmail");
        if (confmail != null && confmail.trim().length()>0) {
          cust.setConfirmEmail(confmail.trim());
        }
        
        
        
        String abiagent = rs.getString("abiagent");
        if (abiagent != null && abiagent.trim().length()>0) {
        	Agent ad = new Agent(abiagent);
        	String vis = rs.getString("abipaev");
        	if(vis != null && vis.trim().length() >0 ){
        		ad.setDays(vis);
        	}
          cust.addAgent(ad);
        }
        
        
        
       double  credit = rs.getDouble("credit");
       if(credit != 0){
    	   cust.setCredit(((int)credit)+"");
       }
         
       
       String chain = rs.getString("chain");
       if (chain != null && chain.trim().length()>0) {
         cust.setChain(chain.trim());
       }
      
       
       String cmd2 =  "  SELECT disc.percent1 AS discount FROM pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
       "  disc.DATAAREAID='VIR' AND disc.RELATION=5 AND"+
       "  disc.ACCOUNTCODE=1 AND disc.ITEMCODE=1 AND disc.CURRENCY='EUR'";
             rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                    
                    
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	laks = false;
                    }
                    
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }

       Customer cccr = new Customer("OTHERSTUFF");
       cccr.setName("x Muud Tegevused");
       cccr.setAddress("Kuskil kohas");

      	Enumeration enume = getParent().getAgentsHash().elements();
          while (enume.hasMoreElements()) {
            Agent a = (Agent) enume.nextElement();
            cccr.addAgent(a);
          }
     
          customers.put("x Muud Tegevused", cccr);
          custMap.put("OTHERSTUFF",cccr);
      
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        debt.setPayerID(debt.getCustID());
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
    
    if(rcompany != null){
    //	Converter.sendCustomerJson(customers, rcompany,false,user,pass,true);
    }
    
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "    SELECT itemgroupid as liik,name as nimi FROM inventitemgroup  where dataareaid ='du2'  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        tm.put(liik,  Converter.enCodeEst(rs.getString("nimi").trim()));
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
  public TreeMap getAgents1() {
		return agents1;
	}
  
  private TreeMap loadMore() {
	   TreeMap res = new TreeMap();
	   try {
		   FileInputStream is = new FileInputStream(new File("eans.txt"));
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader reader = new BufferedReader(isr);
			
			
		    String         line = null;
		 
		    try {
		        while((line = reader.readLine()) != null) {
		      StringTokenizer st = new StringTokenizer(line,";");
		      while(st.hasMoreTokens()) {
		    	  res.put(st.nextToken(), st.nextToken());
		      }
		        }

		       
		    } finally {
		        reader.close();
		    }
		 
	   }catch(Exception e) {
		   
	   }
	   
	
	   
	 System.out.println("lis suurus " + res.size());
	   
	   return res;
  }
  

  private TreeMap agents1 = new TreeMap();
  private TreeMap eanprods = null;
  
  public TreeMap getProducts(Agent agent){

	  String cmd = "  SELECT inv.itemid,inv.itemname +' '+RTRIM(CAST(CAST(inv.DUNALCOHOLPERCENT AS decimal(15,1)) AS char(5)))+'%' AS nimetus, ISNULL(CAST(U.FACTOR AS int), 1) AS kastis,STANDARDPALLETQUANTITY AS alusel, disc.amount AS price   " +
	  " , bar.itembarcode AS ean ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr ," +
	  "(SELECT TOP 1 ISNULL(CONVERT(char(10), PURCHLINE.DELIVERYDATE, 104), '')" + 
	  "FROM PURCHLINE WHERE PURCHLINE.DATAAREAID='vin' AND PURCHLINE.QTYORDERED<>0 AND PURCHLINE.PURCHSTATUS=1 " + 
	  "AND PURCHLINE.ITEMID=inv.itemid ORDER BY PURCHLINE.DELIVERYDATE ) AS tarne_kp" + 
	  " FROM inventtable inv  " +
	  " left JOIN pricedisctable disc ON inv.itemid=disc.itemrelation and disc.dataareaid= inv.dataareaid " +
	  " left JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid "+
	  " left join inventitembarcode bar ON bar.dataareaid = inv.dataareaid and bar.itemid=inv.itemid and bar.useforinput  = 1 " +
	  "  LEFT JOIN UNITCONVERT u ON inv.ITEMID=u.ITEMID AND u.DATAAREAID=inv.dataareaid  AND u.TOUNIT='kast'" +
	  " WHERE disc.dataareaid='vir' and inv.dataareaid='vir' and inv.ssOrdererAccount='DUNKER' and u.dataareaid='vir'   AND mod.dataareaid='vir'  " +
	  " and (disc.todate>getdate()-1 OR (disc.fromdate<=getdate() AND disc.todate=('1900-01-01 00:00:00.00') ) )  "+
	  " AND moduletype=2 and blocked = 0  and inv.itemgroupid not in ('91','92','94') "+
	  "  and disc.accountrelation=''";
    
    getSaveStockInfo();
  
  
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = kood.substring(0,2) + " " +rs.getString("nimetus").trim() + " " + kood.substring(2) ;
     nimetus =  Converter.enCodeEst(nimetus);
    if(kogused.get(kood) != null){
    	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
    }else{
    	prod.setStock(0);
    }
       prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));
        prod.weblink = prod.getID();

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        add = " " +  ean.trim().substring(ll-3);
        if(eanprods == null) {
        	eanprods =new TreeMap();
        }
        eanprods.put(ean.trim(), prod.getID());
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
   
     }

      prod.setName(nimetus + add);

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("20");
    }
  
      String alegr = rs.getString("alegr");
      if(alegr!=null && alegr.trim().length()>0){
       prod.setAlegrupp(alegr);
    }
      
      String tarne_kp = rs.getString("tarne_kp");
      if(tarne_kp !=null && tarne_kp.trim().length()>0){
       prod.setPartii("Saabub : " + tarne_kp);
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
      
    } catch (Exception e){
      getParent().showError(e);
    }

    if(rcompany != null){
    	//Converter.sendProductJson(products, rcompany);
    }
    
    
    
    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    //if (true) return custProds ;
	 int counter = 1;
	 String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok " + 
	" FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON " +
	"  tra.invoiceid=jou.invoiceid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid ='vir'  "+ 
	"  WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid like 'du%'  AND jou.dataareaid like 'du%'  AND cus.dataareaid = 'vir' " +
	"  and tra.qty !=0	  GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	"  ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " ;


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
	                 double hind = rs.getDouble("hind");
	     	        String bill = rs.getString("dok");          
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	        cp.setAbiKood(bill);

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
    
  String cmd =" SELECT  cus.agent as agent,round(SUM(lineamount),0) AS summa " +
  " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid and inv.ssordereraccount='DUNKER' and inv.itemid not like '9%' INNER JOIN custinvoicejour jou ON  " + 
  " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount WHERE " +
  " inv.dataareaid='vir' AND sal.dataareaid='du2' AND sal.DIMENSION='SALE' AND cus.dataareaid='vir'  AND jou.dataareaid='du2' AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
  "  AND MONTH(sal.invoicedate) = MONTH(getdate()) "+
  " GROUP BY cus.agent";

  cmd = "SELECT cus.agent as agent, round(SUM(lineamount),0) AS summa FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid " + 
  		"      and inv.ssordereraccount='DUNKER' and inv.itemid not like '9%' " + 
  		" INNER JOIN custinvoicejour jou ON sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid " + 
  		" INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE  inv.dataareaid='vir' AND sal.dataareaid='du2' " + 
  		" AND sal.DIMENSION in ('SALE','DUN','DEK','MED') " + 
  		" AND left(sal.INVOICEID,3) not in ('VSA','DSA') AND cus.dataareaid='vir' AND jou.dataareaid='du2' " + 
  		" AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY cus.agent" ;
  		

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
    return "AxaDunker";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.orderid AS arve, sal.orderdate AS kuupaev,cus.name AS klient, count(rid.itemid) AS summa  " +
	    " FROM perinttelematable sal INNER JOIN custtable cus ON sal.CUSTACCOUNT=cus.accountnum  " +
	    " INNER JOIN perinttelemaline rid ON sal.orderid=rid.orderid  WHERE sal.dataareaid='dun' AND cus.dataareaid='dun'  AND rid.dataareaid='dun' AND cus.salesrep ='" + agent.getID() + "' AND " +
	    " sal.ORDERDATE > getdate()-0.8  and referenceno = '' GROUP BY sal.orderid, sal.orderdate , cus.name ORDER BY sal.orderdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Vormistatud tellimused:\n\n");

	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }

	        //stmt = con.prepareStatement(sql);
	        //stmt.sets(1, agent.getObject());
	          //stmt.setString(2, agent.getObject());
	                  /* 	    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
	"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
	"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
	"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
	"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
	"inv.dataareaid='bal' AND sal.dimension='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
	"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";
   ResultSet	      rs = stmt.executeQuery(sql);
	          ResultSetMetaData md = rs.getMetaData();
	          sb.append("Nimi  \t\tLadu\t\tM��dud\t\tSumma\n");
	          sb.append("---------------------------------------------------\n");

	          double totalKokku = 0d;

	          while (rs.next()) {
	            int ladu = rs.getInt("ladu");
	            int kogus = rs.getInt("kogus");
	            double hind = round(rs.getDouble("myygihind"), 2);
	            double kokku = round(kogus*hind, 2);
	            totalKokku = round(totalKokku + kokku, 2);

	            sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

	            sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
	              kokku + "\n\n");
	           }

	          sb.append("---------------------------------------------------\n");
	          sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");

*/
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
  private String url2 = "jdbc:odbc:report";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();



	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
	
	private void makePictureCat(){
	    
		
		String pictssou = parent.getProps().getProperty("pict.dir");
		    String pictspda = parent.getProps().getProperty("pictpda.dir");
	   
		 try {
	 	  
	      File f = new File(pictspda +"list.txt");
	      if (f.exists())f.delete();
	      //Kataloogi sisude kustutamine
	  	 	 File res2 = new File (pictspda);
	         String[] ff2 = res2.list();
	         if(ff2 != null) {
	     	 for(int i = 0;ff2.length>i;i++){
	         	(new File ( pictspda + ff2[i])).delete(); 
	         }
	         }
//	     	 Lõppes kustutamine 
			 
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
	         	if( ff2[i].startsWith(toode)){
	           // System.out.println("pilt olemas " + ff2[i]);
	        	File in = new File ( pictssou + ff2[i]); 
	          	if(in.isFile()){
	          	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
	              	Converter.shrinkAndCopyFile(in,inpda,400);
	        	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                	}
	               }
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
