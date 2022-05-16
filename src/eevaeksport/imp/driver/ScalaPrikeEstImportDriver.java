/*
 * Created on 9.11.2007
 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import eevaeksport.Email;
import eevaeksport.Product;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

	/**
	 * Title:        Eeva eksport
	 * Description:
	 * Copyright:    Copyright (c) 2002
	 * Company:
	 * @author Tarmo Tubro
	 * @version 1.0
	 */

	public class ScalaPrikeEstImportDriver
	    extends AbstractImportDriver {

	  static Category log = Category.getInstance(IceImportDriver.class.getName());

	  public ScalaPrikeEstImportDriver(ImportPanel parent) {
	    this.parent = parent;
	    PropertyConfigurator.configure("log4j.properties");
	   
	    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	      
	      if(parent.getEksportFrame().getProps().getProperty("db.url") != null) {
	     	 url = parent.getEksportFrame().getProps().getProperty("db.url");//"jdbc:odbc:scalaDB";	
	       }
	      
	      con = DriverManager.getConnection(url,"iscalaerp","Search2016" );
	      con2 = DriverManager.getConnection(url,"iscalaerp","Search2016" );
	      
	     // updateCustcodes();
	      calNF.setMaximumFractionDigits(0);
	      calNF.setMaximumIntegerDigits(2);
	      calNF.setMinimumIntegerDigits(2);
	      calNF.setGroupingUsed(false);
	      nf2.setMaximumFractionDigits(2);
	      nf.setMaximumFractionDigits(4);
	      nf.setGroupingUsed(false);
	      nf2.setGroupingUsed(false);
	      
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
	    readHaldusXLS();
	  }
	  
	  
	  private String getExcelDir(){
	      String dir = parent.getEksportFrame().getProps().getProperty("excel.dir");
	      if(dir == null){
	    	  return "excel/";
	      }
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
	    }
	  
	  private HashSet<String> halduskliendid = null;
	  private HashMap haldusklienditooted = null;
	  
	  private void readHaldusXLS(){
			File file = new File(getExcelDir() + "haldamistabel.xls");
			
			try {
				halduskliendid = new HashSet<String>();
				haldusklienditooted = new HashMap();
				
				FileInputStream ff = new FileInputStream(file);
				HSSFWorkbook workbook = new HSSFWorkbook((ff));
				HSSFSheet sheet = workbook.getSheetAt(0); //otsime esimeselt lehelt
				HSSFRow row = null;
			     int pikkus = sheet.getLastRowNum();
			
				// Uurime millised ankeedid on aktiivsed ja koodid meelde .
				

				for (int a = 4; a < pikkus+1; a++) {
					row = sheet.getRow(a);
						String klient = null;
						
						try {
						   klient = row.getCell((short)1).getStringCellValue();
						} catch (Exception e) {
							try {
								 klient = ((int)row.getCell((short)1).getNumericCellValue()) +"";
								} catch (Exception d) {
									
								}
						}
						
	                String ean = null;
						
						try {
							ean = row.getCell((short)2).getStringCellValue();
						} catch (Exception e) {
							try {
								ean = row.getCell((short)2).getNumericCellValue() +"";
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
								} catch (Exception d) {
									
								}
						}
						
						
		                String kogus = null;
							
							try {
								kogus = row.getCell((short)3).getStringCellValue();
							} catch (Exception e) {
								try {
									kogus = row.getCell((short)3).getNumericCellValue() +"";
									} catch (Exception d) {
										
									}
							}
							
							
			                String uuskood = null;
								
								try {
									uuskood = row.getCell((short)4).getStringCellValue();
								} catch (Exception e) {
									try {
										uuskood = row.getCell((short)4).getNumericCellValue() +"";
										} catch (Exception d) {
											
										}
								}
								
								
				                String lisakood = null;
									
									try {
										lisakood = row.getCell((short)5).getStringCellValue();
									} catch (Exception e) {
										try {
											lisakood = row.getCell((short)5).getNumericCellValue() +"";
											} catch (Exception d) {
												
											}
									}
								
									
					                String lisakogus = null;
										
										try {
											lisakogus = row.getCell((short)6).getStringCellValue();
										} catch (Exception e) {
											try {
												lisakogus = row.getCell((short)6).getNumericCellValue() +"";
												} catch (Exception d) {
													
												}
										}
						
					//	System.out.println("klient " + klient  + " " +  ean + " " + kogus + " " + uuskood + " " + lisakood + " " + lisakogus );
						
						if(klient != null && klient.trim().length() > 0){
							StringTokenizer st = new StringTokenizer(klient,";");

							while (st.hasMoreTokens()){
								String kl1 = st.nextToken();
								if( !halduskliendid.contains(kl1)){
									halduskliendid.add(kl1);	
								}
								Product prod = new Product(ean);
								try{
									prod.setStock(Double.parseDouble(kogus));
									prod.setOnPallet(Double.parseDouble(lisakogus));
								}catch(Exception e ){
								}
								
								prod.setGroup(lisakood);
								prod.setAgentGroup(uuskood);
								
								
								Vector<Product> vec = (Vector<Product>) haldusklienditooted.get(kl1);
								if(vec == null){
									vec = new Vector<Product>();
								}
								vec.add(prod);
								haldusklienditooted.put(kl1, vec);
							}
					   }
				  }
				}catch(Exception ee ){
					ee.printStackTrace();
				}
				//System.out.println("Haldus kliendid " + haldusklienditooted.toString());
	  }
	  
	  private Product getHaldusCustomerAndProduct(String kl,OrderLine  line){
		  Product res = null;
		 String maksja =  getPayer(kl);
		 String ean = getEan(line.getToode());
		System.out.println("maksja on olemas " + maksja + " " + halduskliendid.contains(maksja));
		 if(maksja != null && halduskliendid.contains(maksja)){
	      Vector<Product> vec = (Vector<Product>)haldusklienditooted.get(maksja);
	     // System.out.println("tooted "+ vec);
	      if(vec != null){
	    	  
	    	  Iterator vItr = vec.iterator();
	    	  while(vItr.hasNext()){
	    		  Product prod = (Product)vItr.next();
	    		//  String ean = line.getEAN();
	    		 
	    		  if(ean != null && ean.trim().length() > 0 && prod.getID().equals(ean)){
	    			 // System.out.println("toode "+ line.getKogus()/prod.getStock() + " | " + (int)(line.getKogus()/prod.getStock()));
		    		  if((line.getKogus()/prod.getStock()) == ((int)(line.getKogus()/prod.getStock()))){ //kui jagatis on t�isarv
		    			//  System.out.println("tagastan toote" + prod );
		    			  return prod;
		    		  }
		    		  
	    		  }
	    	  }
	        }    
		  }
		  return res ;
	  }
	  

	  private String getLineNr(int i){
		  	String res = i + "";
		  	if (res.length() == 1){
		  		res = "0000" + res + "0";
		  	} else if (res.length() == 2){
		  		res = "000" + res + "0";
		  	} else if (res.length() == 3){
		  		res = "00" + res + "0";
		  	} else if (res.length() == 4){
		  		res = "0" + res + "0";
		  	} else if (res.length() == 5){
		  		res = "" + res + "0";
		  	}
		  	return res;
		  }

	  
	  public String getEan(String prodID){
		  	

		    String sql1 = " Select SC01132 AS ean FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
		  log.info("1_LINE_SQL: " + sql1);

		    String grupp = null;

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	grupp = rs1.getString("ean");
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	//    System.out.println("EAN k�es 2 ;" + grupp );
		    return grupp;
		  }
	  
	  
	  
	  public String identfyGroup(String prodID){
		  	


		    String sql1 = " Select SC01037 AS grupp FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
		  log.info("1_LINE_SQL: " + sql1);

		    String grupp = "MUU";

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	grupp = rs1.getString("grupp");
		    	if(grupp == null || grupp.trim().length() == 0){
		    		grupp = "MUU";
		    	}
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  //  System.out.println("Grupp k�es 2 ;" + grupp  + " " + prodID);
		    return grupp;
		  }
	  
	
	private void  addToDB(String agentid,String custid,String toode,double kogus,double disc,String country,double alekogus,boolean zall){
		
		String zallah = "";
		if(zall){
			kogus = alekogus;
			zallah = "zal";
			
		}
     
		    String sql1 = "   insert into discountlines" + zallah + " (agentid,custid,sentdate,prodid,amount,discount,country) values " +
	 "  ('" +agentid + "','" + custid + "',getdate(),'" + toode + "'," +kogus + "," + disc + ",'" + country + "') ";
		  
		    Statement st1;
			try {
				st1 = con.createStatement();
				st1.executeUpdate(sql1);
				st1.close();
		}catch (Exception e){
			
		}
		
		
    }

	private void  addToTelemaDB(String docnum ,String custid,String toode,String tootenimi ,String ean, double kogus){

     
		/*[OrderDate] [datetime] NULL,
      [PartyCode] [nvarchar](255) NULL,
      [DocumentNum] [nvarchar](20) NULL,
      [GTIN] [nvarchar](35) NULL,
      [ItemDescription] [nvarchar](255) NULL,
      [SellerItemCode] [nvarchar](255) NULL,
      [AmountOrdered] [float] NULL

*/
		
		if(docnum.startsWith("T")){
			docnum = docnum.substring(1);
		}
		
		    String sql1 = "   insert into TELEMASTSISSE (OrderDate,PartyCode,DocumentNum,GTIN,ItemDescription,SellerItemCode,AmountOrdered ) values " +
	 "  (getdate(),'" + custid + "','" + docnum + "','" + ean + "','" + tootenimi + "','" + toode + "'," +kogus + ") ";
		  
		    Statement st1;
			try {
				st1 = con.createStatement();
				st1.executeUpdate(sql1);
				st1.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
    }
	
	  private String getDateString(Calendar cal){
		    StringBuffer sb = new StringBuffer();
		    sb.append(cal.get(Calendar.YEAR));
		    sb.append("-");
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    if (month.length() == 1){
		      sb.append("0");
		    }
		    sb.append(month);
		    sb.append("-");
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		    if (day.length() == 1){
		      sb.append("0");
		    }
		    sb.append(day);

		    return sb.toString();
		  }
		
	  
	  public String getMakse(String custid , String grupp){


				    String sql1 = " select Tingimus from maksed20100 m  inner join SL010100 k on k.SL01056=m.SL01001 where k.SL01001= '" + custid + "' and Grupp = '" + grupp + "'";
				  log.info("1_LINE_SQL: " + sql1);

				    String result = null;

				    Statement st1;
					try {
						st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
				    if (rs1.next()) {
				    	result = rs1.getString("Tingimus");
				    }
				    rs1.close();
				    st1.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			   //     System.out.println("Makset�htaja kood "  + result + " " + custid + " " + grupp);
				    return result;

	  
	  
					
}
	
	  
	  private String getPayer(String custid){
			String payer = null;
		  try{
			String sqlp = "Select SL01056  as maksja from SL010100 where SL01001 = '" + custid + "'";
			Statement st1 = con.createStatement();
		     ResultSet  rs1 = st1.executeQuery(sqlp);			
			if (rs1.next()) {
			     payer = rs1.getString("maksja");
			    // System.out.println("maksja " + payer);
			   
	          }else {
	        	  payer = "ALL";
	          }
			
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return payer;
		  }
	  
	  
	  private boolean isokTTTStatus(String custid){
		  boolean res = false;
			
		  try{
			String sqlp = "Select SL01208  as a1 ,SL01209 as a2 from SL010100 where SL01001 = '" + custid + "'";
			Statement st1 = con.createStatement();
		     ResultSet  rs1 = st1.executeQuery(sqlp);
			
			if (rs1.next()) {
			     String a1  = rs1.getString("a1");
			     String a2  = rs1.getString("a2");
			    if(a1 != null && a1.trim().length() > 0 && a2!= null && a2.trim().length() > 0)
			      res = true;
			  }
			
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return res;
		  }
	  
	  private boolean isZeroStatus(String custid){
		  boolean res = false;
		  try{
	String payer = null;
			
			String sqlp = "Select SL01056  as maksja from SL010100 where SL01001 = '" + custid + "'";
			Statement st1 = con.createStatement();
		     ResultSet  rs1 = st1.executeQuery(sqlp);
			
			if (rs1.next()) {
			     payer = rs1.getString("maksja");
			   //  System.out.println("maksja " + payer);
			     if(payer != null && (payer.trim().equals("0104") || payer.trim().equals("1901") || payer.trim().equals("0634")
			    		  || payer.trim().equals("3242") || payer.trim().equals("1986")
			    		  || payer.trim().equals("1072") || payer.trim().equals("2416")
			    		  )){
			    	 // rimi,Maxima,Selver
			      res = true;
			     }
	          }
			
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return res;
		  }
	  
	  
	  private boolean getEttemaks(String custid){


		    String sql1 = " select (select SL01099 from SL010100 where k.SL01056=SL01001 )as ette from  SL010100 k  where k.SL01001= '" + custid + "' ";
		     log.info("1_LINE_SQL: " + sql1);

		    String result = null;

		    Statement st1;
			try {
				st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	result = rs1.getString("ette");
		    	if(result != null && result.toLowerCase().indexOf("ettem") != -1){
		    		return true;
		    	}
		    }
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return false;
		  }

	  public String getHinnakiri(String custid ){


		    String sql1 = " select SL01029 as hinnakiri from SL010100   where SL01001= '" + custid + "' ";
		  log.info("1_LINE_SQL: " + sql1);

		    String result = null;

		    Statement st1;
			try {
				st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	result = rs1.getString("hinnakiri");
		    	if(result != null && result.trim().length() != 0){
		    		result = result.trim();
		    	}
		    }
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	  
		    return result;
		  }



	  
	  
	  private Vector splitOrder (Order order){
		  Vector result = new Vector();
	   
		  Order ordBAT = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordNIV = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordMUU = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordRekl = new Order(order.getCompany(),order.getAgentFirstName());			 
		  Order ordNega = new Order(order.getCompany(),order.getAgentFirstName());	
			 
		  
		  ordBAT.setAgentCode(order.getAgentCode());
		  ordBAT.setDokNr(order.getDokNr());
		  ordBAT.setWarehouse(order.getWarehouse());
		  ordBAT.setInfo(order.getInfo());
		  ordBAT.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordBAT.setStatus(order.getStatus());
		  ordBAT.setPaymTerm(order.getPaymTerm());
		  ordBAT.setILN(order.getILN());
		  ordBAT.setChanged(true); // et teaksime et on tegemita tubakaga T t�he lisamiseks
		 
		  
		  ordNIV.setAgentCode(order.getAgentCode());
		  ordNIV.setDokNr(order.getDokNr());
		  ordNIV.setWarehouse(order.getWarehouse());
		  ordNIV.setInfo(order.getInfo());
		  ordNIV.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordNIV.setStatus(order.getStatus());
		  ordNIV.setPaymTerm(order.getPaymTerm());
		  ordNIV.setILN(order.getILN());
		  ordNIV.setChanged(true); // et teaksime et on tegemita tubakaga T t�he lisamiseks
		 
		  
		  ordNega.setAgentCode(order.getAgentCode());
		  ordNega.setDokNr(order.getDokNr());
		  ordNega.setWarehouse(order.getWarehouse());
		  ordNega.setInfo(order.getInfo());
		  ordNega.setPaymTerm(order.getPaymTerm());
		  ordNega.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordNega.setStatus(order.getStatus());
		  ordNega.setILN(order.getILN());
		  
		  ordMUU.setAgentCode(order.getAgentCode());
		  ordMUU.setDokNr(order.getDokNr());
		  ordMUU.setWarehouse(order.getWarehouse());
		  ordMUU.setInfo(order.getInfo());
		  ordMUU.setPaymTerm(order.getPaymTerm());
		  ordMUU.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordMUU.setStatus(order.getStatus());
		  ordMUU.setILN(order.getILN());
		  
		  ordRekl.setAgentCode(order.getAgentCode());
		  ordRekl.setDokNr(order.getDokNr());
		  ordRekl.setWarehouse(order.getWarehouse());
		  ordRekl.setInfo(order.getInfo());
		  ordRekl.setPaymTerm(order.getPaymTerm());
		  ordRekl.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordRekl.setStatus(order.getStatus());
		  ordRekl.setILN(order.getILN());
		  
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
		
			     String itemid = lin.getToode();
			     
			    String group =  identfyGroup(itemid);
			    
			   // System.out.println("Splitter PL  " + lin.getPl() );
			    
			    
			     if(group.equals("BAT") || group.equals("NIV")){ //|| group.equals("STG")  
			     
			    	 if(lin.getKogus() < 0){
			    		 ordNega.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					     	 	 
			    	 }else{
			    	 	  ordBAT.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					  	 
			    	 }
			    	
			      
			     
			     }else  if(group.equals("NIVEAGGSG") ){ 
			     
			    	if (lin.getKogus() < 0){
			    		 ordNega.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					   } else {
			    	 	  ordNIV.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					 }
			    	
			     
			     } else if(itemid.startsWith("XX")){
			    	 ordRekl.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     } else {
				    	 
			    	 if(lin.getKogus() < 0){
			    		 ordNega.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					 }else{
			    		 ordMUU.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			    				 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			    	 }
				     }
			     
                   }





		  if(ordBAT.getOrderLines() != null && ordBAT.getOrderLines().size()>0){
			  result.add(ordBAT);  
		  }
		  
		  if(ordNIV.getOrderLines() != null && ordNIV.getOrderLines().size()>0){
			  result.add(ordNIV);  
		  }

		  if(ordNega.getOrderLines() != null && ordNega.getOrderLines().size()>0){
			  result.add(ordNega);  
		  }

		  if(ordMUU.getOrderLines() != null && ordMUU.getOrderLines().size()>0){
			  result.add(ordMUU);  
		  }
		  
		  if(ordRekl.getOrderLines() != null && ordRekl.getOrderLines().size()>0){
			  result.add(ordRekl);  
		  }
		 
		  return result ;
	  
	  }



	
	  public String identifyCustomer(String id,String regnr) throws SQLException {
	  	String sql1 = null;
	  	
	  	if(id.equals("stockmann")){
	  		return "T06611";
	  	}
	  	
	  //	 System.out.println("Kliendi M��ramine: " );
	  	
	  	    sql1 = " SELECT SL01001 AS kood FROM SL010100 where   SL01201='" + id + "'";	 //????
		
	  	    String  sql2 = " SELECT SL01001 AS kood FROM SL010100 where   SL01001='" + id + "'";
	  	    
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim() ;

	    }
	    
	    if(acc == null || acc.trim().length()== 0){
	    	rs1 = st1.executeQuery(sql2);
	    	if(rs1.next()){
	    		acc = rs1.getString("kood").trim();
	    	}
	    }
	    
	    rs1.close();
	    st1.close();

	  //  System.out.println("Klient: " + acc + " " + id);
	    return acc;

	  }

	  public double getStdPrice(String id) throws SQLException {

		  	
    String  sql1 = 	    
	" SELECT  SC39004 AS price  FROM SC010100 left outer JOIN SC030100 on SC03001=SC01001 and SC03002='01' AND SC03003!=0 " + 
	" left outer JOIN SC390100 ON SC01001 = SC39001 and SC39002='00'  WHERE  " +
	" LEN(LTRIM(SC01128))>0 and SC39004 > 0 and SC01001 = '" + id + "' ";
    
           System.out.println(sql1);
		    double acc = 0;
		    Connection cccd = DriverManager.getConnection(url,"iscalaerp","Search2016" );
		    Statement st1 = cccd.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		  	    
			    System.out.println(sql1);

			    if (rs1.next()) {
			      acc = rs1.getDouble("price") ;
			    }
			    

		  
		    rs1.close();
		    st1.close();
		    cccd.close();

		    System.out.println("Hind: " + acc + " " + id);
		    return acc;

		  }

	  public double getPrice(String id, String hinnakiri, String klient,double stPrice) throws SQLException {

		  	
		    String  sql1 = 	    
			"SELECT SCP6009 as disc, SCP6008 AS price \r\n" + 
			"		                 FROM SCP60100 INNER JOIN SL010100 ON  SCP6002= SL01056 LEFT JOIN SY240100 on SCP6009=SY24002 and SY24001='BG' WHERE \r\n" + 
			"		               getdate() BETWEEN  SCP6010 AND SCP6011  " +
						" and SL01001 = '" + klient + "' and SCP6002='" + id + "' ";
		    
		    
		           System.out.println(sql1);
				    double acc = 0;
				    Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
				    
		    if (klient != null){
			    while (rs1.next()) {
			    	double dd = rs1.getDouble("disc");
				      acc = rs1.getDouble("price") *((100-dd)*0.01);
				      System.out.println("hind ja ale " + acc + " " + dd );
				    }	
			    
			    String sql0 = " SELECT SL01002 AS nimi,SL01001 AS kood, case when  SC45005 >0 then SC45005 else 0 end AS ale" +
		        "  FROM SL010100 kl INNER JOIN SC450100    ON SL01036=SC45003  " +
		       "   WHERE SL01001 = (select SL01056 from SL010100 where SL01001 = '" + klient +"') ORDER BY SL01002";
		   st1 = con.createStatement();
		   rs1 = st1.executeQuery(sql0);
		   double ale = -1;
		   double alehind = 0;
		  if (rs1.next()) {
		  	ale = rs1.getDouble("ale") ;
		  	
		  }
		  if(ale != -1 ){
			  alehind = stPrice * (100d-ale)/100d;
			  if(acc == 0){
				  acc = alehind;
			  }else if(acc > alehind){
				  acc = alehind;
			   }
		      }	    
		     }

			  
			 	
			
		     
		    
		    
		    sql1 = "  SELECT  SC39004 AS price  FROM SC390100  WHERE  SC39002='" + hinnakiri+ "' and  SC39001 = '" + id + "'"; 	    
		    System.out.println("Teine kontroll " + sql1);
		 
		     st1 = con.createStatement();
		     rs1 = st1.executeQuery(sql1);
		if (hinnakiri != null && acc == 0){
		    while (rs1.next()) {
		      acc = rs1.getDouble("price") ;
		    }	
		}


		    
		    
		    
				    System.out.println("Klient: " + acc + " " + id);
				  if(acc == 0){
					  sql1 = "  SELECT  SC39004 AS price  FROM SC390100  WHERE  SC39002='00' and  SC39001 = '" + id + "'";
				  	    
					    System.out.println(sql1);
					    

					   st1 = con.createStatement();
					   rs1 = st1.executeQuery(sql1);

					    while (rs1.next()) {
					      acc = rs1.getDouble("price") ;

					    }
					    

				  }
				  	   		    rs1.close();
				    st1.close();

				    System.out.println("Klient: " + acc + " " + id);
				    return acc;

				  }

	  
	  public String identifyCustomerAGENT(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01035 as agent FROM SL010100 left JOIN SY240100  ON SL01036=SY24002 " +
	 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();

	   }
	   rs1.close();
	   st1.close();
       if(agent.equals("MUU")){
    	//   agent = "   ";
       }
	   return agent;

	 }
	
	  
	  public double getCustDiscount(String id) throws SQLException {
		
		 	 
		     String sql1 = "SELECT SC45005 AS ale FROM SL010100 kl inner JOIN SC450100 ON SL01036=SC45003  " +
		       "   WHERE  SL01001='" + id +"'"; 
		   System.out.println(sql1);
		   double res = 0;

		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   if (rs1.next()) {
		   	res = rs1.getDouble("ale");

		   }
		   rs1.close();
		   st1.close();
	     
		   return res;

		 }
	  
	  public String getNimi(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01002 as agent FROM SL010100  WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();
       }
	 
	   rs1.close();
	   st1.close();
     
	   return agent;

	 }
	  
	  
	  
	  public String getPhone(String id) throws SQLException {
	 	 String	sql1 = "SELECT SL01011 as agent FROM SL010100  WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();
       }
	 
	   rs1.close();
	   st1.close();
     
	   return agent;

	 }
	  
	  public String getAddress(String id) throws SQLException {
	 	 String	sql1 = "SELECT (SL01003 + ' (' + SL01004 + ')')  as agent FROM SL010100  WHERE SL01001='" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;

	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("agent").trim();

	   }
	   rs1.close();
	   st1.close();
     
	   return agent;

	 }
	
	
	  
	  public String getDemaLadu(String toode, double kogus) throws SQLException{
		 	 
		    String sql1 = " select SC03002 as ladu from  SC030200  " + 
         "  WHERE RTRIM(SC03001)=   '" + toode.trim() + "' and  SC03003-SC03004 > " + kogus + " and SC03002 in ('01') AND SC03003!=0";
		    String ladu = "02";
		    Statement st1 = con2.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	ladu = rs1.getString("ladu").trim();
		    }
		       System.out.println(toode + " ladu1 " + ladu);
		    rs1.close();
		    st1.close();
		    return ladu;
		 }
		
	  private TreeMap<String,Double> tagseis = null;
	  private TreeMap<String,String> hardtoode = null;
	  
	  private void tagSeis(){
		  tagseis = new TreeMap<String,Double>();
		  // select SC33001 as prodid ,sum (SC33005) as kogus from SC330100 where SC33004 ='TAG' group by SC33001 having sum (SC33005) > 0
		 	 
		    String sql1 = " select SC33001 as prodid ,sum (SC33005) as kogus from SC330100 where SC33004 ='TAG' group by SC33001 having sum (SC33005) > 0";
		    
		    Statement st1;
			try {
				st1 = con2.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);
		    while (rs1.next()) {
		    	tagseis.put(rs1.getString("prodid"), rs1.getDouble("kogus"));
		    	
		    }
		      
		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  
			System.out.println("tagseis suurus " + tagseis.size());
	  }
	
	  private void hardEAN(){
		  // anda tagasi failist tulnud ean baasil failis olev toote kood .on �lemuslik 
		  hardtoode = new TreeMap<String,String>();
		  File f = new File("KindlaSKUvalik.txt");

		     if (f.exists()){
		    	 try{
		    		  java.io.InputStreamReader is = new java.io.FileReader(f);
		    	      java.io.BufferedReader br = new java.io.BufferedReader(is);
		    	    
			       String line = null;
			       while((line = br.readLine()) != null){
			    	  	
				         StringTokenizer st = new StringTokenizer(line, ","); 
				         while(st.hasMoreTokens()){
				         
				        	 try{
				        		 
				        		 //ean ja toode
				        	 hardtoode.put(st.nextToken(), st.nextToken()) ;
				         }catch(Exception r){
				        	r.printStackTrace(); 
				         }
				        	
				         }
				         
			       } 
		     }catch(Exception e){
		    	 
		     }
		    	
		     }
		     
		    System.out.println( "hard toode " + hardtoode.toString() );   
	  }
	  
	  
	  private double  getLaoseis(String toode) throws SQLException{
		 	 
		    String sql1 = " select SC03002 as ladu, SC03003-SC03004 as seis from  SC030100  " + 
       "  WHERE RTRIM(SC03001)=   '" + toode.trim() + "' and SC03002 in ('01') AND SC03003!=0 ";
		    double seis = 0;
		    Statement st1 = con2.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	seis = rs1.getDouble("seis");
		    }
		     //  System.out.println(toode + " seis " + seis);
		    rs1.close();
		    st1.close();
		    if(tagseis.get(toode) != null){
		    	seis = seis - tagseis.get(toode);
		    }
		    return seis;
		 }
	  
	  
	  public String getPandiPakend(String toode) throws SQLException{
	 	 
	    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
	    String prod = null;
	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);
	    while (rs1.next()) {
	      prod = rs1.getString("pant").trim();
	    }
	       System.out.println(toode + " prod1 " + prod);
	    rs1.close();
	    st1.close();
	    return prod;
	 }
	  
	  public double getPandiKordaja(String toode) throws SQLException{
		 	 
		    String sql1 = " SELECT RTRIM(SC01019) AS pant FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
		    double prod = 1;
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    if (rs1.next()) {
		    	try{
		    		 prod = Double.parseDouble(rs1.getString("pant"));
		    		 if (prod == 0 ){
		    			 prod = 1;
		    		 }
		    	}catch(Exception e){
		    		//e.printStackTrace();
		    	}
		     
		    }
		       System.out.println(toode + " prod1 " + prod);
		    rs1.close();
		    st1.close();
		    return prod;
		 }
	 
	  public double getJagaja(String toode) throws SQLException{
		 	 
		    String sql1 = " SELECT (case when SC01037 ='BAT' and SC01023='SIGARET' then (case when SC01009 >0  then 10/SC01009 else 1 end) else 1 end)" +
		    		" AS jagaja FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
		    double jagaja = 1;
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    while (rs1.next()) {
		    	jagaja = rs1.getDouble("jagaja");
		    }
		      
		    rs1.close();
		    st1.close();
		    
		    if(parent.getProps().getProperty("nomultiply.prods") != null &&
		    		parent.getProps().getProperty("nomultiply.prods").indexOf(toode) != -1){
		    	jagaja =  1;
		    }
		    System.out.println(toode + " jagaja " + jagaja);
		    return jagaja;
		 }
		   
	  public double getJagajaNIV(String toode) throws SQLException{
		 	 
		    String sql1 = " SELECT (case when SC01037 ='NIV' and SC01023 like 'TUBAKA%' then (case when SC01009 > 0  then 10/SC01009 else 1 end)"
		    		+ " else 1 end) AS jagaja FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
		    double jagaja = 1;
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    while (rs1.next()) {
		    	jagaja = rs1.getDouble("jagaja");
		    }
		      
		    rs1.close();
		    st1.close();
		    
		    if(parent.getProps().getProperty("nomultiply.prods") != null &&
		    		parent.getProps().getProperty("nomultiply.prods").indexOf(toode) != -1){
		    	jagaja =  1;
		    }
		    System.out.println(toode + " jagajaNIV " + jagaja);
		    return jagaja;
		 }
	  
	  private Vector batstatus = new Vector();
	 
	  
	  
	  
	  
	  public String identifyProduct(String ean, String prodID) throws SQLException{
	  	if(ean == null){
	  		ean = "XXXXXXXXXXX";
	  		
	  	}
	  
		    String sql2 = "	  SELECT (SC01001) AS toode ,SC03003 FROM SC010100 LEFT JOIN SC030100 on SC03001=SC01001 and SC03002='01'  WHERE RTRIM(SC01132) like   '%" + ean.trim() + "%'" +
 	"	 and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016,SC03003 desc " ;
 

		  
		 
	   
	    String prod = null;
boolean pane = false;
	    Statement st1 = con.createStatement();
	    ResultSet rs1 ;
	    if(prodID != null && prodID.trim().length() > 0){
	    	  String sql1 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001) = '" + prodID.trim() + "'" +
	 		 "  and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U','R') ";
	    	  rs1 = st1.executeQuery(sql1);
	  	    if (rs1.next()) {
	  	     // prod = rs1.getString("toode").trim(); 
	  	    }else{
	  	      pane = true;	
	  	    }
	  	  rs1.close();
	  	  
	  	  
	  	 if(prod == null && prodID != null && prodID.trim().length() > 6){
	    	  String sql6 = "	  SELECT (SC01001) AS toode ,SC03003 FROM SC010100 LEFT JOIN SC030100 on SC03001=SC01001 and SC03002='01'  WHERE RTRIM(SC01132) =   '" + prodID.trim() + "'" +
			 	"	 and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016,SC03003 desc " ;
		      rs1 = st1.executeQuery(sql6);
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		      rs1.close();
		    }
	  	  
	    }
	  
	   //System.out.println(ean + " prod0 prodid kaudu  " + prod);
	    if(prod == null && ean.trim().length() > 6){
	      rs1 = st1.executeQuery(sql2);
	      if (rs1.next()) {
	        prod = rs1.getString("toode").trim();
	      }
	      rs1.close();
	    }
	    if(prod == null && prodID != null && prodID.trim().length() > 6){
	    	  String sql6 = "	  SELECT (SC01001) AS toode ,SC03003 FROM SC010100 LEFT JOIN SC030100 on SC03001=SC01001 and SC03002='01'  WHERE RTRIM(SC01132) =   '" + prodID.trim() + "'" +
			 	"	 and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016,SC03003 desc " ;
		      rs1 = st1.executeQuery(sql6);
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		      rs1.close();
		    }
	    if(prod == null && ean.trim().length() > 6){
	    	  while(ean != null && ean.startsWith("0")){
	  	    	ean = ean.substring(1);
	  	    }
		      rs1 = st1.executeQuery( "	  SELECT (SC01001) AS toode ,SC03003 FROM SC010100 LEFT JOIN SC030100 on SC03001=SC01001 and SC03002='01'  WHERE RTRIM(SC01132) like   '%" + ean.trim() + "%'" +
		    		 	"	 and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016,SC03003 desc " );
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		      rs1.close();
		    }
	 

	    if(prod == null && prodID != null && prodID.trim().length() > 10){
	    	//l�heme eani kontrollima
	    	  while(prodID != null && prodID.startsWith("0")){
	    		  prodID = prodID.substring(1);
	  	    }
		      rs1 = st1.executeQuery( "	  SELECT (SC01001) AS toode ,SC03003 FROM SC010100 LEFT JOIN SC030100 on SC03001=SC01001 and SC03002='01'  WHERE RTRIM(SC01132) like   '%" + prodID.trim() + "%'" +
		    		 	"	 and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016,SC03003 desc " );
		      if (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      }
		      rs1.close();
		    }
	    //System.out.println(prodID + " prod1 " + prod + " " +  ean);
	    
	    st1.close();
if(pane){
	batstatus.addElement(parent.firma + "|" + prodID);
}
	    return prod;
	  }

	  public String identifyProduct3(String toode, double amount) throws SQLException{
		  	
		    /*-
	          // otsime ean uuesti ja sama piires vaatan laosaldosid 'U' ja 'R' ei vaata  - ja valin vanima    // tootekoodi veini ja kanget eraldi ei k�sitleta
   // SC01116 tooteloomise kuup�ev ja ja kliendi kaardilt vaatata kas on vaja protseduur k�ivitada 
See on see muudatus,kus tuleb EAN piires valida kronoloogiliselt vanim tootekood ja kuna osade klientide puhul on oluline,et antud loogika ei rakenduks(k�vemad restod n�iteks,kus oluline,et m��gimehe poolt tellimusele valitud veini tootekoodi ei muudetaks),siis oli vaja teha nii,et Sinu lahendus k�ib veel sellelt v�ljalt  vaatamas.

Kui v�ljal on 1,siis eelpooltoodud vanema koodi valimise loogika ei rakenduks ja kui v�li on t�hi v�i 0,siis rakenduks.
Tegin CUSTOMERDATA0100 tabelisse lisav�lja � MJkoodivalik, v�ljat��biks on �l�liti�      
	*/
			 //  otsime ean uuesti ja sama piires vaatan laosaldosid 'U' ja 'R' ei vaata
		  
		  
		  
		  String sql0 = " SELECT RTRIM(SC01132) AS toode FROM SC010100   WHERE RTRIM(SC01001)=   '" + toode + "'" +
		   " and rtrim(ltrim(SC01016)) not in  ('U','R')";
		  
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql0);
         String ean = null;
		    if (rs1.next()) {
		      ean = rs1.getString("toode"); 
		    }
		    if(ean == null){
		    	return toode;
		    }
		    
		 //  System.out.println("ean tuvastamine " + toode + " " + ean );
		    if(ean != null && ean.trim().equals("PUUDUB")){
		    	return toode;
		    }
		    
		    
			
		    if(ean != null && hardtoode != null && hardtoode.get(ean) != null){
		   // 	System.out.println("tagastan hard tootest " + hardtoode.get(ean));		    	
		    	return hardtoode.get(ean);
		    }
		    
			    String prod = toode;
			    
			  if(ean != null && ean.trim().trim().length() > 0){
				  
				  boolean puudub = true;
				   String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + ean.trim() + "'" +
		    		"  and rtrim(ltrim(SC01016)) not in  ('U','R') order by SC01116 desc";
				  
				   String sql3 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '0" + ean.trim() + "'" +
			        "  and rtrim(ltrim(SC01016)) not in  ('U','R')  order by SC01116 desc";  
			 
				   
				  
				   
				   rs1 = st1.executeQuery(sql2);
                  
				    while (rs1.next()) {
				     String prod22 = rs1.getString("toode").trim(); 
				     double seis = getLaoseis(prod22);			     
					    	//System.out.println("Laos " + seis + " tellimusel" +  amount); 
					    	if(seis >= amount ){
					    		puudub = false;
					    		prod = prod22;					    		
					    		//System.out.println("katab �ra kyll "); 
					    	}
					  }
				    rs1.close();
				   if(puudub){
					   rs1 = st1.executeQuery(sql3);
            	    while (rs1.next()) {
					     String prod22 = rs1.getString("toode").trim(); 
					     double seis = getLaoseis(prod22);
						    	//System.out.println("Laos " + seis + " tellimusel" +  amount); 
						    	if(seis >= amount ){
						    		puudub = false;
						    			prod = prod22;					    		
						    		//System.out.println("katab �ra kyll "); 
						    	}
					    }
					    rs1.close();  
				   }
				  
		    
			  }else{
				  
			  }
			  
		    return prod;
		  }

		
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  public String identifyProduct2(String toode, double amount) throws SQLException{
		  	
		    /*-tellimusele valitakse see tootekood,mille saldo on suurim ja mis katab �ra tellitud koguse
	             -kui suurim laosaldo ei kata �ra tellitavat kogust ja kui k�ik saldod on nullid,valitakse aktiivse staatusega tootekood
	             -kange alko puhul valitakse alati aktiivse staatusega tootekood
	              
	*/
			 //leiame tootele EAN koodi uuesti 
		  
		  String sql0 = " SELECT RTRIM(SC01132) AS toode FROM SC010100   WHERE RTRIM(SC01001)=   '" + toode + "'" +
 		   " and rtrim(ltrim(SC01016)) not in  ('S','U','R')";
 		  
		    Statement st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql0);
           String ean = null;
		    if (rs1.next()) {
		      ean = rs1.getString("toode"); 
		    }
		//   System.out.println("ean tuvastamine " + toode + " " + ean );
		    if(ean != null && ean.trim().equals("PUUDUB")){
		    	return toode;
		    }
			  //1. Tuvastame kas toode on vein v�i mitte 
			  
			  boolean vein = true ; //k�ikide toodete puhul 
			  
			
			  
			  
			  String sql = " SELECT RTRIM(SC01001) AS toode FROM SC010100   WHERE RTRIM(SC01001)=   '" + toode + "'" +
	  		   " and rtrim(ltrim(SC01016)) not in  ('S','U','R') and left (SC01128 ,2) in ('PV','RV','VV') order by SC01016";
	  		  
			    st1 = con.createStatement();
			    rs1 = st1.executeQuery(sql);

			    if (rs1.next()) {
			      vein = true; 
			    }
			   
			    
			  
			   
			    
			    String prod = toode;
			    
			    
			  if(vein && ean != null){
				//  System.out.println("toode on vein");
				  boolean puudub = true;
				   String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + ean.trim() + "'" +
		    		"  and rtrim(ltrim(SC01016)) not in  ('S','U','R') order by SC01016 ";
				   String sql3 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '0" + ean.trim() + "'" +
			        "  and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016 ";  
			 
				   
				  //-tellimusele valitakse see tootekood,mille saldo on suurim ja mis katab �ra tellitud koguse
				   
				   rs1 = st1.executeQuery(sql2);
                    
                      double suurim = 0;
				    while (rs1.next()) {
				     String prod22 = rs1.getString("toode").trim(); 
				     double seis = getLaoseis(prod22);			     
				//	    	System.out.println("Laos " + seis + " tellimusel" +  amount); 
					    	if(seis >= amount ){
					    		puudub = false;
					    		if(suurim < seis){
					    			suurim = seis;
					    			prod = prod22;					    		
					    		}
			//		    		System.out.println("katab �ra kyll "); 
					    	
					    	}
					    
				    }
				    rs1.close();
				   
				    rs1 = st1.executeQuery(sql3);

				    while (rs1.next()) {
				     String prod22 = rs1.getString("toode").trim(); 
				     double seis = getLaoseis(prod22);
					//    	System.out.println("Laos " + seis + " tellimusel" +  amount); 
					    	if(seis >= amount ){
					    		puudub = false;
					    		if(suurim < seis){
					    			suurim = seis;
					    			prod = prod22;					    		
					    		}
					  //  		System.out.println("katab �ra kyll "); 
					    	}
					   
				    }
				    rs1.close();
				    
	//-kui suurim laosaldo ei kata �ra tellitavat kogust ja kui k�ik saldod on nullid,valitakse aktiivse staatusega tootekood
				    if(puudub){
				    
					    sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + ean.trim() + "'" +
			    		"  and rtrim(ltrim(SC01016)) not in  ('S','U','R') order by SC01016 ";
			    
			           sql3 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '0" + ean.trim() + "'" +
				        " and rtrim(ltrim(SC01016)) not in  ('S','U','R')  order by SC01016 ";  
				 
				  

			    rs1 = st1.executeQuery(sql3);

			    if (rs1.next()) {
			      prod = rs1.getString("toode").trim(); 
			    }
			   
			   
			    //System.out.println(ean + " prod0 " + prod);
			   
			    if(prod == null){
			      rs1 = st1.executeQuery(sql2);
			      if (rs1.next()) {
			        prod = rs1.getString("toode").trim();
			      }
			    }

			 
			    //System.out.println(  " prod1 " + prod );
			    rs1.close();
			    st1.close();
			
				
				    }	    
				    
			  }else{
				  
			  }
			  
		    return prod;
		  }

		

	  public void makeOrder(Order order) throws SQLException{
		  tagSeis();
		  hardEAN();
		  Vector splitted =  splitOrder(order);
      if("true".equals(parent.getProps().getProperty("testing"))){


          }
		  Enumeration en = splitted.elements();
		  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement();
			  if(ord.getOrderLines().size() > 0){
				    setOrder(ord);
				    makeOutput(ord);
				}
		     }
	     }

	  private String getStat(String cust) throws SQLException{
		  	String res = "1";
		 
		  	//Kui maksja kaardil on staatus erinev 0 - st.
		  	String	sql1 = "select(Select (case when SL01060>0 or SL01075>0 then 0 else 1 end)"
		  			+ " from SL010100 where SL01001 = k.SL01056 ) as blokk2 " +
	                     " from SL010100 k where SL01001 = '" + cust + "' "; 
					
					Statement st1 = con.createStatement();
					ResultSet rs1 = st1.executeQuery(sql1);
					
					if (rs1.next()) {
					     res = rs1.getString("blokk2");
					     
			          }
					
			rs1.close();
			st1.close();
			if(res.equals("0")){
				return res;
			}
			
			String payer = null;
			
			String sqlp = "Select SL01056  as maksja from SL010100 where SL01001 = '" + cust + "'";
			st1 = con.createStatement();
		    rs1 = st1.executeQuery(sqlp);
			
			if (rs1.next()) {
			     payer = rs1.getString("maksja");
			     if(payer == null || payer.trim().length() == 0){
			    	payer = cust; 
			     }
	          }
			
			rs1.close();
			st1.close();
			
			//kui on ylemakset�htaja arveid
			String sql2 = "SELECT SUM((SL03013-SL03053)/(case when SL03015 = 0 then 1 else 15.6466 end)) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
            " NOT(SL03013=SL03053) AND SL03006<dateadd(day,-1,GETDATE())  and SL01001='" + payer + "'  and "
            		+ "SL01001 not in ('0634','0104','1901','1986','1072', '2416' ) GROUP BY SL01001 ";
			
			st1 = con.createStatement();
		    rs1 = st1.executeQuery(sql2);
		    double  volg = 0;
			if (rs1.next()) {
			     volg = rs1.getDouble("debt");
			  }

			rs1.close();
			st1.close();
			
			if(volg > 0){
				return "0";
			}
			
			
			//kui on ylemakset�htaja arveid
			String sql3 = "SELECT max(SL01037) - SUM((SL03013-SL03053)/(case when SL03015 = 0 then 1 else 15.6466 end)) AS debt FROM SL030100 INNER JOIN SL010100 ON SL03001 = SL01001  WHERE  " +
            " NOT(SL03013=SL03053)  and SL01001='" + payer + "'  and SL01001 not in ('0634','0104','1901','1986','1072', '2416') GROUP BY SL01001 ";
			
			st1 = con.createStatement();
		    rs1 = st1.executeQuery(sql3);
		    double  limiit = 0;
			if (rs1.next()) {
				limiit = rs1.getDouble("debt");
			  }

			rs1.close();
			st1.close();
			
			if(limiit < 0){
				return "0";
			}						
			
		  	return res ;
		  }
		 
	  private double getDiscCode(String cust) throws SQLException{
		  	double res = 0;
		 	 String	sql1 = "select SC45005 as proce from SL010100 inner JOIN SC450100 ON SL01036=SC45003 where SL01001 = '" + cust + "'"; 
					
					Statement st1 = con.createStatement();
					ResultSet rs1 = st1.executeQuery(sql1);
					
					if (rs1.next()) {
	    				res = rs1.getDouble("proce");
					
					}
					
			rs1.close();
			st1.close();
		  	return res ;
		  }
		 
		  private boolean otsiKoodi(String custid)  throws SQLException {
			  
			  String	sql1 = "select MJkoodivalik as proce from CUSTOMERDATA0100   where SL01001 = '" + custid + "'"; 
				boolean res = true;
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql1);
				
				if (rs1.next()) {
  				String aa = rs1.getString("proce");
				   if(aa != null && aa.trim().length() >0 && aa.trim().equals("1")){
					   res = false;
				   }
				}
				
		rs1.close();
		st1.close();
			  return res;
		  }
	  
	  
	  private String getDoknr(String aa) throws SQLException{
	  	String res = null;
	 	 String	sql1 = "select SY68002 as nr from SY6801XX where SY68001 = '" + aa + "'"; 
				
				Statement st1 = con.createStatement();
				ResultSet rs1 = st1.executeQuery(sql1);
				
				if (rs1.next()) {
				res = rs1.getString("nr").trim();
				
				Statement ss = con.createStatement();
				String sql2 = "UPDATE SY6801XX set SY68002=SY68002+1 where SY68001 = '" +aa +"'";
				 ss.executeUpdate(sql2);
				 ss.close();
				
				}
				
				
				
		rs1.close();
		st1.close();
	  	return res ;
	  }
	 
	  private void sendTagastusEmail(String custid, String nimi ,Vector lines ,String agent, String comment,  String aadress ){
	    	try{
	    		
	    		if(lines.size() > 0){
	    			
	    			String text = "";
	    			
	    			for (int i = 0; i < lines.size(); i++) {
	    		     	OrderLine line = (OrderLine)lines.elementAt(i);
	    		     	text = text + "<br><b><font size=\"5\"> [" + (int)(-1*line.getKogus()) + "] </b></font>  " + this.getProdNimi(line.getToode());
	    		    }
	    			
	    			Email mail = new Email(null);
	 	  		    mail.setSMTP(parent.getProps().getProperty("smtphost"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		mail.setType("text/html");
		  	  		mail.setSubjectFilesTo("Tagastuse soov  "  + custid + " " + nimi);
		  	  		String maksja = this.getPayer(custid);
		  	  		mail.setBody("<h><br><font size=\"6\">Tagastuse soov</font></b>&nbsp;&nbsp m��giesindaja : " + agent  + " <br><br><b><font size=\"5\">" + custid +  " " + nimi 
		  	  				+ "  " + aadress + " </font></b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Tel : " + getPhone(custid)+"<br><br><hr>" + text
		  	  				+ " <br><br>Lisainfo : " + comment + "<br><br><br><br>Maksja klient :" + maksja + " " + this.getNimi(maksja) + "</h1>" );
    
		  	  		//"Maksja  nimi ja kood ka  kommentaar "
                    
		  	  		mail.setSendNotice(false);
		  
		  	  		String toemail = parent.getProps().getProperty("returnemail");
		  	  	StringTokenizer st = new StringTokenizer(toemail,";") ;
		  	   
		  	  	while (st.hasMoreTokens()){
		  	  	 mail.setFilesTo(st.nextToken());
		  	  	 mail.send();
		  	  	}
	  	  		
		  	  		
		  	  		System.out.println("\nTagastuse Soov " + agent  + custid +  " " + nimi + "\n\n" + text);
		  				
	    		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	    }  
	  
    private void sendCommentEmail(String comment,String doknr,String custid, String nimi){
    	try{
    		
    		if(comment != null && comment.trim().length() >0){
    			Email mail = new Email(null);
 	  		   
	  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		mail.setSubjectFilesTo("Tellimuse Kommentaar " + doknr + " " + custid);
	  	  		
	  	  		mail.setBody("\nTellimuse Kommentaar " +  doknr + " " + custid +  " " + nimi + "\n\n" +
	  	  				  "\n >>>>>" + comment + "<<<<<"
	  	  				);
	  	  		mail.setSendNotice(false);
	  	  		mail.send();
	  	  		System.out.println("\nTellimuse Kommentaar " +  doknr + " " + custid + "\n\n" +
	  	  				  "\n >>>>>" + comment + "<<<<<");
	  				
    		}
	  	
	  			}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
    }
	
    private void sendKinnitusEmail(String custid, String nimi ,Vector lines ,String agent, String comment,  String aadress ){
    	try{
    		
    		if(lines.size() > 0){
    			
    			String text = "";
    			for (int i = 0; i < lines.size(); i++) {
    		     	OrderLine line = (OrderLine)lines.elementAt(i);
    		     	text = text + "<br><b><font size=\"5\"> [" + (int)(line.getKogus()) + "] </b></font>  " + this.getProdNimi(line.getToode());
    		    }
    			Email mail = new Email(null);
 	  		    mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		mail.setType("text/html");
	  	  		mail.setSubjectFilesTo("Saabus tellimus  "  + custid + " " + nimi);
	  	  		String maksja = this.getPayer(custid);
	  	  		mail.setBody("<h><br><font size=\"6\">Saabus Tellimus</font></b>&nbsp;&nbsp<br><br><b><font size=\"5\">" + custid +  " " + nimi 
	  	  				+ "  " + aadress + " </font></b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Tel : " + getPhone(custid)+"<br><br><hr>" + text
	  	  				+ " <br><br>Lisainfo : " + comment + "<br><br><br><br>Maksja klient :" + maksja + " " + this.getNimi(maksja) + "</h1>" );
                
	  	  		mail.setSendNotice(false);
	  	  		String toemail = parent.getEksportFrame().getEksportPanel().parseFirstEmail(parent.getEksportFrame().getEksportPanel().getEmail(agent));
	  	  		System.out.println("Email kuhu saadame teate " + toemail);
	  	  	if("true".equals(parent.getProps().getProperty("testing"))){
	  	  	    mail.setFilesTo("aivar@mobilejava.ee");
	  	  	    }else{
	  	  	    mail.setFilesTo(toemail);
	  	  	    }
	  	  	     
	  	  	     mail.send();

    		}
	  	
	  			}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
    }  
    
    private void sendTTTEmail(String custid, String nimi ,Vector lines ,String agent, String comment,  String aadress ){
    	try{
    		
    		if(lines.size() > 0){
    			
    			
    			String text = "TTT KOOD(ID) PUUDU !";
    			
    			Email mail = new Email(null);
 	  		    mail.setSMTP(parent.getProps().getProperty("smtphost"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		mail.setType("text/html");
	  	  		mail.setSubjectFilesTo("Kliendil : " +  custid + " puuduvad TTT kood(id)");
	  	  		
	  	  		mail.setBody("<h><br><font size=\"6\">Saabus Tellimus</font></b>&nbsp;&nbsp<br><br><b><font size=\"5\">" + custid +  " " + nimi 
	  	  				+ "  " + aadress + " </font></b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br><br><hr>" + text
	  	  				+ " <br><br>Klient : " +  custid + "<br><br><br><br></h1>" );
                
	  	  		mail.setSendNotice(false);
	  	  		String toemail = "vigadee@prike.ee";
	  	  		System.out.println("Email kuhu saadame teate " + toemail);
	  	  	    mail.setFilesTo(toemail);
     	      mail.send();

    		}
	  	
	  			}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
    }  

    private boolean sendKinnitus(String custid ){
    	String asi = parent.getProps().getProperty("kinnitus.kliendid");
    	if(asi != null){
    		StringTokenizer st = new StringTokenizer(asi,";");
    		while(st.hasMoreTokens()){
    			String cust = st.nextToken();
    		   if(custid.indexOf(cust) != -1){
    			   return true ;
    		   }
    		}
    	}
    		/*/*
    		 *     	
    		 */
    		return false;
    }
    
	  private void makeOutput(Order order){
		 	StringBuffer sb = new StringBuffer();
		 	Hashtable pandikas =  new Hashtable();
		 	String ls = System.getProperty("line.separator");
		    StringBuffer sb2 = new StringBuffer();
	   	 	    
		 	   
		 	try {
		 	  int cou = 0;
		
		 	 int ccc = 1;
		 	    String cust = order.getCompany();
				String agent = this.identifyCustomerAGENT(cust);
				double custdiscount = this.getCustDiscount(cust);
				if(order.getStatus() > 2 && agent.equals("MUU") 
						 ){
					agent = order.getAgent();
				}
				String hinnakiri = getHinnakiri(cust);
				
				double jae = 0;
		      	if(hinnakiri.equals("01")){
		      		jae = 1;
		      	}
				String com = order.getEmailBody();
				if ( com == null ){
					com =  " ";
				}		
				String ware = order.getWarehouse();
				if ( ware == null ){
					ware = " ";
				}
				
				
				 String doknr = "";

		    
				 if(order.getPaymTerm() == -2){
		        	  doknr = getDoknr("OR09");
		          }else if( 0 > ((OrderLine)order.getOrderLines().elementAt(0)).getKogus() ) {
		        	  doknr = getDoknr("OR02");
		          }else{
		        	  doknr = getDoknr("OR21");
		          }
				 
				 boolean demagoogia  = false;
				
		         String mtaht = null;
		        String status = "1";
		         
		         String type = "1";
		        
		         try{
		        	    status = getStat(cust);   	 
		         }catch (Exception e){
		        	 e.printStackTrace();
		         }
		     
		         
		         
		         sb2.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + ls);
			      sb2.append("<msg:Msg xsi:schemaLocation=\"http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:msg=\"http://Epicor.com/Message/2.0\">" + ls);			      
			      sb2.append("  <msg:Hdr>" + ls);
			      sb2.append("   <msg:Sender>" + ls);
			      sb2.append("     <msg:Name>MobileJava</msg:Name>" + ls);
			      sb2.append("     <msg:Subname>ImportModule</msg:Subname>" + ls);
			      sb2.append("   </msg:Sender>" + ls);
			      sb2.append("    <msg:Ctrl>" + ls);
			      sb2.append("      <msg:MsgID>" + Calendar.getInstance().getTime().getTime() + "</msg:MsgID>" + ls);
			      sb2.append("    </msg:Ctrl>" + ls);
			      sb2.append("   </msg:Hdr>" + ls);
			      sb2.append("   <msg:Body>" + ls);
			      sb2.append("      <msg:Req msg-type=\"Sales Order\" action=\"Process\">" + ls);
			      sb2.append("    <msg:Dta>" + ls);
			      sb2.append("    <dta:SalesOrder xsi:schemaLocation=\"http://www.scala.net/SalesOrder/1.1 http://scshost/schemas/Scala/1.1/SalesOrder.xsd\" xmlns:msg=\"http://Epicor.com/InternalMessage/1.1\" xmlns:dta=\"http://www.scala.net/SalesOrder/1.1\">" + ls);		      
			      sb2.append("     <dta:OrderHeader>" + ls);
			    
			      String ref = "";
		         
	         
		         
		    Vector lines = order.getOrderLines();
		   
		    int i = 0;
		    boolean tagastus = false;
		    
		    boolean otsikoodi = otsiKoodi(order.getCompany());
		    
		    for (i = 0; i < lines.size(); i++) {
		      	OrderLine line = (OrderLine)lines.elementAt(i);
		          String toode = line.getToode();
		          if(order.isChanged() && order.getStatus() < 5){

			       // Madise soovil et pihukatest tulevatel suitst tellimusetel koodi ei muudeta 
		          }else{
			          if(order.getStatus() > 2){
				          toode = identifyProduct2(toode,line.getKogus());  
			          } else if (otsikoodi){
			        	  toode = identifyProduct3(toode,line.getKogus());  
			          }  
		          }
		          
		          
		          
		          // otsime ean uuesti ja sama piires vaatan laosaldosid - ja valin vanima tootekoodi veini ja kanget eraldi ei k�sitleta
		          // SC01116 tooteloomise kuup�ev ja 
		          
		          
		          if (i == 0 ){
		        		try{  
		        			String abitoode = toode;
		        			if(abitoode == null){
		        				abitoode = ((OrderLine)lines.elementAt(lines.size()-1)).getToode();
		        			}
		        	  String grupp = identfyGroup(abitoode);
		       
		        	  mtaht = getMakse(order.getCompany(),grupp);
                       }catch(Exception e){
		            	e.printStackTrace();	
		            	}
		          	if( line.getKogus() < 0){
		          		System.out.println("kogus " + line.getKogus());
		              	type = "1";
		              	status = "0";
		              	tagastus = true;
		          	}
		          	
		          	
		            String comment = order.getInfo();
		            
		            System.out.println("Kommentaari saatmine " + order.getStatus() + " " + comment);
		            if(order.getStatus() < 10 && !tagastus){
		            	try{
		                	sendCommentEmail(comment, doknr, order.getCompany(),getNimi(order.getCompany()));
		        		            		
		            	}catch(Exception e){
		            		e.printStackTrace();
		            	}
		           }
		            
		            
			          System.out.println("Payment " + order.getPaymTerm());
			          if(order.getPaymTerm() == -2){
			        	  comment = "Real";
			        	//  doknr = getDoknr("OR09");
			        	  mtaht = "91";
			          }
			          if(order.getPaymTerm() == -5){ //ettemaks
			        		type = "1";
			              	status = "0";
			          } 
		            try{
			          if(this.getEttemaks(order.getCompany())){
			        		type = "1";
			              	status = "0";
			          }
			          
		               }catch(Exception e){
		            	e.printStackTrace();	
		            	}
		          
			          if(comment == null || comment.trim().length() == 0 ){
			         	comment = "";
			          }
			          
			      	ref = order.getDokNr();
					if ( ref == null ){
						ref =  " ";
					}
					String asik = ref;
					if(order.isChanged()){
						ref =  "T" + ref ;
						//if(isZeroStatus(order.getCompany())){
							//status = "0";
						//}
     						if(!isokTTTStatus(order.getCompany())){
							sendTTTEmail(order.getCompany(),getNimi(order.getCompany()),lines, this.identifyCustomerAGENT(cust) ,
					    			  order.getInfo(), getAddress(order.getCompany()));
							status = "0";
						} 
					}
			         
			
					if(comment.indexOf(asik) == -1 ){
				          ref = ref + " " + comment;	
					}else if(ref.trim().length() < 2 || order.getStatus() < 3 ){
						if(order.isChanged()){
							ref = "T " +comment;
						}else{
							ref = comment;
						}
						
					}
					
					System.out.println("ref " + ref);
			        
					
					
			          if(ref.trim().length()>70){
			        	  ref = ref.substring(0,70);	
			         	}
			          
			            Calendar cal = Calendar.getInstance();
			    	    Calendar dat = order.getShipCal();
			    	  //  order.isShipDateUsed();
			    	    if(dat != null){
			    	    	System.out.println("Cal Shipdate "+ dat.getTime().toLocaleString());	
			    	    }

			    	
			    	    if (dat != null && (order.getStatus() < 10 || order.getStatus() == 98 || order.getStatus() == 80) ){
			    	    	if(dat.get(Calendar.DAY_OF_YEAR)< Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
			    	    		dat.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
			    	    	}
			    	    	cal.setTimeInMillis(dat.getTimeInMillis());
			    	    }
			
			    	      sb2.append("      <dta:OrdType>1</dta:OrdType>" + ls);
					      sb2.append("      <dta:WhID>01</dta:WhID>" + ls);
					      sb2.append("      <dta:OrdStatus>" + status + "</dta:OrdStatus>" + ls);
					  
			    	    	if(order.getStatus() >10){
			    	    	     sb2.append("      <dta:CustPONum>" +  Converter.escapeXML(order.getDokNr()) + "</dta:CustPONum>" + ls);
			    	    	}else{
			    	    		 sb2.append("      <dta:CustPONum>" +  Converter.escapeXML(doknr) + "</dta:CustPONum>" + ls);
			    	    	}
			    	  sb2.append("      <dta:OrdNum>" +  Converter.escapeXML(doknr) + "</dta:OrdNum>" + ls);
				      sb2.append("      <dta:DelivDate>" + escape(getDateString(cal)) + "</dta:DelivDate>" + ls);
				      sb2.append("      <dta:OrdDate>" + escape(getDateString(Calendar.getInstance())) + "</dta:OrdDate>" + ls);
				      sb2.append("      <dta:CustCodeOrd>" + escape(order.getCompany()) + "</dta:CustCodeOrd>" + ls);	
				      sb2.append("      <dta:AccStr>" + ls);
				      sb2.append("          <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
				      sb2.append("      </dta:AccStr>" + ls);
				      sb2.append("      <dta:SalesmanNum>" + agent + "</dta:SalesmanNum>" + ls);
				      sb2.append("      <dta:CustCodeDeliv>" + escape(order.getCompany()) + "</dta:CustCodeDeliv>" + ls);
				      if(mtaht != null){
					      sb2.append("      <dta:TermPay>" + mtaht + "</dta:TermPay>" + ls);    	  
				      }		
				      
				      String ref1 = ref.trim();
				      String ref2 = null;
				      if(ware!= null && ware.trim().length() >0){
				    	  ref2 = escape(getDateString(cal)) + "T"+ ware;
						   
				      }
				      
				      if(order.getStatus()  == 98) {
				    	  ref2 = escape(getDateString(cal)) ;
				      }
				    
				      
				      if( order.getStatus() == 200) {
							ref1 = comment;
							if(order.isChanged()){
								ref1 =  "T" + ref1 ;
								if(isZeroStatus(order.getCompany())){
									status = "0";
								}
								if(!isokTTTStatus(order.getCompany())){
									sendTTTEmail(order.getCompany(),getNimi(order.getCompany()),lines, this.identifyCustomerAGENT(cust) ,
							    			  order.getInfo(), getAddress(order.getCompany()));
									status = "0";
								}
							}
							if(ref.trim().length()>70){
					        	  ref = ref.substring(0,70);	
					         	}
						}
				      
				        if(ref1.length() > 35){
				    	  ref2 = ref1.substring(35);
				    	  ref1 = ref1.substring(0,35);
				    	 
				          }
				      
		              sb2.append("      <dta:Remark1>" +  Converter.escapeXML(ref1) + "</dta:Remark1>" + ls);
		              if(ref2 != null){
		            	  sb2.append("      <dta:Remark2>" +  Converter.escapeXML(ref2) + "</dta:Remark2>" + ls);	      	      
				      }
				      sb2.append("     </dta:OrderHeader>" + ls);
				      sb2.append("     <dta:OrderLines>" + ls);
		          	
		          }
		          
		          
		          if(toode.trim().length() != 0){
		          if(toode.startsWith("XX")){
		        	  toode = toode.substring(2);
		        	  demagoogia = true;
		          }
		          String pant = getPandiPakend(toode);
		          double pandikordaja = getPandiKordaja(toode);
		          if(pant != null ){
		        	  
		        	  double aaa = line.getKogus()*pandikordaja;
		         	  if(pandikas.get(pant) == null){
		         		  pandikas.put(pant,aaa+"");
		               } else {
		               	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+(aaa))+"");
		               }
		         	  
		         //	 System.out.println("Kordaja " + pandikordaja + "  " + pandikas);
		            }
		          
		       
		        	 Product prodExtra = this.getHaldusCustomerAndProduct(order.getCompany(), line);
		        	 if(prodExtra != null){
		        		 toode = prodExtra.getAgentGroup();
		        		 if(prodExtra.getGroup() != null && prodExtra.getGroup().trim().length() > 0 ){
		        			 //kui on ka teised andmed ja tingimused klapivad siis peame lisama toote juurde teeme seda l�bi pandika
		        			
		        			 String lisa = prodExtra.getGroup();
		        			 double kog = line.getKogus()/prodExtra.getStock() * prodExtra.getOnPallet();
		        			  
		   		         	  if(pandikas.get(lisa) == null){
		   		         		  pandikas.put(lisa,(kog) + "");
		   		               } else {
		   		               	  pandikas.put(lisa,(new Double((String)pandikas.get(lisa)).doubleValue()+(kog))+"");
		   		               }
		   		         }
		        	  /*Product prod = new Product(ean);
							prod.setStock(Double.parseDouble(kogus));
							prod.setGroup(lisakood);
							prod.setAgentGroup(uuskood);
							prod.setOnPallet(Double.parseDouble(lisakogus));*/	 
		        	 }
		          
		        
		          
		            double disc = line.getDiscount();
		            if(order.getStatus() == 60){
		            	disc = 0;
		            }
		             int kogus = (int)line.getKogus();
		             boolean prisma = false;
		             if("true".equals(parent.getProps().getProperty("prisma.convert")) && 
		            		 order.getILN() != null && order.getILN().equals("4747001000009")){
		            	 prisma = true;
		             }
		             
		             boolean rimi = false;
		             if("true".equals(parent.getProps().getProperty("prisma.convert")) && 
		            		 order.getILN() != null && order.getILN().equals("4747001000009")){
		            	// rimi = true;
		             }
		             
		             if(order.getStatus() == 98 || order.getCompany().equals("T01044") 
		            		 || order.getCompany().equals("T1072-20") || order.getCompany().equals("T24161")
		            		 || order.getCompany().startsWith("T6248") 
		            		 || order.getCompany().startsWith("T5739")
		            		 || order.getCompany().startsWith("T8587")//lidl
		            		 ||  (order.getILN() != null && order.getILN().equals("4740660999899"))
		            		 || (order.getILN() != null && prisma)
		            		 ){//prismad 
		            	 
		            	 kogus = (int)(kogus /getJagaja(toode));
		            	 
		             }
		             //niv toodete teema maxima + rimi 
		             if((order.getILN() != null && order.getILN().equals("4751008570045")) || order.getStatus() == 98) {
		            	 kogus = (int)(kogus /getJagajaNIV(toode));
		             }
		    
		      	sb.append(addSpaces(doknr, 10));
		      	sb.append(type);//11
		      String	ladu = "01";
		    
		      	if(demagoogia){
		      		agent ="REK";
		      		ladu = getDemaLadu(toode,kogus);
		      	}
		      	
		        sb.append(addSpaces(cust, 10));//22
		        sb.append(getToday());//46
		        sb.append(getTarne(order));//46 siia pane dokkuup		       
		        sb.append(addSpaces(agent, 3));//49	      
		        sb.append(addSpaces(toode, 20));//113
		        sb.append(addSpaces(nf.format(kogus), 8));//129
		        sb.append(addSpaces(ref,70));//181
		        
		        sb.append(ladu);// Laokood 
		        String zallahtext = null;
		        double hind = 0;
		        double hindstd = 0;
		    	hindstd = getStdPrice(toode);
		        double hindArvu = 0;
		        double hindAle = 0;
		        
		        if(order.getStatus() == 60){
		        	System.out.println("Lisan Telema tabelisse");
		        	  this.addToTelemaDB(ref, cust, line.getToode(), line.getTooteNimi(), line.getEAN(), kogus);
			   }
		        if(disc > 0){
		        	if((line.getDiscount() > 0 && line.getDiscount() < 3) || (line.getDiscount() > 0 && line.getPl() == 99)){
		        		int ddd = (int)(line.getDiscount()*10);       	
		       			disc = ddd / (line.getKogus()) * 100;
		       		
		       		    hind = getPrice(toode,hinnakiri,null,-1);
		       		    hindAle = getPrice(toode,hinnakiri,cust,hind);
		       		    double ddisc = getDiscZall(getPayer(cust),toode);
		       		   
		       		    if(ddisc == 0 && custdiscount > 0){
		       		    	ddisc = custdiscount;
		       		    }
		       		 double dii = (100-ddisc)/100;
	    			   if(dii != 0){
	    				  hindArvu = hind * dii *(100-disc)/100;
	    			   }else  if(hindAle != 0 ){
		       		      hindArvu = hindAle  * (100-disc)/100;
		       		   }else{
		       		      hindArvu = hind *(100-disc)/100;
		       		   }
		       		   
		       		 //   hindArvu = hindArvu *(100 - custDisc)/100; // kommentisin v�lja kuna pole vaja 
		       		    disc = (1- hindArvu/hind)* 100;
		       		    
		       		
		        		zallahtext = "Toode on m��dud " + (int)(line.getKogus()-ddd) + "+" + ddd + " allahindlusega";
		        		System.out.println("Zallah Disc " + (disc) + " kogus" + kogus + 
		        				" " + zallahtext + " hind arvu " + hindArvu + " " + hindAle + " " + hind
		        				 );
		        		// siia uus arvutus teha zallahi alusel 
		        		//sb.append(addSpaces(nf.format(disc), 6));
		        		disc = disc-jae;
		        		sb.append(addSpaces((disc)+ "", 6)); // discount	
		        		 addToDB(agent,cust,toode,kogus,disc,"EST",kogus,true);
		        	}else{
		        		disc = disc-jae;
			           	sb.append(addSpaces(nf.format(disc), 6)); // discount	
		        	}
		        }else{
		           	sb.append(addSpaces("", 6)); // discount
		        }
		    
		        
		       
		       
		        if(disc > 2){
		          addToDB(agent,cust,toode,kogus,disc,"EST",0,false);
		        }
		        
		        sb.append(addSpaces("", 10));
		        if(zallahtext != null){
		        	 sb.append(addSpaces(zallahtext, 70));
		        }else{
		        	 sb.append(addSpaces("", 70));
		        }
		        sb.append(addSpaces(status, 1));
		        sb.append(System.getProperty("line.separator"));
		        ccc++;
		        
		          sb2.append("      <dta:OrderLine>" + ls);
		          sb2.append("         <dta:LineNum>" + getLineNr(ccc) + "</dta:LineNum>" + ls);
		          sb2.append("         <dta:StockCode>" + escape(toode) + "</dta:StockCode>" + ls);
		          sb2.append("         <dta:QtyOrdered>" + nf.format(kogus) + "</dta:QtyOrdered>" + ls);
			      	if(hind > 0){
			         // sb2.append("         <dta:UnitPrice>" + nf.format(hind) + "</dta:UnitPrice>" + ls);
			      	}
			      
			    	   if(disc > 1){
			    		   if(zallahtext != null){
			    			  
			    			    sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(disc),6) + "</dta:CustDisc>" + ls);
			    			    sb2.append("         <dta:UnitPrice>" + nf.format(hindstd) + "</dta:UnitPrice>" + ls);
			    		   }else{
			    			   if(custdiscount > 0){
					                sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(disc),6) + "</dta:CustDisc>" + ls);			           
					                sb2.append("         <dta:UnitPrice>" + nf.format(hindstd) + "</dta:UnitPrice>" + ls);
					             } else {
					            	 sb2.append("         <dta:CustDisc>" + addSpaces(nf.format(disc),6) + "</dta:CustDisc>" + ls);			           
						             sb2.append("         <dta:UnitPrice>" + nf.format(hindstd) + "</dta:UnitPrice>" + ls);
					             }   
			    		     }
			    		 }
			    	   
		          sb2.append("         <dta:AccStr>" + ls);
			      sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
			      sb2.append("         </dta:AccStr>" + ls);
			      sb2.append("         <dta:TextLines>" + ls);
			      if(zallahtext != null){
			          sb2.append("               <dta:TextLine>" + zallahtext + "</dta:TextLine>" + ls);
			        }else{
			          sb2.append("               <dta:TextLine></dta:TextLine>" + ls);
			        }
			      sb2.append("         </dta:TextLines>" + ls);
			      
                  sb2.append("      </dta:OrderLine>" + ls);
		        
                  if(pant != null && pant.trim().length() > 0){
                	  
                	 ccc++;
                	     sb2.append("      <dta:OrderLine>" + ls);
				          sb2.append("         <dta:LineNum>" + getLineNr(ccc) + "</dta:LineNum>" + ls);
				          sb2.append("         <dta:StockCode>" + escape(pant) + "</dta:StockCode>" + ls);
				          sb2.append("         <dta:QtyOrdered>" + nf.format(kogus*pandikordaja).replace(',','.').replaceAll(" ","") + "</dta:QtyOrdered>" + ls);
				          sb2.append("         <dta:AccStr>" + ls);
					      sb2.append("               <dta:AccDim1>" + agent + "</dta:AccDim1>" + ls);
					      sb2.append("         </dta:AccStr>" + ls);
					      sb2.append("      </dta:OrderLine>" + ls);
					      
                  }
                  
		       cou++;
		      }
		 	}
              nf.setMaximumFractionDigits(3);
              nf.setGroupingUsed(false);
              
		    	
		      setNumAddedLines(getNumAddedLines() + cou);
		      String add = agent;
		      if(add.equals("   ")){
		    	  add = "MUU";
		      }
	       
	          
	          sb2.append("        </dta:OrderLines>" + ls);
		      sb2.append("      <dta:TextLines />" + ls);
		      sb2.append("    </dta:SalesOrder>" + ls);
		      sb2.append("  </msg:Dta>" + ls);
		      sb2.append("</msg:Req>" + ls);
		      sb2.append("</msg:Body>" + ls);
		      sb2.append("</msg:Msg>" + ls);
		    
		      
		      if(!tagastus){
		    	  if(ccc > 1){
		    		
		    	  try{
				    	String fileName2 = escape(parent.getProps().getProperty("conv.dir3")  + order.getAgent())
				    	+ "." + doknr   +  ".xml";
			          	FileOutputStream fos = new FileOutputStream(fileName2);
				        fos.write(sb2.toString().getBytes());
				        fos.close();
			            	
				    }catch(Exception e){
				    	e.printStackTrace();
				    }
				  
				    try{
				    	String fileName3 = escape(parent.getProps().getProperty("conv.dir4")  + order.getAgent())
				    	+ "." + doknr   +  ".xml";
			          	FileOutputStream fos = new FileOutputStream(fileName3);
				        fos.write(sb2.toString().getBytes());
				        fos.close();
			            	
				    }catch(Exception e){
				    	e.printStackTrace();
				    }
				    
				    if(sendKinnitus(order.getCompany())){
			        	sendKinnitusEmail(order.getCompany(),getNimi(order.getCompany()),lines, this.identifyCustomerAGENT(cust) ,
				    			  order.getInfo(), getAddress(order.getCompany()));
			        }
				    
		    	  }
		      }else{
		    	  this.sendTagastusEmail( order.getCompany(),getNimi(order.getCompany()),lines, order.getAgent() ,
		    			  order.getInfo(), getAddress(order.getCompany()));
		      }
		      
		  
		  
	          
	          
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
		  

			  
	 private double getDiscZall(String cust, String toode) {
		 double res = 0;
			try{
		 String	sql1 = " select SC08004 as disc from SC080100 where  getdate() between cast(SC08005 as datetime) and "
		 		     + " (case when SC08006 = '99999999' then getdate()+1 else cast(SC08006 as datetime) end) " +
                       " and (Select ltrim(SC01037)   FROM SC010100 pr  where pr.SC01001='" + toode + "') = SC08003"
                       		+ " and ltrim(SC08002) = '" + cust + "'"; 
			
					Statement st1 = con.createStatement();
					ResultSet rs1 = st1.executeQuery(sql1);
					
					if (rs1.next()) {
	    				res = rs1.getDouble("disc");
					}
					
			       rs1.close();
			       st1.close();	
			       System.out.println("ale tagasi " + res );
				}catch(Exception r){
					r.printStackTrace();
				}
			
	  	return res ;
	}


	public String getDriverVersion(){
	    return "ScalaPrikeEstImport";
	  }

	  private void setOrder(Order order){

	  }


	  private String escape(String s){
	    if (s == null) return s;
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0 ; i < s.length() ; i++){
	      char c = s.charAt(i);
	      if (c == '&'){
	        sb.append("&amp;");
	      } else if (c == '<'){
	        sb.append("&lt;");
	      } else if (c == '>'){
	        sb.append("&gt;");
	      } else if (c == '\n'){
	        sb.append(" ");
	      } else {
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }

	  

	  private String getToday(){
	    Calendar cal = Calendar.getInstance();
	    String result = calNF.format(cal.get(Calendar.YEAR)) +
	                    calNF.format(cal.get(Calendar.MONTH) + 1) +
	                  calNF.format(cal.get(Calendar.DAY_OF_MONTH));
	    return result;
	  }

	  private String getTarne(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = order.getShipCal();

	    String result = null ;
	    if (dat != null && order.getStatus() < 10){
	    	if(dat.get(Calendar.DAY_OF_YEAR)< Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
	    		dat.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
	    	}
	    	 String yy = (dat.get(Calendar.YEAR))+"";
	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (dat.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  
	    	  result = yy.substring(2,4) + mm + dd;
	    	  
	    }else{
	    
	    	 String yy = (cal.get(Calendar.YEAR))+"";

	    	  String mm = (cal.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (cal.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
		  result = yy.substring(2,4) + mm + dd;
		  System.out.println("P�EV " + result + " " + dat.getTime().toString());
	    }
	    return result;
	  }
	  
	  
	  public static final String addSpaces(String s, int totalLen){
	    int len = s.length();
	    StringBuffer sb = new StringBuffer(s);
	    for (int i=len; i < totalLen ; i++){
	      sb.append(' ');
	    }
	    if( len > totalLen){
	    	return s.substring(0,totalLen);
	    }
	    return sb.toString();
	  }
	  
	  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
	  private NumberFormat calNF = NumberFormat.getInstance();
	  
	  
	  public String getProdNimi(String id) throws SQLException {
		
		  String sql1 = " Select ( RTRIM(SC01002) +rtrim(SC01003))  AS nimi FROM SC010100 pr  where pr.SC01001='" + id + "'";
		 	
		   System.out.println(sql1);
		   String agent = null;

		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   while (rs1.next()) {
		   	agent = rs1.getString("nimi").trim();

		   }
		   rs1.close();
		   st1.close();
	     
		   return agent;

		 }
	  
	  public String getProdGroup(String id) throws SQLException {
			
		  String sql1 = " Select ( RTRIM(SC01002) +rtrim(SC01003))  AS nimi FROM SC010100 pr  where pr.SC01001='" + id + "'";
		 	
		   System.out.println(sql1);
		   String agent = null;

		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   while (rs1.next()) {
		   	agent = rs1.getString("nimi").trim();

		   }
		   rs1.close();
		   st1.close();
	     
		   return agent;

		 }
	  
	  
	  public String getCustNimi(String id) throws SQLException {
		
		  String sql1 = " Select ( RTRIM(SC01002) +rtrim(SC01003))  AS nimi FROM SC010100 pr  where pr.SC01001='" + id + "'";
		 	
		   System.out.println(sql1);
		   String agent = null;

		   Statement st1 = con.createStatement();
		   ResultSet rs1 = st1.executeQuery(sql1);

		   while (rs1.next()) {
		   	agent = rs1.getString("nimi").trim();

		   }
		   rs1.close();
		   st1.close();
	     
		   return agent;

		 }

	  private String url = "jdbc:odbc:scalaDB";
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
	  private Connection con;
	  private TreeMap zalMap = new TreeMap();
	  private Connection con2;
	  private ImportPanel parent;

	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}
	}
