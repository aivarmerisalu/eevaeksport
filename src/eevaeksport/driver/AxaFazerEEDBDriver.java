package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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

public class AxaFazerEEDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter {
  public AxaFazerEEDBDriver(EksportPanel parent){
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
     }
  String user =  null;
  String pass = null;
  String rcompany = null;

  
  
  private TreeMap getDisplayInfo() {
	  TreeMap res = new TreeMap();
	  
	   try{
		   
	
	
	   int a = 0;
	   File f = new File (getExcelDir() + "Displayinfo.xlsx");
	   FileInputStream	 ff = new FileInputStream(f);
	
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		XSSFRow row = null; 
				ff.close();	
					
						
							
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
									cust = (row.getCell((short) 0).getStringCellValue()).toUpperCase();
		 					      } catch (Exception e) {
		 					    	 cust =(( (int)(row.getCell((short) 0).getNumericCellValue()))+"").toUpperCase();
		 						  }
								
								String type = null;
								try {
									type = Converter.enCodeEst((row.getCell((short) 1).getStringCellValue()));
		 					      } catch (Exception e) {
		 					    	 type =( (int)(row.getCell((short) 1).getNumericCellValue()))+"";
		 						  }

								String text = null;
								try {
									text = Converter.enCodeEst((row.getCell((short) 2).getStringCellValue()));
		 					      } catch (Exception e) {
		 					    	 text = Converter.enCodeEst(( (int)(row.getCell((short) 2).getNumericCellValue()))+"");
		 						  }
								
								Date algus = null;
								try {
									algus = (row.getCell((short) 3).getDateCellValue());
		 					      } catch (Exception e) {
		 					    	 
		 						  }
								Date lopp = null;
								try {
									lopp = (row.getCell((short) 4).getDateCellValue());
		 					      } catch (Exception e) {
		 					    	 
		 						  }
								
								Calendar cc = Calendar.getInstance();
								if(cc.getTimeInMillis() > algus.getTime() && cc.getTimeInMillis() < lopp.getTime()) {
									if(res.get(cust) != null) {
										res.put(cust, res.get(cust) + "|" + type + ":" + text);
									}else {
										res.put(cust,  type + ":" + text);
									}
									
								}
								
							}
				
							
							
         }catch(Exception r) {
		 r.printStackTrace();  
	     }			
			//System.out.println("oioi " + res.toString());	
				return res;
  }

  
  /**
   *
   */
  public void connectionUp(){
  }

	public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		

	String sq3 = "  SELECT jou.custaccount as klient, jou.deliverydate  as kuup , tra.itemid as toode ,\r\n" + 
			"						 sum(case when tra.salesqty > 0 then tra.salesqty else 0 end) as kogus,\r\n" + 
			"				 sum(case when tra.salesqty < 0 then tra.salesqty else 0 end) as kogusmin, max (jou.salesid) as doknr , \r\n" + 
			"			 case when  jou.deliverydate > CAST(GETDATE() AS DATE)then 1 else 0 end as color FROM salesline  tra INNER JOIN salestable jou ON \r\n" + 
			"			 tra.salesid=jou.salesid  and  tra.dataareaid= jou.dataareaid  WHERE  jou.deliverydate >=   \r\n" + 
			"			(select top 1 kuupaev  from ( 	 SELECT top 10 jou2.deliverydate as kuupaev FROM  salestable jou2  where ltrim(jou2.custaccount) = ltrim(jou.custaccount)  and jou2.dataareaid= 'eee'  \r\n" + 
			"			 and jou2.deliverydate > getdate()-14 and jou2.deliverydate < getdate()+4 group by jou2.custaccount , jou2.deliverydate order by jou2.deliverydate desc) as dd order by kuupaev)  \r\n" + 
			"			 AND tra.dataareaid ='eee'\r\n" + 
			"	 GROUP BY jou.custaccount,jou.deliverydate , tra.itemid ORDER BY jou.custaccount,jou.deliverydate desc, max(jou.salesid) desc, tra.itemid" ;
					
					Connection con3 = getNewConnection();
					Statement st3 = con3.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(rs3.getString("kuup").substring(5,10));
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
	  makePictureCat();
	  System.exit(0);
   	return null;
   }

  
  private void makePictureCat(){
	     
	  getProducts(null);
    	
    	String picts = parent.getProps().getProperty("pict.dir");
  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
  	 

    	try {

 HashSet tooted = new HashSet();
         StringBuffer buf = new StringBuffer();
         StringBuffer buf0 = new StringBuffer();
         int a = 0;
         int b = 0;
         int c = 0;
         System.out.println("Piltide leidmine ");
         
    	   for  (int i = 0 ; i<prodcodes.size();i++) {
    	    	a++;
    	    	c++;
    	    	if(c == 100){
    	    		  c =0;
    	    		  System.out.println("Piltide leidmine "  + a);	
    	    	}
    	     
                  String toode = prodcodes.elementAt(i) + "";  
                  toode = toode.replace('/', '_');
                  toode = toode.replace('\\', '_');
                
                  File f = new File(picts + toode+".jpg");
                  String len = "0";
                  if(f.exists()){
                    len =	f.length() + "";
                  }
          if(len.equals("0")){
            	//System.out.println(toode+".jpg;"  + len);
            	buf0.append(toode+".jpg\n");
            }else{
            	tooted.add((toode+".jpg").toLowerCase());
            	buf.append(toode+".jpg;"  + len + "\n");
      	    	b++;
            }
          
	   }
      
    	
    	    //oleasoleva kataloogi kustutamine
    	  System.out.println("Kataloogi kustutamine "  + a);	    
                File f = new File("list.txt");
                if (f.exists())f.delete();
                
                  try {
            		PrintWriter os = new PrintWriter(new FileWriter(
            				  "list.txt", true));
            		os.write(buf.toString());
            		os.close();
            	
            	  }catch (Exception ex) {
            		ex.printStackTrace();
            	  }
            	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
            	  File f0 = new File("list0.txt");
            	
            	  	
                     if (f0.exists())f0.delete();
                  
                    try {
              		PrintWriter os1 = new PrintWriter(new FileWriter(
              				  "list0.txt", true));
              		os1.write(buf0.toString());
              		os1.close();
              		//System.out.println( buf.toString());
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }

    //Kataloogi sisude kustutamine
              	  
   
           	 ArrayList names = new ArrayList();
           	 
         	 File res2 = new File (pictspda);
             String[] ff2 = res2.list();
         	 for(int i = 0;ff2.length>i;i++){
             //	System.out.println("fff " + ff2[i]);
             //	(new File ( pictspda + ff2[i])).delete(); 
          names.add(ff2[i].toLowerCase());
             }
//         	 L�ppes kustutamine 
         	 
           System.out.println("Piltide kopeerimine ");	
           //Failide kopeerimine  
             String[] gg = new File(picts).list();
          	  //System.out.println("gg " + gg[0] + " " + gg.length);
             
              for(int i = 0;gg.length>i;i++){
              	String fn = gg[i];
              	File in = new File ( picts + fn); 
              	if(in.isFile() && tooted.contains(fn.toLowerCase()) && !names.contains(fn.toLowerCase())){
              	
              	File inpda = new File ( pictspda +  toUpper( fn )); 
                    	Converter.shrinkAndCopyFile(in,inpda);
              	}
              }
              File fff =  new File (pictspda +  "list.txt");
              Converter.copyFile(f,fff);  	
        	  File fff0 =  new File (pictspda +  "list0.txt");
        	  Converter.copyFile(f0,fff0);  	
    	} catch (Exception e) {
    	    getParent().showError(e);
    	}
    
}
  
  private String toUpper(String asi ){
	   
	   asi = asi.replace('\\', '_');
	   asi = asi.replace('\\', '_');
	   asi = asi.replace('/', '_');
	   asi = asi.replace('/', '_');
	   asi = asi.substring(0,asi.indexOf('.'))+ asi.substring(asi.indexOf('.')).toLowerCase();
	   return asi ;
  }
  



   public Vector getFixedAssortmentData() {
     return null;
   }

   public Vector getPriceMatrixData() {
       Vector lines = new Vector();
try {

    String sql4a = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind , PERCENT1 as perc ,PRI.FROMDATE as algus " +
    		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp ,PRI.RELATION , astcampaignid as kamp2 " +
" FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
"  WHERE PRI.DATAAREAID='eee' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
"    (GETDATE() between PRI.FROMDATE AND PRI.TODATE+1)    and astcampaignid != '' " +
"   ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation " ;
    
  
                         
    Connection con = getNewConnection();
 	Statement stmt = con.createStatement();
    
         ResultSet  rs = stmt.executeQuery(sql4a);

           while (rs.next()) {

                   String clGroup = Converter.cleanLitLatEst(rs.getString("grupp"));
                   String prodID = rs.getString("toode");
                   double price = rs.getDouble("hind");
                   double perc = rs.getDouble("perc");
                   String algus = rs.getString("algus").substring(0,10);
                   String lopp = rs.getString("lopp").substring(0,10);
                   int kuu = rs.getInt("kamp");
                  // int relation = rs.getInt("RELATION");
                   String kamp2 = rs.getString("kamp2");
                   boolean iscamp = false;
                   if(kamp2 != null && kamp2.trim().length() > 0) {
                	   iscamp = true;
                   }
                   
                   MatrixLine mline = new MatrixLine();
                   
               
                   if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                   }

                   if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                   }
                   
                   if( iscamp ){
                  	
                    if (algus  != null && algus.trim().length() > 0 && algus.indexOf("1900")== -1) {
                           mline.setStartDate(algus.trim());
                   }

                   if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                           mline.setEndDate(lopp.trim());
                   }
                   
                   if(kuu > 2  && algus.indexOf("1900")== -1 && lopp.indexOf("1900")== -1){
                  	 //mline.setProdLevel("pr");
                   }
                   
                  // ainult kampaaniate puhul paneme hinna info kaasa .
                   if(price > 0){
                	   mline.setPrice(price);
                       lines.add(mline);
                   }
                     
                       if(perc > 0){
                    	   mline.setDiscount(perc);
                           lines.add(mline);
                       }
                       
                       
                   }
                
                  	
                
                  	
                      
                	
           }
           
           rs.close();

           
           
           String sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind  , PERCENT1 as perc ,PRI.FROMDATE as algus " +
           		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp ,PRI.RELATION , astcampaignid as kamp2 " +
       " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
       "  WHERE PRI.DATAAREAID='eee' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
       "    ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
       "  or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
       "  or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
       "  or( GETDATE()<=PRI.TODATE+1) )   and astcampaignid = '' " +
       "   ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation " ;
           
           
           
           rs = stmt.executeQuery(sql4);

           while (rs.next()) {

                   String clGroup = Converter.cleanLitLatEst(rs.getString("grupp"));
                   String prodID = rs.getString("toode");
                   double price = rs.getDouble("hind");
                   double perc = rs.getDouble("perc");
                   String algus = rs.getString("algus").substring(0,10);
                   String lopp = rs.getString("lopp").substring(0,10);
                   int kuu = rs.getInt("kamp");
                  
                   String kamp2 = rs.getString("kamp2");
                   boolean iscamp = false;
                   if(kamp2 != null && kamp2.trim().length() > 0) {
                	   iscamp = true;
                   }
                   
                   MatrixLine mline = new MatrixLine();
                   
               
                   if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                   }

                   if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                   }
                   
                   if( iscamp ){
                  	
                    if (algus  != null && algus.trim().length() > 0 && algus.indexOf("1900")== -1) {
                           mline.setStartDate(algus.trim());
                   }

                   if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                           mline.setEndDate(lopp.trim());
                   }
                   
                   if(kuu > 2  && algus.indexOf("1900")== -1 && lopp.indexOf("1900")== -1){
                  	 //mline.setProdLevel("pr");
                   }
                   
                   }
                   boolean goes = true;
                 
                   
                   if(goes && iscamp){  //ainult kampaaniate puhul 
                  	
                           if(price > 0){
                        	   mline.setPrice(price);
                               lines.add(mline);
                           }
                	
                		
                           if(perc > 0){
                        	   mline.setDiscount(perc);
                               lines.add(mline);
                           }
                	   }
                  
           }
           
           rs.close();

           
           
         sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind , PERCENT1 as perc ,PRI.FROMDATE as algus " +
           		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp,  astcampaignid as kamp2 " +
" FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
" WHERE PRI.DATAAREAID='eee' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0  and " +
"   ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) "+
" or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
" or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
" or( GETDATE()<=PRI.TODATE+1) )   " +
"    ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                  
                  rs = stmt.executeQuery(sql4);

                  while (rs.next()) {

                          String custID = Converter.cleanLitLatEst(rs.getString("grupp"));
                          String prodID = rs.getString("toode");
                          double price = rs.getDouble("hind");
                          double perc = rs.getDouble("perc");
                          String algus = rs.getString("algus").substring(0,10);
                          String lopp = rs.getString("lopp").substring(0,10);
                          int kuu = rs.getInt("kamp");
                          String kamp2 = rs.getString("kamp2");
                          boolean iscamp = false;
                          if(kamp2 != null && kamp2.trim().length() > 0) {
                       	   iscamp = true;
                          }
                          
                          MatrixLine mline = new MatrixLine();
                          
                      
                          if (custID != null && custID.trim().length() > 0) {
                                  mline.setCliGroup(custID.trim());
                          }

                          if (prodID != null && prodID.trim().length() > 0) {
                                  mline.setProdID(prodID.trim());
                          }
                          
                          
                          if(iscamp) {
                        
                              if (algus  != null && algus.trim().length() > 0  && algus.indexOf("1900")== -1) {
                                  mline.setStartDate(algus.trim());
                          }

                          if (lopp  != null && lopp.trim().length() > 0  && lopp.indexOf("1900")== -1) {
                                  mline.setEndDate(lopp.trim());
                          }
                          
                          if(kuu > 2 ){
                         	 mline.setProdLevel("pr");
                          }  
                          
                          
                          
                      	   if(price > 0 ) {
                   		   mline.setPrice(price);
                              if(price > 0){
                                  lines.add(mline);
                              }
                   	   }
                     	
                   	   if(perc > 0 ) {
                   		   mline.setDiscount(perc);
                              if(perc > 0){
                                  lines.add(mline);
                              }
                   	   }
                   	   
                          }

                       
                     
                	   
                         
                 
                  }
                  
                  rs.close();
                  stmt.close();
                  con.close();
                  
       } catch (SQLException ex) {
       ex.printStackTrace();
       }
       return lines;}
   
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
	  	  

	  String cmd = "  SELECT summ.itemid AS toode , sum(AVAILPHYSICAL) AS stock   FROM INVENTSUM summ \r\n" + 
	  		"			 INNER JOIN inventtable t ON summ.itemid = t.itemid  and  summ.dataareaid=t.dataareaid\r\n" + 
	  		"                 INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid  and  summ.dataareaid= dim.dataareaid \r\n" + 
	  		"                 INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  and  inv.dataareaid= summ.dataareaid \r\n" + 
	  		"                 WHERE summ.DATAAREAID='eee'and dim.DATAAREAID='eee' and inv.DATAAREAID='eee' AND CLOSED = 0 AND  moduletype=0  \r\n" + 
	  		"                   and t.itemgroupid = 'SALD' GROUP BY summ.itemid having sum(AVAILPHYSICAL) != 0 ORDER BY summ.itemid ";
       

	  
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
	           
	           if(seis > 0){
	        	   
	        		   products.append(kood +  ";");
	                   products.append(" ;");
	                   products.append(getParent().getNF().format(seis)   +  "\n"); // + dealer +  "\n");
	           
	            
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

  private void makeCustprod(){
	  int counter = 1;
	  
	  String cmd2 = "SELECT jou.orderaccount,tra.itemid ,sum (tra.qty) as kog\r\n" + 
	  		"	       FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON \r\n" + 
	  		"	       tra.invoiceid=jou.invoiceid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
	  		"	         and  tra.dataareaid= cus.dataareaid  WHERE  jou.invoicedate > getdate()- 30\r\n" + 
	  		"			 AND tra.dataareaid ='eee'  AND jou.dataareaid = 'eee'  AND cus.dataareaid = 'eee' \r\n" + 
	  		"	       GROUP BY jou.orderaccount,tra.itemid \r\n" + 
	  		"	       ORDER BY jou.orderaccount,tra.itemid";
	  
	  
	   String cmd = " SELECT jou.custaccount,tra.itemid ,max(case when tra.salesqty > 0 then tra.salesqty end)  as sisse ,\r\n" + 
	   		"	  min(case when tra.salesqty < 0 then tra.salesqty end ) as puudu,\r\n" + 
	   		"	  jou.deliverydate AS lastdate,max(tra.lineamount/case when tra.salesqty != 0 then tra.salesqty else 1 end) as lastprice \r\n" + 
	   		"	  , packingslipid as lastdoc\r\n" + 
	   		"	    FROM salesline  tra INNER JOIN salestable jou ON \r\n" + 
	   		"	   tra.salesid=jou.salesid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
	   		"	 and  tra.dataareaid= cus.dataareaid  WHERE  jou.deliverydate > getdate()-14\r\n" + 
	   		"	 AND tra.dataareaid ='eee'  AND jou.dataareaid = 'eee'  AND cus.dataareaid = 'eee' \r\n" + 
	   		"	    GROUP BY packingslipid,jou.custaccount,tra.itemid ,jou.deliverydate \r\n" + 
	   		"	    ORDER BY packingslipid,jou.custaccount,tra.itemid ,jou.deliverydate " ;


	    if (isDebug()) System.out.println(cmd);

	    try {
	    	
	    	
	    	Connection con = getNewConnection();
	     	Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd2);
	
	        HashMap<String,Double> cpp = new HashMap<String,Double>();
	        while(rs.next()){
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("kog");
	        cpp.put(kliendikood+"-"+tootekood,kogus);
	     
	      }
	    	
	    	
	    	rs.close();
	    	
	    	
	
	        rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("custaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("sisse");
	        double puudu = 0;
	        try {
	        	puudu = rs.getDouble("puudu");
	        }catch(Exception e) {
	        	
	        }
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double lastprice = rs.getDouble("lastprice");
	                 double l30 = 0;
	                 if(cpp.get(kliendikood+"-"+tootekood) != null) {
	                	 l30 = cpp.get(kliendikood+"-"+tootekood);
	                 }
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus , l30,0,lastdate,lastprice,puudu,0," ");

	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	        prods.addElement(tootekood);
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	      if(counter< 4){
	      	try{
	      		Connection con = getNewConnection();
	            Statement stmt =  con.createStatement();
	            ResultSet	rs = stmt.executeQuery(cmd);
	        	
	             counter++;
	             while(rs.next()){
	               String kliendikood = rs.getString("custaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               double kogus = rs.getDouble("sisse");
	   	        double puudu = 0;
	   	        try {
	   	        	puudu = rs.getDouble("puudu");
	   	        }catch(Exception u) {
	   	        	
	   	        }
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	                        double lastprice = rs.getDouble("lastprice");
	               CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,lastprice,puudu,0," ");
	               custProds.add(cp);
	             }
	             rs.close();
	   	      stmt.close();
	   	      con.close();
	      	}catch (Exception ee){

	      	}

	      }
	    }
  }
  
  public TreeMap getAgents(){
	  
	  Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
    String cmd = "SELECT e.groupid AS kood ,e.name  FROM custtable k INNER JOIN commissionsalesgroup e " +
    		" ON e.groupid=salesgroup  and  k.dataareaid= e.dataareaid " +
	" WHERE e.dataareaid='eee' AND k.dataareaid='eee'  group by e.name ,e.groupid  order by e.name";
    
    
    cmd = "select statisticsgroup as kood, statisticsgroup as name from custtable where dataareaid='eee' and statisticsgroup != ''  group by statisticsgroup";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
    	Connection con = getNewConnection();
        Statement stmt =  con.createStatement();
        ResultSet	rs = stmt.executeQuery(cmd);
    
        while(rs.next()){
        	String agentID = rs.getString("kood").trim();
        	if(agentID.equals("Lääne")) {
           	 agentID = "Laane";
            }
          Agent agent = new Agent(agentID);
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          agent.setLanguage("EST");
          agent.setCountry("et");
          agent.setPassWord("0000");
          String email = " ";
          
          if(propse.get(agent.getID()) != null){
        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
          }
          
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
        
          if(email.length() > 0){
        	  agent.setActive(true);
          }
          
          
          agents.put(name, agent);
          agent1.put(agent.getID(), agent);
        }
     rs.close();
     stmt.close();
     con.close();
         
     Agent agent = new Agent("MERC1");
     
     String name = "MERC1";
     agent.setName(name);
     agent.setIsPilot(true);
     agent1.put("MERC", agent);
     
     
     agent.setLanguage("LAT");
     agent.setCountry("lv");
     agent.setPassWord("0000");
     String email = " ";
     
     if(propse.get(agent.getID()) != null){
   	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
     }
     
     if(email.length() > 3){
   	  agent.setEmail(email);
     }
   
     if(email.length() > 0){
   	  agent.setActive(true);
     }
     agents.put(name, agent);
    } catch (Exception e){
        getParent().showError(e);
      }
    
    return agents;
  }

  private TreeMap agent1 = new TreeMap();
  
  private TreeMap makePlansByCustomers(String fname){
	   TreeMap res = new TreeMap();
	  
	   try{
	
		Calendar cal = Calendar.getInstance();
	     String month = (cal.get(Calendar.MONTH)+1)+ "";
	     if(month.length() == 1){
		   month = "0" + month;
	      }
	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
	   int a = 0;
	   File f = new File (getExcelDir() + fname);
	   FileInputStream	 ff = new FileInputStream(f);
	
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheet(jooksev);
		System.out.println("jooksev " + jooksev + " " + sheet);
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
					String gr1n = null;
				
					
					for(int i = 0 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String cust = null;
						double g1 = 0;
					
						
					if(i == 0){
						gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
						gr1n = "salds";
						
						if(fname.equals("Budget.xlsx")) {
							gr1n = "maize";
						}
					}else{
						
						try {
							cust = (row.getCell((short) 0).getStringCellValue());
 					      } catch (Exception e) {
 					    	 cust =( (int)(row.getCell((short) 0).getNumericCellValue()))+"";
 						  // e.printStackTrace();
 					      }
		
						try {
						g1 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
						} catch (Exception e) {
	 	   					 e.printStackTrace();	
	  					      }
					
					
						
						TreeMap asi= new TreeMap();
						       Toode t1 = new Toode(gr1);
						       t1.setBrand(gr1n);
						       t1.setPlancur(g1);
				          asi.put(gr1, t1);
						
						if(cust != null){
				         res.put(cust,asi );
						}
 					  }
					}
				}
      		   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
	//  System.out.println("kleindi plaanid " + res.toString());
	//  System.exit(0);
	   return res;
 }
  
 TreeMap custplans = null;
  
  
  public TreeMap getCustPlans(){
	  int counter = 1;
	TreeMap res = new TreeMap();
	if(custplans == null) {
		 custplans = this.makePlansByCustomers("Budget.xlsx");	
	}
     


String sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end as grupp ,round(SUM(lineamount),0) AS summa , "
		+ "round(sum(inv.weightkg*sal.qty),2) AS summakg \r\n" + 
		"   FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON  \r\n" + 
		"   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE \r\n" + 
		"   inv.dataareaid='eee'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate()) and jou.orderaccount != '' \r\n" + 
		"   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";

 sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end  as grupp ,round(sum(inv.weightkg*sal.qty),2) AS summakg , "
		+ "  round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa  \r\n" + 
		"  FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON \r\n" + 
		"  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid "
		+ "left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and " + 
		"    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid   WHERE " + 
		" inv.dataareaid='eee'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate())   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";
 



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
if(grupp.equals("leib") ){
                  Toode tt = (Toode)tr.get("1");
                  tt.setDonecur(tt.getDonecur() + sum);
                  }

                }
              }
              rs.close();
              stmt.close();
              con.close();

              
                          
                            int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                            int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                      	  
                      	 // Iterreerime Plaani mappi ja sealme asjad paika 
                      	  
                            
                            
                      	Iterator itera =   custplans.keySet().iterator();
                      	while(itera.hasNext()){
                      		String custID = itera.next() + "";
                      		TreeMap tr = (TreeMap) custplans.get(custID);
                      		
                            if (tr != null){
                      	  

                        
                         String asi2 = "";
                         
                      	  Iterator enume = tr.keySet().iterator();
     	                	 while(enume.hasNext()){
     	                		 String brand = enume.next()+"";
     	                		 if(!brand.equals("allx")){
     	                			Toode tt = (Toode)tr.get(brand);
     	                			int teh = (int)tt.getDonecur();
     	                			if(tt.getPlancur() > 0){
     	                				asi2 = asi2 + ("Gr. " + tt.getBrand() + "    " +
         	                					((int)(teh/tt.getPlancur()*100))
         	                					+ "% Plan.: " +(int) tt.getPlancur() + " €.  Sales.: " + teh + " €.||");	
     	                			}
        	                		;
     	                		   }
     	                		 }
                         res.put(custID, asi2 );
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

  
  
  private HashSet getTopVisit(){
	  HashSet hh = new HashSet();

	   Date now = new Date();
		  File f = new File(this.getExcelDir() + "priority.xlsx");
		 
		  try {
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheetAt(0);
				XSSFRow row = null;
			int currMont = Calendar.getInstance().get(Calendar.MONTH)+2;
				for (int i = 3; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String custID = "";
					Date start = null;
					Date end = null;
			
					try {
							custID = (row.getCell((short) 0).getStringCellValue());
						
						} catch (Exception e) {
							try {
								custID = row.getCell((short) 0).getStringCellValue();
							 } catch (Exception u) {
								custID = (int) (row.getCell((short) 0)
										.getNumericCellValue()) + "";
							}
						}
						
						try {
							start = (row.getCell((short) 2).getDateCellValue());
							end = (row.getCell((short) 3).getDateCellValue());
							
						   } catch (Exception e) {
							//e.printStackTrace();
						  }
						
						if(start != null && end != null && start.before(now) && end.after(now)) {
							hh.add(custID);
						  } else if(start == null && end == null) {
								hh.add(custID);
						  }
						
				   	}
		      } catch (Exception d) {
				 d.printStackTrace();
			  }
		  System.out.println(" prior  " + hh.toString());
		return hh;
	}


  
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  
  public TreeMap getCustomers(Agent agent){
	  nf.setMinimumFractionDigits(2);
	  TreeMap budgets = getCustPlans();
	//  TreeMap openOrders = new TreeMap();
	   HashSet diff = getTopVisit();
	  TreeMap displ = getDisplayInfo();
	  try{
    	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
    		
    }catch(Exception e){
    	e.printStackTrace();
    }
	  makeCustprod();
	  
	  
 String cmd = " SELECT cus.name,cus.namealias,cus.accountnum,(case when len(cus.regnum_w)=0 then cus.accountnum else cus.regnum_w end )AS payer, regnum_w as regno, " +
              "0 AS debt, cus.linedisc , cus.pricegroup,  " +
               "   aad.address, '' AS visit ,statisticsgroup  AS agent ,  numofdays AS term, blocked AS block , " + //salesgroup AS agent
			  " (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount)  "+
			  " as payerblock ,cus.phone  as info , aad.city, aad.street FROM custtable cus LEFT JOIN paymterm  pay on cus.paymtermid=pay.paymtermid and  cus.dataareaid= pay.dataareaid  " +
			  " LEFT JOIN ADDRESS aad ON aad.addrrecid=cus.recid and aad.dataareaid= 'eee'   and  cus.dataareaid= aad.dataareaid" +
			"	 WHERE   (cus.dataareaid='eee' AND blocked != 2 and  len(salesgroup)>0  )  " + 
			 "   order by cus.name" ;
 
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	
    	Connection con = this.getNewConnection();
    	
      	
    	TreeMap<String,Double> debts = new TreeMap<String ,Double>();
    	
    	 String cmd2 = "  SELECT sum(amountmst) as summa , cus.regnum_w as payer FROM custtransopen co INNER JOIN custtable cus on  cus.accountnum = co.accountnum \r\n" + 
    	 		"   and cus.dataareaid = co.dataareaid\r\n" + 
    	 		"    	 		 WHERE co.duedate<getdate() and co.dataareaid = 'eee' and cus.regnum_w != '' group by cus.regnum_w \r\n" + 
    	 		"				 union \r\n" + 
    	 		"   SELECT sum(amountmst) as summa , cus.accountnum as payer FROM custtransopen co INNER JOIN custtable cus on  cus.accountnum = co.accountnum \r\n" + 
    	 		"   and cus.dataareaid = co.dataareaid\r\n" + 
    	 		"    	 		 WHERE co.duedate<getdate() and co.dataareaid = 'eee'  group by cus.accountnum"; 
    	 		
    	 
    	 Statement stmt = con.createStatement();
           ResultSet rs1 = stmt.executeQuery(cmd2);
                     while(rs1.next()){
                       double volg = rs1.getDouble("summa");
                       String pay = rs1.getString("payer").trim();
                       debts.put(pay, volg);
                     }
                    rs1.close();
                    stmt.close();
    	
    	
    	 stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
 
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        
        
        String accountnum = rs.getString("accountnum").trim();
       
        Customer cust = new Customer(accountnum);
          if(accountnum.startsWith("C40")) {
        	  cust.diffBack2 = true;
          }
        cust.setPayer(rs.getString("payer"));
        boolean de = true;
        
        String regno  = rs.getString("regno");
        if(regno != null && regno.trim().length() > 0 && 
        		(regno.trim().equals("10714195") || regno.trim().equals("10180925") // abc + circle
        				|| regno.trim().equals("10765896") || regno.trim().equals("10263574") // maxima + rimi
        				|| regno.trim().equals("10379733")  )) { // selver + 
        	de = false;
        }
        
      
        	
        if(de) {
        	if(debts.get(cust.getPayer()) != null) {
        		// cust.setDebt(debts.get(cust.getPayer()));
        	}else if(debts.get(cust.getID()) != null) {
        		// cust.setDebt(debts.get(cust.getID()));
        	}
        }
        

       // cust.addToCustDataVec(cust.getID());
        
        
        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String pricegroup = rs.getString("pricegroup");
        if(pricegroup != null && pricegroup.trim().length()>0){
        	
       if(!pricegroup.trim().equals("GENERAL")) {
    	   
         if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
        	 cust.setGroup(cust.getGroup() + "|" +pricegroup.trim());
         } else {
        	 cust.setGroup(pricegroup.trim());
         }
       }
         
        } 
        
        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ').replaceAll("Estonia", " ").trim()), "Estonia"));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        
        cust.restName = name;
        
        name = name + " " + cust.getAddress() + " " + accountnum ;
        String budg = "";
        if(budgets.get(accountnum) != null){
        	budg = budgets.get(accountnum) + "";
        	
        }
        
        
        if(budg.length() > 1) {
        	//cust.addToCustDataVec(accountnum+ ";custbudget; ;" + budg);
        }
        
        cust.setName(name );
        
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
        	String agentID = agen.trim();
        	if(agentID.equals("Lääne")) {
           	 agentID = "Laane";
            }
          cust.addAgent(new Agent(agentID));
    
          Agent aa1 = (Agent)agent1.get(agentID);
      	if(aa1 != null) {
      	
  		Vector vec = aa1.getDubbler();
  		if(vec!= null && vec.size() >0){
  			Enumeration en = vec.elements();
  			while(en.hasMoreElements()){
  				String age = (String) en.nextElement();
  				cust.addAgent(new Agent(age));
  			}
  		}
      }
      	
        }
        
        
    

        
        int term = rs.getInt("term");
        if (term > 0) {
       //   cust.setPaymTerm(term + "");
        }
        if(de) {
        int block = rs.getInt("block");
        if (block == 1 || block == 2) {
         // cust.setSalesBlock(true);
        }
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 2) {
         // cust.setSalesBlock(true);
        }
        }
        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData("Tel:" + info.trim());
        }

        
        
        

        String city  = rs.getString("city");
        if (city != null ) {
         	cust.restCity = city.trim();
        }
        String street  = rs.getString("street");
        if (street != null ) {
        	if(street.contains(",")) {
        		cust.restCity = street.trim().substring(0,street.trim().indexOf(","));
        		cust.restStreet = street.trim().substring(street.trim().indexOf(",")+1);
        	}else {
        		cust.restStreet = street.trim();	
        	}
         	cust.setCountry("Estonia");
        }
        
        if(displ.get(cust.getID()) != null) {
        	cust.setInfo(displ.get(cust.getID())+"");
        }
        
        if(diff.contains(cust.getID())) {
     	   cust.diffBack = true;
        }
        
        customers.put(name, cust);
        custMap.put(cust.getID(),cust);
      }

  /*    String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
               "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid  and  co.dataareaid= a.dataareaid " +
               "WHERE co.dataareaid='eee' AND a.dataareaid='eee' and ltrim(a.accountnum) not in ('511' , '109') ORDER BY a.accountnum,co.transdate ";

      sql = "SELECT  a.accountnum AS klient, regnum_w as payer ,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, \r\n" + 
      		"               co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid  and  co.dataareaid= a.dataareaid \r\n" + 
      		"			   INNER JOIN custtable cus ON  a.accountnum  = cus.accountnum and  cus.dataareaid= a.dataareaid \r\n" + 
      		"               WHERE co.dataareaid='eee' AND a.dataareaid='eee' and ltrim(cus.regnum_w) not in ('10714195' , '10180925' , '10765896' , '10263574' , '10379733') ORDER BY a.accountnum,co.transdate" ;
    
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String pay = rs.getString("payer");
                        if(pay != null && pay.trim().length() > 0) {
                        	debt.setPayerID(pay.trim());	
                        }
                        
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        
                        

                      //  this.debt.addElement(debt);
                      }*/

  rs.close();
  stmt.close();
  con.close();
    } catch (Exception e){
      getParent().showError(e);
      System.exit(0);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

	   Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = " SELECT id as liik,name as nimetus FROM ASTTELEMAITEMGROUP  where dataareaid ='eee' ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String nimi = rs.getString("nimetus").trim();
	      nimi = nimi.replaceAll("Leivad", "Lei");
	      nimi = nimi.replaceAll("Saiad", "Sai");
	        tm.put(liik, Converter.enCodeEst(nimi));
	    }
	    rs.close();
	    st.close();
	    con.close();
return tm;
	   
	  
   
}


  public TreeMap getProdGroupsSecond() throws SQLException {
	  
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = "    SELECT astuppersegment as liik,astuppersegment as nimetus FROM inventtable  \r\n" + 
	    		"				   where dataareaid ='eee' and astuppersegment !='' group by astuppersegment ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
	    }
	    rs.close();
	    st.close();
	    con.close();

	    return tm;
		 

  }

  private Vector prodcodes = null;
  
  public TreeMap getProducts(Agent agent){
	  prodcodes = new Vector();
    String cmd = "   SELECT inv.itemid,(inv.itemname) AS nimetus, qtyinbox AS kastis,  mod.price AS price, '' AS ean,asttelemaitemgroupid AS grupp ,\r\n" + 
    		"    		 itemgroupid AS grupp2,mod.unitid AS unit FROM inventtable inv \r\n" + 
    		"    		   		                 INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid  and  mod.dataareaid= inv.dataareaid \r\n" + 
    		"    		    		                       INNER JOIN inventitemsalessetup bar ON bar.itemid = inv.itemid  and  bar.dataareaid= inv.dataareaid\r\n" + 
    		"    		    							     WHERE   inv.dataareaid='eee' AND mod.dataareaid='eee'   AND moduletype=2  AND ASTITEMFORSALE = 1  \r\n" + 
    		"    		    		                            AND len(inv.itemname)>0   and inv.itemid not like ( '%TP') and inv.itemid not like ( '%EX') \r\n" + 
    		"													and bar.stopped =0  order by inv.itemid "  ;

             String cmd2 = "  SELECT summ.itemid AS toode , sum(AVAILPHYSICAL) AS stock , max(dim.inventlocationid) as ladu  FROM INVENTSUM summ \r\n" + 
             		"			 INNER JOIN inventtable t ON summ.itemid = t.itemid  and  summ.dataareaid=t.dataareaid\r\n" + 
             		"                 INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid  and  summ.dataareaid= dim.dataareaid \r\n" + 
             		"                 INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  and  inv.dataareaid= summ.dataareaid \r\n" + 
             		"                 WHERE summ.DATAAREAID='eee'and dim.DATAAREAID='eee' and inv.DATAAREAID='eee' AND CLOSED = 0 AND  moduletype=0  \r\n" + 
             		"                   and t.itemgroupid = 'SALD' GROUP BY summ.itemid having sum(AVAILPHYSICAL) != 0 ORDER BY summ.itemid ";
        
             String cmd3 = "select itemid ,itembarcode as code from inventitembarcode where dataareaid= 'eee'  and barcodesetupid='ean' "+	
		"	";

             
             String cmd4 ="  SELECT  pd.itemrelation as toode , percent1 as ale  FROM pricedisctable pd  where  pd.dataareaid = 'eee'  " +   		 
			  " and  pd.fromdate<getdate() and pd.todate>getdate()  AND pd.fromdate!=('1900-01-01 00:00:00.00') and accountrelation = 'VairumAtl' " +
			  " group by  pd.itemrelation ,percent1 order by  pd.itemrelation " ;
    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap stocks = new TreeMap();
    TreeMap costs = new TreeMap();
    TreeMap maxdiscs = new TreeMap();
    
    try {
        Connection con =   this.getNewConnection();
        Statement stmt = con.createStatement();
    	ResultSet rs1 = stmt.executeQuery(cmd4);
        while(rs1.next()){
          String id = rs1.getString("toode").trim();     
          maxdiscs.put(id,rs1.getDouble("ale")+"");
           
          }
        rs1.close();
        stmt.close();
    	System.out.println("Suurus maxdiscs " + maxdiscs.size());
    	
    	
     stmt =  con.createStatement();
    	
        rs1 = stmt.executeQuery(cmd2);
        while(rs1.next()){
          String id = rs1.getString("toode").trim();     
            stocks.put(id,rs1.getDouble("stock")+"");
           // costs.put(id, rs1.getDouble("cost")+"");
          }
        rs1.close();
    	
    	
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst(Converter.firstToUpperCase(rs.getString("nimetus").trim())) + " " +kood;
        String ette = "";
     if(maxdiscs.get(kood)!= null){
    	 int aa = (int) Double.parseDouble(maxdiscs.get(kood) + "");
    	 prod.setMaxDiscount(aa);
    	// System.out.println("MaxDisc "+ prod.getMaxDiscount());
    	 ette = "%"+aa + " ";
     }
    

        prod.setInCases(rs.getDouble("kastis"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
       // prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        prod.setGroup(grupp.trim());
       // nimetus =  "    " + nimetus ;
     }

      prod.setName(ette + (nimetus.trim()));


     String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>0){
             prod.setGroupSecond(grupp2);
          }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                prod.setUnit(unit);
             }

               if(stocks.get(prod.getID()) != null){
            	   prod.setStock(Double.parseDouble((String)stocks.get(prod.getID())));
               }
               
               double oh = 0;
               
               if(costs.get(prod.getID()) != null){
            	   oh =(Double.parseDouble((String)costs.get(prod.getID())));
               }
               
             	
               
            	if(oh>0){
            		double kate = (1-oh/prod.getPrice())*100;
                	if(kate>0){
                		String kat = (kate + "");
//                        prod.setMargin(kat.substring(0,kat.indexOf(".")));   	
                	//	System.out.println(prod.getName() + "'" + prod.getMargin() + "'");
                	}	
            	}

               
               
               prod.setVat("21");
 //  if(prod.getStock() > 1 || (prods.contains(prod.getID()) && prod.getStock() >0 && prod.getStock() < 3) ){
    products.put(nimetus, prod);
    prodHash.put(prod.getID(), prod);
    prodcodes.add(prod.getID());
   
   //}

      }
      rs.close();
      
      
      rs1 = stmt.executeQuery(cmd3);
      while(rs1.next()){
        String id = rs1.getString("itemid").trim();     
        String code = rs1.getString("code").trim();
         Product prod1 = (Product)prodHash.get(id);
         if (prod1 != null){
            if(prod1.getEAN()== null || prod1.getEAN().trim().length()== 0){
            	prod1.setEAN(code);	
              }else{
                prod1.addBarcode(code);
             }
           }
      }
    rs1.close();
    stmt.close();
    con.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  
  private TreeMap brandplans = null; 

  
  public void setBulkSales(Map agents){
   this.connectionUp();
 
    brandplans = this.makePlans("Budget.xlsx");
 

 String cmd =" SELECT  cus.statisticsgroup as agent ,round(sum(inv.weightkg*sal.salesqty),2) AS summakg , \r\n" + 
 		" 					 round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa ,\r\n" + 
 		" 					 round(sum(lineamount ),0) AS summa2 \r\n" + 
 		" 					  FROM salesline sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN salestable jou with (nolock) ON  \r\n" + 
 		" 		 		  sal.salesid=jou.salesid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.custaccount and jou.dataareaid= cus.dataareaid \r\n" + 
 		" 				   left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and \r\n" + 
 		" 				    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid\r\n" + 
 		" 				  WHERE inv.dataareaid='eee'  AND YEAR(jou.deliverydate) = YEAR(getdate())  \r\n" + 
 		" 		 		   AND MONTH(jou.deliverydate) = MONTH(getdate()) AND DAY(jou.deliverydate) <= DAY(getdate())   and cus.statisticsgroup != ''  and inv.asttelemaitemgroupid not in ('290','400') \r\n" + 
 		" 				    GROUP BY cus.statisticsgroup";


        if (isDebug()) System.out.println(cmd);
           try {
           	
           	Connection con = this.getNewConnection();
               Statement stmt =  con.createStatement();
               ResultSet	      rs = stmt.executeQuery(cmd);
               while(rs.next()) {
                 String agentID = rs.getString("agent").trim();
                 if(agentID.equals("Lääne")) {
                	 agentID = "Laane";
                 }
                 Agent agent = (Agent)agents.get(agentID);
                 
                 int kgs = (int)rs.getDouble("summakg");
                 int sum = (int)rs.getDouble("summa");
                
                 if (agent != null && brandplans.get(agentID) != null){
                   Toode tt = (Toode)brandplans.get(agentID);
                   tt.setDonecur(tt.getDonecur() + sum);
                   tt.setDonevol(tt.getDonevol() + kgs);
                   }
               }
               
               rs.close();
               stmt.close();
               con.close();
            
                           
                             int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                             int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                          
                       	 // Iterreerime Plaani mappi ja sealme asjad paika 
                       	  
                       	Iterator itera =   agents.keySet().iterator();
                       	while(itera.hasNext()){
                       		String agentID = itera.next() + "";
                       		Agent aa = (Agent)agents.get(agentID);
                       		
                          String asi2 = "";
                          
                          if(brandplans.get(aa.getID()) != null){
      	                	Toode tt =   (Toode)brandplans.get(aa.getID());
      	                	
      	                			int teh = (int)tt.getDonecur();
      	                			int kgs = (int)tt.getDonevol();
      	                	  		asi2 = asi2 + (((int)(teh/tt.getPlancur()*100))+ "% Plan.: " +(int) tt.getPlancur() + " €  Sales.: " + teh + " €||");
      	                	  	   asi2 = asi2 + (((int)(kgs/tt.getPlanvol()*100))+ "% Plan.: " +(int) tt.getPlanvol() + " kg.  Sales.: " + kgs + " kg.||");
      	                		   }
      	                		  
      	                	    
                          
                          aa.setStart( Converter.enCodeEst(asi2));
                        
                       	}
           } catch (Exception e) {
               getParent().showError(e);
            }
         }


   double totalPlanSum = 0;
   
   
   
   private TreeMap makePlans(String fname){
 	   TreeMap res = new TreeMap();
 	      totalPlanSum = 0;
 	   try{
 		
 	   Calendar cal = Calendar.getInstance();
 	   String month = (cal.get(Calendar.MONTH)+1)+ "";
 	   if (month.length() == 1){
 		   month = "0" + month;
 	   }
 	   String jooksev = cal.get(Calendar.YEAR) + "-" + month; 
 	   int a = 0;
 	   File f = new File (getExcelDir() + fname);
 		  FileInputStream	 ff = new FileInputStream(f);
 	
 		XSSFWorkbook workbook = new XSSFWorkbook((ff));
 		XSSFSheet sheet = workbook.getSheet(jooksev);
 		System.out.println("jooksev " + jooksev + " " + sheet);
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
 					String gr1n = null;
 					
 					for(int i = 8 ; i < a + 1;i++){ // 9ndast reas hakkame scrollima nii kilosid kui ka summat
 						 row = sheet.getRow(i);
 						String agent = null;
 						double g1 = 0;
 						
 						
 						try {
 							agent = (row.getCell((short) 1).getStringCellValue());
 						
    					      } catch (Exception e) {
    					    	  System.out.println("agent ei  " + agent);
    						 //  e.printStackTrace();
    					      }
 			    
 						try {
 						g1 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
 						
 						} catch (Exception e) {
 	   					 e.printStackTrace();	
  					    }
 						
 					
 						if(agent != null) {
 							i++;
 							 row = sheet.getRow(i);
 							 double g2 = 0;
 							
 							 try {
 		 						g2 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
 		 						} catch (Exception e) {
 		 	   					 e.printStackTrace();	
 		  					    }
 							 
 							 
 						TreeMap asi= new TreeMap();
 				        Toode t1 = new Toode(gr1);
						       t1.setPlanvol(g2);
						       t1.setPlancur(g1);
						       System.out.println("agent " + agent + " " + g1 + " " + g2);
					    res.put(agent, t1);
 					  }
 					}
 						
 					}
 				
 				
         		   
 	   }catch (Exception e){
 		   e.printStackTrace();
 	   }
 	   
 	//   System.out.println("totalPlanSum "+ totalPlanSum );
 	   return res;
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
    return "AxaFazerLV";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){

  	    String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa " +
       " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON " +
       " sal.invoiceid=jou.invoiceid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE len(talinventstatistics1num) >0 AND " +
       " inv.dataareaid='tal' AND sal.dataareaid='tal' AND cus.dataareaid='tal' AND cus.talsalesman='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())" +
       " GROUP BY talinventstatistics1num  ORDER BY talinventstatistics1num ";
  	  StringBuffer sb = new StringBuffer();

  	  System.out.println(sql);
  	 try{
       Connection con =  this.getNewConnection();
    	Statement stmt = con.createStatement();
  	    ResultSet	      rs = stmt.executeQuery(sql);

  	          sb.append("GrupiNimi     kilodes ja EUR\n");
  	          sb.append("---------------------------------------------------\n");

  	          double totalKokku = 0d;
                    double totalSumma = 0d;

  	          while (rs.next()) {
  	            sb.append(rs.getString("grupp").trim() + "\n");
  	          	int kogus = rs.getInt("kogus");
                            int summa = rs.getInt("summa");
  	            totalSumma = round(totalSumma + summa, 2);
  	            totalKokku = round(totalKokku + kogus, 2);
  	            sb.append("\t" + kogus + "\t" + summa + " EUR\n\n");
  	           }
  	          rs.close();
  	          stmt.close();
  	          con.close();

  	          sb.append("---------------------------------------------------\n");
  	          sb.append("\tKuu m��k kokku: " + totalKokku + " Kg." + totalSumma + " EUR");


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
  private String url = "jdbc:odbc:axa";

  private   Vector debt = new Vector();

  private Vector custProds = new Vector();
  private String subjectFilesTo = null;
  private Vector prods = new Vector();
    private NumberFormat NF = NumberFormat.getInstance();
}
