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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;


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

public class VichiCZSQLDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public VichiCZSQLDBDriver(EksportPanel parent) {
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

			String cmd2 = "SELECT t.[No_] AS toode  " +
	   "FROM [Viciunai CZ$Item] t WHERE [Blocked]=0 AND len([Grupės kodas])>0 " ;

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
		        StringBuffer buf0 = new StringBuffer();
		        int a = 0;
		        int b = 0;
		        int c = 0;
		        
		        
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
		                   len = f.length() + "";
		                   if(f.isFile()){            	
		 		             	File inpda = new File ( pictspda + toode + ".jpg"); 
		 		             	Converter.shrinkAndCopyFile(f,inpda);
		 		               }
		                 }else{
		                	 String[] gg = new File(picts).list();
				         	  
				            
				             for(int i = 0 ; gg.length > i ; i++){ 
				            	 
				            	 if(gg[i].startsWith(toode)){
				            		 
				            		System.out.println("gg " + gg[0] + " " + gg.length);
				            		 File in = new File ( picts + gg[i]); 
				            		 len = in.length() + "";
				            		
				            		 if(in.isFile()){            	
				 		             	File inpda = new File ( pictspda + toode + ".jpg"); 
				 		             	Converter.shrinkAndCopyFile(in,inpda);
				 		               }
				            		 
				            	    }
				             	 }
				             }
		                 
		                 
		                 
		                 if(len.equals("0")){
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
		           	  File f0 = new File("list0.txt");
		           	   if (f0.exists())f0.delete();
		                   try {
		             		PrintWriter os1 = new PrintWriter(new FileWriter(
		             				  "list0.txt", true));
		             		os1.write(buf0.toString());
		             		os1.close();
		             	  }catch (Exception ex) {
		             		ex.printStackTrace();
		             	  }

		             File fff =  new File (pictspda +  "list.txt");
		       	  Converter.copyFile(f,fff);  	
		       	  File fff0 =  new File (pictspda +  "list0.txt");
		       	Converter.copyFile(f0,fff0);  	
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   
		}
		 
	
	 private Vector mlines = null;
	
   public Vector getPriceMatrixData(){
		  
	  	 
	  	 String sql =" SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
		            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from " +
		            "   [Viciunai CZ$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or " +
					"   year ([Ending Date]) = 1753) " +
					 "   and [Sales Type] = 0 " +
					 "	union " +
					"   SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
		            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val from " +
		            "   [Viciunai CZ$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1  " +
					"   or year ([Ending Date]) =1753 )  and [Sales Type] in (1) " +
				
					"	union " +
				"		 select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
		        "       0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val " +
				"	    from [Viciunai CZ$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1	" +
				"	 union " +
				"		 select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
		         "      0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday,[Currency code] as val " +
				"	    from [Viciunai CZ$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or " + 
			"		    year ([Ending Date]) = 1753) order by 1,2,7 desc ";

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
	             double disc =  rs.getDouble("disc");
	             String edate = rs.getString("paev");
	             String sdate = rs.getString("startday");

	             String val = rs.getString("val");
	             if(val==null || val.trim().length() == 0) {
	            	 val = "Kč";
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
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
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
    	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Viciunai CZ$Item Ledger Entry] l " +
          " INNER JOIN [Viciunai CZ$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('TAL') " +
          " AND t.[Blocked]=0  AND len(t.[Grupės kodas])>0 " +
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
     String cmd = "  SELECT  [Code] AS kood,[Description] AS nimi  FROM [Viciunai CZ$Product Group] where [Code] != ''order by [Code]";
     	 
       	 Connection con = getNewConnection2();
     //  	System.out.println("asike " +cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood");
   	        if(cleardots(id).equals("28")){
   	        	id = "5.1.";
   	        }
   	                String name = rs.getString("nimi").trim().substring(5).trim() ;
   	             if(id.trim().length()== 4  || id.trim().length() == 5) {
   	             tm.put(cleardots(id),  Converter.enCodeEst(name) + " " + id.substring(0,1) +" "+ cleardots(id) );
   	             }
   	        }
   	        
   	      rs.close();
   	      con.close();

         return tm;
   }

  
   public HashSet getActiveprods() throws SQLException {
	   
	   
	   HashSet   tm = new HashSet();
	     setTreeGroup(true);
	     String sql =" SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
		            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday from " +
		            "   [Viciunai CZ$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or " +
					"   year ([Ending Date]) = 1753) " +
					 "   and [Sales Type] = 0 " +
					 "	union " +
					"   SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
		            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday from " +
		            "   [Viciunai CZ$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1  " +
					" or  year ([Ending Date]) = 1753  )  and [Sales Type] in (1,2) " +
				
					"	union " +
				"		 select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
		        "       0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday " +
				"	    from [Viciunai CZ$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1	" +
				"	 union " +
				"		 select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
		         "      0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday " +
				"	    from [Viciunai CZ$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or " + 
			"		    year ([Ending Date]) = 1753) order by 1,2,7 desc ";
	     	 
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
    String cmd = " SELECT  [Code] AS kood,[Description] AS nimi  FROM [Viciunai CZ$Product Group] where [Code] != ''order by [Code]";
   // System.out.println("Asike 2 " +cmd);
    try {
    	 Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	            String groupID = rs.getString("kood");
	            if(cleardots(groupID).equals("20801")){
	            	groupID = "5.01.01.";
	   	        }
	          /*  if(cleardots(groupID).equals("20802")){
	            	groupID = "5.01.02.";
	   	        } if(cleardots(groupID).equals("20803")){
	            	groupID = "5.01.03.";
	   	        } if(cleardots(groupID).equals("20804")){
	            	groupID = "5.01.04.";
	   	        } if(cleardots(groupID).equals("20805")){
	            	groupID = "5.01.05.";
	   	        } if(cleardots(groupID).equals("20806")){
	            	groupID = "5.01.06.";
	   	        } if(cleardots(groupID).equals("20807")){
	            	groupID = "5.01.07.";
	   	        } if(cleardots(groupID).equals("20808")){
	            	groupID = "5.01.08.";
	   	        } if(cleardots(groupID).equals("20809")){
	            	groupID = "5.01.09.";
	   	        }*/
	        	if (cleardots(groupID).equals("281")) {
					groupID = "5.1.1.";
				}
				if (cleardots(groupID).equals("282")) {
					groupID = "5.1.2.";
				}
				if (cleardots(groupID).equals("283")) {
					groupID = "5.1.3.";
				}
				if (cleardots(groupID).equals("284")) {
					groupID = "5.1.4.";
				}
				if (cleardots(groupID).equals("285")) {
					groupID = "5.1.5.";
				}
				if (cleardots(groupID).equals("286")) {
					groupID = "5.1.6.";
				}
				if (cleardots(groupID).equals("287")) {
					groupID = "5.1.7.";
				}
				if (cleardots(groupID).equals("288")) {
					groupID = "5.1.8.";
				}
				if (cleardots(groupID).equals("289")) {
					groupID = "5.1.9.";
				}


	   	        
	                String name = rs.getString("nimi") ;
	                if(groupID.length()>5){
	                   tm.put(cleardots(groupID),Converter.enCodeEst(cleardots(groupID) + " " +name));
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

   public TreeMap getAgents(){

	   
	   //getPriceMatrixData();
	   
        String cmd = "SELECT [Code] AS kood,[Name] AS nimi,[Location Code] AS objekt FROM " +
		"[Viciunai CZ$Salesperson_Purchaser] ";
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
	          agent.setObject(rs.getString("objekt"));
	          agent.setName(name);


              agent.setLanguage("EST");
	          agent.setCountry("et");
	          agent.setPassWord("0000");
	          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
	          if(email.length() > 1){
	        	  agent.setEmail(email);
	        	  agent.setActive(true);
	          }
	         
	          
	          
	  		
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
	  " FROM [Viciunai CZ$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 " ;
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
   HashSet custs = new HashSet();
 try {

try{
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass); 
				
		}catch(Exception e){
			
		}

   
      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer ,a.[Posting Date] as kuup FROM [Viciunai CZ$Cust_ Ledger Entry] a inner join " +
                            " [Viciunai CZ$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
                            " where " +
							"  (a.[Document type] = 2 or a.[Document type]= 3) and a.[Due Date] < GETDATE()+7 AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
                            " group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], " +
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
try{
	 String doknr = rs1.getString("number").trim();    
     debt.setDoknr(doknr);
}catch(Exception e){
	
}
       
        debt.setDuedate(rs1.getDate("kuupaev"));
        debt.setSum(rs1.getDouble("summa"));
        int dueint = rs1.getInt("due");
        debt.setDate(rs1.getDate("kuup"));
        
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



   String sql =  " SELECT k.[Name]  AS nimi, k.[Name 2] AS nimi2 ,k.[No_] AS kood,adr.[Code] as kood2 , adr.[EDI ILN] as lisa,k.[Bill-to Customer No_] AS payer,\r\n" + 
   		"   		adr.[Address] + ' ' + adr.[Address 2] + ' ' + adr.[City] AS aadress , k.[Address] + ' ' + k.[Address 2] + ' ' + k.[City] AS aadress2,k.[Country Code] as country,k.[Salesperson Code] AS agent , \r\n" + 
   		"   		blocked AS blokk, \r\n" + 
   		"   			 [Payment Terms Code] AS payment,[Line Discount %] AS alekood, 0 AS yle,'' AS visit,'' AS type,  \r\n" + 
   		"   			   [Blocking after overdue days] AS extradays ,'' AS sort, '' AS debt , 0 AS minsum , \r\n" + 
   		"   			  adr.contact +' ' + adr.[Phone no_] + ' ' + k.[E-Mail] as kontakt ,' 'as kett  , '' as chain , \r\n" + 
   		"   			  [Customer Price Group] as hinnak, k.[Currency Code] as currency \r\n" + 
   		"   			  FROM [Viciunai CZ$Customer] k  \r\n" + 
   		"			  LEFT JOIN [Viciunai CZ$Ship-to Address] adr ON adr.[Customer No_]  = k.[No_] \r\n" + 
   		"			  LEFT JOIN [Viciunai CZ$Sales Line Discount] d ON d.[Sales Code] = k.[Customer Disc_ Group]  \r\n" + 
   		"   			  WHERE k.[Salesperson Code] != ''  AND blocked != 3 " ;

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
         
           String id = rs.getString("kood").trim();
           String kood = id;
           String id2 = rs.getString("kood2");
           if(id2 != null && id2.trim().length() > 0) {
        	   id = id + "_" + id2.trim();
           } else {
          	 id  = id + "_00" ; 
           }
           String lisa = rs.getString("lisa");
           if(lisa != null){
          	 name = (name + " " + lisa.trim()).trim();
           }
           //System.out.println(line);
           Customer customer = new Customer(id);
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.setName(Converter.enCodeEst(name));
           	payer = rs.getString("payer");
           	if(payer != null && payer.trim().length()>0){
             customer.setPayer(payer.trim());
           //  customer.setCustType(payer.trim());

             customer.addFixSortCode(payer.trim());
             }
             customer.addFixSortCode(id);
             String aadress = rs.getString("aadress");
             String aadress2 = rs.getString("aadress2");
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
               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),country));
               customer.setName(customer.getName()+ " " + Converter.enCodeEst(aadress));
             }else  if( aadress2 != null && aadress2.trim().length()>0){
            	 
            	 aadress2 = aadress2.replaceAll("  ", " ");
            	 
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
               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress2),country));
               customer.setName(customer.getName()+ " " + Converter.enCodeEst(aadress2)); 
               	 
             }
         	String ag = rs.getString("agent");
             if(ag != null){
               // customer.setMainAgent(ag.trim());
                customer.addAgent(new Agent(ag.trim()));
                
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
             
       
             if(rs.getString("blokk").equals("1")){
               customer.setSalesBlock(true);
             }

            String paym = rs.getString("payment");

           	String num = rs.getString("alekood");
           	if(num != null && num.trim().length()>0){
           	double dd = Double.parseDouble(num.trim());
           	if(dd > 0)
             customer.setCity("Discount : " + getNF().format(Double.parseDouble(num.trim())) + " %");
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

           if (!customer.isNodebt()){
         
  
                String sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
                         " (SELECT     SUM(de.[Amount (LCY)]) FROM [Viciunai CZ$Cust_ Ledger Entry] c INNER JOIN  " +
                         "  [Viciunai CZ$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
                         "   WHERE   c.[Customer No_] = k.No_ and[Due Date] <getdate()+1 ) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
                         "   FROM  [Viciunai CZ$Cust_ Ledger Entry] c INNER JOIN " +
                         "    [Viciunai CZ$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
                         "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [OPEN] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
						 " 	 and [Due Date] <getdate()+1    and c.[Posting Date] > getdate()- 100) " +
					     " 	 END AS yle FROM [Viciunai CZ$Customer] k "+
					      "	WHERE  (No_ = '" + id + "') AND (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

		    try {
		      Connection con3 = getNewConnection2();
		      Statement stmt3 = con3.createStatement();
		      ResultSet rs3 = stmt3.executeQuery(sql3);
		      while(rs3.next()){
		      
		         double sum = rs3.getDouble("yle");
		         if(sum !=0 && sum != 1 && sum != -1){
		         	customer.setDebt(sum);
		         	//System.out.println("yle " + id+ " " +  sum);
		         }
		         
		      }

rs3.close();
stmt3.close();
con3.close();
		    } catch (Exception e){
		         e.printStackTrace();
		       }

            	

        
           	//customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));
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
         
           
           
            String hinnak = rs.getString("hinnak");
           if(hinnak != null && hinnak.trim().length()>0){
               customer.setGroup(kood + "|" + hinnak.trim().replace('Í', 'I'));  
               customer.setCustType(hinnak.trim().replace('Í', 'I'));
               customer.addFixSortCode(hinnak.trim().replace('Í', 'I'));
           }
        //   " [Shipping Agent Code] AS rout  ,[Loading Code] as rout2 ,  [Customer Price Group] as hinnak 
           
           
           String curre  = rs.getString("currency");
           if(curre != null && curre.trim().length() > 1 ) {
        	   customer.currency = curre.trim();
            } else {
        	   customer.currency = Converter.enCodeEst("CZK");
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
   
   public TreeMap getProducts(Agent agent) {
		HashSet inPricelist = null;
	   try {
		   inPricelist = getActiveprods();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	   
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      prodHash = new HashMap();
     try {
   	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Viciunai CZ$Item Ledger Entry] l " +
         " INNER JOIN [Viciunai CZ$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('DASICE') " +
         " AND t.[Blocked]=0  AND len(t.[Product Group Code])>0 " +
         " GROUP BY l.[Location Code],l.[Item No_] HAVING sum(l.[Quantity]) > 0 ORDER BY l.[Item No_] ";

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood").trim();
  	              	seis = rs.getDouble("kogus");
  	              	ladutr.put(toode, seis + "");
  	        }

  	      rs.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     
     TreeMap prices = new TreeMap();
     
     
     try {
      	  String cmd = "SELECT [Item No_] AS kood,max( [Unit Price]) AS price, [Sales Type] as type  from \r\n" + 
      	  		"      	  		               [Viciunai CZ$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or  \r\n" + 
      	  		"      	  						  year ([Ending Date]) = 1753)  and [Sales Type] in (2,0) \r\n" + 
      	  		"      	  					  group by [Item No_], [Sales Type]order by [Item No_]";

         	 Connection con = getNewConnection2();
     	        Statement stmt = con.createStatement();
     	        ResultSet rs = stmt.executeQuery(cmd);
     	        while(rs.next()){
     	             String toode = rs.getString("kood");
     	             double seis = rs.getDouble("price");
     	              int type = rs.getInt("type");
     	              if(type == 2) {
     	            	 prices.put(toode, seis + "");  
     	              } else if (prices.get(toode) == null) {
     	            	 prices.put(toode, seis + "");
     	              }
     	              	
     	        }

     	      rs.close();
     	      con.close();

     	    System.out.println(cmd);
      } catch (Exception e) {
          e.printStackTrace();
      }
        Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     try {
     	String cmd = "   	SELECT [No_] AS toode,(t.[Description]) AS nimi, [Unit Price] AS myygihind, [Product Group Code] AS grupp,[Subgroup Code] AS grupp2" + 
     			"			,[Base Unit of Measure] AS unit,  ref.[Cross-Reference No_] AS ean,'' AS status ,'' AS maxdiscount," + 
     			"			  '' AS type, [Qty_ per Unit of Measure] AS kastis,  t.[Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, t.[Description] AS vatdesc " + 
     			"			  , [Description 2] as info FROM [Viciunai CZ$Item] t LEFT JOIN [Viciunai CZ$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KRT' )" + 
     			"			  LEFT JOIN [Viciunai CZ$Item Cross Reference] ref ON (ref.[Item No_]=[No_] and ref.[Unit of Measure]='JEDN' and len(ref.[Cross-Reference No_]) > 12 and ref.[Cross-Reference Type] = 1)" + 
     			"			  WHERE ([Blocked]=0  AND len([Product Group Code])>0 and  len(t.[Description]) != 0) " + 
     			"			  or [Subgroup Code] like '%2.08.01%' ORDER BY [No_]  ";
     	
      System.out.println(cmd);
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            double weight = 0 ;
            Product prod = new Product(id);
         	String name = rs.getString("nimi");
         	name =   name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
         	if(prices.get(id) != null) {
         		double price = Double.parseDouble(prices.get(id)+"");
                        prod.setPrice(price);
         	}
             
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
                String grupp = cleardots(rs.getString("grupp"));
                if(grupp.equals("28")){
                	grupp = "51";
                }
                prod.setGroup(grupp);

        		String grupp2 = cleardots(rs.getString("grupp2"));
    			
    			if (grupp2.equals("281")) {
    				grupp2 = "511";
    			}
    			if (grupp2.equals("282")) {
    				grupp2 = "512";
    			}
    			if (grupp2.equals("283")) {
    				grupp2 = "513";
    			}
    			if (grupp2.equals("284")) {
    				grupp2 = "514";
    			}
    			if (grupp2.equals("285")) {
    				grupp2 = "515";
    			}
    			if (grupp2.equals("286")) {
    				grupp2 = "516";
    			}
    			if (grupp2.equals("287")) {
    				grupp2 = "517";
    			}
    			if (grupp2.equals("288")) {
    				grupp2 = "518";
    			}
    			if (grupp2.equals("289")) {
    				grupp2 = "519";
    			}
                prod.setGroupSecond(grupp2);

                prod.setUnit(rs.getString("unit"));
              
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
                   prod.setInCases(cases);
                	}

                	
             	weight = rs.getDouble("kaal");
                           if(weight > 0){
                            	// prod.setInUnit(weight);
                            	 prod.setMeasure(weight);
                           }      	 
                                                      
                           
                     	prod.setVat("21");
                    
                   
             /*      if(prod.getEAN().trim().length()==0){
                  	 dateOut.put(id.trim(),id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                  	 buf.append(id.trim() + ";"+ id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
                  }else{
                  	 dateOut.put(prod.getEAN(),id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                  	 buf.append(prod.getEAN() + ";"+ id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
                  	 if(prod.getEanCase().trim().length() >0){
                  		 dateOut.put(prod.getEanCase(),id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                      	 buf.append(prod.getEanCase() + ";"+ id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");	 
                  	 }
                  }*/
                   
               	 String info = rs.getString("info");
                 if(info != null && info.trim().length()>0){   
                	 prod.setPartii(info.trim());
                 }
                   
          //leedu versioonis prod.setName(weight.substring(0,weight.indexOf('.')+3) + " " + name);
          prod.setName(Converter.enCodeEst(name) + " " + prod.getID());
          if(inPricelist.contains(prod.getID())) {
        	    tm.put(name + prod.getID(), prod);
                prodHash.put(prod.getID(), prod);  
          }
       
        }


        
        
        
        
        /*try {
			saveObject(dateOut,"tootedRev.has");
			  if(buf.length() > 2){
	                File f = new File(getDir() + "tootedRev.txt");
	                if (f.exists())f.delete();
	                  try {
	            		PrintWriter os = new PrintWriter(new FileWriter(
	            				getDir() + "tootedRev.txt", true));
	            		os.write(buf.toString());
	            		os.close();
	            	  }catch (IOException ex) {
	            		ex.printStackTrace();
	            	  }
	             }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      */  
        
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
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient, d.[Ship-to Code] as kl2 ,r.[No_] AS toode,sum(r.[Quantity]) AS kogus, d.[Posting Date] AS lastdate, 0 as lastamount \r\n" + 
    		"    		FROM [Viciunai CZ$Sales Shipment Line] r \r\n" + 
    		"    INNER JOIN [Viciunai CZ$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Viciunai CZ$Item] t ON t.[No_]=r.[No_]  WHERE \r\n" + 
    		"      d.[Posting Date]> GETDATE()-70 \r\n" + 
    		"  group by d.[Sell-to Customer No_],d.[Ship-to Code],r.[No_],d.[Posting Date],t.[Search Description] \r\n" + 
    		"	  ORDER BY  d.[Sell-to Customer No_],d.[Ship-to Code], t.[Search Description],r.[No_],d.[Posting Date]";
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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [Viciunai CZ$Sales Invoice Line] r " +
		  " INNER JOIN [Viciunai CZ$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
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
     return "ViciCZSQL";
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
   private Vector debtList = new Vector();
   private HashMap sortimendid = null;
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url2 = "jdbc:odbc:navicz";
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
  	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Viciunai CZ$Cust_ Ledger Entry] " +
  	 			"where Month(getdate())=Month([Posting Date]) and " +
             "Year(getdate())=Year([Posting Date]) " +
              "  group by [Salesperson Code]"; // and [Global Dimension 1 Code]='400'
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
