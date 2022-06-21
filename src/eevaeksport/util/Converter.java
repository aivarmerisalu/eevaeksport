package eevaeksport.util;
 
import java.awt.Color;
import java.awt.Graphics2D;




import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.image.codec.jpeg.JPEGCodec;

import eevaeksport.Agent;
import eevaeksport.Contract;
import eevaeksport.ContractLine;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.driver.Toode;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import imageUtil.Image;
import imageUtil.ImageLoader;

public class Converter {

	/**
	 * @param args
	 * @throws CharacterCodingException 
	 */
	public static boolean isPersonalcode(String code){
		
		try{
			if(code != null && code.length() == 11 && isNumeric(code)
					&& (code.startsWith("3") || code.startsWith("4" )|| code.startsWith("5") || code.startsWith("6"))
							){
					int mm = Integer.parseInt(code.substring(3,5));
					int dd = Integer.parseInt(code.substring(6,7));
					
					if(mm < 13 && dd < 32){
						return true;
					}
				}
					
		}catch(Exception r){
			
		}
		
		return false;
	}
	
	   
	   public static TreeMap loadMore() {
		   TreeMap res = new TreeMap();
		   try {
			   
			   File ff= new File("eans.txt");
			   if(!ff.exists()) {
				  ff =  new File("excelsort/eans/eans.txt");
			   }
			   FileInputStream is = new FileInputStream(ff);
				InputStreamReader isr = new InputStreamReader(is, "utf-8");
				BufferedReader reader = new BufferedReader(isr);
				
				
			    String    line = null;
			 
			    try {
			        while((line = reader.readLine()) != null) {
			      StringTokenizer st = new StringTokenizer(line,";");
			      while(st.hasMoreTokens()) {
			    	  res.put(st.nextToken(), st.nextToken());
			      }
			        }

			       
			    } finally {
			        reader.close();
			    }
			 
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
		   
		
		   
		 System.out.println("lis suurus " + res.size());
		   
		   return res;
	   }
	
	
	 public static TreeMap loadFixSelver(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			 Workbook workbook = null;
			 Sheet sheet = null;
			       FileInputStream	 ff = new FileInputStream(file);
			       if(file.getName().endsWith(".xlsx")) {
			    	   workbook = new XSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
			       }else {
			    	    workbook = new HSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
					
			       }
					
					
					System.out.println("Selver lugemine " + file.getName());
					
					ff.close();	
					
					if(sheet != null){
						TreeMap groups = new TreeMap();
					   Row row = sheet.getRow(0);
					
						
						int r = 28;
						
						boolean found = false;
						int addrow = 0;
						int ea = 5;
						
						for (int a = 0; a < row.getLastCellNum(); a++) {
							try {
							
								//System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.toLowerCase().indexOf("ean") != -1) {
									ea = a;
								}
								if(gg.toLowerCase().indexOf("järve") != -1) {
									r = a;
									found = true;
								
									break;
								}
								if(gg.toLowerCase().indexOf("kadaka") != -1) {
									r = a;
									found = true;
								
									break;
								}
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
					
						
						
						if(!found) {
							 row = sheet.getRow(1);
							
							
							
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
								
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("ean") != -1) {
										ea = a;
										if(gg.toLowerCase().indexOf("järve") != -1) {
											r = a;
											found = true;
											addrow = 1;
											break;
										}}
									
									if(gg.toLowerCase().indexOf("kadaka") != -1) {
										r = a;
										found = true;
										addrow = 1;
										break;
									}
								
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}
						
						if(!found) {
							 row = sheet.getRow(2);
							
							
							
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
								
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("ean") != -1) {
										ea = a;
									}
									if(gg.toLowerCase().indexOf("järve") != -1) {
										r = a;
										found = true;
										
										addrow = 2;
										break;
									}
									if(gg.toLowerCase().indexOf("kadaka") != -1) {
										r = a;
										found = true;
										
										addrow = 2;
										break;
									}
								
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}
						if(!found) {
							 row = sheet.getRow(3);
							
							
							boolean jarve = false;
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
								
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("ean") != -1) {
										ea = a;
									}
									if(gg.toLowerCase().indexOf("järve") != -1) {
										jarve = true;
									}
									if(gg.toLowerCase().indexOf("kadaka") != -1) {
										
										r = a;
										if(jarve) {
											r=r-1 ; // kui järve on esimesena siis teeme kadaka -1
										}
										found = true;
										addrow = 3;
										break;
									}
								
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}
					
						
						if(!found) {
							 row = sheet.getRow(15);
							
							
							
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
								
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("ean") != -1) {
										ea = a;
									}
									if(gg.toLowerCase().indexOf("kadaka") != -1) {
										r = a;
										found = true;
										addrow = 15;
										break;
									}
								
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}
						
					
						
						for (int a = r; a < row.getLastCellNum(); a++) {
							try {
								 System.out.println(row.getCell((short)a).getStringCellValue() + " " + r);
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.toLowerCase().indexOf("ean") != -1) {
									ea = a;
								}
								if(gg.indexOf("E-pood") != -1) {
									break;
								}
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    
							      
							    }
							
							} catch (Exception e) {
								 System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
						//System.out.println("r "  + r + " "+ ea + " " + addrow + " " + groups.toString());
						//System.exit(0);
						
						  
						for(int i = 1+addrow ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							 DataFormatter formatter = new DataFormatter();
								String ean = formatter.formatCellValue(row.getCell(ea));
								String tnimi = formatter.formatCellValue(row.getCell(ea+2));
								String toode = getToode(prods,ean);
							 if(!"Nimetus".equals(tnimi)) {
								 
							 
							  if(toode.equals(ean) ) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Selver formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
								
							  }
							 
							 for (int a = r; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0) {
							 
                                     String pood = "SEL-" + group;
                                     Contract con = new Contract(pood);
							
                                   String value =  formatter.formatCellValue(row.getCell(a));
                                   
                                    
                                    if(value.trim().length() > 0 && !value.trim().equals("1")) {
                                    	// System.out.println("Sees " +a + " " + value + " " + ean);
                                    
							if(res.get(pood) != null){
                        	  con = (Contract)res.get(pood);
                        	 } 
							  ContractLine ll = new ContractLine(toode);
                        	  if(con.lines == null){
                        		  con.lines = new Vector();
                        	  }
                        	  con.lines.add(ll);
                        	  res.put(pood, con);
                                    }
						   }
							 }
							 }
						}
					 }
					
					
					//System.out.println("sorts size " + res.toString() );
					//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "selver",true);
		 }
		 sendFixLoadErrorEmail("\n Selver imported " + res.keySet().size() + " shops fixed sortiments", parent, "selver",false);

 	return res;
 }
	 
	 public static TreeMap loadFixKaup(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			 Workbook workbook = null;
			 Sheet sheet = null;
			       FileInputStream	 ff = new FileInputStream(file);
			       if(file.getName().endsWith(".xlsx")) {
			    	   workbook = new XSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
			       }else {
			    	    workbook = new HSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
					
			       }
					
					
					System.out.println("Kaupmehe lugemine " + file.getName());
					
					ff.close();	
					
					if(sheet != null){
						TreeMap groups = new TreeMap();
					   Row row = sheet.getRow(0);
					
						
						int r = 1;
						int ea = 0;
						int ni = 1;
						
						
						for (int a = 0; a < row.getLastCellNum(); a++) {
							try {
							
								//System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.toLowerCase().indexOf("vöötkood") != -1 ) {
									ea = a;
								}
								if(gg.toLowerCase().indexOf("kauba nimetus") != -1 ) {
									ni = a;
								}
								
								if(gg.toLowerCase().indexOf("1ll") != -1 || gg.toLowerCase().indexOf("1ka") != -1) {
									r = a;
									break;
								}
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
					
						
						
						
						for (int a = r; a < row.getLastCellNum(); a++) {
							try {
							//	 System.out.println(row.getCell((short)a).getStringCellValue() + " " + r);
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
						
						System.out.println("Groups " + 
						r + " " + ea + " " + ni);
						
						
						for(int i = 1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 try {
								 
							
							 DataFormatter formatter = new DataFormatter();
								String ean = formatter.formatCellValue(row.getCell(ea));
								String tnimi = formatter.formatCellValue(row.getCell(ni));
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Kaupmees formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
								
							  }
							 
							 for (int a = r; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0) {
							 
                                  String pood = "KAUP-" + group;
                                  Contract con = new Contract(pood);
							
                                String value =  formatter.formatCellValue(row.getCell(a));
                                
                                 
                                 if(value.trim().length() > 0 && (value.trim().equals("PS") || value.trim().equals("HK") )) {
                                 	 System.out.println("Sees " +a + " " + value + " " + ean + " "+ r);
                                 
							if(res.get(pood) != null){
                     	  con = (Contract)res.get(pood);
                     	 } 
							  ContractLine ll = new ContractLine(toode);
                     	  if(con.lines == null){
                     		  con.lines = new Vector();
                     	  }
                     	  con.lines.add(ll);
                     	  res.put(pood, con);
                                 }
						   }
							 }
						 }catch(Exception e) {
							 e.printStackTrace();
						 }}
					 }
					
					
					//System.out.println("sorts size " + res.toString() );
				//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "kaupmees",true);
		 }
		 sendFixLoadErrorEmail("\n Kaupmees imported " + res.keySet().size() + " shops fixed sortiments", parent, "kaupmees",false);

	return res;
}


	 public static TreeMap loadFixKaup2(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			 Workbook workbook = null;
			 Sheet sheet = null;
			       FileInputStream	 ff = new FileInputStream(file);
			       if(file.getName().endsWith(".xlsx")) {
			    	   workbook = new XSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
			       }else {
			    	    workbook = new HSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
					
			       }
					
					
					System.out.println("Kaupmehe lugemine " + file.getName());
					
					ff.close();	
					
					if(sheet != null){
						TreeMap groups = new TreeMap();
					   Row row = sheet.getRow(0);
					
						
						int r = 1;
						int ea = 0;
						int ni = 1;
						
						
						
						for (int a = 0; a < row.getLastCellNum(); a++) {
							try {
							
								//System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.toLowerCase().indexOf("vöötkood") != -1 ) {
									ea = a;
								}
								if(gg.toLowerCase().indexOf("kauba nimetus") != -1 ) {
									ni = a;
								}
								
								if(gg.toLowerCase().equals("Kad")) {
									r = a;
									break;
								}
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
					
						
						
						for (int a = r; a < row.getLastCellNum(); a++) {
							try {
							//	 System.out.println(row.getCell((short)a).getStringCellValue() + " " + r);
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
								if(gg.equals("Narv")) {
									break;
								}
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
						System.out.println("Groups " +  r + " " + ea +" "+ ni);
						
						
						for(int i = 1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							 DataFormatter formatter = new DataFormatter();
								String ean = formatter.formatCellValue(row.getCell(ea));
								String tnimi = formatter.formatCellValue(row.getCell(ni));
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Kaupmees formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
								
							  }
							 
							 for (int a = r; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0) {
							 
                               String pood = "KAUP-" + group;
                               Contract con = new Contract(pood);
							
                             String value =  formatter.formatCellValue(row.getCell(a));
                             
                              
                              if(value.trim().length() > 0 && (value.trim().equals("PS") || value.trim().equals("HK") )) {
                              //	 System.out.println("Sees " +a + " " + value + " " + ean + " "+ r);
                              
							if(res.get(pood) != null){
                  	  con = (Contract)res.get(pood);
                  	 } 
							  ContractLine ll = new ContractLine(toode);
                  	  if(con.lines == null){
                  		  con.lines = new Vector();
                  	  }
                  	  con.lines.add(ll);
                  	  res.put(pood, con);
                              }
						   }
							 }
						}
					 }
					
					
					//System.out.println("sorts size " + res.toString() );
				//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "kaupmees",true);
		 }
		 sendFixLoadErrorEmail("\n Kaupmees imported " + res.keySet().size() + " shops fixed sortiments", parent, "kaupmees",false);

	return res;
}
	 

	 public static TreeMap loadFixcoopRist(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			 Workbook workbook = null;
			 Sheet sheet = null;
			       FileInputStream	 ff = new FileInputStream(file);
			       if(file.getName().endsWith(".xlsx")) {
			    	   workbook = new XSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
			       }else {
			    	    workbook = new HSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
					
			       }
					
					
					System.out.println("CoopRist lugemine " + file.getName());
					
					ff.close();	
					
					if(sheet != null){
						TreeMap groups = new TreeMap();
					   Row row = sheet.getRow(0);
					
						
						int r = 28;
						
						boolean found = false;
						int addrow = 0;
						
						for (int a = 0; a < row.getLastCellNum(); a++) {
							try {
							
								//System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.toLowerCase().indexOf("abja") != -1) {
									r = a;
									found = true;
								
									break;
								}
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
					//	System.out.println("R " + r );
						
						
						
						if(!found) {
							 row = sheet.getRow(1);
							
							 for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("abja") != -1) {
										r = a;
										found = true;
										addrow = 1;
										break;
									}
								
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}
						
						if(!found) {
							 row = sheet.getRow(2);
							
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
								
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("abja") != -1) {
										r = a;
										found = true;
										
										addrow = 2;
										break;
									}
								
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}
						
						
						
						for (int a = r; a < row.getLastCellNum(); a++) {
							try {
								// System.out.println(row.getCell((short)a).getStringCellValue() + " " + r);
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							} catch (Exception e) {
								 System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
						//System.out.println("Groups " +  groups.toString());
						//System.exit(0);
						
						for(int i = 1+addrow ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							 DataFormatter formatter = new DataFormatter();
								String ean = formatter.formatCellValue(row.getCell(0));
								String tnimi = formatter.formatCellValue(row.getCell(1));
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Coop formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
								
							  }
							 
							 for (int a = r; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0) {
							 
                                  String pood = "COOP-" + group;
                                  Contract con = new Contract(pood);
							
                                String value =  formatter.formatCellValue(row.getCell(a));
                                
                                 
                                 if(value.trim().length() > 0 && !value.trim().equals("-")) {
                                 	// System.out.println("Sees " +a + " " + pood + " " + value + " " + ean + " " + tnimi);
                                 
							if(res.get(pood) != null){
                     	  con = (Contract)res.get(pood);
                     	 } 
							  ContractLine ll = new ContractLine(toode);
                     	  if(con.lines == null){
                     		  con.lines = new Vector();
                     	  }
                     	  con.lines.add(ll);
                     	  res.put(pood, con);
                                 }
						   }
							 }
						}
					 }
					
					
					//System.out.println("sorts size " + res.toString() );
					//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "coop",true);
		 }
		 sendFixLoadErrorEmail("\n Coop imported " + res.keySet().size() + " shops fixed sortiments", parent, "coop",false);

	return res;
}
	 

	 public static TreeMap loadFixMaxima(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					XSSFSheet sheet = workbook.getSheetAt(0); 
					XSSFSheet sheet2 = workbook.getSheetAt(1); 
					 DataFormatter formatter = new DataFormatter();
					ff.close();	
					TreeMap prodSortLevels = new TreeMap();
				
					TreeMap groups = new TreeMap();
					TreeMap nimed = new TreeMap();
					System.out.println("Maxima lugemine " + file.getName());
					// esimeselt lehelt loeme sisse kuni3 blokki infot .ja paneme buffrisse.
					//loeme mitu blokki on olemas ja kust algavad .
					// sort1 sort2 sort3 
					
					
					if(sheet != null){
						XSSFRow row = sheet.getRow(0);
						
						for (int a = 0; a < row.getLastCellNum(); a++) {
							try {
								// System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim();
								
								if(gg.equals("SAP")){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								
							}
						}
						
					//	System.out.println("Groups " +  groups.toString());
					
						
						 //siin käime kõik grupid läbi ja otsime tooteid 
						Iterator itera = groups.keySet().iterator();
						int loe = 0;
						while(itera.hasNext()) {
							int start = Integer.parseInt(itera.next()+"");
					    loe++;
						
						for(int i = 1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							 
							
							 //System.out.println("uu " + (1+start));
								String ean = formatter.formatCellValue(row.getCell(1+start));
								//System.out.println("uu " + ean);
								 if(ean != "") {
										
								String tnimi = formatter.formatCellValue(row.getCell(2+start));
								String toode = getToode(prods,ean);
								String level = loe +  "|" + formatter.formatCellValue(row.getCell(3+start));
								
								nimed.put(toode, tnimi);
							  if(toode.equals(ean)  ) { //  && false
								  
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Maxima formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }else {
								  
								  if(toode != null && toode.trim().length() > 0) {
								
								  Toode tt = new Toode(toode);
								  tt.comment = level;
								  tt.setLadu(tnimi);
								  ArrayList arr = new ArrayList();
								  if(prodSortLevels.get(start) != null) {
									  arr = (ArrayList) prodSortLevels.get(start) ;
								  }
								  arr.add(tt);
								  
								  prodSortLevels.put(start, arr); 
								//  System.out.println("uu " + start + " " + i + " "+ toode);
								  }
							  }
							  
							 }
							
						}
						}
					 }
					
					//nyyd loeme kliendi info sisse koos leveliga ja kohe sortimendi ka otsa 
					//System.out.println("suurus " + prodSortLevels.keySet().toString());
					
					Iterator itera = groups.keySet().iterator();
					
					int loe2 = 0;
					while(itera.hasNext()) {
					
						
						
				        int gr = Integer.parseInt(itera.next()+"");
				        int start = gr/5 * 4 ;//loe2 * 4;
				        
				    //	System.out.println("start " + start + " " + gr );
					
						loe2++;
					for(int i = 1 ; i < sheet2.getLastRowNum()+1;i++){
						Row row = sheet2.getRow(i);
						
						
						 
							String pood = formatter.formatCellValue(row.getCell(start));
							String level = formatter.formatCellValue(row.getCell(2+start));
							// if(start == 32) 
								// System.out.println("start " + start + " " + gr + " " + pood);
						
							 
						 if(pood.trim().length() > 0) {
						//	 if(pood.equals("T287")) 
							// System.out.println("start " + start + " " + gr + " " + pood);
							 
						 pood = "MAX-" + pood;
						  Contract con = new Contract(pood);
			                   //	 System.out.println("Sees " + pood);
			                   
							if(res.get(pood) != null){
			       	           con = (Contract)res.get(pood);
			       	           } 
							
							
							
							//siin siis vaatame läbi mis tooted kaasa panna 
							ArrayList arr = (ArrayList)prodSortLevels.get(gr);
							//if(pood.equals("MAX-T287")) 
							 //System.out.println("gr11" + " " + gr + " "+ arr);
							 
							if(arr != null) {
								//if(pood.equals("MAX-T287")) 
						 //System.out.println("gr10" + " " + gr + " "+ level + " " +start + " " + arr.size() );
									 
								
								for(int y = 0; y < arr.size() ; y++ ) {
									Toode tt = (Toode)arr.get(y);
									
									
									String uu = tt.comment;
									int lll = Integer.parseInt(uu.substring(0, 1));
									try {
									
									int aaa = Integer.parseInt(tt.comment.substring(2)) ;
									
									// if(pood.equals("MAX-T287")) 
										// System.out.println("gr188" +  " "+ level + " " +aaa );
									
									if(lll == loe2 && aaa <=  Integer.parseInt(level) ) {
										ContractLine ll = new ContractLine(tt.getID());
									      if(con.lines == null){
						       		           con.lines = new Vector();
						       	            }
						       	          con.lines.add(ll);
						       	     /* if(pood.equals("MAX-T287")) {
											System.out.println("Toode  " + tt.getID()  + " " + tt.comment + " "
												+ level + " " + loe2 + " " + tt.comment + " " + gr + " "+ level);
										}*/
						       	          
						       	         res.put(pood, con);
									}
									
									} catch(Exception e) {
										
								   }
							    }
							}
			             }
					  }
					}
				 
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		 
	
			//System.out.println(  "MAX-T287 " +((Contract) res.get("MAX-T287")).lines.size());
		
		//System.exit(0);
		 
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "maxima",true);
		 }
		 sendFixLoadErrorEmail("\n Maxima imported " + res.keySet().size() + " shops fixed sortiments", parent, "maxima",false);
		// System.exit(0);
	return res;
}
	
	 
	 
	 public static TreeMap loadFixAldar(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					XSSFSheet sheet = workbook.getSheetAt(0); 
					System.out.println("Aldari lugemine " + file.getName());
					ff.close();	
					 DataFormatter formatter = new DataFormatter();
					if(sheet != null){
						XSSFRow row = sheet.getRow(3);
						TreeMap groups = new TreeMap();
						for (int a = 3; a < row.getLastCellNum(); a++) {
							try {
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								 System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
					//	System.out.println("Groups " +  groups.toString());
						 row = sheet.getRow(3);
						 int y = 1;
						 if(formatter.formatCellValue(row.getCell(0)).equals("Ribakood")) {
							 y = 0;
						 }
						 
						 int z = 2;
						 if(formatter.formatCellValue(row.getCell(0)).equals("Ribakood")) {
							 z = 2;
						 }
						 
						 if(formatter.formatCellValue(row.getCell(1)).equals("Ribakood")) {
							 z = 3;
						 }
						for(int i = 4 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							
								String ean = formatter.formatCellValue(row.getCell(y));
								String tnimi = formatter.formatCellValue(row.getCell(z));
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Aldar formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							 for (int a = 3; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0) {
							 
                                  String pood = "ALD-" + group;
                                  Contract con = new Contract(pood);
							 String value =  formatter.formatCellValue(row.getCell(a));
                                
                                 
                                 if(value.trim().length() > 0) {
                                 	// System.out.println("Sees " +a + " " + value + " " + ean);
                                 
							if(res.get(pood) != null){
                     	  con = (Contract)res.get(pood);
                     	 } 
							  ContractLine ll = new ContractLine(toode);
                     	  if(con.lines == null){
                     		  con.lines = new Vector();
                     	  }
                     	  con.lines.add(ll);
                     	  res.put(pood, con);
                                 }
						   }
							 }
						}
					 }
					
					
			//	System.out.println("Aldarsorts size " + res.toString() );
				//	System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "aldar", false);
		 }
		 sendFixLoadErrorEmail("\n Aldar imported " + res.keySet().size() + " shops fixed sortiments", parent, "aldar",false);
	return res;
}
	
	 public static TreeMap loadFixPrisma2(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					XSSFSheet sheet = workbook.getSheetAt(0); 
					System.out.println("Prisma2 lugemine " + file.getName());
					ff.close();	
					 DataFormatter formatter = new DataFormatter();
					
					if(sheet != null){
						XSSFRow row = sheet.getRow(0);
						
						int ea = 0;
						int ni = 0;
						int gr = 2;
						int ri = 1;
					//	System.out.println("Groups " +  groups.toString());
						try {
					
						 for(int i = 0 ; i < row.getLastCellNum();i++){
							 if(formatter.formatCellValue(row.getCell(i)).contains("EAN")) {
								 ea = i;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("Desc")) {
								 ni = i;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("Siku")) {
								 gr = i;
							 }
						 }
							
						}catch(Exception t) {
							
						}
						
						 row = sheet.getRow(2);
						 for(int i = 0 ; i < row.getLastCellNum();i++){
							 if(formatter.formatCellValue(row.getCell(i)).contains("EAN")) {
								 ea = i;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("Desc") 
									 || formatter.formatCellValue(row.getCell(i)).contains("Nime")) {
								 ni = i;
								 
								 ri = 3;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("Prof")) {
								 gr = i;
							 }
						 }
						 
						 System.out.println("aaaa " + ea + " " + ni);
					
					
					
						
						for(int i = ri ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							
								String ean = formatter.formatCellValue(row.getCell(ea));
								String tnimi = formatter.formatCellValue(row.getCell(ni));
								String groupt  = formatter.formatCellValue(row.getCell(ni+1)).trim(); 
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Prisma2 formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							 int len = groupt.length();
							 int old = 0;
							 
							 for (int a = 2; a < len + 1; a = a+2) {

									String group = groupt.substring(old,a);
									old = a;
									System.out.println("Sees " +a + " " + group + " " + ean);
									
					if (group != null && group.trim().length() > 0) {
							 
                            String pood = "PRIS-" + group;
                            
                        	   Contract con = new Contract(pood);
                               if(res.get(pood) != null){
                             	  con = (Contract)res.get(pood);
                    	             } 	
							  ContractLine ll = new ContractLine(toode);
               	  if(con.lines == null){
               		  con.lines = new Vector();
               	  }
               	  con.lines.add(ll);
               	  res.put(pood, con);
                           
						   }
							 }
				//	System.out.println("Sees " + groupt +" " + res.toString());
                    
					//System.exit(0);
							
						}
					 }
					
					
			//	System.out.println("Prisma size " + res.toString() );
				//	System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		 
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "Prisma2",true);
		 }
		 
		 sendFixLoadErrorEmail("\n Prisma2 imported " + res.keySet().size() + " shops fixed sortiments", parent, "prisma2",false);
	
		 return res;
}
	 public static TreeMap loadFixPrisma(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					XSSFSheet sheet = workbook.getSheetAt(0); 
					System.out.println("Prisma lugemine " + file.getName());
					ff.close();	
					 DataFormatter formatter = new DataFormatter();
					
					if(sheet != null){
						XSSFRow row = sheet.getRow(0);
						
						int ea = 0;
						int ni = 0;
						int gr = 2;
					//	System.out.println("Groups " +  groups.toString());
						
						 for(int i = 0 ; i < row.getLastCellNum();i++){
							 if(formatter.formatCellValue(row.getCell(i)).contains("EAN")) {
								 ea = i;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("Desc")) {
								 ni = i;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("TOODE")) {
								 ni = i;
							 }
							 if(formatter.formatCellValue(row.getCell(i)).contains("Siku")) {
								 gr = i;
							 }
						 }
						
						TreeMap groups = new TreeMap();
						for (int a = gr; a < row.getLastCellNum(); a++) {
							try {
								// System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim().replaceAll("\n", " ");
							
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								 System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
					
						
						for(int i = 1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							
								String ean = formatter.formatCellValue(row.getCell(ea));
								String tnimi = formatter.formatCellValue(row.getCell(ni));
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Prisma formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							 for (int a = 2; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0) {
							 
                               String pood = "PRIS-" + group;
                               Contract con = new Contract(pood);
							 String value =  formatter.formatCellValue(row.getCell(a));
                             
                              
                              if(value.trim().length() > 0) {
                              	// System.out.println("Sees " +a + " " + value + " " + ean);
                              
							if(res.get(pood) != null){
                  	  con = (Contract)res.get(pood);
                  	 } 
							  ContractLine ll = new ContractLine(toode);
                  	  if(con.lines == null){
                  		  con.lines = new Vector();
                  	  }
                  	  con.lines.add(ll);
                  	  res.put(pood, con);
                              }
						   }
							 }
						}
					 }
					
					
			//	System.out.println("Prisma size " + res.toString() );
				//	System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "Prisma",true);
		 }
		 sendFixLoadErrorEmail("\n Prisma imported " + res.keySet().size() + " shops fixed sortiments", parent, "prisma",false);
	return res;
}
	
	 
	 
	 public static TreeMap loadFixRimi(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					Sheet sheet = workbook.getSheetAt(0); 
					System.out.println("Rimi lugemine " + file.getName());
					ff.close();	
					
					//rea tuvastus teha siia
					
					if(sheet != null){
						int pais = 1;
						int r = 4;
						int ea = 0;
						int ni = 3;
						for(int i = 0 ; i < sheet.getLastRowNum()+1;i++){
							Row row = sheet.getRow(i);
							if(row != null) {
							
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
								
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("ener") != -1) {
										r = a;
										pais = i;
										break;
									}else if(gg.toLowerCase().indexOf("er47") != -1) { // kui see tuleb enne
										r = a;
										pais = i;
										//System.out.println(gg);
										break;
									}
									if(gg.toLowerCase().indexOf("ean") != -1) {
										ea = a;
									}
									if(gg.toLowerCase().indexOf("i kirjeldus") != -1) {
										ni = a;
									}
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
						}	
						}
						
						
						
						Row row = sheet.getRow(pais);
						
					
						TreeMap groups = new TreeMap();
						for (int a = r; a < row.getLastCellNum(); a++) {
							try {
								// System.out.println(row.getCell((short)a).getStringCellValue());
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.indexOf("HM") != -1) {
									break;
								}
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								// System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
						//System.out.println("Groups " +  groups.size());
						
						
						for(int i = pais+1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
							 
							 DataFormatter formatter = new DataFormatter();
								String ean = formatter.formatCellValue(row.getCell(ea)).trim();
								if(ean.length() > 13) {
									ean = ean.substring(1);
								}
								 System.out.println("Sees  '" + ean + "'" + ean.trim());
								String tnimi = formatter.formatCellValue(row.getCell(ni));
								String toode = getToode(prods,ean);
							 
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n RIMI formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							 for (int a = r; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0 ) {
							 
                               String pood = "RIMI-" + group;
                               Contract con = new Contract(pood);
							 String value =  formatter.formatCellValue(row.getCell(a));
                             
                              
                              if(value.trim().length() > 0 && (value.trim().equals("x") || value.trim().equals("1")  || value.trim().equals("Yes"))) {
                              	 System.out.println("Sees " +a + " " + value + " '" + ean + "' " + pood + " " + tnimi);
                              
							if(res.get(pood) != null){
                  	  con = (Contract)res.get(pood);
                  	 } 
							  ContractLine ll = new ContractLine(toode);
                  	  if(con.lines == null){
                  		  con.lines = new Vector();
                  	  }
                  	  con.lines.add(ll);
                  	  res.put(pood, con);
                              }
						   }
							 }
						}
					 }
					
					
				//System.out.println("RIMIsorts size " + res.toString() );
					//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "rimi", true);
		 }
		 sendFixLoadErrorEmail("\n Rimi imported " + res.keySet().size() + " shops fixed sortiments", parent, "rimi",false);
			 
	return res;
}

	 
	 
	 public static int doubleLength(double number) {
		 int length = 1;
		 if (number >= 100000000) {
		     length += 8;
		     number /= 100000000;
		 }
		 if (number >= 10000) {
		     length += 4;
		     number /= 10000;
		 }
		 if (number >= 100) {
		     length += 2;
		     number /= 100;
		 }
		 if (number >= 10) {
		     length += 1;
		 }
		 return length;
	 }
	 
	 public static int popularint(Vector<Integer> a){
	   int count = 1, tempCount;
	   int popular = 0;
	   int temp = 0;
	   for (int i = 0; i < (a.lastElement() - 1); i++)
	   {
	     temp = a.get(i);
	     tempCount = 0;
	     for (int j = 1; j < a.lastElement(); j++)
	     {
	       if (temp == a.get(j))
	         tempCount++;
	     }
	     if (tempCount > count)
	     {
	       popular = temp;
	       count = tempCount;
	     }
	   }
	   return popular;
	 }
	 
	 
	 public static int diffValues(int[] numArray){
		    int numOfDifferentVals = 0;

		    ArrayList<Integer> diffNum = new ArrayList<>();

		    for(int i=0; i<numArray.length; i++){
		        if(!diffNum.contains(numArray[i])){
		            diffNum.add(numArray[i]);
		        }
		    }

		    if(diffNum.size()==1){
		            numOfDifferentVals = 0;
		    }
		    else{
		          numOfDifferentVals = diffNum.size();
		        } 

		   return numOfDifferentVals;
		}
	 
	 
	 
	 public static TreeMap loadFixElvi(TreeMap res, File file , TreeMap prods , EksportPanel parent) {

		 Vector check = new Vector();
			String errors = null; 
			TreeMap <String,Vector> klaster= new TreeMap<String,Vector>();
			
			
			
			Vector <String> EAN = new Vector<String>();				//ean koodid stringina
			Vector <Integer> EANcolumn = new Vector<Integer>();		//mis ridadel asuvad ean koodid
			Vector <String> PRODUCT = new Vector<String>();
			//Vector <String> CLUSTER = new Vector<String>();
			int dataLocation=1;
			
			int productLoc=0;
			int clusterLoc=0;
			int clusterLocx=0;
			
			DataFormatter formatter2 = new DataFormatter();
		 try{
			 
		       FileInputStream	 ff = new FileInputStream(file);
				XSSFWorkbook workbook = new XSSFWorkbook(ff); 
				Sheet sheet = workbook.getSheetAt(0); 
				Sheet sheet1 = workbook.getSheetAt(1); 
				System.out.println("Rimi lugemine " + file.getName());
				ff.close();	
			 
			 
			 int viimanerida=sheet.getLastRowNum();
			 for(int xx=0 ; xx<viimanerida; xx++) { 		
				 if(sheet.getRow(xx)!=null) {			
					 
					 int cellsInRow=sheet.getRow(xx).getLastCellNum();
					 
					 for(int yy=0 ; yy<cellsInRow; yy++) {							//loeme cellid read
						 if(sheet.getRow(xx).getCell(yy)!=null) {
							 
							 String celliSisu=formatter2.formatCellValue(sheet.getRow(xx).getCell(yy));	//loeme celli stringina
							 if(celliSisu.isEmpty()==false && celliSisu!=null && celliSisu.contains("EAN")==true) {		//vaatame kus asuvad headlined, tiitlite vector parameetrina?
								 System.out.println("EAN Row: "+xx+ "Col: "+yy);
							 }
							 if(celliSisu.isEmpty()==false && celliSisu!=null && celliSisu.contains("Article name")==true) {
								 System.out.println("Products Row: "+xx+ "Col: "+yy);
								 productLoc=yy;
							 }
							 if(celliSisu.isEmpty()==false && celliSisu!=null && (celliSisu.contains("EEH") || celliSisu.contains("EEMR") )) { //rimi
								 System.out.println("Cluster Row: "+xx+ "Col: "+yy);
								 if(clusterLoc!=0) {break;} //otsime ainult esimese klastri veeru tabelis - sealt edasi ainult klustrid lehe lopuni
								 clusterLoc=yy;
								 clusterLocx=xx;
							 }
							 
							 
							 if(yy>=clusterLoc && xx>clusterLocx) {
								 
								 if(celliSisu.contains("x") || celliSisu.contains("X") || celliSisu.contains("add")) {
									 String currentCluster=formatter2.formatCellValue(sheet.getRow(clusterLocx).getCell(yy)).replaceAll(" " , "");
									 String currentProduct=formatter2.formatCellValue(sheet.getRow(xx).getCell(productLoc));
									 String currentProductEAN=formatter2.formatCellValue(sheet.getRow(xx).getCell(productLoc-1));
										 
									 Vector <Product> tooted = new Vector<Product>();
									 if(klaster.get(currentCluster) == null){
										 Product prod = new Product(currentProduct);
										 prod.setName(currentProduct);
										 prod.setEAN(currentProductEAN);
										 tooted.add(prod);
										 klaster.put(currentCluster, tooted);
										 //System.out.println("Uustoode: " +currentProduct+"  klaster: "+ currentCluster);
									 } else {
									 	 /*if(currentCluster.contains("EEHM A.12.01 ALL Large1 460")) {
											 System.out.println("Lisame: "+ currentCluster+" "+currentProduct);
											 System.out.println(klaster.get(currentCluster).toString());
											 System.out.println(klaster.get(currentCluster).size());
										 }*/
										 Product prod = new Product(currentProduct);
										 prod.setName(currentProduct);
										 prod.setEAN(currentProductEAN);
										 //System.out.println("lisasime: "+currentProduct +" " +klaster.get(currentCluster).contains(currentProduct)+"  klaster: "+ currentCluster);
										 klaster.get(currentCluster).add(prod);
										 
									 }
							
									 
									 
									 
								 }
							 }

							 
							 if(celliSisu.isEmpty()==false && celliSisu!=null && isNumeric(celliSisu) ) {														//kas string on number
								 Double number=Double.parseDouble(celliSisu);										//string->double
								 if(number>1) {
									 if(doubleLength(Math.round(number))==13) {												//EAN on 13x pikkune
										 //System.out.println("clustercol: "+clusterLoc);
										 EAN.add(celliSisu);
										 EANcolumn.add(yy);
										//PRODUCT.add(formatter2.formatCellValue(sheet.getRow(xx).getCell(productLoc)));
										//System.out.println("toode: "+yy);
									 }
								 }
								
							 }

						 }
						 
					 }

				 }

			 }
			 
				TreeMap <String,Vector> cluster= new TreeMap<String,Vector>();				//loeme teise lehe andmed
				for(int x=2 ; x<sheet1.getLastRowNum()+1; x++) { //andmed algavad 3realt
					String clusterkey=formatter2.formatCellValue(sheet1.getRow(x).getCell(3)).trim().replaceAll(" " , "");
					String clustervalue=formatter2.formatCellValue(sheet1.getRow(x).getCell(1)).trim();
					//System.out.println("info: " + clusterkey);
						
					Vector <String> sisuvektor = new Vector<String>();
					
					if(cluster.get(clusterkey) != null) {
						sisuvektor=cluster.get(clusterkey);
					}
					sisuvektor.add(clustervalue);
					cluster.put(clusterkey, sisuvektor);
				}
				
				//System.out.println(klaster.get("EEHM A.12.01 ALL Large1 460"));

				//System.out.println(klaster.get("EEHM A.12.01 ALL Large1 460").toString());
			 
				
			 for (int a = 0; a < klaster.keySet().size(); a++) {		//mitu keyd on mapis

				    String group = klaster.keySet().toArray()[a].toString();	//loeme grupi sisse
				   
				    
				    if(cluster.get(group)!=null) {
				   Vector vec = cluster.get(group);
				    	for(int c=0; c < vec.size(); c++) {   ///loeme grupis olevad poekoodid
					    	//cluster.get(group).get(c);
					    	
					    	
					        String pood = "RIMI-" + vec.get(c);
					        if(!check.contains(vec.get(c))) {
					        	check.add(vec.get(c));
					        }
					        Contract con = new Contract(pood);
					        Vector vec2 = klaster.get(group);
					        
					    //    System.out.println("EAN SIZE " + EAN.size() + "' " + vec2.size());
					        
					        for(int b = 0; b < vec2.size(); b++) {			//mitu toodet on v2tmes
					        	Product prod = (Product)vec2.get(b);							//v2tame mapis oleva vectori elemendi
					        	//System.out.println(klaster.get(group).size());
					        	String toode = getToode(prods,prod.getEAN());
					          //  System.out.println("Sees " +b+ " " + prod.getEAN() + "' " + group + " " + prod.getName());

					        	//klaster.get(klaster.keySet().toArray()[a]).toString();
								  if(toode.equals(prod.getEAN())) {
									  if(errors == null) {
										  errors = "Tuvastamatta tooted sortimendis\n RIMI2 formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
									  }
									  if(!errors.contains(prod.getEAN())) {
										  errors = errors + prod.getEAN() + "   " + prod.getName() + "\n";
									  }
								  }
								  
								  
							        if(res.get(pood) != null){
							            con = (Contract)res.get(pood);
							        } 
							        ContractLine ll = new ContractLine(toode);
							        if(con.lines == null){
							            con.lines = new Vector();
							            } 
							        con.lines.add(ll);
							        res.put(pood, con);
					        }
					    }
				    }
				}
			 //System.out.println(klaster.get("EEHM A.12.01 ALL Large1 460").toString());
		 }catch(Exception e) {
						 e.printStackTrace(); 
					 }	 
		 
		// System.out.println("res " + check.toString().replaceAll(", ", "\n"));
		 
		// System.exit(0);
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "rimi2", true);
		 }
		 sendFixLoadErrorEmail("\n Rimi2 imported " + res.keySet().size() + " shops fixed sortiments", parent, "rimi",false);
			 
	return res;
}
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 

	 public static TreeMap loadFixRimi2(TreeMap res, File file , TreeMap prods , EksportPanel parent) {

		 Vector check = new Vector();
			String errors = null; 
			TreeMap <String,Vector> klaster= new TreeMap<String,Vector>();
			
			
			
			Vector <String> EAN = new Vector<String>();				//ean koodid stringina
			Vector <Integer> EANcolumn = new Vector<Integer>();		//mis ridadel asuvad ean koodid
			Vector <String> PRODUCT = new Vector<String>();
			//Vector <String> CLUSTER = new Vector<String>();
			int dataLocation=1;
			
			int productLoc=0;
			int clusterLoc=0;
			int clusterLocx=0;
			
			DataFormatter formatter2 = new DataFormatter();
		 try{
			 
		       FileInputStream	 ff = new FileInputStream(file);
				XSSFWorkbook workbook = new XSSFWorkbook(ff); 
				Sheet sheet = workbook.getSheetAt(0); 
				Sheet sheet1 = workbook.getSheetAt(1); 
				System.out.println("Rimi lugemine " + file.getName());
				ff.close();	
			 
			 
			 int viimanerida=sheet.getLastRowNum();
			 for(int xx=0 ; xx<viimanerida; xx++) { 		
				 if(sheet.getRow(xx)!=null) {			
					 
					 int cellsInRow=sheet.getRow(xx).getLastCellNum();
					 
					 for(int yy=0 ; yy<cellsInRow; yy++) {							//loeme cellid read
						 if(sheet.getRow(xx).getCell(yy)!=null) {
							 
							 String celliSisu=formatter2.formatCellValue(sheet.getRow(xx).getCell(yy));	//loeme celli stringina
							 if(celliSisu.isEmpty()==false && celliSisu!=null && celliSisu.contains("EAN")==true) {		//vaatame kus asuvad headlined, tiitlite vector parameetrina?
								 System.out.println("EAN Row: "+xx+ "Col: "+yy);
							 }
							 if(celliSisu.isEmpty()==false && celliSisu!=null && celliSisu.contains("Article name")==true) {
								 System.out.println("Products Row: "+xx+ "Col: "+yy);
								 productLoc=yy;
							 }
							 if(celliSisu.isEmpty()==false && celliSisu!=null && (celliSisu.contains("EEH") || celliSisu.contains("EEMR") )) { //rimi
								 System.out.println("Cluster Row: "+xx+ "Col: "+yy);
								 if(clusterLoc!=0) {break;} //otsime ainult esimese klastri veeru tabelis - sealt edasi ainult klustrid lehe lopuni
								 clusterLoc=yy;
								 clusterLocx=xx;
							 }
							 
							 
							 if(yy>=clusterLoc && xx>clusterLocx) {
								 
								 if(celliSisu.contains("x") || celliSisu.contains("X") || celliSisu.contains("add")) {
									 String currentCluster=formatter2.formatCellValue(sheet.getRow(clusterLocx).getCell(yy)).replaceAll(" " , "");
									 String currentProduct=formatter2.formatCellValue(sheet.getRow(xx).getCell(productLoc));
									 String currentProductEAN=formatter2.formatCellValue(sheet.getRow(xx).getCell(productLoc-1));
										 
									 Vector <Product> tooted = new Vector<Product>();
									 if(klaster.get(currentCluster) == null){
										 Product prod = new Product(currentProduct);
										 prod.setName(currentProduct);
										 prod.setEAN(currentProductEAN);
										 tooted.add(prod);
										 klaster.put(currentCluster, tooted);
										 //System.out.println("Uustoode: " +currentProduct+"  klaster: "+ currentCluster);
									 } else {
									 	 /*if(currentCluster.contains("EEHM A.12.01 ALL Large1 460")) {
											 System.out.println("Lisame: "+ currentCluster+" "+currentProduct);
											 System.out.println(klaster.get(currentCluster).toString());
											 System.out.println(klaster.get(currentCluster).size());
										 }*/
										 Product prod = new Product(currentProduct);
										 prod.setName(currentProduct);
										 prod.setEAN(currentProductEAN);
										 //System.out.println("lisasime: "+currentProduct +" " +klaster.get(currentCluster).contains(currentProduct)+"  klaster: "+ currentCluster);
										 klaster.get(currentCluster).add(prod);
										 
									 }
							
									 
									 
									 
								 }
							 }

							 
							 if(celliSisu.isEmpty()==false && celliSisu!=null && isNumeric(celliSisu) ) {														//kas string on number
								 Double number=Double.parseDouble(celliSisu);										//string->double
								 if(number>1) {
									 if(doubleLength(Math.round(number))==13) {												//EAN on 13x pikkune
										 //System.out.println("clustercol: "+clusterLoc);
										 EAN.add(celliSisu);
										 EANcolumn.add(yy);
										//PRODUCT.add(formatter2.formatCellValue(sheet.getRow(xx).getCell(productLoc)));
										//System.out.println("toode: "+yy);
									 }
								 }
								
							 }

						 }
						 
					 }

				 }

			 }
			 
				TreeMap <String,Vector> cluster= new TreeMap<String,Vector>();				//loeme teise lehe andmed
				for(int x=2 ; x<sheet1.getLastRowNum()+1; x++) { //andmed algavad 3realt
					String clusterkey=formatter2.formatCellValue(sheet1.getRow(x).getCell(3)).trim().replaceAll(" " , "");
					String clustervalue=formatter2.formatCellValue(sheet1.getRow(x).getCell(1)).trim();
					//System.out.println("info: " + clusterkey);
						
					Vector <String> sisuvektor = new Vector<String>();
					
					if(cluster.get(clusterkey) != null) {
						sisuvektor=cluster.get(clusterkey);
					}
					sisuvektor.add(clustervalue);
					cluster.put(clusterkey, sisuvektor);
				}
				
				//System.out.println(klaster.get("EEHM A.12.01 ALL Large1 460"));

				//System.out.println(klaster.get("EEHM A.12.01 ALL Large1 460").toString());
			 
				
			 for (int a = 0; a < klaster.keySet().size(); a++) {		//mitu keyd on mapis

				    String group = klaster.keySet().toArray()[a].toString();	//loeme grupi sisse
				   
				    
				    if(cluster.get(group)!=null) {
				   Vector vec = cluster.get(group);
				    	for(int c=0; c < vec.size(); c++) {   ///loeme grupis olevad poekoodid
					    	//cluster.get(group).get(c);
					    	
					    	
					        String pood = "RIMI-" + vec.get(c);
					        if(!check.contains(vec.get(c))) {
					        	check.add(vec.get(c));
					        }
					        Contract con = new Contract(pood);
					        Vector vec2 = klaster.get(group);
					        
					    //    System.out.println("EAN SIZE " + EAN.size() + "' " + vec2.size());
					        
					        for(int b = 0; b < vec2.size(); b++) {			//mitu toodet on v2tmes
					        	Product prod = (Product)vec2.get(b);							//v2tame mapis oleva vectori elemendi
					        	//System.out.println(klaster.get(group).size());
					        	String toode = getToode(prods,prod.getEAN());
					          //  System.out.println("Sees " +b+ " " + prod.getEAN() + "' " + group + " " + prod.getName());

					        	//klaster.get(klaster.keySet().toArray()[a]).toString();
								  if(toode.equals(prod.getEAN())) {
									  if(errors == null) {
										  errors = "Tuvastamatta tooted sortimendis\n RIMI2 formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
									  }
									  if(!errors.contains(prod.getEAN())) {
										  errors = errors + prod.getEAN() + "   " + prod.getName() + "\n";
									  }
								  }
								  
								  
							        if(res.get(pood) != null){
							            con = (Contract)res.get(pood);
							        } 
							        ContractLine ll = new ContractLine(toode);
							        if(con.lines == null){
							            con.lines = new Vector();
							            } 
							        con.lines.add(ll);
							        res.put(pood, con);
					        }
					    }
				    }
				}
			 //System.out.println(klaster.get("EEHM A.12.01 ALL Large1 460").toString());
		 }catch(Exception e) {
						 e.printStackTrace(); 
					 }	 
		 
		// System.out.println("res " + check.toString().replaceAll(", ", "\n"));
		 
		// System.exit(0);
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "rimi2", true);
		 }
		 sendFixLoadErrorEmail("\n Rimi2 imported " + res.keySet().size() + " shops fixed sortiments", parent, "rimi",false);
			 
	return res;
}

	 
	 public static TreeMap loadFixMJ(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
			       Workbook workbook = null;
					 Sheet sheet = null;
					       if(file.getName().endsWith(".xlsx")) {
					    	   workbook = new XSSFWorkbook(ff); 
								 sheet = workbook.getSheetAt(0); 
					       }else {
					    	    workbook = new HSSFWorkbook(ff); 
								 sheet = workbook.getSheetAt(0); 
							
					       }
				
					 DataFormatter formatter = new DataFormatter();
					System.out.println("MJSortiment lugemine " + file.getName());
					ff.close();	
					
					
					
					if(sheet != null){
						
						int rida = 0;
						int alg = 3;
						Row  row = sheet.getRow(rida);
						TreeMap groups = new TreeMap();
						for (int a = alg; a < row.getLastCellNum(); a++) {
							try {
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								 System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
					//	System.out.println("Groups " +  groups.size());
						
						
						for(int i = rida+1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
						
							
								String ean = formatter.formatCellValue(row.getCell(0));
								String tnimi = formatter.formatCellValue(row.getCell(2));
								String toode = getToode(prods,ean);
							 if(ean != null && ean.trim().length()> 0 ) {
								 
							
								
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n MJSortiment formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							 for (int a = alg; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0 ) {
							 
                         String pood =  group;
                         Contract con = new Contract(pood);
							 String value =  formatter.formatCellValue(row.getCell(a));
                       
                        
                        if(value.trim().length() > 0 && value.trim().toLowerCase().equals("1")) {
                        //	 System.out.println("Sees " +a + " " + value + " " + ean + " " + pood);
                        
							if(res.get(pood) != null){
            	                 con = (Contract)res.get(pood);
            	                } 
							  ContractLine ll = new ContractLine(toode);
            	               if(con.lines == null){
            		                 con.lines = new Vector();
            	                  }
            	              con.lines.add(ll);
            	              res.put(pood, con);
                        }
						   }
							 }
							 }
						}
					 }
					
					
				//System.out.println("RIMIsorts size " + res.toString() );
					//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "mjsortiment", true);
		 }
		 sendFixLoadErrorEmail("\n MJSortiment imported " + res.keySet().size() + " shops fixed sortiments", parent, "mjsortiment",false);
			 
	return res;
}
	 
	 public static TreeMap loadFixKeila(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		
		 try{
			 Workbook workbook = null;
			  
			       FileInputStream	 ff = new FileInputStream(file);
			       if(file.getName().endsWith(".xlsx")) {
			    	   workbook = new XSSFWorkbook(ff); 
						 
			       }else {
			    	    workbook = new HSSFWorkbook(ff); 
						
					
			       }
			       Sheet sheet = workbook.getSheetAt(0); 
			
					DataFormatter formatter = new DataFormatter();
					
					System.out.println("Keila lugemine " + file.getName());
					
					ff.close();	
					
					if(sheet != null){
						
						int rida = 1;
						int alg = 3;
						Row row = sheet.getRow(1);
						
						for(int i = 0 ; i < sheet.getLastRowNum()+1;i++){
							row = sheet.getRow(i);
							if(row != null) {
						for (int a = 0; a < row.getLastCellNum(); a++) {
							String ean = formatter.formatCellValue(row.getCell(a));
							if(ean.contains("EAN")) {
								rida = i;
								System.out.println("rida2 " + rida + " algus");
						     }
							if(ean.contains("Jaama")) {
								//alg = a;
								//System.out.println("rida1 " + rida + " algus " + alg);
						     }
							if(ean.contains("Turba")) {
							//	alg = a;
								//System.out.println("rida1 " + rida + " algus " + alg);
						     }
						   }
						 }}
						
					
							
							row = sheet.getRow(0);
							for(int i = 0 ; i < sheet.getLastRowNum()+1;i++){
								row = sheet.getRow(i);
							
								if(row != null) {
								
							for (int a = 0; a < row.getLastCellNum(); a++) {
								String ean = formatter.formatCellValue(row.getCell(a));
								if(ean.contains("EAN")) {
									rida = i;
								 }
							   }
							 }
						}	
						
						
						
						System.out.println("rida " + rida + " algus " + alg);
						
						 row = sheet.getRow(rida);
						TreeMap groups = new TreeMap();
						if(row != null) {
						
						for (int a = alg; a < row.getLastCellNum(); a++) {
							try {
								
								System.out.println(row.getCell((short)a).getStringCellValue());
								
								String gg = row.getCell((short) a).getStringCellValue().trim();
								if(gg.indexOf("HM") != -1) {
									break;
								}
								if(gg.length()>1){
							      groups.put(a + "", gg);
							    }
							
							} catch (Exception e) {
								 System.out.println(row.getCell((short)a).getNumericCellValue());
							}
						}
						
						}
					//	System.out.println("Groups " +  groups.toString());
						
						
						for(int i = rida+1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
						
							
								String ean = formatter.formatCellValue(row.getCell(0));
								String tnimi = formatter.formatCellValue(row.getCell(1));
								String toode = getToode(prods,ean);
							 if(ean != null && ean.trim().length()> 0 ) {
								 
							
								
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n KEILA TÜ formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							 for (int a = alg; a < row.getLastCellNum(); a++) {

									String group = (String) groups.get(a + "");
									if (group != null && group.trim().length() > 0 ) {
							 
                            String pood = "KEI-" + group;
                            Contract con = new Contract(pood);
							 String value =  formatter.formatCellValue(row.getCell(a));
                          
                           
                           if(value.trim().length() > 0 && value.trim().toLowerCase().equals("x")) {
                           //	 System.out.println("Sees " +a + " " + value + " " + ean + " " + pood);
                           
							if(res.get(pood) != null){
               	                 con = (Contract)res.get(pood);
               	                } 
							  ContractLine ll = new ContractLine(toode);
               	               if(con.lines == null){
               		                 con.lines = new Vector();
               	                  }
               	              con.lines.add(ll);
               	              res.put(pood, con);
                           }
						   }
							 }
							 }
						}
					 }
					
					
				//System.out.println("Keilasorts size " + res.toString() );
				//	System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		 //System.exit(0);
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "keila", true);
		 }
		 sendFixLoadErrorEmail("\n Keila TÜ imported " + res.keySet().size() + " shops fixed sortiments", parent, "keila",false);
			 
	return res;
}
	
	 public static JSONArray getAssetsJson(String user , String pass, String company){
			JSONArray cc =  null;
			
			  eevaeksport.util.ReportingService serv = new ReportingService();
			  serv.user =  user;
				serv.pass = pass;
				serv.company = company;
				try {
				 cc = 	serv.getAssets(null);
				int lenn = cc.length();
				//System.out.println("WebCust suurus " + lenn);
	      	} catch (Exception e) {
					e.printStackTrace();
				}
				
			return cc;
		}

	 
	 
	 public static TreeMap loadFixLektus(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
			 DataFormatter formatter = new DataFormatter();
		
		 try{
			 Workbook workbook = null;
			 Sheet sheet = null;
			       FileInputStream	 ff = new FileInputStream(file);
			       if(file.getName().endsWith(".xlsx")) {
			    	   workbook = new XSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
			       }else {
			    	    workbook = new HSSFWorkbook(ff); 
						 sheet = workbook.getSheetAt(0); 
					
			       }
					
					
					System.out.println("Lektus lugemine " + file.getName());
					
					ff.close();	
					
					
					
					if(sheet != null){
						
						int rida = 1;
						int alg = 1;
						Row row = sheet.getRow(1);
						
						for(int i = 0 ; i < sheet.getLastRowNum()+1;i++){
							row = sheet.getRow(i);
							if(row != null) {
								for (int a = 0; a < row.getLastCellNum(); a++) {
									String ean = formatter.formatCellValue(row.getCell(a));
									if(ean.contains("Ribakood")) {
										rida = i;
								     }
								  }	
							}
						} 
						
						
						 if( formatter.formatCellValue(sheet.getRow(rida).getCell(2)).contains("Laogrupid")) {
							 

							 TreeMap sisu = new TreeMap();
							 TreeMap nimi = new TreeMap();
							int  reanr = 3;
							int sortasuk = 2;
							 // määrame andmete rea
							
							 
							// System.out.println("reanr " + reanr );
							//Teeme kõigepealt treemapi valmis kja siis krutime läbi 
								for(int i = rida ; i < sheet.getLastRowNum()+1;i++){
								 row = sheet.getRow(i);
									String ean = formatter.formatCellValue(row.getCell(0));
									String code = formatter.formatCellValue(row.getCell(sortasuk));
									String nimis = formatter.formatCellValue(row.getCell(sortasuk-1));
									if(ean != null && ean.trim().length() > 0) {
										sisu.put(ean.trim(), code);
										nimi.put(ean.trim(), nimis);
									
									} 
									
								}
							 
						
								
								Iterator itera  =  sisu.keySet().iterator();
								while(itera.hasNext()) {
								
								String ean = (String)itera.next();
							    String toode = getToode(prods,ean);
							    if(toode.equals(ean)) {
									  if(errors == null) {
										  errors = "Tuvastamatta tooted sortimendis\n RR-Lektus formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
									  }
									  if(!errors.contains(ean)) {
										  errors = errors + ean + "   " + nimi.get(ean) + "\n";
									  }
								  }
							    
							    //nüüd lööme koodi tykkideks ja lisame 
							    String sort = (String)sisu.get(ean);
							    ArrayList<String> sorts = new ArrayList<String>();
							    if(sort.indexOf("1") != -1) {
							    	sorts.add("1");
							      sort = sort.substring(sort.indexOf("1")+1);
							    }
							    
							    if(sort.indexOf("2") != -1) {
							    	sorts.add("2");
							      sort = sort.substring(sort.indexOf("2")+1);
							    }
							    
							    if(sort.indexOf("3") != -1) {
							    	sorts.add("3");
							      sort = sort.substring(sort.indexOf("3")+1);
							    }
							    
							    if(sort.indexOf("4") != -1) {
							    	sorts.add("4");
							      sort = sort.substring(sort.indexOf("4")+1);
							    }
							    if(sort.indexOf("5") != -1) {
							    	sorts.add("5");
							      sort = sort.substring(sort.indexOf("5")+1);
							    }
							    if(sort.indexOf("6") != -1) {
							    	sorts.add("6");
							      sort = sort.substring(sort.indexOf("6")+1);
							    }
							    if(sort.indexOf("7") != -1) {
							    	sorts.add("7");
							      sort = sort.substring(sort.indexOf("7")+1);
							    }
							    if(sort.indexOf("8") != -1) {
							    	sorts.add("8");
							      sort = sort.substring(sort.indexOf("8")+1);
							    }
							    
							    
							  //  System.out.println(sorts.toString());
							    for(String so : sorts) {
							    	
			                         String pood = "RR-" +so;
										
										Contract con = new Contract(pood);
										
										 System.out.println("Sees " + pood + " " + ean);

										  
										if(res.get(pood) != null){
			                        	  con = (Contract)res.get(pood);
			                        	 } 
										  ContractLine ll = new ContractLine(toode);
			                        	  if(con.lines == null){
			                        		  con.lines = new Vector();
			                        	  }
			                        	  con.lines.add(ll);
			                        	  res.put(pood, con);
							    }
								
								}
								
							  
						 }else {
							 
						
						
						
						for(int i = rida+1 ; i < sheet.getLastRowNum()+1;i++){
							 row = sheet.getRow(i);
						
							
								String ean = formatter.formatCellValue(row.getCell(0));
								String tnimi = formatter.formatCellValue(row.getCell(1));
								String toode = getToode(prods,ean);
							 if(ean != null && ean.trim().length()> 0 ) {
							  if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n RR Lektus formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }
							 
							

							 
                         String pood = "RR-yldsortiment";
                         
                         Contract con = new Contract(pood);
							 String value =  formatter.formatCellValue(row.getCell(0));
                       
                        
                        	 System.out.println("Sees "  + " " + value + " " + ean + " " + pood);
                        
							if(res.get(pood) != null){
            	                 con = (Contract)res.get(pood);
            	                } 
							  ContractLine ll = new ContractLine(toode);
            	               if(con.lines == null){
            		                 con.lines = new Vector();
            	                  }
            	              con.lines.add(ll);
            	              res.put(pood, con);
                        
						   }
							 }
							 }
					 }
					
					
				System.out.println("Lektussorts size " + res.toString() );
				
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		// System.exit(0);;
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "lektus", true);
		 }
		 sendFixLoadErrorEmail("\n  RR Lektus imported " + res.keySet().size() + " shops fixed sortiments", parent, "lektus",false);
			 
	return res;
}

	 
	 
	
	 public static TreeMap loadFixcoop(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					XSSFSheet sheet = workbook.getSheetAt(0); 
					DataFormatter formatter = new DataFormatter();
					
					System.out.println("coop lugemine " + file.getName());
					ff.close();	
					 if(sheet != null){
						 
						 int u = 4;
						 int eanp = 6;
						 int tnim = 8;
						 int seis = 0;
						 XSSFRow rowu = sheet.getRow(0);
						 String pais = formatter.formatCellValue(rowu.getCell(0));
						 if (pais.equals("Kauplus")) {
							 u = 1;
							 pais = formatter.formatCellValue(rowu.getCell(1));
							 if(pais.equals("EAN")) {
								 eanp = 1;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(2));
							 if(pais.equals("EAN")) {
								 eanp = 2;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(3));
							 if(pais.equals("EAN")) {
								 eanp = 3;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(4));
							 if(pais.equals("Nimetus")) {
								 tnim = 4;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(3));
							 if(pais.equals("Nimetus")) {
								 tnim = 3;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(2));
							 if(pais.equals("Nimetus")) {
								 tnim = 2;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(7));
							 if(pais.equals("Seisund")) {
								 seis = 7;
							 }
						 }
						 
						 
						 pais = formatter.formatCellValue(rowu.getCell(1));
						 if (pais.equals("Kauplus")) {
							 u = 1;
							 pais = formatter.formatCellValue(rowu.getCell(2));
							 if(pais.equals("EAN")) {
								 eanp = 2;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(3));
							 if(pais.equals("Nimetus")) {
								 tnim = 3;
							 }
							  seis = 0;
							 
						 }
						 
						 
						 
						 
						 rowu = sheet.getRow(6);
						  pais = formatter.formatCellValue(rowu.getCell(3));
						 if (pais.equals("Kauplus")) {
							 u = 6;
							 pais = formatter.formatCellValue(rowu.getCell(11));
							 if(pais.equals("EAN")) {
								 eanp = 11;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(13));
							 if(pais.equals("Nimetus")) {
								 tnim = 13;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(20));
							 if(pais.equals("Seisund")) {
								 seis = 20;
							 }
						 }
						 
						 rowu = sheet.getRow(3);
						  pais = formatter.formatCellValue(rowu.getCell(0));
						 if (pais.equals("Kauplus")) {
							 u = 3;
							 pais = formatter.formatCellValue(rowu.getCell(2));
							 if(pais.equals("EAN")) {
								 eanp = 2;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(4));
							 if(pais.equals("EAN")) {
								 eanp = 4;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(4));
							 if(pais.equals("Nimetus")) {
								 tnim = 4;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(6));
							 if(pais.equals("Nimetus")) {
								 tnim = 6;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(8));
							 if(pais.equals("Seisund")) {
								 seis = 8;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(11));
							 if(pais.equals("Seisund")) {
								 seis = 11;
							 }
						 }
						 
						 pais = formatter.formatCellValue(rowu.getCell(2));
						 if (pais.equals("Kauplus")) {
							 u = 3;
							 
							 pais = formatter.formatCellValue(rowu.getCell(8));
							 if(pais.equals("EAN")) {
								 eanp = 8;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(6));
							 if(pais.equals("EAN")) {
								 eanp = 6;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(15));
							 if(pais.equals("Seisund")) {
								 seis = 15;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(13));
							 if(pais.equals("Seisund")) {
								 seis = 13;
							 }
							 
							 pais = formatter.formatCellValue(rowu.getCell(10));
							 if(pais.equals("Nimetus")) {
								 tnim = 10;
							 }
							 pais = formatter.formatCellValue(rowu.getCell(8));
							 if(pais.equals("Nimetus")) {
								 tnim = 8;
							 }
						 }
							
						 
						 
						 
						 
				//	 System.out.println("u " + u +" " + tnim + " "+ seis + " "+ eanp );
					
					
						for(int i = u ; i < sheet.getLastRowNum()+1;i++){
							XSSFRow row = sheet.getRow(i);
							 int poo = 0;
                          if(u == 6) {
                        	  poo = 3;
                          }
                          if(seis == 15) {
                        	  poo = 2;
                          }
                          if(seis == 13) {
                        	  poo = 2;
                          }
                          if(seis == 100) {
                        	  poo = 1;
                          }
							 
							Cell nnn = row.getCell(poo);
							if(nnn != null) {
						
                            String pood = "COOP-" +nnn.getStringCellValue().trim();
							String ean = formatter.formatCellValue(row.getCell(eanp));
							String tnimi = formatter.formatCellValue(row.getCell(tnim));
							Boolean seisund = true;
							if(seis != 0 ) {
								String snimi = formatter.formatCellValue(row.getCell(seis));
								if(!snimi.equals("Sortimendis") && !snimi.equals("Toote hankimine")) {
									seisund = false;
								}
								
								//System.out.println("Sees " + i +" "+ seisund + " " + nnn + " " + snimi+ " "+ ean + " " + pood);
								
							}
							
							if(seisund) {
								
							
							Contract con = new Contract(pood);
							String toode = getToode(prods,ean);
							// if(ean.equals("4744136010477"))
							//System.out.println("Sees " + i +" "+ seisund + " " + nnn + " " + ean + " " + pood + " " + toode + " " + eanp);
							  if(toode.equals(ean) && !toode.equals("EAN")) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n Coop formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + tnimi + "\n";
								  }
							  }else {
								
							  
							if(res.get(pood) != null){
                           	  con = (Contract)res.get(pood);
                           	 } 
							  ContractLine ll = new ContractLine(toode);
                           	  if(con.lines == null){
                           		  con.lines = new Vector();
                           	  }
                           	  con.lines.add(ll);
                           	  res.put(pood, con);
						   }
							 // System.exit(0);
							}
							}
						}
					 }
					
					
					//System.out.println("sorts size " + res.toString() + " " + sheet.getLastRowNum());
					//System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		 
	//	System.exit(0);
		 
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "coop", true);
		 }
		 sendFixLoadErrorEmail("\n Coop imported " + res.keySet().size() + " shops fixed sortiments", parent, "coop",false);
			 
    	return res;
    }
	
	 
	 public static TreeMap loadFixOG(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
			String errors = null;
		 try{
			 
			       FileInputStream	 ff = new FileInputStream(file);
			  	 Workbook workbook = null; 
					Sheet sheet = null; 
			  if(file.getName().endsWith("xlsx")) {
			       workbook = new XSSFWorkbook(ff); 
					 sheet = workbook.getSheetAt(0); 
			  }else {
				  workbook = new HSSFWorkbook(ff); 
					 sheet = workbook.getSheetAt(0); 
			  }
			
					DataFormatter formatter = new DataFormatter();
					
					System.out.println("Elektra lugemine " + file.getName());
					ff.close();	
					 if(sheet != null){
						
						 TreeMap sisu = new TreeMap();

						 TreeMap nimi = new TreeMap();
						int  reanr = 4;
						int sortasuk = 3;
						 // määrame andmete rea
						 for(int i = 0 ; i < 10;i++){
							
						 Row row = sheet.getRow(1);
						 for(int y = 2 ; y < 15 ;y++) {
								Cell cellx = row.getCell((short)y);
								if(cellx != null) {
									try {
								//	System.out.println("Elektra " + cellx.getStringCellValue());
									if(formatter.formatCellValue(cellx).indexOf("A+") != -1  && reanr == 4) {
										reanr = i;
										sortasuk = y;
									}
                           }catch(Exception e) {
									//e.printStackTrace();
								}
								}	
							}
						 
						 }
						 
						// System.out.println("reanr " + reanr );
						//Teeme kõigepealt treemapi valmis kja siis krutime läbi 
							for(int i = reanr ; i < sheet.getLastRowNum()+1;i++){
							Row row = sheet.getRow(i);
								String ean = formatter.formatCellValue(row.getCell(0));
								String code = formatter.formatCellValue(row.getCell(sortasuk));
								String nimis = formatter.formatCellValue(row.getCell(sortasuk-1));
								if(ean != null && ean.trim().length() > 0) {
									sisu.put(ean.trim(), code);
									nimi.put(ean.trim(), nimis);
								
								} 
								
							}
						 
						 
						
						 
						Iterator itera  =  sisu.keySet().iterator();
						while (itera.hasNext()) {
							String ean = (String)itera.next();
						    String toode = getToode(prods,ean);
						    if(toode.equals(ean)) {
								  if(errors == null) {
									  errors = "Tuvastamatta tooted sortimendis\n OG-Elektra formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
								  }
								  if(!errors.contains(ean)) {
									  errors = errors + ean + "   " + nimi.get(ean) + "\n";
								  }
							  }
						    
						    //nüüd lööme koodi tykkideks ja lisame 
						    String sort = (String)sisu.get(ean);
						    ArrayList<String> sorts = new ArrayList<String>();
						    if(sort.indexOf("A+") != -1) {
						    	sorts.add("A+");
						      sort = sort.substring(sort.indexOf("A+")+2);
						    }
						    
						    if(sort.indexOf("A") != -1) {
						    	sorts.add("A");
						      sort = sort.substring(sort.indexOf("A")+1);
						    }
						    
						    if(sort.indexOf("B") != -1) {
						    	sorts.add("B");
						      sort = sort.substring(sort.indexOf("B")+1);
						    }
						    
						    if(sort.indexOf("C") != -1) {
						    	sorts.add("C");
						      sort = sort.substring(sort.indexOf("C")+1);
						    }
						    
						  //  System.out.println(sorts.toString());
						    for(String so : sorts) {
						    	
		                         String pood = "OG-" +so;
									
									Contract con = new Contract(pood);
									
									 // System.out.println("Sees " + i + " " + nnn + " " + ean);

									  
									if(res.get(pood) != null){
		                        	  con = (Contract)res.get(pood);
		                        	 } 
									  ContractLine ll = new ContractLine(toode);
		                        	  if(con.lines == null){
		                        		  con.lines = new Vector();
		                        	  }
		                        	  con.lines.add(ll);
		                        	  res.put(pood, con);
						    }
						    
						  }
						 
						
						
						
					 }
					
					
					//System.out.println("sorts size " + res.toString() + " " + sheet.getLastRowNum());
				//	System.exit(0);
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
		 
		 if(errors != null) {
			 sendFixLoadErrorEmail(errors, parent, "elektra", true);
		 }
		 sendFixLoadErrorEmail("\n Elektra imported " + res.keySet().size() + " shops fixed sortiments", parent, "elektra",false);
			 
 	return res;
 }
	
	 
	 
	
	public static TreeMap loadFixFromExcels(String dirx , TreeMap prods, EksportPanel parent) {
		TreeMap res = new TreeMap();
		
	//	Kammime kataloogi läbi ja määrame mis on mis ja proovime sisse lugeda 
		String done = "";
		
		 File dir = new File(dirx);
		 File[] files = dir.listFiles();
		   
		    
		    for (int i = 0; i < files.length ; i++){
		      File file = files[i];
		     
		      if (file.isFile() && !file.getName().contains("Thumbs")) {
					
		        
		          String format = identifyXLS(file);
		          System.out.println("Format tuvastatud " + format + " " + file.getName());
		        //System.exit(0);
		        
		        if(format.equals("mjsortiment")) {
		        	loadFixMJ(res, file , prods, parent);
		          }
		          if(format.equals("coop")) {
		        	  loadFixcoop(res, file , prods, parent);
		          }
		          
		          if(format.equals("cooprist")) {
		        	  loadFixcoopRist(res, file , prods, parent);
		          }
		          
		          if(format.equals("elektra")) {
		        	  loadFixOG(res, file , prods, parent);
		          }
		         
		          if(format.equals("lektus")) {
		        	  loadFixLektus(res, file , prods, parent);
		          }
		          
		          if(format.equals("kaupmees")) {
		        	  loadFixKaup(res, file , prods, parent);
		          }
		          if(format.equals("kaupmees2")) {
		        	  loadFixKaup2(res, file , prods, parent);
		          }
		          
		          if(format.equals("selver")) {
		        	  loadFixSelver(res, file , prods, parent);
		          }
		          if(format.equals("circlek")) {
		        	  loadFixCirclek(res, file , prods, parent);
		          }
		          if(format.equals("keila")) {
		        	  loadFixKeila(res, file , prods, parent);
		          }
		          
		          if(format.equals("aldar")) {
		        	  loadFixAldar(res, file , prods, parent);
		          }
		          
		          if(format.equals("maxima")) {
		        	 loadFixMaxima(res, file , prods, parent);
			      }

		          if(format.equals("rimi")) {
		        	 loadFixRimi(res, file , prods, parent);
			      }
		          
		          if(format.equals("rimi2")) {
		        	 loadFixRimi2(res, file , prods, parent);
			      }
		          
		          if(format.equals("elvi")) {
		        	 loadFixElvi(res, file , prods, parent);
			      }
		          
		          
		          if(format.equals("prisma")) {
		        	 loadFixPrisma(res, file , prods, parent);
			      }
		          if(format.equals("prisma2")) {
			        	 loadFixPrisma2(res, file , prods, parent);
				      }
		       }
		      String nn = file.getName();
		    		  
		    if(!file.isDirectory()) {
		    	File ii = new File(dirx + "/done/" + Converter.getYYYYmMMmDDmString(Calendar.getInstance().getTime()) + " " + nn);
		    	if(ii.exists()) {
		    		ii.delete();
		    	}
		    	System.out.println("renamed " +   file.renameTo(new File(dirx + "/done/" + Converter.getYYYYmMMmDDmString(Calendar.getInstance().getTime()) + " " + nn)));
		      
		    }
		    	
		    }
		
	 
		
		return res;
	}
	
	
	
	
	
	private static String identifyXLS(File f){
		  String result = null;
		  DataFormatter formatter = new DataFormatter();
			int a = 0;
			FileInputStream ff  = null;
		   
			System.out.println("Formaadi tuvastus " + f.getName());
			if(!f.getName().contains("Thumbs")) {
			
			try{
				
				
				 ff = new FileInputStream(f);
				 
				 Workbook workbook = null; 
					Sheet sheet = null; 
			  if(f.getName().endsWith("xlsx")) {
			       workbook = new XSSFWorkbook(ff); 
					 sheet = workbook.getSheetAt(0); 
			  }else {
				  workbook = new HSSFWorkbook(ff); 
					 sheet = workbook.getSheetAt(0); 
			  }
					
			  
			  if(sheet.getSheetName().equals("mjsortiment")) {
				  return "mjsortiment";
			  }
					ff.close();	
					if(sheet != null){
						for(int i = 0 ; i < sheet.getLastRowNum();i++){
							Row row = sheet.getRow(i);
							//System.out.println(row);
							if(row == null && a == 0){
								a = i;
							}else{
								a = sheet.getLastRowNum();
							}
						}
						
						for(int i = 0 ; i < a;i++){
							
							
							Row row = sheet.getRow(i);	
							if(row != null) {
								
								
								Cell cellx = row.getCell((short)0);
								try {
									if(cellx != null) {
									//	System.out.println("xx " +formatter.formatCellValue(cellx));
										if(formatter.formatCellValue(cellx).indexOf("Maksimarket") != -1 ) {
											return "coop";
										}
										if(formatter.formatCellValue(cellx).indexOf("SAP") != -1 ) {
											return "maxima";
										}	
											
									}
								}catch(Exception k) {
									k.printStackTrace();
								}

								cellx = row.getCell((short)1);
								try {
									if(cellx != null) {
										if(formatter.formatCellValue(cellx).indexOf("Maksimarket") != -1 ) {
											return "coop";
										}	
									}
								}catch(Exception k) {
									k.printStackTrace();
								}
								
								cellx = row.getCell((short)3);
								if(cellx != null) {
									try {
										if(formatter.formatCellValue(cellx).indexOf("Maksimarket") != -1 ) {
											return "coop";
										}
										if(formatter.formatCellValue(cellx).indexOf("CircleK") != -1 ) {
											return "circlek";
										}
									}catch(Exception k) {
										k.printStackTrace();
									}
									
								}
								
								for(int y = 4 ; y < 10 ;y++) {
									
									cellx = row.getCell((short)y);
									if(cellx != null) {
										
										try {
											if(formatter.formatCellValue(cellx).indexOf("Abja") != -1 ) {
												return "cooprist";
											}
										}catch(Exception k) {
											k.printStackTrace();
										}
										
									}	
								}
								
									
									 if(row != null) {
									 cellx = row.getCell((short)0);
										if(cellx != null) {
											try {
											String a1 = formatter.formatCellValue(cellx);
										cellx = row.getCell((short)1);
										String a2 = formatter.formatCellValue(cellx);
										cellx = row.getCell((short)2);
										String a3 = formatter.formatCellValue(cellx);
										
											
									
												if(a1 != null && a1.equals("Ribakood") && a2 != null && a2.equals("Nimetus")
														&&  a3 != null && (a3.equals("Kaubagrupp") || a3.equals("Laogrupid"))  ) {
													return "lektus";
												}
											}catch(Exception k) {
												k.printStackTrace();
											}
											
										}	 
								 }
									
									
								
								
	                      for(int y = 3 ; y < 12 ;y++) {
									
									cellx = row.getCell((short)y);
									if(cellx != null) {
										
										try {
											if(formatter.formatCellValue(cellx).indexOf("1ka") != -1 ) {
												return "kaupmees";
											}
											
											if(formatter.formatCellValue(cellx).equals("Kad") ) {
												return "kaupmees2";
											}
										}catch(Exception k) {
											k.printStackTrace();
										}
										
									}	
								}
								

								if(i < 16) {
									//kui on kolmas rida 
									for(int y = 3 ; y < 40 ;y++) {
										
										cellx = row.getCell((short)y);
										if(cellx != null) {
											try {
											//System.out.println("selver " + formatter.formatCellValue(cellx) + " " + i + " " + y + " " + a);
											if(formatter.formatCellValue(cellx).indexOf("Kadaka Selver") != -1 ) {
												return "selver";
											}	
										}catch(Exception k) {
											k.printStackTrace();
										}
										}	
									}
								}
								
								
								if(i == 0 || i == 3) {
									//kui on kolmas rida 
									for(int y = 2 ; y < 40 ;y++) {
										cellx = row.getCell((short)y);
										if(cellx != null) {
											try {
										//	System.out.println("selver " + cellx.getStringCellValue());
											if(formatter.formatCellValue(cellx).indexOf("Sikupilli") != -1 ) {
												return "prisma";
											}
											if(formatter.formatCellValue(cellx).indexOf("SiRo") != -1 ) {
												return "prisma2";
											}
										}catch(Exception k) {
											k.printStackTrace();
										}
										}	
									}
								}
								
												
										//if(i == 5) {
											//kui on kolmas rida 
											for(int y = 2 ; y < 15 ;y++) {
											 cellx = row.getCell((short)y);
												if(cellx != null) {
													try {
										//			System.out.println("Keila " + cellx.getStringCellValue());
													if(formatter.formatCellValue(cellx).indexOf("Jaama") != -1 || formatter.formatCellValue(cellx).indexOf("Turba") != -1) {
														return "keila";
													}
		                                         } catch(Exception e) {
													
												 }
											   }	
											}
										//}
									
								
								
								if(i == 0 || i == 1 || i == 2 || i == 3 || i == 4) {
									for(int y = 4 ; y < 15 ;y++) {
									
									cellx = row.getCell((short)y);
									if(cellx != null) {
									//	System.out.println("RIMI " + y + " " + i + " " + cellx.getStringCellValue());
										try {
											if(formatter.formatCellValue(cellx).indexOf("ER47") != -1 || formatter.formatCellValue(cellx).indexOf("ER11") != -1  ) { // || formatter.formatCellValue(cellx).indexOf("") != -1 
												return "rimi";
											}		
										}catch(Exception e) {
											
										}
										
										try {
											if(formatter.formatCellValue(cellx).indexOf("EEHM") != -1 || formatter.formatCellValue(cellx).indexOf("EESM") != -1  ) { // || formatter.formatCellValue(cellx).indexOf("") != -1 
												return "rimi2";
											}		
										}catch(Exception e) {
											
										}
										
									}	
								
									}
								}
								
								if(i == 3) {
									//kui on kolmas rida 
									for(int y = 3 ; y < 15 ;y++) {
										cellx = row.getCell((short)y);
										if(cellx != null) {
											try {
										//	System.out.println("Aldar " + cellx.getStringCellValue());
											if(formatter.formatCellValue(cellx).indexOf("30.Roela") != -1 ) {
												return "aldar";
											}
                                      }catch(Exception e) {
											e.printStackTrace();
										}
										}	
									}
									
									
								}
								
								
								for(int y = 2 ; y < 15 ;y++) {
									cellx = row.getCell((short)y);
									if(cellx != null) {
										try {
									//	System.out.println("Elektra " + cellx.getStringCellValue());
										if(formatter.formatCellValue(cellx).indexOf("A+") != -1 ) {
											return "elektra";
										}
                                  }catch(Exception e) {
										//e.printStackTrace();
									}
									}	
								}
							}
						}	
					}
				
					
			}catch(Exception aa){
				aa.printStackTrace();
			//	return "";
			}
			
			}
		
		  
		  
		  
		System.out.println("result " + result);
	
		  return result ;
	  }
	  

	
	
	public static String readFile(File f , String encoding, ArrayList arr) throws IOException {
		FileInputStream is = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(is, encoding);
		BufferedReader reader = new BufferedReader(isr);
		
		
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	        	if(arr != null && line.trim().length() > 0) {
	        		arr.add(line);
	        	}
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
	
	
	public static String decode(byte[] inputString, String encoding) throws CharacterCodingException{
		Charset charset = Charset.forName(encoding);
	    CharsetDecoder decoder = charset.newDecoder();	
    	CharBuffer cb = decoder.decode(ByteBuffer.wrap(inputString));
        String s = cb.toString();
        return s;
	}
	
	
	public static void sendReportEmail(String comment1 , String fileName, EksportPanel parent ){
	   	try{
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment =  comment1 + "\n\n" ;
	   		}
	   		
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	               
	              
	                	mail.setSubjectFilesTo("Merisoft Report " );
		                if(parent.getProps().getProperty("report.subject") != null)
		                	mail.setSubjectFilesTo(parent.getProps().getProperty("report.subject"));
		                
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		
		  	  
		  	  	
		  	  		String lisa = "";
		  	  		
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBodyHtml("<br>" + lisa + comment.replaceAll("\n", "<br>") + "<br><br>");
		  	  		mail.setSendNotice(false);
		  	  		mail.addAttach(fileName);
		  	  	 StringTokenizer st = new StringTokenizer(parent.getProps().getProperty("report.email"),",");
		  	  	 while(st.hasMoreTokens()) {
		  	  	    mail.setFilesTo(st.nextToken());
		  	  		mail.send(); 
		  	  	 }
	               	
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
	
	public static void sendFixLoadErrorEmail(String comment1 , EksportPanel parent , String format , boolean error){
	   	try{
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment =  comment1 + "\n\n" ;
	   		}
	   		
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	               
	                if(error) {
	                	mail.setSubjectFilesTo("Fixed sortiment loading error report : " + format );
	                } else {
	                	mail.setSubjectFilesTo("Fixed sortiment upload : " + format );
	                }
		  	  		
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		
		  	  
		  	  	
		  	  		String lisa = "";
		  	  		
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBodyHtml("<br>" + lisa + comment.replaceAll("\n", "<br>") + "<br><br>");
		  	  		mail.setSendNotice(false);
		  	  		
		  	  	 StringTokenizer st = new StringTokenizer(parent.getProps().getProperty("fixsort.erroremail"),",");
		  	  	 while(st.hasMoreTokens()) {
		  	  	    mail.setFilesTo(st.nextToken());
		  	  		mail.send(); 
		  	  	 }
	               	
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
	
	
	
	public static void sendCommentEmail(String comment1 ,String comment2,String doknr,String custid, String nimi , String chain, boolean price, Agent ag ,ImportPanel parent){
	   	try{
	   		String comment = null ;
	   		if(comment1 != null && comment1.trim().length() > 0) {
	   			comment = "Comment : " + comment1 + "\n\n" ;
	   		}
	   		
	   		if(comment2 != null && comment2.trim().length() > 0) {
	   			if(comment == null) {
	   				comment = "";
	   			}
	   			
	   			comment = comment + "Price info : " + comment2 + "\n" ;
	   		}
	   		
	   		if(comment != null && comment.trim().length() >0){
	   			
	   			//System.out.println(comment);
	   			Email mail = new Email(null);
		  		   
	   			
	                mail.setSMTP(parent.getProps().getProperty("smtphost"));
	                if(parent.getProps().getProperty("smtpuser") != null)
	                mail.setUser(parent.getProps().getProperty("smtpuser"));
	                
	                if(parent.getProps().getProperty("smtppass") != null)
	                mail.setPass(parent.getProps().getProperty("smtppass"));
	                
	                if(parent.getProps().getProperty("smtpport") != null)
	                mail.setPort(parent.getProps().getProperty("smtpport"));
	                
		  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
		  	  		mail.setFrom(parent.getProps().getProperty("from"));
		  	  		String agent = "";
		  	  	if(ag != null) {
		  	  		agent = ag.getName() ;
		  	  	}
		  	  		mail.setSubjectFilesTo("Order info : "+ agent + " " + custid + " " + nimi);
		  	  		String lisa = "";
		  	  		
		  	  	String cc = "";
	  	  		if(doknr != null && doknr.trim().length() > 0) {
	  	  		    cc = "<br>Order : " +  doknr ;
	  	  		}
		  	  		mail.setEncoding("utf-8");
		  	  		mail.setBodyHtml(cc + "<br>Customer code : " + custid +  "<br>Customer Name : " + nimi + "<br>" +
		  	  			    "Manager : " + agent + "<br><br>" + lisa + comment.replaceAll("\n", "<br>") + "<br><br>");
		  	  		//mail.setBody("\nOrder :" +  doknr + " \nCustomer code :" + custid +  "\nCustomer Name :" + nimi + "\n" +
		  	  			//    "Manager :" + agent + "\n" +  "\n" + lisa + comment + "\n\n.");
		  	  		mail.setSendNotice(false);
		  	  		
		  	  		mail.send();
		  	  		System.out.println("\nOrder Comment " +  doknr + " " + lisa + custid + " "+ nimi + " " + agent + "\n\n" +
		  	  				  "\n >>>>>" + comment + "<<<<<");
		  				
	   		}
		  	
		  			}catch(Exception e){
		  			e.printStackTrace();	
		  			}
		  	
	   }
	
	 public static boolean isSameDay(Calendar cal1, Calendar cal2) {
	        if (cal1 == null || cal2 == null) {
	            throw new IllegalArgumentException("The dates must not be null");
	        }
	        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
	                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	    }
	 
    private static String getVal(XSSFRow row,int u ){
 	   String asi = null;
 	   if(row.getCell(u) != null ){
 		   try {
					asi = (int) (row.getCell((short) u).getNumericCellValue()) + "";
				} catch (Exception e) {
					asi = (row.getCell((short) u).getStringCellValue());
				}
 		 }
 	  return asi ; 
    }
	
    public static String turn(String aa) {
 	   String res = "";
 	   res = aa.substring(8, 10) + "-" + aa.substring(5, 7) + "-" + aa.substring(0, 4);
 	   return res;
   }
    
    private static String getToode(TreeMap prods, String ean ) {
    	String res = ean;
    	
    	if(prods != null && prods.get(ean) != null) {
    		return (String)prods.get(ean);
    	}
    	
    	if(prods != null && ean.startsWith("23")) {
    		Iterator itera = prods.keySet().iterator();
    		String uu = ean.substring(2,8);
    		if(prods != null && prods.get(uu) != null) {
        		return (String)prods.get(uu);
        	}
    	}
    	
    	if(prods != null && ean.startsWith("600")) {
    		Iterator itera = prods.keySet().iterator();
    		String uu = ean.substring(3,9);
    		if(prods != null && prods.get(uu) != null) {
        		return (String)prods.get(uu);
        	}
    	}
    	
    	return res;
    }
    
    public static void convertCoopSortimenttoMobitrekFormat(TreeMap prods) {
    	

		 try{
		
			 File file = new File("coop/coopsortiment.xlsx");
			       FileInputStream	 ff = new FileInputStream(file);
			
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					TreeMap sorts = new TreeMap();
					TreeMap tooted = new TreeMap();
					XSSFSheet sheet = workbook.getSheetAt(0); 
					
					ff.close();	
					if(sheet != null){
						for(int i = 1 ; i < sheet.getLastRowNum()+1;i++){
							XSSFRow row = sheet.getRow(i);
							
							DataFormatter formatter = new DataFormatter();
 
							String pood = row.getCell(0).getStringCellValue();
							String ean = formatter.formatCellValue(row.getCell(2));
							String nimi = formatter.formatCellValue(row.getCell(4));
							
							ArrayList arr = new ArrayList();
							
							if(sorts.get(pood) != null) {
								arr = (ArrayList)sorts.get(pood);
							}
							arr.add(getToode(prods,ean));
							sorts.put(pood, arr);
							tooted.put(getToode(prods,ean), nimi);
						}
					 }
					
					
					//System.out.println("sorts size " + sorts.size() + " " + sheet.getLastRowNum());
					
					
					  FileInputStream fIPS= new FileInputStream("coop/contractbase.xlsx"); //Read the spreadsheet that needs to be updated
				        XSSFWorkbook  wb = new XSSFWorkbook(fIPS);
				        XSSFSheet worksheet = wb.getSheetAt(0);
				        
				        // siis aiis väljade täitmine teha.
				        //Paneme tooted ylebvalt alla 
				        Iterator itera  = tooted.keySet().iterator();
				        int i = 0;
				       while (itera.hasNext()) {
				    	   String kood = (String) itera.next();
				    	   Row row1 = worksheet.getRow(i+1); 
				    	   if(row1 == null) {
				    		   row1 = worksheet.createRow(i+1); //0 = row number
				    	   }
							Cell c = row1.createCell(0);
							c.setCellValue(kood);
							c = row1.createCell(1);
							c.setCellValue((String)tooted.get(kood));
							i++;
				       }
						
				      
				       
				       //paneme poed paika 
				       
				        itera  = sorts.keySet().iterator();
				         i = 0;
				       while (itera.hasNext()) {
				    	   String kood = (String) itera.next();
				    	   Row row1 = worksheet.getRow(0); 
				    	 	Cell c = row1.createCell(i+2);
							c.setCellValue(kood);
							ArrayList arr = (ArrayList)sorts.get(kood);
							
							for(int e = 1 ; e < worksheet.getLastRowNum()+1;e++){
								
								Row row2 = worksheet.getRow(e); 
								Cell cc = row2.getCell(0);
								String toode = cc.getStringCellValue();
								if(arr.contains(toode)) {
									Cell c2 = row2.createCell(i+2);
									c2.setCellValue(1);
								}
								
							}
							
							i++;
				       }
				    	   
				       
				       
				        
					    fIPS.close(); //Close the InputStream
				        FileOutputStream output_file =new FileOutputStream("coop/mobitrekcontract.xlsx");
				        wb.write(output_file); //write changes
				        output_file.close();  //close the stream
					
						
					 }catch(Exception e) {
						 e.printStackTrace();
					 }	 
			 
    	
    }

	 public static void sendExcelVisits(File file, String rcompany , String user , String pass ){
		 
		 
		
		 
		 try{
			 
				
			  FileInputStream	 ff = new FileInputStream(file);
			
					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
					//Siia teeme tsykli et käiakõik lehed läbi 
					int lehti = workbook.getNumberOfSheets();
					
					System.out.println("lehti " + lehti);
					
				for(int q = 0;q<lehti;q++){
					 int a = 0;
					XSSFSheet sheet = workbook.getSheetAt(q); 
					ff.close();	
					if(sheet != null){
						for(int i = 0 ; i < sheet.getLastRowNum();i++){
							XSSFRow row = sheet.getRow(i);
							//System.out.println(row);
							if(row == null && a == 0){
								a = i;
							}else{
								a = sheet.getLastRowNum();
							}
						}
						
			 
			 
			 
	Vector visits = new Vector();
	TreeMap asjad = new TreeMap();
	//Paneme agentide lõikes ja siis seal sees kliendi lõiles 
	
	XSSFRow row = sheet.getRow(0);
	String agg = null;
	try {
		agg = (int) (row.getCell((short) 1)
				.getNumericCellValue()) + "";
	} catch (Exception e) {
		try{
			agg = (row.getCell((short) 1).getStringCellValue());
		}catch(Exception eh){
		}
	}
	
	
	
	for(int i = 2 ; i < a;i++){
		 row = sheet.getRow(i);
		String cust = null;
		try {
			cust = (row.getCell((short) 2).getStringCellValue());
	    	} catch (Exception e) {
			try{cust = (int) (row.getCell((short) 2)
					.getNumericCellValue()) + "";
				}catch(Exception eh){
					System.out.println("exc " + i );
					eh.printStackTrace();
			}
		}
		
	//  nyyd teeme rea läbi ja kui on X siis loeme sisse
		if(cust != null && !cust.equals("null")){
		
		for (int y = 3; y < 23 ;y++){
		String d = getVal(row,y);
		if(d != null){
			d = d.trim();
		}
	//	System.out.println("agent " + agg + " " + a + " " + cust + " " + d  + " " + y);

		String day = "";
		String type = "V";
		if(d != null && (d.toLowerCase().equals("x") || d.toLowerCase().equals("*") ||  d.toLowerCase().equals("1"))){
			if(y==3 || y==8 || y==13 || y==18){
			 	day = "Mon";
			}	
			if(y==4 || y==9 || y==14 || y==19){
			 	day = "Tue";
			}		
			if(y==5 || y==10 || y==15 || y==20){
			 	day = "Wed";
			}		
			if(y==6 || y==11 || y==16 || y==21){
			 	day = "Thu";
			}		
			if(y==7 || y==12 || y==17 || y==22){
			 	day = "Fri";
			}	
			if(d.equals("1")){
				type = "C";
			}
		}
	
	String weekno = "1";
  if(y > 7 && y < 13){
	  weekno = "2";
  }
  if(y > 12 && y < 18){
	  weekno = "3";
  }

  if(y > 17 && y < 23){
	  weekno = "4";
  }
  
 //System.out.println("Sees " +  cust +" " + y + " '" + day + "'" + weekno);
	
	
	Agent ddd = new Agent(agg);
	ddd.setDays(day);
	ddd.setCode(type);
	if(day.trim().length() > 0){
	
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
	
	}}
		
		}
	}

	//meil on treemap  agent id ja seal treemap custid seal treemap nädala numbritega ja vector sees

System.out.println(asjad.toString());
	Iterator itera = asjad.keySet().iterator();
	while (itera.hasNext()){
	 agg = (String)itera.next();

	TreeMap custs = (TreeMap)asjad.get(agg);

	Iterator iterax = custs.keySet().iterator();
	while (iterax.hasNext()){
	String cust = (String)iterax.next();
	
	TreeMap wns = (TreeMap)custs.get(cust);
	StringBuffer weekObj = new StringBuffer();
	//System.out.println("agent " + agg + " " + " " + cust + " " + wns.toString());

	weekObj.append( "{\"json\": {\"customer\": \"" + cust.trim() + "\",\"weeks\": [ ");
	String com = ",";
	for(int y = 0; y < 4; y++){
	weekObj.append("{");


	Iterator iteray = wns.keySet().iterator();
	 while (iteray.hasNext()){
		
		  String weekno = ((String)iteray.next());
		  int  weekn = Integer.parseInt(weekno);
		  Vector ags = (Vector)wns.get(weekno);
	    if(weekn == y+1){
	   	 //siia vectoris läbi ja kylgejama 
	   	 for(int u = 0; u < ags.size(); u++){
	   		Agent aa = (Agent)ags.elementAt(u);
	   		String coma = "";
	   		if(u > 0){
	   			coma = ",";
	   		}
	   		System.out.println(weekn + " " + aa.getCode() + " " + aa.getDays());
	   		if(aa.getCode().equals("V")){
	   				
	   			if(weekObj.toString().endsWith("{")){
	   				  weekObj.append("\""+ (aa.getDays()) + "\": \"Visit\"");	
	  	  	   		}else{
	   				  weekObj.append(coma + "\""+ (aa.getDays()) + "\": \"Visit\"");	
	  	  	   		}
	   			
	   		}else{
	   				if(weekObj.toString().endsWith("{")){
	   				  weekObj.append( "\""+ (aa.getDays()) + "\": \"Call\"");	
	   	   		   }else{
	   				  weekObj.append(coma + "\""+ (aa.getDays()) + "\": \"Call\"");	
	   	   		   }
	   		}
	   	 }
	    }

	 

	 }
		if(y==3){
			com = "";
		}
    	weekObj.append("}"+ com);
	}

	weekObj.append("],\"manager\": \"" + agg + "\" }}");
	System.out.println("agent " + agg + " " + a + " " + cust + " " + weekObj.toString());

	try{
		visits.add( new JSONObject(weekObj.toString()));	
	}catch(Exception e){
		System.out.println(weekObj.toString());
		e.printStackTrace();
		System.exit(0);
	}
	}
	}


	System.out.println("visits " + visits.size()
	+" \n"+ visits.elementAt(0) 
	+ "\n" +visits.elementAt(1)
	+ "\n" +visits.elementAt(2));

	if(rcompany != null){
	Converter.sendRouteCalsJson(visits,user,pass,rcompany);
	}


	}

					
					
				}
					
	 } catch (Exception e) {
	   e.printStackTrace();
	 }
	 


		 
		 
	 }
	 

	 
	 
	public static void sendContractJson(TreeMap tm , String company, String user , String pass){
		Vector<MyJSON> json = new Vector<MyJSON> ();
		
		  eevaeksport.util.ReportingService serv = new ReportingService();
		  serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			
	  if(company == null){
		  System.out.println("Add companycode to export.ini for  Product Resting");
		  return;
	  }
		MyJSON myson = new MyJSON();
		myson.setUrl("http://merisoft.eu:8080/rest/contract/list/");
	  JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		int cou = 0;
		
		while (itera.hasNext()){
			Contract ag = (Contract)itera.next();
			JSONObject value = new JSONObject();
try{
	value.put("code", ag.code);
	value.put("name", ag.name);
	value.put("contractType", "FIXED");
	
	JSONArray arr = new JSONArray();
	JSONObject level = new JSONObject();
	level.put("code", ag.code);
	arr.put(level);
	value.put("levels", arr);
   
	Calendar cal = Calendar.getInstance();
	value.put("starts", cal.getTimeInMillis());
	cal.add(Calendar.YEAR, 3);
	value.put("ends", cal.getTimeInMillis());
	
	if(ag.customers != null  && ag.customers.size() > 0){
	   JSONArray arrayx = new JSONArray();
	   for(int i = 0; i < ag.customers.size();i++){
		   JSONObject valuex = new JSONObject();
			valuex.put("code", ag.customers.elementAt(i));
			arrayx.put(valuex);  
	   }
		value.put("customers", arrayx);
	}
	
	if(ag.lines != null  && ag.lines.size() > 0){
		   JSONArray arrayx = new JSONArray();
		   for(int i = 0; i < ag.lines.size();i++){
			   JSONObject valuex = new JSONObject();
			   ContractLine lin = (ContractLine)ag.lines.elementAt(i);
			   valuex.put("rownum", i );
			   JSONObject valuep = new JSONObject();
			   valuep.put("code", lin.prodid );
		       valuex.put("product",valuep );
			   arrayx.put(valuex);  
		   }
			value.put("contractLines", arrayx);
		}
		
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
	
	if(cou == 1){
		myson.setJson(deCode(array.toString()));
		json.add(myson);
		System.out.println("MYJSON Contracts 1 " );
		if(json.size() > 0){
			try {
				String aaa = serv.postJson(array.toString(), myson.getUrl());
				System.out.println(aaa);
				Converter.saveFile("json.txt", array.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		json = new Vector<MyJSON> ();
		array = new JSONArray();
		cou = 0;
	}
	   cou ++;
		}
   
		

		myson.setJson(deCode(array.toString()));
		json.add(myson);
		System.out.println("MYJSON Contract ");
		
		if(json.size() > 0){
			try {
				String aaa = serv.postJson(array.toString(), myson.getUrl());
				System.out.println(aaa);

				System.out.println("user " + user + " " + pass + " "  + company + " " + 
						myson.getUrl());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		serv.closeClient();
	}
	

	
	 
	 
	public static void sendProductJson(TreeMap tm , String company){
		Vector<MyJSON> json = new Vector<MyJSON> ();
		
	  if(company == null){
		  System.out.println("Add companycode to export.ini for  Product Resting");
		  return;
	  }
		MyJSON myson = new MyJSON();
		myson.setUrl("http://merisoft.ee:8080/rest/product/list/");
	  JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		int cou = 0;
		
		while (itera.hasNext()){
			Product ag = (Product)itera.next();
			JSONObject value = new JSONObject();
try{
	value.put("code", ag.getID());
	if(ag.restName != null){
		value.put("name", ag.restName);
	}else{
		value.put("name", ag.getName());
	}
	value.put("company", company);
	 JSONArray arrayx = new JSONArray();
	if(ag.getGroup()!= null  && ag.getGroup().trim().length() > 0){
	  JSONObject valuex = new JSONObject();
		valuex.put("code", ag.getGroup());
		valuex.put("name", ag.getGroupname());
		//System.out.println("grname " + ag.getGroupname());
		valuex.put("type", "group");
		valuex.put("company", company);
		arrayx.put(valuex);
	}
	
	if(ag.getGroupSecond()!= null && ag.getGroupSecond().trim().length() > 0){
		  JSONObject valuex = new JSONObject();
			valuex.put("code", ag.getGroupSecond());
			valuex.put("name", ag.getGroupSecname());
			valuex.put("type", "group2");
			valuex.put("company", company);
			arrayx.put(valuex);
		}
	
	
	if(ag.getType()!= null && ag.getType().trim().length() > 0){
		  
			JSONObject valuex = new JSONObject();
			valuex.put("code", ag.getType());
			valuex.put("name", ag.getType());
			valuex.put("type", "type");
			valuex.put("company", company);
			arrayx.put(valuex);
		}
	
	if(ag.getType2()!= null && ag.getType2().trim().length() > 0){
			JSONObject valuex = new JSONObject();
			valuex.put("code", ag.getType2());
			valuex.put("name", ag.getType2());
			valuex.put("type", "category");
			valuex.put("company", company);
			arrayx.put(valuex);
		}
	
	value.put("group", arrayx);
	
	/*{"id":3229776,"created":1431460107251,"modified":1432152348816,"code":"sss","name":"New Product",
	 * "unit":"tk.","description":"terekest see on Ã¤ge toode mis meil siis olemas on","EAN":"435535353535",
	 * "blocked":true,"diffColor":true,"statusDiscount":true,"statusSellout":true,"statusNew":true,
	 * "statusTOP":true,"inCases":123.00,"price":134.34,"stock":1000.00,
	 * "measure":11111.00,"maxDiscount":20.00,"onPallet":132.00,"optAmountInUnits":3.00,"company":"kalevmerc"}*/
	
	if(ag.getStatusNew() != null && ag.getStatusNew().equals("1")){
		value.put("statusNew", "true");
	}
	
	if(ag.sortRest != null){
		value.put("sorting", ag.sortRest);
	}
	
	if(ag.getStatusSellout() != null && ag.getStatusSellout().equals("1")){
		value.put("statusSellout", "true");
	}
	
	
	if(ag.getStatusDiscount() != null && ag.getStatusDiscount().equals("1")){
		value.put("statusDiscount", "true");
	}
	
	if(ag.getStatusTop() != null && ag.getStatusTop().equals("1")){
		value.put("statusTOP", "true");
	}
	
	if(ag.getInCases() > 0){
		value.put("inCases", ag.getInCases());
	}
	
	if(ag.getPrice() > 0){
		value.put("price", ag.getPrice());
	}
	
	if(ag.getStock() > 0){
		//value.put("stock", ag.getStock());
	}
	
	if(ag.getMeasure() > 0){
		value.put("measure", ag.getMeasure());
	}
	
	if(ag.getMaxDiscount() > 0){
		value.put("maxDiscount", ag.getMaxDiscount());
	}
	
	if(ag.getOnPallet() > 1){
		value.put("onPallet", ag.getOnPallet());
	}
	if(ag.getUnit()!= null && ag.getUnit().trim().length() > 0 ){
		value.put("unit", ag.getUnit().trim());
	}
	
	if(ag.getEAN()!= null && ag.getEAN().trim().length() > 0 ){
		value.put("EAN", ag.getEAN());
	}
	
	if(ag.isDiffColor()){
		value.put("diffColor", "true");
	}
	if(ag.getOptAmountInUnits() > 1){
		value.put("optAmountInUnits", ag.getOptAmountInUnits());
	}
	
	
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
	
	if(cou == 200){
		myson.setJson(deCode(array.toString()));
		json.add(myson);
		System.out.println("MYJSON Product 200 " );
		
		if(json.size() > 0){
			setSendRestMsg(json);
		}
		json = new Vector<MyJSON> ();
		array = new JSONArray();
		cou = 0;
	  // return;
	}
		
	   cou ++;
	
		
		}

		myson.setJson(deCode(array.toString()));
		json.add(myson);
		System.out.println("MYJSON PRODUCT " );
		
		if(json.size() > 0){
			setSendRestMsg(json);
		}
		
	}
	

  public static TreeMap getManagerList(String company, String user , String pass){
	  
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
	  serv.pass = pass;
	  serv.company = company;
	  TreeMap result = new TreeMap();
		
		
		try {
			
			org.json.JSONArray array = serv.getManagerList();
		//	System.out.println("oioi "+ array.toString());
		//	throw new Exception("demo"); 

		for(int i = 0; i <	array.length();i++){
			JSONObject ag = (JSONObject)array.get(i);
			String agent = ag.getString("code");
			String name = ag.getString("name");

			result.put(agent, name);

		}
			
		}catch(Exception ff){}
		serv.closeClient();
		
  
	  return result;
  }
	
	
  public static void getManagersReplacements(TreeMap tm , String company, String user , String pass){
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		
		try {
			org.json.JSONArray array = serv.getManagerList();
		//	System.out.println("oioi "+ array.toString());
		//	throw new Exception("demo"); 
			try {
				if(array.toString().contains("replaceManagers")) {
					Converter.saveFile(  "agreplace.json" , array.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		for(int i = 0; i <	array.length();i++){
			JSONObject ag = (JSONObject)array.get(i);
			String agent = ag.getString("code");
			String passx =  null ;
			try {
				passx = ag.getString("passWord");
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			
		//	System.out.println("Pass " + agent + " " + passx);
			
			Iterator itera =  tm.values().iterator();
		       while (itera.hasNext()){
		    	   Agent aa = (Agent)itera.next();
		    	   if(aa.getID().equals(agent) && aa.needsZipPass && passx != null){
		    		   aa.zipPass = passx; 
		    	   }
		       }
		       
			try{
				
				org.json.JSONArray arr = ag.getJSONArray("replaceManagers");
				for(int e = 0; e <	arr.length();e++){
					JSONObject agas = (JSONObject)arr.get(e);
					String code = agas.getString("code");
					
			        itera =  tm.values().iterator();
			       while (itera.hasNext()){
			    	   Agent aa = (Agent)itera.next();
			    	   if(aa.getID().equals(agent)){
			    		   aa.addDubbler(code);
			    	   }
			    	 
			       }
					
				}
					
				
						
			}catch(Exception k){
				//k.printStackTrace();
			}
			
			
			//if(ag.get("subManagers")!= null){
			//System.out.println(ag.get("subManagers").toString());
						
		//}
		}
			
		}catch(Exception ff){
			ff.printStackTrace();
			System.out.println("saime vea kätte ");
			
			try {
				JSONParser parser = new JSONParser();
			
		       Object obj = parser.parse(new FileReader("agreplace.json"));
		       
		       org.json.simple.JSONArray array = (org.json.simple.JSONArray) obj;
	      
	       	
	   		for(int i = 0; i <	array.size(); i++){
	   			org.json.simple.JSONObject ag = (org.json.simple.JSONObject)array.get(i);
	   			String agent = (String)ag.get("code");
	   			
	   			try{
	   				
	   				org.json.simple.JSONArray arr = (org.json.simple.JSONArray)ag.get("replaceManagers");
	   				if(arr != null) {
	   				
	   				for(int e = 0; e <	arr.size();e++){
	   					org.json.simple.JSONObject agas = (org.json.simple.JSONObject)arr.get(e);
	   					String code = (String)agas.get("code");
	   					
	   			       Iterator itera =  tm.values().iterator();
	   			       while (itera.hasNext()){
	   			    	   Agent aa = (Agent)itera.next();
	   			    	   if(aa.getID().equals(agent)){
	   			    		   aa.addDubbler(code);
	   			    		//   System.out.println("replaceManagers failist  " +code + " "+ agent);
	   			    	   }
	   			       }
	   				 }	
	   				}
	   						
	   			}catch(Exception k){
	   				k.printStackTrace();
	   			}
	   			
	   		
	   		}
        } catch(Exception tt) {
				tt.printStackTrace();
			}
		}
		serv.closeClient();
		
  }
  
  public static String getSVData(long from , long to , String company, String user , String pass, boolean week, String type , String subTypes, String nameadd){
	  return getSVData(new Date(from) , new Date(to) , company,  user ,  pass,  week, type , subTypes , nameadd);
  }
	
  
  public static String getTaskData(long from , long to , String company, String user , String pass,  String nameadd){
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		String res = "";
		try {
			org.json.JSONArray array = serv.getTaskdata(new Date(from), new Date(to) );
			//System.out.println("oioi "+ array.toString());
		//	throw new Exception("demo"); 
			try {
				String mm = (new Date(from).getMonth()+1)+ "";
				if(mm.length() == 1)
					mm = "0"+ mm;
				
				String yyyy = (new Date(from).getYear()+1900)+ "";
				String ww = "";
			
				res = "svdata/taskdata-" + nameadd + ww + mm  + yyyy + ".json" ;
					Converter.saveFile(  "svdata/taskdata-" + nameadd + ww + mm  + yyyy + ".json" , array.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		  } catch (Exception t) {
			t.printStackTrace();
		  }
		
		return res;
		
  }
  
  public static String getSVData(Date from , Date to , String company, String user , String pass, boolean week , String type , String subTypes , String nameadd){
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		String res = "";
 
			try {
				String mm = (from.getMonth()+1)+ "";
				if(mm.length() == 1)
					mm = "0"+ mm;
				
				String yyyy = (from.getYear()+1900)+ "";
				String ww = "";
				Calendar cc = Calendar.getInstance();
				cc.setTime(from);
				if(week) {
					ww = cc.get(Calendar.WEEK_OF_YEAR)+"";
				 }else {
					ww = cc.get(Calendar.DAY_OF_MONTH)+"";
				}
				if(ww.length() == 1) {
					ww = "0"+ww;
				}
				
				String tt = "";
				if(type != null) {
					tt = type + "-";
				}
				if(subTypes != null) {
					tt = subTypes.replaceAll(",", "-") + "-";
				}
				res = "svdata/svdata-" + nameadd + tt + ww + mm  + yyyy + ".json" ;
				
				File ff = new File("svdata/svdata-" + nameadd + tt + ww + mm  + yyyy + ".json" );
			
				org.json.JSONArray array = serv.getSVdata(from, to , type , subTypes);
				
				if(array.length() == 1000) {
					//teeme tundideks aja vahe
					for(int i = 0; i<6;i++) {
						Date fr = new Date(from.getTime() + (7200000*i));
						
						Date t = new Date(from.getTime()+(7200000*i)+7200000);
						array = serv.getSVdata(fr, t , type , subTypes);
						if(array.length() > 0) {
							Converter.saveFileInUTF8(  "svdata/svdata-" + nameadd + tt  + i + ww + mm  + yyyy + ".json" , array.toString());
						}
						
					}
					
					
				}else if(array.length() > 0) {
					Converter.saveFileInUTF8(  "svdata/svdata-" + nameadd + tt  + ww + mm  + yyyy + ".json" , array.toString());
				}
					
			
					
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		
		
		return res;
		
  }
	
  
  public static org.json.JSONArray getSVDataArray(Date from , Date to , String company, String user , String pass, boolean week , String type , String subTypes , String nameadd ){
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		org.json.JSONArray array = null;
 
			try {
				String mm = (from.getMonth()+1)+ "";
				if(mm.length() == 1)
					mm = "0"+ mm;
				
				String yyyy = (from.getYear()+1900)+ "";
				String ww = "";
				if(week) {
					Calendar cc = Calendar.getInstance();
					cc.setTime(from);
					ww = cc.get(Calendar.WEEK_OF_YEAR)+"";
					if(ww.length() == 1) {
						ww = "0"+ww;
					}
				}
				
		 array = serv.getSVdata(from, to , type , subTypes);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		
		
		return array;
		
  }
  
	public static TreeMap getCustDimsLoading(  String asi){
		TreeMap res = null;
		 try {
			 JSONParser parser = new JSONParser();
			 Object obj = null;
			 if(asi.equals("chain")){
				 obj = parser.parse(new FileReader( "custchains.json"));
			 }
			 if(asi.equals("type")){
				 obj = parser.parse(new FileReader( "custtypes.json"));
			 }
			 if(asi.equals("level")){
				 obj = parser.parse(new FileReader( "custlevels.json"));
			 }
			
			 
			 
			 res = new TreeMap();
			   	
	            org.json.simple.JSONArray tagList = (org.json.simple.JSONArray) obj;
	            
	            Iterator<org.json.simple.JSONArray> iterator = ((List) tagList).iterator();
	            while (iterator.hasNext()) {
	            	org.json.simple.JSONArray cob = iterator.next();
	            	
	            	Iterator<org.json.simple.JSONObject> itera = ((List) cob).iterator();
	            	int cc = 0;
	            	String key = null;
	            	String value = null;
	            	while(itera.hasNext()){
	            		cc++;
	            		String cobw = itera.next()+ "";
	            		if(cc == 1){
	            			key = cobw;
	            		}
	            		if(cc == 2 ){
	            			value = cobw;
	            			ArrayList vals = new ArrayList(); 
	            			if(res.containsKey(key)){
	            				vals = (ArrayList)res.get(key);
	            			}
	            			vals.add(value);
	            			res.put(key,vals);
	            			key = null;
	            			cc = 0;
	            		}
	            	}
	                    }
	
		 }catch(Exception e){
			e.printStackTrace(); 
		 }

	        	
		//	System.out.println("Asi " + asi + " "+ res.size()+" " +res.toString() );
    	    
		return res;
	}
  
	public static void loadUploadedFiles(Date algus, Date lopp ,String company, String user , String pass){
		
		try {
			
			 eevaeksport.util.ReportingService serv = new ReportingService();
			  serv.user =  user;
				serv.pass = pass;
				serv.company = company;
	
			
				JSONArray array = serv.getUploadedFileList(algus,lopp);
				Converter.saveFile(  "uploadedfileslist.json" , array.toString());
			
			
					serv.closeClient();	
				
			
			} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	 public static void makeWebFilesList(Date algus, Date lopp ,String company, String user , String pass, String dir, String comp) {
			
		   Converter.loadUploadedFiles(algus, null ,company,  user ,  pass);
		
		  StringBuffer sb  = new StringBuffer();
		
			 try {
				
				 JSONParser parser = new JSONParser();
				 Object obj =  parser.parse(new FileReader( "uploadedfileslist.json"));
				
				 
				 
				
				   	
		            org.json.simple.JSONArray tagList = (org.json.simple.JSONArray) obj;
		            
		            Iterator<org.json.simple.JSONObject> iterator = ((List) tagList).iterator();
		            while (iterator.hasNext()) {
		            	org.json.simple.JSONObject cob = iterator.next();
		            	
		            	String key = (String)cob.get("url");
		            	
		            	try {
		            	     key = java.net.URLDecoder.decode(key, StandardCharsets.UTF_8.name());
		            	} catch (UnsupportedEncodingException e) {
		            	  
		            	}
		            	
		            	System.out.println(key);
		            	
		                String agent = key.substring(key.lastIndexOf("/")+1, key.indexOf("-{"));
		            	String cuid = key.substring(key.indexOf("{")+1, key.indexOf("}"));
		            	String date = key.substring(key.indexOf("}")+2, key.indexOf("}")+8);
		            	String time = key.substring(key.indexOf("}")+9, key.indexOf("}")+17).replaceAll("_", ".");
		            	String gr = " "; 
		            	try {
		            		gr = key.substring(key.indexOf("[")+1, key.indexOf("]"));
	            		} catch (Exception e) {
		            		
		            	}
		            	
		            	date = "20" + date.substring(0,2) + "-" + date.substring(2,4) + "-" + date.substring(4,6);
		            	
		            	
		            	
		            	sb.append( cuid + ";" + agent + ";" + date + ";" + time + ";" + gr + ";" + key + "\n" );
		            	
		            	
		              
		            	
		            }
		           		 }catch(Exception e) {
		           			 e.printStackTrace();
		           		 }
			 String fn = dir + "files-" + comp + "-" + Converter.getDDpMMpYYYYpString(Calendar.getInstance()) +".csv";
		      Converter.saveFile(fn, sb.toString());
		      System.out.println( "File ssved  " + fn);
		           		 
	  }

	public static void loadCustDimensions(String asi ,String company, String user , String pass){
		
		try {
			
			 eevaeksport.util.ReportingService serv = new ReportingService();
			  serv.user =  user;
				serv.pass = pass;
				serv.company = company;
	
			 if(asi.equals("chain")){
				JSONArray array = serv.getCustomersChains();
				Converter.saveFile(  "custchains.json" , array.toString());
			 }
			
			 if(asi.equals("level")){
	            JSONArray array = serv.getCustomersLevel();
	            Converter.saveFile(  "custlevels.json" , array.toString());
	         }
				
			if(asi.equals("type")){
	            JSONArray array = serv.getCustomersTypes();
	            Converter.saveFile(  "custtypes.json" , array.toString());
	          
			}
			
					serv.closeClient();	
				
			
			} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
public static void loadVisitLines(String status,String company, String user , String pass){
		
		try {
			
			 eevaeksport.util.ReportingService serv = new ReportingService();
			  serv.user =  user;
				serv.pass = pass;
				serv.company = company;
	
				JSONArray array = serv.getVisitedList(status);
				Converter.saveFile(  "visitlines" + status + ".json" , array.toString());
			
			
					serv.closeClient();	
				
			
			} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
  
	  public static void getContractsFixedByProd(TreeMap tm , String company, String user , String pass, Boolean useweb){
		  if(!useweb) {
			  eevaeksport.util.ReportingService serv = new ReportingService();
			  serv.user =  user;
				serv.pass = pass;
				serv.company = company;
				
				try {
					org.json.JSONArray array = serv.getContracts();
				
				//	throw new Exception("demo"); 
					
						
							Converter.saveFile(  "contracts.json" , array.toString());
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					serv.closeClient();
			  }
			  
			  
					
					try {
			
				JSONParser parser = new JSONParser();
				 Object obj = parser.parse(new FileReader( "contracts.json"));
				
				 
				
		            org.json.simple.JSONArray tagList = (org.json.simple.JSONArray) obj;

			for(int i = 0; i <	tagList.size();i++){
				org.json.simple.JSONObject cob =  ((org.json.simple.JSONObject) tagList.get(i));
				Long start = null;
				try {
		       	 start = (Long)cob.get("starts");
				}catch(Exception j) {
	        		
	        	}
		       	Long end = null;
		       	try {
			       	 end = (Long)cob.get("ends");
					}catch(Exception j) {
		        		
		        	}
		       	Long created = (Long)cob.get("created");
		       	//	String name = (String)cob.get("name");
		        	String contractType = (String)cob.get("contractType");
		    	// System.out.println("load contracts mess " + start + " " + end + " " + created);
		        	Calendar now = Calendar.getInstance();
		        	long weeks2 = 1209600000; 
		        	String type = contractType;
		        	String level = null;
		        	String customer = null;
		        	try {
		        		org.json.simple.JSONArray levelList = (org.json.simple.JSONArray) cob.get("levels");
				          
				         if(levelList != null){
				        	 for (int r = 0 ; r < levelList.size() ;r++) {
				        		 org.json.simple.JSONObject typex = (org.json.simple.JSONObject) levelList.get(r);
					            	level = (String)typex.get("code");
					               	
					            } 
				         }
		        	}catch(Exception j) {
		        		
		        	}
		        	
			        	 
			        	// System.out.println("typs size " + typs.size());
		        	try {  
		        		org.json.simple.JSONArray custList = (org.json.simple.JSONArray) cob.get("customers");
			        
			         if(custList != null){
			        	 for (int r = 0 ; r < custList.size() ;r++) {
			        		 org.json.simple.JSONObject cus = (org.json.simple.JSONObject) custList.get(r);
		                       	customer = (String)cus.get("code");	
		                    } 
			         }
	            }catch(Exception j) {
		        		
		        	}
	                     
	                 
			       
		        	/*  if(customer != null && customer.contains( "00002")) {
                   	   System.out.println("typs  " + customer + " " + level ); 	
                      }
                      if(level != null && level.contains( "00002")) {
                   	   System.out.println("typs0  " + customer + " " + level ); 	
                      }*/
		        	
		      if((type.equals("FIXED")) && 
		       ((start != null && end != null && start < Calendar.getInstance().getTimeInMillis()) && (end > Calendar.getInstance().getTimeInMillis())
		    		   ||  (start == null && end == null && (created - weeks2 < now.getTimeInMillis())))){
		    	
		    	  org.json.simple.JSONArray prodList = null;

		    	  
		    	  try {
			    	 	prodList = (org.json.simple.JSONArray) cob.get("contractLines");

		    	  }catch(Exception e) {
		    		  
		    	  }
		    	 	
	                if(prodList != null) {
	               
	                     for (int r = 0 ; r < prodList.size() ;r++) {
	                    	 
	                    	 org.json.simple.JSONObject line = (org.json.simple.JSONObject) prodList.get(r);
	                     	Long create = (Long)line.get("created");
	                     	 Boolean pour = null;
	                     	 try {
	                     		 pour = (Boolean)line.get("pouringdeal");
	                     	 }catch(Exception k) {
	                     		 
	                     	 }
	                     	
	                     	String till = null;
	                     	try {
	                     		till = (String)line.get("enddate");
	                     	 }catch(Exception k) {
	                     		 
	                     	 }
	                     	
	                     	
	                     	 Boolean coct = null;
	                     	try {
	                     		coct = (Boolean)line.get("cocktaildeal");
	                     	 }catch(Exception k) {
	                     		 
	                     	 }
	                     	 
	                     	Object cnum = (long) 0;
	                     	
	                     	try {
	                     		cnum = line.get("kpi");
	                     	
	                     		//System.out.println("cnum "+  cnum);
	                     		
	                     	 }catch(Exception k) {
	                     		// k.printStackTrace();
	                     	 }
	                     	try {
                            	if(line.get("product") != null) {
                            	org.json.simple.JSONObject prod = (org.json.simple.JSONObject)line.get("product");
	                            String prcode = (String)prod.get("code");
	                            String prname = null ;
	                            try {
	                            	 prname = (String)prod.get("name");
	                            }catch(Exception e) {
	                            	
	                            }
	                           
	                            if(prname == null) {
	                            	prname = "name n/a";
	                            }
	                            
	                            
	                            String prsta = null ;
	                            try {
	                            	 prsta = (String)prod.get("name");
	                            }catch(Exception e) {
	                            	
	                            }
	                            
	                            if(till == null || inFuture(till)) {
	                            
	                            
	                            
	                            ArrayList custs = new ArrayList();
	                            if(tm.get(prcode) != null) {
	                        		custs = (ArrayList)tm.get(prcode);
	                        	}
	                        	if(level != null) {
	                        		Toode tt = new Toode(level);
	                        		tt.setGr1(prcode);
	                        		tt.setBrand(prname);
	                        		tt.setLadu(create+"");
	                        		if(till != null)
	                        		tt.setGr2(till + "");
	                        		
	                        		if(coct != null) {
	                        			tt.setPlancur(1);
	                        		}
	                        		
	                        		if(pour != null) {
	                        			tt.setPlanvol(1);
	                        		}
	                        		
	                        		if(cnum != null ) {
	                        			tt.setKogus(Double.parseDouble(cnum.toString()+ ""));
	                        		}
	                        		
	                        		custs.add(tt);
	                        	//	if(level != null && level.contains( "00002")) {
	                                	 //  System.out.println("seeslev  " + level + " " + prcode + " " + prname ); 	
	                                //   }
	                        	} else if(customer != null) {
	                        		Toode tt = new Toode(customer);
	                        		tt.setBrand(prname);
	                        		tt.setGr1(prcode);
	                        		if(till != null)
	                        		tt.setGr2(till + "");
	                        		tt.setLadu(create+"");
	                        		if(coct != null) {
	                        			tt.setPlancur(1);
	                        		}
	                        		if(pour != null) {
	                        			tt.setPlanvol(1);
	                        		}
	                        		custs.add(tt);
	                        		//if(customer != null && customer.contains( "00002")) {
	                                	//   System.out.println("sees  " + customer + " " + prcode + " " + prname ); 	
	                                   //}
	                        	}
	                        tm.put(prcode, custs);
                            	}
	                        	
	                            }
	                     	}catch(Exception k) {
	                     		 
	                     	 }
	                     }
	                }
	             }
		      }
							
				} catch(Exception k){
					k.printStackTrace();
				}
		
			
	  }
		
	  
	  private static boolean inFuture(String till) {
		  Calendar cal = Calendar.getInstance();
		  cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(till.substring(8,10)));
		  cal.set(Calendar.MONTH, Integer.parseInt(till.substring(5,7)) -1);
		  cal.set(Calendar.YEAR, Integer.parseInt(till.substring(0,4)));
		  if(cal.getTime().after(new Date())) {
			  return true;
		  }
		  
		  return false;
	  }
  
  public static void getContractsFixedByCust(TreeMap tm , String company, String user , String pass, Boolean useweb){
	  
	  if(!useweb) {
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		

			try {
			org.json.JSONArray array = serv.getContracts();
		
				
					Converter.saveFile(  "contracts.json" , array.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			serv.closeClient();
	  }
	  
	  
			
			try {
			JSONParser parser = new JSONParser();
			 Object obj = parser.parse(new FileReader( "contracts.json"));
			
			 
			
	            org.json.simple.JSONArray tagList = (org.json.simple.JSONArray) obj;

		for(int i = 0; i <	tagList.size();i++){
			org.json.simple.JSONObject cob =  ((org.json.simple.JSONObject) tagList.get(i));
			Long start = null;
			try {
	       	 start = (Long)cob.get("starts");
			}catch(Exception j) {
        		
        	}
	       	Long end = null;
	       	try {
		       	 end = (Long)cob.get("ends");
				}catch(Exception j) {
	        		
	        	}
	       	Long created = (Long)cob.get("created");
	       	//	String name = (String)cob.get("name");
	        	String contractType = (String)cob.get("contractType");
	    	// System.out.println("load contracts mess " + start + " " + end + " " + created);
	        	Calendar now = Calendar.getInstance();
	        	long weeks2 = 1209600000; 
	        	String type = contractType;
	        	String level = null;
	        	String customer = null;
	        	try {
	        		org.json.simple.JSONArray levelList = (org.json.simple.JSONArray) cob.get("levels");
			          
			         if(levelList != null){
			        	 for (int r = 0 ; r < levelList.size() ;r++) {
			        		 org.json.simple.JSONObject typex = (org.json.simple.JSONObject) levelList.get(r);
				            	level = (String)typex.get("code");
				               	
				            } 
			         }
	        	}catch(Exception j) {
	        		
	        	}
	        	
		        	 
		        	// System.out.println("typs size " + typs.size());
	        	try {  
	        		org.json.simple.JSONArray custList = (org.json.simple.JSONArray) cob.get("customers");
		        
		         if(custList != null){
		        	 for (int r = 0 ; r < custList.size() ;r++) {
		        		 org.json.simple.JSONObject cus = (org.json.simple.JSONObject) custList.get(r);
	                       	customer = (String)cus.get("code");	
	                    } 
		         }
            }catch(Exception j) {
	        		
	        	}
                       
                 
		        // System.out.println("typs  " + customer + " " + level ); 	
	        	
	        	
	      if((type.equals("FIXED")) && 
	       ((start != null && end != null && start < Calendar.getInstance().getTimeInMillis()) && (end > Calendar.getInstance().getTimeInMillis())
	    		   ||  (start == null && end == null && (created - weeks2 < now.getTimeInMillis())))){
	    	  ArrayList lines = new ArrayList();
	    	  org.json.simple.JSONArray prodList = (org.json.simple.JSONArray) cob.get("contractLines");
                if(prodList != null) {
               
                     for (int r = 0 ; r < prodList.size() ;r++) {
                    	 org.json.simple.JSONObject line = (org.json.simple.JSONObject) prodList.get(r);
                    	 org.json.simple.JSONObject prod = (org.json.simple.JSONObject)line.get("product");
                        	lines.add( (String)prod.get("code"));
                     }
                }
                if(level != null) {
                	  tm.put(level, lines);  
                }else if(customer != null) {
                	tm.put(customer, lines);  
                }
              
	      }
	      }
						
			} catch(Exception k){
				k.printStackTrace();
			}
			
			
		
		
  }
	
	
	
	
 public static void getContractsSuggByCustOrType(TreeMap tm , String company, String user , String pass, Boolean useweb){
	  //jungent LV tellimusel tehtud
	 
	  if(useweb) {
	  eevaeksport.util.ReportingService serv = new ReportingService();
	  serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		

			try {
			org.json.JSONArray array = serv.getContracts();
		
				
					Converter.saveFile(  "contracts.json" , array.toString());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			serv.closeClient();
	  }
	  
	  
			
			try {
			JSONParser parser = new JSONParser();
			 Object obj = parser.parse(new FileReader( "contracts.json"));
			
			 
			
	            org.json.simple.JSONArray tagList = (org.json.simple.JSONArray) obj;

		for(int i = 0; i <	tagList.size();i++){
			org.json.simple.JSONObject cob =  ((org.json.simple.JSONObject) tagList.get(i));
			Long start = null;
			try {
	       	 start = (Long)cob.get("starts");
			}catch(Exception j) {
        		
        	}
	       	Long end = null;
	       	try {
		       	 end = (Long)cob.get("ends");
				}catch(Exception j) {
	        		
	        	}
	       	Long created = (Long)cob.get("created");
	       	//	String name = (String)cob.get("name");
	        	String contractType = (String)cob.get("contractType");
	    	 System.out.println("load contracts mess " + start + " " + end + " " + created);
	        	Calendar now = Calendar.getInstance();
	        	long weeks2 = 1209600000; 
	        	String type = contractType;
	        	String level = null;
	        	String customer = null;
	        	try {
	        		org.json.simple.JSONArray levelList = (org.json.simple.JSONArray) cob.get("types");
			          
			         if(levelList != null){
			        	 for (int r = 0 ; r < levelList.size() ;r++) {
			        		 org.json.simple.JSONObject typex = (org.json.simple.JSONObject) levelList.get(r);
				            	level = (String)typex.get("code");
				               	
				            } 
			         }
	        	}catch(Exception j) {
	        		
	        	}
	        	
		        	 
		        	// System.out.println("typs size " + typs.size());
	        	try {  
	        		org.json.simple.JSONArray custList = (org.json.simple.JSONArray) cob.get("customers");
		        
		         if(custList != null){
		        	 for (int r = 0 ; r < custList.size() ;r++) {
		        		 org.json.simple.JSONObject cus = (org.json.simple.JSONObject) custList.get(r);
	                       	customer = (String)cus.get("code");	
	                    } 
		         }
            }catch(Exception j) {
	        		
	        	}
                       
                 
		    //     System.out.println("typs  " + customer + " " + level ); 	
	        	
	        	
	      if((type.equals("SUGGESTED")) && 
	       ((start != null && end != null && start < Calendar.getInstance().getTimeInMillis()) && (end > Calendar.getInstance().getTimeInMillis())
	    		   ||  (start == null && end == null && (created - weeks2 < now.getTimeInMillis())))){
	    	  ArrayList lines = new ArrayList();
	    	  org.json.simple.JSONArray prodList = (org.json.simple.JSONArray) cob.get("contractLines");
                if(prodList != null) {
               
                     for (int r = 0 ; r < prodList.size() ;r++) {
                    	 org.json.simple.JSONObject line = (org.json.simple.JSONObject) prodList.get(r);
                    	 org.json.simple.JSONObject prod = (org.json.simple.JSONObject)line.get("product");
                        	lines.add( (String)prod.get("code"));
                     }
                //     System.out.println( level + " " + lines.toString());
                }
                if(level != null) {
                	  tm.put(level, lines);  
                }else if(customer != null) {
                	tm.put(customer, lines);  
                }
              
	      }
	      }
						
			} catch(Exception k){
				k.printStackTrace();
			}
			
			
		
		
  }
	
	
	public static void sendCustomerJson(TreeMap tm , String company,boolean addRoutes, String user , String pass, boolean sendpartner){
		sendCustomerJson( tm ,company,addRoutes, user ,  pass, false,sendpartner); 
	}
		
	
	
	
	public static void sendCustomerJson(TreeMap tm , String company,boolean addRoutes, String user , String pass, boolean duplicates , boolean sendpartner){
		Vector<MyJSON> json = new Vector<MyJSON> ();
		
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
	  
	  
	 // loeme koodid 
	
		HashSet used = new HashSet();
		if(!duplicates){
			eevaeksport.util.ReportingService serv = new ReportingService();
			   
			serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			try {
				Vector arra = serv.getCustomersCodes();
		  		for (int i = 0; i < arra.size(); i++) {
						 used.add((String)arra.elementAt(i));
				 }
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			serv.closeClient();
		  System.out.println("Custs used "  + used.size());
		  	
		}
	  
		
		
		MyJSON myson = new MyJSON();
		myson.setUrl("http://merisoft.eu:8080/rest/customer/list/");
	  JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		
		int cou = 0;
		TreeMap visits = new TreeMap();
		
		while (itera.hasNext()){
			Customer ccc = (Customer)itera.next();
			if(!used.contains(ccc.getID()) || used.size() == 0){
				
			
			String visit = ccc.getVisit();
			String call = ccc.getCallDay();
			
			if(ccc.hasAgents()){
			JSONObject value = new JSONObject();
try{

	value.put("code", ccc.getID());
	if(ccc.restName != null){
		value.put("name", ccc.restName);
	}else{
		value.put("name", ccc.getName());
	
	}
		
	
	value.put("company", company);

	if(ccc.restdirect != null) {
		value.put("direct", "direct");
	}
	
	
	if(ccc.restPartner != null && sendpartner) {
		 JSONObject aaa = new JSONObject();
		 aaa.put("code", ccc.restPartner);
		value.put("partner", aaa);	
	}

	
	
	
	if(ccc.getGroup()!= null && ccc.getGroup().trim().length() > 0 && ccc.getGroup().trim().length() < 200){
		
	   JSONArray arrayx = new JSONArray();
	   StringTokenizer st = new StringTokenizer(ccc.getGroup(),"|");
		while (st.hasMoreTokens()){
		String tok = st.nextToken();
		JSONObject valuex = new JSONObject();
		valuex.put("code", tok);
		valuex.put("name", tok);
		valuex.put("type", "group");
		valuex.put("company", company);
		arrayx.put(valuex);
		}
		value.put("group", arrayx);
	}
	
	

	if(ccc.category != null && ccc.category.trim().length() > 0 && ccc.category.trim().length() < 200){
	   JSONArray arrayx = new JSONArray();
	   
	   StringTokenizer st = new StringTokenizer(ccc.category,"|");
		while (st.hasMoreTokens()){
		String tok = st.nextToken();
		JSONObject valuex = new JSONObject();
		valuex.put("code", tok);
		valuex.put("name", tok);
		valuex.put("type", "type");
		valuex.put("company", company);
		arrayx.put(valuex);
		}
		value.put("type", arrayx);
	}
	
	if(ccc.level != null && ccc.level.trim().length() > 0 && ccc.level.trim().length() < 200){
		   JSONArray arrayx = new JSONArray();
		   StringTokenizer st = new StringTokenizer(ccc.level,"|");
			while (st.hasMoreTokens()){
			String tok = st.nextToken();
		   JSONObject valuex = new JSONObject();
			valuex.put("code", tok);
			valuex.put("name", tok);
			valuex.put("type", "level");
			valuex.put("company", company);
			arrayx.put(valuex);
	}
			value.put("level", arrayx);
		}
	
	if(ccc.getChain()!= null && ccc.getChain().trim().length() > 0 ){
		System.out.println(ccc.getChain());
		JSONArray arrayx = new JSONArray();
		JSONObject valuex = new JSONObject();
		valuex.put("code", ccc.getChain());
		valuex.put("name", ccc.getChain());
		valuex.put("type", "chain");
		valuex.put("company", company);
		arrayx.put(valuex);
		value.put("chain", arrayx);
	}
	
	if(ccc.getZipcode()!= null && ccc.getZipcode().trim().length() > 0){
		value.put("zipcode", ccc.getZipcode());
	}
	
	if(ccc.restZipCode!= null && ccc.restZipCode.trim().length() > 0){
		value.put("zipcode", ccc.restZipCode);
	}
	
	if(ccc.getCredit()!= null && ccc.getCredit().trim().length() > 0){
		value.put("credit", ccc.getCredit());
	}
	
	
	
	if(false){
		//sellega saab managerid maha võtta 
		JSONArray arrayx = new JSONArray();
		 value.put("managers", arrayx);
	}else{
	
	if((ccc.getMainAgent() != null && ccc.getMainAgent().trim().length() > 0)){
		JSONArray arrayx = new JSONArray();
		JSONObject valuex = new JSONObject();
		valuex.put("code", ccc.getMainAgent().trim());
		valuex.put("name", ccc.getMainAgent().trim());
		valuex.put("active", true);
		valuex.put("company", company);
       arrayx.put(valuex);
       
    
	 value.put("managers", arrayx);
	 
	 if(visit != null && visit.trim().length() > 0){
			Agent va = new Agent (ccc.getMainAgent());
			va.setVisit(visit);
			Vector vv = new Vector();
			if(visits.get(ccc.getID()) != null){
				vv = (Vector)visits.get(ccc.getID());
			}
			vv.add(va);
			visits.put(ccc.getID(), vv);	
		}
		
	 
	}else if(ccc.hasAgents() ){
		//System.out.println("Has Agent " + visit + " "+ call + " " + ag.getID() );
		JSONArray arrayx = new JSONArray();
		
		TreeMap ags = ccc.getAgents();
		Iterator iterax = ags.values().iterator();
		while (iterax.hasNext()){
			Agent aa = (Agent)iterax.next();
			
			if(addRoutes){
			if(!aa.getID().contains("HOUS")){
				
			
			if(aa.getVisit()!= null && aa.getVisit().trim().length() > 0){
				visit = aa.getVisit();
			}
             
			if(aa.getCall()!= null && aa.getCall().trim().length() > 0){
				call = aa.getCall();
			}
			
			
			//System.out.println("Has Agent2 " + visit + " "+ call + " " + ag.getID() );
			if(visit != null && visit.trim().length() > 0){
				Agent va = new Agent (aa.getID());
				va.setVisit(visit);
				Vector vv = new Vector();
				if(visits.get(ccc.getID()) != null){
					vv = (Vector)visits.get(ccc.getID());
				}
				vv.add(va);
				visits.put(ccc.getID(), vv);	
			}
			
			if(call != null && call.trim().length() > 0){
				Agent va = new Agent (aa.getID());
				va.setCall(call);
				Vector vv = new Vector();
				if(visits.get(ccc.getID()) != null){
					vv = (Vector)visits.get(ccc.getID());
				}
				vv.add(va);
				visits.put(ccc.getID(), vv);	
			}}
			}
			
			JSONObject valuex = new JSONObject();
			valuex.put("code", aa.getID());
			valuex.put("active", true);
			if(aa.getName()!= null && aa.getName().length() > 0 ){
				valuex.put("name", aa.getName());
			 } else {
				valuex.put("name", aa.getID());
			 }
			valuex.put("company", company);
	  if(!aa.getID().equals("")){
		  arrayx.put(valuex);
		 }
		}
		
		value.put("managers", arrayx);
	}
	
	}
	
	
	if(ccc.getInfo()!= null && ccc.getInfo().length() < 255 && ccc.getInfo().trim().length() > 0){
		value.put("info", ccc.getInfo());
	}
	
	if(ccc.getPayer() != null && ccc.getPayer().trim().length() > 0){
		value.put("payer", ccc.getPayer());
	}
	
	if(ccc.getPaymTerm() != null && ccc.getPaymTerm().trim().length() > 0){
		value.put("paymterm", ccc.getPaymTerm());
	}
	if(ccc.restPerson != null && ccc.restPerson.trim().length() > 0){
		value.put("person", ccc.restPerson);
	}
	
	if(ccc.restCity != null && ccc.restCity.trim().length() < 200 && ccc.restCity.trim().length() > 0){
		value.put("city", ccc.restCity);
	}
	
	if(ccc.restStreet != null && ccc.restStreet.trim().length() > 0){
		value.put("street", ccc.restStreet);
	}
	
	if(ccc.restPerEmail != null && ccc.restPerEmail.trim().length() > 0){
		value.put("peremail", ccc.restPerEmail);
	}
	
	if(ccc.restPerPhone != null && ccc.restPerPhone.trim().length() > 0){
		value.put("perphone", ccc.restPerPhone);
	}
	if(ccc.getLicence() != null && ccc.getLicence().trim().length() > 0){
		value.put("licence", ccc.getLicence());
	}
	
	if(ccc.getVatnr() != null && ccc.getVatnr().trim().length() > 0){
		value.put("vatno", ccc.getVatnr());
	}
	
	if(ccc.getRegnr() != null && ccc.getRegnr().trim().length() > 0){
		value.put("regno", ccc.getRegnr());
	}
	
	if(ccc.isSalesBlock()){
		//value.put("blocked", ag.isSalesBlock());
	}else{
		value.put("blocked", false);
	}
	
	
	if(ccc.getCountry() != null && ccc.getCountry().trim().length() > 0 ){
		value.put("country", ccc.getCountry());
	}
	
	
}catch(Exception e){
	e.printStackTrace();
}
	if(ccc.restActive && value.length() > 0 ){
		array.put(value);
	}
	
	if(cou == 150){
		myson.setJson(deCode(array.toString()));
		json.add(myson);
		System.out.println("MYJSON CUSTOMER 150 ");
		
		if(json.size() > 0){
		//	setSendRestMsg(json);
		
			 try {
				 
				 
				 eevaeksport.util.ReportingService  serv = new ReportingService();
				   
					serv.user =  user;
					serv.pass = pass;
					serv.company = company;
				    serv.postJson(deCode(array.toString()), myson.getUrl());
					serv.closeClient();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	
		if(	addRoutes){
		//	System.out.println("Visits "+ visits.size());
			
			addRoutes(visits,user,pass,company);
			visits = new TreeMap();
		}
		
		json = new Vector<MyJSON> ();
		array = new JSONArray();
		cou = 0;
	  // return;
		//System.exit(0);
	}
		
	   cou ++;
	
		}
			
		}
		}

		myson.setJson(deCode(array.toString()));
		json.add(myson);
		System.out.println("MYJSON CUSTOMER " + array.length());
		
		if(json.size() > 0){
			
         try {
			
        	 eevaeksport.util.ReportingService serv = new ReportingService();
			serv.user =  user;
			serv.pass = pass;
			serv.company = company;
		    serv.postJson(deCode(array.toString()), myson.getUrl());
			serv.closeClient();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       //  setSendRestMsg(json);
		}
		if(	addRoutes){
			//addRoutes(visits,user,pass,company);
		}
		
		
	}
	
	
	   
    public static String parseFirstEmail(String emails) {
        String		email = "";
        StringTokenizer st =
            new StringTokenizer(emails, String.valueOf(";"));

        if (st.hasMoreTokens()) {
            email = st.nextToken();
        }

        return email;
    }
    
	

   private static void addRoutes(TreeMap contacts, String user, String pass, String company) {
	 Vector visits = new Vector();
	 Iterator itera = contacts.keySet().iterator();
	 System.out.println("Adding Routes ");
	 while (itera.hasNext()){
		
		String cust = (String)itera.next();
		Vector vvv = (Vector)contacts.get(cust);
		for(int x = 0 ; x < vvv.size();x++){
			Agent ag = (Agent)vvv.elementAt(x);
		
		StringBuffer weekObj = new StringBuffer();
	    char ss = ' ';
	    String day = "";
	    if(ag.getVisit() != null && ag.getVisit().trim().length() > 0){
	    	ss = 'V';
	    	day = ag.getVisit();
	    }
	    if(ag.getCall() != null && ag.getCall().trim().length() > 0){
	    	ss = 'C';
	    	day = ag.getCall();
	    }
	   
	    /// määrama päeva ja nädalad
	   // String weeks = 
	    String days = getDays(day);
	    
	    
	    StringTokenizer st = new StringTokenizer(days,",");
	    
	    while (st.hasMoreTokens()){
	    	
	    	boolean w1 = true;
	    	boolean w2 = true;
	    	boolean w3 = true;
	    	boolean w4 = true;
	    	
	    	String dd = st.nextToken();
	    	if(dd.indexOf(":") != -1){
	    		String wn = dd.substring(dd.indexOf(":")+1);
	    		if(wn.equals("1")){
	    			w1 = false;
	    			w3 = false;
	    		}else if(wn.equals("2")){
	    			w2 = false;
	    			w4 = false;
	    		}else if(wn.equals("4")){
	    			w2 = false;
	    			w3 = false;
	    			w4 = false;
	    		}else if(wn.equals("8")){
	    			w1 = false;
	    			w3 = false;
	    			w4 = false;
	    		}else if(wn.equals("16")){
	    			w1 = false;
	    			w2 = false;
	    			w4 = false;
	    		}else if(wn.equals("32")){
	    			w1 = false;
	    			w2 = false;
	    			w3 = false;
	    		}
	    	 dd = dd.substring(0, dd.indexOf(":"));
	    	}
	    
	    System.out.println("w1" + days + " "+ w1 +" " +w2 + " "+ w3 +" "+ w4 );
	    
	    
	    weekObj.append( "{\"json\": {\"customer\": \"" + cust + "\",\"weeks\": [ ");
	    String com = ",";
	    for(int y = 0 ; y < 4; y++){
	    	
	    	weekObj.append("{");
	    	boolean goes = false;
	    	if(y == 0 && w1){
	    		goes = true;
	    	}
	    	if(y == 1 && w2){
	    		goes = true;
	    	}
	    	if(y == 2 && w3){
	    		goes = true;
	    	}
	    	if(y == 3 && w4){
	    		goes = true;
	    	}
	        
	    	if(goes){
	    	
		        if(ss =='V'){
		        	if(!weekObj.toString().contains("\"" + dd + "\": \"Visit\"")){
		        		  weekObj.append("\"" + dd + "\": \"Visit\"");	
		        	}
		      
		        }
		        if(ss== 'C'){
		        	if(!weekObj.toString().contains("\"" + dd + "\": \"Call\"")){
		                weekObj.append("\""+ dd + "\": \"Call\"");
		    		}
		        }
		       	if(y == 3){
		       		com = "";
		       	}
	    	
	    	}
	        weekObj.append("}"+ com);
	        
	    }
	    
	    weekObj.append("],\"manager\": \"" + ag.getID() + "\" }}");
	   try{
		   visits.add( new JSONObject(weekObj.toString()));
			   
	   }catch(Exception d){
		   d.printStackTrace();
	   }
	    }
	   }
	 }
	 
	 try{
		  System.out.println("visits " + visits.size());
		  System.out.println("visits " + visits.elementAt(0));
			
	  }catch(Exception e){
        e.printStackTrace();		  
	  }
	
	 Converter.sendRouteCalsJson(visits,user,pass,company);

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
   
	public static String getDays(String in ){
		String asi = "";
		for (int i= 0;i < in.length();i++){
			char a = in.charAt(i);
			char b = ' ';
			String add = "";
			try{
				 b = in.charAt(i+1);
				 if(isNumeric(b)){
					add = ":"+b; 
				 }
				 b = in.charAt(i+2);
				 if(isNumeric(b)){
					add = add + b; 
				 }
			}catch(Exception k){
			}
			
			add = add + ",";
			
			if(a=='E'){
				asi = asi+ "Mon" + add;
				
			}else if(a=='T'){
				asi = asi+ "Tue"+ add;
			}else if(a=='K'){
				asi = asi+ "Wed"+ add;
			}else if(a=='N'){
				asi = asi+ "Thu"+ add;
			}else if(a=='R'){
				asi = asi+ "Fri"+ add;
			}else if(a=='L'){
				asi = asi+ "Sat"+ add;
			}else if(a=='P'){
				asi = asi+ "Sun"+ add;
			}
			
		}

		return asi;
	}

	
	public static void sendPodJson(TreeMap tm ,String user,String pass, String company){
	
		
		
		HashSet used = new HashSet();
	
			eevaeksport.util.ReportingService serv = new ReportingService();
			   
			serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			try {
				Vector arra = serv.getPodsCodes();
		  		for (int i = 0; i < arra.size(); i++) {
						 used.add((String)arra.elementAt(i));
				 }
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		  System.out.println("Pods used "  + used.size());
		  	
	  
		Vector<MyJSON> json = new Vector<MyJSON> ();
	
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		String url = "http://merisoft.eu:8080/rest/pod/list/";
	    JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		
		int cou = 0;
		
		while (itera.hasNext()){
			Customer ag = (Customer)itera.next();
			if((!used.contains(ag.restPartner + "_" + ag.getID()) && 
					!used.contains(ag.getID())) || used.size() == 0){
			
			JSONObject value = new JSONObject();
try{
	
	value.put("code", ag.restPartner + "_" + ag.getID());
	value.put("name", deCode(ag.getName()));
	value.put("company", company);
	value.put("extCode", (ag.getID()));
	
	JSONObject partner = new JSONObject();
	partner.put("code", ag.restPartner);
	value.put("partner", partner);
	
	if(ag.restPerson != null){
		value.put("person", deCode(ag.restPerson));
	}
	
	
	if(ag.restZipCode != null){
		value.put("zipcode", ag.restZipCode);
	}
	
	if(ag.restStreet != null){
		value.put("street", ag.restStreet);
	}
	

	if(ag.restPerPos != null){
		value.put("perposition", ag.restPerPos);
	}
	
	if(ag.restCity != null){
		value.put("city", ag.restCity);
	}

	if(ag.restPerPhone != null){
		value.put("perphone", ag.restPerPhone);
	}
	
	
	if(ag.restPerEmail != null){
		value.put("peremail", ag.restPerEmail);
	}
	
	
	if(ag.getCountry() != null){
		value.put("country", ag.getCountry());
	}
	
	if(ag.restRoute != null){
       JSONArray arrayx = new JSONArray();
       for(int i = 0;i< ag.restRoute.size();i++){
    	   JSONObject valuex = new JSONObject();
   		valuex.put("code", ag.restRoute.elementAt(i));
   		arrayx.put(valuex);
   	   }
		value.put("routes", arrayx);	
	}
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
	
	if(cou == 100){
		System.out.println("MYJSON POD 100 " );
		
		try {
			serv.postJson( array.toString(),url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		array = new JSONArray();
		cou = 0;
	}
		
	   cou ++;
	
		}
			
		}
	System.out.println("MYJSON POD jääk " + array.length() );
			try {
				serv.postJson( array.toString(),url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			serv.closeClient();
	}
	

	
	

	public static void sendRoutesJson(TreeMap tm , String user , String pass, String company){
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	String url = "http://merisoft.eu:8080/rest/cargoroute/list/";
	 
	JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		int cou = 0;
		while (itera.hasNext()){
			Customer ag = (Customer)itera.next();
			JSONObject value = new JSONObject();
try{
	value.put("code", ag.restPartner + "_" +  ag.getID());
	value.put("name", ag.getName());
	value.put("company", company);

	if(ag.restComment != null){
		value.put("comment", ag.restComment);
	}
    
	
	JSONObject partner = new JSONObject();
	partner.put("code", ag.restPartner);
	value.put("partner", partner);
	
	JSONArray arrayV = new JSONArray();
	if (ag.restVechicles != null){
		for(int i = 0; i < ag.restVechicles.size() ; i++){
			JSONObject value2 = new JSONObject();
			value2.put("plate", ag.restVechicles.elementAt(i));
			arrayV.put(value2);
		}
		value.put("vehicles", arrayV);
	}
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
	
	if(cou == 10){
		System.out.println("MYJSON ROUTE 10 " );
		
			try {
				serv.postJson(array.toString(), url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		array = new JSONArray();
		cou = 0;
	   }
	   cou ++;
		}

	
		System.out.println("MYJSON ROUTE jääk " );
		
	
		try {
			serv.postJson(array.toString(), url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
	}
	
	public static void saveRoutesJson(TreeMap tm ){
		
	 
	JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		int cou = 0;
		
		
		while (itera.hasNext()){
			Customer ag = (Customer)itera.next();
			JSONObject value = new JSONObject();
try{
	
	String ees = "";
	
	if(ag.restPartner != null ) {
		ees = ag.restPartner + "_" ;
	}
	value.put("code", ees +    ag.getID());
	value.put("name", ag.getName());

	if(ag.restComment != null){
		value.put("comment", ag.restComment);
	}
    
	if(ag.restPartner != null) {
		
	
	JSONObject partner = new JSONObject();
	partner.put("code", ag.restPartner);
	value.put("partner", partner);
	}
	JSONArray arrayV = new JSONArray();
	if (ag.restVechicles != null){
		for(int i = 0; i < ag.restVechicles.size() ; i++){
			JSONObject value2 = new JSONObject();
			value2.put("plate", ag.restVechicles.elementAt(i));
			arrayV.put(value2);
		}
		value.put("vehicles", arrayV);
	}
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
		}
		
		try {
			Converter.saveFile("json" + System.getProperty("file.separator") 
			+ "routes.json" , array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
	}
	
	
	public static void saveShipitListJson(Vector vec, TreeMap custs){
		

	JSONArray array = new JSONArray();
		Enumeration itera = vec.elements();
		int cou = 0;
		while (itera.hasMoreElements()){
			Order ord = (Order)itera.nextElement();
			String id = ord.id;
			String podid = ord.getCompany();
			String origid = ord.origId;
			String priority = ord.restPriority;
			Date deliveryDate = ord.deliveryDate;
			Date completedDate = ord.completedDate;
			String route = ord.route;
			String routeName = ord.routeName;
			String completedMsisdn = ord.completedMsisdn;
			String docType = ord.docType;
			String completedComment = ord.completedComment;
			String extDoc = ord.extDoc;
			String partner = ord.partner;
			String routeToNext = ord.routeToNext;
		    String extId = ord.extId;
		    String driver = ord.driver;

		    String car = ord.car;
		    String trailer = ord.trailer;
		    int carsec1 = ord.carsec1;
		    int carsec2 = ord.carsec2;
		    int carsec3 = ord.carsec3;
		    int trasec1 = ord.trasec1;
		    int trasec2 = ord.trasec2;
		    int trasec3 = ord.trasec3;
		    String color = ord.colorStatus;
			  
			if(extDoc == null){
				extDoc = Calendar.getInstance().getTimeInMillis() + "";
			}
			
			 String add = null;		
try{
	
	    if(origid != null && !origid.equals("-1") ){
	    	add =  "parent: {id: " + origid  + "} " ;
	    }
	    
	    String ees = "";
	    if(partner != null) {
	    	ees =  partner+ "_";
	    }
	   
	    
	    if(driver != null && !driver.equals("-1") ){
	    	if(add != null){
	    		add = add + ", driver: {code: \""+ driver + "\"}";
	    	}else{
	    		add = " driver: {code: \""+ driver + "\"}";
	    	}
	    }
	    
	    if(car != null && !car.equals("-1") ){
	    	if(add != null){
	    		add = add + ", car: {code: \""+ car + "\" , carsec1: " + carsec1 + " , carsec2: " + carsec2 + " , carsec3: " + carsec3 + "}";
	    	}else{
	    		add = ", car: {code: \""+ car + "\" , carsec1: " + carsec1 + " , carsec2: " + carsec2 + " , carsec3: " + carsec3 + "}";
	    	}
	    }
	    
	    
	    if(trailer != null && !trailer.equals("-1") ){
	    	if(add != null){
	    		add = add + ", trailer: {code: \""+ trailer + "\" , trasec1: " + trasec1 + " , trasec2: " + trasec2 + " , trasec3: " + trasec3 + "}";
	    	}else{
	    		add = ", trailer: {code: \""+ trailer + "\" , trasec1: " + trasec1 + " , trasec2: " + trasec2 + " , trasec3: " + trasec3 + "}";
	    	}
	    }
	    
	    if(podid != null){
	    		
	    	Customer ag = (Customer) custs.get(podid);	
	    
	    	if(ag != null) {
		    		JSONObject value = new JSONObject();
	    			value.put("code", ag.getID());
	    			value.put("name", deCode(ag.getName()));
	    			value.put("extCode", (ag.getID()));
	    			
	    			
	    			if(ag.restPerson != null){
	    				value.put("person", deCode(ag.restPerson));
	    			}
	    			
	    			
	    			if(ag.restZipCode != null){
	    				value.put("zipcode", ag.restZipCode);
	    			}
	    			
	    			if(color != null) {
	    				value.put("color", color);
	    			}
	    			
	    			if(ag.restStreet != null){
	    				value.put("street", ag.restStreet);
	    			}
	    			
	    			if(ag.section != null){
	    				value.put("section", ag.section);
	    			}
	    			

	    			if(ag.restPerPos != null){
	    				value.put("perposition", ag.restPerPos);
	    			}
	    			
	    			if(ag.restCity != null){
	    				value.put("city", ag.restCity);
	    			}

	    			if(ag.restPerPhone != null){
	    				value.put("perphone", ag.restPerPhone);
	    			}
	    			
	    			
	    			if(ag.restPerEmail != null){
	    				value.put("peremail", ag.restPerEmail);
	    			}
	    			
	    			
	    			if(ag.getCountry() != null){
	    				value.put("country", ag.getCountry());
	    			}
	    			
	    			
	    			if(ag.restRoute != null){
	    		       JSONArray arrayx = new JSONArray();
	    		       for(int i = 0;i< ag.restRoute.size();i++){
	    		    	   JSONObject valuex = new JSONObject();
	    		   		valuex.put("code", ag.restRoute.elementAt(i));
	    		   		arrayx.put(valuex);
	    		   	   }
	    				value.put("routes", arrayx);	
	    			}
	    	
	    	
	    	
	    	
	    	if(add != null){
	    		add = add + ", pod: " + value.toString();
	    	}else{
	    		add = " pod: " + value.toString();
	    	}

	    	System.out.println("podid  " + add );
	    	
	} else {
		System.out.println("podid not on treemap " + podid + " " + custs.size() +  " " + custs.get(podid));
	}
	    }
	    
	    if(partner != null){
	    	if(add != null){
	    		add = add + ", partner: {code: \""+ partner + "\"}";
	    	}else{
	    		add = " partner: {code: \""+ partner + "\"}";
	    	}
	    	
	    }
	    
	   
	    
	    if(priority != null){
	    	if(add != null){
	    		add = add + ", priority: \""+ priority + "\"";
	    	}else{
	    		add =  " priority: \""+ priority + "\"";	
	    	}
	    	
	    }
	    
	    
	    
	    if(completedDate != null){
	    	if(add != null){
	    		add = add + ", completedDate: " + completedDate.getTime() ;	
	    	}else{
	    		add = " completedDate: " + completedDate.getTime() ;
	    	}
	    	
	    }
	    
	    if(deliveryDate != null){
	    	add = add + ", deliveryDate: " + deliveryDate.getTime();
	    }
	   
	    
	    if(id != null){
	    	add = add + ", id: " + id ;
	    }
	    if(extId != null){
	    	add = add + ", extID: \"" + extId + "\"";
	    }
	    
	    if(route != null){
	    	add = add + ", cargoRoute: {code: \"" + ees + route + "\", name: \"" +  routeName + "\"}";
	    }
	   
	    if(completedComment != null){
	    	add = add + ", completedComment: \""+ completedComment + "\"";
	    }
	    
	    if(extDoc != null){
	    	add = add + ", extDocNum: \""+ extDoc + "\"";
	    }
	   
	    if(docType != null){
	    	add = add + ", docType: \""+ docType + "\"";
	    }

	    if(completedMsisdn != null){
	    	add = add + ", completedMsisdn: \""+ completedMsisdn + "\"";
	    }
	    
	    if(routeToNext != null){
	    	add = add + ", geoRoute: \""+ routeToNext + "\"";
	   
	    }
	    
	    if(ord.dend != null) {
	    	add = add + ", dayEnd: \""+ ord.dend + "\"";
	    }
	    if(ord.dstart != null) {
	    	add = add + ", dayStart: \""+ ord.dstart + "\"";
	    }
	    if(ord.duration != null) {
	    	add = add + ", dayDuration: \""+ ord.duration + "\"";
	    }
	    if(ord.wplanned > 0 ) {
	    	add = add + ", weightPlanned: "+ ord.wplanned + "";
	    };
	     
	    for(int i = 0;i< ord.prodLines.size(); i++){
	    	Product tt = (Product)  ord.prodLines.elementAt(i);
	    	String ean = "";
	    	if(tt.getEAN() != null && tt.getEAN().trim().length() > 0){
	    		ean = " , packID: "+ tt.getEAN(); 
	    	}
	    	if(i != 0){
	    		add = add + ", "; 
	    	}else{
	    		  add = add + ", lines: [";
	    	}
	    	add = add + "{ pack: {code: \"" + tt.getID() + "\"} , amount: " + tt.getInCases() + ean +"}";
	    	if(i == ord.prodLines.size()-1){
	    		 add = add + " ]";
	    	}
	    }
	    
	    
	    add = "{" + add + "}";


    	
    	 
	    
	//JSONObject.wrap(add);
}catch(Exception e){
	e.printStackTrace();
}
	if( add.length() > 0 ){
		try {
			array.put(new JSONObject(add));
		} catch (JSONException e) {
		System.out.println(add);	
			e.printStackTrace();
		}
	}
	
	
	   cou ++;
		}

	
		System.out.println("MYJSON SHIPIT  " );
		
		try {
			Converter.saveFileInUTF8("json" + System.getProperty("file.separator") 
			+ "docsshipit.json" , array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public static int getDifferenceDays(Date d1, Date d2) {
	    int daysdiff = 0;
	    long diff = d2.getTime() - d1.getTime();
	    long diffDays = diff / (24 * 60 * 60 * 1000) + 1;
	    daysdiff = (int) diffDays;
	    return daysdiff;
	}



	public static void savePodJson(TreeMap tm ){
	
		
		Vector<MyJSON> json = new Vector<MyJSON> ();
	    JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		
		int cou = 0;
		
		while (itera.hasNext()){
			Customer ag = (Customer)itera.next();
			JSONObject value = new JSONObject();
try{
String ees = "";
	
	if(ag.restPartner != null ) {
		ees = ag.restPartner + "_" ;
	}
	value.put("code", ees + ag.getID());
	value.put("name", deCode(ag.getName()));
	value.put("extCode", (ag.getID()));
	if(ag.restPartner != null) {
	JSONObject partner = new JSONObject();
	partner.put("code", ag.restPartner);
	value.put("partner", partner);
	}
	
	
	
	if(ag.restPerson != null){
		value.put("person", deCode(ag.restPerson));
	}
	
	
	if(ag.restZipCode != null){
		value.put("zipcode", ag.restZipCode);
	}
	
	if(ag.restStreet != null){
		value.put("street", ag.restStreet);
	}
	

	if(ag.restPerPos != null){
		value.put("perposition", ag.restPerPos);
	}
	
	if(ag.restCity != null){
		value.put("city", ag.restCity);
	}

	if(ag.restPerPhone != null){
		value.put("perphone", ag.restPerPhone);
	}
	
	
	if(ag.restPerEmail != null){
		value.put("peremail", ag.restPerEmail);
	}
	
	if(ag.section != null){
		value.put("section", ag.section);
	}
	
	
	if(ag.getCountry() != null){
		value.put("country", ag.getCountry());
	}
	
	if(ag.restRoute != null){
       JSONArray arrayx = new JSONArray();
       for(int i = 0;i< ag.restRoute.size();i++){
    	   JSONObject valuex = new JSONObject();
   		valuex.put("code", ag.restRoute.elementAt(i));
   		arrayx.put(valuex);
   	   }
		value.put("routes", arrayx);	
	}
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
	   cou ++;
		
		}
	
		
		System.out.println("MYJSON POD jääk " + array.length() );
		
		try {
			Converter.saveFile("json" + System.getProperty("file.separator") 
			+ "pods.json" , array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	


	

	public static void sendRouteCalsJson(Vector vec , String user , String pass, String company){
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	String url = "http://merisoft.eu:8080/rest/routecal/list/";
	 
	JSONArray array = new JSONArray();
		Enumeration itera = vec.elements();
		int cou = 0;
		while (itera.hasMoreElements()){
			JSONObject elem = (JSONObject)itera.nextElement();
			array.put(elem);

	if(cou == 200){
		System.out.println("MYJSON RCals 200 " );
		
			try {
				serv.postJson(array.toString(), url);
				//System.out.println("MYJSON RCals \n "+ aaa + " \n" + array.toString() );
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		array = new JSONArray();
		cou = 0;
	   }
	   cou ++;
		}

	
		System.out.println("MYJSON RCals jääk " );
		
	
		try {
			serv.postJson(array.toString(), url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
	}
	
	
	public static String jsonDate(Date dd){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return   format.format(dd) ;	
	}

	public static void sendAssetsJson(Vector vec , String user , String pass, String company){
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	  
	
	//  "code": "as",
	 // "name": "as",
	 // "ean": "1234677",
	 // "nfcTag": "UneMati34124423DDHH",
	 // "factoryId": "345HD67383"
	String url = "http://merisoft.eu:8080/rest/countit/items/";
	 
	JSONArray array = new JSONArray();
		Enumeration itera = vec.elements();
		int cou = 0;
		while (itera.hasMoreElements()){
			JSONObject elem = (JSONObject)itera.nextElement();
			array.put(elem);

	if(cou == 300){
		System.out.println("MYJSON Assets 300 " );
		
			try {
			String	aaa =  serv.postJson(array.toString(), url);
				System.out.println("MYJSON RCals \n "+ aaa + " \n" + array.toString() );
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		array = new JSONArray();
		cou = 0;
	   }
	   cou ++;
		}

	
		System.out.println("MYJSON Assets jääk " + array.toString() );
		
	
		try {
			
			
			
		String aaa = 	serv.postJson(array.toString(), url);
			System.out.println("MYJSON ASII   \n" + aaa + "\n" + array.toString() );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
	}
	
	
	public static void sendAssetsCustomerJson(Vector vec , String user , String pass, String company){
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	String url = "http://merisoft.eu:8080/rest/countit/addItemToCustomer";
	  System.out.println("Vec size " + vec.size());
	JSONArray array = new JSONArray();
		Enumeration itera = vec.elements();
		int cou = 0;
		while (itera.hasMoreElements()){
			JSONObject elem = (JSONObject)itera.nextElement();
			array.put(elem);

	if(cou == 1000){
		System.out.println("MYJSON AssetsCustomers 1000 " );
		
			try {
				String aaa = serv.postJson(array.toString(), url);
				System.out.println("MYJSON RCals "+ aaa  );
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		array = new JSONArray();
		cou = 0;
	   }
	   cou ++;
		}

	
		System.out.println("MYJSON AssetsCustomer jääk " + array.length());
		
	
		try {
			Converter.saveFile("json" + System.getProperty("file.separator") 
			+ Calendar.getInstance().getTimeInMillis()+ ".txt" , array.toString());
		
			String aaa = serv.postJson(array.toString(), url);
			System.out.println("MYJSON RCals left  "+ aaa  + " " + array.toString() );
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
		

		
		try {
		//	Converter.saveFile("json" + System.getProperty("file.separator") 
			//+ Calendar.getInstance().getTimeInMillis()+ ".txt" , array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	

	

	
	
	public static void sendRouteCalsDelete( String user , String pass, String company){
	eevaeksport.util.ReportingService serv = new ReportingService();
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
	  
		if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	    }
		
	String url = "http://merisoft.eu:8080/rest/routecal/deleteAll";
	 

	
		System.out.println("MYJSON RCals kustutatud " );
		
	
		try {
			
			serv.postJson(null, url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
	}
	


	
	 

	public static void sendVecicleJson(TreeMap tm , String user , String pass, String company){
		Vector<MyJSON> json = new Vector<MyJSON> ();
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	String url = "http://merisoft.eu:8080/rest/vechicle/list/";
	 
	JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		int cou = 0;
		while (itera.hasNext()){
			Customer ag = (Customer)itera.next();
			JSONObject value = new JSONObject();
try{
	value.put("plate", ag.getID());
	value.put("name", ag.getName());
	value.put("company", company);

	
	if (ag.getCustType() != null){
		
			JSONObject value2 = new JSONObject();
			value2.put("code", ag.getCustType());
			value.put("type",value2);
		
	}
	
}catch(Exception e){
	e.printStackTrace();
}
	if( value.length() > 0 ){
		array.put(value);
	}
	
	if(cou == 10){
		System.out.println("MYJSON Vechicle 10 " );
		
			try {
				serv.postJson(array.toString(), url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		array = new JSONArray();
		cou = 0;
	   }
	   cou ++;
		}

	
		System.out.println("MYJSON Vechicle jääk " );
		
	
		try {
			serv.postJson(array.toString(), url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
	}
	

	
	 
	
	
	public static void sendAgentJson(TreeMap tm , String company, boolean merc){
		Vector<MyJSON> json = new Vector<MyJSON> ();
		
	  if(company == null){
		  System.out.println("add companycode to export.ini");
		  return;
	  }
		MyJSON myson = new MyJSON();
		myson.setUrl("http://merisoft.ee:8080/rest/managers/");
	  JSONArray array = new JSONArray();
		Iterator itera = tm.values().iterator();
		while (itera.hasNext()){
			Agent ag = (Agent)itera.next();
			JSONObject value = new JSONObject();
			String add = "";
			if(merc)
				add = "m-";
				
try{
	value.put("code", add + ag.getID());
	value.put("name", ag.getName());
	value.put("company", company);
	
	if(ag.getGroup()!= null){
		value.put("groupcode", ag.getGroup());
	}
	
	if(ag.getEmail()!= null){
		value.put("email", ag.getEmail());
	}
	
	if(ag.getPhone()!= null){
		value.put("phone", ag.getPhone());
	}	

	if(ag.getPassWord()!= null){
		value.put("passWord", ag.getPassWord());
	}
	
	if(ag.getLanguage() != null){
		value.put("language", ag.getLanguage());
	}
	
	if(ag.getCountry() != null){
		value.put("country", ag.getCountry());
	}
	
	if(ag.isActive()){
		value.put("active", true);
	}else{
		value.put("active", false);
	}
	
	
}catch(Exception e){
	e.printStackTrace();
}
	if(ag.isActive() && value.length() > 0 ){
		array.put(value);
	}
				
		}
		
	

		myson.setJson(deCode(array.toString()));
		json.add(myson);
//		System.out.println("MYJSON " + array.toString());
		
		if(json.size() > 0){
			setSendRestMsg(json);
		}
		
	}
	

	public static void sendSalesJson(ArrayList vec, String company, String user , String pass){
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	String url = "http://merisoft.eu:8080/rest/dashboard/salesData";
	

	JSONArray array = new JSONArray();
		Iterator itera = vec.iterator();
		int cou = 0;
		while (itera.hasNext()){
			JSONObject elem = (JSONObject)itera.next();
			array.put(elem);

	if(cou == 100){
		System.out.println("MYJSON Sales 100 " );
		
			try {
				String asi = serv.postJson(array.toString(), url);
				System.out.println("MYJSON Sales  \n "+ asi  );
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		array = new JSONArray();
		cou = 0;
	   }
	   cou ++;
		}

		System.out.println("MYJSON Sales jääk " );
		
		try {
			serv.postJson(array.toString(), url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		serv.closeClient();
		
	}

	public static void deleteSalesJsonFrom(Calendar cal , String company, String user , String pass){
		eevaeksport.util.ReportingService serv = new ReportingService();
		   
		serv.user =  user;
		serv.pass = pass;
		serv.company = company;
		
	  if(company == null){
		  System.out.println("Add companycode to export.ini for Resting");
		  return;
	  }
		
	String url = "http://merisoft.eu:8080/rest/dashboard/salesData&delete?" + cal.getTimeInMillis();
	
				String asi = "";
				try {
					asi = serv.postJson(null, url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("MYJSON Delete Salesdata  \n "+ asi  );
		
		serv.closeClient();
		
	}
	

	
	  public static void sendJsonShipitUserSingle(String user , String pass , String company ,String origid, String route, String completedMsisdn, 
			  Date completedDate, String completedComment, String docType, 
			  Vector lines, String podid, Date deliveryDate, String extDoc,String priority){
		   eevaeksport.util.ReportingService serv = new ReportingService();
		   
			serv.user =  user;
			serv.pass = pass;
			serv.company = company;
					try {
					serv.addNewShipDoc(origid, route, completedMsisdn, completedDate, 
							 completedComment,  docType, lines, podid,deliveryDate,extDoc,priority);
					}catch(Exception r){
						r.printStackTrace();
					}
			serv.closeClient();	
	   }

	  
	  
		

		public static void sendShipitListJson(Vector vec , String user , String pass, String company){
			
			eevaeksport.util.ReportingService serv = new ReportingService();
			serv.user =  user;
			serv.pass = pass;
			serv.company = company;
	
			if(company == null){
			  System.out.println("Add companycode to export.ini for Resting");
			  return;
		  }
			
		String url = "http://merisoft.eu:8080/rest/shipit/list/";
		 
		JSONArray array = new JSONArray();
			Enumeration itera = vec.elements();
			int cou = 0;
			while (itera.hasMoreElements()){
				Order ord = (Order)itera.nextElement();
				
				String podid = ord.getCompany();
				String origid = ord.origId;
				String priority = ord.restPriority;
				Date deliveryDate = ord.deliveryDate;
				Date completedDate = ord.completedDate;
				String route = ord.route;
				String completedMsisdn = ord.completedMsisdn;
				String docType = ord.docType;
				String completedComment = ord.completedComment;
				String extDoc = ord.extDoc;
				String partner = ord.partner;
				String routeToNext = ord.routeToNext;
			    String extId = ord.extId;
				
				if(extDoc == null){
					extDoc = Calendar.getInstance().getTimeInMillis() + "";
				}
				
				 String add = null;		
	try{
		
		    if(origid != null && !origid.equals("-1") ){
		    	add =  "parent: {id: " + origid  + "} " ;
		    }
		    
		    if(podid != null){
		    	if(add != null){
		    		add = add + ", pod: {code: \""+ partner+ "_" + podid + "\"}";
		    	}else{
		    		add = " pod: {code: \""+ partner +"_" + podid + "\"}";
		    	}
		    	
		    }
		    
		    if(partner != null){
		    	if(add != null){
		    		add = add + ", partner: {code: \""+ partner + "\"}";
		    	}else{
		    		add = " partner: {code: \""+ partner + "\"}";
		    	}
		    	
		    }
		    
		   
		    
		    if(priority != null){
		    	if(add != null){
		    		add = add + ", priority: \""+ priority + "\"";
		    	}else{
		    		add =  " priority: \""+ priority + "\"";	
		    	}
		    	
		    }
		    
		    
		    
		    if(completedDate != null){
		    	if(add != null){
		    		add = add + ", completedDate: " + completedDate.getTime() ;	
		    	}else{
		    		add = " completedDate: " + completedDate.getTime() ;
		    	}
		    	
		    }
		    
		    if(deliveryDate != null){
		    	add = add + ", deliveryDate: " + deliveryDate.getTime();
		    }
		   
		    if(extId != null){
		    	add = add + ", extID: \"" + extId + "\"";
		    }
		    
		    if(route != null){
		    	add = add + ", cargoRoute: {code: \"" + partner + "_" + route + "\"}";
		    }
		   
		    if(completedComment != null){
		    	add = add + ", completedComment: \""+ completedComment + "\"";
		    }
		    
		    if(extDoc != null){
		    	add = add + ", extDocNum: \""+ extDoc + "\"";
		    }
		   
		    if(docType != null){
		    	add = add + ", docType: \""+ docType + "\"";
		    }

		    if(completedMsisdn != null){
		    	add = add + ", completedMsisdn: \""+ completedMsisdn + "\"";
		    }
		    
		    if(routeToNext != null){
		    	add = add + ", geoRoute: \""+ routeToNext + "\"";
		   
		    }
		     
		    for(int i = 0;i< ord.prodLines.size(); i++){
		    	Product tt = (Product)  ord.prodLines.elementAt(i);
		    	String ean = "";
		    	if(tt.getEAN() != null && tt.getEAN().trim().length() > 0){
		    		ean = " , packID: "+ tt.getEAN(); 
		    	}
		    	if(i != 0){
		    		add = add + ", "; 
		    	}else{
		    		  add = add + ", lines: [";
		    	}
		    	add = add + "{ pack: {code: \"" + tt.getID() + "\"} , amount: " + tt.getInCases() + ean +"}";
		    	if(i == ord.prodLines.size()-1){
		    		 add = add + " ]";
		    	}
		    }
		    
		    
		    add = "{" + add + "}";


	    	
	    	 
		    
		//JSONObject.wrap(add);
	}catch(Exception e){
		e.printStackTrace();
	}
		if( add.length() > 0 ){
			try {
				array.put(new JSONObject(add));
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
		}
		
		if(cou == 100){
			
				try {
					String aaa = serv.postJson(array.toString(), url);
 //System.out.println("aaa " + aaa);
					Converter.saveFile("json" + System.getProperty("file.separator") 
					+ Calendar.getInstance().getTimeInMillis()+ ".txt" , array.toString());
						
				} catch (Exception e) {
					e.printStackTrace();
				}
				//serv.closeClient();
				//System.exit(0);
				
			array = new JSONArray();
			cou = 0;
		   }
		   cou ++;
			}

		
			System.out.println("MYJSON SHIPIT jääk " );
			
		
			try {
			
				String aaa = serv.postJson(array.toString(), url);
 System.out.println("aaa " + aaa);
 Converter.saveFile("json" + System.getProperty("file.separator") 
	+ Calendar.getInstance().getTimeInMillis()+ ".txt" , array.toString());
			          
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			serv.closeClient();
			
		}
		

	  
	
	
	public static void setSendRestMsg(Vector<MyJSON> json ){
		
		CloseableHttpClient  client = HttpClients.createDefault();
		
		
		RequestConfig defaultRequestConfig = RequestConfig.custom()
		        .setConnectTimeout(10000)
		        .setSocketTimeout(10000)
		        .setConnectionRequestTimeout(10000)
		        .build();
		
	          Vector<MyJSON> oma = (Vector<MyJSON>)json.clone();
	          
	          HttpResponse response = null;
	          Vector<Integer> sent = new Vector<Integer>();
	      for (int u = 0 ; u < oma.size(); u++){
	      	MyJSON asi = oma.elementAt(u);
	      	if(!asi.isSendfile()){ //paneme baasi info 
	      	   try {
	                 HttpPost post = new HttpPost(asi.getUrl());
	              //   post.setConfig(defaultRequestConfig);
	                 StringEntity se = new StringEntity( asi.getJson(),"UTF-8");  
	                 se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	               
	                 post.setEntity(se);
	                 response = client.execute(post);
	         
	                 if(response!=null){
	                     HttpEntity responseEntity = response.getEntity();
	                     if(responseEntity!=null) {
	                     	String asoi = EntityUtils.toString(responseEntity);
	                     	
	                     	 if(asoi.indexOf("Error") != -1){
	                     		 System.out.println("Postimise tulemus vigane teade " + asoi + asi.getJson());
	                     		saveFileInUTF8("error.txt", asi.getJson() );
	                     		 if(asoi.indexOf("Error 409") != -1){
	                     			sent.add(u);
	                        		 System.out.println("Postimise tulemus OK " + asoi  ); 
	                     		 }
	                     		 
	                     	 }else{
	                     		sent.add(u);
	                     		 System.out.println("Postimise tulemus OK " + asoi  );
	                     	 }
	                    }
	                 }

		             //Salvestame faili 
	                 
	             } catch(Exception e) {
	                 System.out.println("Postimine täitsa vigane " + asi.getJson());
	                 
	                 e.printStackTrace();
	             }
	             
	             
	      	  }else{ // laeme faili ylesse foto 
	      		  
	      		try {
	      		  
	                HttpPost post = new HttpPost(asi.getUrl());
	                System.out.println("pildi saatmine " + asi.getJson());
	                File file = new File(asi.getJson());
	                if(file.exists()){
	                	FileBody bin = new FileBody(file);
	                    StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);

	                    HttpEntity reqEntity = MultipartEntityBuilder.create()
	                            .addPart("file", bin)
	                            .addPart("comment", comment)
	                            .build();

	                    post.addHeader("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\"");
	                    post.setEntity(reqEntity);

	                    System.out.println("executing request " + post.getRequestLine());
	                    response = client.execute(post);
	                    
	                } 
	                
	                
	                if(response!=null){
	                    HttpEntity responseEntity = response.getEntity();
	                    if(responseEntity!=null) {
	                    	String asoi = EntityUtils.toString(responseEntity);
	                    	 if(asoi.indexOf("Error") != -1){
	                    		 System.out.println("Faili Postimise tulemus vigane teade " + asoi);
	                    		 if(asoi.indexOf("Error 409") != -1){
	                    			 
	                    			 // topelt rea puhul võtame stmisest maha 
	                    			 sent.add(u);
	                    		 }
	                    	 }else{
	                    		sent.add(u);
	                    		 System.out.println(" Falil Postimise tulemus OK " + asoi );
	                    	 }
	                   }
	                }
	      			
client.close();


	            } catch(Exception e) {
	                System.out.println("Postimine täitsa vigane ");
	                e.printStackTrace();
	            } 
	      	  }
	   	   }
	   }

	
	  
	
	
	static public void trustAllHttpsURLConnection(){
		X509TrustManager tm = new X509TrustManager() {
	        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        }

	        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        }

	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	    };


	try {
	SSLContext sc = SSLContext.getInstance("SSL");
	sc.init(null, new TrustManager[] { tm }, new java.security.SecureRandom());
	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	} catch (Exception e) {
	}
		
	}

	
	
	 public static void doTrustToCertificates() throws Exception {
	        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	        TrustManager[] trustAllCerts = new TrustManager[]{
	                new X509TrustManager() {
	                    public X509Certificate[] getAcceptedIssuers() {
	                        return null;
	                    }

	                

						@Override
						public void checkClientTrusted(X509Certificate[] arg0,
								String arg1) throws CertificateException {
							return;
							
						}

						@Override
						public void checkServerTrusted(X509Certificate[] arg0,
								String arg1) throws CertificateException {
							// TODO Auto-generated method stub
							return;
						}
	                }
	        };

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        HostnameVerifier hv = new HostnameVerifier() {
	          

				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					    return true;
				}
	        };
	        HttpsURLConnection.setDefaultHostnameVerifier(hv);
	    }
	

	  public static void shrinkAndCopyFile150 (File in, File out) throws Exception {
		  shrinkAndCopyFile150 (in.getAbsolutePath(), out.getAbsolutePath());
	  }
	 
	  public static void shrinkAndCopyFile (File in, File out, int size) throws Exception {
		  shrinkAndCopyFile (in.getAbsolutePath(), out.getAbsolutePath(),size);
	  }
	  
	  public static void shrinkAndCopyFile (File in, File out) throws Exception {
		  shrinkAndCopyFile (in.getAbsolutePath(), out.getAbsolutePath(),0);
	  }
	  public static float Round(float Rval, int Rpl) {
		   float p = (float)Math.pow(10,Rpl);
		   Rval = Rval * p;
		   float tmp = Math.round(Rval);
		   return (float)tmp/p;
		   }
		 


	  
	  
	  public static void prepareDocCatalogue(String inP, String outP, HashSet codes, boolean deleteOld ) {
		StringBuffer sb = new StringBuffer();
		   Vector docs = new Vector();
		   
		  
		    File d  = new File(inP);
			if(d.isDirectory()){
	        File[] dd = d.listFiles();
	        Vector cats = new Vector();
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(t.isDirectory()){
	        		cats.add(t.getName());
	        	}
	        }
	       // System.out.println("Docide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
	     	  
	        //vaatame kas katalogides on 
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(!t.isDirectory()){
	        		docs.add(t.getName());
		        }else{
		        	File[] ddd = t.listFiles();
		        	for(int u = 0; u < ddd.length ; u++){
		        		File f = ddd[u];
		        		docs.add(t.getName() + "/" + f.getName());
		        	}
		        }
	        	}
	        }
	        
	        //otsime välja aktuaalsed dokumendid
	        Vector aktual = new Vector();
	       
	  	//  System.out.println("TEEN DOC " + codes.size());
	  	  
	        	Enumeration enume2 = docs.elements();
	        	 while (enume2.hasMoreElements()){
	        		 String fnimi = (String) enume2.nextElement();
	        	//	 System.out.println("fnimi " + fnimi);
	        		try{
	        			
	        			 String vord = fnimi.substring(1,fnimi.lastIndexOf("_"));
	        			 if(vord.contains("/")){
	        				 vord = vord.substring(vord.indexOf("/")+2);
	        			 }
	        		//	 System.out.println("vord " + vord + " " + codes.contains(vord) );
		        		 if(codes.contains(vord)){
		        			 aktual.add(fnimi);
		        			
		        			// System.out.println("TEEN " + fnimi);
		        		 }else if(vord.contains("_")){
		        			 vord = vord.substring(0,vord.indexOf("_"));
		        			 if(codes.contains(vord)){
			        			 aktual.add(fnimi); 
			        			// System.out.println("TEEN2 " + fnimi);
		        			 }
		        		 }
		        		 
		        		 
		        	}catch (Exception k ){
	        		}
	        	 }
	        		
	        //kustutame maha vanad dokumendid 
	      	if(deleteOld){
	      		
	      	 File res2 = new File (outP);
              String[] ff2 = res2.list();
          	 for(int i = 0;ff2.length>i;i++){
              	(new File ( outP + ff2[i])).delete(); 
             }
	  }
	        // failide kopperimine ja nime muutmine 
	        
	        Enumeration enumw = aktual.elements();
	        while(enumw.hasMoreElements()){
	        	String nim = (String)enumw.nextElement();
	        	String outname = Converter.cleanLitLatEst(nim);
	        	int ind = nim.lastIndexOf("/");
	        	if(ind !=-1){
	        	 outname = Converter.cleanLitLatEst(nim.substring(ind +1));
	        	}
	        	
	        	File aa = new File(inP + nim);
	        	if(aa.exists()){
	        		if(sb.indexOf(outname + ";" +aa.length()) == -1){
	        			sb.append(outname + ";" +aa.length() + "\n");	
	        		}
	        		
	        	}
	        	try{
	        		
	        		Converter.copyFile(aa, new File(outP + outname));
	        	}catch(Exception r ){
	        		r.printStackTrace();
	        	}
	        
	        }
          	 
          	 
	        File f = new File(outP+"listdoc.txt");
           if(deleteOld){
        	   if (f.exists())f.delete();
           }
	        
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 outP + "listdoc.txt", true));
        		os.write(sb.toString());
        		os.close();
        		
        	  }catch (Exception ex) {
        		ex.printStackTrace();
        	  }
	        
		  
		   }
		 

	  
		public void writeConsloStr(String path,String e) {
			try {
				PrintWriter os = new PrintWriter(new FileWriter(
						path, true));
				os.write(e);
				os.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}

	}
	  
	  
		  public static void shrinkAndCopyFile150 (String in, String out) throws Exception {
			   if(in != null && in.toLowerCase().indexOf("thrumbs") == -1){
				   try {
			       	BufferedImage inb = ImageIO.read(new File(in));
			       	if(inb != null){

			           	BufferedImage newImage = new BufferedImage(inb.getWidth(), inb.getHeight(), BufferedImage.TYPE_INT_RGB);
			           	Graphics2D g = newImage.createGraphics();
			           	g.drawImage(inb, 0, 0, null);
			           	g.dispose();
			           	inb.flush();
			           	 File outputfile = new File(out);
			           	ImageIO.setUseCache(false);
			           	 ImageIO.write(newImage, "jpg", outputfile);
			           	 Image bb =  ImageLoader.fromFile(out);
			           	if(bb.getWidth() > 151){
			         		//System.out.println("Muudan suurust " + bb.getWidth() + " " + in);
			          		 bb.getResizedToWidth(150).writeToJPG(new File(out), 0.8f);//.soften(0.95f)
			           	
			           	}
			            bb.dispose();
			              }	
			       	  
			       	} catch (Exception ioe) {
			     
			       	copyFile( new File(in), new File(out));
			       }
			   }
			            }
		
		
		  
		  public static void shrinkAndCopyFile (String in, String out  ) throws Exception {
			  shrinkAndCopyFile (in, out , 0 );
		  }
			  
		  
	  public static void shrinkAndCopyFile (String in, String out , int size ) throws Exception {
   if(in != null && in.toLowerCase().indexOf("thrumbs") == -1){
	   if(size == 0){
		   size = 301;
	   }   try {
		   
	   BufferedImage inb = null;
	   try {
		  inb =  ImageIO.read(new File(in));
	   }catch (Exception e){
		   InputStream ins = new FileInputStream(in);
		   try{
			     inb =  JPEGCodec.createJPEGDecoder(ins).decodeAsBufferedImage(); 
				   	   
		   }catch(Exception r){
			   System.out.println("error " + in);
		   }
	   }
		
       	 
        
       	if(inb != null){

           	BufferedImage newImage = new BufferedImage(inb.getWidth(), inb.getHeight(), BufferedImage.TYPE_INT_RGB);
           	Graphics2D g = newImage.createGraphics();
           	g.drawImage(inb, 0, 0, null);
           	g.dispose();
           	inb.flush();
           	 File outputfile = new File(out);
           	 ImageIO.setUseCache(false);
           	 ImageIO.write(newImage, "jpg", outputfile);
           	 
           	 
           	 Image bb =  ImageLoader.fromFile(out);
           	 
           	 boolean portrait  = false ;
           	 if(bb.getWidth() < bb.getHeight()) {
           		 portrait = true ;
           		 double ss = (size /3 *2);
           		 size = (int)ss;
           	 }
           	 
           	
           	 
           	if(bb.getWidth() > size){
         		System.out.println("Muudan suurust " + bb.getWidth() + " " + in);
         		try{
         			
         		    bb.getResizedToWidth(size).writeToJPG(new File(out), 0.8f);//.soften(0.95f)
                   			
         		} catch (Exception ioe) {
         		    ioe.printStackTrace();
              			
         		}
         	 
           	}
            bb.dispose();
              }	else{
            		copyFile( new File(in), new File(out)); 
              }
       	  
       	} catch (Exception ioe) {
        ioe.printStackTrace();
       	copyFile( new File(in), new File(out));
       }
   }
            }
	
	  public static void copyFile(File in, File out) throws Exception {
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
	  
	  
    public  static String firstToUpperCase(String aaa){
   	 if(aaa == null || aaa.equals("")){
   		 return aaa;
   	 }
   	 return aaa.substring(0,1).toUpperCase() + aaa.substring(1).toLowerCase();
    }
    
    
    public static String getVisitDayLetter(String input){
		
		  input = input.toUpperCase();
		  input = input.replace('E', 'R');
		  input = input.replace('A', 'E');
		  input = input.replace('B', 'T');
		  input = input.replace('C', 'K');
		  input = input.replace('D', 'N');		  
		  input = input.replace('F', 'L');
		  input = input.replace('G', 'P');
		  boolean move = true;
		if(input.indexOf("32") == -1 ){
			if(input.endsWith("2")){
				input = input.replace('2', '1');
				move =false;
			}
		}
		 
		if(input.indexOf("16") == -1 && move){
			if(input.endsWith("1")){
				input = input.replace('1', '2');		
			}
		}
		  
		 
		  return input ;
	  }

    public static String visitTurn(String input) {
    	 String res = "";
    	 
    	 if(input.equals("E1")) {
    		 res= "E2";
    	 }
    	 if(input.equals("T1")) {
    		 res= "T2";
    	 }if(input.equals("K1")) {
    		 res= "K2";
    	 }if(input.equals("N1")) {
    		 res= "N2";
    	 }if(input.equals("R1")) {
    		 res= "R2";
    	 }if(input.equals("L1")) {
    		 res= "L2";
    	 }if(input.equals("P1")) {
    		 res= "P2";}
    		 
    		 
    		 if(input.equals("E2")) {
        		 res= "E1";
        	 }
        	 if(input.equals("T2")) {
        		 res= "T1";
        	 }if(input.equals("K2")) {
        		 res= "K1";
        	 }if(input.equals("N2")) {
        		 res= "N1";
        	 }if(input.equals("R2")) {
        		 res= "R1";
        	 }if(input.equals("L2")) {
        		 res= "L1";
        	 }if(input.equals("P2")) {
        		 res= "P1";}
    	 
    	 
    	 return res ;
    }
	
	 public static String getVisitDay(String input){
		  String res = "";
		  
		  for (int i = 0 ; i < input.length(); i++ ){
			char a = input.charAt(i);
			if(a == 'A'){
				res = res + "E";
			}
			if(a == 'B'){
				res = res + "T";
			}
			if(a == 'C'){
				res = res + "K";
			}
			if(a == 'D'){
				res = res + "N";
			}
			if(a == 'E'){
				res = res + "R";
			}
			if(a == 'F'){
				res = res + "L";
			}
			if(a == 'G'){
				res = res + "P";
			}
			if(a == '1'){
				res = res + "2";
			}if(a == '2'){
				res = res + "1";
			}if(a == '3'){
				res = res + "4";
			}if(a == '4'){
				res = res + "8";
			}if(a == '5'){
				res = res + "16";
			}if(a == '6'){
				res = res + "32";
			}
		  }
		  
		  if(res.length() == 0){
			  res = " ";
		  }
		  return res ;
	  }


	 

	  public static String getYYYYMMString(int add){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal = Calendar.getInstance();
			   cal.add(Calendar.MONTH, add);
			   int year = cal.get(Calendar.YEAR);
			   String month = (cal.get(Calendar.MONTH) + 1) + "";
			   if(month.length() == 1){
				   month = "0" + month ;
			   }
			   sb.append(year + "" + month) ;
		
			
		    return sb.toString();
		  }
	 
	  public static String getYYYY_MM_DD_String(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day );
			
			sb.append("-" + month  + "-");
			sb.append("" + cal.get(Calendar.YEAR));
			
		    return sb.toString();
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
	  
	  public static String getYYYYMMString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    sb.append("" + cal.get(Calendar.YEAR));
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
		
			
			sb.append("" + month  );
		    return sb.toString();
		  }
	  
	  public static String getYYYYmMMmDDmString(Date dd){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    cal.setTime(dd);
		    sb.append("" + cal.get(Calendar.YEAR));
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
		
			
			sb.append("-" + month  + "-");
			
			sb.append(day );
		    return sb.toString();
		  }
	  
	  public static String getDDpMMpYYYYpString(Calendar cc){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    if(cc != null)
		    cal.setTime(cc.getTime());
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day );
			
			sb.append("." + month  + ".");
			sb.append("" + cal.get(Calendar.YEAR));
			
		    return sb.toString();
		  }
	  
	  public static String getDDsMMMString(Date dd){
	      String pattern = "dd MMM";
	      SimpleDateFormat simpleDateFormat =
	              new SimpleDateFormat(pattern);

	      return simpleDateFormat.format(dd);	  
	  }
	  
	  public static String getDDsMMMsYYYYString(Date dd){
	      String pattern = "dd MMM yy";
	      SimpleDateFormat simpleDateFormat =
	              new SimpleDateFormat(pattern);
	      return simpleDateFormat.format(dd);	  
	  }
	  

	  
	  public static String getYYYY_MM_DDpString(Calendar cc){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    if(cc != null)
		    cal.setTime(cc.getTime());
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append("" + cal.get(Calendar.YEAR));
			sb.append("-" + month  + "-");
			sb.append(day );
	
			
		    return sb.toString();
		  }
	  
	  private String convTyyp(String tyyp){
		if(tyyp.trim().equals("LIST")){
    		tyyp = "16";
    	}
    	
		if(tyyp.trim().equals("NUM")){
    		tyyp = "3";
    	}
    	
    	if(tyyp.trim().equals("TEXT")){
    		tyyp = "12";
    	} 
    	
    	if(tyyp.trim().equals("YNO")){
    		tyyp = "5";
    	}
    	return tyyp;
	}
	
	public void addQuestiosToCustomer(Customer customer,HashMap kyssad){
	//kõikidele klientidele kui on vaja panna
		if(kyssad != null && kyssad.size() > 0){
			
		if(kyssad.get("ALL") != null ){
	      	 Vector vv = (Vector)kyssad.get("ALL");
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 if(olemas.trim().length() > 0){
	      			 if( olemas.indexOf(asi) == -1){
	      				 customer.setQueryIDs(olemas + "|" + asi);	 
	      			 }
	      			
	      		 }else{
	      			 customer.setQueryIDs(asi); 
	      		 }
	      		
	      	 }
	       }
		//Kliendi koodiga kas on pandud 
		 if(kyssad.get(customer.getID()) != null ){
	      	 Vector vv = (Vector)kyssad.get(customer.getID().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 if(olemas.trim().length() > 0 ){
	      			 if( olemas.indexOf(asi) == -1){
	      				 customer.setQueryIDs(olemas + "|" + asi);	 
	      			 }
	      		 }else{
	      			 customer.setQueryIDs(asi); 
	      		 }      		
	      	 }
	      }
		 
		 // maksjale paneme kysimused kylge 
		 if(kyssad.get(customer.getPayer()) != null ){
	      	 Vector vv = (Vector)kyssad.get(customer.getPayer().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 if(olemas.trim().length() > 0 ){
	      			 if( olemas.indexOf(asi) == -1){
	      				 customer.setQueryIDs(olemas + "|" + asi);	 
	      			 }
	      		    }else{
	      			     customer.setQueryIDs(asi); 
	      		    }      		
	      	   }
	      }
		 
		 // tõmbame negatiivsed maha 
		 if(kyssad.get("*" + customer.getID()) != null ){
	      	 Vector vv = (Vector)kyssad.get("*"+ customer.getID().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 
	      		 if(olemas.trim().length() > 0 ){      			 
	      			 if(asi.equals(olemas)){
		      				customer.setQueryIDs(" ");	
		      			 }else  if( olemas.indexOf(asi) != -1){
	      				 String panna = olemas.substring(0,olemas.indexOf(asi)) + olemas.substring(olemas.indexOf(asi) + asi.length() +1);
	      				 if(panna.length()>2){
	      					 customer.setQueryIDs(panna);	
	      				 }else{
	      					 customer.setQueryIDs(" ");	
	      				  }
	      			  }
	      		  }     		
	      	  }
	      }
		 
		 if(kyssad.get("*" + customer.getPayer()) != null ){
	      	 Vector vv = (Vector)kyssad.get("*"+ customer.getPayer().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 
	      		 if(olemas.trim().length() > 0 ){
	      			 if(asi.equals(olemas)){
		      				customer.setQueryIDs(" ");	
		      			 }else 
	      			 if( olemas.indexOf(asi) != -1){
	      				 String panna = olemas.substring(0,olemas.indexOf(asi)) + olemas.substring(olemas.indexOf(asi) + asi.length() +1);
	      				 if(panna.length()>2){
	      					 customer.setQueryIDs(panna);	
	      				 }else{
	      					customer.setQueryIDs(" ");	
	      				 }
	      				 
	      			 }
	      		 }     		
	      	 }
	      }
		

		 
		 // ankeedi tyybile kylge 
		 
		 if(kyssad.get(customer.getAnkeediTyyp()) != null ){
	      	 Vector vv = (Vector)kyssad.get(customer.getAnkeediTyyp().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 if(olemas.trim().length() > 0 ){
	      			 if( olemas.indexOf(asi) == -1){
	      				 customer.setQueryIDs(olemas + "|" + asi);	 
	      			 }
	      		 }else{
	      			 customer.setQueryIDs(asi); 
	      		 }      		
	      	 }
	      }
		 
		 if(kyssad.get(customer.getGroup()) != null ){
	      	 Vector vv = (Vector)kyssad.get(customer.getGroup().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 if(olemas.trim().length() > 0 ){
	      			 if( olemas.indexOf(asi) == -1){
	      				 customer.setQueryIDs(olemas + "|" + asi);	 
	      			 }
	      		 }else{
	      			 customer.setQueryIDs(asi); 
	      		 }      		
	      	 }
	      }
		 
		 if(kyssad.get(customer.getCustType()) != null ){
	      	 Vector vv = (Vector)kyssad.get(customer.getCustType().trim());
	      	 Enumeration enume = vv.elements();
	      	 while(enume.hasMoreElements()){
	      		 String asi = (String)enume.nextElement();
	      		 String olemas = customer.getQueryIDs();
	      		 if(olemas.trim().length() > 0 ){
	      			 
	      			 if( olemas.indexOf(asi) == -1){
	      				 customer.setQueryIDs(olemas + "|" + asi);	 
	      			 }
	      		 }else{
	      			 customer.setQueryIDs(asi); 
	      		 }      		
	      	 }
	      }
		 
		 
		 try{
			 if(kyssad.get("*" + customer.getAnkeediTyyp()) != null ){
		      	 Vector vv = (Vector)kyssad.get("*"+ customer.getAnkeediTyyp().trim());
		      	 Enumeration enume = vv.elements();
		      	 while(enume.hasMoreElements()){
		      		 String asi = (String)enume.nextElement();
		      		 String olemas = customer.getQueryIDs();
		      		 
		      		 if(olemas.trim().length() > 0 ){
		      	
		      			 if(asi.equals(olemas)){
		      				customer.setQueryIDs(" ");	
		      			 }else if( olemas.indexOf(asi) != -1){
		      				 String panna = olemas.substring(0,olemas.indexOf(asi)) + olemas.substring(olemas.indexOf(asi) + asi.length() +1);
		      				 if(panna.length()>2){
		      					 customer.setQueryIDs(panna);	
		      				 }else{
		      					customer.setQueryIDs(" ");	
		      				 }
		      			 }
		      			 System.out.println("olemas " + olemas + " " + customer.getName() + " " + customer.getQueryIDs());	     
		      			 
		      		 }     		
		      	 }
		      }
				 
		 }catch (Exception r){
			r.printStackTrace(); 
		 }
		 
 try{
	 

	 if(kyssad.get("*" + customer.getGroup()) != null ){
      	 Vector vv = (Vector)kyssad.get("*"+ customer.getGroup().trim());
      	 Enumeration enume = vv.elements();
      	 while(enume.hasMoreElements()){
      		 String asi = (String)enume.nextElement();
      		 String olemas = customer.getQueryIDs();
      		
   
      		 if(olemas.trim().length() > 0 ){    			 
      			 if(asi.equals(olemas)){
	      				customer.setQueryIDs(" ");	
	      			 }else if( olemas.indexOf(asi) != -1){
      				 String panna = olemas.substring(0,olemas.indexOf(asi)) + olemas.substring(olemas.indexOf(asi) + asi.length() +1);
      				 if(panna.length()>2){
      					 customer.setQueryIDs(panna);	
      				 }else{
      					customer.setQueryIDs(" ");	
      				 }
      				 
      			 }
      		 }     		
      	 }
      }
				 
		 }catch (Exception r){
			 
		 }
 
 try{
	 if(kyssad.get("*" + customer.getCustType()) != null ){
      	 Vector vv = (Vector)kyssad.get("*"+ customer.getCustType().trim());
      	 Enumeration enume = vv.elements();
      	 while(enume.hasMoreElements()){
      		 String asi = (String)enume.nextElement();
      		 String olemas = customer.getQueryIDs();
      		 
      		 if(olemas.trim().length() > 0 ){
      			 
      			 if(asi.equals(olemas)){
	      				customer.setQueryIDs(" ");	
	      			 }else  if( olemas.indexOf(asi) != -1){
      				 String panna = olemas.substring(0,olemas.indexOf(asi)) + olemas.substring(olemas.indexOf(asi) + asi.length() +1);
      				 if(panna.length()>2){
      					 customer.setQueryIDs(panna);	
      				 }else{
      					customer.setQueryIDs(" ");	
      				 }
      				 
      			 }
      		 }     		
      	 }
      }
		 
		 }catch (Exception r){
			 
		 }
		
		 
		}
	}
	public HashMap getQuestActiveIDs(String dir){
		getQuestionaire(dir);
		return questIDs;
	}
	
	private HashMap questIDs = null;
	
	public HashMap getQuestionaire(String dir){
		HashMap tree = new HashMap();
		 questIDs = new HashMap();
		
		StringBuffer buf = new StringBuffer();
		StringBuffer buf2 = new StringBuffer();
		StringBuffer buf3 = new StringBuffer();
	    
		File file = new File(dir + "Questions.xls");
		
		try {
			FileInputStream ff = new FileInputStream(file);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0); //otsime esimeselt lehelt
			HSSFRow row = null;
		     int pikkus = sheet.getLastRowNum() + 1;
		
			// Uurime millised ankeedid on aktiivsed ja koodid meelde .
			HashSet aktiivsed = new HashSet();
 
			// System.out.println("quest sees " +  pikkus + " " + sheet.getSheetName());
			for (int a = 0; a < pikkus; a++) {
				row = sheet.getRow(a);
					String asi = null;
					
					try {
					   asi = row.getCell((short)0).getStringCellValue();
					} catch (Exception e) {
						try {
							 asi = row.getCell((short)0).getNumericCellValue() +"";
							} catch (Exception d) {
						}
					}
					
					if(asi != null){
						//System.out.println("aktiivseid  " + asi );
						try{

							long datestart = (long) row.getCell((short) 2).getNumericCellValue();
							
							   long now = Calendar.getInstance().getTimeInMillis();
							  
							   GregorianCalendar gc = new GregorianCalendar(1900, Calendar.JANUARY, 1);
							   gc.add(Calendar.DATE,(int) datestart-2);
							 //  System.out.println("aktiivseid 1 " +new Date( gc.getTimeInMillis()).toGMTString() + " " + datestart );
							     if(now > gc.getTimeInMillis()){
								    long dateend = (long) row.getCell((short) 3).getNumericCellValue();
								   
									    GregorianCalendar gce = new GregorianCalendar(1900, Calendar.JANUARY, 1);
									    gce.add(Calendar.DATE,(int) dateend+2);
									 //   System.out.println("aktiivseid 2 " +new Date( gc.getTimeInMillis()).toGMTString() );
								        if(now < gce.getTimeInMillis()){
			                            	aktiivsed.add(asi);
			                            	try{
			                            		buf3.append(asi + ";" +  row.getCell((short) 1).getStringCellValue() + "\n");
			                            	}catch (Exception k){
			                            		
			                            	}
			                            
								         }   
							   }	
						}catch (Exception e){
						//e.printStackTrace();	
						}
    			   }
			}
			//System.out.println("aktiivseid ankeete " + aktiivsed.size() + " " + aktiivsed.toString() + " " + buf3.toString());
			

			HashMap periodAnkeedid = new HashMap();
			 
			
			for (int a = 0; a < pikkus; a++) {
				row = sheet.getRow(a);
					String asi = null;
					
					try {
					   asi = row.getCell((short)19).getStringCellValue();
					} catch (Exception e) {
						try {
							 asi = (int)row.getCell((short)19).getNumericCellValue() +"";
							} catch (Exception d) {
								
							}
					}
					
					if(asi != null){
						String ank = null;
						try{
							try {
								   ank = row.getCell((short)20).getStringCellValue();
								} catch (Exception e) {
									try {
										 ank = (int)row.getCell((short)20).getNumericCellValue() +"";
										} catch (Exception d) {
											
										}
								}
								
						}catch (Exception e){
							
						}
						
						if(ank != null){
							if(periodAnkeedid.get(ank) != null){
								String aa = (String)periodAnkeedid.get(ank);
								periodAnkeedid.put(ank, aa+ "|" +asi);
							}else{
								periodAnkeedid.put(ank, asi);
							}
						}
   			   }
			}
			
			
			HashMap agentAnkeedid = new HashMap();
			 
			
			for (int a = 0; a < pikkus; a++) {
				row = sheet.getRow(a);
					String asi = null;
					
					try {
					   asi = row.getCell((short)16).getStringCellValue();
					} catch (Exception e) {
						try {
							 asi = (int)row.getCell((short)16).getNumericCellValue() +"";
							} catch (Exception d) {
								
							}
					}
					
					if(asi != null){
						String ank = null;
						try{
							try {
								   ank = row.getCell((short)17).getStringCellValue();
								} catch (Exception e) {
									try {
										 ank = (int)row.getCell((short)17).getNumericCellValue() +"";
										} catch (Exception d) {
											
										}
								}
								
						}catch (Exception e){
							
						}
						
						if(ank != null){
							if(agentAnkeedid.get(ank) != null){
								String aa = (String)agentAnkeedid.get(ank);
								agentAnkeedid.put(ank, aa+ "|" +asi);
							}else{
								agentAnkeedid.put(ank, asi);
							}
						}
   			   }
			}
			
			//System.out.println("agetntide ankeete " + agentAnkeedid.size() + " " + agentAnkeedid.toString());
// loeme välja aktivsed kysimused ja paneme faili 
	HashSet listid = new HashSet();		
			for (int a = 0; a < pikkus; a++) {
				row = sheet.getRow(a);
					String asi = null;
					
					try {
					   asi = row.getCell((short)5).getStringCellValue().trim();
					} catch (Exception e) {
						try {
						 asi = row.getCell((short)5).getNumericCellValue() +"";
						} catch (Exception d) {
							
						}
					}
					
					
					if(asi != null && aktiivsed.contains(asi)){
						try{
						
							
							String kys = "";
							try {
								 kys = row.getCell((short)6).getStringCellValue() ;
								} catch (Exception d) {
									try {
									 kys = ((int) row.getCell((short)6).getNumericCellValue()) +"";
									} catch (Exception w) {
										
									}
								}
								
							String nimi =  " " ;
							try {
								nimi = row.getCell((short)7).getStringCellValue() ;
								} catch (Exception d) {
									try {
										nimi = row.getCell((short)7).getNumericCellValue() +"";
									} catch (Exception w) {
										
									}
								}
							
							String tyyp = " ";
							
							try {
								 tyyp = row.getCell((short)8).getStringCellValue() ;
								} catch (Exception d) {
									try {
									 tyyp = ((int)row.getCell((short)8).getNumericCellValue()) +"";
									} catch (Exception w) {
										
									}
								}
							tyyp = convTyyp(tyyp);
							String peab =  " ";
									
							try {
								peab = row.getCell((short)9).getStringCellValue() ;
								} catch (Exception d) {
									try {
										peab = row.getCell((short)9).getNumericCellValue() +"";
									} catch (Exception w) {
										
									}
								}
								
								
								String agents = " " ;
								
								if(agentAnkeedid.get(asi) != null ){
									agents = (String)agentAnkeedid.get(asi);
								}
								
                                     String perios = " " ;
								
								if(periodAnkeedid.get(asi) != null ){
									perios = "m";//(String)periodAnkeedid.get(asi);
								}
								if(asi.length() >0){
									questIDs.put( asi + kys, tyyp);
									buf.append((asi + ";" + asi + kys + ";" +Converter.enCodeEst(nimi) + ";" +tyyp + ";" + peab + ";" + agents + ";" + perios +  "\n" ).replaceAll(";;", "; ;") );
									listid.add(asi + kys);
												
								}
						
						}catch (Exception e){
				   	}
    			   }
			}
		
        
			
		
//Leiame listi elemendidid 
			
			for (int a = 0; a < pikkus; a++) {
				row = sheet.getRow(a);
					String asi = null;
					
					try {
					   asi = row.getCell((short)10).getStringCellValue().trim();
					} catch (Exception e) {
						try {
						 asi = row.getCell((short)10).getNumericCellValue() +"";
						} catch (Exception d) {
							
						}
					}
					
					if(asi != null && listid.contains(asi)){
						try{
						String nimi = row.getCell((short)11).getStringCellValue().trim();
						buf2.append(asi + ";"  + Converter.enCodeEst(nimi)  + "\n"  );
						}catch (Exception e){
							
						}
  			   }
			}
			
			//leiame kauplused kellel näidata ja mis ankeeti 
			

			for (int a = 0; a < pikkus; a++) {
				row = sheet.getRow(a);
					String asi = null;
					
					try {
					   asi = row.getCell((short)13).getStringCellValue().trim();
					} catch (Exception e) {
						try {
						 asi = ((int)row.getCell((short)13).getNumericCellValue()) +"";
						} catch (Exception d) {
							
						}
					}
					
					if(asi != null ){
						try{
						String ankeet = row.getCell((short)14).getStringCellValue().trim();
						if(aktiivsed.contains(ankeet)){
							if(tree.get(asi) != null){
								Vector vv = (Vector)tree.get(asi);
								vv.add(ankeet);
								tree.put(asi, vv);
							}else{
								Vector vv = new Vector();
								vv.add(ankeet);
								tree.put(asi, vv);
							}
								
						}
						}catch (Exception e){
							
						}
  			   }
			}
		
        }catch(Exception e){
			System.out.println("Questions file is not present Sorry....");
		}
	 
      //  System.out.println(buf);
		// System.out.println(buf2);
		  //  System.out.println(tree);

		    if(buf2.length() > 2){
		    	  File f = new File("custdetch.txt");
                  if (f.exists())f.delete();
                    this.saveFileInUTF8("custdetch.txt", buf2);
		     }
		   
		    if(buf3.length() > 2){
		    	  File f = new File("questnames.txt");
                if (f.exists())f.delete();
                  this.saveFileInUTF8("questnames.txt", buf3);
		     }
		   

		                    File f = new File("questions.txt");
		                    if (f.exists())f.delete();
		                      this.saveFileInUTF8("questions.txt", buf);

		                 
		return tree;
	}
	public static void saveFileInUTF8(String filename, StringBuffer sb){
		saveFileInUTF8(filename,sb.toString());
	}
	
	public static void saveFileInUTF8(String filename, String sb){
	      try{
	    	  
	    	  File f = new File(filename);
	          if (f.exists())f.delete();
	      	  Writer out = new BufferedWriter(new OutputStreamWriter(
	      			    new FileOutputStream(filename), "UTF-8"));
	      			try {
	      			    out.write(sb.toString());
	      			} finally {
	      			    out.close();
	      			}
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
		}
	
	public static void saveFile(String filename, String sb){
	      try{
	    	  
	    	  File f = new File(filename);
	          if (f.exists())f.delete();
	      	  Writer out = new BufferedWriter(new OutputStreamWriter(
	      			    new FileOutputStream(filename)));
	      			try {
	      			    out.write(sb.toString());
	      			} finally {
	      			    out.close();
	      			}
		            	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
		}
	
	public static String adressLink( String addr ,String country){
		if(addr == null || addr.trim().length() == 0){
			return " ";
		}
		return ("." + addr.replace('.', '.').replace('.', '.')+ "," + country+ ".").replaceAll(",,", ",").replaceAll(" ", "_");
	}
	private static void save(String dirout, String name, StringBuffer sb){
		try{
			
				 File fcsv = new File(dirout + name);
		 		 if (fcsv.exists())fcsv.delete();
		 		PrintWriter osv = new PrintWriter(new FileWriter(
		 				dirout + name, true));
		 	String outv = sb.toString();
		 	byte[] someBytesv = outv.getBytes();
		 	
		 	String s = new String ( someBytesv );
		 		osv.write(s);
		 		osv.close();			 
	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
   public static String deCode(String res ){
		   if(res == null){
			   return null;
		   }
    	   while(res.indexOf("11111xx") != -1){
    	   	int aa = res.indexOf("11111xx");
    	   	res = res.substring(0,aa) + "ä" + res.substring(aa+7,res.length()); 	    
    	   }
    	   while(res.indexOf("11112xx") != -1){
    	   	
    	   	int aa = res.indexOf("11112xx");
    	   //	System.out.println(aa + " " +res);
    	   	res = res.substring(0,aa) + "ö" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11113xx") != -1){
    	   	int aa = res.indexOf("11113xx");
    	   	res = res.substring(0,aa) + "ü" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11114xx") != -1){
    	   	int aa = res.indexOf("11114xx");
    	   	res = res.substring(0,aa) + "õ" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11115xx") != -1){
    	   	int aa = res.indexOf("11115xx");
    	   	res = res.substring(0,aa) + "Ä" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11116xx") != -1){
    	   	int aa = res.indexOf("11116xx");
    	   	res = res.substring(0,aa) + "Ö" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11117xx") != -1){
    	   	int aa = res.indexOf("11117xx");
    	   	res = res.substring(0,aa) + "Õ" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11118xx") != -1){
    	   	int aa = res.indexOf("11118xx");
    	   	res = res.substring(0,aa) + "Ü" + res.substring(aa+7,res.length()); 	    
    	   } 
    	   while(res.indexOf("11119xx") != -1){
          	   	int aa = res.indexOf("11119xx");
          	   	res = res.substring(0,aa) + "Š" + res.substring(aa+7,res.length()); 	    
          	   } 
       	   while(res.indexOf("11120xx") != -1){
          	   	int aa = res.indexOf("11120xx");
          	   	res = res.substring(0,aa) + "š" + res.substring(aa+7,res.length()); 	    
          	   } 

    	  
       	   while(res.indexOf("11121xx") != -1){
          	   	int aa = res.indexOf("11121xx");
          	   	res = res.substring(0,aa) + "ž" + res.substring(aa+7,res.length()); 	    
          	   } 
       	   while(res.indexOf("11122xx") != -1){
          	   	int aa = res.indexOf("11122xx");
          	   	res = res.substring(0,aa) + "Ž" + res.substring(aa+7,res.length()); 	    
          	   } 
    	   
    	  
    	
    	   while(res.indexOf("11123xx") != -1){
       	   	int aa = res.indexOf("11123xx");
       	   	res = res.substring(0,aa) + "Ā" + res.substring(aa+7,res.length()); 	    
       	   } 
    	   while(res.indexOf("11124xx") != -1){
       	   	int aa = res.indexOf("11124xx");
       	   	res = res.substring(0,aa) + "ā" + res.substring(aa+7,res.length()); 	    
       	   } 
    	   while(res.indexOf("11127xx") != -1){
       	   	int aa = res.indexOf("11127xx");
       	   	res = res.substring(0,aa) + "Č" + res.substring(aa+7,res.length()); 	    
       	   } 
    	   while(res.indexOf("11128xx") != -1){
       	   	int aa = res.indexOf("11128xx");
       	   	res = res.substring(0,aa) + "č" + res.substring(aa+7,res.length()); 	    
       	   } 
    	   while(res.indexOf("11129xx") != -1){
       	   	int aa = res.indexOf("11129xx");
       	   	res = res.substring(0,aa) + "Ė" + res.substring(aa+7,res.length()); 	    
       	   } 
    	   while(res.indexOf("11130xx") != -1){
          	   	int aa = res.indexOf("11130xx");
          	   	res = res.substring(0,aa) + "ė" + res.substring(aa+7,res.length()); 	    
          	   } 
    	   while(res.indexOf("11131xx") != -1){
         	   	int aa = res.indexOf("11131xx");
         	   	res = res.substring(0,aa) + "Ē" + res.substring(aa+7,res.length()); 	    
         	   } 
    	   while(res.indexOf("11132xx") != -1){
         	   	int aa = res.indexOf("11132xx");
         	   	res = res.substring(0,aa) + "ē" + res.substring(aa+7,res.length()); 	    
         	   } 
    	   while(res.indexOf("11133xx") != -1){
         	   	int aa = res.indexOf("11133xx");
         	   	res = res.substring(0,aa) + "Ģ" + res.substring(aa+7,res.length()); 	    
         	   } 
    	   while(res.indexOf("11134xx") != -1){
         	   	int aa = res.indexOf("11134xx");
         	   	res = res.substring(0,aa) + "ģ" + res.substring(aa+7,res.length()); 	    
         	   } 
    	   while(res.indexOf("11135xx") != -1){
         	   	int aa = res.indexOf("11135xx");
         	   	res = res.substring(0,aa) + "Ī" + res.substring(aa+7,res.length()); 	    
         	   } 
    	   while(res.indexOf("11136xx") != -1){
         	   	int aa = res.indexOf("11136xx");
         	   	res = res.substring(0,aa) + "ī" + res.substring(aa+7,res.length()); 	    
         	   } 
    	   while(res.indexOf("11137xx") != -1){
        	   	int aa = res.indexOf("11137xx");
        	   	res = res.substring(0,aa) + "Į" + res.substring(aa+7,res.length()); 	    
        	   } 
    	   while(res.indexOf("11138xx") != -1){
        	   	int aa = res.indexOf("11138xx");
        	   	res = res.substring(0,aa) + "į" + res.substring(aa+7,res.length()); 	    
        	   } 
    	   while(res.indexOf("11139xx") != -1){
        	   	int aa = res.indexOf("11139xx");
        	   	res = res.substring(0,aa) + "Ķ" + res.substring(aa+7,res.length()); 	    
        	   } 
    	   while(res.indexOf("11140xx") != -1){
        	   	int aa = res.indexOf("11140xx");
        	   	res = res.substring(0,aa) + "ķ" + res.substring(aa+7,res.length()); 	    
        	   } 
     	   while(res.indexOf("11141xx") != -1){
       	   	int aa = res.indexOf("11141xx");
       	   	res = res.substring(0,aa) + "Ļ" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11142xx") != -1){
       	   	int aa = res.indexOf("11142xx");
       	   	res = res.substring(0,aa) + "ļ" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11143xx") != -1){
       	   	int aa = res.indexOf("11143xx");
       	   	res = res.substring(0,aa) + "Ņ" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11144xx") != -1){
       	   	int aa = res.indexOf("11144xx");
       	   	res = res.substring(0,aa) + "ņ" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11145xx") != -1){
       	   	int aa = res.indexOf("11145xx");
       	   	res = res.substring(0,aa) + "Ų" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11146xx") != -1){
       	   	int aa = res.indexOf("11146xx");
       	   	res = res.substring(0,aa) + "ų" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11147xx") != -1){
       	   	int aa = res.indexOf("11147xx");
       	   	res = res.substring(0,aa) + "Ū" + res.substring(aa+7,res.length()); 	    
       	   } 
     	   while(res.indexOf("11148xx") != -1){
       	   	int aa = res.indexOf("11148xx");
       	   	res = res.substring(0,aa) + "ū" + res.substring(aa+7,res.length()); 	    
       	   }    
     	   while(res.indexOf("11125xx") != -1){
          	   	int aa = res.indexOf("11125xx");
          	   	res = res.substring(0,aa) + "ą" + res.substring(aa+7,res.length()); 	    
          	   } 
        	   while(res.indexOf("11126xx") != -1){
          	   	int aa = res.indexOf("11126xx");
          	   	res = res.substring(0,aa) + "Ą" + res.substring(aa+7,res.length()); 	    
          	   }  
     
        	   while(res.indexOf("11150xx") != -1){
             	   	int aa = res.indexOf("11150xx");
             	   	res = res.substring(0,aa) + "é" + res.substring(aa+7,res.length()); 	    
             	   }  
    
     	return res;
     }
	
	
	public static void makeTelemaFiles(String dirout,TreeMap prods,TreeMap groups,TreeMap Customers,String ILN){
		
		 Calendar cal = Calendar.getInstance();
			String time = (cal.getTimeInMillis() + "").substring(5);
	       String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	      String   day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;	
	      String  dateString = cal.get(Calendar.YEAR) +"-"+ month +"-"+ day;
		  String  timestring  = cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE);
		//gruppide tegemine
		if(groups != null){
			StringBuffer sb = new StringBuffer(); 
			StringBuffer sb2 = new StringBuffer(); 
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<E-Document>\n");
			sb.append("<Header><DateIssued>"+ dateString +"</DateIssued><SenderID>" + ILN + "</SenderID><ReceiverGLN>4741410000001</ReceiverGLN></Header>\n");
			sb.append("<Document>\n");
			sb.append("<DocumentType>PRODAT</DocumentType><DocumentFunction>original</DocumentFunction>\n");
			sb.append("<DocumentParties><SellerParty context=\"self\"><PartyCode>" + ILN + "</PartyCode><GLN>" + ILN + "</GLN></SellerParty>\n" +
					"</DocumentParties>\n<DocumentInfo>\n<DocumentName>tootegrupid+tooted</DocumentName>\n<DocumentNum>PRODAT_" +  dateString + "-" + timestring +
					":1:L</DocumentNum>\n<DateInfo><IssueDate>" +  dateString + "</IssueDate></DateInfo></DocumentInfo>\n<DocumentItem>");
			
			sb2.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<E-Document>\n");
			sb2.append("<Header><DateIssued>"+ dateString +"</DateIssued><SenderID>" + ILN + "</SenderID><ReceiverGLN>4741410000001</ReceiverGLN></Header>\n");
			sb2.append("<Document>\n");
			sb2.append("<DocumentType>PRICAT</DocumentType><DocumentFunction>original</DocumentFunction>\n");
			sb2.append("<DocumentParties><SellerParty context=\"self\"><PartyCode>" + ILN + "</PartyCode><GLN>" + ILN + "</GLN></SellerParty>\n" +
					"</DocumentParties>\n<DocumentInfo>\n<DocumentName>hinnad</DocumentName>\n<DocumentNum>PRICAT_" +  dateString + "-" + timestring +
					":1:L</DocumentNum>\n<DateInfo><IssueDate>" +  dateString + "</IssueDate></DateInfo></DocumentInfo>\n<DocumentItem>");
			
			 for (Iterator iter = groups.keySet().iterator(); iter.hasNext(); ) {
		            	    		String kood = (String)iter.next();
				    sb.append("<GroupEntry>\n<LineFunction>original</LineFunction>\n<GroupCode>" + kood + "</GroupCode>\n" +
							  "<GroupDescription lang=\"ET\">" + deCode((String)groups.get(kood)) + "</GroupDescription>\n</GroupEntry>\n");
					            
		          }
			
		
			
			
			
			int count = 1;
			if(prods != null){
			
			 for (Iterator iter = prods.keySet().iterator(); iter.hasNext(); ) {
					String kood = (String)iter.next();
				Product prod = (Product)prods.get(kood);
				String ean = "";
				String ean2 = "";
				
				if(prod.getEAN() != null && prod.getEAN().trim().length() > 3){
					ean ="<GTIN>"  + prod.getEAN() + "</GTIN>\n" ;
					ean2 ="<ItemGTIN>"  + prod.getEAN() + "</ItemGTIN>\n" ;
				}
				
				String kast = "";
				if(prod.getInCases() > 0 ){
					kast = "<ItemUnitRecord>\n<ItemUnit>kast</ItemUnit>\n<UnitNumerator>" + prod.getInCases() + "</UnitNumerator>\n<UnitDenumerator>1</UnitDenumerator>\n</ItemUnitRecord>\n";					
				}
				sb.append("<ItemEntry>\n<LineFunction>original</LineFunction>\n" +
						"<LineItemNum>" + count + "</LineItemNum>\n" +
						"<SellerItemCode>" + escapeXML( prod.getID()) + "</SellerItemCode>\n" +
						 ean +
						
						 "<ItemDescription lang=\"ET\">" + escapeXML((deCode(prod.getName()))) + "</ItemDescription>\n" +
						"<ItemGroupCode>" + escapeXML(prod.getGroup()) + "</ItemGroupCode>\n" +
						"<ItemUnitRecord><ItemUnit>" + prod.getUnit() + "</ItemUnit><UnitNumerator>1</UnitNumerator><UnitDenumerator>1</UnitDenumerator></ItemUnitRecord>\n"+
						kast +
						"<BaseUnit>" + prod.getUnit() + "</BaseUnit>\n" +
						"</ItemEntry>\n");
				sb2.append("<ItemPartyEntry>\n<LineFunction>update</LineFunction>\n" +
						"<PartyCode>MATNR</PartyCode>\n" +
						"<SellerItemCode>" +  escapeXML(prod.getID()) + "</SellerItemCode>\n" +
						 ean2 +
						"<ItemPartyPrice>"  + prod.getPrice() + "</ItemPartyPrice>\n"+
						"<ItemUnit>" + prod.getUnit() + "</ItemUnit>\n" +
						"<ItemVAT><VATRate>" + prod.getVat()+ "</VATRate></ItemVAT>\n" +
						"</ItemPartyEntry>\n");
				/*- <ItemPartyEntry>
  <LineFunction>update</LineFunction> 
  <PartyCode>MATNR</PartyCode> 
- <!--  konstant- hind kehtib kõigile klientidele 
  --> 
  <SellerItemCode>10207</SellerItemCode> 
  <ItemGTIN>4740223013093</ItemGTIN> 
  <ItemUnit>tk</ItemUnit> 
  <ItemPartyPrice>0.500</ItemPartyPrice> 
- <ItemVAT>
  <VATRate>20.00</VATRate> 
  </ItemVAT>
  </ItemPartyEntry>*/
				
				count++;
			}
		}
			 sb.append("</DocumentItem>\n</Document>\n</E-Document>");
			 sb2.append("</DocumentItem>\n</Document>\n</E-Document>");
			 
			 
			 saveFileInUTF8(dirout+"prodat-" + time + ".xml",sb);	
			 saveFileInUTF8(dirout+"pricat-" + time + ".xml",sb2);	
		}
		
		if(Customers != null){
			StringBuffer sb = new StringBuffer(); 
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<E-Document>\n");
			sb.append("<Header><DateIssued>"+ dateString +"</DateIssued><SenderID>" + ILN + "</SenderID><ReceiverGLN>4741410000001</ReceiverGLN></Header>\n");
			sb.append("<Document>\n");
			sb.append("<DocumentType>PARTNER</DocumentType><DocumentFunction>original</DocumentFunction>\n");
			sb.append("<DocumentParties><SellerParty context=\"self\"><PartyCode>" + ILN + "</PartyCode><GLN>" + ILN + "</GLN></SellerParty>\n" +
					"</DocumentParties>\n<DocumentInfo>\n<DocumentName>partnerid</DocumentName>\n<DocumentNum>" +  dateString + "-" + timestring +
					":1:L</DocumentNum>\n<DateInfo><IssueDate>" +  dateString + "</IssueDate></DateInfo></DocumentInfo>\n<DocumentItem>");
			
			int count = 1;
			
			 for (Iterator iter = Customers.keySet().iterator(); iter.hasNext(); ) {
					String kood = (String)iter.next();
				Customer cust = (Customer)Customers.get(kood);
			  String gln = "";
			  if(cust.GLNCode != null ){
				  gln =  "<GLNCode>" +  escapeXML(cust.GLNCode) + "</GLNCode>\n";
			  }
				
			  String payerID = "";
			  if(cust.getPayer() != null && !"0".equals(cust.getPayer())){
				  payerID =  "<payerID>" +  escapeXML(cust.getPayer()) + "</payerID>\n";
			  }
			  
				sb.append("<PartnerEntry>\n<LineFunction>original</LineFunction>\n" +
						  "<LineFunction>update</LineFunction>\n" +
						  "<PartnerCode>" +  escapeXML(cust.getID()) + "</PartnerCode>\n" +
						 gln + payerID + 
						  "<Name>" + escapeXML(Converter.deCode(cust.getName())) +"</Name>\n" +
						/*  <UserID>myygimees1</UserID> <UserID>myygimees2</UserID> <UserID>myygimees3</UserID> <PricatPartyCode>MATNR</PricatPartyCode>*/
					
						"<PartnerAddress><Address1>" + escapeXML(Converter.deCode(cust.getAddress())) + "</Address1><PostalCode>" + cust.getZipcode() + "</PostalCode></PartnerAddress>\n"+
						"<RegNum>" + cust.getRegnr() + "</RegNum>\n" +
						"<VATRegNum>" + cust.getVatnr() + "</VATRegNum>\n" +
						"</PartnerEntry>\n");		
				count++;
			}
			
			 sb.append("</DocumentItem>\n</Document>\n</E-Document>");
			 saveFileInUTF8(dirout+"custat-" + time + ".xml",sb);	
		}
		
	}

	public static Calendar getWeekStartDate(Calendar cal) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(cal.getTime());
	    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
	        calendar.add(Calendar.DATE, -1);
	    }
	    return calendar;
	}

	public static Calendar getWeekEndDate(Calendar cal) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(cal.getTime());
		calendar.add(Calendar.DATE, 1);
	    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
	        calendar.add(Calendar.DATE, 1);
	    }
	    calendar.add(Calendar.DATE, -1);
	    return calendar;
	}

	public static String getXMLDate(Calendar cal) {
		 String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	      String   day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;	
	      return cal.get(Calendar.YEAR)+"-" + month +"-"+ day+"T00:00:00";
		 
	}
	
	public static void makeChumakFiles(String dirout,TreeMap prods,TreeMap Customers,ArrayList Sales,TreeMap smas){
		
		 	Calendar cal = Calendar.getInstance();
			String time = (cal.getTimeInMillis() + "").substring(5);
	       String month = (cal.get(Calendar.MONTH) + 1) + "";
	        if (month.length() == 1) month = "0" + month;
	      String   day = cal.get(Calendar.DAY_OF_MONTH) + "";
	        if (day.length() == 1) day = "0" + day;	
	      String  dateString = cal.get(Calendar.YEAR) + month + day;
		  String  timestring  = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
		  String datetimeString = dateString+" "+timestring;
		  String datetimeString2 = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "T00:00:00";
		  cal.add(Calendar.DAY_OF_MONTH, -7);
		  String lastWeekStart = getXMLDate(getWeekStartDate(cal));
		  String lastWeekEnd = getXMLDate(getWeekEndDate(cal));
		  		//20140623 11:53
		  		//2014-03-01T00:00:00
		  //gruppide tegemine

		  ArrayList usedCustomers = new ArrayList();
		  
			int count = 1;
			if(Sales != null){
				
				StringBuffer sb2 = new StringBuffer(); 

				sb2.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ROOT>\n<SalOuts>\n");
				
				//<SalOut INVOICE_NO="INVOICE_NO_01" OL_CODE="OL_CODE" MERCH_ID="1" DATE="2014-08-27T11:06:08.847" ORDER_NO="ORDER_NO" STATUS="2" DATETO="2014-08-27T11:06:08.847" PARAM1="1" DTLM="20140826 11:00" VATCALCMOD="1" PRINTORDER="0" PRINTCHECK="1" PRNCHKONLY="0" DOC_TYPE="7" WAREH_CODE="WAREH_CODE">
				
			 for (Iterator iter = Sales.iterator(); iter.hasNext(); ) {
				 Order sale = (Order)iter.next();
					StringBuffer sb3 = new StringBuffer(); 

					
				 for (Iterator<Product> iter2 = sale.getOrderLines().iterator(); iter2.hasNext(); ) {
					 Product prod = iter2.next();
					 if(prod.getID().startsWith("CUM")){
					 sb3.append("<SalOutDetail CODE=\"" + prod.getEAN() + "\" LOT_ID=\"" + prod.getPartii()+ "\" "
					 		+ "PRICE=\"" + prod.getPrice() + "\" ORDER_NO=\"" + prod.getGroup()+ "\" QTY=\"" + prod.getStock() + "\" "
					 		+ "DTLM=\"" + datetimeString + "\" STATUS=\"2\" "
					 		+ "VAT=\"20\" ACCPRICE=\"" + prod.getPrice() + "\" CUST_ID=\"153\" />\n");
					 }
					 
				 }
				 if(sb3.toString().length()>0){
				 sb2.append("<SalOut CUST_ID=\"153\" INVOICE_NO=\"" + sale.getDokNr() + "\" OL_CODE=\"" + sale.getCompany() + "\" "
					 		+ "MERCH_ID=\"0\" MERCH_CODE=\"" + sale.getAgent() + "\" DATE=\"" + sale.getShipTime() + "\" ORDER_NO=\"" + sale.getDokNr() + "\" "
					 		+ "STATUS=\"2\" DATETO=\"" + lastWeekEnd + "\" PARAM1=\"1\" DTLM=\"" + datetimeString + "\" "
					 		+ "VATCALCMOD=\"1\" PRINTORDER=\"0\" PRINTCHECK=\"0\" PRNCHKONLY=\"0\" DOC_TYPE=\"2\" "
					 		+ "WAREH_CODE=\"000000001\">\n");
				 usedCustomers.add(sale.getCompany());
				 
				 sb2.append("<SalOutDetails>" + sb3.toString());
				 }
				
				//sb2.append("<ArchivedStock WAREH_CODE=\"000000001\" CODE=\"" + escapeXML(prod.getID()) + 
				//		"\" LOT_ID=\"\" DATE=\"" + datetimeString2 + "\" STOCK=\"" + prod.getStock() + "\" />\n");

				 sb2.append("</SalOutDetails>\n" + "</SalOut>\n");
				count++;
				
			}
			 
			 
			 sb2.append("</SalOuts>\n</ROOT>");
			 
			 saveFileInUTF8(dirout+"SalOuts.xml",sb2);	
		}
			
		  
		//  System.out.println(usedCustomers.toString());
		  
		  
		//  System.out.println(prods);
			
			count = 1;
			if(prods != null){
				
				StringBuffer sb2 = new StringBuffer(); 

				sb2.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ROOT>\n<ArchivedStocks>\n");
	
				
			 for (Iterator iter = prods.keySet().iterator(); iter.hasNext(); ) {
					String kood = (String)iter.next();
				Product prod = (Product)prods.get(kood);
				double sto = 0;
				if(smas.get(kood) != null) {
					sto = sto + Double.parseDouble(smas.get(kood)+"");
				}
				if(prod.getID().startsWith("CUM")){
					sb2.append("<ArchivedStock CUST_ID=\"153\" WAREH_CODE=\"000000001\" CODE=\"" + escapeXML(prod.getEAN()) + 
							"\" LOT_ID=\"\" DATE=\"" + datetimeString2 + "\" STOCK=\"" + (prod.getStock()+ sto) + "\" />\n");
				}

				count++;
			}
		
			 
			 
			 sb2.append("</ArchivedStocks>\n</ROOT>");
			 
			 saveFileInUTF8(dirout+"ArchivedStocks.xml",sb2);	
		}
		
		if(Customers != null){
			StringBuffer sb = new StringBuffer(); 
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ROOT>\n<Outlets>\n");
			

			count = 1;
			
			 for (Iterator iter = Customers.keySet().iterator(); iter.hasNext(); ) {
					String kood = (String)iter.next();
				Customer cust = (Customer)Customers.get(kood);

			  //String 
			  if(usedCustomers.contains(kood)){
			  sb.append("<Outlet CUST_ID=\"153\" OL_ID=\"0\" SUBTYPE_ID=\"0\" AREA_ID=\"0\" "+
					  "OWNER_ID=\"0\" PCOMP_CODE=\"59361\" OL_CODE=\""+escapeXML(Converter.deCode(cust.getID()))+"\" "+
					  "NAME=\"" + escapeXML(Converter.deCode(cust.getName())) + 
					  "\" TRADE_NAME=\"" + escapeXML(Converter.deCode(cust.getName())) + "\" DIRECTOR=\"-\" "+
					  "ADDRESS=\""+ escapeXML(Converter.deCode(cust.getAddress())) +
					  "\" DELIV_ADDR=\""+escapeXML(Converter.deCode(cust.getAddress()))+"\" TELEPHONE=\"-\" FAX=\"-\" "+
					  "EMAIL=\"-\" ACCOUNTANT=\"-\" ACC_PHONE=\"-\" M_MANAGER=\"-\" MM_PHONE=\"-\" P_MANAGER=\"-\" "+
					  "OPEN_TIME=\"00:00\" CLOSE_TIME=\"00:00\" BREAK_FROM=\"00:00\" BREAK_TO=\"00:00\" ZKPO=\"-\" "+
					  "IPN=\"2723075629\" VATN=\""+ escapeXML(cust.getVatnr()) +"\" RR=\"-\" BANKCODE=\"-\" BANKNAME=\"-\" BANKADDR=\"-\" "+
					  "CONTR_NUM=\"\" CONTR_DATE=\"1899-12-30T00:00:00\" DTLM=\""+ datetimeString +"\" STATUS=\"2\" DC_ALLOW=\"0\" OLDISTCENT=\"0\" OLDISTSHAR=\"0.000\" DC_DELIVER=\"0\" DC_PAYER=\"0\" LIC_USAGE=\"0\" CNTR_DT_F=\"1899-12-30T00:00:00\" />\n");
			  }
				count++;
			}
			
			 sb.append("</Outlets>\n</ROOT>");
			 saveFileInUTF8(dirout+"Outlets.xml",sb);	
		}
		
	}

	
	
	
	public static void makeVeresFiles(String dirout,TreeMap prods,TreeMap Customers,ArrayList Sales, String ourid ,TreeMap Agents, TreeMap smas){
		
	 	Calendar cal = Calendar.getInstance();
		String time = (cal.getTimeInMillis() + "").substring(5);
       String month = (cal.get(Calendar.MONTH) + 1) + "";
        if (month.length() == 1) month = "0" + month;
      String   day = cal.get(Calendar.DAY_OF_MONTH) + "";
        if (day.length() == 1) day = "0" + day;	
      String  dateString = day +"."+ month +"."+ cal.get(Calendar.YEAR) ;
	  String  timestring  = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
	  String datetimeString = dateString+" "+timestring;
	  String datetimeString2 = cal.get(Calendar.YEAR) + "-" + month + "-" + day + "T00:00:00";
	  cal.add(Calendar.DAY_OF_MONTH, -7);
	  String lastWeekStart = getXMLDate(getWeekStartDate(cal));
	  String lastWeekEnd = getXMLDate(getWeekEndDate(cal));
	  		//20140623 11:53
	  		//2014-03-01T00:00:00
	  //gruppide tegemine

	  ArrayList usedCustomers = new ArrayList();
	  ArrayList usedAgents = new ArrayList();
	  
		int count = 1;
		if(Sales != null){
			
			StringBuffer sb2 = new StringBuffer(); 

			sb2.append("Distributor's id;Client's ERP code;Date;Distributor's product code;Quantity;Sales value;Sales agent's code;Invoice number\r\n");
			
			//<SalOut INVOICE_NO="INVOICE_NO_01" OL_CODE="OL_CODE" MERCH_ID="1" DATE="2014-08-27T11:06:08.847" ORDER_NO="ORDER_NO" STATUS="2" DATETO="2014-08-27T11:06:08.847" PARAM1="1" DTLM="20140826 11:00" VATCALCMOD="1" PRINTORDER="0" PRINTCHECK="1" PRNCHKONLY="0" DOC_TYPE="7" WAREH_CODE="WAREH_CODE">
			
		 for (Iterator iter = Sales.iterator(); iter.hasNext(); ) {
			 Order sale = (Order)iter.next();
				
			 for (Iterator<Product> iter2 = sale.getOrderLines().iterator(); iter2.hasNext(); ) {
				 Product prod = iter2.next();
				 if(prod.getID().startsWith("CAV")){
				 sb2.append(ourid + ";"  + sale.getCompany()+ ";" + sale.getShipTime() + ";" + prod.getID() + ";" + + prod.getStock() + ";" + Converter.Round((float)(prod.getPrice()*prod.getStock()*1.2),2)
						 + ";" + sale.getAgent() + ";" + sale.getDokNr() + "\r\n");
				 }
			 }
		
			 if(!usedCustomers.contains(sale.getCompany())) {
		    	 usedCustomers.add(sale.getCompany());
			 }
			 if(!usedAgents.contains(sale.getAgent())) {
				 usedAgents.add(sale.getAgent());
			 }
			 
			 
			count++;
		}
		 saveFileInUTF8(dirout + "delivery.csv",sb2);	
	}
		
	  
	 // System.out.println("Veres Used customers " + usedCustomers.size());
	  
		if(prods != null){
			  System.out.println("Veres Used prods " + prods.size());
			StringBuffer sb2 = new StringBuffer(); 
			sb2.append("Distributor's id;Date;Distributor's product code;Quantity\r\n");
			
		 for (Iterator iter = prods.keySet().iterator(); iter.hasNext(); ) {
				String kood = (String)iter.next();
			Product prod = (Product)prods.get(kood);
			if(prod.getID().startsWith("CAV")){
				double sto = prod.getStock();
				if(sto < 0) {
					sto = 0;
				}
				
				if(smas.get(prod.getID()) != null) {
					sto = sto + Double.parseDouble(smas.get(prod.getID())+"");
				}
				sb2.append(ourid + ";" + dateString + 
						";" + prod.getID() + ";" + (int)sto + "\r\n");
			 }
		}
		 
		 saveFileInUTF8(dirout + "stocks.csv",sb2);	
	   }
	
		
	if(Customers != null){
		StringBuffer sb = new StringBuffer(); 
		sb.append("Distributor's id;Client's ERP code;Client's name;Client's address\r\n");
				
		 for (Iterator iter = Customers.keySet().iterator(); iter.hasNext(); ) {
				String kood = (String)iter.next();
			Customer cust = (Customer)Customers.get(kood);

		  if(usedCustomers.contains(kood)){
		     sb.append(ourid + ";" + Converter.deCode(cust.getID()) + ";\"" + Converter.deCode(cust.getName()) + 
				  "\";\"" + Converter.deCode(cust.getAddress().replaceAll("\"", "'")) + "\"\r\n");
		  }
		}
		
		 saveFileInUTF8(dirout+"clients.csv",sb);	
	}
	
	
	if(Agents != null){
		StringBuffer sb = new StringBuffer(); 
		sb.append("Distributor's id;Sales agent's code;Name Sales Agent\r\n");
		
		 for (Iterator iter = Agents.keySet().iterator(); iter.hasNext(); ) {
			String kood = (String)iter.next();
			Agent agent = (Agent)Agents.get(kood);
			if(usedAgents.contains(kood)) {
			     sb.append(ourid + ";" + Converter.deCode(agent.getID()) + ";\"" + Converter.deCode(agent.getName()) +  "\"\r\n");
			}
  		}
		 sb.append(ourid + ";n/a;\"Not Available\"\r\n");
			
		 saveFileInUTF8(dirout + "ta.csv",sb);	
	}
	
	
	if(prods != null){
		StringBuffer sb = new StringBuffer(); 
		sb.append("Distributor's id;Distributor's product code;SKU Description;EAN (bar code);Producer's SKU code;ID units of measurement\r\n");
		
			 for (Iterator iter = prods.keySet().iterator(); iter.hasNext(); ) {
					String kood = (String)iter.next();
				Product prod = (Product)prods.get(kood);
				if(prod.getID().startsWith("CAV")){
					sb.append(ourid + ";" + prod.getID() + 
							";\"" + Converter.deCode(prod.getName()).trim() + "\";" + prod.getEAN() + ";;2\r\n");
				}
			}
		
		
		 saveFileInUTF8(dirout + "sku.csv",sb);	
	}
	
}

	
	   public static  String escapeXML(String s){
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
		      }else if (c == '"'){
		        sb.append("'");
		      }else if (c == '\n'){
		        sb.append(" ");
		      }else if (c == '|'){
		      } else {
		        sb.append(c);
		      }
		    }
		    return sb.toString();
		  }
	
	public static String decode(File f, String encoding) throws Exception{
		Charset charset = Charset.forName(encoding);
	    CharsetDecoder decoder = charset.newDecoder();
    	FileInputStream fis = new FileInputStream(f);
    	byte[] arr = new byte[(int)f.length()];
    	
    	int i;
    	int counter = 0;
    	while( (i = fis.read()) != -1){
    		arr[counter] = (byte)i;
    		counter++;
    		if (counter < 1000){
	    		//System.out.println(i + " " + (char)i);	    			
    		}
    	}
    	fis.close();
    	
    	CharBuffer cb = decoder.decode(ByteBuffer.wrap(arr));
    	
    	
        //ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(sb.toString()));
    
        // Convert ISO-LATIN-1 bytes in a ByteBuffer to a character ByteBuffer and then to a string.
        // The new ByteBuffer is ready to be read.
        //CharBuffer cbuf = decoder.decode(bbuf);
        String s = cb.toString();
        return s;
	}
	
	 public static final String cleanLitLatEst(String ret){
		    if (ret != null){
		    
		        ret = ret.replace('ė','e');
		        ret = ret.replace('Į','I');
		        ret = ret.replace('ų','u');
		        ret = ret.replace('č','c');
		        ret = ret.replace('ž','z');
		        ret = ret.replace('š','s');
		        ret = ret.replace('Š','S');
		        ret = ret.replace('č','c');
		        ret = ret.replace('Į','I');
		        ret = ret.replace('ū','u');
		        ret = ret.replace('į','i');
		        ret = ret.replace('č','c');
		        ret = ret.replace('Ž','Z');
		        ret = ret.replace('Ė','E');
		        ret = ret.replace('Ų','U');
		        ret = ret.replace('Ū','U');

		        ret = ret.replace('Ā','A');
		        ret = ret.replace('ā','a');
		        ret = ret.replace('Č','C');

		        ret = ret.replace('č','c');
		        ret = ret.replace('Ē','E');
		        ret = ret.replace('ē','e');
		        ret = ret.replace('Ģ','G');
		        ret = ret.replace('ģ','g');
		        ret = ret.replace('Ī','I');
		        ret = ret.replace('ī','i');
		        ret = ret.replace('Ķ','K');
		        ret = ret.replace('ķ','k');
		        ret = ret.replace('Ļ','L');
		        ret = ret.replace('ļ','l');
		        ret = ret.replace('Ņ','N');
		        ret = ret.replace('ņ','n');
		        
		        ret = ret.replace('õ','o');
		        ret = ret.replace('ä','a');
		        ret = ret.replace('ö','o');
		        ret = ret.replace('ü','u');
		       
		        ret = ret.replace('Õ','O');
		        ret = ret.replace('Ä','A');
		        ret = ret.replace('Õ','O');
		        ret = ret.replace('Ü','U');
		        ret = ret.replace('Õ','O');
		        ret = ret.replace('Ä','A');
		        ret = ret.replace('Õ','O');
		        ret = ret.replace('Ü','U');
		        ret = ret.replace('Õ','O');
		        ret = ret.replace('Ä','A');
		        ret = ret.replace('Õ','O');
		        ret = ret.replace('Ü','U');
		    	
		    }
		        
		        if(ret == null || ret.trim().length()==0){
		        return " ";
		        }else {
		        return ret;
		        }        
		      }
	
	 public static  String enCodeEst(String res ){
	  	 
		   if(res != null){
				res =res.replaceAll("„","'");
		    	res =res.replaceAll("“","'");
		    	res =res.replaceAll("”","'");
		    	
		    	
		    	res =res.replaceAll("°","11151xx");
		    	res =res.replaceAll("ø","11150xx");
		    	res =res.replaceAll("¹","11152xx");
		    	res =res.replaceAll("²","11153xx");
		    	res =res.replaceAll("³","11154xx");
		    	
		    	res =res.replaceAll("ä","11111xx");
			  	res =res.replaceAll("ö","11112xx");
				res =res.replaceAll("õ","11114xx");
				res =res.replaceAll("ü","11113xx");

				res =res.replaceAll("Ä","11115xx");
				res =res.replaceAll("Ö","11116xx");
				res =res.replaceAll("Õ","11117xx");
				res =res.replaceAll("Ü","11118xx");
				
				res =res.replaceAll("Š","11119xx");
				
				res =res.replaceAll("š","11120xx");
				res =res.replaceAll("ž","11121xx");
				res =res.replaceAll("Ž","11122xx");
			    res = res.replaceAll("Ā","11123xx");
		        res = res.replaceAll("ā","11124xx");
		        res = res.replaceAll("ą","11125xx");
		        res = res.replaceAll("Ą","11126xx");        
		        
		        res = res.replaceAll("Č","11127xx");
		        res = res.replaceAll("č","11128xx");
		        res = res.replaceAll("Ė","11129xx");	
		        res = res.replaceAll("ė","11130xx");
		        res = res.replaceAll("Ē","11131xx");
		        res = res.replaceAll("ē","11132xx");       
		        res = res.replaceAll("Ģ","11133xx");
		        res = res.replaceAll("ģ","11134xx");        
		        res = res.replaceAll("Ī","11135xx");
		        res = res.replaceAll("ī","11136xx");      
		        res = res.replaceAll("Į","11137xx");
		        res = res.replaceAll("į","11138xx");        
		        res = res.replaceAll("Ķ","11139xx");
		        res = res.replaceAll("ķ","11140xx");
		        res = res.replaceAll("Ļ","11141xx");
		        res = res.replaceAll("ļ","11142xx");
		        res = res.replaceAll("Ņ","11143xx");
		        res = res.replaceAll("ņ","11144xx");   
		        res = res.replaceAll("Ų","11145xx");
		        res = res.replaceAll("ų","11146xx");      
		        res = res.replaceAll("Ū","11147xx");
		        res = res.replaceAll("ū","11148xx");
		        res = res.replaceAll("é","11148xx");
		        
			 
		   }


			 	 
			  	return res;
			  }
			  




	public static String getOrderText(Order order,TreeMap names) {
		String res = "\n";
		 Iterator iter = order.getOrderLines().iterator();
	        while (iter.hasNext()){
	          OrderLine line = (OrderLine)iter.next();
	          String toode  = line.getToode();
	 	     if(toode!= null){
	 	    	 toode = toode.trim();
	 	     }else{
	 	    	 toode = "";
	 	     }
	 	     String name = line.getTooteNimi();
	 	     if(name.trim().length() == 0){
	 	    	 name = toode;
	 	     }
	 	     if(names != null && names.get(toode) != null){
	 	    	 name = names.get(toode) + "";
	 	     }
	 	     
	          if(line.getKogus()!= 0 && !toode.equals("")){
	           res = res + toode + " " + name + " " + line.getPrice() + "  [" + line.getKogus() + "] " +" \n";  
	          }
	          }

		return res;
	}

	
 public static Object readObject(String s) throws IOException,
    ClassNotFoundException, IOException {
  Object obj = null;
  File f = new File(s);
 // // // System.out.println(s +f.exists() + f.getAbsolutePath());
  if (f.exists()){
    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
    obj = ois.readObject();
    ois.close();
  }
  return obj;
}
	
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
	
	public static JSONArray getWebCustomersAll( String company, String user , String pass) {
		// TODO Auto-generated method stub
		JSONArray cc =  null;
		
		  eevaeksport.util.ReportingService serv = new ReportingService();
		  serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			try {
			 cc = 	serv.getCustomersAll();
			int lenn = cc.length();
			//System.out.println("WebCust suurus " + lenn);
        	} catch (Exception e) {
				e.printStackTrace();
			}
			
		return cc;
	}
	
	public static JSONArray getWebDayReport(long startTimestamp, long endTimestamp, String company, String user , String pass) {
		// TODO Auto-generated method stub
		JSONArray cc =  null;
		
		  eevaeksport.util.ReportingService serv = new ReportingService();
		  serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			try {
			 cc = 	serv.getDayReport(startTimestamp, endTimestamp);
			int lenn = cc.length();
			//System.out.println("Dayreport suurus " + lenn);
			serv.closeClient();	
        	} catch (Exception e) {
				e.printStackTrace();
			}
			
		return cc;
	}

	public static JSONArray getWebProducts( String company, String user , String pass) {
		// TODO Auto-generated method stub
		JSONArray cc =  null;
		
		  eevaeksport.util.ReportingService serv = new ReportingService();
		  serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			try {
			 cc = 	serv.getProducts();
			int lenn = cc.length();
			//System.out.println("WebCust suurus " + lenn);
        	} catch (Exception e) {
				e.printStackTrace();
			}
			
		return cc;
	}
	
	public static TreeMap getWebCustomersAgents( String company, String user , String pass) {
		// TODO Auto-generated method stub
		TreeMap res = new TreeMap();
		
		  eevaeksport.util.ReportingService serv = new ReportingService();
		  serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			
			
			
			try {
			JSONArray cc = 	serv.getCustomersAll();
			
		
    		int lenn = cc.length();
		

        //    System.out.println("baone algus dd "  + lenn);
            
    		for (int i = 0; i < lenn; i++) {
         
            	JSONObject cob = (JSONObject) cc.getJSONObject(i);
            	String key = (String)cob.get("code");
            	System.out.println("manas " + cob.toString());
				
				try{
					JSONArray manas = (JSONArray)cob.get("managers");
					//System.out.println("manas " + manas.toString());
					int lennx = manas.length();
					for (int u = 0; u < lennx; u++) {
		            	JSONObject gr = manas.getJSONObject(u) ;
		            	
		            	String mainAgent = gr.get("code") + "";
		            	 ArrayList ags = new ArrayList();
		            	 if(res.get(key) != null) {
		            		  ags = (ArrayList)res.get(key);
		            	  }
		           	 	  ags.add(mainAgent);
			             res.put(key, ags);
			        }
		        }catch(Exception d){
                	d.printStackTrace();
                }
            }
            
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			System.out.println("WebCust suurus " + res.size());
		return res;
	}
	
	public static TreeMap getWebMercagents( String company, String user , String pass, boolean allManas,String codepart) {
		// TODO Auto-generated method stub
		TreeMap res = new TreeMap();
		
		  eevaeksport.util.ReportingService serv = new ReportingService();
		  serv.user =  user;
			serv.pass = pass;
			serv.company = company;
			
			
			
			try {
			JSONArray cc = 	serv.getCustomers(codepart);
			
		
    		int lenn = cc.length();
		

           //System.out.println("baone algus dd "  + lenn);
            
    		for (int i = 0; i < lenn; i++) {
         
            	JSONObject cob = (JSONObject) cc.getJSONObject(i);
            	String key = (String)cob.get("code");
		
				
				try{
					JSONArray manas = (JSONArray)cob.get("managers");
					//System.out.println("manas " + manas.toString());
					int lennx = manas.length();
					for (int u = 0; u < lennx; u++) {
		            	JSONObject gr = manas.getJSONObject(u) ;
		            	
		            	String mainAgent = gr.get("code") + "";
		            	 ArrayList ags = new ArrayList();
		            	 if(res.get(key) != null) {
		            		  ags = (ArrayList)res.get(key);
		            	  }
		            	 
		              if((codepart != null && mainAgent.contains(codepart)) || allManas) {
		            	 	  ags.add(mainAgent);
			            	  res.put(key, ags);
			          }else {
		              	 ags.add(mainAgent);
		            	  res.put(key, ags);
		              }
		            }
		        }catch(Exception d){
                	d.printStackTrace();
                }
            }
            
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		//	System.out.println("WebCust suurus " + res.size());
		return res;
	}

	public static TreeMap getNonVisited() {
		TreeMap tr = new TreeMap();
		
		//Kogume kokku need keda pole külastatud 
		
		String status= "plan";
	
		try {
			
			JSONParser parser = new JSONParser();
			   Object obj = parser.parse(new FileReader("visitlines" + status + ".json"));
		       org.json.simple.JSONArray cc = (org.json.simple.JSONArray) obj;
		
		for (int i = 0; i < cc.size(); i++) {
     
	   org.json.simple.JSONObject cob = ( org.json.simple.JSONObject) cc.get(i);
        	String key = (String)cob.get("custid");
        	Long time = (Long)cob.get("contacttime");
        	//System.out.println("manas " + cob.toString());
			tr.put(key, time);
        }
        
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	      //System.out.println("plan suurus"  + tr.size());
	      
	      status= "extra";
	  	
			try {
				
				JSONParser parser = new JSONParser();
				   Object obj = parser.parse(new FileReader("visitlines" + status + ".json"));
			       org.json.simple.JSONArray cc = (org.json.simple.JSONArray) obj;
			
			for (int i = 0; i < cc.size(); i++) {
	     
		   org.json.simple.JSONObject cob = ( org.json.simple.JSONObject) cc.get(i);
	        	String key = (String)cob.get("custid");
	        	
	        	//System.out.println("manas " + cob.toString());
	        	if(tr.containsKey(key)) {
	        		tr.remove(key);
	        	}
		    }
	        
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	      
		//	  System.out.println("plan suurus peale extrat"  + tr.size());
	        
			  status= "done";
			  	
				try {
					
					JSONParser parser = new JSONParser();
					   Object obj = parser.parse(new FileReader("visitlines" + status + ".json"));
				       org.json.simple.JSONArray cc = (org.json.simple.JSONArray) obj;
				
				for (int i = 0; i < cc.size(); i++) {
		     
			   org.json.simple.JSONObject cob = ( org.json.simple.JSONObject) cc.get(i);
		        	String key = (String)cob.get("custid");
		        	
		        	//System.out.println("manas " + cob.toString());
		        	if(tr.containsKey(key)) {
		        		tr.remove(key);
		        	}
			    }
		        
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				  System.out.println("plan suurus peale donet"  + tr.size());
		
		return tr;
	}



	public static String getMatixString(MatrixLine matrixLine) {
		 String result = null;

		   if(matrixLine.custID != null){
		   result = matrixLine.custID + ";" ;
		   }else{
		   result = " ;";
		   }

		   if(matrixLine.cliGroup != null){
		   result = result + matrixLine.cliGroup + ";" ;
		   }else{
		   result = result + " ;";
		   }

		   if(matrixLine.prodID != null){
		   result = result + matrixLine.prodID + ";" ;
		   }else{
		   result = result + " ;";
		   }

		   if(matrixLine.prodGroup != null){
		   result = result + matrixLine.prodGroup + ";" ;
		   }else{
		   result = result + " ;";
		   }
		   String pr = matrixLine.price+"";
		   if(pr.indexOf(".") != -1){
		   	int aa = pr.length()-pr.indexOf(".");
		   	if(aa >5){
		   		pr = pr.substring(0,pr.indexOf(".")+5) ; 	   	
		   	}
		   	}
		   if(pr.indexOf("E") != -1){
			   pr = pr.substring(0,pr.indexOf("E"));
		   }

		   result = result + matrixLine.discount + ";" + pr  + ";";

		   if(matrixLine.discGroup != null){
		    result = result + matrixLine.discGroup.trim()  + ";" ;
		   }else{
		   result = result + " ;";
		   }
		   
		   if(matrixLine.startDate != null){
			   
			   result = result + matrixLine.startDate + ";"  ;
		   }else{
		   result = result + " ;";
		   }
		   
		   if(matrixLine.endDate != null){
			   
			   result = result + matrixLine.endDate +";"  ;
		   }else{
		   result = result + " ;";
		   }
		   
		   if(matrixLine.prodLevel != null){
			     result = result + matrixLine.prodLevel +";"  ;
			     }else{
			     result = result + " ;";
			     }
		   
		   
		   
		   if(matrixLine.currency != null){
			     result = result + matrixLine.currency +";"   ;
			     }else{
			     result = result + " ;";
			     }
		   
		   if(matrixLine.lineinfo != null){
			     result = result + matrixLine.lineinfo +";"  ;
			     }else{
			     result = result + " ;";
			     }
		   
		   if(matrixLine.minAm > 0){
			   result = result + matrixLine.minAm   ;
		   }else{
			   result = result + "0";
		   }
		     return result;
	}



	public static String getStringForFixedAssortment(MatrixLine matrixLine) {
		  String result = null;

		     if(matrixLine.custID != null){
		     result = matrixLine.custID + ";" ;
		     }else{
		     result = " ;";
		     }

		     if(matrixLine.cliGroup != null){
		     result = result + matrixLine.cliGroup + ";" ;
		     }else{
		     result = result + " ;";
		     }

		     if(matrixLine.prodID != null){
		     result = result + matrixLine.prodID  + ";" ;
		     }else{
		     result = result + " ;";
		     }

		     

		     if(matrixLine.prodLevel != null){
		     result = result + matrixLine.prodLevel +";"  ;
		     }else{
		     result = result + " ;";
		     }
		     
		     if(matrixLine.lineinfo != null){
		         result = result + matrixLine.lineinfo +";"  ;
		         }else{
		         result = result + " ;";
		         }
		   
		       return result;

	}



	public static String getStringForLastThree(MatrixLine matrixLine) {
		 String result = null;

	     if(matrixLine.custID != null){
	     result = matrixLine.custID + ";" ;
	     }else{
	     result = " ;";
	     }


	     if(matrixLine.prodID != null){
	     result = result + matrixLine.prodID + ";"  ;
	     }else{
	     result = result + " ;";
	     }

	     if(matrixLine.price != -1){
	     result = result + matrixLine.price + ";"  ;
	     }else{
	     result = result + "-1;";
	     }
	     
	     if(matrixLine.startDate != null){
	     result = result + matrixLine.startDate + ";" ;
	     }else{
	     result = result + " ;";
	     }
	     
	     if(matrixLine.lineinfo != null){
		     result = result + matrixLine.lineinfo + ";" ;
		     }else{
		     result = result + " ;";
		     }
	     
	     if(matrixLine.discount != -1){
		     result = result + matrixLine.discount + ";"    ;
		     }else{
		     result = result + "-1;";
		     }
     
	     if(matrixLine.cliGroup != null){
		     
	    	 result = result + matrixLine.cliGroup + ";" ;
		     }else{
		      result = result + " ;";
		     }
	     
	     if(matrixLine.minAm != 0){
		      result = result + matrixLine.minAm ;
		     }else{
		      result = result + "0" ;
		     }
	       return result;
	}



	public static TreeMap<String, String> getErialused(File f) {
		TreeMap tree = new TreeMap();
		FileInputStream ff  = null;
		
		 //  System.out.println("Erialuste sisselugemine " + f.getName());
		try{
			
			
			 ff = new FileInputStream(f);
		  int a = 0;
				XSSFWorkbook workbook = new XSSFWorkbook(ff); 
				XSSFSheet sheet = workbook.getSheetAt(0); // rimi
				ff.close();	
				
				
				//this.getProducts(new Agent("xxx"));
				
				TreeMap groups = new TreeMap();
				DataFormatter df = new DataFormatter();
				XSSFRow row = sheet.getRow(0);
				// MÄÄRAME ÄRA MIS VÄLJAS ASUVAD Sortimendi tähised.

			//	 System.out.println("rownum " + row.getLastCellNum());
				for (int aa = 3; aa < row.getLastCellNum(); aa++) {
					try {
						// System.out.println(row.getCell((short)a).getStringCellValue());
						groups.put(aa + "", Converter.cleanLitLatEst(df.formatCellValue(row.getCell((short) aa))
								));
					} catch (Exception e) {
				
					}
				}
				
			//	System.out.println("groups  " + groups.toString());

				for (int i = 5; i < sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					String prodID = "";
					String prodName = "";

					if (row != null && row.getCell((short) 0) != null) {

						try {
							prodID = df.formatCellValue(row.getCell((short) 0));
						} catch (Exception e) {
						  e.printStackTrace();
						}
						
						try {
							prodName = df.formatCellValue(row.getCell((short) 1));
						} catch (Exception e) {
						  e.printStackTrace();
						}
	                    	
						// Siin läheme reasisse rokkima ja vaatama kas se toode
						// läheb kaasa
						for (int aa = 3; aa < row.getLastCellNum(); aa++) {

							String group = (String) groups.get(aa + "");
							if (group != null && group.trim().length() > 0) {

								String custID = " ";
								String clGroup = " ";
								String prodGroup = " ";
								String go = null;
								clGroup = group;
								
								// Vaatame kas selles veerus on sellel tootel pandud
								// mingi tähis
								if (row.getCell((short) aa) != null) {
									try {
										go = df.formatCellValue(row.getCell((short) aa));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							if (go != null && go.trim().length() > 0 ) {
						//	 System.out.println( a + " " + prodID + " " + clGroup + " " + go );
							 String value = "   [" + go +  "] " + prodName;
							 if(tree.get(clGroup) != null){
								 value = tree.get(clGroup) + "|" + value ;
							  }
							 	tree.put(clGroup, value);
		    	 				}
							}
						}
					}
				}
					
				//läheme Selveri kallale. 
				
				sheet = workbook.getSheetAt(1);
				
				row = sheet.getRow(2);
				
				String nimi1 = df.formatCellValue(row.getCell(2));
				String nimi2 = df.formatCellValue(row.getCell(3));		
				
			//	System.out.println("aiaiiai " + nimi1 +" " + nimi2);
				
				for (int aa = 3; aa < sheet.getLastRowNum(); aa++) {
					
					row  = sheet.getRow(aa);
					if(row != null) {
				
					String clGroup = df.formatCellValue(row.getCell(1));
					String a1 = df.formatCellValue(row.getCell(2));
					String value = "";
					if (a1 != null && a1.trim().length() > 0) {
					 value = "   [" + a1 +  "] " + nimi1 + "|";
					}
					
					String a2 = df.formatCellValue(row.getCell(3));
					if (a2 != null && a2.trim().length() > 0) {
						 value = value + "   [" + a2 +  "] " + nimi2 ;
						}
					if(value.length() > 1) {
						tree.put(clGroup, value);
					 	//System.out.println("aiaiiai " + value + " " + clGroup);
					}
					 
						
					}
				}
					
					
				
		}catch (Exception e) {
			e.printStackTrace();
		}
		return tree;
	}
	
	
	
	
	
	   public static ArrayList<String> createExcelProductProblemsOosReport(Calendar calAlgus, Calendar calLopp, String exceldir, String rcompany, String user, String pass) {
		   boolean oos=false; boolean prob=false;
		   ArrayList<String> arreturn = new ArrayList<String>();
			
			File f = new File("svdata");
			if(!f.exists()) {
				f.mkdir();
			}


		  	
			
			JSONArray prodsjson = Converter.getWebProducts(rcompany, user, pass);
			//TreeMap managers = Converter.getManagerList(rcompany, user, pass);
			
		/*	TreeMap<String, ArrayList> custagents = new TreeMap();	//populate and merge TreeMap with TreeMap
			Iterator itera = managers.keySet().iterator();
			while(itera.hasNext()) {
				TreeMap<String, ArrayList> temp = Converter.getWebMercagents(rcompany, user, pass, false, itera.next().toString());
				for (String k : temp.keySet()) {
					ArrayList arr = custagents.get(k);
					if(arr == null) {
						arr = new ArrayList();
					}
					arr.add(temp.get(k));
					custagents.put(k, arr);
				} 
				//System.out.println(custagents.size());
			}*/
			//System.out.println("manas" + managers.toString());

			
			//String exceldir = this.getExcelDir();
			String fnamePROB = exceldir + "productdetailproblembase.xlsx";
			try {	
				FileInputStream excelifailOOS = new FileInputStream(fnamePROB);
				FileInputStream excelifailPROB = new FileInputStream(fnamePROB);
				XSSFWorkbook  wbOOS = new XSSFWorkbook(excelifailOOS);
				XSSFWorkbook  wbPROB = new XSSFWorkbook(excelifailPROB);
				XSSFSheet worksheetOOS = wbOOS.getSheetAt(0);
				XSSFSheet worksheetPROB = wbPROB.getSheetAt(0);
				
				
				XSSFFont defaultFontOOS = wbOOS.createFont();					//font ja style
					defaultFontOOS.setFontHeightInPoints((short)11);
					defaultFontOOS.setFontName("Calibri");
					defaultFontOOS.setColor(new XSSFColor(Color.black));
				XSSFFont defaultFontPROB = wbPROB.createFont();					//font ja style
					defaultFontPROB.setFontHeightInPoints((short)11);
					defaultFontPROB.setFontName("Calibri");
					defaultFontPROB.setColor(new XSSFColor(Color.black));
				
				XSSFCellStyle borderOOS = wbOOS.createCellStyle();
					borderOOS.setBorderBottom(BorderStyle.THIN);
					borderOOS.setBorderLeft(BorderStyle.THIN);
					borderOOS.setBorderRight(BorderStyle.THIN);
				XSSFCellStyle borderCenteredOOS = wbOOS.createCellStyle();
					borderCenteredOOS.setAlignment(HorizontalAlignment.CENTER);
					borderCenteredOOS.setBorderBottom(BorderStyle.THIN);
					borderCenteredOOS.setBorderLeft(BorderStyle.THIN);
					borderCenteredOOS.setBorderRight(BorderStyle.THIN);
					borderCenteredOOS.setFont(defaultFontOOS);
				
				XSSFCellStyle borderPROB = wbPROB.createCellStyle();
					borderPROB.setBorderBottom(BorderStyle.THIN);
					borderPROB.setBorderLeft(BorderStyle.THIN);
					borderPROB.setBorderRight(BorderStyle.THIN);
					
				XSSFCellStyle borderCenteredPROB = wbPROB.createCellStyle();
					borderCenteredPROB.setAlignment(HorizontalAlignment.CENTER);
					borderCenteredPROB.setBorderBottom(BorderStyle.THIN);
					borderCenteredPROB.setBorderLeft(BorderStyle.THIN);
					borderCenteredPROB.setBorderRight(BorderStyle.THIN);
					borderCenteredPROB.setFont(defaultFontPROB);
					

					
				//teeme ja loeme sv jsoni ja paneme excelisse
			  	try {
			  		
			  		org.json.JSONArray cc = Converter.getSVDataArray(calAlgus.getTime(), calLopp.getTime(), rcompany, user, pass, false, null, null, "test");
			  		
			  		TreeMap prods = new TreeMap();
			  		for(int z=0; z<prodsjson.length(); z++) {
			  			if(prodsjson.getJSONObject(z).has("code") && prodsjson.getJSONObject(z).has("EAN")) {
				  			prods.put(prodsjson.getJSONObject(z).get("code"), prodsjson.getJSONObject(z).get("EAN"));
			  			}
			  		}

			  		
			  		
			  		int problems=1;int outofstocks=1;
			  		for (int i = 0; i < cc.length(); i++) {
			  			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
			  			
			  			String filterProblem = (String)cob.get("resstr1");
			  			String filterOutOfStock = (String)cob.get("resint3").toString();
			  			
			  			String dateAndTime = (String)cob.get("recetime");
			  			String dateOnly = dateAndTime.split("T")[0];
			  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
			  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
			  			
			  			//String agid = "" +((ArrayList) (custagents.get(cob.get("custid")+"")).get(0)).get(0);
			  			String agid = (String)cob.get("custagent");
			  			String agname = (String)cob.get("custagname");
			  			
			  			if(filterProblem.equals("PRODUCTDIST")) {
			  				prob=true;
			  				worksheetPROB.createRow(problems);
			  				worksheetPROB.getRow(problems).createCell(0).setCellValue((String)cob.get("agentid"));
			  				worksheetPROB.getRow(problems).createCell(1).setCellValue(agid);
			  				worksheetPROB.getRow(problems).createCell(2).setCellValue(agname+"");
			  				worksheetPROB.getRow(problems).createCell(3).setCellValue((String)cob.get("custid"));
			  				worksheetPROB.getRow(problems).createCell(4).setCellValue((String)cob.get("custname"));
			  				worksheetPROB.getRow(problems).createCell(5).setCellValue(dateOnly);
			  				worksheetPROB.getRow(problems).createCell(6).setCellValue(timeOnly);
			  				worksheetPROB.getRow(problems).createCell(7).setCellValue(prods.get(cob.get("prodid"))+"");
			  				worksheetPROB.getRow(problems).createCell(8).setCellValue((String)cob.get("prodname"));
			  				worksheetPROB.getRow(problems).createCell(9).setCellValue((String)cob.get("resstr2"));
			  				worksheetPROB.getRow(problems).createCell(10).setCellValue("");
			  				
			  				for(int x=0; x<11; x++) {
			  					if(x == 0 || x == 1 || x == 3) {
			  						worksheetPROB.getRow(problems).getCell(x).setCellStyle(borderCenteredPROB);
			  					}else {
			  						worksheetPROB.getRow(problems).getCell(x).setCellStyle(borderPROB);
			  					}
			  				}
			  				problems++;
			  			}
			  			
			  			if(filterOutOfStock.startsWith("999")){
			  				oos=true;
			  				String res2 = "";
			  				try {
			  					if((String)cob.get("resstr2") != null) {
				  					res2 = (String)cob.get("resstr2");
				  				}
			  				}catch(Exception e) {
			  					
			  				}
			  			
			  				worksheetOOS.createRow(outofstocks);
			  				worksheetOOS.getRow(outofstocks).createCell(0).setCellValue((String)cob.get("agentid"));
			  				worksheetOOS.getRow(outofstocks).createCell(1).setCellValue(agid);
			  				worksheetOOS.getRow(outofstocks).createCell(2).setCellValue(agname);
			  				worksheetOOS.getRow(outofstocks).createCell(3).setCellValue((String)cob.get("custid"));
			  				worksheetOOS.getRow(outofstocks).createCell(4).setCellValue((String)cob.get("custname"));
			  				worksheetOOS.getRow(outofstocks).createCell(5).setCellValue(dateOnly);
			  				worksheetOOS.getRow(outofstocks).createCell(6).setCellValue(timeOnly);
			  				worksheetOOS.getRow(outofstocks).createCell(7).setCellValue(res2);
			  				worksheetOOS.getRow(outofstocks).createCell(8).setCellValue((String)cob.get("prodname"));
			  				worksheetOOS.getRow(outofstocks).createCell(9).setCellValue("");
			  				worksheetOOS.getRow(outofstocks).createCell(10).setCellValue("");
			  				
			  				
			  				for(int x=0; x<11; x++) {
			  					if(x == 0 || x == 1 || x == 3) {
			  						worksheetOOS.getRow(outofstocks).getCell(x).setCellStyle(borderCenteredOOS);
			  					}else {
			  						worksheetOOS.getRow(outofstocks).getCell(x).setCellStyle(borderOOS);
			  					}
			  				}
			  				outofstocks++;
			  			}
			  			
			  			
			  			
			  		}
			  		
			  	}catch (Exception e){
			  		e.printStackTrace();
				}
			  	
			  	

				exceldir = exceldir + "reports/";
		  		File ff = new File(exceldir);
		  		if(!ff.exists()) {
		  			ff.mkdir();
		  		}
		  		excelifailOOS.close();
		  		excelifailPROB.close();
				
		  		if(oos) {
			  		FileOutputStream output_file_oos =new FileOutputStream(exceldir + "productdetailoos-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
			  		arreturn.add(exceldir + "productdetailoos-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
			  		wbOOS.write(output_file_oos); //write changes
			  		output_file_oos.close();  //close the stream
		  		}
		  		if(prob) {
			  		FileOutputStream output_file_prob =new FileOutputStream(exceldir + "productdetailproblem-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
			  		arreturn.add(exceldir + "productdetailproblem-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
			  		wbPROB.write(output_file_prob); //write changes
			  		output_file_prob.close();  //close the stream
		  		}

		  		System.out.println(oos + " " + prob);
		  		
			}catch (Exception e){
				e.printStackTrace();
			}
			
			
		   return arreturn;
		   
		   
		   
	   }
		   

	
	
	
	
	   public static void createExcelSortVSSoldReport(TreeMap products1, TreeMap customers1, Vector custProds, Vector matrix, String exceldir, String rcompany, String user, String pass) {
		   
			TreeMap prods = new TreeMap();
			Iterator iter1 = products1.values().iterator();
			while (iter1.hasNext()){
				Product prod = (Product) iter1.next();
				prods.put(prod.getID(), prod);
			}
			
			TreeMap customers = new TreeMap();
			iter1 = customers1.values().iterator();
			while (iter1.hasNext()){
				Customer cust = (Customer) iter1.next();
				customers.put(cust.getID(), cust);
			}

		   
		  TreeMap levelTooted = new TreeMap();
		  Converter.getContractsFixedByCust(levelTooted, rcompany, user, pass, true);

		  //array kus on olemas kõik esinevad tooted lepingutes
			Iterator iteraa = levelTooted.keySet().iterator();
			ArrayList leptooted = new ArrayList();
			
			while(iteraa.hasNext()) {
				String cu = iteraa.next()+"";
				ArrayList prods1 = (ArrayList) levelTooted.get(cu);
				Iterator iteraaa = prods1.iterator();
				while(iteraaa.hasNext()) {
					String idd = (String) iteraaa.next();
					
					if(!leptooted.contains(idd)) {
						leptooted.add(idd);
					}
				}
			}
		   
			//k2sime veebist leveli ja kaupluse seost
			  TreeMap custLevels = new TreeMap();
			  TreeMap levelsCustomers = new TreeMap();
			  try {
					Converter.loadCustDimensions("level", rcompany, user, pass);
					custLevels = Converter.getCustDimsLoading("level");
					Iterator itera =	custLevels.keySet().iterator();
					
					while(itera.hasNext()) {
						String cu =itera.next()+"";
						ArrayList<String> ar = (ArrayList<String>)custLevels.get(cu);
						for(String lev: ar) {
							if(levelsCustomers.get(lev) != null) {
								ArrayList vv = (ArrayList)levelsCustomers.get(lev);
								vv.add(cu);
								levelsCustomers.put(lev, vv);
							} else {
								ArrayList vv = new ArrayList();
								vv.add(cu);
								levelsCustomers.put(lev, vv);
							}
						}
				}
				} catch (Exception e) {
					e.printStackTrace();
				}
			  
			  
			  
			  
			  	TreeMap<String, ArrayList> maatriksprods = new TreeMap<String, ArrayList>();
			  
			  try {
				  
					Iterator itera =	custLevels.keySet().iterator();
					
					while(itera.hasNext()) {
						String cu =itera.next()+"";
						
						Iterator itera1 = matrix.iterator();
						while(itera1.hasNext()) {
							MatrixLine line = (MatrixLine) itera1.next();
							if(leptooted.contains(line.prodID) && line.startDate != null && line.endDate != null) {
								Customer cust = (Customer) customers.get(cu);
								ArrayList prodss = new ArrayList();
								if(maatriksprods.get(cu) != null) {
									prodss = (ArrayList) maatriksprods.get(cu);
								}
								
								
								if(cust.getGroup() != null && line.getCliGroup() != null && cust.getGroup().contains(line.getCliGroup())) {
									prodss.add(line.prodID);
								}
								if(cust.getGroup() != null && line.getCliGroup() != null && cust.getPayer().equals(line.getCliGroup())) {
									prodss.add(line.prodID);
								}
								if(line.getCustID() != null && cust.getID().equals(line.getCustID())) {
									prodss.add(line.prodID);
								}
								
								maatriksprods.put(cu, prodss);
								
							}
						}	
					}
			  

					//System.out.println(maatriksprods.toString());
					//System.exit(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
			  
			  
			  
			
			  TreeMap masterSortiment = new TreeMap();
				Iterator itera = custLevels.keySet().iterator();
				
				while(itera.hasNext()) {
					String custid = itera.next()+"";
					ArrayList levels = (ArrayList) custLevels.get(custid);
					ArrayList tooted1 = new ArrayList();
					
					Iterator iteral = levels.iterator();
					while(iteral.hasNext()) {
						String level = (String) iteral.next();
						
						if(levelTooted.get(level) != null) {
							ArrayList aaa = (ArrayList) levelTooted.get(level);
							Iterator itera2 = aaa.iterator();
							
							while(itera2.hasNext()) {
								tooted1.add(itera2.next() + "");
							}
						}
					}
					if(tooted1.size()!=0) {
						masterSortiment.put(custid, tooted1);
					}
				}
				
				
				  TreeMap <String, ArrayList> custBuys = new TreeMap<String, ArrayList>();
				  itera = custProds.iterator();
				
					while(itera.hasNext()) {
						CustProd cp = (CustProd) itera.next();
						String custid = cp.getCustID();
						String prodid = cp.getProdID();
						ArrayList produss = new ArrayList();
						
						if(custBuys.get(custid)!=null) {
							produss = custBuys.get(custid);
						}
						   produss.add(prodid);
							custBuys.put(custid, produss);
					}

		   
		   
			 	NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
			 	nf2.setMaximumFractionDigits(4);
				nf2.setMinimumFractionDigits(2);
				nf2.setGroupingUsed(false);
		    
		    	
		    	String fname = exceldir + "fixreportbase.xlsx";

							
	  try{
		  
							  FileInputStream excelifail = new FileInputStream(fname); //Read the spreadsheet that needs to be updated
						      XSSFWorkbook  wb = new XSSFWorkbook(excelifail);
						      XSSFSheet worksheet = wb.getSheetAt(0);
						      
						      
						      XSSFColor colorBlack = new XSSFColor(Color.black);//212°, 15%, 96%
							  XSSFColor colorBlue = new XSSFColor(new java.awt.Color(204, 255, 240));
							  XSSFColor colorGreen = new XSSFColor(new java.awt.Color(28, 122, 53));
						      
						      XSSFFont defaultFont= wb.createFont();					//tavaline must tekst
						        defaultFont.setFontHeightInPoints((short)10);
						        defaultFont.setFontName("Arial");
						        defaultFont.setColor(colorBlack);
						        defaultFont.setBold(false);
						        defaultFont.setItalic(false);
							  XSSFFont greenBoldFont= wb.createFont();					//roheline bold tekst
							  greenBoldFont.setFontHeightInPoints((short)10);
							  greenBoldFont.setFontName("Arial");
							  greenBoldFont.setColor(colorGreen);
							  greenBoldFont.setBold(true);
							  greenBoldFont.setItalic(true);	
							  greenBoldFont.setUnderline(XSSFFont.U_SINGLE);
		
							  
							  
					        
							  XSSFCellStyle centerStyle = wb.createCellStyle();
							    centerStyle.setFont(defaultFont);
							    centerStyle.setAlignment(CellStyle.ALIGN_CENTER);
								    
						      XSSFCellStyle myydudStyle = wb.createCellStyle();
						      	myydudStyle.setFont(defaultFont);
						      	myydudStyle.setAlignment(CellStyle.ALIGN_CENTER);
						      	myydudStyle.setFillForegroundColor(colorBlue);
						      	myydudStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); 
						      	
						      XSSFCellStyle campaignStyle = wb.createCellStyle();
						      	campaignStyle.setFont(greenBoldFont);
						      	campaignStyle.setAlignment(CellStyle.ALIGN_CENTER);
							      
						      XSSFCellStyle campaignMyydudStyle = wb.createCellStyle();
							      campaignMyydudStyle.setFont(greenBoldFont);
							      campaignMyydudStyle.setAlignment(CellStyle.ALIGN_CENTER);
							      campaignMyydudStyle.setFillForegroundColor(colorBlue);
							      campaignMyydudStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); 
					
							      
						      //genereerime xssf objektid
						        XSSFRow row1 = worksheet.getRow(1);  //0 = row number
						        XSSFCell c = row1.getCell(1);
				
								    row1 = worksheet.getRow(1);  //0 = row number
								      c = row1.getCell(2);
								      c.setCellValue(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
								      
								      
								      // kirjutame kõik sortimendis olevad tooted excelisse ülevalt alla
								  
								      int algusx = 1;
								      int algusy = 5;
								     
								      
								      HashMap<String, Integer> tootedjrk = new HashMap<String, Integer>();
								      Iterator iter2 = leptooted.iterator();
								  		while (iter2.hasNext()){
								  			
								  			String id = (String) iter2.next();
								  			Product prod = (Product) prods.get(id);
								  			if(worksheet.getRow(algusy)==null) {
								  				worksheet.createRow(algusy);
								  				worksheet.getRow(algusy).createCell(algusx);
								  				worksheet.getRow(algusy).createCell(algusx+2);
								  				worksheet.getRow(algusy).createCell(algusx+3);
								  			}
								  			row1 = worksheet.getRow(algusy);
								  			row1.createCell(algusx).setCellValue(prod.getID());
								  			row1.createCell(algusx + 1).setCellValue(prod.getEAN());
								  			row1.createCell(algusx + 2).setCellValue(Converter.deCode(prod.getName()));
					
								  			tootedjrk.put(id, algusy);
								  			
								  			algusy++;
								  		}
								  		int tootedKokku = algusy;
					
								  		//Lisame päisesse kliendi andmed

								  		
								  		algusx = 4;
									    algusy = 1;
								  		
								  		iter2 = masterSortiment.keySet().iterator();
								  		while (iter2.hasNext()){
								  			String id = (String) iter2.next();
								  			Customer cust = (Customer) customers.get(id);
								  			
								  			
								  			//Paneme kliendi id paika
							  				worksheet.getRow(algusy).createCell(algusx).setCellValue(id);
							  				//Paneme paika kliendi nime
							  				worksheet.getRow(algusy+1).createCell(algusx).setCellValue(Converter.deCode(cust.getName()));
							  				//Paneme paika kliendi nime
							  				worksheet.getRow(algusy+2).createCell(algusx).setCellValue(cust.getMainAgent());
							  				//Paneme paika kliendi keti
							  				worksheet.getRow(algusy+3).createCell(algusx).setCellValue(cust.getChain());
							  				
							  				//M2rgistame kliendi tooted sortimendis
							  				ArrayList prodss = (ArrayList) masterSortiment.get(id);
							  				Iterator iter3 = prodss.iterator();
							  				while (iter3.hasNext()){
							  					String prodid = (String) iter3.next();
						  						//System.out.println(prodid + " ");

							  					if(tootedjrk.get(prodid) != null) {
							  						int rownum=tootedjrk.get(prodid);
							  						//System.out.println(prodid + " " + rownum);
							  						XSSFCell ccc = worksheet.getRow(rownum).createCell(algusx);
							  						ccc.setCellValue("1");
							  						ccc.setCellStyle(centerStyle);
							  					}
							  				}
							  				
							  				
							  		
							  					ArrayList produss = custBuys.get(id); //mida pood on müünud
							  					
							  					if(produss!=null) {
							  						Iterator iter33 = produss.iterator();
									  				while (iter33.hasNext()){
									  					String tootdeid = (String) iter33.next();
									  					if(tootedjrk.get(tootdeid) != null) {
									  						int rownum=tootedjrk.get(tootdeid);
									  						
												  			XSSFRow row = worksheet.getRow(rownum);
												  			if(row == null) {
												  				row = worksheet.createRow(rownum);
												  			}
												  			XSSFCell cell = row.getCell(algusx);
												  			if(cell==null) {
												  				cell = row.createCell(algusx);
												  			}
									  						cell.setCellStyle(myydudStyle);
								  						}
									  				}
							  					}
							  					
							  					//v2rvime kampaania teksti roheliseks
							  			  		produss = (ArrayList) maatriksprods.get(id); 
							  					if(produss!=null) {
							  						Iterator iter33 = produss.iterator();
									  				while (iter33.hasNext()){
									  					String tootdeid = (String) iter33.next();
									  					if(tootedjrk.get(tootdeid) != null) {
									  						int rownum=tootedjrk.get(tootdeid);
									  						
												  			XSSFRow row = worksheet.getRow(rownum);
												  			if(row == null) {
												  				row = worksheet.createRow(rownum);
												  			}
												  			XSSFCell cell = row.getCell(algusx);
												  			
												  			if(cell==null) {
												  				cell = row.createCell(algusx);
												  				cell.setCellStyle(campaignStyle);
												  			}else if(cell!=null){
												  	
												  				//kui cell on juba v2rvitud, siis paneme campaignmyydydstyle
												  				XSSFColor fillColor = cell.getCellStyle().getFillForegroundXSSFColor();
												  				if(fillColor != null) {
												  					//System.out.println(fillColor.getARGBHex());
												  					cell.setCellStyle(campaignMyydudStyle);
											  					}else{
												  					cell.setCellStyle(campaignStyle);
											  					}
												  			}
								  						}
									  				}
									  			}
							  				
							  				//Mitu toodet on poes?
									  		String column_letter = CellReference.convertNumToColString(algusx);
									  		CellStyle cellStyle = worksheet.getRow(4).getCell(0).getCellStyle();
								  			Row row = worksheet.getRow(0);
								  			if(row == null) {
								  				row = worksheet.createRow(0);
								  			}
								  			Cell cell = row.createCell(algusx);
								  			cell.setCellType(Cell.CELL_TYPE_FORMULA);
								  			cell.setCellFormula("COUNTA(" + column_letter + "" + 6 + ":" + column_letter + "" + tootedKokku + ")");
								  			cell.setCellStyle(cellStyle);
								  			algusx++;
								  		}
							  		
								  		//Mitmes poes esineb toode?
								  		String last_column_letter = CellReference.convertNumToColString(algusx-1);
								  		CellStyle cellStyle = worksheet.getRow(4).getCell(0).getCellStyle();
								  		for(int x=0; x<tootedKokku-5; x++){
								  			Row row = worksheet.getRow(x + 5);
								  			if(row == null) {
								  				row = worksheet.createRow(x + 5);
								  			}
								  			Cell cell = row.createCell(0);
								  			cell.setCellType(Cell.CELL_TYPE_FORMULA);
								  			cell.setCellFormula("COUNTA(E" + (x + 5 + 1) +":" + last_column_letter + "" + (x+6) + ")");
								  			cell.setCellStyle(cellStyle);
								  		}

								  		exceldir = exceldir + "reports/";
								  		File ff = new File(exceldir);
								  		if(!ff.exists()) {
								  			ff.mkdir();
								  		}
										  excelifail.close(); //Close the InputStream
										  System.out.println(exceldir + "fixreport-" + 
												  Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
									        FileOutputStream output_file =new FileOutputStream(exceldir + "fixreport-" + 
										  Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
									        wb.write(output_file); //write changes
									        output_file.close();  //close the stream
						        
						        
	  }catch (Exception e){
			e.printStackTrace();
		}
		   
	   }
	   

	    public static double distanceBetweenCoords(double lt1, double ln1, double lt2, double ln2) {
	    	double r2d = 180.0D / 3.141592653589793D;
	    	double d2r = 3.141592653589793D / 180.0D;
	    	double d2km = 111189.57696D * r2d;
	        double x = lt1 * d2r;
	        double y = lt2 * d2r;
	        return Math.acos( Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (ln1 - ln2))) * d2km;
	    }
	   

	   
	    
	   
	   public static String saveDayReportReport(String exceldir, String rcompany, String user, String pass) {
		   LocalDate today = LocalDate.now(ZoneId.of("Europe/Tallinn"));
		   //today = today.minusDays(3);
		   while(today.getDayOfWeek().getValue() != 1) { //kui ei ole esmasp siis teeskleme et on
			   today = today.minusDays(today.getDayOfWeek().getValue()-1);
		   }
		   LocalDate lastMonday = today.with(TemporalAdjusters.previous( DayOfWeek.MONDAY ));
		   LocalDate lastSaturday = today.with(TemporalAdjusters.previous( DayOfWeek.SUNDAY ));
		   Long monTimestamp = lastMonday.atStartOfDay(ZoneId.of("Europe/Tallinn")).toEpochSecond()*1000;
		   Long satTimestamp = lastSaturday.atStartOfDay(ZoneId.of("Europe/Tallinn")).toEpochSecond()*1000 + 43200000;
		   //System.exit(0);

		   
		   
	  try{
		  JSONArray dayreportarray = getWebDayReport(monTimestamp, satTimestamp, rcompany, user, pass);
		  JSONArray webcustarray = getWebCustomersAll(rcompany, user, pass);
//System.out.println(dayreportarray.toString());
		  int lenn = webcustarray.length();
		  TreeMap custsloc = new TreeMap();
		  for (int i = 0; i < lenn; i++) {
			JSONObject cob = (JSONObject) webcustarray.getJSONObject(i);
          	String key = (String)cob.get("code");
          	try {
          	String loc = (String)cob.get("location");
          	//System.out.println("kood: " + key);
          	custsloc.put(key, loc);
          	
          	}catch(Exception e){
          	}

		  }
		  
		  StringBuffer sb = new StringBuffer(); 
		  sb.append("Agento Kodas\tKliento kodas\tKliento Pavadinimas\tData\tPradzios laikas\tPabaigos laikas\tKliento kordinates\tKordinates\tSutampa ar ne su Faktiniu adresu \r\n");
		//  sb.append("Код клиента,Название клиента,Дата заказа,Время (начала заказа),Время (Конец заказа),Координаты клиента,Координаты заказа,Совпадает или нет с фактическим адресом клиента\r\n");
			
		  lenn = dayreportarray.length();
		  for (int i = 0; i < lenn; i++) {
			  
		  	  String key = null;
			  JSONObject cob = (JSONObject) dayreportarray.getJSONObject(i);
			  key = (String)cob.get("custid");
			  
			  
			  String custname = null;
			  try {
				  custname = (String)cob.get("custname");
				  custname = custname.replaceAll("\t", ",");
			  }catch (Exception e){
			  }

			  String agentname = null;
			  
			  try {
				  agentname = (String)cob.get("agentname");
				  agentname = agentname.replaceAll("\t", ",");
			  }catch (Exception e){
			  }
			  
			  SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm");
			  String startdates = null;
			  String starttime = null;
			  Long startdate = null;
			  try {
				   startdate = (Long)cob.get("startdate");
				  Date startdatee = new Date(startdate);
				  startdates = Converter.getYYYY_MM_DD_String(startdatee);
				  starttime = hhmmFormat.format(startdatee);
			  }catch (Exception e){
			  }


			  String endtime = null;
			  try {
				  Integer duration = (Integer)cob.get("duration");
				  long enddate = duration+startdate;
				//  Date enddatee = new Date(Long.parseLong(duration+""));
				  Date enddatee = new Date(enddate);
				  endtime = hhmmFormat.format(enddatee);
			  }catch (Exception e){
			  e.printStackTrace();
			 // System.exit(0);
			  }
			  
			  
			  
			  Double lat = null;
			  Double lon = null;
			  String clientcoords = null;
			  try {
				  lat = (Double)cob.get("latitude");
				  lon = (Double)cob.get("longitude");
				  clientcoords = lat.toString() + ":" + lon.toString(); //kliendi kordinaadid
			  }catch (Exception e){
			  }

			  
			  String ordercoords = null;
			  String[] arrOfOrderCoords = null;
			  Double lat1=null;
			  Double lon1=null;
			  try {
				  ordercoords = custsloc.get(key).toString(); 
				  arrOfOrderCoords = ordercoords.split(";");
				  lat1=Double.parseDouble(arrOfOrderCoords[0]);
				  lon1=Double.parseDouble(arrOfOrderCoords[1]);
			  }catch (Exception e){
			  }

			  
			  String isInRange = null;
			  try {
				  Double distance = distanceBetweenCoords(lat, lon, lat1, lon1);
				  if (distance < 850) {
					  isInRange = "OK";
				  } else {
					  distance = (double) Math.round(distance);
					  distance = distance/1000;
					  isInRange = "NOK: " + distance + " km";
				  }
			  }catch (Exception e){
			  }

			  String ooo = "0:0";
			 if( ordercoords != null) {
				 ooo = ordercoords.replace(";", ":");
			  }
				  sb.append(agentname + "\t" + key + "\t" + custname + "\t" + startdates + "\t" + starttime + "\t" + endtime + "\t" + clientcoords + "\t" + ooo + "\t" + isInRange + "\r\n");

		  }

			saveFileInUTF8(exceldir + "vigestareport" + lastMonday + ".csv",sb);
			
return exceldir + "vigestareport" + lastMonday + ".csv";
		  
	  }catch (Exception e){
			e.printStackTrace();
		}
	  return null;    
	 }
	

		 public static TreeMap loadFixCirclek(TreeMap res, File file , TreeMap prods , EksportPanel parent) {
				String errors = null;
				
			 try{

				 Workbook workbook = null;
				 Sheet sheet = null;
				       FileInputStream	 ff = new FileInputStream(file);
				       if(file.getName().endsWith(".xlsx")) {
				    	   workbook = new XSSFWorkbook(ff); 
							 sheet = workbook.getSheetAt(0); 
				       }else {
				    	    workbook = new HSSFWorkbook(ff); 
							 sheet = workbook.getSheetAt(0); 
						
				       }
						
						
						System.out.println("CircleK lugemine " + file.getName());
						
						ff.close();	
						
						if(sheet != null){
							TreeMap groups = new TreeMap();
						    Row row = sheet.getRow(0);
						
							int r = 3;
							int ea = 1;
							
							for (int a = 0; a < row.getLastCellNum(); a++) {
								try {
									//System.out.println(row.getCell((short)a).getStringCellValue());
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.toLowerCase().indexOf("ean") != -1) {
										ea = a;
										//System.out.println(ea);
									}
								} catch (Exception e) {
									// System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
							
						
							
							row = sheet.getRow(1);
							for (int a = r; a < row.getLastCellNum(); a++) {
								try {
									 //System.out.println(row.getCell((short)a).getStringCellValue() + " " + r);
									String gg = row.getCell((short) a).getStringCellValue().trim();
									if(gg.length()>1){
								      groups.put(a + "", gg);
								      //System.out.println(gg);
								    }
								
								} catch (Exception e) {
									 //System.out.println(row.getCell((short)a).getNumericCellValue());
								}
							}
							
						//	System.out.println("r "  + r + " "+ ea + " " + addrow + " " + groups.toString());
							//System.exit(0);
							
							
							for(int i = 2 ; i < sheet.getLastRowNum()+1;i++){
								 row = sheet.getRow(i);
								 
								 DataFormatter formatter = new DataFormatter();
									String ean = formatter.formatCellValue(row.getCell(ea));
									String tnimi = formatter.formatCellValue(row.getCell(ea+1));
									String toode = getToode(prods,ean);
									
								 if(!"Nimetus".equals(tnimi)) {
									 
								 
								  if(toode.equals(ean) ) {
									  if(errors == null) {
										  errors = "Tuvastamatta tooted sortimendis\n CircleK formaat\n fail : " + file.getName() +"\n\nEAN     Nimetus\n";
									  }
									  if(!errors.contains(ean)) {
										  errors = errors + ean + "   " + tnimi + "\n";
									  }
									
								  }
								 
								 for (int a = r; a < row.getLastCellNum(); a++) {

										String group = (String) groups.get(a + "");
										
										if (group != null && group.trim().length() > 0) {
								 
		                                  String pood = "CIR-" + group;
		                                  Contract con = new Contract(pood);
		                                  String value =  formatter.formatCellValue(row.getCell(a));
		                                
		                                 
		                                 if(value.trim().length() > 0 && value.trim().equals("1")) {
		                                 	//System.out.println("Sees " +a + " " + value + " " + ean);
											if(res.get(pood) != null){
					                     	  con = (Contract)res.get(pood);
					                     	 } 
												  ContractLine ll = new ContractLine(toode);
					                     	  if(con.lines == null){
					                     		  con.lines = new Vector();
					                     	  }
					                     	  con.lines.add(ll);
					                     	  res.put(pood, con);
			                                 }
										}
								 	}
								 }
							}

						 }
						
						
						System.out.println("sorts size " + res.toString() );
							
						 }catch(Exception e) {
							 e.printStackTrace();
						 }	 
			 
			 
			//System.exit(0);
			
			
			 if(errors != null) {
				 sendFixLoadErrorEmail(errors, parent, "circlek",true);
			 }
			 sendFixLoadErrorEmail("\n CircleK imported " + res.keySet().size() + " shops fixed sortiments", parent, "circlek",false);

		return res;
	}
		 
	public static org.json.simple.JSONObject readJSONFile(String filename){
		Object obj = null;
		try {
			JSONParser parser = new JSONParser();
	        obj = parser.parse(new FileReader(filename));
	    } catch(Exception tt) {
			tt.printStackTrace();
		}

		return (org.json.simple.JSONObject) obj;
	}
		 
		 



}
