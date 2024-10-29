package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ViaLaimaDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter {

	
	
	public ViaLaimaDBDriver(EksportPanel parent) {
		super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
        String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(databaseDriverName).newInstance();

        } catch (Exception e) {
            parent.showError(e);
        }
        user =  getParent().getProps().getProperty("rest.user");
        pass = getParent().getProps().getProperty("rest.pass");
        rcompany = getParent().getProps().getProperty("rest.company");
        rcompanyx = getParent().getProps().getProperty("rest.companyx");
      }

      String user =  null;
      String pass = null;
      String rcompany = null;
      String rcompanyx = null;
      
      
	public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		File f = new File(this.getExcelDir() + "Sortiments.xlsx");
		
		try {
			// System.out.println("rownum exists" + f.exists() );
				
			
	 if(f.exists()){
				
			
			FileInputStream ff = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook((ff));
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow row = null;
			
			
			
			XSSFSheet sheetc = workbook.getSheetAt(1);
		
			TreeMap shops = new TreeMap();
			for (int a = 0; a < sheetc.getLastRowNum(); a++) {
	           try {
	        	   row = sheetc.getRow(a);
	        	  String custid =  Converter.cleanLitLatEst(row.getCell((short) 0).getStringCellValue());
	        	  String sort =  Converter.cleanLitLatEst(row.getCell((short) 1).getStringCellValue());
		        	  Vector vv = new Vector();
		        	  if(shops.get(sort) != null){
		        		  vv = (Vector)shops.get(sort);
		        	  }
		        	  vv.addElement(custid);
	        	  shops.put(sort, vv);
				}catch(Exception k){
					k.printStackTrace();
				}
				
			}
			
			
			
			
			System.out.println("shops " + shops.size());
			
			
			this.getProducts(new Agent("xxx"));
				
			TreeMap groups = new TreeMap();
			
			row = sheet.getRow(1);
			// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

		//	 System.out.println("rownum " + row.getLastCellNum());
			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", Converter.cleanLitLatEst(row.getCell((short) a)
							.getStringCellValue()));
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

			 System.out.println("groups " + groups.size());

			for (int i = 3; i < sheet.getLastRowNum(); i++) {
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
					for (int a = 2; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
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

							if (go != null && go.equals("1")) {

						// System.out.println( a + " " + prodID + " " +
							//	 clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0 && (prodID.trim()) != null) {
									mline.setProdID("" + (prodID.trim()));
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
		}
		} catch (Exception e) {
			e.printStackTrace();
		}

		 System.out.println("fixed Suurus ");

		return result;

	}


    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getPilotDayData(Agent agent) {
		return null;
	}
    private Vector matrixlines = new Vector();
	
    public Vector getPriceMatrixData() {
      System.out.println("Hinnamaatrixi suurus " + matrixlines.size() );
 	return matrixlines;
    }

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public void setBulkSales(Map agents) {

	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}
	 public String getProductSortingData(){
	   	return null;
	   }
	public String getAssortmentStr(Agent agent) {
		return null;
	}
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	public Vector getDebt(String agentCode) {
		Vector list = new Vector();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("debt") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1 ) {
				file = names[i];
			}
		}
		if (file != null) {

			try {
				File fil = new File(getDir() + file);

				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					Debt debt = new Debt();
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						debt.setCustID(id);
						if (st.hasMoreTokens()) {
							String arve = st.nextToken().trim();
							debt.setDoknr(arve);
						}
						if (st.hasMoreTokens()) {
							String kuup = st.nextToken().trim();
							debt.setDuedate(getDate(kuup));
						}
						if (st.hasMoreTokens()) {
							String sum = st.nextToken().trim();
							double summa = Double.parseDouble(sum);
							debt.setSum(summa);
						}
						if (st.hasMoreTokens()) {
							String yle = st.nextToken().trim();
							debt.setDue(yle);
						}

						/*   if (st.hasMoreTokens()){
						 String prodGroup = st.nextToken().trim();
						 agent.setProdGroup(prodGroup);
						 }*/
							list.addElement(debt);
						
					}
				}

				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public String getDistribution() {
		return null;
	}

	public String getSaveStockInfo() {
		return null;
	}

	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(5, 7));
		int DAY = Integer.parseInt(kuup.substring(8, 10));
		cal.set(YEAR, MONTH -1, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}

	public void connectionDown() {
	}

	public void connectionUp() {
	}

	public TreeMap getProdGroups() throws SQLException {
		TreeMap tm = new TreeMap();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
		}
		
		String filemulti = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filemulti = names[i];
			}
		}
		
		
		String filemulti2 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filemulti2 = names[i];
			}
		}
		
		String filemulti3 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodefourth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefourth")) != -1) {
				filemulti3 = names[i];
			}
		}
		//System.out.println("filemulti3 " + filemulti3);
		
		String filemulti4 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodefifth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefifth")) != -1) {
				filemulti4 = names[i];
			}
		}
		
		String filemulti5 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tootegrupp") != -1 && getParent().getProps().getProperty("stockcodesixt") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesixt")) != -1) {
				filemulti5 = names[i];
			}
		}
	//	System.out.println(file + " " + filemulti);
		if (file != null) {

			try {
				File fil = new File(getDir() + file);

				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				String id = null;
				String name = null;
				while ((line = reader.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);

					if (st.hasMoreTokens()) {
						id = st.nextToken().trim();
					}
					if (st.hasMoreTokens()) {
						name = st.nextToken().trim();
					}
					try{
						tm.put(id, name);	
					}catch(Exception e){
						
					}
					
					
				}
				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + file));
				
				
				
				
				
				if(filemulti != null){
					File film = new File(getDir() + filemulti);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
						try{
							tm.put(id, getParent().getProps().getProperty("stockcodesec") + " " + name);	
						}catch(Exception e){
							
						}
						
					}
					fr.close();
					if(getMoveDir() != null)
					 film.renameTo(new File(getMoveDir() + filemulti));
				}
				
				
				if(filemulti2 != null){
					File film = new File(getDir() + filemulti2);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti2);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
try{
	tm.put(id,getParent().getProps().getProperty("stockcodethird") + " " + name);	
			
						}catch(Exception e){
						}
					}
					fr.close();
					if(getMoveDir() != null)
					 film.renameTo(new File(getMoveDir() + filemulti2));
				}
		
		
				if(filemulti5 != null){
					File film = new File(getDir() + filemulti5);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti5);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
try{
	tm.put(id,getParent().getProps().getProperty("stockcodesixt") + " " + name);	
						}catch(Exception e){
							
						}
					}
					fr.close();
					//if(getMoveDir() != null)
					// film.renameTo(new File(getMoveDir() + filemulti5));
				}
		

				
				if(filemulti4 != null){
					File film = new File(getDir() + filemulti4);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti4);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
try{
	tm.put(id,getParent().getProps().getProperty("stockcodefifth") + " " + name);	
							
						}catch(Exception e){
						}
					}
					fr.close();
					if(getMoveDir() != null)
					 film.renameTo(new File(getMoveDir() + filemulti4));
				}

				
				
				
				if(filemulti3 != null){
					File film = new File(getDir() + filemulti3);
					//  fil.renameTo(new File(getMoveDir()+file));

					 fr = new FileReader(getDir() + filemulti3);
					 reader = new BufferedReader(fr);
					 line = null;
					 id = null;
					 name = null;
					while ((line = reader.readLine()) != null) {
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);

						if (st.hasMoreTokens()) {
							id = st.nextToken().trim();
						}
						if (st.hasMoreTokens()) {
							name = st.nextToken().trim();
						}
						if(name == null){
							name = id;
						}
try{
	tm.put(id,getParent().getProps().getProperty("stockcodefourth") + " " + name);	
			
						}catch(Exception e){
						}
					}
					fr.close();
				//	if(getMoveDir() != null)
					// film.renameTo(new File(getMoveDir() + filemulti3));
				}
				
				
				
				String file1 = "tootelisad.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
				fr = new FileReader(getStatDir() + add + file1);
				reader = new BufferedReader(fr);
				line = null;
                
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				 st.nextToken();
		     	 st.nextToken();
		     	 st.nextToken();
				 String grupp = st.nextToken().trim();
				
				 if(grupp.length()>0){try{

						tm.put(grupp, grupp);
					}catch(Exception e){	
					}
				 }					
			   }
			}
			    fr.close();
				

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if("true".equals(getParent().getProps().getProperty("prodgroup.external"))){
			return new TreeMap();
		}
		return tm;

	}


	
	public TreeMap getAgents() {
		TreeMap tm = new TreeMap();
		try {
				
//			getFixedAssortmentData();

			String file = "agent.txt";
			String add = getParent().getProps().getProperty("add");

			if (add == null || add.trim().length() == 0) {
				add = "";
			}

			FileReader fr = new FileReader(getStatDir() + add + file);
			BufferedReader reader = new BufferedReader(fr);
			String line = null;
			while ((line = reader.readLine()) != null) {

			//	 System.out.println(line);
				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					Agent agent = new Agent(id);
					if (st.hasMoreTokens()) {
						String name = st.nextToken().trim();
						if (name.equals(""))
							name = "<" + id + ">";
						agent.setName(name);
						if (st.hasMoreTokens()) {
							st.nextToken();
							// System.out.println(name + sal);

						}
						agent.setLanguage("EST");
				          agent.setCountry("ee");
				          agent.setPassWord("0000");
				          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
				          if(email.length() > 1){
				        	  agent.setEmail(email);
				        	  agent.setActive(true);
				          }
				         
				          
				         
						
					}
					tm.put(agent.getName(), agent);
				}
			}

			
			String file2 = "dubbler.txt";
			File ff = new File(getStatDir() + add + file2);
			if(ff.exists()){		
			 fr = new FileReader(getStatDir() + add + file2);
			 reader = new BufferedReader(fr);
		 line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					
					Iterator iter = tm.values().iterator();
					while(iter.hasNext()){
						Agent agent = (Agent)iter.next();
						if(agent.getID().equals(id)){
							while (st.hasMoreTokens()) {
								String dubler = st.nextToken().trim();
								agent.addDubbler(dubler);
							//	System.out.println("Lisan Dublanti " + id + " " + dubler );
							}
						}	
					}
					
				}
			}
			}
			
			
			
			String[] names = (new File(getDir())).list();
			file = null;
			for (int i = 0; i < names.length; i++) {
				if ((names[i]).indexOf("agendimyyk") != -1 && getParent().getProps().getProperty("stockcode") != null &&
						(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
					file = names[i];
				}
			}
			if (file != null) {
				fr = new FileReader(getDir() + file);
				File f = new File(getDir() + file);
				reader = new BufferedReader(fr);
				line = null;
				while ((line = reader.readLine()) != null) {

					//System.out.println(line);
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Agent age = (Agent) tm.get(id);

						if (st.hasMoreTokens()) {
							st.nextToken().trim();
							if (st.hasMoreTokens()) {
								String sal = st.nextToken();
								int sales = new Integer(sal.trim().substring(0,
										sal.indexOf("."))).intValue();
								age.setMonthSales(sales);
							}
						}
					}
				}
				reader.close();
				fr.close();
				if(getMoveDir() != null)
				f.renameTo(new File(getMoveDir() + file));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		return tm;
	}

	
	
	
	  private  TreeMap excelketid = new TreeMap();
	
	
	
	  private   TreeMap getKampaaniad(){
 	     TreeMap tm = new TreeMap();
 	     System.out.println("Kampaaniad alustan");
 	      excelketid = new TreeMap();
 	     TreeMap kampid = new TreeMap();// siia tekstid mis klientidele kï¿½lge
			
 	     int a = 0;
 	   try {
 			  File f = new File (getExcelDir() + "Kampaaniad.xls");
 				
 			  FileInputStream	 ff = new FileInputStream(f);
 			
 			  
 			  //mï¿½ï¿½rame ketid
 			HSSFWorkbook workbook = new HSSFWorkbook((ff));
 			HSSFSheet sheet = workbook.getSheet("Ketid");
 			HSSFRow row = null; 
   					ff.close();	
   					if(sheet != null){
   						for(int i = 0 ; i < sheet.getLastRowNum();i++){
   							 row = sheet.getRow(i);
   							//System.out.println(row);
   							if(row == null && a == 0){
   								a = i;
   							}else{
   								a = sheet.getLastRowNum();
   							}
   						}
   						
   						for(int i = 0 ; i < a + 1;i++){
   							 row = sheet.getRow(i);
   							String kett1 = null;
   							String klient = null;
   							try {
   								kett1 = (row.getCell((short) 0).getStringCellValue());
   	    					} catch (Exception e) {
   	    						
   	    					}
   	    					try {
   								klient = (row.getCell((short) 1).getStringCellValue());
   	    					} catch (Exception e) {
   	    						//System.out.println("iiii " + i);
   	    						try{
   	    							klient = ((int)row.getCell((short) 1).getNumericCellValue()) + "";
   	    	      	    							
   	    						}catch(Exception r){
   	    							
   	    						}
   	    					}
   	    				//	System.out.println("www " + kett1 + " " + klient);
   	    					if(klient != null){
                                   excelketid.put(klient,kett1.replaceAll(" ", "") );	
           	    			}
   						  }
   						}
   	         		
 			  
 			  
 			  
 					 sheet = workbook.getSheet("Kampaaniad"); 
 					ff.close();	
 					
 					if(sheet != null){
 						for(int i = 0 ; i < sheet.getLastRowNum();i++){
 							 row = sheet.getRow(i);
 							//System.out.println(row);
 							if(row == null && a == 0){
 								a = i;
 							}else{
 								a = sheet.getLastRowNum();
 							}
 						}
 						for(int i = 0 ; i < a;i++){
 							
 							 row = sheet.getRow(i);
 							String prodid = null;
 							try {
 								prodid = (int) (row.getCell((short) 0)
 										.getNumericCellValue()) + "";
 							} catch (Exception e) {
 								try{
 									prodid = (row.getCell((short) 0).getStringCellValue());
 		 									
 								}catch (Exception k){
 									
 								}
 							}
 				
 							
 							if(prodid != null){
 							
 							//System.out.println("hiljem " +prodid);
 							String nimi = null;
 							try {
 								nimi = (row.getCell((short) 1).getStringCellValue());
 							} catch (Exception e) {
 								
 							}
 							String sisu = null;
 							try {
 								sisu = (row.getCell((short) 2).getStringCellValue());
 							} catch (Exception e) {
 								
 							}
 							
 							double price = 0;
 							try {
 								price = (row.getCell((short) 3).getNumericCellValue());
 							} catch (Exception e) {
 								
 							}
 							
 							java.util.Date alg = null;
 							String algus = null;
 							try {
 								alg = row.getCell((short) 5).getDateCellValue();
 								algus = getDateDDkMMkYYYYStr(alg);
 							} catch (Exception e) {
 								
 							}
 							java.util.Date lop = null ; 
 							String lopp = null;
 							try {
 								lop = row.getCell((short) 6).getDateCellValue();
 								lopp = getDateDDkMMkYYYYStr(lop);
 							} catch (Exception e) {
 								
 							}
 							
 							String kett = null;
 							try {
 								kett = (row.getCell((short) 7).getStringCellValue()).replaceAll(" ", "");
 							} catch (Exception e) {
 								
 							}
 							
 							java.util.Date dd = new java.util.Date();
 							if(alg != null){
 								alg = new Date(alg.getTime()- 604800000);
 							}
 							
 						if(alg != null && lop != null && dd.after(alg) && dd.before(lop)){
 						
 							for(String ke : kett.split(",")){
 							
 							if(kampid.get(ke) != null){
 								String aa = (String)kampid.get(ke) ;
 								kampid.put(ke, aa + "|" +sisu + "|  ("+ parent.getNF().format(price) + "€)"+  nimi + "|  " + algus + "->" + lopp + "|");
     	    				}else{
 								kampid.put(ke,  sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
     	    				}	
 						//	System.out.println("=== " +ke + " " +  sisu + "|  ("+ parent.getNF().format(price) + "€)" + nimi + "|  " + algus + "->" + lopp + "|" + price);
 				    		//System.out.println("AAA " + kett + " " + sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
 							MatrixLine mm = new MatrixLine();
 							mm.setCliGroup(ke);
 							mm.setProdID(prodid);
 							mm.setEndDate(lopp);
 							mm.setStartDate(algus);
 							mm.setPrice(price);
 							mm.setLineinfo(sisu);
 							
 						    matrixlines.add(mm);
 							
 							}

     						
 				   // System.out.println(mm.getStringForFixedAssortment());
 							
							} 
 						}
 						}
			//	System.out.println(kampid.toString());

 							}
 	         		
              	  }catch (Exception e){
                   //  e.printStackTrace();	
 	         	  }
         
              	  Iterator itera = excelketid.keySet().iterator();
              	 while (itera.hasNext()){
              		 String custid = (String) itera.next();
              		 String kett = (String)excelketid.get(custid);
              		 StringTokenizer st = new StringTokenizer(kett,";");
              		//System.out.println(custid + " " + kett);
          				 
              		 while(st.hasMoreElements()){
              			 String uuu = (st.nextElement() + "").trim();
              	//		System.out.println(custid + " " + uuu);
              			
              			 
              			 if(tm.get(custid) != null){
              				 String asi = "" + tm.get(custid);
              				 tm.put(custid, asi + "|" + kampid.get(uuu));
              			 }else{
                  			 tm.put(custid, kampid.get(uuu)); 
              			 }
                      }
              	 }
              	//System.out.println(tm.toString());
      			
 	   return tm;
 	   }  
        

	
	
      

      
      public  String getDateDDkMMkYYYYStr(java.util.Date cc){
			StringBuffer sb = new StringBuffer();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cc.getTime());
			
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}
			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day +"-");
			sb.append(month + "-");		
			sb.append("" + cal.get(Calendar.YEAR));
		
			return sb.toString();
		}
      
	
	
	
	
	
	
	public TreeMap getCustomers(Agent agent) {
		   TreeMap kampaaniad =  getKampaaniad();
		
		TreeMap tm = new TreeMap();
     	custMap = new HashMap();
		this.customers = new ArrayList();
		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("kliendid") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
		}
		
		if (file != null && (new File(getDir() + file)).length()>10) {
			try {

				File fil = new File(getDir() + file);
				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;

				
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";;", "; ;");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Customer customer = new Customer(id);
						customer.setAddress(" ");

						if (st.hasMoreTokens()) {
							String payer = st.nextToken().trim();
							customer.setPayer(payer);
						}

						Agent age = null;
						if (st.hasMoreTokens()) {
							age = new Agent(st.nextToken().trim());
					
							//age = (Agent)getParent().getAgentsHash().get(age.getID());
						
						}
						
						String name = null;
						if (st.hasMoreTokens()) {
						 name = st.nextToken().trim() + " " +id;

							Customer cust = (Customer) tm.get(name);
							if (cust != null) {
									if(age!= null){
									cust.addAgent(age);	
																	}
								tm.put(cust.getName(), cust);
							} else {
								if (name.equals(""))
									name = "<" + id + ">";
								customer.setName(Converter.enCodeEst(name));
								if (st.hasMoreTokens()) {
									String aadress = st.nextToken().trim();
									if (aadress != null) {
										customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
									}
								}

								if (st.hasMoreTokens()) {
								try{
									customer.setDebt(Double.parseDouble(st.nextToken().trim()));
								}catch(Exception e ){
									
								}
							
								}

								if (st.hasMoreTokens()) {
									try{
									customer.setDiscount(Double.parseDouble(st
											.nextToken().trim()));
}catch(Exception e ){
									
								}
								}
								
								if (st.hasMoreTokens()) {
									String blokk = st.nextToken().trim();
									if(blokk != null && blokk.equals("1")){
										customer.setSalesBlock(true);
									}
									
									if(blokk != null && blokk.equals("2")){
										customer.setSalesBlock(true);
									}
									
									if(blokk != null && blokk.equals("3")){
										customer.setSalesBlock(true);
									}
									
									if(blokk != null && blokk.equals("6")){
										customer.setSalesBlock(true);
									}
									
								}
								
								if (st.hasMoreTokens()) {
									
									String makse = st.nextToken();
									if(makse != null && makse.trim().length() > 0)
									makse = makse.substring(1);
								if(makse.equals("ette")){
									customer.setPaymTerm("-1");
								}else{
									//customer.setPaymTerm(makse); muutsin ära et sokki poleks 
								}
								}
				

								 if(kampaaniad.get(customer.getID()) != null){
						        	 customer.setInfo(Converter.enCodeEst(kampaaniad.get(customer.getID()) + ""));
						         }

			                     if(excelketid.get(customer.getID()) != null){
			                    	 customer.setGroup(((String)excelketid.get(customer.getID())).replaceAll(",", "|")
			                    			 .replaceAll(";", "|").replaceAll(" ", ""));
			                     //   System.out.println("Grupp " + customer.getGroup());
			                     }
								
									if(age != null ){
										customer.addAgent(age);	
									customer.addAgent(new Agent("HOUSE"));
										customer.addAgent(new Agent("manager"));

										customer.addAgent(new Agent("FLE12"));
										customer.addAgent(new Agent("INF12"));
										
										String ag4 = getParent().getProps().getProperty("defaultagent");
										String ag5 = getParent().getProps().getProperty("defaultagent2");
										String ag6 = getParent().getProps().getProperty("defaultagent3");
										String ag7 = getParent().getProps().getProperty("defaultagent4");
										String ag8 = getParent().getProps().getProperty("defaultagent5");
										
										if(ag8 != null){
											customer.addAgent(new Agent(ag8));		
								     	}
										
										if(ag4 != null){
											customer.addAgent(new Agent(ag4));		
								     	}
										
						     			if(ag5 != null){
						     				customer.addAgent(new Agent(ag5));
						     			}
						     			
						     			if(ag6 != null){
						     				customer.addAgent(new Agent(ag6));
						     			}
						     			
						     			if(ag7 != null){
						     				customer.addAgent(new Agent(ag7));
						     			}
										
									}
									
									if (age != null && getParent().getAgentsHash().get(age.getID()) != null){
										if(file.indexOf("51")!=- 1){ //ainult goldberger
											customer.setMainAgent(age.getID());	
											customer.addAgent(new Agent("MAN51"));
											customer.addAgent(new Agent("SUP51"));
											customer.addAgent(new Agent("IDA51"));
											customer.addAgent(new Agent("REK51"));
											customer.addAgent(new Agent("RES51"));
											customer.addAgent(new Agent("SIM51"));
											customer.addAgent(new Agent("KAT51"));
												
											
										}
									}
										
							
							
								
								if (age != null && getParent().getAgentsHash().get(age.getID()) != null){
									Agent aa = (Agent)getParent().getAgentsHash().get(age.getID());
									Vector vec = aa.getDubbler();
									if(vec!= null && vec.size() >0){
										Enumeration en = vec.elements();
										while(en.hasMoreElements()){
											String ag = (String) en.nextElement();
											customer.addAgent(new Agent(ag));
										}
									}
								}
								
							        		customer.addFixSortCode(Converter.cleanLitLatEst(customer.getID()));
								        
							        
								
								
								tm.put(name, customer);
								this.customers.add(customer.getID());
								
						          custMap.put(customer.getID(), customer);
						       
							}
						}

					}
				}
				String aggg = getParent().getProps().getProperty("out");
				if(aggg != null){
					System.exit(0);
				}
				
                   fr.close();
                   reader.close();
				String file1 = "lisaklient.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
				fr = new FileReader(getStatDir() + add + file1);
				reader = new BufferedReader(fr);
				line = null;
				Customer abi = null;
                
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				if (st.hasMoreTokens()) {
		     		String agcode = st.nextToken();
		     		
		     		if(agcode.indexOf("29")!=- 1 || agcode.indexOf("31") != -1){
                     abi = new Customer(id);	
                    }else{
                   	 abi = new Customer("ABI" + id);		
                    }
		     		
		     		
		     		
					abi.addAgent(new Agent(agcode));
					
								
					Agent age = (Agent)getParent().getAgentsHash().get(agcode);
					
					
					if (age != null){
						Vector vec = age.getDubbler();
						if(vec!= null && vec.size() >0){
							Enumeration en = vec.elements();
							while(en.hasMoreElements()){
								String ag = (String) en.nextElement();
								abi.addAgent(new Agent(ag));
							}
						}
					}
					
					abi.addAgent(new Agent("KK22"));
					abi.addAgent(new Agent("manager"));
					abi.addAgent(new Agent("HOUSE"));
					if (st.hasMoreTokens()) {	
						String name = st.nextToken().trim();
						if (name.equals(""))
							name = "<" + id + ">";
						

			     		if(agcode.indexOf("29")!=- 1 || agcode.indexOf("31") != -1){
							abi.setName(name.toUpperCase());	
	                    }else{
							abi.setName("S " +name.toUpperCase());		
	                    }
						
						
					}
					if (st.hasMoreTokens()) {	
						String addr = st.nextToken().trim();
						if (addr.equals(""))
							addr = "<" + id + ">";
						abi.setAddress(addr);	
				          custMap.put(abi.getID(), abi);
					}
					if (st.hasMoreTokens()) {	
						String paev = st.nextToken().trim();
						if (!paev.equals(""))
						abi.setVisit(paev);	
				          custMap.put(abi.getID(), abi);
					}
				}

			//	System.out.println(abi.getName() + abi.getID());
				tm.put(abi.getName(), abi);
				 custMap.put(abi.getID(), abi);
				this.customers.add(abi.getID());
			     }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }


                fr.close();
                reader.close();
                
				String file2 = "custvisit.txt";
				add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
		   try{
				fr = new FileReader(getStatDir() + add + file2);
				reader = new BufferedReader(fr);
				line = null;
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Customer abi = (Customer)custMap.get(id);
				if (abi != null){
				
				if (st.hasMoreTokens()) {
		     		String visit = st.nextToken();
		     		if (visit != null && visit.trim().length()>0){
		     			abi.setVisit(visit);								
		     		}
				  }
				}
                }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }				
				

				String file3 = "custagent.txt";
				add = getParent().getProps().getProperty("add");

		   try{
				fr = new FileReader(getStatDir()  + file3);
				reader = new BufferedReader(fr);
				line = null;
			while ((line = reader.readLine()) != null) {
		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Customer abi = (Customer)custMap.get(id);
				if (abi != null){
				
				if (st.hasMoreTokens()) {
		     		String visit = st.nextToken();
		     		if (visit != null && visit.trim().length()>0){
		     			abi.addAgent(new Agent(visit));		
		     			//System.out.println("abiagent" + visit + " " +id);
		     		}
				  }
				}
                }
		        }
				}catch (Exception e){
		     	 // e.printStackTrace();
		        }				
				
				
				String file4 = getParent().getProps().getProperty("custag");
				String ag4 = getParent().getProps().getProperty("defaultagent");
				String ag5 = getParent().getProps().getProperty("defaultagent2");
				String ag6 = getParent().getProps().getProperty("defaultagent3");
				String ag7 = getParent().getProps().getProperty("defaultagent4");
			if(file4 != null){
				
			
				try{
					
					//System.out.println("Abi " + custMap.size()  );
					fr = new FileReader(file4);
					reader = new BufferedReader(fr);
					line = null;
					
				while ((line = reader.readLine()) != null) {
			        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				    if (st.hasMoreTokens()) {
					String id = st.nextToken().trim();
					Customer abi = (Customer)custMap.get(id);
					if (abi != null){
					
					if (st.hasMoreTokens()) {
			     		
			     			abi.addAgent(new Agent(ag4));		
			     			//System.out.println("abiagent" + ag4 + " " +id + " " + ag5 + " " + ag6);
			     			
			     			if(ag5 != null){
			     				abi.addAgent(new Agent(ag5));
			     			}
			     			
			     			if(ag6 != null){
			     				abi.addAgent(new Agent(ag6));
			     			}
			     			if(ag7 != null){
			     				abi.addAgent(new Agent(ag7));
			     			}
			     		
					  }
					}
	                }
			        }
					}catch (Exception e){
			     	 // e.printStackTrace();
			        }
			}	
				
				
				reader.close();
				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + file));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("outbabe");
			System.exit(0);
		}
		
	    
	    if(rcompany != null){
	//		 Converter.sendCustomerJson(tm, rcompany, true,user,pass);
		    }

		return tm;
	}

	public TreeMap getProducts(Agent agent) {
		TreeMap tm = new TreeMap();
		TreeMap koodiga = new TreeMap();
		String[] names = (new File(getDir())).list();
		String file = null;
		String filestock  = null;
		String filemulti = null;
		String filestockmulti  = null;
        boolean pane = false;
		String filemulti2 = null;
		String filestockmulti2  = null;
		
		String filemulti3 = null;
		String filestockmulti3  = null;
		String filemulti4 = null;
		String filestockmulti4  = null;
		String filemulti5 = null;
		String filestockmulti5  = null;
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				filestock = names[i];
			}
		}
		
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filemulti = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filestockmulti = names[i];
			}
		}
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filemulti2 = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filestockmulti2 = names[i];
			}
		}
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodefourth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefourth")) != -1) {
				filemulti3 = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodefourth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefourth")) != -1) {
				filestockmulti3 = names[i];
			}
		}
		
		
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodefifth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefifth")) != -1) {
				filemulti4 = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodefifth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefifth")) != -1) {
				filestockmulti4 = names[i];
			}
		}
		
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("tooted") != -1 && getParent().getProps().getProperty("stockcodesixt") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesixt")) != -1) {
				filemulti5 = names[i];
			}
			if ((names[i]).indexOf("laoseis") != -1 && getParent().getProps().getProperty("stockcodesixt") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesixt")) != -1) {
				filestockmulti5 = names[i];
			}
		}
		
		 TreeMap partii = new TreeMap();
		 
			if (filestockmulti5 != null) {
				try {
					File fil = new File(getDir() + filestockmulti5);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filestockmulti5));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
		 
		 
			if (filestockmulti4 != null) {
				try {
					File fil = new File(getDir() + filestockmulti4);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filestockmulti4));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
		 
			if (filestockmulti3 != null) {
				try {
					File fil = new File(getDir() + filestockmulti3);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filestockmulti3));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
			if (filestockmulti2 != null) {
				try {
					File fil = new File(getDir() + filestockmulti2);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filestockmulti2));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
			
			if (filestockmulti != null) {
				try {
					File fil = new File(getDir() + filestockmulti);
					FileReader fr = new FileReader(fil);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					reader.readLine();
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll("\",\"",",");
						line = line.replaceAll("\"","");
						line = line.replaceAll(",,",", ,");
						line = line.replaceAll(",,",", ,");
						StringTokenizer st = new StringTokenizer(line, ",");
						if( line.startsWith("LIN")){
						
							st.nextToken();
							String prodid = st.nextToken().trim();
							double nn = Double.parseDouble(st.nextToken().trim());
							if(st.hasMoreTokens() && nn > 0){
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								st.nextToken();
								try{
									String info = 	st.nextToken().trim();
									//System.out.println(line + " nnn "+ info);
									if(partii.get(prodid) != null){
										String inn = (String) partii.get(prodid);
										if(inn.length()> 15){
											inn = inn + "|";
										}
										partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
									}else{
										partii.put(prodid, info.replaceAll(";", ":"));
								     }	
										
								}catch (Exception ss){
									
								}
							
							}				
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filestockmulti));
					
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
			
			
		if (filestock != null) {
			try {
				File fil = new File(getDir() + filestock);
				FileReader fr = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				reader.readLine();
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll("\",\"",",");
					line = line.replaceAll("\"","");
					line = line.replaceAll(",,",", ,");
					line = line.replaceAll(",,",", ,");
					StringTokenizer st = new StringTokenizer(line, ",");
					if( line.startsWith("LIN")){
					
						st.nextToken();
						String prodid = st.nextToken().trim();
						double nn = Double.parseDouble(st.nextToken().trim());
						if(st.hasMoreTokens() && nn > 0){
							st.nextToken();
							st.nextToken();
							st.nextToken();
							st.nextToken();
							st.nextToken();
							try{
								String info = 	st.nextToken().trim();
							//	System.out.println(line + " nnn "+ info);
								if(partii.get(prodid) != null){
									String inn = (String) partii.get(prodid);
									if(inn.length()> 15){
										inn = inn + "|";
									}
									partii.put(prodid,inn +  " " + info.replaceAll(";", ":"));
								}else{
									partii.put(prodid, info.replaceAll(";", ":"));
							     }	
									
							}catch (Exception ss){
								
							}
						
						}				
					}
				}
				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + filestock));
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}
		
		
		
		String gronly = null;
		if(getParent().getProps().getProperty("grouponly") != null){
			gronly= getParent().getProps().getProperty("grouponly");
		}
		String codeend = null;
		if(getParent().getProps().getProperty("codeend") != null){
			codeend = getParent().getProps().getProperty("codeend");
		}
		
//System.out.println("file " + file + " " + gronly);
		if (file != null) {
			try {
				File fil = new File(getDir() + file);
				FileReader fr = new FileReader(fil);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";;","; ;");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					
					boolean sv = false;
					//System.out.println(line);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Product prod = new Product(id);
						String name = "";
						if (st.hasMoreTokens()) {
                             name = st.nextToken().trim();
                           
                             if(name.startsWith("* ")){
                            	 sv = true;
                             }
                             
							 if ("22".equals(getParent().getProps().getProperty("stockcode"))) {
							 	prod.setName( Converter.enCodeEst("    "+id.substring(0,2) + " " + name));	
							 }else{
							 	prod.setName(Converter.enCodeEst("    "+name));								 	
							 }
							if (st.hasMoreTokens()) {
								double price = Double.parseDouble(st
										.nextToken().trim());
								// if (price > 0){

								prod.setPrice(price);
								if (st.hasMoreTokens()) {
									double inCases = Double.parseDouble(st
											.nextToken().trim());
									if (inCases > 0) {
										prod.setInCases(inCases);
										if (st.hasMoreTokens()) {
											String ean = st.nextToken().trim();
											if (ean.length() > 0) {
												prod.setEAN(ean);
                                               if(ean.length() > 4){
                                            	   prod.setName(prod.getName() +  " " + ean.substring(ean.length()-4));
                                               }
											}
											
											if (st.hasMoreTokens()) {
												String gr = st.nextToken()
														.trim();
												if (gr.length() > 0) {
													prod.setGroup(gr);
												}
											}
   if(partii.get(id) != null){
	   prod.setPartii((String) partii.get(id));
	//   System.out.println("Sain partii "  + prod.getPartii());
   }
										
											//System.out.println(prod.getEAN());
											if (st.hasMoreTokens()) {
												prod.setStock(Double
														.parseDouble(st
																.nextToken()
																.trim()));
												//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
												// if (prod.getAgentProduct().equals(agent.getProdGroup())){
												//tm.put(prod.getName(), prod);
												//b}
												st.nextToken();
												if (st.hasMoreTokens()) {
													String unit = st
															.nextToken();
													if (unit != null
															&& unit.trim()
																	.length() > 0) {
														prod.setUnit(unit
																.trim());
													}
												}
                                               String hhh = null;
												if (st.hasMoreTokens()) {
													String gr = st.nextToken();
													//System.out.println(gr);
													if (gr != null
															&& gr.trim()
																	.length() > 0) {
														hhh =(gr.trim());
													}
												}
											//	System.out.println(prod.getName().trim().indexOf("Y"));
												//System.out.println(line);
												if(getParent().getProps().getProperty("stockcode").equals("22") ){
													if(prod.getName().trim().indexOf("Y")!=3){
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												   }else{
													   boolean goes = true ;
													   if(sv && prod.getStock()<1 && getParent().getProps().getProperty("stockcode").equals("29")){ // henkeli erijuhtum et SV algusega 
														   goes = false;
													   }
													 //  System.out.println("gr " + hhh);
													   if(gronly != null && gronly.indexOf(",") != -1 ){
														   //kui on mitu siis 
														   StringTokenizer stt = new StringTokenizer (gronly ,",");
														   goes = false;
														   while(stt.hasMoreElements()){
															   String gr = stt.nextToken();
															    if( gr.equals(hhh)){
																 goes = true;
														//		System.out.println("gr " + hhh);
															   }
														   }
														   
													   }else if(codeend != null  ){
														   //kui on mitu siis 
														   StringTokenizer stt = new StringTokenizer (codeend ,",");
														   goes = false;
														   while(stt.hasMoreElements()){
															   String gr = stt.nextToken();
															    if(id.endsWith(gr)){
																 goes = true;
															//	System.out.println("Sisse " + id);
															   }
														   }
														   
													   }else{
														   
														  // Kui on 1 grupp
														   if(gronly != null && !gronly.equals(hhh)){
															   goes = false;
														   }  
													   }
													   
													   
													 //  System.out.println("Nimi " + goes + " " + prod.getName());
													   if(goes){
														  // System.out.println("Nimi " + prod.getName());
															tm.put(prod.getName(), prod);
															koodiga.put(prod.getID(), prod);	   
													   }
												}
											}
										}
									}
								}
							}
						}
					}
				}
				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + file));
				
				//siin pannakse kylge send separate juhul kui on multi firma case
				
				if(filemulti != null || filemulti2 != null  || filemulti3 != null || filemulti4 != null ||
						filemulti5 != null ){
				//	System.out.println("filemulti" + filemulti);
					Iterator ite = tm.keySet().iterator();
					while(ite.hasNext()){
						Product pp = (Product)tm.get(ite.next());
						pp.setSendSeparate(getParent().getProps().getProperty("stockcode"));
					}
				}
				
				
				//siin on blokk teie toote failijaoks
			
				if(filemulti2 != null){
					//System.out.println("filemulti2" + filemulti2);
				
					
				 fil = new File(getDir() + filemulti2);
					 fr = new FileReader(fil);
					 reader = new BufferedReader(fr);
					 line = null;
					//	System.out.println("filemulti2" + fil.getAbsolutePath());
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll(";;","; ;");
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						//System.out.println(line);
						if (st.hasMoreTokens()) {
							String id = st.nextToken().trim();
							Product prod = new Product(id);
							prod.setSendSeparate(getParent().getProps().getProperty("stockcodethird"));
							if (st.hasMoreTokens()) {

									prod.setName(Converter.enCodeEst("   "+getParent().getProps().getProperty("stockcodethird") + " " + st.nextToken().trim()));			 	
								 
								if (st.hasMoreTokens()) {
									double price = Double.parseDouble(st
											.nextToken().trim());
									// if (price > 0){

									prod.setPrice(price);
									if (st.hasMoreTokens()) {
										double inCases = Double.parseDouble(st
												.nextToken().trim());
										if (inCases > 0) {
											prod.setInCases(inCases);
											if (st.hasMoreTokens()) {
												String ean = st.nextToken().trim();
												if (ean.length() > 0) {
													prod.setEAN(ean);
													 if(ean.length() > 4){
		                                            	   prod.setName(prod.getName() +  " " + ean.substring(ean.length()-4));
		                                               }
												}
												if (st.hasMoreTokens()) {
													String gr = st.nextToken()
															.trim();
													if (gr.length() > 0) {
														prod.setGroup(gr);
													}
												}
	   if(partii.get(id) != null){
		   prod.setPartii((String) partii.get(id));
		 // System.out.println("Sain partii "  + prod.getPartii());
	   }
												//System.out.println(line);
												//System.out.println(prod.getEAN());
												if (st.hasMoreTokens()) {
													prod.setStock(Double
															.parseDouble(st
																	.nextToken()
																	.trim()));
													//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
													// if (prod.getAgentProduct().equals(agent.getProdGroup())){
													//tm.put(prod.getName(), prod);
													//b}
													st.nextToken();
													if (st.hasMoreTokens()) {
														String unit = st
																.nextToken();
														if (unit != null
																&& unit.trim()
																		.length() > 0) {
															prod.setUnit(unit
																	.trim());
														}
													}

													if (st.hasMoreTokens()) {
														String gr = st.nextToken();
														//System.out.println(gr);
														if (gr != null
																&& gr.trim()
																		.length() > 0) {
															//	prod.setGroup(gr.trim());
														}
													}
												//	System.out.println(prod.getName().trim().indexOf("Y"));
													if(getParent().getProps().getProperty("stockcode").equals("22") ){
														if(prod.getName().trim().indexOf("Y")!=3){
															tm.put(prod.getName(), prod);
															koodiga.put(prod.getID(), prod);	
														}
													   }else{
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												}
											}
										}
									}
								}
							}
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filemulti2));
				}
				
				if(filemulti3 != null){
					//System.out.println("filemulti2" + filemulti2);
				
					
				 fil = new File(getDir() + filemulti3);
					 fr = new FileReader(fil);
					 reader = new BufferedReader(fr);
					 line = null;
					//	System.out.println("filemulti2" + fil.getAbsolutePath());
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll(";;","; ;");
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						//System.out.println(line);
						if (st.hasMoreTokens()) {
							String id = st.nextToken().trim();
							Product prod = new Product(id);
							prod.setSendSeparate(getParent().getProps().getProperty("stockcodefourth"));
							if (st.hasMoreTokens()) {

									prod.setName(Converter.enCodeEst("   "+getParent().getProps().getProperty("stockcodefourth") + " " + st.nextToken().trim()));			 	
								 
								if (st.hasMoreTokens()) {
									double price = Double.parseDouble(st
											.nextToken().trim());
									// if (price > 0){

									prod.setPrice(price);
									if (st.hasMoreTokens()) {
										double inCases = Double.parseDouble(st
												.nextToken().trim());
										if (inCases > 0) {
											prod.setInCases(inCases);
											if (st.hasMoreTokens()) {
												String ean = st.nextToken().trim();
												if (ean.length() > 0) {
													prod.setEAN(ean);
													 if(ean.length() > 4){
		                                            	   prod.setName(prod.getName() +  " " + ean.substring(ean.length()-4));
		                                               }
												}
												if (st.hasMoreTokens()) {
													String gr = st.nextToken()
															.trim();
													if (gr.length() > 0) {
														prod.setGroup(gr);
													}
												}
	   if(partii.get(id) != null){
		   prod.setPartii((String) partii.get(id));
		 // System.out.println("Sain partii "  + prod.getPartii());
	   }
												//System.out.println(line);
												//System.out.println(prod.getEAN());
												if (st.hasMoreTokens()) {
													prod.setStock(Double
															.parseDouble(st
																	.nextToken()
																	.trim()));
													//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
													// if (prod.getAgentProduct().equals(agent.getProdGroup())){
													//tm.put(prod.getName(), prod);
													//b}
													st.nextToken();
													if (st.hasMoreTokens()) {
														String unit = st
																.nextToken();
														if (unit != null
																&& unit.trim()
																		.length() > 0) {
															prod.setUnit(unit
																	.trim());
														}
													}

													if (st.hasMoreTokens()) {
														String gr = st.nextToken();
														//System.out.println(gr);
														if (gr != null
																&& gr.trim()
																		.length() > 0) {
															//	prod.setGroup(gr.trim());
														}
													}
												//	System.out.println(prod.getName().trim().indexOf("Y"));
													if(getParent().getProps().getProperty("stockcode").equals("22") ){
														if(prod.getName().trim().indexOf("Y")!=3){
															tm.put(prod.getName(), prod);
															koodiga.put(prod.getID(), prod);	
														}
													   }else{
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												}
											}
										}
									}
								}
							}
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filemulti3));
				}
				
				if(filemulti4 != null){
					//System.out.println("filemulti2" + filemulti2);
				
					
				 fil = new File(getDir() + filemulti4);
					 fr = new FileReader(fil);
					 reader = new BufferedReader(fr);
					 line = null;
					//	System.out.println("filemulti2" + fil.getAbsolutePath());
					while ((line = reader.readLine()) != null) {
						line = line.replaceAll(";;","; ;");
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						//System.out.println(line);
						if (st.hasMoreTokens()) {
							String id = st.nextToken().trim();
							Product prod = new Product(id);
							prod.setSendSeparate(getParent().getProps().getProperty("stockcodefifth"));
							if (st.hasMoreTokens()) {

									prod.setName(Converter.enCodeEst("   "+getParent().getProps().getProperty("stockcodefifth") + " " + st.nextToken().trim()));			 	
								 
								if (st.hasMoreTokens()) {
									double price = Double.parseDouble(st
											.nextToken().trim());
									// if (price > 0){

									prod.setPrice(price);
									if (st.hasMoreTokens()) {
										double inCases = Double.parseDouble(st
												.nextToken().trim());
										if (inCases > 0) {
											prod.setInCases(inCases);
											if (st.hasMoreTokens()) {
												String ean = st.nextToken().trim();
												if (ean.length() > 0) {
													prod.setEAN(ean);
													 if(ean.length() > 4){
		                                            	   prod.setName(prod.getName() +  " " + ean.substring(ean.length()-4));
		                                               }
												}
												if (st.hasMoreTokens()) {
													String gr = st.nextToken()
															.trim();
													if (gr.length() > 0) {
														prod.setGroup(gr);
													}
												}
	   if(partii.get(id) != null){
		   prod.setPartii((String) partii.get(id));
		 // System.out.println("Sain partii "  + prod.getPartii());
	   }
												//System.out.println(line);
												//System.out.println(prod.getEAN());
												if (st.hasMoreTokens()) {
													prod.setStock(Double
															.parseDouble(st
																	.nextToken()
																	.trim()));
													//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
													// if (prod.getAgentProduct().equals(agent.getProdGroup())){
													//tm.put(prod.getName(), prod);
													//b}
													st.nextToken();
													if (st.hasMoreTokens()) {
														String unit = st
																.nextToken();
														if (unit != null
																&& unit.trim()
																		.length() > 0) {
															prod.setUnit(unit
																	.trim());
														}
													}

													if (st.hasMoreTokens()) {
														String gr = st.nextToken();
														//System.out.println(gr);
														if (gr != null
																&& gr.trim()
																		.length() > 0) {
															//	prod.setGroup(gr.trim());
														}
													}
												//	System.out.println(prod.getName().trim().indexOf("Y"));
													if(getParent().getProps().getProperty("stockcode").equals("22") ){
														if(prod.getName().trim().indexOf("Y")!=3){
															tm.put(prod.getName(), prod);
															koodiga.put(prod.getID(), prod);	
														}
													   }else{
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												}
											}
										}
									}
								}
							}
						}
					}
					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filemulti4));
				}
				


if(filemulti5 != null){
	//System.out.println("filemulti2" + filemulti2);
	
	String gronly6 = null;
	if(getParent().getProps().getProperty("grouponly6") != null){
		gronly6= getParent().getProps().getProperty("grouponly6");
	}
	
 fil = new File(getDir() + filemulti5);
	 fr = new FileReader(fil);
	 reader = new BufferedReader(fr);
	 line = null;
	//	System.out.println("filemulti2" + fil.getAbsolutePath());
	while ((line = reader.readLine()) != null) {
	
		line = line.replaceAll(";;","; ;");
		StringTokenizer st = new StringTokenizer(line, SEPARATOR);
		//System.out.println(line);
		if (st.hasMoreTokens()) {
			String id = st.nextToken().trim();
			Product prod = new Product(id);
			prod.setSendSeparate(getParent().getProps().getProperty("stockcodesixt"));
			if (st.hasMoreTokens()) {

					prod.setName(Converter.enCodeEst("   "+getParent().getProps().getProperty("stockcodesixt") + " " + st.nextToken().trim()));			 	
				 
				if (st.hasMoreTokens()) {
					double price = Double.parseDouble(st
							.nextToken().trim());
					// if (price > 0){

					prod.setPrice(price);
					if (st.hasMoreTokens()) {
						double inCases = Double.parseDouble(st
								.nextToken().trim());
						if (inCases > 0) {
							prod.setInCases(inCases);
							if (st.hasMoreTokens()) {
								String ean = st.nextToken().trim();
								if (ean.length() > 0) {
									prod.setEAN(ean);
									 if(ean.length() > 4){
                                  	   prod.setName(prod.getName() +  " " + ean.substring(ean.length()-4));
                                     }
								}
								if (st.hasMoreTokens()) {
									String gr = st.nextToken()
											.trim();
									if (gr.length() > 0) {
										prod.setGroup(gr);
									}
								}
if(partii.get(id) != null){
prod.setPartii((String) partii.get(id));
// System.out.println("Sain partii "  + prod.getPartii());
}
								//System.out.println(line);
								//System.out.println(prod.getEAN());
								if (st.hasMoreTokens()) {
									prod.setStock(Double
											.parseDouble(st
													.nextToken()
													.trim()));
									//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
									// if (prod.getAgentProduct().equals(agent.getProdGroup())){
									//tm.put(prod.getName(), prod);
									//b}
									st.nextToken();
									if (st.hasMoreTokens()) {
										String unit = st
												.nextToken();
										if (unit != null
												&& unit.trim()
														.length() > 0) {
											prod.setUnit(unit
													.trim());
										}
									}
									 String hhh = null;
									if (st.hasMoreTokens()) {
										String gr = st.nextToken();
										//System.out.println(gr);
										if (gr != null
												&& gr.trim()
														.length() > 0) {
											//	prod.setGroup(gr.trim());
											hhh = gr.trim();
										}
									}
									boolean goes = true;
									 
									  
									  
									   
									   if(gronly6 != null && gronly6.indexOf(",") != -1 ){
										   //kui on mitu siis 
										   StringTokenizer stt = new StringTokenizer (gronly6 ,",");
										   goes = false;
										   while(stt.hasMoreElements()){
											   String gr = stt.nextToken();
											    if( gr.equals(hhh)){
												 goes = true;
												//System.out.println("gr " + hhh);
											   }
										   }
										   
									   }else{
										   
										  // Kui on 1 grupp
										   if(gronly6 != null && !gronly6.equals(hhh)){
											   goes = false;
										   }  
									   }
									  
								//	System.out.println(prod.getName().trim().indexOf("Y"));
									if(goes){
										//System.out.println(" 12 osakond " + prod.getName());
										tm.put(prod.getName(), prod);
										koodiga.put(prod.getID(), prod);	
									}
								}
							}
						}
					}
				}
			}
		}
	}
	fr.close();
	if(getMoveDir() != null)
	fil.renameTo(new File(getMoveDir() + filemulti5));
}


				
			if(filemulti != null){
			
				
			 fil = new File(getDir() + filemulti);
				 fr = new FileReader(fil);
				 reader = new BufferedReader(fr);
				 line = null;
					//System.out.println("filemulti" + fil.getAbsolutePath());
				while ((line = reader.readLine()) != null) {
					line = line.replaceAll(";;","; ;");
					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					//System.out.println(line);
					if (st.hasMoreTokens()) {
						String id = st.nextToken().trim();
						Product prod = new Product(id);
						prod.setSendSeparate(getParent().getProps().getProperty("stockcodesec"));
						
						if (st.hasMoreTokens()) {
								prod.setName(Converter.enCodeEst("   "+getParent().getProps().getProperty("stockcodesec") + " " + st.nextToken().trim()));			 	
							if (st.hasMoreTokens()) {
								double price = Double.parseDouble(st
										.nextToken().trim());
								// if (price > 0){

								prod.setPrice(price);
								if (st.hasMoreTokens()) {
									double inCases = Double.parseDouble(st
											.nextToken().trim());
									if (inCases > 0) {
										prod.setInCases(inCases);
										if (st.hasMoreTokens()) {
											String ean = st.nextToken().trim();
											if (ean.length() > 0) {
												prod.setEAN(ean);
												 if(ean.length() > 4){
	                                            	   prod.setName(prod.getName() +  " " + ean.substring(ean.length()-4));
	                                               }
											}
											if (st.hasMoreTokens()) {
												String gr = st.nextToken()
														.trim();
												if (gr.length() > 0) {
													prod.setGroup(gr);
												}
											}
   if(partii.get(id) != null){
	   prod.setPartii((String) partii.get(id));
	  // System.out.println("Sain partii "  + prod.getPartii());
   }
											//System.out.println(line);
											//System.out.println(prod.getEAN());
											if (st.hasMoreTokens()) {
												prod.setStock(Double
														.parseDouble(st
																.nextToken()
																.trim()));
												//prod.setStock(Double.parseDouble((String)stockid.get(prod.getID())));
												// if (prod.getAgentProduct().equals(agent.getProdGroup())){
												//tm.put(prod.getName(), prod);
												//b}
												st.nextToken();
												if (st.hasMoreTokens()) {
													String unit = st
															.nextToken();
													if (unit != null
															&& unit.trim()
																	.length() > 0) {
														prod.setUnit(unit
																.trim());
													}
												}

												if (st.hasMoreTokens()) {
													String gr = st.nextToken();
													//System.out.println(gr);
													if (gr != null
															&& gr.trim()
																	.length() > 0) {
														//	prod.setGroup(gr.trim());
													}
												}
											//	System.out.println(prod.getName().trim().indexOf("Y"));
												if(getParent().getProps().getProperty("stockcode").equals("22") ){
													if(prod.getName().trim().indexOf("Y")!=3){
														tm.put(prod.getName(), prod);
														koodiga.put(prod.getID(), prod);	
													}
												   }else{
													tm.put(prod.getName(), prod);
													koodiga.put(prod.getID(), prod);	
												}
											}
										}
									}
								}
							}
						}
					}
				}
				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + filemulti));
			}
			
			
			//siin lõpeb teise firma toodete lisamine
				
				
				
				String file1 = "tootelisad.txt";
				String add = getParent().getProps().getProperty("add");

				if (add == null || add.trim().length() == 0) {
					add = "";
				}
				
		   try{
				fr = new FileReader(getStatDir() + add + file1);
				reader = new BufferedReader(fr);
				line = null;
                
			while ((line = reader.readLine()) != null) {
			
			
//Kaubakood	Nimetus	PDA -s	tk alusel	tk kastis	tk karbis	tk 1/4 müügialusel	tk 1/2 müügialusel	Vöötkood

		        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
			    if (st.hasMoreTokens()) {
				String id = st.nextToken().trim();
				Product prodd =(Product) koodiga.get(id); 
				if( prodd!= null){
				
		     	 String name = st.nextToken();
		     	 if(prodd.getName().toLowerCase().indexOf("everest") != -1){
		     		 prodd.setName(Converter.enCodeEst("    72 " + name.trim()));
		     	 }
		     	 st.nextToken();
				
				 String grupp = st.nextToken().trim();
				 if(grupp.length()>0){
					 prodd.setGroup(grupp);
				 }
	             String alusel = st.nextToken();
	             if(alusel.trim().length()>0){
	            	 try{
		            	 prodd.setOnPallet(Double.parseDouble(alusel.trim())); 
	            	 }catch(Exception ee){
	            	 }	
	             }
	             String kastis = st.nextToken();
	             if(kastis.trim().length()>0){
	            	 try{
		            	 prodd.setInCases(Double.parseDouble(kastis.trim())); 
	            	 }catch(Exception ee){
	            	 }	
	             	
	             }
			    	

				    String minam = st.nextToken();
		             if(minam.trim().length()>0){
		            	 try{
			            	 prodd.setOptAmountInUnits(Double.parseDouble(minam.trim())); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		             
		             String info = st.nextToken();
		             if(info.trim().length()>0){
		            	 try{
			            	 prodd.setPartii( "1/4 alusel " + info.trim() + " | "); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		             
		             info = st.nextToken();
		             if(info.trim().length()>0){
		            	 try{
			            	 prodd.setPartii( prodd.getPartii() + "1/2 alusel " + info.trim() ); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		     	
				} else {
					//lisame need tooted mis on toote lisas aga puuduvad AXA - st .

				
					Product proddu = new Product(id); 
					
					
			     	 String name = "x" + st.nextToken().trim();
			     	proddu.setName(name);
			     	 st.nextToken();
					
					 String grupp = st.nextToken().trim();
					 if(grupp.length()>0){
						 proddu.setGroup(Converter.cleanLitLatEst(grupp));
					 }
		             String alusel = st.nextToken();
		             if(alusel.trim().length()>0){
		            	 try{
		            		 proddu.setOnPallet(Double.parseDouble(alusel.trim())); 
		            	 }catch(Exception ee){
		            	 }	
		             }
		             String sep = getParent().getProps().getProperty("separateemail");
		             proddu.setSendSeparate(sep);
		             String kastis = st.nextToken();
		             if(kastis.trim().length()>0){
		            	 try{
		            		 proddu.setInCases(Double.parseDouble(kastis.trim())); 
		            	 }catch(Exception ee){
		            	 }	
		             	
		             }
				    	

					    String minam = st.nextToken();
			             if(minam.trim().length()>0){
			            	 try{
			            		 proddu.setOptAmountInUnits(Double.parseDouble(minam.trim())); 
			            	 }catch(Exception ee){
			            	 }	
			             }
			             
			             String info = st.nextToken();
			             if(info.trim().length()>0){
			            
			             }
			             
			             info = st.nextToken();
			             if(info.trim().length()>0){
			            	
			             }
			            String ean =  st.nextToken();
			            if(ean.trim().length() >0 ){
			            	proddu.setEAN(ean.trim());
			            	 if(ean.length() > 4){
                          	   proddu.setName(proddu.getName() +  " " + ean.substring(ean.length()-4));
                             }
			            }
			             double hind = 0;
			             try{
			               hind = Double.parseDouble(st.nextToken().replace(',', '.').trim());	 
			             }catch(Exception e){
			            	 
			             }
			             
			             if(hind != 0){
			            	 proddu.setPrice(hind);
			             }
			         
			             if((getParent().getProps().getProperty("separategroup") != null && 
			            		 proddu.getGroup().equals(getParent().getProps().getProperty("separategroup")))
			            	|| (getParent().getProps().getProperty("separategroup2") != null && 
				            		 proddu.getGroup().equals(getParent().getProps().getProperty("separategroup2")))	 
			             ){
			            	 tm.put(proddu.getName(), proddu); 
			            	 pane = true;
			             }
			            
			             
				}
				if(pane){
					
					
						Iterator ite = tm.keySet().iterator();
						while(ite.hasNext()){
							Product pp = (Product)tm.get(ite.next());
							if(pp.getSendSeparate().trim().length() == 0 ){
								pp.setSendSeparate("mobilejava@via3l.ee");	
							}
							
						}
					
					
				}

}
			    
			}

	
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			reader.close();
			fr.close();
			if(getMoveDir() != null)
			fil.renameTo(new File(getMoveDir() + file));
	
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		}
		return tm;
	}

	public Vector getCustomerProductsList(String agentCode) {
		Vector vec = new Vector();

		String[] names = (new File(getDir())).list();
		String file = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcode") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcode")) != -1) {
				file = names[i];
			}
		}

		
		String filemulti = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodesec") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesec")) != -1) {
				filemulti = names[i];
			}
		}
 
		
		
		String filemulti2 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodethird") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodethird")) != -1) {
				filemulti2 = names[i];
			}
		}
		String filemulti3 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodefourth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefourth")) != -1) {
				filemulti3 = names[i];
			}
		}
		
		
		String filemulti4 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodefifth") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodefifth")) != -1) {
				filemulti4 = names[i];
			}
		}
		
		String filemulti5 = null;
		for (int i = 0; i < names.length; i++) {
			if ((names[i]).indexOf("klienditoode") != -1 && getParent().getProps().getProperty("stockcodesixt") != null &&
					(names[i]).indexOf("_" + getParent().getProps().getProperty("stockcodesixt")) != -1) {
				filemulti5 = names[i];
			}
		}
		//System.out.println("filemulti5 " + filemulti5);	
	//System.out.println("filemulti4 " + filemulti4);
	//System.out.println("filemulti3 " + filemulti3);
	//7System.out.println("filemulti2 " + filemulti2);
		if (file != null) {

			try {
				FileReader fr = new FileReader(getDir() + file);
				File fil = new File(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {

					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String custID = st.nextToken().trim();
						if (this.customers.contains(custID)) {
							if (st.hasMoreTokens()) {
								st.nextToken().trim();

								if (st.hasMoreTokens()) {
									String prodID = st.nextToken().trim();
									double sold = Double.parseDouble(st
											.nextToken().trim());
									double retu = Double.parseDouble(st
											.nextToken().trim());
									double last = 0;

									if (st.hasMoreTokens()) {
										last = Double.parseDouble(st
												.nextToken().trim());
									}

									CustProd cp = new CustProd(custID, prodID,
											 sold, retu,last, " ", 0, 0, 0, " ");
									//   Product prod = (Product)getParent().getProducts().get(prodID);
									// if (prod != null){
									vec.addElement(cp);

								}
							}
						}
					}
				}

				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + file));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		if (filemulti != null) {

			try {
				FileReader fr = new FileReader(getDir() + filemulti);
				File fil = new File(getDir() + filemulti);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {

					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String custID = st.nextToken().trim();
						if (this.customers.contains(custID)) {
							if (st.hasMoreTokens()) {
								st.nextToken().trim();

								if (st.hasMoreTokens()) {
									String prodID = st.nextToken().trim();
									double sold = Double.parseDouble(st
											.nextToken().trim());
									double retu = Double.parseDouble(st
											.nextToken().trim());
									double last = 0;

									if (st.hasMoreTokens()) {
										last = Double.parseDouble(st
												.nextToken().trim());
									}

									CustProd cp = new CustProd(custID, prodID,
											 sold, retu,last, " ", 0, 0, 0, " ");
								
									vec.addElement(cp);

								}
							}
						}
					}
				}

				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + filemulti));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		
		
		// teine lisafirma 
		
		if (filemulti2 != null) {

			try {
				FileReader fr = new FileReader(getDir() + filemulti2);
				File fil = new File(getDir() + filemulti2);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				while ((line = reader.readLine()) != null) {

					StringTokenizer st = new StringTokenizer(line, SEPARATOR);
					if (st.hasMoreTokens()) {
						String custID = st.nextToken().trim();
						if (this.customers.contains(custID)) {
							if (st.hasMoreTokens()) {
								 st.nextToken().trim();

								if (st.hasMoreTokens()) {
									String prodID = st.nextToken().trim();
									double sold = Double.parseDouble(st
											.nextToken().trim());
									double retu = Double.parseDouble(st
											.nextToken().trim());
									double last = 0;

									if (st.hasMoreTokens()) {
										last = Double.parseDouble(st
												.nextToken().trim());
									}

									CustProd cp = new CustProd(custID, prodID,
											 sold, retu,last, " ", 0, 0, 0, " ");
						
									vec.addElement(cp);

								}
							}
						}
					}
				}

				fr.close();
				if(getMoveDir() != null)
				fil.renameTo(new File(getMoveDir() + filemulti2));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
			if (filemulti4 != null) {

				try {
					FileReader fr = new FileReader(getDir() + filemulti4);
					File fil = new File(getDir() + filemulti4);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					while ((line = reader.readLine()) != null) {

						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						if (st.hasMoreTokens()) {
							String custID = st.nextToken().trim();
						//	System.out.println( this.customers.contains(custID) + " " + line);
							if (this.customers.contains(custID)) {
								if (st.hasMoreTokens()) {
									 st.nextToken().trim();

									if (st.hasMoreTokens()) {
										String prodID = st.nextToken().trim();
										double sold = Double.parseDouble(st
												.nextToken().trim());
										double retu = Double.parseDouble(st
												.nextToken().trim());
										double last = 0;

										if (st.hasMoreTokens()) {
											last = Double.parseDouble(st
													.nextToken().trim());
										}

										CustProd cp = new CustProd(custID, prodID,
												 sold, retu,last, " ", 0, 0, 0, " ");
									
										vec.addElement(cp);

									}
								}
							}
						}
					}

					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filemulti4));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			if (filemulti5 != null) {

				try {
					FileReader fr = new FileReader(getDir() + filemulti5);
					File fil = new File(getDir() + filemulti5);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					while ((line = reader.readLine()) != null) {
						//System.out.println( line);
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						if (st.hasMoreTokens()) {
							String custID = st.nextToken().trim();
						//	System.out.println( this.customers.contains(custID) + " " + line);
							if (this.customers.contains(custID)) {
							
								if (st.hasMoreTokens()) {
									 st.nextToken().trim();

									if (st.hasMoreTokens()) {
										String prodID = st.nextToken().trim();
										double sold = Double.parseDouble(st
												.nextToken().trim());
										double retu = Double.parseDouble(st
												.nextToken().trim());
										double last = 0;

										if (st.hasMoreTokens()) {
											last = Double.parseDouble(st
													.nextToken().trim());
										}

										CustProd cp = new CustProd(custID, prodID,
												 sold, retu,last, " ", 0, 0, 0, " ");
									
										vec.addElement(cp);

									}
								}
							}
						}
					}

					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filemulti5));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			
			if (filemulti3 != null) {

				try {
					FileReader fr = new FileReader(getDir() + filemulti3);
					File fil = new File(getDir() + filemulti3);
					BufferedReader reader = new BufferedReader(fr);
					String line = null;
					while ((line = reader.readLine()) != null) {
						//System.out.println( line);
						StringTokenizer st = new StringTokenizer(line, SEPARATOR);
						if (st.hasMoreTokens()) {
							String custID = st.nextToken().trim();
						//	System.out.println( this.customers.contains(custID) + " " + line);
							if (this.customers.contains(custID)) {
							
								if (st.hasMoreTokens()) {
									 st.nextToken().trim();

									if (st.hasMoreTokens()) {
										String prodID = st.nextToken().trim();
										double sold = Double.parseDouble(st
												.nextToken().trim());
										double retu = Double.parseDouble(st
												.nextToken().trim());
										double last = 0;

										if (st.hasMoreTokens()) {
											last = Double.parseDouble(st
													.nextToken().trim());
										}

										CustProd cp = new CustProd(custID, prodID,
												 sold, retu,last, " ", 0, 0, 0, " ");
										
										vec.addElement(cp);

									}
								}
							}
						}
					}

					fr.close();
					if(getMoveDir() != null)
					fil.renameTo(new File(getMoveDir() + filemulti3));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		
	
		
		return vec;

	}

	

    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);
      TreeMap ags = new TreeMap();
      Enumeration enums = getParent().getAgentsHash().elements();
	  while (enums.hasMoreElements()) {
	    Agent a = (Agent) enums.nextElement();
	    ags.put(a.getID(),a.getName());
	   }

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
       
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = rs.getString("tekst");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ');
        if (tekst == null) tekst = " ";
        else tekst = tekst.trim();
        sb.append(tekst + sep);

        //Siia peame sama agendi nime 
        sb.append("0;0;" + "nimetu" +  "; ; ; ;0");
        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      Iterator iter = customers.iterator();
      
      StringBuffer bf2 = new StringBuffer();
      while(iter.hasNext()){
      	String resi =(String) iter.next();
      //	System.out.println(resi);
      	Customer cust = (Customer)custMap.get(resi);
      	if(cust != null){
      		
      	TreeMap agents = cust.getAgents();
    Iterator iter2 = agents.values().iterator();
        while(iter2.hasNext()){
        	Agent aa = (Agent)iter2.next();
        	if(!aa.getID().equals("manager")){
        	
        	bf2.append(cust.getID() + sep);
            bf2.append(aa.getID() + sep);
            String agentName = aa.getName();
            if (agentName == null) agentName = " ";
            bf2.append(agentName + sep);
            String email = parent.parseFirstEmail(parent.getEmail(aa.getID()));
            if(email == null || email.trim().length()== 0 )email = " ";
            bf2.append(email + "\n");
        	
        	}
          }
         }	
    	}
        

      md.setClientAgentInfo(bf2.toString());

      return md;
    }

	public String getStartingTime() {
		return null;
	}

	public String getDriverVersion() {
		return "ViaLaima";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public double getSalary(String agentCode) {
		return 0;
	}

	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

	private String getStatDir() {
		String dir = getParent().getProps().getProperty("statdir");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
	private String getMoveDir() {
		String dir = getParent().getProps().getProperty("moveto");
		if (dir != null && (!(dir.endsWith("/") || (dir.endsWith("\\")))))
			dir = dir + "/";
		return dir;
	}

	public static final String SEPARATOR = ";";

	public static final String PRODUCT_FILE = "tooted.txt";

	public static final String AGENT_FILE = "agent.txt";

	public static final String CUSTOMER_FILE = "klient.txt";

	public static final String CUSTOMER_AGENT_FILE = "klagent.txt";

	public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.txt";

	public static final String PRODGROUP_FILE = "tootegrupp.txt";

	public static final String DEBT_FILE = "debt.txt";

	public static final String CUSTOMER_DEBT_FILE = "debt.txt";
	public  HashMap	custMap = new HashMap();
	private String subjectFilesTo = null;
    private String url2 = "jdbc:odbc:report";
	private ArrayList customers = null;
}