package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
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

public class BSMSDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater, Discounter,PictureMaker {
  public BSMSDBDriver(EksportPanel parent){
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

  
  
  
  private void saveFile(String fileName,String content ){
	
try{
		PrintWriter os = new PrintWriter(new FileWriter( fileName , true));
		os.write(content);
		os.close(); 

    // System.out.println("Faili salvestus " + fileName); 
}catch(Exception r){
	  r.printStackTrace();
}
   
      
}
  
    

  public static void copyFile(File in, File out) throws Exception {
      FileInputStream fis  = new FileInputStream(in);
      FileOutputStream fos = new FileOutputStream(out);
      byte[] buf = new byte[1024];
      int i = 0;
      while((i=fis.read(buf))!=-1) {
        fos.write(buf, 0, i);
        }
      fis.close();
      fos.close();
      }
  
  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }


  
  
@Override
public void PictureMaker() {
//	  sendReExReport();
		  System.exit(0);
}
  

	public void loadEventProds() throws IOException{
		custnames = new TreeMap();
		File file = new File(this.getExcelDir() + "Customers.txt");
		if (file.exists()){
			
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
			getCustomers(new Agent("aa"));
	        this.getProducts(null);
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
	             String agname = wr.getInfo().getAgentName();
	             if(wr.getInfo().getAgentName() == null){
	            	 agname = wr.getInfo().getAgentID();
	             }
	             ps.setString(2,Converter.deCode( agname));
	             ps.setString(3, lw.getLine().getCustID());
	             if(custnames.get(lw.getLine().getCustID()) != null){
	            	 ps.setString(4, custnames.get(lw.getLine().getCustID())+"");
	            //	 System.out.println("1 " + custnames.get(lw.getLine().getCustID()));
	               }else{
	            	 ps.setString(4,Converter.deCode( lw.getLine().getCustName()));
	            	// System.out.println("2 " +Converter.deCode( lw.getLine().getCustName()));
	              }
	            
	             ps.setString(5, Converter.deCode(lw.getLine().getProdID()));

	             if(prodnames.get(lw.getLine().getCustID()) != null){
	            	 ps.setString(6, prodnames.get(lw.getLine().getProdID())+"");
	            //	 System.out.println("1 " + custnames.get(lw.getLine().getCustID()));
	               }else{
	            	 ps.setString(6,Converter.deCode(lw.getLine().getProdName()));
	            	// System.out.println("2 " +Converter.deCode( lw.getLine().getCustName()));
	              }
	             
	             
	             
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
	             ps.setInt(35,  lw.getLine().getResInt3()); //111 on eesti andmed
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

  private XSSFSheet getSheet (String lehenimi){
	File f = new File(this.getDir() + "/excel/MasterData.xlsx");
		
		try {
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			return workbook.getSheet(lehenimi);	
		}catch(Exception e){
			e.printStackTrace();
		}	
		return null;
  
  }
  
  
  public static Vector<MatrixLine> sortOrderLineByProdName(Vector<MatrixLine> inVec, TreeMap<?, ?> tooted){
      Vector<MatrixLine> vec = new Vector<MatrixLine>();
      for (int i = 0 ; i < inVec.size() ; i++){
        vec.addElement(inVec.elementAt(i));
      }
      
      TreeMap asi = new TreeMap();
      int o = 0;
      
      for (Enumeration<?> enumr = vec.elements(); enumr.hasMoreElements() ; ){
    	  o++;
      	MatrixLine ol = (MatrixLine)enumr.nextElement();
        String prodname = (String)tooted.get(ol.getProdID());
        if(prodname!= null){
            asi.put(prodname + o , ol);
        }
      }
      
      Vector<MatrixLine> sorted = new Vector<MatrixLine>();
      
      Iterator itera =  asi.keySet().iterator();
    
      while (itera.hasNext()){
    	 sorted.add((MatrixLine)(asi.get(itera.next())));
    	 
      }
      
     // System.out.println("sorted ready " + sorted.size());
      return sorted;
    }

  
  
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
       	 this.getProducts(null);
		try {
			XSSFSheet sheet = getSheet("FixAssortment");
			XSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

		//	 System.out.println("rownum " + row.getLastCellNum());
			for (int a = 6; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// .println(row.getCell((short)a).getNumericCellValue());
				}
			}

			// System.out.println(groups.toString());

			for (int i = 6; i < sheet.getLastRowNum(); i++) {
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
					for (int a = 6; a < row.getLastCellNum(); a++) {

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
      //System.out.println(result.toString());
		
		return sortOrderLineByProdName(result,prodnames);//);

	}


  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
          XSSFSheet sheet = getSheet("Campaigns");
 		 XSSFRow row = null;
 		
 		int max = sheet.getLastRowNum();
 		// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

 		// System.out.println("rownum " + row.getLastCellNum());
 		for (int a = 5; a < max; a++) {
 			row = sheet.getRow(a);
 			String chain = null;
 			String ean = null ;
           Date start = null ;
           Date end = null ;
           
           
           
           try{
			  	  ean = row.getCell((short)0).getStringCellValue();  
			  	 }catch (Exception e){
			  		 try{
			  			 ean = (row.getCell((short)0).getNumericCellValue())+"";  
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
				  	  start =(row.getCell((short)3).getDateCellValue());  
				  	 }catch (Exception e){
				  		//System.out.println( "bbb " + ean );
				  		 //System.out.println( "bbb " + ean + " " +  ((String)eans.get(ean.trim())));
				  		// e.printStackTrace();
				  	 }
           
				  	 try{
					  	  end = (row.getCell((short)4).getDateCellValue());  
					  	 }catch (Exception e){
					  		// System.out.println( "aaa " + ean );
					  		// System.out.println( "aaa " + ean + " " +  ((String)eans.get(ean.trim())));
					  		// e.printStackTrace();
					  	 }
					 
					 try{
						   chain = row.getCell((short)5).getStringCellValue();  
						  	 }catch (Exception e){
						  		// e.printStackTrace();
						  	 }
           
						if(start != null && end != null && chain != null && ean != null &&
							ean.trim()!= null){
							  MatrixLine mline = new MatrixLine();
		                    
	                          if (chain != null && chain.trim().length() > 0) {
                                  mline.setCliGroup(chain.trim());
                              }
	                          if (ean != null && ean.trim().length() > 0 ) {
	                                  mline.setProdID(ean.trim());
	                          }
	                           
	                          
	                          mline.setPrice(0.75);
	                          Calendar ss = Calendar.getInstance();
	                         // ss.add(Calendar.DATE, 1);
	                          Date star = new Date(ss.getTimeInMillis());
	                          
	                          mline.setStartDate(Converter.getYYYYpMMpDDpString(start));
                               mline.setEndDate(Converter.getYYYYpMMpDDpString(end)); 
                            //   System.out.println("mline S" + star + Converter.getYYYYpMMpDDpString(start) +
                            	//	  " " + start.before(star) +  " E" +
                            	 //  Converter.getYYYYpMMpDDpString(end));
    				    
                               Calendar sse = Calendar.getInstance();
 	                          sse.add(Calendar.DATE, 1);
 	                          Date ends = new Date(sse.getTimeInMillis());
                               if(end.after(ends) && start.before(star)){
                                lines.add(mline);
                             //  System.out.println("mline added "  + mline.getStringForFixedAssortment());
        				    }
                      }
						  	 
 		}
 			
 		// System.out.println("mline "  + lines.size());
		    
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
	    
	    XSSFSheet sheet = getSheet("Customers");
		 XSSFRow row = null;
		
		int max = sheet.getLastRowNum();
		// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

		// System.out.println("rownum " + row.getLastCellNum());
		for (int a = 5; a < max; a++) {
			row = sheet.getRow(a);
			String ag = null;
			String ag1 = null;
			String ag2 = null;
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				ag =  row.getCell((short) 6).getStringCellValue();
			} catch (Exception e) {
				try{
				ag =  ((int)row.getCell((short)6).getNumericCellValue()) + "";
                   }catch(Exception y){				
				}
			}
			
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				ag1 =  row.getCell((short) 7).getStringCellValue();
			} catch (Exception e) {
				try{
				ag1 =  ((int)row.getCell((short)7).getNumericCellValue()) + "";
                   }catch(Exception y){
				}
			}
			
			try {
				// System.out.println(row.getCell((short)a).getStringCellValue());
				ag2 =  row.getCell((short) 8).getStringCellValue();
			} catch (Exception e) {
				try{
				ag2 =  ((int)row.getCell((short)8).getNumericCellValue()) + "";
                   }catch(Exception y){
				}
			}
		
			//System.out.println("Lisan Duubleid " + "dub." + ag + " " + ag1 + " " + ag2);
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

   
  public TreeMap getCustomers(Agent agent){
	  custnames = new TreeMap();
	
	  TreeMap customers = new TreeMap();
	  TreeMap custMap = new TreeMap();
	   Vector payers =  new Vector();
  	
	   if(agent == null || !agent.getID().equals("aa")){
		   try{
			   delCusttable();
		 	      
		   }catch(Exception d){
			   
		   }
 		  }
	   try{
        
  		XSSFSheet sheet = getSheet("Customers");
  		XSSFRow row = null;
  		
  		row = sheet.getRow(0);
  	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
  		
  	//	System.out.println("rownum " + row.getLastCellNum());
  		
  		
  		for(int a = 4 ; a < sheet.getLastRowNum()+1;a++){
  			try{
  		
  				row = sheet.getRow(a);
  				if (row.getCell((short) 0) != null) {
  				
 			   String kood = null;
 			   String name = null;
 			   String sort = null;
 			  String payer = null;
 			 String region = null;
 			 String address = null;
 			 String city = null;
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
	    	    			  		visit = row.getCell((short)10).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		try{
		    	    			  		visit = ((int)row.getCell((short)10).getNumericCellValue())+""; 
		    	    			  		 }catch (Exception t){
		    	    			  			 
		    	    			  		 }
		    	    	    		}
		    	    			  	 
		    	    			  	try{
		    	    			  		address = row.getCell((short)3).getStringCellValue().trim() ; 
		    	    			  		
			    	    			  	 }catch (Exception e){
			    	    			  		
			    	    	    		} 
	    	    			  	 
			    	    			  	 
			    	    			  	try{
			    	    			  		city = row.getCell((short)4).getStringCellValue();  
				    	    			  	 }catch (Exception e){
				    	    			  		city = ((int)row.getCell((short)4).getNumericCellValue())+"";  
				    	    	    		} 
	    	    			  	try{
	    	    			  		chain = row.getCell((short)5).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		chain = ((int)row.getCell((short)5).getNumericCellValue())+"";  
		    	    	    		} 
		    	    			 	/*try{
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
			    	    			  	 
			    	    			  	 } */
	    	    			  	try{
	    	    			  		sort = row.getCell((short)2).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			  		 try{
		    	    			  			sort = ((int)row.getCell((short)2).getNumericCellValue())+"";   
		    	    			  		 }catch(Exception t){
		    	    			  			 
		    	    			  		 }
		    	    			  		
		    	    	    		} 
		    	    			  	 
			    	    	     try{
			    	    			  name = row.getCell((short)1).getStringCellValue();  
				    	    		  }catch (Exception e){
				    	    			name = ((int)row.getCell((short)1).getNumericCellValue())+"";  
				    	    	      }  
				    	    	
				    	    		  try{
				    	    			  ag = row.getCell((short)6).getStringCellValue();  
					    	    		  }catch (Exception e){
					    	    			ag = ((int)row.getCell((short)6).getNumericCellValue())+"";  
					    	    	      }  
		    	    			 
		    	    			       
					    	    		  try{
					    	    			  ag1 = row.getCell((short)7).getStringCellValue();  
						    	    		  }catch (Exception e){
						    	    			ag1 = ((int)row.getCell((short)7).getNumericCellValue())+"";  
						    	    	      }  
			    	    			        
						    	    		  try{
						    	    			  ag2 = row.getCell((short)8).getStringCellValue();  
							    	    		  }catch (Exception e){
							    	    			  try{
							    	    			ag2 = ((int)row.getCell((short)8).getNumericCellValue())+"";  
							    	    			  }catch (Exception w){
							    	    				  
							    	    			  } 
							    	    	      }  
							    	    		//     System.out.println(name + " agent'" + ag + "'" + ag1 + " " + ag2);  
		    	    			        Customer cust = new Customer(kood.trim());
		    	    			       if( sort != null){ 
		    	    			    	   Vector vv = new Vector();
		    	    			    	   StringTokenizer st = new StringTokenizer(sort, ",");
		    	    			    	   while (st.hasMoreTokens()){
		    	    			    		   vv.add(st.nextToken().trim());   
		    	    			    	   }
		    	    			    	   
		    	    			    	   cust.setFixSortCode(vv);
		    	    			       }
		    	    			       String aaa = "";
		    	    			       if(sort != null){
		    	    			    	   aaa = sort;
		    	    			       }
                                    //   System.out.println(Converter.enCodeEst(chain + " " + name.trim()) + " " + Converter.enCodeEst(address.trim().replaceAll("\\r\\n|\\r|\\n", " ")));
		    	    			        cust.setName(Converter.enCodeEst( name.trim()) + " " + kood + " " + Converter.enCodeEst(address.trim().replaceAll("\\r\\n|\\r|\\n", " ")));
		    	    			  	 // custnames.put(kood,name.trim() + " " + address.trim());
		    	    			        
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
		    	    			        
		    	    			        if( visit != null && visit.trim().length() > 0){
		    	    			        	cust.setVisit(Converter.getVisitDay(visit.trim()));
		    	    			        }
		    	    			        
		    	    			        if( address != null && address.trim().length() > 0){
		    	    			        	cust.setAddress(Converter.adressLink(Converter.enCodeEst((address.trim() +  ", " + city.trim()).replaceAll("\\r\\n|\\r|\\n", " ")),"Latvia"));
		    	    			        }
		    	    			        
		    	    			        
		    	    			        if(kood.trim().length() > 2){
		    	    			        
			    	    			        customers.put(cust.getName().trim(), cust);
			    	    			        custMap.put(kood.trim(), cust);	
			    	    			        
			    	    			        if(agent == null || !agent.getID().equals("aa")){
			    	    			        	addToCustomers(kood,name
			    	    			        			,ag1,chain,address,"lat",city); 
			    	    			        	//custid,custname,agent,chain,aadress,country,city
			    	    			 	   }
		    	    			        }
			  				}
 			
  			}catch (Exception e){
  			e.printStackTrace();
  			}
  		}
  
  	 }catch (Exception e){
  		e.printStackTrace();
  	}



  	//System.out.println("Customers " + customers.toString());
   

    return customers;
  }



  private void delCusttable() {
	String sql = "delete from dbo.mj_customer";
	 try {
		 	System.out.println("kustutan");
	        Connection con = getNewConnection(url2);
	        Statement ps = con.createStatement();
	        ps.executeUpdate(sql);
	        ps.close();
	        con.close();
	 }catch(Exception e ){
	//	e.printStackTrace(); 
	 }
	      
	
}

  
  

	private void  addToCustomers(String custid,String nimi,String agent,String kett,String aadress,
			String country,String city){
		
		kett = kett.replaceAll("\"", " ").trim();
		kett = kett.replaceAll("'", " ").trim();
		kett = kett.replaceAll("!", " ").trim();
		
		nimi = nimi.replaceAll("\"", " ").trim();
		nimi = nimi.replaceAll("'", " ").trim();
		nimi = nimi.replaceAll("!", " ").trim();
   
		    String sql1 =  "   insert into mj_customer (custid,custname,agent,chain,aadress,country,city) values " +
	 "  ('" + custid + "','" + nimi + "','" + agent + "','" +kett + "','" + aadress + "','" + country + "','" + city + "') ";
		  
		    Statement st1;
			try {
			
				 Connection con = getNewConnection(url2);
				st1 = con.createStatement();
				st1.executeUpdate(sql1);
				st1.close();
				con.close();
		}catch (Exception e){
		//	System.out.println(sql1);
		//	e.printStackTrace();
		}
		
		
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

      private void cleanTables(){
    	  String sql = "delete from supervisor where sentdate < getdate()-180";
    	   String sql2 = "delete from dayreport where enddate < getdate()-180";
    	   try{
    		   Connection con = getNewConnection(url2);
               Statement ps = con.createStatement();
               ps.executeUpdate(sql);
               ps.executeUpdate(sql2);
               ps.close();
               con.close();
        	      
    	   }catch(Exception e){
    		   e.printStackTrace();
    	   }
    	   
    	  
      }

  public TreeMap getProdGroups() throws SQLException {
	  cleanTables();
    return groups;
}
  
  private TreeMap groups = new TreeMap();
  private TreeMap groups2 = new TreeMap();

  public TreeMap getProdGroupsSecond() throws SQLException {
    return groups2;
  }
  
  
  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();
  private TreeMap eans = new TreeMap();
  private TreeMap prodnames = new TreeMap();
  
  
  public TreeMap getProducts(Agent agent){
	  prodnames = new TreeMap();
	   
	  getNF().setMaximumFractionDigits(2);
	   TreeMap products = new TreeMap();
		XSSFSheet sheet = getSheet("FixAssortment");
		XSSFRow row = null;
		

	//	System.out.println("rownum " + row.getLastCellNum());
		
		
		for(int a = 4 ; a < sheet.getLastRowNum()+1;a++){
			
		
				row = sheet.getRow(a);
				if (row.getCell((short) 0) != null) {
				
			   String kood = null;
			   String type = null;
			   String name = null;
			   String ean = null;
			   double price = 1;
		
			   String group = null;
			   String group2 = null;
			  
			   
	    	    			  	try{
		    	    			  	  kood = row.getCell((short)1).getStringCellValue();  
		    	    			  	 }catch (Exception e){
		    	    			   	  kood = ((int)row.getCell((short)1).getNumericCellValue())+"";  
		    	    	    		} 
		    	    			  	 
		    	    			 	try{
			    	    			  	  group = row.getCell((short)0).getStringCellValue();  
			    	    			  	 }catch (Exception e){
			    	    			  		 try{
			    	    			  			  group = ((int)row.getCell((short)0).getNumericCellValue())+"";  
						    	    	    				 
			    	    			  		 }catch(Exception d){
			    	    			  			 
			    	    			  		 }
			    	    			   	} 
			    	    			 	
					    	    			  	 
			    	    			 
				    	    			  	 
			    	    			
			    	    			  	 
				    	    			  	 if(group != null && group.trim().length() > 0){
				    	    			  		 groups.put(group.trim(),  Converter.enCodeEst(group.trim()).trim());
				    	    			  	 }
				    	    			  		 

				    	    			  	try{
					    	    			  	  group2 = row.getCell((short)3).getStringCellValue();  
					    	    			  	 }catch (Exception e){
					    	    			  		 try{
					    	    			  			  group2 = ((int)row.getCell((short)3).getNumericCellValue())+"";  
								    	    		 }catch(Exception r){
					    	    			  			 
					    	    			  		 }
					    	    			   	} 
					    	    			 	
				    	    			  	 if(group2 != null && group2.trim().length() > 0){
				    	    			  		 groups2.put(group2.trim(),  Converter.enCodeEst(group2.trim()).trim());
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
			    	    			  name = row.getCell((short)4).getStringCellValue();  
				    	    		  }catch (Exception e){
				    	    			name = ((int)row.getCell((short)4).getNumericCellValue())+"";  
				    	    	      }  
				    	    		  
	    	    			   if(kood != null && kood.trim().length() > 0 && name != null){
	    	    				   Product prod = new Product(kood.trim());
			    	    	         name =  Converter.enCodeEst(name);
			    	    	         prod.setName(name.trim() + " " + kood );
			    	    	         prod.setUnit("pc");
			    	    	         prod.setPrice(1);
			    	    	         prod.setVat("21");
			    	    	         if(type!= null && type.trim().length() > 0){
			    	    	        	 prod.setType(type.trim());
			    	    	         }
			    	    	         if(ean!= null && ean.trim().length() > 0){
			    	    	        	 prod.setEAN(ean.trim());
			    	    	        	 eans.put(ean, prod.getID());
			    	    	         }
			    	    	         if(group!= null && group.trim().length() > 0){
			    	    	        	 prod.setGroup(group.trim());
			    	    	         }
			    	    	         if(group2!= null && group2.trim().length() > 0){
			    	    	        	 prod.setGroupSecond(group2.trim());
			    	    	         }
			    	    	        	 prod.setPrice(price);
			    	    	        	 prodnames.put(kood, name);
				    	    	     products.put(name + kood, prod); 
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
    return "BSMS";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){
    return null;
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
