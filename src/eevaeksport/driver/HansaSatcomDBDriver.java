
package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.security.GeneralSecurityException;
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
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.htmlgen.HTMLeditor;
import eevaeksport.mailagent.FileSender;
import eevaeksport.util.Converter;
import eevaeksport.util.FileListFilter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HansaSatcomDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter{

  public HansaSatcomDBDriver(EksportPanel parent) {
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

  public void connectionUp(){
      if (con == null){
	try {
           con = DriverManager.getConnection(url);
        
	} catch (Exception e) {
	    getParent().showError(e);
	}
      }
    }

   public String getPilotDayData(Agent agent){
     return null;
   }
	public void setBulkSales(Map agents) {

	}

	private String getBasicAuthenticationEncoding() {
        String userPassword = "API:API432";
        return new String(Base64.encodeBase64(userPassword.getBytes()));
    }
	
	  private String getWeb(String req) throws ParseException, IOException{
			  
			  String uri = "https://books142.excellent.ee:2502/api/1/" + req;
			  System.out.println("weburl " + uri);
			  
			  
			  DefaultHttpClient dhttpclient = new DefaultHttpClient();

		
		      Converter.trustAllHttpsURLConnection();
		      try {
				Converter.doTrustToCertificates();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		        URL myUrl = new URL(uri);
	            HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
	            
	    
	            //httpsurlconnection
	            conn.setRequestProperty("Authorization","Basic "+ getBasicAuthenticationEncoding());
	            conn.connect();
	            
	            
	            InputStream in = conn.getInputStream();
				try {
					Reader reader = new InputStreamReader(in);
					InputStreamReader isr = new InputStreamReader(in, "UTF-8");
					BufferedReader inr = new BufferedReader(isr);
					String inputLine;
                    StringBuffer sb = new StringBuffer();
					while ((inputLine = inr.readLine()) != null) {
					//  System.out.println(inputLine);
					  sb.append(inputLine+ "\n");
					}
					Converter.saveFile(getDir() + req +".xml", sb.toString());

					inr.close();
					reader.close();
					return sb.toString();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (in != null)
						in.close();
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
    	  
    	  
  	public String getSaveStockInfo(){
  		getProducts(new Agent ("stock"));
  		return getSaveStockInfo2();
  	}
	
	  public String getSaveStockInfo2(){
		
	try {
			getWeb("itemstatusvc");
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		  
		  HashSet prodsx = new HashSet();
		  
   	   StringBuffer products = new StringBuffer();
   	   try {
   		 File f = new File(getDir() + "itemstatusvc.xml");
   		
   	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
       
        // System.out.println(root);
      	Node read = getNode(doc, "data");
          for (int i = 0; i < read.getChildNodes().getLength(); i++){
          	  Node rida = read.getChildNodes().item(i);
          	 String kood = null;
  		   String kog = null;
  		   
  		  
  		 for (int u = 0; u < rida.getChildNodes().getLength(); u++){
         	  Node sisu = rida.getChildNodes().item(u);
          	  if(sisu.getNodeName().equals("Code") &&  sisu.getFirstChild() != null){
          		  
          		  kood =  sisu.getFirstChild().getNodeValue();
          		  kog =  getNode(rida,"Instock").getFirstChild().getNodeValue();
          		// System.out.println("rida.getNodeName() " +  kood + " "+ kog);
          		 
          		 
          	   if(kood != null && kood.trim().length() > 0 
          			   && !prodsx.contains(kood)  && !(kood.endsWith("PN"))) {
          		   if(Double.parseDouble(kog) > 0) {
                  	   
          		   products.append(kood +  ";");
                     products.append(" ;");
                     products.append(kog.trim()   +  "\n"); // + dealer +  "\n"); 
                    
                     if(prods != null && prods.contains(kood+"P")) {
                  	   products.append(kood +  "P;");
                         products.append(" ;");
                         products.append(kog.trim()   +  "\n"); // + dealer +  "\n"); 
                        
                     }
                     
                   
                     
                     if(prods != null && prods.contains(kood+"PN")) {
                  	   if (Double.parseDouble(kog) > 0) {
                  		   
                  	   
                  	   products.append(kood +  "PN;");
                         products.append(" ;");
                         products.append(kog.trim()   +  "\n"); // + dealer +  "\n");
                  	   }
                     }
                     try {
                  	   if (Double.parseDouble(kog) > 0) {
                  		   stocks.put(kood, kog.trim());
                    	      stocks.put(kood+"P", kog.trim());
                           stocks.put(kood+"PN", kog.trim());    
                  	   }
                      	
                         
                     }catch(Exception h) {
                  	   
                     }

                     }
                   
                  //   System.out.println("suurus0 "  + " " + kood + " " + kog);
          	   }
          	   if(kood != null && kood.trim().length() > 0  && Double.parseDouble(kog) > 0 
          			   && !prodsx.contains(kood)  && kood.endsWith("PN")) {
          		   stocks.put(kood, kog.trim());
            	      stocks.put(kood.substring(0,kood.length()-1), kog.trim());
            	    stocks.put(kood.substring(0,kood.length()-2), kog.trim());
            	  products.append(kood +  ";");
                products.append(" ;");
                products.append(kog.trim()   +  "\n");
                
                products.append(kood.substring(0,kood.length()-1) +  ";");
                products.append(" ;");
                products.append(kog.trim()   +  "\n");
                
                products.append(kood.substring(0,kood.length()-2) +  ";");
                products.append(" ;");
                products.append(kog.trim()   +  "\n");
            	    
            	    
          	   }
          	   
          	   
          	 
          	prodsx.add(kood);
          	  }  
          	  }
          	  
          }
   	
   	 	


   	   } catch (Exception e) {
     		 e.printStackTrace();  
     	   }  
      return products.toString();
      }


       
       public String getProductSortingData(){
       	return null;
       }

       
       public Vector getFixedAssortmentData() {
        return null;
      }

       

 
   	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
       public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         
         try{
        	    InputStream ins = new FileInputStream(getDir() + PRODUCT_PRICES_FILE);
        	       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
        	       BufferedReader reader = new BufferedReader(isr);
              String line = null;
              while((line = reader.readLine()) != null){
             
              	
              	if(line.indexOf("pl2") != -1){
              	 	
              		 while((line = reader.readLine()) != null ){
              			line = line.replaceAll("\t","| "); 
              			// System.out.println(line.length() + "p " + line );
              			 StringTokenizer st = new StringTokenizer(line, "|");
              			 
              			if( line.trim().length() > 0 ){
              				st.nextToken().trim();st.nextToken().trim();st.nextToken().trim();
              				String hk = st.nextToken().trim();
              				st.nextToken().trim();
              				String prodID = st.nextToken().trim();
              				try {
              				
              				double price = Double.parseDouble(st.nextToken().trim().replaceAll(",", ".").replaceAll(" ", ""));
              				st.nextToken().trim();
              				String custID = st.nextToken().trim();
              				
              			MatrixLine mline = new MatrixLine();
              			  
              			  mline.setProdID(prodID);
              			   mline.setPrice(price);
              			   
              			  if(hk.trim().length() > 0){
              				  mline.setCliGroup(hk);  
              			  }else{
              				  mline.setCustID(custID);
              			  }
              			 
                      if(custID.length() >0 || hk.length() > 0){
                    	  lines.add(mline);
                      }
                  	
              				}catch(Exception k) {
              					
              				}
                       
              		 }else if(line.trim().length() != 0){
              			// ha = st.nextToken().trim();
              		 }
              	   }
              	}
              }
         }catch(Exception e){
        	 e.printStackTrace();
         }

         return lines;
       }
       
   	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(6,10 ));
		int MONTH = Integer.parseInt(kuup.substring(4, 5))-1;
		int DAY = Integer.parseInt(kuup.substring(0, 2));
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}
   	private boolean isThisMonth(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(6, 7))-1;
		if(cal.get(Calendar.YEAR) == YEAR && cal.get(Calendar.MONTH) == MONTH){
			return true;
		}
		return false;
	}
   public Vector getDebt(String agentCode){
    Vector list = new Vector();

    try {

        InputStream ins = new FileInputStream(getDir() + "volg.TXT");
	       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
	       BufferedReader reader = new BufferedReader(isr);
  
      String line = null;    //	System.out.println(line);

TreeMap tr = new TreeMap();
      while((line = reader.readLine()) != null){
      	
      if(line.indexOf("IPVc")!= -1){
      	 while((line = reader.readLine()) != null){
      	 if(!line.startsWith("2\t") && line.length()>0){
      	 
    	line = line.replaceAll("\t","; ");
    	
    	//System.out.println(line);
    	 StringTokenizer st = new StringTokenizer(line, ";");
    	 st.nextToken();
         st.nextToken();
      	String arve = st.nextToken().trim(); 
      double eek = 1;
      	for(int i = 0;i<10;i++){
      		String asi = st.nextToken();
      		
      	}
      	String token = st.nextToken().replaceAll(",",".").replaceAll(" ","");
      	try{
      		double dd = Double.parseDouble(token)/eek;
      		token = dd + "";
      	}catch(Exception e ){
      		
      	}
      	tr.put(arve,token);
        }	
   	   }
      }	
   	 }
    
        ins = new FileInputStream(getDir() + "volg.TXT");
        isr = new InputStreamReader(ins,"UTF-8");
        reader = new BufferedReader(isr);
 
     line = null;    //	System.out.println(line);


    while((line = reader.readLine()) != null){
    if(line.indexOf("IVVc")!= -1){
    
    while((line = reader.readLine()) != null && !line.startsWith("IPVc") ){
    	if( !line.startsWith("1\t")&& !line.startsWith("3\t") && line.length()>0){
    	
    	line = line.replaceAll("\t","; ");
    //System.out.println(line);
   
    StringTokenizer st = new StringTokenizer(line, ";");
	Debt debt = new Debt();
	if (st.hasMoreTokens()) {
			String arve = st.nextToken().trim();
			String miinus = (String) tr.get(arve);
			double min = 0;
			if (miinus != null){
				min = Double.parseDouble(miinus);
			}
			debt.setDoknr(arve);
		st.nextToken();
		String id = st.nextToken().trim();
		debt.setCustID(id);
		
		if (st.hasMoreTokens()) {
			String kuup = st.nextToken().trim();
			try {
				
			
			debt.setDuedate(getDate(kuup));
			
		
		
		
		double kordaja = 1;
		for (int i =0;i<44;i++){
			String asi = st.nextToken();
		if(i == 17 && asi.trim().equals("3")){
			kordaja = -1;
		}
		
		}
			
		
		if (st.hasMoreTokens()) {
			String sum = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
			double summa = Math.round(Double.parseDouble(sum) *1.20 *100) * kordaja;
			debt.setSum(summa/100-(min));
		 }

		
		
		
		if(debt.getDuedate() != null && debt.getDuedate().before(new java.util.Date())){
			String open  = (String)overdue.get(debt.getCustID());
			if (open != null ){
				double res = Double.parseDouble(open) + debt.getSum();
				overdue.put(debt.getCustID(),res+"");
			}else{
				overdue.put(debt.getCustID(),debt.getSum()+"");	
			}
		}
		
		
		
		String open  = (String)creditused.get(debt.getCustID());
		if (open != null ){
			double res = Double.parseDouble(open) + debt.getSum();
			creditused.put(debt.getCustID(),res+"");
		}else{
			creditused.put(debt.getCustID(),debt.getSum()+"");	
		}
		
		
	int due = getDue(debt.getDuedate().getTime()) ;
	if(due < 400){
		debt.setDue(due + "");
		list.addElement(debt);
	    }
			}catch(Exception r) {
				
			}
	   }
		
      }
    	}
    }
    }
	
    }
    
    } catch (Exception e){
      e.printStackTrace();
    }
    return list;
   }

  
   private int getDue(long dd){
      int res = 0;
      res =(int) ((-dd+(new java.util.Date()).getTime())/86400000) ;
   //   System.out.println("res " + res + " dd" + dd + " " +(( -dd+(new java.util.Date()).getTime())/86400000));
      return res ;
   }
   
   public void connectionDown(){
   }
   
   ArrayList<String> prods = null;
   
   private void makePictureCat(){
	     
   this.getProducts(null);
      	
      	String picts = parent.getProps().getProperty("pict.dir");
       String pictspda = parent.getProps().getProperty("pictpda.dir");
    	  

      	try {
    	
  
      	
      
      
           StringBuffer buf = new StringBuffer();
           
           File d  = new File(picts);
           
      	for(String toode : prods) {
      	    	
      		 File in = new File ( picts + toode +  ".jpg"); 

            toode = toode.replace('/', '_');
            toode = toode.replace('/', '_');
      	    	     
                  
           
         	if(in.isFile()){    
         	 
         	File inpda = new File ( pictspda + toode+ ".jpg"); 
         	 if(!inpda.exists()){
         		Converter.shrinkAndCopyFile(in,inpda);
        	  }
         
         	
           String len = "0";
           if(inpda.exists()){
             len =	inpda.length() + "";
             } 
           buf.append(toode+".jpg;"  + len + "\n");
           }
         	}
            
           
                  File f = new File("list.txt");
                  if (f.exists())f.delete();
                  
                  try {
              		PrintWriter os = new PrintWriter(new FileWriter(
              				  "list.txt", true));
              		os.write(buf.toString());
              		os.close();
              	   File fff =  new File (pictspda +  "list.txt");
        	       Converter.copyFile(f,fff);  
              	
              	  }catch (Exception ex) {
              		ex.printStackTrace();
              	  }
          	  
      	} catch (Exception e) {
      	    getParent().showError(e);
      	}
      
   }
 
   public String getSaveDebtInfo() {
	   makePictureCat();
   	System.exit(0);
   	return null;
   }
   
   public String getAssortmentStr(Agent agent){
   	return null;
   }

           public String getDealerCustomerCSV(String agent){
            return null;
            }

                                               public String  getCustProdDiscounts(Agent agent){
                                                return null;
                                                }

   public TreeMap getProdGroups() throws SQLException {
    TreeMap tm = new TreeMap();

   	try {
        
        InputStream ins = new FileInputStream(getDir() + PRODUCT_FILE);
	       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
	       BufferedReader reader = new BufferedReader(isr);
      String line = null;

      while((line = reader.readLine()) != null){
      	
      	// System.out.println(line);
      	if(line.indexOf("ITVc2") != -1){
      		
      	//	System.out.println("###" + line);
      		while((line = reader.readLine()) != null && line.indexOf("artikel5") == -1){
      			 line = line.replaceAll(";"," ");
         		 line = line.replaceAll("\t","; ");
      			// System.out.println(line);
      			 
        StringTokenizer st = new StringTokenizer(line, ";");
     
        if (st.hasMoreTokens()){
        	st.nextToken();st.nextToken();st.nextToken();
          
        	String id = st.nextToken().trim();
         
          if (st.hasMoreTokens()){
            String name = st.nextToken().trim();
            if (name.equals("")) name = "<" + id + ">";
            	 tm.put(id, Converter.enCodeEst(clear(name)));    
            	// System.out.println("see " + id + " " + name);
          }       
        }
      }	
      		
  		}
      
      
      }
      } catch (Exception e){
        e.printStackTrace();
      }
      return tm;
   }
   public Map<String, String[]> getChartData(String label, String type) {
	   Map<String, String[]> agentData = new HashMap<>();
	   if (type.equals("Pie") || type.equals("Donut") || type.equals("Bar")) {
	       agentData.put("EEE2", new String[] { "['" + "Erik" + " (" + "EEE2"+ ")'", "350" + "]," });
	       agentData.put("AIV1", new String[] { "['" + "Aivar" + " (" + "AIV1"+ ")'", "500" + "]," });
	       agentData.put("KAT1", new String[] { "['" + "Katrin" + " (" + "KAT1"+ ")'", "430" + "]," });
	   }
	   if (type.equals("Table")){
	       agentData.put("MIK", new String[] { "['" + "Mike" + "', {v: 10000}, true]" });
	       agentData.put("JIM", new String[] { "['Jim', {v: 8000}, false]" });
	       agentData.put("ALI1", new String[] { "['Alice', {v: 12500}, true]" });
	       agentData.put("ALI", new String[] { "['Karl', {v: 500}, true]" });
	   }
	   if (type.equals("Gauge")){
	       agentData.put("Memory", new String[] { "['Memory', 80]" });
	       agentData.put("CPU", new String[] { "['CPU', 55]" });
		   
	   }
       
       
       agentData.put("Label",new String[] {label });
       agentData.put("Type",new String[] {type });
       
       return agentData;
   }

   public TreeMap getAgents(){
	   Map<String, String[]> agentData = getChartData("Testime", "Donut");
       Map<String, String[]> agentData2 = getChartData("TestimeGauge", "Gauge");
       Map<String, String[]> agentData3 = getChartData("TestimeBar", "Bar");
       Map<String, String[]> agentData4 = getChartData("TestimePie", "Pie");
       Map<String, String[]> agentData5 = getChartData("TestimeGauge", "Gauge");
       Map<String, String[]> agentData6 = getChartData("TestimeTable", "Table");   
       

       
       //Table text allignment /left/center
       
  
    

 
	   ArrayList data = new ArrayList();
	   
	   data.add(agentData);
	   data.add(agentData2);
	   data.add(agentData3);
	   data.add(agentData4);
	   data.add(agentData5);
	   data.add(agentData6);
	   HTMLeditor hm = new HTMLeditor();
	   String url = "http://www.merisoft.ee/help/jungent.png";
	   ArrayList type = new ArrayList();
	   String topLabel = "Jungenti müügiandmed";
	   
	   hm.genChart(url,data,topLabel);
	   
	  /*
	   try {
		 FileSender fs = new FileSender();
		fs.uploadDash();
	   } catch (Exception e1) {
		e1.printStackTrace();
	   }
	   */
	   
	   System.exit(0);
	 //  getSaveStockInfo2();
	   
	   
     TreeMap tm = new TreeMap();
 
     getPriceMatrixData();
     try {
       InputStream ins = new FileInputStream(getDir() + AGENT_FILE);
       InputStreamReader isr = new InputStreamReader(ins);
       BufferedReader reader = new BufferedReader(isr);
       
     String linex = null;
     while((linex = reader.readLine()) != null){
     	
     
     	if(linex.indexOf("user2") != -1){
     	
     		while((linex = reader.readLine()) != null){
     			linex = linex.replace(SEPARATOR + SEPARATOR, SEPARATOR + " " + SEPARATOR);
       StringTokenizer st = new StringTokenizer(linex, SEPARATOR);
       if (st.hasMoreTokens()){
    	   st.nextToken();st.nextToken();st.nextToken();
         String id = st.nextToken().trim();
         System.out.println(id);
         Agent agent = new Agent(id);
         if (st.hasMoreTokens()){
           String name = st.nextToken().trim() + " " + id;
           if (name.equals("")) name = "<" + id + ">";
           agent.setName(this.clear(name));
       
         }
         tm.put(agent.getName(), agent);
       }
     }	
    	}
     }
     reader.close();
     } catch (Exception e){
       e.printStackTrace();
     }
  
     return tm;
   }


   public TreeMap getTing(){
	     TreeMap tm = new TreeMap();

	     try {
	       InputStream ins = new FileInputStream(getDir() + "maksetin.TXT");
	       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
	       BufferedReader reader = new BufferedReader(isr);
      
	     String line = null;
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     
	     
	     while((line = reader.readLine()) != null){	     	
	     		
	            StringTokenizer st = new StringTokenizer(line, SEPARATOR);
	       if (st.hasMoreTokens()){
	         String id = st.nextToken().trim();
	     //    System.out.println("aaaa " + line);
	         if (st.hasMoreTokens()){
	        	 st.nextToken();
	        	 if (st.hasMoreTokens()){

		        	 tm.put(id, st.nextToken()); 
	        	 }
	         }
	        // 
	       }
	     
	     }
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	   //  System.out.println("aaaa " + tm.toString());
	     return tm;
	   }


       public String getDistribution() {
         return null;
       }

       
       
       

   public TreeMap getCustomers(Agent agent){
  
	   
	   TreeMap avegs = new TreeMap();
	   TreeMap thismonth = new TreeMap();
	   
	    try {
	        InputStream ins = new FileInputStream(getDir() + CUSTOMER_PRODUCT_FILE);
		       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		       BufferedReader reader = new BufferedReader(isr);
	      
	      String line = null;
	      while((line = reader.readLine()) != null){
	  		 line = line.replaceAll("\t","; ");
	    		//System.out.println("XX " +line); 
	    		 StringTokenizer st = new StringTokenizer(line, ";");
	        if (st.hasMoreTokens()){
	        	 
	        	st.nextToken();
	        	 if (st.hasMoreTokens()){
	        		 
	        	 
	        	String lastdate = st.nextToken().trim();
	        	long tehing = Calendar.getInstance().getTimeInMillis() -  getDate(lastdate).getTime();
	        	
	        	
	          String custID = st.nextToken().trim();
	      	st.nextToken();	st.nextToken();
	                if (st.hasMoreTokens()){
	                  String prodID = st.nextToken().trim();
	                  String am = st.nextToken().replaceAll(" ","").replaceAll(",",".");
	                  if(am.length()==0){
	                  	am = "0";
	                  }
	                  double lastamount = Double.parseDouble(am);
	                  
	                  if(lastamount > 0){	                  	
	                  CustProd cp = new CustProd(custID, prodID, lastamount, 0, 0,lastdate.substring(0,5),0,0,0," ");
	                    custProds.addElement(cp);
	                  }
	                  st.nextToken();
	                  st.nextToken();
	                  
	                  String sum = st.nextToken().replaceAll(" ","").replaceAll(",",".");
	                  //System.out.println("sum " + sum);
	                 try{
	                     double sales = Double.parseDouble(sum);
		                  
		                  if(tehing < Long.parseLong("7776000000")){
		                	   if(avegs.get(custID) == null){
		 	                	  avegs.put(custID, sales + "");
		 	                  }else{
		 	                	double sold = Double.parseDouble((String)avegs.get(custID));
		 	                	 avegs.put(custID, (sales + sold) + "");
		 	                  }  
		                  }
		                  
		                  if(this.isThisMonth(lastdate)){
		                	  if(thismonth.get(custID) == null){
		                		  thismonth.put(custID, sales + "");
		 	                  }else{
		 	                	double sold = Double.parseDouble((String)thismonth.get(custID));
		 	                	thismonth.put(custID, (sales + sold) + "");
		 	                  } 
		                  }
		                  
	                 }catch (Exception e ){
	                	 
	                 }
	          }
	        }}
	      }
	      reader.close();
	      } catch (IOException e){
	        e.printStackTrace();
	      }
	   
	   
	   

     TreeMap tm = new TreeMap();
     StringBuffer buf = new StringBuffer ();
     this.customers = new ArrayList();
   String confirm = getParent().getProps().getProperty("confirmemail");
     getDebt(null);
     try {
     // TreeMap ting = getTing();
     	
       InputStream ins = new FileInputStream(getDir() + CUSTOMER_FILE);
       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
       BufferedReader reader = new BufferedReader(isr);
  
     String line = null;
     while((line = reader.readLine()) != null){
     	
     	
     	if(line.indexOf("cu5") != -1){
     		//reader.readLine();
  while((line = reader.readLine()) != null){
  	line = line.replaceAll("\t","| ");
  	line = line.replaceAll(";", ":");
 	  StringTokenizer st = new StringTokenizer(line, "|");
 try{
	
 	  if (st.hasMoreTokens()){
    	   st.nextToken().trim();
    	   st.nextToken().trim();
    	   st.nextToken().trim();
    	   
    	   
         String id = st.nextToken().trim();
         Customer customer = new Customer(id); 

         customer.setAddress(" ");
         if (st.hasMoreTokens()){
           String name = st.nextToken().trim();
           if (name.equals("")) name = "<" + id + ">";      
            name = name + " " +  id ;
            try {
            	
           
          String cinfo =  st.nextToken() + " " + st.nextToken() ;
         
          if(cinfo.trim().length()>0){
        	  customer.setPersonalData(this.clear(Converter.enCodeEst(cinfo)));
          }
          // System.out.println(cinfo);
           if (st.hasMoreTokens()){
             String aadress = st.nextToken().trim();
             String lisa = st.nextToken().trim();
            
               if( aadress != null){
            	   if( lisa != null){
            		   aadress = lisa + " " + aadress;
                   }  
             	 aadress = clear(aadress);            
               customer.setAddress(Converter.enCodeEst(Converter.adressLink(aadress, "Estonia")));
             }
           }
           if (st.hasMoreTokens()){
          }

           
           name = clear(name);
             customer.setName(Converter.enCodeEst(name));  

             
             clear(st.nextToken().trim());
             
             if(confirm != null){
                 customer.setConfirmEmail(confirm); 
             }
             
             
           st.nextToken();
          
        String  aadress =  st.nextToken().trim();
           if( aadress != null && aadress.trim().length() > 0){
             customer.setInfo(aadress);
           }
      
      if (st.hasMoreTokens()){
       String lisa = st.nextToken().trim();
       if( lisa != null){
        }
     
     }
           if(avegs.get(customer.getID())!= null){
        	   double sss =  Double.parseDouble((String) avegs.get(customer.getID()));       	   
        	   customer.setAverage((int)sss/3); 
           }

           if(thismonth.get(customer.getID())!= null){
        	   double sss =  Double.parseDouble((String) thismonth.get(customer.getID()));       	   
        	   customer.setSold((int)sss); 
           }
           
      st.nextToken();
      //     st.nextToken();
   //10202	A-Selver AS			P�rnu mnt 238	Tallinn 11624	Kadaka Selver		Kadaka tee 56A	Tallinn 12915		6673400	6673414	SELVE		45	0	0		1			KADSELVER	1			0	GAJAN				1	Kadaka Selver				075		0	09.02.2007					0									0				0	0			0	0	0	19.07.2004	0											0	0	0				0	0																					0	0	1									102068		0	0				0		0			
        
           if (st.hasMoreTokens()){
           	String info = st.nextToken().trim();
           	if(info.length()>0 && info.trim().length()>0){

                customer.setPersonalData(customer.getPersonalData() + " " +clear(info));	
           	}
            }
           st.nextToken();
           st.nextToken();
           st.nextToken();
          
          
           if (st.hasMoreTokens()){
           	String paym = st.nextToken().trim();
			if(paym.length()>0 && paym.trim().length()>0){
				
			//	if(ting.get(paym.trim()) != null ){
				//	customer.setPaymTerm((String)ting.get(paym.trim()));	
			//	}else{
					customer.setPaymTerm(paym.trim());
			//	}
				
		           	
			}
             }

           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
        String hinnak =    st.nextToken();
        if(hinnak != null && hinnak.trim().length() > 0){
        	customer.setGroup(hinnak.trim());	
        }
 
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();

           if (st.hasMoreTokens()){
        	   String agg = st.nextToken().trim();
        	 //  System.out.println("agg" + agg);
        	   if(agg.length()>0){
                   customer.addAgent(new Agent(agg));
                   customer.setCity(agg);
                   customer.addAgent(new Agent("HOUSE"));
                   customer.setMainAgent(agg);
                   if(agg.equals("KRISTJAN")){
                	   customer.addAgent(new Agent("HARRY_O"));
                	   
                   }
                   if(agg.equals("INDREK_2")){
                	   Enumeration enume = getParent().getAgentsHash().elements();
                       while (enume.hasMoreElements()) {
                         Agent a = (Agent) enume.nextElement();
                         customer.addAgent(a);
                       }
                   }
                }
        	   
        	   
          }

           
           if (st.hasMoreTokens()){
           	String cred = st.nextToken().trim();
           	if(cred.length()>0 && cred.trim().length()>0){
           	//	customer.setCredit(cred.replaceAll(" ", "").replaceAll(",", "."));	
           	}          
           }
           
           /*

           if (st.hasMoreTokens()){
            customer.setVisit(st.nextToken().trim());
            System.out.println(customer.getVisit());
          }*/
           
           String tok = "";
           String tok2 = "";
           String pricelist = "";
           String delivery = "";
           for(int i = 0 ;i<86;i++){
           String	toke = st.nextToken();
          
           if(i==9){
             tok = toke.trim();	
           	}
           	if(i==7){
                tok2 = toke.trim();	
                	}
        	if(i==6){
                pricelist = toke.trim();	
                	}
        	if(i==41){
                delivery = toke.trim();	
                	}
           }
        
           if(delivery.trim().length()>0){
        	 //  customer.setAddress(delivery.trim());
           }
              
        	   
           if(pricelist.trim().length()>0){
              	customer.setGroup(pricelist);
              }
           
           if(tok2.trim().length()>0){
           	if(tok2.trim().length()>2){
           		tok2 = tok2.substring(0,2)+ "." + tok2.substring(2,tok2.length());
          	}
           }
        
           //	System.out.println("Ale " + customer.getName() + " " +customer.getDiscount() + " " + tok);
          
         
           if(overdue.containsKey(customer.getID())){
           	customer.setDebt(Double.parseDouble((String)overdue.get(customer.getID())));
          }
           if(creditused.containsKey(customer.getID())){
              	customer.setUsedCredit(Double.parseDouble((String)creditused.get(customer.getID())));
             }
           if(!tok.equals("1555")){
             tm.put(customer.getName(), customer);
             this.customers.add(customer.getID());
             buf.append(customer.getID() + ";" + customer.getName() + ";" +  customer.getAddress() + "; ; ; ; ; ; \n");
           }
            }catch(Exception e) {
            	//e.printStackTrace();
            }
         }

       }
 	 
 	 }catch(Exception r){
 		// r.printStackTrace();
 	 }
     	
    	}
       }
     }
     reader.close();
     

     } catch (IOException e){
       e.printStackTrace();
     } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     
 
     
 
     
     return tm;
   }

   
  
   
  TreeMap stocks = new TreeMap();
  
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     if(agent == null  || (agent != null && !agent.getID().equals("stock"))) {
    	 getSaveStockInfo2();
    	 System.out.println("sees Stocks suurus " + stocks.toString());
      }
     
     prods = new ArrayList<String>();
     try {
     	

	    InputStream ins = new FileInputStream(getDir() +PRODUCT_FILE);
	       InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
	       BufferedReader reader = new BufferedReader(isr);

     String line = null;
     while((line = reader.readLine()) != null){
    	 boolean goes = true;    	
     	if(line.indexOf("artikel5") != -1){
     		 while((line = reader.readLine()) != null && line.indexOf("ITVc2") == -1){
     			 line = line.replaceAll(";"," ");
     		 line = line.replaceAll("\t","; ");
     		if(line.length()>100){
     		//	System.out.println(line);
     	     		
       StringTokenizer st = new StringTokenizer(line, ";");
       if (st.hasMoreTokens()){
    	   st.nextToken().trim();
    	   st.nextToken().trim();
    	   st.nextToken().trim();
         String id = st.nextToken().trim();
         Product prod = new Product(id);
         
         if(id.equals("E1B13")) {
         System.out.println("id " + line);
         }
         String name = "";
         if (st.hasMoreTokens()){
        	 name = st.nextToken().trim();
     
           if (st.hasMoreTokens()){
           	String unit = st.nextToken().trim();
           	if(unit.length()>0){
           		prod.setUnit(unit);
           	}
        
          String kastis =  	st.nextToken();
          if(kastis != null && kastis.trim().length()>0){
        	//  prod.setInCases(Double.parseDouble(kastis.replaceAll(" ","").replace(',', '.')));
          }
           	st.nextToken();
           	st.nextToken();
            if (st.hasMoreTokens()){
            	String pr = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
            	if(pr.length() == 0){
            		pr = "0";
            	}
                double price = Double.parseDouble(pr);
                if (price > 0){
                  prod.setPrice(price);}
               //   System.out.println("price" + prod.getPrice());

               	st.nextToken();
               	String grupp = st.nextToken().trim();
               	if (grupp.length()>0){
               		prod.setGroup(grupp);
               	}
               	
               	
               	if(hinnad.get(prod.getID()) != null){
               	//	prod.setPrice(Double.parseDouble((String) hinnad.get(prod.getID())));
               	}
               	
           	String ii = "";

           	
           	
           	ii = st.nextToken();
           	ii = st.nextToken();      
           	ii = st.nextToken();
          // 	System.out.println("ii" + ii);
           	String cos = ii.trim().replaceAll(",",".").replaceAll(" ","");
           	if(cos.length() == 0){
        		cos = "0";
        	}
           	 double cost = Double.parseDouble(cos);
           	 
           	 double marg = (1-cost/prod.getPrice())*100 ;
           	 if(marg >0){
           	 	 prod.setMargin((int) marg + "");
                //	System.out.println("price" + prod.getPrice() + " " + (int)marg + " " + cos );
                	 
           	 }
          
           	String sortcode =  st.nextToken();
           	if(sortcode != null && sortcode.trim().length() > 0 ){
           		//prod.setSorting(sortcode.trim());
           	}
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken(); 
           	ii =	st.nextToken();
           	String ean2 =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
          ii =	st.nextToken();
           
           	ii = st.nextToken();
             	st.nextToken();
                if(id.equals("E1B13")) {
                    System.out.println("ean2 " + ean2);
                    }
             	
           	if(grupp.trim().length()>0){
           		prod.setGroup(grupp.trim());
           		
           		name =  id + " " +clear(name);
           		
           		
           		
           	 	goes = true;
                prod.setNameSorting(grupp +" "+  id +" "+ name);
           	    prod.setName(Converter.enCodeEst(name));
           	}
           	
           	String partii = st.nextToken().trim();
           
         	
           	if(ean2.trim().length()>0){
           		prod.setEAN(ean2.trim());
           	//	System.out.println("ssssr " + prod.getEAN());
           	}

         	st.nextToken();
    

       	if(partii.trim().length()>0){
       		prod.setEAN(partii.trim());
       	  if(partii.trim().length() > 4){
      		prod.setName(prod.getName() + " " + partii.trim().substring(partii.trim().length()-4)); 
      	  }
       		
       	}
       	
          
           
          int o = 0;
             while (st.hasMoreTokens()){
             	String ca = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
             	 if(o== 27) {
             		 try {
                      	double inCases = Double.parseDouble(ca);
                          if (inCases > 0){
                            prod.setInCases(inCases);
                             System.out.println("Case "+ id + " " + prod.getInCases());
                           }
                        }catch(Exception e) {
                     	   
                        } 
             	 }
             	 o++;
             	if(ca.trim().length()==0){
             		ca = "0";
             	}
              
              }
             

           //	System.out.println("CAse" + st.nextToken());
             }
           }
         }
         
         prod.setStock(0);
        
        	// System.out.println("lisan laoseisu0 " + prod.getID() + prod.getStock()); 
        	    if(stocks.get(prod.getID()) != null){
                 	prod.setStock( Double.parseDouble(((String)stocks.get(prod.getID())).replaceAll(" ","").replaceAll(",",".") ));
              
                 	if(prod.getID().startsWith("5101"))
                 	System.out.println("lisan laoseisu " + prod.getID() + " " + prod.getStock()); 
        	    }
                	 
     
         
        
         if(prod.getGroup().equals("456") || prod.getGroup().equals("457") || prod.getGroup().equals("998") || prod.getGroup().equals("999")){
        	 goes = false;
         }
         
         	if( goes && prod.getName() != null  && prod.getName() .length() > 10 ){
                 tm.put(prod.getGroup() +" "+  id +" "+ name, prod);   
                 prods.add(prod.getID());
         	}
            
       }
     		 }	
      		}
     	}
     }
     } catch (IOException e){
       e.printStackTrace();
     }

     
     return tm;
   }
   
   public Vector getCustomerProductsList(String agentCode){
     return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "HansaPomm";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }

   public double getSalary(String agentCode){
    
     try {
       FileReader fr = new FileReader(getDir() + AGENT_SALES_FILE);
       BufferedReader reader = new BufferedReader(fr);
       
       String line = null;
       while((line = reader.readLine()) != null){
         StringTokenizer st = new StringTokenizer(line, SEPARATOR);
         if (st.hasMoreTokens()){
           String agentID = st.nextToken().trim();
           if (agentID.equals(agentCode)){
             if (st.hasMoreTokens()){
               String sls = st.nextToken().trim();
               setSales(Double.parseDouble(sls));
             }
           }
         }
       }
       reader.close();
       fr.close();
     } catch (IOException e){
       e.printStackTrace();
     }

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }

  

   private String clear (String ss){
    //	System.out.println(ss);
    return ss;
    }
   
   public static final String SEPARATOR = "\t";
   public static final String PRODUCT_FILE = "tooted.TXT";
   public static final String PRODUCT_PRICES_FILE = "hinnad.TXT";
   public static final String AGENT_FILE = "agendid.TXT";
   public static final String CUSTOMER_FILE = "kliendid.TXT";
   public static final String CUSTOMER_AGENT_FILE = "klagent.txt";
   public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.TXT";
   public static final String CUSTOMER_DEBT_FILE = "volg.txt";
   public static final String AGENT_SALES_FILE = "5_sales_agents.txt";
   private TreeMap overdue = new TreeMap();
   private Vector custProds = new Vector();
   private TreeMap creditused = new TreeMap();
   private HashMap hinnad = new HashMap();
   private ArrayList customers = null;
   public static final String DRIVER_VERSION = "HansaSatcom";
   private String	       url = "jdbc:odbc:aaaaa";
   private Connection	       con = null;

   private String	       subjectFilesTo = null;
   
}