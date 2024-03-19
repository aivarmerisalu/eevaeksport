package eevaeksport.driver;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
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
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
import imageUtil.ImageLoader;




public class RautaDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public RautaDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    company = getParent().getProps().getProperty("companycode");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.company");
    rcompanyx = getParent().getProps().getProperty("rest.companyx");
    
    nf.setMaximumFractionDigits(2);
    nf.setMinimumFractionDigits(2);
    nf.setGroupingUsed(false);
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
  String rcompanyx = null;
  
  
  
  private Hashtable dates = new Hashtable();
  
 
  
  public String getSaveDebtInfo() {
	 
	  makePictureCat();
	
	  System.exit(0);
   	return null;
   }
  
  public void shrinkAndCopyFile (String in, String out) throws Exception {

      try {
      	BufferedImage inb = ImageIO.read(new File(in));
      	BufferedImage newImage = new BufferedImage(inb.getWidth(), inb.getHeight(), BufferedImage.TYPE_INT_RGB);
      	Graphics2D g = newImage.createGraphics();
      	g.drawImage(inb, 0, 0, null);
      	g.dispose();
      	 File outputfile = new File(out);
      	 ImageIO.write(newImage, "jpg", outputfile);
      	
        ImageLoader.fromFile(out).getResizedToWidth(300).writeToJPG(new File(out), 0.7f);//.soften(0.95f)
      }
      catch (Exception ioe) {
      	ioe.printStackTrace();
      }
        }
  
  
  
  private void makePictureCat(){
	    
  	
  	String picts = parent.getProps().getProperty("pict.dir");
	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	    String pictssou  = parent.getProps().getProperty("pictpda.source");
	// makeNormpict(pictssou,picts);

  	try {
  	TreeMap prods = this.getProducts(null);

       StringBuffer buf = new StringBuffer();
       StringBuffer buf0 = new StringBuffer();
       int a = 0;
       int b = 0;
       int c = 0;
       Iterator enume = prods.keySet().iterator();
       System.out.println("Piltide leidmine ");
  	    while (enume.hasNext()) {
  	    	String asi = (String)enume.next();
  	    	Product prod = (Product)prods.get(asi);
  	    	a++;
  	    	c++;
  	    	if(c == 100){
  	    		  c =0;
  	    		//  System.out.println("Piltide leidmine "  + a);	
  	    	}
  	     
                String toode = prod.getID().trim();  
                toode = toode.replace('/', '_');
                File f = new File(picts + toode+".jpg");
                String len = "0";
                if(f.exists()){
                  len =	f.length() + "";
                }
                if(len.equals("0")){
                	//System.out.println(toode+".jpg;"  + len);
                	buf0.append(toode+".jpg\n");
                }else{
                	buf.append(toode+".jpg;"  + len + "\n");
          	    	b++;
                }
                }
  	
  	
  	    //oleasoleva kataloogi kustutamine
  //	  System.out.println("Kataloogi kustutamine "  + a);	    
              File f = new File("list.txt");
              if (f.exists())f.delete();
              
                try {
          		PrintWriter os = new PrintWriter(new FileWriter(
          				  "list.txt", true));
          		os.write(buf.toString());
          		os.close();
          	
          	  }catch (Exception ex) {
          		ex.printStackTrace();
          	  }
          	//  System.out.println("buf 0 " + a  + " " +  b + " "+ buf0.toString());
          	  File f0 = new File("list0.txt");
          	
          	  	
                   if (f0.exists())f0.delete();
                
                  try {
            		PrintWriter os1 = new PrintWriter(new FileWriter(
            				  "list0.txt", true));
            		os1.write(buf0.toString());
            		os1.close();
            		//System.out.println( buf.toString());
            	  }catch (Exception ex) {
            		ex.printStackTrace();
            	  }

  //Kataloogi sisude kustutamine
            	  
 
         	 
         	 
       	 File res2 = new File (pictspda);
           String[] ff2 = res2.list();
       	 for(int i = 0;ff2.length>i;i++){
           //	System.out.println("fff " + ff2[i]);
           	(new File ( pictspda + ff2[i])).delete(); 
        
           }
//       	 Lï¿½ppes kustutamine 
       	 
         System.out.println("Piltide kopeerimine ");	
         //Failide kopeerimine  
           String[] gg = new File(picts).list();
        	  //System.out.println("gg " + gg[0] + " " + gg.length);
           
            for(int i = 0;gg.length>i;i++){
            	
            	File in = new File ( picts + gg[i]); 
            	if(in.isFile()){
            	
            	File inpda = new File ( pictspda + gg[i]); 
           
                  	Converter.shrinkAndCopyFile(in,inpda);
            	}
            }
            File fff =  new File (pictspda +  "list.txt");
      	  copyFile(f,fff);  	
      	  File fff0 =  new File (pictspda +  "list0.txt");
      	  copyFile(f0,fff0);  	
  	} catch (Exception e) {
  	    getParent().showError(e);
  	}
  
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
	    		String prodID = (itera.next() + "").trim();
	    		String lineinfo = "";
	    		MatrixLine mline = new MatrixLine();
		        	mline.setCliGroup(custID.trim());
		        	mline.setLineinfo("MULTI " + vvv.get(prodID));
		        	lineinfo = "  .NEW";
		        	String kood = prodsx.get(prodID.trim()) + "";

	  	          	mline.setProdID( kood); 
		    	  	String name = " "; 
		    	  	try{
		    	  	  	name = ((Product)prodHash.get(kood)).getNameSorting().trim();
			     	}catch(Exception r){
			     		name = " " + kood;
		    	  	}
		    	 pre.put(custID  + lineinfo + name  ,mline);
		    	// System.out.println("loeme stuffi " +  custID  + " " + prodID + " " + mline.getLineinfo());
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
  private Hashtable<String,Double> myykKlKokku = null;
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
 
  
       private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
     
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
			    	    	 
			    	    	  int  sisse = Integer.parseInt( getNode(rida,"kos").getFirstChild().getNodeValue().trim());
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
			    	    	  if(myydud == null) {
			    	    		  myydud = new HashSet();
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
		   
		  /* if(myykKlKokkuo.size() > 0){
			   myykKlKokkuo = calculateShare(myykKlKokkuo);
			   
				 try{
					   saveObject(myykKlKokkuo, "myykKlKokku.vec"); 
				   }catch(Exception e){
					  e.printStackTrace(); 
				   }
				
			   }*/
		   
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
		 // System.out.println("Jooksvad myygid " + jooksev + " " + tm.toString());
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
   
   
   private TreeMap makePlans(){
	   TreeMap res = new TreeMap();
	  
	   try{
		
	   Calendar cal = Calendar.getInstance();
	   String month = (cal.get(Calendar.MONTH)+1)+ "";
	   if (month.length() == 1){
		   month = "0" + month;
	   }
	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
	   int a = 0;
	   File f = new File (getExcelDir() + "TP_Budget.xlsx");
		  FileInputStream	 ff = new FileInputStream(f);
	
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheet(jooksev);
		System.out.println("jooksev " + jooksev + " " + sheet);
		XSSFRow row = null; 
				ff.close();	
				if(sheet != null){
				
					
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						 row = sheet.getRow(i);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}
					String gr1 = null;
					String gr2 = null;
					String gr3 = null;
					String gr4 = null;
					
					for(int i = 0 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String agent = null;
						double g1 = 0;
						double g2 = 0;
						double g3 = 0;
						double g4 = 0;
						
					if(i == 0){
						//loemepäised sisse
						
						gr1 = (row.getCell((short) 1).getStringCellValue());
						gr2 = (row.getCell((short) 2).getStringCellValue());
						gr3 = (row.getCell((short) 3).getStringCellValue());
						gr4 = (row.getCell((short) 4).getStringCellValue());
						
						
					}else{
						
						try {
							agent = (row.getCell((short) 0).getStringCellValue());
   					      } catch (Exception e) {
   						   e.printStackTrace();
   					      }
						System.out.println(agent);
						try {
						g1 = Double.parseDouble(row.getCell((short) 1).getNumericCellValue()+ "");
						g2 = Double.parseDouble(row.getCell((short) 2).getNumericCellValue()+ "");
						g3 = Double.parseDouble(row.getCell((short) 3).getNumericCellValue()+ "");
						g4 = Double.parseDouble(row.getCell((short) 4).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					 e.printStackTrace();	
 					      }
						TreeMap asi= new TreeMap();
						
				          asi.put(gr1, g1);
				          asi.put(gr2, g2);
				          asi.put(gr3, g3);
				          asi.put(gr4, g4);
						
						if(agent != null){
							
						    res.put(agent,asi );
										
						}
   					
   					
						
					}
						
					}
				
				
				
				
				}
        		   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
	   
	   System.out.println("RESS " + res.toString());
	   
	   return res;
   }
   
   
   private  TreeMap agents1 = new TreeMap(); 
   private  TreeMap agents2 = new TreeMap(); 
   
   public TreeMap getAgents(){
	 //  getDebt(null);
	   

	  //   File ff = new File(this.getExcelDir() + "graafikud.xlsx");
	   //  Converter.sendExcelVisits(ff,"rivona.lt","rivona.lt","vilnius222");
	   //  Converter.sendExcelVisits(ff,"klp","klp","dione");
	   //  Converter.sendExcelVisits(ff,"orklafood","orklafood","spilva123");
	    // Converter.sendExcelVisits(ff,"orklalv","orkla.lv","Riga111");
	   //  Converter.sendExcelVisits(ff,"droid@skonis-kvapas.lt","skonis","skonis12");
	    // Converter.sendExcelVisits(ff,"pda@dunker.ee","dunker.ee","demo21");
	   // Converter.sendExcelVisits(ff,"ladu@balbiino.ee","balbiino","demo24");
	     
	 //  makePlans();
	 //  getCustomerProductsListVec();
	 //  getCustomerProductsList(null);
	 //  getJooksevMyyk(true);
	 //  getJooksevMyyk(false);
		      TreeMap tm = new TreeMap(); 
//System.out.println(Converter.getDays("E1K32R2"));
//System.out.println(Converter.getDays("ETKNRLP"));
  
		      
 	try {
 		
 		
 	    
    	Properties props = new Properties();
		try {
			props = EksportFrame.loadProps("asendajad.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
 		
		 Properties propse =  new Properties();
		    try{
		    	propse =  EksportFrame.loadProps("emails.txt");
		  	  
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
 		
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
                    

               		String agentAGENTS = props.getProperty(sm.getID());
             		if(agentAGENTS != null){
                        StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
             	    while(stag.hasMoreTokens()){  	
                      sm.addDubbler(stag.nextToken());
                      System.out.println("Lisan Duubleid " + sm.getID() + " " + sm.getDubbler().toString());
             	       }
                      }
             		 sm.setLanguage("EST");
                     sm.setCountry("ee");
                     sm.setPassWord("0000");
                     
                     String email = " ";
                     
                     if(propse.get(sm.getID()) != null){
                   	  email = Converter.parseFirstEmail(propse.get(sm.getID())+"");
                     }
                     
                     
                     if(email.length() > 3){
                   	  sm.setEmail(email);
                     }
                     if(email.length() > 1){
                   	  sm.setActive(true);
                     }
                     
             		 
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
 	
 	
     System.out.println("Agendilist tehtud" ); 
     try {
		tjuhid =  readTootejuhid();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
     if(rcompany != null){
	    	Converter.sendAgentJson(tm, rcompany, false);
	    }
    
     return tm;
   }
   
   Hashtable tjuhid = null;
   public Hashtable readTootejuhid() throws IOException{

	    Hashtable result = null;
	    File f = new File(getExcelDir() + "tootejuhid.txt");
	    if (f.exists()){
	      result = new Hashtable();
	      InputStreamReader is = new FileReader(f);
	      BufferedReader br = new BufferedReader(is);
	      String line = null;
	      while( ( line = br.readLine()) != null){
	      	String id = null;
	      	String aadr = null;
	        StringTokenizer st = new StringTokenizer(line, ";");
	        if (st.hasMoreTokens()){
	        	id = st.nextToken();
	        	 if (st.hasMoreTokens()){
	               aadr = st.nextToken();        	 
	          result.put(id,aadr);
	        }
	      }
	    }
	      is.close();
	      br.close();
	    }
	    return result;
	  }

   Hashtable<String,Double>   myykAgentEelmKokku  = null;
   
   private void readOBJ(){
	   try {
		   myykAgentEelmKokku = (Hashtable<String,Double>) readObject("myykAgentEelmKokku.vec");
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
	
	 try {
		 multJTooted = (Hashtable<String, TreeMap<String,String>>) readObject("multJTooted.vec");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	
	 try {
			sortSuurKl = (Hashtable<String, Vector<String>>) readObject("sortSuurKl.vec");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	
	 try {
		 myydud = (HashSet) readObject("myydud.vec");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
   
		 try {
			 tagKokku =  (Hashtable<String, Double>) readObject("tagKokku.vec");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			 try {
				 myykKlgruppKokku =  (Hashtable<String, Double>) readObject("myykKlgruppKokku.vec");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
			 
			
				 try {
					 myykKlgruppAgent =  (Hashtable <String,Hashtable<String,Double>>) readObject("myykKlgruppAgent.vec");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
	   
			try {
				tagKl =  (Hashtable<String, Double>) readObject("tagKl.vec");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				try {
					hindKlKaan =  (Hashtable<String, Double>) readObject("hindKlKaan.vec");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					
					try {
						tagAg =  (Hashtable<String, Double>) readObject("tagAg.vec");
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						try {
							custProds =  (Vector) readObject("cp.vec");
							} catch (IOException e) {
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
					  
			  
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
      	  // System.out.println("Inventprods lõpetan " + sb.toString());
      	   
      	   
      	   
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
    	   int alg = 21;
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
        	
        	     
        	   
     		    myykKlKokku = new Hashtable<String,Double>();
     		  
        		   
        		   String[] list = new File(getDir()).list();
        			
        		   // 1. kliendi mï¿½ï¿½gid jooksvas kuus
        	    
        		  
        			  for(int u = 0 ; u < list.length ; u++){
        				  String fname = list[u];  
        		
        				  if(fname.indexOf("vnr_myyk_jkuu_rp_") != -1){ // "vnr_myyk_jkuu_"
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
            			    	    	
            			    	    	  if(myykKlKokku.get(klient) == null ){
            			                    	myykKlKokku.put(klient,reasum );
            			                    }else{
            			                    	double olemas = myykKlKokku.get(klient);
            			                    	myykKlKokku.put(klient,reasum + olemas);
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
                     tm.put(key, ((int) bud + " eur / " + (int)done) + " eur / " + perc + "%");
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
              	    	 // e.printStackTrace();
              	     }
              	
              	      int perc = ((int)(done/bud *100));
              	      if(perc > 100) perc = 100;
                   agBudgets.put(key, ((int) bud + " eur / " + (int)done) + " eur / " + perc + "%");
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
              	    	agBudgets.put(key,asi +  "|Eelmine: " + ((int) bud + " eur / " + (int)done) + " eur / " + perc + "%");
              	      }else{
              	    	agBudgets.put(key, "Eelmine: " + ((int) bud + " eur / " + (int)done) + " eur / " + perc + "%");
                         	    	  
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
    						//System.out.println("W " +  week);
    					
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

    	 			 DataFormatter formatter = new DataFormatter();
    					 
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
      								kett1 =  formatter.formatCellValue(row.getCell((short) 0));
      	    					} catch (Exception e) {
      	    						
      	    					}
      	    					try {
      								klient =  formatter.formatCellValue(row.getCell((short) 1));
      	    					} catch (Exception e) {
      	    						//System.out.println("iiii " + i);
      	    					}
      	    				
      	    					
      	    					if(klient != null) {
      	    						if(excelketid.get(klient.trim()) !=  null) {
      	    							 excelketid.put(klient.trim(),excelketid.get(klient.trim()) + "," + kett1.trim() );
      	    						}else {
      	    							 excelketid.put(klient.trim(),kett1.trim() );
      	    						}
      	    						
      	    					}
      						   
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
    						for(int i = 0 ; i < a+1 ; i++){
    							
    							 row = sheet.getRow(i);
    							String prodid = null;
    							try {
    								prodid = (int) (row.getCell((short) 0)
    										.getNumericCellValue()) + "";
    							} catch (Exception e) {
    								try {
    								prodid = (row.getCell((short) 0).getStringCellValue());
    								} catch (Exception r) {
    									
    								}
    							}
    							if(prodid != null) {
    							
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
    				    		//System.out.println("AAA " + kett + " " + sisu + "|  " + nimi + "|  " + algus + "->" + lopp + "|");
    							MatrixLine mm = new MatrixLine();
    							mm.setCliGroup(kett);
    							mm.setProdID(prodid);
    							mm.setEndDate(lopp);
    							mm.setStartDate(algus);
    							mm.setLineinfo(sisu);
    						    matrixlines.add(mm);
    						 }
    						
    							}
    						}
   				
    							}
    	         		
                 	  }catch (Exception e){
                        e.printStackTrace();	
    	         	  }
            
                 	  Iterator itera = excelketid.keySet().iterator();
                 	 while (itera.hasNext()){
                 		 String custid = (String) itera.next();
                 		 String kett = ((String)excelketid.get(custid)).trim();
                 		 StringTokenizer st = new StringTokenizer(kett,",");
                 		
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
      
      
      
      
      private   TreeMap getTop3Asum(){
	     TreeMap tm = new TreeMap();
	     System.out.println("Asumid TOP3 alustan");
	     TreeMap mk = getMaakond();
	   try {
			  File f = new File (getDir() + "asum_va_top.xml");
	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	              
	          Node root = getNode(doc, "asum_va_topid");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("asum")){

		    	    	String id =  getNode(rida,"asum_kd").getFirstChild().getNodeValue();
		    	    	String nimi = id +  " 90 päeva " + "| |";;
		    	    	Node vad = getNode(rida, "valjaanded");
		    	    	for (int e = 0; e < vad.getChildNodes().getLength(); e++){
		    	    		 Node va = vad.getChildNodes().item(e);
		    	    		 if(va.getNodeName().equals("va")){
		    	    	
		    	    			 String vaid =  getNode(va,"va_id").getFirstChild().getNodeValue();
				         		 String kogus =  getNode(va,"kogus_myyk").getFirstChild().getNodeValue();
				         		 String summa =  getNode(va,"summa_mh").getFirstChild().getNodeValue();
				         	//	 System.out.println("TOP3" + vaid + " "+ prodsx.get(vaid)+ " " + kogus + " " + summa);
				         		if(prodsx.get(vaid)!= null && prodHash.get(prodsx.get(vaid)) != null){
				         		Product prod = (Product)prodHash.get(prodsx.get(vaid));
				         		if(nimi.equals("")){
				         			
				         			nimi =  kogus + " tk. ("+summa+" eur) " + prod.getName() + "|";
					        	}else{
 				         			nimi = nimi + kogus + " tk. ("+summa+" eur) " + prod.getName() + "|";
 					         	}
				         		}
		    	    			 
		    	    		 }
		    	    	}
		    	    	
		         		// System.out.println("TOP3" + nimi);
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
      
      private   TreeMap getTop3Cust(){
 	     TreeMap tm = new TreeMap();
 	     System.out.println("Kliendid TOP3 alustan");
 	     this.getProducts(null);
		// System.out.println("TOP3" + prodsx);
	         
 	     //TreeMap mk = getMaakond();
 	   try {
 			  File f = new File (getDir() + "mp_va_top.xml");
 	  		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	  	     DocumentBuilder db;
 	  		db = dbf.newDocumentBuilder();      	
 	      	FileInputStream in = getCleanInputStream(f);
 	      	String handler = null;
 	          Document doc = db.parse(in,handler);
 	              
 	          Node root = getNode(doc, "mp_va_topid");     


 	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
 	        	  Node rida = root.getChildNodes().item(i);
 	        	
 	    	    try{
 	    	    	if(rida.getNodeName().equals("mp")){

 		    	    	String id =  getNode(rida,"mp_id").getFirstChild().getNodeValue();
 		    	    	String nimi = "";
 		    	    	Node vad = getNode(rida, "valjaanded");
 		    	    	for (int e = 0; e < vad.getChildNodes().getLength(); e++){
 		    	    		 Node va = vad.getChildNodes().item(e);
 		    	    		 if(va.getNodeName().equals("va")){
 		    	    	
 		    	    			 String vaid =  getNode(va,"va_id").getFirstChild().getNodeValue();
 				         		 String kogus =  getNode(va,"kogus_myyk").getFirstChild().getNodeValue();
 				         		 String summa =  getNode(va,"summa_mh").getFirstChild().getNodeValue();
 				         	//	 System.out.println("TOP3" + vaid + " "+ prodsx.get(vaid)+ " " + kogus + " " + summa);
 				         		if(prodsx.get(vaid)!= null && prodHash.get(prodsx.get(vaid)) != null){
 						         	
 				         			Product prod = (Product)prodHash.get(prodsx.get(vaid));
 				         		if(nimi.equals("")){
 				         			nimi =  kogus + " tk. ("+summa+" eur) " + prod.getName() + "|";
 					         			
 				         		}else{
 				         			nimi = nimi + kogus + " tk. ("+summa+" eur) " + prod.getName() + "|";
 					         	}
 				         		}
 		    	    			 
 		    	    		 }
 		    	    	}
 		    	    	
 		         		 //System.out.println("TOP3" + nimi);
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
       
      
      private String getTMStr (TreeMap tr ) {
    	  String res  = "";
    	  
    	Iterator it=  tr.keySet().iterator();
    	while (it.hasNext()) {
    		String hh = it.next()+"";
    		res = res + hh + ";" + tr.get(hh) + "\n";
    	}
    	  
    	  return res;
      }
      
      
      
   public TreeMap getCustomers(Agent agent){
	   
	   readOBJ();
	   Converter.getManagersReplacements(agents2, rcompanyx, user , pass);

	   
	   debts = new Vector();
	   TreeMap debtover = new TreeMap();
	   TreeMap debttotal = new TreeMap();
	   TreeMap inventuurDone = new TreeMap();
	   Vector vec = new Vector();
	   
	   TreeMap suhe = new TreeMap();
		
	   
	   
	   
	   
	   try {
		   
			  File f = new File (getDir() + "mp_per_suhe_myyk.xml");
		  		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	    DocumentBuilder db;
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		        Document doc = db.parse(in,handler);
		          in.close();
		          Node root =  getNode(doc, "mp_per_suhe_myygid");     
		          
		          for (int i = 0; i < root.getChildNodes().getLength(); i++){
		        	  Node rida = root.getChildNodes().item(i);
		        	
		    	    try{
		    	    	if(rida.getNodeName().equals("mp")){
		    	    			    	    
		         		  String cus = getNode(rida,"mp_id").getFirstChild().getNodeValue();
		         		  String pros = getNode(rida,"prots").getFirstChild().getNodeValue();
		         		  
	                       suhe.put(cus, pros);
		    	    	} 
		    	    	}catch(Exception g ){
			 			g.printStackTrace();  
			 		  }
		         	  

		 		}
		 		  
		 		   }catch(Exception r){
		 			   r.printStackTrace();
		 		   } 	
		 		   System.out.println("Suhe  seis " +suhe.size());

		   
	   
	   try {
		   File file = new File ( "inventorydone.txt");
		   FileReader ff = new FileReader(file);
			BufferedReader br = new BufferedReader(ff);
			String line = null;
			while( (line = (br.readLine())) != null){
				StringTokenizer st = new StringTokenizer(line, ";");
				if (st.hasMoreTokens()){
					String cuID = st.nextToken().trim();
					String date = st.nextToken().trim();
					inventuurDone.put(cuID, date);
	  
				}
			}	 }catch(Exception g){
		  // g.printStackTrace();
	   }
		   
			
	   
	   try {
		   
		  File f = new File (getDir() + "mp_arve_tasumata.xml");
	  		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	    DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	        Document doc = db.parse(in,handler);
	          in.close();
	          Node root =  getNode(doc, "mp_arved_tasumata");     
	          
	          for (int i = 0; i < root.getChildNodes().getLength(); i++){
	        	  Node rida = root.getChildNodes().item(i);
	        	
	    	    try{
	    	    	if(rida.getNodeName().equals("mp_arve")){
	    	    			    	    
	         		  Debt debt = new Debt();
	         		  String cus = getNode(rida,"mp_id").getFirstChild().getNodeValue();
	         		  String payer = getNode(rida,"firma_id").getFirstChild().getNodeValue();
	         		  debt.setCustID(cus);                     
                      String number = getNode(rida,"arvenr").getFirstChild().getNodeValue(); 
                      debt.setDoknr(number);
                      String amo = getNode(rida,"summa_tasumata").getFirstChild().getNodeValue();
                      
                      debt.setSum(Double.parseDouble(amo));
                      debt.setDuedate(getDate2(getNode(rida,"maksetahtaeg").getFirstChild().getNodeValue(),0));                  
                      debt.setDate(getDate2(getNode(rida,"arvekuup").getFirstChild().getNodeValue(),0));
                      debt.setDue((getNode(rida,"hilin_pv").getFirstChild().getNodeValue())+"");
                      boolean over = false;
                      try{
                        int aa = Integer.parseInt(debt.getDue());
                          if(aa > 0){
                        	  over = true;
                          }  
                      }catch (Exception e){
                    	  
                      }
                      boolean goes = true;
                      if((debt.getSum() < 0.05 && debt.getSum() > -0.05) || debt.getCustID().equals("1000185") ||debt.getCustID().equals("1000209") ||
                    		  debt.getCustID().equals("1000003") || debt.getCustID().equals("1016117")
                    		  || debt.getCustID().equals("1000055")  || debt.getCustID().equals("1015211")
                    		  || debt.getCustID().equals("1000330")  || debt.getCustID().equals("1000229") ){
                    	  goes = false ;
                      }
                      if(goes){
                    	if(over){

                      	  if(debtover.get(payer) != null){
                      		  double asi = Double.parseDouble(debtover.get(payer) +  "");
                      		  asi = asi + debt.getSum();
                      		  debtover.put(payer, asi + "");
                      	 }else{
                      		 debtover.put(payer, debt.getSum() + "");
                      	  }	
                      	  
                      	  if(!cus.equals(payer)){
                      		if(debtover.get(cus) != null){
                        		  double asi = Double.parseDouble(debtover.get(cus) +  "");
                        		  asi = asi + debt.getSum();
                        		  debtover.put(cus, asi + "");
                        	 }else{
                        		 debtover.put(cus, debt.getSum() + "");
                        	  }	 
                      	  }
                    	}

                    	  if(debttotal.get(payer) != null){
                    		  double asi = Double.parseDouble(debttotal.get(payer) +  "");
                    		  asi = asi + debt.getSum();
                    		  debttotal.put(payer, asi + "");
                    	 }else{
                   		  debttotal.put(payer, debt.getSum() + "");
                    	  }  
                    	  
                    	  
                    	  if(!cus.equals(payer)){
                        		if(debttotal.get(cus) != null){
                          		  double asi = Double.parseDouble(debttotal.get(cus) +  "");
                          		  asi = asi + debt.getSum();
                          		debttotal.put(cus, asi + "");
                          	 }else{
                          		debttotal.put(cus, debt.getSum() + "");
                          	  }	 
                        	  }
                    	  
                    	  debts.addElement(debt);
                      }
                     
	    	    	}}catch(Exception g ){
		 			g.printStackTrace();  
		 		  }
	         	  

	 		}
	 		  
	 		   }catch(Exception r){
	 			   r.printStackTrace();
	 		   } 	
	 		   System.out.println("volg seis " + debts.size());

	 		  if("true".equals((parent.getProps().getProperty("rest.loadvisitlines")))){
  			    Converter.loadVisitLines("plan", rcompanyx, user, pass);
  	    	    Converter.loadVisitLines("done", rcompanyx, user, pass);
  	    	    Converter.loadVisitLines("extra", rcompanyx, user, pass);
  	 	 }
	   
	   
	     makeInventoryData();
	   
	     TreeMap tm = new TreeMap();
	     TreeMap tmid = new TreeMap();
	     TreeMap budgets = new TreeMap();
	     System.out.println("Kliendid alustan");
	     TreeMap kampaaniad =  getKampaaniad();
	     
	     String inP = parent.getProps().getProperty("doc.origpath");
	      String outP = parent.getProps().getProperty("doc.destination");
	      HashSet codes = new HashSet();
	     
	     
	     Converter cc = new Converter();
	 	//  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	
	     TreeMap nonvisited = Converter.getNonVisited();
 		 Converter.saveFileInUTF8( "nonevisited.txt", getTMStr(nonvisited));
	     budgets = getBudgets(); 
	     kettKlient = new TreeMap();
	     TreeMap kt = getKett();
	     TreeMap asum = getAsum();
	     TreeMap top3Cust = getTop3Cust();
	     TreeMap top3Asum = getTop3Asum();
	     
	     this.customers = new ArrayList();
	     TreeMap visits = null;
	     try {
	     // visits = getVisits();
	     } catch(Exception e) {
	    	 
	     }
	     int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
	   
	   try {
		   StringBuffer sbcontact = new StringBuffer();
           
           
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
	         		 String askd = "";
		         		 try{
		         			
		         			 linn =   getNode(rida,"asum_kd").getFirstChild().getNodeValue();
		         			askd = linn;
		         			 if(asum.get(linn) != null){
		         				 linn = asum.get(linn)  + "";
		         			 }
			         		 aadr = aadr + " " +(deCode(linn ));  	  
		         		  }catch(Exception ea){
		         		  }
		         		  
	         		  
	         		  String id =  getNode(rida,"myygipunkt_id").getFirstChild().getNodeValue();
	         		  Customer customer = new Customer(id);
	         		  String name =  getNode(rida,"nimi").getFirstChild().getNodeValue() + " " + id + " " + linn;
	         		  customer.restCity = linn;
	         		  customer.setCountry("Estonia");
	         		  
	         		 try{ String blocked  =  getNode(rida,"mp_olek_kd").getFirstChild().getNodeValue();
	         		 if(blocked.equals("SULETUD")){
	         		      customer.setSalesBlock(true);
	         		     name = "x " + name; 
	         		  }
	         	   }catch(Exception e){}
	         		
	         		 try{
	         			  aadr =  getNode(rida,"aadress").getFirstChild().getNodeValue();
	         			  customer.restStreet = aadr;
	         		  }catch(Exception e){
	         		  }
	         		 try{
	         			   customer.setPayer(getNode(rida,"firma_id").getFirstChild().getNodeValue());
	         			   if(debtover.get(customer.getPayer()) != null){
	         				   customer.setDebt(Double.parseDouble(debtover.get(customer.getPayer()) + ""));
	         			   }
	         			  if(debtover.get(customer.getID()) != null){
	         				   customer.setDebt(Double.parseDouble(debtover.get(customer.getID()) + ""));
	         			   }
	         			   
	         			   
	         			  if(debttotal.get(customer.getPayer()) != null){
	         				   customer.setUsedCredit(Double.parseDouble(debttotal.get(customer.getPayer()) + ""));
	         			   }
	         			 if(debttotal.get(customer.getID()) != null){
	         				   customer.setUsedCredit(Double.parseDouble(debttotal.get(customer.getID()) + ""));
	         			   }
	         			   
	         		  }catch(Exception e){
	         		  }
	         		
	         		  
	         		 
	         		 try{
	         		 String post =  getNode(rida,"postiindeks").getFirstChild().getNodeValue(); 
	         		 customer.restZipCode = post;
	         		 aadr = aadr + " " +(deCode(post));  	  
         		  }catch(Exception ea){
         		  }
         		  
         		  
         		   if(this.myykKlgruppAgent.get(customer.getID()) != null){
         			   
         			   
         			Enumeration enume =   myykKlgruppKokku.keys();
         			String total = "";
         			TreeMap sorted = new TreeMap(); 
         			while (enume.hasMoreElements()){
         				String key = (String)enume.nextElement();
         				int val = (myykKlgruppKokku.get(key)).intValue();
         				if(val > 0){
         					
         					if(val < 6){
         						
       						int val2 = 0;
         						if(myykKlgruppAgent.get(customer.getID()).get(key)!= null){
         							val2 =  myykKlgruppAgent.get(customer.getID()).get(key).intValue();
         						}
                            
         						//kui on erinevus yle kolme
         						if(val-val2 >2 ||val-val2 < -2){
                            	  total = total + key +":" + val+ "," + val2 + "|";
                            	  sorted.put(val + key, key +":" + val+ "," + val2 + "|");
           					  }
             					
         					}else{
         						
         						int val2 = 0;
         						if(myykKlgruppAgent.get(customer.getID()).get(key)!= null){
         							val2 =  myykKlgruppAgent.get(customer.getID()).get(key).intValue();
         						}
                            
         						//kui on erinevus yle kolmkymne viie protsendi 
         						if((double)val/(double)val2 > 1.35 || (double)val/(double)val2 < 0.65){
                            	  total = total + key +":" + val+ "," + val2 + "|";
                             	  sorted.put(val + key, key +":" + val+ "," + val2 + "|");
                 					
         						}
         					}
         				}
         			}
         			
         		Iterator iters =	sorted.values().iterator();
         			String erss = "";
         		while (iters.hasNext()){
         			erss = erss + iters.next();
         		}
       	        	customer.addToCustDataVec(customer.getID()+ ";sales;chartpie2;" +
       	        			"CHART2 LIIGI MÜÜGI VÕRDLUS :|Tooteliik|Kett %|Klient%|%|eur|0|0|" + erss);
       	        }
	         		  
	         		 customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr), "Estonia"));
	         		  
	         		 if(visits != null && visits.get(customer.getID()) != null){
	         			 String visit = "" + visits.get(customer.getID());
	         			 if(visit.equals("KV1-3")){
	         				 if(month == 1 || month == 2 || month == 3 ||
	         						month == 7 || month == 8 || month == 9 ){
	         					name = " " + visit + " " + name; 
	         				 }
	         				
	         			 }else if(visit.equals("KV2-4")){
	         				 if(month == 4 || month == 5 || month == 6 ||
		         						month == 10 || month == 11 || month == 12 ){
		         					name = " " + visit + " " + name; 
		         		   }
	         				 }else{
	         				 customer.setVisit(visit); 
	         			 }
	         		 }
	         		 
	         		 customer.setName(deCode(name));
	         		 
	         		   try{
	     		    	 	 String avamine =  getNode(rida, "avamiskuup").getFirstChild().getNodeValue()   ;
		         			 if(avamine.trim().length() >0){
				         		 
		         			 }  	  
		         		  }catch(Exception e){}
	         		 
	         		  try{
	     		   	 Date sulgemine = getDate2( getNode(rida, "sulgemiskuup").getFirstChild().getNodeValue(),0);
		         	  Calendar cal = Calendar.getInstance();
		         	  cal.add(Calendar.MONTH, -1);
		         	  Calendar ss = Calendar.getInstance();
		         	  ss.add(Calendar.DATE, +1);
	     		   	       if(sulgemine.after(cal.getTime()) && sulgemine.before(ss.getTime())){
				         		 customer.setInfo("Suletakse :" 
	     		   	       + getNode(rida, "sulgemiskuup").getFirstChild().getNodeValue());
				         		 
				         		   customer.setContractOver(sulgemine.getTime()+"");
		         			 }  
	     		   	  
	     		   	       
		         		  }catch(Exception e){}
	         		 
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
				         		  customer.setPersonalData(customer.getPersonalData() + "|" + deCode("Tel:" + phone.replaceAll(";", " , ")));  
		         			 }else{
				         		  customer.setPersonalData(deCode(phone.replaceAll(";", " , "))); 
		         			 } 	  
		         		  }catch(Exception e){
		         		  }
	         		  
		         		 try{ String email =  getNode(rida,"mail").getFirstChild().getNodeValue(); 
		         			 if(customer.getPersonalData() != null){
				         		  customer.setPersonalData(customer.getPersonalData() + "| " + deCode(email.replaceAll(";", " , ")));  
		         			 }else{
				         		  customer.setPersonalData(deCode(email.replaceAll(";", " , "))); 
		         			 } 	  
		         			 customer.restPerEmail = email;
		         		  }catch(Exception e){ }
	         		  
		         		 boolean jah = false;
		         		  String klient = customer.getID();
		             	  String nimi = customer.restPerson;
		             	   //   java.sql.Date synna = rs1.getDate("synna");
		             	  String email = customer.restPerEmail;
		             	  String phone = customer.restPerPhone;
		             	  if(customer.restPerson == null || customer.restPerEmail == null 
		             			  || customer.restPerPhone == null ){
		             		  jah = true;
		             	  }
		             		  
		             	 try{
			     		   	 Date muudetud = getDate2( getNode(rida, "muutmiskuup").getFirstChild().getNodeValue(),0);
				         	  Calendar cal = Calendar.getInstance();
				         	  cal.add(Calendar.MONTH, -6);
				         	       if(muudetud.before(cal.getTime())){
						         	  jah = true;
						         	  customer.setAskContactData(true);
				         			 }  
			     		   	    }catch(Exception e){}
			         		 
			         	    
		             	  
		             	  
		             	  if (jah){
		             		  boolean all = false;
		             		  if(customer.restPerson == null && customer.restPerEmail == null 
		             			  && customer.restPerPhone == null){
		             			  all = true;
		             		  }
		             		  if(phone == null){
		             			phone = " ";  
		             		  }
		             		  
		             		 if(nimi == null){
		             			  nimi = " ";  
			             		  }
		             		 
		             		 if(email == null){
			             			email = " ";  
			             		  }
		             		 if(!all){
		             			 sbcontact.append(klient+";"+ nimi.replaceAll(";", ",") + ";" + 0 + "; ;" + phone.replaceAll(";", ",") 
		             			 + ";" + email.replaceAll(";", ",") +  "\n");
		    				 }
		                  }
		             	
		         		  
		         	     String salesman  = null; 
	                     
	                     try{ salesman  =  getNode(rida,"myygiesindaja_kd").getFirstChild().getNodeValue();
		         		      customer.addAgent(new Agent(deCode(salesman)));
		         		     
		         				Agent agg = ((Agent)this.getAgents2().get(salesman.trim()));
		         				
		         				if(agg != null && agg.getDubbler().size()>0){
		         					for (int e = 0 ; e < agg.getDubbler().size() ; e++){
		         						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(e)));
		         					}
		         				
		         			}
		         		      
		         		      customer.setMainAgent(deCode(salesman));    
		         		      custAgent.put(customer.getID(),deCode(salesman));
	                     }catch(Exception e){}
		         	   
		         		    
	                     
	                     if(kampaaniad.get(customer.getID()) != null){
	                    	 if(customer.getInfo() != null && customer.getInfo().length() > 0 ){
	                    		 customer.setInfo(customer.getInfo() + "| |" + deCode(kampaaniad.get(customer.getID()) + ""));
	                    	 }else{
	                    		 customer.setInfo(deCode(kampaaniad.get(customer.getID()) + ""));
	                    	 }
	                    	 
	                     }
	                     
	                     
	                     
	                     

	                     if(top3Cust.get(customer.getID()) != null && top3Asum.get(askd) != null){
	                    	 String add = "Kliendi TOP3||" + top3Cust.get(customer.getID()) + "|Asumi TOP3|" + top3Asum.get(askd); 
	                    	 if(customer.getInfo() != null && customer.getInfo().length() > 0 ){
	                    		 customer.setInfo(customer.getInfo() + "|" + add );
	                    	 }else{
	                    		 customer.setInfo(add);
	                    	 }
	                    	 
	                     }
	                     
	                     
	                     
	                     if(nonvisited.get(customer.getID()) != null){
	                    	 if(customer.getInfo() != null && customer.getInfo().length() > 0 ){
	                    		 customer.setInfo(customer.getInfo() + "| |60p pole käidud!!!");
	                    	 }else{
	                    		 customer.setInfo("60p pole käidud!!!");
	                    	 }
	                    	 
	                     }
	                     
	                     
	                     if(excelketid.get(customer.getID()) != null){
	                    	// customer.setCustType(((String)ketid.get(customer.getID())).replaceAll(",", "|").replaceAll(" ", ""));
	                   //  System.out.println("tyyp " + customer.getCustType());
	                    	 customer.setGroup(((String)excelketid.get(customer.getID())).replaceAll(",", "|").replaceAll(" ", ""));
	                     }
	                     customer.addFixSortCode(customer.getID());
	                     
	         		 try{  String kett =  getNode(rida,"mp_kett_kd").getFirstChild().getNodeValue();
	         		      customer.setCity(kt.get(kett) + "");
	         		      customer.setChain(kt.get(kett) + "");
	         		      customer.addFixSortCode(kett);
	         		      customer.setAnkeediTyyp(kett);
	         		      kettKlient.put(customer.getID(),kett);
	         		     kett = (String) this.ketidSidus.get(kett);
	         		      if(salesman != null && budgets.get(salesman+kett)!= null){
	         				  customer.setCity(customer.getCity() + "|" + deCode("Sinu keti Eelrv. : '" +  budgets.get(salesman+kett) ));  
		   	 			  }  
	         		      if(inventuurDone.get(customer.getID()) != null){
	         		    	 customer.setCity(customer.getCity() + "|Riiuli inventuur:" +  inventuurDone.get(customer.getID()));
	         		      }
	         		      
                         }catch(Exception e){}
                    
	         		 if(myykKlKokku != null && myykKlKokku.get(customer.getID()) != null) {
	         		  	 customer.setCity(customer.getCity() + "|Jooksva kuu müük:" +  nf.format(myykKlKokku.get(customer.getID())) + "EUR");
		         		    
				      }
	         		 
	           		      
                     /*  try{ String payer  =  getNode(rida,"PayerPartnerCode").getFirstChild().getNodeValue();
		         		      customer.setPayer(deCode(payer.trim()));	 
		         		     customer.setGroup(deCode(payer.trim()));	
		         		     if(kliendialed.get(payer) != null){
		         		    	 customer.setDiscount(Double.parseDouble((String)kliendialed.get(payer.trim())));
		         		     }
		         	   }catch(Exception e){ }
		         	   
		         	//  -<PartnerBalanceInfo> <OutstandingAmount>333.72</OutstandingAmount> <OverdueAmount>333.72</OverdueAmount>
		         	   
		         	   try{ String over  =  getNode(getNode(rida,"PartnerBalanceInfo"),"OverdueAmount").getFirstChild().getNodeValue();
			         		      customer.setDebt(Double.parseDouble(over));
			         	   }catch(Exception e){}
			         	   
			          try{ String used  =  getNode(getNode(rida,"PartnerBalanceInfo"),"OutstandingAmount").getFirstChild().getNodeValue();
	         		      customer.setUsedCredit(Double.parseDouble(used));
	         	         }catch(Exception e){}	   
			        */
	         	         
	         	         boolean goes = true;
			          
				   if(goes){
			
					  

					   
					 //  cc.addQuestiosToCustomer(customer,kyssad);
					   codes.add(customer.getID());
					   if(customer.getChain()!= null && customer.getChain().length() > 0 ){
						   codes.add(customer.getChain());   
					   }
					   
					   if(suhe.get(customer.getID()) != null){
						   String aas = suhe.get(customer.getID()) + " %";
						   if(customer.getCity() != null){
		 		         		  customer.setCity(customer.getCity() + "|" + ("Suhe eelm. : '" + aas ));  
		 		 			 }else{
		 		         		  customer.setCity(("Suhe eelm. : '" + aas )); 
		 		 			 }
					   }
		         		  
					   tm.put(name, customer);
	         		    tmid.put(customer.getID(), customer);
	                     this.customers.add(customer.getID());
	         	   }
	     
	         	  }catch(Exception e){
	         	  }

      	   }
               	 
	          }
	        

                 
                if(sbcontact.length() > 2){
                   		 Converter.saveFileInUTF8( "custcontacts.txt", sbcontact);
                   	}
                

	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	 	  if(true){
	 		  
	 		
	 	//getSalesData();
//	   	  System.out.println("hindKlKaan " + hindKlKaan.toString());
	 		  if(hindKlKaan != null){
	 			  Enumeration enumer = hindKlKaan.keys();
	 		   	   String ymonth = Converter.getYYYYMMString(-1);
	 			   String ymonthl = Converter.getYYYYMMString(-2);
	 		//	   System.out.println("keskmine " + tagKokku.toString() + " " + tagAg.toString());
	 		   	  while(enumer.hasMoreElements()){
	 		   		  String aa = enumer.nextElement() + "";
	 		   		  String per = aa.substring(0,aa.indexOf("-"));
	 		   		  String kl = aa.substring(aa.indexOf("-")+1);
	 		   		  if(tmid.get(kl) != null){
	 		   			Customer customer = (Customer) tmid.get(kl);
	 		   			String hind = nf.format(hindKlKaan.get(aa))+ " eur";

	 		 	   		 if(customer.getCity() != null){
	 		         		  customer.setCity(customer.getCity() + "|" + deCode("Kaan : '" + per + " " + hind  ));  
	 		 			 }else{
	 		         		  customer.setCity(deCode("Kaan : '" + per + "  " + hind )); 
	 		 			 }
	 		   		  }
	 		   		} 
	 		  }
	 		  
	 	
	   	  if(tagKl != null){
	   		 Enumeration	enumer = tagKl.keys();
		   	   
		   	  while(enumer.hasMoreElements()){
		   		  String aa = enumer.nextElement() + "";
		   		  String per = aa.substring(0,aa.indexOf("-"));
		   		  String kl = aa.substring(aa.indexOf("-")+1);
		   		  if(tmid.get(kl) != null){
		   			Customer customer = (Customer) tmid.get(kl);
		   			String hind = pf.format(tagKl.get(aa));

		 	   		 if(customer.getCity() != null){
		         		  customer.setCity(customer.getCity() + "|" + deCode("Tag :  '" + per + " " + hind  ));  
		 			 }else{
		         		  customer.setCity(deCode("Tag :  '" + per + "  " + hind )); 
		 			 }
		   		  }
		   		}  
	   	  }
	   	  
	 		
	   	 if(sortSuurKl != null){
	   	 	Enumeration	enumer = sortSuurKl.keys();
		   	  while(enumer.hasMoreElements()){
		   		  String aa = enumer.nextElement() + "";
		   		  String per = aa.substring(0,aa.indexOf("-"));
		   		  String kl = aa.substring(aa.indexOf("-")+1);
		   		  if(tmid.get(kl) != null){
		   			Customer customer = (Customer) tmid.get(kl);
		   			String suurus = ((Vector)sortSuurKl.get(aa)).size() + "";

		 	   		 if(customer.getCity() != null){
		         		  customer.setCity(customer.getCity() + "|" + deCode("Sort :  '" + per + " " + suurus + " toodet" ));  
		 			 }else{
		         		  customer.setCity(deCode("Sort :  '" + per + "  " + suurus + " toodet" )); 
		 			 }
		   		  }
		   	  } 
	   	 }
	   	  
	  
	   	  
	 	  }
	 	  
	 	  Customer ccd = new Customer("KONTOR");
	 	  ccd.setName(" aaa Kontoris olen");
	 	 Enumeration enume = getParent().getAgentsHash().elements();
         while (enume.hasMoreElements()) {
           Agent a = (Agent) enume.nextElement();
           ccd.addAgent(a);
         }
	 	  
	 	  tm.put(" aaa Kontoris Olen", ccd);
		    tmid.put(ccd.getID(), ccd);
		    
		    
		    Customer ccds = new Customer("RIIUL");
		 	  ccds.setName(" aaa Riiulitega tegelemine");
		 	  enume = getParent().getAgentsHash().elements();
	         while (enume.hasMoreElements()) {
	           Agent a = (Agent) enume.nextElement();
	           ccds.addAgent(a);
	         }
		 	  
		 	  tm.put(" aaa Riiulitega tegelemine", ccds);
			    tmid.put(ccds.getID(), ccds);
			    
			    
			    
			    Customer ccdss = new Customer("MUU");
			 	  ccdss.setName(" aaa MUU-ga tegelemine");
			 	  enume = getParent().getAgentsHash().elements();
		         while (enume.hasMoreElements()) {
		           Agent a = (Agent) enume.nextElement();
		           ccdss.addAgent(a);
		         }
			 	  
			 	  tm.put(" aaa MUUGA tegelemine", ccdss);
				    tmid.put(ccdss.getID(), ccds);
	 	 try{
	 	    Converter.prepareDocCatalogue(inP,outP,codes,true);
	 	 }catch(Exception e){
	 		 
	 	 }
	 

	 	if(rcompany != null){
	    	Converter.sendCustomerJson(tm, rcompany,false,user,pass,true);
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

   
   public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
	   
		String sql = "INSERT into supervisor values (" + "?, " // agent C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ " '' ," // receivedTime D,
				+ "?," // normalpr N,
				+ "?," // amoun N,
				+ "?," // grouptotal N,
				+ "?," // cattotal N,
				+ "?," // pertotcat I,
				+ "?," // percolcat I,
				+ "?," // perpalcat I,
				+ "?," // pershecat I,
				+ "?," // perrescat I,
				+ "?," // isaction L,
				+ "?," // ispartner L,
				+ "?," // isbrand L,
				+ "?," // isgroup L
				+ "?," // actionaeva N,
				+ "?," // actionbeva N,
				+ "?," // actiontota N,
				+ "?," // custeval N,
				+ "?," // custcomme C(200),
				+ "?," // comment C(200),
				+ "?," // custagent C(200),
				+ "?," // custagentname C(200),
				+ "?," // mustbe I,
				+ "?," // ideal I,
				+ "?," // resstr1 C(200),
				+ "?," // resstr2 C(200),
				+ "?," // resstr3 C(200),
				+ "?," // resint1 I,
				+ "?," // resint2 I,
				+ "?)"; // resint3 L

		try {
			System.out.println("jam");
			//Connection ccc = this.getNewConnection();
			//PreparedStatement ps = ccc.prepareStatement(sql);
			getProducts(null);


			for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
			
				System.out.println("Sees toimetan");
				String xml = "";
				String sb0 = "";
				String xml2 = "";
				String xml3 = "";
				String xmlInv = "";
				String xmlTJ = "";
				String xmlTJOut = "";
				String inventuur = "";
				String kontakt  = "";

				
			//	ps.setString(1, wr.getInfo().getAgentID());
				String prodID =  prodid.get(lw.getLine().getProdID()) + "";
				
				kontakt = "<contact>";
				
				//xml2 = xml2 +  "<tj_id>" + wr.getInfo().getAgentID() + "</tj_id>";
				xml = xml +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
				kontakt = kontakt +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
				xmlInv = xmlInv +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
				xml2 = xml2 + "<tj>\n";
				xml3 = xml3 + "<tj>\n";
				xml2 = xml2 +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
				xml3 = xml3 +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
                xmlTJ = xmlTJ + "<tj>\n";
                xmlTJ = xmlTJ +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
				
                xmlTJOut = xmlTJOut +  "<tj>\n";
                xmlTJOut = xmlTJOut +  "<myyja_id>" + wr.getInfo().getAgentID() + "</myyja_id>\n";
				
				//ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				//ps.setString(3, lw.getLine().getCustID());
			     xml = xml  +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n";
			     kontakt = kontakt  +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n"; 
			     kontakt = kontakt +  "<muutmiskuup>" + this.getKuup() + "</muutmiskuup>\n";
					
			    xml2 = xml2 +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n";
			    xml3 = xml3 +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n";
			    xmlTJ = xmlTJ +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n";
			    xmlTJOut = xmlTJOut +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n";
			    xmlInv = xmlInv +  "<mp_id>" + lw.getLine().getCustID() + "</mp_id>\n";
			    xmlInv = xmlInv +  "<muutmiskuup>" + this.getKuup() + "</muutmiskuup>\n";
				   
			    
			    sb0 = "Esindaja "+ wr.getInfo().getAgentID() + " " + Converter.deCode(wr.getInfo().getAgentName()) + "\n";
			    sb0 = sb0 + "Müügipunkt "+ lw.getLine().getCustID() + " " + Converter.deCode(lw.getLine().getCustName()) + "\n";
				
			    
				//ps.setString(4, Converter.deCode(lw.getLine().getCustName()));
				//ps.setString(5, lw.getLine().getProdID());
				xml = xml +  "<va_id>" + prodID + "</va_id>\n";
				xml2 = xml2 +  "<va_id>" + prodID + "</va_id>\n";
				xml3 = xml3 +  "<va_id>" + prodID + "</va_id>\n";
				xmlTJ = xmlTJ +  "<va_id>" + prodID + "</va_id>\n";
				xmlTJOut = xmlTJOut +  "<va_id>" + prodID + "</va_id>\n";
				
				xmlTJOut = xmlTJOut +   "<staatus>tjmaha</staatus>\n";
				xmlTJOut = xmlTJOut +  "<muutmiskuup>" + getKuup() + "</muutmiskuup>\n";
				xmlTJOut = xmlTJOut + "</tj>\n";
				
				xmlTJ = xmlTJ +   "<staatus>tjmuudatus</staatus>\n";
				
				
				xml3 = xml3 +  "<staatus>mjmaha</staatus>\n";
				xml2 = xml2 +  "<staatus>mjmuudatus</staatus>\n";
				
				xml3 = xml3 +  "<muutmiskuup>" + getKuup() + "</muutmiskuup>\n";
				xml3 = xml3 + "</tj>\n";
				
				sb0 = sb0 + "Väljaanne kood:"+ lw.getLine().getProdID() + " id:" + prodID+ " " + lw.getLine().getProdName() + "\n";
			      
				//ps.setString(6, Converter.deCode(lw.getLine().getProdName()));
				//ps.setDouble(7, lw.getLine().getNormalPrice());
				xml = xml +  "<kogus_saata>" + lw.getLine().getNormalPrice() + "</kogus_saata>\n";
				xml2 = xml2 +  "<kogus_jaotada>" + lw.getLine().getNormalPrice() + "</kogus_jaotada>\n";
			
				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
			
				if(cr1.equals("RAUTATJ") && lw.getLine().getNormalPrice() > 0){
					 xmlTJ = xmlTJ +  "<y_ks>" + (int)lw.getLine().getNormalPrice() + "</y_ks>\n";
				}
			
				sb0 = sb0 + "Kogus saata  "+ lw.getLine().getNormalPrice() + "\n";
				//ps.setDouble(8, lw.getLine().getAmount());
				
				String jao = "";
				String pae = "";
			    double type = 0;
				//ps.setDouble(9, lw.getLine().getGroupTotal());
				if(lw.getLine().getGroupTotal() < 1 && !lw.getLine().isPartner() && cr1.equals("RAUTA")){
					
					String juht = (String)tooteJuht.get(lw.getLine().getProdID());
					String email = null;
					if(juht != null){
						email = (String) tjuhid.get(juht);
					}
						
					System.out.println("Tjuht " + juht + " aadr "+ email);
					
					this.makeStl(sb0 ,xml,wr.getInfo().getAgentID(),email);
				}
				
				if(lw.getLine().getGroupTotal() > 0){
					
					jao = (int)lw.getLine().getGroupTotal() +""  ;

					xmlTJ = xmlTJ +  "<e_ks>" + (int)lw.getLine().getGroupTotal() + "</e_ks>\n";
				
				}
				
				//ps.setDouble(10, lw.getLine().getCategoryTotal());
				
				if(lw.getLine().getCategoryTotal() > 0){
					if(jao.length() == 0){
						jao = (int)lw.getLine().getCategoryTotal() +""  ;
					}else{
						jao = jao + "/" + (int)lw.getLine().getCategoryTotal()   ;
					}
				
					xmlTJ = xmlTJ +  "<t_ks>" + (int)lw.getLine().getCategoryTotal() + "</t_ks>\n";
                 }
				
				
				//ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
				if(lw.getLine().getPercentageFromTotalCategory() > 0){
					if(jao.length() == 0){
						jao = (int)lw.getLine().getPercentageFromTotalCategory() +""  ;
					}else{
						jao = jao + "/" + (int)lw.getLine().getPercentageFromTotalCategory()   ;
					}
				
					
					xmlTJ = xmlTJ +  "<k_ks>" + (int)lw.getLine().getPercentageFromTotalCategory() + "</k_ks>\n";
				}
				
				//ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
				if(lw.getLine().getPercentageFromColdCategory() > 0){
					if(jao.length() == 0){
						jao = (int)lw.getLine().getPercentageFromColdCategory() +""  ;
					}else{
						jao = jao + "/" + (int)lw.getLine().getPercentageFromColdCategory()   ;
					}
					xmlTJ = xmlTJ +  "<n_ks>" + (int)lw.getLine().getPercentageFromColdCategory() + "</n_ks>\n";
				}
				
				
				//ps.setInt(13, lw.getLine().getPercentageFromPallCategory());

				if(lw.getLine().getPercentageFromPallCategory() > 0){
					 xmlTJ = xmlTJ +  "<r_ks>" + (int)lw.getLine().getPercentageFromPallCategory() + "</r_ks>\n";
				}
				


				
				//ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
				if(lw.getLine().getPercentageFromShelfCategory() > 0){
					 xmlTJ = xmlTJ +  "<l_ks>" + (int)lw.getLine().getPercentageFromShelfCategory() + "</l_ks>\n";
				}
				
				//ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
				if(lw.getLine().getPercentageFromRestCategory() > 0){
					 xmlTJ = xmlTJ +  "<p_ks>" + (int)lw.getLine().getPercentageFromRestCategory() + "</p_ks>\n";
				}
				
				
				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				String sta = "";
				String end = "";
				StringTokenizer stt = new StringTokenizer (cr2,"|");
				while (stt.hasMoreTokens()){
					sta = stt.nextToken();
					if(stt.hasMoreTokens()){
						end = stt.nextToken();
					}
				}
				if(end.equals("")){
					System.out.println("END POLE OLEMAS " + cr2);
				}
				
				if(lw.getLine().getAction1Eval() > 0){
					pae = (int)lw.getLine().getAction1Eval() + "";
					}
				
				if(lw.getLine().getAction2Eval() > 0){
					if(pae.trim().length() == 0){
						pae = "" + (int)lw.getLine().getAction2Eval();
					}else{
						pae = pae + "/" + (int)lw.getLine().getAction2Eval() ;
					}
					}
				
				if(lw.getLine().getActionTotalEval() > 0){
					if(pae.trim().length() == 0){
						pae = "" + (int)lw.getLine().getActionTotalEval();
					}else{
						pae = pae + "/" + (int)lw.getLine().getActionTotalEval() ;
					}
					
					}

				if(lw.getLine().getCustEval() > 0){
					
					if(pae.trim().length() == 0){
						pae = "" + (int)lw.getLine().getCustEval();
					}else{
						pae = pae + "/" + (int)lw.getLine().getCustEval() ;
					}
					
					}
								
				
				if(cr1.equals("RAUTA") && !lw.getLine().isPartner() && lw.getLine().getGroupTotal() > 0){
					xml2 = xml2 +  "<sl_jaotus>" + jao + "</sl_jaotus>\n";
					xml2 = xml2 +  "<sl_pv_arv>" + pae + "</sl_pv_arv>\n";
					if(cr2.trim().length()>0){
						xml2 = xml2 +  "<muutmiskuup>" + this.getYYYYpMMpDD(new Date(Long.parseLong(sta))) + "</muutmiskuup>\n";
					}else{
						xml2 = xml2 +  "<muutmiskuup>" + getKuup() + "</muutmiskuup>\n";
						
					}
					xml2 = xml2 + "</tj>\n";
					
					this.makeMuutus(xml2, wr.getInfo().getAgentID(),"mj");
				}
				
				if(cr1.equals("RAUTATJ") && !lw.getLine().isPartner()){
					    
					   xmlTJ = xmlTJ +  "<muutmiskuup>" + this.getKuup() + "</muutmiskuup>\n";
					
					if(cr2.trim().length()>0 && lw.getLine().isAction()){
						try{
							
						xmlTJ = xmlTJ +  "<algkuup>" + this.getYYYYpMMpDD(new Date(Long.parseLong(sta))) + "</algkuup>\n";
						}catch(Exception e){
							System.out.println("ALGKUUPEV POLE LEITAV" + end  + " " + lw.getLine().getCustID() + " " + prodID );
							xmlTJ = xmlTJ +  "<algkuup>" + this.getKuup() + "</algkuup>\n";
							
						}
					
						try{
							xmlTJ = xmlTJ +  "<loppkuup>" + this.getYYYYpMMpDD(new Date(Long.parseLong(end))) + "</loppkuup>\n";
								
						}catch(Exception e){
							System.out.println("LOPPKUUPEV POLE LEITAV" + end  + " " + lw.getLine().getCustID() + " " + prodID );
							
							Calendar cl  = Calendar.getInstance();
							cl.add(Calendar.MONTH, 2);
							
							xmlTJ = xmlTJ +  "<loppkuup>" + this.getYYYYpMMpDD(cl.getTime()) + "</loppkuup>\n";
							
						}
					}
					xmlTJ = xmlTJ + "</tj>\n";
					this.makeMuutus(xmlTJ, wr.getInfo().getAgentID(),"tj");
				}
				
				
				//ps.setBoolean(16, lw.getLine().isAction());
				//ps.setBoolean(17, lw.getLine().isPartner());
				
				
				
				if(lw.getLine().isPartner()){
					if(cr1.equals("RAUTA")){
						this.makeAway(xml3, wr.getInfo().getAgentID(),"mj");
					}
                    if(cr1.equals("RAUTATJ")){
                    	this.makeAway(xmlTJOut, wr.getInfo().getAgentID(),"tj");
    				}
				}
				//ps.setBoolean(18, lw.getLine().isBrand());
				//ps.setBoolean(19, lw.getLine().isGroup());
            	//ps.setDouble(20, lw.getLine().getAction1Eval());
				//ps.setDouble(21, lw.getLine().getAction2Eval());
				//ps.setDouble(22, lw.getLine().getActionTotalEval());
				//ps.setDouble(23, lw.getLine().getCustEval());
				
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}

				 System.out.println("xmlInv0 " + cr1 + " '" + cc + "'"); 
				if(cr1.equals("INVCOUNT") && cc.trim().length()	> 0){
					 xmlInv = xmlInv +  "<yksus>" + cc + "</yksus>\n";
					 xmlInv = xmlInv +  "<inv_id>" + lw.getLine().getProdID() + "</inv_id>\n";
					 xmlInv = xmlInv +  "<inv_name>" + lw.getLine().getProdName() + "</inv_name>\n";
					 xmlInv = xmlInv +  "<inv_count>" + lw.getLine().getAmount() + "</inv_count>\n";
	                 System.out.println("xmlInv" + xmlInv);   
	                    	this.makeAway(xmlInv, wr.getInfo().getAgentID(),"invcount");
	                    	try {
	                    		
	                    		String aaa = "";
	                    		 try {
	                    			   File file = new File ( "inventorydone.txt");
	                    			   FileReader ff = new FileReader(file);
	                    				BufferedReader br = new BufferedReader(ff);
	                    				String line = null;
	                    				while( (line = (br.readLine())) != null){
	                    					aaa = aaa + " " + line;
	                    				}	 }catch(Exception g){
	                    			   g.printStackTrace();
	                    		   }
	                    		 String asi = lw.getLine().getCustID()+ ";" +this.getYYYYpMMpDDpTimeNiceString(new Date());
		                			if(!aaa.contains(asi)){
		                				PrintWriter os = new PrintWriter(new FileWriter(
			                					 "inventorydone.txt", true));
			                			os.write(asi);
			                			os.write(System.getProperty("line.separator"));
			                			os.close();	
		                			}
	                		}catch (IOException ex) {
	                			ex.printStackTrace();
	                		}

				}
			
				
				//ps.setString(24, Converter.deCode(cc));

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				//ps.setString(25, Converter.deCode(cd));

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				//ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				//ps.setString(27, Converter.deCode(cf));
				//ps.setInt(28, lw.getLine().getDealNr());
				//ps.setInt(29, lw.getLine().getIdealNr());

				
				//ps.setString(30, cr1);
            boolean kon = false;
				if(cr1.equals("CONTACTINFO")){
				
					kontakt = kontakt + "<email>" + lw.getLine().getComment() + "</email>\n" ;
					kontakt = kontakt + "<phone>" + lw.getLine().getCustComment() + "</phone>\n" ;
					kontakt = kontakt + "<pername>" + lw.getLine().getProdName() + "</pername>\n" ;
					try{
						long aaa = Long.parseLong(lw.getLine().getCustAgentName());
						if(aaa > 0){
							cf = Converter.getYYYY_MM_DD_String(new Date(aaa));
						}else{
							cf = "";
						}
					}catch(Exception e){
						cf = "";
					}
					
					kontakt = kontakt + "<bday>" + cf + "</bday>\n" ;
					kontakt = kontakt + "</contact>\n";
					kon = true;
				}
				if(kon){
					makeContact(kontakt.replaceAll("&", "&amp;"),wr.getInfo().getAgentID());
				}
				
				//ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}
				//ps.setString(32, cr3);
				//ps.setInt(33, lw.getLine().getResInt1());
				//ps.setInt(34, lw.getLine().getResInt2());
				//ps.setInt(35, lw.getLine().getResInt3());
			//	ps.executeUpdate();
			}

			//ps.close();
			//ccc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

   
   private String getKuup(){
	   return this.getYYYYpMMpDD(null);
   }
   
   private void makeStl(String body ,String xml, String agent ,String kellele){
	   
	         
	      String agemail = null;
	      Properties propse =  new Properties();
		    try{
		    	propse =  EksportFrame.loadProps("emailid.txt");
		  	  
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
           if(propse.get(agent) != null){
        	   agemail = Converter.parseFirstEmail(propse.get(agent)+"");
           }
           
	         sendOrderEmail(body, kellele, agemail);
	         
	         
	}
   
   
   
   private void sendOrderEmail(String bb , String to, String agemail){
		  String address = parent.getProps().getProperty("orderemail");
		  String subjTX = parent.getProps().getProperty("subjtx");
		 
		  
		  if(address != null || to != null ){
			  Email email = new Email("SalesOrder");
	         
	          email.setFrom(parent.getProps().getProperty("from"));
	          email.setSMTP(parent.getProps().getProperty("smtphost"));
	          email.setUser(parent.getProps().getProperty("smtpuser"));
	          email.setPass(parent.getProps().getProperty("smtppass"));
	          email.setPort(parent.getProps().getProperty("smtpport"));
	          email.setType("text/html");
	         
	          String format = "";
	          String shopCode = "";
	          String sub = "SalesOrder";
	          try{
	        	  if(bb.indexOf("RESOLVED") != -1){
	            	  format = bb.substring(bb.indexOf("RESOLVED") + 8 , bb.toLowerCase().indexOf("format") );
	              }
	              
	       
	              if(bb.indexOf("SHOPCODE") != -1){
	            	  format = bb.substring(bb.indexOf("SHOPCODE") + 8 , bb.toLowerCase().indexOf("by buyer") );
	              }
	           
	              if(subjTX != null){
	            	  sub = subjTX;
	              }
	            	  
	          }catch(Exception e ){
	        	  
	          }
	           String subj = sub + shopCode + format ;
	       //   System.out.println(subj);
	          email.setSubjectFilesTo(subj); //vahele invoice 
	          if(parent.getProps().getProperty("language").equals("Russian")){
	            email.setEncoding("KOI8-R");
	          }
	          
	          //email.setEncoding("iso-8859-1");
	          try {
	        	  
	        		String subject = email.getSubjectFilesTo();
	        		StringBuffer sb = new StringBuffer();
	        		sb.append("<HTML>\n");
	        		sb.append("<HEAD>\n");
	        		sb.append("<TITLE>\n");
	        		sb.append(subject + "\n");
	        		sb.append("</TITLE>\n");
	        		sb.append("</HEAD>\n");

	        		sb.append("<BODY>\n");
	        		//sb.append("<H1>File:  <A href=\"" + filename + "\">" + filename +"</A>" + "</H1><BR>");
	        		//<A href="../images/forest.gif">map of the enchanted forest.</A>
	        		//<A href=" + filename + ">" + filename +"</A>
	        
	        		    sb.append(Converter.deCode(bb).replaceAll("\n", "<BR>"));
	        		    sb.append("<BR>");

	        		sb.append("</BODY>\n");
	        		sb.append("</HTML>\n")    ;    	  
	        	  
	        	email.setBody( sb.toString());

	        	
	        	
	        	if(address != null){
	        		 StringTokenizer st =  new StringTokenizer(address,";");
	 	            while(st.hasMoreTokens()){
	 	            	String aadr = new InternetAddress(st.nextToken()).toString();
	 	              email.setFilesTo(aadr);
	 	              //System.out.println("Setting address " + aadr);
	 	              email.send();
	 	              }
	        	}
	        	
	        	if(agemail != null){
	        		 StringTokenizer st =  new StringTokenizer(agemail,";");
	 	            while(st.hasMoreTokens()){
	 	            	String aadr = new InternetAddress(st.nextToken()).toString();
	 	              email.setFilesTo(aadr);
	 	              //System.out.println("Setting address " + aadr);
	 	              email.send();
	 	              }
	        	}
	        	
	        	
	        	
	 	            if(to != null){
	 	            	StringTokenizer st =  new StringTokenizer(to,";");
	 		            while(st.hasMoreTokens()){
	 		            	String aadr = new InternetAddress(st.nextToken()).toString();
	 		            	 email.setFilesTo(aadr);
	 		 	            email.send();	
	 		            }
	 		            	
	 	            }	
	        	
	           
	           
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  
		  }
	  }

   
   
   private void makeContact(String xml, String agent){
        String fileName = escape(parent.getProps().getProperty("conv.dir2") + "Contact_" + agent) 
	          + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".xml";
	     
	      String pais =  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><contacts>";
	      String lopp =  "</contacts>";
	      try{
	          FileOutputStream fos = new FileOutputStream(fileName);
		        fos.write((pais + xml + lopp).getBytes());
		        fos.close();
	         }catch (Exception r){
	    	  
	        }
	}
   
   
   
   private void makeMuutus(String xml, String agent, String type){
        String fileName = escape(parent.getProps().getProperty("conv.dir") + type + "_muutus_" + agent) 
	          + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".xml";
	     
	      String pais =  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><tjd>";
	      String lopp =  "</tjd>";
	      try{
	          FileOutputStream fos = new FileOutputStream(fileName);
		        fos.write((pais + xml + lopp).getBytes());
		        fos.close();
	         }catch (Exception r){
	    	  
	        }
	}
   
   private void makeAway(String xml, String agent, String type){
	   

	      String fileName = escape(parent.getProps().getProperty("conv.dir") + type + "_maha_" + agent) 
	          + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".xml";
	      String pais =  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><tjd>";
	      String lopp =  "</tjd>";
	      
	      
	      if(type.equals("invcount")){
	    	  fileName = escape(parent.getProps().getProperty("conv2.dir") + type + "_maha_" + agent) 
	          + ( Calendar.getInstance().getTimeInMillis()+"").substring(5,13)   +  ".xml";
	    	  pais =  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><invcou>";
		      lopp =  "</invcou>";
	      }
	      
	     

	      try{
	          FileOutputStream fos = new FileOutputStream(fileName);
		        fos.write((pais + xml + lopp).getBytes());
		        fos.close();
	         }catch (Exception r){
	    	  
	        }
	         
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
	 		cal.set(YEAR, MONTH -1, DAY, 1, 2, 3);
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
   
   Hashtable tooteJuht = null;

   Hashtable ilmumine = null;
   
   private String turn(String asi){
	   return asi.substring(8) + "-" + asi.substring(5,7) + "-" + asi.substring(0,4) ;
   }
   
   public TreeMap getProducts(Agent agent){
	
	   
TreeMap realiseer = new TreeMap();
	      
	   
	   try {
		   
			  File f = new File (getDir() + "vnr_info.xml");
		  		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  	    DocumentBuilder db;
		  		db = dbf.newDocumentBuilder();      	
		      	FileInputStream in = getCleanInputStream(f);
		      	String handler = null;
		        Document doc = db.parse(in,handler);
		          in.close();
		          Node root =  getNode(doc, "vnr_infod");     
		          
		          for (int i = 0; i < root.getChildNodes().getLength(); i++){
		        	  Node rida = root.getChildNodes().item(i);
		    	    	if(rida.getNodeName().equals("vnr_info")){
		         		  String   id = getNode(rida,"va_id").getFirstChild().getNodeValue();
		                  String sisu = turn(getNode(rida,"ilmumiskuup").getFirstChild().getNodeValue())
		                  + " -> " + turn(getNode(rida,"realiseerimiskuup").getFirstChild().getNodeValue());
		                  if(realiseer.get(id) != null){
		                	  sisu = realiseer.get(id) + "|" + sisu;
		                  }
		                  realiseer.put(id, sisu);
		         		  /*<vnr_info>
<vnr_id>443476</vnr_id> <!--va_number_id-->
<va_id>24</> <!--valjaanne_id-->
<vnr>190</vnr> <!--va_number-->
<ilmumiskuup>2015-10-22</ilmumiskuup>
<realiseerimiskuup>2015-10-23</realiseerimiskuup>
*/
		    			}
					}	 
		          }catch(Exception g){
				   g.printStackTrace();
			      }
		
	   
	   
	   ilmumine = getIlmumine();
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
     
    Calendar kuu = Calendar.getInstance();
    kuu.add(Calendar.MONTH, -1);
      try {
     	 Vector koodid = new Vector();
     	tooteJuht = new Hashtable();
        
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
          		 
          		 String nrid = null; 
          		 try {
          		 nrid = getNode(rida,"va_nr_id").getFirstChild().getNodeValue();
         		  // <va_nr_id>588981</va_nr_id>
           	}catch (Exception e){}
          		 
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
       	    		   prod.setPartii("TJ : " + juht.trim());
       	    		   tooteJuht.put(prod.getID(),juht.trim());
       	    		 }
       	    	   }catch (Exception e){}
          	       
       	 

         	     try{
      	    		 String ilmu  = getNode(rida, "va_ilmumissag_kd").getFirstChild().getNodeValue();
      	    		 if(ilmu.trim().length() > 1 && ilmumine.get(ilmu) != null){
      	    			 if(prod.getPartii().trim().length() > 0){
      	    				  prod.setPartii(prod.getPartii() + "|Ilmu. : "  + ilmumine.get(ilmu));
      	      	    	   }else{
      	    				  prod.setPartii("Ilmu. : "  + ilmumine.get(ilmu));
      	      	    	 }
      	    		 }
      	    	   }catch (Exception e){}
         	       
         	     
         	     
         	    try{
     	    		 if(realiseer.get(id) != null ){
     	    			 if(prod.getPartii().trim().length() > 0){
     	    				  prod.setPartii(prod.getPartii() + "|BB : "  + realiseer.get(id));
     	      	    	   }else{
     	    				  prod.setPartii("BB : "  + realiseer.get(id));
     	      	    	 }
     	    		 }
     	    	   }catch (Exception e){}
        	       
        	     
       	    	
       	    	try{
       	    		 String km = getNode(rida, "va_km_grupp_kd").getFirstChild().getNodeValue();
       	    		   prod.setVat(kms.get(km) + "");
          	       }catch (Exception e){}

       	    	
       	    	try{
       	    		 String ls = getNode(rida, "va_nr_ladu").getFirstChild().getNodeValue();
       	    		 Double LS = Double.parseDouble(ls);
       	    		 if(LS != 0) {
       	    		   prod.setStock(LS);
       	    		 }
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
       	    		 Date loodud  = this.getDate2(getNode(rida, "loomiskuup").getFirstChild().getNodeValue(),0
       	    				 );
       	    		//System.out.println("ihaa saime "  + prod.getID() + " " + loodud.toGMTString());
       	    		 if(kuu.getTime().before(loodud)){
       	    			 prod.setStatusNew("1");
       	    			 
       	    			// System.out.println("ihaa saime "  + prod.getID());
       	    		 }
       	    		   
          	       }catch (Exception e){
          	    	  // e.printStackTrace();
          	       }
          	       

            	     try{
         	    		 String ean = getNode(rida, "ean").getFirstChild().getNodeValue();
         	    		 if(nrid != null) {
         	    			 if(ean.length() > 4)
         	    		 prod.setPhotoid(ean+ "/id2/" + nrid+ "-ldpi.jpg"); //9772228156036/id2/588981-ldpi.jpg
         	    		 }else {
         	    			 prod.setPhotoid(ean+ "/latest-ldpi.jpg"); //977029074726944/latest-ldpi.jpg
         	    		 }
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

  private Vector custProds = new Vector();
   public Vector getCustomerProductsList(String agentCode){
	   return custProds;
   }
   
     
   public  Vector upside(Vector inVecu){
	   Vector inVec = (Vector)inVecu.clone();
        Vector sorted = new Vector(inVec.size());
        while(inVec.size() > 0){
        	sorted.addElement(inVec.elementAt(inVec.size()-1));
        	inVec.removeElementAt(inVec.size()-1);
        }
        return sorted;
      }
   
   
   
   
   public Vector getCustomerProductsListVec(){
	   this.getProducts(null);
	   System.out.println("KliendiTooted alustan");
	   Vector custProds = new Vector();
	    TreeMap sales = new TreeMap();
		
	   String[] list = new File(getDir()).list();
       for(int u = 0 ; u < list.length ; u++){
		  String fname = list[u];
	
	if(fname.startsWith("vnr_myyk_kuu_va_liik")  && !fname.contains("_rp")){
	
	try{
	
	  File f = new File (getDir() + fname);
	// System.out.println(getDir() + fname);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
	db = dbf.newDocumentBuilder();      	
	FileInputStream in = getCleanInputStream(f);
	String handler = null;
  Document doc = db.parse(in,handler);
  Node root = getNode(doc, "vnr_myygid");     
  HashSet done = new HashSet();


  for (int i = 0; i < root.getChildNodes().getLength(); i++){
	
  	  Node rida = root.getChildNodes().item(i);
	
  	 try{     
  		if(rida.getNodeName().equals("vnr_myyk")){
  			String klientid = null;
  			 try{
   	    		  klientid = getNode(rida, "mp_id").getFirstChild().getNodeValue();
   	    		   
      	       }catch (Exception e){
      	    	   
      	    	   e.printStackTrace();
      	       }
   	    	 
      	       String toode = null;
      	     try{
   	    		 toode = this.prodsx.get(getNode(rida, "va_id").getFirstChild().getNodeValue())+ "";
   	    	   }catch (Exception e){}
      	       
      	   String vaid = null;
    	     try{
 	    		 vaid = getNode(rida, "vnr_id").getFirstChild().getNodeValue();
 	    	   }catch (Exception e){}
    	       
   	    	double kogus = 0;
      	     try{
   	    		 kogus = Double.parseDouble(getNode(rida, "kogus").getFirstChild().getNodeValue());
   	    	   }catch (Exception e){}
   	    	  Date dd = null; 
   	    	 String date = null;
   	    	try{
   	     	date = getNode(rida,"kp").getFirstChild().getNodeValue();
     		    }catch (Exception e){}
   	    	
   	    	//grupime kokku kogused kui vnr_id on sama
   	    	CustProd cp = new CustProd(klientid, toode, kogus,0 ,0,
             		date,0,0,0," ");
   	    	String dc = klientid + "" + toode + "" +vaid;
   	         if(sales.get(dc) !=null){
   	        	 cp = (CustProd)sales.get(dc);
   	        	 cp.lastAmount = cp.lastAmount + kogus;
   	        	 cp.lastAmountDate = date;
   	           }
   	        	sales.put(dc, cp);	 
   	         
   	     //  if(toode.equals("7110") )
   	     
   	    // System.out.println(klientid + " " + this.prodid.get(toode) + " " + date + " " + kogus + " "+vaid + " " + 
   	    // cp.getLastAmount() + " " + fname);
			  
   	      
   		}
    	
  	 }catch(Exception r){
  	 }
   }
  
 // System.out.println("Sales " + sales.size());
    		
	}catch(Exception t){
		t.printStackTrace();
	}
 	}	
   	}
       
       
       // nyyd on meil tree map iga ID-ga 
       
       //on vaja sorteerida klient toode kuyupäev id lõikes et võtta 5 tk ainult
       
       
       Iterator iterax = sales.keySet().iterator();
       TreeMap resu = new TreeMap();
  		
  		while (iterax.hasNext()){
  			String key = iterax.next()+ "";
   			CustProd cp = (CustProd)sales.get(key);
   			resu.put(cp.getCustID() + "" + cp.getProdID() + "" +cp.getLastAmountDate() + ""+ cp.getLastAmount(), cp);	
  		}
       
  		Vector vec = new Vector();
  		vec.addAll(sales.values());
  		vec = upside(vec);
  		
       
       
       
  	 System.out.println("sales suurus " + sales.size());
     	Iterator itera = vec.iterator();//resu.keySet().iterator();
   		String oldprod = null;
   		String oldcust = null;
   		String olddate = null;
   		String ams = "";
   		double pluss = 0;
   		double minus = 0;
   		int i = 0;
   		int u = 0;
   		while (itera.hasNext()){
   			CustProd cp = (CustProd)itera.next();
   			
   			if(oldcust == null){
   				oldcust = cp.getCustID();
   			}
   			
   			if(oldprod == null){
   				oldprod = cp.getProdID();
   			}
   			if(olddate == null){
   				olddate = cp.getLastAmountDate().substring(5);
   			}
   			
   			String tt = cp.getProdID();
   			String kk = cp.getCustID();
   			int kogus = (int)cp.getLastAmount();
   			String dd = cp.getLastAmountDate().substring(5);
   			
   			
   				if(!tt.equals(oldprod) ){
   					
   					
   					if(ams.length() > 28){
   						ams = ams.substring(ams.length() - 28);
   						ams = ams.substring(ams.indexOf("/")+1);
   					}
   					
   					if(ams.length() == 0){
   						ams = " ";
   					}
   					
   					 CustProd cc = new CustProd(oldcust, oldprod, 0,0 ,0,
   		             		ams,0,0,0," ");
   					  custProds.add(cc);  
   					 //if( oldprod.equals("7110"))  
   					 // System.out.println(kk + " " +oldprod + " " + ams);
   					 ams = kogus +"";
   					
   				}else{
   					ams = ams+"/"+kogus;
   				}
   				
   			oldprod = tt;
   			olddate = dd;
   			oldcust = kk;
   		}
	   
	  try {
		  System.out.println("CP valmis " + custProds.size());
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
	  
   	if(tagKokku!=null){
   	
   	  Enumeration enumer = tagKokku.keys();
   	   String ymonth = Converter.getYYYYMMString(-1);
	   String ymonthl = Converter.getYYYYMMString(-2);
	   String tag = "";
  
	   /*
	   'while(enumer.hasMoreElements()){
   		  String aa = enumer.nextElement() + "";
   		 
   		  
   			String hind = pf.format(tagKokku.get(aa));
   		 if(tag.equals("")){
         		  tag = "Lehepunkt keskmine tagastus|     " + aa + " " + hind; 
 			 }else{
         		  tag = tag + "|     " + aa + " " + hind + "|"; 
 			 }
   		  }
   	*/
   		
  /* 	enumer = tagAg.keys();
   	  
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
 		}*/
	
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
}
