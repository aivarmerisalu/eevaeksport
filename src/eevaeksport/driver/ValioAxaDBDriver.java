package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;

/**
 * Title: Eeva eksport Description: Copyright: Copyright (c) 2002 Company:
 * 
 * @author Tarmo Tubro
 * @version 1.0BulkUpdater,Discounter,Manager
 */

public class ValioAxaDBDriver extends AbstractDBDriver implements Manager,
		Promo, BulkUpdater, Discounter {
	public ValioAxaDBDriver(EksportPanel parent) {
		super(parent);
		this.subjectFilesTo = parent.getString("filemsg");
		// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
		String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

		PropertyConfigurator.configure("log4j.properties");
		try {
			Class.forName(databaseDriverName).newInstance();
			con = getNewConnection();
			stmt = con.createStatement();
		} catch (Exception e) {
			parent.showError(e);
			
		}
	}

	/**
   *
   */
	public void connectionUp() {
	}

	public String getSaveDebtInfo() {
		return null;
	}

	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}

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
	  
	
		
		 public TreeMap getDegu() {
				TreeMap result = new TreeMap();
				File f = new File(this.getDir() + "tooteesitlused.xls");
				try {
					FileInputStream ff = new FileInputStream(f);
					HSSFWorkbook workbook = new HSSFWorkbook((ff));
					HSSFSheet sheet = workbook.getSheet("Sheet1");
					HSSFRow row = null;

					// System.out.println(groups.toString());

					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
						row = sheet.getRow(i);
						String custID = "";
						try{
						if (row.getCell((short) 0) != null) {

							try {
								custID = (int) (row.getCell((short) 0)
										.getNumericCellValue()) + "";
							} catch (Exception e) {
								custID = (row.getCell((short) 0).getStringCellValue());
							}
							
		                       if(!custID.equals("0")){
		                    	
							// Siin l�heme reasisse rokkima ja vaatama kas se toode
							// l�heb kaasa
		                    		try {
							   long date = (long) row.getCell((short) 2).getNumericCellValue();
							   
							   GregorianCalendar gc = new GregorianCalendar(1900, Calendar.JANUARY, 1);
							   gc.add(Calendar.DATE,(int) date-2);
							   Calendar  dd = Calendar.getInstance();
							   dd.setTimeInMillis(gc.getTimeInMillis());
							   dd.add(Calendar.DAY_OF_YEAR, -6);
							   Calendar  bb = Calendar.getInstance();
							   bb.setTimeInMillis(gc.getTimeInMillis());
							   bb.add(Calendar.DAY_OF_YEAR, 1);
							   System.out.println(getYYYYpMMpDDpString(gc.getTime()) + " " +bb.getTime().before(new Date()) +
									   " " + getYYYYpMMpDDpString(bb.getTime())
									   + " " + dd.getTime().after(new Date()) + " " + getYYYYpMMpDDpString(dd.getTime()));
										 
						      if(new Date().after(dd.getTime()) && (new Date()).before(bb.getTime()) ){
						    	 
						    	  String text = "Degu: " + getYYYYpMMpDDpString(gc.getTime()) + " " +  row.getCell((short) 3).getStringCellValue() + " " + row.getCell((short) 4).getStringCellValue()+
						    	   "|" +  row.getCell((short) 5).getStringCellValue()  ;
						    	  try{
						    		  text = text +  "|" +  row.getCell((short) 6).getStringCellValue();
						    	  }catch(Exception ee){
						    		  
						    	  }
						    	  System.out.println(custID + " " + text);
						    	  if(result.get(custID)== null){
							    	  result.put(custID,text);  
						    	  }else{
	                               String tt = (String)result.get(custID);
							    	  result.put(custID,tt+ "|" + text); 
						    	  }
						           }
										
		                    		} catch (Exception e) {
		                    		e.printStackTrace();
		    						}
							
		                    		
							
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
	
		 
  private String tananepaev = "day(sortdate) = day(GETDATE()) and  month(sortdate) = month(GETDATE()) and year(sortdate) = year(GETDATE())"	; 
	
	
	public Vector getFixedAssortmentData() {
			Vector result = new Vector();
			File f = new File(this.getDir() + "sortimendid.xls");
			
			
			
			try {
				
				
				  String sql = "SELECT sortdate from mj_assortment where  " + tananepaev;
	        	  boolean insertassort = true;
	        	  Connection con = DriverManager.getConnection(url2,"aivarm", "mobilejava");
	  	        Statement stmt = con.createStatement();
	  	        
	  	        ResultSet rs = stmt.executeQuery(sql);
	  	        if(rs.next()){
	  	        	insertassort = false;
	  	        	//kontrollime kas on vaja insertida 
	  	        }
	  	        System.out.println("Sisestame sortimenti " + insertassort );
				
				
				FileInputStream ff = new FileInputStream(f);
				HSSFWorkbook workbook = new HSSFWorkbook((ff));
				HSSFSheet sheet = workbook.getSheetAt(0);
				HSSFRow row = null;
				TreeMap groups = new TreeMap();
				row = sheet.getRow(3);
				// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

				// System.out.println("rownum " + row.getLastCellNum());
				for (int a = 4; a < row.getLastCellNum(); a++) {
					try {
						// System.out.println(row.getCell((short)a).getStringCellValue());
						groups.put(a + "", row.getCell((short) a)
								.getStringCellValue());
					} catch (Exception e) {
						// System.out.println(row.getCell((short)a).getNumericCellValue());
					}
				}

				 System.out.println(groups.toString());

				for (int i = 5; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String prodID = "";

					if (row.getCell((short) 2) != null) {

						try {
							prodID = (int) (row.getCell((short) 2)
									.getNumericCellValue()) + "";
						} catch (Exception e) {
							prodID = (row.getCell((short) 2).getStringCellValue());
						}
						
	                       if(!prodID.equals("0")){
	                    	
						// Siin l�heme reasisse rokkima ja vaatama kas se toode
						// l�heb kaasa
						for (int a = 4; a < row.getLastCellNum(); a++) {

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
									 if(insertassort){
			                               insertSortLine(mline);   
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

			return result;

		}
		   private Connection getNewCon2() throws SQLException{
		        return DriverManager.getConnection(url2, "aivarm", "mobilejava");
		      } 
		 
		 private void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
		  	  
		  	  
		  	  
		  	 //=10379733,10379733
		  	  //distribution.email.10379733=aivar@mobilejava.ee
		  	  
		    	getProducts(null);
		    	getCustomers(null);
		    	 
		    	  //1.Fikseeritud sortimendi salvestamine baasi juhul kui tava andmetega pole tehtud 
		    	 this.getFixedAssortmentData();
		    	   
		  	  String saajad = getParent().getProps().getProperty("distribution.reporters");
		  	  
		  	  StringTokenizer sto = new StringTokenizer(saajad,",");
		  	  while (sto.hasMoreElements()){
		  		
		  	  String add = "";
		  	  String regno = sto.nextToken();
		  	  String toThem = getParent().getProps().getProperty("distribution.email." + regno);
		  	  
		  	  if(ccc != null){
		  		  Calendar cal = Calendar.getInstance();
		  		  long diff = cal.getTimeInMillis() -ccc.getTimeInMillis();
		  		  cal.setTimeInMillis(diff);
		  		  add = "-" + cal.get(Calendar.DAY_OF_YEAR);
		  	  }
		  	  //2. t�naste lugemiste 
		  	  // M��rame t�nased kehtivad sortimendid nii suurused kui ka sisu
		  	  
		  	   String sql1 = "select sortgroup, count(prodid) as number from mj_assortment where " + tananepaev+ " " + add  +
		             " group by sortgroup";//t�nase sortimendi laiuse leidmine

		  	   String sql2 = "select sortgroup ,prodid  from mj_assortment" +
		  	   		"  where " + tananepaev+ " " + add + " " + 
		           " group by sortgroup,prodid";  // t�nase sortimendi leidmine
		  	   HashMap sortLen = new HashMap();
		  	   HashMap sortIse = new HashMap();
		  	   HashMap tnimed = new HashMap();
		  	   
		  	   try{
		  		   Connection con1 = this.getNewCon2();
		      	   Statement st = con1.createStatement();
		      	   ResultSet rs = st.executeQuery(sql1);
		      	   
		      	   while(rs.next()){
		      		   sortLen.put(rs.getString("sortgroup").trim(),rs.getString("number").trim());
		      	   }
		      	   
		            rs = st.executeQuery(sql2);
		      	   
		      	   while(rs.next()){
		      		   Vector sort = new Vector();
		      		   String sortim = rs.getString("sortgroup").trim();
		      		   String prodid = rs.getString("prodid").trim();
		      		   Product pr = ((Product) prodHash.get(prodid));
		      		   
		      		 //  System.out.println("toode" + prodid + "+" + pr);
		      		   if(pr == null){
		      			   pr = new Product(prodid);
		      			   pr.setEAN(prodid);
		      			   pr.setName(prodid + " nimipuudub");
		      		   }
		      		   String ean = pr.getEAN();
		      		   if(pr.getEAN() == null)ean = "";
		      		   String prname = pr.getName();
		      		   if(prname == null) prname = "Tundmatu toode";
		      		   String prodname = ean + " " + prname;
		      		   tnimed.put(prodid, prodname);	
		      		   if(sortIse.get(sortim) != null){
		      			   sort = (Vector)sortIse.get(sortim);
		      			   sort.add(prodid);
		      		   }else{
		      			   sort.add(prodid);
		      			   sortIse.put(sortim,sort);  
		      		   }
		      		   
		      	   }
		      	   
		      int counter = 0;	   
		      	   //L�pes sortimentide sisselugemine
		      	   
		      	   // vaja leida mitu toodet oli m�rgitud kontrolli ajal kohale 
		      	   //ja ,m��rame kes k�is mis kaupluses ja kellaaeg
		      	   
		      Calendar ccw = Calendar.getInstance();
		      if(ccw.get(Calendar.DAY_OF_MONTH)== 1){
		    	  ccw.add(Calendar.DAY_OF_MONTH, -1);
		    	  int maxdays = ccw.getMaximum(Calendar.DAY_OF_MONTH);
		    	  ccw.add(Calendar.DAY_OF_MONTH, - maxdays);
		      }else{
		    	  ccw.add(Calendar.DAY_OF_MONTH, -7);
		      }
		     
		String sql3 = "select prodid ,sup.custid,sup.custname, sup.sentdate as date," +
		     	" rtrim(sup.agentid) as agid, rtrim(sup.agentname) as agent ,sup.resstr3 as time " +
		      " from supervisor sup  " +
		      " where  prodid not like 'di%' and sup.resstr3 >'" + ccw.getTimeInMillis() + "'" + // sup.sentdate > current_date()-8 
		      		"order by sup.sentdate desc";
		      	   HashMap visited = new HashMap();
		      	   HashMap names = new HashMap();
		      	   HashMap agen = new HashMap();
		      	   HashMap sorti = new HashMap();
		      	   HashMap times = new HashMap();
		      	   HashMap visitedcoun = new HashMap();
		      	   Vector days = new Vector();
		             HashMap puuduvad = new HashMap(); 
		             rs = st.executeQuery(sql3);
		             
		           Vector kliendid = (Vector)ketid.get(regno);
		           System.out.println("kliendid " + kliendid);

		while(rs.next()){
			   Vector sort = new Vector();
			   String prodid = rs.getString("prodid").trim();
			
			   String custid = rs.getString("custid").trim();
			   String sortim = (String)sortimendid.get(custid);
			   String custname = rs.getString("custname").trim();
			   String date = rs.getString("date").trim().substring(0,10);
			   String agid =  rs.getString("agid").trim();
			   String agent = rs.getString("agent").trim();
			   String time = rs.getString("time");
			  // System.out.println("ahaa " + custid );
			 if(kliendid.contains(custid)){
				
			   if(time != null && time.trim().length() != 0){
				   long lll = Long.parseLong(time.trim());
				   Calendar cc = Calendar.getInstance();
				   cc.setTimeInMillis(lll);
				   time = getYYYYpMMpDDpTimeNiceString(cc.getTime());
				   date = time.substring(0,10);
				   if(!days.contains(date)){
						  days.addElement(date);
					  }
			   }else{
				   if(!days.contains(date)){
						  days.addElement(date);
					  }
				   time = date + " 00:00:00";
			   }
			   	
			   agen.put(custid, agid + " " + agent);
			   names.put(custid, custname);
			   sorti.put(custid, sortim);
			   times.put(custid+date, time);

			   if(visitedcoun.get(custid) != null){
				   Vector dddd = (Vector)visitedcoun.get(custid);
				   if(!dddd.contains(date))dddd.add(date);
				   visitedcoun.put(custid,dddd);  
			   }else{
				   Vector dddd = new Vector();
				   dddd.addElement(date);
				   visitedcoun.put(custid,dddd);  
			   }	   

			   
			   if(sortIse.get(sortim) != null){
				   /*
				    * siin on juhtum kus m�rgitakse mis oli myygil
				   Vector res = (Vector)sortIse.get(sortim);
					   if(puuduvad.get(custid + date) == null){
						   counter++;
		            Vector puud = (Vector)res.clone();
		                 puud.removeElement(prodid);
						   puuduvad.put(custid + date,puud);    
					   }else{
						   Vector puud = (Vector) puuduvad.get(custid + date);
				           puud.removeElement(prodid);
						   puuduvad.put(custid + date,puud);   
					   } 
					   
					   */
					   
				   //siin on juhtum kus m�rgitakse OOS -i
					   Vector res = (Vector)sortIse.get(sortim);
					   if(puuduvad.get(custid + date) == null){
						   counter++;
		            Vector puud = new Vector();
		                 puud.add(prodid);
						   puuduvad.put(custid + date,puud);    
					   }else{
						   Vector puud = (Vector) puuduvad.get(custid + date);
				           puud.add(prodid);
						   puuduvad.put(custid + date,puud);   
					   }  
				  
			   }
			   if(visited.get(custid+date) != null){
				   sort = (Vector)visited.get(custid+date);
				   sort.add(prodid);
				   visited.put(custid+date,sort);  
			   }else{
				   sort.add(prodid);
				   visited.put(custid+date,sort);  
			   }	   
			   
			 }
			 } 


		StringBuffer sb = new StringBuffer();

		StringBuffer sbf = new StringBuffer();

		Iterator ite = names.keySet().iterator();
		int total = 0;
		double perc = 0;
		while (ite.hasNext()){

			String custid = (String)ite.next();
			
			String sortcode = (String)sorti.get(custid);
			sb.append("Klient :\t" + names.get(custid) +  "\n");
			sb.append("Kontrollis:\t" + agen.get(custid) +  "\n");
			
			sbf.append("Klient :," + names.get(custid) +  "\n");
			sbf.append("Kontrollis:," + agen.get(custid) +  "\n");
			
			
			Enumeration ed =  ((Vector)visitedcoun.get(custid)).elements();
			while(ed.hasMoreElements()){
				total++;
				String date = (String)ed.nextElement();
				sb.append("Aeg :\t\t" + times.get(custid + date).toString().substring(0,19) +  "\n");	
				sbf.append("Aeg :," + times.get(custid + date).toString().substring(0,19) +  "\n");	
			

				double sss = Double.parseDouble((String)sortLen.get(sortcode));
			
				double mmm = sss - Double.parseDouble(((Vector)visited.get(custid+date)).size()+ ""); 
				// sss - annab vastupidise tulemuse nad m�rgivad OOS
				double nnn = mmm/sss*100d;	
				int per = (int)(nnn);
				perc = perc + nnn;	
				sb.append("Sortimendis " + (int)sss + " / M��gil " + (int)mmm + " / " + per + "% \n\n");
				sbf.append("Sortimendis, M��gil ,% \n");
				sbf.append((int)sss + "," + (int)mmm + "," + per + "% \n\n\n\n");
			
			}
			    sb.append("\n\n");
			
			

		}


		//Sortimendi v�rdlus  puuduolevad tooted ja v�ljaspool olevad tooted


		StringBuffer lisa = new StringBuffer();
		TreeMap tootedPoed = new TreeMap();
		StringBuffer lisaf = new StringBuffer();
		System.out.println("puuduvad " + puuduvad.keySet());
		Iterator itera = puuduvad.keySet().iterator();
		boolean first = true ;
		while (itera.hasNext()){
			String id = (String)itera.next();
			System.out.println(id);
			String paev = id.substring(id.length() - 10);
		   String custid = id.substring(0,id.length() - 10);
		   System.out.println(custid + "ee" + paev);
		   if(first){
				lisa.append("Lisainfo Puuduste kohta\n");
				lisaf.append("Lisainfo Puuduste kohta\n");
				first = false;
			}
			Vector vvv = (Vector)puuduvad.get(id);
			lisa.append(names.get(custid) + " " + paev + "\nPUUDU: " + vvv.size() + " toodet\n");
			lisaf.append(names.get(custid) + "," + paev + "\nPUUDU:," + vvv.size() + "\n");
			Enumeration enu = vvv.elements();
			while(enu.hasMoreElements()){
				String prodid = (String) enu.nextElement();
				lisa.append("\t" + prodid + " " + tnimed.get(prodid) + "\n");	
				lisaf.append(prodid + "," + tnimed.get(prodid) + "\n");
				if(tootedPoed.get(prodid + " " +  tnimed.get(prodid)) != null){
					Vector kli= (Vector)tootedPoed.get(prodid + " " +  tnimed.get(prodid));
					kli.addElement(names.get(custid));
					tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);
				}else{
					Vector kli= new Vector();
					kli.addElement(names.get(custid));
					tootedPoed.put(prodid + " " +  tnimed.get(prodid), kli);	
				}
			}
			lisa.append("\n\n");
			lisaf.append("\n\n");
		}
			

		StringBuffer toodePood = new StringBuffer();

		Iterator iteri = tootedPoed.keySet().iterator();
		while (iteri.hasNext()){
			String toode = (String)iteri.next();
			toodePood.append(toode + "\n");
			
			Vector vvv = (Vector) 	tootedPoed.get(toode);
			toodePood.append("puudub " + vvv.size() + " poes\n");
			
		 Enumeration enumi = 	vvv.elements();
		 while (enumi.hasMoreElements()){
			 toodePood.append("\t"  + enumi.nextElement() + "\n");
		 }
		 toodePood.append("\n\n");
		}


		if(total == 0 ){
			total = 1;
		}
		String text = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
		+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";
		String textf = "Kontrollperiood : " + days.toString() + "\nKontrollimise kordade arv  : " + counter 
		+"\nKeskmiselt Kauplustes Saadavus " + (int)perc/total + "%\n\n";

		System.out.println(text + "\n" + sb.toString() + lisa.toString()+ "\n" + toodePood );


		Calendar ctc = Calendar.getInstance();

		String filename = "dreports/" +"DR-VAL-" + getYYYYpMMpDDpTimeNiceString(ctc.getTime()).substring(0, 10) + regno + ".csv";  

		   File f = new File(filename);
		   if (f.exists())f.delete();
		     try {
				PrintWriter os = new PrintWriter(new FileWriter(
						filename, true));
				os.write(textf + "\n" + sbf.toString() + "\n" + lisaf.toString());
				os.close();
			  }catch (IOException ex) {
				ex.printStackTrace();
			  }

		Email email = new Email(getParent().getString("datasent"));

		//email.setNoticeTo(parseSecondEmail(getEmail(agentCode)));
		email.setFrom(getParent().getProps().getProperty("from"));
		email.setSMTP(getParent().getProps().getProperty("smtphost"));
		email.setUser(getParent().getProps().getProperty("smtpuser"));
		email.setPass(getParent().getProps().getProperty("smtppass"));
		email.setPort(getParent().getProps().getProperty("smtpport"));
		email.setSubjectFilesTo("Distributsiooni aruanne - Valio Eesti AS klient " + regno);
		email.setBody(text + "\n" + sb.toString() + "\n" + lisa.toString() + "\n"+ toodePood.toString());
		email.addAttach(filename);
		System.out.println("Saadan Emaili");
		//iseendale saatmine
		String toOwn = getParent().getProps().getProperty("distribution.ownemail");
		email.setFilesTo(toOwn);
		email.send();

		email.setFilesTo(toThem);
		email.send();
		      	   
		  	   }catch(Exception e){
		  		   e.printStackTrace();
		  	   }
		  	   
		  	   
		  	  }
		  	  System.exit(0);
		    }

		    private String getYYYYpMMpDDpTimeNiceString(Date dd){
			    StringBuffer sb = new StringBuffer();
			    Calendar cal= Calendar.getInstance();
			    cal.setTime(dd);
			    
			    
			    
			    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
			    
			    
				String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
				if (day.length() == 1) {day = "0" + day;}

				String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
				if (month.length() == 1){month = ("0") + month;}

				sb.append("" + cal.get(Calendar.YEAR));
				sb.append("-" + month  + "-");
				sb.append(day +" ");
			    
			    String min = String.valueOf(cal.get(Calendar.MINUTE));
			    String sec = String.valueOf(cal.get(Calendar.SECOND));
			     if (hour.length() == 1){
			      sb.append("0");
			    }
			     sb.append(hour);
			     sb.append("_");
			    if (min.length() == 1){
			     sb.append("0");
			   }
			     sb.append(min);
			     sb.append("_");
			     if (sec.length() == 1){
			        sb.append("0");
			      }
			    sb.append(sec);
			    return sb.toString();
			  }



		 
		    private void  insertSortLine(MatrixLine mline){
				  try{
						
					      String sql = "INSERT INTO mj_assortment values (" +
					         "'" + mline.getCustID() + "'," + 
					         "GETDATE()," + 
					         "'" + mline.getCliGroup() + "'," + 
					         "'" + mline.getProdID() + "'," + 
					         "0," + 
					         "0,'')" ;
					  
					  	     Connection  con2 = DriverManager.getConnection(url2,"aivarm", "mobilejava");;      
						     Statement stmt2 = con2.createStatement();
						     stmt2.executeUpdate(sql);
				  }catch(Exception e){
					  e.printStackTrace();
				  }
			
				  
			  }
		    
		 

	public Vector getPriceMatrixData() {
		Vector lines = new Vector();
		String sql = "SELECT (case when accountcode = 0 then accountrelation else ''end) AS custid, " +
			"	(case when accountcode = 1 then accountrelation else ''end)  AS clgroup, " +
			"	(case when itemcode = 0 then itemrelation else ''end) AS prodid, " +
			"	(case when itemcode = 1 then itemrelation else ''end) AS prodgroup,percent1 AS discount,amount " +
			"	 AS price ,fromdate , todate FROM pricedisctable where " +
			"	 fromdate<getdate() and todate>getdate() AND " +
			"	 fromdate!=('1900-01-01 00:00:00.00') and dataareaid = 'liv' order by (case when accountcode = 0 then accountrelation else ''end),fromdate";
		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				String custID = rs.getString("custid");
				String clGroup = rs.getString("clgroup");
				String prodID = rs.getString("prodid");
				String prodGroup = rs.getString("prodgroup");
				double discount = rs.getDouble("discount");
				double price = rs.getDouble("price");
				String sdate =  rs.getString("fromdate");
				String tdate =  rs.getString("todate");
				MatrixLine mline = new MatrixLine();

				if (custID != null && custID.trim().length() > 0) {
					mline.setCustID(custID.trim());
				}
				
				if (tdate != null) {
					mline.setEndDate(tdate.substring(0, 10));
				}
				
				if (sdate != null) {
					mline.setStartDate(sdate.substring(0, 10));
				}
				
				if (mline.getCustID() == null && clGroup != null
						&& clGroup.trim().length() > 0) {
					mline.setCliGroup(clGroup.trim());
				}
				if (prodID != null && prodID.trim().length() > 0) {
					mline.setProdID(prodID.trim());
				}
				if (prodGroup != null && prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup.trim());
				}
				if (mline.getProdID() == null && prodGroup != null
						&& prodGroup.trim().length() > 0) {
					mline.setProdGroup(prodGroup);
				}
				mline.setDiscount(discount);

				if (price != 0) {
					mline.setPrice(price);
				}
				lines.add(mline);
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return lines;
	}

	public String getProductSortingData() {
		return null;
	}

	public Vector getDebt(String agentCode) throws SQLException {
		return this.debt;
	}

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAssortmentStr(Agent agent) {
		return null;
	}

	public String getSaveStockInfo() {
		sendDistributionReport(null);
    	System.exit(0);
		return null;
	}

	public String getDealerCustomerCSV(String agent) {
		return null;
	}

	public String getCustProdDiscounts(Agent agent) {
		return null;
	}

	/**
   *
   */
	public void connectionDown() {
	}

	public TreeMap getAgents() {
		String cmd = "select emplid as kood ,name from empltable  where dataareaid='liv' and dimension = '104'";
		if (isDebug())
			System.out.println(cmd);
		TreeMap agents = new TreeMap();
		try {
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				Agent agent = new Agent(rs.getString("kood").trim());
				String name = rs.getString("name").trim();
				agent.setName(name);
				agents.put(name, agent);
			}
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		 Agent aaa = new Agent("PAIG");
		 aaa.setName("Paigutaja");
		 agents.put("Paigutaja", aaa);
		 
		// getFixedAssortmentData();
		return agents;
	}

	public TreeMap getCustomers(Agent agent) {
		TreeMap degu = getDegu();
		
		String cmd = "SELECT cus.name, cus.accountnum,(case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end )AS payer,   "+
				" (SELECT sum(amountmst/( case when year(transdate) >2010 then 1 else 15.64666 end )) FROM custtransopen co WHERE co.duedate<getdate() " +
				 "   AND co.accountnum = (case when len(cus.invoiceaccount)=0 then cus.accountnum else cus.invoiceaccount end ) and dataareaid ='liv') AS debt,custvendregnum_ee as regnr, cus.linedisc , " +
			     "    cus.address, visitingdays AS visit , cus.salesmanager AS agent ,paymtermid AS term,  blocked AS block , custinventsort as grupp  , " +
					"  (select rtrim(cp.name) + ' ' + rtrim (cp.phone) + ' ' + rtrim(cus.phone) from contactperson cp where cp.contactpersonid=cus.contactpersonid and cp.dataareaid ='liv'   ) as kontakt " + 
				   " FROM custtable cus WHERE  cus.dataareaid='liv'  and cus.salesmanager != '' " +
			    " AND blocked != 2  order by cus.name";

		String sql = "SELECT  a.accountnum AS klient,a.invoice AS number,co.duedate AS maksepaev," +
				"co.amountmst/( case when currencycode='EEK' then 15.64666 else 1 end ) AS tasumata, "
				+ "co.transdate AS kuupaev , datediff(day,co.duedate,getdate()) AS yle FROM custtransopen co" +
						" INNER JOIN custtrans a ON co.REFRECID=a.recid and a.dataareaid=co.dataareaid "
				+ "WHERE co.dataareaid='liv' AND a.dataareaid='liv' and  co.duedate < getdate() -5 ORDER BY a.accountnum,co.transdate ";


		if (isDebug())
			System.out.println(cmd);
		TreeMap customers = new TreeMap();
		TreeMap custMap = new TreeMap();
		Vector showSup = new Vector();
	    ketid = new HashMap();
	    sortimendid = new HashMap();
		try {
			  Connection con3 = getNewConnectionRep();
			  String sqll = "select custid from supervisor where prodid like 'di%' " +
			  		" and year(sentdate)= year(getdate()) and MONTH(sentdate)= MONTH(getdate()) group by custid";
			  
			  Statement stt = con3.createStatement();
			  ResultSet rrr = stt.executeQuery(sqll);
			  while(rrr.next()){
				  showSup.add(rrr.getString("custid").trim());
			  }
			  
			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String name = rs.getString("name").trim();
				String accountnum = rs.getString("accountnum").trim();
				Customer cust = new Customer(accountnum);
				 cust.setSupervisormustfill(true);
				if(showSup.contains(accountnum)){
					cust.setSupervisorFilled(true);
					 cust.setSupervisormustfill(false);
				}
				cust.setName(name);
				String payer = rs.getString("payer");
				if(payer != null && payer.trim().length() != 0){
					cust.setPayer(payer.trim());
					
					String debtStr = rs.getString("debt");
					
					
					if (debtStr != null) {
						
						if(!payer.trim().equals("102") 
								 && !payer.trim().equals("100")
								 && !payer.trim().equals("104")
								 && !payer.trim().equals("103")
								 && !payer.trim().equals("106")
								 && !payer.trim().equals("108") 
								 && !payer.trim().equals("107")
								 && !payer.trim().equals("101")
								 && !payer.trim().equals("109") 
								){
							      cust.setDebt(Double.parseDouble(debtStr));
							}
						
					}
				}
				
				String regnum = rs.getString("regnr");
				if(regnum != null && regnum.trim().length() != 0){
					if(ketid.get(regnum.trim()) != null){
	            		Vector vv = (Vector)ketid.get(regnum.trim());
	            		vv.add(cust.getID());
	            		ketid.put(regnum.trim(), vv);
	            	}else{
	            		Vector vv = new Vector();
	            		vv.add(cust.getID());
	            		ketid.put(regnum.trim(), vv);
	            	}	
				}
				

				String aleke = rs.getString("linedisc");
				if (aleke != null && aleke.trim().length() > 0) {
					aleke = aleke.trim();
					cust.setGroup(aleke);
				} else {
					aleke = "000";
				}
				
				String inf = null;
				if(degu.containsKey(accountnum)){
					inf = (String) degu.get(accountnum);
					cust.setInfo(inf);
				}

				String aadr = rs.getString("address");
				if (aadr != null) {
					cust.setAddress(aadr.trim().replace('\n', ' '));
					// System.out.println(aleke +aadr.replace('\n',' '));
				}
				String visit = rs.getString("visit");
				if (visit != null && visit.trim().length() > 0) {
					cust.setVisit(visit.trim());
				}
				String agen = rs.getString("agent");
				if (agen != null) {
					cust.addAgent(new Agent(agen.trim()));
				}
				  cust.addAgent(new Agent("0088"));
			      cust.addAgent(new Agent("0078"));
				      if(payer.trim().equals("102") 
				         || payer.trim().equals("100")
						 || payer.trim().equals("104")
						 || payer.trim().equals("103")
						 || payer.trim().equals("106")
						 || payer.trim().equals("108") 
						 || payer.trim().equals("107")
						 || payer.trim().equals("101")
						 || payer.trim().equals("109") 
						){
					   cust.addAgent(new Agent("PAIG"));
					}
				    
				int block = rs.getInt("block");
				if (block == 1) {
					cust.setSalesBlock(true);
				}

				String grupp = rs.getString("grupp");
				if (grupp != null && grupp.trim().length() != 0) {
					cust.setGroup(grupp);
					sortimendid.put(cust.getID(), grupp);
				}

				String kontakt = rs.getString("kontakt");
				if (kontakt != null && kontakt.trim().length() != 0) {
					cust.setPersonalData(kontakt.replaceAll(";", ",").replaceAll("  ", " "));
				}

				
				customers.put(name, cust);
				custMap.put(cust.getID(), cust);
			}

			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Debt debt = new Debt();
				debt.setCustID(rs.getString("klient").trim());
				String number = rs.getString("number").trim();
				debt.setDoknr(number);
				debt.setDuedate(rs.getDate("maksepaev"));
				debt.setSum(rs.getDouble("tasumata"));
				debt.setDate(rs.getDate("kuupaev"));
				debt.setDue(rs.getString("yle"));
				if(!debt.getCustID().equals("102") 
						&& !debt.getCustID().equals("100") 
						 && !debt.getCustID().equals("106")
						  && !debt.getCustID().equals("104")
						   && !debt.getCustID().equals("103")
						 && !debt.getCustID().equals("108") 
						 && !debt.getCustID().equals("107")
						 && !debt.getCustID().equals("101")
						 && !debt.getCustID().equals("109") 
						){
					this.debt.addElement(debt);	
				}
			}

			
			  String sql2 = "select clid AS kood ,sentdate as kuup,agentnam AS agent , mestext " +
		  		"AS mess from mj_crm where sentdate > getdate()-60 order by clid,sentdate";
			  
			
			  
	       stt = con3.createStatement();
		  ResultSet rr = stt.executeQuery(sql2);
		  StringBuffer sb = new StringBuffer();
		  while (rr.next()){
		  	sb.append(rr.getString("kood").trim() + ";" + rr.getString("kuup").trim() + " " + rr.getString("agent").trim()
		  			+ "|" + rr.getString("mess").trim() + "\n");
		  }
		  
		  if(sb.length() > 2){
		      File f = new File(getDir() + "customercrm.txt");
		      if (f.exists())f.delete();
		        try {
		  		PrintWriter os = new PrintWriter(new FileWriter(
		  				getDir() + "customercrm.txt", true));
		  		os.write(sb.toString());
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

	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	private Connection getNewConnectionRep() throws SQLException {
		return DriverManager.getConnection(url2, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}

	public String getDistribution() {
		return null;
	}

	public TreeMap getProdGroups() throws SQLException {

		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = " select gr.articleclass as liik ,gr.name AS nimi from "
				+ "articleclass gr inner join inventtable t on t.articleclasstype=gr.articleclass and t.dataareaid=gr.dataareaid" +
						"  WHERE t.dataareaid='liv' and gr.dataareaid='liv' "
				+ "group by gr.articleclass,gr.name order by  gr.name";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
		// SELECT talROUTETYPEID AS liik, talROUTETYPEID AS nimetus FROM
		// inventtable WHERE dataareaid='liv' AND talROUTETYPEID!='' GROUP BY
		// talROUTETYPEID

		while (rs.next()) {
			String liik = rs.getString("liik").trim();
			tm.put(liik, rs.getString("nimi").trim());
		}
		rs.close();
		st.close();
		con.close();

		return tm;
	}

	public TreeMap getProdGroupsSecond() throws SQLException {

		Connection con = getNewConnection();
		TreeMap tm = new TreeMap();
		String sql = " select brand from inventtable where brand!='' and dataareaid='liv' group by brand ";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		while (rs.next()) {
			String liik = rs.getString("brand").trim();
			tm.put(liik, liik);
		}
		rs.close();
		st.close();
		con.close();

		return tm;
	}

	public TreeMap getProducts(Agent agent) {

		String cmd = "SELECT inv.itemid,(inv.itemname) AS nimetus, taxpackagingqty AS kastis,    price "
				+ ", itembarcode AS ean,articleclasstype AS grupp ,brand AS grupp2,unitid AS unit, 0 AS yhikukogus FROM inventtable inv "
				+ "  INNER JOIN inventtablemodule mod ON mod.itemid = inv.itemid and mod.dataareaid=inv.dataareaid  "
				+ "  LEFT JOIN inventitembarcode bc on bc.itemid=inv.itemid and bc.dataareaid=inv.dataareaid "
				+ "  WHERE   inv.dataareaid='liv' AND mod.dataareaid='liv' AND moduletype=2 "
				+ "  AND len(inv.itemname)>0   AND blocked = 0 and price !=0 and brand != '' "
				+ "	 and inv.itemid not like 'R%'  and inv.itemid not like 'LV%' and inv.itemid not like 'F%' " 
				+ "  and inv.itemid not like 'PP%' and inv.itemid not like 'uus%' order by inv.itemid";

		
		String cmd2 = " SELECT summ.itemid AS toode , SUM(AVAILPHYSICAL) AS stock FROM INVENTSUM summ "
				+ "INNER JOIN INVENTDIM dim ON summ.inventdimid=dim.inventdimid and dim.dataareaid=summ.dataareaid  "
				+ "INNER JOIN inventtablemodule inv ON inv.itemid=summ.itemid and inv.dataareaid=summ.dataareaid "
				+ "WHERE summ.DATAAREAID='liv'and dim.DATAAREAID='liv' and inv.DATAAREAID='liv' AND CLOSED = 0 AND  moduletype=0 "
				+ "AND  rtrim(dim.inventlocationid)  IN (SELECT inventlocationid FROM INVENTDIM group by inventlocationid ) GROUP BY summ.itemid "
				+ "having SUM(AVAILPHYSICAL) !=0 ORDER BY summ.itemid ";

		if (isDebug())
			System.out.println(cmd);
		if (isDebug())
			System.out.println(cmd2);
		TreeMap products = new TreeMap();
		 prodHash = new HashMap();
		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
		
			while (rs.next()) {
				String kood = rs.getString("itemid").trim();
				Product prod = new Product(kood);
				String nimetus = "    " + rs.getString("nimetus").trim();

				prod.setInCases(rs.getDouble("kastis"));
				prod.setPrice(rs.getDouble("price"));

				String ean = rs.getString("ean");
				if (ean != null && ean.trim().length() > 0) {
					prod.setEAN(ean);
				}

				String grupp = rs.getString("grupp");
				if (grupp != null && grupp.trim().length() > 0) {
					prod.setGroup(grupp);
					nimetus = "    " +grupp.substring(0, 3) + " " + nimetus.trim();
				}

				prod.setName(nimetus);

				String grupp2 = rs.getString("grupp2");
				if (grupp2 != null && grupp2.trim().length() > 0) {
					prod.setGroupSecond(grupp2);
				}

				String unit = rs.getString("unit");
				if (unit != null && unit.trim().length() > 0) {
					prod.setUnit(unit);
				}

				// prod.setInUnit(rs.getDouble("yhikukogus"));

				products.put(nimetus, prod);
				prodHash.put(prod.getID(), prod);

			}
			rs.close();
			ResultSet rs1 = stmt.executeQuery(cmd2);
			while (rs1.next()) {
				String id = rs1.getString("toode").trim();
				Product prod1 = (Product) prodHash.get(id);
				if (prod1 != null) {
					prod1.setStock(rs1.getDouble("stock"));
				}
			}

		} catch (Exception e) {
			getParent().showError(e);
		}

		return products;
	}

	public Vector getCustomerProductsList(String agentCode) {
		Vector custProds = new Vector();
		HashMap custs = new HashMap();
		int counter = 1;
		String cmd = "SELECT max(itemcustomervalidity) as life ,orderaccount as customer,tra.itemid ,tra.qty,jou.invoicedate AS lastdate , "
				+ "	(case when jou.invoicedate   < getdate()-max(itemcustomervalidity) then 1 else 0 end ) as date "
				+ " FROM custinvoicetrans  tra INNER JOIN custinvoicejour jou ON tra.invoiceid=jou.invoiceid and tra.dataareaid=jou.dataareaid " +
						"INNER JOIN inventTABLE cus ON cus.itemid=tra.itemid and cus.dataareaid=tra.dataareaid  "
				+ "  WHERE  jou.invoicedate > getdate()-30 AND tra.dataareaid ='liv'  AND jou.dataareaid = 'liv'  AND cus.dataareaid = 'liv' and articleclasstype!=''  "
				+ "  GROUP BY orderaccount,tra.itemid ,jou.invoicedate ,tra.qty order BY orderaccount,tra.itemid ,jou.invoicedate desc  ";

		if (isDebug())
			System.out.println(cmd);

		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			counter++;
			while (rs.next()) {
				double life = rs.getDouble("life");

				String kliendikood = rs.getString("customer").trim();

				String tootekood = rs.getString("itemid").trim();
				if (!custs.keySet().contains(kliendikood + tootekood)) {

					double kogus = rs.getDouble("qty");
					String lastdate = rs.getString("lastdate").substring(0, 10);
					lastdate = lastdate.substring(0, 10);
					custs.put(kliendikood + tootekood, lastdate);
					lastdate = lastdate.substring(5, 10);

					CustProd cp = new CustProd(kliendikood, tootekood, kogus,
							0, 0, lastdate, 0, 0, 0, " ");
					if (life > 0) {
						cp.setRedline(rs.getString("date"));
					}
					custProds.add(cp);
				}

			}

		} catch (Exception e) {
			getParent().showError(e);
		}
		return custProds;
	}

	public double getSalary(String agentCode) {
		setSales(0);
		return 0;
	}

	public void setBulkSales(Map agents) {
		this.connectionUp();
		String cmd = "  SELECT  cus.salesmanager as agent,round(SUM(lineamount),0) AS summa "
				+ " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid and sal.dataareaid=inv.dataareaid  " +
						"INNER JOIN custinvoicejour jou ON "
				+ " sal.invoiceid=jou.invoiceid and jou.dataareaid=sal.dataareaid " +
						" INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and cus.dataareaid=jou.dataareaid  "
				+ "  WHERE  inv.dataareaid='liv' AND sal.dataareaid='liv' AND cus.dataareaid='liv'AND YEAR(sal.invoicedate) = YEAR(getdate()) "
				+ " AND MONTH(sal.invoicedate) = MONTH(getdate()) "
				+ " GROUP BY cus.salesmanager";

		if (isDebug())
			System.out.println(cmd);
		try {
			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String agentID = rs.getString("agent").trim();
				Agent agent = (Agent) agents.get(agentID);
				if (agent != null) {
					agent.setMonthSales((int) rs.getDouble("summa"));
					// System.out.println(agent.getMonthSales());
				}
			}
			rs.close();
			con.close();
		} catch (Exception e) {
			getParent().showError(e);
		}
	}

	public double getFoodSales(String agentCode) throws SQLException {
		return 0;
	}

	public double getAlcoSales(String agentCode) throws SQLException {
		return 0;
	}

	public ManagerData getManagerData() throws SQLException {
		String sql = "SELECT clid AS klient,enddate AS kuupaev ,docsum AS summa,linecount AS ridu,text AS tekst "
				+ "FROM dayreport WHERE enddate>(getdate()-30) ORDER BY clid,enddate desc";
		this.connectionUp();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);

		String sql2 = "  SELECT accountnum AS kood, 0 AS aastamyyk,0 AS kuumyyk,e.name AS agendinimi,' ' AS kontaktisik "
				+ ",k.phone AS telefon, (case when left(paymtermid,1)='N' then right(paymtermid,len(paymtermid)-1)else paymtermid end )"
				+ " AS maksetin,0 AS ale "
				+ "FROM custtable k INNER JOIN empltable e ON emplid=talsalesman "
				+ "WHERE e.dataareaid='liv' AND k.dataareaid='liv' ORDER BY accountnum";

		// siin tuleks leida kuu ja aasta myyk veel eraldi ja COTACT PERSON
		// tabeli asi ka t��le panna

		Connection con2 = getNewConnection();
		Statement st2 = con2.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = st2.executeQuery(sql2);

		StringBuffer sb = new StringBuffer();
		String sep = ";";
		while (rs.next()) {
			String cliID = rs.getString("klient").trim();
			sb.append(cliID + sep);
			sb.append(rs.getString("kuupaev").trim().substring(0, 10) + sep);
			sb.append(rs.getDouble("summa") + sep);
			sb.append(rs.getInt("ridu") + sep);
			String tekst = rs.getString("tekst");
			tekst = tekst.replace('\'', ' ').replace('\"', ' ');
			if (tekst == null)
				tekst = " ";
			else
				tekst = tekst.trim();
			sb.append(ensureLen(tekst, " ") + sep);

			boolean has = scrollTo(cliID, rs2);
			if (has) {
				sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
				sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
				sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
				sb.append(ensureLen(rs2.getString("telefon"), " ") + sep);
				sb.append(ensureLen(
						rs2.getString("maksetin").replaceAll("SR", "1"), " ")
						+ sep);
				sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
			}

			sb.append("\n");

		}
		con.close();
		con2.close();
		ManagerData md = new ManagerData();
		md.setClientVisitInfo(sb.toString());

		sql = " SELECT k.accountnum AS klient,max(d.talsalesman) AS agent,max(e.name) AS agentname "
				+ " FROM custtable k INNER JOIN empltable e ON emplid=k.talsalesman INNER JOIN custinvoicejour d ON k.accountnum=d.orderaccount "
				+ " and k.talsalesman=d.talsalesman "
				+ " WHERE e.dataareaid='liv' AND k.dataareaid='liv' AND d.dataareaid = 'liv' AND blocked = 0 AND d.invoicedate > getdate()-30 "
				+ " GROUP BY k.accountnum  ORDER BY accountnum ";
		Connection con3 = this.getNewConnection();
		Statement ps = con3.createStatement();
		rs = ps.executeQuery(sql);
		StringBuffer bf2 = new StringBuffer();
		while (rs.next()) {
			bf2.append(rs.getString("klient").trim() + sep);
			bf2.append(rs.getString("agent").trim() + sep);
			String agentName = rs.getString("agentname");
			if (agentName == null)
				agentName = "";
			bf2.append(agentName + "\n");
		}

		md.setClientAgentInfo(bf2.toString());
		con.close();
		return md;
	}

	public double getPromoSales(String agentCode, int nr) throws SQLException {
		return 0;
		/*
		 * String sql =
		 * "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN "
		 * + "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
		 * " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?"
		 * ; PreparedStatement pst = con.prepareStatement(sql); pst.setString(1,
		 * agentCode); //Calendar cal = Calendar.getInstance();
		 * //cal.set(Calendar.DAY_OF_MONTH, 1); //pst.setTimestamp(2, new
		 * Timestamp(cal.getTime().getTime())); pst.setString(2,
		 * String.valueOf(nr)); ResultSet rs = pst.executeQuery(); double total
		 * = 0; while(rs.next()){ double kogus = rs.getDouble("kogus"); String
		 * sisu = rs.getString("sisu"); if (!sisu.equals("")){ total +=
		 * kogus*Double.parseDouble(sisu); } } rs.close(); pst.close();
		 * System.out.println("nr:" + nr + ", total:" + total); return total;
		 */
	}

	public String getStartingTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,
				-Integer.parseInt(getParent().getProps().getProperty("months")));
		int month = cal.get(Calendar.MONTH) + 1;
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
		return sqlDate;
	}

	public String getDriverVersion() {
		return "ValioAxa";
	}

	public String getSubjectFilesTo() {
		return this.subjectFilesTo;
	}

	public String getPilotDayData(Agent agent) {

		String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa "
				+ " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON "
				+ " sal.invoiceid=jou.invoiceid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE len(talinventstatistics1num) >0 AND "
				+ " inv.dataareaid='liv' AND sal.dataareaid='liv' AND cus.dataareaid='liv' AND cus.talsalesman='"
				+ agent.getID()
				+ "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())"
				+ " GROUP BY talinventstatistics1num  ORDER BY talinventstatistics1num ";
		StringBuffer sb = new StringBuffer();

		System.out.println(sql);
		try {

			Statement stmt = this.getNewConnection().createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			sb.append("GrupiNimi     kilodes ja EUR\n");
			sb.append("---------------------------------------------------\n");

			double totalKokku = 0d;
			double totalSumma = 0d;

			while (rs.next()) {
				sb.append(rs.getString("grupp").trim() + "\n");
				int kogus = rs.getInt("kogus");
				int summa = rs.getInt("summa");
				totalSumma = round(totalSumma + summa, 2);
				totalKokku = round(totalKokku + kogus, 2);
				sb.append("\t" + kogus + "\t" + summa + " EUR\n\n");
			}

			sb.append("---------------------------------------------------\n");
			sb.append("\tKuu m��k kokku: " + totalKokku + " Kg." + totalSumma
					+ " EUR");

		} catch (Exception e) {
			getParent().showError(e);
		}

		return sb.toString();
	}

	public NumberFormat getNF() {
		return NF;
	}

	private boolean scrollTo(String id, ResultSet rs) throws SQLException {
		rs.beforeFirst();
		while (rs.next()) {
			if (id.equals(rs.getString("kood").trim())) {
				return true;
			}
		}
		return false;
	}

	private String ensureLen(String str, String replace) {
		if (str == null)
			str = "";
		else
			str = str.trim();
		if (str.length() == 0) {
			return replace;
		} else {
			return str;
		}
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	    
	     }

	 
	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "  INSERT into dbo.supervisor values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "?  ,"   //	sentdate	D,
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
	        Connection con2 = getNewConnectionRep();
	           PreparedStatement ps = con2.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	             eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, wr.getInfo().getAgentName());
	             
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             long lll = Calendar.getInstance().getTimeInMillis();
	             try{
	            	  lll = Long.parseLong((lw.getLine().getResStr3()));
	             }catch(Exception e){
	            	 System.out.println("res3" + lw.getLine().getResStr3());
	            	 e.printStackTrace();
	             }
	             ps.setTimestamp(7, new Timestamp(lll));
	             ps.setDouble(8, lw.getLine().getNormalPrice());
	             ps.setDouble(9, lw.getLine().getAmount());
	             ps.setDouble(10, 0);
	             ps.setDouble(11, 0);
	             ps.setInt(12, lw.getLine().getPercentageFromTotalCategory());
	             ps.setInt(13, lw.getLine().getPercentageFromColdCategory());
	             ps.setInt(14, lw.getLine().getPercentageFromPallCategory());
	             ps.setInt(15, lw.getLine().getPercentageFromShelfCategory());
	             ps.setInt(16, lw.getLine().getPercentageFromRestCategory());
	             ps.setBoolean(17, lw.getLine().isAction());
	             ps.setBoolean(18, lw.getLine().isPartner());
	             ps.setBoolean(19, lw.getLine().isBrand());
	             ps.setBoolean(20, lw.getLine().isGroup());
	             ps.setDouble(21, lw.getLine().getCategoryTotal());
	             ps.setDouble(22, lw.getLine().getPercentageFromRestCategory());
	             ps.setDouble(23, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(24, lw.getLine().getCustEval()*100);
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(25, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(26, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(27, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(28, cf);
	             ps.setInt(29, lw.getLine().getDealNr());
	             ps.setInt(30, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(31, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(32, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(33, cr3);
	             ps.setInt(34, lw.getLine().getResInt1());
	             ps.setInt(35, lw.getLine().getResInt2());
	             ps.setInt(36, lw.getLine().getResInt3());
	             ps.executeUpdate();
	           }

	           ps.close();
	           con2.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }

	/**
	 * 
	 * @param aNF
	 *            - the new value for NF
	 */
	public void setNF(NumberFormat aNF) {
		NF = aNF;
	}

	private String url = "jdbc:odbc:edisoft";
	private String url2 = "jdbc:odbc:report";
    private HashMap ketid = null;
    private HashMap sortimendid = null;
	private Connection con;
    private  HashMap prodHash = null;
	private Vector debt = new Vector();
	private Statement stmt;
	private String subjectFilesTo = null;
	private NumberFormat NF = NumberFormat.getInstance();
}
