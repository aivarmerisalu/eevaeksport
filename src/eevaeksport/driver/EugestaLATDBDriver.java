package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.CycleMatch;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.Quest;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class EugestaLATDBDriver extends AbstractDBDriver  implements Manager,Promo,FixLoader, BulkUpdater,Discounter,WebReporter,PictureMaker {
  public EugestaLATDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
    country = getParent().getProps().getProperty("country");
    

    
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String country = null;
  
  

  
  /**
   *
   */
  public void connectionUp(){
  }

	public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		

	String sq3 = "    SELECT jou.clientBackOfficeId as klient, jou.deliverydate  as kuup , jou.productBackOfficeId as toode ,\r\n" + 
			"					 sum(case when jou.Quantity > 0 then jou.Quantity else 0 end) as kogus, jou.Price as hind,\r\n" + 
			"						 sum(case when jou.Quantity < 0 then jou.Quantity else 0 end) as kogusmin, max (jou.invoiceNumber) as doknr ,\r\n" + 
			"						 case when  jou.deliverydate > CAST(GETDATE() AS DATE)then 1 else 0 end as color FROM EPASAULE_DELIVERIES  jou  WHERE  jou.deliverydate >=  \r\n" + 
			"									(select top 1 kuupaev  from ( 	 SELECT top 10 jou2.deliverydate as kuupaev FROM  EPASAULE_DELIVERIES jou2  where ltrim(jou2.clientBackOfficeId) = ltrim(jou.clientBackOfficeId)    \r\n" + 
			"									 and jou2.deliverydate > getdate()-90 and jou2.deliverydate < getdate()+4 group by jou2.clientBackOfficeId , jou2.deliverydate order by jou2.deliverydate desc) as dd order by kuupaev)   \r\n" + 
			"							 GROUP BY jou.clientBackOfficeId,jou.deliverydate , jou.productBackOfficeId , jou.Price ORDER BY jou.clientBackOfficeId,jou.deliverydate desc, max(jou.invoiceNumber) desc, jou.productBackOfficeId" ;
					
					Connection con3 = getNewConnection();
					Statement st3 = con3.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(Converter.turn(rs3.getString("kuup")).substring(0,5));
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));// see on sassis ja vajab erilähenemist
						ml.setDiscount(rs3.getDouble("hind"));
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

		return result;
	}


	 public String getSaveDebtInfo() {
	 	  
		  String cmd2 = "  SELECT sum(Amount) as tasumata , debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS where \r\n" + 
	  	 		" payDate < getdate()-1 group by debetorBackOfficeId"; 
		  
		  String cmd = " select debitorBackOfficeId as payer ,  Status AS block  from  dbo.EPASAULE_CLIENTS  where status = 'B' "; 
	      		

			HashSet<String> hh = new HashSet<String>();
			TreeMap custs = new TreeMap();

			StringBuffer products = new StringBuffer();
			try {
			Connection con = getNewConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cmd);
			while (rs.next()) {
				hh.add(rs.getString("payer"));
			}
	        
			rs.close();
			rs = st.executeQuery(cmd2);
			
			while (rs.next()) {

				   double volg = rs.getDouble("tasumata");
				String maks = rs.getString("maksja");
			   Customer custw = null;
			if(maks != null){
				 custw = new Customer (maks.trim());
				custw.setDebt(volg);
				if(volg > 0.09 || volg < -0.09){
					custs.put(maks.trim(),custw);
				}
			}
			
			}

			rs.close();
			st.close();
			con.close();
			
			} catch (Exception e) {
			getParent().showError(e);
			}
	      
	      Iterator     codes = custs.values().iterator();
			while (codes.hasNext()){
			Customer cust = (Customer)codes.next();
			String bl = "0";
			if(hh.contains(cust.getID())){
				bl = "1";
				}
			
			 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
			}
	      
			return products.toString();

	     }


  
  private void makePictureCat(){
  	
  	String pictssou = parent.getProps().getProperty("pict.dir");
	    String pictspda = parent.getProps().getProperty("pictpda.dir");
     this.getProducts(null);
	 try {
   	  
        File f = new File(pictspda +"list.txt");
        if (f.exists())f.delete();
       
        File res2 = new File (pictspda);
           String[] ff2 = res2.list();
       	 /*for(int i = 0;ff2.length>i;i++){
           	(new File ( pictspda + ff2[i])).delete(); 
           }*/

       StringBuffer buf = new StringBuffer();
       Iterator enume = prodcodes.iterator();
  
       while (enume.hasNext()) {
  	   
  	    	String toode = (String)enume.next();
  	    	
  	    	
           if(toode.length() > 0){
              	
  	   	  res2 = new File (pictssou);
            ff2 = res2.list();
           
       	 for(int i = 0;ff2.length>i;i++){
           	if( ff2[i].startsWith(toode+".")){
           	File in = new File ( pictssou + ff2[i]); 
            	if(in.isFile()){
            	File inpda = new File ( pictspda + toode+ ".jpg"); 
            	if(!inpda.exists()) {
            	try {
            		int size = 400;
            		if(toode.startsWith("i") || toode.startsWith("u") || toode.startsWith("r") || toode.startsWith("t")) {
            			size = 1280;
            		}
                	Converter.shrinkAndCopyFile(in,inpda,size);
                } catch(Exception e) {
            		System.out.println("pahasti prod.getID() " + toode);
            		e.printStackTrace();
            	}
           	   }
             	buf.append(toode+".jpg;"  + inpda.length() + "\n");
                
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
  


  public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		/*
		
		try {
			
			String  cmd = "SELECT  s.assortimentBackOfficeId as ClientGR ,s.productBackOfficeId as ItemID , s.NEWINASSORT as newin ,s.EG_ASSORTITEMSTATUS as status " + 
					"	FROM EPASAULE_ASSORTMENTS s inner join dbo.EPASAULE_PRODUCTS a ON a.backOfficeId = s.productBackOfficeId " + 
					"	order by str(a.posWeight) + a.Name ";
			
		
		         if (isDebug()) System.out.println(cmd);
		           Connection con = getNewConnection();
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd);

		           while (rs.next()) {

		             String sortid = rs.getString("ClientGR").trim()+"x";
		             String prodID = rs.getString("ItemID").trim();
		             int optio = rs.getInt("newin");
		             int status = rs.getInt("status"); 
		             
		             if(usedProds.contains(prodID)){
							
						MatrixLine mline = new MatrixLine();
						

								if (sortid != null
										&& sortid.trim().length() > 1) {
									mline.setCliGroup(Converter.cleanLitLatEst(sortid.trim()).replaceAll(";", ":"));
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								
								if(optio == 1) {
								   mline.setLineinfo("NEW"); // valikuline optional item
								}else if (status == 0) {
								   mline.setLineinfo("CENTRAL");
								}
							if(status != 2) {
									result.add(mline);	
								}
							}
		           }
						
					 rs.close();
					 st.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

		//System.out.println("fix size " + result.size());
		return result;

	
}

  
  private HashMap<String,List<String>> activeXLSPromos = null;
  
  private void getCampExcelLines(Vector lines) {
	  try {
		  
		  if(this.eancodes == null) {
			  this.getProducts(null);
		  }
		  
	   File f = new File (this.getExcelDir() + "campaigninfo.xlsx");
	   FileInputStream	 ff = new FileInputStream(f);
	
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheetAt(0);

		 DataFormatter formatter = new DataFormatter();
		
		XSSFRow row = null; 
				ff.close();	
				 int a = 0;
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


								 String cust = null;
								try {
									cust = formatter.formatCellValue(row.getCell((short) 0));
		 					      } catch (Exception e) {
		 					    	  //e.printStackTrace();
		 						  }
								
								String segment = null;
								if(cust == null  || cust.trim().length() == 0) {
								try {
									segment = formatter.formatCellValue(row.getCell((short) 2));
		 					      } catch (Exception e) {
                                    //e.printStackTrace();
		 						  }
								}
								 
								//System.out.println("segment " + segment + " " + cust + " " + i);
								 String prod = null;
									try {
										prod = formatter.formatCellValue(row.getCell((short) 3));
			 					      } catch (Exception e) {
			 					       e.printStackTrace();
			 					      }
									 String ean = null;
										try {
											ean = formatter.formatCellValue(row.getCell((short) 4));
											if(ean != null && ean.trim().length() >0 ) {
											if(prod == null || prod.trim().length() == 0) {
												prod = this.eancodes.get(ean.trim());
											}
											}
										} catch (Exception e) {
				 					     //  e.printStackTrace();
				 					      }	
										
										
									
									Double price = null;
									try {
										price = Double.parseDouble(formatter.formatCellValue(row.getCell((short) 6)));
			 					      } catch (Exception e) {
			 					    	 // e.printStackTrace();
			 					      }

									String text = null;
									try {
										text =  Converter.enCodeEst(formatter.formatCellValue(row.getCell((short) 7)));
			 					      } catch (Exception e) {
	                                   //e.printStackTrace();
			 						  }

									String text2 = null;
									try {
										text2 =  Converter.enCodeEst(formatter.formatCellValue(row.getCell((short) 8)));
			 					      } catch (Exception e) {
	                                   //e.printStackTrace();
			 						  }								

									Date naita = null;

									Cell cell = row.getCell((short)9);
									// naita = (row.getCell((short) 9).getDateCellValue());
										if (cell != null) {
										    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
										        naita = cell.getDateCellValue();
										    } else {
										        DataFormatter formatte = new DataFormatter();
										        String value = formatte.formatCellValue(cell).trim();
										        naita = parseDate(value);

										        if (naita == null) {
										            System.out.println("Invalid naita date: " + value);
										        }
										    }
										}
										
										
									Date algus = null;

									 cell = row.getCell((short)10);
										if (cell != null) {
										    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
										        algus = cell.getDateCellValue();
										    } else {
										        DataFormatter formatte = new DataFormatter();
										        String value = formatte.formatCellValue(cell).trim();
										        algus = parseDate(value);

										        if (algus == null) {
										            System.out.println("Invalid algus date: " + value);
										        }
										    }
										}
										
										
			 						Date lopp = null;
			 						 cell = row.getCell((short)11);
										if (cell != null) {
										    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
										        lopp = cell.getDateCellValue();
										    } else {
										        DataFormatter formatte = new DataFormatter();
										        String value = formatte.formatCellValue(cell).trim();
										        lopp = parseDate(value);

										        if (lopp == null) {
										            System.out.println("Invalid lopp date: " + value);
										        }
										    }
										}

								


								
								Calendar cc = Calendar.getInstance();
								boolean goes= false;
								try {
								if(naita != null) {
									if(cc.getTimeInMillis() > naita.getTime() && cc.getTimeInMillis() < lopp.getTime()) {
									  goes = true;
									}
								 } else {
								if(cc.getTimeInMillis() > algus.getTime() && cc.getTimeInMillis() < lopp.getTime()) {
								  goes = true;	
								}
								}
								
								}catch(Exception e) {
									e.printStackTrace();
									System.out.println("oioi " + cust + " "+ prod + " " + segment + " '" + lopp + "'" );
								}
								
							if(goes) {
								if(activeXLSPromos == null) {
									activeXLSPromos = new HashMap<String,List<String>>();
								}
								
								 if(price == null || price == 1) {
						        	   try {
						        	    price = this.prodprices.get(prod);
						        	   }catch(Exception e) {}
						           }
								 
								if(cust != null && cust.trim().length() > 0) {
									  MatrixLine mline = new MatrixLine();
							           mline.setCustID(cust);
							           mline.setProdID(prod);
							           mline.setStartDate(Converter.getDDsMMMsYYYYString(algus));
							           mline.setEndDate(Converter.getDDsMMMsYYYYString(lopp));
							           if(price == null) {
							        	 //  Sytln("prod " + prod + " " + prod);
							        	   price = 1.0;
							           }
							          
							           mline.setPrice(price-0.001);
							           if(text != null && text.trim().length() > 0) {
							        	   mline.setLineinfo((text + " " + text2).trim().replaceAll("\\r?\\n|\\r", "|").replaceAll(";", ","));
							           }
							           lines.add(mline);
							  	
								}else {
									if(segment != null) {
										
									String asi = segment;
									
									List<String> ll = activeXLSPromos.get(asi);
									if(ll == null) {
									  ll = new ArrayList<String>(); 
									}
									if(!ll.contains(asi + "xls")) {
									   ll.add(asi + "xls");
									}
									activeXLSPromos.put(asi + "xls", ll);
								
									
 									  MatrixLine mline = new MatrixLine();
							           mline.setCliGroup(asi + "xls");
							           mline.setProdID(prod);
							           try {
							           mline.setStartDate(Converter.getDDsMMMsYYYYString(algus));
							           mline.setEndDate(Converter.getDDsMMMsYYYYString(lopp));
							           if(price == null) {
							        	  // System.out.println("asi " + asi + " " + prod);
							        	   price = 1.0;
							           }
							           mline.setPrice(price - 0.001);
							           if(text != null && text.trim().length() > 0) {
							        	   mline.setLineinfo((text + " " + text2).replaceAll("\\r?\\n|\\r", "|").replaceAll(";", ","));
							           }else if(text2 != null && text2.trim().length() > 0){
							        	   mline.setLineinfo((text2).replaceAll("\\r?\\n|\\r", "|").replaceAll(";", ","));
							           }
							           lines.add(mline);
							           }catch(Exception e) {e.printStackTrace();
							           System.out.println("oooo '" + algus + "' " + lopp);
							           }
							           }
								
							}
								
							}
							
							}
							
							
         }catch(Exception r) {
		 r.printStackTrace();  
	     }	
	//System.out.println("Active Promos "  + activeXLSPromos.toString());

  }
  
  private static Date parseDate(String text) {
	    List<DateTimeFormatter> formats = Arrays.asList(
	        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
	        DateTimeFormatter.ofPattern("d.M.yyyy"),
	        DateTimeFormatter.ISO_LOCAL_DATE,
	        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
	        DateTimeFormatter.ofPattern("MM/dd/yyyy")
	    );

	    for (DateTimeFormatter f : formats) {
	        try {
	            LocalDate ld = LocalDate.parse(text, f);
	            return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
	        } catch (DateTimeParseException ignored) {
	        }
	    }

	    return null;
	}
  
  
   public Vector getPriceMatrixData() {
       Vector lines = new Vector();
       
       if(custCategories == null) {
    	   custCategories = this.getCustCategories();
       }
       getCampExcelLines(lines);
	try {
	String sss = "Select c.pricelistBackOfficeId as pricegroup\r\n" + 
			"                       				 from  dbo.EPASAULE_CLIENTS c inner join EPASAULE_AGENTS_CLIENTS a on a.ClientBackofficeID = c.BackOfficeId group  by c.pricelistBackOfficeId";
	
                         
    Connection con = getNewConnection();
 	Statement stmt = con.createStatement();
    
 	 ResultSet  rs = stmt.executeQuery(sss);
 	 HashSet<String> prs = new HashSet<String>();

     while (rs.next()) {
    	 prs.add(rs.getString("pricegroup"));
     }
      rs.close();
 	
      
	    String sql4a = "SELECT p.pricelistBackOfficeId as grupp, p.productBackOfficeId as toode, p.price as hind" + 
	    		" FROM EPASAULE_PRICES p  where "
	    		+ "p.price > 0  " ;	
	    
	    sql4a = "SELECT p.custpricegroup as grupp, p.itemid as toode, p.price as hind" + 
	    		" FROM MJ_PRICES p  where "
	    		+ "p.price > 0  " ;	
	    
           rs = stmt.executeQuery(sql4a);
           while (rs.next()) {

                   String gr = rs.getString("grupp");
                   if(prs.contains(gr)) {
                   String tt = rs.getString("toode");
                   Double pr = rs.getDouble("hind");
                             MatrixLine mline = new MatrixLine();
                                       mline.setCliGroup(gr);
                                       mline.setProdID(tt);
                                       mline.setPrice(pr);
                               lines.add(mline);
                   }
                   
             }
           
           rs.close();    	
      
           String sql2 = " 		 select   a.promotionBackOfficeId as clgroup , productBackOfficeId as prodid, [date from] as startday,[date to] as endday,\r\n" + 
           		"			  max([beginsinstoredate]) as store,min(datediff(day,beginsinstoredate, c.nextdelivery)) as vahe,\r\n" + 
           		"              		minPrice as price ,(case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end) as dealer, max(isPaper) as ispaper , MANDATORY from dbo.EPASAULE_PROMOTIONOBJECTS a  \r\n" + 
           		"              		inner join dbo.EPASAULE_PROMO b on a.promotionBackOfficeId = b.promotionBackOfficeId \r\n" + 
           		"        		inner join EPASAULE_CLIENTS c ON a.clientBackOfficeId = c.BackOfficeId\r\n" + 
           		"         		 	 where c.nextdelivery between [date from] and [date to]  and minqty = 0 and minPrice = maxPrice  \r\n" + 
           		"         		  group by  a.promotionBackOfficeId , productBackOfficeId, (case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end),  [date from] ,[date to], minPrice,MANDATORY \r\n" + 
           		"                  order by a.promotionBackOfficeId , productBackOfficeId, (case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end), [date from] ,[date to], minPrice,MANDATORY\r\n" + 
           		"					 ";
           rs = stmt.executeQuery(sql2);
           while (rs.next()) {

           String clGroup = rs.getString("clgroup");
           String prodID = rs.getString("prodid").trim();
           String sdate = rs.getString("startday");
           String edate = rs.getString("endday");
           String store = rs.getString("store");
           Integer vahe = rs.getInt("vahe");
           double price = rs.getDouble("price");
           String dealer = rs.getString("dealer");
           Integer isPaper =  rs.getInt("isPaper");
           String manda = rs.getString("MANDATORY"); // Y või N kui N siis peaks värvima.
            
           
           MatrixLine mline = new MatrixLine();
           mline.setCliGroup(clGroup);
           mline.setProdID(prodID);
           mline.setStartDate(store.substring(0,10) + "/" + sdate.substring(0,10));
           mline.setEndDate(edate.substring(0,10));
           String lisa = "";
           if(vahe < 1) {
             lisa = "SOON ";
           }
           
           // ;AKC1000032775
           if(isPaper != null && isPaper == 1) {
        	   lisa = lisa  + Converter.enCodeEst("AVĪZE") ;
           }
           
           if(lisa.trim().length() > 0) {
        	mline.setLineinfo(lisa.trim());   
           }
           
           mline.setPrice(price);
            if(dealer != null && !dealer.trim().equals("24N")) {// kui ei ole pealadu siis paneme külge
            	mline.dealer = dealer.trim();
            }
           lines.add(mline);
           
           }
           rs.close();
           stmt.close();
      
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
	  StringBuffer result = new StringBuffer();
		
		try {
			
			String cmd2 = "select productBackOfficeId as ItemID from EPASAULE_PRODUCTS_ADVERTISING";
			
			String  cmd = " SELECT  s.assortimentBackOfficeId as ClientGR ,s.productBackOfficeId as ItemID , s.NEWINASSORT as newin, EG_ASSORTITEMSTATUS as status " + 
					"	FROM EPASAULE_ASSORTMENTS s inner join dbo.EPASAULE_PRODUCTS a ON a.backOfficeId = s.productBackOfficeId "
					+ " where s.EG_ASSORTITEMSTATUS in (0,1,2) order by str(a.posWeight) + a.Name ";
			
		
		         if (isDebug()) System.out.println(cmd);
		           Connection con = getNewConnection();
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd2);

		           String ags =   sortags.get("SUGGED");
		           while (rs.next()) {

			             String prodID = rs.getString("ItemID").trim();
			             if(usedProds.contains(prodID)){
								result.append( " ;" + Converter.cleanLitLatEst("SUGGED; ;" + prodID + "; ; ; ; ;"+ags)+ "\n");
			 				}
						
		           }
		           rs.close();

				    rs = st.executeQuery(cmd);
		           
		           while (rs.next()) {

		             String sortid = rs.getString("ClientGR").trim();
		             String prodID = rs.getString("ItemID").trim();
		             int status = rs.getInt("status");
		       
		            ags =   sortags.get(sortid);
		           if(ags == null) {
		        	   ags = "";
		           }
		           String must = " ";
		           if(status == 1) {
		        	   must = "TOP";
		           }
		           
		             if(usedProds.contains(prodID)){
							result.append( " ;" + Converter.cleanLitLatEst(sortid + "; ;" + prodID + ";" + must+ "; ; ; ;"+ags)+ "\n");
		 				}
				      }
						
					 rs.close();
					 st.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
             

	//	System.out.println("ASSO size " + result.toString());
	
		return result.toString();


  }

  public String getSaveStockInfo(){
	  

	  String  cmd = "Select productBackofficeId as toode,sum(Inventory) as stock, regionBackofficeId as ladu, bestBefore as partii from  dbo.EPASAULE_INVENTORIES_detail \r\n" + 
	  		"       	           		group by productBackofficeId,regionBackofficeId,bestBefore \r\n" + 
	  		"					union	" + 
	  		"			select PARENTITEMID as toode,(BOMQTY) as stock , '24N' as ladu, '' as partii  from 	EPASAULE_PHANTOMBOM " + 
	  		"		order by 1, 3 desc,4 " ;
    
	  if(usedProds == null){
		    this.getProducts(null);
		  }
	  HashSet olemas  = new HashSet(); 
	  
	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
	    	   Connection con = this.getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);
	      TreeMap koodid = new TreeMap ();
	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	           String dealer = rs.getString("ladu");
	           if(dealer.equals("24EWC")) {
	        	   dealer = "24N";
	           }
	           String part = rs.getString("partii");
	           String gg = " ";
	           if(part != null && !part.startsWith("1900")) {
	               gg =  part.substring(0,10);
	           }
	           
	           String key = kood+dealer;
	           
	           if(seis > 0 && usedProds.contains(kood) ){
	        	       products.append(kood +  ";");
	                   products.append(" ;");
	                   String parti = gg;
	                   if(olemas.contains(key)) {
	                	   parti = "";
	                   }
	                   products.append(getParent().getNF().format(seis) + ";" + dealer + "; ;" + parti + "\n");
	                   olemas.add(key);
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

  
  HashSet usedProds = null;
  
  public DealerProduct getDealerProductsList() {
	  DealerProduct dealerProds = new DealerProduct();

      try {

    	  String  cmd = "Select productBackofficeId as toode,sum(Inventory) as stock ,(case when regionBackofficeId = '24EWC' then '24N' else regionBackofficeId end) as ladu from \r\n" + 
    	  		"    	  			  		 dbo.EPASAULE_INVENTORIES  group by productBackofficeId,(case when regionBackofficeId = '24EWC' then '24N' else regionBackofficeId end)";
    
    	  if(usedProds == null){
   			  this.getProducts(null);
   		  }
           if (isDebug()) System.out.println(cmd);
           Connection con = getNewConnection();
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(cmd);
                while (rst.next()) {
                  String toode = rst.getString("toode");
                  if (toode != null){
                  toode = toode.trim();
                  if(usedProds.contains(toode)){
               	   double seis = rst.getDouble("stock");
               	        String ladu  =  rst.getString("ladu").trim();
               	     if(ladu.equals("24EWC")) {
      	        	       ladu = "24N";
      	               }
	                    dealerProds.add(ladu, toode,((int)seis)+""," "," " );
                     }
               
                   }
                  }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          e.printStackTrace();
      }

      
       return dealerProds;
     
     }

  
  
  public TreeMap getDealers() {

      TreeMap dealers = new TreeMap();
      try {

    	  String cmd = " select inventlocationid  as ladu , name from EPASAULE_REGIONS";

           if (isDebug()) System.out.println(cmd);
           Connection con = getNewConnection();
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(cmd);
                while (rst.next()) {
                  String kood = rst.getString("ladu").trim();
                  if(!kood.equals("24EWC")) {
                     Dealer dealer = new Dealer(kood);
                     dealer.setName(Converter.enCodeEst(rst.getString("name").trim()));
                     dealers.put(dealer.getID(), dealer);
                   }
                  }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          e.printStackTrace();
      }

      
      System.out.println("Dealers size :" + dealers.size());
      
	  return dealers;
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

  private void makeCustprod(){
		 
	  
		  String cmd1 = " SELECT a.CUSTACCOUNT AS klient, a.ITEMID AS toode, sum(a.INVOICEQTY) AS kogus  FROM EPASAULE_SALESLINE a " + 
		  		"   WHERE a.SALESDATE > GETDATE() - 300 AND a.EG_SALESCANCELREASONID <> 'GKR' AND a.INVENTLOCATIONID != 'STORNO' " + 
		  		"   AND a.SALESSTATUS <> 1  group by CUSTACCOUNT, ITEMID  " ;
		    
		  
		  
		  String cmd2 = "   WITH cte AS (  \r\n" + 
		  		"	  			  		    SELECT    \r\n" + 
		  		"	  			  		        a.CUSTACCOUNT AS klient,  \r\n" + 
		  		"	  			  		        a.ITEMID AS toode,  \r\n" + 
		  		"	  		                    RIGHT('00000000' + CAST(t.posWeight AS VARCHAR(20)), 8) as sorting,  \r\n" + 
		  		"	  			  		        a.SALESQTY AS tellitud,  \r\n" + 
		  		"	  			  		        a.INVOICEQTY AS sisse,  \r\n" + 
		  		"	  			  		        (a.SALESQTY - a.INVOICEQTY) AS tarnimata,  \r\n" + 
		  		"	  			  		        c.NAME AS CANCELREASON,  \r\n" + 
		  		"	  			  		        a.SALESDATE AS lastdate,  \r\n" + 
		  		"	  			  		        (a.INVOICEAMOUNT / CASE WHEN a.INVOICEQTY <> 0 THEN a.INVOICEQTY ELSE 1 END) AS lastprice,  \r\n" + 
		  		"	  			  		        a.SALESID AS ordernr,  \r\n" + 
		  		"	  			  		        ROW_NUMBER() OVER (  \r\n" + 
		  		"	  			  		            PARTITION BY a.CUSTACCOUNT, a.ITEMID  \r\n" + 
		  		"	  			  		            ORDER BY a.SALESDATE DESC  \r\n" + 
		  		"	  			  		        ) AS rn  \r\n" + 
		  		"	  			  		    FROM EPASAULE_SALESLINE a \r\n" + 
		  		"	  		 Inner join dbo.EPASAULE_PRODUCTS t on t.backOfficeId = a.ITEMID  \r\n" + 
		  		"	  			  		    LEFT JOIN EPASAULE_SALESCANCELREASONS c  \r\n" + 
		  		"	  			  		        ON c.EG_SALESCANCELREASONID = a.EG_SALESCANCELREASONID  \r\n" + 
		  		"	  			  		    WHERE a.SALESDATE > GETDATE() - 300  \r\n" + 
		  		"	  			  		      AND a.EG_SALESCANCELREASONID <> 'GKR'  \r\n" + 
		  		"	  			  		      AND a.INVENTLOCATIONID != 'STORNO'  \r\n" + 
		  		"	  			  		      AND a.SALESSTATUS <> 1  )  \r\n" + 
		  		"	  			  		SELECT *  \r\n" + 
		  		"	  			  		FROM cte  \r\n" + 
		  		"	  			  		WHERE rn = 1 order by klient, sorting ,toode ";
		
		  
		    try {
		    	
		    	Calendar cal = Calendar.getInstance();
		    	cal.add(Calendar.DAY_OF_MONTH, -15);
		    	
		    	HashMap<String, Double> last180d = new HashMap<String,Double>();
		    	
		    	Connection con = getNewConnection();
		     	Statement stmt = con.createStatement();
		     	ResultSet rs = stmt.executeQuery(cmd1);
		    	
			      while(rs.next()){
			    	    last180d.put(rs.getString("klient").trim() + "-" +  rs.getString("toode").trim(), rs.getDouble("kogus"));
			        }
			        rs.close();
		     	
			        HashMap lisainfo = new HashMap();
			        String oldkey = null;
			        int count = 0;
			        

			    String last3 = " SELECT jou.clientBackOfficeId as klient , jou.productBackOfficeId as toode , " + 
			    		"	jou.Quantity  as kogus, jou.Price as lastprice, jou.deliverydate  as lastdate, (jou.invoiceNumber) as lastdok "
			    		+ " FROM EPASAULE_DELIVERIES  jou  WHERE  jou.deliverydate >= getdate()-300 and DOCUMENTTYPE in (3) " + 
			    		"	 order by jou.clientBackOfficeId,  jou.productBackOfficeId, jou.deliverydate desc " ;
			    	 
			    	 rs = stmt.executeQuery(last3);
			    	 
			    	 while(rs.next()){

			              String custID = rs.getString("klient").trim();
			              String prodID = rs.getString("toode").trim();;
			              double amount = rs.getDouble("kogus");
			              double lprice = rs.getDouble("lastprice");
				          String lastdate = rs.getString("lastdate").trim().substring(0,10);
			              String ldoc = rs.getString("lastdok");
			              String key = custID + "-" + prodID;
			            
			              if(oldkey == null) {
			              	   oldkey = key;
			                 }
			                
			                if(!oldkey.equals(key)) {
			              	   count = 0;
			                 }
			                
			                 	if(count < 3) {
			                 		String value = ldoc + "," + lastdate + "," +this.getNF().format(amount) + "," + this.getNF().format(lprice) ;
			                 		if(lisainfo.get(key) != null) {
			                 			value = lisainfo.get(key) + "|" + value;
			                 		}
			                 		 lisainfo.put(key, value);
			                 		 count++;
				                }
			                  
			                 oldkey = key;
			                
			        }

			      rs.close();

			        
			        
		     	
		     	 rs = stmt.executeQuery(cmd2);
		
		      while(rs.next()){
		        String kliendikood = rs.getString("klient").trim();
		        String tootekood = rs.getString("toode").trim();

		        if(usedProds.contains(tootekood)){
		        double kogus = rs.getDouble("sisse");
		        double mata = rs.getDouble("tarnimata");
			    String reason =  rs.getString("CANCELREASON");
			    if(reason != null && reason.trim().length() > 0) {
			    	reason = Converter.enCodeEst(reason.trim());
			    } else {
			    	reason = " ";
			    }
		        
			    String dateStr = rs.getString("lastdate").substring(0,10);
			    LocalDate date = LocalDate.parse(dateStr,
		                 java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		         long millis = date.atStartOfDay(ZoneId.systemDefault())
		                           .toInstant()
		                           .toEpochMilli();
		         if(millis < cal.getTimeInMillis()) {
		        	 mata = 0;
		        	 reason = " ";
		         }
			    
			    String lastdate = Converter.turn(dateStr); // EESTI CASE
		                 lastdate = lastdate.substring(0,5);
		                 double lastprice = rs.getDouble("lastprice");
		          
		         // LAT puhul on 300 päeva
		         double sold180 = 0;
		         Double aaa = last180d.get(kliendikood + "-" + tootekood);
		         if(aaa > 0) {
		        	 sold180 = aaa;
		         }
		         if(kogus != 0) {
		  	       CustProd cp = new CustProd(kliendikood, tootekood, kogus , sold180 , 0,lastdate,lastprice,mata,0,reason);

                   String key = kliendikood+ "-" + tootekood; // lisame tagastus docid.
                   		
                   		if(lisainfo.get(key) != null) {
                   			cp.returnData = lisainfo.get(key)+"";
                   		}
                   		
		             custProds.add(cp);
		         }
		        }
		      }
		      rs.close();
		      stmt.close();
		      con.close();
		    } catch (Exception e){
		      getParent().showError(e);
		    }
		    System.out.println("CustProds size " + custProds.size() );
	  }
	  
  
  
  private Map<String, Map<String, Double>> manaPlans;
  private Map<String, Map<String, Double>> custPlans;
  private Map<String, Double> manaTotalPlans;
  private Map<String, Double> custTotalPlans;
  private Map<String, String> groupGroupingFocus;
  private Map<String, String> groupGrouping;
  private ArrayList<String> noSalesGroups;
  
  
  private void loadPlans () {
	  
	  
	  
	  custPlans = new HashMap<String, Map<String, Double>>();
	  manaPlans = new HashMap<String, Map<String, Double>>();
	  custPlanGruppedFocus = new HashMap<String, Map<String, Double>>();
	  custPlanGrupped = new HashMap<String, Map<String, Double>>();
	  custTotalPlans = new HashMap<String, Double>();
	  manaTotalPlans = new HashMap<String, Double>();
	  groupGroupingFocus = new HashMap<String, String>();
	  groupGrouping = new HashMap<String, String>();
	  
	  noSalesGroups = new  ArrayList<String>();
	

		
	  
	  
	  
	  try{
		 	
	 		Calendar cal = Calendar.getInstance();
	 	     String month = (cal.get(Calendar.MONTH)+1)+ "";
	 	     if(month.length() == 1){
	 		   month = "0" + month;
	 	      }
	 	   String jooksev =  month + "" + cal.get(Calendar.YEAR) ; 
	 	   
	 	   File f = new File (getExcelDir() + "CustomerPlans.xlsx");
	 	   FileInputStream	 ff = new FileInputStream(f);
	 	

			DataFormatter formatter = new DataFormatter();
	 		XSSFWorkbook workbook = new XSSFWorkbook((ff));
	 		XSSFSheet sheet = workbook.getSheet("Grouping");
	 		XSSFRow row = null; 
	 		
	 		if(sheet != null) {
	 			for(int i = 0 ; i < sheet.getLastRowNum() + 1;i++){
					 row = sheet.getRow(i);
					String prGR = formatter.formatCellValue(row.getCell(0));
					String grGR = formatter.formatCellValue(row.getCell(1));
					//groupGrouping.put(prGR, grGR);
	 		   }
	 		}
	 		
	 		
	 		 sheet = workbook.getSheet("GroupsNoSale");
	 		 row = null; 
	 		
	 		if(sheet != null) {
	 			for(int i = 0 ; i < sheet.getLastRowNum() + 1;i++){
					 row = sheet.getRow(i);
					String prGR = formatter.formatCellValue(row.getCell(0));
					noSalesGroups.add(prGR);
	 		   }
	 		}
	 		
	 		 String year = Calendar.getInstance().get(Calendar.YEAR)+"";
	 		 sheet = workbook.getSheet("Motivation " + year);
	 		 row = null; 
	 		
	 		if(sheet != null) {
	 			
	 			String currentMonth =
	 			        java.time.Month.of(Calendar.getInstance().get(Calendar.MONTH) + 1)
	 			                .getDisplayName(
	 			                        java.time.format.TextStyle.FULL,
	 			                        Locale.ENGLISH);

	 			 row = sheet.getRow(6);

	 			int monthCellIndex = -1;

	 			for (int c = 0; c < row.getLastCellNum(); c++) {

	 			   Cell cell = row.getCell(c);

	 			    String value = formatter.formatCellValue(cell).trim();

	 			    if (value.equalsIgnoreCase(currentMonth)) {
	 			        monthCellIndex = c ;
	 			        break;
	 			    }
	 			}

	 			System.out.println("Month = " + currentMonth);
	 			System.out.println("Found in column = " + monthCellIndex);
	 			
	 			if(monthCellIndex != -1) {
	 			for(int i = 7 ; i < sheet.getLastRowNum() + 1;i++){
					 row = sheet.getRow(i);
					 
					String prGR = formatter.formatCellValue(row.getCell(0));
					String grGR = formatter.formatCellValue(row.getCell(monthCellIndex));
					if(grGR!= null && grGR.length() > 0) {
						groupGroupingFocus.put(prGR, grGR);	
					}

				     grGR = formatter.formatCellValue(row.getCell(1));
				 	 groupGrouping.put(prGR, grGR);	
					
	 		     }
	 		   }
	 		 }
	 		
	 		//System.out.println("groupGrouping " + groupGrouping.toString());	
	 		
	 		
	 		sheet = workbook.getSheet(jooksev);
	 	
	 				ff.close();	
	 	
	 				if(sheet != null){
	 					for(int i = 1 ; i < sheet.getLastRowNum() + 1;i++){
	 						 row = sheet.getRow(i);
	 	
	 						String mana = formatter.formatCellValue(row.getCell(0));
	 						String cust = formatter.formatCellValue(row.getCell(1));
	 						String gr = formatter.formatCellValue(row.getCell(2));
	 					
	 							 Double plan = null;
	 							 try {
	 							  plan = Double.parseDouble(formatter.formatCellValue(row.getCell(3)));
	 							 }catch(Exception e) {}

	 								   if(plan != null && plan > 0) {
	 									  manaPlans
	 						                .computeIfAbsent(mana, k -> new HashMap<String, Double>())
	 						                .merge(gr, plan, Double::sum);

	 						             custPlans
	 						                .computeIfAbsent(cust, k -> new HashMap<String, Double>())
	 						                .merge(gr, plan, Double::sum);
	 						            if (this.groupGroupingFocus.get(gr) != null) {
	 						  			 custPlanGruppedFocus
	 						                .computeIfAbsent(cust, k -> new HashMap<String, Double>())
	 						                .merge(groupGroupingFocus.get(gr), plan, Double::sum);
	 						            }
	 						            
	 						           if (this.groupGrouping.get(gr) != null) {
		 						  			 custPlanGrupped
		 						                .computeIfAbsent(cust, k -> new HashMap<String, Double>())
		 						                .merge(groupGrouping.get(gr), plan, Double::sum);
		 						            }
	 								  }
	 								}
	 					
	 					
	 					manaPlans.forEach((manager, groups) ->
	 				    manaTotalPlans.put(manager,
	 				        groups.values().stream().mapToDouble(Double::doubleValue).sum())
	 				);

	 				custPlans.forEach((customer, groups) ->
	 				    custTotalPlans.put(customer,
	 				        groups.values().stream().mapToDouble(Double::doubleValue).sum())
	 				);

	 				}
	 
	 				System.out.println("manaPlans " + manaPlans.size());
	 				System.out.println("manaTotPlans " + manaTotalPlans.size());
	 				System.out.println("custPlans " + custPlans.size());
	 				
	 				//System.exit(0);
	   }catch(Exception e) {
		   e.printStackTrace();
	   }

	  getCustomerPRGroupSales(); 
  }
  
  
  
  private HashSet<String> agsActive = null;
  
  public TreeMap getAgents(){
	
      agsActive = new HashSet<String>();
      String todayCycleCode = getCycleDay();
      System.out.println("Today is: " + todayCycleCode);

      Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
	    
    String cmd = "select backOfficeId as kood ,agentName as name , repricingLimit as repr , email , phone from  dbo.EPASAULE_AGENTS";

    String cmdg = "select Agent_BackofficeID as kood , group_backofficeID as grupp,forSale, forMerch from EPASAULE_AGENTS_EAGENTGROUPS";
    String cmdb = "select Agent_BackofficeID as kood , brand_backofficeID as brand,forSale, forMerch from EPASAULE_AGENTS_EAGENTBRANDS";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
    try {
    	Connection con = getNewConnection();
        Statement stmt =  con.createStatement();
        ResultSet	rs = stmt.executeQuery(cmdg);
        
        HashMap grs = new HashMap();
         
         while(rs.next()){
          String agentID = rs.getString("kood").trim();
          
          
          
          String gr = rs.getString("grupp").trim();
          ArrayList gr1 = (ArrayList)grs.get(agentID);
        		  if(gr1 == null) {
        			 gr1 =  new ArrayList();
        		  }
        	gr1.add(gr);
        	grs.put(agentID, gr1);
         }
        
        rs.close();
        
        HashMap grsS = new HashMap();
        
        rs = stmt.executeQuery(cmdb);
        while(rs.next()){
         String agentID = rs.getString("kood").trim();
         String br = rs.getString("brand").trim();
         ArrayList gr1 = (ArrayList)grsS.get(agentID);
       		  if(gr1 == null) {
       			 gr1 =  new ArrayList();
       		  }
       	gr1.add(br);
       	grsS.put(agentID, gr1);
        }
       
       rs.close();
        	
        rs = stmt.executeQuery(cmd);
    
        while(rs.next()){
        String agentID = rs.getString("kood").trim();
          Agent agent = new Agent(agentID);
          
          String name = rs.getString("name").trim();
          Double reprLim = rs.getDouble("repr");
          if(reprLim > 0) {
        	  agent.repriceLimit = reprLim;
           } else if(reprLim < 0) {
        	  agent.repriceLimit = -1.0;
           } else {
         	  agent.repriceLimit = 0.0;
           }

          String email = rs.getString("email");
          if(email != null && email.trim().length() > 0) {
        	  agent.restEmail = email.trim();
          }
          
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length() > 0) {
        	  agent.restPhone = phone.trim();
        	  agent.setPhone(phone.trim());
          }
          
          agent.setName(name);
          agent.setIsPilot(true);
          agent.todayCycleCode = todayCycleCode;
          
          if(grs.size() > 0 && grs.get(agentID) != null) {
        	  agent.setProdGroupList((ArrayList)grs.get(agentID));
          }
          
          if(grsS.size() > 0 && grsS.get(agentID) != null) {
        	  agent.setProdGroupListSec((ArrayList)grsS.get(agentID));
          }
          
          agent.setLanguage("LAT");
          agent.setCountry("lv");
         // agent.setPassWord("0000");
        

   		String agentAGENTS = props.getProperty(agent.getID());
 		if(agentAGENTS != null){
 			agsActive.add(agent.getID());
 			agent.setActive(true);
 		}
          
          agents.put(name + " " + agent.getID(), agent);
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

  private TreeMap agent1 = new TreeMap();
  
   
   
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  

  private TreeMap makeCustomerDeliveryDays(){
		TreeMap res = new TreeMap();
		try{

	      
	String sql3 = "select    BackOfficeId as klient ,  nextdelivery as paev1 ,nextdelivery2 as paev2 \r\n" + 
			"	  from dbo.EPASAULE_CLIENTS";

	Connection con = this.getNewConnection();
		Statement stmt = con.createStatement();

	ResultSet rs3 = stmt.executeQuery(sql3);
	while(rs3.next()){
	String klient = rs3.getString("klient").trim();
	java.sql.Date day1 = rs3.getDate("paev1");
	java.sql.Date day2 = rs3.getDate("paev2");

	String olemas = null;
	
	if (day1.getTime() > 10000) {
		olemas = day1.getTime() + "";
		
		if (day2.getTime() != day1.getTime() && day2.getTime() > 10000) {
	        olemas = olemas + "," +  day2.getTime();
		}
		
			res.put(klient, olemas);
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
  

  
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final LocalDate CYCLE_START =   LocalDate.parse("03.11.2025", FMT);
  
  
 
  public static boolean matchesCycle( String pattern) {

      LocalDate date = LocalDate.now();  // or any date to test
	  
      long days = Duration.between(CYCLE_START.atStartOfDay(), date.atStartOfDay()).toDays();

        if (days < 0) {
          return false; // before cycle start
         }

      int weekInCycle = (int)((days / 7) % 8) + 1;  // 1–8
      int weekday = date.getDayOfWeek().getValue(); // 1=Mon…7=Sun

      char cycleWeekLetter = (char)('A' + (weekInCycle - 1)); // A–H

      // Build what we expect, e.g. "A1"
      String expected = "" + cycleWeekLetter + weekday;

      // Check if pattern contains expected code
      return pattern.contains(expected);
  }
  
  public static int[] countMonthlyDeliveries(String pattern) {

	    LocalDate today = LocalDate.now();
	    YearMonth currentMonth = YearMonth.from(today);

	    LocalDate firstDay = currentMonth.atDay(1);
	    LocalDate lastDay = currentMonth.atEndOfMonth();

	    int totalDeliveries = 0;
	    int passedDeliveries = 0;

	    for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {

	        long days = Duration.between(
	                CYCLE_START.atStartOfDay(),
	                date.atStartOfDay()).toDays();

	        if (days < 0) {
	            continue; // before cycle start
	        }

	        int weekInCycle = (int)((days / 7) % 8) + 1; // 1–8
	        int weekday = date.getDayOfWeek().getValue(); // 1=Mon…7=Sun
	        char cycleWeekLetter = (char)('A' + (weekInCycle - 1)); // A–H

	        String expected = "" + cycleWeekLetter + weekday;

	        if (pattern.contains(expected)) {

	            totalDeliveries++;

	            if (date.isBefore(today)) {
	                passedDeliveries++;
	            }
	        }
	    }

	    return new int[]{ totalDeliveries, passedDeliveries };
	}


  public static List<CycleMatch> nextSevenDaysMatches(String pattern) {

	    DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	    LocalDate cycleStart = LocalDate.parse("03.11.2025", FMT);

	    LocalDate today = LocalDate.now();
	    List<CycleMatch> result = new ArrayList<>();

	    for (int i = 0; i < 7; i++) {

	        LocalDate date = today.plusDays(i);

	        long days = ChronoUnit.DAYS.between(cycleStart, date);

	        if (days < 0) {
	            continue;
	        }

	        // 4-week cycle
	        int weekInCycle = (int) ((days / 7) % 4);

	        // Monday=1 ... Sunday=7
	        int weekday = date.getDayOfWeek().getValue();

	        // A, B, C, D
	        char cycleWeekLetter = (char) ('A' + weekInCycle);

	        // Find corresponding block
	        int index = pattern.indexOf(cycleWeekLetter);

	        if (index != -1 && index + 1 < pattern.length()) {

	            StringBuilder daysPart = new StringBuilder();

	            for (int j = index + 1; j < pattern.length(); j++) {

	                char c = pattern.charAt(j);

	                // Stop at next week letter
	                if (Character.isLetter(c)) {
	                    break;
	                }

	                daysPart.append(c);
	            }

	            // Check if weekday exists in block
	            if (daysPart.toString().contains(String.valueOf(weekday))) {

	                result.add(new CycleMatch(
	                        date,
	                        cycleWeekLetter + String.valueOf(weekday)
	                ));
	            }
	        }
	    }

	    return result;
	}


	public static LocalDate nextMatchingDate(String pattern) {

	    LocalDate date = LocalDate.now().plusDays(1); // start from tomorrow

	    while (true) {

	        long days = ChronoUnit.DAYS.between(
	                CYCLE_START,
	                date
	        );

	        if (days >= 0) {
	            int weekInCycle = (int) ((days / 7) % 8) + 1; // 1–8
	            int weekday = date.getDayOfWeek().getValue(); // 1–7

	            char weekLetter = (char) ('A' + weekInCycle - 1);
	            String code = "" + weekLetter + weekday;

	            if (pattern.contains(code)) {
	                return date;
	            }
	        }

	        date = date.plusDays(1);
	    }
	}

  public TreeMap<String,ArrayList <String>> getCustCategories(){
		
		  
		  TreeMap<String,ArrayList <String>> res = new TreeMap<String,ArrayList <String>>();
		  // custcode - channel - segment -
		
          String cmd = " Select BackOfficeId as accountnum, segmentName from  dbo.EPASAULE_CLIENTS a  " ; 
          

           if (isDebug()) System.out.println(cmd);
          
         try {
           Connection con = this.getNewConnection();
      	   Statement stmt = con.createStatement();
  	       ResultSet rs = stmt.executeQuery(cmd);
			
			while(rs.next()){
			
				   String accountnum = rs.getString("accountnum").trim();
		           String key = rs.getString("segmentName");
		           
		            if(key != null && key.length() > 0) {
		            	key = key.trim();
			            if (!res.containsKey(key)) {
			                res.put(key, new ArrayList<>());
			            }
			            res.get(key).add(accountnum);		            	
		            }
			
			}
			rs.close();
			stmt.close();
			con.close();
          
          } catch (Exception e) {e.printStackTrace();} 
		  return res;
	  }
  

    private TreeMap<String,ArrayList <String>> custCategories = null;
  
    


    
    private static Map<Character, Set<Integer>> parse(String input) {
        Map<Character, Set<Integer>> map = new HashMap<>();

        char current = 0;

        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                current = c;
                map.put(current, new HashSet<>());
            } else if (Character.isDigit(c)) {
                map.get(current).add(Character.getNumericValue(c));
            }
        }
        return map;
    }
    
    
    
    
    private Map<String, String> groupNames = null;
    
    
    private void getGroupNames() {
	try {	
		  groupNames = new HashMap<String, String>();
		  
		  Connection con = this.getNewConnection();
	      Statement stmt = con.createStatement();

		
		  String    sql = "  SELECT backOfficeId as liik,Name as nimetus FROM dbo.EPASAULE_BRANDS ";
	  
		  ResultSet  rs = stmt.executeQuery(sql);
	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      groupNames.put(liik, rs.getString("nimetus").trim());
	    }
	    
	    rs.close();
	    stmt.close();
		con.close();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
    }
    
  private HashMap makeCustBudgetMap() {
	  
	  if(groupNames == null) {
		  getGroupNames();
	  }
	  
	 
	        LocalDate start = LocalDate.of(2025, 11, 3);
	        LocalDate today = LocalDate.now();

	        HashMap<String, Integer> customerDeliveries = new HashMap<String, Integer>();
	        String aaa = "SELECT AgentBackofficeID as kood , ClientBackofficeID as klient, route  from  dbo.EPASAULE_AGENTS_CLIENTS " ;
      		try {
	    	   Connection con = this.getNewConnection();
	    	 
	    		 Statement stmt = con.createStatement();
	    	     ResultSet rs1 = stmt.executeQuery(aaa);
	    	               while(rs1.next()){
	    	                 String ag = rs1.getString("kood").trim();
	    	                 String klient = rs1.getString("klient").trim();
	    	                 String pattern = rs1.getString("route");
	        

	            Map<Character, Set<Integer>> schedule = parse(pattern);

	            int done = 0;
	            int remaining = 0;

	            YearMonth currentMonth = YearMonth.from(today);
	            LocalDate date = currentMonth.atDay(1);
	            LocalDate end = currentMonth.atEndOfMonth();

	            while (!date.isAfter(end)) {

	                if (!date.isBefore(start)) {

	                    long weeksBetween = ChronoUnit.WEEKS.between(start, date);
	                    int cycleIndex = (int) (weeksBetween % 4);
	                    char weekKey = (char) ('A' + cycleIndex);
	                    int dayOfWeek = date.getDayOfWeek().getValue();
	                    if (schedule.containsKey(weekKey) &&
	                        schedule.get(weekKey).contains(dayOfWeek)) {
	                        if (!date.isAfter(today)) {
	                            done++;
	                        } else {
	                            remaining++;
	                        }
	                    }
	                }
	                date = date.plusDays(1);
	            }
	            customerDeliveries.put(klient, done + remaining);
	            customerDeliveries.put(klient+"rem", remaining);
	            
	          //  System.out.println(name + " -> done: " + done + ", remaining: " + remaining);
	        }
	    	  
      		} catch(Exception e) {e.printStackTrace();}
	    	               
	    	               
	  HashMap<String,String> res = new HashMap<String,String>();
	  
      
      int yy = Calendar.getInstance().get(Calendar.YEAR);
      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
      int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
      double planprec = dd/d2;
   
      // siin itereerime plaani läbi 
      
     for(String custID : custPlans.keySet()) {
      
      String asi = "";
   
      
      if (customerDeliveries.get(custID) != null) {
    	  asi = " |Kopa/Atlicis piegades :  " + customerDeliveries.get(custID)+" / "+ customerDeliveries.get(custID + "rem") + "||";
      }
      
        double cSales = 0 ;
        if(custTotalSales.get(custID) != null) {
        	cSales = custTotalSales.get(custID).doubleValue();
        }
        double cpSales = 0 ;
        if(custTotalPlans.get(custID) != null) {
        	cpSales = custTotalPlans.get(custID).doubleValue();
        }

        double plaanitavMyykSal = d2 * cSales/dd;
        double  tehtudsalperc = cSales/cpSales;
        double  prognosalperc = plaanitavMyykSal/cpSales;
        if(tehtudsalperc > 10){
    		  tehtudsalperc = 0;  
    	  }
    	  
    	  if(prognosalperc > 10){
    		  prognosalperc = 0;  
    	  }
    	  

        
       asi = asi + "Izpilde/Plan :   " + (int)cSales + " € / " + (int)cpSales+ " €|";
      
      if(dd > 6 && tehtudsalperc > 0 && prognosalperc > 0){
    	  asi = asi + "Izpilde/Projekcija :  ";
          asi = asi +(int)(tehtudsalperc*100) + " % / " + (int)(prognosalperc*100) + " %| | ";  
        }
   
      
      
 	String asi3 = "";
 	 if(custPlanGruppedFocus != null && custSalesGruppedFocus != null) {
 		 
 		  Map<String, Double> osales = custPlanGruppedFocus.get(custID);
 		  Map<String, Double> rsales = custSalesGruppedFocus.get(custID);
 		 
 		  if(osales != null) {
 		  Iterator<String>  itera2 = osales.keySet().iterator();
 			 while (itera2.hasNext()) {
 				 String grID = itera2.next();
 			double psales = osales.get(grID);
 		 if(rsales != null && rsales.get(grID) != null) {
 		  
 		  double msales = rsales.get(grID);
 		  double  tehtudsalpercC = msales/psales;
       	  double plaanitavMyykSalC = d2 * msales/dd;
       	  double  prognosalpercC = plaanitavMyykSalC/psales;
       	
       	  if(tehtudsalpercC > 10){
       		  tehtudsalpercC = 0;  
       	  }
       	  
       	  if(prognosalpercC > 10){
       		  prognosalpercC = 0;  
       	  }
       	  
       	  String done = (int)(msales - psales)+"";
       	  if(!done.startsWith("-")) {
       		  done = "+" + done;
       	  }

       	   asi3 = asi3  + "Fokus Cat. : " + grID + "|Izpilde/Plan : " + (int) msales +  " € / " + (int) psales +  " €|Starpība : " + done + " € |";
       	   if(msales > 0) {
               asi3 = asi3 + "|Izpilde/Projekcija :   " +(int)(tehtudsalpercC*100) + "% / " + (int)(prognosalpercC*100)  + " %| |";  
       	    } else {
       		   asi3 = asi3 + " | |";
       	    }
 		 } else {
 			   asi3 = asi3  + "Focus Cat. : " + grID + "|Izpilde/Plāns : " + 0 +  " € / " + (int) psales +  " €|Starpība : " + (int) psales + " € |";
 	           asi3 = asi3 + " | |";  
 	 	 }
 		  }
 		}
 	 }
 	 
      
 	 if(custPlanGrupped != null && custSalesGrupped != null) {
 		 
		  Map<String, Double> osales = custPlanGrupped.get(custID);
		  Map<String, Double> rsales = custSalesGrupped.get(custID);
		 
		  if(osales != null) {
		  Iterator<String>  itera2 = osales.keySet().iterator();
			 while (itera2.hasNext()) {
				 String grID = itera2.next();
			double psales = osales.get(grID);
		 if(rsales != null && rsales.get(grID) != null) {
		  
		  double msales = rsales.get(grID);
		  double  tehtudsalpercC = msales/psales;
      	  double plaanitavMyykSalC = d2 * msales/dd;
      	  double  prognosalpercC = plaanitavMyykSalC/psales;
      	
      	  if(tehtudsalpercC > 10){
      		  tehtudsalpercC = 0;  
      	  }
      	  
      	  if(prognosalpercC > 10){
      		  prognosalpercC = 0;  
      	  }
      	  
      	  String done = (int)(msales - psales)+"";
      	  if(!done.startsWith("-")) {
      		  done = "+" + done;
      	  }

      	   asi3 = asi3  + " Cat. : " + grID + "|Izpilde/Plāns : " + (int) msales +  " € / " + (int) psales +  " €|Starpība : " + done + " € |";
      	   if(msales > 0) {
               asi3 = asi3 + "Izpilde/Projekcija :   " +(int)(tehtudsalpercC*100) + "% / " + (int)(prognosalpercC*100)  + " % | |";  
      	   } else {
      		   asi3 = asi3 + "|";
      	   }
		 } else {
			   asi3 = asi3  + " Cat. : " + grID + "|Izpilde/Plāns : " + 0 +  " € / " + (int) psales +  " €|Starpība : -" + (int) psales + " € | ";
	             asi3 = asi3 + "|";  
	 			 
		 }
		  }
		}
	 }  
      
      
      
      
      Map<String,Double> ch = custPlans.get(custID);
   	
       String asi2 = "";
       
       for (String group : ch.keySet()) {
        if(this.groupGroupingFocus.get(group) != null) {
    	 String grName = groupNames.get(group);
         String catName = this.groupGroupingFocus.get(group);
         Double gpSales = ch.get(group);
         Double gSales = 0.0;
         if( custSales.get(custID) != null && custSales.get(custID).get(group) != null) {
        	 gSales = custSales.get(custID).get(group);
         }
         
         if(gpSales != null && gpSales >0) {
        	 plaanitavMyykSal = 0;
        	 tehtudsalperc = 0;
        	 prognosalperc = 0;
        	 
        	 if(gSales != null && gSales > 0) {
               plaanitavMyykSal = d2 * gSales/dd;
               tehtudsalperc = gSales/gpSales;
               prognosalperc = plaanitavMyykSal/gpSales;
        	 }
         if(tehtudsalperc > 10){
     		  tehtudsalperc = 0;  
     	  }
     	  
     	  if(prognosalperc > 10){
     		  prognosalperc = 0;  
     	  }
     	  
     	 String done = (int)(gSales - gpSales)+"";
      	  if(!done.startsWith("-")) {
      		  done = "+" + done;
      	  }else {
      		  done = done.substring(1);
      	  }

      	//Plan- Plāns; Done- Izpilde; Diff- Starpība, esti- Projekcija
      	  asi2 = asi2 + "Fokus GR : " + grName + " (" + catName + ")"  + "|Izpilde/Plāns : " + (int) gSales.doubleValue() + 
      			  " € / " + (int) gpSales.doubleValue() +  " €|Starpība : " + done + " € |";
      	
      	  if(tehtudsalperc > 0) {
               asi2 = asi2 + "Izpilde/Projekcija :  " + (int)(tehtudsalperc*100) + " % / " + (int)(prognosalperc*100)  + " % | |";  
           } else {
        	   asi2 = asi2 + "| |";
           }
         }
    	 }
       
       
       
       
     /*  if(this.groupGrouping.get(group) != null) {
      	 String grName = groupNames.get(group);
           String catName = this.groupGrouping.get(group);
           Double gpSales = ch.get(group);
           Double gSales = 0.0;
           if( custSales.get(custID) != null && custSales.get(custID).get(group) != null) {
          	 gSales = custSales.get(custID).get(group);
           }
           
           if(gpSales != null && gpSales >0) {
          	 plaanitavMyykSal = 0;
          	 tehtudsalperc = 0;
          	 prognosalperc = 0;
          	 
          	 if(gSales != null && gSales > 0) {
                 plaanitavMyykSal = d2 * gSales/dd;
                 tehtudsalperc = gSales/gpSales;
                 prognosalperc = plaanitavMyykSal/gpSales;
          	 }
           if(tehtudsalperc > 10){
       		  tehtudsalperc = 0;  
       	  }
       	  
       	  if(prognosalperc > 10){
       		  prognosalperc = 0;  
       	  }
       	  
       	 String done = (int)(gSales - gpSales)+"";
        	  if(!done.startsWith("-")) {
        		  done = "+" + done;
        	  }else {
        		  done = done.substring(1);
        	  }
        	  
        	  asi2 = asi2 + "GR : " + grName + " (" + catName + ")"  + "|Sales/Plan : " + (int) gSales.doubleValue() + 
        			  " € / " + (int) gpSales.doubleValue() +  " €|Diff. : " + done + " € |";
        	
        	  if(tehtudsalperc > 0) {
                 asi2 = asi2 + "done/esti. :  " + (int)(tehtudsalperc*100) + " % / " + (int)(prognosalperc*100)  + " %| |";  
             } else {
            	 asi2 = asi2 + " | ";
             }
           }
      	 }*/
         }
     
       
       
       
       

	     res.put(custID, asi  +asi3 + asi2);
     }
	  
	  return res ;
  }
  
  
  

  HashMap<String, String> sortags = null;
	
	public TreeMap getCustomers(Agent agent){
	  nf.setMinimumFractionDigits(2);
	
	  sortags = new HashMap<String, String>();
   try{
    	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
    } catch (Exception e){
    	e.printStackTrace();
    }
	//  makeCustprod();
	  
   // TreeMap deldays =  makeCustomerDeliveryDays();
    
    custCategories = getCustCategories();
    
    if(activeXLSPromos == null) {
  		activeXLSPromos = new HashMap<String,List<String>>();
  	    getCampExcelLines(new Vector());
      }
    
    
   TreeMap<String,String> displays  =   Converter.getDisplayInfoFromExcel(this.getExcelDir(), custCategories);
   
    loadPlans();
    
    Map budgets = makeCustBudgetMap();
    
     inTT = new HashSet<String>();
	
	HashMap<String, String> promos = new HashMap<String, String>();
	
    HashMap<String, Vector<Agent>> cliags = new HashMap<String, Vector<Agent>>();
  
    
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap indeksT = new TreeMap();
    
    try {
    	
    	   String aaa = "SELECT AgentBackofficeID as kood , ClientBackofficeID as klient, route  from  dbo.EPASAULE_AGENTS_CLIENTS " ;
      		
    	   Connection con = this.getNewConnection();

       	StringBuffer sb0 = new StringBuffer();
    		 Statement stmt = con.createStatement();
    	     ResultSet rs1 = stmt.executeQuery(aaa);
    	               while(rs1.next()){
    	                 Agent ag = new Agent(rs1.getString("kood").trim());
    	                 if(agsActive.contains(ag.getID())) {
    	                 String cli = rs1.getString("klient").trim();
    	                 String route = rs1.getString("route");
    	                 if(route != null && route.trim().length() > 0) {
    	                	 
                          
    	                	 sb0.append(cli + ";" + ag.getID() + ";" + route.trim() + "\n");
                            
    	                	 List<CycleMatch> ll =  nextSevenDaysMatches(route);
    	                	//   System.out.println("ag.getVisit() " + ag.getID() +" "+ cli + " " + ag.getVisit() + " " + route + " " + ll);
       	                	
    	                	 
    	                	 if(ll.size() > 0){
    	                		 String dd = "";
    	                	   for(CycleMatch cc : ll) {
    	                		 if( cc.dayOfWeek == DayOfWeek.MONDAY ) {
    	                			 dd = dd + "E";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.TUESDAY ) {
    	                			 dd = dd + "T";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.WEDNESDAY ) {
    	                			 dd = dd + "K";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.THURSDAY ) {
    	                			 dd = dd + "N";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.FRIDAY ) {
    	                			 dd = dd + "R";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.SATURDAY ) {
    	                			 dd = dd + "L";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.SUNDAY ) {
    	                			 dd = dd + "P";
    	                		 }
    	                	 }
    	                	
    	                	 if(dd.length() > 0) {
    	                	   ag.setVisit(dd); 
    	                	 }

    	    	          	 }
    	                		
    	                	 
    	                	ag.nextContact = nextMatchingDate(route);
    	                  }
    	                 
    	                 Vector<Agent> vv = cliags.get(cli);
    	                 if(vv == null) {
    	                	 vv = new Vector<Agent>();
    	                 }
    	                 vv.add(ag);
    	               
    	                 cliags.put(cli, vv);
    	               }
    }
    	              rs1.close();
  
    	               if(sb0.length() > 0) {
     		        	  Converter.saveFileInUTF8( "custagentvisit.txt", sb0);
     		           }
    	              
    	              aaa = "SELECT [ACCOUNTNUM] as klient ,[ZIPCODE] as indeks FROM [DWHEE].[dbo].[BID02_CUSTORDER]";
    	              
    	
    	            	rs1 = stmt.executeQuery(aaa);
	       	               while(rs1.next()){
	       	                 String cli = rs1.getString("klient").trim();
	       	                 String indeks = rs1.getString("indeks");
	       	                 if(indeks != null && indeks.length() > 0) {
	       	                	indeksT.put(cli, indeks);
	       	                 }
	       	                  
	       	                  }
	       	              rs1.close();
	       	              
	    	  	            
    	              
    	              aaa = " select a.clientBackOfficeId as klient,  a.promotionBackOfficeId as promo, c.nextdelivery , max([date from]), min([date to])\r\n" + 
    	              		"  from dbo.EPASAULE_PROMOTIONOBJECTS a inner join dbo.EPASAULE_PROMO b on a.promotionBackOfficeId = b.promotionBackOfficeId\r\n" + 
    	              		"inner join EPASAULE_CLIENTS c ON a.clientBackOfficeId = c.BackOfficeId\r\n" + 
    	              		"    	              			where c.nextdelivery between [date from] and [date to]  \r\n" + 
    	              		"    	              			group by  a.promotionBackOfficeId , clientBackOfficeId ,c.nextdelivery\r\n" + 
    	              		"    	              			 order by 1,2 " ;
    	        		
    	              	rs1 = stmt.executeQuery(aaa);
    	       	               while(rs1.next()){
    	       	                 String cli = rs1.getString("klient").trim();
    	       	                 String route = rs1.getString("promo").trim();
    	       	                 String aa = promos.get(cli);
    	       	                 if(aa == null) {
    	       	                	 promos.put(cli, route);
    	       	                  } else {
    	       	                	promos.put(cli, aa + "|" + route);
    	       	                 }
    	       	                  
    	       	                  }
    	       	              rs1.close();
    	       	              stmt.close();
    	       		
    	       	
    	
    	
      	
    	TreeMap<String,Double> debts = new TreeMap<String ,Double>();
    	
    	 String cmd2 = "  SELECT sum(Amount) as tasumata , debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS where \r\n" + 
    	 		" payDate < getdate()-1 group by debetorBackOfficeId"; 
    	 		
    	 
    	  stmt = con.createStatement();
            rs1 = stmt.executeQuery(cmd2);
                     while(rs1.next()){
                       double volg = rs1.getDouble("tasumata");
                       String pay = rs1.getString("maksja").trim();
                       debts.put(pay, volg);
                     }
                    rs1.close();
                    stmt.close();
    	
                    TreeMap<String,Double> debtsall = new TreeMap<String ,Double>();
                	
               	 String cmdall = "  SELECT sum(Amount) as tasumata , debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS group by debetorBackOfficeId"; 
               	 
               	    stmt = con.createStatement();
                       rs1 = stmt.executeQuery(cmdall);
                                while(rs1.next()){
                                  double volg = rs1.getDouble("tasumata");
                                  String pay = rs1.getString("maksja").trim();
                                  debtsall.put(pay, volg);
                                }
                               rs1.close();
                               stmt.close();
               	
                               
              	  
                    String cmd = "Select a.name ,BackOfficeId as accountnum, debitorBackOfficeId as payer,CreditLimit as limit , (case when EG_IGNOREMINORDER = 0 then minOrderSum else 0 end) as minsum, '' as linedisc\r\n" + 
                    		"                                                        , pricelistBackOfficeId as pricegroup,a.address ,\r\n" + 
                    		"                                   Status AS block , channel as chain , clientType as typeo,b.description as type, phone , contactPerson as pername, email as peremail,\r\n" + 
                    		"                                    contactPhone as perphone,  accountantemail as acco,  EG_EMAILDEBTS as bill ,consignationPeriod as term,a.city as city  , alcoLicense as alic, tobaccoLicense as tlic,\r\n" + 
                    		"                                    contractDate as cdate , segmentname as seg , assortimentBackOfficeId as fixed ,subsegmentname as segsub, [EG_EXTERNALCODE] as type2,"
                    		+ " gkrnextdelivery as via3l, nextdelivery as del1, nextdelivery2 as del2 , EG_AGENTREPRICINGALLOWED as reprice, DeliveryComment as del3 \r\n" + 
                    		"                                    from  dbo.EPASAULE_CLIENTS a  left join BID29_LINEOFBUSINESS b on a.clientType=b.LINEOFBUSINESSID\r\n" + 
                    		"                                LEFT JOIN [BID02_CUSTORDER] c on a.BackOfficeId=c.ACCOUNTNUM\r\n" + 
                    		"" ; 
                    

                     if (isDebug()) System.out.println(cmd);
                    
                    
                    
    	 stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
 
      while(rs.next()){
        String name = rs.getString("name").trim();
        
        boolean goes = true;
        
        String accountnum = rs.getString("accountnum").trim();
       
        Customer cust = new Customer(accountnum);
        cust.setPayer(rs.getString("payer"));
      
        Double limit = rs.getDouble("limit");
        if(limit != null ) {
        	if(limit > 0) {
            	cust.setCredit(limit+"");	
        	}else {
        		cust.setSalesBlockPrepay(true);
        		//System.out.println("Ettemakse klient " + cust.getID());
        	}
        }
        
        Double minsum = rs.getDouble("minsum");
        if(minsum != null) {
        	cust.setMinsum(minsum);
        }
        
        boolean de = true;
        	
        if(de) {
        	if(debts.get(cust.getPayer()) != null) {
        		 cust.setDebt(debts.get(cust.getPayer()));
        	}else if(debts.get(cust.getID()) != null) {
        		 cust.setDebt(debts.get(cust.getID()));
        	}
        	
        if(debtsall.get(cust.getPayer()) != null) {
       		 cust.setUsedCredit(debtsall.get(cust.getPayer()));
         	} else if(debtsall.get(cust.getID()) != null) {
       		cust.setUsedCredit(debtsall.get(cust.getID()));
       	    }
        }
        
       
        
        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String pricegroup = rs.getString("pricegroup");
        if(pricegroup != null && pricegroup.trim().length()>0){
        	
       if(!pricegroup.trim().equals("BASE")) {
    	   
         if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
        	 cust.setGroup(cust.getGroup() + "|" +pricegroup.trim());
         } else {
        	 cust.setGroup(pricegroup.trim());
         }
       }
         
        } 
        
        if(promos.get(cust.getID()) != null) {
        
        	 if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
            	 cust.setGroup(cust.getGroup() + "|" + promos.get(cust.getID()));
             } else {
            	 cust.setGroup(promos.get(cust.getID()));
             }
        }
        
        String aadr = rs.getString("address");
        if (aadr != null) {
        	if(aadr.endsWith(", %1")) {
        	 aadr = aadr.substring(0,aadr.indexOf(", %1")).trim();
        	}
        	aadr = aadr.replaceAll("\\s+", " ").trim();
        	
        	if(aadr.contains("LV-")) {
        		if(aadr.indexOf(",LV-") != -1) {
        			aadr = aadr.substring(0,aadr.indexOf(",LV-"));
        		}
        		if(aadr.indexOf(", LV-") != -1) {
        			aadr = aadr.substring(0,aadr.indexOf(", LV-"));
        		}
        	}
        	// viskame aadressist välja lõpust Latvija ja indeksi
        	
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ').replaceAll(country, " ").replaceAll("\\s+", " ").trim()), " "));
       
          if(aadr.contains(",")) {
          		cust.restStreet = aadr.trim().substring(0,aadr.trim().indexOf(","));
          		cust.restCity = aadr.trim().substring(aadr.trim().indexOf(",")+1);
          	}else {
          		cust.restStreet = aadr.trim();	
          	}
           	cust.setCountry(country);
          }
        
        //if(deldays.get(cust.getID())  != null){
        	//cust.setDelDays((String)deldays.get(cust.getID()));
        //}    
        
        
        cust.restName = name;
       // name = Converter.enCodeEst(name);
       // name = name.replaceAll("\\([^)]*\\)", "").replaceAll("\\s+", " ").trim();
        name = name.replaceAll("\\s*\\([^()]*\\)\\s*$", "").trim();
        name = name + " " + cust.getAddress() + " " + accountnum ;
        cust.setName(Converter.enCodeEst(name));
        
        if(indeksT.get(cust.getID()) != null){
        	cust.addCity("Pasta indekss: " + indeksT.get(cust.getID()));
        }
        
        
        
        if(cliags.get(cust.getID()) != null) {
            Vector<Agent> vv = cliags.get(cust.getID());
            
            HashMap agvisit = new HashMap();
            
            //Paneme agendid külge kõik ja nopime välja kellel on Routing ka
            for(Agent agentL : vv) {

                  cust.addAgent(agentL);
                  
   	          if(agentL.nextContact != null) {
   	              cust.addCity("N11124xxkamais: " + agentL.getID() + " '" + agentL.nextContact.format(FMT).replace('.', '-').replace('.', '-') + "'");  
   	          }
   		          
   	          if(agentL.getVisit() != null && agentL.getVisit().length() > 0) {
   	        	  cust.setMainAgent(agentL.getID());
   	        	  agvisit.put(agentL.getID(), agentL.getVisit());
   	          }
           	 
            }	 
            
           // nyyd kammime läbi kõik, kes on kliendil küljes ja lisama asendajatele ka samad routingud
           	 // kui on asendamine sii speame külge panema visit day ka 
          
          
          
          List<Agent> agentsToAdd = new ArrayList<>();

      	//Vaatame ainult pea agendi asendajaid
          
           if(cust.getMainAgent() != null && cust.getMainAgent().trim().length() > 0) {
              String idd = cust.getMainAgent();
              Agent aa1 = (Agent) agent1.get(idd);

              if (aa1 != null) {
                  Vector vec = aa1.getDubbler();

                  if (vec != null && !vec.isEmpty()) {
                      Enumeration en = vec.elements();

                      while (en.hasMoreElements()) {
                          String age = (String) en.nextElement();
                          Agent ee = new Agent(age);
                             
                              ee.setVisit((String)agvisit.get(idd));

                          agentsToAdd.add(ee);
                      }
                  }
              }
          }

          for (Agent age : agentsToAdd) {
              cust.addAgent(age);
          }
            
            
        	}
         

        
       
        if(de) {
        String block = rs.getString("block");
        if (block.equals("B")) {
          cust.setSalesBlockT(true);
        }
        }
        
        
        
        String chain = rs.getString("chain");
        if(chain != null && chain.trim().length() > 0) {
        	//cust.setChain(chain);
        	if(activeXLSPromos.get(chain.trim()+"xls") != null) {
                for (String cc :  activeXLSPromos.get(chain.trim()+"xls")) {
             	   if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
                   	 cust.setGroup(cust.getGroup() + "|" + cc);
                    } else {
                   	 cust.setGroup(cc);
                    }
                }
             }
        }
        
        String type = rs.getString("type");
        if(type != null && type.trim().length() > 0) {
        	//cust.setCustType(type);
        }
        
        
        String info = rs.getString("phone");
        String pername = rs.getString("pername");
        String peremail = rs.getString("peremail");
        String perphone = rs.getString("perphone");
        
        if (pername != null && pername.trim().length()>0 && !cust.restName.contains(pername)) {
            cust.restPerson = pername.trim(); 
            cust.adPersonalData(Converter.enCodeEst( pername.trim()) + "|");
        }
        

        if (perphone != null && perphone.trim().length()>0) {
            cust.restPerPhone = perphone.trim(); 
            cust.adPersonalData("Tel:" + perphone.trim() + "|");
            }
        
        
        
        if (peremail != null && peremail.trim().length()>0) {
            cust.restPerEmail = peremail.trim();
            if(!cust.getPersonalData().contains(peremail.trim())) {
               cust.adPersonalData( " " +peremail.trim().toLowerCase() + "|");
            }
                 
        }
        

        if (info != null && info.trim().length()>0) {
        	 if(!cust.getPersonalData().contains(info.trim())) {
                cust.adPersonalData("Tel:" + info.replaceAll("/", " / ").trim() + "|");
          }}
        
          
        
        String raam = rs.getString("acco");
        
        if (raam != null && raam.trim().length()>0) {
           if(raam.contains(";")) {
        	   raam = raam.substring(0, raam.indexOf(";"));
           }
        
           if(raam.indexOf("@") != -1) {
        	   if(!raam.toLowerCase().contains("arvekontroll")) {
        	        cust.adPersonalData("GV: " + raam.trim().toLowerCase() + "|");
        	   }
           }
        }
        
        
      String arve = rs.getString("bill");
        
        if (arve != null && arve.trim().length()>0) {
           if(arve.contains(";")) {
        	   arve = arve.substring(0, arve.indexOf(";"));
           }
           
           if(arve.indexOf("@") != -1) {
        	   if(!cust.getPersonalData().contains(arve.trim().toLowerCase())) {
        	        cust.adPersonalData(Converter.enCodeEst("Parādi: " + arve.trim().toLowerCase()));
        	   }
           }
        }
        
        
        if(cust.getPersonalData().endsWith("|")) {
        	cust.setPersonalData(cust.getPersonalData().substring(0,cust.getPersonalData().length()-1));
        }
        
        Integer term = rs.getInt("term");
        if(term != null && term > 0) {
        	cust.setPaymTerm(term + "");
        }

        String alic = rs.getString("alic");
        if(alic != null && alic.trim().length() > 0) {
        	//cust.setPaymTerm(term + "");
        }
        
        String tlic = rs.getString("tlic");
        if(tlic != null && tlic.trim().length() > 0) {
        	//cust.setPaymTerm(term + "");
        }
        
        
        
        String seg = rs.getString("seg");
        if(seg != null && seg.trim().length() > 0) {
        	cust.addCity("Segments: " + Converter.enCodeEst(seg.trim()));
        	cust.setChain(seg.trim());
        	
        }
        
        
        String fixed = rs.getString("fixed");
        if(fixed != null && fixed.trim().length() > 0) {
        	String sugg = fixed.trim();
        	if(!cust.getID().startsWith("MRC")) {
        		sugg = sugg + "|" + "SUGGED";
        	}
        	cust.suggAssortCode = sugg ;
          //  cust.addFixSortCode(Converter.cleanLitLatEst(fixed.trim())+"x");
       
        	String ags = sortags.get(fixed.trim()); 
            if(ags == null){
            	ags = "";
            }
          

            String   agsS = sortags.get("SUGGED"); 
              if(agsS == null){
              	agsS = "";
              }
              
            
            Iterator itera =   cust.getAgents().keySet().iterator();
          while(itera.hasNext()) { 
          String aa = (String)itera.next();
            if(!ags.contains(aa)) {
            	ags = ags + "," + aa;
            }
            if(!agsS.contains(aa)) {
              	agsS = agsS + "," + aa;
              }
          }
          

          if(agsS.length() > 0) {
           sortags.put("SUGGED", agsS+",");
          }
          
          if(ags.length() > 0) {
            sortags.put(fixed.trim(), ags+",");
           }
        
          
        }
        
        
        String segsub = rs.getString("segsub");
        if(segsub != null && segsub.trim().length() > 0) {
        	  cust.addCity("Subsegments: " + Converter.enCodeEst(segsub.trim()));
        	  cust.category = segsub.trim();
              // 	cust.addCity("Segment: " + segsub.trim());
               	if(activeXLSPromos.get(segsub.trim()+"xls") != null) {
                       for (String cc :  activeXLSPromos.get(segsub.trim()+"xls")) {
                    	   if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
                          	 cust.setGroup(cust.getGroup() + "|" + cc);
                           } else {
                          	 cust.setGroup(cc);
                           }
                       }
               	}
        }
        
        String type2 = rs.getString("type2");
        if(type2 != null && type2.trim().length() > 0) {
        	//cust.addCity("SubSegment: " + Converter.enCodeEst(type2.trim()));
        }

        
        if(type != null && type.trim().length() > 0) {
        	//cust.addCity("Tips: " + Converter.enCodeEst(type.trim()));
        }
        
        String del1 = rs.getString("del1");
        
        if(del1 != null && del1.trim().length() > 0) {
        	cust.addCity(Converter.enCodeEst("EUG Piegāde: '" + del1.trim() + "'"));
        }
        
        
        Integer reprice = rs.getInt("reprice"); 
        cust.repriceallowed = false;
        if(reprice == 1) {
        	  cust.repriceallowed = true;
        	 // System.out.println(cust.getID() + " allowed " + cust.getAgents());
        }
        
String del3 = rs.getString("del3");
        
        if(del3 != null && del3.trim().length() > 0) {
        	cust.addCity(Converter.enCodeEst("Info piegādei: '" + del3.trim() + "'"));
        }
        
        
        cust.setDealer(new Dealer("24N"));
        
        if(displays != null && displays.get(cust.getID()) != null) {
        	cust.setInfo(displays.get(cust.getID()));
        }
        
        if(budgets.get(accountnum) != null){
        	cust.addToCustDataVec(accountnum+ ";custbudget; ;" + budgets.get(accountnum));
        }
        
        if(goes) {
            customers.put(name, cust);
            custMap.put(cust.getID(),cust);	
        }
  
      }



  rs.close();
  

	 String sql = " SELECT clientBackOfficeId as klient, documentNo as number,payDate as maksepaev,\r\n" + 
	 		" Amount as tasumata ,Date as kuupaev , datediff(day,payDate,getdate())as yle ,debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS "; 
  
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
    debt.setPayerID(rs.getString("maksja").trim());
    if(debt.getSum() > 0.09 || debt.getSum() < -0.09) {
     this.debt.addElement(debt);
    }
  }
  
  
  
  
  String fff = "select BackOfficeId as klient,contactPerson as nimi ,  \r\n" + 
  		"							 '' as synna , email  as email,contactPhone  as phone from  dbo.EPASAULE_CLIENTS where contactPerson != ''";
 

      StringBuffer sb2 = new StringBuffer();
   
	   rs1 = stmt.executeQuery(fff);
      while(rs1.next()){
   	   String klient = rs1.getString("klient").trim();
   	   String nimi = rs1.getString("nimi").trim();
   	   java.sql.Date synna = null ;//rs1.getDate("synna");
   	   if(synna == null){
   		   synna = new java.sql.Date(0);
   	   }
   	  String email = parent.make(rs1.getString("email"));
   	  String phone = parent.make(rs1.getString("phone"));
 //	  String cell = parent.make(rs1.getString("cellularphone"));
   	  if(klient.length() > 0) {
            sb2.append(klient+";"+ nimi + ";" + synna.getTime() + "; ;" + phone  + ";" + email +  "\n");
   	      }
        }
       rs1.close();  

     if(sb2.length() > 2){
        		// Converter.saveFileInUTF8( "custcontacts.txt", sb2);
        	}

              
				 
				  fff = " select a.promotionBackOfficeId as custgr ,a.productBackOfficeId as prodid, a.minqty, a.minPrice , max (a.[date to]) as paev , max(b.description) as nimi , max(case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end) as ladu from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN \r\n" + 
				  		"				  		EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId where minqty > 0  group by a.promotionBackOfficeId,a.productBackOfficeId, minqty, minPrice\r\n" + 
				  		"				  						  				order by a.promotionBackOfficeId,productBackOfficeId, minqty, minPrice";
				 

				  StringBuffer   buf = new StringBuffer();
				   
					   rs1 = stmt.executeQuery(fff);
				      while(rs1.next()){
				   	  
				   	  String gruppx = parent.make(rs1.getString("custgr"));
				   	  String toodex = parent.make(rs1.getString("prodid"));
				   	  Double kogus = rs1.getDouble("minqty");
				   	  Double hind = rs1.getDouble("minPrice");
				      java.sql.Date end = rs1.getDate("paev");
				   	 String nimi = parent.make(rs1.getString("nimi"));
				   	 String ladu = rs1.getString("ladu");
				   	if(ladu.equals("24N")) {
				   		 ladu = "";
				   	 }
						
				   	 if(nimi.contains("2030")) {
				   		 nimi = nimi.substring(nimi.indexOf("2030")+ 11);
				   		 if(nimi.contains("2030")) {
					   		 nimi = nimi.substring(0,nimi.indexOf("2030") - 18);
					   	 }
				   	 }
                   
				   	  
				   	long ttt = 0;
                    if(end.after(new Date())) {
                   	 ttt = end.getTime();
                    }
				   	  



                	      buf.append(toodex+ ";" + gruppx + ";" + Converter.enCodeEst(nimi) + ";" + NF.format(hind).replace(',', '.') + ";" 
                	      + gruppx + ";" + (int)(kogus+0.001) + "; ;" + ttt + "; ;" + ladu);
                        buf.append("\n");	
				   	  
				      }
				     rs1.close(); 
				     stmt.close();
				     con.close();
   

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
				        		ex.printStackTrace();
				        	  }
				         }

				        
				        
								        
							     
              
    } catch (Exception e){
      getParent().showError(e);
      System.exit(0);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
	return DriverManager.getConnection(getParent().getProps().getProperty("dsn.url"), getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }
private HashSet<String> inTT = null;

  public TreeMap getProdGroups() throws SQLException {
      this.setTreeGroup(true);
	   Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = " SELECT backOfficeId as liik,Name as nimetus , posWeight as sort  FROM dbo.EPASAULE_GROUPS\r\n" + 
	    		" where parentbackofficeid ='' ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String nimi = rs.getString("nimetus").trim();
	      Integer sort = rs.getInt("sort");
	      sort = -100000 + sort;
	      String sortt = -sort + "";
	      while(sortt.length()<8) {
	    	  sortt = "0" + sortt;
	      }
		  
	        tm.put(liik,sortt  + "_" + Converter.enCodeEst(nimi));
	    }
	    rs.close();
	    st.close();
	    con.close();
	    
  return tm;	   
}

 private HashMap<String,String> grps = null; 

  public TreeMap getProdGroupsSecond() throws SQLException {
	   Connection con = getNewConnection();
	   
	   grps = new HashMap<String,String>(); 
	    TreeMap   tm = new TreeMap();
	    String    sql = " SELECT backOfficeId as liik, parentbackofficeid as liikparent ,Name as nimetus , posWeight as sort  FROM dbo.EPASAULE_GROUPS\r\n" + 
	    		" where parentbackofficeid !='' ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String liikparent = rs.getString("liikparent").trim();
	      String nimi = rs.getString("nimetus").trim();
	      
	      grps.put(liik, liikparent);
	      Integer sort = rs.getInt("sort");
	      sort = -100000 + sort;
	      
	      String sortt = -sort + "";
	      
	      while(sortt.length()<8) {
	    	  sortt = "0" + sortt;
	      }
		  
	        tm.put(liikparent + "" +liik,sortt  + "_" + Converter.enCodeEst(nimi));
	    }
	    rs.close();
	    st.close();
	    con.close();
 return tm;	
  }

  private Vector prodcodes = null;

  private HashMap<String,Double> prodprices = null;
  private HashMap<String,String> eancodes = null;

  
  public HashSet getUsedProds() {
	   HashSet   tm = new HashSet();
		 
	  try {
	   Connection con = getNewConnection();
	   
	    String    sql = " select kood from(\r\n" + 
	    		"select  productBackOfficeId as kood from dbo.EPASAULE_PROMOTIONOBJECTS a  \r\n" + 
	    		"group by  productBackOfficeId  \r\n" + 
	    		"union all\r\n" + 
	    		"select productBackOfficeId as kood from EPASAULE_ASSORTMENTS\r\n" + 
	    		"group by productBackOfficeId  union all select productBackOfficeId as kood  from EPASAULE_PRODUCTS_ADVERTISING group by productBackOfficeId\r\n" + 
	    		") as asi group by kood ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      tm.add(rs.getString("kood").trim());
	    }
	    
	    rs.close();
	    st.close();
	    con.close();
	    
	  }catch(Exception e) {e.printStackTrace();}  
    return tm;	
 }
  
  
  
  public TreeMap getProducts(Agent agent){
	  prodcodes = new Vector();

	  usedProds = getUsedProds();
	  
	  if(grps == null) {
		 try {
			this.getProdGroupsSecond();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	  }
	  
	  String noOPT = getParent().getProps().getProperty("nostepgroup");
	  
    String cmd = "  Select posWeight as sorting , a.backOfficeId as itemid,a.Name AS nimetus,c.Name as namebr, a.quantityBox AS kastis,a.quantityPallete as alusel , b.CONVERTLAYER as layer , a.quantityPack as pakk,  \r\n" + 
    		"    		a.Price AS price, a.barCode AS ean,a.groupBackOfficeId AS grupp,  \r\n" + 
    		"    		a.brandBackOfficeId AS grupp2 ,a.increasestep as opt, a.minimalOrderQuantity as mini ,a.unitName AS unit, a.vatPercent as vat ,a.MARKUPGROUPID as deposit, "
    		+ " a.MARKUPCOEF as depositmulti from  dbo.EPASAULE_PRODUCTS a LEFT join dbo.BID01_ITEM b on b.itemid= a.backOfficeId\r\n" + 
    		"			 left join EPASAULE_BRANDS c on c.backOfficeId = a.brandBackofficeId "  ;

             String cmd2 = " Select productBackofficeId as toode,sum(Inventory) as stock, (case when regionBackofficeId = '24EWC' then '24N' else regionBackofficeId end) as ladu, bestBefore as partii from  dbo.EPASAULE_INVENTORIES_detail  " + 
             		"  group by productBackofficeId,regionBackofficeId,bestBefore order by productBackofficeId,  (case when regionBackofficeId = '24EWC' then '24N' else regionBackofficeId end) desc,bestBefore";
        

             
    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap<String, Double> stocks = new TreeMap<String, Double>();
    TreeMap<String, String> partii = new TreeMap<String, String>();
    prodprices = new HashMap<String,Double>();
    eancodes = new HashMap<String,String>();
     
    try {
        Connection con =   this.getNewConnection();
        Statement stmt = con.createStatement();
    	ResultSet rs1 = stmt.executeQuery(cmd2);
        
    	while(rs1.next()){
          String id = rs1.getString("toode").trim();   
          double sto = rs1.getDouble("stock");
          String ladu = rs1.getString("ladu").trim();
          String part = rs1.getString("partii");
          String gg = "";
          if(part != null && !part.startsWith("1900")) {
              gg = " - BB:" + part.substring(0,10);
          }
          gg = (int)sto + " - " +ladu  + gg;
          
    	  if(partii.get(id) == null) {
              partii.put(id, gg); 
    	  }  
          
          if(stocks.get(id) != null) {
        	  sto = stocks.get(id) + sto; //summeerime laoseisu 
          }
          if(sto > 0) {
        	  if(!usedProds.contains(id)) {
          	    usedProds.add(id);
          	  }
          }
         // stocks.put(id, sto);
          }
       
    	rs1.close();
    	    	
       ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      
      while(rs.next()){
        String sorting = rs.getString("sorting");
        while(sorting.length() < 7) {
        	sorting = "0" + sorting;
        }
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst((rs.getString("nimetus").trim())) ;//+ " " + kood;
        
       // teeme nime puhastuse kui on topelt brand mini
        boolean cut = false;
        try {

        String nimibr = Converter.enCodeEst((rs.getString("namebr")));
        if(nimetus.endsWith(nimibr)) {
    	   String uu = nimetus.substring(0,nimetus.lastIndexOf(nimibr)-2);
           String nn = nimibr.toLowerCase();
    	   if(uu.toLowerCase().contains(nn)) {
    		   cut = true;
        	   nimetus = nimetus.substring(0,nimetus.lastIndexOf(nimibr)-2);
    	   }
       }

        }catch(Exception e) {}
       
       nimetus = nimetus.replaceAll("- BARILLA MEAL", " ").trim();
       nimetus = nimetus.replaceAll("- BIC (MRC)", "- (MRC)").trim();
       nimetus = nimetus.replaceAll("- HBC FA", "- HBC").trim();
       nimetus = nimetus.replaceAll("- PURINA GROCERY", " ").trim();
       nimetus = nimetus.replaceAll("- PURINA (MRC)", "- (MRC)").trim();
       nimetus = nimetus.replaceAll("- NESTLE (MRC)", "- (MRC)").trim();

       nimetus = nimetus.replaceAll("- HBC GLISS KUR", " ").trim();

       nimetus = nimetus.replaceAll("- PHILIPS (MRC)", "- (MRC)").trim();

       nimetus = nimetus.replaceAll("- KIMBERLY CLARK", " ").trim();
       nimetus = nimetus.replaceAll("- COFFEE SUPPLEMENTS", " ").trim();
       nimetus = nimetus.replaceAll("- NESTLE CONFECTIONERY", " ").trim();
       nimetus = nimetus.replaceAll("- EH AIR WICK", " ").trim();
       nimetus = nimetus.replaceAll("- HHC BREF", " ").trim();
       nimetus = nimetus.replaceAll("- RCore DUREX", " ").trim();

       nimetus = nimetus.replaceAll("- HHC PERWOLL", " ").trim();

       nimetus = nimetus.replaceAll("- HBC SCHAUMA", " ").trim();
       nimetus = nimetus.replaceAll("- SCHAR", " ").trim();
       nimetus = nimetus.replaceAll("- HBC SCHWARZKOPF", " ").trim();       
       

       nimetus = nimetus.replaceAll("- NESTLE COFFEE CAPSULES", " ").trim();

       nimetus = nimetus.replaceAll("- HALEON", " ").trim(); 
       nimetus = nimetus.replaceAll("- HHC SILAN", " ").trim(); 
       nimetus = nimetus.replaceAll("- HHC SOMAT", " ").trim(); 
       nimetus = nimetus.replaceAll("- HBC PALETTE", " ").trim();

       nimetus = nimetus.replaceAll("- HBC PERSIL", " ").trim();
       nimetus = nimetus.replaceAll("- HBC PUR", " ").trim();
       nimetus = nimetus.replaceAll("- HBC SYOSS", " ").trim();
       nimetus = nimetus.replaceAll("- HBC PALETTE", " ").trim();
       nimetus = nimetus.replaceAll("- NESTLE FOOD", " ").trim();
       nimetus = nimetus.replaceAll("- SERVICES FOR SALES", " ").trim();
       
       
       
       
       
        prod.setInCases(rs.getDouble("kastis"));
        Double pall = rs.getDouble("alusel");
        if(pall > 0) {
        prod.setOnPallet(pall);
        }
        Double layer = rs.getDouble("layer");
        if(layer > 0) {
        	prod.layer = layer;
        }
        Double pack = rs.getDouble("pakk");
        if(layer > 0) {
        	prod.pack = pack;
        }
        
        
        	prod.setMaxDiscount(-1);
        
        
        prod.setPrice(rs.getDouble("price"));
        prodprices.put(prod.getID(), prod.getPrice());

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
        	 prod.setEAN(ean.trim());
             eancodes.put( ean.trim(), prod.getID());
        }
boolean noopt = false;
      String grupp = rs.getString("grupp");

      String grupp2 = rs.getString("grupp2");
      
       if(grupp!=null && grupp.trim().length()>0){
    	   
         prod.setAgentGroup(grupp.trim());
         
         if(grps.get(grupp.trim()) != null) {
        	 String parent = grps.get(grupp.trim());
        	 prod.setGroup(parent);
        	 prod.setGroupSecond(parent + grupp.trim());
         }
         
         if(grupp.trim().equals(noOPT)) {
        	 noopt = true;
         }
       }

       if(grupp2!=null && grupp2.trim().length()>0){
    	   prod.setType2(grupp2.trim());
       }
       
        prod.setName(nimetus.trim() + " " + kood);
      
          if(prod.getID().startsWith("MRC")) {
        	  prod.setStock(-1);
          }
          
          if(!noopt) {
                 prod.setOptAmountInUnits(rs.getDouble("opt"));
                 prod.setMinAmountInUnits(rs.getDouble("mini"));
          }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                   prod.setUnit(unit);
               }
         
               String depo = rs.getString("deposit");
               if(depo != null && depo.trim().length()  > 0 ){
                   prod.DEPO_WO_VAT = 0.1;

            	   double dep = rs.getDouble("depositmulti");
            	   if(dep > 0 ) {
            		   prod.DEPO_WO_VAT = 0.1 * dep;
            	   }
               }
               
       if(startsWithDigitDash(nimetus)) {
    	   prod.setStatusTop("1");
       }
       
       if(nimetus.startsWith("L") && nimetus.contains("P - ")) {
    	   prod.setStatusSellout("1");
       }
       
               if(partii.get(prod.getID()) != null) {
            	  prod.setPartii(partii.get(prod.getID())); 
            	  if(!usedProds.contains(prod.getID())) {
            	    usedProds.add(prod.getID());
            	  }
               }
               
               
               Double vat = rs.getDouble("vat");
               prod.setVat(vat + "");
               prod.setNameSorting(sorting + nimetus);
           if(usedProds.contains(prod.getID())) {    
              products.put(nimetus + prod.getID(), prod);
              prodHash.put(prod.getID(), prod);
              prodcodes.add(prod.getID());
           }
       
//System.out.println("toode " + nimetus + " " + prod.getID());
    
      }
      rs.close();
      
  	StringBuffer sb = new StringBuffer();
    rs = stmt.executeQuery("select BACKOFFICEID as id, NAME as nimi from dbo.EPASAULE_RETURN_REASONS");
   while(rs.next()){
   	String id = rs.getString("id");
   	String name = rs.getString("nimi");
   	if(name != null && name.trim().length() > 0){
   	      sb.append( id+ ";" +Converter.enCodeEst(name.trim()) + "\n");	
   	}
   }
   rs.close();

	StringBuffer sb3 = new StringBuffer();
    rs = stmt.executeQuery("select BACKOFFICEID as id, NAME as nimi from  dbo.EPASAULE_REP_REASONS");
   while(rs.next()){
   	String id = rs.getString("id");
   	String name = rs.getString("nimi");
   	if(name != null && name.trim().length() > 0){
   	      sb3.append( id+ ";" +Converter.enCodeEst(name.trim()) + "\n");	
   	}
   }
   rs.close();
   
	StringBuffer sb2 = new StringBuffer();
    rs = stmt.executeQuery("select inventlocationid  as id , name from EPASAULE_REGIONS");
   while(rs.next()){
   	String id = rs.getString("id");
   	String name = rs.getString("name");
   	if(name != null && name.trim().length() > 0){
   	      sb2.append( id+ ";" +Converter.enCodeEst(name.trim()) + " " + id + "\n");	
   	}
   }
   
   StringBuffer sb30 = new StringBuffer();
   rs = stmt.executeQuery("select backofficeid as id , MaxRepricingPercent as perc from epasaule_brands where MaxRepricingPercent > 0");
  while(rs.next()){
  	String id = rs.getString("id");
  	Double name = rs.getDouble("perc");
  	if(name != null && name > 0){
  	      sb30.append( id+ ";" + name  + "\n");	
  	}
  }
   
   
   
  rs.close();
  
  StringBuffer sb0 = new StringBuffer();
	
  //sIIN TYLEB LÄBI HINNA MaxDisc arvutada ja edastada
	String minP = "select a.promotionBackOfficeId as grupp ,a.productBackOfficeId as toode,  (case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end) as ladu,ROUND(100 - [minPrice] * 100.0 / [maxPrice] , 2) AS disc   \r\n" + 
			"    						from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId where minqty = 0 and minPrice != maxPrice and minPrice != 0 \r\n" + 
			"    			    											group by a.promotionBackOfficeId,a.productBackOfficeId,(case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end), ROUND(100 - [minPrice] * 100.0 / [maxPrice] , 2) \r\n" + 
			"    			    											union all\r\n" + 
			"    			    											select a.promotionBackOfficeId as custgr ,a.productBackOfficeId as prodid , (case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end) as ladu , 100 AS disc \r\n" + 
			"    			    							from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId where minqty = 0 and minPrice != maxPrice and minPrice = 0\r\n" + 
			"    			    											group by a.promotionBackOfficeId,a.productBackOfficeId,(case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end) \r\n" + 
			"    			    											order by 1,2";
	
	minP = " select a.promotionBackOfficeId as grupp ,a.productBackOfficeId as toode,  (case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end) as ladu," + 
					" [maxPrice]  AS price from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId " + 
			"	where minqty = 0 AND maxqty = 0  and minPrice != 0  and mandatory = 'N'" + 
			"			group by a.promotionBackOfficeId,a.productBackOfficeId,(case when b.regionBackofficeId = '24EWC' then '24N' else b.regionBackofficeId end), [maxPrice] ";
	
	 rs = stmt.executeQuery(minP);

 
 while(rs.next()){
	String gr = rs.getString("grupp").trim();
	String toode = rs.getString("toode").trim();
	String ladu = rs.getString("ladu").trim();
	Double discprice = rs.getDouble("price");

	if(usedProds.contains(toode) && (Product)prodHash.get(toode) != null && discprice > 0) { 
    Double priceNormal = ((Product)prodHash.get(toode)).getPrice();
	try {
    Double disc = 100 -( discprice / priceNormal )*100;
    disc = BigDecimal.valueOf(disc)
            .setScale(4, RoundingMode.HALF_UP)
            .doubleValue();
	sb0.append(gr+ ";" + toode + ";" + disc + ";" + ladu +"\n");
	}catch(Exception e) {
		System.out.println(toode + " " + priceNormal + " " + discprice );
		e.printStackTrace();
	}
	}
 }
rs.close();
if(sb0.length() > 0) {
	Converter.saveFile("maxdiscounts.txt", sb0.toString());
}
//System.out.println("prodMaxDisc " + prodMaxDisc.toString());
	


  
  stmt.close();
  con.close();
 // System.out.println("REASON " + sb.toString());
  if(sb.length() > 0){
	   Converter.saveFile("returnreason.txt", sb.toString());
  }
  
  if(sb3.length() > 0){
	   Converter.saveFile("repricereason.txt", sb3.toString());
  }
  
  if(sb2.length() > 0){
	 //  Converter.saveFile("warehouse.txt", sb2.toString());
   }
    
  
  if(sb30.length() > 0){
	   Converter.saveFile("reprgroups.txt", sb30.toString());
   }
      
  

	  
  
    } catch (Exception e){
      getParent().showError(e);
    }

    
    
    
    return products;
  }


  public static boolean startsWithDigitDash(String input) {
      return input.matches("^\\d{1,2} - .*");
  }

  public Vector getCustomerProductsList(String agentCode){
	  makeCustprod();
    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  
  class GroupResult {
	    String name;
	    double missing;

	    GroupResult(String name, double missing) {
	        this.name = name;
	        this.missing = missing;
	    }
	}
  
  

  public static String getCycleDay() {
	    LocalDate start = CYCLE_START;

	    LocalDate today = LocalDate.now();
	    long daysBetween = ChronoUnit.DAYS.between(start, today);

	    int cycleDayIndex = (int) (daysBetween % 28);
	    if (cycleDayIndex < 0) {
	        cycleDayIndex += 28;
	    }

	    int weekIndex = cycleDayIndex / 7;
	    int dayIndex = cycleDayIndex % 7;
	    char weekLetter = (char) ('A' + weekIndex);
	    int dayNumber = dayIndex + 1;

	    return weekLetter + String.valueOf(dayNumber);
	}
  
 /* public static String getCycleDayOld() { 8 nädalat on see
	  LocalDate start = CYCLE_START; 
      LocalDate today = LocalDate.now();
      long daysBetween = ChronoUnit.DAYS.between(start, today);

      int cycleDayIndex = (int) (daysBetween % 56);
      if (cycleDayIndex < 0) {
          cycleDayIndex += 56; 
      }

      int weekIndex = cycleDayIndex / 7;
      int dayIndex = cycleDayIndex % 7;
      char weekLetter = (char) ('A' + weekIndex);
      int dayNumber = dayIndex + 1;

      return weekLetter + String.valueOf(dayNumber);
  }*/

  
     HashMap<String, Map<String, Double>> custSalesGruppedFocus = null;
	 HashMap<String, Map<String, Double>> custPlanGruppedFocus = null;


     HashMap<String, Map<String, Double>> custSalesGrupped = null;
	 HashMap<String, Map<String, Double>> custPlanGrupped = null;
  
  public void setBulkSales(Map agents){
	   
    


      int yy = Calendar.getInstance().get(Calendar.YEAR);
      int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
      int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
      double planprec = dd/d2;
	  HashMap<String,String> grnames = new HashMap<String,String>();
	  String grSqlAdd = "";
	  if( noSalesGroups != null && noSalesGroups.size() > 0 ) {
		  grSqlAdd = "and EG_PRODUCTGROUPID not in (";
		  for (String gr : noSalesGroups) {
			  grSqlAdd = grSqlAdd + "'" + gr + "' ,";
		  }
		  grSqlAdd = grSqlAdd.substring(0,grSqlAdd.length()-1); // võtame viimase koma maha
		  grSqlAdd = grSqlAdd + ") ";
	  }
	 // Teeme gruppide myygid valmis 
	  String cmd0 =  "SELECT EG_AGENTID as agent, EG_PRODUCTGROUPID as gr,SUM(d.LINEAMOUNTMST ) AS summa\r\n" + 
	  		"	  		 FROM [BIF01_CUSTINVOICETRANS] d \r\n" + 
	  		"	  		 INNER JOIN BID01_ITEM b ON d.ITEMID = b.ITEMID \r\n" + 
	  		"	  		 WHERE d.INVOICEDATE >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)\r\n" + 
	  		"	  		AND d.INVOICEDATE < DATEADD(MONTH, 1,DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) \r\n" + 
	  		"			group by EG_AGENTID, EG_PRODUCTGROUPID having eg_agentid != ''" ;
	  
	try {	
	   Connection con = this.getNewConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd0);


	  manaSales = new HashMap<String, Map<String, Double>>();
	
	while(rs.next()) {
		
		    String agentID = rs.getString("agent").trim();
			String gr = rs.getString("gr");
		    Double sales = rs.getDouble("summa");
				 

			if(sales != null && sales > 0) {
						 manaSales
			                .computeIfAbsent(agentID, k -> new HashMap<String, Double>())
			                .merge(gr, sales, Double::sum);
			   }
		
	}
	
	rs.close();
	
	
	  String    sql = "  SELECT backOfficeId as liik,Name as nimetus FROM dbo.EPASAULE_BRANDS ";
  
	  rs = stmt.executeQuery(sql);
    while (rs.next()) {
      String liik = rs.getString("liik").trim();
        grnames.put(liik, rs.getString("nimetus").trim());
    }
    
    rs.close();
    stmt.close();
	con.close();
	
	}catch(Exception e) {
		e.printStackTrace();
	}
	//  System.out.println("manaSales " + manaSales.toString());
	  
	//Teeme igale managerile grupeeritud  grupeeritud Objekti 

	 HashMap<String, Map<String, Double>> manaSalesGrupped = new HashMap<String, Map<String, Double>>();
	 HashMap<String, Map<String, Double>> manaPlanGrupped = new HashMap<String, Map<String, Double>>();
	
	
	 HashMap<String, Map<String, Double>> manaSalesFocusGrupped = new HashMap<String, Map<String, Double>>();
	 HashMap<String, Map<String, Double>> manaPlanFocusGrupped = new HashMap<String, Map<String, Double>>();

	 Iterator<String> iterau = manaPlans.keySet().iterator();
	 while (iterau.hasNext()) {
		 String agID = iterau.next();

		 Map<String, Double> osales = manaPlans.get(agID);
		 
		 Iterator<String>  itera2 = osales.keySet().iterator();
			 while (itera2.hasNext()) {
				 String grID = itera2.next();
			double psales = osales.get(grID);
		  
		    if(this.groupGroupingFocus.get(grID) != null) {
		    	manaPlanFocusGrupped 
                .computeIfAbsent(agID, k -> new HashMap<String, Double>())
                .merge(groupGroupingFocus.get(grID), psales, Double::sum);
		 
		    }
		    if(this.groupGrouping.get(grID) != null) {
		    	manaPlanGrupped 
                .computeIfAbsent(agID, k -> new HashMap<String, Double>())
                .merge(groupGrouping.get(grID), psales, Double::sum);
		 
		    }
		    
		 }
	 }
	// System.out.println("grNames " + grnames.toString());
	 
	 HashMap<String, List<GroupResult>> managerResultsFocus = new HashMap<>();

	 HashMap<String, List<GroupResult>> managerResults = new HashMap<>();

	 Iterator<String> iteraq = manaSales.keySet().iterator();
	 while (iteraq.hasNext()) {
	     String agID = iteraq.next();

	     Map<String, Double> osales = manaSales.get(agID);
	     Map<String, Double> oplan = manaPlans.get(agID);

	     Iterator<String> itera2 = osales.keySet().iterator();
	     while (itera2.hasNext()) {
	         String grID = itera2.next();
	         

			 if(this.groupGroupingFocus.get(grID) != null) {
				String cat = this.groupGroupingFocus.get(grID); 
	         double psales = osales.get(grID);

	         if (oplan != null && oplan.get(grID) != null) {
	             double pplan = oplan.get(grID);
	             double missing =  psales - pplan;

	             String grnimi = grnames.get(grID);
	             if (grnimi == null) {
	                 grnimi = grID;
	             }

	             managerResultsFocus
	                 .computeIfAbsent(agID, k -> new ArrayList<>())
	                 .add(new GroupResult("focus " + grnimi + " (" + cat + ")", missing));
	         }

	         if (this.groupGroupingFocus.get(grID) != null) {
	             manaSalesFocusGrupped
	                 .computeIfAbsent(agID, k -> new HashMap<String, Double>())
	                 .merge(groupGroupingFocus.get(grID), psales, Double::sum);
	         }
	     }
			 

			 if(this.groupGrouping.get(grID) != null) {
				String cat = this.groupGrouping.get(grID); 
	         double psales = osales.get(grID);

	         if (oplan != null && oplan.get(grID) != null) {
	             double pplan = oplan.get(grID);
	             double missing =  psales - pplan;

	             String grnimi = grnames.get(grID);
	             if (grnimi == null) {
	                 grnimi = grID;
	             }

	             managerResults
	                 .computeIfAbsent(agID, k -> new ArrayList<>())
	                 .add(new GroupResult(grnimi + " (" + cat + ")", missing));
	         }

	         if (this.groupGrouping.get(grID) != null) {
	             manaSalesGrupped
	                 .computeIfAbsent(agID, k -> new HashMap<String, Double>())
	                 .merge(groupGrouping.get(grID), psales, Double::sum);
	         }
	     }
			 
			 }
	 }
	 
	 
	 HashMap <String, String> manaGrpouTotalsFocus = new HashMap<String, String>();

//	 HashMap <String, String> manaGrpouTotals = new HashMap<String, String>();
	 
	 for (String agID : managerResultsFocus.keySet()) {

	     List<GroupResult> results = managerResultsFocus.get(agID);

	     // sort by smallest missing first
	     results.sort(Comparator.comparingDouble(r -> r.missing));

	     StringBuilder sb = new StringBuilder();

	     for (GroupResult r : results) {
	         if (r.missing < 0) {
	             sb.append(r.name)
	               .append(" Diff : ")
	               .append((int) r.missing)
	               .append(" €|");
	         } else {
	             sb.append(r.name)
	               .append(" Diff : +")
	               .append((int) r.missing)
	               .append(" €|");
	         }
	     }

	     manaGrpouTotalsFocus.put(agID, sb.toString());
	 }	 

	 
/*	 for (String agID : managerResults.keySet()) {

	     List<GroupResult> results = managerResults.get(agID);

	     // sort by smallest missing first
	     results.sort(Comparator.comparingDouble(r -> r.missing));

	     StringBuilder sb = new StringBuilder();

	     for (GroupResult r : results) {
	         if (r.missing < 0) {
	             sb.append(r.name)
	               .append(" Diff : ")
	               .append((int) r.missing)
	               .append(" €|");
	         } else {
	             sb.append(r.name)
	               .append(" Diff : +")
	               .append((int) r.missing)
	               .append(" €|");
	         }
	     }

	     manaGrpouTotals.put(agID, sb.toString());
	 }	 */
	 // System.out.println("manaGrpouTotals " + manaGrpouTotals.toString());
	 
	// System.exit(0);
	// System.out.println("grupped " + manaPlanGrupped.toString() );
	 
	 HashMap <String, String> manaCategoryFocusTotals = new HashMap<String, String>();
	 
	 Iterator<String> itera = manaPlanFocusGrupped.keySet().iterator();
	 while (itera.hasNext()) {
		 String agID = itera.next();
		 
		  Map<String, Double> osales = manaPlanFocusGrupped.get(agID);
		  Map<String, Double> rsales = manaSalesFocusGrupped.get(agID);

		  Iterator<String>  itera2 = osales.keySet().iterator();
			 while (itera2.hasNext()) {
				 String grID = itera2.next();
				 
				 
			double psales = osales.get(grID);
		 if(rsales != null && rsales.get(grID) != null) {
		  
		  double msales = rsales.get(grID);
		  double  tehtudsalperc = msales/psales;
      	  double plaanitavMyykSal = d2 * msales/dd;
      	  double  prognosalperc = plaanitavMyykSal/psales;
      	
      	  if(tehtudsalperc > 10){
      		  tehtudsalperc = 0;  
      	  }
      	  
      	  if(prognosalperc > 10){
      		  prognosalperc = 0;  
      	  }
      	  
      	  String done = (int)(msales - psales)+"";
      	  if(!done.startsWith("-")) {
      		  done = "+" + done;
      	  }
      //	Plan- Plāns; Done- Izpilde; Diff- Starpība, esti- Projekcija
        	
      	  String asi = "Fokus Cat. : " + grID + "|Izpilde/Plāns : " + (int) msales +  " € / " + (int) psales +  " €|Starpība. : " + done + " €";

      	  if(tehtudsalperc > 0) {
      		  asi = asi + "|Izpilde/Projekcija : " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %| |";  
               }
            
            if(manaCategoryFocusTotals.get(agID) != null) {
                manaCategoryFocusTotals.put(agID, manaCategoryFocusTotals.get(agID) + asi);
             } else {
                manaCategoryFocusTotals.put(agID, asi);	
            }
            
		  }
		 
		}
	 }

 HashMap <String, String> manaCategoryTotals = new HashMap<String, String>();
	  itera = manaPlanGrupped.keySet().iterator();
	 while (itera.hasNext()) {
		 String agID = itera.next();
		 
		  Map<String, Double> osales = manaPlanGrupped.get(agID);
		  Map<String, Double> rsales = manaSalesGrupped.get(agID);

		  Iterator<String>  itera2 = osales.keySet().iterator();
			 while (itera2.hasNext()) {
				 String grID = itera2.next();
				 
				 
			double psales = osales.get(grID);
		 if(rsales != null && rsales.get(grID) != null) {
		  
		  double msales = rsales.get(grID);
		  double  tehtudsalperc = msales/psales;
      	  double plaanitavMyykSal = d2 * msales/dd;
      	  double  prognosalperc = plaanitavMyykSal/psales;
      	
      	  if(tehtudsalperc > 10){
      		  tehtudsalperc = 0;  
      	  }
      	  
      	  if(prognosalperc > 10){
      		  prognosalperc = 0;  
      	  }
      	  
      	  String done = (int)(msales - psales)+"";
      	  if(!done.startsWith("-")) {
      		  done = "+" + done;
      	  }
      	
        	//Plan- Plāns; Done- Izpilde; Diff- Starpība, esti- Projekcija
      	  String asi = " Kat. : " + grID + "|Izpilde/Plāns : " + (int) msales +  " € / " + (int) psales +  " €|Starpība : " + done + " €";

      	  if(tehtudsalperc > 0) {
      		  asi = asi + "|Izpilde/Projekcija : " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %| |";  
               }
            
            if(manaCategoryTotals.get(agID) != null) {
                manaCategoryTotals.put(agID, manaCategoryTotals.get(agID) + asi);
             } else {
                manaCategoryTotals.put(agID, asi);	
            }
            
		  }
		 
		}
	 }

	 
	 // teeme siin gruppidele eraldi missingu
	 // 1  selgitame välja top 10 grupi plaani
	 
	 
		String cmd = "SELECT EG_AGENTID as agent, SUM(d.LINEAMOUNTMST ) AS summa\r\n" + 
				"	  		 FROM [BIF01_CUSTINVOICETRANS] d \r\n" + 
				"	  		 INNER JOIN BID01_ITEM b ON d.ITEMID = b.ITEMID \r\n" + 
				"	  		 WHERE d.INVOICEDATE >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)\r\n" + 
				"	  		AND d.INVOICEDATE < DATEADD(MONTH, 1,DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) \r\n" + 
				"			group by EG_AGENTID having eg_agentid != ''";
		
		try {
			
			   Connection con = this.getNewConnection();
		       Statement stmt = con.createStatement();
		       ResultSet rs = stmt.executeQuery(cmd);
			
			while(rs.next()) {
				
				String agentID = rs.getString("agent").trim();
				Agent agent = (Agent)agents.get(agentID);
				
				double msales = (int)rs.getDouble("summa");
				if (agent != null){

					//agent.todayCycleCode = todayCycleCode;
					
				if(this.manaTotalPlans.get(agentID) != null) {
					String asi = "Done: " + (int)msales + " €|";
					
                    if(dd > 6){
                      double psales = manaTotalPlans.get(agentID);
                  	  double  tehtudsalperc = msales/psales;
                  	  double plaanitavMyykSal = d2 * msales/dd;
                  	  double  prognosalperc = plaanitavMyykSal/psales;
                  	
                  	  if(tehtudsalperc > 10){
                  		  tehtudsalperc = 0;  
                  	  }
                  	  
                  	  if(prognosalperc > 10){
                  		  prognosalperc = 0;  
                  	  }
//Plan- Plāns; Done- Izpilde; Diff- Starpība, esti- Projekcija
  					asi = "Izpilde: " + (int)msales + " €|";
  					asi = asi + " Plāns: " + (int)psales + " €|";

  		      	  String done = (int)(msales - psales)+"";
  		      	  if(!done.startsWith("-")) {
  		      		  done = "+" + done;
  		      	  }
  					asi = asi + " Starpība: " + done + " €|";
                  	  
                  	    asi = asi + "Izpilde/Projekcija. :";
                        asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                        
                        if(manaCategoryFocusTotals.get(agentID) != null) {
                        	asi = asi + manaCategoryFocusTotals.get(agentID); 
                        }
                        
                        if(manaCategoryTotals.get(agentID) != null) {
                        	asi = asi + manaCategoryTotals.get(agentID); 
                        }
                        
                        if(manaGrpouTotalsFocus.get(agentID) != null) {
                        	asi = asi + manaGrpouTotalsFocus.get(agentID); 
                        }
                      
                     //   if(manaGrpouTotals.get(agentID) != null) {
                       // 	asi = asi + manaGrpouTotals.get(agentID); 
                        //}
                        
                        agent.setStart(asi);
                        
                    }
                    
				  } else {
					agent.setMonthSales((int)msales);
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
  

  private Map<String, Map<String, Double>> manaSales;
  private Map<String, Map<String, Double>> custSales;

  private Map<String,  Double> custTotalSales;
  
  public void getCustomerPRGroupSales(){
		 

	  custSales = new HashMap<String, Map<String, Double>>();
	  custSalesGruppedFocus = new HashMap<String, Map<String, Double>>();
	  custSalesGrupped = new HashMap<String, Map<String, Double>>();
	  custTotalSales = new HashMap<String, Double>(); 
		String cmd =  " select clientBackOfficeID as cust , brandBackOfficeId as gr , sum(lineamount) as summa  from epasaule_deliveries d INNER JOIN dbo.EPASAULE_PRODUCTS a ON d.productBackOfficeID = a.backOfficeId where \r\n" + 
				"deliverydate >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)\r\n" + 
				"  AND deliverydate <  DATEADD(MONTH, 1,\r\n" + 
				"        DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))\r\n" + 
				"		group by clientBackOfficeID,brandBackOfficeId" ; //k.objekt2
		
		try {
			
			   Connection con = this.getNewConnection();
		       Statement stmt = con.createStatement();
		       ResultSet rs = stmt.executeQuery(cmd);

               int yy = Calendar.getInstance().get(Calendar.YEAR);
               int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
               int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
               double planprec = dd/d2;
		
			
			while(rs.next()) {
				
					String cust = rs.getString("cust");
					String gr = rs.getString("gr");
				    Double sales = rs.getDouble("summa");
						 

					if(sales != null && sales > 0) {
								 custSales
					                .computeIfAbsent(cust, k -> new HashMap<String, Double>())
					                .merge(gr, sales, Double::sum);
								 custTotalSales.merge(cust, sales, Double::sum);
					   
								 if (this.groupGroupingFocus.get(gr) != null) {
						             custSalesGruppedFocus
						                 .computeIfAbsent(cust, k -> new HashMap<String, Double>())
						                 .merge(groupGroupingFocus.get(gr), sales, Double::sum);
						         }
								 

								 if (this.groupGrouping.get(gr) != null) {
						             custSalesGrupped
						                 .computeIfAbsent(cust, k -> new HashMap<String, Double>())
						                 .merge(groupGrouping.get(gr), sales, Double::sum);
						         }
					   }
				 }
			
			rs.close();
			stmt.close();
			con.close();
			
		} catch (Exception e) {
			getParent().showError(e);
		}
		  System.out.println("custSales " + custSales.size());

		  System.out.println("custTotalSales " + custTotalSales.size());
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


  public String getDriverVersion(){
    return "EugestaLAT";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){
  	      return null;
  	  }


       public NumberFormat getNF(){
         return NF;
       }


    	@Override
       	public void WebReporter() {
       		
    	}

       public void setNF(NumberFormat aNF){
         NF = aNF;
       }

      private Vector debt = new Vector();
	  private Vector custProds = new Vector();
	  private String subjectFilesTo = null;
      private NumberFormat NF = NumberFormat.getInstance();



	@Override
	public void PictureMaker() {
		  makePictureCat();
		  System.exit(0);
		
	}

	@Override
	public String getStartingTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void FixLoader() {
		HashMap<String, Customer> customers = new HashMap<String, Customer>();
		HashMap<String, Product> products = new HashMap<String, Product>();

		ArrayList<Quest> quests = Converter.loadPriceTrackerFromExcels("excelpricetracker", rcompany, user, pass, parent, customers, products);
		   System.exit(0);
	
	}


}


