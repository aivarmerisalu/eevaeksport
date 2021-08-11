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

public class AxaZpienasAuditDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AxaZpienasAuditDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
  //  String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
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
    
 
    if("false".equals(getParent().getProps().getProperty("sendkpdata"))){
    	sendKPdata = false;
    }
  }

  
  private boolean sendKPdata = true;
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
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

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
               	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
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
			
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

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
       return dealerProds;
     }

  
  TreeMap agnames = null;
  
  public TreeMap getAgents(){
	 
    String cmd = "select emplid as kood , name AS name  from axa2.empltable where dataareaid = 'zp' ";// +
 // " and title = 'Vadybininkas'";
  // getFixedAssortmentData();
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
    
    //JAGAME REGIOONIDE J�RGI 
    
    Agent aa = new Agent ("AUDIT1");
    aa.setName("AUDIT1");
    agents.put("AUDIT1", aa);
    
    Agent aaf = new Agent ("AUDIT2");
    aaf.setName("AUDIT2");
    agents.put("AUDIT2", aaf);
    
    Agent aad = new Agent ("AUDIT3");
    aad.setName("AUDIT3");
    agents.put("AUDIT3", aad);
    
    Agent aas = new Agent ("AUDIT4");
    aas.setName("AUDIT4");
    agents.put("AUDIT4", aas);
    
    
    if(sb2.length() > 2){
    	try{
    		 Converter.saveFileInUTF8( "agent.txt", sb2);
    	}catch (Exception e){
    		
    	}
      
     }
    return agents;
  }
   
   
  
 
  public TreeMap getCustomers(Agent agent){
	  Converter cc = new Converter();
	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	  makeFixHash();
	  
 String cmd = "  SELECT cus.name,street as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
              " 0 AS debt, cus.linedisc ,   " +
              "    cus.address, SalesPoolId  AS visit ,emplid AS agent ,cus.ordertaker_bbs AS agent2,  numofdays AS term, cus.blocked AS block ,  " + // SalesPollId as visit
			 " (select max (blocked) from axa2.custtable cc where  cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = cus.dataareaid ) " + 
			 "  as payerblock ,phone || ' '|| telefax || ' '|| cellularphone as info , linedisc as grupp, MINORDERAMOUNT_BBS as minord , " +
			"	transportroutecode as transa,'' as discount ,InventQtyRounding_bbs as samm , custsubgroupcode as tyyp , dimension  as osad, ShelfArea_bbs as sharea," +
            " ShelfPart_bbs As shshare ,FMICode_LT as pictemail ,AgreementEndDate_bbs as contrend FROM axa2.custtable cus left outer JOIN axa2.paymterm  " +
			"  pay on SUBSTR(NLS_LOWER(cus.paymtermid),1,20)= SUBSTR(NLS_LOWER(pay.paymtermid),1,20) and SUBSTR(NLS_LOWER(pay.DATAAREAID),1,3) = SUBSTR(NLS_LOWER(cus.DATAAREAID),1,3) and  pay.dataareaid='zp' " +
			"	 WHERE   (cus.dataareaid='zp' AND cus.blocked != 2   and CUSTSUBGROUPCODE not in ('HORECA','BIUDZ') ) OR cus.accountnum in ('NAUJAS','OFISAS')  " ;
 
          

             
           

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

 	
       
       
    	
    	
    	Statement stmt = this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
    
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
        	cust.setInfo("CHART Pardavimai :|M�nuo ir metai|Pardavimai Lt|" + salesres.get(accountnum) );
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
          cust.addAgent(new Agent("HOUSE1"));
          cust.setMainAgent(agen.trim());
         
          
          if(  agen.trim().startsWith("KAU") || agen.trim().startsWith("VIL")){
        	  cust.addAgent(new Agent("AUDIT1"));
              cust.addAgent(new Agent("AUDIT2"));
      
          }else{
        	    cust.addAgent(new Agent("AUDIT3"));
                cust.addAgent(new Agent("AUDIT4"));
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
        //  cust.setPersonalData(info.trim().replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        double  minord  = rs.getDouble("minord");
        if (minord > 0 ) {
          cust.setMinsum(minord);
        }
        
        String transa = rs.getString("transa");
        if (transa != null && transa.trim().length()>0) {
         // cust.setCity("Mar�ruto numeris: " +transa.trim());
          
        }
        String rounding = rs.getString("samm");
        if (rounding != null && rounding.trim().equals("2")) {
          cust.setMakeAmountRounding(true);        
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
        
        String osad = rs.getString("osad");
        
             
        String pictemail = rs.getString("pictemail");
        
        if(pictemail != null && pictemail.trim().length() > 0 ){
        	cust.setPictEmail(pictemail.trim());
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

    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(getParent().getProps().getProperty("dsn.host"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
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
    return custProds;
  }

  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  
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
    return "AxaZpienasAudit";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  private HashMap brandplans = null; 
  

  public String getPilotDayData(Agent agent){
	      return null;
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
