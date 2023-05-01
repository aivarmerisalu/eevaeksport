package eevaeksport.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
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

import com.amazonaws.services.s3.model.S3DataSource.Utils;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class VichiPOLSQLDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public VichiPOLSQLDBDriver(EksportPanel parent) {
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
		    rcompany = getParent().getProps().getProperty("rest.company");
		    rcompanyx = getParent().getProps().getProperty("rest.companyx");
		   
	  }

	  

	  String user =  null;
	  String pass = null;
	  String rcompany = null;
	
      String rcompanyx = null;

   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
		  makePictureCat();
		  System.exit(0);
	   	return null;
	   }
   


	
	 private void makePictureCat(){

		 String cmd2 = " SELECT [No_] AS toode,(t.[Description]) AS nimi, var.[Qty_ per Unit of Measure] as kordaja , pr.[Unit Price] AS myygihind, [Product Group Code] AS grupp,[Subgroup Code] AS grupp2 \r\n" + 
	     			"     			     					,[Sales Unit of Measure] AS unit,  ref.[Cross-Reference No_] AS ean,'' AS status ,'' AS maxdiscount,\r\n" + 
	     			"     			     						  '' AS type, vat.[Qty_ per Unit of Measure] AS kastis,  t.[Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, t.[Description] AS vatdesc  \r\n" + 
	     			"     			     						  , [Description 2] as info ,t.[Item Disc_ Group] as prgroup FROM [VICIUNAI-POL$Item] t \r\n" + 
	     			"     										  left join [VICIUNAI-POL$Sales Price] pr ON pr.[Item No_] = [No_] and pr.[Sales Code] = ''\r\n" + 
	     			"     										  LEFT JOIN [VICIUNAI-POL$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KARTON' ) \r\n" + 
	     			"											   LEFT JOIN [VICIUNAI-POL$Item Unit of Measure] var ON var.[Item No_]=[No_] and var.[Code]=[Sales Unit of Measure]  \r\n" + 
	     			"     			     						  LEFT JOIN [VICIUNAI-POL$Item Cross Reference] ref ON (ref.[Item No_]=[No_] and ref.[Unit of Measure]='SZT'  and ref.[Cross-Reference Type] = 3)\r\n" + 
	     			"     			     						  WHERE ([Blocked]=0  AND len([Product Group Code])>0 and  len(t.[Description]) != 0) \r\n" + 
	     			"     			     						  or [Subgroup Code] like '%2.08.01%' ORDER BY [No_]";

		    String picts = parent.getProps().getProperty("pict.dir");
           	String pictspda = parent.getProps().getProperty("pictpda.dir");

		   	try {
		   		
		   	//Kataloogi sisude kustutamine
		   	 File res2 = new File (pictspda);
	            String[] ff2 = res2.list();
	        	 for(int i = 0;ff2.length>i;i++){
	            	(new File ( pictspda + ff2[i])).delete(); 
	              }
		   		
		   		
		 	   Connection con = getNewConnection2();
			
		   		Statement stmt = con.createStatement();
		   	    ResultSet	      rs = stmt.executeQuery(cmd2);


		        StringBuffer buf = new StringBuffer();
		      
		      
		        HashSet prods = new HashSet();
		        
		        while (rs.next()) {
		   	    	
		   	    	
		   	    	
		   	     String toode = rs.getString("toode").trim();  
		   	     String ean = rs.getString("ean"); 
		       
		   	     if(ean != null && !prods.contains(toode)) {
		   	    	 ean = ean.trim();
		   	    	 prods.add(toode);
		   	    
		   	     toode = toode.replace('/', '_');
		         toode = toode.replace('/', '_');
		   	    	
		                	 String[] gg = new File(picts).list();
				            
				             for(int i = 0 ; gg.length > i ; i++){ 
				            	 String len = "0";
				            	 
				            	 if(gg[i].startsWith(ean)){
				          
				            		 File in = new File ( picts + gg[i]); 
				            		 if(in.isFile()){            	
				 		             	File inpda = new File ( pictspda + toode + ".jpg"); 
				 		             	Converter.shrinkAndCopyFile(in,inpda);
				 		                len = inpda.length() + "";
				 		               }
				            		 buf.append(toode+".jpg;"  + len + "\n");
				            	    }
				             	 }
				            }
		               }
		   	    rs.close();
		   	    stmt.close();
		   	    con.close();
		   	
		  	    
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
		        

		             File fff =  new File (pictspda +  "list.txt");
		       	     Converter.copyFile(f,fff);  	
		   	
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   
		}
		 
	
	 private Vector mlines = null;
	 TreeMap<String,Double> prodprices = new TreeMap<String,Double>();
	
   public Vector getPriceMatrixData(){
		  
	   
	   if(prodprices.size() == 0) {
		   this.getProducts(null);
	   }
	  	 
	  	 String sql =" SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, \r\n" + 
	  	 		"	  	 				              [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from \r\n" + 
	  	 		"	  	 				               [VICIUNAI-POL$Sales Price] where ( (getdate() BETWEEN [Starting Date] AND [Ending Date] )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND year([Ending Date]) < 1900 )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND getdate() <= [Ending Date] )\r\n" + 
	  	 		"        OR ( year([Ending Date]) <1900 AND getdate() >=  [Starting Date] )\r\n" + 
	  	 		"	   )\r\n" + 
	  	 		"	  	 							    and [Sales Type] = 0  \r\n" + 
	  	 		"	  	 							 	union \r\n" + 
	  	 		"	  	 							   SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, \r\n" + 
	  	 		"	  	 				              [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val from \r\n" + 
	  	 		"	  	 				               [VICIUNAI-POL$Sales Price] where ( (getdate() BETWEEN [Starting Date] AND [Ending Date] )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND year([Ending Date]) < 1900 )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND getdate() <= [Ending Date] )\r\n" + 
	  	 		"        OR ( year([Ending Date]) <1900 AND getdate() >=  [Starting Date] ))  and [Sales Type] in (1) \r\n" + 
	  	 		"	  	 						   union \r\n" + 
	  	 		"	  	 						   					  \r\n" + 
	  	 		"	  	 		 SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, \r\n" + 
	  	 		"	  	 				              [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from \r\n" + 
	  	 		"	  	 				               [VICIUNAI-POL$Sales Price] where ( (getdate() BETWEEN [Starting Date] AND [Ending Date] )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND year([Ending Date]) < 1900 )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND getdate() <= [Ending Date] )\r\n" + 
	  	 		"        OR ( year([Ending Date]) <1900 AND getdate() >=  [Starting Date] ))\r\n" + 
	  	 		"	  	 							    and [Sales Type] = 1\r\n" + 
	  	 		"	  	 								union \r\n" + 
	  	 		"	  	 								 select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup,  \r\n" + 
	  	 		"	  	 				               0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val \r\n" + 
	  	 		"	  	 							    from [VICIUNAI-POL$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1	\r\n" + 
	  	 		"	  	 							 union \r\n" + 
	  	 		"	  	 								 select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, \r\n" + 
	  	 		"	  	 				               0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val \r\n" + 
	  	 		"	  	 							    from [VICIUNAI-POL$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and ((getdate() BETWEEN [Starting Date] AND [Ending Date] )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND year([Ending Date]) < 1900 )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND getdate() <= [Ending Date] )\r\n" + 
	  	 		"        OR ( year([Ending Date]) <1900 AND getdate() >=  [Starting Date] ))\r\n" + 
	  	 		"		 union \r\n" + 
	  	 		"	  	 								 select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, \r\n" + 
	  	 		"	  	 				               0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val \r\n" + 
	  	 		"	  	 							    from [VICIUNAI-POL$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0 and ((getdate() BETWEEN [Starting Date] AND [Ending Date] )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND year([Ending Date]) < 1900 )\r\n" + 
	  	 		"        OR (  year([Starting Date]) < 1900 AND getdate() <= [Ending Date] )\r\n" + 
	  	 		"        OR ( year([Ending Date]) <1900 AND getdate() >=  [Starting Date] )) order by 1,2,7 desc";

	        mlines = new Vector();

         try {
	          HashSet hset = new HashSet();
	      	   Connection con = getNewConnection2();
	   	        Statement stmt = con.createStatement();
	   	        ResultSet rs = stmt.executeQuery(sql);
	           while (rs.next()) {

	             String custID = Converter.enCodeEst(rs.getString("custid"));
	             String clGroup = rs.getString("clgroup");
	             String prodID = rs.getString("prodid");
	             String prodGroup = rs.getString("prodgroup");
	            
	             double price = rs.getDouble("price");
	            if(prodprices.get(prodID) != null) {
	            //	price = price * prodprices.get(prodID);
	            }
	             
	            
	             double disc =  rs.getDouble("disc");
	             String edate = rs.getString("paev");
	             String sdate = rs.getString("startday");

	             String val = rs.getString("val");
	             if(val==null || val.trim().length() == 0) {
	            	 val = "zł";
	             }
	     
	             val = val.trim();
	             MatrixLine mline = new MatrixLine();
	             String check = "";
                 mline.setCurrency(val);
                 
	             if (custID != null && custID.length() > 0) {
	               mline.setCustID(custID);
	               check = custID;
	             }
	             
	             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
	                 mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim().replace('Í', 'I')));
	                check = check + clGroup.trim().replace('Í', 'I');
	             }
	             
	            if( sdate != null  && !sdate.startsWith("1753") && edate != null  && !edate.startsWith("1753")){
	            	//kui on mõlemad siis paneme kuupäevad 
	            	 if(sdate != null  && !sdate.startsWith("1753")){
		            	 mline.setStartDate(sdate.substring(0,10));
		             }
		             if(edate != null && !edate.startsWith("1753")){
		            	 mline.setEndDate(edate.substring(0,10));
		             }	
	            }
	            
	             if (prodID != null && prodID.length() > 0) {
	               mline.setProdID(prodID.trim());
	               check = check + prodID.trim();
	             }
	             
	            
	             if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
	               mline.setDiscGroup(prodGroup.trim());
	               check = check + prodGroup.trim();
	             }

	             if (price != 0) {
	               mline.setPrice(price);

	             }
	             if (disc != 0) {
	                 mline.setDiscount(disc);

	               }
	             
	             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
	            	 if(!hset.contains(check)){
	            	      mlines.add(mline);
	            	      hset.add(check);
	            	 }
	              }
	           }
	           rs.close();
	           stmt.close();
	           con.close();
	         }catch(Exception e){
	        	 e.printStackTrace();
	         }
		
	    return mlines;
	      }



   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2+getParent().getProps().getProperty("dsna.url"), getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }
   
	public Vector getLastThreeData() {
		return null;
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

	 
   public ManagerData getManagerData() throws SQLException{
    ManagerData md = new ManagerData();
    return md;

  }

 
   public Vector getFixedAssortmentData() {
   
        if(mlines == null) {
        	return this.getPriceMatrixData();
        }
          return mlines;
   }
   

   public DealerProduct DealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [VICIUNAI-POL$Item Ledger Entry] l " +
          " INNER JOIN [VICIUNAI-POL$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('TAL') " +
          " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupės kodas])>0 " +
          " GROUP BY l.[Location Code],l.[Item No_] ORDER BY l.[Item No_] ";
       

       	 Connection con = getNewConnection2();
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


   public String getSaveStockInfo(){
   	return null;
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
     setTreeGroup(true);
     String cmd = "  SELECT  [Code] AS kood,[Description] AS nimi  FROM [VICIUNAI-POL$Product Group] where [Code] != ''order by [Code]";
     	 
       	 Connection con = getNewConnection2();
     //  	System.out.println("asike " +cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood");
   	        
   	                String name = rs.getString("nimi").trim().substring(5).trim() ;
   	           //  if(id.trim().length()== 4  || id.trim().length() == 5) {
   	             tm.put(cleardots(id),  Converter.enCodeEst(pol(name)));
   	             //}
   	        }
   	        
   	      rs.close();
   	      con.close();

         return tm;
   }

  
   public HashSet getActiveprods() throws SQLException {
	   
	   
	   HashSet   tm = new HashSet();
	     setTreeGroup(true);
	 	 
	  	 String sql ="  select [Item No_] as prodid from [VICIUNAI-POL$Sales Price] where [Sales Code] = '' and [Unit Price] > 0 and\r\n" + 
	  	 		"							  ((getdate() BETWEEN [Starting Date] AND [Ending Date] ) \r\n" + 
	  	 		"	  	 		        OR (  year([Starting Date]) < 1900 AND year([Ending Date]) < 1900 )\r\n" + 
	  	 		"	  	 		        OR (  year([Starting Date]) < 1900 AND getdate() <= [Ending Date] )\r\n" + 
	  	 		"	  	 		        OR ( year([Ending Date]) <1900 AND getdate() >=  [Starting Date] ))" ;

	     	 
	       	 Connection con = getNewConnection2();
	   
	   	        Statement stmt = con.createStatement();
	   	        ResultSet rs = stmt.executeQuery(sql);
	   	        while(rs.next()){
	   	           String id = rs.getString("prodid");
	   	            if(!tm.contains(id)) {
	   	                tm.add(id.trim());
	  	   	         }
	   	        }
	   	        
	   	      rs.close();
	   	      con.close();

	         return tm;
	   }
   
   


   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
    String cmd = " SELECT  [Code] AS kood,[Description] AS nimi  FROM [VICIUNAI-POL$Product Group] where [Code] != ''order by [Code]";
   // System.out.println("Asike 2 " +cmd);
    try {
    	 Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	            String groupID = rs.getString("kood");
	          	   	        
	                String name = rs.getString("nimi") ;
	                if(groupID.length()>5){
	                   tm.put(cleardots(groupID),name);
	           }
	        }
	     //   System.out.println("Teine" + tm.toString());
	        rs.close();
	        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
        return tm;
  }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}


	   return idx;
   }
   
   
   private String pol(String in) {
	 
	   
	   if(in.contains("MEDUZA") ) {
		   System.out.println("NAme " + in );
		  	for(int i =0 ;  i< in.toCharArray().length;i++) {
	     		System.out.println(in.toCharArray()[i] + " " + (int)in.toCharArray()[i]);
	     	}
	   }
	   in = in.replaceAll("\"", " ");
	   in = in.trim();
	   in = in.replaceAll("  "  , " ");
	   in = in.replace('^', 'ł');
	   in = in.replace('^', 'ł');

	   
	   in = in.replace((char)63 ,'Ł');
	   in = in.replace((char)63 ,'Ł');
	   in = in.replace((char)151 ,'Ś');
	   in = in.replace((char)151 ,'Ś');
	   in = in.replace((char)126 ,'ś');
	   in = in.replace((char)126 ,'ś');

	   in = in.replace((char)352 ,'ę');
	   in = in.replace((char)352 ,'ę');
	   in = in.replace((char)168 ,'Ę');
	   in = in.replace((char)168 ,'Ę');

	   in = in.replace((char)164 ,'Ą');
	   in = in.replace((char)164 ,'Ą');
	   
	   in = in.replace((char)228 ,'ń');
	   in = in.replace((char)228 ,'ń');
	   
	   

	  // in = in.replace((char)51 ,'ż');
	  // in = in.replace((char)51 ,'ż');
	   
	  // in = in.replace('~', 'ś');
	   //in = in.replace('~', 'ś');
	   
	   
	 //  spółka z ograniczoną odpowiedzialnością sp.k 
	  // ALMAR TRADING  SPa?KA Z OGRANICZON¤ ODPOWIEDZIALNOCI¤
		//	   Spółka komandytowa
			   // ą, ć, ę, ł, ń, ó, ś, ź and ż.

	   return in;
   }

   public TreeMap getAgents(){

	   
	   //getPriceMatrixData();
	   
        String cmd = "SELECT [Code] AS kood,[Name] AS nimi FROM " +
		"[VICIUNAI-POL$Salesperson_Purchaser] ";
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	     agent1 = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	     
	          agent.setName(pol(name));


              agent.setLanguage("POL");
	          agent.setCountry("pl");
	          agent.setPassWord("0000");
	          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
	          if(email.length() > 1){
	        	  agent.setEmail(email);
	        	  agent.setActive(true);
	          }
	      //    System.out.println("Nimed " + name + " " + pol(name));
	          
	          
	  		
				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
               //  System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }
				}
	          agent1.put(agent.getID(), agent);
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
    
        Agent aas = new Agent("LADU");
        aas.setName("LADU");
        agents.put("LADU", aas);

            rs.close();
	        con.close();
	        
	        
	/*  cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa " +
	  " FROM [VICIUNAI-POL$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 " ;
	  System.out.println(cmd);
	         con = getNewConnection2();
	        Statement stmt1 = con.createStatement();
	        ResultSet rs1 = stmt1.executeQuery(cmd);
	        while(rs1.next()){

	          String id = rs1.getString("klient");
	          Debt debt = new Debt ();
	         // debt.setDoknr(clear(st2.nextToken()));
	          debt.setDue(rs1.getString("summa"));
	          overdebts.put(id,debt);
	     }

            rs1.close();
	      
	        con.close(); */
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }

private TreeMap agent1 = new TreeMap();

   public TreeMap getCustomers(Agent agent){
	   
	   TreeMap<String,Double> overdue= new TreeMap<String,Double>();
	   TreeMap<String,Double> used= new TreeMap<String,Double>();
   HashSet custs = new HashSet();
    
     try {

     try {
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass); 
				
		}catch(Exception e){
			
		}

   
      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer ,a.[Posting Date] as kuup FROM [VICIUNAI-POL$Cust_ Ledger Entry] a inner join " +
                            " [VICIUNAI-POL$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
                            " where " +
							"  (a.[Document type] = 2 or a.[Document type]= 3)  AND a.[Posting Date] > getdate()-300 AND a.[Open]= 1 " +
                            " group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_], " +
                            " a.[Due Date], a.[Posting Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

      ketid = new HashMap();
      sortimendid = new HashMap();
  System.out.println(cmd);

       this.debtList = new Vector();
       con = getNewConnection2();
       Statement stmt1 = con.createStatement();
       ResultSet rs1 = stmt1.executeQuery(cmd);
       while(rs1.next()){
        Debt debt = new Debt();
       String custID = rs1.getString("klient");
        debt.setCustID(custID);
        debt.setPayerID(custID);
try{
	 String doknr = rs1.getString("number").trim();    
     debt.setDoknr(doknr);
}catch(Exception e){
	
}
       
        debt.setDuedate(rs1.getDate("kuupaev"));
        debt.setSum(rs1.getDouble("summa"));
      
        int dueint = rs1.getInt("due");
        debt.setDate(rs1.getDate("kuup"));
        if(dueint > 0) {
        	  if(overdue.get(custID) != null) {
              	overdue.put(custID,overdue.get(custID) + debt.getSum());
              }else {
              	overdue.put(custID, debt.getSum());
              }
        }
        if(used.get(custID) != null) {
          	used.put(custID,used.get(custID) + debt.getSum());
          }else {
          	used.put(custID, debt.getSum());
          }
        
        debt.setDue(dueint + "");
        int old = 0;
        if(debtDays.get(custID) != null){
         old = Integer.parseInt((String)debtDays.get(custID));
        }
        if(old<dueint){
       this.debtDays.put(custID,dueint+"") ;
        }
        this.debtList.add(debt);
            }

       rs1.close();
       con.close();
      } catch (Exception e){
        e.printStackTrace();
      }
/// KLIENDI ANDMETE LEIDMINE

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();




   String sql =  " SELECT k.[Name]  AS nimi, k.[Name 2] AS nimi2 ,adr.[Name] AS nimi3 ,k.[No_] AS kood,adr.[Code] as kood2 ,k.[Bill-to Customer No_] AS payer,\r\n" + 
   		"   		adr.[Address] + ' ' + adr.[Address 2] + ' ' + adr.[City] AS aadress ,k.[Country Code] as country,adr.[Salesperson Code] AS agent , \r\n" + 
   		"		k.[Salesperson Code] AS agent2 ,blocked AS blokk, \r\n" + 
   		"   			 [Payment Terms Code] AS payment,[Line Discount %] AS alekood, 0 AS yle,'' AS visit,'' AS type,   \r\n" + 
   		"   			   [Blocking after overdue days] AS extradays ,'' AS sort, '' AS debt , 0 AS minsum , \r\n" + 
   		"   			  adr.contact +' ' + adr.[Phone no_] + ' ' + adr.[E-Mail] as kontakt ,' 'as kett  , '' as chain , \r\n" + 
   		"   			  [Customer Price Group] as hinnak, k.[Currency Code] as currency ,  k.[Credit Limit (LCY)] as credit \r\n" + 
   		"   			  FROM [VICIUNAI-POL$Customer] k  LEFT JOIN [VICIUNAI-POL$Ship-to Address] adr ON adr.[Customer No_]  = k.[No_]  \r\n" + 
   		"			  LEFT JOIN [VICIUNAI-POL$Sales Line Discount] d ON d.[Sales Code] = k.[Customer Disc_ Group] \r\n" + 
   		"   			 WHERE (k.[Salesperson Code] != ''  AND blocked != 3) or k.[No_] = '2' " ;
   System.out.println(sql);
   try {
        Connection con = getNewConnection2();
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        
        while(rs.next()){
         
         String name = rs.getString("nimi").trim();
         String name2 = rs.getString("nimi2");
         if(name2 != null){
        	 name = (name + " " + name2.trim()).trim();
         }
         
         String name3 = rs.getString("nimi3");
         if(name3 != null){
        	 name = name3.trim(); 
         }
           String id = rs.getString("kood").trim();
           String payer = id;
           String id2 = rs.getString("kood2");
           if(id2 != null && id2.trim().length() > 0) {
        	   id = id + "_" + id2.trim();
           } else {
          	 id  = id + "_00" ; 
           }
           
           
           Customer customer = new Customer(id);
           
           if(overdue.get(payer) != null) {
        	   customer.setDebt(overdue.get(payer));
           }
           
           if(used.get(payer) != null) {
        	   customer.setUsedCredit(used.get(payer));
           }
           
           
           if (name.equals("")) name = "<" + id + ">";
          name = ((pol(name)).trim());
           
           name = name .replaceAll("SPaŁKA", "SPÓŁKA");
           name = name .replaceAll("spałka", "spółka");
           name = name .replaceAll("Spcłka", "Spółka");
           name = name .replaceAll("spcłka", "spółka");
           name = name .replaceAll("Spclka", "Spółka");
           
           name = name .replaceAll("aSKI", "ÓSKI");
           name = name .replaceAll("nY ", "ną ");  
           
           name = name .replaceAll("iY ", "ią ");
           if(!name.contains("NOWY"))
           name = name .replaceAll("Y ", "ą ");
           name = name .replaceAll("1cłtki", "Źółtki");
           name = name .replaceAll("GaRNA", "GÓRNA");
           name = name .replaceAll("WSPaLNICY", "WSPÓLNICY");
           name = name .replaceAll("FORODOaSKA", "FORODOÓSKA");
           name = name .replaceAll("JaZEFA", "JÓZEFA");
           name = name .replaceAll("POZNAa", "POZNAÓ");
           name = name .replaceAll("SKaRCZ", "SKÓRCZ");
        
           
           
           
           
           
           
           
           customer.setName(((name)).trim());
           
       
           	if(payer != null && payer.trim().length()>0){
              customer.setPayer(payer.trim());
              // customer.addFixSortCode(payer.trim());
             }
             customer.addFixSortCode(id);
             String aadress = rs.getString("aadress");
             String country = rs.getString("country");
             if( aadress != null && aadress.trim().length()>0){
            	 
            	 aadress = aadress.replaceAll("  ", " ");
            	 
            	 if(country != null) {
            		 country = country.trim();
            	 }else {
            		 country = "";
            	 }
            	 if(country.equals("HU")) {
            		 country = "Hungary";
            	 }

            	 if(country.equals("CZ")) {
            		 country = "Czech";
            	 }

            	 if(country.equals("SK")) {
            		 country = "Slovakia";
            	 }
            	 if(country.equals("PL")) {
            		 country = "Poland";
            	 }
               customer.setAddress(Converter.adressLink(pol(Converter.enCodeEst(aadress)),country));
               customer.setName((customer.getName().trim())+ " " + 
              (pol(Converter.enCodeEst(aadress))));
             }
         
             String ag = rs.getString("agent");
             if(ag != null){
                customer.addAgent(new Agent(ag.trim()));
                if(ag.trim().startsWith("PHD")) {
                	 customer.addAgent(new Agent("PHD 01"));
                	 customer.addAgent(new Agent("PHD 02"));
                	 customer.addAgent(new Agent("PHD 03"));
                	 customer.addAgent(new Agent("PHD 04"));
                	 customer.addAgent(new Agent("PHD 05"));
                	 customer.addAgent(new Agent("PHD 06"));
                	 customer.addAgent(new Agent("PHD 07"));
                }
               } 
              
               ag = rs.getString("agent2");
                 if(ag != null){
                    customer.addAgent(new Agent(ag.trim()));
                    if(ag.trim().startsWith("PHD")) {
                   	 customer.addAgent(new Agent("PHD 01"));
                   	 customer.addAgent(new Agent("PHD 02"));
                   	 customer.addAgent(new Agent("PHD 03"));
                   	 customer.addAgent(new Agent("PHD 04"));
                   	customer.addAgent(new Agent("PHD 05"));
               	 customer.addAgent(new Agent("PHD 06"));
               	 customer.addAgent(new Agent("PHD 07"));
                   }
                 }
             
          
				
				  if (ag != null && agent1.get(ag.trim()) != null){
						Agent aa = (Agent)agent1.get(ag.trim());
					
						Vector vec = aa.getDubbler();
						if(vec!= null && vec.size() >0){
							Enumeration en = vec.elements();
							while(en.hasMoreElements()){
								String age = (String) en.nextElement();
								customer.addAgent(new Agent(age));
							}
						}
					}
             
             if (ag != null && ag.trim().length()>0){
 				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
 				
 				if(agg != null && agg.getDubbler().size()>0){
 					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
 						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
 					}
 				}
 			}
            
              if(customer.getID().equals("2_00")) {
             Enumeration enume = getParent().getAgentsHash().elements();
             while (enume.hasMoreElements()) {
               Agent a = (Agent) enume.nextElement();
                 customer.addAgent(a);
              }
             }
           

   String bl = rs.getString("blokk");
             if(bl.equals("1") || bl.equals("2")){
               customer.setSalesBlock(true);
             }

            String paym = rs.getString("payment");

           	String num = rs.getString("alekood");
           	if(num != null && num.trim().length()>0){
           	double dd = Double.parseDouble(num.trim());
          // 	if(dd > 0)
          //   customer.setCity("Discount : " + getNF().format(Double.parseDouble(num.trim())) + " %");
           	}


      		String visit = rs.getString("visit");
             if(visit != null && visit.trim().length()>0){
                customer.setVisit(visit.trim());
             }

            String type = rs.getString("type");
            if(type != null && type.trim().length()>0){
           // customer.setCustType(type.trim());
            }

            String bll = rs.getString("extradays");
           	if(bll != null && bll.trim().length()>0){

            if (customer.getSalesBlock()){
              	 customer.setSalesBlock(false);
           	int extra = Integer.parseInt(bll);
           		if(debtDays.get(customer.getID())!= null && customer.getPaymTerm().trim().length()>0 &&
           				(extra < Integer.parseInt((String)debtDays.get(customer.getID())) )){
           	 customer.setSalesBlock(true);
           	}
          }
           	}
           String grupp = rs.getString("sort");
           if(grupp != null && grupp.trim().length()>0){
        	   Vector vv = new Vector ();
				vv.add(Converter.cleanLitLatEst(grupp.trim()));
				customer.setFixSortCode(vv);
           	sortimendid.put(customer.getID(), Converter.cleanLitLatEst(grupp.trim()));
           }

         

           	String yes = rs.getString("debt");

           	if (yes != null && !yes.trim().equals("0")){
                customer.setNodebt(true);
           	}

           	
           	double minsum = rs.getDouble("minsum");
           	if(minsum>0){
           		customer.setMinsum(minsum);
           	}
          
           if(paym != null && paym.trim().length()>0){
        	   paym = paym.replaceAll("D"," ");
               paym = paym.replaceAll("SR","-1").trim();
        	customer.setPaymTerm(paym);
             }

           
           
          //
           String cont = rs.getString("kontakt");
           if(cont != null && cont.trim().length()>0){
               customer.setPersonalData(Converter.enCodeEst(cont.trim().replaceAll(";", ",")));   
           }
           String kett = rs.getString("kett");
           if(kett != null && kett.trim().length()>0){
        	   if(ketid.get(Converter.cleanLitLatEst(kett.trim())) != null){
           		Vector vv = (Vector)ketid.get(Converter.cleanLitLatEst(kett));
           		vv.add(customer.getID());
           		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
           	}else{
           		Vector vv = new Vector();
           		vv.add(customer.getID());
           		ketid.put(Converter.cleanLitLatEst(kett.trim()), vv);
           	}
           }
           String chain = rs.getString("chain");
           if(chain != null && chain.trim().length()>0){
               customer.setChain(Converter.enCodeEst(chain.trim()));   
           }
           
          /* String rout = rs.getString("rout");
           if(rout != null && rout.trim().length()>0){
           if(customer.getCity() != null && customer.getCity().trim().length() > 0){
             customer.setCity(customer.getCity() +"| VeoInfo : " + Converter.enCodeEst(rout.trim()));  
           }else{
             customer.setCity(" VeoInfo : "  +Converter.enCodeEst(rout.trim()));  
           }
              
           }
           
           String rout2 = rs.getString("rout2");
           if(rout2 != null && rout2.trim().length()>0){
           if(customer.getCity() != null && customer.getCity().trim().length() > 0){
             customer.setCity(customer.getCity() +"| VeoRing : " + Converter.enCodeEst(rout2.trim()));  
           }else{
             customer.setCity(" VeoRing : " + Converter.enCodeEst(rout2.trim()));  
           }
              
           }*/
           
           
            String hinnak = rs.getString("hinnak");
           if(hinnak != null && hinnak.trim().length()>0){
               customer.setGroup(hinnak.trim().replace('Í', 'I'));  
               customer.setCustType(hinnak.trim().replace('Í', 'I'));
               customer.addFixSortCode(hinnak.trim().replace('Í', 'I'));
           }
        //   " [Shipping Agent Code] AS rout  ,[Loading Code] as rout2 ,  [Customer Price Group] as hinnak 
           
           
           String curre  = rs.getString("currency");
           if(curre != null && curre.trim().length() > 1 ) {
        	   customer.currency = curre.trim();
           }else {
        	   customer.currency = Converter.enCodeEst("PLN");
           }
           
           
           Double credit = rs.getDouble("credit");
           if(credit != null && credit > 1) {
        	   customer.setCredit(credit +"");
           }
           
          
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
            custs.add(customer.getID());
         
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
   
   private void makeTexts() throws Exception{
		  
		  
		  String    sql = "select No_ as kood , Comment as asi from [VICIUNAI-POL$Comment Line] where [Table Name] = '3' order by No_,[Line No_]";
		  
		  TreeMap<String,String> tr = new TreeMap<String,String>();
	  Connection con =  getNewConnection2();
	  Statement stmt = con.createStatement();
	   ResultSet rs = stmt.executeQuery(sql);
	    StringBuffer sb = new StringBuffer();
	   while (rs.next()) {
	     String kood=rs.getString("kood").trim();
	    	 String nimetus=rs.getString("asi");
	    			 if(nimetus != null){
	    				 nimetus = nimetus.trim().replaceAll("\\r\\n|\\r|\\n", "|").replaceAll(";", ":"); 
	    				 
	    				 String aa = tr.get(kood);
	    				 if(aa == null) {
	    					 aa = nimetus;
	    				 }else {
	    					 aa = aa +" " + nimetus; 
	    				 }
	    				 if(aa.trim().length()> 1)
	    				 tr.put(kood, aa.trim());
	    			 }
	      }
	   
	   rs.close();
	   stmt.close();
	   con.close();
	   
	  Iterator itera =  tr.keySet().iterator();
	  while (itera.hasNext()) {
		  String kood = (String)itera.next();
		  sb.append(kood + ";" + tr.get(kood) + "\n");  
	  }
	   Converter.saveFileInUTF8("texts.txt", sb);

	  }
   
   
   public TreeMap getProducts(Agent agent) {
	   try {
		makeTexts();
	} catch (Exception e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
		HashSet inPricelist = null;
	   try {
		   inPricelist = getActiveprods();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	   
     TreeMap tm = new TreeMap();
     
      TreeMap<String,Double>  ladutr = new TreeMap<String,Double>();
      prodHash = new HashMap();
      
     try {
   	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [VICIUNAI-POL$Item Ledger Entry] l " +
         " INNER JOIN [VICIUNAI-POL$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('MAG15','MAG12','MAG17','MAG30') " +
         " AND t.[Blocked]=0 AND len(t.[Product Group Code])>0 " +
         " GROUP BY l.[Location Code],l.[Item No_] HAVING sum(l.[Quantity]) > 0 ORDER BY l.[Item No_] ";

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        
  	        while(rs.next()){
  	           ladutr.put(rs.getString("kood").trim(), rs.getDouble("kogus"));
  	        }

  	      rs.close();
  	      con.close();

  	    //System.out.println(cmd);

  	   // System.out.println("ladu " + ladutr.toString());
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     try {
     	String cmd = " SELECT [No_] AS toode,(t.[Description]) AS nimi, var.[Qty_ per Unit of Measure] as kordaja , pr.[Unit Price] AS myygihind, [Product Group Code] AS grupp,[Subgroup Code] AS grupp2 \r\n" + 
     			"     			     					,[Sales Unit of Measure] AS unit,  ref.[Cross-Reference No_] AS ean,'' AS status ,'' AS maxdiscount,\r\n" + 
     			"     			     						   vat.[Qty_ per Unit of Measure] AS kastis,  vau.[Qty_ per Unit of Measure] AS layer ,  t.[Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, t.[Description] AS vatdesc  \r\n" + 
     			"     			     						  , [Description 2] as info ,t.[Item Disc_ Group] as prgroup  ,t. [Class Code] AS type FROM [VICIUNAI-POL$Item] t \r\n" + 
     			"     										  left join [VICIUNAI-POL$Sales Price] pr ON pr.[Item No_] = [No_] and pr.[Sales Code] = ''\r\n" + 
     			"LEFT JOIN [VICIUNAI-POL$Item Unit of Measure] vau ON (vau.[Item No_]=[No_] and vau.[Code]='WARSTWA' ) " + 
     			"     										  LEFT JOIN [VICIUNAI-POL$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KARTON' ) \r\n" + 
     			"											   LEFT JOIN [VICIUNAI-POL$Item Unit of Measure] var ON var.[Item No_]=[No_] and var.[Code]=[Sales Unit of Measure]  \r\n" + 
     			"     			     						  LEFT JOIN [VICIUNAI-POL$Item Cross Reference] ref ON (ref.[Item No_]=[No_] and ref.[Unit of Measure]='SZT'  and ref.[Cross-Reference Type] = 3)\r\n" + 
     			"     			     						  WHERE ([Blocked]=0  AND len([Product Group Code])>0 and  len(t.[Description]) != 0) \r\n" + 
     			"     			     						  or [Subgroup Code] like '%2.08.01%' ORDER BY [No_]";
     	
     // System.out.println(cmd);
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            double weight = 0 ;
            Product prod = new Product(id);
         	String name =  (pol( rs.getString("nimi").trim()));
            Double kordaja = rs.getDouble("kordaja");
            double price = rs.getDouble("myygihind");
            if(kordaja != null && kordaja > 0) {
        	  //  price = price * kordaja;
            }
            if(ladutr.get(id) != null ){
            	Double kogus = ladutr.get(id); 
            	if(kordaja != null && kordaja > 0) {
            		kogus = kogus / kordaja;
            	}
          	  prod.setStock(kogus);
          	 // System.out.println(id + "  " + ladutr.get(id));
            }
              
           
                prod.setPrice(price);
                prodprices.put(id, price);
             
                String grupp = cleardots(rs.getString("grupp"));
             if(grupp != null)
                prod.setGroup(grupp.trim());

        		String grupp2 = cleardots(rs.getString("grupp2"));
        		if(grupp2 != null)
    			prod.setGroupSecond(grupp2);

                String unit = rs.getString("unit");
                if(unit.contains("mro")) {
              	  int i = unit.indexOf("mro");
              	  unit = unit.substring(0,i) + "mroż" + unit.substring(i+4);
                }
                prod.setUnit(unit);
              
                String ean = (rs.getString("ean"));
                 if (ean != null && ean.trim().length()>0){
                   prod.setEAN(ean);
                 }
                 

             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases/kordaja);
                	}
               
                double	kihis = rs.getDouble("layer");
                 	if(kihis>0){
                       prod.layer = (kihis/kordaja);
                 	}

                	
             	weight = kordaja;
                           if(weight > 0){
                            	 prod.setMeasure(weight);
                           }      	 
                                                      
                  
          
                   try {
                	   String vv = rs.getString("vat").substring(3);        
                		prod.setVat(vv);
                   }catch(Exception k) {
                	   System.out.println("vat " + prod.getID());
                   }
                     
                    
       
                   
               	 String info = rs.getString("info");
                 if(info != null && info.trim().length()>0){ 
                	 
                	   if(info.contains("mro")) {
                     	  int i = info.indexOf("mro");
                     	 info = info.substring(0,i) + "mroż" + info.substring(i+4);
                       }
                	 
                	 prod.setPartii(pol(info.trim()));
                 }
                 
                 String prgroup = rs.getString("prgroup");
                 if(prgroup != null && prgroup.trim().length()>0){   
                	 prod.setDiscgrupp(prgroup.trim());
                 }
                 
                 String type = rs.getString("type");
                 if(type != null && type.trim().length()>0){  
                	 if(type.startsWith("1")) {
                		 prod.setType("Mrozone");
                	 } 
                	 if(type.startsWith("2")) {
                		 prod.setType("Chlodzone"); 
                	 }
                	 if(type.startsWith("3")) {
                		 prod.setType("Other"); 
                	 }
                	
                 }
                 
                 
                  if(name.contains("mro")) {
                	  int i = name.indexOf("mro");
                	  name = name.substring(0,i) + "mroż" + name.substring(i+4);
                  }
                  
                  if(name.contains("MRO")) {
                	  int i = name.indexOf("MRO");
                	  name = name.substring(0,i) + "MROŻ" + name.substring(i+4);
                  }
                
                   
                  
                  
             
                 
          prod.setName((name) + " " + prod.getID());
          prod.setNameSorting( prod.getID() + " " + (name));
          if(inPricelist.contains(prod.getID())) {
        	    tm.put(name + prod.getID(), prod);
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

   
   
   public static String deCode(String res ){
	  	return res;
	  }
	 


   public String getAssortmentStr(Agent agent){
    return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient, d.[Ship-to Code] as kl2, r.[No_] AS toode,sum(r.[Quantity]) AS kogus, d.[Posting Date] AS lastdate, 0 as lastamount " +
    	" FROM [VICIUNAI-POL$Sales Shipment Line] r " +
        " INNER JOIN [VICIUNAI-POL$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [VICIUNAI-POL$Item] t ON t.[No_]=r.[No_]  WHERE " +
        " d.[Posting Date]> GETDATE()-60 " +
        " group by d.[Sell-to Customer No_],d.[Ship-to Code],r.[No_],d.[Posting Date],t.[Search Description] " +
        " ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();
             String custID2 = rs.getString("kl2");
             if(custID2 != null && custID2.trim().length() > 0) {
          	   custID = custID + "_" + custID2.trim();
               } else {
              	 custID = custID + "_00" ; 
               }

                 String prodID = rs.getString("toode").trim();;

                 String lastdate = " ";
               double  amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                         
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,0,0," ");

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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [VICIUNAI-POL$Sales Invoice Line] r " +
		  " INNER JOIN [VICIUNAI-POL$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
		    Connection con2 = getNewConnection2();
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
     return "ViciPOLSQL";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }


   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private Vector debtList = new Vector();
   private HashMap sortimendid = null;
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url2 = "jdbc:odbc:";
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
		DecimalFormat formatter = new DecimalFormat("##0.00' ‎zł'");
  	 try {
  	 	String cmd = "select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [VICIUNAI-POL$Cust_ Ledger Entry] "+
              "  where Month(getdate())=Month([Posting Date]) and " +
 " Year(getdate())=Year([Posting Date])    group by [Salesperson Code]";

  	 			TreeMap<String, Double> ags = new TreeMap<String, Double>();
  	 	
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	        	 
                 String agentID = rs.getString("agent");
                 double sales = rs.getDouble("summa");
                 Agent agent = (Agent)agents.get(agentID);
                 ags.put(agentID, sales);
                 if (agent != null){
                   agent.setStart("Wyfakturowane: " + formatter.format(sales) + "");
                }
             }

	        rs.close();
	       
	    	cmd= 	" select sum(case when [Currency Factor] <> 0 then [Line Amount]/[Currency Factor]else [Line Amount] end) as summa,[Salesperson Code] AS agent \r\n" + 
	    			"	  	 							 from (select b.[Line Amount],a.[Currency Factor] ,a.[Salesperson Code]  from [VICIUNAI-POL$Sales Header] a \r\n" + 
	    			"	  	 								 inner  join [VICIUNAI-POL$Sales Line] b on a.[No_]=b.[Document No_] where Year(getdate())=Year(a.[Shipment Date]) and \r\n" + 
	    			"	  	 								 Month(getdate())=Month(a.[Shipment Date]) union all select b.[Line Amount],a.[Currency Factor],a.[Salesperson Code] from \r\n" + 
	    			"	  	 								 [VICIUNAI-POL$Sales Cr_Memo Header] a inner join [VICIUNAI-POL$Sales CR_Memo Line] b on a.[No_]=b.[Document No_]  \r\n" + 
	    			"	  	 								 where Year(getdate())=Year(a.[Shipment Date]) and Month(getdate())=Month(a.[Shipment Date])) S \r\n" + 
	    			"	  	 								 group By [Salesperson Code]"; // and [Global Dimension 1 Code]='400'
	    	
	    	rs = stmt.executeQuery(cmd);
	    	while(rs.next()){
	        	   double sales = rs.getDouble("summa");
               String agentID = rs.getString("agent");
               
               if(ags.get(agentID) != null) {
            	  // sales = sales - ags.get(agentID);
               }
            
               Agent agent = (Agent)agents.get(agentID);
               if (agent != null){
                 agent.setStart(agent.getStart()+"|Zamówienia w realizacji: " + formatter.format(sales) + "");
              }
           }

	        rs.close();
	        stmt.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }
}
