package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
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
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class S1XMLRadexDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public S1XMLRadexDBDriver(EksportPanel parent) {
	super(parent);
	this.subjectFilesTo = parent.getString("filemsg");

	     user =  getParent().getProps().getProperty("rest.user");
	     pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	     }
  
  
	  String user =  null;
	  String pass = null;
	  String rcompany = null;
  private Hashtable dates = new Hashtable();
   public String getPilotDayData(Agent agent){
     return null;
   }
   
  private  Map codeMap = new HashMap();
   
   public String getSaveDebtInfo() {
	 

		StringBuffer products = new StringBuffer();
		try {

          TreeMap custs = this.getCustomers(null);

          Iterator itera =  custs.values().iterator();
			while (itera.hasNext()) {
           Customer cc = (Customer) itera.next();
				String kood = cc.getID();
				double volg = cc.getDebt();
				boolean blokk1 = cc.isSalesBlockT();
				boolean blokk2 = cc.isSalesBlock();
				String bl = "0";
				
				if(blokk2 ) {
					bl = "1";
				}
				
				if(blokk1 ) {
					bl = "2";
				}
				
				if (volg > 0 || !bl.equals("0")) {
					products.append(kood + ";"
							+ getParent().getNF().format(volg) + ";" + bl
							+ "\n");
				}
			}
			
			
			

		
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		this.connectionDown();
		return products.toString();
	  
   }
   
   private Hashtable readPlans(){
		Hashtable hh = new Hashtable();
		  File f = new File(this.getExcelDir() + "MeatPlan.xlsx");
		  try {
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				
				int currMont = Calendar.getInstance().get(Calendar.MONTH)+1;
				int currYear = Calendar.getInstance().get(Calendar.YEAR);
				String sh = currYear + "" + currMont;
				
				if((currMont+"").length() == 1) {
					sh = currYear + "0" + currMont;
				  }
				
				 DataFormatter formatter = new DataFormatter();
					
				
				XSSFSheet sheet = workbook.getSheet(sh);
				XSSFRow row = null;
				for (int i = 1; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String agentID = formatter.formatCellValue(row.getCell(0));
					String meatGroup = formatter.formatCellValue(row.getCell(3));
					
					String sum = "";
					String sumkg = "";

					    try {sum = (row.getCell((short) 5).getStringCellValue());
						} catch (Exception e) {
							try { sum = (int) (row.getCell((short) 5).getNumericCellValue()) + "";	
						} catch (Exception u) {}}
						
						try {sumkg = (row.getCell((short) 4).getStringCellValue());
						} catch (Exception e) {
							try { sumkg = (int) (row.getCell((short) 4).getNumericCellValue()) + "";	
						} catch (Exception u) {}}
						
				
						
						if(agentID != null && agentID.trim().length() > 0){
						
							if(meatGroup != null && meatGroup.trim().length() > 0){
							//	System.out.println(agentID + " " + meatGroup + " " + sum + " " + sumkg );
								Toode tt = new Toode(agentID);
								tt.setGr1(meatGroup);
								
								if(sum != null && sum.length() > 0) {
									tt.setPlancur(Double.parseDouble(sum));
								}
								if(sumkg != null && sumkg.length() > 0) {
									tt.setPlanvol(Double.parseDouble(sumkg));
								}
							if(tt.getPlanvol() > 0 || tt.getPlancur() > 0 ) {
								ArrayList arr = new ArrayList();
								if(hh.get(agentID) != null) {
									arr = (ArrayList) hh.get(agentID);
								}
								  arr.add(tt);
				                    hh.put(agentID, arr);
							}
							    
				                    
				                    
							}}}
		  
		  } catch (Exception d) {
				// System.out.println(row.getCell((short)a).getNumericCellValue());
			}
		//  System.out.println(" plans " + hh.toString());
		return hh;
	}
   
   private Vector prlists = null;
   
   private HashMap plistGUID() {
	   HashMap hm = new HashMap();
	   
	 	try {
	 		  File f = new File (getDir() + "Prices.xml");
	 	     
	 	       // System.out.println(getDir() + "Agents.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "PricesList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	 if(rida.getNodeName().equals("Product")){
	         		 
	         	Node pricesNode = getNode(rida, "Prices"); 
	         	for (int j = 0; j < pricesNode.getChildNodes().getLength(); j++){
	         		Node pricetypeNode = pricesNode.getChildNodes().item(j);
	         		if(pricetypeNode.getNodeName().equals("PriceType")) {
	         			String priceTypeGUID   = getNode(pricetypeNode,"PriceTypeGUID").getFirstChild().getNodeValue();
	         			String priceTypeCode   = getNode(pricetypeNode,"PriceTypeCode").getFirstChild().getNodeValue();
	         			hm.put(priceTypeGUID, priceTypeCode);
	         		}
	         	}
	         	  
	         	  
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   
	   
	   return hm;
   }
   
   
   public Vector getPriceMatrixData(){
	   
	   Vector result = new Vector();
	   
	 	try {
	 		  File f = new File (getDir() + "Prices.xml");
	 	     
	 	       // System.out.println(getDir() + "Agents.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "PricesList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	 if(rida.getNodeName().equals("Product")){
	         		 
	         		String prodCode = null;
	         	  try{
	         		 prodCode = getNode(rida,"ProductCode").getFirstChild().getNodeValue();
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         	  
	         	Node pricesNode = getNode(rida, "Prices"); 
	         	for (int j = 0; j < pricesNode.getChildNodes().getLength(); j++){
	         		Node pricetypeNode = pricesNode.getChildNodes().item(j);
	         		if(pricetypeNode.getNodeName().equals("PriceType")) {
	         			String priceTypeGUID   = getNode(pricetypeNode,"PriceTypeGUID").getFirstChild().getNodeValue();
	         			String priceTypeCode   = getNode(pricetypeNode,"PriceTypeCode").getFirstChild().getNodeValue();
	         			Double price   = Double.parseDouble(getNode(pricetypeNode,"Price").getFirstChild().getNodeValue());
	         			//System.out.println(prodCode + " - " + priceTypeGUID + " - " + priceTypeCode + " - " + price);
	         			MatrixLine ml = new MatrixLine();
	         			ml.setCliGroup(priceTypeCode);
	         			ml.setProdID(prodCode);
	         			ml.setPrice(price);
	         			result.add(ml);
	         		}
	         	}
	         	  
	         	  
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	 	
	 	
	 	
//System.exit(0);
	   
	   
	   return result;
   }
       
       
       
   public HashMap getPriceMatrixDataCreate(){
	   HashMap culist = new HashMap();
        prlists = new Vector();
        HashSet check = new HashSet();
	         try {
	          
	        	 FileReader fr = new FileReader(getDir() + "PriceMatrix.csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(";"," | ");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
	            	//System.out.println( line );
//  00012;;00012;0019D;;;7.6;;
                     st.nextToken();
                     st.nextToken();
                     
                     String client = st.nextToken().trim();
                     String prodID = st.nextToken().trim();
                     st.nextToken();
                     st.nextToken();
                     double price = Double.parseDouble(st.nextToken().trim());
                     String sdate =  st.nextToken().trim();
   	                 String edate = st.nextToken().trim();
   	                 String act = st.nextToken().trim();
   	                 String code = st.nextToken().trim();
   	                 if(code.equals("0")) {
   	                	 code = "0000";
   	                 }
   	            
   	             
   	             MatrixLine mline = new MatrixLine();
	             if (code !=null && code.trim().length() > 0) {
	               mline.setCliGroup(Converter.cleanLitLatEst(code.trim()));
	             }
	             
	             if(act.equals("Akc")) {
	           	 if(sdate != null  && !sdate.startsWith("1753") && sdate.trim().length() > 0 ){
	            	 mline.setStartDate(sdate.substring(0,8));
	             }
	             if(edate != null && !edate.startsWith("1753") && sdate.trim().length() > 0 ){
	            	 mline.setEndDate(edate.substring(0,8));
	             }
	             } 
	             
	             if (prodID != null && prodID.length() > 0) {
	               mline.setProdID(prodID);
	             }
	         
	             if (price != 0) {
	                 mline.setPrice(price);
                 }
	             
	             
	             
	             String key = code + "." + prodID + "." + price;
	             
            	 if(culist.get(client) != null) {
            		 
            		String olemas = (String)culist.get(client);
            		if(!olemas.contains(code) ) {
            			culist.put(client,culist.get(client) + "|" + code);
            		}
            		 
            	  } else {
            		 culist.put(client, code);
            	 }
            	
	             if(mline.getPrice() > 0 && !check.contains(key)){
	            	
	            	 prlists.add(mline); 
	            	 check.add(key);
	               }
	              }
	            }
	            
	            reader.close();
	            fr.close();
	         }catch(Exception e){
	         e.printStackTrace();
	         }
		System.out.println("culist " + culist.size());
		System.out.println("prlist " + prlists.size());
	       return culist;
	      }


   
   public Hashtable readCustSales(){
	   
	   Hashtable result = new Hashtable();
	   agGrSales = new Hashtable();
	   this.getCustomers(null);
	         try {
	        	Calendar cal =  Calendar.getInstance();
	        	 String date = Converter.getYYYYMMString(cal.getTime());
	          
	        	 FileReader fr = new FileReader(getDir() + "CustomerSales" + date + ".csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(";"," | ");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
  
                     String custid = st.nextToken().trim();
                     String group = st.nextToken().trim();
                     double saleskg = 0;
                     try {
                    	 saleskg = Double.parseDouble(st.nextToken().trim()); 
                     }catch(Exception e){
                    	 
                     }
                    
                     double saleseur = 0;
                     try {
                    	 saleseur = Double.parseDouble(st.nextToken().trim()); 
                     }catch(Exception e){
                    	 
                     }
                     Toode tt = new Toode(custid);
                     tt.setGr1(group);
                     tt.setDonecur(saleseur);
                     tt.setDonevol(saleskg);
                     ArrayList arr = new ArrayList();
                     if(result.get(custid) != null) {
                    	 arr = (ArrayList)result.get(custid);
                     }
                     arr.add(tt);
                     result.put(custid, arr);
                    
   	               
                    if (custAgents.get(custid) != null){
                    	ArrayList arrag =  (ArrayList)custAgents.get(custid) ;
                    	for(int u = 0; u< arrag.size(); u++ ) {
                    		String agg = (String)arrag.get(u);
                    		
                    		TreeMap tree = new TreeMap();
                        	if(agGrSales.get(agg) != null) {
                        		tree = (TreeMap)agGrSales.get(agg);
                        	}
                        	Toode dd = new Toode(agg);
                             if(tree.get(group) != null) {
                            	 dd = (Toode)tree.get(group) ;
                             }
                        	dd.addDonecur(saleseur);
                        	dd.addDonevol(saleskg);
                        	tree.put(group, dd);
                        	agGrSales.put(agg, tree);
                    	}
                      }
	       
	             }
	            }
	            
	            reader.close();
	            fr.close();
	         }catch(Exception e){
	         e.printStackTrace();
	         }
		
	        // System.out.println(result.toString());
	    return result;
	      }

   Hashtable agGrSales = null;
   Hashtable custAgents = null;


   public ManagerData getManagerData() throws SQLException{
	   return null;
   }
   public String getProductSortingData(){
   	return null;
   }

 
   public DealerProduct getDealerProductsList() {
       return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   
   
   
   
   
   HashMap stocks = null;



	  public String getSaveStockInfo(){
	    	   stocks = new HashMap();
	  	         StringBuffer products = new StringBuffer();
	         
	         try {
	             FileReader fr = new FileReader(getDir() + "Stocks.csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(";"," |");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
	            	   String kood = st.nextToken().trim();
	            	   if(kood.length() > 0) {
	            		try {
	            			   double seis = Double.parseDouble(st.nextToken());
	      	  	             if(seis > 0){
	      	  	            	 stocks.put(kood, seis+ "");
	      	  	                 products.append(kood +  ";");
	      	  	                 products.append(" ;");
	      	  	                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
	      	  	               }		
	            		}catch(Exception e) {
	            		}
	  	             }
	              }
	           }
	             reader.close();
	             fr.close();
	             //System.out.println(tm);
	           } catch (Exception e){
	             e.printStackTrace();
	           }
	         
	        
	         
	         
	         
	  	   boolean templatee = true;
	  	   //faili avamine ja kuupaevaga kontrollimine
	  	   	File template = new File(this.getExcelDir() + "cashtemplate.xlsx");

	  	   	Calendar today = Calendar.getInstance();
	  		  File direct = new File(getParent().getProps().getProperty("cash.read")+"exceloutput/");
	  		    
	  		  //  System.out.println("vvv "+ driver.getImportFile());
	  		    File[] filess = direct.listFiles();
	  		    for (int i = 0; i < filess.length ; i++){
	  		      File filee = filess[i];
	  		      
	  		      if (filee.isFile()){
	  		    	  if(filee.getName().contains(Converter.getYYYY_MM_DDpString(today))) {		//kui on vana fail tanase kuupaevaga olemas 
	  		    		  template = new File(getParent().getProps().getProperty("cash.read") + "exceloutput/" + filee.getName());
	  		    		  templatee = false;
	  		    	  }
	  		    	  }
	  		      }
	  	      
	  	    
	  	      Workbook workbook = null;
	  			 Sheet sheet = null;
	  						try{
	  							 
	  							       FileInputStream	 ff = new FileInputStream(template);
	  							       if(template.getName().endsWith(".xlsx")) {
	  							    	   workbook = new XSSFWorkbook(ff); 
	  										 sheet = workbook.getSheetAt(0); 
	  							       }else {
	  							    	    workbook = new HSSFWorkbook(ff); 
	  										 sheet = workbook.getSheetAt(0); 
	  									
	  							       }
	  									ff.close();	
	  						}catch(Exception e){
	  				    		  e.printStackTrace();
	  				    	  }
	  	   
	  	   
	  	   // Siin teeme Cashi ära.
	  	  
	  		  File dir = new File(getParent().getProps().getProperty("cash.read"));
	  		    
	  		  //  System.out.println("vvv "+ driver.getImportFile());
	  		    File[] files = dir.listFiles();
	  		  this.getCustomers(null);
	  		
	  		  for (int i = 0; i < files.length ; i++){
	  		      File file = files[i];
	  		      if (file.isFile()){
	  		    	  try{
	  		    		
	  		    	  FileReader fr = new FileReader(file);
	  					BufferedReader br = new BufferedReader(fr);
	  					
	  					String line = br.readLine();
	  						line = br.readLine().replaceAll(";;", "; ;");
	  				        
	  						StringTokenizer st = new StringTokenizer(line, ";");
	  						if (st.hasMoreElements()){
	  							//CUS_REALID	SUMA	KVID	USRREALID	ANSPOSTED	ST	VAL
	  							//10002654	300.0	abc=123	1046	25.09.2015	VO	EUR
	  	      String cuid = st.nextToken();

	  	      double summa = Double.parseDouble(st.nextToken());
	  	      String docid = st.nextToken();
	  	      String user = st.nextToken();
	  	      String date = st.nextToken();
	  	      date = date.replace('.', '-').replace('.', '-');
	  	
	  	      
	  	      if(sheet != null){
	  	    	  int lastrow = sheet.getLastRowNum();
	  	    	  if(templatee) {
	  	    		  lastrow = 2;
	  	    		  templatee=false;
	  	    	  }
	  	    	  sheet.createRow(lastrow+1);
	  	    	  sheet.getRow(lastrow+1).createCell(0);
	  	    	  sheet.getRow(lastrow+1).getCell(0).setCellValue("PINGAV");
	  	    	  sheet.getRow(lastrow+1).createCell(1);
	  	    	  sheet.getRow(lastrow+1).getCell(1).setCellValue("S");
	  	    	  sheet.getRow(lastrow+1).createCell(2);
	  	    	  sheet.getRow(lastrow+1).getCell(2).setCellValue("10000");
	  	    	  sheet.getRow(lastrow+1).createCell(3);
	  	    	  sheet.getRow(lastrow+1).getCell(3).setCellValue("Pirkėjas");
	  	    	  sheet.getRow(lastrow+1).createCell(4);
	  	    	  sheet.getRow(lastrow+1).getCell(4).setCellValue(cuid);
	  	    	  sheet.getRow(lastrow+1).createCell(5);
	  	    	  sheet.getRow(lastrow+1).getCell(5).setCellValue(date);
	  	    	  
	  	    	  sheet.getRow(lastrow+1).createCell(6);
	  	    	  sheet.getRow(lastrow+1).getCell(6).setCellValue("Mokėjimas");
	  	    	  sheet.getRow(lastrow+1).createCell(7);
	  	    	  sheet.getRow(lastrow+1).getCell(7).setCellValue(docid);
	  	    	  sheet.getRow(lastrow+1).createCell(8);
	  	    	  sheet.getRow(lastrow+1).getCell(8).setCellValue(codeMap.get(user).toString());
	  	    	  sheet.getRow(lastrow+1).createCell(9);
	  	    	  sheet.getRow(lastrow+1).getCell(9).setCellValue(-summa);
	  	    	  sheet.getRow(lastrow+1).createCell(10);
	  	    	  sheet.getRow(lastrow+1).getCell(10).setCellValue("PIRKL SKOL");
	  	    	  sheet.getRow(lastrow+1).createCell(11);
	  	    	  sheet.getRow(lastrow+1).getCell(11).setCellValue("Banko sąskaita");
	  	    	  sheet.getRow(lastrow+1).createCell(12);
	  	    	  sheet.getRow(lastrow+1).getCell(12).setCellValue(date);
	  	    	  sheet.getRow(lastrow+1).createCell(13);
	  	    	  sheet.getRow(lastrow+1).getCell(13).setCellValue("COMM");
	  	               }
	  						}
	  						
	  						
	  					br.close();
	  					fr.close();
	  					  
	  		    	  }catch(Exception e){
	  		    		  e.printStackTrace();
	  		    	  }
	  		    	  
	  		    	  }
	  		      File dest = new File(getParent().getProps().getProperty("cash.read") + "done/" + file.getName());
	  				if(dest.exists()){
	  					dest.delete();
	  				}
	  				if(file.isFile()) {
	  				     boolean rn =  file.renameTo(dest);
	  				     System.out.println("rename " + file.getName() + " " + rn);
	  				}
	  		
	  		    }
	  		    
	  		    try {
	  		    	if(template.getName().contains(Converter.getYYYY_MM_DDpString(today))) {
	  		    		FileOutputStream out = new FileOutputStream(new File(getParent().getProps().getProperty("cash.read") + "exceloutput/" + template.getName()));
	  		    		workbook.write(out);
	  					out.close();
	  		    	}else {
	  		    		FileOutputStream out = new FileOutputStream(new File(getParent().getProps().getProperty("cash.read") + "exceloutput/" + Converter.getYYYY_MM_DDpString(today) + "-cash.xlsx"));
	  		    		workbook.write(out);
	  					out.close();
	  		    	}
	  		    	
	  		    			
	  		    }catch(Exception e){
	  	    		  e.printStackTrace();
	  	    	  }
	  		    
	  		   // System.exit(0);

	  		  return products.toString();
	     }




	  private int getDue(long dd){
	      int res = 0;
	      res =(int) ((-dd+(new java.util.Date()).getTime())/86400000) ;
	   //   System.out.println("res " + res + " dd" + dd + " " +(( -dd+(new java.util.Date()).getTime())/86400000));
	      return res ;
	   }


   public Vector getDebt(String agentCode){

	   Vector list = new Vector();
	   
	 	try {
	 		  File f = new File (getDir() + "DebtList.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "DebtsList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	  
		         	 if(rida.getNodeName().equals("Customer")){
		         		String custid = null;
		         		String custGUID = null;
		         	  try{
		         		 custGUID   = getNode(rida,"CustomerGUID").getFirstChild().getNodeValue().substring(0, 8);
		         		 custid = (String) custGUIDS.get(custGUID);
		         	  }catch(Exception e){e.printStackTrace();}
		         	  
		         	  
		         	 Node debtsNode = getNode(rida,"Debts");
		         	for (int j = 0; j < debtsNode.getChildNodes().getLength(); j++){
		         		
		         		Debt debt = new Debt();
		         		if(custid != null) {
			         		debt.setCustID(custid);
		         		 }
		         		
			         	  Node debtrida = debtsNode.getChildNodes().item(j);
			         	  if(debtrida.getNodeName().equals("Debt")){
			         		String debtDocNr   = getNode(debtrida,"DocumentNo").getFirstChild().getNodeValue();
			         		String debtDocDate   = getNode(debtrida,"DocDate").getFirstChild().getNodeValue();
			         		String debtPayDate   = getNode(debtrida,"PayDate").getFirstChild().getNodeValue();
			         		Double debtOpenAmount   = Double.parseDouble(getNode(debtrida,"OpenAmount").getFirstChild().getNodeValue());
			         		
			      	        debt.setDoknr(debtDocNr);
			      	        debt.setDuedate(getDate(debtPayDate));
			      	        debt.setSum(debtOpenAmount);
			      	        debt.setDate(getDate(debtDocDate));
			      	        debt.setDue(getDue(getDate(debtPayDate).getTime()) + "");
			      	        
			      	        
			         		String debtAddressGUID = null;
			         		try {
			         			debtAddressGUID   = getNode(debtrida,"AddressGUID").getFirstChild().getNodeValue();
			         		}catch(Exception e){}
		         			 if(debtAddressGUID != null) {
		         				 String addrid = null;
		         				 addrid = (String) custGUIDS.get(debtAddressGUID);
		         				if(addrid != null) {
		         					debt.setCustID(addrid);
		         					
		         					debt.setPayerID(custid);
		         					
		         				}
		         				
		         			 }
		         			list.addElement(debt);
			         		
			         		
			         	  
			         		//System.out.println(debtDocNr + " - " + debtDocDate + " - " + debtPayDate);
			         		
			         	  }
		         	}
		         	  
		         	
		         	 }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();}

	   
	   

	      
	      

	   
	   //System.exit(0);
	   return list;
  }


   public String getDealerCustomerCSV(String agent){
    return null;
    }

   public String  getCustProdDiscounts(Agent agent){
    return null;
    }

   public TreeMap getProdGroups() throws SQLException {

	          return prodGroups;
   }
   
   
   private TreeMap prodGroups = null;
   
   
   public TreeMap getProdGroupsSecond() throws SQLException {
	   TreeMap   tm = new TreeMap();
	   return tm;
   }

   
   
   
	  private Node getNode(Node rt, String s){
  	    return GenericDOM.getNode(rt, s);
  	  }
  	  

  	  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
  		FileInputStream in = new FileInputStream(xmlFile);
  		int bomKount = getBOMCount(xmlFile);
  		if (bomKount > 0) {
  		byte[] buf = new byte[bomKount];
  		in.read(buf);
  		}
  		return in;
  		}
  	private int getBOMCount(File xmlFile) throws FileNotFoundException,
	  IOException {
	  DataInputStream din = new DataInputStream(new FileInputStream(xmlFile));
	  int bomKount = 0;
	  byte b = din.readByte();
	  while (b < 0) {
	  bomKount++;
	  b = din.readByte();
	  }
	  din.close();
	//   System.out.println("Skipping BOM, " + bomKount + " bytes");
	  return bomKount;
	  } 

   private HashMap <String, String> agentGUID = null;
   
   public TreeMap getAgents(){
	   
	     agentGUID = new HashMap<String, String>(); //key=GUID, value=code
	     TreeMap tm = new TreeMap();
	     //
	     //getFixedAssortmentData();
	  //  makeData();
	     
	     // loene võimalikud agendid sisse 
	   
	 	try {
	 		  File f = new File (getDir() + "Agents.xml");
	 	     
	 	       // System.out.println(getDir() + "Agents.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "AgentsList"); 
	     	
	     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	 if(rida.getNodeName().equals("Agent")){
	         	  try{
	         		 String salesman   = getNode(rida,"GUID").getFirstChild().getNodeValue().substring(0, 8);
	         		 String salesmanN   = getNode(rida,"Name").getFirstChild().getNodeValue();
	         		 salesman = Converter.deCode(salesman);
	         		 Agent sm = new Agent (salesman);
	         		 sm.setName(salesmanN);
	                 tm.put(salesman, sm); 
	               //System.out.println(salesman + " " + sm);
	                 String id =  getNode(rida,"GUID").getFirstChild().getNodeValue();
	                 agentGUID.put(id, salesman);
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	     //this.getCustomers(null);
	     //this.getProducts(null);
	 	//this.getPriceMatrixData();
	     return tm;
	   }




       public String getDistribution() {
         return null;
       }
       
       
       private Hashtable dueSums = new Hashtable();
       private Hashtable usedCredit = new Hashtable();

       
       
       private Date getDate(String mmddyy){
    	  	long lon = 0;
    	  	Calendar cal = Calendar.getInstance();
    	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(6, 10)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(3, 4))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(0, 2)));
    	  // System.out.println(cal.getTime()); 
    	   Date date = new Date(lon);
    	   date.setTime(cal.getTimeInMillis());
    	    return date;
    		
    	  }
      
       
       private boolean isDueDate(String yyyymmdd){
    	  	
    	  	Calendar cal = Calendar.getInstance();
    	  	if( yyyymmdd.trim().length() == 0 ) {
    	  		return false;
    	  	 }
    	  
    	    cal.set(Calendar.YEAR, Integer.parseInt( yyyymmdd.substring(0, 4)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(yyyymmdd.substring(4, 6))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(yyyymmdd.substring(6, 8)));
    		Calendar ca = Calendar.getInstance();
    		ca.add(Calendar.DAY_OF_YEAR, -1);
    		
    		if(cal.before(ca)){
    			return true;
    		}else{
    			return false;
    		}
    		
    	  }

       private HashMap custGUIDS = null;
       
       
       public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     custAgents = new Hashtable();
	     custGUIDS = new HashMap();
	     HashMap prg = plistGUID();


    	 try {
	 		  File f = new File (getDir() + "Clients.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "CustomersList"); 
	         
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	    		 String CustGUID   = null;
	    		 String CustName   = null;		
	    		 String CustCode   = null;
	    		 String CustAgentGUID   = null;
	    		 String DAddressGUID   = null;
	    		 String DAddressCode   = null;
	    		 String DAddress   = null;
	    		 String DAddressAgentGUID   = null;
	    		 String DAddressContactName   = null;
	    		 String CustLegalAddres   = null;
	    		 String CustActualAddres   = null;
	    		 String CustPriceTypeGUID   = null;
	    		 String CustPaymentTerm   = null;
	    		 String CustCreditLimit   = null;
	    		 String CustPhone   = null;
	    		 String CustEmail   = null;
	    		 String CustBlocked = null;
	    		 String info = null;
	    		 
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	  if(rida.getNodeName().equals("Customer")){
	         		  try{
		         		 CustGUID   = getNode(rida,"CustomerGUID").getFirstChild().getNodeValue();
		         		 CustCode   = getNode(rida,"CustomerCode").getFirstChild().getNodeValue();
		         		 
		         		try{
			         		 CustName   = getNode(rida,"CustomerName").getFirstChild().getNodeValue();		
		         		}catch(Exception e){}
		         		 
		         		try{
		         			CustAgentGUID = getNode(rida,"MainAgentGUID").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		
		         		try{	
			         		 CustLegalAddres = getNode(rida,"LegalAddres").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		
		         		 try{
			         		 CustActualAddres = getNode(rida,"ActualAddres").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		 
		         		 try{
			         		 CustPriceTypeGUID = getNode(rida,"PriceTypeGUID").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		 
		         		try{
		         			CustPaymentTerm = getNode(rida,"PaymentTerm").getFirstChild().getNodeValue();
		         		}catch(Exception e){}

		         		try{
		         			CustCreditLimit = getNode(rida,"CreditLimit").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		
		         		try{
		         			CustPhone = getNode(rida,"Phone").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		
		         		try{
		         			CustEmail = getNode(rida,"Email").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		
		         		try{
		         			CustBlocked = getNode(rida,"Blocked").getFirstChild().getNodeValue();
		         		}catch(Exception e){}
		         		
		         		
		         		if(CustPhone != null) {
		         			info="Tel:" + CustPhone;
		         		}
		         		if(CustEmail != null) {
		         			if(info == null) {
		         				info = CustEmail;
		         			}else {
		         				info = info + "|" + CustEmail;
		         			}
		         		}
		         		
		         		 CustName = Converter.deCode(CustName);
		         		
		         		 //System.out.println(CustGUID + " " + CustName + " " + CustAgentGUID + " " + agentGUID.get(CustAgentGUID));
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         		 custGUIDS.put(CustGUID, CustCode + "_0");
	         		 
     		  if("false".equals(CustBlocked) || CustBlocked == null) {

	         			  
         		  	Node addressNode = getNode(rida, "DeliveryAddressList");
         		  	if(addressNode == null) {
         		  		
         		  		Customer customer = new Customer(CustCode + "_0" );
         		  		
         		  		if(CustName != null){
	      		         customer.setName(Converter.enCodeEst(CustName));
	      		         
	      		         if(CustActualAddres != null){
	      		        	customer.setAddress(Converter.enCodeEst(Converter.adressLink(CustActualAddres,"Latvia")));
	      		         }else {
	      		        	customer.setAddress(Converter.enCodeEst(Converter.adressLink(CustLegalAddres,"Latvia")));
	      		         }
	      		         
	      		       if(CustPriceTypeGUID != null){
	      		    	   String type = (String) prg.get(CustPriceTypeGUID);
	      		    	   if(type != null) {
	      		    		 customer.setGroup(type);
	      		    	   }
	      		        	
	      		         	}
		      		       if(CustPaymentTerm != null){
		      		        	customer.setPaymTerm(CustPaymentTerm);
		      		         }
		      		     if(CustCreditLimit != null && !CustCreditLimit.equals("0")){
		      		        	customer.setCredit(CustCreditLimit);
		      		         }
		      		     
		      		     if(info != null){
		      		    	customer.setPersonalData(info);
		      		     }
		      		       
		      		     
		      		     if(CustAgentGUID != null) {
		      		    	customer.addAgent(new Agent(agentGUID.get(CustAgentGUID)));
		      		    	tm.put(Converter.cleanLitLatEst(CustName + " "+ DAddressCode), customer);
		      		    	custGUIDS.put(CustGUID, customer.getID());
		      		     }
	      		         
         		  		
         		  	}
         		  		
         		  	}else {
         		  		for (int j = 0; j < addressNode.getChildNodes().getLength(); j++){
    	      				Node ridaAddress = addressNode.getChildNodes().item(j);
    	      				
    	      				if(ridaAddress.getNodeName().equals("DeliveryAddress")){
    	      					try{
    	      						DAddressGUID   = getNode(ridaAddress,"AddressGUID").getFirstChild().getNodeValue();
    			         		}catch(Exception e){}
    	      					
    	      					try{
    	      						DAddressCode   = getNode(ridaAddress,"AddressCode").getFirstChild().getNodeValue();
    			         		}catch(Exception e){}
    	      					
    	      					try{
    	      						DAddress   = getNode(ridaAddress,"Address").getFirstChild().getNodeValue();
    			         		}catch(Exception e){}
    	      					
    	      					try{
    	      						DAddressAgentGUID   = getNode(ridaAddress,"AgentGUID").getFirstChild().getNodeValue();
    			         		}catch(Exception e){}
    	      					
    	      					try{
    	      						DAddressContactName   = getNode(ridaAddress,"ContactName").getFirstChild().getNodeValue();
    			         		}catch(Exception e){}
    	      				
    	      				
    	      				
    	      				
    	      				
    	      				//System.out.println("Addressinfo: "+DAddressGUID + " " + DAddressCode + " " + DAddress + " " + DAddressAgentGUID);
    	      				
    	      		         Customer customer = new Customer(CustCode + "_" + DAddressCode);
    	      		         customer.setName(Converter.enCodeEst(CustName) + "_" + DAddressCode);
    	      		         customer.setAddress(Converter.enCodeEst(Converter.adressLink(DAddress,"Latvia")));
    	      		         
	    	      		       if(DAddressAgentGUID != null){
	    	      		    	 customer.addAgent(new Agent(agentGUID.get(DAddressAgentGUID)));
	   	      		         	}
	    	      		       if(CustAgentGUID != null){
	    	      		         customer.addAgent(new Agent(agentGUID.get(CustAgentGUID)));
	   	      		         	}
	    	      		       if(CustPriceTypeGUID != null){
	    	      		    	   String type = (String) prg.get(CustPriceTypeGUID);
	    	      		    	   if(type != null) {
	    	      		    		 customer.setGroup(type);
	    	      		    	   }
	   	      		        	
	   	      		         	}
		    	      		     if(CustPaymentTerm != null){
				      		        	customer.setPaymTerm(CustPaymentTerm);
				      		      }
		    	      		   if(CustCreditLimit != null && !CustCreditLimit.equals("0")){
			      		        	customer.setCredit(CustCreditLimit);
			      		         }
		    	      		   
		    	      		 if(info != null){
				      		    	if(DAddressContactName != null && DAddressContactName.trim().length() > 0){
				      		    		customer.setPersonalData(DAddressContactName + "|" + info);
				      		    	}else {
				      		    		customer.setPersonalData(info);
				      		    	}
			      		     }else if(DAddressContactName != null && DAddressContactName.trim().length() > 0){
			      		    	customer.setPersonalData(DAddressContactName);
			      		     }
		    	      		 
		    	      		 customer.setPayer(CustCode + "_0");
	    	      		   
    	      		         tm.put(Converter.cleanLitLatEst(CustName + " "+ DAddressCode), customer);
    	      		        custGUIDS.put(DAddressGUID, customer.getID());
    	      				}
    	      			}
         		  	}
	         	  }
	      			
			       }
	         	 
	         	}
	         
	         //customercode_addresscode
	         
	
             in.close();
             
    	 }catch(Exception k ){
				k.printStackTrace();
			}
    	 
    	 
    	 //System.out.println(tm.toString());
    	 
    	 //System.exit(0);
          
    	 
    	 
    	 
        
         return tm;
       }
       
       
       
       
       
       
       
       
   
   

private Hashtable prodNames = null;
private HashMap prodsGUID = new HashMap();

   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     

     
     //Productgroups.xml
     

     HashMap prgrGUID = new HashMap();
     //String ProdGroupsParentGrGUID=null;
		try {
			StringBuffer sbEAN = new StringBuffer();
			  File f = new File (getDir() + "ProductGroups.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
			FileInputStream in = getCleanInputStream(f);
			prodGroups = new TreeMap();
			String handler = null;
		    Document doc = db.parse(in,handler);
		    Node root = getNode(doc, "ProductGroupsList"); 
		     
		    for (int i = 0; i < root.getChildNodes().getLength(); i++){
		        String ProdGroupsGUID = null;
		        String ProdGroupsCode = null;
		        String ProdGroupsDescription = null;
		    	
		    	  Node rida = root.getChildNodes().item(i);
		    	  
		    	  if(rida.getNodeName().equals("Group")){
		    		  try{
		    			  ProdGroupsGUID   = getNode(rida,"GUID").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdGroupsCode   = getNode(rida,"GroupCode").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdGroupsDescription   = Converter.enCodeEst(getNode(rida,"Description").getFirstChild().getNodeValue());
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    			  
		    			  
		    			  
		    			  //rodGroupsParentGrGUID   = getNode(rida,"ParentGrGUID").getFirstChild().getNodeValue();
		    			  prodGroups.put(ProdGroupsCode, ProdGroupsDescription);
		    			  prgrGUID.put(ProdGroupsGUID, ProdGroupsCode);
		        		 //System.out.println("ProdGroups: " + ProdGroupsGUID + " " + ProdGroupsCode + " " + ProdGroupsDescription);
		        	  
		    	  	}
		    	  }
		    		  
		    in.close();
			}catch(Exception k ){
				k.printStackTrace();
			}
 
     
		//Product.xml

     
		
		try {
			StringBuffer sbEAN = new StringBuffer();
			  File f = new File (getDir() + "Product.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
			FileInputStream in = getCleanInputStream(f);
			String handler = null;
		    Document doc = db.parse(in,handler);
		    Node root = getNode(doc, "ProductsList"); 
		    
		    for (int i = 0; i < root.getChildNodes().getLength(); i++){
		        String ProdGUID = null;
			   	 String ProdCode = null;   
			   	 String ProdArticle = null;   
			   	 String ProdDesc = null;   
			   	 String ProdUnit = null;  
			   	 String GroupGUID = null;
			   	 String UnitPrice = null;  
			   	 String ProdBlocked = null; 
			   	 String inventory = null; 
		    	
		    	  Node rida = root.getChildNodes().item(i);
		    	  
		    	  //System.out.println(rida.getNodeName() + " " );
		    	  if(rida.getNodeName().equals("Product")){
		    		  try{
		        		  ProdGUID   = getNode(rida,"GUID").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdCode   = getNode(rida,"Code").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdArticle   = getNode(rida,"Article").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdDesc   = getNode(rida,"Description").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdUnit   = getNode(rida,"Unit").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  UnitPrice   = getNode(rida,"UnitPrice").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  inventory   = getNode(rida,"Inventory").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		    		  try{
		    			  ProdBlocked   = getNode(rida,"Blocked").getFirstChild().getNodeValue();
		    		  }catch(Exception e){e.printStackTrace();}
		    		  
		        		  
		        		  
		        		  
		        		  //GroupGUID2   = getNode(rida,"GroupSecGUID").getFirstChild().getNodeValue();
		        		  
		        		  
		        		  
		        		 //System.out.println(CustGUID + " " +CustName);
		        	  
		    		  
		    		  prodsGUID.put(ProdGUID, ProdCode);
		    		 
		    		  
		    		 Product prod = new Product(ProdCode);
		   			 prod.setStock(Double.parseDouble(inventory));
		   			 
		   			 //if(Double.parseDouble(incase) > 1) {
		   				 //prod.setInCases(Double.parseDouble(incase));
		   			 //}
		   			 
		   			 if(ProdUnit.length() > 0) {   
		   				 prod.setUnit(ProdUnit);
		   			 }
		   			 if(GroupGUID != null) {   
		   				prod.setGroup(prgrGUID.get(GroupGUID) + "");
		   			 }
		   			  
		   			
		   			 //if(groupcodesec.length() > 0) {
		   				 //prod.setGroup(GroupGUID2);
		   			 //}
		   			 
		   			 if(ProdDesc.length() > 0) {
		   				 prod.setName(Converter.enCodeEst(ProdDesc + " "+ ProdArticle));
		   			 }
		   			 
	   				 if(Double.parseDouble(UnitPrice) > 0) {
	   					 prod.setPrice(Double.parseDouble(UnitPrice));
	   				 }
	   				 
	   				//prod.setVat("21");

		    		
		    		
				  	Node EANNode = getNode(rida, "EANList");
				  	
				  	/*XML CHILD NODE DEBUGIMISEKS
				  	System.out.println("Number of child nodes: " + EANNode.getChildNodes().getLength());
				  	for (int j = 0; j < EANNode.getChildNodes().getLength(); j++) {
				  	    Node childNode = EANNode.getChildNodes().item(j);
				  	    System.out.println("Child node name: " + childNode.getNodeName());
				  	    if (childNode.getNodeName().equals("EAN")) {
				  	        String EANKood = childNode.getTextContent();
				  	        System.out.println("EANKOOD: " + EANKood);
				  	    }
				  	}*/
				  	
				  	//System.out.println("EANKOODtk: "+ EANNode.getChildNodes().getLength());
				  	
				  	
				  	int x=0; //ean counter
		 			for (int j = 0; j < EANNode.getChildNodes().getLength(); j++){
		 				Node ridaEAN = EANNode.getChildNodes().item(j);
		 				if(ridaEAN.getNodeName().equals("EAN")){
		 				String EANKood   = EANNode.getChildNodes().item(j).getTextContent();
		 				//System.out.println("EANKOOD: "+ EANNode.getChildNodes().item(j).getTextContent());
		 				
		 				if(x == 0 && EANKood.length() > 0) {
		   					 prod.setEAN(EANKood);
		   				 }
		 				if(x != 0 && EANKood.length() > 0) {
		 					sbEAN.append(ProdCode + ";" + EANKood + "\n"); //prodanalogsidesse
		   				 }
		 				x++;
		 				}
		 				
		 			  }
		 			if("false".equals(ProdBlocked)) {
		 				tm.put(Converter.cleanLitLatEst(ProdDesc + prod.getUnit() + prod.getID()), prod);
		 			}
					 

			       }
		   			if(sbEAN.length() > 0) {
			     		 Converter.saveFileInUTF8("prodanalogs.txt", sbEAN.toString());
			     	 }
		    	}
		    
		    in.close();
		}catch(Exception k ){
				k.printStackTrace();
			}
		return tm;

   }

   
   
   
   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();



	   
	 	try {
	 		  File f = new File (getDir() + "CustomerProducts.xml");

	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "CustomersProductsList"); 
	     	
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	 if(rida.getNodeName().equals("Customer")){
	         	  try{
	         		 String custProdGUID   = getNode(rida,"CustomerGUID").getFirstChild().getNodeValue();
	         		 //String custProdName   = getNode(rida,"CustomerName").getFirstChild().getNodeValue();

	         		Node addressListNode = getNode(rida,"AddressList");
	         		//System.out.println(addressListNode.toString());
	         		
		         		for (int j = 0; j < addressListNode.getChildNodes().getLength(); j++){
		         			Node addressNode = addressListNode.getChildNodes().item(j);
		         			if(addressNode.getNodeName().equals("Address")){
			         			//System.out.println(addressNode.toString());
		         				if(getNode(addressNode,"AddressGUID").getFirstChild() != null) {
				   	         		 String addressGUID   = getNode(addressNode,"AddressGUID").getFirstChild().getNodeValue();
				   	         		System.out.println(addressGUID);
				   	         		String custid = (String) custGUIDS.get(addressGUID);

				   	         	
				   	         	if(custid != null) {
					   	         	Node prodListNode = getNode(addressNode,"ProductsList");
		         					for (int k = 0; k < prodListNode.getChildNodes().getLength(); k++){
		         						Node prodNode = prodListNode.getChildNodes().item(k);
		         					
		         						if(prodNode.getNodeName().equals("Product")){
		         			         		 String prodGUID   = getNode(prodNode,"ProductGUID").getFirstChild().getNodeValue();
		         			         		 String prodLastDate   = getNode(prodNode,"LastDate").getFirstChild().getNodeValue().substring(0,5);
		         			         		 String prodInvNo   = getNode(prodNode,"InvoiceNo").getFirstChild().getNodeValue();
		         			         		 
		         			         		 Double prodQty = 0.0;
		         			         		try{
		         			         			prodQty  = Double.parseDouble(getNode(prodNode,"Qty").getFirstChild().getNodeValue());
		         			         		 }catch(Exception e){}
		         			         		 
		         			         		
		         			         		Double prodLastPrice = 0.0;
		         			         		try{
			         			         		 prodLastPrice   = Double.parseDouble(getNode(prodNode,"LastPrice").getFirstChild().getNodeValue());
		         			         		 }catch(Exception e){}
		         			         		 
		         			         		 //System.out.println(prodGUID + " " + prodLastDate);
		         			         		 String prodid = (String) prodsGUID.get(prodGUID);
		         			         		 
		         			         		 if(prodid != null && prodid.length() > 0){
		         			         			CustProd cp = new CustProd(custid, prodid, prodQty ,0 ,0,prodLastDate,prodLastPrice,0,0," ");
		         			         			 cp.setAbiKood(prodInvNo);
			         			         			custProds.add(cp);
		         			         		 }
		         						}
		         					}
				   	         	}
				   	         		
				   	         		
		         				}else {
		         					Node prodListNode = getNode(addressNode,"ProductsList");
		         					for (int k = 0; k < prodListNode.getChildNodes().getLength(); k++){
		         						Node prodNode = prodListNode.getChildNodes().item(k);
		         					
		         						String custid = (String) custGUIDS.get(custProdGUID);
		         						
		         						if(prodNode.getNodeName().equals("Product")){
		         							String prodGUID   = getNode(prodNode,"ProductGUID").getFirstChild().getNodeValue();
		         			         		 String prodLastDate   = getNode(prodNode,"LastDate").getFirstChild().getNodeValue().substring(0,5);
		         			         		 String prodInvNo   = getNode(prodNode,"InvoiceNo").getFirstChild().getNodeValue();
		         			         		 
		         			         		 Double prodQty = 0.0;
		         			         		try{
		         			         			prodQty  = Double.parseDouble(getNode(prodNode,"Qty").getFirstChild().getNodeValue());
		         			         		 }catch(Exception e){}
		         			         		 
		         			         		
		         			         		Double prodLastPrice = 0.0;
		         			         		try{
			         			         		 prodLastPrice   = Double.parseDouble(getNode(prodNode,"LastPrice").getFirstChild().getNodeValue());
		         			         		 }catch(Exception e){}
		         			         		 
		         			         		 //System.out.println(prodGUID + " " + prodLastDate);
		         			         		 String prodid = (String) prodsGUID.get(prodGUID);
		         			         		 
		         			         		 if(prodid != null && prodid.length() > 0){
		         			         			CustProd cp = new CustProd(custid, prodid, prodQty ,0 ,0,prodLastDate,prodLastPrice,0,0," ");
		         			         			 cp.setAbiKood(prodInvNo);
		         			         			custProds.add(cp);
		         			         		 }
		         						}
		         					}
		         					
		         				}
			         			


		         			}
		         			//System.out.println(addressListNode.getChildNodes().item(j));
		         			
		         		}
	         		

	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         }
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}

	    // System.exit(0);
	   
	    return custProds;
	  }
   


   public String getDriverVersion(){
     return "S1XMLRadex";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
	
	private String clear (String ss){
			return ss;
	}
   
   
   public double getSalary(String agentCode){

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   private String subjectFilesTo = null;
   private TreeMap<String,Double> overdebts = new TreeMap<String,Double>();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);

   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
	  
	   
	 	try {
	 		  File f = new File (getDir() + "AgentSales.xml");

	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	     	
	          
	     	
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "AgentSalesList"); 
	     	
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	  
	         	 if(rida.getNodeName().equals("Sale")){
		         	  try{
		         		 String agentSaleGUID   = getNode(rida,"AgentGUID").getFirstChild().getNodeValue().substring(0, 8);
		         		 
		         		 
		         		Double agentSaleSum  = Double.parseDouble(getNode(rida,"Sum").getFirstChild().getNodeValue()) ;
		         		
		         		Agent agent = (Agent)agents.get(agentSaleGUID);
		                agent.setMonthSales(agentSaleSum.intValue());
		                //System.out.println(agentSaleGUID + " " +agentSaleSum);
		         		
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         		}
	         	 }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}

	   
	     
	     
	  /*
	  while(rs.next()) {
          String agentID = rs.getString("agent").trim();
          Agent agent = (Agent)agents.get(agentID);
          if (agent != null){
        	  double sales = rs.getDouble("summa");
            agent.setMonthSales((int)sales);
                 		
 	     }
          }
          */
	  
	  
	  
	  
  }

  
  
public Vector getFixedAssortmentData() {	
	//  Vector result = new Vector();
	  return prlists;
     /* try {
       
     	 FileReader fr = new FileReader(getDir() + "PriceMatrix.csv");
          BufferedReader reader = new BufferedReader(fr);
        String line = null;
        reader.readLine();
         while((line = reader.readLine()) != null ){

              line = line.replaceAll(";"," | ");
         	StringTokenizer st = new StringTokenizer(line, "|");

           if (st.hasMoreTokens()) {
         	//System.out.println( line );
//00012;;00012;0019D;;;7.6;;
              st.nextToken();
              st.nextToken();
              
              String clGroup = st.nextToken().trim();
              String prodID = st.nextToken().trim();
              st.nextToken();
              st.nextToken();
              double price = Double.parseDouble(st.nextToken().trim());
              String sdate =  st.nextToken().trim();
                 String edate = st.nextToken().trim();
                 String act = st.nextToken().trim();
            
          MatrixLine mline = new MatrixLine();
          if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
            mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
          }
          
          if(act.equals("Akc")) {
        	  
         
try {
	if(sdate != null  && !sdate.startsWith("1753") && sdate.trim().length() > 0 ){
    	 mline.setStartDate(sdate.substring(0,8));
     }
     if(edate != null && !edate.startsWith("1753") && sdate.trim().length() > 0 ){
    	 mline.setEndDate(edate.substring(0,8));
     }
}catch(Exception e) {
	
}
          }  
          
          
          if (prodID != null && prodID.length() > 0) {
            mline.setProdID(prodID);
          }
         

              result.add(mline);    
            
          }
         }
         
         reader.close();
         fr.close();
      }catch(Exception e){
      e.printStackTrace();
      }
	
    // System.out.println("fixed " + result.toString());
 return result;*/
}


@Override
public String getStartingTime() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void connectionUp() {
	// TODO Auto-generated method stub
	
}
@Override
public void connectionDown() {
	// TODO Auto-generated method stub
	
}
@Override
public void PictureMaker() {
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
//     	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

     StringBuffer buf = new StringBuffer();
     Iterator enume = prods.keySet().iterator();

     while (enume.hasNext()) {
	    	String asi = (String)enume.next();
	    	Product prod = (Product)prods.get(asi);
	    	String toode = prod.getEAN().trim();  
	    	//System.out.println("pildid" + toode);
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
 System.exit(0);
}




}
