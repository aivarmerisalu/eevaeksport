package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
import eevaeksport.util.FileListFilter;
import eevaeksport.util.ReportingService;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ErplyTechDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public ErplyTechDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
   String url = getParent().getProps().getProperty("erply.url");
   ownGLN = getParent().getProps().getProperty("owngln");
   if(url == null){
	   url = "https://www.erply.net/api/";
   }
	try {
		myurl = new URL(url);
	} catch (MalformedURLException e) {
		e.printStackTrace();
	}
	
   username = getParent().getProps().getProperty("erply.user");	
   company = getParent().getProps().getProperty("erply.company");
   password = getParent().getProps().getProperty("erply.pass");
   apiversion = getParent().getProps().getProperty("erply.apiversion");
   if(getParent().getProps().getProperty("erply.onpage") != null){
	   try{
		   onpage = Integer.parseInt(getParent().getProps().getProperty("erply.onpage"));
	   }catch(Exception e){
		  e.printStackTrace(); 
	   }
	  
   }
   if(apiversion == null){
	   apiversion = "1.0";
   }
   returntype = getParent().getProps().getProperty("erply.returntype");
   if(returntype == null){
	   returntype = "XML";
   }
    passString =  userstuff = "&version=" + apiversion + "&responseType=" + returntype + "&username=" + username 
      + "&password=" + password;
   
   session = getSessionId(passString);
	System.out.println(session);
   while(session.indexOf("%") != -1){
	   session = getSessionId(passString);
   }
    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
    System.out.println(session + userstuff ) ;
   
    
    if(getParent().getProps().getProperty("laoid") != null){        	   
    	   laoID = getParent().getProps().getProperty("laoid");
       }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  public String getSaveDebtInfo() {
   	return null;
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







  

  
  private String getSessionId(String asi ){

	  String data = "clientCode=" + company + "&request=verifyUser" + asi ;
	//  System.out.println(data);
	  
	  try {
	String res = (postData(data)).toString();
	System.out.println("res " + res);
	session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));
	
	} catch (Exception e) {
		e.printStackTrace();
	}
	 
	  return session;
  }
  
  
  
  private StringBuffer postData(String data) throws Exception {
  HttpURLConnection urlc = null;

 	Converter.doTrustToCertificates();
  StringBuffer inputLineBuf = new StringBuffer();
  try{
  urlc = (HttpURLConnection) myurl.openConnection();
  try{
  urlc.setRequestMethod( "POST" );
  } catch (ProtocolException e) {
    throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
  }
  urlc.setDoOutput(true);
  urlc.setDoInput(true);
  urlc.setUseCaches(false);
  urlc.setAllowUserInteraction(false);
  OutputStream out = urlc.getOutputStream();
  try {
	out.write( data.getBytes() );
  } catch (IOException e){
  throw new Exception("IOException while posting data", e);
  } finally {
  if (out != null)
  out.close();
  }
  InputStream in = urlc.getInputStream();
  try{
  Reader reader = new InputStreamReader(in);
    InputStreamReader isr = new InputStreamReader(in,"UTF-8");
    BufferedReader inr = new BufferedReader(isr);
    String inputLine;

    while ((inputLine = inr.readLine()) != null){
        inputLine = enCode(inputLine);	// enCode(inputLine);	
    	inputLineBuf.append(inputLine.replaceAll("/>","/>\n").replace((char)0x1c, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ')
    			.replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ')
    			.replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
   
     			.replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' ')
    			);
  }

     inr.close();
  
  
  reader.close();
  } catch (IOException e)
  {
  throw new Exception("IOException while reading response", e);
  } finally
  {
  if (in != null)
  in.close();
  }
  } catch (IOException e)
  {
  throw new Exception("Connection error (is server running at " + myurl + " ?): " + e);
  } finally
  {
  if (urlc != null)
  urlc.disconnect();
  }
  return inputLineBuf;
  }
  
  
  private StringBuffer getData(String data) throws Exception {
     	String httpsURL = myurl + "?" + data;
        URL myurl1 = new URL(httpsURL);
        HttpsURLConnection con = (HttpsURLConnection)myurl1.openConnection();
        InputStream ins = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
        BufferedReader in = new BufferedReader(isr);

        String inputLine;
        StringBuffer inputLineBuf = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
        	inputLine = this.enCode(inputLine);
        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
       }
        return inputLineBuf;
	  }
	  
	  
  
  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	
	   
	  	try {
	  		
	  		 FileListFilter fil = new FileListFilter("prlisterp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int ee = 0; ee < ll.length; ee++) {
	    		
	    		
	    		
	    		 String name = ll[ee];
	    		// System.out.println(name);
	 		  File f = new File (getDir() + name);
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	      	
	        Node root = getNode(doc, "document");      
	         // System.out.println(root);
	       	Node read = getNode(root, "records");
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("item")){
	          	  try{
	          		 
	               		 String clGroup  = getNode(rida,"pricelistID").getFirstChild().getNodeValue();
	               			Node read2 = getNode(rida, "pricelistRules");
	               	    	if(read2 != null){
	               	    	
	               			for (int u = 0; u < read2.getChildNodes().getLength(); u++){
	               			 Node rida2 = read2.getChildNodes().item(u);
	               			
	               		    if(rida2 != null && rida2.getNodeName() != null && rida2.getNodeName().equals("item")){
	               		    		
	               		    	//System.out.println(group + " " + getNode(rida2,"id").getFirstChild().getNodeValue());
	               		    	//	System.out.println(group + " " + getNode(rida2,"price").getFirstChild().getNodeValue());
	               			 
	             			MatrixLine mline = new MatrixLine();
	            			if (mline.getCustID() == null && clGroup != null
	            					&& clGroup.trim().length() > 0) {
	            				mline.setCliGroup(clGroup.trim());
	            			}
	            			
                           String prodID = getNode(rida2,"id").getFirstChild().getNodeValue();
	            			if (prodID != null && prodID.trim().length() > 0) {
	            				mline.setProdID(prodID.trim());
	            			}
	            			try{
	            				 double price = Double.parseDouble(getNode(rida2,"price").getFirstChild().getNodeValue());
	 	            			if (price != 0) {
	 	            				mline.setPrice(price);
	 	            			}
	 	            			lines.add(mline);
	 	               		    		
	            			}catch(Exception k ){
	            				
	            			}
	               		    }
	               			}
	               			
	               	    	}
	          	  }catch(Exception r){
	    	  		r.printStackTrace();
	    	  	  }
	          	  }
	           	  }
	           
	    	 } 	
	    	 }catch(Exception e){
	    	  		e.printStackTrace();
	    	  	} 

      return lines;

   }

   private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection("jdbc:odbc:report", "mobile", "mobile");
	  }
   
   
   
    
   private HashMap prodHash = new HashMap();
   

   private Connection con2 = null;
   
   Hashtable prodCodes = null;
   
  
  
   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		
		if("true".equals(getParent().getProps().getProperty("usepricematrix.fixed"))){
			result = getPriceMatrixData();
		}else{
		
		File f = new File(this.getExcelDir() + "Sortiment.xls");
		
		 if( prodCodes == null){
			 this.getProducts(null);
		 }
		
		  boolean insertassort = true;
         
         try {
       	  con2 = this.getNewConnection();  
      	   String sql = "SELECT sortdate from mj_assortment where  year(sortdate) = year(getdate()) and" +
      	   		" month(sortdate) = month(getdate()) and day(sortdate) = day(getdate()) ";
       	 
       	    Connection cons = this.getNewConnection();
   	        Statement stmts = cons.createStatement();
   	        ResultSet rss = stmts.executeQuery(sql);
   	       
   	        if(rss.next()){
   	        	insertassort = false;
   	        }   
   	        
   	        rss.close();
   	        stmts.close();
   	        cons.close();
   	        
         }catch(Exception d){
      	   //d.printStackTrace();
         }
		
	//	System.out.println("asoortment juurde " + insertassort);
		
		
		try {
			
			
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(1);
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

			// System.out.println(groups.toString());

			for (int i = 2; i < sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 1) != null) {

					try {
						
						prodID = (row.getCell((short) 0).getStringCellValue());
					} catch (Exception e) {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					}
					
                   if(!prodID.equals("")){
                	
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

							if (go != null && go.trim().toLowerCase().equals("1")) {
                                  if(a > 10){
                                 //	 System.out.println( a + " " + prodID + " " +
             						//		 clGroup + " " + go); 
                                  }
								 

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									if(prodCodes != null){
										String prodX = (String)prodCodes.get(prodID.trim());
										if(prodX != null){
											mline.setProdID(prodX.trim());		
										}else{
									//		System.out.println("mingi jama toodet pole " + prodID );
										}
										
										
									}
								}
								
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								  if(insertassort){
									  try{
										  insertSortLine(mline); 
									  }catch(Exception s){
										  
									  }
								         
		                           }
								result.add(mline);
							}
						}
					}
					   
                   }
				}
			}
			if(con2 != null)
			con2.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		}
		// System.exit(0);
   System.out.println("fixed size " + result.size());
		return result;

	}

   
   
   public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr ){
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
	          con2 = this.getNewConnection(); 
	          
	          PreparedStatement ps = con2.prepareStatement(sql);

	           
	           for (Iterator it = wr.getInfo().getLines().iterator(); it.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new  eevaeksport.report.SuperVisorLineWrapperMeri(it.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             String agName =  wr.getInfo().getAgentName();
	             if(agName == null){
	            	 agName = (String)this.agents.get(wr.getInfo().getAgentID());
	             }
	             ps.setString(2, agName);
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             ps.setDouble(7, lw.getLine().getNormalPrice()*100);
	             ps.setDouble(8, lw.getLine().getAmount()*100);
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
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
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
	           con2.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	       this.connectionDown();
	     }


   
   
   private void  insertSortLine(MatrixLine mline){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'')" ;
			  
			  	        
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     
				    // System.out.println(sql);
		  }catch(Exception e){
			 // e.printStackTrace();
		  }		  
	  }

   
   
   public Vector getFixedAssortmentData1() {
       Vector lines = new Vector();
       
       try {
   		  File f = new File (getDir() + "hinnakiri.xml");
   	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   	     DocumentBuilder db;
   		db = dbf.newDocumentBuilder();      	
       	FileInputStream in = getCleanInputStream(f);
       	String handler = null;
           Document doc = db.parse(in,handler);
           Node root = getNode(doc, "transport");      
      
       	Node read = getNode(root, "priceformulas");
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  try{
           		  if(rida.getNodeName().equals("priceformula")){
           			 String custID = " ";
                     String clGroup = " ";
                     String prodID = " ";
                     String prodGroup = " ";
                     MatrixLine mline = new MatrixLine();
                    try{
                    	 clGroup = rida.getAttributes().getNamedItem("code").getNodeValue(); 
                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue(); 
                         if (custID != null && custID.trim().length() > 0) {
                           mline.setCustID(custID.trim());
                         }
                         if (clGroup != null && clGroup.trim().length() > 0) {
                           mline.setCliGroup(clGroup.trim());
                         }

                         if (prodID != null && prodID.trim().length() > 0) {
                           mline.setProdID(prodID.trim());
                         }
                         
                         if (mline.getProdID() == null && prodGroup != null
                             && prodGroup.trim().length() > 0) {
                           mline.setProdGroup(prodGroup);
                         }

                        
                        	 
                        	if(!prodsInUse.contains(prodID.trim())){
                           	  this.prodsInUse.add(prodID.trim());	
                        	}
                        	 
                            lines.add(mline);	
                          
                    
                       
                    }catch(Exception e){
                    }
                    
                   }
                	
           	  }catch(Exception e){
           		  e.printStackTrace();
           	  }
                
           }
           
   	}catch(Exception e){
   		e.printStackTrace();
   	}
  //     System.out.println(prodsInUse.size() );
       
       return lines;
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	        Vector vec = new Vector();
	       // System.out.println("Prod22 " + products.size());
	        for (int i = 0 ; i < inVec.size() ; i++){
	          vec.addElement(inVec.elementAt(i));
	        }
	        Vector sorted = new Vector(vec.size());
	        while(vec.size() > 0){
	          MatrixLine smallest = null;
	          Product smProd = null;
	          for (Enumeration enumr = vec.elements(); enumr.hasMoreElements() ; ){
	        	  MatrixLine ol = (MatrixLine)enumr.nextElement();
	            Product prod = (Product)products.get(ol.getProdID());
           //   System.out.println("Prod " + prod + "  " + ol.getProdID());
	            if ( (smallest == null && smProd == null )
	            		|| ( prod.getName().compareTo(smProd.getName()) < 0 )) {
	              smallest = ol;
	              smProd = (Product)products.get(smallest.getProdID());
	            }
	          }
	          vec.removeElement(smallest);
	          sorted.addElement(smallest);
	        }
	        return sorted;
	      }
	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }


 
       public String getSaveStockInfo(){
    	 	
    	   	return null;
       }


   public Vector getDebt(String agentCode){
        return this.debtList;
  }

   public void connectionDown(){
   }
   public void connectionUp(){
   }

   public String getDealerCustomerCSV(String agent){
       return null;
        }

       public String  getCustProdDiscounts(Agent agent){
                     return null;
            }
       
       
       private Hashtable getPricelists(String id){
    	
    	   Hashtable prices = new Hashtable();
    	   
    	  	try {

   	  		 FileListFilter fil = new FileListFilter("prlisterp","xml");
   	    	 String[] ll = new File (getDir()).list(fil);
   	    	 for (int ee = 0; ee < ll.length; ee++) {
   	    		
   	    		
   	    		
   	    		
   	    		 String name = ll[ee];
   	    		File f = new File (getDir() + name);
    	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	  	     DocumentBuilder db;
    	  		db = dbf.newDocumentBuilder();      	
    	      	FileInputStream in = getCleanInputStream(f);
    	      	String handler = null;
    	          Document doc = db.parse(in,handler);
    	      	
    	        Node root = getNode(doc, "document");      
    	         // System.out.println(root);
    	       	Node read = getNode(root, "records");
    	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
    	           	  Node rida = read.getChildNodes().item(i);
    	           	  if(rida.getNodeName().equals("item")){
    	          	  try{
    	          		 
    	               		 String group  = getNode(rida,"pricelistID").getFirstChild().getNodeValue();
    	               		if(group.equals(id)){
    	               		    
    	               			Node read2 = getNode(rida, "pricelistRules");
    	               			//System.out.println("ridu  " + read2.getChildNodes().getLength() + read2.toString());
        	              	    	
    	               			for (int u = 0; u < read2.getChildNodes().getLength(); u++){
    	               			 Node rida2 = read2.getChildNodes().item(u);
    	               			
    	               		    if(rida2 != null && rida2.getNodeName() != null && rida2.getNodeName().equals("item")){
    	               		    		
    	               		    	//System.out.println(group + " " + getNode(rida2,"id").getFirstChild().getNodeValue());
    	               		    	//	System.out.println(group + " " + getNode(rida2,"price").getFirstChild().getNodeValue());
    	               			 prices.put(getNode(rida2,"id").getFirstChild().getNodeValue(),getNode(rida2,"price").getFirstChild().getNodeValue());
    	               			}
    	               			}
    	          	         
    	               		}
    	          	  }catch(Exception r){
      	    	  		r.printStackTrace();
      	    	  	  }
    	          	  }
    	           	  }
    	           
   	    	 }   
    	           
    	          	  }catch(Exception e){
    	    	  		e.printStackTrace();
    	    	  	} 

                  //    System.out.println(prices.toString());
    	          	  return prices;
       }

       
       public   String  addLeadingZero(String aaaa){
		   String res = aaaa.trim();
		  	  int len = aaaa.trim().length();
		  	 while (len < 4){
		  		  res = "0" + res ;
		  		  len = res.length();
		  	  }
		  	  return res;
		   }
       
      
   public TreeMap getProdGroups()  {
     TreeMap   tm = new TreeMap();
     groupList = new TreeMap();
     groupListSec = new TreeMap();
     boolean subgr = false ;
     if("true".equals(getParent().getProps().getProperty("group.usesub"))){
    	 subgr = true;
    	 this.setTreeGroup(true);
     }
  	try {
  		  File f = new File (getDir() + "tootegrupiderp.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
      	
        Node root = getNode(doc, "document");      
         // System.out.println(root);
       	Node read = getNode(root, "records");
      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
           for (int i = 0; i < read.getChildNodes().getLength(); i++){
           	  Node rida = read.getChildNodes().item(i);
           	  if(rida.getNodeName().equals("item")){
          	  try{
          		 
               		 String group  = getNode(rida,"productGroupID").getFirstChild().getNodeValue();
               		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
               		 name.replaceAll("Sibullill", "Sl");
               		 group = deCode(group);
              
               		 if(subgr){
               			 group = addLeadingZero(group); 
               		 }
               		 name  =  deCode(name);
                        tm.put(name,Converter.enCodeEst(name));  
                        tm.put(group,Converter.enCodeEst(name));  
                        groupList.put(group, group);
                        groupNameList.put(group, name);
                    	Node alamad = getNode(rida, "subGroups");
                    	if(alamad != null){
                            for (int u = 0; u < alamad.getChildNodes().getLength(); u++){
                           	  Node alam = alamad.getChildNodes().item(u);
                           	 if(alam.getNodeName().equals("item")){
                           		 String agroup  = getNode(alam,"productGroupID").getFirstChild().getNodeValue();
                           		  String aname  =  "noname " + agroup;
                           		  try{
                           			  aname = getNode(alam,"name").getFirstChild().getNodeValue();
                                  }catch(Exception t ){
                           		  }
                           		 agroup = deCode(agroup);
                           		 if(subgr){
                           			 agroup = addLeadingZero(agroup); 
                           		 }
                           		  aname  =  deCode(aname);
                           		 if(!subgr){
                           			tm.put(group + "." + agroup, Converter.enCodeEst(aname)); 
                           			tm.put(aname, Converter.enCodeEst(aname)); 
                                     
                           		 }
                           		    groupList.put(agroup, group + "." + agroup);
                           		 groupNameList.put(agroup, aname);
                                    groupListSec.put(group + "." + agroup, Converter.enCodeEst(aname));
                                    
                                    
                                    Node alamad2 = getNode(alam, "subGroups");
                                //     	System.out.println("ridu2  " + read.getChildNodes().getLength() + read.toString());
                                	if(alamad2 != null){
                                        for (int w = 0; w < alamad2.getChildNodes().getLength(); w++){
                                       	  Node alam2 = alamad2.getChildNodes().item(w);
                                       	 if(alam2.getNodeName().equals("item")){
                                       		String a2group  = getNode(alam2,"productGroupID").getFirstChild().getNodeValue();
                                     		  String a2name  = "noname " + a2group;
                                     		  try{
                                     			a2name = getNode(alam2,"name").getFirstChild().getNodeValue();
                                     		  }catch(Exception t){
                                     			  
                                     		  }
                                     		 
                                     		 if(subgr){
                                       			 a2group = addLeadingZero(a2group); 
                                       		 }
                                     		  a2group = deCode(a2group);
                                     		  a2name  =  deCode(a2name);
                                     		 if(!subgr){
                                     			tm.put(group + "." + agroup + "." + a2group, Converter.enCodeEst(a2name));
                                     			tm.put(a2name, Converter.enCodeEst(a2name)); 
                                              }	
                                     		  groupList.put(a2group, group + "." + agroup + "." + a2group);
                                     		 groupNameList.put(a2group, a2name);
                                              groupListSec.put(group + "." + agroup + "." + a2group, Converter.enCodeEst(a2name));
                                                          
                                              Node alamad3 = getNode(alamad2, "subGroups");
                                        //   	System.out.println("ridu2  " + read.getChildNodes().getLength() + read.toString());
                                      	if(alamad3 != null){
                                              for (int y = 0; y < alamad3.getChildNodes().getLength(); y++){
                                             	  Node alam3 = alamad3.getChildNodes().item(y);
                                             	 if(alam3.getNodeName().equals("item")){
                                             		String a3group  = getNode(alam3,"productGroupID").getFirstChild().getNodeValue();
                                           		  String a3name  =  getNode(alam3,"name").getFirstChild().getNodeValue();
                                           		 if(subgr){
                                           			 a3group = addLeadingZero(a3group); 
                                           		 }
                                           		 a3group = deCode(a3group);
                                           		  a3name  =  deCode(a3name);
                                            
                                           		 if(!subgr){
                                           			tm.put(group + "." + agroup + "." + a2group + "." + a3group, Converter.enCodeEst(a3name)); 
                                           			tm.put(a3name, Converter.enCodeEst(a3name)); 
                                           		 }
                                           		 
                                          			groupList.put(a3group, group + "." + agroup + "." + a2group +"." + a3group);
                                          			groupNameList.put(a3group, a3name);
                                                    groupListSec.put(group + "." + agroup + "." + a2group +"." + a3group
                                                    		, Converter.enCodeEst(a3name));
                                                    
                                             	 } 
                                                } 
                                        	  }
                                            } 
                                      	 } 
                                  	 }
                                 }
                            }	
                    	}
                             	
                  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 
          	  }
           	  }     
           	  
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  	//System.out.println( "grlist " + groupList.toString());
 //	System.out.println("tm " + tm.toString());
         return tm;
   }

   
   public TreeMap getProdGroupsSecond() throws SQLException {
	   
	   if("true".equals(getParent().getProps().getProperty("brands.group"))){
		   TreeMap   tm = new TreeMap();
		  	try {
		  		  File f = new File (getDir() + "brandserp.xml");
		  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     DocumentBuilder db;
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		          Document doc = db.parse(in,handler);
		      	
		        Node root = getNode(doc, "document");      
		         // System.out.println(root);
		       	Node read = getNode(root, "records");
		      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
		           for (int i = 0; i < read.getChildNodes().getLength(); i++){
		           	  Node rida = read.getChildNodes().item(i);
		           	  if(rida.getNodeName().equals("item")){
		          	  try{
		          		 String group  = getNode(rida,"brandID").getFirstChild().getNodeValue();
	               		 String name  =  getNode(rida,"name").getFirstChild().getNodeValue();
	               		 group = deCode(group);
	               		  name  =  deCode(name);
	                        tm.put("2." + group,Converter.enCodeEst(name));  
		          	  }catch (Exception r){
		          		  r.printStackTrace();
		          	  }
		          	  }             	  
	                 }
		           }catch (Exception r){
		          		r.printStackTrace();  
		          	  }
		           return tm;
	   
	   } else if("true".equals(getParent().getProps().getProperty("supplier.group"))){
		   
		   TreeMap   tm = suppliersTM;
		  	           return tm;	   
	   } else if("true".equals(getParent().getProps().getProperty("group.usesub"))){
		   return this.groupListSec;
		     	   
	   }else{
		   return null;
	   }
	   
	   
        }


   private TreeMap suppliersTM = null;

   private String enCode(String res ){
	  	 

	  	res =res.replaceAll("�","11111xx");
	  	res =res.replaceAll("�","11112xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11113xx");
		res =res.replaceAll("�","e");
		res =res.replaceAll("�"," "); 
		
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res = res.replaceAll("�"," ");
		res = res.replaceAll("�", " ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�","e");
		res =res.replaceAll("�","E");
		res =res.replaceAll("�"," ");
		

		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		res =res.replaceAll("�"," ");
		
		
		res = res.replaceAll("�", "EUR");
		res = res.replaceAll("�", "Nael");
	
		res =res.replaceAll("�","11115xx");
		res =res.replaceAll("�","11116xx");
		res =res.replaceAll("�","11117xx");
		res =res.replaceAll("�","11118xx");
		
		res =res.replaceAll("�","11119xx");
		res =res.replaceAll("�","11120xx");
		res =res.replaceAll("�","11121xx");
		res =res.replaceAll("�","11122xx");

	 	 
	  	return res;
	  }
	  
   
   public static String deCode(String res ){
	   
	   while(res.indexOf("11111xx") != -1){
	   	int aa = res.indexOf("11111xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   }
	   while(res.indexOf("11112xx") != -1){
	   	
	   	int aa = res.indexOf("11112xx");
	   //	System.out.println(aa + " " +res);
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11113xx") != -1){
	   	int aa = res.indexOf("11113xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11114xx") != -1){
	   	int aa = res.indexOf("11114xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11115xx") != -1){
	   	int aa = res.indexOf("11115xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11116xx") != -1){
	   	int aa = res.indexOf("11116xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11117xx") != -1){
	   	int aa = res.indexOf("11117xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 
	   while(res.indexOf("11118xx") != -1){
	   	int aa = res.indexOf("11118xx");
	   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
	   } 

	 
 	return res;
 }
   TreeMap agents = null;
   
   private void sendAsi(){
	   eevaeksport.util.ReportingService serv = new ReportingService();
	   
		serv.user =  getParent().getProps().getProperty("rest.user");
		serv.pass = getParent().getProps().getProperty("rest.pass");
		serv.company = getParent().getProps().getProperty("rest.company");
		

		Vector lines = new Vector();
		Product pp = new Product("kast");
		pp.setInCases(33);
		lines.add(pp);

		Product pp1 = new Product("kast");
		pp1.setInCases(55);
		lines.add(pp1);
		
	  //saadame tagastus dokumnendi 
	
				try {
				serv.addNewShipDoc(null, "ida",
						null, null, null,"SHIPIT",lines, "magi" , new Date(),null,null);
				}catch(Exception r){
					r.printStackTrace();
				}
			
		serv.closeClient();	

   }
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // this.getFixedAssortmentData();
    
    // sendAsi();
     agents = new TreeMap();
    getProdGroups();
     StringBuffer inputLineBuf = new StringBuffer();
	try {
		inputLineBuf = postData("request=getEmployees&recordsOnPage=" + onpage + userstuff);
	} catch (Exception e1) {
		e1.printStackTrace();
	}
     if(inputLineBuf.length() > 2){
         
         try {
       	  File f = new File (getDir() + "tootajad.xml");
       	  if(f.exists())f.delete();
       	  
       	  
       	Writer os = new BufferedWriter(new OutputStreamWriter( 
			    new FileOutputStream(getDir() + "tootajad.xml"), "UTF-8"));
		
		//PrintWriter os = new PrintWriter(new FileWriter(getDir() + "kliendiderp" + counter + ".xml",true));
		os.write(inputLineBuf.toString().replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' '));
		os.close();
       	  
   		  }catch (IOException ex) {
   		ex.printStackTrace();
   	  }
    }
   
     // loene v�imalikud agendid sisse 
   
     try {
 		inputLineBuf = postData("request=getCustomerGroups&recordsOnPage=" + onpage + userstuff);
 	} catch (Exception e1) {
 		e1.printStackTrace();
 	}
      if(inputLineBuf.length() > 2){
          
          try {
        	  File f = new File (getDir() + "kliendigrupid.xml");
        	  if(f.exists())f.delete();
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() + "kliendigrupid.xml", true));
    		os.write(inputLineBuf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
    
    
      Calendar cc = Calendar.getInstance();
      cc.add(Calendar.DAY_OF_MONTH, -3);
      
      
      try {
   		inputLineBuf = postData("request=getAssignments&changedSince=" + (cc.getTimeInMillis()/1000l) + "&recordsOnPage=" + onpage + userstuff);
   	} catch (Exception e1) {
   		e1.printStackTrace();
   	}
        if(inputLineBuf.length() > 2){
            
            try {
          	  File f = new File (getDir() + "tood.xml");
          	  if(f.exists())f.delete();
      		PrintWriter os = new PrintWriter(new FileWriter(
      				getDir() + "tood.xml", true));
      		os.write(inputLineBuf.toString());
      		os.close();
      	  }catch (IOException ex) {
      		ex.printStackTrace();
      	  }
       }
      
      
      
     
 	try {
 		  File f = new File (getDir() + "tootajad.xml");
 		  
 		    Properties propse =  new Properties();
 		    try{
 		    	propse =  EksportFrame.loadProps("emails.txt");
 		  	  
 		    }catch(Exception e){
 		    	e.printStackTrace();
 		    }
 		
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "document");      
      //   System.out.println(root);
     	Node read = getNode(root, "records");
    // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
         for (int i = 0; i < read.getChildNodes().getLength(); i++){
         	  Node rida = read.getChildNodes().item(i);
         	  if(rida.getNodeName().equals("item")){

             	  try{
             		  
             		//	System.out.println("rida  " + rida.getNodeName() );
             		// getNode(getNode(getNode(root, "Order-Parties"), "Buyer"),"CodeBySupplier"));
             		 String salesman  =  getNode(rida, "employeeID").getFirstChild().getNodeValue(); 
             		
             		 Agent agent = new Agent (salesman);
             		 String name  = deCode(getNode(rida, "employeeName").getFirstChild().getNodeValue());        		
             		agent.setName(name);
             		 
             	      agent.setIsPilot(true);
                      agent.setLanguage("EST");
                      agent.setCountry("ee");
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
                    
             		 
             		 
                     tm.put(name, agent); 
                     //getNode(rida,"item")
                     agents.put(salesman, name);	 
             	  }catch(Exception e){
             		  e.printStackTrace();
             	  }  
         	  }
              
         }
         
 	}catch(Exception e){
 		e.printStackTrace();
 	}
     
     System.out.println("Agendilist tehtud" );

//     System.out.println("Hinnakirju kasutusel "  + pricelists.size() +  " " + pricelists.toString() );

  //   System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());

    // fixedLines = getFixedAssortmentData1();
            
     
     Agent sm = new Agent ("HOUSE");
		       		
		 sm.setName("OFFICE");
     tm.put("HOUSE", sm); 
   //  setBulkSales(tm);
     
     
     if(rcompany != null){
	    	//Converter.sendAgentJson(tm, rcompany, false);
	    }
     
     return tm;
   }

   
   private StringBuffer removeTags(String input , String tag){
	   StringBuffer res  = new StringBuffer();
	   int len = tag.length();
	   while (input.indexOf("<" + tag + ">") != -1){
		   int a = input.indexOf("<" + tag + ">");
		   res.append(input.substring(0,a));
		   input = input.substring(a + len + 1);
		  // System.out.println("vahe " + input.substring(0,100));
		   if(input.indexOf("</" +tag + ">") != -1){
			   input = input.substring(input.indexOf("</" +tag + ">") + len + 3);
		   }
		   //System.out.println("vahe2 " + input.substring(0,100));
	   }
	   
	  res.append(input);
	   return res ;
   }
   
private String getMonth(){
	Calendar cal = Calendar.getInstance();
	String res = cal.get(Calendar.YEAR)+ "-";
	String mm = (cal.get(Calendar.MONTH )+1) +"";
	if(mm.length() == 1 )
		mm = "0" + mm;
	
	return res + mm + "-01";
}
   

private String laoID = "2";


public static String getDataDirStr(){
	StringBuffer sb = new StringBuffer();

	Calendar	cal = Calendar.getInstance();
	
	
	String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	if (day.length() == 1) {day = "0" + day;}

	String minute = String.valueOf(cal.get(Calendar.MINUTE));
	if (minute.length() == 1) {minute = "0" + minute;}

	String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
	if (month.length() == 1){month = ("0") + month;}

	sb.append(("" + cal.get(Calendar.YEAR)).substring(2,4));
	sb.append("" + month);
	sb.append(day +"");
	sb.append(minute +"");
	return sb.toString();
}


   private void makeData(){
	   
	   System.out.println("copy data dir " + getParent().getProps().getProperty("copydatadir"));
	   if("true".equals(getParent().getProps().getProperty("copydatadir"))){        	   
    	 File ff = new File(getDir());
    	 File fs = new File(getDir() + getDataDirStr() );
    	 if(!fs.exists()){
    		 fs.mkdir();
    	 }
    	 
    	
         File[] listOfFiles = ff.listFiles();

         for (int i = 0; i < listOfFiles.length; i++) {
        	 System.out.println("copy data dir " + listOfFiles[i].getName());
             if (listOfFiles[i].isFile()) {
                  File f = new File(getDir() +listOfFiles[i].getName()); 
                    f.renameTo(new File(fs.getAbsolutePath()+"/"+ listOfFiles[i].getName()));
             }
         }
    	 
       }
	    
	   
	   
       try{
           StringBuffer inputLineBuf = new StringBuffer();
       	try {
    		inputLineBuf = postData("request=getProductGroups" + userstuff);
    	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
           if(inputLineBuf.length() > 2){          
                 try {
               	  File f = new File (getDir() + "tootegrupiderp.xml");
               	  if(f.exists())f.delete();
           	
				 Converter.saveFileInUTF8(getDir() + "tootegrupiderp.xml", inputLineBuf.toString());
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
          
           
                inputLineBuf = new StringBuffer();
           	try {
        		inputLineBuf = postData("request=getProductPriorityGroups" + userstuff);
        	} catch (Exception e1) {
        		e1.printStackTrace();
        	}
               if(inputLineBuf.length() > 2){          
                     try {
                   	  File f = new File (getDir() + "productPriorityGroups.xml");
                   	  if(f.exists())f.delete();
               	
    				 Converter.saveFileInUTF8(getDir() + "productPriorityGroups.xml", inputLineBuf.toString());
               	  }catch (Exception ex) {
               		ex.printStackTrace();
               	  }
                }
              
            
           
                  
                  inputLineBuf = new StringBuffer();
                	try {
             		inputLineBuf = postData("request=getAssignments" + userstuff);
             	} catch (Exception e1) {
             		e1.printStackTrace();
             	}
                    if(inputLineBuf.length() > 2){          
                          try {
                        	  File f = new File (getDir() + "assignments.xml");
                        	  if(f.exists())f.delete();
                    	
         				 Converter.saveFileInUTF8(getDir() + "assignments.xml", inputLineBuf.toString());
                    	  }catch (Exception ex) {
                    		ex.printStackTrace();
                    	  }
                     }  
           
           try {
       		inputLineBuf = postData("request=getBrands" + userstuff);
       	} catch (Exception e1) {
       		e1.printStackTrace();
       	}
              if(inputLineBuf.length() > 2){          
                    try {
                  	  File f = new File (getDir() + "brandserp.xml");
                  	  if(f.exists())f.delete();
              	
   				 Converter.saveFileInUTF8(getDir() + "brandserp.xml", inputLineBuf.toString());
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }
               }

              
              
              
              FileListFilter fil = new FileListFilter("prlisterp","xml");
 	    	 String[] ll = new File (getDir()).list(fil);
 	    	 for (int i = 0; i < ll.length; i++) {
 	    		 String name = ll[i];
 		    		// System.out.println(name);
 		 		  File f = new File (getDir() + name);
 	           if(f.exists()){
 	        	   f.delete();
 	           }
 	    	 }
              
              try {
             		inputLineBuf = postData("request=getPriceLists&recordsOnPage=" + onpage  + userstuff);
             	} catch (Exception e1) {
             		e1.printStackTrace();
             	}
                                       
                    int tsykkel = 0;
            		if (inputLineBuf.indexOf("recordsTotal") != -1) {
            			int lines = inputLineBuf.indexOf("recordsTotal");
            			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
            			// System.out.println(inputLineBuf);
            			try {
            				tsykkel = Integer.parseInt(inputLineBuf.substring(
            						lines + 13, end - 2));
            			} catch (Exception e) {

            			}
            		}
            		if (inputLineBuf.length() > 2) {
            			try {
            				File f = new File(getDir() + "prlisterp1.xml");
            				if (f.exists())
            					f.delete();
            				 String c = (char)0x19 + "";
            				 Converter.saveFileInUTF8(getDir() + "prlisterp1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
            						 .replace((char)0x19, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0x19, ' ')
            						 .replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
            						 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' '));
            		       
            			} catch (Exception ex) {
            				ex.printStackTrace();
            			}
            		}

            		if (tsykkel > onpage) {
            			int counter = 2;
            			for (; tsykkel > onpage; tsykkel = tsykkel - onpage) {
            				try {
            					inputLineBuf = postData("request=getPriceLists&recordsOnPage=" + onpage + "&pageNo="
            							+ counter + userstuff);
            				} catch (Exception e1) {
            					e1.printStackTrace();
            				}

            				if (inputLineBuf.length() > 2) {
            					try {
            						File f = new File(getDir() + "prlisterp" + counter
            								+ ".xml");
            						if (f.exists())
            							f.delete();
            						
            						 Converter.saveFileInUTF8(getDir() + "prlisterp" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
            								 .replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0x19, ' ')
            								 .replace((char)0xe, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
            								 .replace((char)0x1c, ' ')
            								 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' '));
            					
            					} catch (Exception ex) {
            						ex.printStackTrace();
            					}
            				}

            				counter++;
            			}

            		}

                    

            		   
                    fil = new FileListFilter("appliances","xml");
       	    	  ll = new File (getDir()).list(fil);
       	    	 for (int i = 0; i < ll.length; i++) {
       	    		 String name = ll[i];
       		    		// System.out.println(name);
       		 		  File f = new File (getDir() + name);
       	           if(f.exists()){
       	        	   f.delete();
       	           }
       	    	 }
                    
                    try {
                   		inputLineBuf = postData("request=getAppliances&recordsOnPage=" + onpage  + userstuff);
                   	} catch (Exception e1) {
                   		e1.printStackTrace();
                   	}
                                             
                           tsykkel = 0;
                  		if (inputLineBuf.indexOf("recordsTotal") != -1) {
                  			int lines = inputLineBuf.indexOf("recordsTotal");
                  			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
                  			// System.out.println(inputLineBuf);
                  			try {
                  				tsykkel = Integer.parseInt(inputLineBuf.substring(
                  						lines + 13, end - 2));
                  			} catch (Exception e) {

                  			}
                  		}
                  		if (inputLineBuf.length() > 2) {
                  			try {
                  				File f = new File(getDir() + "appliances1.xml");
                  				if (f.exists())
                  					f.delete();
                  				 String c = (char)0x19 + "";
                  				 Converter.saveFileInUTF8(getDir() + "appliances1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
                  						 .replace((char)0x19, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0x19, ' ')
                  						 .replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
                  						 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' '));
                  		       
                  			} catch (Exception ex) {
                  				ex.printStackTrace();
                  			}
                  		}

                  		if (tsykkel > onpage) {
                  			int counter = 2;
                  			for (; tsykkel > onpage; tsykkel = tsykkel - onpage) {
                  				try {
                  					inputLineBuf = postData("request=getAppliances&recordsOnPage=" + onpage + "&pageNo="
                  							+ counter + userstuff);
                  				} catch (Exception e1) {
                  					e1.printStackTrace();
                  				}

                  				if (inputLineBuf.length() > 2) {
                  					try {
                  						File f = new File(getDir() + "appliances" + counter
                  								+ ".xml");
                  						if (f.exists())
                  							f.delete();
                  						
                  						 Converter.saveFileInUTF8(getDir() + "appliances" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
                  								 .replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0x19, ' ')
                  								 .replace((char)0xe, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
                  								 .replace((char)0x1c, ' ')
                  								 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' '));
                  					
                  					} catch (Exception ex) {
                  						ex.printStackTrace();
                  					}
                  				}

                  				counter++;
                  			}

                  		}

                          

            		
            		
            		
                    
                    
           //l�peb tootegruppidega tegelemine 
          // System.out.println("Tootegrupid tehtud" );
           System.out.println("Teen Dokyumendi listi" );   
           
           FileListFilter fil2 = new FileListFilter("doklist","xml");
	    	 String[] ll2 = new File (getDir()).list(fil2);
	    	 for (int i = 0; i < ll2.length; i++) {
	    		 String name = ll2[i];
		    		// System.out.println(name);
		 		  File f = new File (getDir() + name);
	           if(f.exists()){
	        	   f.delete();
	           }
	    	 }
	    		
	    		boolean doclist = true;
	    	 if("true".equals(getParent().getProps().getProperty("nodoclist"))){        	   
	    		 doclist = false;
	           }

           if(doclist){
        	
         String  data = "request=getSalesDocuments&recordsOnPage=" + onpage + "&dateFrom=" + getMonth() + "&type=INVWAYBILL"
				+ userstuff;// &dateFrom=2010-12-01
		// System.out.println(data);

		inputLineBuf = postData(data);
		// System.out.println(inputLineBuf.toString());

		 tsykkel = 0;
		if (inputLineBuf.indexOf("recordsTotal") != -1) {
			int lines = inputLineBuf.indexOf("recordsTotal");
			int end = inputLineBuf.indexOf("recordsTotal", lines + 12);
			// System.out.println(inputLineBuf);
			try {
				tsykkel = Integer.parseInt(inputLineBuf.substring(
						lines + 13, end - 2));
			} catch (Exception e) {

			}
		}
		if (inputLineBuf.length() > 2) {
			try {
				File f = new File(getDir() + "doklist1.xml");
				if (f.exists())
					f.delete();
				 String c = (char)0x19 + "";
				 Converter.saveFileInUTF8(getDir() + "doklist1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
						 .replace((char)0x19, ' ').replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0xe, ' ').replace((char)0xe, ' ').replace((char)0x19, ' ')
						 .replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
						 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' '));
		       
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (tsykkel > onpage) {
			int counter = 2;
			for (; tsykkel > onpage; tsykkel = tsykkel - onpage) {
				try {
					inputLineBuf = postData("request=getSalesDocuments&recordsOnPage=" + onpage + "&type=INVWAYBILL&dateFrom=" + getMonth() + "&pageNo="
							+ counter + userstuff);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				if (inputLineBuf.length() > 2) {
					try {
						File f = new File(getDir() + "doklist" + counter
								+ ".xml");
						if (f.exists())
							f.delete();
						
						 Converter.saveFileInUTF8(getDir() + "doklist" + counter + ".xml", inputLineBuf.toString().replace((char)0x19, ' ')
								 .replace((char)0x1f, ' ').replace((char)0x1f, ' ').replace((char)0x19, ' ')
								 .replace((char)0xe, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ')
								 .replace((char)0x1c, ' ')
								 .replace((char)0x1d, ' ').replace((char)0x1d, ' ').replace((char)0x1d, ' '));
					
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				counter++;
			}

		}
           
		   
           }
           
           
           System.out.println("Laod alustan" );
       	try {
    		inputLineBuf = postData("request=getWarehouses" + userstuff);
    	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
           if(inputLineBuf.length() > 2){          
                 try {
               	  File f = new File (getDir() + "laoderp.xml");
               	  if(f.exists())f.delete();
               	 Converter.saveFileInUTF8(getDir() + "laoderp.xml", inputLineBuf.toString());
           	
           	  }catch (Exception ex) {
           		ex.printStackTrace();
           	  }
            }
          
           
           
           
       	boolean soldsort = true;
   	 if("true".equals(getParent().getProps().getProperty("nosoldsort"))){        	   
   		soldsort = false;
          }

           if(soldsort){
        	
           System.out.println("kliendisortimendid alustan" );
           
           try {
       		inputLineBuf = postData("request=getReportSalesRows" + userstuff);
       	 if(inputLineBuf.indexOf("<errorCode>") != -1 ){
       		inputLineBuf = postData("request=getReportSalesRows" + userstuff);
         }
       	if(inputLineBuf.indexOf("<errorCode>") != -1 ){
       		inputLineBuf = postData("request=getReportSalesRows" + userstuff);
         }
       		
       	} catch (Exception e1) {
       		e1.printStackTrace();
       	}
              if(inputLineBuf.length() > 2){          
                    try {
                  	  File f = new File (getDir() + "klienditoodeErp.xml");
                  	  if(f.exists())f.delete();
                  	  Converter.saveFileInUTF8(getDir() + "klienditoodeErp.xml", Converter.deCode(inputLineBuf.toString()));
              	 }catch (Exception ex) {
              		ex.printStackTrace();
              	  }
               }
           
           System.out.println("kliendisortimendid tehtud" );
           //algab Hinnakirja faili moodustamine
           
           }
           
           System.out.println("Algab toodete moodustamine" );
            tsykkel = 0;
           
           inputLineBuf = new StringBuffer();
           boolean cut = true;
           if("true".equals(getParent().getProps().getProperty("nocut"))){        	   
        	   cut = false;
           }
           String luba = "";
           if(getParent().getProps().getProperty("okgrups") != null){
                luba =  "&groupIDsWithSubgroups=" + getParent().getProps().getProperty("okgrups") ;  
           }
          
           if(getParent().getProps().getProperty("okstatus") != null){
               luba = luba + "&status=" + getParent().getProps().getProperty("okstatus") ;  
          }
           
        	
       	try {
    		inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage +
    				"&getPackageInfo=1&getStockInfo=1&getContainerInfo=1" + luba + userstuff); //&getStockInfo=1
    		//inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getStockInfo=1&getContainerInfo=1&warehouseID="+ laoID + userstuff); //&getStockInfo=1
    		
    	//	System.out.println("joru1 " + inputLineBuf.substring(0,100));
    		if(cut){
    				inputLineBuf = removeTags(inputLineBuf.toString(),"manufacturerName");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"brandName");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"packages");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"description");
    	    		inputLineBuf = removeTags(inputLineBuf.toString(),"longdesc");	  
    		  }
    		//System.out.println("joru2 " + inputLineBuf.substring(0,100));
        	
       	} catch (Exception e1) {
    		e1.printStackTrace();
    	}
        
           
         if(inputLineBuf.indexOf("recordsTotal") != -1 ){
        	 int lines = inputLineBuf.indexOf("recordsTotal");
        	 int end = inputLineBuf.indexOf("recordsTotal",lines+12);
        	
        	 try{
        		 tsykkel = Integer.parseInt(inputLineBuf.substring(lines+13,end-2));
        	
        		 //kuion olemas siis vanad failid maha.
        		 if(tsykkel != 0){
        			 fil = new FileListFilter("tootederp","xml");
         	    	 ll = new File (getDir()).list(fil);
         	    	 for (int i = 0; i < ll.length; i++) {
         	    		 String name = ll[i];
         		    		// System.out.println(name);
         		 		  File f = new File (getDir() + name);
         	           if(f.exists()){
         	        	   f.delete();
         	           }
         	    	 }	 
        		 }
        		 
        	 }catch(Exception e){
        		 
        	 }
         }
        // System.out.println("Tooted Tsykkel " + tsykkel );
           if(inputLineBuf.length() > 2){        
               try {
             	  File f = new File (getDir() + "tootederp1.xml");
             	  if(f.exists())f.delete();
         		 Converter.saveFileInUTF8(getDir() + "tootederp1.xml", inputLineBuf.toString().replace((char)0x1d, ' ').replace((char)0x1d, ' '));
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
           
           if(tsykkel > onpage ){
        	  int counter = 2;
        	   for(;tsykkel > onpage;tsykkel = tsykkel-onpage){
        		   try {
        	    		inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&pageNo=" + counter + luba + userstuff); //&getStockInfo=1
        	    		//inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&warehouseID=" + laoID + "&pageNo=" + counter + userstuff); //&getStockInfo=1
        	    		if(inputLineBuf.toString().indexOf("1054") != -1 && inputLineBuf.toString().indexOf("error") !=-1){
        	    			 session = getSessionId(passString);
        	    				System.out.println("uus session " + session);
        	    			   while(session.indexOf("%") != -1){
        	    				   session = getSessionId(passString);
        	    			   }
        	    			    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
        	    			    inputLineBuf = postData("request=getProducts&recordsOnPage=" + onpage + "&getPackageInfo=1&getContainerInfo=1&getStockInfo=1&pageNo=" + counter  + luba + userstuff); //&getStockInfo=1
                	    			    
        	    		}
        	    		
        	    		if(cut){
        	    				inputLineBuf = removeTags(inputLineBuf.toString(),"manufacturerName");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"brandName");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"packages");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"description");
                	    		inputLineBuf = removeTags(inputLineBuf.toString(),"longdesc");	 
        	    		 }
        	    	
        		   } catch (Exception e1) {
        	    		e1.printStackTrace();
        	    	}  
        	    	if(inputLineBuf.length() > 2){        
        	               try {
        	             	  File f = new File (getDir() + "tootederp" + counter + ".xml");
        	             	  if(f.exists())f.delete();
        	         
        	       	    	 Converter.saveFileInUTF8(getDir() + "tootederp" + counter + ".xml", inputLineBuf.toString());
        	               }catch (Exception ex) {
        	         		ex.printStackTrace();
        	         	  }
        	          }

        	    	counter++;
        	   }
        	   
           }
         
           
           System.out.println("l�petatud toodete moodustamine" );
           System.out.println("algab klientide moodustamine" ); 
           inputLineBuf = new StringBuffer();
           
        
           
           try {
        	   String custgrupp = "";
        	   if(getParent().getProps().getProperty("customer.group") != null){
        		   custgrupp =  "&groupIDWithSubgroups=" + getParent().getProps().getProperty("customer.group");  
        	   }
       		inputLineBuf = getData("request=getCustomers&recordsOnPage=" + onpage + "&getAddresses=1&responseMode=detail"+ custgrupp + userstuff);
       	
       
       	   } catch (Exception e1) {
       		e1.printStackTrace();
       	   }
       
       	   
       	   tsykkel = 0;
        if(inputLineBuf.indexOf("recordsTotal") != -1 ){
       	 int lines = inputLineBuf.indexOf("recordsTotal");
       	 int end = inputLineBuf.indexOf("recordsTotal",lines+12);
       	
       	 try{
       		 tsykkel = Integer.parseInt(inputLineBuf.substring(lines+13,end-2));
       	//kui �nnestub siis kustutame maha
       		 if(tsykkel != 0){
       			fil = new FileListFilter("kliendiderp","xml");
    	    	 ll = new File (getDir()).list(fil);
    	    	 for (int i = 0; i < ll.length; i++) {
    	    		 String name = ll[i];
    		    		// System.out.println(name);
    		 		  File f = new File (getDir() + name);
    	           if(f.exists()){
    	        	   f.delete();
    	           }
    	    	 } 
    	    	   if(inputLineBuf.length() > 2){
    	        	   try {
    	              	  File f = new File (getDir() + "kliendiderp1.xml");
    	              	  if(f.exists())f.delete();
    	          	
    	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp1.xml", inputLineBuf.toString().replace((char)0x1f, ' ').replace((char)0x1f, ' '));
         	            
    	          	  }catch (Exception ex) {
    	          		ex.printStackTrace();
    	          	  }
    	            }
    	    	 
       		 }
        		
       		 
       	 }catch(Exception e){
       		 
       	 }
       	 //System.out.println("Klientide tsykkel " + tsykkel);
       	 
        }
        
         
           
           if(tsykkel > onpage ){
         	  int counter = 2;
         	   for(;tsykkel > onpage;tsykkel = tsykkel-onpage){
         		  try {
         			//  System.out.println("request=getCustomers&recordsOnPage=100&responseMode=detail&pageNo=" + counter + userstuff);
         	       		inputLineBuf = getData("request=getCustomers&recordsOnPage=" + onpage + "&getAddresses=1&responseMode=detail&pageNo=" + counter + userstuff);
         	       	   } catch (Exception e1) {
         	       		e1.printStackTrace();
         	       	   }
         	        
         		 if(inputLineBuf.toString().contains("errorCode>1054")){
	    			 session = getSessionId(passString);
	    				System.out.println(session);
	    			   while(session.indexOf("%") != -1){
	    				   session = getSessionId(passString);
	    			   }
	    			    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
	    			    inputLineBuf = getData("request=getCustomers&recordsOnPage=" + onpage + "&getAddresses=1&responseMode=detail&pageNo=" + counter + userstuff);
	         	       	   	    
	    		}
         	    	if(inputLineBuf.length() > 2){        
         	               try {
         	             	  File f = new File (getDir() + "kliendiderp" + counter + ".xml");
         	             	  if(f.exists())f.delete();
         	         	
       	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp" + counter + ".xml", inputLineBuf.toString().replace((char)0xe, ' ')
       	          			.toString().replace((char)0xe, ' ')
       	          		.replace((char)0x1f, ' ')
       	          	.replace((char)0x1f, ' '));
            	        
         	         	  }catch (Exception ex) {
         	         		ex.printStackTrace();
         	         	  }
         	          }

         	    	counter++;
         	   }
         	   
            }
           
           
           
           System.out.println("l�petatud klientide moodustamine" );

       
       }catch(Exception e){
       	e.printStackTrace();
       }

	   
   }

   private  Node getNode(Node node, String name){
	    for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	      Node nd = node.getChildNodes().item(i);
	  //    System.out.println(nd.getNodeName());
	      if (nd.getNodeName().equals(name)){
	        return nd;
	      }
	    }
	    return null;
	  }

        	  private FileInputStream getCleanInputStream(File xmlFile)throws FileNotFoundException, IOException {
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
        	//   System.out.println("Skipping BOM, " + bomKount + " bytes");
        	  return bomKount;
        	  } 
        	  
        	  
       public String getDistribution() {
         return null;
       }

private boolean useOldData = false;

   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     HashSet asses = new HashSet();
	     HashSet custs = new HashSet();

	     boolean makedata = true;
		 if("true".equals(getParent().getProps().getProperty("use.olddata")) || useOldData){
			 makedata = false;
		 }
		 
	     if(makedata){
	    	 makeData();
	 	 }
	    
	     sortimendid = new HashMap();
		  ketid = new HashMap();
	     
	     this.customers = new ArrayList();
	   
	   try {
		   
		   FileListFilter fil = new FileListFilter("kliendiderp","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String name = ll[i];
	    		// System.out.println(name);
	 		  File f = new File (getDir() + name);
		   
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "document");      
	     	Node read = getNode(root, "records");
	     	 for (int s = 0; s < read.getChildNodes().getLength(); s++){
	         	  Node rida = read.getChildNodes().item(s);
	         	  try{
	         		 if(rida.getNodeName().equals("item")){
	         			boolean goes = true;
	         		  String id =  deCode(getNode(rida,"customerID").getFirstChild().getNodeValue());
	         		  Customer customer = new Customer(id);
	         		  try{
	         			 String kett = deCode(getNode(rida,"code").getFirstChild().getNodeValue());;
		                 if(kett != null && kett.trim().length()>0){
		              	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
		                 		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett));
		                 		vv.add(customer.getID());
		                 		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
		                 	}else{
		                 		Vector vv = new Vector();
		                 		vv.add(customer.getID());
		                 		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
		                 	}
		                 }
	                    	  
	         		  }catch(Exception e){
	        
	         		  }
	         		
	         		  try{
	         		  String namec =   deCode(getNode(rida,"fullName").getFirstChild().getNodeValue());
	         		  customer.setName(Converter.enCodeEst(namec));
	         		  
                         }catch(Exception ee){
	                     customer.setName(id); 
                         }
                         
                         
    	                 String prlist = null;
    	                 try{ 
    	 	         		 prlist =   getNode(rida,"priceListID").getFirstChild().getNodeValue();
    	 	         		 if(prlist.equals("0")){
    	 	         			prlist = null; 
    	 	         		 }
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist == null){
    	                	 prlist = "";
    	                 }else{
    	                	 prlist = prlist + "|";
    	                 }
    	                 
    	                 String prlist2 = null;
    	                 try{ 
    	 	         		 prlist2 =   getNode(rida,"priceListID2").getFirstChild().getNodeValue();
    	 	         		if(prlist2.equals("0")){
    	 	         			prlist2 = null; 
    	 	         		 }
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist2 != null){
    	                	 prlist = prlist + prlist2 + "|";
    	                 }
    	                 
    	                 String prlist3 = null;
    	                 try{ 
    	 	         		 prlist3 =   getNode(rida,"priceListID3").getFirstChild().getNodeValue();
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist3.equals("0")){
 	 	         			prlist3 = null; 
 	 	         		 }
    	                
    	                 if(prlist3 != null){
    	                	 prlist = prlist + prlist3 + "|" ;
    	                 }
    	                 
    	                 String prlist4 = null;
    	                 try{ 
    	 	         		 prlist4 =   getNode(rida,"priceListID4").getFirstChild().getNodeValue();
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist4 != null && prlist4.equals("0")){
 	 	         			prlist4 = null; 
 	 	         		 }
    	                 
    	                 if(prlist4 != null){
    	                	 prlist = prlist + prlist4 + "|" ;
    	                 }
    	                
    	                 
    	                 String prlist5 = null;
    	                 try{ 
    	 	         		 prlist5 =   getNode(rida,"priceListID5").getFirstChild().getNodeValue();
    	 	        	 }catch(Exception ee){
    		        	 } 
    	                 if(prlist5!= null && prlist5.equals("0")){
 	 	         			prlist5 = null; 
 	 	         		 }
    	                 
    	                 if(prlist5 != null){
    	                	 prlist = prlist + prlist5 ;
    	                 }
    	                 
    	                
                         
    	                 if(prlist.endsWith("|")){
                        	 prlist = prlist.substring(0, prlist.length()-1);
                         }
                         if(prlist.length() >0 ){
                        	 customer.setGroup(prlist);
                        	 if("true".equals(getParent().getProps().getProperty("usepricematrix.fixed"))){
                        		 StringTokenizer st = new StringTokenizer(prlist,"|");
                        		 while(st.hasMoreTokens()){
                        			 customer.addFixSortCode(st.nextToken()); 
                        		 }
                     		 }
                         }
    	                 
	         		  try{ 
	 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
	 	         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(deCode(aadr)),"Estonia"));
	         		 }catch(Exception ee){
		         		 customer.setAddress(""); 
	         		 }
	         		
	         		  try{ 
		 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
		 	         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(deCode(aadr)),"Estonia"));
		         		 }catch(Exception ee){
			         		 customer.setAddress(""); 
		         		 }
		         		
	         		 try{ 
	 	         		 String street =   getNode(rida,"street").getFirstChild().getNodeValue();
	 	         		  customer.restStreet = Converter.enCodeEst(deCode(street));
	         		 }catch(Exception ee){
	         		 }
	         		 

	         		 try{ 
	 	         		 String city =   getNode(rida,"city").getFirstChild().getNodeValue();
	 	         		  customer.restCity = Converter.enCodeEst(deCode(city));
	         		 }catch(Exception ee){
	         		 }
	         		 
	         		 try{ 
	 	         		 String postalCode =   getNode(rida,"postalCode").getFirstChild().getNodeValue();
	 	         		  customer.restZipCode = Converter.enCodeEst(deCode(postalCode));
	 	         		  customer.setCountry(Converter.enCodeEst(deCode("Estonia")));
	         		 }catch(Exception ee){
	         		 }
	         		 

	         		 try{ 
	 	         		 String country =   getNode(rida,"country").getFirstChild().getNodeValue();
	 	         		
	 	         		customer.setCountry(Converter.enCodeEst(deCode(country)));
	         		 }catch(Exception ee){
	         			customer.setCountry(Converter.enCodeEst(deCode("Estonia")));
	         		 }
	         		  

		         		  try{ 
			 	         		 Node aadr =   getNode(rida,"addresses");
			 	         		
			 	         		 for (int ss = 0; ss < aadr.getChildNodes().getLength(); ss++){
			 	         			
			 	  	         	  Node uuu = aadr.getChildNodes().item(ss);
			 	  	         		 if(uuu.getNodeName().equals("item")){
			 	  	         		  String addrID =  deCode(getNode(uuu,"addressID").getFirstChild().getNodeValue());
					 	        	//  System.out.println("aadrID " + addrID);
				         			 }
			 	  	         	
				         		 
			         		 }
			 	  	          
			 	         		 
		         		 }catch(Exception ee){}
		         		 
	         	     
	         		  try{ 
	 	         		 String type =   getNode(rida,"customerType").getFirstChild().getNodeValue();
	 	         		  if(!type.equals("COMPANY")){
	 	         			  goes = false;
	 	         		  }
	         		 }catch(Exception ee){
		         	 
	         		 }
	         		  try{
	         		   String salesman  = deCode( getNode(rida,"customerManagerID").getFirstChild().getNodeValue());
       		            customer.addAgent(new Agent(salesman));
       		            this.custtsUsed.add(id);
	         		   }catch(Exception ee){
		         		 
	         		   }
	         		  customer.addAgent(new Agent("HOUSE"));
	         		 if("true".equals(getParent().getProps().getProperty("all.customers"))){
	         			Enumeration enume = getParent().getAgentsHash().elements();
	         				while(enume.hasMoreElements()){
	         					Agent a = (Agent)enume.nextElement();
	         					customer.addAgent(a);
	         				}
	         		}
	         		 
	         		 
	         		 try{
		         		  String email =   deCode(getNode(rida,"email").getFirstChild().getNodeValue());
		         		  if(email.length() > 0 ){
		         			 if(getParent().getProps().getProperty("confirm.email.domain") != null){
			         			 String ems = getParent().getProps().getProperty("confirm.email.domain");
			         			 
				         			StringTokenizer st = new StringTokenizer(ems,",");
				         			while (st.hasMoreTokens()){
				         				if(email.contains(st.nextToken())){
				         					customer.setConfirmEmail(email);	
				         					//customer.setConfirmEmail("aivar@mobilejava.ee");	
				         				}
				         			}
				         		 }  
		         		  }
		         		
		         		  
	                         }catch(Exception ee){
		                    
	                         }
	         		 
	         		 
	         		 
	         		 if(getParent().getProps().getProperty("all.customers.list") != null){
	         			 String ags = getParent().getProps().getProperty("all.customers.list");
	         			 
		         			StringTokenizer st = new StringTokenizer(ags,",");
		         			while (st.hasMoreTokens()){
		         				Agent a = new Agent(st.nextToken());
		         					customer.addAgent(a);
		         			}
		         			
		         		 }
	         		  
	         		  try{
	  	         		String paym  =  getNode(rida,"paymentDays").getFirstChild().getNodeValue();
	         		            customer.setPaymTerm(paym);
	  	         		 }catch(Exception ee){
	  		        	 }
	  	
	         		  
	          		 try{
		         			Node attr = getNode(rida, "attributes");
		         			if(attr != null){
		         			
		         			//	System.out.println("ridu  " + attr.getChildNodes().getLength() + attr.toString());
			        	         for (int u = 0; u < attr.getChildNodes().getLength(); u++){
			        	        	 try{
			        	        	 	 Node item = attr.getChildNodes().item(u);
				        	      //   System.out.println("name" + item.getNodeName());
				        	        //	System.out.println("value " + getNode(item,"attributeName").getFirstChild().getNodeValue());
				        	        	
				        	            String glnkood  =  getNode(item,"attributeName").getFirstChild().getNodeValue();
				    		            if(glnkood.equals("clattr_gln")){
				    		            	glnkood  =  getNode(item,"attributeValue").getFirstChild().getNodeValue();
				    		            	// this.ilns.put(glnkood.trim(), id);
				    		            	customer.GLNCode = glnkood;
				    		            	// System.out.println("olemas " + glnkood + " " + id);
				    		            }
				        	        	
				        	        	
				        	        	
			        	        	 }catch(Exception e){
			        	        		//e.printStackTrace(); 
			        	        	 }
			        	        }
			     			}
		             		   }catch(Exception ee){
				     		   }
		         		 
	         		  
	         		  
	         		  
	         		  
	         		  
	         		  
	         		  try{
		  	         		String payer  =  getNode(rida,"payerID").getFirstChild().getNodeValue();
		         		            customer.setPayer(payer);
		  	         		 }catch(Exception ee){
		  		        	 }
		  	         
		  	         		
		  	         		  if(getParent().getProps().getProperty("country") != null){
		  	         			 try{
		 		  	         		String country  =  getNode(rida,"country").getFirstChild().getNodeValue();
		 		  	         	         if(country != null && country.trim().length() > 0 && 
		 		         		        		 getParent().getProps().getProperty("country").indexOf(country) == -1){
		 		         		        	 goes = false;
		 		         		         }
		 		  	         		 }catch(Exception ee){
		 		  	         			 
		 		  		        	 }  
		  	         		  }
		  	         		 
	         		 if(goes){
	         			  tm.put(customer.getName(), customer);
		                     this.customers.add(customer.getID()); 
	         		 }
	         		   
	                     
	         		 }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	    	  }    
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
	   try {
	    	 getOutDir();
	 		Converter.makeTelemaFiles(this.getOutDir(), null, null, tm, ownGLN);
	 	} catch (Exception e) {
	 		
	 		e.printStackTrace();
	 	}
		
 try {
	      if(this.rcompany != null){
	    	  
	    	  Converter.sendCustomerJson(tm, rcompany, false,user,pass,false,false);
	    	
	    	  
	    	     //T�mbame alla k�ikmis meil olemas
	      eevaeksport.util.ReportingService servu = new ReportingService();
	  		servu.user =  user;
	  		servu.pass = pass;
	  		servu.company = rcompany;   
	  		JSONArray assets = servu.getAssets(null);
	  		servu.closeClient();
	 
	  		
	  		HashMap asscusts = new HashMap();
	  	     HashSet cuus = new HashSet();	
	  		for (int i = 0; i < assets.length(); i++) {
				 JSONObject cob = assets.getJSONObject(i);
					    String code = (String)cob.get("code");
						try{
				JSONObject cust  = (JSONObject)cob.get("customer");
							 if(cust != null){
								 String kkood = (String)cust.get("code");
								asscusts.put(code, kkood);
								cuus.add(code);
							 }	
						}catch(Exception e){
							
						}
			 }
		

	     	TreeMap sidusAC = new TreeMap();
	  	   FileListFilter fil = new FileListFilter("appliances","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		
	    		
	    		 String name = ll[i];
	    		// System.out.println(name);
	 		  File f = new File (getDir() + name);
		   
	 	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	    DocumentBuilder db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "document");      
	     	Node read = getNode(root, "records");
	     	
	     	System.out.println("AssesCusts size enne  " + asscusts.size());
	     	
	     	Vector vec = new Vector(); 
	     	int counter = 0 ;
            
	     	for (int s = 0; s < read.getChildNodes().getLength(); s++){
	     		  
	     		Node rida = read.getChildNodes().item(s);
	         	  	 
	         		 if(rida.getNodeName().equals("item")){
	         			String id =  deCode(getNode(rida,"applianceID").getFirstChild().getNodeValue());
	      	         			 
	         			 JSONObject obj = new JSONObject();
	                	 obj.put("code", id);
	                     asses.add(id);

	                     try{
	                        String aname = deCode(getNode(rida,"applianceName").getFirstChild().getNodeValue());
	                        if(aname == null){
	                        	aname = id;
	                        }else{
	                        	aname = aname.trim();
	                        }
	                        obj.put("name", aname);
	                        
	                     }catch(Exception e){
	                    	 try{
	 	                        String aname = deCode(getNode(rida,"productName").getFirstChild().getNodeValue());
	 	                        if(aname == null){
	 	                        	aname = id;
	 	                        }else{
	 	                        	aname = aname.trim();
	 	                        }
	 	                       obj.put("name", aname);
		                        
	                    	 }catch(Exception ed){
	                    		 obj.put("name", id + "-noname");
	 	                        }
	                        }
	                        
	                     
	                        
	                        try{
	                        	String ean = deCode(getNode(rida,"productCode2").getFirstChild().getNodeValue());
		                        if(ean != null && ean.trim().length() > 0){
		                        	   obj.put("ean", ean);
		                        }	
	                        }catch(Exception e){
	                        	
	                        }
	                        
	                        
	                        /*<attributes>
<item>
<attributeName>service_days</attributeName>
<attributeType>text</attributeType>
<attributeValue>90</attributeValue>
</item>
</attributes>*/ 
	                        
	                        
	                   	 try{
			         			Node attr = getNode(rida, "attributes");
			         			if(attr != null){
			         			
			         			//	System.out.println("ridu  " + attr.getChildNodes().getLength() + attr.toString());
				        	         for (int u = 0; u < attr.getChildNodes().getLength(); u++){
				        	        	 try{
				        	        	 	 Node item = attr.getChildNodes().item(u);
					        	      //   System.out.println("name" + item.getNodeName());
					        	        //	System.out.println("value " + getNode(item,"attributeName").getFirstChild().getNodeValue());
					        	        	
					        	            String glnkood  =  getNode(item,"attributeName").getFirstChild().getNodeValue();
					    		            if(glnkood.equals("service_days")){
					    		            	glnkood  =  getNode(item,"attributeValue").getFirstChild().getNodeValue();
					    		            	// this.ilns.put(glnkood.trim(), id);
					    		            	obj.put("maintenance", glnkood) ;
					    		             System.out.println("olemas maintance " + glnkood + " " + id);
					    		            }
					        	        	
					        	        	
					        	        	
				        	        	 }catch(Exception e){
				        	        		//e.printStackTrace(); 
				        	        	 }
				        	        }
				     			}
			             		   }catch(Exception ee){
					     		   }
	                        
	                        
	                     
	                        String custcode = deCode(getNode(rida,"customerID").getFirstChild().getNodeValue());
	                        if(custcode != null && custcode.trim().length() > 0){
	                        	   sidusAC.put(id, custcode);
	                        }
	                        try{  
	                        String nfcTag = deCode(getNode(rida,"nfcTag").getFirstChild().getNodeValue());
	                        if(nfcTag != null && nfcTag.trim().length() > 0){
	                     	   obj.put("nfcTag", nfcTag);
	                         }
	                        }catch(Exception e){
	      	            	  
	      	              }
	                        try{ 
	                        String factoryId = deCode(getNode(rida,"serialNumber").getFirstChild().getNodeValue());
	                        if(factoryId != null && factoryId.trim().length() > 0){
	                      	   obj.put("factoryId", factoryId);
	                          }
	         		 }catch(Exception e){
     	            	  
     	              }
	              try{
	            	    String description = deCode(getNode(rida,"notes").getFirstChild().getNodeValue());
                        if(description != null && description.trim().length() > 0){
                      	   obj.put("description", description);
                        }
                  
	              }catch(Exception e){
	            	  
	              }
	              try{    
	                      java.sql.Date born = getDate(deCode(getNode(rida,"salesDate").getFirstChild().getNodeValue()));
	                        if(born != null ){
	                       	   obj.put("born", Converter.jsonDate(born));
	                       }
}catch(Exception e){
	            	  
	              }         
	              try{  
	            	  java.sql.Date contract = getDate(deCode(getNode(rida,"startDateOfWarranty").getFirstChild().getNodeValue()));
	                        if(contract != null ){
	                       	   obj.put("contract", Converter.jsonDate(contract));
	                       }
	                            
	                        	
	         		}catch(Exception e){
		            	  
		              }   
	                     
	              obj.put("active", true);
          		asses.add(id);
	              
	                        if( !cuus.contains(id)){
	                        	 vec.add(obj);
	                        	counter ++;
	                        }
	         		 }
	        }
	         			
	         			    

	                     System.out.println("Counter " + counter + " Asses " + asses.size());
	                     
	    	   Converter.sendAssetsJson(vec,this.user,this.pass,this.rcompany);     
	    	   
	    	 }               
	                		
	             		
	             		
	    	   //siin saadame seosed �ra jujul kui customer on olemas selline 
	     	Vector   vec = new Vector(); 
	              int cou = 0;
	              int coua = 0;
	            Iterator itera =  sidusAC.keySet().iterator();
	                  while(itera.hasNext()){
	                 	
	                 	 JSONObject obj = new JSONObject();
	                 	 coua++;
	                         String code = (String)itera.next();
	                         obj.put("inventoryItemCode", code);
	                         String custcode = (String)sidusAC.get(code);
	                         obj.put("customerCode", custcode);
	                       
	                         if(asses.contains(code) && custs.contains(custcode) ){
	                        	  String olemas = (String)asscusts.get(code);
	                        	  if(olemas == null){
	                              //    vec.add(obj);  
	                                  //cou++;  
	                         	  }else if(olemas != null){
	                        		  if(!olemas.equals(custcode)){
	                        		//	   vec.add(obj);  
	                                  //     cou++; 
	                        		  }
	                        	  }
	                          }
	                         vec.add(obj);  
                             cou++;  
	                       }
	                  
	           System.out.println("assetsnn cou " +  cou + " "+ coua + " " + vec.size());
	                      
	                      // siis peaks tegema vahele kontrolli et kui on muudatus siis ainult laksad ylesse

	    	   Converter.sendAssetsCustomerJson(vec,this.user,this.pass,this.rcompany);     
	      
	    	 }
      } catch (Exception e) {
		e.printStackTrace();
	  }  	   
  
     return tm;
   }

 

 
   
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
       prodCodes = new Hashtable();
    Vector noGrups = new Vector();
    Vector yesGrups = new Vector();
    Vector noBrands = new Vector();
        prodHash = new HashMap();
        TreeMap brands = new TreeMap();
        boolean sortBR = false;
        String status = null;
        String statusnot = null;
        boolean soldGoes = false;
        boolean secgr = false;
        boolean groupmustbe = false;
        
        getSoldprods();  
      
        if("true".equals(getParent().getProps().getProperty("group.usesub"))){
        	secgr = true;
        }
        
        if("true".equals(getParent().getProps().getProperty("soldgoes"))){
        	soldGoes = true;
        }
        
        if("true".equals(getParent().getProps().getProperty("groupmustbe"))){
        	groupmustbe = true;
        }
        
    if(getParent().getProps().getProperty("nogrups") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogrups"),",");
    	while (st.hasMoreElements()){
    		String aa = st.nextToken();
    		if(secgr){
    			aa= this.addLeadingZero(aa);
    		}
    		noGrups.addElement(aa);
    	}
    }
    
    if(getParent().getProps().getProperty("yesgrups") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesgrups"),",");
    	while (st.hasMoreElements()){
    		String aa = st.nextToken();
    		if(secgr){
    			aa= this.addLeadingZero(aa);
    		}
    		yesGrups.addElement(aa);
    	}
    }
    
    
    String priorLadu = null;
    if(getParent().getProps().getProperty("priorLadu") != null){
    	priorLadu = getParent().getProps().getProperty("priorLadu");
    }
  
    String priorLopp = null;
    if(getParent().getProps().getProperty("priorLopp") != null){
    	priorLopp = getParent().getProps().getProperty("priorLopp");
    }
  
    String priorEtte = null;
    if(getParent().getProps().getProperty("priorEtte") != null){
    	priorLopp = getParent().getProps().getProperty("priorEtte");
    }
    
   boolean addpicts = false;
   String pictType = "";
   if(getParent().getProps().getProperty("usepictures") != null){
	   addpicts = true;
	   pictType = getParent().getProps().getProperty("usepictures");
   }
	   
   
    if(getParent().getProps().getProperty("prodstatus") != null){
    	 status = getParent().getProps().getProperty("prodstatus");
    	
    }
    if(getParent().getProps().getProperty("prodstatusnot") != null){
   	 statusnot = getParent().getProps().getProperty("prodstatusnot");
   	
   }
    
    Hashtable prices = null;
    if(getParent().getProps().getProperty("usepricelist") != null){
    	String pr = getParent().getProps().getProperty("usepricelist");
    	prices = getPricelists(pr);
    }

    boolean prbase = false;
    
    String pictroot = null; 
    
    if(getParent().getProps().getProperty("usepricelist2") != null && prices != null){
    	prbase = true;
    	
    }

    if("true".equals(getParent().getProps().getProperty("sortbybrands"))){
    	try {
			brands = this.getProdGroupsSecond();
			sortBR = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    if(getParent().getProps().getProperty("nobrands") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nobrands"),",");
    	while (st.hasMoreElements()){
    		noBrands.addElement(st.nextToken());
    	}
    }
   
    
    if("true".equals(getParent().getProps().getProperty("supplier.group"))){
        suppliersTM = new TreeMap();
    }
		   
		
     try {
    	  FileListFilter fil = new FileListFilter("tootederp","xml");
    	 String[] ll = new File (getDir()).list(fil);
    	 for (int i = 0; i < ll.length; i++) {
    		
    		 String name = ll[i];
 		  File f = new File (getDir() + name);
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
     	
       Node root = getNode(doc, "document");      
       //   System.out.println(root);
      	Node read = getNode(root, "records");
     // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int s = 0; s < read.getChildNodes().getLength(); s++){
          	  Node rida = read.getChildNodes().item(s);
          	  if(rida.getNodeName().equals("item")){
         	  try{
         		  
         		  
         		 
              		 String code  = deCode(getNode(rida,"productID").getFirstChild().getNodeValue());
              		 Product prod = new Product(code);
              		 boolean goes = true ;
              		 String kood = "";
              		 try{
              			 kood = deCode(getNode(rida,"code").getFirstChild().getNodeValue()); 
              			prodCodes.put(kood.trim(), code);
              		 }catch (Exception e){
              			 
              		 }
              		String ean  = null;
              		String add = code;
              		 if("true".equals(getParent().getProps().getProperty("name.code.none"))){
              			 add = "";
              		 }
              		
              		 try{
               		 ean = getNode(rida,"code2").getFirstChild().getNodeValue();
                   		if(ean != null){
                   			prod.setEAN(ean.replaceAll(";", ","));
                   		 if("true".equals(getParent().getProps().getProperty("add.ean"))){
                   			add = ean.substring(ean.length()-4); 
                   		 }
                   		
                   		}	 
               		 }catch(Exception r){
               			 
               		 }
               	String pre = "";
              		 
              		  try{
          				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
          			
          				if((prod.getStock() < 0.1 && sta.equals("NO_LONGER_ORDERED")) || sta.equals("NOT_FOR_SALE") ){
          					goes = false;
          				}
          				if( sta.equals("NO_LONGER_ORDERED") ){
              					pre = "xOUT "; 
          				}
          				if( sta.equals("NOT_FOR_SALE") ){
          					pre = "xPRE "; 
      				}
                    }catch(Exception e){
          				
          			}
                	
              		
              		try{
          				String sta  =  getNode(rida,"priorityGroupID").getFirstChild().getNodeValue();
          			
          				if(priorLadu != null){
          					
          					goes = false;
          					
          					if( sta.equals(priorLadu)){
            					  goes = true;
            				}
          					if((prod.getStock() > 0.1 && sta.equals(priorLopp))){
              					  goes = true;
              				}
              				if( priorLopp != null  && sta.equals(priorLopp) ){
                  					pre = "xOUT "; 
              				}
              				if( priorEtte != null && sta.equals(priorEtte) ){
              					    pre = "xPRE "; 
              					    goes = true;
          				     }
              			}
          				
                    }catch(Exception e){
          				
          			}
              		  
              		  
              		 String namec = null;
              		 try{
              		  namec  =  (getNode(rida,"name").getFirstChild().getNodeValue());
              		 if("true".equals(getParent().getProps().getProperty("name.code.front"))){
              			 prod.setName(pre + kood + " " + (namec) + " " +  add); //Converter.enCodeEst
              		 }else{
              			 prod.setName(pre + (namec) + " " + kood + " " + add); //Converter.enCodeEst
              		 }
              		
                      }catch(Exception r){
              			// r.printStackTrace();
              			 prod.setName("noname " + code);
              		 }
              		
              		if(addpicts){
              			try{
              				Node picts  =  getNode(rida,"images");
                          
              				
              				for (int e = 0; e < picts.getChildNodes().getLength(); e++){
              	          	  Node rr = picts.getChildNodes().item(e);
              	          	  if(rr.getNodeName().equals("item")){
              	          		for (int fs = 0; fs < rr.getChildNodes().getLength(); fs++){
                  	          	   Node rr0 = rr.getChildNodes().item(fs);
              	          			if(rr0.getNodeName().indexOf(pictType) != -1){
                        	          	String pictname  =  rr0.getFirstChild().getNodeValue();
                          	 	       int last = pictname.lastIndexOf("/");
                          	 	    	   //http://www.erply.net/files/20346/pictures/web/product_pict/prd_47548247oz.jpg

                          	 	   //    System.out.println(rr0.getNodeName() + "  " +pictname );
                          	 	    pictroot = pictname.substring(0,last+1);
                          	 	    pictname = pictname.substring(last +1);
                          	 	    int lp = pictname.lastIndexOf(".");
                                    prod.setPhotoid(pictname.substring(0,lp) + "|" +pictname.substring(lp));
                       	 	     //  System.out.println(pictroot + "  " +pictname );
                	          		}
                	          }
                      		   }
              				}
              			}catch(Exception e){
              			 //	e.printStackTrace();
              			}
              		}
              		
              		String price  =  getNode(rida,"price").getFirstChild().getNodeValue();
              		if(price != null){
              			prod.setPrice(Double.parseDouble(price));	
              		}
              		
              		if(prices != null && prices.get(prod.getID()) != null){
              			prod.setPrice(Double.parseDouble((String) prices.get(prod.getID())));	
              		}
              		
              		
              		String vat  =  getNode(rida,"vatrate").getFirstChild().getNodeValue();
              		if(vat != null){
              			prod.setVat(vat);	
              		}
              		
              		
              		if(status != null){
              			try{
              				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
              				if(!sta.equals(status)){
              					goes = false;
              				}
                        }catch(Exception e){
              				
              			}
              		}
              		
              		
              		if(statusnot != null){
              			try{
              				String sta  =  getNode(rida,"status").getFirstChild().getNodeValue();
              				if(sta.equals(statusnot)){
              					goes = false;
              				}
                        }catch(Exception e){
              				
              			}
              		}
              		
              		
              		
              		
              		String group  =  getNode(rida,"groupID").getFirstChild().getNodeValue();
              		if(group.trim().length() == 0 && groupmustbe){
              			goes = false;
              		}
              		 if(secgr){
               			 group = addLeadingZero(group); 
               		 }
              		if(group != null && groupList != null){
              			String ghh = (String)groupList.get(group);
              			
              		//	System.out.println("ghh " + (String)groupNameList.get(group) + " "+ group);
              				
              				if(ghh != null){
              					
              					if(yesGrups.size() > 0){
              						goes = false;
              					}
              					
              				prod.setGroup(ghh);	
              				prod.setGroup((String)groupNameList.get(group));
                  			if(noGrups.contains(group)){
                  				goes = false;
                  			}	
                  			if(yesGrups.contains(group)){
                  				goes = true;
                  			}
                  			
                  			
                  			if(secgr && ghh.contains(".") ){//kui on subgroup kasutuesl
                  				StringTokenizer st = new StringTokenizer(ghh,".");
                  				prod.setGroup(st.nextToken());
                  				prod.setGroupSecond(ghh); 
                  				// ainult viimase grupi panema 
                  			}
              				
              				}
              			
              		}
              		

              		String brand  =  getNode(rida,"brandID").getFirstChild().getNodeValue();
              		if(brand != null){
              		if(noBrands.contains(brand.trim())){
              					goes = false;
                  			}
              		
              		 if("true".equals(getParent().getProps().getProperty("brands.group"))){
              			 prod.setGroupSecond("2." + brand.trim());
              			 if(brands != null && namec != null &&!namec.startsWith("TST") && sortBR && brands.get("2."+ brand.trim()) != null){
              				 prod.setNameSorting((String)brands.get("2."+ brand.trim()) + prod.getName());
              			 }else{
              				 prod.setNameSorting("xxxx" + prod.getName()); 
              			 }
              		  }
              		}
              		//System.out.println(prod.getNameSorting());
              		
              		
             
              		
              		try{
              			String unitName  =  getNode(rida,"unitName").getFirstChild().getNodeValue();
                  		if(unitName != null){
                  			prod.setUnit(deCode(unitName));	
                  		}	
              		}catch(Exception d){
              			
              		}
              		
              		
              	 
              		
              		String active  =  getNode(rida,"active").getFirstChild().getNodeValue();
              	
              		if(active != null && !active.equals("1")){
              				goes = false;
              		}
              	
              		try{
              			
              			StringTokenizer st = new StringTokenizer(laoID,",");
              			while(st.hasMoreElements()){
              				String lll = st.nextToken();
                  			Node wares  =  getNode(rida,"warehouses");
                        	for (int e = 0; e < wares.getChildNodes().getLength(); e++){
              	          	  Node rr = wares.getChildNodes().item(e);
              	          	  if(rr.getNodeName().equals("item")){
              	          		for (int fs = 0; fs < rr.getChildNodes().getLength(); fs++){
                  	          	   Node rr0 = rr.getChildNodes().item(fs);
              	          			if(rr0.getNodeName().indexOf("warehouseID") != -1){
                        	          	String ladu   =  rr0.getFirstChild().getNodeValue();
                          	 	         if(ladu.equals(lll)){
                                       	String stock  =  getNode(rr,"totalInStock").getFirstChild().getNodeValue();
                                       	
                                       		if(stock != null ){
                                       			double seis = Double.parseDouble(stock);
                                       			if(seis > 0){
                                       				if(prod.getStock() > 0){
                                           				prod.setStock(prod.getStock() + seis);	
                                                 	}else{
                                           				prod.setStock(seis);	
                                                    }	
                                       			}
                                       			
                                       		}
                                       //	 System.out.println(rr0.getNodeName() + "  " + prod.getStock() ); 
                          	 	         }
                          	 	     
                          	 	    }
                	          }
                      		   }
              				}              				
              			}

              			
              		
              			
              			String stock  =  getNode(getNode(getNode(rida,"warehouses"),"item"),"totalInStock").getFirstChild().getNodeValue();
                  		if(stock != null){
                  			prod.setStock(Double.parseDouble(stock));	
                  		}	
              		}catch(Exception e){
              			
              		}
              		
              		try{
              			String kastis  =  getNode(getNode(getNode(rida,"packages"),"kast"),"amount").getFirstChild().getNodeValue();
                  		if(kastis != null){
                  			prod.setInCases(Double.parseDouble(kastis));	
                  		}	
              		}catch(Exception e){
              			
              		}
             		  
              		/*
              		try{
              			String karbis  =  getNode(getNode(getNode(rida,"packages"),"karp"),"amount").getFirstChild().getNodeValue();
                  		if(karbis != null){
                  			prod.setInCases(Double.parseDouble(karbis));	
                  		}	
              		}catch(Exception e){
              			
              		}*/
              		
              		if(prices != null && namec != null && prbase && namec.indexOf("TST") == -1 && prices.get(prod.getID()) == null){
              				goes = false;
              		}
     
              		

              	    if("true".equals(getParent().getProps().getProperty("supplier.group"))){
              	         
              	      try{
                			String suppName  =  getNode(rida,"supplierName").getFirstChild().getNodeValue();
                    		if(suppName != null){
                    			prod.setGroupSecond(deCode(suppName));	
                    			suppliersTM.put(suppName, suppName);
                    		}	
                		}catch(Exception d){
                			
                		}
              	    }
    
              	    if(soldGoes && prod.getStock() < 0.1){
              	    	 goes = false;
              	    	if( soldProds.contains(kood)){
              	    		goes = true;
              	    		//System.out.println(kood);
              	         }
              	    }
              	    
              	    //kui status on l�pumyyk siis vaatame laoj��ki 
              	
              	    
         		 if(goes){
         			 String sorting = prod.getID();
         			if(getParent().getProps().getProperty("sorting") != null){
         				if(getParent().getProps().getProperty("sorting").equals("nimi")){
         					sorting = prod.getName();
         				}
         				if(getParent().getProps().getProperty("sorting").equals("gruppnimi")){
         					sorting = prod.getGroup() + prod.getName();
         				}
         				if(getParent().getProps().getProperty("sorting").equals("gruppkood")){
         					sorting = prod.getGroup() + prod.getID();
         				}
         			}
         			  tm.put(sorting , prod); 
         			  prodHash.put(prod.getID(), prod);
         		  }
                      
         	  }catch(Exception e){
         		  e.printStackTrace();
         	  }
          	  }     
         }
    
          
          
    	 }	 
    
     } catch (Exception e){
       e.printStackTrace();
     }
     
     try {
    	 getOutDir();
 		Converter.makeTelemaFiles(this.getOutDir(), tm, this.getProdGroups(), null, ownGLN);
 	} catch (Exception e) {
 		
 		e.printStackTrace();
 	}
	 
 	
 	if(pictroot != null){

		Properties props = parent.getParentFrame().loadProps("update.pro");
		if(props == null){
			props = new Properties();
		}
		props.put("weburl", pictroot);
		

		

		try {
			FileOutputStream fos = new FileOutputStream("update.pro");
			props.store(fos, "");
			fos.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
 	}
 	
     return tm;
   }

   private String ownGLN = null;
   private String getOutDir(){
       String dir = getParent().getProps().getProperty("readout");
       if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
       return dir;
     }

   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   
   private String getKlient(String kli){
	   String klient = null ;
	   return klient ;
   }

   public void getSoldprods(){
	  
	   soldProds = new HashSet();

		try {
	  		  File f = new File (getDir() + "klienditoodeErp.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "document");      
	       	Node read = getNode(root, "records");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("CSV")){
	          	  String csv = rida.getFirstChild().getNodeValue();
	          	  csv = csv.replaceAll("\"\"", "|");
	          
	          	  StringTokenizer st = new StringTokenizer(csv,"|");
	          	  st.nextToken();
	          	  while (st.hasMoreElements()){
	          		  String line = st.nextToken();
	          		 line = line.replaceAll("\";\"", "|");
	          		 line = line.replaceAll("\"", " ").trim();
	          		
	      //    		System.out.println("line  " + line);
	          	//	line  6276|8|Aurusti eGo Sigma|2013-06-29|6.6667|8|1
	          		 StringTokenizer stv = new StringTokenizer(line,"|");
	          		  while (stv.hasMoreElements()){
	          			
	          				 String toode =  " "; 
	 	          			 try{
		          				  toode = stv.nextToken().trim();
			          		  if(!soldProds.contains(toode)){
			          			soldProds.add(toode);
			          		  }
	          			  		 }catch(Exception e){
	          			 } } } }     
	              }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  
	  	   System.out.println("SoldProds size " + soldProds.size());
	  
   
   }
   
 private  Vector custProds = new Vector();
   public Vector getCustomerProductsList(String agentCode){
	   custProds = new Vector();
	   soldProds = new HashSet();

		try {
	  		  File f = new File (getDir() + "klienditoodeErp.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "document");      
	       	Node read = getNode(root, "records");
	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("CSV")){
	          	  String csv = rida.getFirstChild().getNodeValue();
	          	  csv = csv.replaceAll("\"\"", "|");
	          
	          	  StringTokenizer st = new StringTokenizer(csv,"|");
	          	  st.nextToken();
	          	  while (st.hasMoreElements()){
	          		  String line = st.nextToken();
	          		 line = line.replaceAll("\";\"", "|");
	          		 line = line.replaceAll("\"", " ").trim();
	          		
	      //    		System.out.println("line  " + line);
	          	//	line  6276|8|Aurusti eGo Sigma|2013-06-29|6.6667|8|1
	          		 StringTokenizer stv = new StringTokenizer(line,"|");
	          		  while (stv.hasMoreElements()){
	          			  String klient = (String)docCust.get(stv.nextToken().trim());
	          			  if (klient != null){
	          				 String toode =  " "; 
	 	          			
		          			 String kuupaev = " ";
		          			 double kog = 1;
		          			 try{
		          				  toode = stv.nextToken().trim();
			          			   stv.nextToken().trim();//String nimi =
		          			   kuupaev = stv.nextToken().trim().substring(5);
		          			   double lastpr = 0;
		          			   try{
		          				   lastpr = Double.parseDouble( stv.nextToken().trim());
		          			   }catch(Exception e){
		          				   
		          			   }
		          			  //String hindb =
		          			   stv.nextToken().trim();//String hindn =
		          			  String kogus = stv.nextToken().trim();
		          			 kog = Double.parseDouble(kogus); 
		          			 
		          			  
			          		    CustProd cp = new CustProd(klient, toode,kog,0,0,kuupaev,lastpr,0,0," ");
			          		  custProds.add(cp);
			          		  if(!soldProds.contains(toode)){
			          			soldProds.add(toode);
			          		  }
	  
	          			  		 }catch(Exception e){
	          			 }  } } } }     
	          }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  
	  	   System.out.println("Cp size " + custProds.size());
	   return custProds;
   }

   private HashSet soldProds = null;
   
   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "ErplyLtt";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }
   
   private HashMap ketid = null;
   private HashMap sortimendid = null;
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private TreeMap groupList = new TreeMap();
   private TreeMap groupNameList = new TreeMap();
   private TreeMap groupListSec = new TreeMap();
    
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private String session = "";
   private String passString = ""; 
   private String username = null;
   private int onpage = 100;
   private String company = null;
   private String password = null;
   private String apiversion = null;
   private String returntype = null;
   private String userstuff = null;
     private URL myurl = null;

  /**
   * setBulkSales
   *
   * @param agents Map
   */
     
     private java.sql.Date getDate(String ss){
 	  	//2006.06.22
 	  Calendar cc = Calendar.getInstance();  	
 	  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
 	  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
 	  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
 	  	return new java.sql.Date(cc.getTimeInMillis()) ;
 	   	
 	  }
     
     
     
     private Hashtable docCust = null;
     
  public void setBulkSales(Map agents) {
	  TreeMap myygid = new TreeMap();
	  docCust = new Hashtable();
      FileListFilter fil = new FileListFilter("doklist","xml");
 	 String[] ll = new File (getDir()).list(fil);
 	 for (int u = 0; u < ll.length; u++) {
 		 String name = ll[u];
	    		System.out.println(name);
	 		  File f = new File (getDir() + name);
	 		  
	 		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  	     try{
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	        
	        Node root = getNode(doc, "document");      
	        //   System.out.println(root);
	       	Node read = getNode(root, "records");
	      // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	           for (int i = 0; i < read.getChildNodes().getLength(); i++){
	           	  Node rida = read.getChildNodes().item(i);
	           	  if(rida.getNodeName().equals("item")){
	          	  try{
				
				String agent = getNode(rida, "employeeID").getFirstChild().getNodeValue();
				double total = Double.parseDouble(getNode(rida, "total").getFirstChild().getNodeValue());
		        if(myygid.get(agent) != null){
		        	double hetk = Double.parseDouble((String)myygid.get(agent));
		        	myygid.put(agent,(total+ hetk)+"");
		        }else{
		        	myygid.put(agent,total+"");
		        }
		        docCust.put(getNode(rida, "id").getFirstChild().getNodeValue(), 
		        		getNode(rida, "clientID").getFirstChild().getNodeValue());
		          }catch(Exception e){
	          		  e.printStackTrace();
	          	  }
	           	}     
	          }
	           
	           
         	  }catch(Exception e){
         		  e.printStackTrace();
         		
         	  }
         	//  System.out.println("myygid" + myygid.size());
 	         
	 		  Iterator itera = myygid.keySet().iterator();
	 		  while(itera.hasNext()){
	 			  String code = (String)itera.next();
	 			 Agent agent = (Agent)agents.get(code);
	             if (agent != null){
	            	   agent.setMonthSales((int) Double.parseDouble((String)myygid.get(code))); 
	             }
	           
	 		  }
        
 	 }
	  
	  
  }
}
