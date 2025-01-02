package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>MOFF Prekegetproducts
 * 
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviTobisDirectoDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker{

  public NaviTobisDirectoDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    parent.setAbiKood(true);
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
	
	 company = parent.getProps().getProperty("company");
	    comphelp = parent.getProps().getProperty("comphelp");
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.company");
	    logurl = "https://login.directo.ee/xmlcore/";
	  
	    if( parent.getProps().getProperty("stock.dir") != null){
	    	stockConn = parent.getProps().getProperty("stock.dir");
	    }
	    if("true".equals(getParent().getProps().getProperty("use.olddata"))){
			  useold = true;
			  }
  }

  private void makeData(){

	     
      try{
      	String httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=itemclass";
      	
      	Converter.doTrustToCertificates();
          URL myurl = new URL(httpsURL);
          HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
          InputStream ins = con.getInputStream();
          InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
          BufferedReader in = new BufferedReader(isr);

          String inputLine;
          StringBuffer inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
         }

          in.close();
          
          if(inputLineBuf.length() > 2){
       	   String str = inputLineBuf.toString();
              if(str.startsWith("True")) {
             	 str = str.substring(4);
              }
                try {
              	  File f = new File (getDir() + "tootegrupid.xml");
              	  if(f.exists())f.delete();
              	  
              	  
            	 Converter.saveFileInUTF8(getDir() + "tootegrupid.xml", str);
          	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }
          
          //lõpeb tootegruppidega tegelemine 
          System.out.println("Tootegrupid tehtud" );
          //algab Hinnakirja faili moodustamine
          
          
          String gS = getParent().getProps().getProperty("keyStocks");
  	    if(gS == null) {
  	    	System.out.println("keyStocks is missing!");
  	    	System.exit(0);
  	    }
  	   
          String urlParameters  = "get=1&what=stocklevel&key=" + gS;
          httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
          myurl = new URL(httpsURL);
          
          byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
          int postDataLength = postData.length;
          con = (HttpsURLConnection)myurl.openConnection();
          con.setRequestMethod("POST");
          con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
          con.setRequestProperty("charset", "utf-8");
          con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
          con.setUseCaches(false);
          con.setDoOutput(true);
          try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
             wr.write( postData );
          }
          
          ins = con.getInputStream();
          isr = new InputStreamReader(ins,"UTF-8");
          in = new BufferedReader(isr);

          inputLine = "";
         inputLineBuf = new StringBuffer();
         while ((inputLine = in.readLine()) != null){
         	inputLine = Converter.enCodeEst(inputLine);
         	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
         }

         in.close();
         
         if(inputLineBuf.length() > 2){
       	  String str = inputLineBuf.toString();
             if(str.startsWith("True")) {
            	 str = str.substring(4);
             }
               try {
             	  File f = new File (getDir() + "laoseis.xml");
             	  if(f.exists())f.delete();
             	 Converter.saveFileInUTF8(getDir() + "laoseis.xml", str);
         	
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
         
         System.out.println("Laoseisud tehtud" );
         
         
        
         try {
       	  
       	  String gP = getParent().getProps().getProperty("keyPrices");
        	    if(gP == null) {
        	    	System.out.println("keyPrices is missing!");
        	    	System.exit(0);
        	    }
        	   
            urlParameters  = "get=1&what=priceformula&key=" + gP;
           
       	 //?get=1&what=priceformula";
          myurl = new URL(httpsURL);
          con = (HttpsURLConnection)myurl.openConnection();
          postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
          postDataLength = postData.length;
         con = (HttpsURLConnection)myurl.openConnection();
         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
         con.setRequestProperty("charset", "utf-8");
         con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
         con.setUseCaches(false);
         con.setDoOutput(true);
         try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write( postData );
         }
      
          ins = con.getInputStream();
          isr = new InputStreamReader(ins,"UTF-8");
          in = new BufferedReader(isr);

          inputLine = "";
         inputLineBuf = new StringBuffer();
         while ((inputLine = in.readLine()) != null){
         	inputLine = Converter.enCodeEst(inputLine);
         	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
         }

         in.close();
         
         }catch (Exception e) {
       	  e.printStackTrace();
         }
         if(inputLineBuf.length() > 2){
        String str = inputLineBuf.toString();
        if(str.startsWith("True")) {
       	 str = str.substring(4);
        }
               try {
             	  File f = new File (getDir() + "hinnakiri.xml");
             	  if(f.exists())f.delete();
             	 Converter.saveFileInUTF8(getDir() + "hinnakiri.xml", str);
         	
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
         

         //lõpeb toodetega tegelemine 

         System.out.println("Hinnakirjad tehtud" );
          
        
          
         String gB = getParent().getProps().getProperty("keyBrands");
  	    if(gB == null) {
  	    	System.out.println("keyBrands is missing!");
  	    	System.exit(0);
  	    }
  	   
      urlParameters  = "get=1&what=brands&key=" + gB;
     
 	 //?get=1&what=priceformula";
    myurl = new URL(httpsURL);
    con = (HttpsURLConnection)myurl.openConnection();
    postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
    postDataLength = postData.length;
   con = (HttpsURLConnection)myurl.openConnection();
   con.setRequestMethod("POST");
   con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
   con.setRequestProperty("charset", "utf-8");
   con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
   con.setUseCaches(false);
   con.setDoOutput(true);
   try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
      wr.write( postData );
   }
         
      
         ins = con.getInputStream();
         isr = new InputStreamReader(ins,"UTF-8");
         in = new BufferedReader(isr);

         inputLine = "";
        inputLineBuf = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
        	inputLine = Converter.enCodeEst(inputLine);
        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
        }

        in.close();
        
        if(inputLineBuf.length() > 2){
       	  String str = inputLineBuf.toString();
             if(str.startsWith("True")) {
            	 str = str.substring(4);
             }
              try {
            	  File f = new File (getDir() + "brands.xml");
            	  if(f.exists())f.delete();
            	 Converter.saveFileInUTF8(getDir() + "brands.xml", str);
        	
        	  }catch (Exception ex) {
        		ex.printStackTrace();
        	  }
         }
        

        //lõpeb toodetega tegelemine 

        System.out.println("Brandid tehtud" );
         
     
         
         
          //algab tootede faili moodustamine
         String gK = getParent().getProps().getProperty("keyGen");
 	    if(gK == null) {
 	    	System.out.println("keyGen is missing!");
 	    	System.exit(0);
 	    }
 	   
 	   String genKey = "&key=" + gK;
 	    
           httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=BI_ITEM" + genKey;
           if(getParent().getProps().getProperty("stockcode") != null){
           	httpsURL = httpsURL + "&stock=" +  getParent().getProps().getProperty("stockcode");
 		     }
 	 //   System.out.println(httpsURL);
           myurl = new URL(httpsURL);
           con = (HttpsURLConnection)myurl.openConnection();
           ins = con.getInputStream();
           isr = new InputStreamReader(ins,"UTF-8");
           in = new BufferedReader(isr);

           inputLine = "";
          inputLineBuf = new StringBuffer();
          while ((inputLine = in.readLine()) != null){
          	inputLine = Converter.enCodeEst(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
          }

          in.close();
          
          if(inputLineBuf.length() > 2){
       	   String str = inputLineBuf.toString();
              if(str.startsWith("True")) {
             	 str = str.substring(4);
              }
                try {
              	  File f = new File (getDir() + "tooted.xml");
              	  if(f.exists())f.delete();
              	Converter.saveFileInUTF8(getDir() + "tooted.xml", str);
             	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }

          
         
//kui on olemas ka teine ladu siis teeme selle seisu ka.
          if(getParent().getProps().getProperty("stockcode122") != null){
       	   httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=item&stock=" +  getParent().getProps().getProperty("stockcode1");
       	   
       	 //  System.out.println(httpsURL);
              myurl = new URL(httpsURL);
              con = (HttpsURLConnection)myurl.openConnection();
              ins = con.getInputStream();
              isr = new InputStreamReader(ins,"UTF-8");
              in = new BufferedReader(isr);

              inputLine = "";
             inputLineBuf = new StringBuffer();
             while ((inputLine = in.readLine()) != null){
             	inputLine = Converter.enCodeEst(inputLine);
             	inputLineBuf.append(inputLine.replaceAll("<item","\n<item" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
             }

             in.close();
             
             if(inputLineBuf.length() > 2){
           	  String str = inputLineBuf.toString();
                 if(str.startsWith("True")) {
                	 str = str.substring(4);
                 }
                   try {
                 	  File f = new File (getDir() + "tooted" + getParent().getProps().getProperty("stockcode1") +".xml");
                 	  if(f.exists())f.delete();
                 	Converter.saveFileInUTF8(getDir() + "tooted" + getParent().getProps().getProperty("stockcode1") + ".xml", str);
                	
             	  }catch (Exception ex) {
             		ex.printStackTrace();
             	  }
              }
       	   
		  }

          //lõpeb toodetega tegelemine 

          System.out.println("Tooted tehtud" );
          //algab kliendi faili moodustamine
       
          httpsURL = logurl  + company + "/xmlcore.asp?get=1&what=BI_CUSTOMER"+ genKey;
          myurl = new URL(httpsURL);
          con = (HttpsURLConnection)myurl.openConnection();
          ins = con.getInputStream();
          isr = new InputStreamReader(ins,"UTF-8");
          in = new BufferedReader(isr);

          inputLine = "";
         inputLineBuf = new StringBuffer();
         while ((inputLine = in.readLine()) != null){
         	inputLine = Converter.enCodeEst(inputLine);
          	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));	
         	
      //       System.out.println(inputLine);	
         }

         in.close();
         
         if(inputLineBuf.length() > 2){
            
                try {
                   String res = "";
                   String str = inputLineBuf.toString();
                   if(str.startsWith("True")) {
                  	 str = str.substring(4);
                   }
              	  StringTokenizer st = new StringTokenizer(str,"\n");
              	  while(st.hasMoreTokens()){
              		String line = st.nextToken();
              		//if(line.indexOf("\"10173\"") == -1 && line.indexOf("\"13543\"") == -1 && line.indexOf("<contact code=\"2575\"") == -1 ){
              			res = res + line + "\n";
              		//}
              	  }
              	  File f = new File (getDir() + "kliendid.xml");
              	  if(f.exists())f.delete();
              	 
              	Converter.saveFileInUTF8(getDir() + "kliendid.xml", str);
             	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
           }
         
      }catch(Exception e){
      	e.printStackTrace();
      }
      
      

  	

      System.out.println("kliendid tehtud" );
    
      
      
	   
	   
  }

       	  private Node getNode(Node rt, String s){
       	    return GenericDOM.getNode(rt, s);
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
          private String getDir(){
        	     String dir = getParent().getProps().getProperty("readin");
        	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        	     return dir;
        	   }
	  

  
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
		  System.exit(0);
   	return null;
   }

   private String stockConn = null;
   String user =  null;
   String pass = null;
   String rcompany = null;
   String logurl = null;
   private  boolean useold = false;
   private String company = null;
   private String comphelp = null;
   

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
            String[] ff3 = res2.list();
        	 for(int i = 0;ff3.length>i;i++){
        		 
            	//(new File ( pictspda + ff2[i])).delete(); 
            }
//        	 Lõppes kustutamine 
        //	custProds.add("suva");
 		  this.getProducts(null);
 		// System.out.println("olemas " + prodHash.get("ALC-300630"));

        StringBuffer buf = new StringBuffer();
        Iterator enume = prodHash.keySet().iterator();
   
        while (enume.hasNext()) {
   	    	String asi = (String)enume.next();
   	    	
   	    	String toode = asi; 
   	    	
   	    	  toode = toode.replace('/', '_');
   	          toode = toode.replace('/', '_');
   	          
              if(toode.length() > 0){
               	
   	   	  res2 = new File (pictssou);
   	   String[]  ff2 = res2.list();
            
        	 for(int i = 0;ff2.length>i;i++){
            	if( ff2[i].startsWith(toode+".")){
              // System.out.println("pilt olemas " + ff2[i]);
           	File in = new File ( pictssou + ff2[i]); 
             	if(in.isFile()){
             		if(in.getName().toLowerCase().endsWith("png")) {
             		 	File inpda = new File ( pictspda + toode+ ".png");
                     	if(!inpda.exists() || (inpda.exists() && inpda.lastModified() < in.lastModified())){ // kui on 72h ja uuem
                        	if("true".equals(parent.getProps().getProperty("pict.noshrink"))) {
                     			Converter.copyFile(in,inpda);
                     		}else {
                     			Converter.shrinkAndCopyFile(in,inpda,600);
                     		}
                       	 }
                     	buf.append(toode +".png;"  + inpda.length() + "\n");
                   	
                     	 			
             		}else {
             	File inpda = new File ( pictspda + toode+ ".jpg");
             	if(!inpda.exists() || (inpda.exists() && inpda.lastModified() < in.lastModified())){ // kui on 72h ja uuem
                	if("true".equals(parent.getProps().getProperty("pict.noshrink"))) {
             			Converter.copyFile(in,inpda);
             		}else {
             			Converter.shrinkAndCopyFile(in,inpda,600);
             		}
               	 }
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

 
   private String toUpper(String asi ){
	   
	   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
	   System.out.println(asi);
	   return asi ;
   }
   
 public void copyFile(File in, File out) throws Exception {
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


   public Vector getPriceMatrixData(){

	
	  	 String    sql = "  SELECT [Sales Code] AS custid,'' AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from" +
            "   [2015_Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or" +
             "           year ([Ending Date]) = 1753)  and [Sales Type] = 0    " +
                          "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
             " [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from " +
             " [2015_Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or " +
                      "  year ([Ending Date]) = 1753) and [Sales Type] = 1 " +   
                          "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
            "   0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                       "  from [2015_Tobis$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or " +
                      "  year ([Ending Date]) = 1753)  " +     
                   "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
              " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                       "  from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or  " +
                      "   year ([Ending Date]) = 1753) " +
                      " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                     " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                     "    from [2015_Tobis$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or " +  
                     "  year ([Ending Date]) = 1753)       " +
                    " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
                   " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                    "     from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or " + 
                     "    year ([Ending Date]) = 1753) " ; 
			
  	 

       Vector result = new Vector();
         try {
          
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {

             String custID = Converter.enCodeEst(rs.getString("custid").trim());
             String clGroup = rs.getString("clgroup");
             String prodID = rs.getString("prodid").trim();
             String prodGroup = rs.getString("prodgroup");
            
             double price = rs.getDouble("price");
             double disc = rs.getDouble("disc");
             String date = rs.getString("paev");
             String sdate = rs.getString("startday");
             String curr = rs.getString("val");
             MatrixLine mline = new MatrixLine();

             if (custID != null && custID.length() > 0) {
               mline.setCustID(custID);
             }
             
             if (curr != null && curr.length() > 0) {
                 mline.setCurrency(curr.trim());
               }
             
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
             }

             if(sdate != null  && !sdate.startsWith("1753")){
            	 mline.setStartDate(sdate.substring(0,10));
             }
             if(date != null && !date.startsWith("1753")){
            	 mline.setEndDate(date.substring(0,10));
             }
             if (prodID != null && prodID.length() > 0) {
               mline.setProdID(prodID);
             }
             if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
               mline.setProdGroup(Converter.cleanLitLatEst(prodGroup.trim()));
               mline.setDiscGroup(Converter.cleanLitLatEst(prodGroup.trim()));
             }

             if (price != 0) {
               mline.setPrice(price);

             }
             if (disc != 0) {
                 mline.setDiscount(disc);

               }
             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
                 result.add(mline);    
             }
             
           }
           rs.close();
           stmt.close();
           con.close();
         }catch(Exception e){
        	 e.printStackTrace();
         }
	
         getPriceMatrixDataDirecto( result ) ;
         
    return result;
      }

   private java.util.Date getCalXML(String ss){
		  Calendar cc = Calendar.getInstance();
		  	
		  	cc.set(Calendar.YEAR,Integer.parseInt(ss.substring(0,4)));
		  	cc.set(Calendar.MONTH,Integer.parseInt(ss.substring(5,7))-1);
		  	cc.set(Calendar.DATE,Integer.parseInt(ss.substring(8,10)));
		     
		  	return cc.getTime() ;
		  		
	  }
	
	  
   
	 
	private  HashMap fixCustomers = null;
	private  HashMap priceformulas = null;
   
	public  void getPriceMatrixDataDirecto( Vector lines ) {
		   String sdate = Converter.getDDpMMpYYYYpString(Calendar.getInstance());
		   if(priceformulas == null) {
			   this.getCustomersDirecto();
		   }
		   System.out.println("siin matrix enne " + lines.size());
		    //siin oleks
		    try {
				  File f = new File (getDir() + "hinnakiri.xml");
				  System.out.println("siin matix");
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
		        			
		        			// String custID = " ";
		                  String clGroup = " ";
		                  String prodID = " ";
		                  String prodGroup = " ";
		                  String start = " ";
		                  String end = " ";
		                  String currency = "";
		                  MatrixLine mline = new MatrixLine();
		                 try{
		                 	 try{
		                 	     clGroup = (rida.getAttributes().getNamedItem("code").getNodeValue());
		                        }catch(Exception aa){}
		                 	 try{
			                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
		                 	   }catch(Exception aa){ }
		                 	 try{
		                          prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
		                 	  }catch(Exception aa){ }
		                 	  try{
			                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
			                    	  }catch(Exception aa){ }
			                    	  try{
				                             end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
				                    	  }catch(Exception aa){ }
			                    	  try{
				                             currency =  rida.getAttributes().getNamedItem("currency").getNodeValue();
				                    	  }catch(Exception aa){ }
			                    	  String mode = null;  
			                    	  try{  	  
		                       mode = rida.getAttributes().getNamedItem("what").getNodeValue();
			                    	  }catch(Exception aa){ }
			                    	  
			                   mline.setProdLevel(mode);
			                    	  
		                      try{
		                     double value = Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
		                     if(mode != null && mode.equals("8")){
		                    	 mline.setPrice(value);
		                    	// mline.setProdLevel("pr");
		                     }else{
		                         mline.setDiscount(value);
		                     }
		                      }catch(Exception aa){ }
		                  
		                      
		                   

		                      
		                    //  if (custID != null && custID.trim().length() > 0) {
		                      //  mline.setCustID(custID.trim());
		                     // }
		                      if (currency != null && currency.trim().length() > 0) {
		                          mline.setCurrency(currency.trim());
		                        }
		                      
		                      if (clGroup != null
		                          && clGroup.trim().length() > 0) {
		                        mline.setCliGroup(clGroup.trim());
		                      }

		                      if (prodID != null && prodID.trim().length() > 0) {
		                        mline.setProdID(prodID.trim());
		                      }
		                      
		                      if (mline.getProdID() == null && prodGroup != null
		                          && prodGroup.trim().length() > 0) {
		                        mline.setProdGroup(prodGroup);
		                      }   
		                      
		                      if("true".equals(getParent().getProps().getProperty("add.startdate"))){
		                    	  if(clGroup != null  && clGroup.trim().length() > 0) {
		                    		  if (prodID != null && prodID.trim().length() > 0) {
		                    			  if(mline.getDiscount() > 0) {
		                    				  mline.setStartDate(sdate);
		                    			  }
		                    		  }
		                    	  }
		                      }
		                      
		                      if(start != " " && end != " "){
		                     	 mline.setStartDate(start);
		                     	 mline.setEndDate(end);
		                          Date alg = getCalXML(start); 
		                          Date lopp = getCalXML(end); 
		                          
		                          long diff = lopp.getTime() - alg.getTime();
		                          float days = (diff / (1000*60*60*24));
		                          if(days > 100) {
		                        	  mline.setStartDate(" ");
		                        	  mline.setEndDate(" ");
		                        	  
		                        	  if("true".equals(getParent().getProps().getProperty("add.startdate"))){
		                            	  if(clGroup != null  && clGroup.trim().length() > 0) {
		                            		  if (prodID != null && prodID.trim().length() > 0) {
		                            			  if(mline.getDiscount() > 0) {
		                            				  mline.setStartDate(sdate);
		                            			  }
		                            		  }
		                            	  }
		                              }
		                          }
		                          
		                          if(alg.before(new Date()) && lopp.after(new Date())){
		                        	  if(mline.discount > 0 || mline.price > 0) {
		                        		//  if(priceformulas.containsKey(clGroup))
		                         	     lines.add(mline);	

					                      if(clGroup != null && clGroup.equals("BIKUVA")) {
					                    	//  System.out.println("Lisan 1"  + mline.getString());
					                      }
		                        	  }
		                          }
		                        
		                          
		                      } else {
		                    	  if(mline.discount > 0 || mline.price > 0) {
		                    		//  if(priceformulas.containsKey(clGroup))
		                     	      lines.add(mline);	

				                      if(clGroup != null && clGroup.equals("BIKUVA")) {
				                    	 // System.out.println("Lisan ="  + mline.getString());
				                      }
		                    	  }
		  	                 }
		                      
		                      if(clGroup != null && clGroup.equals("BIKUVA")) {
		                    	 // System.out.println(mline.getString());
		                      }
		                 
		                 }catch(Exception e){
		                 	e.printStackTrace();
		                 }
		                 
		                }
		             	
		        	  }catch(Exception e){
		        		  e.printStackTrace();
		        	  }
		             
		        }
		        
			}catch(Exception e){
				e.printStackTrace();
			}
		    
		 System.out.println("siin matrix peale " + lines.size());
		 


		   }
   
   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }


	public Vector getLastThreeData() {
		// TODO Auto-generated method stubfg
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into dbo.supervisor_lit values ("
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
	        Connection con = getNewConnection();
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, wr.getInfo().getAgentName());
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
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
	             ps.setInt(35, 111); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
	     }


	 public String getProductSortingData(){
	   	return null;
	   }

   private String ensureLen(String str, String replace){
    if (str == null) str = "";
    else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
    if (str.length() == 0){
      return replace;
    } else {
      return str;
    }
  }

   public ManagerData getManagerData() throws SQLException{

    String tableName = "dayreport_lit";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }

    String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu," +
    		"text AS tekst,'' AS dealer,clgroup AS agentname" +
        " FROM  " + tableName +
        " WHERE sentdate>(GETDATE()-30) ORDER BY clid ";

   if (isDebug()) System.out.println(sql);
   this.con = getNewConnection();
   Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    StringBuffer sb = new StringBuffer();
    String sep = ";";

    while(rs.next()){
      String cliID = rs.getString("klient").trim();
      sb.append(cliID + sep);
      sb.append(rs.getString("kuupaev").trim() + sep);
      sb.append(getNF().format(rs.getDouble("summa")) + sep);
      sb.append(rs.getInt("ridu") + sep);
      String tekst = ensureLen(rs.getString("tekst")  ," ");
      tekst = tekst.replace('\'', ' ').replace('\"', ' ');
      tekst = tekst.replace('\n', ' ');
      String agendinimi = rs.getString("agentname");
      sb.append(tekst + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(ensureLen((agendinimi), " ") + sep);
        sb.append(ensureLen((" "), " ") + sep);
        sb.append(ensureLen((""), " ") + sep);
        sb.append((" ") + sep);
        sb.append((" ") + sep);
      sb.append("\n");
    }

    ManagerData md = new ManagerData();
    md.setClientVisitInfo(sb.toString());

 StringBuffer bf2 = new StringBuffer();
 Enumeration enums = klient.elements();
 while(enums.hasMoreElements()){
 	Customer cust = (Customer)enums.nextElement();
 	Iterator agen = cust.getAgents().keySet().iterator();
 	while(agen.hasNext()){
    Agent ag = 	(Agent)agents.get((String)agen.next());
   bf2.append(cust.getID() + sep);
   bf2.append(ag.getID() + sep);
   bf2.append(ag.getName() + "\n");
 }
	}

    md.setClientAgentInfo(bf2.toString());
    return md;

  }


   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		   if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
			   System.out.println("siin Fixed " + fixedLines.size());
			   return fixedLines ;
		   }
		   return result;

   }


   public DealerProduct DealerProductsList() {
     return null;
   }

   
   private void  saveDebtFile(String debtstring) {
	   
       if(debtstring != null){
         String fpath = getParent().getProps().getProperty("debtrefdir");
         String fileName = "debtref.txt";
         if (fpath != null) {
           fileName = fpath + fileName;
         }
         File debtFile = new File(fileName);
         java.io.Writer wr;
		try {
			wr = new FileWriter(debtFile);
			   wr.write(debtstring);
		         wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       }
     }

   public void makeDebtFile(){
			  
	   String cmd = "select c.No_ as kood ,sum(case when [Due Date] < getdate() then a.amount else 0 end) as yle,max( c.Blocked) as blokk  from " +
       " [_Merisoft$Tobis$Customer_Open_Docs] a inner join [2015_Tobis$Customer] c on a.[Sell-to Customer No_]= c.No_  " +
       " Group by c.No_ having sum(case when [Due Date] < getdate() then a.amount else 0 end) != 0";

	          if (isDebug()) System.out.println(cmd);
		          StringBuffer products = new StringBuffer();
		          try {

		            	 Connection con = getNewConnection2();
		        	        Statement stmt = con.createStatement();
		        	        ResultSet rs = stmt.executeQuery(cmd);

		            while (rs.next()) {

		              String kood = rs.getString("kood").trim();
		              double yle = rs.getDouble("yle");
		              int bl = rs.getInt("blokk");
		              if(yle > 0){
		                  products.append(kood + ";" + getParent().getNF().format(yle) + ";" + bl + "\n");
		              }
		              }
		              rs.close();
		              stmt.close();
		              con.close();
		          } catch (Exception e) {
		              getParent().showError(e);
		          }
		          
		          
		          saveDebtFile(products.toString());
		          
		      }


       public String getSaveStockInfo(){
    			  
    	   String cmd = " SELECT [Item No_] as kood ,[Quantity] as kogus  ,[Location code] " +
"  FROM [_MERISOFT$Tobis_Stock]";
   
 		  
    	   String cmd2 = "  SELECT [No_] as toode from [2015_Tobis$Item] where  [Show in mob_ device] = 1 ";
    	  
    		   	  HashSet koodid = new HashSet();

    		          if (isDebug()) System.out.println(cmd);
    		          StringBuffer products = new StringBuffer();
    		          try {
    		        		 Connection con = getNewConnection2();
 		        	        Statement stmt = con.createStatement();
 		        	        ResultSet rs = stmt.executeQuery(cmd2);

 		            while (rs.next()) {
 		             	  koodid.add(rs.getString("toode").trim());
 		              }

    		         rs = stmt.executeQuery(cmd);

    		            while (rs.next()) {

    		              String kood = rs.getString("kood").trim();
    		              double seis = rs.getDouble("kogus");
    		              if(seis > 0){
    		            	  koodid.add(kood);
    		                  products.append(kood +  ";");
    		                  products.append(" ;");
    		                  products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
    		              }
    		              }
    		              rs.close();
    		              stmt.close();
    		              con.close();
    		          } catch (Exception e) {
    		              getParent().showError(e);
    		          }
    		          
    		    products.append(  this.getSaveStockInfoDirecto(koodid));
    		          
    		          makeDebtFile();
    		          
    		          return products.toString();
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
           
           
      	 private String ensureLen(String in ){

    		 if(in.length() == 7){
    			 in = "0" + in ;
    		 }
    		 if(in.length() == 6){
    			 in = "00" + in ;
    		 }
    		 if(in.length() == 5){
    			 in = "000" + in ;
    		 }
    		 if(in.length() == 4){
    			 in = "0000" + in ;
    		 }
    		 if(in.length() == 3){
    			 in = "00000" + in ;
    		 }
    		 if(in.length() == 2){
    			 in = "000000" + in ;
    		 }

    		 if(in.length() == 1){
    			 in = "0000000" + in ;
    		 }
    		 return in;
    	 }
           private TreeMap<String, String> cateFullGroup = null;
           TreeMap   group2List = new TreeMap();
           
           
           
   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
     
     setTreeGroup(true);
     
     cateFullGroup = new TreeMap<String, String>();
     
     
     String cmd = "SELECT [code] as kood , [Short Name ] as nimi,[Group 2 Code] as vanem" + 
     		"  FROM [NAV40].[dbo].[2015_Tobis$Dimension Value] where [Dimension Code]='projektas'";
     System.out.println("Asike 1 " +cmd);
     try {
     	 Connection con = getNewConnection2();
     	    docCodes = new HashSet();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	        	
 	          String groupID = rs.getString("kood").trim();
 	          String name = rs.getString("nimi").trim() ;
 	          String vanem = rs.getString("vanem");
 	          
 	          if(groupID.startsWith("/")) {
                   tm.put(Converter.cleanLitLatEst(cleardots(ensureLen(groupID.substring(1)))), name);
 	          }
 	          
 	          if (vanem != null && vanem.trim().length() > 0 ) {
 	        	  String g = Converter.cleanLitLatEst(cleardots(ensureLen(vanem.substring(1))));
 	        	 cateFullGroup.put(groupID,g);
 	        	group2List.put(g + groupID, name);
 	          }
 	        }
 	  
 	        rs.close();
 	        stmt.close();
 	        con.close();
      } catch (Exception e){
        e.printStackTrace();
      }  
     
     tm.put("fake", "FAKE");
      return tm;
   }

   public void getProdGroupsDirecto(TreeMap tm) {
	    HashSet names = new HashSet();
		   String lang = null;
		   
		     if(getParent().getProps().getProperty("productgroup.lang") != null){
		      	 lang = getParent().getProps().getProperty("productgroup.lang");
		      }
		   
	  	try {
	  		  File f = new File (getDir() + "tooted.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "transport");      
	       //   System.out.println(root);
	      	Node read = getNode(root, "items");
	      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	          for (int i = 0; i < read.getChildNodes().getLength(); i++){
	          	  Node rida = read.getChildNodes().item(i);
	          	  try{
	          		  if(rida.getNodeName().equals("bi_item")){
	               		 String group  = rida.getAttributes().getNamedItem("class").getNodeValue();
	               		 try{
	               			 String name  = rida.getAttributes().getNamedItem("classname").getNodeValue(); 
	                   		 group = Converter.cleanLitLatEst(group);
	                   		  name  = "Z" + Converter.enCodeEst(name);
	                   		  name = name.replaceAll("Turtle Wax", "TW");
	                   		  
	                   		  if(lang != null) {
	                   			  try {
	                   			  Node lan=	  rida.getChildNodes().item(0);
	                   		     name = Converter.enCodeEst( lan.getAttributes().getNamedItem("content").getNodeValue());
	                   		
	                   			  }catch(Exception e) {
	                   				  
	                   			  }
	                   			
	                   				
	                   			
	                   		  }
	                   		 if(names.contains(name + " 1")) {
	                  			  name = name + " 2";
	                  		  }
	                   		  
	                   		  if(names.contains(name)) {
	                   			  name = name + " 1";
	                   		  }
	                   		  tm.put(group, name); 
	                   		// System.out.println("gr name " + name + " " + group);
	                   		names.add(name);
	               		 }catch(Exception r){
	               			 
	               		 }
	               	 
	          		  } 
	                  
	          	  }catch(Exception e){
	          		 // e.printStackTrace();
	          		 // System.exit(0);
	          	  }
	               
	          }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	  
	  
	   }



   public TreeMap getProdGroupsSecond() throws SQLException {
        return group2List;
  }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}

	   return idx;
   }
   
   private HashMap dirAgents = null;

   public TreeMap getAgents(){

	  // getPriceMatrixDataDirecto(new Vector());

       String cmd = "  select [2015_Tobis$Salesperson_Purchaser].code as kood , [2015_Tobis$Salesperson_Purchaser].Name as nimi ," +
       " '' AS objekt  ,[Code in DIRECTO] as directo FROM [2015_Tobis$Salesperson_Purchaser] " +
       " WHERE [2015_Tobis$Salesperson_Purchaser].Merisoft=1 ";
    
       dirAgents = new HashMap();
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          String directo = rs.getString("directo");
	          
	          if(directo != null) {
	        	  dirAgents.put(directo, agent.getID()); 
	          }
	          
	          agent.setName(name);

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
	          
	          agents.put(name+agent.getID(), agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
            rs.close();
            stmt.close();
	        con.close();
	        
	         
	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }

	    
	    
		try {
	 		  File f = new File (getDir() + "kliendid.xml");
	 	     
	 	       // System.out.println(f.getAbsolutePath());
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "transport"); 
	     	Node read = getNode(root, "customers");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  try{	
	         		 
	                 		 Node price_list = rida.getAttributes().getNamedItem("price_list");         	
			         	
			         	      if(price_list != null &&  !pricelists.contains(price_list.getNodeValue())){
		 	         		     pricelists.add((price_list.getNodeValue().trim())); 
		 	         		   }
			         	      
			         	     price_list = rida.getAttributes().getNamedItem("priceformula");         	
			         	    if(price_list != null &&  !pricelists.contains(price_list.getNodeValue())){
		 	         		     pricelists.add((price_list.getNodeValue().trim())); 
		 	         		   }
			         	    
			         	      
			         	      price_list =  rida.getAttributes().getNamedItem("price_list_special") ;  
			         	     if(price_list != null) {
					          StringTokenizer st  = new StringTokenizer(price_list.getNodeValue(),",");
			         	      if(st.hasMoreTokens()){
	                            String aa = (st.nextToken());
	                            
				         	      if( !pricelists.contains(aa)){
			 	         		     pricelists.add(aa); 
			 	         		   }
				         	      
			         	      }}
		         	  
	         	  }catch(Exception e){
	         		 // e.printStackTrace();
	         	  }
	         }
	         
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}

	    

     return agents;
   }
   

   private Vector pricelists = new Vector();

       public String getDistribution() {
         return null;
       }
private String conv (String ss){
	return Converter.enCodeEst(ss);
}



private boolean isDueDate(String mmddyy){
	   if("true".equals(getParent().getProps().getProperty("debtlist.all"))) {
		   return true;
	   }
	  	
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.DAY_OF_YEAR, -1);
		
		if(cal.before(ca)){
			return true;
		}else{
			return false;
		}
	  }



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



private HashSet getSales5M() throws SQLException {
	HashSet res = new HashSet();

	 String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,d.[Posting Date] AS lastdate " +	
"  from [2015_Tobis$Sales Shipment Header] d  WHERE   d.[Posting Date]> GETDATE()-150 " ;
	   
      con = getNewConnection2();
      Statement stmt1 = con.createStatement();
      ResultSet rs1 = stmt1.executeQuery(cmd);
      
      while(rs1.next()){
   	   
   	   String custID = rs1.getString("klient").trim();
	           res.add(custID);
	           
      }
      
      rs1.close();
      stmt1.close();
	
       
   	   File cpdatadir = new File("cpdata");
   	   if(!cpdatadir.exists()) {
   		   cpdatadir.mkdir();
   	   }
   	
   	
   	   Calendar cal = Calendar.getInstance();
   	   String d = cal.get(Calendar.DAY_OF_MONTH)+ "";
   	   if(d.length() == 1){
   		   d = "0" + d;
   	   }
   	   String m = (cal.get(Calendar.MONTH) +1)+ "";
   	   if(m.length() == 1){
   		   m = "0" + m;
   	   }
   	   
   	   int days = 150 ;
   	   
   	   String end = d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
   	   cal.add(Calendar.DATE, - days);
   	   
   	    d = cal.get(Calendar.DAY_OF_MONTH)+ "";
   	   if(d.length() == 1){
   		   d = "0" + d;
   	   }
   	    m = (cal.get(Calendar.MONTH) +1)+ "";
   	   if(m.length() == 1){
   		   m = "0" + m;
   	   }
   		   String start =  d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
   		 //  System.out.println(dirCustCodes.get("9535") + " " + start + " uhuu " + end);
   		
   		   Iterator enums = this.dirCustCodes.keySet().iterator();
   	   while(enums.hasNext()){
   		String custid = (String)enums.next();
   		   try{
   			   Converter.doTrustToCertificates();
   			   custid = Converter.deCode(custid);
   			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
   				String httpsURL = logurl  + company + "/xmlcore.asp"; 
   		  //     System.out.println(httpsURL);
   				URL myurl = new URL(httpsURL);
   		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
   		        
   		        String gS = getParent().getProps().getProperty("keyInvoice");
   		   	    if(gS == null) {
   		   	    	System.out.println("keyInvoice is missing!");
   		   	    	System.exit(0);
   		   	    }
   		        String urlParameters  = 
   						"what=invoice&get=1&orderer=" + cuurl + "&invoicedate1=" + start + "&invoicedate2=" 
   		            + end + "&key=" + gS;
   		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
   		           myurl = new URL(httpsURL);
   		           
   		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
   		           int postDataLength = postData.length;
   		           con = (HttpsURLConnection)myurl.openConnection();
   		           con.setRequestMethod("POST");
   		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
   		           con.setRequestProperty("charset", "utf-8");
   		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
   		           con.setUseCaches(false);
   		           con.setDoOutput(true);
   		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
   		              wr.write( postData );
   		           }
   		           
   		     
   		        
   		        
   		        InputStream ins = con.getInputStream();
   		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
   		        BufferedReader in = new BufferedReader(isr);
     
   		        String inputLine;
   		        StringBuffer inputLineBuf = new StringBuffer();
   		        while ((inputLine = in.readLine()) != null){
   		        	inputLine = Converter.enCodeEst(inputLine);
   		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
   		        	   
   				        
   		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
   		        	while (st.hasMoreTokens()){
   		        		
   		        		String line = st.nextToken();
   		        		
   		        	//	
   		        		int ind =  line.indexOf("invoice number=") ;
   		        		String number = "";
   			        	if(ind != -1){
   			        		 number = line.substring(ind + 16);
   			        		number = number.substring(0,number.indexOf("\""));
   			        		// System.out.println(number);
   			        	}
   			        	 ind =  line.indexOf("date=") ;
   			        	if(ind != -1){
   			        		String date = line.substring(ind + 6);
   			        		date = date.substring(0,date.indexOf("\""));
   			        		// 		        		
   			        		 if(isOkDate(date)){
   			        			
   			     					Vector cps = this.getCPCombi(Converter.deCode(custid),number,date);
   			     					
   			     					if(cps != null){
   			     						Enumeration eee = cps.elements();
   			     						while( eee.hasMoreElements()){
   			     						CustProd cp = (CustProd) eee.nextElement();
   			     						if(cp.getCustID().equals(custid) && dirCustCodes.get(custid) != null) {
   			     							String cuid = Converter.enCodeEst(dirCustCodes.get(custid)+"");
   			     							if(!res.contains(cuid)) {
   			     							   res.add(cuid);
   			     							 }
   			     						  }
   				     				   }	
   			     					}
   			     				 }
   			        	      }		
   		        	       }           	
   		                }
   		      //  System.out.println(inputLineBuf);
   		        in.close();
   		        isr.close();
   		        ins.close();
   		        con.disconnect();
   		        
   		       
   		   } catch (Exception a){
   			  
   		   }
   	   }


      System.out.println("sales Size " + res.size());
	
	return res;
}

 private HashMap dirCustCodes = null ;

       public TreeMap getCustomers(Agent agent){
    	   
    	   //teeme hash seti kõigist klientidest kes pole ostnud rohkem
    	   
    	   // teeme kindlaks directo koodid mis on Nvisse sisestatud .
    	   
    	   if(!useold) {
    			  makeData();
    		  }
    	   
    	   try {
    		   
        	     
 	    	  String cmd =  " SELECT k.[No_] AS kood, [Code in DIRECTO LT] as directo FROM [2015_Tobis$Customer] k LEFT join [2015_Tobis$Ship-to Address] a on  a.[Customer No_]=k.[No_] \r\n" + 
                            " and a.Code=k.[Ship-to Code]   WHERE k.[Salesperson Code] != ''  AND Blocked != 3 and [Code in DIRECTO LT] != ''";
 	       
 	    	  dirCustCodes = new HashMap();
 	           System.out.println(cmd);
 	       
 	       con = getNewConnection2();
 	       Statement stmt1 = con.createStatement();
 	       ResultSet rs1 = stmt1.executeQuery(cmd);
 	       
 	       while(rs1.next()){
 	    	   
 	    	   String custID = rs1.getString("kood").trim();
   	           String dir = rs1.getString("directo").trim();
   	        		   
   	           dirCustCodes.put(dir, custID); 
   	           custtsUsed.add(dir);
   	           
 	       }
 	       
 	       rs1.close();
 	       stmt1.close();
 	       con.close();
    	   
    	   }catch(Exception e) {
    		   e.printStackTrace();
    	   }
    	   
    	   if(this.priceformulas == null) {
    		   this.getCustomersDirecto();
    	   }
    	   
    	 //  System.out.println("priceformulas " + priceformulas.toString());
    	   
    	   //Siin peaks directo võla listi ka sisse lugema
    	   
    	   

    	    try {

    	    	  String cmd = 
             "select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, a.[Due Date] AS kuupaev,a.[Document Date] AS kuupaev2, sum(b.[Amount]) AS summa, " + 
              "       DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [2015_Tobis$Cust_ Ledger Entry] a inner join " +
               "      [2015_Tobis$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_]  " +
                "     where   " +
				"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Open]= 1  " +
                 "    group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_], " + 
                  "   a.[Due Date],a.[Document Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date] ";

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	       con = getNewConnection2();
    	       Statement stmt1 = con.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient");
    	        debt.setCustID(conv(custID));
    	        String maksja = rs1.getString("maksja");
    	        if(maksja.trim().equals("JUTA")) {
    	        	maksja = "JUTA1";
	           		}
    	       // debt.set(conv(maksja));
    	       
    	        String doknr = rs1.getString("number");
    	        if(doknr.trim().length()>6){
    	        	doknr = doknr.substring(2,doknr.length());
    	        }
    	        debt.setDoknr(doknr);
    	        debt.setDuedate(rs1.getDate("kuupaev"));
    	        debt.setDate(rs1.getDate("kuupaev2"));
    	        debt.setSum(rs1.getDouble("summa"));
    	        int dueint = rs1.getInt("due");
    	        debt.setDue(dueint + "");
    	        int old = 0;
    	       if(dueint >= 0){
    	    	   if( overdebts.get(maksja) == null){
      	        	 overdebts.put(maksja,debt.getSum() + "");
      	        }else{
      	        	double ss = Double.parseDouble((String)overdebts.get(maksja));
      	        	  ss= ss+ debt.getSum();
      	        	 overdebts.put(maksja,ss + "");
      	        }  
    	       }
    	       
    	        
    	        if( usedcredit.get(maksja) == null){
    	        	usedcredit.put(maksja,debt.getSum() + "");
   	              }else{
   	        	  double ss = Double.parseDouble((String)usedcredit.get(maksja));
   	        	  ss= ss+ debt.getSum();
   	        	   usedcredit.put(maksja,ss + "");
   	             }
    	        
    	        if(debtDays.get(custID) != null){
    	         old = Integer.parseInt((String)debtDays.get(custID));
    	        }
    	        if(old<dueint){
    	          this.debtDays.put(custID,dueint+"") ;
    	        }
    	        this.debtList.add(debt);
    	            }

    	       rs1.close();
    	       stmt1.close();
    	       con.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	    
    	    System.out.println("debt size " + debtList.size());
    	    if(!useold) {
    	 	   Iterator enums = this.dirCustCodes.keySet().iterator();
    	 	   String gS = getParent().getProps().getProperty("keyInvoice");
    	   	    if(gS == null) {
    	   	    	System.out.println("keyInvoice is missing!");
    	   	    	System.exit(0);
    	   	    }
    	 	   while(enums.hasNext()){
    	 		String custid = (String)enums.next();
    	 		
    	 		   try{
    	 			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
    	 			
    	 			   String httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
    	 			   Converter.doTrustToCertificates();
    	 			 	URL myurl = new URL(httpsURL);
    	 		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
    	 		        
    	 		        String urlParameters  = "what=invoice&get=1&orderer=" + cuurl + "&key=" + gS;
    	 		    	
    	 		           myurl = new URL(httpsURL);
    	 		           
    	 		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
    	 		           int postDataLength = postData.length;
    	 		           con = (HttpsURLConnection)myurl.openConnection();
    	 		           con.setRequestMethod("POST");
    	 		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
    	 		           con.setRequestProperty("charset", "utf-8");
    	 		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
    	 		           con.setUseCaches(false);
    	 		           con.setDoOutput(true);
    	 		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
    	 		              wr.write( postData );
    	 		           }
    	 		        
    	 		        
    	 		        InputStream ins = con.getInputStream();
    	 		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
    	 		        BufferedReader in = new BufferedReader(isr);

    	 		        String inputLine;
    	 		        StringBuffer inputLineBuf = new StringBuffer();
    	 		        while ((inputLine = in.readLine()) != null){
    	 		        	inputLine = Converter.enCodeEst(inputLine);
    	 		        	//System.out.println(inputLine);
    	 		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
    	 		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
    	 		        	while (st.hasMoreTokens()){
    	 		        		
    	 		        		String line = st.nextToken();
    	 		        		String number = "";
    	 			        	String date = "";
    	 			        	String duedate = "";
    	 			        	String volg = "";
    	 			        	String payer = "";
    	 		        		
    	 			        	int ind =  line.indexOf("invoice number=") ;
    	 		        		if(ind != -1){
    	 			        		 number = line.substring(ind + 16);
    	 			        		 number = number.substring(0,number.indexOf("\""));
    	 			        	}
    	 			        	
    	 			        	ind =  line.indexOf("date=") ;
    	 				        	if(ind != -1){
    	 				        		 date = line.substring(ind + 6);
    	 				        		 date = date.substring(0,date.indexOf("\""));
    	 				        	}
    	 				        		
    	 				        	
    	 			        	 ind =  line.indexOf("due_date=") ;
    	 			        	if(ind != -1){
    	 			        		 duedate = line.substring(ind + 10);
    	 			        		duedate = duedate.substring(0,duedate.indexOf("\""));
    	 			        	 }
    	 			        	
    	 			        	 ind =  line.indexOf("regno=") ;
    	 				        	if(ind != -1){
    	 				        		 payer = line.substring(ind + 7);
    	 				        		 payer = payer.substring(0,payer.indexOf("\""));
    	 				        	 }
    	 				        	
    	 			        	
    	 			        	ind =  line.indexOf("balance=") ;
    	 				        	if(ind != -1){
    	 				        		 volg = line.substring(ind + 9);
    	 				        		 volg = volg.substring(0,volg.indexOf("\""));
    	 				        	 }
    	 				        //	System.out.println(custid  + " " + number + " " + date  + " " + duedate  + " " + volg );	
    	 				       
    	 			        	try{
    	 			        		 double vv = Double.parseDouble(volg);
    	 			        		 if(vv != 0){
    	 			        			 if(isDueDate(duedate)){
    	 						        		     Debt dd = new Debt();
    	 						     				  dd.setCustID(Converter.enCodeEst((String)dirCustCodes.get(custid)));
    	 						     				  
    	 						     				  dd.setDoknr("GZ-" + number);
    	 						     				  dd.setDate(getDate(date));
    	 						     				  dd.setDuedate(getDate(duedate));
    	 						     				  
    	 						     				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
    	 						     				  dd.setDue(ff + "");
    	 						     				  dd.setSum(vv);
    	 						     					//System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	
    	 						     				debtList.add(dd);  
    	 						     				 if(ff >= 0){
    	 						     					 String maksja = dd.getCustID();
    	 						         	    	   if( overdebts.get(maksja) == null){
    	 						           	        	 overdebts.put(maksja,dd.getSum() + "");
    	 						           	        }else{
    	 						           	        	double ss = Double.parseDouble((String)overdebts.get(maksja));
    	 						           	        	  ss= ss+ dd.getSum();
    	 						           	        	 overdebts.put(maksja,ss + "");
    	 						           	        }
    	 						     				 }
    	 						     			/*	  if(dueSums.get(custid) != null){
    	 						     					  double ss = Double.parseDouble(dueSums.get(custid) + "")+ vv ;
    	 						     					 dueSums.put(custid, ss);    
    	 						     				  }else{
    	 						     					 dueSums.put(custid, volg); 
    	 						     				  }
    	 						     				 debts.add(dd);*/

    	 									        		
    	 						        	 }
    	 			        			 

    	 			     				/*  if(usedCredit.get(custid) != null){
    	 			     					  double ss = Double.parseDouble(usedCredit.get(custid) + "")+ vv ;
    	 			     					 usedCredit.put(custid, ss);    
    	 			     				  }else{
    	 			     					 usedCredit.put(custid, volg); 
    	 			     				  }*/
    	 			        			 
    	 			        		 }
    	 				        	
    	 				        	  
    	 			        	}catch(Exception e){
    	 			        		//e.printStackTrace();
    	 			        	}
    	 				        
    	 			        	 
    	 		        	}           	
    	 		        }
    	 		      //  System.out.println(inputLineBuf);
    	 		        in.close();
    	 		        
    	 		       
    	 		   } catch (Exception a){
    	 			   a.printStackTrace();
    	 		   }
    	 		   
    	 	   }
    	 	 
    	    
    	    }
       	    System.out.println("debt size2 " + debtList.size());
    	    
    	    
    	/// KLIENDI ANDMETE LEIDMINE

    	      
    	      HashMap contacts = new HashMap();
try{
    	      String cmd = " select c.No_ as kood ,k.[Organizational Level Code] as firma,k.Name as nimi, k.[Phone No_] as phone , k.[Mobile Phone No_] as phone2 from [2015_Tobis$Customer] c " +
                         " INNER JOIN [2015_Tobis$Contact Business Relation] b on b.[Link to Table]=1 and (c.No_=b.No_) INNER JOIN  " +
                        " [2015_Tobis$Contact] k on b.[Contact No_]=k.[Company No_] where k.Type=1";

    	  System.out.println(cmd);
    	    
    	       con = getNewConnection2();
    	       Statement stmt1 = con.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	    	   
    	       String custID = rs1.getString("kood");
    	       String res = "";

    	       String firma = rs1.getString("firma");
    	       if(firma != null && firma.trim().length() > 0){
    	    	   res = firma.trim();
    	       }
    	       
    	       String nimi = rs1.getString("nimi");
    	       if(nimi != null && nimi.trim().length() > 0){
    	    	   res = res + " " + nimi.trim();
    	       }
    	       
    	       String phone = rs1.getString("phone");
    	       if(phone != null && phone.trim().length() > 0){
    	    	   res = res + " " + phone.trim().replaceAll(" ", "-");
    	       }
    	       
    	       
    	       String phone2 = rs1.getString("phone2");
    	       if(phone2 != null && phone2.trim().length() > 0){
    	    	   res = res + " , " + phone2.trim().replaceAll(" ", "-");
    	       }
    	       
    	       res = Converter.enCodeEst(res);
    	       if(contacts.get(custID) != null){
    	    	   String asi = (String)contacts.get(custID);
    	    	   contacts.put(custID,asi + "|" + res); 
    	       }else{
    	    	  contacts.put(custID, res); 
    	       }
    	       
    	        }
    	       
    	       rs1.close();
    	       stmt1.close();
    	       con.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }


HashMap lisainfo = new HashMap();
try{
String cmd = " select Customer_No as kood, Posting_Date as kuup, AmountLCY as summa from [_MERISOFT$Tobis$Customer_Last_Payment]";

System.out.println(cmd);

 con = getNewConnection2();
 Statement stmt1 = con.createStatement();
 ResultSet rs1 = stmt1.executeQuery(cmd);
 while(rs1.next()){
	   
 String custID = rs1.getString("kood");
 String res = "";

 String kuup = rs1.getString("kuup");
 double summa = rs1.getDouble("summa");
 if(kuup != null && kuup.trim().length() > 0){
	   res = "Paskutinis mokėjimas: (" + kuup.substring(0, 10).trim() + ") " + this.getNF().format(summa) + " eur" ;
 }
 
	   lisainfo.put(custID.trim(),res); 
 
  }
 
 rs1.close();
 stmt1.close();
 con.close();
 
 System.out.println("lisainfo " + lisainfo.size());
 
} catch (Exception e){
  e.printStackTrace();
}


HashMap points = new HashMap();

try{
String cmd = " SELECT c.no_ as klient, isnull(l.valid_points,0) as valid_points, isnull(l.unpaid_points,0) as unpaid_points \r\n" + 
		"from [2015_Tobis$Customer] c left join [_Merisoft$Tobis$Customer_loyalty_program] l on c.No_ = l.customer_no where c.[Lojalumo programa] = 1";

System.out.println(cmd);

 con = getNewConnection2();
 Statement stmt1 = con.createStatement();
 ResultSet rs1 = stmt1.executeQuery(cmd);
 while(rs1.next()){
	 
 String custID = rs1.getString("klient").trim();
 double xx = rs1.getDouble("valid_points");
 double yy = rs1.getDouble("unpaid_points");
 
	   points.put(conv(custID),"Sukaupti taškai: " + (int) xx + " (=" + (int) xx + "€ dovanų kortelei)" + "|Įsigalios apmokėjus SF: " + (int)yy); 
  }
 
 rs1.close();
 stmt1.close();
 con.close();
 
 System.out.println("points " + points.size());
 
} catch (Exception e){
  e.printStackTrace();
}


/// KLIENDI ANDMETE LEIDMINE

    	      
    	      docCodes = new HashSet();
    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();


    	   String sql =  "   SELECT k.[Name] AS nimi,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address]+' ' +k.[Address 2]+k.[City] " +
    	   	 "   AS aadress , a.[Address]+' '+a.[Address 2]+a.[City] as addr2 ,k.[Salesperson Code] AS agent ,  Blocked AS blokk,   " +
             "    [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,[Route Code] AS visit,'' AS type,  " +
             "  0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum ,k.Contact + '|' + k.[Phone No_] + '|' + k.[E-Mail] as kontakt  , " +
			 "  [Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 ,av.AVG_Line_Count as lines,av.AVG_Amount as summa,av.Last_Invoice_Date as lastdeal , [Lojalumo programa] as loyal " +
             "  	  , k.[Address] as a1,k.[Address 2] as a2,k.[City] as a3, k.[Country Code]as coun , k.[Secondary Salesperson Code] as ag2 , [Code in DIRECTO LT] as directo FROM [2015_Tobis$Customer] k LEFT join [2015_Tobis$Ship-to Address] a on  a.[Customer No_]=k.[No_] " +
              "     and a.Code=k.[Ship-to Code]  " +
				"   LEFT join [_MERISOFT$Tobis$Customer_Average_Invoice] av on  av.[Customer_No]=k.[No_] " +
				 "   WHERE k.[Salesperson Code] != ''  AND Blocked != 3" ;

    	   System.out.println(sql);
    	   try {
    		   
    		   HashSet sales5m = getSales5M();
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer(conv(id));
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	           		payer= conv(payer.trim());
    	           		
    	           		if(payer.equals("JUTA")) {
    	           			payer = "JUTA1";
    	           		}
    	             customer.setPayer(payer);
    	             
    	            
    	            	 
    	             
    	             if(overdebts.get(payer) != null){
    	            	 customer.setDebt(Double.parseDouble((String)overdebts.get(payer)));
    	              }
    	             
    	             if(usedcredit.get(payer) != null){
    	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get(payer)));
    	                 }
    	             }
    	           	
    	           	if(overdebts.get(id) != null){
   	            	    customer.setDebt(Double.parseDouble((String)overdebts.get(id)));
   	                 }
    	           	
    	           	if(usedcredit.get(id) != null){
   	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get(id)));
   	                 }
    	           	
    	             String aadress = rs.getString("aadress");
    	             String aadr2 = rs.getString("addr2");
    	             if( aadr2 != null && aadr2.trim().length()>0){
      	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr2),"Lihtuania"));
      	               name = name + " " + aadr2;
      	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadr2));
      	             }else if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Lihtuania"));
    	               name = name + " " + aadress;
    	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadress));
    	             }
    	         	String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag));
    	             
    	             if (ag != null && ag.equals("MITU")){
    	 			          Enumeration enums2 = getParent().getAgentsHash().elements();
    	 			          while (enums2.hasMoreElements()) {
    	 			            Agent a = (Agent) enums2.nextElement();
    	 			            customer.addAgent(a);
    	 			          }

    	 			}
    	             
    	             if (ag != null ){
	           				Agent agg = ((Agent)this.getAgents1().get(ag));
	         				
	           				if(agg != null && agg.getDubbler().size()>0){
	           					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           					}
	           				}
	           			}
    	             

    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlock(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	          //   customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
    	           	}
    	           	
    	           	
    	            String visit = rs.getString("visit");
    	            
     	             if(visit != null && visit.trim().length()>0){
     	        	  customer.setVisit(Converter.getVisitDayLetter(visit));
     	             }
     	               
    	       
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("D"," ");
    	               paym = paym.replaceAll("PÄEVA"," ");
    	               paym = paym.replaceAll("PäEVA"," ");
    	               paym = paym.replaceAll("SR","-1").trim();
    	        	customer.setPaymTerm(paym);
    	             }

    	         
    	           String cont = rs.getString("kontakt");
    	           if(cont != null && cont.trim().length()>0){
    	               customer.setPersonalData(Converter.enCodeEst(cont.trim().replaceAll("\n", " ").replaceAll(System.getProperty("line.separator")," ").replaceAll("\r", " ")));   
    	           }
    	           if(contacts.get(customer.getID()) != null){
    	        	   if(customer.getPersonalData() != null && customer.getPersonalData().trim().length() > 0){
    	        		   customer.setPersonalData(customer.getPersonalData() + "|" + contacts.get(customer.getID()));
    	        	   }else{
    	        		   customer.setPersonalData((String)contacts.get(customer.getID())); 
    	        	   }
    	           }
    	           
    	           
    	           if(payer != null && lisainfo.get(payer) != null){
  	            	 String inff = (String)lisainfo.get(payer);
  	    	        	   if(customer.getPersonalData() != null && customer.getPersonalData().trim().length() > 0){
  	    	        		   customer.setPersonalData(customer.getPersonalData() + "|" + inff);
  	    	        	   }else{
  	    	        		   customer.setPersonalData(inff); 
  	    	        	 }
  	                  }
    	           
    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	   
    	        	
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	        	if(customer.getGroup().trim().length() >0){
    	        	customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	}else{
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	}
    	           }
    	          
    	           boolean lastDealNo = false;
    	           String lines  = rs.getString("lines");
    	           if(lines != null && lines.trim().length()>0){
    	        	   double summa   = rs.getDouble("summa");
    	        	   String ldate  = rs.getString("lastdeal");
    	        	   Calendar cc = Calendar.getInstance();
    	        	   int y = Integer.parseInt(ldate.substring(0,4));
    	        	   int m = Integer.parseInt(ldate.substring(6,7));
    	        	  int d =  Integer.parseInt(ldate.substring(9,10));
    	        	  cc.set(Calendar.YEAR, y);
    	        	  cc.set(Calendar.MONTH, m);
    	        	  cc.set(Calendar.DAY_OF_MONTH, d);
    	        	  cc.add(Calendar.MONTH, 5);
    	        	  
    	        	  if(cc.getTime().before(new Date())) {
    	        		  lastDealNo = true;
    	        	  }
    	        	  
    	        	   customer.setInfo(Converter.enCodeEst("Paskutinė SF data: " + ldate.substring(0,10) + "|Vidutinė SF (iš 4 paskutinių):|" 
                   +  "*  Eilučių sk.:" + lines +"|  " +
    	        	   		"   Suma: " + (int)summa + " €." ));
    	           }
    	           
    	           if(sales5m.contains(customer.getID()) || lastDealNo) {
    	        	   if(customer.getInfo().trim().length() > 10) {
      	        	     customer.setInfo(customer.getInfo() + "|Pirko seniau nei prieš 5 mėn. – Reikia atlikti rizikos vertinimą.Kreiptis į skolų valdymo specialistę");
    	        	   } else {
    	        	     customer.setInfo("Pirko seniau nei prieš 5 mėn. – Reikia atlikti rizikos vertinimą.Kreiptis į skolų valdymo specialistę");
    	        	   }
    	        	   
    	           }
    	          
    	           String loyal  = rs.getString("loyal");
    	           if(loyal != null) {
    	        	   if( loyal.trim().equals("0")) {
    	        		   customer.setCity("Lojalumo statusas: Nedalyvauja");
    	               }else {
    	            	   String add = "";
    	            	   if(points.get(customer.getID()) != null) {
    	            		   add = (String)points.get(customer.getID());
    	            	   }
    	            	   customer.setCity("Lojalumo statusas: Taškai kaupiami|" + add );
    	                 }
    	        	   
    	           }
    	           
    	           String a1 = rs.getString("a1");
    	           if(a1 != null && a1.trim().length()>0){
    	        	   String str = a1.trim();
    	        	   String a2 = rs.getString("a2");
        	           if(a2 != null && a2.trim().length()>0){
        	        	   str = str + " " + a2.trim();
        	        	   
        	           }   
        	           customer.restStreet = str;
    	           }
    	           String a3 = rs.getString("a3");
    	           if(a3 != null && a3.trim().length()>0){
    	        	   customer.restCity = a3.trim();
    	           }
    	           String a4 = rs.getString("coun");
    	           if(a4 != null && a4.trim().length()>0){
    	        	   customer.setCountry(a4.trim());
    	           }
    	          // customer.setPayer(id);
    	           
    	           
    	       	String ag2 = rs.getString("ag2");
    	       	if(ag2 != null && ag2.trim().length() > 0) {
    	       		customer.addAgent(new Agent(ag2));
    		    }
    	       	

  	           customer.setName(Converter.enCodeEst(id + " " + name));
    	       	
    	    	String dire = rs.getString("directo");
    	       	if(dire != null && dire.trim().length() > 0) {
    	       		customer.addCity("Directo Kodas: " + dire);
    	       		//if(dire.equals("9535"))
    	       		//System.out.println("pr formula " + priceformulas.get(dire) + " " + customer.getID() );
    		         	if(priceformulas.get(dire) != null) {
    		         		 if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
    	    	        		   customer.setGroup(customer.getGroup() + "|" + priceformulas.get(dire) );
    	    	        	   }else{
    	    	        		   customer.setGroup(priceformulas.get(dire) +""); 
    	    	        	   }
    		         	}
    		         	
    		         	if(this.fixCustomers.get(dire) != null ) {
			         		  if("true".equals(getParent().getProps().getProperty("fixed.frompricelist")) ){
			       			   customer.addFixSortCode(fixCustomers.get(dire)+""); 
			       		//	System.out.println("On Fix " + customer.getName() + " " +  price_list);
			       		       }
    		         	}

    	    	           customer.setName(Converter.enCodeEst(id + " "  + dire + " " + name));	
    	       	}
    	       	

	             
    	            tm.put(Converter.cleanLitLatEst(id) + customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	            if(!docCodes.contains(customer.getID())){
               	     docCodes.add(customer.getID());
                  }
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();



    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   
    	   
    	   if("true".equals(getParent().getProps().getProperty("fixed.frompricelist"))){
    	    	  fixedLines = getFixedAssortmentData1();
    	    	     }
    	   
    
    	     return tm;
    	   }

       
       
       
       
       public TreeMap getProducts2(){

    	    TreeMap tm = new TreeMap();
    	     
    	     try {
    	 
    	    	 File f = new File (getDir() + "tooted.xml");
    	         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		     DocumentBuilder db;
    			db = dbf.newDocumentBuilder();      	
    	    	FileInputStream in = getCleanInputStream(f);
    	    	String handler = null;
    	        Document doc = db.parse(in,handler);
    	        Node root = getNode(doc, "transport");      
    	   
    	    	Node read = getNode(root, "items");
    	        for (int i = 0; i < read.getChildNodes().getLength(); i++){
    	        	  Node rida = read.getChildNodes().item(i);
    	        	
    	        		  if(rida.getNodeName().equals("bi_item")){
    	        		
    	       
    	        		String id =  rida.getAttributes().getNamedItem("code").getNodeValue();  
    	        		try {
    	        		  String namec =  rida.getAttributes().getNamedItem("name").getNodeValue(); 
    	        	
    	      	     Product prod = new Product(id);
    	      	     prod.setName(namec);
    		        	
    		      	     tm.put(prod.getID(), prod); 
    	        		}catch(Exception e) {
    	        			
    	        		}
    	        	  }
    	        	
    	          }
    	        in.close();
    	    
    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	     return tm;
    	     
    	   }


       public Vector getFixedAssortmentData1() {
           Vector lines = new Vector();
           TreeMap prods = this.getProducts2();
        //  System.out.println("prods " + prods.size() + " " + pricelists.toString());
           
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
               			 String clGroup = " ";
                         String prodID = null;
                         String start = null;
                         String end = null;
                         MatrixLine mline = new MatrixLine();
                        try{
                        	 clGroup = (rida.getAttributes().getNamedItem("code").getNodeValue()); 
                        	  try{
                        		  prodID =  rida.getAttributes().getNamedItem("item").getNodeValue(); 
                        	  }catch(Exception rr) {
                        		 // System.out.println("jama " + i + " " + clGroup);
                        	  }
                        	  
                            
                            try { 
                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
                  	           }catch(Exception aa){ }
                  	        
                            try{
                               end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
                      	      }catch(Exception aa){ }
                             
                        
                             if (clGroup != null && clGroup.trim().length() > 0) {
                               mline.setCliGroup(clGroup.trim());
                             }

                             if (prodID != null && prodID.trim().length() > 0) {
                               mline.setProdID(prodID.trim());
                             }
                        
                           
                             if(prodID != null && prods.get(prodID) != null && pricelists.contains(clGroup.trim())){
                            	
                            	
                            	 
                            	
                            	
                            	if(start != null && end != null) {
                            		 Date alg = getCalXML(start); 
                                     Date lopp = getCalXML(end); 
                               	 
                               	  if(alg.before(new Date()) && lopp.after(new Date())){
                                      	 lines.add(mline);	
                                      }	
                            	}else {
                            		lines.add(mline);
                            	}
                            	 	
                              }
                        
                           
                        }catch(Exception e){
                        	e.printStackTrace();
                        }
                        
                       }
                    	
               	  }catch(Exception e){
               		  e.printStackTrace();
               	  }
                    
               }
               
       	}catch(Exception e){
       		e.printStackTrace();
       	}
          // System.out.println("prodsinuse " +prodsInUse.size() );
           
           return sortLines(lines,prods);
     }

       private Vector sortLines(Vector inVec,TreeMap products){
    	   TreeMap asi = new TreeMap();
    	       // System.out.println("Prod22 " + products.size());
    	        for (int i = 0 ; i < inVec.size() ; i++){
    	        	
    	        	  MatrixLine ol = (MatrixLine)inVec.elementAt(i);
    	          asi.put(ol.getCliGroup()+ ((Product)products.get(ol.getProdID())).getName() , ol);
    	        }
    	        Vector sorted = new Vector ();
    	        Iterator itera = asi.keySet().iterator();
    	        while (itera.hasNext()){
    	        	String key = (String)itera.next();
    	        	sorted.add((MatrixLine)asi.get(key));
    	        }
    	       
    	     //  System.out.println("sorted Size " + sorted.size());
    	        return sorted;
    	      }
    	


       private Vector fixedLines = new Vector();
       
   public TreeMap getAgents1() {
	   
		return agents1;
	}
   
   
   
   public void getCustomersDirecto(){
	  
	   priceformulas = new HashMap();
	   fixCustomers = new HashMap();
	     
	   try {
	 		  File f = new File (getDir() + "kliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root = getNode(doc, "transport");      
	      //   System.out.println(root);
	     	Node read = getNode(root, "customers");
	     	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	         for (int i = 0; i < read.getChildNodes().getLength(); i++){
	         	  Node rida = read.getChildNodes().item(i);
	         	  
	         	 // System.out.println("node name " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("bi_customer")){
	         		 String name = null;
	         		boolean addaadr = true;
		         	  try{
		         		   name =  rida.getAttributes().getNamedItem("name").getNodeValue();
		         		  String id =  rida.getAttributes().getNamedItem("code").getNodeValue();
		         		  Customer customer = new Customer(id);
		         		  customer.restName = name;
		         		
		         		  try{
		         			  String payer = rida.getAttributes().getNamedItem("payer").getNodeValue();
			         		  
			         		  if(payer != null && payer.trim().length() > 0 ){
			         			  customer.setPayer(payer.trim());
			         		  }
	  
		         		  }catch(Exception e){
		         			  
		         		  }
		         		
		         	/*	if(this.usedCredit.get(id) != null){
		         			customer.setUsedCredit(Double.parseDouble(usedCredit.get(id)+ ""));
		         		}
		         		
		         		if(this.dueSums.get(id) != null){
		         			customer.setDebt(Double.parseDouble(dueSums.get(id)+ ""));
		         		}
		         		*/
		         		 String country = "Lithuania";
		       		  if(getParent().getProps().getProperty("country") != null){
		       			  country = getParent().getProps().getProperty("country");
		       		  }
		         		
		         		
		         		 String sendaadr = ""; 
		         		try{ 
		 	         		  sendaadr =  rida.getAttributes().getNamedItem("send_address_street").getNodeValue(); 
		 	         		  customer.restStreet = sendaadr.replaceAll(";", ",");
		 	         		  name = name + " " + sendaadr;
		 	         		addaadr = false;
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	
		         		
		         		}catch(Exception ee){
		         		 }
			         	
		         		
		         		String asula = "";
		         		try{ 
		 	         		  asula =  rida.getAttributes().getNamedItem("send_address_county").getNodeValue().replaceAll(";", ","); 
		 	         		sendaadr = sendaadr + " " + asula;
		 	         		customer.restCity = asula; 
		 	         	
		 	         		String maa = "";
		 	         		try{
		 	         	  maa =  rida.getAttributes().getNamedItem("send_address_country").getNodeValue().replaceAll(";", ","); 
		 	         		country = maa;
		 	         		}catch(Exception eed){
			         		 }
		 	         		
		 	         		customer.setAddress(Converter.adressLink(sendaadr, country));
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		

		         		String regno = "";
		         		try{ 
		         			regno =  rida.getAttributes().getNamedItem("regno").getNodeValue(); 
		 	         		customer.setRegnr(regno);
		 	         	 }catch(Exception ee){
		         		 }
                         
		         		

		         		String limit = "";
		         		try{ 
		         			limit =  rida.getAttributes().getNamedItem("limit").getNodeValue(); 
		 	         		customer.setCredit(limit);
		 	         	 }catch(Exception ee){
		         		 }
		         		String paymentterm = "";
		         		try{ 
		         			paymentterm =  rida.getAttributes().getNamedItem("paymentterm").getNodeValue(); 
		 	         		customer.setPaymTerm(paymentterm);
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         	

		         		try{ 
		         			double discount  =  Double.parseDouble(rida.getAttributes().getNamedItem("discount").getNodeValue());
		         			if(discount > 0){
		         		 		customer.setDiscount(discount);
		    		 	  }
		 	         	 }catch(Exception ee){
		         		 }
		         		
		         		
		         		 String aadr = "";
		         		 String aadr2 = "";
		         		 try{ 
		 	         		  aadr =  rida.getAttributes().getNamedItem("del_address1").getNodeValue(); 
		 	         		  customer.restStreet = aadr;
			 	         	  customer.setAddress(Converter.adressLink(aadr, country));
			 	         	  if("true".equals(getParent().getProps().getProperty("use.county")) && addaadr){
			 	         			 name = name + " " + aadr;
			 	         		  }
			 	         	  
		 	         		
		 	         	 } catch (Exception ee){ }
		         		 
		         		
		         		 try{ 
		 	         		  aadr2 =  rida.getAttributes().getNamedItem("del_address2").getNodeValue(); 
			 	         	  customer.restCity = aadr2;
		 	         	 } catch (Exception ee){ }
		         		 
		         		 
		         		  customer.setName(customer.getID() + " " + name);
		         	
		         		 
		         		
		         		 try{ 
		 	         		 String yldphone =  rida.getAttributes().getNamedItem("phone").getNodeValue();
		 	         		 if(yldphone.trim().length() > 0){
		 	            		customer.setPersonalData("Tel: " +(yldphone).replaceAll(";", ","));
		 	            		customer.restPhone = yldphone;
		 	  		         }
		 	     		 }catch(Exception ee){
                        	
		         		 }

		         		 
		         		 try{ 
		 	         		 String email =  rida.getAttributes().getNamedItem("email").getNodeValue();
		 	         		 if(email.trim().length() > 0){
		 	            		customer.adPersonalData("|" + (email).replaceAll(";", ","));
		 	            		
		 	  		         }
		 	     		 }catch(Exception ee){
                        	
		         		 }
		         		  
		         		  try{
		         			  
		         			 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
		         			  Node nn = rida.getChildNodes().item(r);
		       	    		 if(nn.getNodeName().equals("contact")){
		         				 String cname =  nn.getAttributes().getNamedItem("name").getNodeValue();
		         				String ctel =  nn.getAttributes().getNamedItem("telephone").getNodeValue();
		         				customer.restPerson = cname;
		         				customer.restPerPhone = ctel;
		         				if(customer.getPersonalData().trim().length() < 1){
		         					customer.setPersonalData((cname + " " + ctel).replaceAll(";", ","));
			         			}else{
			         				String lisa = (cname + " " + ctel).replaceAll(";", ",");
			         				if(customer.getPersonalData().indexOf(ctel) != -1){
			         					lisa = (cname ).replaceAll(";", ",");
			         				}
		         				    customer.setPersonalData(customer.getPersonalData() + "|" + lisa);
		         				}
		         				String cemail =  nn.getAttributes().getNamedItem("email").getNodeValue();
		         				customer.restPerEmail = cemail;
		         				
		  	         		
		  	         		 }}
		         		  }catch(Exception r){
		         			  
		         		  }
		         		  
		         		  
		         		 try{
		         			  
		         			 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
		         			  Node nn = rida.getChildNodes().item(r);
		       	    		 if(nn.getNodeName().equals("contact")){
		         				 String cname =  nn.getAttributes().getNamedItem("name").getNodeValue();
		         				String ctel =  nn.getAttributes().getNamedItem("telephone").getNodeValue();
		         				customer.restPerson = cname;
		         				customer.restPerPhone = ctel;
		         				if(customer.getPersonalData().trim().length() < 1){
		         					customer.setPersonalData((cname + " " + ctel).replaceAll(";", ","));
			         			}else{
			         				String lisa = (cname + " " + ctel).replaceAll(";", ",");
			         				if(customer.getPersonalData().indexOf(ctel) != -1){
			         					lisa = (cname ).replaceAll(";", ",");
			         				}
		         				    customer.setPersonalData(customer.getPersonalData() + "|" + lisa);
		         				}
		         				String cemail =  nn.getAttributes().getNamedItem("email").getNodeValue();
		         				customer.restPerEmail = cemail;
		         				
		  	         		
		  	         		 }}
		         		  }catch(Exception r){
		         			  
		         		  }
		         		      	boolean fix = false;	  
		         		 try{
		         			  
		         			 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
		         			  Node nn = rida.getChildNodes().item(r);
		       	    		 if(nn.getNodeName().equals("datafields")){
		         				
		         				 for (int re = 0; re < nn.getChildNodes().getLength(); re++){
		         					 Node nn2 = nn.getChildNodes().item(re);
		         					 
		         					if("data".equals(nn2.getNodeName())) {
		         						
		         					
		         						
		         						
		         						//System.out.println("nodes " + nn2.getAttributes().getNamedItem("code").getNodeValue());
		         					
		         					if("FIKSUOTAS_ASORTIMENTAS".equals(nn2.getAttributes().getNamedItem("code").getNodeValue())) {
		         						
										if("TAIP".equals(nn2.getAttributes().getNamedItem("content").getNodeValue())) {
											fix = true;
										//			System.out.println("On Fix " + customer.getName());
										}
			         					
		         					}
		         				   }
		         				 }
		  	         		
		  	         		 }}
		         		  }catch(Exception r){
		         			//  r.printStackTrace();
		         		  }
		         		      		  
		         		  
		         		  
		         		  try{
		         			 String price_list =  rida.getAttributes().getNamedItem("priceformula").getNodeValue();         	
			         		   customer.setGroup(Converter.deCode(price_list));
			         		   
			         		   if(dirCustCodes.get(id) != null) {
			         			  priceformulas.put(id, price_list);
			         			  if(fix) {
			         			   fixCustomers.put(id,price_list);
			         			  }
				         			//System.out.println("On  " + id + " "+ dirCustCodes.get(id) + " " +customer.getName() + " '" +  price_list+"");
			         		   }
			         		
		         		  }catch(Exception e){
		         		  }
		         		  
		
		         		  try{
		         			  String	 price_list =  rida.getAttributes().getNamedItem("price_list_special").getNodeValue() ;
		         			  
		         			  StringTokenizer ss = new StringTokenizer(price_list,",");
		         			  while (ss.hasMoreTokens()){
		         				  
                                   String as = (ss.nextToken());
			         			  
                                  if(customer.getGroup() != null && customer.getGroup().trim().length() > 0){
			         				 customer.setGroup(customer.getGroup() + "|" + Converter.deCode(as));
					         	    }else{
			         				 customer.setGroup(Converter.deCode(as));
					         	   }
					         		
                                   if("true".equals(getParent().getProps().getProperty("fixed.frompricelist")) && fix){
					       			   customer.addFixSortCode(Converter.deCode(as)); 
					       		   }  
		         			  }
		         			  
			         		  }catch(Exception e){
			         		  }
		         		  
		         		  
		         		  boolean goes = true;
		         		  
		         		 try{
		         			 String closed =  (rida.getAttributes().getNamedItem("closed").getNodeValue());         	
			         		  if(closed.equals("1")) {
			         			  goes = false;
			         			 // System.out.println("closed " + customer.getName());
			         		  }
		         		  }catch(Exception e){
		         		  }
		         		  
		         		  
     		try {
	         		 String salesman  = rida.getAttributes().getNamedItem("agent").getNodeValue(); 
	         		      customer.addAgent(new Agent(Converter.deCode(salesman.toUpperCase())));
	         		      customer.setMainAgent(Converter.deCode(salesman.toUpperCase()));
	         		      String sm = parent.getEmail(salesman);
	         		      
	         		    
	                  if(sm != null && sm.length() > 1 && goes){
	                	//  System.out.println(sm);
	            	     // tm.put(customer.getName() + " " + customer.getID(), customer);
	                   //   this.customers.add(customer.getID());
	                  }
     		  }catch(Exception t){
	         	 // t.printStackTrace();
         	  }
		         	  }catch(Exception e){
	         		
	         		  e.printStackTrace();
	         	  }
	              
	         	  }}
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
	  // System.out.println("priceformulas " + priceformulas.toString());
	 
	 //  System.exit(0);  
   
   }
   
   HashSet docCodes = new HashSet();
   private TreeMap agents1 = new TreeMap();
  private  HashSet prodsUsed = null;
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();

     try {
		this.getProdGroups();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
     
     docCodes = new HashSet();
     prodsUsed = new HashSet();

     
     HashMap<String, Double> maxdisc = new HashMap<String, Double>();
     
     try {
   	  String cmd = "select [Group Code] as kood, [Group 2 Code] as kood2 , code ,  [Max Discount]  as mdisc from [NAV40].[dbo].[2015_Tobis$Dimension Value] where  [Max Discount]  > 0 " ;
   	  
      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	              	String toode =rs.getString("kood");
  	              	String toode2 =rs.getString("kood2");
  	              	String toode3 =rs.getString("code");
  	              	
  	                Double seis = rs.getDouble("mdisc");
  	              
  	              if(toode != null && toode.trim().length() > 0)
  	              maxdisc.put(toode, seis );

  	              if(toode2 != null && toode2.trim().length() > 0)
  	              maxdisc.put(toode2, seis );

  	              if(toode3 != null && toode3.trim().length() > 0)
  	              maxdisc.put(toode3, seis );
  	              	
  	        }

  	      rs.close();
  	      stmt.close();
  	      con.close();

  	   // System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     TreeMap  ladutr = new TreeMap();
      TreeMap  ladulah = new TreeMap();
     
     try {
   	  String cmd = "select [Item No_] as kood, inventory as kogus,PickupQty from [_MERISOFT$Tobis_ProdStock]" ;
   	  
      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood");
  	              	seis = rs.getDouble("kogus");
  	          	double seis2 = rs.getDouble("PickupQty");
  	              	ladutr.put(toode, seis + "");
  	              	if(seis2 > 0)
  	          	    ladulah.put(toode, seis2 + "");
  	        }

  	      rs.close();
  	      stmt.close();
  	      con.close();

  	   // System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     Hashtable dateOut = new Hashtable();
    


    
     
  
     try {
     
    	 String lubaSQl = " SELECT [No_] as toode from [2015_Tobis$Item] where  [Show in mob_ device] = 1";
    	 HashSet luba = new HashSet();
    	 
    	 Connection con = getNewConnection2();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(lubaSQl);
         while(rs.next()){
        	  String id = rs.getString("toode").trim();
        	  luba.add(id);
         }
       //  System.out.println("luba suurus " + luba.size());
           rs.close();
           stmt.close();
           con.close();
           
           
           
         String sql3 = "  select distinct ItemNo from  ( select sp.[Item No_] as ItemNo from [2015_Tobis$Sales Price] sp " +
         "  left join [2015_Tobis$Item] i on i.No_=sp.[Item No_] where sp.[Sales Type]=2 and CONVERT (date, GETDATE())between sp.[Starting Date] and sp.[Ending Date] " +
          " and i.[Sell Out]=0  union  select i.No_ as ItemNo from [2015_Tobis$Item] i where i.Promotion=1 and i.[Sell Out]=0) f ";

 HashSet top = new HashSet();
    	 
    	  con = getNewConnection2();
          stmt = con.createStatement();
          rs = stmt.executeQuery(sql3);
         while(rs.next()){
        	  String id = rs.getString("ItemNo").trim();
        	  top.add(id);
         }
       //  System.out.println("top suurus " + top.size());
           rs.close();
           stmt.close();
           con.close();
           
           
           
           
    	 String cmd = "  SELECT  t.[No_] AS toode,(t.[Description] + ' ' +   t.[Description 2] ) AS nimi, t.[Unit Price] AS myygihind,"
    	 		+ " '' AS grupp2,t.[Global Dimension 2 Code] AS grupp " +
			" ,t.[Base Unit of Measure] AS unit,[No_ 2] AS ean,'' AS status ,0 AS maxdiscount, " +
			 " '' AS type, [Units per Parcel] AS kastis, [Sell only in parcels]  as optam ,[Item Disc_ Group] as alegr ,  '' AS kaal, "
			 + "[VAT Prod_ Posting Group] AS vat ,[Units per Pallet] as pallet " +
			 " ,[Unit List Price] as listprice,[New until] as newprod ,[Nelikvidas]  as status2 , [Minimum Unit Price]  as jaeh FROM [2015_Tobis$Item] t  WHERE t.[Blocked]=0    ORDER BY t.[No_] ";
      
    	 System.out.println(cmd);
     	 con = getNewConnection2();
        stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            String weight ="";
            Product prod = new Product(id);
         	String name = Converter.enCodeEst(id + " " + rs.getString("nimi").trim()).replaceAll("11153xx", "x2") + " TOBIS";
         	
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }else{
            	  prod.setStock(0);
              }
              
              if(ladulah.get(id) != null ){
            	  prod.setPartii("PickedQty: " + (String)ladulah.get(id));
              }
             //   String grupp2 = cleardots(rs.getString("grupp2"));          
          	
      		//if(grupp2 != null && grupp2.trim().length() >0){
      			//prod.setGroupSecond(grupp2.trim());	
      		//}
              
              String grupp = cleardots(rs.getString("grupp"));
        		if(grupp != null && grupp.trim().length() >0){
        			grupp = grupp.trim();

                 	if(maxdisc != null && maxdisc.get(grupp) != null){
                    prod.setMaxDiscount(maxdisc.get(grupp));
                    }
        			 if(this.cateFullGroup.get(grupp) != null) {
                  	   String g1 = cateFullGroup.get(grupp);
                  	 

                    	if(maxdisc != null && maxdisc.get(g1) != null){
                       prod.setMaxDiscount(maxdisc.get(g1));
                       }
                  	   prod.setGroup(g1);
                  	   prod.setGroupSecond(g1+grupp);
                     }
        			//prod.setGroup(Converter.cleanLitLatEst(grupp.trim()));	
        		}
        	prod.setType("TOBIS");
        		
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null && ean.trim().length()>0){
                   prod.setEAN(ean);
                   if(ean.trim().length() > 4)
                   name = name + " " +ean.trim().substring(ean.trim().length()-4) ;
                 }
                    
                    if(top.contains(prod.getID())){
                    	prod.setStatusTop("1");
                    }
  
              


                double	cases = rs.getDouble("kastis");
                	if(cases > 0){
                       prod.setInCases(cases);
                	}

                    int	opt = rs.getInt("optam");
                    	if(cases > 0 && opt == 1){
                           prod.setOptAmountInUnits(cases);
                    	}                
                	String alegrupp = rs.getString("alegr");
                	if(alegrupp != null && alegrupp.trim().length() > 0){
                		prod.setAlegrupp(Converter.cleanLitLatEst(alegrupp));	
                	}
                 


          	 String vat = rs.getString("vat");
          	 int vati = 21;
                   if(vat != null && vat.trim().length()>0){
                	 	prod.setVat(vat.trim());
                	 	try{
                	 	//	vati = Integer.parseInt(vat.trim());	
                	 	}catch(Exception e){
                	 		
                	 	}
                	 
                   }
               
                   double pallet = rs.getDouble("pallet");
                   if(pallet >0){
                	   prod.setOnPallet(pallet);
                   }
                   
                   double listprice = rs.getDouble("listprice");
                   if(listprice >0){
                	   prod.setPriceSecond(Converter.Round((float)(listprice * 1.21), 2));
                   }
                   
                   
                   Date newprod = rs.getDate("newprod");
                   if(newprod.after(new Date())){
                	  prod.setStatusNew("1");
                	 //  prod.setMustBe("1");
                   }
                   try {
                	   int status = rs.getInt("status2");
                       if(status > 0) {
                    	   prod.setStatusSellout("1");
                       }   
                   }catch(Exception e) {
                	   
                   }
                 
                   try {
                	   double status = rs.getDouble("jaeh");
                       if(status > 0) {
                    	   prod.addPartii("|  Min pard. kaina: " + getNF().format(status) + "€");
                       }   
                   }catch(Exception e) {
                	   
                   }
                   
                   
                   if(luba.contains(prod.getID()) && !name.equals("")){
                	   prod.setName(name);
                       tm.put(name, prod);  

           	    	prodHash.put(prod.getID(), prod);
                       String normcode = prod.getID().replace('/', '_');
                       if(!docCodes.contains(normcode)){
                    	     docCodes.add(normcode);
                       }
                       prodsUsed.add(prod.getID());
                   }
          
        }
        
  	     
      
        rs.close();
        stmt.close();
        con.close();
        
        StringBuffer buf = new StringBuffer();
         cmd = " SELECT [Sales Code] AS action,[Item No_] AS prodid, \r\n" + 
        		"      [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from\r\n" + 
        		"       [2015_Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or\r\n" + 
        		"                year ([Ending Date]) = 1753)  and [Sales Type] = 3  \r\n" + 
        		"				union \r\n" + 
        		"    select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val \r\n" + 
        		"               from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 3 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or \r\n" + 
        		"                 year ([Ending Date]) = 1753) ";
      
    	// System.out.println(cmd);
     	 con = getNewConnection2();
         stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
        	 String  gruppx = rs.getString("action").trim();
           	String  toodex = rs.getString("prodid").trim();
           	try{
           		double  hind  = rs.getDouble("price");
            	double  disc  = rs.getDouble("disc");
             	double  kogus = 0;//rs.getDouble("kogus");
              
               	
                java.sql.Date end = rs.getDate("paev");
                long ttt = 0;
                if(end.after(new Date())) {
               	 ttt = end.getTime();
                }
               	
               
               	      buf.append(toodex+ ";" + gruppx + ";" + gruppx + ";" + NF.format(hind).replace(',', '.') + "; ;" + kogus + "; ;" + ttt + ";" + disc );
                       buf.append("\n");	
                       
           		
           	}catch(Exception e){
           		
           	}
            
        }
        
        rs.close();
        
        
       
        // siin kliendi põhised koguselised
                 cmd = "  SELECT [Sales Code] AS action,[Item No_] AS prodid, \r\n" + 
                 		"        [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val, [Minimum Quantity] as am from\r\n" + 
                 		"         [2015_Tobis$Sales Price] where [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or\r\n" + 
                 		"                  year ([Ending Date]) = 1753)  and [Sales Type] = 0 and  [Minimum Quantity] > 1 "
                 		+ " union SELECT [Sales Code] AS action,[Item No_] AS prodid,  \r\n" + 
                 		"                 		        [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Sales Type] as val, [Minimum Quantity] as am from \r\n" + 
                 		"                 		        [2015_Tobis$Sales Price] where [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                 		                 year ([Ending Date]) = 1753)  and [Sales Type] = 3 " + 
                 		"					union \r\n" + 
                 		"	select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val ,[Minimum Quantity] as am\r\n" + 
                 		"                 from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                   year ([Ending Date]) = 1753) and  [Minimum Quantity] > 1 union "
                 		+ "select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,\r\n" + 
                 		"							[Sales Type] as val ,[Minimum Quantity] as am\r\n" + 
                 		"                 		                 from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] in (3) and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                 		                   year ([Ending Date]) = 1753)";
               
             //	 System.out.println(cmd);
              	 con = getNewConnection2();
                  stmt = con.createStatement();
                  rs = stmt.executeQuery(cmd);
                 while(rs.next()){
                 	    String  gruppx = rs.getString("action").trim();
                    	String  toodex = rs.getString("prodid").trim();
                    	try{
                        double  hind  = rs.getDouble("price");
                     	double  disc  = rs.getDouble("disc");
                      
                         java.sql.Date end = rs.getDate("paev");
                         long ttt = 0;
                         if(end.after(new Date())) {
                        	 ttt = end.getTime();
                         }
                         String  val  = rs.getString("val").trim();
                         String aa = " ";
                         if(val.equals("3")) {
                        	 aa = gruppx;
                        	 gruppx = " ";
                         }
                     	double  kogus = rs.getDouble("am");
                        	if(kogus == 0){
                        		kogus = 1;
                        	}
                        	
                        	      buf.append(toodex+ ";" + aa + ";" + " " + ";" + NF.format(hind).replace(',', '.') + ";" 
                        	      + " " + ";" + (int)(kogus+0.001) + "; ;" + ttt + ";" + disc + ";" + gruppx);
                                buf.append("\n");	
                                
                        	
                    	}catch(Exception e){
                    		
                    	}
                     
                 }
                 
                 rs.close();
                 
                 // siin mitte kliendi põhised koguselised
                 cmd = "  SELECT [Sales Code] AS action,[Item No_] AS prodid, \r\n" + 
                 		"        [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val, [Minimum Quantity] as am from\r\n" + 
                 		"         [2015_Tobis$Sales Price] where [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or\r\n" + 
                 		"                  year ([Ending Date]) = 1753)  and [Sales Type] = 2 and  [Minimum Quantity] > 1\r\n" + 
                 		"					union \r\n" + 
                 		"	select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val ,[Minimum Quantity] as am\r\n" + 
                 		"                 from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 2 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                   year ([Ending Date]) = 1753) and  [Minimum Quantity] > 1 ";
               
             	 System.out.println(cmd);
              	 con = getNewConnection2();
                  stmt = con.createStatement();
                  rs = stmt.executeQuery(cmd);
                 while(rs.next()){
                 	    String  gruppx = rs.getString("action").trim();
                    	String  toodex = rs.getString("prodid").trim();
                    	try{
                        double  hind  = rs.getDouble("price");
                     	double  disc  = rs.getDouble("disc");
                      
                         java.sql.Date end = rs.getDate("paev");
                         long ttt = 0;
                         if(end.after(new Date())) {
                        	 ttt = end.getTime();
                         }
                        	
                     	double  kogus = rs.getDouble("am");
                        	if(kogus == 0){
                        		kogus = 1;
                        	}
                        	
                        
                        	
                        	// siia teha number format 
                        	
                        	      buf.append(toodex+ "; ; ;" + NF.format(hind).replace(',', '.') + ";" 
                        	      + " " + ";" + (int)(kogus + 0.001) + "; ;" + ttt + ";" + disc + ";" );
                                buf.append("\n");	
                                
                 
                                
                    		
                    	}catch(Exception e){
                    		
                    	}
                     
                 }
                 
                 rs.close();
                 
        
        stmt.close();
        con.close();  
        
        
        if(buf.length() > 2){
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write(buf.toString());
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }else{
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write("");
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }
        
        
        
     } catch (Exception e){
       e.printStackTrace();
     }
     
     getProductsDirecto(tm,prodsUsed, maxdisc);
     
     return tm;
   }

   
	public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(obj);
		oos.close();
	}
   
   public static String deCode(String res ){
	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
   return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
TreeMap sorting = new TreeMap();
     
     try {
    	 String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount , t.[Search Description] as nimi" +
    	 		",(r.[Unit Price]*(100-r.[Line Discount %])/100) as lastprice , 1 as type " +
    		" FROM [2015_Tobis$Sales Shipment Line] r " +
    "  INNER JOIN [2015_Tobis$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [2015_Tobis$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-500 " +
   " ORDER BY  1, 6, 2 ,4";
  
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = conv(rs.getString("klient").trim());

                 String prodID = rs.getString("toode").trim();;

                 double amount = 0;
                 String lastdate = " ";
                 amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    String nimi = rs.getString("nimi");
                    
                    double lastprice = rs.getDouble("lastprice");
                    int type = rs.getInt("type");
                    String typeS = " ";
                    if(type == -1){
                    	typeS = "EXP";
                    }
                    
                  Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,lastprice,0,0," ");
                    cp.setAbiKood(typeS);
                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);
                 sorting.put( custID + nimi + prodID , cp);
           }
         rs.close();
         stmt.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [2015_Tobis$Sales Invoice Line] r " +
		  " INNER JOIN [2015_Tobis$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
       //   System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
		    Connection con2 = getNewConnection2();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = conv(rs2.getString("klient").trim());
               String toode = rs2.getString("toode").trim();
               String kogus = "0";
               String date = " ";
                  kogus = rs2.getDouble("kogus")+"";
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(","));
				  }
                   date = rs2.getString("lastdate").trim().substring(0,10);

               if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
               }
               if(!toode.equals(oldtoode)){
               	counter = 0;
               }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){

                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if (al != null){
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}

                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
                 oldtoode = toode;
               }
               cou++;
             }


  rs2.close();
  stmt2.close();
  con2.close();

     } catch (Exception e){
       e.printStackTrace();
     }
     
     
     
     getCustProdsDirecto(sorting);
     
     
     
     Vector vcc = new Vector();
     TreeMap sorting2 = new TreeMap();
     

     Iterator iterax =sorting.keySet().iterator();
     while (iterax.hasNext()){
    	 String key = iterax.next() + "";
    	 CustProd cp = (CustProd)sorting.get(key);
    	 String asi = "XXX";
    	 if(cp.getAbiKood() != null && cp.getAbiKood().trim().length() > 0){
    			asi =  cp.getAbiKood();
    	 }
    	 sorting2.put(key + asi, cp);
     }
     
     Iterator itera =sorting2.values().iterator();
     while (itera.hasNext()){
    	 vcc.add(itera.next());
     }
   //  System.out.println("vcc suurus " + vcc.size());
     return vcc;
   }

   public void getCustProdsDirecto(TreeMap sorting){
	   //1.teeme klientide arvete nimekirja .
	   
	   File cpdatadir = new File("cpdata");
	   if(!cpdatadir.exists()) {
		   cpdatadir.mkdir();
	   }
	
	
	   Calendar cal = Calendar.getInstance();
	   String d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	   String m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
	   
	   int days = 30 ;
	   if(getParent().getProps().getProperty("sortdays") != null){
		   try{
			   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
		   }catch(Exception e){
		   }
	   }
	   String end = d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
	   cal.add(Calendar.DATE, - days);
	   
	    d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	    m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
		   String start =  d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
		 //  System.out.println(dirCustCodes.get("9535") + " " + start + " uhuu " + end);
		
		   Iterator enums = this.dirCustCodes.keySet().iterator();
	   while(enums.hasNext()){
		String custid = (String)enums.next();
		   try{
			   Converter.doTrustToCertificates();
			   custid = Converter.deCode(custid);
			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
				String httpsURL = logurl  + company + "/xmlcore.asp"; 
		  //     System.out.println(httpsURL);
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        
		        String gS = getParent().getProps().getProperty("keyInvoice");
		   	    if(gS == null) {
		   	    	System.out.println("keyInvoice is missing!");
		   	    	System.exit(0);
		   	    }
		        String urlParameters  = 
						"what=invoice&get=1&orderer=" + cuurl + "&invoicedate1=" + start + "&invoicedate2=" 
		            + end + "&key=" + gS;
		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		           
		     
		        
		        
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
  
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	   
				        
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		
		        	//	
		        		int ind =  line.indexOf("invoice number=") ;
		        		String number = "";
			        	if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		number = number.substring(0,number.indexOf("\""));
			        		// System.out.println(number);
			        	}
			        	 ind =  line.indexOf("date=") ;
			        	if(ind != -1){
			        		String date = line.substring(ind + 6);
			        		date = date.substring(0,date.indexOf("\""));
			        		// 		        		
			        		 if(isOkDate(date)){
			        			
			     					Vector cps = this.getCPCombi(Converter.deCode(custid),number,date);
			     					
			     					if(cps != null){
			     						Enumeration eee = cps.elements();
			     						while( eee.hasMoreElements()){
			     						CustProd cp = (CustProd) eee.nextElement();
			     						if(cp.getCustID().equals(custid) && dirCustCodes.get(custid) != null) {
			     							cp.custID = Converter.enCodeEst(dirCustCodes.get(custid)+"");
			     						}
			     						 if(sorting.get(custid +  cp.getProdID()) == null) {
			     							sorting.put(custid +  cp.getProdID()  , cp);
			     						 }
				     				   }	
			     					}
			     				 }
			        	      }		
		        	      }           	
		              }
		      //  System.out.println(inputLineBuf);
		        in.close();
		        isr.close();
		        ins.close();
		        con.disconnect();
		        
		       
		   } catch (Exception a){
			  // a.printStackTrace();
			  
		   }
		   
	   }
	   System.out.println("cp directo size " + sorting.size());
  }

   public static Vector upside(Vector inVecu){
	   Vector inVec = (Vector)inVecu.clone();
        Vector sorted = new Vector(inVec.size());
        while(inVec.size() > 0){
        	sorted.addElement(inVec.elementAt(inVec.size()-1));
        	inVec.removeElementAt(inVec.size()-1);
        }
        return sorted;
      }


   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "NaviTobisDirecto";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap usedcredit = new TreeMap();
   
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:MERISOFT";
   
   private String url2 = "jdbc:odbc:NAV40";
   private Connection con;
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
 

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

  	 try {
  	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [2015_Tobis$Cust_ Ledger Entry] " +
  	 			"where Month(getdate())=Month([Posting Date]) and " +
             "Year(getdate())=Year([Posting Date])  group by [Salesperson Code]";
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
            
                 String agentID = rs.getString("agent");
                 double sales = rs.getDouble("summa");
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
            }
          }

	        rs.close();
	        stmt.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }
  
  //directo asjad siin 
  
  public String getSaveStockInfoDirecto(HashSet koodid) {

	   StringBuffer products = new StringBuffer();
	   seisud = new HashMap<String, Double>();
     
try{
	 Converter.doTrustToCertificates();
	 
	 
	 

    
    String gS = getParent().getProps().getProperty("keyStocks");
	    if(gS == null) {
	    	System.out.println("keyStocks is missing!");
	    	System.exit(0);
	    }
	   
    String urlParameters  = "get=1&what=stocklevel&key=" + gS;
   String  httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
   URL myurl = new URL(httpsURL);
    
    byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
    int postDataLength = postData.length;
    HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
    con.setRequestProperty("charset", "utf-8");
    con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
    con.setUseCaches(false);
    con.setDoOutput(true);
    try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
       wr.write( postData );
    }
    
   InputStream ins = con.getInputStream();
   InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
   BufferedReader in = new BufferedReader(isr);

   String inputLine = "";
   StringBuffer inputLineBuf = new StringBuffer();
   while ((inputLine = in.readLine()) != null){
   	inputLine = Converter.enCodeEst(inputLine);
   	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\u2013", "").replaceAll("\u2122", "").replaceAll("\u2022", "").replaceAll("\u00B0", "").replaceAll("&quot;", "") );
   }

   in.close();
   
   if(inputLineBuf.length() > 2){
 	  String str = inputLineBuf.toString();
       if(str.startsWith("True")) {
      	 str = str.substring(4);
       }
         try {
       	  File f = new File (getDir() + "laoseis.xml");
       	  if(f.exists())f.delete();
       	 Converter.saveFileInUTF8(getDir() + "laoseis.xml", str);
   	
   	  }catch (Exception ex) {
   		ex.printStackTrace();
   	  }
    }
   
   System.out.println("Laoseisud tehtud" );
   

}catch(Exception e){
	e.printStackTrace();
}




try {
	 

	 File f = new File (getDir() + "laoseis.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
    Document doc = db.parse(in,handler);
    Node root = getNode(doc, "transport");      
 //   System.out.println(root);
	Node read = getNode(root, "stocklevels");
	//System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
    for (int i = 0; i < read.getChildNodes().getLength(); i++){
    	  Node rida = read.getChildNodes().item(i);
    	  
    	 // System.out.println("node name " + rida.getNodeName());
    	  if(rida.getNodeName().equals("stocklevel")){
    //line = reader.readLine();
       double  seisx = 0;
       String ladu = rida.getAttributes().getNamedItem("stock_code").getNodeValue();
       String kogus = rida.getAttributes().getNamedItem("stock_level").getNodeValue();
       String toode = rida.getAttributes().getNamedItem("code").getNodeValue();
    
		if( getParent().getProps().getProperty("stockcode") != null && ladu.equals(getParent().getProps().getProperty("stockcode"))){
			
							seisx = ( Double.parseDouble(kogus)); 
				
		}else{
			seisx = ( Double.parseDouble(kogus)); 
		}
		
		boolean kood = true;
		if(koodid != null && koodid.contains(toode)) {
			kood = false;
		}
		
		if(seisx > 0 && kood) {
			products.append(toode +  "; ;");
			products.append(getParent().getNF().format(seisx) + "; "  +  "\n"); 
			seisud.put(toode, seisx);
		}

      	  }}
         in.close();
         
   	  }catch(Exception tt){
   		tt.printStackTrace();	 
   	  }

//System.out.println("laoseisud " + products.toString());
return products.toString();

}
HashMap<String, Double> seisud = null;

public TreeMap getProdGroups2() throws SQLException {
    HashSet names = new HashSet();
	   String lang = null;
	   TreeMap res = new TreeMap();
	     if(getParent().getProps().getProperty("productgroup.lang") != null){
	      	 lang = getParent().getProps().getProperty("productgroup.lang");
	      	// System.out.println("product.lang="+ lang);
	      }
	   
  	try {
  		  File f = new File (getDir() + "tooted.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
          Node root = getNode(doc, "transport");      
       //   System.out.println(root);
      	Node read = getNode(root, "items");
      //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
          for (int i = 0; i < read.getChildNodes().getLength(); i++){
          	  Node rida = read.getChildNodes().item(i);
          	  try{
          		  if(rida.getNodeName().equals("bi_item")){
               		 String group  = rida.getAttributes().getNamedItem("class").getNodeValue();
               		 try{
               			 String name  = rida.getAttributes().getNamedItem("classname").getNodeValue(); 
                   		 group = Converter.cleanLitLatEst(group);
                   		  name  =  Converter.enCodeEst(name);
                   		  name = name.replaceAll("Turtle Wax", "TW");
                   		  
                   		  if(lang != null) {
                   			  try {
                   			  Node lan=	  rida.getChildNodes().item(0);
                   		     name = Converter.enCodeEst( lan.getAttributes().getNamedItem("content").getNodeValue());
                   		
                   			  }catch(Exception e) {
                   				  
                   			  }
                   			
                   				
                   			
                   		  }
                   		 if(names.contains(name + " 1")) {
                  			  name = name + " 2";
                  		  }
                   		  
                   		  if(names.contains(name)) {
                   			  name = name + " 1";
                   		  }
                   		  res.put(group, name); 
                   		  prGroups.put(group, name); 
                   		// System.out.println("gr name " + name + " " + group);
                   		names.add(name);
               		 }catch(Exception r){
               			 
               		 }
               	 
          		  } 
                  
          	  }catch(Exception e){
          		 // e.printStackTrace();
          		 // System.exit(0);
          	  }
               
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  	
  	
  	
	try {
		  File f = new File (getDir() + "tooted.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport"); 
        if(root != null){
     //   System.out.println(root);
    	Node read = getNode(root, "items");
    //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
       if(read != null){
    	
    	
    	for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	  try{
        		  if(rida.getNodeName().equals("bi_item")){
             		 String group  = rida.getAttributes().getNamedItem("supplieritemcode").getNodeValue();
             		 try{
             			 String name  = rida.getAttributes().getNamedItem("name").getNodeValue(); 
                 		 group = Converter.cleanLitLatEst(group);
                 		  name  =  Converter.enCodeEst(name);
                 		//  prGroups.put(group, name);  
             		 }catch(Exception r){
             			 
             		 }
             	 
        		  } 
                
        	  }catch(Exception e){
        		 // e.printStackTrace();
        		 // System.exit(0);
        	  }
             
        }
    	   
       }
       }
	}catch(Exception e){
		e.printStackTrace();
	}
  	
  	return res;
   }

   private boolean hasSupplier(){
	   boolean res = false;
		try {
			  File f = new File (getDir() + "tooted.xml");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db;
			db = dbf.newDocumentBuilder();      	
	    	FileInputStream in = getCleanInputStream(f);
	    	String handler = null;
	        Document doc = db.parse(in,handler);
	        Node root = getNode(doc, "transport"); 
	        if(root != null){
	     //   System.out.println(root);
	    	Node read = getNode(root, "items");
	    //	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
	       if(read != null){
	    	
	    	
	    	for (int i = 0; i < read.getChildNodes().getLength(); i++){
	        	  Node rida = read.getChildNodes().item(i);
	        	  try{
	        		  if(rida.getNodeName().equals("supplieritemcode")){
	             		 String group  = rida.getAttributes().getNamedItem("code").getNodeValue();
	             			  return true;  
	             	  } 
	                
	        	  }catch(Exception e){
	        		  e.printStackTrace();
	        		 // System.exit(0);
	        	  }
	             
	        }
	    	   
	       }
	       }
		 }catch(Exception e){
  		  e.printStackTrace();
  		 // System.exit(0);
  	  }
	   
	   
	   return res ;
   }
   
   public TreeMap getProducts4(){
	   TreeMap tm = new TreeMap();
	    
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "tooted" + 
	    			   getParent().getProps().getProperty("stockcode1")+ ".xml");
	    	   
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		        	  id = id.substring(0,id.indexOf("\""));
		        	 // System.out.println(id);
		      	 	  		len = line.indexOf("stock_level=");
		             	 	 if(len != -1){
		             	 	     String stock   = line.substring(len + 13);
		             	 	     tm.put(id, Double.parseDouble(stock.substring(0,stock.indexOf("\"")))); 
		            	 	 }
		             	 
		      	  }
	          }	    	 
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     return tm;
	     
	   }
   public TreeMap getProducts3(){
	   TreeMap tm = new TreeMap();
	    
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getParent().getProps().getProperty("stockdir") + "tooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		        	  id = id.substring(0,id.indexOf("\""));
		        	 // System.out.println(id);
		      	 	  	 len = line.indexOf("<stocklevels>");
		          	 	 if(len != -1){
		          	 	     String stock   = line.substring(len + 13); 
		          	 	     //System.out.println(stock.substring(0,stock.indexOf("stocklevels")));
		          	 	     stock = stock.substring(0,stock.indexOf("stocklevels"));
		          	 	 if( getParent().getProps().getProperty("stockcode") != null){
		          	 		 
		          	 		 int ladu = stock.indexOf("stock=\"" + getParent().getProps().getProperty("stockcode") + "\"");
		          	 		// System.out.println(ladu);
		          	 		 if(ladu != -1){
		          	 			 String seis = stock.substring(ladu+5);
		          	 			// System.out.println("seis " + seis);
		          	 					 seis = seis.substring(seis.indexOf("level=") + 7);
		          	 					 seis = seis.substring(0, seis.indexOf("\""));
		          	 					 try{
		          	 					tm.put(id, Double.parseDouble(seis)); 
		          	 					 }catch (Exception e){
		          	 						e.printStackTrace(); 
		          	 					 }
		             		 }
		          	 	 }
		         	         // prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
		         	 	 }else{
		         	 		len = line.indexOf("stock_level_total=");
		             	 	 if(len != -1){
		             	 	     String stock   = line.substring(len + 19);
		             	 	     tm.put(id, Double.parseDouble(stock.substring(0,stock.indexOf("\"")))); 
		            	 	 }
		             	 }
		      	  }
	          }	    	 
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     
	     return tm;
	     
	   }
   
   

   
private TreeMap prGroups = null;
private TreeMap prTypes = null;
private HashSet prodCodes = null;


HashMap prodHash = new HashMap();
  
  public void getProductsDirecto(TreeMap prods, HashSet olemasNavis , HashMap<String, Double> maxdisc){
	  // System.out.println("tooted a");
	   
	   if(seisud == null) {
		    getSaveStockInfoDirecto(null);
	   }
	   
	 
	   try {
		//   stocksfile = getLaoSeisud(null); 
	   }catch(Exception e) {
		   
	   }
	 
	   
	   boolean addShelf = false ;
	   try {
		   getProdGroups2();
	     } catch (SQLException e1) {
		  e1.printStackTrace();
	     }
	   
     HashSet usedGroups = new HashSet();
     
     TreeMap stockLisa = new TreeMap();
     if(getParent().getProps().getProperty("stockcode121") != null ){
    	
    	 stockLisa = this.getProducts4();
    //	System.out.println("stockLisaXXXXX " + stockLisa.size());
     }
     
     if((custProds == null || custProds.size() == 0 ) && !useold){
         prodCodes = new HashSet();
    	getCustomerProductsList2();
     }
  TreeMap stockAlter = new TreeMap();
     if(getParent().getProps().getProperty("stockdir") != null ){
    	 stockAlter = this.getProducts3();
    	//System.out.println("stockalter " + stockAlter.size());
     }

   
     
     
     Vector noGrups = new Vector();
     Vector codeEnds = new Vector();
     Vector noTexts = new Vector();
     Vector yesClasses = new Vector();
     Vector noBrands = new Vector();
     String groupIndent = null;
     String typeIndent = null;
         TreeMap brands = new TreeMap();
         boolean sortBR = false;
          boolean hasSupplier = hasSupplier();
     
     if(getParent().getProps().getProperty("codeends") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("codeends"),",");
     	while (st.hasMoreElements()){
     		codeEnds.addElement(st.nextToken());
     	}
     }
     
     if(getParent().getProps().getProperty("yesclasses") != null){
      	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesclasses"),",");
      	while (st.hasMoreElements()){
      		yesClasses.addElement(st.nextToken());
      	}
      }
     
     
     String fixedurl = null;
     
     if(getParent().getProps().getProperty("fixedurl") != null){
    	 fixedurl = getParent().getProps().getProperty("fixedurl");
      }
     
     boolean proddatafile = false;
     
     if("true".equals(getParent().getProps().getProperty("proddatafile"))){
    	 proddatafile = true;
      }
     
     if(getParent().getProps().getProperty("usedatafortypes") != null){
         typeIndent = getParent().getProps().getProperty("usedatafortypes");
      	prTypes = new TreeMap();
      }
     
     if(getParent().getProps().getProperty("nogroups") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogroups"),",");
     	while (st.hasMoreElements()){
     		noGrups.addElement(st.nextToken());
     	}
     }
     
     
     if(getParent().getProps().getProperty("notexts") != null){
     	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("notexts"),",");
     	while (st.hasMoreElements()){
     		noTexts.addElement(st.nextToken().toLowerCase());
     	}
     }
    
     if(getParent().getProps().getProperty("addShelf") != null){
    	 addShelf = true;
      }
    
     Hashtable prices = null;
     
     if(getParent().getProps().getProperty("usepricelist") != null){
     	String pr = getParent().getProps().getProperty("usepricelist");
     	prices = getPricelists(pr);
     }

     String lang = null;
   
     if(getParent().getProps().getProperty("product.lang") != null){
      	 lang = getParent().getProps().getProperty("product.lang");
      	// System.out.println("product.lang="+ lang);
      }
     String lang2 = null;
     
     if(getParent().getProps().getProperty("product.langdefault") != null){
      	 lang2 = getParent().getProps().getProperty("product.langdefault");
      	// System.out.println("product.lang="+ lang);
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
     
     StringBuffer sb = new StringBuffer();
     try {
  
    	 File f = new File (getDir() + "tooted.xml");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream in = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(in,handler);
        Node root = getNode(doc, "transport");      
   
    	Node read = getNode(root, "items");
        for (int i = 0; i < read.getChildNodes().getLength(); i++){
        	  Node rida = read.getChildNodes().item(i);
        	
        		  if(rida.getNodeName().equals("bi_item")){
        		
       
        		String id =  rida.getAttributes().getNamedItem("code").getNodeValue();  
        		
        	
      	     Product prod = new Product(id);
      	   
      		boolean goes = false;
      		
      		
      		Node dd = rida.getChildNodes().item(0);
      		
      	    
      	    if(proddatafile) {
      	    	try {
       	   String longt =  rida.getAttributes().getNamedItem("description").getNodeValue(); 
       	     longt = longt.substring(0,longt.indexOf("\""));
         	 longt = longt.trim().replaceAll("\\r\\n|\\r|\\n", "|").replaceAll(";", ":"); 
         	 longt = longt.replaceAll("&#x0A:", " ");
         	 longt = longt.replaceAll("&#x0D:", " ");
         	 longt = longt.replaceAll("&#x09:", " ");
         	 longt = longt.replaceAll("   ", " ");
         	 longt = longt.replaceAll("  ", " ");
         	 sb.append(id + ";" + longt + "\n");
      	    	}catch(Exception e) {
      	    		
      	    	}
       	       }
   			
      	  try { 
      	    String url =  rida.getAttributes().getNamedItem("pic_url").getNodeValue(); 
      	 	 if(fixedurl != null) {
          		     prod.setPhotoid("phurl:" +fixedurl + id + ".jpg" ); 
          	 } else {
          		 if(url!=null){
              		 prod.setPhotoid("phurl:" +url); 
              	 } 
          	  }
            }catch(Exception e) {}
      	 
      	    	  
      	 try { 
       	    String clo =  rida.getAttributes().getNamedItem("closed").getNodeValue(); 
       	 	 if(clo.equals("1")) {
       	 		 goes = false;
       	 	 }
             }catch(Exception e) {}
      	 
      	 
      	 try { 
        	    String gr =  rida.getAttributes().getNamedItem("class").getNodeValue(); 
        	    String grn =  rida.getAttributes().getNamedItem("classname").getNodeValue(); 
        	    prod.setDiscgrupp(gr);
        	 	prod.setGroup("fake");
        	 	
              }catch(Exception e) {}
      	  
           	
      			String add = "";
      	 try {
      	    	 String ean =  rida.getAttributes().getNamedItem("barcode").getNodeValue();    
      	    
      	    	ean =  ean.replaceAll(";", ":");
      	    	if(ean.contains("&")) {
      	    		ean = ean.substring(0,ean.indexOf("&"));
      	    	}
      	    	ean =  ean.replaceAll(";", ":");
          	     prod.setEAN(ean);
          	     
          		   try{
          				add = ean.substring(ean.length()-4); 
          	       }catch(Exception e ){
          			   
          		   }
          		 
      	 } catch (Exception e) {
      		 
      	 }
      	 
      
      	 try {
      	  String namec =  rida.getAttributes().getNamedItem("name").getNodeValue(); 
    	  //  System.out.println("prod " + id + " + " + namec);
    	
      	  namec = namec.replace("´", "'");
      	  String addid = "";
      	 if("true".equals(getParent().getProps().getProperty("name.code.front"))){
      		 addid = id;
      	 }
        	  if("true".equals(getParent().getProps().getProperty("name.code.front"))){
     			 prod.setName( ( id + " " + Converter.enCodeEst(namec.replaceAll("²", "x2"))).trim() + " GETZ " + add ); 
     		 }else{
     			 prod.setName(( Converter.enCodeEst(namec.replaceAll("²", "x2")) + " " + id ).trim() + " GETZ " + add ); 
     		 }
        	
        	  if(getParent().getProps().getProperty("vat") != null){
        		  prod.setVat(getParent().getProps().getProperty("vat"));
        	  }
      	    
      	   }catch(Exception e) {
      		   goes = false;
      	   }
      	     prod.setPrice(1);
      	     
      	     
   	    	 try{
      		 String pri =  rida.getAttributes().getNamedItem("price").getNodeValue(); 
      	 	      	    prod.setPrice(Double.parseDouble(pri) ); 
 	    	 }catch (Exception e){
 	    
 	    	 }
      	   	
    	   if("true".equals(getParent().getProps().getProperty("stocktozero"))){
      	 		prod.setStock(0);
           	 }
      	 	 
      	 	
      	  try{
      		  
      		 for (int r = 0; r < rida.getChildNodes().getLength(); r++){
           	  Node rid = rida.getChildNodes().item(r);
      		 
       		 if(rid.getNodeName().equals("stocklevels")) {
       			 Node sto = this.getNode(rid, "stocklevel");
       		
       		//System.out.println("Stock " + rid.getNodeName() + " " + rid.getAttributes().getLength());
      	
      	 	 if( getParent().getProps().getProperty("stockcode") != null && sto != null && 
      	 		sto.getAttributes().getNamedItem("stock").getNodeValue().equals(getParent().getProps().getProperty("stockcode"))){
      	 		 
      	 		// System.out.println("Stock " + sto.getAttributes().getNamedItem("level").getNodeValue());
      	 		double stoo = Double.parseDouble(sto.getAttributes().getNamedItem("level").getNodeValue());
      	 		prod.setStock(stoo);
      	 		
      	 	 }
      		 }
       		
    			
  	    		 if(rid.getNodeName().equals("datafields")){
    				
    				 for (int re = 0; re < rid.getChildNodes().getLength(); re++){
    					 Node nn2 = rid.getChildNodes().item(re);
    					 
    					if("data".equals(nn2.getNodeName())) {
    						
    						//System.out.println("nodes " + nn2.getAttributes().getNamedItem("code").getNodeValue());
    					
    					if("WEB_ART".equals(nn2.getAttributes().getNamedItem("code").getNodeValue())) {
    						
							if("Rodyti B2B".equals(nn2.getAttributes().getNamedItem("content").getNodeValue())) {
								goes = true;
							//			System.out.println("On Fix " + customer.getName());
							}
        					
    					}
    				   }
    					/*if(prod.getID().equals("MOT090513OD")) {
    			      		System.out.println("MOT090513OD 0000" + goes );
    			      	}*/	
    					
if("BRAND".equals(nn2.getAttributes().getNamedItem("code").getNodeValue())) {
    						String grupp3 = nn2.getAttributes().getNamedItem("content").getNodeValue();
                           if(this.cateFullGroup.get(grupp3) != null) {
                        	   String g1 = cateFullGroup.get(grupp3);
                        	   
                           	if(maxdisc != null && maxdisc.get(g1) != null){
                              prod.setMaxDiscount(maxdisc.get(g1));
                              }

                           	if(maxdisc != null && maxdisc.get(grupp3) != null){
                              prod.setMaxDiscount(maxdisc.get(grupp3));
                              }
                        	   prod.setGroup(g1);
                        	   prod.setGroupSecond(g1+grupp3);
                           }
    					}
    				 }
	         		
	         		 }
      		 
      		 
      		 }
     	         // prod.setStock((int)Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
                   }catch(Exception e) {
                	   e.printStackTrace();
      		
      	      }
     if(seisud != null && seisud.get(prod.getID()) != null) {
    	 prod.setStock(seisud.get(prod.getID()));
     }
      	 
 /*    if(prod.getID().equals("MOT090513OD")) {
   		System.out.println("MOT090513OD 000" + goes );
   	}*/
      	if(prodCodes != null && !prodCodes.contains(prod.getID())){
      	  goes = false;
      	  if(prod.getStock() > 0 || prod.getStock() < -1){
      		  goes = true;
      	  }
      	}
      	
      	
      	/*if(prod.getID().equals("MOT090513OD")) {
      		System.out.println("MOT090513OD 00" + goes );
      	}*/
      	
      	try {
      	 String stop =  rida.getAttributes().getNamedItem("ART_STOPPED").getNodeValue();
      	if(stop.equals("Yes")){
      		goes = false;
      	}
      	stop =  rida.getAttributes().getNamedItem("E_AGENT").getNodeValue();
      	if(stop.equals("N")){
      		goes = false;
      	}
      	}catch(Exception e) {
      		
      	}
      	/*
      	if(prod.getID().equals("MOT090513OD")) {
      		System.out.println("MOT090513OD 0" + goes );
      	}*/
      	
      	if(olemasNavis.contains(prod.getID())) {
      		goes = false; 
      	}

      	
      	/*if(prod.getID().equals("MOT090513OD")) {
      		System.out.println("MOT090513OD " + goes );
      	}*/
	    	prodHash.put(prod.getID(), prod);
      	    if(goes ){
      	    	prod.setType("GETZ");
      	    	if(!usedGroups.contains(prod.getGroup())){
      	    		usedGroups.add(prod.getGroup());
      	    	}
      	    	
      	    	if(!usedGroups.contains(prod.getGroupSecond())){
      	    		usedGroups.add(prod.getGroupSecond());
      	    	}
      	    	
      	         prods.put(prod.getName(), prod);	
      	        }
      	      }
        }
     }catch(Exception e) {
    	 e.printStackTrace();
     }
   
 
     //teeme puhtaks grupi mapi 
    
    
    
       if(sb.length() > 0) {
    	   Converter.saveFileInUTF8("texts.txt", sb);
       }
       
       System.out.println("directo tooted tehtud ");
     
   }
   
   
   private Vector custProds = null;
   
  public Vector getCustomerProductsList2(){
	   //1.teeme klientide arvete nimekirja .
	   
	   File cpdatadir = new File("cpdata");
	   if(!cpdatadir.exists()) {
		   cpdatadir.mkdir();
	   }
	    custProds = new Vector();
	
	   Calendar cal = Calendar.getInstance();
	   String d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	   String m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
	   
	   int days = 30 ;
	   if(getParent().getProps().getProperty("sortdays") != null){
		   try{
			   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
		   }catch(Exception e){
		   }
	   }
	   String end = d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
	   cal.add(Calendar.DATE, - days);
	   
	    d = cal.get(Calendar.DAY_OF_MONTH)+ "";
	   if(d.length() == 1){
		   d = "0" + d;
	   }
	    m = (cal.get(Calendar.MONTH) +1)+ "";
	   if(m.length() == 1){
		   m = "0" + m;
	   }
		   String start =  d + "-" +m + "-" + cal.get(Calendar.YEAR)+ "";
		//   System.out.println(start + " uhuu " + end);
		   
		   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		   try{
			   Converter.doTrustToCertificates();
			   custid = Converter.deCode(custid);
			   String cuurl =  URLEncoder.encode(custid, "UTF-8");
				String httpsURL = logurl  + company + "/xmlcore.asp"; 
				if(custid.equals("9535")) {
		       System.out.println(httpsURL);
				}
				URL myurl = new URL(httpsURL);
		        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		        
		        String gS = getParent().getProps().getProperty("keyInvoice");
		   	    if(gS == null) {
		   	    	System.out.println("keyInvoice is missing!");
		   	    	System.exit(0);
		   	    }
		        String urlParameters  = 
						"what=invoice&get=1&orderer=" + cuurl + "&invoicedate1=" + start + "&invoicedate2=" 
		            + end + "&key=" + gS;
		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		           
		        
		        
		        
		        
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ).replaceAll("\"l\"","\"L\""));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        	//	System.out.println(" line " + line);
		        		int ind =  line.indexOf("invoice number=") ;
		        		String number = "";
			        	if(ind != -1){
			        		 number = line.substring(ind + 16);
			        		number = number.substring(0,number.indexOf("\""));
			        		// System.out.println(number);
			        	}
			        	 ind =  line.indexOf("date=") ;
			        	if(ind != -1){
			        		String date = line.substring(ind + 6);
			        		date = date.substring(0,date.indexOf("\""));
			        		// System.out.println(date + " " + number + " " + custid);	
				        		
			        		 if(isOkDate(date)){
			        			// System.out.println(date + "  222 " + number + " " + custid);	
					        		
			     					Vector cps = this.getCP(Converter.deCode(custid),number,date);
			     					if(cps != null){
			     						Enumeration eee = cps.elements();
			     						while( eee.hasMoreElements()){
				     						custProds.add(eee.nextElement());
				     					//	System.out.println(date + " " + number + " " + custid);
			     						}			     				
					        			 	
			     					}
			     				}
			        	  }		
		        	}           	
		        }
		      //  System.out.println(inputLineBuf);
		        in.close();
		        isr.close();
		        ins.close();
		        con.disconnect();
		        
		       
		   } catch (Exception a){
			  // a.printStackTrace();
			  
		   }
		   
	   }
	   return custProds;
  }


  
  private Vector custtsUsed = new Vector();
  private Vector custDebtUsed = new Vector();
  
  private Vector getCP(String custid , String number, String date ){

	   Vector result = new Vector();
	   try{
		   File cpdata = new File("cpdata/" + number+".vec");
		   if(cpdata.exists()) {
			   Vector vec = (Vector)Converter.readObject("cpdata/" + number+".vec");
			   for(int i = 0; i < vec.size(); i++) {
				   CustProd cp = (CustProd)vec.elementAt(i);
				   if(!prodCodes.contains(cp.getProdID())){
				        prodCodes.add(cp.getProdID());	
			        }
			   }
			   return vec ;
		   }else {
			   
		 
			String httpsURL = logurl  + company + "/xmlcore.asp";
			
			
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
		        		   String gS = getParent().getProps().getProperty("keyInvRow");
		  	    if(gS == null) {
		  	    	System.out.println("keyInvoice is missing!");
		  	    	System.exit(0);
		  	    }
		        String urlParameters  = 
						"what=invoice_row&get=1&number=" + number + "&key=" + gS;
		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		        
		        InputStream ins = con.getInputStream();
		        
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine =Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		int ind =  line.indexOf("item=") ;
		        		String toode = "";
			        	if(ind != -1){
			        		toode = line.substring(ind + 6);
			        		toode = toode.substring(0,toode.indexOf("\""));
			        	
			        	}
		        
			        	
			        	ind =  line.indexOf("quantity=") ;
		        		double kogus = 0;
		        		String kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 10);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		 kogus = Double.parseDouble(kkk);
			        	}
			        	
			        	ind =  line.indexOf("total=") ;
			        	double hind = 0;
		        		 kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 7);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		
			        		 hind = Double.parseDouble(kkk)/kogus;
			        		// System.out.println("toode "  + line + " "+ toode + " " + hind);
			        	}
			        	
		        if(toode != null && toode.trim().length() > 0){
			        result.add(new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," "));
			        if(!prodCodes.contains(toode)){
				        prodCodes.add(toode);	
			        }
		        }
		         }
		        	
		        }
		        in.close();
		      //  System.out.println("buffer " + result);
		        Converter.saveObject(result, "cpdata/" + number + ".vec");
		   }
		   
		   
		   
		   
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	   return result;
  }


  private Vector getCPCombi(String custid , String number, String date ){
	 if( prodCodes == null ) {
		 prodCodes = new HashSet();
	 }
		 
	   Vector result = new Vector();
	   try{
		   File cpdata = new File("cpdata/" + number+".vec");
		   if(cpdata.exists()) {
			   Vector vec = (Vector)Converter.readObject("cpdata/" + number+".vec");
			   for(int i = 0; i < vec.size(); i++) {
				   CustProd cp = (CustProd)vec.elementAt(i);
				    if(!prodCodes.contains(cp.getProdID())){
				        prodCodes.add(cp.getProdID());	
			        }
			   }
			   return vec ;
		   }else {
			   
		 
			String httpsURL = logurl  + company + "/xmlcore.asp";
			
			
		       // System.out.println("httpsURL " + httpsURL);
				URL myurl = new URL(httpsURL);
		        		   String gS = getParent().getProps().getProperty("keyInvRow");
		  	    if(gS == null) {
		  	    	System.out.println("keyInvoice is missing!");
		  	    	System.exit(0);
		  	    }
		        String urlParameters  = 
						"what=invoice_row&get=1&number=" + number + "&key=" + gS;
		           httpsURL = logurl  + company + "/" + comphelp + "/xmlcore.asp"; //?get=1&what=stocklevel
		           myurl = new URL(httpsURL);
		           
		           byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
		           int postDataLength = postData.length;
		           HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		           con.setRequestMethod("POST");
		           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		           con.setRequestProperty("charset", "utf-8");
		           con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
		           con.setUseCaches(false);
		           con.setDoOutput(true);
		           try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		              wr.write( postData );
		           }
		        
		        InputStream ins = con.getInputStream();
		        
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);
		        
		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine =Converter.enCodeEst(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        	StringTokenizer st = new StringTokenizer(inputLineBuf.toString(),"\n");
		        	while (st.hasMoreTokens()){
		        		
		        		String line = st.nextToken();
		        		int ind =  line.indexOf("item=") ;
		        		String toode = "";
			        	if(ind != -1){
			        		toode = line.substring(ind + 6);
			        		toode = toode.substring(0,toode.indexOf("\""));
			        	
			        	}
		        
			        	
			        	ind =  line.indexOf("quantity=") ;
		        		double kogus = 0;
		        		String kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 10);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		 kogus = Double.parseDouble(kkk);
			        	}
			        	
			        	ind =  line.indexOf("total=") ;
			        	double hind = 0;
		        		 kkk = "";
			        	if(ind != -1){
			        		kkk = line.substring(ind + 7);
			        		kkk = kkk.substring(0,kkk.indexOf("\""));
			        		
			        		 hind = Double.parseDouble(kkk)/kogus;
			        		// System.out.println("toode "  + line + " "+ toode + " " + hind);
			        	}
			        	
		        if(toode != null && toode.trim().length() > 0){
			        result.add(new CustProd(Converter.enCodeEst(dirCustCodes.get(custid)+""), toode, kogus ,0 ,0,date.substring(5,10),hind,0,0," "));

			        if(!prodCodes.contains(toode)){
				        prodCodes.add(toode);	
			        }
		        }
		         }
		        	
		        }
		        in.close();
		        Converter.saveObject(result, "cpdata/" + number + ".vec");
		   }
		   
		   
		   
		   
	   }catch(Exception e){
		   e.printStackTrace();
		   return null;
	   }
	   return result;
 }

  private boolean isOkDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(5, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(8, 10)));
	  // System.out.println(mmddyy + " " +cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		 int days = 30 ;
		   if(getParent().getProps().getProperty("sortdays") != null){
			   try{
				   days = Integer.parseInt(getParent().getProps().getProperty("sortdays"));
			   }catch(Exception e){
			   }
		   }
		
		ca.add(Calendar.DAY_OF_YEAR, -days);
		
		if(cal.after(ca)){
			return true;
		}else{
			return false;
		}
		
	  }
  
  private Hashtable getPricelists(String code ){
	   Hashtable re = new Hashtable();
	   try {
			  File f = new File (getDir() + "hinnakiri.xml");
			//  System.out.println("siin matix");
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
	                  String start = " ";
	                  String end = " ";
	                 try{
	                 	 try{
	                 	     clGroup = rida.getAttributes().getNamedItem("code").getNodeValue();
	                        }catch(Exception aa){}
	                 	 try{
		                         prodID =  rida.getAttributes().getNamedItem("item").getNodeValue();  
	                 	   }catch(Exception aa){ }
	                 	 try{
	                          prodGroup =  rida.getAttributes().getNamedItem("class").getNodeValue();
	                 	  }catch(Exception aa){ }
	                 	  try{
		                             start =  rida.getAttributes().getNamedItem("start_date").getNodeValue();
		                    	  }catch(Exception aa){ }
		                    	  try{
			                             end =  rida.getAttributes().getNamedItem("end_date").getNodeValue();
			                    	  } catch(Exception aa){ }
	                         	                 
	                 } catch(Exception e){
	                 	e.printStackTrace();
	                 }
	                 
	                }
	             	
	        	  }catch(Exception e){
	        		  e.printStackTrace();
	        	  }
	             
	        }
	        
		}catch(Exception e){
			e.printStackTrace();
		}
	   
	   return re;
  }

  
}
