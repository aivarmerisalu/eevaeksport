/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002fget
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class SkaiteKLPDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter, PictureMaker{


    public SkaiteKLPDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	  
	} catch (Exception e) {
	    parent.showError(e);
	}
	user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
    public String getProductSortingData(){
       	return null;
       }
	
    
  
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
	  private Connection getNewConnection3() throws SQLException{
		    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
		  }

	  private Connection getNewConnectionScan() throws SQLException{
		    return DriverManager.getConnection(url3, "ESofficeLogin", "esoffice");
		  }
	

	  public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
			String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																		// C(50),
					+ "?," // agentname C(200),
					+ "?," // clientid C(50),
					+ "?," // clientname C(200),
					+ "?," // prodid C(50),
					+ "?," // prodname C(200),
					+ "getdate() ," // sentdate D,
					+ " '' ," // receivedTime D,
					+ "?," // normalpr N,
					+ "?," // amoun N,
					+ "?," // grouptotal N,
					+ "?," // cattotal N,
					+ "?," // pertotcat I,
					+ "?," // percolcat I,
					+ "?," // perpalcat I,
					+ "?," // pershecat I,
					+ "?," // perrescat I,
					+ "?," // isaction L,
					+ "?," // ispartner L,
					+ "?," // isbrand L,
					+ "?," // isgroup L
					+ "?," // actionaeva N,
					+ "?," // actionbeva N,
					+ "?," // actiontota N,
					+ "?," // custeval N,
					+ "?," // custcomme C(200),
					+ "?," // comment C(200),
					+ "?," // custagent C(200),
					+ "?," // custagentname C(200),
					+ "?," // mustbe I,
					+ "?," // ideal I,
					+ "?," // resstr1 C(200),
					+ "?," // resstr2 C(200),
					+ "?," // resstr3 C(200),
					+ "?," // resint1 I,
					+ "?," // resint2 I,
					+ "?)"; // resint3 I,
					//+ "?)"; // country L

			try {
				System.out.println("jamxxx");
				Connection con = getNewConnection3();
				PreparedStatement ps = con.prepareStatement(sql);
				
				int cou = 1;
	        
				for (Iterator iter = wr.getInfo().getLines().iterator(); iter
						.hasNext();) {
					cou++;
					eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
							iter.next());
					ps.setString(1, wr.getInfo().getAgentID());
					ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
					ps.setString(3, lw.getLine().getCustID());
					ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
					ps.setString(5, lw.getLine().getProdID());
					ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
					ps.setDouble(7, lw.getLine().getNormalPrice() * 100);
					ps.setDouble(8, lw.getLine().getAmount() * 100);
					ps.setDouble(9, 0);
					ps.setDouble(10, 0);
					ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
					ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
					ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
					ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
					ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
					ps.setBoolean(16, lw.getLine().isAction());
					ps.setBoolean(17, lw.getLine().isPartner());
					ps.setBoolean(18, lw.getLine().isBrand());
					ps.setBoolean(19, lw.getLine().isGroup());
					ps.setDouble(20, lw.getLine().getAction1Eval());
					ps.setDouble(21, lw.getLine().getAction2Eval());
					ps.setDouble(22, lw.getLine().getActionTotalEval());
					ps.setDouble(23, lw.getLine().getCustEval());
					String cc = lw.getLine().getCustComment();
					if (cc == null) {
						cc = " ";
					}
					ps.setString(24, Converter.deCode(cc));

					String cd = lw.getLine().getComment();
					if (cd == null) {
						cd = " ";
					}
					ps.setString(25, Converter.deCode(cd));

					String ce = lw.getLine().getCustAgentId();
					if (ce == null) {
						ce = " ";
					}

					ps.setString(26, ce);

					String cf = lw.getLine().getCustAgentName();
					if (cf == null) {
						cf = " ";
					}
					ps.setString(27, Converter.deCode(cf));
					ps.setInt(28, lw.getLine().getDealNr());
					ps.setInt(29, lw.getLine().getIdealNr());

					String cr1 = lw.getLine().getResStr1();
					if (cr1 == null) {
						cr1 = " ";
					}
					
					ps.setString(30, cr1);

					String cr2 = lw.getLine().getResStr2();
					if (cr2 == null) {
						cr2 = " ";
					}
					ps.setString(31, cr2);

					String cr3 = lw.getLine().getResStr3();
					if (cr3 == null) {
						cr3 = " ";
					}

					Calendar cal = Calendar.getInstance();
					try {
						cal.setTimeInMillis(Long.parseLong(cr3));
					} catch (Exception e) {
					//	System.out.println("cr3 '" + cr3 + "'");

						// e.printStackTrace();

					}

					ps.setString(32, getDateStr(cal));
					ps.setInt(33, lw.getLine().getResInt1());
					ps.setInt(34, lw.getLine().getResInt2());
					ps.setInt(35, lw.getLine().getResInt3());
				//	System.out.println("ps asse");
					
					String gru = "1";
					if(lw.getLine().isGroup()) {
						gru = "2";
					}
					
					int leng = lw.getLine().getCustID().length();
					String sb =  wr.getInfo().getAgentID() + ";;"
					 + lw.getLine().getCustID()+  ";" +Converter.deCode(lw.getLine().getCustName().substring(0,lw.getLine().getCustName().length() - leng)) +
					   ";"  ;	
				     sb = sb + getDateTimeStr(cal) + ";" + cr2 + ";" +  Converter.deCode(cc) + ";" +  Converter.deCode(cd); 
				     sb = sb + ";" + lw.getLine().getAction1Eval() + ";" + lw.getLine().getAction2Eval();//long,latt
				     sb = sb + ";" + gru ;//long,latt
					System.out.println("ps asse" + cr1+ " " + sb);
					
					if(cr1.equals("INVSCAN")){
						makeCSV(sb , cr2 + "-"+ cal.get(Calendar.MILLISECOND) +""+ cou);
					}else{
						ps.executeUpdate();	
					}
					
				}

				ps.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	  private void makeCSV(String sb, String scan){
		  StringBuffer sb2 = new StringBuffer();
		  
		  sb2.append("agent;customerIDAXA;customerIDSKAITA;customerName;timestamp;scanned;other;comment;longitude;lattitude\n");
		  sb2.append(sb.replaceAll("\\r\\n|\\r|\\n", " "));
		  
		  String path = "klpscan\\";
		  if(getParent().getProps().getProperty("scan.dir") != null){
			  path = getParent().getProps().getProperty("scan.dir");
		  }
				  try{
			    		 Converter.saveFileInUTF8(path + scan + ".csv", sb2);
			    	}catch (Exception e){
			    		e.printStackTrace();
			    	}  
			   
	  }
	  
	  public String getDateTimeStr(Calendar cal) {

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
			String hour = (cal.get(Calendar.HOUR_OF_DAY)) + "";
			String min = (cal.get(Calendar.MINUTE)) + "";
			String sec = (cal.get(Calendar.SECOND)) + "";
			
			if (month.length() == 1) {
				month = "0" + month;
			}
			if (day.length() == 1) {
				day = "0" + day;
			}
			if (hour.length() == 1) {
				hour = "0" + hour;
			}
			if (min.length() == 1) {
				min = "0" + min;
			}
			if (sec.length() == 1) {
				sec = "0" + sec;
			}
			
			return cal.get(Calendar.YEAR) + "-" + month + "-"+ day + " " + hour + ":" + min + ":" + sec;
		}
		public String getDateStr(Calendar cal) {

			String month = (cal.get(Calendar.MONTH) + 1) + "";
			String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
			if (month.length() == 1) {
				month = "0" + month;
			}
			if (day.length() == 1) {
				day = "0" + day;
			}
			return cal.get(Calendar.YEAR) + "" + month + day;
		}
	  


    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }
    
    public String getSaveDebtInfo() {
    	
    	 String dd = getParent().getProps().getProperty("days");
			Integer days = 1;
			if(dd != null) {
				try {
					days = Integer.parseInt(dd);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
    	  Calendar alg = Calendar.getInstance();
   	    alg.add(Calendar.DATE, -days);
   	    Converter.makeWebFilesList(alg.getTime(), null , rcompany,  user ,  pass, this.getExcelDir() + "files/", "klp");
   	    System.exit(0);
   	  
   	    return null;
   	  
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }
   
          private TreeMap<String,String> stocksTXT = null;

        public String getSaveStockInfo(){
        	  connectionUp(url);
        	  stocksTXT = new TreeMap<String,String>();
        	  String  cmd = "  SELECT     CONVERT(varchar(20), sand.Kodas) AS LADU, pr.kodas AS toode, CONVERT(Int, ps.Likutis_PP) AS UnitsInStock, CONVERT(Int,  " +
                    "  ps.Likutis_PP - ps.KiekisRezervuotas) AS stock " +
" FROM   dbo.Sandeliai AS sand INNER JOIN " +
  "                    dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
   "                   dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
 " WHERE     (sand.SandeliaiId > 1) and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or  CONVERT(Int, ps.Likutis_PP) >0)" ;


		    //   if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {
		        	 String dealer = rs.getString("LADU").trim();
		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		          
		           if(seis > 0 ){
		               products.append(kood +  ";");
		               products.append(dealer + ";");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           
		         if(dealer.equals("KAUNAS_KP") ) {
		        	 if(stocksTXT.get(kood) != null) {
		        		 String value = stocksTXT.get(kood) + "|  KAUN:" + (int)seis; 
		        		 stocksTXT.put(kood, value);
		        	 } else {
		        		 String value = "  KAUN:" + (int)seis; 
		        		 stocksTXT.put(kood, value);
		        	 }
		         }
		         
		         if(dealer.equals("VILNIUS_KP") ) {
		        	 if(stocksTXT.get(kood) != null) {
		        		 String value = stocksTXT.get(kood) + "|  VILN:" + (int)seis; 
		        		 stocksTXT.put(kood, value);
		        	 } else {
		        		 String value = "  VILN:" + (int)seis; 
		        		 stocksTXT.put(kood, value);
		        	 }
		         }
		         if(dealer.equals("PANEVEZYS_KP") ) {
		        	 if(stocksTXT.get(kood) != null) {
		        		 String value = stocksTXT.get(kood) + "|  PANEV:" + (int)seis; 
		        		 stocksTXT.put(kood, value);
		        	 } else {
		        		 String value = "  PANEV:" + (int)seis; 
		        		 stocksTXT.put(kood, value);
		        	 }
		         }
		         
		         
		         
		         }
		           rs.close();
		           con.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       
		     //  System.out.println("stocksTXT suurus " + stocksTXT.size());
		       return products.toString();
		   }
        
                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }

                                       	public Vector getLastThreeData() {
                                    		// TODO Auto-generated method stub
                                    		return null;
                                    	}
        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
                Vector lines = new Vector();
/*
                try {
               	  String cmd = " SELECT  PriceListAsId AS grupp,t.ProductCode AS toode FROM asVwPriceListProducts r  " +
               	  		" INNER JOIN asVwProduct t ON t.ProductAsId=r.ProductAsId WHERE LevelNo is not null ORDER BY ProductName";
               	System.out.println(cmd);
                  	connectionUp(url);;
              	        Statement stmt = con.createStatement();
              	        ResultSet rs = stmt.executeQuery(cmd);
              	        while(rs.next()){
                                 String custID = " ";
                                 String clGroup = " ";
                                 String prodID = " ";
                                 String prodGroup = " ";
                                 MatrixLine mline = new MatrixLine();

                                 clGroup = rs.getString("grupp");
                                 prodID = rs.getString("toode");

                                 if (custID != null && custID.trim().length() > 0) {
                                   mline.setCustID(custID.trim());
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

                                 lines.add(mline);
                               }

              	       rs.close();
              	       con.close();
                              } catch (Exception e) {
                                  getParent().showError(e);
                              }*/
                return lines;
        }

    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }
           con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
           getParent().getProps().getProperty("dsn.pass"));
      } catch (Exception e) {
	    getParent().showError(e);
      }

    }

    /**
     *
     */
    public void connectionDown(){
        try {
           con.close();
           con = null;
	} catch (Exception e) {
	    getParent().showError(e);
	}
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    	 public TreeMap getProdGroups() throws SQLException {
    	      Connection con = this.getNewConnection();
    	      TreeMap gr = new TreeMap();
    	  

     String sql = "select PrekiuGrupesID as kood ,Pavad as nimetus from PrekiuGrupes " ;

    	    Statement st = con.createStatement();
    	    ResultSet rs = st.executeQuery(sql);

    	    while (rs.next()) {
    	    	
    	    	String kood = rs.getString("kood");
    	  	    String nimetus2 =  rs.getString("nimetus");
    	  		    gr.put( kood, Converter.enCodeEst(nimetus2.trim()));	
    	  	    
    	    }
    	  
    	    rs.close();
    	    st.close();
    	    con.close();
    	  //  System.out.println("KPL tootegrupid ending ");
    	 
    	    return gr;
    	 }


    
    public TreeMap getProdGroupsSecond() throws SQLException {
    	 Connection con = this.getNewConnection();
	      TreeMap gr = new TreeMap();
	  

	    String    sql = "	SELECT     CONVERT(varchar(20), md.SkaitaMedisId) AS kood, CONVERT(varchar(50), md.Pavad) AS nimetus, CONVERT(varchar(20), " +
	                   "   CASE md2.SkaitaMedisId WHEN 1 THEN NULL ELSE md2.SkaitaMedisId END) AS ProdGroupParentAsId, CONVERT(varchar(15), md.Kodas) AS ProdGroupCode " +
	 " FROM         dbo.SkaitaMedis_List AS mdl INNER JOIN  " +
	                     " dbo.SkaitaMedis AS md ON mdl.SkaitaMedis_ListId = md.SkaitaMedis_ListId INNER JOIN " +
	                  "    dbo.SkaitaMedis AS md2 ON md.TevasId = md2.SkaitaMedisId " +
	" WHERE     (mdl.SysVardas = 'PREKES_STD') AND (md.SkaitaMedisId > 1) " ;


	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	    	String kood = rs.getString("kood");
	  	String nimetus2 =  rs.getString("nimetus");
	  		 gr.put( kood, Converter.enCodeEst(nimetus2.trim()));	
	  	
	  	}
	    rs.close();
	    st.close();
	    con.close();
	 
	    return gr;
 }
    
    private TreeMap agzone = new TreeMap();

    public  String getExcelDir(){
        String dir = getParent().getProps().getProperty("excel.dir");
        if(dir != null){
      	   if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";  
        }else{
      	    dir =  "excel/"; 
        }
        return dir;
      }
    
   private TreeMap logistics = null;
   
   
   
   private void getLogistics (){
	   File f = new File(this.getExcelDir() + "logistics.xls");
	    logistics = new TreeMap();
	    try{
	    	
	  
		FileInputStream ff = new FileInputStream(f);
		HSSFWorkbook workbook = new HSSFWorkbook((ff));
		HSSFSheet sheet = workbook.getSheetAt(0);
		
		// System.out.println("rownum " + row.getLastCellNum());
		for (int a = 4 ; a <  sheet.getLastRowNum(); a++) {
			HSSFRow row = sheet.getRow(a);
			String prodid = "";
			String boxean = "";
			double incase = 0;
			double onpallet = 0;
			double optimum = 0;
			double netto = 0;
			double brutto = 0;

			try {
                	prodid =  row.getCell((short) 0).getStringCellValue();
			} catch (Exception e) {
				try {
					prodid =  ((int)row.getCell((short) 0).getNumericCellValue()) + "";
				} catch (Exception r) {
				}
			}
			
			try {
				boxean =  row.getCell((short) 3).getStringCellValue();
		      } catch (Exception e) {
			try {
				boxean =  ((int)row.getCell((short) 3).getNumericCellValue()) + "";
			        } catch (Exception r) {
			           }
		            }
		
		
		      try {
					incase =  ((int)row.getCell((short) 11).getNumericCellValue());
			      } catch (Exception e) {
				try {
					incase =  Double.parseDouble( row.getCell((short) 11).getStringCellValue()) ;
				        } catch (Exception r) {
				           }
			            }
			      
			      
			      try {
						brutto =  (row.getCell((short) 16).getNumericCellValue());
				      } catch (Exception e) {
					try {
						brutto =  Double.parseDouble( row.getCell((short) 16).getStringCellValue()) ;
					        } catch (Exception r) {
					           }
				            }
				      
			      
				      
				      
				      try {
							netto =  ((int)row.getCell((short) 12).getNumericCellValue());
					      } catch (Exception e) {
						try {
							netto =  Double.parseDouble( row.getCell((short) 12).getStringCellValue()) ;
						        } catch (Exception r) {
						           }
					            }
					      
				      
			      

			      try {
   					onpallet =  ((int)row.getCell((short) 20).getNumericCellValue());
				      } catch (Exception e) {
					try {
						onpallet =  Double.parseDouble( row.getCell((short) 20).getStringCellValue()) ;
   				        } catch (Exception r) {
   				           }
				            }
		      
				      try {
	    					optimum =  ((int)row.getCell((short) 10).getNumericCellValue());
					      } catch (Exception e) {
						try {
							optimum =  Double.parseDouble( row.getCell((short) 10).getStringCellValue()) ;
	    				        } catch (Exception r) {
	    				           }
					            }
					      
					      Product prod = new Product(prodid);
					      if(incase > 1 ){
					    	  prod.setInCases(incase);
					      }
					      if(onpallet > 1 ){
					    	  prod.setOnPallet(onpallet);
					      }
					      if(optimum > 1 ){
					    	  prod.setOptAmountInUnits(optimum);
					      }
					      if(boxean.trim().length() > 0 ){
					    	  prod.setEAN(boxean.trim());
					      }
					      
					      if(brutto > 1 ){
					    	  prod.setBoxbrutto((int)(brutto*1000));
					      }
					      
					      if(netto> 1 ){
					    	  prod.setBoxnetto(netto);
					      }
					      
					    logistics.put(prodid, prod); 
				
				//	    System.out.println(prodid +" '"+ incase + " '" + onpallet +" '"+ boxean +" '"+optimum);
			      
		}
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		
   }
    
   
    private TreeMap agents = new TreeMap();
    
    public TreeMap getAgents() {
    
    	
    	
    	Vector manas = new Vector();
    	
    	 String	   fileName = "settings/managers.txt";
  	   File f = new File(fileName);
  	   if(f.exists()){
  	  try{
  		  BufferedReader br = new BufferedReader(new FileReader(fileName));
      String	   line = null;
    while ((line = br.readLine()) != null) {
	    	//System.out.println(line);
		StringTokenizer st = new StringTokenizer(line, ";");
		while (st.hasMoreTokens()) {
		manas.add(st.nextToken());	
		}
	    }
  	  }catch(Exception e){
  		  e.printStackTrace();
  	  }
			
  	   }
  	  		
    	
           	
        try {  
    			 connectionUp(url);
        String  cmd = "SELECT      masm.MusuAsmenysId  AS EmployeeAsId, masm.Kodas AS yksus, " +
        		" LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi,abi.MOBIL_Sand  as ladu ,MPAL_Email,Padalinys as asi FROM dbo.MusuAsmenys AS masm " + 
				" INNER JOIN pl_musuasmenys abi ON masm.MusuAsmenysId =abi.MusuAsmenysId " ;
				

        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	    
	   Connection con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
	            getParent().getProps().getProperty("dsn.pass"));
           PreparedStatement  stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim() );
              String nimi = agent.getID() + " " +  rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject(parent.make(rs.getString("ladu")));
              agent.setIsPilot(true);
              
              agent.setLanguage("LIT");
              agent.setCountry("lt");
              agent.setPassWord("0000");
String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
              
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.length() > 1){
            	  agent.setActive(true);
              }
              
            
              
           // System.out.println(agent.getName() +" " + agent.getEmail() + " " + agent.isActive());  
              
              
              if(manas.contains(agent.getID())){
            	  agents.put(nimi, agent);
                  String asi = rs.getString("asi");
                  if(agzone.get(asi) != null){

                	  Vector vv = (Vector)agzone.get(asi);
                	  vv.add(agent.getID());
                	  agzone.put(asi, vv);
                  }else{
                	  Vector vv = new Vector();
                	  vv.add(agent.getID());
                	  agzone.put(asi, vv);
                  }  
              }
            
         
	    }
	    rs.close();
	    stmt.close();
	    con.close();
	 

	} catch (Exception e) {
	    getParent().showError(e);
	}
        Agent agent = new Agent("HOUSE");
        String nimi = agent.getID() ;
        agent.setName(nimi);
        agent.setIsPilot(true);
        agents.put(nimi, agent);
        
       
        
	return agents;
    }

  
    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
       TreeMap hinnad =  getKPHinnad(); //saame baas hinnad mida kasa ei pane 
   //  this.getProducts(null);
   
     
     
String    sql = " select  '' AS custid,ltrim(str(sut.NuolaiduSutartysId)) AS clgroup ,'' AS prodid,r.PrekiuGrupesId AS prodgroup,NuolaidosProc AS discount,0 AS price ,  " +
	  "  cast(hh.DataNuo_Int as datetime) as start  , cast(hh.DataIki_Int as datetime) as endday ,  hh.DataNuo_Int as  prior ,registravimodata ,  sut.ArAsortimentas as action from  " +
	   " NuolaiduAprasymai apr INNER JOIN dbo.NuolaiduKainininkuSudetis hh on  " +
	   "  apr.NuolaiduKainininkaiId= hh.NuolaiduKainininkaiId  " +
	 "	right join dbo.NuolaiduInfoPrekiuGrupes r  on r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId  " +
	"	 INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId  " +
	 "	 where  " +
	 "	 (CONVERT (date, GETDATE()) between   cast(hh.DataNuo_Int as datetime) and  cast(hh.DataIki_Int as datetime) or (hh.DataNuo_Int = 0 and hh.DataIki_Int = 999999)) " +
	 
    "	union  " +
     "	   select '' AS custid,ltrim(str(sut.NuolaiduSutartysId)) AS clgroup ,r.prekesID AS prodid,'' AS prodgroup,NuolaidosProc AS discount,KainaBePVM AS price ,cast(r.DataNuo_Int as datetime) as start " +   
     "	   , cast(r.DataIki_Int as datetime) as endday ,   sut.DataNuo_Int as prior , cast(sut.DataNuo_Int as datetime), sut.ArAsortimentas as action from   dbo.NuolaiduInfoPrekes r   " +
     "	   INNER JOIN  dbo.NuolaiduAprasymai  AS apr ON r.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId   " +
    "	  INNER JOIN  dbo.NuolaiduSutartys  AS sut ON r.NuolaiduAprasymaiId = sut.NuolaiduAprasymaiId and  (r.NuolaiduSutartysId = sut.NuolaiduSutartysId or  r.NuolaiduSutartysId = 1 )    " +
    "	  and (CONVERT (date, GETDATE()) between   cast(r.DataNuo_Int as datetime) and  cast(r.DataIki_Int as datetime) or (r.DataNuo_Int = 0 and r.DataIki_Int = 999999)) " +
     "   union " +
	" select  '' AS custid,'K'+ ltrim(str(sud.NuolaiduKainininkaiId)) AS clgroup ,t.prekesID AS prodid,'' AS prodgroup,0 AS discount,t.Kaina AS price ,cast(sud.DataNuo_Int as datetime) as start " +
	" , cast(sud.DataIki_Int as datetime) as endday ,   sud.DataNuo_Int as prior , cast(sud.DataNuo_Int as datetime) , 1 as action" +
	" from KainosPrekiuKategorijoms as t INNER JOIN PrekiuKainuKategorijos as abi on t.PrekiuKainuKategorijosId= abi.PrekiuKainuKategorijosId " +
	"	 INNER JOIN NuolaiduKainininkuSudetis sud on sud.PrekiuKainuKategorijosId= abi.PrekiuKainuKategorijosId " +
	"	 where kaina > 0 and " +
	"	 (CONVERT (date, GETDATE()) between   cast(sud.DataNuo_Int as datetime) and  cast(sud.DataIki_Int as datetime) or (sud.DataNuo_Int = 0 and sud.DataIki_Int = 99999)) " +
	"   order by 10 desc,2,3,7 " ; //oli 8 järgi sorditud



           try {
             
        	   
             Connection con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
                     getParent().getProps().getProperty("dsn.pass"));
             Statement  st = con.createStatement();
             ResultSet  rs = st.executeQuery(sql);

             while (rs.next()) {

               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid").trim();
               String idd = prodID;
               
               if(prcodes.get(prodID) != null){
            	   prodID = prcodes.get(prodID) + "";
               }
               String prodGroup = rs.getString("prodgroup");
               
               String discount = rs.getString("discount");
               
               double price = rs.getDouble("price");
               String prt = rs.getString("prior");
               int action = rs.getInt("action");
               MatrixLine mline = new MatrixLine();

               mline.setCliGroup(clGroup.trim());
               mline.setProdLevel(prt);
            
            
               if (prodID != null && prodID.trim().length() > 1) {
                 mline.setProdID(prodID.trim());
               }
               if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                 mline.setProdGroup(prodGroup.trim());
                // System.out.println("asi " + prodGroup.trim());
               }
               if(discount.trim().length()==0){
               	discount = "0";
               }
                 mline.setDiscount(Double.parseDouble(discount));

               if (Double.parseDouble(discount) == 0 && price != 0) {
                 mline.setPrice(price);
               }
               
               if(action == 0) {
            	 //  System.out.println("ACTION " + mline.toString());
            	   mline.setProdLevel("99999");  
               }
               
               boolean goes = true ;
               if(clGroup != null && clGroup.trim().startsWith("K")){
            	   if(hinnad.get(idd) != null ){
            		   if(Double.parseDouble(hinnad.get(idd)+"") == price){
            			  goes = false; 
            		   }
            	   }
               }
               
               if(goes){
                   lines.add(mline);   
               }
               
             }

             rs.close();
             st.close();
             con.close();
     
             
         /*    if(baashk != null && baashk.size() > 0){
            	
             for(int r = 0; r<baashk.size();r++){
            	 
         String asi = (baashk.elementAt(r) + "");
       		con = this.getNewConnection();
       		  CallableStatement  msProcedure = con.prepareCall( "uspv_Kainininkas '" + 
       				  asi.substring(1) + "', 'eur' ,0" );
       		    ResultSet rs3 = msProcedure.executeQuery();
       	      while(rs3.next()){
       	    	  String toode = rs3.getString("prekesid");
       	    	  String hind = rs3.getString("kaina");
       	    //  	System.out.println( "Toode " +  toode + " hind " + hind);
       	      	MatrixLine mm = new MatrixLine();
       	      	mm.setProdID(toode);
       	      	mm.setCliGroup(asi);
       	      	if(hind != null){
       	     	mm.setPrice(Double.parseDouble(hind));
         	    }
       	      	mm.setProdLevel("0");
       	      }
       	       rs3.close();
       	       msProcedure.close();
       	    con.close();
       	  
             }
            	 
               	 
              
             }*/
            	   
           } catch (Exception ex) {
             ex.printStackTrace();
           }
          // System.out.println("Maatrix suurus " + lines.size());
         return lines;
    	
       }
private Vector baashk = null;
      
      public TreeMap getCustomers(Agent agent){
    	  
    	  try {
			debts = getDebt2();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
    	  
        String cmd = "SELECT CONVERT(Int, part.PartneriaiId) AS CustomerASId, CONVERT(varchar(20), part.Kodas) AS kood,"
        		+ " adr.Adresas as tanav,adr.Miestas as linn, '' as regioon,adr.Indeksas as indeks ,CONVERT(varchar(40), part.Pavad) AS nimi,   " +
                   "   part.regnr AS payer ,  rtrim(adr.Adresas)  + case when adr.Indeksas IS NULL then '' else ' ' + rtrim(adr.Indeksas)end + ' ' + " +
                   " case when adr.Miestas IS NULL then '' else ' ' + rtrim(adr.Miestas)end AS Address,part.pvmnr as vatno, " +
                   "    CONVERT(char(1), 'N') AS Blocked, CONVERT(char(1), 'Y') AS Valid, CONVERT(Int, ROUND(part.Kreditas, 0)) AS CreditLimit, CONVERT(SmallInt, msal.Terminas)  " +
                   "    AS paym, pl.Tipas1 as typex, CONVERT(nvarchar(30), adr.Miestas) AS City, CONVERT(nvarchar(40), LTRIM(ats.Vardas + ' ' + ats.Pavarde)) AS ContactName, CONVERT(nvarchar(30), ats.Pareigos) AS ContactTitle, " +  
                   "    CONVERT(varchar(50), adr.Tel1) AS CustPhone , " +
                   "   CONVERT(Money, ISNULL  ((SELECT     SUM(ROUND((op.Suma - op.ApmoketaSuma) * op.Kursas, 2) * (CASE pot.PartOperacijosTipas WHEN 1 THEN 1 ELSE - 1 END)) AS Expr1   " +
                   "            FROM         dbo.Operacijos AS op INNER JOIN  " +
                   "                                  dbo.PirkimuOpTipai AS pot ON op.OperacijosTipoId = pot.OperacijosTipoId INNER JOIN  " +
                   "                                  dbo.OperacijosPartneriai AS opp ON op.OperacijosId = opp.OperacijosId  " +
                   "            WHERE     (pot.PartOperacijosTipas IN (1, 3, 12)) AND (opp.spec = 0) and op.Data+CONVERT(SmallInt, msal.Terminas) < getdate() AND (opp.PartneriaiId = part.PartneriaiId) AND " + 
				"		 	  (op.Suma > op.ApmoketaSuma)), 0)) as debt  ,masm.kodas as agent	, pl.padalinys as agent2, " +
				"			  (select rtrim( oo.Pavad) + ' ' +  rtrim(uu.Adresas)  + case when uu.Indeksas IS NULL then '' else ' ' + rtrim(uu.Indeksas)end + ' ' + rtrim(uu.Miestas) from Adresai uu " +
				"INNER JOIN Partneriai oo  ON oo.AdresoId = uu.AdresoId  where oo.PartneriaiId=partpa.SumokaPartneriaiID  ) as jurid  , apr.Pavad  as aletext , sut.NuolaiduAprasymaiId  as hinnak , kain.NuolaiduKainininkaiId  as hinnak2 " +
            " FROM         dbo.Partneriai AS part INNER JOIN  " +
            "                     dbo.PartneriaiPa AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN  " +
             "               dbo.MokejimoSalygos AS msal ON partpa.MokejimoSalygosID = msal.MokejimoSalygosID INNER JOIN " +  
              "      dbo.MokejimoBudai AS mbud ON partpa.MokejimoBudaiID = mbud.MokejimoBudaiID INNER JOIN  " +
               "     dbo.PartneriuKategorijos AS pkat ON partpa.PartneriuKategorijosId = pkat.PartneriuKategorijosId INNER JOIN  " +
                "     dbo.NuolaiduKainininkai AS kain ON pkat.NuolaiduKainininkaiId = kain.NuolaiduKainininkaiId INNER JOIN  " +
		 		 "     dbo.Mokesciai AS mok ON partpa.MokesciaiId = mok.MokesciaiId INNER JOIN " + 
                  "     dbo.Adresai AS adr ON part.AdresoId = adr.AdresoId INNER JOIN  " +
                  "     dbo.Atstovai AS ats ON part.AtstovaiId = ats.AtstovaiId INNER JOIN  " +
                  "     dbo.PL_PARTNERIAI AS pl ON part.PartneriaiId = pl.PARTNERIAIID INNER JOIN          " +           
                  "     dbo.MusuAsmenys AS masm ON part.MusuAsmenysId1 = masm.MusuAsmenysId INNER JOIN  " +
                  "     dbo.PL_MUSUASMENYS AS plmasm ON masm.MusuAsmenysId = plmasm.MUSUASMENYSID LEFT JOIN " + 
                  "     dbo.NuolaiduSutartys AS sut ON part.PartneriaiId = sut.PartneriaiId and " +
				  "     (getdate() between   cast(sut.DataNuo_Int as datetime) and  cast(sut.DataIki_Int as datetime) or (sut.DataNuo_Int = 0 and sut.DataIki_Int = 999999)) LEFT OUTER JOIN " +
				  "     dbo.NuolaiduAprasymai AS apr ON sut.NuolaiduAprasymaiId = apr.NuolaiduAprasymaiId LEFT OUTER JOIN " +
                  "     dbo.Sandeliai AS sand ON plmasm.Mobil_Sand = sand.SandeliaiId LEFT OUTER JOIN  " +
                  "     dbo.PL_MUSUASMENYS AS plmasm2 ON part.MusuAsmenysId2 = plmasm2.MUSUASMENYSID CROSS JOIN  " +
                  "     dbo.FirmosAtributai AS fa  " +
     " WHERE      pl.padalinys is not null ";
	
        log(cmd);

        
        TreeMap<String, String> scans =  getScannedLastDate();
        
        String lepingud = "select kl.PartneriaiId as klient , kl.NuolaiduSutartysId  as leping from dbo.NuolaiduSutarciuPartneriai kl inner join  " +
           " dbo.NuolaiduSutartys AS sut ON kl.NuolaiduSutartysId = sut.NuolaiduSutartysId and " +
			"	   (CONVERT (date, GETDATE()) between   cast(sut.DataNuo_Int as datetime) and  cast(sut.DataIki_Int as datetime) or (sut.DataNuo_Int = 0 and sut.DataIki_Int = 999999)) order by DataNuo_Int desc";
        HashMap lepMap = new HashMap();
        HashMap baasMap = new HashMap();
	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        baashk = new Vector();
	try {
		
		Connection con2 = this.getNewConnection();
        PreparedStatement  stmt = con2.prepareStatement(lepingud);
        ResultSet	      rs = stmt.executeQuery();

	   while (rs.next()) {
		 String klient = (rs.getString("klient").trim()); 
		 String leping = (rs.getString("leping").trim());
		 if(lepMap.get(klient) != null){
			 lepMap.put(klient, lepMap.get(klient) +"|"+leping); 
		 }else{
			 lepMap.put(klient, leping); 
		 }
		
	   }
		 rs.close();
		 stmt.close();
		
		 
		 String sqlb = "SELECT Partneriai.PartneriaiId as klient,(SELECT  Nuolaidukainininkusudetis.PrekiuKainuKategorijosId AS hk FROM " +
   "{oj{oj{oj  dbo.NuolaiduKainininkuSudetis Nuolaidukainininkusudetis    LEFT OUTER JOIN dbo.NuolaiduKainininkai Nuolaidukainininkai " +
   "  ON  Nuolaidukainininkusudetis.NuolaiduKainininkaiId = Nuolaidukainininkai.NuolaiduKainininkaiId} " +
   " LEFT OUTER JOIN dbo.PrekiuKainuKategorijos Prekiukainukategorijos  " +
   "ON  Nuolaidukainininkusudetis.PrekiuKainuKategorijosId = Prekiukainukategorijos.PrekiuKainuKategorijosId}  " +
   " LEFT OUTER JOIN dbo.PartneriuKategorijos Partneriukategorijos " +
   "ON  Nuolaidukainininkusudetis.NuolaiduKainininkaiId = Partneriukategorijos.NuolaiduKainininkaiId} " +
" WHERE  Prekiukainukategorijos.Neaktyvi <> ( 1 ) " +
 "  AND  Nuolaidukainininkusudetis.DataIki_Int = ( 999999 ) and Nuolaidukainininkusudetis.PrekiuKainuKategorijosId  != 93776 and Partneriukategorijos.PartneriuKategorijosId = Pa.PartneriuKategorijosId )" +
 " AS hk FROM   {oj{oj  dbo.Partneriai Partneriai    LEFT OUTER JOIN dbo.PartneriaiPa Pa " +
 "    ON  Partneriai.PartneriaiId = pa.PartneriaiId} " +
 "   LEFT OUTER JOIN dbo.PL_PARTNERIAI Pl_partneriai " +
 "  ON  Partneriai.PartneriaiId = Pl_partneriai.PARTNERIAIID} " +
" WHERE   Pa.PartneriuKategorijosId !=1 order by Partneriai.PartneriaiId";
		 //baas hinnakirjade leidmine
		
		 
		 
		 stmt = con2.prepareStatement(sqlb);
	             rs = stmt.executeQuery();

		   while (rs.next()) {
			 String klient = (rs.getString("klient").trim()); 
			 String hk = rs.getString("hk");
			 if(hk != null && hk.trim().length() > 0)
				 baasMap.put(klient, "B" + hk.trim()); 
			 if(baashk != null && hk != null && !baashk.contains("B" + hk.trim())){
				 baashk.add("B" + hk.trim());
			 }}
			 rs.close();
			 stmt.close();
			
		 
		 
		 
		 
          stmt = con2.prepareStatement(cmd);
       	      rs = stmt.executeQuery();

	   while (rs.next()) {
		   
		   
		   String id = rs.getString("CustomerASId").trim();
		   Customer cust = new Customer(rs.getString("kood").trim());
		   if(scans.get(id) != null) {
			   cust.setCity("Paskutinis skenavimas: " + scans.get(id));
		   }
		   
		   String tanav = rs.getString("tanav");
           if (tanav != null && tanav.length() > 0){
             cust.restStreet = tanav.trim();
           }
           String linn = rs.getString("linn");
           if (linn != null && linn.length() > 0){
             cust.restCity = linn.trim();
           }
           String regioon = rs.getString("regioon");
           if (regioon != null && regioon.length() > 0){
           	cust.restCity = (cust.restCity + " " + regioon.trim()).trim();
           }

           String indeks = rs.getString("indeks");
           if (indeks != null && indeks.length() > 0){
             cust.restZipCode = indeks.trim();
           }

           cust.setCountry("Lithuania");

		   
		   
		   String nim = rs.getString("nimi").replaceAll("\""," ").trim();
		   cust.restName = nim;
		 String nimi = Converter.enCodeEst(nim);
		 String payer = Converter.enCodeEst(rs.getString("payer").replaceAll("\""," ").trim());
		 String addr =  Converter.enCodeEst(rs.getString("Address").replaceAll("\""," ").trim());
		 String vatno = rs.getString("vatno");
		 if(vatno != null){
			vatno = Converter.enCodeEst(vatno.replaceAll("\""," ").trim()); 
			 cust.setVatnr(vatno);
		 }
		 
		 
		  String term = rs.getString("paym");
                cust.setPayer(payer);
                cust.setRegnr(payer);
                
                String type = rs.getString("typex");
                
                if(payer.length() > 0 && !payer.equals("ND") && oDue.contains(payer) && type != null && type.trim().equals("SM")) {
                	cust.setSalesBlockT(true);
                }
                if(oDue.contains(cust.getID()) && type != null && type.trim().equals("SM")) {
                	cust.setSalesBlockT(true);
                }
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                cust.setName(nimi + " " + addr);
                try{
                	String aadr = Converter.enCodeEst(addr.trim());
                	aadr = Converter.adressLink(aadr, "Lithuania");
                	 cust.setAddress(aadr);
                }catch(Exception aa){
                	
                }
               
                
                
               /* String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }*/
              
                cust.setPaymTerm(parent.make(term));
              
                double debti = rs.getDouble("debt");
               
                if(debti > 0.1 || debti< -0.1) {
                	cust.setDebt(debti);
                }
                	
                //cust.setVisit(rs.getString("visit"));
                
                //String info = rs.getString("info");
               // cust.setPersonalData(enCode(parent.make(info)));
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                  cust.setMainAgent(agen.trim());
                }
                boolean goes = true;
                
                String agen2 = rs.getString("agent2");
              
                if(agen2 != null){
                	 
                	if(agzone.get(agen2.trim()) != null){
                		Vector vv = (Vector)agzone.get(agen2.trim());
                		for(int r = 0 ;r < vv.size() ; r++){
                			cust.addAgent(new Agent((String)vv.elementAt(r)));
                            cust.setMainAgent((String)vv.elementAt(r));
                		}
                	}
                	cust.addAgent(new Agent("HOUSE"));
                }
              
                
                //cust.addAgent(new Agent("HOUSE"));
               /* String group = rs.getString("grupp");
                if(group != null){
                  cust.setGroup(group.trim());
                }
                String tyyp = rs.getString("tyyp");
                int aktive = rs.getInt("aktiivne");
               
                if(tyyp != null && tyyp.trim().length() >0 && tyyp.trim().equals("NENAUDOJAMAS")){
                   goes = false;
                }
                if(aktive == 1){
                	goes = false;
                }*/
                String aadrjur = rs.getString("jurid");
                if(aadrjur != null && aadrjur.trim().length()>0){
                  cust.setJuridics(Converter.enCodeEst(aadrjur.trim()));
                }
                
                String aletext = rs.getString("aletext");
                if(aletext != null && aletext.trim().length()>0){
                  cust.setZipcode(Converter.enCodeEst(aletext.trim()));
                }
                String hinnak = rs.getString("hinnak");
                if(hinnak != null && hinnak.trim().length()>0){
                  cust.setGroup(hinnak.trim());
                }
                
                String hinnak2 = rs.getString("hinnak2");
                if(hinnak2 != null && !hinnak2.trim().equals("1")){
                	if(cust.getGroup().trim().length() > 0 ){
                		 cust.setGroup(cust.getGroup() + "|K" + hinnak2.trim());
                	}else{
                		 cust.setGroup("K" + hinnak2.trim());
                	}
                 
                }
                 if(lepMap.get(id) != null){
                	 if(cust.getGroup().trim().length() > 0 ){
                		 cust.setGroup(cust.getGroup() + "|" +lepMap.get(id));
                	 }else{
                		 cust.setGroup("" +lepMap.get(id)); 
                	 }
                 }
                 
                 if(baasMap.get(id) != null){
                	 if(cust.getGroup().trim().length() > 0 ){
                		 cust.setGroup(cust.getGroup() + "|" +baasMap.get(id));
                	 }else{
                		 cust.setGroup("" +baasMap.get(id)); 
                	 }
                 }
                
                if(goes){
                	 customers.put(Converter.cleanLitLatEst(nim+addr), cust);
                     custMap.put(cust.getID(), cust);	
                }
               
	    }
            rs.close();
            stmt.close();
            
            
           Customer cc = new Customer("999999");
           cc.setName("x Pakrovos užsakymas sandėliui");
           Iterator itera = agents.values().iterator();
           while(itera.hasNext()) {
        	   cc.addAgent((Agent) itera.next());
           }
           custMap.put(cc.getID(), cc);
           customers.put("x Pakrovos užsakymas sandėliui", cc);
            


		      String sql3 = "SELECT op.Doknr,  part.Kodas as klient ,  OrderAsID = CONVERT(varchar(20), op.OperacijosId),   ProductAsId = CONVERT(Int, pr.PrekesId), prodid = CONVERT(varchar(35),   \r\n" + 
		      		"       pr.Kodas), Price = CONVERT(Money, pk.ppKainaBePVM / pk.Kiekis, 4), amount = CONVERT(Int, pk.Kiekis),     op.Data as kuupaev   \r\n" + 
		      		"  FROM         Operacijos AS op INNER JOIN   \r\n" + 
		      		"  Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN   \r\n" + 
		      		"  Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN   \r\n" + 
		      		"                      PardavimuKunai AS pk ON op.OperacijosId = pk.OperacijosId INNER JOIN   \r\n" + 
		      		"                      OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN   \r\n" + 
		      		"                     Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN   \r\n" + 
		      		"                    Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId   \r\n" + 
		      		"                WHERE   op.OperacijosTipoId = 53 and op.Data> getdate()-5 and pk.Kiekis != 0 order by part.Kodas,op.Data,op.Doknr ";

		      stmt=  con2.prepareStatement(sql3);
		      rs = stmt.executeQuery();

		StringBuffer buf = new StringBuffer();
		while(rs.next()){
			String ordnum = rs.getString("Doknr");
			if(ordnum.trim().length() > 7 && !ordnum.startsWith("P")) {
				int ll = ordnum.trim().length();
				ordnum = ordnum.substring(ll-7,ll);
			}
		 String id = rs.getString("klient").trim();
		 Customer cust = (Customer)custMap.get(id);
		 if (cust != null){
			 
		 	String pr = rs.getString("prodid");
		 	double hind = rs.getDouble("price");
		 	double am =  rs.getDouble("amount");
		
		 	String hi = hind+"";
		 	int len  = (hi.substring(hi.indexOf("."),hi.length())).length();
		 	if(len > 2){
		 		hi = hi.substring(0,hi.indexOf(".")+3);
		 	} else {
		 		hi = hi.substring(0,hi.indexOf(".")+2);
		 	}

		 	if(hi.endsWith("E")) {
		 		hi = hi.substring(0,hi.length()-1);
		 	}
		 	
		 	String kuupaev = rs.getString("kuupaev").substring(0,10);
		 	buf.append(id + ";" + pr + ";" + ((int)am) + ";" + hi+ ";" + ordnum +" "+ kuupaev + "\n");
		 	
		 }
		}
		 stmt.close();
         con2.close();
         
		if(buf.length() > 2){
		    File f = new File("offer.txt");
		    if (f.exists())f.delete();
		      try {
				PrintWriter os = new PrintWriter(new FileWriter(
						 "offer.txt", true));
				os.write(buf.toString());
				os.close();
			  }catch (IOException ex) {
				ex.printStackTrace();
			  }
		 }   
         
        
	} catch (Exception e) {
            getParent().showError(e);
        }

	return customers;
    }

    public DealerProduct getDealerProductsList() {


        DealerProduct dealerProds = new DealerProduct();
        try {


        	  String  sql = " SELECT     CONVERT(varchar(20), sand.SandeliaiId) AS ladu, pr.kodas AS toode, CONVERT(Int, ps.Likutis_PP) AS UnitsInStock, CONVERT(Int, " + 
            "   ps.Likutis_PP - ps.KiekisRezervuotas) AS stock ,' '  as real " +
 " FROM   dbo.Sandeliai AS sand INNER JOIN " +
   "                 dbo.PrekesSandeliuose AS ps ON sand.SandeliaiId = ps.SandeliaiId INNER JOIN " +
   "                dbo.Prekes AS pr ON ps.PrekesId = pr.PrekesID " +
" WHERE     (sand.SandeliaiId > 1)  and  (CONVERT(Int, ps.Likutis_PP - ps.KiekisRezervuotas) >0 or  CONVERT(Int, ps.Likutis_PP) >0)" ;

             if (isDebug()) System.out.println(sql);
              Connection con = this.getNewConnection();
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                	  String ladu  =  rst.getString("ladu").trim();
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String real  =  rst.getString("real");
                    if(real != null){
                    	real =  real.trim().replaceAll(";", ",");
                    }else{
                    	real = " ";
                    }
                   
                    dealerProds.add(ladu, toode,seis+"",real," " );                }
                rst.close();
                stm.close();
                con.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

         return dealerProds;
        
       }
    
  
    public TreeMap getDealers() {
        return dealers;
      }
    
    Vector debts = null;
    Vector oDue = null;
    public Vector getDebt(String agentCode) throws SQLException{
    	
    	return debts;
    }
    
    public Vector getDebt2() throws SQLException{
      Vector list = new Vector();
       oDue = new Vector();
       TreeMap<String,Double> ylekl = new TreeMap<String,Double>();
       
      
       
       
      String sql = " select  CustomerCode AS klient,Payer as payer, billcode AS number,billissuedate+paymenttime AS kuupaev, " + 
		" (case when OperacijosTipoId =64 then -1 else 1 end)*billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime ,getdate())as yle from  " +
	 " (  SELECT     OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
         "             (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0),  " +
          "            BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data, BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma),  " +
          "            PaidAmount = CONVERT(Money, op.ApmoketaSuma), CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), " +
          "             Employeecode = CONVERT(varchar(35), masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), " + 
          "            op.Valiuta), DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
          "            CustomerCode = part.Kodas, Payer = part.regnr, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
          "       FROM         Operacijos AS op INNER JOIN " +
          "            Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
          "            Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
          "            PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
          "            MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
          "            Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
          
          "            MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
          "            OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId " +
         " WHERE     0 = 1 AND op.Kilme = 35 " +
 " UNION ALL " +
" SELECT     OrderAsID = CONVERT(varchar(20) ,CASE WHEN op.OperacijosTipoId = 50 THEN op2.KilmeInfo ELSE NULL END), DeliveredOrderAsID = CONVERT(varchar(20),  " +
  "                    op.OperacijosId), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0), BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data,  " +
   "                   BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma), PaidAmount = CONVERT(Money, op.ApmoketaSuma),  " +
    "                  CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), Employeecode = CONVERT(varchar(35),  " +
     "                 masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta),  " +
      "                DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
       "               customercode = part.Kodas, Payer = part.regnr, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
"FROM         Operacijos AS op INNER JOIN " +
 "                     Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
  "                    Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
   "                   PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
    "                  MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
     "                 Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
      "                MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
       "               OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId INNER JOIN " +
        "              RysioOperacijos AS rop ON op.OperacijosId = rop.OperacijosId INNER JOIN " +
         "             Operacijos AS op2 ON rop.OperacijosId2 = op2.OperacijosId " +
" WHERE     op.OperacijosTipoId IN (50, 64) AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 ) " +
" d  where billamount-paidamount>0 and " +
 "         billissuedate > getdate()-600  order by CustomerCode,billissuedate+paymenttime";

      Connection con = this.getNewConnection();
    PreparedStatement  stmt = con.prepareStatement(sql);
   int lisa = 5;
   if(getParent().getProps().getProperty("debt.extradays") != null) {
	   lisa = Integer.parseInt(getParent().getProps().getProperty("debt.extradays").trim());
   }

      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
    	  try{
    		    Debt debt = new Debt();
    	        debt.setCustID(rs.getString("klient").trim());
    	        String payer = rs.getString("payer").trim();
    	        //debt.setPayerID(rs.getString("payer").trim());
    	        
    	        String dok = rs.getString("number");
    	        if(dok != null){
    	        	 dok = dok.trim();
    	    	        if(dok.length() >5){
    	    	        	dok = dok.substring(4);
    	    	        }
    	    	        debt.setDoknr(dok);   	    	        
    	    	        debt.setDuedate(rs.getDate("kuupaev"));
    	    	        debt.setSum(rs.getDouble("summa")); 
    	    	        int duu = rs.getInt("yle");
    	    	        debt.setDue(duu+""); 	
    	    	        if((duu - lisa) >= 0 && payer.length() > 0) {
    	    	        	if(payer.equals("ND")) {
    	    	        		payer = debt.getCustID();
    	    	        	}
    	    	        	   if(ylekl.get(payer) != null) {
    	    	    	        	ylekl.put(payer,ylekl.get(payer) + debt.getSum());
    	    	    	        } else {
    	    	    	        	ylekl.put(payer, debt.getSum());
    	    	    	        }
    	    	    	          
    	    	        }
    	    	     	  if(debt.getSum() < - 0.1 || debt.getSum() > 0.1)  {
    	    	     		  list.addElement(debt);	 
    	    	     	  }
    	    	      
    	        }
    	       
    	  }catch (Exception e){
    		  e.printStackTrace();
    	  }
    
        }
      rs.close();
      stmt.close();
      con.close();
      
      
   Iterator itera =  ylekl.keySet().iterator();
   while (itera.hasNext()) {
	   String payer = itera.next()+"";
	   if(ylekl.get(payer) > 0.1 && payer.length() > 0) {
		   oDue.add(payer);
	   }
	   
   }
      
    // System.out.println("debtlist size " + list.size());
      return list;
    }
    
    
    
    

	
    
    
    
    private TreeMap getKPHinnad(){
  	  TreeMap res = new TreeMap();
  	  String  hk = "79001";
  	  
  	 
  	  
  	  try{
  		  
  		  String baas = "(select PrekiuKainuKategorijosID as hk from NuolaiduKainininkuSudetis " +
  		  		"where nuolaidukainininkaiid= 1 and DataIki_Int = 999999)";
  		  
  		  Connection con = this.getNewConnection();
  	    PreparedStatement  stmt = con.prepareStatement(baas);


  	      ResultSet rs = stmt.executeQuery();

  	      while(rs.next()){
  	    	hk = rs.getInt("hk") + "";  
  	      }
  	      rs.close();
  	      stmt.close();
  	      con.close();
  		  
  		  String sql = "uspv_Kainininkas '" + hk + "', 'eur' ,0";
  		  con = getNewConnection();
  		  CallableStatement  msProcedure = con.prepareCall( sql );
  		     
  			
  	       //  System.exit(0);
  	         //stmt2.setTimestamp(2, new java.sql.Timestamp(order.getShipCal().getTimeInMillis()));
  	      ResultSet rs3 = msProcedure.executeQuery();
  	      while(rs3.next()){
  	    	  String toode = rs3.getString("prekesid");
  	    	  String hind = rs3.getString("kaina");
  	    //  	System.out.println( "Toode " +  toode + " hind " + hind);
  	      	res.put(toode, hind);
  	      }
  	       rs3.close();
  	       msProcedure.close();
  	       con.close();
  	  }catch (Exception e){
  		 e.printStackTrace();
  	  }
  	  
  	  return res;
    }

    private TreeMap prcodes = new TreeMap();
    
    
    public TreeMap getProducts(Agent agent){
    	
	TreeMap products = new TreeMap();
	TreeMap hinnad = getKPHinnad();
	prcodes = new TreeMap();
	this.getLogistics();
	this.getSaveStockInfo();
	
	TreeMap inde = new TreeMap();
	 
    String cmd = "SELECT  Pr.PrekesID as tid, Pr.Kodas AS kood, Pr.Pavad AS nimetus, CONVERT(Money, Pr.PK_PardKaina) AS hind, CASE WHEN pr.mvid = pr.ipokavimomvid OR " +
     "                ISNULL(pr.ipokavimomvid, 0) < 2 OR " +
      "               pr.MVId NOT IN (2, 62733) THEN mv.Kodas ELSE ipokmv.Kodas END AS unit , pr.MV_Pastaba  AS kastis,  " +
     " CONVERT(Money, NULL) AS CostPrice, CONVERT(varchar(20), Pr.KAPKodas) AS ean,   " +
     "                CONVERT(varchar(20),  pr.PrekiuGrupesID) AS grupp, md.SkaitaMedisId AS grupp2, CONVERT(char(1), 'Y') AS Valid,	pl.Cert_Seal as lice " +
" FROM         dbo.Prekes AS Pr INNER JOIN Pl_prekes pl on Pr.PrekesID = Pl.PrekesID   LEFT OUTER JOIN " +
 "                    dbo.MVGSantykiai AS mvs ON Pr.MVId = mvs.MVId1 AND Pr.IpokavimoMVId = mvs.MVId2 AND Pr.MVGId = mvs.MVGId INNER JOIN " +
  "                   dbo.Mokesciai AS mok ON Pr.MokesciaiId = mok.MokesciaiId INNER JOIN " +
   "                  dbo.PrekiuTipai AS pt ON Pr.PrekiuTipaiId = pt.PrekiuTipaiId INNER JOIN " + 
    "                 dbo.MV ON Pr.MVId = dbo.MV.MVid INNER JOIN " +
     "                dbo.MV AS IpokMV ON Pr.IpokavimoMVId = IpokMV.MVid LEFT OUTER JOIN " +
      "               dbo.SkaitaMedis_Prekes AS mdp ON Pr.PrekesID = mdp.PrekesID LEFT OUTER JOIN " +
       "              dbo.SkaitaMedis AS md ON mdp.SkaitaMedisId = md.SkaitaMedisId " +
      " WHERE     (Pr.PrekesID > 1) AND (pt.ArSandeliuojama = 1) and Pr.Nenaudoti <> ( 1 ) and md.SkaitaMedis_ListId IN (2)" ;

    //Prekiugrupes.Pavad
  String cmd2 = "SELECT Skaitamedis.Kodas  AS grouptext,  Skaitamedis_lapai.Indeksas AS jrk, Prekes.Kodas AS kood " +
    " FROM  {oj{oj{oj  dbo.SkaitaMedis Skaitamedis    LEFT OUTER JOIN dbo.SkaitaMedis_Lapai Skaitamedis_lapai    ON  Skaitamedis.SkaitaMedisId = Skaitamedis_lapai.SkaitaMedisId} " +
    " LEFT OUTER JOIN dbo.Prekes Prekes  ON  Skaitamedis_lapai.LapaiId = Prekes.PrekesID} "+
    " LEFT OUTER JOIN dbo.PrekiuGrupes Prekiugrupes ON  Prekes.PrekiuGrupesId = Prekiugrupes.PrekiuGrupesID} " +
    " WHERE  Skaitamedis_lapai.Indeksas <> ( '' ) ORDER BY Prekiugrupes.Pavad, Skaitamedis.Kodas,Skaitamedis_lapai.Indeksas";
    
    
      if (isDebug()) {
          System.out.println(cmd);
      }

      try {
        Connection con = getNewConnection();
        Statement st = con.createStatement();
       
          
          ResultSet	      rs = st.executeQuery(cmd2);
          
          while (rs.next()) {
        	  String ti = rs.getString("grouptext").trim();
         	    String tid = rs.getString("jrk").trim();
         	    if(tid.length() == 1){
         	    	tid = "00" + tid;
         	    }
         	   if(tid.length() == 2){
        	    	tid = "0" + tid;
        	    }
                String kood = rs.getString("kood").trim();
                inde.put(kood , ti + "" + tid);
          }
          rs.close();
     	  
          	      rs = st.executeQuery(cmd);
             
             while (rs.next()) {
        	  boolean goes = true;
        	  String tid = rs.getString("tid");
              String kood = rs.getString("kood").trim();
            
              String nimetus =  rs.getString("nimetus");
               
              Product prod = new Product(kood);
               goes = false;
             try{
           	  double price = Double.parseDouble((String)hinnad.get(tid));
                 if(price == 0){
              	   price = 1;
                 }
                 prod.setPrice(price);
                 goes = true;
               //  System.out.println("hind " + prod.getPrice());  
             }catch(Exception d){
           	  
             }
           
             if(nimetus == null){
           	   goes = false;
             }else{
           	   nimetus = nimetus.trim().replaceAll("\n", " ") ;
             }
           	 
         
              String unit = rs.getString("unit");
              if(unit != null && unit.trim().length()>0){
              	prod.setUnit(unit.trim());
                }
try{
	 prod.setInCases(rs.getInt("kastis"));
}catch (Exception e){
	
}

              String ean = rs.getString("ean");
              if(ean != null && ean.trim().length()>0){
              	prod.setEAN(ean.trim());
              }
       
              String grupp = rs.getString("grupp");
              if(grupp != null && grupp.trim().length()>0){

              	 prod.setGroup(grupp.trim());
               
              }
              String grupp2 = rs.getString("grupp2");
              if(grupp2 != null && grupp2.trim().length()>0){

              	 prod.setGroupSecond(grupp2.trim());
               
              }
              
              String lice = rs.getString("lice");
              if(lice != null && lice.trim().length()>0){
              	 prod.licence = lice.trim();
              }
         
              if(nimetus != null){
                 prod.setName(Converter.enCodeEst(nimetus));
                 
                  if(inde.get(prod.getID()) != null){
                	  String aaa = inde.get(prod.getID()) + "";
                      prod.setNameSorting(aaa + nimetus);
                     // System.out.println("Sorting " + aaa + nimetus);
                  }
             
              }
              
              prod.setStock(0);          
              prod.setVat("21");
          
              if(this.stocksTXT != null && this.stocksTXT.get(prod.getID()) != null) {
            	  prod.setOldName("Likutis sandėliuose:|" + stocksTXT.get(prod.getID()));
              }
              
              if(logistics.get(prod.getID()) != null){
            	  Product pp = (Product)logistics.get(prod.getID());
            	  
            	  if(pp.getInCases() > 1){
            		  prod.setInCases(pp.getInCases());
            		}
            	  
            	  if(pp.getEAN()!= null && pp.getEAN().trim().length() >0){
            		prod.addBarcode(pp.getEAN().trim());  
            	  }
            	  
            	  if(pp.getOptAmountInUnits() > 1){
            		 // prod.setOptAmountInUnits(pp.getOptAmountInUnits());
            	  }
            	  
            	  if(pp.getOnPallet() > 1){
            		//  prod.setOnPallet(pp.getOnPallet());
            	  }
            	  
            	  if(pp.getBoxbrutto() > 1){
              		  prod.setBoxbrutto(pp.getBoxbrutto());
              	  }
            	  
            	  if(pp.getBoxnetto() > 1){
              		  prod.setBoxnetto(pp.getBoxnetto());
              	  }
              }
            
              
              if(goes){
            	  prcodes.put(tid, kood);
         		   products.put(prod.getName(), prod);
                  prodHash.put(prod.getID(), prod);  
              }
             
          }
          rs.close();
          st.close();
          con.close();
      } catch (Exception e) {
          getParent().showError(e);
      }
      System.out.println("Klaipedos Pienas Tooted l�petan ");

	return products;
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public Vector getCustomerProductsList(String agentCode) {

 	Vector custProds = new Vector();
	
	 String sql1 = "SELECT   part.Kodas as klkood ,  OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20),  " +
     " (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
     " pr.Kodas), Price = CONVERT(Money,(case when pk.Kiekis = 0 then 0 else pk.ppKainaBePVM / pk.Kiekis end), 4), CostPrice = CONVERT(Money, NULL), kogus = CONVERT(Int, pk.Kiekis),  " +
    "  ODDiscount = CONVERT(real, 0), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0) + ISNULL(mok.TarifasProc2, " + 
    "  0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15), pk.PardavimuKunoId))),  " +
    "  op.OperacijosId , op.Data as kuupaev " +
" FROM         Operacijos AS op INNER JOIN " +
" Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                     PardavimuKunai AS pk ON op.OperacijosId = pk.OperacijosId INNER JOIN " +
"                     OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                    Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                   Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId " +
"WHERE     0 = 1 AND op.Kilme = 35 and op.Data> getdate()-30 " +
"UNION ALL " +
"SELECT  part.Kodas as klkood ,    OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
"                     (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), ProductAsId = CONVERT(Int, pr.PrekesId), toode = CONVERT(varchar(35),  " +
"                     pr.Kodas), Price = CONVERT(Money, (case when pk.Kiekis = 0 then 0 else pk.ppKainaBePVM / pk.Kiekis end), 4), CostPrice = CONVERT(Money, xSav.Savikaina), kogus = CONVERT(Int, pk.Kiekis),  " +
"                     ODDiscount = CONVERT(real, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta), VAT = CONVERT(tinyInt, ISNULL(mok.TarifasProc1, 0)  " +
"                     + ISNULL(mok.TarifasProc2, 0) + ISNULL(mok.TarifasProc3, 0)), OrderDetailAsID = CONVERT(varchar(20), ISNULL(ok.KilmeInfo, 'S' + CONVERT(varchar(15),  " +
"                     pk.PardavimuKunoId))), op.OperacijosId , op.Data as kuupaev " +
"FROM         PardavimuKunai AS pk INNER JOIN " +
"                      Operacijos AS op ON pk.OperacijosId = op.OperacijosId INNER JOIN " +
"					   Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
" Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
"                      OperacijuKuneliai AS ok ON pk.PardavimuKunoId = ok.OperacijosKunoId INNER JOIN " +
"                      Prekes AS pr ON pk.PrekesId = pr.PrekesId INNER JOIN " +
"                      Mokesciai AS mok ON pk.MokesciaiId = mok.MokesciaiId LEFT OUTER JOIN " +
"                      xPardavimuKunai_Savikaina AS xSav ON pk.PardavimuKunoId = xsav.OperacijosKunoId " +
"WHERE     op.OperacijosTipoId = 50 and op.Data > getdate()-30";



        if (isDebug()) {
            System.out.println(sql1);
        }

        try {
          Connection con = this.getNewConnection();
          PreparedStatement stmt = con.prepareStatement(sql1);
          ResultSet	      rs = stmt.executeQuery();

          HashMap kliendid = new HashMap();
          while (rs.next()) {
            String kliendikood = rs.getString("klkood").trim();
          
         	   String tootekood = rs.getString("toode").trim();
         	 double hind = rs.getDouble("Price");
                double kogus = rs.getDouble("kogus");
                
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
               Product prod = new Product(tootekood);
                  CustProd cp = new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                               rs.getString("kuupaev").substring(5,10), hind, 0, 0, " ");
               	
               	    cust.addProduct(prod,cp,false);
          
               	    kliendid.put(kliendikood, cust);
                  custProds.add(cp);

            }

          rs.close();
          stmt.close();
          con.close();
        } catch (Exception e) {
          getParent().showError(e);
        }
       	return custProds;
    }

    public String getPilotDayData(Agent agent){
    	
        return null;

    }


    public String getNonMobileOrders(Agent agent){
    	return null;
    
     
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
	return 0;
    }
    


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
       
       
         String cmd = "select  a.employeecode AS agent, sum(billamount) AS summa from  "  +
			" (  SELECT     OrderAsID = CONVERT(varchar(20), op.OperacijosId), DeliveredOrderAsID = CONVERT(varchar(20), " + 
         "             (CASE WHEN op.OperacijosTipoId = 50 THEN op.OperacijosId ELSE NULL END)), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0),  " +
          "            BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data, BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma),  " +
          "            PaidAmount = CONVERT(Money, op.ApmoketaSuma), CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), " +
          "             Employeecode = CONVERT(varchar(35), masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), " + 
          "            op.Valiuta), DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
          "            CustomerCode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
          "       FROM         Operacijos AS op INNER JOIN " +
          "            Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
          "            Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
          "            PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
          "            MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
          "            Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
          "            MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
          "            OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId " +
         " WHERE     0 = 1 AND op.Kilme = 35 " +
 " UNION ALL " +
" SELECT     OrderAsID = CONVERT(varchar(20), CASE WHEN op.OperacijosTipoId = 50 THEN op2.KilmeInfo ELSE NULL END), DeliveredOrderAsID = CONVERT(varchar(20),  " +
  "                    op.OperacijosId), OrderDate = op.Data, OrderDiscount = CONVERT(Real, 0), BillCode = CONVERT(varchar(20), op.DokNr), BillIssueDate = op.Data,  " +
   "                   BillPaymentDate = CONVERT(DateTime, NULL), BillAmount = CONVERT(Money, op.Suma), PaidAmount = CONVERT(Money, op.ApmoketaSuma),  " +
    "                  CustomerAsID = CONVERT(Int, part.PartneriaiId), EmployeeAsID = CONVERT(varchar(35), masm.MusuAsmenysId), Employeecode = CONVERT(varchar(35),  " +
     "                 masm.Kodas), OrderState = CONVERT(Int, NULL), OrderStateExt = CONVERT(Int, NULL), CurrencyCode = CONVERT(varchar(2), op.Valiuta),  " +
      "                DeliveryAddressNo = CONVERT(varchar(2), NULL), OrderRemark = CONVERT(varchar(35), SUBSTRING(okp.Pastaba1, 1, 35)), op.OperacijosId,  " +
       "               customercode = part.Kodas, PaymentTime = CONVERT(SmallInt, msal.Terminas), PayerCode = part.regnr, op.OperacijosTipoId " +
"FROM         Operacijos AS op INNER JOIN " +
 "                     Pard_BendraInf AS pbi ON op.OperacijosId = pbi.OperacijosId INNER JOIN " +
  "                    Partneriai AS part ON pbi.PartneriaiId = part.PartneriaiId INNER JOIN " +
   "                   PartneriaiPA AS partpa ON part.PartneriaiId = partpa.PartneriaiId INNER JOIN " +
    "                  MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN " +
     "                 Pirk_VykdymoInf AS pvi ON pbi.Pirk_VykdymoInfId = pvi.Pirk_VykdymoInfId INNER JOIN " +
      "                MusuAsmenys AS masm ON pvi.MusuAsmenysId = masm.MusuAsmenysId LEFT OUTER JOIN " +
       "               OperacijuKuneliuPastabos AS okp ON op.OperacijosId = okp.OperacijosId INNER JOIN " +
        "              RysioOperacijos AS rop ON op.OperacijosId = rop.OperacijosId INNER JOIN " +
         "             Operacijos AS op2 ON rop.OperacijosId2 = op2.OperacijosId " +
" WHERE     op.OperacijosTipoId IN (50, 64) AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 ) " +
 " d INNER JOIN (SELECT  masm.MusuAsmenysId  AS EmployeeAsId, masm.Kodas AS employeecode, " +
        		" LTRIM(masm.Vardas + ' ' + masm.Pavarde) AS nimi,masm.Kodas as ladu FROM dbo.MusuAsmenys AS masm) a  ON a.employeeasid=d.employeeasid " +
			" where MONTH( billissuedate)=MONTH(GETDATE()) AND YEAR( billissuedate)=YEAR(GETDATE()) group by a.employeecode";
         log(cmd);


         
         try {
        	 
        	 HashMap<String ,Double> grsales = new HashMap<String ,Double>();
        	Connection con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
        	           getParent().getProps().getProperty("dsn.pass"));
             PreparedStatement stmt = con.prepareStatement(cmd);
     
             ResultSet rs = stmt.executeQuery();

             while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 
                    }

             rs.close();
             stmt.close();
             con.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }


   private TreeMap<String, String> getScannedLastDate(){
	   TreeMap<String, String> res = new TreeMap<String, String>();
	   String sql = "select partneris_id as klient, max (scan_time) as daat, max (patv_time) as lastconf from turto_list group by partneris_id";
	   try {
		   Connection c = this.getNewConnectionScan();
		   Statement st = c.createStatement();
		   ResultSet rs = st.executeQuery(sql);
		   
		   while(rs.next()){
		 	  String cliID = rs.getString("klient");
		 	  if(cliID != null) {
		 		
		 	  Date d2 = rs.getDate("daat");
		 	//  Date d2 = rs.getDate("lastconf");
		 	  
		   if(d2 != null){
		 		 res.put(cliID,(d2+"").substring(0,10));
		 	  }
		 	  
		 	 
		 	  }
		 	 
		   }
		   rs.close();
		   st.close();
		   c.close();
	   }catch(Exception e) {
		   e.printStackTrace();
	   }

       
	   return res;
   }


    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = ensureLen(rs.getString("tekst")," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
        if (tekst == null) tekst = " ";

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         String tel =  rs2.getString("telefon") ;
         tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
         sb.append(ensureLen(tel, " ") + sep);
        // System.out.println(ensureLen(tel, " "));
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
         if (agentName != null) agentName = agentName.trim();
        if (agentName == null) agentName = " ";
        bf2.append(agentName + "\n");
      }
      rs.close();
      ps.close();
      con.close();

      md.setClientAgentInfo(bf2.toString());

      return md;
    }

    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
    }

    private String ensureLen(String str, String replace){
      if (str == null) str = "";
      else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
      if (str.length() == 0){
        return replace;
      } else {
        return str;

      }
    }
    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getStartingTime() {
      return null;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getDriverVersion() {
	return DRIVER_VERSION;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getSubjectFilesTo() {
	return this.subjectFilesTo;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void log(String s) {
	getParent().log(s);
    }

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
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
       public void setDealers(TreeMap aDealers) {
        dealers = aDealers;
      }
     

    public static final String DRIVER_VERSION = "SkaiteKLP";
    private String             url = "jdbc:odbc:skaita";
    private String             url2 = "jdbc:odbc:report";
    private String             url3 = "jdbc:odbc:scan";
    private Connection	       con = null;
    private Vector grouplist = new Vector();
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();

	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
  private HashMap prodHash = new HashMap();
  
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
//       	 Lõppes kustutamine 
       //	custProds.add("suva");
		  this.getProducts(null);

       StringBuffer buf = new StringBuffer();
       Iterator enume = prodHash.keySet().iterator();
  
       while (enume.hasNext()) {
  	    	String asi = (String)enume.next();
  	    	
  	    	String toode = asi; 
  	    	
  	    	  toode = toode.replace('/', '_');
  	          toode = toode.replace('/', '_');
  	          
             if(toode.length() > 0){
              	
  	   	  res2 = new File (pictssou);
            ff2 = res2.list();
           
       	 for(int i = 0;ff2.length>i;i++){
           	if( ff2[i].startsWith(toode+".")){
             // System.out.println("pilt olemas " + ff2[i]);
          	File in = new File ( pictssou + ff2[i]); 
            	if(in.isFile()){
            	File inpda = new File ( pictspda + toode+ ".jpg");
            	if(!inpda.exists()){
               			Converter.shrinkAndCopyFile(in,inpda,600);
            	 }
            	
          		   buf.append(toode +".jpg;"  + inpda.length() + "\n");
          	
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

    

	
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

