
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.Email;
import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class NaviOrklaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public NaviOrklaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
   if( parent.getProps().getProperty("dimension") != null ){
	   dimension = parent.getProps().getProperty("dimension");
   }
   loadSignatures();
   nf.setMaximumFractionDigits(4);
   nf.setMinimumFractionDigits(4);
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
    
    aletooted = getAmountCampProdstring();
  }

   private TreeMap aletooted = null ;
	private Vector signatures = new Vector();

	private void loadSignatures(){
		
try{		
		File f = new File("signatures.vec");
		Calendar ss = Calendar.getInstance();
		
		if (f.exists() && f.lastModified() < ss.getTimeInMillis() - 13600000) {// kui on yle tunni vana siis maha 
			f.delete();
			signatures = new Vector();
		} else {
			signatures = (Vector) readObject("signatures.vec");
		}

		
	} catch (Exception e) {
		signatures = new Vector();
	}

	}

		
	   public static Object readObject(String s) throws IOException,
     ClassNotFoundException, IOException {
   Object obj = null;
   File f = new File(s);
   if (f.exists()){
     ObjectInputStream ois = new ObjectInputStream(new FileInputStream(s));
     obj = ois.readObject();
     ois.close();
   }
   return obj;
 }

  
  
  private String dimension = "ork";
  
  public String identifyCustomer(String id,String regnr) throws SQLException {

	    String acc = null;
	    if(regnr != null && regnr.trim().length()>0){

	    String sql2 = " SELECT accountnum  AS kood FROM bmssa.custtable where dataareaid='" + dimension + "'"
	    		+ " and  (REGNUM_W)= '" + regnr + "' AND ouraccountnum = '" + id + "'";
	    System.out.println(sql2);
	    

	Connection 	con3 = 	getNewConnection();	   	
	
	 Statement st2 = con3.createStatement();
	    ResultSet rs2 = st2.executeQuery(sql2);

	    while (rs2.next()) {
	      acc = rs2.getString("kood").trim();

	    }
	    rs2.close();
	    st2.close();
	    con3.close();

	    }

	    

	    if(regnr == null && acc == null){

	        String sql2 = " SELECT accountnum  AS kood FROM bmssa.custtable where dataareaid='" + dimension + "' and"
	        		+ "   accountnum = '" + id + "'";
	        System.out.println(sql2);

	        Connection con3 = 	getNewConnection();	   	
	    	
	        Statement st2 = con3.createStatement();
	        ResultSet rs2 = st2.executeQuery(sql2);

	        while (rs2.next()) {
	          acc = rs2.getString("kood").trim();

	        }
	        rs2.close();
	        st2.close();
	   con3.close();
	        }

	    if(regnr == null && acc == null){

	        String sql2 = " SELECT accountnum  AS kood FROM bmssa.custtable where dataareaid='" + dimension + "' "
	        		+ "and   outsideid = '" + id + "' ";
	        System.out.println(sql2);


	        Connection con3 = 	getNewConnection();	   	
	    	
	        Statement st2 = con3.createStatement();
	        ResultSet rs2 = st2.executeQuery(sql2);

	        while (rs2.next()) {
	          acc = rs2.getString("kood").trim();
	        }
	        
	        rs2.close();
	        st2.close();
	        con3.close();
	        }
	    
	    
	  	if (acc == null){

	  	String sql1 = " SELECT accountnum  AS kood FROM bmssa.custtable where dataareaid='"+dimension+"' "
	  			+ "and  (REGNUM_W)= '" + id + "' AND ouraccountnum = '" + id + "'";
	    System.out.println(sql1);


	    Connection con3 = this.getNewConnection();
	    Statement st1 = con3.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim();

	    }
	    rs1.close();
	    st1.close();
       con3.close();
	  	}
	   System.out.println("Klient tuvastatud: " + acc);
	    return acc;

	  }



  
  private Connection getNewConnection() throws SQLException{
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsna.user"), parent.getProps().getProperty("dsna.pass"));
	  }

 
  
 
	  public String identifyProduct(String ean, String prodID) throws SQLException{
		    String sql1 = "SELECT inv.itemid AS toode ,bar.itembarcode FROM bmssa.inventtable inv left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid"
		    		+ "  left join bmssa.inventitembarcode bar ON bar.dataareaid = inv.dataareaid "
		    		+ "and bar.itemid=inv.itemid and bar.useforprinting = 1  where "
		    		+ "blocked = 0 and moduletype = 2 and  mod.dataareaid = '" + dimension + "' and "
		    				+ "inv.dataareaid='" + dimension + "' " +
		    		" AND bar.itembarcode='" + ean + "' order by inv.itemid " ;


		    String sql2 = "SELECT inv.itemid AS toode FROM bmssa.inventtable inv left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid"
		    		+ " where blocked = 0  and moduletype = 2 and  inv.dataareaid = '" + dimension + "' and "
		    				+ "mod.dataareaid = '" + dimension + "' and inv.itemid='" + prodID + "' ";

		    String prod = null;
		    ResultSet rs1 = null;

		    Connection con3 = 	getNewConnection();	   	
			
		    Statement  st1 = con3.createStatement();
		  if(prodID != null){
			   
			     rs1 = st1.executeQuery(sql2);
			//	System.out.println(sql2);
			    while (rs1.next()) {
			      prod = rs1.getString("toode").trim();
			  //	System.out.println("prod1 " + prod );
			    } 
			    
		  }
		 

		  

		    if(prod == null){
		      rs1 = st1.executeQuery(sql1);
		      while (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      //	System.out.println("prod2 " + prod );
		      }
		    }
		    rs1.close();
		    st1.close();
		    con3.close();
		   System.out.println("prod" + prod +" "+ ean+ " " + prodID);
		   if(prod == null){
		   	prod = prodID;
		   }
		    return prod;
		  }

	  
	  

	  
	  


	public void writeConsloStr(String path,String e) {
		
		try {
			
			File f = new File(path);
			if(!f.exists()){
				  try{
				      FileOutputStream fos = new FileOutputStream(path);
				        fos.write("".getBytes());
				        fos.close();
				
			} catch (Exception uu) {
				uu.printStackTrace();
			}
			}
			
			PrintWriter os = new PrintWriter(new FileWriter(
					path, true));
			os.write(e);
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

}
	   
private String getDir(){
 String dir = parent.getProps().getProperty("excel.dir");
 if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
 return dir;
}

	
	
	 private TreeMap getAmountCampProdstring( ){
		  TreeMap asi = new TreeMap();
		  StringBuffer buf = new StringBuffer();
		  try{
			
		  FileReader fr = new FileReader(this.getDir() + "amountdiscProds.txt");
	       BufferedReader reader = new BufferedReader(fr);
	     String line = null;
	    
	      while((line = reader.readLine()) != null ){
	    	
	      	StringTokenizer st = new StringTokenizer(line, ";");
	      	if (st.hasMoreTokens()) {
	      	 
	         	String  toodex = st.nextToken();
	         	try{
	         		double  perc = Double.parseDouble(st.nextToken());
	             	double  kogus = Double.parseDouble(st.nextToken());
	             	
	             	
	             	if(asi.get(toodex) != null){
	             	  double pp = Double.parseDouble(asi.get(toodex)+ "");
	             		if(pp < perc){
	             			asi.put(toodex, perc);	
	             		}
	             	}else{
	             		asi.put(toodex, perc);
	             	}
	             	
	             	
	         	}catch(Exception e){
	         		
	         	}
	        }

	      }
	      reader.close();
	      fr.close();
	      
		  }catch(Exception r){
			r.printStackTrace();  
		  }
		

		  return asi;
	  }

	
	
	
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
	
	
	  private String identfyType(OrderLine lin) throws SQLException{
		 	 String res = "norm";
		 	 if(lin.getKogus() < 0 && lin.getDiscount() > 0){
		   	  res = "reprice";
		     }else if(lin.getKogus() < 0){
		      res = "return";
		     }
		 	 return res;
		  }
	
	 private Vector splitOrder (Order order){
		  Vector result = new Vector();
	      
		  Order ordNorm = new Order(order.getCompany(),order.getAgentFirstName());
		  Order ordReprice = new Order((order.getCompany()),order.getAgentFirstName());
		  Order ordReturn = new Order((order.getCompany()),order.getAgentFirstName());

		     
		  ordNorm.setAgentCode(order.getAgentCode());
		  ordNorm.setDokNr(order.getDokNr());
		  ordNorm.setInfo(order.getInfo());
		  ordNorm.setTeostaja((order.getTeostaja()));
		  ordNorm.setChanged(order.isChanged());
		  ordNorm.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordNorm.setStatus(order.getStatus());
		  ordNorm.setShipTime(order.getShipTime());
		  ordNorm.setWarehouse("norm");
		     
		  
		  
		  ordReprice.setAgentCode(order.getAgentCode());
		  ordReprice.setDokNr(order.getDokNr());
		  ordReprice.setInfo(order.getInfo());
		  ordReprice.setChanged(order.isChanged());
		  ordReprice.setTeostaja((order.getTeostaja()));
		  ordReprice.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordReprice.setStatus(order.getStatus());
		  ordReprice.setShipTime(order.getShipTime());
		  ordReprice.setWarehouse("reprice");
		
		
		  ordReturn.setAgentCode(order.getAgentCode());
		  ordReturn.setDokNr(order.getDokNr());
		  ordReturn.setInfo(order.getInfo());
		  ordReturn.setTeostaja((order.getTeostaja()));
		  ordReturn.setChanged(order.isChanged());
		  ordReturn.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordReturn.setStatus(order.getStatus());
		  ordReturn.setShipTime(order.getShipTime());
		  ordReturn.setWarehouse("return");  
		
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		  
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
			
			    String ware = null;
				try {
					ware = identfyType(lin);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			     if(ware.equals("norm")){  
			    		 ordNorm.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);	 
			     	 
			         }else if(ware.equals("reprice") ){
				    		ordReprice.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     
				         }  else if(ware.equals("return") ){
					    	
					     	ordReturn.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
					     			 lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
					     }
			
		  }

		  if(ordNorm.getOrderLines() != null && ordNorm.getOrderLines().size()>0){
			  result.add(ordNorm);  
		  }

		  if(ordReprice.getOrderLines() != null && ordReprice.getOrderLines().size()>0){
			  result.add(ordReprice);  
		  }
		  
		  if(ordReturn.getOrderLines() != null && ordReturn.getOrderLines().size()>0){
			  result.add(ordReturn);  
		  }
		 
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());

		 return result ;
	  }



	
	

  
  public void makeOrder(Order order) throws SQLException{
	   if (signatures == null){
    	   signatures = new Vector();
       }
	  
	   
	   Vector splitted =  splitOrder(order);
	   System.out.println("tellimusi firmadesse " + splitted.size());
         Enumeration en = splitted.elements();
		  while(en.hasMoreElements()){
			  
			  Order ord = (Order)en.nextElement(); 
			  
			  
			  //kui on tavalised read 
				  if(ord.getWarehouse().equals("norm")){
						
					        counter = 0;
					  	    addlocks(false);
					  	    
					  	  String signature = ord.getSignature();
					  	 //System.out.println("signature " +signature);
						 
					  	  //System.out.println("signatures " +signatures.contains(signature) + " " + signatures.toString());
						  if(signatures.contains(signature)){
							  this.trouble = "REPRETATING ORDER agent :" + ord.getAgent() + " customer :" + ord.getCompany() ;
						   }else{
							  makeOutput(ord);  
							  signatures.add(signature);
							  this.trouble = null;
						  }
					  	    
						  try {
								saveObject(signatures, "signatures.vec");
							} catch (IOException e) {
								e.printStackTrace();
							}
					  	     
					 		  String lineSep = System.getProperty("line.separator");
				  		  	    writeConsloStr(parent.getProps().getProperty("conv.dir") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
				       		    addlocks(true);
				          setNumAddedLines(counter);
				          
	  
				  }
				  
				  //kui on reprice read 
				  if(ord.getWarehouse().equals("reprice")){
					  addlocks2(false); 
				  		 makeOutput2(ord);
				  		  String lineSep = System.getProperty("line.separator");
						  		 writeConsloStr(parent.getProps().getProperty("conv.dir2") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
				             addlocks2(true); 
				   		     setNumAddedLines(ord.getOrderLines().size()); 
					    	  System.out.println("Repriceing " +fileName + " " + ord.getOrderLines().size());
				  		  				
		
				  }	
				  
				//kui on reprice read 
				  if(ord.getWarehouse().equals("return")){
					  addlocks3(false); 
				     		 makeOutput3(ord);
				  		     String lineSep = System.getProperty("line.separator");
						     writeConsloStr(parent.getProps().getProperty("conv.dir3") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
				             addlocks3(true); 
				   		     setNumAddedLines(ord.getOrderLines().size()); 
					    	 System.out.println("Return " +fileName + " " + ord.getOrderLines().size());
						  }	
			  
			  
		  }
	   
	   
	/*  boolean salvesta = true;
	 	 Vector lin = order.getOrderLines();
	 	 int nega = 0;
	 	 for (int i = 0; i < lin.size(); i++) {
	 		OrderLine ol = (OrderLine) lin.elementAt(i);
	 		//System.out.println("XXXXX orderStatus " + ord.getStatus());
	 		if(ol.getKogus() < 0 && ol.getDiscount() > 0 && (order.getStatus() == 5 || order.getStatus() == 4)){
	 			nega++;
	 		}
	 		
	 	 }  
		    if(nega == lin.size()){
		    	salvesta = false;
		    }
	  if(salvesta){
		
	  counter = 0;
	  	    addlocks(false);
	  	    
	  	  String signature = order.getSignature();
	  	 System.out.println("signature " +signature);
		 
	  	  System.out.println("signatures " +signatures.contains(signature) + " " + signatures.toString());
		  if(signatures.contains(signature)){
			  this.trouble = "REPRETATING ORDER agent :" + order.getAgent() + " customer :" + order.getCompany() ;
			    		  
		  }else{
			  makeOutput(order);  
			  signatures.add(signature);
			  this.trouble = null;
			  
		  }
	  	    
		  try {
				saveObject(signatures, "signatures.vec");
			} catch (IOException e) {
				e.printStackTrace();
			}
	  	     
	 		  String lineSep = System.getProperty("line.separator");
  		  	    writeConsloStr(parent.getProps().getProperty("conv.dir") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
       		    addlocks(true);
          setNumAddedLines(counter);
          
      }else{
 	     addlocks2(false); 
  		 makeOutput2(order);
  		  String lineSep = System.getProperty("line.separator");
		  		 writeConsloStr(parent.getProps().getProperty("conv.dir2") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
             addlocks2(true); 
   		 setNumAddedLines(lin.size()); 
	    	  System.out.println("Ei Salvesta kuna neagtiivne on tell " +fileName + " " + lin.size());
  		  
	  }*/
	    
  }

  
  private String trouble = null;
  
  private void makeOutput2(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      String agent = order.getAgent();
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			if(ref.endsWith("null")){
				ref = ref.substring(0, ref.length()-4);
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         String klient = order.getCompany();
	         
	         
	         try{
	         sendCommentEmail(comm + " " + ref ,null, fn, order.getCompany(),getNimi(order.getCompany()),order.getAgent(), true);
	    		            		
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
				
	         
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	//  "LV0197-005_NP_120216","","MPAULICANS","LV0197-005","H180100700","4.0","45.0","374054","12.02.2016","7.1","3.905","2kg",
    // "13944_11291473","06.07.2016","CENTRS","13944","SP034562LV","20.0","25.0","11291473","06.07.2016","2","2"

	        String toode = ol.getToode();
	         String disc = "0";
		        double disco = 0;
		        if(ol.getDiscount()> 0){
		        	disc = ol.getDiscount()+"";
		        	disco = ol.getDiscount();
		        
		        double jaga = 1;
		        if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
		        	jaga = Double.parseDouble(ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
		        }
		        
		        if(ref == null || ref.trim().length() == 0){
		        	ref = order.getAgent().substring(0, 2)+ "-" + Calendar.getInstance().getTimeInMillis();
		        }
		        if(ref.length() > 30 ){
		        	ref = ref.substring(0,29);
		        }
		        if(ref.trim().indexOf(" ") != -1) {
		        	ref = ref.trim();
		        	ref = ref.substring(0,ref.trim().indexOf(" "));
		        }
		        
	        sb.append("\""+ ref + "\""+ sep);
	        sb.append("\""+"\"" + sep);
	        sb.append("\""+ agent + "\"" + sep);
	        sb.append("\""+ klient + "\"" + sep);
	        sb.append("\""+toode+"\"" + sep);
	        sb.append("\""+(ol.getKogus()*-1)+"\"" + sep);
	        sb.append("\""+disc+"\"" + sep);
	        sb.append("\""+comm+"\"" + sep); // kysimärk
	        sb.append("\""+ getDateString(Calendar.getInstance())+ "\"" + sep);
	        sb.append("\""+ol.getPrice()/jaga +"\"" + sep);
	        sb.append("\""+((ol.getPrice() - (ol.getPrice()*(disco/100)))/jaga)+ "\"" + sep);
	        sb.append("\""+ol.getUnit()+"\"" + sep);
	        sb.append(lineSep);
		        }
	       counter++;
		        
		        }
	      
	      setNumAddedLines(counter); 
	      

 	   fileName =   "R-" + order.getAgent() +"_"+ klient +"_" +fn  + ".csv";
	      
    String fname = (parent.getProps().getProperty("conv.dir2")) + fileName;
			    	if(sb.length() > 10){// kui pole discontiga ridu siis ei salvestatta faili 
			    	      FileOutputStream fos = new FileOutputStream(fname);
					        fos.write(sb.toString().getBytes());
					        fos.close();
			    	    		
			    	}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


  private void makeOutput3(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      String agent = order.getAgent();
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			if(ref.endsWith("null")){
				ref = ref.substring(0, ref.length()-4);
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         String klient = order.getCompany();
	        

	         Calendar cal = Calendar.getInstance();
		        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
		        	cal.add(Calendar.DAY_OF_MONTH, 4);
		        }else{
		        	cal.add(Calendar.DAY_OF_MONTH, 2);
		        }
		        
		        String date = null;
		        Calendar cal2 = Calendar.getInstance();
		         if(cal2.get(Calendar.DAY_OF_MONTH) != order.getShipCal().get(Calendar.DAY_OF_MONTH)){
		        	 cal = order.getShipCal();
		        	 date = Converter.getDDpMMpYYYYpString(cal);
		         }
	         
	         try{
	            	sendCommentEmail(comm + " " + ref , date, fn, order.getCompany(),getNimi(order.getCompany()),order.getAgent(), true);
	    		            		
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
				
	         
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	        OrderLine ol = (OrderLine) lines.elementAt(i);

	        String toode = ol.getToode();
	         String disc = ol.getDiscount()+"";
		        
		        double jaga = 1;
		        if(ol.getPartii() != null && ol.getPartii().indexOf("|") != -1){
		        	jaga = Double.parseDouble(ol.getPartii().substring(ol.getPartii().indexOf("|")+1).trim());
		        }
		        
		        if(ref == null || ref.trim().length() == 0){
		        	ref = order.getAgent().substring(0, 2)+ "-" + Calendar.getInstance().getTimeInMillis();
		        }
		        if(ref.trim().indexOf(" ") != -1) {
		        	ref = ref.trim();
		        	ref = ref.substring(0,ref.trim().indexOf(" "));
		        }
		       
		        
	        sb.append("\""+ ref + "\""+ sep);
	        sb.append("\""+ klient + "\"" + sep);
	        sb.append("\""+ getDateString(Calendar.getInstance())+ "\"" + sep);
	        sb.append("\""+toode+"\"" + sep);
	        sb.append("\""+(ol.getKogus()*-1)+"\"" + sep);
	        sb.append("\""+ol.getUnit()+"\"" + sep);
	        sb.append("\""+ol.getPrice()/jaga +"\"" + sep);
	        sb.append("\""+disc+"\"" + sep);
	        sb.append("\""+"C"+"\"" + sep); // kysimärk
	        sb.append("\""+ getDateString(cal)+ "\"" + sep);
		    sb.append("\""+"\"" + sep);
	        sb.append("\""+ agent + "\"" + sep);
	      
	        //sb.append("\""+((ol.getPrice() - (ol.getPrice()*(disco/100)))/jaga)+ "\"" + sep);
	       
	        sb.append(lineSep);
		        
	       counter++;
		        
		        }
	      
	      setNumAddedLines(counter); 
	      

	   fileName =   "R-" + order.getAgent() +"_"+ klient +"_" +fn  + ".csv";
	      
  String fname = (parent.getProps().getProperty("conv.dir3")) + fileName;
			    	if(sb.length() > 10){// kui pole discontiga ridu siis ei salvestatta faili 
			    	      FileOutputStream fos = new FileOutputStream(fname);
					        fos.write(sb.toString().getBytes());
					        fos.close();
			    	    		
			    	}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }


  
  
  private String fileName = null;
  
  
  private void sendCommentEmail(String comment,  String date ,String doknr,String custid, String nimi, 
		  String agent,boolean credit){
  	try{
  		
  		if(comment != null && comment.trim().length() >0 ){
  			Email mail = new Email(null);
	  		   
	  	  	mail.setSMTP(parent.getProps().getProperty("smtphost"));
            mail.setUser(parent.getProps().getProperty("smtpuser"));
            mail.setPass(parent.getProps().getProperty("smtppass"));
            mail.setPort(parent.getProps().getProperty("smtpport"));
            
	  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		if(credit){
	  	  		mail.setSubjectFilesTo( custid + " " + agent + " Kreditrekinam " + doknr + " tika pievienots komentars " );
		  	  		
	  	  		}else{
	  	  		mail.setSubjectFilesTo( custid + " " + agent + " Pasutijumam " + doknr + " tika pievienots komentars " );
		  	  			
	  	  		}
	  	  		String daatum = "";
	  	  	if(date != null && date.trim().length() >0){
	  	  daatum  = 	" Piegādes datums "+  "\n" +
	  				  "\n >>>>>" + date + "<<<<<\n\n";
	  	  	}
	  	  		
	  	  	if(credit){
	  	  	mail.setBody("\n" + custid + " " + nimi + " " + agent + "\n Kreditrekinam " + doknr + " tika pievienots komentars "+  "\n" +
	  				  "\n >>>>>" + comment + "<<<<<\n\n  " + daatum
	  				);
	  	  		}else{
	  	  		mail.setBody("\n" + custid + " " + nimi + " " + agent + "\n Pasutijumam " + doknr + " tika pievienots komentars "+  "\n" +
	  	  				  "\n >>>>>" + comment + "<<<<<\n\n  " + daatum
	  	  				);	
	  	  		}
	  	  		
	  	  	System.out.println("\n" + custid + " " + nimi + " " + agent + "\n Kreditrekinam " + doknr + " tika pievienots komentars "+  "\n" +
	  				  "\n >>>>>" + comment + "<<<<<\n\n  " + daatum
	  				);
	  	  	
	  	  		mail.setSendNotice(false);
	  	  		mail.send();
	  	  		
	  				
  		}else if(date != null && date.trim().length() >0){
  			Email mail = new Email(null);
	  		   
	  	  	mail.setSMTP(parent.getProps().getProperty("smtphost"));
            mail.setUser(parent.getProps().getProperty("smtpuser"));
            mail.setPass(parent.getProps().getProperty("smtppass"));
            mail.setPort(parent.getProps().getProperty("smtpport"));
            
	  	  		mail.setFilesTo(parent.getProps().getProperty("operatoremail"));
	  	  		mail.setFrom(parent.getProps().getProperty("from"));
	  	  		if(credit){
	  	  		mail.setSubjectFilesTo( custid + " " + agent + " Kreditrekinam " + doknr + " Piegādes datums " );
		  	  		
	  	  		}else{
	  	  		mail.setSubjectFilesTo( custid + " " + agent + " Pasutijumam " + doknr + " Piegādes datums " );
		  	  			
	  	  		}
	  	  		
	  	  	if(credit){
	  	  	mail.setBody("\n" + custid + " " + nimi + " " + agent + "\n Kreditrekinam " + doknr + " Piegādes datums "+  "\n" +
	  				  "\n >>>>>" + date + "<<<<<\n\n  "
	  				);
	  	  		}else{
	  	  		mail.setBody("\n" + custid + " " + nimi + " " + agent + "\n Pasutijumam " + doknr + " Piegādes datums "+  "\n" +
	  	  				  "\n >>>>>" + date + "<<<<<\n\n  "
	  	  				);	
	  	  		}
	  	  		
	  	  		mail.setSendNotice(false);
	  	  		mail.send();
	  	  		
	  				
  		}

  	
  	}catch(Exception e){
	  			e.printStackTrace();	
	  			}
	  	
  }

  
  
  private void addlocks(boolean delete ){
		 
	  	String fileNam = parent.getProps().getProperty("conv.dir") + "lock"; 
		 
		  File lck = new File(fileNam);
	  	
		  if(delete){
			 lck.delete(); 
		  }else{
			
		  if(lck.exists()){
			  System.out.println("LOCK Exists " + fileNam);
			  log.error(" LOCK Exists :" + new Date(lck.lastModified()).toGMTString());
			  System.exit(0);
		  }else {
			  try{
		      FileOutputStream fos = new FileOutputStream(fileNam);
		        fos.write("".getBytes());
		        fos.close();
		        log.error(" LOCK Created :" + new Date(lck.lastModified()).toGMTString());
				} catch (Exception e) {
					e.printStackTrace();
				}	  
			  }
		  
		  }
		  
		  }
  
  
  private void addlocks2(boolean delete ){
		 
	  	String fileNam = parent.getProps().getProperty("conv.dir2") + "lock"; 
		 
		  File lck = new File(fileNam);
	  	
		  if(delete){
			 lck.delete(); 
		  }else{
			
		  if(lck.exists()){
			  System.out.println("LOCK Exists " + fileNam);
			  log.error(" LOCK Exists :" + new Date(lck.lastModified()).toGMTString());
			  System.exit(0);
		  }else {
			  try{
		      FileOutputStream fos = new FileOutputStream(fileNam);
		        fos.write("".getBytes());
		        fos.close();
		        log.error(" LOCK Created :" + new Date(lck.lastModified()).toGMTString());
				} catch (Exception e) {
					e.printStackTrace();
				}	  
			  }
		  
		  }
		  
		  }
	  
	  
  
  private void addlocks3(boolean delete ){
		String fileNam = parent.getProps().getProperty("conv.dir3") + "lock"; 
		  File lck = new File(fileNam);
	  	  if(delete){
			 lck.delete(); 
		  }else{
		  if(lck.exists()){
			  System.out.println("LOCK Exists " + fileNam);
			  log.error(" LOCK Exists :" + new Date(lck.lastModified()).toGMTString());
			  System.exit(0);
		  }else {
			  try{
		      FileOutputStream fos = new FileOutputStream(fileNam);
		        fos.write("".getBytes());
		        fos.close();
		        log.error(" LOCK Created :" + new Date(lck.lastModified()).toGMTString());
				} catch (Exception e) {
					e.printStackTrace();
				}	  
			  }
		  }
    }
	  
	  

  
  
  
    private String getDateString(Calendar cal){
	    StringBuffer sb = new StringBuffer();
	    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	    if (day.length() == 1){
	      sb.append("0");
	    }
	    sb.append(day+"." );
	    
	    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
	    if (month.length() == 1){
	      sb.append("0");
	    }
	    sb.append( month + ".");
	    
	    sb.append(cal.get(Calendar.YEAR) + "");
		    return sb.toString();
	  }
	  
  
    public String getNimi(String id) throws SQLException {
	 	 String	sql1 = "SELECT k.[Name] AS nimi FROM [Orkla Foods Latvija SIA$Customer] k  " +
	  	"	  WHERE k.[Salesperson Code] != ''  AND No_ =  '" + id +"'"; 
	 	
	   System.out.println(sql1);
	   String agent = null;
	   Connection con = 	getNewConnection();	
	   Statement st1 = con.createStatement();
	   ResultSet rs1 = st1.executeQuery(sql1);

	   while (rs1.next()) {
	   	agent = rs1.getString("nimi").trim();
      }
	 
	   rs1.close();
	   st1.close();
	   con.close();
    
	   return agent;

	 }
  
  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    
		    String doknr = order.getDokNr();
		      String fn = doknr;
		      if(doknr.trim().length()== 0){
		    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
		      }
		      
			    String agent = order.getAgent();
			    
			      if(agent == null || agent.trim().length()== 0){
			    	 agent = "xxx";
			      } else{
			    	  agent = agent.trim();
			      }
			String ref = order.getTeostaja(); //mobiilse telli ref number
			if ( ref == null ){
				ref =  "";
			}
			String com = order.getEmailBody();
			if ( com == null ){
				com =  " ";
			}		
		
			String ware = order.getWarehouse();
			if ( ware == null ){
				ware = " ";
			}
			
			String comm = order.getInfo();
			if ( comm == null ){
				comm = " ";
			}
	         
			
			
	         String sep = ",";
	         String lineSep = System.getProperty("line.separator");
	         String klient = order.getCompany();
	       
	         Calendar cal = Calendar.getInstance();
		        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
		        	cal.add(Calendar.DAY_OF_MONTH, 4);
		        }else{
		        	cal.add(Calendar.DAY_OF_MONTH, 2);
		        }
		        
		        String date = null;
		        Calendar cal2 = Calendar.getInstance();
		         if(cal2.get(Calendar.DAY_OF_MONTH) != order.getShipCal().get(Calendar.DAY_OF_MONTH)){
		        	 cal = order.getShipCal();
		        	 date = Converter.getDDpMMpYYYYpString(cal);
		         }
	        
		         
		         try{
		            	sendCommentEmail(comm, date, fn, order.getCompany(),getNimi(order.getCompany()),order.getAgent(),false);
		    		            		
		        	}catch(Exception e){
		        		e.printStackTrace();
		        	}
					
		         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	 //    _E20587,"LV2174-003","04.02.2016","P120504511","120.0000000000","0.7634000000","10","","05.02.2016","4539006126","","0"
	//     _E20587,"LV2174-003","04.02.2016","N200111100","1440.0000000000","0.2720000000","0","","05.02.2016","4539006126","",""
      
	        String toode = ol.getToode();
	        	         
	        sb.append("\""+ fn + "\""+ sep);
	        sb.append("\""+ klient+ "\"" + sep);
	        sb.append("\""+ getDateString(Calendar.getInstance())+ "\"" + sep);
	        sb.append("\""+toode+"\"" + sep);
	        sb.append("\""+(int)(ol.getKogus())+"\"" + sep);
	     
	       
	        
	        sb.append("\"UNIT\"" + sep);
	        sb.append("\""+getNF().format(ol.getPrice())+"\"" + sep);
	        if(ol.getDiscount() == 100 || ol.getDiscount() == - 2 ){
	        	sb.append("\"100.00\"" + sep);
		    }else{
		    	
		    	if(ol.getDiscount() > 0 && aletooted.get(ol.getToode()) != null){
		    		sb.append(getNF().format(ol.getDiscount()) + sep);
				}else{
		    		sb.append("\"0.00\"" + sep);
				}
		    	
		    }
	        sb.append("\"C\"" + sep);
	        sb.append("\""+ getDateString( cal)+ "\"" + sep);
	        sb.append("\"\"" + sep);
	        
	        
	        sb.append("\"" + order.getAgent() +"\"" + sep);
	        
	        
	       /* sb.append("\""+disc+"\"" + sep);
	        sb.append("\""+"\"" + sep); //tyhi
	        sb.append("\""+ getDateString( order.getShipCal())+ "\"" + sep);
	        sb.append("\""+fn+"\"" + sep);
	        sb.append("\""+ getDateString( order.getShipCal())+ "\"" + sep);
		    sb.append("\""+"\"" + sep);
	        sb.append("\""+"\"" + sep);
	     */
	          sb.append(lineSep);
	 
	       counter++;
	      }
	      
	      setNumAddedLines(counter); 
	      

   	   fileName =   "o-" + order.getAgent() +"."+ order.getCompany()+"." +fn  + ".csv";
	      
      String fname = (parent.getProps().getProperty("conv.dir")) + fileName;
  
			      FileOutputStream fos = new FileOutputStream(fname);
			        fos.write(sb.toString().getBytes());
			        fos.close();
	    		
	    	

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
	  
 
	public NumberFormat getNF() {
		return this.nf;
	}
 
  public String getDriverVersion(){
    return "NaviOrkla";
  }

  public static final String addSpaces(String s, int totalLen){
    int len = s.length();
    StringBuffer sb = new StringBuffer(s);
    for (int i=len; i < totalLen ; i++){
      sb.append(' ');
    }
    if( len > totalLen){
    	return s.substring(0,totalLen);
    }
    return sb.toString();
  }
  
  public String getImportFile(){
    return parent.getProps().getProperty("imp.read");
  }
  
  private NumberFormat calNF = NumberFormat.getInstance();
  private String url = "jdbc:odbc:navi";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}
