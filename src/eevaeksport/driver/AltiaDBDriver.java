package eevaeksport.driver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class AltiaDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public AltiaDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    prepareDateInfo();
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
  private Hashtable dates = new Hashtable();
  
  private void  prepareDateInfo(){
	
	  String[] list = new File(getDir()).list();

	  for(int i = 0 ; i < list.length ; i++){
		  String name = list[i];
		//  System.out.println(list[i]);
		  
		  if(name.indexOf("PARTNER")!= -1){
			  String date = name.substring(name.indexOf("PARTNER")+9,name.indexOf("PARTNER")+17);
			  String ddd = (String) dates.get("PARTNER");
			 // System.out.println(date + " " + ddd + " " + (date));
			  
			
			  if(ddd == null){
				  dates.put("PARTNER", date); 
			  }else{
				//  System.out.println(date + " " + ddd + " " + (date) + " " + ddd.compareTo(date));
				  if(ddd.compareTo(date) <= -1){
					  dates.put("PARTNER", date); 
				  }
			  }
		  }
		  
		  if(name.indexOf("NELDATAMS")!= -1){
			  String date = name.substring(name.indexOf("NELDATAMS")+9,name.indexOf("NELDATAMS")+17);
			//  System.out.println(date);
			  
			  String ddd = (String) dates.get("NELDATAMS");
			  if(ddd == null){
				  dates.put("NELDATAMS", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("NELDATAMS", date); 
				  }
			  }
		  }
		 
		  if(name.indexOf("SORTMENTMS")!= -1){
			  String date = name.substring(name.indexOf("SORTMENTMS")+10,name.indexOf("SORTMENTMS")+18);
			//  System.out.println(date);
			  
			  String ddd = (String) dates.get("SORTMENTMS");
			  if(ddd == null){
				  dates.put("SORTMENTMS", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("SORTMENTMS", date); 
				  }
			  }
		  }
		  
		  if(name.indexOf("PRICAT")!= -1){
			  String date = name.substring(name.indexOf("PRICAT")+8,name.indexOf("PRICAT")+16);
			//  System.out.println(date);
			  
			  String ddd = (String) dates.get("PRICAT");
			  if(ddd == null){
				  dates.put("PRICAT", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("PRICAT", date); 
				  }
			  }
		  }
		  
		  
		  if(name.indexOf("PRODAT")!= -1){
			  String date = name.substring(name.indexOf("PRODAT")+8,name.indexOf("PRODAT")+16);
			  //System.out.println(date);
			  
			  String ddd = (String) dates.get("PRODAT");
			  if(ddd == null){
				  dates.put("PRODAT", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("PRODAT", date); 
				  }
			  }
		  }
		  
		  if(name.indexOf("OPENINV")!= -1){
			  String date = name.substring(name.indexOf("ICESMS")+6,name.indexOf("ICESMS")+14);
			  //System.out.println(date);
			  
			  String ddd = (String) dates.get("OPENINV");
			  if(ddd == null){
				  dates.put("OPENINV", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("OPENINV", date); 
				  }
			  }
		  }
		  if(name.indexOf("INVRPT")!= -1){
			  String date = name.substring(name.indexOf("INVRPT")+8,name.indexOf("INVRPT")+16);
			 // System.out.println(date);
			  
			  String ddd = (String) dates.get("INVRPT");
			  if(ddd == null){
				  dates.put("INVRPT", date); 
			  }else{
				  if(ddd.compareTo(date) <= -1){
					  dates.put("INVRPT", date); 
				  }
			  }
		  }		  
	  }
	//  System.out.println(dates.toString());
  }
  
  public String getSaveDebtInfo() {
	  makePictureCat();
	  System.exit(0);
   	return null;
   }
  
  
  
  private void makeNormpict(String pictssou,String pictspda){
	  File res2 = new File (pictssou);
      String[] ff2 = res2.list();
  	 for(int i = 0; ff2.length > i ; i++){
  		 
  		File sou = new File ( pictssou + ff2[i]); 
  		//System.out.println();
  		if(ff2[i].indexOf("_") != -1 ){
  			File out = new File ( pictspda + ff2[i].substring(0,ff2[i].indexOf("_")) + ".jpg");
  	  		try {
  				copyFile(sou,out);
  			} catch (Exception e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}	
  		}
  		
  	 }
  }
  
  
  private void makePictureCat(){
	    
  	
  	String picts = parent.getProps().getProperty("pict.dir");
	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	    String pictssou  = parent.getProps().getProperty("pictpda.source");
	 makeNormpict(pictssou,picts);

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
  	    		  System.out.println("Piltide leidmine "  + a);	
  	    	}
  	     
                String toode = prod.getID().trim();
                String ean = prod.getEAN().trim();
                toode = toode.replace('/', '_');
                File f = new File(picts + ean +".jpg");
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
  	  System.out.println("Kataloogi kustutamine "  + a);	    
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
//       	 L�ppes kustutamine 
       	 
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


  
   public String getPilotDayData(Agent agent){
   	return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
	private HashMap hinnad = new HashMap();

	private HashMap kliendialed = new HashMap();
	
	
	
	
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	String today = getYYYYpMMpDDpTimeNiceString(null);
   	
	
 	try {

	   	   String[] list = new File(getDir()).list();
			  //System.out.println(list.toString());
	      	hinnad = new HashMap();
		   	kliendialed = new HashMap();
			  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
			  for(int u = 0 ; u < list.length ; u++){
				
				  String fname = list[u];
				
			if(fname.indexOf("PRICAT") != -1 && fname.indexOf((String)dates.get("PRICAT")) != -1){
				//  System.out.println("File" + getDir() + fname);
			
			  File f = new File (getDir() + fname);
	 	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	 	     DocumentBuilder db;
	 		db = dbf.newDocumentBuilder();      	
	     	FileInputStream in = getCleanInputStream(f);
	     	String handler = null;
	         Document doc = db.parse(in,handler);
	         Node root  = getNode(doc, "n0:PDA_Prices");     
	      //   System.out.println(root);
	     //	System.out.println("ridu  " + root.getChildNodes().getLength() + root.toString());
	         for (int i = 0; i < root.getChildNodes().getLength(); i++){

	         	  Node rida = root.getChildNodes().item(i); 
	       //  	 System.out.println("ridu  " + rida.getNodeName());
	         	  if(rida.getNodeName().equals("PDA_Price")){
	         		 
	         			 
		         				MatrixLine mline = new MatrixLine();

		         			    double price = 0;
		         				String custID = null;
		         				String clGroup = null;
		         				String prodID = null;
		         				String discGroup = null;
		         				String prodGroup = null;

		         				double discount = 0;
		         				String start = null;
		         				String end = null;
		         				 Node item = getNode(rida,"Material");
		         				
		         				try{
		         					prodID  = item.getFirstChild().getNodeValue();	         					
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         			
		         				
		         				try{
		         					end  = getNode(rida,"PriceEndDate").getFirstChild().getNodeValue();	         					
		         				}catch(Exception e){
		         					
		         					e.printStackTrace();
		         				}
		         				try{
		         					start  = getNode(rida,"PriceStartDate").getFirstChild().getNodeValue();	         					
		         				}catch(Exception e){
		         					
		         					e.printStackTrace();
		         				}
		         				try{
		         					 price  = Double.parseDouble(getNode(rida,"Price").getFirstChild().getNodeValue());		         					
		         				}catch(Exception e){
		         					
		         					if(prodID != null && prodID.startsWith("102101")) {
		         						System.out.println("id " +prodID + " "+ end + " " + start + " " );
		         						e.printStackTrace();
		         					}
		         					
		         				}
		         				
		         				try{
		         					 discount  = Double.parseDouble(getNode(rida,"Discount").getFirstChild().getNodeValue());		         					
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         				boolean goes = true ;
		         				
		         				Boolean pr1 = false;
		         				Boolean pr2 = false;
		         				try{
		         					clGroup  = getNode(rida,"Payer").getFirstChild().getNodeValue();		         					
		         				} catch(Exception e){
		         				  	pr1 = true;
			         			}
		         				
		         				
		         				try{
		         					clGroup  = getNode(rida,"HierarchyCustomer").getFirstChild().getNodeValue();		         					
		         				} catch (Exception e){
		         						pr2 = true;
			         		    }
		         				
		         				if(pr1 && pr2) {
		         					if(prodID != null && price > 0) {
	         							hinnad.put(prodID.trim(), price + "");
			         				}
		         					goes = false;
		         				}
		         				
		         				try{
		         					prodGroup  = getNode(rida,"ProductGroupCode").getFirstChild().getNodeValue();
		         				   if(prodGroup.equals("01001004B")){
		         					  prodGroup = "0100104B";
		         		    	   }
		         		    	   
		         		    	   
		         		    	   if(prodGroup.startsWith("01003002")){
		         		    		  prodGroup = prodGroup.replaceAll("01003002", "01003001");
		         		    		  
		         		   		   }
		         		   		 if(prodGroup.startsWith("01003003")){
		         		   			prodGroup = prodGroup.replaceAll("01003003", "01003001");
		         		   			
		         				   }
		         		   		
		         		   		
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         				
		         				  if(prodID == null && prodGroup == null){
		         					  kliendialed.put(clGroup.trim(), discount + "");
		         					  goes = false;
		         				  }
		         				
		         				if (custID != null && custID.trim().length() > 0) {
		         					mline.setCustID(custID.trim());
		         				}
		         				
		         				if (mline.getCustID() == null && clGroup != null
		         						&& clGroup.trim().length() > 0) {
		         					 if(payerstree.get(clGroup.trim()) != null) {
		         						clGroup = (String) payerstree.get(clGroup.trim());
		       	                  }
		         					mline.setCliGroup(clGroup.trim());
		         				}

		         				if (prodID != null && prodID.trim().length() > 0) {
		         					mline.setProdID(prodID.trim());
		         				}
		         				
		         				if (mline.getProdID() == null && discGroup != null && discGroup.trim().length() > 0) {
		         					mline.setDiscGroup(discGroup);
		         				}
		         				
		         				
		         				if(start != null){
		         					mline.setStartDate(start);
		         				}
		         				
                                if(end != null && end.indexOf("9999") == -1){
                                	mline.setEndDate(end);
                                }
                                
                                if(end.indexOf("9999") != -1){
                                	mline.setStartDate(today);
                                	mline.setProdLevel("pr");
                                }
                                
		         				if (price > 0) {
		         					mline.setPrice(price);
		         				}
		         				
		         				
		         				if(discount >0){
		         					mline.setDiscount(discount);
		         					mline.setPrice(0);
		         				}
		         				
		         				if(discount == 0 && price == 0) {
		         					goes = false;
		         				}
		         				if (mline.getProdID() == null && prodGroup != null
		         						&& prodGroup.trim().length() > 0) {
		         					mline.setProdGroup(prodGroup);
		         				
		         					if(prodGroup.equals("01001004")){
			         		   			MatrixLine mm =  new MatrixLine();
			         		   			
			         		   			mm.setDiscount(mline.getDiscount());
			         		   			mm.setProdGroup("0100104B");
			         		   		 if(end.indexOf("9999") != -1){
		                                	mm.setStartDate(today);
		                                	mm.setProdLevel("pr");
		                                }
			         		   		 
				         				
				         				if (custID != null && custID.trim().length() > 0) {
				         					mm.setCustID(custID.trim());
				         				}
				         				
				         				if (mm.getCustID() == null && clGroup != null
				         						&& clGroup.trim().length() > 0) {
				         					mm.setCliGroup(clGroup.trim());
				         				}

				         				if (prodID != null && prodID.trim().length() > 0) {
				         					mm.setProdID(prodID.trim());
				         				}
				         				
				         				if (mm.getProdID() == null && discGroup != null && discGroup.trim().length() > 0) {
				         					mm.setDiscGroup(discGroup);
				         				}
				         				
			         		   		 
			         		   		 
			         		   		lines.add(mm);	
			         		   		}
		         				}
		         				

		                    if(goes)     				
		         				lines.add(mline);				         			
		         				  
	         		  }
	                   }}  
	         	  }
	         }catch(Exception t){
	        	 t.printStackTrace();
	         }

 //System.out.println(this.hinnad);
	return lines;

   }

   public Vector getFixedAssortmentData() {
       return fixedLines ;
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
       return null;
       }


   public Vector getDebt(String agentCode){
	   Vector vec = new Vector();
	   try {
	
		   String[] list = new File(getDir()).list();
	 	        for(int u = 0 ; u < list.length ; u++){
	 			
	 			
	 			  String fname = list[u];
	 		
	 		if(fname.indexOf("OPENINV") != -1 && fname.indexOf((String)dates.get("OPENINV")) != -1){
	 		
	 		  File f = new File (getDir() + fname);
	  	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	  	     DocumentBuilder db;
	  		db = dbf.newDocumentBuilder();      	
	      	FileInputStream in = getCleanInputStream(f);
	      	String handler = null;
	          Document doc = db.parse(in,handler);
	          Node root = getNode(doc, "n0:MT_PDA_OpenInvoices");     
	          
         //  System.out.println(root);
	     
              for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	         
	         	  try{
	         		
	         		  Debt debt = new Debt();
	         		  String payer = getNode(rida,"Payer").getFirstChild().getNodeValue();
	         		  if(payer.startsWith("000")){
	         			  payer = payer.substring(3);
	         		  }
	         		  
	         		 if(payerstree.get(payer) != null) {
	                	  payer = (String) payerstree.get(payer);
	                  }
                      debt.setCustID(payer); 
                      debt.setPayerID(payer);
                      String number = getNode(rida,"BillingDocumentNo").getFirstChild().getNodeValue(); 
                      debt.setDoknr(number);
                      String amo = getNode(rida,"UnpaidAmount").getFirstChild().getNodeValue();
                      if(amo.endsWith("-")){
                    	  amo = "-" +  amo.substring(0,amo.length()-1);
                      }
                      debt.setSum(Double.parseDouble(amo));
                      debt.setDuedate(getDate(getNode(rida,"DueDate").getFirstChild().getNodeValue()));                  
                      debt.setDate(getDate(getNode(rida,"BillingDate").getFirstChild().getNodeValue()));
                      debt.setDue(days(debt.getDuedate())+"");
                      boolean goes = true;
                      if(debt.getCustID().equals("1000185") ||debt.getCustID().equals("1000209") ||
                    		  debt.getCustID().equals("1000003") || debt.getCustID().equals("1016117")
                    		  || debt.getCustID().equals("1000055")  || debt.getCustID().equals("1015211")
                    		  || debt.getCustID().equals("1000330")  || debt.getCustID().equals("1000229") ){
                    	  goes = false ;
                      }
                      if(goes){
                    	  vec.addElement(debt);
                      }
                     
		 		  }catch(Exception g ){
		 			//g.printStackTrace();  
		 		  }
	         	  }

	 		}
	 		  }
	 		   }catch(Exception r){
	 			   r.printStackTrace();
	 		   } 	
	 		   System.out.println("volg seis " + vec.size());
	 		
      return vec;
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
     TreeMap   tm = new TreeMap();
     this.setTreeGroup(true);
     try{
    	
     
     String[] list = new File(getDir()).list();
	  //System.out.println(list.toString());
	  
	  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
	  for(int u = 0 ; u < list.length ; u++){
		
		  String fname = list[u];
	
	if(fname.indexOf("PRODAT") != -1 && fname.indexOf((String)dates.get("PRODAT")) != -1){
	
	  File f = new File (getDir() + fname);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
	db = dbf.newDocumentBuilder();      	
	FileInputStream inx = getCleanInputStream(f);
	String handler = null;
   Document doc = db.parse(inx,handler);
   inx.close();
   
   Node rr = getNode(doc, "E-Document");     
   Node rr1 = getNode(rr, "Document"); 
   Node root = getNode(rr1, "DocumentItem");
         
 if(root.getChildNodes().getLength() > 0 ){
	     for (int i = 0; i < root.getChildNodes().getLength(); i++){
      	  Node rida = root.getChildNodes().item(i);
      	  
      	if(rida.getNodeName().equals("GroupEntry")){
      	
     	     String id = getNode(rida,"GroupCode").getFirstChild().getNodeValue();
     		   
     	    String name = ""; 
   	      try{
   	    	 name   = getNodeAttr(rida,"GroupDescription","ET").getFirstChild().getNodeValue();
    	   }catch(Exception e ){     	    	
   	    	 name   = getNodeAttr(rida,"GroupDescription","EN").getFirstChild().getNodeValue(); 
   	      }    
    	   if(id.equals("01001004B")){
    		   id = "0100104B";
    	   }
    	   
    	   
    	   if(id.startsWith("01003002")){
    		   id = id.replaceAll("01003002", "01003001");
    		  
   		   }
   		 if(id.startsWith("01003003")){
   			id = id.replaceAll("01003003", "01003001");
   			
		   }
   		 
   		 if(id.startsWith("04001004")){
    			//id = id.replaceAll("04001004", "04001XXX");
    			
 		   }
   		 
   		 if(id.equals("04001004")){
   			 name = "x POS";
   		 }
   		 
    	     if(id.length() == 8){

     		   		 if(id.startsWith("01003001")){
     		   			 name = "Veinid";
     		   		 }
     			   tm.put(id, deCode(name));    
     		   }  else{
     			  teine.put(id.substring(id.length()-2), deCode(name));    
     		   }
      	}}}}}
	  
     }catch(Exception r){
    	 r.printStackTrace();
     }
     	 // System.out.println("Grupid " + teine.toString());
         return tm;
    }
   private TreeMap teine = new TreeMap();
 
   public TreeMap getProdGroupsSecond() throws SQLException {
	   return teine;
        }


   public static String deCode(String res ){	  
 	return Converter.enCodeEst(res);
 }
   
   private TreeMap payerstree = new TreeMap();
   
   private void loadpayers() {
	   ArrayList<String> arr = new ArrayList<String>();
	   try {
		Converter.readFile(new File("payers.txt"), "UTF-8", arr);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   for (String line : arr) {
		   StringTokenizer st = new StringTokenizer(line,";");
		   while(st.hasMoreElements()) {
			   String klient = st.nextToken();
			   String payer = st.nextToken();
			   payerstree.put(klient, payer);
			   
		   }
	   }
	}
   
   public TreeMap getAgents(){
	 
		
		  loadpayers();
		  
     TreeMap tm = new TreeMap(); 
     getPriceMatrixData();
 	try {
 		
 		 String[] list = new File(getDir()).list();
 		  //System.out.println(list.toString());
 		  
 		  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
 		  for(int u = 0 ; u < list.length ; u++){
 			
 			  String fname = list[u];
 		
 		if(fname.indexOf("PARTNER") != -1 && fname.indexOf((String)dates.get("PARTNER")) != -1){
 		
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
         	// System.out.println("ridu2  " + rida.getChildNodes().toString() + rida.getNodeValue());
         	  try{
         		
         		 String salesman  = getNode(rida,"UserID").getFirstChild().getNodeValue(); 
         		 String salesmanName  =  salesman.substring(salesman.indexOf(":")+1);
         		salesman =  salesman.substring(0,salesman.indexOf(":"));
         		
         		if(salesman != null){
         			
             		 Agent sm = new Agent (salesman);
             		 sm.setName(salesmanName);
                     
                      if(getParent().getProps().getProperty("groups."+ salesman) != null){
                    	  StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("groups."+ salesman),",");
                    	  while (st.hasMoreTokens()){
                    	 	  sm.addProdGroupList(st.nextToken());   
                    	 	//  System.out.println("gruplist " +salesmanName  + sm.getProdGroupList().toString());
                    	  }
                      }
                      agents1.put(sm.getID(), sm);
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
 		}
 		  }
 	}catch(Exception e){
// 		e.printStackTrace();
 	}
     System.out.println("klientide  nimistu " + payers.size());
     System.out.println("Agendilist tehtud" ); 
  //   setBulkSales(tm);
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
       
       private TreeMap agents1 = new TreeMap();

   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();
	     System.out.println("Kliendid alustan");
	     getLisaKliendid(tm);
	     this.customers = new ArrayList();
	     
	     try{
	    	 if(rcompany != null) {
	     	   Converter.getManagersReplacements(agents1, rcompany, user, pass , false); 
	    	 }
	     }catch(Exception e){
	     	e.printStackTrace();
	     }
	   
	   try {
		   String[] list = new File(getDir()).list();
	 		  //System.out.println(list.toString());
	 		  
	 		  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
	 		  for(int u = 0 ; u < list.length ; u++){
	 			
	 			  String fname = list[u];
	 		
	 		if(fname.indexOf("PARTNER") != -1 && fname.indexOf((String)dates.get("PARTNER")) != -1){
	 		
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
         
              for (int i = 0; i < root.getChildNodes().getLength(); i++){
	          	  Node rida = root.getChildNodes().item(i);
	         
	         	  try{
	         		  String id =  getNode(rida,"PartnerCode").getFirstChild().getNodeValue();
	         		  Customer customer = new Customer(id);
	         		  String name =  getNode(rida,"Name").getFirstChild().getNodeValue() + " " + id;
	         		 
	         		 customer.setName(deCode(name));
	         		 
	         		
	         		  try{
	         			 String aadr =  getNode(getNode(rida,"PartnerAddress"),"Address1").getFirstChild().getNodeValue();
	         			 customer.restStreet = aadr;
	         			customer.setCountry("Estonia");
	         			 try{
		         			 String post =   getNode(getNode(rida,"PartnerAddress"),"PostalCode").getFirstChild().getNodeValue(); 
		         			String linn =   getNode(getNode(rida,"PartnerAddress"),"City").getFirstChild().getNodeValue();  
			         		 aadr = aadr + " " +(deCode(post + "," +linn ));
			         		customer.restCity = linn;
		         			customer.restZipCode = post;
		         		  }catch(Exception ea){
		         		  }
	         			 
		         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr), "Estonia"));
		         		 
	         		  }catch(Exception e){
	         			  
	         		  }
	         		
	         	     try{
     		    	 	 String contacname =  getNode(rida, "ContactFirstName").getFirstChild().getNodeValue()   ;
	         			 if(contacname.trim().length() >0){
			         		  customer.setPersonalData(deCode(contacname)); 
			         		  customer.restPerson = contacname;
	         			 }  	  
	         		  }catch(Exception e){}
	         		  
	         		   try{
		         			 String contacname =  getNode(rida, "ContactLastName").getFirstChild().getNodeValue()   ;
		         			 if(contacname.trim().length() >0){
		         				 if(customer.getPersonalData() != null){
					         		  customer.setPersonalData(customer.getPersonalData() + " " + deCode(contacname)); 
					         		 customer.restPerson = customer.getPersonalData() + " " + (contacname);
			         			 }else{
					         		  customer.setPersonalData(deCode(contacname)); 
					         		 customer.restPerson = contacname;
			         			 } 	  
		         			 }  	  
		         		  }catch(Exception e){ }
		         		  
	         		     try{
		         			 String phone =  getNode(rida,"PhoneNumber").getFirstChild().getNodeValue(); 
		         			 if(customer.getPersonalData() != null){
				         		  customer.setPersonalData(customer.getPersonalData() + " " + deCode(phone));  
		         			 }else{
				         		  customer.setPersonalData(deCode(phone)); 
		         			 } 	  
		         			 customer.restPerPhone = phone;
		         		  }catch(Exception e){
		         		  }
	         		  

		         		
		         		    
	         		 try{  String paym =  getNode(getNode(rida,"ContractInfo"),"PaymentTerm").getFirstChild().getNodeValue();
	         		  customer.setPaymTerm(deCode(paym));
                         }catch(Exception e){ }
                        
                         
                     try{ String crlim =  getNode(getNode(rida,"ContractInfo"),"CreditLimit").getFirstChild().getNodeValue();
                       double dd = Double.parseDouble(crlim);
                       if(dd > 1){  
                    	   customer.setCredit(crlim);
                    	   
                       } 
                    	         }catch(Exception e){  } 
                     
                                 try{ String visit =  getNode(rida,"VisitDay").getFirstChild().getNodeValue();
           	         		 visit = visit.replaceAll(",", "");
                                 customer.setVisit(visit);
                               //  System.out.println("visit " + visit);
                                    }catch(Exception e){ } 
                                 
                     try{ String salesman  =  getNode(rida,"UserID").getFirstChild().getNodeValue();
	         		      salesman = salesman.substring(0,salesman.indexOf(":"));
	         		      customer.addAgent(new Agent(deCode(salesman)));
	         		      
	         		     Agent aa1 = (Agent)agents1.get(salesman.trim());
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
	         		      
	         		      
	         		     if(!parent.getEmail(salesman).equals("") &&  !custtsUsed.contains(id)){     		   
	     	         		   custtsUsed.add(id);
	           		      } }catch(Exception e){ }
	         	   
	           		      
	                  try{ String payer  =  getNode(rida,"PayerPartnerCode").getFirstChild().getNodeValue();
	                  if(kliendialed.get(payer) != null){
	         		    	 customer.setDiscount(Double.parseDouble((String)kliendialed.get(payer.trim())));
	         		     }
	                  if(payerstree.get(payer) != null) {
	                	  payer = (String) payerstree.get(payer);
	                  }
	                  if(payerstree.get(id) != null) {
	                	  payer = (String) payerstree.get(id);
	                  }
		         		      customer.setPayer(deCode(payer.trim()));	 
		         		     customer.setGroup(deCode(payer.trim()));	
		         		    
		         	   }catch(Exception e){ }
	                  
	                  try{
                       String grupp  =  getNode(rida,"PricatPartyCode").getFirstChild().getNodeValue();
	                  if(customer.getGroup() != null && customer.getGroup().trim().length() > 0 ) {
	                	  if(!customer.getGroup().contains(grupp)) {
	                		   customer.setGroup(customer.getGroup()+ "|" + deCode(grupp.trim()));
	       	              }
	                  } else {
		         		     customer.setGroup(deCode(grupp.trim()));
	                  }
	         		     Vector vv = new Vector();
	         		     vv.add(deCode(grupp.trim()));
	         		    customer.setFixSortCode(vv);
	         	            }catch(Exception e){ }
		         	   
		         	//  -<PartnerBalanceInfo> <OutstandingAmount>333.72</OutstandingAmount> <OverdueAmount>333.72</OverdueAmount>
		         	   double overd = 0;
		         	   try{ String over  =  getNode(getNode(rida,"PartnerBalanceInfo"),"OverdueAmount").getFirstChild().getNodeValue();
		         	  overd = Double.parseDouble(over);	      
		         	   customer.setDebt(overd);
			         	   }catch(Exception e){}
			         	   
			          try{ String used  =  getNode(getNode(rida,"PartnerBalanceInfo"),"OutstandingAmount").getFirstChild().getNodeValue();
	         		      customer.setUsedCredit(Double.parseDouble(used));
	         	         }catch(Exception e){}	   
			        
	         	         
	         	         boolean goes = true;
			          try{ String blocked  =  getNode(rida,"Blocked").getFirstChild().getNodeValue();
				         		
				         		 if(blocked.equals("1")){
				         			 if(overd>-0.1){
				         				customer.setSalesBlock(true); 
				         			 }
				         		       
				         	      goes = false;
				         		    //  System.out.println("Blkoitud " + customer.getName());
				         		  }
				         	   }catch(Exception e){}
			          
			          if(name.startsWith("ZDEL")) {
			        	  goes = false;
			          }
				         	   
	         	   if(goes){
	         		  tm.put(name, customer);
	                     this.customers.add(customer.getID());
	                   
	         	   }
	         		      
	         	  }catch(Exception e){
	         		 // e.printStackTrace();
	         	  }
	         	    
 	         }    
	         }    
		         }
	 	}catch(Exception e){
	 		e.printStackTrace();
	 	}
	   
	    
	 	
	 	
	 
	    System.out.println("Kliendid tehtud");
	    
     return tm;
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
	
	
	
	
	
	 private TreeMap getLinks (){
     	TreeMap res = new TreeMap();
     	
     	
     	File f = new File(this.getExcelDir() + "links.xlsx");
     	 System.out.println(f.exists() +  " " + f.getAbsolutePath());
 		try {
 			FileInputStream ff = new FileInputStream(f);
 			XSSFWorkbook workbook = new XSSFWorkbook((ff));
 			XSSFSheet sheet = workbook.getSheetAt(0);
 			XSSFRow row = null;
 		
 			row = sheet.getRow(0);
 			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

 			// System.out.println("rownum " + row.getLastCellNum() + " " + sheet.getSheetName() + " " + sheet.getLastRowNum());
 			 
 			 DataFormatter formatter = new DataFormatter();
				

 			 
 			for (int a = 0; a <  sheet.getLastRowNum(); a++) {
 				row = sheet.getRow(a);
 				String link = "";
 				String toode = "";
 				try {
 					// System.out.println(row.getCell((short)a).getStringCellValue());
 					link =  formatter.formatCellValue(row.getCell((short) 0));
 				} catch (Exception e) {
 					e.printStackTrace();
 					try {
 						
     					// System.out.println(row.getCell((short)a).getStringCellValue());
     					link =  ((int)row.getCell((short) 0).getNumericCellValue()) + "";
     				} catch (Exception r) {
     					// System.out.println(row.getCell((short)a).getNumericCellValue());
     				}// System.out.println(row.getCell((short)a).getNumericCellValue());
 				}
 				
 				try {
 					toode =  formatter.formatCellValue(row.getCell((short) 1));
 				} catch (Exception e) {
 					try {
 						
     					toode =  ((int)row.getCell((short) 1).getNumericCellValue()) + "";
     				} catch (Exception r) {
     				}
 				}
 				
 				if(link.length()> 0 && 
 						link.indexOf("https://anora.ee/") != -1){
 					link = link.substring("https://anora.ee/".length());
 					res.put(toode, link);
 		     		
 	 				// System.out.println(toode +  " " + link);
 				}
 				System.out.println(toode +  " " + link);
 				
 	 			
 			}
 		}catch (Exception d){
 			d.printStackTrace();
 		}
     	
     	return res ;
     }

	
	 private void getLisaKliendid (TreeMap tm ){
	     	
	     	
	     	File f = new File(this.getExcelDir() + "lisakliendid.xlsx");
	 		try {
	 			FileInputStream ff = new FileInputStream(f);
	 			XSSFWorkbook workbook = new XSSFWorkbook((ff));
	 			XSSFSheet sheet = workbook.getSheetAt(0);
	 			XSSFRow row = null;
	 		

		     	System.out.println(" suurus: " + sheet.getLastRowNum() );
	 			
	 			row = sheet.getRow(0);
	 			// M��RAME �RA MIS V�LJAS ASUVAD Sortimendi t�hised.

	 			// System.out.println("rownum " + row.getLastCellNum());
	 			for (int a = 0; a <  sheet.getLastRowNum()+1; a++) {
	 				row = sheet.getRow(a);
	 				String kood = "";
	 				String kett = "";
	 				String nimi = "";
	 				String agent = "";
	 				String aadress = "";
	 				String phone = "";
	 				try {
	 					kood =  row.getCell((short) 0).getStringCellValue();
	 				} catch (Exception e) {
	 					try {
	 						kood =  ((int)row.getCell((short) 0).getNumericCellValue()) + "";
	     				} catch (Exception r) {
	     				}// System.out.println(row.getCell((short)a).getNumericCellValue());
	 				}
	 				
	 				try {
	 					kett =  row.getCell((short) 1).getStringCellValue();
	 				} catch (Exception e) {
	 					try {
	 						kett =  ((int)row.getCell((short) 1).getNumericCellValue()) + "";
	     				} catch (Exception r) {
	     				}
	 				}
	 				
	 				try {
	 					nimi =  row.getCell((short) 2).getStringCellValue();
	 				} catch (Exception e) {
	 					try {
	 						nimi =  ((int)row.getCell((short) 2).getNumericCellValue()) + "";
	     				} catch (Exception r) {
	     				}
	 				}
	 				try {
	 					agent =  row.getCell((short) 3).getStringCellValue();
	 				} catch (Exception e) {
	 					try {
	 						agent =  ((int)row.getCell((short) 3).getNumericCellValue()) + "";
	     				} catch (Exception r) {
	     				}
	 				}
	 				try {
	 					aadress =  row.getCell((short) 5).getStringCellValue();
	 				} catch (Exception e) {
	 					try {
	 						aadress =  ((int)row.getCell((short) 5).getNumericCellValue()) + "";
	     				} catch (Exception r) {
	     				}
	 				}
	 				try {
	 					phone =  row.getCell((short) 6).getStringCellValue();
	 				} catch (Exception e) {
	 					try {
	 						phone =  ((int)row.getCell((short) 6).getNumericCellValue()) + "";
	     				} catch (Exception r) {
	     				}
	 				}
	 				
	 				if(kood != null && kood.trim().length() > 0){
	 				
	 				Customer cc = new Customer(kood);
	 				cc.setName("xyz " + kett + " " + nimi);
	 				cc.addAgent(new Agent(agent));
	 				cc.setPersonalData(phone);
	 				cc.setAddress(Converter.adressLink(Converter.enCodeEst(aadress), "Estonia"));
	 				cc.setChain(kett);
	 				
	 	 			tm.put(kett + " " +nimi, cc);
	 	 			
	 				}
	 			}
	 		}catch (Exception d){
	 			d.printStackTrace();
	 		}
	     	System.out.println("Lisakliendid " + tm.size() );
	     	
	     }
    
   public TreeMap getProducts(Agent agent){

    TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     System.out.println("Tooted alustan");
     
     TreeMap links= getLinks();
     System.out.println("links" + links.size());
      try {
     	 Vector koodid = new Vector();
         
  	   String[] list = new File(getDir()).list();
		  //System.out.println(list.toString());
		  
		  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
		  for(int u = 0 ; u < list.length ; u++){
			
			  String fname = list[u];
		
		if(fname.indexOf("PRODAT") != -1 && fname.indexOf((String)dates.get("PRODAT")) != -1){
		
		  File f = new File (getDir() + fname);
	     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db;
		db = dbf.newDocumentBuilder();      	
    	FileInputStream inx = getCleanInputStream(f);
    	String handler = null;
        Document doc = db.parse(inx,handler);
        inx.close();
        
        Node rr = getNode(doc, "E-Document");     
        Node rr1 = getNode(rr, "Document"); 
        Node root = getNode(rr1, "DocumentItem");
              
      if(root.getChildNodes().getLength() > 0 ){
    	     for (int i = 0; i < root.getChildNodes().getLength(); i++){
           	  Node rida = root.getChildNodes().item(i);
           	  
           	if(rida.getNodeName().equals("ItemEntry")){
           	   boolean pos = false;
          		 boolean goes = true;
          	     String id = getNode(rida,"SellerItemCode").getFirstChild().getNodeValue();
          		   Product prod = new Product(deCode(id));
          		  
          		 try{
       	    		 String grupp = getNode(rida, "ItemGroupCode").getFirstChild().getNodeValue();
       	    		 //this.prGroups.put(grupp, grupp);
       	    		 if(grupp.equals("01001004B")){
       	    			grupp = "0100104B";
       	    		    prod.setGroup(grupp);
       	    		    
       	    	   }else{
       	    		   if(grupp.startsWith("01003002")){
       	    			   grupp = grupp.replaceAll("01003002", "01003001");
       	    		   }
       	    		if(grupp.startsWith("02001002")){
    	    			 //  grupp = grupp.replaceAll("02001002", "02001001");
    	    		   }
       	    		
       	    		if(grupp.startsWith("01003003")){
 	    			   grupp = grupp.replaceAll("01003003", "01003001");
 	    		   }
       	    		   
       	    	 if(grupp.startsWith("04001004")){
         			//id = id.replaceAll("04001004", "04001XXX");
         			pos = true;
      		   }
       	    	 
       	    	// System.out.println("Grupp " + grupp);
       	    	    prod.setGroup(grupp.substring(0,8));
       	    	    if(grupp.length()> 8){
       	    	     prod.setGroupSecond(grupp.substring(grupp.length()-2));   
       	       	    }
      	    	   }
       	    	    
       	    	 }catch (Exception e){
       	    		 
       	   	 }
          		      		   
          		 String eans = "";
       	      try{
          		 String ean   = getNode(rida,"GTIN").getFirstChild().getNodeValue();
          	     prod.setEAN(ean);
          	     if(ean.length()>4){
          	    	 eans = ean.substring(ean.length()-4);
          	     }
      	      }catch(Exception e ){}
      	      
	         try{  
   	    		 String ean   = getNode(getNode(rida,"ItemUnitRecord"),"ItemUnitGTIN").getFirstChild().getNodeValue();
        	     prod.setEAN(ean);
        	     if(ean.length()>4){
        	    	 eans = ean.substring(ean.length()-4);
        	     }
    	      }catch(Exception e ){}
    	      
       	   
        	      try{
        	    	  String name   = getNodeAttr(rida,"ItemDescription","ID").getFirstChild().getNodeValue();
        	    	  String add = "";
        	    	  if(pos){
        	    		  add = "x ";
        	    	  }
        	          prod.setName(add + deCode(firstToUpperCase(name)) + " " + prod.getID() + " " + eans); 
    
         	 
        	      }catch(Exception e ){ 
        	    	  
        	    	  try{ 
        	    	  String name   = getNodeAttr(rida,"ItemDescription","ET").getFirstChild().getNodeValue();
        	          prod.setName(deCode(firstToUpperCase(name)) + " " + prod.getID() + " " + eans); 
 }catch(Exception tt){
	 try{
		  
		  
		  
		  String name   = getNodeAttr(rida,"ItemDescription","EN").getFirstChild().getNodeValue();
         prod.setName( deCode(firstToUpperCase(name)) + " " +  prod.getID() + " " + eans);   
	  }catch(Exception uu){
		  
	  }	  
        	    	  }
        	    	  
        	    	  
        	      }              
        	 
   	    	 try{
   	      	    prod.setPrice(Double.parseDouble(getNode(rida,"SalesPrice").getFirstChild().getNodeValue())); 
   	    	 }catch (Exception e){
   	    		prod.setPrice(1);
   	    	 }
   	   if(hinnad.get(prod.getID()) != null){
   		   double hind = Double.parseDouble((String) hinnad.get(prod.getID()) );
   		   if(prod.getUnit().equals("C6")){
   			   hind = hind / 4;
   		   }
   		prod.setPrice(hind);
   	   }else{
   		  // goes = false;
   	   }
   	    	 
   	    	 try{  prod.setInCases(Double.parseDouble(getNode(getNode(rida,"ItemUnitRecord"),"UnitNumerator").getFirstChild().getNodeValue())); 
 	    	 }catch (Exception e){}
 	    

   	    	 
 	    	 try{  prod.setOnPallet(Double.parseDouble(getNode(getNode(rida,"ItemUnitRecord"),"PiecesPerPallet").getFirstChild().getNodeValue())); 
 	    	 }catch (Exception e){}
 	    
   	    	 try{ prod.setUnit(getNode(rida, "BaseUnit").getFirstChild().getNodeValue());	 
   	    	 }catch (Exception e){}
    	    	
   	       	   prod.setVat("24");
    	      //    prod.setStock(Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
        	 	 if(links.get(prod.getID()) != null){
        	 		 prod.weblink = links.get(prod.getID()) + "";
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
   		 }
   	  }  
   	 

   	 
     
   	 
   	 
   	 
   }}}
    } catch (Exception e){
      e.printStackTrace();
    }

    System.out.println("Tooted tehtud" );
     return tm;
   }



   private TreeMap getPrices(){
	   TreeMap result = new TreeMap();
			   
			 	try {

				   	   String[] list = new File(getDir()).list();
						  //System.out.println(list.toString());
						  
						  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
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
		
			 	System.out.println(" hinnad " + result.size());
	   return result ;
   }

   
   private TreeMap getStocks(Vector prods){
	   TreeMap result = new TreeMap();
			   
			 	try {
			 		
			   	   String[] list = new File(getDir()).list();
					  //System.out.println(list.toString());
					  
					  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
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
		
			 	System.out.println("laoseis " + result.size());
	   return result ;
   }
   
   
   public String getAssortmentStr(Agent agent){
    return null;
  }


   public Vector getCustomerProductsList(String agentCode){
	   
	   System.out.println("KliendiTooted alustan");
	   Vector custProds = new Vector();
	   
	   String[] list = new File(getDir()).list();
		  boolean first = true;
		  for(int u = 0 ; u < list.length ; u++){
			try{
				  String fname = list[u];
				 
					if(fname.indexOf("SORTMENTMS") != -1 && fname.indexOf((String)dates.get("SORTMENTMS")) != -1 && first){
						  File f = new File (getDir() + fname);
						  first = false;
						     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						     DocumentBuilder db;
							db = dbf.newDocumentBuilder();      	
					   	FileInputStream in = getCleanInputStream(f);
					   	String handler = null;
					       Document doc = db.parse(in,handler);
					    
					       Node rr = getNode(doc, "n0:MT_PDA_SalesAssortments");
					  // 	System.out.println("ridu nr  " + rr.getChildNodes().getLength());
					       for (int i = 0; i < rr.getChildNodes().getLength(); i++){

					         	  Node rida = rr.getChildNodes().item(i); 
					       
					       	if(rida.getNodeName().equals("SalesAssortment")){
					         	String custID = getNode(rida,"Customer").getFirstChild().getNodeValue().trim();
					         	String prodID = getNode(rida,"Material").getFirstChild().getNodeValue().trim();
								double sold = Double.parseDouble(getNode(rida,"LastInvoiceQty").getFirstChild().getNodeValue().trim());							
								String last = getNode(rida,"LastSoldDate").getFirstChild().getNodeValue().trim().substring(4);
								last = last.substring(2) + "."+ last.substring(0,2);
							//	System.out.println("last " + last);
								String lastdoc = getNode(rida,"LastInvoiceNo").getFirstChild().getNodeValue().trim();
								
								CustProd cp = new CustProd(custID, prodID,
										 sold, 0,0, last, 0, 0, 0, " ");
						
								cp.setAbiKood(lastdoc);
								custProds.addElement(cp);
								
					         	}
					         	}
					      break; 
					       
					}	
			}catch(Exception e){
				e.printStackTrace();
			}
		  }
	  
	   return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Altia";
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
	  String[] list = new File(getDir()).list();
	  boolean first = true;
	  for(int u = 0 ; u < list.length ; u++){
		try{
			  String fname = list[u];
			 
				if(fname.indexOf("NELDATAMS") != -1 && fname.indexOf((String)dates.get("NELDATAMS")) != -1 && first){
					  File f = new File (getDir() + fname);
					  first = false;
					     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					     DocumentBuilder db;
						db = dbf.newDocumentBuilder();      	
				   	FileInputStream in = getCleanInputStream(f);
				   	String handler = null;
				       Document doc = db.parse(in,handler);
				       Node rr = getNode(doc, "n0:MT_PDA_SalesPersons");
				       
				       for (int i = 0; i < rr.getChildNodes().getLength(); i++){

				         	  Node rida = rr.getChildNodes().item(i); 
				         //	 System.out.println("ridu  " + rida.getNodeName());
				         	try{
				         	  String agent  = getNode(rida,"Code").getFirstChild().getNodeValue().trim();
				         	 String salesCur = getNode(rida,"SalesInLocalCurrency").getFirstChild().getNodeValue().trim();
				         	 String salesVol = getNode(rida,"SalesInVolume").getFirstChild().getNodeValue().trim();
				         	 Agent ag = (Agent)agents.get(agent);
				         	 ag.setMonthSales((int)Double.parseDouble(salesCur));
				         	 ag.setMonthSalesVol((int)Double.parseDouble(salesVol));
				       }catch(Exception d){
							//d.printStackTrace();
						}
				         	// System.out.println("ridu  " + ag.toString());
				         	 
				       }
				      break; 
				       
				}	
		}catch(Exception e){
			e.printStackTrace();
		}
	
		}		
		     
  }
}
