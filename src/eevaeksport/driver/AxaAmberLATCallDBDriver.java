package eevaeksport.driver;

import java.sql.DriverManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import eevaeksport.EksportPanel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Calendar;
import java.util.Enumeration;

import eevaeksport.Customer;
import eevaeksport.Product;
import java.sql.SQLException;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import java.text.NumberFormat;

import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;
import eevaeksport.util.ReportingService;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashMap;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;

import eevaeksport.MatrixLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaAmberLATCallDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public AxaAmberLATCallDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
    nodebt = new Vector();
    String nd = getParent().getProps().getProperty("nodebt.list");
    if(nd != null){
 	   StringTokenizer st = new StringTokenizer(nd,",");
 	   while(st.hasMoreTokens()){
 		   nodebt.add(st.nextToken());
 	   }
    }
    System.out.println("nodebt .list " +nodebt.toString() );
    NF.setMaximumFractionDigits(4);
    NF.setGroupingUsed(false);
    
    
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
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }


  
  
  HashSet fixedSortiments = null;
  
  public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		fixedSortiments = new HashSet();
		File f = new File(this.getExcelDir() + "listing.xlsx");
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(4);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			for (int a = 9; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					String gg = row.getCell((short) a).getStringCellValue().trim();
					
					if(gg.length()>1){
				      groups.put(a + "", gg);
				     
				      if(!fixedSortiments.contains(gg)){
				    	  fixedSortiments.add(gg);
					  }
				      
				    }
				
				} catch (Exception e) {
					 System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println("groups " + groups.size()  );

			for (int i = 8; i <= sheet.getLastRowNum(); i++) {
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
					for (int a = 9; a < row.getLastCellNum(); a++) {

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

							if (go != null && ( go.toLowerCase().equals("1") || 
									go.toLowerCase().equals("2"))) {

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
								if(go.equals("2")){
									mline.setLineinfo("CENTRAL");
								}
								if(go.equals("1")){
									mline.setLineinfo("TOP");
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

		System.out.println("fix size " + result.size());
		// System.exit(0);
		return result;

	
}


   

   

  public Vector getPriceMatrixData() {
      Vector lines = new Vector();
     String sql = " SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, max(PRICEDISCTABLE.AMOUNT) AS price " +  
"   FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='spil' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=0 AND  " +
" PRICEDISCTABLE.itemcode=0  and PRICEDISCTABLE.AMOUNT > 0.01 and  " +
"   ((GETDATE() between PRICEDISCTABLE.FROMDATE-(case when DATEPART(DW, PRICEDISCTABLE.FROMDATE) = 2 then 3 else 1 end) AND PRICEDISCTABLE.TODATE  -(case when DATEPART(DW, PRICEDISCTABLE.TODATE) = 1 then 2 else 0 end)) " +
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
"   WHERE PRI.DATAAREAID='spil' AND PRI.RELATION in (4) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and " + 
"     ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 3 else 0 end)) " +
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
 "  WHERE PRI.DATAAREAID='spil' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
 "    ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 3 else 0 end)) " +
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
                                     mline.setCliGroup(clGroup.trim().replaceAll(";", ":"));
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
   	  " WHERE PRI.DATAAREAID='spil' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0  and " +
       "   ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 3 else 0 end)) "+
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
                                            mline.setCliGroup(custID.trim().replaceAll(";", ":"));
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
                                    
                                    if(kuu > 2 ){
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
                            
                            
                            
                            
                            
                            
                            
                            sql4 = "select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind ,PRI.FROMDATE as algus ,PRI.FROMDATE as algus2" +
            "   	 ,PRI.TODATE as lopp,DATEDIFF(week, PRI.FROMDATE, PRI.TODATE)  as kamp,PRIORITY as prior " +
" from astPromoGMLine l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum " +
" where l.dataareaid = 'spil' and ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 3 else 0 end)) " +
"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
"   or( GETDATE()<=PRI.TODATE+1)  and year(PRI.fromDATE) =1900 ) and accepted = 2 and onlycrmaction = 0 and fromqty = 0 and promostatus in (1,4)  and maxdiscountpromo = 0 " ;
                                     
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
                                                     mline.setCliGroup(custID.trim().replaceAll(";", ":"));
                                             }

                                             if (prodID != null && prodID.trim().length() > 0) {
                                                     mline.setProdID(prodID.trim());
                                             }
                                             

                                             
                                             if(kuu > 16 ){
                                            	 mline.setProdLevel("pr");
                                               }else{
                                               mline.setStartDate(algus.trim());
                                               mline.setEndDate(lopp.trim());
                                             }
                                             

                                             mline.setProdLevel(pri); 
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



  public String getAssortmentStr(Agent agent) {
		StringBuffer sb = new StringBuffer();

File f = new File(this.getExcelDir() + "/listing.xlsx");
		
		try {
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(6);
			XSSFRow row2 = null;
			row2 = sheet.getRow(2);
			HashSet done = new HashSet();
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 8; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					String asi = row.getCell((short) a).getStringCellValue();
					if(asi != null && asi.trim().length() > 0){
						if(asi.trim().equals("Agrees")){
							asi = asi + row2.getCell((short) a).getStringCellValue().trim();
						}
						
						groups.put(a + "",asi );
					}
					
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println(groups.toString());

			for (int i = 8 ; i < sheet.getLastRowNum()+1 ; i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row != null && row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	 // System.out.println("prodId " + prodID);
					// Siin läheme reasisse rokkima ja vaatama kas se toode
					// läheb kaasa
					for (int a = 8; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {
							String go = null;

							if (row.getCell((short) a) != null) {
								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									try {
									go = (row.getCell((short) a)
											.getStringCellValue());
									} catch (Exception w) {
										w.printStackTrace();
									}
								}
							}

							if (go != null && (go.equals("1") || go.equals("TRUE"))) { //  || go.equals("2")
								String ju = " ; ;" + group + ";" + prodID + ";TOP";
								if(!done.contains(ju)){
									sb.append(ju + "\n");// 100 oli siin 
									done.add(ju);
								}
								
							}
						}
					 }
                  }
			  }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	//	System.out.println (sb.toString());

		return sb.toString();
	}


  
  private TreeMap kogused = null;

 public String getSaveStockInfo(){
	  
   kogused = new TreeMap();
	  

//For IMPORT (including RED BULL) products - only SPIL ('GPP-VT','GPP-AK-LB') warehouses must be taken;
//For LB products - SPIL's warehouse 'GPP-VT' and warehouse from LB AX file - Lb_l - 'GPP'.


   String cmd = " select  summ.itemid  AS toode ,s.itemid as toode2 ,SUM( " +
 " case when itemgroupid ='LB' and dim.inventlocationid in ('GPP-AK-LB')then 0 else AVAILPHYSICAL   end ) AS stock    FROM INVENTSUM summ with(nolock) " +
	"          INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
	 "         INNER JOIN inventtablemodule inv with(nolock) ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid   " + 
		"	   INNER JOIN inventtable t with(nolock) ON t.itemid=summ.itemid and t.dataareaid=dim.dataareaid " +
		"	     left  JOIN inventtablecompany s ON s.itemid=t.itemidcompany    " +
	    "  WHERE  inv.DATAAREAID='spil' and CLOSED = 0 AND moduletype=0 and dim.inventlocationid in ('GPP-VT','GPP-AK-CIG') " + //,'GPP-AK-LB'
	"		GROUP BY  summ.itemid,s.itemid  having sum(availphysical) >0";


       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
    	   Connection con = this.getNewConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);
      TreeMap koodid = new TreeMap ();
         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           String kood2 = rs.getString("toode2");
           double seis = rs.getDouble("stock");
           
           if(seis > 0){
        	   if(kood2 !=null){
        		   kood2 = kood2.trim();
        		   koodid.put(kood2, kood);
        	   }else{
        		   products.append(kood +  ";");
                   products.append(" ;");
                   products.append(getParent().getNF().format(seis)   +  "\n"); // + dealer +  "\n");
               }
        	   
             kogused.put(kood, seis + "");
           }
           }

         cmd = "select  s.itemid as toode2 ,sum(AVAILPHYSICAL) AS stock  , t2.itemgroupid   FROM INVENTSUM summ with(nolock) " +
   	          "INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
   	          "INNER JOIN inventtablemodule inv with(nolock) ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid    " +
   			  " INNER JOIN inventtable t with(nolock) ON t.itemid=summ.itemid and t.dataareaid=dim.dataareaid " +
   			  "  INNER  JOIN inventtablecompany s with(nolock) ON s.itemid=t.itemidcompany   INNER  JOIN inventtable t2 ON"
   			   + "  t2.itemidcompany=s.itemid and t2.dataareaid = 'spil'  " +
   	          " WHERE  inv.DATAAREAID='lb_l' and dim.inventlocationid in ('GPP') and t2.itemgroupid ='LB'" +
   			  " GROUP BY  s.itemid, t2.itemgroupid   having sum(availphysical) >0 ";
         
     rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood2 = rs.getString("toode2").trim();
           double seis = rs.getDouble("stock");
           if(seis > 0){
        	   String kood = (String)koodid.get(kood2);
        	   if(kood != null){
        		   if(kogused.get(kood)!= null){
                	     kogused.put(kood,(Double.parseDouble(kogused.get(kood)+"")+ seis) + ""); 
                     	   
                   }else{
                	     kogused.put(kood, seis + ""); 
                   }
               }
               
           }
           }
         
      Iterator itera =   kogused.keySet().iterator();
      while(itera.hasNext()){
    	  String kood = (String)itera.next();
    	  double seis = Double.parseDouble(kogused.get(kood)+"");
    	  products.append(kood +  ";");
          products.append(" ;");
          products.append(getParent().getNF().format(seis)   +  "\n"); // + dealer +  "\n");
         
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
	  //makePlans();

	  //getAssortmentStr(null);
	 // System.exit(0);
	  agent1 = new TreeMap();
	  
	  
	  
    String cmd = " SELECT  a.ACCOUNTID as kood , d.NAME FROM  EAgentTable a inner join  EMPLTABLE  e  on e.EMPLID = a.EMPLID and e.DATAAREAID = a.DATAAREAID INNER JOIN " +
                       "  DIRPARTYTABLE AS d ON d.DATAAREAID = 'v02' AND e.PARTYID = d.PARTYID " +
               " WHERE   (e.DATAAREAID = 'spil') ";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    StringBuffer sb = new StringBuffer();
    try {
    	
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
        	String kood = Converter.cleanLitLatEst(rs.getString("kood").trim());
            String name = rs.getString("name").trim();
            sb.append(kood + ";" + Converter.enCodeEst(name) + "\n"); 
            name = name + " " + kood;
        	
          Agent agent = new Agent(kood);       
    
          agent.setName(name);
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
    
    
    if(sb.length() > 2){
		  	try{
		  		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb.toString()));
		  	}catch (Exception e){	    		
		  	}	      
		   }
    
  
   // setBulkSales(agents1);
    return agents;
  }

  
  public static float Round(float Rval, int Rpl) {
	   float p = (float)Math.pow(10,Rpl);
	   Rval = Rval * p;
	   float tmp = Math.round(Rval);
	   return (float)tmp/p;
	   }
  
  
  Vector nodebt = null;
  
  public TreeMap getCustomers(Agent agent){

	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
	   
	   
	   this.getFixedAssortmentData();
	   System.out.println("fixedSortiments "+  fixedSortiments.toString());
	   
 String  cmd = " SELECT	cus.name, cus.custclassificationid as nimelisa, cus.namealias, cus.accountnum,cus.invoiceaccount AS payer, " +
			"	(SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID " +
			"		WHERE co.duedate<getdate() and ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and co.dataareaid = N'spil' AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " +
			"			cus.invoiceaccount end )) AS debt,(SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct "+
		" on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE  ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and co.dataareaid = N'spil' " +
			"			AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " +
			"			cus.invoiceaccount end )) AS debta,(case when len(cus.INVOICEACCOUNT)=0 then cus.CREDITMAX else (select max (creditmax)  " +
	"		from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = N'spil') end) as limit, " +
	"	cus.linedisc ,cus.address, '' AS visit,  SPIEAGENTACCOUNTID as agent, SPITELESALESAGENTGROUPID as teles, " +
	"	(select top 1 numofdays from paymterm pt where pt.paymtermid=(select top 1 paymtermid from custtable  " +
	"			where accountnum = cus.invoiceaccount and dataareaid = N'spil'  ) and pt.dataareaid = N'v02') AS term, " +
	"	blocked AS block,(case when len(cus.INVOICEACCOUNT)=0 then cus.BLOCKED else  " +
	"		(select max (blocked) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = N'spil') end) as payerblock, " +
	"	cus.phone + ' , ' +  cus.cellularphone + ' , ' + cus.email  as info, PriceGroup as grupp , COMPANYCHAINID as chain, " +
	"	(case when len(cus.INVOICEACCOUNT)=0 then cus.creditrating else (select max (creditrating ) from custtable cc  " +
	"			where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = N'spil') end) as rating,subsegmentid as sugg ,  " +
	"	cus.StatisticsGroup as fixed, erpagentdiscedit as maxallow , custclassificationid as ette , subsegmentid as cate,cus.SPICOMMENT as cuinfo, " +
	"	 a.city as city , a.street as street,a.zipcode as ind ,(select spiminorderamount  " +
	"		from smmBusRelChainGroup e where e.dataareaid= cus.dataareaid and e.chainid = COMPANYCHAINID ) as minam , cus.exportformssamples  as sample " +
    " FROM custtable cus  inner JOIN dirpartytable dp ON dp.partyid=cus.partyid and dp.DATAAREAID = N'v02'	 " +
    " inner join address a on a.ADDRRECID = dp.RECID and a.DATAAREAID = N'v02' " +
    " WHERE   cus.dataareaid = N'spil'   and (SPIEAGENTACCOUNTID != '' or SPITELESALESAGENTGROUPID != '') and erpactive = 1 " ; //and blocked != 2
 
 
 String tra = "select  accountnum as cuid , monday as mon , tuesday as tue , wednesday as wed, thursday as thu , friday as fri , saturday as sat, sunday as sun " +
" from custtable cus inner join TRDDeliveryPlaces tr on tr.dataareaid = cus.dataareaid and  cus.trdoutletid = tr.outletid where cus.dataareaid = 'spil'";
 
 
 String ccc = "select AccountNum from custtable where dataareaid='spil' and (INVOICEACCOUNT = '' or ACCOUNTNUM = INVOICEACCOUNT)";

 String promo = "select custaccount as custid , l.promonum as grupp   from astPromoJournal pri  " + 
			" INNER JOIN astPromoCustomers l on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  where " +
			" l.dataareaid = 'spil' and ( (GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) " +
			"  AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 2 else 0 end))  " +
		  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
		  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
		  " or( GETDATE()<=PRI.TODATE+1 AND year(PRI.fromDATE) =1900) ) and promostatus in (1,4)";
	 

 
 String sql = "  SELECT a.orderaccount AS klient, a.accountnum as payer ,(case when a.ordertypeid like 'REALIZ%' then 'REALI-' else '' end)  + a.invoice AS number,co.duedate AS maksepaev,a.amountmst AS tasumata,  " +
         "    co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle  FROM custtransopen co with(nolock)  " +
		"	 INNER JOIN custtrans a with(nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
		"	   WHERE co.dataareaid='spil'  and a.invoice != '' " +
		"		and a.storno != 1 and a.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') ORDER BY a.accountnum,co.transdate " ;
			  
           
           String salessql = " SELECT  jou.orderaccount as klient,( ltrim( case when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
        "  round(SUM(lineamount),0) AS summa " +
	    "  FROM custinvoicetrans sal with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON  " +
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
		" WHERE sal.dataareaid='spil'   AND jou.dataareaid='spil' AND sal.invoicedate > getdate()-200 " +
        " GROUP BY jou.orderaccount ,(YEAR(sal.invoicedate)),(month(sal.invoicedate))  order by   jou.orderaccount ,YEAR(sal.invoicedate), month(sal.invoicedate) ";

                 

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
     rs1 = stmr.executeQuery("select reasoncodeid as id, description as nimi from ReturnReasonCode where dataareaid ='spil'");
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
   payers.add(rs1.getString("AccountNum"));
      
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
	   }  }
    rs1.close();
    stmr.close();
   
   
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim()).replace('*', ' ').trim();
        boolean addDebt = true;
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim()))
        	name = " " + alias.replace('*', ' ').trim() + " " + name ;
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        name = name.replaceAll("Jaunpiebalga", "Jau.");
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        }
        
        //panesiia lätlaste list .
        if(nodebt.contains(cust.getPayer())){
        	addDebt = false;
        }
        if(addDebt){
        String debtStr = rs.getString("debt");
        
        if (debtStr != null){
        	double ddd = Double.parseDouble(debtStr);
        	if(ddd > 0.04 || ddd < -0.04){
        	      cust.setDebt(round(ddd,2));
        	}
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',',')),"Latvia"));
        }
        
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        
        String age = rs.getString("agent");
        if (age != null) {
         cust.addAgent(new Agent(age.trim()));
         cust.setMainAgent(age.trim());
        
        
        
 			Agent aa = (Agent)agent1.get(age.trim());
 			if(aa != null){
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
    
        
        
        
        
        
        String teles = rs.getString("teles");
        if (teles != null && teles.trim().length() > 0) {
         cust.addAgent(new Agent(teles.trim()));
         
        
          if(teles.trim().startsWith("T"))
         	 cust.setCity("Tele: " + teles.trim());
     
       //paneme kõigile kõik juurde kui on teles olemas 
       Iterator itera =  agent1.keySet().iterator();
       
       while(itera.hasNext()){
    	String agd = (String) itera.next();
    	if(agd.startsWith("T")){
    		cust.addAgent(new Agent(agd));
    	}
       }
         
      
       if (teles != null && agent1.get(teles.trim()) != null){
			Agent aa = (Agent)agent1.get(teles.trim());
		
			Vector vec = aa.getDubbler();
			if(vec!= null && vec.size() >0){
				Enumeration en = vec.elements();
				while(en.hasMoreElements()){
					String ag = (String) en.nextElement();
					cust.addAgent(new Agent(ag));
				}
			}
		}
         
       cust.addAgent(new Agent("T1"));
       cust.addAgent(new Agent("T2"));
       cust.addAgent(new Agent("T3"));
       cust.addAgent(new Agent("T4"));
       cust.addAgent(new Agent("T5"));
       cust.addAgent(new Agent("T6"));
       cust.addAgent(new Agent("T7"));
       cust.addAgent(new Agent("T8"));
        }
    
        
        String term = rs.getString("term");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
        
        
        int block = rs.getInt("block");
        if (block == 1 ) {
          cust.setSalesBlock(true);
        }
        boolean goes = true;
        if (block == 2 ) {
            cust.setSalesBlockT(true);
           // goes = false;
          }
       
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }
        if (payerblock == 2) {
            cust.setSalesBlockT(true);
         //   goes = false;
          }

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>2) {
        	if(info.trim().startsWith(",")){
        		info = info.trim().substring(1).trim();
        	}
        	if(info.trim().startsWith(",")){
        		info = info.trim().substring(1).trim();
        	}
          cust.setPersonalData(info.trim());
        }
        if(salesres.get(accountnum) != null){
        	cust.setInfo(Converter.enCodeEst("CHART Pārdošana :|Mēnesis un gads|pārdošana |" )+ salesres.get(accountnum) );
        }
        
        if(transa.get(cust.getID())!= null){
        	String ppp = (String)transa.get(cust.getID());
        	if(ppp.startsWith(",")){
        		ppp = ppp.substring(1);
        	}
        	if(cust.getCity().length()>1){
        	 	cust.setCity(cust.getCity() + "|" +Converter.enCodeEst("Piegādes dienas : ") + ppp);
        	}else{
        	 	cust.setCity(Converter.enCodeEst("Piegādes dienas : ") + ppp );
        	}
        }
        
        if(visits.get(cust.getID())!= null){
        	cust.setVisit(visits.get(cust.getID())+"");
        }
        
        
  
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0 && grupp.indexOf("Stand") == -1) {
         
          if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
      		cust.setGroup(cust.getGroup() + "|" + Converter.cleanLitLatEst(grupp.trim()));
      	}else{
      		cust.setGroup(Converter.cleanLitLatEst(grupp.trim()));
      	}
          
          cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
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
          if(cust.getCity().length()>1){
      	 	cust.setCity(cust.getCity() + "|" +Converter.enCodeEst("Chain : " + chain.trim()));
      	}else{
      	 	cust.setCity(Converter.enCodeEst("Chain : " + chain.trim()));
      	}
        }
        
        
        String rating = rs.getString("rating");
        if (rating != null && rating.trim().length()>0) {
             if(cust.getCity().length()>1){
      	 	cust.setCity(cust.getCity() + "|" +Converter.enCodeEst("CRate : " + rating.trim()));
      	}else{
      	 	cust.setCity(Converter.enCodeEst("CRate : " + rating.trim()));
      	}
        }
        
        String fixed = rs.getString("fixed");
        if(fixed != null && fixed.trim().length() > 0){
     	   if(fixedSortiments.contains(fixed.trim())){
     		   cust.addFixSortCode(fixed.trim());
     		   
     	   }else{
     		   cust.suggAssortCode = (fixed.trim()); 
     		 //  System.out.println("panin sugg " + cust.getName() + " "+fixed);
     	   }
     	 
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
                           " ASSETTABLE  WHERE (DATAAREAID = 'spil') and  LOCATION='" + accountnum + "'";

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
                               
                          
                    
                    
                             
                    
                    if(goes){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      

      sql = "SELECT a.orderaccount AS klient, \r\n" + 
        		"a.accountnum as payer,\r\n" + 
        		"(case when a.ordertypeid like 'REALIZ%' then 'REALI-' else '' end) + a.invoice AS number,\r\n" + 
        		"co.duedate AS maksepaev, \r\n" + 
        		"a.amountmst AS tasumata,\r\n" + 
        		"co.transdate AS kuupaev, \r\n" + 
        		"datediff(day,co.duedate,getdate()) AS yle\r\n" + 
        		"FROM custtransopen co with (nolock) \r\n" + 
        		"INNER JOIN custtrans a with (nolock) \r\n" + 
        		"ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid\r\n" + 
        		"WHERE co.dataareaid='spil' and a.invoice != '' and a.storno = 0 and a.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT')\r\n" + 
        		"ORDER BY a.accountnum,co.transdate";
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                    	 boolean addDebt = true;
                        Debt debt = new Debt();
                        String saaja = rs.getString("klient").trim();
                        debt.setCustID(saaja);
                        String payerId = rs.getString("payer").trim();
                        debt.setPayerID(payerId);
                       // debt.setCustID(payerId); // NB!!!
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(round(rs.getDouble("tasumata"),2));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        if(nodebt.contains(saaja) || nodebt.contains(payerId)){
                        	addDebt = false;
                        }
                        
                        if(addDebt){
                        	 this.debt.addElement(debt);
                        }
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



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
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
  	    String    sql = "   select ITEMSUBGROUPID as liik , name as nimi from INVENTITEMSUBGROUP"
  	    		+ " where dataareaid ='spil' ";
  	    Statement st = con.createStatement();
  	    ResultSet rs = st.executeQuery(sql);

  	    while (rs.next()) {
  	    	   String abi = rs.getString("liik").trim();
  	    	  String liik = abi;
  	    	 if(liik.contains("_")){
  	    		 abi = liik.substring(0,liik.indexOf("_")) + liik.substring(liik.indexOf("_") + 1);
  	    	 }
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
  	    
  	    	
  	      String nimi = liik;// rs.getString("nimi").trim();

  	    if(liik.contains("WINE")){
	    	  nimi = "WINE";
	      } 
  if(liik.contains("GIFT")){
  	  nimi = "GIFT";
    }
	      if(liik.contains("SPAR")){
	    	  nimi = "SPARKL";
	      }
	    if(liik.contains("BEER")){
	    	  nimi = "BEER";
	      }
	      
	    if(liik.contains("SOFT")){
	    	  nimi = "SOFT";
	      }
	  if(liik.contains("SPIR")){
  	  nimi = "SPIRIT";
    }
  	        tm.put(abi,  Converter.enCodeEst(nimi.toLowerCase()));

  	    }
  	    rs.close();
  	    st.close();
  	    con.close();
  	    groups = tm;
  	    return tm;
  	    
    	  }else{
    		  return groups ;
    	  }
  	}




  public TreeMap getProdGroupsSecond() throws SQLException {

  TreeMap   tm = new TreeMap();
  	  Connection con = getNewConnection();
  	    
  	  String    sql = "  select  inv.ITEMSUBGROUPID as abi , BRANDID as liik , name as nimi from SPIBRANDTABLE sp inner join inventtable inv " + 
       " on sp.BRANDID = inv.SPIBRANDID and sp.dataareaid=inv.dataareaid "
       + " where sp.dataareaid ='spil' group by BRANDID,inv.ITEMSUBGROUPID, sp.name ";

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
			  " , bar.itembarcode AS ean ,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2,mod.unitid As unit, '21' as vat,mod.LineDisc as alegr  " +
			   " , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti ,NEWITEM as uus ,sellout as out , topitem as tops , topoffitem as tops2,"
			   + " giftbox as gift  , SPIDONTCONTROLMINAMOUNTMS as nocount  ,unitvolume as small , MarkupGroupId as pant FROM inventtable inv with(nolock) " +
			  "  left join InventItemSalesSetup s with(nolock) on s.itemid = inv.itemid and s.dataareaid=inv.dataareaid left join pricedisctable as disc with(nolock) on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid " +
			  "  and disc.module in ( 0 , 1 )  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation = 'Standarts' and (disc.todate > getdate() or year( disc.todate ) = 1900) and   (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 )" +
			  "  left join inventitembarcode bar with(nolock)  on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid and bar.barcodesetupid ='EANBottle'  and useforprinting = 1  " +
			  " left JOIN inventtablemodule mod with(nolock) ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
			  " WHERE  inv.dataareaid='spil'    AND mod.dataareaid='spil'   " +
			  "  AND moduletype=2 and active = 1  and inv.ITEMID not like  'AM%' and inv.ITEMID not like  'NE%' and inv.onlyretail = 0 " +
			  "  order by inv.itemid, disc.module , disc.todate desc ";
    
    
    TreeMap products = new TreeMap();
    
    try { 

    	
 this.getSaveStockInfo();
Connection con = this.getNewConnection();


    
     if (isDebug()) System.out.println(cmd);
 

    	Statement stmt =  con.createStatement();
    	
    	String pos = " select itemid as toode from inventTable where dataareaid = 'spil' and itemid like '60%' and spialwaysexporttoms = 1 ";
    	  HashSet posmust = new HashSet();
    	stmt =  con.createStatement();
    	ResultSet rs = stmt.executeQuery(pos);

    	while(rs.next()){
    	    String kood = rs.getString("toode").trim();
    	    posmust.add(kood);
    	}
    	rs.close();
    	stmt.close();
    	
    	stmt =  con.createStatement();
      rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus =  " " +rs.getString("nimetus").trim() + " " + kood ;
 
     nimetus =  Converter.enCodeEst(nimetus.replaceAll("bottle", "bot.").replaceAll("Reserve", "Res.")
     		.replaceAll("Premium", "Prem.")
     		.replaceAll("Chateau", "Cha.").replaceAll("Stolichnaya", "Stoli.")
     		.replaceAll("Riserva", "Ris.").replaceAll("Classic", "Cla.").replaceAll("Collection", "Col.")
     		.replaceAll("Chablis", "Cha.")
     		.replaceAll("Chardonnay", "Char.").replaceAll("Cabernet", "Cab.").replaceAll("Sauvignon", "Sauv.").replaceAll("Rouge", "Rou.").replaceAll("Blanc", "Bla."));
  
        prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        boolean goes = true;
        
        double price = rs.getDouble("price"); 
        if(price > 0){
            prod.setPrice(price);
        }  else if(!prod.getID().startsWith("60")){
     		 //  goes = false;
        }
        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        if(ean.length() >4){
            add = " " +  ean.trim().substring(ll-4);	
        }
      }
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
       
    
       
       
   
  
       String ette = "";
       if(kogused.get(kood) != null){
    	  
    	   double lseis = Double.parseDouble(kogused.get(kood)+"");
    	   if(lseis > 0){
    		   prod.setStock((int)lseis);
    		   if(prod.getID().startsWith("60")){
    			   ette = "xx "; 
    		   }
    	   }
    	   }else if(prod.getID().startsWith("60") && !posmust.contains(prod.getID())){
    		   goes = false;
    	   }else{
    		   prod.setStock(0); 
    	   }
      
       
       //lisa kilo hind 
       
      prod.setName(Converter.enCodeEst(ette + nimetus + add));

     
      
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("21");
    }
  
      String alegr = rs.getString("alegr");
      if(alegr!=null && alegr.trim().length()>0){
       prod.setAlegrupp(alegr);
    }
   
      String type = rs.getString("type");
      if(type!=null && type.trim().length()>0){
       prod.setType(type);
    }

      int mini = rs.getInt("mini");
      int opti = rs.getInt("opti");
      if(opti != mini && mini > 1){
    	//  prod.setMinAmountInUnits(mini);
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
    	//  prod.setStatusTop("1");
      }
      

      int giftitem = rs.getInt("gift");
      if(giftitem == 1){
    	  prod.setStatusGiftPack("1");
      }
      
    
    

      int  nocount = rs.getInt("nocount");
      if(nocount == 1){
    	 prod.sumNoCount = true;
      }
      
      double small = rs.getDouble("small");
      if(small < 0.21 && small > 0.005){
    	  prod.statusSmallPack = "1";
      }

      String pant = rs.getString("pant");
      if(pant != null && pant.trim().length() > 0){
    	 prod.setPandPakendiKood(pant.trim());
      }
      
      if(goes){
     	  products.put(Converter.cleanLitLatEst(ette) + nimetus, prod);
          prodHash.put(prod.getID(), prod);   
    
      }
       

      }
      rs.close();
      stmt.close();
      con.close();
      
      

      cmd = "  select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind , fromqty as kogus ,case when qtyprice >0 then(1-l.price/qtyprice)*100 else 0 end as perc,linedisc "+
      " from astPromoGMLine l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  " +
      " where l.dataareaid = 'spil' and (GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) " +
		"  AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 2 else 0 end)) " +
	   "  and accepted = 2 and promostatus in (1,4) and maxdiscountpromo = 1  ";
      
	    StringBuffer buf = new StringBuffer();
		try {
			
			 con = this.getNewConnection();
		     stmt =  con.createStatement();
		      rs = stmt.executeQuery(cmd);

	           while(rs.next()){
	        	        String  gruppx = rs.getString("grupp").trim();
	                  	String  toodex = rs.getString("toode").trim();
	                  	try{
	                  		double  hind = ((Product)prodHash.get(toodex)).getPrice();
		                  	double  kogus = rs.getDouble("kogus");
		                  	double  perc = rs.getDouble("perc");
		                  	double  lined = 0;// rs.getDouble("linedisc");
		                  	
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
    		os.write(Converter.cleanLitLatEst(buf.toString()));
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
    
    makeTexts();
      
      
      
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }


private void makeTexts() throws Exception{
	  
	  String    sql = " SELECT inv.itemid as kood,  itemfreetextannotation  as asi FROM inventtable inv  " +  
" WHERE  inv.dataareaid='spil'  and itemfreetextannotation !='' and  active = 1  " +
" and inv.ITEMID not like  'AM%' and inv.ITEMID not like  'NE%'";
	  Connection con =  this.getNewConnection();
  Statement stmt = con.createStatement();
   ResultSet rs = stmt.executeQuery(sql);
    StringBuffer sb = new StringBuffer();
   while (rs.next()) {
     String kood=rs.getString("kood").trim();
    	 String nimetus=rs.getString("asi");
    			 if(nimetus != null){
    				 nimetus = nimetus.trim().replaceAll("\\r\\n|\\r|\\n", "|").replaceAll(";", ":"); 
    				   sb.append(kood + ";" + nimetus + "\n");
    			 }
      
     
     }
   rs.close();
   stmt.close();
   con.close();
     Converter.saveFileInUTF8("texts.txt", sb);

  }
  


  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();

	 int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok  " +
	   "    FROM custinvoicetrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid   " + 
	   "    WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid ='spil'  AND jou.dataareaid = 'spil'   " +
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

	     String cmd =" select spitelesalesagentgroupid as agent , itemsubgroup as grupp , salesamount as planx,focussubgroup "
			    		+ " from ASTTeleSalesAgentForecast where year(transdate)=year(getdate()) " +
                          " and  month(transdate) = month(getdate()) and dataareaid = 'spil' ";

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
 	 int yy = Calendar.getInstance().get(Calendar.YEAR);
     int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
     int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

      
      String cmd ="   SELECT  cus.SPIEAGENTACCOUNTID as agent,  " +
     "  round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) then lineamount else 0  end),0) AS summa, " +
	 "	 round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate())-1 then lineamount else 0  end),0) AS summalast " +  
     "    FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
     "    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
	 "	 WHERE inv.dataareaid='spil' AND sal.dataareaid='spil' AND cus.dataareaid='spil'  AND jou.dataareaid='spil' AND ((sal.invoicedate >= DATEADD(month, DATEDIFF(month, 0, getdate()), 0) \r\n" + 
	 "     AND sal.invoicedate <  DATEADD(month, DATEDIFF(month, 0, getdate())+1, 0) )\r\n" + 
	 "       OR (sal.invoicedate >= DATEADD(month, DATEDIFF(month, 0, getdate())-12, 0)  \r\n" + 
	 "    AND sal.invoicedate <  DATEADD(month, DATEDIFF(month, 0, getdate())-12+1, 0) ))    " + 
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
                   agent.setStart(Converter.enCodeEst("Tekošais mēnesis, pagājušais gads :|") + summalast + "€|" );  
                   
                   agent.buffer = (int)(sales);

             	  double msales = agent.buffer;
         		  double psales = 0;
         		try{
         		  psales = Double.parseDouble(plans.get(agent.getID())+"");
         		  double  tehtudsalperc = msales/psales;
             	  double plaanitavMyykSal = d2 * msales/dd;
             	  double  prognosalperc = plaanitavMyykSal/psales;
               String asi = Converter.enCodeEst("Pārdošana: ") + (int)msales + " €|";
               if(dd > 6){
             	  asi = asi + Converter.enCodeEst("Plāna izpilde/Prognoze :");
             	  if(tehtudsalperc > 3){
                 		tehtudsalperc = 0;
                 	  }
                 	if(prognosalperc > 3){
                 		prognosalperc = 0;
                 	  }
                   asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
               }
               
                  asi = asi + Converter.enCodeEst("Pārdošanas plāns: ") + (int)psales + " €|";    
                agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
               // System.out.println(Converter.enCodeEst(agentID + "' " +asi  + " "  + plans.get(agent.getID())) );
                 
         		}catch(Exception e){
         		//  e.printStackTrace();
         	  }
         	     }
                  }
                rs.close();
                stmt.close();
                
                
                
                 cmd ="     SELECT  cus.SPIEAGENTACCOUNTID as agent,  SUM(lineamount ) AS summa, inv.ITEMSUBGROUPID as grupp , ITEMGROUPID as type " +
          	     "  FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
                 "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
          	 " WHERE inv.dataareaid='spil' AND sal.dataareaid='spil' AND cus.dataareaid='spil'  AND jou.dataareaid='spil' AND "
          	 + " MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) " +
              "      and  cus.inventlocation != '' and inv.ITEMGROUPID not in  ('TARA','POS') and cus.SPIEAGENTACCOUNTID != '' GROUP BY cus.SPIEAGENTACCOUNTID ,ITEMSUBGROUPID,ITEMGROUPID";

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
                   		  double psales = 0;
                   		try{
                   		  psales = Double.parseDouble(plans.get(agent.getID()+"|"+ grupp+"|"+type)+"");
                   		 if(psales > 0){
                   			
                   		  double  tehtudsalperc = msales/psales;
                       	  double plaanitavMyykSal = d2 * msales/dd;
                       	  double  prognosalperc = plaanitavMyykSal/psales;
                         String asi = " | " + grupp +"-"+type + " " + Converter.enCodeEst("pārdošana: ") + (int)msales + " €|";
                         asi = asi +grupp +"-"+type + " " + Converter.enCodeEst("plāns: ") + (int)psales + " €|";    
                         
                         if(dd > 6){
                       	  asi = asi + Converter.enCodeEst("Plāna izpilde/Prognoze :");
                     	  if(tehtudsalperc > 3){
                         		tehtudsalperc = 0;
                         	  }
                         	if(prognosalperc > 3){
                         		prognosalperc = 0;
                         	  }
                             asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                         }
                         
                          agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
                          //System.out.println(Converter.enCodeEst(agentID + "gg' " +asi) );
                          
                   		 }
                   		}catch(Exception e){
                   		 // e.printStackTrace();
                   	  }
                   	     }
                            }
                          rs.close();


                          
                          cmd ="     SELECT  cus.SPIEAGENTACCOUNTID as agent,  SUM(lineamount ) AS summa, inv.ITEMSUBGROUPID as grupp  " +
                   	     "  FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
                          "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock)ON cus.accountnum = jou.orderaccount " +
                   	 " WHERE inv.dataareaid='spil' AND sal.dataareaid='spil' AND cus.dataareaid='spil'  AND jou.dataareaid='spil' AND "
                   	 + " MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) " +
                       "      and  cus.inventlocation != '' and inv.ITEMGROUPID not in  ('TARA','POS') and cus.SPIEAGENTACCOUNTID != '' GROUP BY cus.SPIEAGENTACCOUNTID ,ITEMSUBGROUPID";

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
                            		 if(psales > 0){
                            			
                            		  double  tehtudsalperc = msales/psales;
                                	  double plaanitavMyykSal = d2 * msales/dd;
                                	  double  prognosalperc = plaanitavMyykSal/psales;
                                  String asi =  " | " + grupp  + " " + Converter.enCodeEst("pārdošana: ") + (int)msales + " €|";
                                  asi = asi + grupp + " " + Converter.enCodeEst("plāns: ") + (int)psales + " €|";    
                                  
                                  if(dd > 6){
                                	  asi = asi + Converter.enCodeEst("Plāna izpilde/Prognoze :");
                                 	  if(tehtudsalperc > 3){
                                     		tehtudsalperc = 0;
                                     	  }
                                     	if(prognosalperc > 3){
                                     		prognosalperc = 0;
                                     	  }
                                      asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                                  }
                                  
                                   agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
                                 //  System.out.println(Converter.enCodeEst(agentID + "jj' " +asi  + " " ) );
                                   
                            		 }
                            		}catch(Exception e){
                            		 // e.printStackTrace();
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

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT sal.invoiceid AS arve,ledgervoucher as num , sal.invoicedate AS kuupaev,cus.name AS klient, invoiceamount AS summa  " +
	"	FROM custinvoicejour sal INNER JOIN custtable cus ON sal.orderaccount=cus.accountnum  and cus.dataareaid= sal.dataareaid " +
	"	WHERE sal.dataareaid='ade' AND cus.dataareaid='ade' AND cus.SPIEAGENTACCOUNTID ='" + agent.getID()+ "' AND " +
	"   sal.invoicedate > getdate()-0.8    ORDER BY sal.invoicedate " ;
			     
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
	          sb.append("Nimi  \t\tLadu\t\tMüüdud\t\tSumma\n");
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
	          sb.append("\tPäeva müük kokku: " + totalKokku + " EUR");

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
  private String url = "jdbc:odbc:amberbev";
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
    
    
	
	  private void makePictureCat(){
	  	    
	    	
	    	String pictssou = parent.getProps().getProperty("pict.dir");
	  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	  	    String pictsean = parent.getProps().getProperty("pictean.dir");
	       
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
	             	if( ff2[i].startsWith(toode)){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
              	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                    	}
              	    if(prod.getEAN() != null && prod.getEAN().length() > 5 && inpda.exists()){
              	  	File out = new File ( pictsean + prod.getEAN()+ ".jpg"); 
              	    	Converter.copyFile(inpda, out);
     	            }
              	    	
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
