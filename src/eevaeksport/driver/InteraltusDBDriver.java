/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.Contract;
import eevaeksport.ContractLine;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.imp.Order;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class InteraltusDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker,FixLoader {

    /**Fgetcustomers
     * 
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @seefsavest
     * 
     */
    public InteraltusDBDriver(EksportPanel parent) {
        super(parent);
        this.subjectFilesTo = parent.getString("filemsg");

        //Rotimaja9
        
        // String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompanyx = getParent().getProps().getProperty("rest.companyx");
	    rcompany = getParent().getProps().getProperty("rest.company");
          }
	  
	  String user =  null;
	  String rcompany =  null;
	  String pass = null;
	  String rcompanyx = null;

	  
	  
		@Override
		public void PictureMaker() {
			
			
			this.getCustomers(null);
			this.getProducts(null);
			
			   TreeMap smas = new TreeMap();
			   String sql = "set search_path to firma1,public;\r\n" ; 
	     		  String sql2 ="select  s.toode  , sum(s.kogus) as kogus from (\r\n" + 
	      		"select * from crlaosei6())as  s where s.objekt = '3' or s.toode like 'FLi%' group by s.toode ";

					try{
						Connection con = DriverManager.getConnection(url);
						
						PreparedStatement pstm = null ;
						ResultSet rs6 = null ;
						try {
							pstm = con.prepareStatement(sql);
							 rs6 = pstm.executeQuery();
						}catch(Exception e) {
							
						}

						
						 pstm = con.prepareStatement(sql2);
						 rs6 = pstm.executeQuery();

						while(rs6.next()){
						smas.put(rs6.getString("toode").trim(),rs6.getDouble("kogus") );
						}
						rs6.close();
						pstm.close();
						con.close();
						//System.out.println("smas " + smas.size());
						
					}catch(Exception e) {
						e.printStackTrace();
					}
					
				
			
					if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY || "true".equals(parent.getProps().getProperty("flint"))){
						sendFlintReports(ppprods,smas);
					}
					
					
					
			if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.MONDAY || "true".equals(parent.getProps().getProperty("chumak"))){
				sendChumakReports(pprods,smas);
			
				String path="cmd /c start C:\\MobileJava\\MjFtpTransfer\\FtpUpload.bat";
				Runtime rn=Runtime.getRuntime();
				
				try {
					Process pr=rn.exec(path);
					InputStream is = pr.getInputStream();
				    int i = 0;
				    while( (i = is.read() ) != -1) {
				        System.out.print((char)i);
				    }
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
			
	     	sendVeresReports(pprods,smas);
			makePictureCat();
		
			System.exit(0);
			
		}
	  
		  private TreeMap eanprods = null;
		
		 @Override
			public void FixLoader() {
			  eanprods =  Converter.loadMore();
			  
			  if(eanprods == null) {
				  eanprods = new TreeMap();
			  }
				this.getProducts(null);
				TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
				System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
				if(contracts.size() > 0) {
					Converter.sendContractJson(contracts ,  rcompanyx,  user ,  pass);
				}
				
				
				System.exit(0);
			}
		
		 
		 private void loadPictures() {
			 	
	            connectionUp();
	        	String  cmd2 = "SELECT t.toode,pi.pilt ,  pi.id, pildityyp FROM firma1.toode t   INNER JOIN firma1.artpilt pi ON pi.toode=t.toode  \r\n"  ;
	        	
	        	
	        	String picts = parent.getProps().getProperty("pict.dir");
	      	  
	        	try {
	        		Converter.doTrustToCertificates();
			       	Converter.trustAllHttpsURLConnection();
	        		
	        		Statement st = con.createStatement();
	         		    ResultSet	      rs = st.executeQuery(cmd2);
	        	    while (rs.next()) {
	                      String toode = rs.getString("toode").trim();
	                      byte[] bufbyte = rs.getBytes("pilt");
	                      
	                      String id = rs.getString("id");
	                      String laiend = rs.getString("pildityyp").trim().toLowerCase();
	                      
	                      System.out.println(toode + " " + bufbyte.length);
	                      
	                      if(bufbyte != null){
	                          String filename = picts + toode+"."+laiend; // Function for generating temporary filenames basing on GUID 
	                          File fp = new File(filename);
	                          if(!fp.exists()){
	                        	 // fp.delete();
	                         
	                          
	                       	Converter.doTrustToCertificates();
	                       	Converter.trustAllHttpsURLConnection();
	                  
	                         
	                          FileUtils.copyURLToFile(
	                        		  new URL("https://erp.eetasoft.ee/interaltus/Image/ProductDetail?id=" + id), 
	                        		  new File(filename), 
	                        		  2000, 
	                        		  10000);
	                          }
	                                  
	                        }
	                      }
	        	    rs.close();
	        	    st.close();
	        	    connectionDown(); 
	        	}catch(Exception e) {
	        		e.printStackTrace();
	        	}
		 }
		
		  private void makePictureCat(){
		  	    
		    	loadPictures();
		    	String pictssou = parent.getProps().getProperty("pict.dir");
		  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
		  	   
		  	 try {
	       	  
	            File f = new File(pictspda +"list.txt");
	            if (f.exists())f.delete();
	            //Kataloogi sisude kustutamine
	        	 	 File res2 = new File (pictspda);
		             String[] ff2 = res2.list();
		         	// for(int i = 0;ff2.length>i;i++){
		             	//(new File ( pictspda + ff2[i])).delete(); 
		             //}
//		         	 L�ppes kustutamine 
		  		 
		  		 TreeMap prods = this.getProducts(null);

		         StringBuffer buf = new StringBuffer();
		         Iterator enume = prods.keySet().iterator();
		    
		         while (enume.hasNext()) {
		    	    	String asi = (String)enume.next();
		    	    	Product prod = (Product)prods.get(asi);
		    	    	String toode = prod.getID().trim();  
		               if(toode.length() > 0){
		                	
		    	   	  res2 = new File (pictssou);
		              ff2 = res2.list();
		             
		         	 for(int i = 0;ff2.length>i;i++){
		             	if( ff2[i].startsWith(toode)){
		               // System.out.println("pilt olemas " + ff2[i]);
		            	File in = new File ( pictssou + ff2[i]); 
		              	if(in.isFile()){
		              	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
		              	if(!inpda.exists()){
		                  	Converter.shrinkAndCopyFile(in,inpda,400);
	              	   
	              	     }
		              	 if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                    	}
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
	    

	  
	  
    public String getSaveDebtInfo() {
 	  
 String cmd3 = "SELECT k.maksja,d.klient AS klient, SUM(d.tasumata) AS debt  from firma1.dok d inner join firma1.klient k" +
 		" on k.kood = d.klient  WHERE  " + 
  " current_date>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND length(klient) > 0 GROUP BY d.klient,k.maksja ORDER BY d.klient";


 		TreeMap custs = new TreeMap();




 		StringBuffer products = new StringBuffer();
 		try {
        this.connectionUp();
 		Statement st = con.createStatement();
 		ResultSet rs = st.executeQuery(cmd3);

 		while (rs.next()) {
 			String maks = rs.getString("maksja");
 		String kood = rs.getString("klient");
 		double volg = rs.getDouble("debt");
 		Customer custw = null;
 		if(maks != null){
 			 custw = new Customer (maks.trim());
 			custw.setDebt(volg);
 			if(volg>0){
 				if(volg > 499){
 					custw.setSalesBlock(true);
 				}
 				
 				custs.put(maks.trim(),custw);
 			}
 		}
 		if(kood != null){
 			 custw = new Customer (kood.trim());
 			custw.setDebt(volg);
 			if(volg>0){
 				if(volg > 499){
 					custw.setSalesBlock(true);
 				}
 				
 				custs.put(kood.trim(),custw);
 			}
 		}
 	
 		
 		}

 		rs.close();
 		st.close();
 		con.close();
 		
 		} catch (Exception e) {
 		getParent().showError(e);
 		}
 		 
        this.getCustomers(null);

        
        Iterator     codes = custs.values().iterator();
 		while (codes.hasNext()){
 		Customer cust = (Customer)codes.next();
 		String bl = "0";
 		if(cust.isSalesBlock()){
 			bl = "1";
 			}
 		
 		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
 		}
        
 		return products.toString();

       }
    

	private void sendChumakReports(TreeMap prods, TreeMap smas){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -7);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		  Calendar lastWeekStart = Converter.getWeekStartDate(cal);
		  Calendar lastWeekEnd = Converter.getWeekEndDate(cal);
		  
		  
		ArrayList sales = new ArrayList();
	    String    sql = "SELECT dok.kuupaev, dok.tasudok as number,dok.tasudok as tellimus ,  dok.saaja,rid.toode,(rid.kogus) as kogus,(rid.hind) as hind, "+
	    		"ribakood as ean,dok.pais2obj as agent, rid.partii as lotid , rid.rtellimus as origtell FROM firma1.dok INNER JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode t on t.toode=rid.toode "+
	    		"WHERE length(trim(dok.klient)) > 1 AND rid.toode like 'CUM%' "+
	    		//"and kuupaev > current_date-35 "+
	    		"AND dok.kuupaev between ? and ? "+
	    		"AND (dok.doktyyp = 'G' or dok.doktyyp = 'V') and eimuuda = false ORDER BY dok.tasudok, dok.saaja, rid.toode,dok.kuupaev ";
	    
	    		PreparedStatement st = null;
	    		TreeMap vahe = new TreeMap();

	    	          try {
	    	              connectionUp();
	    	            st = con.prepareStatement(sql);
	    	            st.setTimestamp(1, new Timestamp(lastWeekStart.getTimeInMillis()));
	    	            st.setTimestamp(2, new Timestamp(lastWeekEnd.getTimeInMillis()));
	    	            ResultSet rs = st.executeQuery();
	    	            

	    	            while (rs.next()) {
	    	            	  Timestamp kuupaev = rs.getTimestamp("kuupaev");
	    	            	  Calendar cale = Calendar.getInstance();
	    	            	  cale.setTimeInMillis(kuupaev.getTime());
	    	            	  String doknum = rs.getString("number").trim();
	    	            	  String tellimus = rs.getString("tellimus").trim();
	    	            	  String klient = rs.getString("saaja").trim();
	    	            	  String toode = rs.getString("toode").trim();
	    	            	  Double kogus = rs.getDouble("kogus");
	    	            	  Double hind = rs.getDouble("hind");
	    	            	  String ean = rs.getString("ean").trim();
	    	            	  String agent = rs.getString("agent");
	    	            	  String partii = rs.getString("lotid");
	    	            	  String origtell =  tellimus;
	    	            	  if(agent == null)
	    	            		  agent = "suva";
	    	            	  if(agent.trim().length() > 0)
	    	            		  agent = agent.trim();
	    	            	  
	    	            	  //System.out.println(klient+toode+kogus);
	    	            	  Order ord = new Order(klient, agent);
	    	            	  if(vahe.get(doknum)!=null){
	    	            		  ord=(Order)vahe.get(doknum);
	    	            	  }else{
	    	            	  ord.setDokNr(doknum);
	    	            	  ord.setCurrency(tellimus);
	    	            	  ord.setShipTime(Converter.getXMLDate(cale));
	    	            	  }
	    	            	  ord.setAgentCode(agent);
	    	            	  Product prod = new Product(toode);
	    	            	  prod.setPrice(hind);
	    	            	  prod.setStock(kogus);
	    	            	  prod.setEAN(ean);
	    	            	  if(partii != null && partii.trim().length() > 1) {
	    	            		  prod.setPartii(partii.trim());
	    	            	  }else {
	    	            		  prod.setPartii("1");
	    	            	  }
	    	            	  if(origtell != null && origtell.trim().length() > 1) {
	    	            		  prod.setGroup(origtell.trim());
	    	            	  }else {
	    	            		  prod.setPartii("1");
	    	            	  }
	    	            	 
	    	            	  
	    	            	  ord.getOrderLines().add(prod);
	    	            	  vahe.put(doknum, ord);
	    	            	  
	    	            } 
	    	            //System.out.println(vahe.toString());
	    	            rs.close();
	    	            st.close();
	    	            con.close();
	    	          }catch(Exception e){
	    	        	  
	    	          }
	    	        	  
	    	     Iterator itera = vahe.values().iterator();
	    	     while(itera.hasNext()){
	    	    	 sales.add(itera.next());
	    	     }
	    	     
	    	            
 		Converter.makeChumakFiles(this.getOutDir(), prods, this.custMap2, sales, smas);
	}
    
    
    
    
    
	private void sendVeresReports(TreeMap prods , TreeMap smas){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -45);
		
	
		  
		  
		ArrayList sales = new ArrayList();
	    String    sql = "SELECT dok.kuupaev, dok.tasudok as number, dok.saaja  , dok.pais2obj as agent ,rid.toode,(rid.kogus) as kogus,(rid.hind)  as hind "+
	    		"FROM firma1.dok INNER JOIN firma1.rid ON dok.dokumnr = rid.dokumnr "+
	    		"WHERE length(trim(dok.klient)) > 1 AND rid.toode like 'CAV%' "+
	    		"AND dok.kuupaev > ? "+
	    		"AND (dok.doktyyp = 'G' or dok.doktyyp = 'V') and eimuuda = false ORDER BY dok.tasudok, dok.saaja, rid.toode,dok.kuupaev ";
	    		
	    	
	    		TreeMap vahe = new TreeMap();

	    	          try {
	    	        	 Connection con = DriverManager.getConnection(url);
	    	         
	    	          	PreparedStatement st = con.prepareStatement(sql);
	    	            st.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
	    	            ResultSet rs = st.executeQuery();
	    	            

	    	            while (rs.next()) {
	    	            	  Timestamp kuupaev = rs.getTimestamp("kuupaev");
	    	            	  Calendar cale = Calendar.getInstance();
	    	            	  cale.setTimeInMillis(kuupaev.getTime());
	    	            	  String doknum = rs.getString("number").trim();
	    	            	  String klient = rs.getString("saaja").trim();
	    	            	  String ag = rs.getString("agent");
	    	            	  String toode = rs.getString("toode").trim();
	    	            	  Double kogus = rs.getDouble("kogus");
	    	            	  Double hind = rs.getDouble("hind");
	    	            	  //System.out.println(klient+toode+kogus);
	    	            	  if(ag == null) {
	    	            		  ag = " ";
	    	            	  }
	    	            	  Order ord = new Order(klient, ag.trim());
	    	            	  if(vahe.get(doknum)!=null){
	    	            		  ord=(Order)vahe.get(doknum);
	    	            	  }else{
	    	            	  ord.setDokNr(doknum);
	    	            	  ord.setShipTime(Converter.getDDpMMpYYYYpString(cale));
	    	            	  if(ag != null) {
	    	            		  ord.setAgentCode(ag.trim());
	    	            	  }
	    	            	 
	    	            	  }
	    	            	  Product prod = new Product(toode);
	    	            	  prod.setPrice(hind);
	    	            	  prod.setStock(kogus);
	    	            	  ord.getOrderLines().add(prod);
	    	            	  vahe.put(doknum, ord);
	    	            	  
	    	            } 
	    	           // System.out.println(vahe.toString());
	    	            rs.close();
	    	            st.close();
	    	            con.close();
	    	          }catch(Exception e){
	    	        	 e.printStackTrace(); 
	    	          }
	    	        	  
	    	     Iterator itera = vahe.values().iterator();
	    	     while(itera.hasNext()){
	    	    	 sales.add(itera.next());
	    	     }
	    	     
	    	     this.getAgents();
	    	     System.out.println("Prods Size " + prods.size());
 		Converter.makeVeresFiles(this.getOutDir2(), prods, this.custMap2, sales,"136", agent1, smas);
	}
    
    
    
	private void sendFlintReports(TreeMap prods , TreeMap smas){
		
		Calendar cals = Calendar.getInstance();
		if(cals.get(Calendar.DAY_OF_MONTH) < 5) {
			cals.add(Calendar.DAY_OF_MONTH, -6);
			 cals.set(Calendar.DAY_OF_MONTH, 1);
		}else {
		 cals.set(Calendar.DAY_OF_MONTH, 1);
		}
		
		Calendar cale = Calendar.getInstance();
		if("true".equals(parent.getProps().getProperty("flint"))){
		   //cale.add(Calendar.DAY_OF_MONTH, -7);
		}
		if(cale.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				    while (cale.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				        cale.add(Calendar.DAY_OF_WEEK, 1);
				    }
		}	    
			
		Calendar cc = Calendar.getInstance();
		cc.setTimeInMillis(cale.getTimeInMillis());
		cc.add(Calendar.DAY_OF_MONTH, -1);
		String period  = Converter.getDDpMMpYYYYpString(cals) + " - " + Converter.getDDpMMpYYYYpString(cc) ;
	
		  
    	String fname = this.getExcelDir() + "flint/flint.xlsx";
	 
	    
	    String    sql = "SELECT rid.toode,sum  (rid.kogus) as kogus,sum(rid.hind*rid.kogus)  as summa " + 
	    		"	    		FROM firma1.dok INNER JOIN firma1.rid ON dok.dokumnr = rid.dokumnr " + 
	    		"	    		WHERE length(trim(dok.klient)) > 1 AND rid.toode like 'FLI%' " + 
	    		"	    		AND dok.kuupaev >= ? and  dok.kuupaev < ? " + 
	    		"	    		AND (dok.doktyyp = 'G' or dok.doktyyp = 'V') and eimuuda = false " + 
	    		"				group by 1 ORDER BY rid.toode ";
	    		
	    	
	    		TreeMap vahe = new TreeMap();

	    	          try {
	    	        	  
	    	        	  
	    	        	  
	    	        	  FileInputStream fIPS= new FileInputStream(fname); //Read the spreadsheet that needs to be updated
	    	  	        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
	    	  	        XSSFSheet worksheet = wb.getSheetAt(0);	
	    	  	        
	    	  	      XSSFRow row1 = worksheet.getRow(0);  //0 = row number
				        Cell c = row1.getCell(1);
				             c.setCellValue(period);
				    
				         
	    	        	 Connection con = DriverManager.getConnection(url);
	    	         
	    	          	PreparedStatement st = con.prepareStatement(sql);
	    	            st.setTimestamp(1, new Timestamp(cals.getTimeInMillis()));
	    	            st.setTimestamp(2, new Timestamp(cc.getTimeInMillis()));
	    	            ResultSet rs = st.executeQuery();
	    	            
                            int i = 2;
                            double sum = 0;
                            double sumkog = 0;
                            double sumlad = 0;
	    	            while (rs.next()) {
	    	            	
	    	            	  String toode = rs.getString("toode").trim();
	    	            	  Double kogus = rs.getDouble("kogus");
	    	            	  Double hind = rs.getDouble("summa");
	    	            	  sum = sum + hind;
	    	            	  sumkog = sumkog + kogus;
	    	            	  Product prod = (Product)prods.get(toode);
	    	            	  String ean = prod.getEAN();
	    	            	  String nimi = prod.getName();
	    	            	  String unit = prod.getUnit();
	    	            	  if(unit.trim().length() == 0) {
	    	            		  unit = "pc.";
	    	            	  }
	    	            	  Double laos = (Double)smas.get(toode);
	    	            	  if(laos == null) {
	    	            		  laos = 0d;
	    	            	  }
	    	            	  
	    	            	  sumlad= sumlad + laos;
	    	            	  worksheet.createRow(i);
	    	            	  row1 = worksheet.getRow(i);  //0 = row number
	    	            	  
	    	            	  row1.createCell(0);
						      c = row1.getCell(0);
						      c.setCellValue(ean);
						      
						      row1.createCell(1);
						      c = row1.getCell(1);
						      c.setCellValue(Converter.deCode(nimi));
						      
						      row1.createCell(2);
						      c = row1.getCell(2);
						      c.setCellValue(unit);
						      
						      row1.createCell(3);
						      c = row1.getCell(3);
						      c.setCellValue(kogus);
						      
						      row1.createCell(4);
						      c = row1.getCell(4);
						      c.setCellValue(hind);
						    
						      row1.createCell(5);
						      c = row1.getCell(5);
						      c.setCellValue(laos);
	    	            	  
	    	            	i++;  
	    	            } 

	    	            
	    	            CellStyle style = wb.createCellStyle();
	    	            Font font = wb.createFont();
	    	            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    	            style.setFont(font);
	    	            
  	            	   worksheet.createRow(i);
	    	            row1 = worksheet.getRow(i); 
	    	            
	    	          
	    	            
	    	              row1.createCell(2);
	    	              c = row1.getCell(2);
					      c.setCellValue("Total");
					      c.setCellStyle(style);
					      
					      row1.createCell(3);
					      c = row1.getCell(3);
					      c.setCellValue(sumkog);
					      c.setCellStyle(style);
					      
					      row1.createCell(4);
					      c = row1.getCell(4);
					      c.setCellValue(sum);
					      c.setCellStyle(style);
					      
					      row1.createCell(5);
					      c = row1.getCell(5);
					      c.setCellValue(sumlad);
					      c.setCellStyle(style);
					      
					      
	    	           // System.out.println(vahe.toString());
	    	            rs.close();
	    	            st.close();
	    	            con.close();
	    	            
	    	            
	    	            
	    	            
	   	    	     FileOutputStream output_file =new FileOutputStream(this.getExcelDir() + "flint/Flint_" + period + ".xlsx");
	   			        wb.write(output_file); //write changes
	   			        output_file.close();  //close the stream
	   			        
	   			        
	   			        
	   			     String address = parent.getProps().getProperty("flintemail");
	   			    		 
	   			    
	   			 
	   		//	  System.out.println("määratud email " + emai + " " + props.toString());
	   			  if(address != null ){
	   				  Email email = new Email("Interaltus Salesreport - Flint");
	   		          email.setFrom(parent.getProps().getProperty("from"));
	   		          email.setSMTP(parent.getProps().getProperty("smtphost"));
	   		          email.setUser(parent.getProps().getProperty("smtpuser"));
	   		          email.setPass(parent.getProps().getProperty("smtppass"));
	   		          email.setPort(parent.getProps().getProperty("smtpport"));
	   		          email.setSubjectFilesTo("Interaltus Salesreport - Flint");
	   		          email.setEncoding("iso-8859-1");
	   		         
	   		          try {
	   		        	  
	   		        	email.setBodyHtml("Sales report for last week " + period);
	   	                email.addAttach(this.getExcelDir() +"flint/Flint_" + period + ".xlsx");
	   		            StringTokenizer st1 =  new StringTokenizer(address,";");
	   		            String aadr = null;
	   		            while(st1.hasMoreTokens()){
	   		            	 aadr = new InternetAddress(st1.nextToken()).toString();
	   		              email.setFilesTo(aadr);
	   		              email.send();
	   		              }
	   		            
	   		              
	   		            
	   				} catch (AddressException e) {
	   					// TODO Auto-generated catch block
	   					e.printStackTrace();
	   				} catch (UnsupportedEncodingException e) {
	   					// TODO Auto-generated catch block
	   					e.printStackTrace();
	   				} catch (MessagingException e) {
	   					// TODO Auto-generated catch block
	   					e.printStackTrace();
	   				}
	   				  		  
	   			  }
	   			        
	   			        
	   			        
	   			        
	   			        
	   			        
	    	          }catch(Exception e){
	    	        	 e.printStackTrace(); 
	    	          }
	    	        	  
	    	  
	    		
	    	     System.out.println("Prods Size " + prods.size());
 	    	     
	}
    
    
    
    
    
    
    public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,0 AS discount,mt.hind " + 
       "   AS price , algus , lopp FROM firma1.hinnamtr mt inner join firma1.hkpais p on p.hinnak = mt.hinnak where " +
       "  algus <= current_date and current_date<=lopp order by mt.klient,mt.klliik,mt.toode";
          Statement st = null;
          try {
        	  
            connectionUp();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid");
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid");
              String prodGroup = rs.getString("prodgroup");
              double discount = rs.getDouble("discount");
              double price = rs.getDouble("price");
              String algus = rs.getString("algus");
              String lopp = rs.getString("lopp");
              
              MatrixLine mline = new MatrixLine();

              if (lopp != null) {
					mline.setEndDate(lopp.substring(0, 10));
				}
				if (algus != null) {
					mline.setStartDate(algus.substring(0, 10));
				}
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
                price = 0;
              }
                mline.setDiscount(discount);

              if (price != 0) {
                mline.setPrice(price);
              }
              if(price>0 || discount >0)
             lines.add(mline);
            }

            rs.close();
            st.close();
            this.connectionDown();
          }
          catch (SQLException ex) {
          }
          
          
              sql = " SELECT mt.klient AS custid,mt.klliik AS clgroup,toode AS prodid,mt.artliik AS prodgroup,0 AS discount,mt.hind  " + 
          " AS price ,algus,lopp FROM firma1.hinnamtr mt inner join firma1.hkpais p on p.hinnak = mt.hinnak where " +
          "algus <= current_date and current_date<=lopp order by mt.klient,mt.klliik,mt.toode";
            st = null;
           try {
             connection2Up();
             st = con2.createStatement();
             ResultSet rs = st.executeQuery(sql);

             while (rs.next()) {

               String custID = rs.getString("custid");
               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid");
               String prodGroup = rs.getString("prodgroup");
               double discount = rs.getDouble("discount");
               double price = rs.getDouble("price");
               String algus = rs.getString("algus");
               String lopp = rs.getString("lopp");
               
               MatrixLine mline = new MatrixLine();

               if (lopp != null) {
					mline.setEndDate(lopp.substring(0, 10));
				}
				if (algus != null) {
					mline.setStartDate(algus.substring(0, 10));
				}
               if (custID != null && custID.trim().length() > 0) {
            	  // System.out.println("Kiili koodi vastavus olemas " + custID + " " + custCodes.get(custID.trim()));
            	   if(custCodes.get(custID.trim()) != null){
            		   mline.setCustID(custCodes.get(custID.trim())+ "");
                       	   
            	   }
               }
               if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                 mline.setCliGroup(clGroup.trim());
               }

               if (prodID != null && prodID.trim().length() > 0) {
                 mline.setProdID("KI-" + prodID.trim());
               }
               if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                 mline.setProdGroup(prodGroup.trim());
                 price = 0;
               }
                 mline.setDiscount(discount);

               if (price != 0) {
                 mline.setPrice(price);
               }
               if(price>0 || discount >0)
              lines.add(mline);
             }

             rs.close();
             st.close();
             this.connection2Down();
           }
           catch (SQLException ex) {
           }

          
        return lines;
      }
    
    

    
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }
    public String getSaveStockInfo(){
    	
    	  StringBuffer products = new StringBuffer();
        String sql3 = "select s.kogus as asi , s.toode  from  " +
        " firma1.seisud s  where s.osak ='1'";

try{
	 Connection conv2 = DriverManager.getConnection(this.url2);
PreparedStatement pstm = conv2.prepareStatement(sql3);
ResultSet rs6 = pstm.executeQuery();

while(rs6.next()){
	   double seis = rs6.getDouble("asi");
	   if(seis > 0){
		   products.append("KI-" + rs6.getString("toode").trim() + ";");
			products.append(" ;");
			products.append(getParent().getNF().format(seis) + "; "
					+ "\n");  
	   }
      
}
//System.out.println("Klili laoseisud tehtud " + products.toString());
rs6.close();
pstm.close();
conv2.close();

}catch (Exception e){
	e.printStackTrace();
}
    	
    	
        stocks = new TreeMap();
          

         String sql = "set search_path to firma1,public;\r\n" ; 
        		  String sql2 ="select sum(s.kogus) as kogus, s.toode from (\r\n" + 
         		"select * from crlaosei6())as  s where s.objekt = '3' or s.toode like 'FLi%' group by s.toode ";
         if (isDebug()) {
             System.out.println(sql);
         }

         try {

        	 Connection con = DriverManager.getConnection(url);
        	 PreparedStatement pstm = con.prepareStatement(sql);
        	 ResultSet rs = null;
        	  try {
        		   pstm.executeQuery();
        	  }catch(Exception ee) {
        		  
        	  }
     
          pstm = con.prepareStatement(sql2);
          rs = pstm.executeQuery();
          
          while (rs.next()) {
        	  double seis = rs.getDouble("kogus");
        	  String  toode =  rs.getString("toode").trim();
        	  if(seis > 0){
        		  stocks.put(toode, seis);
        		  products.append(toode + ";");
           		  products.append(" ;");
           		  products.append(getParent().getNF().format(seis) + "; "
           				+ "\n");

                 // System.out.println("IA laoseisud " + seis + " " + toode);
        	  }
        	
          }
          rs.close();
          pstm.close();
           con.close();
            } catch (Exception e) {
                getParent().showError(e);
            }
            
         //  System.out.println("K�ik laoseisud tehtud " + products.toString());
            
            return products.toString();
        }

private TreeMap stocks = null;


                public String getDealerCustomerCSV(String agent){
                 return null;
                 }

                                                    public String  getCustProdDiscounts(Agent agent){
                                                     return null;
                                                     }


    /**
     *
     */
    public void connectionUp(){
      if (con == null){
        try {
           con = DriverManager.getConnection(url);
        } catch (Exception e) {
            getParent().showError(e);
        }
      }
    }
    public void connection2Up(){
        if (con2 == null){
          try {
             con2 = DriverManager.getConnection(url2);
          
          } catch (Exception e) {
            //  getParent().showError(e);
          }
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
    public void connection2Down(){
        try {
          
           con2.close();
           con2 = null;
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
    private String getOutDir(){
        String dir = getParent().getProps().getProperty("readout");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }
    
    private String getOutDir2(){
        String dir = getParent().getProps().getProperty("readout2");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }
    
    private TreeMap agent1 = new TreeMap();
    
    public TreeMap getAgents() {
    
        String  cmd = "SELECT yksus,nimetus,objekt FROM firma1.yksus2 ";
        TreeMap agents = new TreeMap();

        try {
        	con = DriverManager.getConnection(url);
            Statement	       stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String obj = rs.getString("objekt");
              if(obj != null){
            	  agent.setObject(obj);
              }
             
              String name = rs.getString("nimetus").trim();
              agent.setName(name);
                agents.put(name, agent);
                agent1.put(agent.getID(), agent);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            getParent().showError(e);
        }
        connectionDown();
        
        
        if("true".equals(parent.getProps().getProperty("flint"))){
        	this.getProducts(null);
			
			   TreeMap smas = new TreeMap();
				 
			   String sql = "set search_path to firma1,public;\r\n" ; 
     		  String sql2 ="select  s.toode  , sum(s.kogus) as kogus from (\r\n" + 
      		"select * from crlaosei6())as  s where s.objekt = '3' or s.toode like 'FLi%' group by s.toode ";

				try{
					Connection con = DriverManager.getConnection(url);
					
					PreparedStatement pstm = null ;
					ResultSet rs6 = null ;
					try {
						pstm = con.prepareStatement(sql);
						 rs6 = pstm.executeQuery();
					}catch(Exception e) {
						
					}

					
					 pstm = con.prepareStatement(sql2);
					 rs6 = pstm.executeQuery();

					while(rs6.next()){
					smas.put(rs6.getString("toode").trim(),rs6.getDouble("kogus") );
					}
					rs6.close();
					pstm.close();
					con.close();
					//System.out.println("smas " + smas.size());
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			sendFlintReports(ppprods,smas);
		}
        
        
        return agents;
    }
    
private Hashtable custCodes = null;
    
    public TreeMap getCustomers(Agent agent) {

    	  File fff = new File(this.getExcelDir() + "/sortiment.xlsx" );
          TreeMap groups = new TreeMap(); 
      	try{
              FileInputStream ff = new FileInputStream(fff);
      		XSSFWorkbook workbook = new XSSFWorkbook((ff));
      		XSSFSheet sheet = workbook.getSheet("shops");
      		XSSFRow row = null;
      		
      		row = sheet.getRow(0);
      	    // teeme valmis gruppide Treemapi key grupp ja Vectoris koodid
      		
      	//	System.out.println("rownum " + row.getLastCellNum());
      		
      		
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
    	    	    			  	 if(kood.indexOf(".")!= -1){
    	    	    			  	 	 kood = (kood.substring(0,kood.indexOf(".")));	
    	    	    			  	 }
    	    	    			  	 String sort = null;
    	    	    			  	try{
    	    	    			  		sort = row.getCell((short)1).getStringCellValue();  
    		    	    			  	 }catch (Exception e){
    		    	    			  		sort = row.getCell((short)1).getNumericCellValue()+"";  
    		    	    	    		} 
    		    	    			  	 if(sort.indexOf(".")!= -1){
    		    	    			  	 	 sort = (sort.substring(0,kood.indexOf(".")));	
    		    	    			  	 }
    	    	    			  	 groups.put(kood.trim(),sort.trim());
      	    			  
      	    				
      				}	   
      				}
     			}catch (Exception e){
      				//System.out.println(row.getCell((short)a).getNumericCellValue());
      			}
      		}
      		
      		//System.out.println(groups.toString());
      
      	 }catch (Exception e){
      		e.printStackTrace();
      	}
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
        custCodes = new Hashtable();
        // String cmd = "SELECT k.kood, k.nimi FROM (klient k INNER JOIN omrid ON k.kood = omrid.klient INNER JOIN rid ON rid.dokumnr = omrid.tasudjrk) WHERE omrid.rid2obj='" + agentCode + "' AND (len(rid.toode) > 2) GROUP BY k.kood";
        String cmd =
            " SELECT k.kood,k.maksja, k.nimi,aadress,k.hinnaale AS ale,k.visit, k.tarne as abi,k.objekt2  as agent,k.klikaart as agent2 ,wwwpage as kkyl" +
            " ,k.infomail as partner ,liik " +
          "  FROM firma1.klient k  where mitteakt is false ORDER BY k.nimi";

        log(cmd);

     
        
       TreeMap customers = new TreeMap();
       
        try {
        	Connection   con = DriverManager.getConnection(url);
        	Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd);

            while (rs.next()) {
              Customer cust = new Customer(rs.getString("kood").trim());
              String maks = rs.getString("maksja");
              if(maks != null && maks.trim().length() >0){
            	  cust.setPayer(maks.trim());
              }
            String nimi = Converter.enCodeEst(rs.getString("nimi").trim());
            cust.setName(Converter.enCodeEst(nimi));
            
            String addr = rs.getString("aadress");
            if(addr != null){
            	addr = addr.replaceAll("\\r\\n|\\r|\\n", " ");
            	 cust.setAddress(Converter.enCodeEst(Converter.adressLink(addr.trim(),"Estonia")));	
            }
           
        
            String visit = rs.getString("visit");
            if(visit != null && visit.trim().length()>0){
            	cust.setVisit(visit.trim());	
            }
            String abi = rs.getString("abi");
            if(abi != null && abi.trim().length()>0){
            	cust.addAgent(new Agent(abi.trim()));	
            }
            String abi2 = rs.getString("agent");
            if(abi2 != null && abi2.trim().length()>0){
            	cust.addAgent(new Agent(abi2.trim()));	
            }
            
            if(groups.get(cust.getID()) != null){
            	cust.addFixSortCode(groups.get(cust.getID())+"");
            }
            
            String abi5 = rs.getString("agent2");
            if(abi5 != null && abi5.trim().length()>0){
            	String abi6 = rs.getString("kkyl");
            	Agent aa = new Agent(abi5.trim());
            	if(abi6 != null && abi6.trim().length() >0){
            		aa.setVisit(abi6.trim());
            	}
            	cust.addAgent(aa);	
            //	System.out.println("aa " + aa.getID());
            }
            
            String abi3 = rs.getString("partner");
            if(abi3 != null && abi3.trim().length()>0){
            	custCodes.put(abi3.trim(), cust.getID());            	
            	cust.setCity("KIILI Kood: " + abi3.trim());	
            }
            
            String liik = rs.getString("liik");
            if (liik != null && liik.trim().length()>0){
            	cust.setGroup(liik.trim());
            }
           
                customers.put(nimi, cust);
                custMap2.put(cust.getID(), cust);
            }
            

            String sql = "SELECT d.klient AS klient, SUM(d.tasumata) AS summa  from firma1.dok d  WHERE  " +
              "  current_date>d.tasukuup and d.doktyyp='G' AND d.tasumata <> 0 AND length(klient) > 0 GROUP BY d.klient ORDER BY d.klient";
           ResultSet   rst =  stmt.executeQuery(sql);
            while(rst.next()){
              String id = rst.getString("klient").trim();
              double debt = Double.parseDouble(rst.getString("summa").trim());
           Customer cc1 = (Customer)custMap2.get(id);
           
            Iterator iter = custMap2.values().iterator(); 
            //System.out.println("Olemas2 "  + " " + id + " " + debt);
            while (iter.hasNext()){
            	Customer cc = (Customer)iter.next();
            	// 
            	try {
            		if(cc.getPayer().equals(id) || cc.getID().equals(id) || cc1.getPayer().equals(cc.getPayer())){
                		cc.setDebt(debt);   
                		if(debt > 499){
                			cc.setSalesBlock(true);
                		}
                	}
            	}catch(Exception e) {
            		
            	}
            	
            }
            	  
              
            }
            rst.close();
            stmt.close();
            
            
        } catch (Exception e) {
            getParent().showError(e);
        }

        
        System.out.println("Customers: " + customers.size());

        return customers;
    }

   private TreeMap custMap2 = new TreeMap();


		public String getDistribution() {
          return null;
        }

		
		  private java.util.Date getCalXML(String ss){
			  Calendar cc = Calendar.getInstance();
			  	if(ss != null && ss.length() == 6){
			  		cc.set(Calendar.YEAR,2000 + Integer.parseInt(ss.substring(0,2)));
				  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(2,4))-1);
				  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(4,6)));
				}
			  	return cc.getTime() ;
		  }
		

		  
    public TreeMap getProducts(Agent agent) {
    	
    	eanprods = new TreeMap();
    	getSaveStockInfo();
        connectionUp();
        TreeMap products = new TreeMap();
        HashMap partiid = new HashMap();
        HashMap omahinnad = new HashMap();
        HashMap kriitiline = new HashMap();
        
        
        
 

 String sql = "set search_path to firma1,public;\r\n" ; 
 String sql2 ="select  s.toode , (s.kogus) as kokku , s.partii ,s.laosumma/s.kogus as oh , t.real as kuud  from (\r\n" + 
 		"select * from crlaosei6())as  s  inner join firma1.toode t on t.toode=s.toode  where s.objekt = '3' and s.kogus > 0 ";

try{
	Connection con = DriverManager.getConnection(url);

PreparedStatement pstm = null ;
ResultSet rs6 = null ;
try {
	pstm = con.prepareStatement(sql);
	 rs6 = pstm.executeQuery();
}catch(Exception e) {
	
}


pstm = con.prepareStatement(sql2);
 rs6 = pstm.executeQuery();
while(rs6.next()){
	String toode  = rs6.getString("toode").trim(); 
	double kog  = rs6.getDouble("kokku");
	String partii = rs6.getString("partii");
	double oh  = rs6.getDouble("oh");
	int kuud = rs6.getInt("kuud");
	
	if(kog > 0){
	
	if(partii != null ){
	
	try{
		
		long pross = (long)(kuud * 0.15 * 1000* 60*60*24*30);
		java.util.Date now = new java.util.Date();
		java.util.Date parimenne = getCalXML(partii.trim());
		if(now.getTime()+pross > parimenne.getTime()){
			kriitiline.put(toode, partii + " : " + kog );
			//System.out.println("KriitilineMM " + pross + " " + parimenne.toLocaleString());

		}
	}catch(Exception w){
		//w.printStackTrace();
	}
	
	}
	
	
	
	if(partiid.get(toode) != null){
   String asi = ((String)partiid.get(toode)).trim() ;
		partiid.put(toode,asi + "|  Par-" + partii + " : " + kog );	
	}else{
		partiid.put(toode, "  Par-" + partii + " : " + kog );	
	}
	
	omahinnad.put(toode, oh);
	}

}

// System.out.println("Kriitiline " + kriitiline.toString());

rs6.close();
pstm.close();
con.close();
}catch (Exception e){
	e.printStackTrace();
}

 
           //    System.out.println("Omahinnad " + omahinnad.toString());
      

          String cmd = "SELECT toode, nimetus, aktsiis as tykke, myygihind, yhik ,hankija " +
          		",ribakood ,photoid ,minmargin , droid FROM firma1.toode WHERE " +
              " myygihind >0  AND length(trim(toode)) >3 and length(trim(nimetus)) > 1 ORDER BY toode";
try{


Connection conw = DriverManager.getConnection(url);
Statement stmtw = conw.createStatement();
                    ResultSet  rs = stmtw.executeQuery(cmd);

                      while (rs.next()) {
                          String kood = rs.getString("toode").trim();
                        
                          double kogus = -1;
                          if (stocks.get(kood) != null){
                            kogus = Double.parseDouble(stocks.get(kood)+"");
                          } 
                          String nimetus = " " +  kood + " " + rs.getString("nimetus").trim();
                          String yhik = rs.getString("yhik"); 
                          if(yhik != null){
                        	  nimetus = nimetus + " " + yhik.trim();
                          }
                          Product prod = new Product(kood);
                          prod.setName(Converter.enCodeEst(nimetus));
                          prod.setPrice(rs.getDouble("myygihind"));
                          prod.setInCases(rs.getDouble("tykke"));
                          prod.setStock(kogus);
                          prod.setGroup(parent.make(rs.getString("hankija")));
                          prod.setEAN(parent.make(rs.getString("ribakood")).replaceAll(";", "*"));
                          if(prod.getEAN() != null) {
                        	   eanprods.put(prod.getEAN(), prod.getID());  
                          }
                          prod.setType("INTER");
                          String phot = rs.getString("photoid");
                          if(phot != null && phot.trim().length() > 0){
                        	//  prod.setPhotoid("s_" + phot.trim()+".jpg"); 
                          }
                         
                          if(kriitiline.get(kood) != null){
                        	 // System.out.println("Kriitiline " + kriitiline.get(kood) + " " + kood );
                          //	prod.setStatusNew("1") ; 
                          	prod.setStatusGiftPack("1");
                          	prod.setDiffColor(true);
                            }
                         
                          
                          
                         if(partiid.get(kood) != null){
                        	prod.setPartii(partiid.get(kood) + "") ; 
                          }
                          
                         if(omahinnad.get(kood) != null){
                        	 double oh = Double.parseDouble(omahinnad.get(kood)+"");
                        	 double kate = (1-oh/prod.getPrice())*100;
                         	if(kate>0){
                         		String kat = (kate + "");
                                prod.setMargin(kat.substring(0,kat.indexOf(".")));
                         	}
                         	prod.setMinPrice(oh*1.2);
                           }
                         
                         double minprice = rs.getDouble("minmargin");
                         if(minprice > 0){
                        	 prod.setMinPrice(minprice);
                        	 
                         }
                         
                         
                boolean droid = rs.getBoolean("droid");
                         if(droid) {
                        	  products.put(nimetus, prod); 
                         }
                         pprods.put(nimetus + kood,prod);
                         
                         ppprods.put( kood,prod);

                      }
                      rs.close();
                      stmtw.close();
                      conw.close();

                      
                  
                 
                      
                      
        } catch (Exception e) {
            getParent().showError(e);
        }
		//System.out.println("Products: " + products);
        return products;
    }

    
    private TreeMap pprods = new TreeMap();
    private TreeMap ppprods = new TreeMap();
    
    
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
    public TreeMap getProdGroups() throws SQLException {
    	   this.connectionUp();
    	     TreeMap   tm = new TreeMap();
    	     String    sql = "SELECT k.kood, k.nimi FROM firma1.toode t inner join firma1.klient k on k.kood =t.hankija WHERE " +
            " t.myygikood = 'K' AND length(trim(t.toode)) >3 group by k.nimi,k.kood ORDER BY k.kood  ";
    	      Statement	       stmt = con.createStatement();
    	     ResultSet rs = stmt.executeQuery(sql);

    	     while (rs.next()) {
    	       String liik=rs.getString("kood").trim();
    	       String nimetus= " " + Converter.enCodeEst(rs.getString("nimi").trim());
    	       if (liik != ""){
    	       tm.put(liik,nimetus);
    	       }
    	     }
    	     rs.close();
    	     stmt.close();

    	     connectionDown();
    	     
    	     this.connection2Up();
    	  
    	     String    sql2 = "SELECT k.kood, k.nimi FROM firma1.toode t inner join firma1.klient k on k.kood =t.hankija WHERE " +
            "  length(trim(t.toode)) >3 group by k.nimi,k.kood ORDER BY k.kood";
    	     Statement	   stmt2 = con2.createStatement();
    	     ResultSet rs2 = stmt2.executeQuery(sql2);

    	     while (rs2.next()) {
    	       String liik = rs2.getString("kood").trim();
    	       String nimetus = Converter.enCodeEst(rs2.getString("nimi").trim());
    	       if (liik != ""){
    	       tm.put(liik,nimetus);
    	       }
    	     }
    	     
    	     rs2.close();
    	     stmt2.close();

    	     connection2Down();
    	     return tm;
    	 }

    public Vector getCustomerProductsList(String agentCode) {
        connectionUp();
	/*
	 * String cmd = "SELECT omrid.klient,rid.toode FROM omrid INNER JOIN rid ON omrid.tasudjrk = rid.dokumnr WHERE omrid.rid2obj='" +
	 * agentCode + "' AND (len(omrid.klient) > 1) AND (len(rid.toode) > 2) AND omrid.tasudtyyp='G' AND omrid.reakuupaev >= " +
	 * getStartingTime() + " GROUP BY omrid.klient, rid.toode ORDER BY omrid.klient, rid.nimetus";
	 */
	String cmd =
	    "SELECT dok.saaja,rid.toode,dok.kuupaev,max(rid.kogus) as kogus,max(rid.hind)  as hind  FROM firma1.dok INNER " +
	  " JOIN firma1.rid ON dok.dokumnr = rid.dokumnr INNER JOIN firma1.toode on rid.toode = toode.toode INNER JOIN firma1.klient k on k.kood= dok.saaja WHERE " +
	   "  length(trim(dok.klient)) > 1 AND length(trim(rid.toode)) > 3 " +
	   "  AND dok.kuupaev >= current_date-130 " +
	   "  AND  dok.doktyyp = 'G'  GROUP BY dok.saaja, rid.toode , dok.kuupaev ORDER BY dok.saaja, rid.toode,dok.kuupaev ";
    
	if (isDebug()) {
	    System.out.println(cmd);
	}

	Vector custProds = new Vector();

	try {
		 Statement	       stmt = con.createStatement();
	     
	    ResultSet	      rs = stmt.executeQuery(cmd);

	    while (rs.next()) {
		String kliendikood = rs.getString("saaja").trim();
		
		String tootekood = rs.getString("toode").trim();
		String kuupaev = rs.getString("kuupaev");
        double kogus = Double.parseDouble(rs.getString("kogus").trim());
        double hind = rs.getDouble("hind");
            if (kliendikood.length() > 1) {
                  CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
                  custProds.add(cp);
                }
	    }

            rs.close();
            stmt.close();

            connection2Up();
            Statement	   stmt2 = con2.createStatement();
            rs = stmt2.executeQuery(cmd);

    	    while (rs.next()) {
    		String kliendikood = rs.getString("saaja").trim();
    		try{
    			kliendikood = (String)custCodes.get(kliendikood);
    		}catch(Exception e){
    			
    		}
    		
    		String tootekood = rs.getString("toode").trim();
    		String kuupaev = rs.getString("kuupaev");
            double kogus = Double.parseDouble(rs.getString("kogus").trim());
            double hind = rs.getDouble("hind");
                if (kliendikood != null && kliendikood.length() > 1) {
                      CustProd cp = new CustProd(kliendikood, "KI-" + tootekood, kogus ,0 ,0,kuupaev.substring(5,10),hind,0,0," ");
                      custProds.add(cp);
                    }
    	    }

                rs.close();
                stmt2.close();
           this.connection2Down();
            
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return custProds;
    }



   

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT klient,saaja,tasudok AS number,tasukuup AS maksepaev,tasumata, kuupaev FROM " +
                   "firma1.dok WHERE  tasukuup<current_date+5 AND doktyyp='G' AND tasumata<>0 " +
                   "ORDER BY klient";

      connectionUp();
      PreparedStatement st = con.prepareStatement(sql);
     // st.setString(1, agentCode);
      ResultSet rs = st.executeQuery();
      while(rs.next()){
        Debt debt = new Debt();
        String saaja = rs.getString("saaja");
        if(saaja != null){
        	 debt.setCustID(saaja.trim());
             debt.setDoknr(rs.getString("number").trim());
             debt.setDuedate(rs.getDate("maksepaev"));

             debt.setSum(rs.getDouble("tasumata"));
             debt.setDate(rs.getDate("kuupaev"));
             list.addElement(debt);
        }
       
      }
      st.close();
      return list;
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
        connectionUp();
        double  salary = 0;

        try {

/*	String  sql = "SELECT ajut,tasu,plaan FROM yksus WHERE yksus = '" + agentCode
                      + "'";

        if (debug) {
            System.out.println(sql);
        }

            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                ajut = rs.getBoolean("ajut");
                koef = rs.getDouble("tasu");
                setBudget(rs.getDouble("plaan"));
            }

            rs.close();
*/
           /* sql = "select sum(doksumma) as eelmineaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())-1=year(kuupaev)";
            //+ "< (select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())=year(kuupaev))";
          if (debug) {
              System.out.println(sql);
          }

            double eelmineaasta = 0;
            double seeaasta = 0;

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                eelmineaasta = rs.getDouble("eelmineaasta");
            }
            rs.close();


            sql = "select sum(doksumma) as seeaasta  from dok where (month(kuupaev))=(month(date())) and doktyyp='G' and year(date())=year(kuupaev)";
            if (debug) {
                System.out.println(sql);
            }

            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                seeaasta = rs.getDouble("seeaasta");
            }
            rs.close();

            if (eelmineaasta < seeaasta){
              bonus = 3000;
            }

*/	    // k�sioleva m��gimehe selle kuu m��gi leidmine
            String sql =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
                System.out.println(sql);
            }

            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, agentCode);

            ResultSet rs = pstmt.executeQuery();

            double seekuu = 0;

            if (rs.next()) {
              seekuu = rs.getDouble("seekuu");
              setSales(seekuu);
            }
            rs.close();
            pstmt.close();
            String sql2 =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and yksus='3' and doktyyp='G'"
                + " AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
              System.out.println(sql2);
            }

            PreparedStatement pstmt2 = con.prepareStatement(sql2);
            pstmt2.setString(1, agentCode);

            ResultSet rs2 = pstmt2.executeQuery();

            double seekuuladu3 = 0;

            if (rs2.next()) {
              seekuuladu3 = rs2.getDouble("seekuu");
              setSales3(seekuuladu3);
            }
            rs2.close();
            pstmt2.close();

            String sql1 =
                "select sum(doksumma)/1.20 as seekuu  from firma1.dok where (month(kuupaev))=(month(current_date)) and yksus='4' and doktyyp='G' AND year(current_date)=year(kuupaev) AND pais2obj=?";
            if (isDebug()) {
              System.out.println(sql1);
            }

            PreparedStatement pstmt1 = con.prepareStatement(sql1);
            pstmt1.setString(1, agentCode);

            ResultSet rs1 = pstmt1.executeQuery();

            double seekuuladu2 = 0;

            if (rs1.next()) {
              seekuuladu2 = rs1.getDouble("seekuu");
              setSales4(seekuuladu2);
            }
            rs1.close();
            pstmt1.close();


        } catch (Exception e) {
            getParent().showError(e);
        }

        salary = Math.round(salary * 100d) / 100d;

        return salary;
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
        Calendar cal = Calendar.getInstance();

        // cal.add(Calendar.MONTH, -6);
        cal.add(Calendar.MONTH,
                -Integer.parseInt(getParent().getProps().getProperty("months")));

        int month = cal.get(Calendar.MONTH) + 1;
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String sqlDate = "{^" + day + '-' + month + '-'
                         + cal.get(Calendar.YEAR) + '}';

        return sqlDate;
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

    public String getPilotDayData(Agent agent){
      return null;
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

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "Interaltus";
    private String	       url = "jdbc:odbc:eeva";
    private String	       url2 = "jdbc:odbc:eevakiil";
    private Connection	       con = null;
    private Connection	       con2 = null;

    private String	       subjectFilesTo = null;
	@Override
	public void setBulkSales(Map agents) {
		  connectionUp();
    		String cmd =  "  select sum(doksumma)/1.20 as sales , dok.pais2obj  AS agent   from firma1.dok where (month(kuupaev))=(month(current_date)) and doktyyp='G' AND year(current_date)=year(kuupaev) " +
			  "  GROUP BY dok.pais2obj" ;
    	
    		log(cmd);
    		
    		try {
    			
    			Statement stmt = con.createStatement();		
    		
    			
    			ResultSet	      rs = stmt.executeQuery(cmd);
    			while(rs.next()) {
    				String agentID = rs.getString("agent");
    				if(agentID != null){
    					Agent agent = (Agent)agents.get(agentID.trim());
        				if (agent != null){
        					agent.setMonthSales((int)rs.getDouble("sales"));
        				}
    				}
    				
    			}
    			rs.close();
    			stmt.close();
    			con.close();
    		} catch (Exception e) {
    			getParent().showError(e);
    		}
    		connectionDown();
		
	}

	@Override
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "sortiment.xlsx");
		
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			

			XSSFSheet sheetc = workbook.getSheetAt(1);
		
			TreeMap shops = new TreeMap();
			TreeMap tm = new TreeMap();
			for (int a = 0; a < sheetc.getLastRowNum(); a++) {
	           try {
	        	   row = sheetc.getRow(a);
	        	  String custid = null;
	        	  try{
	        		  custid = Converter.cleanLitLatEst(row.getCell((short) 0).getStringCellValue());
	        	  }catch(Exception r){
	        		  custid = Converter.cleanLitLatEst((int)row.getCell((short) 0).getNumericCellValue() + ""); 
	        	  }
	        	  String sort =  Converter.cleanLitLatEst(row.getCell((short) 1).getStringCellValue());
		        	  Vector vv = new Vector();
		        	  if(shops.get(sort) != null){
		        		  vv = (Vector)shops.get(sort);
		        	  }
		        	  vv.addElement(custid);
	        	  shops.put(sort, vv);
				}catch(Exception k){
					//k.printStackTrace();
				}
				
			}
			
			
			
			
		//	System.out.println("shops " + shops.toString());
			
		
			
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

		//	 System.out.println(groups.toString());

			 DataFormatter formatter = new DataFormatter();
				
				for (int i = 7; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String prodID = formatter.formatCellValue(row.getCell(1));

				if (row.getCell((short) 1) != null) {

					try {
						prodID = (int) (row.getCell((short) 1)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						try{
							prodID = (row.getCell((short) 1).getStringCellValue());
						}catch(Exception y){
							
						}
					}
					
					System.out.println(prodID + " " + i);
					  if(!prodID.equals("0") && !prodID.equals("")){
                  	
					// Siin l�heme reasisse rokkima ja vaatama kas se toode
					// l�heb kaasa
					for (int a = 1; a < row.getLastCellNum(); a++) {

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

							if (go != null && 
									(go.trim().toLowerCase().equals("x") 
									|| go.trim().toLowerCase().equals("1"))) {
                                
								 

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
									clGroup = clGroup.trim();
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
								if(clGroup.equals("A02") && "CUM009".equals(prodID.trim())) {
									System.out.println(go + " ss " + a +" " +mline.getStringForFixedAssortment() + " " + sheet.getLastRowNum());
								}
							}
							
							
							if(clGroup.equals("A02") && "CUM009".equals(prodID.trim())) {
								System.out.println(go + " " + a +" " +mline.getStringForFixedAssortment()+ " " + sheet.getLastRowNum());
							}
						}
					}
					   
                     }
				}
			}
			//Converter.sendContractJson(tm, rcompanyx,user,pass);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		// System.exit(0);

		return result;
	}

	@Override
	public String getProductSortingData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/
