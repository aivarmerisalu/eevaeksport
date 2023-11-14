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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.json.simple.parser.JSONParser;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class HorizonKonigDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker, WebReporter {
  public HorizonKonigDBDriver(EksportPanel parent){
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
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
    nodebt = new Vector();
   String nd = getParent().getProps().getProperty("nodebt.list");
   if(nd != null){
	   StringTokenizer st = new StringTokenizer(nd,",");
	   while(st.hasMoreTokens()){
		   nodebt.add(st.nextToken());
	   }
   }
    
  }

  
  
  
  private TreeMap getMaxDiscs() {
	  TreeMap res = new TreeMap();
	  File fff = new File(this.getExcelDir() + "/maxdiscounts.xlsx" );
      TreeMap groups = new TreeMap(); 
  	try{
  		
  
          FileInputStream ff = new FileInputStream(fff);
  		XSSFWorkbook workbook = new XSSFWorkbook((ff));
  		XSSFSheet sheet = workbook.getSheetAt(0);
  		XSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  		System.out.println("rownum " + row.getLastCellNum());
  		
  		
  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
  			try{
  			//	Loeme sisse rea
  				row = sheet.getRow(a);
  				if (row.getCell((short) 0) != null) {
  				


  	    			   String kood = null;
  	    			  if(row.getCell((short)0) != null){
	    	    			  	try{
	    	    			  	  kood = row.getCell((short)0).getStringCellValue();
	    	    			  	}catch (Exception e1) {
	    	    			  		e1.printStackTrace();
	    	    			  	}
	    	    	    }
  	    			  
  	    			  Double  hind = null;
    	    			if(row.getCell((short)3) != null){
    	    				
    	    				try{
    	  	    			  	  hind = row.getCell((short)3).getNumericCellValue();
    	  	    			  	}catch (Exception e1) {
    	  	    			  	
    	  	    			  	try{
    	    	    			  	  hind = Double.parseDouble(row.getCell((short)4).getStringCellValue());
    	    	    			  	}catch (Exception e2) {
    	    	    			  		e2.printStackTrace();
    	    	    			  	}
    	  	    			  	}
  	    			  	
  	    	    }
  	    			  
  	    			  Double prots = null;
  	    			if(row.getCell((short)4) != null){
	    			  	try{
	    			  	  prots = row.getCell((short)4).getNumericCellValue();
	    			  	}catch (Exception e1) {
	    			  		e1.printStackTrace();
	    			  	}
	    	    }
  	    			
  	    		//java.util.Date d1= getDateYYYY(row.getCell(5).getStringCellValue());
  	    		//java.util.Date d2= getDateYYYY(row.getCell(6).getStringCellValue());
  	    		
  	    			java.util.Date d1= null;
  	    			try {
  	    				d1 = (row.getCell(5).getDateCellValue());
  	    			}catch(Exception e) {
  	    				try {
  	    					d1= getDateYYYY(row.getCell(5).getStringCellValue());	
  	    				}catch(Exception r) {
  	    				}
  	    			}
  	    			
  	    			
  	    		java.util.Date d2= null;
  	    		try {
	    				d2 = (row.getCell(6).getDateCellValue());
	    			}catch(Exception e) {
	    				try {
	    					d2= getDateYYYY(row.getCell(6).getStringCellValue());	
	    				}catch(Exception r) {
	    				}
	    			}
  	    		
  	    		
  	    	//	System.out.println(kood + " " + hind + " " + prots + " " + d1 + " " + d2  +
  	    		//		" " + d1.before(new java.util.Date()) + " " + d2.after(new java.util.Date()));	
  	    		if(d1.before(new java.util.Date()) && d2.after(new java.util.Date()) )	{
  	    			//System.out.println(kood + " " + hind + " " + prots + " " + d1 + " " + d2 );	
  	    			if (hind > 0) {
  	    				res.put(kood, hind);
  	    			}
  	    			if (prots > 0) {
  	    				res.put(kood+"%", prots);
  	    			}
  	    			
  	    		}
  	    			
  				}
  			}catch (Exception e2) {
  		  		e2.printStackTrace();
  		  	}
  		}
  		
	}catch (Exception e) {
  		e.printStackTrace();
  	}
  	
  //	System.out.println(res.toString());

	  return res;
  }
  
  
	private Date getDateYYYY(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0,4 ));
		int MONTH = Integer.parseInt(kuup.substring(6, 7))-1;
		int DAY = Integer.parseInt(kuup.substring(8, 10));
		//System.out.println( YEAR + " " + MONTH + " " + DAY);
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}
   
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  Vector nodebt = null;
  HashSet usedProds = null;
  public void connectionUp(){
  }
  
  
		public Vector getLastThreeData() {

			Vector result = new Vector();
			try {
				   
	
	  
		        Connection con3 = getNewConnection();
				
				
				

						String sq3 = "select d.PK_KLIENTS AS klient  ,dat_dok AS kuup ,n.kods as toode,sum(r.DAUDZ) as kogus, max(r.CENA)  as hind, max(d.summa) as summa  from  APV04761.NDOK r INNER JOIN APV04761.DPIRMDOK D ON r.PK_DOK=D.PK_DOK \r\n" + 
								"INNER JOIN APV04761.DKLIENTS K ON k.PK_KLIENTS= d.PK_KLIENTS LEFT OUTER JOIN APV04761.NNOM N ON r.PK_NOM=N.PK_NOM \r\n" + 
								" WHERE D.PAMV_ID = 2711  and DOK_NR not like 'del%' and d.summa > 0 and DOK_NR like 'KD%'\r\n" + 
								"AND dat_dok >=\r\n" + 
								"(select top 1 kuupaev  from ( 	  SELECT top 5 jou2.dat_dok as kuupaev \r\n" + 
								"		FROM  APV04761.DPIRMDOK jou2  where ltrim(jou2.PK_KLIENTS) = ltrim(d.PK_KLIENTS)  \r\n" + 
								"			 and jou2.dat_dok > getdate()-180  and PAMV_ID = 2711  and DOK_NR not like 'del%' and summa > 0 and DOK_NR like 'KD%' group by jou2.PK_KLIENTS , jou2.dat_dok order by jou2.dat_dok desc) as dd order by kuupaev)\r\n" + 
								"		 GROUP BY d.PK_KLIENTS,dat_dok , n.kods ORDER BY d.PK_KLIENTS,dat_dok desc , n.kods" ;
						
					 
						Statement st3 = con3.createStatement();
						ResultSet rs3 = st3.executeQuery(sq3);
						while (rs3.next()) {
							MatrixLine ml = new MatrixLine();
							ml.setCustID(rs3.getString("klient").trim());
							String kuup = rs3.getString("kuup");
							ml.setStartDate(kuup.substring(5,10));
							String toode = rs3.getString("toode");
							if(toode != null) {
								ml.setProdID(toode.trim());
								ml.setPrice(rs3.getDouble("kogus"));
								ml.discount = rs3.getDouble("hind");
								double summa = rs3.getDouble("summa");
								ml.setLineinfo(kuup.substring(0,10)+ " ("+ (int)summa + "€)" );
							    
								result.add(ml);
							}
							
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
      Vector lines = new Vector();
      return lines;
}

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  
  private String turn(String aa) {
	   String res = "";
	   res = aa.substring(8, 10) + "-" + aa.substring(5, 7) + "-" + aa.substring(0, 4);
	   return res;
  }

  private StringBuffer oorders = null;
  
  private TreeMap  getOpenOrders(){
		TreeMap res = new TreeMap();
		oorders = new StringBuffer();
		oorders.append(Calendar.getInstance().getTimeInMillis() + "\n");
		

		
		   String sql2 =  " select  d.PK_DOK AS id, '' as tyyp, d.PK_KLIENTS AS kkood ,  DOK_NR as number ,dat_dok AS kuup , summa as summa from APV04761.DPIRMDOK D INNER JOIN APV04761.DKLIENTS K ON k.PK_KLIENTS= d.PK_KLIENTS and  k.PIEZIMES like '% HRC %' \r\n" + 
		   		"	   WHERE D.DAT_DOK > getdate() AND D.PAMV_ID = 2711  and DOK_NR not like 'del%' and summa > 0 " ;
		
	
	 this.connectionUp();
	try {
	   nf.setMinimumFractionDigits(2);
	   Connection con = this.getNewConnection();
	   PreparedStatement stmt2 = con.prepareStatement(sql2);
	   ResultSet  rs = stmt2.executeQuery();
	   StringBuffer sb = new StringBuffer();

	while(rs.next()){
		
		String id = rs.getString("id");
		String text = rs.getString("tyyp").trim();
		String nimi = rs.getString("kkood");
	    
		if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim() + "  ");
	    String kuup =(rs.getString("kuup").trim().substring(0,10)) ;
		sb.append(turn(kuup) + " " + text + " ");
		
  	 sb.append("|" );	
		
  	 String summa = nf.format(rs.getDouble("summa"));
  	 
  	oorders.append(nimi + ";" + summa + ";" + kuup + "\n" );
	    String sql3 = "   select r.DAUDZ as amount,  'gab' as unit  , n.nosauk as name \r\n" + 
	    		"				    FROM   APV04761.NDOK r inner JOIN  APV04761.DPIRMDOK D  on r.PK_DOK=D.PK_DOK \r\n" + 
	    		"					 LEFT OUTER JOIN APV04761.NNOM N ON r.PK_NOM=N.PK_NOM AND r.RN_VEIDS IN (0,3,5)\r\n" + 
	    		"					 where  d.PK_DOK =  " + id + "";
	 //   System.out.println("sql 3 " + sql3 );
	    Connection cc = this.getNewConnection();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
	
		/*   CREATE TABLE [dbo].[OpenOrders](
	       [klkood] [nvarchar](20) NULL,
	       [kuupaev] [datetime] NULL, 
	       [tellimusenr] [nvarchar](50) NULL,
	       [toode] [nvarchar](50) NULL,
	       [kogus] [decimal](38, 20) NULL,
	       [price]  [decimal](38, 20) NULL)
*/

	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	try {
		    	  	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
		    			       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").toLowerCase().replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    			 	
		    	}catch(Exception e) {
		    		
		    	}
		     }
	   
		    rs2.close();
	        stmt3.close();
	        cc.close();
	        
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	     } else {
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }
	   	sb = new StringBuffer();
	       
	}
	
	
	rs.close();
	stmt2.close();
	con.close(); 	 
	
	} catch (Exception e) {
	  getParent().showError(e);
	}

  	connectionDown();
  	Converter.saveFile(parent.getProps().getProperty("savefilesdir")  + "customerorders.txt", oorders.toString());
		
		return res;
	}
	
  
  
  public Vector getPriceMatrixData() {
	  Vector lines = new Vector();
	  
	String sql = " SELECT  N.KODS as toode,[CENA] as hind ,[NO_DAT] as algus ,[LIDZ_DAT] as lopp , " +
   " h. [PK_KLIENTS] as klient,[PK_GRUPA] as clgrupp,datediff(day,[NO_DAT] , [LIDZ_DAT]) as paevi " +
    " FROM [ftgwin7].[APV04761].[NCEN]  h inner join  APV04761.NNOM n on n.PK_NOM= h.PK_NOM where GETDATE()"
    + " between NO_DAT and LIDZ_DAT and year(LIDZ_DAT) != 3000";
	  Statement st = null;
      try {
    	  
        connectionUp();
        Connection       con = getNewConnection();
        
        st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String prodID = rs.getString("toode").trim();
          double price = rs.getDouble("hind");
          String algus = rs.getString("algus");
          String lopp = rs.getString("lopp");
        
          String custID = rs.getString("klient");//.trim();
          String clgr = rs.getString("clgrupp");//.trim();
          int paevi = rs.getInt("paevi");
          
          
          MatrixLine mline = new MatrixLine();

     
			
          if (custID != null && custID.trim().length() > 0 && !custID.trim().equals("1")) {
            mline.setCustID(custID.trim());
          }else if ( clgr != null && clgr.trim().length() > 0) {
                mline.setCliGroup(clgr.trim());
          }

          if (prodID != null && prodID.trim().length() > 0) {
            mline.setProdID(prodID.trim());
          }
      
          if(paevi < 125){
	
          if (lopp != null) {
     				mline.setEndDate(lopp.substring(0, 10));
     			}
     	  if (algus != null) {
     				mline.setStartDate(algus.substring(0, 10));
     			}
     	  
     	  }
     	  
     	  
          mline.setPrice(price);

          if(mline.getCustID() != null || mline.getCliGroup() != null){
              lines.add(mline);
          }
     	
        }

        
        
        
        
  	  // võtame välja need mis on aktiivsed - kui on Klient ja kaup konkreetselt 
        String sql2 = "   SELECT  N.KODS AS prodid , '' as custid ,  CENA.PK_CENVEID as clgroup, " +
  "    0 AS discount  ,  CENA.CENA AS price , CENA.NO_DAT as algus , CENA.LIDZ_DAT  as lopp , " +
	"	 datediff( day,CENA.NO_DAT , CENA.LIDZ_DAT)  as paevi  " +
  "		  FROM   APV04761.NCEN CENA, APV04761.NNOM N, APV04761.NCENVEID CV, APV05308.SVALUTAS VAL  WHERE " +
  "		  CENA.PK_NOM=N.PK_NOM AND CENA.PK_CENVEID=CV.PK_CENVEID AND  " +
  "		  CENA.PK_VAL=VAL.PK_VAL AND N.STATUSS = 0 AND CV.STATUSS = 1 AND " + 
  "		 CENA.NO_DAT <= getdate()+1  AND  " +
  "		  CENA.LIDZ_DAT >= getdate()-1   AND CV.TIPS IN (0)  and VAL.KODS = 'EUR' " +
	"		   and  CENA.PK_CENVEID != 117 order by  N.KODS";

       
   
                   st = con.createStatement();
                   rs = st.executeQuery(sql2);


              	                  while (rs.next()) {
              	                	  String prodID = rs.getString("prodid");
              	                          String clgroup = rs.getString("clgroup");
              	                          double price = rs.getDouble("price");
              	                      
              	                        //  String algus = rs.getString("algus");
              	                        //  String lopp = rs.getString("lopp");
              	                        //  int paevi = rs.getInt("paevi");
              	                          MatrixLine mline = new MatrixLine();
              	                          
              	                    //KLIENDIGRUPP JA TOOTEGRUPP
              	                          if (clgroup != null && clgroup.trim().length() > 0) {
              	                                  mline.setCliGroup(clgroup.trim());
              	                          }
              	                       
              	                          if (prodID != null && prodID.trim().length() > 0) {
              	                                  mline.setProdID(prodID.trim());
              	                          }
              	                     

              	                          if (price != 0) {
              	                                  mline.setPrice(price);
              	                          }
              	                          
              	                         
              	                          
              	                          
              	                           if(mline.getPrice() > 0){
              	                        	   lines.add(mline);
              	                           }
              	                         
              	                  }


        
        
        rs.close();
        st.close();
        this.connectionDown();
      }
      catch (SQLException ex) {
      }
  System.out.println("Pmatrix "+ lines.size());	
	
	  return lines ;

  }
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  
  public Vector getPriceMatrixDataold() {
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  Vector lines = new Vector();
      
	  // võtame välja need mis on aktiivsed - kui on Klient ja kaup konkreetselt 
          String sql = "   SELECT  N.KODS AS prodid , '' as custid ,  CENA.PK_CENVEID as clgroup, " +
    "    0 AS discount  ,  CENA.CENA AS price , CENA.NO_DAT as algus , CENA.LIDZ_DAT  as lopp , " +
	"	 datediff( day,CENA.NO_DAT , CENA.LIDZ_DAT)  as paevi  " +
    "		  FROM   APV04761.NCEN CENA, APV04761.NNOM N, APV04761.NCENVEID CV, APV05308.SVALUTAS VAL  WHERE " +
    "		  CENA.PK_NOM=N.PK_NOM AND CENA.PK_CENVEID=CV.PK_CENVEID AND  " +
    "		  CENA.PK_VAL=VAL.PK_VAL AND N.STATUSS = 0 AND CV.STATUSS = 1 AND " + 
    "		 CENA.NO_DAT <= getdate()+1  AND  " +
    "		  CENA.LIDZ_DAT >= getdate()-1   AND CV.TIPS IN (0)  and VAL.KODS = 'EUR' " +
	"		   and  CENA.PK_CENVEID != 117 order by  N.KODS";

try{        
      Connection       con = getNewConnection();
           Statement stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);


                	                  while (rs.next()) {
                	                	  String prodID = rs.getString("prodid");
                	                          String clgroup = rs.getString("clgroup");
                	                          double price = rs.getDouble("price");
                	                      
                	                        //  String algus = rs.getString("algus");
                	                        //  String lopp = rs.getString("lopp");
                	                        //  int paevi = rs.getInt("paevi");
                	                          MatrixLine mline = new MatrixLine();
                	                          
                	                    //KLIENDIGRUPP JA TOOTEGRUPP
                	                          if (clgroup != null && clgroup.trim().length() > 0) {
                	                                  mline.setCliGroup(clgroup.trim());
                	                          }
                	                       
                	                          if (prodID != null && prodID.trim().length() > 0) {
                	                                  mline.setProdID(prodID.trim());
                	                          }
                	                     

                	                          if (price != 0) {
                	                                  mline.setPrice(price);
                	                          }
                	                          
                	                         
                	                          
                	                          
                	                           if(mline.getPrice() > 0){
                	                        	   lines.add(mline);
                	                           }
                	                         
                	                  }

                                    rs.close();                  
                  stmt.close();
                  con.close();

          } catch (SQLException ex) {
          ex.printStackTrace();
          }
	  
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
	  
	   getOpenOrders();
	 
	   kogused = new TreeMap();
	   

		  
	   String    cmd = " SELECT toode, sum(kogus) as kogus, ladu FROM\r\n" + 
	   		"(select toode , kogus , \r\n" + 
	   		"case  when ladu = 81209 then 53296  \r\n" + 
	   		"else(case  when ladu = 81208 then 53269  \r\n" + 
	   		"else (case  when ladu = 53277 then 53268  \r\n" + 
	   		"else  (case  when ladu = 81161 then 53268  \r\n" + 
	   		"else (case  when ladu = 81734 then 53269  \r\n" + 
	   		"else (case  when ladu = 81735 then 53270  \r\n" + 
	   		"else (case when ladu = 79742 then 53268 \r\n" + 
	   		"else (case  when ladu = 79743 then 53269 \r\n" + 
	   		"else (case  when ladu = 79744 then 53270 \r\n" + 
	   		"else (case  when ladu = 83018 then 53268 \r\n" + 
	   		"else ladu end) end) end) end) end) end)end) end)end )end AS ladu\r\n" + 
	   		"from\r\n" + 
	   		"(SELECT TOP (30001)   N.KODS AS toode,  BATL.DAUDZ AS kogus,  PCN.PK_KLIENTS as ladu \r\n" + 
	   		"FROM  APV04761.NNOM N \r\n" + 
	   		"LEFT OUTER JOIN APV04761.NVIEN VIEN ON N.PK_VIEN=VIEN.PK_VIEN\r\n" + 
	   		"LEFT OUTER JOIN \r\n" + 
	   		"(SELECT NPBATL.PK_NOM AS PK_NOM,SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ END) AS DAUDZ,\r\n" + 
	   		"SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ2 END) AS DAUDZP, \r\n" + 
	   		"SUM(CASE NPP.STATUSS WHEN 2 THEN NPBATL.DAUDZ ELSE 0 END) AS DAUDZBR,\r\n" + 
	   		"SUM(NPBATL.SUMMA) AS SUMMA\r\n" + 
	   		"FROM APV04761.NPBATL, APV04761.NNOMPART NPP \r\n" + 
	   		"WHERE  NPP.PK_NOMPART = NPBATL.PK_NOMPART \r\n" + 
	   		"GROUP BY NPBATL.PK_NOM) KBATL ON N.PK_NOM = KBATL.PK_NOM \r\n" + 
	   		"LEFT OUTER JOIN APV04761.NCEN_LAST PCN ON N.PK_NOM = PCN.PK_NOM AND NOT (PCN.PK_KLIENTS = 2)\r\n" + 
	   		"LEFT OUTER JOIN (SELECT NPBATL.PK_NOM AS PK_NOM, NPBATL.PK_NOL AS PK_NOL,SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ END) AS DAUDZ,\r\n" + 
	   		"SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ2 END) AS DAUDZP,\r\n" + 
	   		"SUM(CASE NPP.STATUSS WHEN 2 THEN NPBATL.DAUDZ ELSE 0 END) AS DAUDZBR,\r\n" + 
	   		"SUM(NPBATL.SUMMA) AS SUMMA \r\n" + 
	   		"FROM APV04761.NPBATL, APV04761.NNOMPART NPP \r\n" + 
	   		"WHERE  NPP.PK_NOMPART = NPBATL.PK_NOMPART \r\n" + 
	   		"GROUP BY NPBATL.PK_NOM, NPBATL.PK_NOL) BATL ON PCN.PK_NOM = BATL.PK_NOM AND PCN.PK_KLIENTS = BATL.PK_NOL\r\n" + 
	   		"WHERE (N.PAMVID IN (2015,7713,24522,18205, 12200 ,12201,13204,13205,15139)) AND ((  ( (N.STATUSS = 0) )  ))) as vol\r\n" + 
	   		"where kogus >0 and ladu not in ('53273') \r\n" + 
	   		") AS toodegroupingneeded\r\n" + 
	   		"GROUP BY toode, ladu\r\n" + 
	   		"" ;
		  

	       if (isDebug()) System.out.println(cmd); 
	       StringBuffer products = new StringBuffer();
	       try {
             
	    	 Connection  con = getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);

	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("kogus");
	           String dealer = rs.getString("ladu");
	           if(seis > 0){
	               products.append(kood +  ";");
	               products.append(" ;");
	               products.append(getParent().getNF().format(seis) + ";" + dealer  +  "\n"); // + dealer +  "\n");
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

  
  
  public TreeMap getDealers() {

      TreeMap dealers = new TreeMap();
      try {

    	  String cmd = "select pk_klients as ladu  , nosauk as name from APV04761.DKLIENTS where kods in ('N01','N02','N03','N05','N04')";

           if (isDebug()) System.out.println(cmd);
           Connection con = getNewConnection();
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(cmd);
                while (rst.next()) {
                  String kood = rst.getString("ladu").trim();
                   Dealer dealer = new Dealer(kood);
                   String name = rst.getString("name").trim();
                   if (name.length() > 10){
                	//   name = name.substring(0, 10);
                   }
                   dealer.setName(Converter.enCodeEst(name));
                   dealers.put(dealer.getID(), dealer);
                  }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          e.printStackTrace();
      }

      
    //  System.out.println("Dealers size :" + dealers.size());
      
	  return dealers;
	    }
  

  public DealerProduct getDealerProductsList() {
	  DealerProduct dealerProds = new DealerProduct();

  try {
	  


	  String    cmd = "SELECT toode, sum(kogus) as kogus, ladu FROM\r\n" + 
	  		"(select toode , kogus , \r\n" + 
	  		"case  when ladu = 81209 then 53296  \r\n" + 
	  		"else(case  when ladu = 81208 then 53269  \r\n" + 
	  		"else (case  when ladu = 53277 then 53268  \r\n" + 
	  		"else  (case  when ladu = 81161 then 53268  \r\n" + 
	  		"else (case  when ladu = 81734 then 53269  \r\n" + 
	  		"else (case  when ladu = 81735 then 53270  \r\n" + 
	  		"else (case when ladu = 79742 then 53268 \r\n" + 
	  		"else (case  when ladu = 79743 then 53269 \r\n" + 
	  		"else (case  when ladu = 79744 then 53270 \r\n" + 
	  		"else (case  when ladu = 83018 then 53268 \r\n" + 
	  		"else ladu end) end) end) end) end) end)end) end)end )end AS ladu\r\n" + 
	  		"from\r\n" + 
	  		"(SELECT TOP (30001)   N.KODS AS toode,  BATL.DAUDZ AS kogus,  PCN.PK_KLIENTS as ladu \r\n" + 
	  		"FROM  APV04761.NNOM N \r\n" + 
	  		"LEFT OUTER JOIN APV04761.NVIEN VIEN ON N.PK_VIEN=VIEN.PK_VIEN\r\n" + 
	  		"LEFT OUTER JOIN \r\n" + 
	  		"(SELECT NPBATL.PK_NOM AS PK_NOM,SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ END) AS DAUDZ,\r\n" + 
	  		"SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ2 END) AS DAUDZP, \r\n" + 
	  		"SUM(CASE NPP.STATUSS WHEN 2 THEN NPBATL.DAUDZ ELSE 0 END) AS DAUDZBR,\r\n" + 
	  		"SUM(NPBATL.SUMMA) AS SUMMA\r\n" + 
	  		"FROM APV04761.NPBATL, APV04761.NNOMPART NPP \r\n" + 
	  		"WHERE  NPP.PK_NOMPART = NPBATL.PK_NOMPART \r\n" + 
	  		"GROUP BY NPBATL.PK_NOM) KBATL ON N.PK_NOM = KBATL.PK_NOM \r\n" + 
	  		"LEFT OUTER JOIN APV04761.NCEN_LAST PCN ON N.PK_NOM = PCN.PK_NOM AND NOT (PCN.PK_KLIENTS = 2)\r\n" + 
	  		"LEFT OUTER JOIN (SELECT NPBATL.PK_NOM AS PK_NOM, NPBATL.PK_NOL AS PK_NOL,SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ END) AS DAUDZ,\r\n" + 
	  		"SUM(CASE NPP.STATUSS WHEN 2 THEN 0 ELSE NPBATL.DAUDZ2 END) AS DAUDZP,\r\n" + 
	  		"SUM(CASE NPP.STATUSS WHEN 2 THEN NPBATL.DAUDZ ELSE 0 END) AS DAUDZBR,\r\n" + 
	  		"SUM(NPBATL.SUMMA) AS SUMMA \r\n" + 
	  		"FROM APV04761.NPBATL, APV04761.NNOMPART NPP \r\n" + 
	  		"WHERE  NPP.PK_NOMPART = NPBATL.PK_NOMPART \r\n" + 
	  		"GROUP BY NPBATL.PK_NOM, NPBATL.PK_NOL) BATL ON PCN.PK_NOM = BATL.PK_NOM AND PCN.PK_KLIENTS = BATL.PK_NOL\r\n" + 
	  		"WHERE (N.PAMVID IN (2015,7713,24522,18205, 12200 ,12201,13204,13205,15139)) AND ((  ( (N.STATUSS = 0) )  ))) as vol\r\n" + 
	  		"where kogus >0 and ladu not in ('53273') \r\n" + 
	  		") AS toodegroupingneeded\r\n" + 
	  		"GROUP BY toode, ladu\r\n"  
	  		 ;
	  
	  

//		 53268	RĪGAS NOLIKTAVA (Galvena)	N01
//	53269	Daugavpils noliktava	N02
//	53270	Liepājas noliktava	N03
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
           	   double seis = rst.getDouble("kogus");
           	       String ladu  =  rst.getString("ladu").trim();
           	       
              	dealerProds.add(ladu, toode,((int)seis)+""," "," " );
//        	  System.out.println("toode " +  toode + " " + ladu + " " + seis);  
              }
           
               }
              }
          rst.close();
          stm.close();
          con.close();

  } catch (Exception e) {
      e.printStackTrace();
  }

  
  //  System.out.println( " dealerProds suurus " + dealerProds);
   return dealerProds;
 
 }


	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
  
  
  public TreeMap getAgents(){
	  
	  
    String cmd = "   SELECT M.PK_PARD AS agent, M.NOSAUK AS nimi,piezime as info   FROM  APV04761.NPARD M  WHERE  " +
    		" M.PAMVID IN (2110) AND M.STATUSS = 1 ";
   // if (isDebug()) System.out.println(cmd);
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
          String info = rs.getString("info");
          if(info != null && info.contains("#")){
        	  agent.setObject(info.substring(info.indexOf("#")+1, info.indexOf("#")+4));
        	  
          }
          
          
          agent.setName(name + " " + agent.getID());
          //System.out.println(name + " " + agent.getID() + " " + agent.getObject());
          agent.setIsPilot(true);
          
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
          agents.put(name+ " " + agent.getID(), agent);
        }
        rs.close();
        stmt.close();
        con.close();
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

  //  this.setBulkSales(agents1);
    return agents;
  }
  
  private String getLongs (String days) {
	  if(days != null) {
		  
	  
	  Calendar date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String mon1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String mon2 = date1.getTimeInMillis() +"" ;
	  
	  date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String tue1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String tue2 = date1.getTimeInMillis() +"" ;
	  
	  
	  date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String wed1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String wed2 = date1.getTimeInMillis() +"" ;
	  
	  date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String thu1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String thu2 = date1.getTimeInMillis() +"" ;
	  
	  
	  
	  date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String fri1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String fri2 = date1.getTimeInMillis() +"" ;
	  
	  date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String sat1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String sat2 = date1.getTimeInMillis() +"" ;
	  
	  
	  date1 = Calendar.getInstance();
	   date1.add(Calendar.DATE, 1);
	  while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
	      date1.add(Calendar.DATE, 1);
	  }
	  String sun1 = date1.getTimeInMillis() +"" ;
	  date1.add(Calendar.DATE, 7);
	  String sun2 = date1.getTimeInMillis() +"" ;
	  
	  
	  String res = "";
	  StringTokenizer st = new StringTokenizer (days,",");
	  while (st.hasMoreTokens()) {
		  String day = st.nextToken();
		  if(day.equals("1")) {
			  res = res +","+ mon1 ;
		  }
		  if(day.equals("2")) {
			  res = res +","+ tue1 ;
		  }
		  if(day.equals("3")) {
			  res = res +","+ wed1 ;
		  }
		  if(day.equals("4")) {
			  res = res +","+ thu1 ;
		  }
		  if(day.equals("5")) {
			  res = res +","+ fri1 ;
		  }
		  if(day.equals("6")) {
			  res = res +","+ sat1 ;
		  }
		  if(day.equals("7")) {
			  res = res +","+ sun1 ;
		  }
	  }
	  
	  st = new StringTokenizer (days,",");
	  while (st.hasMoreTokens()) {
		  String day = st.nextToken();
		  if(day.equals("1")) {
			  res = res +","+ mon2 ;
		  }
		  if(day.equals("2")) {
			  res = res +","+ tue2 ;
		  }
		  if(day.equals("3")) {
			  res = res +","+ wed2 ;
		  }
		  if(day.equals("4")) {
			  res = res +","+ thu2 ;
		  }
		  if(day.equals("5")) {
			  res = res +","+ fri2 ;
		  }
		  if(day.equals("6")) {
			  res = res +","+ sat2 ;
		  }
		  if(day.equals("7")) {
			  res = res +","+ sun2 ;
		  }
	  }
	  return res.substring(1);
       }else {
    	   return null;  
	  }
	  
	 
  }
  
  
  private TreeMap makeCustomerDeliveryData(){
		TreeMap res = new TreeMap();
		try{

	      
	String sql3 = " select  kilent , paev from kuskilt ";

	Connection con = this.getNewConnection();
		Statement stmt = con.createStatement();

	ResultSet rs3 = stmt.executeQuery(sql3);
	while(rs3.next()){
	String klient = rs3.getString("klient").trim();
	String days = rs3.getString("paev").replace(";", " ").trim();

	String lll =getLongs(days);
	if(lll != null) {
		res.put(klient, lll);
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
  

  public TreeMap getCustomers(Agent agent){
	  TreeMap openOrders = this.getOpenOrders();
 
	//  TreeMap deldays =  makeCustomerDeliveryData();
	  
    String salessql = " SELECT K.PK_KLIENTS as klient ,A.PK_ADRESE as aid, round(SUM(summa_pv),0) AS summa,str(datepart(YYYY,D.DAT_DOK))+' '+str(datepart(mm,D.DAT_DOK)) as kuu " +
		 "  FROM   APV04761.DPIRMDOK D LEFT OUTER JOIN APV04761.DKLIENTS N ON D.PK_ESPATS=N.PK_KLIENTS " +
		 "  LEFT JOIN APV04761.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
		 "  JOIN APV04761.NDOKG G ON D.PK_DOK = G.PK_DOK " +
		 "  LEFT OUTER JOIN (SELECT PK_DOK, MAX(LAIKS) AS LAIKS FROM APV04761.SDOKEVNT GROUP BY PK_DOK) LASTEV ON LASTEV.PK_DOK = D.PK_DOK " +
		 "  LEFT OUTER JOIN APV04761.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE, APV05308.SVALUTAS V " +
		 "   WHERE D.PAMV_ID IN (2710,2720,2711,2721,2712,2786,2713,2737,2718,2728,18212,2745,2782,24501,2797,2798,2739,18086,2736,2783,2799,18091,15008,15011,2738,2792,2795,2742,2793,2796)" +
		"	AND D.PK_VAL=V.PK_VAL AND  D.DAT_DOK > getdate()-120 " +
		"	 AND D.PAMV_ID = 2711 AND  NOT (D.STATUSS = 4)  and K.KODS is not null   " +
		"	 GROUP BY K.PK_KLIENTS,A.PK_ADRESE,str(datepart(YYYY,D.DAT_DOK))+' '+str(datepart(mm,D.DAT_DOK)) " +
		"	  ORDER BY  K.PK_KLIENTS ASC " ;
  	


 
 
 //    tal_visitationdays ,talspartnercode,

 try{
	   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
}catch(Exception e){

}
 
 TreeMap custAgents =  new TreeMap();
 
 
 
 
 
 TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap defDealers = new TreeMap();
    Vector payers =  new Vector();
    
    HashMap<String, Double> overd = new HashMap();
    HashMap<String, Double> alld = new HashMap();
    HashMap salesres = new HashMap();
    	ResultSet rs1 ;
    	Statement stmr ;
    	
    	
	
    	
    	try{
    		
    		String custAgs = " SELECT K.PK_KLIENTS AS klient , A.PK_PARD as agent " + 
 "  FROM   APV04761.DKLIENTS K LEFT OUTER JOIN  APV04761.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS and a.LEG_ADR = 2 " +
 "	 LEFT OUTER JOIN APV04761.DGRUPAS GRU ON K.PK_GRUPA = GRU.PK_GRUPA  " +
 "    WHERE (K.STATUSS <> 2 AND K.PK_DOKT IS NOT NULL AND ( NOT K.PAMV_ID IN (1810,11970))) AND " +
 "   (( ( (K.TIPS = 3)  OR  (K.TIPS = 4)  OR  (K.TIPS = 6)  OR  (K.TIPS = 8) ) AND ( (K.STATUSS = 0)  OR " +
 "   (K.STATUSS = 1) ) AND ( (K.STATUSS = 0) ) AND ( (K.TIPS <> 8) ) AND ( (K.DBKRPAZ = 1) ) ))  and K.KODS is not null  " +
 "	  and A.PK_PARD is not null " +
 "    ORDER BY   K.NOSAUK ASC ";
    		
        	Connection con = this.getNewConnection(); 
        		 stmr = con.createStatement();
            	
                 rs1 = stmr.executeQuery(custAgs);
                
                 while(rs1.next()){
                	String custID = rs1.getString("klient").trim();
                	String  group  = rs1.getString("agent");
                	Vector ags = new Vector();
						if(custAgents.get(custID) != null){
							ags = (Vector)custAgents.get(custID);
						}
						ags.add(group);
						custAgents.put(custID,ags);
                    }
						
               rs1.close();
               stmr.close();
               con.close();
        	
                 }catch(Exception s){
        		s.printStackTrace();
        	}
    		
    	
    	
    	
    	
try{
	
	 
	 String sqldea = "	  select  s.PK_KLIENTS as klient ,"
	 		+ " PK_KLGR as dealer from APV04761.DKLSAGR s  where PK_KLGR in ('267','268','269')";
    		
        	Connection con = this.getNewConnection(); 
        		 stmr = con.createStatement();
            	
                 rs1 = stmr.executeQuery(sqldea);
                
                 while(rs1.next()){
                	String klient = rs1.getString("klient").trim();
                	String dealer = rs1.getString("dealer").trim();
                	if(dealer.equals("267")){
                		dealer = "53269";
                	}
                	if(dealer.equals("268")){
                		dealer = "53270";
                	}
                	if(dealer.equals("269")){
                		dealer = "53268";
                	}
                	

                //	53268	RĪGAS NOLIKTAVA (Galvena)	N01
                //	53269	Daugavpils noliktava	N02
                //	53270	Liepājas noliktava	N03
                //	53271	Atdzesetas GAĻAS noliktava BAJARDS	N04
                //	53272	Atdzesetas ZIVIS noliktava ROTANS	N05
//               	 267	zKD-D	Daugavpils Noliktava
            		//	 268	zKD-L	Liepaja Noliktava
            		//	 269	zKD-R	Riga Noliktava
                	
                	
                	defDealers.put(klient, dealer);
                }
                
               rs1.close();
               stmr.close();
               con.close();
        	}catch(Exception s){
        		s.printStackTrace();
        	}

    //	System.out.println("defDealers " + defDealers.size());


    	 String sqldebt = "	  select K.REG_NR AS klient,sum(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata FROM   APV04761.DPIRMDOK  d " +
    	   " LEFT JOIN APV04761.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0 AND d.APM_TERM < getdate()+1 " +
    		" group by K.REG_NR ";
    	 
    	 String sqldebtall = "	  select K.REG_NR AS klient,sum(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata FROM   APV04761.DPIRMDOK  d " +
    			   " LEFT JOIN APV04761.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0  " +
    				" group by K.REG_NR ";
    	
    	try{
    		
        	Connection con = this.getNewConnection(); 
        		 stmr = con.createStatement();
            	
                 rs1 = stmr.executeQuery(sqldebt);
                
                 while(rs1.next()){
                	String klient = rs1.getString("klient");
                	if(klient != null) {
                		klient = klient.trim();
                    	double summa = rs1.getDouble("tasumata");
                    	overd.put(klient, summa);
                	}
                		
                }
                
               rs1.close();
               stmr.close();
               con.close();
        	}catch(Exception s){
        		s.printStackTrace();
        	}
    	
    	
    	try{
    		
        	Connection con = this.getNewConnection(); 
        		 stmr = con.createStatement();
            	
                 rs1 = stmr.executeQuery(sqldebtall);
                
                 while(rs1.next()){
                	 String klient = rs1.getString("klient");
                 	if(klient != null) {
                 		klient = klient.trim();
                     	double summa = rs1.getDouble("tasumata");
                     	alld.put(klient, summa);
                 	}
                }
                
               rs1.close();
               stmr.close();
               con.close();
        	}catch(Exception s){
        		s.printStackTrace();
        	}
    		
    	
    	try{
    		
    	Connection con = this.getNewConnection(); 
    		 stmr = con.createStatement();
        	
             rs1 = stmr.executeQuery(salessql);
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim() ;
            	String abi =  rs1.getString("aid");
            	
            	if(abi != null){
                    abi = 	"_" + abi.trim();
                    }else{
                    	abi = "";
                    }
            	
            	klient = klient + abi;
            	int summa = (int)rs1.getDouble("summa");
            	
            	String aeg = rs1.getString("kuu").trim();
            	String kuu = aeg.substring(0, 4).trim() ;
            	if(kuu.length() == 1){
            		kuu = "0" + kuu;
            	}
            	String aasta = aeg.substring(4).trim();
            	if(aasta.length() == 1){
            		aasta = "0" + aasta;
            	}
              if(salesres.get(klient) == null){
            	  salesres.put(klient,"   " + aasta + " : " + summa + " €" );
              }else{
            	  String asi = (String)salesres.get(klient);     	  
            	  salesres.put(klient,"   " + aasta + " : " + summa +" €|" + asi);
              }
            }
           rs1.close();
           stmr.close();
           con.close();
    	}catch(Exception s){
    		s.printStackTrace();
    	}
    
    
    
    
    try {

    	
    	 

    	String cmd = "SELECT K.NOSAUK AS nimi, K.PK_KLIENTS AS id ,A.PK_ADRESE AS aid , K.PK_KLIENTS AS kood,K.REG_NR as payer, a.KONTAKTP  as tarne,K.ADRESE as address ,A.ADRESE as address2 ,k.PIEZIMES as address3, K.PK_PARD as agent , " +
    " K.APMTERM as paym ,K.KREDLIM as credit, K.LIC_NR as licence ,K.LIC_IZDAT as lepalg , " +
    " K.TELEFONS as phone,K.MOBTEL as mphone , (case WHEN C.PK_DKLKPERSGR=7 THEN C.MOBTEL ELSE NULL END) AS orderphone ,K.EPASTS as email ,k.PK_CENVEID as hinnak  ,gru.nosauk as chain ,kkat.nosauk as kate " +
    " , K.PK_GRUPA as alegru  FROM   APV04761.DKLIENTS K LEFT OUTER JOIN  APV04761.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS and a.LEG_ADR = 2 "
    + " LEFT OUTER JOIN APV04761.DGRUPAS GRU ON K.PK_GRUPA = GRU.PK_GRUPA  " +
    " LEFT OUTER JOIN APV04761.DKLKAT KKAT  ON K.PK_KLKAT = KKAT.PK_KLKAT LEFT OUTER JOIN APV04761.DKLKPERSR AS G ON K.PK_KLIENTS = G.PK_KLIENTS " + 
    "LEFT OUTER JOIN APV04761.DKLKPERS AS C ON G.PK_KLKPERS = C.PK_KLKPERS " + 
    " WHERE (K.STATUSS <> 2 AND K.PK_DOKT IS NOT NULL AND ( NOT K.PAMV_ID IN (1810,11970))) AND " +
    " (( ( (K.TIPS = 3)  OR  (K.TIPS = 4)  OR  (K.TIPS = 6)  OR  (K.TIPS = 8) ) AND ( (K.STATUSS = 0)  OR " +
    "  (K.STATUSS = 1) ) AND ( (K.STATUSS = 0) ) AND ( (K.TIPS <> 8) ) AND ( (K.DBKRPAZ = 1) ) ))  and K.KODS is not null   " +
    " ORDER BY   K.NOSAUK ASC " ;
     
      if (isDebug()) System.out.println(cmd);
       
    	
    	
    	Connection con = this.getNewConnection();
    	Statement stmt =con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      
      while(rs.next()){
        String name = (rs.getString("nimi").trim());
        String abi  =  rs.getString("id").trim();
        boolean laks = true;
        String aab = abi.trim() ;
       
        if(abi != null){
        abi = 	"_" + abi.trim();
        }else{
        	abi = "";
        }
        String kood = rs.getString("kood").trim();
        String accountnum = kood ;//+  abi;
        Customer cust = new Customer(accountnum);
        cust.setPayer(kood);
       if(defDealers.get(aab) != null){
        cust.setDealer(new Dealer(""+defDealers.get(aab)));	
        }
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Sales :|Year Month|Sales EUR|" + salesres.get(accountnum) );
        }
        
       
        cust.restName = name;
        
        name = Converter.enCodeEst(name);
       
        cust.setName(name + " " + accountnum);
         
        if(overd.get(kood) != null){
        	cust.setDebt(overd.get(kood));
       // 	System.out.println("oioi" + kood);
        }
        if(alld.get(kood) != null){
        	cust.setUsedCredit(alld.get(kood));
        }
        
        String payer = rs.getString("payer");
        if(payer != null && payer.trim().length() > 0){
        	
        	 if(overd.get(payer.trim()) != null){
             	cust.setDebt(overd.get(payer.trim()));
            // 	System.out.println("oioi" + kood);
             }
             if(alld.get(payer.trim()) != null){
             	cust.setUsedCredit(alld.get(payer.trim()));
             }
        	
        }else{
        	//cust.setPayer(accountnum);
        }
       
 String tarne = rs.getString("tarne");
     

    
        if (tarne != null && tarne.trim().length()>0) {
        	String asi = "";
        	StringTokenizer st = new StringTokenizer(tarne.trim(),",");
        	while(st.hasMoreTokens()) {
        		String a = st.nextToken();
        		if(a.equals("1")) {
        			asi = asi + "Pirm.";  
        		}
        		if(a.equals("2")) {
        			asi = asi + "-Otrd.";  
        		}
        		if(a.equals("3")) {
        			asi = asi + "-Tre\\u0161d.";  
        		}
        		if(a.equals("4")) {
        			asi = asi + "-Ceturt.";  
        		}
        		if(a.equals("5")) {
        			asi = asi + "-Piektd.";  
        		}
        		if(a.equals("6")) {
        			asi = asi + ">-Sestd.";  
        		}
        		if(a.equals("7")) {
        			asi = asi + "-Sv\u0113td.";  
        		}
        	}
        	if(asi.length() > 0) {
        		if(asi.startsWith("-")) {
        			asi = asi.substring(1); // et kriips maha saada 
        		}
        		try {
        			cust.setCity("Piegades dienas :" + asi + "|  nākamā piegāde :" + Converter.getDDpMMpYYYYpString(getNextShipCal(tarne.trim())));
                		
        		}catch(Exception e) {
        			
        		}
        	
        	}
        	
        }
        
    
     //  String aadr = rs.getString("address");
       String aadr = rs.getString("address3");
       
 
        if (aadr != null) {
        	String asi = aadr.trim();
        	if(asi.toLowerCase().indexOf("latvija") != -1){
        		try{
        			asi = asi.substring(0,asi.toLowerCase().indexOf("latvija")-1).trim();
           	}catch (Exception k){
        		}
        	}
        	
        	String addrx = Converter.enCodeEst(asi.replace('\n',' ').replaceAll("\\r\\n|\\r|\\n", " "));
         	 name = name + " " + addrx;
             cust.setAddress(Converter.adressLink(addrx,"Latvia"));
             cust.restStreet = addrx;
             cust.setCountry("Latvia");
        	
        	   int koma = asi.lastIndexOf(",");
               if(koma != -1){
            	   String indeks = asi.substring(koma+1);
                   
                   if (indeks != null && indeks.length() > 0){
                    // cust.restZipCode = indeks.trim();
                   }
                   asi = asi.substring(0, koma);
                   koma = asi.indexOf(",");
                   if(koma != -1){

                   	String tanav = asi.substring(0,koma);
                       if (tanav != null && tanav.length() > 0){
                        // cust.restStreet = tanav.trim();
                       }    	   
                       asi = asi.substring(koma+1);
                       
                     try{
                    	 String linn = asi.substring(0 ,asi.length()-1);
                         if (linn != null && linn.length() > 0){
                          // cust.restCity = linn.trim();
                         } 
                     }catch(Exception k){
                    	 
                     }
                   }
               }
        }
        	

        name = name + " " + accountnum;
        cust.setName(name);
    
       if(custAgents.get(aab) != null){
    	
       Vector ags = (Vector)custAgents.get(aab);
       
       for(int o = 0;o<ags.size();o++){
    	
        String agen = ags.elementAt(o) + "";
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim())); 
          if( agen.trim().equals("146")) {
        	  laks = false;
          }
         
          if (agen != null && agents1.get(agen.trim()) != null){
				Agent aa = (Agent)agents1.get(agen.trim());
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
        
       }
        
       }
      
       
      if(kood.equals("38894")){
    	//  System.out.println("klient on OLEMAS 38894");
    	  Enumeration enume = getParent().getAgentsHash().elements();
          while (enume.hasMoreElements()) {
              cust.addAgent((Agent) enume.nextElement());
            //  System.out.println("lisan agenti " + cust.getAgents().toString());
          }
      }

    
    
      
    /*  if(deldays.get(cust.getID())  != null){
      	cust.setDelDays((String)deldays.get(cust.getID()));
      }*/
        String term = rs.getString("paym");
        if(term != null && term.trim().length() >0){
        	cust.setPaymTerm(term);	
        }
        

        double  credit = rs.getDouble("credit");
        if(credit != 0){
     	   cust.setCredit(((int)credit)+"");
        }
        if(openOrders.get(cust.getID()) != null){
     	   cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Open Order |"   +  openOrders.get(cust.getID()));
        }
        
       
        String licence = rs.getString("licence");
        if (licence != null && licence.trim().length()>0) {
        		cust.setLicence(licence.trim());
        }
        
        String info = rs.getString("phone");
        if (info != null && info.trim().length()> 2) {
           if (info.trim().endsWith(",")){
        	   info = info.substring(0,info.length()-1);
           }
           if (info.trim().startsWith(",")){
        	   info = info.substring(1);
           }
        	cust.setPersonalData("Tel:"+ info.trim());
        }
        
        String mphone = rs.getString("mphone");
        if (mphone != null && mphone.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + "Tel:"+ mphone.trim());
        	}else{
        		 cust.setPersonalData( "Tel:"+ mphone.trim());
        	}
        }
        
        String ophone = rs.getString("orderphone");
        if (ophone != null && ophone.trim().length()>0) {
        	cust.setJuridics(ophone);
        	
        }
        
        
        String confmail = rs.getString("email");
        if (confmail != null && confmail.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + confmail.trim());
        	}else{
        		 cust.setPersonalData(confmail.trim());
        	}
        }
        
        String hinnak = rs.getString("hinnak");
        if (hinnak != null && hinnak.trim().length()>0) {
        	cust.setGroup(hinnak.trim());
        }
        
        String city = null; 
        String chain = rs.getString("chain");
        if (chain != null && chain.trim().length()>0) {
        	cust.setChain(chain.trim());
        	city = chain.trim();
        }
       
        
        
        String kate = rs.getString("kate");
        if (kate != null && kate.trim().length()>0) {
        	cust.category = (kate.trim());
        	if(city != null) {
        		city = city + " - " + kate.trim();
        	}else {
        		city =  kate.trim();
        	}
        }
          
        
        if(city != null) {
        	if(cust.getCity() != null && cust.getCity().trim().length() > 0) {
        		cust.setCity(cust.getCity() + "|" + city);
        	}else {
        		cust.setCity(city);
        	}
        	
        }
        
        String alegru = rs.getString("alegru");
        if (alegru != null && alegru.trim().length()>0) {
        	if(cust.getGroup()!= null && cust.getGroup().trim().length() > 0){
        		cust.setGroup(cust.getGroup().trim() +"|"+alegru.trim());
            }else{
        		cust.setGroup(alegru.trim());
            }
        	
        	}
        	
        
        
       
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    //	laks = false;
                    }
                    
                    if(laks){
                        customers.put(name+abi, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      
      

      String sql = "	  select k.PK_KLIENTS AS klient,d.DOK_NR AS number,d.DOK_NR2 AS number2 ,d.DOK_SERIJA  as seria,APM_TERM AS maksepaev,(D.SUMMA_APMP-D.SUMMA_PIESP) AS tasumata, " +
      "  DAT_DOK AS kuupaev , datediff(day,APM_TERM,getdate()) AS yle FROM   APV04761.DPIRMDOK  d  " +
       "  LEFT JOIN APV04761.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS where  (D.SUMMA_PIESP-D.SUMMA_APMP)!=0 and APM_TERM > getdate()-300  ";
                   
                rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        String saaja = rs.getString("klient").trim();
                        debt.setCustID(saaja);
                        debt.setPayerID(saaja);
                        
                        String number = rs.getString("number").trim();

                        String number2 = rs.getString("number2");
                        String seria = rs.getString("seria");
                        if(number2 != null && seria != null && seria.trim().length() > 0){
                        	number = seria + number2.trim();
                        }
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
    }
    
    return customers;
  }

  private Calendar  getNextShipCal(String days) throws SQLException{
	  	Calendar res = null;
	 		//	Saame kõik 2 nädala päevad kätte 
				LocalDate now = new LocalDate();
				int paev = now.getDayOfWeek();
				LocalDate mon = now.withDayOfWeek(DateTimeConstants.MONDAY);
				LocalDate tue = mon.plusDays(1);
				LocalDate wed = mon.plusDays(2);
				LocalDate thu = mon.plusDays(3);
				LocalDate fri = mon.plusDays(4);
				LocalDate sat = mon.plusDays(5);
				LocalDate sun = mon.plusDays(6);
				LocalDate mon1 = mon.plusDays(7);
				LocalDate tue1 = mon.plusDays(8);
				LocalDate wed1 = mon.plusDays(9);
				LocalDate thu1 = mon.plusDays(10);
				LocalDate fri1 = mon.plusDays(11);
				LocalDate sat1 = mon.plusDays(12);
				LocalDate sun1 = mon.plusDays(13);
				
			LocalDate kaes = new LocalDate();
			ArrayList <LocalDate> aaa = new ArrayList<LocalDate>();
				 
				 StringTokenizer st = new StringTokenizer(days,",");  
				 while(st.hasMoreTokens()) {
					 
					int day = Integer.parseInt(st.nextToken());
				//   System.out.println(day + " uuu " + paev);
				   
				   if(day == 1) {
					   aaa.add(mon1);
					   aaa.add(mon);
					   
				   }
				   if(day == 2) {
					   aaa.add(tue1);
					   aaa.add(tue);
				   }
				   if(day == 3) {
					   aaa.add(wed1);
					   aaa.add(wed);
				   }						
				   if(day == 4) {
					   aaa.add(thu1);
					   aaa.add(thu);
				   }
				   if(day == 5) {
					   aaa.add(fri1);
					   aaa.add(fri);
				   }
				   if(day == 6) {
					   aaa.add(sat1);
					   aaa.add(sat);
				   }
				   if(day == 7) {
					   aaa.add(sun1);
					   aaa.add(sun);
				   }
				   
				 }
				 
				 // aaa sees on päevad millal action toimub - nyyd onvaja leida tänasele päevale lähin neist .
				 Collections.sort(aaa);
				 Iterator<LocalDate> asd = aaa.iterator();
				 while (asd.hasNext()) {
					 LocalDate fff = asd.next();
					 if(fff.isAfter(now)) {
						 kaes = fff;
						 break;
					 }
				 }
				 
				 
		//System.out.println("kaes " + kaes + " "  );
		
				 try {
					 if(kaes != null) {
							java.util.Date d = new SimpleDateFormat("yyyy-MM-dd").parse(kaes.toString());
							res = Calendar.getInstance();
							res.setTime(d);
					 }
			
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//System.out.println("päev tagasi " + kaes);
			
	  	return res ;
	  }
	  
  
  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

    public String getDistribution() {
        return null;
      }

 private HashSet usedGr = null;
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
     
    String    sql = " SELECT  G.PK_NOMGR AS kood, G.KODS AS abi, G.NOSAUK AS nimi FROM   APV04761.NNOMGR G  " +
	"  LEFT OUTER JOIN APV04761.NNOMGR VGR ON G.PK_GRUPA=VGR.PK_NOMGR  WHERE G.STATUSS = 0 ORDER BY G.NPK ";
    sql = " select pk_nomkat as kood , nosauk as nimi  from APV04761.LGNOMKAT  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("kood").trim();
      String nimi = rs.getString("nimi").trim();
     
      if(usedGr.contains("2." + liik)){
    	  tm.put("2." + liik, Converter.enCodeEst(nimi));  
      }
       
    }
   
    rs.close();
    st.close();
    con.close();
    return tm;
}

  
  public TreeMap getProdGroupsSecond() throws SQLException {
	    Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	     
	    String    sql = " select pk_nomkat as kood , nosauk as nimi  from APV04761.LGNOMKAT  ";
	    
	      sql = "select KODS as kood, NOSAUK as nimi from APV04761.CHD_MG1";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("kood").trim();
	      String nimi = rs.getString("nimi").trim();
	     
	      if(usedGr.contains( liik)){
	    	  tm.put(liik, Converter.enCodeEst(nimi));  
	      }
	       
	    }
	   
	    rs.close();
	    st.close();
	    con.close();
	    return tm;}

  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();

 HashMap eans = null;
  
  public TreeMap getProducts(Agent agent){
	  eans = new HashMap();
	  usedProds = new HashSet();
	  usedGr  = new HashSet();
	  
	  TreeMap maxdis = this.getMaxDiscs();
	  
	  TreeMap types = new TreeMap();
	  
	  String grout = "307,845,757,924,1,309";
	  if( getParent().getProps().getProperty("groups.out") != null){
		  grout = getParent().getProps().getProperty("groups.out");
	  }
	  
	  String cmd = "select t.PK_NOM as abi, t.KODS as  itemid,t.NOSAUK  AS nimetus, t.M_ATT12 AS kastis,M_ATT13 AS alusel,  " +
	"  	 bar.BAR_KODS AS ean ,PK_NOMKAT as grupp,gr.kods as gruppuus, PK_NOMGR AS grupp2, " +
	"  		yh.kods As unit, 0 as liter , MINPARD as minmax ,  PIEG_SOLIS as opt ,'' as type " +
	" from APV04761.NNOM  t LEFT OUTER JOIN APV04761.NBARKODS bar ON bar.PK_NOM=t.PK_NOM AND bar.IS_MAIN = 1 " +
	" LEFT OUTER JOIN APV04761.NVIEN yh ON t.PK_VIEN=yh.PK_VIEN " +
	"  LEFT OUTER JOIN APV04761.CHD_MG1 gr ON t.PK_MG1=gr.PK_MG1 " +
	"  LEFT OUTER JOIN APV04761.NMINMAX aa ON aa.PK_NOM=t.PK_NOM and aa.PK_NOL=53268 " +
	"  where t.STATUSS = 0  and PAMVID = 2015 and t.PK_NOMGR not in (" + grout + ")   and t.NOSAUK not like '%Ledusskapis%' ";
	    
	  
    getSaveStockInfo();
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
   
    try {
    	
        String hsql =  "   SELECT  N.KODS AS toode, CENA.CENA AS hind,CENA.PK_CENVEID  " +
    		"  FROM   APV04761.NCEN CENA, APV04761.NNOM N, APV04761.NCENVEID CV, APV05308.SVALUTAS VAL  WHERE " +
    		"  CENA.PK_NOM=N.PK_NOM AND CENA.PK_CENVEID=CV.PK_CENVEID AND  " +
    		"  CENA.PK_VAL=VAL.PK_VAL AND N.STATUSS = 0 AND CV.STATUSS = 1 AND " +
    		" CENA.NO_DAT <= getdate()+1  AND  " +
    		"  CENA.LIDZ_DAT >= getdate()-1   AND CV.TIPS IN (0)  and VAL.KODS = 'EUR' " +
			"   and CENA.PK_CENVEID in ( 84,67)  group by  N.KODS,CENA.PK_CENVEID,CENA.CENA order by  N.KODS ";
 	
    	
    Connection con = this.getNewConnection();
   	Statement stmt =  con.createStatement();
    ResultSet   rs = stmt.executeQuery(hsql);
  HashMap<String, Double> hinnad = new HashMap<String, Double>();
  
  HashMap<String, Double> hinnad2 = new HashMap<String, Double>();
   
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   double hh = rs.getDouble("hind");
    	   if(hinnad.get(kood) == null){
    		  	hinnad.put(kood, hh);
    	   }
    	 
    }
    rs.close();
    
    
    hsql =  "    SELECT   N.KODS AS toode, N.NOSAUK AS N_NOSAUK, CENA.CENA AS hind, VAL.KODS AS VAL_KODS, CV.NOSAUK AS CV_NOSAUK, '0' AS MULTISEL, N.PK_NOM AS N_PK_NOM, CV.TIPS AS CV_TIPS, CENA.PK_CEN AS CENA_PK_CEN, " + 
 " CENA.COUNTER AS CENA_COUNTER ,CENA.NO_DAT,CENA.LIDZ_DAT ,CENA.PK_CENVEID " +
 " FROM   APV04761.NCEN CENA, APV04761.NNOM N, APV04761.NCENVEID CV, APV05308.SVALUTAS VAL  " +
 " WHERE (CENA.PK_NOM=N.PK_NOM) AND (CENA.PK_CENVEID=CV.PK_CENVEID) AND (CENA.PK_VAL=VAL.PK_VAL) AND (( ( (N.STATUSS = 0) ) " +
 " AND ( (CV.STATUSS = 1) ) " +
 " AND  CENA.PK_CENVEID=117 " + //horeca 54 ja 64 on baasid
 " AND ( (CENA.NO_DAT <= getdate()) ) AND ( (CENA.LIDZ_DAT >= getdate()) ) )) AND (CV.TIPS IN (0)) and VAL.KODS = 'EUR' " +
 " ORDER BY   CV_NOSAUK ASC";
	
    rs = stmt.executeQuery(hsql);
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   double hh = rs.getDouble("hind");
    	   	hinnad2.put(kood, hh);
    }
   	rs.close();
   	
   	String sss = "select pk_nom as toode ,a.kods as type from APV04761.NKSORTIMR r "
	  		+ "inner join APV04761.NKSORTIM a on a.PK_KSORTIM= r.PK_KSORTIM order by pk_nom ";
	  
   	
	
    rs = stmt.executeQuery(sss);
    while(rs.next()){
    	   String kood = rs.getString("toode").trim();
    	   String hh = rs.getString("type");
    	   	types.put(kood, hh);
    }
   	rs.close();
   	
   	
      rs = stmt.executeQuery(cmd);
      while(rs.next()){
    	  String abi = rs.getString("abi").trim();
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim()  ;
      
     nimetus =  Converter.enCodeEst(nimetus);
    if(kogused.get(kood) != null){
   // 	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
    }else{
    	//prod.setStock(0);
    }
       
    double kast = rs.getDouble("kastis");
    if(kast > 0){
    	 prod.setInCases(kast);
    }
       
    double alusel = rs.getDouble("alusel");
    if(alusel > 0){
    	 prod.setOnPallet(alusel);
    	 }
     
       
       if(hinnad.get(kood) != null){
    	   prod.setPrice(hinnad.get(kood));
       }
        

        String ean = rs.getString("ean");
        String addx = "";
        if(ean != null && ean.trim().length()>4){
        prod.setEAN(ean.trim());
        eans.put(ean.trim(), prod.getID());
        addx = ean.trim().substring(ean.trim().length() -4);
        }
        String grp = rs.getString("grupp");
        if(grp != null){
        	  prod.setGroup("1." +grp.trim());  
        	  if(!usedGr.contains("1." +grp.trim())){
            	  usedGr.add("1." +grp.trim());  
        	  }
        }
        
        String grpuus = rs.getString("gruppuus");
        
        if(grpuus != null){
      	 prod.setGroupSecond(grpuus.trim());  
      	  if(!usedGr.contains(grpuus.trim())){
          	  usedGr.add(grpuus.trim());  
      	  }
      }
        
       // String grp2 = rs.getString("grupp2");
        if(grp != null){
        	  prod.setGroup("2." + grp.trim());  
        	  if(!usedGr.contains("2." +grp.trim())){
            	  usedGr.add("2." +grp.trim());  
        	  }
        }
        
        if(hinnad2.get(kood) != null){
        	if(hinnad2.get(kood) != prod.getPrice()){
        	 	 //  nimetus = nimetus + " !pr:" +(hinnad2.get(kood) + "€!");
        	}
        }
        
        nimetus = nimetus.replaceAll("konfektes", "konf.");
        nimetus = nimetus.replaceAll("Ikdienas", "Ikdie.");
        nimetus = nimetus.replaceAll("skalojamais", "skaloj.");
        nimetus = nimetus.replaceAll("Premium", "Prem.");
        nimetus = nimetus.replaceAll("Dressing", "Dres.");
        nimetus = nimetus.replaceAll("SANDWITCH", "Sandw.");
        nimetus = nimetus.replaceAll("salvetes", "salv.");
        nimetus = nimetus.replaceAll("tualete", "tual.");
        nimetus = nimetus.replaceAll("Piedeva", "Pied.");
        nimetus = nimetus.replaceAll("Pyramid", "Pyr.");
    
        if(maxdis.get(kood + "%") != null) {
      	  prod.setMaxDiscount(Double.parseDouble(maxdis.get(kood+"%")+"") * 100);
      	  prod.setDiffColor(true);
      	addx = addx + " MDC" + NF.format((Double.parseDouble(maxdis.get(kood+"%")+"") * 100))+ "%";
        } else  if(maxdis.get(kood) != null) {
      	  prod.setMinPrice(Double.parseDouble(maxdis.get(kood)+""));
        } else {
      	  //prod.setMaxDiscount(-1);
        }
        
      prod.setName(nimetus + " " + addx);
      prod.setAlegrupp("1");
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
        }
      double liter = rs.getDouble("liter");
      	
      if (liter > 0){
    	 // prod.setInUnit(liter);
      }
      double minmax = rs.getDouble("minmax");
      if(minmax > 0){
    	  prod.setOptAmountInUnits(minmax);
      }
      
      
      
      String type = rs.getString("type");
      if(type!=null && type.trim().length()>0){
    	  if(type.trim().equals("F")){
    		  prod.setType("FF");
    	  }else{
    		  prod.setType(type.trim());
    	  }
    	  
    	 
        }
      boolean goes = true ;
      if(types.get(abi) != null){
    	 type = (types.get(abi)+"");
    	 if(type.trim().equals("M")){
   		  goes = false;
   	     }
    	  if(type.trim().equals("F")){
    		  prod.setType("FF");
    	  }else{
    		  prod.setType(type.trim());
    	  }
    	  
      }
      
    
      
      
      
       prod.setVat("21");
       
    if(goes){
    	 products.put(nimetus+ " " + kood, prod);
         usedProds.add(prod.getID());
    }
     
      }
      rs.close();
      stmt.close();
      con.close();
    } catch (Exception e){
      getParent().showError(e);
    }
	
    
    return products;
  }

 



	    

  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    //if (true) return custProds ;
	 int counter = 1;

	 String cmd = " SELECT    K.PK_KLIENTS as klient,N.kods as prodid ,r.DAUDZ as qty,D.DAT_DOK AS lastdate ,max(r.CENA) as hind ,max(D.DOK_NR) as dok " +
	 " FROM   APV04761.NDOK r inner JOIN  APV04761.DPIRMDOK D  on r.PK_DOK=D.PK_DOK  " +
	 " LEFT OUTER JOIN APV04761.NNOM N ON r.PK_NOM=N.PK_NOM AND r.RN_VEIDS IN (0,3,5)  " +
	 " LEFT OUTER JOIN APV04761.NVIEN VIEN ON r.PK_VIEN=VIEN.PK_VIEN " +
	 " LEFT JOIN APV04761.NDOKG G ON D.PK_DOK = G.PK_DOK " +
	 " LEFT JOIN APV04761.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
	 "  LEFT OUTER JOIN  APV04761.NADRESES a ON A.PK_KLIENTS=K.PK_KLIENTS and a.LEG_ADR = 2 " +
	 "  WHERE D.DAT_DOK > getdate()-180 AND D.PAMV_ID = 2711 AND N.PK_NOMGR != '122'  " +
	 "  GROUP BY K.PK_KLIENTS,N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ " +
	 "  ORDER BY K.PK_KLIENTS,N.NOSAUK,N.KODS  ,D.DAT_DOK ,r.DAUDZ " ;


	    if (isDebug()) System.out.println(cmd);

	    try {
        Connection con = this.getNewConnection();
	    Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	   
	    	  String kliendikood = rs.getString("klient").trim() ;
	        String tootekood = rs.getString("prodid").trim();
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





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  public void setBulkSales(Map agents){
 
   
     String cmd ="  SELECT K.PK_PARD as agent , round(SUM(summa_pv),0) AS summa " +
		"   FROM   APV04761.DPIRMDOK D LEFT OUTER JOIN APV04761.DKLIENTS N ON D.PK_ESPATS=N.PK_KLIENTS " +
		"   LEFT JOIN APV04761.DKLIENTS K ON D.PK_KLIENTS=K.PK_KLIENTS " +
		"   JOIN APV04761.NDOKG G ON D.PK_DOK = G.PK_DOK " +
		"   LEFT OUTER JOIN (SELECT PK_DOK, MAX(LAIKS) AS LAIKS FROM APV04761.SDOKEVNT GROUP BY PK_DOK) LASTEV ON LASTEV.PK_DOK = D.PK_DOK " +
		"   LEFT OUTER JOIN APV04761.NADRESES A ON A.PK_ADRESE=G.PK_ADRESE, APV05308.SVALUTAS V " +
		"    WHERE D.PAMV_ID IN (2710,2720,2711,2721,2712,2786,2713,2737,2718,2728,18212,2745,2782,24501,2797,2798,2739,18086,2736,2783,2799,18091,15008,15011,2738,2792,2795,2742,2793,2796) " +
		"	AND D.PK_VAL=V.PK_VAL AND  YEAR(D.DAT_DOK) = YEAR( getdate()) and  MONTH(D.DAT_DOK) = MONTH( getdate())  " +
		"	 AND D.PAMV_ID = 2711 AND  NOT (D.STATUSS = 4) and K.PK_PARD !='' " +
		"	 GROUP BY K.PK_PARD " +
		"	  ORDER BY  K.PK_PARD ASC";

     //   if (isDebug()) System.out.println(cmd);
           try {
        	   Connection con =  this.getNewConnection();
               Statement stmt = con.createStatement();
               ResultSet	      rs = stmt.executeQuery(cmd);
               while(rs.next()) {
                 String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("summa"));
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


    private TreeMap brandplans = null; 

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
    return "HorizonKonig";
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
  private String url = "jdbc:odbc:horizon2";
  private   Vector debt = new Vector();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

	
    
    
    
   
    
	
	  private void makePictureCat(){
	  	    
	    	
	    	String pictssou = parent.getProps().getProperty("pict.dir");
	  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	       
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
	    
	         res2 = new File (pictssou);
             ff2 = res2.list();
          //   System.out.println(ff2.toString());
	         
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	    	    	String ean = prod.getEAN().trim(); 
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(ean+".")){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
            	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                    	}
            	  	}   }
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
  

	  @Override
	  public void WebReporter() {
		  
		  ArrayList<String> filenames = new ArrayList<String>();
		// teeme kataloogi salvestamiseks
				File f = new File("svdata");
				if(!f.exists()) {
					f.mkdir();
				}
				
				
	// loeme signatuurid sisse kui on olemas	
				HashSet done = null;
				try {
					done = (HashSet) Converter.readObject("signadone.obj");
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				if(done == null) {
					 done = new HashSet();
				}
				
				
	//teeme perioodi valmis
				Calendar algus = Calendar.getInstance();  
			  	Calendar lopp = Calendar.getInstance();  	
			  	Integer days = 20;
			  if(	parent.getProps().getProperty("webreport.days") != null) {
				  try {
					  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
				  }catch(Exception e) {
					  
				  }
			  };

			  	algus.add(Calendar.DATE, -1 * days);
			  	algus.set(Calendar.HOUR, 0);
			  	lopp.set(Calendar.HOUR, 23);
			  	lopp.set(Calendar.MINUTE, 59);
			  	lopp.add(Calendar.DATE, +2);
				
			  	
			  	try {
			  		String fnameBASE = this.getExcelDir() + "reportbase.xlsx";
			  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
			  		XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
					XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
					
					CreationHelper createHelper = wbBASE.getCreationHelper();
					
					 XSSFCellStyle hlinkstyle = wbBASE.createCellStyle();
				      XSSFFont hlinkfont = wbBASE.createFont();
				      hlinkfont.setUnderline(XSSFFont.U_SINGLE);
				      hlinkfont.setColor(IndexedColors.BLUE.getIndex());
				      hlinkstyle.setFont(hlinkfont);

				      System.out.println("files alustan" );
				  		
				  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), rcompany, user, pass,true);
				  		System.out.println("files " + files.toString());
				 
			  		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), rcompany, user, pass, false, "PRODUCT", null, "test");
			  	   System.out.println("cc" + cc.toString() );
			  		Converter.saveFileInUTF8(f.getPath() + "/currSVjson.json", cc.toString());
			  		int palletrow = 1;
			  		boolean savefile = false;
			  		
			  		for (int i = 0; i < cc.length(); i++) {
			  			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
			  		   	String filterOutOfStock = (String)cob.get("resint3").toString();
			  			String dateAndTime = (String)cob.get("recetime");
			  			String dateOnly = dateAndTime.split("T")[0];
			  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
			  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
			  		  String signa = dateAndTime + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
			  		  
			  			if(filterOutOfStock != null && filterOutOfStock.startsWith("333") && done != null && !done.contains(signa )){  	
			  			String filterProblem = (String)cob.get("resstr1");
			  			
			  		  savefile = true;
			  			
			  	    //	String cagid = (String)cob.get("custagent");
			  		//	String cagname = (String)cob.get("custagname");
			  			String agid = (String)cob.get("agentid");
			  			String agname = (String)cob.get("managerName");
			  			if(agname.contains(agid)) {
			  				agname = agname.substring(0,agname.indexOf(agid)).trim();
			  			}
			  			String custname = "";
			  			String custchain = "";
			  			String custaddr = "";
			  			try {
			  				custchain = (String)cob.get("custchain");
			  			} catch(Exception e) {
			  				
			  			}
			  			
			  			try {
			  				custname = (String)cob.get("custname");
			  				if(custname.indexOf(",") != -1) {
			  					custaddr = custname.substring(custname.indexOf(",")+1);
			  					custname = custname.substring(0,custname.indexOf(","));
			  				}
			  			} catch(Exception e) {
			  				
			  			}
			  			
			  		
			  			
			  		//	System.out.println(chain + chDaily + " " + chDaily.contains(chain));
			  	    	
			  			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
			  	    	 JSONParser parser = new JSONParser();
			  	    	 
			  	    	org.json.simple.JSONObject obj1 = (org.json.simple.JSONObject) parser.parse((cob.get("comment").toString()));
			  	    
			  	    	String comID = "";
			  	    	String comment = "";
			  	    	
			  	    	try {
			  	    		org.json.simple.JSONObject obj2 = (org.json.simple.JSONObject) parser.parse((cob.get("custcomme").toString()));
			  	    		comID = ((String)obj2.get("comID"));
			  	    		comment = ((String)obj2.get("comNa"));
			  	    	}catch(Exception e) {
			  	    	}
			  	    	String typeid = (String)obj1.get("disID");
			  	    	String typename = (String)obj1.get("disNa");
			  	    	Double typekogus = (Double)obj1.get("disAm");
			  	    	
			  	    	String bonusid = (String)obj1.get("bonID");
			  	    	String bonusname = (String)obj1.get("bonNa");
			  	    	Double bonuskogus = 0d;
			  	    	try {
			  	    		bonuskogus = Double.parseDouble((String)obj1.get("bonAm"));
			  	    	}catch(Exception e) {
			  	    	}
			  	    	
			  	    	Date fromdate = null; //new Date((Long)obj1.get("sDay"));
			  	    	Date todate = null; //new Date((Long)obj1.get("eDay"));
			  	    	try {
			  	    	  fromdate = new Date((Long)obj1.get("sDay"));
			  	    	  todate = new Date((Long)obj1.get("eDay"));
			  	  	     } catch(Exception k) {
		  	    		  k.printStackTrace();
		  	    	     }
			  	    	
			  	    	Date created = new Date(Long.parseLong(((String)cob.get("resstr3"))));
			  	    	
			  	  
			  	    	String cuname = (String)cob.get("custname");
		  				String cuid = (String)cob.get("custid");
		  				if(cuname.endsWith(cuid)) {
		  					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
		  				}
			  				
			  				
			  				worksheetBASE.createRow(palletrow);
			  				
			  				worksheetBASE.getRow(palletrow).createCell(0).setCellValue(agname);
			  				
			  			    worksheetBASE.getRow(palletrow).createCell(1).setCellValue(created);
			  				XSSFCell celld3 = worksheetBASE.getRow(palletrow).getCell(1);
			  				XSSFCellStyle cellStyle3 = (XSSFCellStyle)celld3.getCellStyle();
				  	        cellStyle3.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
				  	       
			  				worksheetBASE.getRow(palletrow).createCell(2).setCellValue(cuname);
			  				worksheetBASE.getRow(palletrow).createCell(3).setCellValue(custchain);
			  				worksheetBASE.getRow(palletrow).createCell(4).setCellValue(custaddr);

			  				
			  			
			  				//worksheetBASE.getRow(palletrow).createCell(2).setCellValue(typeid);
			  				worksheetBASE.getRow(palletrow).createCell(5).setCellValue(typename);
			  				worksheetBASE.getRow(palletrow).createCell(6).setCellValue(typekogus);
			  				XSSFCell cellk1 = worksheetBASE.getRow(palletrow).getCell(6);
			  				XSSFCellStyle pstyle = worksheetBASE.getRow(0).getCell(6).getCellStyle();
			  				cellk1.setCellStyle(pstyle);
			  				
			  				 
			  				worksheetBASE.getRow(palletrow).createCell(7).setCellValue(fromdate);
			  				XSSFCell celld1 = worksheetBASE.getRow(palletrow).getCell(7);
			  				XSSFCellStyle cellStyle = (XSSFCellStyle)celld1.getCellStyle();
				  	        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			  				
			  				worksheetBASE.getRow(palletrow).createCell(8).setCellValue(todate);
			  				XSSFCell celld2 = worksheetBASE.getRow(palletrow).getCell(8);
			  				XSSFCellStyle cellStyle2 = (XSSFCellStyle)celld2.getCellStyle();
				  	        cellStyle2.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
				  	        
				  	        
				  	        
				  	        worksheetBASE.getRow(palletrow).createCell(9).setCellValue((String)cob.get("prodgroupname"));
				  	      try {
				  	    	  worksheetBASE.getRow(palletrow).createCell(10).setCellValue((String)cob.get("resstr2"));  
				  	      }catch(Exception e) {
				  	    	  
				  	      }
				  	      
				  	        worksheetBASE.getRow(palletrow).createCell(11).setCellValue((String)cob.get("prodname"));
			  			
			  				//worksheetBASE.getRow(palletrow).createCell(12).setCellValue(bonusid);
			  				
			  				worksheetBASE.getRow(palletrow).createCell(12);
			  				XSSFCell cellk2 = worksheetBASE.getRow(palletrow).getCell(12);
			  				XSSFCellStyle pstyle2 = worksheetBASE.getRow(0).getCell(4).getCellStyle();
			  				cellk2.setCellStyle(pstyle2);
			  				cellk2.setCellValue(bonuskogus);
			  		
			  				// worksheetBASE.getRow(palletrow).createCell(12).setCellValue(comID);
			  				 worksheetBASE.getRow(palletrow).createCell(13).setCellValue(comment);
			  				
			  				
			  				
			  				try {
			  				String foto = null;
			  				org.json.simple.JSONArray phs = (org.json.simple.JSONArray)parser.parse((cob.get("pictureLinks").toString()));
			  				
			  				if(files != null && files.get(cuid) != null && phs != null) {
			  					int a = 0;
			  				//	if(cuid.equals("747724"))
			  						System.out.println("phs " + phs.toString());
			  						for(int y = 0; y < phs.size() ; y++) {
			  							String fname = (String)phs.get(y);
			  						
			  					
			  				
			  					ArrayList arr = (ArrayList)files.get(cuid);
			  					for(int e = 0; e<arr.size(); e++) {
			  						Toode t = (Toode)arr.get(e);
			  						foto = t.getLadu();
			  						if(foto != null && foto.indexOf(fname) !=-1 && t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
			  							
			  							
			  							
			  								
			  				  		         Cell  cell = worksheetBASE.getRow(palletrow).createCell(14+a);
			  				  		         cell.setCellValue("Photo Link "+(a+1) );
			  				  		         XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(Hyperlink.LINK_URL);
			  				  		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
			  				  		         cell.setHyperlink((XSSFHyperlink) link);
			  				  		         cell.setCellStyle(hlinkstyle);
			  				  		     a++;
			  				  				
			  						}
			  					}
			  						}
			  						
			  				}
			  				}catch(Exception e) {
			  					e.printStackTrace();
			  				}
			  				 //URL Link
			  				
			  				
			  				
			  			 
			  				worksheetBASE.getRow(palletrow).createCell(18).setCellValue((String)cob.get("prodid"));
			  				worksheetBASE.getRow(palletrow).createCell(19).setCellValue(agid);
			  				worksheetBASE.getRow(palletrow).createCell(20).setCellValue("LV");
			  				worksheetBASE.getRow(palletrow).createCell(21).setCellValue((String)cob.get("custid"));
			  				worksheetBASE.getRow(palletrow).createCell(22).setCellValue(bonusid);
			  				worksheetBASE.getRow(palletrow).createCell(23).setCellValue(comID);
			  				//
			  				
			  				
			  				
			  			/*	for(int x=0; x<11; x++) {
			  					if(x == 0 || x == 1 || x == 3) {
			  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderCenteredOOS);
			  					}else {
			  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderOOS);
			  					}
			  				}*/
			  				palletrow++;
			  				done.add(signa );
			  			
			       			
			       			
			  			}
			  		}
			  		String exceldir = this.getExcelDir() + "reports/";
			  		File ff = new File( exceldir );
			  		if(!ff.exists()) {
			  			ff.mkdir();
			  		}
			  		excelifailBase.close();
			  		
if(savefile) {
	FileOutputStream output_file_oos =new FileOutputStream(exceldir + "productPallet-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
		//filenames.add(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
		wbBASE.write(output_file_oos); //write changes
		output_file_oos.close();  //close the stream
		System.out.println("report saved " + exceldir + "productPallet-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
      
}else {
	System.out.println("no data to save");
}
			  	
			  	}catch(Exception e) {
					 e.printStackTrace(); 
				  }
			  	
			  	
				try {
					Converter.saveObject(done, "signadone.obj");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			  
			  System.exit(0);
	  }
	  
	  
	
	
}
