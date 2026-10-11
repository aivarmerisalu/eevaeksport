
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.imp.driver;



import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

public class PragmaImportDriver
    extends AbstractImportDriver {

  static Category log = Category.getInstance(IceImportDriver.class.getName());

  public PragmaImportDriver(ImportPanel parent) {
    this.parent = parent;
    PropertyConfigurator.configure("log4j.properties");
   
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = DriverManager.getConnection(url, "pda","pda");
      calNF.setMaximumFractionDigits(0);
      calNF.setMaximumIntegerDigits(2);
      calNF.setMinimumIntegerDigits(2);
      calNF.setGroupingUsed(false);
    
    }
    catch (Exception e) {
      parent.showError(e);
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
 	 String	sql1 = "select   vardas AS agent  from filialai f , darbuotojai ag " +
        " WHERE   ag.vardas=f.svfirma  and f.filialasid ="  + id + " and ag.xkred='PMOBILE' " +
	        "  order by f.filialas"; 
 	
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

  
 
  
 
  public void makeOrder(Order order) throws SQLException{
    setOrder(order);
    makeOutput(order);
  }

 
  
 private void makeOutput(Order order){
 	 String uniq = "";
 	StringBuffer sbuus = new StringBuffer();
 	 
 	 nf.setGroupingUsed(false);
 	 
  	try { 
 	 String cmd = "select f.filialasid as kood, f.filialas as saanimi ,f.pilnas as saanimi2,f.adresas AS saaaadr,k.pilnas AS klinimi, k.adresas AS kliaadr,f.firma as klikood ," +
			"  f.saskaita as fkonto,f.bankokodas AS fbankood,f.bankas AS fpanknimi, k.saskaita as kkonto,k.bankokodas AS kbankood,k.bankas AS kpanknimi, " +
	  " k.nuolaida as ale,k.kreditmax AS limit,k.grupe as kligrupe, k.firmaid AS maksja, k.firkodas, k.pastaba as firarve , k.apmterminas  as term, " +
	  "  tabelionr AS agent , k.koment as komment, * " +
      "   from filialai f , firmos k , darbuotojai ag   " +
      "   WHERE   f.firma=k.firma and ag.vardas=f.svfirma and xkred='PMOBILE' and tipas=2  and f.filialasid = " + order.getCompany() +   
	   "  order by f.filialas";
 	 
 System.out.println(cmd);
 
        Statement stmt  = con.createStatement();
	    ResultSet  rs = stmt.executeQuery(cmd);
      String klinimi = "";
      String kliaadr = "";
      String klikood = "";
      String saanimi = "";
      String firkodas = "";
      String saanimi2 = "";

      String saaaadr = "";
      String fbankood = "";
      String fpanknimi = "";
      String kkonto = "";
      String kbankood = "";
      String kpanknimi = "";
      String kligrupe = "";
      String firarve= "";
      String komment = "";
      int term = 1;
      
while (rs.next()) {
	klinimi =  rs.getString("klinimi");
	term = rs.getInt("term");
	kliaadr = rs.getString("kliaadr");
	klikood = rs.getString("klikood");
	firkodas = rs.getString("firkodas");
	firarve = rs.getString("firarve");
	saanimi2 = rs.getString("saanimi2");
		saanimi = 	rs.getString("saanimi");
		saaaadr = rs.getString("saaaadr");
		//saaaadr = rs.getString("fkonto");
		kligrupe = rs.getString("kligrupe");
		fbankood = rs.getString("fbankood");
		fpanknimi = rs.getString("fpanknimi");
		kkonto = rs.getString("kkonto");
		kbankood = rs.getString("kbankood");
		kpanknimi = rs.getString("kpanknimi");
	//	saaaadr = rs.getString("saaaadr");
		komment = rs.getString("komment");
				
}


if(komment == null){
	komment = "";
	}
if(kpanknimi == null){
	kpanknimi = "";
	}
if(kbankood == null){
	kbankood = "";
	}
if(kkonto == null){
	kkonto = "";
	}



if(klinimi == null){
	klinimi = "";
	}

if(kliaadr == null){
	kliaadr = "";
	}

if(klikood == null){
	klikood = "";
	}
if(firkodas == null){
	firkodas = "";
	}

if(firarve == null){
	firarve = "";
	}
if(saanimi2 == null){
	saanimi2 = "";
	}
if(saanimi == null){
		saanimi = "";
	}
if(saaaadr == null){
	saaaadr = "";
	}
if(kligrupe == null){
	kligrupe = "";
	}
if(fbankood == null){
	fbankood = "";
	}
if(fpanknimi == null){
	fpanknimi = "";
	}
 	 System.out.println("nimi" + klinimi);
 	
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
        // if(doknr.length()>6){
         //	doknr = doknr.substring(doknr.length()-6,doknr.length());
         //}
		
         
    Vector lines = order.getOrderLines();
      for (int i = 0; i < lines.size(); i++) {

        	OrderLine line = (OrderLine)lines.elementAt(i);
            String toode = line.getToode();
              double disc = line.getDiscount();
          
    	  
    	 	 String cmd2 = " select nomnr as tkood,preke  AS tnimi, pak AS kastis,  pardkaina AS price  " +
                  " , par1 AS ean,grupe AS grupp ,matas AS tyhik ,pvmproc AS km from prekes where wuzs=1 " +
				  " and prekeid = " + toode + "   order by prekeid";
    	 	 
    	 System.out.println(cmd2);
    	 
    	        Statement stmt2  = con.createStatement();
    		    ResultSet  rs2 = stmt2.executeQuery(cmd2);
    	      String tnimi = "";
    	      String tyhik = "";
    	      String tkood = "";
    	      double hind = 0;
    	      String ean = "";
    	while (rs2.next()) {
    		tnimi =  rs2.getString("tnimi");
    		tyhik = rs2.getString("tyhik");
    		tkood = 	rs2.getString("tkood");
    		hind = rs2.getDouble("price");
    		ean = rs2.getString("ean");
    			//saaaadr = rs2.getString("saaaadr");
    			
    	}
    	  
    	
    	
    	
    	
    	
	 	 String cmd3 = " select ikaina from invent where nomnr='"  + tkood + "'";
	 
System.out.println(cmd3);
String oh  = "0";
       Statement stmt3  = con.createStatement();
	    ResultSet  rs3 = stmt3.executeQuery(cmd3);
  
while (rs3.next()) {
	oh =  rs3.getString("ikaina");
}
    	
    	
        if(disc > 0 && disc != 100){
            }   
            if(disc == 0){
            }
            if(disc == 100){
            }
            
            nf.setMaximumFractionDigits(4);
         
          
/*
1553008,2,"Number siia","",08.12.5 00:00:00,08.12.19 00:00:00,08.12.5 00:00:00,"Airoda","UAB 'Airoda'","Naujo gyvenimo 1b, Domeikava","","","","Nerijus","UAB 'Rita'","Raudondvario pl.94, Kaunas","LT55 7044 0600 0348 2475","70440","AB SEB bankas","Preki�  Kaune","Lt-pard preki� VP Market trump.skolon","240","5004","240","4701","604","203","18.00000000","","","1.00000000","240","5004","0.0000","","","0.00000000","","","0.00000000","240","5004","75.4000","13.5700","0.0000","0.0000","0.0000","61.80000000","88.9700","","0.00000000","0.0000","0.0000","0.0000","0.00000000",,"0.0000","0.0000","0.0000",1,1,0,1,1,"-","",1,"",0,"","","","","","","0.0000","Admin","Kal�dinis rinkinys 123g","123Kal�dRink","vnt","240","5004","10.0000","0.0000",0,"0.00000000","0.00000000","604","203","0.0000","0.0000","2.89000000","3.41020000","28.90000000","18.00000000","5.20000000","0.00000000","0.0000","2.8900","0.00000000","0.00000000","0.00000000","0.0000","0.00000000","0.0000","0.0000","0.0000","2.4500",08.12.5 00:00:00,08.12.5 00:00:00,2,"","43","0.00000000",0,"",0,"0.21739130","0.0000",0,"","123Kal�dRink","","","Naujo gyvenimo 1b, Domeikava","","","Virgis,Nerijus","5985932","LT598593219","","I�krovimas: Neries 26, Domeikava","","","",08.12.5 00:00:00,"","","",,"",,,,"5906868440079","0","",0
1553008,2,"Egle2","",08.12.5 00:00:00,08.12.19 00:00:00,08.12.5 00:00:00,"Airoda","UAB 'Airoda'","Naujo gyvenimo 1b, Domeikava","","","","Nerijus","UAB 'Rita'","Raudondvario pl.94, Kaunas","LT55 7044 0600 0348 2475","70440","AB SEB bankas","Preki�  Kaune","Lt-pard preki� VP Market trump.skolon","240","5004","240","4701","604","203","18.00000000","","","1.00000000","240","5004","0.0000","","","0.00000000","","","0.00000000","240","5004","75.4000","13.5700","0.0000","0.0000","0.0000","61.80000000","88.9700","","0.00000000","0.0000","0.0000","0.0000","0.00000000",,"0.0000","0.0000","0.0000",1,1,0,1,1,"-","",1,"",0,"","","","","","","0.0000","Admin","Pieni�kas �okoladas 'Terravita' 250g","250�okTerravita","vnt","240","5004","10.0000","0.0000",0,"0.00000000","0.00000000","604","203","0.0000","0.0000","4.65000000","5.48700000","46.50000000","18.00000000","8.37000000","0.00000000","0.0000","4.6500","0.00000000","0.00000000","0.00000000","0.0000","0.00000000","0.0000","0.0000","0.0000","3.7300",08.12.5 00:00:00,08.12.5 00:00:00,2,"","43","0.00000000",0,"",0,"1.11111111","0.0000",0,"","250�okTerravita","","","Naujo gyvenimo 1b, Domeikava","","","Virgis,Nerijus","5985932","LT598593219","","I�krovimas: Neries 26, Domeikava","","","",08.12.5 00:00:00,"","","",,"",,,,"5900915010338","0.276","",0

 
 select * from vazpirkimo where vaztipas = 2 order by vazdata desc

select * from dokserijos

insert into vazpirkimo (vaztipas,svpilnas,svadresas,svsaskaita,svbankokodas,svbankas,dmedz,dpvm,kpvm,
dsav,ksav,procpvm,dtransp,dkiti,stat,pvmib) values ( 2,'UAB "Rita"','Raudondvario pl. 94, Kaunas','LT55 7044 0600 0348 2475',
'70440','AB SEB bankas','240','240','4701','603','203',18,'240','240',1,1
)


  */          
   
          if (i == 0 ){
          	if( line.getKogus() < 0){
          		System.out.println("kogus " + line.getKogus());
             // 	doknr = getDoknr("OR05");
              	
          	}
          	
          }
       //   String paymterm = getTerm(order.getPaymTerm());
          
          Calendar cal = Calendar.getInstance();
          Calendar cal2 = Calendar.getInstance();
          cal2.add(Calendar.DAY_OF_MONTH,term );
           uniq =(cal.getTimeInMillis() + "").substring(5);
          
          String summa = "0";
          String km = "0";
          String kokku = "0";
         
          
          sbuus.append("1553000,2,\"" + "MJ" + uniq+ "\",\"\"," + this.getPragmaDate(cal)+ "," + this.getPragmaDate(cal2)+ ","
        		  + this.getPragmaDate(cal)+ ",\"" + klikood + "\",\"" + saanimi2 +"\"" +
          		",\"" + saaaadr + "\",\"\",\"\",\"\",\"" + agent + "\",\"UAB 'Rita'\",\"Raudondvario pl.94, Kaunas\"," +
          		"\"LT55 7044 0600 0348 2475\",\"70440\",\"AB SEB bankas\",\"Preki�  Kaune\",\"Lt-pard preki� VP Market trump.skolon\",\"240\",\"5004\",\"240\"" +
          		",\"4701\",\"604\",\"203\",\"19.00000000\",\"\",\"\",\"1.00000000\",\"240\",\"5004\",\"0.0000\",\"\",\"\",\"0.00000000\"" +
          		",\"\",\"\",\"0.00000000\",\"240\",\"5004\",\"" + summa + "\",\"" + km + "\",\"0.0000\",\"0.0000\",\"0.0000\",\"" + kokku + "\"," +
          		"\"88.9700\",\"\",\"0.00000000\",\"0.0000\",\"0.0000\",\"0.0000\",\"0.00000000\",,\"0.0000\",\"0.0000\"," +
          		"\"0.0000\",1,1,0,1,1,\"-\",\"\",1,\"\",0,\"\",\"\",\"\",\"\",\"\",\"\",\"0.0000\",\"Admin\",\"" + tnimi + "\"," +
          		"\"" + tkood + "\",\"" + tyhik +"\",\"240\",\"5004\",\""+line.getKogus()+"\",\"0.0000\",0,\"0.00000000\",\"0.00000000\",\"604\"," +
          		"\"203\",\"0.0000\",\"0.0000\",\"" + hind + "\",\""+ (hind*1.19) + "\",\"" +( hind * line.getKogus())+ "\",\"19.00000000\",\"" +( hind * line.getKogus())+ "\"," +
          		"\"0.00000000\",\"0.0000\",\"2.8900\",\"0.00000000\",\"0.00000000\",\"0.00000000\",\"0.0000\",\"0.00000000\"" +
          		",\"0.0000\",\"0.0000\",\"0.0000\",\"" + oh + "\"," + this.getPragmaDate(cal) + "," + this.getPragmaDate(cal) + ",2,\"\",\"43\",\"0.00000000\",0,\"\",0" +
          		",\"0.21739130\",\"0.0000\",0,\"\",\"" + tkood + "\",\"\",\"" + saanimi + "\",\"" + kliaadr + "\",\"\",\"\"," +
          		"\"" + kligrupe + "\",\"" + firkodas + "\",\"" + firarve +"\",\"\",\"" + komment + "\",\"\",\"\",\"\"," 
          		+ this.getPragmaDate(cal) + ",\"\",\"\",\"\",,\"\",,,,\"" + ean + "\",\"0\",\"\",0\n");

      
                
                cou++;
      }

    	
      setNumAddedLines(cou);
      String fileName = escape(parent.getProps().getProperty("conv.dir")) + agent  + "." + "MJ" + uniq   +  ".txt";
      System.out.println(fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(sbuus.toString().getBytes());
        fos.close();

	} catch (Exception e) {
		e.printStackTrace();
	}
 }
  
  public String getDriverVersion(){
    return "PragmaImport";
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

  private String getPragmaDate(Calendar ca ){
	  String res = "";
	  int y = ca.get(Calendar.YEAR);
	  int m = ca.get(Calendar.MONTH) + 1;
	  int d = ca.get(Calendar.DAY_OF_MONTH);
	  res = (y+"").substring(2)+ "."+ m + "." + d + " 00:00:00";
	return res;
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


  private String url = "jdbc:odbc:pragma";
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Connection con;
  private ImportPanel parent;

public String getTrouble() {
	// TODO Auto-generated method stub
	return null;
}
}
