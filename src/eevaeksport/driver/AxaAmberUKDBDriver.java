package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;
import eevaeksport.util.ReportingService;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaAmberUKDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public AxaAmberUKDBDriver(EksportPanel parent){
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
  //  System.out.println("nodebt .list " +nodebt.toString() );
    NF.setMaximumFractionDigits(4);
    NF.setGroupingUsed(false);
    
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
  private String url = "jdbc:odbc:amberbev";
 // private String url = "jdbc:odbc:lit";
  
  
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

	public static int daysInMonth(int month, int year) {
	    if (month != 1) {
	    	if(month == 11 || month == 0 || month == 2
	    			|| month == 4 || month == 6 || month == 7
	    			|| month == 9 ) {
	    		return 31 ;
	    	}else {
	    		return 30;
	    	}
	        
	    }
	    else {
	        if ((year & 3) == 0 && ((year % 25) != 0 || (year & 15) == 0)) { // leap year
	            return 29;
	        } else {
	            return 28;
	        }
	    }
	}
	
	public static Calendar getWeekStartDate(Calendar calendar) {
		Calendar res = Calendar.getInstance();
		res.setTimeInMillis(calendar.getTimeInMillis());
	    while (res.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
	    	res.add(Calendar.DATE, -1);
	    }
	    res.add(Calendar.DATE, -1);
	    return res;
	}
	

	public static Calendar getWeekEndDate(Calendar calendar) {
		Calendar res = Calendar.getInstance();
		res.setTimeInMillis(calendar.getTimeInMillis());
	    while (res.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
	        res.add(Calendar.DATE, 1);
	    }
	    res.add(Calendar.DATE, -1);
	    res.add(Calendar.DATE, -1);
	    return res;
	}
	
	
	private TreeMap getListingdata(String filename) {
		TreeMap res = new TreeMap();
		try {
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(filename));
	         org.json.simple.JSONArray conList = (org.json.simple.JSONArray) obj;
	         
	         Iterator<org.json.simple.JSONObject> iterat = ((List) conList).iterator();
	         
	         while (iterat.hasNext()) {
	        	 org.json.simple.JSONObject cob = iterat.next();
	        	 String type = (String)cob.get("resstr2");
	        	 if(type != null ) {
	        		String customer = (String)cob.get("custid"); 
	        		String product = (String)cob.get("prodid"); 
	        		String agentid = (String)cob.get("agentid"); 
	        		String comment = (String)cob.get("comment"); 
	        		String sentdate  = (Long)cob.get("sentdate")+""; 
	        		ArrayList arr = new ArrayList();
	        		if(res.get(product) != null) {
	        			arr = (ArrayList)res.get(product);
	        		}
	        		
	        		Toode tt = new Toode(customer);
	        		tt.setGr2(product);
	        		if(agentid != null) {
	        			tt.setBrand(agentid);
	        		}
	        		tt.comment = comment;
	        		
	        		double kogus = 1;
	        		if(type.equals("DELISTED")) {
	        			kogus = -1;
	        		}
	        		tt.setKogus(kogus);
	        		try {
	        			tt.setGr1((String)cob.get("prodname")); 
	        		}catch(Exception k) {
	        			
	        		}
	        		
	        		tt.setGr2(type);
	        		tt.setLadu(sentdate);
	        		
	        		arr.add(tt);
	        		res.put(product, arr);
	        	 }
	        	 
	         }
	         
		}catch(Exception e) {
			e.printStackTrace();
		}
	
        	
		return res;
	}
	
	
	private TreeMap getSelListdata(String filename , String prodnam) {
		TreeMap res = new TreeMap();
		try {
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(filename));
	         org.json.simple.JSONArray conList = (org.json.simple.JSONArray) obj;
	         
	         Iterator<org.json.simple.JSONObject> iterat = ((List) conList).iterator();
	         
	         while (iterat.hasNext()) {
	        	 org.json.simple.JSONObject cob = iterat.next();
	        	 String type = (String)cob.get("resstr2");
	        	 if(type != null ) {
	        		String customer = (String)cob.get("custid"); 
	        		String product = (String)cob.get("prodid"); 
	        		String comment = (String)cob.get("comment"); 
	        		String prodname = (String)cob.get("prodname"); 
	        		Long resint1 = (Long)cob.get("resint1");
	        		Long custeval = (Long)cob.get("custeval");
	        		boolean OK = false;
	        		
	        		if(resint1 == 1) {
	        			OK = true;
	        		}
	        		if(custeval > 0) {
	        			OK = true;
	        		}
	        		
	        		
	        		if(prodnam != null && !prodname.startsWith(prodnam)) {
	        			OK = false;
	        		}
	        		
	        		if(OK) {
	        		
	        		String agentid = (String)cob.get("agentid"); 
	        		String sentdate  = (Long)cob.get("sentdate")+""; 
	        		ArrayList arr = new ArrayList();
	        		if(res.get(product) != null) {
	        			arr = (ArrayList)res.get(product);
	        		}
	        		
	        		Toode tt = new Toode(customer);
	        		tt.setGr2(product);
	        		if(agentid != null) {
	        			tt.setBrand(agentid);
	        		}
	        		
	        		
	        		double kogus = 1;
	        		if(type.equals("DELISTED")) {
	        			kogus = -1;
	        		}
	        		tt.setKogus(kogus);
	        		try {
	        			tt.setGr1((String)cob.get("prodname")); 
	        		}catch(Exception k) {
	        			
	        		}
	        		try {
	        			tt.setKastis(custeval);; 
	        		}catch(Exception k) {
	        			
	        		}
	        		tt.setGr2(type);
	        		tt.setLadu(sentdate);
	        		if(comment != null) {
	        			tt.comment = comment;
	        		}
	        		
	        		arr.add(tt);
	        		res.put(product, arr);
	        	 }
	        	}
	         }
	         
		}catch(Exception e) {
			e.printStackTrace();
		}
	
        	
		return res;
	}
	
	
	private void addDirectCustdata(TreeMap seg, TreeMap sta) {
		 try {
				String  cmd = "SELECT   cus.accountnum as custid, segmentid  FROM custtable cus   	 WHERE   cus.dataareaid in ('ct')  \r\n" + 
						"			   and erpactive = 1  and  internal != " ;
				  Connection con = this.getNewConnection();
			      
				 	Statement stmt =  con.createStatement();
				         ResultSet rs = stmt.executeQuery(cmd);
				      HashMap prodHash = new HashMap();
				      while(rs.next()){
				        String kood = rs.getString("custid").trim();
				        String segment =  rs.getString("segmentid").trim() ;
				      seg.put(kood, segment);
				      sta.put(kood, "direct");
				     }
				      rs.close();
				      stmt.close();
				      con.close();
			  }catch(Exception m) {
			  } 
	}
	
  public String getSaveDebtInfo() {
	  
	   TreeMap<String,Integer> custMPO = new TreeMap<String,Integer>();
	   TreeMap<String,String> custNames = new TreeMap<String,String>();
	   TreeMap<String,String> custPostal = new TreeMap<String,String>();
	   TreeMap<String,String> custCities = new TreeMap<String,String>();
	   TreeMap<String,String> custRegions = new TreeMap<String,String>();
	   TreeMap<String,String> custCategory = getExcelCategories();
	   TreeMap<String,String> productGroup = new TreeMap<String,String>();
	   TreeMap<String,ArrayList> groupProducts = new TreeMap<String,ArrayList>();
	   TreeMap<String,String> productSubGroup = new TreeMap<String,String>();
	   TreeMap<String,ArrayList> subGroupProducts = new TreeMap<String,ArrayList>();
	  
	   TreeMap<String,String> custSegment = getExcelSegments();
	   
		  TreeMap<String,String> custStatus = new TreeMap<String,String>();  
		  addDirectCustdata(custSegment,custStatus);
	
	   try {
			String  cmd = "	select  inv.itemid  , BRANDID as liik , name as nimi from inventtable inv  inner join SPIBRANDTABLE sp\r\n" + 
					"    	        on sp.BRANDID = inv.SPIBRANDID and sp.dataareaid=inv.dataareaid \r\n" + 
					"    	        where sp.dataareaid ='ct'" ;
			  Connection con = this.getNewConnection();
		      
			 	Statement stmt =  con.createStatement();
			         ResultSet rs = stmt.executeQuery(cmd);
			      HashMap prodHash = new HashMap();
			      while(rs.next()){
			        String kood = rs.getString("itemid").trim();
			        String nimetus =  rs.getString("nimi").trim() ;
			        productGroup.put(kood, nimetus);
			        ArrayList arr = new ArrayList();
			        if(groupProducts.get(nimetus) != null) {
			        	arr  = (ArrayList)groupProducts.get(nimetus) ;
			        }
			        arr.add(kood);
			        groupProducts.put(nimetus, arr);
			     }
			      rs.close();
			      stmt.close();
			      con.close();
		  }catch(Exception m) {
		  }
	   
	   
	   try {
			String  cmd = "	select  inv.itemid  , inv.BRANDSUBID as liik , name as nimi from inventtable inv  \r\n" + 
					"	inner join SPIBrandSubTable sp on sp.BRANDSUBID = inv.BrandSubId and sp.dataareaid=inv.dataareaid  \r\n" + 
					"					    	        where sp.dataareaid ='ct' " ;
			  Connection con = this.getNewConnection();
		      
			 	Statement stmt =  con.createStatement();
			         ResultSet rs = stmt.executeQuery(cmd);
			      HashMap prodHash = new HashMap();
			      while(rs.next()){
			        String kood = rs.getString("itemid").trim();
			        String nimetus =  rs.getString("nimi").trim() ;
			        productSubGroup.put(kood, nimetus);
			        ArrayList arr = new ArrayList();
			        if(subGroupProducts.get(nimetus) != null) {
			        	arr  = (ArrayList)subGroupProducts.get(nimetus) ;
			        }
			        arr.add(kood);
			        subGroupProducts.put(nimetus, arr);
			     }
			      rs.close();
			      stmt.close();
			      con.close();
		  }catch(Exception m) {
		  } 
	   
	
	  try {
			JSONArray cc = 	Converter.getWebCustomersAll(rcompany, user, pass);
		
 		int lenn = cc.length();
 		//System.out.println("prod suurused " + groupProducts.size() + " " + productGroup.size() + " " + cc.toString());
 		//fSystem.exit(0);
 		for (int i = 0; i < lenn; i++) {
          
 			JSONObject cob = (JSONObject)cc.get(i);
 			String kood = (String)cob.get("code");
 			String name = (String)cob.get("name");
 			
 			try {
 				Integer mpo = (Integer)cob.get("salespoints");
 	 			
 	 			if(mpo != null && mpo > 0) {
 	 				custMPO.put(kood, mpo);
 	 			}
 			}catch(Exception k) {
 				
 			}
 			
 			
 			
 			if(name != null && name.trim().length() > 0) {
 				custNames.put(kood, name);
 			}
 			
 			try {
 				String city = (String)cob.get("city");
 	 			if(city != null && city.trim().length() > 0) {
 	 				custCities.put(kood, city);
 	 			}
 			}catch(Exception e) {
 				
 			}
 			
 			try {
 				String status = (String)cob.get("direct");
 	 			if(status != null && status.trim().length() > 0) {
 	 				custStatus.put(kood, status);
 	 			}
 			}catch(Exception e) {
 				
 			}
 			
 			
 			
 			try {
 				String postal = (String)cob.get("zipcode");
 	 			if(postal != null && postal.trim().length() > 0) {
 	 				custPostal.put(kood, postal);
 	 			}
 			}catch(Exception e) {
 				
 			}
 			
 			try {
 				String region = (String)cob.get("region");
 	 			if(region != null && region.trim().length() > 0) {
 	 				custRegions.put(kood, region);
 	 			}
 			}catch(Exception e) {
 				
 			}
 			
 		
         }
         
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	  
	
	  

     
	 
	  TreeMap custLevels = new TreeMap();
	  TreeMap levelsCustomers = new TreeMap();
	
	
	  //System.out.println("suurused " + custMPO.size() + " " + custNames.size());
		
	  try {
				Converter.loadCustDimensions("level", rcompany, user, pass);
				custLevels = Converter.getCustDimsLoading("level");
			Iterator itera =	custLevels.keySet().iterator();
			while(itera.hasNext()) {
				String cu =itera.next()+"";
				ArrayList<String> ar = (ArrayList<String>)custLevels.get(cu);
				for(String lev: ar) {
					if(levelsCustomers.get(lev) != null) {
						ArrayList vv = (ArrayList)levelsCustomers.get(lev);
						vv.add(cu);
						levelsCustomers.put(lev, vv);
					} else {
						ArrayList vv = new ArrayList();
						vv.add(cu);
						levelsCustomers.put(lev, vv);
					}
					
				}
			}
		

     //    System.out.println("baone levels "  + custLevels.size() + " " +levelsCustomers.size() );
         
 		 
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	  
	  
	  
	  TreeMap prodNames = new TreeMap();
	  
	  TreeMap tr = new TreeMap();
	   
 	  Converter.getContractsFixedByProd(tr, rcompany, user, pass, true); 
 	  
 	
	  // teeme aruande read valmis ja paneme brandide järgi objekti 
  //paneme branäde lõikes TreeMappi
 	  
 	  TreeMap bybrands = new TreeMap();
 	  TreeMap bybrandsTOT = new TreeMap();
 	
 	 Iterator itemain = tr.keySet().iterator();
 	 while (itemain.hasNext()) {
 		 String prod = itemain.next()+"";
 		 String brand = productGroup.get(prod);
 		 if(brand == null) {
 			brand =  "brand n/a";
 		 }
 		 String subbrand = productSubGroup.get(prod);
 		 if(subbrand == null) {
 			subbrand =  "subbrand n/a";
 		 }
 		 brand = brand + "|" + subbrand;
 		 
 	
 		 
 		 TreeMap byCuGrs = new TreeMap();
 		 if(bybrands.get(brand) != null) {
 			byCuGrs = (TreeMap) bybrands.get(brand);
 		 }

 		TreeMap byCuGrsTOT = new TreeMap();
 		 if(bybrandsTOT.get(brand) != null) {
			byCuGrsTOT = (TreeMap) bybrandsTOT.get(brand);
		 }
		
 		 
 		 ArrayList arrp = (ArrayList)tr.get(prod); 
 		 // siin on see kliendid ja levelid 
 		 for (int y = 0 ; y < arrp.size() ; y++) {
 			 Toode tt = (Toode)arrp.get(y);
 			 String cu = tt.getID();
 		    	prodNames.put(prod, tt.getBrand());
 			
 			 String cuGr =  custCategory.get(cu); 
 			
 			 if(cuGr == null || cuGr.trim().length() == 0) {
 				cuGr = "subsegment n/a";
 			 }
 				
 			 String cuReg = custRegions.get(cu);
 			 
 			 if(cuReg == null || cuReg.trim().length() == 0) {
 				cuReg = "n/a region";
  			 }
 			 cuGr = cuReg + "|" + cuGr;
 			
 			 
 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
 			 if(byCuGrs.get(cuGr) != null) {
 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
 			 }
 			
  			ArrayList too = new ArrayList();
  			 if(cus.get(cu) != null) {
  				 too = (ArrayList)cus.get(cu);
  			 }
  			 too.add(tt);
  			 cus.put(cu,too);
  			 byCuGrs.put(cuGr,cus);
  		   
  			 
  			 
  		   int mpo = 1;
  		   if(custMPO.get(cu) != null) {
  			   mpo = custMPO.get(cu);
  		   }
 			 
 			 Integer cusTOT = 1;
 			 if(byCuGrsTOT.get(cuGr) != null) {
 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
 				 cusTOT = cusTOT+(1*mpo);
 				 byCuGrsTOT.put(cuGr,cusTOT);
 			 } else {
 				 byCuGrsTOT.put(cuGr,cusTOT*mpo);
 			 }
 			 
 			 if(tt.getPlancur() > 0 || tt.getKogus() > 0) {
 				Integer cusTOTCCC = 1;
 				 if(byCuGrsTOT.get(cuGr+"coct") != null) {
 					 cusTOTCCC = (Integer) byCuGrsTOT.get(cuGr+"coct") ;
 					 cusTOTCCC = cusTOTCCC+(1*mpo);
 					 byCuGrsTOT.put(cuGr+"coct",cusTOTCCC);
 				 }else {
 					 byCuGrsTOT.put(cuGr+"coct",cusTOTCCC*mpo);
 				 }	 
 			 }
 			
 			if(tt.getPlanvol() > 0) {
 				Integer cusTOTCCC = 1;
 				 if(byCuGrsTOT.get(cuGr+"pour") != null) {
 					 cusTOTCCC = (Integer) byCuGrsTOT.get(cuGr+"pour") ;
 					 cusTOTCCC = cusTOTCCC+(1*mpo);
 					 byCuGrsTOT.put(cuGr+"pour",cusTOTCCC);
 				 }else {
 					 byCuGrsTOT.put(cuGr+"pour",cusTOTCCC*mpo);
 				 }	 
 			 }
 		 }
 		  
 		
 		 bybrandsTOT.put(brand, byCuGrsTOT); 
 		 bybrands.put(brand, byCuGrs);
 	 }
		    
// 	System.exit(0);
 	
 	 
 	 
 	Calendar seeastaA = Calendar.getInstance();
 	seeastaA.set(Calendar.MONTH, 0);
 	seeastaA.set(Calendar.DAY_OF_MONTH, 1);
		 
	Calendar seeastaL = Calendar.getInstance();
	seeastaL.set(Calendar.MONTH, 11);
	seeastaL.set(Calendar.DAY_OF_MONTH, 31);
 	 
 	  
	  Calendar seekuuA = Calendar.getInstance();
	  seekuuA.set(Calendar.DAY_OF_MONTH, 1);
	  Calendar seekuuL = Calendar.getInstance();
	  seekuuL.set(Calendar.DAY_OF_MONTH, daysInMonth(seekuuL.get(Calendar.MONTH) , seekuuL.get(Calendar.YEAR)) ); 
	  
	   //laeme alla viimase 2 kuu tulemused
	   
	  
	   
	   Calendar eelkuuA = Calendar.getInstance();
	   eelkuuA.add(Calendar.MONTH, -1);
		  eelkuuA.set(Calendar.DAY_OF_MONTH, 1);
		  Calendar eelkuuL = Calendar.getInstance();
		  eelkuuL.add(Calendar.MONTH, -1);
		  eelkuuL.set(Calendar.DAY_OF_MONTH, daysInMonth(eelkuuL.get(Calendar.MONTH) , eelkuuL.get(Calendar.YEAR)) ); 
		  
		 
		  
		  //data on nyyd olemas tuleb sisse parsida lepingud 
		  
		  final DateTime input = new DateTime();
		  //System.out.println(input);
		  final DateMidnight startOfLastWeek = 
		             new DateMidnight(input.minusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY));
		  //System.out.println(startOfLastWeek);
		  final DateMidnight endOfLastWeek = startOfLastWeek.plusDays(6);
		 // System.out.println(endOfLastWeek);
		 
				//	System.exit(0);
		   
			String filenameWeek =   Converter.getSVData(startOfLastWeek.getMillis(), endOfLastWeek.getMillis(), rcompany, user, pass,true,"PRODUCTPROSPECT","LISTED,DELISTED","listing");
				 
			  TreeMap lweek = getListingdata(filenameWeek);
		      TreeMap bybrandsL = new TreeMap();
		      TreeMap bybrandsLTOT = new TreeMap();
		 
		   Iterator itemainL = lweek.keySet().iterator();
		 	 while (itemainL.hasNext()) {
		 		 String prod = itemainL.next()+"";
		 		 String brand = productGroup.get(prod);
		 		 if(brand == null) {
		 			brand =  "brand n/a";
		 		 }
		 		 String subbrand = productSubGroup.get(prod);
		 		 if(subbrand == null) {
		 			subbrand =  "subbrand n/a";
		 		 }
		 		 brand = brand + "|" + subbrand;
		 		 
		 		 TreeMap byCuGrs = new TreeMap();
		 		 if(bybrandsL.get(brand) != null) {
		 			byCuGrs = (TreeMap) bybrandsL.get(brand);
		 		 }
		 		
		 		 TreeMap byCuGrsTOT = new TreeMap();
		 		 if(bybrandsLTOT.get(brand) != null) {
		 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
		 		 }
		 		
		 		 
		 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
		 		 // siin on see kliendid ja levelid 
		 		 for (int y = 0 ; y < arrp.size() ; y++) {
		 			 Toode cut = (Toode)arrp.get(y);
		 			 String cu = cut.getID();
		 			 String cuGr =  custCategory.get(cu); 
		 			 String agentid = cut.getBrand();
		 			 if(cuGr == null || cuGr.trim().length() == 0) {
		 				cuGr = "subsegment n/a";
		 			 }
		 			 
		 			 String cuReg = custRegions.get(cu);
		 			 if(cuReg == null || cuReg.trim().length() == 0) {
		 				cuReg = "n/a region";
		  			 }
		 			 cuGr = cuReg + "|" +cuGr;
		 			 
		 			TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
		 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
		 			 if(byCuGrs.get(cuGr) != null) {
		 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
		 			 }
		 			 
		 			 ArrayList  count = new ArrayList();
		 			 if(cus.get(cu) != null) {
		 				 count = cus.get(cu);
		 			 }
		 			 count.add(cut);
		 			 cus.put(cu,count);
		 			 cus1.put(cu,agentid);
		 			 byCuGrs.put(cuGr,cus);
		 			 byCuGrs.put(cuGr + "XXX",cus1);
		 			 
		 			 int mpo = 1;
		 	  		   if(custMPO.get(cu) != null) {
		 	  			   mpo = custMPO.get(cu);
		 	  		   }
		 	 			 
		 	 			 Integer cusTOT = 1;
		 	 			 if(byCuGrsTOT.get(cuGr) != null) {
		 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
		 	 				 cusTOT = cusTOT+(1*mpo);
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			  } else {
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			 }
		 	 		 }
		 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
		 		bybrandsL.put(brand, byCuGrs);
		 	 }
		  
		 	 
		 	//System.out.println("lweek2 " );
		 	 
		 	 
		 	 
		 	 Calendar eelnadA = Calendar.getInstance();
		 	eelnadA.setTimeInMillis(startOfLastWeek.getMillis());
		 	 Calendar eelnadL = Calendar.getInstance();
		 	eelnadL.setTimeInMillis(endOfLastWeek.getMillis());
		 	
		 	 
		 	 
		  //jooksev nädal 
			  makeListingExcel( bybrands, custNames, custCities,  custMPO , bybrandsL ,eelnadA ,  eelnadL, prodNames, custRegions,productSubGroup,bybrandsTOT,bybrandsLTOT,custSegment,custStatus);
			  System.out.println("lweek3" );
		//-------------------------	  
			 // System.exit(0);
			  
			  //kuu võrdlus tehakse ainult kuu esimesel 7 päeval - muidu ei tee
			 Calendar cal = Calendar.getInstance(); 
			  //if(cal.get(Calendar.DAY_OF_MONTH) < 8) {
				  if(true) {
				
				String filenameLmonth =   Converter.getSVData(eelkuuA.getTime(), eelkuuL.getTime(), rcompany, user, pass,false,"PRODUCTPROSPECT","LISTED,DELISTED","listing");
				 
				  lweek = getListingdata(filenameLmonth);
		 		
			 bybrandsL = new TreeMap();
			 bybrandsLTOT = new TreeMap();
			
			 	  itemainL = lweek.keySet().iterator();
			 	 while (itemainL.hasNext()) {
			 		 String prod = itemainL.next()+"";
			 		 String brand = productGroup.get(prod);
			 		 
			 		
			 		 if(brand == null) {
			 			brand =  "brand n/a";
			 		 }
			 		 
			 		 String subbrand = productSubGroup.get(prod);
			 		 if(subbrand == null) {
			 			subbrand =  "subbrand n/a";
			 		 }
			 		 
			 		 brand = brand + "|" + subbrand;
			 		 
			 		 TreeMap byCuGrs = new TreeMap();
			 		 if(bybrandsL.get(brand) != null) {
			 			byCuGrs = (TreeMap) bybrandsL.get(brand);
			 		 }
			 		
			 		 
			 		 TreeMap byCuGrsTOT = new TreeMap();
			 		 if(bybrandsLTOT.get(brand) != null) {
			 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
			 		 }
			 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
			 		 // siin on see kliendid ja levelid 
			 		 for (int y = 0 ; y < arrp.size() ; y++) {
			 			 Toode cut = (Toode)arrp.get(y);
			 			 String cu = cut.getID();
			 			 String cuGr =  custCategory.get(cu); 
			 			 String agentid = cut.getBrand();
			 			
			 			 if(cuGr == null || cuGr.trim().length() == 0) {
			 				cuGr = "subsegment n/a";
			 			 }
			 			 String cuReg = custRegions.get(cu);
			 			 
			 			 if(cuReg == null || cuReg.trim().length() == 0) {
			 				cuReg = "n/a region";
			  			 }
			 			 cuGr = cuReg + "|" +cuGr;
			 			 
			 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
			 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
			 			 if(byCuGrs.get(cuGr) != null) {
			 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
			 			 }
			 			 
			 			 ArrayList  count = new ArrayList();
			 			 if(cus.get(cu) != null) {
			 				 count = cus.get(cu);
			 			 }
			 			 count.add(cut);
			 			 
			 			 cus.put(cu,count);
			 			 cus1.put(cu,agentid);
			 			 byCuGrs.put(cuGr,cus);
			 			 byCuGrs.put(cuGr + "XXX",cus1);
			 			 
			 			 int mpo = 1;
			 	  		   if(custMPO.get(cu) != null) {
			 	  			   mpo = custMPO.get(cu);
			 	  		   }
			 	 			 
			 	 			 Integer cusTOT = 1;
			 	 			 if(byCuGrsTOT.get(cuGr) != null) {
			 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
			 	 				 cusTOT = cusTOT+(1*mpo);
			 	 				 byCuGrsTOT.put(cuGr,cusTOT);
			 	 			  } else {
			 	 				 byCuGrsTOT.put(cuGr,cusTOT);
			 	 			 }
			 	 		 }
			 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
			 		 
			 		bybrandsL.put(brand, byCuGrs);
			 	 }
			 	  makeListingExcel( bybrands, custNames, custCities,  custMPO , bybrandsL ,eelkuuA ,  eelkuuL , prodNames, custRegions,productSubGroup,bybrandsTOT,bybrandsLTOT,custSegment,custStatus);
			 //---------------------------------- 
			 	// System.exit(0);
			 	  
			  }  
			  
	   
		//aasta jooksev.   	
			  String filenameCyear =  Converter.getSVData(seeastaA.getTime(), seeastaL.getTime(), rcompany, user, pass,false,"PRODUCTPROSPECT","LISTED,DELISTED","listing");
				 
			  lweek = getListingdata(filenameCyear);
		//  System.out.println(lweek.toString());
			
		 bybrandsL = new TreeMap();
		
		 	  itemainL = lweek.keySet().iterator();
		 	 while (itemainL.hasNext()) {
		 		 String prod = itemainL.next()+"";
		 		 String brand = productGroup.get(prod);
		 		 if(brand == null) {
		 			brand =  "brandin n/a";
		 		 }
		 		 String subbrand = productSubGroup.get(prod);
		 		 if(subbrand == null) {
		 			subbrand =  "subbrand n/a";
		 		 }
		 		 brand = brand + "|" + subbrand;
		 		 
		 		 
		 		 TreeMap byCuGrsTOT = new TreeMap();
		 		 if(bybrandsLTOT.get(brand) != null) {
		 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
		 		 }
		 		 TreeMap byCuGrs = new TreeMap();
		 		 if(bybrandsL.get(brand) != null) {
		 			byCuGrs = (TreeMap) bybrandsL.get(brand);
		 		 }
		 		
		 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
		 		 // siin on see kliendid ja levelid 
		 		 for (int y = 0 ; y < arrp.size() ; y++) {
		 			 Toode cut = (Toode)arrp.get(y);
		 			 String cu = cut.getID();
		 			 String cuGr =  custCategory.get(cu); 
		 			 String agentid = cut.getBrand();
		 			
		 			 if(cuGr == null || cuGr.trim().length() == 0) {
		 				cuGr = "subsegment n/a";
		 			 }
		 			 
		 			 String cuReg = custRegions.get(cu);
		 			 
		 			 if(cuReg == null || cuReg.trim().length() == 0) {
		 				cuReg = "n/a region";
		  			 }
		 			 cuGr = cuReg + "|" +cuGr;
		 			 
		 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
		 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
		 			 if(byCuGrs.get(cuGr) != null) {
		 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
		 			 }
		 			 
		 			 
		 			 ArrayList  count = new ArrayList();
		 			 if(cus.get(cu) != null) {
		 				 count = cus.get(cu);
		 			 }
		 			 count.add(cut);
		 			 
		 			 cus.put(cu,count);
		 			 cus1.put(cu,agentid);
		 			 byCuGrs.put(cuGr,cus);
		 			 byCuGrs.put(cuGr + "XXX",cus1);
		 			 int mpo = 1;
		 	  		   if(custMPO.get(cu) != null) {
		 	  			   mpo = custMPO.get(cu);
		 	  		   }
		 	 			 
		 	 			 Integer cusTOT = 1;
		 	 			 if(byCuGrsTOT.get(cuGr) != null) {
		 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
		 	 				 cusTOT = cusTOT+(1*mpo);
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			  } else {
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			 }
		 	 			 
		 	 			
		 	 			 
		 	 		 }
		 		 
		 		
		 		 
		 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
		 		bybrandsL.put(brand, byCuGrs);
		 	 }
		 	  makeListingExcel( bybrands, custNames,  custCities , custMPO , bybrandsL ,seeastaA ,  seeastaL , prodNames, custRegions, productSubGroup,bybrandsTOT,bybrandsLTOT,custSegment,custStatus);
		 	 
		 	 makeListingExcelCurrent( bybrands, custNames,  custCities , custMPO , bybrandsL ,seeastaA ,  seeastaL , prodNames, custRegions, productSubGroup,bybrandsTOT,bybrandsLTOT,custPostal,custSegment,custStatus);
		 	 
		 	 
		 	 
		 	 
		 	 
		 	 // teeme Prospecti faili ka valmis 
		 	 	//aasta jooksev.   	
		 filenameCyear =  Converter.getSVData(seeastaA.getTime(), seeastaL.getTime(), rcompany, user, pass,false,"PRODUCTPROSPECT","GREEN,YELLOW,RED","prospect");
						 
					  lweek = getListingdata(filenameCyear);
				//  System.out.println(lweek.toString());
					
				 bybrandsL = new TreeMap();
				 bybrandsLTOT = new TreeMap();
				
				 	  itemainL = lweek.keySet().iterator();
				 	 while (itemainL.hasNext()) {
				 		 String prod = itemainL.next()+"";
				 		 String brand = productGroup.get(prod);
				 		 if(brand == null) {
				 			brand =  "brandin n/a";
				 		 }
				 		 String subbrand = productSubGroup.get(prod);
				 		 if(subbrand == null) {
				 			subbrand =  "subbrand n/a";
				 		 }
				 		 brand = brand + "|" + subbrand;
				 		 
				 		 
				 		 TreeMap byCuGrsTOT = new TreeMap();
				 		 if(bybrandsLTOT.get(brand) != null) {
				 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
				 		 }
				 		 TreeMap byCuGrs = new TreeMap();
				 		 if(bybrandsL.get(brand) != null) {
				 			byCuGrs = (TreeMap) bybrandsL.get(brand);
				 		 }
				 		
				 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
				 		
				 		 for (int y = 0 ; y < arrp.size() ; y++) {
				 			 Toode cut = (Toode)arrp.get(y);
				 			 String cu = cut.getID();
				 			 String cuGr =  custCategory.get(cu); 
				 			 String agentid = cut.getBrand();
				 			 String tyyp = cut.getGr2();
				 			
				 			 if(cuGr == null || cuGr.trim().length() == 0) {
				 				cuGr = "subsegment n/a";
				 			 }
				 			 
				 			 String cuReg = custRegions.get(cu);
				 			 
				 			 if(cuReg == null || cuReg.trim().length() == 0) {
				 				cuReg = "n/a region";
				  			 }
				 			 cuGr = cuReg + "|" +cuGr;
				 			 
				 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
				 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
				 			 if(byCuGrs.get(cuGr) != null) {
				 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
				 			 }
				 			 
				 			 
				 			 ArrayList  count = new ArrayList();
				 			 if(cus.get(cu) != null) {
				 				 count = cus.get(cu);
				 			 }
				 			 count.add(cut);
				 			 
				 			 cus.put(cu,count);
				 			 cus1.put(cu,agentid);
				 			 byCuGrs.put(cuGr,cus);
				 			 byCuGrs.put(cuGr + "XXX",cus1);
				 			 int mpo = 1;
				 	  		   if(custMPO.get(cu) != null) {
				 	  			   mpo = custMPO.get(cu);
				 	  		   }
				 	 			 
				 	 			 Integer cusTOT = 1;
				 	 			 if(byCuGrsTOT.get(cuGr) != null) {
				 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
				 	 				 cusTOT = cusTOT+(1*mpo);
				 	 				 byCuGrsTOT.put(cuGr,cusTOT);
				 	 			  } else {
				 	 				 byCuGrsTOT.put(cuGr,cusTOT);
				 	 			 }
				 	 			
				 	 		 }
				 		 
				 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
				 		bybrandsL.put(brand, byCuGrs);
				 		// System.out.println("byCuGrs " + byCuGrs.toString());
				 	 }
		 	// System.out.println(bybrandsL.toString());
		 	 
	    makeProspectExcelCurrent( custNames,  custCities , custMPO , bybrandsL ,seeastaA ,  seeastaL , prodNames, custRegions, productSubGroup,
		 			 bybrandsLTOT,custPostal,custSegment,custStatus);
	 
	 
	 
	 
	 
	 
 	 // teeme POS faili ka valmis 
	 	//aasta jooksev.   	
       filenameCyear =  Converter.getSVData(seeastaA.getTime(), seeastaL.getTime(), rcompany, user, pass,false,"SELLIST",null,"pos");
				 
			  lweek = getSelListdata(filenameCyear,"POS");
		//  System.out.println(lweek.toString());
			
		 bybrandsL = new TreeMap();
		 bybrandsLTOT = new TreeMap();
		
		 	  itemainL = lweek.keySet().iterator();
		 	 while (itemainL.hasNext()) {
		 		 String prod = itemainL.next()+"";
		 		 String brand = productGroup.get(prod);
		 		 if(brand == null) {
		 			brand =  "brandin n/a";
		 		 }
		 		 String subbrand = productSubGroup.get(prod);
		 		 if(subbrand == null) {
		 			subbrand =  "subbrand n/a";
		 		 }
		 		 brand = brand + "|" + subbrand;
		 		 
		 		 
		 		 TreeMap byCuGrsTOT = new TreeMap();
		 		 if(bybrandsLTOT.get(brand) != null) {
		 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
		 		 }
		 		 TreeMap byCuGrs = new TreeMap();
		 		 if(bybrandsL.get(brand) != null) {
		 			byCuGrs = (TreeMap) bybrandsL.get(brand);
		 		 }
		 		
		 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
		 		
		 		 for (int y = 0 ; y < arrp.size() ; y++) {
		 			 Toode cut = (Toode)arrp.get(y);
		 			 String cu = cut.getID();
		 			 String cuGr =  custCategory.get(cu); 
		 			 String agentid = cut.getBrand();
		 			 String tyyp = cut.getGr2();
		 			
		 			 if(cuGr == null || cuGr.trim().length() == 0) {
		 				cuGr = "subsegment n/a";
		 			 }
		 			 
		 			 String cuReg = custRegions.get(cu);
		 			 
		 			 if(cuReg == null || cuReg.trim().length() == 0) {
		 				cuReg = "n/a region";
		  			 }
		 			 cuGr = cuReg + "|" +cuGr;
		 			 
		 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
		 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
		 			 if(byCuGrs.get(cuGr) != null) {
		 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
		 			 }
		 			 
		 			 
		 			 ArrayList  count = new ArrayList();
		 			 if(cus.get(cu) != null) {
		 				 count = cus.get(cu);
		 			 }
		 			 count.add(cut);
		 			 
		 			 cus.put(cu,count);
		 			 cus1.put(cu,agentid);
		 			 byCuGrs.put(cuGr,cus);
		 			 byCuGrs.put(cuGr + "XXX",cus1);
		 			 int mpo = 1;
		 	  		   if(custMPO.get(cu) != null) {
		 	  			   mpo = custMPO.get(cu);
		 	  		   }
		 	 			 
		 	 			 Integer cusTOT = 1;
		 	 			 if(byCuGrsTOT.get(cuGr) != null) {
		 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
		 	 				 cusTOT = cusTOT+(1*mpo);
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			  } else {
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			 }
		 	 			
		 	 		 }
		 		 
		 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
		 		bybrandsL.put(brand, byCuGrs);
		 		// System.out.println("byCuGrs " + byCuGrs.toString());
		 	 }
	 //System.out.println(bybrandsL.toString());

	 
	 makePosExcelCurrent( custNames,  custCities , custMPO , bybrandsL ,seeastaA ,  seeastaL , prodNames, custRegions, productSubGroup,
 			 bybrandsLTOT,custPostal,custSegment,custStatus);
	 
	 
	 
	 
 	 // teeme Koolituste faili ka valmis 
	 	//aasta jooksev.   	
filenameCyear =  Converter.getSVData(seeastaA.getTime(), seeastaL.getTime(), rcompany, user, pass,false,"SELNUMLIST",null,"training");
				 
			  lweek = getSelListdata(filenameCyear,"Educa");
		//  System.out.println(lweek.toString());
			
		 bybrandsL = new TreeMap();
		 bybrandsLTOT = new TreeMap();
		
		 	  itemainL = lweek.keySet().iterator();
		 	 while (itemainL.hasNext()) {
		 		 String prod = itemainL.next()+"";
		 		 String brand = productGroup.get(prod);
		 		 if(brand == null) {
		 			brand =  "brandin n/a";
		 		 }
		 		 String subbrand = productSubGroup.get(prod);
		 		 if(subbrand == null) {
		 			subbrand =  "subbrand n/a";
		 		 }
		 		 brand = brand + "|" + subbrand;
		 		 
		 		 
		 		 TreeMap byCuGrsTOT = new TreeMap();
		 		 if(bybrandsLTOT.get(brand) != null) {
		 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
		 		 }
		 		 TreeMap byCuGrs = new TreeMap();
		 		 if(bybrandsL.get(brand) != null) {
		 			byCuGrs = (TreeMap) bybrandsL.get(brand);
		 		 }
		 		
		 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
		 		
		 		 for (int y = 0 ; y < arrp.size() ; y++) {
		 			 Toode cut = (Toode)arrp.get(y);
		 			 String cu = cut.getID();
		 			 String cuGr =  custCategory.get(cu); 
		 			 String agentid = cut.getBrand();
		 			 String tyyp = cut.getGr2();
		 			
		 			 if(cuGr == null || cuGr.trim().length() == 0) {
		 				cuGr = "subsegment n/a";
		 			 }
		 			 
		 			 String cuReg = custRegions.get(cu);
		 			 
		 			 if(cuReg == null || cuReg.trim().length() == 0) {
		 				cuReg = "n/a region";
		  			 }
		 			 cuGr = cuReg + "|" +cuGr;
		 			 
		 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
		 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
		 			 if(byCuGrs.get(cuGr) != null) {
		 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
		 			 }
		 			 
		 			 
		 			 ArrayList  count = new ArrayList();
		 			 if(cus.get(cu) != null) {
		 				 count = cus.get(cu);
		 			 }
		 			 count.add(cut);
		 			 
		 			 cus.put(cu,count);
		 			 cus1.put(cu,agentid);
		 			 byCuGrs.put(cuGr,cus);
		 			 byCuGrs.put(cuGr + "XXX",cus1);
		 			 int mpo = 1;
		 	  		   if(custMPO.get(cu) != null) {
		 	  			   mpo = custMPO.get(cu);
		 	  		   }
		 	 			 
		 	 			 Integer cusTOT = 1;
		 	 			 if(byCuGrsTOT.get(cuGr) != null) {
		 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
		 	 				 cusTOT = cusTOT+(1*mpo);
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			  } else {
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			 }
		 	 			
		 	 		 }
		 		 
		 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
		 		bybrandsL.put(brand, byCuGrs);
		 		// System.out.println("byCuGrs " + byCuGrs.toString());
		 	 }
	 
	 makeEducationExcelCurrent( custNames,  custCities , custMPO , bybrandsL ,seeastaA ,  seeastaL , prodNames, custRegions, productSubGroup,
 			 bybrandsLTOT,custPostal,custSegment,custStatus);

	 

 	 // teeme POS faili ka valmis 
	 	//aasta jooksev.   	
filenameCyear =  Converter.getSVData(seeastaA.getTime(), seeastaL.getTime(), rcompany, user, pass,false,"SELLIST",null,"activation");
				 
			  lweek = getSelListdata(filenameCyear,"Activ");
		//  System.out.println(lweek.toString());
			
		 bybrandsL = new TreeMap();
		 bybrandsLTOT = new TreeMap();
		
		 	  itemainL = lweek.keySet().iterator();
		 	 while (itemainL.hasNext()) {
		 		 String prod = itemainL.next()+"";
		 		 String brand = productGroup.get(prod);
		 		 if(brand == null) {
		 			brand =  "brandin n/a";
		 		 }
		 		 String subbrand = productSubGroup.get(prod);
		 		 if(subbrand == null) {
		 			subbrand =  "subbrand n/a";
		 		 }
		 		 brand = brand + "|" + subbrand;
		 		 
		 		 
		 		 TreeMap byCuGrsTOT = new TreeMap();
		 		 if(bybrandsLTOT.get(brand) != null) {
		 			byCuGrsTOT = (TreeMap) bybrandsLTOT.get(brand);
		 		 }
		 		 TreeMap byCuGrs = new TreeMap();
		 		 if(bybrandsL.get(brand) != null) {
		 			byCuGrs = (TreeMap) bybrandsL.get(brand);
		 		 }
		 		
		 		 ArrayList arrp = (ArrayList)lweek.get(prod); 
		 		
		 		 for (int y = 0 ; y < arrp.size() ; y++) {
		 			 Toode cut = (Toode)arrp.get(y);
		 			 String cu = cut.getID();
		 			 String cuGr =  custCategory.get(cu); 
		 			 String agentid = cut.getBrand();
		 			 String tyyp = cut.getGr2();
		 			
		 			 if(cuGr == null || cuGr.trim().length() == 0) {
		 				cuGr = "subsegment n/a";
		 			 }
		 			 
		 			 String cuReg = custRegions.get(cu);
		 			 
		 			 if(cuReg == null || cuReg.trim().length() == 0) {
		 				cuReg = "n/a region";
		  			 }
		 			 cuGr = cuReg + "|" +cuGr;
		 			 
		 			 TreeMap<String, ArrayList> cus = new TreeMap<String, ArrayList>();
		 			TreeMap<String, String> cus1 = new TreeMap<String, String>();
		 			 if(byCuGrs.get(cuGr) != null) {
		 				 cus = (TreeMap<String, ArrayList>) byCuGrs.get(cuGr) ;
		 			 }
		 			 
		 			 
		 			 ArrayList  count = new ArrayList();
		 			 if(cus.get(cu) != null) {
		 				 count = cus.get(cu);
		 			 }
		 			 count.add(cut);
		 			 
		 			 cus.put(cu,count);
		 			 cus1.put(cu,agentid);
		 			 byCuGrs.put(cuGr,cus);
		 			 byCuGrs.put(cuGr + "XXX",cus1);
		 			 int mpo = 1;
		 	  		   if(custMPO.get(cu) != null) {
		 	  			   mpo = custMPO.get(cu);
		 	  		   }
		 	 			 
		 	 			 Integer cusTOT = 1;
		 	 			 if(byCuGrsTOT.get(cuGr) != null) {
		 	 				 cusTOT = (Integer) byCuGrsTOT.get(cuGr) ;
		 	 				 cusTOT = cusTOT+(1*mpo);
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			  } else {
		 	 				 byCuGrsTOT.put(cuGr,cusTOT);
		 	 			 }
		 	 			
		 	 		 }
		 		 
		 	 	bybrandsLTOT.put(brand, byCuGrsTOT); 
		 		bybrandsL.put(brand, byCuGrs);
		 		// System.out.println("byCuGrs " + byCuGrs.toString());
		 	 }
	 //System.out.println(bybrandsL.toString());

	 
		 	makeActivationExcelCurrent( custNames,  custCities , custMPO , bybrandsL ,seeastaA ,  seeastaL , prodNames, custRegions, productSubGroup,
 			 bybrandsLTOT,custPostal,custSegment,custStatus);
	 
	 
		  System.exit(0);
   	return null;
   }

     private void makeListingExcel(TreeMap bybrands, TreeMap custNames,TreeMap custCities,TreeMap custMPO 
    		 , TreeMap bybrandsL ,Calendar algus , Calendar lopp , TreeMap prodNames , TreeMap  custRegions ,TreeMap prodSubBrands,TreeMap bybrandsTOT
    		 ,TreeMap bybrandsLTOT ,TreeMap custSegment , TreeMap custStatus) {
		  
    	 
    	
    	 
    	 
		  	try{
		  		TreeMap manas = getExcelManas();
				String fname = this.getExcelDir()  + "listbase.xlsx";

				  FileInputStream fIPS= new FileInputStream(fname);
			        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
			        
			        Font smFont = wb.createFont();
			        smFont.setFontName("Arial");
			        smFont.setFontHeightInPoints((short) 10);
			        // titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			    
			   
			        
			        
			        
			        XSSFSheet worksheet = wb.getSheetAt(0);
			        XSSFRow row1 = worksheet.getRow(2); 
			        Cell c = row1.getCell(15);
			             c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
			             row1 = worksheet.getRow(4); 
			             c = row1.getCell(8);
			             c.setCellValue(Converter.getDDpMMpYYYYpString(algus));
			             c = row1.getCell(10);
			             c.setCellValue(Converter.getDDpMMpYYYYpString(lopp));
			        //   TreeMap bybrandsLX = (TreeMap)bybrandsL.clone();
			             int loe = 0;
			             Iterator itr = bybrands.keySet().iterator();
			           
			             while (itr.hasNext()) {
			            	 String brand = (String) itr.next();
			            	 
			            	 TreeMap bysegL = (TreeMap)bybrandsL.get(brand);
			            	 boolean hasL= false;
			            	 if(bysegL != null) {
			            		 hasL = true;
			            	 }
			            	
			            	 TreeMap byseg = (TreeMap)bybrands.get(brand);
			            	  
			            	 
			            	 Iterator itra = byseg.keySet().iterator();
				             while (itra.hasNext()) {
				            	 String segm = (String) itra.next();
				            	
				            	 TreeMap<String,ArrayList> custsL = null;
				            	 TreeMap<String,String> custsLag = null;
				            	 
				            	 if(hasL && bysegL.keySet().contains(segm)) {
				            		 custsL = (TreeMap<String,ArrayList>)bysegL.get(segm);
				            		 custsLag = (TreeMap<String,String>)bysegL.get(segm+"XXX");
				            		// System.out.println("siin1 "  + custsL.toString() + " " + segm + " "+ brand);
				            	 }
				            	 
				            	 
				            	 TreeMap<String,ArrayList> custs = (TreeMap<String,ArrayList>)byseg.get(segm);
				            	
				            	// System.out.println("test " + brand + " " + segm + " "+  custs.toString().substring(0,20));
				            	 Iterator itrasr = custs.keySet().iterator();
					             while (itrasr.hasNext()) {
					            //	 System.out.println("siin0 " + custs.keySet().size());
				            		String cuid = (String)itrasr.next();
				            		//System.out.println("siin " + custs.get(cuid));
				            		ArrayList coun = custs.get(cuid);
				            		String cuName = (String)custNames.get(cuid);
				            		String cuCity = (String)custCities.get(cuid);
				            		String segment = (String)custSegment.get(cuid);
				            		String status = (String)custStatus.get(cuid);
				            		
				            		ArrayList aa = (ArrayList)manas.get(cuid);
				            	    String mana = null;
				            				if(aa != null) {
				            					String a = (String)aa.get(0);
				            					Agent ag = (Agent)agent1.get(a);
				            					if(ag != null) {
				            						mana = a + ":" + ag.getName();
				            					}else {
				            						mana = a;
				            					}
				            					
				            					try {
				            						 a = (String)aa.get(1);
					            					 	Agent ag1 = (Agent)agent1.get(a);
						            					if(ag1 != null) {
						            						mana = mana + ","+ a + ":" + ag1.getName();
						            					}else {
						            						mana = mana + ","+ a;
						            					}
				            					}catch (Exception k) {
				            						
				            					}
				            					
				            					
				            				}
				            		
				            		
				            		ArrayList counL = null;
				            		
				            		if(custsL != null && custsL.get(cuid) != null) {
				            			counL = custsL.get(cuid);
				            			custsL.remove(cuid); // maha kui on olemas sortimendis 
				            			//if(cuid.contains("00607"))
				            			//System.out.println("suurus " + brand +" " + custsL.size() + " " + counL);
				            		}
				            		
				            		
				            		
				            		
				            		
				            	
				            		for(int y = 0; y<coun.size();y++) {
				            			Toode ttt = (Toode)coun.get(y);
				            			String mmm = null;
				            		int arv = 0;
				            		 if(counL != null) {
				            			// System.out.println("suurus " + cuid + " " + counL);
				            			 for(int u = 0; u < counL.size(); u++) {
				            				 Toode ty = (Toode)counL.get(u);
				            				// if(cuid.contains("00607"))
				            				// System.out.println("tt " + ttt.getGr1() + "\n " + ty.getGr2() + "\n " + ty.getKogus() + "\n" + ty.toString());
				            				 if (ty.getGr2().equals(ttt.getGr1())){
				            					 arv = (int)ty.getKogus();
				            					 mmm = ty.getBrand();
				            				 }
				            			 }
				            		 }
				            		
				            		
				            		//Teeme rea siia 
				            		row1 = worksheet.getRow(6+loe); 
				            		//if(row1 != null) {
				            		if(row1 == null) {
				            			row1 = worksheet.createRow(6+loe);
				            		}
				            		
				            		c = row1.getCell(1);
			            			  if (c == null) {
			            			        c = row1.createCell(1);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String brr = brand.substring(0,brand.indexOf("|"));
			            			  c.setCellValue(brr);
			            			  
			            			  c = row1.getCell(2);
			            			  if (c == null) {
			            			        c = row1.createCell(2);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String subb = "n/a subbrand";
			            			  try {
			            				  subb = brand.substring(brand.indexOf("|") + 1);
			            			  } catch (Exception e) {}
			            			  
			            			  c.setCellValue(subb);
			            			  
			            			  c = row1.getCell(3);
			            			  if (c == null) {
			            			        c = row1.createCell(3);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			
			            			  String regb = "n/a region";
			            			  try {
			            				  regb  = segm.substring(0,segm.indexOf("|"));
			            			  } catch (Exception e) {}
			            			
			            			  c.setCellValue(regb);
				            		  
				            		  c = row1.getCell(4);
				            		  if (c == null) {
			            			        c = row1.createCell(4);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
				            		
				            		  
				            		  c = row1.getCell(5);
				            		  if (c == null) {
			            			        c = row1.createCell(5);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(status);
				            		  
				            		  c = row1.getCell(6);
				            		  if (c == null) {
			            			        c = row1.createCell(6);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segment);
				            		  
				            		  
				            		// itemname siia 
				            		  
				            		  c = row1.getCell(7);
				            		  if (c == null) {
			            			        c = row1.createCell(7);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(ttt.getBrand() != null) {
				            			  c.setCellValue(ttt.getGr1() + " " + ttt.getBrand());	  
				            		  }
				            		  
				            		  c = row1.getCell(8);
				            		  if (c == null) {
			            			        c = row1.createCell(8);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(cuCity != null) {
				            			  c.setCellValue(cuCity);	  
				            		  }
				            		
				            		  c = row1.getCell(9);
			            			  if (c == null) {
			            			        c = row1.createCell(9);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		if(custsLag != null && custsLag.get(cuid)!= null) {
				            			
				            	  c.setCellValue( custsLag.get(cuid) + ":" + ((Agent)agent1.get(custsLag.get(cuid))).getName());
				            		} else if (mmm != null) {
				            			 c.setCellValue( mmm + ":" + ((Agent)agent1.get(mmm)).getName());
				            		}
				            		

				            		 
				            		
				            		if(mana != null) {
				            			c = row1.getCell(10);
				            			  if (c == null) {
				            			        c = row1.createCell(10);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
				            			  c.setCellValue(mana);
				            		}
				            			  
					            		  
					            		  c = row1.getCell(11);
					            		  if (c == null) {
				            			        c = row1.createCell(11);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  c.setCellValue(cuName);
					            		  
					            		
					            		  
					            		  
					            		  int pp = 1;
					            		  if(custMPO.get(cuid) != null && Integer.parseInt(custMPO.get(cuid)+"") > 1) {
					            			  pp = Integer.parseInt(custMPO.get(cuid)+"");
					            			  c = row1.getCell(12);
						            		  if (c == null) {
					            			        c = row1.createCell(12);
					            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			    }
						            		
						            		  c.setCellValue(pp);  
						            		  // System.out.println("test MPO " + brand + " " + loe + " " + cuid + " " + cuName + " " + custMPO.get(cuid) );
					            		     }else {
					            		    	 c = row1.getCell(12);
							            		  if (c == null) {
						            			        c = row1.createCell(12);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    } 
					            		     }
					            		
					            		  c = row1.getCell(13);
					            		  if (c == null) {
				            			        c = row1.createCell(13);
				            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			    }
					            		 
					            			  c.setCellValue(1 * pp - arv * pp );  
					            		  
					            		 
					            		  		
					            		/*  if(arv != 0) {
					            			  System.out.println("test arv "+ arv + " " + brand + 
					            					  " " + loe + " " + cuid + " " + cuName + " " + custMPO.get(cuid) );
						            		      
					            		  }*/
					            		  
					            		  c = row1.getCell(14);
						            		  if (c == null) {
					            			        c = row1.createCell(14);
					            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			    }
						            		  c.setCellValue(arv * pp);
					            		 
					            		 
					            		  
					            		  c = row1.getCell(15);
					            		  if (c == null) {
				            			        c = row1.createCell(15);
				            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			    }
					            		    c.setCellValue(1 * pp);


					            		   Cell c1 = row1.getCell(16);
						            		  if (c1 == null) {
					            			        c1 = row1.createCell(16);
					            			        c1.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			        CellStyle ff = c1.getCellStyle();
					            			        ff.setFont(smFont);
					            			        c1.setCellStyle(ff);
					            			    }
						            		  if(ttt.getPlancur() == 1) {
						            		    c1.setCellValue(1); //coctail
						            		  }
					            		  
						            		  c1 = row1.getCell(17);
						            		  if (c1 == null) {
					            			        c1 = row1.createCell(17);
					            			        c1.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			        CellStyle ff = c1.getCellStyle();
					            			        ff.setFont(smFont);
					            			        c1.setCellStyle(ff);
					            			    }
						            		  if(ttt.getPlanvol() == 1) {
						            		    c1.setCellValue(1); //pouring
						            		  }
						            		  
				            		loe++;
				            		}				            	 
					             
				            		}
					             
					             
					             if(custsL != null && custsL.size() > 0 && false) {
					            	 
					            	 //custsLag = (TreeMap<String,String>)bysegL.get(segm+"XXX"); 
					           
					            
					            	 Iterator itrasL = custsL.keySet().iterator();
						             while (itrasL.hasNext()) {
						            	 
					            		String cuid = (String)itrasL.next();
					            		String cuName = (String)custNames.get(cuid);
					            		String cuCity = (String)custCities.get(cuid);
					            	//	System.out.println("customerID " + cuid);
					            		String segment = (String)custSegment.get(cuid);
					            		String status = (String)custStatus.get(cuid);
					            		ArrayList	counL = custsL.get(cuid);
					            		
					            		for(int u =0 ; u < counL.size(); u++) {
					            			Toode hhh = (Toode)counL.get(u);
					            		//System.out.println(hhh.toString());
					            		
					            		//Teeme rea siia 
					            		row1 = worksheet.getRow(6+loe); 
					            		//if(row1 != null) {
					            		if(row1 == null) {
					            			row1 = worksheet.createRow(6+loe);
					            		}
					            		ArrayList aa = (ArrayList)manas.get(cuid);
					            		String mana = null;
			            			
					            		if(aa != null) {
			            					String a = (String)aa.get(0);
			            					Agent ag = (Agent)agent1.get(a);
			            					if(ag != null) {
			            						mana = a + ":" + ag.getName();
			            					}else {
			            						mana = a;
			            					}
			            				}
					            		
					            		c = row1.getCell(1);
				            			  if (c == null) {
				            			        c = row1.createCell(1);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
				            			  String brr = brand.substring(0,brand.indexOf("|"));
				            			  c.setCellValue(brr);
					            		
				            			  
				            			  
				            			  c = row1.getCell(2);
				            			  if (c == null) {
				            			        c = row1.createCell(2);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
				            			  String subb = "n/a subbrand";
				            			  try {
				            				  subb = brand.substring(brand.indexOf("|") + 1 );
				            			  } catch (Exception e) {}
				            			  
				            			  c.setCellValue(subb);
				            			  
				            			  c = row1.getCell(3);
				            			  if (c == null) {
				            			        c = row1.createCell(3);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
				            			
				            			  String regb = "n/a region";
				            			  try {
				            				  regb  = segm.substring(0,segm.indexOf("|"));
				            			  } catch (Exception e) {}
				            			
				            			  c.setCellValue(regb);
					            		  
					            		  c = row1.getCell(4);
					            		  if (c == null) {
				            			        c = row1.createCell(4);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
					            		
					            		  c = row1.getCell(5);
					            		  if (c == null) {
				            			        c = row1.createCell(5);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  c.setCellValue(status);
					            		  c = row1.getCell(6);
					            		  if (c == null) {
				            			        c = row1.createCell(6);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  c.setCellValue(segment);
					            		  
					            		  
					            		  c = row1.getCell(7);
					            		  if (c == null) {
				            			        c = row1.createCell(7);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		 
					            		  if(hhh.getGr1() != null) {
					            			  c.setCellValue(hhh.getGr2() + " " + hhh.getGr1());	  
					            		  }
					            		  
					            		  
					            		  
					            		  c = row1.getCell(8);
					            		  if (c == null) {
				            			        c = row1.createCell(8);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  if(cuCity != null) {
					            			  c.setCellValue(cuCity);	  
					            		  }
					            		  
					            		  c = row1.getCell(9);
				            			  if (c == null) {
				            			        c = row1.createCell(9);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  
			            				if(custsLag != null && custsLag.get(cuid)!= null) {
					            			
					            			  c.setCellValue( custsLag.get(cuid) + ":" + ((Agent)agent1.get(custsLag.get(cuid))).getName());
					            		}else {
					            			  c.setCellValue( hhh.getBrand() + ":" + ((Agent)agent1.get(hhh.getBrand())).getName());
								            		
					            		}
			            				
			            				
			            				
					            		if(mana != null) {
					            			c = row1.getCell(10);
					            			  if (c == null) {
					            			        c = row1.createCell(10);
					            			        c.setCellType(Cell.CELL_TYPE_STRING);
					            			    }
					            			  c.setCellValue(mana);
					            		}
					            			  
						            		  
						            		  c = row1.getCell(11);
						            		  if (c == null) {
					            			        c = row1.createCell(11);
					            			        c.setCellType(Cell.CELL_TYPE_STRING);
					            			    }
						            		  c.setCellValue(cuName);
						            		  
						            		  
						            		  int pp = 1; // mpo
						            		  if(custMPO.get(cuid) != null && Integer.parseInt(custMPO.get(cuid)+"") > 1) {
						            			  pp = Integer.parseInt(custMPO.get(cuid)+"");
						            			  c = row1.getCell(12);
							            		  if (c == null) {
						            			        c = row1.createCell(12);
						            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
						            			    }
							            		
							            		  c.setCellValue(pp);  
							            		  // System.out.println("test MPO " + brand + " " + loe + " " + cuid + " " + cuName + " " + custMPO.get(cuid) );
						            		     }else {
						            		    	 c = row1.getCell(12);
								            		  if (c == null) {
							            			        c = row1.createCell(12);
							            			        c.setCellType(Cell.CELL_TYPE_STRING);
							            			    } 
						            		     }
						            		  
						            		  boolean nega = false;
						            		  
						            		  if(hhh.getKogus() * pp < 0) {
						            			  nega = true;
						            		  }
						            		  if(nega) {
						            			  c = row1.getCell(13);
							            		  if (c == null) {
						            			        c = row1.createCell(13);
						            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
						            			    }
							            		  c.setCellValue(hhh.getKogus() * pp * -1);  
						            		  }
						            		
						            			  c = row1.getCell(14);
							            		  if (c == null) {
						            			        c = row1.createCell(14);
						            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
						            			    }
							            		  c.setCellValue(hhh.getKogus() * pp);
						            		 
						            		  
						            		  c = row1.getCell(15);
						            		  if (c == null) {
					            			        c = row1.createCell(15);
					            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			    }
						            		  
						            		  c.setCellValue(hhh.getKogus() * pp );
						            		
						            		  Cell c1 = row1.getCell(16);
						            		  if (c1 == null) {
					            			        c1 = row1.createCell(16);
					            			        c1.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			        CellStyle ff = c1.getCellStyle();
					            			        ff.setFont(smFont);
					            			        c1.setCellStyle(ff);
					            			    }
						            		  if(hhh.getPlancur() == 1) {
						            		    c1.setCellValue(1); //coctail
						            		  }
					            		  
						            		  c1 = row1.getCell(17);
						            		  if (c1 == null) {
					            			        c1 = row1.createCell(17);
					            			        c1.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			        CellStyle ff = c1.getCellStyle();
					            			        ff.setFont(smFont);
					            			        c1.setCellStyle(ff);
					            			    }
						            		  if(hhh.getPlanvol() == 1) {
						            		    c1.setCellValue(1); //pouring
						            		  }
						            		  if(nega) {
						            			 // c.setCellValue(0);  
						            		  }
					            			loe++;
					            	 }}
					             }
					         }
				         }
			             
			             //siin lõpeb 1 sheediga tegelemine ja võtame käsile 2 sheedi 
			             
			             
			             
			             
			             
			           worksheet = wb.getSheetAt(1);
					        row1 = worksheet.getRow(2); 
					         c = row1.getCell(7);
					             c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
					             row1 = worksheet.getRow(4); 
					             c = row1.getCell(3);
					             c.setCellValue(Converter.getDDpMMpYYYYpString(algus));
					             c = row1.getCell(5);
					             c.setCellValue(Converter.getDDpMMpYYYYpString(lopp));
					        //   TreeMap bybrandsLX = (TreeMap)bybrandsL.clone();
					              loe = 0;
					              itr = bybrandsTOT.keySet().iterator();
					         // System.out.println("bybrandsTOT suurus " + bybrandsTOT.size());
					              
					             while (itr.hasNext()) {
					            	 String brand = (String) itr.next();
					            
					            	 TreeMap bysegL = (TreeMap)bybrandsLTOT.get(brand);
					            	 boolean hasL= false;
					            	 if(bysegL != null) {
					            		 hasL = true;
					            	 }
					            	
					            	 TreeMap byseg = (TreeMap)bybrandsTOT.get(brand);
					            	  
					            	/* if(brand.contains("Cherry H")) {
					            		 System.out.print("byseg " + Converter.getDDpMMpYYYYpString(algus) 
					            		 + " " + byseg.toString() );
					            		 if(bysegL != null) {
					            			 System.out.println( bysegL.toString());
					            		 }else {
					            			 System.out.println(""); 
					            		 }
					            		
					            	 }*/
					            	
					            	 
					            	 Iterator itra = byseg.keySet().iterator();
						             while (itra.hasNext()) {
						            	 String segm = (String) itra.next();
						            	 if(!segm.endsWith("pour") && !segm.endsWith("coct")) {
						            		

						            	 Integer custsL = null;
						            	Integer custsLag = null;
						            	 
						            	 if(hasL && bysegL.keySet().contains(segm)) {
						            		 custsL = (Integer)bysegL.get(segm);
						            		 custsLag = (Integer)bysegL.get(segm+"XXX");
						            		// System.out.println("siin1 "  + custsL.toString() + " " + segm + " "+ brand);
						            	 }
						            	 
						            	 
						                 int ttt = (Integer)byseg.get(segm);
						                 Integer   ccc = (Integer)byseg.get(segm + "coct");
						                 Integer   ppp = (Integer)byseg.get(segm + "pour");
						               
						                 
						            		int arv  = 0;
						            		if(custsL != null) {
						            			arv = custsL;
						            			bysegL.remove(segm); // eemaldame et seda mitte lisada lõpus
						            			/*if(brand.contains("Cherry H")) {
								            		 System.out.println("arv "  + segm + " " + arv );
								            	 }*/	
						            		}
						            		
						            		
						            		//Teeme rea siia 
						            		row1 = worksheet.getRow(6+loe); 
						            		//if(row1 != null) {
						            		if(row1 == null) {
						            			row1 = worksheet.createRow(6+loe);
						            		}
						            		
						            		c = row1.getCell(1);
					            			  if (c == null) {
					            			        c = row1.createCell(1);
					            			        c.setCellType(Cell.CELL_TYPE_STRING);
					            			    }
					            			  String brr = brand.substring(0,brand.indexOf("|"));
					            			  c.setCellValue(brr);
					            			  
					            			  c = row1.getCell(2);
					            			  if (c == null) {
					            			        c = row1.createCell(2);
					            			        c.setCellType(Cell.CELL_TYPE_STRING);
					            			    }
					            			  
					            			  String subb = "n/a subbrand";
					            			  try {
					            				  subb = brand.substring(brand.indexOf("|") + 1);
					            			  } catch (Exception e) {}
					            			  c.setCellValue(subb);
					            			  
					            			  c = row1.getCell(3);
					            			  if (c == null) {
					            			        c = row1.createCell(3);
					            			        c.setCellType(Cell.CELL_TYPE_STRING);
					            			    }
					            			
					            			  String regb = "n/a region";
					            			  try {
					            				  regb  = segm.substring(0,segm.indexOf("|"));
					            			  } catch (Exception e) {}
					            			  c.setCellValue(regb);
						            		  
						            		  c = row1.getCell(4);
						            		  if (c == null) {
					            			        c = row1.createCell(4);
					            			        c.setCellType(Cell.CELL_TYPE_STRING);
					            			    }
						            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
						            		
						            			  // kui palju on alguse ja vaat amuutusi ka 
						            		  
						            		  
						            		  
						            		 
							            		  c = row1.getCell(5);
								            		  if (c == null) {
							            			        c = row1.createCell(5);
							            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
							            			    }
								            		  if(arv < 0 ) {
								            			  c.setCellValue(ttt -arv ); 
								            		  }else {
								            			  c.setCellValue(ttt - arv );  
								            		  }
							            		 
							            		 
							            		  
							            		  c = row1.getCell(6);
							            		  if (c == null) {
						            			        c = row1.createCell(6);
						            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
						            			    }
							            		  
							            		  if(arv == -1 ) {
							            			  c.setCellValue(0);
							            		  }else {
							            			  c.setCellValue(arv);
							            		  }
							            		  
							            		  
							            		  c = row1.getCell(7);
							            		  if (c == null) {
						            			        c = row1.createCell(7);
						            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
						            			    }
							            			  c.setCellValue(ttt);
							            			 
							            			  
							            			  if(ccc != null) {//kokteili stuff
							            			  c = row1.getCell(8);
								            		  if (c == null) {
							            			        c = row1.createCell(8);
							            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
							            			    }
								            			  c.setCellValue(ccc);
						                                   }
								            			  
							            			  if(ppp != null) { //pouring staff
							            				  c = row1.getCell(9);
									            		  if (c == null) {
								            			        c = row1.createCell(9);
								            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
								            			    }
									            			  c.setCellValue(ppp);
									            	  }	 
							            			  
							            			  
						            	loe++;  // järgmine rida 				            	 
						            	 
						            	 }
						             }
							         // siin sai yks piirkond läbi - nyyd vaatame kas seal uusi ridu pole    
							             
							             if(bysegL != null && bysegL.size() > 0 && false) {
							            	 
							            	 //custsLag = (TreeMap<String,String>)bysegL.get(segm+"XXX"); 
							            	/* if(brand.contains("Cherry H")) {
							            		 System.out.println("bysegL " + Converter.getDDpMMpYYYYpString(algus) + " " + bysegL.toString());
							            	 }*/
							            // siin on ainult miinus cased siis 
							            	 Iterator itrasL = bysegL.keySet().iterator();
							            	 
								             while (itrasL.hasNext()) {
								            	String aaa = itrasL.next()+""; 
								            //	System.out.println(aaa + " " + segm);
								            
								            	
								            	int custsL = (Integer)bysegL.get(aaa);
							            		
							            		//Teeme rea siia 
							            		row1 = worksheet.getRow(6+loe); 
							            		//if(row1 != null) {
							            		if(row1 == null) {
							            			row1 = worksheet.createRow(6+loe);
							            		}
							            		
							            		c = row1.getCell(1);
						            			  if (c == null) {
						            			        c = row1.createCell(1);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    }
						            			 String brr = brand.substring(0,brand.indexOf("|"));
						            			  c.setCellValue(brr);
							            		  
						            			  c = row1.getCell(2);
						            			  if (c == null) {
						            			        c = row1.createCell(2);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    }
						            			  String subb = "n/a subbrand";
						            			  try {
						            				  subb = brand.substring(brand.indexOf("|") + 1 );
						            			  } catch (Exception e) {}
						            			  
						            			  c.setCellValue(subb);
						            			  
						            			  c = row1.getCell(3);
						            			  if (c == null) {
						            			        c = row1.createCell(3);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    }
						            			
						            			 String  regb = "n/a region";
						            			  try {
						            				  regb  = aaa.substring(0,aaa.indexOf("|"));
						            			  } catch (Exception e) {}
						            			
						            			  c.setCellValue(regb);
							            		  
							            		  c = row1.getCell(4);
							            		  if (c == null) {
						            			        c = row1.createCell(4);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    }
							            		  c.setCellValue(aaa.substring(aaa.indexOf("|")+1));
							            		
							            		//  System.out.println("uued muutused " + brand +" "+ aaa + " " + custsL);
							            		  
							            		
					            				    int pp = 1;
								            		  boolean nega = false;
								            		  if(custsL * pp < 0) {
								            			  nega = true;
								            		  }
								            		  if(nega) {
								            			  c = row1.getCell(5);
									            		  if (c == null) {
								            			        c = row1.createCell(5);
								            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
								            			    }
									            		  c.setCellValue(custsL * pp * -1);  
								            		   }
								            		
								            			  c = row1.getCell(6);
									            		  if (c == null) {
								            			        c = row1.createCell(6);
								            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
								            			    }
									            		  c.setCellValue(custsL * pp);
								            		 
								            		  
								            		  c = row1.getCell(7);
								            		  if (c == null) {
							            			        c = row1.createCell(7);
							            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
							            			    }
								            		  c.setCellValue(custsL * pp );
								            		  if(nega) {
								            			  c.setCellValue(0);  
								            		  }
								            		  loe++;	
								            	}
							            	 }
								        }
					     
			             
			 		    fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("listings/Listing- " + Converter.getDDpMMpYYYYpString(algus)+" - " + Converter.getDDpMMpYYYYpString(lopp) + ".xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					

		}catch(Exception e) {
		   e.printStackTrace();	
		} 
     }
  

     private void makeListingExcelCurrent(TreeMap bybrands, TreeMap custNames,TreeMap custCities,TreeMap custMPO 
    		 , TreeMap bybrandsL ,Calendar algus , Calendar lopp , TreeMap prodNames , TreeMap  custRegions ,TreeMap prodSubBrands,TreeMap bybrandsTOT
    		 ,TreeMap bybrandsLTOT, TreeMap  custPostal , TreeMap custSegment , TreeMap custStatus) {
		  
		  	try{
		  		TreeMap manas = getExcelManas();
				String fname = this.getExcelDir()  + "currentlistbase.xlsx";
				
				//XMl moodustamine 
				  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				     DocumentBuilder db = dbf.newDocumentBuilder();
			        Document doc = db.newDocument();
			        Element root = doc.createElement("CurrentListings");
			        doc.appendChild(root);

				
				//Exceli sisselugemine

				  FileInputStream fIPS= new FileInputStream(fname);
			        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
			        XSSFSheet worksheet = wb.getSheetAt(0);
			        XSSFRow row1 = worksheet.getRow(3); 
			        Cell c = row1.getCell(4);
			            c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
			            
			             int loe = 0;
			             Iterator itr = bybrands.keySet().iterator();
			           
			             while (itr.hasNext()) {
			            	 String brand = (String) itr.next();
			            	 TreeMap bysegL = (TreeMap)bybrandsL.get(brand);
			            	 boolean hasL= false;
			            	 if(bysegL != null) {
			            		 hasL = true;
			            	 }
			            	
			            	 TreeMap byseg = (TreeMap)bybrands.get(brand);
			            	 Iterator itra = byseg.keySet().iterator();
				             while (itra.hasNext()) {
				            	 String segm = (String) itra.next();
				            	
				            	 TreeMap<String,ArrayList> custsL = null;
				            	 TreeMap<String,String> custsLag = null;
				            	 
				            	 if(hasL && bysegL.keySet().contains(segm)) {
				            		 custsL = (TreeMap<String,ArrayList>)bysegL.get(segm);
				            		 custsLag = (TreeMap<String,String>)bysegL.get(segm+"XXX");
				            		// System.out.println("siin1 "  + custsL.toString() + " " + segm + " "+ brand);
				            	 }
				            	 
				            	 
				            	 TreeMap<String,ArrayList> custs = (TreeMap<String,ArrayList>)byseg.get(segm);
				            	
				            	// System.out.println("test " + brand + " " + segm + " "+  custs.toString().substring(0,20));
				            	 Iterator itrasr = custs.keySet().iterator();
					             while (itrasr.hasNext()) {
					            	 
					            	
					            	 
					            //	 System.out.println("siin0 " + custs.keySet().size());
				            		String cuid = (String)itrasr.next();
				            		//System.out.println("siin " + custs.get(cuid));
				            		ArrayList coun = custs.get(cuid);
				            		String cuName = (String)custNames.get(cuid);
				            		String cuCity = (String)custCities.get(cuid);
				            		String postal = (String)custPostal.get(cuid);
				            		String segment = (String)custSegment.get(cuid);
				            		String status = (String)custStatus.get(cuid);
				            		
				            		ArrayList aa = (ArrayList)manas.get(cuid);
				            	    String mana = null;
				            				if(aa != null) {
				            					String a = (String)aa.get(0);
				            					Agent ag = (Agent)agent1.get(a);
				            					if(ag != null) {
				            						mana = a + ":" + ag.getName();
				            					}else {
				            						mana = a;
				            					}
				            					
				            					try {
				            						 a = (String)aa.get(1);
					            					 	Agent ag1 = (Agent)agent1.get(a);
						            					if(ag1 != null) {
						            						mana = mana + ","+ a + ":" + ag1.getName();
						            					}else {
						            						mana = mana + ","+ a;
						            					}
				            					}catch (Exception k) {
				            						
				            					}
				            					
				            					
				            				}
				            		
				            		
				            		ArrayList counL = null;
				            		
				            		if(custsL != null && custsL.get(cuid) != null) {
				            			counL = custsL.get(cuid);
				            			custsL.remove(cuid); // maha kui on olemas sortimendis 
				            			//if(cuid.contains("00607"))
				            			//System.out.println("suurus " + brand +" " + custsL.size() + " " + counL);
				            		}
				            		
				            		
				            		
				            		
				            		
				            	
				            		for(int y = 0; y<coun.size();y++) {
				            			Toode ttt = (Toode)coun.get(y);
				            			String mmm = null;
				            		int arv = 0;
				            		 if(counL != null) {
				            			// System.out.println("suurus " + cuid + " " + counL);
				            			 for(int u = 0; u < counL.size(); u++) {
				            				 Toode ty = (Toode)counL.get(u);
				            				// if(cuid.contains("00607"))
				            				// System.out.println("tt " + ttt.getGr1() + "\n " + ty.getGr2() + "\n " + ty.getKogus() + "\n" + ty.toString());
				            				 if (ty.getGr2().equals(ttt.getGr1())){
				            					 arv = (int)ty.getKogus();
				            					 mmm = ty.getBrand();
				            				 }
				            			 }
				            		 }
				            		 Node listline = doc.createElement("CurrentListing");
					 		         root.appendChild(listline);
					 		        
					 		  
				            		
				            		//Teeme rea siia 
				            		row1 = worksheet.getRow(5+loe); 
				            		//if(row1 != null) {
				            		if(row1 == null) {
				            			row1 = worksheet.createRow(5+loe);
				            		}
				            		
				            		c = row1.getCell(1);
			            			  if (c == null) {
			            			        c = row1.createCell(1);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String brr = brand.substring(0,brand.indexOf("|"));
			            			  c.setCellValue(brr);
			            			  this.addNode(listline, doc, "Brand", brr);
			            		    	  
			            			  
			            			  c = row1.getCell(2);
			            			  if (c == null) {
			            			        c = row1.createCell(2);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String subb = "n/a subbrand";
			            			  try {
			            				  subb = brand.substring(brand.indexOf("|") + 1);
			            			  } catch (Exception e) {}
			            			  
			            			  c.setCellValue(subb);
			            			
						 		       this.addNode(listline, doc, "BrandSub", subb);
				            		      
			            			  
			            			  c = row1.getCell(3);
			            			  if (c == null) {
			            			        c = row1.createCell(3);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			
			            			  String regb = "n/a region";
			            			  try {
			            				  regb  = segm.substring(0,segm.indexOf("|"));
			            			  } catch (Exception e) {}
			            			
			            			  c.setCellValue(regb);
				            		  this.addNode(listline, doc, "Region", regb);
			            		      
			            			  
				            		
				            		  c = row1.getCell(4);
				            		  if (c == null) {
			            			        c = row1.createCell(4);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(status);
				            		  
				            		  this.addNode(listline, doc, "Status", status);
			            		       
				            		  c = row1.getCell(5);
				            		  if (c == null) {
			            			        c = row1.createCell(5);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segment);
				            		  this.addNode(listline, doc, "Segment", segment);
			            		      
				            		  
				            		  c = row1.getCell(6);
				            		  if (c == null) {
			            			        c = row1.createCell(6);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
				            		  
				            		  
				            		  this.addNode(listline, doc, "SegmentSub", segm.substring(segm.indexOf("|")+1));
				            		
				            		  
				            		  c = row1.getCell(7);
				            		  if (c == null) {
			            			        c = row1.createCell(7);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(ttt.getBrand() != null) {
				            			  c.setCellValue(ttt.getGr1() + " " + ttt.getBrand());	 
				            			  addNode(listline, doc, "ProductCode", ttt.getGr1());
				            			  addNode(listline, doc, "ProductName", ttt.getBrand());
						            		
				            		  }
				            		  
				            		  c = row1.getCell(8);
				            		  if (c == null) {
			            			        c = row1.createCell(8);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(cuCity != null) {
				            			  c.setCellValue(cuCity);	
				            			  addNode(listline, doc, "CustomerCity", cuCity);
				            		  }
				            		  
				            		  
				            		  c = row1.getCell(9);
				            		  if (c == null) {
			            			        c = row1.createCell(9);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(postal != null) {
				            			  c.setCellValue(postal);	
				            			  addNode(listline, doc, "CustomerPostal", postal);
				            		  }
				            		  
				            		  c = row1.getCell(10);
				            		  if (c == null) {
			            			        c = row1.createCell(10);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  String since = ttt.getLadu();
				            		  try {

					            		  if(since != null) {
					            			  Calendar cc = Calendar.getInstance();
					            			  cc.setTimeInMillis(Long.parseLong(since));
					            			  c.setCellValue(Converter.getDDpMMpYYYYpString(cc));	
					            			  addNode(listline, doc, "DateSince", Converter.getYYYYmMMmDDmString(cc.getTime()));
					            		  }  
				            		  }catch(Exception e) {
				            			  
				            		  }
				            		  
				            		  c = row1.getCell(11);
				            		  if (c == null) {
			            			        c = row1.createCell(11);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  String till = ttt.getGr2();
				            		  try {

					            		  if(till != null) {
					            			 
					            			  c.setCellValue(till.substring(8,10) + "." +till.substring(5,7) + "." +
					            					  till.substring(0,4));	  
					            			  addNode(listline, doc, "DateTill",till.substring(8,10) + "." +till.substring(5,7) + "." +
					            					  till.substring(0,4) );
					            		  }  
				            		  }catch(Exception e) {
				            			  
				            		  }
				            		  
				            		
				            		  c = row1.getCell(12);
			            			  if (c == null) {
			            			        c = row1.createCell(12);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			        CellStyle cellStyle = c.getCellStyle();
			            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
			            			        c.setCellStyle(cellStyle);
			            			    }
				            		  
				            		if(custsLag != null && custsLag.get(cuid)!= null) {
				            		     c.setCellValue( custsLag.get(cuid) + ":" + ((Agent)agent1.get(custsLag.get(cuid))).getName());
				            		     addNode(listline, doc, "Modified", custsLag.get(cuid) + ":" + ((Agent)agent1.get(custsLag.get(cuid))).getName());
				            		  } else if (mmm != null) {
				            			 c.setCellValue( mmm + ":" + ((Agent)agent1.get(mmm)).getName());
				            		     addNode(listline, doc, "Modified",  mmm + ":" + ((Agent)agent1.get(mmm)).getName());
						            		
				            			 
				            		}
				            		
				            		
				            		if(mana != null) {
				            			c = row1.getCell(13);
				            			  if (c == null) {
				            			        c = row1.createCell(13);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
				            			  c.setCellValue(mana);
				            			  addNode(listline, doc, "CustomerManager",  mana);
				            		}
				            			  
					            	  c = row1.getCell(14);
					            		  if (c == null) {
				            			        c = row1.createCell(14);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
					            		  c.setCellValue(cuName);
					            		  addNode(listline, doc, "CustomerName",  cuName);
					            		  //cocktail num
					            		  
					            		  int cnum = 0;
                                        String dealtype = null;
					            		  if(ttt.getPlancur() == 1) {
					            			  dealtype = "c";
					            			  cnum = 1;
					            		  }
					            		  
					            		
					            		  if(ttt.getKogus() > 1) {
					            			  cnum = (int) ttt.getKogus();
					            		  }
					            		  
					            	Cell c12 = row1.getCell(15);
					            		  if(cnum > 0) {
					            		  if (c12 == null) {
				            			        c12 = row1.createCell(15);
				            			        c12.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			        CellStyle cellStyle = c12.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c12.setCellStyle(cellStyle);
				            			    }
					            			  c12.setCellValue(cnum);
					            			  addNode(listline, doc, "Coctail",  cnum+"");
					            		  }
					            		
					            		
					            		  
                                          
					            		  //cocktail status 
					            		  
					            		  Cell c14 = row1.getCell(16);
					            		  if(dealtype != null) {
					            		  if (c14 == null) {
				            			        c14 = row1.createCell(16);
				            			        c14.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c14.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c14.setCellStyle(cellStyle);
				            			    }
					            			  c14.setCellValue(dealtype);
					            			  addNode(listline, doc, "CoctailBase",  "true");
					            		  }
					            		
					            		  
					            		//pouring  
					            		  
					            		  String dealtype2 = null;
					            		  if(ttt.getPlanvol() == 1) {
					            			  dealtype2 = "p";
					            		  }
					            		 Cell c13 = row1.getCell(17);
					            		  
					            		  if(dealtype2 != null) {
							            		if (c13 == null) {
				            			        c13 = row1.createCell(17);
				            			        c13.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c13.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c13.setCellStyle(cellStyle);
				            			    }
					            			  c13.setCellValue(dealtype2);
					            			  addNode(listline, doc, "PouringDeal",  "true");
					            		  
					            		  }
					            		  
					            		  
					            		
					            		  
					            		  
					            	  int pp = 1;
					            		  if(custMPO.get(cuid) != null && Integer.parseInt(custMPO.get(cuid)+"") > 1) {
					            			  pp = Integer.parseInt(custMPO.get(cuid)+"");
					            			  c = row1.getCell(18);
						            		  if (c == null) {
					            			        c = row1.createCell(18);
					            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			    }
						            		  c.setCellValue(pp); 
						            		  addNode(listline, doc, "MPO",  pp +"");
						            	     } else {
					            		    	 c = row1.getCell(18);
							            		  if (c == null) {
						            			        c = row1.createCell(18);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    } 
					            		     }
					            		
					            		 
					            	
					            		  
					            		  c = row1.getCell(19);
					            		  if (c == null) {
				            			        c = row1.createCell(19);
				            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			    }
					            		  
					            		  if(arv ==-1 ) {
					            			  c.setCellValue(0);
					            		  }else {
					            			  c.setCellValue(1 * pp);
					            			  addNode(listline, doc, "Listings",  pp +"");
					            		  }
					            		
				            		loe++;
				            		}				            	 
					        	}
					         }
				         }
			             
			         
			             
			             CellStyle cellStyle = wb.createCellStyle();
			             

			             cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);

			             
			            
			                 Font titleFont = wb.createFont();
			                 titleFont.setFontName("Arial");
			                 titleFont.setFontHeightInPoints((short) 12);
			                // titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			            
			             row1 = worksheet.getRow(3); 
			             Cell cell = row1.createCell(15);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("SUM(P6:P30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(16);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(Q6:Q30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(17);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(R6:R30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(19);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("SUM(T6:T30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             
			 		     fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("listings/ListingCurrent-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					
				        
				        //salvestame xml-i 
				        
				        String fileName = "listings/xml/ListingCurrent-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance())+  ".xml";
					      System.out.println(fileName);
					      OutputFormat of = new OutputFormat();
					        of.setIndenting(true);
					        of.setLineSeparator(System.getProperty("line.separator"));
					        of.setEncoding("ISO-8859-1");
					        XMLSerializer ser = new XMLSerializer(of);
					        FileOutputStream fos = new FileOutputStream(fileName);
					        PrintWriter out = new PrintWriter(fos);
					        ser.setOutputCharStream(out);
					        ser.serialize(doc);
					        out.close();
				        

		}catch(Exception e) {
		   e.printStackTrace();	
		} 
     }
  
     
     private void addNode(Node root ,Document doc, String label, String value) {
    	
    	 if(value != null) {
    		 Node nr = doc.createElement(label);
 	        Text nrVal = doc.createTextNode( value.trim());
 	        nr.appendChild(nrVal);
 	        root.appendChild(nr); 
    	 }
    
	 }

     private void makeProspectExcelCurrent( TreeMap custNames,TreeMap custCities,TreeMap custMPO 
    		 , TreeMap bybrandsL ,Calendar algus , Calendar lopp , TreeMap prodNames , TreeMap  custRegions ,TreeMap prodSubBrands
    		 ,TreeMap bybrandsLTOT, TreeMap  custPostal , TreeMap custSegment , TreeMap custStatus) {
		  
		  	try{
		  		TreeMap manas = getExcelManas();
				String fname = this.getExcelDir()  + "currentprospectbase.xlsx";

				  FileInputStream fIPS= new FileInputStream(fname);
			        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
			        XSSFSheet worksheet = wb.getSheetAt(0);
			        XSSFRow row1 = worksheet.getRow(3); 
			        Cell c = row1.getCell(4);
			            c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
			            
			             int loe = 0;
			             Iterator itr = bybrandsL.keySet().iterator();
			           
			             while (itr.hasNext()) {
			            	 String brand = (String) itr.next();
			            	
			            	 TreeMap byseg = (TreeMap)bybrandsL.get(brand);
			            	 Iterator itra = byseg.keySet().iterator();
				             while (itra.hasNext()) {
				            	 String segm = (String) itra.next();
				            	
				            	 
				            	 TreeMap<String,ArrayList> custs = (TreeMap<String,ArrayList>)byseg.get(segm);
				            	
				            	 //System.out.println("test " + brand + " " + segm + " "+  custs.toString());
				            	 Iterator itrasr = custs.keySet().iterator();
					             while (itrasr.hasNext()) {
					            	// System.out.println("siin0 " + custs.keySet().size());
				            		String cuid = (String)itrasr.next();
				            	//	System.out.println("siin " + cuid+ " | " + custs.get(cuid));
				            		try {
				            			
				            		
				            		ArrayList coun = custs.get(cuid);
				            		String cuName = (String)custNames.get(cuid);
				            		String cuCity = (String)custCities.get(cuid);
				            		String postal = (String)custPostal.get(cuid);
				            		String segment = (String)custSegment.get(cuid);
				            		String status = (String)custStatus.get(cuid);
				            		
				            		ArrayList aa = (ArrayList)manas.get(cuid);
				            	    String mana = null;
				            				if(aa != null) {
				            					String a = (String)aa.get(0);
				            					Agent ag = (Agent)agent1.get(a);
				            					if(ag != null) {
				            						mana = a + ":" + ag.getName();
				            					}else {
				            						mana = a;
				            					}
				            					
				            					try {
				            						 a = (String)aa.get(1);
					            					 	Agent ag1 = (Agent)agent1.get(a);
						            					if(ag1 != null) {
						            						mana = mana + ","+ a + ":" + ag1.getName();
						            					}else {
						            						mana = mana + ","+ a;
						            					}
				            					}catch (Exception k) {
				            					}
				            					
				            				}
				            		
				            		
				            		
				            	
				            		for(int y = 0; y<coun.size();y++) {
				            			Toode ttt = (Toode)coun.get(y);
				            			String mmm = ttt.getBrand();
				            		int arv = 0;
				            		
				            		
				            		//Teeme rea siia 
				            		row1 = worksheet.getRow(5+loe); 
				            		//if(row1 != null) {
				            		if(row1 == null) {
				            			row1 = worksheet.createRow(5+loe);
				            		}
				            		
				            		c = row1.getCell(1);
			            			  if (c == null) {
			            			        c = row1.createCell(1);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String brr = brand.substring(0,brand.indexOf("|"));
			            			  c.setCellValue(brr);
			            			  
			            			  
			            			  c = row1.getCell(2);
			            			  if (c == null) {
			            			        c = row1.createCell(2);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String subb = "n/a subbrand";
			            			  try {
			            				  subb = brand.substring(brand.indexOf("|") + 1);
			            			  } catch (Exception e) {}
			            			  
			            			  c.setCellValue(subb);
			            			  
			            			  
			            			  c = row1.getCell(3);
			            			  if (c == null) {
			            			        c = row1.createCell(3);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			
			            			  String regb = "n/a region";
			            			  try {
			            				  regb  = segm.substring(0,segm.indexOf("|"));
			            			  } catch (Exception e) {}
			            			
			            			  c.setCellValue(regb);
				            		  
			            			  
			            			  
				            		
				            		  c = row1.getCell(4);
				            		  if (c == null) {
			            			        c = row1.createCell(4);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(status);
				            		  
				            		  c = row1.getCell(5);
				            		  if (c == null) {
			            			        c = row1.createCell(5);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segment);
				            		  
				            		  c = row1.getCell(6);
				            		  if (c == null) {
			            			        c = row1.createCell(6);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
				            		  
				            		  
				            		
				            		
				            		  
				            		  c = row1.getCell(7);
				            		  if (c == null) {
			            			        c = row1.createCell(7);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(ttt.getBrand() != null) {
				            			  c.setCellValue(ttt.getGr1() + " " + ttt.getBrand());	  
				            		  }
				            		  
				            		  c = row1.getCell(8);
				            		  if (c == null) {
			            			        c = row1.createCell(8);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(cuCity != null) {
				            			  c.setCellValue(cuCity);	  
				            		  }
				            		  
				            		  
				            		  c = row1.getCell(9);
				            		  if (c == null) {
			            			        c = row1.createCell(9);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(postal != null) {
				            			  c.setCellValue(postal);	  
				            		  }
				            		  
				            		  c = row1.getCell(10);
				            		  if (c == null) {
			            			        c = row1.createCell(10);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  String since = ttt.getLadu();
				            		  try {

					            		  if(since != null) {
					            			  Calendar cc = Calendar.getInstance();
					            			  cc.setTimeInMillis(Long.parseLong(since));
					            			  c.setCellValue(Converter.getDDpMMpYYYYpString(cc));	  
					            		  }  
				            		  }catch(Exception e) {
				            			  
				            		  }
				            		  
				            		
				            		  
				            		
				            		  c = row1.getCell(11);
			            			  if (c == null) {
			            			        c = row1.createCell(11);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			        CellStyle cellStyle = c.getCellStyle();
			            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
			            			        c.setCellStyle(cellStyle);
			            			    }
				            		  
				            			 c.setCellValue( mmm + ":" + ((Agent)agent1.get(mmm)).getName());
				            		
				            		
				            		if(mana != null) {
				            			c = row1.getCell(12);
				            			  if (c == null) {
				            			        c = row1.createCell(12);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
				            			  c.setCellValue(mana);
				            		}
				            			  
					            	  c = row1.getCell(13);
					            		  if (c == null) {
				            			        c = row1.createCell(13);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
					            		  c.setCellValue(cuName);
					            		  
					            		  //cocktail num
					            		  
					            		  int cnum = 0;
                                        String dealtype = null;
                                        String green = "";
                                        String yellow = "";
                                        String red = "";
					            		  if(ttt.getGr2() != null &&  ttt.getGr2().equals("GREEN")) {
					            			  green = "GREEN";
					            		  }
					            		  if(ttt.getGr2() != null &&  ttt.getGr2().equals("YELLOW")) {
					            			  yellow = "YELLOW";
					            		  }
					            		  if(ttt.getGr2() != null &&  ttt.getGr2().equals("RED")) {
					            			  red = "RED";
					            		  }
					            		
					            		  
					            	Cell c12 = row1.getCell(14);
					            	 if(green.length() > 0) {
					            		  if (c12 == null) {
				            			        c12 = row1.createCell(14);
				            			        c12.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			        CellStyle cellStyle = c12.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c12.setCellStyle(cellStyle);
				            			    }
					            			  c12.setCellValue(green);
					            		  }
					            		
					            		
					            		  
                                          
					            		  //cocktail status 
					            		  
					            		  Cell c14 = row1.getCell(15);
					            		  if(yellow.length() > 0) {
					            		  if (c14 == null) {
				            			        c14 = row1.createCell(15);
				            			        c14.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c14.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c14.setCellStyle(cellStyle);
				            			    }
					            			  c14.setCellValue(yellow);
					            		  }
					            		
					            		  
					            		
					            		 Cell c13 = row1.getCell(16);
					            		  
					            		  if(red.length() > 0) {
							            		if (c13 == null) {
				            			        c13 = row1.createCell(16);
				            			        c13.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c13.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c13.setCellStyle(cellStyle);
				            			    }
					            			  c13.setCellValue(red);
					            		  }
					            		  
					            		  
					            		
					            		  
					            		  
					            	  int pp = 1;
					            		  if(custMPO.get(cuid) != null && Integer.parseInt(custMPO.get(cuid)+"") > 1) {
					            			  pp = Integer.parseInt(custMPO.get(cuid)+"");
					            			  c = row1.getCell(17);
						            		  if (c == null) {
					            			        c = row1.createCell(17);
					            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
					            			    }
						            		  c.setCellValue(pp);  
						            	     } else {
					            		    	 c = row1.getCell(17);
							            		  if (c == null) {
						            			        c = row1.createCell(17);
						            			        c.setCellType(Cell.CELL_TYPE_STRING);
						            			    } 
					            		     }
					            		
					            		 
					            	
					            		  
					            		  c = row1.getCell(18);
					            		  if (c == null) {
				            			        c = row1.createCell(18);
				            			        c.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			    }
					            		  
					            		  if(arv ==-1 ) {
					            			  c.setCellValue(0);
					            		  }else {
					            			  c.setCellValue(1 * pp);
					            		  }
					            		  
					            		  c = row1.getCell(19);
					            		  if (c == null) {
				            			        c = row1.createCell(19);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			    }
					            		  
					            		  if(ttt.comment != null ) {
					            			  c.setCellValue(ttt.comment);
					            		  }
					            		
				            		loe++;
				            		}				            	 
					        	
					             }catch(Exception ee) {
				            			//ee.printStackTrace();
				            		}
				            		}
					         }
				         }
			             
			         
			             
			             CellStyle cellStyle = wb.createCellStyle();
			             

			             cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);

			             
			            
			                 Font titleFont = wb.createFont();
			                 titleFont.setFontName("Arial");
			                 titleFont.setFontHeightInPoints((short) 12);
			                // titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			            
			             row1 = worksheet.getRow(3); 
			             Cell cell = row1.createCell(14);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(O6:O30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(15);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(P6:P30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(16);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(Q6:Q30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             
			 		     fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("listings/ProspectCurrent-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					

		}catch(Exception e) {
		   e.printStackTrace();	
		} 
     }
  

     
     private void makePosExcelCurrent( TreeMap custNames,TreeMap custCities,TreeMap custMPO 
    		 , TreeMap bybrandsL ,Calendar algus , Calendar lopp , TreeMap prodNames , TreeMap  custRegions ,TreeMap prodSubBrands
    		 ,TreeMap bybrandsLTOT, TreeMap  custPostal , TreeMap custSegment , TreeMap custStatus) {
		  
		  	try{
		  		TreeMap manas = getExcelManas();
				String fname = this.getExcelDir()  + "currentposbase.xlsx";

				  FileInputStream fIPS= new FileInputStream(fname);
			        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
			        XSSFSheet worksheet = wb.getSheetAt(0);
			        XSSFRow row1 = worksheet.getRow(3); 
			        Cell c = row1.getCell(4);
			            c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
			            
			             int loe = 0;
			             Iterator itr = bybrandsL.keySet().iterator();
			           
			             while (itr.hasNext()) {
			            	 String brand = (String) itr.next();
			            	
			            	 TreeMap byseg = (TreeMap)bybrandsL.get(brand);
			            	 Iterator itra = byseg.keySet().iterator();
				             while (itra.hasNext()) {
				            	 String segm = (String) itra.next();
				            	
				            	 
				            	 TreeMap<String,ArrayList> custs = (TreeMap<String,ArrayList>)byseg.get(segm);
				            	
				            	 //System.out.println("test " + brand + " " + segm + " "+  custs.toString());
				            	 Iterator itrasr = custs.keySet().iterator();
					             while (itrasr.hasNext()) {
					            
					              String cuid = (String)itrasr.next();
				            	
				            		try {
				            		ArrayList coun = custs.get(cuid);
				            		String cuName = (String)custNames.get(cuid);
				            		String cuCity = (String)custCities.get(cuid);
				            		String postal = (String)custPostal.get(cuid);
				            		String segment = (String)custSegment.get(cuid);
				            		String status = (String)custStatus.get(cuid);
				            		ArrayList aa = (ArrayList)manas.get(cuid);
				            	      String mana = null;
				            				if(aa != null) {
				            					String a = (String)aa.get(0);
				            					Agent ag = (Agent)agent1.get(a);
				            					if(ag != null) {
				            						mana = a + ":" + ag.getName();
				            					}else {
				            						mana = a;
				            					}
				            					
				            					try {
				            						 a = (String)aa.get(1);
					            					 	Agent ag1 = (Agent)agent1.get(a);
						            					if(ag1 != null) {
						            						mana = mana + ","+ a + ":" + ag1.getName();
						            					}else {
						            						mana = mana + ","+ a;
						            					}
				            					}catch (Exception k) {
				            					}
				            				}
				            		
				            	
				            		for(int y = 0; y < coun.size(); y++) {
				            			Toode ttt = (Toode)coun.get(y);
				            			String mmm = ttt.getBrand();
				            		int arv = 0;
				            		
				            		
				            		//Teeme rea siia 
				            		row1 = worksheet.getRow(5+loe); 
				            		//if(row1 != null) {
				            		if(row1 == null) {
				            			row1 = worksheet.createRow(5+loe);
				            		}
				            		
				            		c = row1.getCell(1);
			            			  if (c == null) {
			            			        c = row1.createCell(1);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String brr = brand.substring(0,brand.indexOf("|"));
			            			  c.setCellValue(brr);
			            			  
			            			  
			            			  c = row1.getCell(2);
			            			  if (c == null) {
			            			        c = row1.createCell(2);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String subb = "n/a subbrand";
			            			  try {
			            				  subb = brand.substring(brand.indexOf("|") + 1);
			            			  } catch (Exception e) {}
			            			  
			            			  c.setCellValue(subb);
			            			  
			            			  
			            			  c = row1.getCell(3);
			            			  if (c == null) {
			            			        c = row1.createCell(3);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			
			            			  String regb = "n/a region";
			            			  try {
			            				  regb  = segm.substring(0,segm.indexOf("|"));
			            			  } catch (Exception e) {}
			            			
			            			  c.setCellValue(regb);
				            		  
			            			  
			            			  
				            		
				            		  c = row1.getCell(4);
				            		  if (c == null) {
			            			        c = row1.createCell(4);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(status);
				            		  
				            		  c = row1.getCell(5);
				            		  if (c == null) {
			            			        c = row1.createCell(5);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segment);
				            		  
				            		  c = row1.getCell(6);
				            		  if (c == null) {
			            			        c = row1.createCell(6);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
				            		  
				            		  
				            		
				            		
				            		  
				            		  c = row1.getCell(7);
				            		  if (c == null) {
			            			        c = row1.createCell(7);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(ttt.getBrand() != null) {
				            			  c.setCellValue(ttt.getGr1() + " " + ttt.getBrand());	  
				            		  }
				            		  
				            		  c = row1.getCell(8);
				            		  if (c == null) {
			            			        c = row1.createCell(8);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(cuCity != null) {
				            			  c.setCellValue(cuCity);	  
				            		  }
				            		  
				            		  
				            		  c = row1.getCell(9);
				            		  if (c == null) {
			            			        c = row1.createCell(9);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(postal != null) {
				            			  c.setCellValue(postal);	  
				            		  }
				            		  
				            		  c = row1.getCell(10);
				            		  if (c == null) {
			            			        c = row1.createCell(10);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  String since = ttt.getLadu();
				            		  try {

					            		  if(since != null) {
					            			  Calendar cc = Calendar.getInstance();
					            			  cc.setTimeInMillis(Long.parseLong(since));
					            			  c.setCellValue(Converter.getDDpMMpYYYYpString(cc));	  
					            		  }  
				            		  }catch(Exception e) {
				            			  
				            		  }
				            		  
				            		 
				            		  c = row1.getCell(11);
			            			  if (c == null) {
			            			        c = row1.createCell(11);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			        CellStyle cellStyle = c.getCellStyle();
			            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
			            			        c.setCellStyle(cellStyle);
			            			    }
				            		  
				            			 c.setCellValue( mmm + ":" + ((Agent)agent1.get(mmm)).getName());
				            		
				            		
				            		if(mana != null) {
				            			c = row1.getCell(12);
				            			  if (c == null) {
				            			        c = row1.createCell(12);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
				            			  c.setCellValue(mana);
				            		}
				            			  
					            	  c = row1.getCell(13);
					            		  if (c == null) {
				            			        c = row1.createCell(13);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
					            		  c.setCellValue(cuName);
					            		  
					            		
					            	Cell c12 = row1.getCell(14);
					            	 if(ttt.getGr2().length() > 0) {
					            		  if (c12 == null) {
				            			        c12 = row1.createCell(14);
				            			        c12.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			        CellStyle cellStyle = c12.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c12.setCellStyle(cellStyle);
				            			       }
					            			 c12.setCellValue(ttt.getGr2());
					            		  }
					            		  
					            	 c = row1.getCell(15);
				            		  if (c == null) {
			            			        c = row1.createCell(15);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  if(ttt.comment != null ) {
				            			  c.setCellValue(ttt.comment);
				            		  }
				            		loe++;
				            		}				            	 
					        	 }catch(Exception ee) {
				            			//ee.printStackTrace();
				            		}
				            		}
					         }
				         }
			             
			         
			             CellStyle cellStyle = wb.createCellStyle();
			             
			             cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
			            
			                 Font titleFont = wb.createFont();
			                 titleFont.setFontName("Arial");
			                 titleFont.setFontHeightInPoints((short) 12);
			            
			             row1 = worksheet.getRow(3); 
			             Cell cell = row1.createCell(14);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(O6:O30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             /*cell = row1.createCell(16);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(Q6:Q30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(17);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(R6:R30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(19);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("SUM(T6:T30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             */
			 		     fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("listings/PosCurrent-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					

		}catch(Exception e) {
		   e.printStackTrace();	
		} 
     }
  

     private void makeActivationExcelCurrent( TreeMap custNames,TreeMap custCities,TreeMap custMPO 
    		 , TreeMap bybrandsL ,Calendar algus , Calendar lopp , TreeMap prodNames , TreeMap  custRegions ,TreeMap prodSubBrands
    		 ,TreeMap bybrandsLTOT, TreeMap  custPostal , TreeMap custSegment , TreeMap custStatus) {
		  
		  	try{
		  		TreeMap manas = getExcelManas();
				String fname = this.getExcelDir()  + "currentactivationbase.xlsx";

				  FileInputStream fIPS= new FileInputStream(fname);
			        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
			        XSSFSheet worksheet = wb.getSheetAt(0);
			        XSSFRow row1 = worksheet.getRow(3); 
			        Cell c = row1.getCell(4);
			            c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
			            
			             int loe = 0;
			             Iterator itr = bybrandsL.keySet().iterator();
			           
			             while (itr.hasNext()) {
			            	 String brand = (String) itr.next();
			            	
			            	 TreeMap byseg = (TreeMap)bybrandsL.get(brand);
			            	 Iterator itra = byseg.keySet().iterator();
				             while (itra.hasNext()) {
				            	 String segm = (String) itra.next();
				            	
				            	 
				            	 TreeMap<String,ArrayList> custs = (TreeMap<String,ArrayList>)byseg.get(segm);
				            	
				            	 //System.out.println("test " + brand + " " + segm + " "+  custs.toString());
				            	 Iterator itrasr = custs.keySet().iterator();
					             while (itrasr.hasNext()) {
					            
					              String cuid = (String)itrasr.next();
				            	
				            		try {
				            		ArrayList coun = custs.get(cuid);
				            		String cuName = (String)custNames.get(cuid);
				            		String cuCity = (String)custCities.get(cuid);
				            		String postal = (String)custPostal.get(cuid);
				            		String segment = (String)custSegment.get(cuid);
				            		String status = (String)custStatus.get(cuid);
				            		ArrayList aa = (ArrayList)manas.get(cuid);
				            	      String mana = null;
				            				if(aa != null) {
				            					String a = (String)aa.get(0);
				            					Agent ag = (Agent)agent1.get(a);
				            					if(ag != null) {
				            						mana = a + ":" + ag.getName();
				            					}else {
				            						mana = a;
				            					}
				            					
				            					try {
				            						 a = (String)aa.get(1);
					            					 	Agent ag1 = (Agent)agent1.get(a);
						            					if(ag1 != null) {
						            						mana = mana + ","+ a + ":" + ag1.getName();
						            					}else {
						            						mana = mana + ","+ a;
						            					}
				            					}catch (Exception k) {
				            					}
				            				}
				            		
				            	
				            		for(int y = 0; y < coun.size(); y++) {
				            			Toode ttt = (Toode)coun.get(y);
				            			String mmm = ttt.getBrand();
				            		int arv = 0;
				            		
				            		
				            		//Teeme rea siia 
				            		row1 = worksheet.getRow(5+loe); 
				            		//if(row1 != null) {
				            		if(row1 == null) {
				            			row1 = worksheet.createRow(5+loe);
				            		}
				            		
				            		c = row1.getCell(1);
			            			  if (c == null) {
			            			        c = row1.createCell(1);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String brr = brand.substring(0,brand.indexOf("|"));
			            			  c.setCellValue(brr);
			            			  
			            			  
			            			  c = row1.getCell(2);
			            			  if (c == null) {
			            			        c = row1.createCell(2);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String subb = "n/a subbrand";
			            			  try {
			            				  subb = brand.substring(brand.indexOf("|") + 1);
			            			  } catch (Exception e) {}
			            			  
			            			  c.setCellValue(subb);
			            			  
			            			  
			            			  c = row1.getCell(3);
			            			  if (c == null) {
			            			        c = row1.createCell(3);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			
			            			  String regb = "n/a region";
			            			  try {
			            				  regb  = segm.substring(0,segm.indexOf("|"));
			            			  } catch (Exception e) {}
			            			
			            			  c.setCellValue(regb);
				            		  
			            			  
			            			  
				            		
				            		  c = row1.getCell(4);
				            		  if (c == null) {
			            			        c = row1.createCell(4);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(status);
				            		  
				            		  c = row1.getCell(5);
				            		  if (c == null) {
			            			        c = row1.createCell(5);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segment);
				            		  
				            		  c = row1.getCell(6);
				            		  if (c == null) {
			            			        c = row1.createCell(6);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
				            		  
				            		  
				            		
				            		
				            		  
				            		  c = row1.getCell(7);
				            		  if (c == null) {
			            			        c = row1.createCell(7);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(ttt.getBrand() != null) {
				            			  c.setCellValue(ttt.getGr1() + " " + ttt.getBrand());	  
				            		  }
				            		  
				            		  c = row1.getCell(8);
				            		  if (c == null) {
			            			        c = row1.createCell(8);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(cuCity != null) {
				            			  c.setCellValue(cuCity);	  
				            		  }
				            		  
				            		  
				            		  c = row1.getCell(9);
				            		  if (c == null) {
			            			        c = row1.createCell(9);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(postal != null) {
				            			  c.setCellValue(postal);	  
				            		  }
				            		  
				            		  c = row1.getCell(10);
				            		  if (c == null) {
			            			        c = row1.createCell(10);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  String since = ttt.getLadu();
				            		  try {

					            		  if(since != null) {
					            			  Calendar cc = Calendar.getInstance();
					            			  cc.setTimeInMillis(Long.parseLong(since));
					            			  c.setCellValue(Converter.getDDpMMpYYYYpString(cc));	  
					            		  }  
				            		  }catch(Exception e) {
				            			  
				            		  }
				            		  
				            		 
				            		  c = row1.getCell(11);
			            			  if (c == null) {
			            			        c = row1.createCell(11);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			        CellStyle cellStyle = c.getCellStyle();
			            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
			            			        c.setCellStyle(cellStyle);
			            			    }
				            		  
				            			 c.setCellValue( mmm + ":" + ((Agent)agent1.get(mmm)).getName());
				            		
				            		
				            		if(mana != null) {
				            			c = row1.getCell(12);
				            			  if (c == null) {
				            			        c = row1.createCell(12);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
				            			  c.setCellValue(mana);
				            		}
				            			  
					            	  c = row1.getCell(13);
					            		  if (c == null) {
				            			        c = row1.createCell(13);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
					            		  c.setCellValue(cuName);
					            		  
					            		
					            	Cell c12 = row1.getCell(14);
					            	 if(ttt.getGr2().length() > 0) {
					            		  if (c12 == null) {
				            			        c12 = row1.createCell(14);
				            			        c12.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			        CellStyle cellStyle = c12.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c12.setCellStyle(cellStyle);
				            			       }
					            			 c12.setCellValue(ttt.getGr2());
					            		  }
					            	 c = row1.getCell(15);
				            		  if (c == null) {
			            			        c = row1.createCell(15);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  if(ttt.comment != null ) {
				            			  c.setCellValue(ttt.comment);
				            		  }
					            		
				            		loe++;
				            		}				            	 
					        	 }catch(Exception ee) {
				            			//ee.printStackTrace();
				            		}
				            		}
					         }
				         }
			             
			         
			             CellStyle cellStyle = wb.createCellStyle();
			             
			             cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
			            
			                 Font titleFont = wb.createFont();
			                 titleFont.setFontName("Arial");
			                 titleFont.setFontHeightInPoints((short) 12);
			            
			             row1 = worksheet.getRow(3); 
			             Cell cell = row1.createCell(14);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(O6:O30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			            
			 		     fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("listings/ActivationCurrent-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					

		}catch(Exception e) {
		   e.printStackTrace();	
		} 
     }
  

     
     private void makeEducationExcelCurrent( TreeMap custNames,TreeMap custCities,TreeMap custMPO 
    		 , TreeMap bybrandsL ,Calendar algus , Calendar lopp , TreeMap prodNames , TreeMap  custRegions ,TreeMap prodSubBrands
    		 ,TreeMap bybrandsLTOT, TreeMap  custPostal , TreeMap custSegment , TreeMap custStatus) {
		  
		  	try{
		  		TreeMap manas = getExcelManas();
				String fname = this.getExcelDir()  + "currenteducationbase.xlsx";

				  FileInputStream fIPS= new FileInputStream(fname);
			        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
			        XSSFSheet worksheet = wb.getSheetAt(0);
			        XSSFRow row1 = worksheet.getRow(3); 
			        Cell c = row1.getCell(4);
			            c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
			            
			             int loe = 0;
			             Iterator itr = bybrandsL.keySet().iterator();
			           
			             while (itr.hasNext()) {
			            	 String brand = (String) itr.next();
			            	
			            	 TreeMap byseg = (TreeMap)bybrandsL.get(brand);
			            	 Iterator itra = byseg.keySet().iterator();
				             while (itra.hasNext()) {
				            	 String segm = (String) itra.next();
				            	
				            	 
				            	 TreeMap<String,ArrayList> custs = (TreeMap<String,ArrayList>)byseg.get(segm);
				            	
				            	 //System.out.println("test " + brand + " " + segm + " "+  custs.toString());
				            	 Iterator itrasr = custs.keySet().iterator();
					             while (itrasr.hasNext()) {
					            
					              String cuid = (String)itrasr.next();
				            	
				            		try {
				            		ArrayList coun = custs.get(cuid);
				            		String cuName = (String)custNames.get(cuid);
				            		String cuCity = (String)custCities.get(cuid);
				            		String postal = (String)custPostal.get(cuid);
				            		String segment = (String)custSegment.get(cuid);
				            		String status = (String)custStatus.get(cuid);
				            		ArrayList aa = (ArrayList)manas.get(cuid);
				            	      String mana = null;
				            				if(aa != null) {
				            					String a = (String)aa.get(0);
				            					Agent ag = (Agent)agent1.get(a);
				            					if(ag != null) {
				            						mana = a + ":" + ag.getName();
				            					}else {
				            						mana = a;
				            					}
				            					
				            					try {
				            						 a = (String)aa.get(1);
					            					 	Agent ag1 = (Agent)agent1.get(a);
						            					if(ag1 != null) {
						            						mana = mana + ","+ a + ":" + ag1.getName();
						            					}else {
						            						mana = mana + ","+ a;
						            					}
				            					}catch (Exception k) {
				            					}
				            				}
				            		
				            	
				            		for(int y = 0; y < coun.size(); y++) {
				            			Toode ttt = (Toode)coun.get(y);
				            			String mmm = ttt.getBrand();
				            		int arv = 0;
				            		
				            		
				            		//Teeme rea siia 
				            		row1 = worksheet.getRow(5+loe); 
				            		//if(row1 != null) {
				            		if(row1 == null) {
				            			row1 = worksheet.createRow(5+loe);
				            		}
				            		
				            		c = row1.getCell(1);
			            			  if (c == null) {
			            			        c = row1.createCell(1);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String brr = brand.substring(0,brand.indexOf("|"));
			            			  c.setCellValue(brr);
			            			  
			            			  
			            			  c = row1.getCell(2);
			            			  if (c == null) {
			            			        c = row1.createCell(2);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			  String subb = "n/a subbrand";
			            			  try {
			            				  subb = brand.substring(brand.indexOf("|") + 1);
			            			  } catch (Exception e) {}
			            			  
			            			  c.setCellValue(subb);
			            			  
			            			  
			            			  c = row1.getCell(3);
			            			  if (c == null) {
			            			        c = row1.createCell(3);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
			            			
			            			  String regb = "n/a region";
			            			  try {
			            				  regb  = segm.substring(0,segm.indexOf("|"));
			            			  } catch (Exception e) {}
			            			
			            			  c.setCellValue(regb);
				            		  
			            			  
			            			  
				            		
				            		  c = row1.getCell(4);
				            		  if (c == null) {
			            			        c = row1.createCell(4);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(status);
				            		  
				            		  c = row1.getCell(5);
				            		  if (c == null) {
			            			        c = row1.createCell(5);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segment);
				            		  
				            		  c = row1.getCell(6);
				            		  if (c == null) {
			            			        c = row1.createCell(6);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  c.setCellValue(segm.substring(segm.indexOf("|")+1));
				            		  
				            		  
				            		
				            		
				            		  
				            		  c = row1.getCell(7);
				            		  if (c == null) {
			            			        c = row1.createCell(7);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(ttt.getBrand() != null) {
				            			  c.setCellValue(ttt.getGr1() + " " + ttt.getBrand());	  
				            		  }
				            		  
				            		  c = row1.getCell(8);
				            		  if (c == null) {
			            			        c = row1.createCell(8);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(cuCity != null) {
				            			  c.setCellValue(cuCity);	  
				            		  }
				            		  
				            		  
				            		  c = row1.getCell(9);
				            		  if (c == null) {
			            			        c = row1.createCell(9);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  if(postal != null) {
				            			  c.setCellValue(postal);	  
				            		  }
				            		  
				            		  c = row1.getCell(10);
				            		  if (c == null) {
			            			        c = row1.createCell(10);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  String since = ttt.getLadu();
				            		  try {

					            		  if(since != null) {
					            			  Calendar cc = Calendar.getInstance();
					            			  cc.setTimeInMillis(Long.parseLong(since));
					            			  c.setCellValue(Converter.getDDpMMpYYYYpString(cc));	  
					            		  }  
				            		  }catch(Exception e) {
				            			  
				            		  }
				            		  
				            		  
				            		  c = row1.getCell(11);
			            			  if (c == null) {
			            			        c = row1.createCell(11);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			        CellStyle cellStyle = c.getCellStyle();
			            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
			            			        c.setCellStyle(cellStyle);
			            			    }
				            		  
				            			 c.setCellValue( mmm + ":" + ((Agent)agent1.get(mmm)).getName());
				            		
				            		
				            		if(mana != null) {
				            			c = row1.getCell(12);
				            			  if (c == null) {
				            			        c = row1.createCell(12);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
				            			  c.setCellValue(mana);
				            		}
				            			  
					            	  c = row1.getCell(13);
					            		  if (c == null) {
				            			        c = row1.createCell(13);
				            			        c.setCellType(Cell.CELL_TYPE_STRING);
				            			        CellStyle cellStyle = c.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
				            			        c.setCellStyle(cellStyle);
				            			    }
					            		  c.setCellValue(cuName);
					            		  
					            		
					            	Cell c12 = row1.getCell(14);
					            	 if(ttt.getGr2().length() > 0) {
					            		  if (c12 == null) {
				            			        c12 = row1.createCell(14);
				            			        c12.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			        CellStyle cellStyle = c12.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c12.setCellStyle(cellStyle);
				            			       }
					            			 c12.setCellValue(ttt.getGr2());
					            		  }
					            	 
					            	 Cell c13 = row1.getCell(15);
					            	 if(ttt.getKastis() > 0) {
					            		  if (c13 == null) {
				            			        c13 = row1.createCell(15);
				            			        c13.setCellType(Cell.CELL_TYPE_NUMERIC);
				            			        CellStyle cellStyle = c13.getCellStyle();
				            			        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				            			        c13.setCellStyle(cellStyle);
				            			       }
					            			 c13.setCellValue(ttt.getKastis());
					            		  }
					            	 
					            	 c = row1.getCell(16);
				            		  if (c == null) {
			            			        c = row1.createCell(16);
			            			        c.setCellType(Cell.CELL_TYPE_STRING);
			            			    }
				            		  
				            		  if(ttt.comment != null ) {
				            			  c.setCellValue(ttt.comment);
				            		  }
					            		  
					            		
				            		loe++;
				            		}				            	 
					        	 }catch(Exception ee) {
				            			//ee.printStackTrace();
				            		}
				            		}
					         }
				         }
			             
			         
			             CellStyle cellStyle = wb.createCellStyle();
			             
			             cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
			             cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
			            
			                 Font titleFont = wb.createFont();
			                 titleFont.setFontName("Arial");
			                 titleFont.setFontHeightInPoints((short) 12);
			            
			             row1 = worksheet.getRow(3); 
			             Cell cell = row1.createCell(14);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(O6:O30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             /*cell = row1.createCell(16);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(Q6:Q30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(17);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("COUNTA(R6:R30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             cell = row1.createCell(19);
			             cell.setCellStyle(cellStyle);
			             cell.getCellStyle().setFont(titleFont);
			             cell.setCellFormula("SUM(T6:T30000)");
			             cell.setCellType(Cell.CELL_TYPE_FORMULA);
			             */
			 		     fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("listings/EducationCurrent-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) + ".xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					

		}catch(Exception e) {
		   e.printStackTrace();	
		} 
     }
  

     
  
	   public Vector getFixedAssortmentData() {
			Vector result = new Vector();
	
			return result;
	}
	 

	   
	   

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          
         String sql = " SELECT PRICEDISCTABLE.ITEMRELATION as prodid , PRICEDISCTABLE.ACCOUNTRELATION as custid, max(PRICEDISCTABLE.AMOUNT) AS price " +  
    "   FROM PRICEDISCTABLE WHERE PRICEDISCTABLE.DATAAREAID='ct' AND PRICEDISCTABLE.RELATION=4 AND PRICEDISCTABLE.ACCOUNTCODE=0 AND  " +
	" PRICEDISCTABLE.itemcode=0  and PRICEDISCTABLE.AMOUNT > 0.01 and  " +
    "   ((GETDATE() between PRICEDISCTABLE.FROMDATE-(case when DATEPART(DW, PRICEDISCTABLE.FROMDATE) = 2 then 3 else 1 end)"
    + " AND PRICEDISCTABLE.TODATE -(case when DATEPART(DW, PRICEDISCTABLE.TODATE) = 1 then 2 else 0 end)) " +
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
	"   WHERE PRI.DATAAREAID='ct' AND PRI.RELATION in (4) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and " + 
    "     ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) = 1 then 2 else 0 end)) " +
	"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) "+
	"   or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION   like '%DPD%' " +
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
	 "  WHERE PRI.DATAAREAID='ct' AND PRI.RELATION in (4,5) AND PRI.ACCOUNTCODE=1 AND pri.itemcode=0  and  " +
     "    ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) = 1 then 2 else 0 end)) " +
	 "  or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	 "  or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	 "  or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION not like 'DPD%'" +
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
                                         mline.setCliGroup(clGroup.trim());
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
        
                         
                         
                          sql4 = "SELECT  PRI.groupid as grupp,  " +
                     			"(case when itemcode = 0 then  PRI.ITEMRELATION else '' end) as toode , " +
                     			"(case when itemcode = 5 then  PRI.ITEMRELATION else '' end) as alcgroup , " +
                     			"(case when itemcode = 3 then  PRI.ITEMRELATION else '' end) as prgroup , " +
                     			"pri.ENDPRICE as hind ,pri.PERCENT_ as perc0 , pri.RETROPERCENT as perc ,PRI.FROMDATE as algus " +
                                  " ,PRI.TODATE as lopp,datediff(week ,PRI.FROMDATE ,PRI.TODATE)  as kamp ,PRI.RELATION ,priority " +
                          " FROM ASTPRICEDISCTABLEAGREEMENT pri WHERE PRI.DATAAREAID='ct' AND   " +
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
                                                      if(pri!= null){
                                                      if(pri.length() == 1){
                                                     	 pri = "000"+ pri;
                                                      }
                                                      if(pri.length() == 2){
                                                     	 pri = "00"+ pri;
                                                      }
                                                      if(pri.length() == 3){
                                                     	 pri = "0"+ pri;
                                                      }                               
                                                      }
                                                      
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
                                                      

                                                      mline.setProdLevel(pri); 
                                                      
                                                    if(goes){
                                                      if(price > 0){
                                                          mline.setPrice(price);
                                                          lines.add(mline);       	 
                                                      }else if (perc0 >0){
                                                     	 mline.setDiscount(perc0);
                                                          lines.add(mline);   
                                                      }/*else if (perc >0){
                                                     	 mline.setDiscount(perc);
                                                          lines.add(mline);   
                                                      }*/                               
                                                    }
                                              }
                                              
                                          //    System.out.println("lines1 " + lines.size());
                                              

                         
                         
                         
                         
                         
                         
                         
                         
                         
                         
                         
                       sql4 = "SELECT PRI.ACCOUNTRELATION as grupp,   PRI.ITEMRELATION as toode ,pri.AMOUNT as hind ,PRI.FROMDATE as algus " +
                         		" ,PRI.TODATE as lopp,month(PRI.TODATE -PRI.FROMDATE)  as kamp " +
             " FROM PRICEDISCTABLE pri INNER JOIN INVENTTABLE inv on inv.dataareaid=pri.dataareaid and inv.itemid =pri.itemrelation " +
       	  " WHERE PRI.DATAAREAID='ct' AND PRI.RELATION=4 AND PRI.ACCOUNTCODE=0 AND pri.itemcode=0  and " +
           "   ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) = 1 then 2 else 0 end)) "+
	  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900) " +
	  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	  " or( GETDATE()<=PRI.TODATE+1) )  and pri.AMOUNT > 0.01 and PRI.ACCOUNTRELATION not like 'DPD%'" +
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
                                                mline.setCliGroup(custID.trim());
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
 " where l.dataareaid = 'ct' and ((GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) = 1 then 2 else 0 end)) " +
	"   or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
	"   or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	"   or( GETDATE()<=PRI.TODATE+1) ) and accepted = 2  and fromqty = 0 and promostatus in (1,4) and maxdiscountpromo = 0 " ;
                                         
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
                                                         mline.setCliGroup(custID.trim());
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
/*
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
*/
		return null;
	}


  
  private TreeMap kogused = null;
  TreeMap<String , Integer> posseisud = new TreeMap<String , Integer>();
 public String getSaveStockInfo(){
	  
   kogused = new TreeMap();
	  

//For IMPORT (including RED BULL) products - only SPIL ('GPP-VT','GPP-AK-LB') warehouses must be taken;
//For LB products - SPIL's warehouse 'GPP-VT' and warehouse from LB AX file - Lb_l - 'GPP'.


   String  cmd = "select   summ.itemid  AS toode ,sum(AVAILPHYSICAL)/case when max(inv.taxpackagingqty) > 0\r\n" + 
   		"   		 then max(inv.taxpackagingqty)  else 1 end  AS stock ,dim.inventlocationid as ladu ,"
   		+ " case when dim.configid ='' then 'ALL' else dim.configid end as config  FROM INVENTSUM summ  with(nolock) \r\n" + 
   		"   		   INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid \r\n" + 
   		"   		   inner join  inventtable inv on summ.itemid = inv.itemid and inv.dataareaid=summ.dataareaid \r\n" + 
   		"   		 WHERE  summ.DATAAREAID='ct' and CLOSED = 0  and dim.inventlocationid in ('TTUB','TTDP','DAWDP')\r\n" + 
   		"		 GROUP BY  summ.itemid  ,dim.inventlocationid ,dim.configid having sum(availphysical) >0" ;

   posseisud = new TreeMap<String , Integer>();
       if (isDebug()) System.out.println(cmd);
       StringBuffer products = new StringBuffer();
       try {
    	   Connection con = this.getNewConnection();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery(cmd);
      TreeMap<String,Double> seisud = new TreeMap<String,Double> ();
      
      
         while (rs.next()) {
           String kood = rs.getString("toode").trim();
           double seis = rs.getDouble("stock");
           String dealer = rs.getString("ladu").trim();
           String config = rs.getString("config").trim();
           
           if (dealer.equals("DAWDP")) {
        	 //  config = "ALL"; 
        	   if(posseisud.get(kood) != null) {
        		   posseisud.put(kood,  posseisud.get(kood) + (int)seis);
        	   }else {
        		   posseisud.put(kood, (int)seis); 
        	   }
        	 
           }
           
           String aa = kood + "|" + config;
           if(seis > 0){
        	//   •	NDS configuration, we will show stock balances in TTUB and TTDP warehouse for only with configuration NDS and/or blank
        	 //  •	For others we will show stock balances in TTUB and TTDP warehouse for items with configuration DS or blank
             if(config.equals("NDS") || config.equals("ALL")) {
            	 if(seisud.get(kood + "|NDS") != null) {
            		 seisud.put(kood + "|NDS", seisud.get(kood + "|NDS")+ seis);
            	 }else {
            		 seisud.put(kood + "|NDS", seis);
            	 }
             }
        	   
             if(config.equals("DS") || config.equals("ALL")) {
            	 if(seisud.get(kood + "|DS") != null) {
            		 seisud.put(kood + "|DS", seisud.get(kood + "|DS")+ seis);
            	 }else {
            		 seisud.put(kood + "|DS", seis);
            	 }
             }
            
            }
           }

         
           rs.close();
           st.close();
           con.close();
           
           Iterator itera =  seisud.keySet().iterator();
          while(itera.hasNext()) {
        	  String kood = itera.next()+"";
        	  products.append(kood.substring(0,kood.indexOf("|")) +  ";");
              products.append(" ;");
              products.append(getParent().getNF().format(seisud.get(kood)) + ";" + kood.substring(kood.indexOf("|")+1) + "\n"); // + dealer +  "\n");
        kogused.put(kood, seisud.get(kood) + "");  
          }

		  
           
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
	  DealerProduct dealerProds = new DealerProduct();


	   String  cmd = "select   summ.itemid  AS toode ,sum(AVAILPHYSICAL)/case when max(inv.taxpackagingqty) > 0\r\n" + 
	   		"   		 then max(inv.taxpackagingqty)  else 1 end  AS stock ,dim.inventlocationid as ladu ,"
	   		+ " case when dim.configid ='' then 'ALL' else dim.configid end as config  FROM INVENTSUM summ  with(nolock) \r\n" + 
	   		"   		   INNER JOIN INVENTDIM dim with(nolock) ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid \r\n" + 
	   		"   		   inner join  inventtable inv on summ.itemid = inv.itemid and inv.dataareaid=summ.dataareaid \r\n" + 
	   		"   		 WHERE  summ.DATAAREAID='ct' and CLOSED = 0  and dim.inventlocationid in ('TTUB','TTDP','DAWDP')\r\n" + 
	   		"		 GROUP BY  summ.itemid  ,dim.inventlocationid ,dim.configid having sum(availphysical) >0" ;


	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
	    	   Connection con = this.getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);
	      TreeMap<String,Double> seisud = new TreeMap<String,Double> ();
	      
	      
	         while (rs.next()) {
	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	           String dealer = rs.getString("ladu").trim();
	           
	           String config = rs.getString("config").trim();
	           if (dealer.equals("DAWDP") ) {
	        	  // config = "ALL";   
	           }
	           
	           String aa = kood + "|" + config;
	           if(seis > 0){
	        	//   •	NDS configuration, we will show stock balances in TTUB and TTDP warehouse for only with configuration NDS and/or blank
	        	 //  •	For others we will show stock balances in TTUB and TTDP warehouse for items with configuration DS or blank
	             if(config.equals("NDS") || config.equals("ALL")) {
	            	 if(seisud.get(kood + "|NDS") != null) {
	            		 seisud.put(kood + "|NDS", seisud.get(kood + "|NDS")+ seis);
	            	 }else {
	            		 seisud.put(kood + "|NDS", seis);
	            	 }
	             }
	        	   
	             if(config.equals("DS") || config.equals("ALL")) {
	            	 if(seisud.get(kood + "|DS") != null) {
	            		 seisud.put(kood + "|DS", seisud.get(kood + "|DS")+ seis);
	            	 }else {
	            		 seisud.put(kood + "|DS", seis);
	            	 }
	             }
	            
	            }
	           }

	         
	           rs.close();
	           st.close();
	           con.close();
	           
	           Iterator itera =  seisud.keySet().iterator();
	          while(itera.hasNext()) {
	        	  String kood = itera.next()+"";
	        	  dealerProds.add(kood.substring(kood.indexOf("|")+1),
	        			  kood.substring(0,kood.indexOf("|")),(seisud.get(kood))+""," "," " );

	          }

			  
	           
	       } catch (Exception e) {
	           getParent().showError(e);
	       }
	       


      
      //  System.out.println( " dealerProds suurus " + dealer + " " + dealerProds.toString());
       // System.exit(0);
       return dealerProds;
     
     }

  
  
  
  
  private TreeMap agentDim = new TreeMap();
  
  private TreeMap agent1 = new TreeMap();
  

 
  
  public TreeMap getAgents(){
	 
	  agent1 = new TreeMap();
	  
	  
	  
    String cmd = " SELECT  a.ACCOUNTID as kood , d.NAME FROM  EAgentTable a inner join  EMPLTABLE  e  on e.EMPLID = a.EMPLID and e.DATAAREAID = a.DATAAREAID INNER JOIN " +
                       "  DIRPARTYTABLE AS d ON d.DATAAREAID = 'v02' AND e.PARTYID = d.PARTYID " +
               " WHERE   (e.DATAAREAID = 'ct') ";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
   
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
          Agent agent = new Agent(Converter.cleanLitLatEst(rs.getString("kood").trim()));       
          String name = rs.getString("name").trim();
          agent.setName(name);
          name = name + " " + agent.getID();
          agent.setIsPilot(true);
          agent.addProdGroupList("ALL");
          if(agent.getID().startsWith("H")){
        	  agent.addProdGroupList("HORECA");
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
    Agent agent = new Agent("WARE");
    agent.setName("WARE");
    agents.put("WARE", agent);
    agent1.put(agent.getID(), agent);
    
    
   // setBulkSales(agent1);
    return agents;
  }

  
  public static float Round(float Rval, int Rpl) {
	   float p = (float)Math.pow(10,Rpl);
	   Rval = Rval * p;
	   float tmp = Math.round(Rval);
	   return (float)tmp/p;
	   }
  
  
  Vector nodebt = null;
  
  
  private void sendAssets (HashSet custs){
	  try{
		
	  eevaeksport.util.ReportingService servu = new ReportingService();
		servu.user =  user;
		servu.pass = pass;
		servu.company = rcompany;   
		JSONArray assets = servu.getAssets(null);
		servu.closeClient();
System.out.println("Assets serveris " + assets.length());
		
		HashMap asscusts = new HashMap();
	     HashSet cuus = new HashSet();	
		for (int i = 0; i < assets.length(); i++) {
			 JSONObject cob = assets.getJSONObject(i);
				 //   String id = cob.get("id") + "";
				    String code = (String)cob.get("code");
					//String name = (String)cob.get("name");
					
					try{

						JSONObject cust  = (JSONObject)cob.get("customer");
						 if(cust != null){
							 String kkood = (String)cust.get("code");
							// String kname = (String)cust.get("name");
							asscusts.put(code, kkood);
							cuus.add(code);
						 }	
					}catch(Exception e){
						
					}
		 }
	
  	  
  	  
  	  System.out.println("Asses size enne  " + asscusts.size());
  	  Vector vec = new Vector(); 
  	  String cmd3 = "select a.ASSETID as code ,a.name  ,' (' + l.name + ')' as name1, '' as ean ,'' as nfcTag, a.serialnum as factoryID ,'' as description , "+
  	  		" '' as born , '' as contract ,a.LOCATION as custcode " +
		    " FROM  dbo.ASSETTABLE a LEFT JOIN assetlocation l on l.location=a.LOCATION  " +
			" WHERE (a.DATAAREAID = 'ct') and assetgroup = 'IZN'"; 
		  	Connection con = getNewConnection();
		    Statement stm = con.createStatement();
            ResultSet rs1 = stm.executeQuery(cmd3);
            HashSet asses = new HashSet();
             int counter = 0;
               while(rs1.next()){
              	
              	 JSONObject obj = new JSONObject();
              	 
                      String code = rs1.getString("code").trim();
                      obj.put("code", code);
                      asses.add(code);
                      try{
                      	
                      String name = rs1.getString("name");
                      if(name == null){
                      	name = code;
                      }else{
                      	name = name.trim();
                      }
                      
                      String name1 = rs1.getString("name1");
                      if(name1 != null){
                      	name = name + " " + name1.trim();
                      }
                      
                      
                      obj.put("name", name);
                      
                      String ean = rs1.getString("ean");
                      if(ean != null && ean.trim().length() > 0){
                      	   obj.put("ean", ean);
                      }
                   
                      String nfcTag = rs1.getString("nfcTag");
                      if(nfcTag != null && nfcTag.trim().length() > 0){
                   	   obj.put("nfcTag", nfcTag);
                        }
                      
                      String factoryId = rs1.getString("factoryId");
                      if(factoryId != null && factoryId.trim().length() > 0){
                    	   obj.put("factoryId", factoryId);
                        }
                      
                      String description = rs1.getString("description");
                      if(description != null && description.trim().length() > 0){
                    	   obj.put("description", description);
                    }
                    /*  java.sql.Date born = rs1.getDate("born");
                      if(born != null ){
                     	   obj.put("born", Converter.jsonDate(born));
                     }
                      
                      java.sql.Date contract = rs1.getDate("contract");
                      if(born != null ){
                     	   obj.put("contract", Converter.jsonDate(contract));
                     }*/
                  
                      boolean goes = false;
                      if(asses.contains(code)){
                      	goes = true;
                      }
                      
                      String ccode = rs1.getString("custcode");
                      if(ccode != null && ccode.trim().length() > 0){
                      	if(ccode.equals("1005")){
                      		 obj.put("active", false);
                      	}else{
                      		 obj.put("active", true);
                      	}
                      		goes = true;
                      		asses.add(code);
                      }
                   
                      if(goes && !cuus.contains(code)){
                      	 vec.add(obj);
                      	counter ++;
                      }
                         
                        
                      
                      }catch (Exception r){
                       r.printStackTrace();    
                      }
                    }
                   rs1.close();
  	  
                   System.out.println("Counter " + counter + " Asses " + asses.size());
                   
                   Converter.sendAssetsJson(vec,this.user,this.pass,this.rcompany);     
  	   
              		
           		
           		
  	   //siin saadame seosed ära jujul kui customer on olemas selline 
   	   vec = new Vector(); 
   	 cmd3 = "select a.ASSETID as itemcode , '' as contract ,LOCATION as custcode " +
		    " FROM  dbo.ASSETTABLE a WHERE (a.DATAAREAID = 'ct') and assetgroup = 'IZN'"; 
            int cou = 0;
            int coua = 0;
   	  rs1 = stm.executeQuery(cmd3);
                while(rs1.next()){
               	
               	 JSONObject obj = new JSONObject();
               	 coua++;
                       String code = rs1.getString("itemcode").trim();
                       obj.put("inventoryItemCode", code);
                   //    Date contract = rs1.getDate("contract");
                     //  obj.put("contractDate",contract.getTime() );
                       String custcode = rs1.getString("custcode").trim();
                       obj.put("customerCode", custcode);
                       
                       if(code.indexOf("1104090041") != -1){
                      //	 System.out.println("1104090041 " + custcode +" "+ (asses.contains(code)) + " " +custs.contains(custcode));
                       }
                     
                       if(asses.contains(code) && custs.contains(custcode) ){
                      	  String olemas = (String)asscusts.get(code);
                      	  if(olemas == null){
                                vec.add(obj);  
                                cou++;  
                       	  }else if(olemas != null){
                      		 // if(!olemas.equals(custcode)){
                      			   vec.add(obj);  
                                     cou++; 
                      		  //}
                      	  }
                        }
                     }
                
                    rs1.close();
                    stm.close();
  	   System.out.println("assets cou " +  cou + " "+ coua);
                    
                    // siis peaks tegema vahele kontrolli et kui on muudatus siis ainult laksad ylesse
  	   
  	   
  	   
                  Converter.sendAssetsCustomerJson(vec,this.user,this.pass,this.rcompany);     
                  
	  }catch(Exception e){
		  e.printStackTrace();
	  }     	   

  }
  
  
  private TreeMap getExcelCategories() {
	  TreeMap res = new TreeMap();
	  
	  File f = new File(this.getExcelDir() + "/CustomersWeb.xlsx");
		
		try {
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;


			for (int i = 1 ; i < sheet.getLastRowNum()+1 ; i++) {
			
				row = sheet.getRow(i);
				String cuID = "";
				String ags = "";

				if (row != null && row.getCell((short) 0) != null) {
									try {
						cuID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						cuID = (row.getCell((short) 0).getStringCellValue());
					}
				}	
				
				if (row != null && row.getCell((short) 19) != null) {
					try {
					ags = (row.getCell((short) 19).getStringCellValue());
					 res.put(cuID, ags);
					} catch (Exception e) {
						
					}
				}
				
			//	System.out.println(cuID + " " + ags);
				
				
		            
				
				
				}
		}catch(Exception e) {
			e.printStackTrace();
		}

	  return res;
  }
  
  private TreeMap getExcelSegments() {
	  TreeMap res = new TreeMap();
	  
	  File f = new File(this.getExcelDir() + "/CustomersWeb.xlsx");
		
		try {
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;


			for (int i = 1 ; i < sheet.getLastRowNum()+1 ; i++) {
			
				row = sheet.getRow(i);
				String cuID = "";
				String ags = "";

				if (row != null && row.getCell((short) 0) != null) {
									try {
						cuID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						cuID = (row.getCell((short) 0).getStringCellValue());
					}
				}	
				
				if (row != null && row.getCell((short) 18) != null) {
					try {
					ags = (row.getCell((short) 18).getStringCellValue());
					} catch (Exception e) {
						
					}
				}
				
		             res.put(cuID, ags);
				}
		}catch(Exception e) {
			e.printStackTrace();
		}
			
		//System.out.println(res.toString());
		
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

	  return res;
  }
  
  private TreeMap getExcelManas() {
	  TreeMap res = new TreeMap();
	  
	  File f = new File(this.getExcelDir() + "/CustomersWeb.xlsx");
		
		try {
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;


			for (int i = 1 ; i < sheet.getLastRowNum()+1 ; i++) {
			
				row = sheet.getRow(i);
				String cuID = "";
				String ags = "";

				if (row != null && row.getCell((short) 0) != null) {
									try {
						cuID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						cuID = (row.getCell((short) 0).getStringCellValue());
					}
				}	
				
				if (row != null && row.getCell((short) 10) != null) {
					try {
					ags = (row.getCell((short) 10).getStringCellValue());
					} catch (Exception e) {
						
					}
				}
				
			//	System.out.println(cuID + " " + ags);
				
				StringTokenizer st = new StringTokenizer(ags,",");
				while(st.hasMoreElements()) {
					String mainAgent = st.nextToken();
					mainAgent = mainAgent.substring(0,mainAgent.indexOf(":"));
					 ArrayList agsw = new ArrayList();
	            	 if(res.get(cuID) != null) {
	            		  agsw = (ArrayList)res.get(cuID);
	            	  }
	           	 	  agsw.add(mainAgent);
		             res.put(cuID, agsw);
				}
				
				
				}
		}catch(Exception e) {
			e.printStackTrace();
		}
			
		
		
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

	  return res;
  }
  
  
  private void loadAgentsandEmail() {
	  webAgendid = new TreeMap();
	  
	  try {
		  webAgendid = (TreeMap) readObject("webagents.trm"); 
	  }catch(Exception e) {
		 e.printStackTrace();
	  }
	// System.out.println("webagen " + webAgendid ); 
  }
  
  
	
  
  
  private TreeMap webAgendid = null;
  
  public TreeMap getCustomers(Agent agent){
	  
	  TreeMap openOrders = this.getOpenOrders();
	  
	  ArrayList samples = new ArrayList();
	  try {
		Converter.readFile(new File("sampleaccounts.txt"), "utf-8", samples);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  System.out.println("sampleaccounts " + samples.toString());
	  
try{
	
	//TreeMap aa = Converter.getWebCustomersAgents(rcompany, user, pass);
	TreeMap aa = getExcelManas();
	//System.out.println("aa " + aa.toString());
	Converter.saveObject(aa, "webagents.trm");
	//System.exit(0);

	   Converter.getManagersReplacements(agent1, rcompany, user, pass); 



}catch(Exception e){
e.printStackTrace();
}


	   HashSet lepingud = new HashSet();
	   
 String cmd = "  SELECT   cus.name,cus.ouraccountnum as nimelisa,cus.namealias,cus.accountnum, cus.invoiceaccount AS payer, " +
            "  (SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct " + 
             	                    " on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and "  + 
             	                    " ct.storno = 0  and co.duedate<getdate()  and co.dataareaid = 'ct'   " +
             "  AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else   " +
 	         " cus.invoiceaccount end )) AS debt, "
 	         + "(SELECT sum(co.amountmst ) FROM custtransopen co inner join CUSTTRANS ct "  + 
 	          	                    " on ct.RECID=co.REFRECID and ct.DATAAREAID=co.DATAAREAID WHERE ct.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') and "  + 
 	          	                    " ct.storno = 0 AND  co.dataareaid = 'ct'  "+  
              " AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else  " + 
 	         " cus.invoiceaccount end )) AS debta, " + 
			  "(select max (creditmax) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ct') " + 
			  "  as limit ,cus.linedisc , cus.address, '' AS visit ,  SPIEAGENTACCOUNTID as agent , SPITELESALESAGENTGROUPID as teles,  (select top 1 numofdays from paymterm pt where pt.paymtermid=	" +
			  " (select top 1 paymtermid from custtable where accountnum = cus.invoiceaccount and dataareaid in ('ct')  ) and pt.dataareaid in ('v02')) AS term, " +
			  " blocked AS block , " +
			  " (select max (blocked) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ct') " +
			  "  as payerblock 	,cus.phone  + ' , ' +  cus.cellularphone as info , PriceGroup as grupp , COMPANYCHAINID as chain , "
			  + " (select max (creditrating ) from custtable cc where  cc.accountnum=cus.invoiceaccount and cc.dataareaid = 'ct') as rating "
			  + " ,subsegmentid as sugg , erpagentdiscedit as maxallow , custclassificationid as ette ,  subsegmentid as cate  ,cus.memo as cuinfo"
			  + " , cus.StatisticsGroup as fixed, a.city as city , a.street as street, a.zipcode as ind  , agreementdisc as leping, "
				+"  (select spiminorderamount from smmBusRelChainGroup e where e.dataareaid= cus.dataareaid and e.chainid = COMPANYCHAINID ) as minam , ItemConfigId as dealer"
				
			  + " FROM custtable cus  LEFT JOIN dirpartytable dp ON dp.partyid=cus.partyid  inner join address a on a.ADDRRECID = dp.RECID left outer JOIN paymterm  pay on cus.paymtermid=pay.paymtermid and  pay.dataareaid='ct'  " +
			  "	 WHERE   cus.dataareaid in ('ct')   and (SPIEAGENTACCOUNTID != '' or SPITELESALESAGENTGROUPID != '')  "
			  + " and erpactive = 1  and  internal != 1 " ; //and blocked != 2
 
 
 String ccc = "select invoiceaccount from custtable where dataareaid='ct' group by invoiceaccount";
 
 String promo = "select custaccount as custid , l.promonum as grupp   from astPromoJournal pri  " + 
		" INNER JOIN astPromoCustomers l on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  where " +
		" l.dataareaid = 'ct' and ( (GETDATE() between PRI.FROMDATE-(case when DATEPART(DW, PRI.FROMDATE) = 2 then 3 else 1 end) AND PRI.TODATE -(case when DATEPART(DW, PRI.TODATE) = 1 then 2 else 0 end)) " +
	  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
	  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900) " +
	  " or( GETDATE()<=PRI.TODATE+1 AND year(PRI.fromDATE) =1900) ) and promostatus in (1,4)";
 
          
           String salessql = " SELECT  jou.orderaccount as klient,( ltrim( case when month(sal.invoicedate) <10 then  '0' + ltrim(str(month(sal.invoicedate))) else ltrim(str(month(sal.invoicedate))) end) ) as kuu, " +
        "  round(SUM(lineamount),0) AS summa " +
	    "  FROM custinvoicetrans sal with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON  " +
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid   " +
		" WHERE sal.dataareaid='ct'   AND jou.dataareaid='ct' AND sal.invoicedate > getdate()-200 " +
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
    HashSet custs = new HashSet();
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
    
     
     
     String lepsql = "   select  pri.groupid as grupp  from ASTPRICEDISCTABLEAGREEMENT pri  " +
       		 " where  pri.dataareaid = 'ct' and ((GETDATE() between PRI.FROMDATE AND PRI.TODATE+1) " +
       	  " or(GETDATE()>=PRI.FROMDATE AND year(PRI.toDATE) =1900)  " +
       	  " or(year(PRI.FROMDATE) =1900 and year(PRI.toDATE) =1900)  " + 
       	  " or( GETDATE()<=PRI.TODATE+1)) group by pri.groupid" ;
        
        
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
     
     
     
     
 // teeme return reason file valmis 
     
     
     
     
     stmr = con.createStatement();
 	StringBuffer sb = new StringBuffer();
     rs1 = stmr.executeQuery("select reasoncodeid as id, description as nimi from ReturnReasonCode where dataareaid ='ct'");
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
   payers.add(rs1.getString("invoiceaccount"));
      
    }
   rs1.close();
   stmr.close();
int i = 0;

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
        	name =  name + " " + alias.replace('*', ' ').trim() ;
        }
                 
        
        String accountnum = rs.getString("accountnum").trim();
        Customer cust = new Customer(accountnum);

        name = name + " " + accountnum;
        
        cust.setName(Converter.enCodeEst(name));
        String payer = rs.getString("payer");
        if(payer == null || payer.trim().length()== 0){
        	cust.setPayer(cust.getID());
        	cust.setIsPayer(true);
        	
        	
        //	System.out.println("is Payer " + cust.getID() + " " + payer.trim());
        }else{
        	cust.setPayer(payer.trim());
        
        	if(cust.getID().equals(payer.trim())) {
        		
        		//System.out.println("is Payer " + cust.getID() + " " + cust.getName());
        	}
        }
        
        
        cust.restdirect = "direct";
       
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
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',',')),"UK"));
        }
        String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }
        String age = rs.getString("agent");
        if (age != null) {
         cust.addAgent(new Agent(age.trim()));
        // cust.addAgent(new Agent("WARE"));
        // cust.setMainAgent(age.trim());
         if (age != null && agent1.get(age.trim()) != null){
				Agent aag = (Agent)agent1.get(age.trim());
			
				Vector vec = aag.getDubbler();
				if(vec!= null && vec.size() >0){
					Enumeration en = vec.elements();
					while(en.hasMoreElements()){
						String ag = (String) en.nextElement();
						cust.addAgent(new Agent(ag));
					//	System.out.println("lisan dublante " + age.trim() + " " + ag);
					}
				}
			}
        }
    
        String teles = rs.getString("teles");
        if (teles != null && teles.trim().length() > 0) {
         cust.addAgent(new Agent(teles.trim()));
         System.out.println("lisan telesales  " + teles.trim());
		
        }
        
        
        if(samples.contains(cust.getID())) {
        	 cust.diffBack2 = true;
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
        	cust.setInfo(Converter.enCodeEst("CHART Sales :|Month and year|sales |" )+ salesres.get(accountnum) );
        }
        
        if(transa.get(cust.getID())!= null){
        	
        	
        }
        
        if(visits.get(cust.getID())!= null){
        	cust.setVisit(visits.get(cust.getID())+"");
        }
        
        
        
        String grupp = rs.getString("grupp");
        if (grupp != null && grupp.trim().length()>0 && grupp.indexOf("DPD") == -1) {
         
          if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
      		cust.setGroup(cust.getGroup() + "|" + Converter.cleanLitLatEst(grupp.trim()));
      	}else{
      		cust.setGroup(Converter.cleanLitLatEst(grupp.trim()));
      	}
          
          cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
       //   System.out.println("fix juurde " + grupp.trim());
          cust.addFixSortCode(grupp.trim());
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
        }
        
        
        String licec = cust.getLicence();
        
        
        String rating = rs.getString("rating");
        
      
     
        int maxallow = rs.getInt("maxallow");
                    
        if(maxallow == 1){
        	cust.setMaxdisc(1);
        }
                    
        String ette = rs.getString("ette");
        if (ette != null && ette.trim().equals("PRAPM")) {
         	cust.isSalesBlockPrepay();
        }
        
        
 String cate = rs.getString("cate");
 if (cate != null && cate.trim().length() > 0) {
  	cust.setAnkeediTyyp(cate.trim());
  	cust.category = cate.trim();
  	
  	 if(cate.trim().equals("Wholesaler")) {
    	 cust.restPartner = accountnum;
    }
 }
                    

 String cuinfo = rs.getString("cuinfo");
 if (cuinfo != null && cuinfo.trim().length() > 2) {
  	cust.setInfo(cuinfo.trim().replaceAll("\\r\\n|\\r|\\n", " ").replaceAll(";", ":"));
 }
            
 
 if(cust.isPayer() && !samples.contains(cust.getID())) {
	 cust.setInfo("Seems that it's PAYER - do not make order to this account");
 }
 
 if(samples.contains(cust.getID())) {
	 cust.setInfo("This account is for SAMPLES ONLY, "
	 		+ "all what you enter on productlist is in BOTTLES");
 }
           
// System.out.println(cust.getInfo());
                               i++;
                               
                               

                               String city  = rs.getString("city");
                               if (city != null ) {
                                	cust.restCity = city.trim();
                               }
                               String street  = rs.getString("street");
                               if (street != null ) {
                                	cust.restStreet = street.trim();
                               }
                               
                               String ind  = rs.getString("ind");
                               if (ind != null ) {
                                	cust.restZipCode = ind.trim();
                               }
                               
                               
                               String leping = rs.getString("leping");
                               // kui on määratud siis teeme määrangute järgi 
                             
                                   if(leping != null && leping.trim().length() > 0 && lepingud.contains(leping.trim())){ //lepingud. kas on aktiivne leping
                               		if(cust.getGroup() != null && cust.getGroup().trim().length() > 0 ){
                                   		cust.setGroup(cust.getGroup() + "|" + leping.trim());
                                   	}else{
                                   		cust.setGroup(leping.trim());
                                   	}
                               	   cust.addFixSortCode(leping.trim());
                                   }
                               
                                   double minam = rs.getDouble("minam");
                                   if(minam > 0 ){
                                	   cust.setMinsum(minam);
                                   }
                                   if(openOrders.get(cust.getID()) != null){
                                   	cust.addToCustDataVec(cust.getID() + ";tarasaldo; ;Open Order |"   +  openOrders.get(cust.getID()));
                                   }              
                    
                                   
                                   String ladu = rs.getString("dealer");
                                   if(ladu != null && ladu.trim().length() > 0){
                                   	cust.setDealer(new Dealer(ladu.trim()));
                                   }else {
                                	   cust.setDealer(new Dealer("DS"));
                                   }
                                   
                    if(goes){
                        customers.put(name, cust);
                        custMap.put(cust.getID(),cust);	
                        custs.add(cust.getID());
                    }
      }

      
      

      String sql = "  SELECT a.orderaccount AS klient, a.accountnum as payer ,a.invoice AS number,co.duedate AS maksepaev,co.amountmst AS tasumata,  " +
             "  co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle  FROM custtransopen co with(nolock)  " +
			" INNER JOIN custtrans a with(nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
            "   WHERE co.dataareaid='ct' AND a.dataareaid='ct'  and a.invoice != '' " +
			"	and a.storno = 0 and a.POSTINGPROFILE not in('SAUBD', 'ABEJSKOL', 'DOUBT') ORDER BY a.accountnum,co.transdate " ; //and co.duedate < getdate()+5 
		  
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
    
    if("true".equals(getParent().getProps().getProperty("send.assets"))){
    	this.sendAssets(custs);
    }
    return customers;
  }


  
  public TreeMap getDealers() {

      TreeMap dealers = new TreeMap();
   

    	
                   Dealer dealer = new Dealer("NDS");
                   dealer.setName("NDS");
                   dealers.put(dealer.getID(), dealer);
      
                   Dealer dealer2 = new Dealer("DS");
                   dealer2.setName("DS");
                   dealers.put(dealer2.getID(), dealer2);
      
	  return dealers;
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
  	    String    sql = "   select ITEMSUBGROUPID as liik , name as nimi from INVENTITEMSUBGROUP where dataareaid ='ct' ";
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

    if(liik.equals("WINE")){
  	    	  nimi = "Wine";
  	      } 
  	      
  	      if(liik.equals("SPAR")){
  	    	  nimi = "Sparkling";
  	      }
  	      
  	    if(liik.equals("POS_")){
	    	  nimi = "POS sell";
	      }
  	     if(liik.equals("POS0")){
    	  nimi = "POS consumable";
          }
  	      
  	        tm.put(liik,  Converter.enCodeEst(nimi.toLowerCase()));

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
    		    
    		  String    sql = "  select  inv.ITEMSUBGROUPID as abi , BRANDID as liik , name as nimi from SPIBRANDTABLE sp inner join inventtable inv " + 
    	       " on sp.BRANDID = inv.SPIBRANDID and sp.dataareaid=inv.dataareaid "
    	       + " where sp.dataareaid ='ct' group by BRANDID,inv.ITEMSUBGROUPID, sp.name " ;
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

	  
    String cmd  = "SELECT inv.itemid, inv.itemname AS nimetus, ERPOLDITEMID as oldcode , inv.taxpackagingqty AS kastis,inv.standardpalletquantity AS alusel, disc.amount AS price   \r\n" + 
    		"    		     , bar.itembarcode AS ean  ,bar2.itembarcode as ean2,inv.ITEMSUBGROUPID AS grupp,inv.SPIBRANDID AS grupp2, origcountryregionid as coun,mod.unitid As unit, '21' as vat,mod.LineDisc as alegr ,inv.alcstrengthgroupid as gruppale    \r\n" + 
    		"    	     , ITEMGROUPID as type ,lowestqty as mini , multipleqty as opti ,NEWITEM as uus ,sellout as out , topitem as tops , topoffitem as tops2, giftbox as gift\r\n" + 
    		"    		     ,  itemfreetextannotation  as annot , SPIDONTCONTROLMINAMOUNTMS as nocount ,inv.grossWidth , \r\n" + 
    		"				 inv.grossDepth, inv.grossHeight, inv.NetWeight+inv.TaraWeight  as grossWeight , inv.qtyPerLayer AS layer\r\n" + 
    		"				 ,LengthCS,WidthCS ,HeightCS,WeightEmptyCS, (inv.NetWeight+inv.TaraWeight)*inv.taxpackagingqty + WeightEmptyCS as fcase FROM inventtable inv \r\n" + 
    		"    		inner join InventItemSalesSetup s  on s.itemid = inv.itemid  and s.dataareaid=inv.dataareaid  and s.InventDimId = N'AllBlank' \r\n" + 
    		"    		inner join inventtablemodule mod  ON mod.itemid = inv.itemid  and mod.dataareaid=inv.dataareaid   AND mod.moduletype=2    \r\n" + 
    		"    		left join pricedisctable as disc  on inv.itemid=disc.itemrelation  and disc.dataareaid=inv.dataareaid \r\n" + 
    		"    		   and disc.module in ( 0 , 1 )   and disc.relation=4   and disc.itemcode=0 and disc.accountcode=1\r\n" + 
    		"    		   and disc.accountrelation = 'DPD'   and disc.Currency = 'GBP' and disc.unitid = mod.unitid  and disc.fromdate <= getdate() \r\n" + 
    		"    		   and(disc.todate >= getdate() or disc.todate = '')     left join inventitembarcode bar    on bar.dataareaid = inv.dataareaid  \r\n" + 
    		"    		   and bar.itemid = inv.itemid    and bar.barcodesetupid ='EANBottle'   and bar.InventDimId = N'AllBlank'   and len(bar.itembarcode)>8   \r\n" + 
    		"    		   and bar.useforprinting = 1 left join inventitembarcode bar2  on bar2.dataareaid = inv.dataareaid \r\n" + 
    		"    		   and bar2.itemid = inv.itemid    and bar2.barcodesetupid ='EANCase'    and bar2.InventDimId = N'AllBlank' WHERE s.stopped = 0 and inv.dataareaid='ct' and inv.active = 1   \r\n" + 
    		"    		order by inv.itemid,  disc.module , disc.todate" ;
    
    
    TreeMap products = new TreeMap();
    getSaveStockInfo();
     
    
    
 
    try { 

    
     if (isDebug()) System.out.println(cmd);

     Connection con = this.getNewConnection();
   
     
 	Statement stmt =  con.createStatement();
       ResultSet  rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus =  " " +rs.getString("nimetus").trim() ;
         String oldcode = rs.getString("oldcode");
         nimetus =  Converter.enCodeEst(nimetus);
 


     boolean goes = true;
     
        prod.setInCases(rs.getDouble("kastis"));
        double kord = 1;
        if(prod.getInCases()> 1) {
        	kord = prod.getInCases();
        }
        prod.setOnPallet(rs.getDouble("alusel")/kord);
        double price = rs.getDouble("price"); 
        if(price > 0){
            prod.setPrice(price);
        }  else if(!prod.getID().startsWith("60")){
     		  // goes = false;
        }
        	

        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean);
        int ll  = ean.trim().length();
        if(ean.length() >4){
           // add = " " +  ean.trim().substring(ll-4);	
        }
        }
        
        String ean2 = rs.getString("ean2");
       
        if(ean2 != null && ean2.trim().length()>0){
        prod.setPartii("EANCase " + ean2.trim());
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
             
             String coun = rs.getString("coun");
             if(coun != null){
           	 // prod.setGroupSecond(prod.getGroup() + coun);
             }
             
        	
     }
       
    
       
       
       prod.setAgentGroup("ALL");
  
       String ette = "";
       if(kogused.get(kood) != null){
    	  
    	  
    
       }else if(prod.getID().startsWith("60") || prod.getID().startsWith("70")){
    		   goes = false;
    	   }
      
       prod.setInCases(1);
      
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

      
      String prodALC = rs.getString("gruppale");
      if(prodALC !=null && prodALC.trim().length()>0){
   	   
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
      
      String bre = " " + prod.getID();
      prod.setName(Converter.enCodeEst(ette + nimetus + add + bre));
      
   
       if(grupp.trim().startsWith("POS")){
    	   String posSt = "";
    	   if(posseisud.get(prod.getID()) != null) {
    		   posSt = " |Sto:" + posseisud.get(prod.getID())  + "|";
    		  
    		    	  if(prod.getPartii() != null && prod.getPartii().trim().length() > 1) {
    		    		  prod.setPartii(prod.getPartii() + "|DAWDP:"+ posseisud.get(prod.getID())); 
    		    	  }else {
    		    		  prod.setPartii("DAWDP:"+ posseisud.get(prod.getID()));
    		    	  }
    		      
    		      
    	   }
    	    prod.setName(Converter.enCodeEst("xx " + nimetus + posSt));
    	    prod.setDiffColor(true);
    	    prod.setInCases(0);
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
      if(prod.getStock() < 1){
    	  goes = false;
      }
      }

      int topitem = rs.getInt("tops");
      if(topitem == 1){
    	//  prod.setStatusTop("1");
      }

      int topitemoff = rs.getInt("tops2");
      if(topitemoff == 1){
    	   //prod.setStatusTop("1");
      }
      

      int giftitem = rs.getInt("gift");
      if(giftitem == 1){
    	  prod.setStatusGiftPack("1");
      }

      
      String annot = rs.getString("annot");
      if(annot != null && annot.trim().length() > 0){
    	 
      }
      
      int  nocount = rs.getInt("nocount");
      if(nocount == 1){
    	 prod.sumNoCount = true;
      }
      
     
      prod.layer = (rs.getDouble("layer")/kord);
      
      double grossLengthCS = rs.getDouble("LengthCS");
      double grossWidthCS = rs.getDouble("WidthCS");
      double grossHeightCS = rs.getDouble("HeightCS");
      double grossWEmptyCS = rs.getDouble("WeightEmptyCS");
    
      double grossWeightCS = rs.getDouble("fcase");
      
    
      
      String partii = "";
      if(grossLengthCS > 0) {
        	partii = " LengthCS: " +  nf.format(grossLengthCS) + " m";
          }
      if(grossWidthCS > 0) {
      	partii = partii + "| WidthCS: " +  nf.format(grossWidthCS) + " m";
        }
      
      if(grossHeightCS > 0) {
        	partii =  partii + "| HeightCS: " +  nf.format(grossHeightCS) + " m";
          }
      
        if(grossWEmptyCS > 0) {
          	partii =  partii + "| WeightEmptyCS: " +  nf.format(grossWEmptyCS) + " kg";;
            }
        if(grossWeightCS > 0) {
          	partii =  partii + "| WeightCS: " +  nf.format(grossWeightCS) + " kg";;
            }

      
    

      if(partii.length() > 0) {
    	  if(prod.getPartii() != null && prod.getPartii().trim().length() > 1) {
    		  prod.setPartii(prod.getPartii() + "|Dimensions|"+ (partii)); 
    	  }else {
    		  prod.setPartii("Dimensions|" + partii);
    	  }
      }
      
      
     
      
      if(goes){
     	  products.put(Converter.cleanLitLatEst(ette) + nimetus + prod.getID(), prod);
          prodHash.put(prod.getID(), prod);   
    
      }
       

      }
      rs.close();
    stmt.close();
    con.close();
   
      
      
      
//see on vana
	  cmd = " select  PRI.PROMONUM as grupp,l.ITEMID as toode ,l.PRICE as hind , fromqty as kogus ,(1-l.price/qtyprice)*100 as perc,linedisc "+
       " from astPromoGMLine l INNER JOIN astPromoJournal pri on pri.dataareaid = l.dataareaid and pri.promonum = l.promonum  " +
       " where l.dataareaid = 'ct' and ( GETDATE() between PRI.FROMDATE-1 AND PRI.TODATE ) " +
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
	                  		double  hind  = ((Product)prodHash.get(toodex)).getPrice();
		                  	double  kogus = rs.getDouble("kogus");
		                  	double  perc  = rs.getDouble("perc");
		                  	double  lined = 0;
		                  	
		                  	if(kogus == 0){
		                  		kogus = 1;
		                  	}
		                  	
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
    

      
    makeTexts();
      
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }

  
  private String make (String type, String value) {
	  String res = "";
	  if(value != null && value.trim().length() > 0) {
		  res = "|" + value.trim() + " : " +  type ;
	  }
	  return res;
  }

 private void makeTexts() throws Exception{
	  
	  
	  String    sql = " SELECT inv.itemid as kood,  itemfreetextannotation  as asi, grossWidth,grossDepth,grossHeight"
	  		+ ", inv.NetWeight+inv.TaraWeight  as grossWeight FROM inventtable inv  " +  
" WHERE  inv.dataareaid='ct'  and itemfreetextannotation !='' and  active = 1  " +
" and inv.ITEMID not like  'AM%' and inv.ITEMID not like  'NE%'";
	  StringBuffer sb = new StringBuffer();
	    TreeMap res = new TreeMap();
	  
  Connection con =  this.getNewConnection();
  Statement stmt = con.createStatement();
   ResultSet rs = stmt.executeQuery(sql);
   
   while (rs.next()) {
     String kood=rs.getString("kood").trim();
    	 String nimetus=rs.getString("asi");
    			 if(nimetus != null){
    				 nimetus = nimetus.trim().replaceAll("\\r\\n|\\r|\\n", "|").replaceAll(";", ":"); 
    				 
    				  
    				   double grossWidth = rs.getDouble("grossWidth");
    				   double grossDepth = rs.getDouble("grossDepth");
    				   double grossHeight = rs.getDouble("grossHeight");
    				   double grossWeight = rs.getDouble("grossWeight");
    				     if(grossWidth > 0) {
    				    	 nimetus = nimetus + " Width: " +  nf.format(grossWidth) + " mm";
    				 }
    				 if(grossDepth > 0) {
    					 nimetus = nimetus + "| Depth: " +  nf.format(grossDepth) + " mm";
    				   }
    				 if(grossHeight > 0) {
    					 nimetus =  nimetus + "| Height: " +  nf.format(grossHeight) + " mm";
    				     }
    				 if(grossWeight > 0) {
    					 nimetus =  nimetus + "| Weight: " +  nf.format(grossWeight) + " kg";;
    				     }
    				   
    				 
    				 
    				 
    				 res.put(kood, nimetus);  
    			 }
      }
   
   rs.close();
   
   
   
   String sql2 = "select * from InventTableNuitritionalData where dataareaid = 'ct'";
 
   rs = stmt.executeQuery(sql2);
   
   while (rs.next()) {
         String kood=rs.getString("itemid").trim();
    	 String nimetus = "";
    	 
    	 String COUNTRYREGIONID = rs.getString("COUNTRYREGIONID");
    	 nimetus = nimetus + make("Country of Origin",COUNTRYREGIONID);
    	 
    	 String LOCALINGREDIENTS65 = rs.getString("LOCALINGREDIENTS65");
    	 nimetus = nimetus + make("Local Ingrediens",LOCALINGREDIENTS65);
    	 
    	 
    	 double SERVINGSPERPACKAGE = rs.getDouble("SERVINGSPERPACKAGE");
    	 if(SERVINGSPERPACKAGE > 0) {
    		 nimetus = nimetus + make("Servings per Package",(int)SERVINGSPERPACKAGE + "");
    	 }
    	 
    	 String INGEDIENTS = rs.getString("INGEDIENTS");
    	 if(INGEDIENTS != null) {
    		 nimetus = nimetus + make("Ingrediens",INGEDIENTS+"");
    	 }
    	
    	 
    	 

    	 double KILOCALORIES = rs.getDouble("KILOCALORIES");
    	 if(KILOCALORIES > 0) {
    		 nimetus = nimetus + make("Kilocalories",(int)KILOCALORIES + "");
    	 }
    	
    	 double CALORIESPERSERVING = rs.getDouble("CALORIESPERSERVING");
    	 if(CALORIESPERSERVING > 0) {
    		 nimetus = nimetus + make("Calories per serving",(int)CALORIESPERSERVING + "");
    	 }
    	
    	 
    	 double CALORIESPARBOTTLE = rs.getDouble("CALORIESPARBOTTLE");
    	 if(CALORIESPARBOTTLE > 0) {
    		 nimetus = nimetus + make("Calories per bottle",(int)CALORIESPARBOTTLE + "");
    	 }
    	 
    	 
    	 double ENERGYKJ = rs.getDouble("ENERGYKJ");
    	 if(ENERGYKJ > 0) {
    		 nimetus = nimetus + make("Energy KJ",(int)ENERGYKJ + "");
    	 }
    	 
    	 
    	 double PROTEIN = rs.getDouble("PROTEIN");
    	 if(PROTEIN > 0) {
    		 nimetus = nimetus + make("Protein",(int)PROTEIN + "");
    	 }
    	 
    	 
    	 double CARBOHYDRATE = rs.getDouble("CARBOHYDRATE");
    	 if(CARBOHYDRATE > 0) {
    		 nimetus = nimetus + make("Carbohydrate",(int)CARBOHYDRATE + "");
    	 }
    	 
    	 double FATOFWHICHSATURATES = rs.getDouble("FATOFWHICHSATURATES");
    	 if(FATOFWHICHSATURATES > 0) {
    		 nimetus = nimetus + make("Fat of which saturates",(int)FATOFWHICHSATURATES + "");
    	 }
    	 
    	 double FAT = rs.getDouble("FAT");
    	 if(FAT > 0) {
    		 nimetus = nimetus + make("Fat",(int)FAT + "");
    	 }
    	 
    	 double SODIUM = rs.getDouble("SODIUM");
    	 if(SODIUM > 0) {
    		 nimetus = nimetus + make("Sodium",(int)SODIUM + "");
    	 }
    	 
    	 double CARBOHYDRATEOFWHICHSUGARS = rs.getDouble("CARBOHYDRATEOFWHICHSUGARS");
    	 if(CARBOHYDRATEOFWHICHSUGARS > 0) {
    		 nimetus = nimetus + make("CarboHydrateOfWhitchSugars",(int)CARBOHYDRATEOFWHICHSUGARS + "");
    	 }
    	 
    	 double SUITABLEFORVEGETARIANS = rs.getDouble("SUITABLEFORVEGETARIANS");
    	 if(SUITABLEFORVEGETARIANS > 0) {
    		 nimetus = nimetus + make("SuitableForVegetarians",(int)SUITABLEFORVEGETARIANS + "");
    	 }
    	
    	 
    	 
    	 double SUITABLEFORVAGANS = rs.getDouble("SUITABLEFORVAGANS");
    	 if(SUITABLEFORVAGANS > 0) {
    		 nimetus = nimetus + make("SuitableForVegans",(int)SUITABLEFORVAGANS + "");
    	 }
    	 
    	 double SUITABLEFORLACTOSEINTOLERANTS = rs.getDouble("SUITABLEFORLACTOSEINTOLERANTS");
    	 if(SUITABLEFORLACTOSEINTOLERANTS > 0) {
    		 nimetus = nimetus + make("SuitableForLactoseINTolerance",(int)SUITABLEFORLACTOSEINTOLERANTS + "");
    	 }
    	 double SUITABLEFORNUTALLERGYSUFFERERS = rs.getDouble("SUITABLEFORNUTALLERGYSUFFERERS");
    	 if(SUITABLEFORNUTALLERGYSUFFERERS > 0) {
    		 nimetus = nimetus + make("SuitableForNUTALLERGYSUFFERERS",(int)SUITABLEFORNUTALLERGYSUFFERERS + "");
    	 }
    	 
    	 //          FIBREG    
    	 // SUITABLEFORCOELIACS MARKEDASKOSHER MARKEDASHALALAPPROVED FREEFOMARTIFICIALCOLOURS FREEFROMGMADDITIVES
    	 //FREEFOMPRESERVATIVES FREEFOMDECLARABLEGMFOODSTUFFS FREEFROMADDEDGLUTAMATES FREEFROMMILK FREEFROMEGG FREEFROMFISH FREEFROMPEANUTS
    	 //FREEFROMTREENUTS FREEFROMTREENUTOILS FREEFROMWHEATRYEBARLEYOATS FREEFROMSEEDSEXCLUDINGSPICES FREEFROMMAIZE FREEFROMSOYA FREEFROMBEEF
    	 //FREEFROMMEAT FREEFROMPOULTRY FREEFROMSULPHITES FREEFROMMUSTARD FREEFROMCELERY FREEFROMMOLLUSC FREEFROMLUPINS FREEFROMCRUSTACEANS 
    	 //FREEFROMADDITIVES FREEFROMMRM FREEFROMIRRADIATEDFOODSTUFFS 

    	 
    	 
    			 if(nimetus != null){
    				 nimetus = nimetus.trim().replaceAll("\\r\\n|\\r|\\n", "|").replaceAll(";", ":"); 
    				 if(res.get(kood) != null) {
    					 res.put(kood, res.get(kood) + "|" + nimetus);  
    				 }else {
    					 res.put(kood, nimetus);   
    				 }
    				
    			 }
      }
   
   rs.close();
   stmt.close();
   con.close();
   
   Iterator itera  = res.keySet().iterator();
   while(itera.hasNext()) {
	   String kood = (String)itera.next();
	   String nimetus = (String)res.get(kood);
	   sb.append(kood + ";" + nimetus + "\n");
   }
   
 
     Converter.saveFileInUTF8("texts.txt", sb);

  }
  
 
 
 private TreeMap  getOpenOrders(){
		TreeMap res = new TreeMap();
		

		   String sql2 =  "select  sal.salesid AS id,case when ediordernumber like 'MS-%' then 1 else 0 end as abi, sal.custaccount AS kkood , "+ 
"sal.salesid as number ,SPISalesOrderStatus  as status,sal.createddatetime AS kuup, estimate AS summa "+
"from SalesTable sal with (nolock) "+
"WHERE sal.dataareaid='ct' AND sal.createddatetime > getdate()-60 and SPISalesOrderStatus in"
+ "  ('10','20','25','30','40','50','51','59','71','93','94','96') "+
"ORDER BY sal.createddatetime " ;
	

	
	 this.connectionUp();
	try {
	   nf.setMinimumFractionDigits(2);
	   Connection con = this.getNewConnection();
	   PreparedStatement stmt2 = con.prepareStatement(sql2);
	   ResultSet  rs = stmt2.executeQuery();


	while(rs.next()){
		StringBuffer sb = new StringBuffer();
		String id = rs.getString("id");
		 int abi = Integer.parseInt(rs.getString("abi"));
		 String text = "edi";
		 if(abi == 1){
			 text = "merisoft";
		 }
	    String nimi = rs.getString("kkood");
	    if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim() + " ('" +getStatusText(rs.getString("status").trim()) +"' ");
		sb.append(rs.getString("kuup").trim().substring(0,10) + " " + text + ") ");
		

		double summa = Double.parseDouble(rs.getString("summa").trim());
		if(summa > 0 ){
			String sum = getNF().format(summa) ;
		    sb.append(sum + " £ |" );	
		}else{
			 sb.append("|" );	
		}
		
	  
	    String sql3 = "select qtyordered/(case when inv.taxpackagingqty = 0 then 1 else  inv.taxpackagingqty end) as amount,  salesunit as unit  , name from  " +
				   " SalesLine rid INNER JOIN inventtable inv with(nolock) ON rid.itemid=inv.itemid  and  inv.dataareaid= rid.dataareaid  where rid.dataareaid='ct' and  salesid =  '" + id + "'";
	    Connection cc = this.getNewConnection();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
		 
	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    }
	   
		    rs2.close();
	       stmt3.close();
	       cc.close();
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	       
	       }else{
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	    	   
	       }
	   	
	}
	
	
	// siin lisame Realizatsiooni asjad juurde 
	
	sql2 = "		select  sal.salesid AS id,case when ediordernumber like 'MS-%' then 1 else 0 end as abi,  " +
		   "  sal.custaccount AS kkood , sal.salesid as number ,sal.createddatetime AS kuup , " +
		   "	 estimate AS summa from SalesTable sal  WHERE sal.dataareaid='ct'  AND  " +
		   "  sal.createddatetime > getdate()-360  and sal.salesid like 'REALI%' ORDER BY sal.createddatetime";
	
      stmt2 = con.prepareStatement(sql2);
	     rs = stmt2.executeQuery();


	while(rs.next()){

		StringBuffer sb = new StringBuffer();
		String id = rs.getString("id");
		 int abi = Integer.parseInt(rs.getString("abi"));
		 String text = "edi";
		 if(abi == 1){
			 text = "merisoft";
		 }
	    String nimi = rs.getString("kkood");
	    if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim()+ " Realization");
		sb.append(rs.getString("kuup").trim().substring(0,10) + " " + text + ") ");
		

		double summa = Double.parseDouble(rs.getString("summa").trim());
		if(summa > 0 ){
			String sum = getNF().format(summa) ;
		    sb.append(sum + " eur ar PVN|" );	
		}else{
			 sb.append("|" );	
		}
		
	  
	    String sql3 = "select qtyordered as amount,  salesunit as unit  , name from  " +
				   " SalesLine rid where rid.dataareaid='ct' and  salesid =  '" + id + "'";
	    Connection cc = this.getNewConnection();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
		 
	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    }
	   
		    rs2.close();
	       stmt3.close();
	       cc.close();
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }else{
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }
	   	
	}
	
	
	
	rs.close();
	stmt2.close();
	con.close(); 	 
	

	
	
	
	
	
	} catch (Exception e) {
	  getParent().showError(e);
	}

	connectionDown();

		
		return res;
	}
  
 private String getStatusText(String aa){
	 if(aa.equals("10")){
         return "Open";
}
if(aa.equals("20")){
         return "Prepayment";
}
if(aa.equals("25")){
         return "HigherPromo";
}

if(aa.equals("30")){
         return "CreditControl";
}
if(aa.equals("40")){
         return "LowCost";
}
if(aa.equals("50")){
         return "Confirmed";
}
if(aa.equals("51")){
         return "Execution";
}
if(aa.equals("52")){
         return "Completed";
}
if(aa.equals("53")){
         return "Invoiced";
}
if(aa.equals("54")){
         return "OnTheWay";
}
       
if(aa.equals("55")){
         return "Canceled";
}
       
if(aa.equals("56")){
         return "PartlyCompleted";
}
       
if(aa.equals("57")){
         return "Returned";
}
if(aa.equals("58")){
         return "UnConfirmed";
}
       
if(aa.equals("59")){
         return "Verified";
}
if(aa.equals("71")){
         return "Sent to 3PL warehouse";
}
if(aa.equals("72")){
         return "EEWhsConfirmError";
}
if(aa.equals("73")){
         return "EEWhsConfirmation";
}
if(aa.equals("74")){
         return "Error in 3PL warehouse invoice";
}
if(aa.equals("75")){
         return "3PL Warehouse invoiced";
}
if(aa.equals("76")){
         return "EEInvoicedAndSent";
}
if(aa.equals("59")){
         return "Verified";
}
if(aa.equals("59")){
         return "Verified";
}
if(aa.equals("80")){
         return "InProduction";
}
if(aa.equals("90")){
         return "LoadApproved";
}
if(aa.equals("91")){
         return "IncorrectItem";
}
if(aa.equals("92")){
         return "IncorrectPrices";
}
if(aa.equals("93")){
         return "NotGive";
}
if(aa.equals("94")){
         return "Samples";
}
if(aa.equals("95")){
         return "Realization";
}
	return aa;
	  
 }
 

  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();

	 int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok , jou.payment as pay " +
	   "    FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid   " + 
	   "    WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid ='ct'  AND jou.dataareaid = 'ct'   " +
	   "    and tra.qty !=0 and tra.itemid != '' " +
	   "    GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate, jou.payment ,tra.qty " +
	    "   ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate , jou.payment,tra.qty" ;

	   

	      cmd = " SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok  , jou.payment as pay, max (co.amountmst) as tasu " +
		     " FROM custinvoicetrans  tra with(nolock) INNER JOIN custinvoicejour jou with(nolock) ON   tra.invoiceid=jou.invoiceid and jou.dataareaid =tra.dataareaid    " +
			 " left JOIN custtrans a with(nolock) ON jou.invoiceid =a.invoice and a.dataareaid=jou.dataareaid LEFT JOIN  custtransopen co with(nolock) ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid " +
			 "  WHERE  jou.invoicedate > getdate()-200 AND tra.dataareaid ='ct'  AND jou.dataareaid = 'ct'   " +
		     "  and tra.qty !=0 and tra.itemid != '' " +
		     "  GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,jou.payment,tra.qty " + 
		     "  ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,jou.payment,tra.qty";
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
	     	        String pay = rs.getString("pay");
	     	        double tasu = 0;
	     	        
	     	        try{
	     	        	tasu = rs.getDouble("tasu");
	     	        }catch(Exception w){
	     	        	
	     	        }
	     	      
	     	        
	     	        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0 ,lastdate,hind,0,0," ");
	        //cp.setAbiKood(bill);
	     	       if(pay != null && pay.trim().equals("90D") && tasu != 0){
	     	          // cp.setRedline("90");
	     	           cp.setAbiKood("|t");
	     	        //   System.out.println("sisse sahhh");
	     	           
	     	       }
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
	  
  String cmd =" select eagentaccountid as agent , itemsubgroup as grupp , salesamount as planx,"
  		+ " focussubgroup ,itemgroupid "
			  + " from ASTSalesAgentForecast where year(transdate)=year(getdate())"
			   + " and  month(transdate) = month(getdate()) and dataareaid = 'ct' ";

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
					               String abigrupp = rs.getString("itemgroupid");
					               if(abigrupp == null){
					            	   abigrupp = "";
					               }else if(abigrupp.trim().length() > 0){
					            	   abigrupp = "|"+ abigrupp.trim();
					               }else{
					            	   abigrupp = "";   
					               }
					               
					               if(focussubgroup == 1){
					            	   res.put(agentID+"|"+grupp+abigrupp,plan);
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
    

      String	 cmd ="   SELECT  cus.SPIEAGENTACCOUNTID as agent,  " +
     "  round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate()) then lineamount else 0  end),0) AS summa, " +
	 "	 round(SUM(case when MONTH(sal.invoicedate) = MONTH(getdate()) and YEAR(sal.invoicedate) = YEAR(getdate())-1 then lineamount else 0  end),0) AS summalast " +  
     "    FROM custinvoicetrans sal with(nolock) INNER JOIN inventtable inv with(nolock) ON sal.itemid=inv.itemid  and  inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with(nolock) ON  " +
     "    sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus with(nolock) ON cus.accountnum = jou.orderaccount " +
	 "	 WHERE inv.dataareaid='ct' AND sal.dataareaid='ct' AND cus.dataareaid='ct'  AND jou.dataareaid='ct' AND sal.invoicedate > getdate()-400 " +
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
                    agent.setMonthSales((int)sales);
                         		
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
    return "AxaAmberUK";
    
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  

	 public static Object readObject(String s) throws IOException,
ClassNotFoundException, IOException {
Object obj = null;
File f = new File(s);
if (f.exists()){
ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
obj = ois.readObject();
ois.close();
}
return obj;
}
	 
	 private Vector signatures = new Vector();

private void loadSignatures(){
		
	  try{		
	  		File f = new File("orders.vec");
	  		Calendar ss = Calendar.getInstance();
	  		
	  		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 360000000) {// kui on yle tunni vana siis maha 
	  			f.delete();
	  			signatures = new Vector();
	  		} else {
	  			signatures = (Vector) readObject("orders.vec");
	  		}

	  		
	  	} catch (Exception e) {
	  		signatures = new Vector();
	  	}

	  	}
  
  public String getPilotDayData(Agent agent){
	  
	  Properties props = new Properties();
			try {
				props = EksportFrame.loadProps("emailown.txt");
				loadAgentsandEmail();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	  
	  String emailto = null;
	  if(props.get(agent.getID()) != null) {
		  emailto = props.get(agent.getID()) + "";
	  }
	  
	  System.out.println("Agent ja email " + agent.getID() + " " +  props.get(agent.getID()) );
	 
	 if(emailto != null && emailto.trim().length() > 4) {
		
		 loadSignatures();
	 
  String  sql2 ="  select sal.salesid as doknum, sal.custaccount as klient , sal.salesname as klnimi, deliverydate as kuupaev ,"
  		+ " spinotes as comment , cus.subsegmentid as chain ,deliveryaddress as aadr" + 
			 " from salestable sal INNER JOIN custtable cus with(nolock) ON sal.custaccount=cus.accountnum and "
			 + " sal.dataareaid = cus.dataareaid  where sal.dataareaid='ct'  AND cus.SPIEAGENTACCOUNTID ='" + agent.getID()+ "' and " + 
			 " sal.createddatetime > getdate() - 2 ";
			     

	    StringBuffer sbx = new StringBuffer();

	      try {
	    	          Connection con = this.getNewConnection();
	    		      Statement stmt = con.createStatement();
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                 
	                  while (rs2.next()) {
	                	  
	                	  String doknr = rs2.getString("doknum").trim();
	                	  String klient = rs2.getString("klient").trim();
	                	  String custn = rs2.getString("klnimi").trim();
	                	  String kuup = rs2.getString("kuupaev").substring(0,10);
	                	  String comment = rs2.getString("comment");
	                	  String chain = rs2.getString("chain");
	                	  String aadr = rs2.getString("aadr");
	                	  
	                	  
	                	  if(comment == null) {
	                		  comment = "";
	                	     } else {
	                		  comment = comment.trim();
	                	   }
	                			  
	                	 
	                	  
	                	  if(chain == null) {
	                		  chain = "";
	                	       } else {
	                	    	 chain = chain.trim();
	                	     }
	                	  
	                	  
	                	  
	                	  String signature = doknr;
	                	  if(signatures == null) {
	                		  signatures  = new Vector();
	                	  }
	                	//  System.out.println("Sees 000 " + signatures.contains(signature) + " '" + emailto + "' " + agent.getID());
	                	  
	                	  if(!signatures.contains(signature)){
	        			  Email mail = new Email(null);
	        		  		    mail.setSMTP(parent.getProps().getProperty("smtphost"));
	        		            mail.setUser(parent.getProps().getProperty("smtpuser"));
	        		            mail.setPass(parent.getProps().getProperty("smtppass"));
	        		            mail.setFilesTo(emailto);
	        		  	  		mail.setFrom(parent.getProps().getProperty("from"));
	        		  	  		mail.setSubjectFilesTo("Order DIRECT " + klient);
	        		  	  		mail.setEncoding("utf-8");
	        		  	 
	                	  
	                	  //läheme seda teadet saatma 
	                	
	                	  
	                		int i = 0;
		          			int j = 0;
		          			nf.setGroupingUsed(false);
		          			nf2.setMaximumFractionDigits(4);
		          			nf2.setMinimumFractionDigits(2);
		          			nf2.setGroupingUsed(false);
		          			double total = 0;
		          			String custname = custn;

		          			String comp = custn;
		          			if (comp == null) {
		          				comp = "";
		          			}
		          			
		          			String axat = "IMPORTED to AXAPTA";
                            StringBuffer sb = new StringBuffer();
		          			String subtext = (axat +  " Cellar Trends Ltd. <br>Stonehouse Farm/Ashby Rd, Swadlincote DE11 7BP, United Kingdom<br>").trim();
		          			String regno = (" No. 3705002<br> Manager : " + agent.getID());

		          			      subtext = subtext + " " + regno;
		          			
		          			sb.append("<font face=\"Arial\" size=\"2\" color=\"#E42217\"> Deliverydate "
		          					+ kuup + "</font><br> ") ;
		          			sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b></b> " + subtext + "</font>") ;
		          			sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;</font><br> ") ;
		          	        sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\"><b>Customer:</b> " + custn +"<br>" + aadr +"<br>" +  "</font> ") ;
		          			sb.append("<br><font face=\"Arial\" size=\"2\" color=\"#000000\">&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;" + "</font><br><br>") ;
		          															sb.append("</font>");
		          			
		          			sb.append("<font face=\"Arial\" size=\"4\"  color=\"#382D2C\"><b>Order Confirmation " + axat + "</b><br><br>" );
		          															
		          			sb.append("	<style type=\"text/css\">");
		          			sb.append("table.tb-table {");
		          			sb.append("border: 1px inset #98bf21; font-family: Arial, Helvetica, sans-serif");
		          			sb.append("font-size: 12px;} ");
		          			sb.append(".tb-table td {");
		          			sb.append("	padding: 4px;");
		          			sb.append("	margin: 3px;");
		          			sb.append("		border: 1px inset #FF3399;}");
		          			sb.append(".tb-table th {");
		          			sb.append("	background-color: #FFF; ");
		          			sb.append("color: #00000;");
		          			sb.append("		font-weight: bold;}");
		          			sb.append("</style><table class=\"tb-table\">" );
		          			sb.append("<tr><th>Nr.</th><th>Code</th><th>Description</th><th>Unit</th><th>Amount</th><th>Price £</th><th>LineSum £</th>"
		          					+ "</tr>" );

		          			
	                	  
		          		  sql2 = " select linenum, itemid , name, salesqty, lineamount as price, salesunit as unit"
		  	                		+ " from salesline where  dataareaid = 'ct' and  salesid ='" + doknr +"'" ;   
	                	  
	        		  	  Connection co = this.getNewConnection();
	  	    		      Statement  stm = co.createStatement();
	  	                  ResultSet	 rs = stm.executeQuery(sql2);
	  	              
	        		  	  double totalKokku = 0;
	                	  double totalcs = 0;
	        		  	  while (rs.next()) {
	        		  		//System.out.println("Sees 2"); 
	        		  		int linenum = rs.getInt("linenum");
	        		  	    String kood = rs.getString("itemid").trim();
	        		  		String tnimi =  rs.getString("name").trim();
	          	            int kogus = rs.getInt("salesqty");
	          	             
	          	            double linesum = round(rs.getDouble("price"), 2);
	          	            		double hind = round(linesum/kogus,2);
	          	         	String unit  =  rs.getString("unit").trim();

	          	            
	          	            double kokku = round(hind, 2);
	          	            totalKokku = round(totalKokku + linesum, 2);
	          	            totalcs = totalcs + kogus;
	          							
	          					        
	          					sb.append("<tr><td>" + (linenum) + "." + "" + 
	          								"</td><td>" + kood + 
	          								"</td><td>" + tnimi + 
	          								"</td><td>" + unit + 
	          								"</td><td>" + kogus + 
	          								"</td><td>" + nf.format(hind)  + 
	          								"</td><td>" + nf.format(linesum) + 
	          								"</td></tr>" );
	          				
	          					j++;
	          				          	            
	          	            


	          	           }
	                	  
	                	  rs.close();
	    	              stm.close();
	    	              co.close();

	    	        		
	          				sb.append("<tr><th></th><th>" +
	          						
	          						
	          					 
	          						"</th><td>Total" +
	          						"</td><td></td><td>" + nf.format(totalcs) +
	          							"</td><td></td><td>" + totalKokku + "£</td></tr>" );
	          				
	          				sb.append("<tr><th></th><th>" +
	          						"</th><th>" + 
	          						"</th><th>" + 
	          						
	          						"</th><th>" + 
	          						"</th><th>" +  
	          						"</th></tr>" );

	    	              
	    	            mail.setBodyHtml(sb.toString());
	    	          	mail.setSendNotice(false);
	    	          	try {
	    	          		mail.send();
	    	          		System.out.println("Email was sent " + emailto + " " + klient);
	    	          	}catch(Exception e) {
	    	          		
	    	          	}
    		  	  		
    		  	  		
    		  		  
	                /*	  if(this.webAgendid.get(klient)!= null) {
	                 		 ArrayList<String> arr = (ArrayList) webAgendid.get(klient);
	                 		 for (String agg : arr) {
	                 			 emailto = null;
	                 			 
	                 			 if(!agg.equals(agent.getID())) { // kui juba korra me saatsime siis pole vaja
	                    	    	  if(props.get(agg) != null) {
	                    	    		  emailto = props.get(agg) + "";
	                    	    	  } 
	                    	    	  System.out.println(klient + " " + arr.toString());
	                    	    	if(emailto != null && emailto.trim().length() > 4) {
	                    	    		 mail.setFilesTo(emailto);
	                        		 }
	                 			 }
	                 		 }
	                 	 }*/
    		  	  		
    		  	  		
    		  	  		
    		  	  		
    		  	  		System.out.println("\nOrder Comment " +  doknr + " " + klient + "\n\n" +
    		  	  				  "\n >>>>>" + comment + "<<<<<");
	                	  }
	                
	                	  
	                	  signatures.add(signature);
    	   		
	                  }

	              rs2.close();
	              stmt.close();
	              con.close();

	        


	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }
	      try {
				saveObject(signatures, "orders.vec");
			} catch (IOException e) {
				e.printStackTrace();
			} 
	 }
//System.exit(0);
	      return null;
	  }



  public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}

  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

 
  
  
  
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
  private   Vector debt = new Vector();
  private Statement stmt;
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
