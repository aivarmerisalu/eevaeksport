package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DirectoKEYDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker {

  public DirectoKEYDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    company = parent.getProps().getProperty("company");
    comphelp = parent.getProps().getProperty("comphelp");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    logurl = "https://login.directo.ee/xmlcore/";
    if( parent.getProps().getProperty("logurl") != null){
    	  logurl = parent.getProps().getProperty("logurl");
    }
  
    if( parent.getProps().getProperty("stock.dir") != null){
    	stockConn = parent.getProps().getProperty("stock.dir");
    }
    if("true".equals(getParent().getProps().getProperty("use.olddata"))){
		  useold = true;
		  }
  }
  

  
  private String stockConn = null;
  String user =  null;
  String pass = null;
  String rcompany = null;
  String logurl = null;
  
  public TreeMap getProductsOther(){
	    TreeMap tm = new TreeMap();
	     if(stockConn != null){
	    	
	     try {
	 
	    	   FileReader fr = new FileReader(stockConn + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		 // System.out.println(line);
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		      	      Product prod = new Product(id.substring(0,id.indexOf("\"")).trim());
		      	      len = line.indexOf("default_name=");
		      	     String name   = line.substring(len + 14);
		      	      len = line.indexOf("stock_level_total=");
		      	 	 if(len != -1){
		      	 	     String stock   = line.substring(len + 19);  
		     	          prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
		     	         tm.put(prod.getID(), prod.getStock());  
		      	 	 }
		      	
	        	  }
	        	
	          }	    	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     }
	     System.out.println("Stocks OTHER size " + tm.size());
	     
	     return tm;
	     
	     
	   }
  


  
  
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
	         		 
	             	//(new File ( pictspda + ff2[i])).delete(); 
	             }
//	         	 Lõppes kustutamine 
	         	custProds.add("suva");
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim(); 
	    	    	
	    	    	  toode = toode.replace('/', '_');
	    	          toode = toode.replace('/', '_');
	    	          
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(toode)){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + toode+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
            	  
            	  	     } 
	                if(buf.toString().indexOf(toode +".jpg;"  + inpda.length()) == -1){
                		buf.append(toode +".jpg;"  + inpda.length() + "\n");
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

  
  
  private Logger log = Logger.getLogger(DirectoKEYDBDriver.class.getName());
  
  private String company = null;
  private String comphelp = null;
  
  public String getSaveDebtInfo() {
   	return null;
   }
   
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	  private java.util.Date getCalXML(String ss){
		  Calendar cc = Calendar.getInstance();
		  	
		  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
		  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
		  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
		     
		  	return cc.getTime() ;
		  		
	  }
	
	  
	 private String ensureLen(String in ){
		 
		 if(in.length() == 4){
			 in = "0" + in ;
		 }
		 if(in.length() == 3){
			 in = "00" + in ;
		 }
		 if(in.length() == 2){
			 in = "000" + in ;
		 }

		 if(in.length() == 1){
			 in = "0000" + in ;
		 }
		 return in;
	 }
	  
	public Vector getPriceMatrixData() {
   String sdate = Converter.getDDpMMpYYYYpString(Calendar.getInstance());
   	Vector lines = new Vector();
    
    try {
		  File f = new File (getDir() + "hinnakiri.xml");
		  System.out.println("siin matix");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport");      
   
    	Node read = getNode(root, "priceformulas");
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	  try{
        		  if(rida.getNodeName().equals("priceformula")){
        			
        			 String custID = " ";
                  String clGroup = " ";
                  String prodID = " ";
                  String prodGroup = " ";
                  String start = " ";
                  String end = " ";
                  String currency = "";
                  MatrixLine mline = new MatrixLine();
                 try{
                 	 try{
                 	     clGroup = ensureLen(rida.getAttributes().getNamedItem("code").getNodeValue());
                        }catch(Exception aa){}
                 	 try{
	                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
                 	   }catch(Exception aa){ }
                 	 try{
                          prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
                 	  }catch(Exception aa){ }
                 	  try{
	                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
	                    	  }catch(Exception aa){ }
	                    	  try{
		                             end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
		                    	  }catch(Exception aa){ }
	                    	  try{
		                             currency =  rida.getAttributes().getNamedItem("currency").getNodeValue();
		                    	  }catch(Exception aa){ }
	                    	  String mode = null;  
	                    	  try{  	  
                       mode = rida.getAttributes().getNamedItem("what").getNodeValue();
	                    	  }catch(Exception aa){ }
                      try{
                     double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
                     if(mode != null && mode.equals("8")){
                    	 mline.setPrice(value);
                     }else{
                         mline.setDiscount(value);
                     }
                      }catch(Exception aa){ }
                  
                      
                      mline.setProdLevel(mode);
                    
                      
                      if (custID != null && custID.trim().length() > 0) {
                        mline.setCustID(custID.trim());
                      }
                      if (currency != null && currency.trim().length() > 0) {
                          mline.setCurrency(currency.trim());
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
                      
                      if("true".equals(getParent().getProps().getProperty("add.startdate"))){
                    	  if(clGroup != null  && clGroup.trim().length() > 0) {
                    		  if (prodID != null && prodID.trim().length() > 0) {
                    			  if(mline.getDiscount() > 0) {
                    				  mline.setStartDate(sdate);
                    			  }
                    		  }
                    	  }
                      }
                      
                      if(start != " " && end != " "){
                     	 mline.setStartDate(start);
                     	 mline.setEndDate(end);
                          Date alg = getCalXML(start); 
                          Date lopp = getCalXML(end); 
                          
                          long diff = lopp.getTime() - alg.getTime();
                          float days = (diff / (1000*60*60*24));
                          if(days > 100) {
                        	  mline.setStartDate(" ");
                        	  mline.setEndDate(" ");
                        	  
                        	  if("true".equals(getParent().getProps().getProperty("add.startdate"))){
                            	  if(clGroup != null  && clGroup.trim().length() > 0) {
                            		  if (prodID != null && prodID.trim().length() > 0) {
                            			  if(mline.getDiscount() > 0) {
                            				  mline.setStartDate(sdate);
                            			  }
                            		  }
                            	  }
                              }
                          }
                          
                          if(alg.before(new Date()) && lopp.after(new Date())){
                        	  if(mline.discount > 0 || mline.price > 0)
                         	 lines.add(mline);	
                          }
                        
                          
                      } else {
                    	  if(mline.discount > 0 || mline.price > 0)
                     	 lines.add(mline);	
  	                 }
                 
                 }catch(Exception e){
                 	e.printStackTrace();
                 }
                 
                }
             	
        	  }catch(Exception e){
        		  e.printStackTrace();
        	  }
             
        }
        
	}catch(Exception e){
		e.printStackTrace();
	}
    
 System.out.println("siin matrix" + lines.size());
    return lines;


   }

 //  public Vector getFixedAssortmentData() {
      // return fixedLines ;
 //}
   
   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		   if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
			   return fixedLines ;
		   }
			   
		
		this.getProducts(null);
		
		File f = new File(this.getExcelDir() + "/sortiments.xls");
				
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Assortment");
			HSSFRow row = null;
			
			HSSFSheet sheetKood = workbook.getSheet("Code");
			
			HSSFSheet sheetKoodSort = workbook.getSheet("CodeSorts");
			
			TreeMap omakoodid = new TreeMap();

			TreeMap omagroups = new TreeMap();
			
			TreeMap omaTootegroups = new TreeMap();
			
			row = sheetKood.getRow(0);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD TOOTEKOODIDE Sortimendi tähised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 1; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					omagroups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println("omagroups " + omagroups.toString());

			
			//mis kood on sortimedi koodid ja tootekoodide sortimendi tähised kokku 
			for (int i = 0; i < sheetKoodSort.getLastRowNum(); i++) {
				row = sheetKoodSort.getRow(i);
				String sortkood = "";
				if (row.getCell((short) 0) != null) {
                  try {
						sortkood = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						sortkood = (row.getCell((short) 0).getStringCellValue());
					}
			}
				String omakood = "";

				if (row.getCell((short) 1) != null) {

					try {
						omakood = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						omakood = (row.getCell((short) 1).getStringCellValue());
					}
				//	System.out.println("omakood" + omakood);

			}
				
				if(!omakood.equals("") && !sortkood.equals("")){
					
						omaTootegroups.put(sortkood, omakood);
					//	omaTootegroups.put(sortkood, i+"");
				}
			}
				
			 System.out.println("omaTootegroups " + omaTootegroups.toString());
			
			TreeMap tulemusKood = new TreeMap();

			for (int i = 1; i < sheetKood.getLastRowNum(); i++) {
				row = sheetKood.getRow(i);
				
				String prodID = "";
               if (row.getCell((short) 0) != null) {
                 try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
					}
								}
				

				String prodID1 = "";
               if (row.getCell((short) 1) != null) {
                	try {
						prodID1 = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID1 = (row.getCell((short) 1).getStringCellValue());
					}
			     }

               if(prodID.trim().length() > 0 && prodID1.trim().length() > 0){
            	  
            	   String key = omagroups.get("1") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	    }
                          
               }
				
               
				String prodID2 = "";
               if (row.getCell((short) 2) != null) {
                 try {
						prodID2 = (int) (row.getCell((short) 2)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID2 = (row.getCell((short) 2).getStringCellValue());
					}
			       }
				

               if(prodID.trim().length() > 0 && prodID2.trim().length() > 0){
             	  
            	   String key = omagroups.get("2") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	    }
               }
               
               
				String prodID3 = "";
               if (row.getCell((short) 3) != null) {
              	try {
						prodID3 = (int) (row.getCell((short) 3)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID3 = (row.getCell((short) 3).getStringCellValue());
					}
			    }
				

               if(prodID.trim().length() > 0 && prodID3.trim().length() > 0){
             	  
            	   String key = omagroups.get("3") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	    }
               }
               
				String prodID4 = "";
               if (row.getCell((short) 4) != null) {
             	try {
						prodID4 = (int) (row.getCell((short) 4)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID4 = (row.getCell((short) 4).getStringCellValue());
					}
			    }
               


               if(prodID.trim().length() > 0 && prodID4.trim().length() > 0){
             	  
            	   String key = omagroups.get("4") + "";
            	   if(tulemusKood.get(key) != null){
            		   TreeMap t1 = (TreeMap) tulemusKood.get(key);
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	   }else{
            		   TreeMap t1 = new TreeMap();
            		   t1.put(prodID, prodID1);
            		   tulemusKood.put(key, t1);
            	    }
               }
			}
			
			System.out.println("TulemusKood " + tulemusKood.toString());
			
			
			
			TreeMap groups = new TreeMap();
			row = sheet.getRow(2);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.
             
			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
				}
			}

			 System.out.println("groups" + groups.toString());

			for (int d = 6; d < sheet.getLastRowNum(); d++) {
				row = sheet.getRow(d);
				String prodIDe = "";

				if (row.getCell((short) 0) != null) {

					try {
						prodIDe = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodIDe = (row.getCell((short) 0).getStringCellValue());
					}
					
                      if(!prodIDe.equals("0")){
                   	
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

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi tähis
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && go.equals("1")) {

						//	 System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
									
								}

								if (prodIDe != null	&& prodIDe.trim().length() > 0 ) {
										mline.setProdID(prodIDe.trim());	
									}
								
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								

								if (clGroup != null && prodIDe != null
									&& clGroup.trim().length() > 0 && prodIDe.trim().length() > 0 ) {
                                   if(omaTootegroups.get(clGroup.trim()) != null){
                                	   String ass = (String)(omaTootegroups.get(clGroup.trim()));
                                	   if(tulemusKood.get(ass) != null){
                                		  
                                		     TreeMap tr = (TreeMap)tulemusKood.get(ass);
                                		     if(tr.get(prodIDe) != null){
                                		    	 mline.setLineinfo("" +tr.get(prodIDe));
                                		 //   	 System.out.println(clGroup.trim() + " " + omaTootegroups.get(clGroup.trim())
                                		   // 			 + " " + tr.get(prodIDe) + " " + prodIDe);
                                           	  
                                		     }
                                			
                                	   }
                                   }
									//tulemusKood.get()
								}
											
								// if(insertassort){
										//  insertSortLine(mline);  
									// }
								
								//System.out.println("Milne " + mline.toString());
								result.add(mline);
							}
						}
					}
					   
                      }
				}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

   
   
   
   public Vector getFixedAssortmentData1() {
       Vector lines = new Vector();
       TreeMap prods = this.getProducts2();
    //  System.out.println("prods " + prods.size());
       
       try {
   		  File f = new File (getDir() + "hinnakiri.xml");
   	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	     DocumentBuilder db;
   		db = dbf.newDocumentBuilder();      	
       	FileInputStream in = getCleanInputStream(f);
       	String handler = null;
           Document doc = db.parse(in,handler);
           Node root = getNode(doc, "transport");      
      
       	Node read = getNode(root, "priceformulas");
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  try{
           		  if(rida.getNodeName().equals("priceformula")){
           			 String custID = " ";
                     String clGroup = " ";
                     String prodID = null;
                     String prodGroup = " ";
                     String start = null;
                     String end = null;
                     MatrixLine mline = new MatrixLine();
                    try{
                    	 clGroup = ensureLen(rida.getAttributes().getNamedItem("code").getNodeValue()); 
                    	  try{
                    		  prodID =  rida.getAttributes().getNamedItem("item").getNodeValue(); 
                    	  }catch(Exception rr) {
                    		 // System.out.println("jama " + i + " " + clGroup);
                    	  }
                    	  
                        
                        try { 
                         start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
              	           }catch(Exception aa){ }
              	        
                        try{
                           end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
                  	      }catch(Exception aa){ }
                         
                    
                         if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                         }

                         if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                         }
                    
                       
                         if(prodID != null && prods.keySet().contains(prodID) && pricelists.contains(clGroup.trim())){
                        	
                        	
                        	 
                        	if(!prodsInUse.contains(prodID.trim())){
                           	  this.prodsInUse.add(prodID.trim());	
                        	}
                        	
                        	
                        	if(start != null && end != null) {
                        		 Date alg = getCalXML(start); 
                                 Date lopp = getCalXML(end); 
                           	 
                           	  if(alg.before(new Date()) && lopp.after(new Date())){
                                  	 lines.add(mline);	
                                  }	
                        	}else {
                        		lines.add(mline);
                        	}
                        	 	
                          }
                    
                       
                    }catch(Exception e){
                    	e.printStackTrace();
                    }
                    
                   }
                	
           	  }catch(Exception e){
           		  e.printStackTrace();
           	  }
                
           }
           
   	}catch(Exception e){
   		e.printStackTrace();
   	}
      // System.out.println("prodsinuse " +prodsInUse.size() );
       
       return sortLines(lines,prods);
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	   TreeMap asi = new TreeMap();
	       // System.out.println("Prod22 " + products.size());
	        for (int i = 0 ; i < inVec.size() ; i++){
	        	
	        	  MatrixLine ol = (MatrixLine)inVec.elementAt(i);
	          asi.put(ol.getCliGroup()+ ((Product)products.get(ol.getProdID())).getName() , ol);
	        }
	        Vector sorted = new Vector ();
	        Iterator itera = asi.keySet().iterator();
	        while (itera.hasNext()){
	        	String key = (String)itera.next();
	        	sorted.add((MatrixLine)asi.get(key));
	        }
	       
	     //  System.out.println("sorted Size " + sorted.size());
	        return sorted;
	      }
	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }

   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }
   public void connectionUp(){
   }

   public String getDealerCustomerCSV(String agent){
       return null;
        }

       public String  getCustProdDiscounts(Agent agent){
                     return null;
            }
  private TreeMap   prGroups= new TreeMap();
  private TreeMap   prTypes= new TreeMap();
  
  public TreeMap getProdGroups() throws SQLException {
	  return prGroups;
  }
	    
  
  private TreeMap getLaoSeisud (String ladu) {
	  TreeMap seis = new TreeMap();
	  
	 	try {
	  		  File f = new File (getDir() + "laoseis.xml");
	  		  if(f.exists()) {
	  			
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "transport");      
	       //   System.out.println(root);
	      	Node read = getNode(root, "stocklevels");
	      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	          for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          	  Node rida = read.getChildNodes().item(i);
	          	  try{
	          		  if(rida.getNodeName().equals("stocklevel")){
	               		 String prodid  = rida.getAttributes().getNamedItem("code").getNodeValue();
	               		 double kog = Double.parseDouble(rida.getAttributes().getNamedItem("stock_level").getNodeValue());
	               		 seis.put(prodid, kog);
	               		 try{
	               			 String lkood = rida.getAttributes().getNamedItem("stock_code").getNodeValue();
	               		 }catch(Exception r){
	               			 
	               		 }
	          		  } 
	          	  }catch(Exception e){
	          		 // e.printStackTrace();
	          	  }
	          }
	          
	  		  }
	  	}catch(Exception e){
	  		//e.printStackTrace();
	  	}
	    
		  
	  
	  return seis;
  }
  
   public TreeMap getProdGroups2() throws SQLException {
    HashSet names = new HashSet();
	   String lang = null;
	   TreeMap res = new TreeMap();
	     if(getParent().getProps().getProperty("productgroup.lang") != null){
	      	 lang = getParent().getProps().getProperty("productgroup.lang");
	      	// System.out.println("product.lang="+ lang);
	      }
	   
  	try {
  		  File f = new File (getDir() + "tooted.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
          Node root = getNode(doc, "transport");      
       //   System.out.println(root);
      	Node read = getNode(root, "items");
      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int i = 0; i < read.getChildNodes().getLength(); i++){
          	  Node rida = read.getChildNodes().item(i);
          	  try{
          		  if(rida.getNodeName().equals("bi_item")){
               		 String group  = rida.getAttributes().getNamedItem("class").getNodeValue();
               		 try{
               			 String name  = rida.getAttributes().getNamedItem("classname").getNodeValue(); 
                   		 group = Converter.cleanLitLatEst(group);
                   		  name  =  Converter.enCodeEst(name);
                   		  name = name.replaceAll("Turtle Wax", "TW");
                   		  
                   		  if(lang != null) {
                   			  try {
                   			  Node lan=	  rida.getChildNodes().item(0);
                   		     name = Converter.enCodeEst( lan.getAttributes().getNamedItem("content").getNodeValue());
                   		
                   			  }catch(Exception e) {
                   				  
                   			  }
                   			
                   				
                   			
                   		  }
                   		 if(names.contains(name + " 1")) {
                  			  name = name + " 2";
                  		  }
                   		  
                   		  if(names.contains(name)) {
                   			  name = name + " 1";
                   		  }
                   		  res.put(group, name); 
                   		  prGroups.put(group, name); 
                   		// System.out.println("gr name " + name + " " + group);
                   		names.add(name);
               		 }catch(Exception r){
               			 
               		 }
               	 
          		  } 
                  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 // System.exit(0);
          	  }
               
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  	
  	
  	
	try {
		  File f = new File (getDir() + "tooted.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport"); 
        if(root != null){
     //   System.out.println(root);
    	Node read = getNode(root, "items");
    //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
       if(read != null){
    	
    	
    	for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	  try{
        		  if(rida.getNodeName().equals("bi_item")){
             		 String group  = rida.getAttributes().getNamedItem("supplieritemcode").getNodeValue();
             		 try{
             			 String name  = rida.getAttributes().getNamedItem("name").getNodeValue(); 
                 		 group = Converter.cleanLitLatEst(group);
                 		  name  =  Converter.enCodeEst(name);
                 		//  prGroups.put(group, name);  
             		 }catch(Exception r){
             			 
             		 }
             	 
        		  } 
                
        	  }catch(Exception e){
        		 // e.printStackTrace();
        		 // System.exit(0);
        	  }
             
        }
    	   
       }
       }
	}catch(Exception e){
		e.printStackTrace();
	}
  	
  	return res;
   }

   private boolean hasSupplier(){
	   boolean res = false;
		try {
			  File f = new File (getDir() + "tooted.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
	    	FileInputStream in = getCleanInputStream(f);
	    	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport"); 
	        if(root != null){
	     //   System.out.println(root);
	    	Node read = getNode(root, "items");
	    //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	       if(read != null){
	    	
	    	
	    	for (int i = 0; i < read.getChildNodes().getLength(); i++){
	        	  Node rida = read.getChildNodes().item(i);
	        	  try{
	        		  if(rida.getNodeName().equals("supplieritemcode")){
	             		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
	             			  return true;  
	             	  } 
	                
	        	  }catch(Exception e){
	        		  e.printStackTrace();
	        		 // System.exit(0);
	        	  }
	             
	        }
	    	   
	       }
	       }
		 }catch(Exception e){
  		  e.printStackTrace();
  		 // System.exit(0);
  	  }
	   
	   
	   return res ;
   }
   


   
   public TreeMap getProdGroupsSecond() throws SQLException {
	   
	   if("true".equals(getParent().getProps().getProperty("usesecondclass"))){
		   return  getProdGroups2();
	   }
	   
	   if(getParent().getProps().getProperty("usedataforgroups") != null){
		   return null;
	   }
	   TreeMap res2 =  getProdGroups2();
	  
      return res2;
        }



     
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
   
     
     //makeData();
    // System.out.println( getPricelists("1").toString());
   //  this.getProducts(null);
     //File ff = new File(this.getExcelDir() + "graafikud.xlsx");
   //  Converter.sendExcelVisits(ff,rcompany,user,pass);
     
     // loene võimalikud agendid sisse 
     agent1 = new TreeMap();
 	try {
 		  File f = new File (getDir() + "kliendid.xml");
 	     
 	       // System.out.println(f.getAbsolutePath());
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "transport"); 
     	Node read = getNode(root, "customers");
     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
         for (int i = 0; i < read.getChildNodes().getLength(); i++){
         	  Node rida = read.getChildNodes().item(i);
         	  try{	
         		 String salesman  = rida.getAttributes().getNamedItem("agent").getNodeValue(); 
         		 salesman = Converter.deCode(salesman);
         		 Agent sm = new Agent (salesman);
         		 String salesmanName  = rida.getAttributes().getNamedItem("agent_name").getNodeValue(); 
         		 sm.setName(salesmanName);
                  tm.put(salesman, sm); 
                 
                  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
                  if(!parent.getEmail(salesman).equals("") &&  !custtsUsed.contains(id)){
                	  agent1.put(salesman,sm);
	         		   custtsUsed.add(id);
	         		  String debt  = rida.getAttributes().getNamedItem("balance").getNodeValue(); 
	           		
	         		   if(!debt.equals("0")){
	         			custDebtUsed.add(id);
	         			//System.out.println("balance " + debt);
	         		   }
      		      }
	         			 String price_list = ensureLen(rida.getAttributes().getNamedItem("price_list").getNodeValue());         	
		         	
		         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(price_list)){
	 	         		     pricelists.add(price_list.trim()); 
	 	         		   }
		         	      price_list =  rida.getAttributes().getNamedItem("price_list_special").getNodeValue() ;         	
				         	
		         	      StringTokenizer st  = new StringTokenizer(price_list,",");
		         	      if(st.hasMoreTokens()){
                            String aa = ensureLen(st.nextToken());
                            
			         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(aa)){
		 	         		     pricelists.add(aa); 
		 	         		   }
			         	      
		         	      }
	         	  
         	  }catch(Exception e){
         		 // e.printStackTrace();
         	  }
         }
         
 	}catch(Exception e){
 		e.printStackTrace();
 	}
     
    // System.out.println("Agendilist tehtud" );
     log.info("Agendilist tehtud");
     log.fine("Hinnakirju kasutusel "  + pricelists.size() );
     System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());
     System.out.println("Võlas olevaid kliente kasutusel "  + custDebtUsed.size());
     System.out.println("Agent1 suurus "  + agent1.size());
       
     
     if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
    	  fixedLines = getFixedAssortmentData1();
    	     }
   
     
     Agent aaa = new Agent ("LADU");
     aaa.setName("LADu");
     tm.put("LADU", aaa);

    // setBulkSales(tm);
     return tm;
   }

   TreeMap agent1 = null;
   
   private void makeData(){
	   
	  
	 

	     
       try{
       	String httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=itemclass";
       	
       	Converter.doTrustToCertificates();
           URL myurl = new URL(httpsURL);
           HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
           InputStream ins = con.getInputStream();
           InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
           BufferedReader in = new BufferedReader(isr);

           String inputLine;
           StringBuffer inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLine = Converter.enCodeEst(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
          }

           in.close();
           
           if(inputLineBuf.length() > 2){
        	   String str = inputLineBuf.toString();
               if(str.startsWith("True")) {
              	 str = str.substring(4);
               }
                 try {
               	  File f = new File (getDir() + "tootegrupid.xml");
               	  if(f.exists())f.delete();
               	  
               	  
             	 Converter.saveFileInUTF8(getDir() + "tootegrupid.xml", str);
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
           
           //lõpeb tootegruppidega tegelemine 
           System.out.println("Tootegrupid tehtud" );
           //algab Hinnakirja faili moodustamine
           
           
           String gS = getParent().getProps().getProperty("keyStocks");
   	    if(gS == null) {
   	    	System.out.println("keyStocks is missing!");
   	    	System.exit(0);
   	    }
   	   
           String urlParameters  = "get=1&what=stocklevel&key=" + gS;
           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
           myurl = new URL(httpsURL);
           
           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
           int postDataLength = postData.length;
           con = (HttpsURLConnection)myurl.openConnection();
           con.setRequestMethod("POST");
           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
           con.setRequestProperty("charset", "utf-8");
           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
           con.setUseCaches(false);
           con.setDoOutput(true);
           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
              wr.write( postData );
           }
           
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
        	  String str = inputLineBuf.toString();
              if(str.startsWith("True")) {
             	 str = str.substring(4);
              }
                try {
              	  File f = new File (getDir() + "laoseis.xml");
              	  if(f.exists())f.delete();
              	 Converter.saveFileInUTF8(getDir() + "laoseis.xml", str);
          	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }
          
          System.out.println("Laoseisud tehtud" );
          
          
         
          try {
        	  
        	  String gP = getParent().getProps().getProperty("keyPrices");
         	    if(gP == null) {
         	    	System.out.println("keyPrices is missing!");
         	    	System.exit(0);
         	    }
         	   
             urlParameters  = "get=1&what=priceformula&key=" + gP;
            
        	 //?get=1&what=priceformula";
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
           postDataLength = postData.length;
          con = (HttpsURLConnection)myurl.openConnection();
          con.setRequestMethod("POST");
          con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
          con.setRequestProperty("charset", "utf-8");
          con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
          con.setUseCaches(false);
          con.setDoOutput(true);
          try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
             wr.write( postData );
          }
       
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
          }

          in.close();
          
          }catch (Exception e) {
        	  e.printStackTrace();
          }
          if(inputLineBuf.length() > 2){
         String str = inputLineBuf.toString();
         if(str.startsWith("True")) {
        	 str = str.substring(4);
         }
                try {
              	  File f = new File (getDir() + "hinnakiri.xml");
              	  if(f.exists())f.delete();
              	 Converter.saveFileInUTF8(getDir() + "hinnakiri.xml", str);
          	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }
          

          //lõpeb toodetega tegelemine 

          System.out.println("Hinnakirjad tehtud" );
           
         
           
          String gB = getParent().getProps().getProperty("keyBrands");
   	    if(gB == null) {
   	    	System.out.println("keyBrands is missing!");
   	    	System.exit(0);
   	    }
   	   
       urlParameters  = "get=1&what=brands&key=" + gB;
      
  	 //?get=1&what=priceformula";
     myurl = new URL(httpsURL);
     con = (HttpsURLConnection)myurl.openConnection();
     postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
     postDataLength = postData.length;
    con = (HttpsURLConnection)myurl.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
    con.setRequestProperty("charset", "utf-8");
    con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
    con.setUseCaches(false);
    con.setDoOutput(true);
    try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
       wr.write( postData );
    }
          
       
          ins = con.getInputStream();
          isr = new InputStreamReader(ins,"UTF-8");
          in = new BufferedReader(isr);

          inputLine = "";
         inputLineBuf = new StringBuffer();
         while ((inputLine = in.readLine()) != null){
         	inputLine = Converter.enCodeEst(inputLine);
         	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
         }

         in.close();
         
         if(inputLineBuf.length() > 2){
        	  String str = inputLineBuf.toString();
              if(str.startsWith("True")) {
             	 str = str.substring(4);
              }
               try {
             	  File f = new File (getDir() + "brands.xml");
             	  if(f.exists())f.delete();
             	 Converter.saveFileInUTF8(getDir() + "brands.xml", str);
         	
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
         

         //lõpeb toodetega tegelemine 

         System.out.println("Brandid tehtud" );
          
      
          
          
           //algab tootede faili moodustamine
          String gK = getParent().getProps().getProperty("keyGen");
  	    if(gK == null) {
  	    	System.out.println("keyGen is missing!");
  	    	System.exit(0);
  	    }
  	   
  	   String genKey = "&key=" + gK;
  	    
            httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=BI_ITEM" + genKey;
            if(getParent().getProps().getProperty("stockcode") != null){
            	httpsURL = httpsURL + "&stock=" +  getParent().getProps().getProperty("stockcode");
  		     }
  	    System.out.println(httpsURL);
            myurl = new URL(httpsURL);
            con = (HttpsURLConnection)myurl.openConnection();
            ins = con.getInputStream();
            isr = new InputStreamReader(ins,"UTF-8");
            in = new BufferedReader(isr);

            inputLine = "";
           inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLine = Converter.enCodeEst(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
           }

           in.close();
           
           if(inputLineBuf.length() > 2){
        	   String str = inputLineBuf.toString();
               if(str.startsWith("True")) {
              	 str = str.substring(4);
               }
                 try {
               	  File f = new File (getDir() + "tooted.xml");
               	  if(f.exists())f.delete();
               	Converter.saveFileInUTF8(getDir() + "tooted.xml", str);
              	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }

           
          
// kui on olemas ka teine ladu siis teeme selle seisu ka.
           if(getParent().getProps().getProperty("stockcode122") != null){
        	   httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=item&stock=" +  getParent().getProps().getProperty("stockcode1");
        	   
        	   System.out.println(httpsURL);
               myurl = new URL(httpsURL);
               con = (HttpsURLConnection)myurl.openConnection();
               ins = con.getInputStream();
               isr = new InputStreamReader(ins,"UTF-8");
               in = new BufferedReader(isr);

               inputLine = "";
              inputLineBuf = new StringBuffer();
              while ((inputLine = in.readLine()) != null){
              	inputLine = Converter.enCodeEst(inputLine);
              	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
              }

              in.close();
              
              if(inputLineBuf.length() > 2){
            	  String str = inputLineBuf.toString();
                  if(str.startsWith("True")) {
                 	 str = str.substring(4);
                  }
                    try {
                  	  File f = new File (getDir() + "tooted" + getParent().getProps().getProperty("stockcode1") +".xml");
                  	  if(f.exists())f.delete();
                  	Converter.saveFileInUTF8(getDir() + "tooted" + getParent().getProps().getProperty("stockcode1") + ".xml", str);
                 	
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }
               }
        	   
 		  }

           //lõpeb toodetega tegelemine 

           System.out.println("Tooted tehtud" );
           //algab kliendi faili moodustamine
        
           httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=BI_CUSTOMER"+ genKey;
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));	
          	
       //       System.out.println(inputLine);	
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
             
                 try {
                    String res = "";
                    String str = inputLineBuf.toString();
                    if(str.startsWith("True")) {
                   	 str = str.substring(4);
                    }
               	  StringTokenizer st = new StringTokenizer(str,"\n");
               	  while(st.hasMoreTokens()){
               		String line = st.nextToken();
               		//if(line.indexOf("\"10173\"") == -1 && line.indexOf("\"13543\"") == -1 && line.indexOf("<contact code=\"2575\"") == -1 ){
               			res = res + line + "\n";
               		//}
               	  }
               	  File f = new File (getDir() + "kliendid.xml");
               	  if(f.exists())f.delete();
               	 
               	Converter.saveFileInUTF8(getDir() + "kliendid.xml", str);
              	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
          
       }catch(Exception e){
       	e.printStackTrace();
       }
       
       

   	

       System.out.println("kliendid tehtud" );
     
       
       
	   
	   
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
        	  
        	  
       public String getDistribution() {
         return null;
       }
      private  boolean useold = false;
 	 

   public TreeMap getCustomers(Agent agent){
	   TreeMap tm = new TreeMap();
	 
	  if(!useold) {
		  makeData();
	  }
	//   System.out.println("kliendid a");
		
		 
	     this.customers = new ArrayList();
	   
	     
	     this.debtList = makeDebtlist();
	     
	   //  System.out.println("kliendid b");  
	     
	   try {
	 		  File f = new File (getDir() + "kliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "transport");      
	      //   System.out.println(root);
	     	Node read = getNode(root, "customers");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("bi_customer")){
	         		 String name = null;
	         		boolean addaadr = true;
		         	  try{
		         		   name =  rida.getAttributes().getNamedItem("name").getNodeValue();
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		  Customer customer = new Customer(id);
		         		  customer.restName = name;
		         		  String kkood = id;
		         		  try{
		         			  String payer = rida.getAttributes().getNamedItem("payer").getNodeValue();
			         		  
			         		  if(payer != null && payer.trim().length() > 0 ){
			         			  customer.setPayer(payer.trim());
			         		  }
	  
		         		  }catch(Exception e){
		         			  
		         		  }
		         		
		         		if(this.usedCredit.get(id) != null){
		         			customer.setUsedCredit(Double.parseDouble(usedCredit.get(id)+ ""));
		         		}
		         		
		         		if(this.dueSums.get(id) != null){
		         			customer.setDebt(Double.parseDouble(dueSums.get(id)+ ""));
		         		}
		         		
		         		 String country = "Lithuania";
		       		  if(getParent().getProps().getProperty("country") != null){
		       			  country = getParent().getProps().getProperty("country");
		       		  }
		         		
		         		String kname = name;
		         		 String sendaadr = ""; 
		         		try{ 
		 	         		  sendaadr =  rida.getAttributes().getNamedItem("send_address_street").getNodeValue(); 
		 	         		  customer.restStreet = sendaadr.replaceAll(";", ",");
		 	         		  name = name + " " + sendaadr;
		 	         		addaadr = false;
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	
		         		
		         		}catch(Exception ee){
		         		 }
			         	
		         		
		         		String asula = "";
		         		try{ 
		 	         		  asula =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue().replaceAll(";", ","); 
		 	         		sendaadr = sendaadr + " " + asula;
		 	         		customer.restCity = asula; 
		 	         	
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		

		         		String regno = "";
		         		try{ 
		         			regno =  rida.getAttributes().getNamedItem("regno").getNodeValue(); 
		 	         		customer.setRegnr(regno);
		 	         	 }catch(Exception ee){
		         		 }
                         
		         		

		         		String limit = "";
		         		try{ 
		         			limit =  rida.getAttributes().getNamedItem("limit").getNodeValue(); 
		 	         		customer.setCredit(limit);
		 	         	 }catch(Exception ee){
		         		 }
                         
		         		
		         		

		         		try{ 
		         			double discount  =  Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
		         			if(discount > 0){
		         		 		customer.setDiscount(discount);
		    		 	  }
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		
		         		 String aadr = "";
		         		 String aadr2 = "";
		         		 try{ 
		 	         		  aadr =  rida.getAttributes().getNamedItem("address_street").getNodeValue(); 
		 	         		  aadr2 =  rida.getAttributes().getNamedItem("address_county").getNodeValue(); 
			 	         	  customer.restStreet = aadr;
			 	         	  customer.restCity = aadr2;
		 	         		  customer.setAddress(Converter.adressLink(aadr, country));
		 	         		  
		 	         		  if("true".equals(getParent().getProps().getProperty("use.county")) && addaadr){
		 	         			 name = name + " " + aadr;
		 	         		  }
		 	         			 
		 	         		  
		         		 }catch(Exception ee){ }
		         		 
		         		 
		         		  customer.setName(name);
		         		 
		         		try{ 
		         			country =  rida.getAttributes().getNamedItem("address_country").getNodeValue(); 
		 	         		  customer.setCountry(country);
			 	         	  customer.setAddress(Converter.adressLink(aadr, country));
		         		 }catch(Exception ee){
			 		         		 }
		         		 
		         		
		         		 try{ 
		 	         		 String yldphone =  rida.getAttributes().getNamedItem("telephone").getNodeValue();
		 	         		 if(yldphone.trim().length() > 0){
		 	            		customer.setPersonalData((yldphone).replaceAll(";", ","));
		 	            		customer.restPhone = yldphone;
		 	  		         }
		 	     		 }catch(Exception ee){
                        	 customer.setAddress(""); 
		         		 }

		         		  
		         		  try{
		         			  
		         			 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
		         			  Node nn = rida.getChildNodes().item(r);
		       	    		 if(nn.getNodeName().equals("contact")){
		         				 String cname =  nn.getAttributes().getNamedItem("name").getNodeValue();
		         				String ctel =  nn.getAttributes().getNamedItem("telephone").getNodeValue();
		         				customer.restPerson = cname;
		         				customer.restPerPhone = ctel;
		         				if(customer.getPersonalData().trim().length() < 1){
		         					customer.setPersonalData((cname + " " + ctel).replaceAll(";", ","));
			         			}else{
			         				String lisa = (cname + " " + ctel).replaceAll(";", ",");
			         				if(customer.getPersonalData().indexOf(ctel) != -1){
			         					lisa = (cname ).replaceAll(";", ",");
			         				}
		         				    customer.setPersonalData(customer.getPersonalData() + "|" + lisa);
		         				}
		         				String cemail =  nn.getAttributes().getNamedItem("email").getNodeValue();
		         				customer.restPerEmail = cemail;
		         				
		  	         		
		  	         		 }}
		         		  }catch(Exception r){
		         			  
		         		  }
		         		      		  
		         		  try{
			         			 String city =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue().replaceAll(";", ",") ;         	
				         		  customer.setCity(city);
			         		  }catch(Exception e){
			         			 try{
				         			 String city =  rida.getAttributes().getNamedItem("address_county").getNodeValue().replaceAll(";", ",") ;         	
					         		  customer.setCity(city);
			         			}catch(Exception es){

				         			  customer.setCity(" ");	
			         			}
			         		  }
		         		 
		         		  
		         		  try{
		         			 String price_list =  ensureLen(rida.getAttributes().getNamedItem("price_list").getNodeValue());         	
			         		   customer.setGroup(Converter.deCode(price_list));
			         		  if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
			       			   customer.addFixSortCode(Converter.deCode(price_list)); 
			       		       }
		         		  }catch(Exception e){
		         		  }
		         		  
		
		         		  try{
		         			  String	 price_list =  rida.getAttributes().getNamedItem("price_list_special").getNodeValue() ;
		         			  
		         			  StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         				  
                                   String as = ensureLen(ss.nextToken());
			         			  
                                  if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
			         				 customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
					         	    }else{
			         				 customer.setGroup(Converter.deCode(as));
					         	   }
					         		
                                   if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
					       			   customer.addFixSortCode(Converter.deCode(as)); 
					       		   }  
		         			  }
		         			  
			         		  }catch(Exception e){
			         		  }
		         		  
		         		  
		         		  boolean goes = true;
		         		  
		         		 try{
		         			 String closed =  ensureLen(rida.getAttributes().getNamedItem("closed").getNodeValue());         	
			         		  if(closed.equals("1")) {
			         			  goes = false;
			         			  System.out.println("closed " + customer.getName());
			         		  }
		         		  }catch(Exception e){
		         		  }
		         		  
		         		  
     		try {
	         		 String salesman  = rida.getAttributes().getNamedItem("agent").getNodeValue(); 
	         		      customer.addAgent(new Agent(Converter.deCode(salesman.toUpperCase())));
	         		      customer.setMainAgent(Converter.deCode(salesman.toUpperCase()));
	         		      String sm = parent.getEmail(salesman);
	         		      
	         		    // System.out.println(salesman);
	                  if(sm != null && sm.length() > 1 && goes){
	                	//  System.out.println(sm);
	            	      tm.put(customer.getName(), customer);
	                      this.customers.add(customer.getID());
	                  }
     		  }catch(Exception t){
	         	 // t.printStackTrace();
         	  }
		         	  }catch(Exception e){
	         		
	         		  e.printStackTrace();
	         	  }
	              
	         	  }}
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
	   if(rcompany != null){
	//    	  Converter.sendCustomerJson(tm, rcompany, false, null, null);
		    }
	     Customer cc = new Customer("1777");
	     cc.setName("Ladu tagastus");
	     cc.addAgent(new Agent("LADU"));
	   tm.put("Ladu tagastus", cc);
	   
	   System.out.println("kliendid b " + tm.size());
	   
	   if(tm.size()< 2) {
		   System.exit(0);
	   }
	 //  System.exit(0);  
     return tm;
   }
   

   private boolean isOkDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(mmddyy + " " +cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		 int days = 30 ;
		   if(getParent().getProps().getProperty("sortdays") != null){
			   try{
				   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
			   }catch(Exception e){
			   }
		   }
		
		ca.add(Calendar.DAY_OF_YEAR, -days);
		
		if(cal.after(ca)){
			return true;
		}else{
			return false;
		}
		
	  }
   
   private Date getDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
	    return date;
		
	  }
  
   
   private boolean isDueDate(String mmddyy){
	  	
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.DAY_OF_YEAR, -1);
		
		if(cal.before(ca)){
			return true;
		}else{
			return false;
		}
		
	  }

   private Vector getCP(String custid , String number, String date ){

	   Vector result = new Vector();
	   try{
		   File cpdata = new File("cpdata/" + number+".vec");
		   if(cpdata.exists()) {
			   Vector vec = (Vector)Converter.readObject("cpdata/" + number+".vec");
			   for(int i = 0; i < vec.size(); i++) {
				   CustProd cp = (CustProd)vec.elementAt(i);
				   if(!prodCodes.contains(cp.getProdID())){
				        prodCodes.add(cp.getProdID());	
			        }
			   }
			   return vec ;
		   }else {
			   
		 
			String httpsURL = logurl  + company + "/xmlcore.asp";
			
			
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
		        		   String gS = getParent().getProps().getProperty("keyInvRow");
		  	    if(gS == null) {
		  	    	System.out.println("keyInvoice is missing!");
		  	    	System.exit(0);
		  	    }
		        String urlParameters  = 
						"what=invoice_row&get=1&number=" + number + "&key=" + gS;
		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		        
		        InputStream ins = con.getInputStream();
		        
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine =Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		int ind =  line.indexOf("item=") ;
		        		String toode = "";
			        	if(ind != -1){
			        		toode = line.substring(ind + 6);
			        		toode = toode.substring(0,toode.indexOf("\""));
			        	
			        	}
		        
			        	
			        	ind =  line.indexOf("quantity=") ;
		        		double kogus = 0;
		        		String kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 10);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		 kogus = Double.parseDouble(kkk);
			        	}
			        	
			        	ind =  line.indexOf("total=") ;
			        	double hind = 0;
		        		 kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 7);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		
			        		 hind = Double.parseDouble(kkk)/kogus;
			        		// System.out.println("toode "  + line + " "+ toode + " " + hind);
			        	}
			        	
		        if(toode != null && toode.trim().length() > 0){
			        result.add(new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," "));
			        if(!prodCodes.contains(toode)){
				        prodCodes.add(toode);	
			        }
		        }
		         }
		        	
		        }
		        in.close();
		      //  System.out.println("buffer " + result);
		        Converter.saveObject(result, "cpdata/" + number + ".vec");
		   }
		   
		   
		   
		   
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	   return result;
   }

   HashSet prodCodes = null;
   
   
   private Hashtable getPricelists(String code ){
	   Hashtable re = new Hashtable();
	   try {
			  File f = new File (getDir() + "hinnakiri.xml");
			  System.out.println("siin matix");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
	    	FileInputStream in = getCleanInputStream(f);
	    	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport");      
	   
	    	Node read = getNode(root, "priceformulas");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	        	  Node rida = read.getChildNodes().item(i);
	        	  try{
	        		  if(rida.getNodeName().equals("priceformula")){
	        			
	        			 String custID = " ";
	                  String clGroup = " ";
	                  String prodID = " ";
	                  String prodGroup = " ";
	                  String start = " ";
	                  String end = " ";
	                 try{
	                 	 try{
	                 	     clGroup = rida.getAttributes().getNamedItem("code").getNodeValue();
	                        }catch(Exception aa){}
	                 	 try{
		                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
	                 	   }catch(Exception aa){ }
	                 	 try{
	                          prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
	                 	  }catch(Exception aa){ }
	                 	  try{
		                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
		                    	  }catch(Exception aa){ }
		                    	  try{
			                             end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
			                    	  }catch(Exception aa){ }
	                 	  
	               //       String mode = rida.getAttributes().getNamedItem("what").getNodeValue();
	                     
	                  /*    try{
	                     
	                    	  double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
	                    	
	                    	  if(clGroup.equals(code)){
	                    		  
	                    // System.out.println("qqq " +  mode + " " + code + " " + clGroup + " " + value + " "+ prodID) ;
	                     
	                   //  if(mode.equals("8") ){
	                    	//  re.put(prodID.trim(), value );
	                     // }
	                    	  }
	 	                     
	                    	  }catch(Exception aa){
	                    		  aa.printStackTrace();
	                    	  }*/
                    	 
	                     
	 	                     	                 
	                 }catch(Exception e){
	                 	e.printStackTrace();
	                 }
	                 
	                }
	             	
	        	  }catch(Exception e){
	        		  e.printStackTrace();
	        	  }
	             
	        }
	        
		}catch(Exception e){
			e.printStackTrace();
		}
	   
	//   System.out.println("re " + re.toString());
	   return re;
   }
   
   public TreeMap getProducts(Agent agent){
	   System.out.println("tooted a");
	   TreeMap otherStocks = this.getProductsOther();
	   
	   TreeMap stocksfile = null;
	   try {
		   stocksfile = getLaoSeisud(null); 
	   }catch(Exception e) {
		   
	   }
	 
	   
	   boolean addShelf = false ;
	   try {
		   getProdGroups2();
	     } catch (SQLException e1) {
		  e1.printStackTrace();
	     }
	   
     TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     HashSet usedGroups = new HashSet();
     
     TreeMap stockLisa = new TreeMap();
     if(getParent().getProps().getProperty("stockcode121") != null ){
    	
    	 stockLisa = this.getProducts4();
    //	System.out.println("stockLisaXXXXX " + stockLisa.size());
     }
     
     
     
     if(custProds.size() == 0 && !useold){
         prodCodes = new HashSet();
    	getCustomerProductsList2();
     }
  TreeMap stockAlter = new TreeMap();
     if(getParent().getProps().getProperty("stockdir") != null ){
    	 stockAlter = this.getProducts3();
    	System.out.println("stockalter " + stockAlter.size());
     }

   
     
     
     Vector noGrups = new Vector();
     Vector codeEnds = new Vector();
     Vector noTexts = new Vector();
     Vector yesClasses = new Vector();
     Vector noBrands = new Vector();
     String groupIndent = null;
     String typeIndent = null;
         TreeMap brands = new TreeMap();
         boolean sortBR = false;
          boolean hasSupplier = hasSupplier();
     
     if(getParent().getProps().getProperty("codeends") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("codeends"),",");
     	while (st.hasMoreElements()){
     		codeEnds.addElement(st.nextToken());
     	}
     }
     
     if(getParent().getProps().getProperty("yesclasses") != null){
      	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesclasses"),",");
      	while (st.hasMoreElements()){
      		yesClasses.addElement(st.nextToken());
      	}
      }
     
     if(getParent().getProps().getProperty("usedataforgroups") != null){
         groupIndent = getParent().getProps().getProperty("usedataforgroups");
      	prGroups = new TreeMap();
      	try {
			this.getProdGroups2();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
     
     String fixedurl = null;
     
     if(getParent().getProps().getProperty("fixedurl") != null){
    	 fixedurl = getParent().getProps().getProperty("fixedurl");
      }
     
     boolean proddatafile = false;
     
     if("true".equals(getParent().getProps().getProperty("proddatafile"))){
    	 proddatafile = true;
      }
     
     if(getParent().getProps().getProperty("usedatafortypes") != null){
         typeIndent = getParent().getProps().getProperty("usedatafortypes");
      	prTypes = new TreeMap();
      }
     
     if(getParent().getProps().getProperty("nogroups") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogroups"),",");
     	while (st.hasMoreElements()){
     		noGrups.addElement(st.nextToken());
     	}
     }
     
     
     if(getParent().getProps().getProperty("notexts") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("notexts"),",");
     	while (st.hasMoreElements()){
     		noTexts.addElement(st.nextToken().toLowerCase());
     	}
     }
    
     if(getParent().getProps().getProperty("addShelf") != null){
    	 addShelf = true;
      }
    
     Hashtable prices = null;
     
     if(getParent().getProps().getProperty("usepricelist") != null){
     	String pr = getParent().getProps().getProperty("usepricelist");
     	prices = getPricelists(pr);
     }

     String lang = null;
   
     if(getParent().getProps().getProperty("product.lang") != null){
      	 lang = getParent().getProps().getProperty("product.lang");
      	// System.out.println("product.lang="+ lang);
      }
     String lang2 = null;
     
     if(getParent().getProps().getProperty("product.langdefault") != null){
      	 lang2 = getParent().getProps().getProperty("product.langdefault");
      	// System.out.println("product.lang="+ lang);
      }
      
    
     if("true".equals(getParent().getProps().getProperty("sortbybrands"))){
     	try {
 			brands = this.getProdGroupsSecond();
 			sortBR = true;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     if(getParent().getProps().getProperty("nobrands") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nobrands"),",");
     	while (st.hasMoreElements()){
     		noBrands.addElement(st.nextToken());
     	}
     }
     
     StringBuffer sb = new StringBuffer();
     try {
  
    	 File f = new File (getDir() + "tooted.xml");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport");      
   
    	Node read = getNode(root, "items");
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	
        		  if(rida.getNodeName().equals("bi_item")){
        		
       
        		String id =  rida.getAttributes().getNamedItem("code").getNodeValue();  
        		
        	
      	     Product prod = new Product(id);
      	   
      		boolean goes = true;
      	    
      	    if(proddatafile) {
      	    	try {
       	   String longt =  rida.getAttributes().getNamedItem("description").getNodeValue(); 
       	     longt = longt.substring(0,longt.indexOf("\""));
         	 longt = longt.trim().replaceAll("\\r\\n|\\r|\\n", "|").replaceAll(";", ":"); 
         	 longt = longt.replaceAll("&#x0A:", " ");
         	 longt = longt.replaceAll("&#x0D:", " ");
         	 longt = longt.replaceAll("&#x09:", " ");
         	 longt = longt.replaceAll("   ", " ");
         	 longt = longt.replaceAll("  ", " ");
         	 sb.append(id + ";" + longt + "\n");
      	    	}catch(Exception e) {
      	    		
      	    	}
       	       }
   			
      	  try { 
      	    String url =  rida.getAttributes().getNamedItem("pic_url").getNodeValue(); 
      	 	 if(fixedurl != null) {
          		     prod.setPhotoid("phurl:" +fixedurl + id + ".jpg" ); 
          	 } else {
          		 if(url!=null){
              		 prod.setPhotoid("phurl:" +url); 
              	 } 
          	  }
            }catch(Exception e) {}
      	 
      	    	  
      	 try { 
       	    String clo =  rida.getAttributes().getNamedItem("closed").getNodeValue(); 
       	 	 if(clo.equals("1")) {
       	 		 goes = false;
       	 	 }
             }catch(Exception e) {}
      	 
      	 
      	 try { 
        	    String gr =  rida.getAttributes().getNamedItem("class").getNodeValue(); 
        	    String grn =  rida.getAttributes().getNamedItem("classname").getNodeValue(); 
        	 	prod.setGroup(gr);
        	 	prGroups.put(gr, grn);
              }catch(Exception e) {}
      	  
           	
      			String add = "";
      	 try {
      	    	 String ean =  rida.getAttributes().getNamedItem("barcode").getNodeValue();    
      	    
      	    	ean =  ean.replaceAll(";", ":");
      	    	if(ean.contains("&")) {
      	    		ean = ean.substring(0,ean.indexOf("&"));
      	    	}
      	    	ean =  ean.replaceAll(";", ":");
          	     prod.setEAN(ean);
          	     
          	   if("true".equals(getParent().getProps().getProperty("add.ean"))){
          		   try{
          				add = ean.substring(ean.length()-4); 
          	       }catch(Exception e ){
          			   
          		   }
          		 }
      	 } catch (Exception e) {
      		 
      	 }
      	 
      
      	 try {
      	  String namec =  rida.getAttributes().getNamedItem("name").getNodeValue(); 
    	  //  System.out.println("prod " + id + " + " + namec);
    	
      	  namec = namec.replace("´", "'");
      	  String addid = "";
      	 if("true".equals(getParent().getProps().getProperty("name.code.front"))){
      		 addid = id;
      	 }
        	  if("true".equals(getParent().getProps().getProperty("name.code.front"))){
     			 prod.setName( ( id + " " + Converter.enCodeEst(namec) + " " +  add).trim()); 
     		 }else{
     			 prod.setName(( Converter.enCodeEst(namec) + " " + id + " " + add ).trim()); 
     		 }
        	
        	  if(getParent().getProps().getProperty("vat") != null){
        		  prod.setVat(getParent().getProps().getProperty("vat"));
        	  }
      	    
      	   }catch(Exception e) {
      		   goes = false;
      	   }
      	     prod.setPrice(1);
      	     
      	     
   	    	 try{
      		 String pri =  rida.getAttributes().getNamedItem("price").getNodeValue(); 
      	 	      	    prod.setPrice(Double.parseDouble(pri) ); 
 	    	 }catch (Exception e){
 	    
 	    	 }
      	   
      	    	
        		
    	   /*
    	   
           	 len = line.indexOf("unit=");
           	 if(len != -1){
           	   String unit   =  line.substring(len + 6);  
 	          prod.setUnit(unit.substring(0,unit.indexOf("\"")));
   	   		 }
           	 
           	 String kast = "package2=";
           	 if(getParent().getProps().getProperty("kastis") != null){
           		kast =  getParent().getProps().getProperty("kastis");
           	 }
           	 
           	len = line.indexOf(kast);
          	 if(len != -1){
          	   String incase   =  line.substring(len + 10); 
          	   try{
          		   prod.setInCases(Double.parseDouble(incase.substring(0,incase.indexOf("\""))));
         	   }catch(Exception e){
          	   }
	         }
          	 
         
          	 
           	 
      	
      	if(noTexts.size() > 0){
      		for(int u = 0 ; u < noTexts.size(); u++){
      			String aa = noTexts.elementAt(u) + "";
      			if(namec.toLowerCase().contains(aa)){
          			goes = false;	
      			}	
      		}
      	}

     	  
      	
	if(codeEnds.size() > 0){
      		for(int u = 0 ; u < codeEnds.size(); u++){
      			String aa = codeEnds.elementAt(u) + "";
      			if(id.toLowerCase().endsWith(aa.toLowerCase())){
          			goes = false;	
      			}	
      		}
      	}
	 	  
  	
      		 len = line.indexOf("class=");
      	 	 if(len != -1){
      	 	     String grupp   = line.substring(len + 7);
      	 	     grupp =   grupp.substring(0,grupp.indexOf("\""));
      	 	    // System.out.println("grupp" + grupp + " " + yesClasses.toString());
      	 	     if(yesClasses.size() > 0 && !yesClasses.contains(grupp)){
      	 	    	goes = false; 
      	 	     }
      	 
      	    //  	log.info("33 " + goes + " " + grupp);
      	 	  prod.setGroup(Converter.cleanLitLatEst(grupp.replaceAll(";",":").replaceAll("&amp;", "&")));

          	  len = line.indexOf("supplier_code=");
        	    if(len != -1 && hasSupplier){
        	    	 String gr1  = line.substring(len + 15);
        	   	    gr1 = gr1.substring(0,gr1.indexOf("\""));
            	     prod.setGroup(Converter.cleanLitLatEst(gr1.replaceAll(";",":").replaceAll("&amp;", "&")));
            	     prod.setGroupSecond(Converter.cleanLitLatEst(grupp.replaceAll(";",":").replaceAll("&amp;", "&")));
            	   //  System.out.println("Grupid " +  prod.getGroup() + "*" + prod.getGroupSecond() + "*" + gr1);
        	         
         	    	  
               }
      	 	  
      	 	    
        	 if( typeIndent !=  null){
            		
              		 len = line.indexOf("<data code=\"" +typeIndent + "\"");
              		 int lll = ("<data code=\"" +typeIndent + "\"").length();
               	    if(len != -1){
               	    	 String  gr   = line.substring(len + lll + 10);
               	    	 gr = gr.substring(0,gr.indexOf("\""));
               	    	 prod.setType(gr.replaceAll("&amp;", "&")); 
                   	 }
              	}
        	    
      	 	  if( groupIndent !=  null){
            		
           		 len = line.indexOf("<data code=\"" +groupIndent + "\"");
           		 int lll = ("<data code=\"" +groupIndent + "\"").length();
            	    if(len != -1){
            	    	 String  gr   = line.substring(len + lll + 10);
            	    	 
            	    	 gr = gr.substring(0,gr.indexOf("\""));
            	    	 prod.setGroup(gr.replaceAll(";",":").replaceAll("&amp;", "&"));
                	   this.prGroups.put(gr.replaceAll(";",":").replaceAll("&amp;", "&"), gr.replaceAll(";",":").replaceAll("&amp;", "&")); 
                	   prod.setGroupSecond(Converter.cleanLitLatEst(grupp.replaceAll(";",":").replaceAll("&amp;", "&")));
                	 }
           	     }
      	 	 }

      	 	 len = line.indexOf("data code=\"GENERAL\""); 
      	 	 if(len != -1){
      	 		 try {
      	 		   String    areto =  line.substring(len + 29); 
       	 	      areto = areto.substring(0,areto.indexOf("\""));
                 // System.out.println("areto " + areto );
       	 	      prod.addBarcode(areto); 
      	 		 }catch(Exception e) {
      	 			 
      	 		 }
      	 	      	 	 }
      	 
      	 	 if(lang2 != null && line.indexOf("param=\"" + lang2 + "\"") != -1) {
           	 	
          	 	 len = line.indexOf("data code=\"ART_LANG\""); 
          	 	 if(len != -1){
          	 		 try {
          	 		   String   vahe =  line.substring(len + 30); 
           	 	    String  areto = vahe.substring(0,vahe.indexOf(">"));
                      //System.out.println("areto " + areto  + "\n vahe: " + vahe);
                      if(areto.contains("param=\"" + lang2 + "\"")) {
                    	  prod.setName(addid + " " + areto.substring(0,areto.indexOf("\"")) + " " +  add + shelf); 
                      } else {
                    	  len = vahe.indexOf("data code=\"ART_LANG\""); 
                    	//  System.out.println("len2 " + len);
                    	  if(len != -1){
                   	 		
                   	 		    vahe =  vahe.substring(len + 30); 
                    	 	     areto = vahe.substring(0,vahe.indexOf(">"));
                    	 	     
                    	 	   // System.out.println("areto2 " + areto );
                                if(areto.contains("param=\"" + lang2 + "\"")) {
                              	  prod.setName(addid + " " + areto.substring(0,areto.indexOf("\""))+ " " +  add + shelf); 
                                }else {
                                	len = vahe.indexOf("data code=\"ART_LANG\""); 
                              	  
                              	  if(len != -1){
                             	 		 try {
                             	 		    vahe =  vahe.substring(len + 30); 
                              	 	     areto = vahe.substring(0,vahe.indexOf(">"));
                              	 	     
                              	 	    //System.out.println("areto3 " + areto );
                                          if(areto.contains("param=\"" + lang2 + "\"")) {
                                        	  prod.setName(addid + " " + areto.substring(0,areto.indexOf("\""))+ " " +  add + shelf); 
                                          }	
                                
                   	 	 }catch(Exception ee) {
              	 			 
              	 		 }
                    	  
                              	  }  
                      }
                    	  }
           	 	  
          	 	      	 	 
                    	  }
          	 	 }catch(Exception ee) {
      	 			 
      	 		 }
          	 	 }
          	 //	 System.out.println(prod.getName());
          	 //System.exit(0);	 
          	  } 
      	 	 
      	 	 
      	  if(lang != null && line.indexOf("param=\"" + lang + "\"") != -1) {
      	 	
      	 	 len = line.indexOf("data code=\"ART_LANG\""); 
      	 	 if(len != -1){
      	 		 try {
      	 		   String   vahe =  line.substring(len + 30); 
       	 	    String  areto = vahe.substring(0,vahe.indexOf(">"));
                  //System.out.println("areto " + areto  + "\n vahe: " + vahe);
                  if(areto.contains("param=\"" + lang + "\"")) {
                	  prod.setName(addid + " " + areto.substring(0,areto.indexOf("\""))+ " " +  add + shelf); 
                  } else {
                	  len = vahe.indexOf("data code=\"ART_LANG\""); 
                	//  System.out.println("len2 " + len);
                	  if(len != -1){
               	 		
               	 		    vahe =  vahe.substring(len + 30); 
                	 	     areto = vahe.substring(0,vahe.indexOf(">"));
                	 	     
                	 	   // System.out.println("areto2 " + areto );
                            if(areto.contains("param=\"" + lang + "\"")) {
                          	  prod.setName(addid + " " + areto.substring(0,areto.indexOf("\""))+ " " +  add + shelf); 
                            }else {
                            	len = vahe.indexOf("data code=\"ART_LANG\""); 
                          	  
                          	  if(len != -1){
                         	 		 try {
                         	 		    vahe =  vahe.substring(len + 30); 
                          	 	     areto = vahe.substring(0,vahe.indexOf(">"));
                          	 	     
                          	 	    //System.out.println("areto3 " + areto );
                                      if(areto.contains("param=\"" + lang + "\"")) {
                                    	  prod.setName(addid + " " + areto.substring(0,areto.indexOf("\""))+ " " +  add + shelf); 
                                      }	
                            
               	 	 }catch(Exception ee) {
          	 			 
          	 		 }
                	  
                          	  }  
                  }
                	  }
       	 	  
      	 	      	 	 
                	  }
      	 	 }catch(Exception ee) {
  	 			 
  	 		 }
      	 	 }
      	 //	 System.out.println(prod.getName());
      	 //System.exit(0);	 
      	  }
      	  
      	  
      	  
      	 String  areto = "";
      	 	 len = line.indexOf("data code=\"ALK_REG\""); 
      	 	 if(len != -1){
      	 	      areto =  line.substring(len + 29); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                // System.out.println("areto " + areto );

      	 	 }

      	 	 len = line.indexOf("data code=\"TELEMA_EXP\""); 
      	 	 if(len != -1){
      	 	     goes = true;

      	 	 }      	 	
      	 	 
      	 	 double mahto = 0;
      	 	 String maht = "";
      	 	 len = line.indexOf("data code=\"KOGUS\""); 
      	 	 if(len != -1){
      	 		 try{ 
      	 		 maht =  line.substring(len + 18); 
      	 	      maht = maht.substring(0,maht.indexOf("\""));
                 //  System.out.println("maht " + maht );
                  
                	   mahto = Double.parseDouble(maht.replace(',', '.'));
                   }catch(Exception e){
                	   
                   }
      	 	 }
      	 	 
      	 	 */
      	 	if("true".equals(getParent().getProps().getProperty("stocktozero"))){
      	 		prod.setStock(0);
           	 }
      	 	 
      	 	
      	  try{
       		 Node sto =  getNode(rida ,"stocklevels"); 
       	 	      	   
      	 	 
      	 	 if( getParent().getProps().getProperty("stockcode") != null && sto != null){
      	 		 
      	 		 System.out.println("Stock " + sto.getAttributes().getNamedItem("level").getNodeValue());
      	 		double stoo = Double.parseDouble(sto.getAttributes().getNamedItem("level").getNodeValue());
      	 		prod.setStock(stoo);
      	 		
      	 	 }
     	         // prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
                   }catch(Exception e) {
                	   e.printStackTrace();
      		
      	}
     
      	 
      	if(prodCodes != null && !prodCodes.contains(prod.getID())){
      	  goes = false;
      	  if(prod.getStock() > 0){
      		  goes = true;
      	  }
      	}
      	try {
      	 String stop =  rida.getAttributes().getNamedItem("ART_STOPPED").getNodeValue();
      	if(stop.equals("Yes")){
      		goes = false;
      	}
      	stop =  rida.getAttributes().getNamedItem("E_AGENT").getNodeValue();
      	if(stop.equals("N")){
      		goes = false;
      	}
      	}catch(Exception e) {
      		
      	}
     
      	    if(goes ){
      	    	if(!usedGroups.contains(prod.getGroup())){
      	    		usedGroups.add(prod.getGroup());
      	    	}
      	    	
      	    	if(!usedGroups.contains(prod.getGroupSecond())){
      	    		usedGroups.add(prod.getGroupSecond());
      	    	}
      	    	
      	    	prodHash.put(prod.getID(), prod);
      	         tm.put(prod.getName(), prod);	
      	        }
      	      }
        }
     }catch(Exception e) {
    	 e.printStackTrace();
     }
   
 
     //teeme puhtaks grupi mapi 
     TreeMap grups  = new TreeMap();
    Iterator itera =  prGroups.keySet().iterator();
    while (itera.hasNext()){
    	String key = itera.next()+"";
    	if(usedGroups.contains(key)){
    		grups.put(key,prGroups.get(key) );
    	}
    }
    
    prGroups = grups;
    
    
     if(rcompany != null){
    	  Converter.sendProductJson(tm, rcompany);
	    }
     
    
       if(sb.length() > 0) {
    	   Converter.saveFileInUTF8("texts.txt", sb);
       }
       
       System.out.println("tooted tehtud ");
     
     return tm;
   }
   
   
   
   
   
   
   
   
   public TreeMap getProducts2(){

	    TreeMap tm = new TreeMap();
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		 // System.out.println(line);
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		      	      Product prod = new Product(id.substring(0,id.indexOf("\"")).trim());
		      	      len = line.indexOf("default_name=");
		      	     String name   = line.substring(len + 14);
		      	     prod.setName( prod.getID() + " " + Converter.deCode(name.substring(0,name.indexOf("\""))));
		      	     tm.put(prod.getID(), prod); 
	        	  }
	        	
	          }	    	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     return tm;
	     
	   }

   
   
   
   public TreeMap getProducts3(){
	   TreeMap tm = new TreeMap();
	    
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getParent().getProps().getProperty("stockdir") + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		        	  id = id.substring(0,id.indexOf("\""));
		        	 // System.out.println(id);
		      	 	  	 len = line.indexOf("<stocklevels>");
		          	 	 if(len != -1){
		          	 	     String stock   = line.substring(len + 13); 
		          	 	     //System.out.println(stock.substring(0,stock.indexOf("stocklevels")));
		          	 	     stock = stock.substring(0,stock.indexOf("stocklevels"));
		          	 	 if( getParent().getProps().getProperty("stockcode") != null){
		          	 		 
		          	 		 int ladu = stock.indexOf("stock=\"" + getParent().getProps().getProperty("stockcode") + "\"");
		          	 		// System.out.println(ladu);
		          	 		 if(ladu != -1){
		          	 			 String seis = stock.substring(ladu+5);
		          	 			// System.out.println("seis " + seis);
		          	 					 seis = seis.substring(seis.indexOf("level=") + 7);
		          	 					 seis = seis.substring(0, seis.indexOf("\""));
		          	 					 try{
		          	 					tm.put(id, Double.parseDouble(seis)); 
		          	 					 }catch (Exception e){
		          	 						e.printStackTrace(); 
		          	 					 }
		             		 }
		          	 	 }
		         	         // prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
		         	 	 }else{
		         	 		len = line.indexOf("stock_level_total=");
		             	 	 if(len != -1){
		             	 	     String stock   = line.substring(len + 19);
		             	 	     tm.put(id, Double.parseDouble(stock.substring(0,stock.indexOf("\"")))); 
		            	 	 }
		             	 }
		      	  }
	          }	    	 
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     return tm;
	     
	   }
   
   
   
   
   public TreeMap getProducts4(){
	   TreeMap tm = new TreeMap();
	    
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted" + 
	    			   getParent().getProps().getProperty("stockcode1")+ ".xml");
	    	   
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		        	  id = id.substring(0,id.indexOf("\""));
		        	 // System.out.println(id);
		      	 	  		len = line.indexOf("stock_level=");
		             	 	 if(len != -1){
		             	 	     String stock   = line.substring(len + 13);
		             	 	     tm.put(id, Double.parseDouble(stock.substring(0,stock.indexOf("\"")))); 
		            	 	 }
		             	 
		      	  }
	          }	    	 
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     return tm;
	     
	   }
   

   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   private Hashtable dueSums = new Hashtable();
   private Hashtable usedCredit = new Hashtable();

   public Vector makeDebtlist(){
	   //1.teeme klientide arvete nimekirja .
	   Vector debts = new Vector();
	   Enumeration enums = this.custDebtUsed.elements();
	   String gS = getParent().getProps().getProperty("keyInvoice");
  	    if(gS == null) {
  	    	System.out.println("keyInvoice is missing!");
  	    	System.exit(0);
  	    }
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		
		   try{
			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
			
			   String httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
			   Converter.doTrustToCertificates();
			 	URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        
		        String urlParameters  = "what=invoice&get=1&orderer=" + cuurl + "&key=" + gS;
		    	
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		        
		        
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = Converter.enCodeEst(inputLine);
		        	//System.out.println(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		String number = "";
			        	String date = "";
			        	String duedate = "";
			        	String volg = "";
			        	String payer = "";
		        		
			        	int ind =  line.indexOf("invoice number=") ;
		        		if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		 number = number.substring(0,number.indexOf("\""));
			        	}
			        	
			        	ind =  line.indexOf("date=") ;
				        	if(ind != -1){
				        		 date = line.substring(ind + 6);
				        		 date = date.substring(0,date.indexOf("\""));
				        	}
				        		
				        	
			        	 ind =  line.indexOf("due_date=") ;
			        	if(ind != -1){
			        		 duedate = line.substring(ind + 10);
			        		duedate = duedate.substring(0,duedate.indexOf("\""));
			        	 }
			        	
			        	 ind =  line.indexOf("regno=") ;
				        	if(ind != -1){
				        		 payer = line.substring(ind + 7);
				        		 payer = payer.substring(0,payer.indexOf("\""));
				        	 }
				        	
			        	
			        	ind =  line.indexOf("balance=") ;
				        	if(ind != -1){
				        		 volg = line.substring(ind + 9);
				        		 volg = volg.substring(0,volg.indexOf("\""));
				        	 }
				        //	System.out.println(custid  + " " + number + " " + date  + " " + duedate  + " " + volg );	
				       
			        	try{
			        		 double vv = Double.parseDouble(volg);
			        		 if(vv != 0){
			        			 if(isDueDate(duedate)){
						        		     Debt dd = new Debt();
						     				  dd.setCustID(custid);
						     				  
						     				  dd.setDoknr(number);
						     				  dd.setDate(getDate(date));
						     				  dd.setDuedate(getDate(duedate));
						     				  
						     				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
						     				  dd.setDue(ff + "");
						     				  dd.setSum(vv);
						     					//System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	
								        			  
						     				  if(dueSums.get(custid) != null){
						     					  double ss = Double.parseDouble(dueSums.get(custid) + "")+ vv ;
						     					 dueSums.put(custid, ss);    
						     				  }else{
						     					 dueSums.put(custid, volg); 
						     				  }
						     				 debts.add(dd);

									        		
						        	 }
			        			 

			     				  if(usedCredit.get(custid) != null){
			     					  double ss = Double.parseDouble(usedCredit.get(custid) + "")+ vv ;
			     					 usedCredit.put(custid, ss);    
			     				  }else{
			     					 usedCredit.put(custid, volg); 
			     				  }
			        			 
			        		 }
				        	
				        	  
			        	}catch(Exception e){
			        		//e.printStackTrace();
			        	}
				        
			        	 
		        	}           	
		        }
		      //  System.out.println(inputLineBuf);
		        in.close();
		        
		       
		   } catch (Exception a){
			   a.printStackTrace();
		   }
		   
	   }
	   
	     System.out.println("debts size" + debts.size());
	   
	   //  System.exit(0);
	   return debts;
   }
   private Vector custProds = new Vector();
   
   
   public Vector getCustomerProductsList(String agentCode){
	 // return new Vector();
	   return custProds;
   }
   
   public Vector getCustomerProductsList2(){
	   //1.teeme klientide arvete nimekirja .
	   
	   File cpdatadir = new File("cpdata");
	   if(!cpdatadir.exists()) {
		   cpdatadir.mkdir();
	   }
	    custProds = new Vector();
	
	   Calendar cal = Calendar.getInstance();
	   String d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	   String m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
	   
	   int days = 30 ;
	   if(getParent().getProps().getProperty("sortdays") != null){
		   try{
			   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
		   }catch(Exception e){
		   }
	   }
	   String end = d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
	   cal.add(Calendar.DATE, - days);
	   
	    d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	    m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
		   String start =  d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
		//   System.out.println(start + " uhuu " + end);
		   
		   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		   try{
			  // String httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=itemclass";
			   Converter.doTrustToCertificates();
			   custid = Converter.deCode(custid);
			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
				String httpsURL = logurl  + company + "/xmlcore.asp"; 
		  //     System.out.println(httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        
		        String gS = getParent().getProps().getProperty("keyInvoice");
		   	    if(gS == null) {
		   	    	System.out.println("keyInvoice is missing!");
		   	    	System.exit(0);
		   	    }
		        String urlParameters  = 
						"what=invoice&get=1&orderer=" + cuurl + "&invoicedate1=" + start + "&invoicedate2=" 
		            + end + "&key=" + gS;
		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		           
		        
		        
		        
		        
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        	//	System.out.println(" line " + line);
		        		int ind =  line.indexOf("invoice number=") ;
		        		String number = "";
			        	if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		number = number.substring(0,number.indexOf("\""));
			        		// System.out.println(number);
			        	}
			        	 ind =  line.indexOf("date=") ;
			        	if(ind != -1){
			        		String date = line.substring(ind + 6);
			        		date = date.substring(0,date.indexOf("\""));
			        		// System.out.println(date + " " + number + " " + custid);	
				        		
			        		 if(isOkDate(date)){
			        			// System.out.println(date + "  222 " + number + " " + custid);	
					        		
			     					Vector cps = this.getCP(Converter.deCode(custid),number,date);
			     					if(cps != null){
			     						Enumeration eee = cps.elements();
			     						while( eee.hasMoreElements()){
				     						custProds.add(eee.nextElement());
				     					//	System.out.println(date + " " + number + " " + custid);
			     						}			     				
					        			 	
			     					}
			     				}
			        	  }		
		        	}           	
		        }
		      //  System.out.println(inputLineBuf);
		        in.close();
		        isr.close();
		        ins.close();
		        con.disconnect();
		        
		       
		   } catch (Exception a){
			  // a.printStackTrace();
			  
		   }
		   
	   }
	   return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "DirectoLinpet";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private Vector custDebtUsed = new Vector();
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

	Iterator itera =   agent1.keySet().iterator();
	while(itera.hasNext()){
	
	String agentID = (String) itera.next();
		
	  try{

			String httpsURL = logurl  + company + "/xmlcore.asp?what=sale&get=1&code=" + agentID;
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
				Converter.doTrustToCertificates();
			    HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine =Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		String line = st.nextToken();
		        		//<sale code="EVA" total="10136.23"></sale>
		        		int ind =  line.indexOf("total=") ;
		        		String summa = "";
			        	if(ind != -1){
			        		summa = line.substring(ind + 7);
			        		summa = summa.substring(0,summa.indexOf("\""));
			                Agent agent = (Agent)agents.get(agentID);
			                 if (agent != null){
			                   agent.setMonthSales((int)Double.parseDouble(summa));
			        	   System.out.println("agsales0 " + agentID +" "+ summa);     	
			        	}
			                 System.out.println("agsales1 " + agentID +" "+ summa);     
		        	}
		        }}  
	  }catch(Exception e){
		  e.printStackTrace();
	  }	
		
		
		}  
	  }

@Override
public String getSaveStockInfo() {

	   StringBuffer products = new StringBuffer();
  	 
      
 try{
	 Converter.doTrustToCertificates();
   String httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=item";
    if(getParent().getProps().getProperty("stockcode") != null){
    	httpsURL = httpsURL + "&stock=" +  getParent().getProps().getProperty("stockcode");
	  }
  System.out.println(httpsURL);
  URL  myurl = new URL(httpsURL);
  HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
   InputStream ins = con.getInputStream();
   InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
   BufferedReader in = new BufferedReader(isr);

    String inputLine = "";
    StringBuffer inputLineBuf = new StringBuffer();
   while ((inputLine = in.readLine()) != null){
   	inputLine = Converter.enCodeEst(inputLine);
   	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
   }

   in.close();
   
   if(inputLineBuf.length() > 2){
         try {
       	  File f = new File (getDir() + "tooted.xml");
       	  if(f.exists())f.delete();
       	Converter.saveFileInUTF8(getDir() + "tooted.xml", inputLineBuf.toString());
      	
   	  }catch (Exception ex) {
   		ex.printStackTrace();
   	  }
    }
   

   //lõpeb toodetega tegelemine 

   System.out.println("Tooted tehtud" );

}catch(Exception e){
	e.printStackTrace();
}
 
 
 

 try {
	 

	   FileReader fr = new FileReader(getDir() + "tooted.xml");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     //line = reader.readLine();
     
  //   System.out.println("suurus " +prodsInUse.size());
      while((line = reader.readLine()) != null  ){
    	  
    	  if((line.trim().length()) > 40 ){
    	  int len = line.indexOf("code=");
    	  String id   = line.substring(len +6);
    	  id = id.substring(0,id.indexOf("\""));
 	 len = line.indexOf("<stocklevels>");
 	 double seisx = 0; 
if(len != -1){
    String stock   = line.substring(len + 13); 
    stock = stock.substring(0,stock.indexOf("stocklevels"));
if( getParent().getProps().getProperty("stockcode") != null){
	 int ladu = stock.indexOf("stock=\"" + getParent().getProps().getProperty("stockcode") + "\"");
	 if(ladu != -1){
		 String seis = stock.substring(ladu+5);
				 seis = seis.substring(seis.indexOf("level=") + 7);
				 seis = seis.substring(0, seis.indexOf("\""));
				 try{
					seisx = ( Double.parseDouble(seis)); 
				 }catch (Exception e){
					e.printStackTrace(); 
				 }
	 }
     }
}else{
	len = line.indexOf("stock_level_total=");
	 if(len != -1){
	     String stock   = line.substring(len + 19);  
      seisx = ((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
	 }
}
if(seisx > 0) {
	products.append(id +  "; ;");
	products.append(getParent().getNF().format(seisx) + "; "  +  "\n"); 
}

       	  }}
      reader.close();
    	  }catch(Exception tt){
    			 
    	  }

 
 return products.toString();
 
}
}
