package eevaeksport.driver;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
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

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
  
//import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.util.Converter;
import imageUtil.ImageLoader;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class AltiaLATManDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public AltiaLATManDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    prepareDateInfo();
    readHaldusXLS();
  }
  private Hashtable dates = new Hashtable();
  
  
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

 
  
  private HashSet<String> halduskliendid = null;
  private HashMap haldusklienditooted = null;
  
  private void readHaldusXLS(){
		File file = new File(getExcelDir() + "haldamistabel.xls");
		
		try {
			halduskliendid = new HashSet<String>();
			haldusklienditooted = new HashMap();
			
			FileInputStream ff = new FileInputStream(file);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheetAt(0); //otsime esimeselt lehelt
			HSSFRow row = null;
		     int pikkus = sheet.getLastRowNum();
		
			// Uurime millised ankeedid on aktiivsed ja koodid meelde .
			

			 System.out.println("haldus sees " +  pikkus + " " + sheet.getSheetName());
			for (int a = 4; a < pikkus; a++) {
				row = sheet.getRow(a);
					String klient = null;
					
					try {
					   klient = row.getCell((short)1).getStringCellValue();
					} catch (Exception e) {
						try {
							 klient = row.getCell((short)1).getNumericCellValue() +"";
							} catch (Exception d) {
								
							}
					}
					
                String ean = null;
					
					try {
						ean = row.getCell((short)2).getStringCellValue();
					} catch (Exception e) {
						try {
							ean = row.getCell((short)2).getNumericCellValue() +"";
							 if(ean.indexOf("E") != -1){
						        	ean = ean.substring(0,ean.indexOf("E") );
						         int len  = ean.length();
						          ean = ean.substring(0,1) + ean.substring(2,len);
						          if(ean.length() == 12){
						        	  ean = ean+"0";
						          }
						        }else if(ean.indexOf(".") != -1) {
						        	ean = ean.substring(0,ean.indexOf("."));
						        }
							} catch (Exception d) {
								
							}
					}
					
					
	                String kogus = null;
						
						try {
							kogus = row.getCell((short)3).getStringCellValue();
						} catch (Exception e) {
							try {
								kogus = row.getCell((short)3).getNumericCellValue() +"";
								} catch (Exception d) {
									
								}
						}
						
						
		                String uuskood = null;
							
							try {
								uuskood = row.getCell((short)4).getStringCellValue();
							} catch (Exception e) {
								try {
									uuskood = row.getCell((short)4).getNumericCellValue() +"";
									} catch (Exception d) {
										
									}
							}
							
							
			                String lisakood = null;
								
								try {
									lisakood = row.getCell((short)5).getStringCellValue();
								} catch (Exception e) {
									try {
										lisakood = row.getCell((short)5).getNumericCellValue() +"";
										} catch (Exception d) {
											
										}
								}
							
								
				                String lisakogus = null;
									
									try {
										lisakogus = row.getCell((short)6).getStringCellValue();
									} catch (Exception e) {
										try {
											lisakogus = row.getCell((short)6).getNumericCellValue() +"";
											} catch (Exception d) {
												
											}
									}
					
					System.out.println("klient " + klient  + " " +  ean + " " + kogus + " " + uuskood + " " + lisakood + " " + lisakogus );
					
					if(klient != null){
						int ind = klient.indexOf(";");
						String kl1 = klient;
						String kl2 = null;
						if(ind != -1 ){
							kl1 = klient.substring(0,ind);
							kl2 = klient.substring(ind+1);
						}
						
						if(!halduskliendid.contains(kl1)){
							halduskliendid.add(kl1);
						}
						
						if(kl2 != null && !halduskliendid.contains(kl2)){
							halduskliendid.add(kl2);
						}
						
						Product prod = new Product(ean);
						prod.setStock(Double.parseDouble(kogus));
						prod.setGroup(lisakood);
						prod.setAgentGroup(uuskood);
						prod.setOnPallet(Double.parseDouble(lisakogus));
						Product prod2 = new Product(ean);
						prod2.setStock(Double.parseDouble(kogus));
						prod2.setGroup(lisakood);
						prod2.setAgentGroup(uuskood);
						prod2.setOnPallet(Double.parseDouble(lisakogus));
						Vector<Product> vec = (Vector<Product>) haldusklienditooted.get(kl1);
						if(vec == null){
							vec = new Vector<Product>();
						}
						vec.add(prod);
						haldusklienditooted.put(kl1, vec);
						Vector<Product> vec2 = (Vector<Product>) haldusklienditooted.get(kl2);
						if(vec2 == null){
							vec2 = new Vector<Product>();
						}
						vec2.add(prod2);
						haldusklienditooted.put(kl2, vec2);						
					}
			}
			}catch(Exception ee ){
				ee.printStackTrace();
			}
  }
  
  private Product hasHaldusCustomerAndProduct(String kl,String ean){
	  Product res = null;
	  if(halduskliendid.contains(kl)){
      Vector<Product> vec = (Vector<Product>)haldusklienditooted.get(kl);
      if(vec != null){
    	  Iterator vItr = vec.iterator();
    	  while(vItr.hasNext()){
    		  Product prod = (Product)vItr.next();
    		  if(prod.getID().equals(ean)){
    			  return prod;
    		  }
    	  }
        }    
	  }
	  return res ;
  }
  
  
  
  private void  prepareDateInfo(){
	  String[] list = new File(getDir()).list();
	  //System.out.println(list.toString());
	  
	  //meil on 4 tyypi faile ja on vaja m��rata nende kuup�evad mis on k�ige v�rskem 
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
	  System.out.println(dates.toString());
  }
  
  public String getSaveDebtInfo() {
	  makePictureCat();
	  System.exit(0);
   	return null;
   }
  
  
  
  private void makeNormpict(String pictspda,String pictssou ){
  	  File res2 = new File (pictspda);
        String[] ff2 = res2.list();
        System.out.println("ff2 suur " + ff2.length);
    	 for(int i = 0; ff2.length > i ; i++){
    		if(ff2[i].indexOf("img") != -1 ){
    	  		try {
    	  			System.out.println("min " + pictspda + ff2[i] + " | " + ff2[i].substring(0,ff2[i].indexOf("img")) + ".jpg");
    				shrinkAndCopyFile(pictspda + ff2[i],pictssou + ff2[i].substring(0,ff2[i].indexOf("img")) + ".jpg");
    			} catch (Exception e) {
    				e.printStackTrace();
    			}	
    		}
    		
    	 }
    }
  private void makePictureCat(){
	    
  	
  	String picts = parent.getProps().getProperty("pict.dir");
	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	    String pictssou  = parent.getProps().getProperty("pictpda.source");
	 makeNormpict(picts,pictssou);
     
	System.exit(0);
  	
	 try {
  	TreeMap prods = this.getProducts(null);

       StringBuffer buf = new StringBuffer();
       StringBuffer buf0 = new StringBuffer();
       int a = 0;
       int b = 0;
       int c = 0;
       Iterator enume = prods.keySet().iterator();
  //     System.out.println("Piltide leidmine ");
  	    while (enume.hasNext()) {
  	    	String asi = (String)enume.next();
  	    	Product prod = (Product)prods.get(asi);
  	    	a++;
  	    	c++;
  	    	if(c == 100){
  	    		  c =0;
  	    //		  System.out.println("Piltide leidmine "  + a);	
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
  	//  System.out.println("Kataloogi kustutamine "  + a);	    
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
       	 
     //    System.out.println("Piltide kopeerimine ");	
         //Failide kopeerimine  
           String[] gg = new File(picts).list();
        	  //System.out.println("gg " + gg[0] + " " + gg.length);
           
            for(int i = 0;gg.length>i;i++){
            	
            	File in = new File ( picts + gg[i]); 
            	if(in.isFile()){
            	
            	File inpda = new File ( pictspda + gg[i]); 
           
                  	copyFile(in,inpda);
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
  public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
	  TreeMap toxml = new TreeMap();
	  String agent = wr.getDefective().getAgentID();
      for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {   	  
        DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());
	              if(toxml.get(lw.getLine().getCustID()) != null){
            	Vector vv = (Vector)toxml.get(lw.getLine().getCustID());
            	vv.add(lw);
            	toxml.put(lw.getLine().getCustID(), vv);
            }else{
            	Vector vv = new Vector();
            	vv.add(lw);
            	toxml.put(lw.getLine().getCustID(), vv);
            }
          
  }
         // makeXMLMeri(toxml,agent);
  }
  
  public void insertDefective(DefectiveWrapper wr){
	
        
      
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


  
 


  private String getDateString(Calendar cal){
    StringBuffer sb = new StringBuffer();
    String year =   cal.get(Calendar.YEAR) + "";
    sb.append(year.substring(2,4));
    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    if (month.length() == 1){
      sb.append("0");
    }
    sb.append(month);

    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    if (day.length() == 1){
      sb.append("0");
    }
    sb.append(day);

    return sb.toString();
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
   	fixedLines = new Vector();                                                             

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
		         				MatrixLine fline = new MatrixLine();

		         			    double price = 0;
		         				String custID = null;
		         				String clGroup = null;
		         				String prodID = null;
		         				String discGroup = null;
		         				String prodGroup = null;
                                 boolean yks = false;
                                 boolean kaks = false;
                                 boolean kolm = false;
		         				double discount = 0;
		         				String start = null;
		         				String kamp  = null;
		         				String end = null;
		         				 Node item = getNode(rida,"Material");
		         				
		         				try{
		         					prodID  = item.getFirstChild().getNodeValue();	
		         					kaks = true;
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         				 
		         				try{
		         					discGroup  = getNode(rida,"HierarchyCustomer").getFirstChild().getNodeValue();
		         					mline.setDiscGroup(discGroup.trim());
		         					fline.setCliGroup(discGroup.trim());
		         					yks = true;
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         				
		         				
		         				try{
		         					prodGroup  = getNode(rida,"ProductGroupCode").getFirstChild().getNodeValue();
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
		         					 kolm = true;
		         				}catch(Exception e){
		         					//System.out.println("id " +prodID);
		         					//e.printStackTrace();
		         				}
		         				
		         				try{
		         					 discount  = Double.parseDouble(getNode(rida,"Discount").getFirstChild().getNodeValue());		         					
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         				boolean goes = true ;
		         				boolean kampaania = false;
		         				try{
		         					 kamp  = getNode(rida,"Fixed").getFirstChild().getNodeValue();	
		         					 if(kamp.equals("X")){
		         						 kampaania = true;
		         					 }
		         				}catch(Exception e){
		         					//e.printStackTrace();
		         				}
		         				
		         				try{
		         					clGroup  = getNode(rida,"Payer").getFirstChild().getNodeValue();
		         					fline.setCliGroup(clGroup.trim());
		         					yks = true;
		         					if(clGroup.trim().length() == 0 && discGroup == null ){
		         						goes = false;
			         					hinnad.put(prodID.trim(), price + "");
		         					}
		         				}catch(Exception e){
		         					if(discGroup == null ){
		         						goes = false;
		         						hinnad.put(prodID.trim(), price + "");
		         					}
		         				}
		         				
		         				  if(prodID == null && prodGroup == null){
		         					  kliendialed.put(clGroup.trim(), discount + "");
		         					  goes = false;
		         				  }
		         				
		         				if (mline.getCustID() == null && clGroup != null
		         						&& clGroup.trim().length() > 0) {
		         					mline.setCliGroup(clGroup.trim());
		         				//	mline.setCustID(clGroup.trim());
		         				}

		         				if (prodID != null && prodID.trim().length() > 0) {
		         					mline.setProdID(prodID.trim());
		         					fline.setProdID(prodID.trim());
		         				}
		         				if (mline.getProdID() == null && prodGroup != null
		         						&& prodGroup.trim().length() > 0) {
		         					mline.setProdGroup(prodGroup);
		         				}
		         				
		         				if(start != null){
		         					mline.setStartDate(start);
		         				}
                                if(end != null && end.indexOf("999") == -1){
                                	mline.setEndDate(end);
		         				}
                                
                                if(end.indexOf("999") != -1){
                                	mline.setStartDate(" ");
                                }
		         				
                                 if(!kampaania){
                                	 mline.setStartDate(" "); 	 
                                 }
                                 
                                if (price != 0) {
		         					mline.setPrice(price);
		         				}
		         				
		         				//mline.setCustID(mline.getCliGroup());
		         				if(discount >0){
		         					mline.setDiscount(discount);
		         					mline.setPrice(0);
		         				}
		         			

		                    if(goes)   {
		                    	lines.add(mline);
		                    //	System.out.println(mline.getCustID() + " " + mline.getCliGroup() + " " + mline.getProdID());
		                    }
		         					
		                    if(yks && kaks && kolm ){
		                    	fixedLines.add(fline);
		                    }
		         				  
	         		  }
	                   }}  
	         	  }
	         }catch(Exception t){
	        	 t.printStackTrace();
	         }

 System.out.println("Hinna maatriksi suurus " + lines.size());
	return lines;

   }
   
   
   private Vector sortLines(Vector inVec,TreeMap products){
       Vector vec = new Vector();
       System.out.println("Prod22 " + products.size() + " " + inVec.size());
       for (int i = 0 ; i < inVec.size() ; i++){
         vec.addElement(inVec.elementAt(i));
       }
       Vector sorted = new Vector(vec.size());
       while(vec.size() > 0){
    	//   System.out.println("Vec  " + vec.size());
         MatrixLine smallest = null;
         Product smProd = null;
         for (Enumeration enumr = vec.elements(); enumr.hasMoreElements() ; ){
       	  MatrixLine ol = (MatrixLine)enumr.nextElement();
           Product prod = (Product)products.get(ol.getProdID());
           if(prod != null){
        	//   System.out.println("Prod " + prod.getID() + "  " + ol.getProdID());
               if ( (smallest == null && smProd == null )
               		|| ( prod.getName().compareTo(smProd.getName()) < 0 )) {
                 smallest = ol;
                 smProd = (Product)products.get(smallest.getProdID());
               }   
           }else{
        	   smallest = ol; 
           }
      
          
         }
         vec.removeElement(smallest);
         sorted.addElement(smallest);
        
       }
       System.out.println("sorting tehtud " + sorted.size());
       return sorted;
     }


   public Vector getFixedAssortmentData() {
	   getProducts(null);
	   System.out.println("fixlines sees");
	   return sortLines(fixedLines,prods);
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
	         		  if(payer.length() > 7){
	         			  payer = payer.substring(payer.length()-7);  
	         		  }
	         	
                      debt.setCustID(payer);                     
                      String number = getNode(rida,"BillingDocumentNo").getFirstChild().getNodeValue().replaceAll("  ", " "); 
                      debt.setDoknr(number);
                      String sum = getNode(rida,"UnpaidAmount").getFirstChild().getNodeValue().trim();
                      if(sum.endsWith("-")){
                    	  sum = "-" + sum .substring(0,sum.length()-1);
                      }
                      debt.setSum(Double.parseDouble(sum));
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
		 			//  g.printStackTrace();
		 		  }
	         	  }

	 		}
	 		  }
	 		   }catch(Exception r){
	 			  r.printStackTrace(); 
	 		   } 		
	 		System.out.println("arveid v�las " + vec.size());
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
   	    	 name   = getNodeAttr(rida,"GroupDescription","LV").getFirstChild().getNodeValue();
    	   }catch(Exception e ){     	    	
   	    	 name   = getNodeAttr(rida,"GroupDescription","EN").getFirstChild().getNodeValue(); 
   	      }    
    	   
    	//   System.out.println(id + " " + name);
     		     tm.put(id, deCode(name));    
     		   
      	}}}}}
	  
     }catch(Exception r){
    	 r.printStackTrace();
     }
     	  
         return tm;
    }
   private TreeMap teine = new TreeMap();
 
   public TreeMap getProdGroupsSecond() throws SQLException {
	   return teine;
        }


   public static String deCode(String res ){	  
 	return Converter.enCodeEst(res);
 }
   

   
  
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap(); 
     try {
    	// makePictureCat();
    	
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
     getPriceMatrixData();
     StringBuffer sb2 = new StringBuffer();
 	try {
 		
 		 
 		 FileReader fr = new FileReader("vacation.txt");
         BufferedReader reader = new BufferedReader(fr);
       String line = null;
      HashMap asendajad = new HashMap();
      
        while((line = reader.readLine()) != null ){
           
        	StringTokenizer st = new StringTokenizer(line, ";");
//  System.out.println(line);
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
	 
 		
        Vector done = new Vector();
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
                      

                      if(asendajad.get(salesman) != null){
                        	Vector vv = (Vector)asendajad.get(salesman);
                        	Enumeration aa = vv.elements();
                        	while(aa.hasMoreElements()){
                        		sm.addDubbler((String)aa.nextElement());
                        	}
                        }
                 
                      if(done.contains(sm.getID()) && salesmanName != null){
                    	  sb2.append(sm.getID() + ";" + salesmanName + "\n");  
                          done.add(sm.getID()); 
                          agents1.put(sm.getID(), sm);
                          tm.put(salesmanName, sm);
                          System.out.println("ag " + sm.getDubbler().toString());
                      }
                     
                   
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
     Agent a = new Agent("HOUSE");
     a.setName("HOUSE");
     tm.put("HOUSE", a);
  
     if(sb2.length() > 2){
     	try{
     		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
     	}catch (Exception e){
     		
     	}
       
      }
     
     return tm;
   }

   public TreeMap getAgents1() {
  		return agents1;
  	}
      private TreeMap agents1 = new TreeMap();

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

   public TreeMap getCustomers(Agent agent){
	     TreeMap tm = new TreeMap();

	    
	     this.customers = new ArrayList();
	   
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
	         			 customer.setName(deCode(name + " " + aadr));
	         			 
	         		//	 System.out.println(customer.getName());
	         			 
	         			 try{
		         			 String post =   getNode(getNode(rida,"PartnerAddress"),"PostalCode").getFirstChild().getNodeValue(); 
		         			String linn =   getNode(getNode(rida,"PartnerAddress"),"City").getFirstChild().getNodeValue();  
			         		 aadr = aadr + " " +(deCode(post + "," +linn ));  	  
		         		  }catch(Exception ea){
		         		  }
	         			 
		         		  customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr), "Latvia"));
		         		  
	         		  }catch(Exception e){
	         			  
	         		  }
	         		
	         	     try{
     		    	 	 String contacname =  getNode(rida, "ContactFirstName").getFirstChild().getNodeValue()   ;
	         			 if(contacname.trim().length() >0){
			         		  customer.setPersonalData(deCode(contacname)); 
	         			 }  	  
	         		  }catch(Exception e){}
	         		  
	         		   try{
		         			 String contacname =  getNode(rida, "ContactLastName").getFirstChild().getNodeValue()   ;
		         			 if(contacname.trim().length() >0){
		         				 if(customer.getPersonalData() != null){
					         		  customer.setPersonalData(customer.getPersonalData() + " " + deCode(contacname));  
			         			 }else{
					         		  customer.setPersonalData(deCode(contacname)); 
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
		         		  }catch(Exception e){
		         		  }
	         		  
                    	
                        try{ String grupp  =  getNode(rida,"PricatPartyCode").getFirstChild().getNodeValue();
	         		     customer.setGroup(deCode(grupp.trim()));
	         		     Vector vv = new Vector();
	         		     vv.add(deCode(grupp.trim()));
	         		    customer.setFixSortCode(vv);
	         	            }catch(Exception e){ }
                        
		         		    
	         		 try{  String paym =  getNode(getNode(rida,"ContractInfo"),"PaymentTerm").getFirstChild().getNodeValue();
	         		  customer.setPaymTerm(deCode(paym));
                         }catch(Exception e){ }
                        
                         
                     try{ String crlim =  getNode(getNode(rida,"ContractInfo"),"CreditLimit").getFirstChild().getNodeValue();
        	         		  customer.setCredit(crlim);
                                 }catch(Exception e){  } 
                     
                                 try{ String visit =  getNode(rida,"VisitDay").getFirstChild().getNodeValue();
           	         		 visit = visit.replaceAll(",", "");
                                 customer.setVisit(visit);
                               //  System.out.println("visit " + visit);
                                    }catch(Exception e){ } 
                                 
                     try{ String salesman  =  getNode(rida,"UserID").getFirstChild().getNodeValue();
	         		      salesman = salesman.substring(0,salesman.indexOf(":"));
	         		      Agent ag = new Agent(deCode(salesman));
	         		      customer.addAgent(ag);
	         		      customer.setMainAgent(salesman);
	         		     customer.addAgent(new Agent(deCode("HOUSE")));
	         		     
	         		     
	         		    if (ag != null ){
	           				Agent agg = ((Agent)this.getAgents1().get(salesman));
	         				
	           				if(agg != null && agg.getDubbler().size()>0){
	           					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           					}
	           				}
	           			}
	         		     
	         		     if(!parent.getEmail(salesman).equals("") &&  !custtsUsed.contains(id)){     		   
	     	         		   custtsUsed.add(id);
	           		      } }catch(Exception e){ }
	         	   
	           		      
	                  try{ String payer  =  getNode(rida,"PayerPartnerCode").getFirstChild().getNodeValue();
		         		      customer.setPayer(deCode(payer.trim()));	 
		         		   // customer.setGroup(deCode(payer.trim()));	
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
			        
	         	         
	         	         boolean goes = true;
			          try{ String blocked  =  getNode(rida,"Blocked").getFirstChild().getNodeValue();
				         		
				         		 if(blocked.equals("1")){
				         		      customer.setSalesBlock(true); 
				         //		      goes = false;
				         		  }
				         	   }catch(Exception e){}
				         	   
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
	 TreeMap prods = new TreeMap();
	 
   public TreeMap getProducts(Agent agent){
	   prods = new TreeMap();
    TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     System.out.println("Tooted alustan");
     
     
    
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
           	
          		 boolean goes = true;
          	     String id = getNode(rida,"SellerItemCode").getFirstChild().getNodeValue();
          		   Product prod = new Product(deCode(id));
          		  
          		 try{
       	    		 String grupp = getNode(rida, "ItemGroupCode").getFirstChild().getNodeValue();
       	    		 //this.prGroups.put(grupp, grupp);
       	    		
       	    		   
       	    	    prod.setGroup(grupp.trim());
      	    	   //  prod.setGroupSecond(grupp.substring(grupp.length()-2));   
       	    	   
       	    	 
       	    	 // prod.setGroupSecond(grupp.substring(0,1) + grupp);
       	    	  //	System.out.println("grupp "+ prod.getGroup());   
       	    	 }catch (Exception e){
       	    		 e.printStackTrace();
       	   	 }
          		      		   
          		 String eans = "";
       	      try{
          		 String ean   = getNode(rida,"GTIN").getFirstChild().getNodeValue();
          	     prod.setEAN(ean);
          	     if(ean.length()>4){
          	    	 eans = ean.substring(ean.length()-4);
          	     }
      	      }catch(Exception e ){}
      	      
      	     
      	    String name   = "";
      	  String name2   = "";
        	      try{
        	    	   name   = getNodeAttr(rida,"ItemDescription","Z1").getFirstChild().getNodeValue();
        	         name2 = Converter.cleanLitLatEst(name.substring(0,2));
        	    	   prod.setName(name2 + deCode(firstToUpperCase(name)) + " " + prod.getID() + " " + eans); 
    
         	 
        	      }catch(Exception e ){ 
        	    	  
        	    	  try{ 
        	    	   name   = getNodeAttr(rida,"ItemDescription","LV").getFirstChild().getNodeValue();
        	    	   name2 = Converter.cleanLitLatEst(name.substring(0,2));
        	    	   prod.setName(name2 + deCode(firstToUpperCase(name)) + " " + prod.getID() + " " + eans); 
 }catch(Exception tt){
	 try{
		  
		   name   = getNodeAttr(rida,"ItemDescription","EN").getFirstChild().getNodeValue();
		   name2 = Converter.cleanLitLatEst(name.substring(0,2));
		   prod.setName(name2 + deCode(firstToUpperCase(name)) + " " +  prod.getID() + " " + eans);   
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
   		 
   		prod.setPrice(hind);
   	   }else{
   		//   goes = false;
   	   }
   	    	 
   	    	 try{  prod.setInCases(Double.parseDouble(getNode(getNode(rida,"ItemUnitRecord"),"UnitNumerator").getFirstChild().getNodeValue())); 
 	    	 }catch (Exception e){}
 	    
 	    	 try{  prod.setOnPallet(Double.parseDouble(getNode(getNode(rida,"ItemUnitRecord"),"PiecesPerPallet").getFirstChild().getNodeValue())); 
 	    	 }catch (Exception e){}
 	    
   	    	 try{ prod.setUnit(getNode(rida, "BaseUnit").getFirstChild().getNodeValue());	 
   	    	 }catch (Exception e){   	 }
    	    	
   	       	   prod.setVat("21");
    	      //    prod.setStock(Double.parseDouble(stock.substring(0,stock.indexOf("\""))));
        	 	 
        	    if(goes && prod.getName()!= null){
        	    
        	         prodHash.put(prod.getID(), prod);
        	         prods.put(prod.getID(), prod);
        	         tm.put(Converter.cleanLitLatEst(name) + prod.getID(), prod);	
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
							 System.out.println("File" + getDir() + fname);
						
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
		
		//	 	System.out.println("laoseis " + result.size());
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
     return "AltiaLAT";
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
				         	  if(rida.getNodeName().equals("SalesPerson")){
//				         		 System.out.println("ridu  " + rida.getNodeName());
						         	 String agent  = getNode(rida,"Code").getFirstChild().getNodeValue().trim();
						         	 String salesCur = getNode(rida,"SalesInLocalCurrency").getFirstChild().getNodeValue().trim();
						         	 String salesVol = getNode(rida,"SalesInVolume").getFirstChild().getNodeValue().trim();
						         	 Agent ag = (Agent)agents.get(agent);
						         	 ag.setMonthSales((int)Double.parseDouble(salesCur));
						         	 ag.setStart("Litriem :" + (int)Double.parseDouble(salesVol));
						         	// System.out.println("ridu  " + ag.toString());
						         	 
				         	  }
				         
				       }
				      break; 
				       
				}	
		}catch(Exception e){
			e.printStackTrace();
		}
	
		}		
		     
  }
}
