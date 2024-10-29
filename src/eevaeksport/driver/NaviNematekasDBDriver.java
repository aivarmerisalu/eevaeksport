package eevaeksport.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
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

public class NaviNematekasDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviNematekasDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
	    user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	     
  NF.setMinimumFractionDigits(2);
  NF.setMaximumFractionDigits(2);
  
  }
	  String user =  null;
	  String pass = null;
	  String rcompany = null;


  
  
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getPriceMatrixData(){

	
	  	 String  sql = " select * from ( SELECT [Sales Code] AS custid,'' AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi ,[Sales Campaign] as camp from" +
            "   [ŽŪB Nematekas$Sales Price] where (getdate()+5 between [Starting Date] and [Ending Date]+5  or  " +
                       "  (year ([Ending Date]) = 1753 and getdate()+5 > [Starting Date]) ) and [Sales Type] = 0   " +
                          "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup,  " +
             "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi,[Sales Campaign] as camp from  " +
             "  [ŽŪB Nematekas$Sales Price] where ( getdate()+1 between [Starting Date] and [Ending Date] or (getdate()+1 > [Starting Date]) and  year ([Ending Date]) = 1753) " +
			  "  and [Sales Type] = 1  and [Location Code] != 'S03'	" +   
                          "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
            "   0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi ,[Sales Campaign] as camp " +
                       "  from [ŽŪB Nematekas$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0   " +     
                   "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
              " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi ,[Sales Campaign] as camp  " +
                       "  from [ŽŪB Nematekas$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()-1 or  " +
                      "   year ([Ending Date]) = 1753) " +
                      " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                     " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi,[Sales Campaign] as camp " +
                     "    from [ŽŪB Nematekas$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1        " +
                    " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
                   " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi,[Sales Campaign] as camp " +
                    "     from [ŽŪB Nematekas$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()-1 or " + 
                     "    year ([Ending Date]) = 1753) ) as ta order by custid , clgroup, prodid ,paev desc " ; 
			
  	 

       Vector result = new Vector();
       HashSet olemas = new HashSet();
         try {
          
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {

             String custID = Converter.enCodeEst(rs.getString("custid").trim());
             String clGroup = rs.getString("clgroup");
             String prodID = rs.getString("prodid").trim();
             String prodGroup = rs.getString("prodgroup");
            
             double price = rs.getDouble("price");
             double disc = rs.getDouble("disc");
             String date = rs.getString("paev");
             String sdate = rs.getString("startday");
             String curr = rs.getString("val");
             int paevi = rs.getInt("paevi");
             int camp  = rs.getInt("camp");
             
             MatrixLine mline = new MatrixLine();

             if (custID != null && custID.length() > 0) {
               mline.setCustID(custID);
             }else{
            	 custID = ""; 
             }
             
             
             
             
             if (curr != null && curr.length() > 0) {
                 mline.setCurrency(curr.trim());
               }
             
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
             }

             
           
             
            if(camp == 1){
            	 if(sdate != null  && !sdate.startsWith("1753")){
                	 mline.setStartDate(sdate.substring(0,10));
                 }
                 if(date != null && !date.startsWith("1753")){
                	 mline.setEndDate(date.substring(0,10));
                 }
             }else if(paevi > 75 && camp == 0){
            	// mline.setProdLevel("pr");
             }
             
             
             
             
             if (prodID != null && prodID.length() > 0) {
               mline.setProdID(prodID);
             }
             if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
               mline.setProdGroup(Converter.cleanLitLatEst(prodGroup.trim()));
               mline.setDiscGroup(Converter.cleanLitLatEst(prodGroup.trim()));
             }

             if (price != 0) {
               mline.setPrice(price);

             }
             if (disc != 0) {
                 mline.setDiscount(disc);
               }
             
             
             if((mline.getPrice() > 0 || mline.getDiscount() > 0) && !olemas.contains(clGroup+"" + prodID + custID)
            		 && !"AKTYV25".equals( mline.getCliGroup())){
            	 if(!curr.equals("LTL")){
                     result.add(mline); 
                     olemas.add(clGroup+"" + prodID + custID);
            	 }
             }
             
           }
           rs.close();
           stmt.close();
           con.close();
         }catch(Exception e){
        	 e.printStackTrace();
         }
	
    return result;
      }


   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }

  	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	  
	     }

	
	 public String getProductSortingData(){
	   	return null;
	   }

   public ManagerData getManagerData() throws SQLException{
     return null;
  }

 
   public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		return result;
	}


   public DealerProduct getDealerProductsList() {
 return null;
      }


       public String getSaveStockInfo(){

 	  	  
    		  String  cmd = 
    " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [ŽŪB Nematekas$Item Ledger Entry] l " +
    "        INNER JOIN [ŽŪB Nematekas$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('S01','S02','S03','S41') and t.[Blocked]=0 and t.[End Product] = 1  " + //,'MAR003','UTE002'
	"	   GROUP BY l.[Location Code], l.[Item No_] " ;
	

    		  
    		  
    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
  
    		        	 
   
 
      		           Statement st = con.createStatement();
      		           ResultSet rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood");
    		            		 if(kood != null){

    		            		 kood = kood.trim();
    		             double seis = rs.getDouble("kogus");
    		            
    		             if(seis > 9999){
    	  	              		seis = 9999;
    	  	              	}
    	  	              	
    		             if(seis < 0){
    		            	 seis = 0;
    		             }
    		             
    		             String dealer = rs.getString("ladu").trim();
    		             if(seis > 0){
    		                  if(!dealer.equals("S03")){
    		                	 if(ladutr.get(kood) != null){
    		                		 double olem = Double.parseDouble(ladutr.get(kood)+"");
    		                		 ladutr.put( kood, seis + olem); 
    		                	 }else{
    		                		 ladutr.put( kood, seis );  
    		                	 }
    		                	 
    		                 }else{
    		                	 ladupraak.put( kood, seis ); 
    		                 }
    		                 
    		                }
    		               }    		            			 
	            		 }
    		           
    		             rs.close();
    		             st.close();
    		             con.close();
    		       Iterator itera =   ladutr.keySet().iterator();
    		       while (itera.hasNext()){
    		    	   String kood = (String) itera.next();
    		    	   double seis = Double.parseDouble(ladutr.get(kood)+"");
		                 products.append(kood +  ";");
		                 products.append(" ;");
		                 products.append(getParent().getNF().format(seis) + "; \n"); 
  		             }
    		            
    		         } catch (Exception e) {
    		             getParent().showError(e);
    		         }
    		         
    		         return products.toString();

       }


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

                         tm.put("marinu", "Marinu");
                         tm.put("kiauls", "Kiaulš");
                         tm.put("rukgam", "RūkGam");
          	     	     tm.put("vyniot", "Vyniot");
          	     	     tm.put("pusgam", "Pusgam");
                         tm.put("virdkp", "VirDKP");
                         tm.put("virdsl", "VirDšl");
                         tm.put("krdad", "KrD&D");
                         tm.put("srvytd", "ŠrVytD");
                         tm.put("pauksv", "Paukšv");
          	     	
        	         return tm;
        	   }

           
           private String getGrCode (String gr){
        	   String res = gr;
        	   
        	   
        	   
        	   if(gr.equals("13") ){
        		   return "srvytd";
        	   }
        	   
        	   
        	   if(gr.equals("12") || gr.equals("17") ){
        		   return "krdad";
        	   }
        	   
        	   if(gr.equals("11") ){
        		   return "virdsl";
        	   }
        	   
        	   if(gr.equals("10")|| gr.equals("14") || gr.equals("19")  ){
        		   return "virdkp";
        	   }
        	   
        	   
        	   
        	   if(gr.equals("41")|| gr.equals("44")|| gr.equals("43") || gr.equals("45")  ){
        		   return "pauksv";
        	   }
        	   
        	   if(gr.equals("04")  ){
        		   return "pusgam";
        	   }
        	   
        	   if(gr.equals("23")|| gr.equals("33") || gr.equals("46")  ){
        		   return "vyniot";
        	   }
        	   
        	   
        	   if(gr.equals("36")||gr.equals("48")){
        		   return "marinu";
        	   }
        	  
        	   if(gr.equals("15")||gr.equals("31")  || gr.equals("4") || gr.equals("35")){
        		   return "kiauls";
        	   }
        	   
        	   if(gr.equals("22")|| gr.equals("32") || gr.equals("37") || gr.equals("42") ){
        		   return "rukgam";
        	   }
        	   return res ;
           }
           
           

         
           public TreeMap getProdGroupsSecond() throws SQLException {
               return teised;
             }

  private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   
	    String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM " + 
		"  [ŽŪB Nematekas$Salesperson_Purchaser] ";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	   
	    	
	    	  
		    Properties propse =  new Properties();
		    try{
		    	propse =  EksportFrame.loadProps("emails.txt");
		  	  
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		
	    	
	    	
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim() + " " + agent.getID();
	          String obj = rs.getString("objekt");
	          if(obj != null && obj.trim().length() > 0){
	        	  //agent.setObject(obj.trim());
	        	  if(!obj.trim().contains("CENTR")){
	        		 // agent.setIsPilot(true);
	        	  }
	          }
	         
	          agent.setName(name);
	          agent.setLanguage("LIT");
	          agent.setCountry("lt");
	          agent.setPassWord("0000");
            
	          String email = "";
	          
	          if(propse.get(agent.getID()) != null){
	        	  email = Converter.parseFirstEmail(propse.get(agent.getID())+"");
	          }
	             
	          if(email.length() > 3){
	        	  agent.setEmail(email);
	          }
	          if(email.length() > 0){
	        	  agent.setActive(true);
	          }
	          
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        
	        rs.close();
            stmt.close();
	        con.close();            
	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }
	    

     return agents;
   }

       public String getDistribution() {
         return null;
       }
   
  private HashSet sorts = new HashSet();
  

  
	
  
       public TreeMap getCustomers(Agent agent){

    	   try{
    		   Converter.getManagersReplacements(agents1, rcompany, user, pass, false); 
    			
    	}catch(Exception e){
    		e.printStackTrace();
    	}

   TreeMap<String,Double> debts = new TreeMap<String,Double>();
   TreeMap<String,Double> blokk = new TreeMap<String,Double>();
   TreeMap<String,Double> used = new TreeMap<String,Double>();

   
   
 String  sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
           " (SELECT     SUM(de.[Amount (LCY)]) FROM [ŽŪB Nematekas$Cust_ Ledger Entry] c INNER JOIN  " +
           "  [ŽŪB Nematekas$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
           "   WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
           "   FROM  [ŽŪB Nematekas$Cust_ Ledger Entry] c INNER JOIN " +
           "    [ŽŪB Nematekas$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
           "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [Open] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
			 " 	 ) " +
		     " 	 END AS used FROM [ŽŪB Nematekas$Customer] k "+
		      "	WHERE   (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

try {
Connection con3 = getNewConnection2();
Statement stmt3 = con3.createStatement();
ResultSet rs3 = stmt3.executeQuery(sql3);
while(rs3.next()){
	  String maksja  = rs3.getString("kood");
   double sum = rs3.getDouble("used");
   if(sum !=0){
	   used.put(maksja, sum);
   }
 }

rs3.close();
stmt3.close();
con3.close();
} catch (Exception e){
   e.printStackTrace();
 }

            sql3 =  " select  a.[Customer No_] AS maksja, sum(b.[Amount]) AS yle FROM [ŽŪB Nematekas$Cust_ Ledger Entry] a inner join " + 
                       " [ŽŪB Nematekas$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_]  " +
                       " where  (a.[Document Type] = 2 or a.[Document Type]= 3 or a.[Document Type]= 0) and a.[Due Date] <getdate()+1    and a.[Posting Date] > getdate()- 1000 " +
                       "  group by a.[Customer No_]  having sum(b.[Amount])<> 0  " ;

	    try {
	      Connection con3 = getNewConnection2();
	      Statement stmt3 = con3.createStatement();
	      ResultSet rs3 = stmt3.executeQuery(sql3);
	      while(rs3.next()){
	         String maksja  = rs3.getString("maksja").trim();
	         double sum = rs3.getDouble("yle");
	         if(sum !=0){
	        	 debts.put(maksja, sum);
	         }
	      }
	      
	      
	    String  sqlb =  "  select  a.[Customer No_] AS maksja, sum(b.[Amount]) AS yle , k.[Overdue Amount Incl_ Tolerance] as buff FROM [ŽŪB Nematekas$Cust_ Ledger Entry] a inner join " +
                  " [ŽŪB Nematekas$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] inner join [ŽŪB Nematekas$Customer] k on k.[No_] = a.[Customer No_]  " +
                  " where  (a.[Document Type] = 2 or a.[Document Type]= 3 or a.[Document Type]= 0) and a.[Due Date] < getdate() -  case when k.[Overdue Days Tolerance] > 300 then  300 else k.[Overdue Days Tolerance]  end "
                  + " and a.[Posting Date] > getdate()- 1000 " +
                  "  group by a.[Customer No_] ,k.[Overdue Amount Incl_ Tolerance] having sum(b.[Amount]) -  k.[Overdue Amount Incl_ Tolerance]  > 0" ;


	      rs3 = stmt3.executeQuery(sqlb);
	      while(rs3.next()){
	         String maksja  = rs3.getString("maksja").trim();
	         double sum = rs3.getDouble("yle");
	         if( sum > 0 ){
	        	 blokk.put(maksja, sum);
	         }
	         
	      }
 System.out.println("R7011 " + blokk.get("R7011"));
	      
rs3.close();
stmt3.close();
con3.close();
	    } catch (Exception e){
	         e.printStackTrace();
	       }


    	   
	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number,a.[Document No_] AS number2"
    	      		+ ", a.[Posting Date] AS kuupaev1 , a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [ŽŪB Nematekas$Cust_ Ledger Entry] a inner join " +
    	                            " [ŽŪB Nematekas$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-1000 AND a.[Open]= 1 " +
    	                            "  group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_],a.[Document No_], " +
    	                            " a.[Posting Date] ,a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection2();
    	       Statement stmt1 = conv.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient").trim();
    	        debt.setCustID(custID);
    	        String payerID = rs1.getString("maksja").trim();
                debt.setPayerID(payerID);
    	        String doknr = rs1.getString("number");
    	       
    	        String doknr2 = rs1.getString("number2");
    	        if(doknr != null && doknr.trim().length() > 0){
    	        	debt.setDoknr(doknr.trim());
    	        }else{
    	        	debt.setDoknr(doknr2.trim());
    	        }
    	        debt.setDate(rs1.getDate("kuupaev1"));
    	        debt.setDuedate(rs1.getDate("kuupaev"));
    	        debt.setSum(rs1.getDouble("summa"));
    	        int dueint = rs1.getInt("due");
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
    	       stmt1.close();
    	       conv.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	/// KLIENDI ANDMETE LEIDMINE

    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();


    	   String sql =  "   SELECT k.[Name] AS nimi, k.[Name 2] AS nimi2 , k.City as addr2 ,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address] + ' ' + k.[City] AS aadress " +  
    	"   	,k.[Salesperson Code] AS agent ,  k.Blocked AS blokk,   " +
    	"  [Payment Terms Code] AS payment,'' AS alekood,  (select p.[Credit Limit (LCY)] from  [ŽŪB Nematekas$Customer] p where k.[Bill-to Customer No_]= p.[No_]) AS krediit,'' AS visit,'' AS type,  " +
    	"	   0 AS extradays ,[Customer Price Group]  AS sort,'' AS debt  ,k.Contact +' ' + k.[Phone No_] as kontakt , 0 as sendall " +
    	" ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 , k.[Address] as street , k.[City]"
    	+ " ,k.[Post Code] as inde,  k.[E-Mail] as mail,k.[Registration No_] as regno , k.[Minimum Sales Amount] as minsum FROM [ŽŪB Nematekas$Customer] k  " +
	  	"	  WHERE k.[Salesperson Code] != ''  AND Blocked != 3  " ;

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
    	         String addr2 = rs.getString("addr2");
    	         if(addr2 != null){
    	        	
    	         }
    	         name = name.replaceAll("\"", " ");
    	         name = name.replace('*', ' ');
    	         name = name.replace('*', ' ');
    	         name = name.replace('*', ' ');
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer(id);
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(name) + " " + id);
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             }

    	           	
    	           	if(used.get(id) != null){
    	           		customer.setUsedCredit(used.get(id));
    	           	}
    	           	if(debts.get(customer.getPayer()) != null){
    	           		customer.setDebt(debts.get(customer.getPayer()));
    	           	}
    	           	
    	           	if(blokk.get(customer.getPayer()) != null && blokk.get(customer.getPayer()) > 0){
    	           		customer.setSalesBlockT(true);
    	           	}
    	        	if(blokk.get(customer.getID()) != null && blokk.get(customer.getID()) > 0){
    	           		customer.setSalesBlockT(true);
    	           	}
    	        	
    	        	if(customer.getID().equals("R7011")) {
    	        		System.out.println("R7011 " + customer.isSalesBlockT() + " " +  blokk.get(customer.getID()) + " " +   blokk.get(customer.getPayer()));
    	        	}
    	           	
    	             String aadress = rs.getString("aadress");
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Lithuania"));
    	               name = name + " " + Converter.enCodeEst(aadress);
    	              customer.setName(Converter.enCodeEst(name));
    	             }
    	         	String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag));
    	             customer.setMainAgent(ag.trim());
    	             
    	             if (ag != null ){
	           				Agent agg = ((Agent)this.getAgents1().get(ag));
	         				
	           				if(agg != null && agg.getDubbler().size()>0){
	           					//System.out.println("dubbing " + agg + "  " + agg.getDubbler().toString());
	           					for (int r = 0 ; r < agg.getDubbler().size() ; r++){
	           						Agent dub = new Agent((String)agg.getDubbler().elementAt(r));
	           						customer.addAgent(dub);
	           					}
	           				}
	           			}
    	             if (ag != null && ag.equals("MITU")){
    	 			          Enumeration enums = getParent().getAgentsHash().elements();
    	 			          while (enums.hasMoreElements()) {
    	 			            Agent a = (Agent) enums.nextElement();
    	 			            customer.addAgent(a);
    	 			          }

    	 			}

    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlockT(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	             customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
    	           	}
    	           	
    	
    	        	String visit = rs.getString("visit");
   	             if(visit != null && visit.trim().length()>0 && !visit.trim().toLowerCase().equals("x")){
   	                customer.setVisit(Converter.getVisitDay(visit.trim()));
   	             }

   	          if(customer.getPayer().equals("101937200")  ){
     	        	customer.setNodebt(true);
     	         }
    	      
    	           if (!customer.isNodebt()){
    	         
    	      			


    	        
    	           	//customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));
    	           }
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("D"," ");
    	        	customer.setPaymTerm(paym.trim());
    	             }

    	           
    	           
    	          //
    	           String cont = rs.getString("kontakt");
    	           if(cont != null && cont.trim().length()>0){
    	               customer.setPersonalData(cont.trim());   
    	           }
    	           
    	           String all = rs.getString("sendall");
    	           if("1".equals(all)){
    	        	   Enumeration enums = getParent().getAgentsHash().elements();
    	               while (enums.hasMoreElements()) {
    	                 Agent a = (Agent) enums.nextElement();
    	                 customer.addAgent(a);
    	               }
    	               customer.setMainAgent(" ");
    	           }
    	           
    	           	          

    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	       
    	        	if(customer.getGroup().trim().length() >0){
    	        	//customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	        	 	customer.setGroup(customer.getGroup() + "|" + Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	}else{
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	}
    	           }
    	           
    	           String street = rs.getString("street");
    	           if(street != null && street.trim().length()>0){
    	        	customer.restStreet = street.trim();   
    	           }   
    	           

    	           String city  = rs.getString("city");
    	           if(city != null && city.trim().length()>0){
    	        	customer.restCity = city.trim();   
    	           } 
    	           

    	           String inde  = rs.getString("inde");
    	           if(inde != null && inde.trim().length()>4){
    	        	customer.restZipCode = inde.trim();   
    	           } 
    	           
    	           

    	           String mail  = rs.getString("mail");
    	           if(mail != null && mail.trim().length()>0){
    	        	customer.restPerEmail = mail.trim();
    	        	
    	           } 
    	           
    	           String regno  = rs.getString("regno");
    	           if(regno != null && regno.trim().length()>0){
    	        	customer.setRegnr(regno.trim());
    	        	
    	           } 
    	           
    	           double minsum = rs.getDouble("minsum");
    	           if(minsum ==0 ){
    	        	  // minsum = Double.parseDouble(getParent().getProps().getProperty("minordsum"));
    	           }
    	           
    	           if(minsum > 0 ){
    	        	  
    	        	//   customer.setMinsum(minsum);
    	           }
    	           
    	            tm.put(customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	           
    	         }

    	        rs.close();
    	        stmt.close();
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
   private TreeMap<String,Double> ladutr = new TreeMap<String,Double>();
   private TreeMap<String,Double> ladupraak = new TreeMap<String,Double>();
  
   /**/
   
 
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     
     StringBuffer buf = new StringBuffer();
       ladutr = new TreeMap<String,Double>();
       ladupraak = new TreeMap<String,Double>();
       this.getSaveStockInfo();
       System.out.println("ladupraak " + ladupraak.size());
       
      TreeMap<String,Double>  prices = new TreeMap<String,Double>();
      TreeMap<String,Double>  pricesSood = new TreeMap<String,Double>();
      TreeMap<String,Double>  pricesS03 = new TreeMap<String,Double>();
      TreeMap<String,String>  yhikSood = new TreeMap<String,String>();
      
     try {
    	 String   cmd = "  SELECT [Item No_]  AS kood,  [Unit Price] AS hind ,[Starting Date] from   "+
    	  	         " [ŽŪB Nematekas$Sales Price] where [Sales Type] = 2 and [Location Code] != 'S03' and "+
    				  " (( getdate() between [Starting Date] and [Ending Date]+1 )  "+
    				  "  or ((getdate() > [Starting Date]) and  year ([Ending Date]) = 1753) ) and [Unit Price] != 0 "+
					"	order by [Item No_] ,[Starting Date] " ;

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood");
  	              	if(toode != null){
  	              	seis = rs.getDouble("hind");
  	              	prices.put(toode.trim(), seis);	
  	              	}
  	              	
  	        }

  	      rs.close();
  	
  	    cmd = " SELECT [Item No_]  AS kood,  [Unit Price] AS hind  , [Unit of Measure Code] as yhik from  " + 
  	          " [ŽŪB Nematekas$Sales Price] where [Sales Type] = 2 " //   and [Location Code] != 'S03'
  	          + "  and   (( getdate()+1 between [Starting Date] and [Ending Date]+1 ) "
  	          + "or (getdate()+1 > [Starting Date]) and year([Ending Date]) =1753) and [Sales Campaign] = 1 order by [Sales Campaign]  ";
  	  rs = stmt.executeQuery(cmd);
  	  Vector check = new Vector(); 
  	  while(rs.next()){
             String toode = null;
             double seis = 0;;
              	toode =rs.getString("kood");
              	if(toode != null){
              	seis = rs.getDouble("hind");
              	pricesSood.put(toode.trim(), seis);	
              	String yh = rs.getString("yhik");
              	if(yh != null && yh.trim().length() > 0 ){
              		yhikSood.put(toode.trim(), yh.trim());	
              		if(seis>0 ){
                       	if(!check.contains(toode+yh.trim())){
                               buf.append(toode+ ";" + yh.trim()
                               		+ ";" + yh.trim() + ";" + seis);
                               buf.append("\n");	
                       	}
             	      }
             	    check.add(toode+yh.trim());
              	}
              		
              	}
              	
        }
        
        
      
      
        
        cmd = " SELECT [Item No_]  AS kood,  [Unit Price] AS hind ,[Starting Date] from  " +
	  	        "   [ŽŪB Nematekas$Sales Price] where [Sales Type] = 2 and [Location Code] = 'S03' and " +
				 "   (( getdate()+1 between [Starting Date] and [Ending Date]+1 ) " +
				  "  or ((getdate()+1 > [Starting Date]) and  year ([Ending Date]) = 1753) ) and [Unit Price] != 0 order by [Item No_] ,[Starting Date] ";
	  	 

    	  rs = stmt.executeQuery(cmd);
          while(rs.next()){
               String toode = null;
               double seis = 0;;
                	toode =rs.getString("kood");
                	if(toode != null){
                	seis = rs.getDouble("hind");
                	pricesS03.put(toode.trim(), seis);	
                	}
                	
          }
        
  	      
  	      stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
      
   //  getFixedAssortmentData();
 
  
     try {
     	String cmd = "  SELECT t.[No_] AS toode,t.[Description] AS nimi, 0 AS myygihind, " +
     	"	 t.[Product Group Code] AS grupp   " +
"	 ,t.[Base Unit of Measure] AS unit,[External Barcode] AS ean,'' AS status ,0 AS maxdiscount," +
"			 '' AS type, [Units per Parcel] AS kastis,  [Approximate Weight]   AS kaal, [VAT Prod_ Posting Group] AS vat,[Item Disc_ Group] as grd " +
"			,[Vadybininku preke] as goo  , [MinOrderAmount] as minam , t.[location Code] as ware , t.[Order Receipt Time To] as mintime "
+ "  FROM [ŽŪB Nematekas$Item] t   left join [ŽŪB Nematekas$Item Marking] ref on"
		+ " ref.[Item No_]=t.[No_]   and ref.Code like  '%VISI PIRK%' " +
"			  WHERE t.[Blocked]=0 and ref.Code like  '%VISI PIRK%' and  t.[Blocked sale_purch_] = 0  and  t.[location Code] in ('S01','S02','S03','S41')  ORDER BY t.[No_]"; 
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
              	 
            
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	name = name.replace('{', '(');
         	name = name.replace('}', ')');
         	name =   Converter.firstToUpperCase(name);
         	name = name + " " + id;
         	
             double price = 1 ;
             
             if(prices.get(id) != null){
            	 price = prices.get(id);
             }
             
             if(pricesSood.get(id) != null){
            	 price = pricesSood.get(id);
            	 prod.setStatusDiscount("1");
            	 if(yhikSood.get(prod.getID()) != null){
                 	//name = name + " " + (yhikSood.get(prod.getID()));
                 }
             }
              
             
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(ladutr.get(id));
              }
             
              
                String grupp = Converter.enCodeEst(rs.getString("grupp"));
                
                //asendame brandiga kui tootel pole gruppi 
              if(grupp != null && grupp.trim().length()>0 ){
               prod.setGroup(getGrCode(grupp.trim()));
              }
          
                prod.setUnit(rs.getString("unit"));
                
                if(yhikSood.get(prod.getID()) != null){
                //	prod.setUnit(yhikSood.get(prod.getID()));
                }
            
                String ean = rs.getString("ean");
                
                if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                 }
        
             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}

                	 double	kaal = rs.getDouble("kaal");
                 	if(kaal>0){
                       prod.setName(name + " " + ((int)(kaal* 1000)) + "g");
                     
                  // 	System.out.println("kaal " + kaal);
                 	}
                 	
                 

                 	 
                 	 
                 	 prod.setVat("21");
                           

                 	     String grd = rs.getString("grd");
                	      if(grd != null && grd.trim().length()>0){
                           	prod.setDiscgrupp(Converter.cleanLitLatEst(grd.trim()));
                          }

                	      int goo = rs.getInt("goo");
                          	
                   String add = "";
                   if(id.length() > 5){
                	   add = id.substring(0,5);
                   }
                   
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>3){
                        // ee = " " + ean.substring(ean.trim().length()-3);
                       }
                	 
                	double minam = rs.getDouble("minam");
                	 
                	if (minam > 1){
                		prod.setMinAmountInUnits(minam);
                	}
                	 prod.setName(Converter.enCodeEst(name) + ee);
                    
                	 String ware = rs.getString("ware");
                	 if(ware != null && ware.trim().length() > 0 && ware.trim().equals("S41")){
                		 
                		 prod.setInUnit(1);
                	 }
                	 
                java.sql.Time  time =  rs.getTime("mintime");
             //   System.out.println("time " + time);
                if (time != null &&  time.getTime() > 100){
                	prod.setTimeToSell(time.getTime());
                }
                	
               
                	 if(ladupraak.get(id) != null ){
                      	  prod.setName(prod.getName() + " @S3:" + ((int)(ladupraak.get(id).doubleValue())));
                      	  if(pricesS03.get(id) != null){
                      		  prod.setName(prod.getName() + "->" + (NF.format(pricesS03.get(id))) + "€");
                      		  prod.setType("S03");
                      	  }
                      	  
                      	  System.out.println("praak " + prod.getName());
                        }
                	 if( goo ==1){
                		 tm.put( name, prod); 
                     }
                
          
        }
  
        rs.close();
        stmt.close();
        con.close();
        
        System.out.println(buf.toString());
    	if(buf.length() > 2){
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write(buf.toString());
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }else{
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write("");
        		os.close();
        	  }catch (IOException ex) {
        		ex.printStackTrace();
        	  }
         }
        
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
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
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount" +
    		" FROM [ŽŪB Nematekas$Sales Shipment Line] r " +
    "  INNER JOIN [ŽŪB Nematekas$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [ŽŪB Nematekas$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-30 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();

                 String prodID = rs.getString("toode").trim();;

                 double amount = 0;
                 String lastdate = " ";
                 amount = rs.getDouble("kogus");
                 lastdate = rs.getString("lastdate").trim().substring(5,10);
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    
                    
                    double lastamount = rs.getDouble("lastamount");
                    double percent = 0;
                    if(amount>0){
                    	percent =  amount / lastamount * 100;
                    }
                    
               
                    double open = 0;
                    if(amount == 0 && lastamount >0){
                    	open = lastamount;
                       
                    }else if (percent < 70){
                    	open = lastamount - amount ;
                    }
                    
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,open,0," ");

                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);

           }
         rs.close();
         stmt.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [ŽŪB Nematekas$Sales Invoice Line] r " +
		  " INNER JOIN [ŽŪB Nematekas$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
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
                  kogus = rs2.getDouble("kogus")+"";
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
     return "NaviNematekas";
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
   private String url2 = "jdbc:odbc:navi";
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);
   private TreeMap debtDays = new TreeMap();
 
   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}



public void setBulkSales(Map agents) {

 	 try {
 	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [ŽŪB Nematekas$Cust_ Ledger Entry] " +
 	 			"where Month(getdate())=Month([Posting Date]) and " +
            "Year(getdate())=Year([Posting Date]) " +
             " group by [Salesperson Code]";// and [Global Dimension 1 Code]='400'
 	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
                String agentID = rs.getString("agent");
                double sales = rs.getDouble("summa");
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)(sales));
                 }
           }
	        rs.close();
	        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }

 }


}
