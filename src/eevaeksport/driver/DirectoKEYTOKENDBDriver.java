package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.imp.driver.AbstractImportDriver;
import eevaeksport.util.Converter;
import eevaeksport.util.WebShopPiguLT;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DirectoKEYTOKENDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker,WebShopper,WebShopper2 {

  public DirectoKEYTOKENDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    companytoken = parent.getProps().getProperty("companytoken");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    logurl = "https://login.directo.ee/xmlcore/cap_xml_direct/xmlcore.asp?";
    
  
    if( parent.getProps().getProperty("stock.dir") != null){
    	stockConn = parent.getProps().getProperty("stock.dir");
    }
    
    if( parent.getProps().getProperty("weeks") != null){
    	WEEKS = Integer.parseInt(parent.getProps().getProperty("weeks"));
    }
    if("true".equals(getParent().getProps().getProperty("use.olddata"))){
		  useold = true;
		  }
  }
    
 private int WEEKS = 24;
  
  private String stockConn = null;
  String user =  null;
  String pass = null;
  String rcompany = null;
  String logurl = null;
  
  

  
  
  public TreeMap getProductsOther(){
	    TreeMap tm = new TreeMap();
	     if(stockConn != null){
	    	
	     try {
	    	 File f = new File (stockConn + "laoseis.xml");
	  		  if(f.exists()) {
	  			
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	        Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "transport");      
	      	  Node read = getNode(root, "stocklevels");
	          for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          	  Node rida = read.getChildNodes().item(i);
	          	  try{
	          		  if(rida.getNodeName().equals("stocklevel")){
	               		 String prodid  = rida.getAttributes().getNamedItem("code").getNodeValue();
	               		 double kog = Double.parseDouble(rida.getAttributes().getNamedItem("freequantity").getNodeValue());
	               		 tm.put(prodid, kog);  
	               	  } 
	          	  }catch(Exception e){}
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
	             	if( ff2[i].startsWith(toode+ '.')){
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

  
  
  private Logger log = Logger.getLogger(DirectoLinpetDBDriver.class.getName());
  
  private String companytoken = null;
  
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
   
    	Node read = getNode(root, "priceformularows");
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	  try{
        		  if(rida.getNodeName().equals("priceformularow")){
        			
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
	                             start =  rida.getAttributes().getNamedItem("date1").getNodeValue();
	                    	  }catch(Exception aa){ }
	                    	  try{
		                             end =  rida.getAttributes().getNamedItem("date2").getNodeValue();
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
                    	  Date alg = getCalXML(start); 
                          Date lopp = getCalXML(end); 
                          
                    	  start = start.substring(0,10).substring(5);
                    	  end = end.substring(0,10).substring(5);
                    	  mline.setStartDate(start);
                     	  mline.setEndDate(end);
                          
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
                         	 lines.add(mline);	
                          }
                        
                          
                      } else {
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
			//e.printStackTrace();
		}

		return result;

	}

   
   public TreeMap getProducts2(){
	   eanProds = new HashMap();

	    TreeMap tm = new TreeMap();
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
	    	   
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
	 	        	  try{
	 	        		  if(rida.getNodeName().equals("item")){
	 	        			  
	 	        			 String id = rida.getAttributes().getNamedItem("code").getNodeValue();
	 	        			 String name = rida.getAttributes().getNamedItem("name").getNodeValue();
	 	        			 String ean = rida.getAttributes().getNamedItem("barcode").getNodeValue();
	 	        			
	 	        			    Product prod = new Product(id);
	 	        			    prod.setName(name);
	 	        			    prod.setEAN(ean);
	 			          	    eanProds.put(ean, prod);
	 			          	 
	 			          	    tm.put(prod.getID(), prod); 
	 	        			    
	 	        		  }
	 	        	  }catch(Exception e) {
	 	        		  
	 	        	  }
	 	        }
	     
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     return tm;
	     
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
      
       	Node read = getNode(root, "priceformularows");
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  try{
           		  if(rida.getNodeName().equals("priceformularow")){
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
                         start =  rida.getAttributes().getNamedItem("date1").getNodeValue();
              	           }catch(Exception aa){ }
              	        
                        try{
                           end =  rida.getAttributes().getNamedItem("date2").getNodeValue();
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
                    //	e.printStackTrace();
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
	    
  
  private TreeMap<String,Double> getLaoSeisud (String ladu) {
	  TreeMap<String,Double> seis = new TreeMap<String,Double>();
	  
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
	      	  Node read = getNode(root, "stocklevels");
	          for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          	  Node rida = read.getChildNodes().item(i);
	          	  try{
	          		  if(rida.getNodeName().equals("stocklevel")){
	               		 String prodid  = rida.getAttributes().getNamedItem("code").getNodeValue();
	               		 double kog = Double.parseDouble(rida.getAttributes().getNamedItem("freequantity").getNodeValue());
	               		 String lkood = null;
	               		 try{
	               			 lkood = rida.getAttributes().getNamedItem("stock").getNodeValue();
	               		 }catch(Exception r){}
	               		 if(ladu == null) {
	               			seis.merge(prodid, kog, Double::sum);
	               		 } else if(lkood != null && ladu.endsWith(lkood)){
	               			seis.merge(prodid, kog, Double::sum);
	               		 }
	          		  } 
	          	  }catch(Exception e){}
	            }
	          }
	  	}catch(Exception e){}
	    
		  
	  
	  return seis;
  }
  
   public TreeMap getProdGroups2() throws SQLException {
    HashSet names = new HashSet();
	   String lang = null;
	   TreeMap res = new TreeMap();
	     if(getParent().getProps().getProperty("productgroup.lang") != null){
	      	 lang = getParent().getProps().getProperty("productgroup.lang");
	      }
	   
  	try {
  		  File f = new File (getDir() + "tootegrupid.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
          Node root = getNode(doc, "transport");      
       //   System.out.println(root);
      	Node read = getNode(root, "itemclasses");
      	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int i = 0; i < read.getChildNodes().getLength(); i++){
          	  Node rida = read.getChildNodes().item(i);
          	  try{
          		  if(rida.getNodeName().equals("itemclass")){
               		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
               		 try{
               			 String name  = rida.getAttributes().getNamedItem("name").getNodeValue(); 
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
               			r.printStackTrace(); 
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
		  File f = new File (getDir() + "supplier.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport"); 
        if(root != null){
     //   System.out.println(root);
    	Node read = getNode(root, "suppliers");
    //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
       if(read != null){
    	
    	
    	for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	  try{
        		  if(rida.getNodeName().equals("supplier")){
             		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
             		 try{
             			 String name  = rida.getAttributes().getNamedItem("name").getNodeValue(); 
                 		 group = Converter.cleanLitLatEst(group);
                 		  name  =  Converter.enCodeEst(name);
                 		//  prGroups.put(group, name);  
             		 }catch(Exception r){
             			 
             		 }
             	 
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
	}
  	
  	return res;
   }

   private boolean hasSupplier(){
	   boolean res = false;
		try {
			  File f = new File (getDir() + "supplier.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
	    	FileInputStream in = getCleanInputStream(f);
	    	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport"); 
	        if(root != null){
	     //   System.out.println(root);
	    	Node read = getNode(root, "suppliers");
	    //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	       if(read != null){
	    	
	    	
	    	for (int i = 0; i < read.getChildNodes().getLength(); i++){
	        	  Node rida = read.getChildNodes().item(i);
	        	  try{
	        		  if(rida.getNodeName().equals("supplier")){
	             		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
	             		 try{
	             			 String name  = rida.getAttributes().getNamedItem("name").getNodeValue(); 
	                 		 group = Converter.cleanLitLatEst(group);
	                 		  name  =  Converter.enCodeEst(name);
	                 		  res = true;  
	             		 }catch(Exception r){
	             			 
	             		 }
	             	 
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
 
   
    // System.out.println( getPricelists("1").toString());
   //  this.getProducts(null);
     //File ff = new File(this.getExcelDir() + "graafikud.xlsx");
   //  Converter.sendExcelVisits(ff,rcompany,user,pass);
     
     // loene võimalikud agendid sisse 
     agent1 = new TreeMap();
 	try {
 		  File f = new File (getDir() + "kliendid.xml");
 	      
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "transport"); 
     	 Node read = getNode(root, "customers");
     	
         for (int i = 0; i < read.getChildNodes().getLength(); i++){
         	  Node rida = read.getChildNodes().item(i);
         	 if(rida.getNodeName().equals("customer")){
         	  try{	
         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
         		// System.out.println("sm " + salesman);
         		 salesman = Converter.deCode(salesman);
         		 if(salesman.length() > 0) {
         		 Agent sm = new Agent (salesman);
         		 sm.setName(salesman);
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
	         			 String price_list = ensureLen(rida.getAttributes().getNamedItem("priceformula").getNodeValue());         	
		         	
		         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(price_list)){
	 	         		     pricelists.add(price_list.trim()); 
	 	         		   }
		         	      price_list =  rida.getAttributes().getNamedItem("specialpriceformula").getNodeValue() ;         	
				         	
		         	      StringTokenizer st  = new StringTokenizer(price_list,",");
		         	      if(st.hasMoreTokens()){
                            String aa = ensureLen(st.nextToken());
                            
			         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(aa)){
		 	         		     pricelists.add(aa); 
		 	         		   }
			         	      
		         	      }
         		 }
         	  }catch(Exception e){
         		 // e.printStackTrace();
         	  }
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
    // loadInvoices(WEEKS);
    // makeData();
   //  makeDebtlist();
    // setBulkSales(tm);
     return tm;
   }

   TreeMap agent1 = null;
   
   
   private boolean getUrlData(String param, String fname) {

       StringBuilder response = new StringBuilder();
	   try {
       URL url = new URL(this.logurl);
       HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
       connection.setRequestMethod("POST");
       connection.setDoOutput(true);
       connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

       // Build form parameters (same as --data-urlencode)
       String params =
               "token=" + URLEncoder.encode(this.companytoken, StandardCharsets.UTF_8.toString()) + param;
       
            //   "&get=" + URLEncoder.encode("1", StandardCharsets.UTF_8.toString()) +
            //   "&what=" + URLEncoder.encode("xxxxx", StandardCharsets.UTF_8.toString()) +
             //  "&ts=" + URLEncoder.encode("xx.xx.xxxx", StandardCharsets.UTF_8.toString());

       // Send request body
       try (OutputStream os = connection.getOutputStream()) {
           byte[] input = params.getBytes(StandardCharsets.UTF_8);
           os.write(input, 0, input.length);
       }

       // Get response
       int responseCode = connection.getResponseCode();
       System.out.println("Response Code: " + responseCode);

       InputStream responseStream =
               responseCode >= 200 && responseCode < 300
                       ? connection.getInputStream()
                       : connection.getErrorStream();

       try (BufferedReader br = new BufferedReader(
               new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {

           String line;
           while ((line = br.readLine()) != null) {
               response.append(line);
           }

           System.out.println("Response Body:");
          //.out.println(response.toString());
       }

       connection.disconnect();
	
	   } catch (Exception e) {
	       e.printStackTrace();
	   }
	   
	   
	   if(response.toString().length() > 2){
      	 try {
             	  File f = new File (getDir() + fname);
             	  if(f.exists())f.delete();
              Converter.saveFileInUTF8(getDir() + fname, response.toString());
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }else {
        	  return false;
          }
    return true;
   
   }


   
   public void downloadLastWeeks(int weeks) {

	    DateTimeFormatter apiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    WeekFields weekFields = WeekFields.ISO;

	    LocalDate today = LocalDate.now();

	    for (int i = 0; i < weeks; i++) {
   
	        LocalDate targetWeek = today.minusWeeks(i);
        LocalDate start = targetWeek.with(weekFields.dayOfWeek(), 1);
        LocalDate end = targetWeek.with(weekFields.dayOfWeek(), 7);
        String startStr = start.format(apiFormat);
	        String endStr = end.format(apiFormat);
        int year = targetWeek.get(weekFields.weekBasedYear());
	        int week = targetWeek.get(weekFields.weekOfWeekBasedYear());

	        String yyyyww = String.format("%d%02d", year, week);

	        File f = new File(this.getDir() + "invoice" + yyyyww + ".xml");
	        System.out.println(this.getDir() + "invoice" + yyyyww + ".xml" + " " + f.exists());
	        
	       // if(!f.exists() || i < WEEKS) {
	         getUrlData( "&get=1&what=invoice&date1=" + startStr + "&date2=" + endStr,
	            "invoice" + yyyyww + ".xml" );
	        //}
	        
	    }
	}
   
   

   
   private void makeData(){

	     
       try{
    	   
    	   boolean aaa = false;
    	   
    	   downloadLastWeeks(WEEKS);
    	 
    	  
    	   aaa = getUrlData("&get=1&what=customer" , "kliendid.xml");
           System.out.println("Kliendid tehtud " + aaa);
           
           aaa = getUrlData("&get=1&what=stocklevel" , "laoseis.xml");
           System.out.println("laoseis tehtud " + aaa);
    	   
    		aaa = getUrlData("&get=1&what=item" , "tooted.xml");
             System.out.println("tooted tehtud " + aaa);
 	      
        	aaa = getUrlData("&get=1&what=priceformularow" , "hinnakiri.xml");
             System.out.println("priceformularow tehtud " + aaa);
            	   
       	    aaa = getUrlData("&get=1&what=itemclass" , "tootegrupid.xml");
       	     System.out.println("Tootegrupid tehtud " + aaa);
           
       	    aaa = getUrlData("&get=1&what=supplier" , "supplier.xml");
             System.out.println("Supplier tehtud " + aaa);
             
       } catch (Exception ee){
   		 ee.printStackTrace();
		}

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
			
	  try{
   	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
   } catch (Exception e){
   	e.printStackTrace();
   }
		 
	     this.customers = new ArrayList();
	     this.debtList = makeDebtlist();
	     
	   try {
	 		  File f = new File (getDir() + "kliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport");      
	     	Node read = getNode(root, "customers");
	     	
	     	HashMap<String, Customer> payersDebt = new HashMap<String, Customer>();

	     	HashMap<String, String> payersPRLists = new HashMap<String, String>();
	     	
	     	  for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  if(rida.getNodeName().equals("customer")){
	         		 String name = null;
	         		boolean addaadr = true;
		         	  try{
		         		
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		  Customer cust = new Customer(id);
		         		 String vatregno = "";
			         		try{ 
			         			vatregno =  rida.getAttributes().getNamedItem("vatregno").getNodeValue(); 
			 	         		cust.setVatnr(vatregno);
			 	         		cust.setPayer(vatregno);
			 	         		
			 	         		if(rida.getAttributes().getNamedItem("specialpriceformula") != null) {
			 	         	      String price_list =  rida.getAttributes().getNamedItem("specialpriceformula").getNodeValue();

		 	         	    	  String old = payersPRLists.get(vatregno);
			 	         	      if(old != null) {
			 	         	    	  if(!old.contains(price_list+ ",")) {
			 	         	    	     payersPRLists.put(vatregno, old +  price_list + "," );
			 	         	    	  }
			 	         	       }else {
			 	         	         payersPRLists.put(vatregno, price_list + ",");
			 	         	      }
			 	         		}
			 	         		
				         		Double limit = 0.0;
				         		try{ 
				         			limit =  Double.parseDouble(rida.getAttributes().getNamedItem("creditlimit").getNodeValue()); 
				         			if(limit > 0)
				 	         		cust.setCredit(limit+"");
				 	         	 }catch(Exception ee){
				         		 }
		                         
				         	
				         		Double debt = null;
				         		try{ 
				         			debt =  Double.parseDouble(rida.getAttributes().getNamedItem("balance").getNodeValue()); 
				         			if(debt > 0) {
				 	         		cust.setUsedCredit(debt);
				         			payersDebt.put(vatregno, cust);
				         			}
				 	         	 }catch(Exception ee){
				         		 }
			 	         		
			 	         	 }catch(Exception ee){
			         		 }
	                         
		         	  }catch(Exception e) {
		         		  
		         	  }
	         	  }
	     	  }
	     	  
	     	 read = getNode(root, "customers");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("customer")){
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
		         		
		         	
		         		
		         		 String country = "Estonia";
		       		  if(getParent().getProps().getProperty("country") != null){
		       			  country = getParent().getProps().getProperty("country");
		       		  }
		       		//deliveryaddress1="Klonėnų vs. 1" deliveryaddress2="19156 Širvintų r" deliveryaddress3="Leedu" country="LT"
		         		 String kname = name;
		         		 String sendaadr = ""; 
		         		 
		         		try { 
		 	         		  sendaadr =  rida.getAttributes().getNamedItem("deliveryaddress1").getNodeValue();
		 	         		  String linn = "";
		 	         		try {
				 	         	    linn =  rida.getAttributes().getNamedItem("deliveryaddress2").getNodeValue().replaceAll(";", ","); 
				 	         	    sendaadr = sendaadr + " " + linn;
				 	         		customer.restCity = linn;
				 	         		}catch(Exception eed){
					         		 }
		 	         		  customer.restStreet = sendaadr.replaceAll(";", ",");
		 	         		  name = name + " " + sendaadr;
		 	         		addaadr = false;
		 	         		
		 	         		String maa = "";
		 	         		try{
		 	         	      maa =  rida.getAttributes().getNamedItem("deliveryaddress3").getNodeValue().replaceAll(";", ","); 
		 	         		  country = maa;

		 	         		  if("true".equals(getParent().getProps().getProperty("use.country"))){
		 	         			name = name + " " + maa;	  
		 	         		  }
		 	         		
		 	         		} catch(Exception eed){
			         		 
		 	         		}
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	
		         		
		         		} catch (Exception ee){
		         		 
		         		}
			         	
		         		
		         		String asula = "";
		         		try{ 
		 	         		  asula =  rida.getAttributes().getNamedItem("deliveryaddress2").getNodeValue().replaceAll(";", ","); 
		 	         		sendaadr = sendaadr + " " + asula;
		 	         		customer.restCity = asula; 
		 	         	
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("deliveryaddress3").getNodeValue().replaceAll(";", ","); 
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
                         
		         		String vatregno = "";
		         		try{ 
		         			vatregno =  rida.getAttributes().getNamedItem("vatregno").getNodeValue(); 
		 	         		customer.setVatnr(vatregno);
		 	         		customer.setPayer(vatregno);
		 	         	 }catch(Exception ee){
		         		 }
                         
		         		

		         		String limit = "";
		         		try{ 
		         			limit =  rida.getAttributes().getNamedItem("creditlimit").getNodeValue(); 
		 	         		customer.setCredit(limit);
		 	         	 }catch(Exception ee){
		         		 }
                         
		         		Double used = null;
		         		try{ 
		         			used =  Double.parseDouble(rida.getAttributes().getNamedItem("balance").getNodeValue()); 
		 	         		customer.setUsedCredit(used);
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		
		         		
		         		Customer cust = payersDebt.get(customer.getPayer()); 
		         		if( cust != null) {
		         			if(cust.getUsedCredit() != 0) {
		         				customer.setUsedCredit(cust.getUsedCredit());
		         			}
		         			if(cust.getCredit().trim().length() > 0) {
		         				customer.setCredit(cust.getCredit());
		         			}
		         		}
		         
		         		if(this.dueSums.get(id) != null){
		         			customer.setDebt(Double.parseDouble(dueSums.get(id)+ ""));
		         		}
		         		if(this.dueSums.get(customer.getPayer()) != null){
		         			customer.setDebt(Double.parseDouble(dueSums.get(customer.getPayer())+ ""));
		         		}

		         		try{ 
		         			double discount  =  Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
		         			if(discount > 0){
		         		 		customer.setDiscount(discount);
		    		 	  }
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		
		         		
		         		 
		         		 
		         		  customer.setName(name);
		         		 
		         		
		         		 //contact="Justyna Marciniak-Ashtiani" phone="+358503235307" email="justyna.marciniak-ashtiani@edenred.com"
		         		
		         		 try{ 
		 	         		 String yldphone =  rida.getAttributes().getNamedItem("phone").getNodeValue();
		 	         		 if(yldphone.trim().length() > 0){
		 	            		customer.setPersonalData((yldphone).replaceAll(";", ","));
		 	            		customer.restPhone = yldphone;
		 	  		         }
		 	     		 }catch(Exception ee){
                        	 customer.setAddress(""); 
		         		 }
		         		 
		         		try{ 
		 	         		 String kont =  rida.getAttributes().getNamedItem("contact").getNodeValue();
		 	         		 if(kont.trim().length() > 0){
		 	         			customer.restPerson = kont;
		 	         			
		 	         			if(customer.getPersonalData().trim().length() < 1){
		         					customer.setPersonalData((kont).replaceAll(";", ","));
			         			}else{
			         				String lisa = (kont).replaceAll(";", ",");
			         			    customer.setPersonalData(customer.getPersonalData() + "|" + lisa);
		         				}
		 	         			
		 	         			String cemail =  rida.getAttributes().getNamedItem("email").getNodeValue();
		         				customer.restPerEmail = cemail;
		 	  		         }
		 	     		 }catch(Exception ee){
                       	 customer.setAddress(""); 
		         		 }

		         		  
		         		
		         		
		         		  
		         		  try{
		         			 String price_list =  ensureLen(rida.getAttributes().getNamedItem("priceformula").getNodeValue());         	
			         		   customer.setGroup(Converter.deCode(price_list));
			         		  if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
			       			   customer.addFixSortCode(Converter.deCode(price_list)); 
			       		       }
		         		  }catch(Exception e){
		         		  }
		         		  
		         		  
		         		  
		
		         		  try{
		         			  String	 price_list =  rida.getAttributes().getNamedItem("specialpriceformula").getNodeValue() ;
		         			  
		         			  StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         				  
                                   String as = ensureLen(ss.nextToken());
			         			  
                                  if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
                                	  if(!customer.getGroup().contains(as+"|")) {
			         				    customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
                                	  }
					         	    }else{
			         				 customer.setGroup(Converter.deCode(as));
					         	   }
					         		
                                   if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
					       			   customer.addFixSortCode(Converter.deCode(as)); 
					       		   }  
		         			  }
		         			  
			         		  }catch(Exception e){
			         		  }
		         		  
		         		if( vatregno != null &&  payersPRLists.get(vatregno) != null) {
		         			String price_list = payersPRLists.get(vatregno);
		         			StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         			     String as = ensureLen(ss.nextToken());
			         		      if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
			         		    	 if(!customer.getGroup().contains(as+"|")) {
				         			 customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
			         		    	 }
			         		    	 } else {
			         				 customer.setGroup(Converter.deCode(as));
					         	    }
					         		
                                 if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
					       			   customer.addFixSortCode(Converter.deCode(as)); 
					       		   }  
		         			  }
		         		}
		 	         		
		         		  
		         		  
     		try {
	         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
	         		      customer.addAgent(new Agent(Converter.deCode(salesman)));
	         		     
	         		      String sm = parent.getEmail(salesman);
	         		      
	         		     Agent aa1 = (Agent)agent1.get(salesman);
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
	        		      
	         		    
	                  if(sm != null && sm.length() > 1){
	                	//  System.out.println(salesman+","+customer.getID()+ " " + customer.getName());
	            	      tm.put(customer.getName(), customer);
	                      this.customers.add(customer.getID());
	                  }
     		  }catch(Exception e){}
     		
     		
     		   	  }catch(Exception e){
	         		
	         		  e.printStackTrace();
	         	  }
	              
	         	  }}
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	
	   
	   if(tm.size()< 2) {
		   System.exit(0);
	   }
		   
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
	   
	    	Node read = getNode(root, "priceformularows");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	        	  Node rida = read.getChildNodes().item(i);
	        	  try{
	        		  if(rida.getNodeName().equals("priceformularow")){
	        			
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
		                             start =  rida.getAttributes().getNamedItem("date1").getNodeValue();
		                    	  }catch(Exception aa){ }
		                    	  try{
			                             end =  rida.getAttributes().getNamedItem("date2").getNodeValue();
			                    	  }catch(Exception aa){ }
	                         	                 
	                 } catch(Exception e){
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
	   
	   return re;
   }
   
   
   private HashMap eanProds = null;
   
   public TreeMap getProducts(Agent agent){
	 
	   TreeMap otherStocks = this.getProductsOther();
	   eanProds = new HashMap();
	   
	   TreeMap stocksfile = null;
	   try {
		   String scode = null;
		   if(getParent().getProps().getProperty("stockcode") != null ){
			   scode = getParent().getProps().getProperty("stockcode");
		   }
		   stocksfile = getLaoSeisud(scode); 
	   }catch(Exception e) {
	   }
	
	   
	   System.out.println("stockLisaXXXXX " + stocksfile.size());
	   
	   boolean addShelf = false ;
	   try {
		   getProdGroups2();
	     } catch (SQLException e1) {
		  e1.printStackTrace();
	     }
	   
     TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     HashSet usedGroups = new HashSet();
        
     
     if(custProds.size() == 0 && !useold){
         prodCodes = new HashSet();
    	getCustomerProductsList2();
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

     String allowedDataField = null;
     if(getParent().getProps().getProperty("ARTICLE_GROUP") != null){
    	 allowedDataField =  getParent().getProps().getProperty("ARTICLE_GROUP");
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
	        	  try{
	        		  if(rida.getNodeName().equals("item")){
	        			 
	        			  boolean goes = true;
	        			  
	        			 String id = rida.getAttributes().getNamedItem("code").getNodeValue();
	        			 
	        			 Node nd = rida.getAttributes().getNamedItem("name");
	        			 if(nd != null) {
	        			 String name = rida.getAttributes().getNamedItem("name").getNodeValue();
	        			
	        			    Product prod = new Product(id);
	        			    prod.setName(name);
	        			    
	        			    
	        	      	    if(proddatafile) {
	        	      	    	try{
	        	      	     String desc = rida.getAttributes().getNamedItem("description").getNodeValue();
	 	        	    		sb.append(id + ";" + desc + "\n"); 
	        	      	    	 }catch(Exception aa){}
	        	      	    }
	        	      	    	
		        			 try{
			        			 prod.setUnit(rida.getAttributes().getNamedItem("unit").getNodeValue());
		                 	    } catch(Exception aa){}	   
		        			 
		        			 
		        			 String add = "";
	        			 try{
	        				
	        			 String ean = rida.getAttributes().getNamedItem("barcode").getNodeValue();
	                 	     prod.setEAN(ean);
	               			 eanProds.put(ean, id);
	                  	     
	                    	   if("true".equals(getParent().getProps().getProperty("add.ean"))){
	                    		   try{ add = ean.substring(ean.length()-4); 
	                    	       }catch(Exception e ){}
	                    		 }
	                       }catch(Exception aa){}


       		        	  if("true".equals(getParent().getProps().getProperty("name.code.front"))){
       		     			 prod.setName((id + " " + name + " " +  add).trim()); 
       		     		 }else{
       		     			 prod.setName((name + " " + id + " " + add).trim()); 
       		     		 }
	        			

       		        	  if(getParent().getProps().getProperty("vat") != null){
       		        		  prod.setVat(getParent().getProps().getProperty("vat"));
       		        	  }
       		      	    
       		      	     prod.setPrice(1);
       		      	     
       		      	    try{
	        			 prod.setPrice(Double.parseDouble(rida.getAttributes().getNamedItem("salesprice").getNodeValue()));
                 	    } catch(Exception aa){}	 
       		      	    
       		      	 try{
	        			 prod.setInCases(Double.parseDouble(rida.getAttributes().getNamedItem("package1").getNodeValue()));
                 	    } catch(Exception aa){}	 
       		      	    
       		      try{
	        			 prod.setGroup(rida.getAttributes().getNamedItem("class").getNodeValue());
	        			 if(yesClasses.size() > 0 && !yesClasses.contains(prod.getGroup())){
	           	 	    	goes = false; 
	           	 	     }
              	    } catch(Exception aa){}	   
     			 
    		         
       	    	if(noTexts.size() > 0){
       	      		for(int u = 0 ; u < noTexts.size(); u++){
       	      			String aa = noTexts.elementAt(u) + "";
       	      			if(name.toLowerCase().contains(aa)){
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
       		      
       		
       		
       	
       	 String url = null;
       	 
       	 try{
			 url = (rida.getAttributes().getNamedItem("url").getNodeValue());
			 if(url.contains("?")) {
				 url = url.substring(0,url.lastIndexOf("?"));
			 }
     	     } catch(Exception aa){}	   
		 
       	// System.out.println(url);
       	 
       	  if(fixedurl != null) {
       		 prod.setPhotoid("phurl:" +fixedurl + id + ".jpg" ); 
  	      } else {
	  		 if(url!=null){
	      		 prod.setPhotoid("phurl:" +url); 
	      	 } 
  	      }
       		      
       	  
       	  if(stocksfile != null && stocksfile.get(prod.getID()) != null) {
       		 try{
 				 prod.setStock((int)Double.parseDouble(stocksfile.get(prod.getID())+""));  
 	      		}catch(Exception t){} 
       	   }
       		
       		        	
       		         	if(prodCodes != null && !prodCodes.contains(prod.getID())){
       		         	  goes = false;
       		         	  if(prod.getStock() > 0 ){
       		         		  goes = true;
       		         	  }
       		         	}
       		         	
       		         try{
       					 if("1".equals(rida.getAttributes().getNamedItem("closed").getNodeValue())){
       						goes = false; 
       					 }
       		     	     } catch(Exception aa){}	   
       				 
       		// vaatame kas data fields sees on olemas selline väli 
       		      
       		      if(allowedDataField != null) {
       		    	goes = false;
       		   NodeList dataList = rida.getChildNodes();//("data");

       		   String articleGroup = null;

       		   for (int is = 0; is < dataList.getLength(); is++) {

       		       Node dataElement = (Node) dataList.item(is);
       		    

		       if(dataElement.getNodeName().equals("datafields")) {
		    	   
		    	   for (int is0 = 0; is0 < dataElement.getChildNodes().getLength(); is0++) {

	       		       Node dataElement2 = (Node) dataElement.getChildNodes().item(is0);
		    	   
		    	   if(dataElement2.getNodeName().equals("data")) {
          		   	 String codeAttr = dataElement2.getAttributes().getNamedItem("code").getNodeValue();
       		         articleGroup = dataElement2.getAttributes().getNamedItem("content").getNodeValue();
       		       if (articleGroup.equals(allowedDataField)) {
       		    	   goes = true;
       		           break;
       		       }
       		       }
		    	   }
       		   }}
       		   
       		  
       		     
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
	               
       		        	  
       		        	  
       		        	  
	        }}
	        	  }catch (Exception e) {
	        		  e.printStackTrace();
	        	  }
	        }
       /*   
      	
    	   
           	 
           	 String kast = "package2=";
           	 if(getParent().getProps().getProperty("kastis") != null){
           		kast =  getParent().getProps().getProperty("kastis");
           	 }
       
           	 
      	  boolean goes = true;
  
	 	  
  	
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
      	 	 
      	 	 
      	 	if("true".equals(getParent().getProps().getProperty("stocktozero"))){
      	 		prod.setStock(0);
           	 }
      	 	 
      	 	 
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
      	 						prod.setStock( Double.parseDouble(seis)); 
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
        	          prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
        	 	 }
         	
     	 	 }
      	 	len = line.indexOf("stock_level=");
    	 	 if(len != -1){
    	 	     String stock   = line.substring(len + 13);  
    	 	     int st = (int)Double.parseDouble(stock.substring(0,stock.indexOf("\"")));
    	 	     if(st > 0) {
    	 	    	 prod.setStock(st);
    	 	     }
   	         
   	 	 }
      	 	 
      	 	 if(stockAlter.get(prod.getID()) != null){
      	 		 prod.setStock((Double)stockAlter.get(prod.getID()));
      	 	//  System.out.println("olemas "+ prod.getID() +" ");
        	  
      	 	 }
      	 	 
      	   if(otherStocks.get(prod.getID()) != null){
        		 try{
    				 prod.setStock((int)Double.parseDouble(otherStocks.get(prod.getID())+""));  
    	      		}catch(Exception t){
    				} 
        	   }
      	   
      	 
      	 	 
      	 
      	 	 
      	 
      	 if(stockLisa.get(prod.getID()) != null) {
      		 
      		// System.out.println("Lisan " +  stockLisa.get(prod.getID())  + " " + prod.getID());
      		 try{
				 prod.setPartii( getParent().getProps().getProperty("stockcode1") + ":" + (int)Double.parseDouble(stockLisa.get(prod.getID())+""));  
	      		}catch(Exception t){
				} 
      	   }
      	  if(line.indexOf("ARTIKKEL_KAMP") != -1){
      	    	prod.setStatusDiscount("1");
      	    }
     
    */
    
     } catch (Exception e){
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
    
    
    
       if(sb.length() > 0) {
    	   Converter.saveFileInUTF8("texts.txt", sb);
       }
       
       System.out.println("tooted tehtud " + tm.size());
     
     return tm;
   }
   
   
   
  
   
   


   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   private Hashtable dueSums = new Hashtable();
   private Hashtable usedCredit = new Hashtable();

   public Vector makeDebtlist(){
	   
	   
	   Vector debts = new Vector();
	   
		    DateTimeFormatter apiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		    WeekFields weekFields = WeekFields.ISO;
		    LocalDate today = LocalDate.now();

		    for (int i = 0; i < WEEKS; i++) {
	  
		   LocalDate targetWeek = today.minusWeeks(i);
	       LocalDate start = targetWeek.with(weekFields.dayOfWeek(), 1);
	       LocalDate end = targetWeek.with(weekFields.dayOfWeek(), 7);
	       String startStr = start.format(apiFormat);
		        String endStr = end.format(apiFormat);
	            int year = targetWeek.get(weekFields.weekBasedYear());
		        int week = targetWeek.get(weekFields.weekOfWeekBasedYear());

		        String yyyyww = String.format("%d%02d", year, week);

		        File f = new File(this.getDir() + "invoice" + yyyyww + ".xml");
		        System.out.println("invoices cp " + f.getName() + " " + f.exists());
		 	   
		        
		        if(f.exists()) {
		        	try {
		        		
		        		
		        		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        		DocumentBuilder db = dbf.newDocumentBuilder();

		        		try (FileInputStream in = getCleanInputStream(f)) {
		        		    Document doc = db.parse(in);
		        		    doc.getDocumentElement().normalize();

		        		    NodeList invoiceList = doc.getElementsByTagName("invoice");

		        		    for (int iu = 0; iu < invoiceList.getLength(); iu++) {
		        		        Node inv = invoiceList.item(iu);

		        		        if (inv.getNodeType() == Node.ELEMENT_NODE) {

		        		            Element invoiceElement = (Element) inv;

		        		            String number = invoiceElement.getAttribute("number");
		        		            String date = invoiceElement.getAttribute("date");
		        		            String duedate = invoiceElement.getAttribute("duedate");
		        		            String custid = invoiceElement.getAttribute("customercode");
		        		            String payer = invoiceElement.getAttribute("vatregno");
		        		            Double volg = 0.0;
		        		            try {
		        		              volg = Double.parseDouble(invoiceElement.getAttribute("balance"));
		        		            } catch(Exception r) {}
		        		          //  if(payer.equals("EE101795531"))
		    		        		//	 System.out.println("arve " + number + " " + payer+ " " + volg + " " + isDueDate(duedate));
		    	                   
		    	               	try{
		    		        		 double vv = volg;
		    		        		 if(vv != 0){
		    		        			
		    		        			 if(isDueDate(duedate)){
		    					        		     Debt dd = new Debt();
		    					     				  dd.setCustID(custid);
		    					     				  
		    					     				  dd.setDoknr(number);
		    					     				  dd.setDate(getDate(date));
		    					     				  dd.setDuedate(getDate(duedate));
		    					     				  if(payer != null) {
		    					     				   dd.setPayerID(payer);
		    					     				  }
		    					     				  
		    					     				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
		    					     				  dd.setDue(ff + "");
		    					     				  dd.setSum(vv);
		    					     				  
		    					     				  
								     				  if(dueSums.get(payer) != null){
								     					  double ss = Double.parseDouble(dueSums.get(payer) + "")+ vv ;
								     					 dueSums.put(payer, ss);    
								     				  }else{
								     					 dueSums.put(payer, volg); 
								     				  }
		    					     				 debts.add(dd);
				        		
		    					        	 }
		    		        		 }
		    			         	  
		    		        	}catch(Exception e){
		    		        		//e.printStackTrace();
		    		        	}
		        		   }
		        		 }	
		        		}
		        		        	
		        	} catch(Exception e) {
		        		e.printStackTrace();
		        	}
		        }
		        }
	
	 
	   
	     System.out.println("debts size" + debts.size());
	   
	   return debts;
   }
   private Vector custProds = new Vector();
   
   
   public Vector getCustomerProductsList(String agentCode){
	 // return new Vector();
	   return custProds;
   }
   
   private  Vector<CustProd> loadInvoices(int weeks) {
        if(prodCodes == null) {
        	prodCodes = new HashSet();
        }
	   HashSet<String> olemas = new HashSet<String>();
	   
         Vector<CustProd> res = new Vector<CustProd>();
	    DateTimeFormatter apiFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    WeekFields weekFields = WeekFields.ISO;
	    LocalDate today = LocalDate.now();

	    for (int i = 0; i < weeks; i++) {
  
	   LocalDate targetWeek = today.minusWeeks(i);
       LocalDate start = targetWeek.with(weekFields.dayOfWeek(), 1);
       LocalDate end = targetWeek.with(weekFields.dayOfWeek(), 7);
       String startStr = start.format(apiFormat);
	        String endStr = end.format(apiFormat);
            int year = targetWeek.get(weekFields.weekBasedYear());
	        int week = targetWeek.get(weekFields.weekOfWeekBasedYear());

	        String yyyyww = String.format("%d%02d", year, week);

	        File f = new File(this.getDir() + "invoice" + yyyyww + ".xml");
	        System.out.println("invoices cp " + f.getName() + " " + f.exists());
	 	   
	        
	        if(f.exists()) {
	        	try {
	        		
	        		
	        		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        		DocumentBuilder db = dbf.newDocumentBuilder();

	        		try (FileInputStream in = getCleanInputStream(f)) {
	        		    Document doc = db.parse(in);
	        		    doc.getDocumentElement().normalize();

	        		    NodeList invoiceList = doc.getElementsByTagName("invoice");

	        		    for (int iu = 0; iu < invoiceList.getLength(); iu++) {
	        		        Node inv = invoiceList.item(iu);

	        		        if (inv.getNodeType() == Node.ELEMENT_NODE) {

	        		            Element invoiceElement = (Element) inv;

	        		            String number = invoiceElement.getAttribute("number");
	        		            String date = invoiceElement.getAttribute("date");
	        		            String customerID = invoiceElement.getAttribute("customercode");
	        		            String totalAmount = invoiceElement.getAttribute("totalamount");
	        		          //  System.out.println("invoices cp yyy " + customerID);
	        		     	   
	        		            NodeList rows = invoiceElement.getElementsByTagName("row");

	        		            for (int r = 0; r < rows.getLength(); r++) {
	        		                Element row = (Element) rows.item(r);

	        		                String item = row.getAttribute("item");
	        		                String description = row.getAttribute("description");
	        		                Double hind = Double.parseDouble(row.getAttribute("price"));
	        		                
	        		                Double kogus =  0.0;
	        		                try {
	        		                	kogus =  Double.parseDouble(row.getAttribute("quantity"));
	        		                }catch (Exception e) {}
	        		                String sum = row.getAttribute("sum");
	        		            	
	        		   		        if(item != null && item.trim().length() > 0 && !olemas.contains(item + "_" + customerID)){
	        		   		        	
	        		   			    res.add( new CustProd(customerID, item, kogus ,0 ,0,date.substring(5,10),hind,0,0," "));
	        		   			    

	        		   			        if(!prodCodes.contains(item)){
	        		   				        prodCodes.add(item);	
	        		   			        }
	        		   			     olemas.add(item + "_" + customerID);
	        		               }

	        		        }
	        		   }
	        		}	
	        		}
	        		        	
	        	} catch(Exception e) {
	        		e.printStackTrace();
	        	}
	        }
	        }
	    System.out.println("invoices cp size " + res.size());
		   
	    
	    return res;
	}
   
   public Vector getCustomerProductsList2(){
	  
	    custProds =  loadInvoices(WEEKS);

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

			String httpsURL = logurl  +  "/xmlcore.asp?what=sale&get=1&code=" + agentID;
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

	boolean ok =  getUrlData("&get=1&what=stocklevel" , "laoseis.xml");
     System.out.println("laoseis tehtud " + ok);
	
	   StringBuffer products = new StringBuffer();
	   
	   try {
		   String scode = null;
		   if(getParent().getProps().getProperty("stockcode") != null ){
			   scode = getParent().getProps().getProperty("stockcode");
		   }
		   TreeMap<String,Double> seisud =  getLaoSeisud(scode);
		  for(String id : seisud.keySet()) {
			  Double seisx = seisud.get(id);
			  if(seisx > 0) {
					products.append(id +  "; ;");
					products.append(getParent().getNF().format(seisx) + "; "  +  "\n"); 
				}  
		  }
		   
	   }catch(Exception e) {  }

 return products.toString();
 
}

public ArrayList loadOpenOrders() {
	  
	 String httpsURL =  logurl +  "/xmlcore.asp?get=1&what=order";
	 System.out.println("httpsURL " + httpsURL);
	 Calendar yesterday = Calendar.getInstance();
	 // HILJEM ASENDA -2 -1GA
	 yesterday.add(Calendar.DAY_OF_YEAR, -2);
	 String ydate = Converter.getDDpMMpYYYYpString(yesterday);
	 String tdate = Converter.getDDpMMpYYYYpString(Calendar.getInstance());
	 String URLTSToday = httpsURL + "&ts=" + tdate; 
	 String URLTSYesterday = httpsURL + "&ts=" + ydate; 
	 
	 
     ArrayList arr = new ArrayList();
   System.out.println(URLTSToday);
   try {
   	
  
    URL myurl = new URL(URLTSToday);
    URLConnection con = (HttpsURLConnection)myurl.openConnection();
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
       	  File f = new File (getDir() + "torders.xml");
       	  if(f.exists())f.delete();
       	Converter.saveFileInUTF8(getDir() + "torders.xml", inputLineBuf.toString());
      	
   	  }catch (Exception ex) {
   		ex.printStackTrace();
   	  }
    }
   
   }catch(Exception e) {
   	e.printStackTrace();
   } 

   
   
   
   try {
	   	
	   
	    URL myurl = new URL(URLTSYesterday);
	    URLConnection con = (HttpsURLConnection)myurl.openConnection();
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
	       	  File f = new File (getDir() + "yorders.xml");
	       	  if(f.exists())f.delete();
	       	Converter.saveFileInUTF8(getDir() + "yorders.xml", inputLineBuf.toString());
	      	
	   	  }catch (Exception ex) {
	   		ex.printStackTrace();
	   	  }
	    }
	   
	   }catch(Exception e) {
	   	e.printStackTrace();
	   } 
	//System.exit(0);
   HashSet loaded = new HashSet();
   
   for (int i = 0; i < 2; i++) {
	   String fName = "yorders.xml";
	   if (i == 1) {
		   fName = "torders.xml";
	   }
		   
	   
	 String  kaup24Klient = parent.getProps().getProperty("kaup24");
     String piguLTKlient = parent.getProps().getProperty("piguLT");
		String lati220Klient = parent.getProps().getProperty("220LV");
	   
 
	
		try {
			 File f = new File (getDir() + fName);
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node read = getNode(doc, "transport");      
	      //   System.out.println(root);
	    
	     //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int t = 0; t < read.getChildNodes().getLength(); t++){
	         	  Node rida = read.getChildNodes().item(t);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	 // if(rida.getNodeName().equals("order")){
	         		
		         	
		         	  String id = rida.getAttributes().getNamedItem("ccode").getNodeValue();
		         	  if (id.equals(kaup24Klient) || id.equals(piguLTKlient) || id.equals(lati220Klient)) {
		         	  
		         	  
		      
			    	  String arve = rida.getAttributes().getNamedItem("webid").getNodeValue();
			    	  
			    	  String number = null ;
			    	  try {
			    		  number = rida.getAttributes().getNamedItem("cutomer_orderno").getNodeValue();
			    	  }catch(Exception e) {
			    		  
			    	  }
			    		
			    	  
			   if (number != null && number.trim().length() > 0) 	{
				   
				   Order savedOrder = null ;
				   try {
				   savedOrder = (Order) Converter.readObject("WebShopper\\ordIDs\\" + number + ".order");
				   }catch(Exception e) {
					   
				   }
			    	if (savedOrder != null) {
			    	String kuup = null;
			    String kuup2 = null;
			    	  try {
			    	  kuup =  (rida.getAttributes().getNamedItem("webdate").getNodeValue()).substring(0,10);
			    	  kuup2 =  (rida.getAttributes().getNamedItem("webdate").getNodeValue()).substring(0,10);
			    	  }catch(Exception e) {
			    		  
			    	  }
			    	boolean goes = false; 
			    	
			    	try {
			    	String status =  (rida.getAttributes().getNamedItem("status").getNodeValue());
	    			 if(status != null && status.equals("DONE") ) { // coop
	    				if(!loaded.contains(arve)) {
	    				 goes = true;
	    				 loaded.add(arve);
	    			 }}
			    	}catch(Exception e) {
			    		
			    	}
			    	  if (goes){
			    		  
			    	 

			    		  
			    Order ord = new Order(id, null);
			    ord.setDokNr(number);
			    String comm = rida.getAttributes().getNamedItem("comment").getNodeValue();
			    
			    Vector lines = new Vector();
			    	Node read2 = getNode(rida, "lines");
			    	
			      for (int u = 0; u < read2.getChildNodes().getLength(); u++){
			    	  
			      Node rida2 = read2.getChildNodes().item(u);
			      String toode = null;
			  try {
			    	   toode = rida2.getAttributes().getNamedItem("pcode").getNodeValue();
			     }catch(Exception e) { }
			  
			  if(toode != null) {
			    	  Node nnn = rida2.getAttributes().getNamedItem("description");
			    	  
			    	  String nimi = "pole nime";
			    	  if(nnn != null) {
			    		  nimi = nnn.getNodeValue();
			    	  }
			    	  double kogus =  0;
			    	  try {
			    		  kogus =  Double.parseDouble(rida2.getAttributes().getNamedItem("quantity").getNodeValue()); 
			    	  
			    	}catch(Exception e) {
		    		 
		    	  }
			    	  
			    	String orderProdID = null;
			    	
			    	
			    	
			    	 // System.out.println(id + " " + number + " " +  toode );
			    	  OrderLine ordLine = new OrderLine();
			    	  ordLine.setToode(toode);
			    	  ordLine.setKogus(kogus);
			    	 
			    	 
			    	  Vector savedOrderVec =  savedOrder.getOrderLines();
			    	  //saved ordersist vector kus sees on orderlinesid, kai vector labi, milline toode on seal orderliensis ja vaata kas matchib praeguse tootega
			    	  for(int e = 0; e <  savedOrderVec.size() ; e++) {
			    		  OrderLine sOrdLine =(OrderLine) savedOrderVec.get(e);
			    		  if (sOrdLine.getToode().equals(toode)) {
			    			  ordLine.selActCode = sOrdLine.selActCode;
			    			  lines.add(ordLine);
			    		  }
			  
			    	  }	
			        }
			  }
			      
			  
			      
			      ord.setOrderLines(lines);
			  
			    	  arr.add(ord);
			    	  // System.out.println(id + " " + comm );
			    	  }
				  
			        }
			   }
		         	   }
			      
	         }
		      
			 } catch (Exception e) {
		 		  getParent().showError(e);
		 	 }
   }
   
  // System.exit(0);
   //System.out.println(arr.toString());
  	return arr;
  }


@Override
public void WebShopper()  {
	
	
	
	
	
	getProducts2();
	WebShopPiguLT vp = new WebShopPiguLT(eanProds);
	
	vp.login(parent);
	vp.getSellerID();
	
	
	

	
	HashSet orderHistory = null;
	try {
		orderHistory = (HashSet)Converter.readObject("orderHistory.hash");
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	if (orderHistory == null) {
		orderHistory = new HashSet();
	}
	ImportPanel importPanel = null;
	try {
		 importPanel = new ImportPanel(parent.getParentFrame());
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	AbstractImportDriver driver = importPanel.driver;
	

	
	ArrayList orders = vp.getOrderList();
	System.out.println(orders.toString());
	for (int i = 0; i < orders.size(); i++) {
		Order ord = (Order) orders.get(i);
		if (!orderHistory.contains(ord.getDokNr())) {
			System.out.println(ord.getDokNr());
			  try {
				driver.makeOrder(ord);
				  vp.saveOrderIDs(ord);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			
			orderHistory.add(ord.getDokNr());
		}
	}   
	try {
		Converter.saveObject(orderHistory, "orderHistory.hash");
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	
	
	System.exit(0);
}




@Override
public void WebShopper2() {
	
	getProducts2();
	WebShopPiguLT vp = new WebShopPiguLT(eanProds);
	
	vp.login(parent);

	vp.getSellerID();
	
	
	ArrayList confOrders = loadOpenOrders();
	System.out.println("confOrders " + confOrders.size());
     vp.confirmLinesGetSlip(confOrders);
	
	
	
	System.exit(0);
}
}
