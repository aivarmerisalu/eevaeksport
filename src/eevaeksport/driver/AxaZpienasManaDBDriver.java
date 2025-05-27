package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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

public class AxaZpienasManaDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaZpienasManaDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    NF.setMaximumFractionDigits(2);
    NF.setGroupingUsed(false);
    NF.setMinimumFractionDigits(2);
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
   	return null;
   }
  private HashMap fixGroups = new HashMap();
  
  private void makeFixHash(){
	  File f = new File(this.getExcelDir() + "Sortiments.xls");
	  try {
			
			this.fixGroups = new HashMap();
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(1);
			HSSFRow row = null;
		
			for (int i = 0; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String custID = "";
				String sortcode = "";

		
					try {
						custID = (row.getCell((short) 0).getStringCellValue());
					
					} catch (Exception e) {
						try {
						custID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
						} catch (Exception u) {
							
						}
					}
					
					try {
						sortcode = (row.getCell((short) 1).getStringCellValue());
						
					} catch (Exception e) {
						try {
						sortcode = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";	
					} catch (Exception u) {
						
					}
					}
					
					if(!custID.equals("")){
						 if(this.fixGroups.get(custID) != null){
						    	Vector sorts = (Vector)fixGroups.get(custID);
						    	sorts.add(sortcode);
						    	fixGroups.put(custID, sorts);
						    }else{
						    	Vector sorts = new Vector();
						    	sorts.add(sortcode);
						    	fixGroups.put(custID, sorts);
						    }
		
					}
			   			}

	  
	  } catch (Exception d) {
			// System.out.println(row.getCell((short)a).getNumericCellValue());
		}
	// System.out.println(fixGroups.toString()); 
  }



  public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "Sortiments.xls");
		
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 7; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                    if(!prodID.equals("0")){
                 	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

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

		// System.exit(0);

		return result;

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         String sql = "SELECT  cus.invoiceaccount as custid , max(accountnum) as klient,  '' AS clgroup,  " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid,  " +
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount) " +  
                  "  AS price  FROM axa2.pricedisctable pd left join axa2.custtable cus on SUBSTR(NLS_LOWER(cus.accountnum),1,20) = " +
                  "  SUBSTR(NLS_LOWER(pd.accountrelation),1,20) " +
                  "   and SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(pd.DATAAREAID),1,3) where  " +
				  "  SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3) = 'zp'   and SUBSTR(NLS_LOWER(pd.DATAAREAID),1,3) = 'zp'     " +
				  " and  pd.fromdate<SYSDATE and pd.todate>SYSDATE " +
				  " and  pd.relation in (4,5) " +
				  " group by cus.invoiceaccount, pd.itemrelation,pd.itemcode  order by  cus.invoiceaccount,pd.itemrelation ";

          try {

                  Connection ss = this.getNewConnection();
        	      Statement stmt = ss.createStatement();
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
                  
                  
                  String sql2 = "  select pd.accountrelation AS clgroup,  " +
				  " (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +
				  " (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,(percent1) AS discount, " +
				  "   pd.fromdate as algus , pd.todate as lopp from  " +
				  " axa2.pricedisctable pd where   pd.fromdate<sysdate+3 and pd.todate>=sysdate and pd.accountcode=1 and percent1 > 0 and pd.dataareaid ='zp' " +
				  "   order by pd.accountrelation , pd.itemrelation" ;
                  
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
                  
                  
                  rs.close();
                  stmt.close();
                  ss.close();

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

  
  public DealerProduct getDealerProductsList() {

  
      DealerProduct dealerProds = new DealerProduct();
      try {

        String  sql = "SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu FROM axa2.INVENTSUM summ " +
               "  INNER JOIN axa2.INVENTDIM dim ON SUBSTR(NLS_LOWER(summ.inventdimid),1,20)=SUBSTR(NLS_LOWER(dim.inventdimid),1,20)  and SUBSTR(NLS_LOWER(dim.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(summ.DATAAREAID),1,3) " +
               "  INNER JOIN axa2.inventtablemodule inv ON inv.itemid=summ.itemid   and  SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(summ.DATAAREAID),1,3) " +
               "  WHERE SUBSTR(NLS_LOWER(summ.DATAAREAID),1,3)='zp'and SUBSTR(NLS_LOWER(dim.DATAAREAID),1,3)='zp' and " +
               "  SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)='zp' AND CLOSED = 0 AND  moduletype=0 " +
               "    and dim.inventlocationid like'%PROD%' " +
               "  GROUP BY dim.inventlocationid,summ.itemid  ORDER BY dim.inventlocationid,summ.itemid ";

           if (isDebug()) System.out.println(sql);
         Connection con2 = this.getNewConnection();
              Statement stm = con2.createStatement();
              ResultSet rst = stm.executeQuery(sql);

                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  dealerProds.add(ladu, toode,seis+""," "," " );
              }
              rst.close();
              stm.close();
              con2.close();

      } catch (Exception e) {
          getParent().showError(e);
      }

       return dealerProds;
     }

  
  TreeMap agnames = null;
  
  public TreeMap getAgents(){
    String cmd = "select emplid as kood , name AS name  from axa2.empltable where dataareaid = 'zp' ";// +
 // " and title = 'Vadybininkas'";
    Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    StringBuffer sb2 = new StringBuffer();
    agnames  = new TreeMap();
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);
          agents.put(name + agent.getID(), agent);
         // if(props.containsKey(agent.getID())){
        	  agnames.put(agent.getID(), name);
              sb2.append(agent.getID() + ";" + name + "\n");  
        //  }
          
        }
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    
    
    Agent bb = new Agent ("KLABOS");
    bb.setName("KLABOS");
    agents.put("KLABOS", bb);
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
    	}catch (Exception e){
    		
    	}
      
     }
    makeManagerFile();
    
    return agents;
  }
  
  

  
  private void makeManagerFile(){
	    String sql = "SELECT clid AS klient , enddate AS kuupaev , docsum AS summa , linecount AS ridu,text AS tekst," +
        " longitude,lattitude FROM dayreport WHERE enddate>(GETDATE()-15) ORDER BY clid";
	    try{
	    	Connection c = getNewConnection3();
	    	Statement st = c.createStatement();
	    	ResultSet rs = st.executeQuery(sql);

           StringBuffer sb = new StringBuffer();
	    	String sep = ";";
	    	while(rs.next()){
	    	String cliID = rs.getString("klient").trim();
	    	sb.append(cliID + sep);
	    	sb.append(rs.getString("kuupaev").trim() + sep);
	    	sb.append(rs.getDouble("summa") + sep);
	    	sb.append(rs.getInt("ridu") + sep);
	    	String tekst = ensureLen(rs.getString("tekst")," ");
	    	tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
	    	if (tekst == null) tekst = " ";
	    	sb.append(tekst + sep);
	    	String longi = rs.getString("longitude") ;
	    	String latti = rs.getString("lattitude") ;
	    	if(longi.indexOf(".0000") != -1){
	    		longi = "0";
	    	}
	    	if(latti.indexOf(".0000") != -1){
	    		latti = "0";
	    	}
	    	sb.append(latti + sep);
	    	sb.append(longi + sep);
	    
	    	sb.append("\n");
	    	}
	    	
	      Converter.saveFileInUTF8("managerdata.txt", sb);
	    
	    }catch(Exception e){
          e.printStackTrace();	    	
	    }
  }

  public TreeMap getCustomers(Agent agent){
	  
	
	  makeFixHash();
	 // makeManagerFile();
	  
 String cmd = "  SELECT cus.name,street as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
              " 0 AS debt, cus.linedisc ,   " +
              "    cus.address, SalesPoolId  AS visit ,emplid AS agent,cus.ordertaker_bbs AS agent2 ,  numofdays AS term, cus.blocked AS block ,  " + // SalesPollId as visit
			 " (select max (blocked) from axa2.custtable cc where  cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = cus.dataareaid ) " + 
			 "  as payerblock ,phone || ' '|| telefax || ' '|| cellularphone as info , linedisc as grupp, MINORDERAMOUNT_BBS as minord , " +
			"	transportroutecode as transa,'' as discount ,InventQtyRounding_bbs as samm , custsubgroupcode as tyyp , ShelfArea_bbs as sharea," +
            " ShelfPart_bbs As shshare FROM axa2.custtable cus left outer JOIN axa2.paymterm  " +
			"  pay on SUBSTR(NLS_LOWER(cus.paymtermid),1,20)= SUBSTR(NLS_LOWER(pay.paymtermid),1,20) and SUBSTR(NLS_LOWER(pay.DATAAREAID),1,3) = SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3) and  pay.dataareaid='zp' " +
			"	 WHERE   cus.dataareaid='zp' AND cus.blocked != 2  " ;
 
 String ccc = "select invoiceaccount from axa2.custtable where dataareaid='zp' and invoiceaccount is not null  group by invoiceaccount";
 
           String sql = "SELECT  a.ORDERACCOUNT AS klient,a.invoice AS numbe,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
                  "  co.transdate AS kuupaev , round (sysdate - co.duedate,0) AS yle FROM axa2.custtransopen co " +
				  "  INNER JOIN axa2.custtrans a ON co.REFRECID=a.recid and  SUBSTR(NLS_LOWER(co.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(a.DATAAREAID),1,3)  " +
                  "   WHERE co.dataareaid='zp' AND a.dataareaid='zp' and a.invoice is not null		 " +
				  " and a.accountnum not in('140700','9000') ORDER BY a.accountnum,co.transdate ";

           
           String salessql = "    SELECT  jou.orderaccount as klient,round(SUM(lineamount),0) AS summa,TO_CHAR(TRUNC(sal.invoicedate, 'MM'),'YYYYMM') as kuu " +  
        " FROM axa2.custinvoicetrans sal  INNER JOIN axa2.custinvoicejour jou ON  SUBSTR(NLS_LOWER(sal.invoiceid),1,20) =SUBSTR(NLS_LOWER(jou.invoiceid),1,20)  and " + 
       "  SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) WHERE " + 
	   " sal.dataareaid='zp' AND sal.invoicedate > sysdate -390  GROUP BY jou.orderaccount,TO_CHAR(TRUNC(sal.invoicedate, 'MM'),'YYYYMM') " +
	   " ORDER BY jou.orderaccount,TO_CHAR(TRUNC(sal.invoicedate, 'MM'),'YYYYMM') " ;
               
           
           
           String sss = "SELECT  accountnum, sum(case when co.duedate<sysdate then amountmst else 0 end) as asi ,sum(amountmst) as asi2 FROM axa2.custtransopen co " +
           		"WHERE co.dataareaid='zp'   " +
           "  group by  accountnum";
           
           
           
           String cmd2 = " SELECT disc.accountrelation as klient ,disc.percent1 AS discount FROM axa2.pricedisctable disc  WHERE " +
                 "   disc.dataareaid='zp' AND  ltrim(disc.itemrelation)='0'  AND fromdate<=sysdate and todate >= sysdate ";
          
           TreeMap aled = new TreeMap();
           try{
         
        	   Statement stm = this.getNewConnection().createStatement();
        	  ResultSet rs1 = stm.executeQuery(cmd2);
               while(rs1.next()){
            	   String klient = rs1.getString("klient").trim();
                 double ale = rs1.getDouble("discount");
                aled.put(klient, ale + "");
               }
              rs1.close();  
              stm.close();
           }catch (Exception r){
        	   System.out.println(cmd2);
        	   r.printStackTrace();
           }

       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap custDebt = new TreeMap();
    TreeMap totalDebt = new TreeMap();
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();
    try {
    	Statement stmr = this.getNewConnection().createStatement();
    	
        ResultSet rs1 = stmr.executeQuery(salessql);
        while(rs1.next()){
        	String klient = rs1.getString("klient").trim();
        	
        	int summa = (int)rs1.getDouble("summa");
        	String kuu = rs1.getString("kuu");
          if(salesres.get(klient) == null){
        	  salesres.put(klient,"   " + kuu + " : " + summa + " lt" );
          }else{
        	  String asi = (String)salesres.get(klient);     	  
        	  salesres.put(klient,"   " + kuu + " : " + summa +" lt|" + asi);
          }
        }
       rs1.close();
       stmr.close();    	
    	
    	
       Statement stmu = this.getNewConnection().createStatement();
    	
         rs1 = stmu.executeQuery(sss);
        while(rs1.next()){
        	
       String ee = rs1.getString("accountnum").trim();
       double debti = rs1.getDouble("asi");
       if(debti != 0){
    	   custDebt.put(ee, debti + "");
       }
       double alld = rs1.getDouble("asi2");
       if(alld != 0){
    	   totalDebt.put(ee, alld + "");
       }
          
        }
       rs1.close();
       stmu.close();
    	
        stmr = this.getNewConnection().createStatement();
    	 rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
    	
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = rs.getString("name").replaceAll("UAB", " ").trim();
        
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name =  name + " " + alias ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
       	
        Vector grupp = (Vector)fixGroups.get(cust.getID());
          if(grupp != null  ){
          	cust.setFixSortCode(grupp);
          }
        name = name + " " + accountnum;
        name = name.replaceAll("\\r\\n|\\r|\\n", " ");
        name = name.replaceAll("'", " ").replaceAll("\"", " ").replaceAll("  ", " ");
        cust.setName( Converter.enCodeEst(name.trim()));
        
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        cust.setPayer(payer);
        String debtStr = (String)custDebt.get(payer);
        if (debtStr != null){
          cust.setDebt(Double.parseDouble(debtStr));
        }
        String totalStr = (String)totalDebt.get(payer);
        if (totalStr != null){
          cust.setUsedCredit(Double.parseDouble(totalStr));
        }

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }
        
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Pardavimai :|M�nuo ir metai|Pardavimai Lt|" + salesres.get(accountnum) );
        }
        

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Lithuania").replaceAll("\\r\\n|\\r|\\n", " "));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(Converter.getVisitDay(visit.trim()));
        }
        String agen = rs.getString("agent");
      
        if (agen != null && agen.trim().length() >0) {
          cust.addAgent(new Agent(agen.trim()));
          cust.addAgent(new Agent("HOUSE1"));
          cust.setMainAgent(agen.trim());
          if(agen.trim().startsWith("VIL") || agen.trim().startsWith("ALY") ){
        	  cust.addAgent(new Agent("BLAVAL"));
          }
          
          if(agen.trim().startsWith("KLA")){
        	  cust.addAgent(new Agent("KLA002"));
        	  cust.addAgent(new Agent("KLABOS"));
          }
          
          if(agen.trim().startsWith("PAN")){
        	  cust.addAgent(new Agent("MONSTV"));
          }
          
          if(agen.trim().startsWith("KAU")){
        	  cust.addAgent(new Agent("BAGEVA"));
          }
          
          if(agen.trim().startsWith("TEL") || agen.trim().startsWith("MAZ")
        		  || agen.trim().startsWith("TAU") || agen.trim().startsWith("PLU")){
        	  cust.addAgent(new Agent("DAVEIL"));
          }
        }

        
        String agen2 = rs.getString("agent2");
        
        if (agen2 != null && agen2.trim().length() >0) {
          cust.addAgent(new Agent(agen2.trim()));
         
          if(agen2.trim().startsWith("VIL") || agen2.trim().startsWith("ALY") ){
        	  cust.addAgent(new Agent("BLAVAL"));
          }
          
          if(agen2.trim().startsWith("KLA")){
        	  cust.addAgent(new Agent("KLA002"));
          }
          
          if(agen2.trim().startsWith("PAN")){
        	  cust.addAgent(new Agent("MONSTV"));
          }
          
          if(agen2.trim().startsWith("KAU")){
        	  cust.addAgent(new Agent("BAGEVA"));
          }
          
          if(agen2.trim().startsWith("TEL") || agen2.trim().startsWith("MAZ")
        		  || agen2.trim().startsWith("TAU") || agen2.trim().startsWith("PLU")){
        	  cust.addAgent(new Agent("DAVEIL"));
          }
        }
        
    
        
        int term = rs.getInt("term");
        if (term > 0) {
          cust.setPaymTerm(term + "");
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
          cust.setPersonalData(info.trim().replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        double  minord  = rs.getDouble("minord");
        if (minord > 0 ) {
          cust.setMinsum(minord);
        }
        
        String transa = rs.getString("transa");
        if (transa != null && transa.trim().length()>0) {
          cust.setCity("Mar�ruto numeris: " +transa.trim());
          
        }
        String rounding = rs.getString("samm");
        if (rounding != null && rounding.trim().equals("2")) {
          cust.setMakeAmountRounding(true);        
        }
        
        if(aled.get(cust.getID())!= null){
        	cust.setDiscount(Double.parseDouble((String)aled.get(cust.getID())));
        }
        
        String tyyp = rs.getString("tyyp");
        if (tyyp != null && tyyp.trim().length() > 0) {
        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("Tipas :" + tyyp.trim());
        	}else{
        		cust.setCity(cust.getCity() + "| Tipas :" + tyyp.trim());
        	}
          cust.setMakeAmountRounding(true);        
        }
        
        
        String sharea = rs.getString("sharea");
        String shshare = rs.getString("shshare");
        String add = "";
        if(sharea != null && sharea.trim().length() > 0 && !sharea.equals("0")){
        	add = "Shelf Area : " + sharea.trim() + "|";
        }
        if(shshare != null && shshare.trim().length() > 0 && !sharea.equals("0")){
        	add = add +  "Shelf Share : " + shshare.trim();
        }
       
        
        if(add.length() > 0){
        	   if(cust.getCity()== null || cust.getCity().length() == 0 ){
           		cust.setCity(add);
           	}else{
           		cust.setCity(cust.getCity() + "|" + add);
           	}	
        }
     
      //  cc.addQuestiosToCustomer(cust,kyssad);
     
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }

      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("numbe").trim();
                        if(number != null && number.trim().length() > 0){
                            debt.setDoknr(number);
                            debt.setDuedate(rs.getDate("maksepaev"));
                            debt.setSum(rs.getDouble("tasumata"));
                            debt.setDate(rs.getDate("kuupaev"));
                            debt.setDue(rs.getString("yle"));
                            this.debt.addElement(debt); 	
                        }
                      }
                rs.close();
                
                String fff = "SELECT  custaccount as klient,name as nimi,birthdate_bbs as synna ,email,phone, cellularphone FROM axa2.Contactperson co " +
               	"	WHERE co.dataareaid='zp'  and length (trim(custaccount)) > 1  and {fn year(birthdate_bbs)} != 1900 ";
              

                   StringBuffer sb2 = new StringBuffer();
                   Connection kkk = this.getNewConnection();
            	    stm = kkk.createStatement();
            	   rs1 = stm.executeQuery(fff);
                   while(rs1.next()){
                	   String klient = rs1.getString("klient").trim();
                	   String nimi = rs1.getString("nimi").trim();
                	   java.sql.Date synna = rs1.getDate("synna");
                	  String email = parent.make(rs1.getString("email"));
                	  String phone = parent.make(rs1.getString("phone"));
              	  String cell = parent.make(rs1.getString("cellularphone"));
                    sb2.append(klient+";"+ nimi + ";" + synna.getTime() + "; ;" + phone + " " +  cell + ";" + email +  "\n");
                   }
                  rs1.close();  
                  stm.close();
                  kkk.close();
                  if(sb2.length() > 2){
                     		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
                     	}
                  


                      
                      
                      
                      
    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host2"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
  private Connection getNewConnection3() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
  
  
  private Connection getNewConnection4() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host4"));
	  }

      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  //  String    sql = "   SELECT PRODUCTLINEID as liik,name as nimi FROM axa2.inventproductline  where dataareaid ='zp'  ";
 
    String    sql = "   SELECT code as liik,name as nimi FROM axa2.itemanagroup  where dataareaid ='zp'  ";
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
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = "  SELECT distinct ANALYTICGROUPID as liik,name as nimi FROM axa2.inventanalyticgroup  where dataareaid ='zp' ";
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

  public TreeMap getProducts(Agent agent){

    String cmd = " SELECT inv.itemid, inv.itemname AS nimetus,  sui.qty AS kastis,'' AS alusel, disc.amount AS price " +
	"  , bar.itembarcode AS ean ,inv.PRODUCTLINEID AS grupp2, inv.ITEMANAGROUPCODE   AS grupp, gr.ANALYTICGROUPID as grid,mod.unitid As unit, 21 as vat , " +
    " mod.LowestQty  as samm ,prodsorting as sorting    FROM axa2.inventtable inv " +
	"   left  JOIN axa2.pricedisctable disc ON SUBSTR(NLS_LOWER(inv.itemid),1,20)= SUBSTR(NLS_LOWER(disc.itemrelation),1,20) and " +
    " SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(disc.DATAAREAID),1,3)and disc.amount != 0  " +
	"   and (disc.todate>sysdate  or   {fn YEAR(disc.todate)} =1900)  and disc.accountcode='2'  " +
	"   left  JOIN axa2.suitetable sui ON  SUBSTR(NLS_LOWER(inv.itemid),1,20)= SUBSTR(NLS_LOWER(sui.suitelineitem),1,20) and " +
    " SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(sui.DATAAREAID),1,3) and sui.type=0  AND sui.dataareaid='zp' and   sui.qty > 1  " +
	"  left JOIN axa2.inventtablemodule mod ON mod.itemid = inv.itemid and SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(mod.DATAAREAID),1,3) " +
	"   left join axa2.inventitembarcode bar on bar.dataareaid = 'zp' and bar.useforprinting = 1 and bar.itemid=inv.itemid and  " +
    "   SUBSTR(NLS_LOWER(bar.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3) " +
	"   left join axa2.inventanalyticgroup gr on gr.dataareaid = 'zp' and gr.ANALYTICGROUPID=inv.ANALYTICGROUPID and  " +
    "     SUBSTR(NLS_LOWER(gr.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3) " +
	"   WHERE   moduletype=2 and blocked = 0 and  inv.dataareaid='zp'   and disc.dataareaid='zp'   and mod.dataareaid='zp'  " +
	"   and (inv.itemgroupid like 'Z%' or inv.itemgroupid like 'S%') and inv.Assortment = 1 order by inv.itemid";
    
    //z p algusega
  

    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst( rs.getString("nimetus").trim() + " " + kood) ;
   
  
        prod.setInCases(rs.getDouble("kastis"));
       // prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        prod.setEAN(ean);
      }
String add = "";
      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
        add = grupp.trim().substring(0,2);
     }
       String grid = rs.getString("grid");
       if(grid!=null && grid.trim().length()>0){
           grid = grid.trim();
           prod.setGroupSecond(grid);
        }else{
        	grid = "";
        }
      String grupp2 = rs.getString("grupp2");
       if(grupp2!=null && grupp2.trim().length()>0){
       // prod.setGroupSecond(grupp2.trim());
   
     }
       
      prod.setName((add +  " " + nimetus.replaceAll("'", " ").replaceAll("\"", " ").trim()).replaceAll("\n", " ").trim());

      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("21");
    }

      int  samm = rs.getInt("samm");
      if(samm>0){
      // prod.setOptAmountInUnits(samm);
    }
      
      String top = rs.getString("sorting");
      if(top != null && top.trim().length()>0 && top.trim().equals("TOP")){
       prod.setStatusTop("1");
    }
        products.put(grid + nimetus, prod);
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
   String cmd = " SELECT orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate " +
     "  FROM axa2.custinvoicetrans  tra INNER JOIN axa2.custinvoicejour jou ON " +
     "  SUBSTR(NLS_LOWER(tra.invoiceid),1,20)=  SUBSTR(NLS_LOWER(jou.invoiceid),1,20) and  SUBSTR(NLS_LOWER(tra.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3) " +
     "  WHERE  jou.invoicedate > sysdate-30 AND SUBSTR(NLS_LOWER(tra.DATAAREAID),1,3) = ('zp')  AND SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)  = ('zp')  " +
     "  GROUP BY orderaccount,tra.itemid,tra.name ,jou.invoicedate ,tra.qty Order by orderaccount,tra.name,jou.invoicedate" ;
  
   if (isDebug()) System.out.println(cmd);

   Calendar cal = Calendar.getInstance();
   cal.add(Calendar.MONTH, -1);

   String asi = cal.get(Calendar.YEAR)+"";
   String m = (cal.get(Calendar.MONTH)+1) + "";
   if(m.length()==1){
	   m = "0" +m;
   }
   String d = cal.get(Calendar.DAY_OF_MONTH) + "";
   if(d.length()==1){
	   d = "0" + d;
   }
   asi = asi + m + d;
   

   String cmdu = " select r.customercode,r.itemcode,r.salesqty,r.invoicedateid from fctSales r inner join dimItem t on t.ItemCode=r.itemcode"
	   		+ " and t.CompanyId=2 where r.invoicedateid > " + asi + " and r.CompanyId=2 " +
	 " order by  r.customercode,t.itemname,r.invoicedateid " ;
	   if (isDebug()) System.out.println(cmdu);

	    try {

	    	Connection ccc = this.getNewConnection4();
	    	Statement stmt =  ccc.createStatement();
	      ResultSet rs = stmt.executeQuery(cmdu);
	      counter++;
	      while(rs.next()){
	    	
	        String kliendikood = rs.getString("customercode").trim();
	        String tootekood = rs.getString("itemcode").trim();
	        if(!custs.keySet().contains(kliendikood+ tootekood)){
			        double kogus = rs.getDouble("salesqty");
			        String lastdate = rs.getString("invoicedateid").substring(4);
			        
			        custs.put(kliendikood + tootekood,lastdate); 
			       
			        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
			        custProds.add(cp);
	        }
	        
	      }
	      rs.close();
	      stmt.close();
	      ccc.close();
      
      
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
    
      int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
      int yy = Calendar.getInstance().get(Calendar.YEAR);
      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
      int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
      double planprec = dd/d2;
      
      String cmd ="SELECT  cus.emplid as agent,round(SUM(lineamount),0) AS summa , round(SUM(sal.qty*inv.NetWeight),0)  as kaal ,count (distinct orderaccount)  as poode " +
      "   FROM axa2.custinvoicetrans sal INNER JOIN axa2.inventtable inv ON SUBSTR(NLS_LOWER(sal.itemid),1,20)=SUBSTR(NLS_LOWER(inv.itemid),1,20) " +
      "    and SUBSTR(NLS_LOWER(inv.dataareaid),1,3)= SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) INNER JOIN axa2.custinvoicejour jou ON  " +
      "  SUBSTR(NLS_LOWER(sal.invoiceid),1,20) =   SUBSTR(NLS_LOWER(jou.invoiceid),1,20)  and SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) " +
      "   INNER JOIN axa2.custtable cus ON SUBSTR(NLS_LOWER(cus.accountnum),1,20) =SUBSTR(NLS_LOWER(jou.orderaccount),1,20)  and SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)  WHERE " +
	  " SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3)='zp' AND SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)='zp' AND SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)='zp' " +
      " AND SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)='zp' AND {fn YEAR(sal.invoicedate)} ={fn YEAR(sysdate)} " +
      "   AND {fn MONTH(sal.invoicedate)} = {fn MONTH(sysdate)}  " +
      "  GROUP BY cus.emplid";
      
      String cmd2 =" SELECT sp as agent, c" + mm + " as volume , t" + mm + " *1000 as summa from SRCADMIN31.\"vadyba2012\"  ";
      if(yy == 2013){
    	  cmd2 = " SELECT sp as agent, c" + mm + " as volume ,t" + mm + " *1000  as summa from SRCADMIN30.\"vadyba2013\"  ";
      }
      if(yy == 2014){
    	  cmd2 = " SELECT sp as agent, c" + mm + " as volume ,t" + mm + " *1000  as summa from SRCADMIN51.\"VADYBA2014\"  ";
      }
      if(yy == 2015){
    	  cmd2 = " SELECT sp as agent, c" + mm + " as volume ,t" + mm + " *1000  as summa from SRCADMIN61.\"VADYBA2015\"  ";
      }
      if(yy == 2016){
    	  cmd2 = " SELECT sp as agent, c" + mm + " as volume ,t" + mm + " *1000  as summa from SRCADMIN71.\"VADYBA2016\"  ";
      }


         if (isDebug()) System.out.println(cmd);
            try {
            	
            	Connection conr = this.getNewConnection2();
                Statement stmtr =  conr.createStatement();
                ResultSet	      rsr = stmtr.executeQuery(cmd2);
                HashMap agplansal = new HashMap();
                HashMap agplanvol = new HashMap();
                while(rsr.next()) {
                  String agentID = rsr.getString("agent").trim();
                  agplansal.put(agentID, (int)rsr.getDouble("volume") + "");
                  agplanvol.put(agentID, (int)rsr.getDouble("summa") + "");
                }
                
                rsr.close();
                conr.close();
            	
            	
            	Connection cone = this.getNewConnection();
                Statement stmt =  cone.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                	  double msales = (int)rs.getDouble("summa");
                	  double mkaal = (int)rs.getDouble("kaal");
                	  
                	  
                	  double psales = 0;
                	  double pkaal = 0;
                	  
                	  try{
                		  psales = Double.parseDouble((String)agplansal.get(agentID));
                	  }catch(Exception e){
                		  
                	  }
                      try{
                    	  pkaal = Double.parseDouble((String)agplanvol.get(agentID));
                	  }catch(Exception e){
                		  
                	  }
                	  
                	  double  tehtudsalperc = msales/psales;
                	  double  tehtudkaalperc = mkaal/pkaal;
                	  
                	  double plaanitavMyykSal = d2 * msales/dd;
                	  double plaanitavMyykKaal = d2 * mkaal/dd;
                	  
                	  double  prognosalperc = plaanitavMyykSal/psales;
                	  double  prognokaalperc = plaanitavMyykKaal/pkaal;
                  String asi = "Pardavimai: " + msales + " �||";
                  if(dd > 6){
                	  asi = asi + "Plano vykdymas/prognoz� (�):";
                      asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                  }
                
                  asi = asi + "Pardavim� planas: " + psales + " �||";
                  
                  asi = asi +"Pardavimai: " + mkaal + " Kg||";
                  if(dd > 6){
                	  asi = asi + "Plano vykdumas/prognoz� (t):";
                      asi = asi + "     " +NF.format(tehtudkaalperc*100) + "% / " + NF.format(prognokaalperc*100)  + " %||";  
                  }
                  
                  asi = asi + "Pardavim� planas: " + pkaal + " Kg||";
                  
               /*   

                	  
                	  Pardavim� planas:
                	  579.438 Lt

                	  Plano vykdumas/prognoz� (t):
                	  74,56% / 94,89%
                	  Pardavimas planas:
                	  102,32 T*/

                  
                    agent.setStart( Converter.enCodeEst(asi + "Aktyv�s klientai ") + (int)rs.getDouble("poode"));
                  }
                }
                rs.close();
                cone.close();
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
    

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
      if (str.length() == 0){
        return replace;
      } else {
        return str;
      }
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
    return "AxaZpienasMana";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.ordernum AS arve, sal.createddate AS kuupaev,departname AS klient, count(rid.itemid) AS summa  " +
	    " FROM axa2.MAXIMSALESTABLE sal " +
	    " INNER JOIN axa2.MAXIMSALESLINE rid ON sal.ordernum=rid.ordernum  " +
 " WHERE  sal.departmen ='" + agent.getID() + "' AND " +
	  "   sal.CREATEDDATE > sysdate-1  GROUP BY sal.ordernum, sal.createddate , departname,sal.dlvdate ORDER BY sal.dlvdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Priimt� u�sakym� s�ra�as:\n\n");

	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  eilut�s: " + (int)rs2.getDouble("summa") + "\n");
	                  }


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
  private String url = "jdbc:odbc:oracle";
  private String url2 = "jdbc:odbc:report";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
