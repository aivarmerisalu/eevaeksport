package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BiovelaUusDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker {

  public BiovelaUusDBDriver(EksportPanel parent) {
    super(parent);
    PropertyConfigurator.configure("log4j.properties");
    this.subjectFilesTo = parent.getString("filemsg");	
     country = getParent().getProps().getProperty("companycode");
     user =  getParent().getProps().getProperty("rest.user");
     pass = getParent().getProps().getProperty("rest.pass");
     rcompany = getParent().getProps().getProperty("rest.company");
      }
   String user =  null;
   String pass = null;
   String rcompany = null;
  
  
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
	             String[] ff2 = res2.list();
	         	 for(int i = 0;ff2.length>i;i++){
	             	(new File ( pictspda + ff2[i])).delete(); 
	             }
//	         	 Lõppes kustutamine 
	  		 
	  		 TreeMap prods = this.getProducts(null);

	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prodids.iterator();
	     //    System.out.println("pilt olemas " + ff2.toString() + " "+ pictssou);
	         while (enume.hasNext()) {
	    	    	String toode = (String)enume.next();
	       	  if(toode.length() > 0){
	           	  res2 = new File (pictssou);
	              ff2 = res2.list();
	          //    System.out.println("pilt olemas " + ff2.toString() + " "+ pictssou);
	         	 for(int i = 0;ff2.length>i;i++){
	             //     System.out.println("pilt olemas " + ff2[i]);
	         	    
	         		 if( ff2[i].startsWith(toode)){
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + toode+ ".jpg"); 
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
              	    if(buf.toString().indexOf(toode +".jpg;"  + inpda.length()) == -1){
	                    		buf.append(toode +".jpg;"  + inpda.length() + "\n");
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
	    


  
   
  private String country = null;
  static Category log = Category.getRoot();
  
  private String getStringFromFile(String file){
	  StringBuilder sb = new StringBuilder();
	  try {
		  BufferedReader br = new BufferedReader( new InputStreamReader(
                     new FileInputStream(getParent().getProps().getProperty("data.dir")
           				  +  file), "UTF8"));
	      String line = br.readLine();

	      while (line != null) {
	          sb.append(line);
	         // sb.append(System.lineSeparator());
	          line = br.readLine();
	      }
	      String everything = sb.toString();
	      br.close();

	  } catch (Exception e) {
		  e.printStackTrace();
	  } finally {
	   
	  }
	 return sb.toString();	
  }
  
  
  private String getStringFromFile2(String req ,String service , String action){
	  Content cnt = null;
	
	  try{
		  
//http://jboss-03.biovela.lt:8181/cxf/merisoft/
System.out.println("http://jboss-02.biovela.lt:8181/cxf/merisoft/" + service);

System.out.println(action);
System.out.println(req);
Request re =
		   Request.Post("http://jboss-02.biovela.lt:8181/cxf/merisoft/" + service)
			.bodyString(req, ContentType.create("text/xml", "UTF-8"))
			.version(HttpVersion.HTTP_1_1)
			.addHeader("SOAPAction", action  );
System.out.println("re " + re.toString());

		  cnt =  re.execute().returnContent();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  return cnt.asString().replaceAll("qdoc:", "");
	 	
  }
  
  public String getSaveDebtInfo() {
  // Siin teeme Cashi ära.
	  File dir = new File(getParent().getProps().getProperty("cash.read"));
	    
	  //  System.out.println("vvv "+ driver.getImportFile());
	    File[] files = dir.listFiles();
	  this.getCustomers(null);
	    HashSet loetud = new HashSet();
	    for (int i = 0; i < files.length ; i++){
	      File file = files[i];
	      if (file.isFile()){
	    	  try{
	    		
	    	  FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String line = br.readLine();
				line = br.readLine().replaceAll(";;", "; ;");
		        try{
		        	  line = line +  br.readLine().replaceAll(";;", "; ;");
						  	
		        }catch(Exception e){
		        	
		        }
					StringTokenizer st = new StringTokenizer(line, ";");
					if (st.hasMoreElements()){
						//CUS_REALID	SUMA	KVID	USRREALID	ANSPOSTED	ST	VAL
						//10002654	300.0	abc=123	1046	25.09.2015	VO	EUR
      String cuid = st.nextToken();
      if(payers.get(cuid) != null){
    	  cuid = payers.get(cuid)+"";
      }else {
    	  this.getCustomers(null); 
    	  if(payers.get(cuid) != null){
    		  cuid = payers.get(cuid)+"";
    	  }
      }
      double summa = Double.parseDouble(st.nextToken());
      String docid = st.nextToken();
      String user = st.nextToken();
      String date = st.nextToken();
      date = date.replace('.', '-').replace('.', '-');//substring(8)+ "-"+date.substring(4, 6)+"-"+date.substring(0,1);
      System.out.println("date " + date);
      
      String uniq = date + cuid + user + (summa+"") ;
      
      if(!loetud.contains(uniq)){
    	  
      
      Converter.saveFile(getParent().getProps().getProperty("cash.read")+ "response/" + user+"-"+ 
              cuid+"-"+ (int)summa + "-sourceCU.xml", cashWebCU(cuid , docid , user , date, summa));
   
      Converter.saveFile(getParent().getProps().getProperty("cash.read")+ "response/" + user+"-"+ 
    	      cuid+"-"+ (int)summa + "-sourceSM.xml", cashWebSM(cuid , docid , user , date, summa));
    			
      
     
     
    String  asi = getStringFromFile2( cashWebSM(cuid , docid , user , date, summa), "DrcrMemo","urn:biovela:merisoft:drcr-memo/import");
      Converter.saveFile(getParent().getProps().getProperty("cash.read")+ "response/" + user+"-"+ cuid+"-"+ (int)summa + "-SM.xml", asi);
	
     asi =  getStringFromFile2( cashWebCU(cuid , docid , user , date, summa) ,"Payment","urn:biovela:merisoft:payment/import");
      Converter.saveFile(getParent().getProps().getProperty("cash.read")+ "response/" + user+"-"+ cuid+"-"+ (int)summa + "-CU.xml", asi);
     
      
      loetud.add(uniq);
      }
					}
			
					
				br.close();
				fr.close();
				File dest = new File(getParent().getProps().getProperty("cash.read") + "done/" + file.getName());
				if(dest.exists()){
					dest.delete();
				}
				file.renameTo(dest);
				  
	    	  }catch(Exception e){
	    		  e.printStackTrace();
	    	  }
	    	  }
	      
	    }
	    System.exit(0);
	  return null;
   }
  
  
  private String cashWebCU(String cuid ,String docid ,String user ,String date,double summa){
	  
	  
	  return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:biovela:merisoft:payment\">" +
		"<soapenv:Header/>" +
		"<soapenv:Body>" +
			"<urn:import>" +
				"<urn:messageId>" + (Calendar.getInstance().getTimeInMillis()+ "").substring(5)+ "</urn:messageId>" +
				"<urn:domain>MPLG</urn:domain>" +
				"<urn:payment>" +
				"<urn:arBill>" + cuid + "</urn:arBill>" +
				"<urn:arPo>" + docid.replace('&', ' ').trim() + "</urn:arPo>" +
				"<urn:arCurr>EUR</urn:arCurr>" +
				"<urn:artotal>" + summa + "</urn:artotal>" +
				"<urn:arDate>" + date + "</urn:arDate>" +
				"<urn:arEffdate>" + date + "</urn:arEffdate>" +
			"</urn:payment>" +
	"		</urn:import>" +
	"	</soapenv:Body>" +
	"</soapenv:Envelope>";

  }
  
  private String cashWebSM(String cuid ,String docid ,String user ,String date,double summa){
	  String cust = cuid;
	  if(agents1.get(user) != null){
		  Agent ag = (Agent)agents1.get(user);
		  if(ag.getAgcust() != null){
			  cust = ag.getAgcust();
		  }
	  }
	  
	  return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:biovela:merisoft:drcr-memo\">" +
	"<soapenv:Header/>" +
	"<soapenv:Body>" +
		"<urn:import>" +
			"<urn:messageId>" + (Calendar.getInstance().getTimeInMillis()+ "").substring(5)+ "</urn:messageId>" +
			"<urn:domain>MPLG</urn:domain>" +
			"<urn:drcrMemo>" +
				"<urn:reference>" +
					"<urn:arBill>" + cust + "</urn:arBill>" +
					"<urn:arCurr>EUR</urn:arCurr>" +
					"<urn:arDate>" + date + "</urn:arDate>" +
					"<urn:arEffdate>" + date + "</urn:arEffdate>" +
					"<urn:account>" +
					"	<urn:ardAmt>" + summa + "</urn:ardAmt>" +
					"</urn:account>" +
				"</urn:reference>" +
			"</urn:drcrMemo>" +
"		</urn:import>" +
"	</soapenv:Body>" +
"</soapenv:Envelope>";

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

  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	 public Vector getPriceMatrixData() {
		return priceass;
	}
	
	 public Vector getPriceMatrixDataXX() {
			TreeMap vahe = new TreeMap();
	   	Vector lines = new Vector();
	   	fixedass = new Vector();
	   	prices = new HashMap();
	   	usedCustCodes = new HashSet();
	   	
	   		InputSource xml = new InputSource(new StringReader(getStringFromFile("PriceMeriEsb.xml")));	
			XPath xPath =  XPathFactory.newInstance().newXPath();
			 try{
			NodeList nodeList = (NodeList) xPath.compile("//pi_mstr").evaluate(xml, XPathConstants.NODESET);
				
			for (int i = 0 ; i < nodeList.getLength() ; i++){
				Node o = nodeList.item(i);
				   
				double price = 0;
					String custID = null;
					String clGroup = null;
					String prodID = null;
					String discGroup = null;
					String start = null;
					String end = null;
					String listname = null;
					String lineinfo = null;
					String discT = null;
					double discount = 0;
		            String type = null;  
		
					NodeList ll = o.getChildNodes();
				for (int r = 0 ; r < ll.getLength() ; r++){
					Node nn = ll.item(r);
					
					  if(nn.getNodeName().equals("piAmtType")){
						   try{  type = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
		
					  
					  if(nn.getNodeName().equals("piList")){
						   try{  listname = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
		
					  if(nn.getNodeName().equals("piStart")){
						   try{  start = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
					  if(nn.getNodeName().equals("piExpire")){
						   try{  end = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
			  if(nn.getNodeName().equals("piCsCode")){
				   try{  clGroup = nn.getFirstChild().getNodeValue();
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("piPartCode")){
				   try{  prodID = nn.getFirstChild().getNodeValue();
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("piListPrice")){
				   try{  price = Double.parseDouble(nn.getFirstChild().getNodeValue());
				   }catch(Exception e){ }
			   }
	 
			  
			  /*
			          <qdoc:pid_det>
	                  <qdoc:operation/>
	                  <qdoc:pidAmt>0.74</qdoc:pidAmt>
	                  <qdoc:pidDomain>D012LT</qdoc:pidDomain>
	                  <qdoc:pidListId>103218</qdoc:pidListId>
	                  <qdoc:pidQty>0</qdoc:pidQty>
	               </qdoc:pid_det>
	*/
			 				
				
			
			  if(nn.getNodeName().equals("pid_det")){
				  
	 NodeList llx = nn.getChildNodes();
				  
				  for (int rr = 0 ; rr < llx.getLength() ; rr++){
					  Node nnn = llx.item(rr);
					  if(nnn.getNodeName().equals("pidAmt")){
						   try{ 
							   if(discount == 0){
								   discount = Double.parseDouble(nnn.getFirstChild().getNodeValue());
								   discT = discount + "";
								}else{
									discT = nnn.getFirstChild().getNodeValue();
								}
						   }catch(Exception e){ }
					  }
					  
					  if(nnn.getNodeName().equals("pidQty")){
						   try{ 
							   String aaa =  nnn.getFirstChild().getNodeValue();
							   if(!aaa.equals("0")){
								   if(lineinfo == null){
									   lineinfo = "Kiek." + aaa + " Kain." + discT + "€";
								   }else{
									   lineinfo = lineinfo + "|Kiek." + aaa + " Kain." + discT + "€";
								   }
								}
						   }catch(Exception e){ }
					  }
				 }
			   }
			}
			  
			  
				 MatrixLine mline = new MatrixLine();

			if (custID != null && custID.trim().length() > 0) {
				mline.setCustID(custID.trim());
				if(!usedCustCodes.contains(custID.trim())){
					usedCustCodes.add(custID.trim());
				}
			}
		
			
			if (mline.getCustID() == null && clGroup != null
					&& clGroup.trim().length() > 0) {
				String ddd = clGroup.trim();
				if(ddd.equals("AIBE")) {
					ddd = ddd + "00";
				}
				mline.setCliGroup(ddd);
				if(!usedCustCodes.contains(ddd)){
					usedCustCodes.add(ddd);
				}
			}

			if (prodID != null && prodID.trim().length() > 0) {
				mline.setProdID(prodID.trim());
			}
			if (mline.getProdID() == null && discGroup != null
					&& discGroup.trim().length() > 0) {
				mline.setDiscGroup(discGroup);
				
			}
			//mline.setDiscount(discount);

			boolean action = false;
			
			if((listname != null && listname.trim().startsWith("AK"))){
				//System.out.println( clGroup + "'" + prodID + "'" + discGroup + "'" + discount +  " " 
					//		+ type + " " + start + " " + end + " " + lineinfo) ;
					
				     
				     if(start != null ){
							mline.setStartDate(start);
							if(end != null){
								mline.setEndDate(end);
							}else{
								mline.setEndDate(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
							}
							action = true;
						}
					}
					
			 mline.setProdLevel("000");
			 
			if((type != null && type.equals("4")) ){
		//System.out.println("'" + custID + "'" + clGroup + "'" + prodID + "'" + discGroup + "'" + discount +  " " 
				//	+ type + " " + start + " " + end + " " + lineinfo) ;
			
				if(start != null ){
					mline.setStartDate(start);
					if(end != null){
						mline.setEndDate(end);
					}else{
						mline.setEndDate(Converter.getDDpMMpYYYYpString(Calendar.getInstance()));
					}

					action = true;
					mline.setProdLevel("004");
				}
			}
			

			
			
			 
			
			//vaatame ka on hapu 
			boolean validdate = true;
			
			
			
			if(validdate){
				
			
			if (price != 0 || discount  > 0) {
				 if(discount > 0){
						price = discount;
					}
					mline.setPrice(price);
				
				   if(prices.get(prodID) != null){
						double pr = Double.parseDouble((String)prices.get(prodID));
						if(price > pr)
						prices.put(prodID, price + "");
					}else{
						prices.put(prodID, price + "");
					}
				  
			}
			
			
			if(clGroup != null && prodID != null && price > 0){
				if(lineinfo != null){
					mline.setLineinfo(lineinfo);
					//System.out.println("Lineinfo " + lineinfo);
				}
				String idd = clGroup+"|"+prodID;
			
				if(vahe.get(idd) != null )  {
					if( action ) {
						vahe.put(idd, mline);
					}
					
				}else {
					vahe.put(idd, mline);
				}
			
		  }
		}
	}
			
		Iterator itera = vahe.keySet().iterator();
		while(itera.hasNext()) {
			Object mline = vahe.get(itera.next()+"");
			lines.add(mline);
		}
				
		

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	   	System.out.println("Price matrix tehtud " + lines.size() + "\nPrices size" + prices.size());
	   	fixedass = (Vector)lines.clone();
		return lines;

	   }
   
   private HashSet usedCustCodes = null;

   private HashMap prices = new HashMap();
	
   
   private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection("jdbc:odbc:report", "mobile", "mobile");
	  }
   
      
   
  
   private HashMap prodHash = new HashMap();
   
	
   private Connection con2 = null;
   
   Hashtable prodCodes = null;
   
  
  
   private Vector fixedass = new Vector();
   private Vector priceass = new Vector();
   
   
   public Vector getFixedAssortmentData() {
	  return fixedass;

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
              return lines;
 }
   


	


   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }

  
 
   
   
   public String getSaveStockInfo(){
 	  
	   StringBuffer products = new StringBuffer();
	   stocks = new TreeMap<String,Double>();
	   	File ff = new File(getParent().getProps().getProperty("data.dir") + "Likuciai.csv");
			
			   try {
				
				    FileInputStream is = new FileInputStream(ff);
					InputStreamReader isr = new InputStreamReader(is, "utf-8");
					BufferedReader reader = new BufferedReader(isr);
				    String line = null;
				 
				    try {
				        while((line = reader.readLine()) != null) {
				          StringTokenizer st = new StringTokenizer(line,",");
				          try {
				        	 String  toode = st.nextToken();
				        	if(line.endsWith("\"")) {
				        		  line = line.substring(0,line.length()-1);
				        	}
				        	
				        	  
				        		 double kog = 0;
				        		 try {
				        			 kog = Double.parseDouble((line.substring(line.indexOf("KG,")+3).replace(",", ".").replaceAll("\"", " ").trim()));
				        		 }catch(Exception t ) {
				        			 
				        		 }
				        		if(kog == 0) {
				        			 kog = Double.parseDouble((line.substring(line.indexOf("VN,")+3).replace(",", ".").replaceAll("\"", " ").trim()));
				        		}
				        	
					        	   products.append(toode +  ";");
					               products.append(" ;");
					               products.append(getParent().getNF().format(kog) + "; "  +  "\n");
					        	   stocks.put(toode, kog);
				        	 }catch(Exception r) {
				        		
				        	 }
				           
				        }

				    } finally {
				        reader.close();
				    }
				 
			   }catch(Exception e) {
				   e.printStackTrace();
			   }
			   
		
	   System.out.println("stocks" + products.toString());
	   
   return products.toString();

   }
   
       public String getSaveStockInfoOLD(){
    	  
    	   StringBuffer products = new StringBuffer();
    	  
    	   stocks = new TreeMap<String,Double>();
    	   
    	   try{
    		   
    		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		    DocumentBuilder db;
    			
    				db = dbf.newDocumentBuilder();
    			
//    			System.out.println(f.getAbsolutePath());
    			File f = new File(getParent().getProps().getProperty("data.dir") + "ItemsInStock.xml");
    			FileInputStream in = getCleanInputStream(f);
    			String handler = null;
    			
    		    Document doc = db.parse(in,handler);
    		    
    		    Node read = getNode(doc, "ItemsList");
    		   
  		   //  InputSource xml = new InputSource(new StringReader(getStringFromFile("ItemsInStock.xml")));	
  			//	XPath xPath =  XPathFactory.newInstance().newXPath();
  				//NodeList nodeList = (NodeList) xPath.compile("/ItemList").evaluate(xml, XPathConstants.NODESET);
    	
    	    	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
    	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
    		    
    			
  					Node o = read.getChildNodes().item(i);
  					
  			        if(o.getNodeName().equals("Item")){
  	                
  					NodeList ll = o.getChildNodes();
  				  String toode = null;
                  double kogus = 0;
  				for (int r = 0 ; r < ll.getLength() ; r++){
                     
  					Node nn = ll.item(r);
                   
  				
                  
  				
  			 try{ 
  				
  				 if("NV".equals(nn.getNodeName())){
  					 toode = nn.getFirstChild().getNodeValue(); 
  					// System.out.println("nodename " + nn.getNodeName()  + " "+ nn.getFirstChild().getNodeValue() );//
  				 }
  				
  						
  			 }catch(Exception e){e.printStackTrace(); }
  					   
  		   if(nn.getNodeName().equals("Quantity")){
			   try{ 
				   kogus = Double.parseDouble(nn.getFirstChild().getNodeValue());
				   int kog = (int)(kogus);
                	
                	   products.append(toode +  ";");
 		               products.append(" ;");
 		               products.append(getParent().getNF().format(kog) + "; "  +  "\n");
                	 stocks.put(toode, kogus);
			   }catch(Exception e){ e.printStackTrace();}
		   }
  					
  					
  					
    	   		  }
  				 }
  	          	}
                	
              }catch (Exception k) {
            	  k.printStackTrace();
          }
    	   
    	   System.out.println("stocks" + products.toString());
    	   
       return products.toString();

       }

       
          
       public Vector getDebt(String agentCode){
    	   return debtList;
       }
       
     private  Vector debtList = null;
   
     private HashMap<String,Double> overx = null;

     private HashMap<String,Double> used = null;
     public Vector getDebtX(){
    	
    	 overx = new HashMap<String,Double>();
    	  used = new HashMap<String,Double>();
    	  
	    debtList = new Vector();
	   String firma = "";
	   
	
	   InputSource xml = new InputSource(new StringReader(getStringFromFile("DebtMeriEsb.xml")));	
		XPath xPath =  XPathFactory.newInstance().newXPath();
		 try{
		NodeList nodeList = (NodeList) xPath.compile("//ar_mstr").evaluate(xml, XPathConstants.NODESET);
			
		for (int i = 0 ; i < nodeList.getLength() ; i++){
		
			Node o = nodeList.item(i);
		
		   String saaja = null;

		   String maksja = null;
		   String number = null;
           Date duedate =  null;
           Date date =  null;
           double volg = 0;
          
           
			NodeList ll = o.getChildNodes();
		for (int r = 0 ; r < ll.getLength() ; r++){
			Node nn = ll.item(r);
			
	  if(nn.getNodeName().equals("arCust")){
		   try{  saaja = nn.getFirstChild().getNodeValue();
		   }catch(Exception e){ }
	   }
		
		
	  if(nn.getNodeName().equals("arBill")){
		   try{  maksja = nn.getFirstChild().getNodeValue();
		   }catch(Exception e){ }
	   }
		
	  
	  if(nn.getNodeName().equals("arNbr")){
		   try{  number = nn.getFirstChild().getNodeValue();
		   }catch(Exception e){ }
	   }

	  if(nn.getNodeName().equals("arDueDate")){
		   try{  duedate = getDate(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }

	  if(nn.getNodeName().equals("arEffdate")){
		   try{  date = getDate(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }
	  
	  if(nn.getNodeName().equals("arOpenAmt")){
		   try{  volg = Double.parseDouble(nn.getFirstChild().getNodeValue());
		   }catch(Exception e){ }
	   }
		}
		
	          Debt debt = new Debt();
	          debt.setCustID(saaja);
	          debt.setPayerID(maksja);
	          debt.setDoknr(number);
	     if(duedate != null){
	          debt.setDuedate(duedate);
	          int due = getDue(debt.getDuedate().getTime()) ;
	          debt.setDue(due+"");	    	 
              debt.setDate(date);
	          debt.setSum(volg);
	          if(volg != 0){
	        	  
	        		if(used.get(saaja) == null ){
	        			used.put(saaja, volg);
	        		}else{
	        			used.put(saaja,used.get(saaja) + volg);
	        		}
	        	 if(due > 0){
	        		 if(overx.get(saaja) == null ){
		        			overx.put(saaja, volg);
		        		}else{
		        			overx.put(saaja,overx.get(saaja) + volg);
		        		} 
	        	     }
	        	  
	        	  debtList.addElement(debt);  
	          }
	         
	     }

	      //    System.out.println("Debt " + debt.getCustID() + " " + debt.getSum());
	          
		}
		}catch(Exception m){
			m.printStackTrace();
		}
		
		
	   System.out.println("DebtList size " + debtList.size());
        return debtList;
  }

   
   private int getDue(long dd){
	      int res = 0;
	      res =(int) ((-dd+(new java.util.Date()).getTime())/86400000) ;
	      return res ;
	   }
   
  	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0,4));
		int MONTH = Integer.parseInt(kuup.substring(5,7))-1;
		int DAY = Integer.parseInt(kuup.substring(8,10));
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
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
       
       
       
       
       private String getPriceListXml(){
    	   return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
"<soapenv:Envelope xmlns=\"urn:schemas-qad-com:xml-services\" " +
"  xmlns:qcom=\"urn:schemas-qad-com:xml-services:common\"" +
 " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
 " <soapenv:Header>" +
 "   <wsa:Action/>" +
 "   <wsa:To>urn:services-qad-com:Excelerator</wsa:To> " +
  "  <wsa:MessageID>urn:services-qad-com::Excelerator</wsa:MessageID> " +
  "  <wsa:ReferenceParameters> " +
  "    <qcom:suppressResponseDetail>false</qcom:suppressResponseDetail>" +
  "  </wsa:ReferenceParameters>" +
  "  <wsa:ReplyTo>" +
  "    <wsa:Address>urn:services-qad-com:</wsa:Address>" +
  "  </wsa:ReplyTo>" +
  " </soapenv:Header>" +
  "<soapenv:Body>" +
  "  <queryMaintainGeneralizedCode>" +
   "   <qcom:dsSessionContext>" +
    "    <qcom:ttContext>" +
     "     <qcom:propertyQualifier>QAD</qcom:propertyQualifier>" +
      "    <qcom:propertyName>domain</qcom:propertyName>" +
       "   <qcom:propertyValue>D013LT</qcom:propertyValue>" +
        "</qcom:ttContext>" +
        "<qcom:ttContext>" +
       "   <qcom:propertyQualifier>QAD</qcom:propertyQualifier>" +
      "    <qcom:propertyName>version</qcom:propertyName>" +
     "     <qcom:propertyValue>eB21_1</qcom:propertyValue>" +
    "    </qcom:ttContext>" +
   "   </qcom:dsSessionContext>" +
  "    <dsQueryServiceRequest>" +
    "    <QueryServiceRequest>" +
     "     <SourceApplication>QADERP</SourceApplication>" +
    "      <Profile>PriceMeriEsb</Profile>" +
   "       <Filter>(pi_Expire >= date(today) and date(today + 3) >= pi_Start) or (pi_Expire = 'null' and date(today + 3) >= pi_Start ) </Filter>" +
  "        <MaxRows>0</MaxRows>" +
     "     <IgnoreBOFilter>false</IgnoreBOFilter>" +
 "       <IgnoreBOInnerJoin>false</IgnoreBOInnerJoin>" +
    "    </QueryServiceRequest>" +
   "   </dsQueryServiceRequest>" +
  "  </queryMaintainGeneralizedCode>" +
 " </soapenv:Body> " +
"</soapenv:Envelope>";
       }
       
       
       private String getGroupXml(){
    	   return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
"<soapenv:Envelope xmlns=\"urn:schemas-qad-com:xml-services\" " +
"  xmlns:qcom=\"urn:schemas-qad-com:xml-services:common\"" +
 " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
 " <soapenv:Header>" +
 "   <wsa:Action/>" +
 "   <wsa:To>urn:services-qad-com:Excelerator</wsa:To> " +
  "  <wsa:MessageID>urn:services-qad-com::Excelerator</wsa:MessageID> " +
  "  <wsa:ReferenceParameters> " +
  "    <qcom:suppressResponseDetail>false</qcom:suppressResponseDetail>" +
  "  </wsa:ReferenceParameters>" +
  "  <wsa:ReplyTo>" +
  "    <wsa:Address>urn:services-qad-com:</wsa:Address>" +
  "  </wsa:ReplyTo>" +
  " </soapenv:Header>" +
  "<soapenv:Body>" +
  "  <queryMaintainGeneralizedCode>" +
   "   <qcom:dsSessionContext>" +
    "    <qcom:ttContext>" +
     "     <qcom:propertyQualifier>QAD</qcom:propertyQualifier>" +
      "    <qcom:propertyName>domain</qcom:propertyName>" +
       "   <qcom:propertyValue>D013LT</qcom:propertyValue>" +
        "</qcom:ttContext>" +
        "<qcom:ttContext>" +
       "   <qcom:propertyQualifier>QAD</qcom:propertyQualifier>" +
      "    <qcom:propertyName>version</qcom:propertyName>" +
     "     <qcom:propertyValue>eB21_1</qcom:propertyValue>" +
    "    </qcom:ttContext>" +
   "   </qcom:dsSessionContext>" +
  "    <dsQueryServiceRequest>" +
    "    <QueryServiceRequest>" +
     "     <SourceApplication>QADERP</SourceApplication>" +
    "      <Profile>ItemGrpMeriEsb</Profile>" +
   "       <Filter></Filter>" +
  "        <MaxRows>0</MaxRows>" +
     "     <IgnoreBOFilter>false</IgnoreBOFilter>" +
 "       <IgnoreBOInnerJoin>false</IgnoreBOInnerJoin>" +
    "    </QueryServiceRequest>" +
   "   </dsQueryServiceRequest>" +
  "  </queryMaintainGeneralizedCode>" +
 " </soapenv:Body> " +
"</soapenv:Envelope>";
       }

   public TreeMap getProdGroups()  {
     TreeMap   tm = new TreeMap();
     this.isTreeGroup();
     baskets = new TreeMap();
     types = new TreeMap();
     prodSorting = new TreeMap();
     
  	try {

		InputSource xml = new InputSource(new StringReader(getStringFromFile("ItemGrpMeriEsb.xml")));

		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile("//dsOutput").evaluate(xml, XPathConstants.NODESET);
				
		for (int i = 0 ; i < nodeList.getLength() ; i++){
			Node o = nodeList.item(i);
			
			String grpCode = null;
			String grpDesc = null;
			String grpType = null;
			String sorting = null;
			
					NodeList lll = o.getChildNodes();
					
					for (int r = 0 ; r < lll.getLength() ; r++){
							
						
								Node nnn2 = lll.item(r);
								
								
								NodeList lll2 = nnn2.getChildNodes();
								
								for (int w = 0 ; w < lll2.getLength() ; w++){
									Node nnn = lll2.item(w);
								
								
						if(nnn.getNodeName().equals("grpCode")){
							   try{  grpCode = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }
						

						  if(nnn.getNodeName().equals("grpDesc")){
							   try{  grpDesc = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }
						  
						  if(nnn.getNodeName().equals("codeFldName")){
							   try{  grpType = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }

						  if(nnn.getNodeName().equals("sorting")){
							   try{  sorting = nnn.getFirstChild().getNodeValue();
							   }catch(Exception e){ }
						   }
							
						//  System.out.println(grpType + " " + grpCode);
						  
							
							if(grpType != null && grpCode != null){
							
					
						
					if(grpType.equals("pt__chr08")){
						tm.put(grpCode, Converter.enCodeEst(grpDesc  ));
						baskets.put(grpCode, Converter.enCodeEst(grpDesc + " " + grpCode ));	
						
					}
					
					if(grpType.equals("pt__chr02")){
						types.put(grpCode, Converter.enCodeEst(grpDesc));				
					}
					
					if(sorting != null){
					   prodSorting.put(grpCode, sorting);	
					}
					
					sorting = null;
							}
								}}
					}
  
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  	System.out.println("tm " + tm.size());
         return tm;
   }

   
   public TreeMap getProdGroupsSecond() throws SQLException {
		         return types;
		   }


   private TreeMap suppliersTM = null;
   TreeMap agents = null;
   TreeMap agents1 = null;

   
   private String getAgentXmlBulk(){
	   return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
" <soapenv:Envelope xmlns=\"urn:schemas-qad-com:xml-services\" " +
"   xmlns:qcom=\"urn:schemas-qad-com:xml-services:common\" " +
"  xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"> " +
	"  <soapenv:Header> " +
	"  <wsa:Action/> " +
	"    <wsa:To>urn:services-qad-com:Excelerator</wsa:To> " +
	"    <wsa:MessageID>urn:services-qad-com::Excelerator</wsa:MessageID> " +
	"     <wsa:ReferenceParameters> " +
	"    <qcom:suppressResponseDetail>false</qcom:suppressResponseDetail> " +
	" </wsa:ReferenceParameters> " +
	" <wsa:ReplyTo> " +
	"  <wsa:Address>urn:services-qad-com:</wsa:Address> " +
	" </wsa:ReplyTo> " +
	" </soapenv:Header> " +
	" <soapenv:Body> " +
	" <queryMaintainGeneralizedCode> " +
	"   <qcom:dsSessionContext> " +
	"     <qcom:ttContext> " +
	"       <qcom:propertyQualifier>QAD</qcom:propertyQualifier> " +
	"       <qcom:propertyName>domain</qcom:propertyName> " +
	"       <qcom:propertyValue>D013LT</qcom:propertyValue> " +
	"    </qcom:ttContext> " +
	"   <qcom:ttContext> " +
	"     <qcom:propertyQualifier>QAD</qcom:propertyQualifier> " +
	"      <qcom:propertyName>version</qcom:propertyName> " +
	"     <qcom:propertyValue>eB21_1</qcom:propertyValue> " +
	"   </qcom:ttContext> " +
	" </qcom:dsSessionContext> " +
	" <dsQueryServiceRequest> " +
	"   <QueryServiceRequest> " +
	"     <SourceApplication>QADERP</SourceApplication> " +
	"     <Profile>SalesPersonMeriEsb</Profile> " +
	"     <Filter></Filter> " +
	"     <MaxRows>0</MaxRows> " +
	"     <IgnoreBOFilter>false</IgnoreBOFilter> " +
	"     <IgnoreBOInnerJoin>false</IgnoreBOInnerJoin> " +
	"   </QueryServiceRequest> " +
	" </dsQueryServiceRequest> " +
	" </queryMaintainGeneralizedCode> " +
	" </soapenv:Body> " +
	" </soapenv:Envelope>";
   }
   
   
   
   
  
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // this.getProdGroups();
       agents = new TreeMap();
       agents1 = new TreeMap();
       HashMap asendajad = new HashMap();
       
       try {
			 FileReader fr = new FileReader("settings/vacation.txt");
	         BufferedReader reader = new BufferedReader(fr);
	       String line = null;
	 
	        while((line = reader.readLine()) != null ){
	           
	        	StringTokenizer st = new StringTokenizer(line, ";");
	
	          if (st.hasMoreTokens()) {
	            String ag1 =	st.nextToken();
	            String man = st.nextToken().trim();
	            Vector vv = new Vector();
	            if(asendajad.get(ag1) != null){
	           	 vv = (Vector)asendajad.get(ag1);
	           	 vv.add(man);
	           	 asendajad.put(ag1, vv);
	            }else{
	           	 vv.add(man);
	           	 asendajad.put(ag1, vv); 
	             }
	            }
	          }
		 
		}catch(Exception d){
		d.printStackTrace();	
		}
		
	
		 String aaa = getStringFromFile("SalesPersonMerisoftEsb2.xml");
		
		 
       InputSource xml = new InputSource(new StringReader(aaa));	
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		 try{
		NodeList nodeList = (NodeList) xPath.compile("//salesPerson").evaluate(xml, XPathConstants.NODESET);
	

		for (int i = 0 ; i < nodeList.getLength() ; i++){
		
			Node o = nodeList.item(i);
			String kood = null;
			String nimi = null;
			String phone = null;
			String agcust = null;

			String basket = null;
				NodeList ll = o.getChildNodes();
				for (int r = 0 ; r < ll.getLength() ; r++){
					
					
					Node nn = ll.item(r);
					//System.out.println(nn.getNodeName());
					
					  if(nn.getNodeName().equals("spAddr")){
						   try{  kood = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
					  
					  if(nn.getNodeName().equals("adBasket")){
						   try{  basket = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
					

					  if(nn.getNodeName().equals("adName")){
						   try{  nimi = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
						

					  if(nn.getNodeName().equals("adPhone")){
						   try{  phone = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
					  
					  if(nn.getNodeName().equals("spCmAddr")){
						   try{  agcust = nn.getFirstChild().getNodeValue();
						 //  System.out.println(agcust);
						   }catch(Exception e){ }
					   }
					  
					 
				}
				
			
			
			
			//	 System.out.println("nimi " + nimi + " " + kood);
			       	
			
		
                   Agent sm = new Agent (kood);
          
                   if(asendajad.get(kood) != null){
                     	Vector vv = (Vector)asendajad.get(kood);
                     	Enumeration aa = vv.elements();
                     	while(aa.hasMoreElements()){
                     		sm.addDubbler((String)aa.nextElement());
                     	//	System.out.println("Lisan asendaja " + kood + " " + sm.getDubbler().toString());
                     	}
                     }
                  
                   sm.setAgcust(agcust);
                   
                   sm.setName(kood + " " + nimi);
                  
                   sm.setLanguage("LIT");
                   sm.setCountry("lt");
                   sm.setPassWord("0000");
                  String email = parent.parseFirstEmail(parent.getEmail(kood));
                  if(email.length() > 3){
                	  sm.setEmail(email);
                  }
                  if(email.length() > 0){
                	  sm.setActive(true);
                  }
                  
                  if(phone != null){
                	  sm.setPhone(phone);
                  }
             
                	//agentidele on avaja settid abasketid .
                  if(basket != null){
                	  sm.addProdGroupList(basket);
                  }
              	//System.out.println("Agendi data " + kood + " " + nimi + " "+ phone + " "+ sm.getEmail());
             
                  
                     tm.put(kood + " " +nimi, sm); 
                     agents.put(kood, nimi);
                     agents1.put(kood, sm);
        } 
 	 
		 }catch(Exception e){
    		  e.printStackTrace();
    	  }
     System.out.println("Agendilist tehtud" );
  //   Converter.sendAgentJson(tm, country, false);
     
     Agent sm = new Agent ("HOUSE");
		       		
		 sm.setName("OFFICE");
     tm.put("HOUSE", sm); 
   //  setBulkSales(agents1);
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



  private String getcal(char day){
	  String res = null;
	  Calendar cal = Calendar.getInstance();
	//  cal.add(Calendar.DAY_OF_YEAR, 1);
	  int weekday = cal.get(Calendar.DAY_OF_WEEK);
	 
	
	  if(day =='A'){
		  if (weekday != Calendar.MONDAY){
			  while (weekday != Calendar.MONDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  

	  if(day =='B'){
		  if (weekday != Calendar.TUESDAY){
			  while (weekday != Calendar.TUESDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		   }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }

	  if(day =='C'){
		  if (weekday != Calendar.WEDNESDAY){
			  while (weekday != Calendar.WEDNESDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  

	  if(day =='D'){
		  if (weekday != Calendar.THURSDAY){
			  while (weekday != Calendar.THURSDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }

	  if(day =='E'){
		  if (weekday != Calendar.FRIDAY){
			  while (weekday != Calendar.FRIDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  

	  if(day =='F'){
		  if (weekday != Calendar.SATURDAY){
			  while (weekday != Calendar.SATURDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }

	  if(day =='G'){
		  if (weekday != Calendar.SUNDAY){
			  while (weekday != Calendar.SUNDAY) {
				    cal.add(Calendar.DATE, 1);
				    weekday = cal.get(Calendar.DAY_OF_WEEK);
				}
		  }else{
			  cal.add(Calendar.DAY_OF_YEAR, 7); 
		  }
	  }
	  
	  res = cal.getTimeInMillis() + "";
	  cal.add(Calendar.DATE, 7);
	  res = res + "," + cal.getTimeInMillis() + "";  
	  
	  
	  return res ;
  }

  private String makeDeldays(String deldays){
	  String res = null;
	  char[] chars = deldays.toCharArray();
	  for (int i = 0; i < chars.length ; i++) {
	      char c = chars[i];
	     // System.out.println("char " + c);
		  if(res == null){
			  res = getcal(c);
		  }else{
			  res = res + "," + getcal(c);
		  }
	  }
		 
	  
	  
	  return res ;
  }
        	  
        	  
       public String getDistribution() {
         return null;
       }
       
       
     
       
       private HashMap getCalldata(){
    	   
    	   HashMap call = new HashMap();
    	   
    	   try{
    			 
    		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		    DocumentBuilder db = dbf.newDocumentBuilder();
    			FileInputStream in = getCleanInputStream(new File(  getParent().getProps().getProperty("data.dir")
    					+ "CallDatesMerisoft.xml"));
    			String handler = null;
    			Document doc = null;
    			try{
    				doc = db.parse(in,handler);
    			}catch(SAXException a){
    				System.out.println("Viga XML-is ");
    				
    			}
    		    Node root = getNode(doc, "dsOutput");
    		  
    			//System.out.println("AAAAXY " + root.getNodeName()+ " " + root.getChildNodes().getLength());
		
    		    
    		for (int i = 0 ; i < root.getChildNodes().getLength() ; i++){
    			Node o = root.getChildNodes().item(i);
    			String agent = null;
    			String klient = null;
    			String day = null;

    			if(o.getNodeName().equals("xxcmsp_det")){
    			
    				NodeList ll = o.getChildNodes();
    				//System.out.println("AAAAX " + o.getNodeName() + " " + ll.getLength());
    				for (int r = 0 ; r < ll.getLength() ; r++){
    					
    					Node nn = ll.item(r);
    				//	System.out.println("AAAA " + nn.getNodeName());
    					if(nn.getNodeName().equals("xxcmspSpad")){
    						   try{  agent= nn.getFirstChild().getNodeValue();
    						   }catch(Exception e){ }
    					   }
    		
    					  if(nn.getNodeName().equals("xxcmspCmad")){
    						   try{  klient = nn.getFirstChild().getNodeValue();
    						   }catch(Exception e){ }
    					       }
    			        	
    				
    				 if(nn.getNodeName().equals("xxcmspCallcode")){
    					   try{  day = nn.getFirstChild().getNodeValue();
    					   }catch(Exception e){ }
    				       }
    		        	}
    			
    				
    				if(agent != null && day != null && klient != null && agent.length() > 0 && day.length() > 0 && klient.length() > 0){
    					call.put(klient.trim()+ "-" + agent.trim(), Converter.getVisitDay(day));
    				}
    				
    			}	
    			         }
    		 }catch(Exception e){
    			 e.printStackTrace();
    		 }
    		// System.out.println("Call "+ call.toString());

    		 return call;
    	   
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
    	  
    	  private Node getNode(Node rt, String s){
    		    return GenericDOM.getNode(rt, s);
    		  }
       

   public TreeMap getCustomers(Agent agent){
	      getDebtX();
	      payers = new TreeMap();
	      priceass =  this.getPriceMatrixDataXX();
			System.out.println("fixedass suurus " + fixedass.size());
		
	      HashMap visits =  getCalldata();
	      TreeMap tm = new TreeMap();
	    int loe = 0;
	     StringBuffer vips = new StringBuffer();
	    
	    	 
	    	 String tulem = getStringFromFile("CustomerMeriEsb.xml");
	    	 
	    	
	     InputSource xml = new InputSource(new StringReader(tulem));	
			XPath xPath =  XPathFactory.newInstance().newXPath();
			 try{
			NodeList nodeList = (NodeList) xPath.compile("//Customer").evaluate(xml, XPathConstants.NODESET);
System.out.println(" Node size " + nodeList.getLength());
			for (int i = 0 ; i < nodeList.getLength() ; i++){
			
				Node o = nodeList.item(i);
                  String fiAndPr = null;    
				NodeList ll = o.getChildNodes();
				String kood = null;
				String payer = null;
                String name =  null;
                String street =  null;
                String visit =  null;
                String deldays =  null;
                String inde =  null;
                String city =  null;
                String phone =  null;
                String person =  null; 
                String ag =    null; 
                String state = null;
                double balance = 0;
                double over = 0;
                double limit = 0;
                int due = 0 ;
    			double used = 0;
    			boolean vip = false ;
                boolean goes = true;
    			  
    		for (int r = 0 ; r < ll.getLength() ; r++){

				Node nn = ll.item(r);

		  if(nn.getNodeName().equals("cmAddr")){
			   try{  kood = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
			
		  
		  if(nn.getNodeName().equals("anx_det")){
			  NodeList llx = nn.getChildNodes();
			  String asi = null;
			  goes = true;
			  for (int rr = 0 ; rr < llx.getLength() ; rr++){
				  Node nnn = llx.item(rr);
				
				  if(nnn.getNodeName().equals("anxActive")){
					String res =   nnn.getFirstChild().getNodeValue();
					if(res.equals("false")){
						goes = false;
					}
				  }
				  
				  if( nnn.getNodeName().equals("anxCode")){
					  asi =  nnn.getFirstChild().getNodeValue();
					  if(asi.equals("BND")){
						   asi = "BNDx";
					   }
					  if(asi.equals("AIBE")){
						   asi = "AIBE00";
					   }
				  }
			 }
			  
			  if(goes){
				
			  try{ 
				   if(fiAndPr == null){
					   fiAndPr = asi;
									   
				   }else{
					   fiAndPr = fiAndPr + "|" + asi;
				   }
			   }catch(Exception e){ }
			  
			  }
			  
						   }
		  
		  
		  
		  
		  if(nn.getNodeName().equals("cmBill")){
			   try{  payer = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmChr04")){
			   try{  deldays = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  
		  if(nn.getNodeName().equals("adName")){
			   try{  name = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  

		  if(nn.getNodeName().equals("adVip")){
			   try{  String na = nn.getFirstChild().getNodeValue();
			   if(na.equals("true")){
				   vip = true;
			   }
			   }catch(Exception e){ }
		   }
		  
		  
		 
		  
		  if(nn.getNodeName().equals("adLine1")){
			   try{  street = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("adZip")){
			   try{  inde = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("adCity")){
			   try{  city = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  

		  if(nn.getNodeName().equals("adState")){
			   try{  state = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
			
		  if(nn.getNodeName().equals("adPhone")){
			   try{  phone = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("adAttn")){
			   try{  person = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("cmSlspsn")){
			   try{ 
				   if(ag != null){
					   ag = ag + "," + nn.getFirstChild().getNodeValue();
					  }else{
					   ag = nn.getFirstChild().getNodeValue();
					}
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("cmBalance")){
			   try{ balance = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmUsedCredit")){
			   try{ used = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmDebtDue")){
			   try{ over = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("cmCrLimit")){
			   try{ limit = Double.parseDouble( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  

		  if(nn.getNodeName().equals("ctDueDays")){
			   try{ due = Integer.parseInt( nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
			}
			
			
				 if(kood != null  && name != null ){
					
					 if(vip){
						 vips.append(kood + "\n"); 
					 }
					 vip = false;
					 
	         	  try{	
				Customer customer = new Customer(kood);
				if(payer != null){
					customer.setPayer(payer);
				}
				customer.setPaymTerm(due +"");
				customer.restName = name;
				
				String per = "";
				if(person != null){
					per = person;
				customer.restPerson = per;
				}
				
				if(phone != null){
					per = per + " " + phone;
					customer.restPhone = phone;
				}
				
				if(per.trim().length()>0){
					customer.setPersonalData(Converter.enCodeEst(per.trim()));
				}
				String aadr = "";
				if(street != null){
					aadr = street;
					customer.restStreet = street ;
				}
				
				if(city != null){
					aadr = aadr + " " + city;
					customer.restCity = city;
				}
				 
				customer.setCountry("Lithuania");
			
				 
				if(deldays  != null && deldays.trim().length() > 0  && !deldays.equals("ABCDEFG")){
					//System.out.println("del days " + customer.getID() + " " + deldays);
		        	customer.setDelDays(makeDeldays(deldays));
		        	
		        	//System.out.println("del days " + customer.getID() + " " + customer.getDelDays());
		        }else if(deldays == null || deldays.trim().length() == 0 ) {
		        	//goes = false;
		        	customer.noOrder = true;
		        	//customer.diffBack = true;
		        	System.out.println("del days " + customer.getID() + "  null");
		        }
				
				customer.restActive = goes ;
				customer.setAddress(Converter.enCodeEst(Converter.adressLink(aadr.trim(),"Lithuania")));
				//customer.setName(Converter.enCodeEst(name + " " + aadr));
				customer.setName(Converter.enCodeEst(name + " " + kood +   " " + aadr ));
				
				if(ag != null){
					StringTokenizer st = new StringTokenizer(ag,",");
					while (st.hasMoreElements()){
						String add = st.nextToken();
						Agent aas = new Agent(add);
						if(visits.get(customer.getID()+"-"+add) != null){
							aas.setVisit((String)visits.get(customer.getID()+"-"+add));
						}
						customer.addAgent(aas);
						  Agent agg = ((Agent)this.agents1.get(add));
	         				if(agg != null && agg.getDubbler().size()>0){
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           						customer.addAgent(new Agent("HOUSE"));
	           					}
	           				}
	           			}
			    	}
				
				
				if(fiAndPr != null){
					customer.setGroup(fiAndPr);
					StringTokenizer st = new StringTokenizer(fiAndPr,"|");
					while(st.hasMoreElements()){
						String aa = st.nextToken();
						
							if(aa.equals("AIBE")) {
								aa = aa + "00";
							}
							
						
						customer.addFixSortCode(aa);
						
					}
					if(usedCustCodes.contains(customer.getID())){
						customer.addFixSortCode(customer.getID());
						customer.setGroup(fiAndPr + "|" + customer.getID());
					}
					if(usedCustCodes.contains(customer.getPayer()) &&
							!customer.getFixSortCode().contains(customer.getPayer())){
						customer.addFixSortCode(customer.getPayer());
						customer.setGroup(fiAndPr + "|" + customer.getPayer());
					}
					//System.out.println("custGroup " + fiAndPr);
				}
				
				if(state != null && state.equals("19")){
					customer.setIsPayer(true);
				}
				
				customer.setUsedCredit(balance);
			if(overx.get(customer.getID()) != null){
				customer.setDebt(overx.get(customer.getID()));
			}
			
			if(overx.get(customer.getPayer()) != null){
				customer.setDebt(overx.get(customer.getPayer()));
			}	
				customer.setCredit(limit + "");
	     
	         		 if(getParent().getProps().getProperty("all.customers.list") != null){
	         			 String ags = getParent().getProps().getProperty("all.customers.list");
	             			StringTokenizer st = new StringTokenizer(ags,",");
		         			while (st.hasMoreTokens()){
		         				Agent a = new Agent(st.nextToken());
		         					customer.addAgent(a);
		         			}
		         		 }
	         		  
		  
	         		 if(visit != null && visit.trim().length() > 0){
	         			customer.setVisit(Converter.getVisitDay(visit)) ;
	         		 }
	         		 
	         		 if(goes) {
	         			   tm.put(customer.getName(), customer);
	     		          //         this.customers.add(customer.getID()); 
	     	         			  payers.put(customer.getID(), customer.getPayer());
	     	         		 	 
	         		 }
	         		
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
			}
			 }
			}catch(Exception r){
        		  r.printStackTrace();
        	  }
	     
 System.out.println("cust suurus " + tm.size());
 
 if(rcompany != null){
 //	Converter.sendCustomerJson(tm, rcompany,true,user,pass);
 }
			return tm;
   }

   private TreeMap payers = null;

private TreeMap<String,Double> stocks = null; 

private HashSet grpsUsed = null;
   public TreeMap getProducts(Agent agent){
	   getSaveStockInfo();
    TreeMap tm = new TreeMap();
   
    
    grpsUsed = new HashSet();
	System.out.println("Tooted algan");
	getProdGroups();
 
	
     try {   
 		XPath xPath =  XPathFactory.newInstance().newXPath();
 		InputSource xml = new InputSource(new StringReader(this.getStringFromFile("ItemMeriEsb.xml")));		
 		NodeList nodeList = (NodeList) xPath.compile("//Item").evaluate(xml, XPathConstants.NODESET);
 	  	 
    	 
 		System.out.println("Products process "  + nodeList.getLength() ) ;
 		
 		for (int i = 0 ; i < nodeList.getLength() ; i++){
			Node o = nodeList.item(i);
			
			String kood = null;
			String top = null;
			String nimi = null;
			String nimi2 = null;
			String nimilit = null;
			String grupp = null;
			String gruppSec = null;
			String basket = null;
			String type = null;
			String latName = null;
			String status = null;
			String unit = null;
			double price = 0;
			double stock = 0;

			double incase = 0;
			double minamount = 0;
			double optamount = 0;	
			
			NodeList ll = o.getChildNodes();
			for (int r = 0 ; r < ll.getLength() ; r++){
			
				Node nn = ll.item(r);
		  if(nn.getNodeName().equals("ptPart")){
			   try{  kood = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }

		  
		  
		  
		  if(nn.getNodeName().equals("ptChr08")){
			   try{  basket = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }

		  if(nn.getNodeName().equals("ptChr02")){
			   try{  type = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptLog01")){
			   try{  top = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  
		  if(nn.getNodeName().equals("ptDesc1")){
			   try{  nimi = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptDesc2")){
			   try{  nimi2 = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptDescLt")){
			   try{  nimilit = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("pgNbr")){
			   try{  grupp = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("pgNbr2")){
			   try{  gruppSec = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptStatus")){
			   try{  status = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  if(nn.getNodeName().equals("ptUm")){
			   try{  unit = nn.getFirstChild().getNodeValue();
			   }catch(Exception e){ }
		   }
		  
		  if(nn.getNodeName().equals("ptPrice")){
			   try{  price = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
          if(nn.getNodeName().equals("inQtyOhCalc")){
			   try{  stock = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
		  
          

          if(nn.getNodeName().equals("incase")){
			   try{  incase = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
          
          

          if(nn.getNodeName().equals("ptDec02")){
			   try{  minamount = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
          
         

          if(nn.getNodeName().equals("ptDec01")){
			   try{  optamount = Double.parseDouble(nn.getFirstChild().getNodeValue());
			   }catch(Exception e){ }
		   }
			}
		
			boolean goes = true;
			if(status != null && status.equals("BLOKUOTA")){
				goes = false;
			}
			
if(kood != null && nimi != null && goes){

	//System.out.println("toode " + kood + " " + nimi + " " + grupp + " " + gruppSec);
		      Product prod = new Product(kood);
		      String add = "";
		      prod.setVat("21");
		      
			if(nimi2 != null){
				nimi = nimi + " " + nimi2 ;
			}
			
			if(nimilit != null && nimilit.trim().length() > 0){
				
				nimi = nimilit;
			}
			
			nimi = nimi + " " + kood;
			
			if(basket != null){
				prod.setGroup(basket);
			 if(baskets.get(basket) != null){
				String aaa =  baskets.get(basket).toString();
				if(aaa.length()>3){
				    add = aaa.substring(0,3) + " ";
				}else{
					add = aaa + " ";
				}
			 }
				//if(!grpsUsed.contains(grupp)){
					//grpsUsed.add(grupp);
				//}
			}
			
			prod.setName(Converter.enCodeEst( add + nimi));
			
			
			if(top != null && top.equals("true")){
				prod.setStatusTop("1");
			}
				
				
			if(type != null){
				prod.setGroupSecond(type);
				
			}
			
			if(unit != null){
				prod.setUnit(unit);
				if(unit.equals("VN")){
					prod.setOptAmountInUnits(1);
					prod.setNoCommas(true);
				}
			}
			
			if(price > 0){
				prod.setPrice(price);
			}
			

			if(minamount > 0){
				prod.setMinAmountInUnits(minamount);
			}
			
			
			if(optamount > 0){
				prod.setOptAmountInUnits(optamount);
			}
			
			
			if(incase > 0){
				prod.setInCases(incase);
			}
			
			
if(stocks.get(kood) != null) {
	stock = stocks.get(kood);
	if(stock < 10000){
    	prod.setStock(stock);
    }
	
}
			
			
			prodids.add(prod.getID());
			
			if(prices.get(prod.getID()) != null){
				try{
					prod.setPrice(Double.parseDouble(prices.get(prod.getID())+""));
				}catch(Exception e){
					
				}
				//panenme toote juhul kui hind on kuskilolemas.
                 if(!kood.startsWith("9")){

    				 tm.put(prod.getName() , prod); 
    			  prodHash.put(prod.getID(), prod);
    	        		  
                 } 
			}
}       
 		}
         	  }catch(Exception e){
         		  e.printStackTrace();
         	  }
     
     System.out.println("Tooted läbi " + tm.size()); 
        
     return tm;
   }

   
private Vector prodids = new Vector();
   
   public String getAssortmentStr(Agent agent){
    return null;
  }

   
   private String getKlient(String kli){
	   String klient = null ;
	   return klient ;
   }
   
   
    public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
	   
	   try{
		     InputSource xml = new InputSource(new StringReader(getStringFromFile("CustProductMeriEsb.xml")));	
				XPath xPath =  XPathFactory.newInstance().newXPath();
				NodeList nodeList = (NodeList) xPath.compile("//Line").evaluate(xml, XPathConstants.NODESET);
				System.out.println("CP nodelist suurus " + nodeList.getLength());
				
				for (int i = 0 ; i < nodeList.getLength() ; i++){
				
					Node o = nodeList.item(i);
					
					String saaja = null;
					String toode = null;
	                String kuupaev =  null;
	                String lastbill =  null;
	                double kogus = 0;
	                double lastprice = 0;
	                if(o.getNodeName().equals("Line")){
	                
					NodeList ll = o.getChildNodes();
				for (int r = 0 ; r < ll.getLength() ; r++){
					Node nn = ll.item(r);
					
					/*  
<ihCust>10000941</ihCust>
<ihInvDate>2015-09-28</ihInvDate>
<idhInvNbr>MPA94635</idhInvNbr>
<idhPart>611037</idhPart>
<idhQtyInv>2.0</idhQtyInv>
<idhPrice>1.24</idhPrice>
</Line>
*/
					
					if(nn.getNodeName().equals("idhInvNbr")){
						   try{  lastbill = nn.getFirstChild().getNodeValue();
						   }catch(Exception e){ }
					   }
			  if(nn.getNodeName().equals("ihCust")){
				   try{  saaja = nn.getFirstChild().getNodeValue();
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("idhPart")){
				   try{  toode = nn.getFirstChild().getNodeValue();
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("ihInvDate")){
				   try{  kuupaev = nn.getFirstChild().getNodeValue().substring(5);
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("idhQtyInv")){
				   try{  kogus = Double.parseDouble(nn.getFirstChild().getNodeValue());
				   }catch(Exception e){ }
			   }
			  if(nn.getNodeName().equals("idhPrice")){
				   try{  lastprice = Double.parseDouble(nn.getFirstChild().getNodeValue());
				   }catch(Exception e){ }
			   }
			  
			  
			  // System.out.println("Tere " + saaja + " " + toode + " " +  kogus);
			    if(saaja != null && toode != null){
			    	CustProd cp = new CustProd(saaja, toode, kogus, 0, 0,kuupaev,lastprice,0,0," ");
			    	cp.setAbiKood(lastbill);
					  custProds.add(cp);
				}
				}	
	                }
	
				 
				    
			    }
				
			
	   }catch(Exception e){
		   e.printStackTrace();
	   }
	   
	   System.out.println("Tere custprods " + custProds.size());
     return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "BiovelaUus";
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
   private TreeMap baskets = new TreeMap();
   private TreeMap types = new TreeMap();
   private TreeMap prodSorting = new TreeMap();
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   private String session = "";
 
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
     
     private Hashtable docCust = null;
     private Hashtable brandplans = null;
     private Hashtable basketnames = null;
     private Hashtable branddones = null;
     
     
  public void setBulkSales(Map agents) {
	         
	  
	  // loeme sisse kuu plaanid igale managerile
	    brandplans = new Hashtable();
	    basketnames = new Hashtable();
        branddones = new Hashtable();
	    
	   try{
		

		   BufferedReader br = new BufferedReader(new FileReader( "plan/GroupNames.csv"));
		    String line = null;
		    line = br.readLine();
		    while((line = br.readLine()) != null){
		    	StringTokenizer st = new StringTokenizer(line, ";");
			       if (st.hasMoreTokens()){
			    	
			    	   String code = st.nextToken();
			    	   String name = st.nextToken();
			    	   basketnames.put(code, name); 
			    	   
			       }   
		       }
		  
		   
	   br = new BufferedReader(new FileReader( "plan/PlanData.csv"));
	    line = null;
	    line = br.readLine();
	    while((line = br.readLine()) != null){
	    	StringTokenizer st = new StringTokenizer(line, ";");
		       if (st.hasMoreTokens()){
		    	   String period = st.nextToken();
		    	   String basket = st.nextToken();
		    	   String agent = st.nextToken();
		    	 
		    	   String planeur = st.nextToken();
		    	   if(planeur.indexOf(".") >0){
		    		   planeur = planeur.substring(0,planeur.indexOf(".")) + planeur.substring(planeur.indexOf(".")+1);
		    	   }
		    	   
		    	   String plankg = st.nextToken();
		    	   if(plankg.indexOf(".") >0){
		    		   plankg = plankg.substring(0,plankg.indexOf(".")) + plankg.substring(plankg.indexOf(".")+1);
		    	   }
		    	   
		    	   Toode tt = new Toode(basket);
		        	  tt.setPlancur(Double.parseDouble(planeur));
		        	  tt.setPlanvol(Double.parseDouble(plankg));
		        	  Vector asjad = new Vector();
	                  if(brandplans.get(agent) != null){
	                	   asjad = (Vector)brandplans.get(agent);
	                  }
	                  asjad.add(tt);
	                  brandplans.put(agent, asjad);
	                  
		       }
		         
		       
		       
		       
		
	    }
	    
	       
		   br = new BufferedReader(new FileReader( "plan/SalesData.csv"));
		    line = null;
		    line = br.readLine();
		    while((line = br.readLine()) != null){
		    	StringTokenizer st = new StringTokenizer(line, ";");
			       if (st.hasMoreTokens()){
			    	   //01.07.2015	1001	1074	868	385	1.103	468

			    	   String period = st.nextToken();
			    	   String basket = st.nextToken();
			    	   String agent = st.nextToken();
			    	 
			    	   String planeur = st.nextToken();
			    	   if(planeur.indexOf(".") >0){
			    		   planeur = planeur.substring(0,planeur.indexOf(".")) + planeur.substring(planeur.indexOf(".")+1);
			    	   }
			    	   
			    	   String plankg = st.nextToken();
			    	   if(plankg.indexOf(".") >0){
			    		   plankg = plankg.substring(0,plankg.indexOf(".")) + plankg.substring(plankg.indexOf(".")+1);
			    	   }
			    	   
			    	   String doneeur = st.nextToken();
			    	   if(doneeur.indexOf(".") >0){
			    		   doneeur = doneeur.substring(0,doneeur.indexOf(".")) + doneeur.substring(planeur.indexOf(".")+1);
			    	   }
			    	   
			    	   String donekg = st.nextToken();
			    	   if(donekg.indexOf(".") >0){
			    		   donekg = donekg.substring(0,donekg.indexOf(".")) + donekg.substring(donekg.indexOf(".")+1);
			    	   }
			    	  
			    	   Toode tt = new Toode(basket);
			        	  tt.setPlancur(Double.parseDouble(planeur));
			        	  tt.setPlanvol(Double.parseDouble(plankg));
			        	  tt.setDonecur(Double.parseDouble(doneeur));
			        	  tt.setDonevol(Double.parseDouble(donekg));
			        	
			    	   
			    	   Vector asjad = new Vector();
		                  if(branddones.get(agent) != null){
		                	   asjad = (Vector)branddones.get(agent);
		                	   boolean added = false;
		                       for(int i = 0;i < asjad.size();i++){
		                    	   Toode ts = (Toode)asjad.elementAt(i);
		                    	 //  System.out.println("TS enne " + ts.toString());
		                    	   if(ts.getID().equals(tt.getID())){
		                    		added = true;  
		                    		ts.setPlancur(ts.getPlancur() + Double.parseDouble(planeur));
                                    ts.setPlanvol(ts.getPlanvol() + Double.parseDouble(plankg));
		                    		ts.setDonecur(ts.getDonecur() + Double.parseDouble(doneeur));
		                    		ts.setDonevol(ts.getDonevol() + Double.parseDouble(donekg));

			                    	//   System.out.println("TS hiljem " + ts.toString());
		                    	   }
		                       }
		                      
		                       if(!added){
		                    	   asjad.add(tt);
				               }
		                       branddones.put(agent, asjad);
				                       
		                  }else{
		                	  asjad.add(tt);
			                  branddones.put(agent, asjad);
			                  
		                  }
			    	   
			        	  
			       }
		    }
	
	    
	    
	    
	   
	   // System.out.println("branddones " + branddones.toString());

	 //   System.out.println("bransplans " + brandplans.toString() );
	    
	   }catch(Exception k){
		   k.printStackTrace();
	   }
		       
		      InputSource xml = new InputSource(new StringReader(getStringFromFile("SalesPersonMeriEsb.xml")));	
				XPath xPath =  XPathFactory.newInstance().newXPath();
				 try{
				NodeList nodeList = (NodeList) xPath.compile("//salesPerson").evaluate(xml, XPathConstants.NODESET);
		
				for (int i = 0 ; i < nodeList.getLength() ; i++){
					Node o = nodeList.item(i);
					String kood = null;
					double sales = 0;
						NodeList ll = o.getChildNodes();
						for (int r = 0 ; r < ll.getLength() ; r++){
							
							Node nn = ll.item(r);
				
							if(nn.getNodeName().equals("spAddr")){
								   try{  kood = nn.getFirstChild().getNodeValue();
								   }catch(Exception e){ }
							   }
				
							  if(nn.getNodeName().equals("calcCurrSales")){
								   try{  sales = Double.parseDouble(nn.getFirstChild().getNodeValue());
								   }catch(Exception e){ }
							       }
					        	}
						//System.out.println("BulkSales " + kood + " " + sales );
						   if(kood != null && agents.get(kood) != null){
							   ((Agent)agents.get(kood)).setMonthSales((int)sales);
							   String asia = "Basket EUR ir KG |";
							    String asi = "";
							   if(branddones.get(kood) != null){
								   
				                	Vector vv =   (Vector)branddones.get(kood);
				                	 Enumeration enume = vv.elements();
				                	 while(enume.hasMoreElements()){
				                		 Toode tt = (Toode)enume.nextElement();
				                		 
				                		 double plankg = 0;
										   double planeur = 0;
										   
										   if(brandplans.get(kood) != null){
											   Vector vvf =   (Vector)brandplans.get(kood);
											   Enumeration enumec = vvf.elements();
							                	 while(enumec.hasMoreElements()){
							                		 Toode tx = (Toode)enumec.nextElement();
							                		 if(tx.getID().equals(tt.getID())){
							                			 plankg = tx.getPlanvol();
							                			 planeur = tx.getPlancur();
							                		 }
							                	 }
							               }
				                		 
				                		 
				                		 
				                		 if(false && tt.getPlancur() != 0 && tt.getDonecur() != 0  ){
				                			asi = asi + ( this.basketnames.get(tt.getID()) + "| " +
				                					("Day: " + (int)(tt.getDonecur()/tt.getPlancur()*100))
				                					+ "% Plan.: " +(int) tt.getPlancur() + " Parda.: " + (int)tt.getDonecur() + "| ");
				                			asi = asi + ("Month: " +((int)(tt.getDonecur()/planeur*100))
				                					+ "% Plan.: " +(int) planeur + "| ");
				                		       
				                			
				                			asi = asi + ("Kg Day: " +
				                					((int)(tt.getDonevol()/tt.getPlanvol()*100))
				                					+ "% Plan.: " +(int) tt.getPlanvol() + " Parda.: " + (int)tt.getDonevol() + "| ");
				                			asi = asi + ("KG Month :  " +
				                					((int)(tt.getDonevol()/plankg*100))
				                					+ "% Plan.: " +(int) plankg + "| | ");
				                		       
				                			
				                		 
				                		   }
				                	    }
				                    }
			                    if(asi.trim().length() > 10){
			                    	((Agent)agents.get(kood)).setStart( Converter.enCodeEst(asia + asi) );
					                   System.out.println( kood + " " + asi);
						        }
						   }
					      }
		 	 
				 }catch(Exception e){
		    		  e.printStackTrace();
		    	 }
	
				 System.out.println("BulkSales tehtud" );
    
	}
  
}
