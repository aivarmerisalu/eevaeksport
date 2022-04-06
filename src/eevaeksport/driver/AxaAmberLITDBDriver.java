package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

public class AxaAmberLITDBDriver extends AbstractDBDriver  implements Manager,Promo,
BulkUpdater, Discounter,PictureMaker,FixLoader {
  public AxaAmberLITDBDriver(EksportPanel parent){
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
    this.noprodcat = new Vector();
    nd = getParent().getProps().getProperty("noprodcat.list");
    if(nd != null){
 	   StringTokenizer st = new StringTokenizer(nd,",");
 	   while(st.hasMoreTokens()){
 		  noprodcat.add(st.nextToken());
 	   }
    }
    System.out.println("noprodcat .list " +noprodcat.toString() );
  
    NF.setMaximumFractionDigits(4);
    NF.setGroupingUsed(false);
    
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
	private TreeMap eanprods = null;
	@Override
	public void FixLoader() {
		
	eanprods =  Converter.loadMore();
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
		this.getProducts(null);
		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
		System.out.println("Contracts " + contracts.toString());
		Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
		System.exit(0);
	}

  
	@Override
	public void PictureMaker() {
		System.out.println("Alustan pitidega ");
		makePictureCat();
		System.out.println("Lõpetan pitidega ");
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
		  // Siin teeme Cashi ära.
			  File dir = new File(getParent().getProps().getProperty("cash.read"));
			  //  System.out.println("vvv "+ driver.getImportFile());
			    File[] files = dir.listFiles();
			
			    Random randomGenerator = new Random();
			    
			      
			    this.getCustomers(null);
			    HashSet loetud = new HashSet();
			    for (int i = 0; i < files.length ; i++){
			      File file = files[i];
			      if (file.isFile()){
			    	  try{
			    		
			    	  FileReader fr = new FileReader(file);
						BufferedReader br = new BufferedReader(fr);
						String line = br.readLine();
					        line = br.readLine().replaceAll(";;", "; ;");
					        try{
					        	  line = line +  br.readLine().replaceAll(";;", "; ;");
									  	
					        }catch(Exception e){
					        	
					        }
					      StringTokenizer st = new StringTokenizer(line, ";");
							if (st.hasMoreElements()){
								//CUS_REALID	SUMA	KVID	USRREALID	ANSPOSTED	ST	VAL COMM
								//10002654	300.0	abc=123	1046	25.09.2015	VO	EUR  COMMENT
		      String cuids = st.nextToken();
		      String payer = cuids;
		      if(payers.get(cuids) != null){
		    	  payer = payers.get(cuids)+"";
		      }
		      double summa = Double.parseDouble(st.nextToken());
		      String docid = st.nextToken();
		      String user = st.nextToken();
		      String date = st.nextToken();
		      String sts = st.nextToken();
		      String curr = st.nextToken();
		      String comment = " ";
		      try{
		    	  comment = st.nextToken();
		      }catch(Exception s){
		    	  
		      }
		       
		      date = date.replace('.', '-').replace('.', '-');//substring(8)+ "-"+date.substring(4, 6)+"-"+date.substring(0,1);
		      System.out.println("date " + date);
		      
		      String uniq = docid + cuids + user + (summa+"") ;
		      
		      if(!loetud.contains(uniq)){
		    	  int randomInt = randomGenerator.nextInt(100);
		    	  String  asi = cashWebSM(cuids,payer , docid , user , date, summa , comment,randomInt+"" , i);
		   
		      Converter.saveFile(getParent().getProps().getProperty("cash.read")+ "response/" + user+"-"+ 
		    	      cuids+"-"+ (int)summa + "-" + randomInt + "-sourceSM.xml", asi);
		    
		      Converter.saveFile(getParent().getProps().getProperty("cash.out") + user+"-"+ cuids+"-"+ (int)summa + "-" + randomInt +  ".xml", asi);
			
		      loetud.add(uniq);
		      }
							}
					
							
						br.close();
						fr.close();
						File dest = new File(getParent().getProps().getProperty("cash.read") + "done/" + file.getName());
						if(dest.exists()){
							dest.delete();
						}
						file.renameTo(dest);
						  
			    	  }catch(Exception e){
			    		  e.printStackTrace();
			    	  }
			    	  }
			      
			    }
			    System.exit(0);
			  return null;
		   }



	  private String cashWebSM(String cuid,String payer ,String docid ,String user ,String date,double summa, 
			  String comment ,String x , int u){
		  String cust = cuid;
		  if(agent1.get(user) != null){
			  Agent ag = (Agent)agent1.get(user);
			  if(ag.getAgcust() != null){
				  cust = ag.getAgcust();
			  }
		  }
		  
		  String ser = "";
		  
		  if(docid.indexOf("|") != -1){
			  ser = "<ReceiptSe>" + docid.substring(0,docid.indexOf("|")) + "</ReceiptSe>";
			  docid = docid.substring(docid.indexOf("|") + 1);
		  }
		  
		  String numi = (Calendar.getInstance().getTimeInMillis()+ "").substring(3) + x + u;
		  
		  return  
		"<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><Cash>" +
			"<messageId>" + numi +  "</messageId>" +
					"<data>" +
					    "<Payer>" + payer + "</Payer>" +
					    "<Customer>" + cuid + "</Customer>" +
					    "<Manager>" + user + "</Manager>" +
						"<ReceiptNo>" + docid + "</ReceiptNo>" +
					     ser + 
						"<Currency>EUR</Currency>" +
						"<Date>" + date + "</Date>" +
						"<CashOnHand>" + summa + "</CashOnHand>" +
						"<Comment>" + comment + "</Comment>" +
					"</data>" +
	               "</Cash>";

	  }
	  

	  
  
   public Vector getFixedAssortmentData() {
	   
	   Vector lines = new Vector();
	      return lines;
	}
	 
   

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
     

          try {

        	   Connection con = this.getNewConnection();
           	Statement stmt = con.createStatement();
        	  
        	  // loeme välja baashinnad 
        	  
              
              String sthind = "SELECT PRI.ITEMRELATION as toode ,pri.AMOUNT as hind  " +
"   FROM PRICEDISCTABLE pri " +
"   WHERE PRI.DATAAREAID='bd' AND PRI.RELATION in (4) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and " + 
"     ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) "+
"   or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION   like '%Stan%' " +
"   ORDER BY PRI.ACCOUNTRELATION,pri.itemrelation";
              
        
              
              TreeMap hinake = new TreeMap();
              
              ResultSet   rs = stmt.executeQuery(sthind);

              while (rs.next()) {
                    hinake.put(rs.getString("toode").trim(), rs.getDouble("hind"));
              }
              rs.close();
                  
                  
                  //delivery hinnad ja aled loeme välja prioriteet 010 

                  String sql4 = "SELECT  PRI.groupid as grupp,  " +
			"(case when itemcode = 0 then  PRI.ITEMRELATION else '' end) as toode , " +
			"(case when itemcode = 5 then  PRI.ITEMRELATION else '' end) as alcgroup , " +
			"(case when itemcode = 3 then  PRI.ITEMRELATION else '' end) as prgroup , " +
			"pri.ENDPRICE as hind ,pri.PERCENT_ as perc0 , pri.RETROPERCENT as perc ,PRI.FROMDATE as algus " +
             " ,PRI.TODATE as lopp,datediff(week ,PRI.FROMDATE ,PRI.TODATE)  as kamp ,PRI.RELATION ,priority " +
     " FROM ASTPRICEDISCTABLEAGREEMENT pri WHERE PRI.DATAAREAID='bd' AND   " +
      "   ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
	   "or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	   "or( GETDATE()<=PRI.TODATE+1) ) ORDER BY  PRI.groupid,pri.itemrelation" ;
                                       
                          
                  
                         rs = stmt.executeQuery(sql4);

                         while (rs.next()) {

                                 String clGroup = Converter.cleanLitLatEst(rs.getString("grupp"));
                                 String prodID = rs.getString("toode");
                                 String prodALC = rs.getString("alcgroup");
                                 String prodGR = rs.getString("prgroup");
                                 double price = rs.getDouble("hind");
                                 double perc0 = rs.getDouble("perc0");
                                // double perc = rs.getDouble("perc");

                                 String algus = rs.getString("algus").substring(0,10);
                                 String lopp = rs.getString("lopp").substring(0,10);
                                 int nadal = rs.getInt("kamp");
                                 String relation = rs.getString("relation");
                                 String pri = rs.getString("priority");
                                 
                                 
                                 MatrixLine mline = new MatrixLine();
                                 
                                 if (clGroup != null && clGroup.trim().length() > 0) {
                                         mline.setCliGroup(clGroup.trim());
                                 }
                          boolean goes = false;
                                 if (prodID != null && prodID.trim().length() > 0) {
                                         mline.setProdID(prodID.trim());
                                         goes = true;
                                 }
                                 
                                 if (prodALC != null && prodALC.trim().length() > 0) {
                                	 prodALC =  prodALC.trim();
                                	 if(prodALC.length() == 1){
                                		 prodALC = "000"+ prodALC;
                                     }
                                	 if(prodALC.length() == 2){
                                		 prodALC = "00"+ prodALC;
                                     } 
                                	 if(prodALC.length() == 3){
                                		 prodALC = "0"+ prodALC;
                                     }                               
                                     mline.setDiscGroup(prodALC);
                                     goes = true;
                                 }
                                 
                                 if (prodGR != null && prodGR.trim().length() > 0) {
                                     mline.setProdGroup(prodGR.trim());
                                     goes = true;
                                 }
                                   //kolm kuud on kampaania hetkel                                
                                 if(nadal > 36 ){
                                	 mline.setProdLevel("pr");
                                   }else if (nadal > 0 && nadal < 37){
                                   mline.setStartDate(algus.trim());
                                   mline.setEndDate(lopp.trim());
                                 }
                                 
                                 if(pri!= null){
                                     if(pri.length() == 1){
                                    	 pri = "111"+ pri;
                                     }
                                     if(pri.length() == 2){
                                    	 pri = "11"+ pri;
                                     }
                                     if(pri.length() == 3){
                                    	 pri = "1"+ pri;
                                     }                               
                                     }

                                // mline.setProdLevel(pri); 
                                 
                               if(goes){
                                 if(price > 0){
                                     mline.setPrice(price);
                                     lines.add(mline);       	 
                                 }else if (perc0 >0){
                                	 mline.setDiscount(perc0);
                                     lines.add(mline); 
                                   //  mline.setProdLevel("2"+pri);  // vähendame prioriteeti alel
                                 }/*else if (perc >0){
                                	 mline.setDiscount(perc);
                                     lines.add(mline);   
                                 }*/                               
                               }
                         }
                         
                     //    System.out.println("lines1 " + lines.size());
                         
                         
                         sql4 = "select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind ,PRI.FROMDATE as algus " +
         "   	 ,PRI.TODATE as lopp,DATEDIFF(week, PRI.FROMDATE, PRI.TODATE)  as kamp,PRIORITY as prior " +
" from astPromoGMLine l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum " +
" where l.dataareaid = 'bd' and ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
"   or( GETDATE()<=PRI.TODATE+1) ) and accepted = 2  and promostatus in (1,4) and maxdiscountpromo = 0 " ;
                                  // and fromqty = 0
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
                                         	 pri = "111"+ pri;
                                          }
                                          if(pri.length() == 2){
                                         	 pri = "11"+ pri;
                                          }

                                          if(pri.length() == 3){
                                         	 pri = "1"+ pri;
                                          }
                                          MatrixLine mline = new MatrixLine();
                                          
                                      
                                          if (custID != null && custID.trim().length() > 0) {
                                                  mline.setCliGroup(custID.trim());
                                          }

                                          if (prodID != null && prodID.trim().length() > 0) {
                                                  mline.setProdID(prodID.trim());
                                          }
                                          

                                          
                                          if(kuu > 36 ){
                                         	 mline.setProdLevel("pr");
                                            }else{
                                            mline.setStartDate(algus.trim());
                                            mline.setEndDate(lopp.trim());
                                          }
                                          

                                       //   mline.setProdLevel(pri); 
                                          mline.setPrice(price);
                                 
                                          lines.add(mline);
                                  }

                        // System.out.println("lines2 " + lines.size());
                         
                         
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
			row = sheet.getRow(8);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 10; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println(groups.toString());

			for (int i = 9 ; i < sheet.getLastRowNum()+1 ; i++) {
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
                    	//   System.out.println("prodId " + prodID);
					// Siin läheme reasisse rokkima ja vaatama kas se toode
					// läheb kaasa
					for (int a = 10; a < row.getLastCellNum(); a++) {

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

							if (go != null && go.equals("1")) {
								sb.append(" ; ;" + group + ";" + prodID + "\n");
							}
						}
					 }
                  }
			  }
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}


	//	System.out.println (sb.toString());

		return sb.toString();
	}


  
  private TreeMap kogused = null;

 public String getSaveStockInfo(){
	  
   kogused = new TreeMap();
 /*  Vilnius - VLKM
   Kaunas – KNKM
   Klaipėda – KLKM
   Šiauliai – SLKM
   Panevėžys - PNKM*/

	  String cmd = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu  FROM INVENTSUM summ " +  
		         " INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
		         " INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid        " +
		     " WHERE  inv.DATAAREAID='bd' and CLOSED = 0 AND moduletype=0 and dim.inventlocationid in ('VLTB','VLKM','KNKM','KLKM','SLKM','PNKM') " + 
			"	GROUP BY  summ.itemid ,dim.inventlocationid having sum(availphysical) >0 ";
	  
	  cmd = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu   FROM INVENTSUM summ  with(nolock) " +
	      "   INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
	     " WHERE  summ.DATAAREAID='bd' and CLOSED = 0  and dim.inventlocationid in ('VLTB','VLKM','KNKM','KLKM','SLKM','PNKM') " +
		"	GROUP BY  summ.itemid ,dim.inventlocationid having sum(availphysical) >0";


       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
    	   Connection con = this.getNewConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
           String dealer = rs.getString("ladu").trim();
           if(seis > 0){
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  ";" + dealer +  "\n");
             kogused.put(kood, seis + "");
           }
           
           if(dealer.equals("VLTB")){
        	   products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  ";" + "VLKM" +  "\n");
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  ";" + "KNKM" +  "\n");
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  ";" + "SLKM" +  "\n");
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  ";" + "PNKM" +  "\n");
               products.append(kood +  ";");
               products.append(" ;");
               products.append(getParent().getNF().format(seis)   +  ";" + "KLKM" +  "\n");
            
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
  HashMap vilnius = new HashMap();
  HashMap aktsiis = new HashMap();

  public HashSet hasStock(){
	  
	  HashSet olemas = new HashSet();
	  vilnius  = new HashMap();
	  aktsiis  = new HashMap();

	  String  cmd = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu   FROM INVENTSUM summ  with(nolock) " +
		      "   INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
		     " WHERE  summ.DATAAREAID='bd' and CLOSED = 0  and dim.inventlocationid in ('VLTB','VLKM','KNKM','KLKM','SLKM','PNKM') " +
			"	GROUP BY  summ.itemid ,dim.inventlocationid having sum(availphysical) >0";


	  String  cmd2 = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ  with(nolock) " +
		      "   INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
		     " WHERE  summ.DATAAREAID='bd' and CLOSED = 0  and dim.inventlocationid in ('VLAK','VLAKKOOPS','VLAKTB','VLRK') " +
			"	GROUP BY  summ.itemid  having sum(availphysical) >0";
	  
	  
	       if (isDebug()) System.out.println(cmd);
	       try {
	    	 Connection con = this.getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd2);
	         while (rs.next()) {

	            String kood = rs.getString("toode").trim();
	            if(!olemas.contains(kood)){
	            	 olemas.add(kood);
	            }
	            double stock =   rs.getDouble("stock");
	            	 aktsiis.put(kood, (int)stock);
               }
	         
	         rs = st.executeQuery(cmd);
	         while (rs.next()) {

	            String kood = rs.getString("toode").trim();
	            if(!olemas.contains(kood)){
	            	 olemas.add(kood);
	            }
	            double stock =   rs.getDouble("stock");
	            String ladu = rs.getString("ladu").trim();
	            if(ladu.equals("VLKM") || ladu.equals("VLTB")){
	            	double kog = stock;
	            	if(aktsiis.get(kood)!=null){
	            		kog = kog + Double.parseDouble(aktsiis.get(kood)+"");
	            	}
	            	 vilnius.put(kood, (int)kog);
                 }
	            
	           }
	         
	           rs.close();
	           st.close();
	           con.close();
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       return olemas;
	   }


  HashSet usedProds = null;
  
  public DealerProduct getDealerProductsList() {
	  DealerProduct dealerProds = new DealerProduct();

      try {

    	  String  cmd = "select   summ.itemid  AS toode ,SUM(AVAILPHYSICAL) AS stock ,dim.inventlocationid as ladu   FROM INVENTSUM summ  with(nolock) " +
    		      "   INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  " +
    		     " WHERE  summ.DATAAREAID='bd' and CLOSED = 0  and dim.inventlocationid in ('VLTB','VLKM','KNKM','KLKM','SLKM','PNKM') " +
    			"	GROUP BY  summ.itemid ,dim.inventlocationid having sum(availphysical) >0";

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
               	   double akt = 0;
               	   if(aktsiis.get(toode) != null){
               		   akt = Double.parseDouble(aktsiis.get(toode)+"");
               	   }
	                   String ladu  =  rst.getString("ladu").trim();
	                   if(ladu.equals("VLTB")){
	                	      dealerProds.add("PNKM", toode,((int)seis)+""," "," " );
	                	      dealerProds.add("VLKM", toode,((int)seis)+""," "," " );
	                	      dealerProds.add("KNKM", toode,((int)seis)+""," "," " );
	                	      dealerProds.add("KLKM", toode,((int)seis)+""," "," " );
	                	      dealerProds.add("SLKM", toode,((int)seis)+""," "," " );
	                	     
	                   }else if (ladu.equals("VLKM")){
	                	      dealerProds.add(ladu, toode,((int)(seis + akt))+""," "," " );
//   	        	    	   
                         }else{
	                	      dealerProds.add(ladu, toode,((int)seis)+""," "," " );
//	        	        	    	   
	                   }
	                    dealerProds.add(ladu, toode,((int)seis)+""," "," " );
//	        	  System.out.println("toode " +  toode + " " + ladu + " " + seis);  
                  }
               
                   }
                  }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          e.printStackTrace();
      }

      
        System.out.println( " dealerProds suurus " + dealerProds);
       return dealerProds;
     
     }

  
  
  public TreeMap getDealers() {

      TreeMap dealers = new TreeMap();
      try {

    	  String cmd = "	  select   inventlocationid as ladu  ,name  as name  FROM INVENTLOCATION summ  " +
  "    WHERE  DATAAREAID='bd' and inventlocationid in ('VLKM','KNKM','KLKM','SLKM','PNKM')";

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
  
  
  private TreeMap agentDim = new TreeMap();
  
  private TreeMap agent1 = new TreeMap();
  
  public TreeMap getAgents(){
	  //makePlans();

	  //getAssortmentStr(null);
	 // System.exit(0);
	  agent1 = new TreeMap();
	  
	  
	  
    String cmd = " SELECT  a.ACCOUNTID as kood , d.NAME FROM  EAgentTable a inner join  EMPLTABLE  e "
    		+ " on e.EMPLID = a.EMPLID and e.DATAAREAID = a.DATAAREAID INNER JOIN " +
                       "  DIRPARTYTABLE AS d ON d.DATAAREAID = 'v02' AND e.PARTYID = d.PARTYID " +
               " WHERE   (e.DATAAREAID = 'bd') ";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
   
    try {
    	
    	  
	    Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	
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
          Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("kood").trim()));       
          String name = rs.getString("name").trim() + " " + agent.getID();
          agent.setName(name);
         // agent.setObject("01");
          agent.setIsPilot(true);
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
          if(email.length() > 0){
        	  agent.setActive(true);
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
  Vector noprodcat = null;
  private TreeMap payers = null;
  
  public TreeMap getCustomers(Agent agent){
try{
	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 
		
}catch(Exception e){
	e.printStackTrace();
}

payers = new TreeMap();
	   



 String cmd = "  SELECT  cus.name,cus.custclassificationid as nimelisa,cus.namealias,cus.accountnum,cus.invoiceaccount AS payer, " +
            "  (SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct "+ 
            " on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and "
            + " ct.storno = 0  and co.duedate<getdate()  and co.dataareaid = 'bd'   " +
             "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else   " +
 	         " cus.invoiceaccount end ) ) AS debt, "
 	         + "(SELECT sum(co.amountmst ) FROM custtransopen co  inner join CUSTTRANS ct " + 
 	                    " on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and " + 
 	                    " ct.storno = 0  and   co.dataareaid = 'bd'  "+  
              " AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  " + 
 	         " cus.invoiceaccount end )) AS debta, " + 
			  "(select max (creditmax) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'bd') " + 
			  "  as limit ,cus.linedisc , cus.address, '' AS visit ,  SPIEAGENTACCOUNTID as agent , SPITELESALESAGENTGROUPID as teles,  (select top 1 numofdays from paymterm pt where pt.paymtermid=	" +
			  " (select top 1 paymtermid from custtable where accountnum = cus.invoiceaccount and dataareaid in ('bd')  ) and pt.dataareaid in ('v02')) AS term, " +
			  " blocked AS block , " +
			  " (select max (blocked) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'bd') " +
			  "  as payerblock 	,a.phone + ' , ' + a.telefax as info , PriceGroup as grupp , COMPANYCHAINID as chain , "
			  + " (select max (creditrating ) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'bd') as rating "
			  + " ,subsegmentid as sugg , erpagentdiscedit as maxallow , inventlocation as dealer , astsalesorderpriority as hinnasyst "
			  + " , agreementdisc as leping " +
			  " , (select max (agreementdisc) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'bd')  as lepingpayer " +
			  "  , (select  cc.agreementdisc from smmbusrelchaingroup  cc where cc.dataareaid ='bd' and cc.chainid = cus.COMPANYCHAINID) as lepingchain " + 
			  "   , custclassificationid as ette , a.city as city , a.street as street  ,cus.memo as cuinfo , subsegmentid as cate ,"
				+"  (select spiminorderamount from smmBusRelChainGroup e where e.dataareaid= cus.dataareaid and e.chainid = COMPANYCHAINID ) as minam "
			  +  " FROM custtable cus left outer JOIN paymterm  pay on cus.paymtermid=pay.paymtermid and  pay.dataareaid='bd'  " +
			  "	LEFT JOIN dirpartytable dp ON dp.partyid=cus.partyid  inner join address a on a.ADDRRECID = dp.RECID WHERE   cus.dataareaid in ('bd')   and (SPIEAGENTACCOUNTID != '' or SPITELESALESAGENTGROUPID != '')  and erpactive =1 " +
			  "   " ; 
 
 
 
 String ccc = "select invoiceaccount from custtable where dataareaid='bd' group by invoiceaccount";
 ccc = "select AccountNum from custtable where dataareaid='bd' and (INVOICEACCOUNT = '' or ACCOUNTNUM = INVOICEACCOUNT)";

 String promo = "select custaccount as custid , l.promonum as grupp   from astPromoJournal pri  " + 
			" INNER JOIN astPromoCustomers l on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  where " +
			" l.dataareaid = 'bd' and ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
		  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
		  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
		  " or( GETDATE()<=PRI.TODATE+1 AND year(PRI.fromDATE) =1900) ) and promostatus in (1,4)";
 
  promo = "select custaccount as custid , l.promonum as grupp   from astPromoJournal pri  " + 
			" INNER JOIN astPromoCustomers l on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  where " +
			" l.dataareaid = 'bd' and ( (GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) " +
			"  AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) in (1,2) then 2 else 0 end))  " +
		  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
		  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
		  " or( GETDATE()<=PRI.TODATE+1 AND year(PRI.fromDATE) =1900) ) and promostatus in (1,4)";
 
 
           String   sql = "  SELECT a.orderaccount AS klient, a.accountnum as payer ,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata,  " +
                  "  co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle, "
                  + "case when  co.amountmst = a.amountmst then 0 else a.amountmst end as orig FROM custtransopen co with(nolock) " +
				" INNER JOIN custtrans a with(nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
                 "   WHERE co.dataareaid='bd' AND a.dataareaid='bd'  and a.storno = 0  and a.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT')" +
				"	 ORDER BY a.accountnum,co.transdate " ; // tahtsid kõiki arveid  and co.duedate < getdate()+5  and a.invoice != ''
			  
           
           String salessql = " SELECT  jou.orderaccount as klient,( ltrim( case when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
        "  round(SUM(lineamount),0) AS summa FROM custinvoicetrans sal with(nolock) INNER JOIN custinvoicejour jou  with(nolock) ON  " +
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
		" WHERE sal.dataareaid='bd'   AND jou.dataareaid='bd' AND sal.invoicedate > getdate()-200 " +
        " GROUP BY jou.orderaccount ,(YEAR(sal.invoicedate)),(month(sal.invoicedate))  order by   jou.orderaccount ,YEAR(sal.invoicedate), month(sal.invoicedate) ";

                 

       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers2 =  new Vector();
    HashMap salesres = new HashMap();
    HashMap visits = new HashMap();
    HashMap transa = new HashMap();
    HashMap promogroups = new HashMap();
    HashSet lepingud = new HashSet();
    TreeMap contractInff = new TreeMap();
    TreeMap contractDates = new TreeMap();
    
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
 
          
       String sqlc = "select contracttodate as datex ,contracttodate as datey, accountnum as klient,pg.groupid as nimi " +
    		   " from PriceDiscGroup pg with(nolock) Inner join custtable cu with(nolock) on cu.agreementdisc=pg.groupid and cu.dataareaid = pg.dataareaid " +
    		   " where cu.dataareaid = 'bd' " +
    		   "and contracttodate > getdate()-30 and contracttodate < getdate()+30 " ;
    
       stmr = con.createStatement();
   	    rs1 = stmr.executeQuery(sqlc);
       while(rs1.next()){
    	   	String datex = rs1.getString("datex").substring( 0,10);
    	   	java.sql.Date dd = rs1.getDate("datey"); 
       	String klient = rs1.getString("klient").trim();
       	String nimi = rs1.getString("nimi").trim();
      
       	contractInff.put(klient,  nimi + " - " + datex );
       	contractDates.put(klient, dd.getTime());
         
       }
      rs1.close();
      stmr.close();

       
       
 // teeme return reason file valmis 
     
     stmr = con.createStatement();
 	StringBuffer sb = new StringBuffer();
     rs1 = stmr.executeQuery("select reasoncodeid as id, description as nimi from ReturnReasonCode where dataareaid ='bd'");
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
       

   System.out.println("reason sql ok");
   	 stmr = con.createStatement();
	 rs1 = stmr.executeQuery(ccc);
    
	 while(rs1.next()){
   payers2.add(rs1.getString("AccountNum"));
      
    }
   rs1.close();
   stmr.close();

   System.out.println("payers sql ok");
   
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
   
    System.out.println("promod sql ok");
    
    
    String lepsql = "   select  pri.groupid as grupp  from ASTPRICEDISCTABLEAGREEMENT pri  " +
   		 " where  pri.dataareaid = 'bd' and ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
   	  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
   	  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900)  " + 
   	  " or( GETDATE()<=PRI.TODATE+1))" ;
    
    
    stmr = con.createStatement();
    rs1 = stmr.executeQuery(lepsql);
  
    while(rs1.next()){
 
 	   String gr = Converter.cleanLitLatEst(rs1.getString("grupp").trim()) ;
 	   if( !lepingud.contains(gr)){
 		  lepingud.add(gr);
 	    } 
 	   }
    
     rs1.close();
     stmr.close();
    
    // System.out.println("lepingud sql ok");
    	Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      while(rs.next()){
    	  String namesort = rs.getString("name").replace('*', ' ').trim();
        String name = Converter.enCodeEst(namesort);
        boolean addDebt = true;
        
        String alias = rs.getString("nimelisa");
        if(alias != null && alias.trim().length()>0 && !alias.trim().equals(name)){
        	if(!name.trim().startsWith(alias.trim())){
        		name = " " + alias.replace('*', ' ').trim() + " " + name ;
        		namesort = alias.replace('*', ' ').trim() + namesort;	
        	}
        }
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        
        name = name + " " + accountnum;
        namesort = namesort + accountnum;
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        }else{
        	cust.setPayer(payer.trim());
        	if(contractDates.get(payer.trim()) != null){
            	cust.setContractOver(contractDates.get(payer.trim()) + "");
            }
            
            if(contractInff.get(payer.trim()) != null){
              	cust.addToCustDataVec(accountnum+ ";contractinfo; ;Sutartys baigiasi |"   +  contractInff.get(payer.trim()) + "");
            }
        }
      
        
      
        
        if(contractDates.get(accountnum) != null){
        	cust.setContractOver(contractDates.get(accountnum) + "");
        }
        
        if(contractInff.get(accountnum) != null){
          	cust.addToCustDataVec(accountnum+ ";contractinfo; ;Sutartys baigiasi |"   +  contractInff.get(accountnum) + "");
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

        double limit = rs.getDouble("limit");
        if (limit > 0){
          cust.setCredit(limit+ "");
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',',')),"Lithuania"));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String age = rs.getString("agent");
        if (age != null) {
            cust.addAgent(new Agent(age.trim()));
            cust.setMainAgent(age.trim());
            if (age != null && agent1.get(age.trim()) != null){
   				Agent aa = (Agent)agent1.get(age.trim());
   			
   				Vector vec = aa.getDubbler();
   				if(vec!= null && vec.size() >0){
   					Enumeration en = vec.elements();
   				//	System.out.println("Duubel " + age.trim() + " " + vec.toString());
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
         cust.setCity("Tele: " + teles.trim());
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
          }
       
        
        int payerblock = rs.getInt("payerblock");
        if (payerblock == 1) {
          cust.setSalesBlock(true);
        }
        if (payerblock == 2) {
            cust.setSalesBlockT(true);
          }

        
        String info = rs.getString("info");
        if (info != null && info.trim().length()>2) {
        	if(info.trim().startsWith(",")){
        		info = info.trim().substring(1);
        	}
          cust.setPersonalData(info.trim());
        }
        if(salesres.get(accountnum) != null){
        	cust.setInfo(Converter.enCodeEst("CHART Pardavimas :|Mėnesį ir metus|pardavimas |" )+ salesres.get(accountnum) );
        }
        
        if(transa.get(cust.getID())!= null){
        	String ppp = (String)transa.get(cust.getID());
        	if(ppp.startsWith(",")){
        		ppp = ppp.substring(1);
        	}
        	if(cust.getCity().length()>1){
        	 	cust.setCity(cust.getCity() + "|" +Converter.enCodeEst("Pristatymo data : ") + ppp);
        	}else{
        	 	cust.setCity(Converter.enCodeEst("Pristatymo data : ") + ppp );
        	}
        }
        
        if(visits.get(cust.getID())!= null){
        	cust.setVisit(visits.get(cust.getID())+"");
        }
        
        
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0 && grupp.indexOf("Stand") == -1) {
          cust.setGroup(Converter.cleanLitLatEst(grupp.trim()));
          cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
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
      
        String sugg = rs.getString("sugg");
        if (sugg != null && sugg.trim().length()>0) {
         	cust.suggAssortCode = sugg.trim();
        }
      
        int maxallow = rs.getInt("maxallow");
                    
        if(maxallow == 1){
        	cust.setMaxdisc(1);
        }
            
        String ladu = rs.getString("dealer");
        if(ladu != null && ladu.trim().length() > 0){
        	cust.setDealer(new Dealer(ladu.trim()));
        }
        
        int hinnasyst = rs.getInt("hinnasyst");
        if(hinnasyst > 0  ){
        	// 1.Only delivery 1
        	// 2.Only Invoice  2
        	// 3.Only Chain  4
        	// 4.Chain Invoice delivery 3
        	// 5.Delivery-Chain-Invoice 7
        	// vaata prioriteeti ainult delivery pealt .
        	
        	
        	
        }
        
        String leping = rs.getString("leping");
        String lepingp = rs.getString("lepingpayer");
        String lepingc = rs.getString("lepingchain");
        // kui on määratud siis teeme määrangute järgi 
        if(hinnasyst == 1){
            if(leping != null && leping.trim().length() > 0 && lepingud.contains(leping.trim())){ //lepingud. kas on aktiivne leping
        		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
            		cust.setGroup(cust.getGroup() + "|" + leping.trim());
            	}else{
            		cust.setGroup(leping.trim());
            	}
        	}
        }else if(hinnasyst == 2){
            if(lepingp != null && lepingp.trim().length() > 0){
        		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 && lepingud.contains(lepingp.trim())){
            		cust.setGroup(cust.getGroup() + "|" + lepingp.trim());
            	}else{
            		cust.setGroup(lepingp.trim());
            	}
        	}
        }else if(hinnasyst == 4){
               if(lepingc != null && lepingc.trim().length() > 0){
        		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 && lepingud.contains(lepingc.trim())){
            		cust.setGroup(cust.getGroup() + "|" + lepingc.trim());
            	}else{
            		cust.setGroup(lepingc.trim());
            	}
        	   }	
        }else{
        	// kui on teised 2 määratud või pole yldse 
        	if(leping != null && leping.trim().length() > 0 && lepingud.contains(leping.trim())){
        		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
            		cust.setGroup(cust.getGroup() + "|" + leping.trim());
            	}else{
            		cust.setGroup(leping.trim());
            	}
        	}
        	 if(lepingp != null && lepingp.trim().length() > 0 && lepingud.contains(lepingp.trim())){
         		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
             		cust.setGroup(cust.getGroup() + "|" + lepingp.trim());
             	}else{
             		cust.setGroup(lepingp.trim());
             	}
         	}
        	 
        	if(lepingc != null && lepingc.trim().length() > 0 && lepingud.contains(lepingc.trim())){
        		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
            		cust.setGroup(cust.getGroup() + "|" + lepingc.trim());
            	}else{
            		cust.setGroup(lepingc.trim());
            	}
        	   }
        }
        
        
        
        
        if(promogroups.get(cust.getID()) != null){
        	if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
        		cust.setGroup(cust.getGroup() + "|" + (String)promogroups.get(cust.getID()));
        	}else{
        		cust.setGroup((String)promogroups.get(cust.getID()));
        	}
        	//System.out.println("grupp " + cust.getGroup());
        }
        
                
        String ette = rs.getString("ette");
        if (ette != null && ette.trim().equals("PRAPM")) {
         	cust.isSalesBlockPrepay();
        }
        
        String city  = rs.getString("city");
        if (city != null ) {
         	cust.restCity = city.trim();
        }
        String street  = rs.getString("street");
        if (street != null ) {
         	cust.restStreet = street.trim();
        }
        
        String cuinfo = rs.getString("cuinfo");
        if (cuinfo != null && cuinfo.trim().length() > 2) {
         	cust.setInfo(cuinfo.trim().replaceAll("\\r\\n|\\r|\\n", " ").replaceAll(";", ":"));
        }
    
        
        String cate = rs.getString("cate");
        if (cate != null && cate.trim().length() > 0) {
         	//cust.setAnkeediTyyp(cate.trim());
         	cust.category = cate.trim();
        }
        
        cust.setCountry("Lithuania");
        
        double minam = rs.getDouble("minam");
        if(minam > 0 ){
     	   cust.setMinsum(minam);
        }
        
        
        if(!payers2.contains(cust.getID())){
        
                    if(goes){
                    	customers.put(namesort, cust);
                        custMap.put(cust.getID(),cust);	
                        payers.put(cust.getID(), cust.getPayer());
                    }
                	
        }
        }

      System.out.println("kliendid sql ok");
      rs = stmt.executeQuery(sql);
                      while(rs.next()){
                    	 boolean addDebt = true;
                        Debt debt = new Debt();
                        String saaja = rs.getString("klient").trim();
                        debt.setCustID(saaja);
                        String payerId = rs.getString("payer").trim();
                        debt.setPayerID(payerId);
                       // debt.setCustID(payerId); // NB!!!
                        String number = rs.getString("number");
                        if(number == null || number.trim().length() == 0) {
                        	number = "CreditInvoice";
                        }
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(round(rs.getDouble("tasumata"),2));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        double orig = round(rs.getDouble("orig"),2);
                        if(orig != 0){
                        	number = number + "<" + orig + ">";
                        }
                        debt.setDoknr(number);
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

  System.out.println("debt sql ok");
  
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
  	    String    sql = "   select ITEMSUBGROUPID as liik , name as nimi from "
  	    		+ "INVENTITEMSUBGROUP where dataareaid ='bd' ";
  	    Statement st = con.createStatement();
  	    ResultSet rs = st.executeQuery(sql);

  	    while (rs.next()) {
  	    	   String abi = rs.getString("liik").trim();
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
  	      String liik = abi;
  	      String nimi = rs.getString("nimi").trim();
  	     // int count = nimi.length() - nimi.replace(" ", "").length();
  	     // while(count > 3){
  	    //	  nimi = nimi.substring(0, nimi.lastIndexOf(" "));
  	    //	 count = nimi.length() - nimi.replace(" ", "").length();
  	     // }
  	      if(liik.equals("WINE")){
  	    	  nimi = "Wine";
  	      } 
  	      
  	      if(liik.equals("SPAR")){
  	    	  nimi = "Sparkling";
  	      }
  	      
  	        tm.put(liik,  Converter.enCodeEst(nimi.toLowerCase()));

  	      //  tm.put("bs" + liik,  Converter.enCodeEst(nimi.toLowerCase() + " BS") );
  	    }
  	    rs.close();
  	    st.close();
  	    con.close();
        groups = tm;
	    return tm;
	  }else{
		 return groups; 
	  }
    	  
  	}


public TreeMap getProdGroupsSecond() throws SQLException {

TreeMap   tm = new TreeMap();
	  Connection con = getNewConnection();
	    
	  String    sql = "  select inv.ITEMSUBGROUPID as liik , inv.origcountryregionid as liik2, name as nimi from INVENTITEMSUBGROUP gr inner join inventtable inv " + 
     " on gr.ITEMSUBGROUPID = inv.ITEMSUBGROUPID  and gr.dataareaid=inv.dataareaid where inv.dataareaid ='bd' and inv.origcountryregionid != '' " +
	 " group by inv.ITEMSUBGROUPID,origcountryregionid, name order by  inv.ITEMSUBGROUPID , inv.origcountryregionid ";
  Statement st = con.createStatement();
  ResultSet rs = st.executeQuery(sql);

  while (rs.next()) {
  	String liik = rs.getString("liik").trim();
  	String maa =  rs.getString("liik2").trim();
      tm.put(liik+maa,Converter.enCodeEst( rs.getString("nimi").trim()+ " " + maa+ liik));
    }
  rs.close();
  st.close();
  con.close();

  return tm;
}
public TreeMap getProdGroupsSecondOLD() throws SQLException {

TreeMap   tm = new TreeMap();
	  Connection con = getNewConnection();
	    
	  String    sql = "  select  inv.ITEMSUBGROUPID as abi , BRANDID as liik , name as nimi from SPIBRANDTABLE sp inner join inventtable inv " + 
     " on sp.BRANDID = inv.SPIBRANDID and sp.dataareaid=inv.dataareaid "
     + " where sp.dataareaid ='bd' group by BRANDID,inv.ITEMSUBGROUPID, sp.name " +
     " order by  ITEMSUBGROUPID ";
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
      tm.put(abi+"."+liik,Converter.enCodeEst( rs.getString("nimi").trim()+ " " + liik));
    }
  rs.close();
  st.close();
  con.close();

  return tm;
}

  public TreeMap getProducts(Agent agent){

	  String list = "'ALCO','CIGA','NALC','TARA','NFOO','FOOD'";
	  if( parent.getProps().getProperty("prodgrouplist") != null){
		  list = parent.getProps().getProperty("prodgrouplist"); 
	  }
	   
	  System.out.println("noprodcat " + noprodcat);
	  
	  
	  
    String cmd = "   SELECT  inv.itemid, inv.itemname AS nimetus, inv.taxpackagingqty AS kastis,inv.standardpalletquantity AS alusel, disc.amount AS price  " + 
" , bar.itembarcode AS ean ,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2 , inv.alcstrengthgroupid as gruppale,mod.unitid As unit, '21' as vat,mod.LineDisc as alegr  " +
 " , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti ,NEWITEM as uus ,sellout as out , topitem as tops , topoffitem as tops2 "
 + ", giftbox as gift ,balticstate as baltic, origcountryregionid as coun , SPIDONTCONTROLMINAMOUNTMS as nocount FROM inventtable inv with(nolock) " +
"  left join InventItemSalesSetup s with(nolock) on s.itemid = inv.itemid and s.dataareaid=inv.dataareaid left join pricedisctable as disc with(nolock) on inv.itemid=disc.itemrelation and disc.dataareaid=inv.dataareaid " +
"  and disc.module = 1  and disc.relation=4  and disc.itemcode=0 and disc.accountcode=1 and accountrelation like 'Stan%' and (disc.todate > getdate() or year( disc.todate ) = 1900) and   (disc.fromdate < getdate()+1 or year( disc.fromdate ) = 1900 ) " +
"  left join inventitembarcode bar with(nolock)  on bar.dataareaid = inv.dataareaid  and  bar.itemid = inv.itemid and bar.barcodesetupid ='EANBottle'  and useforprinting = 1  " +
" left JOIN inventtablemodule mod with(nolock) ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
" WHERE  inv.dataareaid='bd'    AND mod.dataareaid='bd'   " +
"  AND moduletype=2 and active = 1  and inv.onlyretail = 0  and ITEMGROUPID in (" + list + ") " +
"  order by inv.itemid, disc.todate desc "; // vynas
    
    
    TreeMap products = new TreeMap();
     usedProds = new HashSet();
    try { 

    	HashSet  olemas = hasStock();

    	Connection con = this.getNewConnection();

     if (isDebug()) System.out.println(cmd);

    	Statement stmt =  con.createStatement();
     ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus =  rs.getString("nimetus").trim()  ;
    String nnnn = nimetus + " " + kood;
    prod.setNameSorting(nnnn);
     nimetus =  Converter.enCodeEst(nimetus.replaceAll("bottle", "bot.").replaceAll("Reserve", "Res.")
     		.replaceAll("Premium", "Prem.")
     		.replaceAll("Chateau", "Cha.").replaceAll("Stolichnaya", "Stoli.")
     		.replaceAll("Riserva", "Ris.").replaceAll("Classic", "Cla.").replaceAll("Collection", "Col.")
     		.replaceAll("Chablis", "Chab.")
     		.replaceAll("Chardonnay", "Char.").replaceAll("Cabernet", "Cab.").replaceAll("Sauvignon", "Sauv.").replaceAll("Rouge", "Rou.").replaceAll("Blanc", "Bla."));
  
        prod.setInCases(rs.getDouble("kastis"));
        String uu = "(" + (int)prod.getInCases() + ")";
        if(nimetus.contains(uu)) {
        	nimetus = nimetus.substring(0,nimetus.indexOf(uu)).trim();
        }
        nimetus = nimetus + " " + kood;
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        	 prod.setEAN(ean.trim());
             
             if(eanprods == null) {
         		eanprods= new TreeMap();
         	}
       
         eanprods.put(prod.getEAN(), prod.getID());
        int ll  = ean.trim().length();
        if(ean.length() >4){
            add = " " +  ean.trim().substring(ll-4);	
        }
      }
     
        if(vilnius.get(prod.getID()) != null){
        	add = add + " V:" + vilnius.get(prod.getID()) ;
        }
        
        if(aktsiis.get(prod.getID()) != null){
        	prod.setPartii("Akciz.sand. : " +  aktsiis.get(prod.getID()));
        }
        
        boolean goes = true;
      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
    	   String abi = grupp.trim();
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
    	   if(noprodcat.contains(grupp.trim())){
        	   goes = false;
           }

          
           String grupp2 = rs.getString("grupp2");
           if(grupp2!=null && grupp2.trim().length()>0){
        	//   prod.setGroupSecond(abi + "." + grupp2.trim());
        	   prod.setAlegrupp(grupp2.trim());
        	   if(noprodcat.contains(grupp2.trim())){
            	   goes = false;
               }
        	  
           }
           
          
           
       }
       
       
      
       
       
      String prodALC = rs.getString("gruppale");
       if(prodALC !=null && prodALC.trim().length()>0){
    	   if(noprodcat.contains( noprodcat.contains(prodALC.trim()))){
        	   goes = false;
           }
    	   prodALC =  prodALC.trim();
      	 if(prodALC.length() == 1){
      		 prodALC = "000"+ prodALC;
           }
      	 if(prodALC.length() == 2){
      		 prodALC = "00"+ prodALC;
           } 
      	 if(prodALC.length() == 3){
      		 prodALC = "0"+ prodALC;
           } 
      	 if(prod.getAlegrupp().trim().length() > 1){
      		 prod.setAlegrupp(prod.getAlegrupp() + "|" + prodALC);
      	 }else{
      		 prod.setAlegrupp(prodALC);
      	 }
    	  
      }
    
      String unit = rs.getString("unit");
      if(unit!=null && unit.trim().length()>0){
       prod.setUnit(unit);
    }
      
      String vat = rs.getString("vat");
      if(vat!=null && vat.trim().length()>0){
       prod.setVat("21");
    }
  
   
      String type = rs.getString("type");
      if(type!=null && type.trim().length()>0){
       prod.setType(type.trim());
    }

      
      String ette = "";
      if(olemas.contains(kood)){
   	 	   if(prod.getType().startsWith("POSM")){
			   ette = "xx "; 
	 	   }
   	   }else if(prod.getType().startsWith("POSM")){
   		   goes = false;
   	   }
     
      
     prod.setName(Converter.enCodeEst(ette + nimetus + add));

      
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
    	  prod.setStatusTop("1");
      }

      int topitemoff = rs.getInt("tops2");
      if(topitemoff == 1){
    	  prod.setStatusTop("1");
      }
      

      int giftitem = rs.getInt("gift");
      if(giftitem == 1){
    	  prod.setStatusGiftPack("1");
      }

      int bs = rs.getInt("baltic");
      if(bs == 1){
    	//  prod.setGroup("bs" + prod.getGroup());
      }
      String coun = rs.getString("coun");
      if(coun != null){
    	  prod.setGroupSecond(prod.getGroup() + coun);
      }
      
      int  nocount = rs.getInt("nocount");
      if(nocount == 1){
    	 prod.sumNoCount = true;
      }
      
      if(goes){
     	  products.put(Converter.cleanLitLatEst(ette) + nimetus, prod);
          prodHash.put(prod.getID(), prod);   
          usedProds.add(prod.getID());
      }
       

      }
      rs.close();
      stmt.close();
      con.close();
      
      
      
      
//See on vana 
	  cmd = " select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind , fromqty as kogus ,astpromopercent as perc,linedisc "+
       " from astPromoGMLine l with(nolock) INNER JOIN astPromoJournal pri with(nolock) on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  " +
       " where l.dataareaid = 'bd' and   ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " + 
       "   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " + 
       "   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " + 
       "   or( GETDATE()<=PRI.TODATE+1) )  " +
	   "   and accepted=2 and promostatus in (1,4) and maxdiscountpromo = 1 order by l.ITEMID,fromqty "; //and fromqty > 0
		
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
		                  	if(kogus == 0){
		                  		kogus = 1;
		                  	}
		                  	double  perc = rs.getDouble("perc");
		                  	double  lined = 0;//rs.getDouble("linedisc");
		                  	
		                  	double hl = (hind -lined) *(100-perc)/100;
		                  	// siia teha number format 
		                  	
		                  	      buf.append(toodex+ "; ; ;" + NF.format(hl).replace(',', '.') + ";" + Converter.cleanLitLatEst(gruppx) + ";" + kogus + ";"  );
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
    

      
      
      
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    HashMap kliendid = new HashMap();
	 int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok  " +
	   "    FROM custinvoicetrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid   " + 
	   "    WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid ='bd'  AND jou.dataareaid = 'bd'   " +
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
	        	        custProds.add(cp);
	    	Product prod = new Product(tootekood);
			Customer cust = (Customer) kliendid.get(kliendikood);
			if (cust == null) {
				cust = new Customer(kliendikood);
			}
			cust.addProduct(prod, cp,false);
			kliendid.put(kliendikood, cust);
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    

			int counte = 0;
			int cou = 0;
			String oldklient = null;
			String olddate = " ";
			String oldtoode = null;

	
		
			String cmd2 = "SELECT jou.orderaccount as klient,tra.itemid  as toode ,tra.qty as kogus,jou.invoicedate AS lastdate  " +
					   "    FROM custinvoicetrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid   " + 
					   "    WHERE  jou.invoicedate > getdate()-60 AND tra.dataareaid ='bd'  AND jou.dataareaid = 'bd'   " +
					   "    and tra.qty !=0 and tra.itemid != '' " +
					   "    GROUP BY jou.orderaccount , tra.itemid , jou.invoicedate , tra.qty " +
					    "   ORDER BY jou.orderaccount , tra.itemid , jou.invoicedate desc , tra.qty" ;
			System.out.println(cmd2);
			// "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"

			con = getNewConnection();
			Statement stmt6 = con.createStatement();
			ResultSet rs2 = stmt6.executeQuery(cmd2);

			while (rs2.next()) {
				String klient = rs2.getString("klient").trim();
				String toode = rs2.getString("toode").trim();
				String kogus = "0";
				String date = " ";
				kogus = rs2.getDouble("kogus") + "";
				kogus = kogus.replace('.', ',');
				if (kogus.indexOf(",") != -1) {
					kogus = kogus.substring(0, kogus.indexOf(","));
				}
				date = rs2.getString("lastdate").trim().substring(0, 10);

				if (cou == 0) {
					oldklient = klient;
					olddate = date;
					oldtoode = toode;
				}
				if (!oldklient.equals(klient)) {
					counte = 0;
					oldklient = klient;
					olddate = date;
				}
				if (!toode.equals(oldtoode)) {
					counte = 0;
				}
				Customer c = (Customer) kliendid.get(klient);
				if (c != null) {

					ArrayList al = (ArrayList) c.getProducts().get(toode);
					if (al != null) {
						CustProd cp = (CustProd) al.get(1);
						if (cp != null) {
							if (!olddate.equals(date)) {
								counte++;
							}
							if (counte == 0) {
								cp.setS1(kogus);
							}
							if (counte == 1) {
								cp.setS2(kogus);
							}
							if (counte == 2) {
								cp.setS3(kogus);
							}
							if (counte == 3) {
								cp.setS4(kogus);
							}
							if (counte == 4) {
								cp.setS5(kogus);
							}

							custProds.add(cp);
						}
					}
					oldklient = klient;
					olddate = date;
					oldtoode = toode;
				}
				cou++;
			}

			rs2.close();
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
	  
	   try{
		
	   Calendar cal = Calendar.getInstance();
	   int month = (cal.get(Calendar.MONTH)+1);
	    int a = 0;
	   File f = new File (getExcelDir() + "SalesPlans.xlsx");
		  FileInputStream	 ff = new FileInputStream(f);
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		int sheets =workbook.getNumberOfSheets();
		
		
		for(int y = 0;y < sheets;y++){
			XSSFSheet sheet = workbook.getSheetAt(y);
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
					String grupp = "";
					String abi = "";
					if(y != 0){
						try{
							row = sheet.getRow(1);
							grupp = "|"+ (row.getCell((short) 1).getStringCellValue()); 
							row = sheet.getRow(2);
							abi = "|"+  (row.getCell((short) 1).getStringCellValue()); 
								
						}catch(Exception t){
							
						}
					
					}

					for(int i = 5 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String agent = null;
						
						try {
							agent = (row.getCell((short) 0).getStringCellValue());
					      } catch (Exception e) {
						   //e.printStackTrace();
					      }
					//	System.out.println(agent + " " + grupp + " " + abi );
						double plan = 0;
						try {
			    			plan = Double.parseDouble(row.getCell((short) month+1).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					// e.printStackTrace();	
							try {
							  	 plan = Double.parseDouble(row.getCell((short) month+1).getStringCellValue());
												
							}catch(Exception r){
								
							}
	   					}
					 
						if(plan > 0 && agent != null && agent.length() > 0){
						    res.put(agent+grupp.trim()+abi.trim(),plan);
						}
					}}
		}
	   }catch (Exception e){
		 //  e.printStackTrace();
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
	 "	 WHERE inv.dataareaid='bd' AND sal.dataareaid='bd' AND cus.dataareaid='bd'  AND jou.dataareaid='bd'  AND((sal.invoicedate >= DATEADD(month, DATEDIFF(month, 0, getdate()), 0) \r\n" + 
	 "     AND sal.invoicedate <  DATEADD(month, DATEDIFF(month, 0, getdate())+1, 0) )\r\n" + 
	 "       OR (sal.invoicedate >= DATEADD(month, DATEDIFF(month, 0, getdate())-12, 0)  \r\n" + 
	 "    AND sal.invoicedate <  DATEADD(month, DATEDIFF(month, 0, getdate())-12+1, 0) ))            \r\n" + 
	 " " +
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
                   agent.setStart(Converter.enCodeEst("Einamasis menuo, praeiti metai :|") + summalast + "€|" );  
                   
                   agent.buffer = (int)(sales);

             	  double msales = agent.buffer;
         		  double psales = 0;
         		try{
         		  psales = Double.parseDouble(plans.get(agent.getID())+"");
         		  double  tehtudsalperc = msales/psales;
             	  double plaanitavMyykSal = d2 * msales/dd;
             	  double  prognosalperc = plaanitavMyykSal/psales;
               String asi = Converter.enCodeEst("Pardavimas: ") + (int)msales + " €|";
               if(dd > 6){
             	  asi = asi + Converter.enCodeEst("Plano vykdymas/ Prognozė :");
             	  if(tehtudsalperc > 3){
                 		tehtudsalperc = 0;
                 	  }
                 	if(prognosalperc > 3){
                 		prognosalperc = 0;
                 	  }
                   asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %|";  
               }
               
                  asi = asi + Converter.enCodeEst("Pardavimo planas: ") + (int)psales + " €|";    
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
          	     "  FROM custinvoicetrans sal INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
                 "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
          	 " WHERE inv.dataareaid='bd' AND sal.dataareaid='bd' AND cus.dataareaid='bd'  AND jou.dataareaid='bd' AND "
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
                         String asi =  " | " +grupp +"-"+type + " " + Converter.enCodeEst("pardavimas: ") + (int)msales + " €|";
                         asi = asi +grupp +"-"+type + " " + Converter.enCodeEst("planas: ") + (int)psales + " €|";    
                         
                         if(dd > 6){
                       	  asi = asi + Converter.enCodeEst("Plano vykdymas/ Prognozė :");
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
                          "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
                   	 " WHERE inv.dataareaid='bd' AND sal.dataareaid='bd' AND cus.dataareaid='bd'  AND jou.dataareaid='bd' AND "
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
                                  String asi =  " | " +grupp  + " " + Converter.enCodeEst("Pardavimas: ") + (int)msales + " €|";
                                  asi = asi + grupp + " " + Converter.enCodeEst("planas: ") + (int)psales + " €|";    
                                  
                                  if(dd > 6){
                                	  asi = asi + Converter.enCodeEst("Plano vykdymas/Prognozė :");
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

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  
  public String getPilotDayData(Agent agent){

	   String sql2 =  "SELECT sal.recid AS id,case when eorderid like 'MS-%' then 1 else 0 end as abi,cus.name AS nimi ,sal.date AS kuup,eorderid as number , " +
	   	" linecount AS summa FROM ESalesOrderTable sal with(nolock) INNER JOIN custtable cus with(nolock) ON sal.deliverycode=cus.accountnum  and cus.dataareaid= sal.dataareaid  " +
	     "      WHERE sal.dataareaid='bd' AND cus.dataareaid='bd' AND cus.SPIEAGENTACCOUNTID ='" + agent.getID()+ "' AND  " +
	      "     sal.date > getdate()-0.8    ORDER BY sal.date " ;
StringBuffer sb = new StringBuffer();
this.connectionUp();
try {
	 nf.setMinimumFractionDigits(2);
 Connection con = this.getNewConnection();
PreparedStatement stmt2 = con.prepareStatement(sql2);
//stmt2.setString(1, agent.getID());
String sep= System.getProperty("line.separator");
  ResultSet  rs = stmt2.executeQuery();
double droidsum = 0;
double muusum = 0;
boolean droid = true;

while(rs.next()){
	 String id = rs.getString("id");
	 int abi = Integer.parseInt(rs.getString("abi"));
   String nimi = rs.getString("nimi");
   if (nimi == null){
   nimi = " ";
   } else {nimi = nimi.trim();}

	sb.append(nimi + "\n");
	sb.append(rs.getString("kuup").trim().substring(0,10));
	sb.append("  Nr." + rs.getString("number").trim() + "\t");
	double summa = Double.parseDouble(rs.getString("summa").trim());

	
	String sum = getNF().format(summa) ;
   sb.append("(" + sum + " lines)\n" + sep);
 
   String sql3 = "select orderedqty as amount,  unitofmeasure as unit  ,itemdescription as name from " +
			   " ESalesOrderLines rid where rid.dataareaid='bd' and  refrecid = " + id;
   Connection cc = this.getNewConnection();
   PreparedStatement stmt3 = cc.prepareStatement(sql3);
	 
      ResultSet  rs2 = stmt3.executeQuery();
  
	    while(rs2.next()){
	    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
	       + rs2.getString("unit").trim()+ " " + rs2.getString("name").
	       replaceAll("\\r\\n|\\r|\\n", " ")
	       .replaceAll("  ", " ")
	       .replaceAll("  ", " ")
	       .replaceAll("  ", " ")
	       .replaceAll("  ", " ")
	       .replaceAll("  ", " ")
	       .trim() + "\n");
	    }
  
	    rs2.close();
      stmt3.close();
      cc.close();
   	sb.append( "\n\n");
	 	  
   
	if(abi == 0){
		droidsum = droidsum + summa;
	}else{
		muusum = muusum + summa;
		if(droid && droidsum > 0 ){
			droid = false;
			sb.append("------------------------------\n");
			sb.append("Android total :" + getNF().format(droidsum)  + "lines\n\n");
		}
	}
}
rs.close();
stmt2.close();
con.close(); 	 
	if(droid && droidsum > 0 ){
			droid = false;
		//	sb.append("------------------------------\n");
		//	sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
		}

if(muusum > 0){
		//sb.append("------------------------------\n");
		//sb.append("E-tellimused Kokku :" + getNF().format(muusum)  + "eur");
		 
}

} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();

Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent.getID() + "report.txt", sb.toString());
return "";
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
System.out.println("prods " + prods.size());
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
	                System.out.println("pilt olemas " + ff2[i]);
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
