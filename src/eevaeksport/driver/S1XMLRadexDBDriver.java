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
   
   
   public Vector getPriceMatrixData(){
	   Vector result = new Vector();
	   if(prlists != null) {
		   return prlists;
	   }
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







   public Vector getDebt(String agentCode){
        return this.debtList;
  }


   public String getDealerCustomerCSV(String agent){
    return null;
    }

   public String  getCustProdDiscounts(Agent agent){
    return null;
    }

   public TreeMap getProdGroups() throws SQLException {

	    TreeMap   tm = new TreeMap();
	    //  setTreeGroup(true);
	      try {
	    	  FileReader fr = new FileReader(getDir() + "Group.csv");
	    	  BufferedReader reader = new BufferedReader(fr);
	    	  String line = null;
	  
	    	  line=reader.readLine();
		     while((line = reader.readLine()) != null ){
		        line = line.replaceAll(";","| ");
		        StringTokenizer st = new StringTokenizer(line, "|");
		        
		        //GroupCode		ParentGrCode	Description

		         if (st.hasMoreTokens()){
		             String groupcode = st.nextToken().trim();
		             String parentgroupcode = st.nextToken().trim();
		             String name = st.nextToken().trim();
		             
		          
		             if (parentgroupcode.equals("15")) {
		        		 name = "PrDlės " + name;
		        	 }
		             if (parentgroupcode.equals("3")) {
		        		 name = "PrDros " + name;
		        	 }

		             if (parentgroupcode.equals("24")) {
		        		 name = "J.Pusf " + name;
		        	 }
		             if (parentgroupcode.equals("27")) {
		        		 name = "Š.Pusf " + name;
		        	 }
		             if (parentgroupcode.equals("25")) {
		        		 name = "K.Pusf " + name;
		        	 }
		             if (parentgroupcode.equals("5")) {
		        		 name = "KrGami " + name;
		        	 }
		             if (parentgroupcode.equals("14")) {
		        		 name = "Skerdi " + name;
		        	 }
		             
		        	 if (parentgroupcode.equals("4")) {
		        		 name = "ŠrDros " + name;
		        	 }
		        	 if (parentgroupcode.equals("11")) {
		        		 name = "ŠrGami " + name;
		        	 }
		        	 if (parentgroupcode.equals("1")) {
		        		 name = "VDšros " + name;
		        	 }
		        	 if (parentgroupcode.equals("2")) {
		        		 name = "VDrlės " + name;
		        	 }
		        	 if(name.length()> 2) {
		        		 tm.put(Converter.cleanLitLatEst(parentgroupcode), Converter.enCodeEst(name));
		 		        
		        	 }
		           }
		         }
	     reader.close();
	     fr.close();
		    	
	       } catch (IOException e){
	         e.printStackTrace();
	       }
	          return tm;
   
 
   }

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
	         		 String salesman   = getNode(rida,"Code").getFirstChild().getNodeValue();
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
    	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(4, 6))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(6, 8)));
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


       public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     custAgents = new Hashtable();
	
		 String CustGUID   = null;
		 String CustName   = null;		
		 String CustCode   = null;
		 String DAddressGUID   = null;
		String DAddressCode   = null;
		String DAddress   = null;
		String DAddressAgentGUID   = null;
		
    	 
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
	         	  Node rida = root.getChildNodes().item(i);
	         	  //System.out.println(rida.getNodeName() + " " );
	         	  if(rida.getNodeName().equals("Customer")){
	         		  try{
		         		 CustGUID   = getNode(rida,"CustomerGUID").getFirstChild().getNodeValue();
		         		 CustName   = getNode(rida,"CustomerName").getFirstChild().getNodeValue();		
		         		 CustCode   = getNode(rida,"CustomerCode").getFirstChild().getNodeValue();
		         		 CustName = Converter.deCode(CustName);
		         		
		         		 //System.out.println(CustGUID + " " +CustName);
		         	  }catch(Exception e){
		         		  e.printStackTrace();
		         	  }
	         	  
         		  	Node addressNode = getNode(rida, "DeliveryAddressList");
	      			for (int j = 0; j < addressNode.getChildNodes().getLength(); j++){
	      				Node ridaAddress = addressNode.getChildNodes().item(j);
	      				
	      				if(ridaAddress.getNodeName().equals("DeliveryAddress")){
	      				DAddressGUID   = getNode(ridaAddress,"AddressGUID").getFirstChild().getNodeValue();
	      				DAddressCode   = getNode(ridaAddress,"AddressCode").getFirstChild().getNodeValue();
	      				DAddress   = getNode(ridaAddress,"Address").getFirstChild().getNodeValue();
	      				DAddressAgentGUID   = getNode(ridaAddress,"AgentGUID").getFirstChild().getNodeValue();
	      				//System.out.println("Addressinfo: "+DAddressGUID + " " + DAddressCode + " " + DAddress + " " + DAddressAgentGUID);
	      				
	      		         Customer customer = new Customer(CustCode + "_" + DAddressCode);
	      		         customer.setName(Converter.enCodeEst(CustName) + "_" + DAddressCode);
	      		         customer.setAddress(Converter.enCodeEst(Converter.adressLink(DAddress,"Latvia")));
	      		         
	      		         customer.addAgent(new Agent(agentGUID.get(DAddressAgentGUID)));
	      		         
	      		         tm.put(Converter.cleanLitLatEst(CustName + " "+ DAddressCode), customer);
	      				
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
   

   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     

     
     //Productgroups.xml
     
     String ProdGroupsGUID=null;
     String ProdGroupsCode=null;
     String ProdGroupsDescription=null;
     //String ProdGroupsParentGrGUID=null;
		try {
			StringBuffer sbEAN = new StringBuffer();
			  File f = new File (getDir() + "ProductGroups.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
			FileInputStream in = getCleanInputStream(f);
			String handler = null;
		    Document doc = db.parse(in,handler);
		    Node root = getNode(doc, "ProductGroupsList"); 
		    for (int i = 0; i < root.getChildNodes().getLength(); i++){
		    	  Node rida = root.getChildNodes().item(i);
		    	  
		    	  if(rida.getNodeName().equals("Group")){
		    		  try{
		    			  ProdGroupsGUID   = getNode(rida,"GUID").getFirstChild().getNodeValue();
		    			  ProdGroupsCode   = getNode(rida,"GroupCode").getFirstChild().getNodeValue();
		    			  ProdGroupsDescription   = getNode(rida,"Description").getFirstChild().getNodeValue();
		    			  //rodGroupsParentGrGUID   = getNode(rida,"ParentGrGUID").getFirstChild().getNodeValue();

		        		 System.out.println("ProdGroups: " + ProdGroupsGUID + " " + ProdGroupsCode + " " + ProdGroupsDescription);
		        	  }catch(Exception e){
		        		  e.printStackTrace();
		        	  }
		    	  	}
		    	  }
		    		  
		    in.close();
			}catch(Exception k ){
				k.printStackTrace();
			}
 
     
		//Product.xml
     String ProdGUID=null;
	 String ProdCode=null;   
	 String ProdDesc=null;   
	 String ProdUnit=null;  
	 String GroupGUID=null;
	 String UnitPrice=null;  
	 String inventory=null; 
     
		
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
		    	  Node rida = root.getChildNodes().item(i);
		    	  
		    	  //System.out.println(rida.getNodeName() + " " );
		    	  if(rida.getNodeName().equals("Product")){
		    		  try{
		        		  ProdGUID   = getNode(rida,"GUID").getFirstChild().getNodeValue();
		        		  ProdCode   = getNode(rida,"Code").getFirstChild().getNodeValue();
		        		  ProdDesc   = getNode(rida,"Description").getFirstChild().getNodeValue();
		        		  ProdUnit   = getNode(rida,"Unit").getFirstChild().getNodeValue();
		        		  GroupGUID   = getNode(rida,"GroupGUID").getFirstChild().getNodeValue();
		        		  //GroupGUID2   = getNode(rida,"GroupSecGUID").getFirstChild().getNodeValue();
		        		  UnitPrice   = getNode(rida,"UnitPrice").getFirstChild().getNodeValue();
		        		  inventory   = getNode(rida,"Inventory").getFirstChild().getNodeValue();
		        		
		        		 //System.out.println(CustGUID + " " +CustName);
		        	  }catch(Exception e){
		        		  e.printStackTrace();
		        	  }
		    		  
		    		  
		    		 Product prod = new Product(ProdCode);


		   			 prod.setStock(Double.parseDouble(inventory));
		   			 
		   			 //if(Double.parseDouble(incase) > 1) {
		   				 //prod.setInCases(Double.parseDouble(incase));
		   			 //}
		   			 
		   			 if(ProdUnit.length() > 0) {   
		   				 prod.setUnit(ProdUnit);
		   			 }
		   			  prod.setType(GroupGUID);
		   			
		   			 //if(groupcodesec.length() > 0) {
		   				 //prod.setGroup(GroupGUID2);
		   			 //}
		   			 
		   			 if(ProdDesc.length() > 0) {
		   				 prod.setName(ProdDesc + " "+ ProdCode);
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
		 				System.out.println("EANKOOD: "+ EANNode.getChildNodes().item(j).getTextContent());
		 				
		 				if(x == 0 && EANKood.length() > 0) {
		   					 prod.setEAN(EANKood);
		   				 }
		 				if(x != 0 && EANKood.length() > 0) {
		 					sbEAN.append(ProdCode + ";" + EANKood + "\n"); //prodanalogsidesse
		   				 }
		 				x++;
		 				}
		 				
		 			  }
		 			
					 tm.put(Converter.cleanLitLatEst(ProdDesc + prod.getUnit() + prod.getID()), prod);

			       }
		   			if(sbEAN.length() > 0) {
			     		 Converter.saveFileInUTF8("prodanalogs.txt", sbEAN.toString());
			     	 }
		    	}
		    
		    	  
		    
		    /*	 
				 
				 tm.put(Converter.cleanLitLatEst(description + prod.getUnit() + prod.getID()), prod);
			 
		     */
		    //customercode_addresscode
		    	  
		
		    in.close();
		}catch(Exception k ){
				k.printStackTrace();
			}
		
		

 




return tm;









/*

     try {

     	//laoseisude lugemine ja liitmine algab
          
    	
         String line = null;
         
		 FileReader fr = new FileReader(getDir() + "Product.csv");
		 BufferedReader reader = new BufferedReader(fr);

        reader.readLine();
        while(( line = reader.readLine()) != null ){
        line = line.replaceAll(";"," |");
     
       StringTokenizer st = new StringTokenizer(line, "|");
      //Code		Description		Unit	incase	GroupCode	GroupSecCode	UnitPrice	EAN	Inventory	Pallet	Blocked

       if (st.hasMoreTokens()){
    	   String id = st.nextToken().trim();
    	   String description = st.nextToken().trim();
    	   String unit = st.nextToken().trim();
    	   String incase = st.nextToken().trim();
    	   String groupcode = st.nextToken().trim();
    	   String groupcodesec = st.nextToken().trim();
    	   String unitprice = st.nextToken().trim();
    	   String ean = st.nextToken().trim();
    	  // String inventory = st.nextToken().trim();

    	   Product prod = new Product(id);
    	   //System.out.println(description);
            if(stocks.get(id) != null) {
           	 if(Double.parseDouble(stocks.get(id)+"") > 1) {
				 prod.setStock(Double.parseDouble(stocks.get(id)+""));
			 }
			 	
            }
		
			 
			 if(Double.parseDouble(incase) > 1) {
				 prod.setInCases(Double.parseDouble(incase));
			 }
			 
			 if(unit.length() > 0) {
				 prod.setUnit(unit);
			 }
			 
			
				 prod.setType(groupcode);
			
			 if(groupcodesec.length() > 0) {
				 prod.setGroup(groupcodesec);
			 }
			 
			 if(description.length() > 0) {
				 prod.setName(description + " "+ id);
			 
	
				 if(Double.parseDouble(unitprice) > 0) {
					 prod.setPrice(Double.parseDouble(unitprice));
				 }
				 
				 if(ean.length() > 0) {
					 prod.setEAN(ean);
				 }
				 
				 prod.setVat("21");
				 tm.put(Converter.cleanLitLatEst(description + prod.getUnit() + prod.getID()), prod);
			 
			 }
			 
			 
			 

         }
       }
        
        reader.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     
     
     return tm;*/
   }

   
   
   
   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
  try {
	  
     FileReader fr = new FileReader(getDir() + "CustomerProducts.csv");
		 BufferedReader reader = new BufferedReader(fr);
		 String line = null;
      
		 reader.readLine();
      while(( line = reader.readLine()) != null ){
    	  line = line.replaceAll(";"," |");
    	   StringTokenizer st = new StringTokenizer(line, "|"); 
    	 //  CustCode;ProductCode;Qty;LastDate;InvoiceNo;LastPrice
    	   String custID = st.nextToken().trim();
        	String prodID = st.nextToken().trim();
			double sold = Double.parseDouble(st.nextToken().trim());							
			String last = st.nextToken().trim().substring(4);
			String lastdoc = st.nextToken().trim();
			double lastprice = Double.parseDouble(st.nextToken().trim());	
			
			CustProd cp = new CustProd(custID, prodID,
					 sold, 0,0, last, lastprice, 0, 0, " ");
	
			cp.setAbiKood(lastdoc);
			custProds.addElement(cp);
      }
      
	  reader.close();
  }catch(Exception e) {
	  e.printStackTrace();
  }
	
  //System.out.println(custProds.toString());
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

	 Hashtable plans =  readPlans();
	if( agGrSales == null) {
		readCustSales();
	}
	
	 int d1 = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
     int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

	TreeMap plandone = new TreeMap();
	
	Enumeration enume =  agGrSales.keys();
  	 while(enume.hasMoreElements()) {
               String agentID = (String)enume.nextElement();
               TreeMap tree  = (TreeMap)agGrSales.get(agentID);
               Iterator itera =   tree.keySet().iterator();
                while(itera.hasNext()) {
            	  String grid = (String)itera.next();
                 Toode dd = (Toode)tree.get(grid);
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(dd.getDonecur()));
                   agent.setKgSales((int)dd.getDonevol());
                   
                   TreeMap hh = new TreeMap();
                   if(plandone.get(agentID) != null) {
                	   hh = (TreeMap) plandone.get(agentID);
                   }
                   
                   if(grid.equals("1") || grid.equals("2") || grid.equals("3") || grid.equals("4") || grid.equals("5") 
                		   || grid.equals("11") || grid.equals("15") ) {
                	Toode yy = new Toode("Gaminiai");
                	 if(  hh.get("Gaminiai") != null ) {
                		 yy = (Toode)hh.get("Gaminiai");
                	 }
                	   yy.addDonecur(dd.getDonecur());
                	   yy.addDonevol(dd.getDonevol());
                	   hh.put("Gaminiai", yy);
                   }
            
                   
                   if(grid.equals("29") ) {
                   	Toode yy = new Toode("Mar. gaminiai");
                   	 if(  hh.get("Mar. gaminiai") != null ) {
                   		 yy = (Toode)hh.get("Mar. gaminiai");
                   	 }
                   	   yy.addDonecur(dd.getDonecur());
                   	   yy.addDonevol(dd.getDonevol());
                   	   hh.put("Mar. gaminiai", yy);
                      }
                   
               
                   if( grid.equals("27") || grid.equals("24") || grid.equals("25") || grid.equals("14")
                		   || grid.equals("12") || grid.equals("10") || grid.equals("9") || grid.equals("7") || grid.equals("6")) {
                      	Toode yy = new Toode("Sv.Mesa");
                      	 if(  hh.get("Sv.Mesa") != null ) {
                      		 yy = (Toode)hh.get("Sv.Mesa");
                      	 }
                      	   yy.addDonecur(dd.getDonecur());
                      	   yy.addDonevol(dd.getDonevol());
                      	   hh.put("Sv.Mesa", yy);
                         }
                      
                   plandone.put(agentID, hh);
                  }
               }
      	    }
  	 
  Enumeration enume2 = plans.keys();
  	 while(enume2.hasMoreElements()) {
  		 String agid = (String)enume2.nextElement();
  		  Agent agent = (Agent)agents.get(agid);
  		  if(agent != null) {
  			  TreeMap doneall = (TreeMap)plandone.get(agid);
  			  ArrayList<Toode> pl = (ArrayList<Toode>)plans.get(agid);
  			  
  		for(Toode tt : pl) {
  			
  		  double kgplan = tt.getPlanvol();
  		  double eurplan = tt.getPlancur();
  		  double kgsold = 0;
  		  double eursold = 0;
  		  double estkg = 0;
 		  double esteur = 0;
  		 
  		//  System.out.println("rida " + agid + " " + kgplan + " " + eurplan + " " + tt.getGr1()+ "\n " + doneall.keySet().toString());
  		  
  		 if(doneall != null && doneall.get(tt.getGr1()) != null) {
  			 Toode hhh = (Toode) doneall.get(tt.getGr1());
  			   kgsold = hhh.getDonevol();
  	   		   eursold = hhh.getDonecur();
  	   	       double plaanitavMyykKG = d2 * kgsold/d1;
			   estkg = plaanitavMyykKG/kgplan;
			   double plaanitavMyykEUR = d2 * eursold/d1;
			   esteur = plaanitavMyykEUR/eurplan;
  		 }
  		
  		 String asi = tt.getGr1() +  (" Plan/Pard/Est% |") + (int)kgplan + "/" + (int)kgsold + "/"  +(int)(estkg*100) + "% kg|";
  		  asi = asi + (int)eurplan + "/" + (int)eursold  + "/"  +(int)(esteur*100) + "% €| |";
  		 agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
  			
  		}	
  		}
  	 }
  		
		        
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
