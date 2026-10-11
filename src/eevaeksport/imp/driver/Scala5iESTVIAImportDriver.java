
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
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

public class Scala5iESTVIAImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public Scala5iESTVIAImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "pda","pda");
      updateCustcodes();
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }
  
  private void updateCustcodes(){
  	Hashtable res = new Hashtable();
  	try {
		File fil = new File("sidus.txt");
		 System.out.println("Via Koodide sidumine ");
if (fil.exists()){
       System.out.println("Seon koode ... ");
		FileReader fr = new FileReader("sidus.txt");
		BufferedReader reader = new BufferedReader(fr);
		String line = null;
		while ((line = reader.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, ";");
			while(st.hasMoreTokens()){
				res.put(st.nextToken(),st.nextToken());
			}
			
       }
		System.out.println("Kokku firmasi :" + res.size());
		if(res.size() > 0){
			Enumeration enumr = res.keys();
			while(enumr.hasMoreElements()){
			String omakood = (String)enumr.nextElement();
			String viakood = (String)res.get(omakood);
			   String sql1 = "UPDATE  SL010100 set SL01008 ='" + viakood+ "' where SL01001  =  '" + omakood + "'";			
			    System.out.println(sql1);
			    Statement st1 = con.createStatement();
			    st1.executeUpdate(sql1);
			    st1.close();	
			}
			System.out.println("Kliendi Koodide sidumine valmis " );
		}
		
		
		
}
		}catch (Exception e){
			e.printStackTrace();
			
		}
  }

  public String identifyCustomer(String id,String regnr) throws SQLException {
  	String sql1 = null;
  	
  	 System.out.println("Kliendi M��ramine: " );
  	
  	    sql1 = "SELECT SL01001 AS kood FROM SL010100 where SL01008  =  '" + id + "'";	
	
    System.out.println(sql1);
    String acc = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      acc = rs1.getString("kood").trim() ;

    }
    rs1.close();
    st1.close();

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
 	 String	sql1 = "SELECT SL01035 as agent FROM SL010100 left JOIN SY240100  ON SL01036=SY24002 " +
 	 		"and SY24001 ='BG' WHERE SL01001='" + id +"'"; 
 	
   System.out.println(sql1);
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
 	 
    String sql1 = " SELECT RTRIM(SC01150) AS pant FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode.trim() + "'";
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
  	
    
    String sql1 = " SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01132)=   '" + ean.trim() + "'";

    String sql2 = "SELECT RTRIM(SC01001) AS toode FROM SC010100  WHERE RTRIM(SC01001)=  '" + prodID.trim() + "'";

    String prod = null;

    Statement st1 = con.createStatement();
    ResultSet rs1 = st1.executeQuery(sql1);

    while (rs1.next()) {
      prod = rs1.getString("toode").trim();
    }
   System.out.println(ean + " prod0 " + prod);
    if(prod == null){

      rs1 = st1.executeQuery(sql2);

      while (rs1.next()) {
        prod = rs1.getString("toode").trim();
      }
    }

    System.out.println(prodID + " prod1 " + prod);
    rs1.close();
    st1.close();

    return prod;
  }

  
  private String getTerm(int term){
  	String res = "";
  	 String sql1 = " SELECT SL22002 AS term FROM SL220100  WHERE rtrim(ltrim(SL22005))=" + term + "";


 			try{
 				 Statement st1 = con.createStatement();
 				    ResultSet rs1 = st1.executeQuery(sql1);
 			
 				      while (rs1.next()) {
 				        res = rs1.getString("term");
 				      }
 			          rs1.close();
 				      st1.close();
 			
 				      
 			}catch (Exception e){
 				
 			}
 			if(res == ""){
 				sql1 = " SELECT SL22002 AS term FROM SL220100  WHERE rtrim(ltrim(SL22005))=" + (term +1)+ "";


 	 			try{
 	 				 Statement st1 = con.createStatement();
 	 				    ResultSet rs1 = st1.executeQuery(sql1);
 	 			
 	 				      while (rs1.next()) {
 	 				        res = rs1.getString("term");
 	 				      }
 	 			          rs1.close();
 	 				      st1.close();
 	 			
 	 				      
 	 			}catch (Exception e){
 	 				
 	 			}

 			}
 			 System.out.println( "Makset�htaja koodileidmine " + res  + " " + term);
 if(term == 0){
 	return "";
 }else{
	return res;	
 }
  }
  
  private double getRealDiscount(String toode,double price){
  	double res = 0;
  	
    String sql1 = " SELECT SC01004 AS price FROM SC010100  WHERE RTRIM(SC01001)=   '" + toode + "'";

    double  pr = 0;
			try{
				 Statement st1 = con.createStatement();
				    ResultSet rs1 = st1.executeQuery(sql1);
			
				      while (rs1.next()) {
				        pr = rs1.getDouble("price");
				      }
				    
			}catch (Exception e){
				
			}

  
if(pr != 0 && price != 0){
	res =  1- (price/pr);
}
System.out.println( "Ale arvutus " + pr + " prod1 " + price + " ale " + res*100);
  	return res*100;
  }

  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    makeOutput(order);
  }

  private String getDoknr(String aa) throws SQLException{
  	String res = null;
 	 String	sql1 = "select SY68002 as nr from SY6801XX where SY68001 = '" +aa +"'"; 
			
			Statement st1 = con.createStatement();
			ResultSet rs1 = st1.executeQuery(sql1);
			
			if (rs1.next()) {
			res = rs1.getString("nr").trim();
			
			Statement ss = con.createStatement();
			String sql2 = "UPDATE SY6801XX set SY68002=SY68002+1 where SY68001 = '" +aa +"'";
			 ss.executeUpdate(sql2);
			 ss.close();
			
			}
			
			
			
	rs1.close();
	st1.close();
  	return res ;
  }
  
  
 private void makeOutput(Order order){
 	StringBuffer sb = new StringBuffer();
 	 Hashtable pandikas =  new Hashtable();
	 boolean duplicate = false;
 	 
 	 nf.setGroupingUsed(false);
 	 
 	 
 	try {
 	  int cou = 0;

      
 	    String cust = order.getCompany();
		String agent = this.identifyCustomerAGENT(cust);
		
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
         if(doknr.length()>6){
         	doknr = doknr.substring(doknr.length()-6,doknr.length());
         }
		String tellnr = (Calendar.getInstance().get(Calendar.YEAR)+"").substring(2) + doknr;
         String type = "2";
         
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {
      	
          
      	OrderLine line = (OrderLine)lines.elementAt(i);
          String toode = line.getToode();
          double price = line.getPrice();
            double discount = getRealDiscount(toode,price);
            double pricecounted = price;
            double disc = line.getDiscount();
         
            if(disc > 0 && disc != 100){
            	pricecounted = price/((100-disc)/100);
            }
            

            if(disc == 0){
            	discount = 0;
            }
            
            if(disc == 100){
            	discount = 100;
            	//pricecounted = 0;
            }
            
            nf.setMaximumFractionDigits(4);
           System.out.println("linedisc:" + disc + " hindarvutuslik:" + pricecounted );
            
          
/*OR04	322569
 OR05	941450
 OR06	809199
 OR07	210013*/
          
          
   	   String pant = getPandiPakend(toode);
       
       if(pant != null && discount != 100){
     	  if(pandikas.get(pant) == null){
     		  pandikas.put(pant,line.getKogus()+"");
           } else {
           	pandikas.put(pant,(new Double((String)pandikas.get(pant)).doubleValue()+line.getKogus())+"");
           }
        }
 
          if (i == 0 ){
          	if( line.getKogus() < 0){
          		System.out.println("kogus " + line.getKogus());
             // 	doknr = getDoknr("OR05");
              	type = "8";
          	}
          	if(cust.equals("INTR.DEG") || cust.equals("INTR.MAHA")){
          		doknr = getDoknr("OR06");
          	}
         	
          }
          String paymterm = getTerm(order.getPaymTerm());
          
          
             
     
//329900    22413                2  1 1  07092545      329900                              2  4230                        2       18.579     ;TAGASTUS;SAATELEHT NR. 20;
       
         //v�tsime ksutusest �ra
         
         
      	 sb.append(addSpaces(tellnr, 10));
      	sb.append(type);//11
        sb.append(addSpaces(cust, 10));//22
        sb.append("          ");//32
        sb.append("2");//33
        sb.append(addSpaces(paymterm,2));//35
        sb.append("1 ");//37
        sb.append("1  ");//40
        sb.append(getTarne(order));//46
        sb.append(addSpaces(agent, 3));//49
        sb.append("     ");//54
        sb.append(addSpaces(doknr, 6));//60
        sb.append("      ");////66
        sb.append("      ");//72
        sb.append("      ");//78 Warehouse Order
        sb.append("      ");//84
        sb.append("      ");//90
        sb.append(addSpaces(String.valueOf(cou + 1), 3));//93
        sb.append(addSpaces(toode, 20));//113
        if(disc==100){
        	sb.append(addSpaces("",8));//121
        }else{
            sb.append(addSpaces(nf.format(pricecounted).replace(',','.').replaceAll(" ",""),8));//121
        }
       
        sb.append(addSpaces(nf.format(line.getKogus()).replace(',','.').replaceAll(" ",""), 8));//129
        sb.append(addSpaces(nf.format(disc).replace(',','.'), 6));//135
        sb.append("      ");//140 
        sb.append("        ");//146
        sb.append(addSpaces(com,70));//181
        if(disc==100){
        	sb.append(addSpaces("",8));//121
        }else{
            sb.append(addSpaces(nf.format(pricecounted).replace(',','.').replaceAll(" ",""),8));//121
        }
        
        sb.append(addSpaces(nf.format(disc).replace(',','.'), 6));

        sb.append(System.getProperty("line.separator"));
        
       cou++;
      }

      if(pandikas.size()>0){
      	Enumeration enumpant = pandikas.keys();
       while(enumpant.hasMoreElements()){
       	
       	String kood = (String)enumpant.nextElement();
       	if(kood .trim().length()>0){
       	
    	double pand = new Double((String)pandikas.get(kood)).doubleValue();
    	  
          nf.setMaximumFractionDigits(4);
          nf.setGroupingUsed(false);
          
         
      	 sb.append(addSpaces(tellnr, 10));
      	sb.append(type);//11
      //	sb.append("1");//11
        sb.append(addSpaces(cust, 10));//22
        sb.append("          ");//32
        sb.append("2");//33
        sb.append("  ");//35
        sb.append("1 ");//37
        sb.append("1  ");//40
        sb.append(getTarne(order));//46
        sb.append(addSpaces(agent, 3));//49
        sb.append("     ");//54
        sb.append(addSpaces(doknr, 6));//60
        sb.append("      ");////66
        sb.append("      ");//72
        sb.append("      ");//78 Warehouse Order
        sb.append("      ");//84
        sb.append("      ");//90
        sb.append(addSpaces(String.valueOf(cou + 1), 3));//93
        sb.append(addSpaces(kood, 20));//113
        sb.append("        ");//121
       // sb.append(addSpaces(nf.format(line.getPrice()).replace(',','.'), 8));//121
        sb.append(addSpaces(nf.format(pand), 8));//129
        sb.append(addSpaces(nf.format(0).replace(',','.'), 6));//135
        sb.append("      ");//140 
        sb.append("        ");//146
        sb.append(addSpaces(com,70));//181
        sb.append(addSpaces("", 20));
    	sb.append(addSpaces("0", 6));
        
      //  sb.append(cust.getComment());
        sb.append(System.getProperty("line.separator"));
        
                
       
       cou++;
   	
       	}
      	
      }
      }
    	
      setNumAddedLines(cou);
      
      
     
        

        	 
          String fileName = escape(parent.getProps().getProperty("conv.dir") + agent)  + "." + doknr   +  ".txt";
 	     
 	      System.out.println(fileName);
 	        FileOutputStream fos = new FileOutputStream(fileName);
 	        fos.write(sb.toString().getBytes());
 	        fos.close();
        	
       
        
        

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "Scala5iESTVIAImport";
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

  
 

  private String getTarne(Order order){
	    Calendar cal = Calendar.getInstance();
	    Date dat = order.getShipCal().getTime();

	    String result = null ;
	    if (dat != null){
	    	cal.setTime(dat);
	    	 String yy = (cal.get(Calendar.YEAR))+"";

	    	  String mm = (cal.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (cal.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }
	    	  
	    	  
	    	  result = yy.substring(2,4) + mm + dd;
	    	  System.out.println("P�EV SHIP " + result + " " + dat.toString());
	    	  
	    }else{
	     String yy = (cal.get(Calendar.YEAR))+"";

		  String mm = (cal.get(Calendar.MONTH)+1)+"";
		  if(mm.length()== 1){
		  	mm = "0"+mm;
		  }
		  
		  int d = cal.get(Calendar.DAY_OF_MONTH);
		  String dd = d+"";
		  if(dd.length()== 1){
		  	dd = "0" + dd;
		  }
		  
		  
		  result = yy.substring(2,4) + mm + dd;
	    }
	    return result;
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


  private String url = "jdbc:odbc:scalaDB";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
