package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
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
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaOrklaLVIMDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public AxaOrklaLVIMDBDriver(EksportPanel parent){
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
    rcompany = getParent().getProps().getProperty("rest.company");
  }

  String user =  null;
  String pass = null;
  String rcompany = null;
	
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

  public String getSaveDebtInfo() {
   	return null;
   }


  public Vector getFixedAssortmentData() {
      Vector lines = new Vector();

      try {
     	  String cmd = " select CatalogueCode as grupp ,itemid as toode from bmssa.TraCatalogueItem where dataareaid = 'olv'"
     	  		+ " and cataloguecode like 'LV%' order by CatalogueCode,itemid";
     	
     	System.out.println(cmd);
     	 Connection cc = getNewConnection();
    	        Statement stmt = cc.createStatement();
    	        ResultSet rs = stmt.executeQuery(cmd);
    	        while(rs.next()){
                       String custID = " ";
                       String clGroup = " ";
                       String prodID = " ";
                       String prodGroup = " ";
                       MatrixLine mline = new MatrixLine();

                       clGroup = rs.getString("grupp");
                       prodID = rs.getString("toode");

                       if (custID != null && custID.trim().length() > 0) {
                         mline.setCustID(custID.trim());
                       }
                       if (clGroup != null
                           && clGroup.trim().length() > 0) {
                         mline.setCliGroup(clGroup.trim());
                       }

                       if (prodID != null && prodID.trim().length() > 0) {
                         mline.setProdID(prodID.trim());
                       }
                       if (mline.getProdID() == null && prodGroup != null
                           && prodGroup.trim().length() > 0) {
                         mline.setProdGroup(prodGroup);
                       }
                     
                       lines.add(mline);
                     }

    	       rs.close();
    	       cc.close();
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
      
      
      
      
      try {
    	  String cmd = " select CatalogueCode as grupp ,itemid as toode from bmssa.TraCatalogueItem where dataareaid = 'k12'"
       	  		+ " order by CatalogueCode,itemid";
       	
     	
     	System.out.println(cmd);
     	 Connection cc = getNewConnection2();
    	        Statement stmt = cc.createStatement();
    	        ResultSet rs = stmt.executeQuery(cmd);
    	        while(rs.next()){
                       String custID = " ";
                       String clGroup = " ";
                       String prodID = " ";
                       String prodGroup = " ";
                       MatrixLine mline = new MatrixLine();

                       clGroup = rs.getString("grupp");
                       prodID = rs.getString("toode");

                       if (custID != null && custID.trim().length() > 0) {
                         mline.setCustID(custID.trim());
                       }
                       if (clGroup != null
                           && clGroup.trim().length() > 0) {
                         mline.setCliGroup("con-" + clGroup.trim());
                       }

                       if (prodID != null && prodID.trim().length() > 0) {
                         mline.setProdID("con-" + prodID.trim());
                       }
                       if (mline.getProdID() == null && prodGroup != null
                           && prodGroup.trim().length() > 0) {
                         mline.setProdGroup(prodGroup);
                       }
                     
                       lines.add(mline);
                     }

    	       rs.close();
    	       cc.close();
                    } catch (Exception e) {
                        getParent().showError(e);
                    }
                 
   //   System.out.println(lines.size() + " " + lines.toString());
      
                 
      return lines;
}



  public Vector getPriceMatrixData() {
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
	  Vector lines = new Vector();
      
	  
	   TreeMap yldorkla = new TreeMap();
	   TreeMap yldkond = new TreeMap();
	  
	  String cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
		       "  disc.DATAAREAID='olv' AND "+
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
		             		       "  disc.DATAAREAID='olv' AND "+
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
	  
	  
	  

	        
	        
	  	    cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
			        "  disc.DATAAREAID='k12' AND "+
			        "  disc.ACCOUNTCODE=1 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
		  
		  
		   con = getNewConnection2();
	  	    stmt = con.createStatement();

			              rs1 = stmt.executeQuery(cmd2);
			                     while(rs1.next()){
			                    	 String asi = rs1.getString("asi").trim();
			                       double ale = rs1.getDouble("discount");
			                       yldkond.put(asi, ale);
			                     }
			                    rs1.close();
			                    
			                    
			                    cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
			             		       "  disc.DATAAREAID='k12' AND "+
			             		       "  disc.ACCOUNTCODE=0 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
			             	               	             rs1 = stmt.executeQuery(cmd2);
			                    	                     while(rs1.next()){
			                    	                    	 String asi = rs1.getString("asi").trim();
			                  		                       double ale = rs1.getDouble("discount");
			                  		                       yldkond.put(asi, ale);
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
        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='olv' AND PRICEDISCTABLE.RELATION=5 "
        + "AND PRICEDISCTABLE.ACCOUNTCODE=0 AND PRICEDISCTABLE.ITEMCODE=0 " +
        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
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
                         
                          
                           if(paevi < 60 && paevi > 0){
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
                  
                  //kliendi ALEGRUPP ja Toode 
                  
                  sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
                	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
                			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
                	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='olv' "
                	        + "AND PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=0 " +
                	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
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
                	                         
                	                          
                	                           if(paevi < 60 && paevi > 0){
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

                  
                  
                  //kliendi ALEGRUPP ja tootegrupp 
                  
                  sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
                	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
                			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
                	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='olv' AND "
                	        + "PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=1 " +
                	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
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
                	                         
                	                          
                	                           if(paevi < 60 && paevi > 0 ){
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
                  
                  
                  
                  
                  
                  //kliendi klient ja tootegrupp 
                  
                  sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
                	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
                			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
                	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='olv' AND "
                	        + "PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=0 AND PRICEDISCTABLE.ITEMCODE=1 " +
                	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
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
                	                          
                	                    //KLIENDIGRUPP JA TOODE
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
                	                          
                	                           if(paevi < 60 && paevi > 0 ){
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
	  
	  getPriceMatrixDataKond(lines);
          return lines;
  }

  
  
  
  
  
  
  
  public Vector getPriceMatrixDataKond(Vector lines) {
	  
	   // jätame meelde yldised aled et saaks summeerida 
	  
     
	  
	   TreeMap yldorkla = new TreeMap();
	   TreeMap yldkond = new TreeMap();
	  
	  String cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
		       "  disc.DATAAREAID='olv' AND "+
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
		             		       "  disc.DATAAREAID='olv' AND "+
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
	  
	  
	  

	        
	        
	  	    cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
			        "  disc.DATAAREAID='k12' AND "+
			        "  disc.ACCOUNTCODE=1 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
		  
		  
		   con = getNewConnection2();
	  	    stmt = con.createStatement();

			              rs1 = stmt.executeQuery(cmd2);
			                     while(rs1.next()){
			                    	 String asi = rs1.getString("asi").trim();
			                       double ale = rs1.getDouble("discount");
			                       yldkond.put(asi, ale);
			                     }
			                    rs1.close();
			                    
			                    
			                    cmd2 =  "  SELECT disc.accountrelation as asi, disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE " +
			             		       "  disc.DATAAREAID='k12' AND "+
			             		       "  disc.ACCOUNTCODE=0 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
			             	               	             rs1 = stmt.executeQuery(cmd2);
			                    	                     while(rs1.next()){
			                    	                    	 String asi = rs1.getString("asi").trim();
			                  		                       double ale = rs1.getDouble("discount");
			                  		                       yldkond.put(asi, ale);
			                    	                     }
			                    	                    rs1.close();
			                    
		         stmt.close();
		        con.close();
	        
	  
	  System.out.println("yldkond " + yldkond.size());

	  System.out.println("yldorkale " + yldorkla.size());
	  
	  
      String sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
    "  MIN(PRICEDISCTABLE.PERCENT2) AS discount  , MIN(PRICEDISCTABLE.PERCENT1) AS discount2  ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
	" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
    "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='k12' AND PRICEDISCTABLE.RELATION=5 "
    + "AND PRICEDISCTABLE.ACCOUNTCODE=0 AND PRICEDISCTABLE.ITEMCODE=0 " +
    "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
    " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
   "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

    
        con = getNewConnection2();
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
                     
                      
                       if(paevi < 60 && paevi > 0){
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
              
              //kliendi ALEGRUPP ja Toode 
              
              sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
            	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
            			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
            	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='k12' "
            	        + "AND PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=0 " +
            	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
	                    " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
            	        "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

            	        
            	            con = getNewConnection2();
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
            	                         
            	                          
            	                           if(paevi < 60 && paevi > 0){
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

              
              
              //kliendi ALEGRUPP ja tootegrupp 
              
              sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
            	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
            			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
            	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='k12' AND "
            	        + "PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=1 AND PRICEDISCTABLE.ITEMCODE=1 " +
            	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
	                    " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
            	       "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

            	        
            	            con = getNewConnection2();
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
            	                         
            	                          
            	                           if(paevi < 60 && paevi > 0 ){
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
              
              
              
              
              
              //kliendi klient ja tootegrupp 
              
              sql = "  SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, " + 
            	        "  MIN(PRICEDISCTABLE.PERCENT2) AS discount , MIN(PRICEDISCTABLE.PERCENT1) AS discount2   ,min(PRICEDISCTABLE.FROMDATE) as algus , max(PRICEDISCTABLE.TODATE) as lopp , " +
            			" datediff( day,min(PRICEDISCTABLE.FROMDATE) , max(PRICEDISCTABLE.TODATE) )  as paevi " +
            	        "   FROM bmssa.PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='k12' AND "
            	        + "PRICEDISCTABLE.RELATION=5 AND PRICEDISCTABLE.ACCOUNTCODE=0 AND PRICEDISCTABLE.ITEMCODE=1 " +
            	        "  AND ( (GETDATE()>=PRICEDISCTABLE.FROMDATE AND GETDATE()<=PRICEDISCTABLE.TODATE+1) or  " +
	                    " (GETDATE()>=PRICEDISCTABLE.FROMDATE AND PRICEDISCTABLE.TODATE = ('1900-01-01 00:00:00.00') ) ) " +
            	       "   GROUP BY ITEMCODE,ACCOUNTCODE,ITEMRELATION,ACCOUNTRELATION";

            	        
            	            con = getNewConnection2();
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
            	                                  mline.setProdGroup(prodID.trim());
            	                          }
            	                     

            	                          if (discount != 0) {
            	                                  mline.setDiscount(discount);
            	                          }
            	                          if (discount2 != 0) {
        	                                  mline.setDiscount(discount2);
        	                             }
            	                          
            	                           if(paevi < 60 && paevi > 0 ){
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
	  
String  cmd = " SELECT summ.itemid AS toode ,' ' as ladu ,SUM(AVAILPHYSICAL) AS stock    FROM INVENTSUM summ " +
          "  INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " +
          " INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid        " +       
       " WHERE  inv.DATAAREAID='npl' and CLOSED = 0 AND moduletype=0 and dim.inventlocationid in ('B') " +
	"	and summ.itemid like 'NPF%'" +
	"	GROUP BY summ.itemid having sum(availphysical) >0 order by summ.itemid ";


	  

       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
         Connection con = this.getNewConnectionLadu();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);

         while (rs.next()) {

        	 
        	 
           String kood = rs.getString("toode").trim();
           if(kood.startsWith("NPF-")){
        	   kood = kood.substring(4);
           }
           
           double seis = rs.getDouble("stock");
           if(seis > 0){
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

       //kondiitri laoseisud
        cmd = " SELECT summ.itemid AS toode ,' ' as ladu ,SUM(AVAILPHYSICAL) AS stock    FROM bmssa.INVENTSUM summ " +
    	       "     INNER JOIN  bmssa.INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid " +
    	       "    INNER JOIN  bmssa.inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=dim.dataareaid        "+      
    	       "    WHERE  inv.DATAAREAID='k12' and CLOSED = 0 AND moduletype=0 and dim.inventlocationid = 'GPN' "+
    		"	   GROUP BY summ.itemid having sum(availphysical) >0 order by summ.itemid ";


    		  

    	       if (isDebug()) System.out.println(cmd);
    	       try {
    	         Connection con = this.getNewConnection2();
    	         Statement st = con.createStatement();
    	         ResultSet rs = st.executeQuery(cmd);

    	         while (rs.next()) {

    	        	 
    	        	 
    	           String kood = "con-" + rs.getString("toode").trim();
    	          
    	        	 
    	           double seis = rs.getDouble("stock");
    	           if(seis > 0){
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
	  
	  
    String cmd = "select agentsid as agent , name as nimi from bmssa.traagentstable ag INNER JOIN bmssa.empltable emp ON ag.emplid = emp.emplid  and " +
	" ag.dataareaid =emp.dataareaid  INNER JOIN bmssa.TRAAGENTCUSTTABLE  cu ON  cu.agent = agentsid "
	+ " and cu.dataareaid =emp.dataareaid where ag.dataareaid = 'olv' " +
	" group by agentsid,name";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
   // getFixedAssortmentData();
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
          agent.setName(name);
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
         System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
	       }
         }
		
		 agents1.put(agent.getID(), agent);
          agents.put(name, agent);
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
    	Converter.sendAgentJson(agents, rcompany, false);
    }
    return agents;
  }

  public TreeMap getCustomers(Agent agent){

  	String cmd = "SELECT cus.name,cus.accountnum,cus.invoiceaccount AS payer, " + 
  	" (SELECT sum(amountmst ) FROM bmssa.custtransopen co WHERE co.duedate<(getdate()-1)  and  co.dataareaid = 'olv' " +
  	" AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " +
  	" cus.invoiceaccount end )) AS debt,(SELECT sum(amountmst ) FROM bmssa.custtransopen co WHERE  co.dataareaid = 'olv' " +
  	" AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  " +
  	" cus.invoiceaccount end )) AS used, cus.linedisc ,  " +
  	" stbaddressdelivery as address, '' AS visit ,agent AS agent , numofdays AS term, blocked AS block ,  " +
  	" (select max (blocked) from bmssa.custtable cc where cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = 'olv')  " +
    " as payerblock ,phone + ', ' + CellularPhone as info , PriceGroup as grupp , email as mail, '' as abiagent, '' " +
  	" as abipaev ,  creditmax as credit ,stbcustchainid as chain , TraCatalogueCode as fixed , transportthreshold as minam" +
  	" FROM bmssa.custtable cus " +
	" INNER JOIN bmssa.TRAAGENTCUSTTABLE  ag ON  ag.custaccount = cus.accountnum  and ag.dataareaid = cus.dataareaid " +
	" left outer JOIN bmssa.paymterm " +
  	" pay on cus.paymtermid=pay.paymtermid and pay.dataareaid='olv' " +
	" left outer JOIN bmssa.stbcustgrouptable  cb on cb.custgroupid=cus.stbcustgroupid and cb.dataareaid=cus.dataareaid " +
  	" WHERE cus.dataareaid='olv' AND blocked != 2  " +
  	" order by cus.name" ;
 
    String salessql = "  SELECT  jou.orderaccount  as klient,round(SUM(lineamount),0) AS summa,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) as kuu  " + 
     "   FROM bmssa.custinvoicetrans sal  INNER JOIN bmssa.custinvoicejour jou ON  sal.invoiceid = jou.invoiceid  and  " +
     "    jou.DATAAREAID=sal.DATAAREAID WHERE  " +
	 "   sal.dataareaid='olv' AND sal.invoicedate > getdate() -123  GROUP BY jou.orderaccount,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) " +
	 "   ORDER BY jou.orderaccount,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) " ;
  	
    
 String ccc = "select invoiceaccount from bmssa.custtable where dataareaid='olv' group by invoiceaccount";


 
 String sql = "	SELECT  a.accountnum AS klient,a.orderaccount as saaja,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +  
 " co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM bmssa.custtransopen co  " +
 " INNER JOIN bmssa.custtrans a ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
 " WHERE  co.dataareaid='olv' AND co.dataareaid='olv' " +
 " and a.invoice != '' AND co.duedate < getdate()+5 ORDER BY a.accountnum,co.transdate  ";

 
           
           
       //    tal_visitationdays ,talspartnercode,

  if (isDebug()) System.out.println(cmd);
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    Vector payers =  new Vector();
    
    
    
    HashMap salesres = new HashMap();
    
    	ResultSet rs1 ;
    	Statement stmr ;
    	try{
    		
    	Connection con = this.getNewConnection(); 
    		 stmr = con.createStatement();
        	
             rs1 = stmr.executeQuery(salessql);
            while(rs1.next()){
            	String klient = rs1.getString("klient").trim();
            	
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
            	  salesres.put(klient,"   " + kuu+"-"+aasta + " : " + summa + " €" );
              }else{
            	  String asi = (String)salesres.get(klient);     	  
            	  salesres.put(klient,"   " + kuu +"-"+aasta + " : " + summa +" €|" + asi);
              }
            }
           rs1.close();
           stmr.close();
           con.close();
    	}catch(Exception s){
    		s.printStackTrace();
    	}
    
    
    
    
    try {
    	Connection con = this.getNewConnection();
    	 stmr = con.createStatement();
    	
        rs1 = stmr.executeQuery(ccc);
        while(rs1.next()){
         payers.add(rs1.getString("invoiceaccount"));
        }
  
        rs1.close();
        stmr.close();
    	 con = this.getNewConnection();
    	Statement stmt =con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      
      Statement stm = getNewConnection().createStatement();
      while(rs.next()){
        String name = (rs.getString("name").trim());
        
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        boolean addDebt = true;
        
        if(accountnum.startsWith("LV00000")){
        	addDebt = false;
        }
        
        if(salesres.get(accountnum) != null){
        	cust.setInfo("CHART Sales :|Year Month|Sales EUR|" + salesres.get(accountnum) );
        }
        
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
           // cust.setGroup(aleke);
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
                     	  
                           
                         //  System.out.println(aadr.trim()+ "| " +cust.restCity + "* "+ cust.restStreet + " *" + cust.restZipCode);
                       
                   }
               }
               
              
               
        	
           

            
         
            
        	
        	
        	
        	
        	
        	
        	
        	 name = name + " " + Converter.enCodeEst(aadr.trim().replace('\n',' '));
           cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Latvia").
        		  replaceAll("\\r\\n|\\r|\\n", " "));
        }
        
        cust.setName(name);
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
        //  cust.setVisit(visit.trim());
        }
        String agen = rs.getString("agent");
        if (agen != null) {
          cust.addAgent(new Agent(agen.trim()));   
          
          if (agen != null && agen.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
				
				if(agg != null && agg.getDubbler().size()>0){
					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
					//	System.out.println(cust.getName() + cust.getAgents());
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
          cust.setGroup(grupp.trim());
        }
        
     
        
        String confmail = rs.getString("mail");
        if (confmail != null && confmail.trim().length()>0) {
        	if(cust.getPersonalData() != null && cust.getPersonalData().trim().length() > 0 ){
        		 cust.setPersonalData(cust.getPersonalData()+"|" + confmail.trim());
        	}else{
        		 cust.setPersonalData(confmail.trim());
        	}
        }
        
        
        
        String abiagent = rs.getString("abiagent");
        if (abiagent != null && abiagent.trim().length()>0) {
        	Agent ad = new Agent(abiagent);
        	String vis = rs.getString("abipaev");
        	if(vis != null && vis.trim().length() >0 ){
        		ad.setDays(vis);
        	}
          cust.addAgent(ad);
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
		if (fixed != null && fixed.trim().length() > 0) {
			
			cust.addFixSortCode(fixed.trim());
		}
		
		double minam = rs.getDouble("minam");
		if(minam > 0){
			cust.setMinsum(minam);
		}
       
       String cmd2 =  "  SELECT disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
       "  disc.DATAAREAID='olv' AND "+
       "  disc.ACCOUNTCODE=1 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
             rs1 = stm.executeQuery(cmd2);
                     while(rs1.next()){
                       double ale = rs1.getDouble("discount");
                       cust.setDiscount(ale);
                     }
                    rs1.close();
                    
                    
     cmd2 =  "  SELECT disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE disc.accountrelation='" + cust.getPayer() + "' AND " +
                    	       "  disc.DATAAREAID='olv' AND "+
                    	       "  disc.ACCOUNTCODE=0 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
                    	             rs1 = stm.executeQuery(cmd2);
                    	                     while(rs1.next()){
                    	                       double ale = rs1.getDouble("discount");
                    	                       cust.setDiscount(ale);
                    	                     }
                    	                    rs1.close();
                    
                    
                    cust.setPrintean("orkla");
                    boolean laks = true;
                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
                    	laks = false;
                    }
                    
                    if(laks){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                    }
      }
      
      
     
                rs = stmt.executeQuery(sql);
                      while(rs.next()){
                        Debt debt = new Debt();
                        String saaja = rs.getString("saaja").trim();
                        debt.setCustID(saaja);
                        String number = rs.getString("number").trim();
                        debt.setDoknr(number);
                        debt.setDuedate(rs.getDate("maksepaev"));
                        debt.setSum(rs.getDouble("tasumata"));
                        debt.setDate(rs.getDate("kuupaev"));
                        debt.setDue(rs.getString("yle"));
                        if(!saaja.startsWith("LV00000")){
                            this.debt.addElement(debt);
    	                        	
                        }
                      }

           rs.close();
           stmt.close();
           con.close();
    } catch (Exception e){
      getParent().showError(e);
    }
    
    if(rcompany != null){
		// Converter.sendCustomerJson(customers, rcompany, false,user,pass);
	    }
    getCustomersCond(customers);
    
    return customers;
  }


  
  public void getCustomersCond(TreeMap customers){

	  	String cmd = " SELECT cus.name,cus.accountnum,cus.invoiceaccount AS payer, " +
	  	" (SELECT sum(amountmst ) FROM bmssa.custtransopen co WHERE co.duedate<(getdate()-1)  and  co.dataareaid = 'k12' " +
	  	" AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else " +
	  	" cus.invoiceaccount end )) AS debt,(SELECT sum(amountmst ) FROM bmssa.custtransopen co WHERE  co.dataareaid = 'k12' " +
	  	" AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  " +
	  	" cus.invoiceaccount end )) AS used, cus.linedisc ,  " +
	  	" stbaddressdelivery as address, '' AS visit ,agent AS agent , numofdays AS term, blocked AS block , " +
	  	" (select max (blocked) from bmssa.custtable cc where cc.invoiceaccount=cus.invoiceaccount and cc.dataareaid = 'k12')" +  
	    " as payerblock ,phone + ', ' + CellularPhone as info , PriceGroup as grupp , email as mail, '' as abiagent, '' " +
	  	" as abipaev ,  creditmax as credit ,stbcustchainid as chain, TraCatalogueCode as fixed , minsalesamount as minam FROM bmssa.custtable cus " +
		" INNER JOIN bmssa.TRAAGENTCUSTTABLE  ag ON  ag.custaccount = cus.accountnum  and ag.dataareaid = cus.dataareaid " +
		" left outer JOIN bmssa.paymterm  pay on cus.paymtermid=pay.paymtermid and pay.dataareaid='k12'" + 
		"  left outer JOIN bmssa.stbcustchaintable  cb on cb.custchainid=cus.stbcustchainid and cb.dataareaid='k12' " +
	  	" WHERE cus.dataareaid='k12' AND blocked != 2 order by cus.name" ;
	 
	  	String salessql = "  SELECT  jou.orderaccount  as klient,round(SUM(lineamount),0) AS summa,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) as kuu  " + 
	  		     "   FROM bmssa.custinvoicetrans sal  INNER JOIN bmssa.custinvoicejour jou ON  sal.invoiceid = jou.invoiceid  and  " +
	  		     "    jou.DATAAREAID=sal.DATAAREAID WHERE  " +
	  			 "   sal.dataareaid='k12' AND sal.invoicedate > getdate() -123  GROUP BY jou.orderaccount,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) " +
	  			 "   ORDER BY jou.orderaccount,str(datepart(YYYY,sal.invoicedate))+' '+str(datepart(mm,sal.invoicedate)) " ;
	  		  	
	  	
	 String ccc = "select invoiceaccount from bmssa.custtable where dataareaid='k12' group by invoiceaccount";
	 
	 String sql = "	SELECT  a.accountnum AS klient,a.orderaccount as saaja, a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata, " +  
	 " co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM bmssa.custtransopen co  " +
	 " INNER JOIN bmssa.custtrans a ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
	 " WHERE  co.dataareaid='k12' AND co.dataareaid='k12' " +
	 " and a.invoice != '' AND co.duedate < getdate()+5 ORDER BY a.accountnum,co.transdate  ";

	 
	           
	           

	  if (isDebug()) System.out.println(cmd);
	   
	    TreeMap custMap = new TreeMap();
	    Vector payers =  new Vector();
	    
	    
	    
	    HashMap salesres = new HashMap();
	    
	    	ResultSet rs1 ;
	    	Statement stmr ;
	    	try{
	    		
	    	Connection con = this.getNewConnection2(); 
	    		 stmr = con.createStatement();
	        	
	             rs1 = stmr.executeQuery(salessql);
	            while(rs1.next()){
	            	String klient = "con-" + rs1.getString("klient").trim();
	            	
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
	            	  salesres.put(klient,"   " + kuu+"-"+aasta + " : " + summa + " €" );
	              }else{
	            	  String asi = (String)salesres.get(klient);     	  
	            	  salesres.put(klient,"   " + kuu +"-"+aasta + " : " + summa +" €|" + asi);
	              }
	            }
	           rs1.close();
	           stmr.close();
	           con.close();
	    	}catch(Exception s){
	    		s.printStackTrace();
	    	}
	    
	    
	    
	    
	    try {
	    	Connection con = this.getNewConnection2();
	    	 stmr = con.createStatement();
	    	
	        rs1 = stmr.executeQuery(ccc);
	        while(rs1.next()){
	         payers.add(rs1.getString("invoiceaccount"));
	        }
	  
	        rs1.close();
	        stmr.close();
	    	 con = this.getNewConnection2();
	    	Statement stmt =con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      
	      Statement stm = getNewConnection2().createStatement();
	      while(rs.next()){
	        String name = (rs.getString("name").trim());
	        
	        
	        String accountnum = "con-" + rs.getString("accountnum").trim();
	        Customer cust = new Customer(accountnum);

	        boolean addDebt = true;
	        
	        if(accountnum.startsWith("LV00000")){
	        	addDebt = false;
	        }
	        
	        if(salesres.get(accountnum) != null){
	        	cust.setInfo("CHART Sales :|Year  Month|Sales EUR|" + salesres.get(accountnum) );
	        }
	        
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
	           // cust.setGroup(aleke);
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
	                     	  
	                           
	                         //  System.out.println(aadr.trim()+ "| " +cust.restCity + "* "+ cust.restStreet + " *" + cust.restZipCode);
	                       
	                   }
	               }
	               

	        	
	        	 name =  "z " + name + " " + Converter.enCodeEst(aadr.trim().replace('\n',' '));
	           cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ')),"Latvia").
	        		  replaceAll("\\r\\n|\\r|\\n", " "));
	        }
	        cust.setPrintean("konditer");
	        cust.setName(name);
	        String visit = rs.getString("visit");
	        if (visit != null && visit.trim().length()>0) {
	        //  cust.setVisit(visit.trim());
	        }
	        String agen = rs.getString("agent");
	        if (agen != null) {
	        	//System.out.println("aaa " + agen.trim());
	        	agen = "M" + agen.trim().substring(1);
	        	//System.out.println("aaab " + agen.trim());
	        	
	        	cust.addAgent(new Agent(agen.trim()));   
	          
	          if (agen != null && agen.trim().length()>0){
					Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
					
					if(agg != null && agg.getDubbler().size()>0){
						//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
						for (int i = 0 ; i < agg.getDubbler().size() ; i++){
							cust.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
						//	System.out.println(cust.getName() + cust.getAgents());
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
	        if (info != null && info.trim().length()> 2) {
	           if (info.trim().endsWith(",")){
	        	   info = info.substring(0,info.length()-1);
	           }
	           if (info.trim().startsWith(",")){
	        	   info = info.substring(1);
	           }
	        	cust.setPersonalData("Tel:"+info.trim());
	        }
	        
	        String grupp = rs.getString("grupp");
	        if (grupp != null && grupp.trim().length()>0) {
	          cust.setGroup(grupp.trim());
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
				cust.setChain(Converter.enCodeEst(chain.trim().replaceAll("&", "ja")));
			}
			
			 String fixed = rs.getString("fixed");
				if (fixed != null && fixed.trim().length() > 0) {
					cust.addFixSortCode("con-" + fixed.trim());
				}
				
				double minam = rs.getDouble("minam");
				if(minam > 0){
					cust.setMinsum(minam);
				}
				
				 String cmd2 =  "  SELECT disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE disc.accountrelation='" + aleke + "' AND " +
					       "  disc.DATAAREAID='k12' AND "+
					       "  disc.ACCOUNTCODE=1 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
					             rs1 = stm.executeQuery(cmd2);
					                     while(rs1.next()){
					                       double ale = rs1.getDouble("discount");
					                       cust.setDiscount(ale);
					                     }
					                    rs1.close();
					                    
					                    
					     cmd2 =  "  SELECT disc.percent1 AS discount FROM bmssa.pricedisctable disc  WHERE disc.accountrelation='" + cust.getPayer() + "' AND " +
					                    	       "  disc.DATAAREAID='k12' AND "+
					                    	       "  disc.ACCOUNTCODE=0 AND disc.ITEMCODE=2 AND disc.CURRENCY='EUR'";
					                    	             rs1 = stm.executeQuery(cmd2);
					                    	                     while(rs1.next()){
					                    	                       double ale = rs1.getDouble("discount");
					                    	                       cust.setDiscount(ale);
					                    	                     }
					                    	                    rs1.close();
					                    
	                    
	                    
	                    boolean laks = true;
	                    if((payer == null || payer.trim().length() == 0) && payers.contains(cust.getID())){
	                    	laks = false;
	                    }
	                    
	                    if(laks){
	                        customers.put(name, cust);
	                        custMap.put(cust.getID(),cust);	
	                    }
	      }
	      
	      rs = stmt.executeQuery(sql);
	                      while(rs.next()){
	                        Debt debt = new Debt();
	                        String saaja = rs.getString("saaja").trim();
	                        debt.setCustID("con-" + saaja);
	                        String number = rs.getString("number").trim();
	                        debt.setDoknr(number);
	                        debt.setDuedate(rs.getDate("maksepaev"));
	                        debt.setSum(rs.getDouble("tasumata"));
	                        debt.setDate(rs.getDate("kuupaev"));
	                        debt.setDue(rs.getString("yle"));
	                        if(!saaja.startsWith("LV00000")){
	                            this.debt.addElement(debt);
	    	                        	
	                        }
	                      }

	           rs.close();
	           stmt.close();
	           con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	    }
	    
	    if(rcompany != null){
		//	 Converter.sendCustomerJson(customers, rcompany, false,user,pass);
		    }
		
	  }


  
  
  

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  private Connection getNewConnection2() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
  
  private Connection getNewConnectionLadu() throws SQLException{
	    return DriverManager.getConnection(url3, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
      public String getDistribution() {
        return null;
      }

      TreeMap grups = null;

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
     grups = new TreeMap ();
    String    sql = "  SELECT groupid as liik ,sortidx as sorting,name as nimi  FROM bmssa.TraItemSortGroup  where dataareaid ='olv'  ";
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
    getProdGroupsCond(tm);
    return tm;
}

  
  public void  getProdGroupsCond(TreeMap tm) throws SQLException {

	    Connection con = getNewConnection2();
	  
	    String    sql = "  SELECT groupid as liik ,sortidx as sorting,name as nimi FROM bmssa.TraItemSortGroup  where dataareaid ='k12' ";
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
	      grups.put("con-" + liik , "con-" + sort + liik );
	        tm.put( "con-" + sort + liik, sort+ "_" + Converter.enCodeEst(rs.getString("nimi").trim()));
	    }
	    rs.close();
	    st.close();
	    con.close();
 //System.out.println("grups" + grups.toString());
	}

  public TreeMap getProdGroupsSecond() throws SQLException {

    //Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
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

	  String cmd = "  SELECT inv.itemid,inv.itemname  AS nimetus, stbqtyinbox AS kastis,stbqtyinpallet AS alusel, disc.amount AS price   " +
	 "  , bar.itembarcode AS ean ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr ,mod.QUANTITY as opti  FROM bmssa.inventtable inv  " +
	 "  left JOIN bmssa.pricedisctable disc ON inv.itemid=disc.itemrelation and disc.dataareaid= inv.dataareaid  " +
	 "  left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
	 "  left join bmssa.inventitembarcode bar ON bar.dataareaid = inv.dataareaid and bar.itemid=inv.itemid and bar.useforprinting = 1 " +
	  " WHERE disc.dataareaid='olv' and inv.dataareaid='olv'   AND mod.dataareaid='olv'  " +
	  " and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') )  " +
	  " AND moduletype=2 and blocked = 0   "+
	  "  and disc.accountrelation=''";
    
	  String cmd2 = " select groupid as liik,itemid as toode ,sortidx as sorting from"
	  		+ " bmssa.TraItemSortItem where dataareaid ='olv' ";
	  
	  
    getSaveStockInfo();
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
    	
    	getProdGroups();
    	
    Connection con = this.getNewConnection();

    	Statement stmt =  con.createStatement();
    	 String sql2 = "select itemid from bmssa.TraCatalogueItem where dataareaid = 'olv' "
 	    		+ "and cataloguecode like 'LV%' group by itemid ";
 	  
 	 allowedProds = new HashSet();
 	ResultSet rs = stmt.executeQuery(sql2);
 	 while(rs.next()){
 		 allowedProds.add(rs.getString("itemid").trim());
 	 }
 	      
 	 TreeMap liigid = new TreeMap();
 	TreeMap sortcodes = new TreeMap();
 	rs = stmt.executeQuery(cmd2);
	 while(rs.next()){
		 String liik = rs.getString("liik").trim();
		 String kood = rs.getString("toode").trim();
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
		liigid.put(kood, liik);
		sortcodes.put(kood, sort);
	 }
	      
 	 
 	 System.out.println("sortcodes " + sortcodes.toString());
       rs = stmt.executeQuery(cmd);
  
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimetus").trim()  ;
     nimetus =  Converter.enCodeEst(nimetus);
    if(kogused.get(kood) != null){
    	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
    }else{
    	prod.setStock(0);
    }
       prod.setInCases(rs.getDouble("kastis"));
       if(prod.getID().startsWith("N")){
    	      prod.setOnPallet(rs.getDouble("alusel"));
       }
        prod.setPrice(rs.getDouble("price"));

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        add = " " +  ean.trim().substring(ll-3);
      }

      String grupp = (String)liigid.get(kood);
       if(grupp!=null && grupp.trim().length()>0){
    	   if(grups.get(grupp.trim())!= null)
        prod.setGroup(grups.get(grupp.trim())+"");
       }
     
      prod.setName(nimetus + add);

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
      
      
      prod.setType("orkla");
  
      
      String ss =  nimetus + add;
      if(sortcodes.get(kood)!= null){
   	   prod.setNameSorting(sortcodes.get(kood) + nimetus + add); 
   	   ss = sortcodes.get(kood) + nimetus + add;
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
    getProductsCond(products);
    return products;
  }


  public void getProductsCond(TreeMap products){

	  String cmd = "  SELECT inv.itemid,inv.itemname  AS nimetus, stbqtyinbox AS kastis,stbqtyinpallet AS alusel, disc.amount AS price   " +
	 "  , bar.itembarcode AS ean ,inv.itemgroupid AS grupp,mod.unitid As unit, taxitemgroupid as vat,mod.LineDisc as alegr  FROM bmssa.inventtable inv  " +
	 "  left JOIN bmssa.pricedisctable disc ON inv.itemid=disc.itemrelation and disc.dataareaid= inv.dataareaid  " +
	 "  left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  " +
	 "  left join bmssa.inventitembarcode bar ON bar.dataareaid = inv.dataareaid and bar.itemid=inv.itemid and bar.useforprinting = 1 " +
	  " WHERE disc.dataareaid='k12' and inv.dataareaid='k12'   AND mod.dataareaid='k12'  " +
	  " and (disc.todate>getdate() OR disc.todate=('1900-01-01 00:00:00.00') )  " +
	  " AND moduletype=2 and blocked = 0   "+
	  "  and disc.accountrelation=''";
    
   
    if (isDebug()) System.out.println(cmd);
    try {
Connection con = this.getNewConnection2();
    	Statement stmt =  con.createStatement();
    	 String sql2 = " select itemid from bmssa.TraCatalogueItem where dataareaid = 'k12' "
    	    		+ " and cataloguecode not like  ('ek%') " +
					" and cataloguecode not like  ('% lt') and cataloguecode not like  ('% ee') group by itemid ";
    	  
    	ResultSet rs = stmt.executeQuery(sql2);
    	 while(rs.next()){
    		 allowedProds.add("con-" + rs.getString("itemid").trim());
    	 }
    	      
    	
    	 String cmd2 = " select groupid as liik,itemid as toode ,sortidx as sorting"
    	 		+ " from bmssa.TraItemSortItem where dataareaid ='k12' ";
    	 
     	 TreeMap liigid = new TreeMap();
     	TreeMap sortcodes = new TreeMap();
     	rs = stmt.executeQuery(cmd2);
    	 while(rs.next()){
    		 String liik = "con-" + rs.getString("liik").trim();
    		 String kood = "con-" + rs.getString("toode").trim();
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
    		liigid.put(kood, liik);
    		sortcodes.put(kood, sort);
    	 }    	 
    	 
    	 
    	 
       rs = stmt.executeQuery(cmd);
      while(rs.next()){
        String kood = "con-" + rs.getString("itemid").trim();
        Product prod = new Product( kood);
        prod.setType("konditer");
        String nimetus = rs.getString("nimetus").trim()  ;
     nimetus =  Converter.enCodeEst(nimetus);
    if(kogused.get(kood) != null){
    	prod.setStock(Double.parseDouble((String)kogused.get(kood)));
    }
       
    prod.setInCases(rs.getDouble("kastis"));
        prod.setOnPallet(rs.getDouble("alusel"));
        prod.setPrice(rs.getDouble("price"));
        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        add = " " +  ean.trim().substring(ll-3);
      }

   


       String grupp = (String)liigid.get(kood);
        if(grupp!=null && grupp.trim().length()>0){
     	   if(grups.get(grupp.trim())!= null)
         prod.setGroup(grups.get(grupp.trim())+"");
        }
        if(sortcodes.get(kood)!= null){
     	   prod.setNameSorting(sortcodes.get(kood) + nimetus + add);  
        }
       
       
      prod.setName(nimetus + add);

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
      
      
      String ss =  nimetus + add;
      if(sortcodes.get(kood)!= null){
   	   prod.setNameSorting(sortcodes.get(kood) + nimetus + add); 
   	   ss = sortcodes.get(kood) + nimetus + add;
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

  }



  
  public Vector getCustomerProductsList2(String agentCode){
	  Vector custProds = new Vector();
	  return custProds;
  }
	    

  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    //if (true) return custProds ;
	 int counter = 1;

	 String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok " + 
	    		 " FROM  bmssa.custinvoicetrans  tra INNER JOIN  bmssa.custinvoicejour jou ON " +
	    		 " tra.invoiceid=jou.invoiceid and tra.dataareaid =jou.dataareaid  INNER JOIN  bmssa.inventTABLE cus ON cus.itemid=tra.itemid " +
				 " and tra.dataareaid =cus.dataareaid    " +
	    		 " WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid = 'olv'  " +
	    		 " and tra.qty !=0	  GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	    		 " ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " ;


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
	        cp.setAbiKood(bill);

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

	    
	    
    cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok " + 
	    		" FROM  bmssa.custinvoicetrans  tra INNER JOIN  bmssa.custinvoicejour jou ON " +
	    		"  tra.invoiceid=jou.invoiceid and tra.dataareaid =jou.dataareaid  INNER JOIN  bmssa.inventTABLE cus ON cus.itemid=tra.itemid " +
				"  and tra.dataareaid =cus.dataareaid    " +
	    		"  WHERE  jou.invoicedate > getdate()-30 AND tra.dataareaid = 'k12'  " +
	    		"  and tra.qty !=0	  GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	    		"  ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " ;


	    		    if (isDebug()) System.out.println(cmd);

	    		    try {
	    	        Connection con = this.getNewConnection2();
	    		    Statement stmt =  con.createStatement();
	    		      ResultSet rs = stmt.executeQuery(cmd);
	    		      counter++;
	    		      while(rs.next()){
	    		    	  String kliendikood = "con-" + rs.getString("orderaccount").trim();
   		               String tootekood = "con-" + rs.getString("itemid").trim();
	    		        double kogus = rs.getDouble("qty");
	    		  
	    		        String lastdate = rs.getString("lastdate").substring(0,10);
	    		                 lastdate = lastdate.substring(5,10);
	    		                 double hind = rs.getDouble("hind");
	    		     	        String bill = rs.getString("dok"); 
	    		     if(allowedProds.contains(tootekood)){
	    		     	CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	    		        cp.setAbiKood(bill);
                        custProds.add(cp);
	    		         }
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
	    		               String kliendikood = "con-" + rs.getString("orderaccount").trim();
	    		               String tootekood = "con-" + rs.getString("itemid").trim();
	    		               String lastdate = rs.getString("lastdate").substring(0,10);
	    		                        lastdate = lastdate.substring(5,10);
	    		                        if(allowedProds.contains(tootekood)){
	    		               CustProd cp = new CustProd(kliendikood, tootekood, 0 ,0 ,0,lastdate,0,0,0," ");
	    		               cp.setRedline(rs.getString("date"));
	    		               custProds.add(cp);
	    		                        }
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
  this.connectionUp();
    
  String cmd ="  SELECT  traagentsid as agent,round(SUM(lineamount),0) AS summa " +
  " FROM bmssa.custinvoicetrans sal INNER JOIN bmssa.inventtable inv ON sal.itemid=inv.itemid  INNER JOIN bmssa.custinvoicejour jou ON  " + 
  " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN bmssa.custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE " + 
  " inv.dataareaid='olv' AND sal.dataareaid='olv'  AND cus.dataareaid='olv'   AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
  "  AND MONTH(sal.invoicedate) = MONTH(getdate()) GROUP BY traagentsid";


         if (isDebug()) System.out.println(cmd);
            try {
            	Connection con = this.getNewConnection();
                Statement stmt =  con.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
                while(rs.next()) {
                  String agentID = rs.getString("agent").trim();
                  Agent agent = (Agent)agents.get(agentID);
                  if (agent != null){
                    agent.setMonthSales((int)rs.getDouble("summa"));
                   // System.out.println(agent.getMonthSales());
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
  private String url = "jdbc:odbc:orkla";
  private String url2 = "jdbc:odbc:kondit";
  private String url3 = "jdbc:odbc:ladu";
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
	    	    	String toode = prod.getEAN().trim();  
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(toode)){
	                System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
                	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
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
