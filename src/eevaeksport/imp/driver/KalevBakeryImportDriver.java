
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
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

public class KalevBakeryImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public KalevBakeryImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "mobilejava","mobilejava");
   
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String identfySorting (String prodID){
	  	


	    String sql1 = " Select SC01014 AS sort FROM SC010300 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String sort = "000";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	sort = rs1.getString("sort");
	    //	System.out.println("sort k�es 1 ;" + sort );
	    	if(sort == null || sort.trim().length() == 0){
	    		sort = "xxx";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
  // System.out.println("Sort k�es 2 ;" + sort );
	    return sort;
	  }
  public boolean makeRounding (String prodID){
	  	


	    String sql1 = " Select SC01097 AS sort FROM SC010300 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    boolean sort = true;

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	if( rs1.getString("sort").trim().equals("1")){//kui seal on 0 -st erinev v��rtus
	    		//sort = false; // kommentisin v�lja sest peaks t��tama ilma selleta kah
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// System.out.println("Sort k�es 2 ;" + sort );
	    return sort;
	  }


  
  
  public String identfyWare(String prodID){
//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5300003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


	    String sql1 = " Select SC01094 AS ladu FROM SC010300 pr  where pr.SC01001='" + prodID + "'";
	  log.info("1_LINE_SQL: " + sql1);

	    String ladu = "23";

	    Statement st1;
		try {
			st1 = con.createStatement();
		
	    ResultSet rs1 = st1.executeQuery(sql1);

	    while (rs1.next()) {
	    	ladu = rs1.getString("ladu");
	    //	System.out.println("Ladu k�es 1 ;" + ladu );
	    	if(ladu == null || ladu.trim().length() == 0){
	    		ladu = "23";
	    	}
	    }

	    rs1.close();
	    st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
   //  System.out.println("Ladu k�es 2 ;" + ladu );
	    return ladu;
	  }

  
  public String identfyRing(String id){
	//M14544    M14544    M14544    MAXJ10701 45283067952010010720100108                                                                                                                                                                                                                  54                                                                       34013                              Linda kirsitasku 3x55g                            6           1            5300003033J1                                                     20100108                                                                                                                                                                                                                                                                                                            


		    String sql1 = "SELECT SL01027 AS kood FROM SL010300 where SL01001  =  '" + id + "'";	
		  log.info("1_LINE_SQL: " + sql1);
		    String ring = "";

		    Statement st1;
			try {
				st1 = con.createStatement();
			
		    ResultSet rs1 = st1.executeQuery(sql1);

		    while (rs1.next()) {
		    	ring = rs1.getString("kood").trim();
		  
		    }

		    rs1.close();
		    st1.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	     System.out.println("Ring k�es  ;" + ring );
		    return ring;
		  }

 
  
  

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010300 where SL01125  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;
    }
    rs1.close();
    st1.close();
if(acc == null){
  sql1 = "SELECT SL01001 AS kood FROM SL010300 where SL01001  =  '" + id + "'";	
    System.out.println(sql1);
     st1 = con.createStatement();
     rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;
    }
    
    rs1.close();
    st1.close();   
	
}    
    
    System.out.println("Klient: " + acc + " " + id);
    return acc;

  }

  public String identifyCustomerADDR(String id) throws SQLException {
  	 String	sql1 = "SELECT N33_ADRESAS AS aadress FROM n08_klij  left JOIN N33_KBAN ON N33_KODAS_KS = N08_KODAS_KS WHERE" +
  	 		"  trim(N08_KODAS_KS)||'-'|| N33_EIL_NR = '" + id +"'"; 
  	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("aadress").trim();

    }
    rs1.close();
    st1.close();

    return acc;

  }
  
  public String identifyCustomerAGENT(String id) throws SQLException {
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010300 WHERE SL01001='" + id +"'"; 
 	
  // System.out.println(sql1);
   String agent = null;

   Statement st1 = con.createStatement();
   ResultSet rs1 = st1.executeQuery(sql1);

   while (rs1.next()) {
   	agent = rs1.getString("agent").trim();

   }
   rs1.close();
   st1.close();

   return agent;

 }
 
  
  public String getPandiPakend(String toode) throws SQLException{
 	 
    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010300  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
    String prod = null;
    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);
    while (rs1.next()) {
      prod = rs1.getString("pant").trim();
    }
       System.out.println(toode + " prod1 " + prod);
    rs1.close();
    st1.close();
    return prod;
 }
 
 
  public String identifyProduct(String ean, String prodID) throws SQLException{
  	
	    String prod = null;
	
	    if(prodID == null ){
	    	prodID = "XXXXX";
	    }
	   
	  
	  
    String sql2 = " SELECT RTRIM(SC01001) AS toode FROM SC010300  WHERE (RTRIM(SC01132)=   '" + ean.trim() 
                              + "' or RTRIM(SC01003) = '" + ean.trim() + "') ";

    String sql1 = "SELECT RTRIM(SC01001) AS toode FROM SC010300  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "'";



    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }

   
    if(prod == null){

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

  
  private long getAndUpdateNext() throws SQLException{
	    long nextRecID = 0;
	    String sql = "SELECT  nextval FROM mj_numbers WHERE serial='ord'";
	    log.info("getAndUpdateRecID(): " + sql);
	      Statement st = con.createStatement();
	      ResultSet rs = st.executeQuery(sql);
	      while(rs.next()){
	       
	        //System.out.println("number:" + rs.getString("NEXTREC"));
	        nextRecID = rs.getLong("nextval");
	        log.info("NEXTRECID = " + nextRecID);
	        sql = "UPDATE mj_numbers SET nextval =" + (nextRecID+1) +
	        " WHERE serial='ord' ";
	        log.info("getAndUpdateRecID(): " + sql);
	        Statement st2 = con.createStatement();
	        int i = st2.executeUpdate(sql);
	        st2.close();
	        log.info("UPDATED ROWS = " + i);
	      }
	      rs.close();
	      st.close();

	    return nextRecID;
	  }

  
  private double getMultiplier(String toode , int status){
  	double res = 1;
  	
    String sql1 = " SELECT (case when SC01015!='' then cast(SC01015 as integer) else 1 end) AS res , SC01097 as aaa FROM SC010300  WHERE RTRIM(SC01001)=   '" + toode + "'";

			try{
				 Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
			
				      while (rs1.next()) {
				        res = rs1.getDouble("res");
				        int aaa = rs1.getInt("aaa");
				        if(aaa >0 && status < 50){
				        	res = aaa;
				        }
				      }
				    
			}catch (Exception e){
				
			}
  // System.out.println("Multiplier " + res);
  // System.out.println(sql1);
  	return res;
  }

  public void makeOrder(Order order) throws SQLException{
	  
	    cou = 0;
			
			  
			  java.util.Collections.sort(order.getOrderLines(), new java.util.Comparator(){
					 

			        public boolean equals(Object obj) {
			          return false;
			        }

			     
			        public int compare(Object o1, Object o2) {
			          OrderLine ol1 = (OrderLine)o1;
			          OrderLine ol2 = (OrderLine)o2;
			          return ol1.getTooteNimi().compareTo(ol2.getTooteNimi());
			        }
			      });
			     

              makeOutput(order);  
		 
  //  addListings(order);
  }

  private String getNimi(String aa) throws SQLException{
  	String res = null;
 	 String	 sql2 = "SELECT RTRIM(SC01002) AS toode FROM SC010300  WHERE RTRIM(SC01001)=  '" + aa + "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
			   res = rs1.getString("toode").trim();
			 }
			
	rs1.close();
	st1.close();
  	return res ;
  }
  
  
  private String getAgentSym(String agent) {
	  String res = agent;
	  try{
		  String	 sql2 = "SELECT SY24003 AS nimi  FROM SY240300 WHERE SY24001='BK' and (SY24002)=  '" + agent+ "'";
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql2);
			
			 if (rs1.next()) {
				String aa = rs1.getString("nimi").trim();
			 
				   res = aa.substring(0,1) +(aa.substring(aa.indexOf(' '),aa.indexOf(' ')+2)).trim() ;
			   
			 }
			 

				rs1.close();
				st1.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }
		
			
	
	  return res;
  }
  
 
  private void addDBLine(String cust,String doknum,String agent,Order order,String toode,String comment,double kogus,String ring) throws SQLException{
	  
	  String sss = "SELECT order_number from mj_orderbuffer_jarle where order_number = '" + doknum + "' and  product = '" + toode + "' and amount = " + kogus;
	  boolean go = true;
	  PreparedStatement st = con.prepareStatement(sss);
	   ResultSet rs = st.executeQuery();
	   if(rs.next()){
		   go = false;
	   }
	   rs.close();
      st.close();
	  
		 String	 sql = "INSERT INTO mj_orderbuffer_jarle (order_date,shipping_date,order_number,agent,customer,product,amount,comment) values (" +
		 		" getdate(),? ,'" + doknum + "','" + agent+ "','" + cust + "','" + toode + "'," + kogus + ",'" + comment + "')";
			
		     st = con.prepareStatement(sql);
		    st.setTimestamp(1, new Timestamp(order.getShipCal().getTimeInMillis()));
		    if(go){
			    st.executeUpdate();	
		    }
	        st.close();
	}
  
  
  private boolean notInt(double sisse){
  	try{
  	String res = (sisse+"").substring(0,(sisse+"").indexOf('.'));
  	int hhh = Integer.parseInt(res);
  	if(sisse-hhh>0){
  		return true;
  	}else{
  		return false;	
  	}
  	}catch (Exception e){
  		e.printStackTrace();
  	
  	}
  	return true;
  }
  
  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 nf.setGroupingUsed(false);
 	 
 	
 	 // SIIA TEE ROUNDIN statuse j�rgi ja kampaania koguse kontroll ja koodi muutmine 
 	 
 	try {
 		 String scala = getAndUpdateNext()+ "";
 	
      
 	    String cust = order.getCompany();
		String agent = this.identifyCustomerAGENT(cust);
		String agent2 = order.getAgent();
        String ring = identfyRing(cust);
		
		System.out.println("Tuvastatud agent :" + agent + " tellimusel " + agent2 );
	
		String ref = order.getReference();
		if ( ref == null ){
			ref =  " ";
		}
		String com = order.getEmailBody();
		if ( com == null ){
			com =  " ";
		}		
		String ware = order.getWarehouse();
		if ( ware == null ){
			ware = " ";
		}
		 String doknr = order.getDokNr();
        if(doknr == null){
        	doknr = ref;
        	if(doknr.indexOf("-") != -1){
        		doknr = doknr.substring(doknr.indexOf("-"));
        	}
        }
        
         String type = "1";
         
         
         String status = getAgentSym(agent)+  "-" + this.getTarneLyh(order)+ " " +
          ( Calendar.getInstance().getTimeInMillis() + "").substring(5);
         if(order.getStatus() == 98){     	 
        	 status = "MAX" + this.getTarneLyh(order) + " " ;
         }
      
         
         if(order.getCompany().indexOf("INTR") != -1 || inList(order.getCompany())){     	 
        	 status = "SYS." +  this.getTarneLyh(order) + " " +
             ( Calendar.getInstance().getTimeInMillis() + "").substring(5);
         }
         
         
         if(order.getStatus() == 96){  	 
        	 status = "AAA"  + this.getTarneLyh(order) + " " ;
         }
         
         boolean house = false;
         
         if(agent2.equals("HOUSE1")){
        	 status = "L��N " +  this.getTarneLyh(order) + " " ;
        	 house = true;
         }
         

         if(agent2.equals("HOUSE2")){
        	 status = "SVETLANA " +  this.getTarneLyh(order) + " " ;
        	 house = true;
         }
         

         if(agent2.equals("HOUSE3")){
        	 status = "JELENA " +  this.getTarneLyh(order) + " " ;
        	 house = true;
         }
         if(order.getStatus()< 50 && !house && !order.getAgent().equals(parent.getProps().getProperty("defaultagent"))){
        	 System.out.println("Agent v�eti tellimusest" + order.getAgent());
        	 agent = order.getAgent();
         }
         
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
         try{
        	
          if(toode != null && order.getStatus()>1){
         	  
         	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
         	      "  FROM SC030300 INNER JOIN SC010300 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
         	      		" or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2') and SC01014 != '' AND SC03003-SC03004 > " + line.getKogus();
         	  Statement st1 = con.createStatement();//and SC01014 != '' 
         	  ResultSet rs1 = st1.executeQuery(sql);
         	  while (rs1.next()) {
         		  boolean goes = true;
         		  String datum = rs1.getString("datum");
         		
         		  if(datum != null && datum.trim().length() > 0 && datum.compareTo(this.getTana()) < 0){
         			  goes = false;
         		  }
         		  
         		  if(goes){
             	      toode = rs1.getString("toode").trim();   
         		  }
         		  
         	    }
         	 System.out.println(sql); 
         	  rs1.close();
         	  st1.close();
           }
          
         }catch(Exception e){
        	 e.printStackTrace();
         }
          
          
         try{
         //see blokk on selleks et Esindajatelyt tulnud tellimusel kontrollida kas on kampaania tootel piisav kogus ja kui ei siis asendada kood p�hi koodiga 	
             if(toode != null  && (toode.endsWith("-EE") || toode.endsWith("-EE1") || toode.endsWith("-EE2") )&& order.getStatus() == 0){
            	  
            	  String sql = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
            	      "  FROM SC030300 INNER JOIN SC010300 ON SC03001= SC01001 WHERE RTRIM(SC03001) = '" + toode + "' and SC01014 != '' AND SC03003-SC03004 > " + line.getKogus();
            	  Statement st1 = con.createStatement();//and SC01014 != '' 
            	  ResultSet rs1 = st1.executeQuery(sql);
            	  if (rs1.next()) {
            		  /* boolean goes = true;
            		  String datum = rs1.getString("datum");
            		
            		  if(datum != null && datum.trim().length() > 0 && datum.compareTo(this.getTana()) < 0){
            			  goes = false;
            		  }
            		  if(goes){*/
                	      toode = rs1.getString("toode").trim();   
            		 // }   	       	      
            	    }else{
            	    	if(toode.endsWith("-EE")){
                	    	toode = toode.substring(0,toode.length()-3);	
            	    	}else if(toode.endsWith("-EE1")){
                	    	toode = toode.substring(0,toode.length()-4);	
            	    	}else if(toode.endsWith("-EE2")){
                	    	toode = toode.substring(0,toode.length()-4);	
            	    	}
            	    	
            	    }
            	 System.out.println(sql); 
            	  rs1.close();
            	  st1.close();
              }else {
            	  //siin vaatame kampaania alguse puhul ka p�hi tootel on PDA tunnus olemas kui ei siis kontrillime kapmaania koodi olemasolu ja laoj��ki 
            	  
            	  String sql = "  SELECT  RTRIM(SC01001) AS toode    " +
        	      "  FROM SC010300  WHERE RTRIM(SC01001) = '" + toode + "' and SC01014 != '' ";
        	  Statement st1 = con.createStatement();//and SC01014 != '' 
        	  ResultSet rs1 = st1.executeQuery(sql);
        	  boolean ok = false;
        	  if (rs1.next()) {
                 ok = true;
        	    }
        	 System.out.println(sql); 
        	  rs1.close();
        	  st1.close();
        	  
        	  if(!ok){
        		  //Kontrollime kas kampaania toode on olemas aktiivne 
        		  sql  = "  SELECT  SC01010 as datum, RTRIM(SC03001) AS toode    " +
         	      "  FROM SC030300 INNER JOIN SC010300 ON SC03001= SC01001 WHERE (RTRIM(SC03001) = '" + toode + "-EE' " +
   	      		" or RTRIM(SC03001) = '" + toode + "-EE1' or RTRIM(SC03001) = '" + toode + "-EE2') and SC01014 != '' AND SC03003-SC03004 > " + line.getKogus()+
   	      		 " order by SC03001 desc";
   	 
        	  st1 = con.createStatement();//and SC01014 != '' 
               rs1 = st1.executeQuery(sql);
        	
        	  if (rs1.next()) {
                  toode = rs1.getString("toode").trim();
        	    }
        	 System.out.println(sql); 
        	  rs1.close();
        	  st1.close();
        		  
        		  
        		  
        	  }
            	  
              }
             
            }catch(Exception e){
           	 e.printStackTrace();
            }
         
     
          double kogus = line.getKogus();         
          
          double rounder = getMultiplier(toode,order.getStatus());
          if(order.getStatus() == 0){
        	  rounder = 1;
          }

             if(ware.equals("23") && makeRounding(line.getToode()) && !notInt(kogus) && notInt(kogus/rounder)){
            	 // kui kogus on t�isarv ja m��dud kogus pole kastisuhtes t�isarv siis ymmardame ylesse
           	 // kogus = makeRoundUp(kogus,rounder);
             } 
          

            nf.setMaximumFractionDigits(2);
 
          if (i == 0 ){
          	if( line.getKogus() < 0){
          	//	System.out.println("kogus " + line.getKogus());
             // 	doknr = getDoknr("OR05");
              	type = "8";
          	}         	
          }

          String nimi = getNimi(toode);
     if(nimi == null){
    	 nimi = line.getTooteNimi();
     }
        
     String comment = order.getInfo();
     
     
     if(comment == null || comment.trim().length() == 0 ){
    	comment = "";
     }else{
    	if(comment.trim().length()>70){
    		comment = comment.substring(0,70);	
    	}
    	}
     if(order.isChanged()){
    	 comment = "Muudetud!!!" + comment;
    	 if(comment.trim().length()>70){
     		comment = comment.substring(0,70);	
     	}
     }

	    System.out.println("yy " + order.getShipCal().get(Calendar.DATE));
     sb.append(addSpaces(cust, 10));//
     sb.append(addSpaces(cust, 10));//11      
     sb.append(addSpaces(cust, 10));//21
     sb.append(addSpaces(status + doknr, 20));//51
     sb.append(getTana());//59
     sb.append(getTarne(order));//68
     sb.append(addSpaces("",210)); //lisan tyhikuid hunniku 
     sb.append(addSpaces(agent, 3));//280
     sb.append(addSpaces(comment,70)); //lisan tyhikuid hunniku 350 kommentaar koht
     sb.append(addSpaces(toode, 10));//360 tootekood
     sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 350
     sb.append(addSpaces(nimi , 25));  //375 tootenimi
     sb.append(addSpaces("",25)); //lisan tyhikuid hunniku 435
     sb.append(addSpaces(nf.format(kogus).replace(',','.').replaceAll(" ",""), 12));//447    
     sb.append(type);//448
     sb.append(addSpaces("",12)); //lisan tyhikuid hunniku 470
     sb.append(addSpaces(scala,10));
     sb.append("  ");//472 ladu 
     sb.append(addSpaces("",51));//525
     sb.append(addSpaces(ring,2));//525
     sb.append(getTarne(order));//532
     

        sb.append(addSpaces(" ",300));//310
        sb.append(System.getProperty("line.separator"));
        
       cou++;
try{
       addDBLine(cust,scala,agent,order,toode,doknr,kogus,ring);
}catch (Exception e ){
	e.printStackTrace();
	log.error(e);
}
       
      }
      

      
      setNumAddedLines(cou);
      String fn = doknr;
      if(doknr.trim().length()== 0){
    	  fn =( Calendar.getInstance().getTimeInMillis() + "").substring(3);
      }
      String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + fn  + ".txt";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sb.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  

/* private double makeRoundUp(double kogus,double rounder){

	 double ii = Math.round(kogus/rounder + 0.5);	 
	 return rounder * ii;
 }*/
 
 private boolean inList(String intr){
	  
	  if(intr.equals("K504")){
		 return true;
	  }
	  if(intr.equals("M12320")){
			 return true;
		  }
	  if(intr.equals("K501")){
			 return true;
		  }
	  if(intr.equals("K511")){
				 return true;
		  } 
	  if(intr.equals("K503")){
				 return true;
		  } 
	  if(intr.equals("K517")){
				 return true;
		  } 
	  if(intr.equals("K509")){
				 return true;
		  } 
	  if(intr.equals("K502")){
				 return true;
		  } 
	  if(intr.equals("K512")){
				 return true;
		  } 
	  if(intr.equals("K510")){
			 return true;  
	  }
		   if(intr.equals("K513")){
				 return true;
		  }
		  if(intr.equals("K506")){
				 return true;
		  }
		  if(intr.equals("K507")){
				 return true;
		  }
		  if(intr.equals("K515")){
				 return true;
		  }
		  if(intr.equals("K508")){
				 return true;
		  }
		  if(intr.equals("K516")){
				 return true;
		  }
	
	 return false;
 }
 
  public String getDriverVersion(){
    return "KalevBakeryImport";
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

  


  private String getTarne(Order order){
    Calendar cal = Calendar.getInstance();
    Calendar dat = order.getShipCal();

    String result = null ;
    if (dat != null){
    	 String yy = (dat.get(Calendar.YEAR))+"";

    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
    	  if(mm.length()== 1){
    	  	mm = "0"+mm;
    	  }
    	  
    	  
    	  String dd = (dat.get(Calendar.DATE))+"";
    	  if(dd.length()== 1){
    	  	dd = "0" + dd;
    	  }
    	  
    	  
    	  result = yy + mm + dd;
    	 System.out.println("TARNEP�EV SHIP0  " + result + " " + dat.get(Calendar.DATE));
    	  
    }else{
     String yy = (cal.get(Calendar.YEAR))+"";

	  String mm = (cal.get(Calendar.MONTH)+1)+"";
	  if(mm.length()== 1){
	  	mm = "0"+mm;
	  }
	  
	  int d = cal.get(Calendar.DATE);
	  String dd = d+"";
	  if(dd.length()== 1){
	  	dd = "0" + dd;
	  }
	  

	//  result = yy.substring(2,4) + mm + dd;
	  result = yy + mm + dd;
    }
    return result;
  }
  
  private String getTarneLyh(Order order){
	    Calendar cal = Calendar.getInstance();
	    Calendar dat = order.getShipCal();

	    String result = null ;
	    if (dat != null){
	    	if (dat.getTime().after(cal.getTime())){
	    		//dat.setTimeInMillis(dat.getTimeInMillis() - 86400000 ); siin tekkis jama 
	    	}
       
	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (dat.get(Calendar.DATE))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  
	    	  result = dd + mm ;
	    	 // System.out.println("TARNEP�EV SHIP " + result + " " + dat.toGMTString());
	    	  
	    }else{
	     

		  String mm = (cal.get(Calendar.MONTH)+1)+"";
		  if(mm.length()== 1){
		  	mm = "0"+mm;
		  }
		  
		  int d = cal.get(Calendar.DATE);
		  String dd = d+"";
		  if(dd.length()== 1){
		  	dd = "0" + dd;
		  }
		   result = dd + mm;
		//  System.out.println("TARNEP�EV " + result + " " + dat.toGMTString());
	    }
	    return result;
	  }
  
  
  private String getTana(){
	    Calendar cal = Calendar.getInstance();

	    String result = null ;
	    	 String yy = cal.get(Calendar.YEAR)+"";
	    	  String mm = (cal.get(Calendar.MONTH) +1 )+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = cal.get(Calendar.DAY_OF_MONTH)+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;	  
	            }
	    	  result = yy + mm + dd;
			 // System.out.println("T�NAP�EV " + result + " " + dat.toGMTString());
	    return result;
	  }
  
  public static final String addSpaces(String s, int totalLen){
	  if(s == null) s = "";
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


  private String url = "jdbc:odbc:scalaDB";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;
  private int cou = 0;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
