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
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviEppDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public NaviEppDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }
  public String getSaveDebtInfo() {
   	return null;
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
   	/*
   	try {
    FileReader fr = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader = new BufferedReader(fr);
  String line = null;

  while(!(line = reader.readLine()).equals("\"[Get DiscountMatrix]\"")){
  }
  line = reader.readLine();
    System.out.println("line" +line);
  while(((line = reader.readLine()).trim().length()) > 3 ){
  	
    line = line.replaceAll("\",\"", "|");
    line = line.replaceAll("\",", "|");
    line = line.replaceAll("\""," ");
 
    StringTokenizer st = new StringTokenizer(line, SEPARATOR);
    double price = 0;
	String custID = null;
	String clGroup = null;
	String prodID = null;
	String discGroup = null;
	double discount = 0;
		if(st.hasMoreTokens()){
			custID = cleardots(st.nextToken());
			
		}
				if(st.hasMoreTokens()){
		 clGroup = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				 prodID = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				discGroup = cleardots(st.nextToken());	
			}
			
			if(st.hasMoreTokens()){
				 discount = Double.parseDouble(cleardots(st.nextToken()).replaceAll(",","."));	
			}
			
		//	System.out.println("'" + custID + "'" + clGroup + "'" + prodID + "'" + discGroup + "'" + discount) ;
			if(st.hasMoreTokens()){
				price = Double.parseDouble(cleardots(st.nextToken()));	
			}
			MatrixLine mline = new MatrixLine();

			if (custID != null && custID.trim().length() > 0) {
				mline.setCustID(custID.trim());
			}
			if (mline.getCustID() == null && clGroup != null
					&& clGroup.trim().length() > 0) {
				mline.setCliGroup(clGroup.trim());
			}

			if (prodID != null && prodID.trim().length() > 0) {
				mline.setProdID(prodID.trim());
			}
			if (mline.getProdID() == null && discGroup != null
					&& discGroup.trim().length() > 0) {
				mline.setDiscGroup(discGroup);
			}
			mline.setDiscount(discount);

			if (price != 0) {
				mline.setPrice(price);
			}
			lines.add(mline);
		}

	  

	} catch (Exception ex) {
		ex.printStackTrace();
	}*/
	return lines;

   }

   public Vector getFixedAssortmentData() {
       return fixedLines ;
 }
   
   
   public Vector getFixedAssortmentData1() {
       Vector lines = new Vector();
       TreeMap prods = this.getProducts2();
       System.out.println("prods " + prods.size());
       
       try {
   		  File f = new File (getDir() + "navihinnakiri.xml");
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

                        
                         if(prods.keySet().contains(prodID) && pricelists.contains(clGroup.trim())){
                        	
                        	 
                        	if(!prodsInUse.contains(prodID.trim())){
                           	  this.prodsInUse.add(prodID.trim());	
                        	}
                        	 
                            lines.add(mline);	
                          }
                    
                       
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
       System.out.println(prodsInUse.size() );
       
       return sortLines(lines,prods);
 }
   
   private Vector sortLines(Vector inVec,TreeMap products){
	        Vector vec = new Vector();
	        System.out.println("Prod22 " + products.size());
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

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
     this.setTreeGroup(true);
  	try {
  		  File f = new File (getDir() + "navitootegrupid.xml");
  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	     DocumentBuilder db;
  		db = dbf.newDocumentBuilder();      	
      	FileInputStream in = getCleanInputStream(f);
      	String handler = null;
          Document doc = db.parse(in,handler);
          Node root = getNode(doc, "ProductGroupList");      
       //   System.out.println(root);
      	
          for (int i = 0; i < root.getChildNodes().getLength(); i++){
          	  Node rida = root.getChildNodes().item(i);
          	  try{
          	
          		if (rida.getNodeName().equals("ProductGroup")){
          		//	System.out.println("ridu  " + root.getChildNodes().getLength() + rida.getNodeName());
          		 String group =  getNode(rida,"Code").getFirstChild().getNodeValue();
          		try{

             		 String name =  getNode(rida,"Description").getFirstChild().getNodeValue();
	         	
          	   		 group = deCode(group);
              		  name  =  deCode(name);
              		  if(group.endsWith("00")  && !group.startsWith("6") && !group.startsWith("7") && !group.startsWith("8")){
              			  group = group.substring(0,1);
                          tm.put(group, name);  
              		  }         		
                     }catch(Exception e){
	         			  
	         		  }
                    
                 	
          		}
            	  
          	  }catch(Exception e){
          		  e.printStackTrace();
          		 // System.exit(0);
          	  }
               
          }
          
  	}catch(Exception e){
  		e.printStackTrace();
  	}
         return tm;
   }
   
   public TreeMap getProdGroups3() throws SQLException {
	     TreeMap   tm = new TreeMap();
	     this.setTreeGroup(true);
	  	try {
	  		  File f = new File (getDir() + "navitootegrupid.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "ProductGroupList");      
	       //   System.out.println(root);
	      	
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	          	  try{
	          	
	          		if (rida.getNodeName().equals("ProductGroup")){
	          		//	System.out.println("ridu  " + root.getChildNodes().getLength() + rida.getNodeName());
	          		 String group =  getNode(rida,"Code").getFirstChild().getNodeValue();
	          		try{

	             		 String name =  getNode(rida,"Description").getFirstChild().getNodeValue();
		         	
	          	   		 group = deCode(group);
	              		  name  =  deCode(name);
	              		
	              		 //tm.put("521", "521");
	              		  if(group.endsWith("109") || group.startsWith("205") || group.startsWith("305")){
	                        //  tm.put(group, name);  
	              		  } 
	              		  if(!group.endsWith("00")  && !group.startsWith("7")&& !group.startsWith("8")){
	                          tm.put(group, name);  
	              		  }         		
	                     }catch(Exception e){
		         			  
		         		  }
	                    
	                 	
	          		}
	            	  
	          	  }catch(Exception e){
	          		  e.printStackTrace();
	          		 // System.exit(0);
	          	  }
	               
	          }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	         return tm;
	   }
   

   public TreeMap getProdGroupsSecond() throws SQLException {
	   TreeMap   tm = new TreeMap();
	   
	  	try {
	  		  File f = new File (getDir() + "navitootegrupid.xml");
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "ProductGroupList");      
	       //   System.out.println(root);
	      	
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	          	  try{
	          	
	          		if (rida.getNodeName().equals("ProductGroup")){
	          		//	System.out.println("ridu  " + root.getChildNodes().getLength() + rida.getNodeName());
	          		 String group =  getNode(rida,"Code").getFirstChild().getNodeValue();
	          		try{

	             		 String name =  getNode(rida,"Description").getFirstChild().getNodeValue();
		         	
	          	   		 group = deCode(group);
	              		  name  =  deCode(name);
	              		  if(!group.endsWith("00")  && !group.startsWith("6") && !group.startsWith("7") && !group.startsWith("8")){
	              			  String add  = group.substring(0,1);
	              			  group = add + group;
	                          tm.put(group, name);  
	              		  }         		
	                     }catch(Exception e){
		         			  
		         		  }
	                    
	                 	
	          		}
	            	  
	          	  }catch(Exception e){
	          		  e.printStackTrace();
	          		 // System.exit(0);
	          	  }
	               
	          }
	          
	  	}catch(Exception e){
	  		e.printStackTrace();
	  	}
	         return tm;
        }



   private String enCode(String res ){
	  	 

	  	res =res.replaceAll("�","11111xx");
	  	res =res.replaceAll("�","11112xx");
		res =res.replaceAll("�","11114xx");
		res =res.replaceAll("�","11113xx");

		res =res.replaceAll("�","11115xx");
		res =res.replaceAll("�","11116xx");
		res =res.replaceAll("�","11117xx");
		res =res.replaceAll("�","11118xx");
		
		res =res.replaceAll("�","11119xx");
		res =res.replaceAll("�","11120xx");
		res =res.replaceAll("�","11121xx");
		res =res.replaceAll("�","11122xx");
		res =res.replaceAll("�","11120xx");

		res =res.replaceAll("�","11121xx");
        res =res.replaceAll("�","E");



	 	 
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

	   
	   while(res.indexOf("11119xx") != -1){
		   	int aa = res.indexOf("11119xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 

	   while(res.indexOf("11120xx") != -1){
		   	int aa = res.indexOf("11120xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 

	   

	   while(res.indexOf("11121xx") != -1){
		   	int aa = res.indexOf("11121xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 	   

	   while(res.indexOf("11122xx") != -1){
		   	int aa = res.indexOf("11122xx");
		   	res = res.substring(0,aa) + "�" + res.substring(aa+7,res.length()); 	    
		   } 
	
 	return res;
 }
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
  // makeData();
     
         
     // loene v�imalikud agendid sisse 
   
 	try {
 		  File f = new File (getDir() + "navikliendid.xml");
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "CustomerList");      
      //   System.out.println(root);
     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
         for (int i = 0; i < root.getChildNodes().getLength(); i++){
         	  Node rida = root.getChildNodes().item(i);
         	// System.out.println("ridu2  " + rida.getChildNodes().toString() + rida.getNodeValue());
         	  try{
         		
         		 String salesman  = getNode(rida,"SalesPersonCode").getFirstChild().getNodeValue(); 
         		 String salesmanName  =  getNode(rida,"SalesPersonName").getFirstChild().getNodeValue();
         		
         		if(salesman != null){
         			 salesman = deCode(salesman);
         			 salesmanName = deCode(salesmanName);
             		 Agent sm = new Agent (salesman);
             		 sm.setName(salesmanName);
                     
                      if(getParent().getProps().getProperty("groups."+ salesman) != null){
                    	  StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("groups."+ salesman),",");
                    	  while (st.hasMoreTokens()){
                    	 	  sm.addProdGroupList(st.nextToken());   
                    	 	//  System.out.println("gruplist " +salesmanName  + sm.getProdGroupList().toString());
                    	  }
                      }
                      tm.put(salesmanName, sm);
                   
         		}
         		 
         		
         		
         		
         		 try{
         			 
	         		String payer  =  getNode(rida,"CustomerNo").getFirstChild().getNodeValue();
	         		String name = deCode(getNode(rida,"Name").getFirstChild().getNodeValue());
	         			
	         		name = name.replaceAll("TARBIJATE �HISTU", "T�");	
	         		name = name.replaceAll("KAUPLUSED", " ");
	         		name = name.replaceAll("O�", " ");
	         		name = name.replaceAll(" AS ", " ");
	         		name = name.replaceAll("PEREMARKET ", " ");
	         		name = name.replaceAll("EESTI FOOD ", " ");
	         		name = name.replaceAll("BALTIC ", " ");	         		
	         		name = name.replaceAll("SUPERMARKETS ", " ");
	         		name = name.replaceAll("EESTI ", " ");
	         		name = name.replaceAll("APTEEK ", " ");
	         		
	         		
	         		 
	         		payers.put(deCode(payer), name.trim()  )  ;
	         		
	         		 String real  =  getNode(rida,"BillToCustomerNo").getFirstChild().getNodeValue();
        		      realpayers.addElement(deCode(real));
	         	   }catch(Exception e){
	         			//e.printStackTrace();  
	         	   }
	         	   
         		
         		
                   
         	  }catch(Exception e){
         		//  e.printStackTrace();
         	  }
              
         }
 	}catch(Exception e){
// 		e.printStackTrace();
 	}
     System.out.println("klientide  nimistu " + payers.size());
     /*try{ 
        	String httpsURL = "http://localhost:8010/naviwebservice/naviservice.asmx/SubmitRequest2?Company=EPP&" +
     		"XmlRequest=<?xml%20version=\"1.0\"%20encoding=\"ISO-8859-1\"?>" +
     	
                   "<GetProductGroupList/>" +
     		
     		"&XmlRequestAsLink=";
            URL myurl = new URL(httpsURL);
            URLConnection con = (URLConnection)myurl.openConnection();
            InputStream ins = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
            BufferedReader in = new BufferedReader(isr);

            String inputLine;
            StringBuffer inputLineBuf = new StringBuffer();
            while ((inputLine = in.readLine()) != null){
            	inputLine = enCode(inputLine);
            	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
            	
            }

            in.close();
            
            if(inputLineBuf.length() > 2){
              
                  try {
                	  File f = new File (getDir() + "navitootegrupid.xml");
                	  if(f.exists())f.delete();
            		PrintWriter os = new PrintWriter(new FileWriter(
            				getDir() + "navitootegrupid.xml", true));
            		os.write(inputLineBuf.toString());
            		os.close();
            	  }catch (Exception ex) {
            		ex.printStackTrace();
            	  }
             }
            
            //l�peb tootegruppidega tegelemine 
            System.out.println("Tootegrupid tehtud" );
            
           
        }catch(Exception e){
        	e.printStackTrace();
        }*/
 	
 	
     System.out.println("Agendilist tehtud" );

    
   //  System.out.println("Hinnakirju kasutusel "  + pricelists.size() +  " " + pricelists.toString() );

    // System.out.println("Aktiivseid kliente kasutusel "  + custtsUsed.size());

   //  fixedLines = getFixedAssortmentData1();
            
     return tm;
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
        	  
        	  
       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){
	   
	   try{
		     
	    	 
		    String httpURL = "http://localhost:8010/naviwebservice/naviservice.asmx/SubmitRequest2?Company=EPP&" +
		    		"XmlRequest=<?xml%20version=\"1.0\"%20encoding=\"ISO-8859-1\"?>" +
		    		"<GetCustomer><FromDateModified>2006-01-01</FromDateModified></GetCustomer>" +
		    		"&XmlRequestAsLink=";
		    URL myurl = new URL(httpURL);
		    URLConnection con = (URLConnection)myurl.openConnection();
		    InputStream ins = con.getInputStream();
		    InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		    BufferedReader in = new BufferedReader(isr);

		    String inputLine = "";
		    StringBuffer inputLineBuf = new StringBuffer();
		    while ((inputLine = in.readLine()) != null){
		    	inputLine = this.enCode(inputLine);
		     	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));	
		    	
		      //  System.out.println(inputLine);	
		    }

		    in.close();
		    
		    if(inputLineBuf.length() > 2){
		       
		           try {
		              File f = new File (getDir() + "navikliendid1.xml");
		         	  if(f.exists())f.delete();
		     		PrintWriter os = new PrintWriter(new FileWriter(
		     				getDir() + "navikliendid1.xml", true));
		     		os.write(inputLineBuf.toString());
		     		os.close();
		     		if(f.exists() && f.length() > 50000){
		     			 File ff = new File (getDir() + "navikliendid.xml");
		     			 if(ff.exists()){
		     				 ff.delete();
		     				 
		     			 }
		     			 f.renameTo(new File (getDir() + "navikliendid.xml"));
			 		}
		     	  }catch (IOException ex) {
		     		ex.printStackTrace();
		     	  }
		      }
		    
		 }catch(Exception e){
		 	e.printStackTrace();
		 }
		 
		 System.out.println("kliendid tehtud" );
		 
		 
		     

	   
	   
	     TreeMap tm = new TreeMap();
	     System.out.println("Kliendid alustan");
	    
	     this.customers = new ArrayList();
	   
	   try {
	 		  File f = new File (getDir() + "navikliendid.xml");
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     
	 		if(f.exists() && f.length() < 50000){
	 			System.exit(0);
	 		}
	 		
	 		FileInputStream in = getCleanInputStream(f);
	     	String handler = null;

	 		  Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "CustomerList");      
	          System.out.println(root);
	      	System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	         
	         	  try{
	         		  String id =  getNode(rida,"CustomerNo").getFirstChild().getNodeValue();
	         		  Customer customer = new Customer(id);

	         		  String name =  getNode(rida,"Name").getFirstChild().getNodeValue();
	         		 
	         		  
	         		 try{
	         		  String name2 =  getNode(rida,"Name2").getFirstChild().getNodeValue();
	         		  name = name + " "+deCode(name2) ;
                       }catch(Exception e){
	         			  
	         		  }
                      
	         		  
	         		 customer.setName(deCode(name + " " + id));
	         		  try{
	         			 String aadr =  getNode(rida,"Address").getFirstChild().getNodeValue();
	         			 try{
		         			 String post =  getNode(rida,"PostCode").getFirstChild().getNodeValue(); 
		         			String linn =  getNode(rida,"City").getFirstChild().getNodeValue();  
			         		 aadr = aadr + " " +(deCode(post + "," +linn ));  	  
		         		  }catch(Exception ea){
		         		  }
	         			 
		         		  customer.setAddress(deCode(aadr));
		         		  
	         		  }catch(Exception e){
	         			  
	         		  }
	         		
	         	     try{
     		    	 	 
	         			 String contacname =  getNode(getNode(getNode(rida, "ContactList"), "Contact"),"ContactName").getFirstChild().getNodeValue()   ;
	         			 if(contacname.trim().length() >0){

			         		  customer.setPersonalData(deCode(contacname)); 
	         			 }  	  
	         		  }catch(Exception e){
	         		  }
	         		
	         		     try{
		         			 String phone =  getNode(rida,"Phone").getFirstChild().getNodeValue(); 
		         			 if(customer.getPersonalData() != null){
				         		  customer.setPersonalData(customer.getPersonalData() + " " + deCode(phone));  
		         			 }else{
				         		  customer.setPersonalData(deCode(phone)); 
		         			 } 	  
		         		  }catch(Exception e){
		         		  }
	         		  

		         		
		         		    
	         		 try{
	         		 String paym =  getNode(rida,"PaymentTermCode").getFirstChild().getNodeValue();
	         		  customer.setPaymTerm(deCode(paym));
                         }catch(Exception e){
	         			  
	         		  }
                        
                         
                         try{
	         		 String salesman  =  getNode(rida,"SalesPersonCode").getFirstChild().getNodeValue();
	         		      customer.addAgent(new Agent(deCode(salesman)));
	         		     if(!parent.getEmail(salesman).equals("") &&  !custtsUsed.contains(id)){     		   
	     	         		   custtsUsed.add(id);
	           		      }
	         	   }catch(Exception e){
	         			  
	         		  }
	               try{
		         		 String payer  =  getNode(rida,"BillToCustomerNo").getFirstChild().getNodeValue();
		         		      customer.setPayer(deCode(payer));
		         		      if(payers.get(payer) != null){
		         		    	 customer.setName((String)payers.get(payer) + " " + customer.getName());
		         		      }
		         	   }catch(Exception e){
		         			  
		         		  }
		         	   
		         	   
		         	   try{
			         		 String price_List  =  getNode(rida,"CustomerDiscGorupCode").getFirstChild().getNodeValue();
			         		      customer.setDiscount(Double.parseDouble(deCode(price_List)));
			         	   }catch(Exception e){
			         			  
			         		  }
			         	  boolean goes = true;
			         	   try{
				         		 String blocked  =  getNode(rida,"Blocked").getFirstChild().getNodeValue();
				         		
				         		 if(blocked.equals("1")){
				         		      customer.setSalesBlock(true); 
				         		      goes = false;
				         		 }
				         	   }catch(Exception e){
				         			  
				         		  }
				         	   if(realpayers.contains(customer.getID())){
				         		   goes = false;
				         	   }
				         	   
	         	   if(goes){
	         		  tm.put(customer.getName(), customer);
	                     this.customers.add(customer.getID());
	                   
	         	   }
	         		      
	         	  }catch(Exception e){
	         		  
	         	  }
	              
	         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   
	    
	 	
	 	
	 
	    System.out.println("Kliendid tehtud");
	    
     return tm;
   }

   private boolean isOkDate(String mmddyy){
	  	long lon = 0;
	  	Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(6, 7))-1);
	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(9, 10)));
	  // System.out.println(cal.getTime()); 
	   Date date = new Date(lon);
	   date.setTime(cal.getTimeInMillis());
		Calendar ca = Calendar.getInstance();
		ca.add(Calendar.MONTH, -6);
		
		if(cal.after(ca)){
			return true;
		}else{
			return false;
		}
		
	  }

   private Vector getCP(String custid , String number, String date ,Node doc){

	   Vector result = new Vector();
	   try{
		                  
		   Node lines = getNode(doc, "InvoiceLines");
		   for (int i = 0; i < lines.getChildNodes().getLength(); i++){
	           	  Node rida = lines.getChildNodes().item(i);
	           	
	          	if (rida.getNodeName().equals("InvoiceLine")){
		   try{
			   String toode = getNode(rida,"ItemNo").getFirstChild().getNodeValue();
		          String kkk = getNode(rida,"Quantity").getFirstChild().getNodeValue();
		          String lp = getNode(rida,"UnitPrice").getFirstChild().getNodeValue();
			        	
			        	double kogus = Double.parseDouble(kkk);
			        	double lastp = Double.parseDouble(lp);
			        	CustProd cp = new CustProd(custid, toode, kogus ,0 ,0,date.substring(5,10),lastp,0,0," ");			        	
			        	result.add(cp); 
		   }catch (Exception e ){
			   
		   }
		       
			        	}
		        
		        
		         }
		        
		        
	   }catch(Exception e){
		   e.printStackTrace();
		   
	   }
	   return result;
   }

   private String filter(String res ){
	   res = res.replaceAll("salvr�tikud","salvr.");
	   res = res.replaceAll("Salvr�tid","Salvr.");
	   res = res.replaceAll("Haavaplaastrid","Haavapl.");
	   res = res.replaceAll("laaster","l.");
	   res = res.replaceAll("angadega","ang.");
	   res = res.replaceAll("Hambahari","HambHa");
	   res = res.replaceAll("Kreem","Kr.");
	   res = res.replaceAll("kreem","kr.");
	   res = res.replaceAll("akkepaber","ak.pab");
	   res = res.replaceAll("Kinkekott","KinkKt.");
	   res = res.replaceAll("pudel","pdl");
	   res = res.replaceAll("unane","un.");
	   res = res.replaceAll("kuldne ","kuld.");
	   res = res.replaceAll("h�bedane ","h�be.");
	   res = res.replaceAll("sinine","sin.");
	   res = res.replaceAll("roheline","roh.");
	   res = res.replaceAll("kollane ","kol.");
	   res = res.replaceAll("christmas","chri.");
	   res = res.replaceAll("j�ulu","j�");
	   res = res.replaceAll("assortii","asso.");
	   res = res.replaceAll("SPLENDENE","SPLE");
	   res = res.replaceAll("REFLEX","REF");
	   res = res.replaceAll("Rosetipael","Rosetip.");
	   res = res.replaceAll("COUNTRY","COU");
	   res = res.replaceAll("Kalkulaator","Kalk.");
	   res = res.replaceAll("Vihikumapp","VihiMp");

	   res = res.replaceAll("Viltpliiatsid","Viltpl.");
	   res = res.replaceAll("V�rvipliiatsid","V�rvipl.");

	   res = res.replaceAll("puhastamiseks","puhast.");
	   res = res.replaceAll("Massaazi","Mass.");
	   res = res.replaceAll("higistamist  ennetav","hig.enne.");
	   res = res.replaceAll("toestusega","toe.");
	   res = res.replaceAll("Anatoomiline","Anat.");
	   res = res.replaceAll("reguleeritavad","reg.");
	   res = res.replaceAll("l�hike","l�h");
	   res = res.replaceAll("meestele","mees.");
	   res = res.replaceAll("p�lvikud","p�lv.");
	   res = res.replaceAll("villased","vill.");
	   res = res.replaceAll("trombootilised","tromb.");
	   res = res.replaceAll("Sukkp�ksid","Sukkp�. ");
	   res = res.replaceAll("naturaalne","nat.");
	   res = res.replaceAll("tselluliidi","tsellu.");
	   res = res.replaceAll("rasedale","rase.");
	   res = res.replaceAll("naistele","nais.");
	   res = res.replaceAll("permanent","perm.");
	   res = res.replaceAll("Geelpliiats","Geelpl.");
	   res = res.replaceAll("kalligraafiline","kalligraaf.");
	   res = res.replaceAll("Helestusmarker ","Hel.marker ");
	   res = res.replaceAll("astapliiats","astapl.");
	   res = res.replaceAll("Komplekt","Kompl.");
	   res = res.replaceAll("intsel","int.");
	   res = res.replaceAll("Modelleerimis","Model.");
	   res = res.replaceAll("Voolimis","Vool.");
	   res = res.replaceAll("Pinal","Pin");
	   res = res.replaceAll("Spiraal","Spir.");
	   res = res.replaceAll("aamat","mt.");
	   res = res.replaceAll("V�rvipaberite","V�rPab.");
	   res = res.replaceAll("kleebised","kleeb");
	   res = res.replaceAll("plastik","pla.");
	   res = res.replaceAll("kiirk�itja","kiirK�i.");
	   res = res.replaceAll("klambriga","klamb.");
	   res = res.replaceAll("Registraator","Registr.");
	   res = res.replaceAll("Dokumendi�mbrik","Dok�mb");
	   res = res.replaceAll("Dokumendikohver","DokKoh");

	   res = res.replaceAll("Dokumendimapp","Dok.mapp");
	   
	   return res;
   }
   
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     String grr = "";
     System.out.println("Tooted alustan");
     try{
    	 TreeMap grup = this.getProdGroups3();
         Iterator enu = grup.keySet().iterator();
         System.out.println("Tooted alustan +" + grup.keySet().size() );
         while(enu.hasNext()){
        	 String inputLine = "";
             StringBuffer inputLineBuf = new StringBuffer();
              grr = (String)enu.next();
        	 String httpsURL = "http://localhost:8010/naviwebservice/naviservice.asmx/SubmitRequest2?Company=EPP&XmlRequest=<?xml%20version=\"1.0\"%20encoding=\"ISO-8859-1\"?>" +
     		"<GetItemList><ProductGroup>" + grr + "</ProductGroup></GetItemList>&XmlRequestAsLink=";
    URL myurl = new URL(httpsURL);
    URLConnection con = (URLConnection)myurl.openConnection();
    InputStream ins = con.getInputStream();
    InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
    BufferedReader in = new BufferedReader(isr);


    while ((inputLine = in.readLine()) != null){
    	inputLine = enCode(inputLine);
    	inputLineBuf.append(inputLine.replaceAll("<Item","\n<Item" ).replaceAll("&quot;", "") );
    }

      in.close();  
      
     System.out.println("grupp " + grr + " " +inputLineBuf.length());      
      
      if(inputLineBuf.length() > 2){
          
          try {
        	  File f = new File (getDir() + grr +"navitooted.xml");
        	  if(f.exists())f.delete();
    		PrintWriter os = new PrintWriter(new FileWriter(
    				getDir() +grr+  "navitooted.xml", true));
    		os.write(inputLineBuf.toString());
    		os.close();
    	  }catch (IOException ex) {
    		ex.printStackTrace();
    	  }
     }
      

      try {
     	 Vector koodid = new Vector();
     	 Vector lopumyyk = new Vector();
   	  File f = new File (getDir() + grr+"navitooted.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream inx = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(inx,handler);
        inx.close();
        Node root = getNode(doc, "ItemList");      
      System.out.println("grr ja len  " + grr + " " +root.getChildNodes().getLength());
      if(root.getChildNodes().getLength() > 0 ){
    	     for (int i = 0; i < root.getChildNodes().getLength(); i++){
           	  Node rida = root.getChildNodes().item(i);
           	
          	if (rida.getNodeName().equals("Item")){
          		 boolean goes = true;
          	     String id = getNode(rida,"No").getFirstChild().getNodeValue();
          		   Product prod = new Product(deCode(id));
          		  
          		 try{
       	    		 String grupp = getNode(rida, "ProductGroupCode").getFirstChild().getNodeValue();
       	    		
       	    	   prod.setGroup(grupp.substring(0,1));
       	    	  prod.setGroupSecond(grupp.substring(0,1) + grupp);
       	    	  	   
       	    	 }catch (Exception e){
       	    		 
       	   	 }
          		
          		   
          		   
        	      try{
        	    	  String name   = getNode(rida,"Description").getFirstChild().getNodeValue();
        	    	try{
        	    		name = (name +   getNode(rida,"Description2").getFirstChild().getNodeValue()).trim();
        	    	}catch(Exception e){
        	    		
        	    	}
        	    	  String status = null;
        	    	 
        	    	 try{
        	    	   status = getNode(rida,"Status").getFirstChild().getNodeValue();
        	    	   status = deCode(status);
        	    	   if(status.indexOf("HEKORDNE") >-1){
        	    		   name =  name + " *ET* ";
        	    	   }
        	    	   
        	    	   
        	    	   
        	    	   if(status.equals("L�PETATAV")){
        	    		   name =name +  " *L*";
        	    		   lopumyyk.add(id);
        	    	   }
        	    	   
        	    	   if(status.equals("P�SISORTIMENT")){
        	    		   name = name +  " *PS*";
        	    	   }
        	    	   if(status.equals("HOOAJAKAUP")){
        	    		   name = name +  " *HK*";
        	    	   }
        	    	   if(status.equals("UUS KAUP")){
        	    		   name = name +  " *UUS*";
        	    	   }
        	    	   if(status.equals("TESTER")){
        	    		   name = name +  " *TES*";
        	    	   }
        	    	   
        	    	   if(status.equals("T�HJENDUSM��K")){
        	    		   name = name +  " *T�H*";
        	    	   }
        	    	   if(status.equals("KAMPAANIA")){
        	    		   name = name +  " *KAM*";
        	    	   }
        	    	   if(status.equals("N�IDIS")){ //vmi
        	    		  goes = false;
        	    	   }
        	    	   if(status.equals("VMI")){ //vmi
         	    		  goes = false;
         	    	   }

        	    	   
        	    	  prod.setPartii(filter(deCode(status)));
        	    		   
        	      }catch(Exception e){
      	    		
      	    	}
        	      String eee = "";
        	      try{
             		 String ean   = getNode(rida,"CurrentBarCode").getFirstChild().getNodeValue();
             	     prod.setEAN(ean);
             	     if(ean.length()>6){
             	    	 eee = " " + ean.substring(ean.length()-6,ean.length());
             	     }
         	    	  
     	      }catch(Exception e ){
     	    	  
     	      }
     	      Vector vvv = new Vector();
     	     if(getParent().getProps().getProperty("kood.ees.grupid") != null){
     	    	 StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("kood.ees.grupid"),",");
     	    	 while(st.hasMoreTokens()){
     	    		 vvv.addElement(st.nextToken());
     	    	 }
     	     }
     	     if(vvv.contains(prod.getGroupSecond())){
         	     prod.setName("    " + grr + " " + prod.getID() + " " + filter(deCode(name)) + " "  + eee);
     	     }else{
         	     prod.setName("    " + grr + " " + filter(deCode(name)) + " " +  prod.getID() + eee); 
     	     }
             	   System.out.println("name   " + prod.getName());
        	      }catch(Exception e ){
        	    	  e.printStackTrace();
        	      }
        	      
               
        	 
   	    	 try{
   	      	    prod.setPrice(Double.parseDouble(getNode(rida,"SalesPrice").getFirstChild().getNodeValue())); 
   	    	 }catch (Exception e){
   	    		prod.setPrice(1);
   	    	 }
        	
   	    	 try{
 	      	    prod.setInCases(Double.parseDouble(getNode(rida,"UnitsPerParcel").getFirstChild().getNodeValue())); 
 	    	 }catch (Exception e){
 	    		
 	    	 }
 	    	 try{
 	    		 double ooo = Double.parseDouble(getNode(rida,"MinimalOrderQty").getFirstChild().getNodeValue());
 	    		 if(ooo > 1){
 	     	      	    prod.setInCases(ooo);  
 	    		 }
  	    	 }catch (Exception e){
  	    		
  	    	 }
   	    	 
   	    	 try{
   	    	  	  prod.setUnit(getNode(getNode(getNode(rida, "UoMList"), "UoM"),"Code").getFirstChild().getNodeValue());	 
   	    	 }catch (Exception e){
    	    		 
    	   	 }
    	    	
   	    
   	    	 try{
     	    	   prod.setVat(getNode(rida, "VATCode").getFirstChild().getNodeValue());
    	    	  	   
    	    	 }catch (Exception e){
     	    		 
     	   	 }
     	   	   
         	 
       	      //    prod.setStock(Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
        	 	 
          	    
        	   try{
        		   if("1".equals(getNode(rida, "Blocked").getFirstChild().getNodeValue())){
             	    	goes = false;
             	       }  
        	   }catch(Exception e){
        		   
        	   }
        	
        	    
        	 
        	    if(goes && prod.getName()!= null){
        	         prodHash.put(prod.getID(), prod);
        	         tm.put(prod.getName(), prod);	
        	         koodid.add(id);
        	    }
        		
          	}
            
            } 
      }
   if(koodid.size() >0){
	
   	 TreeMap stocks = getStocks(koodid);
   	 
   	 Iterator ite = stocks.keySet().iterator();
   	 while(ite.hasNext()){
   		 String toode = (String)ite.next();
   		 if(prodHash.get(toode)!= null){
   			 Product ppp = (Product)prodHash.get(toode);
   			 
   			 ppp.setStock(Double.parseDouble((String)stocks.get(toode)));
   			 if( lopumyyk.contains(toode) && ppp.getStock() < 1){
   				 tm.remove(ppp.getName());  //l�pumyygi toote korral kui laoseis on null siis ei pane kaasa 
   				 System.out.println(" removed " + ppp.getName());
   			 } else if(lopumyyk.contains(toode)){
   				// ppp.setStatusSellout("1");
   			 }
   		 }
   	 }
     
   }
    } catch (Exception e){
      e.printStackTrace();
    }
    
      

      //l�peb toodetega tegelemine 

           System.out.println("Tooted tehtud" );
         }	 
     



     
     
     }catch (Exception e){
    	 
     }
   
     return tm;
   }

   private TreeMap getStocks(Vector prods){
	   TreeMap result = new TreeMap();
	   Enumeration en = prods.elements();
	   String add = "";
	   while (en.hasMoreElements()){
		 //http://localhost:8010/naviwebservice/naviservice.asmx/SubmitRequest2?Company=EPP&XmlRequest=<?xml%20version="1.0"%20encoding="ISO-8859-1"?><GetAvailableQuantity><ItemList><ItemNo>S609/1</ItemNo></ItemList></GetAvailableQuantity>&XmlRequestAsLink=         	
String sss = (String)en.nextElement();
add = add + "<ItemNo>" + sss + "</ItemNo>" ;
		   }
		   try{
			     
		    	 
			    String httpURL = "http://localhost:8010/naviwebservice/naviservice.asmx/SubmitRequest2?Company=EPP&XmlRequest=<?xml%20version=\"1.0\"%20encoding=\"ISO-8859-1\"?>" +
			    		"<GetAvailableQuantity><ItemList>" +add + "</ItemList></GetAvailableQuantity>&XmlRequestAsLink=";
			    URL myurl = new URL(httpURL);
			    URLConnection con = (URLConnection)myurl.openConnection();
			    InputStream ins = con.getInputStream();
			    InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
			    BufferedReader in = new BufferedReader(isr);

			    String inputLine = "";
			    StringBuffer inputLineBuf = new StringBuffer();
			    while ((inputLine = in.readLine()) != null){
			    	inputLine = this.enCode(inputLine);
			     	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));	
			    	
			      //  System.out.println(inputLine);	
			    }

			    in.close();
			    
			    if(inputLineBuf.length() > 2){
			       
			           try {
			         	  
			         	  File f = new File (getDir() + "navistock.xml");
			         	  if(f.exists())f.delete();
			     		PrintWriter os = new PrintWriter(new FileWriter(
			     				getDir() + "navistock.xml", true));
			     		os.write(inputLineBuf.toString());
			     		os.close();
			     	  }catch (IOException ex) {
			     		ex.printStackTrace();
			     	  }
			      }
			    
			 }catch(Exception e){
			 	e.printStackTrace();
			 }
			 
			 System.out.println("laoseis tehtud" );
			 
			 
			     
			     
			     
			     // loene v�imalikud agendid sisse 
			   
			 	try {
			 		  File f = new File (getDir() + "navistock.xml");
				 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				 	     DocumentBuilder db;
				 		db = dbf.newDocumentBuilder();      	
				     	FileInputStream in = getCleanInputStream(f);
				     	String handler = null;
				         Document doc = db.parse(in,handler);
				         Node root = getNode(doc, "AvailableQuantity");      
				      //   System.out.println(root);
				     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
				         for (int i = 0; i < root.getChildNodes().getLength(); i++){

				         	  Node rida = root.getChildNodes().item(i); 
				         	  
				         	  if(rida.getNodeName().equals("Item")){
				         		  Node item = getNode(rida,"ItemNo");
				         		  String id = item.getFirstChild().getNodeValue();
				         		  
				         		 for (int a = 0; a < rida.getChildNodes().getLength(); a++){
				         			Node seis = rida.getChildNodes().item(a); 
				         			
				         			if(seis.getNodeName().equals("QuantityByLocation")){
				         			
				         			 String ladu =(getNode(seis, "LocationCode").getFirstChild().getNodeValue());
				         			
					         		  if(ladu.equals("01")){
					         			 String kogus =  (getNode(seis, "AvailableQuantity").getFirstChild().getNodeValue());
					         			 result.put(id, kogus);
					         		  } 
					         			
				         			}
				         		 }
				         		  
				         		 
				         		 
				         	  }
				         	  
				         }
		
			 	}catch(Exception aa){
			 		
			 	}
			 	System.out.println("laoseis " + result.toString());
	   return result ;
   }
   
   public TreeMap getProducts2(){

	    TreeMap tm = new TreeMap();
	     
	     try {
	 
	    	   FileReader fr = new FileReader(getDir() + "navitooted.xml");
	           BufferedReader reader = new BufferedReader(fr);
	         String line = null;

	          while((line = reader.readLine()) != null  ){
	        	 
		        	
	        	  if((line.trim().length()) > 50){
	        		 // System.out.println(line);
	        		  int len = line.indexOf("code=");
		        	  String id   = line.substring(len + 6);
		      	      Product prod = new Product(id.substring(0,id.indexOf("\"")).trim());
		      	      len = line.indexOf("default_name=");
		      	     String name   = line.substring(len + 14);
		      	     prod.setName( prod.getID() + " " + deCode(name.substring(0,name.indexOf("\""))));
		      	     tm.put(prod.getID(), prod); 
	        	  }
	        	
	          }	    	 
	    
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	     return tm;
	     
	   }
   
   public String getAssortmentStr(Agent agent){
    return null;
  }


   public Vector getCustomerProductsList(String agentCode){
	 //1.teeme klientide arvete nimekirja .

	     System.out.println("KliendiTooted alustan");
	   Vector custProds = new Vector();
	//   this.custtsUsed = new Vector();
	   Enumeration enums = this.custtsUsed.elements();
	   while(enums.hasMoreElements()){
		String custid = (String)enums.nextElement();
		   try{
				String httpsURL = "http://localhost:8010/naviwebservice/naviservice.asmx/SubmitRequest2?Company=EPP&XmlRequest=<?xml%20version=\"1.0\"%20encoding=\"ISO-8859-1\"?>" +
						"<GetInvoices><CustomerNo>" + custid + "</CustomerNo></GetInvoices>&XmlRequestAsLink=" ;
		      //  System.out.println(httpsURL);
				URL myurl = new URL(httpsURL);
		        URLConnection con = (URLConnection)myurl.openConnection();
		        InputStream ins = con.getInputStream();
		        InputStreamReader isr = new InputStreamReader(ins,"UTF-8");
		        BufferedReader in = new BufferedReader(isr);

		        String inputLine;
		        StringBuffer inputLineBuf = new StringBuffer();
		        while ((inputLine = in.readLine()) != null){
		        	inputLine = this.enCode(inputLine);
		        	inputLineBuf.append(inputLine.replaceAll("/>","/>\n" ));
		        }
		        	  if(inputLineBuf.length() > 2){
					       
				           try {
				         	  
				         	  File f = new File (getDir() + "navicustprod.xml");
				         	  if(f.exists())f.delete();
				     		PrintWriter os = new PrintWriter(new FileWriter(
				     				getDir() + "navicustprod.xml", true));
				     		os.write(inputLineBuf.toString());
				     		os.close();
				     	  }catch (IOException ex) {
				     		ex.printStackTrace();
				     	  }
				      }
		        	


		        	  
		        	  
		        	  
		      	 	try {
				 		  File f = new File (getDir() + "navicustprod.xml");
					 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					 	     DocumentBuilder db;
					 		db = dbf.newDocumentBuilder();      	
					     	FileInputStream inx = getCleanInputStream(f);
					     	String handler = null;
					         Document doc = db.parse(inx,handler);
					         inx.close();
					         Node root = getNode(doc, "Invoices");      
					      //   System.out.println(root);
					     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
					         for (int i = 0; i < root.getChildNodes().getLength(); i++){

					         	  Node rida = root.getChildNodes().item(i); 
					         	  
					         	  if(rida.getNodeName().equals("Invoice")){
					         		  
					         			String date = getNode(rida,"DocumentDate").getFirstChild().getNodeValue();
					         			String number = getNode(rida,"InvoiceNo").getFirstChild().getNodeValue();
						        		
						        		 if(isOkDate(date)){
						        			 System.out.println(date + " " + number + " " + custid);	
						        		
						     					Vector cps = this.getCP(custid,number,date,rida);
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
			
				 	}catch(Exception aa){
				 		
				 	}
		        in.close();
		       
		   } catch (Exception a){
			   a.printStackTrace();
		   }
	   }
	   System.out.println("KliendiTooted alustan");
	  	
	   return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "NaviEpp";
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


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private Vector realpayers = new Vector();
   private TreeMap payers = new TreeMap();
   private Vector fixedLines = new Vector();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
/*
  	 try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;

    //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

    while(!(line = reader.readLine()).equals("[Get Salary]")){
    }
    line=reader.readLine();
    while((line = reader.readLine())!= null &&  line.trim().length() > 0 ){
       line = line.replaceAll("\",\"","|");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
          double sales = Double.parseDouble(clear(st.nextToken()));
                if (st.hasMoreTokens()){
                 String agentID = clear(st.nextToken());
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
            }
          }
        }
    }
      } catch (IOException e){
        e.printStackTrace();
      }*/
  }
}
