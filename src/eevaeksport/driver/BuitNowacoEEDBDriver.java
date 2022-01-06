package eevaeksport.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BuitNowacoEEDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker,PictureMaker2 {

  public BuitNowacoEEDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
	user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  String user =  null;
  String pass = null;
  String rcompany = null;
    
  
  private void makePictureCat(){
	     
	   	String  cmd2 = " SELECT t.preke AS toode  " +
	" FROM prekes t inner join grupes g on g.grupe=t.grupe inner join tipai ty on ty.tipas=t.tipas " +
	" where pard_kaina != 0 or kaina_1 < 321.77 or kaina_2 < 321.77 or kaina_3 < 321.77 or kaina_4 < 321.77 or kaina_5 < 321.77 or " +
	" kaina_6 < 321.77 or kaina_7 < 321.77 or kaina_8 < 321.77 or kaina_9  < 321.77 or kaina_10 < 321.77 ";

	    
	   	
	     	String picts = parent.getProps().getProperty("pict.dir");
	 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	 	 

	   	try {
	 	   Connection con = getNewConnection();
		
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd2);


	        StringBuffer buf = new StringBuffer();
	        StringBuffer buf0 = new StringBuffer();
	        int a = 0;
	        int b = 0;
	        int c = 0;
	        
	        // leiame kataloogid ja peame meeles 
	        Vector cats = new Vector();
	        File d  = new File(picts);
	        File[] dd = d.listFiles();
	     
	        //System.out.println("file " + picts + " " + d.exists() );
		     
	        
	        for(int i = 0; i < dd.length ; i++){
	        	File t = dd[i];
	        	if(t.isDirectory()){
	        		cats.add(t.getName());
	        	}
	        }
	        
	        System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
	   	    while (rs.next()) {
	   	    	a++;
	   	    	c++;
	   	    	
	   	     String toode = rs.getString("toode").trim();  
	         toode = toode.replace('/', '_');
	         toode = toode.replace('/', '_');
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		  System.out.println("Piltide leidmine "  + a);	
	   	    		  
	   	    	}
	   	     
	              
	                 File f = new File(picts + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                 } else if(cats.size() > 0 ){
	                	 for(int w = 0 ;w < cats.size();w++){
	                		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
	                		
	                		if(f.exists()){
	                			System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
	                		     len =	f.length() + "";	
	                		}                     
	                	 }
	                 }
	                 
	                 if(len.equals("0")){
	                 	//System.out.println(toode+".jpg;"  + len);
	                 	buf0.append(toode+".jpg\n");
	                 }else{
	                 	buf.append(toode+".jpg;"  + len + "\n");
	           	    	b++;
	                 }
	                 }
	   	    rs.close();
	   	    stmt.close();
	   	    con.close();
	   	
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
//	        	 Lï¿½ppes kustutamine 
	        	 
	          System.out.println("Piltide kopeerimine ");	
	          //Failide kopeerimine  
	            String[] gg = new File(picts).list();
	         	  //System.out.println("gg " + gg[0] + " " + gg.length);
	            
	             for(int i = 0;gg.length>i;i++){          	
	             	File in = new File ( picts + gg[i]); 
	             	if(in.isFile()){            	
	             	File inpda = new File ( pictspda + toUpper(gg[i])); 
	                   	Converter.shrinkAndCopyFile(in,inpda);
	             	}
	             }
	             
	             //piltide kopeerimine alam kataloogist ..
	             
	             if(cats.size() >0){
	            	 for(int h = 0;h < cats.size(); h++){
	            		
	            	   String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
	               	 
	                  
	                   for(int i = 0;i < ff.length  ;i++){          	
	                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
	                   	if(in.isFile()){   
	                   		
	                   	File inpda = new File ( pictspda + toUpper(ff[i])); 
	                   	Converter.shrinkAndCopyFile(in,inpda);
	                   	 }
	                   }
	                   
	            	 }
	             }
	             
	             File fff =  new File (pictspda +  "list.txt");
	       	  Converter.copyFile(f,fff);  	
	       	  File fff0 =  new File (pictspda +  "list0.txt");
	       	Converter.copyFile(f0,fff0);  	
	   	} catch (Exception e) {
	   	    getParent().showError(e);
	   	}
	   
	}
	 
	   private String toUpper(String asi ){
		   
		   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
		   System.out.println(asi);
		   return asi ;
	   }
	

 
 
 

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	  
   public String getPilotDayData(Agent agent){

	   StringBuffer sb = new StringBuffer();
	           
	   String sql2 =  " select gavejas as klient,dok_serija as seria , kitas_dok as arve , data as kuupaev,(viso_1) AS summa,a.manager as agent ,vairuotoj as juht , tran_priem as autoke FROM apyvarta a " +
                      " where month(Data) =month(current_date()) and YEAR(Data) = YEAR(current_date()) " +
					" and day(Data) = day(current_date()) " +
				    "	and operacija  not like'%Reklam%' and operacija  not like'%Pirkimas%' and " +
				    "	(tiekejas is null or tiekejas like '%idvest%' ) and a.manager = '" + agent.getID()+ "'" +
				    "	order by gavejas";
	    
	
	   
	   System.out.println("siin");
	     String header = " Yesterdays sales :\n\n";
	    
	     boolean first = true;
	     nf.setMinimumFractionDigits(2);
	       try {
	       	
	       	
	             Connection con2 = getNewConnection();
       	
                Statement st = con2.createStatement();
               ResultSet rs2 = st.executeQuery(sql2);
                   while (rs2.next()) {
                   	if(first){
                   		sb.append(header);
                   		first = false;
                   	}
                   	String nimi = rs2.getString("klient").trim() ;
                    String juht =  rs2.getString("juht");
                    if(juht == null){
                    	juht = "";
                    }else{
                    	juht = juht.trim();
                    }
                    
                    String auto =  rs2.getString("autoke");
                    if(auto == null){
                    	auto = "";
                    }else{
                    	auto =  " " + auto.trim();
                    }
                    
                    String seria = rs2.getString("seria");
                  
                    if(seria == null){
                    	seria = "AAA";
                    }else{
                    	seria = seria.trim();
                    }
                    
                    String arve = rs2.getString("arve");
                    
                    if(arve == null){
                    	arve = "00000000";
                    }else{
                    	arve = arve.trim();
                    }
                    
                    String kuup = rs2.getString("kuupaev");
                    if(kuup == null){
                    	kuup = "0000000000";
                    }else{
                    	kuup = kuup.trim();
                    }
                    
                    String summa =  rs2.getString("summa");
                    if(summa == null){
                    	summa = "0000000000";
                    }else{
                    	summa = summa.trim();
                    }
                    
                   
                    
                    
                   sb.append(nimi  +
                   		"\n    (" + seria + "-" + arve + ")[" + kuup.substring(0,10) + "] " +
                   		summa + "lt  Trans.:" + juht + auto
                   		+  "\n\n");
                   }

                   rs2.close();
                   st.close();
                   con2.close();
                   
                   } catch (Exception e) {
                       getParent().showError(e);
                   }
      
  
       return sb.toString();

}




   public String getSaveDebtInfo() {
	   String cmd =  "   SELECT k.klient_id AS kood,k.kodas AS payer  FROM klientai k  INNER JOIN " +
		"  (select klientas, tab_nr from (select * from manager order by klientas,data desc)as man  group by klientas order by klientas,data ) " +
		"   a ON a.klientas = k.klientas and a.tab_nr >0 LEFT join marsrut ma on ma.kodas = k.marsrutas where  filialas =1 " ;

	   //kellel on blokid 

String cmd3 = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS debt FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " +
      " where a.terminas < CURRENT_DATE() and  dok_serija is not null group by b.kodas having sum(skola_w) != 0 " ;
		


		if (isDebug()) System.out.println(cmd);
		TreeMap custs = new TreeMap();




		StringBuffer products = new StringBuffer();
		try {

	    Connection con = getNewConnection();
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(cmd3);

		while (rs.next()) {
			String maks = rs.getString("maksja");
		String kood = rs.getString("klient");
		double volg = rs.getDouble("debt");
		Customer custw = null;
		if(maks != null){
			 custw = new Customer (maks.trim());
			custw.setDebt(volg);
			if(volg>0){
				custs.put(maks.trim(),custw);
			}
		}
		if(kood != null){
			 custw = new Customer (kood.trim());
			custw.setDebt(volg);
			if(volg>0){
				custs.put(kood.trim(),custw);
			}
		}
	
		
		}

		//siin pannakse peale PLOKKID

		rs = st.executeQuery(cmd);
		while (rs.next()) {
			String kood = rs.getString("kood").trim();
			String payer = rs.getString("payer");
		    Customer cus =  (Customer)custs.get(kood);
		    if(cus != null){
		    	cus.setSalesBlock(true);
		    }else{
		    	Customer cusnew = new Customer(kood);
		    	cusnew.setSalesBlock(true);
		    	custs.put(kood,cusnew);
		    }
		    if(payer != null){
		    	 cus =  (Customer)custs.get(payer.trim());
			    if(cus != null){
			    	cus.setSalesBlock(true);
			    }else{
			    	Customer cusnew = new Customer(payer.trim());
			    	cusnew.setSalesBlock(true);
			    	custs.put(payer.trim(),cusnew);
			    }
		    }
		 }

		rs.close();
		st.close();
		con.close();
		
		} catch (Exception e) {
		getParent().showError(e);
		}
		
		
       this.getCustomers(null);
		
		
		
		Iterator     codes = custs.values().iterator();
		while (codes.hasNext()){
		Customer cust = (Customer)codes.next();
		String bl = "0";
		if(cust.isSalesBlock()){
			bl = "1";
			}
		if(blockedcusts.contains(cust.getID())){
			bl = "1";
		}
		if(cust.isSalesBlockT()){
			bl = "2";
			}
		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
		}
       
		return products.toString();

      }

   
   
   
     
   public Vector getPriceMatrixData() {
   	
	   
	   
	   String sql = "select max(klient_id) as custid, k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup, " +
	    "  kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday " +
	    "    from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe " +	
	    "	where   data_iki is null  and (procentas != 0 or kaina > 0) and (preke >0 or grup_id >0) " +
	    " group by  k.kodas,preke,grup_id,kaina,procentas,data_nuo,data_iki";
	   
		//pï¿½si allahindlused toodetele ja gruppidele 
	  	 String    sql1 = "  select(klient_id) AS custid,k.kodas AS clgroup,  sistema as grupp " +
	       " from   klientai k where kodas != '' " +
		"	order by  k.kodas,klient_id " ;

Vector result = new Vector();
  try {
	  TreeMap custs = new TreeMap();
	  
	  Connection con = getNewConnection();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql1);
while (rs.next()) {
	String id = rs.getString("custid").trim();
	String pay = rs.getString("clgroup").trim();
	String grupp = rs.getString("grupp");
	
	
	if(grupp != null && grupp.trim().length() > 0){
		grupp = grupp.trim();
		if(custs.get(grupp)!= null){
			Vector vv = (Vector)custs.get(grupp);
			vv.add(id);
			custs.put(grupp, vv);
		  } else {
			Vector vv = new Vector();
			vv.add(id);
			custs.put(grupp, vv);
		  }
	}
	
	
	
	if(custs.get(pay)!= null){
		Vector vv = (Vector)custs.get(pay);
		vv.add(id);
		custs.put(pay, vv);
	}else{
		Vector vv = new Vector();
		vv.add(id);
		custs.put(pay, vv);
	}
	
	if(custs.get(pay)!= null){
		Vector vv = (Vector)custs.get(pay);
		vv.add(id);
		custs.put(pay, vv);
	}else{
		Vector vv = new Vector();
		vv.add(id);
		custs.put(pay, vv);
	}
}
  	
	  
	         rs = stmt.executeQuery(sql);
   
	 while (rs.next()) {
      String custID = rs.getString("custid").trim();
      String clGroup = rs.getString("clgroup");
      String prodID = rs.getString("prodid");
      String prodGroup = rs.getString("prodgroup");
      double price = rs.getDouble("price");
      double disc = rs.getDouble("disc");
     
  
      MatrixLine mline = new MatrixLine();

      if (custID != null && custID.length() > 0) {
        mline.setCustID(custID);
      }
   
      if (prodID != null && prodID.trim().length() > 0) {
        mline.setProdID(prodID.trim());
      }
      if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
        mline.setProdGroup(prodGroup.trim());
        mline.setDiscGroup(prodGroup.trim());
      }

      if (price > 0.01) {
        mline.setPrice(price);

      }
  
      if (disc != 0) {
          mline.setDiscount(disc);
      }
    
      
      if( mline.getPrice() > 0 || mline.getDiscount() > 0){
          result.add(mline);    
        if(clGroup != null && clGroup.trim().length() >0 && custs.get(clGroup.trim()) != null){
        	Vector vv = (Vector)custs.get(clGroup.trim());
        	for (int i =0 ; i< vv.size();i++){
        		String aa = (String)vv.get(i);
        		MatrixLine mm = new MatrixLine();
        		mm.setCustID(aa);
        		mm.setPrice(mline.getPrice());
        		mm.setProdID(mline.getProdID());
        		 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
              		mm.setProdGroup(mline.getProdGroup()); 
        		 }
        		 result.add(mm); 
        	}
        	
        }
      
      }
      
    }
    rs.close();
    
    
    
    
    sql = "select ale.klientas AS clgroup,preke AS prodid,grup_id AS prodgroup, " + 
	    " kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday , datediff(data_iki ,data_nuo) as kamp " + 
	     "  from  nuolaid ale inner join klientai k  ON ale.klientas=k.sistema left join  grupes g on g.grupe=ale.grupe  " +
	    "	where (current_date() between data_nuo and data_iki) or(current_date() > data_nuo and data_iki is null) and ( preke >0 or grup_id >0) " +
		"	group by ale.klientas ,preke,grup_id,kaina,procentas,data_nuo,data_iki " ;


	  
    rs = stmt.executeQuery(sql);

while (rs.next()) {
String clGroup = rs.getString("clgroup");
String prodID = rs.getString("prodid");
String prodGroup = rs.getString("prodgroup");
double price = rs.getDouble("price");
double disc = rs.getDouble("disc");


MatrixLine mline = new MatrixLine();



if (prodID != null && prodID.trim().length() > 0) {
mline.setProdID(prodID.trim());
}

if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
mline.setProdGroup(prodGroup.trim());
mline.setDiscGroup(prodGroup.trim());
}

if (price > 0.01) {
mline.setPrice(price);

}

if (disc != 0) {
 mline.setDiscount(disc);
}


if( mline.getPrice() > 0 || mline.getDiscount() > 0){
 //result.add(mline);    
if(clGroup != null && clGroup.trim().length() >0 && custs.get(clGroup.trim()) != null){
	Vector vv = (Vector)custs.get(clGroup.trim());
	for (int i =0 ; i< vv.size();i++){
		String aa = (String)vv.get(i);
		MatrixLine mm = new MatrixLine();
		mm.setCustID(aa);
		mm.setPrice(mline.getPrice());
		mm.setProdID(mline.getProdID());
		 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
     		mm.setProdGroup(mline.getProdGroup()); 
		 }
		 result.add(mm);
		// System.out.println("sisse sistema kaudu " + aa + " " + mline.getPrice() + " " +mline.getProdID());
	}	
}
}
}
rs.close();
    
    // teeme hinnakirjad valmis 
    
    for (int i = 1 ;i < 11; i++){
    	sql = "select " + (i+5) + " as pr1,preke ,prek_id as prodid, kaina_" + i + " as price from prekes where kaina_" + i + " < 321.77";
    	 con = getNewConnection();
	         stmt = con.createStatement();
	         rs = stmt.executeQuery(sql);
 while (rs.next()) {

   String clGroup = rs.getString("pr1").trim();
   String prodID = rs.getString("preke").trim();
   double price = rs.getDouble("price");
  
   MatrixLine mline = new MatrixLine(); 
     mline.setCliGroup(clGroup.trim());
     mline.setProdID(prodID);
     mline.setPrice(price);
     if(mline.getPrice()> 0.05 && mline.getPrice() < 1110){
    	 result.add(mline);    
    	    
     }
        }
    }
    
    
    
    
    
    //kampaaniate kysimine
    
    sql = " select klient_id AS custid,k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup, "+
	     "  kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday , datediff(data_iki ,data_nuo) as kamp" +
	     "   from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe  	" +
		"	where (current_date() between data_nuo and data_iki) or(current_date() > data_nuo and data_iki is null and ( preke >0 or grup_id >0)) " ;

    
    sql= " select max(klient_id) AS custid,k.kodas AS clgroup,preke AS prodid,grup_id AS prodgroup,  " +
	    " kaina AS price ,procentas as disc, data_iki as paev,data_nuo as startday , datediff(data_iki ,data_nuo) as kamp " +
	    "    from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe  " +
	"		where (current_date() between data_nuo and data_iki) or(current_date() > data_nuo and data_iki is null and ( preke >0 or grup_id >0)) " +
	"		group by  k.kodas,preke,grup_id,kaina,procentas,data_nuo,data_iki";
     rs = stmt.executeQuery(sql);
    while (rs.next()) {

      String custID = rs.getString("custid");// Converter.enCodeEst(rs.getString("custid").trim());
      String clGroup =  rs.getString("clgroup");
      String prodID = rs.getString("prodid");
      String prodGroup = rs.getString("prodgroup");     
      double price = rs.getDouble("price");
      double disc = rs.getDouble("disc");
      String date = rs.getString("paev");
      String sdate = rs.getString("startday");
      String kamp = rs.getString("kamp");
    
   
      MatrixLine mline = new MatrixLine();

      if (custID != null && custID.trim().length() > 0) {
        mline.setCustID(custID.trim());
      }
    

      if(kamp != null && kamp.trim().length() >0 ){
    	  try{
         if(Integer.parseInt(kamp.trim()) < 60){
        	 mline.setEndDate(date.substring(0,10));
        	 mline.setStartDate(sdate.substring(0,10));
        	  }  
    	  }catch(Exception e){ }  }
    	
    
       	 if (prodID != null && prodID.trim().length() > 0) {
           mline.setProdID(prodID.trim());
          }
     
       	 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
    	  mline.setProdGroup(prodGroup.trim());
          mline.setDiscGroup(prodGroup.trim());
      }
      
 

      if (price != 0) {
           mline.setPrice(price);
         }else if (disc != 0) {
           mline.setDiscount(disc);
         }
      
      if( (mline.getPrice() > 0.05 && mline.getPrice() < 1110 )|| mline.getDiscount() > 0){
          result.add(mline);    
          
          if(clGroup != null && clGroup.trim().length() >0 && custs.get(clGroup.trim()) != null){
          	Vector vv = (Vector)custs.get(clGroup.trim());
          	for (int i =0 ; i< vv.size();i++){
          		String aa = (String)vv.get(i);
          		MatrixLine mm = new MatrixLine();
          		mm.setCustID(aa);
          		mm.setPrice(mline.getPrice());
          		mm.setProdID(mline.getProdID());
          		 if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                		mm.setProdGroup(mline.getProdGroup()); 
          		 }
          		 result.add(mm); 
          	 }
          }
       }
    }
    
    stmt.close();
    con.close();
    }catch(Exception e){
 	 e.printStackTrace();
    }
	
   return result;

   }

   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   

   public void insertDefective(DefectiveWrapper wr){
   
  }
   
	public Vector getLastThreeData() {
		return null;
	}

	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																	// C(50),
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
				+ "?," // resint3 I,
				+ "?)"; // country L

		try {
			System.out.println("jam");
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4,  Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6,  Converter.deCode(lw.getLine().getProdName()));
				ps.setDouble(7, lw.getLine().getNormalPrice() );
				ps.setDouble(8, lw.getLine().getAmount() );
				ps.setDouble(9, 0);
				ps.setDouble(10, 0);
				ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
				ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
				ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
				ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
				ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
				ps.setBoolean(16, lw.getLine().isAction());
				ps.setBoolean(17, lw.getLine().isPartner());
				ps.setBoolean(18, lw.getLine().isBrand());
				ps.setBoolean(19, lw.getLine().isGroup());
				ps.setDouble(20, lw.getLine().getAction1Eval());
				ps.setDouble(21, lw.getLine().getAction2Eval());
				ps.setDouble(22, lw.getLine().getActionTotalEval());
				ps.setDouble(23, lw.getLine().getCustEval());
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24,  Converter.deCode(cc));

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25,  Converter.deCode(cd));

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27,  Converter.deCode(cf));
				ps.setInt(28, lw.getLine().getDealNr());
				ps.setInt(29, lw.getLine().getIdealNr());

				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
				ps.setString(30, cr1);

				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}

				

				ps.setString(32, cr3);
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				ps.setString(36, "et");
				ps.executeUpdate();
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public String getDateStr(Calendar cal) {

		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}


	public String getDateStr() {
		Calendar cal = Calendar.getInstance();
		String month = (cal.get(Calendar.MONTH) + 1) + "";
		String day = (cal.get(Calendar.DAY_OF_MONTH)) + "";
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return cal.get(Calendar.YEAR) + "" + month + day;
	}
	
	 public String getProductSortingData(){
	   	return null;
	   }

   private String ensureLen(String str, String replace){
    if (str == null) str = "";
    else str = str.trim().replace('\'', ' ').replace('\"', ' ').replace(';',' ');
    if (str.length() == 0){
      return replace;
    } else {
      return str;
    }
  }

   public ManagerData getManagerData() throws SQLException{

    String tableName = "dayreport";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }

    String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu," +
    		"text AS tekst,'' AS dealer,clgroup AS agentname" +
        " FROM  " + tableName +
        " WHERE sentdate>(GETDATE()-30) ORDER BY clid ";

   if (isDebug()) System.out.println(sql);
   this.con = getNewConnection();
   Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    StringBuffer sb = new StringBuffer();
    String sep = ";";

    while(rs.next()){
      String cliID = rs.getString("klient").trim();
      sb.append(cliID + sep);
      sb.append(rs.getString("kuupaev").trim() + sep);
      sb.append(getNF().format(rs.getDouble("summa")) + sep);
      sb.append(rs.getInt("ridu") + sep);
      String tekst = ensureLen(rs.getString("tekst")  ," ");
      tekst = tekst.replace('\'', ' ').replace('\"', ' ');
      tekst = tekst.replace('\n', ' ');
      String agendinimi = rs.getString("agentname");
      sb.append(tekst + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(ensureLen((agendinimi), " ") + sep);
        sb.append(ensureLen((" "), " ") + sep);
        sb.append(ensureLen((""), " ") + sep);
        sb.append((" ") + sep);
        sb.append((" ") + sep);
      sb.append("\n");
    }

    ManagerData md = new ManagerData();
    md.setClientVisitInfo(sb.toString());

 StringBuffer bf2 = new StringBuffer();
 Enumeration enums = klient.elements();
 while(enums.hasMoreElements()){
 	Customer cust = (Customer)enums.nextElement();
 	Iterator agen = cust.getAgents().keySet().iterator();
 	while(agen.hasNext()){
    Agent ag = 	(Agent)agents.get((String)agen.next());
   bf2.append(cust.getID() + sep);
   bf2.append(ag.getID() + sep);
   bf2.append(ag.getName() + "\n");
 }
	}

    md.setClientAgentInfo(bf2.toString());
    return md;

  }


   public void insertDistribution(DistEvalListWrapper wr){
   
   }

 
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
        /*   boolean insertassort = true;
           
           try {
        	   String sql = "SELECT sortdate from mj_assortment where  year(sortdate) = year(getdate()) and" +
        	   		" month(sortdate) = month(getdate()) and day(sortdate) = day(getdate()) and country ='et' ";
         	 
         	    Connection cons = this.getNewConnection();
     	        Statement stmts = cons.createStatement();
     	        ResultSet rss = stmts.executeQuery(sql);
     	       
     	        if(rss.next()){
     	        	insertassort = false;
     	        }   
     	        
     	        rss.close();
     	        stmts.close();
     	        cons.close();
     	        
           }catch(Exception d){
        	   d.printStackTrace();
           }
     	
           
           try {
          	  String cmd = "  select s.[Item No_] as toode,s.[Assortment Code] + s.[Assortment Level] as grupp from " +
          	  		" [Viï¿½iunai Baltic$Item Assortment Level] s INNER JOIN [Viï¿½iunai Baltic$Item] t on t.No_= [Item No_] " +
          	  		" ORDER BY [Search Description]";
          	System.out.println(cmd);
             	 Connection con = getNewConnection();
         	        Statement stmt = con.createStatement();
         	        ResultSet rs = stmt.executeQuery(cmd);
         	        while(rs.next()){
                            String custID = " ";
                            String clGroup = " ";
                            String prodID = " ";
                            String prodGroup = " ";
                            MatrixLine mline = new MatrixLine();
                            prodID = rs.getString("toode");
                            clGroup = rs.getString("grupp");
                         

                            if (custID != null && custID.trim().length() > 0) {
                              mline.setCustID(custID.trim());
                            }
                            if (clGroup != null
                                && clGroup.trim().length() > 0) {
                              mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
                            }

                            if (prodID != null && prodID.trim().length() > 0) {
                              mline.setProdID(prodID.trim());
                            }
                            if (mline.getProdID() == null && prodGroup != null
                                && prodGroup.trim().length() > 0) {
                              mline.setProdGroup(prodGroup);
                            }
                            if(insertassort){
	                               insertSortLine(mline);   
	                           }
                            lines.add(mline);
                          }

         	       rs.close();
         	       con.close();
                         } catch (Exception e) {
                             getParent().showError(e);
                         }*/
           return lines;
   }
   
   

   private void  insertSortLine(MatrixLine mline){
		  try{
				
			      String sql = "INSERT INTO mj_assortment values (" +
			         "'" + mline.getCustID() + "'," + 
			         "getdate()," + 
			         "'" + mline.getCliGroup() + "'," + 
			         "'" + mline.getProdID() + "'," + 
			         "0," + 
			         "0,'','et')" ;
			  
			  	     Connection  con2 = this.getNewConnection();      
				     Statement stmt2 = con2.createStatement();
				     stmt2.executeUpdate(sql);
				     con2.close();
		  }catch(Exception e){
			  e.printStackTrace();
		  }		  
	  }

   public DealerProduct DealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Viï¿½iunai Baltic$Item Ledger Entry] l " +
          " INNER JOIN [Viï¿½iunai Baltic$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('TAL') " +
          " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupï¿½s kodas])>0 " +
          " GROUP BY l.[Location Code],l.[Item No_] ORDER BY l.[Item No_] ";
       

       	 Connection con = getNewConnection();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	             String toode = null;
   	             double seis = 0;;
   	             String ladu  = null;
   	              	toode =rs.getString("kood");
   	              	seis = rs.getDouble("kogus");
   	              	ladu = rs.getString("ladu");
   	                 dealerProds.add(ladu, toode,seis+""," "," " );
   	        }

   	      rs.close();
   	      con.close();

   	    System.out.println(cmd);
    } catch (Exception e) {
        e.printStackTrace();
    }
     return dealerProds;
   }
   public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }


   private void saveFile(String fileName,String content ){
		File fil = new File( getImportFile() + "reponse/");
   	if(!fil.exists()){
   		fil.mkdir();
   	}
 try{
		PrintWriter os = new PrintWriter(new FileWriter( fileName , true));
		os.write(content);
		os.close(); 

       System.out.println("Faili salvestus " + fileName); 
 }catch(Exception r){
	  r.printStackTrace();
 }
     
        
 }
   
   public static Document loadXMLFromString(String xml) throws Exception
   {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       DocumentBuilder builder = factory.newDocumentBuilder();
       InputSource is = new InputSource(new StringReader(xml));
       return builder.parse(is);
   }
   
   private  Node getNode(Node node, String name){
	    for (int i = 0 ; i < node.getChildNodes().getLength() ; i++){
	      Node nd = node.getChildNodes().item(i);
	  //    System.out.println(nd.getNodeName());
	      if (nd.getNodeName().equals(name)){
	        return nd;
	      }
	    }
	    return null;
	  }

   private TreeMap<String,Double> stocks = new TreeMap<String,Double> ();
   private TreeMap<String,Vector> stocksdet = new TreeMap<String,Vector> ();
   
   public String getSaveStockInfo(){
  StringBuffer products = new StringBuffer();
	   stocks = new TreeMap<String,Double> ();
		try {
	
		
			String cmd = "  SELECT preke , (kiekis)   FROM likuciai where kiekis >0 and sandelis='PL' order by preke ";
	     	 
	       	 Connection con = getNewConnection();
	 
	   	        Statement stmt = con.createStatement();
	   	        ResultSet rs = stmt.executeQuery(cmd);
	   	        while(rs.next()){
	   	        
      			 String id =  rs.getString("preke").trim();
      			double kogus =  rs.getDouble("kiekis");
      		
      				if(stocks.get(id)!= null){
      					stocks.put(id, stocks.get(id) + kogus);
      				}else{
      					stocks.put(id, kogus);
      				}
      			
	   	        }
      			
      		   
     	  }catch(Exception e){
     	   e.printStackTrace();	  
     	  }
     
    Iterator itera = stocks.keySet().iterator();
    while (itera.hasNext()){
    	String id = itera.next() + "";
    	 products.append( id+  ";");
           products.append(" ;");
           products.append(getParent().getNF().format(stocks.get(id)) + "; "  +  "\n"); // + dealer +  "\n");
    }
		
	   
    return products.toString();
   }

  HashMap ketid = new HashMap();
 
	  

   
   public Vector getDebt(String agentCode){
        return this.debtList;
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
     //setTreeGroup(true);
     String cmd = "  SELECT  tip_id AS kood,tipas AS nimi  FROM tipai order by tipas";
     	 
       	 Connection con = getNewConnection();
 
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood").trim();
   	                String name = rs.getString("nimi").trim() ;
   	             tm.put(id, Converter.enCodeEst(name));
   	        }
   	        
   	      rs.close();
   	      con.close();

         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
	   String cmd = "  SELECT  grup_id AS kood,grupe AS nimi  FROM grupes order by grupe";
	   TreeMap   tm = new TreeMap();
     	/* Connection con = getNewConnection();

 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	            String id = rs.getString("kood").trim();
 	                String name = rs.getString("nimi").trim() ;
 	             tm.put(id, Converter.enCodeEst(name));
 	        }
 	        
 	      rs.close();
 	      con.close();*/

       return tm;
    }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}

	   return idx;
   }

   public TreeMap getAgents(){
	
        String cmd = " SELECT tab_nr AS kood,CONCAT(pavarde,' ' ,vardas) AS nimi,'' AS objekt, properties as dubs FROM darbuot ";
	    if (isDebug()) System.out.println(cmd);
	    agents1 = new TreeMap();
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt"));
	          
	          agent.setName(name);
	          agent.setIsPilot(true);
	          
	          String dubs = rs.getString("dubs");
	          
	          if(dubs != null && dubs.trim().length() > 1){
	        	  dubs = dubs.trim();
	        	  if(dubs.indexOf(",") != -1 ){
	        		  StringTokenizer stag = new StringTokenizer(dubs, ",");
					    while(stag.hasMoreTokens()){  	
		                 agent.addDubbler(stag.nextToken());
		                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
					    }  
	        	  }else  if(dubs.indexOf(";") != -1 ){
	        		  StringTokenizer stag = new StringTokenizer(dubs, ";");
					    while(stag.hasMoreTokens()){  	
		                 agent.addDubbler(stag.nextToken());
		                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
					    }  
	        	  }else{
	        		  agent.addDubbler(dubs.trim()); 
	        		   System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
						  
	        	  }
	        	 
	          }

	          agent.setLanguage("EST");
	          agent.setCountry("ee");
	          agent.setPassWord("0000");
	          
             String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
             if(email.length() > 3){
            	 agent.setEmail(email);
             }
             if(email.length() > 0){
            	 agent.setActive(true);
             }else{
            	 agent.setActive(false);
             }
        
	          
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
	        Agent aae = new Agent("HOUSE2");
	        aae.setName("office2");
	        agents.put("office2", aae);
            rs.close();
	        con.close();
	        
	        
	
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }

   public TreeMap getCustomers(Agent agent){

	     TreeMap discs = new TreeMap(); 
 try {
	 Converter.getManagersReplacements(agents1, rcompany, user , pass);
	 String cmd = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS summa FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " +
      " where a.terminas < CURRENT_DATE()  group by b.kodas having sum(skola_w) != 0 " ;
System.out.println(cmd);
   Connection   cons = this.getNewConnection();
     Statement stmt1 = cons.createStatement();
     ResultSet rs1 = stmt1.executeQuery(cmd);
     while(rs1.next()){

       String id = rs1.getString("maksja");
       String kl = rs1.getString("klient");
       String summa = rs1.getString("summa");
       if(id != null)
       overdebts.put(id,summa);
       if(kl != null)
       overdebts.put(kl,summa);
  }

     rs1.close();
     stmt1.close();
     cons.close();
	 
     
     
     
     
	 String cmd2 = " select  b.kodas AS maksja,b.klient_id as klient,sum(skola_w) AS summa FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " +
     "   group by b.kodas having sum(skola_w) != 0 " ;
System.out.println(cmd2);
  Connection   cons2 = this.getNewConnection();
    Statement stmt12 = cons2.createStatement();
    ResultSet rs12 = stmt12.executeQuery(cmd2);
    while(rs12.next()){

      String id = rs12.getString("maksja");
      String kl = rs12.getString("klient");
      String summa = rs12.getString("summa");
      if(id != null)
      totaldebts.put(id,summa);
      if(kl != null)
      totaldebts.put(kl,summa);
 }

    rs12.close();
    stmt12.close();
    cons2.close();
     
     
     cmd = " select klient_id AS custid, procentas as disc " +
    " from  nuolaid ale inner join klientai k  ON ale.klientas=k.klientas left join  grupes g on g.grupe=ale.grupe " +		
	"	where   data_iki is null  and (procentas != 0 ) and (preke is null and grup_id is null) group by klient_id" ;
System.out.println(cmd);
     cons = this.getNewConnection();
     stmt1 = cons.createStatement();
     rs1 = stmt1.executeQuery(cmd);
    while(rs1.next()){

     
      String kl = rs1.getString("custid");
      String dis = rs1.getString("disc");
      discs.put(kl, dis);
  
 }

    rs1.close();
    stmt1.close();
    cons.close();
   
    

     
     
     
     
     
       cmd = "  select b.klient_id AS klient, b.kodas AS maksja,kitas_dok AS number, " +
	  "   a.Terminas AS kuupaev, a.data as kuup, skola_w AS summa, DATEDIFF(terminas,Current_DATE()) AS due ,b.kodas AS payer FROM apyvarta a INNER JOIN klientai b ON b.klientas=a.gavejas " + 
      "  where terminas is not null   AND skola_w != 0 " +
	  "   and (kitas_dok) > 0  and Operacija not like '%Cash%'"; 

       
      ketid = new HashMap();
      sortimendid = new HashMap();
  System.out.println(cmd);
       this.debtList = new Vector();
     Connection   con = getNewConnection();
        stmt1 = con.createStatement();
        rs1 = stmt1.executeQuery(cmd);
       while(rs1.next()){
        Debt debt = new Debt();
       String custID = rs1.getString("klient");
        debt.setCustID(custID.trim());
        
        
try{
	 String doknr = rs1.getString("number").trim();    
     debt.setDoknr(doknr);
}catch(Exception e){}
        debt.setDuedate(rs1.getDate("kuupaev"));
        debt.setDate(rs1.getDate("kuup"));
        debt.setSum(rs1.getDouble("summa"));
        int dueint = -1 * rs1.getInt("due");
        debt.setDue(dueint + "");
        int old = 0;
        if(debtDays.get(custID) != null){
         old = Integer.parseInt((String)debtDays.get(custID));
        }
        
        if(old<dueint){
           debtDays.put(custID,dueint+"") ;
           if(dueint > 49){
        	// this.blockedcusts.add(custID);  
           }
        }
       // System.out.println("get debt" + debt.getCustID());
        if(debt.getDoknr() != null){
        	 this.debtList.add(debt);
        }
       
            }

       rs1.close();
       con.close();
      } catch (Exception e){
        e.printStackTrace();
      }
/// KLIENDI ANDMETE LEIDMINE

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();
  	TreeMap freeze = new TreeMap();


  	String agextra = getParent().getProps().getProperty("all.customers");

   String sql =  "  SELECT k.klientas  AS nimi,k.klient_id AS kood,k.kodas AS payer,k.Adresas AS aadress ,k.tab_nr AS agent ,k.dienos as paymterm, " + 
  "  filialas AS blokk, 0 AS yle,'' AS visit,kainos_nr AS type, " +
	"  atstovas as kontakt , k.kl_grupe as kett , max_skola as credit, kainos_nr as hinnak ,telefonai as phone , e_mail as email, 0 as maxdisc , '' as mars " +
	" ,k.kl_tipas as type2 ,k.kl_grupe as rcatrgory , k.miestas AS rcity , k.indeksas AS rindex , k.sritis AS rmaakond , salis as rcountry,atstovas as rkontakt "+ 
	" ,telefonai as rperphone, e_mail as remail " +
	"  FROM klientai k   where  k.tab_nr > 0 " ;

   System.out.println(sql);
   try {
        Connection con = getNewConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
         String name = rs.getString("nimi").trim();
           String id = rs.getString("kood").trim();
     
           Customer customer = new Customer(id);
           customer.setCountry("Estonia");
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.restName = name;
           customer.setName(Converter.enCodeEst(name));
           	payer = rs.getString("payer");
           	if(payer != null && payer.trim().length()>0){
             customer.setPayer(payer.trim());
             customer.setRegnr(payer.trim());
             }
           	
           	if(discs.get(customer.getID()) != null){
           		customer.setDiscount(Double.parseDouble((String)discs.get(customer.getID())));
           	}

             String aadress = rs.getString("aadress");
             if( aadress != null && aadress.trim().length()>0){
            
               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress.trim()),"Estonia"));
               customer.setName(customer.getName()+ " " + Converter.enCodeEst(aadress.trim()));
               customer.restStreet = aadress.trim();
            		   
             }
         	String ag = rs.getString("agent");
             customer.addAgent(new Agent(ag.trim()));
             customer.setMainAgent(ag.trim());
             if(ag != null && !ag.trim().equals("13")){
            	 customer.addAgent(new Agent("13"));
             }
             customer.addAgent(new Agent("HOUSE1"));
             customer.addAgent(new Agent("HOUSE2"));
             if ( ag != null && agents1.get( ag.trim()) != null){
                 Agent aa = (Agent) agents1.get( ag.trim());
                  Vector vec = aa.getDubbler();
                  if( vec!= null && vec.size() > 0 ){
                       Enumeration en = vec.elements();
                       while(en.hasMoreElements()){
                            String ags = (String) en.nextElement();
                         customer.addAgent( new Agent(ags));
                      }
                 }
           }
             if(agextra != null){
            	 StringTokenizer st = new StringTokenizer(agextra,",");
            	 while(st.hasMoreTokens()){
            		 customer.addAgent(new Agent(st.nextToken()));
            	 }
             }
             
             //dublantide lisamine 
             if (ag != null && ag.trim().length()>0){
 				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
 				
 				if(agg != null && agg.getDubbler().size()>0){
 					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
 						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
 					}
 				}
 			}
             
             //Eraiski lisamine 
             if(name.indexOf("Eraisik") != -1){
            	 System.out.println("ERAISISK");
            	 Iterator itera = this.getAgents1().keySet().iterator();
            	 while (itera.hasNext()){
            	  	 Agent agg = ((Agent)this.getAgents1().get(itera.next()));
            	    	customer.addAgent(agg); 
            	    	 System.out.println("ERAISISK lisan agenti " + agg.getID());
            	 }
             }
             
             
             String paym = rs.getString("paymterm");
             
             
             String blokk = rs.getString("blokk");

         

        /*   	String num = rs.getString("alekood");
           	if(num != null && num.trim().length()>0){
             customer.setDiscount(Double.parseDouble((num.trim())));
           	}
*/

      		String visit = rs.getString("visit");
             if(visit != null && visit.trim().length()>0){
                customer.setVisit(visit.trim());
             }

            String type = rs.getString("type");
            if(type != null && type.trim().length()>0){
            		customer.setCustType(type.trim());
            }

           
   

            if(customer.getPayer() != null  && this.overdebts.get(customer.getPayer()) != null){
            	customer.setDebt(Double.parseDouble((String)overdebts.get(customer.getPayer())));
            }else if( this.overdebts.get(customer.getID()) != null){
            	customer.setDebt(Double.parseDouble((String)overdebts.get(customer.getID())));
            }
            	
          
            
            	
   
          
           if(paym != null && paym.trim().length()>0){
        	  customer.setPaymTerm(paym);
             }

           
           
          //
           String cont = rs.getString("kontakt");
         
           String kett = rs.getString("kett");
           if(kett != null && kett.trim().length()>0){
        	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
           		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett.trim()));
           		vv.add(customer.getID());
           		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
           	}else{
           		Vector vv = new Vector();
           		vv.add(customer.getID());
           		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
           	}
           }
           
           double credit = rs.getDouble("credit");
           if(credit > 0){
        	   customer.setCredit(credit + "");
           }
         
           
           if(customer.getPayer() != null  && this.totaldebts.get(customer.getPayer()) != null){
           	customer.setUsedCredit(Double.parseDouble((String)totaldebts.get(customer.getPayer())));
          if(credit > 0 && (credit - Double.parseDouble((String)totaldebts.get(customer.getID()))) < -100){
            	customer.setSalesBlock(true);
            	blockedcusts.add(customer.getID());
            }
           }else if( this.totaldebts.get(customer.getID()) != null){
           	customer.setUsedCredit(Double.parseDouble((String)totaldebts.get(customer.getID())));
            if(credit > 0 && (credit - Double.parseDouble((String)totaldebts.get(customer.getID()))) < -100){
            	customer.setSalesBlock(true);
            	blockedcusts.add(customer.getID());
            }
           }
           	
           if( blokk != null && blokk.trim().equals("1")){
               customer.setSalesBlockT(true);
               blockedcusts.add(customer.getID());
             }

           
           
           String phone = rs.getString("phone");
           String email = rs.getString("email");
           
           if(cont != null && cont.trim().length()>0){
               customer.setPersonalData(Converter.enCodeEst(cont.trim().replaceAll(";", ",")));   
           }
           
           if(phone != null && phone.trim().length()>0){
               customer.setPersonalData( customer.getPersonalData() + " " + Converter.enCodeEst(phone.trim().replaceAll(";", ",")));   
           }
           
           if(email != null && email.trim().length()>0){
               customer.setPersonalData( customer.getPersonalData() +  " " + Converter.enCodeEst(email.trim().replaceAll(";", ",")));   
           }
           
           
           double maxdisc = -1;
           try{
        	   maxdisc =  rs.getDouble("maxdisc");
           }catch(Exception g){
        	   maxdisc = 0;
           }
           
           if(maxdisc  != 99){
        	 //  customer.setMaxdisc(maxdisc);
           }
           
           String mars = rs.getString("mars");
           if(mars  != null && mars.trim().length() > 0){
        	//   customer.setCity("Marsrutai dienos :" + mars.trim());
           }
           
           String type2 = rs.getString("type2");
           if(type2 != null && type2.trim().equals("K")){
        	  // customer.setMaxdisc(15);
           }
           
           String rcatrgory = rs.getString("rcatrgory");
           if(rcatrgory != null && rcatrgory.trim().length() > 0){
        	   customer.category = rcatrgory;
           }
           
           String rcity = rs.getString("rcity");
           if(rcity != null && rcity.trim().length() > 0){
        	   if(customer.restStreet != null && !customer.restStreet.toLowerCase().contains(rcity.trim().toLowerCase())){
        		 //  customer.restCity = rcity;
        	   }
           }
           //
           
           String rindex = rs.getString("rindex");
           if(rindex != null && rindex.trim().length() > 0){
        	   if(customer.restStreet != null && !customer.restStreet.toLowerCase().contains(rindex.trim().toLowerCase())){
        		   customer.restZipCode = rindex;
        	   }
           }
           
           String rcountry = rs.getString("rcountry");
           if(rcountry != null && rcountry.trim().length() > 0){
        	   if(customer.restStreet != null && !customer.restStreet.toLowerCase().contains(rcountry.trim().toLowerCase())){
        		   customer.setCountry(rcountry);
        	   }
           }
           
           String atstovas = rs.getString("rkontakt");
           if(atstovas != null && atstovas.trim().length() > 0){
        	      customer.restPerson = (atstovas);
        	   
           }
           
           
           String rperphone = rs.getString("rperphone");
           if(rperphone != null && rperphone.trim().length() > 0){
        	      customer.restPerPhone = (rperphone);
        	   
           }
           
           String remail = rs.getString("remail");
           if(remail != null && remail.trim().length() > 0){
        	      customer.restPerEmail = (remail);
        	   
           }
          
           
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           
         }

        rs.close();
        con.close();



     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   public TreeMap getAgents1() {
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   private HashMap prodHash = new HashMap();
   
   private void getPartiid(){
	   
	   try {
			  
	    	 String cmd =  "  select preke as toode , kiekis as kogus , sandelis as ladu ,artikulas as bb from likuciai where  kiekis != 0";
	    		  System.out.println(cmd);
	    	 
	    	 
		

	      	 Connection con = getNewConnection();
	  	        Statement stmt = con.createStatement();
	  	        ResultSet rs = stmt.executeQuery(cmd);
	  	        while(rs.next()){
              String id = rs.getString("toode");
              double kogus = rs.getDouble("kogus");
              String ladu = rs.getString("ladu");
              String partii = rs.getString("bb");
            
              if(partii != null){
            	if(stocksdet.get(id)!= null){
	  	  				Vector<Toode> vv = stocksdet.get(id);
	  	  				Toode tt = new Toode(id);
	  	  				tt.setKogus(kogus);
	  	  				tt.setLadu(ladu);
	  	  				tt.setBrand(partii.trim());
	  	  				vv.add(tt);
	  	  				stocksdet.put(id, vv);
	  	  			}else{
	  	  				Vector<Toode> vv = new Vector<Toode>();
	  	  				Toode tt = new Toode(id);
	  	  				tt.setKogus(kogus);
	  	  				tt.setLadu(ladu);
	  	  				tt.setBrand(partii.trim());
	  	  				vv.add(tt);
	  	  				stocksdet.put(id, vv);
	  	  			}
                  }
	  	        }

	  	      rs.close();
	  	      con.close();
	         System.out.println(cmd);
	   
	     } catch (Exception e) {
	       e.printStackTrace();
	   }
	   
	   
   }
   
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     HashSet  ladutr = new HashSet();
     HashMap<String,Double>  hinnad = new HashMap<String,Double>();
     getSaveStockInfo();
     getPartiid();
      prodHash = new HashMap();
     try {
	  
    	 String cmd =  " select * from (	SELECT  r.preke AS toode  FROM apyv_gr r " +
    		" INNER JOIN apyvarta d ON r.apyv_id= d.apyv_id and DATE_ADD(data,interval 150 day) > current_date() " +
    		" INNER JOIN klientai b ON b.klientas=d.gavejas " +
    		" where DATE_ADD(data,interval 150 day) > current_date() order by klient_id ,r.preke ,data desc ) as tab group by toode";
    		 
    	 
    	 System.out.println(cmd);
    	 
    	 
      	 Connection con = getNewConnection();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("toode");
  	              	ladutr.add(toode);
  	        }

  	      rs.close();
  	      con.close();
         System.out.println(cmd);

         
         String cmdh =  "   SELECT preke , savikaina   FROM likuciai where kiekis >0 and sandelis='PL' order by preke,data   ";
 		 
 	 
 	 
   	  con = getNewConnection();
	       stmt = con.createStatement();
	       rs = stmt.executeQuery(cmdh);
	        while(rs.next()){
	             String toode = rs.getString("preke");
	             double hind = rs.getDouble("savikaina");
	              	
	              	hinnad.put(toode,hind);
	        }

	      rs.close();
	      con.close();

         
         
         
     } catch (Exception e) {
       e.printStackTrace();
   }
     
     Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     try {
     	String cmd = " SELECT t.preke AS toode,t.prek_id as id,t.pavad AS nimi, t.pard_kaina AS myygihind , kaina_1 as hind1, kaina_2 as hind2,  kaina_3 as hind3, kaina_4 as hind4, " +
 " kaina_5 as hind5, kaina_6 as hind6, kaina_7 as hind7, kaina_8 as hind8, kaina_9 as hind9, kaina_10 as hind10,grup_id AS grupp ,tip_id AS tupe ,t.tipas  " +
	"		 , t.mato_vien AS unit, t.bar_kodas AS ean,0 AS status ,t.max_nuol AS maxdiscount,  " +
	"		  t.tipas AS type, t.orig_pak AS kastis,  t.prek_neto AS kaal,21 AS vat,  pak_kiek as opt ,prek_mode as info ,'' as staatus  " +
	"		  FROM prekes t inner join grupes g on g.grupe=t.grupe inner join tipai ty on ty.tipas=t.tipas " +
	"		   ";
     	
      System.out.println(cmd);
     	Connection con = getNewConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
        	boolean goes = false;
            String id = rs.getString("toode").trim();
            String weight ="";
            Product prod = new Product(id);
            prod.setStock(0);
            

            if(ladutr.contains(prod.getID())){
           	 goes = true;
            }else{
           	 goes = false;
            }
            
            if(stocks.get(id) != null){
            	prod.setStock(stocks.get(id));
            	goes = true;
            }
            
            if(stocksdet.get(id) != null){
            	Vector<Toode> vv = stocksdet.get(id);
            	for(int i = 0; i < vv.size();i++){
            	 Toode tt = vv.elementAt(i)	;
            	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
            		 prod.setPartii(prod.getPartii() + tt.getLadu() + ":" + tt.getKogus() + " BB:" + tt.getBrand() + "|");	
            	 }else{
            		 prod.setPartii("|" + tt.getLadu() + ":" + tt.getKogus() + " BB: " + tt.getBrand() + "|");	 
            	 }
            	
            	}
            	
            }
         	String name = rs.getString("nimi").replaceAll("ritolumaa", "m");
         	name = name.replaceAll("gipiirkond", " ");
         	name = name.replaceAll("tootja", "to");
         	name = name.replaceAll("ean:", " ").trim() + " " + id;
         	name = name.replaceAll(":", ".");
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
             double price = rs.getDouble("myygihind");
             if(price != 0 && price != 321.77 && price < 1111){
            	 prod.setPrice(price); 
             }
             
             double price1 = rs.getDouble("hind1");
             if(price1 != 0 && price1 != 321.77 && price1 < 1111 && prod.getPrice() < price1){
            	 prod.setPrice(price1); 
             }
             double price2 = rs.getDouble("hind2");
             if(price2 != 0 && price2 != 321.77 && price2 < 1111 && prod.getPrice() < price2){
            	 prod.setPrice(price2); 
             }
             double price3 = rs.getDouble("hind3");
             if(price3 != 0 && price3 != 321.77 && price3 < 1111 && prod.getPrice() < price3){
            	 prod.setPrice(price3); 
             }
             double price4 = rs.getDouble("hind4");
             if(price4 != 0 && price4 != 321.77 && price4 < 1111 && prod.getPrice() < price4){
            	 prod.setPrice(price4); 
             }
             double price5 = rs.getDouble("hind5");
             if(price5 != 0 && price5 != 321.77 && price5 < 1111 && prod.getPrice() < price5){
            	 prod.setPrice(price5); 
             }
             double price6 = rs.getDouble("hind6");
             if(price6 != 0 && price6 != 321.77 && price6 < 1111 && prod.getPrice() < price6){
            	 prod.setPrice(price6); 
             }
             double price7 = rs.getDouble("hind7");
             if(price7 != 0 && price7 != 321.77 && price7 < 1111 && prod.getPrice() < price7){
            	 prod.setPrice(price7); 
             }
             
             double price8 = rs.getDouble("hind8");
             if(price8 != 0 && price8 != 321.77 && price8 < 1111 && prod.getPrice() < price8){
            	 prod.setPrice(price8); 
             }
             double price9 = rs.getDouble("hind9");
             if(price9 != 0 && price9 != 321.77 && price9 < 1111 && prod.getPrice() < price9){
            	 prod.setPrice(price9); 
             }
             double price10 = rs.getDouble("hind10");
             if(price10 != 0 && price10 != 321.77 && price10 < 1111 && prod.getPrice() < price10){
            	 prod.setPrice(price10); 
             }
             
             
             // panime selle peale myygi hinna 
             if(price8 != 0 ){
            	 prod.setPrice(price8); 
             }
             
             if(prod.getPrice() > 0 ){
            	 goes = true;
             }
             
                
             
              String grupp = cleardots(rs.getString("grupp"));
                String tupe = cleardots(rs.getString("tupe"));
                String typenimi =  Converter.cleanLitLatEst(cleardots(rs.getString("tipas")));
                String sortname =  typenimi.substring(0,3) + " " + name;
                prod.setGroup(tupe);
                //prod.setGroupSecond(grupp);

        	//	String grupp2 = cleardots(rs.getString("grupp2"));
    			//                prod.setGroupSecond(grupp2);
                String unit = (rs.getString("unit"));
                if (unit != null && unit.trim().length()>0){
                  prod.setUnit(unit.trim());
                }
                
              String ean = (rs.getString("ean"));
                 if (ean != null && ean.trim().length()>0){
                   prod.setEAN(ean.trim());
                 }
                
             
                 

                 if(hinnad.get(prod.getID()) != null){
                	// prod.setPrice(hinnad.get(prod.getID()) / 0.62);
                	 prod.setMinPrice(hinnad.get(prod.getID()));
                 }
                 
                 
                 
             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                 // prod.setMaxDiscount(Double.parseDouble(discou));
             	 // prod.setMinPrice(0);
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                   prod.setInCases(cases);
                	}

             	weight = rs.getString("kaal");
                           if(weight != null && weight.trim().length()>0){
                             weight = weight.trim();
                             if(weight.indexOf('.')==0){
                             	weight = "0" + weight;
                             }
                               }

          	 String vat = rs.getString("vat");
                   if(vat != null && vat.trim().length()>0){
                     	prod.setVat("20");
                   }
                  
                   
                   double opt = rs.getDouble("opt");
                   if (opt > 0){
                	   prod.setOptAmountInUnits(opt);
                   }
             String info = rs.getString("info");
                 if(info != null && info.trim().length()>0){ 
                	 if(prod.getPartii() != null && prod.getPartii().trim().length() > 0){
                		 prod.setPartii(prod.getPartii() + "Pirkimai: " +info.trim()); 
                	 }else{
                		 prod.setPartii("Pirkimai: " +info.trim()); 
                	 }
                	
                 }
                 String stattus = rs.getString("staatus");
                 if(stattus != null && stattus.trim().length()>0){
                	 if(stattus.trim().toLowerCase().indexOf("a") != -1){
                		// prod.setStatusDiscount("1");
                	 }
                	 if(stattus.trim().toLowerCase().indexOf("n") != -1){
                		// prod.setStatusNew("1");
                	 }
                	
                 }
                   if(goes ){
                	   prod.setName(Converter.enCodeEst(name));
                	   prod.setNameSorting(Converter.cleanLitLatEst(name));
                       tm.put(sortname, prod);
                       prodHash.put(prod.getID(), prod);  
                   }
                
        }

        rs.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   
   private String getDir(){
	      String dir = getParent().getProps().getProperty("readin");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
	    }

	public static void saveObject(Object obj, String file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(obj);
		oos.close();
	}
   
   public static String deCode(String res ){

	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
   /* StringBuffer sb = new StringBuffer();
     String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [Viï¿½iunai Baltic$Item] " +
     		" WHERE len([Type])>0  ORDER BY [Net Weight] , [Search Description]";
    try {
     	 Connection con = getNewConnection();
     	System.out.println(cmd);
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
        String id = rs.getString("toode");
        String type = rs.getString("type");
			//ABCD
			int i = type.trim().length();
			if(i>0){
			while (i != 0){
				sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
				i = i -1;
			 }
			}
	       }

	        rs.close();
	        con.close();
       } catch (Exception e){
      e.printStackTrace();
    }
    return sb.toString();*/
    return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  " select * from (	SELECT  b.klient_id AS klient,r.preke AS toode,(kiekis) AS kogus, Data AS lastdate, " +
    		" kaina as price FROM apyv_gr r " +
            " INNER JOIN apyvarta d ON r.apyv_id= d.apyv_id and DATE_ADD(data,interval 180 day) > current_date()  INNER JOIN klientai b ON b.klientas=d.gavejas " + 
	        " where DATE_ADD(data,interval 180 day) > current_date() order by klient_id ,r.preke ,data desc ) as tab group by klient,toode";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
    Connection con = getNewConnection();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();
               String prodID = rs.getString("toode").trim();
                 String lastdate = " ";
             
                 double  amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                    double price = rs.getDouble("price");
               
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate + "_" + price + "€",price,0,0," ");

                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);

           }
         rs.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  b.klient_id AS klient,r.preke AS toode,r.kiekis AS kogus, data AS lastdate  FROM  apyv_gr r " +
		  " INNER JOIN apyvarta d ON r.apyv_id = d.apyv_id INNER JOIN klientai b ON b.klientas=d.gavejas WHERE  DATE_ADD(data,interval 30 day) > current_date() " +
		  " Order by b.klient_id ,r.preke,d.data desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
		    Connection con2 = getNewConnection();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = rs2.getString("klient").trim();
               String toode = rs2.getString("toode").trim();
               String kogus = "0";
               String date = " ";
                  kogus = (rs2.getDouble("kogus")+0.99)+"";
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(","));
				  }
                   date = rs2.getString("lastdate").trim().substring(0,10);

               if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
               }
               if(!toode.equals(oldtoode)){
               	counter = 0;
               }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){

                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if (al != null){
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}

                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
                 oldtoode = toode;
               }
               cou++;
             }


  rs2.close();
  con2.close();

     } catch (Exception e){
       e.printStackTrace();
     }
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "BuitNowacoEE";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap totaldebts = new TreeMap();
   private Vector blockedcusts = new Vector();
   private Vector debtList = new Vector();
   private HashMap sortimendid = null;
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:my_nowest";
   private Connection con;
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
   private String lang = null;

   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

  	 try {
  	 	String cmd = "select sum(viso_1) AS summa,a.manager as agent FROM apyvarta a " +
                          "   where month(Data) =month(current_date()) and YEAR(Data) = YEAR(current_date()) " +
						"	  and day(Data) <= day(current_date())" +
					"		and operacija  not like'%Reklam%' and operacija  not like'%Pirkimas%' and " +
					"		(tiekejas is null or tiekejas like '%idvest%' ) group by a.manager" +
					"		order by gavejas";
  	  Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
                double sales = rs.getDouble("summa");
                 String agentID = rs.getString("agent");
                 if(agentID != null){
                	 if(agents.get(agentID) != null){
                 	    Agent agent = (Agent)agents.get(agentID);
                         if (agent != null){
                           agent.setMonthSales((int)(sales));
                    } 
                  } 
                 }
                
             
          }

	        rs.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }

  

  @Override
  public void PictureMaker2() {
  	 makePictureCat();
  	 System.exit(0);
  }

  
  
@Override
public void PictureMaker() {
		  System.exit(0);
}
}
