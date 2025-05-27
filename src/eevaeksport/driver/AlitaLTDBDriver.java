package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AlitaLTDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public AlitaLTDBDriver(EksportPanel parent){
    super(parent);
    
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
  //    con = getNewConnection();
    //  stmt = con.createStatement();
      loadEventProds();
    } catch (Exception e){
       parent.showError(e);
    }
  }

  

	public void loadEventProds() throws IOException{

		File file = new File(this.getExcelDir() + "Customers.txt");
		if (file.exists()){
			custnames = new TreeMap();
			FileReader ff = new FileReader(file);
			BufferedReader br = new BufferedReader(ff);
			String line = null;
			while( (line = br.readLine()) != null){
			//	System.out.println(line);
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()){
					String prodID = st.nextToken().trim();
					if (st.hasMoreTokens()){
						String name = st.nextToken().trim();
						custnames.put(prodID, name);	
					}
				
					}
				}
			
		ff.close();
		br.close();
		}
	
		
	}
	
	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
		 try {
			loadEventProds();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
	     String sql = "INSERT into supervisor values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
	       + " '' ,"   //	receivedTime	D,
	       + "?,"   //normalpr	N,
	       + "?,"   // amoun	N,
	       + "?,"   // grouptotal	N,
	       + "?,"   // cattotal	N,
	       + "?,"   // pertotcat	I,
	       + "?,"   // percolcat	I,
	       + "?,"   // perpalcat	I,
	       + "?,"   // pershecat	I,
	       + "?,"   // perrescat	I,
	       + "?,"   // isaction	L,
	       + "?,"   // ispartner	L,
	       + "?,"   // isbrand	L,
	       + "?,"   // isgroup	L
	       + "?,"   // actionaeva	N,
	       + "?,"   // actionbeva N,
	       + "?,"   // actiontota	N,
	       + "?,"   // custeval	N,
	       + "?,"   // custcomme	C(200),
	       + "?,"   // comment	C(200),
	       + "?,"   // custagent	C(200),
	       + "?,"   // custagentname	C(200),
	       + "?,"   // mustbe	I,
	       + "?,"   // ideal	I,
	       + "?,"   // resstr1	C(200),
	       + "?,"   // resstr2	C(200),
	       + "?,"   // resstr3	C(200),
	       + "?,"   // resint1	I,
	       + "?,"   // resint2	I,
		      + "?)" ; //	resint3	L

	       try {
	       	System.out.println("jam");
	        Connection con = getNewConnection(url2);
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2,Converter.deCode( wr.getInfo().getAgentName()));
	             ps.setString(3, lw.getLine().getCustID());
	             if(custnames.get(lw.getLine().getCustID()) != null){
	            	 ps.setString(4, custnames.get(lw.getLine().getCustID())+"");
	            	 System.out.println("1 " + custnames.get(lw.getLine().getCustID()));
	               }else{
	            	 ps.setString(4,Converter.deCode( lw.getLine().getCustName()));
	            	 System.out.println("2 " +Converter.deCode( lw.getLine().getCustName()));
	              }
	            
	             ps.setString(5, Converter.deCode(lw.getLine().getProdID()));
	             ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
	             ps.setDouble(9, lw.getLine().getGroupTotal());
	             ps.setDouble(10, lw.getLine().getCategoryTotal());
	             ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	             ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	             ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	             ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	             ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	             ps.setBoolean(16, lw.getLine().isAction());
	             ps.setBoolean(17, lw.getLine().isPartner());
	             ps.setBoolean(18, lw.getLine().isBrand());
	             ps.setBoolean(19, lw.getLine().isGroup());
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, Converter.deCode(cc));

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, Converter.deCode(cd));

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
	             ps.setInt(28, lw.getLine().getDealNr());
	             ps.setInt(29, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(30, Converter.deCode(cr1));


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, Converter.deCode(cr2));


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, Converter.deCode(cr3));
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, 111); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
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
   	return null;
   }

  private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

  private HSSFSheet getSheet (String lehenimi){
	File f = new File(this.getDir() + "/excel/MasterData.xls");
		
		try {
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			return workbook.getSheet(lehenimi);	
		}catch(Exception e){
			e.printStackTrace();
		}	
		return null;
  
  }
  
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		try {
			HSSFSheet sheet = getSheet("FixAssortment");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 5; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 5; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 1).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 4; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi t�his
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
								// clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
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
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.exit(0);

		return result;

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          return lines;
  }

  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
		StringBuffer sb = new StringBuffer();
	
		HSSFSheet sheet = getSheet("FixAssortment");
		HSSFRow row = null;
		

	//	System.out.println("rownum " + row.getLastCellNum());
		
		
		for(int a = 5 ; a < sheet.getLastRowNum()+1;a++){
			
		
				row = sheet.getRow(a);
				if (row.getCell((short) 1) != null) {
				
			   String kood = null;	
			   String yes = null;
			   try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
				   kood =  row.getCell((short) 1).getStringCellValue();
				} catch (Exception e) {
					try{
						kood =  ((int)row.getCell((short)1).getNumericCellValue()) + "";
	                   }catch(Exception y){
					}
				}
				 try {
						// System.out.println(row.getCell((short)a).getStringCellValue());
					   yes =  row.getCell((short) 12).getStringCellValue();
					} catch (Exception e) {
						try{
							yes =  ((int)row.getCell((short)12).getNumericCellValue()) + "";
		                   }catch(Exception y){
						}
					}

					if(yes != null && yes.trim().length() >0){
						sb.append(" ; ;PRICE;" + kood
								+ "\n");
					}
						
				
				}
			}
				return sb.toString();
  }
  
  
  
  private TreeMap kogused = null;

  
 public String getSaveStockInfo(){
	return null ;
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
      return dealerProds;
     }

  
  
  
  public TreeMap getAgents(){
	  //  getCustomers(null);
	    TreeMap agents = new TreeMap(); 
	 	Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("replacement.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    HSSFSheet sheet = getSheet("Customers");
		 HSSFRow row = null;
		
		int max = sheet.getLastRowNum();
		// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

		// System.out.println("rownum " + row.getLastCellNum());
		for (int a = 0; a < max; a++) {
			row = sheet.getRow(a);
			String ag = null;
			String ag1 = null;
			String ag2 = null;
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				ag =  row.getCell((short) 10).getStringCellValue();
			} catch (Exception e) {
				try{
				ag =  ((int)row.getCell((short)10).getNumericCellValue()) + "";
                   }catch(Exception y){				
				}
			}
			
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				ag1 =  row.getCell((short) 11).getStringCellValue();
			} catch (Exception e) {
				try{
				ag1 =  ((int)row.getCell((short)11).getNumericCellValue()) + "";
                   }catch(Exception y){
				}
			}
			
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				ag2 =  row.getCell((short) 12).getStringCellValue();
			} catch (Exception e) {
				try{
				ag2 =  ((int)row.getCell((short)12).getNumericCellValue()) + "";
                   }catch(Exception y){
				}
			}
		
			//System.out.println("Lisan Duubleid " + "dub." + ag + " " + name);
			if(ag!= null && ag.trim().length() >0){
				Agent agent = new Agent(ag.trim());
				agent.setName(ag.trim());
				 agents1.put(ag.trim(), agent);
		          agents.put(ag.trim(), agent);	
			}
			if(ag1!= null && ag1.trim().length() >0){
				Agent agent = new Agent(ag1.trim());
				agent.setName(ag1.trim());
				 agents1.put(ag1.trim(), agent);
		          agents.put(ag1.trim(), agent);	
			}
			if(ag2!= null && ag2.trim().length() >0){
				Agent agent = new Agent(ag2.trim());
				agent.setName(ag2.trim() + " SV");
				 agents1.put(ag2.trim(), agent);
		          agents.put(ag2.trim(), agent);	
			}
			
		}
  return agents;
  }
  

  TreeMap custnames = null;
  TreeMap custMap = null;
  
  public TreeMap getCustomers(Agent agent){
	  custnames = new TreeMap();
	  Converter cc = new Converter();
	
	  TreeMap customers = new TreeMap();
	  custMap = new TreeMap();
	   Vector payers =  new Vector();
  	
	   try{
        
  		HSSFSheet sheet = getSheet("Customers");
  		HSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  	//	System.out.println("rownum " + row.getLastCellNum());
  		
  		
  		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
  			try{
  		
  				row = sheet.getRow(a);
  				if (row.getCell((short) 0) != null) {
  				
 			   String kood = null;
 			   String name = null;
 			   String sort = null;
 			  String payer = null;
 			 String region = null;
 			 String address = null;
 			 String visit = null;
 			 String type = null;
 			String chain = null;
 			   String ag = null;
 			   String ag1 = null; 
 			   String ag2 = null;
	    	    			  	try{
	    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
	    	    			  	 }catch (Exception e){
	    	    			   	  kood = ((int)row.getCell((short)0).getNumericCellValue())+"";  
	    	    	    		}
	    	    			  	try{
	    	    			  		visit = row.getCell((short)13).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		try{
				    	    			  	
		    	    			  		 visit = ((int)row.getCell((short)13).getNumericCellValue())+"";  
                                           }catch (Exception o){
			    	    			  		
			    	    	    		} 
	    	    			  	 }
		    	    			  	 
		    	    			  	try{
		    	    			  		address = row.getCell((short)4).getStringCellValue().trim() + ", " + row.getCell((short)5).getStringCellValue().trim();  
			    	    			  	 }catch (Exception e){
			    	    			  		
			    	    	    		} 
	    	    			  	 
	    	    			  	try{
	    	    			  		chain = row.getCell((short)6).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		chain = ((int)row.getCell((short)6).getNumericCellValue())+"";  
		    	    	    		} 
		    	    			 	try{
		    	    			  		type = row.getCell((short)7).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		type = ((int)row.getCell((short)7).getNumericCellValue())+"";  
			    	    	    		} 
	    	    			  	try{
	    	    			  		payer = row.getCell((short)3).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		 try{
		    	    			 	  		payer = ((int)row.getCell((short)3).getNumericCellValue())+"";  
		    			    	    	 }catch(Exception k ){
		    	    			  			 
		    	    			  		 }
		    	    				} 
	    	    			  	
		    	    			 	try{
		    	    			  		region = row.getCell((short)3).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  			region = ((int)row.getCell((short)3).getNumericCellValue())+"";  
					    	    			  	    	 }catch(Exception k ){
				    	    			  			 
				    	    			  		 }
			    	    			  	 
			    	    			  	 } 
	    	    			  	try{
	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		 try{
		    	    			  			sort = ((int)row.getCell((short)1).getNumericCellValue())+"";   
		    	    			  		 }catch(Exception t){
		    	    			  			 
		    	    			  		 }
		    	    			  		
		    	    	    		} 
		    	    			  	 
			    	    	     try{
			    	    			  name = row.getCell((short)2).getStringCellValue();  
				    	    		  }catch (Exception e){
				    	    			name = ((int)row.getCell((short)2).getNumericCellValue())+"";  
				    	    	      }  
				    	    	//	  custnames.put(kood,name);
				    	    		  try{
				    	    			  ag = row.getCell((short)10).getStringCellValue();  
					    	    		  }catch (Exception e){
					    	    			ag = ((int)row.getCell((short)10).getNumericCellValue())+"";  
					    	    	      }  
		    	    			    //    System.out.println("agent'" + ag + "'");
		    	    			       
					    	    		  try{
					    	    			  ag1 = row.getCell((short)11).getStringCellValue();  
						    	    		  }catch (Exception e){
						    	    			ag1 = ((int)row.getCell((short)11).getNumericCellValue())+"";  
						    	    	      }  
			    	    			        
						    	    		  try{
						    	    			  ag2 = row.getCell((short)12).getStringCellValue();  
							    	    		  }catch (Exception e){
							    	    			ag2 = ((int)row.getCell((short)12).getNumericCellValue())+"";  
							    	    	      }  
				    	    			        
		    	    			        Customer cust = new Customer(kood.trim());
		    	    			       if( sort != null){ 
		    	    			    	   Vector vv = new Vector();
		    	    			    	   vv.add(sort.trim());
		    	    			    	   cust.setFixSortCode(vv);
		    	    			       }
                                       
		    	    			        cust.setName(Converter.enCodeEst(name.trim()) + " " + Converter.enCodeEst(address.trim().replaceAll("\\r\\n|\\r|\\n", " ")));
		    	    			        cust.addAgent(new Agent(ag.trim()));
		    	    			        if(ag1 != null && ag1.trim().length() > 0){
		    	    			        	cust.addAgent(new Agent(ag1.trim()));
		    	    			        }
		    	    			        
		    	    			        if(ag2 != null && ag2.trim().length() > 0){
		    	    			        	cust.addAgent(new Agent(ag2.trim()));
		    	    			        }
		    	    			        
		    	    			        if(payer != null && payer.trim().length() > 0){
		    	    			        	cust.setPayer(payer.trim());
		    	    			        }
		    	    			        
		    	    			        if(region != null && region.trim().length() > 0){
		    	    			        	cust.setCity(region.trim());
		    	    			        }
		    	    			        
		    	    			        if(chain != null && chain.trim().length() > 0){
		    	    			        	cust.setGroup(chain.trim());
		    	    			        	cust.setAnkeediTyyp(chain.trim());
		    	    			        }
		    	    			        
		    	    			        if( type != null && type.trim().length() > 0){
		    	    			        	
		    	    			        }
		    	    			        cust.setCustType("PRICE");
		    	    			        
		    	    			        if( visit != null && visit.trim().length() > 0){
		    	    			        	cust.setVisit(Converter.getVisitDay(visit.trim()));
		    	    			        }
		    	    			        
		    	    			        if( address != null && address.trim().length() > 0){
		    	    			        	cust.setAddress(Converter.adressLink(Converter.enCodeEst(address.trim().replaceAll("\\r\\n|\\r|\\n", " ")),"Lithuania"));
		    	    			        }
		    	    			        
		    	    			        
		    	    			        if(kood.trim().length() > 3){
		    	    			            
			    	    			        customers.put(cust.getName(), cust);
			    	    			        custMap.put(kood.trim(), cust);	
		    	    			        }
		    	    			    
  	    				
  				   
  				}
 			}catch (Exception e){
  			e.printStackTrace();
  			}
  		}
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}



  //	System.out.println("Customers " + customers.toString());
   

    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
      public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {
    return groups;
}
private TreeMap groups = new TreeMap();

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

  public TreeMap getProducts(Agent agent){

	  getNF().setMaximumFractionDigits(2);
	   TreeMap products = new TreeMap();
		HSSFSheet sheet = getSheet("FixAssortment");
		HSSFRow row = null;
		

	//	System.out.println("rownum " + row.getLastCellNum());
		
		
		for(int a = 5 ; a < sheet.getLastRowNum()+1;a++){
			
		
				row = sheet.getRow(a);
				if (row.getCell((short) 0) != null) {
				
			   String kood = null;
			   String type = null;
			   String name = null;
			   String ean = null;
			   double price = 1;
			   double pricer = 1;
			   double priced = 1;
			   String group = null;
			   String groupname = null;
			  
			   
	    	    			  	try{
		    	    			  	  kood = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = ((int)row.getCell((short)1).getNumericCellValue())+"";  
		    	    	    		} 
		    	    			  	 
		    	    			 	try{
			    	    			  	  group = row.getCell((short)3).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			   	  group = ((int)row.getCell((short)3).getNumericCellValue())+"";  
			    	    	    		} 
			    	    			 	try{
				    	    			  	  pricer = Double.parseDouble(row.getCell((short)10).getStringCellValue());  
				    	    			  	 }catch (Exception e){
				    	    			  		 try{
				    	    			  	 
				    	    			  		  pricer = row.getCell((short)10).getNumericCellValue();  
				    	    			  	 }catch(Exception r){
				    	    			   
				    	    			  	 }} 
				    	    			  	 
				    	    			 	try{
					    	    			  	  priced = Double.parseDouble(row.getCell((short)11).getStringCellValue());  
					    	    			  	 }catch (Exception e){
					    	    			  		 try{
					    	    			  	 
					    	    			  		  priced = row.getCell((short)11).getNumericCellValue();  
					    	    			  	 }catch(Exception r){
					    	    			   
					    	    			  	 }} 
					    	    			  	 
			    	    			 	try{
				    	    			  	  price = Double.parseDouble(row.getCell((short)8).getStringCellValue());  
				    	    			  	 }catch (Exception e){
				    	    			  		 try{
				    	    			  	 
				    	    			  		  price = row.getCell((short)8).getNumericCellValue();  
				    	    			  	 }catch(Exception r){
				    	    			   
				    	    			  	 }} 
				    	    			  	// System.out.println("price'" + price +"'");
			    	    			 	try{
				    	    			  	  groupname = row.getCell((short)4).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			   	  groupname = ((int)row.getCell((short)4).getNumericCellValue())+"";  
				    	    	    		} 
			    	    			  	 
				    	    			  	 if(group != null && groupname != null){
				    	    			  		 groups.put(group.trim(),  Converter.enCodeEst(groupname).trim());
				    	    			  	 }
				    	    			  		 
				    	    			  	 
		    	    			  	 
		    	    			  	try{
			    	    			  	  ean = row.getCell((short)2).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  			 ean = (row.getCell((short)2).getNumericCellValue())+"";  
			    	    			  			 if(ean.indexOf("E") != -1){
			    	    	   				        	ean = ean.substring(0,ean.indexOf("E") );
			    	    	   				         int len  = ean.length();
			    	    	  				          ean = ean.substring(0,1) + ean.substring(2,len);
			    	    	  				          if(ean.length() == 12){
			    	    	  				        	  ean = ean+"0";
			    	    	  				          }
			    	    	   				        }else if(ean.indexOf(".") != -1) {
			    	    	   				        	ean = ean.substring(0,ean.indexOf("."));
			    	    	   				        }
			    	    			  		 }catch(Exception u){
			    	    			  			 
			    	    			  		 }
			    	    			   	  
			    	    	    		} 
			    	    			  	 
		    	    			   	try{
			    	    			  	  type = row.getCell((short)0).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			   	  type = ((int)row.getCell((short)0).getNumericCellValue())+"";  
			    	    	    		} 
			    	    			   	try{
				    	    			  	  type = row.getCell((short)0).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			   	  type = ((int)row.getCell((short)0).getNumericCellValue())+"";  
				    	    	    		} 
			    	    			  
	    	    			  	
	    	    	    	     try{
			    	    			  name = row.getCell((short)5).getStringCellValue();  
				    	    		  }catch (Exception e){
				    	    			name = ((int)row.getCell((short)5).getNumericCellValue())+"";  
				    	    	      }  
				    	    		  
	    	    			   if(kood != null && kood.trim().length() > 0 && name != null){
	    	    				   Product prod = new Product(kood.trim());
			    	    	         name =  Converter.enCodeEst(name);
			    	    	         prod.setName(name.trim() + " (R-" + getNF().format(pricer) + ")(A-" +getNF().format(priced) + ")");
			    	    	         prod.setUnit("btl.");
			    	    	         prod.setPrice(1);
			    	    	         prod.setVat("21");
			    	    	         if(type!= null && type.trim().length() > 0){
			    	    	        	 prod.setType(type.trim());
			    	    	         }
			    	    	         if(ean!= null && ean.trim().length() > 0){
			    	    	        	 prod.setEAN(ean.trim());
			    	    	         }
			    	    	         if(group!= null && group.trim().length() > 0){
			    	    	        	 prod.setGroup(group.trim());
			    	    	         }
			    	    	        	 prod.setPrice(price);
			    	    	         
				    	    	     products.put(name, prod); 
	    	    			   }
					    	    	     

	    		}
		}

    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	    //if (true) return custProds ;
	/* int counter = 1;
	   String cmd = "SELECT jou.orderaccount,tra.itemid ,tra.qty,jou.invoicedate AS lastdate ,max(tra.lineamount/tra.qty) as hind ,max(jou.salesid) as dok " +
	      " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON  " +
	      " tra.invoiceid=jou.invoiceid INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid =tra.dataareaid  " +
	      " WHERE  jou.invoicedate > getdate()-120 AND tra.dataareaid ='dun'  AND jou.dataareaid = 'dun'  AND cus.dataareaid = 'dun'" +
	      " and tra.qty !=0 " +
	      " GROUP BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " +
	      " ORDER BY jou.orderaccount,tra.itemid ,jou.invoicedate ,tra.qty " ;


	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Statement stmt =  this.getNewConnection().createStatement();
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
	    } catch (Exception e){
	      getParent().showError(e);
	      if(counter< 4){
	      	try{
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
	      	}catch (Exception ee){

	      	}

	      }
	    }*/
	    return custProds;
	  }


  public Customer getCustomerSingle(String custid) {
	  this.getCustomers(null);
	  if(custMap.get(custid) != null){
		 return (Customer) custMap.get(custid);
	  }
	  return null;
  }


  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
 /* this.connectionUp();
    String cmd =" SELECT  cus.agent as agent,round(SUM(lineamount),0) AS summa  " +
        " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON  " + 
        " sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid  WHERE " +
	  	" inv.dataareaid='dun' AND sal.dataareaid='dun' AND cus.dataareaid='dun'  AND jou.dataareaid='dun' AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
        "  AND MONTH(sal.invoicedate) = MONTH(getdate()) "+
        " GROUP BY cus.agent";

         if (isDebug()) System.out.println(cmd);
            try {
                Statement stmt =  this.getNewConnection().createStatement();
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
                con.close();
            } catch (Exception e) {
                getParent().showError(e);
             }*/
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
    return "AltiaLT";
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
	                  ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append("(" + rs2.getString("arve").trim() + ") [" + rs2.getString("kuupaev").substring(0,10) + "] - " + rs2.getString("klient") + "  Ridu: " + rs2.getDouble("summa") + "\n");
	                  }

	        //stmt = con.prepareStatement(sql);
	        //stmt.sets(1, agent.getObject());
	          //stmt.setString(2, agent.getObject());
	                  /* 	    String sql =" SELECT  (SELECT SUM(PHYSICALINVENT)FROM INVENTSUM summ INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid WHERE summ.DATAAREAID='bal' and dim.DATAAREAID='bal'" +
	"AND CLOSED = 0 AND dim.inventlocationid ='" + agent.getObject() + "' " +
	"AND summ.itemid=inv.itemid GROUP BY summ.itemid) AS ladu,SUM(sal.qty) AS kogus,avg(sal.lineamountmst/sal.qty) AS myygihind, " +
	"inv.itemname AS nimetus, inv.itemqtyinabox AS tykke " +
	"FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid  WHERE " +
	"inv.dataareaid='bal' AND sal.dimension='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate()) AND DAY(sal.invoicedate) = DAY(getdate()) " +
	"GROUP BY inv.itemname ,inv.itemqtyinabox,inv.itemid ORDER BY inv.itemname ";
   ResultSet	      rs = stmt.executeQuery(sql);
	          ResultSetMetaData md = rs.getMetaData();
	          sb.append("Nimi  \t\tLadu\t\tM��dud\t\tSumma\n");
	          sb.append("---------------------------------------------------\n");

	          double totalKokku = 0d;

	          while (rs.next()) {
	            int ladu = rs.getInt("ladu");
	            int kogus = rs.getInt("kogus");
	            double hind = round(rs.getDouble("myygihind"), 2);
	            double kokku = round(kogus*hind, 2);
	            totalKokku = round(totalKokku + kokku, 2);

	            sb.append(rs.getString("nimetus").trim() + " " + rs.getInt("tykke")+ "\n");

	            sb.append("\t\t\t\t" + ladu + "\t\t" + kogus + "\t\t" +
	              kokku + "\n\n");
	           }

	          sb.append("---------------------------------------------------\n");
	          sb.append("\tP�eva m��k kokku: " + totalKokku + " EUR");

*/
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
  private String url = "jdbc:odbc:AXA";
  private String url2 = "jdbc:odbc:report";
  private Connection con;
  private   Vector debt = new Vector();
  private Statement stmt;
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}
