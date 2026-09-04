package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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

public class EugestaESTMERCDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter,WebReporter {
  public EugestaESTMERCDBDriver(EksportPanel parent){
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
		

	String sq3 = "   SELECT jou.clientBackOfficeId as klient, jou.deliverydate  as kuup , jou.productBackOfficeId as toode ,\r\n" + 
			"			 sum(case when jou.Quantity > 0 then jou.Quantity else 0 end) as kogus,\r\n" + 
			"			 sum(case when jou.Quantity < 0 then jou.Quantity else 0 end) as kogusmin, max (jou.invoiceNumber) as doknr ,\r\n" + 
			"			 case when  jou.deliverydate > CAST(GETDATE() AS DATE)then 1 else 0 end as color FROM EPASAULE_DELIVERIES  jou  WHERE  jou.deliverydate >=   \r\n" + 
			"						(select top 1 kuupaev  from ( 	 SELECT top 10 jou2.deliverydate as kuupaev FROM  EPASAULE_DELIVERIES jou2  where ltrim(jou2.clientBackOfficeId) = ltrim(jou.clientBackOfficeId)   \r\n" + 
			"						 and jou2.deliverydate > getdate()-30 and jou2.deliverydate < getdate()+4 group by jou2.clientBackOfficeId , jou2.deliverydate order by jou2.deliverydate desc) as dd order by kuupaev)   \r\n" + 
			"				 GROUP BY jou.clientBackOfficeId,jou.deliverydate , jou.productBackOfficeId ORDER BY jou.clientBackOfficeId,jou.deliverydate desc, max(jou.invoiceNumber) desc, jou.productBackOfficeId" ;
					
					Connection con3 = getNewConnection();
					Statement st3 = con3.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(Converter.turn(rs3.getString("kuup")).substring(0,5));
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
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
	
   	return null;
   }

  

  public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		try {
			
			String  cmd = " SELECT  s.assortimentBackOfficeId as ClientGR ,s.productBackOfficeId as ItemID , s.NEWINASSORT as newin " + 
					"					FROM EPASAULE_ASSORTMENTS s inner join dbo.EPASAULE_PRODUCTS a ON a.backOfficeId = s.productBackOfficeId"
					+ "  where s.EG_ASSORTITEMSTATUS = 1 order by str(a.posWeight) + a.Name ";
			
		
		         if (isDebug()) System.out.println(cmd);
		           Connection con = getNewConnection();
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd);

		           while (rs.next()) {

		        	   
		             String sortid = rs.getString("ClientGR").trim()+"x";
		             String prodID = rs.getString("ItemID").trim();
		             int optio = rs.getInt("newin");
		             
		   
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
								}
								if(!inTT.contains(sortid)) {
									result.add(mline);	
								}
							}
						
					 rs.close();
					 st.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}


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
		 					    	  e.printStackTrace();
		 						  }
								
								String segment = null;
								if(cust == null  || cust.trim().length() == 0) {
								try {
									segment = formatter.formatCellValue(row.getCell((short) 2));
		 					      } catch (Exception e) {
                                    e.printStackTrace();
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
				 					       e.printStackTrace();
				 					      }	
										
										
									
									Double price = null;
									try {
										price = Double.parseDouble(formatter.formatCellValue(row.getCell((short) 6)));
			 					      } catch (Exception e) {
			 					    	  e.printStackTrace();
			 					      }

									String text = null;
									try {
										text =  Converter.enCodeEst(formatter.formatCellValue(row.getCell((short) 7)));
			 					      } catch (Exception e) {
	                                   e.printStackTrace();
			 						  }

									String text2 = null;
									try {
										text2 =  Converter.enCodeEst(formatter.formatCellValue(row.getCell((short) 8)));
			 					      } catch (Exception e) {
	                                   e.printStackTrace();
			 						  }								

									Date naita = null;
									try {
										naita = (row.getCell((short) 9).getDateCellValue());
			 					      } catch (Exception e) {
			 					    	 
			 						  }
									Date algus = null;
									try {
										algus = (row.getCell((short) 10).getDateCellValue());
			 					      } catch (Exception e) {
			 					    	 
			 						  }
									Date lopp = null;
									try {
										lopp = (row.getCell((short) 11).getDateCellValue());
			 					      } catch (Exception e) {
			 					    	 
			 						  }
									
								


								
								Calendar cc = Calendar.getInstance();
								boolean goes= false;
								if(naita != null) {
									if(cc.getTimeInMillis() > naita.getTime() && cc.getTimeInMillis() < lopp.getTime()) {
									  goes = true;
									}
								 } else {
								if(cc.getTimeInMillis() > algus.getTime() && cc.getTimeInMillis() < lopp.getTime()) {
								  goes = true;	
								}
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
							        	 //  System.out.println("acust " + cust + " " + prod);
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
							           mline.setStartDate(Converter.getDDsMMMsYYYYString(algus));
							           mline.setEndDate(Converter.getDDsMMMsYYYYString(lopp));
							           if(price == null) {
							        	//   System.out.println("asi " + asi + " " + prod);
							        	   price = 1.0;
							           }
							           mline.setPrice(price - 0.001);
							           if(text != null && text.trim().length() > 0) {
							        	   mline.setLineinfo((text + " " + text2).replaceAll("\\r?\\n|\\r", "|").replaceAll(";", ","));
							           }else if(text2 != null && text2.trim().length() > 0){
							        	   mline.setLineinfo(text2.replaceAll("\\r?\\n|\\r", "|").replaceAll(";", ","));
							           }
							           lines.add(mline);
							      // 	System.out.println("line " + mline.getString());
									}
								
							}
								
							}
							
							}
							
							
         }catch(Exception r) {
		 r.printStackTrace();  
	     }	
	//System.out.println("Active Promos "  + activeXLSPromos.toString());

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
	    		" FROM EPASAULE_PRICES p inner join EPASAULE_PRODUCTS a on a.backOfficeId = productBackOfficeId and p.price !=a.price where "
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
      
           String sql2 = "select   a.promotionBackOfficeId as clgroup , productBackOfficeId as prodid, [date from] as startday,[date to] as endday, \r\n" + 
           		"           	          		minPrice as price ,b.regionbackofficeid as dealer from dbo.EPASAULE_PROMOTIONOBJECTS a \r\n" + 
           		"           		           		inner join dbo.EPASAULE_PROMO b on a.promotionBackOfficeId = b.promotionBackOfficeId  \r\n" + 
           		"								inner join EPASAULE_CLIENTS c ON a.clientBackOfficeId = c.BackOfficeId\r\n" + 
           		"    	              		 	 where c.nextdelivery between [date from] and [date to]  and minqty = 0 and minPrice = maxPrice   \r\n" + 
           		"           		           		 group by  a.promotionBackOfficeId , productBackOfficeId, b.regionbackofficeid,  [date from] ,[date to], minPrice \r\n" + 
           		"           		           		 order by a.promotionBackOfficeId , productBackOfficeId, b.regionbackofficeid, [date from] ,[date to], minPrice";
           rs = stmt.executeQuery(sql2);
           while (rs.next()) {

           String clGroup = rs.getString("clgroup");
           String prodID = rs.getString("prodid").trim();
           String sdate = rs.getString("startday");
           String edate = rs.getString("endday");
           double price = rs.getDouble("price");
           String dealer = rs.getString("dealer");
           
           MatrixLine mline = new MatrixLine();
           mline.setCliGroup(clGroup);
           mline.setProdID(prodID);
           mline.setStartDate(sdate.substring(0,10));
           mline.setEndDate(edate.substring(0,10));
           mline.setPrice(price);
            if(dealer != null && !dealer.trim().equals("03N")) {// kui ei ole pealadu siis paneme külge
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
	/*  StringBuffer result = new StringBuffer();
		
		try {
			
			String  cmd = " SELECT  s.assortimentBackOfficeId as ClientGR ,s.productBackOfficeId as ItemID , s.NEWINASSORT as newin " + 
					"	FROM EPASAULE_ASSORTMENTS s inner join dbo.EPASAULE_PRODUCTS a ON a.backOfficeId = s.productBackOfficeId  where s.EG_ASSORTITEMSTATUS = 1 order by str(a.posWeight) + a.Name";
			
		
		         if (isDebug()) System.out.println(cmd);
		           Connection con = getNewConnection();
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd);

		           while (rs.next()) {

		             String sortid = rs.getString("ClientGR").trim();
		             String prodID = rs.getString("ItemID").trim();
		       
								if(inTT.contains(sortid)) {
									result.append( " ;" + Converter.cleanLitLatEst(sortid + "; ;" + prodID + "; ;")+ "\n");
		 						}
							}
						
					 rs.close();
					 st.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
             

	//	System.out.println("ASSO size " + result.toString());
	
		return result.toString();*/

	  return null;

  }

  public String getSaveStockInfo(){
	  	  

	  String cmd = " Select productBackofficeId as toode,sum(Inventory) as stock , regionBackofficeId as ladu   from "
	  		+ " dbo.EPASAULE_INVENTORIES   group by productBackofficeId,regionBackofficeId ";
       //where regionBackofficeId in ('03N', '03D', '03C')

	  
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
	           
	           if(seis > 0){
	        	       products.append(kood +  ";");
	                   products.append(" ;");
	                   products.append(getParent().getNF().format(seis) + ";" + dealer +  "\n");
	                   products.append(kood +  ";");
	                   products.append(" ;");
	                   products.append(getParent().getNF().format(seis) + ";" + dealer +  "\n");
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

    	  String  cmd = "Select productBackofficeId as toode,sum(Inventory) as stock ,regionBackofficeId as ladu from " + 
    	  		"	  		 dbo.EPASAULE_INVENTORIES  group by productBackofficeId,regionBackofficeId";
     // where regionBackofficeId in ('03N','03D','03C')
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
                   Dealer dealer = new Dealer(kood);
                   dealer.setName(Converter.enCodeEst(rst.getString("name").trim()));
                   dealers.put(dealer.getID(), dealer);
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
		 
	  
		//  1. teeme tellimus ja arve listi 
		  
		  String cmd = "select SALESID as tell,  INVOICEID as arve from BIF01_CUSTINVOICETRANS b where invoicedate > getdate()-100 group by SALESID,INVOICEID " ;

		  String cmd1 = "	 SELECT    a.CUSTACCOUNT AS klient,   a.ITEMID AS toode,    sum(a.INVOICEQTY) AS kogus   FROM BIF03_SALESLINE a  \r\n" + 
		  		"	  			  		    WHERE a.SALESDATE > GETDATE() - 180 AND a.EG_SALESCANCELREASONID <> 'GKR'   AND a.INVENTLOCATIONID != 'STORNO'  \r\n" + 
		  		"	  			  		      AND a.SALESSTATUS <> 1 group by CUSTACCOUNT, ITEMID  \r\n" + 
		  		"	  						  union  \r\n" + 
		  		"	  						   SELECT   a.CUSTACCOUNT AS klient, a.SellerItemCode AS toode, sum( a.AmountDespatched) AS kogus \r\n" + 
		  		"	  			  		    FROM [MJ_EE].[dbo].[Sales_GKR] a  group by CUSTACCOUNT, SellerItemCode " ;
		  
			  
		  
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
		  		"	  			  		    FROM BIF03_SALESLINE a \r\n" + 
		  		"	  		 Inner join dbo.EPASAULE_PRODUCTS t on t.backOfficeId = a.ITEMID  \r\n" + 
		  		"	  			  		    LEFT JOIN BID44_SALESCANCELREASONS c  \r\n" + 
		  		"	  			  		        ON c.EG_SALESCANCELREASONID = a.EG_SALESCANCELREASONID  \r\n" + 
		  		"	  			  		    WHERE a.SALESDATE > GETDATE() - 180  \r\n" + 
		  		"	  			  		      AND a.EG_SALESCANCELREASONID <> 'GKR'  \r\n" + 
		  		"	  			  		      AND a.INVENTLOCATIONID != 'STORNO'  \r\n" + 
		  		"	  			  		      AND a.SALESSTATUS <> 1  \r\n" + 
		  		"	  						  union  \r\n" + 
		  		"	  						   SELECT    \r\n" + 
		  		"	  			  		        a.CUSTACCOUNT AS klient,  \r\n" + 
		  		"	  			  		        a.SellerItemCode AS toode,  \r\n" + 
		  		"	  		                    RIGHT('00000000' + CAST(t.posWeight AS VARCHAR(20)), 8) as sorting,  \r\n" + 
		  		"	  			  		        0 AS tellitud,  \r\n" + 
		  		"	  			  		        a.AmountDespatched AS sisse,  \r\n" + 
		  		"	  			  		        0 AS tarnimata,  \r\n" + 
		  		"	  			  		        '' AS CANCELREASON,  \r\n" + 
		  		"	  			  		        a.DELIVERYDATEACTUAL AS lastdate,  \r\n" + 
		  		"	  			  		        (a.ItemPrice * (100 - a.DiscountPercent)/100 ) AS lastprice,  \r\n" + 
		  		"	  			  		        a.orderno AS ordernr, \r\n" + 
		  		"	  			  		        ROW_NUMBER() OVER (  \r\n" + 
		  		"	  			  		            PARTITION BY a.CUSTACCOUNT, a.SellerItemCode  \r\n" + 
		  		"	  			  		            ORDER BY a.DELIVERYDATEACTUAL DESC  \r\n" + 
		  		"	  			  		        ) AS rn  \r\n" + 
		  		"	  			  		    FROM [MJ_EE].[dbo].[Sales_GKR] a   \r\n" + 
		  		"	  		 Inner join dbo.EPASAULE_PRODUCTS t on t.backOfficeId = a.SellerItemCode  \r\n" + 
		  		"	  			  		)  \r\n" + 
		  		"	  			  		  \r\n" + 
		  		"	  			  		SELECT *  \r\n" + 
		  		"	  			  		FROM cte  \r\n" + 
		  		"	  			  		WHERE rn = 1 order by klient, sorting ,toode ";
		  

		   // if (isDebug()) System.out.println(cmd2);

		    try {
		    	
		    	Calendar cal = Calendar.getInstance();
		    	cal.add(Calendar.DAY_OF_MONTH, -15);
		    	
		    	HashMap<String, String> arveNRid = new HashMap<String,String>();

		    	HashMap<String, Double> last180d = new HashMap<String,Double>();
		    	
		    	Connection con = getNewConnection();
		     	Statement stmt = con.createStatement();
		
		    	
		     	ResultSet rs = stmt.executeQuery(cmd);
		
		      while(rs.next()){
		    	  String tell = rs.getString("tell").trim();
		    	  
		    	  if(tell.length() > 9) {
		    		  tell = tell.substring(0,9);
		    	  }
		        arveNRid.put(tell, rs.getString("arve").trim());
		      }
		        rs.close();
		     	
		        rs = stmt.executeQuery(cmd1);
		    	
			      while(rs.next()){
			    	  last180d.put(rs.getString("klient").trim() + "-" +  rs.getString("toode").trim(), rs.getDouble("kogus"));
			      }
			        rs.close();
		     	
		     	System.out.println("last180d size " + last180d.size());
		     	
		     	 rs = stmt.executeQuery(cmd2);
		
		      while(rs.next()){
		        String kliendikood = rs.getString("klient").trim();
		        String tootekood = rs.getString("toode").trim();
		        double kogus = rs.getDouble("sisse");
		        double mata = rs.getDouble("tarnimata");
			    String reason =  rs.getString("CANCELREASON");
			    if(reason != null && reason.trim().length() > 0) {
			    	reason = reason.trim();
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
		               //  lastdate = lastdate.substring(5,10);
		                 lastdate = lastdate.substring(0,5);
		                 double lastprice = rs.getDouble("lastprice");
		                 String ord = rs.getString("ordernr");
		                 String tell = ord.trim();
		   	    	  
		   	    	  if(tell.length() > 9) {
		   	    		  tell = tell.substring(0,9);
		   	    	  }
		                 String arv = arveNRid.get(tell);

		                 if(ord != null && ord.trim().length() > 0 ) {
		                	 ord = "t: " + ord.trim();
		                 }else {
		                	 ord = "";
		                 }
		                 
		                 if(arv != null && arv.trim().length() > 0 ) {
		                	 arv = "a: " + arv.trim();
		                 }else {
		                	 arv = "";
		                 }
		                 
		         String lastdoc = (ord + " " + arv).trim();
		         
		         double sold180 = 0;
		         Double aaa = last180d.get(kliendikood + "-" + tootekood);
		         if(aaa > 0) {
		        	 sold180 = aaa;
		         }
		         if(kogus != 0) {
		  	       
		        CustProd cp = new CustProd(kliendikood, tootekood, kogus , sold180 , 0,lastdate,lastprice,mata,0,reason);
		        if(lastdoc.length() > 0) {
	                cp.setAbiKood(lastdoc);
		        }
		        custProds.add(cp);
		        prods.addElement(tootekood);
		      }
		      }
		      rs.close();
		      stmt.close();
		      con.close();
		    } catch (Exception e){
		      getParent().showError(e);
		    }
	  }
	  


  private HashSet<String> agsActive = null;
  
  public TreeMap getAgents(){
	  
	    
    String cmd = "select backOfficeId as kood ,agentName as name , repricingLimit as repr from  dbo.EPASAULE_AGENTS";
    String cmdg = "select Agent_BackofficeID as kood , group_backofficeID as grupp,forSale, forMerch from EPASAULE_AGENTS_EAGENTGROUPS";
    String cmdb = "select Agent_BackofficeID as kood , brand_backofficeID as brand,forSale, forMerch from EPASAULE_AGENTS_EAGENTBRANDS";

    Properties props = EksportFrame.loadProps(parent.getProps().getProperty("emailfile"));
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();

    agsActive = new HashSet<String>();
    
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
          }else {
         	  agent.repriceLimit = 0.0;
          }
          agent.setName(name);
          agent.setIsPilot(true);
          
          if(grs.size() > 0 && grs.get(agentID) != null) {
        	  agent.setProdGroupList((ArrayList)grs.get(agentID));
          }
          
          if(grsS.size() > 0 && grsS.get(agentID) != null) {
        	  agent.setProdGroupListSec((ArrayList)grsS.get(agentID));
          }
          
          agent.setLanguage("EST");
          agent.setCountry("et");
          agent.setPassWord("0000");
          String agentAGENTS = props.getProperty(agent.getID());
   		if(agentAGENTS != null){
   			agsActive.add(agent.getID());
   		}
          
          agents.put(name, agent);
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
  private static final LocalDate CYCLE_START =   LocalDate.parse("01.11.2021", FMT);
  
  
 
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

  
  public static boolean matchesNextSevenDays(String pattern) {
	    return !nextSevenDaysMatches(pattern).isEmpty();
	}
  
  public static List<CycleMatch> nextSevenDaysMatches(String pattern) {

	    DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	    LocalDate cycleStart = LocalDate.parse("01.11.2021", FMT);

	    LocalDate today = LocalDate.now();
	    List<CycleMatch> result = new ArrayList<>();

	    for (int i = 0; i < 7; i++) {

	        LocalDate date = today.plusDays(i);

	        long days = ChronoUnit.DAYS.between(cycleStart, date);
	        if (days < 0) continue;

	        int weekInCycle = (int) ((days / 7) % 8);
	        int weekday = date.getDayOfWeek().getValue();

	        char cycleWeekLetter = (char) ('A' + weekInCycle);

	        // find that week block in pattern
	        int index = pattern.indexOf(cycleWeekLetter);

	        if (index != -1 && index + 1 < pattern.length()) {

	            // read digits after letter until next letter
	            StringBuilder daysPart = new StringBuilder();

	            for (int j = index + 1; j < pattern.length(); j++) {
	                char c = pattern.charAt(j);
	                if (Character.isLetter(c)) break;
	                daysPart.append(c);
	            }

	            if (daysPart.toString().contains(String.valueOf(weekday))) {
	                result.add(new CycleMatch(date, cycleWeekLetter + String.valueOf(weekday)));
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
		  
         
        // System.out.println("ress " + res.toString());
		  return res;
	  }
  
	private  TreeMap<String,ArrayList <String>> custCategories = null;
  
  public TreeMap getCustomers(Agent agent){
	  nf.setMinimumFractionDigits(2);
	
   try{
    	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
    } catch (Exception e){
    	e.printStackTrace();
    }
   
   
    custCategories  = getCustCategories();
    if(activeXLSPromos == null) {
		activeXLSPromos = new HashMap<String,List<String>>();
	    getCampExcelLines(new Vector());
    }
    

	 // System.out.println("custCategories " +  custCategories.toString());
   TreeMap<String,String> displays  =   Converter.getDisplayInfoFromExcel(this.getExcelDir(), custCategories);
   
	 // System.out.println("displays " +  displays.toString());
	  
	 /// System.exit(0);
    TreeMap deldays =  makeCustomerDeliveryDays();
   
    
     inTT = new HashSet<String>();
	
	HashMap<String, String> promos = new HashMap<String, String>();
    HashMap<String, Vector<Agent>> cliags = new HashMap<String, Vector<Agent>>();
  
    
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap indeksT = new TreeMap();
    
    try {
    	
    	   String aaa = "SELECT AgentBackofficeID as kood , ClientBackofficeID as klient, route  from  dbo.EPASAULE_AGENTS_CLIENTS " ;

       	StringBuffer sb0 = new StringBuffer();
    	   Connection con = this.getNewConnection();
    	 
    		 Statement stmt = con.createStatement();
    	     ResultSet rs1 = stmt.executeQuery(aaa);
    	               while(rs1.next()){
    	                 Agent ag = new Agent(rs1.getString("kood").trim());
    	                 String cli = rs1.getString("klient").trim();
    	                 String route = rs1.getString("route");
    	                 if(route != null && route.trim().length() > 0) {

                             if(agsActive.contains(ag.getID())) {
     	                	   sb0.append(cli + ";" + ag.getID() + ";" + route + "\n");
                             }
                             
    	                	 List<CycleMatch> ll =  nextSevenDaysMatches(route);
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
    	       		
    	       	
    	
    	
      	
              	  
                    String cmd = " Select a.name ,BackOfficeId as accountnum, debitorBackOfficeId as payer,CreditLimit as limit , (case when EG_IGNOREMINORDER = 0 then minOrderSum else 0 end) as minsum, '' as linedisc\r\n" + 
                    		"                                                                                                             , pricelistBackOfficeId as pricegroup,a.address ,\r\n" + 
                    		"                                                             Status AS block , channel as chain , clientType as typeo,b.description as type, phone , contactPerson as pername, email as peremail,\r\n" + 
                    		"                                                                 contactPhone as perphone,  accountantemail as acco,  EG_EMAILDEBTS as bill ,consignationPeriod as term,a.city as city  , alcoLicense as alic, tobaccoLicense as tlic,\r\n" + 
                    		"                                                             contractDate as cdate , segmentname as seg , assortimentBackOfficeId as fixed ,subsegmentname as segsub, [EG_EXTERNALCODE] as type2, gkrnextdelivery as via3l, nextdelivery as del1, nextdelivery2 as del2  \r\n" + 
                    		"                                                                                           from  dbo.EPASAULE_CLIENTS a  left join BID29_LINEOFBUSINESS b on a.clientType=b.LINEOFBUSINESSID\r\n" + 
                    		"                                                                                           LEFT JOIN [BID02_CUSTORDER] c on a.BackOfficeId=c.ACCOUNTNUM\r\n" + 
                    		" " ; //, EG_EXTERNALCODE as type2
                    

                     if (isDebug()) System.out.println(cmd);
                    
                    
                    
    	 stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
 
      while(rs.next()){
        String name = rs.getString("name").trim();
        
        boolean goes = true;
        
        String accountnum = rs.getString("accountnum").trim();
       
        Customer cust = new Customer(accountnum);
        cust.setPayer(rs.getString("payer"));
      
         
        Double minsum = rs.getDouble("minsum");
        if(minsum != null) {
        	cust.setMinsum(minsum);
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
        	
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ').replaceAll(country, " ").replaceAll("\\s+", " ").trim()), country));
       
          if(aadr.contains(",")) {
          		cust.restStreet = aadr.trim().substring(0,aadr.trim().indexOf(","));
          		cust.restCity = aadr.trim().substring(aadr.trim().indexOf(",")+1);
          	}else {
          		cust.restStreet = aadr.trim();	
          	}
           	cust.setCountry(country);
          }
        
        if(deldays.get(cust.getID())  != null){
        	cust.setDelDays((String)deldays.get(cust.getID()));
        }    

        
        cust.restName = name;
       // name = Converter.enCodeEst(name);
      //  name = name.replaceAll("\\([^)]*\\)", "").replaceAll("\\s+", " ").trim();
        name = name.replaceAll("\\s*\\([^()]*\\)\\s*$", "").trim();
        name = name + " " + cust.getAddress() + " " + accountnum ;
        cust.setName(name);
        
        if(indeksT.get(cust.getID()) != null){
        	cust.addCity("Postiindeks: " + indeksT.get(cust.getID()));
        }
        
        
       if(cliags.get(cust.getID()) != null) {
         Vector<Agent> vv = cliags.get(cust.getID());
         
         for(Agent agentL : vv) {
          cust.addAgent(agentL);
          if(agentL.nextContact != null) {
              cust.addCity("Järgmine: " + agentL.getID() + " '" + agentL.nextContact.format(FMT).replace('.', '-').replace('.', '-') + "'");  
          }
          
          if(agentL.getVisit() != null && agentL.getVisit().length() > 0) {
        	  cust.setMainAgent(agentL.getID());
          }
        
         Agent aa1 = (Agent)agent1.get(agentL.getID());
           if(aa1 != null) {
      		Vector vec = aa1.getDubbler();
  		      if(vec!= null && vec.size() >0){
  			   Enumeration en = vec.elements();
  			   while(en.hasMoreElements()){
  				 String age = (String) en.nextElement();
  				 Agent ee = new Agent(age);
 		          if(agentL.getVisit() != null && agentL.getVisit().length() > 0) {
 				     ee.setVisit(agentL.getVisit());// panene ka asendatava päeva plaani juurde
 		          }
  				 cust.addAgent(new Agent(age));
  			   }
  	       }
          }
         }
     	}
      
        
        
        String chain = rs.getString("chain");
        if(chain != null && chain.trim().length() > 0) {
        	cust.setChain(chain.trim());
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
        
        
        
        String info = rs.getString("phone");
        String pername = rs.getString("pername");
        String peremail = rs.getString("peremail");
        String perphone = rs.getString("perphone");
        
        if (pername != null && pername.trim().length()>0 && !cust.restName.contains(pername)) {
            cust.restPerson = pername.trim(); 
            cust.adPersonalData( pername.trim() + "|");
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
        	        cust.adPersonalData("RP: " + raam.trim().toLowerCase() + "|");
        	   }
           }
        }
        
        
        String arve = rs.getString("bill");
        if (arve != null && arve.trim().length()>0) {
           if(arve.contains(";")) {
        	   arve = arve.substring(0, arve.indexOf(";"));
           }
           
           if(arve.indexOf("@") != -1) {
        	        cust.adPersonalData("Arve: " + arve.trim().toLowerCase());
           }
        }
        
        
        if(cust.getPersonalData().endsWith("|")) {
        	cust.setPersonalData(cust.getPersonalData().substring(0,cust.getPersonalData().length()-1));
        }
        
        Integer term = rs.getInt("term");
        if(term != null && term > 0) {
        //	cust.setPaymTerm(term + "");
        }

        String alic = rs.getString("alic");
        if(alic != null && alic.trim().length() > 0) {
        	//cust.setPaymTerm(term + "");
        }
        
        String tlic = rs.getString("tlic");
        if(tlic != null && tlic.trim().length() > 0) {
        	//cust.setPaymTerm(term + "");
        }
        
        java.sql.Date cdate = rs.getDate("cdate");
        if(cdate.getYear() > 1900) {
        	
        }
        
        String seg = rs.getString("seg");
        if(seg != null && seg.trim().length() > 0) {
        	cust.addCity("Kett: " + seg.trim());
        	
        }
        
        String fixed = rs.getString("fixed");
        if(fixed != null && fixed.trim().length() > 0) {
        	if(seg != null && seg.trim().equals("TT")) {
        		inTT.add(fixed.trim());
        		cust.suggAssortCode = fixed.trim();
        	 } else {
        	  cust.addFixSortCode(fixed.trim()+"x");
        	}
        }

        String segsub = rs.getString("segsub");
        if(segsub != null && segsub.trim().length() > 0) {
        	cust.addCity("Segment: " + segsub.trim());
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
        	cust.addCity("Tunnus: " + type2.trim());
        }

        
        String del1 = rs.getString("del1");
        String del2 = rs.getString("del2");
       
        if(del1 != null && del1.trim().length() > 0) {
        	cust.addCity("EUG Tarne: '" + del1.trim() + "'");
        }
        if(del2 != null && del2.trim().length() > 0) {
        	//cust.addCity("Tarne 2: '" + del2.trim()+ "'");
        }
        
        cust.setDealer(new Dealer("03N"));
        
        if(displays != null && displays.get(cust.getID()) != null) {
        	cust.setInfo(displays.get(cust.getID()));
        //	cust.addCity("|" +displays.get(cust.getID())+"|");
        }
      
        
        if(goes) {
            customers.put(name, cust);
            custMap.put(cust.getID(),cust);	
        }
  
      }



  rs.close();
  

	       
				 
			String fff = " select a.promotionBackOfficeId as custgr ,a.productBackOfficeId as prodid, a.minqty, a.minPrice , max (a.[date to]) as paev , max(b.description) as nimi , max(b.regionbackofficeid) as ladu from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN \r\n" + 
				  		"EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId where minqty > 0  group by a.promotionBackOfficeId,a.productBackOfficeId, minqty, minPrice\r\n" + 
				  		"				  				order by a.promotionBackOfficeId,productBackOfficeId, minqty, minPrice";
				 

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
				   	if(ladu.equals("03N")) {
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
				        
				  /*  StringBuffer reprBuff = new StringBuffer();    
				        reprBuff.append("R101;20\nR212;30");
				        
				        
				        if(reprBuff.length() > 2){
				            File f = new File("reprgroups.txt");
				            if (f.exists())f.delete();
				              try {
				        		PrintWriter os = new PrintWriter(new FileWriter(
				        				 "reprgroups.txt", true));
				        		os.write(reprBuff.toString());
				        		os.close();
				        	  }catch (IOException ex) {
				        		ex.printStackTrace();
				        	  }
				         }else{
				            File f = new File("reprgroups.txt");
				            if (f.exists())f.delete();
				              try {
				        		PrintWriter os = new PrintWriter(new FileWriter(
				        				 "reprgroups.txt", true));
				        		os.write("");
				        		os.close();
				        	  }catch (IOException ex) {
				        		ex.printStackTrace();
				        	  }
				         }*/
				     
              
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

	   Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();

	    String    sql = " SELECT backOfficeId as liik,Name as nimetus FROM dbo.EPASAULE_GROUPS ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String nimi = Converter.cleanLitLatEst(rs.getString("nimetus").trim());
	      nimi = nimi.replaceAll("NESTLE", "NES");
	      nimi = nimi.replaceAll("BARILLA", "BAR");
	      nimi = nimi.replaceAll("IMPERIAL", "IMP");
		  
	        tm.put(liik, Converter.enCodeEst(nimi));
	    }
	    rs.close();
	    st.close();
	    con.close();
  return tm;	   
}


  public TreeMap getProdGroupsSecond() throws SQLException {
	  
	    TreeMap   tm = new TreeMap();
	  /*  String    sql = "  SELECT backOfficeId as liik, Name as nimetus FROM dbo.EPASAULE_GROUPS  ";
	    Connection con = getNewConnection();
		
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
	    }
	    
	    rs.close();
	    st.close();
	    con.close();*/

	    return tm;

  }

  private Vector prodcodes = null;

  private HashMap<String,Double> prodprices = null;
  private HashMap<String,String> eancodes = null;
  
  public TreeMap getProducts(Agent agent){
	  prodcodes = new Vector();

	  String noOPT = getParent().getProps().getProperty("nostepgroup");
	  
	    String cmd = "  Select posWeight as sorting , a.backOfficeId as itemid,a.Name AS nimetus,c.Name as namebr, a.quantityBox AS kastis,a.quantityPallete as alusel , b.CONVERTLAYER as layer , a.quantityPack as pakk,  \r\n" + 
	    		"    		a.Price AS price, a.barCode AS ean,a.groupBackOfficeId AS grupp,  \r\n" + 
	    		"    		a.groupBackOfficeId AS grupp2 ,a.increasestep as opt, a.minimalOrderQuantity as mini ,a.unitName AS unit, a.vatPercent as vat , '' as sorting from  dbo.EPASAULE_PRODUCTS a LEFT join dbo.BID01_ITEM b on b.itemid= a.backOfficeId\r\n" + 
	    		"			 left join EPASAULE_BRANDS c on c.backOfficeId = a.brandBackofficeId "  ;


             String cmd2 = " Select productBackofficeId as toode,sum(Inventory) as stock, regionBackofficeId as ladu, bestBefore as partii from  dbo.EPASAULE_INVENTORIES_detail  \r\n" + 
             		"             		group by productBackofficeId,regionBackofficeId,bestBefore order by productBackofficeId, case when regionBackofficeId = '303_SHOP' then '03A' else regionBackofficeId end desc,bestBefore";
        

             usedProds = new HashSet();
    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap<String, Double> stocks = new TreeMap<String, Double>();
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
          
          if(stocks.get(id) != null) {
        	  sto = stocks.get(id) + sto; //summeerime laoseisu 
          }
         // stocks.put(id, sto);
          }
       
    	rs1.close();
    	

    	
     ResultSet  rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      
      while(rs.next()){
        String sorting = rs.getString("sorting");
        while(sorting.length() < 7) {
        	sorting = "0" + sorting;
        }
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst((rs.getString("nimetus").trim())) ;//+ " " + kood;
        String nimibr = Converter.enCodeEst((rs.getString("namebr").trim()));
       // teeme nime puhastuse kui on topelt brand mini
        boolean cut = false;
       if(nimetus.endsWith(nimibr)) {
    	   String uu = nimetus.substring(0,nimetus.lastIndexOf(nimibr)-2);
           String nn = nimibr.toLowerCase();
    	   if(uu.toLowerCase().contains(nn)) {
    		   cut = true;
    	   }
       }

       if(cut) {
    	   nimetus = nimetus.substring(0,nimetus.lastIndexOf(nimibr)-2);
       }
       
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
         if(grupp!=null && grupp.trim().length()>0){
           prod.setAgentGroup(grupp.trim());
           if(grupp.trim().equals(noOPT)) {
          	 noopt = true;
           }
         }

   
        prod.setName(nimetus.trim() + " " + kood);
        

       String grupp2 = rs.getString("grupp2");
            if(grupp2 != null && grupp2.trim().length()>0){ 
                prod.setGroup(grupp2);
                if(grupp2.trim().equals(noOPT)) {
               	 noopt = true;
                }
             }
            
            
            
            if(!noopt) {
                   prod.setOptAmountInUnits(rs.getDouble("opt"));
                   prod.setMinAmountInUnits(rs.getDouble("mini"));
            }
            String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                   prod.setUnit(unit);
               }
       
       if(nimetus.startsWith("L") && nimetus.contains("P - ")) {
    	   prod.setStatusSellout("1");
       }
            
               Double vat = rs.getDouble("vat");
               prod.setVat(vat + "");
               prod.setNameSorting(sorting + nimetus);
               
       products.put(nimetus + prod.getID(), prod);
       prodHash.put(prod.getID(), prod);
       prodcodes.add(prod.getID());
       usedProds.add(prod.getID());
   
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
  stmt.close();
  con.close();
 
  if(sb.length() > 0){
	   Converter.saveFile("returnreason.txt", sb.toString());
  }
       
  
  if(sb3.length() > 0){
	   Converter.saveFile("repricereason.txt", sb3.toString());
  }
 
    } catch (Exception e){
      getParent().showError(e);
    }

    
    return products;
  }


 

  public Vector getCustomerProductsList(String agentCode){
	  makeCustprod();
    return custProds;
  }

 public double getSalary(String agentCode) {
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


  public String getDriverVersion(){
    return "EugestaEST";
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
	  private Vector prods = new Vector();
      private NumberFormat NF = NumberFormat.getInstance();



	@Override
	public String getStartingTime() {
		// TODO Auto-generated method stub
		return null;
	}


}


