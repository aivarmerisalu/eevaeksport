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
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

public class EugestaESTDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter,WebReporter {
  public EugestaESTDBDriver(EksportPanel parent){
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
	  	  

	  String cmd = " Select productBackofficeId as toode,sum(Inventory) as stock from "
	  		+ " dbo.EPASAULE_INVENTORIES  where regionBackofficeId = '03N' group by productBackofficeId ";
       

	  
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
	  
	    
    String cmd = "select backOfficeId as kood ,agentName as name, * from  dbo.EPASAULE_AGENTS";
    
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    try {
    	Connection con = getNewConnection();
        Statement stmt =  con.createStatement();
        ResultSet	rs = stmt.executeQuery(cmd);
    
        while(rs.next()){
        String agentID = rs.getString("kood").trim();
          Agent agent = new Agent(agentID);
          String name = rs.getString("name").trim();
          agent.setName(name);
          agent.setIsPilot(true);
          
          agent.setLanguage("EST");
          agent.setCountry("et");
          agent.setPassWord("0000");
          
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

  
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  
  public TreeMap getCustomers(Agent agent){
	  nf.setMinimumFractionDigits(2);
	
   try{
    	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
    } catch (Exception e){
    	e.printStackTrace();
    }
	//  makeCustprod();
	  
    HashMap<String, Vector<String>> cliags = new HashMap<String, Vector<String>>();
    
   TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    try {
    	
    	
    	   String aaa = "SELECT AgentBackofficeID as kood , ClientBackofficeID as klient, route  from  dbo.EPASAULE_AGENTS_CLIENTS " ;
      		
    	   Connection con = this.getNewConnection();
    	   

    		 Statement stmt = con.createStatement();
    	     ResultSet rs1 = stmt.executeQuery(aaa);
    	               while(rs1.next()){
    	                 String ag = rs1.getString("kood");
    	                 String cli = rs1.getString("klient").trim();
    	                 Vector<String> vv = cliags.get(cli);
    	                 if(vv == null) {
    	                	 vv = new Vector<String>();
    	                 }
    	                 vv.add(ag);
    	                 cliags.put(cli, vv);
    	               }
    	              rs1.close();
    	              stmt.close();
    		
    	
    	
    	 con = this.getNewConnection();
    	
      	
    	TreeMap<String,Double> debts = new TreeMap<String ,Double>();
    	
    	 String cmd2 = "  SELECT sum(Amount) as tasumata , debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS where \r\n" + 
    	 		" payDate<getdate() group by debetorBackOfficeId"; 
    	 		
    	 
    	  stmt = con.createStatement();
            rs1 = stmt.executeQuery(cmd2);
                     while(rs1.next()){
                       double volg = rs1.getDouble("summa");
                       String pay = rs1.getString("maksja").trim();
                       debts.put(pay, volg);
                     }
                    rs1.close();
                    stmt.close();
    	
                    
              	  
                    String cmd = "Select name ,BackOfficeId as accountnum, debitorBackOfficeId as payer,CreditLimit as limit , minOrderSum as minsum, '' as linedisc\r\n" + 
                    		"		, pricelistBackOfficeId as pricegroup,address ,\r\n" + 
                    		"		 '' AS visit ,''  AS agent ,  '' AS term, Status AS block , assortimentBackOfficeId as fixed, channel as chain , clientType as type,\r\n" + 
                    		"		* from  dbo.EPASAULE_CLIENTS" ;
                    

                     if (isDebug()) System.out.println(cmd);
                    
                    
                    
    	 stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
 
      while(rs.next()){
        String name = Converter.enCodeEst(rs.getString("name").trim());
        boolean goes = true;
        
        String accountnum = rs.getString("accountnum").trim();
       
        Customer cust = new Customer(accountnum);
        cust.setPayer(rs.getString("payer"));
      
        Double limit = rs.getDouble("limit");
        if(limit != null) {
        	cust.setCredit(limit+"");
        }
        
        Double minsum = rs.getDouble("minsum");
        if(minsum != null) {
        	cust.setMinsum(minsum);
        }
        
        
        boolean de = true;
        String regno  = cust.getPayer();
        
        if(regno != null && regno.trim().length() > 0 && 
        		(regno.trim().equals("10714195") || regno.trim().equals("10180925") // abc + circle
        				|| regno.trim().equals("10765896") || regno.trim().equals("10263574") // maxima + rimi
        				|| regno.trim().equals("10379733")  )) { // selver + 
        	de = false;
        }
        
      
        	
        if(de) {
        	if(debts.get(cust.getPayer()) != null) {
        		 cust.setDebt(debts.get(cust.getPayer()));
        	}else if(debts.get(cust.getID()) != null) {
        		 cust.setDebt(debts.get(cust.getID()));
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
        
        cust.setName(name );
        
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        
        if(cliags.get(cust.getID()) != null) {
         Vector<String> vv = cliags.get(cust.getID());
         
         for(String agentID : vv) {
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
     	}
      
        
    

        
       /* int term = rs.getInt("term");
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
        }*/
        
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
        String fixed = rs.getString("fixed");
        if(fixed != null && fixed.trim().length() > 0) {
        	cust.addFixSortCode(fixed.trim());
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
    this.debt.addElement(debt);
  }
  
  
  
  
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
	    String    sql = " SELECT backOfficeId as liik,Name as nimetus FROM dbo.EPASAULE_GROUPS ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String nimi = rs.getString("nimetus").trim();
	   
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
	    String    sql = "  SELECT backOfficeId as liik, Name as nimetus FROM dbo.EPASAULE_BRANDS  ";
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
    String cmd = "   Select backOfficeId as itemid,Name AS nimetus, quantityBox AS kastis,quantityPallete as alusel,increasestep as opt, minimalOrderQuantity as mini, Price AS price, barCode AS ean,groupBackOfficeId AS grupp,\r\n" + 
    		"brandBackofficeId AS grupp2,unitName AS unit, vatPercent as vat,* from  dbo.EPASAULE_PRODUCTS"  ;

             String cmd2 = " Select productBackofficeId as toode,Inventory as stock, regionBackofficeId as ladu, bestBefore as partii from  dbo.EPASAULE_INVENTORIES ";
        

             
    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap<String, Double> stocks = new TreeMap<String, Double>();
    TreeMap<String, String> partii = new TreeMap<String, String>();
    
    try {
        Connection con =   this.getNewConnection();
        Statement stmt = con.createStatement();
    	ResultSet rs1 = stmt.executeQuery(cmd2);
        
    	while(rs1.next()){
          String id = rs1.getString("toode").trim();   
          Double sto = rs1.getDouble("stock");
          String ladu = rs1.getString("ladu").trim();
          String part = rs1.getString("partii");
          
          if(part != null && !part.startsWith("1900")) {

              String gg = "BB:" + part.substring(0,10);
              gg = sto + "-" +ladu + "-" + gg;
        	  if(partii.get(id) != null) {
            	 gg = partii.get(id)+ "|" + gg;
              }  
        	  partii.put(id, gg);
          }
          
          if(stocks.get(id) != null) {
        	  sto = stocks.get(id) + sto; //summeerime laoseisu 
          }
          stocks.put(id, sto);
          }
       
    	rs1.close();
    	
    	
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst(Converter.firstToUpperCase(rs.getString("nimetus").trim())) + " " + kood;
     
        prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setOptAmountInUnits(rs.getDouble("opt"));
        prod.setMinAmountInUnits(rs.getDouble("mini"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
          prod.setEAN(ean);
        }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
         prod.setGroup(grupp.trim());
       }

      prod.setName(nimetus.trim());

     String grupp2 = rs.getString("grupp2");
          if(grupp2 != null && grupp2.trim().length()>0){ 
              prod.setGroupSecond(grupp2);
           }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                   prod.setUnit(unit);
               }

               if(stocks.get(prod.getID()) != null){
            	   prod.setStock(stocks.get(prod.getID()));
               }
               
               if(partii.get(prod.getID()) != null) {
            	  prod.setPartii(partii.get(prod.getID())); 
               }
               
               prod.setVat("24");
               
       products.put(nimetus, prod);
       prodHash.put(prod.getID(), prod);
       prodcodes.add(prod.getID());

    
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
                 if(agentID.equals("Lääne-Eest")) {
                	 agentID = "Laane-Eest";
                 }
                 if(agentID.equals("Lõuna-Eest")) {
                	 agentID = "Louna-Eest";
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
         	
         	  }

         	  System.out.println("days " + days);
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
       		
          		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), rcompany, user, pass, false, "PRODUCT", null, "test");
          		
          		Converter.saveFile(f.getPath() + "/currSVjson.json", cc.toString());
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
       				try {
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
       				

       	   			String signa = dateAndTime + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") + brand ;
          		
          			
          			if( done != null && !done.contains(signa)){
          				
          				   				
          				Double addi = 0d;
          				
          				try {
          				  addi = Double.parseDouble(cob.get("grouptotal")+"");
          				 } catch(Exception e) {}
          				
          				worksheetBASE.createRow(outofstocks);
          				worksheetBASE.getRow(outofstocks).createCell(0).setCellValue(agid);
          				// agname = new String(agname.getBytes(), "UTF-8");
          				
          				if(agid.equals("Klaip")) {
          					agname = "Asta Razmienė";
          				}
          				
          				if(agid.equals("Panev")) {
          					agname = "Valentinas Jankevičius";
          				}
          				
          				
          				worksheetBASE.getRow(outofstocks).createCell(1).setCellValue(agname);
          				String cuname = (String)cob.get("custname");
          				String cuid = (String)cob.get("custid");
          				if(cuname.endsWith(cuid)) {
          					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
          				}
          				
          				worksheetBASE.getRow(outofstocks).createCell(3).setCellValue(cuname);
          				worksheetBASE.getRow(outofstocks).createCell(2).setCellValue(cuid);
       				String prodid = (String)cob.get("prodid");
          				
       				if(brand.startsWith("R") && brand.endsWith("ta")) {
       					brand = "Rīta";
       				}
       				if(brand.startsWith("Sp") && brand.endsWith("katota")) {
       					brand = "Spēkatota";
       				}
       				if(brand.startsWith("Gard")) {
       					brand = "Gardā";
       				}
       				if(brand.startsWith("M") && brand.endsWith("aize")) {
       					brand = "Mākoņmaize";
       				}
       				if(brand.startsWith("Tost") && !brand.contains("maiz") ) {
       					brand = "Tostē";
       				}
       				if(brand.startsWith("Kur")) {
       					brand = "Kurši";
       				}
       				if(brand.startsWith("Maizi")) {
       					brand = "Maiziņš";
       				}
       				
       				if(brand.startsWith("Dz")) {
       					brand = "Dzīvā";
       				}
       				
       				if(brand.startsWith("Isten")) {
       					brand = "Istenā";
       				}
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
                     }catch(Exception e) {
                   	  
                     }
          			
          	   			}
        		
       			if(outofstocks > 1) {
       				String exceldir = this.getExcelDir()  +"reports/est/";
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

 


       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:eugestaest";

  private   Vector debt = new Vector();

  private Vector custProds = new Vector();
  private String subjectFilesTo = null;
  private Vector prods = new Vector();
    private NumberFormat NF = NumberFormat.getInstance();
}
