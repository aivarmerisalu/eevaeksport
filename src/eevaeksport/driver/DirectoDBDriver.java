package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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

public class DirectoDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public DirectoDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    company = parent.getProps().getProperty("company");
    
    if( parent.getProps().getProperty("stock.dir") != null){

    	stockConn = parent.getProps().getProperty("stock.dir");
    	
    }
    
    if("true".equals(parent.getProps().getProperty("logistika.pluss"))){
    	lpnum = parent.getProps().getProperty("logistika.pluss.user");
  	  
      try {
		conlp = DriverManager.getConnection(parent.getProps().getProperty("logistika.pluss.url"),lpnum,
				parent.getProps().getProperty("logistika.pluss.pass"));
	} catch (SQLException e) {
		e.printStackTrace();
	}
    }	
    
  }
  
  private String lpnum = null;
  private String stockConn = null;
  private String company = null;
  private Connection conlp;
  
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
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	/*
   	try {
    FileReader fr = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader = new BufferedReader(fr);
  String line = null;

  while(!(line = reader.readLine()).equals("\"[Get DiscountMatrix]\"")){
  }
  line = reader.readLine();
    System.out.println("line" +line);
  while(((line = reader.readLine()).trim().length()) > 3 ){
  	
    line = line.replaceAll("\",\"", "|");
    line = line.replaceAll("\",", "|");
    line = line.replaceAll("\""," ");
 
    StringTokenizer st = new StringTokenizer(line, SEPARATOR);
    double price = 0;
	String custID = null;
	String clGroup = null;
	String prodID = null;
	String discGroup = null;
	double discount = 0;
		if(st.hasMoreTokens()){
			custID = cleardots(st.nextToken());
			
		}
				if(st.hasMoreTokens()){
		 clGroup = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				 prodID = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				discGroup = cleardots(st.nextToken());	
			}
			
			if(st.hasMoreTokens()){
				 discount = Double.parseDouble(cleardots(st.nextToken()).replaceAll(",","."));	
			}
			
		//	System.out.println("'" + custID + "'" + clGroup + "'" + prodID + "'" + discGroup + "'" + discount) ;
			if(st.hasMoreTokens()){
				price = Double.parseDouble(cleardots(st.nextToken()));	
			}
			MatrixLine mline = new MatrixLine();

			if (custID != null && custID.trim().length() > 0) {
				mline.setCustID(custID.trim());
			}
			if (mline.getCustID() == null && clGroup != null
					&& clGroup.trim().length() > 0) {
				mline.setCliGroup(clGroup.trim());
			}

			if (prodID != null && prodID.trim().length() > 0) {
				mline.setProdID(prodID.trim());
			}
			if (mline.getProdID() == null && discGroup != null
					&& discGroup.trim().length() > 0) {
				mline.setDiscGroup(discGroup);
			}
			mline.setDiscount(discount);

			if (price != 0) {
				mline.setPrice(price);
			}
			lines.add(mline);
		}

	  

	} catch (Exception ex) {
		ex.printStackTrace();
	}*/
	return lines;

   }

 //  public Vector getFixedAssortmentData() {
      // return fixedLines ;
 //}
   
   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		this.s(null);
		File f = new File(this.getExcelDir() + "/sortiments.xls");
				
		try {
			
			
			/*  String sql = "SELECT DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) from mj_assortment where res3 = 'EE' and DATEADD(dd, 0, DATEDIFF(dd, 0, sortdate)) = DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) ";
       	  boolean insertassort = true;
        	
       	   Connection con = getNewConnection(url);
 	           Statement stmt = con.createStatement();
 	        
 	        ResultSet rs = stmt.executeQuery(sql);
 	        if(rs.next()){
 	        	insertassort = false;
 	        	//kontrollime kas on vaja insertida 
 	        }
 	        rs.close();
 	        stmt.close();
 	        con.close();
 	        
 	        System.out.println("Sisestame sortimenti " + insertassort );
			*/
			
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

		//	 System.out.println("omagroups " + omagroups.toString());

			
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
				
			// System.out.println("omaTootegroups " + omaTootegroups.toString());
			
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
			
		//	System.out.println("TulemusKood " + tulemusKood.toString());
			
			
			
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

			// System.out.println("groups" + groups.toString());

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
       TreeMap prods = this.s2();
       System.out.println("prods " + prods.size());
       
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
                     String prodID = " ";
                     String prodGroup = " ";
                     MatrixLine mline = new MatrixLine();
                    try{
                    	 clGroup = rida.getAttributes().getNamedItem("code").getNodeValue(); 
                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue(); 
                         if (custID != null && custID.trim().length() > 0) {
                           mline.setCustID(custID.trim());
                         }
                         if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                         }

                         if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                         }
                         
                         if (mline.getProdID() == null && prodGroup != null
                             && prodGroup.trim().length() > 0) {
                           mline.setProdGroup(prodGroup);
                         }

                        
                         if(prods.keySet().contains(prodID) && pricelists.contains(clGroup.trim())){
                        	
                        	 
                        	if(!prodsInUse.contains(prodID.trim())){
                           	  this.prodsInUse.add(prodID.trim());	
                        	}
                        	 
                            lines.add(mline);	
                          }
                    
                       
                    }catch(Exception e){
                    }
                    
                   }
                	
           	  }catch(Exception e){
           		  e.printStackTrace();
           	  }
                
           }
           
   	}catch(Exception e){
   		e.printStackTrace();
   	}
     //  System.out.println(prodsInUse.size() );
       
       return sortLines(lines,prods);
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	        Vector vec = new Vector();
	       // System.out.println("Prod22 " + products.size());
	        for (int i = 0 ; i < inVec.size() ; i++){
	          vec.addElement(inVec.elementAt(i));
	        }
	        Vector sorted = new Vector(vec.size());
	        while(vec.size() > 0){
	          MatrixLine smallest = null;
	          Product smProd = null;
	          for (Enumeration enumr = vec.elements(); enumr.hasMoreElements() ; ){
	        	  MatrixLine ol = (MatrixLine)enumr.nextElement();
	            Product prod = (Product)products.get(ol.getProdID());
           //   System.out.println("Prod " + prod + "  " + ol.getProdID());
	            if ( (smallest == null && smProd == null )
	            		|| ( prod.getName().compareTo(smProd.getName()) < 0 )) {
	              smallest = ol;
	              smProd = (Product)products.get(smallest.getProdID());
	            }
	          }
	          vec.removeElement(smallest);
	          sorted.addElement(smallest);
	        }
	        return sorted;
	      }
	


   public String SortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }

   private HashMap stockslp = new HashMap();
 
   public String getLPStockInfo() {
		
		String cmd = " select [LPK kauba nr] as toode ," +
				"Kogus-[Kogus muugitellimustel] as stock from [vLPK Laoseis] where [LPK nr] = '" + lpnum+ "'";
		
		if (isDebug())
			System.out.println(cmd);
		StringBuffer products = new StringBuffer();
		try {

			Statement st = conlp.createStatement();
			ResultSet rs = st.executeQuery(cmd);

			while (rs.next()) {

				String kood = rs.getString("toode").trim();
				double seis = rs.getDouble("stock");
				if (seis > 0) {
					products.append(kood + ";");
					products.append(" ;");
					products.append(getParent().getNF().format(seis) + "; "
							+ "\n"); // + dealer + "\n");
					stockslp.put(kood, seis+ "");
				}
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		return products.toString();
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
  
   public TreeMap getProdGroups() throws SQLException {
    
     
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
      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int i = 0; i < read.getChildNodes().getLength(); i++){
          	  Node rida = read.getChildNodes().item(i);
          	  try{
          		  if(rida.getNodeName().equals("itemclass")){
               		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
               		 try{
               			 String name  = rida.getAttributes().getNamedItem("default_name").getNodeValue(); 
                   		 group = deCode(group);
                   		  name  =  deCode(name);
                   		  prGroups.put(group, name);  
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
         return prGroups;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
        }



   private String enCode(String res ){
	 
	   return Converter.enCodeEst(res);
	  }
	  
   
   public static String deCode(String res ){
	   
 	return Converter.deCode(res);
 }
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
     //getFixedAssortmentData();
  //  makeData();
     
     // loene võimalikud agendid sisse 
   
 	try {
 		  File f = new File (getDir() + "kliendid.xml");
 	     
 	      //  System.out.println(f.getAbsolutePath());
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
         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
         		 salesman = deCode(salesman);
         		 Agent sm = new Agent (salesman);
         		 sm.setName(salesman);
                  tm.put(salesman, sm); 
                  
                  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
                  if(!parent.getEmail(salesman).equals("") &&  !custtsUsed.contains(id)){     		   
	         		   custtsUsed.add(id);
      		      }
	         			 String price_list =  rida.getAttributes().getNamedItem("price_list").getNodeValue() ;         	
		         	
		         	      if(!parent.getEmail(salesman).equals("") &&  !pricelists.contains(price_list)){
	 	         		     pricelists.add(price_list.trim()); 
	 	         		   }
	         	  
         	  }catch(Exception e){
         		 // e.printStackTrace();
         	  }
         }
         
 	}catch(Exception e){
 		e.printStackTrace();
 	}
     
     System.out.println("Agendilist tehtud" );
     System.out.println("Hinnakirju kasutusel "  + pricelists.size() +  " " + pricelists.toString() );
     System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());
     fixedLines = getFixedAssortmentData1();
     return tm;
   }

   
   
   private void makeData(){
	   

	     
       try{
       	String httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=itemclass";
       	
       	Converter.doTrustToCertificates();
           URL myurl = new URL(httpsURL);
           HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
           InputStream ins = con.getInputStream();
           InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
           BufferedReader in = new BufferedReader(isr);

           String inputLine;
           StringBuffer inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLine = this.enCode(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
          }

           in.close();
           
           if(inputLineBuf.length() > 2){
             
                 try {
               	  File f = new File (getDir() + "tootegrupid.xml");
               	  if(f.exists())f.delete();
               	  
               	  
             	 Converter.saveFileInUTF8(getDir() + "tootegrupid.xml", inputLineBuf.toString());
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
           
           //lõpeb tootegruppidega tegelemine 
           System.out.println("Tootegrupid tehtud" );
           //algab Hinnakirja faili moodustamine
           
           httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=priceformula";
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = this.enCode(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
         
                try {
              	  File f = new File (getDir() + "hinnakiri.xml");
              	  if(f.exists())f.delete();
              	 Converter.saveFileInUTF8(getDir() + "hinnakiri.xml", inputLineBuf.toString());
          	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }
          

          //lõpeb toodetega tegelemine 

          System.out.println("Hinnakirjad tehtud" );
           
           
           
           
           //algab tootede faili moodustamine
           
            httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=item";
            myurl = new URL(httpsURL);
            con = (HttpsURLConnection)myurl.openConnection();
            ins = con.getInputStream();
            isr = new InputStreamReader(ins,"UTF-8");
            in = new BufferedReader(isr);

            inputLine = "";
           inputLineBuf = new StringBuffer();
           while ((inputLine = in.readLine()) != null){
           	inputLine = this.enCode(inputLine);
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
           //algab kliendi faili moodustamine
           
           httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=customer";
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = this.enCode(inputLine);
           	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));	
          	
       //       System.out.println(inputLine);	
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
             
                 try {
                    String res = "";
               	  StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
               	  while(st.hasMoreTokens()){
               		String line = st.nextToken();
               		//if(line.indexOf("\"10173\"") == -1 && line.indexOf("\"13543\"") == -1 && line.indexOf("<contact code=\"2575\"") == -1 ){
               			res = res + line + "\n";
               		//}
               	  }
               	  File f = new File (getDir() + "kliendid.xml");
               	  if(f.exists())f.delete();
               	Converter.saveFileInUTF8(getDir() + "kliendid.xml", inputLineBuf.toString());
              	
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


   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     makeData();

		  String country = "Estonia";
		  if(getParent().getProps().getProperty("country") != null){
			  country = getParent().getProps().getProperty("country");
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
	      //   System.out.println(root);
	     	Node read = getNode(root, "customers");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("customer")){
	         		 String name = null;
		         	  try{
		         		   name =  rida.getAttributes().getNamedItem("name").getNodeValue();
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		  Customer customer = new Customer(id);
		         		  String kkood = id;
		         		  try{
		         			  String payer = rida.getAttributes().getNamedItem("regno").getNodeValue();
			         		  
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
		         		
		         		String kname = name;
		         		 String sendaadr = ""; 
		         		try{ 
		 	         		  sendaadr =  rida.getAttributes().getNamedItem("send_address_street").getNodeValue(); 
		 	         		  name = name + " " + sendaadr;
		         		 }catch(Exception ee){

			         	
		         		 }
			         	
		         		String asula = "";
		         		try{ 
		 	         		  asula =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue(); 
		 	         		sendaadr = sendaadr + " " + asula;
		 	         		 customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	 }catch(Exception ee){
		         		 }
		 	         	
                         
		         		
		         		  customer.setName(name);
		         		 String aadr = "";
		         		 String aadr2 = "";
		         		 try{ 
		 	         		  aadr =  rida.getAttributes().getNamedItem("address_street").getNodeValue(); 
		 	         		  aadr2 =  rida.getAttributes().getNamedItem("address_country").getNodeValue(); 
			 	         		
		 	         		  customer.setAddress(Converter.adressLink(aadr, country));
		         		 }catch(Exception ee){

			 		         		 }
		         		 String aadress = aadr + " " + aadr2;

		         		 try{ 
		 	         		 String yldphone =  rida.getAttributes().getNamedItem("telephone").getNodeValue();
		 	         		 if(yldphone.trim().length() > 0){
		 	            		customer.setPersonalData((yldphone).replaceAll(";", ","));
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
		         				if(customer.getPersonalData().trim().length() > 1){
		         					customer.setPersonalData((cname + " " + ctel).replaceAll(";", ","));
			         			}else{
		         				   customer.setPersonalData(customer.getPersonalData() + "|" +(cname + " " + ctel).replaceAll(";", ","));
		         				}
		  	         		
		  	         		 }}
		         		  }catch(Exception r){
		         			  
		         		  }
		         		 
		         		  	        		  
		         		  try{
			         			 String city =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue() ;         	
				         		  customer.setCity(city);
			         		  }catch(Exception e){
			         			 try{
				         			 String city =  rida.getAttributes().getNamedItem("address_county").getNodeValue() ;         	
					         		  customer.setCity(city);
			         			}catch(Exception es){

				         			  customer.setCity(" ");	
			         			}
			         		  }
		         		 
		         		  
		         		  try{
		         			 String price_list =  rida.getAttributes().getNamedItem("price_list").getNodeValue() ;         	
			         		  customer.setGroup(deCode(price_list));
		         	  
		         		  }catch(Exception e){
		         			  
		         		  }
		         		  
		         		  try{
			         			 String price_list_special =  rida.getAttributes().getNamedItem("price_list_special").getNodeValue() ;   

			         			 if(customer.getGroup() != null && customer.getGroup().trim().length() > 0) {
			         				customer.setGroup(customer.getGroup() + "|" + deCode(price_list_special.replaceAll(",", "|"))); 
			         			 }else {
			         				customer.setGroup( deCode(price_list_special.replaceAll(",", "|"))); 
			         			 }
				         		  
			         	  
			         		  }catch(Exception e){
			         			  
			         		  }
     		
	         		 String salesman  = rida.getAttributes().getNamedItem("salesman").getNodeValue(); 
	         		      customer.addAgent(new Agent(deCode(salesman.toUpperCase())));
	         		      customer.setMainAgent(deCode(salesman.toUpperCase()));
	    
	         		      tm.put(customer.getName(), customer);
	                      this.customers.add(customer.getID());
	                     addCusttoLP(customer,kkood,kname,aadress,salesman,salesman, sendaadr);
	         	  }catch(Exception e){
	         		
	         		  e.printStackTrace();
	         	  }
	              
	         	  }}
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
     return tm;
   }
   
   
   private void addCusttoLP(Customer cust,String kkood, String knimi,String aadr, String agname , String ag, String asula){
	   
       String indeks = "";
       String asi = "";
       
       String tinfo = "";
       String aad = aadr;
       if(asula.trim().length() > 0){
    	aad = asula;   
       }
       
       try{
    	   String ins = "insert into [vLPK - Klient]([Andmevahetuse tunnus], [LPK nr], Nr, Nimi " +
	       ", Aadress, Postiindeks, Asula, Telefon, [Muugiisiku tahis], [Muugiisiku nimi], [Muugiisiku telefon] " +
	       ", Tarneinfo, [Kohaletoimetamine alates], [Kohaletoimetamine kuni], [Makseviisi tahis] " + //
	       ", [Aktsepteerimise viga], [Aktsepteerimise veateade]) VALUES (0,'" + lpnum + "','" + Converter.deCode(kkood) + 
	       "','" + Converter.deCode(knimi) + "','" + Converter.deCode(aad) + "','" + Converter.deCode(indeks) + "'," +
	       		" '"+ Converter.deCode(asi)  + "','','" + Converter.deCode(ag) 
		   + "','" + Converter.deCode(agname) + "','','tarneinfo'" +
		   ",'1753-01-01 0:00:00','1753-01-01 0:00:00','',0, '')";
    	   
   //   System.out.println(ins);
	      //" + cust.getPaymTerm()+ "
	      Statement st3 = conlp.createStatement();
	      st3.executeUpdate((ins));
	      st3.close();   
       }catch(Exception t){
    	 //  t.printStackTrace();
       }
       
	      
   }

   private boolean isOkDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(6, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(9, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.MONTH, -6);
		
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
			String httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice_row&get=1&number=" + number;
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = this.enCode(inputLine);
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
			        		// System.out.println("toode " + toode + " " + hind);
			        	}
			        	
		        
		        result.add(new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," "));
		         }
		        	
		        }
		        in.close();
		      //  System.out.println("buffer " + result);
		        
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	   return result;
   }

   Hashtable prodCodes = null;
   
   
   public TreeMap s(Agent agent){

	   
	   TreeMap otherStocks = this.sOther();
	   
     TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     if("true".equals(parent.getProps().getProperty("logistika.pluss"))){
 		 this.getLPStockInfo();
 	}
     prodCodes = new Hashtable();
     Vector noGrups = new Vector();
     Vector codeEnds = new Vector();
     Vector noTexts = new Vector();
     Vector yesClasses = new Vector();
     Vector noBrands = new Vector();
     String groupIndent = null;
         TreeMap brands = new TreeMap();
         boolean sortBR = false;
         boolean useBrandAsType = false;
         if(getParent().getProps().getProperty("usebrand") != null){
        	 useBrandAsType = true;
         }
     
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
     
     Hashtable prices = null;
     
     if(getParent().getProps().getProperty("usepricelist") != null){
     	String pr = getParent().getProps().getProperty("usepricelist");
     //	prices = getPricelists(pr);
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
    
     
     try {
 
    	   FileReader fr = new FileReader(getDir() + "tooted.xml");
           BufferedReader reader = new BufferedReader(fr);
         String line = null;

         //line = reader.readLine();
         
         System.out.println("suurus " +prodsInUse.size());
          while((line = reader.readLine()) != null  ){
        	  
        	  if((line.trim().length()) > 50 ){
        		  
        	  int len = line.indexOf("code=");
        	  String id   = line.substring(len +6);
        	  id = id.substring(0,id.indexOf("\""));
      	     Product prod = new Product(id);
      	   
      	      len = line.indexOf("default_name=");
      	    String namec  = line.substring(len + 14);
      	     	 namec = namec.substring(0,namec.indexOf("\""));
      
      	    
      	 
      			String add = "";
      	    len = line.indexOf("stripe_code=");
      	    if(len != -1){
      	    	 String ean   = line.substring(len + 13);
      	    	 ean = ean.substring(0,ean.indexOf("\""));
          	     prod.setEAN(ean);
          	     
          	   if("true".equals(getParent().getProps().getProperty("add.ean"))){
          			add = ean.substring(ean.length()-3); 
          		 }
      	    }
      	   
      	
      	     
    	    
        	  if("true".equals(getParent().getProps().getProperty("name.code.front"))){
     			 prod.setName(id + " " + Converter.enCodeEst(namec) + " " +  add); 
     		 }else{
     			 prod.setName(Converter.enCodeEst(namec) + " " + id + " " + add); 
     		 }
        	    
      	    
      	     
      	     
      	   len = line.indexOf("web_price1=");
      	 len = line.indexOf("base_price=");
      	   if(len == -1){
      		 len = line.indexOf("base_price=");
      		String price   =  line.substring(len + 12);  
 	    	// System.out.println(" siin " +  prod.getName() + " " + price);
 	    	 try{

 	      	    prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))) ); 
 	    	 }catch (Exception e){
 	    		prod.setPrice(1);
 	    	 }
      	   }else{
      		 String price   =  line.substring(len + 12);  
  	    	// System.out.println(" siin " +  prod.getName() + " " + price);
       	    prod.setPrice(Double.parseDouble(price.substring(0,price.indexOf("\""))));
       	    
      	   }
      	    	
           	 len = line.indexOf("unit=");
           	 if(len != -1){
           	   String unit   =  line.substring(len + 6);  
 	          prod.setUnit(unit.substring(0,unit.indexOf("\"")));
   	   		 
           	 }
      	  boolean goes = true;
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
      	 	     if(!yesClasses.contains(grupp)){
      	 	    	goes = false; 
      	 	     }
      	 	  prod.setGroup(grupp);
      	 	  
      	 	  if( groupIndent !=  null){
            		
           		 len = line.indexOf("<data code=\"" +groupIndent + "\"");
           		 int lll = ("<data code=\"" +groupIndent + "\"").length();
            	    if(len != -1){
            	    	 String  gr   = line.substring(len + lll + 10);
            	    	 
            	    	 gr = gr.substring(0,gr.indexOf("\""));
            	    	 prod.setGroup(gr);
                	   this.prGroups.put(gr, gr);  
                	 }
           		
           	}
      	 	 }
        	
      	 	 String areto = "";
      	 	 
      	 	 len = line.indexOf("data code=\"ALK_REG\""); 
      	 	 if(len != -1){
      	 	      areto =  line.substring(len + 29); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                // System.out.println("areto " + areto );

      	 	 }
      	 	 
      	 	 len = line.indexOf("data code=\"BRAND\""); 
      	 	 if(len != -1){
      	 	      areto =  line.substring(len + 27); 
      	 	      areto = areto.substring(0,areto.indexOf("\""));
                 System.out.println("areto " + prod.getID() + " " + areto );
                if(useBrandAsType) {
                	prod.setType(areto);
                }

      	 	 }

      	 	 double mahto = 0;
      	 	 String maht = "";
      	 	 len = line.indexOf("data code=\"KOGUS\""); 
      	 	 if(len != -1){
      	 	      maht =  line.substring(len + 27); 
      	 	      maht = maht.substring(0,maht.indexOf("\""));
                 //  System.out.println("maht " + maht );
                   try{
                	   mahto = Double.parseDouble(maht.replace(',', '.'));
                   }catch(Exception e){
                	   
                   }
      	 	 }
      	 	 
      	 	 len = line.indexOf("stock_level_total=");
      	 	 if(len != -1){
      	 	     String stock   = line.substring(len + 19);  
     	          prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
     	          
      	 	 }
      	 	 
      	   if("true".equals(parent.getProps().getProperty("logistika.pluss"))){
      		   if(stockslp.get(prod.getID()) != null){
      			   try{
      				 prod.setStock((int)Double.parseDouble(stockslp.get(prod.getID())+""));  
      	      		}catch(Exception t){
      				}
      		   }
      	   }
      	   
      	   if(otherStocks.get(prod.getID()) != null){
      		 try{
  				 prod.setStock((int)Double.parseDouble(otherStocks.get(prod.getID())+""));  
  	      		}catch(Exception t){
  				} 
      	   }
        	  
        	  
      	  if(line.indexOf("ARTIKKEL_KAMP") != -1){
      	    	prod.setStatusDiscount("1");
      	    }
      	    	  
      	    
      	  if(namec.startsWith("*") && prod.getStock() < 1){
    	    	//goes = false;
    	    }
      	    
      	  if(namec.startsWith("**")){
    	    //	goes = false;
    	    }
      	  
      	  
      	if(!prodsInUse.contains(prod.getID())){
      	//  goes = false;
      	}
      	 
      	    if(goes){
      	         prodHash.put(prod.getID(), prod);
      	         tm.put(prod.getName(), prod);	
      	       if("true".equals(parent.getProps().getProperty("logistika.pluss"))){
      	            addLPProduct(prod,areto,mahto); 	
      	            }
      	        }
      	    
              }
          }
    
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   
   private void addLPProduct(Product prod,String areto ,double maht){
	   
	   //lisame toote LP-sse
	   	double nkaal = 0;
		double bkaal = 0;
		//double maht =  0;
		double alusel = 0;
		double protsent  = 0;
		String pandikood = "" ;
		String maa = "";
		String ekn = "";
		//String areto = "";
		String tootjaKood = "";
		String tootjaNimi = "";
		String grupinimi = "";
	 
	   try{
	    String ins = " insert into [vLPK - Kaup]([Andmevahetuse tunnus], [LPK nr] " +
	    "	, Nr, Kirjeldus, Ribakood, [Pohimootuhiku tahis], Netokaal, Brutokaal, Maht, Kastis, Alusel, Aktiivne, [Kanguse protsent] " +
		", [Seotud pandikauba nr], [Paritolumaa tahis], [EKN kood], [Areto nr], [Tootja tahis], [Tootja nimetus] " +
		", [Aktsepteerimise viga], [Aktsepteerimise veateade]" +
		", [KM maar],[Kaubagrupi tahis], [Kaubagrupi nimetus]) VALUES (1, '" + lpnum + "','" + prod.getID()+ "',('" + enCode((prod.getName().trim())) + "'),'" + prod.getEAN() + "','" + prod.getUnit()+ "',"
		 + nkaal + "," + bkaal + "," + maht + "," + prod.getInCases() + "," + alusel + "," + 1  + "," + protsent + ",'" + pandikood + "','" +
		 maa + "','" + ekn + "','" + areto + "','" + tootjaKood + "',('" + enCode((tootjaNimi)) + "'), 0, '',20,('"
		 + prod.getGroup() + "'),('"+ enCode(grupinimi.trim())	+"'))";
	     
	      
	     // System.out.println(ins);
	   	Statement st3 = conlp.createStatement();
	    st3.executeUpdate(ins);
	   // st3.close();
	   }catch (Exception e){
	 //  	e.printStackTrace();
	   	
	    String ins = " update [vLPK - Kaup]set [Kaubagrupi tahis] = dbo.Rus2Est('" + prod.getGroup().trim() + "')" +
	    		", [Kaubagrupi nimetus] = ('" + enCode(grupinimi.trim()) + "'),[Areto nr] = '" + areto+ "' where  [LPK nr] = '" + lpnum + "' and Nr= '" + prod.getID() + "' " ;
	   
	      
	     // System.out.println(ins);
	try{
		Statement st3 = conlp.createStatement();
	    st3.executeUpdate(ins);
	    st3.close();
	   
	}catch(Exception r){
		r.printStackTrace();
	}
	    
	   }
	      
	        
	      }
	  

	   
   
   
   

   public TreeMap sOther(){
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
		      	     prod.setName( prod.getID() + " " + deCode(name.substring(0,name.indexOf("\""))));
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
   
   public TreeMap s2(){

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
		      	     prod.setName( prod.getID() + " " + deCode(name.substring(0,name.indexOf("\""))));
		      	     tm.put(prod.getID(), prod); 
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
	   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		
		   try{
			 	String httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice&get=1&customer=" + custid;
		      // System.out.println(httpsURL);
			 	Converter.doTrustToCertificates();
			 	URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = this.enCode(inputLine);
		        //	System.out.println(inputLine);
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
						     				//	System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	
								        			  
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
	   
	   return debts;
   }

   
   public Vector getCustomerProductsList(String agentCode){
	   //1.teeme klientide arvete nimekirja .
	   Vector custProds = new Vector();
	   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		   try{
			  // String httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?get=1&what=itemclass";
			   Converter.doTrustToCertificates();
				String httpsURL = "https://directo.gate.ee/xmlcore/" + company + "/xmlcore.asp?what=invoice&get=1&customer=" + custid;
		     //  System.out.println(httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = this.enCode(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
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
			        		
			        		 if(isOkDate(date)){
			        			// System.out.println(date + " " + number + " " + custid);	
			        		
			     					Vector cps = this.getCP(deCode(custid),number,date);
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
		        
		       
		   } catch (Exception a){
			   a.printStackTrace();
		   }
		   
	   }
	   return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Directo";
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
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
/*
  	 try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;

    //   "klient","toode","kogus","viimane kuup„ev","viimane kogus"

    while(!(line = reader.readLine()).equals("[Get Salary]")){
    }
    line=reader.readLine();
    while((line = reader.readLine())!= null &&  line.trim().length() > 0 ){
       line = line.replaceAll("\",\"","|");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
          double sales = Double.parseDouble(clear(st.nextToken()));
                if (st.hasMoreTokens()){
                 String agentID = clear(st.nextToken());
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
            }
          }
        }
    }
      } catch (IOException e){
        e.printStackTrace();
      }*/
  }

@Override
public String getSaveStockInfo() {

	if("true".equals(parent.getProps().getProperty("logistika.pluss"))){
		return this.getLPStockInfo();
	}
	return null;
}

@Override
public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public TreeMap getProducts(Agent agent) {
	// TODO Auto-generated method stub
	return null;
}
}
