package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
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
 * @author Tarmo Tubro''
 * @version 1.0
 */

public class AxaJNGLATDBDriver extends AbstractDBDriver  implements Manager, Promo, BulkUpdater, Discounter, PictureMaker, FixReporter {
  public AxaJNGLATDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    
    } catch (Exception e){
       parent.showError(e);
    }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    instance = getParent().getProps().getProperty("company");
  if(instance == null) {
	  instance = "";
     }
  }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String instance = null;
  
  public TreeMap getDealers() {
		return null;
	}
 
  /**
   *
   */
  public void connectionUp(){
  }
	public Vector getLastThreeData() {
		Vector result = new Vector();
      
		if(instance.equals("shell")) {
    			try {
    	    		String sq3 = "select * from getlastteninvoices" ;
    						
    						Connection con3 = getNewConnection();
    						Statement st3 = con3.createStatement();
    						ResultSet rs3 = st3.executeQuery(sq3);
    						while (rs3.next()) {
    							MatrixLine ml = new MatrixLine();
    							ml.setCustID(rs3.getString("klient").trim());
    							ml.setStartDate(rs3.getString("kuup").substring(5,10));
    							ml.setProdID(rs3.getString("toode").trim());
    							ml.setPrice(rs3.getDouble("kogus"));
    							double kmin = rs3.getDouble("kogusmin");
    							
    							ml.setMinAm(kmin);
    							ml.setLineinfo(rs3.getString("doknr").trim());
    							
    							int color = rs3.getInt("color");
    							if(color == 1) {
    								ml.setCliGroup("green");
    							}
    	                        
    							if(kmin < 0) {
    								ml.setCliGroup("red");
    							}
    						    
    							result.add(ml);
    						}
    						rs3.close();
    						st3.close();
    						con3.close();
    				
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			}

    			return result;
    	

	}

  public String getSaveDebtInfo() {
   	return null;
   }


	@Override
	public void PictureMaker() {
		makePictureCat();
		 System.exit(0);
		
	}
	
	  private void makePictureCat(){
	  	    
	    	
	    	String pictssou = parent.getProps().getProperty("pict.dir");
	  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	  	   
	  	 try {
   	  
        File f = new File(pictspda +"list.txt");
        if (f.exists())f.delete();
        //Kataloogi sisude kustutamine
    	 	 File res2 = new File (pictspda);
	           //  String[] ff2 = res2.list();
	         	/* for(int i = 0;ff2.length>i;i++){
	             	(new File ( pictspda + ff2[i])).delete(); 
	             }*/
	        
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	    	   	String[] ff2 = res2.list();
	             
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

  
  
	public Vector getFixedAssortmentData() {
		
		 Vector lines = new Vector();
		try {
			
			
			String cmd = "select * from getfixedassortmentdata";
			//Vaata kettide koodid 
			//'511','109' ,'791'
			//Max,rim.mego
			System.out.println(cmd);

			Connection con = getNewConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				
				String clGroup = " ";
				String prodID = " ";
				MatrixLine mline = new MatrixLine();

				clGroup = rs.getString("grupp");
				prodID = rs.getString("toode");

				if (clGroup != null && clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}

				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				
				lines.add(mline);

			}

			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
		System.out.println("Mlines fixed " + lines.size());
		
   return lines;
}

  public Vector getPriceMatrixData() {
          Vector lines = new Vector();
         String sql = " select  * , datediff(day,fromdate,todate) as paev from getprices where (getdate() between fromdate-2 and todate) " + 
         		" or ( getdate() > fromdate-2 ) order by accountrelation,relation desc,itemrelation ";
        HashSet hh = new HashSet();
          try {

        	   Connection con = getNewConnection();
            	Statement stmt = con.createStatement();
                  ResultSet rs = stmt.executeQuery(sql);

                  while (rs.next()) {
                	  String prodID = rs.getString("itemrelation");
                	  String custID = rs.getString("accountcode");
                          String custGR = rs.getString("accountrelation");
                         
                      
                          String algus = rs.getString("fromdate");
                          String lopp = rs.getString("todate");
                          double perc = rs.getDouble("percent1");
                          double price = rs.getDouble("amount");
                          int relation = rs.getInt("relation");
                          int paev = rs.getInt("paev");
                          MatrixLine mline = new MatrixLine();
                          
                          String check = "";
                          if (custID != null && custID.trim().length() > 1) {
                              mline.setCustID(custID.trim());
                              check = check + custID.trim();
                          }
                          
                          if (custGR != null && custGR.trim().length() > 1) {
                                  mline.setCliGroup(custGR.trim());
                                  check = check + custGR.trim();
                          }
                       
                          if (prodID != null && prodID.trim().length() > 1) {
                                  mline.setProdID(prodID.trim());
                                  check = check + prodID.trim();
                          }
                          if (perc != 0) {
                              mline.setDiscount(perc);
                          }

                          if (price != 0) {
                                  mline.setPrice(price);
                          }
                          if((paev > 1 && paev < 90) || relation == 8) {
                        	    
                                 if (lopp != null && lopp.trim().length() > 0  && !lopp.trim().contains("1900")) {
                                     mline.setEndDate(lopp.trim().substring(0,10));
                                     // panin sisse et poleks algust kui pole lõppu .
                                     if (algus != null && algus.trim().length() > 0 && !algus.trim().contains("1900")) {
                                         mline.setStartDate(algus.trim().substring(0,10));
                                     }
                                 } 
                          }
                     
                          if(!hh.contains(check)){
                        	  lines.add(mline);  
                          }
                        
                          hh.add(check);
                  }
                  rs.close();
                  stmt.close();
                  con.close();
          } catch (SQLException ex) {
          ex.printStackTrace();
          }
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
  
  private HashMap kogused = null;
  private HashMap kogusedStr = null;

  public String getSaveStockInfo(){
	  
	  kogused = new HashMap();
	  kogusedStr = new HashMap();

	  StringBuffer products = new StringBuffer();

	//  if(instance.equals("shell")) {
	  	
	  	  String  cmd = " select l.*,OTC from GETSTOCK l inner join getproducts t on t.toode=l.kood ";
	    

	         if (isDebug()) System.out.println(cmd);
	         try {

	      	 Connection con = getNewConnection();
	      	 Statement st = con.createStatement();
	           ResultSet rs = st.executeQuery(cmd);

	           while (rs.next()) {

	             String kood = rs.getString("kood").trim();
	             double seis = rs.getDouble("stock");
	             String otc = rs.getString("OTC");
	        
	             
	             if(seis > 0){
	          	   
	          	   
	          	   if(instance.equals("shell")) {
	          		   if(otc != null && otc.trim().equals("SHELL")) {
	    	        		  products.append(kood +  ";");
	    	                   products.append(" ;");
	    	                   products.append(getParent().getNF().format(seis) +  "\n"); 
	    	        	  }
	    	        	
	    	        }else {
	    	        	 if(otc == null || !otc.trim().equals("SHELL")) {
	    	        	  products.append(kood +  ";");
	                    products.append(" ;");
	                    products.append(getParent().getNF().format(seis) +  "\n"); 
	    	        	 }
	    	        	
	    	        }
	          	        if(kogused.get(kood) != null){
	                  	   double vahe = Double.parseDouble((String) kogused.get(kood));
	                  	   kogused.put(kood, (vahe+ seis) + "");
	                     }else{
	                  	   kogused.put(kood, seis + ""); 
	                     }
	                    
	             }
	             }
	             rs.close();
	             st.close();
	             con.close();
	             
	         } catch (Exception e) {
	             getParent().showError(e);
	         }
	      
	   /* } else {
	  	
	     	try{
	  		String file2 = "stockref.txt";
	  		File ff = new File(getExcelDir() +  file2);
	  		if(ff.exists()){		
	  			FileReader fr = new FileReader(getExcelDir()  + file2);
	  		 BufferedReader reader = new BufferedReader(fr);
	  	 String line = null;
	  		while ((line = reader.readLine()) != null) {
	  			StringTokenizer st = new StringTokenizer(line, ";");
	  			if (st.hasMoreTokens()) {
	  				String kood = st.nextToken().trim();
	  					
	  						while (st.hasMoreTokens()) {
	  							Double seis = Double.parseDouble(st.nextToken().trim());
	  							if(0 < seis) {
	  							  products.append(kood +  ";");
	  			                  products.append(" ;");
	  			                  products.append(getParent().getNF().format(seis) +  "\n"); 
	  						
	  						if(kogused.get(kood) != null){
	  		                	   double vahe = Double.parseDouble((String) kogused.get(kood));
	  		                	   kogused.put(kood, (vahe+ seis) + "");
	  		                   }else{
	  		                	   kogused.put(kood, seis + ""); 
	  		                   }
	  						}}
	  				}
	  			}
	  		}
	  		
	  	
	  	}catch(Exception e){
	  	e.printStackTrace();	
	  	}
	  	
	  }*/

	         return products.toString();
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

       return null;
     }

  
  
  
  
  public TreeMap getAgents(){
    String cmd = "select * from getagent";
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
    
    try {
    
        Connection  con = getNewConnection();
        Statement   stmt = con.createStatement();
    	ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("kood").trim());
          String name = rs.getString("nimi").trim() ;
                   agent.setName(name);
          agent.setIsPilot(true);
          agents1.put(agent.getID(), agent);
          agents.put(name, agent);
          ags.add(agent.getID());
        }
        rs.close();
        stmt.close();
        con.close();
    } catch (Exception e){
        getParent().showError(e);
      }
   Agent aa = new Agent ("HOUSE1");
    aa.setName("HOUSE1");
    agents.put("HOUSE1", aa);
    
    
    return agents;
  }
  
  HashSet ags = new HashSet();
  
  private   HashMap logMap = new HashMap();
  
  
  
  private TreeMap<String,String> getVeod(){
	  	TreeMap<String,String> res = new TreeMap<String,String>();
	  	
	  	File f = new File(this.getExcelDir() + "piegades dienas.xlsx");
			try {
				
				 DataFormatter formatter = new DataFormatter();
				
				
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheetAt(0);
				XSSFRow row = null;
				
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					
					String custID = formatter.formatCellValue(row.getCell((short) 1));
					String vedu = "";


	                       if(custID != null && custID.trim().length() > 0){
	                    	   
	                           for (int a = 7; a < 15; a++) {
	                        	   
	                        	   String asi = formatter.formatCellValue(row.getCell((short) a));
	                        	   
                                     if(a == 7 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "1";
                                     }
                                     if(a == 8 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "2";
                                     }
                                     
                                     if(a == 9 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "3";
                                     }
                                     
                                     if(a == 10 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "4";
                                     }
                                     
                                     if(a == 11 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "5";
                                     }
                                     
                                     if(a == 12 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "6";
                                     }
                                     
                                     if(a == 13 && asi != null && asi.trim().length() > 1 ) {
                                    	 vedu = vedu + "7";
                                     }
                                     
                                     if(vedu.length() > 0) {
                                    	  if(a == 14 && asi != null && asi.trim().startsWith("24") ) {
                                         	 vedu = vedu + " -24h"  ;
                                          }
                                          if(a == 14 && asi != null && asi.trim().startsWith("48") ) {
                                         	 vedu = vedu + " -48h"  ;
                                          }
                                     }
                                   
	                           	   
							    }    
	                          }
	                       
	                       if(vedu.trim().length() > 0) {
	                    	res.put(custID, vedu)   ;
	                    	res.put("0" + custID, vedu)   ;
	                       }
	                    }
				
				
			}catch(Exception r){
				r.printStackTrace();	
				}
				System.out.println("Vedu size  " + res.size());
	 // 	System.exit(0);
	  	return res;
	  }
  
  
  private HashMap<String,Vector<String>> getMerc(){
	  HashMap<String,Vector<String>> res = new HashMap<String,Vector<String>>();
	  	
	  	File f = new File(this.getExcelDir() + "ClientAgent.xlsx");
			try {
				
				 DataFormatter formatter = new DataFormatter();
				
				
				FileInputStream ff = new FileInputStream(f);
				XSSFWorkbook workbook = new XSSFWorkbook((ff));
				XSSFSheet sheet = workbook.getSheetAt(0);
				XSSFRow row = null;
				
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					
					String custID = formatter.formatCellValue(row.getCell((short) 0));
					String ag = formatter.formatCellValue(row.getCell((short) 1));
	                       
	                       if(ag.trim().length() > 0) {
	                    	   if(ag.startsWith("ag")) {
	                    		   ag = ag.substring(2);
	                    	   }
	                    	   Vector<String> vec  = new Vector<String>();
	                           if(res.get(custID) != null) {
	                           	vec = res.get(custID);
	                           }
	                           if(!vec.contains(ag)) {
	                           	  vec.add(ag);
	                           }
	                           res.put(custID, vec);
	                       }
	                    }
				
			    } catch(Exception r){
				  r.printStackTrace();	
				}
				System.out.println("Merc size  " + res.size());
	 // 	System.exit(0);
	  	return res;
	  }
  
  
  public TreeMap getCustomerTopProds(TreeMap custTypes, TreeMap mustCon){
	    TreeMap custProds = new TreeMap();
	 
	
	    
	    String cmd = "select KLIENT ,TOODE  from getcustomerproductlist where KUUPAEV > getdate()-90 and len(KLIENT) > 0 " + 
	    		" GROUP BY klient, toode ORDER BY klient,toode" ;

	    
	    

	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	  
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        if(custTypes != null && custTypes.get(kliendikood) != null) {
	        	ArrayList type = (ArrayList)custTypes.get(kliendikood);
	        	if(mustCon.get(type.get(0)) != null) {
	        		ArrayList lines = (ArrayList)mustCon.get(type.get(0));
	        		if(lines != null && lines.contains(tootekood)) {
	        			ArrayList arr = new ArrayList();
	        			if(custProds.get(kliendikood) != null) {
	        				arr =  (ArrayList)custProds.get(kliendikood);
	        			}
	        			if(!arr.contains(tootekood)) {
	        				arr.add(tootekood);
	        				custProds.put(kliendikood, arr);
	        			}
	        		
	        		}
	        	}
	        }
	      }
	      
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
          e.printStackTrace();
	    }
	    
	 //   System.out.println(custProds.toString());
	    
	    
	    TreeMap res = new TreeMap();
	    
	  Iterator itera =   custProds.keySet().iterator();
	  while(itera.hasNext()) {
		  String kkood = (String)itera.next();
		  double cpsize =  ((ArrayList)custProds.get(kkood)).size();
		  
		  ArrayList type = (ArrayList)custTypes.get(kkood);
		  if(type != null && mustCon.get(type.get(0)) != null ) {
			  double arrs = ((ArrayList) mustCon.get(type.get(0))).size();
			  res.put(kkood,cpsize/arrs);
			  
			 // System.out.println(kkood + " " + cpsize + " " + arrs);
		  }
		  
		 
	  }
	    
	//   System.out.println(res.toString());
	    return res;
	  }

  
  private TreeMap<String,ArrayList<Double>> agentMust = null;
  
  
  public TreeMap getCustomers(Agent agent){
	  
	  TreeMap custTop = new TreeMap();
	  
	  agentMust = new TreeMap<String,ArrayList<Double>>();
	  
	  if(!instance.equals("shell")) {
		  // teeme Must toodete mapi ja protsendid 
		  TreeMap mustCon = new TreeMap();
			Converter.getContractsSuggByCustOrType(mustCon,rcompany,user,pass,true) ;	 
		    TreeMap custTypes =  Converter.getCustDimsLoading("type");
		    custTop =   getCustomerTopProds(custTypes , mustCon);
     }
	  
	  System.out.println("custTop " + custTop.size() + " " + custTop.toString());
	  

	  double minordsum = 0;
      if(getParent().getProps().getProperty("minordsum") != null){
      	try{
      		minordsum = new Double(getParent().getProps().getProperty("minordsum")).doubleValue();
      	}catch (Exception e){
      		e.printStackTrace();
      	}
      }
	  
	  try{
		   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
	   } catch(Exception e) {

	   }
	  
	  
	  HashMap salesres = new HashMap();
	  
	  
	   try {
		   
		   String salessql = "  select  klient , trim(str(year(kuupaev))) + case when len(trim(str(month(kuupaev)))) = 1 then '0' +trim(str(month(kuupaev))) else  trim(str(month(kuupaev))) end as kuu , sum(kogus*lprice) as summa from getcustomerproductlist where\r\n" + 
		   		"		   kuupaev > getdate()-200  and len(trim(klient)) >1 group by klient ,  trim(str(year(kuupaev))) + case when len(trim(str(month(kuupaev)))) = 1 then '0' +trim(str(month(kuupaev))) else  trim(str(month(kuupaev))) end \r\n" + 
		   		"		    order by klient ,  trim(str(year(kuupaev)))  + case when len(trim(str(month(kuupaev)))) = 1 then '0' +trim(str(month(kuupaev))) else  trim(str(month(kuupaev))) end";
		   
		     Connection con = getNewConnection();
		     	Statement stmt =  con.createStatement();
		          ResultSet rs1 = stmt.executeQuery(salessql);
		          
		          while(rs1.next()){
		          	String klient = rs1.getString("klient").trim();
		          	String kuu = rs1.getString("kuu");
		          	int summa = (int)rs1.getDouble("summa");
		            if(salesres.get(klient) == null){
		          	  salesres.put(klient,"   " + kuu + " : " + summa + " €" );
		            }else{
		          	  String asi = (String)salesres.get(klient);     	  
		          	  salesres.put(klient,"   " + kuu + " : " + summa +"€|" + asi);
		            }
		          }
		         rs1.close();
		         stmt.close();
		         con.close();
		     // 	System.out.println(salesres.toString());   
	   }catch(Exception e) {
		   
	   }

	  
	  
      String cmd = "select * from getCustomers";
      String cmd2 = "select * from getCustomers";
      
         TreeMap<String,String> veod = new TreeMap<String,String>();
          if(!instance.equals("shell")) {
        	 veod =  getVeod();
           }
                
        //System.out.println("kliendid alustan, veod " + veod.size());
        
        TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();
        HashMap custMap2 = new HashMap();
        HashMap<String,Vector<String>> custAgents = new HashMap<String,Vector<String>>();
        HashMap<String,Vector<String>> custMercs = getMerc();
        
        try {
        	Connection  con = getNewConnection();
            Statement   stmt = con.createStatement();        
            ResultSet rs = stmt.executeQuery(cmd2);
            
            while (rs.next()) {
                String klient = rs.getString("kood").trim();
                String ag = rs.getString("agent").trim();
                Vector<String> vec  = new Vector<String>();
                if(custAgents.get(klient) != null) {
                	vec = custAgents.get(klient);
                }
                if(!vec.contains(ag)) {
                	  vec.add(ag);
                }
                custAgents.put(klient, vec);
            }
            rs.close();
            
            rs = stmt.executeQuery(cmd);
            while (rs.next()) {
                String nimi = rs.getString("nimi").trim();
              Customer cust = new Customer(rs.getString("kood").trim());
          
              cust.restName = nimi;

              if(!nimi.endsWith("#")){
                   // System.out.println("nimi1 " + nimi);
                	nimi = nimi.replaceAll("Z/S","");
                	nimi = nimi.replaceAll(" SIA","");
                	nimi = nimi.replaceAll("AIZKRAUKLES","AIZ.");
                	nimi = nimi.replaceAll("NAFTA ","NA. ");
                	nimi = nimi.replaceAll("SAUL�TE","");
                	nimi = nimi.replaceAll("''","\"");
                	nimi = nimi.replaceAll("A/S","");
                	nimi = nimi.replaceAll("VIDZEMES ","VID ");
                	nimi = nimi.replaceAll(" DUS","");
                	nimi = nimi.replaceAll("VESEKS Sabiedr�ba ar Ierobe�otu Atbild�bu","VESEKS SIA");
                	nimi = nimi.replaceAll("TIRDZNIEC�BAS","TIRD.");
                	nimi = nimi.replaceAll("SENTOR FARM APTIEKAS","SENTOR APT.");
                	nimi = nimi.replaceAll("SAULES APTIEKU P�RVALDE","SAULES APT.");
                	nimi = nimi.replaceAll("KOMERCIJA","KOM");
                 	nimi = nimi.replaceAll("LATVIA ","LAT ");
                 	nimi = nimi.replaceAll("R�GAS","R�.");
                 	nimi = nimi.replaceAll("R�GA","R�");
                 	nimi = nimi.replaceAll("R�ZEKNES","R�Z");
                 	nimi = nimi.replaceAll("PRESES APVIEN�BA","PRESES APVI.");
                 	nimi = nimi.replaceAll("CENTRS ","CEN ");
                 	nimi = nimi.replaceAll("PARTNERI","PAR");
                 	nimi = nimi.replaceAll(" BALTIJA","");
                 	nimi = nimi.replaceAll("SERVISS","SER");
                 	nimi = nimi.replaceAll("APTIEKA","APT");
                 	nimi = nimi.replaceAll("P�RTIKA","P�R");
                 	nimi = nimi.replaceAll("LATVIJAS ","LAT ");
                 	nimi = nimi.replaceAll("JELGAVAS","JEL.");
                 	nimi = nimi.replaceAll("PHARMA","PH");
                 	nimi = nimi.replaceAll("BALTIC RESTAURANTS LATVIA","BalResLat");

                	nimi = nimi.replaceAll("DOUBLE COFFEE","DouCof");
                	nimi = nimi.replaceAll("AIZKRAUKLES SABIEDR�BA","AIZKRAUKLES SAB.");

            
                String aadress = rs.getString("address");
                if(aadress != null && aadress.trim().length()>0){
                    cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Latvia"));
                    nimi = nimi + " " +aadress.trim()+ " " + cust.getID();
                }
               // System.out.println("nimi2 " + nimi);
                cust.setName(Converter.enCodeEst(nimi));

                String ale = rs.getString("discount");
                if (ale != null && ale.trim().length()>0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }

                String visit=rs.getString("visit");
                if (visit!=null && visit.trim().length()>0){
                  // cust.setVisit(visit.trim().toUpperCase());
                }
                
                if(minordsum != 0){
                	cust.setMinsum(minordsum);
                }

                String info = rs.getString("info");
                if(info != null && info.trim().length()>0){
                	cust.setPersonalData(Converter.enCodeEst(info.trim()));
                }

                String agentc = rs.getString("agent");
              
                if (agentc != null && agents1.get(agentc.trim()) != null){
    				Agent aa = (Agent)agents1.get(agentc.trim());
    			
    				Vector vec = aa.getDubbler();
    				if(vec!= null && vec.size() >0){
    					Enumeration en = vec.elements();
    					while(en.hasMoreElements()){
    						String ag = (String) en.nextElement();
    						cust.addAgent(new Agent(ag));
    					}
    				}
    			}
                
               
                
            
                String aginfo = rs.getString("aginfo");
                if(aginfo != null && aginfo.trim().length()>0){
                	
                	StringTokenizer st = new StringTokenizer(aginfo.trim(),";");
                	while(st.hasMoreTokens()){
                		String deal = st.nextToken();
                		StringTokenizer de = new StringTokenizer(deal.trim(),"|");
                		while (de.hasMoreTokens()){
               
                			try{
                				String paev = de.nextToken();
                    			String nadal = de.nextToken();
                    			String age = de.nextToken();
                    			paev = paev.replaceAll("1", "E").replaceAll("2", "T").replaceAll("3", "K").replaceAll("4", "N").replaceAll("5", "R").replaceAll("6", "L").replaceAll("7", "P");
                    			if(nadal.equals("2")){
                    				nadal = "1";
                    			}else if (nadal.equals("1")){
                    				nadal = "2";
                    			}else if (nadal.equals("0")){
                    				nadal = "";
                    			}
                    			paev = paev + nadal;
                    			
                    			Agent aaa = new Agent(age);
                    			if(!paev.equals("0")) {
                    				aaa.setVisit(paev);
                    			}
                    			
                    			if(!age.contains("|") )
                    			cust.addAgent(aaa);
                    				
                			}catch (Exception ee){
                				//ee.printStackTrace();
                			//	System.out.println("Probleemne klient " + cust.getID() );		
                			}
                
                			
                		}
                	}
                	
                    if(aginfo.trim().indexOf("SH") == -1){
                        cust.addAgent(new Agent("HOUSE1")); 
                    } 
                }
                
              
                if(custAgents.get(cust.getID()) != null) {
                	Vector<String> ags = custAgents.get(cust.getID());
                	for (String ag : ags) {
                		cust.addAgent(new Agent(ag));
                		if(custTop.get(cust.getID()) != null) {
                			 Double vv = Double.parseDouble( custTop.get(cust.getID()) + "");
                			 if(agentMust.get(ag) != null) {
                				 ArrayList<Double> vv1 = agentMust.get(ag);
                				 vv1.add(vv);
                				 this.agentMust.put(ag,  vv1 );
                			 } else {
                				 ArrayList<Double> vv1 = new ArrayList<Double>();
                				 vv1.add(vv);
                				 this.agentMust.put(ag, vv1 );	 
                			 }
                			
                		}
                	  
                		//System.out.println("Lisan Agenti " + ag +" " + cust.getID());
                		 if(ag.trim().indexOf("SH") == -1 && !cust.hasAgent("HOUSE1")){
                             cust.addAgent(new Agent("HOUSE1")); 
                         } 
                	}
                }
                
                if(custMercs.get(cust.getID()) != null) {
                	Vector<String> ags = custMercs.get(cust.getID());
                	for (String ag : ags) {
                		cust.addAgent(new Agent(ag));
                	}
                }

           
                if (cust.getID().equals("0100000")) {
                	agentc = "aia";

                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        if(ags.contains(a.getID().trim()))
                        cust.addAgent(a);
                        
                        }
                      cust.addAgent(new Agent("HOUSE"));  
                      cust.addAgent(new Agent("HOUSE1"));
                      cust.addAgent(new Agent("HOUSE3"));
                      }

         String payer = rs.getString("payer");
         if (payer != null && payer.trim().length()>0){
            cust.setPayer(payer.trim());
         }

         String makset = rs.getString("makse");
         if (makset != null && makset.trim().length()>0){
         	if(makset.trim().equals("0")){
         		makset = "-1";
         	}
            cust.setPaymTerm(makset.trim());
         }


         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length()>0){
         	if(grupp.trim().equals("01")){
         		grupp = "A";
         	}
        	if(grupp.trim().equals("02")){
         		grupp = "B";
         	}
        	if(grupp.trim().equals("03") || grupp.trim().equals("04") ||
        			grupp.trim().equals("05")  || grupp.trim().equals("08")
					 || grupp.trim().equals("09")
					 || grupp.trim().equals("14")){
         		grupp = "C";
         	}
        	if(grupp.trim().equals("06")){
         		grupp = "D";
         	}
            cust.setCustType(Converter.cleanLitLatEst(grupp.trim()));
            Vector vv = new Vector();
            vv.add(grupp.trim());
            cust.setFixSortCode(vv);
         }

         String alegr = rs.getString("klale");
         if(alegr != null && alegr.trim().length()>0){
         	cust.setGroup(Converter.cleanLitLatEst(alegr.trim()));

         }
     	 // cust.setSupervisorFilled(true); //vaja  nagu oleks k�igi juures tehtud 

         
         double minsum = rs.getDouble("minsum");
          if(minsum > 0){
        	  cust.setMinsum(minsum);
          }
          if(minordsum == -1){
        	  cust.setMinsum(-1);
          }
          
          String logsol = rs.getString("logsol");
          if(logsol != null){
        	  logMap.put(logsol, cust.getID());
          }
          String teade = rs.getString("eucerin");
          if(teade != null && teade.trim().length() != 0){
        	  if(teade.equals("1")){
        		  cust.setInfo("Shell netirgot !||");
        		  cust.setCity("Shell netirgot !");
        	  }
          }
          
          String lic  = rs.getString("lic");
          if(lic != null && lic.trim().length() != 0){
        	  if(lic.equals("1")){
        		  cust.setLicence("OTC");
        		  //kllendile v�ib m��a
        		  
        		  // 2 ei v�i 
        		  // tyhi siis ka v�ib 
        	  }
        	  
        	  if(lic.equals("3")){
        		  cust.setLicence("ALC");
        	  }
        	  
          }
      
        	if(cust.getID().equals("0100000")){
        		  cust.setLicence("OTC");
        	}
        	
        	   String klgrupp  = rs.getString("klgrupp");
               if(klgrupp != null && klgrupp.trim().length() != 0){
              	 cust.setBlockingDealer(klgrupp.trim());
               }

        	
        	
          
          
             String rstreet  = rs.getString("rstreet");
             if(rstreet != null && rstreet.trim().length() != 0){
            	 cust.restStreet = rstreet.trim();
             }
             
             String rcity  = rs.getString("rcity");
             if(rcity != null && rcity.trim().length() != 0){
            	 cust.restCity = rcity.trim();
            	 cust.setCountry("Latvia");
             }
             
             String rkategooria  = rs.getString("rkategooria");
             if(rkategooria != null && rkategooria.trim().length() != 0){
            	 cust.category = rkategooria.trim() ;
             }
           
             String rchain  = rs.getString("rchain");
             if(rchain != null && rchain.trim().length() != 0){
            	 cust.setChain(rchain.trim());
             }

             String vedu  = rs.getString("vedu");
             vedu = veod.get(cust.getID());
             if(vedu != null && vedu.trim().length() != 0){
            	 if(cust.getCity() != null && cust.getCity().trim().length() > 0){
            		 cust.setCity( cust.getCity() + "|Piegades diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }else{
            		 cust.setCity("Piegades diena - " +  vedu.replaceAll(";", "-")); 	 
            	 }
                 cust.setCity(Converter.enCodeEst(cust.getCity()));
             }
             
             try {
            	 
            
             if(custTop.get(cust.getID()) != null) {
            	
            	 Double vv = Double.parseDouble( custTop.get(cust.getID()) + "");
            	// System.out.println("oioi " + vv);
            	 if(vv != null && vv > 0) {
            		 if(cust.getCity() != null && cust.getCity().trim().length() > 0){
                		 cust.setCity( cust.getCity() + "|Must prod Percent - " +  getNF().format(vv*100) + "%"); 	 
                	 }else{
                		 cust.setCity("Must prod Percent - " +  getNF().format(vv*100) + "%"); 	 
                	 }  
            	 }
            	
             }
             }catch(Exception e) {
            	 e.printStackTrace();
             }
            
             
             if(salesres.get(cust.getID()) != null){
               	cust.setInfo(Converter.enCodeEst("CHART sales Eur. :|Month & year|Sales |" )+ salesres.get(cust.getID()) );
               }

             
          
          if(cust.getID().toLowerCase().indexOf("pr")==-1 && agentc != null && agentc.toLowerCase().indexOf("pr")==-1){
              customers.put(cust.getName(), cust);
              custMap.put(cust.getPayer(), cust);
              custMap2.put(cust.getID(), cust);
        	  
               }
              }
            }

            rs.close();
            
            
            String sql3 = "select klient , sum(summa) as summa from getdebt where kuupaev <= getdate() group by klient ";
          //  System.out.println(sql3);
                rs = stmt.executeQuery(sql3);
                while(rs.next()){
                
            Customer cc = (Customer)custMap2.get(rs.getString("klient"));
           	if (cc != null){
               double debt = Double.parseDouble(rs.getString("summa").trim());
               if(debt != 0){
               	cc.setDebt(debt);
               }
           	 }
        	
                }
            
            sql3 = "select * from getdebt ";
        //    System.out.println(sql3);
                rs = stmt.executeQuery(sql3);
                
               while(rs.next()){
            	     Debt debt = new Debt();
                     debt.setCustID(rs.getString("klient"));
                     debt.setDoknr(rs.getString("number").trim());
                     debt.setDuedate(rs.getDate("kuupaev"));
                     debt.setSum(round(rs.getDouble("summa"),2));
                     debt.setDue(rs.getString("yle"));
                     debt.setDate(rs.getDate("algp"));
                     this.debt.addElement(debt);
                 }
               
        rs.close();
        stmt.close();
        con.close(); 
           
               
        } catch (Exception e) {
            getParent().showError(e);
        }
        
        
        
        System.out.println("kliendid l�petan ");
        return customers;
    }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

  
  
     public String getDistribution() {
        return null;
      }


  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " select * from GETPRODGROUPS";
    	
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
      String liik = rs.getString("grupa").trim();
      String nimi = Converter.enCodeEst(rs.getString("nimetus2").trim());
      if(instance.equals("shell")) {
    	  if(liik.startsWith("74")) {
    	        tm.put(liik, nimi);
    	    }
      }else {
    	  if(!liik.startsWith("74")) {
    		  
    		  nimi = nimi.replaceAll("Ajax ", "Aja ");
    		  nimi = nimi.replaceAll("Balsnack ", "Bal ");
    		  nimi = nimi.replaceAll("Colgate zobu ", "Col Z ");
    		  nimi = nimi.replaceAll("Colgate ", "Col ");
    		  nimi = nimi.replaceAll("Colgate-Palmolive", "Col Cit");
    		  nimi = nimi.replaceAll("Libresse ", "Lib ");
    		  
    		  nimi = nimi.replaceAll("Nivea deo - ", "Niv D ");
    		  nimi = nimi.replaceAll("Nivea virie11120xxu ", "Niv V ");
    		  nimi = nimi.replaceAll("Nivea ", "Niv ");;
    		  
    		  nimi = nimi.replaceAll("Palmolive ", "Pal ");
    		  nimi = nimi.replaceAll("Vileda ", "Vil ");
    		  nimi = nimi.replaceAll("Zewa ", "Zew ");
    		  
    		  
  	        tm.put(liik, nimi );
  	    }}  
      }
      
    rs.close();
    st.close();
    con.close();

    return tm;
}


  public TreeMap getProdGroupsSecond() throws SQLException {
  TreeMap   tm = new TreeMap();
      return tm;
  }

  public TreeMap getAgents1() {
		return agents1;
	}

  private TreeMap agents1 = new TreeMap();

  public TreeMap getProducts(Agent agent){

    String cmd = " select * from getproducts where nimi1 != ''";
    shell = new  HashSet<String>();
    getSaveStockInfo();
  
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {

    	Statement stmt =  this.getNewConnection().createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      while(rs.next()){
        String kood = rs.getString("toode").trim();
        Product prod = new Product(kood);
        String nimetus = rs.getString("nimi1").trim() ;
     nimetus =  Converter.enCodeEst(nimetus);
     prod.setPrice(rs.getDouble("myygihind"));
     String vat = rs.getString("vat");
     if(vat!=null && vat.trim().length()>0){
       prod.setVat(vat.trim());
     }

     if(kogused.get(kood) != null) {
    	 prod.setStock(Double.parseDouble(kogused.get(kood)+""));
     }else {
    	 prod.setStock(0);
     }

     String unit = rs.getString("unit");
     if(unit!=null && unit.trim().length()>0){
        prod.setUnit(unit.trim());
      }
  
        prod.setInCases(rs.getDouble("tykke"));
     
        double min = rs.getDouble("minimum");
        if(min > 1) {
        	prod.setMinAmountInUnits(min);
        }
        
        double opt = rs.getDouble("optimum");
        if(opt > 1) {
        	prod.setOptAmountInUnits(opt);
        }
        

        String otc = parent.make(rs.getString("otc"));  
        
        String grupp2 = rs.getString("grupp");     
        if(grupp2 != null && grupp2.trim().length()>0){
               prod.setGroup(grupp2);
            }


        String ean = rs.getString("ean");
        String add = "";
        if(ean != null && ean.trim().length()>0){
        prod.setEAN(ean.trim());
        if(ean.trim().length() > 4) {
        	//add = ean.substring(ean.length()-4);
        }
      }
     

      
      prod.setName(nimetus + add);
   
      
      if(kogusedStr.get(kood) != null){
      	prod.setPartii(prod.getPartii() +(String)kogusedStr.get(kood) );     	
      }    
      if(instance.equals("shell")) {
    	
    	  if(otc.equals("SHELL")) {
    		  products.put(nimetus, prod);
              prodHash.put(prod.getID(), prod);	 
              shell.add(prod.getID());
    	  }
    
      }else {
    	  if(!otc.equals("SHELL")) {
    		  products.put(nimetus, prod);
              prodHash.put(prod.getID(), prod); 	  
    	  }else {
    		  shell.add(prod.getID());
    	  }
    	 
      }
       

      }
      rs.close();
      
    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }

HashSet<String> shell = null;


  public Vector getCustomerProductsList(String agentCode){
	    Vector custProds = new Vector();
	 
	
	    
	    String cmd = "select KLIENT ,TOODE,max(KOGUS) as KOGUS ,KUUPAEV, max(DAYS30) as DAYS30,max(LPRICE) as LPRICE ,max(BILL) as BILL"
	    		+ " from getcustomerproductlist where KUUPAEV > getdate()-180 and len(KLIENT) > 0 \r\n" + 
	    		" GROUP BY klient,toode,kuupaev  ORDER BY klient,toode,kuupaev" ;

	    
	    

	    if (isDebug()) System.out.println(cmd);

	    try {

	    	Connection con = this.getNewConnection();
	    	Statement stmt =  con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	  
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("kogus");
	        String lastdate = rs.getString("kuupaev").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double hind = rs.getDouble("lprice");
	     	        String bill = rs.getString("bill");          
	        CustProd cp = new CustProd(kliendikood , tootekood, kogus ,0 ,0,lastdate,hind,0,0," ");
	           cp.setAbiKood(bill);

	        if(instance.equals("shell")) {
	        	 if( shell.contains(tootekood)){
	        		  custProds.add(cp);
	        	  }
	        	
	        }else {
	        	if( !shell.contains(tootekood)){
	        		 custProds.add(cp);	
	        	 }
	        	
	        }
	        
	        
	       
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
         e.printStackTrace();
	    }
	    return custProds;
	  }





  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


    public void setBulkSales(Map agents){
  this.connectionUp();
    
      
      
      String cmd =" SELECT *  FROM getAgentSales  ";

         if (isDebug()) System.out.println(cmd);
            try {
            	Connection con = this.getNewConnection();
                Statement stmt =  con.createStatement();
                ResultSet	      rs = stmt.executeQuery(cmd);
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
            
            
             if(agentMust != null) {
            	Iterator itera =  agentMust.keySet().iterator();
            	while(itera.hasNext()) {
            		String ag = (String) itera.next();
            		  Agent agent = (Agent)agents.get(ag);
                      if (agent != null){
                    	  double avg = 0;
                    	  ArrayList<Double> aa = agentMust.get(ag);
                    	  double size = aa.size();
                    	  double sum = 0;
                    	  for(Double x : aa) {
                    		  sum = sum + x;
                    	  }
                    	  
                        agent.setStart("Must Prods: " + getNF().format(sum/size*100) + "%");
                      }
            	}
             }
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
    return "AxaJNGLAT";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  public String getPilotDayData(Agent agent){



	       String sql2 =  "SELECT custname + ' '+ custaddress as klient , ordernum as arve "
	       		+ ",orderdate as kuupaev ,sumoforder as summa from GETORDERS  where managerid ='"+ agent.getID()+"'";

	    StringBuffer sb = new StringBuffer();
	    sb.append("\\n\\n Sanemtie pasutijumi :\n\n");
	   

	      try {
	    	  Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs2 = stmt.executeQuery(sql2);
	                  while (rs2.next()) {
	                  sb.append(rs2.getString("klient")  +
                       		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev") + "] " +
                       		getParent().getNF().format(rs2.getDouble("summa"))
                       	+" EUR "	+  "\n\n");
	                  
	                  }
	                  rs2.close();
	                  stmt.close();
	                  con.close();

	                  } catch (Exception e) {
	                      getParent().showError(e);
	                  }

    //    System.out.println(agent.getID() + " " + sb.toString());
	      return sb.toString();
	  }




       public NumberFormat getNF(){
         return NF;
       }

       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:axa";
  private   Vector debt = new Vector();
  private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();

	@Override
	public void FixReporter() {
		 try{
		    // tõmbame lepingud sisse 	
			 TreeMap contracts = new TreeMap();
		    	Converter.getContractsSuggByCustOrType(contracts,rcompany,user,pass,true) ;	 
		    	Converter.loadCustDimensions("type",rcompany,user,pass);
		     TreeMap custTypes =  Converter.getCustDimsLoading("type");
		    	
		     
		     
		    	System.exit(0);
		     }catch(Exception r){
		    	 r.printStackTrace();
		     }
		
	}
}
