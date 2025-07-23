package eevaeksport.driver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ee.mobilejava.earve.GenericDOM;
import eevaeksport.Agent;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.imp.Order;
import eevaeksport.util.Converter;
import eevaeksport.util.ReportingService;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class RivonaLogDBDriver extends AbstractDBDriver  {

  public RivonaLogDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
        Class.forName(databaseDriverName).newInstance();
    } catch (Exception e) {
        parent.showError(e);
    }

    company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
   
  
  public String getSaveDebtInfo() {
	 
	
	  try{
	 	  Converter.sendRoutesJson(getRoutes(),user,pass, rcompany);
	 	 }catch(Exception e){
	 		 e.printStackTrace();
	 	 }
	  TreeMap custs = this.getCustomers(null);
	  
	 
 	sendShipitDocs(custs);
	  System.exit(0);
   	return null;
   }
  
  
  public  Object readObject(String fileName) throws Exception{
		Object obj = null;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
		try  {
			obj = in.readObject();
		} catch (InvalidClassException e){
		}
		in.close();
		return obj;
	}
  
  

  
  private  void sendShipitDocs(TreeMap custs){
	    
	     System.out.println("Doc alustan");
	     
	     Vector uploadedXML = new Vector();
	     
	     String user =  getParent().getProps().getProperty("rest.user");
	     String pass = getParent().getProps().getProperty("rest.pass");
	     String company = getParent().getProps().getProperty("rest.company");
         int cou = 0;
         Vector<Order> res = new Vector<Order>();
	  
         HashMap<String,Integer> usedroutes = new HashMap<String,Integer>();
         try{
        	 usedroutes = (HashMap<String,Integer>)this.readObject("memory.hash");
         }catch(Exception e){
        	 e.printStackTrace();
         }
         
	   try {
		   
		   TreeMap aLine = new TreeMap();
		   
		   //Teeme read valmis 

		   String aaa = null;
	         int cc = 1;
	         		   
		   File dir = new File("docs");
		     File[] files = dir.listFiles();
		     for (int i = 0; i < files.length ; i++){
		       File file = files[i];
		   		      if (file.isFile()){
		         String fileName = file.getAbsolutePath();
		         
		      if(fileName.toLowerCase().endsWith(".xml")) {
		    	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    	    DocumentBuilder db = dbf.newDocumentBuilder();
		    		FileInputStream in = getCleanInputStream(file);
		    		String handler = null;
		    	    Document doc = db.parse(in,handler);
		    	    Node root = getNode(doc, "Shipment");
		    	    String id = getNode(root, "RouteId").getFirstChild().getNodeValue();
		    	    Node read =  getNode(root, "Customers");
                    Node plat =  getNode(root, "TruckLp");
                    String plate =  null;
                    if(plat != null &&  plat.getFirstChild() != null) {
                    	 plate =  plat.getFirstChild().getNodeValue();
                    
                   
		    	    String rid = id+"-"+plate;
		        	   if(aaa == null || !aaa.equals(plate)){
		        		   cc = 1;
		        	   }
		        	   if(usedroutes.get(rid) == null){
		        		   usedroutes.put(rid, cc); 
		        	   }
		         cc++;
		         aaa = id;
		   	    for (int u = 0; u < read.getChildNodes().getLength(); u++){
		    	          Node rida = read.getChildNodes().item(u);
		    	          if (rida.getNodeName().equalsIgnoreCase("Customer")){
		    	        	 String cuid = getNode(rida, "CustomerId").getFirstChild().getNodeValue();
		    	        	String idx = id + "-" + cuid;
		    	        	 Node details =  getNode(rida, "Details");
		       	 for (int ue = 0; ue < details.getChildNodes().getLength(); ue++){
				    	         
        	   String ean = details.getChildNodes().item(ue).getFirstChild().getNodeValue();
        	   Product pr = new Product("Container");
       		   pr.setEAN(ean);
       		   pr.setInCases(1);
       		   Vector prods = new Vector();
       		   if(aLine.get(idx) != null){
       			prods = (Vector)aLine.get(idx);
       		   }
       		   prods.add(pr);
       		   aLine.put(idx,prods);
		       	 } 		      
           }}}
                    in.close();}}}

		     this.saveObject(usedroutes, "memory.hash");
           

      
      	     
         System.out.println("UsedLines " + aLine.size());
    
           		      files = dir.listFiles();
		     for (int i = 0; i < files.length ; i++){
		       File file = files[i];
		   		      if (file.isFile()){
		         String fileName = file.getAbsolutePath();
		         
		      if(fileName.toLowerCase().endsWith(".xml")) {
		    	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    	    DocumentBuilder db = dbf.newDocumentBuilder();
		    		FileInputStream in = getCleanInputStream(file);
		    		String handler = null;
		    	    Document doc = db.parse(in,handler);
		    	    Node root = getNode(doc, "Shipment");
		    	    String rid = getNode(root, "RouteId").getFirstChild().getNodeValue();
		    
		    	  Node plat =  getNode(root, "TruckLp");
                  String plate =  null;
                  if(plat != null &&  plat.getFirstChild() != null) {
                  	 plate =  plat.getFirstChild().getNodeValue();
                  
		    	  
		    	  
                  String date = getNode(root, "Date").getFirstChild().getNodeValue();
                  String coordinates = getNode(root, "Coordinates").getFirstChild().getNodeValue();
		    	     	   	        
		    	  Node read =  getNode(root, "Customers");
		   	    for (int u = 0; u < read.getChildNodes().getLength(); u++){
		    	          Node rida = read.getChildNodes().item(u);
		    	          if (rida.getNodeName().equalsIgnoreCase("Customer")){
		    	        	 String cuid = getNode(rida, "CustomerId").getFirstChild().getNodeValue();
		    	        	 String id = rid + "-" + cuid;
		    	        	 
		    	        	  
		    		           String doknr =  rid;
		    		 	       String routes = plate;
		    		 	       String aaaa = coordinates.replaceAll("_", "-");
		    		 	       String routeToNext = aaaa; 
		    		 	        
		    		 	         	 if(usedroutes.get(rid+"-"+routes) != null){
		    		 	         		 int ddd = usedroutes.get(rid+"-"+routes);
		    		 	         		 if(ddd > 1)
		    		 	         		 	 routes = routes + "-" + ddd;
		    		 	         		 }
		    		 	         	 
		    		         		 Date deldate  =  getDate(date);
		    		         		 String prior = getNode(rida, "Sequence").getFirstChild().getNodeValue();
		    		         		 int pp = 0;
		    		         		 try{
		    		         			pp =   Integer.parseInt(prior);
		    		         			prior = pp + "";
		    		         		 }catch(Exception r){
		    		         			 
		    		         		 }
		    		         		 
		    		         		 if(prior.length() == 1){
		    		         			 prior= "00" + prior;
		    		         		 }
		    		         		 
		    		         		if(prior.length() == 2){
		    		         			 prior= "0" + prior;
		    		         		 }
		    		         		
		    		         		if(doknr == null || doknr.trim().length() == 0 ){
		    		         			doknr = cuid;
		    		         		}
		    		     	     
		    		     	     	
		    		     	     	if(aLine != null && aLine.get(id)!= null) {
		    		     	     		
		    		     	     		Order shipit = new Order(cuid,"autosender");
		    		         			shipit.prodLines = (Vector<Product>)aLine.get(id);
		    		         			shipit.docType = "SHIPIT";
		    		         			shipit.origId = null;
		    		         			shipit.extId = rid;
		    		         			shipit.completedMsisdn = null;
		    		         			shipit.completedDate = null;
		    		         			shipit.completedComment = null;
		    		         			shipit.deliveryDate = deldate;
		    		         			shipit.extDoc = doknr+cuid;
		    		         			shipit.restPriority = prior;
		    		         			shipit.route =  routes;
		    		         			shipit.partner = "RIVONA";
		    		         		    shipit.routeToNext  = routeToNext; 
		    		         			 res.add(shipit);
		    		     	     	}
		    		    	
		    	          }
		    	          }   
		   	
		   	 
		   	 in.close();
                  }     	
                  uploadedXML.add(file.getName());
	    	    	}}}
	         		
		      files = dir.listFiles();
			     for (int i = 0; i < files.length ; i++){
			       File file = files[i];
	   	 
		       
		      //    System.out.println( "uhuuu1 "+  file.getAbsolutePath() + " " + uploadedXML.toString());
	            
	            if(uploadedXML.contains(file.getName())){
	            	File ren  = new File(dir+ "/read/" + file.getName());
	            	boolean dd = file.renameTo(ren);
	                System.out.println( "uhuuu2 "+ dd + " "+ren.getAbsolutePath() + " " + uploadedXML.toString());
		  	        
	            }
	        }
		     
          	  } catch (Exception e){
                 e.printStackTrace();	
	          }
	          
	 
	         	
	        System.out.println("Dokumente postima " + res.size() );
	        Converter.sendShipitListJson(res, user, pass, company) ;
	          
	  
	   System.out.println("Dokumente kokku " + cou );
		
	 
		sendDocs(uploadedXML);
	   
	
	        
	   
	   }  

private Date getDate(String date) {
	int year = Integer.parseInt(date.substring(0,4));
	int mm = Integer.parseInt(date.substring(5,7));
	int dd = Integer.parseInt(date.substring(8,10));
	Calendar cc = Calendar.getInstance();
	cc.set(Calendar.YEAR, year);
	cc.set(Calendar.MONTH, mm-1);
	cc.set(Calendar.DAY_OF_MONTH, dd);
	return cc.getTime();
}



private void sendDocs(Vector uploadedXML){
	  //Saadame dokumentte 
	   String dir = getParent().getProps().getProperty("docs.dir");
	   if(dir != null){
		   
		   File d2 = new File(dir+ "read/");
		   if(!d2.exists()){
			   d2.mkdir();
		   }

		   
				   File docs = new File(dir);
		   
		   String[] children2 = docs.list();
      	 Calendar cal = Calendar.getInstance();
      	// cal.set(Calendar.HOUR_OF_DAY,0 );
      	//cal.set(Calendar.MINUTE,0 ); 
      	 cal.add(Calendar.HOUR_OF_DAY, -20);
	        for (int i=0; i<children2.length; i++) {
	        	File ff = new File(dir, children2[i]);
	        	if(ff.isFile() && ff.lastModified() < cal.getTimeInMillis()){
	        	  	ff.renameTo(new File(dir+ "/read/" + ff.getName()));
	        	}
	        }
	           
	        docs = new File(dir);
		   
		
		  if( docs.list().length > 2){// kui on peale rename ja error dirri ka midagi sees 
			   eevaeksport.util.ReportingService serv = new ReportingService();
				serv.user =  user;
				serv.pass = pass;
				serv.company = company;
				
				JSONArray doksid = null;
				HashMap malu = new HashMap();
				try {
					try{
						Calendar cals = Calendar.getInstance();
						cals.set(Calendar.HOUR_OF_DAY, 0);
						cals.set(Calendar.MINUTE, 0);
					doksid = serv.getDocumentsShipit(cals.getTimeInMillis());
					 System.out.println("malu " + doksid.toString() + " " + cals.getTimeInMillis());
					
						Converter.saveFile("json" + System.getProperty("file.separator") 
						+ Calendar.getInstance().getTimeInMillis()+ ".txt" , doksid.toString());
			 //   vaatame mis t�nased on 		
				    for (int u =0 ; u< doksid.length();u++) {
		            	org.json.JSONObject cob = doksid.getJSONObject(u);
		            	int docId = (Integer)cob.get("id");
		            	String extID = (String)cob.get("extID");
		            	//org.json.JSONArray fils = (org.json.JSONArray)cob.get("files");
		            	Vector vv = new Vector();
		            	if(malu.get(extID) != null){
		            		vv =(Vector)malu.get(extID);
		            	}
		            	vv.addElement(docId);
		        	malu.put(extID,vv);
		            }
					}catch(Exception r){
						r.printStackTrace();
					}
		            
				//	malu.put("526136", "18431350");
		            System.out.println("malu " + malu.size());
		            Vector uploaded = new Vector();
		            Vector uploadedNot = new Vector();
		            
		           Iterator itera = malu.keySet().iterator();
		            while(itera.hasNext()){
		            	String key  = (String)itera.next();
		            	Vector value = (Vector)malu.get(key); 
		            	String[] children = docs.list();
		            	 
		     	        for (int i=0; i<children.length; i++) {
		     	           File ff = new File(dir, children[i]);
		     	           // siia siis iteratsioon ids.
		     	          Iterator iterax = value.iterator();
		     	          while (iterax.hasNext()){
		     	        	  String did = iterax.next()+ "";
		     	            if(children[i].indexOf(key) != -1 && children[i].endsWith("pdf")){
		     	        if( serv.uploadFile("https://reporting-files.mobitrek.eu/rest/fileUpload/uploadFile/" , ff.getAbsolutePath(), did, "shipit") ){
		     	        	System.out.println("Uploaded " + ff.getName() + " " + ff.getAbsolutePath());
		     	        	uploaded.add(ff.getName());
		     	        }else{
		     	         	//System.out.println("NOT Uploaded " + ff.getName() + " " + ff.getAbsolutePath());
		     	         	uploadedNot.add(ff.getName());
		     	        }
		     	            }
		     	        }
		     	        }
		     	        }
		            
		            serv.closeClient();
		            String[] children = docs.list();
	            	 
	     	        for (int i=0; i<children.length; i++) {
	     	           File ff = new File(dir, children[i]);
	     	        
	     	          File tt = new File(dir+ "/read/" + ff.getName());
	     	        // System.out.println( "uhuuu1 "+  ff.getAbsolutePath() + " " + tt.getAbsolutePath());
	     	          if(uploaded.contains(ff.getName())){
			              	ff.renameTo(tt);
				     	}
			            
			            if(uploadedNot.contains(ff.getName())){
			              	ff.renameTo(tt);
				     	}
			            
			            if(uploadedXML.contains(ff.getName())){
			              	ff.renameTo(tt);
			            }
			        }
	     	        
		            
		            
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		  }
		  }
	 System.out.println( "uhuuu "+  uploadedXML.size());
}
  
  

public static String getYYYYpMMpDDpTimeNiceString(Date dd){
    StringBuffer sb = new StringBuffer();
    Calendar cal= Calendar.getInstance();
    if(dd != null){
    	 cal.setTime(dd);
    }
    
	String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	if (day.length() == 1) {day = "0" + day;}

	String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
	if (month.length() == 1){month = ("0") + month;}

	sb.append("" + cal.get(Calendar.YEAR));
	sb.append("" + month  + "");
	sb.append(day);
    
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
	return null;
   }


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
 
    	   System.exit(0);
       return null;
       }
   
         
        
       public Vector getDebt(String agentCode){
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
	   return tm;
    }
     
   
   public TreeMap getProdGroupsSecond() throws SQLException {
       
		     TreeMap tm = new TreeMap();
		   return tm;
	    }
	   
   
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	

   public static String deCode(String res ){	  
 	return Converter.enCodeEst(res);
 }
   
   public TreeMap getAgents(){
	 
		      TreeMap tm = new TreeMap(); 
          tm.put("Driver", new Agent("Driver"));
       //   sendDocs();
     return tm;
   }
   

       public String getDistribution() {
         return null;
       }

       public  static String firstToUpperCase(String aaa){
	    	 if(aaa == null || aaa.equals("")){
	    		 return aaa;
	    	 }
	    	 return aaa.substring(0,1).toUpperCase() + aaa.substring(1).toLowerCase();
	     }
     
    
      
       public  String getDateDDkMMkYYYYStr(Date cc){
			StringBuffer sb = new StringBuffer();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(cc.getTime());
			
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}
			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}
			sb.append(day +"-");
			sb.append(month + "-");		
			sb.append("" + cal.get(Calendar.YEAR));
		
			return sb.toString();
		}
       
       
             
      
   public TreeMap getCustomers(Agent agent){
	   
	   
	   
	     TreeMap tm = new TreeMap();
	     System.out.println("Kliendid alustan");
	     
	   try {
		  File dir = new File("docs");
		     File[] files = dir.listFiles();
		     for (int i = 0; i < files.length ; i++){
		       File file = files[i];
		   		      if (file.isFile()){
		         String fileName = file.getAbsolutePath();
		         
		      if(fileName.toLowerCase().endsWith(".xml")) {
		    	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    	    DocumentBuilder db = dbf.newDocumentBuilder();
		    		FileInputStream in = getCleanInputStream(file);
		    		String handler = null;
		    	    Document doc = db.parse(in,handler);
		    	    Node root = getNode(doc, "Shipment");
		    	    Node read =  getNode(root, "Customers");

		    	    for (int u = 0; u < read.getChildNodes().getLength(); u++){
		    	          Node rida = read.getChildNodes().item(u);
		    	          if (rida.getNodeName().equalsIgnoreCase("Customer")){
		    	        	 String id = getNode(rida, "CustomerId").getFirstChild().getNodeValue();
		    	             String name = getNode(rida, "Name").getFirstChild().getNodeValue();
		    	 	         String aadr =  "" ;
		    	 	         String linn = "";
		    	 	         
		    	 	         Node aad = getNode(rida, "Address");
		    	 	         if(aad != null && aad.getFirstChild() != null) {
		    	 	        	aadr = aad.getFirstChild().getNodeValue();
			    	 	              linn = aadr.substring(aadr.lastIndexOf(" ")+1);
			    	 	        try{
			    	 	        	  aadr = aadr.substring(0, aadr.lastIndexOf(" "));
			    	 	           }catch(Exception k){}  
		    	 	         }
		    	 	         

		    		          Customer customer = new Customer(id);
		    		         		 customer.restCity = linn;
		    		         		 customer.setCountry("Lithuania");
		    		         		 customer.restStreet = aadr;
		    		         		 customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr), "Lithuania"));
		    		         		 customer.setName(deCode(name));
		    		         		 customer.restPartner = "RIVONA";
		    		         	 tm.put(customer.getID(), customer);
		    	          }
        		    	}
		    	    in.close();}}}
        
	 	
	   }catch(Exception e){
	 		e.printStackTrace();
	 	}
	 			    
	    System.out.println("Kliendid tehtud " + tm.size());
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
   
   private   TreeMap getRoutes(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Routes alustan");
	   
	   try {
		   
		  // loeme xmlist sisse kõik failid 
		   File dir = new File("docs");
		    
		   //  System.out.println("vvv "+ driver.getImportFile());
		     File[] files = dir.listFiles();
		    
		     
		     for (int i = 0; i < files.length ; i++){
		    	
		       File file = files[i];
		       StringBuffer troubles = new StringBuffer();

		       if (file.isFile()){
		         String fileName = file.getAbsolutePath();
		         
		      if(fileName.toLowerCase().endsWith(".xml")) {
		    	  
		    	  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    	    DocumentBuilder db;
		    		
		    			db = dbf.newDocumentBuilder();
		    		
//		    		System.out.println(f.getAbsolutePath());
		    		
		    		FileInputStream in = getCleanInputStream(file);
		    		String handler = null;
		    		
		    	    Document doc = db.parse(in,handler);
		    	    
		    	    Node root = getNode(doc, "Shipment");
		    	   Node idn =  getNode(root, "TruckLp");

          
             if(idn != null && idn.getFirstChild()!= null){
            	String id= idn.getFirstChild().getNodeValue();
             String nimi = id;
	    	     Customer cc = new Customer(id.trim());
 	         		 cc.setName(nimi);
 	         		 cc.restPartner = "RIVONA";
 	         		 tm.put(id.trim(), cc);
 	         		 Customer cc2 = new Customer(id.trim()+"-2");
 	         		 cc2.setName(nimi + "-2");
 	         		 cc2.restPartner = "RIVONA";
 	         		 tm.put(id.trim()+"-2", cc2);
 	         		}
             in.close();
                  } 
		        }
		       }
		       
		                
          
	         	  
	   }catch (Exception e){
      	  e.printStackTrace();
  	   }
	   System.out.println("Routesid kokku " + tm.size() );
	   
	   return tm;
	   }  

   
   
   
   
   private String company = null;
   
   
   
	private Connection getCon() throws SQLException {
		return DriverManager.getConnection(url, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
   
   
   
   public TreeMap getProducts(Agent agent){
	   
    TreeMap tm = new TreeMap();
         return tm;
   }

      
   public String getAssortmentStr(Agent agent){
    return null;
  }

  private Vector custProds = new Vector();
 
  public Vector getCustomerProductsList(String agentCode){
	   return custProds;
   }
     
   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "RivonaLog";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
 

  public double getSalary(String agentCode){
     return 0;
   }

 


   private String subjectFilesTo = null;
   private String         url = "jdbc:odbc:CROR7";

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
	  
}



@Override
public void connectionUp() {
	// TODO Auto-generated method stub
	
}



@Override
public void connectionDown() {
	// TODO Auto-generated method stub
	
}
}
