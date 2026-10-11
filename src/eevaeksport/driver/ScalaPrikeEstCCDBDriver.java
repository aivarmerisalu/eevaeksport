/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ScalaPrikeEstCCDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ScalaPrikeEstCCDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    }
    public String getProductSortingData(){
       	return null;
       }
    /**
     *
     */
	  private void updateCustcodes(){
	  	Hashtable res = new Hashtable();
	  	try {
			File fil = new File("sidus.txt");
			 System.out.println("Via Koodide sidumine ");
	if (fil.exists()){
	       System.out.println("Seon koode ... ");
			FileReader fr = new FileReader("sidus.txt");
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ";");
				while(st.hasMoreTokens()){
					res.put(st.nextToken(),st.nextToken());
				}
				
	       }
			System.out.println("Kokku firmasi :" + res.size());
			if(res.size() > 0){
				Enumeration enums = res.keys();
				while(enums.hasMoreElements()){
				String omakood = (String)enums.nextElement();
				String viakood = (String)res.get(omakood);
				   String sql1 = "UPDATE  SL010200 set SL01129 ='" + viakood+ "' where SL01001  =  '" + omakood + "'";			
				    System.out.println(sql1);
				    Statement st1 = con.createStatement();
				    st1.executeUpdate(sql1);
				    st1.close();	
				}
				System.out.println("Kliendi Koodide sidumine valmis " );
			}
			
			
			
	}
			}catch (Exception e){
				//e.printStackTrace();
				
			}
	  }
    
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


        public String getSaveStockInfo(){
        	  connectionUp(url);//'PR' + 
        	  
        	  connectionUp2(url);

        	  StringBuffer logbook = new StringBuffer();
        	  String add =( Calendar.getInstance().getTimeInMillis()+ "").substring(5);
        	  File log = new File(getDirHind() + add + "logi.txt");
        
        	  	try {
        			File fil = new File(getDirHind() + "hinnamuutus.txt");
        			 System.out.println("Hinnamuudatuste t��tlemine");
        	if (fil.exists()){
        	       System.out.println("Loen muudatuste faili  ... ");
        			FileReader fr = new FileReader(getDirHind() + "hinnamuutus.txt");
        			BufferedReader reader = new BufferedReader(fr);
        			String line = null;
        			while ((line = reader.readLine()) != null) {
        				String ean = null;
        				String hinnakiri = null;
        				 double uushind = -1;
        				try{
        					StringTokenizer st = new StringTokenizer(line, ";");
            				 ean = st.nextToken();
            				 hinnakiri = st.nextToken();
            			     uushind = Double.parseDouble(st.nextToken());	
        				}catch(Exception d ){
        					logbook.append("Toote " + ean + " hk:" + hinnakiri + " hinnauuendus ei vasta n�uetele - kontrolli hinna formaati\n\n");
        					d.printStackTrace();
        				}
        				
        				logbook.append(ean + "-le vastavad tooted Scalas\n");
        				
        				String sql= "SELECT  RTRIM(SC01001) AS toode,( RTRIM(SC01002)+rtrim(SC01003))  AS nimi " +
                        " FROM SC010100  WHERE LEN(LTRIM(SC01128))>0 and rtrim(ltrim(SC01016)) != 'S' and SC01132='" + ean + "'	";
        		
        				try {

        			         Statement st = con.createStatement();
        			         ResultSet rs = st.executeQuery(sql);

        			         while (rs.next()) {
        			        	 String kood = rs.getString("toode").trim();
        			        	 String nimi = rs.getString("nimi").trim();
        			        	 logbook.append("\n" + ean +  " "  + kood + " " + nimi +  "\n");
        			        	 
        			        	 if(uushind !=-1){     

        			        			Statement ss = con2.createStatement();
        			    				String sql2 = "UPDATE SC390100 set SC39004 = " + uushind + " where SC39002='" + hinnakiri +"' AND SC01001 ='" +kood +"'";
        			    				// ss.executeUpdate(sql2);
        			    				 ss.close();

            			        		 logbook.append("  Hind Muudetud HK:" + hinnakiri + " uushind" + uushind + " "  + kood + " " + nimi +  "\n");
            			   		
        			    				}        			    				
     	                                 }
        			        	 
        			         
        			         
        				}catch(Exception e){
        	        		e.printStackTrace();
        	        	}
        				
        				
        	       }
        			
        	}
        
        	  	if(logbook.length() > 30){
                         if (log.exists())log.delete();
                           try {
                     		PrintWriter os = new PrintWriter(new FileWriter(
                     				getDirHind() + add + "logi.txt", true));
                     		os.write(logbook.toString());
                     		os.close();
                     	  }catch (IOException ex) {
                     		ex.printStackTrace();
                     	  }                   
        	  	}
        	  	}catch(Exception e){
        		e.printStackTrace();
        	}
        	
        
		       return null;
		   }
        
        public Hashtable getAksStock(){
      	  connectionUp(url);//'PR' + 
      	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , sum (SC03003) AS stock  " +  
		    "  FROM SC030100  WHERE SC03002 in ('22','23') AND SC03003!=0 group by SC03001 ORDER BY SC03001" ;


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

        public Vector getFixedAssortmentData() {
    		Vector result = new Vector();
    		File f = new File(this.getDir() + "Listingud.xls");
    		try {
    			FileInputStream ff = new FileInputStream(f);
    			HSSFWorkbook workbook = new HSSFWorkbook((ff));
    			HSSFSheet sheet = workbook.getSheet("listingud");
    			HSSFRow row = null;
    			TreeMap groups = new TreeMap();
    			row = sheet.getRow(5);
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

    			for (int i = 6; i < sheet.getLastRowNum(); i++) {
    				row = sheet.getRow(i);
    				String prodID = "";

    				if (row.getCell((short) 0) != null) {

    					try {
    						prodID = (int) (row.getCell((short) 0)
    								.getNumericCellValue()) + "";
    					} catch (Exception e) {
    						prodID = (row.getCell((short) 0).getStringCellValue());
    					}


                           if(!prodID.equals("0")){
                        		
                           		
          				                  	
    					// Siin l�heme reasisse rokkima ja vaatama kas se toode
    					// l�heb kaasa
    					for (int a = 5; a < row.getLastCellNum(); a++) {

    						String group = (String) groups.get(a + "");
    						// System.out.println(prodID + " " + group);
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

    							if (go != null && go.equals("X")) {

    								 System.out.println( a + " " + prodID + " " +
    								 clGroup + " " + go);

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
          String nimetus = rs.getString("nimetus").trim();
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
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240100 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();
	updateCustcodes();
	 Properties props =  new Properties();
	    try{
	    	props =  EksportFrame.loadProps("asendajad.txt");
	  	  
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
              
				String agentAGENTS = props.getProperty(agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 System.out.println("Lisan Duubleid "  +agent.getID() + " " + agent.getDubbler().toString());
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
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public Vector getPriceMatrixData()  {
    	    	
    	
      	   String    sql = "SELECT SL01001 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, " +
				" SC18008 AS price ,( case when SC18011 < getdate()+100 then SC18011 else null end) as paev " +
                " FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,7)= SL01001 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE " +
                " SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011  " +
               " order by SL01001,SUBSTRING(SC18002,11,35)";

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
                   String discount = rs.getString("SC18009");
                   double price = rs.getDouble("price");
                   String date = rs.getString("paev");
                   MatrixLine mline = new MatrixLine();

                   if (custID != null && custID.length() > 0) {
                	   mline.setCliGroup(custID.trim());
                	   mline.setDiscGroup(custID.trim());
                   }
                   if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                    // mline.setCliGroup(clGroup.trim());
                   }

                   
                   if(date != null){
                  	 mline.setEndDate(date.substring(0,10));
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
                  	// e.printStackTrace();
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
                  result.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
                 }
                 
                 String sql4 = "Select  SC08002 as custid,SC08003 as prodgr,SC08004 as discount  from SC080100 where" +
                 		"  CONVERT(char(8), getdate(),112)  BETWEEN  SC08005 AND SC08006 ";
                 rs = st.executeQuery(sql4);

                 while (rs.next()) {

                   String custID = rs.getString("custid").trim();
                   String prodGR = rs.getString("prodgr").trim();
                   double discount = rs.getDouble("discount");       
     			   
                   
                
                   MatrixLine mline = new MatrixLine();
                 
                   if (custID !=null && custID.trim().length() > 0) {
                     mline.setCliGroup(custID.trim());
                   }

                   if (prodGR != null && prodGR.trim().length() > 0) {
                     mline.setProdGroup(prodGR.trim());
                   }
       
                   if (discount != 0) {
                     mline.setDiscount(discount);
                   }
                  result.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
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

	private String getDirAbi() {
		String dir = getParent().getProps().getProperty("readabi");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

	private String getDirHind() {
		String dir = getParent().getProps().getProperty("readhind");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}


    public TreeMap getSort() {
		TreeMap result = new TreeMap();
		File f = new File(this.getDir() + "Listingud.xls");
		try {
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("tarnekliendid");
			HSSFRow row = null;

			// System.out.println(groups.toString());

			for (int i = 3; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String custID = "";
				//System.out.println(row);
				if (row != null && row.getCell((short) 1) != null) {

					try {
						custID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						custID = (row.getCell((short) 1).getStringCellValue());
					}
					
                       if(!custID.equals("0")){
                    // Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
                    		try {
			     	    	  String text =  row.getCell((short) 0).getStringCellValue() ;
				    		  result.put(custID.trim(),text);  
				    		} catch (Exception e) {
                    		 e.printStackTrace();
    						}
					
                    		
					
				}
			}}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}



    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        connectionUp2(url);
        TreeMap sort = getSort();
        HashMap lisainfo = new HashMap();
        Vector kysi = new Vector();
        StringBuffer sb2 = new StringBuffer();
        String sep = ";";

        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45005 AS ale,SL01036 AS ale2 ,  " +
       "   SL01053 AS visit, ' ' AS info ,SL01035 as agent,SL22005 as paym , SL01056 as klgrupp, SL01075 as block ,SL01056 as payer ,SL14007 as delday " +
       "  ,(SELECT SL01037 from SL010100 where SL01001=kl.SL01056) as limiit ,vr.day+' ' + vr.DESCR as veoring ," +
       "   (SELECT  SL01052 from SL010100 where SL01001=kl.SL01056) as leping " + 
       "   FROM SL010100 kl left JOIN SC450100    ON SL01036=SC45003  " +
	   "   LEFT JOIN veoring0100 vr on vr.VR=SL01027    LEFT OUTER JOIN SL140100 ON SL01001=SL14001 AND SL14002='00' " +
       "   LEFT OUTER JOIN SY240100 ON SL01035=SY24002 AND SY24001='BK' " +
       "   LEFT OUTER JOIN SL220100 ON SL01024=SL22002 AND SL22001='0'  " +
       "   WHERE len(SL01001)>4 and SL01099 not like '%EI TOIMI%' ORDER BY SL01002"; 

        connectionUp(url);	
        log(cmd);
           
        	
           String  sql = "	 select SL01001 as klient,CEOname as boss ,CEOtel as bosstel,ACCTname as book," +
 			"ACCTtel as booktel ,CONTACTname as nimi,CONTACTtel as tel,OTHERname , OTHERtel , LastUpdate  from CUSTOMERDATA0100";
             
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
                	String book  = rs.getString("book");
                	String booktel  = rs.getString("booktel");
                	String nimi  = rs.getString("nimi");
                	String tel  = rs.getString("tel");
                	Date lastd  = rs.getDate("LastUpdate");
                	
                	if(nimi != null && nimi.trim().length() >0){
                		res = res + "Kontakt: " + nimi ;
                	}
                	if(tel != null && tel.trim().length() >0){
                		res = res + " " + tel +"|" ;
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
                	
                	
                	
                	if(boss != null && boss.trim().length() >0){
                		res = res + "CEO: " + boss ;
                	}
                
                	if(bosstel != null && bosstel.trim().length() >0){
                		res = res + " " + bosstel +"|" ;
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
            
            System.out.println("lisainfo " + lisainfo.size());
            
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
                cust.setAddress(rs.getString("aadress").trim());
                  nimi = nimi + " " + cust.getAddress();
                
                cust.setName(nimi);
                
                
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
                if(vis.trim().length() > 0 ){
                	//System.out.println(vis);
                    cust.setVisit(vis.trim());	
                }
                
                String agen = rs.getString("agent");
                if(agen != null){
                  //cust.addAgent(new Agent(agen.trim()));
                  cust.addAgent(new Agent("VTI"));
                  cust.addAgent(new Agent("EGN"));
                  cust.addAgent(new Agent("KAD"));
                  Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
                  sb2.append(cust.getID() + sep + agen +sep);
                  
                  String agentName = null;
                  if(agg != null){
                	  agentName = agg.getName();
                  }
                  if (agentName != null) agentName = agentName.trim();
                  if (agentName == null) agentName = " ";
                  sb2.append(agen + " " +agentName + "\n");
                  
                  cust.setCity("KLIENDIAGENT: " + agen +  " " + agentName);
  				if(agg != null && agg.getDubbler().size()>0){
  					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
  						cust.addAgent(new Agent((String)((Agent)this.getAgents1().get(agen.trim())).getDubbler().elementAt(i)));
  					}
  				}
  				
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
                	 cust.setGroup((String)sort.get(cust.getID()));
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
                
                String limiit = rs.getString("limiit");
                if(limiit != null && limiit.trim().length()>0 && limiit.trim().length()<8){
                	cust.setCredit(limiit.trim());              	
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
                    	cust.setInfo("Lepingu kuup�ev aegunud |" + leping.trim()  + "|");   	
                	}
                	if("ERROR".equals(lep)){
                    	cust.setInfo("Lepingu kuup�ev puudub v�i vigane|");   	
                	}
                }else{
                	cust.setInfo("Lepingu kuup�ev puudub v�i vigane|");  
                }
                
                
                if(lisainfo.get(cust.getID())!= null){
                	String lisa = (String)lisainfo.get(cust.getID());
                	while(lisa.endsWith("|")){
                		lisa = lisa.substring(0,lisa.length()-1);
                	}
                //	System.out.println(cust.getID() + " " + (String)lisainfo.get(cust.getID()));
                	if(hi){
                    	cust.setPersonalData(veoring.trim() + "|" +lisa);	
                	}else{
                    	cust.setPersonalData(lisa);
                	}
                }
                
                if(cust.getPersonalData()== null || cust.getPersonalData().trim().length() == 0){
                	if(hi){
                //		System.out.println("Veoring " +veoring);
                		cust.setPersonalData(veoring.trim());
                	}
                }
                		String info= cust.getInfo();
                		if(info == null){
                			info = "";
                		}else{
                			info = info + "|";
                		}
                if(kysi.contains(cust.getID())){
                	cust.setInfo(info + "Vajalik kontrollida ja vajadusel|sisestada (muuta) kontaktandmed|ava tipit�pi->Detailne info");
                }
                	
                //otsime v�lasumma igale kauplusele 
                
                String sql6 = "SELECT SL01001 AS id,SUM((SL03013-SL03053)/(case when SL03015 = 0 then 1 else 15.6466 end)) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
                " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01001='" + cust.getPayer()+ "'  and SL01001 not in ('0634','1682','0104','1901','1986') GROUP BY SL01001 ";

   Statement stm = con2.createStatement();

   ResultSet rs3 = stm.executeQuery(sql6);
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
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();

            if(sb2.length() > 2){
                File f = new File(getDirOut() + "clientagent.txt");
                if (f.exists())f.delete();
                  try {
            		PrintWriter os = new PrintWriter(new FileWriter(
            				getDirOut() + "clientagent.txt", true));
            		os.write(sb2.toString());
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
    
	private String getDirOut() {
		String dir = getParent().getProps().getProperty("writeout");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
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
                   rs.getString("nimetus").trim() +" " + kood);
        }
        
        tm.put("RRR","Reklaammaterjal RRR");
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
                   "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5  and SL01001 not in ('0634','1682','0104','1901','1986') ";
      sql += " ORDER BY SL01001";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
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

        Hashtable aksiis = getAksStock();
//'PR' +
        String cmde =" SELECT  RTRIM(SC01001) AS toode,( RTRIM(SC01002) " + //right(SC01128,2) + ' ' +
               " +rtrim(SC01003)) + RTRIM(SC01001) AS nimi,SC39004 AS myygihind ,SC01009 AS tykke,SC03003-SC03004 AS stock, SC01018 AS maxale,' ' AS pandikas , " +  
    " case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 as grupp1 ,SC01128 AS grupp2,SC01067 as riik ,SC01132 AS ean  , SC01016 as letter " +
    " FROM SC010100 left outer JOIN SC030100 on SC03001=SC01001 and SC03002='01' AND SC03003!=0 left outer JOIN SC390100 ON SC01001 " +
    " = SC39001 and SC39002='00' " +
    "    WHERE   LEN(LTRIM(SC01128))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) not in ( 'S' ,'U') ORDER BY SC01001 " ;
        
        String cmd2 = "select SC33001 as toode ,sum(SC33005) as kogus from SC330100 where   SC33005 >0 and SC33002='01' and SC33010 = 0  group by SC33001";
        
        
       String cmd = " (SELECT  RTRIM(SC01001) AS toode,( RTRIM(SC01002)  "+
            "    +rtrim(SC01003))  AS nimi,SC39004 AS myygihind ,SC01009 AS tykke,SC03003-SC03004 AS stock, SC01018 AS maxale,' ' AS pandikas ,   " +
      "case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 as grupp1 ,SC01128 AS grupp2,SC01067 as riik ,SC01132 AS ean  , SC01016 as letter " +
    " FROM SC010100 left outer JOIN SC030100 on SC03001=SC01001 and SC03002='01' AND SC03003!=0 left outer JOIN SC390100 ON SC01001 " +
    " = SC39001 and SC39002='00' " +
    "    WHERE   LEN(LTRIM(SC01128))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) != 'S' " +
" union " +
 " SELECT  'XX' +RTRIM(SC01001) AS toode, 'zz ' + ( RTRIM(SC01002)+' '  " +
   "             +rtrim(SC01003)) + ' ' +RTRIM(SC01001) AS nimi,1 AS myygihind ,SC01009 AS tykke,SC03003-SC03004 AS stock, SC01018 AS maxale,' ' AS pandikas ,  " +
    " case when  SC01031 = 1 then 1 else 0 end AS out ,'RRR' AS grupp1,''as grupp2,SC01067 as riik ,SC01132 AS ean  , SC01016 as letter " +
    " FROM SC010200 left outer JOIN SC030200 on SC03001=SC01001 and SC03002 in ('01','02') AND SC03003!=0 " +
    "    WHERE  SC03003-SC03004 >0  ) order by toode";
      
        
	if (isDebug()) {
	    System.out.println(cmd);
	}

	
    
    
    String sql3 = " SELECT SC18002 AS prodid," +
    " SC18016 AS discount " +
  "   FROM SC180100 INNER JOIN SC010100 on SC01001=SC18002 WHERE  " +
   "  SC18001='E'  and rtrim(ltrim(SC01016)) != 'S' " +
   " order by SC18002";
	TreeMap products = new TreeMap();

	try {

	
    Properties props = EksportFrame.loadProps(getDirAbi() + "Lisainfo.txt");
	System.out.print( "Propsid laiali" + getDirAbi() + "Lisainfo.txt" +" " + props.toString());
	
	String maxdisc = getParent().getProps().getProperty("maxdiscount");

    TreeMap aktiiv = new TreeMap();

    Statement  st = con.createStatement();
    ResultSet	      rs = st.executeQuery(cmd2);
	    while (rs.next()) {
	    	String kood = rs.getString("toode").trim();
	    	double kogus = rs.getDouble("kogus");
	    	aktiiv.put(kood, kogus + "");
	    }
			
	    
	    rs = st.executeQuery(cmd);
	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim();
		
                Product prod = new Product(kood);
               
                if(maxdisc != null){
                	prod.setMaxDiscount(Double.parseDouble(maxdisc));
                }
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
               prod.setStock(rs.getDouble("stock"));
               
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
              //    nimetus = nimetus + " "+ gr.trim().substring(gr.trim().length()-4); 
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
                 
                 if(letter != null && letter.trim().length()>0 && letter.trim().equals("X")){
                	 nimetus = nimetus + " *X*";  
                	// System.out.println("On olemas X  " +  nimetus);
                 }
                 String add = "      ";
                 if(props.getProperty(prod.getID()) != null && props.getProperty(prod.getID()).trim().length() > 0){
                	// add =    "    " + props.getProperty(prod.getID()) + "_"; 
                	 prod.setOldName(props.getProperty(prod.getID()));
                 }
                  nimetus = addSpaces(kood ,14) + "_" + nimetus;
                
                     add = add + "" + nimetus;
                 
                // nimetus = add + "__" + prod.getEAN() + "__" ;
                 
                 
                 
                 prod.setName(add + "__" + prod.getEAN() + "__");
              prod.setVat("21"); 
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

    
    public static final String addSpaces(String s, int totalLen){
  	    int len = s.length();
  	    StringBuffer sb = new StringBuffer(s);
  	    for (int i=len; i < totalLen ; i++){
  	      sb.append('_');
  	    }
  	    if( len > totalLen){
  	    	return s.substring(0,totalLen);
  	    }
  	    return sb.toString();
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
         "   FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 and left(SL01001,2) !='M3' " +
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

            
           // Siin lisame listingud mida pole myydud aga on l�bi summeerimis koodi vaja lisada
            
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
                     "  FROM ST030100 INNER JOIN SL010100 ON ST03001 = SL01001 WHERE ST03015> getdate()-180 AND ST03016>0 AND ST03020>0 " +				 
					 "  group by SL01001,ST03017,ST03015 " +
                     "  ORDER BY SL01001,ST03017 ,ST03015 DESC ";
  System.out.println(cmd2);
   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

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
        System.out.println("CustProd l�peb");
	return custProds;
    }


    public String getPilotDayData(Agent agent){
        String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020100  " +
        " LEFT JOIN OR200100 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
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
   sb.append(sum + "\n" + sep);
 }
} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();

System.out.println(sb.toString());
return sb.toString();

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


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
         String cmd = " select ST03007 as agent, sum(ST03021*ST03020*(100-ST03022)/100) as sales , " +
			" (sum(ST03021*ST03020*(100-ST03022)/100) - sum(ST03023*ST03020)) as omv " +
			"   from ST030100  where MONTH(ST03015)=  " +
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
       
    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "ScalaPrikeEstCC";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:reportEST";
    private Connection	       con = null;
    private Connection	       con2 = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
  
	private TreeMap agents1 = new TreeMap();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

