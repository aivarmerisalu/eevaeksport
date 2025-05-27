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
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

public class OskarLTDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter {
  public OskarLTDBDriver(EksportPanel parent){
    super(parent);
    
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
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
	             ps.setString(4,Converter.deCode( lw.getLine().getCustName()));
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
	             ps.setInt(35, lw.getLine().getResInt3()); 
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

  private XSSFSheet getSheet (){
	File f = new File(this.getDir() + "/excel/oskardata.xlsx");
		
	
			try {
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			return workbook.getSheetAt(0);	
			}catch(Exception es){
				es.printStackTrace();	
				
			}
			
	
		return null;
  
  }
  
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		  String sql = "SELECT DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) from mj_assortment where DATEADD(dd, 0, DATEDIFF(dd, 0, sortdate)) = DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) ";
    	  boolean insertassort = true;
		try {
			
			 Connection con2 = getNewConnection(url2);
 	        Statement stmt2 = con2.createStatement();
 	        
 	        ResultSet rs2 = stmt2.executeQuery(sql);
 	        if(rs2.next()){
 	        	insertassort = false;
 	        	//kontrollime kas on vaja insertida 
 	        }
 	        rs2.close();
 	        stmt2.close();
 	        con2.close();
 	        
			XSSFSheet sheet = (XSSFSheet)getSheet();
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(4);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 4; a < row.getLastCellNum()+1; a++) {
				try {
					
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println(groups.toString());

			for (int i = 7; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 2) != null) {

					try {
						
						prodID = (row.getCell((short) 2).getStringCellValue());
						
					} catch (Exception e) {
						prodID = (int) (row.getCell((short) 2)
								.getNumericCellValue()) + "";
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

							// System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);

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
								 if(insertassort){
		                               insertSortLine(mline);   
		                           }
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
	
	 private void  insertSortLine(MatrixLine mline ){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'')" ;
			  
			  	     Connection  con2 = getNewConnection(url2);      
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     stmt2.close();
				     con2.close();
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	
		  
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
	
		XSSFSheet sheet =(XSSFSheet) getSheet();
		XSSFRow row = null;
		

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
		   StringBuffer sb2 = new StringBuffer();
	    
	    XSSFSheet sheet =(XSSFSheet) getSheet();
		 XSSFRow row = null;
		
		
		row = sheet.getRow(6);
		int max = row.getLastCellNum();
		System.out.println("Max veerge tabelis "  + max );
		
		for (int a = 4; a < max; a++) {
			
			String ag = null;
		
			try {
				
				ag =  row.getCell((short) a).getStringCellValue();
			} catch (Exception e) {
				try{
				ag =  ((int)row.getCell((short)a).getNumericCellValue()) + "";
                   }catch(Exception y){				
				}
			}

			if(ag!= null && ag.trim().length() >0){
				Agent agent = new Agent(ag.trim());
				agent.setName(ag.trim());
				 agents1.put(ag.trim(), agent);
		          agents.put(ag.trim(), agent);	
		          sb2.append(ag.trim() + ";" + agent + "\n");  
			}
		}
		Agent agent = new Agent("ALDO");
		agent.setName("ALDO");
		 agents1.put("ALDO", agent);
          agents.put("ALDO", agent);	
      	Agent agent2 = new Agent("MARGUS");
		agent2.setName("MARGUS");
		 agents1.put("MARGUS", agent2);
          agents.put("MARGUS", agent2);
          
          if(sb2.length() > 2){
          	try{
          		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
          	}catch (Exception e){
          		
          	}
            
           }
		
  return agents;
  }
  

  TreeMap custnames = null;
  
  public TreeMap getCustomers(Agent agent){
	  custnames = new TreeMap();
	
	
	  TreeMap customers = new TreeMap();
	  TreeMap custMap = new TreeMap();
	   Vector payers =  new Vector();
  	
	   try{
        
  		XSSFSheet sheet =(XSSFSheet) getSheet();
  		XSSFRow rowkett = sheet.getRow(3);
  		XSSFRow rowkood = sheet.getRow(4);
  		XSSFRow rownimi = sheet.getRow(5);
  		XSSFRow rowagent = sheet.getRow(6);
  		
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  	//	System.out.println("rownum " + row.getLastCellNum());
  		
  		
  		
  	  for(int a = 4 ; a < rowkood.getLastCellNum()+1;a++){	
 			   String kood = null;
 			   String name = null;
 			   String sort = null;
 			  String payer = null;
 			 String region = null;
 			 String address = null;
 			 String visit = null;
 			 String type = null;
 			String chain = "";
 			   String ag = null;
 			   String ag1 = null; 
 			   String ag2 = null;

 			
 				  
	    	    			  	try{
	    	    			  	  kood = rowkood.getCell((short)a).getStringCellValue();  
	    	    			  	 }catch (Exception e){
	    	    			  		 try{
	    	    			  			 kood = ((int)rowkood.getCell((short)a).getNumericCellValue())+"";  
	    	    			  		 } catch (Exception ey){
	    	    			  			 
	    	    			  		 }
	    	    			   	  
	    	    	    		}
	    	    			  	try{
	    	    			  	//	visit = row.getCell((short)13).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  //		visit = ((int)row.getCell((short)13).getNumericCellValue())+"";  
		    	    	    		}
		    	    			  	 
		    	    			  	try{
		    	    			  	//	address = row.getCell((short)4).getStringCellValue().trim() + ", " + row.getCell((short)5).getStringCellValue().trim();  
			    	    			  	 }catch (Exception e){
			    	    			  		
			    	    	    		} 
	    	    			  	 
	    	    			  	try{
	    	    			  		chain = rowkett.getCell((short)a).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		try{
		    	    			  			chain = ((int)rowkett.getCell((short)a).getNumericCellValue())+"";  
				    	    	    		}catch(Exception m){
		    	    			  		}
		    	    			  } 
		    	    			 	try{
		    	    			  		//type = row.getCell((short)a).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		//type = ((int)row.getCell((short)a).getNumericCellValue())+"";  
			    	    	    		} 
	    	    			  	try{
	    	    			  	//	payer = row.getCell((short)3).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		 try{
		    	    			 //	  		payer = ((int)row.getCell((short)3).getNumericCellValue())+"";  
		    			    	    	 }catch(Exception k ){
		    	    			  			 
		    	    			  		 }
		    	    				} 
	    	    			  	
		    	    			 	try{
		    	    			  	//	region = row.getCell((short)3).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  //			region = ((int)row.getCell((short)3).getNumericCellValue())+"";  
					    	    			  	    	 }catch(Exception k ){
				    	    			  			 
				    	    			  		 }
			    	    			  	 
			    	    			  	 } 
	    	    			  	try{
	    	    			  		sort = rowkood.getCell((short)a).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		 try{
		    	    			  			sort = ((int)rowkood.getCell((short)a).getNumericCellValue())+"";   
		    	    			  		 }catch(Exception t){
		    	    			  			 
		    	    			  		 }
		    	    			  		
		    	    	    		} 
		    	    			  	 if(kood != null){
		    	    			  		 
		    	    			  	 
			    	    	     try{
			    	    			  name = rownimi.getCell((short)a).getStringCellValue();  
				    	    		  }catch (Exception e){
				    	    			name = ((int)rownimi.getCell((short)a).getNumericCellValue())+"";  
				    	    	      }  
				    	    	//	  custnames.put(kood,name);
				    	    		  try{
				    	    			  ag = rowagent.getCell((short)a).getStringCellValue();  
					    	    		  }catch (Exception e){
					    	    			ag = ((int)rowagent.getCell((short)a).getNumericCellValue())+"";  
					    	    	      }  
		    	    			    //   System.out.println("agent'" + ag + "'");
		    	    			       
					    	    		
				    	    			        
		    	    			        Customer cust = new Customer(kood.trim());
		    	    			       if( sort != null){ 
		    	    			    	   Vector vv = new Vector();
		    	    			    	   vv.add(sort.trim());
		    	    			    	   cust.setFixSortCode(vv);
		    	    			       }
                                       
		    	    			        cust.setName(Converter.enCodeEst(chain.trim() + " " + name.trim()));
		    	    			        cust.addAgent(new Agent(ag.trim()));
		    	    			        cust.setMainAgent(ag.trim());
		    	    			        cust.addAgent(new Agent("ALDO"));
		    	    			        cust.addAgent(new Agent("MARGUS"));
		    	    			     
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
  	    				
  				   
  		
  		}
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	  }




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
		XSSFSheet sheet = (XSSFSheet)getSheet();
		XSSFRow row = null;
		

	//	System.out.println("rownum " + row.getLastCellNum());
		
		
		for(int a = 7 ; a < sheet.getLastRowNum()+1;a++){
			
		
				row = sheet.getRow(a);
				if (row.getCell((short) 2) != null) {
				
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
		    	    			  	  kood = row.getCell((short)2).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = ((int)row.getCell((short)2).getNumericCellValue())+"";  
		    	    	    		} 
		    	    			  	 
		    	    			 	try{
			    	    			  	  group = row.getCell((short)1).getStringCellValue();  
			    	    			  	 }catch (Exception e){try{
			    	    			  	   	  group = ((int)row.getCell((short)1).getNumericCellValue())+"";  
						    	    	    	  		 
			    	    			  	 }catch(Exception k){
			    	    			  		System.out.println("kood " +kood); 
			    	    			  	 }
			    	    				} 
			    	    			 	try{
				    	    			  	 // pricer = Double.parseDouble(row.getCell((short)10).getStringCellValue());  
				    	    			  	 }catch (Exception e){
				    	    			  		 try{
				    	    			  	 
				    	    			  		//  pricer = row.getCell((short)10).getNumericCellValue();  
				    	    			  	 }catch(Exception r){
				    	    			   
				    	    			  	 }} 
				    	    			  	 
				    	    			 	try{
					    	    			  	 // priced = Double.parseDouble(row.getCell((short)11).getStringCellValue());  
					    	    			  	 }catch (Exception e){
					    	    			  		 try{
					    	    			  	 
					    	    			  		//  priced = row.getCell((short)11).getNumericCellValue();  
					    	    			  	 }catch(Exception r){
					    	    			   
					    	    			  	 }} 
					    	    			  	 
			    	    			 	try{
				    	    			  	//  price = Double.parseDouble(row.getCell((short)8).getStringCellValue());  
				    	    			  	 }catch (Exception e){
				    	    			  		 try{
				    	    			  	 
				    	    			  		//  price = row.getCell((short)8).getNumericCellValue();  
				    	    			  	 }catch(Exception r){
				    	    			   
				    	    			  	 }} 
				    	    			  	// System.out.println("price'" + price +"'");
			    	    			 	try{
				    	    			  //	  groupname = row.getCell((short)4).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			   	//  groupname = ((int)row.getCell((short)4).getNumericCellValue())+"";  
				    	    	    		} 
			    	    			  	 
				    	    			  	 if(group != null ){
				    	    			  		 groups.put(Converter.cleanLitLatEst(group.trim()),Converter.enCodeEst( group.trim()));
				    	    			  	 }
				    	    			  		 
				    	    			  	 
		    	    			  	 
		    	    			  	try{
			    	    			  	  ean = row.getCell((short)0).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  			 ean = (row.getCell((short)0).getNumericCellValue())+"";  
			    	    			  			 if(ean != null && ean.indexOf("E") != -1){
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
			    	    			  	 // type = row.getCell((short)0).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			   	  //type = ((int)row.getCell((short)0).getNumericCellValue())+"";  
			    	    	    		} 
			    	    			   	try{
				    	    			  	//  type = row.getCell((short)0).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			   	 // type = ((int)row.getCell((short)0).getNumericCellValue())+"";  
				    	    	    		} 
			    	    			  
	    	    			  	
	    	    	    	     try{
			    	    			  name = row.getCell((short)3).getStringCellValue();  
				    	    		  }catch (Exception e){
				    	    			name = ((int)row.getCell((short)3).getNumericCellValue())+"";  
				    	    	      }  
				    	    		  
	    	    			   if(kood != null && kood.trim().length() > 0 && name != null){
	    	    				   Product prod = new Product(kood.trim());
			    	    	         name =  Converter.enCodeEst(name);
			    	    	         String aa = "";
			    	    	         if(group != null){
			    	    	        	 aa = group.trim();
			    	    	         }
			    	    	         prod.setName(aa + " " + name.trim());
			    	    	         prod.setUnit("tk.");
			    	    	         prod.setPrice(1);
			    	    	         prod.setVat("22");
			    	    	         if(type!= null && type.trim().length() > 0){
			    	    	        	 prod.setType(type.trim());
			    	    	         }
			    	    	         if(ean!= null && ean.trim().length() > 0){
			    	    	        	 prod.setEAN(ean.trim());
			    	    	         }
			    	    	         if(group!= null && group.trim().length() > 0){
			    	    	        	 prod.setGroup(Converter.cleanLitLatEst(group.trim()));
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
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
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
    return "OskarLT";
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
