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
import java.util.Enumeration;
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

public class AxaZpienasMercDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaZpienasMercDBDriver(EksportPanel parent){
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
	 System.out.println(fixGroups.toString()); 
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

								 System.out.println( a + " " + prodID + " " +
								 clGroup + " " + go);

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
          if(props.containsKey(agent.getID())){
        	  agnames.put(agent.getID(), name);
              sb2.append(agent.getID() + ";" + name + "\n");  
          }
          
        }
    } catch (Exception e){
        getParent().showError(e);
      }
   Agent aa = new Agent ("UA001");
   aa.setName("Ukraina");
   agnames.put(aa.getID(), aa.getName());
   agents.put( aa.getName() + aa.getID(), aa);
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    		
    	}
      
     }
    return agents;
  }
  

  

  public TreeMap getCustomers(Agent agent){
	  
	 
	  makeFixHash();
	
	  
 String cmd = "  SELECT cus.name,street as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
              " 0 AS debt, cus.linedisc ,   " +
              "    cus.address, SalesPoolId  AS visit ,emplid AS agent ,  numofdays AS term, cus.blocked AS block ,  " + // SalesPollId as visit
			 " (select max (blocked) from axa2.custtable cc where  cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = cus.dataareaid ) " + 
			 "  as payerblock ,phone || ' '|| telefax || ' '|| cellularphone as info , linedisc as grupp, MINORDERAMOUNT_BBS as minord , " +
			"	transportroutecode as transa,'' as discount ,InventQtyRounding_bbs as samm , custsubgroupcode as tyyp , ShelfArea_bbs as sharea," +
            " ShelfPart_bbs As shshare , CustGroup as maar FROM axa2.custtable cus left outer JOIN axa2.paymterm  " +
			"  pay on SUBSTR(NLS_LOWER(cus.paymtermid),1,20)= SUBSTR(NLS_LOWER(pay.paymtermid),1,20) and SUBSTR(NLS_LOWER(pay.DATAAREAID),1,3) = SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3) and  pay.dataareaid='zp' " +
			"	 WHERE   cus.dataareaid='zp' AND cus.blocked != 2  " ;
 
  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();

    
    try{	
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
      
       

        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
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
          cust.setMainAgent(agen.trim());      
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
        String sharea = rs.getString("sharea");
        String shshare = rs.getString("shshare");
        String maar =  rs.getString("maar");
        if(maar != null && maar.trim().equals("7000UZS")){
        	cust.addAgent(new Agent("UA001"));
        }
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
     
                 
                    customers.put(name, cust);
                    custMap.put(cust.getID(),cust);	
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
     "  WHERE  jou.invoicedate > sysdate-15 AND SUBSTR(NLS_LOWER(tra.DATAAREAID),1,3) = ('zp')  AND SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)  = ('zp')  " +
     "  GROUP BY orderaccount,tra.itemid,tra.name ,jou.invoicedate ,tra.qty Order by orderaccount,tra.name,jou.invoicedate " ;
  
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


  private HashMap brandplans = null; 
  
    public void setBulkSales(Map agents){


    	//siin on vaja liita kokku regiooni p�hiselt
    	
      
      this.connectionUp();
        
      if(brandplans == null){
    	  brandplans = new HashMap();
    	
    	  int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
          int yy = Calendar.getInstance().get(Calendar.YEAR);
          int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	  
        
          String cmd2 =" SELECT sp as agent ,brands, c" + mm + " as summa ,t" + mm + "*1000   as volume from SRCADMIN30.vadyba" + yy + "_3 order by sp,brands ";
      // loene k�ik plaanid sisse 
          String ass = null;
          try {

        	  Connection conr = this.getNewConnection2();
              Statement stmtr =  conr.createStatement();
              ResultSet rsr = stmtr.executeQuery(cmd2);
        	  
                      while (rsr.next()) {
                      String age = rsr.getString("agent").trim();
                      String brand = rsr.getString("brands").trim();

                      double plancur = rsr.getDouble("summa");
                      double planvol = rsr.getDouble("volume");
                      
                      Toode tt = new Toode(brand);
                      tt.setPlancur(plancur);
                      tt.setPlanvol(planvol);
                      Vector asjad = new Vector();
                      if(brandplans.get(age) != null){
                    	   asjad = (Vector)brandplans.get(age);
                      }
                      asjad.add(tt);
                      brandplans.put(age, asjad);
                      System.out.println("plaan " + tt.toString());
                      
                      }
                      rsr.close();
                      stmtr.close();
                      conr.close();

                      } catch (Exception e) {
                          getParent().showError(e);
                      }
        	  
    	  
                      String cmd ="SELECT  cus.emplid as agent,AnalyticGroupId as brand,round(SUM(lineamount),0) AS summa  ,round(SUM(sal.qty*inv.NetWeight),0)  as kaal " +
                       "  FROM axa2.custinvoicetrans sal INNER JOIN axa2.inventtable inv ON SUBSTR(NLS_LOWER(sal.itemid),1,20)=SUBSTR(NLS_LOWER(inv.itemid),1,20) " +
                       "   and SUBSTR(NLS_LOWER(inv.dataareaid),1,3)= SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) INNER JOIN axa2.custinvoicejour jou ON  " +
                       " SUBSTR(NLS_LOWER(sal.invoiceid),1,20) =   SUBSTR(NLS_LOWER(jou.invoiceid),1,20)  and SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) " + 
                       "  INNER JOIN axa2.custtable cus ON SUBSTR(NLS_LOWER(cus.accountnum),1,20) =SUBSTR(NLS_LOWER(jou.orderaccount),1,20)  and SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)  WHERE " + 
                	   " SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3)='zp' AND SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)='zp' AND SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)='zp' " +
                       " AND SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)='zp' AND {fn YEAR(sal.invoicedate)} ={fn YEAR(sysdate)} " +
                        " AND {fn MONTH(sal.invoicedate)} = {fn MONTH(sysdate)}  " +
                        "     GROUP BY cus.emplid,AnalyticGroupId order by cus.emplid,AnalyticGroupId";
                      //loeme k�ik myygid sisse 
    	 

    	      try {
    	    	  Connection conr = this.getNewConnection();
    	          Statement stmtr =  conr.createStatement();
    	          ResultSet rsr = stmtr.executeQuery(cmd);
    	          
    	                  while (rsr.next()) {
    	                	  String age = rsr.getString("agent").trim();
    	                      String brand = rsr.getString("brand").trim();
    	                      double donecur = rsr.getDouble("summa");
    	                      double donevol = rsr.getDouble("kaal");
    	                     
    	                    
    	                      Vector asjad = new Vector();
    	                      if(brandplans.get(age) != null){
    	                    	  ass = age;
    	                    	   asjad = (Vector)brandplans.get(age);
    	                    	   Enumeration enume = asjad.elements();
    	                    	   while (enume.hasMoreElements()){
    	                    		   Toode tt = (Toode)enume.nextElement();
    	                    		   if(tt.getID().equals(brand)){
    	                    			   tt.setDonecur(donecur);
    	                    			   tt.setDonevol(donevol);
    	                    		   }
    	                    		  // System.out.println("done " + tt.toString());
    	                    	   }
    	                    	  
    	                    	  // brandplans.put(age, asjad);
    	                      }
    	                    
    	                     
    	                  }
    	                  rsr.close();
    	                  stmtr.close();
    	                  conr.close();

    	                  } catch (Exception e) {
    	                      getParent().showError(e);
    	                  }

    	                  
      }
      
      
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
          
          String cmd2 =" SELECT sp as agent, c" + mm + " as volume ,t" + mm + " *1000  as summa from SRCADMIN30.\"vadyba" + yy + "\"  ";
          
        	  
         

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
                      String asi = "Pardavimai: " + msales + " Lt||";
                      if(dd > 6){
                    	  asi = asi + "Plano vykdymas/prognoz� (Lt):";
                          asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                      }
                    
                      asi = asi + "Pardavim� planas: " + psales + " Lt||";
                      
                    /*  asi = asi +"Pardavimai: " + mkaal + " Kg||";
                      if(dd > 6){
                    	  asi = asi + "Plano vykdumas/prognoz� (t):";
                          asi = asi + "     " +NF.format(tehtudkaalperc*100) + "% / " + NF.format(prognokaalperc*100)  + " %||";  
                      }
                      
                      asi = asi + "Pardavim� planas: " + pkaal + " Kg||";
                      */
                   /*   

                    	  
                    	  Pardavim� planas:
                    	  579.438 Lt

                    	  Plano vykdumas/prognoz� (t):
                    	  74,56% / 94,89%
                    	  Pardavimas planas:
                    	  102,32 T*/

                      
                        
                        String asi2 = "";                    
                        if(brandplans.get(agent.getID()) != null){
    	                	Vector vv =   (Vector)brandplans.get(agent.getID());
    	                	 Enumeration enume = vv.elements();
    	                	 while(enume.hasMoreElements()){
    	                		 Toode tt = (Toode)enume.nextElement();
    	                		 if(tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
    	                			asi2 = asi2 + ("Brand " + tt.getID() + "    " +
    	                					((int)(tt.getDonecur()/tt.getPlancur()*100))
    	                					+ "% Plan.: " +(int) tt.getPlancur() + " Lt  Parda.: " + (int)tt.getDonecur() + " Lt||");
    	                		 }
    	                	 }
    	                	  
    	                  }
                        
                        agent.setStart( Converter.enCodeEst(asi + "Aktyv�s klientai ") + (int)rs.getDouble("poode") + "|"
                        		+ asi2 );
                        
                        
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
    return "AxaZpienasMerc";
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
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
