
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import eevaeksport.imp.ImportPanel;
import eevaeksport.imp.Order;
import eevaeksport.imp.OrderLine;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class AxaOrklaLVIMImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public AxaOrklaLVIMImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
   if( parent.getProps().getProperty("dimension") != null ){
	   dimension = parent.getProps().getProperty("dimension");
   }
   
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
  }


  private String dimension = "olv";
  
  public String identifyCustomer(String id,String regnr) throws SQLException {

	    String acc = null;
	    if(regnr != null && regnr.trim().length()>0){

	    String sql2 = " SELECT accountnum  AS kood FROM bmssa.custtable where dataareaid='" + dimension + "'"
	    		+ " and  (REGNUM_W)= '" + regnr + "' AND ouraccountnum = '" + id + "'";
	    System.out.println(sql2);
	    

	Connection con3 = null;
	if(dimension.equals("olv")){
	con3 = 	getNewConnection();	   	
	}else{
		con3 = 	getNewConnection2();	
	}
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

	        Connection con3 =  null;
	    	if(dimension.equals("olv")){
	    	con3 = 	getNewConnection();	   	
	    	}else{
	    		con3 = 	getNewConnection2();	
	    	}
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


	        Connection con3 = null;
	    	if(dimension.equals("olv")){
	    	con3 = 	getNewConnection();	   	
	    	}else{
	    		con3 = 	getNewConnection2();	
	    	}
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
	    return DriverManager.getConnection(url, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
	  }

	  private Connection getNewConnection2() throws SQLException{
		    return DriverManager.getConnection(url2, parent.getProps().getProperty("dsn.user"), parent.getProps().getProperty("dsn.pass"));
		  }
	  
 
 
  
 
	  public String identifyProduct(String ean, String prodID) throws SQLException {
		  
		    String sql1 = "SELECT inv.itemid AS toode ,bar.itembarcode FROM bmssa.inventtable inv left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid"
		    		+ "  left join bmssa.inventitembarcode bar ON bar.dataareaid = inv.dataareaid "
		    		+ "and bar.itemid=inv.itemid and bar.useforprinting = 1  where "
		    		+ "blocked = 0 and moduletype = 2 and  mod.dataareaid = '" + dimension + "' and "
		    				+ "inv.dataareaid='" + dimension + "' " +
		    		" AND bar.itembarcode='" + ean + "' order by inv.itemid " ;


		    String sql2 = "SELECT inv.itemid AS toode FROM bmssa.inventtable inv left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid"
		    		+ " where blocked = 0  and moduletype = 2 and  inv.dataareaid = '" + dimension + "' and "
		    				+ "mod.dataareaid = '" + dimension + "' and inv.itemid='" + prodID + "' ";

		    String sql3 = "SELECT inv.itemid AS toode FROM bmssa.inventtable inv left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid"
		    		+ " where blocked = 0  and moduletype = 2 and  inv.dataareaid = '" + dimension + "' and "
		    				+ "mod.dataareaid = '" + dimension + "' and inv.itemid='" + prodID + "' ";

		    String sql4 = "SELECT inv.itemid AS toode FROM bmssa.inventtable inv left JOIN bmssa.inventtablemodule mod ON mod.itemid = inv.itemid"
		    		+ " where blocked = 0  and moduletype = 2 and  inv.dataareaid = '" + dimension + "' and "
		    				+ "mod.dataareaid = '" + dimension + "' and inv.itemidGU='" + prodID + "' ";

		    
		    
		    String prod = null;
		    ResultSet rs1 = null;

		    Connection con3 = null;
			if(dimension.equals("olv")){
			con3 = 	getNewConnection();	   	
			}else{
				con3 = 	getNewConnection2();	
			}
		    Statement  st1 = con3.createStatement();
		  if(prodID != null){
			   

			  rs1 = st1.executeQuery(sql4);
					System.out.println(sql4);
				    while (rs1.next()) {
				      prod = rs1.getString("toode").trim();
				  	System.out.println("prod1 " + prod );
				    } 
				    
				    if(prod == null || prod.trim().length() == 0){
				    	  rs1 = st1.executeQuery(sql3);
							//	System.out.println(sql2);
							    while (rs1.next()) {
							      prod = rs1.getString("toode").trim();
							  	System.out.println("prod1 " + prod );
							    } 
				    }
						
			  
			  if(prod == null || prod.trim().length() == 0){
				   rs1 = st1.executeQuery(sql2);
					//	System.out.println(sql2);
					    while (rs1.next()) {
					      prod = rs1.getString("toode").trim();
					  	System.out.println("prod11 " + prod );
					    }  
			  }
			  
			    
		  }
		 

		  

		    if(prod == null){
		      rs1 = st1.executeQuery(sql1);
		      while (rs1.next()) {
		        prod = rs1.getString("toode").trim();
		      	System.out.println("prod2 " + prod );
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

	  
	  

	  
	  
	  public double getJagaja(String klient , String prodID) throws SQLException{
		  
		  double res = 1;
		   // con-LV0746
		   // LV2219
		  
		  
		  String sql2 = "SELECT inv.StbMinInQty  AS toode FROM bmssa.inventtable inv "
		    		+ " where inv.dataareaid = '" + dimension + "' and inv.itemid='" + prodID + "' ";

	//	   System.out.println("opaa0 " + res +" " + klient + " " + prodID + "\n" + sql2);
		
if(klient.startsWith("LV2182") || klient.startsWith("LV2171") ||
		klient.startsWith("LV2173") || klient.startsWith("LV2199") ||
		klient.startsWith("LV3864") || klient.startsWith("LV5373") || 
		klient.startsWith("LV3536") ){
	
   // System.out.println("opaa1 " + res +" " + klient + " " + prodID + "\n" + sql2);


		    ResultSet rs1 = null;
		    Connection con3 = null;
			if(dimension.equals("olv")){
			    con3 = getNewConnection();	   	
			}else{
				con3 = getNewConnection2();	
			}
		    Statement  st1 = con3.createStatement();
		 
		    if(prodID != null){
			      rs1 = st1.executeQuery(sql2);
			    if(rs1.next()) {
			      res = rs1.getDouble("toode");
		//	      System.out.println("opaa2 " + res +" " + klient + " " + prodID + "\n" + sql2);
			     } 
		      }
		   }	


// teeme Kalevi juhtumi ka siis eraldi 

if(klient.startsWith("EE4176")){
	
   // System.out.println("opaa1 " + res +" " + klient + " " + prodID + "\n" + sql2);
	  sql2 = "SELECT inv.StbMinInQty  AS toode FROM bmssa.inventtable inv "
	    		+ " where inv.dataareaid = '" + dimension + "' and inv.itemid='" + prodID + "' and STBWEIGHTITEM = 1    ";

		    ResultSet rs1 = null;
		    Connection con3 = null;
			if(dimension.equals("olv")){
			    con3 = getNewConnection();	   	
			}else{
				con3 = getNewConnection2();	
			}
		    Statement  st1 = con3.createStatement();
		 
		    if(prodID != null){
			      rs1 = st1.executeQuery(sql2);
			    if(rs1.next()) {
			      res = rs1.getDouble("toode");
		//	      System.out.println("opaa2 " + res +" " + klient + " " + prodID + "\n" + sql2);
			     } 
		      }
		   }	
          
           System.out.println("jagaja " + res + " " + prodID);
		  
            return res;
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

  
  public void makeOrder(Order order) throws SQLException{
	  if(order.getCompany() != null && order.getCompany().length() > 3){
	  counter = 0;
	  	    addlocks(false);
	  	    	makeOutput(order); 
	 		  String lineSep = System.getProperty("line.separator");
  		  	    writeConsloStr(parent.getProps().getProperty("conv.dir") + "index.csv",lineSep + fileName + ",0,,," + lineSep);
       		    addlocks(true);
          setNumAddedLines(counter);
      	
	    }
  }

  
  private String fileName = null;
  
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
	  
  
  
  
  private void makeOutput(Order order){
	 	StringBuffer sb = new StringBuffer();
	 	
	 	try {
	 	    
		    String doknr = order.getDokNr();
		      String fn = doknr.replace('/', '-');
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
	       System.out.println("Status " + order.getStatus());
	         
	    Vector lines = order.getOrderLines();
	      for (int i = 0; i < lines.size(); i++) {
	    	  
	        OrderLine ol = (OrderLine) lines.elementAt(i);
	 //    _E20587,"LV2174-003","04.02.2016","P120504511","120.0000000000","0.7634000000","10","","05.02.2016","4539006126","","0"
	//     _E20587,"LV2174-003","04.02.2016","N200111100","1440.0000000000","0.2720000000","0","","05.02.2016","4539006126","",""
      
	        String toode = ol.getToode();
	         if(toode.startsWith("con-")){
	        	 toode = toode.substring(4);
	         }
	         
	         System.out.println("unit  " + ol.getUnit() + " " + klient);
	         
	         double jagaja = 1; 
	         
	        	/* if(false && klient.startsWith("LV2182")){
	        		 
	        		 if(ol.getUnit() != null){
	        			 if(ol.getUnit().equals("KG")){
	        				 jagaja = this.getJagaja(klient, toode); 
	        		     }
		        	 }else{
	        			 jagaja = this.getJagaja(klient, toode);  
	        		 }
	        		 
	        	 }else{*/
	        		 jagaja = this.getJagaja(klient, toode);
	        	// }
	        	 
	        	 System.out.println("unit  " + ol.getUnit() + " " + jagaja);
	        	
	         
	        
	         
	        sb.append("MSE-"+ fn + sep);
	        sb.append("\""+ klient+ "\"" + sep);
	        sb.append("\""+ getDateString(Calendar.getInstance())+ "\"" + sep);
	        sb.append("\""+toode+"\"" + sep);
	        sb.append("\""+(int)(ol.getKogus()/jagaja+ 0.49)+"\"" + sep);
	        
	        String disc = "0";
	        if(ol.getDiscount()> 0){
	        	disc = ol.getDiscount()+"";
	        }
	        
	        sb.append("\""+getNF().format(ol.getPrice())+"\"" + sep);
	        sb.append("\""+disc+"\"" + sep);
	        sb.append("\""+"\"" + sep); //tyhi
	        sb.append("\""+ getDateString( order.getShipCal())+ "\"" + sep);
	        sb.append("\""+fn+"\"" + sep);
	        sb.append("\""+ getDateString( order.getShipCal())+ "\"" + sep);
		    sb.append("\""+"\"" + sep);
	        sb.append("\""+"\"" + sep);
	     
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
    return "AxaOrklaLVIM";
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
  private String url = "jdbc:odbc:orkla";
  private String url2 = "jdbc:odbc:kondit";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private int counter = 0;
  private ImportPanel parent;

public String getTrouble() {
	return null;
}
}
