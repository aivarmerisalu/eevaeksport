package eevaeksport.driver;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.sun.net.ssl.TrustManager;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BalmerkCSVDBDriver extends AbstractDBDriver implements 
               BulkUpdater,Discounter,Manager,PictureMaker, WebReporter, FixLoader {

  public BalmerkCSVDBDriver(EksportPanel parent) {
	super(parent);
	this.subjectFilesTo = parent.getString("filemsg");

	     user =  getParent().getProps().getProperty("rest.user");
	     pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	   try {
	  //  testConn();
	   }catch (Exception e) {
		   e.printStackTrace();
	   }
	     }
  
  
  
	
  
	  String user =  null;
	  String pass = null;
	  String rcompany = null;
  
	  
	  private Hashtable dates = new Hashtable();
	  private TreeMap eanprods = null;

	  @Override
		public void FixLoader() {
		  
		  eanprods =  Converter.loadMore();
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
			this.getProducts(null);
			TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
			System.out.println("Contracts Fixx " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts ,  rcompany,  user ,  pass, "FIXED");
			}
			contracts = Converter.loadSuggFromExcels("excelsugg",eanprods,parent);
			System.out.println("Contracts Sugg " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts ,  rcompany,  user ,  pass, "SUGGESTED");
			}
			
			
			System.exit(0);
		}
	  
  
  
  
  
  
  public String getPilotDayData(Agent agent){
     return null;
   }
   
  private  Map codeMap = new HashMap();
   
   public String getSaveDebtInfo() {
	 

		StringBuffer products = new StringBuffer();
		try {

          TreeMap custs = this.getCustomers(null);

          Iterator itera =  custs.values().iterator();
			while (itera.hasNext()) {
           Customer cc = (Customer) itera.next();
				String kood = cc.getID();
				double volg = cc.getDebt();
				boolean blokk1 = cc.isSalesBlockT();
				boolean blokk2 = cc.isSalesBlock();
				String bl = "0";
				
				if(blokk2 ) {
					bl = "1";
				}
				
				if(blokk1 ) {
					bl = "2";
				}
				
				if (volg > 0 || !bl.equals("0")) {
					products.append(kood + ";"
							+ getParent().getNF().format(volg) + ";" + bl
							+ "\n");
				}
			}
			
			
			

		
		} catch (Exception e) {
			getParent().showError(e);
		}
		
		
		return products.toString();
	  
   }
      
   
   public Vector getPriceMatrixData(){
	   Vector prlists= new Vector();
	   try {
	          
      	 FileReader fr = new FileReader(getDir() + "D365GetPriceMatrix_CustomerPrices.csv");
           BufferedReader reader = new BufferedReader(fr);
         String line = null;
         reader.readLine();
          while((line = reader.readLine()) != null ){

               line = line.replaceAll(","," | ");
          	StringTokenizer st = new StringTokenizer(line, "|");

            if (st.hasMoreTokens()) {
         //	System.out.println( line );

              String prodID = st.nextToken().trim();
               String client = st.nextToken().trim();
               double price = Double.parseDouble(st.nextToken().trim());
               String sdate =  st.nextToken().trim();
	                 String edate = st.nextToken().trim();
	                
               
	            
	             
	             MatrixLine mline = new MatrixLine();
	             mline.setCustID(client);
           
           
           if(sdate != null  && !sdate.startsWith("1753") && sdate.trim().length() > 0 ){
          	 mline.setStartDate(sdate.substring(0,10));
           }
           if(edate != null && !edate.startsWith("1753") && sdate.trim().length() > 0 ){
          	 mline.setEndDate(edate.substring(0,10));
           }
           
           if (prodID != null && prodID.length() > 0) {
             mline.setProdID(prodID);
           }
       
           if (price > 0) {
               mline.setPrice(price);
               prlists.add(mline);
           }
            }
          }
          
          reader.close();
          fr.close();
       }catch(Exception e){
       e.printStackTrace();
       }
       
       
       try {
	          
      	 FileReader fr = new FileReader(getDir() + "D365GetPriceMatrix_GroupPrices.csv");
           BufferedReader reader = new BufferedReader(fr);
         String line = null;
         reader.readLine();
          while((line = reader.readLine()) != null ){

               line = line.replaceAll(","," | ");
          	StringTokenizer st = new StringTokenizer(line, "|");

            if (st.hasMoreTokens()) {
      
              String clGroup = st.nextToken().trim();
              String prodID = st.nextToken().trim();
               double price = Double.parseDouble(st.nextToken().trim());
               String sdate =  st.nextToken().trim();
	                 String edate = st.nextToken().trim();
	             
	             MatrixLine mline = new MatrixLine();
	                mline.setCliGroup(clGroup);
           
           if(sdate != null  && !sdate.startsWith("1753") && sdate.trim().length() > 0 ){
          	 mline.setStartDate(sdate.substring(0,10));
           }
           
           if(edate != null && !edate.startsWith("1753") && sdate.trim().length() > 0 ){
          	 mline.setEndDate(edate.substring(0,10));
           }
           
           if (prodID != null && prodID.length() > 0) {
               mline.setProdID(prodID);
           }
       
           if (price > 0) {
               mline.setPrice(price);
            	 prlists.add(mline); 
             }
            }
          }
          
          reader.close();
          fr.close();
       }catch(Exception e){
       e.printStackTrace();
       }
       
	System.out.println("prlist " + prlists.size());
	   
	   return prlists;
   }
       
       
       



   
   public Hashtable readCustSales(){
	   
	   Hashtable result = new Hashtable();
	   agGrSales = new Hashtable();
	   this.getCustomers(null);
	         try {
	        	Calendar cal =  Calendar.getInstance();
	        	 String date = Converter.getYYYYMMString(cal.getTime());
	          
	        	 FileReader fr = new FileReader(getDir() + "D365CustomerSales" + date + ".csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(";"," | ");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
  
                     String custid = st.nextToken().trim();
                     String group = st.nextToken().trim();
                     double saleskg = 0;
                     try {
                    	 saleskg = Double.parseDouble(st.nextToken().trim()); 
                     }catch(Exception e){
                    	 
                     }
                    
                     double saleseur = 0;
                     try {
                    	 saleseur = Double.parseDouble(st.nextToken().trim()); 
                     }catch(Exception e){
                    	 
                     }
                     Toode tt = new Toode(custid);
                     tt.setGr1(group);
                     tt.setDonecur(saleseur);
                     tt.setDonevol(saleskg);
                     ArrayList arr = new ArrayList();
                     if(result.get(custid) != null) {
                    	 arr = (ArrayList)result.get(custid);
                     }
                     arr.add(tt);
                     result.put(custid, arr);
                    
   	               
                    if (custAgents.get(custid) != null){
                    	ArrayList arrag =  (ArrayList)custAgents.get(custid) ;
                    	for(int u = 0; u< arrag.size(); u++ ) {
                    		String agg = (String)arrag.get(u);
                    		
                    		TreeMap tree = new TreeMap();
                        	if(agGrSales.get(agg) != null) {
                        		tree = (TreeMap)agGrSales.get(agg);
                        	}
                        	Toode dd = new Toode(agg);
                             if(tree.get(group) != null) {
                            	 dd = (Toode)tree.get(group) ;
                             }
                        	dd.addDonecur(saleseur);
                        	dd.addDonevol(saleskg);
                        	tree.put(group, dd);
                        	agGrSales.put(agg, tree);
                    	}
                      }
	       
	             }
	            }
	            
	            reader.close();
	            fr.close();
	         }catch(Exception e){
	         e.printStackTrace();
	         }
		
	        // System.out.println(result.toString());
	    return result;
	      }

   Hashtable agGrSales = null;
   Hashtable custAgents = null;


   public ManagerData getManagerData() throws SQLException{
	   return null;
   }
   public String getProductSortingData(){
   	return null;
   }

 
   public DealerProduct getDealerProductsList() {
       return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   
   
   
   
   
   HashMap stocks = null;



	  public String getSaveStockInfo(){
	    	   stocks = new HashMap();
	  	         StringBuffer products = new StringBuffer();
	         
	         try {
	             FileReader fr = new FileReader(getDir() + "D365GetStocks.csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(","," |");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
	            	   String kood = st.nextToken().trim();
	            	   if(kood.length() > 0) {
	            		try {
	            			String ladu = st.nextToken();
	            			   double seis = Double.parseDouble(st.nextToken());
	      	  	             if(seis > 0){
	      	  	            	 stocks.put(kood, seis+ "");
	      	  	                 products.append(kood +  ";");
	      	  	                 products.append(" ;");
	      	  	                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); 
	      	  	               }		
	            		}catch(Exception e) {
	            		}
	  	             }
	              }
	           }
	             reader.close();
	             fr.close();
	           } catch (Exception e){
	             e.printStackTrace();
	           }
	       //  System.out.println("stocks " + products.toString());
	         getStockBronnData();
	  	  
	  		  return products.toString();
	     }




	  public void getStockBronnData(){
		  StringBuffer sb = new StringBuffer();
	 	    
		  try {
	             FileReader fr = new FileReader(getDir() + "D365GetStockBronnData.csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(","," |");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
	            	   String toode = st.nextToken().trim();
	            	   if(toode.length() > 0) {
	            		try {
	            			String kuhu = st.nextToken();
	            			 double seis = Double.parseDouble(st.nextToken());
	      	  	             if(seis > 0){
	      	  	             sb.append(kuhu + ";" + toode + ";" + seis + "\n" );
	      	  	               }		
	            		}catch(Exception e) {
	            		}
	  	             }
	              }
	           }
	             reader.close();
	             fr.close();
	           } catch (Exception e){
	             e.printStackTrace();
	           }

	 


	 Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "stockbronn.txt", sb.toString());
	 System.out.println(sb.toString());
	 			  

	   }



   public Vector getDebt(String agentCode){
        return this.debtList;
  }


   public String getDealerCustomerCSV(String agent){
    return null;
    }

   public String  getCustProdDiscounts(Agent agent){
    return null;
    }

   public TreeMap getProdGroups() throws SQLException {

	    TreeMap   tm = new TreeMap();
	    //  setTreeGroup(true);
	      try {
	    	  FileReader fr = new FileReader(getDir() + "D365GetProductGroups.csv");
	    	  BufferedReader reader = new BufferedReader(fr);
	    	  String line = null;
	  
	    	  line=reader.readLine();
		     while((line = reader.readLine()) != null ){
		        line = line.replaceAll(",","| ");
		        StringTokenizer st = new StringTokenizer(line, "|");
		        
		        //GroupCode		ParentGrCode	Description

		         if (st.hasMoreTokens()){
		             String groupcode = st.nextToken().trim();
		             String name = st.nextToken().trim();

		        	 if(name.length()> 2) {
		        		 tm.put(groupcode, Converter.enCodeEst(name));
		 		        
		        	 }
		           }
		         }
	     reader.close();
	     fr.close();
		    	
	       } catch (IOException e){
	         e.printStackTrace();
	       }
	          return tm;
   
 
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
	   TreeMap   tm = new TreeMap();
	   return tm;
   }



   private Hashtable custagents = new Hashtable();
   private String teamx = null;
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
     
   
     Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
     agents = new TreeMap();
     try {
       FileReader fr = new FileReader(getDir() + "D365GetAgentList.csv");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     reader.readLine();
      while((line = reader.readLine()) != null ){

           line = line.replaceAll(","," |");
      	StringTokenizer st = new StringTokenizer(line, "|");
        	String id = st.nextToken().trim();
        	
            Agent agent = new Agent(id);
            agent.setName(Converter.enCodeEst(st.nextToken().trim()));
          
            
                tm.put(agent.getName(), agent);	
                agents.put(agent.getID(), agent);
                agent1.put(agent.getID(), agent);
          
        
      }
       reader.close();
       fr.close();
      // System.out.println(codeMap.toString());
     } catch (Exception e){
       e.printStackTrace();
     }
     
  
     return tm;
   }




       public String getDistribution() {
         return null;
       }
       
       
       private Hashtable dueSums = new Hashtable();
       private Hashtable usedCredit = new Hashtable();

       
       
       private Date getDate(String mmddyy){
    	  	long lon = 0;
    	  	Calendar cal = Calendar.getInstance();
    	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
    	  // System.out.println(cal.getTime()); 
    	   Date date = new Date(lon);
    	   date.setTime(cal.getTimeInMillis());
    	    return date;
    		
    	  }
      
       


       private TreeMap agent1 = new TreeMap();
   public TreeMap getCustomers(Agent agent){
     TreeMap tm = new TreeMap();
     custAgents = new Hashtable();
     debtList = new Vector();
   

     try{
  	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
  		
  }catch(Exception e){
  	e.printStackTrace();
  }
    String tsales =  parent.getProps().getProperty("telesales");
    HashSet tsal = new HashSet();
    if(tsales != null) {
    	StringTokenizer st = new StringTokenizer(tsales,",");
    	while (st.hasMoreElements()) {
    		tsal.add(st.nextElement());
    	}
    }
  //  System.out.println("Telesales " + tsales + " " + tsal.toString() );

    HashSet payers = new HashSet();

    try {
    FileReader fr = new FileReader(getDir() + "D365GetPayers.csv");
	 BufferedReader reader = new BufferedReader(fr);
	 reader.readLine();
	 
	 String line = null;
	 
    while((line = reader.readLine()) != null){
    	 line = line.replaceAll(","," |");
         StringTokenizer st = new StringTokenizer(line, "|");
            	while (st.hasMoreTokens()){
            		payers.add(st.nextToken());
            	}
      	
    }
    reader.close();
    
    }catch(Exception e) {
     e.printStackTrace();	
    }
    
    System.out.println("payers " + payers.size());   
    
     try {
     FileReader fr = new FileReader(getDir() + "D365GetDebtList.csv");
	 BufferedReader reader = new BufferedReader(fr);
	 reader.readLine();
	 
	 String line = null;
	 
     while((line = reader.readLine()) != null){
    	 
    	 line = line.replaceAll(","," |");
        StringTokenizer st = new StringTokenizer(line, "|");
           	while (st.hasMoreTokens()){
     		String custid = st.nextToken().trim();
           		String number = st.nextToken().trim();
           		String duedate = st.nextToken().trim();
           		String volg = st.nextToken().trim();
           		String date = st.nextToken().trim();
           		String days = st.nextToken().trim();
           		String payer = st.nextToken().trim();
	           
	        try {
	        		 double vv = Double.parseDouble(volg);
	        		 if(vv != 0){
	        			
	        			 Debt dd = new Debt();
				     				  dd.setCustID(custid);
				     				  dd.setPayerID(payer);
				     				  dd.setDoknr(number);
				     				  dd.setDate(getDate(date));
				     				  Calendar cc = Calendar.getInstance();
				     				   cc.setTime(getDate(duedate));
				     				   dd.setDuedate(cc.getTime());
				     				   dd.setDue(days);  
				     				   dd.setSum(vv);
				     						  
				     				  if(dueSums.get(payer) != null){
				     					  double ss = Double.parseDouble(dueSums.get(payer) + "")+ vv ;
				     					 dueSums.put(payer, ss);    
				     				  }else{
				     					 dueSums.put(payer, volg); 
				     				  }
				     				 debtList.add(dd);


	     				  if(usedCredit.get(payer) != null){
	     					  double ss = Double.parseDouble(usedCredit.get(payer) + "")+ vv ;
	     					 usedCredit.put(payer, ss);    
	     				  }else{
	     					 usedCredit.put(payer, volg); 
	     				  }
	        			 
	        		 }
	        }catch(Exception e) {
	        	
	        }
           	}
     }
     

     
	  fr = new FileReader(getDir() + "D365GetCustomers.csv");
	  reader = new BufferedReader(fr);
      line = null;
 
      
      Properties propse =  new Properties();
	    try{
	    	propse =  EksportFrame.loadProps("emails.txt");
	  	  
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
      
     line=reader.readLine();
     while((line = reader.readLine()) != null){
    	 
    	 line = line.replaceAll(","," | ");
        StringTokenizer st = new StringTokenizer(line, "|");
       if (st.hasMoreTokens()){
    	// name,nimelisa,namealias,accountnum,payer,ssordereraccount,debt,used,linedisc,address,visit,agent,term,block,payerblock,phone,info,grupp,confmail,abiagent,abipaev,credit,chain

    	   String name = st.nextToken().trim();
    	   if(name.startsWith("\"")) {
        	   name = name + " " + st.nextToken().trim();
        	   if(!name.endsWith("\"")) {
        		   name = name + " " + st.nextToken().trim();
            	}
        	   if(!name.endsWith("\"")) {
        		   name = name + " " + st.nextToken().trim();
               }   
        	   if(!name.endsWith("\"")) {
        		   name = name + " " + st.nextToken().trim();
               }   
        	   name = name.replaceAll("\"", " ").trim();
           }
    	   try {
    	   String name2 = st.nextToken().trim();
    	   String nameAlias = st.nextToken().trim();
           String id = st.nextToken().trim();
           String payer = st.nextToken().trim();
    	   String firma = st.nextToken().trim();
    	   
    	   if(firma.equals("DUNKER")) {
           String deptdue = st.nextToken().trim();
           String usedcredit = st.nextToken().trim();
           String custdisc = st.nextToken().trim();
           
           if(custdisc.startsWith("\"")) {
        	   custdisc = custdisc + " " + st.nextToken().trim();
        	   custdisc = custdisc.replaceAll("\"", " ").trim();
           }
           

           
           String address = st.nextToken().trim();
        //   System.out.println(address);
           if(address.startsWith("\"")) {
        	   address = address + " " + st.nextToken().trim();
        	   if(!address.endsWith("\"")) {
            	   address = address + " " + st.nextToken().trim();
            	}
        	   
        	   if(!address.endsWith("\"")) {
            	   address = address + " " + st.nextToken().trim();
               }   

        	   if(!address.endsWith("\"")) {
            	   address = address + " " + st.nextToken().trim();
               }   
        	   address = address.replaceAll("\"", " ").trim();
             //  System.out.println("address " + address);
           }

           String visit = st.nextToken().trim();
           String agentcode = st.nextToken().trim();
           String payterm = st.nextToken().trim();
           String block = st.nextToken().trim();
           String blockPayer = st.nextToken().trim();

           String contactphone = st.nextToken().trim();

           String contactname = st.nextToken().trim();
           String grupp = st.nextToken().trim();
           
           String confmail = st.nextToken().trim();
           String abiagent = st.nextToken().trim();
           String abipaev = st.nextToken().trim();
           String crlimit = st.nextToken().trim();
           String chain = st.nextToken().trim();

  
           Customer customer = new Customer(id);
           if(grupp.length() > 0) {
            customer.setGroup(grupp.trim());
           }
           
           if(chain.length() > 0) {
               customer.setChain(chain);
              }
        	 customer.setAddress(Converter.enCodeEst(Converter.adressLink(address,"Eesti")));           
           
           
           customer.setName(Converter.enCodeEst(name) + " " + id );
           
	       	double dept = 0;
	       	try{
	       		dept = Double.parseDouble(deptdue);
	       		customer.setDebt(dept);
	       	}catch(Exception e ){
	       	}
	       	double used = 0;
	       	try{
	       		used = Double.parseDouble(usedcredit);
	       		customer.setUsedCredit(used);
	       	}catch(Exception e ){
	       	}
           
           if(agentcode.length() > 0) {
        	   
        	   customer.addAgent(new Agent(agentcode));
        	   
        	   Agent aa1 = (Agent)agent1.get(agentcode);
            	if(aa1 != null) {
            	
        		Vector vec = aa1.getDubbler();
        		if(vec!= null && vec.size() >0){
        			Enumeration en = vec.elements();
        			while(en.hasMoreElements()){
        				String age = (String) en.nextElement();
        				customer.addAgent(new Agent(age));
        			}
        		}
            }
        	   
        	   
        	 //  System.out.println("ag " + agentcode);
        	   ArrayList arr = new ArrayList();
        	   if(custAgents.get(customer.getID()) != null) {
        		   arr = (ArrayList) custAgents.get(customer.getID());
        	   }
        	   arr.add(agentcode);
        	   custAgents.put(customer.getID(), arr);
        	   if(propse.containsKey(agentcode)) {
        		   Iterator itera =     tsal.iterator();
                   while(itera.hasNext()) {
                	   customer.addAgent(new Agent(itera.next()+""));
                   }  
        	   }
        	   
         if ( agents.get(agentcode.trim()) != null){
   			  Agent aa = (Agent)agents.get(agentcode.trim());
   			  Vector vec = aa.getDubbler();
   				if(vec!= null && vec.size() >0){
   					Enumeration en = vec.elements();
   					while(en.hasMoreElements()){
   						String ag = (String) en.nextElement();
   						customer.addAgent(new Agent(ag));
   					}
   				}
   			}
        	  
           }
           
           if(customer.getID().equals("ERAISIK") || customer.getID().equals("PAKKUMINE") || 
        		   customer.getID().equals("MUUD") ){
           	Enumeration enume = getParent().getAgentsHash().elements();
               while (enume.hasMoreElements()) {
                 Agent a = (Agent) enume.nextElement();
                 customer.addAgent(a);
               }
           }
           
           
           
           String cont = "";
           if(contactname.length() > 0) {
        	   cont=contactname;
           }

           if(contactphone.length() > 0) {
        	   cont=cont + " Tel:" + contactphone.trim();
           }
           
           if (cont.length()>0){    	
              customer.setPersonalData(Converter.enCodeEst(cont.trim()));
         	}
           

         if(payer.length() > 0){
             customer.setPayer(payer);  
         }
         
             
    	double limit = 0;
    	try{
    		limit = Double.parseDouble(crlimit);
    		if(limit > 0) {
    			customer.setCredit(limit + "");	
      		}
    	
    	}catch(Exception e ){
    	}
    	
       
        
        if(payterm.length() > 0) {
        	customer.setPaymTerm(payterm);
        }
   
        if(custdisc.trim().length() > 0) {
        	try {
        	customer.setDiscount(Double.parseDouble(custdisc));
        	}catch(Exception e){
        		
        	}
        }
        
        if(block != null && block.trim().equals("B")) {
        	customer.setSalesBlockT(true);
        }
          boolean goes = true;
          
          if(payers.contains(customer.getID())) {
        	  goes = false;
          }
          if(goes) {
	     tm.put(Converter.cleanLitLatEst(name + " "+ id), customer);
          }
        }
       
       }catch(Exception e) {
    	 //  e.printStackTrace();
       }
     }}
     
     reader.close();
     fr.close();
    	
     } catch (IOException e){
       e.printStackTrace();
     }
     
     Customer cccr = new Customer("OTHERSTUFF");
     cccr.setName("x Muud Tegevused");
     cccr.setAddress("Kuskil kohas");

    	Enumeration enume = getParent().getAgentsHash().elements();
        while (enume.hasMoreElements()) {
          Agent a = (Agent) enume.nextElement();
          cccr.addAgent(a);
        }
   
        tm.put("x Muud Tegevused", cccr);
    
     return tm;
   }

private Hashtable prodNames = null;
   

   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     if(eanprods == null) {
    	 eanprods = new TreeMap();
     }
     
if(stocks == null) {
	getSaveStockInfo();
}
     try {

     	//laoseisude lugemine ja liitmine algab
          
    	
         String line = null;
         
		 FileReader fr = new FileReader(getDir() + "D365GetProducts.csv");
		 BufferedReader reader = new BufferedReader(fr);

        reader.readLine();
        while(( line = reader.readLine()) != null ){
        line = line.replaceAll(","," |");
        
        line = line.replaceAll("\"\"", "''");
     
       StringTokenizer st = new StringTokenizer(line, "|");

       if (st.hasMoreTokens()){
    	 //  System.out.println(line);
    	 //  itemid,nimetus,kastis,alusel,price,ean,grupp,unit,vat,alegr,tarne_kp
    	   // uus itemid,nimetus,alkohol,kastis,alusel,price,ean,grupp,unit,vat,alegr,tarne_kp
    	   String id = st.nextToken().trim();
    	   String description = st.nextToken().trim();
           if(description.startsWith("\"") && !description.contains("''") ) {
        	   description = description + " " + st.nextToken().trim();
        	   if(!description.endsWith("\"")) {
        		   description = description + " " + st.nextToken().trim();
            	}
        	   
        	   if(!description.endsWith("\"")) {
        		   description = description + " " + st.nextToken().trim();
               }   

        	   if(!description.endsWith("\"")) {
        		   description = description + " " + st.nextToken().trim();
               }   
        	   description = description.replaceAll("\"", " ").trim();
           }
    	   
       //    WIN10108,Guigal Crozes-Hermitage Rouge 75cl 12.5%,12.5,12,576,30.66,3536650801003,11,pudel,22%,All,
         //  String alko = st.nextToken().trim();
           
           String incase = st.nextToken().trim();
           String onPallet = st.nextToken().trim();
    	   String unitprice = st.nextToken().trim();
    	   String ean = st.nextToken().trim();
    	   String groupcode = st.nextToken().trim();
           String unit = st.nextToken().trim();
    	   String vat = st.nextToken().trim();
    	   String alegr = st.nextToken().trim();
    	   String tarne_KP = null;

    	   if(st.hasMoreTokens()) {
    		   tarne_KP = st.nextToken().trim();
    	   }

    	   Product prod = new Product(id);
    	   if(onPallet.length() > 0) {
    		   try {
    		    prod.setOnPallet(Double.parseDouble(onPallet));
    		   }catch(Exception e) {
    			   
    		   }
    	   }
    	   if(tarne_KP != null) {
    		   prod.setPartii("Saabub : " + tarne_KP.substring(0,10));
    	   }
    	   //System.out.println(description);

       	     prod.setStock(0);
             
       	    if(stocks.get(id) != null) {
           	 if(Double.parseDouble(stocks.get(id)+"") > 0) {
				 prod.setStock(Double.parseDouble(stocks.get(id)+""));
			 }
			 	
            }
		
			 try {

				 if(Double.parseDouble(incase) > 1) {
					 prod.setInCases(Double.parseDouble(incase));
				 } 
			 }catch(Exception e) {
				 
			 }
			 prod.weblink = prod.getID();
			 
			 if(unit.length() > 0) {
				 prod.setUnit(unit);
			 }
			
			 if(groupcode.length() > 0) {
				 prod.setGroup(groupcode);
			 }
			 
			 if(description.length() > 0) {
						 try {
					 if(Double.parseDouble(unitprice) > 0) {
						 prod.setPrice(Double.parseDouble(unitprice));
					 } 
				 }catch(Exception e) {
				 }
				 
				 String add = "";
				 if(ean.length() > 4) {
					 prod.setEAN(ean);
				        int ll  = ean.trim().length();
				        add = " " +  ean.trim().substring(ll-3);
				        System.out.println(add);
				        eanprods.put(ean.trim(), prod.getID());
				   }
				 
				 prod.setName(Converter.enCodeEst(description) + " "+ id + add );
				 prod.restName = description;
				 prod.setVat(vat.replace("%", " ").trim());
				 
				 tm.put(description + prod.getUnit() + prod.getID(), prod);
			 
			 }
			 
			 
			 

         }
       }
        
        reader.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     
     
     return tm;
   }

   
   
   
   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
  try {
	  
     FileReader fr = new FileReader(getDir() + "D365GetCustomerProductsList.csv");
		 BufferedReader reader = new BufferedReader(fr);
		 String line = null;
      
		 reader.readLine();
      while(( line = reader.readLine()) != null ){
    	  line = line.replaceAll(","," |");
    	   StringTokenizer st = new StringTokenizer(line, "|"); 
    	 //  orderaccount,itemid,qty,lastdate,hind,dok
    	   String custID = st.nextToken().trim();
        	String prodID = st.nextToken().trim();
			double sold = 1; 
			try {
			sold = Double.parseDouble(st.nextToken().trim());	
			}catch(Exception e) {
				
			}
			String last = st.nextToken().trim().substring(5,10);
			double lastprice = Double.parseDouble(st.nextToken().trim());	
			String lastdoc = st.nextToken().trim();

			//System.out.println(last + " " + lastdoc);
			CustProd cp = new CustProd(custID, prodID,
					 sold, 0,0, last, lastprice, 0, 0, " ");
	
			cp.setAbiKood(lastdoc);
			custProds.addElement(cp);
      }
      
	  reader.close();
  }catch(Exception e) {
	  e.printStackTrace();
  }
	
  //System.out.println(custProds.toString());
	    return custProds;
	  }
   


   public String getDriverVersion(){
     return "BalmerkCSV";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
	
	private String clear (String ss){
			return ss;
	}
   
   
   public double getSalary(String agentCode){

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   private String subjectFilesTo = null;
   private TreeMap<String,Double> overdebts = new TreeMap<String,Double>();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);

   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

			        
  }

  
public Vector getFixedAssortmentData() {	
	  return null;
}


@Override
public String getStartingTime() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void connectionUp() {
	// TODO Auto-generated method stub
	
}
@Override
public void connectionDown() {
	// TODO Auto-generated method stub
	
}
@Override
public void PictureMaker() {
	String pictssou = parent.getProps().getProperty("pict.dir");
    String pictspda = parent.getProps().getProperty("pictpda.dir");
   
	 try {
	  
    File f = new File(pictspda +"list.txt");
    if (f.exists())f.delete();
    //Kataloogi sisude kustutamine
	 	 File res2 = new File (pictspda);
         String[] ff2 = res2.list();
     	 for(int i = 0;ff2.length>i;i++){
         	//(new File ( pictspda + ff2[i])).delete(); 
         }
//     	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

     StringBuffer buf = new StringBuffer();
     Iterator enume = prods.keySet().iterator();

     while (enume.hasNext()) {
	    	String asi = (String)enume.next();
	    	Product prod = (Product)prods.get(asi);
	    	String toode = prod.getID().trim();  
	    	System.out.println("pildid" + toode);
           if(toode.length() > 0){
            	
	   	  res2 = new File (pictssou);
          ff2 = res2.list();
         
     	 for(int i = 0;ff2.length>i;i++){
         	if( ff2[i].startsWith(toode)){
        //    System.out.println("pilt olemas " + ff2[i]);
        	File in = new File ( pictssou + ff2[i]); 
          	if(in.isFile()){
          	
          		File inpda = new File ( pictspda + prod.getID()+ ".jpg");
          	if(!inpda.exists()){
              	Converter.shrinkAndCopyFile(in,inpda,400);
          	}
          	
      		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
            
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
 System.exit(0);
}



@Override
public void WebReporter() {
	
	  
	  ArrayList<String> filenames = new ArrayList<String>();
	// teeme kataloogi salvestamiseks
			File f = new File("svdata");
			if(!f.exists()) {
				f.mkdir();
			}
			
			
// loeme signatuurid sisse kui on olemas	
			HashSet done = null;
			try {
				done = (HashSet) Converter.readObject("signadone.obj");
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			if(done == null) {
				 done = new HashSet();
			}
			
			
//teeme perioodi valmis
			Calendar algus = Calendar.getInstance();  
		  	Calendar lopp = Calendar.getInstance();  	
		  	Integer days = 20;
		  if(	parent.getProps().getProperty("webreport.days") != null) {
			  try {
				  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
			  }catch(Exception e) {
				  
			  }
		  };

		  	algus.add(Calendar.DATE, -1 * days);
		  	algus.set(Calendar.HOUR, 0);
		  	lopp.set(Calendar.HOUR, 23);
		  	lopp.set(Calendar.MINUTE, 59);
		  	lopp.add(Calendar.DATE, +2);
			
		  	
		  	try {
		  		String fnameBASE = this.getExcelDir() + "reportbase.xlsx";
		  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
		  		XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
				XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
				
				CreationHelper createHelper = wbBASE.getCreationHelper();
				
				 XSSFCellStyle hlinkstyle = wbBASE.createCellStyle();
			      XSSFFont hlinkfont = wbBASE.createFont();
			      hlinkfont.setUnderline(XSSFFont.U_SINGLE);
			      hlinkfont.setColor(IndexedColors.BLUE.getIndex());
			      hlinkstyle.setFont(hlinkfont);

			      System.out.println("files alustan" );
			  		
			  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), rcompany, user, pass,false);
			  		System.out.println("files " + files.toString());
			 
			  		 JSONParser parser = new JSONParser();
			  		org.json.simple.JSONArray cc =  (org.json.simple.JSONArray)parser.parse(new FileReader( "svdata/currSVjson.json"));
		  		//org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), rcompany, user, pass, false, "PRODUCT", null, "test");
		  	 //  System.out.println("cc" + cc.toString() );
		  		//Converter.saveFileInUTF8(f.getPath() + "/currSVjson.json", cc.toString());
		   
		  		int palletrow = 1;
		  		boolean savefile = false;
		  		
		  		for (int i = 0; i < cc.size(); i++) {
		  			org.json.simple.JSONObject cob = (org.json.simple.JSONObject) cc.get(i);
		  		   	String filterOutOfStock = (String)cob.get("resint3").toString();
		  			String dateAndTime = (String)cob.get("recetime");
		  			String dateOnly = dateAndTime.split("T")[0];
		  			String timeOnly = dateAndTime.split("T")[1].split("Z")[0];
		  			timeOnly = timeOnly.split(":")[0] + ":" + timeOnly.split(":")[1];
		  		  String signa = dateAndTime + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") ;
		  		  
		  			if(filterOutOfStock != null && filterOutOfStock.startsWith("333") && done != null && !done.contains(signa )){  	
		  			String filterProblem = (String)cob.get("resstr1");
		  			
		  		  savefile = true;
		  			
		  	    //	String cagid = (String)cob.get("custagent");
		  		//	String cagname = (String)cob.get("custagname");
		  			String agid = (String)cob.get("agentid");
		  			String agname = (String)cob.get("managerName");
		  			if(agname.contains(agid)) {
		  				agname = agname.substring(0,agname.indexOf(agid)).trim();
		  			}
		  			String custname = "";
		  			String custchain = "";
		  			String custaddr = "";
		  			try {
		  				custchain = (String)cob.get("custchain");
		  			} catch(Exception e) {
		  				
		  			}
		  			
		  			try {
		  				custname = (String)cob.get("custname");
		  				if(custname.indexOf(",") != -1) {
		  					custaddr = custname.substring(custname.indexOf(",")+1);
		  					custname = custname.substring(0,custname.indexOf(","));
		  				}
		  			} catch(Exception e) {
		  				
		  			}
		  			
		  		
		  			if(custname.indexOf("VRV")  != -1) {
		  		//	System.out.println("cob " +  cob.toJSONString());
		  		
		  			}
		  			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
		  	    	  parser = new JSONParser();
		  	    	 
		  	    	org.json.simple.JSONObject obj1 = (org.json.simple.JSONObject) parser.parse((cob.get("comment").toString()));
		  	    
		  	    	String comID = "";
		  	    	String comment = "";
		  	    	
		  	    	try {
		  	    		org.json.simple.JSONObject obj2 = (org.json.simple.JSONObject) parser.parse((cob.get("custcomme").toString()));
		  	    		comID = ((String)obj2.get("comID"));
		  	    		comment = ((String)obj2.get("comNa"));
		  	    	}catch(Exception e) {
		  	    	}
		  	    	
		  	    	String typeid = (String)obj1.get("disID");
		  	    	String typename = (String)obj1.get("disNa");
		  	    	Double typekogus = (Double)obj1.get("disAm");
		  	    	
		  	    	String bonusid = (String)obj1.get("bonID");
		  	    	String bonusname = (String)obj1.get("bonNa");
		  	    	Double bonuskogus = 0d;
		  	    	try {
		  	    		bonuskogus = Double.parseDouble((String)obj1.get("bonAm"));
		  	    	}catch(Exception e) {
		  	    	}
		  	    	
		  	    	Date fromdate = null; //new Date((Long)obj1.get("sDay"));
		  	    	Date todate = null; //new Date((Long)obj1.get("eDay"));
		  	    	try {
		  	    	  fromdate = new Date((Long)obj1.get("sDay"));
		  	    	  todate = new Date((Long)obj1.get("eDay"));
		  	  	     } catch(Exception k) {
	  	    		  k.printStackTrace();
	  	    	     }
		  	    	
		  	    	Date created = new Date(Long.parseLong(((String)cob.get("resstr3"))));
		  	    	
		  	  
		  	    	String cuname = (String)cob.get("custname");
	  				String cuid = (String)cob.get("custid");
	  				if(cuname.endsWith(cuid)) {
	  					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
	  				}
		  				
		  				
		  				worksheetBASE.createRow(palletrow);
		  				
		  				worksheetBASE.getRow(palletrow).createCell(0).setCellValue(agname);
		  				
		  			    worksheetBASE.getRow(palletrow).createCell(1).setCellValue(created);
		  				XSSFCell celld3 = worksheetBASE.getRow(palletrow).getCell(1);
		  				XSSFCellStyle cellStyle3 = (XSSFCellStyle)celld3.getCellStyle();
			  	        cellStyle3.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			  	       
		  				worksheetBASE.getRow(palletrow).createCell(2).setCellValue(cuname);
		  				worksheetBASE.getRow(palletrow).createCell(3).setCellValue(custchain);
		  				worksheetBASE.getRow(palletrow).createCell(4).setCellValue(custaddr);

		  				
		  			
		  				//worksheetBASE.getRow(palletrow).createCell(2).setCellValue(typeid);
		  				worksheetBASE.getRow(palletrow).createCell(5).setCellValue(typename);
		  				worksheetBASE.getRow(palletrow).createCell(6).setCellValue(typekogus);
		  				XSSFCell cellk1 = worksheetBASE.getRow(palletrow).getCell(6);
		  				XSSFCellStyle pstyle = worksheetBASE.getRow(0).getCell(6).getCellStyle();
		  				cellk1.setCellStyle(pstyle);
		  				
		  				 
		  				worksheetBASE.getRow(palletrow).createCell(7).setCellValue(fromdate);
		  				XSSFCell celld1 = worksheetBASE.getRow(palletrow).getCell(7);
		  				XSSFCellStyle cellStyle = (XSSFCellStyle)celld1.getCellStyle();
			  	        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
		  				
		  				worksheetBASE.getRow(palletrow).createCell(8).setCellValue(todate);
		  				XSSFCell celld2 = worksheetBASE.getRow(palletrow).getCell(8);
		  				XSSFCellStyle cellStyle2 = (XSSFCellStyle)celld2.getCellStyle();
			  	        cellStyle2.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
			  	        
			  	        
			  	        
			  	        worksheetBASE.getRow(palletrow).createCell(9).setCellValue((String)cob.get("prodgroupname"));
			  	      try {
			  	    	  worksheetBASE.getRow(palletrow).createCell(10).setCellValue((String)cob.get("resstr2"));  
			  	      }catch(Exception e) {
			  	    	  
			  	      }
			  	      
			  	        worksheetBASE.getRow(palletrow).createCell(11).setCellValue((String)cob.get("prodname"));
		  			
		  				//worksheetBASE.getRow(palletrow).createCell(12).setCellValue(bonusid);
		  				
		  				worksheetBASE.getRow(palletrow).createCell(12);
		  				XSSFCell cellk2 = worksheetBASE.getRow(palletrow).getCell(12);
		  				XSSFCellStyle pstyle2 = worksheetBASE.getRow(0).getCell(4).getCellStyle();
		  				cellk2.setCellStyle(pstyle2);
		  				cellk2.setCellValue(bonuskogus);
		  		
		  				// worksheetBASE.getRow(palletrow).createCell(12).setCellValue(comID);
		  				 worksheetBASE.getRow(palletrow).createCell(13).setCellValue(comment);
		  				
		  				
		  				
		  				try {
		  				String foto = null;
		  				org.json.simple.JSONArray phs = (org.json.simple.JSONArray)parser.parse((cob.get("pictureLinks").toString()));
		  				
		  				if(files != null && files.get(cuid) != null && phs != null) {
		  					int a = 0;
		  				//	if(cuid.equals("747724"))
		  					//	System.out.println("phs " + phs.toString());
		  						for(int y = 0; y < phs.size() ; y++) {
		  							String fname = (String)phs.get(y);
		  						
		  					
		  				
		  					ArrayList arr = (ArrayList)files.get(cuid);
		  					for(int e = 0; e<arr.size(); e++) {
		  						Toode t = (Toode)arr.get(e);
		  						foto = t.getLadu();
		  						if(foto != null && foto.indexOf(fname) !=-1 && t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
		  							
		  							
		  							
		  								
		  				  		         Cell  cell = worksheetBASE.getRow(palletrow).createCell(14+a);
		  				  		         cell.setCellValue("Photo Link "+(a+1) );
		  				  		         XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(Hyperlink.LINK_URL);
		  				  		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
		  				  		         cell.setHyperlink((XSSFHyperlink) link);
		  				  		         cell.setCellStyle(hlinkstyle);
		  				  		     a++;
		  				  				
		  						}
		  					}
		  						}
		  						
		  				}
		  				}catch(Exception e) {
		  					//e.printStackTrace();
		  				}
		  				 //URL Link
		  				
		  				
		  				
		  			 
		  				worksheetBASE.getRow(palletrow).createCell(18).setCellValue((String)cob.get("prodid"));
		  				worksheetBASE.getRow(palletrow).createCell(19).setCellValue(agid);
		  				worksheetBASE.getRow(palletrow).createCell(20).setCellValue("LV");
		  				worksheetBASE.getRow(palletrow).createCell(21).setCellValue((String)cob.get("custid"));
		  				worksheetBASE.getRow(palletrow).createCell(22).setCellValue(bonusid);
		  				worksheetBASE.getRow(palletrow).createCell(23).setCellValue(comID);
		  				//
		  				
		  				
		  				
		  			/*	for(int x=0; x<11; x++) {
		  					if(x == 0 || x == 1 || x == 3) {
		  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderCenteredOOS);
		  					}else {
		  						worksheetBASE.getRow(outofstocks).getCell(x).setCellStyle(borderOOS);
		  					}
		  				}*/
		  				palletrow++;
		  				done.add(signa );
		  			
		       			
		       			
		  			}
		  		}
		  		String exceldir = this.getExcelDir() + "reports/";
		  		File ff = new File( exceldir );
		  		if(!ff.exists()) {
		  			ff.mkdir();
		  		}
		  		excelifailBase.close();
		  		
if(savefile) {
FileOutputStream output_file_oos =new FileOutputStream(exceldir + "productPallet-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	//filenames.add(exceldir + "productOOSbase-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");
	wbBASE.write(output_file_oos); //write changes
	output_file_oos.close();  //close the stream
	//System.out.println("report saved " + exceldir + "productPallet-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + ".xlsx");

}else {
//System.out.println("no data to save");
}
		  	
		  	}catch(Exception e) {
				 e.printStackTrace(); 
			  }
		  	
		  	
			try {
				Converter.saveObject(done, "signadone.obj");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		  
		  System.exit(0);

				  	
				  	
	}





}
