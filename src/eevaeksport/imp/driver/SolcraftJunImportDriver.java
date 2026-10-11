/*
 * Created on 9.11.2007

 */
package eevaeksport.imp.driver;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */


    import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
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

	public class SolcraftJunImportDriver
	    extends AbstractImportDriver {

	  static Category log = Category.getInstance(IceImportDriver.class.getName());

	  public SolcraftJunImportDriver(ImportPanel parent) {
	    this.parent = parent;
	    PropertyConfigurator.configure("log4j.properties");
	   
	    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	    try {
	      Class.forName(databaseDriverName).newInstance();
	      con = DriverManager.getConnection(url,"SYSDBA","master" );
	      try{
	    	//  conlog = DriverManager.getConnection(urllog,"SYSDBA","master" );  
	      }catch(Exception rr){
	    	 
	      }
	      
	      
	      if(parent.getProps().getProperty("grupp") != null){
	    	  	firma = parent.getProps().getProperty("grupp") ;
	    	  }
	        if(parent.getProps().getProperty("grupp2") != null){
	    	  	firma2 = parent.getProps().getProperty("grupp2") ;
	    	  }
	        if(parent.getProps().getProperty("grupp3") != null){
	    	  	firma3 = parent.getProps().getProperty("grupp3") ;
	    	  }
	        if(parent.getProps().getProperty("grupp4") != null){
	    	  	firma4 = parent.getProps().getProperty("grupp4") ;
	    	  }
	        if(parent.getProps().getProperty("grupp5") != null){
	    	  	firma5 = parent.getProps().getProperty("grupp5") ;
	    	  }
	        if(parent.getProps().getProperty("grupp6") != null){
	    	  	firma6 = parent.getProps().getProperty("grupp6") ;
	    	  }
	        if(parent.getProps().getProperty("grupp7") != null){
	    	  	firma7 = parent.getProps().getProperty("grupp7") ;
	    	  }
	        
	        if(parent.getProps().getProperty("grupp8") != null){
	    	  	firma8 = parent.getProps().getProperty("grupp8") ;
	    	  }
	        if(parent.getProps().getProperty("grupp9") != null){
	    	  	firma9 = parent.getProps().getProperty("grupp9") ;
	    	  }
	 
	        calNF.setMaximumFractionDigits(0);
	      calNF.setMaximumIntegerDigits(2);
	      calNF.setMinimumIntegerDigits(2);
	      calNF.setGroupingUsed(false);
	      nf2.setMaximumFractionDigits(2);
	      nf.setMaximumFractionDigits(4);
	      nf.setGroupingUsed(false);
	      nf2.setGroupingUsed(false);
	      logMap = getLogcodes();
	      logprods = getLogProds();
	      agents = getAgents();
	      
	      
	    }
	    catch (Exception e) {
	      parent.showError(e);
	    }
	  }

	  public HashMap getLogcodes(){

		    String sql1 = "SELECT a.pkonts AS klient,JGT_VS_LOGSOL as logsol FROM adreses a  WHERE  (a.tips!='A' " +
           "  and a.nosauk not starting '#'  and a.nosauk not starting '*'  and JGT_VS_LOGSOL  is not null) or a.pkonts = '0100000' ORDER BY a.nosauk";
		  log.info("1_LINE_SQL: " + sql1);

		  
		    HashMap grupp = new HashMap();

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	String id = rs1.getString("klient");
		     	String log = rs1.getString("logsol");
		    	if(log != null ){
		    		grupp.put(id, log);
		    	}
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	   // System.out.println("Logsoli Koodid k�es ;" + grupp );
			grupp.put("0110000000", "0100000");
			return grupp;
		  }

	  public HashMap getAgents(){

		    String sql1 = "SELECT a.pkonts AS klient,agents as agent FROM adreses a  WHERE  (a.tips!='A' " +
         "  and a.nosauk not starting '#'  and a.nosauk not starting '*'  and agents  is not null and agents != '') ";
		  log.info("1_LINE_SQL: " + sql1);

		  
		    HashMap grupp = new HashMap();

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	String id = rs1.getString("klient");
		     	String log = rs1.getString("agent");
		    	if(log != null ){
		    		grupp.put(id.trim(), log.trim());
		    	}
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    System.out.println("Jungenti agendid k�es ;" + grupp.size() );
		    return grupp;
		  }

	
	  public String getName(String code){

		    String sql1 = "SELECT a.nosauk  as klient FROM adreses a  WHERE a.pkonts= '" + code + "'  ORDER BY a.nosauk";
		  log.info("1_LINE_SQL: " + sql1);

		  
		    String  grupp = " ";

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	grupp = rs1.getString("klient");
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	   // System.out.println("Logsoli Koodid k�es ;" + grupp );
		    return grupp;
		  }

	  
	  public HashMap getLogProds(){
		  	


		    String sql1 = "  SELECT pr.artikuls AS toode FROM preces  pr INNER JOIN grupas g ON g.grupa=pr.grupa " +
              " where pr.act='+' and pr.nosauk2 >'' and (pr.artikuls  starting '" + firma3 + "' or pr.artikuls  starting '" + firma4 + "' " +
              		"or pr.artikuls  starting '" + firma5 + "' or pr.artikuls  starting '" + firma + "' or pr.artikuls " +
              		"  starting '" + firma6 + "' or pr.artikuls  starting '" + firma7 + "' or pr.artikuls  starting '" + firma8 + "' or pr.artikuls  starting '" + firma9 + "' or pr.artikuls " +
              		" starting '" + firma2 + "')" ;
  			
		  log.info("1_LINE_SQL: " + sql1);

		    HashMap grupp = new HashMap();
/*
		    Statement st1;
			try {
				st1 = conlog.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	String id = rs1.getString("toode");	    
		    		grupp.put(id, id);
		    }

		    rs1.close();
		    st1.close();
			} catch (Exception e) {
				//e.printStackTrace();
			}
	  //  System.out.println("Logsoli Tooted k�es ;" + grupp );*/
		    return grupp;
		  }

	  
	  
	  public String identfyGroup(String prodID){
		  	


		    String sql1 = " Select SC01037 AS grupp FROM SC010100 pr  where pr.SC01001='" + prodID + "'";
		  log.info("1_LINE_SQL: " + sql1);

		    String grupp = "MUU";

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	grupp = rs1.getString("grupp");
		    	if(grupp == null || grupp.trim().length() == 0){
		    		grupp = "MUU";
		    	}
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    System.out.println("Grupp k�es 2 ;" + grupp );
		    return grupp;
		  }
	  
	
		
	
	  
	 /* private Vector splitOrderByAm (Order order, int num){
		  Vector result = new Vector();
	   
		  Order ord01 = new Order(order.getCompany(),order.getAgentFirstName());
	     	System.out.println("order " + order.getDokNr());
		  String ag = order.getAgent();
		  String comm = order.getInfo();
		  
		  ord01.setAgentCode(ag);
		  ord01.setDealer(ag);
		  ord01.setDokNr(order.getDokNr());	 
		  ord01.setInfo(comm);
		  ord01.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ord01.setStatus(order.getStatus());
		  ord01.setPaymTerm(order.getPaymTerm());
		  ord01.setCompName(order.getCompany());
		  ord01.setCurrency(order.getCurrency());
		  ord01.setChanged(order.isChanged()); // s
		  
 
		  Vector lines = order.getOrderLines();
		  Enumeration enum = lines.elements();
		  while  (enum.hasMoreElements()){
			  OrderLine lin = (OrderLine)enum.nextElement();
		
			     String itemid = lin.getToode();			     
			    boolean jung = true;
			   
			    
			    System.out.println("Splitter " +itemid + " " + jung );
			    
			    
			     if(jung){   
			     	  ord01.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN());
			     }
			     }





		  if(ord01.getOrderLines() != null && ord01.getOrderLines().size()>0){
			  result.add(ord01);  
		  }

		
		
		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }
*/
	  
	private boolean isNoSpilt(String company){
		boolean res = false;
	    String sql1 = "SELECT PVN_KODS as regnr FROM adreses a  WHERE a.pkonts= '" + company + "' ";
		  String asi = parent.getProps().getProperty("nospliting");
		  log.info("1_LINE_SQL: " + sql1);
    Vector vec = new Vector();
    if(asi != null){
        StringTokenizer st = new StringTokenizer (asi,",");
    	  while(st.hasMoreTokens()){
    		  vec.addElement(st.nextElement());	  
    	  }	
    }
   
		    Statement st1;
			try {
				st1 = con.createStatement();
		    ResultSet rs1 = st1.executeQuery(sql1);
		    while (rs1.next()) {
		    	String regnr = rs1.getString("regnr");
		    	if(regnr != null && vec.contains(regnr.trim())){
			    	res = true;	
		    	}
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    System.out.println("Splitter t��le " + res );
		return res;
	}
	  
	  private Vector splitOrder (Order order){
		  Vector result = new Vector();
	   if(isNoSpilt(order.getCompany())){
		   String ag = order.getAgent();
		   if(ag.indexOf("HOUSE") != -1 && agents.get(order.getCompany()) != null){
				 ag = (String)agents.get(order.getCompany()); 
			  }
		   order.setAgentCode(ag);
		   order.setWarehouse(order.getAgent());
		   result.add(order);
	   }else{
		   Order ordJUN = new Order(order.getCompany(),order.getAgentFirstName());
	     	System.out.println("order " + order.getDokNr());
		  String logcode = null;
		  String ag = order.getAgent();
		  String agorig = order.getAgent();
		  if(ag.indexOf("HOUSE") != -1 && agents.get(order.getCompany()) != null){
			 ag = (String)agents.get(order.getCompany()); 
		  }
		  if(logMap.get(order.getCompany()) != null ){
			  logcode = (String)logMap.get(order.getCompany());
		  }
		  String add = "";
		  if(logcode == null){
			  add = getName(order.getCompany());
			  logcode = "0110000000";
		  }
		  Order ordLOG = new Order(logcode,order.getAgentFirstName());
		     
		  ordJUN.setAgentCode(ag);
		  ordJUN.setDealer(ag);
		  ordJUN.setDokNr(order.getDokNr());
		  String comm = order.getInfo();
		  if(comm != null){
			  if(comm.indexOf("JG:") != -1 && comm.indexOf("LS:") != -1 ){
				  comm = comm.substring(comm.indexOf("JG:")+3,comm.indexOf("LS:") ).trim();
			  }else if(comm.indexOf("JG:") != -1 && comm.indexOf("LS:") == -1){
				  comm = comm.substring(comm.indexOf("JG:")+3).trim();
			  }  
		  }
		
		  ordJUN.setInfo(comm);
		  ordJUN.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordJUN.setStatus(order.getStatus());
		  ordJUN.setPaymTerm(order.getPaymTerm());
		  ordJUN.setWarehouse(agorig);
		
		  
		  ordLOG.setAgentCode(ag);
		  ordLOG.setDealer(ag);
		  ordLOG.setDokNr(order.getDokNr());
		   comm = order.getInfo();
		   if(comm != null){
			   if(comm.indexOf("LS:") != -1 ){
					  comm = comm.substring(comm.indexOf("LS:") +3 ).trim();
				  }else if(comm.indexOf("JG:") != -1 && comm.indexOf("LS:") == -1){
					  comm = "";
				  }else if(comm.indexOf("JG:") == -1 && comm.indexOf("LS:") == -1){
					  comm = comm.trim();
				  }   
		   }
		
		  ordLOG.setInfo(comm);
		  
		  ordLOG.setPaymTerm(order.getPaymTerm());
		  ordLOG.setShipCal(order.getShipCal().getTimeInMillis() +"");
		  ordLOG.setStatus(order.getStatus());
		  ordLOG.setCompName(order.getCompany());
		  ordLOG.setCurrency(add);
		  ordLOG.setWarehouse(agorig);
		  ordLOG.setChanged(true); // selleks et teise kataloogi kirjutada 
		 
		  Vector lines = order.getOrderLines();
		  Enumeration enumr = lines.elements();
		  while  (enumr.hasMoreElements()){
			  OrderLine lin = (OrderLine)enumr.nextElement();
		
			     String itemid = lin.getToode();			     
			    boolean jung = true;
			    if(logprods.get(itemid) != null){
			    	jung = false;
			    }
			    
			    System.out.println("Splitter " +itemid + " " + jung );
			    
			    
			     if(jung){   
			     	  ordJUN.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
			     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
			     	 
			     }else{
			    	 ordLOG.add(lin.getToode(), lin.getKogus(), lin.getBonus(), lin.getDiscount(), lin.getPl()+"", lin.getPrice(),
				     			lin.getTooteNimi(), lin.getUnit(), lin.getPartii(),lin.getEAN(),null,null,null);
				     }
                  }

		  if(ordLOG.getOrderLines() != null && ordLOG.getOrderLines().size()>0){
			  result.add(ordLOG);  
		  }

		  if(ordJUN.getOrderLines() != null && ordJUN.getOrderLines().size()>0){
			  result.add(ordJUN);  
		  }
		
  
	   }
		 		 System.out.println("Result =" +order.getDokNr()
				 + " "+ result.size());
		 //System.exit(0);
		  return result ;
	  }


	
	  public String identifyCustomer(String id,String regnr) throws SQLException {
	  	String sql1 = null;
	  	
	  	 System.out.println("Kliendi M��ramine: " );
	  	
	  	    sql1 = " SELECT SL01001 AS kood FROM SL010100 where   SL01129='" + id + "'";	 //????
		
	  	    String  sql2 = " SELECT SL01001 AS kood FROM SL010100 where   SL01001='" + id + "'";
	  	    
	    System.out.println(sql1);
	    String acc = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	      acc = rs1.getString("kood").trim() ;

	    }
	    
	    if(acc == null || acc.trim().length()== 0){
	    	rs1 = st1.executeQuery(sql2);
	    	if(rs1.next()){
	    		acc = rs1.getString("kood").trim();
	    	}
	    }
	    
	    rs1.close();
	    st1.close();

	    System.out.println("Klient: " + acc + " " + id);
	    return acc;

	  }


	  
	  

	 
	  public String identifyProduct(String ean, String prodID) throws SQLException{
	  	
		    String sql2 = "	  SELECT (SC01001) AS toode ,SC03003 FROM SC010100 LEFT JOIN SC030100 on SC03001=SC01001 and SC03002='01'  WHERE RTRIM(SC01132) like   '%" + ean.trim() + "%'" +
 	"	 and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U')  order by SC01016,SC03003 desc " ;
 
	    String sql1 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001) = '" + prodID.trim() + "'" +
		 "  and  LEN((SC01128))>0 and rtrim(ltrim(SC01016)) not in  ('S','U') ";
	    
	   
	    String prod = null;

	    Statement st1 = con.createStatement();
	    ResultSet rs1 = st1.executeQuery(sql1);

	    if (rs1.next()) {
	      prod = rs1.getString("toode").trim(); 
	    }
	   System.out.println(ean + " prod0 " + prod);
	    if(prod == null && ean.trim().length() > 7){
	      rs1 = st1.executeQuery(sql2);
	      if (rs1.next()) {
	        prod = rs1.getString("toode").trim();
	      }
	      
	    }

	 
	    System.out.println(prodID + " prod1 " + prod);
	    rs1.close();
	    st1.close();

	    return prod;
	  }

			

	  public void makeOrder(Order order) throws SQLException{
		  
		  Vector splitted =  splitOrder(order);

		  Enumeration en = splitted.elements();
		  while(en.hasMoreElements()){
			  Order ord = (Order)en.nextElement();
				  setOrder(ord);
				  makeOutput(ord);
		  }
		  
	  }

	  
	  private void makeOutput(Order order){
		 	StringBuffer sb = new StringBuffer();
		 	  String separator  = ";";
		 	try {
		 	  int cou = 0;
		
				String agent = order.getAgent();
				if(order.getDealer() != null && order.getDealer().trim().length() != 0){
					agent = order.getDealer();
				}
				
			    Calendar cal = Calendar.getInstance();
			      cal.setTime(order.getShipCal().getTime());
              	 String doknr = order.getDokNr();
              	 
              	String comment = order.getInfo();
		          if(comment == null || comment.trim().length() == 0 ){
		         	comment = " ";
		          }
		       
                 if(order.isChanged()){
		        	  comment = "|" + order.getCompName()  + "|" + order.getCurrency() + " "+ comment;
		        	  agent = "FA" + agent;
		          }

		          if(100 < comment.length() ){
		        	  comment = comment.substring(0,100);
		          }
         
		    Vector lines = order.getOrderLines();
		      for (int i = 0; i < lines.size(); i++) {
		      	OrderLine line = (OrderLine)lines.elementAt(i);
		      
		      	
		        
		             double disc = line.getDiscount();
		             
		             sb.append( order.getCompany() + separator
		                      + line.getToode() + separator
		                      + line.getKogus() );

		        	  sb.append(separator + "0");//DOKNR
		        	  sb.append(separator + " ");//LADU
		        	  sb.append(separator + "0");//NULL
		        	  sb.append(separator + disc);//ale
		              sb.append(separator + "" + cal.getTime().getTime());//stamp
		  	          sb.append(separator + "0");// pl
		  	          sb.append(separator + "0");// price
		  	          sb.append(separator + "0");// makset�htaeg
		              sb.append(separator + agent);// agent
		              sb.append(separator + "0");// offer/order
		              sb.append(separator + " ");//unit
		              sb.append(separator + getDate(cal)); //p�ev 
		              sb.append(separator + comment); //kommetaar
		              sb.append(separator + " ");//action kood
		              sb.append(separator   + " ");// action hind 
		              sb.append(separator   + order.getDokNr());// tellimuse number
		              sb.append(separator   + " ");//shipmode 
		              if(i==0){
		            	  sb.append(separator + order.getWarehouse());//lisame originaal tellimuse tegija
		              }
		              sb.append(System.getProperty("line.separator"));		        
		       cou++;
		      }
		 	
              nf.setMaximumFractionDigits(3);
              nf.setGroupingUsed(false);
                
		      setNumAddedLines(getNumAddedLines() + cou);
		      String add = agent;
		 
	            String fileName = escape(parent.getProps().getProperty("conv.dir1") + add)  + "." + doknr   +  ".txt";
	   	     if(order.isChanged()){
	   	    	 fileName = escape(parent.getProps().getProperty("conv.dir2") + add)  + "." + doknr   +  ".txt";
	   	     }
	   	      System.out.println(fileName);
	   	        FileOutputStream fos = new FileOutputStream(fileName);
	   	        fos.write(sb.toString().getBytes());
	   	        fos.close();
	          
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
		  

			  
	  public String getDriverVersion(){
	    return "SolcraftJunImport";
	  }

	  private void setOrder(Order order){

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

	  
	  private String getDate(Calendar cal){
		  StringBuffer sb = new StringBuffer();
		  String year =   cal.get(Calendar.YEAR) + "";
		  String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		  String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		   if (day.length() == 1){
		    sb.append("0");
		  }
		   sb.append(day);
		   sb.append(".");
		  if (month.length() == 1){
		   sb.append("0");
		 }
		   sb.append(month );
		  sb.append( "." + year);
		  return sb.toString();
		}
	

	  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }
	  
	  private NumberFormat calNF = NumberFormat.getInstance();


	  private String url = "jdbc:odbc:materiali";
	//  private String urllog = "jdbc:odbc:logsol";
	  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  private NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
	  private Connection con;
	  private HashMap logMap = null;
	  private HashMap logprods = null;
	  private HashMap agents = null;
	  private Connection conlog;
	  private ImportPanel parent;
	  private String firma;
	  private String firma2;
	  private String firma3;
	  private String firma4;
	  private String firma5;
	  private String firma6;
	  private String firma7;
      private String firma8;
      private String firma9;
      
	public String getTrouble() {
		// TODO Auto-generated method stub
		return null;
	}
	}
