package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
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

public class ErplyOstDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public ErplyOstDBDriver(EksportPanel parent) {
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
   if(apiversion == null){
	   apiversion = "1.0";
   }
   returntype = getParent().getProps().getProperty("erply.returntype");
   if(returntype == null){
	   returntype = "XML";
   }
    userstuff = "&version=" + apiversion + "&responseType=" + returntype + "&username=" + username 
      + "&password=" + password;
   
   session = getSessionId();
	System.out.println(session);
   while(session.indexOf("%") != -1){
	   session = getSessionId();
   }
    userstuff = "&clientCode=" + company +"&version=" + apiversion + "&responseType=" + returntype + "&sessionKey=" + session;
    System.out.println(session + userstuff ) ;
   
  }
    
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







  

  
  private String getSessionId(){

	  String data = "clientCode=" + company + "&request=verifyUser" + userstuff ;
	//  System.out.println(data);
	  try {
	String res = (postData(data)).toString();
	System.out.println(res);
	  Converter.doTrustToCertificates();
		
	session = res.substring(res.indexOf("sessionKey") + 11 ,res.indexOf("</sessionKey"));
	//System.out.println(session);
	} catch (Exception e) {
		e.printStackTrace();
	}
	 
	  return session;
  }
  
  
  private StringBuffer postData(String data) throws Exception {
  HttpURLConnection urlc = null;
  StringBuffer inputLineBuf = new StringBuffer();
  try{
	  Converter.doTrustToCertificates();
		
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
    	inputLineBuf.append(inputLine.replaceAll("/>","/>\n").replace((char)0x1c, ' '));
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
     	Converter.doTrustToCertificates();
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

	return lines;

   }


 
  
   private HashMap prodHash = new HashMap();
   


  
   Hashtable prodCodes = null;
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		return result;

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

   public TreeMap getProdGroups()  {
     TreeMap   tm = new TreeMap();
     groupList = new TreeMap();
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
               		 group = deCode(group);
               		  name  =  deCode(name);
                        tm.put(group,Converter.enCodeEst(name));  
                        groupList.put(group, group);
                    	Node alamad = getNode(rida, "subGroups");
                    	if(alamad != null){
                            for (int u = 0; u < alamad.getChildNodes().getLength(); u++){
                           	  Node alam = alamad.getChildNodes().item(u);
                           	 if(alam.getNodeName().equals("item")){
                           		 String agroup  = getNode(alam,"productGroupID").getFirstChild().getNodeValue();
                           		  String aname  =  getNode(alam,"name").getFirstChild().getNodeValue();
                           		 agroup = deCode(agroup);
                           		  aname  =  deCode(aname);
                                    tm.put(group + "." + agroup, aname); 
                                    groupList.put(agroup, group + "." + agroup);
                                    
                                    
                                    Node alamad2 = getNode(alam, "subGroups");
                                    // 	System.out.println("ridu  " + read.getChildNodes().getLength() + read.toString());
                                	if(alamad2 != null){
                                        for (int w = 0; w < alamad2.getChildNodes().getLength(); w++){
                                       	  Node alam2 = alamad2.getChildNodes().item(w);
                                       	 if(alam2.getNodeName().equals("item")){
                                       		String a2group  = getNode(alam2,"productGroupID").getFirstChild().getNodeValue();
                                     		  String a2name  =  getNode(alam2,"name").getFirstChild().getNodeValue();
                                     		 a2group = deCode(a2group);
                                     		  a2name  =  deCode(a2name);
                                              tm.put(group + "." + agroup + "." + a2group, Converter.enCodeEst(a2name)); 
                                              groupList.put(a2group, group + "." + agroup + "." + a2group);
                                            } 
                                      	 } 
                                  	 }
                                 }
                            }	
                    	}
                             	
                  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 // System.exit(0);
          	  }
           	  }     
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  
  	System.out.println( "grlist " + groupList.toString());
 	System.out.println("tm " + tm.toString());
         return tm;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
        }



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
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
  
agents = new TreeMap();
   //  makeData();
   //  getCustomerProductsList(null);
//this.getFixedAssortmentData();
getProdGroups();
     StringBuffer inputLineBuf = new StringBuffer();
	try {
		inputLineBuf = postData("request=getEmployees&recordsOnPage=100" + userstuff);
	} catch (Exception e1) {
		e1.printStackTrace();
	}
     if(inputLineBuf.length() > 2){
         
         try {
       	  File f = new File (getDir() + "tootajad.xml");
       	  if(f.exists())f.delete();
   		PrintWriter os = new PrintWriter(new FileWriter(
   				getDir() + "tootajad.xml", true));
   		os.write(inputLineBuf.toString());
   		os.close();
   	  }catch (IOException ex) {
   		ex.printStackTrace();
   	  }
    }
   
     // loene v�imalikud agendid sisse 
   
 	try {
 		  File f = new File (getDir() + "tootajad.xml");
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
             		
             		 Agent sm = new Agent (salesman);
             		 String name  = deCode(getNode(rida, "employeeName").getFirstChild().getNodeValue());        		
             		 sm.setName(name);
                     tm.put(name, sm); 
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
   
   private void makeData(){
	   

	     
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
           
           //l�peb tootegruppidega tegelemine 
          // System.out.println("Tootegrupid tehtud" );
           System.out.println("Teen Dokyumendi listi" );   
           
           FileListFilter fil = new FileListFilter("doklist","xml");
	    	 String[] ll = new File (getDir()).list(fil);
	    	 for (int i = 0; i < ll.length; i++) {
	    		 String name = ll[i];
		    		// System.out.println(name);
		 		  File f = new File (getDir() + name);
	           if(f.exists()){
	        	   f.delete();
	           }
	    	 }
	    		
	    		
	    
           
         String  data = "request=getSalesDocuments&recordsOnPage=100&dateFrom=" + getMonth() + "&type=INVWAYBILL"
				+ userstuff;// &dateFrom=2010-12-01
		// System.out.println(data);

		inputLineBuf = postData(data);
		// System.out.println(inputLineBuf.toString());

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
				File f = new File(getDir() + "doklist1.xml");
				if (f.exists())
					f.delete();
				 String c = (char)0x19 + "";
				 Converter.saveFileInUTF8(getDir() + "doklist1.xml", inputLineBuf.toString().replace((char)0x19, ' ')
						 .replace((char)0x19, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' '));
		       
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (tsykkel > 100) {
			int counter = 2;
			for (; tsykkel > 100; tsykkel = tsykkel - 100) {
				try {
					inputLineBuf = postData("request=getSalesDocuments&recordsOnPage=100&type=INVWAYBILL&dateFrom=" + getMonth() + "&pageNo="
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
								 .replace((char)0x19, ' ').replace((char)0x19, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' ').replace((char)0x1c, ' '));
					
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				counter++;
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
          
           
           
           
           System.out.println("Algab toodete moodustamine" );
            tsykkel = 0;
           
           inputLineBuf = new StringBuffer();
           boolean cut = true;
           if("true".equals(getParent().getProps().getProperty("nocut"))){        	   
        	   cut = false;
           }
          String laoID = "2";
          if(getParent().getProps().getProperty("laoid") != null){        	   
       	   laoID = getParent().getProps().getProperty("laoid");
          }
        	
       	try {
    		inputLineBuf = postData("request=getProducts&recordsOnPage=100&getPackageInfo=1&getStockInfo=1&warehouseID="+ laoID + userstuff); //&getStockInfo=1
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
         		 Converter.saveFileInUTF8(getDir() + "tootederp1.xml", inputLineBuf.toString());
         	  }catch (Exception ex) {
         		ex.printStackTrace();
         	  }
          }
           
           if(tsykkel > 100 ){
        	  int counter = 2;
        	   for(;tsykkel > 100;tsykkel = tsykkel-100){
        		   try {
        	    		inputLineBuf = postData("request=getProducts&recordsOnPage=100&getPackageInfo=1&getStockInfo=1&warehouseID=" + laoID + "&pageNo=" + counter + userstuff); //&getStockInfo=1
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
        	 //  System.out.println("request=getCustomers&recordsOnPage=100&responseMode=detail" + userstuff);
       	
       		inputLineBuf = getData("request=getSuppliers&recordsOnPage=100&responseMode=detail" + userstuff);
       	
       
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
    	          	
    	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp1.xml", inputLineBuf.toString());
         	            
    	          	  }catch (Exception ex) {
    	          		ex.printStackTrace();
    	          	  }
    	            }
    	    	 
       		 }
        		
       		 
       	 }catch(Exception e){
       		 
       	 }
       	 //System.out.println("Klientide tsykkel " + tsykkel);
       	 
        }
        
         
           
           if(tsykkel > 100 ){
         	  int counter = 2;
         	   for(;tsykkel > 100;tsykkel = tsykkel-100){
         		  try {
         			//  System.out.println("request=getCustomers&recordsOnPage=100&responseMode=detail&pageNo=" + counter + userstuff);
         	       		inputLineBuf = getData("request=getSuppliers&recordsOnPage=100&responseMode=detail&pageNo=" + counter + userstuff);
         	       	   } catch (Exception e1) {
         	       		e1.printStackTrace();
         	       	   }
         	        
         	    	if(inputLineBuf.length() > 2){        
         	               try {
         	             	  File f = new File (getDir() + "kliendiderp" + counter + ".xml");
         	             	  if(f.exists())f.delete();
         	         	
       	          		 Converter.saveFileInUTF8(getDir() + "kliendiderp" + counter + ".xml", inputLineBuf.toString());
            	        
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


   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	    makeData();
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
	         			
	         		  String id =  deCode(getNode(rida,"supplierID").getFirstChild().getNodeValue());
	         		  Customer customer = new Customer(id);
	         		
	         		  try{
	         		  String namec =   deCode(getNode(rida,"fullName").getFirstChild().getNodeValue());
	         		  customer.setName(Converter.enCodeEst(namec));
                         }catch(Exception ee){
	                     customer.setName(id); 
                         }
                      
                         
	         		  try{ 
	 	         		 String aadr =   getNode(rida,"address").getFirstChild().getNodeValue();
	 	         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(deCode(aadr)),"Estonia"));
	         		 }catch(Exception ee){
		         		 customer.setAddress(""); 
	         		 }
	         		  try{
	         		   String salesman  = deCode( getNode(rida,"customerManagerID").getFirstChild().getNodeValue());
       		            customer.addAgent(new Agent(salesman));
       		            this.custtsUsed.add(id);
	         		   }catch(Exception ee){
		         		 
	         		   }
	         		  customer.addAgent(new Agent("HOUSE"));
	         		     tm.put(customer.getName(), customer);
	                     this.customers.add(customer.getID());
	                     
	         		 }
	         	  }catch(Exception e){
	         		  e.printStackTrace();
	         	  }
	         
	    	  }    
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	    
     return tm;
   }

 

 
   
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
       prodCodes = new Hashtable();
    Vector noGrups = new Vector();
        prodHash = new HashMap();
    
    if(getParent().getProps().getProperty("nogrups") != null){
    	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("nogrups"),",");
    	while (st.hasMoreElements()){
    		noGrups.addElement(st.nextToken());
    	}
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
              	
              		 try{
              		 String namec  =  (getNode(rida,"name").getFirstChild().getNodeValue());
              		 prod.setName(Converter.enCodeEst(namec) + " " + kood + " " + code);
                      }catch(Exception r){
              			 r.printStackTrace();
              			 prod.setName("noname " + code);
              		 }
              		 try{
              			String ean  =  getNode(rida,"code2").getFirstChild().getNodeValue();
                  		if(ean != null){
                  			prod.setEAN(ean);	
                  		}	 
              		 }catch(Exception r){
              			 
              		 }
              		
              		
              		String price  =  getNode(rida,"price").getFirstChild().getNodeValue();
              		if(price != null){
              			prod.setPrice(Double.parseDouble(price));	
              		}
              		
              		String vat  =  getNode(rida,"vatrate").getFirstChild().getNodeValue();
              		if(vat != null){
              			prod.setVat(vat);	
              		}
              		
              		
              		String group  =  getNode(rida,"groupID").getFirstChild().getNodeValue();
              		if(group != null){
              			String ghh = (String)groupList.get(group);
              			//System.out.println("ghh " + ghh );
              			if(ghh != null){
              				prod.setGroup(ghh);	
                  			if(noGrups.contains(group)){
                  				goes = false;
                  			}	
              			}
              			
              		}
              		
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
             		  
                  
         		 if(goes){
         		
         			  tm.put(prod.getName() , prod); 
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

   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();

			   return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "ErplyOst";
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
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap groupList = new TreeMap();
   private Vector custtsUsed = new Vector();
   private String session = "";
   private String username = null;
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

@Override
public Vector getDebt(String agentCode) throws SQLException {
	// TODO Auto-generated method stub
	return null;
}
}
