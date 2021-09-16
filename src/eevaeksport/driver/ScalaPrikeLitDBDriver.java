/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ScalaPrikeLitDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *Tere Karl  
     *
     * @param pare
     *
     * @see
     */
    public ScalaPrikeLitDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}

	
	if(getParent().getProps().getProperty("db.url") != null) {
   	 url = getParent().getProps().getProperty("db.url");//"jdbc:odbc:scalaDB";	
   }
	   user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	    if("true".equals(getParent().getProps().getProperty("girteka.stock"))){
	    	girteka = true;	
	    	System.out.println("girteka");
	    }
	    
	    
     }
    
    boolean girteka = false;
	  String user =  null;
	  String pass = null;
	  String rcompany = null;
    
	  
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
		//	System.out.println("Kokku firmasi :" + res.size());
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
				e.printStackTrace();
				
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

		       StringBuffer products = new StringBuffer();
        	  if(girteka){
        		TreeMap gir =  getGirStock();
        	Iterator itera = 	gir.keySet().iterator();
        	while(itera.hasNext()){
        		String prodcode = itera.next() + "";
        		String kogus = gir.get(prodcode) + "";
        	       products.append(prodcode +  ";");
	               products.append(" ;");
	               products.append(kogus + "; "  +  "\n"); // + dealer +  "\n");
	           
        	}
        	  }else{
        		  
        	  
        		  
        	  
        	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , '01' AS LADU ,SC03003-SC03004 AS stock   " +
		      "  FROM SC030500  WHERE SC03002='01' AND (SC03003-SC03004)!=0 ORDER BY SC03001" ;


		       if (isDebug()) System.out.println(cmd);
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
		             }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
        	  }
		       return products.toString();
		   }
        
        public TreeMap getBROStockInfo(){
      	  connectionUp(url);//'PR' + 
      	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , 'BROKAS' AS LADU ,SC03003-SC03004 AS stock   " +
		      "  FROM SC030500  WHERE SC03002='BROKAS' AND (SC03003-SC03004)!=0 ORDER BY SC03001" ;

TreeMap res = new TreeMap();
		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           if(seis > 0){
		             res.put(kood, seis);
		           
		           }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return res;
		   }
      


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }


        public String getDistribution() {
          return null;
        }

    	private String getDir() {
    		String dir = getParent().getProps().getProperty("readin2");
    		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
    			dir = dir + "/";
    		return dir;
    	}
        
        public Vector getFixedAssortmentData() {
    		Vector result = new Vector();
    		File f = new File(this.getDir() + "Listing.xls");
    		try {
    			FileInputStream ff = new FileInputStream(f);
    			HSSFWorkbook workbook = new HSSFWorkbook((ff));
    			
    			
    			HSSFSheet sheet = workbook.getSheet("shops");
    			HSSFRow row = null;
    			for (int i = 1; i < sheet.getLastRowNum(); i++) {
    				row = sheet.getRow(i);
    				String sort = null;
    				try {
						 sort = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						sort = (row.getCell((short) 0).getStringCellValue());
					}
					
					String shop = null;
    				try {
    					shop = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						shop = (row.getCell((short) 1).getStringCellValue());
					}
					
					if(sort != null && sort.trim().length() > 0 && shop != null && shop.trim().length() > 0 ){
						this.sortCodes.put(shop.trim(), sort.trim());
					}

    			}
    				
    			
    			
    			 sheet = workbook.getSheet("listing");
    			 row = null;
    			TreeMap groups = new TreeMap();
    			row = sheet.getRow(0);
    			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 4; a < row.getLastCellNum(); a++) {
    				try {
    				//	 System.out.println(row.getCell((short)a).getStringCellValue());
    					groups.put(a + "", row.getCell((short) a)
    							.getStringCellValue());
    				} catch (Exception e) {
    					// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    			}

    			for (int i = 2; i < sheet.getLastRowNum(); i++) {
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
                        		
                           		
          				                  	
    					// Siin läheme reasisse rokkima ja vaatama kas se toode
    					// läheb kaasa
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

    							if (go != null && go.toLowerCase().equals("x")) {

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

    public TreeMap getProdGroups() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240500 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood=  rs.getString("liik").trim();
            tm.put(kood,
                   rs.getString("nimetus").trim() +" " + kood);
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }
    
    
    public TreeMap getProdGroupsSecond() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " select SC01067 as riik FROM SC010500 WHERE   LEN(LTRIM(SC01128))>0 ";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood =  rs.getString("riik").trim();
            tm.put(kood , kood);
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

    public TreeMap getAgents() {
        connectionUp(url);
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240500 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();
	updateCustcodes();
	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agent.setIsPilot(true);
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
        
         return prlines;
       }






    public TreeMap getCustomers(Agent agent){
    	
    	  Converter.getManagersReplacements(getAgents1(), rcompany, user , pass);
        connectionUp(url);
        connectionUp2(url);
       // System.out.println("sort kodes " + this.sortCodes.size());
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 , " +
         "  ' ' AS visit, ' ' AS info ,SL01035 as agent,SL22005 as paym , SL01029 as klgrupp, (SELECT SL01060 FROM SL010500 WHERE SL01001=k.SL01056) as block ,"
         + " SL01060 as block2 ,SL01056 as payer ,SL14007 as delday " +
         ", (SELECT SL01037 from SL010500 where SL01001=k.SL01056) as limiit  FROM SL010500 k left JOIN SC450500  " +
		 "  ON SL01036=SC45003    LEFT OUTER JOIN SL140500 ON SL01001=SL14001 AND SL14002='00'" +
         " LEFT OUTER JOIN SY240500 ON SL01035=SY24002 AND SY24001='BK'" +
         " LEFT OUTER JOIN SL220500 ON SL01024=SL22002 AND SL22001='0' " +
         " WHERE len(SL01001)>4 ORDER BY SL01002";//WHERE ( SL01002 != ''  AND SL01129 != '') or SL01129 = '0100000' 

        connectionUp(url);
        String cmd3 = "SELECT SL01056 as pea   FROM SL010500 left JOIN SC450500   ON SL01036=SC45003 " +
                  "  WHERE SL01002 != '' AND SL01035 != ''  and SL01056 != '' ORDER BY SL01002";
        
        String cmdtara = "SELECT  ST03001 AS klient ,sum(ST03020) AS Kogus FROM ST030500 WITH(NOLOCK) WHERE ST03017 like '%GVT%' GROUP BY ST03001";
	
      log(cmd);

	Vector vec = new Vector();

   Statement st = null;
   ResultSet	      rs = null;
	try {   
		st = con.createStatement();
	      rs = st.executeQuery(cmd3);
	    while(rs.next()){
	    	vec.addElement(rs.getString("pea").trim());
	    }
	    rs.close();
	    st.close();
	}catch (Exception e){
		e.printStackTrace();
	}
	 TreeMap taraSaldo = new TreeMap();
	
	try {   
		st = con.createStatement();
	      rs = st.executeQuery(cmdtara);
	    while(rs.next()){
	    	taraSaldo.put(rs.getString("klient").trim(), (int)rs.getDouble("kogus") + "");
	    }
	    rs.close();
	    st.close();
	}catch (Exception e){
		e.printStackTrace();
	}
	
	
	  //  System.out.println("vecsize " + vec.size());
	// vektoris on klindid kellel on peaklient mï¿½rgitud ja neile ei pane kaasa allahindlust
	
        String    sql = "SELECT SL01008 AS custid,'' AS clgroup,SCP6001 AS prodid,'' AS prodgroup,SCP6009, \r\n" + 
        		"        	(case when SY24003=null then '0' else SY24003 end ) AS discount,SCP6008 AS price, '' as delday  \r\n" + 
        		"           FROM SCP60500 INNER JOIN SL010500 ON  SCP6002= SL01001 LEFT JOIN SY240500 on SCP6009=SY24002 and SY24001='BG' WHERE \r\n" + 
        		"          getdate() BETWEEN  SCP6010 AND SCP6011   and len(SL01008) >0 \r\n" + 
        		"          order by SL01008, SCP6002";

        
          try {
           
            st = con.createStatement();
             rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
              String sss = rs.getString("SCP6009");
              String discount = rs.getString("discount");
              if(discount == null){
              	discount = sss.trim();
              }else{
              	discount = discount.trim().replaceAll("%"," ").trim();
              }
			   
              double price = rs.getDouble("price");
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup.trim());
              }
              if(discount.trim().length()==0){
              	discount = "0";
              }
                mline.setDiscount(Double.parseDouble(discount));

              if (Double.parseDouble(discount) == 0 && price != 0) {
                mline.setPrice(price);

              }
              if(!vec.contains(custID.trim())){
                  prlines.add(mline);   
              }
            }

            // siisn lï¿½hevad ainult need kilendid kellel pole peaklienti
            
            rs.close();
            
            
            String sqq = "    SELECT  SCP6002 AS clgroup, SCP6001  AS prodid, SCP6008 AS price  " + 
            		"        FROM SCP60500 WHERE   getdate() BETWEEN  SCP6010 AND SCP6011 " ;
  
            
            rs = st.executeQuery(sqq);

            while (rs.next()) {

                String clGroup = rs.getString("clgroup").trim();
                String prodID = rs.getString("prodid").trim();
  	   
                double price = rs.getDouble("price");
                MatrixLine mline = new MatrixLine();
      
                if (clGroup != null && clGroup.trim().length() > 0) {
                    mline.setCliGroup(clGroup.trim());
                  }
                
                if (prodID != null && prodID.trim().length() > 0) {
                  mline.setProdID(prodID.trim());
                }

                if (price != 0) {
                    mline.setPrice(price);
                }
               
                    prlines.add(mline);   
                
            }
            
            //Siin panime ï¿½ra PEAKLIENDI SOODUKAD ja kasutame seal pricergroupi
            
            
            
            String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
            " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
            " SC39005 else SC39004 end AS price  " +
            " FROM SC390500 WHERE SC39002 != '00' order by SC39002,SC39001 ";
            rs = st.executeQuery(sql2);
/*
select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001)) + '03'  AS prodid,' ' AS prodgroup, 
             

case when CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN SC39006 and SC39007 then 
             SC39005 else SC39004 end AS price  
             FROM SC390100 WHERE SC39002 != '00' order by SC39002,SC39001 */
            while (rs.next()) {

              String clGroup = rs.getString("clgroup");
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
             prlines.add(mline);  // Kommentisin vï¿½lja kuniks uus hinna skeem hakkab toimima
            }

            rs.close();
            st.close();
            
            
            
         //   System.out.println("prlines " + prlines.size());
            
            
            
            
            
            
            
            connectionDown();
          }
          catch (SQLException ex) {
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
	   	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(rs.getString("aadress").trim()),"Lihuania"));
                nimi = nimi + " " + cust.getAddress();
                
                cust.setName(nimi);
                
                
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                 // cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	//cust.setDiscount(17);
                	
                }
                cust.setVisit(rs.getString("visit").trim());
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
                
                Agent agg = ((Agent)this.getAgents1().get(agen.trim()));
				
				if(agg != null && agg.getDubbler().size()>0){
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						String iid= (String)((Agent)this.getAgents1().get(agen.trim())).getDubbler().elementAt(i);
						if(!cust.hasAgent(iid)){
    						cust.addAgent(new Agent(iid));	
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
             
                cust.setGroup(rs.getString("klgrupp"));
                
                String block  =  rs.getString("block");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("2")){
                		cust.setSalesBlock(true);
                	}
                	if(block.trim().equals("1")){
                		cust.setSalesBlockT(true);
                	}
                	
                	block  =  rs.getString("block2");
                    if(block != null && block.trim().length()>0){
                    	if(block.trim().equals("2")){
                    		cust.setSalesBlock(true);
                    	}
                    	if(block.trim().equals("1")){
                    		cust.setSalesBlockT(true);
                    	}
                     }
                	
                }
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer); 
                	
                }
                
              if(taraSaldo.get(cust.getID()) != null) {
            	  cust.setInfo("Tara Saldo " + taraSaldo.get(cust.getID()) +  " kegs");
              }

                String delday = rs.getString("delday");
                if(delday != null && delday.trim().length()>0){
                	cust.setPersonalData(delday.trim().replaceAll(";", ":"));              	
                }
                //otsime võlasumma igale kauplusele 
                
                String sql6 = "SELECT SUM( case when SL03006<=getdate() then SL03013-SL03053 else 0 end ) AS debtover,"
                		+ "SUM(SL03013-SL03053) AS debt , max(datediff(dd, SL03006,getdate())) as days"
                		+ " FROM SL030500 INNER JOIN SL010500 ON SL03001 = SL01001  WHERE  " +
                " NOT(SL03013=SL03053) and SL01001='" + cust.getPayer() +  "'  GROUP BY SL01001 having SUM(SL03013-SL03053) != 0";

   Statement stm = con2.createStatement();
   ResultSet rs3 = stm.executeQuery(sql6);

   while(rs3.next()){    
	   double debtover = Double.parseDouble(rs3.getString("debtover").trim());
       double debt = Double.parseDouble(rs3.getString("debt").trim());
     
       cust.setDebt(cust.getDebt() + debtover);
       cust.setUsedCredit(debt);
       
       if (rs3.getInt("days") > 30 && debt > 50){
    	   cust.setSalesBlockT(true);
        } else {
    	   cust.setSalesBlockT(false);
       }
   }
   
   rs3.close();
   stm.close();
   
   String limit = rs.getString("limiit");
   if(limit != null && limit.trim().length() != 0){
   	double limiit = Double.parseDouble(limit.trim());
   	if(limiit > 0) {
   		cust.setCredit(limit.trim());
   	}
   		
   		if(limiit < cust.getUsedCredit()){
   			//cust.setSalesBlock(true);
   			//cust.noOrder = true;
   			}
   	}
   
   
                
   if(cust.getDebt()<0.02 && cust.getDebt() > 0 ){
   	   cust.setDebt(0);
     }    
   
   
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();



        } catch (Exception e) {
            getParent().showError(e);
        }

        
	return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa , -datediff(day ,getdate(),SL03006) as yle " +
      		"FROM SL030500 INNER JOIN SL010500 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                   "LEN(LTRIM(SL03013))>0 "; // AND SL03006<GETDATE()+5 
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
    
    
    
    private TreeMap getLinks (){
        
    	TreeMap res = new TreeMap();
     	File f = new File(this.getExcelDir() + "links.txt");
     	try{
     		InputStreamReader is = new FileReader(f);
    		BufferedReader br = new BufferedReader(is);
    		String rida = null;
    		while ((rida = br.readLine()) != null) {
    			StringTokenizer st = new StringTokenizer(rida,";");
    			res.put(st.nextToken().trim(),st.nextToken().trim());
    		}	
     	}catch(Exception e){
     		//e.printStackTrace();
     	}
     	
     	System.out.println("getLinks size : " + res.size());

     	
     	return res ;
    }
    
    
	private Node getNode(Node node, String name) {
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node nd = node.getChildNodes().item(i);
			// System.out.println(nd.getNodeName());
			if (nd.getNodeName().equals(name)) {
				return nd;
			}
		}
		return null;
	}

	
	private FileInputStream getCleanInputStream(File xmlFile)
			throws FileNotFoundException, IOException {
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
// System.out.println("Skipping BOM, " + bomKount + " bytes");
return bomKount;
}
   
	public static String deCode(String res) {

		while (res.indexOf("11111xx") != -1) {
			int aa = res.indexOf("11111xx");
			res = res.substring(0, aa) + "ä"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11112xx") != -1) {

			int aa = res.indexOf("11112xx");
			// System.out.println(aa + " " +res);
			res = res.substring(0, aa) + "ö"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11113xx") != -1) {
			int aa = res.indexOf("11113xx");
			res = res.substring(0, aa) + "ü"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11114xx") != -1) {
			int aa = res.indexOf("11114xx");
			res = res.substring(0, aa) + "õ"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11115xx") != -1) {
			int aa = res.indexOf("11115xx");
			res = res.substring(0, aa) + "Ä"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11116xx") != -1) {
			int aa = res.indexOf("11116xx");
			res = res.substring(0, aa) + "Ö"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11117xx") != -1) {
			int aa = res.indexOf("11117xx");
			res = res.substring(0, aa) + "Õ"
					+ res.substring(aa + 7, res.length());
		}
		while (res.indexOf("11118xx") != -1) {
			int aa = res.indexOf("11118xx");
			res = res.substring(0, aa) + "Ü"
					+ res.substring(aa + 7, res.length());
		}

		return res;
	}
	
  private TreeMap braakGir = null;
  private TreeMap nepaGir = null;
  
	
   private TreeMap getGirStock(){
		
	   TreeMap<String,Double> res = new TreeMap<String,Double>();
	   braakGir = new TreeMap();
	   nepaGir = new TreeMap();
   	String fname = this.getExcelDir() + "GirtekaStockUTF8.xml";

	try {   	
    File f = new File ( fname);
	   
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     DocumentBuilder db = dbf.newDocumentBuilder();      	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
    Document doc = db.parse(in,handler);
    Node root = getNode(doc, "xml-message");      
   
	Node read = getNode(root, "item-list");
	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
    for (int s = 0; s < read.getChildNodes().getLength(); s++){
    
   	 Node rida = read.getChildNodes().item(s);
    		 if(rida.getNodeName().equals("item")){
    		
    		try {
    			String busena = deCode( getNode(rida,"Busena").getFirstChild().getNodeValue());
    			 String prodcode = deCode( getNode(rida,"PrekId").getFirstChild().getNodeValue());
    			  double kogus = Double.parseDouble(deCode( getNode(rida,"Kiekis").getFirstChild().getNodeValue()));
    			  String sandelis = deCode( getNode(rida,"Sandelis").getFirstChild().getNodeValue());
    			  
    			//  1 – Tavaline laoseis mida on näha kohe sellel tootereal -  
                 // Laoseis saadakse ainult ladude LK1 ja NEAT ning ainult nende staatuste „Paruošta KL“ ja „Paruošta NB“ korral vastavaid laoseise kokku liites:
  
    			  if( (busena.equals("Paruošta NB") || busena.equals("Paruošta KL"))
    					  && (sandelis.equals("LK1") || sandelis.equals("NEAT")) ){ //|| ladu.equals("Grąžinimas")
    					if(res.get(prodcode) != null){
    	               	   res.put(prodcode, res.get(prodcode) + kogus);
    	                }else{
    	               	   res.put(prodcode, kogus);
    	                }
    	            }
    			  
    			//  3 – In brokas warehouse; näha ainult rohelise tooteinfo nupu   vajutamisel avanevas aknas.
    			 // Näidatakse ainult laos „BROKAS“ olevate toodete laoseis olenemata toote staatusest.

    			  
    			  if(sandelis.equals("BROKAS")){
    					if(braakGir.get(prodcode) != null){
    	        			braakGir.put(prodcode, Double.parseDouble(braakGir.get(prodcode)+"") + kogus);
    	                }else{
    	                	braakGir.put(prodcode, kogus);
    	                }
    	            }
    		
    			  
    		//	  Not prepared to sell; näha ainult rohelise tooteinfo nupu   vajutamisel avanevas aknas.
             //     Näidatakse ainult staatustega „Neparuoštas Aktas“ ja „Neparuošta“ tooteid olenemata lao tähisest ehk siis üle kõikide ladude.

    			  if(busena.equals("Neparuošta") || busena.equals("Neparuoštas Aktas")){
  					if(nepaGir.get(prodcode) != null){
  	        			nepaGir.put(prodcode, Double.parseDouble(nepaGir.get(prodcode)+"") + kogus);
  	                }else{
  	                	nepaGir.put(prodcode, kogus);
  	                }
  	            }
              }catch(Exception e) {
    			e.printStackTrace();
    		}
    		    }       		
		    }
	       
	        
	    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //Read the spreadsheet that needs to be updated
	
	System.out.println("XLS laoseisu suurus " + res.size() + " " + res.get("AW500T"));
		return res;
		
 }
 
   

    public TreeMap getProducts(Agent agent){
        connectionUp(url);
        TreeMap links= getLinks();
//'PR' +
        String cmd =" SELECT  RTRIM(SC01001) AS toode,(RTRIM(SC01002)+' ' " +
               " +rtrim(SC01003)) + ' ' +RTRIM(SC01001) AS nimi,SC39004 AS myygihind ,SC01009 AS tykke, SC03004 ,(SC03003-SC03004) AS stock, SC01011 AS maxale,SC01150 AS pandikas , " +  
    " case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 AS grupp,SC01132 AS ean  , SC01016 as letter , SC01067 as gr2  ,SC01161 as statustop" +
    " FROM SC010500 left outer JOIN SC030500 on SC03001=SC01001 and SC03002='01' AND (SC03003-SC03004)!=0 left outer JOIN SC390500 ON SC01001 " +
    " = SC39001 and SC39002='00' " +
    "    WHERE   LEN(LTRIM(SC01037))>0  and rtrim(ltrim(SC01016)) != 'S' ORDER BY LEFT(RTRIM(SC01037),1),SC01001 " ;
	
        //and SC39004 > 0
        if (isDebug()) {
	    System.out.println(cmd);
	}

      
        
        TreeMap gir =  getGirStock();
        TreeMap bro = braakGir;
        TreeMap nepa = nepaGir;
    	
        
	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim();
	     Product prod = new Product(kood);
		String info = "";
		if(bro.get(kood) != null){
			String aa = "";
			
			try{
			  aa =	" @br:" + (int)Double.parseDouble(bro.get(kood)+"")	;
			}catch(Exception e){
			  e.printStackTrace();	
			}
			info = "Brokas: "+(int)Double.parseDouble(bro.get(kood)+"")+ "|"; 
			System.out.println("braak " + bro.get(kood) + " " + aa);
			
			nimetus = nimetus + aa;
		}
		
		if(nepa.get(kood) != null){
			
			info = info + "Neparuošta: "+(int)Double.parseDouble(nepa.get(kood)+""); 
			System.out.println("nepa " + nepa.get(kood) );
			
		}
           
		if(info != null && info.length()>1) {
			prod.setPartii(info);
		}
        
                prod.setName( nimetus );
                try{
                    prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
                	
                }catch(Exception k){
                	
                }
                prod.setStock(0);
                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                prod.setStock(rs.getDouble("stock"));
                if(girteka){
                  if( gir.get(prod.getID())!= null ){
                	  double rr = Double.parseDouble("" +gir.get(prod.getID()));
                	  if(rr != 0) {
                		  prod.setStock(rr);   
                	  }
                
                	//  System.out.println("Setin stocki " + prod.getID() + " " + prod.getStock());
                  }
                }
              //  prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                

                String pant = rs.getString("pandikas");
                if(pant != null && pant.trim().length()>0){
                  prod.setPandPakendiKood(pant.trim());
                }
                String out = rs.getString("out");
                if(out != null && out.trim().equals("1")){
                //  prod.setStatusSellout("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>4){
                  prod.setEAN(gr.trim());
                  prod.setName( nimetus  + " " + gr.trim().substring(gr.trim().length() - 4));
                }
                
                String letter = rs.getString("letter");
                
                boolean ok = true;
                 if(letter != null && letter.trim().length()>0 && letter.trim().equals("X") && prod.getStock() <= 0){
                  ok = false;
                //  System.out.println("Välja Pole laos " +  prod.getName());
                }
                 
                 gr = rs.getString("gr2");
                 if(gr != null && gr.trim().length()>0){
                   prod.setGroupSecond(gr.trim());
                 }
         
                 if(links.get(prod.getID()) != null){
        	 		 prod.weblink = links.get(prod.getID()) + "";
        	 	 }
                
                 String statustop = rs.getString("statustop");

                 if(statustop != null && statustop.trim().length()>0 && (statustop.trim().toLowerCase().equals("yes") || 
                		 statustop.trim().toLowerCase().equals("y"))){
                	 prod.setStatusTop("1");  
                	// System.out.println("On olemas X  " +  nimetus);
                 }
                 
              prod.setVat("21"); 
              if(ok){
  		        products.put(nimetus, prod);  
              }

	    }
            rs.close();
            st.close();
	} catch (Exception e) {
	    getParent().showError(e);
	    System.exit(0);
	}

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
        connectionUp(url);
	/*
   select "Customer Code" AS klient ,"Stock Code" AS toode,Qty AS kogus from st02 where"Salesman"='45'
AND "Invoice order date">'20021101' AND "Actual Del  Date">'00000000'
 group by "Stock Code","Customer Code",qty  order by "Customer Code","Stock Code","Invoice order date" */

     /*   SELECT  ST02001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015
         FROM ST020100 WHERE ST02015>? AND ST02016>0 AND ST02020>0 GROUP BY ST02017,ST02001, ST02020,ST02015 ORDER BY ST02001,ST02017 ,ST02015 asc
*/
//Seelega tuleb teha hï¿½kk
        

        String sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr,(ST03021 *((100-ST03022)/100)) as hind , ST03022 as ale" +
                      "  FROM ST030500 WITH(NOLOCK) INNER JOIN SL010500 WITH(NOLOCK) ON ST03001 = SL01001 WHERE ST03015> getdate()-120 AND ST03016>0 AND ST03020>0 " +
					"	GROUP BY ST03017,SL01001, ST03020,ST03015 ,ST03021 ,ST03022 ORDER BY SL01001,ST03017 ,ST03015 ASC";

	if (isDebug()) {
	    System.out.println(sql2);
	}

	Vector custProds = new Vector();
      
          try {
            stmt = con.prepareStatement(sql2);
           // stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            if (agentCode != null){
             // stmt.setString(2, agentCode);
            }
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
            double kogus =  Double.parseDouble(rs.getString("kogus").trim());
           String ld =  rs.getString("lastdate").substring(5,10);
            String arve = rs.getString("arvenr").trim();
            double hind = rs.getDouble("hind");
            double ale = rs.getDouble("ale");
              CustProd cp = new CustProd(kliendikood, tootekood,kogus ,0 ,0,
              		ld,hind,0,0," ");
                cp.setAbiKood(arve);
                cp.ale = ale;
                custProds.add(cp);
             
            }
            rs.close();
            stmt.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
        
        connectionDown();
	return custProds;
    }

    public String getPilotDayData(Agent agent){
        String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020500  " +
        " LEFT JOIN OR200500 ON  OR20001=ST02009   " +
        " LEFT JOIN SL010500 l ON SL01001=ST02034 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
 // System.out.println("asi  " + agent.getID() + " " + sql);
  
  
  try {
 stmt = con.prepareStatement(sql);
 stmt.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");

 //System.out.println("asi  " + agent.getID() + " " + sql);
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

//System.out.println(sb.toString());
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
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020500  " +
        " LEFT JOIN OR200500 ON  OR20001=ST02009 LEFT JOIN SL140500 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010500 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
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
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR200500 WHERE MONTH(OR20015)=" +
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
         String cmd = "SELECT OR20019 AS agent,SUM(OR20024) AS sales FROM OR200500 WHERE MONTH(OR20015)=" +
                      "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
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
                       }
                    }

             rs.close();
             stmt.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020500 INNER JOIN " +
                   "SC010500 ON ST02017=SC01001 WHERE ST02007=?" +
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
                    " FROM SL010500 LEFT JOIN SY240500 ON SY24002=SL01035 ORDER BY " +
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

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200500 " +
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "ScalaPrikeLit";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:reportLAT";
    private Connection	       con = null;
    private Connection	       con2 = null;
    private PreparedStatement  stmt = null;
    private TreeMap sortCodes = new TreeMap();
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private  Vector prlines = new Vector();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

