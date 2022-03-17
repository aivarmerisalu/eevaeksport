package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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

public class NaviValdoDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,FixLoader,PictureMaker {

  public NaviValdoDBDriver(EksportPanel parent) {
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
            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi , [Akcija]  as camp from" +
            "   [VOLDEMĀRS SIA$Sales Price] where (getdate()+5 between [Starting Date] and [Ending Date]+5  or  " +
                       "  (year ([Ending Date]) = 1753 and getdate()+5 > [Starting Date]) ) and [Sales Type] = 0   " +
                          "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup,  " +
             "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi , [Akcija] as camp from  " +
             "  [VOLDEMĀRS SIA$Sales Price] where ( getdate()+1 between [Starting Date] and [Ending Date] or (getdate()+1 > [Starting Date]) and  year ([Ending Date]) = 1753) " +
			  "  and [Sales Type] = 1 " +   
                          "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
            "   0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi ,'' as camp " +
                       "  from [VOLDEMĀRS SIA$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0   " +     
                   "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
              " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi ,'' as camp  " +
                       "  from [VOLDEMĀRS SIA$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()-1 or  " +
                      "   year ([Ending Date]) = 1753) " +
                      " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                     " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi,'' as camp " +
                     "    from [VOLDEMĀRS SIA$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1        " +
                    " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
                   " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi,'' as camp " +
                    "     from [VOLDEMĀRS SIA$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()-1 or " + 
                     "    year ([Ending Date]) = 1753) ) as ta order by custid , clgroup, prodid ,paevi " ; 
			
  	 

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
             String camp  = rs.getString("camp");
             
             MatrixLine mline = new MatrixLine();

             if (custID != null && custID.length() > 0) {
               mline.setCliGroup(custID); //asendasin cliGroup kuna cust asemel on payer
             }else{
            	 custID = ""; 
             }
             
             
             
          
             
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
             }

             
           
             
            if( paevi < 60 || camp.length() > 0 ){
            	 if(sdate != null  && !sdate.startsWith("1753")){
                	 mline.setStartDate(sdate.substring(0,10));
                 }
                 if(date != null && !date.startsWith("1753")){
                	 mline.setEndDate(date.substring(0,10));
                 }
             }else if(paevi > 60){ //  && camp == 0
            	// mline.setProdLevel("pr");
             }
             
             if(camp != null && camp.trim().length() > 0) {
            	 mline.setLineinfo(camp);
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
             
             
             if((mline.getPrice() > 0 || mline.getDiscount() > 0) && !olemas.contains(clGroup+"" + prodID + custID)){
            	               result.add(mline); 
                     olemas.add(clGroup+"" + prodID + custID);
            	 
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
		File f = new File(this.getExcelDir() + "sortimendid.xls");
		try {
			
			
  	        
			FileInputStream ff = new FileInputStream(f);
			HSSFWorkbook workbook = new HSSFWorkbook((ff));
			HSSFSheet sheet = workbook.getSheet("Atria Tooted");
			HSSFRow row = null;
			TreeMap groups = new TreeMap();
			row = sheet.getRow(3);
			// MÃÃRAME ÃRA MIS VÃLJAS ASUVAD Sortimendi tÃ¤hised.

			// System.out.println("rownum " + row.getLastCellNum());
			for (int a = 2; a < row.getLastCellNum(); a++) {
				try {
					// System.out.println(row.getCell((short)a).getStringCellValue());
					groups.put(a + "", row.getCell((short) a)
							.getStringCellValue());
				} catch (Exception e) {
					// System.out.println(row.getCell((short)a).getNumericCellValue());
				}
			}

		//	 System.out.println("groups " + groups.toString());

			for (int i = 5; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				String prodID = "";

				if (row.getCell((short) 0) != null) {

					try {
						prodID = (int) (row.getCell((short) 0)
								.getNumericCellValue()) + "";
					} catch (Exception e) {
						prodID = (row.getCell((short) 0).getStringCellValue());
					}
					
                       if(!prodID.equals("0")){
                    	
					// Siin lÃ¤heme reasisse rokkima ja vaatama kas se toode
					// lÃ¤heb kaasa
					for (int a = 2; a < row.getLastCellNum(); a++) {

						String group = (String) groups.get(a + "");
						if (group != null && group.trim().length() > 0) {

							String custID = " ";
							String clGroup = " ";
							String prodGroup = " ";

							String go = null;
							clGroup = group;

							MatrixLine mline = new MatrixLine();
							// Vaatame kas selles veerus on sellel tootel pandud
							// mingi tÃ¤his
							if (row.getCell((short) a) != null) {

								try {
									go = (int) (row.getCell((short) a)
											.getNumericCellValue()) + "";
								} catch (Exception e) {
									go = (row.getCell((short) a)
											.getStringCellValue());
								}

							}

							if (go != null && ( go.toLowerCase().equals("x") || go.length() > 3)) {

							// System.out.println( a + " " + prodID + " " +
								// clGroup + " " + go);

								if (custID != null
										&& custID.trim().length() > 0) {
									mline.setCustID(custID.trim());
								}
								if (clGroup != null
										&& clGroup.trim().length() > 0) {
									mline.setCliGroup(clGroup.trim());
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								if (mline.getProdID() == null
										&& prodGroup != null
										&& prodGroup.trim().length() > 0) {
									mline.setProdGroup(prodGroup);
								}
								
								if(go.length()> 3){
									mline.setLineinfo("Partner:" + go);
								}
								result.add(mline);
							}
						}
					}
					   
                       }
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

		// System.exit(0);

		return result;

	}
    private String notNull(String res){
    	if(res == null){
    		return "";
    	}else{
    		return res;
    	}
    }



   public DealerProduct getDealerProductsList() {
 return null;
      }

   
   @Override
	public void FixLoader() {
		this.getProducts(null);
		TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
		System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
		if(contracts.size() > 0) {
			Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
		}
		
		System.exit(0);
	}

       public String getSaveStockInfo(){

 	  	  
    		  String  cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [VOLDEMĀRS SIA$Item Ledger Entry] l " + 
    "              INNER JOIN [VOLDEMĀRS SIA$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('010P','080P','015A') and t.[Send to E-Pasaule] = 1  " + 
    "  		   GROUP BY l.[Location Code], l.[Item No_]  having sum(l.[Quantity])  != 0 " ;
	
    		  
    		  String  cmd2 = "  SELECT l.[No_] AS kood, sum(l.[Quantity]) AS kogus FROM [VOLDEMĀRS SIA$Sales Line] l " + 
    		  		"                  INNER JOIN [VOLDEMĀRS SIA$Item] t ON t.[No_]=l.[No_] WHERE  t.[Send to E-Pasaule] = 1 " + 
    		  		"			      GROUP BY  l.[No_]  having sum(l.[Quantity])  != 0 " ;
    		  
    				
    		  
    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
                        Statement st = con.createStatement();
      		           ResultSet rs = st.executeQuery(cmd2);

      		     
      		           
      		           
      		       while (rs.next()) {
      		         String kood = rs.getString("kood");
	            		 if(kood != null){
                            kood = kood.trim();
	                        double seis = rs.getDouble("kogus"); 
	                        ladutr.put( kood, seis * -1);  
      		           }
      		       }
                  
      		       rs.close();
  		        
      		           
      		           
      		           
      		           
      		         rs = st.executeQuery(cmd);
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
    		             
    		             if(seis > 0){
    		                 if(ladutr.get(kood) != null){
    		                		 double olem = Double.parseDouble(ladutr.get(kood)+"");
    		                		 ladutr.put( kood, seis + olem); 
    		                	 }else{
    		                		 ladutr.put( kood, seis );  
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

           
           public TreeMap getProdGroupsSecond()  throws SQLException {
      	     TreeMap   tm = new TreeMap();
      	    
      	     String cmd = "SELECT  t.[Product Group Code] AS kood ,g.[Description] AS nimi FROM [VOLDEMĀRS SIA$Item] t inner join [VOLDEMĀRS SIA$Product Group] g ON t.[Product Group Code]=g.[Code]\r\n"
      	     		+ "where t.[Gen_ Prod_ Posting Group] = 'PRECES'   and t.[Product Group Code]  != ''\r\n"
      	     		+ "group by t.[Product Group Code],g.[Description] order by t.[Product Group Code] 	 ";
      	       	 Connection con = getNewConnection2();
      	       	System.out.println(cmd);
      	   	        Statement stmt = con.createStatement();
      	   	        ResultSet rs = stmt.executeQuery(cmd);
      	   	        while(rs.next()){
      	   	            String id = Converter.cleanLitLatEst(rs.getString("kood"));     
      	                String name = (rs.getString("nimi")) ;
      	   	             tm.put((id), name);  	             
      	   	        }
      	   	      rs.close();
      	   	      con.close();


      	         return tm;
      	   }

           
        
           
           

         
           public TreeMap  getProdGroups() throws SQLException {
        	   TreeMap   tm = new TreeMap();
        	   
        	     String cmd = " SELECT  t.[Item Brend Dimension Code] AS kood ,g.[Name] AS nimi FROM [VOLDEMĀRS SIA$Item] t " + 
        	     		"					  inner join [VOLDEMĀRS SIA$Dimension Value] g ON t.[Item Brend Dimension Code]=g.[Code] and g.[Dimension Code] = 'Y-BRENDS' " + 
        	     		"        	     		where   [Send to E-Pasaule] = 1 " + 
        	     		"        	     	group by t.[Item Brend Dimension Code],g.[Name] order by t.[Item Brend Dimension Code]  ";
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = Converter.cleanLitLatEst(rs.getString("kood"));     
        	                String name = (rs.getString("nimi")).replaceAll("Valdo", "Val")
        	                		.replaceAll("Tautas", "Tau") 
        	                		.replaceAll("TAUTAS", "Tau")
        	                		.replaceAll("Draugu konditorija", "DrKo") ;
        	                 

        	   	             tm.put((id), name.trim());  	             
        	   	        }
        	   	      rs.close();
        	   	      con.close();


        	         return tm;
             }


   
   public TreeMap getAgents(){
	   
	    String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM  " + 
	    		"		  [VOLDEMĀRS SIA$Salesperson_Purchaser] where [Send to E-Pasaule] = 1 order by[Code] ";
	    
	    
        
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
	          agent.setLanguage("EST");
	          agent.setCountry("ee");
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


	        Agent aa = new Agent("HOUSE");
	        aa.setName("Kontor");
	        agents.put(aa.getName(), aa);
	        agents1.put(aa.getID(), aa);
	        
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
  
private HashSet getNoDebts() {
	
	  HashSet nodebts = new HashSet();
	  
	  nodebts.add("19037900"); //MAXIMA Latvija, SIA
	  nodebts.add("19008400"); // RIMI LATVIA, SIA
	  nodebts.add("19000121"); // RANVUS, SIA
	  nodebts.add("19000015"); //LENOKA, SIA
	  nodebts.add("16109200"); // BALTIC RESTAURANTS LATVIA, SIA
	  nodebts.add("11433000"); // FIRMA MADARA 89, SIA
	  nodebts.add("16141863"); // DEPO DIY, SIA
	  nodebts.add("19033900"); // VITA MĀRKETS, SIA
	  nodebts.add("16000396"); // GABRIĒLA, SIA
	  nodebts.add("16141952"); // VIRŠI-A, AS
	  nodebts.add("19000708"); // TOPLOGISTIC, SIA
	  nodebts.add("10500385"); // DGR SOLUTIONS, SIA
	  nodebts.add("16110100"); // DEVONA, SIA
	  nodebts.add("16049100"); // SANITEX ,SIA
	  nodebts.add("16141863"); // DEPO DIY LT, UAB
return nodebts;
	
}
  
	
  
       public TreeMap getCustomers(Agent agent){

    	   try{
    		   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
    			
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	   

    	HashSet noDebts = this.getNoDebts();
    	   
   TreeMap<String,Double> debts = new TreeMap<String,Double>();
   TreeMap<String,Double> blokk = new TreeMap<String,Double>();
   TreeMap<String,Double> used = new TreeMap<String,Double>();
   

   String sqlus = "   SELECT l.[Sell-to Customer No_] AS kood,  sum([Shipped Not Invoiced (LCY)]) as summa FROM [VOLDEMĀRS SIA$Sales Line] l " + 
   		"                  GROUP BY  l.[Sell-to Customer No_]   having sum([Shipped Not Invoiced (LCY)] )  != 0 ";

try {
Connection con3 = getNewConnection2();
Statement stmt3 = con3.createStatement();
ResultSet rs3 = stmt3.executeQuery(sqlus);
while(rs3.next()){
	  String maksja  = rs3.getString("kood");
   double sum = rs3.getDouble("summa");
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

   
 String  sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
           " (SELECT     SUM(de.[Amount (LCY)]) FROM [VOLDEMĀRS SIA$Cust_ Ledger Entry] c INNER JOIN  " +
           "  [VOLDEMĀRS SIA$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
           "   WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
           "   FROM  [VOLDEMĀRS SIA$Cust_ Ledger Entry] c INNER JOIN " +
           "    [VOLDEMĀRS SIA$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
           "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [Open] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
			 " 	 ) " +
		     " 	 END AS used FROM [VOLDEMĀRS SIA$Customer] k "+
		      "	WHERE   (Blocked <> 3) AND ([Salesperson Code] <> '') " ;
 
 

try {
Connection con3 = getNewConnection2();
Statement stmt3 = con3.createStatement();
ResultSet rs3 = stmt3.executeQuery(sql3);
while(rs3.next()){
	  String maksja  = rs3.getString("kood");
   double sum = rs3.getDouble("used");
   if(sum !=0){
	   if(used.get(maksja) != null) {
		   double vana = used.get(maksja);
		   used.put(maksja, sum + vana);
	   } else {
		   used.put(maksja, sum);
	   }
	   used.put(maksja, sum);
   }
 }

rs3.close();
stmt3.close();
con3.close();
} catch (Exception e){
   e.printStackTrace();
 }

            sql3 =  " select  a.[Customer No_] AS maksja, sum(b.[Amount]) AS yle FROM [VOLDEMĀRS SIA$Cust_ Ledger Entry] a inner join " + 
                       " [VOLDEMĀRS SIA$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_]  " +
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
	    String  sqlb =  "  select  a.[Customer No_] AS maksja, sum(b.[Amount]) AS yle  FROM [VOLDEMĀRS SIA$Cust_ Ledger Entry] a inner join " +
                  " [VOLDEMĀRS SIA$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] inner join [VOLDEMĀRS SIA$Customer] k on k.[No_] = a.[Customer No_]  " +
                  " where  (a.[Document Type] = 2 or a.[Document Type]= 3 or a.[Document Type]= 0) and a.[Due Date] < getdate()-8    and a.[Posting Date] > getdate()- 1000 " +
                  "  group by a.[Customer No_]  having sum(b.[Amount]) > 0" ;


	      rs3 = stmt3.executeQuery(sqlb);
	      while(rs3.next()){
	         String maksja  = rs3.getString("maksja").trim();
	         double sum = rs3.getDouble("yle");
	         if(sum !=0){
	        	 blokk.put(maksja, sum);
	         }
	         
	      }

	      
rs3.close();
stmt3.close();
con3.close();
	    } catch (Exception e){
	         e.printStackTrace();
	       }


    	   
	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_]  + '_' + a.[Ship-to code] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number,a.[Document No_] AS number2"
    	      		+ ", a.[Posting Date] AS kuupaev1 , a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [VOLDEMĀRS SIA$Cust_ Ledger Entry] a inner join " +
    	                            " [VOLDEMĀRS SIA$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-1000 AND a.[Open]= 1 " +
    	                            "  group by a.[Sell-to Customer No_] + '_' + a.[Ship-to code],a.[Customer No_] ,a.[External Document No_],a.[Document No_], " +
    	                            " a.[Posting Date] ,a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection2();
    	       Statement stmt1 = conv.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient").trim();
    	       if(custID.endsWith("-")) {
    	    	   custID = custID.substring(0,custID.length()-1);
    	       }
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
    	        
    	        if (!noDebts.contains(payerID)) {
    	            this.debtList.add(debt);
    	        }
    	   
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


    	   String sql =  "  SELECT k.[Name] AS nimi, k.[Name 2] AS nimi2  ,k.[No_] AS kood,a.Code as abikood ,k.[Bill-to Customer No_] AS payer  , a.[Address] + ' ' + a.[City] as address \r\n" + 
    	   		"	,a.[Salesperson Code] AS agent ,  k.Blocked AS blokk,   \r\n" + 
    	   		" [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type, " + 
    	   		"	   0 AS extradays ,[Customer Price Group]  AS sort,'' AS debt  ,a.Contact +' ' + a.[Phone No_] as kontakt , 0 as sendall " + 
    	   		",[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 , a.[Address] as street , a.[City] " + 
    	   		", a.[Post Code] as inde,  a.[E-Mail] as mail,'' as regno , 0 as minsum , dim.Name as chainname , dim.[Code] as chain , k.[Allow Line Disc_] as allowline "
    	   		+ " FROM [VOLDEMĀRS SIA$Ship-to Address] a LEFT join  [VOLDEMĀRS SIA$Customer] k    on  a.[Customer No_]=k.[No_]  "
    	   		+ " LEFT JOIN [VOLDEMĀRS SIA$Dimension Value] dim ON dim.[Code] = a.[Customer Group Dimension Code] and dim.[Dimension Code] = 'Y-TIKL'" + 
    	   		"	  WHERE a.[Salesperson Code] != ''  AND k.Blocked != 3   and a.[Send to E-Pasaule] = 1 " ;
    	   
    	   
    	   

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	         String name2 = rs.getString("nimi2");
    	         if(name2 != null){
    	        	// name = (name + " " + name2.trim()).trim();
    	         }
    	       
    	         name = name.replaceAll("\"", " ");
    	         name = name.replace('*', ' ');
    	         name = name.replace('*', ' ');
    	         name = name.replace('*', ' ');
    	           String id = rs.getString("kood").trim();
    	           String id2 = rs.getString("abikood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer(id + "_" + id2);
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(name) + " " + id);
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             }
    	            customer.setPayer(id);
    	           	
    	            if (!noDebts.contains(id)) {
    	            	if(used.get(id) != null){
        	           		customer.setUsedCredit(used.get(id));
        	           	}
        	           	if(debts.get(customer.getPayer()) != null){
        	           		customer.setDebt(debts.get(customer.getPayer()));
        	           	}
        	           	
    	            }
    	            
    	           
    	           	if(blokk.get(customer.getPayer()) != null && blokk.get(customer.getPayer()) > 0){
    	           		customer.setSalesBlock(true);
    	           	}
    	           	
    	     
    	           	
    	             String aadress = rs.getString("address");
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Latvia"));
    	               name = name + " " + Converter.enCodeEst(aadress);
    	              customer.setName(Converter.enCodeEst(name));
    	             }
    	         	String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag.trim()));
    	             customer.setMainAgent(ag.trim());
    	             
    	            // customer.addAgent(new Agent("HOUSE"));
    	             
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
    	               customer.setSalesBlock(true);
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

    	           String chainname  = rs.getString("chainname");
    	           if(chainname != null && chainname.trim().length()>0){
    	        	customer.setCity("Tīkls :" +  chainname.trim());
    	           } 
    	           
    	           String chain  = rs.getString("chain");
    	           if(chain != null && chain.trim().length()>0){
    	        	customer.setChain(chain.trim());
    	           } 
    	           
    	           int allowline  = rs.getInt("allowline");
    	           if(allowline > 0){

    	        	   if(customer.getGroup().trim().length() >0){
    	    	        	//customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	    	        	 	customer.setGroup(customer.getGroup() + "|" + Converter.cleanLitLatEst("ldok")); 	
    	    	        	}else{
    	    	        	 	customer.setGroup(Converter.cleanLitLatEst("ldok")); 
    	    	        	}
    	        	   
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
   
   private TreeMap loadMore() {
	   TreeMap res = new TreeMap();
	   try {
		   FileInputStream is = new FileInputStream(new File("eans.txt"));
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			BufferedReader reader = new BufferedReader(isr);
			
			
		    String         line = null;
		 
		    try {
		        while((line = reader.readLine()) != null) {
		      StringTokenizer st = new StringTokenizer(line,";");
		      while(st.hasMoreTokens()) {
		    	  res.put(st.nextToken(), st.nextToken());
		      }
		        }

		       
		    } finally {
		        reader.close();
		    }
		 
	   }catch(Exception e) {
		   
	   }
	   
	
	   
	 System.out.println("lis suurus " + res.size());
	   
	   return res;
   }
   
   private TreeMap eanprods = null;
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     
     
     eanprods =  loadMore();
       ladutr = new TreeMap<String,Double>();
       ladupraak = new TreeMap<String,Double>();
       this.getSaveStockInfo();
       
      TreeMap<String,Double>  prices = new TreeMap<String,Double>();
      TreeMap<String,String>  yhikSood = new TreeMap<String,String>();
      
     try {
    	 String   cmd = "  SELECT [Item No_]  AS kood,  [Unit Price] AS hind  from   "+
    	  	         " [VOLDEMĀRS SIA$Sales Price] where [Sales Type] = 2  and "+
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
  	    System.out.println("prices Size " + prices.size());
  	 
  	      
  	      stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
      
   //  getFixedAssortmentData();
 
  
     try {
     	String cmd = "SELECT t.[No_] AS toode,t.[Description] AS nimi, [Unit Price] AS myygihind, \r\n"
     			+ "[Product Group Code] AS grupp  ,[Item Brend Dimension Code] as grupp2  \r\n"
     			+ " ,t.[Base Unit of Measure] AS unit,[Barcode] AS ean,'' AS status ,0 AS maxdiscount,\r\n"
     			+ "   [Item Type] AS type, [Units per Parcel] AS kastis,  [Net Weight]   AS kaal, [VAT Prod_ Posting Group] AS vat,[Item Disc_ Group] as grd ,"
     			+ "(case when [Shelf life period type] = 2 then [Shelf life period]  else  [Shelf life period]*30 end) as maxdays , [Units per pallet] as pallet ,[Reorder Point] as optimum "
     			+ "	  FROM [VOLDEMĀRS SIA$Item] t \r\n"
     			+ "	  WHERE t.[Blocked]=0 and  t.[Gen_ Prod_ Posting Group] = 'PRECES'  and   [Send to E-Pasaule] = 1  ORDER BY t.[No_]"; 
     	
     	
     	
     	
      System.out.println(cmd);
      
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
              	 
            
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	name = name.replace('{', '(');
         	name = name.replace('}', ')');
         //	name =   Converter.firstToUpperCase(name);
         //	name = name + " " + id;
         	
             double price = rs.getDouble("myygihind");
             if(price > 0){
            	  prod.setPrice(price);
              }
             
              
                
              if(ladutr.get(id) != null && ladutr.get(id) > 0){
            	  prod.setStock(ladutr.get(id));
              }
             
              
                String grupp = Converter.cleanLitLatEst(rs.getString("grupp"));
                
              if(grupp != null && grupp.trim().length()>0 ){
                 prod.setGroupSecond(grupp.trim());
              }
              String grupp2 = Converter.cleanLitLatEst(rs.getString("grupp2"));
              
              if(grupp2 != null && grupp2.trim().length()>0 ){
                 prod.setGroup(grupp2.trim());
              }
          
                prod.setUnit(rs.getString("unit"));
                
                if(yhikSood.get(prod.getID()) != null){
                //	prod.setUnit(yhikSood.get(prod.getID()));
                }
            
                String ean = rs.getString("ean");
              
                if (ean != null && ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                   eanprods.put(ean.trim(), prod.getID());
                   
                 }
        
           
             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

             	  String type = rs.getString("type");
           	      if(type != null && type.trim().length() > 0 ){
           	    	  	prod.setType(type.trim());
                     }
                 	 
             	
                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}

                	 double	kaal = rs.getDouble("kaal");
                 	if(kaal>0){
                   //    prod.setInUnit(kaal);
                  	}
                 	
                 
            	     String vat = rs.getString("vat");
           	      if(vat != null && vat.trim().equals("PVN21")){
           	    	  	prod.setVat("21");
                     }
                 	 
                 	 
                           

                 	     String grd = rs.getString("grd");
                	      if(grd != null && grd.trim().length()>0){
                           //	prod.setDiscgrupp(Converter.cleanLitLatEst(grd.trim()));
                          }
                	      String maxdays = (rs.getString("maxdays"));
                	      
                	      if(maxdays != null && maxdays.trim().length()>0 && !maxdays.trim().equals("0") ){
       			        	prod.setPartii("MaxDays " + maxdays.trim());
       			        }
                          	
               
                	      
                	      double	pallet = rs.getDouble("pallet");
                       	if(pallet > 0){
                             prod.setOnPallet(pallet);
                        	}
                       	
                       	
              	      double	opt = rs.getDouble("optimum");
                     	if(opt > 0){
                           prod.setOptAmountInUnits(opt);
                      	}
                     	
                       	
                       	
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>3){
                         ee = " " + ean.substring(ean.trim().length()-3);
                       }
                	 
                	 	 prod.setName(Converter.enCodeEst(  name.trim() + " " + ee ));
                          
                	
               
                	 tm.put( name, prod); 
                
          
        }
        
        
        
        
        
        
        
  
        rs.close();
        stmt.close();
        con.close();
        
        
        
        
  	  cmd = " SELECT [Item No_] AS prodid,  [Unit Price] AS price , [Minimum Quantity]  as kogus, [Ending Date] as paev, [Starting Date] as startday  from \r\n" + 
  	  		"                              [VOLDEMĀRS SIA$Sales Price] where (getdate()+5 between [Starting Date] and [Ending Date]+5  or   \r\n" + 
  	  		"                               (year ([Ending Date]) = 1753 and getdate()+5 > [Starting Date]) )  and [Sales Type] = 2  ";
  	 
  		
  	    StringBuffer buf = new StringBuffer();
  		try {
  			
  			 con = this.getNewConnection2();
  		     stmt =  con.createStatement();
  		      rs = stmt.executeQuery(cmd);

  	           while(rs.next()){
  	        	        String  gruppx = "ldok";
  	                  	String  toodex = rs.getString("prodid").trim();
  	                  	try{
  	                  	double  hind  = rs.getDouble("price");
  	                   	    double  kogus = rs.getDouble("kogus");
  	                  	
  		                  	
  		                  	if(kogus == 0){
  		                  		kogus = 1;
  		                  	}
  		                  	      	
  		                  	      buf.append(toodex+ "; ; ;" + NF.format(hind).replace(',', '.') + ";" + gruppx + ";" + kogus + ";"  );
  		                          buf.append("\n");	
  		                      	
  	                  		
  	                  	}catch(Exception e){
  	                  	 e.printStackTrace();	
  	                  	}
  	                  }	
  	           rs.close();
  	           stmt.close();
  	           con.close();
        
        
      
        if(buf.length() > 2){
            File f = new File("prodactions.txt");
            if (f.exists())f.delete();
              try {
        		PrintWriter os = new PrintWriter(new FileWriter(
        				 "prodactions.txt", true));
        		os.write(Converter.cleanLitLatEst(buf.toString()));
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
        
        
  	   } catch (Exception t){
  	       t.printStackTrace();
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
    String cmd =  " SELECT  d.[Sell-to Customer No_] AS klient,  d.[Ship-to Code]  as abiklient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount," + 
    		"    (r.[Unit Price]*(100-r.[Line Discount _])/100) as lastprice	, dd.[External Document No_]  as lastdoc , dd.[Order No_]  as lastdoc1 , "
    		+ " (case when ([Shelf life period] = 0 or [Shelf life period type] = 0 or d.[Posting Date]+ (case when [Shelf life period type] = 2 then [Shelf life period]  else  [Shelf life period]*30 end) > getdate()) then 0 else 1 end) as paevi  "
    		+ "		FROM [VOLDEMĀRS SIA$Sales Shipment Line] r " + 
    		"      INNER JOIN [VOLDEMĀRS SIA$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [VOLDEMĀRS SIA$Item] t ON t.[No_]=r.[No_] "
    		+  "  LEFT JOIN [VOLDEMĀRS SIA$Sales Invoice Header] dd ON dd.[Linked Sales Shipment No_] = d.[No_] WHERE " + 
    		"      d.[Posting Date]> GETDATE()-300 ORDER BY  d.[Sell-to Customer No_], d.[Ship-to Code], t.[Search Description],r.[No_],d.[Posting Date]";
    
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupÃ¯Ā¿Ā½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();
             String custAbi = rs.getString("abiklient").trim();

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
                    double lprice = rs.getDouble("lastprice");
                    String ldoc = rs.getString("lastdoc");
                    String ldoc1 = rs.getString("lastdoc1");
                    Integer paevi = rs.getInt("paevi");
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID+ "_" + custAbi , prodID,amount , 0, 0,lastdate,lprice,open,0," ");
                 
                    if(ldoc1 != null && ldoc1.trim().length() > 0) {
                    	  cp.setAbiKood(ldoc1);
                    }
                  
                    if(ldoc != null && ldoc.trim().length() > 0) {
                    	 cp.setAbiKood(ldoc.trim());
                    }
                    	
                    cp.setRedline(paevi+"");
                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);

           }
         rs.close();
         stmt.close();
         con.close();



     } catch (Exception e){
       e.printStackTrace();
     }
     return vec;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "NaviAtria";
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
 	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [VOLDEMĀRS SIA$Cust_ Ledger Entry] " +
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


private void makePictureCat(){
	    
  	
  	String pictssou = parent.getProps().getProperty("pict.dir");
	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	    TreeMap phsize = new TreeMap();
	    
	    
	 try {
   	  File f = new File(pictspda +"list.txt");
        if (f.exists())f.delete();
        //Kataloogi sisude kustutamine
    	  File res2 = new File (pictspda);
           String[] ff2 = res2.list();
      
//       	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);
       StringBuffer buf = new StringBuffer();
       Iterator enume = prods.keySet().iterator();
  
       while (enume.hasNext()) {
  	    	String asi = (String)enume.next();
  	    	Product prod = (Product)prods.get(asi);
  	    	String toode = prod.getID();  
             if(toode.length() > 0){
              	
  	   	  res2 = new File (pictssou);
            ff2 = res2.list();
          
       	 for(int i = 0;ff2.length>i;i++){
           	if( ff2[i].startsWith(toode)){
             // System.out.println("pilt olemas " + ff2[i]);
          	File in = new File ( pictssou + ff2[i]); 
            	if(in.isFile()){
            	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
            	if(!inpda.exists()) {
            		Converter.shrinkAndCopyFile(in,inpda,400);
            	}
                	
                	File kkk =  new File ( pictspda + prod.getID()+ ".jpg"); 
                	phsize.put(prod.getID(), kkk.length());
          	    }
               }
             }
           }
  	   }
      
       
     Iterator itera =   phsize.keySet().iterator();
     while(itera.hasNext()) {
  	   String prid = (String)itera.next();
  		buf.append(prid +".jpg;"  + phsize.get(prid) + "\n");
             
     }
      
       
       try {
   		PrintWriter os = new PrintWriter(new FileWriter(pictspda + "list.txt", true));
   		os.write(buf.toString());
   		os.close();
   	  }catch (Exception ex) {
   		ex.printStackTrace();
   	  }
	 
	 } catch (Exception e) {
  	    getParent().showError(e);
  	}
    
}



@Override
public void PictureMaker() {
	 makePictureCat();
	  System.exit(0);
	
}


}
