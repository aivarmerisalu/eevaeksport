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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class SkaiteCCDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter{


    public SkaiteCCDBDriver(EksportPanel parent) {
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
    
    public static String getYYYYpMMpDDpString(Date dd){
	    StringBuffer sb = new StringBuffer();
	    Calendar cal= Calendar.getInstance();
	    cal.setTime(dd);
	    
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}
		sb.append(day );
		
		sb.append("." + month  + ".");
		sb.append("" + cal.get(Calendar.YEAR));
		
	    return sb.toString();
	  }

    
	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
	private String getDirOut() {
		String dir = getParent().getProps().getProperty("writeout");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
	
	 public TreeMap getDegu() {
			TreeMap result = new TreeMap();
			File f = new File(this.getDir() + "CC.xls");
			try {
				FileInputStream ff = new FileInputStream(f);
				HSSFWorkbook workbook = new HSSFWorkbook((ff));
				HSSFSheet sheet = workbook.getSheet("Bendras");
				HSSFRow row = null;

				// System.out.println(groups.toString());

				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String custID = "";
					try{
					if (row.getCell((short) 0) != null) {

						try {
							custID = (int) (row.getCell((short) 0).getNumericCellValue()) + "";
						} catch (Exception e) {
							custID = (row.getCell((short) 0).getStringCellValue());
						}
						
	                       if(!custID.equals("0")){
	                    	Customer cust = new Customer(custID);
						
	                    		try {
						   String agent = (row.getCell((short) 5).getStringCellValue());
						   cust.setCity(agent);
						 
									
	                    		} catch (Exception e) {
	                    		e.printStackTrace();
	    						}
	                    		String callday = "";
	                    		try {
	     						   callday = (row.getCell((short) 7).getStringCellValue());
	     						   if(callday.length() <  4){
	     							 callday = makeCall(callday.trim() ,"");   
	     						   }
	     									
	     	                    		} catch (Exception e) {
	     	                    			callday = (int) (row.getCell((short) 7).getNumericCellValue()) + "";
	     	                    			 if(callday.length() < 4){
	     		     							  callday = makeCall(callday.trim(),"");   
	     		     						   }
	     	    						}
	                    		
	                    		String mainday = "";
	                    		int mainseq = 0;
	                    		String callday1 = "";
	                    		try {
	     						   callday1 = (row.getCell((short) 8).getStringCellValue());
	     						   if(callday1.length() <  4 && callday1.length() != 0){
	     							   mainday = makeCall(callday1.trim() ,"");
	     							   mainseq = 4;  
	     						   }
	     									
	     	                    		} catch (Exception e) {
	     	                    			callday1 = (int) (row.getCell((short) 8).getNumericCellValue()) + "";
	     	                    			 if(callday1.length() < 4 && callday1.length() != 0){
	     	                    				   mainday = makeCall(callday1.trim() ,"");
	     		     							   mainseq = 4;    
	     		     						   }
	     	    						}
	     	                    		String callday2 = "";
	     	                    		
	     	                    		try {
	     	     						   callday2 = (row.getCell((short) 9).getStringCellValue());
	     	     						   if(callday2.length() <  4 && callday2.length() != 0){
	     	     							   if(mainday.length() == 0){
	     	     								 mainday = makeCall(callday2.trim() ,"");
		    	     						    }
	     	     							   mainseq = mainseq + 8;   
	     	     						   }
	     	     									
	     	     	                    		} catch (Exception e) {
	     	     	                    			callday2 = (int) (row.getCell((short) 9).getNumericCellValue()) + "";
	     	     	                    			 if(callday2.length() < 4 && callday2.length() != 0){
	     	     	                    				 if(mainday.length() == 0){
	     		     	     								 mainday = makeCall(callday2.trim() ,"");
	     			    	     						    }
	     		    	     							   mainseq = mainseq + 8; 
	     		    	     							   }
	     	     	    						}
	     	                    		
	     	                    		
	     	     	                    		
	     	     	                    		String callday3 = "";
	    	     	                    		
	    	     	                    		try {
	    	     	     						   callday3 = (row.getCell((short) 10).getStringCellValue());
	    	     	     						   if(callday3.length() <  4 && callday3.length() != 0){
	    	     	     							 if(mainday.length() == 0){
	    	     	     								 mainday = makeCall(callday3.trim() ,"");
	    		    	     						    }
	  	    	     							   mainseq = mainseq + 16;    
	    	     	     						   }
	    	     	     									
	    	     	     	                    		} catch (Exception e) {
	    	     	     	                    			callday3 = (int) (row.getCell((short) 10).getNumericCellValue()) + "";
	    	     	     	                    			 if(callday3.length() < 4 && callday3.length() != 0){
	    	     	     	                    				 if(mainday.length() == 0){
	    	     		     	     								 mainday = makeCall(callday3.trim() ,"");
	    	     			    	     						    }
	    	     	     	                    				 mainseq = mainseq + 16;    
	    	  	  	    	     							   }
	    	     	     	    						}
	     	     	                    		
	    	     	     	                    		String callday4 = "";
	    	    	     	                    		
	    	    	     	                    		try {
			    	    	     	     						   callday4 = (row.getCell((short) 11).getStringCellValue());
			    	    	     	     						   if(callday4.length() <  4 && callday4.length() != 0){
			    	    	     	     							 if(mainday.length() == 0){
			    	    	     	     								 mainday = makeCall(callday4.trim() ,"");
			    	    		    	     						    }
			    	    	     	     							 mainseq = mainseq + 32;     }
			    	    	     	     									
	    	    	     	     	                    		} catch (Exception e) {
	    	    	     	     	                    			callday4 = (int) (row.getCell((short) 11).getNumericCellValue()) + "";
	    	    	     	     	                    			 if(callday4.length() < 4 && callday4.length() != 0){
	    	    	     	     	                    				 if(mainday.length() == 0){
	    	    	     		     	     								 mainday = makeCall(callday4.trim() ,"");
	    	    	     			    	     						    }
	    	    	     	     	                    				 mainseq = mainseq + 32;       }
	    	    	     	     	    						}
	    	    	     	     	                    		
	    	    	     	     	                    		
	    	    	     	     	                    		
	    	    	     	     	           	 String calld = "";
	    	    	     	     	                    	 if(mainseq == 20){
	    	    	     	     	                    		 mainseq = 2;
	    	    	     	     	                    	 }
	    	    	     	     	                    	 if(mainseq == 40){
	    	    	     	     	                    		 mainseq = 1;
	    	    	     	     	                    	 }	 
	    	    	     	     	               if(mainday.length()!= 1){
	    	    	     	     	                	  for(int o = 0;o < mainday.length(); o++){
	    	    	     	     	                		  calld = calld +  mainday + "" + mainseq;
	    	    	     	     	                	  }
	    	    	     	     	                		  
	    	    	     	     	                  }else {
	    	    	     	     	                	 calld =  mainday + "" + mainseq;
	    	    	     	     	                  }
	     	                    		
	     	                    		 if(callday.trim().length() != 0){
	     	                    			 calld = callday;
	     	                    		 }
	     	                    		 
	     	                    		 if(calld.length() != 0){
	     	                    			 cust.setCallDay(calld);
	     	                    			 System.out.println(cust.getID() + " z" + mainday + "z" +mainseq +   "y " + calld);
	     	                    		 }
	     	                    		 
	     	                    		 String name = " ";
		     	                    		try {
		     	                    			name = (row.getCell((short) 15).getStringCellValue());
		     	     						  if(name.trim().length() != 0){
		     	     							  cust.setPersonalData(name);
		     	     						  }
		     	     									
		     	     	                    		} catch (Exception e) {
		     	     	                    			try{

			     	     	                    			name = (int) (row.getCell((short) 15).getNumericCellValue()) + "";
			     	     	                    			if(name.trim().length() != 0){
			     		     	     							  cust.setPersonalData(name);
			     		     	     						  }	
		     	     	                    			}catch(Exception r){
		     	     	                    				
		     	     	                    			}
		     	     	    						}
	     	                    		 
	     	                    		 String phone = " ";
	     	                    		try {
	     	     						  phone = (row.getCell((short) 13).getStringCellValue());
	     	     						  if(phone.trim().length() != 0){
	     	     							  cust.setPersonalData((cust.getPersonalData() + " " + phone).trim());
	     	     						  }
	     	     									
	     	     	                    		} catch (Exception e) {
	     	     	                    			phone = (int) (row.getCell((short) 13).getNumericCellValue()) + "";
	     	     	                    			if(phone.trim().length() != 0){
	     	     	                    				 cust.setPersonalData((cust.getPersonalData() + " " + phone).trim());
	     		     	     						  }
	     	     	    						}
	     	     	                    		
	     	     	        	//System.out.println(cust.getID() + " xx " + cust.getCallDay());
	     	     	         result.put(custID, cust);
					}
	                   
					}
					
					
					}catch(Exception eee){
						eee.printStackTrace();
					}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}

		//	 System.exit(0);

			return result;

		}

private String makeCall(String day ,String add){
	  String res = " ";
	 // System.out.println("dd" + day);
	  if(day.length()==1){
		  if(day.equals("1")){
			  res = "E" + add;
		  }
		  if(day.equals("2")){
			  res = "T" + add;
		  }
		  if(day.equals("3")){
			  res = "K"+ add;
		  }
		  if(day.equals("4")){
			  res = "N"+ add;
		  }
		  if(day.equals("5")){
			  res = "R"+ add;
		  }
		  if(day.equals("6")){
			  res = "L"+ add;
		  }
		  if(day.equals("7")){
			  res = "P"+ add;
		  }
	  }else{
		for(int i = 0; i <  day.length();i++){
			if(isNumeric(day.charAt(i))){
				  if((day.charAt(i)+"").equals("1")){
					  res = res + "E" + add;
				  }
				  if((day.charAt(i)+"").equals("2")){
					  res =res + "T" + add;
				  }
				  if((day.charAt(i)+"").equals("3")){
					  res =res + "K" + add;
				  }
				  if((day.charAt(i)+"").equals("4")){
					  res =res + "N" + add;
				  }
				  if((day.charAt(i)+"").equals("5")){
					  res =res + "R" + add;
				  }
				  if((day.charAt(i)+"").equals("6")){
					  res =res + "L" + add;
				  }
				  if((day.charAt(i)+"").equals("7")){
					  res =res + "P" + add;
				  }
			}
		}
	  }
	    
	  
	  return res ;
}

  
  public static boolean isNumeric(String s){
      for (int i = 0 ; i < s.length(); i++){
        if (!isNumeric(s.charAt(i))){
          return false;
        }
      }
      return true;
    }

    public static boolean isNumeric(char ch)
    {
        return ch == '-' || ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '.' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
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

            public String getDealerCustomerCSV(String agent){
             return null;
             }


        public String getSaveStockInfo(){
        	  connectionUp(url);
        	  String  cmd = "  SELECT  productcode AS toode ,w.unitsinstockfree AS stock, w.warehouseasid AS ladu  " +  
	           " FROM asVwWareHouse w INNER JOIN   asVwProduct t ON w.productasid=t.productasid " +
               " WHERE unitsinstockfree !=0  ORDER BY warehouseasid" ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           String dealer = rs.getString("LADU").trim();
		           if(seis > 0 && dealer.equals("EQ")){
		               products.append(kood +  ";");
		               products.append(dealer + ";");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		           st.close();
		           con.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
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

                try {
                	String vana = " SELECT  PriceListAsId AS grupp,t.ProductCode AS toode FROM asVwPriceListProducts r  " +
            	  		" INNER JOIN asVwProduct t ON t.ProductAsId=r.ProductAsId WHERE LevelNo is not null ORDER BY ProductName";
            
                	String cmd = "SELECT  PriceListasId AS grupp, ProductCode AS toode FROM vMRS_PriceListsProducts  "+
            	" ORDER BY PriceListasId, ProductCode";
                	
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
                              }
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
    
    private Connection connectionUp1(){
        
        Connection con2 = null;
		try {
			con2 = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
			     getParent().getProps().getProperty("dsn.pass"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    return con2;

    }

    private Connection connectionUp2(){
      
          Connection con2 = null;
		try {
			con2 = DriverManager.getConnection(url3, getParent().getProps().getProperty("dsn.user"),
			     getParent().getProps().getProperty("dsn.pass"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return con2;

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
        String    sql = " SELECT prodgroupcode AS liik ,groupname AS nimetus ,(select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId= " +
                     " gr.ProdGroupAsId ) as parent from dbo.asVwProdGroup g where ProdGroupParentAsId is null " +
         " ORDER BY prodgroupcode";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
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
        String    sql = "  SELECT g.prodgroupcode AS liik ,g.groupname AS nimetus , " +
			" (select gr.groupname from  asVwProdGroup gr where g.ProdGroupParentAsId= gr.ProdGroupAsId ) " +
			" as parent,ProdGroupParentAsId as id from dbo.asVwProdGroup g " +
			" where g.ProdGroupParentAsId>0  ORDER BY g.prodgroupcode";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
          String nimi = rs.getString("nimetus").trim() +" " + kood;
          if(kood.length()>0 ){
          	String nimeosa = rs.getString("parent");
          	if(nimeosa != null && nimeosa.length()>3){
          		nimi = nimeosa.substring(0,3) + " " + nimi;	
          	}
          	tm.put(kood,nimi);
          }
          
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }

    public TreeMap getAgents() {
    
        connectionUp(url);
    	TreeMap agents = new TreeMap();
        try {  
        TreeMap gr = new TreeMap();
        String sss = "select pricelistasid as id,prodgroupcode as gr from " +
		" dbo.asVwPriceListProducts pr INNER JOIN asVwProduct t ON pr.productasid=t.productasid  " +
		" INNER JOIN asVwProdGroup gr ON gr.prodgroupasid=t.prodgroupasid " +
		" GROUP BY pricelistasid,gr.prodgroupcode " +
		" ORDER BY pricelistasid,gr.prodgroupcode";
      
        

        String uus  = "SELECT   ProductCode AS gr , PriceListasId AS id FROM vMRS_PriceListsProducts  "+
     	" ORDER BY PriceListasId, ProductCode";
       
       PreparedStatement stmt2 = con.prepareStatement(uus);
	    ResultSet	      rs2 = stmt2.executeQuery();
	    while (rs2.next()) {
	    	String gru = rs2.getString("gr").trim();
	    	String id = rs2.getString("id").trim();
	    	
	    	Vector vec = (Vector)gr.get(id);
	    	if(vec == null){
	    		vec = new Vector();
	    	}
	    	vec.add(gru);
	    	gr.put(id,vec);
	    	
	    }
	    rs2.close();
	    stmt2.close();

        
        
        String  cmd = "SELECT employeeasid ,employeecode AS yksus,empname AS nimi,warehouseno AS ladu , " +
        " pricelistasid AS hinnakiri FROM asVwEmployee WHERE warehouseno ='EQ' order by empname";



            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi + " " + agent.getID());
              
              agent.setObject(parent.make(rs.getString("ladu")));
              String hk = parent.make(rs.getString("hinnakiri"));
              Vector ss = (Vector)gr.get(hk);
              if(ss != null){
              	for(int i=0;i<ss.size();i++){
              		String gg = (String)ss.get(i);
              		agent.addProdGroupList(gg);
              		if(!grouplist.contains(gg)){
                  		grouplist.addElement(gg);	
              		}
              	}
              }
              agent.setIsPilot(true);
              agents.put(nimi, agent);
	    }
	    rs.close();
	    stmt.close();
	    con.close();
	    Agent aa = new Agent ("HOUSE");
	    aa.setName("Office");
	    agents.put("Office", aa);
	 

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         String    sql = " select  part.Regnr,pr.Kodas as toode , '' as grupp,pre.NuolaidosProc from NuolaiduSutartys lep " + 
" INNER JOIN NuolaiduAprasymai abi ON abi.NuolaiduAprasymaiId = lep.NuolaiduAprasymaiId " +
" INNER JOIN NuolaiduInfoPrekes pre ON pre.NuolaiduAprasymaiId = lep.NuolaiduAprasymaiId " +
" INNER JOIN Prekes pr ON pr.PrekesId = pre.PrekesId  " +
" INNER JOIN Partneriai part ON lep.PartneriaiId = part.PartneriaiId " +
" where  pr.Kodas not like'%*%' and " +
" getdate() between " +
" DATEADD( dd, lep.DataNuo_Int, CONVERT( DateTime, '1900.01.01') ) and " +
" DATEADD( dd, lep.DataIki_Int, CONVERT( DateTime, '1900.01.01') )and " +
"  part.Kodas != '' " +
" union all " +
" select  part.Regnr,''as toode,pr.Kodas as grupp,pre.NuolaidosProc from NuolaiduSutartys lep " + 
" INNER JOIN NuolaiduAprasymai abi ON abi.NuolaiduAprasymaiId = lep.NuolaiduAprasymaiId " +
" INNER JOIN NuolaiduInfoPrekiuGrupes pre ON pre.NuolaiduAprasymaiId = lep.NuolaiduAprasymaiId " +
" INNER JOIN PrekiuGrupes pr ON pr.PrekiuGrupesId = pre.PrekiuGrupesId " +
" INNER JOIN Partneriai part ON lep.PartneriaiId = part.PartneriaiId " +
" where getdate() between " +
" DATEADD( dd, lep.DataNuo_Int, CONVERT( DateTime, '1900.01.01') ) and " +
" DATEADD( dd, lep.DataIki_Int, CONVERT( DateTime, '1900.01.01') ) and  " +
" part.Kodas != '' order by part.Regnr ";
         
           Statement st = null;
           try {
             connectionUp(url);
             st = con.createStatement();
             ResultSet rs = st.executeQuery(sql);

             while (rs.next()) {

               String custID = rs.getString("Regnr").trim();
               //String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("toode").trim();
               String prodGroup = rs.getString("grupp");
              
               String discount = rs.getString("NuolaidosProc");
               
			   
              // double price = rs.getDouble("price");
               MatrixLine mline = new MatrixLine();

               if (custID != null && custID.trim().length() > 0) {
                 mline.setCustID(custID.trim());
               }
               //if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                // mline.setCliGroup(clGroup.trim());
               //}

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

               //if (Double.parseDouble(discount) == 0 && price != 0) {
                // mline.setPrice(price);
              // }
                 
              lines.add(mline);
             }
             rs.close();
             st.close();
             connectionDown();
           }
           catch (SQLException ex) {
             ex.printStackTrace();   
           }
         return lines;
    
       }

    private TreeMap<String, String> loadMinsums() {
  		TreeMap<String, String> res = new TreeMap<String, String>();
  		
  		try {
  			FileReader reader = new FileReader("minsums.txt");
  			BufferedReader br = new BufferedReader(reader);
  			String line;
  		
  			while ( (line = br.readLine()) != null) {
  				StringTokenizer st = new StringTokenizer(line, ";");
  				if (st.hasMoreTokens()) {
  					String modeID = st.nextToken();
  					if (st.hasMoreTokens()) {
  						String modeName = st.nextToken();
  							res.put(modeID, modeName);
  					}
  				}
  			}
  			br.close();
  			reader.close();
  		}
  		catch (Exception e) {
  		}
  		
  		return res;
  	}
    

    
    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        TreeMap ccd = getDegu();
        String sep = ";";

        TreeMap minsums = loadMinsums();
        
        String cmd = " SELECT customername + address AS nimi,customercode AS kood,address + ' ' + rtrim(case when city is null then '' else city end) AS aadress, " +
       " paymenttime AS term,'' AS ale , regno as payer, " +
        "   ' ' AS visit, custphone AS info ,employeecode as agent , employeename as agentname, kl.PriceListAsId as grupp," +
        " KliendiSystem, CallCenter , (case when CategoryAsID  in ('10','03','05','11','12','20','21') then 100 else 150 end) as minsum "
        + "  , CategoryAsID as msid FROM E_Partner.asVwMJCustomer kl   " +
        "   INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid  " +
        "   LEFT JOIN PL_Partneriai pl ON part.PartneriaiId = pl.PartneriaiId" +
        "   LEFT JOIN IvNormatyvuReiksmes ivnr ON pl.TIPAS6ID = ivnr.IvNormatyvuReiksmesId" +
        "  where ivnr.Pavad !='NENAUDOJAMAS' ORDER BY kl.customercode asc";
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        StringBuffer sb2 = new StringBuffer();

	try {
            stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	    ResultSet	      rs = stmt.executeQuery();

	    String minam = getParent().getProps().getProperty("minsumma");
	    double minsum = 0;
	    if(minam != null){
	    	try{
	    		minsum = Double.parseDouble(minam);
	    	 }catch(Exception r){
	    		
	    	 }
	      }
	    
	    while (rs.next()) {
		String nimi = rs.getString("nimi").replaceAll("\""," ").trim();
	
                Customer cust = new Customer(rs.getString("kood").trim());
                if(minsum > 0){
                	cust.setMinsum(minsum);
                }       
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                
                cust.setName(nimi + " " + cust.getID());
                cust.setAddress(rs.getString("aadress").trim());
                
               /* String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }*/
                boolean gok = true;
                String term = rs.getString("term");
                cust.setPaymTerm(parent.make(term));
                
                
                 String payer = rs.getString("payer");
                 if(parent.make(payer).length() > 1){
                     cust.setPayer(parent.make(payer));
                 }
                 
                String info = rs.getString("info");
                if(parent.make(info).length() > 1 && gok){
                    cust.setPersonalData("Tel. " + parent.make(info).replaceAll("\n", ""));
                }
                
                
                if(ccd.get(cust.getID())!= null ){
                	Customer ccc = (Customer) ccd.get(cust.getID());
                	if(ccc.getCity() != null && ccc.getCity().length() != 0){
                		cust.setCity("AGENTAS: " +ccc.getCity().trim());// lisame vastutava agendi 
                	}
                	if(ccc.getCallDay() != null && ccc.getCallDay().length() != 0){
                		cust.setVisit(ccc.getCallDay());// lisame kylastus p�eva. 
                	}
                	if(ccc.getPersonalData() != null && ccc.getPersonalData().length() != 0){
                		cust.setPersonalData(ccc.getPersonalData().replaceAll("\n", ""));// lisame kontakti ja tel numbri. 
                	gok = false;
                	}
                	
                }
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                  sb2.append(cust.getID() + sep + agen +sep);
                 
                  String agentName = rs.getString("agentname");
                  if (agentName != null) agentName = agentName.trim();
                  if (agentName == null) agentName = " ";
                  sb2.append(agen + " " +agentName + "\n");
                  
                  
                  if(cust.getCity().indexOf("AGENTAS: ") == -1){
                	  cust.setCity("AGENTAS: " + agen.trim());
                  }
                  
                }
               
                String group = rs.getString("grupp");
                if(group != null){
                  cust.setGroup(group.trim());
                }
                

                String kliendisystem = rs.getString("KliendiSystem");
                if(kliendisystem != null && kliendisystem.trim().length() != 0){
                  cust.setAddress("<" +kliendisystem.trim() +  "> " + cust.getAddress());
                }
                 int cc = rs.getInt("CallCenter");
                
                  minsum = rs.getDouble("minsum");
                  
                  String category = rs.getString("msid");
                  if(category != null && category.trim().length() >0 ){
                  	category = category.trim();
                  	if(minsums != null && minsums.get(category) != null){
                  		try{
                  			minsum = Double.parseDouble(minsums.get(category)+"");	
                          			
                  		}catch(Exception e){
                  		e.printStackTrace();	
                  		}
                  	}
                  }
                  cust.setMinsum(minsum);
                 
                if( cust.getName().indexOf("**") == -1 && cust.getAgents() != null && cust.getAgents().size() !=0
                		 && cust.getID().startsWith("240")){
                	if(cc > 1){
                    	cust.addAgent(new Agent("F143"));
                    	cust.addAgent(new Agent("F153"));
                    	cust.addAgent(new Agent("F041"));
                    	cust.addAgent(new Agent("S563"));
                    	cust.addAgent(new Agent("F180"));
                    	cust.addAgent(new Agent("F102"));
                	}
                	
                	  cust.addAgent(new Agent("HOUSE"));
                	  customers.put(cust.getName(), cust);
                      custMap.put(cust.getID(), cust);	
                } 
                
	    }
            rs.close();
            stmt.close();

    Customer custo = new Customer("newcust");
	    custo.setName("Nauj� Klient�");
	    custo.addAgent(new Agent("F146"));
	    custo.addAgent(new Agent("F153"));
	    custo.addAgent(new Agent("S563"));
		custo.addAgent(new Agent("HOUSE"));
	    customers.put(custo.getName(), custo);
        custMap.put(custo.getID(), custo);	
    
        

            String sql = " select customercode AS klient,employeecode as agent,employeename as agentname from  " +
              " asVwDepCustomers si where employeecode not like 'zz%' ";

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = rs.getString("klient").trim();
               Agent aa = new Agent(rs.getString("agent").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.addAgent(aa);
                sb2.append(cust.getID() + sep + aa.getID() +sep);
                
                String agentName = rs.getString("agentname");
                if (agentName != null) agentName = agentName.trim();
                if (agentName == null) agentName = " ";
                sb2.append(aa.getID() + " " + agentName + "\n");
                
                if(cust.getCity().indexOf(aa.getID()) == -1){
                	 cust.setCity((cust.getCity() + " " + aa.getID()).trim());
                }
               
                if(cust.getCity().indexOf("AGENTAS: ") == -1){
              	  cust.setCity("AGENTAS: " + cust.getCity());
                }
               
               
              }
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
            
            
            
            
            sql = "select  payercode AS klient,sum(billamount-paidamount) AS summayle   from   " +
            		" asVwOrder  d 	where billissuedate+paymenttime < getdate() and billamount-paidamount>0 " + 
                      "   group by payercode ";


        
            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
            	 String id = rs.getString("klient").trim();
  	           double debt =rs.getDouble("summayle");
  	          
  	           Iterator iter = custMap.values().iterator();
  	           while(iter.hasNext()){
  	          Customer cust = (Customer)iter.next();   	
  	           	if(cust.getPayer().equals(id)){
  	                cust.setDebt(cust.getDebt()+ debt);
  	           	}
  	           }
            }
            

            rs.close();
            stmt.close();
        
         
            
           sql = "  select  payercode AS klient,sum(billamount-paidamount) AS summayle   from  " + 
    	   "	asVwOrder  d 	where billissuedate+paymenttime +7 < getdate() and billamount-paidamount>0 " + 
            "         group by payercode "; 
       stmt = con.prepareStatement(sql);

       rs = stmt.executeQuery();
       while(rs.next()){
       	 String id = rs.getString("klient").trim();
	           
	          
	           Iterator iter = custMap.values().iterator();
	           while(iter.hasNext()){
	          Customer cust = (Customer)iter.next();   	
	           	if(cust.getPayer().equals(id)){
	                cust.setSalesBlock(true);
	           	}
	           }
       }
       

       rs.close();
       stmt.close();
 
           
           
            sql = " select  payercode AS klient,sum(billamount-paidamount) AS summa   from  asVwOrder  d  "+ 
            	"	where billamount-paidamount>0  group by payercode ";
            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
            	 String id = rs.getString("klient").trim();
    	           double debt =rs.getDouble("summa");
    	          
    	           Iterator iter = custMap.values().iterator();
    	           while(iter.hasNext()){
    	          Customer cust = (Customer)iter.next();   	
    	           	if(cust.getPayer().equals(id)){
    	                cust.setUsedCredit(debt);
    	           	}
    	           }
            }
            

            rs.close();
            stmt.close();
            con.close();
	} catch (Exception e) {
            getParent().showError(e);
        }
     System.out.println("Kliendid tehtud");
	return customers;
    }

    public DealerProduct getDealerProductsList() {


        DealerProduct dealerProds = new DealerProduct();
        try {


          String  sql = "  SELECT  productcode AS toode ,w.unitsinstockfree AS stock, w.warehouseasid AS ladu  " +  
		           " FROM asVwWareHouse w INNER JOIN   asVwProduct t ON w.productasid=t.productasid " +
                   " WHERE unitsinstockfree !=0  ORDER BY warehouseasid" ;

             if (isDebug()) System.out.println(sql);
             connectionUp(url);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                    
                    if(seis > 0 && ladu.equals("EQ")){
                    	   dealerProds.add(ladu, toode,seis+""," "," " );
                    }
                 
                    //System.out.println(ladu+" "+toode+" "+seis+"");
                }
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
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "select  payercode AS klient, billcode AS number,billissuedate+paymenttime AS kuupaev, " + 
			" billamount-paidamount AS summa  , datediff(day,billissuedate+paymenttime ,getdate() )as yle from " + 
		  " 	asVwOrder  d  " +  
		"	where billissuedate+paymenttime < getdate() +6 and billamount-paidamount>0 and  " +
           "       billissuedate > getdate()-150   order by payercode,billissuedate+paymenttime ";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        String dok = rs.getString("number");
        if(dok != null){
        	 dok = dok.trim();
    	        if(dok.length() >5){
    	        	dok = dok.substring(4);
    	        }
    	        debt.setDoknr(dok);   	    	        
    	        debt.setDuedate(rs.getDate("kuupaev"));
    	        debt.setSum(rs.getDouble("summa"));       
    	        debt.setDue(rs.getString("yle")); 	
    	        if(debt.getSum()>0.5 || debt.getSum()<-0.5){
    	        list.addElement(debt);	
             }
        
        
        }
        }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }
    
    
    public static final String cleanLitLatEst(String ret){
        if (ret != null){
        Converter.cleanLitLatEst(ret);
        }
        if(ret == null || ret.trim().length()==0){
            return " ";
            }else {
            return ret;
            }        
          }
    
  

    public TreeMap getProducts(Agent agent){
    	
   
        connectionUp(url);


        String cmd = " SELECT productcode AS toode,pr.Pavad AS nimi,retailprice AS myygihind ,0 AS stock, " +
                    " prodgroupcode AS grupp,productbarcode AS ean, (select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId=  " +
                    "  gr.ProdGroupAsId ) AS teineGrupp ,(select UnitsInStockFree  from asVwWareHouse w where   " +
                    " w.WareHouseAsId ='EQBR' and ProductAsId=t.ProductAsId)   AS sellout ,EQ_IPOK AS tykke,MPAL_MinKiekis as minamount, " +
                    "( case when MPAL_prekiutipaiid = 11715 then L_10000103 else 0 end) as prioriteet, " +
                    " ( case when NuolaidosProc is not null then NuolaidosProc else 0 end ) as maxdisc , kiekiomv as unit FROM asVwProduct t LEFT JOIN  " +
                    "  asVwProdGroup g ON g.prodgroupasid=t.prodgroupasid INNER JOIN Prekes pr on pr.Kodas = t.productcode " +
                   " LEFT JOIN PL_PREKES a on a.prekesid=pr.prekesid " + 
                   "LEFT JOIN NuolaiduInfoPrekes ale on ale.prekesid=pr.prekesid  and NuolaiduAprasymaiId = 221990" +
                   " where productcode not like '%*%' and  productcode not like '/%' and pr.Nenaudoti  = 0";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim().replaceAll("\"","").replaceAll("\n", " ");
		try{
			nimetus =  nimetus.substring(0,1) + nimetus.substring(1,nimetus.length()).toLowerCase();
			
		}catch(Exception e){
			
		}
		
		
		


                Product prod = new Product(kood);
              
                try{

                    prod.setPrice(parseDouble(rs.getString("myygihind").trim()));	
                }catch(Exception ee){
                	prod.setPrice(0);
                }

              //  prod.setStock(parseDouble(rs.getString("stock").trim()));
              
                

                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                String  ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                  prod.setEAN(ean.trim().replace(';', ':'));
                }
                
                
                String  gr2 = rs.getString("teineGrupp");
                if(gr2 != null && gr2.trim().length()>0){
                  prod.setGroupSecond(gr2.trim());
                }
               try{ 
	                double  sellout = rs.getDouble("sellout");
	                if(sellout > 1){
	                 // prod.setStatusSellout("1");
	                  prod.setName(prod.getName() +  " @BR"  + sellout + "@");
	                }
                
	          } catch (Exception e) {
		       getParent().showError(e);
		      }
                

              prod.setInCases(rs.getDouble("tykke"));
              double minkogus = rs.getDouble("minamount");
              
              
              if(minkogus > 0){
            	  prod.setMinAmountInUnits(minkogus);
              }
              int prior = rs.getInt("prioriteet");
              
              if(prior == 1 || prior == 2){
            	//  prod.setStatusTop("1");
              }
              if(prior != 0){
            	  prod.setType(prior + "");
              }
             
              double max = rs.getDouble("maxdisc");
              String unit = rs.getString("unit");
              boolean udd = false;
              if(unit!= null && unit.trim().length()>0){
            	  prod.setUnit(unit.trim());
            	  if(unit.toLowerCase().indexOf("kg") >-1 || unit.toLowerCase().indexOf("pak.") >-1){
            		  udd = true;
            	    }
              }
              
              String iss ="";
              boolean ddd = false;
              if(max > 0){
                  prod.setMaxDiscount(max);
                  iss = " MD" +(int) max ;
                  ddd = true;
              }
              String algus =  "   "+ kood;
              if(ddd){
            	  
            	  if(udd){
            		  algus = algus +  iss + "_KG_" + nimetus;  
            	  }else{
            		  algus = algus +  iss + "_" + nimetus;	  
            	  }
                  
              }else if (udd){
                  algus = algus + "___KG_" + nimetus;
              }else {
            	  algus = algus + "______" + nimetus;
              }
              nimetus = addSpaces(algus,70) + "_" + ean + "__" ;
          if(udd){
        	//  System.out.println("udd " + nimetus);
          }
              
              prod.setName( nimetus);
              
	          if(//(grouplist.contains(prod.getGroup()) || grouplist.contains(prod.getGroupSecond())) &&
	        		  prod.getID().indexOf("**") == -1 || prod.getName().indexOf("**") == -1){
	      		products.put(nimetus, prod);  
	          }

	    }
            rs.close();
            st.close();
            con.close();
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
       Connection con2 = connectionUp1();
	
        String sqlo = "select customercode AS klient ,productcode AS toode, quantity AS kogus ,billissuedate  AS lastdate , count(customercode) as arv  " + 
  " from asVwOrder d INNER JOIN asVwOrderDetails r ON d.DeliveredOrderAsID = r.DeliveredOrderAsID  " +
 " WHERE billissuedate > ? GROUP BY productcode,customercode,r.quantity,d.billissuedate ORDER BY customercode asc,  " +
 " count(customercode),productcode,billissuedate asc";
      
        String sql = "select customercode AS klient ,productcode AS toode,quantity AS kogus ,billissuedate  AS lastdate , count(customercode) as arv  " +
        	  " from asVwOrder d  WITH(NOLOCK) INNER JOIN asVwOrderDetails r WITH(NOLOCK) ON d.DeliveredOrderAsID = r.DeliveredOrderAsID  " +
        	" WHERE billissuedate > ? GROUP BY productcode,customercode,r.quantity,d.billissuedate ORDER BY customercode asc,  " +
        	" count(customercode),productcode,billissuedate asc";


        sql = ";WITH aaa( OperacijosId, Data ) AS ( SELECT o.OperacijosId, o.Data " +
        	      " FROM Operacijos2 o2 WITH(NOLOCK) INNER JOIN Operacijos o WITH(NOLOCK) ON o2.OperacijosId = o.OperacijosId " +
        	       "                                 INNER JOIN RysioOperacijos rop WITH(NOLOCK) ON o.OperacijosId = rop.OperacijosId " +
        	     " WHERE o.OperacijosTipoId IN ( 50, 64 )AND rop.RysiuTipaiId = 3 AND rop.OperacijosStatusasId = 778 " +
        	      "  and o2.Data_Int > DATEDIFF( d, 140, GETDATE())) " +
        	" SELECT klient = part.Kodas,   toode = p.Kodas, kogus = Convert( Int, pk.Kiekis ),  lastdate = aaa.Data " +
        	 " FROM aaa INNER JOIN PardavimuKunai pk WITH(NOLOCK) ON aaa.OperacijosId = pk.OperacijosId " +
        	  "         INNER JOIN Prekes p WITH(NOLOCK) ON pk.PrekesId = p.PrekesId " +
        	   "        INNER JOIN Pard_BendraInf pbi WITH(NOLOCK) ON aaa.OperacijosId = pbi.OperacijosId " +
        	    "       INNER JOIN Partneriai part WITH(NOLOCK) ON pbi.PartneriaiId = part.PartneriaiId " +
        	" ORDER BY part.Kodas ASC,  p.Kodas, aaa.Data ASC";
        
	if (isDebug()) {
	    System.out.println(sql);
	}

	Vector custProds = new Vector();
	
          try {
         PreparedStatement   stmt1 = con2.prepareStatement(sql);
          // stmt1.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            if (agentCode != null){
             // stmt.setString(2, agentCode);
            }
            ResultSet	      rs = stmt1.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              double kogus =  Double.parseDouble(rs.getString("kogus").trim());
              String date = rs.getString("lastdate").substring(5,10);
              String arv = "";//rs.getString("arv");
              CustProd cp = new CustProd(kliendikood, tootekood,kogus,0 ,0,
              		date + "+" + arv,0,0,0," ");
                    custProds.add(cp);
              
            }
            rs.close();
            stmt1.close();
            con2.close();
          } catch (Exception e) {
            getParent().showError(e);
            try {
            	 con2 = connectionUp1();
                 PreparedStatement   stmt1 = con2.prepareStatement(sql);
            //    stmt1.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
                 if (agentCode != null){
                  // stmt.setString(2, agentCode);
                 }
                 ResultSet	      rs = stmt1.executeQuery();

                 while (rs.next()) {
                   String kliendikood = rs.getString("klient").trim();
                   String tootekood = rs.getString("toode").trim();
                   double kogus =  Double.parseDouble(rs.getString("kogus").trim());
                   String date = rs.getString("lastdate").substring(5,10);
                   String arv = ""; rs.getString("arv");
                   CustProd cp = new CustProd(kliendikood, tootekood,kogus,0 ,0,
                   		date + "+" + arv,0,0,0," ");
                         custProds.add(cp);
                   
                 }
                 rs.close();
                 stmt1.close();
                 con2.close();

            } catch (Exception d) {
            	 getParent().showError(d);
            	 
            	 
                 try {
                 	 con2 = connectionUp1();
                      PreparedStatement   stmt1 = con2.prepareStatement(sql);
              //       stmt1.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
                      if (agentCode != null){
                       // stmt.setString(2, agentCode);
                      }
                      ResultSet	      rs = stmt1.executeQuery();

                      while (rs.next()) {
                        String kliendikood = rs.getString("klient").trim();
                        String tootekood = rs.getString("toode").trim();
                        double kogus =  Double.parseDouble(rs.getString("kogus").trim());
                        String date = rs.getString("lastdate").substring(5,10);
                        String arv = "";//rs.getString("arv");
                        CustProd cp = new CustProd(kliendikood, tootekood,kogus,0 ,0,
                        		date + "+" + arv,0,0,0," ");
                              custProds.add(cp);
                        
                      }
                      rs.close();
                      stmt1.close();
                      con2.close();

                 } catch (Exception y) {
                 	 getParent().showError(y);
                 }
            	 
            }
               
                
                      }
     
	return custProds;

   // 	Vector custProds = new Vector();
    //	return custProds;
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
         String cmd = "select  employeecode AS agent, sum(billamount) AS summa from  "  +
			" asVwOrder  d   where MONTH( billissuedate)=MONTH(GETDATE()) AND YEAR( billissuedate)=YEAR(GETDATE()) group by employeecode";
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
                      agent.setMonthSales((int)rs.getDouble("summa"));
                       }
                    }

             rs.close();
             stmt.close();
             con.close();
         } catch (Exception e) {
             getParent().showError(e);
         }

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
      
      File ff = new File ("clientagent.txt");

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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "SkaiteCC";
    
    private String             url = "jdbc:odbc:EUGESTA";
    private String             url2 = "jdbc:odbc:report";
    private String             url3 = "jdbc:odbc:ATASKAITOS";
    private Connection	       con = null;
    private Vector grouplist = new Vector();
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

