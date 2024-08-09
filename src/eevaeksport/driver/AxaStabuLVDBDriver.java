package eevaeksport.driver;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.json.JSONObject;

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
 * get
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaStabuLVDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker,FixReporter {
  public AxaStabuLVDBDriver(EksportPanel parent){
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
    
   
    if("true".equals(getParent().getProps().getProperty("testing"))) {
    	 url = "jdbc:odbc:test";
    }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    rcompanyx = getParent().getProps().getProperty("rest.companyx");
    nodebt = new Vector();
    
   String nd = getParent().getProps().getProperty("nodebt.list");
   if(nd != null){
	   StringTokenizer st = new StringTokenizer(nd,",");
	   while(st.hasMoreTokens()){
		   nodebt.add(st.nextToken());
	   }
   }
    
  }

  String user =  null;
  String pass = null;
  String rcompany = null;
  String rcompanyx = null;
  Vector nodebt = null;
  
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
	  getFixedAssortmentDataVec();
	  
	  
	
   	return null;
   }

  
  
  public Vector getFixedAssortmentDataVec() {
		Vector result = new Vector();
		
		File f = new File(this.getExcelDir() + "sortiments.xlsx");
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheet("Products");
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(2);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 3; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					String gg = row.getCell((short) a)
							.getStringCellValue().trim();
					if(gg.length() < 4){
						//gg = gg + "x";
					}
					if(gg.length() < 4){
						//gg = gg + "x";
					}
					groups.put(a + "", gg);
				} catch (Exception e) {
					
					
					//if(row != null && row.getCell((short)a) != null){
						// System.out.println(row.getCell((short)a).getNumericCellValue());
								
					//}
				}
			}

			// System.out.println("groups " + groups.size() + " " + groups.toString());

			for (int i = 4; i <= sheet.getLastRowNum(); i++) {
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

							if (go != null && ( go.toLowerCase().equals("1") || go.length() > 3)) {

							// System.out.println( a + " " + prodID + " " +
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

		System.out.println("fix size " + result.size());
		// System.exit(0);
		
		
		System.out.println("FIX valmis " + result.size());
		try {
			Converter.saveObject(result, "fix.vec");
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
		
		
		  
		  File fff = new File(this.getExcelDir() + "sortiments.xlsx" );
	      TreeMap groups = new TreeMap(); 
	  	try{
	          FileInputStream ff = new FileInputStream(fff);
	  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
	  		XSSFSheet sheet = workbook.getSheet("shops");
	  		XSSFRow row = sheet.getRow(0);
	  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
	  		
	  		int lopp =sheet.getLastRowNum();
	  		System.out.println("sheet "  + " " + sheet.getSheetName() + " " + lopp);
	  		for(int a = 1 ; a < lopp;a++){
	  			
	  		 
	  	      
	  			try{
	  			//	Loeme sisse rea
	  				row = sheet.getRow(a);
	  			//	System.out.println("lastrow error "  + " "+row.getCell((short) 0) + " "+row.getCell((short) 1));
	  				if (row != null && row.getCell((short) 0) != null) {
	  					
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
			    	    			  		try{
			    	    			  			sort = row.getCell((short)1).getNumericCellValue()+"";  
			    	    			  		}catch (Exception y){
			    	    			  		//	System.out.println("lastrow error "  + " " + a);
			    	    			  		}
			    	    			  		
			    	    	    		} 
		    	    			  	if(sort !=null){
		    	    			  	
			    	    			  	 if(sort.indexOf(".")!= -1){
			    	    			  	 	 sort = (sort.substring(0,sort.indexOf(".")));	
			    	    			  	 }
			    	    			  	 sort = sort.replace(',', '|').replace(',', '|').replace(',', '|').replace(',', '|');
			    	    			  	 
			    	    				String gg = sort.trim();
			    	 	
			    	 					if(gg.length() > 1){
			    	 						 groups.put(kood,gg);
			    	 					}
			    	 					
		    	    			  	}
	  	    			
	  	    				
	  				}	   
	  				}
	 			}catch (Exception e){
	  				
	  				e.printStackTrace();
	  			}
	  		}
	  		
	  		//System.out.println("Groups valmis " + groups.size() + " " + groups.get("LV9320-000"));
			try {
				Converter.saveObject(groups , "groups.vec");
			} catch (IOException e) {
			
				e.printStackTrace();
			}
			
	  
	  	 }catch (Exception e){
	  		e.printStackTrace();
	  	}
		
		
		
		
		
		
		
		return result;

	}



  public Vector getPriceMatrixData() {
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  Vector lines = new Vector();
      
	  
	   TreeMap yldorkla = new TreeMap();
	   TreeMap yldkond = new TreeMap();
	  
	  String cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
		       "  disc.DATAAREAID='ost' AND "+
		       "  disc.ACCOUNTCODE=1 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
	  
	  try {
	  Connection con = getNewConnection();
  	   Statement stmt = con.createStatement();

		            ResultSet  rs1 = stmt.executeQuery(cmd2);
		                     while(rs1.next()){
		                    	 String asi = rs1.getString("asi").trim();
		                       double ale = rs1.getDouble("discount");
		                       yldorkla.put(asi, ale);
		                     }
		                    rs1.close();
		                    
		                    
		                    cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
		             		       "  disc.DATAAREAID='ost' AND "+
		             		       "  disc.ACCOUNTCODE=0 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
		             	  
		                    rs1 = stmt.executeQuery(cmd2);
		                    	                     while(rs1.next()){
		                    	                    	 String asi = rs1.getString("asi").trim();
		                  		                       double ale = rs1.getDouble("discount");
		                  		                       yldorkla.put(asi, ale);
		                    	                     }
		                    	                    rs1.close();
		                    
	         stmt.close();
	         con.close();
	  

	        
	  
	  System.out.println("yldkond " + yldkond.size());

	  System.out.println("yldorkale " + yldorkla.size());
	  
	  
	  
	  //orkla aled 
	  
	  // võtame välja need mis on aktiivsed - kui on Klient ja kaup konkreetselt 
          String sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount  , MIN(PRICEDISCTABLE.PERCENT1) AS discount2  ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
		" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ost' AND PRICEDISCTABLE.RELATION=5 "
        + "AND PRICEDISCTABLE.ACCOUNTCODE=0 AND PRICEDISCTABLE.ITEMCODE=0 " +
        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE) or  " +
        " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
       "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

        
            con = getNewConnection();
            stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String prodID = rs.getString("prodid");
                          String custID = rs.getString("custid");
                          double discount = rs.getDouble("discount");
                          double discount2 = rs.getDouble("discount2");
                      
                          String algus = rs.getString("algus");
                          String lopp = rs.getString("lopp");
                          int paevi = rs.getInt("paevi");
                          MatrixLine mline = new MatrixLine();
                          
                    //MAKSJA JA TOODE
                          if (custID != null && custID.trim().length() > 0) {
                        	     mline.setCustID(custID.trim());
                                //  mline.setCliGroup(custID.trim());
                          }
                       
                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                     

                          if (discount != 0) {
                                  mline.setDiscount(discount);
                          }
                          if (discount2 != 0) {
                              mline.setDiscount(discount2);
                         }
                         
                          
                           if(paevi < 155 && paevi > 0){
                        	   if (algus != null && algus.trim().length() > 0) {
                                   mline.setStartDate(algus.trim().substring(0,10));
                           }
                               if (lopp != null && lopp.trim().length() > 0) {
                                   mline.setEndDate(lopp.trim().substring(0,10));
                           }  	  
                          }else{
                        	  mline.setProdLevel("pr");
                          }
                           if(mline.getDiscount() > 0)
                          lines.add(mline);
                  }
                  rs.close();
                  stmt.close();
                  con.close();
                  
                  //kliendi ALEGRUPP ja tootegrupp 
                  
                  TreeMap graled = new TreeMap();
                  
                  sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
                	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
                			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
                	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ost' AND "
                	        + "PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=1 " +
                	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE) or  " +
		                    " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
                	       "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

                	        
                	            con = getNewConnection();
                	            stmt = con.createStatement();
                	                rs = stmt.executeQuery(sql);

                	                  while (rs.next()) {
                	                	  String prodID = rs.getString("prodid");
                	                          String custID = rs.getString("custid");
                	                          double discount = rs.getDouble("discount");
                	                          double discount2 = rs.getDouble("discount2");
                	                      
                	                          String algus = rs.getString("algus");
                	                          String lopp = rs.getString("lopp");
                	                          int paevi = rs.getInt("paevi");
                	                          MatrixLine mline = new MatrixLine();
                	                          
                	                    //KLIENDIGRUPP JA TOOTEGRUPP
                	                          if (custID != null && custID.trim().length() > 0) {
                	                                  mline.setCliGroup(custID.trim());
                	                          }
                	                       
                	                          if (prodID != null && prodID.trim().length() > 0) {
                	                                  mline.setProdGroup(prodID.trim());
                	                          }
                	                     

                	                          if (discount != 0) {
                	                                  mline.setDiscount(discount);
                	                          }
                	                          if (discount2 != 0) {
            	                                  mline.setDiscount(discount2);
            	                             }
                	                         
                	                          
                	                           if(paevi < 155 && paevi > 0 ){
                	                        	   if (algus != null && algus.trim().length() > 0) {
                	                                   mline.setStartDate(algus.trim().substring(0,10));
                	                           }
                	                               if (lopp != null && lopp.trim().length() > 0) {
                	                                   mline.setEndDate(lopp.trim().substring(0,10));
                	                           }  	  
                	                          }else{
                	                        	  mline.setProdLevel("pr");
                	                          }
                	                           if(mline.getDiscount() > 0){
                	                        	   graled.put(mline.getCliGroup()+mline.getProdGroup(), mline.getDiscount());
                	                        	   lines.add(mline);
                	                           }
                	                         
                	                  }
                  
                	                  rs.close();
                	                  stmt.close();
                	                  con.close();                  
                  
                  System.out.println("graled " + graled.size());

                  
                  
                  
                //  kui on püsialed siis korjame info mapi 
                  
                  sql = " 	  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, \r\n" + 
                  		"                	         (PRICEDISCTABLE.PERCENT1) AS discount2  \r\n" + 
                  		"                	           FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ost' \r\n" + 
                  		"                	        AND PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=0 \r\n" + 
                  		"                	          AND  (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') )  \r\n" + 
                  		"							 and (PRICEDISCTABLE.PERCENT1) > 0\r\n" + 
                  		"							  order   BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION , (PRICEDISCTABLE.PERCENT2)desc, (PRICEDISCTABLE.TODATE)desc";
                 	        
              TreeMap<String,Double> pysialed = new TreeMap<String,Double>();
                 	            con = getNewConnection();
                 	            stmt = con.createStatement();
                 	                   rs = stmt.executeQuery(sql);

                 	                  while (rs.next()) {
                 	                	 String prodID = rs.getString("prodid");
            	                          String custID = rs.getString("custid");
            	                          double discount2 = rs.getDouble("discount2"); 
            	                          pysialed.put(custID + prodID, discount2);
                 	                  }
                 	                	 
                 	                 rs.close();
               	                  stmt.close();
               	                  con.close();
                  
                  //kliendi ALEGRUPP ja Toode 
                  
                  sql = " 	  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " +
                	     "   (PRICEDISCTABLE.PERCENT2) AS discount , (PRICEDISCTABLE.PERCENT1) AS discount2   ,(PRICEDISCTABLE.FROMDATE) as algus , (PRICEDISCTABLE.TODATE) as lopp , " +
                		"	 datediff( day,(PRICEDISCTABLE.FROMDATE) , (PRICEDISCTABLE.TODATE) )  as paevi " +
                	     "      FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ost' " +
                	     "   AND PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=0 " +
                	     "     AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE) or  " +
		                 "    (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
                	      "      order   BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION , (PRICEDISCTABLE.PERCENT2)desc, (PRICEDISCTABLE.TODATE)desc	";

                	        
                  HashSet deal = new HashSet();
                	            con = getNewConnection();
                	            stmt = con.createStatement();
                	                   rs = stmt.executeQuery(sql);

                	                  while (rs.next()) {
                	                	  String prodID = rs.getString("prodid");
                	                          String custID = rs.getString("custid");
                	                          double discount = rs.getDouble("discount");
                	                          double discount2 = rs.getDouble("discount2");
                	                      
                	                          String algus = rs.getString("algus");
                	                          String lopp = rs.getString("lopp");
                	                          int paevi = rs.getInt("paevi");
                	                          MatrixLine mline = new MatrixLine();
                	                          
                	                    //KLIENDIGRUPP JA TOODE
                	                          if (custID != null && custID.trim().length() > 0) {
                	                                  mline.setCliGroup(custID.trim());
                	                          }
                	                       
                	                          if (prodID != null && prodID.trim().length() > 0) {
                	                                  mline.setProdID(prodID.trim());
                	                          }
                	                     

                	                          if (discount != 0) {
                	                                  mline.setDiscount(discount);
                	                          }
                	                          
                	                          if (discount2 != 0) {
            	                                  mline.setDiscount(discount2);
            	                             }
                	                         
                	                          
                	                           if(paevi < 155 && paevi > 0){
                	                        	   if (algus != null && algus.trim().length() > 0) {
                	                                   mline.setStartDate(algus.trim().substring(0,10));
                	                           }
                	                               if (lopp != null && lopp.trim().length() > 0) {
                	                                   mline.setEndDate(lopp.trim().substring(0,10));
                	                           }  	  
                	                          }else{
                	                        	  mline.setProdLevel("pr");
                	                          }
                	                           if(mline.getDiscount() > 0){
                	                        	   if(graled.get(mline.getCliGroup()+ mline.getProdID().substring(0, 3)) != null){
                	                        		double di1 = Double.parseDouble(graled.get(mline.getCliGroup()+ mline.getProdID().substring(0, 3))+"");
                	                        		
                	                        		double di2 = mline.getDiscount();
                	                        		   mline.setDiscount(100-100*(100-di1)/100 * (100-di2)/100); 
                	                      // System.out.println("Sees Disc " + mline.getProdID() + " " + mline.getDiscount());
                	                        	   }
                	                        	   if(discount > 0  && pysialed.get(custID + prodID) != null) {
                	                        		   double di1 = pysialed.get(custID + prodID);
                   	                        		
                   	                        		double di2 = mline.getDiscount();
                   	                        		   mline.setDiscount(100-100*(100-di1)/100 * (100-di2)/100); 
                   	                      // System.out.println("Sees Disc " + mline.getProdID() + " " + mline.getDiscount());
               	                        		  }
                	                        	   
                	                        	   if(!deal.contains(custID + prodID)){
                	                        		   lines.add(mline);
                	                        		   deal.add(custID + prodID);
                	                        	   }
                	                           }
                	                  }
                  
                	                  rs.close();
                	                  stmt.close();
                	                  con.close();

                  
                  
                  
                  
                  
                  //kliendi klient ja tootegrupp 
                  
                  sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
                	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
                			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
                	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ost' AND "
                	        + "PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=0 AND PRICEDISCTABLE.ITEMCODE=1 " +
                	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE) or  " +
		                    " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
                	       "  and PRICEDISCTABLE.ACCOUNTRELATION like 'LV%'  GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

                	        
                	            con = getNewConnection();
                	            stmt = con.createStatement();
                	                rs = stmt.executeQuery(sql);

                	                  while (rs.next()) {
                	                	  String prodID = rs.getString("prodid");
                	                          String custID = rs.getString("custid");
                	                          double discount = rs.getDouble("discount");
                	                          double discount2 = rs.getDouble("discount2");
                    	                      
                	                          
                	                          String algus = rs.getString("algus");
                	                          String lopp = rs.getString("lopp");
                	                          int paevi = rs.getInt("paevi");
                	                          MatrixLine mline = new MatrixLine();
                	                          
                	                    //KLIENT JA TOOTEGrupp
                	                          if (custID != null && custID.trim().length() > 0) {
                	                                  mline.setCustID(custID.trim());
                	                          }
                	                       
                	                          if (prodID != null && prodID.trim().length() > 0) {
                	                                  mline.setProdGroup(prodID.trim());
                	                          }
                	                     

                	                          if (discount != 0) {
                	                                  mline.setDiscount(discount);
                	                          }
                	                          if (discount2 != 0) {
            	                                  mline.setDiscount(discount2);
            	                             }
                	                          
                	                           if(paevi < 155 && paevi > 0 ){
                	                        	   if (algus != null && algus.trim().length() > 0) {
                	                                   mline.setStartDate(algus.trim().substring(0,10));
                	                           }
                	                               if (lopp != null && lopp.trim().length() > 0) {
                	                                   mline.setEndDate(lopp.trim().substring(0,10));
                	                           }  	  
                	                          }else{
                	                        	  mline.setProdLevel("pr");
                	                          }
                	                           if(mline.getDiscount() > 0)
                	                          lines.add(mline);
                	                  }
                  
                  rs.close();                  
                  stmt.close();
                  con.close();

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
	  
	 // getPriceMatrixDataKond(lines);
	  System.out.println("PriceLines " + lines.size());
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
	  

String  cmd2 = "select  summ.itemid AS toode ,' ' as ladu ,SUM(AVAILPHYSICAL) AS stock \r\n" + 
		"		   FROM bmssa.INVENTSUM summ  with (nolock) \r\n" + 
		"            INNER JOIN bmssa.INVENTDIM dim   with (nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid \r\n" + 
		"           INNER JOIN bmssa.inventtablemodule inv  with (nolock) ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid          \r\n" + 
		"        WHERE  inv.DATAAREAID='ost' and CLOSED = 0 AND moduletype=0 \r\n" + 
		"		and (summ.itemid like 'V%' or summ.itemid like 'S%') and dim.inventlocationid in ('GPBLS-STAN')\r\n" + 
		"		GROUP BY summ.itemid having sum(availphysical) > 0 \r\n" + 
		"		order by  summ.itemid ";

  cmd2 = "select  summ.itemid AS toode ,' ' as ladu ,SUM(AVAILPHYSICAL) AS stock , dim.inventlocationid\r\n" + 
  		"				   FROM bmssa.INVENTSUM summ  with (nolock) \r\n" + 
  		"		            INNER JOIN bmssa.INVENTDIM dim   with (nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  \r\n" + 
  		"		           INNER JOIN bmssa.inventtablemodule inv  with (nolock) ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid           \r\n" + 
  		"		        WHERE  inv.DATAAREAID='ost' and CLOSED = 0 AND moduletype=0\r\n" + 
  		"				GROUP BY dim.inventlocationid,summ.itemid having sum(availphysical) > 0 \r\n" + 
  		"				order by  summ.itemid ";
	  

       StringBuffer products = new StringBuffer();
       
       try {
       
         Connection  con = this.getNewConnection();
         Statement   st = con.createStatement();
         ResultSet   rs = st.executeQuery(cmd2);

           while (rs.next()) {

        	String kood = rs.getString("toode").trim();
             if(kood.startsWith("NPF-")){
          	   kood = kood.substring(4);
             }
             
             double seis = rs.getDouble("stock");
             if(seis > 0){
          	   if(seis > 9999){
          		   seis = 9999;
          	   }
          	   
                 products.append(kood +  ";");
                 products.append(" ;");
                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
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

  
  
  
  public TreeMap getAgents(){
	  
	
	 
	  
    String cmd = "select agentsid as agent , name as nimi ,ocslassortment  as grupp, oflassortment  as grupp2 "
    		+ " from bmssa.traagentstable ag INNER JOIN bmssa.empltable emp ON ag.emplid = emp.emplid  and " + 
    		"	 ag.dataareaid =emp.dataareaid   where ag.dataareaid = 'ost'";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
   
  // this.getPriceMatrixData();
    try {
    
    	Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("asendajad.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Connection con = this.getNewConnection();
    	Statement stmt = con.createStatement();
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimi").trim();
          String gr = rs.getString("grupp").trim();
          String gr2 = rs.getString("grupp2").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          if (gr.equals("1")) {
        	//  agent.addProdGroupList("laima");
          }
          
          if(gr2.equals("1")) {
        	  //agent.addProdGroupList("spilva");
          }
          
          if((gr2.equals("1") && gr.equals("1")) || 
        		  (gr2.equals("0") && gr.equals("0")) ) {
        	  //agent.addProdGroupList("all");
          }
          
          agent.setLanguage("LAT");
          agent.setCountry("lv");
          agent.setPassWord("0000");
          
         
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
         
          
          agent.setActive(true);
  		String agentAGENTS = props.getProperty("dub." +agent.getID());
		if(agentAGENTS != null){
           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
	    while(stag.hasMoreTokens()){  	
         agent.addDubbler(stag.nextToken());
       //  System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
	       }
         }
		
		 agents1.put(agent.getID(), agent);
          agents.put(name+agent.getID(), agent);
       //   System.out.println(agents.toString());
        }
        rs.close();
        stmt.close();
        con.close();
    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent ("CHAIN");
    aa.setName("CHAIN");
    agents.put("CHAIN", aa);
    
   
	//	setBulkSales(agents);
	

    return agents;
  }

  private Vector fixed = null;
  private TreeMap groups = new TreeMap();
  
  
  public TreeMap getCustomers(Agent agent){
	  
	/*  try{
   	   Converter.getManagersReplacements(agents1, rcompanyx, user, pass); 
   		
   }catch(Exception e){
   //	e.printStackTrace();
   }*/
	  
	  try {
			fixed =  (Vector) Converter.readObject("fix.vec");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	  
	  try {
			groups =  (TreeMap) Converter.readObject("groups.vec");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	  
	  
	  TreeMap budgets = getCustPlans();
	  TreeMap displays = getCustDisplays();
	  
      
  	nomaxdisc = new ArrayList();
	  

  	String cmd =  
  			"SELECT cus.name,cus.accountnum,cus.invoiceaccount AS payer,   \r\n" + 
  			"  	  (SELECT sum(amountmst ) FROM bmssa.custtransopen co  with (nolock) WHERE co.duedate<(getdate()-1)  and  co.dataareaid = 'ost' and co.dataareaid=cus.dataareaid \r\n" + 
  			"  	  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  \r\n" + 
  			"  	  cus.invoiceaccount end )) AS debt,(SELECT sum(amountmst ) FROM bmssa.custtransopen co  with (nolock) WHERE  co.dataareaid = 'ost' and co.dataareaid=cus.dataareaid \r\n" + 
  			"  	  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else   \r\n" + 
  			"  	  cus.invoiceaccount end )) AS used, cus.linedisc ,   \r\n" + 
  			"  	  stbaddressdelivery as address, '' AS visit ,'' AS agent , \r\n" + 
  			"	  (case when cus.invoiceaccount != '' then (select pay.numofdays from  bmssa.paymterm \r\n" + 
  			"  	  pay where (select (paymtermid) from bmssa.custtable cc where cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ost'and cc.dataareaid=cus.dataareaid) = pay.paymtermid and pay.dataareaid='ost' and pay.dataareaid=cus.dataareaid ) \r\n" + 
  			"	  else numofdays end ) AS term   , \r\n" + 
  			"	   case when cus.invoiceaccount != '' then (select  (PaymTermIdBLS) from bmssa.custtable cc where cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ost' and cc.dataareaid=cus.dataareaid )  \r\n" + 
  			"      else PaymTermIdBLS end as term2 ,\r\n" + 
  			"	   blocked AS block ,   \r\n" + 
  			"  	  case when cus.invoiceaccount != '' then (select max (blocked) from bmssa.custtable cc where cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = 'ost')  \r\n" + 
  			"      else cus.blocked end as payerblock ,phone + ', ' + CellularPhone as info , PriceGroup as grupp , email as mail, '' as abiagent, '' \r\n" + 
  			"  	  as abipaev ,\r\n" + 
  			"	  case when cus.invoiceaccount != '' then (select  (creditmax) from bmssa.custtable cc where cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ost' and cc.dataareaid=cus.dataareaid )  \r\n" + 
  			"      else creditmax end \r\n" + 
  			"	   as credit ,stbcustchainid as chain , TraCatalogueCode as fixed , transportthreshold as minam \r\n" + 
  			"  	  , case when cus.invoiceaccount != '' then (select max (PaymMode) from bmssa.custtable cc where cc.invoiceaccount=cus.invoiceaccount  and cc.dataareaid = 'ost')\r\n" + 
  			"	   else cus.PaymMode end as ette\r\n" + 
  			"  	  , cb2.LVDISCNOTALLOW as nomax ,  paymtermidstk as sdkterm ,cus.recid as refe, cus.supplyrouteid as refe2, cus.traregionid as refe3 FROM bmssa.custtable cus \r\n" + 
  			"	  left outer JOIN bmssa.paymterm \r\n" + 
  			"  	  pay on cus.paymtermid=pay.paymtermid and pay.dataareaid='ost'  and pay.dataareaid = cus.dataareaid\r\n" + 
  			"	  left outer JOIN bmssa.stbcustgrouptable  cb on cb.custgroupid=cus.stbcustgroupid and cb.dataareaid=cus.dataareaid \r\n" + 
  			"	  left outer JOIN bmssa.stbcustchaintable  cb2 on cb2.custchainid=cus.stbcustchainid and cb2.dataareaid='ost' and cb2.dataareaid=cus.dataareaid\r\n" + 
  			"  	  WHERE cus.dataareaid='ost' \r\n" + 
  			"  	  order by cus.name ";
  	
 
    String merc = "  SELECT  custaccount as klient,agent   " + 
     "   FROM bmssa.TraTradeCustTable   WHERE  dataareaid='ost'  and agent != '' " ;

    String ags = "  SELECT  custaccount as klient,agent    \r\n" + 
    		"        FROM bmssa.TRAAGENTCUSTTABLE   WHERE  dataareaid='ost'  and agent != '' " ;
  	
    
    String salsesqluus = " SELECT  jou.orderaccount  as klient, sum(  round((lineamount),0)) AS summa, \r\n" + 
    		"      str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) as kuu  \r\n" + 
    		"      FROM bmssa.custinvoicetrans sal  with (nolock) INNER JOIN bmssa.custinvoicejour jou  with (nolock) ON  sal.invoiceid = jou.invoiceid  and  \r\n" + 
    		"      jou.DATAAREAID=sal.DATAAREAID WHERE   \r\n" + 
    		"	  sal.dataareaid='ost' AND sal.invoicedate > getdate() -488 \r\n" + 
    		"	  GROUP BY jou.orderaccount,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate))\r\n" + 
    		"	  union all\r\n" + 
    		" SELECT  jou.orderaccount  as klient, sum(  round((lineamount),0)) AS summa, \r\n" + 
    		"      str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) as kuu  \r\n" + 
    		"      FROM  Axdb_O.bmssa.custinvoicetrans sal  with (nolock) INNER JOIN Axdb_O.bmssa.custinvoicejour jou  with (nolock) ON  sal.invoiceid = jou.invoiceid  and  \r\n" + 
    		"      jou.DATAAREAID=sal.DATAAREAID WHERE   \r\n" + 
    		"	  sal.dataareaid='ork' AND sal.invoicedate > getdate() -488  \r\n" + 
    		"	   GROUP BY jou.orderaccount,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) "; 
	
    
    
    
 String ccc = "select invoiceaccount  from bmssa.custtable where dataareaid='ost' group by invoiceaccount";


 
 String sql = "	SELECT  a.accountnum AS klient,a.orderaccount as saaja,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +  
 " co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM bmssa.custtransopen co  with (nolock)  " +
 " INNER JOIN bmssa.custtrans a  with (nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
 " WHERE  co.dataareaid='ost' AND co.dataareaid='ost' " +
 " and a.invoice != '' AND co.duedate < getdate()+5 ORDER BY a.accountnum,co.transdate  ";
 
 
 String deliv = "	SELECT   CAST(refrecid AS varchar(12)) AS klient, TRALOGISTICSPARTNERID+'' as par, case when MONDAY = 1 then '.1' else '' end +\r\n" + 
 		" 		 case when TUESDAY = 1 then '.2' else '' end + \r\n" + 
 		" 	case when WEDNESDAY = 1 then '.3' else '' end + \r\n" + 
 		" 		case when THURSDAY = 1 then '.4' else '' end + \r\n" + 
 		" 		case when FRIDAY = 1 then '.5' else '' end +\r\n" + 
 		" 		case when SATURDAY = 1 then '.6' else '' end + case when SUNDAY = 1 then '.7' else '' end +\r\n" + 
 		" 		 case when TraServiceType = 1 then '-24H'  when TraServiceType = 2 then '-48H' end as dely \r\n" + 
 		" 		FROM bmssa.TraDeliveryTime co where  co.dataareaid='ost' and TRALOGISTICSPARTNERID like 'BLS%' 	\r\n" + 
 		"		union\r\n" + 
 		"	 SELECT  CAST(supplyrouteid AS varchar(12)) AS klient, 'STK' as par, case when MONDAY = 1 then '.1' else '' end +\r\n" + 
 		" 		 case when TUESDAY = 1 then '.2' else '' end + \r\n" + 
 		" 	case when WEDNESDAY = 1 then '.3' else '' end + \r\n" + 
 		" 		case when THURSDAY = 1 then '.4' else '' end + \r\n" + 
 		" 		case when FRIDAY = 1 then '.5' else '' end +\r\n" + 
 		" 		case when SATURDAY = 1 then '.6' else '' end + case when SUNDAY = 1 then '.7' else '' end  as dely \r\n" + 
 		" 		FROM bmssa.supplyroutetable co where  co.dataareaid='ost' \r\n" + 
 		"	union	\r\n" + 
 		"		SELECT  CAST(reg.regionid AS varchar(12)) AS klient, 'CS' as par, case when MONDAY = 1 then '.1' else '' end +\r\n" + 
 		" 		 case when TUESDAY = 1 then '.2' else '' end + \r\n" + 
 		" 	case when WEDNESDAY = 1 then '.3' else '' end + \r\n" + 
 		" 		case when THURSDAY = 1 then '.4' else '' end + \r\n" + 
 		" 		case when FRIDAY = 1 then '.5' else '' end +\r\n" + 
 		" 		case when SATURDAY = 1 then '.6' else '' end + case when SUNDAY = 1 then '.7' else '' end +\r\n" + 
 		" 		 case when TraServiceType = 1 then '-24H'  when TraServiceType = 2 then '-48H' end as dely \r\n" + 
 		" 		FROM bmssa.TraDeliveryTime co INNER JOIN bmssa.TraRegionTable reg on reg.recid = co.refrecid  where  co.dataareaid='ost' and reg.dataareaid='ost' \r\n" + 
 		"		and TRALOGISTICSPARTNERID not like 'BLS%'";
 
 
    TreeMap deliveries = new TreeMap();
    TreeMap alekesed = new TreeMap();
    TreeMap pds = new TreeMap();
 String cmd2 =  "  SELECT disc.percent1 AS discount,disc.accountrelation as aleke FROM bmssa.pricedisctable disc  WHERE  " +
	       "  disc.DATAAREAID='ost' AND disc.ACCOUNTCODE=1 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR' and "
	       + "((disc.fromdate<getdate() and disc.todate=('1900-01-01 00:00:00.00') ) or  getdate()"
	       + " between disc.fromdate and disc.todate)";

 
 try{
	 Connection cc = getNewConnection();
	 Statement stm = cc.createStatement();
     ResultSet rs1 = stm.executeQuery(cmd2);
              while(rs1.next()){
                double ale = rs1.getDouble("discount");
                String aleke = rs1.getString("aleke");
                alekesed.put(aleke, ale);
              }
             rs1.close();
             
             
             
             rs1 = stm.executeQuery(deliv);
             while(rs1.next()){
            
               String klientref = rs1.getString("klient");
               String par = rs1.getString("par");
               String dely = rs1.getString("dely");
               if(deliveries.get(klientref) != null) {
            	   deliveries.put(klientref, deliveries.get(klientref) + "|" + par + " : " + dely);
               }else {
            	   deliveries.put(klientref, par + " : " + dely);
               }
             }
           
             rs1.close();
             
             cmd2 =  "  SELECT disc.percent1 AS discount ,disc.accountrelation as payer FROM bmssa.pricedisctable disc  "
             		+ "WHERE  disc.DATAAREAID='ost' AND "+
          	       "  disc.ACCOUNTCODE=0 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR' and " +
"((disc.fromdate<getdate() and disc.todate=('1900-01-01 00:00:00.00') ) or  getdate()"
+ " between disc.fromdate and disc.todate)";

          	             rs1 = stm.executeQuery(cmd2);
          	                     while(rs1.next()){
          	                       double ale = rs1.getDouble("discount");
          	                       String payer = rs1.getString("payer");
          	                       pds.put(payer.trim(), ale);
          	                     }
          	                    rs1.close();
          	                  stm.close();
          	                cc.close();
 }catch(Exception u){
	 
 }
 
	                    
 
           
       //    tal_visitationdays ,talspartnercode,

 // if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    Calendar tt = Calendar.getInstance();
    String yearthis = tt.get(Calendar.YEAR)+"";
    String yearlast = (tt.get(Calendar.YEAR)-1)+"";
    String  currmonth = (tt.get(Calendar.MONTH)+1)+"";
    tt.add(Calendar.MONTH, -1);
    String  lastmonth = (tt.get(Calendar.MONTH)+1)+"";
    tt.add(Calendar.MONTH, -1);
    String  prelastmonth = (tt.get(Calendar.MONTH)+1)+"";
    
    HashSet luba = new HashSet();
    if((currmonth).length() == 1){
    	currmonth = "0" + currmonth;
	}
    if((lastmonth).length() == 1){
    	lastmonth = "0" + lastmonth;
	}
    if((prelastmonth).length() == 1){
    	prelastmonth = "0" + prelastmonth;
	}
    luba.add(yearthis+currmonth );
    luba.add(yearthis+lastmonth );
    luba.add(yearthis+prelastmonth );
    luba.add(yearlast+currmonth );
    luba.add(yearlast+lastmonth );
    luba.add(yearlast+prelastmonth );
    
    HashMap salesres = new HashMap();
    
    	
    	Statement stmr ;
    	try{
    		
    	Connection con = this.getNewConnection(); 
    		 stmr = con.createStatement();
        	Calendar cc = Calendar.getInstance();
        	ResultSet rs1 = stmr.executeQuery(salsesqluus);
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim();
            	int summa = (int)rs1.getDouble("summa");
            	int summa2 = 0;
            	String aeg = rs1.getString("kuu").trim();
            	String kuu = aeg.substring(4).trim() ;
            	if(kuu.length() == 1){
            		kuu = "0" + kuu;
            	}
                 if(luba.contains(aeg.substring(0, 4) +  kuu)){
                  if(salesres.get(klient) == null){
		        	  salesres.put(klient,"   " + aeg.substring(0, 4) +  kuu + " : " + summa + "," + summa2 + " " );
		          }else{
		        	  String asi = (String)salesres.get(klient);     	  
		        	  salesres.put(klient,"   " + aeg.substring(0, 4) + kuu + " : " + summa + "," + summa2 + " |" + asi);
		          } 
                 }
            }
           rs1.close();
           stmr.close();
           con.close();
    	}catch(Exception s){
    		s.printStackTrace();
    	}
    	
    	HashMap<String,ArrayList> mercs = new HashMap<String,ArrayList>();
    try{
    	Connection con = this.getNewConnection(); 
		 stmr = con.createStatement();
   	
   	ResultSet rs1 = stmr.executeQuery(merc);
       while(rs1.next()){
       	String klient = rs1.getString("klient").trim();
    	String ags1  = rs1.getString("agent").trim();
    	ArrayList aa = mercs.get(klient);
    	if(aa == null) {
    		aa = new ArrayList();
    	}
    	aa.add(ags1);
    	
       	mercs.put(klient, aa);
       	
       }
      rs1.close();
      stmr.close();
      con.close();
	}catch(Exception s){
		s.printStackTrace();
	}
   
   
   //System.out.println("mercs size " + mercs.size());
    
	HashMap<String,ArrayList> agss = new HashMap<String,ArrayList>();
    try{
    	Connection con = this.getNewConnection(); 
		 stmr = con.createStatement();
   	
   	ResultSet rs1 = stmr.executeQuery(ags);
       while(rs1.next()){
       	String klient = rs1.getString("klient").trim();
    	String ags1  = rs1.getString("agent").trim();
    	ArrayList aa = agss.get(klient);
    	if(aa == null) {
    		aa = new ArrayList();
    	}
    	aa.add(ags1);
    	agss.put(klient, aa);
       	
       }
      rs1.close();
      stmr.close();
      con.close();
	}catch(Exception s){
		s.printStackTrace();
	}
    
  //  System.out.println("agss size " + agss.size());
    
    
    try {
    	Connection con = this.getNewConnection();
    	 stmr = con.createStatement();
    	
    	 ResultSet  rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
         payers.add(rs1.getString("invoiceaccount"));
        }
  
        rs1.close();
        stmr.close();
    	 con = this.getNewConnection();
    	Statement stmt =con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      
      while(rs.next()){
        String name = (rs.getString("name").trim()) ;
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        
        if(salesres.get(accountnum) != null){
        	cust.addToCustDataVec(cust.getID()+ ";sales2years;chart;" 
        + "CHART SALES :|Month|" + yearthis + "|" + yearlast + "|Eur.|Eur.|0|0|" + salesres.get(accountnum));
        }
       
        
        if(budgets.get(accountnum) != null){
        	cust.addToCustDataVec(accountnum+ ";custbudget; ;" + budgets.get(accountnum));
        }
        
   
        
        if(displays.get(accountnum) != null){
        	String aa = "  ";
        	Vector vv = (Vector)displays.get(accountnum);
        	for(int i = 0;i< vv.size();i++){
        		aa = aa+ vv.elementAt(i)+"|  ";
        	}
        	cust.addToCustDataVec(accountnum+ ";questinfo; ;" + aa);
        	
        	
        }
        
        if(salesres.get(accountnum) != null){
        	//cust.setInfo("CHART Sales :|Year Month|Sales EUR|" + salesres.get(accountnum) );
        }
        
        
        boolean addDebt = true;
        
        
       
        
        cust.restName = name;
        
        name = Converter.enCodeEst(name);
        name = name + " " + accountnum;
        cust.setName(name);
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	cust.setPayer(payer.trim());
        }else{
        	cust.setPayer(accountnum);
        }
        if(nodebt.contains(cust.getPayer())){
        	addDebt = false;
        }
 
        String debtStr = rs.getString("debt");
        if (debtStr != null && addDebt){
          cust.setDebt((int)Double.parseDouble(debtStr));
        }
        
        String usedStr = rs.getString("used");
        if (usedStr != null && addDebt){
          cust.setUsedCredit((int)Double.parseDouble(usedStr));
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
        	String asi = aadr.trim();
        	   int koma = asi.lastIndexOf(",");
               if(koma != -1){
            	   String indeks = asi.substring(koma+1);
                   
                   if (indeks != null && indeks.length() > 0){
                     cust.restZipCode = indeks.trim();
                   }
                   asi = asi.substring(0, koma);
                   koma = asi.indexOf(",");
                   if(koma != -1){

                   	String tanav = asi.substring(0,koma);
                       if (tanav != null && tanav.length() > 0){
                         cust.restStreet = tanav.trim();
                       }    	   
                       asi = asi.substring(koma+1);
                       
                     try{
                    	 String linn = asi.substring(0 ,asi.length()-1);
                         if (linn != null && linn.length() > 0){
                           cust.restCity = linn.trim();
                         } 
                     }catch(Exception k){
                    	 
                     }
                   }
               }
              
        	
        	
        	 name = name + " " + Converter.enCodeEst(aadr.trim().replace('\n',' '));
           cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Latvia").
        		  replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        cust.setName(name);
        
      
        
        if(mercs.get(cust.getID()) != null){
        	
        	ArrayList  agen =mercs.get(cust.getID());
        	for(int u =0 ; u < agen.size();u++) {
        		String agge = (String)agen.get(u);
        		cust.addAgent(new Agent(agge));
           		Agent agg = ((Agent)this.getAgents1().get(agge));
    				if(agg != null && agg.getDubbler().size()>0){
    					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
    						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
    					}
    				}
    				
        	}
        	
        	
        }
        if(agss.get(cust.getID()) != null){
        	ArrayList  agen =agss.get(cust.getID());
        	for(int u =0 ; u < agen.size();u++) {
        		String agge = (String)agen.get(u);
        		cust.addAgent(new Agent(agge));
           		Agent agg = ((Agent)this.getAgents1().get(agge));
    				if(agg != null && agg.getDubbler().size()>0){
    					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
    						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
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
        	cust.addCity("CF Apmaksas : " + term.trim());
        }
        
        
        String term2 = rs.getString("term2");
        if (term2 != null && term2.trim().length() > 0 ) {
			cust.addCity("OF Apmaksas : " + term2.trim().substring(1));
		}
        
        int block = rs.getInt("block");
        if (block == 1 ) {
          cust.setSalesBlock(true);
        }
        
        if ( block == 2) {
           // cust.setSalesBlockT(true);
          }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }
        
        if (payerblock == 2) {
          //  cust.setSalesBlockT(true);
          }
        
        String info = rs.getString("info");
        if (info != null && info.trim().length()> 2) {
           if (info.trim().endsWith(",")){
        	   info = info.substring(0,info.length()-1);
           }
           if (info.trim().startsWith(",")){
        	   info = info.substring(1);
           }
        	cust.setPersonalData("Tel:"+ info.trim());
        }
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0) {
        	if(cust.getGroup()!= null && cust.getGroup().trim().length()> 0){
        		cust.setGroup(cust.getGroup() + "|" + grupp.trim());
        	}else{
        		cust.setGroup(grupp.trim());
         	}
        }
        
     
        
        String confmail = rs.getString("mail");
        if (confmail != null && confmail.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + confmail.trim());
        	}else{
        		 cust.setPersonalData(confmail.trim());
        	}
        }
        
        
 
        
        
       double  credit = rs.getDouble("credit");
       if(credit != 0){
    	   cust.setCredit(((int)credit)+"");
       }
         
       
       String chain = rs.getString("chain");
		if (chain != null && chain.trim().length() > 0) {
			if(!chain.trim().startsWith("Hinnakiri"))
			cust.setChain(Converter.enCodeEst(chain.trim().replaceAll("&", "ja")));
		}
       
		String fixed = rs.getString("fixed");
		if(groups != null) {
        String sortiment = (String)groups.get(accountnum);
		if (sortiment != null && sortiment.trim().length() > 0) {
			 cust.addFixSortCode(Converter.cleanLitLatEst(sortiment).trim());
			 cust.level = Converter.cleanLitLatEst(sortiment).trim();
		}else if (fixed != null && fixed.trim().length() > 0 ) {
			cust.addFixSortCode(Converter.cleanLitLatEst(fixed.trim()));
		}
		} else if (fixed != null && fixed.trim().length() > 0 ) {
			cust.addFixSortCode(Converter.cleanLitLatEst(fixed.trim()));
		}
		
	//	double minam = rs.getDouble("minam");
		//if(minam > 0){
			//cust.setMinsum(minam);
	//	}
       
		String ette = rs.getString("ette");
		if (ette != null && ette.trim().length() > 0 && ette.trim().equals("Priek")) {
			cust.setSalesBlockPrepay(true);
		}
		
		int nomax = rs.getInt("nomax");
		if(nomax == 1 ){
			cust.setMaxdisc(0);
			nomaxdisc.add(cust.getID());
		}
		
		String sdkterm = rs.getString("sdkterm");
		if (sdkterm != null && sdkterm.trim().length() > 0 ) {
			cust.addCity("STA Apmaksas : " + sdkterm.trim().substring(1));
		}
		
		String refe = rs.getString("refe");
		if (refe != null && refe.trim().length() > 0 ) {
			if(deliveries.get(refe.trim())!= null) {
			   cust.addCity("Dienas : " + deliveries.get(refe.trim()));
			}
		}
		
		String refe2 = rs.getString("refe2");
		if (refe2 != null && refe2.trim().length() > 0 ) {
			if(deliveries.get(refe2.trim())!= null) {
				if(cust.getCity().contains("Dienas")) {
					 cust.addCity( deliveries.get(refe2.trim())+"");
				} else {
					 cust.addCity("Dienas : " + deliveries.get(refe2.trim()));
				}
			  
			}
		}
		String refe3 = rs.getString("refe3");
		if (refe3 != null && refe3.trim().length() > 0 ) {
			if(cust.getCity().contains("Dienas")) {
				 cust.addCity(""+deliveries.get(refe3.trim()));
			} else {
				 cust.addCity("Dienas : " + deliveries.get(refe3.trim()));
			}
		}
		
		
		if(alekesed.get(aleke) != null){
			cust.setDiscount(Double.parseDouble(alekesed.get(aleke)+""));
		}
    
		if(pds.get( cust.getPayer()) != null){
			cust.setDiscount(Double.parseDouble(pds.get( cust.getPayer())+""));
		}
                    
   
                    
                    
                    //cust.setPrintean("orkla");
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	laks = false;
                    }
                    
               
                    
                    if(true){//laks
                        customers.put(name + cust.getID(), cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      
      
     
                rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        String maksja = rs.getString("klient").trim();
                        debt.setPayerID(maksja);
                        String saaja = rs.getString("saaja").trim();
                        debt.setCustID(saaja);
                       
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        boolean addDebt = true;
                        if(nodebt.contains(saaja)){
                        	addDebt = false;
                        }
                 
                        if(addDebt){
                            this.debt.addElement(debt);
    	                        	
                        }
                      }

           rs.close();
           stmt.close();
           con.close();
    } catch (Exception e){
      getParent().showError(e);
      System.exit(0);
    }
    
    
    return customers;
  }

  ArrayList nomaxdisc = null;
  
 
  
  

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


  

      public String getDistribution() {
        return null;
      }

      TreeMap grups = null;

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
     grups = new TreeMap ();
    String    sql = "  SELECT groupid as liik ,sortidx as sorting,name as nimi  FROM bmssa.TraItemSortGroup  where dataareaid ='ost' and formerisoft=1 ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("liik").trim();
      String sort = rs.getString("sorting").trim();
      if(sort.length() == 1){
    	  sort = "000"+sort;
      }
      if(sort.length() == 2){
    	  sort = "00"+sort;
      }
      if(sort.length() == 3){
    	  sort = "0"+sort;
      }
      grups.put(liik ,sort+liik );
        tm.put(sort+liik, sort+ "_"+ Converter.enCodeEst(rs.getString("nimi").trim()));
    }
    rs.close();
    st.close();
    con.close();
  
    return tm;
}

  
 
  public TreeMap getProdGroupsSecond() throws SQLException {

    //Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    
    tm.put("Biscuits and Cakes total", "Biscuits and Cakes total");
    tm.put("Chips and Drinkables total","Chips and Drinkables total");
    tm.put("Confectionary total", "Confectionary total");
    tm.put("Foods total", "Foods total");
    
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

  private TreeMap agents1 = new TreeMap();
  private HashSet allowedProds = null;
  
  public TreeMap getProducts(Agent agent){

	  String cmd = "  SELECT inv.itemid,inv.itemname  AS nimetus, stbqtyinbox AS kastis,stbqtyinpallet AS alusel,0 AS price \r\n" + 
	  		"                  ,(select top 1 bar.itembarcode from  bmssa.inventitembarcode bar where bar.dataareaid = inv.dataareaid \r\n" + 
	  		"				  and bar.itemid=inv.itemid and bar.useforprinting = 1) AS ean \r\n" + 
	  		"                  ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr \r\n" + 
	  		"                 ,mod.QUANTITY as opti , TPdiscount as maxdisc ,StbMinInQty as inpack,  ItemProdTypeId as status , \r\n" + 
	  		"                 pdgroupid as pgrupp , inventovergroupid as cate2 , StbMaxDiscount as max2 , StbMustHave   as topstatus , gr.ocslassortment  as gr1, gr.oflassortment  as gr2 "
	  		+ " ,netweight as gross ,InvenQlikGroupId as cate FROM bmssa.inventtable inv  \r\n" + 
	  		"     left JOIN bmssa.TraCatalogueItem cat ON  inv.itemid=cat.ITEMID and cat.dataareaid= inv.dataareaid \r\n" + 
	  		"                  left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid \r\n" + 
	  		"				  LEFT JOIN bmssa.INVENTITEMGROUP gr\r\n" + 
	  		"                     ON inv.ITEMGROUPID = gr.ITEMGROUPID AND \r\n" + 
	  		"                     Inv.DATAAREAID = gr.DATAAREAID  WHERE  inv.dataareaid='ost'  \r\n" + 
	  		"   AND moduletype=2 and blocked = 0     and cat.CATALOGUECODE='LV2111A' ";
    
	  String cmd2 = " select groupid as liik,itemid as toode ,sortidx as sorting from"
	  		+ " bmssa.TraItemSortItem where dataareaid ='ost' ";
	  
	 
    getSaveStockInfo();
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    TreeMap brakis = new TreeMap();
    try {
    	
    	getProdGroups();
    	
    Connection con = this.getNewConnection();

    	 String sql2 = "select itemid from bmssa.TraCatalogueItem where dataareaid = 'ost' "
 	    		+ "and cataloguecode like 'LV%' group by itemid ";
 	  
 	 allowedProds = new HashSet();
 	Statement stmt =  con.createStatement();
	
 	 ResultSet rs = stmt.executeQuery(sql2);
 	 while(rs.next()){
 		 allowedProds.add(rs.getString("itemid").trim());
 	 }
 	      
 	 
 	 String prs ="   Select disc.todate, disc.fromdate ,itemrelation as itemid, amount as price from bmssa.pricedisctable disc where disc.dataareaid= 'ost' and  accountrelation='' " +
				 "  and ((disc.fromdate<=getdate() and disc.todate=('1900-01-01 00:00:00.00') ) or " +
                 "  getdate() between disc.fromdate and disc.todate +1)  order by disc.itemrelation , disc.searchagain desc" ;
 	 
 			HashMap prices = new HashMap();
  	 //stmt =  con.createStatement();
 	
  	  rs = stmt.executeQuery(prs);
  	 while(rs.next()){
  		prices.put(rs.getString("itemid").trim(),rs.getDouble("price"));
  	 }
 	 
 	 TreeMap maxid = new TreeMap();
 	  
 	 String maxdis = "SELECT inventgrouporid as toode ,max(percent_) as perc , min(DATESTART) as dstart ,max(DATEEND) as dend  FROM bmssa.NpfCustInventBonusesStep " + 
    "  where NPFCUSTINVENTBONUSTYPE=2  and GETDATE()>= DATESTART-2 AND GETDATE()<=DATEEND+1 group by inventgrouporid";
 	
 	 rs = stmt.executeQuery(maxdis);
 	 while(rs.next()){
 		 String tt = rs.getString("toode").trim();
 		 double perc = rs.getDouble("perc");
 		 String sta = rs.getString("dstart").trim().substring(0,10);
 		 String end = rs.getString("dend").trim().substring(0,10);
 		 Toode tood = new Toode(tt);
 		 tood.setGr1(sta);
 		 tood.setGr2(end);
 		 tood.addKogus(perc);
 		maxid.put(tt,tood);
 	 }
 	 
 	 
 	 TreeMap liigid = new TreeMap();
 	TreeMap sortcodes = new TreeMap();
 	rs = stmt.executeQuery(cmd2);
	 while(rs.next()){
		 String liik = rs.getString("liik").trim();
		 String kood = rs.getString("toode").trim();
		 String sort = rs.getString("sorting").trim();
		 if(sort.equals("0")){
			 sort = "9999";
		 }else{
			 if(sort.length() == 1){
		    	  sort = "000"+sort;
		      }
		      if(sort.length() == 2){
		    	  sort = "00"+sort;
		      }
		      if(sort.length() == 3){
		    	  sort = "0"+sort;
		      }	 
		 }
		
		liigid.put(kood, liik);
		sortcodes.put(kood, sort);
	 }
	      
 	 
 	// System.out.println("sortcodes " + sortcodes.toString());
       rs = stmt.executeQuery(cmd);
  
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim()  ;
        nimetus = nimetus.replaceAll("Laima", "La.").replaceAll("LAIMA", "La.").
        		replaceAll("Skudru", "Sk.").replaceAll("marcipāns", "marc.").
        		replaceAll("piena", "pie.").
        		replaceAll("šokolādē", "šok.").
        		replaceAll("Asorti", "As.").
        	    replaceAll("Tumšās", "Tum.");
        		


     nimetus =  Converter.enCodeEst(nimetus);
   
       prod.setInCases(rs.getDouble("kastis"));
       
    	      prod.setOnPallet((rs.getDouble("alusel")/prod.getInCases()));
    	      if(prices.get(prod.getID()) != null){
    	    	  prod.setPrice(Double.parseDouble(prices.get(prod.getID())+""));  
    	      }else{
    	    	  prod.setPrice(rs.getDouble("price"));  
    	      }
             

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>4){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        add = " " +  ean.trim().substring(ll-5);
      }

      String grupp = (String)liigid.get(kood);
       if(grupp!=null && grupp.trim().length()>0){
    	   if(grups.get(grupp.trim())!= null)
        prod.setGroup(grups.get(grupp.trim())+"");
       }
       if(kogused.get(kood) != null){
       	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
       }else{
       	
       	prod.setStock(0);
       }
       
       String br = "";
       if(brakis.get(prod.getID()) != null){
    	   br = " BR@" + (int)Double.parseDouble(brakis.get(prod.getID())+"");
       }
      prod.setName(nimetus + add + br);


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
      
      
      double opti = rs.getDouble("opti");
      if(opti > 0){
    	  prod.setOptAmountInUnits(opti);
      }
      try{
    	  double maxdisc = 0;
    	  
    	  prod.setMaxDiscount(-1);
    	  Toode tt = null;
    	  if(maxid.get(prod.getID()) !=null){
    		 tt = (Toode)maxid.get(prod.getID());
    		  maxdisc = tt.getKogus();
    	  }
          if(maxdisc > 0){
        	  prod.setMaxDiscount(maxdisc);
        	  prod.setName(prod.getName() +  " MD-"  + (int)maxdisc + "%");
        	  prod.setPartii("MD Period :" + tt.getGr1() + "->" + tt.getGr2());
          } 
      }catch (Exception e){
    	  
      }
     
      try{
    	  double inpack = rs.getDouble("inpack");
          if(inpack > 0){
        	 prod.innerCaseContent =  inpack    ;      } 
      }catch (Exception e){
    	  
      }
      
      
     
  
      
      String ss =  nimetus + add;
      if(sortcodes.get(kood)!= null){
   	   prod.setNameSorting(sortcodes.get(kood) + nimetus + add + br);
   	   prod.sortRest = sortcodes.get(kood) +"";
   	   ss = sortcodes.get(kood) + nimetus + add;
      }else{
    	  prod.setNameSorting("xxxx" + nimetus + add);  
    	  ss = "xxxx" + nimetus + add;
      }
      
      String status = rs.getString("status");
      if(status!=null && status.trim().length()>0){
        prod.setType2(status.trim());
    }
      
      
      String pdgroupid = rs.getString("pgrupp");
 
      pdgroupid = rs.getString("cate2");
      
      
// panenme grupp2 paika 
      if(pdgroupid!=null && pdgroupid.trim().length()>0){
    	  String asi = null;
       if(pdgroupid.equals("Biscuits") || pdgroupid.equals("Cakes")  ){
              asi = "Biscuits and Cakes total";
      } else if(pdgroupid.equals("Chips") || pdgroupid.equals("Drinkables")  ){
	      asi = "Chips and Drinkables total";
      } else if(pdgroupid.equals("Zephyr") || pdgroupid.equals("Sugar") || pdgroupid.equals("Choco")){
	      asi = "Confectionary total";
      }else if(pdgroupid.equals("Other Food") || pdgroupid.equals("Other sauc") || pdgroupid.equals("Preserved") 
    			  || pdgroupid.equals("Tomato")   || pdgroupid.equals("Mayo")){
    	  asi = "Foods total";     
          }
    	
    	  if(asi != null){
    	 	  prod.setGroupSecond(asi.trim());
    	  }
    }
  //    Laima, Staburadze and Spilva 
    
      // paneme type siia
      if(pdgroupid!=null && pdgroupid.trim().length()>0){
    	  String type = null;
       if(pdgroupid.equals("Choco") || pdgroupid.equals("Sugar")  
    			  || pdgroupid.equals("Zephyr") 
    			  || pdgroupid.equals("Chips") ||  
		 pdgroupid.equals("Zephyr") 
		|| pdgroupid.equals("Biscuits")
		  || pdgroupid.equals("Other") ){
    	  type = "OLL";
    	  
      } else if(pdgroupid.equals("Cakes") || pdgroupid.equals("Pedro") ||
    		  pdgroupid.equals("Raw Materi") ){
    	  type = "STK";    
    	  
      }else if(pdgroupid.equals("Other sauc") || pdgroupid.equals("Preserved")  
    			  || pdgroupid.equals("Drinkables") || pdgroupid.equals("Mayo")   
    			  || pdgroupid.equals("Tomato")	  || pdgroupid.equals("Other Food")) {
    			   type = "BLS";     

      }
    	  if(type != null){
    	 	  prod.setType(type.trim());
    	  }
    }
      
      double max2 = rs.getDouble("max2");
      if(max2 > 0){
    	  prod.setMaxDiscount(100);
    	  prod.setStatusDiscount("1");
      }
      
      int top = rs.getInt("topstatus");
      if(top == 1) {
    	  prod.setStatusTop("1");
    	 // prod.setMustBe("1");
      }
    
      
      double weig = rs.getDouble("gross");
      if(weig > 0 ) {
    	  if(prod.getPartii() != null && prod.getPartii().trim().length() > 1) {
    		  prod.setPartii(prod.getPartii() + "|Neto svars :" + NF.format(weig) + " kg.");
    	  }else {
    		  prod.setPartii("Neto svars :" + NF.format(weig) + " kg.");
    	  }
    	 
      }
      
      String cate = rs.getString("cate");
      if(cate!=null && cate.trim().length()>0){
        prod.setTypeSimi(cate.trim());
      }
      
      
      if(allowedProds.contains(kood)){
    	  products.put(ss, prod);
      }

       
      }
      rs.close();
      stmt.close();
      con.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    
    
    
	
	  cmd = " select ItemRelation AS toode,MinimumQty as kogus ,SuppItemQty as akogus  from  bmssa.SuppItemTable  " + 
             " WHERE dataareaid = 'ost'  and  GETDATE()>= FROMDATE AND GETDATE()<=TODATE+1";
	
    StringBuffer buf = new StringBuffer();
	try {
		
		Connection con = this.getNewConnection();
	    Statement stmt =  con.createStatement();
		ResultSet	      rs = stmt.executeQuery(cmd);

		 Vector check = new Vector();
           while(rs.next()){
                  	String toodex = rs.getString("toode").trim();
                  	double  kogus = rs.getDouble("kogus");
                  	double  boonus = rs.getDouble("akogus");
                 if( allowedProds.contains(toodex)){
                  	      buf.append(toodex+ "; ; ;-1; ;" + kogus + ";" + boonus );
                          buf.append("\n");	
                  }
        	  }		
           rs.close();
           stmt.close();
           con.close();
	}catch (Exception e ){
		e.printStackTrace();
	}
	
	
	 cmd = " SELECT inventgrouporid as toode ,amountfrom as kogus,percent_ as perc FROM bmssa.NpfCustInventBonusesStep " +
     " where NPFCUSTINVENTBONUSTYPE=2  and GETDATE()>= DATESTART-2 AND GETDATE()<=DATEEND+1 ";
	
    try {
		
		Connection con = this.getNewConnection();
	    Statement stmt =  con.createStatement();
		ResultSet	      rs = stmt.executeQuery(cmd);

           while(rs.next()){
                  	String toodex = rs.getString("toode").trim();
                  	double  kogus = rs.getDouble("kogus");
                  	double  disc = rs.getDouble("perc");
                 if( allowedProds.contains(toodex)){
                  	      buf.append(toodex+ "; ; ;-1; ;" + kogus + ";0;0;" + disc );
                          buf.append("\n");	
                  }
        	  }		
	}catch (Exception e ){
		e.printStackTrace();
	}
	
	
	//System.out.println(buf.toString());
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
    		//ex.printStackTrace();
    	  }
     }
    
    return products;
  }



  

  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    HashMap kliendid = new HashMap();
	       HashMap ei = null;
	    //if (true) return custProds ;
	       

	 	  try {
	 			custProds =  (Vector) Converter.readObject("custprod.vec");
	 			} catch (IOException e) {
	 				e.printStackTrace();
	 			} catch (ClassNotFoundException e) {
	 				e.printStackTrace();
	 			}
	 	  
	 int counter = 1;

	 String  cmd = "  SELECT jou.custaccount,tra.itemid ,tra.qtyordered,jou.deliverydate AS lastdate ,max(tra.lineamount/tra.qtyordered) as "
	 		+ "hind ,max(jou.salesid) as dok , (case when jou.deliverydate > getdate()-14 then  RemainSalesPhysical else 0 end ) as pole " +
	    	"	  FROM  bmssa.SalesLine  tra  with (nolock) INNER JOIN  bmssa.SalesTable jou with (nolock) ON  " +
	    	"  tra.salesid=jou.salesid and tra.dataareaid =jou.dataareaid  INNER JOIN  bmssa.inventTABLE cus with (nolock) ON cus.itemid=tra.itemid " +
		"		  and tra.dataareaid =cus.dataareaid  inner JOIN bmssa.TraCatalogueItem cat ON  cus.itemid=cat.ITEMID and cat.dataareaid= cus.dataareaid  left join bmssa.TraItemSortItem  tr with (nolock) ON tr.dataareaid = tra.dataareaid and tr.itemid = tra.itemid " +
	    "		  WHERE  jou.deliverydate > getdate()-200 AND tra.dataareaid = 'ost'  " +
	    "		  and tra.qtyordered !=0	and cat.CATALOGUECODE='LV2111A'  GROUP BY jou.custaccount,cus.itemname,tra.itemid  ,jou.deliverydate ,tra.qtyordered ,RemainSalesPhysical,ItemProdTypeId,sortidx " +
	    "		  ORDER BY jou.custaccount,case when (ItemProdTypeId) = 'Diamond' then  'aaaa' " +
     	 " 		 else 'bbbb' end , " +
			"	 case when sortidx = '0' then '9999' else ( case when len (sortidx) = 1 then '00'+ sortidx  " +
			"	  else (case when len (sortidx) = 2 then '0'+ sortidx else sortidx end ) end) end ,cus.itemname "
			+ ",jou.deliverydate ,tra.qtyordered , RemainSalesPhysical  ";
	 
	   // if (isDebug()) System.out.println(cmd);

	    try {
        Connection con = this.getNewConnection();
	    Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("custaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("qtyordered");
	  
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("hind");
	     	        String bill = rs.getString("dok");   
	     	       double pole = rs.getDouble("pole");
	     	       
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	      //  cp.setAbiKood(bill);
	        if(pole > 0){
	        	cp.setOpenAmount(pole);
	        }

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
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");

	               cp.setRedline(rs.getString("date"));
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


  TreeMap custplans = null;
  
  
  public TreeMap getCustPlans(){
	  int counter = 1;
	TreeMap res = new TreeMap();
	if(custplans == null) {
		// custplans = this.makePlansByCustomers();	
		custplans = new TreeMap();
	}
     


String sqlo = " SELECT  jou.orderaccount as agent,pdgroupid as grupp ,round(SUM(lineamount),0) AS summa " +
 "  FROM bmssa.custinvoicetrans sal  with (nolock) INNER JOIN bmssa.inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN bmssa.custinvoicejour jou with (nolock) ON  " +
 "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN bmssa.custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " +
 "  inv.dataareaid='ost'  AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
 "  AND MONTH(sal.invoicedate) = MONTH(getdate()) and jou.orderaccount != '' and pdgroupid != '' GROUP BY jou.orderaccount,pdgroupid";




          try {
          	
          	Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(sqlo);
              while(rs.next()) {
                String custID = rs.getString("agent").trim();
                String grupp = rs.getString("grupp").trim();
                
                int sum = (int)rs.getDouble("summa");
                if (custplans.get(custID) != null){
                  TreeMap tr = (TreeMap) custplans.get(custID);
if(grupp.equals("Choco") || grupp.equals("Sugar") || grupp.equals("Other") || grupp.equals("Maigums sver") 
		|| grupp.equals("Cocoa prod.") || grupp.equals("ExclChocProd") || grupp.equals("Product sets") || grupp.equals("Trial products")
		|| grupp.equals("Zephyr") ||grupp.equals("Kalev") || grupp.equals("Choc.bars") ||grupp.equals("Choc.boxes")
		|| grupp.equals("Choc.candies") ||grupp.equals("Choc.tablets") || grupp.equals("SourcedJellyCan") || grupp.equals("BaltZefMais")
		|| grupp.equals("Caramels") || grupp.equals("Dragees") || grupp.equals("Maigums mais")|| grupp.equals("Maigums karb") || grupp.equals("BaltZefSver")
		|| grupp.equals("Jelly candies") || grupp.equals("Marmelade") || grupp.equals("Marzipan") || grupp.equals("Sherbert")){
	
	
	              Toode tt = (Toode)tr.get("1");
                  tt.setDonecur(tt.getDonecur() + sum);
                  }

if(grupp.equals("Flour") || grupp.equals("Flour products") ||
		grupp.equals("Biscuits") || grupp.equals("Wafer cakes")	
		|| grupp.equals("WaferCakesMini") || grupp.equals("Wafers")
		|| grupp.equals("Trial products") || grupp.equals("Wafer cakes")
		|| grupp.equals("Cakes") || grupp.equals("Other pastry")
		|| grupp.equals("Other products") || grupp.equals("Pastry")){
	
	
	
	
    Toode tt = (Toode)tr.get("2");
    tt.setDonecur(tt.getDonecur() + sum);
    }
if(grupp.equals("Chips") || grupp.equals("Pelets") || grupp.equals("Extruders") || grupp.equals("Water") ||grupp.equals("Dipi")
		|| grupp.equals("Dipi subcat.")
		|| grupp.equals("Kettle chips") || grupp.equals("Packed nuts") || grupp.equals("Soft drinks") ||grupp.equals("Syrups")
		|| grupp.equals("Pellets") || grupp.equals("Regular chips") || grupp.equals("Smoothies") ||grupp.equals("Juice")){
	
	Toode tt = (Toode)tr.get("3");
    tt.setDonecur(tt.getDonecur() + sum);
    }

if(grupp.equals("Mayo") || grupp.equals("Fish products") || grupp.equals("Flour products") || grupp.equals("Nutrition")
		|| grupp.equals("Pizza") || grupp.equals("Spices") || grupp.equals("Sweet group") || grupp.equals("VEGAN")
		|| grupp.equals("Mustard") || grupp.equals("MERCES") || grupp.equals("MĀRR_SINEP") || grupp.equals("Horseradish")
		|| grupp.equals("Preserve foods") || grupp.equals("Ready meal") || grupp.equals("Ketchup") || grupp.equals("Tomato sauce")
		|| grupp.equals("Dressings") ){
	

    Toode tt = (Toode)tr.get("4");
    tt.setDonecur(tt.getDonecur() + sum);
    }


                }
              }
              rs.close();
              stmt.close();
              con.close();

              

String cmd ="  SELECT  jou.orderaccount as agent,round(SUM(lineamount),0) AS summa " +
" FROM bmssa.custinvoicetrans sal INNER JOIN bmssa.inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid  INNER JOIN bmssa.custinvoicejour jou with (nolock) ON  " + 
" sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN bmssa.custtable cus with (nolock) ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
" inv.dataareaid='ost'    AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
"  AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY jou.orderaccount";

          	
          	 con = this.getNewConnection();
              stmt =  con.createStatement();
        	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String custID = rs.getString("agent").trim();
                TreeMap tr = (TreeMap) custplans.get(custID);
                if (tr != null){
                	
                	
                	Toode tt = (Toode)tr.get("allx");
                		if(tt != null){
                			tt.setDonecur(((int)rs.getDouble("summa")));
                        	 tr.put("allx", tt);	
                        	 custplans.put(custID, tr);
                		}
                 	 
                 // System.out.println(agent.getMonthSales());
                }
              }
              rs.close();
              stmt.close();
              con.close();
              
              
              //koogipoodidelel ka sama asi 
              
                            
                          
                            int yy = Calendar.getInstance().get(Calendar.YEAR);
                            int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                            int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                            double planprec = dd/d2;
                      	  
                      	 // Iterreerime Plaani mappi ja sealme asjad paika 
                      	  
                            
                            
                      	Iterator itera =   custplans.keySet().iterator();
                      	while(itera.hasNext()){
                      		String custID = itera.next() + "";
                      		TreeMap tr = (TreeMap) custplans.get(custID);
                      		
                      		
                      		double psales = 0;
                      		double msales = 0;
                            if (tr != null){
                            	Toode tt = (Toode)tr.get("allx");
                             
                      			if(tt != null){
                      			  psales = tt.getPlancur();
                      			  msales = tt.getDonecur();
                            	}
                      	  

                          		
                      			
                      	  double  tehtudsalperc = msales/psales;
                      	  double plaanitavMyykSal = d2 * msales/dd;
                      	  double  prognosalperc = plaanitavMyykSal/psales;
                      	  if(tehtudsalperc > 10){
                      		  tehtudsalperc = 0;  
                      	  }
                      	  if(prognosalperc > 10){
                      		  prognosalperc = 0;  
                      	  }
                      	  
                        String asi = "Sales: " + (int)msales + " €||";
                        
                        if(dd > 6 && tehtudsalperc > 0 && prognosalperc > 0){
                      	  asi = asi + "Plan done/estimate :";
                            asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                           }
                          asi = asi + "Sales plan: " + (int)psales + " €||";    
                     	
                         if(tt == null){
                     		asi = "";
                     	   }
                     	
                         String asi2 = "";
                         
                      	  Iterator enume = tr.keySet().iterator();
     	                	 while(enume.hasNext()){
     	                		 String brand = enume.next()+"";
     	                		 if(!brand.equals("allx")){
     	                			 tt = (Toode)tr.get(brand);
     	                			int teh = (int)tt.getDonecur();
     	                			if(brand.equals("4")){
     	                			//	teh = aa.buffer;
     	                			}
     	                			
     	                			if(tt.getPlancur() > 0){
     	                				asi2 = asi2 + ("Gr. " + tt.getBrand() + "    " +
         	                					((int)(teh/tt.getPlancur()*100))
         	                					+ "% Plan.: " +(int) tt.getPlancur() + " €  Sales.: " + teh + " €||");	
     	                			}
        	                		;
     	                		   }
     	                		 }
     	                	    
     	                
                      	

     	                   
                        
                         res.put(custID, Converter.enCodeEst(asi ) + "|" + asi2 );
                      	}
                            }
          } catch (Exception e) {
              getParent().showError(e);
              if(counter < 3 ) {
            	  counter++;
            	  getCustPlans();
              }
           }
          
          return res;
        }


  
  private TreeMap brandplans = null; 
  private TreeMap subplans = null; 
  private TreeMap chainplans = null; 
  private TreeMap regionplans = null; 



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  public void setBulkSales(Map agents){
 this.connectionUp();
   
 
 brandplans = this.makePlansCAT();
// subplans = this.makePlansSUB();

//System.out.println(brandplans.toString());
 
String sqlo = " SELECT  traagentsid as agent,inventovergroupid as grupp ,round(SUM(lineamount),0) AS summa \r\n" + 
		"   FROM bmssa.custinvoicetrans sal  with (nolock) INNER JOIN bmssa.inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN bmssa.custinvoicejour jou with (nolock) ON \r\n" + 
		"   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN bmssa.custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid\r\n" + 
		"   LEFT JOIN bmssa.INVENTITEMGROUP gr ON inv.ITEMGROUPID = gr.ITEMGROUPID AND  Inv.DATAAREAID = gr.DATAAREAID   WHERE \r\n" + 
		"   inv.dataareaid= 'ost'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"    AND MONTH(sal.invoicedate) = MONTH(getdate()) and traagentsid != '' and inventovergroupid != '' GROUP BY traagentsid,inventovergroupid";
 
 
 String cmd ="  SELECT  traagentsid as agent,round(SUM(lineamount),0) AS summa " +
 " FROM bmssa.custinvoicetrans sal  with (nolock) INNER JOIN bmssa.inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid  INNER JOIN bmssa.custinvoicejour jou with (nolock) ON  " + 
 " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN bmssa.custtable cus with (nolock) ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
 " inv.dataareaid='ost'    AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
 "  AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY traagentsid";


     //   if (isDebug()) System.out.println(cmd);
           try {
           	
           	
           	Connection con = this.getNewConnection();
               Statement stmt =  con.createStatement();
               ResultSet	      rs = stmt.executeQuery(sqlo);

        	   groupsCat.put("Cakes", "Cakes");
               while(rs.next()) {
                 String agentID = rs.getString("agent").trim();
                 String grupp = rs.getString("grupp").trim();
                 int sum = (int)rs.getDouble("summa");
                 
                 
                 
                 if ( brandplans.get(agentID) != null ){
                   TreeMap tr = (TreeMap) brandplans.get(agentID);
                   System.out.println("myygi uurimine " +  tr.toString());
                   
                   try {
                   	
                	   Toode tt = (Toode)tr.get(this.groupsCat.get(grupp));
                        System.out.println("myygi uurimine " + grupp + " " +  this.groupsCat.get(grupp) + " " + tt + " " + sum  + " " + groupsCat.toString()) ;
                        
                         if(tt != null) {
                       	  tt.setDonecur(tt.getDonecur() + sum);  
                         }
                          
                         
                   }catch(Exception o ) {
                   	//System.out.println("vvvv " + grupp + " " + groupsCat.get(grupp) );
                   }
                 }    
               }
               rs.close();
               stmt.close();
               con.close();
               
               
               
           	int count = 1;
               try {
              	 con = this.getNewConnection();
                stmt =  con.createStatement();
          	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.buffer2 = ((int)rs.getDouble("summa"));
                   // System.out.println(agent.getMonthSales());
                  }
                }
                rs.close();
                stmt.close();
                con.close();
               }catch(Exception e) {
               	if(count < 3) {
               		count++;
               		con = this.getNewConnection();
                       stmt =  con.createStatement();
                 	      rs = stmt.executeQuery(cmd);
                       while(rs.next()) {
                         String agentID = rs.getString("agent").trim();
                         Agent agent = (Agent)agents.get(agentID);
                         if (agent != null){
                           agent.buffer2 = ((int)rs.getDouble("summa"));
                          // System.out.println(agent.getMonthSales());
                         }
                       }
                       rs.close();
                       stmt.close();
                       con.close();
               	}
               }
           	
           
               
               
               
                             
                           
                             int yy = Calendar.getInstance().get(Calendar.YEAR);
                             int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                             int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                             double planprec = dd/d2;
                       	  
                       	 // Iterreerime Plaani mappi ja sealme asjad paika 
                       	  
                       	Iterator itera =   agents.keySet().iterator();
                       	while(itera.hasNext()){
                       		String agentID = itera.next() + "";
                       		Agent aa = (Agent)agents.get(agentID);
                       		  double msales = aa.buffer2 + aa.buffer;
                       		  double psales = 0;
                       		try{
                       		  psales = Double.parseDouble(((TreeMap)brandplans.get(agentID)).get("all")+"");
                       	  }catch(Exception e){
                       //		  e.printStackTrace();
                       	  }
                       	  double  tehtudsalperc = msales/psales;
                       	  double plaanitavMyykSal = d2 * msales/dd;
                       	  double  prognosalperc = plaanitavMyykSal/psales;
                       	  if(tehtudsalperc > 10){
                       		  tehtudsalperc = 0;  
                       	  }
                       	  if(prognosalperc > 10){
                       		  prognosalperc = 0;  
                       	  }
                       	  
                         String asi = "Sales: " + (int)msales + " €||";
                         if(dd > 6){
                       	  asi = asi + "Plan done/estimate :";
                             asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                         }
                          asi = asi + "Sales plan: " + (int)psales + " €||";    
                         
                          String asi2 = "";
                          
                          if(brandplans.get(aa.getID()) != null){
      	                	TreeMap vv =   (TreeMap)brandplans.get(aa.getID());
      	                	 Iterator enume = vv.keySet().iterator();
      	                	 while(enume.hasNext()){
      	                		 String brand = enume.next()+"";
      	                		 if(!brand.equals("all")){
      	                			Toode tt = (Toode)vv.get(brand);
      	                			int teh = (int)tt.getDonecur();
      	                			
         	                		asi2 = asi2 + ("Group " + tt.getBrand() + "    " +
      	                					((int)(teh/tt.getPlancur()*100))
      	                					+ "% Plan.: " +(int) tt.getPlancur() + " €  Sales.: " + teh + " €||");
      	                		   }
      	                		 }
      	                	    }
      	                   
                          aa.setStart( Converter.enCodeEst(asi ) + "|" + asi2 );
                        //  System.out.println(Converter.enCodeEst(asi ) + "|" + asi2 );
                          
                       	}
           } catch (Exception e) {
               getParent().showError(e);
            }
         }


   double totalPlanSum = 0;
   
   
   private TreeMap makePlansCAT(){
	   TreeMap res = new TreeMap();
	      totalPlanSum = 0;
	     groupsCat = new TreeMap();
	   try{
		
	   Calendar cal = Calendar.getInstance();
	   String month = (cal.get(Calendar.MONTH)+1)+ "";
	   if (month.length() == 1){
		   month = "0" + month;
	   }
	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
	   int a = 0;
	   File f = new File (getExcelDir() + "TP_Budget.xlsx");
		  FileInputStream	 ff = new FileInputStream(f);
	
		  
		
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		
		XSSFSheet sheet = workbook.getSheet("GROUPS");
 		XSSFRow row = null; 
 		// täidame groupsSub ära
 		for(int i = 0 ; i < sheet.getLastRowNum();i++){
				 row = sheet.getRow(i);
				if(row == null && a == 0){
					a = i;
				}else{
					a = sheet.getLastRowNum();
				}
			}
 		
 		
		for(int i = 1 ; i < a + 1;i++){
			 row = sheet.getRow(i);
			 try {
			String gr = (row.getCell((short) 1).getStringCellValue());
			String sub = (row.getCell((short) 2).getStringCellValue());
			if(gr != null && gr.trim().length() > 0)
			groupsCat.put(gr.trim(), sub.trim());
			 }catch (Exception e) {
				 
			 }
		  		
		} 
		System.out.println("groupsCat " + groupsCat.toString());
		sheet = workbook.getSheet(jooksev);
				ff.close();	
				if(sheet != null){
				
					
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						 row = sheet.getRow(i);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}
					String gr1 = null;
					String gr2 = null;
					String gr3 = null;
					String gr4 = null;

					String gr1n = null;
					String gr2n = null;
					String gr3n = null;
					String gr4n = null;
					
					for(int i = 0 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String agent = null;
						double g1 = 0;
						double g2 = 0;
						double g3 = 0;
						double g4 = 0;
						double gt = 0;
						
					if(i == 0){
						//loemepäised sisse
						
						gr1 = (row.getCell((short) 1).getStringCellValue()).trim();
						//gr2 = (row.getCell((short) 2).getStringCellValue()).trim();
						//gr3 = (row.getCell((short) 3).getStringCellValue()).trim();
						//gr4 = (row.getCell((short) 4).getStringCellValue()).trim();
						
						gr1n = (row.getCell((short) 1).getStringCellValue().trim());
						//gr2n = (row.getCell((short) 2).getStringCellValue().trim());
						//gr3n = (row.getCell((short) 3).getStringCellValue().trim());
						//gr4n = (row.getCell((short) 4).getStringCellValue().trim());
						
						
					}else{
						
						try {
							agent = (row.getCell((short) 0).getStringCellValue());
   					      } catch (Exception e) {
   						 //  e.printStackTrace();
   					      }
		//				System.out.println(agent);
						try {
						g1 = Double.parseDouble(row.getCell((short) 1).getNumericCellValue()+ "");
						//g2 = Double.parseDouble(row.getCell((short) 2).getNumericCellValue()+ "");
						//g3 = Double.parseDouble(row.getCell((short) 3).getNumericCellValue()+ "");
						//g4 = Double.parseDouble(row.getCell((short) 4).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					 e.printStackTrace();	
 					      }
						
						
						
						TreeMap asi= new TreeMap();
						       Toode t1 = new Toode(gr1);
						       t1.setBrand(gr1n);
						       t1.setPlancur(g1);
						/*       Toode t2 = new Toode(gr2);
						       t2.setPlancur(g2);
						       t2.setBrand(gr2n);
						       Toode t3 = new Toode(gr3);
						       t3.setPlancur(g3);
						       t3.setBrand(gr3n);
						       Toode t4 = new Toode(gr4);
						       t4.setBrand(gr4n);
						       t4.setPlancur(g4);
						*/
						       
				          asi.put(gr1, t1);
				          //asi.put(gr2, t2);
				          //asi.put(gr3, t3);
				          //asi.put(gr4, t4);
				          asi.put("all", g1+g2+g3+g4);
				         
						
						if(agent != null && !agent.equals("Total")){
							
						    res.put(agent,asi );
						 //  totalPlanSum = totalPlanSum + (int)(g1+g2+g3+g4); 
						    
						}
						
						if(agent != null && agent.equals("Total")){
							res.put("totals", asi);
						//	System.out.println("totals olemas " + asi.toString() + " " + asi.get("all"));
						}
   					
   					
						
					}
						
					}
				
				}
        		   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
	
	 // System.out.println("groupsCat  "+ groupsCat.toString() );
	   return res;
   }

   private TreeMap groupsSub = null;
   private TreeMap groupsCat = null;
    
    private TreeMap makePlansSUB(){
  	   TreeMap res = new TreeMap();
  	     // totalPlanSum = 0;
  	 groupsSub = new TreeMap();
  	   try{
  		
  	   Calendar cal = Calendar.getInstance();
  	   String month = (cal.get(Calendar.MONTH)+1)+ "";
  	   if (month.length() == 1){
  		   month = "0" + month;
  	   }
  	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
  	   int a = 0;
  	   File f = new File (getExcelDir() + "TP_Budget.xlsx");
  		  FileInputStream	 ff = new FileInputStream(f);
  	
  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
  		XSSFSheet sheet = workbook.getSheet("GROUPS");
  		XSSFRow row = null; 
  		// täidame groupsSub ära
  		for(int i = 0 ; i < sheet.getLastRowNum();i++){
				 row = sheet.getRow(i);
				if(row == null && a == 0){
					a = i;
				}else{
					a = sheet.getLastRowNum();
				}
			}
  		
  		
		for(int i = 1 ; i < a + 1;i++){
			try {
			 row = sheet.getRow(i);
			String gr = (row.getCell((short) 0).getStringCellValue());
			String sub = (row.getCell((short) 1).getStringCellValue());
			if(gr != null && gr.trim().length() > 0)
			groupsSub.put(sub.trim(), sub.trim());
			}catch(Exception e) {
				
			}
		}
			
  		
  		sheet = workbook.getSheet(jooksev);
  		
  	//	System.out.println("jooksev " + jooksev + " " + sheet);
  		
  				ff.close();	
  				if(sheet != null){
  				
  					
  					for(int i = 0 ; i < sheet.getLastRowNum();i++){
  						 row = sheet.getRow(i);
  						if(row == null && a == 0){
  							a = i;
  						}else{
  							a = sheet.getLastRowNum();
  						}
  					}
  					String gr1 = null;
  					String gr2 = null;
  					String gr3 = null;
  					String gr4 = null;
  					String gr5 = null;
  					
  					String gr1n = null;
  					String gr2n = null;
  					String gr3n = null;
  					String gr4n = null;
  					String gr5n = null;
  					
  					for(int i = 0 ; i < a + 1;i++){
  						 row = sheet.getRow(i);
  						String agent = null;
  						double g1 = 0;
  						double g2 = 0;
  						double g3 = 0;
  						double g4 = 0;
  						double g5 = 0;
  						double gt = 0;
  						
  					if(i == 0){
  						//loemepäised sisse
  						
  						gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
  						gr2 = "2";//(row.getCell((short) 2).getStringCellValue());
  						gr3 = "3";//(row.getCell((short) 3).getStringCellValue());
  						gr4 = "4";//(row.getCell((short) 4).getStringCellValue());
  						gr5 = "5";//(row.getCell((short) 4).getStringCellValue());
  						
  						gr1 = (row.getCell(5).getStringCellValue());
  						gr2 = (row.getCell((short) 6).getStringCellValue());
  						gr3 = (row.getCell((short) 7).getStringCellValue());
  						gr4 = (row.getCell((short) 8).getStringCellValue());
  						gr5 = (row.getCell((short) 9).getStringCellValue());
  						
  						
  						gr1n = (row.getCell((short) 5).getStringCellValue());
  						gr2n = (row.getCell((short) 6).getStringCellValue());
  						gr3n = (row.getCell((short) 7).getStringCellValue());
  						gr4n = (row.getCell((short) 8).getStringCellValue());
  						gr5n = (row.getCell((short) 9).getStringCellValue());
  						
  						
  					}else{
  						
  						try {
  							agent = (row.getCell((short) 0).getStringCellValue());
     					      } catch (Exception e) {
     						 //  e.printStackTrace();
     					      }
  		//				System.out.println(agent);
  						try {
  						g1 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
  						g2 = Double.parseDouble(row.getCell((short) 6).getNumericCellValue()+ "");
  						g3 = Double.parseDouble(row.getCell((short) 7).getNumericCellValue()+ "");
  						g4 = Double.parseDouble(row.getCell((short) 8).getNumericCellValue()+ "");
  						g5 = Double.parseDouble(row.getCell((short) 9).getNumericCellValue()+ "");
  						
  						} catch (Exception e) {
  	   					 e.printStackTrace();	
   					      }
  						
  						
  						
  						TreeMap asi= new TreeMap();
  						       Toode t1 = new Toode(gr1);
  						       t1.setBrand(gr1n);
  						       t1.setPlancur(g1);
  						       Toode t2 = new Toode(gr2);
 						       t2.setPlancur(g2);
 						       t2.setBrand(gr2n);
 						       Toode t3 = new Toode(gr3);
  						       t3.setPlancur(g3);
  						       t3.setBrand(gr3n);
  						       Toode t4 = new Toode(gr4);
  						       t4.setBrand(gr4n);
  						       t4.setPlancur(g4);
  						       
  						     Toode t5 = new Toode(gr5);
						       t5.setBrand(gr5n);
						       t5.setPlancur(g5);
						       
  				          asi.put(gr1, t1);
  				          asi.put(gr2, t2);
  				          asi.put(gr3, t3);
  				          asi.put(gr4, t4);
  				          asi.put(gr5, t5);
  				          asi.put("all", g1+g2+g3+g4+g5);
  				         
  						
  						if(agent != null && !agent.equals("Total")){
  							
  						    res.put(agent,asi );
  						 //  totalPlanSum = totalPlanSum + (int)(g1+g2+g3+g4); 
  						    
  						}
  						
  						if(agent != null && agent.equals("Total")){
  							res.put("totals", asi);
  						//	System.out.println("totals olemas " + asi.toString() + " " + asi.get("all"));
  						}
     					
     					
  						
  					}
  						
  					}
  				
  				}
          		   
  	   }catch (Exception e){
  		   e.printStackTrace();
  	   }
  	   
  	  // System.out.println("SubPlaanid  "+ res.toString() );
  //	 System.out.println("groupsSub  "+ groupsSub.toString() );
  	 
  	   return res;
     }

    

  
    
    private TreeMap makePlansByChains(){
  	   TreeMap res = new TreeMap();
  	      totalPlanSum = 0;
  	   try{
  		
  	   Calendar cal = Calendar.getInstance();
  	   String month = (cal.get(Calendar.MONTH)+1)+ "";
  	   if (month.length() == 1){
  		   month = "0" + month;
  	   }
  	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
  	   int a = 0;
  	   File f = new File (getExcelDir() + "CustChains_Budget.xlsx");
  		  FileInputStream	 ff = new FileInputStream(f);
  	
  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
  		XSSFSheet sheet = workbook.getSheet(jooksev);
  	//	System.out.println("jooksev " + jooksev + " " + sheet);
  		XSSFRow row = null; 
  				ff.close();	
  				if(sheet != null){
  				
  					
  					for(int i = 0 ; i < sheet.getLastRowNum();i++){
  						 row = sheet.getRow(i);
  						if(row == null && a == 0){
  							a = i;
  						}else{
  							a = sheet.getLastRowNum();
  						}
  					}
  					String gr1 = null;
  					String gr2 = null;
  					String gr3 = null;
  					String gr4 = null;

  					String gr1n = null;
  					String gr2n = null;
  					String gr3n = null;
  					String gr4n = null;
  					
  					for(int i = 0 ; i < a + 1;i++){
  						 row = sheet.getRow(i);
  						String agent = null;
  						double g1 = 0;
  						double g2 = 0;
  						double g3 = 0;
  						double g4 = 0;
  						double gt = 0;
  						
  					if(i == 0){
  						//loemepäised sisse
  						
  						gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
  						gr2 = "2";//(row.getCell((short) 2).getStringCellValue());
  						gr3 = "3";//(row.getCell((short) 3).getStringCellValue());
  						gr4 = "4";//(row.getCell((short) 4).getStringCellValue());
  						
  						gr1n = (row.getCell((short) 1).getStringCellValue());
  						gr2n = (row.getCell((short) 2).getStringCellValue());
  						gr3n = (row.getCell((short) 3).getStringCellValue());
  						gr4n = (row.getCell((short) 4).getStringCellValue());
  						
  						
  					}else{
  						
  						try {
  							agent = (row.getCell((short) 0).getStringCellValue());
     					      } catch (Exception e) {
     						   e.printStackTrace();
     					      }
  		//				System.out.println(agent);
  						try {
  						g1 = Double.parseDouble(row.getCell((short) 1).getNumericCellValue()+ "");
  						g2 = Double.parseDouble(row.getCell((short) 2).getNumericCellValue()+ "");
  						g3 = Double.parseDouble(row.getCell((short) 3).getNumericCellValue()+ "");
  						g4 = Double.parseDouble(row.getCell((short) 4).getNumericCellValue()+ "");
  						} catch (Exception e) {
  	   					 e.printStackTrace();	
   					      }
  						
  						
  						
  						TreeMap asi= new TreeMap();
  						       Toode t1 = new Toode(gr1);
  						       t1.setBrand(gr1n);
  						       t1.setPlancur(g1);
  						       Toode t2 = new Toode(gr2);
 						       t2.setPlancur(g2);
 						       t2.setBrand(gr2n);
 						       Toode t3 = new Toode(gr3);
  						       t3.setPlancur(g3);
  						       t3.setBrand(gr3n);
  						       Toode t4 = new Toode(gr4);
  						       t4.setBrand(gr4n);
  						       t4.setPlancur(g4);
  						
  						       
  				          asi.put(gr1, t1);
  				          asi.put(gr2, t2);
  				          asi.put(gr3, t3);
  				          asi.put(gr4, t4);
  				          asi.put("all", g1+g2+g3+g4);
  				         
  						
  						if(agent != null && !agent.equals("Total")){
  							
  						    res.put(agent,asi );
  						   totalPlanSum = totalPlanSum + (g1+g2+g3+g4); 
  						    
  						}
  						
  						if(agent != null && agent.equals("Total")){
  							res.put("totals", asi);
  						//	System.out.println("totals olemas " + asi.toString() + " " + asi.get("all"));
  						}
     					
     					
  						
  					}
  						
  					}
  				
  				}
          		   
  	   }catch (Exception e){
  		   e.printStackTrace();
  	   }
  	   
  	  // System.out.println("totalPlanSum "+ totalPlanSum );
  	   return res;
     }
     

     
    
    
    
    private TreeMap makePlansByCustomers(){
 	   TreeMap res = new TreeMap();
 	  
 	   try{
 	
 		Calendar cal = Calendar.getInstance();
 	     String month = (cal.get(Calendar.MONTH)+1)+ "";
 	     if(month.length() == 1){
 		   month = "0" + month;
 	      }
 	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
 	   int a = 0;
 	   File f = new File (getExcelDir() + "TP_Budget_client.xlsx");
 	   FileInputStream	 ff = new FileInputStream(f);
 	
 		XSSFWorkbook workbook = new XSSFWorkbook((ff));
 		XSSFSheet sheet = workbook.getSheet(jooksev);
 		//System.out.println("jooksev " + jooksev + " " + sheet);
 		XSSFRow row = null; 
 				ff.close();	
 				if(sheet != null){
 				
 					
 					for(int i = 0 ; i < sheet.getLastRowNum();i++){
 						 row = sheet.getRow(i);
 						if(row == null && a == 0){
 							a = i;
 						}else{
 							a = sheet.getLastRowNum();
 						}
 					}
 					String gr1 = null;
 					String gr2 = null;
 					String gr3 = null;
 					String gr4 = null;

 					String gr1n = null;
 					String gr2n = null;
 					String gr3n = null;
 					String gr4n = null;
 					
 					for(int i = 0 ; i < a + 1;i++){
 						 row = sheet.getRow(i);
 						String cust = null;
 						double g1 = 0;
 						double g2 = 0;
 						double g3 = 0;
 						double g4 = 0;
 						
 					if(i == 0){
 						//loemepäised sisse
 						
 						gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
 						gr2 = "2";//(row.getCell((short) 2).getStringCellValue());
 						gr3 = "3";//(row.getCell((short) 3).getStringCellValue());
 						gr4 = "4";//(row.getCell((short) 4).getStringCellValue());
 						
 						gr1n = (row.getCell((short) 4).getStringCellValue());
 						gr2n = (row.getCell((short) 5).getStringCellValue());
 						gr3n = (row.getCell((short) 6).getStringCellValue());
 						gr4n = (row.getCell((short) 7).getStringCellValue());
 						
 						
 					}else{
 						
 						try {
 							cust = (row.getCell((short) 1).getStringCellValue());
    					      } catch (Exception e) {
    						   e.printStackTrace();
    					      }
 		//				System.out.println(agent);
 						try {
 						g1 = Double.parseDouble(row.getCell((short) 4).getNumericCellValue()+ "");
 						} catch (Exception e) {
 	 	   					 e.printStackTrace();	
 	  					      }
 						try {
 						g2 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
 					    } catch (Exception e) {
	   					 e.printStackTrace();	
 					      }
 						try {
 						g3 = Double.parseDouble(row.getCell((short) 6).getNumericCellValue()+ "");
 					    } catch (Exception e) {
	   					 e.printStackTrace();	
 					      }
 						try {
 						 g4 = Double.parseDouble(row.getCell((short) 7).getNumericCellValue()+ "");
 						 } catch (Exception e) {
 	   					 e.printStackTrace();	
  					      }
 						
 						TreeMap asi= new TreeMap();
 						       Toode t1 = new Toode(gr1);
 						       t1.setBrand(gr1n);
 						       t1.setPlancur(g1);
 						       Toode t2 = new Toode(gr2);
						       t2.setPlancur(g2);
						       t2.setBrand(gr2n);
						       Toode t3 = new Toode(gr3);
 						       t3.setPlancur(g3);
 						       t3.setBrand(gr3n);
 						       Toode t4 = new Toode(gr4);
 						       t4.setBrand(gr4n);
 						       t4.setPlancur(g4);
 				          asi.put(gr1, t1);
 				          asi.put(gr2, t2);
 				          asi.put(gr3, t3);
 				          asi.put(gr4, t4);
 				         Toode t5 = new Toode("allx");
					       t5.setPlancur(g1+g2+g3+g4);
 				          asi.put("allx", t5);
 						
 						if(cust != null){
 				
 						
 						    res.put(cust,asi );
 										
 						}
    					
    					
 						
 					}
 						
 					}
 				
 				}
         		   
 	   }catch (Exception e){
 		   e.printStackTrace();
 	   }
 	//   System.out.println("kleindi plaanid " + res.toString());
 	   return res;
    }
    
    private TreeMap getRegions(){
    	 TreeMap   tm = new TreeMap();
		   
    		    Connection con;
				try {
					con = getNewConnection();
				
    		     grups = new TreeMap ();
    		    String    sql = "  select accountnum as klient  , regionname as region from   bmssa.custtable cus " +
								"    INNER JOIN bmssa.traregiontable reg with (nolock)  " +
			"   ON cus.traregionid = reg.regionid and reg.dataareaid= cus.dataareaid where cus.dataareaid = 'ost' and  regionid is not null ";
    		    Statement st = con.createStatement();
    		    ResultSet rs = st.executeQuery(sql);

    		    while (rs.next()) {
    		      String liik = rs.getString("klient").trim();
    		      String sort = rs.getString("region").trim();
    		    
    		        tm.put(liik, sort);
    		    }
    		    rs.close();
    		    st.close();
		        con.close();
		        
		    } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		    return tm;
    		}

    
    private TreeMap makePlansByRegions(){
  	   TreeMap res = new TreeMap();
  	  TreeMap regions = getRegions();
  	   try{
  	
  		Calendar cal = Calendar.getInstance();
  	     String month = (cal.get(Calendar.MONTH)+1)+ "";
  	     if(month.length() == 1){
  		   month = "0" + month;
  	      }
  	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
  	   int a = 0;
  	   File f = new File (getExcelDir() + "TP_Budget_client.xlsx");
  	   FileInputStream	 ff = new FileInputStream(f);
  	
  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
  		XSSFSheet sheet = workbook.getSheet(jooksev);
  	//	System.out.println("jooksev " + jooksev + " " + sheet);
  		XSSFRow row = null; 
  				ff.close();	
  				if(sheet != null){
  				
  					
  					for(int i = 0 ; i < sheet.getLastRowNum();i++){
  						 row = sheet.getRow(i);
  						if(row == null && a == 0){
  							a = i;
  						}else{
  							a = sheet.getLastRowNum();
  						}
  					}
  					String gr1 = null;
  					String gr2 = null;
  					String gr3 = null;
  					String gr4 = null;

  					String gr1n = null;
  					String gr2n = null;
  					String gr3n = null;
  					String gr4n = null;
  					
  					for(int i = 0 ; i < a + 1;i++){
  						 row = sheet.getRow(i);
  						String cust = null;
  						double g1 = 0;
  						double g2 = 0;
  						double g3 = 0;
  						double g4 = 0;
  						
  					if(i == 0){
  						//loemepäised sisse
  						
  						gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
  						gr2 = "2";//(row.getCell((short) 2).getStringCellValue());
  						gr3 = "3";//(row.getCell((short) 3).getStringCellValue());
  						gr4 = "4";//(row.getCell((short) 4).getStringCellValue());
  						
  						gr1n = (row.getCell((short) 4).getStringCellValue());
  						gr2n = (row.getCell((short) 5).getStringCellValue());
  						gr3n = (row.getCell((short) 6).getStringCellValue());
  						gr4n = (row.getCell((short) 7).getStringCellValue());
  						
  						
  					}else{
  						
  						try {
  							cust = (row.getCell((short) 1).getStringCellValue());
  							cust = (String)regions.get(cust);
     					      } catch (Exception e) {
     						   e.printStackTrace();
     					      }
  					//	System.out.println(cust);
  						
  						if(cust != null){
  						
  						try {
  						g1 = Double.parseDouble(row.getCell((short) 4).getNumericCellValue()+ "");
  						} catch (Exception e) {
  	 	   				//	 e.printStackTrace();	
  	  					      }
  						try {
  						g2 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
  					    } catch (Exception e) {
 	   					// e.printStackTrace();	
  					      }
  						try {
  						g3 = Double.parseDouble(row.getCell((short) 6).getNumericCellValue()+ "");
  					    } catch (Exception e) {
 	   					// e.printStackTrace();	
  					      }
  						try {
  						 g4 = Double.parseDouble(row.getCell((short) 7).getNumericCellValue()+ "");
  						 } catch (Exception e) {
  	   					// e.printStackTrace();	
   					      }
  						
  						TreeMap asi= (TreeMap)res.get(cust);
  						
  						if(asi == null ){
  							asi = new TreeMap();
  							 Toode t1 = new Toode(gr1);
						       t1.setPlancur(g1);
						       Toode t2 = new Toode(gr2);
						       t2.setPlancur(g2);
						       Toode t3 = new Toode(gr3);
						       t3.setPlancur(g3);
						       Toode t4 = new Toode(gr4);
						       t4.setPlancur(g4);
						       Toode t5 = new Toode("all");
						       t5.setPlancur(g1+g2+g3+g4);
	 				          
						  asi.put(gr1, t1);
				          asi.put(gr2, t2);
				          asi.put(gr3, t3);
				          asi.put(gr4, t4);
				          asi.put("all", t5);
  						} else {


  							Toode t1 = (Toode) asi.get(gr1);
  							if(t1 == null){
  							   t1 = new Toode(gr1);
 						       t1.setPlancur(g1);
  							}else{
  								t1.setPlancur(t1.getPlancur() + g1);
  							}
  							Toode t2 = (Toode) asi.get(gr2);
  							if(t2 == null){
  							   t2 = new Toode(gr2);
 						       t2.setPlancur(g2);
  							}else{
  							   t2.setPlancur(t2.getPlancur() + g2);
  							}
  							Toode t3 = (Toode) asi.get(gr3);
  							if(t3 == null){
  							   t3 = new Toode(gr3);
 						       t3.setPlancur(g3);
  							}else{
  							   t3.setPlancur(t3.getPlancur() + g3);
  							}    
  							Toode t4 = (Toode) asi.get(gr4);
  							if(t4 == null){
  							   t4 = new Toode(gr4);
 						       t4.setPlancur(g4);
  							}else{
  							   t4.setPlancur(t4.getPlancur() + g4);
  							}    
  							Toode t5 = (Toode) asi.get("all");
  							if(t5 == null){
  							   t5 = new Toode("all");
 						       t5.setPlancur(g1+g2+g3+g4);
  							}else{
  							   t5.setPlancur(t5.getPlancur() + g1+g2+g3+g4);
  							} 
  							
  						 
  						}
  						      
  				         
  						
  						if(cust != null){
  						    res.put(cust,asi );
  						}
     					
  						
  						}
  						
  					}
  						
  					}
  				
  				}
          		   
  	   }catch (Exception e){
  		   e.printStackTrace();
  	   }
  	  System.out.println("regiooni plaanid " );
  	   return res;
     }
     

   
    public TreeMap getCustDisplays() {
  	  TreeMap result = new TreeMap();
  		File f = new File(this.getExcelDir() + "Customer_Displays.xlsx");
  		try {
  			
  			Calendar cal = Calendar.getInstance();
  	  	     String month = (cal.get(Calendar.MONTH)+1)+ "";
  	  	     if(month.length() == 1){
  	  		   month = "0" + month;
  	  	      }
  	  	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
  			
  			FileInputStream ff = new FileInputStream(f);
  			XSSFWorkbook workbook = new XSSFWorkbook((ff));
  			XSSFSheet sheet = workbook.getSheet(jooksev);
  			XSSFRow row = null;
  			TreeMap groups = new TreeMap();
  			row = sheet.getRow(3);
  			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

  			// System.out.println("rownum " + row.getLastCellNum());
  			for (int a = 8; a < row.getLastCellNum(); a++) {
  				try {
  					
  					String gg = row.getCell((short) a).getStringCellValue().trim();
  				//	 System.out.println(gg);
  					groups.put(a + "", gg);
  				} catch (Exception e) {
  					// System.out.println(row.getCell((short)a).getNumericCellValue());
  				}
  			}

  			// System.out.println("groups " + groups.size() + " " + groups.toString());

  			// System.exit(0);
  			for (int i = 8; i <= sheet.getLastRowNum(); i++) {
  				row = sheet.getRow(i);
  				
  				String custID = "";

  				if (row.getCell((short) 2) != null) {

  					try {
  						custID = (row.getCell((short) 2).getStringCellValue());
  					} catch (Exception e) {
  					    e.printStackTrace();	
  					}
  					
                         if(!custID.equals("0")){
                      	
  					// Siin läheme reasisse rokkima ja vaatama kas se toode
  					// läheb kaasa
  					for (int a = 8; a < row.getLastCellNum(); a++) {

  						String group = (String) groups.get(a + "");
  						if (group != null && group.trim().length() > 0) {

  						
  							String go = null;
  						
  							// Vaatame kas selles veerus on sellel tootel pandud
  							// mingi tähis
  							if (row.getCell((short) a) != null) {

  								try {
  									    go = (int) (row.getCell((short) a)
  											.getNumericCellValue()) + "";
  								} catch (Exception e) {
  									
  									try{
  										go = (row.getCell((short) a).getStringCellValue());
  									}catch(Exception k ){
  										
  									}
  								}
  							}

  							if (go != null && go.length() > 0 && !go.equals("0")) {

  								//System.out.println( a + " xxx " + custID + " " + go + " " + group );
  								
  								Vector vv = (Vector)result.get(custID);
  								if(vv == null){
  									vv = new Vector();
  								}
  								vv.add(go + " " + group);
  								result.put(custID, vv);
  							}
  						}
  					 }
  					}
  				}
  			}
  		} catch (Exception e) {
  		//	e.printStackTrace();
  		}

  		// System.exit(0);
  		System.out.println("Display info " + result.size());
  		
  		return result;

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


 //   private TreeMap brandplans = null; 
  //  private TreeMap subplans = null; 
  //  private TreeMap chainplans = null; 
   // private TreeMap regionplans = null; 

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
    return "AxaOrklaLV";
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
	    	  Connection con = this.getNewConnection();
	  	    Statement stmt =  con.createStatement();
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }
                       rs2.close();
                       stmt.close();
                       con.close();
	                  
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
  private String url = "jdbc:odbc:stabu";
  private String url2 = "jdbc:odbc:kondit";
  private String url3 = "jdbc:odbc:ladu";
  private String url4 = "jdbc:odbc:myyk";
  private   Vector debt = new Vector();
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
	  	    TreeMap phsize = new TreeMap();
	  	    
	  	    
	  	 try {
         	  File f = new File(pictspda +"list.txt");
              if (f.exists())f.delete();
              //Kataloogi sisude kustutamine
          	  File res2 = new File (pictspda);
	             String[] ff2 = res2.list();
	         	 for(int i = 0;ff2.length>i;i++){
	             //	(new File ( pictspda + ff2[i])).delete(); 
	             }
//	         	 Lõppes kustutamine 
	  		 
	  		 TreeMap prods = this.getProducts(null);
	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getEAN().trim();  
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	            
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(toode)){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
	              	if(!inpda.exists()) {
	              		Converter.shrinkAndCopyFile(in,inpda,400);
	              	}
	                  	
	                  	File kkk =  new File ( pictspda + prod.getID()+ ".jpg"); 
	                  	phsize.put(prod.getID(), kkk.length());
                	    }
	                 }
	               }
	             }
	    	   }
	        
	         
	       Iterator itera =   phsize.keySet().iterator();
	       while(itera.hasNext()) {
	    	   String prid = (String)itera.next();
	    		buf.append(prid +".jpg;"  + phsize.get(prid) + "\n");
	               
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
	
	
	
	@Override
	public Vector getFixedAssortmentData() {
		
		return fixed;
	}
	@Override
	public void FixReporter() {
		
	//	On vaja tekitada 3 faili 
	//	1. Dayreports käimistega 
	//	2. Tasks 
	//	3. Vastustega
		
		  
		/*		
		 * 
		 *      Kui on vaja päeva haaval välja lugeda siis peab seda kasutama.
		     for(int u = 0 ; u < 50 ;u++) {
		    	 
				Calendar start = Calendar.getInstance();
				start.add(Calendar.DATE, -u);
 
				Calendar end = Calendar.getInstance();
				end.add(Calendar.DATE, -u+1);
								 
					String filenameTask =   Converter.getTaskData(start.getTimeInMillis(), end.getTimeInMillis(), "orklalv", "orkla.lv", "Riga-012","Tasks-"+ Converter.getDDpMMpYYYYpString(start)+"-" );
				System.out.println("filenameTask "+  filenameTask);
			  
		  }
		  
	
		 
		 
		 System.exit(0); 
*/
		
		  //System.out.println(input);
		
		  
		  DateMidnight nn = new DateMidnight();
		     final DateTime input = new DateTime(nn.getMillis());
		    
		     
		     for(int u = 0 ; u < 28 ;u++) {
				  final DateTime startOfLastWeek2 = 
				             new DateTime(input.minusHours(u*12+12));
				  final DateTime endOfLastWeek2 =  new DateTime(input.minusHours(u*12));
				
			  
				System.out.println("algus " +startOfLastWeek2.toString());
				System.out.println("lopp " +endOfLastWeek2.toString());
			  String filenameSV =   Converter.getSVData(startOfLastWeek2.getMillis(), endOfLastWeek2.getMillis(),
					  rcompanyx, user, pass,false,"SELYNLIST",null,"answers"+ u+ "-");
				 System.out.println("filenameSV "+  filenameSV);
		  }
		  
		  final DateMidnight startOfLastWeek = 
		             new DateMidnight(input.minusDays(14));
		  
		
		  //System.out.println(startOfLastWeek);
		  final DateMidnight endOfLastWeek =  new DateMidnight(input.plusDays(1));
		
		 
			String filenameTask =   Converter.getTaskData(startOfLastWeek.getMillis(), endOfLastWeek.getMillis(), rcompanyx, user, pass,"Tasks");
			// System.out.println("filenameTask "+  filenameTask);
		 
		
		 System.exit(0);
	}
	    

	
	
	
}
