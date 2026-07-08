
package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
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

public class ViaLaimaUUSDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter,PictureMaker,WebReporter {

	
	
	
	
	public ViaLaimaUUSDBDriver(EksportPanel parent) {
		super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.company");
        rcompanyx = getParent().getProps().getProperty("rest.companyx");
       
        jsonprice0 = getParent().getProps().getProperty("json.price0");
        jsonprice1 = getParent().getProps().getProperty("json.price1");
        jsonprice2 = getParent().getProps().getProperty("json.price2");
        jsonprice3 = getParent().getProps().getProperty("json.price3");
        jsonprice4 = getParent().getProps().getProperty("json.price4");
        jsoncust0 = getParent().getProps().getProperty("json.cust0");
        jsoncust1 = getParent().getProps().getProperty("json.cust1");
        jsoncust2 = getParent().getProps().getProperty("json.cust2");
        jsoncust3 = getParent().getProps().getProperty("json.cust3");
        jsoncust4 = getParent().getProps().getProperty("json.cust4");
        jsonprgroup0 = getParent().getProps().getProperty("json.prgr0");
        jsonprgroup1 = getParent().getProps().getProperty("json.prgr1");
        jsonprgroup2 = getParent().getProps().getProperty("json.prgr2");
        jsonprgroup3 = getParent().getProps().getProperty("json.prgr3");
        jsonprgroup4 = getParent().getProps().getProperty("json.prgr4");
        jsonprod0 = getParent().getProps().getProperty("json.prod0");
        jsonprod1 = getParent().getProps().getProperty("json.prod1");
        jsonprod2 = getParent().getProps().getProperty("json.prod2");
        jsonprod3 = getParent().getProps().getProperty("json.prod3");
        jsonprod4 = getParent().getProps().getProperty("json.prod4");
        jsonstock0 = getParent().getProps().getProperty("json.stock0");
        jsonstock1 = getParent().getProps().getProperty("json.stock1");
        jsonstock2 = getParent().getProps().getProperty("json.stock2");
        jsonstock3 = getParent().getProps().getProperty("json.stock3");
        jsonstock4 = getParent().getProps().getProperty("json.stock4");
        jsoncustprod0 = getParent().getProps().getProperty("json.custprod0");
        jsoncustprod1 = getParent().getProps().getProperty("json.custprod1");
        jsoncustprod2 = getParent().getProps().getProperty("json.custprod2");
        jsoncustprod3 = getParent().getProps().getProperty("json.custprod3");
        jsoncustprod4 = getParent().getProps().getProperty("json.custprod4");
        jsondebt0 = getParent().getProps().getProperty("json.debt0");
        jsondebt1 = getParent().getProps().getProperty("json.debt1");
        jsondebt2 = getParent().getProps().getProperty("json.debt2");
        jsondebt3 = getParent().getProps().getProperty("json.debt3");
        jsondebt4 = getParent().getProps().getProperty("json.debt4");
        
        jsonopen0 = getParent().getProps().getProperty("json.open0");
        jsonopen1 = getParent().getProps().getProperty("json.open1");
        jsonopen2 = getParent().getProps().getProperty("json.open2");
        jsonopen3 = getParent().getProps().getProperty("json.open3");
        jsonopen4 = getParent().getProps().getProperty("json.open4");
        jsonshmode = "http://84.50.148.253/edimap2/index.php?loc=php/reports/actionsreports.php&trackid=0899e9728ad5c6d69baf32d059759064b478957db0cbfdc9adc3ff0d7ec7ae1f54d61d5506df844b9f3f7ef3bb9e7ce2c82e31363431333335303539&output=json";
    
	}

      String user =  null;
      String pass = null;
      String rcompany = null;
      String rcompanyx = null;
      String jsoncust0 = null;
      String jsoncust1 = null;
      String jsoncust2 = null;
      String jsoncust3 = null;
      String jsoncust4 = null;
      String jsoncustprod0 = null;
      String jsoncustprod1 = null;
      String jsoncustprod2 = null;
      String jsoncustprod3 = null;
      String jsoncustprod4 = null;
      String jsonstock0 = null;
      String jsonstock1 = null;
      String jsonstock2 = null;
      String jsonstock3 = null;
      String jsonstock4 = null;
      String jsonprice0 = null;
      String jsonprice1 = null;
      String jsonprice2 = null;
      String jsonprice3 = null;
      String jsonprice4 = null;
      String jsonprgroup0 = null;
      String jsonprgroup1 = null;
      String jsonprgroup2 = null;
      String jsonprgroup3 = null;
      String jsonprgroup4 = null;
      String jsonprod0 = null;
      String jsonprod1 = null;
      String jsonprod2 = null;
      String jsonprod3 = null;
      String jsonprod4 = null;
      String jsondebt0 = null;
      String jsondebt1 = null;
      String jsondebt2 = null;
      String jsondebt3 = null;
      String jsondebt4 = null;
      String jsonopen0 = null;
      String jsonopen1 = null;
      String jsonopen2 = null;
      String jsonopen3 = null;
      String jsonopen4 = null;
      String jsonshmode = null;
      
      
      private NumberFormat NF = NumberFormat.getInstance();
      
      private void loadCamp() {
    	  NF.setMaximumFractionDigits(4);
    	    NF.setGroupingUsed(false);
    	  
    	  File ffa = new File(this.getExcelDir() + "TTKampaania.xlsx");
    		
    		try {
    			
    			
    	 if(ffa.exists()){
    		   
    		    StringBuffer buf = new StringBuffer();
    			FileInputStream ff = new FileInputStream(ffa);
    			XSSFWorkbook workbook = new XSSFWorkbook((ff));
    			XSSFRow row = null;
    			XSSFSheet sheetc = workbook.getSheetAt(0);
    		
    			TreeMap shops = new TreeMap();
    			
    			for (int a = 4; a < sheetc.getLastRowNum(); a++) {
    		               row = sheetc.getRow(a);
    				try {
    			String prodid = null;
    			try {
    				prodid = row.getCell((short) 0).getStringCellValue();
    			}catch(Exception r) {
    				try {
    					prodid = (int)row.getCell((short) 0).getNumericCellValue() + "";
    				}catch(Exception rr) {
    					
    				}
    				
    			}
    			if(prodid != null) {
    				 double kogus =  row.getCell((short) 7).getNumericCellValue();
    	        	 double hind =  row.getCell((short) 5).getNumericCellValue();
    	        	 if(prodid != null && prodid.trim().length() > 3) {
    	        	       buf.append(prodid+ "; ; ;" + NF.format(hind).replace(',', '.') + ";" + "TTT" + ";" + kogus + ";"  );
    	                   buf.append("\n");	
    	      		 }
    	        	 
    			}
    		
	        	 
	      }catch(Exception h) {
    			h.printStackTrace();
    		}
    				 
    				
    				
    				
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
    			    
    			}
    	   
    		}catch(Exception k) {
    		  k.printStackTrace();	
    		}
    				
    		
    
      }
      
      
      private HashSet loadAllowedProds() {
    	  
    	  HashSet res = new HashSet();
    	  
    	    NF.setMaximumFractionDigits(4);
    	    NF.setGroupingUsed(false);
    	  
    	  File ffa = new File(this.getExcelDir() + "products.xlsx");
    		
    		try {
    			
    	 if(ffa.exists()){
    		   
    			FileInputStream ff = new FileInputStream(ffa);
    			XSSFWorkbook workbook = new XSSFWorkbook((ff));
    			XSSFRow row = null;
    			XSSFSheet sheetc = workbook.getSheetAt(0);
    		
    			
    			for (int a = 0; a < sheetc.getLastRowNum(); a++) {
    		               row = sheetc.getRow(a);
    				try {
    			String prodid = null;
    			try {
    				prodid = row.getCell((short) 0).getStringCellValue();
    			}catch(Exception r) {
    				try {
    					prodid = (int)row.getCell((short) 0).getNumericCellValue() + "";
    				}catch(Exception rr) {
    					
    				}
    				
    			}
    			if(prodid != null) {
    				
    	        	res.add(prodid); 
    			}
    		
	        	 
	      }catch(Exception h) {
    			h.printStackTrace();
    		}
    			}}
    				 
    		  }catch(Exception h) {
      			h.printStackTrace();
      		}	
    		System.out.println("allowed prods " + res.size());	   	    
      return res;    		
    
      }
      
  	public void makeFixSortNewProds() {
		Vector lines = new Vector();
		
		   StringBuffer sb = new StringBuffer();
		
		   TreeMap kampid = new TreeMap();// siia tekstid mis klientidele kï¿½lge
			
		     int a = 0;
		   try {
				  File f = new File (getExcelDir() + "fixnewprods.xlsx");
					
				  FileInputStream	 ff = new FileInputStream(f);

		 			 DataFormatter formatter = new DataFormatter();
						 
				  //mï¿½ï¿½rame ketid
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFRow row = null; 
	 					ff.close();	
	 					XSSFSheet	 sheet = workbook.getSheetAt(0); 
						ff.close();	
						
						if(sheet != null){
							for(int i = 0 ; i < sheet.getLastRowNum();i++){
								 row = sheet.getRow(i);
								//System.out.println(row);
								if(row == null && a == 0){
									a = i;
								}else{
									a = sheet.getLastRowNum();
								}
							}
							
							
							for(int i = 1 ; i < a+1 ; i++){
								
								 row = sheet.getRow(i);
								
						 String kett = null;
									try {
										kett = formatter.formatCellValue(row.getCell((short) 0));
									} catch (Exception e) {
										e.printStackTrace();
									}
									
						String prodid = null;
								try {
									prodid = formatter.formatCellValue(row.getCell((short) 2));
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								
								if(prodid != null) {
								
								
								java.util.Date lop = null ; 
								try {
									lop = row.getCell((short) 1).getDateCellValue();
								
								} catch (Exception e) {
									
								}
								
								
								
								 java.util.Date dd = new java.util.Date();
										
										
								if(lop != null &&  dd.before(lop)){
									sb.append(kett+";"+prodid + ";"+ lop.getTime()+ "\n");
								}
								    }
							      }
								}
		         		
	            	  }catch (Exception e){
	                  // e.printStackTrace();	
		         	  }
    if(sb.toString().length() > 2) {
    	Converter.saveFile("fixsortnew.txt", sb.toString());
    }

	
	}
	

      
      private ArrayList<Product> lisatooted = null;
      private ArrayList<String> lisagrupid = null;
      private TreeMap<String,ArrayList<String>> lisaagendigrupid = null;
      private Vector<MatrixLine> matrixlines = null;
      private void loadLisaInfo() {
    	  NF.setMaximumFractionDigits(4);
    	    NF.setGroupingUsed(false);
    	  
    	  File ffa = new File(this.getExcelDir() + "lisainfo.xlsx");
    		
    		try {
    			
    			
    	 if(ffa.exists()){
    		 lisatooted = new ArrayList<Product>();
    		 lisagrupid = new ArrayList<String>();
    		 lisaagendigrupid = new  TreeMap<String,ArrayList<String>>();
    		   
    			FileInputStream ff = new FileInputStream(ffa);
    			XSSFWorkbook workbook = new XSSFWorkbook((ff));
    			XSSFRow row = null;
    			XSSFSheet sheetc = workbook.getSheet("tooted");
    		
    			
    			for (int a = 5; a < sheetc.getLastRowNum()+1; a++) {
    		               row = sheetc.getRow(a);
    				try {
    			String prodid = null;
    			try {
    				prodid = row.getCell((short) 0).getStringCellValue();
    			}catch(Exception r) {
    				try {
    					prodid = (int)row.getCell((short) 0).getNumericCellValue() + "";
    				}catch(Exception rr) {
    					
    				}
    			}
    			
    			String ean = null;
    			try {
    				
    				 DataFormatter formatter = new DataFormatter();
						 ean = formatter.formatCellValue(row.getCell(1));
    				
    			}catch(Exception r) {
    				try {
    					ean = (int)row.getCell((short) 1).getNumericCellValue() + "";
    					
    				}catch(Exception rr) {
    					rr.printStackTrace();
    				}
    			}
    			
    			
    			String name = null;
    			try {
    				name = Converter.enCodeEst(row.getCell((short) 2).getStringCellValue());
    			}catch(Exception r) {
    				try {
    					name = Converter.enCodeEst((int)row.getCell((short) 2).getNumericCellValue() + "");
    				}catch(Exception rr) {
    					
    				}
    			}
    			
    			String type = null;
    			try {
    				type = row.getCell((short) 3).getStringCellValue();
    			}catch(Exception r) {
    				try {
    					type = (int)row.getCell((short) 3).getNumericCellValue() + "";
    				}catch(Exception rr) {
    					
    				}
    			}
    		
    			String grupp = null;
    			try {
    				   grupp = Converter.enCodeEst(row.getCell((short) 4).getStringCellValue());
    			}catch(Exception r) {
    				try {
    					grupp = Converter.enCodeEst((int)row.getCell((short) 4).getNumericCellValue() + "");
    				}catch(Exception rr) {
    				}
    			}
    			
    			String yhik = null;
    			try {
    				yhik = row.getCell((short) 7).getStringCellValue();
    			}catch(Exception r) {
    				try {
    					yhik = (int)row.getCell((short) 7).getNumericCellValue() + "";
    				}catch(Exception rr) {
    				}
    			}
    		
    		
    		
    			if(prodid != null) {
    		    	 double hind =  1;
    		    	 try{
    		    		hind = row.getCell((short) 8).getNumericCellValue();
    		    	 }catch(Exception t) {
    		    		 
    		    	 }
    	          Product prod = new Product(prodid);
    	          prod.setPrice(hind);
    	          if(name != null) {
    	        	  prod.setName("yy " + name);
    	          }
    	          
    	          if(grupp != null) {
    	        	  prod.setGroup(grupp);
    	          }
    	         
    	          
    	          if(type != null) {
    	        	  prod.setAgentGroup(type);
    	        	 // System.out.println(" " + type );
    	          }
        	       
    	          if(yhik != null) {
    	        	  prod.setUnit(yhik);
    	          }
    			  

    	          if(ean != null) {
    	        	  prod.setEAN(ean);
    	        	 // System.out.println("ean " + prodid + " " + ean);
    	          }
    	          prod.noOrder = true;
    	          
    	          lisatooted.add(prod);
    	          
    	          if(!lisagrupid.contains(grupp)) {
    	        	  lisagrupid.add(grupp);
    	          }
    	         
    			}
    		 
    			
    		  } catch (Exception h) {
    			h.printStackTrace();
    		  }	
    		 }  
    			
    			
       sheetc = workbook.getSheet("agendigrupp");
    		
    			
    			for (int a = 1; a < sheetc.getLastRowNum()+1; a++) {
    			       row = sheetc.getRow(a);
    			   	String grupp = null;
        			try {
        				   grupp = Converter.enCodeEst(row.getCell((short) 1).getStringCellValue());
        			}catch(Exception r) {
        				try {
        					grupp = Converter.enCodeEst((int)row.getCell((short) 1).getNumericCellValue() + "");
        				}catch(Exception rr) {
        				}
        			}	
        			
        			
        			String agen = null;
        			try {
        				   agen = Converter.enCodeEst(row.getCell((short) 0).getStringCellValue());
        			}catch(Exception r) {
        				try {
        					agen = Converter.enCodeEst((int)row.getCell((short) 0).getNumericCellValue() + "");
        				}catch(Exception rr) {
        				}
        			}
        			
        			if(lisaagendigrupid == null) {
        				lisaagendigrupid = new TreeMap<String,ArrayList<String>>();
        			}
        			
        			if(grupp != null && agen != null) {
        				ArrayList<String> arr = lisaagendigrupid.get(agen);
        				if(arr == null) {
        					arr = new ArrayList<String>();
        					//arr.add("all");
        				}
        				arr.add(grupp);
        				lisaagendigrupid.put(agen, arr);
        			}
        			
    			}
    		    
   	   sheetc = workbook.getSheet("kampaaniad");
    	    		
   	   matrixlines = new Vector<MatrixLine>();
       
   	        for (int a = 1; a < sheetc.getLastRowNum()+1; a++) {
       			       row = sheetc.getRow(a);
       			   	String prodid = null;
       			
					try {
						prodid = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						try {
						prodid = (row.getCell((short) 0).getStringCellValue());
						} catch (Exception r) {
							
						}
					}
					if(prodid != null) {
					
					String sisu = null;
					try {
						sisu = (row.getCell((short) 2).getStringCellValue());
					} catch (Exception e) {
						
					}
					java.util.Date alg = null;
					String algus = null;
					try {
						alg = row.getCell((short) 3).getDateCellValue();
						algus = getDateDDkMMkYYYYStr(alg);
					} catch (Exception e) {
						
					}
					java.util.Date lop = null ; 
					String lopp = null;
					try {
						lop = row.getCell((short) 4).getDateCellValue();
						lopp = getDateDDkMMkYYYYStr(lop);
					} catch (Exception e) {
						
					}
					
					String kett = null;
					try {
						kett = (row.getCell((short) 5).getStringCellValue()).replaceAll(" ", "").trim();
					} catch (Exception e) {
						
					}
					
					String saaja = null;
					try {
						saaja = (row.getCell((short) 6).getStringCellValue()).replaceAll(" ", "");
					} catch (Exception e) {
						
					}
					
					java.util.Date dd = new java.util.Date();
					if(alg != null){
						alg = new Date(alg.getTime()- 604800000);
					}
           			
					
					if(alg != null && lop != null && dd.after(alg) && dd.before(lop)){
						MatrixLine mm = new MatrixLine();
						
						if(saaja != null && saaja.trim().length() > 0) {
							mm.setCustID(saaja.trim());
						}else {
							//mm.setCliGroup(kett);
							mm.setDiscGroup(kett);
						}
						
						mm.setProdID(prodid);
						mm.setEndDate(lopp);
						mm.setStartDate(algus);
						mm.setLineinfo(sisu);
						mm.setDiscount(10);
					    matrixlines.add(mm);
					   // System.out.println("kett " +  kett);
					}
					
           			
           		
           			
       			}}
    			
    		}
    	   
    		}catch(Exception k) {
    		  k.printStackTrace();	
    		}
    	//	System.out.println("Lisa suurused lisaagendigrupid " + lisaagendigrupid.size() );
    
      }
      private Vector fixed = null;
      
      public Vector getFixedAssortmentData() {
    	  
    	  return fixed;
      }
      
      
      private void getFixedAssortmentDataVec() {
    	    
    	   java.util.Date date = new java.util.Date();
           SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
           
           Vector result = new Vector();
	File f = new File(this.getExcelDir() + "Sortiments.xlsx");
		 StringBuffer sb  = new StringBuffer();
		try {
			
	      if(f.exists()){
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
				
			TreeMap groups = new TreeMap();
			
			row = sheet.getRow(1);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					groups.put(a + "", Converter.cleanLitLatEst(row.getCell((short) a)
							.getStringCellValue()));
				} catch (Exception e) {
				}
			}


			for (int i = 3; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row != null && row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	
					// Siin läheme reasisse rokkima ja vaatama kas se toode
					// läheb kaasa
					for (int a = 2; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi tähis
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									try{
										go = (row.getCell((short) a)
												.getStringCellValue());		
									}catch(Exception r){
										
									}
								
								}

							}

							if (go != null && (go.equals("1") || go.equals("2") 
									|| go.startsWith("a")  || go.startsWith("k")) ) {

						// System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);
								MatrixLine mline = new MatrixLine();
								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0 && (prodID.trim()) != null) {
									mline.setProdID("" + (prodID.trim()));
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								if(go.equals("2")) {
									mline.setLineinfo("MULTI" );
									sb.append(" ;" +clGroup + "; ;" + prodID + "\n" );
								}
								

//3.	Sortimendi 3 staatust – sisse , välja , uus teie olemasoleva exceli põhjal 7h tööd
//                              alates-16.06.2026
//								kuni-20.06.2026

								if(go.startsWith("alates-")) {

                                    String dateStr = go.substring(7);
                                     java.util.Date dd = sdf.parse(dateStr);
                                    
                                     long diffMillis = dd.getTime() - date.getTime();
                                     long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                                     if (diffDays <= 14 && diffDays >= 0) {
                                         System.out.println("Today is within 7 days before the start date");
                                         mline.setLineinfo("SOON");
                                     } else if (diffDays >= -14 && diffDays <= 0) {
                                    	    mline.setLineinfo("NEW");
                                    	}
                                     
								 } else if (go.startsWith("kuni-")) {

	                                    String dateStr = go.substring(5);
	                                     java.util.Date endDate = sdf.parse(dateStr);
	                                     long diffMillis = endDate.getTime() - date.getTime();
	                                     long diffDays = diffMillis / (1000L * 60 * 60 * 24);

	                                     if (diffDays < 0 && diffDays >= -14) {
	                                         System.out.println("End date expired within the last 14 days");
	                                         mline.setLineinfo("ENDING");
	                                     }
	                                    
								}
								
								 /*if(insertassort){
		                               insertSortLine(mline);   
		                           }*/
								result.add(mline);
								
	    	 						
	    	 					
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

		 System.out.println("fixed Suurus " + result.size());
		 System.out.println("Soovit Suurus " + sb.toString().length());
			try {
				Converter.saveObject(result, "fix.vec");
				if(sb.toString().length() > 0 ) {
					Converter.saveFile("assortment.txt", sb.toString());
				}else {
					File ff = new File("assortment.txt");
					if(ff.exists()) {
					//	ff.delete();
					}
				}
				
			} catch (IOException e) {
			
				e.printStackTrace();
			}
		

	}


    public String getSaveDebtInfo() {
    	
    	getFixedAssortmentDataVec();
       	return null;
       }
       
    public String getPilotDayData(Agent agent) {
		return null;
	}
	
    
    private java.util.Date getCalLit(String ss){
	  	//2006.06.22
	  Calendar cc = Calendar.getInstance();  	
	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
	  	return cc.getTime() ;
	   	
	  }
    
    private Vector pmLines = null;
    private Vector pmCodes = null;
    
    
    public Vector getPriceMatrixData() {
    	
    	if(pmLines != null) {
    		return pmLines;
    	}
    	
    	 Vector lines = new Vector();
    	    pmLines = new Vector();
    	    pmCodes = new Vector();
    	 
    	 String urlStr = null;
    	 for(int u = 0 ;u < 5;u++){
    		if(u==0){
    			urlStr = this.jsonprice0;
    		}
    		if(u==1){
    			urlStr = this.jsonprice1;
    		}
    		if(u==2){
    			urlStr = this.jsonprice2;
    		}
    		if(u==3){
    			urlStr = this.jsonprice3;
    		}
    		if(u==4){
    			urlStr = this.jsonprice4;
    		}
          try{
        	  System.out.println("ProdJson alustan ");

    		final File  v  = new File("pricejson" + u + ".txt");
    	       if(urlStr != null){ 
    	    	   try{
    	    	   URL urlst = new URL(urlStr);
    				Converter.trustAllHttpsURLConnection();
    				Converter.doTrustToCertificates();
    			URLConnection con = urlst.openConnection();
    			InputStream is = con.getInputStream();
    			OutputStream os = new FileOutputStream(v);
    			int i;
    			while ( (i = is.read()) != -1) {
    				os.write(i);
    			}
    			is.close();
    			os.close();
    			
    	    	   }catch(Exception w){
    			    	w.printStackTrace();	
    			       }	
    	    	   System.out.println("ProdJson lopetan ");
    			JSONParser parser = new JSONParser();
    		       Object obj = parser.parse(new FileReader("pricejson" + u + ".txt"));
    	           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
    	           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
    	      
    	      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
    	      while (iterator.hasNext()) {
    	    	  org.json.simple.JSONObject cob = iterator.next();
    	    	  

                  Long isProdGroup = (Long)cob.get("ITEMCODE");
                  
                String clGroup = (String)cob.get("ACCOUNTRELATION");
                String prodID = (String)cob.get("ITEMRELATION");
                double discount = 0;
                try{
                	discount = (Double)cob.get("PERCENT1");
                }catch (Exception e){
                  	discount = Double.parseDouble(((Long)cob.get("PERCENT1")+""));
                }
                
                String sdate =  (String)cob.get("FROMDATE");
				String tdate =  (String)cob.get("TODATE");
				String relation = (cob.get("RELATION"))+"";
                		

                MatrixLine mline = new MatrixLine();
                
                if (mline.getCustID() == null && clGroup != null
                                && clGroup.trim().length() > 0) {
                	clGroup = clGroup.trim();
                       // mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()).substring(0, clGroup.trim().length() - 2));
                   //    mline.setDiscGroup(clGroup);
                       mline.setCliGroup(clGroup);
                        if(!pmCodes.contains(clGroup)) {
                        	 pmCodes.add(clGroup);	
                        }
                       
                }

                if (prodID != null && prodID.trim().length() > 0) {
                	
                	if( isProdGroup == 0) {
                		mline.setProdID(prodID.trim());
                    } else {
                	 //  mline.setProdGroup(prodID.trim());
                	   mline.discGroup = prodID.trim()+"DG";
                	   
                    }
                }
                          mline.setDiscount(discount);
               
                String algus = "";
               
                java.util.Date alg = null;
                if(sdate != null && sdate.trim().length() != 0){
                	algus = sdate.substring(0,10);
                }
               
                java.util.Date lop = null;
                
				 if(tdate != null && tdate.trim().length() != 0 && relation.equals("8")){
                   mline.setEndDate(tdate.substring(0,10));
                   lop = getCalLit(tdate.substring(0,10));
                   if(!algus.equals("")){
                       mline.setStartDate(algus);
                       alg = getCalLit(algus);
                       }
                 }
					
					java.util.Date dd = new java.util.Date();
					if(alg != null){
						alg = new Date(alg.getTime()- 8640000);
					}
					
				if(alg != null && lop != null && dd.after(alg) && dd.before(lop)){
					if(discount != 0) {
						  lines.add(mline);	
						  pmLines.add(mline);	
					}
				}else if ("TB".equals(getParent().getProps().getProperty("stockcode"))){
					
					
					if(alg == null && tdate.trim().startsWith("1900")){
						if(discount != 0) {
							//System.out.println("aaanull " + alg + " " +  tdate + " " +discount);
							lines.add(mline);	
							  pmLines.add(mline);	
						}
					} else if(alg != null && tdate.trim().length() != 0 && dd.after(alg) && tdate.trim().startsWith("1900")){
						if(discount != 0) {
							//System.out.println("aaa " + alg + " " +  tdate + " " +discount);
							  lines.add(mline);	
							  pmLines.add(mline);	
						}
					}
					
				}
				 
              
        }
    	      
    	    	 }
    	      
    }catch(Exception e){
   	e.printStackTrace();
   } 
    	 }
   System.out.println("pricematrix suurus " + lines.size());

   if(matrixlines != null) {
	   lines.addAll(matrixlines);
	   pmLines.addAll(matrixlines);
   }
   System.out.println("pricematrix suurus2 " + lines.size());
       return lines; 
    }
    
    

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public void setBulkSales(Map agents) {
      
		
		StringBuffer sb1 = new StringBuffer();
TreeMap<String,Double> sales = new TreeMap<String, Double>();
TreeMap<String,Double> saleslast = new TreeMap<String, Double>();

TreeMap<String,Double> salesbyinv = new TreeMap<String, Double>();
		try {
			
			String urlStr = null;
			
			 for(int u = 0 ;u < 5;u++){
		 		if(u == 0){
		 			urlStr = this.jsoncustprod0;
		 		}
		 		if(u == 1){
		 			urlStr = this.jsoncustprod1;
		 		}
		 		if(u == 2){
		 			urlStr = this.jsoncustprod2;
		 		}
		 		if(u == 3){
		 			urlStr = this.jsoncustprod3;
		 		}
		 		if(u == 4){
		 			urlStr = this.jsoncustprod4;
		 		}
		 		System.out.println("custProdJson alustan ");
		 		
		 		
		 		
			final File  v  = new File("custprodjsontemp" + u + ".txt");
		       if(urlStr != null){ 
		    	   
		    	   if(!cpDone) {
		    		   
		    	  if(!"true".equals(getParent().getProps().getProperty("useolddata"))) {
		    		  
		    	 
		    	   try{
		    	   URL urlst = new URL(urlStr);
					Converter.trustAllHttpsURLConnection();
					Converter.doTrustToCertificates();
				URLConnection con = urlst.openConnection();
				InputStream is = con.getInputStream();
				OutputStream os = new FileOutputStream(v);
				int i;
				while ( (i = is.read()) != -1) {
					os.write(i);
				}
				is.close();
				os.close();
		    	   }catch(Exception w){
				    	w.printStackTrace();	
				       }
		    	   }
		    	  
		    	   }
		    	   
		    	   

			 		 File  vv  = new File("custprodjson"   + u+ ".txt");   
			    	   File  uu  = new File("custprodjsontemp"   + u+ ".txt");  
			    	   System.out.println("custProductJson suurus " +  uu.length() );
			    	   if(uu.exists() && uu.length() > 2000) {
			    		 if(vv.exists()) {
			    			   vv.delete();
			   		    }
			    		
			    		 boolean done =   uu.renameTo(new File("custprodjson"   + u+ ".txt")) ;
			    		// System.out.println("productJson renamed" + done);
			    	   }
		    	   
		    	   
		    	   //System.out.println("custProdJson lopp ");
				JSONParser parser = new JSONParser();
			       Object obj = parser.parse(new FileReader("custprodjson" + u + ".txt"));
		           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
		           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
		      
		      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
		      while (iterator.hasNext()) {
		    	  org.json.simple.JSONObject cob = iterator.next();
		    	 
		    	
		    	  String SALESGROUP = (String)cob.get("SALESGROUP");
		    	  String custid  = (String)cob.get("ORDERACCOUNT");
		    	  String SALESID  = (String)cob.get("SALESID");
		    	 
		    	  
		    	  if( custAgendid.get(custid) != null) {
		    	    SALESGROUP =  custAgendid.get(custid)+"";	 
		    	    //System.out.println("SalesGroup Change");
		    	  }
		    	 
		    	  String idate = ((String)cob.get("INVOICEDATE"));
		    	
		    	  
		    	  String toode = ((String)cob.get("ITEMID"));
		     if(koodiga.get(toode) != null) {
		    	  double lastSum = 0;
		    	  try {
		    		  lastSum = (Double)cob.get("LINEAMOUNT");
		    		 
		    	  }catch(Exception e) {
		    		  try {
		    			  lastSum = (Long)cob.get("LINEAMOUNT") + 0d;
		    			
		    		  }catch(Exception t) {
		    			  
		    		  }
		    	  }
		    	 

		    	  Agent agent = (Agent)agents.get(SALESGROUP);
	              if (agent != null && isCurrentMonth(getDate(idate)) ){
	            	  Double ll = lastSum;
		                 if(salesbyinv.get(SALESGROUP + ";" +custid + ";" + SALESID) != null) {
		                	  ll = salesbyinv.get(SALESGROUP + ";" +custid + ";" + SALESID) + lastSum;
		                 }
	            	    sb1.append(SALESGROUP + ";" +  custid + ";" + SALESID + ";" + toode + ";" + lastSum + "\n");
	            	  
	            	  if(sales.get(SALESGROUP) != null) {
	            		lastSum = lastSum + sales.get(SALESGROUP);  
	            	  }
	            	  sales.put(SALESGROUP, lastSum);
	            	
	                 
	                 salesbyinv.put(SALESGROUP + ";" +custid + ";" + SALESID, ll);
	                 
	              }
                 if (agent != null && isLastMonth(getDate(idate)) ){
	            	  
	            	  if(saleslast.get(SALESGROUP) != null) {
	            		lastSum = lastSum + saleslast.get(SALESGROUP);  
	            	  }
	            	  saleslast.put(SALESGROUP, lastSum);
	               
	               
	              }
		    	  }
                 // System.out.println("Sales Last " + " " + isLastMonth(getDate(idate))+ " " + idate + " "+ saleslast.toString() + " ");
  		            	  }
	       	          }
		          }
			  	   }catch(Exception e){
					e.printStackTrace();
				}
		StringBuffer ss = new StringBuffer();
		Iterator iterax = salesbyinv.keySet().iterator();
		while (iterax.hasNext()) {
			String ag = iterax.next()+"";
			ss.append(ag+ ";" +salesbyinv.get(ag)+ "\n");
		}
			
			
		
		Iterator itera = sales.keySet().iterator();
		
		while (itera.hasNext()) {
			String ag = itera.next()+"";
			  Agent agent = (Agent)agents.get(ag);
              if (agent != null ){
            	  agent.setMonthSales((int)(sales.get(ag)+0d));
            	//  System.out.println("myyk " +  ag + " :" + (int)(sales.get(ag)+0d));
              }
		
		}
		
   itera = saleslast.keySet().iterator();
		
		while (itera.hasNext()) {
			String ag = itera.next()+"";
			  Agent agent = (Agent)agents.get(ag);
              if (agent != null ){
            	  agent.setStart("Eelmine kuu : " + (int)(saleslast.get(ag)+0d) + " eur");
              }
		}
		
		
	/*	if(sb1.length() > 0) {
			Converter.saveFile("ilus.txt", sb1.toString());
		}
		String kuu = (Calendar.getInstance().get(Calendar.MONTH)+1)+"";
		if(ss.length() > 0) {
			Converter.saveFile("ilus2-" + kuu  + ".txt", ss.toString());
		}*/
		
		cpDone = true;
     	}
	

	private boolean isLastMonth(Date kuup) {
		
		Calendar cal = Calendar.getInstance();
		Calendar dd = Calendar.getInstance();
		dd.setTime(kuup);
		cal.add(Calendar.MONTH, -1);
		
		if(cal.get(Calendar.MONTH) == dd.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == dd.get(Calendar.YEAR)) {
			return true;
		}
		
		
		return false;
	}

	
	private boolean isCurrentMonth(Date kuup) {
		
		Calendar cal = Calendar.getInstance();
		Calendar dd = Calendar.getInstance();
		dd.setTime(kuup);
		
		if(cal.get(Calendar.MONTH) == dd.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == dd.get(Calendar.YEAR)) {
			return true;
		}
		
		
		return false;
	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}
	
	public String getProductSortingData(){
	   	return null;
	}
	 
	public String getAssortmentStr(Agent agent) {
		return null;
	}
	
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Vector getDebt(String agentCode) {
		return dlist;
	}
	
	private Vector dlist = null;
	
	 private void makeShipModeFile(String countries) {
		 
		 StringBuffer sb1 = new StringBuffer();
		   TreeMap<String,String> vahe = new TreeMap<String,String>();
		 final File  v  = new File("shmodejson.txt");
	          
	    	   try{
	    	   URL urlst = new URL(jsonshmode);
				Converter.trustAllHttpsURLConnection();
				Converter.doTrustToCertificates();
			URLConnection con = urlst.openConnection();
			InputStream is = con.getInputStream();
			OutputStream os = new FileOutputStream(v);
			int i;
			while ( (i = is.read()) != -1) {
				os.write(i);
			}
			is.close();
			os.close();
	    	   }catch(Exception w){
			    	w.printStackTrace();	
			       }
	    	   System.out.println("SHModeJson lopetan ");
	    	   try{	   
	    	 
	    	   
			JSONParser parser = new JSONParser();
		       Object obj = parser.parse(new FileReader("shmodejson.txt"));
	           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
	           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
	      
	          
	        
	      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
	      while (iterator.hasNext()) {
	    	  org.json.simple.JSONObject cob = iterator.next();
	    	 
	    	  String id = (String)cob.get("reference");
	    	  String nimi = (String)cob.get("name");
	    	  String carr = (String)cob.get("carrier_name");
	    	  String carrreg = (String)cob.get("carrier_reg_no");
	    	  String abi = "";
	    	  if(carr.equals("Eesti Post AS")) {
	    		  abi = "EP ";
	    	  }
	    	  if(carr.toLowerCase().contains("dpd")) {
	    		  abi = "DPD ";
	    	  }
	    	  if(carr.toLowerCase().contains("itell")) {
	    		  abi = "SP ";
	    	  }
	    	  String mode = "box";

	    	  String country =  (String)cob.get("country");
	    	  if(countries.contains(country)) {
	    		  String data = id+ ";" + abi + nimi + ";"+ mode + ";" + carr + ";" + carrreg ;
	    		  vahe.put(abi+nimi, data);
	    	  }
	    	 /*   "id": "2351",
	    	      "reference": "88854",
	    	      "name": "Alytaus PC ARENA pa\u0161tomatas (PERPILDYTAS)",
	    	      "carrier_name": "Eesti Post AS",
	    	      "carrier_id": "267",
	    	      "carrier_reg_no": "10328799",
	    	      "address_row_1": "\u016adrijos g. 1",
	    	      "city": "Alytaus m. sav.",
	    	      "postcode": "62411",
	    	      "country": "LT"
	    	    },*/
	        }
		 
	    	   }catch(Exception w){
			    	w.printStackTrace();	
			       }
		 try{
			 
			 //lisame kullerid 
			 vahe.put("KUL Itella Estonia", "kul1;KUL Itella Estonia;dtd;Itella;10891224" );
			 // vahe.put("KUL DPD Eesti", "kul1;KUL DPD Eesti;dtd;DPD;10092256" );
			  vahe.put("KUL SendPack", "kul2;KUL SendPack;dtd;SEP;12703132" );
			 
			Iterator itera =  vahe.values().iterator();
			while (itera.hasNext()) {
				sb1.append(itera.next()+ "\n");
			}
		        if(sb1.toString().length() >0) {
		        	  Writer out = new BufferedWriter(new OutputStreamWriter(
		  	  			    new FileOutputStream("shipmode.txt"), "UTF-8"));
		  	  			try {
		  	  			    out.write(sb1.toString());
		  	  			} finally {
		  	  			    out.close();
		  	  			}
		        }
	       	
	        } catch(Exception r) {
	        	
	        }
	 }
	
	 private TreeMap  getOpenOrders(){
		 
		 TreeMap<String, ArrayList<Toode>> docs = new TreeMap<String, ArrayList<Toode>>();
			TreeMap res = new TreeMap();
			StringBuffer oorders = new StringBuffer();
			oorders.append(Calendar.getInstance().getTimeInMillis() + "\n");
			if(jsonopen0 != null) {
			try {
				
				String urlStr = null;
				
				 for(int u = 0 ;u < 5;u++){
			 		if(u==0){
			 			urlStr = this.jsonopen0;
			 		}
			 		if(u==1){
			 			urlStr = this.jsonopen1;
			 		}
			 		if(u==2){
			 			urlStr = this.jsonopen2;
			 		}
			 		if(u==3){
			 			urlStr = this.jsonopen3;
			 		}
			 		if(u==4){
			 			urlStr = this.jsonopen4;
			 		}
			 		System.out.println("OpenJson alustan ");

				final File  v  = new File("openjson"   + u+ ".txt");
			       if(urlStr != null){       
			    	   try{
			    	   URL urlst = new URL(urlStr);
						Converter.trustAllHttpsURLConnection();
						Converter.doTrustToCertificates();
					URLConnection con = urlst.openConnection();
					InputStream is = con.getInputStream();
					OutputStream os = new FileOutputStream(v);
					int i;
					while ( (i = is.read()) != -1) {
						os.write(i);
					}
					is.close();
					os.close();
			    	   }catch(Exception w){
					    	w.printStackTrace();	
					       }
			    	   System.out.println("OpenJson lopetan ");
			    	   
			    	 
			    	   
					JSONParser parser = new JSONParser();
				       Object obj = parser.parse(new FileReader("openjson" + u + ".txt"));
			           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
			           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
			      
			          
			           
			      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
			      while (iterator.hasNext()) {
			    	  org.json.simple.JSONObject cob = iterator.next();
			    	 
			    	  String id = (String)cob.get("EDICUSTACCOUNT");
			    	  String arve = (String)cob.get("SALESID");
			    	  String toode = (String)cob.get("ITEMID");
			    	  String nimi = (String)cob.get("NAME");
			    	  double kogus =  0;
			    	  try {
			    		  kogus =  (Double)cob.get("QTYORDERED"); 
			    	  }catch(Exception e) {
			    		  kogus =   (Long)cob.get("QTYORDERED");
			    	  }
			    	  String kuup =  ((String)cob.get("SHIPPINGDATEREQUESTED")).substring(0,10);

			    	  String kuup2 =  ((String)cob.get("CONFIRMEDDLV")).substring(0,10);
			    	  
			    	  
			    	  Toode tt = new Toode(toode);
			    	  tt.setBrand(nimi);
			    	  tt.setKogus(kogus);
			    	  tt.setGr1(arve);
			    	  tt.setGr2(kuup2);
			    	  
			    	  
			    	  java.util.Date dd = getDate(kuup);
			    	  boolean ddOK = dd.after(new java.util.Date()) || isToday(dd.getTime()) ;
			    	  java.util.Date dd2 = getDate(kuup2);
			    	  boolean dd2OK = dd2.after(new java.util.Date()) || isToday(dd2.getTime()) ;
			    	  if(ddOK || dd2OK) {
			    		
			    	  ArrayList<Toode> pr = new ArrayList<Toode>();
			    		if(docs.get(id) != null) {
			    			 pr = docs.get(id);
			    		}
			    		pr.add(tt);
			    		if(id != null && id.trim().length() > 0) {
			    			docs.put(id, pr);
			    		}
			    		
			    	  }
			      }
			      
			      StringBuffer sb = new StringBuffer();
			      
			    Iterator itera =   docs.keySet().iterator();
			      
			    while (itera.hasNext()) {
			    	String id = (String)itera.next();
			    	ArrayList<Toode> ar = docs.get(id);
			    	String kuup = null;
			        String numb = null;
			    	String text = "";
			        
			    	for (int i = 0 ; i < ar.size(); i++) {
			    		Toode tt = ar.get(i);
			    		kuup = tt.getGr2();
			    		numb = tt.getGr1();
			    		text = text + "[" + NF.format(tt.getKogus()) + "] " 
						       + " " + tt.getBrand().trim() + "|";
					}
			    	
			    	 sb.append(numb + "  " + kuup + "|");
					 sb.append("|" + text);
			    	  
				     res.put(id, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
				   	 sb = new StringBuffer();
					  oorders.append(id + ";" + ar.size() + ";" + kuup + "\n" );
			        }
			       }
			      }
				 } catch (Exception e) {
			 		  getParent().showError(e);
			 	 }
			}
			
	   if(parent.getProps().getProperty("savefilesdir") != null) {
		   Converter.saveFile(parent.getProps().getProperty("savefilesdir")  + "customerorders.txt", oorders.toString());   
	   }
	   if(parent.getProps().getProperty("savefilesafteremail") != null) {
		   Converter.saveFile(parent.getProps().getProperty("savefilesafteremail")  + "customerorders.txt", oorders.toString());   
	   }
     	  	
			
			return res;
		}
		
	
	 public static boolean isToday(long lll){
	    	Calendar cc = Calendar.getInstance();
	    	cc.set(Calendar.HOUR_OF_DAY, 1);
	    	if(cc.getTime().getTime() < lll){
	    		return true;
	    	}else{
	    		return false ;	
	    	}   	
	    }
	
	
	public Vector getDebt2(HashMap custMap) {
	 dlist = new Vector();
		if(jsondebt0 != null) {
		
		try {
			
			String urlStr = null;
			
			 for(int u = 0 ;u < 4;u++){
		 		if(u==0){
		 			urlStr = this.jsondebt0;
		 		}
		 		if(u==1){
		 			urlStr = this.jsondebt1;
		 		}
		 		if(u==2){
		 			urlStr = this.jsondebt2;
		 		}
		 		if(u==3){
		 			urlStr = this.jsondebt3;
		 		}
		 		if(u==4){
		 			urlStr = this.jsondebt4;
		 		}
		 		System.out.println("DebtJson alustan ");

			final File  v  = new File("debtjson"   + u+ ".txt");
		       if(urlStr != null){       
		    	   try{
		    	   URL urlst = new URL(urlStr);
					Converter.trustAllHttpsURLConnection();
					Converter.doTrustToCertificates();
				URLConnection con = urlst.openConnection();
				InputStream is = con.getInputStream();
				OutputStream os = new FileOutputStream(v);
				int i;
				while ( (i = is.read()) != -1) {
					os.write(i);
				}
				is.close();
				os.close();
		    	   }catch(Exception w){
				    	w.printStackTrace();	
				       }
		    	   System.out.println("DebtJson lopetan ");
				JSONParser parser = new JSONParser();
			       Object obj = parser.parse(new FileReader("debtjson" + u + ".txt"));
		           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
		           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
		      
		      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
		      while (iterator.hasNext()) {
		    	  org.json.simple.JSONObject cob = iterator.next();
		    	 
		    	  String id = (String)cob.get("EDICUSTACCOUNT");
		    	  String arve = (String)cob.get("INVOICE");
		    	  String payer = (String)cob.get("ACCOUNTNUM");
		    	  double summa =  0;
		    	  try {
		    		 summa =  (Double)cob.get("AMOUNTMST"); 
		    	  }catch(Exception e) {
		    		  summa =   (Long)cob.get("AMOUNTMST");
		    	  }
		    			  
		    			
		    	  String kuupv =  (String)cob.get("DUEDATE");
		    	 
		    			  
		    	  Debt debt = new Debt();
		    		debt.setCustID(id);
		    		debt.setPayerID(payer);
					debt.setDoknr(arve);
					debt.setDuedate(getDate(kuupv));
					debt.setSum(summa);

					Calendar cal = Calendar.getInstance();
			    	  cal.setTime(getDate(kuupv));
			    	  try {
			    		  if(custMap.get(id) != null) {
			    			  
			    			  Customer cc = ((Customer)custMap.get(id));
			    			  
			    			  //System.out.println("cust " + cc);
				    		  cal.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(cc.getPaymTerm()));
				    		  debt.setDate(cal.getTime());
				    		  int due = Converter.getDifferenceDays( debt.getDuedate(), new Date(Calendar.getInstance().getTimeInMillis()));
				    		  debt.setDue(due +"");
				    		  
				    		  if(due > 0  &&  debtShow(payer)) {
				    			  cc.addDebt(summa);
				    		  }
				    			  
				    	  }  
			    	  }catch(Exception e) {
			    		  e.printStackTrace();
			    	  }
			    	
			    	  
					// System.out.println("uhuu " + id + " " + debt.getDate() + "\n" + debt.getDuedate() );
				    	
					
		    	   dlist.addElement(debt);
		    			
		    	  }
		      }
		      
		       }
			 
		      	   }catch(Exception e){
					e.printStackTrace();
				}
		}
		return dlist;
	}

	
	private boolean debtShow(String id ) {
		if(id.startsWith("Prisma") || id.startsWith("Selver") || id.startsWith("Comarket") || id.startsWith("Kesko")  || id.startsWith("Statoil")
				|| id.startsWith("T-Market")) {
			return false;
		}
			
		return true;	
	}
	
	public String getDistribution() {
		return null;
	}

	private TreeMap<String,String> laduBR = null;

	private TreeMap<String,String> laduParimEnne = null;

        public String getSaveStockInfo(){
        	StringBuffer products = new StringBuffer();
        	laduBR = new TreeMap<String,String>();
        	laduParimEnne = new TreeMap<String,String>();
        	TreeMap<String,Double> seisudSum = new TreeMap<String,Double>();
				    try {
				    
				    	String urlStr = this.jsonstock0;
						
				    	int u =  0;
				    	System.out.println("StockJson alustan ");
						final File  v  = new File("stockjson"   + u+ ".txt");
					       
						if(urlStr != null){
						
					    try{
							URL urlst = new URL(urlStr);
								Converter.trustAllHttpsURLConnection();
								Converter.doTrustToCertificates();
							URLConnection con = urlst.openConnection();
							InputStream is = con.getInputStream();
							OutputStream os = new FileOutputStream(v);
							int i;
							while ( (i = is.read()) != -1) {
								os.write(i);
							}
							is.close();
							os.close();
					       }catch(Exception w){
						    	w.printStackTrace();	
						       }
						
					    
						}
						System.out.println("stockJson lopp ");
				    	String laodq = getParent().getProps().getProperty("laoseis.laod");
				    	//võtame välja ainut yhe laoseisu 
				    	String ladu1 = null;
				    	String ladu2 = null;
				    	String ladu3 = null;
				    	if(laodq != null && laodq.indexOf(",") != -1) {
				    		StringTokenizer st = new StringTokenizer(laodq,",");
				    		if(st.hasMoreTokens()) {
                             ladu1 = st.nextToken();
				    		}
				    		if(st.hasMoreTokens()) {
	                             ladu2 = st.nextToken();
					    		}
				    		if(st.hasMoreTokens()) {
	                             ladu3 = st.nextToken();
					    		}
				    	}
				    	
        	System.out.println("ladu 1 " + ladu1);
        	JSONParser parser = new JSONParser();
		       Object obj = parser.parse(new FileReader("stockjson" + u + ".txt"));
	           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
	           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
	      
	      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
	      while (iterator.hasNext()) {
	    	  org.json.simple.JSONObject cob = iterator.next();
	    	 
	    	  String prodid = (String)cob.get("Kaubakood");
	    	  String ladu = (String)cob.get("Ladu");
	    	  try {
	    		  double nn = Double.parseDouble(((Long)cob.get("Laoseis"))+"");

					if( nn > 0 ){

						String parim = (String)cob.get("S\u00e4ilivusaeg");
				    	  
						if(laodq == null) {
							
							   products.append(prodid +  ";");
				               products.append( ladu +";");
				               products.append(getParent().getNF().format(nn) + "; "  +  "\n"); // + dealer +  "\n");
				               
						  } else if (ladu1.equals("." + ladu + ".") || (ladu3 != null && ladu3.equals("." + ladu + ".")) ) {
						     if(parim != null) {
						    	 String asi = (String)laduParimEnne.get(prodid);
						    	 if(asi == null) {
						    		 asi = "BB:" + parim +" Ls:" + nn;  
						    	 }else {
						    		 asi = asi + "|BB:" + parim +" Ls:" + nn; 
						    	 }
						    	 laduParimEnne.put(prodid, asi);
						     }
							if(seisudSum.get(prodid) != null) {
								nn = nn + Double.parseDouble(seisudSum.get(prodid) + "");
							}
							seisudSum.put(prodid, nn);
						} else if ( ladu2.equals("." +ladu+ ".") ) {
							laduBR.put(prodid, "'" + ladu + "@" + (int)nn + "'");
						}
						      
				     }
	    	  }catch(Exception f) {
	    		//f.printStackTrace();  
	    	  }
	    	
	       }
	  	
	      
	    Iterator itera = seisudSum.keySet().iterator();
	    
	    while(itera.hasNext()) {
	    	String prodid = itera.next()+"";
	    	double seis = seisudSum.get(prodid);
	    	
	    	 products.append(prodid +  ";");
             products.append(  " ;");//ladu + 
             products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
	    }
	      
				    }catch(Exception e) {
				    	e.printStackTrace();
				    }
				    
				    System.out.println("laduBR " + laduBR.size());
				    System.out.println("laduParimEnne " + laduParimEnne.size());
				    
			   return products.toString();
		   }


        
	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(5, 7));
		int DAY = Integer.parseInt(kuup.substring(8, 10));
		cal.set(YEAR, MONTH -1, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}

	public void connectionDown() {
	}

	public void connectionUp() {
	}

	 private TreeMap agent1 = new TreeMap();
	
	public TreeMap getAgents() {
		TreeMap tm = new TreeMap();
		try {
			loadLisaInfo();
			getProdGroups2();
			String file = "agent.txt";
			String add = getParent().getProps().getProperty("add");

			if (add == null || add.trim().length() == 0) {
				add = "";
			}

			FileReader fr = new FileReader(getStatDir() + add + file);
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {

				// System.out.println(line);
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					Agent agent = new Agent(id);
					
					//System.out.println(id);
					if (st.hasMoreTokens()) {
						String name = st.nextToken().trim();
						//System.out.println(name);
						if (name.equals(""))
							name = "<" + id + ">";
						agent.setName(name);

						agent.setLanguage("EST");
				          agent.setCountry("ee");
				          agent.setPassWord("0000");
				          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
				          if(email.length() > 1){
				        	  agent.setEmail(email);
				        	  agent.setActive(true);
				          }
				         
				          if(lisaagendigrupid != null) {
				        	  ArrayList<String> grs = lisaagendigrupid.get(agent.getID());
				        	  if(grs != null) {
				        		  agent.prodGroupList = grs;
				        		  System.out.println(grs.toString());
				        		
				        	  }else {
				        		  agent.addProdGroupList("all");
				        	  }
				          }
				         
						
					}
					
					agent1.put(agent.getID(), agent);
					tm.put(agent.getName(), agent);
				}
			}
      reader.close();
			
			String file2 = "dubbler.txt";
			File ff = new File(getStatDir() + add + file2);
			if(ff.exists()){		
			 fr = new FileReader(getStatDir() + add + file2);
			 reader = new BufferedReader(fr);
		 line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					
					Iterator iter = tm.values().iterator();
					while(iter.hasNext()){
						Agent agent = (Agent)iter.next();
						if(agent.getID().equals(id)){
							while (st.hasMoreTokens()) {
								String dubler = st.nextToken().trim();
								agent.addDubbler(dubler);
							//	System.out.println("Lisan Dublanti " + id + " " + dubler );
							}
						}	
					}
					
				}
			}
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(rcompany != null){
	    	//Converter.sendAgentJson(tm, rcompany, false);
	    }
		
	//	setBulkSales(agent1);
		
		return tm;
	}

	
	
	
	 
      
      public  String getDateDDkMMkYYYYStr(java.util.Date cc){
			StringBuffer sb = new StringBuffer();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cc.getTime());
			
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}
			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day +"-");
			sb.append(month + "-");		
			sb.append("" + cal.get(Calendar.YEAR));
		
			return sb.toString();
		}
      
      public TreeMap getProdGroupsSecond() throws SQLException {
          TreeMap res = new TreeMap();
  		if ( "TB".equals(getParent().getProps().getProperty("stockcode"))) {
    	  ArrayList arr = new ArrayList();
    	  try {
			Converter.readFile(new File("prodgrsec.txt"), "UTF-8", arr);
			for(int i = 0; i< arr.size();i++) {
				String line = (String)arr.get(i);
				if(line != null && line.trim().length() > 3 && line.contains(";")) {
					res.put(line.substring(0,line.indexOf(";")), line.substring(line.indexOf(";")+1));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		}
  		System.out.println("res "+ res);
          return res;
    }
      
      public TreeMap getProdGroups(){
    	  return grs;
      }
      TreeMap grs = new TreeMap();
      
	public TreeMap getProdGroups2(){
		grs = new TreeMap();
	
		try {
	
	String urlStr = null;
	
	 for(int u = 0 ;u < 5;u++){
 		if(u==0){
 			urlStr = this.jsonprgroup0;
 		}
 		if(u==1){
 			urlStr = this.jsonprgroup1;
 		}
 		if(u==2){
 			urlStr = this.jsonprgroup2;
 		}
 		if(u==3){
 			urlStr = this.jsonprgroup3;
 		}
 		if(u==4){
 			urlStr = this.jsonprgroup4;
 		}

	final File  v  = new File("prgroupjson"   + u+ ".txt");
       if(urlStr != null){       
    	   try{
    	   URL urlst = new URL(urlStr);
			Converter.trustAllHttpsURLConnection();
			Converter.doTrustToCertificates();
		URLConnection con = urlst.openConnection();
		InputStream is = con.getInputStream();
		OutputStream os = new FileOutputStream(v);
		int i;
		while ( (i = is.read()) != -1) {
			os.write(i);
		}
		is.close();
		os.close();
    	   }catch(Exception w){
		    	w.printStackTrace();	
		       }
		
		JSONParser parser = new JSONParser();
	       Object obj = parser.parse(new FileReader("prgroupjson" + u + ".txt"));
           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
      
      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
      while (iterator.hasNext()) {
    	  org.json.simple.JSONObject cob = iterator.next();
    	 
    	  String idd = (String)cob.get("PRODUCTGROUPID");
    	  String name = (String)cob.get("DESCRIPTION");
    	  name = name.replaceAll("Kommikarbid", "Karbid");
    	  name = name.replaceAll("Kommikotid", "Kotid");
    	  name = name.replaceAll("Kaalukompvekid", "Kaalu");
    	  name = name.replaceAll("Shokolaadibatoonid", "Batoonid");
    	  name = name.replaceAll("Shokolaaditahvlid", "Tahvlid");
    	  name = name.replaceAll("Sweet Home Backing", "Küpsetus");
    	  name = name.replaceAll("Hooajatooted", "Hooaeg");
    	 
    	  String pre = "";
    	  if(u == 3) {
    		  pre = "GR ";
    	  }
    			  grs.put(idd, pre + Converter.enCodeEst(name));
    			
    	  }
      }
      
       }
	 
      	   }catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
		if(lisagrupid != null) {
			for(String gr : lisagrupid) {
				if(gr != null)
				grs.put(gr,"yy " + gr);
			}
		}
		
		return grs;
	}
	
	
	private TreeMap getGroups(){
		 TreeMap<String, String> map = new TreeMap<>();
		 if("true".equals(getParent().getProps().getProperty("webprodgroups"))) {
		
		JSONArray arr =  Converter.getWebProducts(rcompanyx, user, pass);

		Converter.saveFile("webprods.json", arr.toString());
	   //	 System.out.println("arr " + arr);
	   	 int lennx = arr.length();
				for (int u = 0; u < lennx; u++) {
	        	JSONObject gr = null;
				try {
					gr = arr.getJSONObject(u);
					String prodID = (String)gr.get("code");
					JSONArray key = (JSONArray)gr.get("group");
					JSONObject grr = (JSONObject)key.get(0);
					String grcode = (String)grr.get("code");
					map.put(prodID, grcode);
				}catch(Exception e) {
				  e.printStackTrace();	
				}
			//	System.out.println("map " + map);		
				}
		 }else {
				
	        try (BufferedReader br = new BufferedReader(new FileReader(getStatDir() + "tootegrupid.txt"))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                String[] tokens = line.split(";");
	                if (tokens.length >= 3) {
	                    String key = tokens[0].trim();
	                    String value = tokens[2].trim();
	                    map.put(key, value);
	                }
	            }
	        } catch (IOException e) {
	           // e.printStackTrace();
	        }	
		 }
		return map;
	}
	
	
	private TreeMap getCustAgsExcel() {
		TreeMap asi = null;
	
		
File f = new File(this.getExcelDir() + "Partnerid.xls");
		
		try {
			
		
			// System.out.println("rownum exists" + f.exists() );
				
			
	 if(f.exists()){
		 
		 
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			
			
			
	
			for (int a = 0; a <= sheet.getLastRowNum(); a++) {
	           try {
	        	   row = sheet.getRow(a);
	        	   if(asi == null) {
	        		   asi = new TreeMap();
	        	   }
	        	  String custid =  (row.getCell((short) 0).getStringCellValue());
	        	  String agent =  Converter.cleanLitLatEst(row.getCell((short) 13).getStringCellValue());
		        	StringTokenizer st = new StringTokenizer(agent,";");
		        	String uu = "";
		        	while (st.hasMoreTokens()){
		        		
		        		String aa = st.nextToken();
		        		if(aa.indexOf(":") != -1) {
		        			aa = aa.substring(0,aa.indexOf(":"));
		        			uu = uu + ":" + aa;
		        		}
		        		
		        		asi.put(custid, uu);
		        		
		        	}
		        	
		        	try {
		        		  String phone =  Converter.cleanLitLatEst(row.getCell((short) 5).getStringCellValue());
		        		  if(phone != null && phone.length() >0) {
		        			  asi.put(custid+"phone", phone);
		        			  System.out.println("PH "+ phone);
		        		  }
		        	}catch(Exception e) {
		        		
		        	}
		        	
		        	try {
		        		String email =  Converter.cleanLitLatEst(row.getCell((short) 7).getStringCellValue());
		        		  if(email != null && email.length() >0) {
		        			  asi.put(custid+"mail", email);
		        			  System.out.println("EM "+ email);
		        		  }
		        	}catch(Exception e) {
		        		
		        	}
		        	try {
		        		 String name =  Converter.cleanLitLatEst(row.getCell((short) 17).getStringCellValue());
		        		  if(name != null && name.length() >0) {
		        			  asi.put(custid+"name", name);
		        			  System.out.println("NM "+ name);
		        		  }
		        	}catch(Exception e) {
		        		
		        	} 
		        	  
		        	  
		        	  
		        	 
	        	  
				}catch(Exception k){
					k.printStackTrace();
				}
			}
	    }

		}catch(Exception e) {
		e.printStackTrace();	
		}		
		
		return asi ;
	}
	
	HashMap custAgendid = null;
	
	
	public TreeMap getCustomers(Agent agent) {

        
        
        HashSet contacted = new HashSet();
        
        if ("true".equals(getParent().getProps().getProperty("notcontacted30d"))) { 
        Calendar now = Calendar.getInstance();
   	 long end = now.getTimeInMillis();
   	 now.add(Calendar.DAY_OF_MONTH, -32);
   	 long sta = now.getTimeInMillis();
   	
  try {
    if(rcompanyx != null) {
   	 JSONArray dayr =  Converter.getWebDayReport(sta, end, rcompanyx, user, pass);
   	 
   	// System.out.println("dayr " + dayr);
   	 int lennx = dayr.length();
			for (int u = 0; u < lennx; u++) {
        	JSONObject gr = null;
			try {
				gr = dayr.getJSONObject(u);
				 String key = (String)gr.get("custid");
				
	           String agentname = null;
			    try {
				  agentname = (String)gr.get("agent");
				  agentname = agentname.replaceAll("\t", ",");
			     }catch (Exception e){
			     }
			    
			    if(agentname != null && !agentname.contains("MERC")) {
			    	 contacted.add(key);
			    }
			
			} catch (JSONException e) {
			  e.printStackTrace();
			}
        
			}}
  
        }catch(Exception e){
         	
        }
			System.out.println("contacted "+ contacted.size() ); 
			 
     }
        
        

        try{
        	if(rcompanyx != null) {
        		//if(!"false".equals(getParent().getProps().getProperty("webagentreplacement"))) {
     	   Converter.getManagersReplacements(agent1, rcompanyx, user, pass, false); 
        		//}
        	}
     }catch(Exception e){
     	
     }
        
        try {
			fixed =  (Vector) Converter.readObject("fix.vec");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        
        
        if ("true".equals(getParent().getProps().getProperty("shipmodefile"))) {
			makeShipModeFile("EE");
		}
        
        String turnAgents = getParent().getProps().getProperty("turnagents");
		
        
      TreeMap  oOrders = this.getOpenOrders();
        
        custAgendid = new HashMap();
		
		if(pmLines == null) {
			getPriceMatrixData();
		}
	
		TreeMap custAgs = null;
		if ( "17".equals(getParent().getProps().getProperty("stockcode"))) {
			custAgs = getCustAgsExcel();
		}

		TreeMap custAgsWeb = null;
		if ("true".equals(getParent().getProps().getProperty("webcustagents"))) {
			custAgsWeb = Converter.getWebCustomersAgents(rcompanyx, user, pass);
			//System.out.println("custAgs " + custAgs.toString());
		}
		
		TreeMap tm = new TreeMap();
     	custMap = new HashMap();
		this.customers = new ArrayList();
		
		try{
			String urlStr = null;
		     

			 for(int u = 0 ;u < 5;u++){
		 		if(u==0){
		 			urlStr = this.jsoncust0;
		 		}
		 		if(u==1){
		 			urlStr = this.jsoncust1;
		 		}
		 		if(u==2){
		 			urlStr = this.jsoncust2;
		 		}
		 		if(u==3){
		 			urlStr = this.jsoncust3;
		 		}
		 		if(u==4){
		 			urlStr = this.jsoncust4;
		 		}

		 		

		    	   System.out.println("custoemrJson alustan ");
		    	   if(!"true".equals(getParent().getProps().getProperty("useolddata"))) {
		    		
			final File  v  = new File("customerjson" + u + ".txt");
		       if(urlStr != null){             
		    	   try{
		    	   URL urlst = new URL(urlStr);
					Converter.trustAllHttpsURLConnection();
					Converter.doTrustToCertificates();
				URLConnection con = urlst.openConnection();
				InputStream is = con.getInputStream();
				OutputStream os = new FileOutputStream(v);
				int i;
				while ( (i = is.read()) != -1) {
					os.write(i);
				}
				is.close();
				os.close();
			
		       }catch(Exception w){
		    	w.printStackTrace();	
		       }		
		    	   
		    	   System.out.println("custoemrJson lõpetasin ");
		    	   
	    	   }
		    	   }
			       File ft = new File("customerjson" + u + ".txt");
			       if(ft.exists()) {
				JSONParser parser = new JSONParser();
			       Object obj = parser.parse(new FileReader("customerjson" + u + ".txt"));
		           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
		           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
		      
		      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
		      while (iterator.hasNext()) {
		    	  org.json.simple.JSONObject cob = iterator.next();
		    	 
		    	  String fid = (String)cob.get("MMFID");
		    	  String id = (String)cob.get("ACCOUNTNUM");
		    	
		    	  String payer = (String)cob.get("INVOICEACCOUNT");
		    	  String agd = (String)cob.get("SALESGROUP");
		    	  String name = (String)cob.get("DESCRIPTION");
		    	  String aadress = (String)cob.get("ADDRESS");
		    	  String blokk = cob.get("BLOCKED")+"";
		    	  String makse = (String)cob.get("PAYMTERMID") ;
		    	  String linedisc = (String)cob.get("LINEDISC") ;
		    	  
		    	  Agent age = null;
					if (agd != null && agd.trim().length() > 0 && custAgsWeb == null ) {
						age = new Agent(agd);
					}

					Customer customer = new Customer(id);
					customer.setAddress(" ");
					customer.restName = name;
                    customer.setPayer(payer);
                    customer.setGroup(payer);
					name = name + " " + id;
						Customer cust = (Customer) tm.get(name);
						if (cust != null) {
								if(age!= null){
								cust.addAgent(age);	
							   tm.put(cust.getName(), cust);
						     }
						}else {
							if (name.equals(""))
								name = "<" + id + ">";
							customer.setName(Converter.enCodeEst(name));
								if (aadress != null) {
									if(aadress.endsWith("EST")) {
										aadress = aadress.substring(0,aadress.lastIndexOf("EST"));
									}
									customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
						            customer.restStreet = aadress;
								}
							

								if(blokk != null && blokk.equals("1")){
									customer.setSalesBlock(true);
								}
								if(blokk != null && blokk.equals("2")){
									customer.setSalesBlock(true);
								}
								if(blokk != null && blokk.equals("3")){
									customer.setSalesBlock(true);
								}
								if(blokk != null && blokk.equals("6")){
									customer.setSalesBlock(true);
								}
								
							
								
								if(makse != null && makse.trim().length() > 0){
									makse = makse.substring(1);
									customer.setPaymTerm(makse);
								}
						
								if(makse.equals("ette")){
								customer.setPaymTerm("-1");
						    	}
							
								 if ("78".equals(getParent().getProps().getProperty("stockcode"))) {
		                    		 customer.setGroup("TTT");
		                    	 }

		                     if(linedisc != null && linedisc.trim().length() > 0) {
		                    	 
		                    	// linedisc = linedisc.substring(0, linedisc.length()-2);
		                    	 
		                    	 if( "78".equals(getParent().getProps().getProperty("stockcode"))) {
		                    		 
		                    		 if(pmCodes.contains(linedisc)) {
		                    			 customer.setGroup(linedisc);
		                    		 }
		                    		
		                    	 }else {
		                    		 customer.setGroup(linedisc);
		                    	 }
		                    	
		                     }
		              
		                     if( custAgsWeb != null && custAgsWeb.get(customer.getID()) != null) {
		     	         		ArrayList arr =  (ArrayList) custAgsWeb.get(customer.getID());
		     	         		for(int i = 0 ; i< arr.size();i++) {
		     	         			String ss = arr.get(i)+""; 
		     	         		   customer.addAgent(new Agent(ss));
		     	         		   if (ss != null && agent1.get(ss.trim()) != null){
               						Agent aa = (Agent)agent1.get(ss.trim());
               					
               						Vector vec = aa.getDubbler();
               						if(vec!= null && vec.size() >0){
               							Enumeration en = vec.elements();
               							while(en.hasMoreElements()){
               								String ag = (String) en.nextElement();
               								customer.addAgent(new Agent(ag));
               							}
               						}
               					}
		   	 			       	}
		     	         		
		     	         		
		     	         		
		     	         	}else if ("17".equals(getParent().getProps().getProperty("stockcode"))
		                			 && custAgs != null) {
		                		// if(customer.getName().startsWith("P"))
		                		 //System.out.println("acust " + customer.getID()+ " " + custAgs.get(customer.getID()));
		                		 String info = (String)custAgs.get(customer.getID()+"phone");
		                		 String nam = (String)custAgs.get(customer.getID()+"name");
		                		 String mail = (String)custAgs.get(customer.getID()+"mail");


		                		 
		                		 
		                		 if (mail != null && mail.trim().length() > 0 ){
		                         	customer.setPersonalData(mail);
		                         	mail = " " + mail.trim();
		                         } else {
		                         	mail = "";
		                         }
		                         String pers  = nam;
		                         if (pers != null && pers.trim().length() > 0 ){
		                         		customer.setPersonalData(pers + mail);
		                         }
		                         
		                         if (info != null && info.trim().length()>2) {
		                         	if(info.trim().startsWith(",")){
		                         		info = info.trim().substring(1);
		                         	}
		                         	if(customer.getPersonalData() != null && customer.getPersonalData().trim().length() > 2) {
		                         	     customer.setPersonalData(customer.getPersonalData() +  "|Tel:" + info.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
		                                  
		                         	}else {
		                         	     customer.setPersonalData( "Tel:" + info.trim().replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " "));
		                                     		
		                         	}
		                          }
		                         
		                        
		                    	String ags = (String)custAgs.get(customer.getID());
		                    	if(ags != null) {
		                    	
		                    		if(ags.startsWith(":")) {
		                    			ags = ags.substring(1);
		                    		}
		                    	
		                    			StringTokenizer st = new StringTokenizer(ags,":");
		                    			while(st.hasMoreElements()) {
		                    				String ss = st.nextToken();
		                    				customer.addAgent(new Agent(ss));	
		                    				if(customer.getAgents().size() == 1 ) {
												custAgendid.put(customer.getID(), ss);
											}
		                    				
		                    				   if (ss != null && agent1.get(ss.trim()) != null){
		                    						Agent aa = (Agent)agent1.get(ss.trim());
		                    					
		                    						Vector vec = aa.getDubbler();
		                    						if(vec!= null && vec.size() >0){
		                    							Enumeration en = vec.elements();
		                    							while(en.hasMoreElements()){
		                    								String ag = (String) en.nextElement();
		                    								customer.addAgent(new Agent(ag));
		                    							}
		                    						}
		                    					}
		                    			}
		                    		
		                    	}
		                    		
		                    	
		                     } else if(age != null ){
		                    	 if(!age.equals("0000"))
									customer.addAgent(age);
		                    	 customer.setMainAgent(age.getID());
									//customer.addAgent(new Agent("manager"));
								}
								
								
								String ag4 = getParent().getProps().getProperty("defaultagent");
								String ag5 = getParent().getProps().getProperty("defaultagent2");
								String ag6 = getParent().getProps().getProperty("defaultagent3");
								String ag7 = getParent().getProps().getProperty("defaultagent4");
								String ag8 = getParent().getProps().getProperty("defaultagent5");
								
								if(ag4 != null){
									customer.addAgent(new Agent(ag4));		
						     	}
								if(ag5 != null){
				     				customer.addAgent(new Agent(ag5));
				     			}
				     			if(ag6 != null){
				     				customer.addAgent(new Agent(ag6));
				     			}
				     			if(ag7 != null){
				     				customer.addAgent(new Agent(ag7));
				     			}
				     			if(ag8 != null){
				     				customer.addAgent(new Agent(ag8));
				     			}
								
								
								if(fid.equals("51") || fid.equals("TB")){
									
									
									 if (age != null && agent1.get(age.getID().trim()) != null){
                 						Agent aa = (Agent)agent1.get(age.getID().trim());
                 					
                 						Vector vec = aa.getDubbler();
                 						if(vec!= null && vec.size() >0){
                 							Enumeration en = vec.elements();
                 							while(en.hasMoreElements()){
                 								String ag = (String) en.nextElement();
                 								customer.addAgent(new Agent(ag));
                 							}
                 						}
                 					}
									
									
									if (age != null && getParent().getAgentsHash().get(age.getID()) != null){
										customer.setMainAgent(age.getID());	
									}
									
							
								
								}
							
								customer.addAgent(new Agent("HOUSE"));
								
							if (age != null && getParent().getAgentsHash().get(age.getID()) != null){
								Agent aa = (Agent)getParent().getAgentsHash().get(age.getID());
								Vector vec = aa.getDubbler();
								if(vec!= null && vec.size() >0){
									Enumeration en = vec.elements();
									while(en.hasMoreElements()){
										String ag = (String) en.nextElement();
										customer.addAgent(new Agent(ag));
									}
								}
							}
							
							
								 if(oOrders.get(customer.getID()) != null){
					            	 customer.addToCustDataVec(customer.getID() + ";tarasaldo; ;Open Order |" 
					                  +  oOrders.get(customer.getID()));
					              }
							
								
							
					   		customer.addFixSortCode(Converter.cleanLitLatEst(customer.getID()));
					   		customer.suggAssortCode = Converter.cleanLitLatEst(customer.getID());
					   		
					   	 if(!contacted.contains(customer.getID())) {
        	            	 customer.diffBack2 = true;
        	            	 customer.needsToBeVisited = true;
        	            	// System.out.println(" ei ole külas " + customer.getID() );
        	             }
					   		if(tm.get(name) == null) {
					   			tm.put(name, customer);
								this.customers.add(customer.getID());
						        custMap.put(customer.getID(), customer);		
					   		}else {
					   			
					   			if(linedisc != null && linedisc.length() > 0) {
					   			
					   				if(customer.getGroup().trim().length() == 0 ) {
					   					customer.setGroup(linedisc);
								     } else {
										customer.setGroup(customer.getGroup() + "|" + linedisc);
								   }
					   			}
					   		}
				      }
  		           
		        }
		     
			 }}
		       
		      	   }catch(Exception e){
					e.printStackTrace();
				}
		
		
		//System.out.println("CustVisit ");
		String file2 = "custvisit.txt";
	String	add = getParent().getProps().getProperty("add");

		if (add == null || add.trim().length() == 0) {
			add = "";
		}
   try{
	   FileReader	fr = new FileReader(getStatDir() + add + file2);
	   BufferedReader reader = new BufferedReader(fr);
		 String line = null;
	while ((line = reader.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(line, ";");
	    if (st.hasMoreTokens()) {
		String id = st.nextToken().trim();
		Customer abi = (Customer)custMap.get(id);
		if (abi != null){

		//	System.out.println("CustVisit1 ");
		if (st.hasMoreTokens()) {
     		String visit = st.nextToken();
     		if (visit != null && visit.trim().length()>0){

 				//abi.setVisit(Converter.visitTurn(visit));
 				abi.setVisit((visit)); //2206 läheb nii
 				
     			if(turnAgents != null) {
     				StringTokenizer s1 = new StringTokenizer(turnAgents,",");
     				while (s1.hasMoreTokens()) {
     					String aa = s1.nextToken().trim();
     					if(abi.hasAgent(aa)) {
     						//abi.setVisit((visit));
     	         			abi.setVisit(Converter.visitTurn(visit));		
     	     			}
     				}
     			}
     			//turnagents=KIR51,TAL51,RAU51,LAA51,POH51,PAR51,LOU51
     		 }
		   }
		  }
         }
        }
		}catch (Exception e){
     	//  e.printStackTrace();
        }				
		
   getDebt2(custMap);		
		System.out.println("klientide suurus " + tm.size() );
		
		 if(rcompany != null){
			// Converter.sendCustomerJson(tm, rcompany, true,user,pass);
		    }

		return tm;

	}
	
	private TreeMap koodiga = null;
	
	public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
            }
        }

        String name = sb.toString().trim();
        
        Pattern pattern = Pattern.compile("\\d+(?:/\\d+)+"); 
        
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                String found = matcher.group(); // the part like 48/24/20
                name = matcher.replaceAll("").replaceAll("\\s{2,}", " ").trim();
            }
        
        return name;
    }
	
	  public static String extractNthPart(String text, int index) {
	        Pattern pattern = Pattern.compile("\\d+(?:/\\d+)+");
	        Matcher matcher = pattern.matcher(text);

	        if (matcher.find()) {
	            String sequence = matcher.group(); // e.g. "48/24/20"
	            String[] parts = sequence.split("/");

	            if (index >= 0 && index < parts.length) {
	                return parts[index].trim();  // return requested part
	            }
	        }
	        return null; // not found or index invalid
	    }

	public TreeMap getProducts(Agent agent) {
		makeFixSortNewProds();
		loadCamp();
		HashSet allowedProds = this.loadAllowedProds();
		koodiga = new TreeMap();
		TreeMap tm = new TreeMap();
		
		TreeMap groups = this.getGroups();
		//System.out.println(groups.toString());
		
		String laod = getParent().getProps().getProperty("laoseis.laod");
		
		if(laod != null) {
			getSaveStockInfo();
		}
		String gronly = null;
		if(getParent().getProps().getProperty("grouponly") != null){
			gronly= getParent().getProps().getProperty("grouponly");
		}
		String gronly12 = null;
		if(getParent().getProps().getProperty("grouponly12") != null){
			gronly12= getParent().getProps().getProperty("grouponly12");
		}
		String codeend = null;
		if(getParent().getProps().getProperty("codeend") != null){
			codeend = getParent().getProps().getProperty("codeend");
		}

	
		String nogroups  = null;
		if(getParent().getProps().getProperty("nogroups") != null){
			nogroups = getParent().getProps().getProperty("nogroups");
		}
		
		String namepart  = null;
		if(getParent().getProps().getProperty("namepart") != null){
			namepart = getParent().getProps().getProperty("namepart");
		}
		String namepart2  = null;
		if(getParent().getProps().getProperty("namepart2") != null){
			namepart2 = getParent().getProps().getProperty("namepart2");
		}
		String namepart3  = null;
		if(getParent().getProps().getProperty("namepart3") != null){
			namepart3 = getParent().getProps().getProperty("namepart3");
		}
		
		String namepartno  = null;
		if(getParent().getProps().getProperty("namepartno") != null){
			namepartno = getParent().getProps().getProperty("namepartno");
		}
		
		String namepartend  = null;
		if(getParent().getProps().getProperty("namepartend") != null){
			namepartend = getParent().getProps().getProperty("namepartend");
		}
		String noprods = null;
		if(getParent().getProps().getProperty("noprods") != null){
			noprods = getParent().getProps().getProperty("noprods");
		}
		
		try {
			
			String urlStr = null;
			
			 for(int u = 0 ;u < 5;u++){
		 		if(u==0){
		 			urlStr = this.jsonprod0;
		 		}
		 		if(u==1){
		 			urlStr = this.jsonprod1;
		 		}
		 		if(u==2){
		 			urlStr = this.jsonprod2;
		 		}
		 		if(u==3){
		 			urlStr = this.jsonprod3;
		 		}
		 		if(u==4){
		 			urlStr = this.jsonprod4;
		 		}
		 		 System.out.println("productJson alustan ");
		 		 if(!"true".equals(getParent().getProps().getProperty("useolddata"))) {
		 			
			
			final File  v  = new File("productjsontemp"   + u+ ".txt");
		       if(urlStr != null){  
		    	   try{
		    	   URL urlst = new URL(urlStr);
					Converter.trustAllHttpsURLConnection();
					Converter.doTrustToCertificates();
				URLConnection con = urlst.openConnection();
				InputStream is = con.getInputStream();
				OutputStream os = new FileOutputStream(v);
				int i;
				while ( (i = is.read()) != -1) {
					os.write(i);
				}
				is.close();
				os.close();
			
		    	   }catch(Exception w){
				    	w.printStackTrace();	
				       }
		    
		    	   
		    	   File  vv  = new File("productjson"   + u+ ".txt");   
		    	   File  uu  = new File("productjsontemp"   + u+ ".txt");  
		    	//   System.out.println("productJson suurus " +  uu.length() );
		    	   if(uu.exists() && uu.length() > 2000) {
		    		 if(vv.exists()) {
		    			   vv.delete();
		   		    }
		    		
		    		 boolean done =   uu.renameTo(new File("productjson"   + u+ ".txt")) ;
		    		// System.out.println("productJson renamed");
		    	   }
		    	   
		    	 
		    	   System.out.println("productJson lõpetan ");
		    	   
		 		 }
		 		 }
		    	   File fe = new File("productjson" + u + ".txt");
		    	   if(fe.exists()) {
				JSONParser parser = new JSONParser();
			       Object obj = parser.parse(new FileReader("productjson" + u + ".txt"));
		           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
		           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
		      
		      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
		      while (iterator.hasNext()) {
		    	  org.json.simple.JSONObject cob = iterator.next();
		    	 
		    	  String fid = (String)cob.get("MMFId");
		    	  String vendorid = (String)cob.get("PrimaryVendorId");
		    	  String id = (String)cob.get("ItemId");
		    	  String name = (String)cob.get("Name");
		    	  String nameOrig = name;
		    	  Double price = 0.0;
		    	  try{
		    		  price = (Double)cob.get("Price");
		    	  }catch(Exception e){
		    		  price = ((Long)cob.get("Price")).doubleValue();
		    	  }
		    	  
		    	  Long inCases = null ;
		    	  try {
		    		  inCases = (Long)cob.get("CaseQty");	  
		    	  }catch(Exception e) {
		    		  e.printStackTrace();
		    	  }
		    	 
		    	  String ean = (String)cob.get("ItemBarcode");
		    	  Long stock = (Long)cob.get("Qty");
			      String unit = (String)cob.get("UnitId");
			      String gr = (String)cob.get("MmfItemProductGroupId");
			      String grPr = (String)cob.get("DiscountGroup");
			      
			      Long onPallet = (Long)cob.get("OnPallet");
		    	 
			      String ette = "";
			      if(laduBR != null && laduBR.get(id) != null) {
			    	 ette =  laduBR.get(id);
			      }
			      name = (name + " " + ette).trim();
			      
		    	//  System.out.println(name);
					
					Product prod = new Product(id);
					if(laduParimEnne != null && laduParimEnne.get(id) != null) {
						prod.setPartii(laduParimEnne.get(id));
					}

					 if (inCases != null && inCases > 0) {
						prod.setInCases(inCases);	
			  		 }
					
					
						 if ("22".equals(getParent().getProps().getProperty("stockcode"))) {
						 	prod.setName( Converter.enCodeEst(id.substring(0,2) + " " + name));	
						 	
						 } else if ("TB".equals(getParent().getProps().getProperty("stockcode"))) {
					
							 int ind = name.indexOf("pant ");
							 if(ind != -1) {
								 name = name.substring(0,ind) + name.substring(ind + 6);
							 }
							 
							 ind = name.indexOf("Pant ");
							 if(ind != -1) {
								 name = name.substring(0,ind) + name.substring(ind + 6);
							 }
							 
							 ind = name.indexOf("PANT ");
							 if(ind != -1) {
								 name = name.substring(0,ind) + name.substring(ind + 6);
							 }
							 
							 
							 
							 if(onPallet != null && onPallet > 1) {
								 try {
								    prod.setOnPallet(onPallet);
								  }catch(Exception e) {
								  }
							 }
							 
							  name = name.replaceAll("alk.", " ");
							  name = name.replaceAll(" vol ", " ");
							  name = name.replaceAll("karb.-mata ", " ");
							  name = name.replaceAll("karb.-tud ", "karb. ");
							  name = name.replaceAll("maitseline ", " ");
							  name = name.replaceAll("%", "°");
							  name = name.replaceAll("karastusjook ", " ");
							  name = name.replaceAll("mahlajook ", " ");
							  name = name.replaceAll("naturaalne", "nat.");
							  name = name.replaceAll("A,C,E", "ACE");
							  name = name.replaceAll("A,D,E", "ADE");
							  name = name.replaceAll("vitamiinidega ", " ");
							  name = name.replaceAll("vitamiinidega", " ");
							  name = name.replaceAll("vitamiiniga ", " ");
							  name = name.replaceAll("karboniseer", "karb.");
							  name = name.replaceAll("Karboniseer", "karb.");
							  name = name.replaceAll("pdl ", " ");
							  name = name.replaceAll("Meistrite Gildi", "MGildi");
							  name = name.replaceAll("MEISTRITE GILDI", "MGildi");
							  name = name.replaceAll("Mõisakeldri", "MKeldri");
							  name = name.replaceAll("MÕISAKELDRI", "MKeldri");
							  name = name.replaceAll("Koola karb.", " ");
							  name = name.replaceAll("koola karb.", " ");
							  name = name.replaceAll(".vesi ", " ");
							  name = name.replaceAll(" vesi ", " ");
							  name = name.replaceAll("Virsiku ", " ");
							  name = name.replaceAll("Roheline ", " ");
name = name.replaceAll("Poolkuiv ", " ");
name = name.replaceAll("õlu ", " ");
name = name.replaceAll("Active ", " ");
name = name.replaceAll("Sidruni ", " ");
name = name.replaceAll("sidrunimahlaga ", " ");
name = name.replaceAll("apelsinimahlaga ", " ");

							  name = name.replaceAll("Õlu ", " ");
							  name = name.replaceAll(" EE ", " ");
							  name = name.replaceAll("Jäätee ", " ");
							  name = name.replaceAll("jäätee ", " ");
							  name = name.replaceAll("ekstraktiga ", " ");
							  name = name.replaceAll("mahl ", " ");
							  name = name.replaceAll("vaba ", " ");
							  name = name.replaceAll("Pint ", " ");
							  name = name.replaceAll("Õlu ", " ");
							  name = name.replaceAll("   ", " ");
							  
							  if(name.contains("kasti EAN")) {
								  name = name.substring(0, name.indexOf("kasti EAN"));
							  }
							  

								// System.out.println("name 0 " + name);
								   
							  name = toTitleCase(name);
							  //System.out.println("name 1 " + name);

							  name = name.replaceAll("Prism", " ");
							  name = name.replaceAll("Pris.", " ");
							  name = name.replaceAll("Pris", " ");
							  name = name.replaceAll("pantc", " ");
							  name = name.replaceAll("pantl D", " ");
							  name = name.replaceAll("Klaaspudel\\+", "pdl");
							  name = name.replaceAll("Purk\\+", "purk");
							  name = name.replaceAll("Pet\\+", "pet");
							  name = name.replaceAll("\\+ ", " ");
							  name = name.replaceAll(" \\+", " ");
							  name = name.replaceAll(" D ", " ");
                              name = name.replaceAll(" K ", " ");
							  name = name.replaceAll("0.5l", "0,5l");
							  name = name.replaceAll("Original", "Orig.");
							  name = name.replaceAll("Tagastatav Taara K", " ");
							  name = name.replaceAll("Klaaspudel", "Klaas K");
							  name = name.replaceAll("Hartwall", "Hartw.");
							  name = name.replaceAll("Strong", "Str.");
							  name = name.replaceAll("Long Drink", "LongD.");
							  name = name.replaceAll("Vaarikas", "Vaar.");
							  name = name.replaceAll("Non-alco", "NonAl.");
							  name = name.replaceAll("Apelsin", "Apel.");
							  name = name.replaceAll("Cranberry", "Cran.");
							  name = name.replaceAll("Lemonade", "Lem.");
							   
							  name = name.replaceAll("\\s+", " ");
							  name = name.trim();
							  
							 prod.setName(Converter.enCodeEst(name + " " + prod.getID()));	
							 prod.setNameSorting(name + " " + prod.getID());
						 

							 if(prod.getName().contains("1/4") || 
							     prod.getName().contains("1/2") || 
								 prod.getName().contains("¼") ||
								 prod.getName( ).contains("½")  ) {
								 prod.setInCases(1);
								 prod.setDiffColor(true);
							 }
						 

							 if(prod.getName().contains(" Alusel") || 
									 prod.getName().contains(" Vaat") ||
									 prod.getName().contains("672 Tk") 
									 ) {
								 prod.setDiffColor(true);
								 prod.setInCases(1);
							 }
							 
							 if(prod.getID().equals("36048TB")) {
								 prod.setDiffColor(true);
							 }
							 
						 }else if ("17".equals(getParent().getProps().getProperty("stockcode"))) {
							 
							 name = name.replaceAll("šokolaad ", "šok. ");
							 name = name.replaceAll("šokolaadi", "šok.");
							 name = name.replaceAll("kommikott ", "kott ");
							 name = name.replaceAll("kommikarp ", "karp ");
							 name = name.replaceAll("küpsised ", "küps. ");
							 name = name.replaceAll("virsiku", "virs.");
							 name = name.replaceAll("täidisega", "täid.");
							 name = name.replaceAll("maitselised", "maits.");
							 name = name.replaceAll("kompvekid", "kompv.");
							 name = name.replaceAll("virsiku", "virs.");
							 
							 if (inCases != null && inCases > 0) {
								if(name.contains("/" + inCases)) {
									name = name.substring(0,name.indexOf("/" + inCases));
								  }
						  		 }
							 
							 prod.setName(Converter.enCodeEst(name));	
						  } else {
						 	 prod.setName(Converter.enCodeEst(name));								 	
						  } 
						 
						 
						 if (u == 1 || u == 2){ // wita tooted ja Lamuu tooted
							
							 prod.setName( Converter.enCodeEst(id.substring(id.length()-2) + " " + name));
						
						 }
						 
						
						 prod.setPrice(price);
							
								String compname = 	prod.getName();
								
										if (ean.length() > 0) {
											prod.setEAN(ean);
                                           if(ean.length() > 4){
                                        	   prod.setName(prod.getName().trim() +  " " + ean.substring(ean.length()-4).trim());
                                           }
										}
										
										
										if (gr.length() > 0) {
												prod.setGroup(gr);
												//prod.setDiscgrupp(gr);
												if ("TB".equals(getParent().getProps().getProperty("stockcode"))) {
													prod.setGroupSecond("Muud");
													if(groups.get(prod.getID()) != null) {
													prod.setGroupSecond((String)groups.get(prod.getID()));
													//System.out.println("TB grupp " + prod.getID() + " " + prod.getGroup());
													}
												}
											}

										if (grPr != null && grPr.length() > 0) {
												prod.setDiscgrupp(grPr+"DG");
											}
										
									
							                prod.setStock(0);
		        
					
									        if(stock > 0){
										        prod.setStock(stock);
									        }
												
								            if (unit != null && unit.trim().length() > 0) {
												prod.setUnit(unit.trim());
										    }
											
							
									boolean goes = true ;

									 //System.out.println("gr " + gr + " " + fid + " " + );
												 
												   if(gronly != null && gronly.indexOf(",") != -1 ){
													   //kui on mitu siis 
													   StringTokenizer stt = new StringTokenizer (gronly ,",");
													   goes = false;
													   while(stt.hasMoreElements()){
														   String gr1 = stt.nextToken();
														 //  System.out.println("gr " + gr);
														    if( gr1.equals(gr)){
														   // 	 System.out.println("gr0 " + gr);
															 goes = true;
														   }
													   }
													   
												   } else if(gronly12 != null   && fid.equals("12")){
													   //kui on mitu siis 
													   StringTokenizer stt = new StringTokenizer (gronly12 ,",");
													   goes = false;
													   while(stt.hasMoreElements()){
														   String gr1 = stt.nextToken();
														 // System.out.println("gr " + gr + " " + gr1);
														    if( gr1.equals(gr)){
														   // 	 System.out.println("gr0 " + gr);
															 goes = true;
															  prod.setName(" 12osak " + prod.getName() );
														   }
													   }
													   
												   }  else if(codeend != null  ){
													   
													
													   //kui on mitu siis 
													   StringTokenizer stt = new StringTokenizer (codeend ,",");
													   goes = false;
													   while(stt.hasMoreElements()){
														   String gr1 = stt.nextToken();
														    if(id.endsWith(gr1)){
															 goes = true;
														   }
													   }
													   
												   }else{
													 //  System.out.println("gr00 " + gr + " " + gronly);
													   if(gronly != null && !gronly.equals(gr)){
														   goes = false;
													   }  
												   }

												   
												   if(nogroups != null) {
													   if(nogroups.contains(gr)) {
														   goes = false;
													   }
												   }
												   

												   if(namepart != null) {
													   goes = false;
													   if(prod.getName().toLowerCase().contains(namepart)) {
														   goes = true;
													   }
												   }
												   
												   if(namepart2 != null) {
													 
													   if(prod.getName().toLowerCase().contains(namepart2)) {
														   goes = true;
													   }
												   }

												   if(namepart3 != null) {
														 
													   if(prod.getName().toLowerCase().contains(namepart3)) {
														   goes = true;
													   }
												   }

												   if(namepartno != null) {
													   if(prod.getName().toLowerCase().contains(namepartno)) {
														   goes = false;
													   }
												   }
												   
												   if(namepartend != null) {
													   if(compname.endsWith(namepartend)) {
														   goes = false;
													   }
												   }
												   
												   if(noprods != null) {
													   if(noprods.contains(prod.getID().substring(0, prod.getID().length()-2))) {
														//   System.out.println("Nimi noprods " + prod.getName());
														   goes = false;
													   }
												   }
												   
												   if(allowedProds.size() > 0) {
													       goes = false;
													   if(allowedProds.contains(prod.getID())) {

															//  System.out.println("Nimi allowed " + prod.getName());
														   goes = true;
													   }
												   }
												   
												   if(goes){
													 //  System.out.println("Nimi " + prod.getName());
													   prod.setAgentGroup("all");
													   prod.setVat("24");
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	   
												   }
		                                           
		                                       }
		                                     }
			                                }
			 
		      	   }catch(Exception e){
					e.printStackTrace();
				   }
		
		
		if(lisatooted != null) {
			for(Product pr : lisatooted) {
				pr.setVat("24");
				try {
				tm.put(pr.getName(), pr);
				koodiga.put(pr.getID(), pr);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
     	return tm;
	}

	boolean cpDone = false;
	
	public Vector getCustomerProductsList(String agentCode) {
		Vector vec = new Vector();
		
		try {
			
			String urlStr = null;
			
			 for(int u = 0 ;u < 5;u++){
		 		if(u == 0){
		 			urlStr = this.jsoncustprod0;
		 		}
		 		if(u == 1){
		 			urlStr = this.jsoncustprod1;
		 		}
		 		if(u == 2){
		 			urlStr = this.jsoncustprod2;
		 		}
		 		if(u == 3){
		 			urlStr = this.jsoncustprod3;
		 		}
		 		if(u == 4){
		 			urlStr = this.jsoncustprod4;
		 		}
		     
		 		 System.out.println("custoerProdJson alustan " + u );
		    	   
			final File  v  = new File("custprodjsontemp"   + u+ ".txt");
		       if(urlStr != null){ 
		    	 if(!cpDone) {
		    		 
		    	
		    	   try{
		    	   URL urlst = new URL(urlStr);
					Converter.trustAllHttpsURLConnection();
					Converter.doTrustToCertificates();
				URLConnection con = urlst.openConnection();
				InputStream is = con.getInputStream();
				OutputStream os = new FileOutputStream(v);
				int i;
				while ( (i = is.read()) != -1) {
					os.write(i);
				}
				is.close();
				os.close();
				
				
				
				   File  vv  = new File("custprodjson"   + u+ ".txt");   
		    	   File  uu  = new File("custprodjsontemp"   + u+ ".txt");  
		    	   System.out.println("custprodJson suurus " +  uu.length() );
		    	   if(uu.exists() && uu.length() > 2000) {
		    		 if(vv.exists()) {
		    			   vv.delete();
		   		    }
		    		
		    		 boolean done =   uu.renameTo(new File("custprodjson"   + u+ ".txt")) ;
		    		 System.out.println("custprodJson renamed");
				
				
		    	   }
		    	   }catch(Exception w){
				    	w.printStackTrace();	
				       }
		    	 }
		    	   
		    	   System.out.println("customerProdJson lopp ");
				JSONParser parser = new JSONParser();
			       Object obj = parser.parse(new FileReader("custprodjson" + u + ".txt"));
		           org.json.simple.JSONObject company = (org.json.simple.JSONObject) obj;
		           org.json.simple.JSONArray companyList = (org.json.simple.JSONArray) company.get("data");
		      
		      Iterator<org.json.simple.JSONObject> iterator = ((List) companyList).iterator();
		      while (iterator.hasNext()) {
		    	  org.json.simple.JSONObject cob = iterator.next();
		    	 
		    	  String custID = (String)cob.get("ORDERACCOUNT");
		    	  String prodID = (String)cob.get("ITEMID");
		    	  String idate = ((String)cob.get("INVOICEDATE")).substring(5);
		    	  Long last = (Long)cob.get("QTY");
		    	  Double lastSum = null;
		    	  try {
		    		  lastSum = (Double)cob.get("LINEAMOUNT");
			    	  if(lastSum != null) {
			    		  lastSum = lastSum/last;
			    	  }else {
			    		  lastSum = 0d;
			    	  }
		    	  }catch(Exception e) {
		    		  try {
		    			  lastSum = (Long)cob.get("LINEAMOUNT") + 0d;
				    	  if(lastSum != null) {
				    		  lastSum = lastSum/last;
				    	  }else {
				    		  lastSum = 0d;
				    	  } 
		    		  }catch(Exception t) {
		    			  
		    		  }
		    	  }
		    	 
		    	  
		    	  String SALESID = (String)cob.get("SALESID");

						if (this.customers.contains(custID)) {
									CustProd cp = new CustProd(custID, prodID,
											last, 0,0, idate, lastSum, 0, 0, " ");
									cp.setAbiKood(SALESID);
									vec.addElement(cp);
							}
  		            	  }
	       	          }
		          }
			  	   }catch(Exception e){
					e.printStackTrace();
				}
	
		System.out.println("Cp suurus " + vec.size());
		cpDone = true;
		return vec;

	}

	

    public ManagerData getManagerData() throws SQLException{
     
      return null;
    }

	public String getStartingTime() {
		return null;
	}

	public String getDriverVersion() {
		return "ViaLaima";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public double getSalary(String agentCode) {
		return 0;
	}

	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

	private String getStatDir() {
		String dir = getParent().getProps().getProperty("statdir");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
	


	public  HashMap	custMap = new HashMap();
	private String subjectFilesTo = null;
    private String url2 = "jdbc:odbc:report";
	private ArrayList customers = null;

	@Override
	public void PictureMaker() {
		 
		   this.getProducts(null);
       	
       	String picts = parent.getProps().getProperty("pict.dir");
        String pictspda = parent.getProps().getProperty("pictpda.dir");
     	  

       	try {
     	
            StringBuffer buf = new StringBuffer();
            
            File d  = new File(picts);
            for(String fname : d.list() ) {
            
            	 File in = new File ( picts + fname); 
            	 String ean = fname.substring(0,fname.indexOf("."));
            	 String ext = fname.substring(fname.indexOf(".") + 1);
            	 String code = ean;
            	 if(fname.contains(" ")) {
            		 code = fname.substring(0,fname.indexOf(" "));
            	 }
            	 
            		if(in.isFile() ) {//&& prodsH.contains(ean)    
            			
            			if(koodiga.get(ean) != null) {
                     	 
                      	File inpda = new File ( pictspda + ean + "." + ext); 
                      	
                      		Converter.shrinkAndCopyFile(in,inpda);
                     
                      	
                        String len = "0";
                        if(inpda.exists()){
                          len =	inpda.length() + "";
                          } 
                        
                          buf.append(ean + "." + ext + ";"  + len + "\n");
                        }
            			if(!code.equals(ean) && koodiga.get(code) != null) {
                        	 
                          	File inpda = new File ( pictspda + code + "." + ext); 
                          	
                          		Converter.shrinkAndCopyFile(in,inpda);
                            String len = "0";
                            if(inpda.exists()){
                              len =	inpda.length() + "";
                              } 
                            
                              buf.append(code + "." + ext + ";"  + len + "\n");
                            }
            		}
                 }
          	
        
       	    //oleasoleva kataloogi kustutamine
       	    
                   File f = new File("list.txt");
                   if (f.exists())f.delete();
                   
                   try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				  "list.txt", true));
               		os.write(buf.toString());
               		os.close();
               	   File fff =  new File (pictspda +  "list.txt");
         	      Converter.copyFile(f,fff);  
               	
               	  }catch (Exception ex) {
               		ex.printStackTrace();
               	  }
           	  
       	} catch (Exception e) {
       	    getParent().showError(e);
       	}
    
System.exit(0);

		
	}
	
	@Override
	public void WebReporter() {
		
		Converter.uploadQuestsAndAnswers(rcompanyx,user,pass,parent.getProps(),this.getExcelDir());
		  System.exit(0);
	}
}