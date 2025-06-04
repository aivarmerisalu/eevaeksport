package eevaeksport.driver;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DashboardData;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.htmlgen.HTMLeditor;
import eevaeksport.mailagent.FileSender;
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

public class AxaJNGLATDBDriver extends AbstractDBDriver  implements Manager, Promo, BulkUpdater, Discounter, PictureMaker, FixReporter ,DashboardCreator{
  public AxaJNGLATDBDriver(EksportPanel parent){
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
    instance = getParent().getProps().getProperty("company");
  if(instance == null) {
	  instance = "";
     }
  }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String instance = null;
  
  private HashMap getMustSales() {
	  HashMap res = new HashMap();
	  StringBuffer sb = new StringBuffer();
		
	  try {
			    TreeMap custTypes =  new TreeMap(); // Converter.getCustDimsLoading("type");
			    
			    String sq3 = "select kood, customgroup2 from getcustomers where customgroup2 != ''" ;
				
				Connection con3 = getNewConnection();
				Statement st3 = con3.createStatement();
				ResultSet rs3 = st3.executeQuery(sq3);
				while (rs3.next()) {
					String kood = rs3.getString("kood").trim();
					String grupp = rs3.getString("customgroup2").trim();
					
					custTypes.put(kood, grupp);
				}
				rs3.close();
			    st3.close();
			    String date = Converter.getDDpMMpYYYYpString(Calendar.getInstance());
			    sb.append(date+"\n");
			
			//1. peame tegema kindlaks, mis grupid on ja mis tooted sinna kuuluvad
			
			ArrayList<String> mustGroups = new ArrayList<String>(); 
			HashMap mustProds = new HashMap();
			HashMap<String, String> groupTypes = new HashMap<String, String>();
			
    		 sq3 = "select * from getproducts where customgroup1 != '' and nimi1 != '' " ;
					
					
				 st3 = con3.createStatement();
				 rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						String kood = rs3.getString("toode").trim();
						String nimi = rs3.getString("nimi1").trim();
						String prGrupp = rs3.getString("grupp");
						String grupp = rs3.getString("customgroup1").trim();
						
						if( !mustGroups.contains(grupp) ) {
						 mustGroups.add(grupp);
						 String type = "NONFOOD";
						 if(prGrupp != null &&
								 (prGrupp.trim().startsWith("151") ||
										 prGrupp.trim().startsWith("25") ||
										 prGrupp.trim().startsWith("31") ||
										 prGrupp.trim().startsWith("29") ||
										 prGrupp.trim().startsWith("42") ||
										 prGrupp.trim().startsWith("22") ||
										 
										 
										 
										        prGrupp.trim().equals("201") ||
										        prGrupp.trim().equals("5") ||
												 prGrupp.trim().equals("16") ||
												 prGrupp.trim().equals("34") ||
												 prGrupp.trim().equals("35") ||
												 prGrupp.trim().equals("51")
												 
										 )) {
							 type = "FOOD";
						 }
						 groupTypes.put(grupp, type);
						 
						}
						//Product tt = new Product(kood);
						//tt.setName(nimi);
						mustProds.put(kood, grupp);
						
					}
					rs3.close();
					st3.close();
					
					
					//2.Nüüd võtame müügid klientide lõikes ja paneme kliendi lõiles külge mis müüdi ja mis ei müüdud  
					
					 TreeMap custProds = new TreeMap();
					 
					    String cmd = "select KLIENT ,TOODE  from getcustomerproductlist where KUUPAEV > getdate()-90 and len(KLIENT) > 0 " + 
					    		" GROUP BY klient, toode ORDER BY klient,toode" ;



					    Statement stmt =  con3.createStatement();
					      ResultSet rs = stmt.executeQuery(cmd);
					  
					      while(rs.next()){
					        String kliendikood = rs.getString("klient").trim();
					        String tootekood = rs.getString("toode").trim();
					        
					        			ArrayList arr = new ArrayList();
					        			if(custProds.get(kliendikood) != null) {
					        				arr =  (ArrayList)custProds.get(kliendikood);
					        			}
					        			if(!arr.contains(tootekood)) {
					        				arr.add(tootekood);
					        				custProds.put(kliendikood, arr);
					        			}
					           }
					      
					      rs.close();
					      stmt.close();
					 
					    
					   Iterator itera =    custProds.keySet().iterator();
					   
					   
					   
					   while(itera.hasNext()) {
						   
						   ArrayList<String>  soldGR = new ArrayList<String>();
						   ArrayList<String>  nonSoldGR = (ArrayList<String>) mustGroups.clone();
						   String klkood = (String)itera.next();
						   String klgr = "NONE";
						   if(custTypes != null && custTypes.get(klkood) != null) {
							   
					        	String type = (String)custTypes.get(klkood);
					        	if(type != null) {
					        	 klgr = type; 
					        	
					        	 if(klgr.equals("A")){
					        		 klgr = "FOOD";
					        	 } else if (klgr.equals("B")){
					        		 klgr = "NONFOOD";
					        	 }else if (klgr.equals("C")){
					        		 klgr = "ALL";
					        	 }
					        	}
						   
						   
						   
						   ArrayList<String> sold = (ArrayList<String>)custProds.get(klkood);
						   
						   for(String toode : sold) {
							   if(mustProds.get(toode) != null) {
								   String grsold = (String)mustProds.get(toode);
								   if(  !soldGR.contains(grsold)) {
								   soldGR.add(grsold); 
								   nonSoldGR.removeIf(n -> n.equals(grsold));
								   }
							   }
						   }
						   ArrayList foodgrs = new ArrayList();
						   ArrayList nonfoodgrs = new ArrayList();
							  // siin tuleb müümata gruppe rehitseda vastavalt kliendi tyybile	   
						   if(klgr.equals("FOOD")) {
							 Iterator ite =	 groupTypes.keySet().iterator();
							 while(ite.hasNext()) {
								 String grr = (String)ite.next();
								 if(grr.equals("NONFOOD")) {
									 String typ = (String)groupTypes.get(grr);
									 nonSoldGR.remove(typ);
									 soldGR.remove(typ);
									 
								 }else {
									 String typ = (String)groupTypes.get(grr);
									 foodgrs.add(typ);
								 }
							 }
						   }
						   
						   
						   if(klgr.equals("NONFOOD")) {
							   Iterator ite =	 groupTypes.keySet().iterator();
								 while(ite.hasNext()) {
									 String grr = (String)ite.next();
									 if(grr.equals("FOOD")) {
										 String typ = (String)groupTypes.get(grr);
										 nonSoldGR.remove(typ);
										 soldGR.remove(typ);
										
									 }else {
										 String typ = (String)groupTypes.get(grr);
										 nonfoodgrs.add(typ);
									 }
								 }
						   }
						   
						   if(!klgr.equals("NONE")) {
							  double perc = 1;
							  
							  if(nonSoldGR.size() > 0) {
								  
								  double non = mustGroups.size() * 1.0;
								  if(klgr.equals("FOOD")) {
									  non = foodgrs.size();
								  }
								  if(klgr.equals("NONFOOD")) {
									  non = nonfoodgrs.size();
								  }
								  
								  perc = soldGR.size() *1.0 / non;
							  }
							
							   res.put(klkood , perc);//
							   res.put(klkood + "sold", soldGR);
							   res.put(klkood + "nonsold", nonSoldGR);
							   if(soldGR.size() > 0) {
								//   System.out.println("Must% " + klkood + " " + soldGR.size() + " " + nonSoldGR.size() + " " + perc); 
								   sb.append(klkood + "," + (perc*100) + ","+ soldGR.size() + "," + nonSoldGR.size() + "\n");
							   }
							  }
							    
				  	  }
						   
					
					
					   }
					
					st3.close();
					con3.close();
					  res.put("groups", mustGroups);
					  res.put("prods", mustProds);
					  
					  Converter.saveFile("reports/mustcustomer.csv", sb.toString());

			  
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		
		
		  return res;
  }
  
  public TreeMap getDealers() {
		return null;
	}
 
  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		Vector result = new Vector();
      
		if(instance.equals("shell")) {
    			try {
    	    		String sq3 = "select * from getlastteninvoices" ;
    						
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
    			}

    			return result;
    	

	}

  public String getSaveDebtInfo() {
   	return null;
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
	           //  String[] ff2 = res2.list();
	         	/* for(int i = 0;ff2.length>i;i++){
	             	(new File ( pictspda + ff2[i])).delete(); 
	             }*/
	        
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	    	   	String[] ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(toode+".")){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
	              	if(!inpda.exists()){
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
          	  
          	  	          } 
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

  
  
	public Vector getFixedAssortmentData() {
		
		 Vector lines = new Vector();
		try {
			
			
			String cmd = "select * from getfixedassortmentdata";
			//Vaata kettide koodid 
			//'511','109' ,'791'
			//Max,rim.mego
			System.out.println(cmd);

			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				
				String clGroup = " ";
				String prodID = " ";
				MatrixLine mline = new MatrixLine();

				clGroup = rs.getString("grupp");
				prodID = rs.getString("toode");

				if (clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				
				lines.add(mline);

			}

			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		System.out.println("Mlines fixed " + lines.size());
		
   return lines;
}

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          HashSet hh = new HashSet();
               
          // leiame need mis on kahekordsed ja siis välistame teised läbi HH et ei tule topelt ridu kui overlapib
            try {
            	 String sql1 = " select  itemrelation , accountrelation , min(fromdate) as fdate, max(todate) as tdate , max(percent1) as perc,max(amount) as amount," + 
            	 		" max(relation) as relation,max(description) as descr, datediff(day,min(fromdate),max(todate)) as paev ,datediff(day, getdate() ,max(todate) ) as tillend , \r\n" + 
            	 		"                    		   datediff(day, min(fromdate),  getdate()) as fromst\r\n" + 
            	 	
            	 		" from getprices where ((getdate() between fromdate-6 and todate) \r\n" + 
            	 		"                    		 or ( getdate() > fromdate-6 ) )and todate > '1900-01-01 00:00:00.000' group by accountrelation ,itemrelation\r\n" + 
            	 		"							  having count (itemrelation) > 1  order by accountrelation,itemrelation";
                  
          	   Connection con = getNewConnection();
              	Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(sql1);

                    while (rs.next()) {
                    	
                    	boolean goes = false;
                  	        String prodID = rs.getString("itemrelation");
                  	        String custGR = rs.getString("accountrelation");
                            String algus = rs.getString("fdate");
                            String lopp = rs.getString("tdate");
                            double perc = rs.getDouble("perc");
                            double price = rs.getDouble("amount");
                        
                            int relation = rs.getInt("relation");
                            String camp = rs.getString("descr");
                            int paev = rs.getInt("paev");
                            int tillend = rs.getInt("tillend");
                            int fromst =  rs.getInt("fromst");
                            MatrixLine mline = new MatrixLine();
                            
                            String check = "";
                            
                            
                            if (custGR != null && custGR.trim().length() > 1) {
                                    mline.setCliGroup(custGR.trim());
                                    check = check + custGR.trim();
                            }
                         
                            if (prodID != null && prodID.trim().length() > 1) {
                                    mline.setProdID(prodID.trim());
                                    check = check + prodID.trim();
                            }
                            if (perc > 0 ) {
                                mline.setDiscount(perc);
                                goes = true;
                            }

                            if (price  > 0) {
                                    mline.setPrice(price);
                                    goes = true;
                            }
                            
                            boolean uu = false;
                            if((paev > 1 && paev < 90) || relation == 8) {
                          	    
                                   if (lopp != null && lopp.trim().length() > 0  && !lopp.trim().contains("1900")) {
                                       mline.setEndDate(lopp.trim().substring(0,10));
                                       uu = true;
                                       // panin sisse et poleks algust kui pole lõppu .
                                       if (algus != null && algus.trim().length() > 0 && !algus.trim().contains("1900")) {
                                           mline.setStartDate(algus.trim().substring(0,10));
                                       }
                                   } 
                            }
                            
                            if(camp != null && camp.trim().length() > 0 && camp.trim().startsWith("MAGAZ")) {
                           	 mline.setLineinfo(camp);
                           	 if(uu) {
                           	  if(tillend < 6 && tillend > -100) {
                           		 mline.setLineinfo(camp+"|"+tillend + "end");
                                }
                           	  if(fromst < 0) {
                          	 	 mline.setLineinfo(camp+"|"+fromst + "start");
                                }
                           	 }
                            } else {
                            	if(uu) {
                                if(tillend < 6 && tillend > -100) {
                              	  mline.setLineinfo(tillend + "end");  
                                }
                                if(fromst < 0) {
                              	  mline.setLineinfo(fromst + "start");  
                                } 
                            	}
                            }
                       
                            if(!hh.contains(check) && goes){
                          	  lines.add(mline);  
                            }
                          
                            hh.add(check);
                    }
                    rs.close();
                    stmt.close();
                    con.close();
            } catch (SQLException ex) {
            ex.printStackTrace();
            }          
          
          
          
          
          // siin leiame kõik mis ei ole topelt 
         String sql = " select  * , datediff(day,fromdate,todate) as paev ,datediff(day, getdate() ,todate ) as tillend , " + 
         		"   datediff(day, fromdate,  getdate()) as fromst from getprices where (getdate() between fromdate-6 and todate) " + 
         		" or ( getdate() > fromdate-6 ) order by accountrelation,relation desc,itemrelation ";
          try {

        	   Connection con = getNewConnection();
            	Statement stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String prodID = rs.getString("itemrelation");
                	  String custID = rs.getString("accountcode");
                          String custGR = rs.getString("accountrelation");
                         
                      
                          String algus = rs.getString("fromdate");
                          String lopp = rs.getString("todate");
                          double perc = rs.getDouble("percent1");
                          double price = rs.getDouble("amount");
                          int relation = rs.getInt("relation");
                          String camp = rs.getString("description");
                          int paev = rs.getInt("paev");
                          int tillend = rs.getInt("tillend");
                          int fromst =  rs.getInt("fromst");
                          MatrixLine mline = new MatrixLine();
                          
                          String check = "";
                          if (custID != null && custID.trim().length() > 1) {
                              mline.setCustID(custID.trim());
                              check = check + custID.trim();
                          }
                          
                          if (custGR != null && custGR.trim().length() > 1) {
                                  mline.setCliGroup(custGR.trim());
                                  check = check + custGR.trim();
                          }
                       
                          if (prodID != null && prodID.trim().length() > 1) {
                                  mline.setProdID(prodID.trim());
                                  check = check + prodID.trim();
                          }
                          if (perc != 0) {
                              mline.setDiscount(perc);
                          }

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          if((paev > 1 && paev < 90) || relation == 8) {
                        	    
                                 if (lopp != null && lopp.trim().length() > 0  && !lopp.trim().contains("1900")) {
                                     mline.setEndDate(lopp.trim().substring(0,10));
                                     // panin sisse et poleks algust kui pole lõppu .
                                     if (algus != null && algus.trim().length() > 0 && !algus.trim().contains("1900")) {
                                         mline.setStartDate(algus.trim().substring(0,10));
                                     }
                                 } 
                          }
                          
                          if(camp != null && camp.trim().length() > 0 && camp.trim().startsWith("MAGAZ")) {
                         	 mline.setLineinfo(camp);
                         	  if(tillend < 6 && tillend > -100) {
                         		 mline.setLineinfo(camp+"|"+tillend + "end");
                              }
                         	  if(fromst < 0) {
                        	 	 mline.setLineinfo(camp+"|"+fromst + "start");
                              }
                          } else {
                              if(tillend < 6  && tillend > -100) {
                            	  mline.setLineinfo(tillend + "end");  
                              }
                              if(fromst < 0) {
                            	  mline.setLineinfo(fromst + "start");  
                              }  
                          }
                     
                          if(!hh.contains(check)){
                        	  lines.add(mline);  
                          }
                        
                          hh.add(check);
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
  
  private HashMap kogused = null;
  private HashMap kogusedStr = null;

  public String getSaveStockInfo(){
	  
	  kogused = new HashMap();
	  kogusedStr = new HashMap();

	  StringBuffer products = new StringBuffer();

	//  if(instance.equals("shell")) {
	  	
	  	  String  cmd = " select l.*,OTC from GETSTOCK l inner join getproducts t on t.toode=l.kood ";
	    

	         if (isDebug()) System.out.println(cmd);
	         try {

	      	 Connection con = getNewConnection();
	      	 Statement st = con.createStatement();
	           ResultSet rs = st.executeQuery(cmd);

	           while (rs.next()) {

	             String kood = rs.getString("kood").trim();
	             double seis = rs.getDouble("stock");
	             String otc = rs.getString("OTC");
	        
	             
	             if(seis > 0){
	          	   
	          	   
	          	   if(instance.equals("shell")) {
	          		   if(otc != null && otc.trim().equals("SHELL")) {
	    	        		  products.append(kood +  ";");
	    	                   products.append(" ;");
	    	                   products.append(getParent().getNF().format(seis) +  "\n"); 
	    	        	  }
	    	        	
	    	        }else {
	    	        	 if(otc == null || !otc.trim().equals("SHELL")) {
	    	        	  products.append(kood +  ";");
	                    products.append(" ;");
	                    products.append(getParent().getNF().format(seis) +  "\n"); 
	    	        	 }
	    	        	
	    	        }
	          	        if(kogused.get(kood) != null){
	                  	   double vahe = Double.parseDouble((String) kogused.get(kood));
	                  	   kogused.put(kood, (vahe+ seis) + "");
	                     }else{
	                  	   kogused.put(kood, seis + ""); 
	                     }
	                    
	             }
	             }
	             rs.close();
	             st.close();
	             con.close();
	             
	         } catch (Exception e) {
	             getParent().showError(e);
	         }
	      
	   /* } else {
	  	
	     	try{
	  		String file2 = "stockref.txt";
	  		File ff = new File(getExcelDir() +  file2);
	  		if(ff.exists()){		
	  			FileReader fr = new FileReader(getExcelDir()  + file2);
	  		 BufferedReader reader = new BufferedReader(fr);
	  	 String line = null;
	  		while ((line = reader.readLine()) != null) {
	  			StringTokenizer st = new StringTokenizer(line, ";");
	  			if (st.hasMoreTokens()) {
	  				String kood = st.nextToken().trim();
	  					
	  						while (st.hasMoreTokens()) {
	  							Double seis = Double.parseDouble(st.nextToken().trim());
	  							if(0 < seis) {
	  							  products.append(kood +  ";");
	  			                  products.append(" ;");
	  			                  products.append(getParent().getNF().format(seis) +  "\n"); 
	  						
	  						if(kogused.get(kood) != null){
	  		                	   double vahe = Double.parseDouble((String) kogused.get(kood));
	  		                	   kogused.put(kood, (vahe+ seis) + "");
	  		                   }else{
	  		                	   kogused.put(kood, seis + ""); 
	  		                   }
	  						}}
	  				}
	  			}
	  		}
	  		
	  	
	  	}catch(Exception e){
	  	e.printStackTrace();	
	  	}
	  	
	  }*/

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
    String cmd = "select * from getagent";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
    
    try {
    
        Connection  con = getNewConnection();
        Statement   stmt = con.createStatement();
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("nimi").trim() ;
                   agent.setName(name);
          agent.setIsPilot(true);
          
          if("true".equals(  getParent().getProps().getProperty("usewebpass"))) {
          	agent.needsZipPass = true;
          }
          agents1.put(agent.getID(), agent);
          agents.put(name, agent);
          ags.add(agent.getID());
        }
        rs.close();
        stmt.close();
        con.close();
    } catch (Exception e){
        getParent().showError(e);
      }
   Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    
    
    return agents;
  }
  
  HashSet ags = new HashSet();
  
  private   HashMap logMap = new HashMap();
  
  
  
  private TreeMap<String,String> getVeod(){
	  	TreeMap<String,String> res = new TreeMap<String,String>();
	  	
	  	File f = new File(this.getExcelDir() + "piegades dienas.xlsx");
			try {
				
				 DataFormatter formatter = new DataFormatter();
				
				
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheetAt(0);
				XSSFRow row = null;
				
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					
					String custID = formatter.formatCellValue(row.getCell((short) 1));
					String vedu = "";


	                       if(custID != null && custID.trim().length() > 0){
	                    	   
	                           for (int a = 7; a < 15; a++) {
	                        	   
	                        	   String asi = formatter.formatCellValue(row.getCell((short) a));
	                        	   
                                     if(a == 7 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "1";
                                     }
                                     if(a == 8 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "2";
                                     }
                                     
                                     if(a == 9 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "3";
                                     }
                                     
                                     if(a == 10 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "4";
                                     }
                                     
                                     if(a == 11 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "5";
                                     }
                                     
                                     if(a == 12 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "6";
                                     }
                                     
                                     if(a == 13 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "7";
                                     }
                                     
                                     if(vedu.length() > 0) {
                                    	  if(a == 14 && asi != null && asi.trim().startsWith("24") ) {
                                         	 vedu = vedu + " -24h"  ;
                                          }
                                          if(a == 14 && asi != null && asi.trim().startsWith("48") ) {
                                         	 vedu = vedu + " -48h"  ;
                                          }
                                     }
                                   
	                           	   
							    }    
	                          }
	                       
	                       if(vedu.trim().length() > 0) {
	                    	res.put(custID, vedu)   ;
	                    	res.put("0" + custID, vedu)   ;
	                       }
	                    }
				
				
			}catch(Exception r){
				r.printStackTrace();	
				}
				System.out.println("Vedu size  " + res.size());
	 // 	System.exit(0);
	  	return res;
	  }
  
  
  private HashMap<String,Vector<String>> getMerc(){
	  HashMap<String,Vector<String>> res = new HashMap<String,Vector<String>>();
	  	
	  	File f = new File(this.getExcelDir() + "ClientAgent.xlsx");
			try {
				
				 DataFormatter formatter = new DataFormatter();
				
				
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheetAt(0);
				XSSFRow row = null;
				
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					try {
					String custID = formatter.formatCellValue(row.getCell((short) 0));
					String ag = formatter.formatCellValue(row.getCell((short) 1));
	                       
	                       if(ag.trim().length() > 0) {
	                    	   if(ag.startsWith("ag")) {
	                    		   ag = ag.substring(2);
	                    	   }
	                    	   Vector<String> vec  = new Vector<String>();
	                           if(res.get(custID) != null) {
	                           	vec = res.get(custID);
	                           }
	                           if(!vec.contains(ag)) {
	                           	  vec.add(ag);
	                           }
	                           res.put(custID, vec);
	                       }
					}catch(Exception e) {
						
					}
	                    }
				
			    } catch(Exception r){
				  r.printStackTrace();	
				}
				System.out.println("Merc size  " + res.size());
	 // 	System.exit(0);
	  	return res;
	  }
  
  
  public TreeMap getCustomerTopProds(TreeMap custTypes, TreeMap mustCon){
	    TreeMap custProds = new TreeMap();
	 
	
	    
	    String cmd = "select KLIENT ,TOODE  from getcustomerproductlist where KUUPAEV > getdate()-90 and len(KLIENT) > 0 " + 
	    		" GROUP BY klient, toode ORDER BY klient,toode" ;

	    
	    

	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	  
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        if(custTypes != null && custTypes.get(kliendikood) != null) {
	        	ArrayList type = (ArrayList)custTypes.get(kliendikood);
	        	if(mustCon.get(type.get(0)) != null) {
	        		ArrayList lines = (ArrayList)mustCon.get(type.get(0));
	        		if(lines != null && lines.contains(tootekood)) {
	        			ArrayList arr = new ArrayList();
	        			if(custProds.get(kliendikood) != null) {
	        				arr =  (ArrayList)custProds.get(kliendikood);
	        			}
	        			if(!arr.contains(tootekood)) {
	        				arr.add(tootekood);
	        				custProds.put(kliendikood, arr);
	        			}
	        		
	        		}
	        	}
	        }
	      }
	      
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
          e.printStackTrace();
	    }
	    
	 //   System.out.println(custProds.toString());
	    
	    
	    TreeMap res = new TreeMap();
	    
	  Iterator itera =   custProds.keySet().iterator();
	  while(itera.hasNext()) {
		  String kkood = (String)itera.next();
		  double cpsize =  ((ArrayList)custProds.get(kkood)).size();
		  
		  ArrayList type = (ArrayList)custTypes.get(kkood);
		  if(type != null && mustCon.get(type.get(0)) != null ) {
			  double arrs = ((ArrayList) mustCon.get(type.get(0))).size();
			  res.put(kkood,cpsize/arrs);
			  
			 // System.out.println(kkood + " " + cpsize + " " + arrs);
		  }
		  
		 
	  }
	    
	//   System.out.println(res.toString());
	    return res;
	  }

  
  private TreeMap<String,ArrayList<Double>> agentMust = null;
  
  
  public TreeMap getCustomers(Agent agent){
	  
	  HashMap custTop = new HashMap();
	  
	
	  
	  agentMust = new TreeMap<String,ArrayList<Double>>();
	  
	  if(!instance.equals("shell")) {
		  // teeme Must toodete mapi ja protsendid 
		  // TreeMap mustCon = new TreeMap();
		//7	Converter.getContractsSuggByCustOrType(mustCon,rcompany,user,pass,true) ;	 
		   // TreeMap custTypes =  Converter.getCustDimsLoading("type");
		  
		    custTop =  this.getMustSales();
     }
	  
	 // System.out.println("custTop " + custTop.size() + " " + custTop.toString());
	  

	  double minordsum = 0;
      if(getParent().getProps().getProperty("minordsum") != null){
      	try{
      		minordsum = new Double(getParent().getProps().getProperty("minordsum")).doubleValue();
      	}catch (Exception e){
      		e.printStackTrace();
      	}
      }
	  
	  try{
		   Converter.getManagersReplacements(agents1, rcompany, user, pass, false); 
	   } catch(Exception e) {

	   }
	  
	  
	  HashMap salesres = new HashMap();
	  
	  
	   try {
		   
		   String salessql = "  select  klient , trim(str(year(kuupaev))) + case when len(trim(str(month(kuupaev)))) = 1 then '0' +trim(str(month(kuupaev))) else  trim(str(month(kuupaev))) end as kuu , sum(kogus*lprice) as summa from getcustomerproductlist where\r\n" + 
		   		"		   kuupaev > getdate()-200  and len(trim(klient)) >1 group by klient ,  trim(str(year(kuupaev))) + case when len(trim(str(month(kuupaev)))) = 1 then '0' +trim(str(month(kuupaev))) else  trim(str(month(kuupaev))) end \r\n" + 
		   		"		    order by klient ,  trim(str(year(kuupaev)))  + case when len(trim(str(month(kuupaev)))) = 1 then '0' +trim(str(month(kuupaev))) else  trim(str(month(kuupaev))) end";
		   
		     Connection con = getNewConnection();
		     	Statement stmt =  con.createStatement();
		          ResultSet rs1 = stmt.executeQuery(salessql);
		          
		          while(rs1.next()){
		          	String klient = rs1.getString("klient").trim();
		          	String kuu = rs1.getString("kuu");
		          	int summa = (int)rs1.getDouble("summa");
		            if(salesres.get(klient) == null){
		          	  salesres.put(klient,"   " + kuu + " : " + summa + " €" );
		            }else{
		          	  String asi = (String)salesres.get(klient);     	  
		          	  salesres.put(klient,"   " + kuu + " : " + summa +"€|" + asi);
		            }
		          }
		         rs1.close();
		         stmt.close();
		         con.close();
		     // 	System.out.println(salesres.toString());   
	   }catch(Exception e) {
		   
	   }

	  
	  
      String cmd = "select * from getCustomers";
      String cmd2 = "select * from getCustomers";
      
         TreeMap<String,String> veod = new TreeMap<String,String>();
          if(!instance.equals("shell")) {
        	 veod =  getVeod();
           }
                
        //System.out.println("kliendid alustan, veod " + veod.size());
        
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();
        HashMap<String,Vector<String>> custAgents = new HashMap<String,Vector<String>>();
        HashMap<String,Vector<String>> custMercs = null;
        if(!instance.equals("shell")) {
        	custMercs =  getMerc();
        }
        
        try {
        	Connection  con = getNewConnection();
            Statement   stmt = con.createStatement();        
            ResultSet rs = stmt.executeQuery(cmd2);
            
            while (rs.next()) {
                String klient = rs.getString("kood").trim();
                String ag = rs.getString("agent");
                if(ag != null) {
                	ag = ag.trim();
                	  Vector<String> vec  = new Vector<String>();
                      if(custAgents.get(klient) != null) {
                      	vec = custAgents.get(klient);
                      }
                      if(!vec.contains(ag)) {
                      	  vec.add(ag);
                      }
                      custAgents.put(klient, vec);
                }
              
            }
            rs.close();
            
            rs = stmt.executeQuery(cmd);
            while (rs.next()) {
                String nimi = rs.getString("nimi").trim();
              Customer cust = new Customer(rs.getString("kood").trim());
          
              cust.restName = nimi;

              if(!nimi.endsWith("#")){
                   // System.out.println("nimi1 " + nimi);
                	nimi = nimi.replaceAll("Z/S","");
                	nimi = nimi.replaceAll(" SIA","");
                	nimi = nimi.replaceAll("AIZKRAUKLES","AIZ.");
                	nimi = nimi.replaceAll("NAFTA ","NA. ");
                	nimi = nimi.replaceAll("SAUL�TE","");
                	nimi = nimi.replaceAll("''","\"");
                	nimi = nimi.replaceAll("A/S","");
                	nimi = nimi.replaceAll("VIDZEMES ","VID ");
                	nimi = nimi.replaceAll(" DUS","");
                	nimi = nimi.replaceAll("VESEKS Sabiedr�ba ar Ierobe�otu Atbild�bu","VESEKS SIA");
                	nimi = nimi.replaceAll("TIRDZNIEC�BAS","TIRD.");
                	nimi = nimi.replaceAll("SENTOR FARM APTIEKAS","SENTOR APT.");
                	nimi = nimi.replaceAll("SAULES APTIEKU P�RVALDE","SAULES APT.");
                	nimi = nimi.replaceAll("KOMERCIJA","KOM");
                 	nimi = nimi.replaceAll("LATVIA ","LAT ");
                 	nimi = nimi.replaceAll("R�GAS","R�.");
                 	nimi = nimi.replaceAll("R�GA","R�");
                 	nimi = nimi.replaceAll("R�ZEKNES","R�Z");
                 	nimi = nimi.replaceAll("PRESES APVIEN�BA","PRESES APVI.");
                 	nimi = nimi.replaceAll("CENTRS ","CEN ");
                 	nimi = nimi.replaceAll("PARTNERI","PAR");
                 	nimi = nimi.replaceAll(" BALTIJA","");
                 	nimi = nimi.replaceAll("SERVISS","SER");
                 	nimi = nimi.replaceAll("APTIEKA","APT");
                 	nimi = nimi.replaceAll("P�RTIKA","P�R");
                 	nimi = nimi.replaceAll("LATVIJAS ","LAT ");
                 	nimi = nimi.replaceAll("JELGAVAS","JEL.");
                 	nimi = nimi.replaceAll("PHARMA","PH");
                 	nimi = nimi.replaceAll("BALTIC RESTAURANTS LATVIA","BalResLat");

                	nimi = nimi.replaceAll("DOUBLE COFFEE","DouCof");
                	nimi = nimi.replaceAll("AIZKRAUKLES SABIEDR�BA","AIZKRAUKLES SAB.");

            
                String aadress = rs.getString("address");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim()+ " " + cust.getID();
                }
               // System.out.println("nimi2 " + nimi);
                cust.setName(Converter.enCodeEst(nimi));

                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  // cust.setVisit(visit.trim().toUpperCase());
                }
                
                if(minordsum != 0 &&  !instance.equals("shell") ){
                	cust.setMinsum(minordsum);
                }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");
              
                if (agentc != null && agents1.get(agentc.trim()) != null){
    				Agent aa = (Agent)agents1.get(agentc.trim());
    			
    				Vector vec = aa.getDubbler();
    				if(vec!= null && vec.size() >0){
    					Enumeration en = vec.elements();
    					while(en.hasMoreElements()){
    						String ag = (String) en.nextElement();
    						cust.addAgent(new Agent(ag));
    					}
    				}
    			}
                
               
                
            
                String aginfo = rs.getString("aginfo");
                if(aginfo != null && aginfo.trim().length()>0){
                	
                	StringTokenizer st = new StringTokenizer(aginfo.trim(),";");
                	while(st.hasMoreTokens()){
                		String deal = st.nextToken();
                		StringTokenizer de = new StringTokenizer(deal.trim(),"|");
                		while (de.hasMoreTokens()){
               
                			try{
                				String paev = de.nextToken();
                    			String nadal = de.nextToken();
                    			String age = de.nextToken();
                    			paev = paev.replaceAll("1", "E").replaceAll("2", "T").replaceAll("3", "K").replaceAll("4", "N").replaceAll("5", "R").replaceAll("6", "L").replaceAll("7", "P");
                    			if(nadal.equals("2")){
                    				nadal = "1";
                    			}else if (nadal.equals("1")){
                    				nadal = "2";
                    			}else if (nadal.equals("0")){
                    				nadal = "";
                    			}
                    			paev = paev + nadal;
                    			
                    			Agent aaa = new Agent(age);
                    			if(!paev.equals("0")) {
                    				//aaa.setVisit(paev);
                    			}
                    			
                    			if(!age.contains("|") )
                    			cust.addAgent(aaa);
                    				
                			}catch (Exception ee){
                				//ee.printStackTrace();
                			//	System.out.println("Probleemne klient " + cust.getID() );		
                			}
                
                			
                		}
                	}
                	
                    if(aginfo.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE1")); 
                    } 
                }
                
              
                if(custAgents.get(cust.getID()) != null) {
                	Vector<String> ags = custAgents.get(cust.getID());
                	for (String ag : ags) {
                		cust.addAgent(new Agent(ag));
                		if(custTop.get(cust.getID()) != null) {
                			 Double vv = Double.parseDouble( custTop.get(cust.getID()) + "");
                			 if(agentMust.get(ag) != null) {
                				 ArrayList<Double> vv1 = agentMust.get(ag);
                				 vv1.add(vv);
                				 this.agentMust.put(ag,  vv1 );
                			 } else {
                				 ArrayList<Double> vv1 = new ArrayList<Double>();
                				 vv1.add(vv);
                				 this.agentMust.put(ag, vv1 );	 
                			 }
                		}
                	  
                		
                		if(ag.trim().indexOf("SH") == -1 && !cust.hasAgent("HOUSE1")){
                             cust.addAgent(new Agent("HOUSE1")); 
                         } 
                	}
                }
                
                if(custMercs != null && custMercs.get(cust.getID()) != null) {
                	Vector<String> ags = custMercs.get(cust.getID());
                	for (String ag : ags) {
                		cust.addAgent(new Agent(ag));
                	}
                }

           
                if (cust.getID().equals("0100000")) {
                	agentc = "aia";

                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        if(ags.contains(a.getID().trim()))
                        cust.addAgent(a);
                        
                        }
                      cust.addAgent(new Agent("HOUSE"));  
                      cust.addAgent(new Agent("HOUSE1"));
                      cust.addAgent(new Agent("HOUSE3"));
                      }

         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }


         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length()>0){
         	if(grupp.trim().equals("01")){
         		grupp = "A";
         	}
        	if(grupp.trim().equals("02")){
         		grupp = "B";
         	}
        	if(grupp.trim().equals("03") || grupp.trim().equals("04") ||
        			grupp.trim().equals("05")  || grupp.trim().equals("08")
					 || grupp.trim().equals("09")
					 || grupp.trim().equals("14")){
         		grupp = "C";
         	}
        	if(grupp.trim().equals("06")){
         		grupp = "D";
         	}
            cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
            Vector vv = new Vector();
            vv.add(grupp.trim());
            cust.setFixSortCode(vv);
         }

         String alegr = rs.getString("klale");
         if(alegr != null && alegr.trim().length()>0){
         	cust.setGroup(Converter.cleanLitLatEst(alegr.trim()));
            cust.setChain(alegr.trim());
         }
     	 // cust.setSupervisorFilled(true); //vaja  nagu oleks k�igi juures tehtud 

         if(!instance.equals("shell")) {
         double minsum = rs.getDouble("minsum");
          if(minsum > 0){
        	  cust.setMinsum(minsum);
          }
          if(minordsum == -1){
        	  cust.setMinsum(-1);
          }
         }
          String logsol = rs.getString("logsol");
          if(logsol != null){
        	  logMap.put(logsol, cust.getID());
          }
          String teade = rs.getString("eucerin");
          if(teade != null && teade.trim().length() != 0){
        	  if(teade.equals("1")){
        		  cust.setInfo("Shell netirgot !||");
        		  cust.setCity("Shell netirgot !");
        	  }
          }
          
          String lic  = rs.getString("lic");
          if(lic != null && lic.trim().length() != 0){
        	  if(lic.equals("1")){
        		  cust.setLicence("OTC");
        		  //kllendile v�ib m��a
        		  
        		  // 2 ei v�i 
        		  // tyhi siis ka v�ib 
        	  }
        	  
        	  if(lic.equals("3")){
        		  cust.setLicence("ALC");
        	  }
        	  
          }
      
        	if(cust.getID().equals("0100000")){
        		  cust.setLicence("OTC");
        	}
        	
        	   String klgrupp  = rs.getString("klgrupp");
               if(klgrupp != null && klgrupp.trim().length() != 0){
              	 cust.setBlockingDealer(klgrupp.trim());
               }

        	
        	
          
          
             String rstreet  = rs.getString("rstreet");
             if(rstreet != null && rstreet.trim().length() != 0){
            	 cust.restStreet = rstreet.trim();
             }
             
             String rcity  = rs.getString("rcity");
             if(rcity != null && rcity.trim().length() != 0){
            	 cust.restCity = rcity.trim();
            	 cust.setCountry("Latvia");
             }
             
             String rkategooria  = rs.getString("rkategooria");
             if(rkategooria != null && rkategooria.trim().length() != 0){
            	 cust.category = rkategooria.trim() ;
             }
           
             String rchain  = rs.getString("rchain");
             if(rchain != null && rchain.trim().length() != 0){
            	// cust.setChain(rchain.trim());
            	 
             }

             String vedu  = rs.getString("vedu");
             vedu = veod.get(cust.getID());
             if(vedu != null && vedu.trim().length() != 0){
            	 if(cust.getCity() != null && cust.getCity().trim().length() > 0){
            		 cust.setCity( cust.getCity() + "|Piegades diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }else{
            		 cust.setCity("Piegades diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }
                 cust.setCity(Converter.enCodeEst(cust.getCity()));
             }
             
             try {
            	 
            
             if(custTop.get(cust.getID()) != null) {
            	
            	 Double vv = Double.parseDouble( custTop.get(cust.getID()) + "");
            	// System.out.println("oioi " + vv);
            	 if(vv != null && vv > 0) {
            		 if(cust.getCity() != null && cust.getCity().trim().length() > 0){
                		 cust.setCity( cust.getCity() + "|Must prod Percent - " +  getNF().format(vv*100) + "%"); 	 
                	 }else{
                		 cust.setCity("Must prod Percent - " +  getNF().format(vv*100) + "%"); 	
                		 
                	 }  
            	 }
            	
             }
             
             
             if(custTop.get(cust.getID()+"nonsold") != null) {
            	
            	 ArrayList vv = (ArrayList) custTop.get(cust.getID()+"nonsold");
            	// System.out.println("oioi " + vv);
            	 if(vv != null && vv.size() > 0) {
            		 String nn = vv.toString().replace('[', ' ').replace(']', ' ').trim();
            		 if(nn.length() > 100) {
            			 nn = nn.substring(0,100);
            		 }
            		cust.addCity("NonSold Groups: " + nn); 
            	 }
            	
             }
             
             }catch(Exception e) {
            	 e.printStackTrace();
             }
            
             
             if(salesres.get(cust.getID()) != null){
               	cust.setInfo(Converter.enCodeEst("CHART sales Eur. :|Month & year|Sales |" )+ salesres.get(cust.getID()) );
               }

             cust.topstatus = false;
             String customgroup2 = rs.getString("customgroup2");
             if(customgroup2 != null && customgroup2.trim().equals("MustStock")){
            	 cust.topstatus = true;
             }
             
          
          if(cust.getID().toLowerCase().indexOf("pr")==-1 && agentc != null && agentc.toLowerCase().indexOf("pr")==-1){
              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);
        	  
               }
              }
            }

            rs.close();
            
            
            String sql3 = "select klient , sum(summa) as summa from getdebt where kuupaev <= getdate() group by klient ";
          //  System.out.println(sql3);
                rs = stmt.executeQuery(sql3);
                while(rs.next()){
                
            Customer cc = (Customer)custMap2.get(rs.getString("klient"));
           	if (cc != null){
               double debt = Double.parseDouble(rs.getString("summa").trim());
               if(debt != 0){
               	cc.setDebt(debt);
               }
           	 }
        	
                }
            
            sql3 = "select * from getdebt ";
        //    System.out.println(sql3);
                rs = stmt.executeQuery(sql3);
                
               while(rs.next()){
            	     Debt debt = new Debt();
                     debt.setCustID(rs.getString("klient"));
                     debt.setDoknr(rs.getString("number").trim());
                     debt.setDuedate(rs.getDate("kuupaev"));
                     debt.setSum(round(rs.getDouble("summa"),2));
                     debt.setDue(rs.getString("yle"));
                     debt.setDate(rs.getDate("algp"));
                     this.debt.addElement(debt);
                 }
               
        rs.close();
        stmt.close();
        con.close(); 
           
               
        } catch (Exception e) {
            getParent().showError(e);
        }
        
        StringBuffer sb = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
	    String date = Converter.getDDpMMpYYYYpString(Calendar.getInstance());
	   
	
	    sb2.append(date+"\n");
        
        if(agentMust != null) {
        	sb.append("<!DOCTYPE html><html><body>");

        	 sb.append("<p>");
     	    sb.append("Must products by Agent " + date);
     	    sb.append("<br></p>");
     	    
        	Iterator itera =  agentMust.keySet().iterator();
        	while(itera.hasNext()) {
        		String ag = (String) itera.next();
        		  Agent agen = (Agent)agents1.get(ag);
                  if (agen != null){
                	  double avg = 0;
                	  ArrayList<Double> aa = agentMust.get(ag);
                	  double size = aa.size();
                	  double sum = 0;
                	  for(Double x : aa) {
                		  sum = sum + x;
                	  }
                	  sb2.append(ag + "," + agen.getName() + "," + (100* sum/size) + "\n");
                	  sb.append("<p><br>");
                	 sb.append(agen.getName() + " = " + this.getNF().format(100* sum/size) + "%");
                	  		 sb.append("</p>");

                  }
        	}
        	
        	sb.append("</body></html>");
        	if(!instance.equals("shell")) {
        	Converter.saveFile("reports/mustreportagent.html", sb.toString());
        	Converter.saveFile("reports/mustreportagent.csv", sb2.toString());
        	}
        
         }
        
        System.out.println("kliendid l�petan ");
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
    String    sql = " select * from GETPRODGROUPS";
    	
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("grupa").trim();
      String nimi = Converter.enCodeEst(rs.getString("nimetus2").trim());
      if(instance.equals("shell")) {
    	  if(liik.startsWith("74")) {
    	        tm.put(liik, nimi);
    	    }
      }else {
    	  if(!liik.startsWith("74")) {
    		  
    		  nimi = nimi.replaceAll("Ajax ", "Aja ");
    		  nimi = nimi.replaceAll("Balsnack ", "Bal ");
    		  nimi = nimi.replaceAll("Colgate zobu ", "Col Z ");
    		  nimi = nimi.replaceAll("Colgate ", "Col ");
    		  nimi = nimi.replaceAll("Colgate-Palmolive", "Col Cit");
    		  nimi = nimi.replaceAll("Libresse ", "Lib ");
    		  
    		  nimi = nimi.replaceAll("Nivea deo - ", "Niv D ");
    		  nimi = nimi.replaceAll("Nivea virie11120xxu ", "Niv V ");
    		  nimi = nimi.replaceAll("Nivea ", "Niv ");;
    		  
    		  nimi = nimi.replaceAll("Palmolive ", "Pal ");
    		  nimi = nimi.replaceAll("Vileda ", "Vil ");
    		  nimi = nimi.replaceAll("Zewa ", "Zew ");
    		  
    		  
  	        tm.put(liik, nimi );
  	    }}  
      }
      
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = " select customgroup1 from getproducts where customgroup1 != '' group by customgroup1 ";
	    	
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("customgroup1").trim();
	  	        tm.put(liik, liik );
	      }
	    
	    rs.close();
	    st.close();
	    con.close();

	    return tm;
  }

  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();

  public TreeMap getProducts(Agent agent){

    String cmd = " select * from getproducts where nimi1 != ''";
    shell = new  HashSet<String>();
    getSaveStockInfo();
  
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("toode").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi1").trim() ;
     nimetus =  Converter.enCodeEst(nimetus);
     prod.setPrice(rs.getDouble("myygihind"));
     String vat = rs.getString("vat");
     if(vat!=null && vat.trim().length()>0){
       prod.setVat(vat.trim());
     }

     if(kogused.get(kood) != null) {
    	 prod.setStock(Double.parseDouble(kogused.get(kood)+""));
     }else {
    	 prod.setStock(0);
     }

     String unit = rs.getString("unit");
     if(unit!=null && unit.trim().length()>0){
        prod.setUnit(unit.trim());
      }
  
        prod.setInCases(rs.getDouble("tykke"));
     
        double min = rs.getDouble("minimum");
        if(min > 1) {
        	//prod.setMinAmountInUnits(min);
        }
        
        double opt = rs.getDouble("optimum");
        if(opt > 1) {
        	prod.setOptAmountInUnits(opt);
        }
        

        String otc = parent.make(rs.getString("otc"));  
        
        String grupp2 = rs.getString("grupp");     
        if(grupp2 != null && grupp2.trim().length()>0){
               prod.setGroup(grupp2);
            }


        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean.trim());
        if(ean.trim().length() > 4) {
        	//add = ean.substring(ean.length()-4);
        }
      }
     

        String sort =  ""; 
        if("true".equals(getParent().getProps().getProperty("use.prodsorting"))) {
        sort = rs.getString("sorting");
       
        if(sort != null && sort.trim().length()>0){
         if(sort.length() < 3) {
        	 sort = "0" + sort;
         }
         if(sort.length() < 3) {
        	 sort = "0" + sort;
         }
       
         
         String gr2 = rs.getString("customgroup1");
         if(gr2 != null && gr2.trim().equals("MustStock")){
           //  prod.setGroupSecond(gr2.trim());
             prod.setStatusTop("1");
            // System.out.println("Must toode " + prod.getID());
           }
        	
        	 
        	  sort = prod.getGroup() + " " + sort; 
        	  prod.setNameSorting(sort + nimetus);
         
       
      }else {
    	  sort = "xxx";
      }
        
        }
      
      prod.setName(nimetus + add);
      
      prod.setAllowNotGift(true);
      if(prod.getGroup().startsWith("151") || prod.getGroup().startsWith("159")) {
    	  prod.setAllowNotGift(false);
      }
   
      
      if(kogusedStr.get(kood) != null){
      	prod.setPartii(prod.getPartii() +(String)kogusedStr.get(kood) );     	
      }    
      if(instance.equals("shell")) {
    	
    	  if(otc.equals("SHELL")) {
    		  products.put(nimetus, prod);
              prodHash.put(prod.getID(), prod);	 
              shell.add(prod.getID());
    	  }
    
      }else {
    	  if(!otc.equals("SHELL")) {
    		  products.put(sort + nimetus, prod);
              prodHash.put(prod.getID(), prod); 	  
    	  }else {
    		  shell.add(prod.getID());
    	  }
    	 
      }
       

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }

HashSet<String> shell = null;


  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	 
	
	    
	    String cmd = " select KLIENT ,r.TOODE,max(KOGUS) as KOGUS ,KUUPAEV, max(DAYS30) as DAYS30,max(LPRICE) as LPRICE ,max(BILL) as BILL ,max(pr.grupp), max(pr.sorting)\r\n" + 
	    		"	    		 from getcustomerproductlist r inner join getproducts pr on pr.toode= r.toode  where KUUPAEV > getdate()-180 and len(KLIENT) > 0 \r\n" + 
	    		"	    		 GROUP BY klient,r.toode,kuupaev  ORDER BY klient,max(pr.grupp),max(pr.sorting),r.toode,kuupaev " ;

	    
	    

	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	  
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("kogus");
	        String lastdate = rs.getString("kuupaev").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("lprice");
	     	        String bill = rs.getString("bill");          
	        CustProd cp = new CustProd(kliendikood , tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	           cp.setAbiKood(bill);

	        if(instance.equals("shell")) {
	        	 if( shell.contains(tootekood)){
	        		  custProds.add(cp);
	        	  }
	        	
	        }else {
	        	if( !shell.contains(tootekood)){
	        		 custProds.add(cp);	
	        	 }
	        	
	        }
	        
	        
	       
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
         e.printStackTrace();
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
    
      
      
      String cmd =" SELECT *  FROM getAgentSales  ";

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
                  }
                  
                }
                rs.close();
                stmt.close();
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }
            
            
             if(agentMust != null) {
            	Iterator itera =  agentMust.keySet().iterator();
            	while(itera.hasNext()) {
            		String ag = (String) itera.next();
            		  Agent agent = (Agent)agents.get(ag);
                      if (agent != null){
                    	  double avg = 0;
                    	  ArrayList<Double> aa = agentMust.get(ag);
                    	  double size = aa.size();
                    	  double sum = 0;
                    	  for(Double x : aa) {
                    		  sum = sum + x;
                    	  }
                    	  
                        agent.setStart("Must Prods: " + getNF().format(sum/size*100) + "%");
                      }
            	}
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
    return "AxaJNGLAT";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT custname + ' '+ custaddress as klient , ordernum as arve "
	       		+ ",orderdate as kuupaev ,sumoforder as summa from GETORDERS  where managerid ='"+ agent.getID()+"'";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\\n\\n Sanemtie pasutijumi :\n\n");
	   

	      try {
	    	  Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append(rs2.getString("klient")  +
                       		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev") + "] " +
                       		getParent().getNF().format(rs2.getDouble("summa"))
                       	+" EUR "	+  "\n\n");
	                  
	                  }
	                  rs2.close();
	                  stmt.close();
	                  con.close();

	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }

    //    System.out.println(agent.getID() + " " + sb.toString());
	      return sb.toString();
	  }




       public NumberFormat getNF(){
         return NF;
       }

       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:axa";
  private   Vector debt = new Vector();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

	@Override
	public void FixReporter() {
		 try{
		    // tõmbame lepingud sisse 	
			 TreeMap contracts = new TreeMap();
		    	Converter.getContractsSuggByCustOrType(contracts,rcompany,user,pass,true) ;	 
		    	Converter.loadCustDimensions("type",rcompany,user,pass);
		     TreeMap custTypes =  Converter.getCustDimsLoading("type");
		    	
		     
		     
		    	System.exit(0);
		     }catch(Exception r){
		    	 r.printStackTrace();
		     }
		
	}

@Override
public void createDashboard() {
	System.out.println("Testin");
 
	this.getCustomers(null);
	Map<String, String[]> agentData = null;
	TreeMap data1 = new TreeMap();
	
   	Iterator itera =  agentMust.keySet().iterator();
	while(itera.hasNext()) {
		String ag = (String) itera.next();
		  Agent agen = (Agent)agents1.get(ag);
          if (agen != null){
        	  double avg = 0;
        	  ArrayList<Double> aa = agentMust.get(ag);
        	  double size = aa.size();
        	  double sum = 0;
        	  for(Double x : aa) {
        		  sum = sum + x;
        	  }
        	  
        	   DashboardData dd1 = new DashboardData(ag);
    		   dd1.dataLabel = agen.getName();
    		   dd1.dataValue = (double) Converter.Round(((float)(100* sum/size)), 2);
    		   data1.put(ag, dd1);
        	  
        	

          }
	}
	
	 DashboardData dd1 = new DashboardData("LABEL");
	 DashboardData dd01 = new DashboardData("LABEL");
	 dd01.dataLabel = "Name";
	 dd01.dataType = "string";
	 dd1.addArrElement(dd01);
	 DashboardData dd02 = new DashboardData("LABEL");
	 dd02.dataLabel = "Percent";
	 dd02.dataType = "number";
	 dd02.sorting = "desc";
	 dd1.addArrElement(dd02);
	 
	 
	   data1.put("LABEL", dd1);
	


	
	 agentData = Converter.getChartData("AgenMustProducts", "Table", data1);
	   ArrayList data = new ArrayList();
	   
	   data.add(agentData);
	   
	/*   Map<String, String[]> agentData1 = null;
	   
	   TreeMap data2 = new TreeMap();
		 itera = agent1.keySet().iterator();
		while (itera.hasNext()){
			
			String agId = (String) itera.next();
			Agent ag = (Agent) agent1.get(agId);
		    String agName = Converter.deCode(ag.getName());
	        int agSales = ag.getMonthSales();
	        if (agSales < 0) {
	        	agSales = 0 ;
	        }
			System.out.println(agId + " " + agName + " " + agSales);
			   DashboardData dd1 = new DashboardData(agId);
			   dd1.dataLabel = agName;
			   dd1.dataValue = (double) agSales;
			   data2.put(agId, dd1);
		}
		 agentData1 = Converter.getChartData("Testime Sõõrikut", "Donut", data2);
	   data.add(agentData1);*/
	   //data.add(agentData3);
	   //data.add(agentData4);
	   //data.add(agentData5);
	   //data.add(agentData6);
	   HTMLeditor hm = new HTMLeditor();
	   String url = "https://mobitrek.eu/reporting/logoproxy/help/jungent.png";
	   ArrayList type = new ArrayList();
	   String topLabel = "Jungent Latvia data";
	   
	   hm.genChart(url,data,topLabel);
	   
	   try {
			 FileSender fs = new FileSender();
			fs.uploadDash();
		   } catch (Exception e1) {
			e1.printStackTrace();
		   }
	
	System.exit(0);
}
	
}
