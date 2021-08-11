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
import java.util.HashSet;
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

public class AxaZpienasExportDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
	
  public AxaZpienasExportDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    NF.setMaximumFractionDigits(2);
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
   coulist = "'PL','HU','UA','FR','BE','IT','DE','SP'";
  }
private String coulist = null;
  
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
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

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
                 	
					// Siin läheme reasisse rokkima ja vaatama kas se toode
					// läheb kaasa
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
							// mingi tähis
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

								// System.out.println( a + " " + prodID + " " +
								 //clGroup + " " + go);

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
		TreeMap maxCodes = null;
		//otsime Maximale eraldi 
		try {
			 maxCodes = getMaxcodes();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// System.out.println("max codes " + maxCodes.toString());
		
		TreeMap eans = null;
		//otsime Maximale eraldi 
		try {
			 eans = getEancodes();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		// System.out.println("eans codes " + eans.toString());
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("MAXIMA");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(0);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a).getStringCellValue());
				} catch (Exception e) {
					
				}
			}

			// System.out.println(sheet.getLastRowNum() + " max groups " + groups.toString());

			for (int i = 1; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "0";
			
				//if (row.getCell(1) != null) {

					try {
					
						prodID =  (row.getCell(1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						//e.printStackTrace();
						try{
							prodID = (row.getCell(1).getStringCellValue());
										
						}catch(Exception k ){
							k.printStackTrace();
						}
					}
				
					
					
                    if(!prodID.equals("0")){
                 	
					// Siin läheme reasisse rokkima ja vaatama kas se toode
					// läheb kaasa
					for (int a = 3; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						
						if (maxCodes != null && group != null &&  group.trim().length() > 0 &&
								maxCodes.get("A" +group.trim())!= null 
							) {
						
							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";
                          	String go = null;
						
							custID = maxCodes.get("A" +group.trim())+ "";
							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi tähis
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.trim().equals("1")) {
          
            
                           		if (custID != null && custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if(prodID.indexOf("E") != -1){
									prodID = prodID.substring(0,prodID.indexOf("E") );
						         int len  = prodID.length();
						         prodID = prodID.substring(0,1) + prodID.substring(2,len);
						          if(prodID.length() == 12){
						        	  prodID = prodID+"0";
						          }
						        }else if(prodID.indexOf(".") != -1) {
						        	prodID = prodID.substring(0,prodID.indexOf("."));
						        }
								//System.out.println("prodid " + prodID);
							
								if(eans != null && eans.get(prodID) != null){
									
									if (prodID != null	&& prodID.trim().length() > 0) {
										mline.setProdID(eans.get(prodID.trim()) + "");
									}
								//	  System.out.println("Telekas " + go + " " +  custID + " prodID " +  prodID + " " + mline.getProdID());
								
									result.add(mline);	
								}
								
							}
						}
					}
					   
                    }
				//}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("IKI");
			HSSFRow row = null;
			
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			

			 System.out.println(sheet.getLastRowNum() + " IKI  " );

			for (int i = 1; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";
				String custID = "";
	
				try {
					
					custID =  (int)(row.getCell(0)
							.getNumericCellValue()) + "";
				} catch (Exception e) {
					//e.printStackTrace();
					try{
						custID = (row.getCell(0).getStringCellValue());
									
					}catch(Exception k ){
						k.printStackTrace();
					}
				}
			
			
	try {
					
					prodID =  (row.getCell(2)
							.getNumericCellValue()) + "";
				} catch (Exception e) {
					//e.printStackTrace();
					try{
						prodID = (row.getCell(2).getStringCellValue());
									
					}catch(Exception k ){
						k.printStackTrace();
					}
				}
			
				if(prodID.indexOf("E") != -1){
					prodID = prodID.substring(0,prodID.indexOf("E") );
		         int len  = prodID.length();
		         prodID = prodID.substring(0,1) + prodID.substring(2,len);
		          if(prodID.length() == 12){
		        	  prodID = prodID+"0";
		          }
		        }else if(prodID.indexOf(".") != -1) {
		        	prodID = prodID.substring(0,prodID.indexOf("."));
		        }
				
					
                    if(!prodID.equals("0")){
                 	
						MatrixLine mline = new MatrixLine();
						
							
								if(eans != null && eans.get(prodID) != null && custID != null && custID.length() > 3){
									
									if (prodID != null	&& prodID.trim().length() > 0) {
										mline.setProdID(eans.get(prodID.trim()) + "");
									}
								//	System.out.println(" IKI prodID " +  prodID + " " + custID);
						
										mline.setCustID(custID);
								
									result.add(mline);	
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
         String sql = " SELECT (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) as custid, (accountnum) as klient,  '' AS clgroup,  " +
				 "  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid,  " +
				 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount)  " +
                 "  AS price FROM axa2.pricedisctable pd " +
                 "    left join axa2.custtable cus on SUBSTR(NLS_LOWER(cus.accountnum),1,20) = SUBSTR(NLS_LOWER(pd.accountrelation),1,20)  " + 
                 "    and SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(pd.DATAAREAID),1,3) where  " +
				 "  SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3) = 'zp'   and SUBSTR(NLS_LOWER(pd.DATAAREAID),1,3) = 'zp' " +    
				 "  and  pd.fromdate<SYSDATE and pd.todate>SYSDATE " +
				 "  and  pd.relation in (4,5)  " +
				 "  group by (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) " +
                 "  ,cus.accountnum, pd.itemrelation,pd.itemcode  order by  (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end),pd.itemrelation ";

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
                          if(klient.trim().indexOf("0751001") != -1 ||custID.trim().indexOf("0751001") != -1){
                        	  System.out.println("0751001" + " " + prodID + " " + custID + " " + discount);
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
 /*     try {

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
*/
       return dealerProds;
     }

  
  TreeMap agnames = null;
  
  public TreeMap getAgents(){

    String cmd = "select emplid as kood , name AS name  from axa2.empltable where dataareaid = 'zp' ";// +
 // " and title = 'Vadybininkas'";
 //  getFixedAssortmentData();
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
    Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    		
    	}
      
     }
    return agents;
  }
 
  

  
  public String getDateTimeStr(Calendar cal) {

		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		String hour = (cal.get(Calendar.HOUR_OF_DAY)) + "";
		String min = (cal.get(Calendar.MINUTE)) + "";
		String sec = (cal.get(Calendar.SECOND)) + "";
		
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		if (min.length() == 1) {
			min = "0" + min;
		}
		if (sec.length() == 1) {
			sec = "0" + sec;
		}
		
		return cal.get(Calendar.YEAR) + "-" + month + "-"+ day + " " + hour + ":" + min + ":" + sec;
	}
	public String getDateStr(Calendar cal) {

		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}
  
	private HashSet skaikodes = null;
  public TreeMap getCusts(){
	  skaikodes = new HashSet();
	  TreeMap res = new TreeMap();
	 // accountnum as klp ,ExtCodeValue 
      String ddd = "select accountnum ,ExtCodeValue as zp from axa2.custtable a , axa2.ExtCodeValueTable b " +
" where  a.dataareaid='klp' and  b.dataareaid='klp' and a.RecId= b.ExtCodeRelationRecId  and  b.ExtCodeId = 'ZP' and b.extCodeTableTableId=77";
      try{
    	  Statement stmu = this.getNewConnection().createStatement();
       	
          ResultSet rs1 = stmu.executeQuery(ddd);
         while(rs1.next()){
        String kp = rs1.getString("accountnum");
        String eg = rs1.getString("zp");
        skaikodes.add(kp);
        if(eg != null && eg.trim().length() >0 &&  kp != null && kp.trim().length() > 0){
       	 res.put(eg.trim(), kp.trim());
       	
        }
     	   
         }
        rs1.close();
        stmu.close();
    	  
    		  
      }catch(Exception e){
    	  
      }
      return res ;
  }
  
  
  
   
 
  public TreeMap getCustomers(Agent agent){
	  Converter cc = new Converter();
	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	  makeFixHash();
	  
	  
	  System.out.println("Alustan Kliendid");
 String cmd = "  SELECT cus.name,street as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
              " 0 AS debt, cus.linedisc ,   " +
              "   cus.country, cus.address, SalesPoolId  AS visit ,emplid AS agent ,cus.ordertaker_bbs AS agent2,  numofdays AS term, cus.blocked AS block ,  " + // SalesPollId as visit
			 " (select max (blocked) from axa2.custtable cc where  cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = cus.dataareaid ) " + 
			 "  as payerblock ,phone || ' '|| telefax || ' '|| cellularphone as info , linedisc as grupp, MINORDERAMOUNT_BBS as minord , " +
			"	transportroutecode as transa,'' as discount ,InventQtyRounding_bbs as samm , custsubgroupcode as tyyp , dimension  as osad, ShelfArea_bbs as sharea," +
            " ShelfPart_bbs As shshare ,FMICode_LT as pictemail ,AgreementEndDate_bbs as contrend  FROM axa2.custtable cus left outer JOIN axa2.paymterm  " +
			"  pay on SUBSTR(NLS_LOWER(cus.paymtermid),1,20)= SUBSTR(NLS_LOWER(pay.paymtermid),1,20) and SUBSTR(NLS_LOWER(pay.DATAAREAID),1,3) = SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3) and  pay.dataareaid='zp' " +
			"	 WHERE   (cus.dataareaid='zp' AND cus.blocked != 2 and  cus.country  in (" + coulist + ")) OR cus.accountnum in ('NAUJAS','OFISAS')  " ;
 
 String ccc = "select invoiceaccount from axa2.custtable where dataareaid='zp' and invoiceaccount is not null  group by invoiceaccount";
 
           String sql = "SELECT  a.ORDERACCOUNT AS klient,a.invoice AS numbe,co.duedate AS maksepaev,sum(co.amountmst) AS tasumata, " +
                 "   co.transdate AS kuupaev , round (sysdate - co.duedate,0) AS yle FROM axa2.custtransopen co  " +
				"    INNER JOIN axa2.custtrans a ON co.REFRECID=a.recid and  SUBSTR(NLS_LOWER(co.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(a.DATAAREAID),1,3)   "+
                "     WHERE co.dataareaid='zp' AND a.dataareaid='zp'	 " +
				"  group by a.invoice, a.ORDERACCOUNT,co.duedate, co.transdate  ORDER BY co.transdate,a.invoice, a.ORDERACCOUNT,co.duedate ";

           
           String salessql = "    SELECT  jou.orderaccount as klient,round(SUM(lineamount),0) AS summa,TO_CHAR(TRUNC(sal.invoicedate, 'MM'),'YYYYMM') as kuu " +  
        " FROM axa2.custinvoicetrans sal  INNER JOIN axa2.custinvoicejour jou ON  SUBSTR(NLS_LOWER(sal.invoiceid),1,20) =SUBSTR(NLS_LOWER(jou.invoiceid),1,20)  and " + 
       "  SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) " +
       " INNER JOIN axa2.custtable cus on cus.accountnum = jou.orderaccount and SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) WHERE " + 
	   " sal.dataareaid='zp' AND sal.invoicedate > sysdate -390  and cus.country in (" + coulist + ") GROUP BY jou.orderaccount,TO_CHAR(TRUNC(sal.invoicedate, 'MM'),'YYYYMM') " +
	   " ORDER BY jou.orderaccount,TO_CHAR(TRUNC(sal.invoicedate, 'MM'),'YYYYMM') " ;
               
           
           
           String sss = "SELECT  accountnum, sum(case when co.duedate<sysdate then amountmst else 0 end) as asi ," +
           		"sum(amountmst) as asi2,sum(case when DebtType ='4'  then amountmst else 0 end) as tara FROM axa2.custtransopen co " +
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
           



     	  System.out.println("Alustan Kliendid 2");
           
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap custDebt = new TreeMap();
    TreeMap taraDebt = new TreeMap();
    TreeMap totalDebt = new TreeMap();
    TreeMap custKlp = new TreeMap();
    Vector payers =  new Vector();
    HashMap salesres = new HashMap();
    try {
    	ResultSet rs1 ;
    	Statement stmr ;
    	try{
    		 stmr = this.getNewConnection().createStatement();
        	
             rs1 = stmr.executeQuery(salessql);
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim();
            	
            	int summa = (int)rs1.getDouble("summa");
            	String kuu = rs1.getString("kuu");
              if(salesres.get(klient) == null){
            	  salesres.put(klient,"   " + kuu + " : " + summa + " €" );
              }else{
            	  String asi = (String)salesres.get(klient);     	  
            	  salesres.put(klient,"   " + kuu + " : " + summa +" €|" + asi);
              }
            }
           rs1.close();
           stmr.close();
    	}catch(Exception s){
    		
    	}
    	    	
    	

  	  System.out.println("Alustan Kliendid 3");
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
       double tara = rs1.getDouble("tara");
       if(tara != 0){
    	   taraDebt.put(ee, tara + "");
       }
          
        }
       rs1.close();
       stmu.close();
    

	  System.out.println("Alustan Kliendid 4");
    	
        stmr = this.getNewConnection().createStatement();
    	 rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
    	

 	  System.out.println("Alustan Kliendid 5");
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
       	
        Vector grupp = (Vector)fixGroups.get( accountnum);
          if(grupp != null  ){
          	cust.setFixSortCode(grupp);
          	//System.out.println("Tere panen gruppi " + accountnum+ " "+ grupp.toString());
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
        	cust.setInfo("CHART Sales :|Month and Year |Sales |" + salesres.get(accountnum) );
        }
        
        String country = rs.getString("country").trim();

        if(country.equals("PL")){
        	country = "poland";
        }
        
        if(country.equals("HU")){
        	country = "hungary";
        }
        
        String aadr = rs.getString("address");
        if (aadr != null) {
        	int ss = aadr.toLowerCase().indexOf(country);
        	if(ss != -1){
        		aadr = aadr.substring(0,ss);
        	}
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),country).
        		  replaceAll("\\r\\n|\\r|\\n", " "));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
        	
          cust.setVisit(Converter.getVisitDayLetter(visit.trim()));
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
         // cust.setCity("Route number: " +transa.trim());
          
        }
        String rounding = rs.getString("samm");
        if (rounding != null && rounding.trim().equals("2")) {
          //cust.setMakeAmountRounding(true);        
        }
        
        if(aled.get(cust.getID())!= null){
        	cust.setDiscount(Double.parseDouble((String)aled.get(cust.getID())));
        }
        
        String tyyp = rs.getString("tyyp");
        if (tyyp != null && tyyp.trim().length() > 0) {
        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("Type :" + tyyp.trim());
        	}else{
        		cust.setCity(cust.getCity() + "| Type :" + tyyp.trim());
        	}
          //cust.setMakeAmountRounding(true);        
        }
        
        
        
        
        
        if(cust.getID().equals("NAUJAS") || cust.getID().equals("OFISAS") ){
        	   Enumeration enume = getParent().getAgentsHash().elements();
               while (enume.hasMoreElements()) {
                 Agent a = (Agent) enume.nextElement();
                   cust.addAgent(a);
               }
               cust.setName("New Customer");
        }
             
        String pictemail = rs.getString("pictemail");
        
        if(pictemail != null && pictemail.trim().length() > 0 ){
        	cust.setPictEmail(pictemail.trim());
        }
        
       java.sql.Date contrend = rs.getDate("contrend");
        String add = "";
        
        if(contrend != null && contrend.toString().trim().length() > 0 && contrend.toString().indexOf("1900") == -1 ){
        	 
        		  add =  "Contract over :" + contrend.toString().substring(0,10);  
        	 
        		cust.setContractOver(contrend.getTime() + "");	 //PANEME LÕPU KUUPÄEVA KÜLGE
        	
        }
        
        
        
        if(add.length() > 0){
        	   if(cust.getCity()== null || cust.getCity().length() == 0 ){
           		cust.setCity(add);
           	}else{
           		cust.setCity(cust.getCity() + "|" + add);
           	}	
        }
     
        
        
        
        cc.addQuestiosToCustomer(cust,kyssad);
     
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }

      
      
      

    
      
      
      
 	  System.out.println("Alustan Kliendid 1");
      try{
          
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
         	  if(custMap.get(klient) != null){
         		  sb2.append(klient+";"+ nimi + ";" + synna.getTime() + "; ;" + phone + " " +  cell + ";" + email +  "\n");
              }
              }
             rs1.close();  
             stm.close();
             kkk.close();
             if(sb2.length() > 2){
                		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
                	}
             

         
      }catch (Exception r){
   	   System.out.println(cmd2);
   	   r.printStackTrace();
      }

        
      
	  System.out.println("Alustan Kliendid 6");
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        
                        String number = rs.getString("numbe").trim();
                        if(number != null && number.trim().length() > 0){
                            debt.setDoknr(number.replace(';',','));
                         }else{
                        	debt.setDoknr("NoNumber");
                        }
                        
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        this.debt.addElement(debt); 	
                      }

                	  System.out.println("Alustan Kliendid 7 ");

    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  //
  
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host2"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
  private Connection getNewConnection3() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
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
  
  public TreeMap getEancodes() throws SQLException {
	  Connection con = getNewConnection();
	 
	    TreeMap   tm = new TreeMap();
	    String    sql = " SELECT inv.itemid AS kood, bar.itembarcode AS ean  FROM axa2.inventtable inv " +
	    "  inner join axa2.inventitembarcode bar on bar.dataareaid = 'zp' and bar.useforprinting =" +
	    " 1 and bar.itemid=inv.itemid  WHERE   inv.dataareaid='zp'  and  bar.dataareaid='zp'  " +
	    "  and (inv.itemgroupid like 'Z%' or inv.itemgroupid like 'S%') and inv.Assortment = 1";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("kood").trim();
	      String maks = rs.getString("ean");
	      
	      if(maks != null && maks.trim().length() > 0){
	    	  tm.put( maks.trim(),liik);
	      }
	      
	    }
	    rs.close();
	    st.close();
	    con.close();

	    return tm;
  }
  
  
  public TreeMap getMaxcodes() throws SQLException {
	  Connection con = getNewConnection();
	 
	    TreeMap   tm = new TreeMap();
	    String    sql = " SELECT accountnum as kood,externalcode as maksima FROM axa2.custtable cus " +
				" WHERE   cus.dataareaid='zp' and name like '%axima%' ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("kood").trim();
	      String maks = rs.getString("maksima");
	      
	      if(maks != null && maks.trim().length() > 0){
	    	  tm.put("A" +maks.trim().toUpperCase(),liik);
	      }
	      
	    }
	    rs.close();
	    st.close();
	    con.close();

	    return tm;
  }

  public TreeMap getProducts(Agent agent){

    String cmd = " SELECT inv.itemid, inv.itemname AS nimetus,ven.ExternalItemTxt  as nimetus2,  sui.qty AS kastis,'' AS alusel, disc.amount AS price " +
	"  , bar.itembarcode AS ean ,inv.PRODUCTLINEID AS grupp2, inv.ITEMANAGROUPCODE   AS grupp, gr.ANALYTICGROUPID as grid,mod.unitid As unit, 21 as vat , " +
    " mod.LowestQty  as samm ,prodsorting as sorting   " +
    " FROM axa2.inventtable inv " +
    "  inner join axa2.CustVendExternalItem ven ON ven.itemid =inv.itemid and SUBSTR(NLS_LOWER(inv.itemid),1,20)= SUBSTR(NLS_LOWER(ven.itemid),1,20) and" + 
	" SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(ven.DATAAREAID),1,3) and ven.ModuleType=6  " +
    "   left  JOIN axa2.pricedisctable disc ON SUBSTR(NLS_LOWER(inv.itemid),1,20)= SUBSTR(NLS_LOWER(disc.itemrelation),1,20) and " +
    " SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(disc.DATAAREAID),1,3)and disc.amount != 0  " +
	"   and (disc.todate>sysdate  or   {fn YEAR(disc.todate)} =1900)  and disc.accountcode='2' and disc.currency = 'EUR' " +
	"   left  JOIN axa2.suitetable sui ON  SUBSTR(NLS_LOWER(inv.itemid),1,20)= SUBSTR(NLS_LOWER(sui.suitelineitem),1,20) and " +
    " SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(sui.DATAAREAID),1,3) and sui.type=0  AND sui.dataareaid='zp' and   sui.qty > 1  " +
	"  left JOIN axa2.inventtablemodule mod ON mod.itemid = inv.itemid and SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(mod.DATAAREAID),1,3) " +
	"   left join axa2.inventitembarcode bar on bar.dataareaid = 'zp' and bar.useforprinting = 1 and bar.itemid=inv.itemid and  " +
    "   SUBSTR(NLS_LOWER(bar.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3) " +
	"   left join axa2.inventanalyticgroup gr on gr.dataareaid = 'zp' and gr.ANALYTICGROUPID=inv.ANALYTICGROUPID and  " +
    "     SUBSTR(NLS_LOWER(gr.DATAAREAID),1,3)=SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3) " +
	"   WHERE   moduletype=2 and blocked = 0 and  inv.dataareaid='zp'   and disc.dataareaid='zp'   and mod.dataareaid='zp'  " +
	"   and (inv.itemgroupid like 'Z%' or inv.itemgroupid like 'S%') and inv.Assortment = 1 and ven.CustVendRelation='PL' order by inv.itemid";
    
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
        String nimetus = Converter.enCodeEst( rs.getString("nimetus2").trim() + " " + kood) ;
   
  
        prod.setInCases(rs.getDouble("kastis"));
       // prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));
        prod.setType("ZP");
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
      // prod.setStatusTop("1");
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
    
System.out.println("CustProdlisti tegemine ");
 int counter = 1;
   String cmd = " SELECT /*+ leading(tra,jou) use_hash(tra,jou) */orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate " +
     "  FROM axa2.custinvoicetrans  tra INNER JOIN axa2.custinvoicejour jou ON " +
     "  SUBSTR(NLS_LOWER(tra.invoiceid),1,20)=  SUBSTR(NLS_LOWER(jou.invoiceid),1,20) and  SUBSTR(NLS_LOWER(tra.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3) " +
     " and SUBSTR(NLS_LOWER(tra.SALESID),1,20) = SUBSTR(NLS_LOWER(jou.SALESID),1,20)  WHERE  jou.invoicedate > sysdate-60 AND SUBSTR(NLS_LOWER(tra.DATAAREAID),1,3) = ('zp')  AND SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)  = ('zp')  " +
     "  GROUP BY orderaccount,tra.itemid,tra.name ,jou.invoicedate ,tra.qty  Order by orderaccount,tra.name,jou.invoicedate desc " ;
  
   if (isDebug()) System.out.println(cmd);

    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      counter++;
      while(rs.next()){
    	
    	  
        String kliendikood = rs.getString("customer").trim();

        String tootekood = rs.getString("itemid").trim();
        if(!custs.keySet().contains(kliendikood+ tootekood)){
		           	
		       
		        double kogus = rs.getDouble("qty");
		        String lastdate = rs.getString("lastdate").substring(0,10);
		        lastdate = lastdate.substring(0,10);
		        custs.put(kliendikood + tootekood,lastdate); 
		        lastdate = lastdate.substring(5,10); 
		                 
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,0,0,0," ");
		        
		        custProds.add(cp);
        }
        
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
  
  System.out.println("CustProdlisti tegemine TEHTUD ");
    return custProds;
  }
 
  
  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  

    public void setBulkSales(Map agents){
    
      
      String cmd ="SELECT /*+ leading(cus,jou,sal,inv) use_hash(jou,sal,inv) */ cus.emplid as agent,round(SUM(lineamount),0) AS summa , round(SUM(sal.qty*inv.NetWeight),0)  as kaal ,count (distinct orderaccount)  as poode " +
      "   FROM axa2.custinvoicetrans sal INNER JOIN axa2.inventtable inv ON SUBSTR(NLS_LOWER(sal.itemid),1,20)=SUBSTR(NLS_LOWER(inv.itemid),1,20) " +
      "    and SUBSTR(NLS_LOWER(inv.dataareaid),1,3)= SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) INNER JOIN axa2.custinvoicejour jou ON  " +
      "  SUBSTR(NLS_LOWER(sal.invoiceid),1,20) =   SUBSTR(NLS_LOWER(jou.invoiceid),1,20)  and SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3) " +
      "   INNER JOIN axa2.custtable cus ON SUBSTR(NLS_LOWER(cus.accountnum),1,20) =SUBSTR(NLS_LOWER(jou.orderaccount),1,20)  and SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)= SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)  WHERE " +
	  " SUBSTR(NLS_LOWER(sal.DATAAREAID),1,3)='zp' AND SUBSTR(NLS_LOWER(inv.DATAAREAID),1,3)='zp' AND SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3)='zp' " +
      " AND SUBSTR(NLS_LOWER(jou.DATAAREAID),1,3)='zp' AND {fn YEAR(sal.invoicedate)} ={fn YEAR(sysdate)} " +
      "   AND {fn MONTH(sal.invoicedate)} = {fn MONTH(sysdate)}  " +
      "  GROUP BY cus.emplid";
      
      
     
      System.out.println("Müügid 2"); 

         if (isDebug()) System.out.println(cmd);
            try {
            	
            	
                System.out.println("Müügid 3"); 
            	Connection cone = this.getNewConnection();
                Statement stmt =  cone.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                	  double msales = (int)rs.getDouble("summa");
                	  double mkaal = (int)rs.getDouble("kaal");
                	  
                     String asi = "SalesCurrent Month EUR : " + msales + " ||";
                      agent.setStart(asi);   
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
    return "AxaZpienas";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private HashMap brandplans = null; 
  private HashMap typeplans = null; 
  
  private HashMap typebrand = null; 
  

  public String getPilotDayData(Agent agent){

  if(brandplans == null){
	  brandplans = new HashMap();
	  typeplans = new HashMap();
	  typebrand = new HashMap();
	  
	  int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
      int yy = Calendar.getInstance().get(Calendar.YEAR);
      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	  
    
      String cmd2 =" SELECT sp as agent ,brands, c" + mm + " as summa ,t" + mm + "*1000   as volume from SRCADMIN61.vadyba" + yy + " order by sp,brands ";
  // loene kõik plaanid sisse 
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
                  //loeme kõik myygid sisse 
	 

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

	                  
	                //  System.out.println(((Toode)((Vector)brandplans.get(ass)).elementAt(0)).toString());
	                  
	                  
  }
	  

	       String sql2 =  "SELECT sal.ordernum AS arve, sal.createddate AS kuupaev,departname AS klient, count(rid.itemid) AS summa  " +
	    " FROM axa2.MAXIMSALESTABLE sal " +
	    " INNER JOIN axa2.MAXIMSALESLINE rid ON sal.ordernum=rid.ordernum  " +
 " WHERE  sal.departmen ='" + agent.getID() + "' AND " +
	  "   sal.CREATEDDATE > sysdate-1  GROUP BY sal.ordernum, sal.createddate , departname,sal.dlvdate ORDER BY sal.dlvdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Priimtø uþsakymø sàraðas:\n\n");

	      try {
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  eilutës: " + (int)rs2.getDouble("summa") + "\n");
	                  }
	                  sb.append("\n");

	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }

	                  if(brandplans.get(agent.getID()) != null){
	                	Vector vv =   (Vector)brandplans.get(agent.getID());
	                	 Enumeration enume = vv.elements();
	                	 while(enume.hasMoreElements()){
	                		 Toode tt = (Toode)enume.nextElement();
	                		 if(tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
	                			 sb.append("Brand " + tt.getID() + "\n     plan: " +(int) tt.getPlancur() + " lt   done: " + (int)tt.getDonecur() + " lt\n\n");
	                		 }
	                	 }
	                	  
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
  private String url3 = "jdbc:odbc:skaita";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
