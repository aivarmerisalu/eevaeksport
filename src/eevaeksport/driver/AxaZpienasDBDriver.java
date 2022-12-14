package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
import org.json.JSONException;

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

public class AxaZpienasDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter, FixReporter  {
  public AxaZpienasDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    NF.setMaximumFractionDigits(2);
    NF.setMaximumFractionDigits(2);
    NF.setGroupingUsed(false);
    NF.setMinimumFractionDigits(2);
     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
 
    if("false".equals(getParent().getProps().getProperty("sendkpdata"))){
    	sendKPdata = false;
    }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
  
  private boolean sendKPdata = true;
  /**
   *
   */
  
  
  private void saveAssetFile() {
	org.json.JSONArray data = Converter.getAssetsJson(user, pass, rcompany);
//	System.out.println(data.toString().substring(0, 3000));
	   String sisu = "InventoryCode;InventoryName;FactoryId;NFC;CustomerCode;CustomerName"
	     		+ ";LastCheck;LastCheckManagerCode;LastCheckManagerName;Status;PictureLink;PictureDate\n";
	try {
	
	 Connection con = this.getNewConnectionAsset();
   

     
	
	  
	  int i = data.length();
	 for(int a = 0; a < i;a++) {
		 String nimi = null;
		 String knimi = null;
		 String kkood = null;
		 String kood = null;
		 String mnimi = null;
		 String mkood = null;
		 String nfc = null;
		 String fnimi = null;
		 String dnimi = null;
		 Long viimaneKin = 0l;
		 String fId = null;
		 String status = null;
		 Long fcreated = 0l;
		 
		 

		 
	 try {
		 org.json.JSONObject asset =(org.json.JSONObject) data.get(a);
		nimi = ((String) asset.get("name")).replaceAll("\\r\\n|\\r|\\n", " ");
		kood = (String) asset.get("code");
		

		
	try {
		org.json.JSONArray fileArr =(org.json.JSONArray) asset.get("files");
		if(fileArr != null) {
			int e = fileArr.length();
			 for(int u = 0; u < e;u++) {
				 org.json.JSONObject fileO =(org.json.JSONObject) fileArr.get(u);
				 fnimi = (String) fileO.get("url");
				 fcreated = (Long) fileO.get("created");
				 }
		}
	} catch (JSONException e) {
	}	
	
	try {
		viimaneKin = (Long) asset.get("lastConfirmed");
		//System.out.println(Converter.getDDsMMMsYYYYString(new Date(viimaneKin)));
	} catch (JSONException e) {
	}	
	
	
	try {
		status = (String) asset.get("status");
	}catch (JSONException e) {
	}	
	
	
	
	
	try {
		fId = (String) asset.get("factoryId");
	} catch (JSONException e) {
	}	
	
	
	
	try {
		org.json.JSONArray dims =(org.json.JSONArray) asset.get("dimensions");
		if(dims != null) {
			int e = dims.length();
			 for(int u = 0; u < e;u++) {
				 org.json.JSONObject dimO =(org.json.JSONObject) dims.get(u);
				 dnimi = (String) dimO.get("name");
				}
		}
	} catch (JSONException e) {
	}	
		
		
		try {
			nfc = (String) asset.get("nfcTag");
		} catch (JSONException e) {
		}	
		
		
		
		try {
			 org.json.JSONObject manager =(org.json.JSONObject) asset.get("changeManager");
			 if(manager != null) {
				 mnimi = ((String) manager.get("name")).replaceAll("\\r\\n|\\r|\\n", " ");
				 mkood = (String) manager.get("code");
			 }
			
		} catch (JSONException e) {
		}	
		
		
		try {
			
			 org.json.JSONObject customer =(org.json.JSONObject) asset.get("customer");
			 if(customer != null) {
				 knimi = ((String) customer.get("name")).replaceAll("\\r\\n|\\r|\\n", " ");
				 kkood = (String) customer.get("code");

			 }
		} catch (JSONException e) {
		}	

		
		//"InventoryCode;InventoryName;FactoryId;NFC;CustomerCode;CustomerName"
		//+ ";LastCheck;LastCheckManagerCode;LastCheckManagerName;Status;PictureLink;PictureDate\n";
		
		sisu = sisu + kood + ";" + nimi + ";" + nonull(fId) + ";" + nonull(nfc) + ";" + nonull(kkood) + ";" + nonull(knimi) + ";" + lnonull(viimaneKin)
		+ ";" + nonull(mkood) + ";" + nonull(mnimi) + ";" + nonull(status) + ";" + nonull(fnimi) + ";" + lnonull(fcreated);
		sisu = sisu + "\n";
		
		String sql = " INSERT INTO merisoftassets VALUES('" + kood + "','" + nimi + "', '" + nonull(fId) + "', '" + nonull(nfc) + "', '" +nonull(kkood)+ "' , "
				+ "'" + nonull(knimi)+ "',?,'"  + nonull(mkood) +  "','" + nonull(mnimi) +  "','" + nonull(status) + "',?,?,cast(cast(getdate() as date) as datetime))";

		PreparedStatement st = con.prepareStatement(sql);
		
		 st.setTimestamp(1, new Timestamp(viimaneKin));
		 System.out.println(fnimi);
		 st.setString(2, nonull(fnimi) );
		 st.setTimestamp(3, new Timestamp(fcreated));
	   	 st.executeUpdate();
	   
		//	System.exit(0);
		
	} catch (JSONException e) {
		e.printStackTrace();
	}
		//System.out.println(kood + " ja " + nimi + " ja " +  knimi + " ja " + kkood);
		//System.out.println(  " ja " + nfc +   " ja " +  mnimi + " ja " + mkood);
		//System.out.println( dnimi +" "+ kood);
	 }
	 
	//  Calendar cal = ;
	 con.close();
		
		}catch(Exception e) {
			
			e.printStackTrace();
		}
	
	 Converter.saveFile("inventorydata/Inventorydata"+  Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".txt", sisu);
	 System.exit(0);
	
}
  private String lnonull(Long a) {
	   if(a == null) {
		   return " ";
		   
	   }
	   Calendar cal = Calendar.getInstance();
	   cal.setTimeInMillis(a);
	   return Converter.getDDpMMpYYYYpString(cal);
  }
  
  
  
  private String nonull(String a) {
	   if(a == null) {
		   return " ";
		   
	   }
	   return a;
  }
  

  
  
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	  public String getSaveDebtInfo() {
		  String dd = getParent().getProps().getProperty("days");
			Integer days = 1;
			if(dd != null) {
				try {
					days = Integer.parseInt(dd);
				}catch(Exception e) {
					e.printStackTrace();
				}
				
			}
	    	  Calendar alg = Calendar.getInstance();
	   	    alg.add(Calendar.DATE, -days);
   	    Converter.makeWebFilesList(alg.getTime(), null , rcompany,  user ,  pass, "C:/mobilejavaklp/excel/files/", "zp");
   	    System.exit(0);
   	  
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
         String sql = " SELECT (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) as custid, (accountnum) as klient,  '' AS clgroup, " + 
				 "  (case when pd.itemcode = 0 then pd.itemrelation else ''end) AS prodid, " +  
				 "  (case when pd.itemcode = 1 then pd.itemrelation else ''end) AS prodgroup,max(percent1) AS discount,max(amount)  " +
                 "  AS price FROM pricedisctable pd " +
                 "   left join custtable cus on cus.accountnum = pd.accountrelation  " +
                 "    and cus.DATAAREAID=pd.DATAAREAID where  " +
				 "  cus.DATAAREAID = 'zp'   and pd.DATAAREAID = 'zp'   " +
				 "  and  pd.fromdate<getdate() and pd.todate>getdate()" +
				 "  and  pd.relation in (4,5)  " +
				 "  group by (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end) " +
                 "  ,cus.accountnum, pd.itemrelation,pd.itemcode  order by " +
				 "  (case when  cus.invoiceaccount = '' then cus.accountnum else cus.invoiceaccount end),pd.itemrelation ";

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
				  "  pricedisctable pd where   pd.fromdate<getdate()+3 and pd.todate>=getdate() and pd.accountcode=1 and percent1 > 0 and pd.dataareaid ='zp' " +
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
	  getKLPDebt();
     return this.debt;
   }
  
  
  
  public Vector getKLPDebt() throws SQLException{
      Vector list = new Vector();
      String sql = " select  CustomerCode AS klient, billcode AS number,billissuedate+paymenttime AS kuupaev, " + 
		" (case when OperacijosTipoId =64 then -1 else 1 end)*billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime ,getdate())as yle from  " +
	 " (  SELECT     OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
         "             (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0),  " +
          "            BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data, BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma),  " +
          "            PaidAmount = CONVERT(Money, op.ApmoketaSuma), CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), " +
          "             Employeecode = CONVERT(varchar(35), masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), " + 
          "            op.Valiuta), DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
          "            CustomerCode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
          "       FROM         Operacijos AS op INNER JOIN " +
          "            Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
          "            Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
          "            PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
          "            MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
          "            Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
          
          "            MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
          "            OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId " +
         " WHERE     0 = 1 AND op.Kilme = 35 " +
 " UNION ALL " +
" SELECT     OrderAsID = CONVERT(varchar(20), CASE WHEN op.OperacijosTipoId = 50 THEN op2.KilmeInfo ELSE NULL END), DeliveredOrderAsID = CONVERT(varchar(20),  " +
  "                    op.OperacijosId), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0), BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data,  " +
   "                   BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma), PaidAmount = CONVERT(Money, op.ApmoketaSuma),  " +
    "                  CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), Employeecode = CONVERT(varchar(35),  " +
     "                 masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta),  " +
      "                DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
       "               customercode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
"FROM         Operacijos AS op INNER JOIN " +
 "                     Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
  "                    Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
   "                   PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
    "                  MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
     "                 Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
      "                MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
       "               OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId INNER JOIN " +
        "              RysioOperacijos AS rop ON op.OperacijosId = rop.OperacijosId INNER JOIN " +
         "             Operacijos AS op2 ON rop.OperacijosId2 = op2.OperacijosId " +
" WHERE     op.OperacijosTipoId IN (50, 64) AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 ) " +
" d  where billamount-paidamount>0 and " +
 "         billissuedate > getdate()-600  order by CustomerCode,billissuedate+paymenttime";

      Connection con = this.getNewConnectionSkaita();
    PreparedStatement  stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
    	  try{
    		    Debt debt = new Debt();
    		    String kliendikood = rs.getString("klient").trim();
    		    
    		    if(this.klpMap.get(kliendikood) != null){
    		    	  kliendikood = (String)klpMap.get(kliendikood);
    		    }
    	          	 
    	        debt.setCustID(kliendikood);
    	     
    	        String dok = "KLP-" + rs.getString("number");
    	        if(dok != null){
    	        	 dok = dok.trim();
    	    	        if(dok.length() >5){
    	    	        	dok = dok.substring(4);
    	    	        }
    	    	        debt.setDoknr(dok);   	    	        
    	    	        debt.setDuedate(rs.getDate("kuupaev"));
    	    	        debt.setSum(rs.getDouble("summa"));       
    	    	        debt.setDue(rs.getString("yle")); 	    	        
    	    	        this.debt.addElement(debt);	
    	        }
    	       
    	  }catch (Exception e){
    		  e.printStackTrace();
    	  }
    
        }
      rs.close();
      stmt.close();
      con.close();
      System.out.println("debtlist size " + list.size());
      return list;
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

        String  sql = "SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu FROM  INVENTSUM summ " +
               "  INNER JOIN  INVENTDIM dim ON summ.inventdimid =dim.inventdimid   and dim.DATAAREAID =summ.DATAAREAID  " +
               "  INNER JOIN  inventtablemodule inv ON inv.itemid=summ.itemid   and  inv.DATAAREAID =summ.DATAAREAID  " +
               "  WHERE summ.DATAAREAID ='zp'and dim.DATAAREAID ='zp' and " +
               "  inv.DATAAREAID ='zp' AND CLOSED = 0 AND  moduletype=0 " +
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

    String cmd = " select w.value as kood , w.name as name from  DIMATTRIBUTEHCMWORKER w   inner join HCMWORKER h on h.personnelnumber = w.value " + 
			" inner join custtable c on c.SALESMANAGER_BBS=h.recid or c.SALESTAKER_BBS=h.recid group by  w.value  , w.name";// +
 // " and title = 'Vadybininkas'";
 //  getFixedAssortmentData();
  
    
    Properties propse =  new Properties();
    try{
    	propse =  EksportFrame.loadProps("emails.txt");
  	  
    }catch(Exception e){
    	e.printStackTrace();
    }
    
	
	
    
    Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    StringBuffer sb2 = new StringBuffer();
    agnames  = new TreeMap();
    try {
   	 Connection con = getNewConnection();
     Statement   stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name + " " + agent.getID());
          agent.setIsPilot(true);


     		String agentAGENTS = props.getProperty(agent.getID());
   		if(agentAGENTS != null){
              StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
   	    while(stag.hasMoreTokens()){  	
   	    	agent.addDubbler(stag.nextToken());
            System.out.println("Lisan Duubleid " + agent.getID() + " " + agent.getDubbler().toString());
   	       }
            }

        agent.setLanguage("LIT");
        agent.setCountry("lt");
        agent.setPassWord("0000");
String email = " ";
        
        if(propse.get(agent.getID()) != null){
      	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
        }
        
        
        if(email.length() > 3){
      	  agent.setEmail(email);
        }
        if(email.length() > 3){
      	  agent.setActive(true);
        }
        
        
        
          
          
          agents.put(name + agent.getID(), agent);
          agents2.put(agent.getID(), agent);
          if(props.containsKey(agent.getID())){
        	  agnames.put(agent.getID(), name);
              sb2.append(agent.getID() + ";" + name + "\n");  
          }
          
        }
        rs.close();
        stmt.close();
        con.close();
        
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("WARE1");
    aa.setName("WARE1");
    agents.put("WARE1", aa);
    
    Agent aa1 = new Agent ("WARE2");
    aa1.setName("WARE2");
    agents.put("WARE2", aa1);
    Agent aa2 = new Agent ("WARE3");
    aa2.setName("WARE3");
    agents.put("WARE3", aa2);
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    		
    	}
      }
    

    if(rcompany != null){
    	Converter.sendAgentJson(agents, rcompany, false);
    }
  
   // setBulkSales(agents);
    
    return agents;
  }
 
  
  public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																	// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ " '' ," // receivedTime D,
				+ "?," // normalpr N,
				+ "?," // amoun N,
				+ "?," // grouptotal N,
				+ "?," // cattotal N,
				+ "?," // pertotcat I,
				+ "?," // percolcat I,
				+ "?," // perpalcat I,
				+ "?," // pershecat I,
				+ "?," // perrescat I,
				+ "?," // isaction L,
				+ "?," // ispartner L,
				+ "?," // isbrand L,
				+ "?," // isgroup L
				+ "?," // actionaeva N,
				+ "?," // actionbeva N,
				+ "?," // actiontota N,
				+ "?," // custeval N,
				+ "?," // custcomme C(200),
				+ "?," // comment C(200),
				+ "?," // custagent C(200),
				+ "?," // custagentname C(200),
				+ "?," // mustbe I,
				+ "?," // ideal I,
				+ "?," // resstr1 C(200),
				+ "?," // resstr2 C(200),
				+ "?," // resstr3 C(200),
				+ "?," // resint1 I,
				+ "?," // resint2 I,
				+ "?)"; // resint3 I,
				//+ "?)"; // country L

		try {
			System.out.println("jamxxx");
			Connection con = getNewConnection3();
			PreparedStatement ps = con.prepareStatement(sql);
			TreeMap custs = getCusts();
			
        
			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
				ps.setDouble(7, lw.getLine().getNormalPrice() * 100);
				ps.setDouble(8, lw.getLine().getAmount() * 100);
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
				ps.setDouble(20, lw.getLine().getAction1Eval());
				ps.setDouble(21, lw.getLine().getAction2Eval());
				ps.setDouble(22, lw.getLine().getActionTotalEval());
				ps.setDouble(23, lw.getLine().getCustEval());
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24, Converter.deCode(cc));

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25, Converter.deCode(cd));

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27, Converter.deCode(cf));
				ps.setInt(28, lw.getLine().getDealNr());
				ps.setInt(29, lw.getLine().getIdealNr());

				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
				
				ps.setString(30, cr1);

				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}

				Calendar cal = Calendar.getInstance();
				try {
					cal.setTimeInMillis(Long.parseLong(cr3));
				} catch (Exception e) {
					System.out.println("cr3 '" + cr3 + "'");

					// e.printStackTrace();

				}

				ps.setString(32, getDateStr(cal));
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
			//	System.out.println("ps asse");
				String skai = "";
				
				if(skaikodes.contains(lw.getLine().getCustID())){
					skai = lw.getLine().getCustID();
				}
				
				if(custs.get(lw.getLine().getCustID()) != null){
					skai = (String)custs.get(lw.getLine().getCustID());
				}
				
				String gru = "1";
				if(lw.getLine().isGroup()) {
					gru = "2";
				}
				int leng = lw.getLine().getCustID().length();
				String sb =  wr.getInfo().getAgentID() + ";" + lw.getLine().getCustID()+ ";"
				+ skai +  ";" +Converter.deCode(lw.getLine().getCustName().substring(0,lw.getLine().getCustName().length() - leng)) +
				   ";"  ;	
			     sb = sb + getDateTimeStr(cal) + ";" + cr2 + ";" + Converter.deCode(cc) + ";" +  Converter.deCode(cd); 
			     sb = sb + ";" + lw.getLine().getAction1Eval() + ";" + lw.getLine().getAction2Eval();//long,latt
			     sb = sb + ";" + gru ;//long,latt
				System.out.println("ps asse" + sb);
				
				if(cr1.equals("INVSCAN")){
					makeCSV(sb , cr2 + "-"+ cal.get(Calendar.MILLISECOND));
				}else{
					ps.executeUpdate();	
				}
				
			}

			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  private void makeCSV(String sb, String scan){
	  StringBuffer sb2 = new StringBuffer();
	  
	  sb2.append("agent;customerIDAXA;customerIDSKAITA;customerName;timestamp;scanned;other;comment;longitude;lattitude\n");
	  sb2.append(sb);
	  
	  if(sb2.length() > 2){
		  if(scan.toLowerCase().startsWith("klp")){
			  try{
		    		 Converter.saveFileInUTF8("klpscan\\"+ scan + ".csv", sb2);
		    	}catch (Exception e){
		    		
		    	}  
		  }else{
			  try{
		    		 Converter.saveFileInUTF8("zpscan\\"+ scan + ".csv", sb2);
		    	}catch (Exception e){
		    	} 
		  }
     }
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
      String ddd = "select accountnum ,ExtCodeValue as zp from  custtable a ,  ExtCodeValueTable b " +
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
  
  public TreeMap getAgents2() {
		return agents2;
	}
  
  private  TreeMap agents2 = new TreeMap(); 
  
  
  public TreeMap getKLPCustomers(){
	  
      String cmd = "SELECT CONVERT(Int, part.PartneriaiId) AS CustomerASId, CONVERT(varchar(20), part.Kodas) AS kood, CONVERT(varchar(40), part.Pavad) AS nimi,   " +
                 "   part.regnr AS payer ,  rtrim(adr.Adresas)  + case when adr.Indeksas IS NULL then '' else ' ' + rtrim(adr.Indeksas)end + ' ' + " +
                 " case when adr.Miestas IS NULL then '' else ' ' + rtrim(adr.Miestas)end AS Address,part.pvmnr as vatno, " +
                 "    CONVERT(char(1), 'N') AS Blocked, CONVERT(char(1), 'Y') AS Valid, CONVERT(Int, ROUND(part.Kreditas, 0)) AS CreditLimit, CONVERT(SmallInt, msal.Terminas)  " +
                 "    AS paym,  CONVERT(nvarchar(30),  " +
                 "    adr.Miestas) AS City, CONVERT(nvarchar(40), LTRIM(ats.Vardas + ' ' + ats.Pavarde)) AS ContactName, CONVERT(nvarchar(30), ats.Pareigos) AS ContactTitle, " +  
                 "    CONVERT(varchar(50), adr.Tel1) AS CustPhone , " +
                 "   CONVERT(Money, ISNULL  ((SELECT     SUM(ROUND((op.Suma - op.ApmoketaSuma) * op.Kursas, 2) * (CASE pot.PartOperacijosTipas WHEN 1 THEN 1 ELSE - 1 END)) AS Expr1   " +
                 "            FROM         dbo.Operacijos AS op INNER JOIN  " +
                 "                                  dbo.PirkimuOpTipai AS pot ON op.OperacijosTipoId = pot.OperacijosTipoId INNER JOIN  " +
                 "                                  dbo.OperacijosPartneriai AS opp ON op.OperacijosId = opp.OperacijosId  " +
                 "            WHERE     (pot.PartOperacijosTipas IN (1, 3, 12)) AND (opp.spec = 0) and op.Data+CONVERT(SmallInt, msal.Terminas) < getdate() AND (opp.PartneriaiId = part.PartneriaiId) AND " + 
				"		 	  (op.Suma > op.ApmoketaSuma)), 0)) as debt  ,masm.kodas as agent	, pl.padalinys as agent2, " +
				"			  (select  rtrim(uu.Adresas)  + case when uu.Indeksas IS NULL then '' else ' ' + rtrim(uu.Indeksas)end + ' ' + rtrim(uu.Miestas) from Adresai uu " +
				"INNER JOIN Partneriai oo  ON oo.AdresoId = uu.AdresoId  where oo.PartneriaiId=partpa.SumokaPartneriaiID  ) as jurid  , apr.Pavad  as aletext , sut.NuolaiduAprasymaiId  as hinnak , kain.NuolaiduKainininkaiId  as hinnak2 " +
          " FROM         dbo.Partneriai AS part INNER JOIN  " +
          "                     dbo.PartneriaiPa AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN  " +
           "               dbo.MokejimoSalygos AS msal ON partpa.MokejimoSalygosID = msal.MokejimoSalygosID INNER JOIN " +  
            "      dbo.MokejimoBudai AS mbud ON partpa.MokejimoBudaiID = mbud.MokejimoBudaiID INNER JOIN  " +
             "     dbo.PartneriuKategorijos AS pkat ON partpa.PartneriuKategorijosId = pkat.PartneriuKategorijosId INNER JOIN  " +
              "     dbo.NuolaiduKainininkai AS kain ON pkat.NuolaiduKainininkaiId = kain.NuolaiduKainininkaiId INNER JOIN  " +
		 		 "     dbo.Mokesciai AS mok ON partpa.MokesciaiId = mok.MokesciaiId INNER JOIN " + 
                "     dbo.Adresai AS adr ON part.AdresoId = adr.AdresoId INNER JOIN  " +
                "     dbo.Atstovai AS ats ON part.AtstovaiId = ats.AtstovaiId INNER JOIN  " +
                "     dbo.PL_PARTNERIAI AS pl ON part.PartneriaiId = pl.PARTNERIAIID INNER JOIN          " +           
                "     dbo.MusuAsmenys AS masm ON part.MusuAsmenysId1 = masm.MusuAsmenysId INNER JOIN  " +
                "     dbo.PL_MUSUASMENYS AS plmasm ON masm.MusuAsmenysId = plmasm.MUSUASMENYSID LEFT JOIN " + 
                "     dbo.NuolaiduSutartys AS sut ON part.PartneriaiId = sut.PartneriaiId and " +
				  "     (getdate() between   cast(sut.DataNuo_Int as datetime) and  cast(sut.DataIki_Int as datetime) or (sut.DataNuo_Int = 0 and sut.DataIki_Int = 999999)) LEFT OUTER JOIN " +
				  "     dbo.NuolaiduAprasymai AS apr ON sut.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId LEFT OUTER JOIN " +
                "     dbo.Sandeliai AS sand ON plmasm.Mobil_Sand = sand.SandeliaiId LEFT OUTER JOIN  " +
                "     dbo.PL_MUSUASMENYS AS plmasm2 ON part.MusuAsmenysId2 = plmasm2.MUSUASMENYSID CROSS JOIN  " +
                "     dbo.FirmosAtributai AS fa  " +
   " WHERE      pl.padalinys is not null ";
	TreeMap customers = new TreeMap();
      HashMap custMap = new HashMap();
try{
      Connection con2 = this.getNewConnectionSkaita();
		 
    PreparedStatement        stmt = con2.prepareStatement(cmd);
     	     ResultSet rs = stmt.executeQuery();

	   while (rs.next()) {
		   
		   String  id = rs.getString("CustomerASId").trim();
		   String  kood = rs.getString("kood").trim();
		 
		   if(klpMap.get(kood) != null ){
			   kood = (String)klpMap.get(kood) + "";
			   double debti = rs.getDouble("debt");
            	klpover.put(kood, debti);
            	  klids.put( kood,id);
		   }
            
	    }
          rs.close();
          stmt.close();
   
	} catch (Exception e) {
          getParent().showError(e);
      }

	return customers;
  }

  
  private TreeMap klpover = new TreeMap();
  private TreeMap klids = new TreeMap();
 
  private Connection getNewConnectionScan() throws SQLException{
	    return DriverManager.getConnection(url4, "ESofficeLogin", "esoffice");
	  }

  
  private TreeMap<String, String> getScannedLastDate(){
	   TreeMap<String, String> res = new TreeMap<String, String>();
	   String sql = "select partneris_id as klient, max (scan_time) as daat, max (patv_time) as lastconf from turto_list group by partneris_id";
	   try {
		   Connection c = this.getNewConnectionScan();
		   Statement st = c.createStatement();
		   ResultSet rs = st.executeQuery(sql);
		   
		   while(rs.next()){
		 	  String cliID = rs.getString("klient");
		 	 if(cliID != null) {
			 	
			 	  Date d2 = rs.getDate("daat");
				 	//  Date d2 = rs.getDate("lastconf");
				 	  
				   if(d2 != null){
				 		 res.put(cliID,(d2+"").substring(0,10));
				 	  }
				 	  
			 	  
			 	  }
			 	 
		 	 
		   }
		   rs.close();
		   st.close();
		   c.close();
	   }catch(Exception e) {
		   e.printStackTrace();
	   }

      
	   return res;
  }

 
  public TreeMap getCustomers(Agent agent){

	 // makeFixHash();
	  getKLPCustomers();
	  System.out.println("Alustan Kliendid");
	  
 String cmd = "  SELECT dp.name,street as nimelisa,dp.namealias,cus.accountnum, " +
           "     adr.street as tanav,adr.city as linn, adr.county as regioon,adr.zipcode as indeks ,cus.invoiceaccount AS payer, " +
           "    0 AS debt, cus.linedisc , " +  
           "       adr.address, VisitTimeTblCode_BBS  AS visit ,(Select h.personnelnumber from HCMWORKER h WITH (NOLOCK) where SALESMANAGER_BBS = h.recid) AS agent ," +
		"		  (Select h.personnelnumber from HCMWORKER h where SALESTAKER_bbs = h.recid) AS agent2,  numofdays AS term, cus.blocked AS block , " +
         "                      (select max (blocked) from  custtable cc  WITH (NOLOCK) where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = cus.dataareaid ) " +
          "               as payerblock ,(select l.locator  from  LOGISTICSELECTRONICADDRESS l WITH (NOLOCK)  where l.recid = dp.primarycontactphone)  " +
		"				  as info , linedisc as grupp, MINORDERAMOUNT_BBS as minord , " +
         "                    ROUTECODE_TR as transa,'' as discount ,'' as samm , SubGroupCode_BBS as tyyp ,TURNOVERLEVEL_BBS as tyyp2 ," +
			"				 (Select top 1 DISPLAYVALUE from DIMENSIONATTRIBUTEVALUESETITEM dv WITH (NOLOCK) where dv.DIMENSIONATTRIBUTEVALUESET = cus.DEFAULTDIMENSION    ) as osad, SHELFAREA_BBS as sharea, " +
            " SHELFSHARE_BBS As shshare ,'' as pictemail ,(select top  1 defaultagreementlineexpirationdate from agreementheader ah WITH (NOLOCK) where ah.custaccount=cus.invoiceaccount order by defaultagreementlineexpirationdate desc)  as contrend  "
            + " FROM  custtable cus with(nolock) inner join DirPartyTable dp with(nolock) ON cus.partY=dp.REcid  inner join " +
			" LOGISTICSPOSTALADDRESS adr ON adr.LOCATION = dp.PRIMARYADDRESSLOCATION and getdate() >=VALIDFROM  and  getdate() <= VALIDTO left outer JOIN  paymterm  " +
             "           pay on cus.paymtermid = pay.paymtermid  and pay.DATAAREAID  = cus.DATAAREAID  " +
              "                WHERE   (cus.dataareaid='zp'  and (SALESMANAGER_BBS != 0 or SALESTAKER_bbs != 0)) OR cus.accountnum in ('NAUJAS','OFISAS') " ; //47406
 
 String ccc = "select invoiceaccount from  custtable where dataareaid='zp' and invoiceaccount is not null  group by invoiceaccount";
 
           String sql = " SELECT  a.ORDERACCOUNT AS klient,a.invoice AS numbe,co.duedate AS maksepaev,sum(co.amountmst) AS tasumata, " +
                 "   co.transdate AS kuupaev , Datediff (dayofyear, co.duedate, getdate()) AS yle FROM  custtransopen co with(nolock) " +
				 "   INNER JOIN  custtrans a ON co.REFRECID=a.recid and  co.DATAAREAID =a.DATAAREAID    " +
                 "    WHERE co.dataareaid='zp' AND a.dataareaid='zp'	 " +
				 " group by a.invoice, a.ORDERACCOUNT,co.duedate, co.transdate  ORDER BY co.transdate,a.invoice, a.ORDERACCOUNT,co.duedate ";

           
           String salessql = "    SELECT  jou.orderaccount as klient,round(SUM(lineamount),0) AS summa,MONTH(sal.invoicedate) as kuu " +  
        " FROM  custinvoicetrans sal  with(nolock) INNER JOIN  custinvoicejour jou ON  sal.invoiceid  =jou.invoiceid   and " + 
       "  jou.DATAAREAID =sal.DATAAREAID  WHERE " + 
	   " sal.dataareaid='zp' AND sal.invoicedate > getdate() -390  GROUP BY jou.orderaccount,MONTH(sal.invoicedate) " +
	   " ORDER BY jou.orderaccount,MONTH(sal.invoicedate)" ;
               
           
           
           
           String sss = "SELECT  accountnum, sum(case when co.duedate<getdate() then amountmst else 0 end) as asi ," +
           		"sum(amountmst) as asi2,0 as tara FROM  custtransopen co " + // sum(case when DebtType ='4'  then amountmst else 0 end)
           		"WHERE co.dataareaid='zp'   " +
           "  group by  accountnum";
           
           
          

           
           String ddd = "select accountnum as klp ,ExtCodeValue as zp from  custtable a ,  ExtCodeValueTable b " +
 " where  a.dataareaid='klp' and  b.dataareaid='klp' and a.RecId= b.ExtCodeRelationRecId  and  b.ExtCodeId = 'ZP' and b.extCodeTableTableId=77";
           
           
           String cmd2 = " SELECT disc.accountrelation as klient ,disc.percent1 AS discount FROM  pricedisctable disc  WHERE " +
                 "   disc.dataareaid='zp' AND  ltrim(disc.itemrelation)='0'  AND fromdate<=getdate() and todate >= getdate() ";
          
           TreeMap aled = new TreeMap();
           Connection con = null;
           try{
                 con = this.getNewConnection();
        	   Statement stm = con.createStatement();
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
           

     	  System.out.println("Alustan Kliendid 1");
           try{
               
               String fff = " select custaccount as klient,name as nimi ,  " +
  " case when birthyear  > 1930 then CONVERT(date, ( CAST(birthyear AS varchar) +'-'+ CAST(birthmonth AS varchar)  +'-'+ CAST(birthday AS varchar) ), 126) else null end " +
  "   as synna , (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactemail)  as email, " +
  " (select l.locator  from  LOGISTICSELECTRONICADDRESS l  where l.recid = dp.primarycontactphone)  as phone " +
  "  from  Contactperson co inner join DirPartyTable dp ON co.partY=dp.REcid where dataareaid='zp'";
              

                   StringBuffer sb2 = new StringBuffer();
                
            	   Statement stm = con.createStatement();
            	  ResultSet rs1 = stm.executeQuery(fff);
                   while(rs1.next()){
                	   String klient = rs1.getString("klient").trim();
                	   String nimi = rs1.getString("nimi").trim();
                	   java.sql.Date synna = rs1.getDate("synna");
                	   if(synna == null){
                		   synna = new java.sql.Date(0);
                	   }
                	  String email = parent.make(rs1.getString("email"));
                	  String phone = parent.make(rs1.getString("phone"));
              //	  String cell = parent.make(rs1.getString("cellularphone"));
                    sb2.append(klient+";"+ nimi + ";" + synna.getTime() + "; ;" + phone  + ";" + email +  "\n");
                   }
                  rs1.close();  
                  stm.close();
                  if(sb2.length() > 2){
                     		 Converter.saveFileInUTF8( "custcontacts.txt", sb2);
                     	}
                  

              
           }catch (Exception r){
        	   System.out.println(cmd2);
        	   r.printStackTrace();
           }

             
           TreeMap<String, String> scans =  getScannedLastDate();
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
    		 stmr = con.createStatement();
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
  	   Statement stmu = con.createStatement();
    	
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
       
       stmu = con.createStatement();
   	
       rs1 = stmu.executeQuery(ddd);
      while(rs1.next()){
    	  String kp = rs1.getString("klp");
          String eg = rs1.getString("zp");
     
     if(eg != null && eg.trim().length() >0 &&  kp != null && kp.trim().length() > 0){
    	 custKlp.put(eg.trim(), kp.trim());
    	 klpMap.put(kp.trim(), eg.trim());
     }
  	   
      }
     rs1.close();
     stmu.close();

	//  System.out.println("Alustan Kliendid 4");
    	
        stmr = con.createStatement();
    	 rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
        	
       payers.add(rs1.getString("invoiceaccount"));
          
        }
       rs1.close();
       stmr.close();
    	

 	  System.out.println("Alustan Kliendid 5");
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      Statement stm = con.createStatement();
      while(rs.next()){
        String name = rs.getString("name").replaceAll("UAB", " ").trim();
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name =  name + " " + alias ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);
       	
        
        String tanav = rs.getString("tanav");
        if (tanav != null && tanav.length() > 0){
          cust.restStreet = tanav.trim();
        }
        String linn = rs.getString("linn");
        if (linn != null && linn.length() > 0){
          cust.restCity = linn.trim();
        }
        String regioon = rs.getString("regioon");
        if (regioon != null && regioon.length() > 0){
        	cust.restCity = (cust.restCity + " " + regioon.trim()).trim();
        }

        String indeks = rs.getString("indeks");
        if (indeks != null && indeks.length() > 0){
          cust.restZipCode = indeks.trim();
        }
      
        name = name.replaceAll("\\r\\n|\\r|\\n", " ");
        name = name.replaceAll("'", " ").replaceAll("\"", " ").replaceAll("  ", " ");
      
        cust.restName = name;
        cust.setCountry("Lithuania");


        
        
        
        
        Vector grupp = (Vector)fixGroups.get( accountnum);
          if(grupp != null  ){
          //	cust.setFixSortCode(grupp);
          	//System.out.println("Tere panen gruppi " + accountnum+ " "+ grupp.toString());
          }
        name = name + " " + accountnum;
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
        	cust.setInfo("CHART Pardavimai :|Menuo ir metai|Pardavimai €|" + salesres.get(accountnum) );
        }
        

        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Lithuania").replaceAll("\\r\\n|\\r|\\n", " "));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
        	
          cust.setVisit(Converter.getVisitDayLetter(visit.trim()));
        }
        String agen = rs.getString("agent");
      
        if (agen != null && agen.trim().length() >0) {
          cust.addAgent(new Agent(agen.trim()));
    
          cust.addAgent(new Agent("WARE1"));
          cust.addAgent(new Agent("WARE2"));
          cust.addAgent(new Agent("WARE3"));
          cust.setMainAgent(agen.trim());
          
          
          Agent agg = ((Agent)this.getAgents2().get(agen.trim()));
			
			if(agg != null && agg.getDubbler().size()>0){
				for (int e = 0 ; e < agg.getDubbler().size() ; e++){
					cust.addAgent(new Agent((String)agg.getDubbler().elementAt(e)));
				}
          	}
			
          
/*          if(agen.trim().startsWith("VIL") || agen.trim().startsWith("ALY") ){
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
          }*/
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
          cust.setSalesBlockT(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1 || payerblock == 2) {
          cust.setSalesBlockT(true);
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
          cust.setCity("Marsruto numeris: " +transa.trim());
          
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
        
        String tyyp2 = rs.getString("tyyp2");
        if (tyyp2 != null && tyyp2.trim().length() > 0) {

        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("Apyv.lygis :" + tyyp2.trim());
        	}else{
        		cust.setCity(cust.getCity() + "| Apyv.lygis :" + tyyp2.trim());
        	}     
        }
        
        if(this.klpover.get(cust.getID()) != null){
        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("KLP debitory : " + (klpover.get(cust.getID()) + "").replace('.', ',') + " €");
        	}else{
        		cust.setCity( cust.getCity() + "| KLP debitory : " + klpover.get(cust.getID()) + " €");
        	}
        
        }
        
        String id = (String) klids.get(cust.getID());
        
        if(id != null && scans.get(id) != null) {
        	
        	if(cust.getCity()== null || cust.getCity().length() == 0 ){
        		cust.setCity("KLP Paskutinis skenavimas: " + scans.get(id));
        	}else{
        		cust.setCity( cust.getCity() + "| KLP Paskutinis skenavimas: " + scans.get(id));
        	}
			
		   }
        
        
        String osad = rs.getString("osad");
        
        String sharea = rs.getString("sharea");
        String shshare = rs.getString("shshare");
        String add = "";
        if(sharea != null && sharea.trim().length() > 0 && !sharea.equals("0")){
        	add = "Lentynos plotas : " + sharea.trim() + "|";
        }
        if(shshare != null && shshare.trim().length() > 0 && !sharea.equals("0")){
        	add = add +  Converter.enCodeEst("Mûsø plotas % : ") + shshare.trim();
        }
       
        if(cust.getID().equals("NAUJAS") || cust.getID().equals("OFISAS") ){
        	   Enumeration enume = getParent().getAgentsHash().elements();
               while (enume.hasMoreElements()) {
                 Agent a = (Agent) enume.nextElement();
                   cust.addAgent(a);
               }
        }
             
        String pictemail = rs.getString("pictemail");
        
        if(pictemail != null && pictemail.trim().length() > 0 ){
        	cust.setPictEmail(pictemail.trim());
        }
        
       java.sql.Date contrend = rs.getDate("contrend");
        
        if(contrend != null && contrend.toString().trim().length() > 0 && contrend.toString().indexOf("1900") == -1 ){
        	  if(add.length() > 0){
        		  add = "Sutartis baigiasi :" + contrend.toString().substring(0,10);  
        	  }else{
        		  add = add + "|Sutartis baigiasi :" + contrend.toString().substring(0,10);  
        	  }
        	
        		cust.setContractOver(contrend.getTime() + "");	 //PANEME LÕPU KUUPÄEVA KÜLGE
        	
        }
        
        String taraStr = (String)taraDebt.get(payer);
        if (taraStr != null){
        	if(add.length() > 0){
        		add = "Taros skola :" +  taraStr;
        	}else{
        		add = add + "|Taros skola :" +  taraStr;
        	}
          
        }
        
        
        if(add.length() > 0){
        	   if(cust.getCity()== null || cust.getCity().length() == 0 ){
           		cust.setCity(add);
           	}else{
           		cust.setCity(cust.getCity() + "|" + add);
           	}	
        }
     
        if(custKlp.get(accountnum)  != null){
        	String partnercode = "KLP Kodas :" + (String)custKlp.get(accountnum) ;
        	   if(cust.getCity()== null || cust.getCity().length() == 0 ){
              		cust.setCity(partnercode);
              	}else{
              		cust.setCity(cust.getCity() + "|" + partnercode);
              	}	        	
        }
        
        
        
    
        
    //    cc.addQuestiosToCustomer(cust,kyssad);
     
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	//laks = false;
                    }
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      rs.close();

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
                      rs.close();
                      stmt.close();
                      con.close();

                	  System.out.println("Alustan Kliendid 7 ");

    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    if(rcompany != null){
    // 	Converter.sendCustomerJson(customers, rcompany,false,user,pass);
     }
    
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  //
  
  private Connection getNewConnectionAsset() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.asset"), getParent().getProps().getProperty("dsn.userasset"), getParent().getProps().getProperty("dsn.passasset"));
	  }
  
  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host2"));
	  }
  private Connection getNewConnection3() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }

  
  private Connection getNewConnectionSkaita() throws SQLException{
	    return DriverManager.getConnection(url3, "sa", "01234567");
	  }
  
      public String getDistribution() {
        return null;
      }

      
      private TreeMap getKPHinnadOld(){
    	  TreeMap res = new TreeMap();
    	  String  hk = "93776";
    	  if(getParent().getProps().getProperty("kpPricelist") != null){
    		  hk = getParent().getProps().getProperty("kpPricelist");
    	  }
    	  
    	  try{
    		  String sql = "uspv_Kainininkas '" + hk + "', 'lt' ,0";
    		  Connection con = getNewConnectionSkaita();
    		  CallableStatement  msProcedure = con.prepareCall( sql );
    		     
    			
    	       //  System.exit(0);
    	         //stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
    	      ResultSet rs3 = msProcedure.executeQuery();
    	      while(rs3.next()){
    	    	  String toode = rs3.getString("prekesid");
    	    	  String hind = rs3.getString("kaina");
    	    //  	System.out.println( "Toode " +  toode + " hind " + hind);
    	      	res.put(toode, hind);
    	      }
    	       rs3.close();
    	       msProcedure.close();
    	  }catch (Exception e){
    		 e.printStackTrace();
    	  }
    	  
    	  return res;
      }
      
      
      private TreeMap getKPHinnad(){
    	  TreeMap res = new TreeMap();
    	  String  hk = "79001";
    	  
    	 
    	  
    	  try{
    		  
    		  String baas = "(select PrekiuKainuKategorijosID as hk from NuolaiduKainininkuSudetis where nuolaidukainininkaiid= 1 and DataIki_Int = 999999)";
    		  Connection con = getNewConnectionSkaita();
     	      PreparedStatement  stmt = con.prepareStatement(baas);


    	      ResultSet rs = stmt.executeQuery();

    	      while(rs.next()){
    	    	hk = rs.getInt("hk") + "";  
    	      }
    	      rs.close();
    	      stmt.close();
    	      con.close();
    		  
    		  String sql = "uspv_Kainininkas '" + hk + "', 'eur' ,0";
    		  con = getNewConnectionSkaita();
    		  CallableStatement  msProcedure = con.prepareCall( sql );
    		     
    			
    	       //  System.exit(0);
    	         //stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
    	      ResultSet rs3 = msProcedure.executeQuery();
    	      while(rs3.next()){
    	    	  String toode = rs3.getString("prekesid");
    	    	  String hind = rs3.getString("kaina");
    	    //  	System.out.println( "Toode " +  toode + " hind " + hind);
    	      	res.put(toode, hind);
    	      }
    	       rs3.close();
    	       msProcedure.close();
    	       con.close();
    	  }catch (Exception e){
    		 e.printStackTrace();
    	  }
    	  
    	  return res;
      }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
  //  String    sql = "   SELECT PRODUCTLINEID as liik,name as nimi FROM  inventproductline  where dataareaid ='zp'  ";
 
    String    sql = "select e.recid as liik  , e.name  as nimi from EcoResCategory e inner join inventtable t on t. CATEGORYINVENTGROUP_BBS= e.recid " + 
			"  where  costgroupid ='Prod' group by e.recid , e.name  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    HashSet names = new HashSet();
    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      String nim = rs.getString("nimi").trim();
      if(nim.indexOf(" ") != -1){
    	  nim = nim.substring(nim.indexOf(" ")).trim();
      }
      if(names.contains(nim)) {
    	  nim = nim + " .";
      }else {
    	  names.add(nim);
      }
        tm.put(liik,  Converter.enCodeEst(nim));
    }
    rs.close();
    st.close();
    con.close();
    try{
    	if(sendKPdata){
    		   getKlpProdGroups(tm);
    		    }
 	}catch(Exception rr){   
    }
    return tm;
}


  
  public void getKlpProdGroups(TreeMap gr) throws SQLException {
      Connection con = this.getNewConnectionSkaita();
      System.out.println("Kpienas tootegrupid alustan ");

    String    sql = "	SELECT     CONVERT(varchar(20), md.SkaitaMedisId) AS kood, CONVERT(varchar(50), md.Pavad) AS nimetus, CONVERT(varchar(20), " +
                   "   CASE md2.SkaitaMedisId WHEN 1 THEN NULL ELSE md2.SkaitaMedisId END) AS ProdGroupParentAsId, CONVERT(varchar(15), md.Kodas) AS ProdGroupCode " +
 " FROM         dbo.SkaitaMedis_List AS mdl INNER JOIN  " +
                     " dbo.SkaitaMedis AS md ON mdl.SkaitaMedis_ListId = md.SkaitaMedis_ListId INNER JOIN " +
                  "    dbo.SkaitaMedis AS md2 ON md.TevasId = md2.SkaitaMedisId " +
" WHERE     (mdl.SysVardas = 'PREKES_STD') AND (md.SkaitaMedisId > 1) " ;

    
  
    

    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
    	String kood = rs.getString("kood");
  	String nimetus2 =  rs.getString("nimetus");
  		 gr.put("kl" + kood, " KL " + Converter.enCodeEst(nimetus2.trim()));	
  	
  	}
    rs.close();
    st.close();
    con.close();
    System.out.println("Logsol tootegrupid ending ");
    }

  
  public TreeMap getProdGroupsSecond() throws SQLException {
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = "select e.recid as liik , e.name as nimi from EcoResCategory e inner join inventtable t on t. CATEGORYPRODUCTLINE_BBS= e.recid " +
			" where  costgroupid ='Prod' group by e.recid , e.name ";
	    
	    
	    sql = "Select ww.displayvalue as liik , ww.displayvalue as nimi from DimensionAttributeValueSetItemView ww where " + 
		"	dimensionattribute = 5637146830";
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
	    String    sql = " SELECT inv.itemid AS kood, bar.itembarcode AS ean  FROM  inventtable inv " +
	     " inner join  inventitembarcode bar on bar.dataareaid = 'zp' and bar.useforprinting =" +
	   " 1 and bar.itemid=inv.itemid  WHERE   inv.dataareaid='zp'  and  bar.dataareaid='zp' " ;
	  //  "  and (inv.itemgroupid like 'Z%' or inv.itemgroupid like 'S%') and inv.Assortment = 1";
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
	    String    sql = "   SELECT accountnum as kood,CustCode_EDI_BBS as maksima FROM "
	    		+ " custtable cus inner join DirPartyTable dp ON cus.partY=dp.REcid  " +
                           "    WHERE   cus.dataareaid='zp' and dp.name like '%axima%' ";
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
	  codeEanprods = new TreeMap();
	   ArrayList extraProds = new ArrayList();
	    File f = new File ("extracodes.txt");
	    try {
			Converter.readFile(f, "utf-8", extraProds);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    
	    System.out.println("Exstra prods " + extraProds);
	    String addsql  = "";
	    if(extraProds.size() > 0) {
	    	addsql = " or inv.itemid in (";
	    	
	    	
	    	String sisu = "";
	    	Iterator itera = extraProds.listIterator();
	    	while (itera.hasNext()) {
	    		String tok = (String)itera.next();
	    		if(tok.length() > 0) {
	    			sisu = sisu + "'" + tok + "'";
		    		if(itera.hasNext()) {
		    			sisu = sisu + ", ";
		    		}	
	    		}
	    		
	    	}
	    	
	    	
	    	addsql = addsql + sisu + ") ";
	    }
	    
	  
    String cmd = " SELECT inv.itemid, inv. namealias AS nimetus, sui.qty  AS kastis,'' AS alusel, disc.amount AS price " +
  "     , bar.itembarcode AS ean ,inv.CATEGORYPRODUCTLINE_BBS AS grupp2, inv.CATEGORYINVENTGROUP_BBS   AS grupp , "
  + " ww.displayvalue as grkood, 0 as grid,mod.unitid As unit, 21 as vat , " +
  "   0  as samm ,sortcode as sorting  ,  0 as nosales  FROM  inventtable inv  " +
  "       left  JOIN  pricedisctable disc ON inv.itemid = disc.itemrelation  and " +
  "   disc.amount != 0  and (disc.todate>getdate()  or   YEAR(disc.todate) =1900)  and disc.accountcode='2' and disc.currency = 'EUR' and disc.dataareaid='zp' " +
  "      left JOIN  inventtablemodule mod ON mod.itemid = inv.itemid and inv.DATAAREAID =mod.DATAAREAID  " +
  "     left join  inventitembarcode bar  ON bar.useforprinting = 1 and bar.itemid=inv.itemid and  " +
  "     bar.DATAAREAID =inv.DATAAREAID  and bar.inventdimid = 'AllBlank' " +
"	     left  JOIN  BBSSuiteTable sui ON  inv.mainsuiteitemid_bbs = sui.suiteitemid  and " +
 "     sui.type=0  AND sui.dataareaid='zp' and   sui.qty > 1 " +
"   left join EcoResCategory e on  inv. CATEGORYINVENTGROUP_BBS= e.recid " +
 "  left join DimensionAttributeValueSetItemView ww on ww.DimensionAttributeValueSet = inv.defaultdimension " + 
		"	 and  dimensionattribute = 5637146830 " +
  "    WHERE   moduletype=2 and (blocked = 0 or blocked is null) and  inv.dataareaid='zp'  and ( Assortment_bbs = 1 " + addsql + " )  ";
    //z p algusega
  
    
    String sss = "select itemid , itembarcode from inventitembarcode  bb where bb.dataareaid = 'zp' and bb.useforprinting = 1 group by itemid , itembarcode";

 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
     Connection ccc = getNewConnection();
    	Statement stmt =  ccc.createStatement();
      ResultSet rs = stmt.executeQuery(sss);
      
      
      HashMap eans = new HashMap();
      while(rs.next()){
    	  String kood = rs.getString("itemid").trim();
    	  String ean = rs.getString("itembarcode").trim();
    	  eans.put(kood, ean);
      }
      rs.close();
      
      rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst( rs.getString("nimetus").trim() + " " + kood) ;
   
  
        prod.setInCases(rs.getDouble("kastis"));
       // prod.setOnPallet(rs.getDouble("alusel"));
        Double price = rs.getDouble("price");
        if(price != null) {
        	 prod.setPrice(price);
        }else {
        	prod.setPrice(1);
        }
       
        prod.setType("ZP");
        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
          prod.setEAN(ean);
        } else if(eans.get(kood) != null) {
    	  prod.setEAN(eans.get(kood)+"");
        }
        codeEanprods.put(prod.getID(), prod.getEAN());
        
    String add = "";
      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
       // add = grupp.trim().substring(0,2);
     }
       String gruppc = rs.getString("grkood");
       if(gruppc!=null && gruppc.trim().length()>0){
    	   
        add = gruppc.trim().substring(0,2);
        gruppc = gruppc.trim();
        prod.setGroupSecond(gruppc);
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

     
      
      String top = rs.getString("sorting");
      if(top != null && top.trim().length()>0 && top.trim().equals("TOP")){
       prod.setStatusTop("1");
    }
      
      String nosales = rs.getString("nosales");
      if(nosales != null && nosales.trim().length()>0 && nosales.trim().equals("1")){
       prod.noOrder = true;
    }
        products.put(add + nimetus, prod);
        prodHash.put(prod.getID(), prod);

      }
      rs.close();
      stmt.close();
      ccc.close();
    } catch (Exception e){
      getParent().log(e.toString());
      
    	System.exit(0);
    }

    try{
    	if(sendKPdata){
    		 KpienasProds(products);
    	}
   		
   }catch(Exception rr){
   	 rr.printStackTrace();  
   }
    return products;
  }



  private TreeMap codeEanprods = null;
  
  
  public void KpienasProds(TreeMap products){
 	 System.out.println("Kpienas Tooted alustan ");
 	TreeMap hinnad = getKPHinnad();
 	 
     String cmd = "  SELECT  Pr.PrekesID as tid, Pr.Kodas AS kood,'xKLP '+ CONVERT(varchar(40), ISNULL ((SELECT Pavad FROM dbo.PrekiuKalbos AS prkl WHERE (PrekesId = Pr.PrekesID)  " +
	 "  AND (KalbosId = 2)), Pr.Pavad)) AS nimetus, CONVERT(Money, Pr.PK_PardKaina) AS hind, CASE WHEN pr.mvid = pr.ipokavimomvid OR " +
                     " ISNULL(pr.ipokavimomvid, 0) < 2 OR " +
                     " pr.MVId NOT IN (2, 62733) THEN mv.Kodas ELSE ipokmv.Kodas END AS unit , pr.MV_Pastaba  AS kastis,  " +
     "  CONVERT(Money, NULL) AS CostPrice, CONVERT(varchar(20), Pr.KAPKodas) AS ean,   " +
     "                 CONVERT(varchar(20), md.SkaitaMedisId) AS grupp, CONVERT(char(1), 'Y') AS Valid " +
" FROM         dbo.Prekes AS Pr LEFT OUTER JOIN " +
  "                    dbo.MVGSantykiai AS mvs ON Pr.MVId = mvs.MVId1 AND Pr.IpokavimoMVId = mvs.MVId2 AND Pr.MVGId = mvs.MVGId INNER JOIN " +
   "                   dbo.Mokesciai AS mok ON Pr.MokesciaiId = mok.MokesciaiId INNER JOIN " +
    "                  dbo.PrekiuTipai AS pt ON Pr.PrekiuTipaiId = pt.PrekiuTipaiId INNER JOIN " +
     "                 dbo.MV ON Pr.MVId = dbo.MV.MVid INNER JOIN " +
      "                dbo.MV AS IpokMV ON Pr.IpokavimoMVId = IpokMV.MVid LEFT OUTER JOIN " +
       "               dbo.SkaitaMedis_Prekes AS mdp ON Pr.PrekesID = mdp.PrekesID LEFT OUTER JOIN " +
        "              dbo.SkaitaMedis AS md ON mdp.SkaitaMedisId = md.SkaitaMedisId " +
" WHERE      (Pr.PrekesID > 1) AND (pt.ArSandeliuojama = 1) and Pr.Nenaudoti <> ( 1 ) and md.SkaitaMedis_ListId IN (2)";

     
     
 
     
     
     String cmd2 = "SELECT    sand.Kodas AS WareHouseAsId,  pr.Kodas AS ProductAsId, CONVERT(Int, ps.Likutis_PP) AS UnitsInStock, CONVERT(Int, " + 
                "  ps.Likutis_PP - ps.KiekisRezervuotas) AS UnitsInStockFree  FROM  dbo.Sandeliai AS sand INNER JOIN " +
                "  dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
                "  dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
                " WHERE (sand.SandeliaiId > 1) and Likutis_PP > 0";
       if (isDebug()) {
           System.out.println(cmd);
       }

       try {
         Connection con = getNewConnectionSkaita();
         Statement st = con.createStatement();
          // ResultSet	      rs = st.executeQuery(cmd2);
           HashMap prodHash = new HashMap();
        /*   HashMap stocks = new HashMap();
             while (rs.next()) {
            	 
             }
             rs.close();
             st.close();
              con = getNewConnectionSkaita();
              st = con.createStatement();
              rs = st.executeQuery(cmd);  
          */
           
           ResultSet	      rs = st.executeQuery(cmd);
              
              while (rs.next()) {
         	  boolean goes = true;
         	  String tid = rs.getString("tid");
               String kood = "KLP-" + rs.getString("kood").trim();
               String abi = kood;
             
               String nimetus =  rs.getString("nimetus");
               if(nimetus == null){
             	  goes = false;
               }else{
             	 nimetus = nimetus.trim().replaceAll("\n", " ") ;
               }
             	  
               Product prod = new Product(kood);
              try{
            	  double price = Double.parseDouble((String)hinnad.get(tid));
                  if(price == 0){
               	   price = 1;
                  }
                  prod.setPrice(price);
                //  System.out.println("hind " + prod.getPrice());  
              }catch(Exception d){
            	  
              }
              prod.setVat("21");
              prod.setType("KLP");
               prod.setPrice(prod.getPrice());
               String unit = rs.getString("unit");
               if(unit != null && unit.trim().length()>0){
               	prod.setUnit(unit.trim());

               }
try{
	 prod.setInCases(rs.getInt("kastis"));
}catch (Exception e){
	
}
              
           

               String ean = rs.getString("ean");
               if(ean != null && ean.trim().length()>0){
               	prod.setEAN(ean.trim());
               }
               
        
               String grupp = rs.getString("grupp");
               if(grupp != null && grupp.trim().length()>0){

               	 prod.setGroup("kl" + grupp.trim());
                
               }

          
               if(nimetus != null){
                   prod.setName(Converter.enCodeEst(nimetus));
               }
               
           
           
               if(goes){
          		   products.put(prod.getName(), prod);
                   prodHash.put(prod.getID(), prod);  
               }
              
           }
           rs.close();
           st.close();
           con.close();
       } catch (Exception e) {
           getParent().showError(e);
       }
       System.out.println("Klaipedos Pienas Tooted lõpetan ");
   }
 

  

  public Vector getCustomerProductsList(String agentCode){
    Vector custProds = new Vector();
    HashMap custs = new HashMap();
    //if (true) return custProds ;
    
System.out.println("CustProdlisti tegemine ");
 int counter = 1;
   String cmd = " SELECT /*+ leading(tra,jou) use_hash(tra,jou) */orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate " +
     "  FROM  custinvoicetrans  tra INNER JOIN  custinvoicejour jou ON " +
     "  tra.invoiceid =  jou.invoiceid  and  tra.DATAAREAID = jou.DATAAREAID  " +
     " and tra.SALESID  = jou.SALESID   WHERE  jou.invoicedate > getdate()-30 AND tra.DATAAREAID  = ('zp')  AND jou.DATAAREAID   = ('zp')  " +
     "  GROUP BY orderaccount,tra.itemid,tra.name ,jou.invoicedate ,tra.qty  Order by orderaccount,tra.name,jou.invoicedate desc " ;
  
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
   //2016
   
   String cmdu = " select r.customercode,r.itemcode,r.salesqty,r.invoicedateid from fctSales r inner join dimItem t on t.ItemCode=r.itemcode"
   		+ " and t.CompanyId=2 where r.invoicedateid > " + asi + " and r.CompanyId=2 " +
 " order by  r.customercode,t.itemname,r.invoicedateid desc " ;
   if (isDebug()) System.out.println(cmdu);

    try {

    	Connection ccc = this.getNewConnection2();
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
    try{
    	System.out.println("CustProdlisti tegemine KLP ");
  	if(sendKPdata){
  		getCustomerProductsListKLP(custProds);
		    }
  }catch(Exception ss){
  	
  }
  System.out.println("CustProdlisti tegemine TEHTUD ");
    return custProds;
  }
  private   HashMap klpMap = new HashMap();

  public void getCustomerProductsListKLP(Vector custProds) {
      // return new Vector();
	  String sql1 = "SELECT   part.Kodas as klkood ,  OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20),  " +
      " (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
      " pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), CostPrice = CONVERT(Money, NULL), kogus = CONVERT(Int, pk.Kiekis),  " +
     "  ODDiscount = CONVERT(real, 0), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0) + ISNULL(mok.TarifasProc2, " + 
     "  0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15), pk.PardavimuKunoId))),  " +
     "  op.OperacijosId , op.Data as kuupaev " +
" FROM         Operacijos AS op INNER JOIN " +
" Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                     PardavimuKunai AS pk ON op.OperacijosId = pk.OperacijosId INNER JOIN " +
"                     OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                    Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                   Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId " +
"WHERE     0 = 1 AND op.Kilme = 35 and op.Data> getdate()-30 and pk.Kiekis != 0 " +
"UNION ALL " +
"SELECT  part.Kodas as klkood ,    OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
"                     (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
"                     pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), CostPrice = CONVERT(Money, xSav.Savikaina), kogus = CONVERT(Int, pk.Kiekis),  " +
"                     ODDiscount = CONVERT(real, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0)  " +
"                     + ISNULL(mok.TarifasProc2, 0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15),  " +
"                     pk.PardavimuKunoId))), op.OperacijosId , op.Data as kuupaev " +
"FROM         PardavimuKunai AS pk INNER JOIN " +
"                      Operacijos AS op ON pk.OperacijosId = op.OperacijosId INNER JOIN " +
"					   Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                      OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                      Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                      Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId LEFT OUTER JOIN " +
"                      xPardavimuKunai_Savikaina AS xSav ON pk.PardavimuKunoId = xsav.OperacijosKunoId " +
"WHERE     op.OperacijosTipoId = 50 and op.Data > getdate()-30 and pk.Kiekis != 0";





         if (isDebug()) {
             System.out.println(sql1);

         }

         try {
           Connection con = this.getNewConnectionSkaita();
           PreparedStatement stmt = con.prepareStatement(sql1);

           //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
           ResultSet	      rs = stmt.executeQuery();

           HashMap kliendid = new HashMap();
           while (rs.next()) {
             String kliendikood = rs.getString("klkood").trim();
             if(this.klpMap.get(kliendikood) != null){
          	   kliendikood = (String)klpMap.get(kliendikood);
          	   String tootekood = rs.getString("toode").trim();
          	 double hind = rs.getDouble("Price");
                 double kogus = rs.getDouble("kogus");
                 
                 Customer cust = (Customer)kliendid.get(kliendikood);
                 if (cust == null){
                   cust = new Customer(kliendikood);
                 }
                Product prod = new Product("KLP-" + tootekood);
                   CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                                rs.getString("kuupaev").substring(5,10), hind, 0, 0, " ");
                	
                	    cust.addProduct(prod,cp,false);
           
                	    kliendid.put(kliendikood, cust);
                   custProds.add(cp);
 
             }
                         }

           rs.close();
           stmt.close();
           con.close();
         } catch (Exception e) {
           getParent().showError(e);
         }
        
     }
  
 
  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  

    public void setBulkSales(Map agents){
  this.connectionUp();
    
  if(brandplans == null){
	  brandplans = new HashMap();
	  typeplans = new HashMap();
	  typebrand = new HashMap();
	
	  int mm = Calendar.getInstance().get(Calendar.MONTH)+ 1;
      int yy = Calendar.getInstance().get(Calendar.YEAR);
      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
      String mmm = mm+"";
      if(mmm.length() ==1){
    	  mmm ="0"+mmm;
      }
	  
         
      String       cmd2 = "  select  substr(a.BpcTimeId,1,4) , substr(a.BpcTimeId,5,2) ,a.EmployeeCode as agent,b.BpcItemBrandCode as brands, " +
    	        " b.BpcItemSalesClass as class,   " +
    	        "      sum(a.Suma) as summa, sum(a.Kiekis_T) as volume " +
    	        "        from fctBPC_PardavimaiVadyba as a  INNER join dimbpcItem as b on a.BPCItemid=b.bpcItemid " + // a.Itemcode=b.ItemCode
    	     "   where substr(a.BpcTimeId,1,4) ='" + yy + "'   and substr(a.BpcTimeId,5,2) = '" + mmm + "' and b.CompanyCode='ZP'  and a.bpcversioncode= 'VER01'" +
    	        "     group BY  a.EmployeeCode, substr(a.BpcTimeId,1,4), substr(a.BpcTimeId,5,2), b.BpcItemBrandCode,b.BpcItemSalesClass " ;

      
      System.out.println(cmd2);
      
      // loene kõik plaanid sisse 
      String ass = null;
      try {

    	  Connection conr = this.getNewConnection2();
          Statement stmtr =  conr.createStatement();
          ResultSet rsr = stmtr.executeQuery(cmd2);
    	  
                  while (rsr.next()) {
                  String age = rsr.getString("agent");
                  String brand = rsr.getString("brands");
                  String type = rsr.getString("class");
                
                 
              	if(age != null){
              	 age = age.trim();
                  double plancur = rsr.getDouble("summa");
                  double planvol = rsr.getDouble("volume");
                  
                  if(brand != null){
                	brand = brand.trim();
                  Toode tt = new Toode(brand);
                  tt.setPlancur(plancur);
                  tt.setPlanvol(planvol);
                  Vector asjad = new Vector();
                  if(brandplans.get(age) != null){
                	   asjad = (Vector)brandplans.get(age);
                  }
                  asjad.add(tt);
                  brandplans.put(age, asjad);
                  
                  
                  }
                  
                  if(age.equals("KAU004")){
             //    	   System.out.println("type" + " " +brand + " "+type);
                         
                   }
                  
                  if(type != null){
                	  type = type.trim();
                	  if(type.length() > 1)
                  typebrand.put(brand, type); 
                  
                  TreeMap tyybid = new TreeMap();
                  if(typeplans.get(age) != null){
               	   tyybid = (TreeMap)typeplans.get(age);
               	 
               	 if(tyybid.get(type) != null){
                    	Toode aa = (Toode) tyybid.get(type);
                       aa.setPlancur(aa.getPlancur() + plancur);
                       aa.setPlanvol(aa.getPlanvol() + planvol); 
                       tyybid.put(type, aa);
               	  }else{
               		Toode aa = new Toode(type);
                    aa.setPlancur(plancur);
                    aa.setPlanvol(planvol); 
                    tyybid.put(type, aa);
             		}
               	  typeplans.put(age, tyybid);
                 }else{
                	 Toode aa = new Toode(type);
                     aa.setPlancur(plancur);
                     aa.setPlanvol(planvol); 
                     tyybid.put(type, aa);
                	 typeplans.put(age, tyybid);	 
                 }}
              	
              	}  
                  
                  }
                  rsr.close();
                  stmtr.close();
                  conr.close();

                  } catch (Exception e) {
                      getParent().showError(e);
                  }
      
    	  
      System.out.println("plaan 2 " + typebrand.toString() + " plaan 2");
                  String
	              cmd = "SELECT Manager_dimCustomer.EmployeeCode as agent, dimItem.AnalyticGroupCode as brand , sum(fctSales.SalesAmountExcVATWithDiscMCurr) as summa, " +
"  sum(fctSales.SalesNettoWeightInKG) as kaal, Count (distinct dimCustomer.CustomerCode) as cust " +
" FROM dimEmployee  Manager_dimCustomer, dimCustomer, dimItem, fctSales, dimTimeTable, dimCompany WHERE " +
"  ( dimTimeTable.TimeTableId=fctSales.InvoiceDateId  )  AND  ( dimCustomer.CustomerId=fctSales.CustomerId  )  AND  ( dimItem.ItemId=fctSales.ItemId  )  AND  ( Manager_dimCustomer.EmployeeId=dimCustomer.EmployeeId  ) " +
"  AND  ( dimCompany.CompanyId=fctSales.CompanyId  ) AND  ( fctSales.OperationTypeCode='P'  ) AND  ( fctSales.SalesTypeCode>0  ) AND " +
"  ( dimTimeTable.TimeTableYear  = year(today())   AND dimTimeTable.TimeTableMonth  = month(today())   AND dimCompany.CompanyCode = 'ZP' ) " +
" GROUP BY Manager_dimCustomer.EmployeeCode,dimItem.AnalyticGroupCode";
                  
                  System.out.println("Kõik müügid"); 
	      try {
	    	  Connection conr = this.getNewConnection2();
	          Statement stmtr =  conr.createStatement();
	          ResultSet rsr = stmtr.executeQuery(cmd);
	          
	                  while (rsr.next()) {
	                	  String age = rsr.getString("agent");
	                      String brand = rsr.getString("brand");
	                      double donecur = rsr.getDouble("summa");
	                      double donevol = rsr.getDouble("kaal");
	                      double donecust = rsr.getDouble("cust");
	                    
	                      if(age.equals("KAU004")){
                       	   System.out.println("type" + " " +brand + " "+donecur);
                               
                         }
	                      
	                      if(age != null){
	                    	age = age.trim();
	                    	if(brand !=null){
	                    	brand = brand.trim();
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
	                    			   tt.setKogus(donecust);
	                    		   }
	                    		  // System.out.println("done " + tt.toString());
	                    	   }
	                      }	
	                    	}
	                      
	                      }
	                    
	                      TreeMap tyybid = new TreeMap();
	                      
	                      if(typeplans.get(age) != null){
	                    	  ass = age;
	                    	  tyybid = (TreeMap)typeplans.get(age);
	                    	   String type = typebrand.get(brand)+ "";
	                    	   if(type != null && tyybid.get(type) != null){
	                    		   
	                    		   Toode tt = (Toode)tyybid.get(type);
	                    		      tt.setDonecur(tt.getDonecur() + donecur);
	                    			   tt.setDonevol(tt.getDonevol() + donevol);
	                    			   tt.setKogus(tt.getKogus() + donecust);
	                    			   if(age.equals("KAU004")){
	                                	   System.out.println("type" + type + " " +brand + " "+donecur);
	                                        
	                                  }
	                    		  // System.out.println("done " + tt.toString());
	                    	   }
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
      String mmm = mm+"";
      if(mmm.length() ==1){
    	  mmm ="0"+mmm;
      }
      int yy = Calendar.getInstance().get(Calendar.YEAR);
      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
      int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
      double planprec = dd/d2;
      
      // see veel vaja vahetada ...
      
      String cmd ="SELECT Manager_dimCustomer.EmployeeCode as agent, sum(fctSales.SalesAmountExcVATWithDiscMCurr) as summa, sum(fctSales.SalesNettoWeightInKG) as kaal, Count (distinct dimCustomer.CustomerCode) as poode " +
" FROM dimEmployee  Manager_dimCustomer, dimCustomer, dimItem, fctSales, dimTimeTable, dimCompany " +
" WHERE ( dimTimeTable.TimeTableId=fctSales.InvoiceDateId  ) AND  ( dimCustomer.CustomerId=fctSales.CustomerId  ) AND  ( dimItem.ItemId=fctSales.ItemId  )  AND  ( Manager_dimCustomer.EmployeeId=dimCustomer.EmployeeId  ) " +
"  AND  ( dimCompany.CompanyId=fctSales.CompanyId  ) AND  ( fctSales.OperationTypeCode='P'  )  AND  ( fctSales.SalesTypeCode>0  )" +
"  AND  ( dimTimeTable.TimeTableYear  = year(today())   AND dimTimeTable.TimeTableMonth  = month(today())   AND dimCompany.CompanyCode = 'ZP')" +
" GROUP BY  Manager_dimCustomer.EmployeeCode ";
      
      String cmd2 = "  select a.EmployeeCode as agent,sum(a.Suma) as volume, sum(a.Kiekis_T) as summa " + //b.BpcItemBrandCode as brands,
    	        "  from fctBPC_PardavimaiVadyba as a  INNER join dimbpcItem as b on a.BPCItemid=b.bpcItemid " +
    		    "	  where substr(a.BpcTimeId,1,4) ='" + yy + "'   and substr(a.BpcTimeId,5,2) = '" + mmm + "' and b.CompanyCode='ZP' and a.bpcversioncode= 'VER01' " +
    	        "     group BY  a.EmployeeCode " ;

    	      
      System.out.println("Müügid 2"); 

         if (isDebug()) System.out.println(cmd2);
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
            	
                System.out.println("Müügid 3"); 
            	Connection cone = this.getNewConnection2();
                Statement stmt =  cone.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent");
                  
                  if(agentID != null){
                	agentID = agentID.trim();
                  Agent agent = (Agent)agents.get(agentID);
                 // System.out.println("leitud agent " + agent);
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
                  String asi = "Pardavimai: " + msales + " €||";
                  if(dd > 6){
                	  asi = asi + "Plano vykdymas/prognozė ():";
                      asi = asi + "     " +NF.format(tehtudsalperc*100) + "% / " + NF.format(prognosalperc*100)  + " %||";  
                  }
                
                  asi = asi + "Pardavimų planas: " + psales + " €||";
                  
                    String asi2 = "";                    
                    
                    if(typeplans.get(agent.getID()) != null){
	                	TreeMap vv =   (TreeMap)typeplans.get(agent.getID());
	                	 Iterator enume = vv.keySet().iterator();
	                	 while(enume.hasNext()){
	                		 String type = enume.next() + "";
	                		 Toode tt = (Toode)vv.get(type);
	                		
	                		 if(tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
	                			asi2 = asi2 + ("Tipas " + tt.getID() + "    " +
	                					((int)(tt.getDonecur()/tt.getPlancur()*100))
	                					+ "% Plan.: " +(int) tt.getPlancur() + " €  Parda.: " + (int)tt.getDonecur() + " € ||");
	                		    }
	                	    }
	                  }
                    
                    
                    if(brandplans.get(agent.getID()) != null){
	                	Vector vv =   (Vector)brandplans.get(agent.getID());
	                	 Enumeration enume = vv.elements();
	                	 while(enume.hasMoreElements()){
	                		 Toode tt = (Toode)enume.nextElement();
	                		 if(tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
	                			asi2 = asi2 + ("Brand " + tt.getID() + "    " +
	                					((int)(tt.getDonecur()/tt.getPlancur()*100))
	                					+ "% Plan.: " +(int) tt.getPlancur() + " €  Parda.: " + (int)tt.getDonecur() + " €  Distr. gylis : " +(int)tt.getKogus() +"||");
	                		    }
	                	    }
	                  }
                    
                    agent.setStart( Converter.enCodeEst(asi + "Aktyvûs klientai ") + (int)rs.getDouble("poode") + "|" + asi2 );
             
                  }
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
      String mmm = mm+"";
      if(mmm.length() ==1){
    	  mmm ="0"+mmm;
      }
    

String   cmd2 = "  select  a.EmployeeCode as agent,b.BpcItemBrandCode as brands, " +
          " b.BpcItemSalesClass as class,   " +
          "   	  sum(a.Suma) as summa, sum(a.Kiekis_T) as volume " +
          "        from fctBPC_PardavimaiVadyba as a  INNER join dimbpcItem as b on a.Itemcode=b.ItemCode " +
  	    "	  where substr(a.BpcTimeId,1,4) ='" + yy + "'   and substr(a.BpcTimeId,5,2) = '" + mmm + "' and b.CompanyCode='ZP' " +
          "     group BY  a.EmployeeCode, substr(a.BpcTimeId,1,4), substr(a.BpcTimeId,5,2), b.BpcItemBrandCode,b.BpcItemSalesClass " ;

      
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
                  
                  
                  }
                  rsr.close();
                  stmtr.close();
                  conr.close();

                  } catch (Exception e) {
                      getParent().showError(e);
                  }
    	  
      
	  
                  String  cmd = "SELECT Manager_dimCustomer.EmployeeCode as agent, dimItem.AnalyticGroupCode as brand, " +
 " sum(fctSales.SalesAmountExcVATWithDiscMCurr)as summa, sum(fctSales.SalesNettoWeightInKG) as kaal FROM " +
 " dimEmployee  Manager_dimCustomer, dimItem, fctSales, dimTimeTable, dimCompany, dimCustomer " +
 " WHERE ( dimTimeTable.TimeTableId=fctSales.InvoiceDateId  ) AND  ( dimCustomer.CustomerId=fctSales.CustomerId  ) " +
 "  AND  ( dimItem.ItemId=fctSales.ItemId  ) AND  ( Manager_dimCustomer.EmployeeId=dimCustomer.EmployeeId  ) " +
 "  AND  ( dimCompany.CompanyId=fctSales.CompanyId  ) AND  ( fctSales.OperationTypeCode='P'  ) AND  ( fctSales.SalesTypeCode>0  ) " +
 "  AND  ( dimTimeTable.TimeTableYear  = year(today())   AND " +
 "   dimTimeTable.TimeTableMonth  = month(today())   AND dimCompany.CompanyCode = 'ZP' ) GROUP BY Manager_dimCustomer.EmployeeCode, dimItem.AnalyticGroupCode";
                
                  //loeme kõik myygid sisse 

	      try {
	    	  Connection conr = this.getNewConnection3();
	          Statement stmtr =  conr.createStatement();
	          ResultSet rsr = stmtr.executeQuery(cmd);
	          
	                  while (rsr.next()) {
	                	  String age = rsr.getString("agent");
	                      String brand = rsr.getString("brand");
	                      double donecur = rsr.getDouble("summa");
	                      double donevol = rsr.getDouble("kaal");
	                     
	                    if(age != null){
	                       age = age.trim();
	                       
	                      Vector asjad = new Vector();
	                      if(brandplans.get(age) != null){
	                    	  ass = age;
	                    	   asjad = (Vector)brandplans.get(age);
	                    	   Enumeration enume = asjad.elements();
	                    	   while (enume.hasMoreElements()){
	                    		   Toode tt = (Toode)enume.nextElement();
	                    		   if(brand != null){
	                    	  		   if(tt.getID().equals(brand.trim())){
		                    			   tt.setDonecur(donecur);
		                    			   tt.setDonevol(donevol);
		                    		   }
		                    	   }
	                    	   }
	                    	  
	                    	  // brandplans.put(age, asjad);
	                      }
	                    
	                  	
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
	    " FROM  MAXIMSALESTABLE sal " +
	    " INNER JOIN  MAXIMSALESLINE rid ON sal.ordernum=rid.ordernum  " +
 " WHERE  sal.departmen ='" + agent.getID() + "' AND " +
	  "   sal.CREATEDDATE > getdate()-1  GROUP BY sal.ordernum, sal.createddate , departname,sal.dlvdate ORDER BY sal.dlvdate";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\n\n\n Priimtø uþsakymø sàraðas:\n\n");

	      try {
	    	  
	    	 Connection con = getNewConnection();
	       Statement   stmt = con.createStatement();
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  eilutës: " + (int)rs2.getDouble("summa") + "\n");
	                  }
	                  sb.append("\n");

	                  rs2.close();
	                  stmt.close();
	                  con.close();
	                  
	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }

	                  if(brandplans.get(agent.getID()) != null){
	                	Vector vv =   (Vector)brandplans.get(agent.getID());
	                	 Enumeration enume = vv.elements();
	                	 while(enume.hasMoreElements()){
	                		 Toode tt = (Toode)enume.nextElement();
	                		 if(tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
	                			 sb.append("Brand " + tt.getID() + "\n     plan: " +(int) tt.getPlancur() + " €   done: " + (int)tt.getDonecur() + " €\n\n");
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
  private String url = "jdbc:odbc:axauus";
  private String url2 = "jdbc:odbc:report";
  private String url3 = "jdbc:odbc:skaita";
  private String url4 = "jdbc:odbc:scan";
  private   Vector debt = new Vector();

  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
	@Override
	public void FixReporter() {
		saveAssetFile();
	}
	
	
}
