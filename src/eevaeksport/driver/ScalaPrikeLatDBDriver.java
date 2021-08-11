/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import java.util.HashSet;
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
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class ScalaPrikeLatDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ScalaPrikeLatDBDriver(EksportPanel parent) {
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
	
		company = getParent().getProps().getProperty("companycode");
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.company");
	     }
	  
	  String company =  null;
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
	public static final String SEPARATOR = ";";
    
    private Vector getNoCrecitInfo(){
  	  Vector aa = new Vector();
  	  
  		try {
  			FileReader fr = new FileReader(getDir() + "nocreditlimit.txt");
  			BufferedReader reader = new BufferedReader(fr);
  			String line = null;
  			while ((line = reader.readLine()) != null) {
  				line = line.replaceAll(";","; ");
  				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
  				
  				if(st.hasMoreTokens()){
  					aa.add(st.nextToken());
  				}
  				
  			}
  		}catch(Exception e){
  			e.printStackTrace();
  		}

  			
  				
  	  
  	  return aa;
    }
    
    
 public String getSaveDebtInfo() {
    	
    	Vector nocrinfo = getNoCrecitInfo();
    	
    	

String cmd3 = " select tbl.klient as kli ,sum(tbl.overdebt) as ove,sum(tbl.debt) as deb , max(tbl.limit) as limi	from( " +
	"	SELECT distinct left (SL01001,4) AS klient,sum(case when SL03006<=getdate() then SL03013-SL03053 else 0 end) AS overdebt ,sum( SL03013-SL03053 ) AS debt , '' as limit " +  
" FROM SL030400 INNER JOIN SL010400 kl ON SL03001 = SL01056  or SL03001 = SL01001  WHERE     NOT(SL03013=SL03053)   GROUP BY SL01001 having " +
"  sum(SL03013-SL03053) !=0 " +
"  union " +
 " SELECT RTRIM(OR01003) AS klient,  0 as overdebt, sum(OR03009) AS debt   , '' as limit " +
 " FROM  OR010400 LEFT JOIN OR030400 on OR01001 = OR03001  GROUP BY  OR01003 " +
 " union " +
 " SELECT distinct left (SL01001,4) AS klient,0 AS overdebt ,0 AS debt , " +
 "   SL01037  as limit from SL010400 where len (SL01001) = 4  ) as tbl " +
"	group by  tbl.klient order by tbl.klient";



		TreeMap custs = new TreeMap();
		StringBuffer debts = new StringBuffer();
		HashSet blokid = new HashSet();
		try {
	     this.connectionUp();
	     
	     Statement st = con.createStatement();
		 ResultSet rs = st.executeQuery(cmd3);

			while (rs.next()) {
				blokid.add(rs.getString("sckood").trim());
			}
			
	        rs.close();
			st.close();
	     
			 st = con.createStatement();
			 rs = st.executeQuery(cmd3);

		while (rs.next()) {

		String kood = rs.getString("kli").trim();
		double volg = rs.getDouble("ove");
		int usedcredit = (int)rs.getDouble("deb");
		String limiit = rs.getString("limi");
		Customer custw = null;
		if(custs.get(kood) != null){
		 custw =	(Customer)custs.get(kood);
		}else{
			 custw =	 new Customer (kood);
		}
		
		custw.setDebt( volg);
		custw.setUsedCredit(  usedcredit);
		
		double limit = 0 ;
		if(limiit != null && limiit.trim().length() > 0){
			custw.setCredit(limiit.trim());
			limit = Double.parseDouble(limiit.trim());			
		}
		
		try{
			limit = Double.parseDouble(custw.getCredit());
		}catch(Exception e){
			
		}
		
		if( limit != 0 && limit < custw.getUsedCredit()){
			custw.setSalesBlock(true);
			custw.noOrder = true;
		}
		
			custs.put(kood,custw);
		
		}
		rs.close();
		} catch (Exception e) {
		getParent().showError(e);
		}
		Iterator     codes = custs.values().iterator();
		while (codes.hasNext()){
		Customer cust = (Customer)codes.next();
		String bl = "0";
		if(cust.isSalesBlockT()){
			bl = "1"; // 2
			}
		String creditLimit = cust.getCredit();
		String usedCredit = cust.getUsedCredit() + "";
		if(blokid.contains(cust.getID())){
			bl = "1"; // 2
		}
		if(nocrinfo.contains(cust.getID())){
			creditLimit = "0";
		//	usedCredit = " ";
			bl = "0";
		}
		
		debts.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + ";" + creditLimit + ";" + usedCredit + "\n");
		}
         this.connectionDown();
		return debts.toString();

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
        	/*  connectionUp(url);//'PR' + 
        	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , '07' AS LADU ,SC03003 AS stock   " +
		      "  FROM SC030400  WHERE SC03002='07' AND SC03003!=0 ORDER BY SC03001" ;


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
		             }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();*/
        	return null;
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
    			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 4; a < row.getLastCellNum(); a++) {
    				try {
    					 System.out.println(row.getCell((short)a).getStringCellValue());
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

    							if (go != null && go.toLowerCase().equals("x")) {

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

    public TreeMap getProdGroups() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240400 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'II' ORDER BY SY24003";
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
    
    
    public TreeMap getAgents() {
        connectionUp(url);
        
        HashMap asendajad = new HashMap();
        
        try {
 			 FileReader fr = new FileReader("settings/vacation.txt");
 	         BufferedReader reader = new BufferedReader(fr);
 	       String line = null;
 	 
 	        while((line = reader.readLine()) != null ){
 	           
 	        	StringTokenizer st = new StringTokenizer(line, "=");
 	
 	          if (st.hasMoreTokens()) {
 	            String ag1 =	st.nextToken();
 	            String manX = st.nextToken().trim();
 	            StringTokenizer ss = new StringTokenizer(manX, ",");
 	            while (ss.hasMoreTokens()){
 	            String man = ss.nextToken();
 	            Vector vv = new Vector();
 	            if(asendajad.get(ag1) != null){
 	           	 vv = (Vector)asendajad.get(ag1);
 	             }
 	             vv.add(man);
	           	 asendajad.put(ag1, vv);
 	       	
 	            }
 	            }
 	          }
 	        System.out.println("Asendajad "+ asendajad.toString());
 		 
 		}catch(Exception d){
 		//d.printStackTrace();	
 		}
        
        Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240400 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();
	
	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agent.setIsPilot(true);
              agent.setLanguage("LAT");
              agent.setCountry("lv");
              agent.setPassWord("0000");
              String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
            
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.trim().length() > 1){
            	  agent.setActive(true);
              }
              
              if(asendajad.get(agent.getID()) != null){
               	Vector vv = (Vector)asendajad.get(agent.getID());
               	Enumeration aa = vv.elements();
               	while(aa.hasMoreElements()){
               		agent.addDubbler((String)aa.nextElement());
               	//	System.out.println("Lisan asendaja " + agent.getID() + " " + agent.getDubbler().toString());
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
	if(rcompany != null){
    	//Converter.sendAgentJson(agents, rcompany, false);
    }
	
        connectionDown();
	return agents;
    }


    public Vector getPriceMatrixData()  {
        
         return prlines;
       }


    private TreeMap agents1 = new TreeMap();
    
    
    public TreeMap getAgents1() {
   		return agents1;
   	}
    

    private TreeMap custs = null; 
    

    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        connectionUp2(url);
        Vector nocrinfo = getNoCrecitInfo();
        String cmd = "SELECT SL01001 as sckood,SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress, " +
		"	SL01003 as tanav,SL01004 as linn,SL01005 as regioon,SL01083 as indeks,(SELECT SC45005 FROM SC450400 WHERE SC45003=(select SL01036 from SL010400 WHERE SL01001=k.SL01056)) AS ale,SL01036 AS ale2 , " +
        "  upper(SL01053)  AS visit, ' ' AS info ,SL01035 as agent,SL22005 as paym ,(SELECT SL01029 FROM SL010400 WHERE SL01001=k.SL01056)  as klgrupp," +
        " (SELECT SL01060 FROM SL010400 WHERE SL01001=k.SL01056) as block ,SL01060 as block2,SL01056 as payer " +
         " ,v.day as delday ,(SELECT SL01037 from SL010400 where SL01001=k.SL01056) as limiit ,  SL01010 AS chain " +
         " FROM SL010400  k LEFT OUTER JOIN SL140400 ON SL01001=SL14001 AND SL14002='00' " +
        "  LEFT OUTER JOIN SY240400 ON SL01035=SY24002 AND SY24001='BK' " +
        "  LEFT OUTER JOIN SL220400 ON SL01024=SL22002 AND SL22001='0'  " +
        " LEFT OUTER JOIN veoring0400 v ON v.VR = SL01027 " + 
		"   WHERE (len(SL01001)>4 and SL01002 != ''  AND SL01129 != '' AND SL01070!='D')"
		+ " or SL01129 = '0100000' ORDER BY SL01002";
        
 connectionUp(url);
       
      log(cmd);


   Statement st = null;
   ResultSet rs = null;

   
   // Siin on soodukad k�ik 
   try{
          
            String sqq = " SELECT  SCP6002 AS clgroup,SCP6001  AS prodid, SCP6008 AS price \r\n" + 
            		"      , SCP6010 as start , SCP6011 as lopp FROM  SCP60400 WHERE  \r\n" + 
            		"        getdate() BETWEEN  SCP6010 AND SCP6011 \r\n" + 
            		"        and len(SCP6002)=4 ";
            
            st = con.createStatement();       
            rs = st.executeQuery(sqq);

            while (rs.next()) {

                String clGroup = rs.getString("clgroup").trim();
                String prodID = rs.getString("prodid").trim();
  	   
                double price = rs.getDouble("price");
                String alg = rs.getString("start").trim().substring(0,10);
                
                String lopp = rs.getString("lopp").trim().substring(0,10);
                MatrixLine mline = new MatrixLine();
              
                
                mline.setStartDate(alg);
                mline.setEndDate(lopp);
                if (clGroup != null && clGroup.trim().length() > 0) {
                    mline.setDiscGroup(clGroup.trim());
                  }
                
                if (prodID != null && prodID.trim().length() > 0) {
                  mline.setProdID(prodID.trim());
                }

                if (price != 0) {
                    mline.setPrice(price);
                }
                prlines.add(mline);                 
            }
            
            //Siin panime PEAKLIENDI SOODUKAD ja kasutame seal pricergroupi
            
            
            
         // Siin loetkse v�lja hinnakirjad toodetele   
            
            String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
            " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
            " SC39005 else SC39004 end AS price ,SC39004  " +
            " FROM SC390400 WHERE SC39002 != '00' order by SC39002,SC39001 ";
            rs = st.executeQuery(sql2);

            while (rs.next()) {

              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String discount = "0";       
			   
              double price = rs.getDouble("price");
              if(price == 0){
            	  price = rs.getDouble("SC39004");
              }
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
             prlines.add(mline); 
            }

            rs.close();
            st.close();
           
            
            connectionDown();
          } catch (SQLException ex) {
            ex.printStackTrace();
          }
   
        
        
        
        
        
	log(cmd);
//System.out.println("sortkoodi suurus"+ sortCodes.size());
	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        connectionUp();
	try {
           PreparedStatement stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	   	      rs = stmt.executeQuery();
	   	   custs = new TreeMap(); 
	   	      
	   	      
	    while (rs.next()) {
	    	String sckood = rs.getString("sckood").trim();
		String nimi = rs.getString("nimi").trim();
                Customer cust = new Customer(rs.getString("kood").trim());
                custs.put(sckood, cust.getID());
                
                cust.setAddress(rs.getString("aadress").trim());
                  nimi = nimi + " " + cust.getAddress();
                
                cust.setName(nimi);
                
                
                
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
                
                cust.setCountry("Latvia");

                
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }
              
                cust.setVisit(Converter.getVisitDay(parent.make(rs.getString("visit")).trim()));
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
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
  				
                if(agen.trim().equals("OBU") || agen.trim().equals("KAJ") || agen.trim().equals("EDE") ){
                	 cust.addAgent(new Agent("ABL"));
                	 cust.addAgent(new Agent("MMI"));
                	 //paneme horeca manageri juurde
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
             String klgrupp = rs.getString("klgrupp");
             if(klgrupp != null && klgrupp.trim().length()>0){
            	 cust.setGroup(klgrupp.trim());
             }
                
             if(sortCodes.get(sckood)!= null){
             	cust.addFixSortCode((String)sortCodes.get(sckood));
             }
                
                String block  =  rs.getString("block");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("1")){
                		cust.setSalesBlock(true);
                		cust.noOrder = true;
                	}
                	
                	if(block.trim().equals("2")){
                		cust.setSalesBlock(true);
                		cust.noOrder = true;
                	}
                	
                }
                 block  =  rs.getString("block2");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("1")){
                		cust.setSalesBlock(true);
                		cust.noOrder = true;
                	}
                	if(block.trim().equals("2")){
                		cust.setSalesBlock(true);
                		cust.noOrder = true;
                	}
                 }
         
                String payer = rs.getString("payer");
                if(payer != null && payer.trim().length()>0){
                	cust.setPayer(payer); 
                	cust.setCustType(payer);            	
                }
                String delday = rs.getString("delday");
                if(delday != null && delday.trim().length()>0){
                	cust.setPersonalData(delday.trim().replaceAll(";", ":"));             	
                }
              
                
              
                //otsime v�lasumma igale kauplusele 
                
                String sql6 = "SELECT SUM( case when SL03006<=getdate() then SL03013-SL03053 else 0 end ) AS debtover," +
                		"SUM(SL03013-SL03053) AS debt FROM SL030400 INNER JOIN SL010400 ON SL03001 = SL01001  WHERE  " +
                " NOT(SL03013=SL03053) AND SL03006<=getdate() and SL01001='" + cust.getPayer()+ "'  GROUP BY SL01001 ";

   Statement stm = con2.createStatement();

   ResultSet rs3 = stm.executeQuery(sql6);
   while(rs3.next()){    
       double debtover = Double.parseDouble(rs3.getString("debtover").trim());
       double debt = Double.parseDouble(rs3.getString("debt").trim());
       cust.setDebt(cust.getDebt() + debtover);
       cust.setUsedCredit(debt);
   }
   
   rs3.close();
   stm.close();

   String limit = rs.getString("limiit");
   if(limit != null && limit.trim().length() != 0){
   	double limiit = Double.parseDouble(limit.trim());
   	if(!nocrinfo.contains(cust.getPayer())){
   		cust.setCredit(limit.trim());
   		if(limiit < cust.getUsedCredit()){
   			cust.setSalesBlock(true);
   			cust.noOrder = true;
   			}
   	}
   }
   
   String chain = rs.getString("chain");
   if(chain != null && chain.trim().length()>0 ){
   	cust.setChain(chain.trim());
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


    if(rcompany != null){
    	// Converter.sendProductJson(products, rcompany);
    	//Converter.sendCustomerJson(customers, rcompany,true,user,pass);
    }
        
	return customers;
    }


    public Vector getDebt(String agentCode) throws SQLException{
        Vector list = new Vector();
        String sql = "SELECT SL01001 AS payer, SL03087 as custid ,  SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa , -datediff(day ,getdate(),SL03006) as yle " +
        		"FROM SL030400 INNER JOIN SL010400 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                     "LEN(LTRIM(SL03013))>0  ORDER BY SL01001";

        connectionUp(url);
        stmt = con.prepareStatement(sql);


        ResultSet rs = stmt.executeQuery();

        while(rs.next()){
          Debt debt = new Debt();
          
          debt.setPayerID(rs.getString("payer").trim());
          String cuid = rs.getString("custid").trim();
          if(custs.get(cuid) != null){
          	debt.setCustID(custs.get(cuid)+"");	
          }
          
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
        StringBuffer buf = new StringBuffer();

        String cmd =" SELECT    RTRIM(SC01001) AS toode,(RTRIM(SC01002)+' '  " +
             "   +rtrim(SC01003)) + ' ' +RTRIM(SC01001) AS nimi,(SC39004) AS myygihind ,SC01009 AS tykke , SC01011 AS maxale,'KE' +RTRIM(SC01150) AS pandikas ,   " +
             " case when  SC01031 = 1 then 1 else 0 end AS out ,SC01128 AS grupp,SC01132 AS ean , SC01016 as letter , SC01071 /100 as maht  " +
             ", SC03003-SC03004 AS stock , SC01097 as separate FROM SC010400 left outer JOIN SC390400 ON SC01001 " +
             "  = SC39001 and SC39002='00' left outer JOIN SC030400 on SC03001=SC01001 and SC03002='07' AND SC03003!=0 WHERE  "
             + " LEN((SC01128))>0 and SC39004 > 0 and rtrim(ltrim(SC01016)) not in  ('S','U') " +
             "  ORDER BY LEFT(RTRIM(SC01037),1),SC01001 " ;
	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim().replaceAll("'", "").replaceAll("\"", "");

                Product prod = new Product(kood);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
                prod.setInCases(parseDouble(rs.getString("tykke").trim()));

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
                 String aaa = "";
                if(gr != null && gr.trim().length()>0){
                  prod.setEAN(gr.trim());
                  aaa = gr.trim().substring(gr.trim().length()-3);
                }
                prod.setName( nimetus + " " + aaa );  
                
      String letter = rs.getString("letter");
                
              prod.setVat("21"); 
              
              double stock = rs.getDouble("stock");
            
              
              String separate  = rs.getString("separate");
              prod.setSendSeparate("alco");
              if(separate != null ){
            	  if (separate.trim().equals("8")){
            		 prod.setSendSeparate("water");
            	  }
            	  if (separate.trim().equals("3")){
            		  prod.setSendSeparate("nonalco");
            	  }
              }
              
              if(prod.getGroup().equals("TAR")){
            	  prod.setSendSeparate("alco");
              }
              
              boolean ok = true;
              if(letter != null && letter.trim().length()>0 && 
            		  (letter.trim().equals("X") || letter.trim().equals("K")) && stock <= 0){
               ok = false;
             // System.out.println("V�lja Pole laos " +  prod.getName());
             }
              if(ok){
            	
            	   prod.setVat("21");
  		        products.put(nimetus, prod);  
              }
	    }
            rs.close();
            st.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}
	
	 if(rcompany != null){
	    	 Converter.sendProductJson(products, rcompany);
	    	//Converter.sendCustomerJson(customers, rcompany,false,null,null);
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
//Seelega tuleb teha h�kk

        String sql2 = "SELECT  SL01001 AS klient , ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate " +
                      " FROM ST030400 WITH(NOLOCK) INNER JOIN SL010400 WITH(NOLOCK) ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0  and SL01129 !='' " +
                      " GROUP BY ST03017,SL01001, ST03020,ST03015 ORDER BY SL01001,ST03017 ,ST03015 ASC";

	if (isDebug()) {
	    System.out.println(sql2);
	}

	Vector custProds = new Vector();
      
          try {
            stmt = con.prepareStatement(sql2);
            stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            if (agentCode != null){
             // stmt.setString(2, agentCode);
            }
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()),0 ,0,
              		rs.getString("lastdate").substring(5,10),0,0,0," ");
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
        String sql = "SELECT ST02016 as date , RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020400  " +
        " LEFT JOIN OR200400 ON  OR20001=ST02009 LEFT JOIN SL140400 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010400 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-3 AND ST02007=? " +
        " GROUP BY ST02009,SL01002,ST02016 order by ST02016 ,SL01002";

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
 	String date = rs.getString("date").substring(0, 10);
     String nimi = rs.getString("nimi");
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append(date + " " + nimi + "\n");
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

      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020400  " +
        " LEFT JOIN OR200400 ON  OR20001=ST02009 LEFT JOIN SL140400 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010400 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
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
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR200400 WHERE MONTH(OR20015)=" +
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
         String cmd = "SELECT OR20019 AS agent,SUM(OR20024) AS sales FROM OR200400 WHERE MONTH(OR20015)=" +
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
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020400 INNER JOIN " +
                   "SC010400 ON ST02017=SC01001 WHERE ST02007=?" +
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
      ManagerData md = new ManagerData();
       return md;
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

       
    public static final String DRIVER_VERSION = "ScalaPrikeLat";
    private String             url = "jdbc:odbc:scalaDB";

    private Connection	       con = null;
    private Connection	       con2 = null;
    private PreparedStatement  stmt = null;
    private TreeMap sortCodes = new TreeMap();
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private  Vector prlines = new Vector();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

