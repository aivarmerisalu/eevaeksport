/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.ReportLine;
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

public class ScalaTobaDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Router,Promo,Discounter,PictureMaker{

    /**fgetc
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public ScalaTobaDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	
	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	} 
	
	company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
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
    
private String company = null;
	
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
        String cmd = "SELECT SL01001 as klient FROM SL01"+area+"00 where SL01075 >0";

String cmd3 = "SELECT SL01001 AS klient,sum(SL03014-SL03054) " +
       "   AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01056 " +
		"		or SL03001 = SL01001  WHERE  " +
         "        NOT(SL03013=SL03054) AND SL03006<=getdate()  GROUP BY SL01001 having " +
		" sum(SL03014-SL03054) !=0";
		


		if (isDebug()) System.out.println(cmd);
		TreeMap custs = new TreeMap();




		StringBuffer products = new StringBuffer();
		try {

	     this.connectionUp();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(cmd3);

		while (rs.next()) {

		String kood = rs.getString("klient").trim();
		double volg = rs.getDouble("debt");
		Customer custw = new Customer (kood);
		custw.setDebt(volg);
		if(volg>0){
			custs.put(kood,custw);
		}
		}

		//siin pannakse peale PLOKKID

		rs = st.executeQuery(cmd);
		while (rs.next()) {
			String kood = rs.getString("klient").trim();
		    Customer cus =  (Customer)custs.get(kood);
		    if(cus != null){
		    	cus.setSalesBlock(true);
		    }else{
		    	Customer cusnew = new Customer(kood);
		    	cusnew.setSalesBlock(true);
		    	custs.put(kood,cusnew);
		    }
		 }

		rs.close();
		} catch (Exception e) {
		getParent().showError(e);
		}
		Iterator     codes = custs.values().iterator();
		while (codes.hasNext()){
		Customer cust = (Customer)codes.next();
		String bl = "0";
		if(cust.isSalesBlock()){
			bl = "1";
			}
		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
		}
         this.connectionDown();
		return products.toString();

       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }


        public String getSaveStockInfo(){
        	  connectionUp(url);//'PR' + 
        	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , '10' AS LADU ,SC03003 AS stock   " +
		      "  FROM SC03"+area+"00  WHERE SC03002='10' AND SC03003!=0 ORDER BY SC03001" ;


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
		       return products.toString();
		   }
        


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }


        public String getDistribution() {
          return null;
        }
        public Vector getFixedAssortmentData() {
    		Vector result = new Vector();
    		File f = new File(this.getExcelDir() + "/Sortiments.xlsx");
    		
    		try {
    			
    			FileInputStream ff = new FileInputStream(f);
    			XSSFWorkbook workbook = new XSSFWorkbook((ff));
    			XSSFSheet sheet = workbook.getSheet("Products");
    			XSSFRow row = null;
    			TreeMap groups = new TreeMap();
    			row = sheet.getRow(3);
    			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

    			// System.out.println("rownum " + row.getLastCellNum());
    			for (int a = 3; a < row.getLastCellNum(); a++) {
    				try {
    					// System.out.println(row.getCell((short)a).getStringCellValue());
    					groups.put(a + "", row.getCell((short) a)
    							.getStringCellValue());
    				} catch (Exception e) {
    					// System.out.println(row.getCell((short)a).getNumericCellValue());
    				}
    			}

    			 System.out.println(groups.toString());

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
                        	 //  System.out.println("prodId " + prodID);
    					// Siin l�heme reasisse rokkima ja vaatama kas se toode
    					// l�heb kaasa
    					for (int a = 3; a < row.getLastCellNum(); a++) {

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
System.out.println("Fixed suurus ");
    		return result;

    	}


        
        private Connection getConnectionORA() throws SQLException{
                Connection con = DriverManager.getConnection(url3, "V5EUGEE_RPT","V5EUGEE_RPT");
				return con;
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
        	if(con != null){
                con.close();
                con = null;	
        	}
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
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY24"+area+"00 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood=  rs.getString("liik").trim();
            tm.put(kood, Converter.enCodeEst(rs.getString("nimetus").trim() +" " + kood));
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }
    
    
    public String parseFirstEmail(String emails) {
        String		email = "";
        StringTokenizer st =
            new StringTokenizer(emails, String.valueOf(";"));

        if (st.hasMoreTokens()) {
            email = st.nextToken();
        }

        return email;
    }

    public TreeMap getProdGroupsSecond() throws SQLException {
       TreeMap   tm = new TreeMap();
            tm.put("TAG","RealiseerimisLadu");
        return tm;
 }
    public TreeMap getAgents() {
        connectionUp(url);
      //  this.getFixedAssortmentData();
        
        String  cmd = "select Code ,Name from PlanEmployee";
	TreeMap agents = new TreeMap();

	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();
	    Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("Code").trim());
              String nimi = rs.getString("Name").trim();
              agent.setName(nimi);
              agent.setObject("10");
              agent.setIsPilot(true);
              agent.setLanguage("EST");
              agent.setCountry("et");
              agent.setPassWord("0000");
String email = " ";
              
              if(propse.get(agent.getID()) != null){
            	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
              }
              
              
              if(email.length() > 3){
            	  agent.setEmail(email);
              }
              if(email.length() > 0){
            	  agent.setActive(true);
              }
              
              
              
              agents.put(nimi, agent);
	    }
	    Agent aaa = new Agent("HOUSE");
	    aaa.setName("HOUSE");
	    aaa.setObject("10");
        aaa.setIsPilot(true);
        aaa.setLanguage("EST");
        aaa.setCountry("et");
        aaa.setPassWord("0000");
	    agents.put("HOUSE", aaa);
	    
	    
	    Agent aaa2 = new Agent("2");
	    aaa2.setName("2");
	    aaa2.setObject("10");
        aaa2.setIsPilot(true);
        aaa2.setLanguage("EST");
        aaa2.setCountry("et");
        aaa2.setPassWord("0000");
	    agents.put("2", aaa2);
	    
	    Agent aaa3 = new Agent("3");
	    aaa3.setName("3");
	    aaa3.setObject("10");
        aaa3.setIsPilot(true);
        aaa3.setLanguage("EST");
        aaa3.setCountry("et");
        aaa3.setPassWord("0000");
	    agents.put("3", aaa3);
	    
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
        
        if(rcompany != null){
	    	//Converter.sendAgentJson(agents, rcompany, false);
	    }
	return agents;
    }

    public Vector getPriceMatrixData()  {
        
    	
  	   String    sql = "SELECT SL01001 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, " +
         "	(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price ,( case when SC18011 < getdate()+100 then SC18011 else null end) as paev" +
         "   FROM SC18"+area+"00 INNER JOIN SL01"+area+"00 ON  left(SC18002,10)= SL01001 LEFT JOIN SY24"+area+"00 on SC18009=SY24002 and SY24001='BG' WHERE " +
          "  SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   and len(SL01008) >0  " +
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
               String sss = rs.getString("SC18009");
               String discount = rs.getString("discount");
               if(discount == null){
               	discount = sss.trim();
               }else{
               	discount = discount.trim().replaceAll("%"," ").trim();
               }
 			   
               double price = rs.getDouble("price");
               String date = rs.getString("paev");
               MatrixLine mline = new MatrixLine();

               if (custID != null && custID.length() > 0) {
                 mline.setCustID(custID);
               }
               if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                 mline.setCliGroup(clGroup.trim());
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
               mline.setDiscount(disc);

               if (disc == 0 && price != 0) {
                 mline.setPrice(price +0.001);

               }
               if(mline.getPrice() > 0 || mline.getDiscount() > 0){
                   result.add(mline);    
               }
               
             }

             
             String sql2 = "    SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35)  AS prodid, SC18008 AS price, SC18009 as ale  ,SC18010 as algus ,SC18011 as lopp " +
            "  FROM SC18"+area+"00  WHERE  SC18001='C' AND " +
            "   CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 and (SC18011<getdate()+150 or year(SC18011) > 2030 ) order by LEFT(SC18002,10)";
             rs = st.executeQuery(sql2);

             while (rs.next()) {

               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid").trim();
               String discount = "0";       
 			   
               double price = rs.getDouble("price");
               String ale = rs.getString("ale");
               if(ale != null &&ale.trim().length()>0){
            	   try{
            		   price = price * (100-Double.parseDouble(ale))/100 + 0.001;
            	   }catch(Exception ee){
            		  ee.printStackTrace(); 
            	   }
               }
               
               String fromdate = rs.getString("algus");
               String todate = rs.getString("lopp");
               MatrixLine mline = new MatrixLine();
               mline.setStartDate(fromdate.substring(0,10));
               mline.setEndDate(todate.substring(0,10));
               if (clGroup !=null && clGroup.trim().length() > 0) {
                 mline.setCliGroup(clGroup.trim());
                 mline.setCustID(clGroup.trim());
               }

               if (prodID != null && prodID.trim().length() > 0) {
                 mline.setProdID(prodID.trim());
               }
   
               if (Double.parseDouble(discount) == 0 && price != 0) {
                 mline.setPrice(price);

               }
              result.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
             }
             
             
             
             
           }catch(Exception e){
          	 e.printStackTrace();
           }
  	
      return result;
       }

    
    private String getTimeStringShort(long mils){
        
        int ee = 0;
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(mils);
        	if(cc.get(Calendar.DST_OFFSET) != 0){
            	ee = cc.get(Calendar.HOUR) + cc.get(Calendar.DST_OFFSET)/60;
        	}
            String hour = ee + "";
            String minute = cc.get(Calendar.MINUTE) + "";

        if(hour.length()==1){
        	hour= "0" + hour;
        }
        
        if(minute.length()==1){
        	minute= "0" + minute;
        }          
        String ttt = hour + minute; 
           if((ttt).length()>4){
        	   System.out.print(hour + "-" + minute);
        	   ttt = ttt.substring(0,4);
           }
           
        	return ttt ;
        }
        
    
	public void setAlternativeReport(ReportLine rr) throws SQLException {
		connectionUp();
		//M��rame reportline kuup�eva 
		Calendar cal = Calendar.getInstance();
		try{
			
		
	      cal.setTime(rr.getEndTime());
		  String kuupaev = getDateAAAADDMMStr(cal, 0);
		
		//Esitekas m��rame kindlaks kas sihuke visit on olemas
		
		String sql = "select VISI_ID   from PDA_VISIT vis INNER JOIN PDA_CLIENT cl on cl.CLIE_ID = vis.CLIE_ID " +
				"where PLANNED_TIME='" + kuupaev + "' and cl.CODE='" + rr.getClientCode() + "'";
			
		String visi_id = null; 
		Statement st = con.createStatement();
        ResultSet rs8 = st.executeQuery(sql);
        if (rs8.next()){
        	visi_id = rs8.getString("VISI_ID");
        }
        rs8.close();
         st.close();
        System.out.println("Aruanderida ALTERNATIVE " + visi_id );
      
        
        
        //nyyd lisame vajalikud vidinad olemasolevale reale 
      
        if(visi_id != null){
        	 String   sql5 = " UPDATE PDA_VISIT set START_TIME ='"  + getTimeStringShort(rr.getStartTime().getTime())+ "' , END_TIME ='"
        	    + getTimeStringShort(rr.getEndTime().getTime()) + "' , VISIT_DATE ='" + kuupaev+ "' where VISI_ID =" + visi_id + "";
        	 System.out.println(sql5);
   		              PreparedStatement st2 = con.prepareStatement(sql5);
   		              st2.executeUpdate();	              
   		              st2.close();
    			  
        }
        
        String text = "0-OK";
        if(rr.getLineSum()== 0){
        	if(rr.getText() != null && rr.getText().indexOf("Muu mure") != -1){
        		text = "1-Other";
        	}
        	if(rr.getText() != null && rr.getText().indexOf("Oli v") != -1){
        		text = "3-Debt";
        	}
        	if(rr.getText() != null && rr.getText().indexOf("Palju") != -1){
        		text = "2-Stock";
        	}
        }
        
      
         }catch(Exception e){
			e.printStackTrace();
		}
    
	}
    
    
	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr ,Date dat){}

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr ,Date dat){
	     String sql = "INSERT into dbo.supervisor values ("
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
	          this.connectionUp();
	           
	          
	          
	          
	          
	          String prodshort = "";
		      String grupp = "";
		      String stit = "";
		      String route = "";
		      
		      
		      

		      
		      
		      
		      
		      
		        Calendar  ddd =  Calendar.getInstance();
		        ddd.setTime(dat) ;      
	              ddd.set(Calendar.HOUR,0);
	              ddd.set(Calendar.MINUTE,0);
	              ddd.set(Calendar.SECOND,0);
	              String dd = ddd.getTimeInMillis() + "";
	              dd = dd.substring(0,dd.length()-3) + "000";
	  
	          
	          
	    
            for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
            	eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
              
              String      sql4 = " select t.SC01011 , t.SC01037 ,it.STIT_ID from SC01"+area+"00 t inner join PDA_STOCK_ITEM it on it.SCALA_CODE = t.SC01001 where SC01001 = '"  + lw.getLine().getProdID() + "'";
		        Statement st = con.createStatement();
		        ResultSet rs8 = st.executeQuery(sql4);
		        if (rs8.next()){
		        	prodshort = rs8.getString("SC01011");
		        	grupp = rs8.getString("SC01037");
		        	stit = rs8.getString("STIT_ID");
		        }
		        rs8.close();
		         st.close();
		        System.out.println("listing2 " + grupp + " " + prodshort  + " " + stit);
		        System.out.println("listing3 " + sql4  );
              
		        
		        
		        
		        
		        
		        
		        
		        
		     //   if(grupid != null && !grupp.equals("") && grupid.indexOf(grupp.trim())!= -1){

			        if(true){

		        	
		        	  if(lw.getLine().getResInt1() == 1){
		        		   String      sql5 = "insert into PXPI"+area+"00 (CustomerID ,ProductID, DateStamp ,IsOOS , InList, SalesmanCode ,WarehouseID, Product, AvailQTY)"+
			        	      "values ( '" + lw.getLine().getCustID() + "','" + lw.getLine().getProdID() + "',getdate(),1,1,'" +  wr.getInfo().getAgentID() + "','10','"+ prodshort.trim() +"',2) ";

			        		              PreparedStatement st2 = con.prepareStatement(sql5);
			        		              st2.executeUpdate();	              
			        		              st2.close();
			    
			        		              
			        		              
			        		              
			        		  		  	Calendar cal = Calendar.getInstance();
			        				      cal.setTime(lw.getLine().getDate());
			        					  String kuupaev = getDateAAAADDMMStr(cal, 0);
			        				  	String sql77 = "select VISI_ID   from PDA_VISIT vis INNER JOIN PDA_CLIENT cl on cl.CLIE_ID = vis.CLIE_ID " +
			        					"where PLANNED_TIME='" + kuupaev + "' and cl.CODE='" + lw.getLine().getCustID() + "'";
			        				
			        			Statement st77 = con.createStatement();
			        		    ResultSet rs77 = st77.executeQuery(sql77);
			        		    if (rs77.next()){
			        		    	route = rs77.getString("VISI_ID");
			        		    }
			        		    rs77.close();
			        		     st77.close();
			        		   
			        		              if(route.equals("")){
			        		            	  route = "1";
			        		              }
			        		   
			        		              
			        		       	   String      sql44 = " insert into PDA_OOS values (" + route + " , " + stit + ")  ";
			        		       	  System.out.println("Sisemine ROUTE ID " + route + " " +  lw.getLine().getDate() + "\n" +
		        		            		   sql44);
				        		              try{

					        		              PreparedStatement st4 = con.prepareStatement(sql44);
					        		              st4.executeUpdate();	              
					        		              st4.close();
					        		                         
				        		              }catch (SQLException eee){
				        		            	  
				        		              }
			        		           
						        		            
						        		              
		        	  }else  if(lw.getLine().getResInt1() == 0 && lw.getLine().getComment() != null && lw.getLine().getComment().indexOf("OUT")!=-1 ){
		        		  
		        		   String      sql5 = "insert into PXPI"+area+"00 (CustomerID ,ProductID, DateStamp ,IsOOS , InList, SalesmanCode ,WarehouseID, Product, AvailQTY)"+
			        	      "values ( '" + lw.getLine().getCustID() + "','" + lw.getLine().getProdID() + "',getdate(),0,0,'" +  wr.getInfo().getAgentID() + "','10','"+ prodshort.trim() +"',2) ";

			        		              PreparedStatement st2 = con.prepareStatement(sql5);
			        		              st2.executeUpdate();	              
			        		              st2.close();
		        	  }else  if(lw.getLine().getResInt1() == 0 && lw.getLine().getComment() != null &&  lw.getLine().getComment().indexOf("TO THE LIST")!=-1 ){
		        		  
		        		   String      sql5 = "insert into PXPI"+area+"00 (CustomerID ,ProductID, DateStamp ,IsOOS , InList, SalesmanCode ,WarehouseID, Product, AvailQTY)"+
			        	      "values ( '" + lw.getLine().getCustID() + "','" + lw.getLine().getProdID() + "',getdate(),0,1,'" +  wr.getInfo().getAgentID() + "','10','"+ prodshort.trim() +"',2) ";

			        		              PreparedStatement st2 = con.prepareStatement(sql5);
			        		              st2.executeUpdate();	              
			        		              st2.close();
		        	  }		        	  
		              }
		        
		        
		        
     
              
            }
	          
	          
	          
	          
	          
	          
	          
	          
	          
	          
	          
	          
	          
	          
	          PreparedStatement ps = con.prepareStatement(sql);

	           
	           for (Iterator it = wr.getInfo().getLines().iterator(); it.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(it.next());
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
	             ps.setDouble(20, lw.getLine().getAction1Eval());
	             ps.setDouble(21, lw.getLine().getAction2Eval());
	             ps.setDouble(22, lw.getLine().getActionTotalEval());
	             ps.setDouble(23, lw.getLine().getCustEval());
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

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
	             ps.setInt(35, lw.getLine().getResInt3());
	             ps.executeUpdate();
	             System.out.println("lopp real");
	           }

	           ps.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	       this.connectionDown();
	     }



    public static String getDateAAAADDMMStr(Calendar cal,int add){
		StringBuffer sb = new StringBuffer();
		cal.add(Calendar.DAY_OF_YEAR,add);
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append("" + cal.get(Calendar.YEAR));
		sb.append("" + month);
		sb.append(day +"");
		return sb.toString();
	}

    private String getSisu(int week , int asi ,String  paev ,String type, int lisa){
    	Calendar cal = Calendar.getInstance();
    	int today = cal.get(Calendar.DAY_OF_WEEK);
    	int pp = 1;
    	if(paev.equals("E")){
    		pp = 2;
    	}
    	if(paev.equals("T")){
    		pp = 3;
    	}
    	if(paev.equals("K")){
    		pp = 4;
    	}
    	if(paev.equals("N")){
    		pp = 5;
    	}
    	if(paev.equals("R")){
    		pp = 6;
    	}
    	
    	String tyyp = "Kyl.";
    	if(type.equals("C")){
    		tyyp = "Hel.";
    	}
    	
    	
    	
    	Calendar cc = Calendar.getInstance();
    	cc.add(Calendar.WEEK_OF_YEAR,lisa );
    	cc.set(Calendar.DAY_OF_WEEK , pp);
    	//kui on sama n�dal siis vaatame kas p�ev on k�rgem 
    	
    	if(week == asi && today < pp){

        	//System.out.println("Sama " + tyyp + " " + Converter.getDDpMMpYYYYpString(cc) + " " + lisa + " " + pp );
    		return tyyp + " " + Converter.getDDpMMpYYYYpString(cc) ;   		
    	}else if(week != asi){
    		//System.out.println("Eri " + tyyp + " " + Converter.getDDpMMpYYYYpString(cc) + " " + lisa + " " + pp  + " " + week);
    		
    		 return tyyp + " " + Converter.getDDpMMpYYYYpString(cc) ;  
    	}
    	
    	return null ;
    }
    
    
    public String getExcelDir(){
        String dir = getParent().getProps().getProperty("excel.dir");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    
    private HashMap sortimendid = null;

    
    public TreeMap getCustomers(Agent agent){

  	  sortimendid = new HashMap();
        
        File fff = new File(this.getExcelDir() + "/Sortiments.xlsx" );
        TreeMap groups = new TreeMap(); 
    	try{
            FileInputStream ff = new FileInputStream(fff);
          XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheet("shops");
			XSSFRow row = null;
    		
    		row = sheet.getRow(0);
    	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
    		
    		
    		
    		for(int a = 1 ; a < sheet.getLastRowNum()+1;a++){
    			try{
    			//	Loeme sisse rea
    				row = sheet.getRow(a);
    				if (row.getCell((short) 0) != null) {
    				


    	    			   String kood = null;
    	    			  if(row.getCell((short)0) != null){
  	    	    			  	try{
  	    	    			  	  kood = row.getCell((short)0).getStringCellValue();  
  	    	    			  	 }catch (Exception e){
  	    	    			   	  kood = row.getCell((short)0).getNumericCellValue()+"";  
  	    	    	    		} 
  	    	    			  //	System.out.println("kood0 " + kood );
  	    	    			  	 if(kood.indexOf(".")!= -1){
  	    	    			  		 int ii = kood.indexOf(".");
  	    	    			  	 	 kood = (kood.substring(0,ii) + kood.substring(ii+1,kood.indexOf("E")));	
  	    	    			  	 }
  	    
  	    	    				//System.out.println("kood1 " + kood );
  	    	    		    	
  	    	    			  	 String sort = null;
  	    	    			  	try{
  	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
  		    	    			  	 }catch (Exception e){
  		    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
  		    	    	    		} 
  		    	    			   if(sort.indexOf(".")!= -1){
    	    	    			  		 int ii = sort.indexOf(".");
    	    	    			  	 	 sort = (sort.substring(0,ii) + sort.substring(ii+1,sort.indexOf("E")));	
    	    	    			  	 }
  		    	    			   
  	    	    			  	 groups.put(kood,sort);
  	    	    			  	if(kood.length() == 9  || kood.length() == 8){
		    	    				   kood = kood + "0";
		    	    				   groups.put(kood,sort);
		    	    				   kood = kood + "0";
		    	    				   groups.put(kood,sort);
		    	    			   }
  	    	    			  	if(kood.startsWith("102851")){
  	    	    			  	 System.out.println("102851010 " + kood + " " + sort);
  	    	    			  	}
    	    			 
    	    				
    				}	   
    				}
   			}catch (Exception e){
    			//	System.out.println(row.getCell((short)a).getNumericCellValue());
    			}
    		}
    		
    	//	System.out.println(groups.toString());
    
    	 }catch (Exception e){
    		e.printStackTrace();
    	}

    	
    	
    	
    	
    	
    	int week = 0;
    	  TreeMap custagents = new TreeMap();
    	try {
    		FileReader reader = new FileReader("kliendiagent.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()) {
					custagents.put(st.nextToken(),st.nextToken());
				}
			}
    	}catch(Exception rr ){
    		rr.printStackTrace();
    	}
		System.out.println("custagents siZe " + custagents.size());
		
    	
    	//k�lastusp�eva leidmine 
    	  Vector thisweek = new Vector ();
    	  TreeMap custemps = new TreeMap();

    	  TreeMap custsort = new TreeMap();
    	  TreeMap custwhen = new TreeMap();
    	 
    	  connectionUp(url);
    	  
  			 Statement st = null;
  		   ResultSet	      rs = null;
  			try {   
  				
  			    //ajaarnamise algus 
  			  
  			  Calendar dd = Calendar.getInstance();
  			
  			   week = dd.get(Calendar.WEEK_OF_YEAR);
  			   week = week + 6; //siin m��rad nende ajaarvamise.
  			 System.out.println("result week abs " + week);
  			  if(week >12){
  				  for (;week > 12 ; week = week -12){
  				  }
  			  }
  			  System.out.println("result week no " + week);
  			  
  			  //ajaarvamise l�pp
  			    
  			  String custsql = "select ScalaCode as klient,  substring(ContactPlan," + week + ",1) as ag from PlanClient " +
  			  		"inner join  SL01"+area+"00 on SL01001 = ScalaCode where   substring(ContactPlan," + week + ",1) != '' " ; 
  			  System.out.println(custsql);
  			  
    			try {   
    				st = con.createStatement();
    			      rs = st.executeQuery(custsql);
    			    while(rs.next()){
    			    	String ccc = rs.getString("klient").trim();
    			    	thisweek.addElement(ccc);
    			    
    			    Agent aa = (Agent)getParent().getAgentsHash().get(rs.getString("ag").trim());
    			    if(aa != null){
    			    	aa.addTodayCusts(ccc);
    			    }
    			    }
    			    
    			    rs.close();
    			    st.close();  
    			}catch(Exception e){
    		  		e.printStackTrace();
    	  		}
 	  			
    			
    			
    			  String cuemsql = " select ScalaCode as klient,Salesman as agent from PlanClient order by  ScalaCode,Salesman "; 
    			 // where contactplan != '' 
    			  //Siit saadakse k�ik agendid klientidele 
      			try {   
      				st = con.createStatement();
      			      rs = st.executeQuery(cuemsql);
      			    while(rs.next()){
      			    	String cc = rs.getString("klient").trim();
      			    	String em = rs.getString("agent").trim();
      			    	Vector vv = null;
      			    	if(!custemps.containsKey(cc)){
      			    		vv = new Vector();
      			    		vv.addElement(em);
      			    		custemps.put(cc, vv);
      			    	}else{
      			    		Vector va = (Vector)custemps.get(cc);
      			    		va.addElement(em);
      			    		custemps.put(cc, va);
      			    	}
      			    }
      			    rs.close();
      			    st.close();  
      			}catch(Exception e){
      		  		e.printStackTrace();
      	  		}
      	
      			
      			
      		  String custtm = "select Client,Stop,WeekDay from PlanContact where WeekNo = '" + week + "' and ContactType='V' " +
      		  		"group by Client,Stop,WeekDay order by Client,Stop,WeekDay "; 
	  			
    			try {   
    				st = con.createStatement();
    			      rs = st.executeQuery(custtm);
    			    //  System.out.println();
    			    while(rs.next()){
    			    	String cc = rs.getString("Client").trim();
    			    	String em = rs.getString("Stop").trim();
    			         if(em.length()==1){
    			        	 em = "0" + em;
    			         }
    			         String pa = rs.getString("WeekDay").trim();
    			    
    			    		custsort.put(cc, em);
    			    		if(custwhen.get(cc) == null){
    			    			custwhen.put(cc, pa);	
    			    		}else{
    			    			String ss = (String)custwhen.get(cc);
    			    			custwhen.put(cc, ss+pa);
    			    		}
    			    	
    			    }
    			    rs.close();
    			    st.close();  
    			}catch(Exception e){
    		  		e.printStackTrace();
    	  		}
      			
    			

    	  		System.out.println("Custsort size " + custsort.size());
  		}catch(Exception e){
  		e.printStackTrace();
  		}
  			
  			
  		System.out.println("Thisweek size " + thisweek.size());
  		
  		//jooksev n�dal tuleb kus tuleb k�ija see nadal
  	//	select client,w9 from plancontactexcel where w9 != ''
  		
  	//	ja vaata ka kas n�dala p�ev on olemas kui jah siis pane 1 
    	
  		
  		// arvutame v�lja 5 j�rgmise n�dala j�rgmise visiidi calli  ja paneme objekti 
         TreeMap nextContact = new TreeMap();  	
          	
    	
    	
         for (int uu = 0 ;uu<6;uu++){
        	 
        	 int asi = week + uu;
        	 if(asi > 12){
        		 asi = asi-12;
        	 }
         
 		  String custtm = "select Client,WeekDay,ContactType  from PlanContact where WeekNo in ('" + asi + "') and ContactType in ('V','C') " +
      		  		" order by Client,WeekNo,case when WeekDay ='E' then '00' when WeekDay ='T' then '01'  when WeekDay ='K' then '02' "  +
					" when WeekDay ='N' then '03' when WeekDay ='R' then '04' when WeekDay ='L' then '05' when WeekDay ='P' then '06' end "; 
		//System.out.println(custtm);
		try {   
			st = con.createStatement();
		      rs = st.executeQuery(custtm);
		     
		    while(rs.next()){
		    	String cc = rs.getString("Client").trim();
		         String pa = rs.getString("WeekDay").trim();
		         String ty = rs.getString("ContactType").trim();
		    	if(nextContact.get(cc + "" + ty) == null){
		    			String sisu = getSisu(week , asi , pa , ty, uu);
		    			if(sisu != null){
		    				nextContact.put(cc+""+ty, sisu);	
				    	}
		    			}
		    }
		    rs.close();
		    st.close();  
		}catch(Exception e){
	  		e.printStackTrace();
		}
		
		
   }         
  		
  		
  		//System.out.println("Uhuu " + nextContact.size());
  		
        connectionUp2(url);
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL01003 + ' (' + SL01004 + ')') AS aadress,"
        		+ "SL01003 as tanav,SL01004 as linn,SL01005 as regioon,SL01083 as indeks, SC45004 AS ale,SL01036 AS ale2 , " +
        "   ' ' AS visit, SL22005 as paym ,SL01084 as tyyp, SL01029 as klgrupp, SL01075 as block ,SL01056 as payer, " +
        "  WeekDay AS millal  ,SL01006 + ' ' + SL01011 as info ,SL01193 as pemail ,SL01012 as credit,SL01021  as leping, SL01005 as region" +
        " , SL01004 as city , case when SL01072 = '98' then 'COOP' else (select SL010100_PEAKLIENT.SL01002 from SL010100_PEAKLIENT where SL010100_PEAKLIENT.SL01001=SL010100.SL01056 ) end as chain " +
		"  FROM SL01"+area+"00 left JOIN SC45"+area+"00 " + 
		"   ON SL01036=SC45003    LEFT OUTER JOIN SL14"+area+"00 ON SL01001=SL14001 AND SL14002='00'" +
        "  LEFT OUTER JOIN SY24"+area+"00 ON SL01035=SY24002 AND SY24001='BK'" +
        "  LEFT OUTER JOIN SL22"+area+"00 ON SL01024=SL22002 AND SL22001='0' " +
		"   inner join PlanClient pl ON pl.ScalaCode=SL01001 " +
        "  WHERE len(SL01001)>3  and len(SL01002) >0 and (rtrim(SL01071) not like'%Z' and  rtrim(SL01071) not like'%X')  ORDER BY SL01002 ";
        

        connectionUp(url);
        String cmd3 = "SELECT SL01056 as pea   FROM SL01"+area+"00 left JOIN SC45"+area+"00   ON SL01036=SC45003 " +
                  "  WHERE SL01002 != '' AND SL01035 != ''  and SL01056 != '' ORDER BY SL01002";
	
      log(cmd);

	Vector vec = new Vector();
  StringBuffer sb = new StringBuffer();
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
	    System.out.println("vecsize " + vec.size());
	// vektoris on klindid kellel on peaklient m�rgitud ja neile ei pane kaasa allahindlust
	
        String    sql = "SELECT SL01008 AS custid,'' AS clgroup,SUBSTRING(SC18002,11,35) AS prodid,'' AS prodgroup,SC18009, " +
        "	(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price  " +
        "   FROM SC18"+area+"00 INNER JOIN SL01"+area+"00 ON  left(SC18002,4)= SL01001 LEFT JOIN SY24"+area+"00 on SC18009=SY24002 and SY24001='BG' WHERE " +
         "  SC18001='C' AND CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011   and len(SL01008) >0  " +
         " order by SL01008,SUBSTRING(SC18002,11,35)";

        
          try {
           
            st = con.createStatement();
             rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
              String sss = rs.getString("SC18009");
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

            // siisn l�hevad ainult need kilendid kellel pole peaklienti
            
            rs.close();
            
            
            String sqq = "       SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35)  AS prodid, SC18008 AS price  " +
     "   FROM SC18"+area+"00 WHERE  SC18001='C' AND " +
     "   CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011 " +
     "   and LEFT(SC18002,10) like '%P%'";
            /*      SELECT  LEFT(SC18002,10) AS clgroup,SUBSTRING(SC18002,11,35) + '03' AS prodid, SC18008 AS price 
            FROM SC18"+area+"00 WHERE  SC18001='C' AND
            CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN  SC18010 AND SC18011
            and LEFT(SC18002,10) like '%P%'*/
            
            rs = st.executeQuery(sqq);

            while (rs.next()) {

                String clGroup = rs.getString("clgroup").trim();
                String prodID = rs.getString("prodid").trim();
  	   
                double price = rs.getDouble("price");
                MatrixLine mline = new MatrixLine();
      
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
            
            //Siin panime �ra PEAKLIENDI SOODUKAD ja kasutame seal pricergroupi
            
            
            
            String sql2 = "select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001))  AS prodid,' ' AS prodgroup, " +
            " case when SC39006 <= getdate() AND (SC39007 >= getdate() OR SC39007 = null) then " +
            " SC39005 else SC39004 end AS price  " +
            " FROM SC39"+area+"00 WHERE SC39002 not in ('00') order by SC39002,SC39001 ";
            rs = st.executeQuery(sql2);
/*
select '' AS custid ,SC39002 AS clgroup , rtrim(ltrim(SC39001)) + '03'  AS prodid,' ' AS prodgroup, 
             

case when CAST(CONVERT(char(8), getdate(),112) AS datetime) BETWEEN SC39006 and SC39007 then 
             SC39005 else SC39004 end AS price  
             FROM SC39"+area+"00 WHERE SC39002 != '00' order by SC39002,SC39001 */
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
             prlines.add(mline);  // Kommentisin v�lja kuniks uus hinna skeem hakkab toimima
            }

            rs.close();
            st.close();
            
            
            
            
            
            
            
            
            
            
            
            connectionDown();
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
   
        
        
        
        
        
	log(cmd);

	TreeMap customers = new TreeMap();
        connectionUp();
	try {
           PreparedStatement stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	   	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim();
		nimi = nimi.substring(0,1) + nimi.substring(1).toLowerCase();
		String nimi2 = nimi;
                Customer cust = new Customer(rs.getString("kood").trim());
                String addr = rs.getString("aadress").trim();
                  cust.setAddress(Converter.adressLink(Converter.enCodeEst(addr),"Estonia"));
                  nimi =  Converter.enCodeEst(nimi + " " + addr);
                
                  
                  //SL01003 as tanav,SL01004 as linn,SL01005 as regioon,SL01083 as indeks
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
                
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                 // cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	//cust.setDiscount(17);
                	
                }
             //   cust.setVisit(rs.getString("visit").trim());
              //  String agen = rs.getString("agent");
                //if(agen != null){
                    // cust.addAgent(new Agent(agen.trim()));
                  // }
                if(custemps.containsKey(cust.getID())){
                	Vector vvv = (Vector)custemps.get(cust.getID());
                       Enumeration en = vvv.elements();
                       while (en.hasMoreElements()){
                    	   String aa = (String)en.nextElement();
                    	   if(!aa.equals("J")){

                        	   Agent as = (Agent)parent.getAgentsHash().get(aa);
                        	   if(as != null){
                            	   cust.addAgent(as);   
                        	   }   
                        	   
                        	   
                        	   if(custagents.containsKey(aa)){
                               	String vvvs = (String)custagents.get(aa);
                                      int ens = vvvs.length();
                                      for (int asa = 0;asa<ens;asa++){
                                   	   String aas = vvvs.substring(asa,asa+1);
                                   	      Agent ass = (Agent)parent.getAgentsHash().get(aas);
                                       	   if(ass != null){
                                       		   if(!aas.equals("J")){
                                       			 cust.addAgent(ass);   
                                       		   }
                                           	    
                                       	   }
                                    }
                               }
                        	   
                        	   
                        	   
                        	   
                        	   
                        	   
                    	   }
                        }
                     }
             
                
                
                if(custagents.containsKey(cust.getID())){
                	String vvv = (String)custagents.get(cust.getID());
                       int en = vvv.length();
                       for (int a = 0;a<en;a++){
                    	   String aa = vvv.substring(a,a+1);
                    	   if(!aa.equals("J")){
                    		   Agent as = (Agent)parent.getAgentsHash().get(aa);
                        	   if(as != null){
                            	   cust.addAgent(as);  
                            	  // System.out.println("Lisan eraldi agendi" + aa + " " + cust.getID());
                        	   }
                    	   }
                     }
                }
                
                
                
                String paym = rs.getString("paym");
                if(paym != null && paym.trim().length()>0){
                	cust.setPaymTerm(paym.trim());
                }
                
                boolean tyyp = false;
                String typ = rs.getString("tyyp");
                if(typ != null && ( typ.trim().equals("88"))){ // typ.trim().equals("66") ||
                	tyyp = true;
                }
                if( tyyp){
                	  cust.addAgent(new Agent("J"));
                }
               
              //  cust.addAgent(new Agent("HOUSE"));
              
              
                
                if (cust.getID().equals("0100000")) {
                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        }
                      }
             
                cust.setGroup(rs.getString("klgrupp"));
                
                
                String gro = (String) groups.get(cust.getID());
                if( gro != null && gro.trim().length() > 0){
                	cust.addFixSortCode(gro);
                	//System.out.println("Settin fixsorti " + gro);
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
             
                
                String millal = rs.getString("millal");
                if(custwhen.get(cust.getID())!= null){
                	millal = (String)custwhen.get(cust.getID());
                }
                
               

                if(millal != null && millal.trim().length()>0){   

            		if(isToday(millal) && thisweek.contains(cust.getID())){
                		cust.setVisitToday(true);
                		 if(custsort.get(cust.getID()) != null){
                         	String sort = (String)custsort.get(cust.getID());
                         	//cust.setSortCode(sort);
                         	cust.setName(sort + " " + cust.getName());
                         }		
                		
                	} else  if (thisweek.contains(cust.getID()) && (!isToday(millal) || millal.length()>1)){
                		cust.setVisit(millal);
                	}
                 
            	
                }
                
               
                
                
                if(custsort.get(cust.getID()) != null){
                 	String sort = (String)custsort.get(cust.getID());
                 	cust.setSortCode(sort);
                 //	cust.setName(sort + " " + cust.getName());
                 }
                
                String inff = rs.getString("info");
                if(inff != null && inff.trim().length() > 0 ){
                	cust.setPersonalData(Converter.enCodeEst(inff.trim()));
                }
                
                String email = rs.getString("pemail");
                if(email != null && email.trim().length() > 0 ){
                	cust.setPersonalData(cust.getPersonalData() + "|" +Converter.enCodeEst(email.trim()));
                }
                
                try{
             	   double credit = rs.getDouble("credit");
                    if(credit > 0 ){
                    	cust.setCredit(credit + "");              	
                    }   	
             }catch(Exception e ){
             	
             }
          
             String inff2 = rs.getString("leping");
             if(inff2 != null && inff2.trim().length() > 0 ){
             	cust.setCity(Converter.enCodeEst("Leping: " + inff2.trim()));
             }
             
             if(nextContact.get(cust.getID() + "V") != null){
            	 if(cust.getCity() != null && cust.getCity().trim().length() > 0 ){
            		 cust.setCity(cust.getCity() + "|" +nextContact.get(cust.getID()+"V") + "");
            	 }else{
            		 cust.setCity(nextContact.get(cust.getID()+"V") + "");
            	 }
             }
             
             if(nextContact.get(cust.getID() + "C") != null){
            	 if(cust.getCity() != null && cust.getCity().trim().length() > 0 ){
            		 cust.setCity(cust.getCity() + "|" +nextContact.get(cust.getID()+"C") + "");
            	 }else{
            		 cust.setCity(nextContact.get(cust.getID()+"C") + "");
            	 }
             }
             
             
             String region = rs.getString("region");
             if(region != null && region.trim().length() > 0 ){
             	region = region.trim();
             	
             }else{
            	 region = " ";
             }
             
             String city = rs.getString("city");
             if(city != null && city.trim().length() > 0 ){
            	 city = city.trim();
             	
             }else{
            	 city = " ";
             }
             
             
             
             String chain = rs.getString("chain");
             if(chain != null && chain.trim().length() > 0 ){
            	 chain = chain.trim();
             }else{
            	 chain = " ";
             }
             
           cust.setChain(chain);  
             
             
             
             
             
             sb.append(cust.getID() + ";" + nimi2 + ";" + addr  + ";" + cust.getPayer() 
            		 + ";" + city + ";" + region + ";" + chain + "\n");
             
                //otsime v�lasumma igale kauplusele 
                
                String sql6 = " SELECT sum(SL03014-SL03054) AS debt " +
  " FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
             "    NOT(SL03014=SL03054)  AND SL03006<=getdate() and SL01001='" + cust.getPayer()+ "'  GROUP BY SL01001 ";

   Statement stm = con2.createStatement();

   ResultSet rs3 = stm.executeQuery(sql6);
   while(rs3.next()){    
     double debt = Double.parseDouble(rs3.getString("debt").trim());
       cust.setDebt(cust.getDebt() + debt);
       if(cust.getDebt() > -1 && cust.getDebt() < -4){
    	   cust.setDebt(0);
       }
   }
   String sql7 = " SELECT sum(SL03014-SL03054) AS debt " +
   " FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
              "    NOT(SL03014=SL03054)  and SL01001='" + cust.getPayer()+ "'  GROUP BY SL01001 ";


   //otsime kasutatud krediidi igale kauplusele 
   rs3 = stm.executeQuery(sql7);
   while(rs3.next()){    
     double debt = Double.parseDouble(rs3.getString("debt").trim());
       cust.setUsedCredit(cust.getUsedCredit() + debt);
       if(cust.getUsedCredit() > -1 && cust.getUsedCredit() < -4){
    	   cust.setUsedCredit(0);
       }
   }
   rs3.close();
   stm.close();

                
   if(cust.getDebt()<0.02 && cust.getDebt() > 0 ){
   	   cust.setDebt(0);
     }           
        cust.setCountry("Eesti");
	   customers.put(cust.getName(), cust);
      
              
	    }
            rs.close();
            stmt.close();
            if(sb.length() > 2){
                File f = new File("custmaster.txt");
                if (f.exists())f.delete();
                  try {
            		PrintWriter os = new PrintWriter(new FileWriter(
            				"custmaster.txt", true));
            		os.write(sb.toString());
            		os.close();
            	  }catch (IOException ex) {
            		ex.printStackTrace();
            	 System.exit(0);
            	  }
             }
            
            if(rcompany != null){
            //	Converter.sendCustomerJson(customers, company);
                	
            }

            
  String uus = "select ScalaCode , Salesman, VisitPattern,WeekDay from PlanClient where  VisitPattern != '' order by 1";

  uus= "select Client as ScalaCode, Employee as Salesman ,WeekDay,WeekNo,ContactType  from PlanContact where  ContactType in ('V','C') " +
		" order by Client,WeekNo,case when WeekDay ='E' then '00' when WeekDay ='T' then '01'  when WeekDay ='K' then '02' " +
	" when WeekDay ='N' then '03' when WeekDay ='R' then '04' when WeekDay ='L' then '05' when WeekDay ='P' then '06' end ";
  
  connectionUp(url);
  stmt = con.prepareStatement(uus);

  Vector visits = new Vector();
   rs = stmt.executeQuery();
TreeMap asjad = new TreeMap();
//Paneme agentide l�ikes ja siis seal sees kliendi l�iles 
  while(rs.next()){
    String cust = rs.getString("ScalaCode").trim();
    String agg = rs.getString("Salesman").trim();
  //  String pat = rs.getString("VisitPattern");
    String day = getDay(rs.getString("WeekDay").trim());
    String weekno = rs.getString("WeekNo").trim();
    String type = rs.getString("ContactType").trim();
	  Agent ddd = new Agent(agg);
	  ddd.setDays(day);
	  ddd.setCode(type);
	  TreeMap custs = new TreeMap();
		 TreeMap wns = new TreeMap();
			 Vector ags = new Vector();
			 if(asjad.get(agg) != null){
				   custs = (TreeMap) asjad.get(agg);
				   if(custs.get(cust) != null){
					   wns = (TreeMap)custs.get(cust); 
					   if(wns.get(weekno) != null){
						   ags = (Vector)wns.get(weekno);
					   }
				   }
			  }
	     ags.add(ddd);
	    wns.put(weekno, ags);
	   custs.put(cust,wns);
	  asjad.put(agg,custs);
    }
    
  //meil on treemap  agent id ja seal treemap custid seal treemap n�dala numbritega ja vector sees
  
  
    Iterator itera = asjad.keySet().iterator();
    while (itera.hasNext()){
    String agg = (String)itera.next();
  
  TreeMap custs = (TreeMap)asjad.get(agg);
  
  Iterator iterax = custs.keySet().iterator();
  while (iterax.hasNext()){
	  String cust = (String)iterax.next();
	  
	  TreeMap wns = (TreeMap)custs.get(cust);
	  
	  
  
  StringBuffer weekObj = new StringBuffer();
  
  weekObj.append( "{\"json\": {\"customer\": \"" + cust + "\",\"weeks\": [ ");
  String com = ",";
  for(int y = 0; y<12; y++){
  	weekObj.append("{");
  	

	  Iterator iteray = wns.keySet().iterator();
	    while (iteray.hasNext()){
		
		  String weekno = ((String)iteray.next());
		  int  weekn = Integer.parseInt(weekno);
		  Vector ags = (Vector)wns.get(weekno);
  	     if(weekn == y+1){
  	    	 //siia vectoris l�bi ja kylgejama 
  	    	 for(int u = 0; u < ags.size(); u++){
  	    		Agent aa = (Agent)ags.elementAt(u);
  	    		String coma = "";
  	    		if(u > 0){
  	    			coma = ",";
  	    		}
  	    		if(aa.getCode().equals("V")){
  	    		  weekObj.append(coma + "\""+ (aa.getDays()) + "\": \"Visit\"");	
  	    		}else{
  	    		  weekObj.append(coma + "\""+ (aa.getDays()) + "\": \"Call\"");	
  	    		}
  	    	 }
  	     }
   
  	     /*  char ss = pat.charAt(y);
      if(ss =='V'){
      weekObj.append("\""+ day+ "\": \"Visit\"");	
      }
      if(ss== 'C'){
          weekObj.append("\""+ day+ "\": \"Call\"");
      }*/
  	
	    }
     	if(y==11){
     		com = "";
     	}
      weekObj.append("}"+ com);
      
  }
  
  weekObj.append("],\"manager\": \"" + agg + "\" }}");
 
	try{
		visits.add( new JSONObject(weekObj.toString()));	
	}catch(Exception e){
		System.out.println(weekObj.toString());
		e.printStackTrace();
		
	}
  
  
	  
  
  }
  
	
    }
  
  
  //System.out.println("visits " + visits.size()+" \n"+ visits.elementAt(0) 
  //+ "\n" +visits.elementAt(1)
  //+ "\n" +visits.elementAt(2));

  if(rcompany != null){
	 Converter.sendRouteCalsJson(visits,user,pass,rcompany);
  }
 
   rs.close();
  stmt.close();

  
        } catch (Exception e) {
            getParent().showError(e);
        }
        
	 
	
	
        
	return customers;
    }
    
    
    private String getDay(String d){
    	
    	if(d.equals("E")){
    		return "Mon";
    	}
    	
    	if(d.equals("T")){
    		return "Tue";
    	}
    	
    	if(d.equals("K")){
    		return "Wed";
    	}
    	
    	if(d.equals("N")){
    		return "Thu";
    	}
    	
    	if(d.equals("R")){
    		return "Fri";
    	}
    	
    	if(d.equals("L")){
    		return "Sat";
    	}
    	
    	if(d.equals("P")){
    		return "Sun";
    	}
    	return null;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,(SL03014-SL03054)  AS summa ,-datediff(day ,getdate(),SL03006) as yle " +
      		" FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03054) AND " +
             "      LEN(LTRIM(SL03014))>0 AND SL03006<GETDATE()+7  and (SL03014-SL03054) !=0 ORDER BY SL01001 ";
				

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
        
       TreeMap groups =  null;
	try {
		groups = this.getProdGroups();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
        
        String  cmd0 = "  SELECT  RTRIM(SC03001) AS toode , '10' AS LADU ,SC03003 AS stock   " +
	      "  FROM SC03"+area+"00  WHERE SC03002='93' AND SC03003!=0 ORDER BY SC03001" ;
      this.connectionUp(url);
      HashMap hh = new HashMap();
	       if (isDebug()) System.out.println(cmd0);
	       try {

	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd0);

	         while (rs.next()) {
	        	  String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           if(seis > 0){
		        	  hh.put(kood,(int)seis + ""); 
		           }	 
	         }

	       }catch(Exception e){
	    	   e.printStackTrace();
	       }
        System.out.println("suurus 93 laos " + hh.size());

        

        
        
        // v�tame Equinoxi wievst andmed 
        String  cmdora = "  select toode as tood, sum(to_number(seis,'99999999.999')) as kogus,partii from( " +
" select SKUID as toode , (QUANTITY_UOM) as seis, SHELFLIFE as partii from " +
 " V_INVDETATTR2 where SUBSTR(LOCATION,1,2) in ('SV','SS','RV','RS','TS') and SHELFLIFE is not null ) " +
" tbl group by toode,partii " +
" order by tbl.toode,tbl.partii ASC" ;

      HashMap hhora = new HashMap();
	       if (isDebug()) System.out.println(cmdora);
	       try {
               Connection ora = this.getConnectionORA();
	         Statement st = ora.createStatement();
	         ResultSet rs = st.executeQuery(cmdora);
	         Calendar now = Calendar.getInstance();
	         now.add(Calendar.DAY_OF_YEAR, 40);

	         while (rs.next()) {
	        	  String kood = rs.getString("tood").trim();
	        	  Toode tt = new Toode(kood);
		           double seis = rs.getDouble("kogus");
		           tt.setKogus(seis);
		           Date par = rs.getDate("partii");
		           String red = "";
		           if(par.before(now.getTime())){
		        	   red = "@";
		           }
		           tt.setBrand(red + Converter.getYYYYpMMpDDpString(par));
		          if(seis > 0){
		           if(hhora.get(kood) != null){
		        	   Vector vv = (Vector)hhora.get(kood);
		        	   vv.add(tt);
		        	   hhora.put(kood,vv); 
		           }else{
		        	   Vector vv = new Vector();
		        	   vv.add(tt);
		        	   hhora.put(kood,vv);  
		           }
		         }	 
	         }

	       }catch(Exception e){
	    	   e.printStackTrace();
	       }
      //  System.out.println("suurus EQ laos " + hhora.size());

        
        
        
        
        String text = "  and SC01015 != '' " ;
        if(getParent().getProps().getProperty("merc")!= null){
        	text = " and  SC01018 != '' " ;
        }
        
        
        String cmd = " SELECT  RTRIM(SC01001) AS toode,SC01015 as sort, SC01037 + ' ' +RTRIM(SC01002) AS nimi,SC01019 as abi,SC39004 AS myygihind ,SC01013 " +
        	  "  AS tykke , SC03003-SC03004 AS stock, SC01017 AS maxale, ' ' AS pandikas , " +  
              "  case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 AS grupp ,SY24003 AS liik,SC01060 AS ean  ,SC01144 as vat , SC01012 as opt " +
              "  FROM SC01"+area+"00 left outer JOIN SC03"+area+"00 on SC03001=SC01001 and SC03002='10' AND SC03003!=0 left outer JOIN SC39"+area+"00 ON SC01001 = SC39001 and SC39002='00'  " +
             " left join SY240100 sy on sy.SY24002 = SC01037 and RTRIM(SY24003)<>'' AND SY24001 = 'IB' " +
              "  WHERE   LEN(LTRIM(SC01037))>0 and rtrim(ltrim(SC01016)) != 'S' " +
               text + "  ORDER BY RTRIM(SC01037),SC01002";
        
        //V�tsin maha and SC39004 > 0  kuna yks hinnairir niikui nii
        
	if (isDebug()) {
	    System.out.println(cmd);
	}
	  StringBuffer sb = new StringBuffer();
	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String sort = rs.getString("sort");
		if(sort == null || sort.trim().length() == 0){
			sort = "0000";
		}
		
		String nimetus =  Converter.enCodeEst(rs.getString("nimi").trim());
      
		String abi = rs.getString("abi");
		if(abi != null){
			abi = abi.trim();
		}else{
			abi = "";
		}
		
               String space = "      ";
               if(abi.length()==1){
            	   space = "     ";
               }
               if(abi.length()==2){
            	   space = "    ";
               }
               Product prod = new Product(kood);
               if(hh.get(kood) != null){
           		nimetus = nimetus + " <BR:" + hh.get(kood)+ ">";
           		prod.setGroupSecond("TAG");
           		}
               prod.restName = nimetus;
                prod.setName(sort + abi + space  + nimetus );
                
                if(abi.indexOf("A")!=-1){
               	  prod.setType("A"); 
                  }

                if(abi.indexOf("B")!=-1){
               	  prod.setType("B"); 
                  }

                if(abi.indexOf("C")!=-1){
               	  prod.setType("C"); 
                  }

               
              try{
            	  prod.setPrice(parseDouble(rs.getString("myygihind").trim())); 
              }catch(Exception e){
            	  
              }
                
                prod.setInCases(parseDouble(rs.getString("tykke").trim()));
                prod.setStock(rs.getDouble("stock"));
                String mmmm = rs.getString("maxale");
              try{
            	  if(mmmm != null){
            	  double maxdisc = Double.parseDouble(mmmm);
                  if(maxdisc >0 ){
                       prod.setMaxDiscount(maxdisc);	
                  }else{
                  	 prod.setMaxDiscount(-1);
                  }
                  
            	  }
              }catch(Exception e){
            	  
              }
               
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
                  prod.setGroupname(rs.getString("liik").trim());
               //   prod.restGroup = gr.trim();
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>0){
                	if(gr.length()>13){
                		gr = gr.substring(0,13);
                	}
                  prod.setEAN(gr.trim());
                  try{
                	   prod.setName(prod.getName() + "." + prod.getEAN().substring(prod.getEAN().length()-3));	  
                  }catch(Exception e){
                	  
                  }
               
                }
                
                
                String vat = rs.getString("vat");
                if(vat != null && vat.trim().length()>0){
                	if(vat.trim().equals("1")){
                		prod.setVat("20");	
                	}else if(vat.trim().equals("2")) {
                		prod.setVat("9");
                	}             
                  }               
                
                String opt = rs.getString("opt");
                if(opt!= null){
                	try{
                		prod.setMinAmountInUnits(Integer.parseInt(opt.trim()));  
                	}catch(Exception k){
                		
                	}
                	          
                  }   
                
                
                if(hhora.get(prod.getID()) != null){
                	Vector vv = (Vector)hhora.get(prod.getID());
                	String par = null;
                	for(int i = 0;i<vv.size();i++){
                		Toode tt = (Toode)vv.elementAt(i);
                		if(par == null){
                			par = "[" + tt.getKogus() +"] " + tt.getBrand();
                		}else{
                			par = par + "|[" + tt.getKogus() +"] " + tt.getBrand();
                    	}
                	
                		if(i == 1){
                			break;
                		}
                    		
                	}
                	if(par != null){
                		prod.setPartii(par);
                	}
                	
                }
                
                
               // sb.append(prod.getID() + ";" + prod.getName() + ";" + prod.getGroup()  + ";" +  
               	//	  "\n");
              
                
		products.put(prod.getName(), prod);

	    }
            rs.close();
            st.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

		//Converter.makeTelemaFiles(this.getOutDir(), products, groups, null, "9961002889608");
		
		  if(sb.length() > 2){
              File f = new File("prodmaster.txt");
              if (f.exists())f.delete();
                try {
          		PrintWriter os = new PrintWriter(new FileWriter(
          				"prodmaster.txt", true));
          		os.write(sb.toString());
          		os.close();
          	  }catch (IOException ex) {
          		ex.printStackTrace();
          	 System.exit(0);
          	  }
           }

	
	
	return products;
    }

    private String getOutDir(){
        String dir = getParent().getProps().getProperty("readout");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }
    
    private boolean isToday(String aa){
    	String day =getDayFromDate(new Date());
    	if(aa.indexOf(day) != -1){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    
	public static String getDayFromDate(Date dat){
		String day = " ";
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dat);
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
		      day = "E";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
		      day = "T";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
		      day = "K";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
		      day = "N";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
		      day = "R";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
		      day = "L";
		    }
		 if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
		      day = "P";
		    }
		
		  	return day;
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
        	  
// ESITEKS VAATAMAE MIS ON MJ-ga OUT pandud listist objekti aeg 
        	  String sql3 = "select custid,prodid,sentdate from supervisor where ispartner = 1 and resint3 = -1 and sentdate > getdate()-540";
        	     
        	  
              Connection con2 = getNewConnection2();
              PreparedStatement   stmt2 = con2.prepareStatement(sql3);
        	
            HashMap aeg = new HashMap();
            ResultSet rs2 = stmt2.executeQuery();
            
          while (rs2.next()) {
            String kliendikood = rs2.getString("custid").trim();
            String tootekood = rs2.getString("prodid").trim();
            long dd = rs2.getDate("sentdate").getTime();          
            	aeg.put(kliendikood+tootekood, dd+"");
          }
            
            
      
      // Siin lisame listingud mida pole myydud aga on listingusse lisatud
      
     String sql2 = " select custid,prodid,sentdate,prodname from supervisor where comment like '%TO THE LIST%' ";

				if (isDebug()) {
				System.out.println(sql2);
				}
				 
     
     stmt = con.prepareStatement(sql2);
    // stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
     
     ResultSet   rs = stmt.executeQuery();

       while (rs.next()) {
         String kliendikood = rs.getString("custid").trim();
         String tootekood = rs.getString("prodid").trim();
         double kogus = 0;
         Date date = rs.getDate("sentdate");
         
         
         boolean ok = true;
         if(aeg.get(kliendikood+tootekood) != null ){
         	
         	String vv = (String)aeg.get(kliendikood+tootekood);
         	if(Long.parseLong(vv) > date.getTime()){
         		ok = false;
         	//	System.out.println("siin on sortimendi erand LISTINGUTE OMA  " + kliendikood + "  " + tootekood);
         	}
         
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
     
   
      
      
    //  Siin Paneme sortimendid Sisse 
            
          sql2 = "SELECT  SL01001 AS klient ,ST03017 AS toode ,ST03020 AS Kogus,ST03015  as lastdate ,max(ST03014) as arvenr " +
          "  FROM ST03"+area+"00 INNER JOIN SL01"+area+"00 ON ST03001 = SL01001 WHERE ST03015> ? AND ST03016>0 AND ST03020>0 " +
          " GROUP BY ST03017,SL01001, ST03020,ST03015 ORDER BY SL01001,ST03017 ,ST03015 ";

					if (isDebug()) {
					System.out.println(sql2);
					}
          
          
          stmt = con.prepareStatement(sql2);
          stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
          
           	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double kogus = Double.parseDouble(rs.getString("kogus").trim());
              Date date = rs.getDate("lastdate");
              String arvenr = rs.getString("arvenr");
              boolean ok = true;
              if(aeg.get(kliendikood+tootekood) != null ){
              	
              	String vv = (String)aeg.get(kliendikood+tootekood);
              	if(Long.parseLong(vv) > date.getTime()){
              		ok = false;
              	//	System.out.println("siin on sortimendi erand " + kliendikood + "  " + tootekood);
              	}
              
              }
              if(ok){

                  CustProd cp = new CustProd(kliendikood, tootekood, kogus,0 ,0,
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
            } 
            rs.close();
            stmt.close();

            
           
   

   int counter = 0;
   int cou = 0;
   String oldklient = null;
   String olddate = " ";
   String oldtoode = null;

  String cmd2 =  " SELECT  SL01001 AS klient , ST03017 AS toode ,sum(ST03020) AS Kogus,ST03015  as lastdate ,ST03022 as ale " + 
                     "  FROM ST03"+area+"00 INNER JOIN SL01"+area+"00 ON ST03001 = SL01001 WHERE ST03015> getdate()-180 AND ST03016>0 AND ST03020>0 " +				 
					 "  group by SL01001,ST03017,ST03015,ST03022 " +
                     "  ORDER BY SL01001,ST03017 ,ST03015 desc ";
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
        double ale = rs2.getDouble("ale");
        String aa = "";
        if(ale > 0){
        	aa = "(" +(int)ale + "%)";
        }
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
             if (counter == 0){cp.setS1(kogus +aa );}
             if (counter == 1){cp.setS2(kogus +aa);}
             //if (counter == 2){cp.setS3(kogus +aa);}
             //if (counter == 3){cp.setS4(kogus +aa);}
             //if (counter == 4){cp.setS5(kogus +aa);}
             if (counter == 2){cp.setS3(kogus);}
             if (counter == 3){cp.setS4("");}
             if (counter == 4){cp.setS5("");}

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

    private Connection getNewConnection2() throws SQLException{
        return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsn.user"),
                getParent().getProps().getProperty("dsn.pass"));
      }
    
    
    public String getPilotDayData(Agent agent){
    	
    	
    	System.out.println("Hakkasin tegema");
    	
    	 String sql = "SELECT RTRIM(SL01002) AS nimi, OR01001 AS number,  sum(OR03009) AS summa , count(OR03001) as ridu FROM  OR01"+area+"00 LEFT JOIN OR03"+area+"00 on OR01001 = OR03001  " +  
         " LEFT JOIN SL01"+area+"00 l ON SL01001=OR01004 WHERE OR01016>GETDATE()-1 AND right(OR01019,1) =  ? " +
         " GROUP BY OR01001,SL01002";

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
    sb.append(sum + "\t");
    String ridu = getNF().format(Double.parseDouble(rs.getString("ridu").trim())) ;
    sb.append("[" + ridu + "]\n" + sep);
  }
 } catch (Exception e) {
  getParent().showError(e);
 }

 connectionDown();


    	
    	
    	
         sql = " SELECT  pl.Employee AS agent ,  " +
     " Sum(case when SC01037='08' and (SC01002 like '%Leek%' or SC01002 like '%Vermont%' or SC01002 like '%Saint%')   then  ST03020 else 0 end)  AS kohalik ,  " +
 " Sum(case when SC01037='08' and (SC01002  not like '%Leek%' and SC01002  not like '%Vermont%' and  SC01002 not like '%Saint%')   then  ST03020 else 0 end)  AS global  " +
 "  ,sum(ST03021) AS summa   FROM ST03"+area+"00 INNER JOIN  SC01"+area+"00 pr on ST03017=SC01001  inner join PlanRegion pl ON pl.Code=ST03007  " + 
				  "   WHERE  MONTH(ST03015)=  MONTH(GETDATE())-1 AND YEAR(ST03015)=YEAR(GETDATE()) AND ST03016>0 AND ST03020>0  " +
                       "   GROUP BY pl.Employee ORDER BY pl.Employee ASC  ";

  this.connectionUp();
try {
 stmt = con.prepareStatement(sql);
 //stmt.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");
 ResultSet	      rs = stmt.executeQuery();
 String header =  "Agent  Kohal  Glob  Summa" + sep + "-------------------------------------------" + sep;
 boolean heade = true;
 while(rs.next()){
 	if (heade){
 		sb.append(header);
 		heade = false;
 	}
 	
     String nimi = rs.getString("agent");
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append(nimi + " ");
    sb.append(getNF().format(Double.parseDouble(rs.getString("kohalik").trim())/5000) + "\t");
    sb.append(getNF().format(Double.parseDouble(rs.getString("global").trim())/5000) + "\t");
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
    /*  connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST02"+area+"00  " +
        " LEFT JOIN OR20"+area+"00 ON  OR20001=ST02009 LEFT JOIN SL14"+area+"00 k ON ST02001=SL14001  " +
        " LEFT JOIN SL01"+area+"00 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
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

        connectionDown();*/
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
         String cmd = "SELECT Employee AS agent,SUM(OR20024) AS sales FROM OR20"+area+"00 inner join PlanRegion pl " +
         		"ON pl.Code=OR20019 WHERE MONTH(OR20015)= " +
                   "   MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY Employee ";
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
                   "SC01"+area+"00 ON ST02017=SC01001 WHERE ST02007=?" +
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
                    " FROM SL01"+area+"00 LEFT JOIN SY24"+area+"00 ON SY24002=SL01035 ORDER BY " +
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

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR20"+area+"00 " +
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
    public static final String DRIVER_VERSION = "ScalaToba";
    private String             url = "jdbc:odbc:scalamobile";
    private String             url2 = "jdbc:odbc:scalamobile";
    private String             url3 = "jdbc:odbc:CROR7";

    private Connection	       con = null;
    private Connection	       con2 = null;
    private PreparedStatement  stmt = null;
    private String area = "01";
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private  Vector prlines = new Vector();
	public void PictureMaker() {
		  makePictureCat();
		  System.exit(0);
   	
	}
	private void makePictureCat(){
	     
	   	String cmd = " SELECT  RTRIM(SC01001) AS toode " +
              "  FROM SC01"+area+"00   " +
              "  WHERE   LEN(LTRIM(SC01037))>0 and rtrim(ltrim(SC01016)) != 'S' and (SC01015 != '' or SC01018 != '') ";
	    
	   	
	     	String picts = parent.getProps().getProperty("pict.dir");
	 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	 	 

	   	try {
            connectionUp();
		    Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd);


	        StringBuffer buf = new StringBuffer();
	        StringBuffer buf0 = new StringBuffer();
	        int a = 0;
	        int b = 0;
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        Vector cats = new Vector();
	        File d  = new File(picts);
	        File[] dd = d.listFiles();
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(t.isDirectory()){
	        		cats.add(t.getName());
	        	}
	        }
	        
	        System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
	   	    while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	    	
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		  System.out.println("Piltide leidmine "  + a);	
	   	    		  
	   	    	}
	   	     
	              
	                 File f = new File(picts + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                 } else if(cats.size() > 0 ){
	                	 for(int w = 0 ;w < cats.size();w++){
	                		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
	                		
	                		if(f.exists()){
	                			System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
	                		     len =	f.length() + "";	
	                		}                     
	                	 }
	                 }
	                 
	                 if(len.equals("0")){
	                 	//System.out.println(toode+".jpg;"  + len);
	                 	buf0.append(toode+".jpg\n");
	                 }else{
	                 	buf.append(toode+".jpg;"  + len + "\n");
	           	    	b++;
	                 }
	                 }
	   	    rs.close();
	   	    stmt.close();
	   	    con.close();
	   	
	   	    //oleasoleva kataloogi kustutamine
	   	  System.out.println("Kataloogi kustutamine "  + a);	    
	               File f = new File("list.txt");
	               if (f.exists())f.delete();
	               
	                 try {
	           		PrintWriter os = new PrintWriter(new FileWriter(
	           				  "list.txt", true));
	           		os.write(buf.toString());
	           		os.close();
	           	
	           	  }catch (Exception ex) {
	           		ex.printStackTrace();
	           	  }
	           	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
	           	  File f0 = new File("list0.txt");
	           	
	           	  	
	                    if (f0.exists())f0.delete();
	                 
	                   try {
	             		PrintWriter os1 = new PrintWriter(new FileWriter(
	             				  "list0.txt", true));
	             		os1.write(buf0.toString());
	             		os1.close();
	             		//System.out.println( buf.toString());
	             	  }catch (Exception ex) {
	             		ex.printStackTrace();
	             	  }

	   //Kataloogi sisude kustutamine
	             	  
	  
	          	 
	          	 
	        	 File res2 = new File (pictspda);
	            String[] ff2 = res2.list();
	        	 for(int i = 0;ff2.length>i;i++){
	            //	System.out.println("fff " + ff2[i]);
	            	(new File ( pictspda + ff2[i])).delete(); 
	         
	            }
//	        	 L�ppes kustutamine 
	        	 
	          System.out.println("Piltide kopeerimine ");	
	          //Failide kopeerimine  
	            String[] gg = new File(picts).list();
	         	  //System.out.println("gg " + gg[0] + " " + gg.length);
	            
	             for(int i = 0;gg.length>i;i++){          	
	             	File in = new File ( picts + gg[i]); 
	             	if(in.isFile()){            	
	             	File inpda = new File ( pictspda + (gg[i]).toLowerCase()); 
	                   	copyFile(in,inpda);
	             	}
	             }
	             
	             //piltide kopeerimine alam kataloogist ..
	             
	             if(cats.size() >0){
	            	 for(int h = 0;h < cats.size(); h++){
	            		
	            	   String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
	               	 
	                  
	                   for(int i = 0;i < ff.length  ;i++){          	
	                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
	                   	if(in.isFile()){   
	                   		
	                   	File inpda = new File ( pictspda + (ff[i]).toLowerCase()); 
	                         	copyFile(in,inpda);
	                   	 }
	                   }
	                   
	            	 }
	             }
	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  copyFile(f,fff);  	
	       	  File fff0 =  new File (pictspda +  "list0.txt");
	       	  copyFile(f0,fff0);  	
	   	} catch (Exception e) {
	   	    getParent().showError(e);
	   	}
	   
	}

	
	   
	 public void copyFile(File in, File out) throws Exception {
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
	@Override
	public void renewRoutingCals() {
		Converter.sendRouteCalsDelete(user, pass, company);
		this.getCustomers(null);
	}

	

}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

