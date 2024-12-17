package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jhlabs.image.FieldWarpFilter.Line;

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

public class AxaFazerLVDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter,WebReporter {
  public AxaFazerLVDBDriver(EksportPanel parent){
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

  /**
   *
   */
  public void connectionUp(){
  }
  
  

	public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		
			
			
			
//			paneme tuleviku siia ette 
			String  sqlt = "SELECT jou.salesid,jou.custaccount,tra.itemid ,tra.name ,tra.salesqty,jou.deliverydate AS lastdate,(tra.lineamount/case when tra.salesqty != 0 then tra.salesqty else 1 end) as lastprice   \r\n" + 
					"	 			       FROM salesline  tra INNER JOIN salestable jou ON  \r\n" + 
					"	 			       tra.salesid=jou.salesid  and  tra.dataareaid= jou.dataareaid  WHERE  jou.deliverydate between getdate()and getdate()+2 \r\n" + 
					"	 				  	  AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  \r\n" + 
					"	 			        ORDER BY 2,3 ,6 ,5";

  
	        Connection con3 = getNewConnection();
			Statement st3 = con3.createStatement();
			ResultSet rs3 = st3.executeQuery(sqlt);
			boolean hasTom = false;
			while (rs3.next()) {
				hasTom = true;
				MatrixLine ml = new MatrixLine();
				
				String dok = rs3.getString("salesid").trim();
	         	String kl = rs3.getString("custaccount").trim();
	         	String tootekood = rs3.getString("itemid").trim();
	         	double kogus = rs3.getDouble("salesqty");
	         	String delyveryD = rs3.getString("lastdate").substring(0,10);
	         	
				
				ml.setCustID(kl);
				ml.setProdID(tootekood);
				ml.setPrice(kogus);
				ml.setStartDate(delyveryD.substring(5,10));
				ml.setLineinfo(dok + " ("+ (int)0 + "€)" );
				ml.setCliGroup("green");
				result.add(ml);
			}
			rs3.close();
			st3.close();
			String asi ="10";
			if(hasTom){
				asi = "9";
			}
			

					String sq3 = "SELECT jou.orderaccount as klient, jou.invoicedate  as kuup , tra.itemid as toode ,sum(tra.qty) as kogus,max(invoiceamount) as summa, jou.invoiceid \r\n" + 
							"														as doknr  FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON \r\n" + 
							"														       tra.invoiceid=jou.invoiceid  and  tra.dataareaid= jou.dataareaid \r\n" + 
							"															  WHERE  jou.invoicedate >=  \r\n" + 
							"															  (select top 1 kuupaev  from ( 	 \r\n" + 
							"																	 SELECT top " + asi + " jou2.invoicedate as kuupaev\r\n" + 
							"															       FROM  custinvoicejour jou2  where ltrim(jou2.orderaccount) = ltrim(jou.orderaccount)  and jou2.dataareaid= 'lve' \r\n" + 
							"																   and jou2.invoicedate > getdate()-60  group by jou2.orderaccount , jou2.invoicedate order by jou2.invoicedate desc) as dd order by kuupaev) \r\n" + 
							"															    AND tra.dataareaid ='lve'\r\n" + 
							"														               GROUP BY jou.orderaccount,jou.invoiceid,jou.invoicedate , tra.itemid\r\n" + 
							"																	   union\r\n" + 
							"						SELECT jou.orderaccount as klient, jou.deliverydate  as kuup , tra.itemid as toode ,sum(tra.qty) as kogus,max(0) as summa, jou.PackingSlipId  \r\n" + 
							"																				as doknr  FROM CustPackingSlipTrans  tra INNER JOIN CustPackingSlipJour jou ON \r\n" + 
							"																					       tra.PackingSlipId=jou.PackingSlipId  and  tra.dataareaid= jou.dataareaid  \r\n" + 
							"																						  WHERE  jou.deliverydate >=  \r\n" + 
							"																						  (select top 1 kuupaev  from ( 	 \r\n" + 
							"																								 SELECT top 6 jou2.deliverydate as kuupaev\r\n" + 
							"																						       FROM  CustPackingSlipJour jou2  where ltrim(jou2.orderaccount) = ltrim(jou.orderaccount)  and jou2.dataareaid= 'lve' \r\n" + 
							"																							   and jou2.deliverydate > getdate()-20  group by jou2.orderaccount , jou2.deliverydate order by jou2.deliverydate desc) as dd order by kuupaev)  \r\n" + 
							"																						    AND tra.dataareaid ='lve'\r\n" + 
							"																					               GROUP BY jou.orderaccount,jou.PackingSlipId,jou.deliverydate , tra.itemid \r\n" + 
							"																								   ORDER BY 1,2 desc ,6 desc, 3" ;
					
				 
					 st3 = con3.createStatement();
					 rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(rs3.getString("kuup").substring(5,10));
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						double summa = rs3.getDouble("summa");
						ml.setLineinfo(rs3.getString("doknr").trim() + " ("+ (int)summa + "€)" );
					    
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
   Vector lines = new Vector();
			try {
				
				
				String cmd = "SELECT jou.custaccount as clGroup,tra.itemid as tootekood FROM salesline  tra INNER JOIN salestable jou ON \r\n" + 
						"			   tra.salesid=jou.salesid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
						"		    and  tra.dataareaid= cus.dataareaid  WHERE  jou.deliverydate > getdate()-11 \r\n" + 
						"		 AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  AND cus.dataareaid = 'lve' and\r\n" + 
						"	 ltrim(rtrim(jou.invoiceaccount)) in ('511','109' ,'791')   \r\n" + 
						"	GROUP BY jou.custaccount,tra.itemid   ORDER BY jou.custaccount,tra.itemid";
				//Vaata kettide koodid 
				//'511','109' ,'791'
				//Max,rim.mego
				System.out.println(cmd);

				Connection con = getNewConnection();

				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(cmd);
				while (rs.next()) {
					
					String clGroup = " ";
					String prodID = " ";
					MatrixLine mline = new MatrixLine();

					clGroup = rs.getString("clGroup");
					prodID = rs.getString("TooteKood");

					if (clGroup != null && clGroup.trim().length() > 0) {
						mline.setCliGroup(clGroup.trim());
					}

					if (prodID != null && prodID.trim().length() > 0) {
						mline.setProdID(prodID.trim());
					}
					
					lines.add(mline);

				}

				rs.close();
				stmt.close();
				con.close();
			} catch (Exception e) {
				getParent().showError(e);
			}
			System.out.println("Mlines fixed " + lines.size());
			
       return lines;
   }

   public Vector getPriceMatrixData() {
       Vector lines = new Vector();
try {

    String sql4a = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus " +
    		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp ,PRI.RELATION , astcampaignid as kamp2 " +
" FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
"  WHERE PRI.DATAAREAID='lve' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
"    (GETDATE() between PRI.FROMDATE AND PRI.TODATE+1)  and pri.AMOUNT > 0.01  and astcampaignid != '' " +
"   ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation " ;
    
  
                         
    Connection con = getNewConnection();
 	Statement stmt = con.createStatement();
    
         ResultSet  rs = stmt.executeQuery(sql4a);

           while (rs.next()) {

                   String clGroup = Converter.cleanLitLatEst(rs.getString("grupp"));
                   String prodID = rs.getString("toode");
                   double price = rs.getDouble("hind");

                   String algus = rs.getString("algus").substring(0,10);
                   String lopp = rs.getString("lopp").substring(0,10);
                   int kuu = rs.getInt("kamp");
                   int relation = rs.getInt("RELATION");
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
                 
                   
                   if(goes){
                  	
                   if(relation == 5 ){
                   }else{
                  	 mline.setPrice(price);
                       if(price > 0){
                           lines.add(mline);
                       }
                   }
                   
                   }
           }
           
           rs.close();

           
           
           String sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus " +
           		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp ,PRI.RELATION , astcampaignid as kamp2 " +
       " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
       "  WHERE PRI.DATAAREAID='lve' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
       "    ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
       "  or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
       "  or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
       "  or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and astcampaignid = '' " +
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
                 
                   
                   if(goes){
                  	
                   if(relation == 5 ){
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
           		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp,  astcampaignid as kamp2 " +
" FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
" WHERE PRI.DATAAREAID='lve' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0  and " +
"   ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) "+
" or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
" or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
" or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 " +
"    ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation" ;
                  
                  rs = stmt.executeQuery(sql4);

                  while (rs.next()) {

                          String custID = Converter.cleanLitLatEst(rs.getString("grupp"));
                          String prodID = rs.getString("toode");
                          double price = rs.getDouble("hind");

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
                          }

                          boolean goes = true;
                          
                          
                          if (goes){
                              mline.setPrice(price);
                              lines.add(mline);
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
	  		"                 WHERE summ.DATAAREAID='lve'and dim.DATAAREAID='lve' and inv.DATAAREAID='lve' AND CLOSED = 0 AND  moduletype=0  \r\n" + 
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
	  		"	  			       FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON \r\n" + 
	  		"	  			       tra.invoiceid=jou.invoiceid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
	  		"	  			         and  tra.dataareaid= cus.dataareaid  WHERE  jou.invoicedate > getdate()- 30 \r\n" + 
	  		"	  					 AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  AND cus.dataareaid = 'lve' \r\n" + 
	  		"	  			       GROUP BY jou.orderaccount,tra.itemid \r\n" + 
	  		"	  			      union\r\n" + 
	  		"					  SELECT jou.orderaccount,tra.itemid ,sum (tra.qty) as kog\r\n" + 
	  		"	  			       FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON \r\n" + 
	  		"	  			       tra.invoiceid=jou.invoiceid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
	  		"	  			         and  tra.dataareaid= cus.dataareaid  WHERE  jou.deliverydate > getdate()- 30 \r\n" + 
	  		"	  					 AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  AND cus.dataareaid = 'lve' \r\n" + 
	  		"	  			       GROUP BY jou.orderaccount,tra.itemid \r\n" + 
	  		"					   ORDER BY 1,2";
	  
	  
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate,max(tra.lineamount/case when tra.qty != 0 then tra.qty else 1 end) as lastprice \r\n" + 
	   		"	       FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON \r\n" + 
	   		"	       tra.invoiceid=jou.invoiceid  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
	   		"	         and  tra.dataareaid= cus.dataareaid  WHERE  jou.invoicedate > getdate()- (case when ltrim(itemgroupid) = 'SALD' then 120 else 30 end ) \r\n" + 
	   		"			 AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  AND cus.dataareaid = 'lve' \r\n" + 
	   		"	       GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty \r\n" + 
	   		"	      union\r\n" + 
	   		"SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.deliverydate AS lastdate,max(tra.lineamount/case when tra.qty != 0 then tra.qty else 1 end) as lastprice \r\n" + 
	   		"	       FROM CustPackingSlipTrans   tra INNER JOIN CustPackingSlipJour  jou ON \r\n" + 
	   		"	       tra.PackingSlipId=jou.PackingSlipId  and  tra.dataareaid= jou.dataareaid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid \r\n" + 
	   		"	         and  tra.dataareaid= cus.dataareaid  WHERE  jou.deliverydate > getdate()- (case when ltrim(cus.itemgroupid) = 'SALD' then 120 else 30 end ) \r\n" + 
	   		"			 AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  AND cus.dataareaid = 'lve' \r\n" + 
	   		"	       GROUP BY jou.orderaccount,tra.itemid ,jou.deliverydate ,tra.qty \r\n" + 
	   		"	       ORDER BY 1,2,3,5 " ;


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
	        String kliendikood = rs.getString("orderaccount").trim();
	        String tootekood = rs.getString("itemid").trim();
	        double kogus = rs.getDouble("qty");
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double lastprice = rs.getDouble("lastprice");
	                 double l30 = 0;
	                 if(cpp.get(kliendikood+"-"+tootekood) != null) {
	                	 l30 = cpp.get(kliendikood+"-"+tootekood);
	                 }
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus , l30,0,lastdate,lastprice,0,0," ");

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
	               String kliendikood = rs.getString("orderaccount").trim();
	               String tootekood = rs.getString("itemid").trim();
	               String lastdate = rs.getString("lastdate").substring(0,10);
	                        lastdate = lastdate.substring(5,10);
	                        double lastprice = rs.getDouble("lastprice");
	               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,lastprice,0,0," ");
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
	    
    String cmd = "SELECT e.groupid AS kood ,e.name from  commissionsalesgroup e " +
    		
	" WHERE e.dataareaid='lve'   group by e.name ,e.groupid  order by e.name";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
    	Connection con = getNewConnection();
        Statement stmt =  con.createStatement();
        ResultSet	rs = stmt.executeQuery(cmd);
    
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
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
  
  private TreeMap getDisplayInfo() {
	  TreeMap res = new TreeMap();
	  
	   try{
		   
	
	
	   int a = 0;
	   File f = new File (getExcelDir() + "displayinfo.xlsx");
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
									cust = (row.getCell((short) 0).getStringCellValue());
		 					      } catch (Exception e) {
		 					    	 try {
		 					    		 cust =( (int)(row.getCell((short) 0).getNumericCellValue()))+"";
		 					    	 }catch(Exception o) {
		 					    		 
		 					    	 }
		 					    	
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
		 					    	 try {
		 					    		 text = Converter.enCodeEst(( (int)(row.getCell((short) 2).getNumericCellValue()))+"");
		 					    	 } catch (Exception u) {
		 					    		 
		 					    	 }
		 					    	
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
								if(cust != null && algus != null && lopp != null && text != null &&
										cc.getTimeInMillis() > algus.getTime() && cc.getTimeInMillis() < lopp.getTime()) {
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
 TreeMap custplansmagus = null;
  
  
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
		"   inv.dataareaid='lve'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate()) and jou.orderaccount != '' \r\n" + 
		"   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";

 sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end  as grupp ,round(sum(inv.weightkg*sal.qty),2) AS summakg , "
		+ "  round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa  \r\n" + 
		"  FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON \r\n" + 
		"  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid "
		+ "left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and " + 
		"    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid   WHERE " + 
		" inv.dataareaid='lve'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
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
     	                				asi2 = asi2 + ("Gr " + tt.getBrand() + "    " +
         	                					((int)(teh/tt.getPlancur()*100))
         	                					+ "% Plan: " +(int) tt.getPlancur() + " €  Sales: " + teh + " €||");	
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

  public TreeMap getCustPlansMagus(){
	  int counter = 1;
	TreeMap res = new TreeMap();
	if(custplansmagus == null) {
		 custplansmagus = this.makePlansByCustomers("Budget_sweet.xlsx");	
	}
     

String sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end as grupp ,round(SUM(lineamount),0) AS summa , round(sum(inv.weightkg*sal.qty),2) AS summakg \r\n" + 
		"   FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON  \r\n" + 
		"   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE \r\n" + 
		"   inv.dataareaid='lve'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate()) and jou.orderaccount != '' \r\n" + 
		"   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";



sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end  as grupp ,round(sum(inv.weightkg*sal.qty),2) AS summakg , "
		+ "  round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa  \r\n" + 
		"  FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON \r\n" + 
		"  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid "
		+ "left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and " + 
		"    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid   WHERE " + 
		" inv.dataareaid='lve'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate())   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";
 

          try {
          	
          	Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(sqlo);
              while(rs.next()) {
                String custID = rs.getString("agent").trim();
                String grupp = rs.getString("grupp").trim();
                
                int sum = (int)rs.getDouble("summa");
                if (custplansmagus.get(custID) != null){
                	TreeMap tr = (TreeMap) custplansmagus.get(custID);
                  if(grupp.equals("magus") ){
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
                      	  
                      	Iterator itera =   custplansmagus.keySet().iterator();
                      	while(itera.hasNext()){
                      		String custID = itera.next() + "";
                      		TreeMap tr = (TreeMap) custplansmagus.get(custID);
                            String asi2 = "";
                         
                      	  Iterator enume = tr.keySet().iterator();
     	                	 while(enume.hasNext()){
     	                		 String brand = enume.next()+"";
     	                		 
     	                		Toode	 tt = (Toode)tr.get(brand);
     	                			int teh = (int)tt.getDonecur();
     	                			if(tt.getPlancur() > 0){
     	                				asi2 = asi2 + ("Gr. " + tt.getBrand() + "    " +
         	                					((int)(teh/tt.getPlancur()*100))
         	                					+ "% Plan.: " +(int) tt.getPlancur() + " €.  Sales.: " + teh + " €.||");	
     	                			}
        	                		;
     	                		   }
                         res.put(custID, Converter.enCodeEst(asi2));
                      	}
                            
          } catch (Exception e) {
              getParent().showError(e);
             
           }

         // System.out.println("custMAgusPlan " + res.toString());
          return res;
        }

  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  

  public TreeMap getCustomers(Agent agent){
	  nf.setMinimumFractionDigits(2);
	  TreeMap openOrders = new TreeMap();
	  TreeMap budgets = getCustPlans();
	  TreeMap budgetsmagus = getCustPlansMagus();
	//  System.out.println("budgets " + budgets.toString());
	  TreeMap displ = getDisplayInfo();
	  try{
    	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
    		
    }catch(Exception e){
    	e.printStackTrace();
    }
	  makeCustprod();
 String cmd = " SELECT cus.name,cus.namealias,cus.accountnum,(case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end )AS payer,  \r\n" + 
 		"              (SELECT sum(amountmst) FROM custtransopen co WHERE co.duedate<getdate()  \r\n" + 
 		"                AND co.accountnum = cus.accountnum) AS debt, cus.linedisc , cus.pricegroup,  \r\n" + 
 		"                 aad.address, '' AS visit ,salesgroup AS agent ,  numofdays AS term, blocked AS block , \r\n" + 
 		"			   (select max (blocked) from custtable cc where  cc.invoiceaccount=cus.invoiceaccount) \r\n" + 
 		"			   as payerblock ,cus.phone  as info , aad.city, aad.street, astdeliverytermid as magus, ch.groupid as chain FROM custtable cus \r\n" + 
 		"			   LEFT JOIN paymterm  pay on cus.paymtermid=pay.paymtermid and  cus.dataareaid= pay.dataareaid  \r\n" + 
 		"			   LEFT JOIN ADDRESS aad ON aad.addrrecid=cus.recid and aad.dataareaid= 'lve'   and  cus.dataareaid = aad.dataareaid "
 		+ " left join ASTCUSTTREEDENORMALIZATIO30138 ch on ch.custaccount = cus.accountnum and cus.dataareaid = ch.dataareaid\r\n" + 
 		"		 WHERE   (cus.dataareaid='lve' AND  len(salesgroup)>0 and astblocked != 1 )   \r\n" + //blocked != 2 and 
 		"			    order by cus.name " ;
 
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();

    try {
    	
    	Connection con = this.getNewConnection();
    	  Statement stmr = con.createStatement();
          ResultSet rs1 = stmr.executeQuery("SELECT jou.salesid,jou.custaccount,tra.itemid ,tra.name ,tra.salesqty,jou.deliverydate AS lastdate,(tra.lineamount/case when tra.salesqty != 0 then tra.salesqty else 1 end) as lastprice  \r\n" + 
          		" 			       FROM salesline  tra INNER JOIN salestable jou ON  \r\n" + 
          		" 			       tra.salesid=jou.salesid  and  tra.dataareaid= jou.dataareaid  WHERE  jou.deliverydate between getdate()-1 and getdate()+2 \r\n" + 
          		" 				  	  AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'  \r\n" + 
          		" 			       union\r\n" + 
          		"					SELECT jou.salesid,jou.orderaccount as custaccount,tra.itemid ,tra.name ,tra.qty as salesqty,jou.deliverydate AS lastdate,(tra.lineamount/case when tra.qty != 0 then tra.qty else 1 end) as lastprice  \r\n" + 
          		"						 			       FROM CustPackingSlipTrans  tra INNER JOIN CustPackingSlipJour  jou ON  \r\n" + 
          		"				 			       tra.PackingSlipId = jou.PackingSlipId  and  tra.dataareaid= jou.dataareaid  WHERE  jou.deliverydate between getdate() -1 and getdate()+2  \r\n" + 
          		"						 				  	  AND tra.dataareaid ='lve'  AND jou.dataareaid = 'lve'    ORDER BY 2,3 ,5 ,4");
         
        
          
          while(rs1.next()){
         	 String dok = rs1.getString("salesid").trim();
         	String kl = rs1.getString("custaccount").trim();
         	String tootekood = rs1.getString("itemid").trim();
         	String tootenimi = rs1.getString("name");
         	double kogus = rs1.getDouble("salesqty");
         	String delyveryD = rs1.getString("lastdate").substring(0,10);

 	        	String linetext = "[" + nf.format(kogus)+"] " + tootekood + " "+ tootenimi +"|";
 	        	String text = linetext;
 	    	       if(openOrders.get(kl) != null){
 			    	    text  = (String)openOrders.get(kl) + linetext;
 			       } else {
 			    	   text  = "|" + dok + " " +delyveryD + "|" + linetext;
 			       }
 	    	         openOrders.put(kl,text);
 	              }
 		  
         rs1.close();
         stmr.close();
         
     
         
    	
    	
         
         
         
         
     	
  	   stmr = con.createStatement();
         rs1 = stmr.executeQuery(" select custaccount ,rtrim(ro.standartrouteid) as route,  \r\n" + 
         		" 					   (case when monday = 1 then '1' else '' end) +  (case when tuesday = 1 then '2' else '' end)   \r\n" + 
         		" 					   +  (case when wednesday = 1 then '3' else '' end)  \r\n" + 
         		" 					   +  (case when thursday = 1 then '4' else '' end)  \r\n" + 
         		" 					   +  (case when friday = 1 then '5' else '' end)  \r\n" + 
         		" 				       +  (case when saturday = 1 then '6' else '' end)\r\n" + 
         		" 					   +  (case when sunday = 1 then '7' else '' end)  as days \r\n" + 
         		"					   from ASTSTANDARTROUTECUST ro where dataareaid= 'lve'");
       
      TreeMap routes = new TreeMap();
        
        while(rs1.next()){
       	
       	String kl = rs1.getString("custaccount").trim();
       	String rrr = rs1.getString("route").trim();
       	String days = rs1.getString("days");
       	   if(days != null && days.trim().length() > 0) {
       			String text = rrr + " " + days.trim();
	    	       if(routes.get(kl) != null){
			    	    text  = (String)routes.get(kl) +"|"+ text;
			        } 
	    	         routes.put(kl,text);
	            }
       	   }
       rs1.close();
       stmr.close();
         
         
         
         
         
         
         
         
    	
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
 
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        
        
        String accountnum = rs.getString("accountnum").trim();
       
        Customer cust = new Customer(accountnum);
         String payer = rs.getString("payer");
         if(payer != null && payer.trim().length() > 0) {
        	 cust.setPayer(payer.trim());
         }
         
         
        
        boolean de = true;
        	 if(cust.getPayer().equals("109") || cust.getPayer().equals("511")) {
        		 de = false;
        		 cust.addFixSortCode(accountnum);
        		//System.out.println(accountnum + " fix kood lisatud" );
        	 }
        	 
        if(de) {
        	String debtStr = rs.getString("debt");
            if (debtStr != null){
              cust.setDebt(Double.parseDouble(debtStr));
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
         if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
        	 cust.setGroup(cust.getGroup() + "|" +pricegroup.trim());
         }else {
        	 cust.setGroup(pricegroup.trim());
         }
        } 
        
        String aadr = rs.getString("address");
        if (aadr != null) {
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ').replaceAll("Latvija", " ").trim()), "Latvija"));
         // System.out.println(aleke +aadr.replace('\n',' '));
        }
        
        cust.restName = name;
        
        name = name + " " + cust.getAddress() + " " + accountnum ;
        String budg = "";
        if(budgets.get(accountnum) != null){
        	budg = budgets.get(accountnum) + "";
        	
        }
        if(budgetsmagus.get(accountnum) != null){
        	if(budg.length() > 1) {
        		budg = budg + "|" + budgetsmagus.get(accountnum) + "";
        	}else {
        		budg = budgetsmagus.get(accountnum) + "";
        	}
        }
        
        if(budg.length() > 1) {
        	//cust.addToCustDataVec(accountnum+ ";custbudget; ;" + budg);
        	cust.setCity(budg);
        }
        
        cust.setName(name );
        
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));
          
          if(agen.trim().equals("12")) {
        	   cust.addAgent(new Agent("MERC1"));
          }
    
          Agent aa1 = (Agent)agent1.get(agen.trim());
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
      	
      	cust.addAgent(new Agent("15"));
    	cust.addAgent(new Agent("16"));
      	cust.addAgent(new Agent("17"));
      	cust.addAgent(new Agent("18"));
        }
        
        
    

        
        int term = rs.getInt("term");
        if (term > 0) {
          cust.setPaymTerm(term + "");
        }
        if(de) {
        int block = rs.getInt("block");
        if (block == 1 ) {
          cust.setSalesBlock(true);
        }
        
        if (block == 2 ) {
            cust.setSalesBlockT(true);
          }
       
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 2) {
          cust.setSalesBlock(true);
        }
        }
        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>0) {
          cust.setPersonalData("Tel:" + info.trim());
        }

        
        
        if(openOrders.get(cust.getID()) != null){
        	cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Open orders |"   +  openOrders.get(cust.getID()));
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
         	cust.setCountry("Latvia");
        }
        
        
        String magus = rs.getString("magus");
        if(magus != null && magus.trim().length() > 0) {
        	magus = magus.replaceAll(";", ",").trim();
        	magus = "Sald Pieg: "+ magus;
        	
        	  if(cust.getCity() != null && cust.getCity().trim().length() > 0) {
             	 cust.setCity(cust.getCity() + "|" +magus);
              }else {
             	 cust.setCity(magus);
              }
       
        }
        
        String rout = routes.get(cust.getID())+ "";
        if(rout != null && rout.trim().length() > 0) {
        	rout = rout.replaceAll(";", ",").trim();
        	rout = "Route: "+ rout;
        	
        	  if(cust.getCity() != null && cust.getCity().trim().length() > 0) {
             	 cust.setCity(cust.getCity() + "|" +rout);
              }else {
             	 cust.setCity(rout);
              }
       
        }
        
        if(displ.get(cust.getID()) != null) {
        	cust.setInfo(displ.get(cust.getID())+"");
        }
        String chain = rs.getString("chain");
        if(chain != null && chain.trim().length() > 0) {
        	cust.setChain(chain);
        	
        }
        
        
        customers.put(name, cust);
        custMap.put(cust.getID(),cust);
      }

      String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +
               "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co INNER JOIN custtrans a ON co.REFRECID=a.recid  and  co.dataareaid= a.dataareaid " +
               "WHERE co.dataareaid='lve' AND a.dataareaid='lve' and ltrim(a.accountnum) not in ('511' , '109') ORDER BY a.accountnum,co.transdate ";


      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        debt.setCustID(rs.getString("klient").trim());
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        
                        

                        this.debt.addElement(debt);
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



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "    SELECT astuppersegment as liik,astuppersegment as nimetus FROM inventtable  \r\n" + 
    		"				   where dataareaid ='lve' and astuppersegment !='' group by astuppersegment ";
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


  public TreeMap getProdGroupsSecond() throws SQLException {
	  

		  /*  Connection con = getNewConnection();
		    TreeMap   tm = new TreeMap();
		    String    sql = " SELECT itemgroupid as liik,name as nimetus FROM inventitemgroup  where dataareaid ='lve' ";
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
		   
*/
		    return null;
  }

  private Vector prodcodes = null;
  
  public TreeMap getProducts(Agent agent){
	  prodcodes = new Vector();
    String cmd = "   SELECT inv.itemid,(inv.itemname) AS nimetus, qtyinbox AS kastis,  mod.price AS price, itembarcode AS ean,itemgroupid AS grupp ,\r\n" + 
    		" astuppersegment AS grupp2,mod.unitid AS unit \r\n" + 
    		"    		                FROM inventtable inv \r\n" + 
    		"    		                 INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid  and  mod.dataareaid= inv.dataareaid\r\n" + 
    		"    		                       LEFT JOIN inventitembarcode bar ON bar.itemid = inv.itemid  and  bar.dataareaid= inv.dataareaid\r\n" + 
    		"    							    and bar.barcodesetupid='STD' AND bar.dataareaid='lve'  \r\n" + 
    		"    		                         WHERE   inv.dataareaid='lve' AND mod.dataareaid='lve'   AND moduletype=2  AND ASTITEMFORSALE = 1 \r\n" + 
    		"    		                            AND len(inv.itemname)>0   and inv.itemid not like ( '%TP') and inv.itemid not like ( '%EX')\r\n" + 
    		"    		                                order by inv.itemid "  ;

             String cmd2 = "  SELECT summ.itemid AS toode , sum(AVAILPHYSICAL) AS stock , max(dim.inventlocationid) as ladu  FROM INVENTSUM summ \r\n" + 
             		"			 INNER JOIN inventtable t ON summ.itemid = t.itemid  and  summ.dataareaid=t.dataareaid\r\n" + 
             		"                 INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid  and  summ.dataareaid= dim.dataareaid \r\n" + 
             		"                 INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid  and  inv.dataareaid= summ.dataareaid \r\n" + 
             		"                 WHERE summ.DATAAREAID='lve'and dim.DATAAREAID='lve' and inv.DATAAREAID='lve' AND CLOSED = 0 AND  moduletype=0  \r\n" + 
             		"                   and t.itemgroupid = 'SALD' GROUP BY summ.itemid having sum(AVAILPHYSICAL) != 0 ORDER BY summ.itemid ";
        
             String cmd3 = "select itemid ,itembarcode as code from inventitembarcode where dataareaid= 'lve'  and barcodesetupid='STD' "+	
		"	";

             
             String cmd4 ="  SELECT  pd.itemrelation as toode , percent1 as ale  FROM pricedisctable pd  where  pd.dataareaid = 'lve'  " +   		 
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
        String nimetus = Converter.enCodeEst(rs.getString("nimetus").trim()) + " " +kood;
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
        //prod.setEAN(ean);
      }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
        //prod.setGroup(grupp.trim());
       // nimetus =  "    " + nimetus ;
     }

      prod.setName(ette + nimetus.trim());


     String grupp2 = rs.getString("grupp2");
      if(grupp2 != null && grupp2.trim().length()>0){
             prod.setGroup(grupp2);
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
  private TreeMap brandplansmag = null; 

  public void setBulkSales(Map agents){
 this.connectionUp();
   
 
 brandplans = this.makePlans("Budget.xlsx");
 
 brandplansmag = this.makePlans("Budget_sweet.xlsx");
 



 
String sqlo = "   SELECT  cus.salesgroup as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end  as grupp ,round(sum(inv.weightkg*sal.qty),2) AS summakg , "
		+ "  round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa  \r\n" + 
		"  FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON \r\n" + 
		"  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid "
		+ "left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and " + 
		"    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid   WHERE " + 
		" inv.dataareaid='lve'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate()) and cus.salesgroup != ''  GROUP BY cus.salesgroup,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";
 

 
 String cmd =" SELECT  cus.salesgroup as agent ,round(sum(inv.weightkg*sal.qty),2) AS summakg , \r\n" + 
 		"			 round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa ,\r\n" + 
 		"			 round(sum(lineamount ),0) AS summa2 \r\n" + 
 		"			  FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON \r\n" + 
 		" 		  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid \r\n" + 
 		"		   left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and\r\n" + 
 		"		    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid\r\n" + 
 		"		  WHERE inv.dataareaid='lve'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
 		" 		   AND MONTH(sal.invoicedate) = MONTH(getdate())   and cus.salesgroup != '' \r\n" + 
 		"		    GROUP BY cus.salesgroup";


        if (isDebug()) System.out.println(cmd);
           try {
           	
           	
           	
           	Connection con = this.getNewConnection();
               Statement stmt =  con.createStatement();
               ResultSet	      rs = stmt.executeQuery(sqlo);
               while(rs.next()) {
                 String agentID = rs.getString("agent").trim();
                 String grupp = rs.getString("grupp").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 int sum = (int)rs.getDouble("summa");
                 if (agent != null && brandplans.get(agentID) != null){
                   TreeMap tr = (TreeMap) brandplans.get(agentID);
             if(grupp.equals("leib")){
                   Toode tt = (Toode)tr.get("1");
                   tt.setDonecur(tt.getDonecur() + sum);
                   }
                }
                 
                 if (agent != null && brandplansmag.get(agentID) != null){
                     TreeMap tr = (TreeMap) brandplansmag.get(agentID);
               if(grupp.equals("magus")){
                     Toode tt = (Toode)tr.get("1");
                     tt.setDonecur(tt.getDonecur() + sum);
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
               	e.printStackTrace();
               }
           	
           
                             
                           
                             int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                             int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                          
                       	 // Iterreerime Plaani mappi ja sealme asjad paika 
                       	  
                       	Iterator itera =   agents.keySet().iterator();
                       	while(itera.hasNext()){
                       		String agentID = itera.next() + "";
                       		Agent aa = (Agent)agents.get(agentID);
                       		
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
                          
String asi3 = "";
                          
                          if(brandplansmag.get(aa.getID()) != null){
      	                	TreeMap vv =   (TreeMap)brandplansmag.get(aa.getID());
      	                	 Iterator enume = vv.keySet().iterator();
      	                	 while(enume.hasNext()){
      	                		 String brand = enume.next()+"";
      	                		 if(!brand.equals("all")){
      	                			Toode tt = (Toode)vv.get(brand);
      	                			int teh = (int)tt.getDonecur();
      	                	  		asi3 = asi3 + ("Group " + tt.getBrand() + "    " +
      	                					((int)(teh/tt.getPlancur()*100))
      	                					+ "% Plan.: " +(int) tt.getPlancur() + " €  Sales.: " + teh + " €||");
      	                		   }
      	                		  }
      	                	    }
                          
      	                   
                          aa.setStart( Converter.enCodeEst(asi2 ) + "|" + asi3 );
                        
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
 						String agent = null;
 						double g1 = 0;
 						if(i == 0){
 							//loemepäised sisse
 							gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
 							gr1n = "salds";
 							
 							if(fname.equals("Budget.xlsx")) {
 								gr1n = "maize";
 							}
 						
 					}else{
 						
 						try {
 							agent = (row.getCell((short) 4).getStringCellValue());
 							agent = agent.substring(0,agent.indexOf(" "));
    					      } catch (Exception e) {
    					    	  System.out.println("agent ei  " + agent);
    						 //  e.printStackTrace();
    					      }
 			//System.out.println("agent " + agent);
 						try {
 						g1 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
 						
 						} catch (Exception e) {
 	   					 e.printStackTrace();	
  					      }
 						if(agent != null) {
 							
 						
 						TreeMap asi= new TreeMap();
 						if(res.get(agent) != null) {
 							asi = (TreeMap) res.get(agent);
 							
 							Toode t1 = new Toode(gr1);
 							if(asi.get(gr1) != null) {
 								t1 = (Toode)asi.get(gr1);
 							}
 					      
						       t1.setBrand(gr1n);
						       t1.setPlancur(t1.getPlancur() + g1);
						       
						       
				          asi.put(gr1, t1);
				          double dd = 0;
				        if(asi.get("all")!= null) {
				        	dd = Double.parseDouble(asi.get("all")+"");
				        }
				          asi.put("all",dd + g1);
				         
						
						if(agent != null && !agent.equals("Total")){
						    res.put(agent,asi );
						}
						
						if(agent != null && agent.equals("Total")){
							res.put("totals", asi);
						}
 							
 							
 						}else {
 				
 					       Toode t1 = new Toode(gr1);
						       t1.setBrand(gr1n);
						       t1.setPlancur(g1);
						  
					asi.put(gr1, t1);
				          asi.put("all", g1);
				         
						
						if(agent != null && !agent.equals("Total")){
						    res.put(agent,asi );
						}
						
						if(agent != null && agent.equals("Total")){
							res.put("totals", asi);
						}
 							
 						}
 					}}
 						
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

	@Override
	public void WebReporter() {
		
 	   ArrayList<String> filenames = new ArrayList<String>();
  // teeme kataloogi salvestamiseks
  		File f = new File("svdata");
  		if(!f.exists()) {
  			f.mkdir();
  		}
  		
  		Calendar cal = Calendar.getInstance();
  		String hh = cal.getTime().getHours()+"";
  		if(hh.length() == 1) {
  			hh = "0" + hh;
  		}
  		String mm = cal.getTime().getMinutes()+"";
  		if(mm.length() == 1) {
  			mm = "0" + mm;
  		}
  		String time2 = "_" + hh +"-" +mm;
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
  		
  		
  		
  	
  		Calendar algus = Calendar.getInstance();  
  	  	Calendar lopp = Calendar.getInstance();  	
  	  	Integer days = 8;
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
  	 // Teeme SV listi 
  	  
  	  	try {
  	  		String fnameBASE = this.getExcelDir() + "ReportBase.xlsx";
  	  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
  	  		XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
  			
  			XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
  			
  			
  			
  			CreationHelper createHelper = wbBASE.getCreationHelper();
  			
  			 XSSFCellStyle hlinkstyle = wbBASE.createCellStyle();
  		      XSSFFont hlinkfont = wbBASE.createFont();
  		      hlinkfont.setUnderline(XSSFFont.U_SINGLE);
  		      hlinkfont.setColor(IndexedColors.BLUE.getIndex());
  		      hlinkstyle.setFont(hlinkfont);

  			
  	  		
  	  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), rcompany, user, pass, true);
  	  	//	System.out.println("files " + files.toString());
		
   		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), rcompany, user, pass, false, null, null, "test");
   		
   		Converter.saveFileInUTF8(f.getPath() + "/currSVjson.json", cc.toString());
   		int outofstocks = 1;
 		for (int i = 0; i < cc.length(); i++) {
   			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
   			
   			String filterOutOfStock = (String)cob.get("resint3").toString();
   			String dateAndTime = (String)cob.get("recetime");
   		
   			 cal = Calendar.getInstance();
	  			try {
	  				cal.setTimeInMillis(Long.parseLong(filterOutOfStock));
	  			}catch(Exception e) {
	  				cal.setTimeInMillis((Long)cob.get("sentdate"));
	  			}
   			
	  			String dateOnly = Converter.getYYYYmMMmDDmString(cal.getTime());
	  					//dateAndTime.split("T")[0];
	  			String time = "";
	  			String hour = cal.get(Calendar.HOUR_OF_DAY)+"";
	  			if(hour.length() == 1) {
	  				hour = "0"+hour;
	  			}

	  			String minute = cal.get(Calendar.MINUTE)+"";
	  			if(minute.length() == 1) {
	  				minute = "0"+minute;
	  			}
	  			String timeOnly = hour+":"+minute;
   			
   	    	String cagid = "";
   	    	try{
   	    		cagid =	(String)cob.get("custagent");
   	    	}catch(Exception e) {
   	    		
   	    	}
   			String cagname = "";
   			try{
   				cagname =	(String)cob.get("custagname");
   	    	}catch(Exception e) {
   	    		
   	    	}
   			String agid = (String)cob.get("agentid");
   			
   			String agname = "NoName Manager" ; 
   			try {
   			  agname = (String)cob.get("managerName");
   			 } catch(Exception r) {
   				
   			 }
   			String chain = (String)cob.get("custchain");
   			
   		
   			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
   			
   			String res2 = "";
				try {
					if((String)cob.get("resstr2") != null) {
	  					res2 = (String)cob.get("resstr2");
	  				}
				}catch(Exception e) {
					
				}
			
				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(res2);
			    String segment = (String)obj.get("segment");
			    String brand = (String)obj.get("brand");
			    String type = (String)obj.get("type");
			    Long start = (Long)obj.get("start");
			    String startT = (String)obj.get("startT");
			    Double  wei =  0d;
			    try {
			       wei = Double.parseDouble(obj.get("weight")+"");
			     } catch(Exception e) {
			    	
			     }
			    Long end = (Long)obj.get("end");
			    String endT = (String)obj.get("endT");

				System.out.println(obj.toJSONString());
				
				String pricet = "1";
				String perct = "0";
				Boolean isPerc = (Boolean)cob.get("isgroup");
				if(isPerc) {
					pricet = "0";
					perct  = "1";
				}
				

	   			String signa = dateOnly + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") + brand ;
   		
   			
   			if( done != null && !done.contains(signa)){
   				
   				   				
   				Double addi = 0d;
   				
   				try {
   				  addi = Double.parseDouble(cob.get("grouptotal")+"");
   				 } catch(Exception e) {}
   				
   				worksheetBASE.createRow(outofstocks);
   				worksheetBASE.getRow(outofstocks).createCell(0).setCellValue(agid);
   				worksheetBASE.getRow(outofstocks).createCell(1).setCellValue(agname);
   				String cuname = (String)cob.get("custname");
   				String cuid = (String)cob.get("custid");
   				if(cuname.endsWith(cuid)) {
   					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
   				}
   				
   				worksheetBASE.getRow(outofstocks).createCell(3).setCellValue(cuname);
   				worksheetBASE.getRow(outofstocks).createCell(2).setCellValue(cuid);
				String prodid = (String)cob.get("prodid");
   				//worksheetBASE.getRow(outofstocks).createCell(4).setCellValue(prodid);
   				worksheetBASE.getRow(outofstocks).createCell(4).setCellValue(brand);
   				
   				
   				worksheetBASE.getRow(outofstocks).createCell(5).setCellValue(startT);
   				worksheetBASE.getRow(outofstocks).createCell(6).setCellValue(endT);
   				

   				worksheetBASE.getRow(outofstocks).createCell(7).setCellValue(pricet);
   				worksheetBASE.getRow(outofstocks).createCell(8).setCellValue(perct);
			   				
			try {
			    Double price = Double.parseDouble(cob.get("normalpr")+"");
			     if(price > 0) {
			   				worksheetBASE.getRow(outofstocks).createCell(9).setCellValue(price);
			     }
			     }catch(Exception e) {}
			
			try {
			     Double priceA = Double.parseDouble(cob.get("amoun")+"");
			     if(priceA > 0) {
			   				worksheetBASE.getRow(outofstocks).createCell(10).setCellValue(priceA);
			     }
			     }catch(Exception e) {}   
			
			try {          
			       if(wei > 0) {
					  worksheetBASE.getRow(outofstocks).createCell(11).setCellValue(wei);
					}
			    } catch (Exception e) {}
			

   				if(segment != null) {
   				  worksheetBASE.getRow(outofstocks).createCell(12).setCellValue(segment);
   				}

   				if(type != null) {
   				  worksheetBASE.getRow(outofstocks).createCell(13).setCellValue(type);
   				}
   				   				

   				if(addi > 0) {
   				  worksheetBASE.getRow(outofstocks).createCell(14).setCellValue(addi);
   				}
   				

   				try {
   	  				worksheetBASE.getRow(outofstocks).createCell(15).setCellValue((String)cob.get("comment"));
   	  				}catch(Exception e) {
   	  					
   	  				}
   				worksheetBASE.getRow(outofstocks).createCell(17).setCellValue(dateOnly);
   				worksheetBASE.getRow(outofstocks).createCell(18).setCellValue(timeOnly);
   
   				
   				String foto = null;
  				if(files != null && files.get(cuid) != null) {
  					ArrayList arr = (ArrayList)files.get(cuid);
  					for(int e = 0; e<arr.size(); e++) {
  						Toode t = (Toode)arr.get(e);
  						if(!t.getID().equals("prodid")) {
  						//System.out.println(t.getID()+ " " +  (String)cob.get("prodid") +" " + agid+ " " + t.comment +" " + t.getGr1() + " "  + dateOnly );
  						  if(t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
  							foto = t.getLadu();
  						   }
  					}}
  				}
   				
   				 //URL Link
   				if(foto != null) {
   		         Cell  cell = worksheetBASE.getRow(outofstocks).createCell(16);
   		         cell.setCellValue("Photo Link");
   		         XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(Hyperlink.LINK_URL);
   		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
   		         cell.setHyperlink((XSSFHyperlink) link);
   		         cell.setCellStyle(hlinkstyle);
   				}
   				//worksheetBASE.getRow(outofstocks).createCell(15).setCellValue(cagname);
   				
   				
   				outofstocks++;
   				done.add(signa);
   			}
   			
   	   			}
 		
			if(outofstocks > 1) {
	   	 		String exceldir = this.getExcelDir() + "reports/";
	   	   		File ff = new File( exceldir );
	   	   		if(!ff.exists()) {
	   	   			ff.mkdir();
	   	   		}
	   	   		excelifailBase.close();
	   	   		

	   	   		FileOutputStream output_file_oos =new FileOutputStream(exceldir +
	   	   				"productCamp-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + time2 + ".xlsx");
	   	   		filenames.add(exceldir + "productCamp-" + Converter.getYYYY_MM_DD_String(new java.util.Date())+ time2 + ".xlsx");
	   	   		wbBASE.write(output_file_oos); //write changes
	   	   		output_file_oos.close();  //close the stream
	   			}else {
	   				System.out.println("pole mida teha");
	   			}
   		
   		try {
         Converter.saveObject(done,"signadone.obj");
  		}catch(Exception e) {
  			e.printStackTrace();
  		}
  	
  	  	}catch(Exception e) {
  	  		e.printStackTrace();
  	  	}
 	
		System.exit(0);
	}
}
