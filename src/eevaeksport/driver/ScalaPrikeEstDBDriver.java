/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ee.merisoft.sm.SuperVisorEvalLine;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
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
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ScalaPrikeEstDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ScalaPrikeEstDBDriver(EksportPanel parent) {
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
    if(getParent().getProps().getProperty("db.url") != null) {
    	 url = getParent().getProps().getProperty("db.url");//"jdbc:odbc:scalaDB";	
    }
   
    
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
  
  
    public String getProductSortingData(){
       	return null;
       }
    /**
     *
     */
	
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
            public String getDealerCustomerCSV(String agent){
             return null;
             }

            HashMap stocks = new HashMap();

        public String getSaveStockInfo(){
        	  connectionUp(url);//'PR' + 
        	
        	  String cmd = "select SC33001 as toode ,sum( SC33005)  as stock  from SC330100 where " +
              " SC33005 >0 and SC33002='01' and SC33004 <> 'TAG' and " +
             " SC33003  not in (Select distinct BatchID from PXSCPRBatchAllocations01) group by SC33001 ";
        	  stocks = new HashMap();

		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           if(seis > 0){
		               products.append(kood +  ";");
		               products.append(" ;");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		               stocks.put(kood, seis); 
		           }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
        
        public Hashtable getAksStock(){
      	  connectionUp(url);//'PR' + 
      	  String  cmd = "select SC33001 as toode ,sum( SC33005)  as stock from SC330100 where " +
        	 "  SC33005 >0 and SC33002 in ('22','23') and left(ltrim(SC33004),1)   !='P'  " +
			 " and SC33010 = 0   and " +
        	 "   SC33003  not in (Select distinct BatchID from PXSCPRBatchAllocations01) " +
			"	group by SC33001" ;


		       if (isDebug()) System.out.println(cmd);
		       Hashtable products = new Hashtable();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           if(seis > 0){
		               products.put(kood ,getParent().getNF().format(seis) );
		          
		             }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products;
		   }


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }


        public String getDistribution() {
          return null;
        }
        
        private TreeMap extraVisits (){
        	TreeMap res = new TreeMap();
        	
        	
        	File f = new File(this.getDir() + "MSasendused.xls");
    		try {
    			FileInputStream ff = new FileInputStream(f);
    			HSSFWorkbook workbook = new HSSFWorkbook((ff));
    			HSSFSheet sheet = workbook.getSheetAt(0);
    			HSSFRow row = null;
    		
    			row = sheet.getRow(0);
    			// Mï¿½ï¿½RAME ï¿½RA MIS Vï¿½LJAS ASUVAD Sortimendi tï¿½hised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 0; a <  sheet.getLastRowNum(); a++) {
    				row = sheet.getRow(a);
    				String custid = "";
    				String paev = "";
    				String agent = "";
    				try {
    					// System.out.println(row.getCell((short)a).getStringCellValue());
    					custid =  row.getCell((short) 0).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						
        					// System.out.println(row.getCell((short)a).getStringCellValue());
        					custid =  ((int)row.getCell((short) 0).getNumericCellValue()) + "";
        				} catch (Exception r) {
        					// System.out.println(row.getCell((short)a).getNumericCellValue());
        				}// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    				
    				try {
    					// System.out.println(row.getCell((short)a).getStringCellValue());
    					paev =  row.getCell((short) 6).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						
        					// System.out.println(row.getCell((short)a).getStringCellValue());
        					paev =  ((int)row.getCell((short) 6).getNumericCellValue()) + "";
        				} catch (Exception r) {
        					// System.out.println(row.getCell((short)a).getNumericCellValue());
        				}// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    				
    				try {
    					// System.out.println(row.getCell((short)a).getStringCellValue());
    					agent =  row.getCell((short) 8).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						
        					// System.out.println(row.getCell((short)a).getStringCellValue());
        					agent =  ((int)row.getCell((short) 8).getNumericCellValue()) + "";
        				} catch (Exception r) {
        					// System.out.println(row.getCell((short)a).getNumericCellValue());
        				}// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    			//	System.out.println(custid + " " + paev + " " + agent);
    				if(agent.length() > 0 && paev.length() > 0 && custid.length() > 0){
    					Toode cc = new Toode (agent.trim());
        				cc.setBrand(paev.trim());
        				res.put(custid.trim(), cc);
        				
    				}
    				
    				
    			}
    		}catch (Exception d){
    			d.printStackTrace();
    		}
    		

        	
    	//	System.out.println("ekstra kliendid suurus " + res.size());
        	
        	
        	return res ;
        }
        
        private HashSet longKliendid = new HashSet();

        private TreeMap longVisits (){
        	TreeMap res = new TreeMap();
        	
        	
        	File f = new File(this.getDir() + "Visits-long_interval.xls");
    		try {
    			FileInputStream ff = new FileInputStream(f);
    			HSSFWorkbook workbook = new HSSFWorkbook((ff));
    			HSSFSheet sheet = workbook.getSheetAt(0);
    			HSSFRow row = null;
    		
    			row = sheet.getRow(0);
    			// Mï¿½ï¿½RAME ï¿½RA MIS Vï¿½LJAS ASUVAD Sortimendi tï¿½hised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 0; a <  sheet.getLastRowNum(); a++) {
    				row = sheet.getRow(a);
    				String custid = "";
    				String paev = "";
    				String agent = "";
    				String nadalad = "";
    				try {
    					custid =  row.getCell((short) 1).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						custid =  ((int)row.getCell((short) 1).getNumericCellValue()) + "";
        				} catch (Exception r) {
        				}
    				}
    				
    				longKliendid.add(custid.trim());
    				
    				try {
    					nadalad =  row.getCell((short) 4).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						nadalad =  ((int)row.getCell((short) 4).getNumericCellValue()) + "";
        				} catch (Exception r) {
        				}
        				}
    				
    				try {
    					paev =  row.getCell((short) 5).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						paev =  ((int)row.getCell((short) 5).getNumericCellValue()) + "";
        				} catch (Exception r) {
        				}}
    				
    				try {
    					agent =  row.getCell((short) 0).getStringCellValue();
    				} catch (Exception e) {
    					try {
    						agent =  ((int)row.getCell((short) 0).getNumericCellValue()) + "";
        				} catch (Exception r) {
        				}}
    			
    			
    				
    				int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    				
    				if(custid.indexOf("T19251") != -1)
    				System.out.println(week + " " + custid + " " + paev + " " + agent);
    				
    				//boolean goes = false;
    				boolean goes = true;
    				StringTokenizer st = new StringTokenizer(nadalad,",");
    				while(st.hasMoreTokens()){
    					String num = st.nextToken();
    					if(num.equals(week+"")){
    						goes = true;
    					}
    				}
    				
    				if(goes && agent.length() > 0 && paev.length() > 0 && custid.length() > 0){
    					Toode cc = new Toode (agent.trim());
    					 st = new StringTokenizer(nadalad,",");
        				while(st.hasMoreTokens()){
        					String num = st.nextToken();
        					
        	    				
        					if(num.equals(week+"")){
        						if(cc.getBrand() == null){
        							cc.setBrand(paev.trim());
            						if(custid.indexOf("T19251") != -1){
                						System.out.println("ohoobrand " + num + " " + week + " " + custid.trim() + " " +paev);
                					}
            							
        						}
        					}
        				}
    					
        				
        				res.put(custid.trim(), cc);
        					}
    				
    				
    			}
    		}catch (Exception d){
    			d.printStackTrace();
    		}
    		

        	
    		//System.out.println("ekstra kliendid suurus " + res.size() + " kiendid kellel on " + this.longKliendid);
        	
        	
        	return res ;
        }


        
        private TreeMap longTableExtra(){
        	TreeMap res = new TreeMap();
        	
        	
    		try {

    			connectionUp(url);
    	        String    sql = "SELECT SL01001 AS kood, SL01198 AS paev,SL01199 as agent FROM SL010100 " + 
    	        		 " where SL01198 != '' and SL01199 != ''" ;
    	        Statement st = con.createStatement();
    	        ResultSet rs = st.executeQuery(sql);

    	        
    	        while (rs.next()) {

    	        	String custid = rs.getString("kood").trim();
    				String paev = rs.getString("paev").trim();
    				String agent = rs.getString("agent").trim();
    				
    				
    				if( agent.length() > 0 && paev.length() > 0 && custid.length() > 0){
    					Toode cc = new Toode (agent.trim());
        				cc.setBrand(paev.trim());
        				res.put(custid.trim(), cc);
        				
    				}
    			}
    	        rs.close();
    	        st.close();
    	        
    		}catch (Exception d){
    			d.printStackTrace();
    		}
    		

        	
    		//System.out.println("ekstra kliendid suurus " + res.size() + " kiendid kellel on " + this.longKliendid);
        	
        	
        	return res ;
        }



        
        
  private TreeMap getEanProds() throws SQLException{
	  TreeMap prods  = new TreeMap();
	  
	//  1. Vï¿½tame toodetele vï¿½lja lyhikoodid
	  String sql3 = " select SC01010 as lyhi ,SC01001 as kood from SC010100 where SC01010 <>'' and  rtrim(ltrim(SC01016)) != 'S' order by SC01010";
 	  
      Connection con2 = getNewConnection2();
      PreparedStatement   stmt2 = con2.prepareStatement(sql3);
      HashMap lyhike = new HashMap();
      Vector kood = new Vector();
      HashMap koodid = new HashMap();
      ResultSet rs2 = stmt2.executeQuery();
    while (rs2.next()) {
      String lyhi = rs2.getString("lyhi").trim();
      String tootekood = rs2.getString("kood").trim();
      if(!kood.contains(tootekood)){
            kood.addElement(tootekood);
      }
      if(lyhike.get(lyhi) == null){
      	Vector vv = new Vector ();
      	vv.addElement(tootekood);
      	lyhike.put(lyhi, vv);
      }else{
      	Vector vv = (Vector)lyhike.get(lyhi);
      	vv.add(tootekood);
      	lyhike.put(lyhi, vv) ;
      }
    }
    rs2.close();
    stmt2.close();
      
      for(int i = 0 ;i<kood.size();i++){
      	String toode = (String)kood.elementAt(i);
      	Iterator iter = lyhike.values().iterator();
      	while(iter.hasNext()){
      		Vector vv = (Vector)iter.next();
      		if(vv.contains(toode)){
               koodid.put(toode, vv);
      		} 		
      	}
      }
      
      // vectoris on tpooted millel on paraleelkood ja hashis vector igal tootel kaaslased
      
      getSaveStockInfo();
       // stocks . sees on hetke laoseisud ..
      
	  try{
		//  leiame aktiivsed tooted eanidele 
		  
	        String    sql = " Select SC01001 AS kood, SC01132 AS ean  " +
" FROM SC010100 WHERE  ( LEN(LTRIM(SC01128))>0 and  LEN(LTRIM(SC01132))>0 and rtrim(ltrim(SC01016)) not in ( 'S','L' ,'U','R') ) " ;
  Statement st = con2.createStatement();
	        ResultSet rs = st.executeQuery(sql);

	        while (rs.next()) {

		          String koodi = Converter.enCodeEst(rs.getString("kood").trim());
	          String ean =  rs.getString("ean").trim();
	          double sts = 0;
	          if(stocks.get(koodi) != null){
	        	  sts = Double.parseDouble(stocks.get(koodi)+"");
	          }
	          
	          
	          if(koodid.get(koodi) != null){
	        	  Vector vv = (Vector)koodid.get(koodi);
	        	  for(int i = 0;i<vv.size();i++){
	        		  String toode = (String)vv.elementAt(i);
	        		  
	        		  if(stocks.get(toode) != null){
	        			  double seis = Double.parseDouble(stocks.get(toode)+""); 
	    	        	  if(sts < seis){
	    	        		  koodi = toode;
	    	        		  sts = seis;
	    	        	  }
	    	          }  
	        	  
	        	  }
            	}
	          
	          prods.put(ean,koodi);
	                  }
	        
	        rs.close();
	        st.close();
	        con2.close();

	  }catch (Exception e){
		  e.printStackTrace();
	  }
	  System.out.println("Prods size " + prods.size());
	  return prods;
  }
        
        
        public Vector getFixedAssortmentData() {
    		Vector result = new Vector();
    		File f = new File(this.getDir() + "Listingud.xlsx");
    		try {
    			TreeMap prods = getEanProds();
    			FileInputStream ff = new FileInputStream(f);
    			XSSFWorkbook workbook = new XSSFWorkbook((ff));
    			XSSFSheet sheet = workbook.getSheet("listingud");
    			XSSFRow row = null;
    			TreeMap groups = new TreeMap();
    			row = sheet.getRow(5);
    			// Mï¿½ï¿½RAME ï¿½RA MIS Vï¿½LJAS ASUVAD Sortimendi tï¿½hised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 5; a < row.getLastCellNum(); a++) {
    				try {
    					// System.out.println(row.getCell((short)a).getStringCellValue());
    					String jj = row.getCell((short) a)
    							.getStringCellValue();
    					if(jj != null && jj.length() > 0) {
    						groups.put(a + "", jj);
    					}
    					
    				} catch (Exception e) {
    					// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    			}
//System.out.println("groups " + groups.toString());
    			
    			for (int i = 6; i < sheet.getLastRowNum(); i++) {
    				row = sheet.getRow(i);
    				String prodID = "";
                    String ean = "";
    				if (row.getCell((short) 0) != null) {

    					try {
    						prodID = (int) (row.getCell((short) 0)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID = (row.getCell((short) 0).getStringCellValue());
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
  	    			  	 
	    			
    					if(prodID.equals("VS356")){
    					//	 System.out.println("muudan koodi" + prodID  + " " + ean + " " + prods.get(ean)); 
  						   
    					}
    					if(ean != null && ean.trim().length() > 0 && prods.get(ean) != null){

						    //System.out.println("muudan koodi" + prodID  + " " + ean + " "); 
						    //prodID = prods.get(ean) + "";
						   // System.out.print(prodID);    
    					}

                           if(!prodID.equals("0")){
                        	
                         // Siin lï¿½heme reasisse rokkima ja vaatama kas se toode
    					// lï¿½heb kaasa
    					for (int a = 5; a < row.getLastCellNum(); a++) {

    						String group = (String) groups.get(a + "");
    						
    						if (group != null && group.trim().length() > 0) {

    							String custID = " ";
    							String clGroup = " ";
    							String prodGroup = " ";

    							String go = null;
    							clGroup = group;

    							MatrixLine mline = new MatrixLine();
    							// Vaatame kas selles veerus on sellel tootel pandud
    							// mingi tï¿½his
    							if (row.getCell((short) a) != null) {

    								try {
    									go = (int) (row.getCell((short) a)
    											.getNumericCellValue()) + "";
    								} catch (Exception e) {
    									go = (row.getCell((short) a)
    											.getStringCellValue());
    								}

    							}
    							
    							
    							if(prodID.equals("VS356")){
		    					//	 System.out.println("muudan koodi sees " + go + " " + clGroup + " " + prodID  + " " + ean + " " + prods.get(ean)); 
		  						   
		    					}

    							if (go != null && (go.equals("X") || go.equals("K") || go.equals("O"))) {

    							//	System.out.println(prodID + " " + group + " " + ean + " " + go);
    								// System.out.println( a + " " + prodID + " " +
    								 //clGroup + " " + go);

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
                                    if(go.equals("K")){
                                    	mline.setLineinfo("CENTRAL");
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
System.out.println("fixed size " + result.size());
    		return result;

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

    
  
    
    private void connectionUp2(String url){
        try {
          if (con2 != null){
            con2.close();
          }
             con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
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

    public TreeMap getProdGroupsSecond() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240100 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'II' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood=  rs.getString("liik").trim();
          String nimetus = Converter.enCodeEst(rs.getString("nimetus").trim());
          if(kood.equals("VVN") || kood.equals("PVN") || kood.equals("RVN")){
      this.connectionUp2(url);
      Statement st1 = con2.createStatement();
      ResultSet rs1 = st1.executeQuery("select SC01067 as riik FROM SC010100 WHERE   LEN(LTRIM(SC01128))>0 and  SC01128 ='" + kood + "' and rtrim(ltrim(SC01016)) != 'S' group BY SC01067 ");

      while (rs1.next()) {
    	  String riik = rs1.getString("riik");
    	  tm.put(kood + riik  , nimetus + " " + riik );
      }
           rs1.close();
           st1.close();
          }else{
              tm.put(kood, nimetus +" " + kood);  
          }
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        tm.put("RRR", "Propaganda ja Demagoogia");
        
        return tm;
 }
    
 
    public TreeMap getAgents() {
        connectionUp(url);
        
        
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi , '' as grupp  FROM SY240100 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();
	
	 StringBuffer sb2 = new StringBuffer();
	 Properties props =  new Properties();
	    try{
	    	props =  EksportFrame.loadProps("asendajad.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    
	    Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	
	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();
	
	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agent.setIsPilot(true);
              agent.setLanguage("EST");
              agent.setCountry("ee");
              agent.setPassWord("0000");
              String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
              if(email.trim().length() > 5){
            	    sb2.append(agent.getID() + ";" + nimi + "; ; ;" + email +"\n");  
             }
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.length() > 0){
            	  agent.setActive(true);
              }
              
          	String agentAGENTS = props.getProperty(agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
               //  System.out.println("Lisan Duubleid "  +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
              
              agents.put(nimi, agent);
          	agents1.put(agent.getID(), agent);
	    }
	    Agent aaa = new Agent("HOUSE1");
	    aaa.setName("HOUSE1");
	    aaa.setObject("01");
        aaa.setIsPilot(true);
	    agents.put("HOUSE1", aaa);
	    

	    if(sb2.length() > 2){
	    	try{
	    		 Converter.saveFileInUTF8( "agent.txt", sb2);
	    	}catch (Exception e){
	    	}
	     }
	    if(rcompany != null){
	    	//Converter.sendAgentJson(agents, rcompany, false);
	    }
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
      //  setBulkSales(agents1);
	return agents;
    }

    public Vector getPriceMatrixData()  {
    	    	
    	
      	   String    sql = "SELECT  SL01001 AS custid,'' AS clgroup,SCP6001 AS prodid,'' AS prodgroup,SCP6009, \r\n" + 
      	   		"      	  SCP6008 AS price ,SCP6011  as lpaev , SCP6010  as spaev \r\n" + 
      	   		"		   FROM SCP60100 WITH (NOLOCK) INNER JOIN SL010100 ON  SCP6002= SL01001 LEFT JOIN SY240100 on SCP6009=SY24002 and SY24001='BG' WHERE \r\n" + 
      	   		"                getdate() BETWEEN  SCP6010 AND SCP6011  \r\n" + 
      	   		"                order by SL01001";

             Vector result = new Vector();
               try {
                
              	 connectionUp(url);
               Statement  st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql);

                 while (rs.next()) {

                   String custID = rs.getString("custid").trim();
                   String clGroup = rs.getString("clgroup");
                   String prodID = rs.getString("prodid").trim();
                   String prodGroup = rs.getString("prodgroup");           
                   String discount = rs.getString("SCP6009");
                   double price = rs.getDouble("price");
                   String ldate = rs.getString("lpaev");
                   String sdate = rs.getString("spaev");
                   MatrixLine mline = new MatrixLine();

                   if (custID != null && custID.length() > 0) {
                	   mline.setCliGroup(custID.trim());
                	   mline.setDiscGroup(custID.trim());
                   }
                   if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                    // mline.setCliGroup(clGroup.trim());
                   }

                   
                   if(ldate != null){
                  	 mline.setEndDate(ldate.substring(0,10));
                   }
                   if(sdate != null){
                    	 mline.setStartDate(sdate.substring(0,10));
                     }
                   if (prodID != null && prodID.length() > 0) {
                     mline.setProdID(prodID);
                   }
                   if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                     mline.setProdGroup(prodGroup.trim());
                   }
                   if(discount.trim().length()==0){
                   	discount = "0";
                   }
                   double disc = 0; 
                   try{
                  	disc = Double.parseDouble(discount.replaceAll(",", "."));
                   }catch(Exception e){
                  	 e.printStackTrace();
                   }
                  // mline.setDiscount(disc);

                   
                   if(disc != 0){
                	   price = price*(100-disc)/100;
                	   
                   }
                   if (price != 0) {
                     mline.setPrice(price);
                   }
                   if(mline.getPrice() > 0 ){
                       result.add(mline);    
                   }
                   
                 }

                 
                 
                 // toote gruppide lisamine 
                 
                 
                 
                 
                 
                 String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
                 " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
                 " SC39005 else SC39004 end AS price  " +
                 " FROM SC390100 WHERE SC39002 not in ('00','11','10') order by SC39002,SC39001 ";
                 rs = st.executeQuery(sql2);

                 while (rs.next()) {

                   String clGroup = rs.getString("clgroup").trim();
                   String prodID = rs.getString("prodid").trim();
                   String discount = "0";       
     			   
                   double price = rs.getDouble("price");
                   MatrixLine mline = new MatrixLine();
                 
                   if (clGroup !=null && clGroup.trim().length() > 0) {
                     mline.setCliGroup(clGroup.trim());
                   }

                   if (prodID != null && prodID.trim().length() > 0) {
                     mline.setProdID(prodID.trim());
                   }
       
                   if (Double.parseDouble(discount) == 0 && price != 0) {
                     mline.setPrice(price);

                   }
                  result.add(mline);  // Kommentisin vï¿½lja kuniks uus hinna skeem hakkab toimima
                 }
                 
                 String sql4 = "Select  SC08002 as custid,SC08003 as prodgr,SC08004 as discount  from SC080100 where" +
                 		"  CONVERT(char(8), getdate(),112)  BETWEEN  SC08005 AND SC08006 ";
                 rs = st.executeQuery(sql4);

                 while (rs.next()) {

                   String custID = rs.getString("custid").trim();
                   String prodGR = rs.getString("prodgr").trim();
                   double discount = 0;
                   try{
                	   discount = rs.getDouble("discount");   
                   }catch(Exception e){
                	   
                   }
                 
                   
                
                   MatrixLine mline = new MatrixLine();
                 
                   if (custID !=null && custID.trim().length() > 0) {
                     mline.setCliGroup(custID.trim());
                   }

                   if (prodGR != null && prodGR.trim().length() > 0) {
                     mline.setProdGroup(prodGR.trim());
                   }
       
                   if (discount != 0) {
                     mline.setDiscount(discount);
                     result.add(mline);
                   }
                   // Kommentisin vï¿½lja kuniks uus hinna skeem hakkab toimima
                 }
                 
                 
            
                 
                 
      	
                 
                 
               }catch(Exception e){
              	 e.printStackTrace();
               }
      	
          return result;
        }
       
	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
    
	TreeMap abi = new TreeMap();
	
	
    public TreeMap getSort() {
		TreeMap result = new TreeMap();
		abi = new TreeMap();
		File f = new File(this.getDir() + "Listingud.xlsx");
		try {
			
			if(!f.exists()){
				System.out.println("listingut pole");
			}
			FileInputStream ff = new FileInputStream(f);
			
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheet("tarnekliendid");
			XSSFRow row = null;

			

			for (int i = 3; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String custID = "";
				//System.out.println(row);
				if (row != null && row.getCell((short) 1) != null) {

					try {
						custID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						try {
							custID = (row.getCell((short) 1).getStringCellValue());
						}catch(Exception ee) {
							ee.printStackTrace();
						}
					
					}
					
				//	System.out.println("i ja custid " + i + " '" + custID+ "'");
                       if(custID != null && !custID.equals("0")  && custID.length() > 0){
                 
                    		try {
			     	    	  String text =  row.getCell((short) 0).getStringCellValue() ;
				    		  result.put(custID.trim(),text);  
				    		} catch (Exception e) {
                    		 e.printStackTrace();
    						}

                    		try {
			     	    	  String text =  row.getCell((short) 5).getStringCellValue() ;
			     	    	  if(text != null && text.trim().length() > 0){
					    		  abi.put(custID.trim(),text);    
			     	    	  }
				    		} catch (Exception e) {
                    		// e.printStackTrace();
    						}
                }
			}}
		} catch (Exception e) {
			e.printStackTrace();
		}
//System.out.println("sortcodes "+ result.size() + " " + result.toString());
		return result;

	}



    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        connectionUp2(url);
        
        Converter.getManagersReplacements(getAgents1(), rcompany, user , pass);
        
        TreeMap sort = getSort();
        System.out.println("sort " + sort.size());
        HashMap lisainfo = new HashMap();
        HashMap broninfo = new HashMap();
        TreeMap longVisits = longVisits();
        TreeMap tableExtra = longTableExtra();
        
        Converter cc = new Converter();
        HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
        
        Vector kysi = new Vector();
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress,"
        		+ "SL01003 as tanav,SL01004 as linn,SL01005 as regioon,SL01083 as indeks, SC45005 AS ale,SL01036 AS ale2 ,  " +
       "   SL01053 AS visit, ' ' AS info ,SL01035 as agent,SL22005 as paym , SL01056 as klgrupp, SL01075 as block ,SL01056 as payer ,SL14007 as delday ,SL01099  as lisa " +
       "  ,(SELECT SL01037 from SL010100 where SL01001=kl.SL01056) as limiit ,vr.day+' ' + vr.DESCR as veoring ," +
       "   (SELECT  SL01052 from SL010100 where SL01001=kl.SL01056) as leping , SL01010 AS CHAIN " + 
       "   FROM SL010100 kl left JOIN SC450100    ON SL01036=SC45003  " +
	   "   LEFT JOIN veoring0100 vr on vr.VR=SL01027    LEFT OUTER JOIN SL140100 ON SL01001=SL14001 AND SL14002='00' " +
       "   LEFT OUTER JOIN SY240100 ON SL01035=SY24002 AND SY24001='BK' " +
       "   LEFT OUTER JOIN SL220100 ON SL01024=SL22002 AND SL22001='0'  " +
       "   WHERE len(SL01001)>4 ORDER BY SL01002"; 

        connectionUp(url);	
        log(cmd);
           
        	
           String  sql = " select SL01001 as klient,CEOname as boss ,CEOtel as bosstel,CEOemail as bossmail,ACCTname as book, " +
 		"	ACCTtel as booktel,ACCTemail as bookmail ,CONTACTname as nimi,CONTACTtel as tel,CONTACTemail as mail,OTHERname , OTHERtel ,OTHERemail , LastUpdate  from CUSTOMERDATA0100";
             
           try{ 
                Statement  st = con.createStatement();
                ResultSet  rs = st.executeQuery(sql);
                Calendar ccc = Calendar.getInstance();
                ccc.add(Calendar.DAY_OF_YEAR, -356);
                while (rs.next()) {
                String id = rs.getString("klient");
                String res = "";
                	String boss  = rs.getString("boss");
                	String bosstel  = rs.getString("bosstel");
                	String bossmail  = rs.getString("bossmail");
                	String book  = rs.getString("book");
                	String booktel  = rs.getString("booktel");
                	String bookmail  = rs.getString("bookmail");
                    	String nimi  = rs.getString("nimi");
                	String tel  = rs.getString("tel");
                	String mail  = rs.getString("mail");
                	Date lastd  = rs.getDate("LastUpdate");
                	
                	if(nimi != null && nimi.trim().length() >0){
                		res = res + "Kontakt: " + nimi ;
                	}
                	if(tel != null && tel.trim().length() >0){
                		res = res + " " + tel +"|" ;
                	}else{
                		res = res + "|" ;
                	}
                	
                	if(mail != null && mail.trim().length() >0){
                		res = res + " " + mail +"|" ;
                	}else{
                		res = res + "|" ;
                	}
                	
                	
                	if(book != null && book.trim().length() >0){
                		res = res + "RP: " + book ;
                	}
                	if(booktel != null && booktel.trim().length() >0){
                		res = res + " " + booktel +"|" ;
                	}else{
                		res = res + "|" ;
                	}
                	if(bookmail != null && bookmail.trim().length() >0){
                		res = res + " " + bookmail +"|" ;
                	}else{
                		res = res + "|" ;
                	}
                	
                	
                	if(boss != null && boss.trim().length() >0){
                		res = res + "CEO: " + boss ;
                	}
                
                	if(bosstel != null && bosstel.trim().length() >0){
                		res = res + " " + bosstel +"|" ;
                	}else{
                		res = res + "|" ;
                	}
                	
                	if(bossmail != null && bossmail.trim().length() >0){
                		res = res + " " + bossmail +"|" ;
                	}else{
                		res = res + "|" ;
                	}
                	
                	if(!res.equals("|||")){
                    	lisainfo.put(id.trim(), res);	
                	}
                	
                	if(lastd.before(ccc.getTime())){
                		kysi.add(id.trim());
                	}
                }
            
          ///  System.out.println("lisainfo " + lisainfo.size());
            
                rs.close();
                st.close();
                
                
            
            
            connectionDown();
          }catch (SQLException ex) {
            ex.printStackTrace();
          }
   
        

          
          
          String  sql6 = "	 select CustomerCode as klient ,'(' + ltrim(str(sum(SC33005))) + ') ' +SC01002 + SC01003  as info from PXSCPRBatchAllocations01 " +
 " inner join SC010100 on SC01001 = StockCode inner join SC330100 on BatchID = SC33003 " +
 " group by CustomerCode,SC01002 , SC01003";
           
         try{ 
        	 connectionUp(url);
              Statement  st = con.createStatement();
              ResultSet  rs = st.executeQuery(sql6);
             
              while (rs.next()) {
              String id = rs.getString("klient").trim();
              String res = rs.getString("info");;
              	
              if(broninfo.get(id) != null){
            	  String aso = (String)broninfo.get(id);
            	  
            	  broninfo.put(id, aso + "|" + res);
              }else{
            	  broninfo.put(id,"Bronni Info|" + res); 
              }
             
              }
          
       //  System.out.println("bronni " + broninfo.size());
          
              rs.close();
              st.close();
              
              
          
          
          connectionDown();
        }catch (SQLException ex) {
          ex.printStackTrace();
        }
          
          
          
          
          
          
          
          
        
        
        
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        connectionUp();
	try {
           PreparedStatement stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	   	  ResultSet    rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("aadress").trim()),"Estonia"));
                  nimi = nimi + " " + cust.getAddress();
                
                cust.setName(Converter.enCodeEst(nimi));
                
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
                if(abi.get(cust.getID()) != null){
                	cust.setCity("Sort : " + abi.get(cust.getID()));
                }

                String indeks = rs.getString("indeks");
                if (indeks != null && indeks.length() > 0){
                  cust.restZipCode = indeks.trim();
                }

                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                 cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	//cust.setDiscount(17);
                	
                }
                String visit = rs.getString("visit").trim();
                String vis = (visit);
                
                
                String agen = rs.getString("agent");
            
                
                boolean setvis = true;
               

                
                

                
                
                if(longVisits.get(cust.getID()) != null){
                	
                	Toode t = (Toode)longVisits.get(cust.getID());
                	Agent d = null;
                	if(cust.hasAgent(t.getID())){
                		d = (Agent) cust.getAgents().get(t.getID());
                	}else{
                		d = new Agent(t.getID());
                	}
                	
                	if(t.getBrand() != null && t.getBrand().trim().length() > 0){
                		d.setVisit(t.getBrand());
                	}
                	
                	if(cust.getAgents().get(t.getID()) != null){
                		Agent olemas = (Agent) cust.getAgents().get(t.getID());
                		
                		if(t.getBrand() != null && t.getBrand().trim().length() > 0){
                    		olemas.setVisit(t.getBrand());
                    	}
                		
                		
						
                		cust.addAgent(olemas);
                    }else{
                		cust.addAgent(d);
                    }
                	
			  	
                }
    
                if(!longKliendid.contains(cust.getID())){
                	
                	if(tableExtra.get(cust.getID()) != null){
                    	Toode t = (Toode)tableExtra.get(cust.getID());
                    	Agent d = new Agent(t.getID());
                    	if(t.getBrand()!= null)
                    	d.setVisit(t.getBrand());
                    	 if(!cust.hasAgent(t.getID()))
                          cust.addAgent(d);
                        
                    	  }
                }
                
                
                
                if(agen != null){
                	Agent ee = new Agent(agen.trim());
                	if(vis.trim().length() > 0){
                		ee.setVisit(vis.trim());
                	}
                      if(!cust.hasAgent(agen.trim())){
                    	  cust.addAgent(ee); 	  
                      }
                   
                    cust.setMainAgent(agen.trim());
                    
                    Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
    				
    				if(agg != null && agg.getDubbler().size()>0){
    					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
    						String iid= (String)((Agent)this.getAgents1().get(agen.trim())).getDubbler().elementAt(i);
    						if(!cust.hasAgent(iid)){
        						cust.addAgent(new Agent(iid));	
    						}
    					}
    				}
    				
                  }
                
                if(vis.trim().length() > 0 && setvis){
                //	cust.setVisit(vis.trim());	
                }
                
                
                String paym = rs.getString("paym");
                if(paym != null && paym.trim().length()>0){
                	cust.setPaymTerm(paym.trim());
                }
                cust.addAgent(new Agent("HOUSE1"));
              
                
                if (cust.getID().equals("0100000")) {
                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        }
                      }
                
              
                
                String type  =  rs.getString("klgrupp");
                
                if(type != null && type.trim().length()>0){
                	  cust.setCustType(type);
                      
                }
                
                
              
                if(sort.get(cust.getID()) != null){
                	 cust.addFixSortCode((String)sort.get(cust.getID()));
                }
                
                String block  =  rs.getString("block");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("1")){
                		cust.setSalesBlockT(true);
                	}
                }
                
                
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer);              	
                }

                String delday = rs.getString("delday");
                if(delday != null && delday.trim().length()>0){
                	//cust.setPersonalData(delday.trim().replaceAll(";", ":"));              	
                }
                
                String lisaa = rs.getString("lisa");
                if(lisaa != null && lisaa.trim().length()>0){
                	if(cust.getCity().trim().length() > 1) {
                		cust.setCity(cust.getCity() + "|Lisainfo: " + lisaa.trim().replaceAll(";", ":"));  
                	}else {
                		cust.setCity("Lisainfo: " + lisaa.trim().replaceAll(";", ":"));  
                	}
                       	
                }
                
                String limiit = rs.getString("limiit");
                if(limiit != null && limiit.trim().length()>0 ){
                	try {
                		int lim = (int)Double.parseDouble(limiit.trim());
                		if(lim < 100000) {
                			cust.setCredit(lim+""); 
                		}
                      
                	}catch(Exception e) {
                		e.printStackTrace();
                	}
                           	
                }
                
                
                
                
                boolean hi = false;
                String veoring = rs.getString("veoring");
                if(veoring != null && veoring.trim().length()>0 ){
                	hi = true ;
                }
          
                String leping = rs.getString("leping");
                if(leping != null && leping.trim().length()>0 ){
                	String lep = getLeping(leping);
                	if("NO".equals(lep)){
                    	//cust.setInfo(Converter.enCodeEst("Lepingu kuupäev aegunud |" + leping.trim()  + "|"));   	
                	}
                	if("ERROR".equals(lep)){
                    	//cust.setInfo(Converter.enCodeEst("Lepingu kuupüev puudub või vigane|"));   	
                	}
                }else{
                	//cust.setInfo(Converter.enCodeEst("Lepingu kuupäev puudub või vigane|"));  
                }
                String chain = rs.getString("chain");
                if(chain != null && chain.trim().length()>0 ){
                	cust.setChain(chain.trim());
                }
                	
                
                if(lisainfo.get(cust.getID())!= null){
                	String lisa = (String)lisainfo.get(cust.getID());
                	while(lisa.endsWith("|")){
                		lisa = lisa.substring(0,lisa.length()-1);
                	}
                	if(hi){
                    	cust.setPersonalData(Converter.enCodeEst(veoring.trim() + "|" +lisa));	
                	}else{
                    	cust.setPersonalData(Converter.enCodeEst(lisa));
                	}
                }
                
             
                
                
                if(cust.getPersonalData()== null || cust.getPersonalData().trim().length() == 0){
                	if(hi){
                		cust.setPersonalData(Converter.enCodeEst(veoring.trim()));
                	}
                }
                		String info= cust.getInfo();
                		if(info == null){
                			info = "";
                		}else{
                			info = info + "|";
                		}
                if(kysi.contains(cust.getID())){
                	cust.setInfo(info + Converter.enCodeEst("Vajalik kontrollida ja vajadusel|sisestada (muuta) kontaktandmed|ava tipitï¿½pi->Detailne info"));
                }
                	
                if(broninfo.get(cust.getPayer()) != null){
                	String lisa = (String)broninfo.get(cust.getPayer());
                   	cust.setInfo(Converter.enCodeEst(lisa));	
                }
                if(broninfo.get(cust.getID()) != null){
                	String lisa = (String)broninfo.get(cust.getID());
                   	cust.setInfo(Converter.enCodeEst(lisa));	
                }
                
                cc.addQuestiosToCustomer(cust,kyssad);
                
                
                //otsime vï¿½lasumma igale kauplusele 
                
                String sql8 = "SELECT SL01001 AS id,SUM((SL03013-SL03053)/(case when SL03015 = 0 then 1 else 15.6466 end)) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
                " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01001='" + cust.getPayer()+ "'  and SL01001 not in ('0634','1682','0104','1901','1986') GROUP BY SL01001 ";

   Statement stm = con2.createStatement();

   ResultSet rs3 = stm.executeQuery(sql8);
   while(rs3.next()){    
     double debt = Double.parseDouble(rs3.getString("debt").trim());
       cust.setDebt(cust.getDebt() + debt);
   }
   
   rs3.close();
   stm.close();

   String sql7 = "SELECT SL01001 AS id,SUM((SL03013-SL03053)/(case when SL03015 = 0 then 1 else 15.6466 end)) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
   " NOT(SL03013=SL03053) and SL01001='" + cust.getPayer()+ "' and SL01001 not in ('0634','1682','0104','1901','1986') GROUP BY SL01001 ";

			Statement stmr = con2.createStatement();
			
			ResultSet rs4 = stmr.executeQuery(sql7);
			while(rs4.next()){    
			double xxx = Double.parseDouble(rs4.getString("debt").trim());
			cust.setUsedCredit(xxx);
			}
			
			rs4.close();
			stmr.close();

   if(cust.getDebt()<0.02 && cust.getDebt() > 0 ){
   	   cust.setDebt(0);
     }           
                cust.setCountry("Eesti");
                customers.put(cust.getName() + cust.getID(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();

            if(rcompany != null){
    	    //	Converter.sendCustomerJson(customers, rcompany,true,user,pass);
    	    }

        } catch (Exception e) {
            getParent().showError(e);
        }

        
	return customers;
    }

    
    private String getLeping(String ins){
    	String res = "OK";
    	//System.out.println("ins " + ins);
    	
    	if(ins.indexOf(".") != -1){
    	 try{
    		 int dd = Integer.parseInt(ins.substring(0,ins.indexOf(".")));
    		 String aa =ins.substring(ins.indexOf(".")+1);
    	//	 System.out.println("aa " + aa);
    		 if(aa.indexOf(".") != -1){
    			 int mm = Integer.parseInt(aa.substring(0,aa.indexOf(".")));
    			 String bb =aa.substring(aa.indexOf(".")+1);
    			// System.out.println("bb " + bb);
    			 if(bb.trim().length() == 2 ){
    				 bb = "20" + bb.trim();
    			 }else if(bb.trim().length() == 4){
    				 bb = bb.trim();
    			 }else if(bb.trim().length() == 3){
    				 bb = bb.substring(0,2);
    			 }
    			
    		  int yyyy = Integer.parseInt(bb);
    		  
    		  Calendar cc = Calendar.getInstance();
    		  cc.set(Calendar.YEAR, yyyy);
    		  cc.set(Calendar.DAY_OF_MONTH,dd);
    		  cc.set(Calendar.MONTH, mm-1);
    		  Calendar cal = Calendar.getInstance();
    		  cal.add(Calendar.DAY_OF_MONTH, -720);
    		  if(cal.getTime().after(cc.getTime())){
    			// System.out.println("ins " + ins);
    			  return "NO";
    		  }
    		 }
    		 
    	 }catch(Exception e){
    		// e.printStackTrace();
    		 res = "ERROR";
    	 }
    	}else{
    		res = "ERROR";
    	}
    	
    	return res;
    }
    
    public TreeMap getProdGroups() throws SQLException {
    	try{
    		connectionUp(url);
    	}catch(Exception e ){
    		connectionUp(url);
    	}
    
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240100 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
            tm.put(kood,
            		Converter.enCodeEst(rs.getString("nimetus").trim() +" " + kood));
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }    
    
    
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,(SL03013-SL03053)/(case when SL03015 = 0 then 1 else 15.6466 end) AS summa , -datediff(day ,getdate(),SL03006) as yle " +
      		"FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                   "LEN(LTRIM(SL03013))>0 and SL01001 not in ('0634','1682','0104','1901','1986') "; // AND SL03006<GETDATE()+5 
      sql += " ORDER BY SL01001";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setPayerID(debt.getCustID());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        debt.setDue(rs.getString("yle"));
        if(debt.getSum()>0.04 || debt.getSum()<-0.04){
        	list.addElement(debt);	
        }
        }
      rs.close();
      stmt.close();
      return list;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);
        getSaveStockInfo();
        Hashtable aksiis = getAksStock();
        String lisa = "";
        if(getParent().getProps().getProperty("lisatooted") != null){
        	lisa = " or SC01001 in (" + getParent().getProps().getProperty("lisatooted") + ")";
        }

        String cmd =" SELECT  RTRIM(SC01001) AS toode,( RTRIM(SC01002) " + 
        " +rtrim(SC01003)) + ' ' +RTRIM(SC01001) AS nimi,SC39004 AS myygihind , SC01009 AS tykke, SC01162 AS alusel, SC01163 as kihis,SC03003-SC03004 AS stock, SC01018 AS maxale,'' AS pandikas , " +  
" case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 as grupp1 ,SC01128 AS grupp2,SC01067 as riik ,SC01132 AS ean  , SC01016 as letter ,SC01161 as statustop" +
" FROM SC010100 left outer JOIN SC030100 on SC03001=SC01001 and SC03002='01' AND SC03003!=0 left outer JOIN SC390100 ON SC01001 " +
" = SC39001 and SC39002='00' " +
"    WHERE  ( LEN(LTRIM(SC01128))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) not in ( 'S','L' ,'U','R') ) " + lisa  ;
        
        
        String cmd2 = "select SC33001 as toode ,sum( SC33005)  as kogus  from SC330100 where  " +
       " SC33005 >0 and SC33002='01' and SC33004 <> 'TAG' and  " +
       "SC33003  not in (Select distinct BatchID from PXSCPRBatchAllocations01) group by SC33001 " ;

        
        
   	if (isDebug()) {
	    System.out.println(cmd);
	}

	
    
    

	TreeMap products = new TreeMap();
	try {
    
   
	
	
    Properties props = parent.getParentFrame().loadProps("Lisainfo.txt");
	
	
	String maxdisc = getParent().getProps().getProperty("maxdiscount");

    TreeMap aktiiv = new TreeMap();

    Statement       st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd2);
	    while (rs.next()) {
	    	String kood = rs.getString("toode").trim();
	    	double kogus = rs.getDouble("kogus");
	    	aktiiv.put(kood, kogus + "");
	    }
			
	    
	    rs = st.executeQuery(cmd);
	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = Converter.enCodeEst(rs.getString("nimi").trim());
		
		
                Product prod = new Product(kood);
               
                if(maxdisc != null){
                	prod.setMaxDiscount(Double.parseDouble(maxdisc));
                }
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                
                 String alus = rs.getString("alusel");
                 if(alus != null) {
                	 try {
                		 double all = parseDouble(alus.trim());
                		 
                		 if(all > 0) {
                			 prod.setOnPallet(all*prod.getInCases()); 
                		 }
                		
                	 }catch(Exception e) {
                		 
                	 }
                 }
                 
                 String kihis = rs.getString("kihis");
                 if(kihis != null) {
                	 try {
                		 double all = parseDouble(kihis.trim());
                		 
                		 if(all > 0) {
                			 prod.setPartii("Kihis : " + (int)all); 
                		 }
                		
                	 }catch(Exception e) {
                		 
                	 }
                 }
               
               
                
               if (stocks.get(prod.getID()) != null){
            	   prod.setStock(Double.parseDouble(stocks.get(prod.getID()) + ""));
                }
              
               
               String maxale = rs.getString("maxale");
               if(maxale != null & maxale.trim().length()>0){
            	   prod.setMaxDiscount(parseDouble(maxale.trim()));  
               }
               
                

                String pant = rs.getString("pandikas");
                if(pant != null && pant.trim().length()>0){
                  prod.setPandPakendiKood(pant.trim());
                }
                String out = rs.getString("out");
                if(out != null && out.trim().equals("1")){
                //  prod.setStatusSellout("1");
                }
                String gr1 = rs.getString("grupp1");
                prod.setGroup(gr1.trim());
                String gr = rs.getString("grupp2");
                String riik = rs.getString("riik");
                if(gr != null && gr.trim().length()>0){
                	if((gr.equals("PVN") || gr.equals("RVN") || gr.equals("VVN")) && riik != null){
                		gr = gr.trim() + riik.trim();
                	}
                	
                  prod.setGroupSecond(gr.trim());
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>4){
                  prod.setEAN(gr.trim());
                  nimetus = nimetus + " "+ gr.trim().substring(gr.trim().length()-4); 
                }
                
                if(aksiis.get(kood) != null){
                  	 nimetus = nimetus + "|Aks." + aksiis.get(kood) +  "|";
                   }
                  
                   
                
                String letter = rs.getString("letter");
                
                
                boolean ok = true;
                double seis = 0 ;
                if(aktiiv.get(prod.getID()) != null){
                	seis = Double.parseDouble((String)aktiiv.get(prod.getID()));
                }
               
                 if(letter != null && letter.trim().length()>0 && letter.trim().equals("X") && seis <= 0){
                     ok = false;
                 }else if(letter != null && letter.trim().length()>0 && letter.trim().equals("X") && seis > 0){
                	 prod.setStock(seis);
                 }
                 
                 if(lisa.indexOf(prod.getID()) != -1){
                	 ok = true; 
                 }
                 
                 if(letter != null && letter.trim().length()>0 && letter.trim().equals("X")){
                	 nimetus = nimetus + " *X*";  
                	// System.out.println("On olemas X  " +  nimetus);
                 }
                 String add = "      ";
                 if(props.getProperty(prod.getID()) != null  && props.getProperty(prod.getID()).trim().length() > 0){
                	// add =    "    " + props.getProperty(prod.getID()) + " "; 
                	 prod.setOldName(props.getProperty(prod.getID()));
                 }
                 prod.setName(add +nimetus );
              prod.setVat("21"); 
              
              String statustop = rs.getString("statustop");

              if(statustop != null && statustop.trim().length()>0 && statustop.trim().toLowerCase().equals("jah")){
             	 prod.setStatusTop("1");  
             	// System.out.println("On olemas X  " +  nimetus);
              }
              
              if(ok){
  		        products.put(nimetus, prod);  
              }

	    }
            rs.close();
            st.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return products;
    }

    
    private Connection getNewConnection2() throws SQLException{
        return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
                getParent().getProps().getProperty("dsn.pass"));
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

    
    
    public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
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
	        Connection con = getNewConnection2();
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
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
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
	             String cc = Converter.deCode(lw.getLine().getCustComment());
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = Converter.deCode(lw.getLine().getComment());
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = Converter.deCode(lw.getLine().getCustAgentName());
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
	             ps.setString(30, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, lw.getLine().getResInt3()); //111 on eesti andmed
	             if(hasNotLine(wr.getInfo().getAgentID() ,lw.getLine())){
	            	  ps.executeUpdate();
	   	            	 
	             }
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }

  private boolean hasNotLine(String agent ,SuperVisorEvalLine ol){
	  boolean res = true;
	  try{
		  
		  String check = "'" + agent + ol.getCustID() + ol.getProdID()  + ol.getResStr3() + "' + LEFT(CONVERT(VARCHAR, getdate(), 120), 10)";
		  Connection con = getNewConnection2();
	      String    sql = " select rtrim(agentid) + '' +  rtrim(custid)+ ''+ rtrim(prodid) + '' +  rtrim(resstr3) + '' + LEFT(CONVERT(VARCHAR, sentdate, 120), 10)" +
	      		" from supervisor" +
	      		" where rtrim(agentid) + '' +  rtrim(custid)+ ''+ rtrim(prodid) + '' +  rtrim(resstr3) + ''" +
	      		" + LEFT(CONVERT(VARCHAR, sentdate, 120), 10) = " +
	      		check;
	      System.out.println(sql);
	      
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);

	      if (rs.next()) {
	          res = false;
	    	  System.out.println("Ei lisa ");
	  	    	    	  
	      }else{
	    	  System.out.println("Lisan Rea ");
	  	    
	      }
   rs.close();
   st.close();
   con.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }

 
	  
	  return res;
	  
  }

    
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp(url);

  
	  HashMap kliendid = new HashMap();
	Vector custProds = new Vector();
      
          try {
        	  
// ESITEKS Moodustame kaks objekti paraleel koodide tarbeks
        	  String sql3 = " select SC01010 as lyhi ,SC01001 as kood from SC010100 where SC01010 <>'' and  rtrim(ltrim(SC01016)) != 'S' order by SC01010";
        	         	  
              Connection con2 = getNewConnection2();
              PreparedStatement   stmt2 = con2.prepareStatement(sql3);
        	
            HashMap aeg = new HashMap();

            HashMap lyhike = new HashMap();
            Vector kood = new Vector();
            HashMap koodid = new HashMap();
            ResultSet rs2 = stmt2.executeQuery();
          while (rs2.next()) {
            String lyhi = rs2.getString("lyhi").trim();
            String tootekood = rs2.getString("kood").trim();
            if(!kood.contains(tootekood)){
                  kood.addElement(tootekood);
            }
            if(lyhike.get(lyhi) == null){
            	Vector vv = new Vector ();
            	vv.addElement(tootekood);
            	lyhike.put(lyhi, vv);
            }else{
            	Vector vv = (Vector)lyhike.get(lyhi);
            	vv.add(tootekood);
            	lyhike.put(lyhi, vv) ;
            }
          }
            
            for(int i = 0 ;i<kood.size();i++){
            	String toode = (String)kood.elementAt(i);
            	Iterator iter = lyhike.values().iterator();
            	while(iter.hasNext()){
            		Vector vv = (Vector)iter.next();
            		if(vv.contains(toode)){
                     koodid.put(toode, vv);
            		} 		
            	}
            }
  
      
    //  Siin Paneme sortimendid Sisse 
            
          String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,sum(case when ST03015 >= getdate()-30 " +
        "  		 then ST03020 else 0 end ) as pluss,max(ST03020) AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr " +
         "   FROM ST030100  WITH(NOLOCK) INNER JOIN SL010100 WITH(NOLOCK) ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 and left(SL01001,2) !='M3' " +
         "  GROUP BY SL01001,ST03017, ST03015 ORDER BY SL01001,ST03017 ,ST03015 ASC";

					if (isDebug()) {
					System.out.println(sql2);
					}
          
          
          stmt = con.prepareStatement(sql2);
          stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          
            ResultSet	      rs = stmt.executeQuery();
            TreeMap ttt = new TreeMap();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double pluss = rs.getDouble("pluss");
              double kogus = Double.parseDouble(rs.getString("kogus").trim());
           
              Date date = rs.getDate("lastdate");
              String arvenr = rs.getString("arvenr");
              boolean ok = true;
              if(ttt.get(kliendikood+tootekood) != null ){           	  
              	String vv = (String)ttt.get(kliendikood+tootekood);
              	pluss = pluss + Double.parseDouble(vv);
              	ttt.put(kliendikood+tootekood, pluss + "");
              
              }else {
            	  	ttt.put(kliendikood+tootekood, pluss + "");
              }
              if(ok){

                  CustProd cp = new CustProd(kliendikood, tootekood, kogus,pluss ,0,
                  		date.toString().substring(5,10),0,0,0," ");
                  cp.setAbiKood(arvenr);
                    custProds.add(cp);  
                    Product prod = new Product(tootekood);
                    Customer cust = (Customer)kliendid.get(kliendikood);
                    if (cust == null){
                        cust = new Customer(kliendikood);
                      }
                    cust.addProduct(prod,cp,false);
                      kliendid.put(kliendikood, cust);
                     
              }
              if(koodid.get(tootekood) != null){
            	  Vector vv = (Vector)koodid.get(tootekood);
            	  for(int i = 0;i<vv.size();i++){
            		  String toode = (String)vv.elementAt(i);
            		  if(!toode.equals(tootekood)){
            			 // System.out.println("lisan jama " + kliendikood + " " + tootekood );
            			  CustProd cp = new CustProd(kliendikood, toode, 0,0 ,0,
                          		date.toString().substring(5,10),0,0,0," ");
                          
                            custProds.add(cp);  
                            Product prod = new Product(tootekood);
                            Customer cust = (Customer)kliendid.get(kliendikood);
                            if (cust == null){
                                cust = new Customer(kliendikood);
                              }
                            cust.addProduct(prod,cp,false);
                              kliendid.put(kliendikood, cust);
            		  }
            		 
                            
            	  }
              }
            }
            rs.close();
            stmt.close();

            
           // Siin lisame listingud mida pole myydud aga on lï¿½bi summeerimis koodi vaja lisada
            
             sql2 = " select custid,prodid,sentdate,prodname from supervisor where comment like '%TO THE LIST%' ";

  					if (isDebug()) {
  					System.out.println(sql2);
  					}
            
            
            stmt = con.prepareStatement(sql2);
           // stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            
              	      rs = stmt.executeQuery();

              while (rs.next()) {
                String kliendikood = rs.getString("custid").trim();
                String tootekood = rs.getString("prodid").trim();
                double kogus = 0;
                Date date = rs.getDate("sentdate");
                
                
                boolean ok = true;
                if(aeg.get(kliendikood+tootekood) != null ){             	
                	ok = false;
                //kui sihuke toode on juba olemas antud kliendil siis ei pane	
                }
                if(ok){

                    CustProd cp = new CustProd(kliendikood, tootekood, kogus,0 ,0,
                    		date.toString().substring(5,10),0,0,0," ");
                    
                      custProds.add(cp);  
                      Product prod = new Product(tootekood);
                      Customer cust = (Customer)kliendid.get(kliendikood);
                      if (cust == null){
                          cust = new Customer(kliendikood);
                        }
                      cust.addProduct(prod,cp,false);
                        kliendid.put(kliendikood, cust);
                       
                }
              }
              rs.close();
              stmt.close();
            
            
            
            
   
            
          
            

   int counter = 0;
   int cou = 0;
   String oldklient = null;
   String olddate = " ";
   String oldtoode = null;

  String cmd2 =  " SELECT  SL01001 AS klient , ST03017 AS toode ,sum(ST03020) AS Kogus,ST03015  as lastdate " + 
                     "  FROM ST030100 WITH(NOLOCK) INNER JOIN SL010100 WITH(NOLOCK) ON ST03001 = SL01001 WHERE ST03015> getdate()-180 AND ST03016>0 AND ST03020>0 " +				 
					 "  group by SL01001,ST03017,ST03015 " +
                     "  ORDER BY SL01001,ST03017 ,ST03015 DESC ";
  System.out.println(cmd2);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"

               con2 = getNewConnection2();
  Statement stmt6 = con2.createStatement();
     rs2 = stmt6.executeQuery(cmd2);


while (rs2.next()){
String klient = rs2.getString("klient").trim();
       String toode = rs2.getString("toode").trim();
       String kogus = "0";
       String date = " ";
          kogus = rs2.getDouble("kogus")+"";
          kogus = kogus.replace('.',',');
		  if(kogus.indexOf(",") != -1){
		  	kogus=kogus.substring(0,kogus.indexOf(","));
		  }
           date = rs2.getString("lastdate").trim().substring(0,10);

       if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
       if(!oldklient.equals(klient)){
                     counter = 0;
                     oldklient = klient;
                     olddate = date;
       }
       if(!toode.equals(oldtoode)){
       	counter = 0;
       }
       Customer c = (Customer)kliendid.get(klient);
       if (c != null ){

         ArrayList al = (ArrayList)c.getProducts().get(toode);
         if (al != null){
           CustProd cp = (CustProd)al.get(1);
           if (cp != null){
            if(!olddate.equals(date)){counter++;}
             if (counter == 0){cp.setS1(kogus );}
             if (counter == 1){cp.setS2(kogus );}
             if (counter == 2){cp.setS3(kogus );}
             if (counter == 3){cp.setS4(kogus );}
             if (counter == 4){cp.setS5(kogus );}

             custProds.add(cp);
           }
         }
         oldklient = klient;
         olddate = date;
         oldtoode = toode;
       }
       cou++;
     }


rs2.close();
con2.close();
    
            
          } catch (Exception e) {
            getParent().showError(e);
          }
        
          
          
          
          
          
        connectionDown();
        System.out.println("CustProd lï¿½peb");
	return custProds;
    }


    public String getPilotDayData(Agent agent){
    	
    	
        String sql = "  SELECT case when OR01001 = OR01072 then 0 else 1 end as abi , SL01002 AS nimi ,OR01015 AS kuup ,OR01001  AS number, OR01024*1.2 AS summa FROM  " + 
                 "  OR010100 INNER JOIN SL010100 ON OR01004 = SL01001 WHERE OR01015 > getdate()-1 and OR01019=?  "
                 + " ORDER BY case when OR01001 = OR01072 then 0 else 1 end ";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
try {
 stmt = con.prepareStatement(sql);
 stmt.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");
 ResultSet	      rs = stmt.executeQuery();
 double droidsum = 0;
 double muusum = 0;
 boolean change = false;

 boolean droid = true;
 
 while(rs.next()){
	 int abi = rs.getInt("abi");
	  	 
     String nimi = rs.getString("nimi");
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append(nimi + "\n");
 	sb.append(rs.getString("kuup").trim().substring(0,10));
 	sb.append("  Nr." + rs.getString("number").trim() + "\t");
 	double summa = Double.parseDouble(rs.getString("summa").trim());
 
 	
 	String sum = getNF().format(summa) ;
     sb.append(sum + " eur\n" + sep);
   
	if(abi == 0){
  		droidsum = droidsum + summa;
  	}else{
       change = true;
  		muusum = muusum + summa;
  		if(droid && droidsum > 0 ){
  			droid = false;
  			sb.append("------------------------------\n");
  			sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
  		}
  	}
   
	
	
 }
 
	if(droid && droidsum > 0 ){
			droid = false;
			sb.append("------------------------------\n");
			sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
		}
 
 if(muusum > 0){
		sb.append("------------------------------\n");
		sb.append("E-tellimused Kokku :" + getNF().format(muusum)  + "eur");
		 
 }
 
} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();



Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent.getID() + "report.txt", sb.toString());
//System.out.println(sb.toString());
return "";//sb.toString();

    }


    public String getNonMobileOrders(Agent agent){
      connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      */
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020100  " +
        " LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            String header = getParent().getString("name") + "  \t\t" + getParent().getString("number") +
            "\t" + getParent().getString("sum") + sep + "-------------------------------------------" + sep;
            boolean heade = true;
            while(rs.next()){
            	if (heade){
            		sb.append(header);
            		heade = false;
            	}
                String nimi = rs.getString("nimi");
                if (nimi == null){
                nimi = " ";
                } else {nimi = nimi.trim();}

            	sb.append(nimi + "\n");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep +  sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();
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
      if (agentCode == null) return 0;
      connectionUp(url);
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR200100 WHERE MONTH(OR20015)=" +
                     "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) AND OR20019=?";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery();

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

	return 0;
    }

    
    private TreeMap makePlans(){
 	   TreeMap res = new TreeMap();
 	  
 	   try{
 		
 	   Calendar cal = Calendar.getInstance();
 	   int month = (cal.get(Calendar.MONTH)+1);
 	    int a = 0;
 	   File f = new File (getExcelDir() + "SalesPlans.xlsx");
 		  FileInputStream	 ff = new FileInputStream(f);
 		XSSFWorkbook workbook = new XSSFWorkbook((ff));
 		int sheets =workbook.getNumberOfSheets();
 		
 		
 		for(int y = 0;y < sheets;y++){
 			XSSFSheet sheet = workbook.getSheetAt(y);
 	     XSSFRow row = null; 
 				ff.close();	
 				if(sheet != null){
 					for(int i = 0 ; i < sheet.getLastRowNum();i++){
 						 row = sheet.getRow(i);
 						if(row == null && a == 0){
 							a = i;
 						}else{
 							a = sheet.getLastRowNum();
 						}
 					}
 					String grupp = "";
 					String abi = "";
 					if(y != 0){
 						try{
 							row = sheet.getRow(1);
 							grupp = "|"+ (row.getCell((short) 1).getStringCellValue()); 
 							row = sheet.getRow(2);
 							abi = "|"+  (row.getCell((short) 1).getStringCellValue()); 
 								
 						}catch(Exception t){
 							
 						}
 					
 					}

 					for(int i = 5 ; i < a + 1;i++){
 						 row = sheet.getRow(i);
 						String agent = null;
 						
 						try {
 							agent = (row.getCell((short) 0).getStringCellValue());
 					      } catch (Exception e) {
 						   //e.printStackTrace();
 					      }
 					//	System.out.println(agent + " " + grupp + " " + abi );
 						double plan = 0;
 						try {
 			    			plan = Double.parseDouble(row.getCell((short) month+1).getNumericCellValue()+ "");
 						} catch (Exception e) {
 	   					 e.printStackTrace();	
 	   				  	 plan = Double.parseDouble(row.getCell((short) month+1).getStringCellValue());
 						}
 						
 						if(agent != null && agent.length() > 0){
 						    res.put(agent+grupp.trim()+abi.trim(),plan);
 						}
 					}}
 		}
 	   }catch (Exception e){
 		   e.printStackTrace();
 	   }
 		System.out.println("jooksevplaan " + res.toString());
 		
 	   return res;
 }


    

    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
       int yy = Calendar.getInstance().get(Calendar.YEAR);
       int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
       int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);       
       TreeMap plans = makePlans();
         String cmd = " select ST03007 as agent, sum(ST03021*ST03020*(100-ST03022)/100) as sales , " +
			" (sum(ST03021*ST03020*(100-ST03022)/100) - sum(ST03023*ST03020)) as omv ,sum(ST03020*SC01071/900) as kaste " +
			"   from ST030100 WITH(NOLOCK) INNER JOIN  SC010100 WITH(NOLOCK) ON ST03017=SC01001  " +
			"    where MONTH(ST03015)=  " +
            " MONTH(GETDATE()) AND YEAR(ST03015)=YEAR(GETDATE())  " +
			" group by ST03007";
         log(cmd);

         try {
             stmt = con.prepareStatement(cmd);
             //stmt.setString(1, agentCode);

             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet	      rs = stmt.executeQuery();

             while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)rs.getDouble("sales"));
                   agent.setMonthProfit((int)rs.getDouble("omv"));
                   agent.buffer = (int)(rs.getDouble("kaste"));

             	  double msales = agent.buffer;
         		  double psales = 0;

               	try{
           		  psales = Double.parseDouble(plans.get(agent.getID())+"");
           		  double  tehtudsalperc = msales/psales;
               	  double plaanitavMyykSal = d2 * msales/dd;
               	  double  prognosalperc = plaanitavMyykSal/psales;
                 String asi = Converter.enCodeEst("Müük: ") + (int)msales + " €||";
                 if(dd > 6){
               	  asi = asi + Converter.enCodeEst("Plaan tehtud/Prognoos :");
                     asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
                 }
                 
                    asi = asi + Converter.enCodeEst("Müügi plaan: ") + (int)psales + " €||";    
                 // agent.setStart(agent.getStart() +  Converter.enCodeEst(asi ));
                 // System.out.println(Converter.enCodeEst(agentID + "' " +asi  + " "  + plans.get(agent.getID())) );
                   
           		}catch(Exception e){
           		//  e.printStackTrace();
           	  }
                 
                 
                 
                 }
                    }

             rs.close();
             stmt.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
      //System.out.println("nr:" + nr + ", total:" + total);
      return total;
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
    	   NF.setMaximumFractionDigits(2);
         return NF;
       }

       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }

       
       public TreeMap getAgents1() {
   		return agents1;
   	}
       private TreeMap agents1 = new TreeMap();
       
    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "ScalaPrikeEst";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:reportEST";
    private Connection	       con = null;
    private Connection	       con2 = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
  
	
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

