package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.imp.Order;
import eevaeksport.util.Converter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class RautaLogDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public RautaLogDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
  private Hashtable dates = new Hashtable();
  
 
  
  public String getSaveDebtInfo() {
	 
	  TreeMap custs = this.getCustomers(null);
	  try{
	 	  Converter.sendRoutesJson(getRoutes(),user,pass, rcompany);
	 	 }catch(Exception e){
	 		 e.printStackTrace();
	 	 }
	  
	  try{
 		  Converter.sendPodJson(custs,user,pass, company,false);
 	 }catch(Exception e){
 		 e.printStackTrace();
 	 }
 	 
 	
 	 
 	sendShipitDocs(custs);
	
	  System.exit(0);
   	return null;
   }
  
  
  
  private  void sendShipitDocs(TreeMap custs){
	    
	     System.out.println("Doc alustan");
	   
	     HashMap prio = new HashMap();
		try {
			prio = loadPriors();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	     
	     String user =  getParent().getProps().getProperty("rest.user");
	     String pass = getParent().getProps().getProperty("rest.pass");
	     String company = getParent().getProps().getProperty("rest.company");
         int cou = 0;
         Vector<Order> res = new Vector<Order>();
	     
	   try {
			  File f = new File (getDir() + "mp_saateleht_trans.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root =  getNode(doc, "mp_saatelehed");     
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	  cou ++;
	    	    try{
	    	    	if(rida.getNodeName().equals("sl")){
	
	    	    	 String custid =  getNode(rida,"mp_id").getFirstChild().getNodeValue();
	    	    	 String  routes = null;
	    	    	 String  prior = null;
	    	    	 if(custs.get(custid) != null){
	    	    		 Customer cc = (Customer)custs.get(custid);
	    	    		// prior = cc.restPriority;
	    	    		 // see tuleb kylge Webist kui ei pane 
	    	    	 }
	    	    	 if(prio.get(custid)!= null ){
	    	    		 prior = prio.get(custid)+"";
	    	    	 }
	    	    	// System.out.println(prior); 
	         		 String doknr =  null ;
	         		 try{
	         			 doknr = getNode(rida,"doknr").getFirstChild().getNodeValue();
	 	         	 }catch(Exception k){
	         			 
	         		 }
	         	
	         		 routes = getNode(rida,"mp_marsruut_kd").getFirstChild().getNodeValue();
	         		 Date deldate  =  getDate2(getNode(rida,"kuup").getFirstChild().getNodeValue(),0);
	         		 Vector<Product> lines = new Vector<Product>();
	         		 Product pr = new Product("kast");
	         		 double kogus = 1;
	         		 try{
	         			 kogus = Double.parseDouble(getNode(rida,"kogus").getFirstChild().getNodeValue());
	         		 }catch(Exception e){
	         			 
	         		 }
	         		 pr.setInCases(kogus);
	         		 lines.add(pr);
	         		 Calendar ccc = Calendar.getInstance();
	         		 Date now = new Date(ccc.getTimeInMillis());
	         		
	         		 
	         		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
	         		 
	         		
	         		 
	         		// if(now.before(deldate) && now2.after(deldate)){
	         			 if(fmt.format(deldate).equals(fmt.format(now))){
	     	         			
	         		Order shipit = new Order(custid,"autosender");
	         			shipit.prodLines = lines;
	         			shipit.docType = "SHIPIT";
	         			shipit.origId = null;
	         			shipit.completedMsisdn = null;
	         			shipit.completedDate = null;
	         			shipit.completedComment = null;
	         			shipit.deliveryDate = deldate;
	         			shipit.extDoc = doknr;
	         			shipit.restPriority = prior;
	         			shipit.route =  routes;
	         			shipit.partner= "LEHEPUNKT";
	         		 
	         			 res.add(shipit);
		         	  }
	         		
	    	    	}
	         		
          	  }catch (Exception e){
                 e.printStackTrace();	
	         	  }
	          }
	 
	          in.close();
	         	
	        System.out.println("Dokumente postima " + res.size() );
	        Converter.sendShipitListJson(res, user, pass, company) ;
	          
	   }catch (Exception e){
   	  e.printStackTrace();
	  }
	   System.out.println("Dokumente kokku " + cou );
	
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



public static String getYYYYpMMpDD(Date dd){
    StringBuffer sb = new StringBuffer();
    Calendar cal= Calendar.getInstance();
    if(dd != null){
    	 cal.setTime(dd);
    }
    
    
	String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	if (day.length() == 1) {day = "0" + day;}

	String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
	if (month.length() == 1){month = ("0") + month;}

	String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY ));
	if (hour.length() == 1){hour = ("0") + hour;}

	String min = String.valueOf(cal.get(Calendar.MINUTE ));
	if (min.length() == 1){min = ("0") + min;}
	
	sb.append("" + cal.get(Calendar.YEAR));
	sb.append("-" + month  + "-");
	sb.append(day);
	sb.append("T" + hour  + ":" + min+ ":00" );
	
	//2003-11-05T00:00:00
    
    return sb.toString();
  }


  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	private HashMap hinnad = new HashMap();

	private HashMap kliendialed = new HashMap();
	private Vector matrixlines = new Vector();
	
   public Vector getPriceMatrixData() {
     System.out.println("Hinnamaatrixi suurus " + matrixlines.size() );
	return matrixlines;
   }

   
   private Vector hasCamp( TreeMap ketidKlient ,TreeMap kampid){
	  
	   Vector res = new Vector();
	   
		   //kï¿½ime kï¿½ik kampaaniad lï¿½bi ja vaatame keti ja toote vastavuste
		   Iterator itera = matrixlines.iterator();
		   while (itera.hasNext()){
			   MatrixLine kamp = (MatrixLine)itera.next();
			   // siin on Exceli kett sees 
			   String excelkett = kamp.getCliGroup();
			   String exelprod = kamp.getProdID();
			   
			   //teema vaatluse kï¿½igist custid-dest exeli kaudu 
			   Vector vv = new Vector();
			   Iterator itert = excelketid.keySet().iterator();
			   while (itert.hasNext()){
				   String exccustid = (String)itert.next();
				   if(excelketid.get(exccustid) != null){
					   if(kettKlient.get(exccustid) != null){
						   res.add(kettKlient.get(exccustid)+exelprod );
					   }
				   }
			   }
			   
			   
			   
			   
		   }
	//	System.out.println("kampaniad on " + res.toString());
	   return res ;
   }
   
   
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		this.getProducts(null);
		
		TreeMap kampid = this.getKampaaniad();
		
		Vector hascamp =  hasCamp(kettKlient,kampid);
		
		//System.out.println("Tere olen sees FIXED  " + prodsx.size() + " " );
		TreeMap pre = new TreeMap();
     String[] list = new File(getDir()).list();
	        for(int u = 0 ; u < list.length ; u++){
			  String fname = list[u];
		
		if(fname.startsWith("va_mp_kett") ){
		try{
		
		  File f = new File (getDir() + fname);
		
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    
	     DocumentBuilder db = dbf.newDocumentBuilder();  
	     
	     
   	FileInputStream in = getCleanInputStream(f);
   	String handler = null;
       Document doc = db.parse(in,handler);
      
       in.close();
       Node root = getNode(doc, "va_mp_ketid");     
       HashSet done = new HashSet();
  
       for (int i = 0; i < root.getChildNodes().getLength(); i++){
    	
       	  Node rida = root.getChildNodes().item(i);
		
       	 try{
 	    	if(rida.getNodeName().equals("va_mp_kett")){
 	    	  String custID =  getNode(rida,"mp_kett_kd").getFirstChild().getNodeValue();
       		 String prodID = getNode(rida,"va_id").getFirstChild().getNodeValue();
       		 String date = getNode(rida,"muutmiskuup").getFirstChild().getNodeValue();
       		String lubatud = getNode(rida,"lubatud_tun").getFirstChild().getNodeValue();
      		 
       		
       		  Date dd = getDate2(date,90);
       		MatrixLine mline = new MatrixLine();
	         if (custID != null && custID.trim().length() > 0) {
				mline.setCliGroup(custID.trim());
			}
	         String lineinfo = " ";
	         boolean go = false;
	         if(dd.after(new Date())){
		    	   mline.setLineinfo("NEW");
		    	   lineinfo = " .NEW";
		    	   go = true;
		    	//   System.out.println("Uus" + custID + " " + prodID + " " + dd.toString());
		       }

	          
	         
	         if(lubatud.equals("J")){

	        	 
			       if (true || (prodID != null	&& prodID.trim().length() > 0 && myydud.contains(prodID))  || go) {
			    	   if( prodsx.get(prodID.trim()) != null){
			    		   String kood = prodsx.get(prodID.trim()) + "";
				    	  	mline.setProdID( kood);  


			    	  		
				    	  	if(!done.contains(kood+custID) && ((Product)prodHash.get(kood)) != null){
				    	  		//System.out.println("Asi korras pole veel " + custID + kood);
				    	  		String kamp = "";
				  	            	        	
				    	  		if(hascamp.contains(custID + kood)){
				  	        	  kamp = " ";

				  	          }
				    	  		

					     /*   	 if(custID.equals("RKIOSK"))
					    	  			System.out.println("Asi korras " + custID + "  " + kood  + "  "+ prodID.trim() + "\n"  +
					    	  					custID +  lineinfo 
						    	  				 );
						   */ 	  	
				    	  		
				    	  		 pre.put(custID + kamp + lineinfo +
				    	  				 ((Product)prodHash.get(kood))
				    	  				 .getNameSorting().trim() 
				    	  				 ,mline);

				    	  		 
				    	  		 done.add(kood+custID);
				    	  	}
				        }
			    	} 
	         }
		    }
      	  }catch (Exception e){
          e.printStackTrace();	
      	  }
       }
       
       doc = null;
}catch(Exception d){
			d.printStackTrace();
		}
		}
	        }
	       // Paneme ka multi tooted sortimentidesse CUSTID kaudu 
	     try{   
	       Enumeration enumes =  this.multJTooted.keys();
	       
	       while (enumes.hasMoreElements()){
	    	String custID = enumes.nextElement() +"";
	    	TreeMap vvv = multJTooted.get(custID);
	    	Iterator itera = vvv.keySet().iterator();
	    	while(itera.hasNext()){
	    		String prodID = itera.next() + "";
	    		String lineinfo = "";
	    		MatrixLine mline = new MatrixLine();
		        	mline.setCustID(custID.trim());
		        	mline.setLineinfo("MULTI " + vvv.get(prodID));
		        	lineinfo = "  .NEW";
		        	String kood = prodsx.get(prodID.trim()) + "";

	  	          	mline.setProdID( kood); 
		    	  	String name = " "; 
		    	  	try{
		    	  	  	name = ((Product)prodHash.get(kood)).getNameSorting().trim();
			     	}catch(Exception r){
		    	  	}
		    	 pre.put(custID  + lineinfo + name ,mline);
	 		  }			  
	       }
	       
   }catch (Exception e){
       e.printStackTrace();	
   	  }
	        
	        Iterator itera = pre.keySet().iterator();
	        while(itera.hasNext()){
	        	String ite = itera.next() +"";
	     
	        	  result.add((MatrixLine) pre.get(ite));
	        	  if(ite.indexOf("ETKX") != -1){
	        		//  System.out.println(ite);
	        	  }
	        }
	
	        
	      //  result =  sortLinesByName(result,prodHash);
	       // System.out.println("Fix suurus " + result.size() + " " + myydud.size());
	        pre = null;
	        return result;

	}

  
   
 

   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
 return null;
   }

  private Hashtable<String,Double> tagKl = null;
  private Hashtable<String,Double> tagAg = null;
  private Hashtable<String,Double> tagKokku = null;
  private Hashtable<String,Double> hindKlKaan = null;
  private Hashtable<String,Vector<String>> sortSuurKl = null;
  private Hashtable<String,TreeMap<String,String>> multJTooted = null;
  
  
  private Hashtable<String,Double> myykKlgruppKokku = null;
  private Hashtable <String,Hashtable<String,Double>> myykKlgruppAgent = null;
 
       public String getSaveStockInfo(){
    	   getSalesData();
    	   getMultiJinfo();
    	 
    	   getCustomerProductsListVec();
    	   System.exit(0);
       return null;
       }
       
       private double calculateAverage(List <Double> marks) {
    	   double sum = 0;
    	   if(!marks.isEmpty()) {
    		   try{
    			   for (Double mark : marks) {
    	    	         sum += mark;
    	    	     }
    		   }catch(Exception e){
    			 marks.toString();
    			 e.printStackTrace();
    		   }
    	    
    	     return sum / marks.size();
    	   }
    	   return sum;
    	 }
       
       
       private Hashtable<String,Double> calculateShare(Hashtable <String,Double> asi ) {
    	  
    	   Hashtable<String,Double> aa = new Hashtable<String,Double>();
    	double sum = 0;
      Enumeration enumer = 	asi.keys();
      while (enumer.hasMoreElements()){
    	  sum = sum + asi.get(enumer.nextElement());
      }
    enumer = 	asi.keys();
      while (enumer.hasMoreElements()){
    	  String key = enumer.nextElement()+ "";
    	 aa.put(key, (asi.get(key)/ sum *100));
      }
   
    	   return aa;
    	 }
       
     
       
       private double calculateWeightAverage(List <String> marks) {
    	   double sum = 0;
    	   double summa = 0;
    	   Hashtable<String,Double> set = new Hashtable<String,Double>();
    	   if(!marks.isEmpty()) {
    		   try{
    			   for (String mark : marks) {
    				   
    				    String hind = mark.substring(0,mark.indexOf("|"));
    				    String kogus = mark.substring(mark.indexOf("|")+1);
    				    double reasum = Double.parseDouble(kogus)*Double.parseDouble(hind);
    				    summa += reasum ;
    				    if(set.get(hind) != null){
    				    	set.put(hind, set.get(hind) + reasum);
    				    }else{
    				    	set.put(hind, reasum);
    				    }
    	    	   
    			   }
    			   
    		   }catch(Exception e){
    			 marks.toString();
    			 e.printStackTrace();
    		   }
    		   
    		   // set is on hind ja tema vï¿½ï¿½rtus - tuleb siis leida iga hinna kaal % 
    		   // 
    		   
    		  Enumeration enumx = set.keys();
    		  double weight = 0;
    		  while (enumx.hasMoreElements()){
    			  String asi = enumx.nextElement() + "";
    			  double hind = Double.parseDouble(asi);
    			  double sums = set.get(asi);
    			  weight += hind * sums;
    			  
    		  }
    	    
    	     return weight / summa;
    	   }
    	   return sum;
    	 }
       
       String period1 = null;
       String period2 = null;
 
       public DecimalFormat cf = (DecimalFormat) NumberFormat
		.getCurrencyInstance();
       
     
		NumberFormat pf = NumberFormat.getPercentInstance();
		HashSet myydud = null;
       
		
	private void 	getMultiJinfo(){
		multJTooted = new Hashtable<String,TreeMap<String,String>>();
	    

		   String[] list = new File(getDir()).list();
		
		  for(int u = 0 ; u < list.length ; u++){
			
			  String fname = list[u];  
	try{
		 if(fname.indexOf("tj_") != -1 ){
			  File f = new File (getDir() + fname);
			 TreeMap<String,String> prods = new TreeMap<String,String>();
			 String mpid = null;
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          in.close();
	              
	          Node root =  getNode(doc, "tjd");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    
	    	    	if(rida.getNodeName().equals("tj")){
	    	    		      mpid = getNode(rida,"mp_id").getFirstChild().getNodeValue();
	    	     		String vaid = getNode(rida,"va_id").getFirstChild().getNodeValue();
	    		       try{
	    		    	   String jaot = getNode(rida,"sl_jaotus").getFirstChild().getNodeValue();
	    		    	   String asi = "";
	    		    	   
		    		        if(jaot.length() > 0){
		    		        	try{
		    		        		String algus = getNode(rida,"sl_pv_arv").getFirstChild().getNodeValue();
				    		    	 	asi = algus + "p ";
		    		        	 }catch (Exception r){
		    		    		}
	

		    		        	try{
			    		    		   String algus = getNode(rida,"tell_akp").getFirstChild().getNodeValue();
				    		    	   String lopp = getNode(rida,"tell_lkp").getFirstChild().getNodeValue();
				    		    	
				    		    	   if(getDate(algus).after(new Date()) && getDate(lopp).before(new Date())  ){
				    		    		  asi = asi + "(" + algus.substring(5) + "->" + lopp.substring(5) + ")";
				    		    	  }
				    		    	   
			    		    	   }catch (Exception r){
			    		    		}
		    		        	
		    		        	prods.put(vaid,(jaot + "% " + asi).trim() );
		    		        }   
	    		       }catch(Exception e){
	    		    	   
	    		       }
	    	     		
	    		        	   
	    	    	}
	    	    	  
			  }
	          if(prods.size() > 0){
	        	  this.multJTooted.put(mpid, prods);
	          }
	          
		  }
		  

	}catch(Exception m ){
      m.printStackTrace();		
	}
			 	  }
		
		
		 System.out.println("Multi tooted " + multJTooted.size()) ;
		 try{
			   saveObject(this.multJTooted, "multJTooted.vec"); 
			   
			   
		   }catch(Exception e){
			  e.printStackTrace(); 
		   }
		
		}
		
		
       private void getSalesData(){
    	   Currency c  = Currency.getInstance("EUR");
    	   cf.setCurrency(c);
    	   pf.setMinimumFractionDigits(2);
    	   pf.setMaximumFractionDigits(2);
    	   myydud = new HashSet();
	
	  TreeMap tm = new TreeMap();
	     System.out.println("Müügiandmed alustan");
	   
	    //tee listid ja lï¿½pus arvuta keskmine la pan objekti 
	     
	   try {
		   String ymonth = Converter.getYYYYMMString(-1);
		   String ymonthl = Converter.getYYYYMMString(-2);
		  
		   getProducts(null);
		   getCustomers(null);
		   String[] list = new File(getDir()).list();
			
		   // 1. kliendi tagastused
	    
		    tagKl = new Hashtable<String,Double>();
		    tagAg = new Hashtable<String,Double>();
		    tagKokku = new Hashtable<String,Double>();
		    hindKlKaan = new Hashtable<String,Double>();
		    sortSuurKl = new Hashtable<String,Vector<String>>();
		    myykKlgruppAgent = new Hashtable <String,Hashtable<String,Double>>();
		    myykKlgruppKokku = new Hashtable<String,Double>();
		    
		  
		    Hashtable<String,Hashtable <String,List<Double>>>    tagKlo = new Hashtable<String,Hashtable <String,List<Double>>>();
		    Hashtable<String,Hashtable <String,List<Double>>>   tagAgo = new Hashtable<String,Hashtable <String,List<Double>>>();
		    Hashtable<String,List<Double>>  tagKokkuo = new Hashtable<String,List<Double>>();
		    Hashtable<String,Hashtable <String,List<String>>>   hindKlKaano = new Hashtable<String,Hashtable <String,List<String>>>();
		    Hashtable<String,Double>   myykKlgruppKokkuo = new Hashtable<String,Double>();
		    Hashtable<String,Double>   myykAgentEelmKokkuo = new Hashtable<String,Double>();
		    Hashtable <String,Hashtable<String,Double>>   myykKlgruppAgento
		       = new Hashtable <String,Hashtable<String,Double>>();
			   // agent- > klient - > grupp + grmyyk  
			  for(int u = 0 ; u < list.length ; u++){
				  String fname = list[u];  
		
			//	  System.out.println("ymonth" + ymonth + " "+ ymonthl + " " + fname);
				  if(fname.indexOf("mp_va_myyk_" + ymonth) != -1 || fname.indexOf("mp_va_myyk_" + ymonthl) != -1){
					  File f = new File (getDir() + fname);
					  boolean lastm = false;
					  if(fname.indexOf(ymonth) != -1){
						  lastm = true;
					  }
			  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			  	     DocumentBuilder db;
			  		db = dbf.newDocumentBuilder();      	
			      	FileInputStream in = getCleanInputStream(f);
			      	String handler = null;
			          Document doc = db.parse(in,handler);
			          in.close();
			              
			          Node root =  getNode(doc, "mp_va_myygid");     
			          
			          for (int i = 0; i < root.getChildNodes().getLength(); i++){
			        	  Node rida = root.getChildNodes().item(i);
			        	
			    	    try{
			    	    	if(rida.getNodeName().equals("mp_va_myyk")){
			    	    	  String id =  getNode(rida,"rid").getFirstChild().getNodeValue();
			    	    	  String per =  getNode(rida,"per_kd").getFirstChild().getNodeValue();
			    	    	  String klient =  getNode(rida,"mp_id").getFirstChild().getNodeValue();
			    	    	  String toode =  getNode(rida,"va_id").getFirstChild().getNodeValue();
			    	    	  double jaehind = Double.parseDouble(getNode(rida,"kh").getFirstChild().getNodeValue());
			    	    	  double omahind = Double.parseDouble(getNode(rida,"oh").getFirstChild().getNodeValue());
			    	    	  double hind = Double.parseDouble(getNode(rida,"mh").getFirstChild().getNodeValue());
			    	    	  double km = Double.parseDouble(getNode(rida,"km_prots").getFirstChild().getNodeValue());
			    	    	  int  sisse = Integer.parseInt( getNode(rida,"kos").getFirstChild().getNodeValue());
			    	    	  int  tagasi = Integer.parseInt( getNode(rida,"kot").getFirstChild().getNodeValue());
			    	    	  int  number = Integer.parseInt( getNode(rida,"va_nr_arv").getFirstChild().getNodeValue());
			    	    	  int  kogunumber = Integer.parseInt( getNode(rida,"mp_va_nr_arv").getFirstChild().getNodeValue());
		                      String liik = (String)yldgrupid.get(toode);
		                      if(liik == null){
		                    	//  System.out.println("klient " + klient + " " + toode);
		                      }
			    	    	  double myyk = sisse-tagasi;
			    	    	  double tag = 0;
			    	    	  
			    	    	  String agent = null ;
			    	    	  
			    	    	  
			    	    	  if ( custAgent.get(klient) != null){
			    	    		  agent = custAgent.get(klient)+ "";
			    	    	  }
			    	    	  
			    	    	  if(agent != null && lastm){
			    	    		  
			    	    		  if(myykAgentEelmKokkuo.get(agent) != null){
				                    	double marks = myykAgentEelmKokkuo.get(agent);
				                    	marks = marks + (myyk * hind / (1 + km)) ;
				                    	myykAgentEelmKokkuo.put(agent, marks);
				                    }else{
				                    	double marks = (myyk * hind / (1 + km)) ;
				                    	myykAgentEelmKokkuo.put(agent, marks);   
				                    }
			    	    		  
			    	    	  }
			    	    	  
			    	    	  try{
			    	    		  tag = tagasi / sisse ;
			    	    	  }catch(Exception e){
			    	    		  
			    	    	  }
			    	    	  
			    	    	  if(!myydud.contains(toode)){
		                    		myydud.add(toode);
			                    }
			    	    //	  System.out.println("asi " + per + " "+ klient);  	    	  
			    	    	  
		                    if(tagKokkuo.get(per) != null){
		                    	List <Double> marks = tagKokkuo.get(per);
		                    	marks.add(tag);
		                    	tagKokkuo.put(per, marks);
		                    }else{
		                    	List <Double> marks = new ArrayList <Double>();
		                    	marks.add(tag);
		                    	tagKokkuo.put(per, marks);   
		                    }
		                  
		                    if(liik != null){
		                    	 if(myykKlgruppKokkuo.get(liik) == null ){
				                    	myykKlgruppKokkuo.put(liik,(myyk * hind / (1 + km)) );
				                    }else{
				                    	double olemas = myykKlgruppKokkuo.get(liik);
				                    	myykKlgruppKokkuo.put(liik,(myyk * hind / (1 + km)) + olemas);
				                    }
		                    }
		                   
		                    
		                    
		                    
		    if(liik != null){
		        if(myykKlgruppAgento.get(klient) == null ){
			    	//kui ï¿½ldse pole agendil andmeid 
			      Hashtable<String,Double> hhh = new Hashtable<String,Double>();
			                    	hhh.put(liik,(myyk * hind / (1 + km)) );
			                        myykKlgruppAgento.put(klient, hhh)  ;  
			            }else{
			                    
			            	//agendil olemas kaks juhtumit edasi 
			            	 Hashtable<String,Double>hhh = myykKlgruppAgento.get(klient);
			            	    if(hhh.get(liik) != null){
			            	    	double asi = hhh.get(liik);
			            	    	hhh.put(liik,(myyk * hind / (1 + km)) + asi);
			            	    }else{
			            	    	hhh.put(liik,(myyk * hind / (1 + km)) );
			            	    }
			            	    myykKlgruppAgento.put(klient, hhh);	
			                  }
			                    
			                   	
		    }
		 
		                    
		                    
		                    
		                    if(prodsx.get(toode) != null){
		                    	//System.out.println("aaaaa ");
		                    	
		                    	String tkood = prodsx.get(toode) + "";
				                   if(sortSuurKl.get(per + "-" + klient ) != null){
				                    	 Vector<String>  marks = sortSuurKl.get(per + "-" + klient);
				                    	  if(!marks.contains(tkood)){
				                    		  marks.add(tkood);
				                    	  }
				                    	  sortSuurKl.put(per + "-" + klient, marks);
				                    }else{
				                    	Vector<String>  marks = new Vector<String>();
				                    	   marks.add(tkood);
				                    	   sortSuurKl.put(per + "-" + klient, marks); 
				                    }
		                    }
		                 
		                    
		                    
		                //  kliendi pï¿½hise tagastuse array tekitamine
		                    if(tagKlo.get(per) != null){
		                    	Hashtable <String, List<Double>>  marks = tagKlo.get(per);
		                    	if(marks.get(klient) != null){
		                    		List <Double> li = (List<Double>) marks.get(klient);
		                    		li.add(tag);
		                    		marks.put(klient, li);
		                    	}else{
		                    		List <Double> li = new ArrayList <Double>();
		                    		li.add(tag);
		                    		marks.put(klient, li);
		                    	}
		                    	tagKlo.put(per, marks);
		                    }else{
		                    	Hashtable <String, List<Double>>  marks = new Hashtable <String, List<Double>>();
		                    	    List <Double> li = new ArrayList <Double>();
		                    		li.add(tag);
		                    		marks.put(klient, li);
		                    	    tagKlo.put(per, marks);  
		                    }
		                    
		                    
		                    if(hindKlKaano.get(per) != null){
		                    	Hashtable <String, List<String>>  marks = hindKlKaano.get(per);
		                    	if(marks.get(klient) != null){
		                    		List <String> li = (List) marks.get(klient);
		                    		li.add(jaehind + "|" +myyk);
		                    		marks.put(klient, li);
		                    	}else{
		                    		List <String> li = new ArrayList <String>();
		                    		li.add(jaehind + "|" +myyk);
		                    		marks.put(klient, li);
		                    	}
		                    	hindKlKaano.put(per, marks);
		                    }else{
		                    	Hashtable <String, List<String>>  marks = new Hashtable <String, List<String>>();
		                    	    List <String> li = new ArrayList <String>();
		                    	    li.add(jaehind + "|" +myyk);
		                    		marks.put(klient, li);
		                    		hindKlKaano.put(per, marks);  
		                    }
		                    
		                  
	                    	if(agent != null){
	                    		
	                    		  if(tagAgo.get(per) != null){
	  		                    	Hashtable <String, List<Double>>  marks = tagAgo.get(per);
	  		                    	
	  		                    	if(marks.get(agent) != null){
	  		                    		List <Double> li = (List<Double>) marks.get(agent);
	  		                    		li.add(tag);
	  		                    		marks.put(agent, li);
	  		                    	}else{
	  		                    		List <Double> li = new ArrayList <Double>();
	  		                    		li.add(tag);
	  		                    		marks.put(agent, li);
	  		                    	}
	  		                    	tagAgo.put(per, marks);
	  		                    }else{
	  		                    	Hashtable <String, List<Double>>  marks = new Hashtable <String, List<Double>>();
	  		                    	    List <Double> li = new ArrayList <Double>();
	  		                    		li.add(tag);
	  		                    		marks.put(agent, li);
	  		                    		tagAgo.put(per, marks);  
	  		                    }
	  		                    
	  		                  	
	                    	}
			    	    	  
			    	    	}
			         		
		        	  }catch (Exception e){
		        		  e.printStackTrace();	
			            }
			            }
			             
			             doc = null;
					    }
				  }
			  
			  
			  
			  try{
				   saveObject(this.sortSuurKl, "sortSuurKl.vec"); 
				   saveObject(myydud, "myydud.vec"); 
				   saveObject(myykAgentEelmKokkuo, "myykAgentEelmKokku.vec"); 
				   
			   }catch(Exception e){
				  e.printStackTrace(); 
			   }
			  
			  //siin arvuta keskmised ja ï¿½ldobjekti 
			  
		   if(tagKokkuo.size() > 0){
			 Enumeration enume =   tagKokkuo.keys(); 
			 while(enume.hasMoreElements()){
				 String nn = (String)enume.nextElement();
				 tagKokku.put(nn , this.calculateAverage((List)tagKokkuo.get(nn)));
			 }
			 try{
				   saveObject(tagKokku, "tagKokku.vec"); 
			   }catch(Exception e){
				  e.printStackTrace(); 
			   }
			
		   }
		   
		 
		   if(myykKlgruppKokkuo.size() > 0){
			   myykKlgruppKokkuo = calculateShare(myykKlgruppKokkuo);
			   
				 try{
					   saveObject(myykKlgruppKokkuo, "myykKlgruppKokku.vec"); 
				   }catch(Exception e){
					  e.printStackTrace(); 
				   }
				
			   }
		   
		   
		   if(myykKlgruppAgento.size() > 0){
				 Enumeration enume =   myykKlgruppAgento.keys(); 
				  while(enume.hasMoreElements()){
					 String klient = (String)enume.nextElement();
					 Hashtable hhh = this.calculateShare(myykKlgruppAgento.get(klient));
					// System.out.println("myyk kokku " + hhh.toString());
					 myykKlgruppAgent.put(klient , hhh);
				 }
				 try{
					   saveObject(myykKlgruppAgent, "myykKlgruppAgent.vec"); 
				   }catch(Exception e){
					  e.printStackTrace(); 
				   }
				
			   }
		   
		   
		   if(tagKlo.size() > 0){
				 Enumeration enume =   tagKlo.keys(); 
				 while(enume.hasMoreElements()){
					 String nn = (String)enume.nextElement();
					 Hashtable hh = tagKlo.get(nn);
					 Enumeration enumea =   hh.keys(); 
					 while(enumea.hasMoreElements()){
					   String kl = (String)enumea.nextElement();
						 tagKl.put(nn+"-"+kl , this.calculateAverage((List)hh.get(kl)));
						
					 }
				  }
				 try{
					   saveObject(tagKl, "tagKl.vec"); 
				   }catch(Exception e){
					  e.printStackTrace(); 
				   }
				 
			   }
		   
		  
		   
		   
		   if(hindKlKaano.size() > 0){
				 Enumeration enume =   hindKlKaano.keys(); 
				 while(enume.hasMoreElements()){
					 String nn = (String)enume.nextElement();
					 Hashtable hh = hindKlKaano.get(nn);
					 Enumeration enumea =   hh.keys(); 
					 while(enumea.hasMoreElements()){
					   String kl = (String)enumea.nextElement();
					  // System.out.println("kl " + kl + " " + hh.get(kl).toString() );
					   hindKlKaan.put(nn+"-"+kl , this.calculateWeightAverage((List)hh.get(kl)));
					 }
				  }
				 try{
					   saveObject(hindKlKaan, "hindKlKaan.vec"); 
				   }catch(Exception e){
					  e.printStackTrace(); 
				   }
			   
			   }
		   

		  
		   
		   if(tagAgo.size() > 0){
				 Enumeration enume =   tagAgo.keys(); 
				 while(enume.hasMoreElements()){
					 String nn = (String)enume.nextElement();
					 Hashtable hh = tagAgo.get(nn);
					 Enumeration enumea =   hh.keys(); 
					 while(enumea.hasMoreElements()){
					   String kl = (String)enumea.nextElement();
					   tagAg.put(nn+"-"+kl , this.calculateAverage((List)hh.get(kl)));
					 }
				  }
				 try{
					   saveObject(tagAg, "tagAg.vec"); 
				   }catch(Exception e){
					  e.printStackTrace(); 
				   }
			   }
		   
		  
		    		
		   
	   }catch (Exception e){
 	    e.printStackTrace();
	  }
	   System.out.println("Mï¿½ï¿½giandmed lï¿½petasin " + myydud.size());
	  
	   
  }
       
       public  Object readObject(String s) throws IOException,
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
       
       
       
       private HashMap loadPriors() throws IOException{
        HashMap re = new HashMap();
   		File file = new File( "priors.txt");
   		if (file.exists()){
   		
   			FileReader ff = new FileReader(file);
   			BufferedReader br = new BufferedReader(ff);
   			String line = null;
   			while( (line = br.readLine()) != null){
   				StringTokenizer st = new StringTokenizer(line, ";");
   				if (st.hasMoreTokens()){
   					String prodID = st.nextToken().trim();
   					try{
   						String name = (st.nextToken().trim());
   						if(name.length() > 0)
   	   					re.put(prodID, name);
   					}catch(Exception r){
   					//	r.printStackTrace();
   					}
   					}
   				
   				}
   			
   		ff.close();
   		br.close();
   		}
   	System.out.println("priors " +  re.size());
   		return re;
   	}

  Hashtable custAgent = new Hashtable();
  Vector debts = null;
  
   public Vector getDebt(String agentCode){
	 	 		
      return debts;
   }
   private int days(Date date){
	   int i = 0;
	   long diff =  new Date().getTime() -  date.getTime()  ;
	   i = (int) (diff / 86400000 );
	   return i;
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
	     TreeMap tm = new TreeMap();
	     System.out.println("Grupid alustan");
	   
	   try {
			  File f = new File (getDir() + "va_kategooria.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          in.close();
	              
	          Node root =  getNode(doc, "va_kategooriad");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("va_kategooria")){
	    	    	  String id =  getNode(rida,"va_kategooria_kd").getFirstChild().getNodeValue();
 	         		 String nimi = getNode(rida,"nimi").getFirstChild().getNodeValue();
 	         		 tm.put(id, nimi);	
	    	    	}
	         		
             	  }catch (Exception e){
             		
	             e.printStackTrace();	
	         
	         	  }
	          }
	          doc = null;
	         	  
	   }catch (Exception e){
      	  e.printStackTrace();
  	  }
	  // System.out.println("Groups-s "+ tm.toString());
	   return tm;
    }
   
   
   
   private Hashtable secGroups = new Hashtable();

   
  private TreeMap getJooksevMyyk(boolean jooksev){
	       
		     TreeMap tm = new TreeMap();
		     System.out.println("Jooksev kuu myyk alustan");
		   
		   try {
				  File f = new File (getDir() + "me_myyk.xml");
		  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     DocumentBuilder db;
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		          Document doc = db.parse(in,handler);
		              
		          Node root1 =  getNode(doc, "me_myygid");     

		          for (int i = 0; i < root1.getChildNodes().getLength(); i++){
		        	  Node root = root1.getChildNodes().item(i);
			     //   System.out.println(root.getNodeName());
		        	  if(root.getNodeName().equals("periood")){
		        		  

		    	    		String algus =  getNode(root,"alguskuup").getFirstChild().getNodeValue();
                   //    System.out.println("algus " + algus);
                       
                       
		          for (int r = 0; r < root.getChildNodes().getLength(); r++){
		        	  Node rida = root.getChildNodes().item(r);
		        	
		    	    try{
		    	    	if(rida.getNodeName().equals("me_myyk")){
		    	    		
			    	    	  String id =  getNode(rida,"myygiesindaja_kd").getFirstChild().getNodeValue();
			    	    	  String nimi = getNode(rida,"summa_mh").getFirstChild().getNodeValue();
			    	    	  //System.out.println(nimi);
			    	    	  try{
			    	    		  nimi = nimi.substring(0,nimi.indexOf("."));
					    	    		  
			    	    	  }catch(Exception k ){
			    	    		  
			    	    	  }
			    	    	  
		    	    		if(getDate2(algus,0).getMonth() == (new Date()).getMonth() && jooksev){
		    	    			 //jooksev kuu 
                                 tm.put(id, nimi);	
		    	    		 }else if (getDate2(algus,0).getMonth() != (new Date()).getMonth() && !jooksev){
				    	    	 tm.put(id, nimi);
		    	    		 }
		    	    		
		    	    	}
		         		
	             	  }catch (Exception e){
	             	 e.printStackTrace();	
		           }
		          }
		        	  }
		          }
		          in.close();
		          doc = null;
		         	  
		   }catch (Exception e){
	      	  e.printStackTrace();
	  	  }
		  System.out.println("Jooksvad myygid " + jooksev + " " + tm.toString());
		   return tm;
	    }
  
  
  
  
   
   public TreeMap getProdGroupsSecond() throws SQLException {
       
		     TreeMap tm = new TreeMap();
		     System.out.println("GrupidSec alustan");
		   
		   try {
				  File f = new File (getDir() + "va_liik.xml");
		  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	     DocumentBuilder db;
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		          Document doc = db.parse(in,handler);
		              
		          Node root =  getNode(doc, "va_liigid");     
		          
		          for (int i = 0; i < root.getChildNodes().getLength(); i++){
		        	  Node rida = root.getChildNodes().item(i);
		        	
		    	    try{
		    	    	if(rida.getNodeName().equals("va_liik")){
		    	    	  String id =  getNode(rida,"va_liik_kd").getFirstChild().getNodeValue();
		    	    	  String nimi = getNode(rida,"nimi").getFirstChild().getNodeValue();
		    	    	  tm.put(id, nimi);	
		    	    	  try{
		    	    		  secGroups.put(id,getNode(rida,"va_liik_kd_ylem").getFirstChild().getNodeValue());
				    	    		  
		    	    	  }catch(Exception e){
		    	    		//  e.printStackTrace();
		    	    	  }
		    	    	}
		         		
	             	  }catch (Exception e){
	             		
		             e.printStackTrace();	
		         
		         	  }
		          }
		          in.close();
		          doc = null;
		         	  
		   }catch (Exception e){
	      	  e.printStackTrace();
	  	  }
		 //  System.out.println("GroupsSec-s "+ tm.toString());
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
   private  TreeMap agents1 = new TreeMap(); 
   private  TreeMap agents2 = new TreeMap(); 
   
   public TreeMap getAgents(){
	 
		      TreeMap tm = new TreeMap(); 

  
		      
 	try {
 		
 		
 	    
    	
 		
 		  File f = new File (getDir() + "tootaja.xml");
 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	     DocumentBuilder db;
 		db = dbf.newDocumentBuilder();      	
     	FileInputStream in = getCleanInputStream(f);
     	String handler = null;
         Document doc = db.parse(in,handler);
         Node root = getNode(doc, "tootajad");     
   
         for (int i = 0; i < root.getChildNodes().getLength(); i++){
         	  Node rida = root.getChildNodes().item(i);
         //	 System.out.println("ridu2  " + rida.getNodeName());
         	  try{
         		 if(rida.getNodeName().equals("tootaja")){
         		 String salesman  = getNode(rida,"tootaja_kd").getFirstChild().getNodeValue(); 
         		 String salesmanName  =  getNode(rida,"nimi").getFirstChild().getNodeValue();
         		
         		if(salesman != null){
             		 Agent sm = new Agent (salesman);
             		 sm.setName(salesmanName);
             		 tm.put(salesmanName, sm);
                     agents1.put(salesman, salesmanName);
                     agents2.put(salesman, sm);
         		}}
         		   
         	  }catch(Exception e){
         	  }
         
 		  }
         in.close();
         doc = null;
 	}catch(Exception e){
		e.printStackTrace();
 	}
 	 Agent sm = new Agent ("aiku");
		 sm.setName("aiku");
		 tm.put("aiku", sm);
 	
 	Converter.getManagersReplacements(tm,rcompany,user,pass, false);
    
 	//System.out.println("Agendilist tehtud " + tm.toString() ); 
    
     return tm;
   }
   

 


   public static Node getNode(Node node, String name){
     for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
       Node nd = node.getChildNodes().item(i);
       if (nd.getNodeName().equals(name)){
         return nd;
       }
     }
     return null;
   }

   public static Node getNodeAttr(Node node, String name,String attr){
	     for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	       Node nd = node.getChildNodes().item(i);
	       if (nd.getNodeName().equals(name) && nd.getAttributes().item(0).getNodeValue().equals(attr)){
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
       public  static String firstToUpperCase(String aaa){
	    	 if(aaa == null || aaa.equals("")){
	    		 return aaa;
	    	 }
	    	 return aaa.substring(0,1).toUpperCase() + aaa.substring(1).toLowerCase();
	     }
     
    
       private   TreeMap getTyypName(){
    	     TreeMap tm = new TreeMap();
    	     System.out.println("TyypName alustan");
    	    
    	   try {
    			  File f = new File (getDir() + "riiulityyp.xml");
    	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	  	     DocumentBuilder db;
    	  		db = dbf.newDocumentBuilder();      	
    	      	FileInputStream in = getCleanInputStream(f);
    	      	String handler = null;
    	          Document doc = db.parse(in,handler);
    	              
    	          Node root =  getNode(doc, "riiulityybid");     
    	          
    	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
    	        	  Node rida = root.getChildNodes().item(i);
    	        	
    	    	    try{
    	    	    	if(rida.getNodeName().equals("riiulityyp")){
    	    	    	  String id =  getNode(rida,"riiulityyp_kd").getFirstChild().getNodeValue();
     	         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
     	         		tm.put(id, nimi);	
    	    	    	}
    	         		
                 	  }catch (Exception e){
                 		
    	             e.printStackTrace();	
    	         
    	         	  }
    	          }
    	          in.close();
    	         	  
    	   }catch (Exception e){
          	  e.printStackTrace();
      	  }
    	   return tm;
    	   }  
      

       private   TreeMap getLiikName(){
    	     TreeMap tm = new TreeMap();
    	     System.out.println("LiikName alustan");
    	    
    	   try {
    			  File f = new File (getDir() + "riiuliliik.xml");
    	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	  	     DocumentBuilder db;
    	  		db = dbf.newDocumentBuilder();      	
    	      	FileInputStream in = getCleanInputStream(f);
    	      	String handler = null;
    	          Document doc = db.parse(in,handler);
    	              
    	          Node root =  getNode(doc, "riiuliliigid");     
    	          
    	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
    	        	  Node rida = root.getChildNodes().item(i);
    	        	
    	    	    try{
    	    	    	if(rida.getNodeName().equals("riiuliliik")){
    	    	    	  String id =  getNode(rida,"riiuliliik_kd").getFirstChild().getNodeValue();
     	         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
     	         		tm.put(id, nimi);	
    	    	    	}
    	         		
                 	  }catch (Exception e){
                 		
    	             e.printStackTrace();	
    	         
    	         	  }
    	          }
    	          in.close();
    	         	  
    	   }catch (Exception e){
          	  e.printStackTrace();
      	  }
    	   return tm;
    	   }  
      
      
       private void makeInventoryData(){
    	   StringBuffer  sb  = new StringBuffer();
    	   System.out.println("Inventorydata alustan");
      	   
    	   TreeMap tyypN= getTyypName();

    	   TreeMap gruppN= getLiikName();
      	   try {
      			  File f = new File (getDir() + "riiul.xml");
      	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      	  	     DocumentBuilder db;
      	  		db = dbf.newDocumentBuilder();      	
      	      	FileInputStream in = getCleanInputStream(f);
      	      	String handler = null;
      	          Document doc = db.parse(in,handler);
      	              
      	          Node root =  getNode(doc, "riiulid");     
      	          
      	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
      	        	  Node rida = root.getChildNodes().item(i);
      	        	
      	    	    try{
      	    	    	if(rida.getNodeName().equals("riiul")){
      	    	    	 String id =  getNode(rida,"id").getFirstChild().getNodeValue();
       	         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
       	         	     String formaat = "";
       	         	     try{
       	         	     formaat = getNode(rida,"formaat").getFirstChild().getNodeValue();
        	         	 }catch(Exception fs){
       	         	    }
       	         	     String facearv = "";
       	         	 try{
       	         		facearv = "f:" +getNode(rida,"face_arv").getFirstChild().getNodeValue();
        	         	 }catch(Exception fs){
       	         	    }
 	         		     String liik =  getNode(rida,"liik_kd").getFirstChild().getNodeValue();
 	         		     if(gruppN.get(liik) != null){
 	         		    	 liik = gruppN.get(liik) +"";
 	         		     }
       	         	     String tyyp =  parent.getProps().getProperty("riiulilist");
       	         	 
   	         			sb.append(id + ";" + ((nimi + " " + formaat).trim() + " " + facearv).trim() + ";" + liik + ";" +tyyp+ "\n");
       	         	     
      	    	    	}
      	         		
                   	  }catch (Exception e){
                   		
      	             e.printStackTrace();	
      	         
      	         	  }
      	          }
      	          in.close();
      	         	  
      	   }catch (Exception e){
            	  e.printStackTrace();
        	  }
      	   
      	   Converter.saveFileInUTF8("inventprods.txt", sb);
      	   System.out.println("Inventprods lõpetan " + sb.toString());
      	   
      	   
      	   
       }
       
       private Hashtable getIlmumine(){
    	   Hashtable tm = new Hashtable();
    	   System.out.println("Ilmumine alustan");
      	   
      	   try {
      			  File f = new File (getDir() + "va_ilmumissag.xml");
      	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      	  	     DocumentBuilder db;
      	  		db = dbf.newDocumentBuilder();      	
      	      	FileInputStream in = getCleanInputStream(f);
      	      	String handler = null;
      	          Document doc = db.parse(in,handler);
      	              
      	          Node root =  getNode(doc, "va_ilmumissagedused");     
      	          
      	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
      	        	  Node rida = root.getChildNodes().item(i);
      	        	
      	    	    try{
      	    	    	if(rida.getNodeName().equals("va_ilmumissag")){
      	    	    	  String id =  getNode(rida,"va_ilmumissag_kd").getFirstChild().getNodeValue();
       	         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
       	         		tm.put(id, nimi);	
      	    	    	}
      	         		
                   	  }catch (Exception e){
                   		
      	             e.printStackTrace();	
      	         
      	         	  }
      	          }
      	          in.close();
      	         	  
      	   }catch (Exception e){
            	  e.printStackTrace();
        	  }
      	   
      	   System.out.println("Ilmumine lõpetan " + tm.size());
      	   
      	   return tm;
      	   }
       
       private   TreeMap getKett(){
  	     TreeMap tm = new TreeMap();
  	     System.out.println("Kett alustan");
  	   
  	   try {
  			  File f = new File (getDir() + "mp_kett.xml");
  	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  	  	     DocumentBuilder db;
  	  		db = dbf.newDocumentBuilder();      	
  	      	FileInputStream in = getCleanInputStream(f);
  	      	String handler = null;
  	          Document doc = db.parse(in,handler);
  	              
  	          Node root =  getNode(doc, "mp_ketid");     
  	          
  	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
  	        	  Node rida = root.getChildNodes().item(i);
  	        	
  	    	    try{
  	    	    	if(rida.getNodeName().equals("mp_kett")){
  	    	    	  String id =  getNode(rida,"mp_kett_kd").getFirstChild().getNodeValue();
   	         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
   	         		tm.put(id, nimi);	
  	    	    	}
  	         		
               	  }catch (Exception e){
               		
  	             e.printStackTrace();	
  	         
  	         	  }
  	          }
  	          in.close();
  	         	  
  	   }catch (Exception e){
        	  e.printStackTrace();
    	  }
  	   return tm;
  	   }  
        
       
       
       
       
       
       
       
       private String getVal(XSSFRow row,int u ){
    	   String asi = "";
    	   if(row.getCell(u) != null ){
    		   try {
					asi = (int) (row.getCell((short) u).getNumericCellValue()) + "";
				} catch (Exception e) {
					asi = (row.getCell((short) u).getStringCellValue());
				}
    		 }
    	  return asi ; 
       }
       
       private String getPaev(XSSFRow row , int week){
    	   String paev = "";
    	   int alg = 17;
    	   String w1 = "";
			if(getVal(row,alg).equals("1")){
				w1 = "E";
			}
			if(getVal(row,alg+1).equals("1")){
				w1 = w1 + "T";
			}
			if(getVal(row,alg+2).equals("1")){
				w1 = w1+ "K";
			}
			
			if(getVal(row,alg+3).equals("1")){
				w1 = w1+ "N";
			}
			if(getVal(row,alg+4).equals("1")){
				w1 = w1+ "R";
			}
			
			String w12 = "";
			if(getVal(row,alg+5).equals("1")){
				w12 = "E";
			}
			if(getVal(row,alg+6).equals("1")){
				w12 = w12 + "T";
			}
			if(getVal(row,alg+7).equals("1")){
				w12 = w12+ "K";
			}
			
			if(getVal(row,alg+8).equals("1")){
				w12 = w12+ "N";
			}
			if(getVal(row,alg+9).equals("1")){
				w12 = w12+ "R";
			}
			
			String w13 = "";
			if(getVal(row,alg+10).equals("1")){
				w13 = "E";
			}
			if(getVal(row,alg+11).equals("1")){
				w13 = w13 + "T";
			}
			if(getVal(row,alg+12).equals("1")){
				w13 = w13+ "K";
			}
			
			if(getVal(row,alg+13).equals("1")){
				w13 = w13+ "N";
			}
			if(getVal(row,alg+14).equals("1")){
				w13 = w13+ "R";
			}
			
			String w14 = "";
			if(getVal(row,alg+15).equals("1")){
				w14 = "E";
			}
			if(getVal(row,alg+16).equals("1")){
				w14 = w14 + "T";
			}
			if(getVal(row,alg+17).equals("1")){
				w14 = w14+ "K";
			}
			
			if(getVal(row,alg+18).equals("1")){
				w14 = w14+ "N";
			}
			if(getVal(row,alg+19).equals("1")){
				w14 = w14+ "R";
			}
			
			if(getVal(row,alg+21).equals("1")){
				return  "KV1-3";
			}
			if(getVal(row,alg+22).equals("1")){
				return  "KV2-4";
			}
			
			//peame vaatama nï¿½dala numbri 
			if(week == 1 ){
				return w1;
			}
			if(week == 2 ){
				return w12;
			}
			if(week == 3 ){
				return w13;
			}
			if(week == 4 ){
				return w14;
			}
			
    	   return paev;
       }
       
       TreeMap ketidSidus = new TreeMap();
       Hashtable agBudgets = new Hashtable();
       
       private TreeMap getBudgets(){
    	   TreeMap tm = new TreeMap();
    	    ketidSidus = new TreeMap();
    	    TreeMap eelarved = new TreeMap();
    	    TreeMap eelarvedeel = new TreeMap();
    	    TreeMap ageel = new TreeMap();
    	    TreeMap ageelPl = new TreeMap();
    	    TreeMap agjooksev2 = getJooksevMyyk(true);
    	    TreeMap ageelmine2 = getJooksevMyyk(false);
    	   int month = Calendar.getInstance().get(Calendar.MONTH) +1;
    	   System.out.println("Eelarved alustan");
  	    
    	   int a = 0;
  	   try {
  			  File f = new File (getExcelDir() + "budget.xls");
  			  FileInputStream	 ff = new FileInputStream(f);
  		
  			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("ketid");
			HSSFRow row = null; 
  					ff.close();	
  					if(sheet != null){
  						for(int i = 0 ; i < sheet.getLastRowNum();i++){
  							 row = sheet.getRow(i);
  							//System.out.println(row);
  							if(row == null && a == 0){
  								a = i;
  							}else{
  								a = sheet.getLastRowNum();
  							}
  						}
  						
  						for(int i = 0 ; i < a + 1;i++){
  							 row = sheet.getRow(i);
  							String kett1 = null;
  							String kett2 = null;
  							try {
  								kett1 = (row.getCell((short) 0).getStringCellValue());
  	    					} catch (Exception e) {
  	    						
  	    					}
  	    					try {
  								kett2 = (row.getCell((short) 1).getStringCellValue());
  	    					} catch (Exception e) {
  	    						
  	    					}
  	    					
  						    ketidSidus.put(kett2,kett1 );
  						}
  					
  						}
  	         		
  					
  					
  					// loeme vï¿½lja esindaja budgetid 
  					sheet = workbook.getSheet("eelarve");
  					
  					        
  					        row = null; 
  		  					if(sheet != null){
  		  						for(int i = 0 ; i < sheet.getLastRowNum();i++){
  		  							 row = sheet.getRow(i);
  		  							if(row == null && a == 0){
  		  								a = i;
  		  							}else{
  		  								a = sheet.getLastRowNum();
  		  							}
  		  						}
  		  						
  		  						for(int i = 1 ; i < a + 1;i++){
  		  						 row = sheet.getRow(i);
  		  						
  		  						    String kett = null;
  		  							String esindaja = null;
  		  							double eelarve = 0;
  		  							double eelarveeelm = 0;
  		  							
  		  							try {
  		  								kett = (row.getCell((short) 0).getStringCellValue());
  		  	    					} catch (Exception e) {
  		  	    					}
  		  	    					
  		  	    					try {
  		  								esindaja = (row.getCell((short) 1).getStringCellValue());
  		  	    					} catch (Exception e) {
  		  	    					}
  		  	    				try {
		  	    						eelarveeelm = (row.getCell((short) month + 1).getNumericCellValue());
	  	    					} catch (Exception e) {
	  	    					}
	  	    					
  		  	    					try {
  		  	    						eelarve = (row.getCell((short) month + 2).getNumericCellValue());
		  	    					} catch (Exception e) {
		  	    					}
		  	    					
		  	    					if(kett != null){
		  	    		            	eelarved.put(esindaja + kett, eelarve );	
		  	    		            	eelarvedeel.put(esindaja + kett, eelarveeelm );	
		  	    					}
		  	    					if(esindaja != null){
		  	    						if(ageel.get(esindaja) != null){
		  	    							double eek = (Double)ageel.get(esindaja);
			  	    						ageel.put(esindaja , eelarve + eek );	
		  	    						}else{
		  	    							ageel.put(esindaja , eelarve);	
		  	    						}
		  	    						
		  	    						if(ageelPl.get(esindaja) != null){
		  	    							double eek = (Double)ageelPl.get(esindaja);
		  	    							ageelPl.put(esindaja , eelarveeelm + eek );	
		  	    						}else{
		  	    							ageelPl.put(esindaja , eelarveeelm);	
		  	    						}
		  	    					 }
                           		  }
  		  						}
  		  			 System.out.println("ageelmine plaan  " + ageelPl.toString() );
  					
               	  }catch (Exception e){
                      e.printStackTrace();	
  	         	  }
               	  
               	  
               	  // loeme sisse jooksva kuu mï¿½ï¿½gid 
               	  
               	
               	  makeOBS();
               	  
               	 TreeMap jooksev = new TreeMap();
            	 TreeMap agjooksev = new TreeMap();
        	     System.out.println("Müügiandmed jooksev kuu alustan");
        	
        		   
        		   String[] list = new File(getDir()).list();
        			
        		   // 1. kliendi mï¿½ï¿½gid jooksvas kuus
        	    
        		  
        			  for(int u = 0 ; u < list.length ; u++){
        				  String fname = list[u];  
        		
        				  if(fname.indexOf("vnr_myyk_jkuu_") != -1){
        					  File f = new File (getDir() + fname);
        			  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        			  	     DocumentBuilder db;
        			  	
        			  	   try{
        			  		   db = dbf.newDocumentBuilder();      	
        			  	   
        			      	FileInputStream in = getCleanInputStream(f);
        			      	String handler = null;
        			          Document doc = db.parse(in,handler);
        			              
        			          Node root =  getNode(doc, "vnr_myygid");     
        			          
        			          for (int i = 0; i < root.getChildNodes().getLength(); i++){
        			        	  Node rida = root.getChildNodes().item(i);
        			        	
        			    	    try{
        			    	    	if(rida.getNodeName().equals("vnr_myyk")){
        			    	    	  String klient =  getNode(rida,"mp_id").getFirstChild().getNodeValue();
        			    	    	  String toode =  getNode(rida,"va_id").getFirstChild().getNodeValue();
        			    	    	//  double jaehind = Double.parseDouble(getNode(rida,"kh").getFirstChild().getNodeValue());
        			    	    	 // double omahind = Double.parseDouble(getNode(rida,"oh").getFirstChild().getNodeValue());
        			    	    	  double hind = Double.parseDouble(getNode(rida,"mh").getFirstChild().getNodeValue());
        			    	    	  double km = Double.parseDouble(getNode(rida,"km_prots").getFirstChild().getNodeValue());
        			    	    	  try{
            			    	    	  double  kogus = Double.parseDouble( getNode(rida,"kogus").getFirstChild().getNodeValue());
              			    	    	
            			    	    	  double reasum = kogus * hind ;
            			    	        String agent = custAgent.get(klient) +"" ;
            			    	    	String kett = (String) ketidSidus.get(kettKlient.get(klient)) ;
                                    
            			    	    	if(jooksev.get(agent+kett) != null){
            			    	    		double sum = Double.parseDouble(jooksev.get(agent+kett)+"");
            			    	    		jooksev.put(agent+kett, reasum + sum );
            			    	    	}else{
            			    	    		jooksev.put(agent+kett, reasum );
            			    	    	}
            			    	    	
            			    	    	

            			    	    	if(agjooksev.get(agent) != null){
            			    	    		double sum = Double.parseDouble(agjooksev.get(agent)+"");
            			    	    		agjooksev.put(agent, reasum + sum );
            			    	    	}else{
            			    	    		agjooksev.put(agent, reasum );
            			    	    	}
            			    	    	
            			    	    	
        			    	    	  }catch(Exception e){
            			    	    }	
}
        			    	    }catch(Exception e){
        			    	    	e.printStackTrace();
        			    	    }	
      			    	      }
        			  	    }catch(Exception r){
        			  	    	r.printStackTrace();
        				  }
       	
        				  }
        			  }
               	  
               	  
              // 	  System.out.println("jooksev "  + jooksev.toString());
               	   
               	Iterator enumr = eelarved.keySet().iterator();
               	 // vaatame eelarve ï¿½le ja otsime tï¿½ituvust ja teeme stiringi mille paneme Treemapi .
               	 while (enumr.hasNext()){
               		  String key = (String) enumr.next();
               	      double bud = (Double) eelarved.get(key);
               	      double done = 0;
               	      try{
               	    	  done =  (Double) jooksev.get(key);  
               	      }catch(Exception e){
               	     }
               	
               	      int perc = ((int)(done/bud *100));
               	      if(perc > 100) perc = 100;
                 // 	 System.out.println("koos " + key + " " +((int) bud + " / " + (int)done) + " / " + ((int)(done/bud *100)));
                     tm.put(key, ((int) bud + "€ / " + (int)done) + "€ / " + perc + "%");
               	  }
               	 
               	 
             
               	 
               	 
               	 enumr = ageel.keySet().iterator();
              	 // vaatame eelarve ï¿½le ja otsime tï¿½ituvust ja teeme stiringi mille paneme Treemapi .
              	 while (enumr.hasNext()){
              		  String key = (String) enumr.next();
              	      double bud = (Double) ageel.get(key);
              	      double done = 0;
              	      try{
              	    	  done =  Double.parseDouble(agjooksev2.get(key)+ "");  
              	      }catch(Exception e){
              	    	  e.printStackTrace();
              	     }
              	
              	      int perc = ((int)(done/bud *100));
              	      if(perc > 100) perc = 100;
                   agBudgets.put(key, ((int) bud + "€ / " + (int)done) + "€ / " + perc + "%");
              	  }
              	 
              	 
              	 enumr = ageelPl.keySet().iterator();
              	 // vaatame eelarve ï¿½le ja otsime tï¿½ituvust ja teeme stiringi mille paneme Treemapi .
              	 while (enumr.hasNext()){
              		  String key = (String) enumr.next();
              	      double bud = (Double) ageelPl.get(key);
              	      double done = 0;
              	      try{
              	    	  done =   Double.parseDouble(ageelmine2.get(key) + "");  
              	      }catch(Exception e){
              	     }
              	
              	      int perc = ((int)(done/bud *100));
              	      if(perc > 100) perc = 100;
              	      if(agBudgets.get(key) != null){
              	    	  String asi = agBudgets.get(key)+"";
              	    	agBudgets.put(key,asi +  "|Eelmine: " + ((int) bud + "€ / " + (int)done) + "€ / " + perc + "%");
              	      }else{
              	    	agBudgets.put(key, "Eelmine: " + ((int) bud + "€ / " + (int)done) + "€ / " + perc + "%");
                         	    	  
              	      }
              	  }
              	 
              	 
               	 
             	 System.out.println("agBudgets " +agBudgets.toString());
               		 
               	
               	 
               	  
           return tm;
       }
       
       
       
       private   TreeMap getVisits(){
    	     TreeMap tm = new TreeMap();
    	     System.out.println("Visiidid alustan");
    	     int a = 0;
    	   try {
    			  File f = new File (getExcelDir() + "Kylastusgraafik.xlsx");
    				
    			  FileInputStream	 ff = new FileInputStream(f);
    			
    					XSSFWorkbook workbook = new XSSFWorkbook(ff); 
    					XSSFSheet sheet = workbook.getSheet("GRAAFIK"); 
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
    						
    						Calendar cal = Calendar.getInstance();
    						int week = cal.get(Calendar.WEEK_OF_YEAR);
    						//taandame 
    					//	week = 41;
    						System.out.println("W " +  week);
    					
    						if(week < 5){
    						}else{
    							int aa = (int) week / 4 ;
        						week = week -(aa * 4) ;
        					}
    						if(week == 0){
    							week = 4;
    						}
    						
    						System.out.println("W2 " +  week);
    						
    						for(int i = 9 ; i < a;i++){
    							XSSFRow row = sheet.getRow(i);
    							String custid = null;
    							try {
    								custid = (int) (row.getCell((short) 0)
    										.getNumericCellValue()) + "";
    							} catch (Exception e) {
    								try{
    									custid = (row.getCell((short) 0).getStringCellValue());
    		    									
    								}catch(Exception eh){
    									
    								}
    							}
    							
    							if(custid != null){
    								String paev = getPaev(row , week);
        							if(paev != null && paev.trim().length() > 0){
        								tm.put(custid, paev);
            						}	
    							}
    							
    							//if(paev.equals("K"))
    							//System.out.println("Pï¿½ev " + a + " " + i +" " + custid + " *" + paev + " " + week);
    							
    						}
    					}
    	         		
                 	  }catch (Exception e){
                        e.printStackTrace();	
    	         	  }
            
    	   return tm;
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
       
       
       private  TreeMap excelketid = new TreeMap();
       
       private   TreeMap getKampaaniad(){
    	     TreeMap tm = new TreeMap();
    	     System.out.println("Kampaaniad alustan");
    	      excelketid = new TreeMap();
    	     TreeMap kampid = new TreeMap();// siia tekstid mis klientidele kï¿½lge
 			
    	     int a = 0;
    	   try {
    			  File f = new File (getExcelDir() + "Kampaaniad.xlsx");
    				
    			  FileInputStream	 ff = new FileInputStream(f);
    			
    			  
    			  //mï¿½ï¿½rame ketid
    			XSSFWorkbook workbook = new XSSFWorkbook((ff));
    			XSSFSheet sheet = workbook.getSheet("Ketid");
    			XSSFRow row = null; 
      					ff.close();	
      					if(sheet != null){
      						for(int i = 0 ; i < sheet.getLastRowNum();i++){
      							 row = sheet.getRow(i);
      							//System.out.println(row);
      							if(row == null && a == 0){
      								a = i;
      							}else{
      								a = sheet.getLastRowNum();
      							}
      						}
      						
      						for(int i = 0 ; i < a + 1;i++){
      							 row = sheet.getRow(i);
      							String kett1 = null;
      							String klient = null;
      							try {
      								kett1 = (row.getCell((short) 0).getStringCellValue());
      	    					} catch (Exception e) {
      	    						
      	    					}
      	    					try {
      								klient = (row.getCell((short) 1).getStringCellValue());
      	    					} catch (Exception e) {
      	    						//System.out.println("iiii " + i);
      	    						try{
      	    							klient = ((int)row.getCell((short) 1).getNumericCellValue()) + "";
      	    	      	    							
      	    						}catch(Exception r){
      	    							
      	    						}
      	    					}
      	    				//	System.out.println("www " + kett1 + " " + klient);
      	    					if(klient != null)
      						    excelketid.put(klient,kett1.replaceAll(" ", "") );
      						}
      					
      						}
      	         		
    			  
    			  
    			  
    					 sheet = workbook.getSheet("Kampaaniad"); 
    					ff.close();	
    					
    					if(sheet != null){
    						for(int i = 0 ; i < sheet.getLastRowNum();i++){
    							 row = sheet.getRow(i);
    							//System.out.println(row);
    							if(row == null && a == 0){
    								a = i;
    							}else{
    								a = sheet.getLastRowNum();
    							}
    						}
    						for(int i = 0 ; i < a;i++){
    							
    							 row = sheet.getRow(i);
    							String prodid = null;
    							try {
    								prodid = (int) (row.getCell((short) 0)
    										.getNumericCellValue()) + "";
    							} catch (Exception e) {
    								prodid = (row.getCell((short) 0).getStringCellValue());
    							}
    						//	System.out.println("enne " +prodid);
    							if(prodsx.get(prodid) != null){
    								//prodid = prodsx.get(prodid)+ "";
    							}
    							//System.out.println("hiljem " +prodid);
    							String nimi = null;
    							try {
    								nimi = (row.getCell((short) 1).getStringCellValue());
    							} catch (Exception e) {
    								
    							}
    							String sisu = null;
    							try {
    								sisu = (row.getCell((short) 2).getStringCellValue());
    							} catch (Exception e) {
    								
    							}
    							Date alg = null;
    							String algus = null;
    							try {
    								alg = row.getCell((short) 3).getDateCellValue();
    								algus = getDateDDkMMkYYYYStr(alg);
    							} catch (Exception e) {
    								
    							}
    							Date lop = null ; 
    							String lopp = null;
    							try {
    								lop = row.getCell((short) 4).getDateCellValue();
    								lopp = getDateDDkMMkYYYYStr(lop);
    							} catch (Exception e) {
    								
    							}
    							
    							String kett = null;
    							try {
    								kett = (row.getCell((short) 5).getStringCellValue()).replaceAll(" ", "");
    							} catch (Exception e) {
    								
    							}
    							
    							Date dd = new Date();
    							if(alg != null){
    								alg = new Date(alg.getTime()- 604800000);
    							}
    							
    						if(alg != null && lop != null && dd.after(alg) && dd.before(lop)){
    						
    							
    							if(kampid.get(kett) != null){
    								String aa = (String)kampid.get(kett) ;
    								kampid.put(kett, aa + "|" +sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
        	    					
    							}else{
    								kampid.put(kett,  sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
        	    								
    							}

        					//	System.out.println("=== " +kett + " " +  sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
    				    		//System.out.println("AAA " + kett + " " + sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
    							MatrixLine mm = new MatrixLine();
    							mm.setCliGroup(kett);
    							mm.setProdID(prodid);
    							mm.setEndDate(lopp);
    							mm.setStartDate(algus);
    							mm.setLineinfo(sisu);
    							
    						    matrixlines.add(mm);
    				   // System.out.println(mm.getStringForFixedAssortment());
    						 }
    						}
   			//	System.out.println(kampid.toString());

    							}
    	         		
                 	  }catch (Exception e){
                        e.printStackTrace();	
    	         	  }
            
                 	  Iterator itera = excelketid.keySet().iterator();
                 	 while (itera.hasNext()){
                 		 String custid = (String) itera.next();
                 		 String kett = (String)excelketid.get(custid);
                 		 StringTokenizer st = new StringTokenizer(kett,",");
                 		//System.out.println(custid + " " + kett);
             				 
                 		 while(st.hasMoreElements()){
                 			 String uuu = (st.nextElement() + "").trim();
                 	//		System.out.println(custid + " " + uuu);
                 			
                 			 
                 			 if(tm.get(custid) != null){
                 				 String asi = "" + tm.get(custid);
                 				 tm.put(custid, asi + "|" + kampid.get(uuu));
                 			 }else{
                     			 tm.put(custid, kampid.get(uuu)); 
                 			 }
                         }
                 	 }
                 	//System.out.println(tm.toString());
         			
    	   return tm;
    	   }  
           

       
       private   TreeMap getMaakond(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Maakond alustan");
	   
	   try {
			  File f = new File (getDir() + "maakond.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root = getNode(doc, "maakonnad");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	    	    try{
	    	    	if(rida.getNodeName().equals("maakond")){
	    	    		String id =  getNode(rida,"maakond_kd").getFirstChild().getNodeValue();
	    	    		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
	 	         		tm.put(id, nimi);
	 	         		
	    	    	}
	         		
             	  }catch (Exception e){
	                e.printStackTrace();	  
	         	  }
	          }
	          in.close(); 
	   }catch (Exception e){
      	  e.printStackTrace();
  	  }
	   return tm;
	   }  
       

       
       private   TreeMap getAsum(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Asumid alustan");
	     TreeMap mk = getMaakond();
	   try {
			  File f = new File (getDir() + "asum.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root = getNode(doc, "asumid");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("asum")){

		    	    	String id =  getNode(rida,"asum_kd").getFirstChild().getNodeValue();
		         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
		         		 String maakond =  getNode(rida,"maakond_kd").getFirstChild().getNodeValue();
		         		 if(mk.get(maakond) != null){
		         			 nimi = nimi + ", " + mk.get(maakond);
		         		 }
		         		// System.out.println(nimi);
		         		tm.put(id, nimi);
	                    }
		    	  }catch (Exception e){
             		  e.printStackTrace();	  
	         	  }
	          }
	          in.close();
	         	  
	   }catch (Exception e){
      	  e.printStackTrace();
  	  }
	   return tm;
	   }  
       
      private TreeMap kettKlient = null;
      private boolean runob = false; 
      
      
   public TreeMap getCustomers(Agent agent){
	   
	  
	   
	     TreeMap tm = new TreeMap();
	     TreeMap tmid = new TreeMap();
	   //  System.out.println("Kliendid alustan prio size " + prio.size());
	     
	     kettKlient = new TreeMap();
	     TreeMap asum = getAsum();
	     this.customers = new ArrayList();
	     int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
	   
	   try {
		  
	 		
	 		  File f = new File (getDir() + "mp.xml");
	 	
		     
	 		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root = getNode(doc, "mpd");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	      
            if(rida.getNodeName().equals("mp")){
            	
         
	         	  try{
	         		  
	         		 String aadr = ""; 
	         		 String linn = ""; 
		         		 try{
		         			
		         			 linn =   getNode(rida,"asum_kd").getFirstChild().getNodeValue();
		         			 if(asum.get(linn) != null){
		         				 linn = asum.get(linn)  + "";
		         			 }
			         		 aadr = aadr + " " +(deCode(linn ));  	  
		         		  }catch(Exception ea){
		         		  }
		         		  
	         		  
	         		  String id =  getNode(rida,"myygipunkt_id").getFirstChild().getNodeValue();
	         		  Customer customer = new Customer(id);
	         		  String name =  getNode(rida,"nimi").getFirstChild().getNodeValue() ;
	         		  customer.restCity = linn;
	         		  customer.setCountry("Estonia");
	         		  boolean goes = true;
	         		 try{ String blocked  =  getNode(rida,"mp_olek_kd").getFirstChild().getNodeValue();
	         		 if(blocked.equals("SULETUD")){
	         		    // name = "x " + name;
	         			 goes = false;
	         		  }
	         	   }catch(Exception e){}
	         		
	         		 try{
	         			  aadr =  getNode(rida,"aadress").getFirstChild().getNodeValue();
	         			  customer.restStreet = aadr;
	         		  }catch(Exception e){
	         		  }
	         		 try{
	         			   customer.setPayer(getNode(rida,"firma_id").getFirstChild().getNodeValue());
	         			   
	         		  }catch(Exception e){
	         		  }
	         		
	         		  
	         		  
	         		 try{
	         			String prior = getNode(rida,"jrk_nr_marsruudis").getFirstChild().getNodeValue();
	         					
	         					if(prior.length() == 1){
	        	         			 prior= "00" + prior;
	        	         		 }
	        	         		 
	        	         		if(prior.length() == 2){
	        	         			 prior= "0" + prior;
	        	         		 }
	        	         		
	         			// customer.restPriority = prior;
	         			  
	         		  }catch(Exception e){
	         			 // e.printStackTrace();
	         		  }
	         		
	         		 
	         		 try{
	         		 String post =  getNode(rida,"postiindeks").getFirstChild().getNodeValue(); 
	         		 customer.restZipCode = post;
	         		 aadr = aadr + " " +(deCode(post));  	  
         		  }catch(Exception ea){
         		  }
         		  
         		  
         		   
	         		  
	         		 customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr), "Estonia"));
	         		 customer.setName(deCode(name));
	         		 customer.restPartner = "LEHEPUNKT";
	         	     try{
     		    	 	 String contacname =  getNode(rida, "kontaktisik").getFirstChild().getNodeValue()   ;
	         			 if(contacname.trim().length() >0){
			         		  customer.setPersonalData(deCode(contacname)); 
			         		  customer.restPerson = contacname.trim();
	         			 }  	  
	         		  }catch(Exception e){}
	         		  
	         		     try{
		         			 String phone =  getNode(rida,"telefon").getFirstChild().getNodeValue();
		         			 customer.restPerPhone = phone;
		         			 if(customer.getPersonalData() != null){
				         		  customer.setPersonalData(customer.getPersonalData() + " " + deCode(phone.replaceAll(";", " , ")));  
		         			 }else{
				         		  customer.setPersonalData(deCode(phone.replaceAll(";", " , "))); 
		         			 } 	  
		         		  }catch(Exception e){
		         		  }
	         		  
		         		 try{ String email =  getNode(rida,"mail").getFirstChild().getNodeValue(); 
		         			 if(customer.getPersonalData() != null){
				         		  customer.setPersonalData(customer.getPersonalData() + " " + deCode(email.replaceAll(";", " , ")));  
		         			 }else{
				         		  customer.setPersonalData(deCode(email.replaceAll(";", " , "))); 
		         			 } 	  
		         			 customer.restPerEmail = email;
		         		  }catch(Exception e){ }
	         	         if(goes){
	         	            tm.put(customer.getID(), customer);
		         		    tmid.put(customer.getID(), customer);
		                    this.customers.add(customer.getID());
		                  //  System.out.println("prior" + customer.restPriority);
	         	         }
                    
	     
	         	  }catch(Exception e){
	         	  }

      	   }
               	 
	          }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	 			    
	    System.out.println("Kliendid tehtud");
	 return tm;
   }
   
   
   
   private   TreeMap getRoutes(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Routes alustan");
	   
	   try {
			  File f = new File (getDir() + "mp_marsruut.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root =  getNode(doc, "mp_marsruudid");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("mp_marsruut")){
	    	    	  String id =  getNode(rida,"mp_marsruut_kd").getFirstChild().getNodeValue();
 	         		 String nimi =  getNode(rida,"nimi").getFirstChild().getNodeValue();
 	         		 String comment =  getNode(rida,"teenindaja_kd").getFirstChild().getNodeValue();
 	         		 Customer cc = new Customer(id);
 	         		 cc.setName(nimi);
 	         		 cc.restPartner = "LEHEPUNKT";
 	         		 tm.put(id, cc);
 	         		
 	        	   
	    	    	}
	         		
             	  }catch (Exception e){
             		
	             e.printStackTrace();	
	         
	         	  }
	          }
	          in.close();
	         	  
	   }catch (Exception e){
      	  e.printStackTrace();
  	  }
	   System.out.println("Routesid kokku " + tm.size() );
	   return tm;
	   }  

   
   
   
   
   private String company = null;
   
   private String             url2 = "jdbc:odbc:report";
	private Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url2, getParent().getProps()
				.getProperty("dsn.user"),
				getParent().getProps().getProperty("dsn.pass"));
	}
	
   
	
	  public TreeMap getAgents2() {
			return agents2;
		}

   
 
   private String getKuup(){
	   return this.getYYYYpMMpDD(null);
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

   
   public void makeOBS(){
	     TreeMap tm = new TreeMap();
	     TreeMap tmid = new TreeMap();
	     kettKlient = new TreeMap();
	     this.customers = new ArrayList();
	     int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
	   
	   try {
		  
	 		
	 		  File f = new File (getDir() + "mp.xml");
	 	
		     
	 		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root = getNode(doc, "mpd");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	      
          if(rida.getNodeName().equals("mp")){
          	
       
	         	  try{
	         		  
	         		 String aadr = ""; 
	         		 String linn = ""; 
		         	
		          String id =  getNode(rida,"myygipunkt_id").getFirstChild().getNodeValue();
	         		  Customer customer = new Customer(id);
	         		  String name =  getNode(rida,"nimi").getFirstChild().getNodeValue() + " " + id + " " + linn;
	         		 
	         		
	         		  
	         
	         		  
	         		 customer.setName(deCode(name));
	          		  
		         		  
		         	     String salesman  = null; 
	                     
	                     try{ salesman  =  getNode(rida,"myygiesindaja_kd").getFirstChild().getNodeValue();
		         		      custAgent.put(customer.getID(),deCode(salesman));
	                     }catch(Exception e){}
		         	   
		         		    
	         		 try{  String kett =  getNode(rida,"mp_kett_kd").getFirstChild().getNodeValue();
	         		      kettKlient.put(customer.getID(),kett);
	                   }catch(Exception e){}
                  
	           		      
             	         
	         	         boolean goes = true;
			 	   if(goes){
	           		  tm.put(name, customer);
	         		    tmid.put(customer.getID(), customer);
	              }
	     
	         	  }catch(Exception e){
	         	  }

    	   }
             	 
	          }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
 }
   
   
	
   private Date getDate2(String kuup , int add) {
	 //2013-05-29T00:00:00
	 		Calendar cal = Calendar.getInstance();
	 		int YEAR = Integer.parseInt(kuup.substring(0, 4));
	 		int MONTH = Integer.parseInt(kuup.substring(5, 7));
	 		int DAY = Integer.parseInt(kuup.substring(8, 10));
	 		cal.set(YEAR, MONTH -1, DAY, 0, 0, 0);
	 		cal.add(Calendar.DAY_OF_YEAR, add);
	 		Date now = new Date(cal.getTimeInMillis());
	 		return now;
	 	}
   

   private Date getDate3(String kuup , int add) {
	 //2013-05-29T00:00:00
	 		Calendar cal = Calendar.getInstance();
	 		int YEAR =  Integer.parseInt(kuup.substring(0,4));
	 		int MONTH = Integer.parseInt(kuup.substring(4,6));
	 		int DAY = 1;
	 		cal.set(YEAR, MONTH -1, DAY, 1, 2, 3);
	 		cal.add(Calendar.DAY_OF_YEAR, add);
	 		Date now = new Date(cal.getTimeInMillis());
	 		return now;
	 	}
   
   
   private Date getDate(String kuup) {
//20120301
		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(4, 6));
		int DAY = Integer.parseInt(kuup.substring(6, 8));
		cal.set(YEAR, MONTH -1, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}
    
   private   TreeMap getKeel(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Keeled alustan");
	   
	   try {
			  File f = new File (getDir() + "va_keel.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root =  getNode(doc, "va_keeled");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("va_keel")){
	    	    	  String id =  getNode(rida,"va_keel_kd").getFirstChild().getNodeValue();
	         		 String nimi = getNode(rida,"nimi").getFirstChild().getNodeValue();
	         		 tm.put(id, nimi);	
	    	    	}
	         		
           	  }catch (Exception e){
           		
	             e.printStackTrace();	
	         
	         	  }
	          }
	          in.close();
	         	  
	   }catch (Exception e){
    	  e.printStackTrace();
	  }
	   //System.out.println("keeled "+ tm.toString());
	   return tm;
	   }  
   
   private   TreeMap getKM(){
	     TreeMap tm = new TreeMap();
	  //   System.out.println("KMs alustan");
	   
	   try {
			  File f = new File (getDir() + "va_km_grupp.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root =  getNode(doc, "va_km_gruppid");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("va_km_grupp")){
	    	    	  String id =  getNode(rida,"va_km_grupp_kd").getFirstChild().getNodeValue();
 	         		 int nimi = (int) (Double.parseDouble(getNode(rida,"km_prots").getFirstChild().getNodeValue())*100);
 	         		 tm.put(id, nimi);	
	    	    	}
	         		
             	  }catch (Exception e){
             		
	             e.printStackTrace();	
	         
	         	  }
	          }
	          in.close();
	         	  
	   }catch (Exception e){
      	  e.printStackTrace();
  	  }
	  // System.out.println("KM-s "+ tm.toString());
	   return tm;
	   }  
   
   
   HashMap prodsx = new HashMap();
   HashMap prodid = new HashMap();
   HashMap prodsn = new HashMap();
   TreeMap prodHash = null;
   

   
   public TreeMap getProducts(Agent agent){
	
	   try {
		getProdGroupsSecond();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
//	System.out.println("secgroups  "+ this.secGroups.toString());
	   
    TreeMap tm = new TreeMap();
      prodHash = new TreeMap();
     System.out.println("Tooted alustan");
     TreeMap kms = getKM();
     TreeMap keel = getKeel();
     prodsx = new HashMap();
     prodid = new HashMap();
     prodsn = new HashMap();
     
    
      try {
     	 Vector koodid = new Vector();
        
		  File f = new File (getDir() + "va.xml");
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream inx = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(inx,handler);
        inx.close();
   
        Node root = getNode(doc, "vad");
              
      if(root.getChildNodes().getLength() > 0 ){
    	     for (int i = 0; i < root.getChildNodes().getLength(); i++){
           	  Node rida = root.getChildNodes().item(i);
           	  
           	if(rida.getNodeName().equals("va")){
       		 boolean goes = true;
          		 String nameadd = "";
          	     String id = getNode(rida,"valjaanne_id").getFirstChild().getNodeValue();
          		  
          		 String kood = ""; 
          		 
          		try{
          			kood = getNode(rida,"kood").getFirstChild().getNodeValue();
          			
          			
          	       }catch (Exception e){}
          		 
          	     prodsx.put (id,kood);
	        	 prodid.put(kood,id); 
	             
          	     Product prod = new Product(deCode(kood));
          	    boolean sort = true;
          		 try{
       	    		 String olek = getNode(rida, "va_olek_kd").getFirstChild().getNodeValue();
       	    		    if(olek.equals("SULETUD")){
       	    		    	sort = false;
       	    		    	nameadd = "üü ";
       	    		    }
          	       }catch (Exception e){}
       	    	 
          	       
          	     try{
       	    		 String hind = getNode(rida, "kaanehind").getFirstChild().getNodeValue();
       	    		   prod.setPrice(Double.parseDouble(hind));
          	       }catch (Exception e){}
       	    	
          	       
          	     try{
       	    		 String juht  = getNode(rida, "tootejuht_kd").getFirstChild().getNodeValue();
       	    		 if(juht.trim().length() > 2){
       	    		  
       	    		 }
       	    	   }catch (Exception e){}
          	       
       	 

       	    	
       	    	
       	    	try{
       	    		 String km = getNode(rida, "va_km_grupp_kd").getFirstChild().getNodeValue();
       	    		   prod.setVat(kms.get(km) + "");
          	       }catch (Exception e){}
       	    	 
          		 String nimisort = "";
          		 
          		   String nimi = null;   
          		 try{
          			nimi = getNode(rida,"nimi").getFirstChild().getNodeValue();
          			prod.setNameSorting(nameadd + nimi);
          			prod.setName(Converter.enCodeEst(nameadd + nimi + " " + kood));
          			
          			if(nimi.startsWith("OTSAS")){
          				goes = false;
          			}
          			int ll = nimi.lastIndexOf(" ");
          			
          			if(ll > 0 && nimi.substring(ll).toLowerCase().indexOf("export") != -1 ){
          				goes = false;
          			}
           	       }catch (Exception e){}
           		     
          		 try{
       	    		 String grupp = getNode(rida, "va_kategooria_kd").getFirstChild().getNodeValue();
       	    		    prod.setGroup(grupp);
          	       }catch (Exception e){}
          		      		   
          		
          	     try{
       	    		 String liik = getNode(rida, "va_liik_kd").getFirstChild().getNodeValue();
       	    		    prod.setGroupSecond(liik);
       	    		    try{
       	    		     yldgrupid.put(id, (liik));	
       	    		    }catch(Exception y ){
       	    		    	y.printStackTrace();
       	    		    }
       	    		
          	       }catch (Exception e){}
          		    
          	     try{
       	    		 String kee = getNode(rida, "va_keel_kd").getFirstChild().getNodeValue();
       	    		    prod.setType(keel.get(kee)+"");
          	       }catch (Exception e){}
          	       

            	     try{
         	    		 String ean = getNode(rida, "ean").getFirstChild().getNodeValue();
         	    		 if(ean.startsWith("00")){
         	    			 ean = ean.substring(1);
         	    		 }
         	    		if(ean.startsWith("0")){
        	    			 ean = ean.substring(1);
        	    		 }
         	    		 
         	    		    prod.setEAN(ean.trim());
            	       }catch (Exception e){}
       	    	 	 	 
        	    if(goes && prod.getName()!= null){
        	         tm.put(prod.getName(), prod);	
        	         koodid.add(id);
        	         if(sort){
        	        	 prodHash.put(prod.getID(), prod);
        	         }
        	    }
    	     }
            } 
      }
   /*if(koodid.size() >0){
	
   	 TreeMap stocks = getStocks(koodid);
   	 
   	 Iterator ite = stocks.keySet().iterator();
   	 while(ite.hasNext()){
   		 String toode = (String)ite.next();
   		 if(prodHash.get(toode)!= null){
   			 Product ppp = (Product)prodHash.get(toode);
   			 ppp.setStock(Double.parseDouble((String)stocks.get(toode)));
   		 }
   	  }  
   }*/
    } catch (Exception e){
      e.printStackTrace();
    }

    System.out.println("Tooted tehtud ");
     return tm;
   }

  private Hashtable yldgrupid = new Hashtable();

   private TreeMap getPrices(){
	   TreeMap result = new TreeMap();
			   
			 	try {

				   	   String[] list = new File(getDir()).list();
						  //System.out.println(list.toString());
						  
						  //meil on 4 tyypi faile ja on vaja mï¿½ï¿½rata nende kuupï¿½evad mis on kï¿½ige vï¿½rskem 
						  for(int u = 0 ; u < list.length ; u++){
							
							  String fname = list[u];
						
						if(fname.indexOf("PRICAT") != -1 && fname.indexOf((String)dates.get("PRICAT")) != -1){
							// System.out.println("File" + getDir() + fname);
						
						  File f = new File (getDir() + fname);
				 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				 	     DocumentBuilder db;
				 		db = dbf.newDocumentBuilder();      	
				     	FileInputStream in = getCleanInputStream(f);
				     	String handler = null;
				         Document doc = db.parse(in,handler);
				         Node rr    = getNode(doc, "E-Document");     
				         Node rr1   = getNode(rr, "Document"); 
				         Node rr2   = getNode(rr1, "DocumentInfo"); 
				         Node root  = getNode(rr2, "DocumentItem");     
				      //   System.out.println(root);
				     //	System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
				         for (int i = 0; i < root.getChildNodes().getLength(); i++){

				         	  Node rida = root.getChildNodes().item(i); 
				       //  	 System.out.println("ridu  " + rida.getNodeName());
				         	  if(rida.getNodeName().equals("ItemPartyEntry")){
				         		  if( getNode(rida,"PartyCode").getFirstChild().getNodeValue().equals("MATNR")){
				         			  Node item = getNode(rida,"SellerItemCode");
					         		  String id = item.getFirstChild().getNodeValue();
					         				 String kogus = getNode(rida,"ItemPartyPrice").getFirstChild().getNodeValue();
						         			 result.put(id, kogus);					         			
					         			}	  
				         		  }
				                   }}  
				         	  }
				         }catch(Exception t){
				        	 t.printStackTrace();
				         }
		
			 //	System.out.println(" hinnad " + result.size());
	   return result ;
   }

   
   private TreeMap getStocks(Vector prods){
	   TreeMap result = new TreeMap();
			   
			 	try {
			 		
			   	   String[] list = new File(getDir()).list();
					  //System.out.println(list.toString());
					  
					  //meil on 4 tyypi faile ja on vaja mï¿½ï¿½rata nende kuupï¿½evad mis on kï¿½ige vï¿½rskem 
					  for(int u = 0 ; u < list.length ; u++){
						
						  String fname = list[u];
					
					if(fname.indexOf("INVRPT") != -1 && fname.indexOf((String)dates.get("INVRPT")) != -1){
					
					  File f = new File (getDir() + fname);
				 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				 	     DocumentBuilder db;
				 		db = dbf.newDocumentBuilder();      	
				     	FileInputStream in = getCleanInputStream(f);
				     	String handler = null;
				         Document doc = db.parse(in,handler);
				         Node rr = getNode(doc, "E-Document");     
				         Node rr1 = getNode(rr, "Document"); 
				         Node rr2 = getNode(rr1, "DocumentInfo"); 
				         Node root = getNode(rr2, "DocumentItem");     
				      //   System.out.println(root);
				     	//System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
				         for (int i = 0; i < root.getChildNodes().getLength(); i++){

				         	  Node rida = root.getChildNodes().item(i); 
				         	  
				         	  if(rida.getNodeName().equals("ItemEntry")){
				         		  Node item = getNode(rida,"SellerItemCode");
				         		  String id = item.getFirstChild().getNodeValue();
				         				 String kogus =  (getNode(getNode(rida, "ItemUnitRecord"),"AmountActual").getFirstChild().getNodeValue());
					         			 result.put(id, kogus);					         			
				         			}
				         	  }}}
				         }catch(Exception t){
				        	 
				         }
		
			 //	System.out.println("laoseis " + result.size());
	   return result ;
   }
   
   
   public String getAssortmentStr(Agent agent){
    return null;
  }
   
   public String getSoldString(CustProd cp) {
	  	  if(cp.soldString != null && cp.soldString.trim().length() >0){
	  		  return cp.soldString;
	  	  }
	      if(cp.s1.equals("=") && cp.s2.equals("=") && cp.s3.equals("=") && cp.s4.equals("=") && cp.s5.equals("=")  ){
	      return " ";
	      }
	      return cp.s1 + "." + cp.s2 + "." + cp.s3 + "." + cp.s4 + "." + cp.s5 + ".";
	    }  

  private Vector custProds = new Vector();
   public Vector getCustomerProductsList(String agentCode){
	   return custProds;
   }
   public Vector getCustomerProductsListVec(){
	   this.getProducts(null);
	   System.out.println("KliendiTooted alustan");
	   Vector custProds = new Vector();
	    TreeMap sales = new TreeMap();
		
	   String[] list = new File(getDir()).list();
       for(int u = 0 ; u < list.length ; u++){
		  String fname = list[u];
	
	//if(fname.startsWith("vnr_myyk") ){

		if(fname.startsWith("mp_va_myyk") ){
	try{
	
	  File f = new File (getDir() + fname);
	// System.out.println(getDir() + fname);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
	db = dbf.newDocumentBuilder();      	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
  Document doc = db.parse(in,handler);
  Node root = getNode(doc, "mp_va_myygid");     
  HashSet done = new HashSet();
Calendar cal = Calendar.getInstance();
int days = cal.get(Calendar.DAY_OF_MONTH);
cal.add(Calendar.DATE, -(120+days));


  for (int i = 0; i < root.getChildNodes().getLength(); i++){
	
  	  Node rida = root.getChildNodes().item(i);
	
  	 try{     
  		if(rida.getNodeName().equals("mp_va_myyk")){
  			String klientid = null;
  			 try{
   	    		  klientid = getNode(rida, "mp_id").getFirstChild().getNodeValue();
   	    		   
      	       }catch (Exception e){}
   	    	 
      	       String toode = null;
      	       String idd = null;
      	     try{
      	    	 idd = getNode(rida, "va_id").getFirstChild().getNodeValue();
   	    		 toode = this.prodsx.get(idd)+ "";
   	    	   }catch (Exception e){}
      	       
   	    	double kogus = 0;
      	     try{
   	    		 kogus = Double.parseDouble(getNode(rida, "kos").getFirstChild().getNodeValue());
   	    	   }catch (Exception e){}
   	    
   	    	double miinus = 0;
     	     try{
  	    		 miinus = Double.parseDouble(getNode(rida, "kot").getFirstChild().getNodeValue());
  	    	   }catch (Exception e){}
  	    
  	    	   
   	    	   Date dd = null; 
   	    	 String date = null;
   	    	try{
   	     	date = getNode(rida,"per_kd").getFirstChild().getNodeValue();
     		   dd = getDate3(date,0); 	
   	            }catch (Exception e){}
   	       if (dd != null && dd.after(cal.getTime())){
   	    	   int month = dd.getMonth() ;
   	    	 String mm = "";
   	         if(month == 0)mm = "ja";
   	         if(month == 1)mm = "ve";

   	         if(month == 2)mm = "mr";
   	         if(month == 3)mm = "ap";
   	         if(month == 4)mm = "mi";
   	         if(month == 5)mm = "jn";
   	         if(month == 6)mm = "jl";
   	         if(month == 7)mm = "au";
   	         if(month == 8)mm = "se";
   	         if(month == 9)mm = "ok";
   	         if(month == 10)mm = "no";
   	         if(month == 11)mm = "de";
   	    	   
   	    	CustProd cp = new CustProd(klientid, toode, kogus,0 ,miinus,
             		date.substring(5),0,0,0,mm);
   	    	cp.setGroup(idd);
   	        
   	         sales.put(klientid + "" + toode + "" + date + "" + kogus, cp);
   	      
   	       }
   		}
    	
  	 }catch(Exception r){
  		 
  	 }
  
  	 
   }		
    		
	}catch(Exception t){
		t.printStackTrace();
	}
 	}	
   	}
       
  	 System.out.println("sales suurus " + sales.size());
     	Iterator itera = sales.keySet().iterator();
   		String oldprod = null;
   		String oldcust = null;
   		String oldmm = null;
   		String olddd = null;
   		
   		String ams = "";
   		double pluss = 0;
   		double minus = 0;
   		int i = 0;
   		int u = 0;
   		int mkogus = 0;
   		while (itera.hasNext()){
   			String key = itera.next()+ "";
   			CustProd cp = (CustProd)sales.get(key);
   			
   			if(oldcust == null){
   				oldcust = cp.getCustID();
   			}
   			
   			if(oldprod == null){
   				oldprod = cp.getProdID();
   			}
   			
   			if(oldmm == null){
   				oldmm = getSoldString(cp);
   			}
   			
   			if(olddd == null){
   				olddd = cp.getLastAmountDate();
   			}
   			String tt = cp.getProdID();
   			String kk = cp.getCustID();
   			int kogus = (int)cp.getLastAmount();
   			int miinus = (int)cp.getMovedStockMinus();
   			
   			String dd = cp.getLastAmountDate();
   			String mm = getSoldString(cp);
   			String idd = cp.getGroup();
   			if(miinus > 0 || kogus > 0){
   			
   			
   			
   			if(!kk.equals(oldcust)){
   				//Kliend Vahetusel lisame viimase rea juurde
   				if(ams.trim().length() > 0){
						ams = ams + "/" + oldmm + "" + mkogus;
					}else{
						ams =  oldmm + "" + mkogus;
		   						
					}
				
					int perc = ((int)(minus/pluss*100));
					if( perc > 0){
						if(perc > 100){
							ams = ams + " 100%";
						}else{
							ams = ams + " " + ((int)(minus/pluss*100)) + "%";	
						}
						
					}
					//ams = ams + " " + olddd;
					
					if(ams.length() > 28){
						ams = ams.substring(ams.length() - 28);
						ams = ams.substring(ams.indexOf("/")+1);
					}
					if(ams.length()== 0 ){
						ams = " ";
					}
					
					 CustProd cc = new CustProd(oldcust, oldprod, 0,0 ,0,
		             		ams,0,0,0," ");
					  custProds.add(cc);  
					 if(kk.equals("10113")){
					  System.out.println("asikliendivahetus  " + idd +  " "  + oldcust + " " + oldprod + " " + ams + " " + perc );
 						 
					 }
					 ams = "";
					 mkogus = 0;
					 oldmm = mm;
					 olddd = dd;
					 oldprod = tt;
				     oldcust = kk;
		   			 pluss =  0;
					 minus = 0;
				
   			}
   			
   			
			if(!tt.equals(oldprod)){
					if(ams.trim().length() > 0){
						ams = ams + "/" + oldmm + "" + mkogus;
					}else{
						ams =  oldmm + "" + mkogus;
		   						
					}
				
					int perc = ((int)(minus/pluss*100));
					if( perc > 0){
						if(perc > 100){
							ams = ams + " 100%";
						}else{
							ams = ams + " " + ((int)(minus/pluss*100)) + "%";	
						}
						
					}
					//ams = ams + " " + olddd;
					
					if(ams.length() > 28){
						ams = ams.substring(ams.length() - 28);
						ams = ams.substring(ams.indexOf("/")+1);
					}
					if(ams.length() == 0){
						ams = " ";
					}
					
					 CustProd cc = new CustProd(kk, oldprod, 0,0 ,0,
		             		ams,0,0,0," ");
					  custProds.add(cc);  
					  if(kk.equals("10113")){
						//  System.out.println("asitote vahetus  " + idd +  " "  + oldcust + " " + oldprod + " " + ams );
	 						 
						 }
						 
					    ams = "";
					    	mkogus = 0;
						
					    oldmm = mm;
					    olddd = dd;
						pluss =  0;
						minus = 0;
			   			oldprod = tt;
					
				}		
   					
   					if(kogus > 0){
   						pluss = pluss + kogus;
   					}
   					
   					if(miinus > 0){
   						minus = minus + miinus;
   					}
   						
   						
   					if(!oldmm.equals(mm)){//oli muudatus
   						if(ams.length() > 0){
   							ams = ams + "/" + oldmm + mkogus;
   		                }else{
                   		    ams = oldmm + mkogus;
   						}
   						mkogus = 0;
   					}
   				
   				if(kogus > 0){
						mkogus = mkogus + kogus;
		   			}
   				
   			 
   		    oldmm = mm; 
   		    olddd = dd;
   		}
   		
		}
   		if(ams.length() == 0){
   			ams = " ";
   		}
			 CustProd cc = new CustProd(oldcust, oldprod, 0,0 ,0,
           		ams,0,0,0," ");
			  custProds.add(cc);  
			//   System.out.println("asilopplopp  " + oldcust + " " + oldprod + " " + ams );
							 
					 
	  try {
		  System.out.println("CP valmis " + custProds.size());
		this.saveObject(custProds, "cp.vec");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	   
	   return custProds;
   }

   
   
   public Vector getCustomerProductsListVecOLD(){
	   this.getProducts(null);
	   System.out.println("KliendiTooted alustan");
	   Vector custProds = new Vector();
	    TreeMap sales = new TreeMap();
		
	   String[] list = new File(getDir()).list();
       for(int u = 0 ; u < list.length ; u++){
		  String fname = list[u];
	
	if(fname.startsWith("vnr_myyk") ){
	
	try{
	
	  File f = new File (getDir() + fname);
	 System.out.println(getDir() + fname);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
	db = dbf.newDocumentBuilder();      	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
  Document doc = db.parse(in,handler);
  Node root = getNode(doc, "vnr_myygid");     
  HashSet done = new HashSet();
Calendar cal = Calendar.getInstance();
cal.add(Calendar.DATE, -90);


  for (int i = 0; i < root.getChildNodes().getLength(); i++){
	
  	  Node rida = root.getChildNodes().item(i);
	
  	 try{     
  		if(rida.getNodeName().equals("vnr_myyk")){
  			String klientid = null;
  			 try{
   	    		  klientid = getNode(rida, "mp_id").getFirstChild().getNodeValue();
   	    		   
      	       }catch (Exception e){}
   	    	 
      	       String toode = null;
      	     try{
   	    		 toode = this.prodsx.get(getNode(rida, "va_id").getFirstChild().getNodeValue())+ "";
   	    	   }catch (Exception e){}
      	       
   	    	double kogus = 0;
      	     try{
   	    		 kogus = Double.parseDouble(getNode(rida, "kogus").getFirstChild().getNodeValue());
   	    	   }catch (Exception e){}
   	    	  Date dd = null; 
   	    	 String date = null;
   	    	try{
   	     	date = getNode(rida,"kp").getFirstChild().getNodeValue();
     		   dd = getDate2(date,0); 	
   	            }catch (Exception e){}
   	       if (dd != null && dd.after(cal.getTime())){
   	    	CustProd cp = new CustProd(klientid, toode, kogus,0 ,0,
             		date.substring(5),0,0,0," ");
   	         
   	         sales.put(klientid + "" + toode + "" + date + "" + kogus, cp);
   	      
   	       }
   		}
    	
  	 }catch(Exception r){
  		 
  	 }
  
  	 
   }		
    		
	}catch(Exception t){
		t.printStackTrace();
	}
 	}	
   	}
       
  	 //System.out.println("sales suurus " + sales.size());
     	Iterator itera = sales.keySet().iterator();
   		String oldprod = null;
   		String oldcust = null;
   		String ams = "";
   		double pluss = 0;
   		double minus = 0;
   		int i = 0;
   		int u = 0;
   		while (itera.hasNext()){
   			String key = itera.next()+ "";
   			CustProd cp = (CustProd)sales.get(key);
   			
   			if(oldcust == null){
   				oldcust = cp.getCustID();
   			}
   			
   			if(oldprod == null){
   				oldprod = cp.getProdID();
   			}
   			
   			String tt = cp.getProdID();
   			String kk = cp.getCustID();
   			int kogus = (int)cp.getLastAmount();
   			String dd = cp.getLastAmountDate();
   				if(!tt.equals(oldprod)){
   					
   					int perc = ((int)(-minus/pluss*100));
   					if( perc > 0){
   						if(perc > 100){
   							ams = ams + " 100%";
   						}else{
   							ams = ams + " " + ((int)(-minus/pluss*100)) + "%";	
   						}
   						
   					}
   					ams = ams + " " + dd;
   					
   					if(ams.length() > 28){
   						ams = ams.substring(ams.length() - 28);
   						ams = ams.substring(ams.indexOf("/")+1);
   					}
   					
   					if(ams.length() == 0){
   						ams = " ";
   					}
   					
   					 CustProd cc = new CustProd(kk, oldprod, 0,0 ,0,
   		             		ams,0,0,0," ");
   					  custProds.add(cc);  
   					 
   					 ams = kogus +"";
   					if(kogus > 0){
   						pluss =  kogus;
   						minus = 0;
   					}else{
   						minus = kogus;
   						pluss = 0;
   					}
   					
   				}else{
   					if(kogus > 0){
   						pluss = pluss + kogus;
   					}else{
   						minus = minus + kogus;
   					}
   					ams = ams+"/"+kogus;
   				}
   			
   			oldprod = tt;
   			
   			
   		}
	   
	  try {
	//	  System.out.println("CP valmis " + custProds.size());
		this.saveObject(custProds, "cp.vec");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	   
	   return custProds;
   }

   
   
   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Rauta";
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


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private Vector realpayers = new Vector();
   private TreeMap payers = new TreeMap();
   private Vector prodsInUse = new Vector();
   private Vector pricelists = new Vector();
   private Vector custtsUsed = new Vector();
   

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
	  
   	
   	  Enumeration enumer = tagKokku.keys();
   	   String ymonth = Converter.getYYYYMMString(-1);
	   String ymonthl = Converter.getYYYYMMString(-2);
	   String tag = "";
   	  while(enumer.hasMoreElements()){
   		  String aa = enumer.nextElement() + "";
   		 
   		  
   			String hind = pf.format(tagKokku.get(aa));
   		 if(tag.equals("")){
         		  tag = "Lehepunkt keskmine tagastus|     " + aa + " " + hind; 
 			 }else{
         		  tag = tag + "|     " + aa + " " + hind + "|"; 
 			 }
   		  }
   		
   	enumer = tagAg.keys();
   	  
   	while(enumer.hasMoreElements()){
 		  String aa = enumer.nextElement() + "";
 		  String per = aa.substring(0,aa.indexOf("-"));
 		  String agent = aa.substring(aa.indexOf("-")+1);
 		  if(agents.get(agent) != null){
 			 Agent ag = (Agent)agents.get(agent);
 			String hind = pf.format(tagAg.get(aa));
 			if(!ag.getStart().contains("Minu t") ){
 				ag.setStart(tag + "|Minu tagastus :|    " + per + " " + hind + "|" );
 			}else{
 				ag.setStart(ag.getStart() + "   " + per + " " + hind  );
 			}
     	  }
 		}
	
    enumer = this.agBudgets.keys();
 	  
   	while(enumer.hasMoreElements()){
 		  String aa = enumer.nextElement() + "";
 		  String agent = aa;
 		  if(agents.get(agent) != null){
 			 Agent ag = (Agent)agents.get(agent);
 		  	String asi =(String) agBudgets.get(aa);
 				ag.setStart(ag.getStart() + "|Budg :|    " + asi + "|" );
 			}
 		}
	}		
}
