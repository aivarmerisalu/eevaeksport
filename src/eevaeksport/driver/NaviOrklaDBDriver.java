package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Dealer;
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

public class NaviOrklaDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public NaviOrklaDBDriver(EksportPanel parent) {
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
	    rcompany = getParent().getProps().getProperty("rest.company");
	   
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
        "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi from" +
        "   [Orkla Foods Latvija SIA$Sales Price] where (getdate()+2 between [Starting Date] and [Ending Date]+1  or  " +
                   "  (year ([Ending Date]) = 1753 and getdate()+1 > [Starting Date]) ) and [Sales Type] = 0   " +
                      "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
         " [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi from " +
         " [Orkla Foods Latvija SIA$Sales Price] where [Starting Date] < getdate()+2  and ([Ending Date] > getdate()+1 or " +
                  "  year ([Ending Date]) = 1753) and [Sales Type] = 1 " +   
                      "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
        "   0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi " +
                   "  from [Orkla Foods Latvija SIA$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0   " +     
               "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
          " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi " +
                   "  from [Orkla Foods Latvija SIA$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()+2  and ([Ending Date] > getdate()+1 or  " +
                  "   year ([Ending Date]) = 1753) " +
                  " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                 " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val ,  datediff(dd,[Starting Date],[Ending Date]) as paevi " +
                 "    from [Orkla Foods Latvija SIA$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1        " +
                " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
               " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val  ,  datediff(dd,[Starting Date],[Ending Date]) as paevi" +
                "     from [Orkla Foods Latvija SIA$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()+2  and ([Ending Date] > getdate()+1 or " + 
                 "    year ([Ending Date]) = 1753) ) as ta order by custid , clgroup, prodid ,paev desc " ; 
			
	 

  	 

       Vector result = new Vector();
         try {
          
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {

             String custID = Converter.enCodeEst(rs.getString("custid"));
             String clGroup = Converter.cleanLitLatEst(rs.getString("clgroup"));
             String prodID = rs.getString("prodid");
             String prodGroup = Converter.cleanLitLatEst(rs.getString("prodgroup"));
            
             double price = rs.getDouble("price");
             double disc = rs.getDouble("disc");
             String date = rs.getString("paev");
             String sdate = rs.getString("startday");
          
             int paevi = rs.getInt("paevi");
             
             MatrixLine mline = new MatrixLine();

             if (custID != null && custID.trim().length() > 0) {
               mline.setCustID(custID.trim());
             }
          
             
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
             }

             if(paevi > 1 && paevi < 75){
            	 if(sdate != null  && !sdate.startsWith("1753")){
                	 mline.setStartDate(sdate.substring(0,10));
                 }
                 if(date != null && !date.startsWith("1753")){
                	 mline.setEndDate(date.substring(0,10));
                 }
             }
             
            
             //pika hinna määramine
             
             if (prodID != null && prodID.trim().length() > 0) {
               mline.setProdID(prodID.trim());
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
             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
                 result.add(mline);    
             }
             
           }
           rs.close();
           stmt.close();
           con.close();
         }catch(Exception e){
        	 e.printStackTrace();
         }
	System.out.println("matrix result " + result.size());
    return result;
      }


   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }

   public Vector getLastThreeData() {

		Vector result = new Vector();
		// Siin loeme igale kliendile kylge viimase kolem korra ostud
		try {


			TreeMap data = new TreeMap();
			
			String cmd =  " SELECT  d.[Sell-to Customer No_] AS klient ,d.[LV Invoice No_] as arve,d.[Posting Date] as daat"
			    		+ " FROM  [Orkla Foods Latvija SIA$Sales Shipment Header] d  INNER JOIN [Orkla Foods Latvija SIA$Customer] k  "
		      + " ON  d.[Sell-to Customer No_] =k.[No_]  WHERE " +
			     " d.[Posting Date]> GETDATE()-60 and d.[LV Invoice No_] != '' and  k.[Salesperson Code] != '' ORDER BY  d.[Sell-to Customer No_],d.[Posting Date]desc " ;
			    
				Connection con2 = getNewConnection2();
				Statement st2 = con2.createStatement();
				ResultSet rs2 = st2.executeQuery(cmd);
				int custcou = 0;
				String oldcust = null;
				while (rs2.next()) {
					String klient = rs2.getString("klient").trim();
					custcou ++;
					if(oldcust == null){
						oldcust = klient;
					}else if(!oldcust.equals(klient)){
						custcou = 0;
					}
					String arve = rs2.getString("arve").trim();
					
					Vector vv = (Vector)data.get(klient);
					if(vv == null){
						vv = new Vector();
					}
					if(custcou < 6 && arve.length() > 0 ){
						vv.add(arve);
						data.put(klient, vv);
					}
					
					oldcust = klient;
					
				}
				rs2.close();
				st2.close();
				con2.close();

			
			Iterator itera = data.keySet().iterator();
			while(itera.hasNext()){
				String klkood = (String)itera.next();
			  Vector vv = (Vector)data.get(klkood);
			  String arved = null;
			  for(int i = 0; i<vv.size();i++){
				  if(arved== null){
					  arved = vv.elementAt(i)+"";
				  }else{
					  arved = arved + " , " + vv.elementAt(i); 
				  }
				 
			  }
			  
if(vv.size() > 2){

				String sq3 = " SELECT r.[No_] AS toode,r.[Quantity] AS kogus ,d.[Posting Date] as kuup , d.[LV Invoice No_] as doknr ,r.[VAT Base Amount] as hind "
						+ " FROM [Orkla Foods Latvija SIA$Sales Shipment Line] r " +
					     " INNER JOIN [Orkla Foods Latvija SIA$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN " +
					     " [Orkla Foods Latvija SIA$Item] t ON t.[No_]=r.[No_]  WHERE  d.[LV Invoice No_] in ( " + arved + 
					     ") and d.[Posting Date] > getdate()-60 ORDER BY  d.[Posting Date], t.[Search Description],r.[No_]";
							
							
								System.out.println(sq3);
								Connection con3 = getNewConnection2();
								Statement st3 = con3.createStatement();
								ResultSet rs3 = st3.executeQuery(sq3);
							
							while (rs3.next()) {
								MatrixLine ml = new MatrixLine();
								ml.setCustID(klkood);
								ml.setProdID(rs3.getString("toode").trim());
								ml.setPrice(rs3.getDouble("kogus"));
								ml.setStartDate(rs3.getString("kuup").trim().substring(0,10) + "");
								ml.setLineinfo(rs3.getString("doknr").trim());
								ml.setDiscount(rs3.getDouble("hind"));
								result.add(ml);
								}
							
							rs3.close();
							st3.close();
							con3.close();
							
						}							
			  
			}
			
				
						
				
				
				
				
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("L5 "  + result.size());
		
		return result;
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


   public DealerProduct getDealerProductsList(){

	       DealerProduct dealerProds = new DealerProduct();

	       try {


	    		  String  cmd = "  SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, case when l.[Location Code] "
	    		  		+ "like '%BLS%' then 'BLS-STAND' else l.[Location Code] end AS ladu FROM [Orkla Foods Latvija SIA$Item Ledger Entry] l " +
          " INNER JOIN [Orkla Foods Latvija SIA$Item] t ON [No_]=l.[Item No_] WHERE    t.[Blocked]=0 " +
          " GROUP BY (case when l.[Location Code] like '%BLS%' then 'BLS-STAND' else l.[Location Code] end),"
          + "l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_] ";
	    		  
	    		  if(usedProds == null){
	    			  this.getProducts(null);
	    		  }
	            if (isDebug()) System.out.println(cmd);
	            Connection con = getNewConnection2();
	               Statement stm = con.createStatement();
	               ResultSet rst = stm.executeQuery(cmd);
	                 while (rst.next()) {
	                   String toode = rst.getString("kood");
	                   if (toode != null){
	                   toode = toode.trim();
	                   if(usedProds.contains(toode)){
	                	   double seis = rst.getDouble("kogus");
		                   String ladu  =  rst.getString("ladu").trim();
		                    dealerProds.add(ladu, toode,((int)seis)+""," "," " );
		        	     }
	                
	                    }
	                   }
	               rst.close();
	               stm.close();
	               con.close();

	       } catch (Exception e) {
	           e.printStackTrace();
	       }

	    	   
	    	
	      
	       
	         System.out.println( " dealerProds suurus " + dealerProds.toString());
	        return dealerProds;
	      
	      }




       public String getSaveStockInfo(){

 	  	  
    		  String  cmd = "  SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, case when l.[Location Code] "
    		  		+ " like '%BLS%' then 'BLS-STAND' else l.[Location Code] end AS ladu FROM [Orkla Foods Latvija SIA$Item Ledger Entry] l " + 
         "  INNER JOIN [Orkla Foods Latvija SIA$Item] t ON [No_]=l.[Item No_] WHERE    t.[Blocked]=0 " +
          " GROUP BY (case when l.[Location Code] like '%BLS%' then 'BLS-STAND' else l.[Location Code] end),l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]";

    		  
    		  if(usedProds == null){
    			  this.getProducts(null);
    		  }
    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
    		           Statement st = con.createStatement();
    		           ResultSet rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood").trim();
    		             double seis = rs.getDouble("kogus");
    		             String ladu = rs.getString("ladu").trim();
    		             
    		             if(seis > 0 && usedProds.contains(kood)){
    		                 products.append(kood +  ";");
    		                 products.append(" ;");
    		                 products.append(getParent().getNF().format(seis) + ";" + ladu  +  "\n"); // + dealer +  "\n");
    		             }
    		             }
    		             rs.close();
    		             st.close();
    		             con.close();
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
           
           
           public TreeMap getDealers() {
        	   TreeMap   dealers = new TreeMap();
      	     setTreeGroup(true);
      	     String cmd = "SELECT k.[Location Code] as kood,max( l.Name) as nimi FROM [Orkla Foods Latvija SIA$Customer] k  INNER JOIN [Orkla Foods Latvija SIA$Location] l " + 
					" on l.Code = k.[Location Code]   WHERE k.[Salesperson Code] != ''  AND Blocked != 3 and k.[Location Code] != '' " +
			 " GROUP BY k.[Location Code] ";
      	     
      	     
      	     	  try {
      	       	 Connection con = getNewConnection2();
      	         	System.out.println(cmd);
      	   	        Statement stmt = con.createStatement();
      	   	        ResultSet rs = stmt.executeQuery(cmd);
      	   	        while(rs.next()){
      	   	            String id = Converter.cleanLitLatEst(rs.getString("kood").trim());     
      	                String name = Converter.enCodeEst(rs.getString("nimi")) ;
      	              Dealer dealer = new Dealer(id);
                       dealer.setName(name); 
                       dealers.put(id, dealer);
      	   	        }
      	   	      rs.close();
      	   	      con.close();

      	        } catch (Exception e){
      	          e.printStackTrace();
      	        }

      	         return dealers;
        	    }
           
           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

           
           public TreeMap getProdGroups() throws SQLException {
        	     TreeMap   tm = new TreeMap();
        	     setTreeGroup(true);
        	     String cmd = " SELECT  [Code] AS kood ,[Description] AS nimi  FROM [Orkla Foods Latvija SIA$Product Group] " +
        	     		"  where [Item Category Code] !=''  order by [Code] ";
        	     cmd = " SELECT  [Code] AS kood ,[Description] AS nimi  FROM [Orkla Foods Latvija SIA$Item Discount Group] " +
         	     		"  order by [Code] ";
        	     	  try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = Converter.cleanLitLatEst(rs.getString("kood").trim());     
        	                String name = rs.getString("nimi") ;
        	                if(name == null || name.trim().length() == 0){
        	                	name = id.trim();
        	                }
        	   	             tm.put(id, name.trim());  	             
        	   	        }
        	   	      rs.close();
        	   	      con.close();

        	        } catch (Exception e){
        	          e.printStackTrace();
        	        }
        	     tm.put("CITI", "No Group");  
        	         return tm;
        	   }

           
           

         
           public TreeMap getProdGroupsSecond() throws SQLException {
               return teised;
             }


           
   private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   StringBuffer sb2 = new StringBuffer();
	 //  getLastThreeData();
	    String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM " + 
		"  [Orkla Foods Latvija SIA$Salesperson_Purchaser] where ltrim(Name) not like '-%' and rtrim(Name) not like '   % '";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	    	

	 	    	 FileReader fr = new FileReader(this.getExcelDir() + "vacation.txt");
	 	         BufferedReader reader = new BufferedReader(fr);
	 	       String line = null;
	 	      HashMap asendajad = new HashMap();
	 	      
	 	        while((line = reader.readLine()) != null ){
	 	           
	 	        	StringTokenizer st = new StringTokenizer(line, ";");
	 	//  System.out.println(line);
	 	          if (st.hasMoreTokens()) {
	 	            String ag1 =	st.nextToken();
	 	            String man = st.nextToken().trim();
	 	            Vector vv = new Vector();
	 	            if(asendajad.get(ag1) != null){
	 	           	 vv = (Vector)asendajad.get(ag1);
	 	           	 vv.add(man);
	 	           	 asendajad.put(ag1, vv);
	 	            }else{
	 	           	 vv.add(man);
	 	           	 asendajad.put(ag1, vv); 
	 	             }
	 	            }
	 	          }
	 		 
	 	    	
	    	
	    	
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt"));
	          agent.setName(name);

	          agent.setLanguage("LAT");
	          agent.setCountry("lv");
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
              //   System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
			    if(asendajad.get(agent.getID()) != null){
                	Vector vv = (Vector)asendajad.get(agent.getID());
                	Enumeration aa = vv.elements();
                	while(aa.hasMoreElements()){
                		agent.addDubbler((String)aa.nextElement());
                	}
                }
			    sb2.append(agent.getID() + ";" + name + "\n");  
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
            rs.close();
            stmt.close();
	        con.close();            
	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }
	    
	    if(sb2.length() > 2){
	    	try{
	    		// Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    	}
	      }

	    if(rcompany != null){
	    	Converter.sendAgentJson(agents, rcompany, false);
	    }
	  // this.setBulkSales(agents);
     return agents;
   }

       public String getDistribution() {
         return null;
       }
   
  private HashSet sorts = new HashSet();
     

  
  
  private TreeMap getAmountCampProdstring(HashMap prodHash ){
	  TreeMap asi = new TreeMap();
	  StringBuffer buf = new StringBuffer();
	  try{
		
	  FileReader fr = new FileReader(this.getExcelDir() + "amountdiscProds.txt");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
    
      while((line = reader.readLine()) != null ){
    	
      	StringTokenizer st = new StringTokenizer(line, ";");
      	if (st.hasMoreTokens()) {
      	 
         	String  toodex = st.nextToken();
         	try{
         		double  perc = Double.parseDouble(st.nextToken());
             	double  kogus = Double.parseDouble(st.nextToken());
             	
             	
             	if(asi.get(toodex) != null){
             	  double pp = Double.parseDouble(asi.get(toodex)+ "");
             		if(pp < perc){
             			asi.put(toodex, perc);	
             		}
             	}else{
             		asi.put(toodex, perc);
             	}
             	
             	if(prodHash != null){
             		double  hind = ((Product)prodHash.get(toodex)).getPrice();
                  	
                  	double hl = (hind) *(100-perc)/100;
                 	// siia teha number format 
                 	
                 	     buf.append(toodex+ "; ; ;" + NF.format(hl).replace(',', '.') + ";AMDISC;" + kogus + "; ; ;" + perc   );
                         buf.append("\n");	
             	}
             	
         	}catch(Exception e){
         		
         	}
        }

      }
      reader.close();
      fr.close();
      
	  }catch(Exception r){
		r.printStackTrace();  
	  }
	  
	  if(buf.length() > 5){
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

	  return asi;
  }

  private HashSet getAmountCampNoCusts(){
	  HashSet amcamp = new HashSet();
	  try{
		
	  FileReader fr = new FileReader(this.getExcelDir() + "amountdiscNoCust.txt");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
    
      while((line = reader.readLine()) != null ){
    	
      	StringTokenizer st = new StringTokenizer(line, ";");

      	if (st.hasMoreTokens()) {
        	amcamp.add(st.nextToken());
        }
        }
      reader.close();
      fr.close();
      
	  }catch(Exception r){
		r.printStackTrace();  
	  }
	  return amcamp;
  }
	
  
       public TreeMap getCustomers(Agent agent){

     		HashSet amcamp = getAmountCampNoCusts();

	    	  
	    try {

    	      String cmd = "   select a.[Sell-to Customer No_] AS klient, c.[Registration No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa, " +
    	                        "     DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Posting Date] as kuup FROM [Orkla Foods Latvija SIA$Cust_ Ledger Entry] a inner join " +
    	                        "     [Orkla Foods Latvija SIA$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
								"	  inner join  [Orkla Foods Latvija SIA$Customer] c on c.[No_]=a.[Sell-to Customer No_] " +
    	                          "   where " +
    							"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-500 AND a.[Open]= 1 and getdate() +8 > a.[Due Date] " +
    	                         "    and  c.[Registration No_] not in ('40003520643','40003053029') group by a.[Sell-to Customer No_] ,a.[External Document No_], " +
    	                          "   a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,c.[Registration No_],a.[Posting Date] having sum(b.[Amount])<> 0 order by  a.[Due Date]" ;
									
    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection2();
    	       Statement stmt1 = conv.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	      
    	        debt.setCustID(rs1.getString("klient"));
    	        debt.setPayerID(rs1.getString("maksja").trim());
    	        debt.setDoknr(rs1.getString("number"));
    	        debt.setDuedate(rs1.getDate("kuupaev"));
    	        debt.setSum(rs1.getDouble("summa"));
    	        debt.setDue(rs1.getInt("due") + "");
    	        debt.setDate(rs1.getDate("kuup"));
      	        
    	        this.debtList.add(debt);
    	        
    	            }

    	       rs1.close();
    	       stmt1.close();
    	       conv.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	/// KLIENDI ANDMETE LEIDMINE

	    
        
  	  
        String sql3 =  "SELECT k.[Registration No_]  as kood ,   sum(de.[Amount (LCY)]) as koik ,sum(case when  [Due Date] <getdate()-1  then de.[Amount (LCY)] else 0 end) as yle " +
        		"FROM [Orkla Foods Latvija SIA$Cust_ Ledger Entry] c INNER JOIN  "+
                 " [Orkla Foods Latvija SIA$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
				 " inner join [Orkla Foods Latvija SIA$Customer] k on k.[No_] = c.[Customer No_] "+
                  "  WHERE    [Open] = 1 GROUP BY  k.[Registration No_]" ;

        TreeMap debtsover  = new TreeMap();
        TreeMap debtsall  = new TreeMap();
    try {
      Connection con3 = getNewConnection2();
      Statement stmt3 = con3.createStatement();
      ResultSet rs3 = stmt3.executeQuery(sql3);
      while(rs3.next()){
    	  String kl = rs3.getString("kood");
    	  double sum = rs3.getDouble("koik");
         double yle = rs3.getDouble("yle");
         if(sum !=0){
        	 debtsall.put(kl, sum);
         }
         
         if(yle !=0){
        	 debtsover.put(kl, yle);
         }
      }

rs3.close();
stmt3.close();
con3.close();
    } catch (Exception e){
         e.printStackTrace();
       }



	    
	    
	    
	    
    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();


    	   String sql =  "   SELECT k.[Name] AS nimi ,k.[No_] AS kood,k.[Registration No_] AS payer"
    	   		+ ",k.[Address]+ ' '+ k.[Address 2] + ' ' + k.[City] AS aadress " +  
    	"   	, a.[Address]+ ' '+ a.[Address 2] as addr2 ,  k.Blocked AS blokk ,k.[Salesperson Code] AS agent ,   " +
    	"  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type,  " +
    	"	   0 AS extradays ,[Customer Price Group]  AS sort,'' AS debt , 0 AS minsum ,k.Contact +' ' + k.[Phone No_] as kontakt , 0 as sendall " +
    	" ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2  ,k.[Location Code] as dealer "
    	+ ",k.[Payment Method Code] as paymcode,k.[Delivery day] as delivery ,k.[Veikala kods] as chain ,"
    	+ " [Delivery Day Frozen] as del3 , k.[Delivery speed] as del2  FROM [Orkla Foods Latvija SIA$Customer] k  "
      + " LEFT JOIN [Orkla Foods Latvija SIA$Ship-to Address] a on  a.[Customer No_]=k.[No_]   " +
	  	"	  WHERE k.[Salesperson Code] != '' or  k.[No_] = '99978'   " ;

    	   int minordsum = 0;
    	   
    	   if( parent.getProps().getProperty("minordsum") != null){
    		   try{
    			   minordsum = Integer.parseInt(parent.getProps().getProperty("minordsum"));
    		   }catch(Exception e){
    		   }
    	   }
    	   
    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	        int namelen = name.length();
    	         name = name.replaceAll("\"", " ");
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           name = name + " " + id;
    	           Customer customer = new Customer(id);
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             customer.setRegnr(payer.trim());
    	             }
                   if(minordsum > 0){
                	   customer.setMinsum(minordsum);
                   }
    	           	
                   
    	             String aadress = rs.getString("aadress");
    	             String aadress2 = rs.getString("addr2");
    	             if( aadress2 != null && aadress2.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress2.trim()),"Latvia"));
    	               name = name + " " + Converter.enCodeEst(aadress2.trim());
    	              customer.setName(Converter.enCodeEst(name));
    	             }else if( aadress != null && aadress.trim().length()>0){
    	    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Latvia"));
    	    	               name = name + " " + Converter.enCodeEst(aadress);
    	    	              customer.setName(Converter.enCodeEst(name));
    	    	             }
    	          
    	             String sbb = rs.getString("blokk");
    	             if(sbb.equals("1")){
    	               customer.setSalesBlock(true);
    	             }
    	             if(sbb.equals("2")){
      	               customer.setSalesBlockT(true);
      	             }
    	             	 
    	             
    	         	String ag = rs.getString("agent");
    	         	if(!sbb.equals("3")){
    	         	
        	             
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
    	             
    	             
    	             if (customer.getID().equals("99978")){// uus klient lisame kõik 
    	 			          Enumeration enums = getParent().getAgentsHash().elements();
    	 			          while (enums.hasMoreElements()) {
    	 			            Agent a = (Agent) enums.nextElement();
    	 			            customer.addAgent(a);
    	 			          }
    	 			}
                  
    	         	
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

    	      
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("DIENAS"," ");
    	               paym = paym.replaceAll("SR","-1").trim();
    	               try{
    	            		customer.setPaymTerm(Integer.parseInt(paym.trim())+ "");
    	               }catch(Exception e){
    	            	   //e.printStackTrace();
    	               }
    	        
    	             }

    	           if(debtsall.get(customer.getPayer()) != null){
    	        	   customer.setUsedCredit(Double.parseDouble(debtsall.get(customer.getPayer())+""));
    	           }
    	           
    	           if(debtsover.get(customer.getPayer()) != null){
    	        	   customer.setDebt(Double.parseDouble(debtsover.get(customer.getPayer())+""));
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

                   if(!amcamp.contains(customer.getID())){
                	   customer.setCustType("AMDISC");
                   } 	          
                   customer.setCustType("AMDISC");

    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	       
    	        	if(customer.getGroup().trim().length() >0){
    	        	//customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	}else{
    	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	}
    	           }
    	           
    	           String dealer = rs.getString("dealer");
    	           if(dealer != null && dealer.trim().length()>0){
    	        customer.setDealer(new Dealer (Converter.cleanLitLatEst(dealer.trim())));   
    	       //    customer.setWare(dealer);
    	           }
    	           String paymcode = rs.getString("paymcode");
    	           if(paymcode != null && paymcode.trim().length()>0){
    	           customer.setCity("Maksājuma metode:" + paymcode.trim());   
    	           }
    	      
    	           String delivery = rs.getString("delivery");
    	           if(delivery != null && delivery.trim().length()>0){
    	        	  delivery = delivery.replaceAll(";", ",").replaceAll("\\r\\n|\\r|\\n", " ").trim();
    	        	     if(customer.getCity()!= null && customer.getCity().trim().length() > 0 ){
        	        		   customer.setCity(customer.getCity() + "|DD:" + delivery); 
        	       	             }else{
        	        			   customer.setCity("DD:" + delivery );
        	       	         }
        	           }
    	        	

    	           String chain = rs.getString("chain");
    	           if(chain != null && chain.trim().length()>0){
    	           customer.setChain(chain.trim());   
    	           }
    	           
    	           String delivery3 = rs.getString("del3");
    	           if(delivery3 != null && delivery3.trim().length()>0){
    	        	  delivery3 = delivery3.trim();
    	        		   if(customer.getCity()!= null && customer.getCity().trim().length() > 0 ){
        	        		   customer.setCity(customer.getCity() + " FDD:" + delivery3); 
        	       	             }else{
        	        			   customer.setCity("FDD:" + delivery3  );
        	       	         }
        	           }
    	           
    	           
    	           String delivery2 = rs.getString("del2");
    	           if(delivery2 != null && delivery2.trim().length()>0){
    	        	  delivery2 = delivery2.trim();
    	        	  if(delivery2.equals("1")){
    	        		  delivery2 = "24h"; 
    	        	  }else{
    	        		  delivery2 = "48h";
    	        	  }
    	        		   if(customer.getCity()!= null && customer.getCity().trim().length() > 0 ){
        	        		   customer.setCity(customer.getCity() + " DS:" + delivery2); 
        	       	             }else{
        	        			   customer.setCity("DS:" + delivery2  );
        	       	         }
        	           }
    	           
    	           
    	            customer.setName(customer.getName() + "{" + namelen +"}");
    	           
    	           
    	            tm.put(customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();



    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   
    	   
    	   if(rcompany != null){
  			 Converter.sendCustomerJson(tm, rcompany, false,user,pass,true);
  		    }
  		
    	   
    	     return tm;
    	   }

   public TreeMap getAgents1() {
	   
		return agents1;
	}
   

   private TreeMap agents1 = new TreeMap();
   private HashSet usedProds = null;
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      TreeMap  ladumuu = new TreeMap();
      HashSet laduhas = new HashSet();
       usedProds = new HashSet();
      HashMap prodHash = new HashMap();
      
     TreeMap maxdisc =  getAmountCampProdstring(prodHash);
      
     try {
   	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, case when l.[Location Code] like '%BLS%' then 'BLS-STAND' else l.[Location Code] end AS ladu FROM [Orkla Foods Latvija SIA$Item Ledger Entry] l " + 
         "  INNER JOIN [Orkla Foods Latvija SIA$Item] t ON [No_]=l.[Item No_] WHERE    t.[Blocked]=0 " +
          " GROUP BY (case when l.[Location Code] like '%BLS%' then 'BLS-STAND' else l.[Location Code] end),l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]";
  

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	              String	toode =rs.getString("kood").trim();
  	            double	seis = rs.getDouble("kogus");
  	          String ladu  =rs.getString("ladu").trim();
  	          if(ladu.equals("GAT-BABITE")){
  	          	ladutr.put(toode, seis + "");
  	          if(!laduhas.contains(toode)){
    	          	laduhas.add(toode);
    	  	      }
  	  	      }
  	          
  	        if(ladu.startsWith("NPL")){
  	          	ladumuu.put(toode, seis + "");
  	          if(!laduhas.contains(toode)){
    	          	laduhas.add(toode);
    	  	      }
  	  	      }
  	          
  	        }

  	      rs.close();
  	      stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
 
  
     try {
     	String cmd = "   SELECT t.[No_] AS toode,t.[Description]+t.[Description 2] AS nimi,t.[Net Weight] as netto, t.[Unit Price] AS myygihind,"
     			+ "t.[Product Group Code] AS grupp ,t.[Item Category Code] AS grupp2  "+ 
	" ,t.[Base Unit of Measure] AS unit,[No_ 2] AS ean,'' AS status , " +
    "			 '' AS type, [Units per Parcel] AS kastis, [Order Divider]  as optam , '' AS kaal, 21 AS vat " + //[Minimum Order Quantity]
    "			 ,1 as goes , [eAgent Bonus Item] as bonus ,[Top Product] as topprod,[Item Disc_ Group] as discgroup FROM [Orkla Foods Latvija SIA$Item] t  WHERE t.[Blocked]=0 "
   + " and  t.[Vat Prod_ Posting Group] like '%21'  and   t.[Export to eAgent] = 1    ORDER BY t.[No_]"; // and t.[Unit Price] > 0
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	double netto = rs.getDouble("netto");
         	String namesort = name;
         	int namelen = name.length();
         	name =   Converter.firstToUpperCase(name);
         	if(netto > 0 ){
         		name = name + " sv:" + NF.format(netto);
         	}
         	
         	
             double price = rs.getDouble("myygihind");
             if(price == 0){
            	 price = 1;
             }
                prod.setPrice(price);
                
                
                
              if(ladutr.get(id) != null ){
            	  prod.setPartii("GAT-BABITE : " + ((int) new Double((String)ladutr.get(id)).doubleValue()));
            	//  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }

              if(ladumuu.get(id) != null ){
            	  if(prod.getPartii() != null && prod.getPartii().length() > 1){
            		  prod.setPartii(prod.getPartii() +"|NPL : " + ((int) new Double((String)ladumuu.get(id)).doubleValue()));
                        
            	  }else{
            		  prod.setPartii("NPL : " + ((int) new Double((String)ladumuu.get(id)).doubleValue()));
                      		  
            	  }
            	//  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
              
                String grupp = Converter.cleanLitLatEst(rs.getString("grupp"));
                 
              if(grupp != null && grupp.trim().length()>0 ){
                 prod.setGroup(grupp.trim());  
              }else{
            	  prod.setGroup("CITI");  
              }
          
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                   name = name + " " + ean.substring(ean.length()-4);
                 }
        
             	if(maxdisc.get(prod.getID()) != null){
                prod.setMaxDiscount(Double.parseDouble(maxdisc.get(prod.getID())+""));
                }
             
             	

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                    }
                	
                	prod.setVat("21");
                   	
                    double	optam = rs.getDouble("optam");
                	if(optam>0){
                      prod.setOptAmountInUnits(optam);
                	}else{
                		 prod.setOptAmountInUnits(1);
                	}
                   
                   String bonus = rs.getString("bonus");
                   if(bonus.trim().equals("1")){
                	   prod.setType("BONUS");
                	   prod.setDiffColor(true);
                   }
                
                   boolean topprod = rs.getBoolean("topprod");
                   if(topprod){
                	   prod.setStatusTop("1");
                   }
                   
                   
                   String discgroup = rs.getString("discgroup");
                   if(discgroup != null  && discgroup.trim().length() > 0){
                       prod.setAlegrupp(Converter.cleanLitLatEst(discgroup.trim()));
                       prod.setGroup(Converter.cleanLitLatEst(discgroup.trim()));  
                   }else{
                 	  prod.setGroup("CITI");  
                   }
                   
            
                   
                
                   boolean goes = true;
                   if(!prod.getID().endsWith("LV") && !laduhas.contains(prod.getID())){
                	  // kui pole läti siseturg ja laoseis on null siis ei saada
                	   goes = false;
                   }
                   
                   if(goes){
                	   usedProds.add(prod.getID());
                	   name = name + "{" + namelen + "}";
                	   prod.setName(Converter.enCodeEst(name));
                	   prod.setNameSorting(namesort);
                       tm.put(name, prod); 
                       prodHash.put(prod.getID(), prod);
                   }
              
                   
        }
  
        getAmountCampProdstring(prodHash);
        rs.close();
        stmt.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     
     
     if(rcompany != null){
     	Converter.sendProductJson(tm, rcompany);
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
    		" ,d.[LV Invoice No_] as arve FROM [Orkla Foods Latvija SIA$Sales Shipment Line] r " +
    "  INNER JOIN [Orkla Foods Latvija SIA$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Orkla Foods Latvija SIA$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-120 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
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
                    
                    String arve = rs.getString("arve");
               
                    double open = 0;
                    if(amount == 0 && lastamount >0){
                    	open = lastamount;
                       
                    }else if (percent < 70){
                    	open = lastamount - amount ;
                    }
                    
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,open,0," ");
                     cp.setAbiKood(arve);
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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [Orkla Foods Latvija SIA$Sales Invoice Line] r " +
		  " INNER JOIN [Orkla Foods Latvija SIA$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
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
     return "NaviOrkla";
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
   
   public NumberFormat getNF() {
	return NF;
}
/**
 * @param nf The nF to set.
 */
public void setNF(NumberFormat nf) {
	NF = nf;
}

private TreeMap makePlans(){
	   TreeMap res = new TreeMap();
	  
	   try{
		
	   Calendar cal = Calendar.getInstance();
	   int month = (cal.get(Calendar.MONTH)+1);
	    int a = 0;
	   File f = new File (getExcelDir() + "budget.xlsx");
		  FileInputStream	 ff = new FileInputStream(f);
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheetAt(0);
	XSSFRow row = null; 
				ff.close();	
				if(sheet != null){
				
					
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						 row = sheet.getRow(i);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}

					for(int i = 1 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String agent = null;
						
						try {
							agent = (row.getCell((short) 0).getStringCellValue());
 					      } catch (Exception e) {
 						   e.printStackTrace();
 					      }
					//	System.out.println(agent);
						double plan = 0;
						try {
			    			plan = Double.parseDouble(row.getCell((short) month+1).getNumericCellValue()+ "");
						} catch (Exception e) {
	   					 e.printStackTrace();	
	   				  	 plan = Double.parseDouble(row.getCell((short) month+1).getStringCellValue());
						}
						
						if(agent != null){
						    res.put(agent.toUpperCase(),plan);
						}
 						
					}}
			   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
		System.out.println("jooksevplaan " + res.toString());
		
	   return res;
 }
 




public void setBulkSales(Map agents) {

	TreeMap plans = makePlans();
	 int yy = Calendar.getInstance().get(Calendar.YEAR);
     int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
     int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	 
 	 try {
 	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Orkla Foods Latvija SIA$Cust_ Ledger Entry] " +
 	 			" where Month(getdate())=Month([Posting Date]) and " +
            " Year(getdate())=Year([Posting Date]) and [Salesperson Code] != ''  and [Salesperson Code] != ''" +
             " group by [Salesperson Code]";// and [Global Dimension 1 Code]='400'
 	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
                String agentID = rs.getString("agent").trim();
                double sales = rs.getDouble("summa");
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                 // agent.setMonthSales((int)(sales));
                  agent.buffer = (int)(sales);

            	  double msales = agent.buffer;
        		  double psales = 0;
        		try{
        		  psales = Double.parseDouble(plans.get(agent.getID())+"");
        	  }catch(Exception e){
        		  e.printStackTrace();
        	  }
        	  double  tehtudsalperc = msales/psales;
        	  double plaanitavMyykSal = d2 * msales/dd;
        	  double  prognosalperc = plaanitavMyykSal/psales;
        	  
        	  if(tehtudsalperc > 10){
        		  tehtudsalperc = 0;  
        	  }
        	  if(prognosalperc > 10){
        		  prognosalperc = 0;  
        	  }
        	  
          String asi = "Sales: " + (int)msales + " €||";
          if(dd > 6){
        	  asi = asi + "Plan done/estimate :";
              asi = asi + "     " +(int)(tehtudsalperc*100) + "% / " + (int)(prognosalperc*100)  + " %||";  
          }
           asi = asi + "Sales plan: " + (int)psales + " €||";    
           agent.setStart( Converter.enCodeEst(asi ));
           System.out.println(Converter.enCodeEst(agentID + "' " +asi  + " "  + plans.get(agent.getID())) );
           
                  
                
                
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
   
	 try {
 	  
      File f = new File(pictspda +"list.txt");
      if (f.exists())f.delete();
      //Kataloogi sisude kustutamine
  	 	 File res2 = new File (pictspda);
         String[] ff2 = res2.list();
     	 for(int i = 0;ff2.length>i;i++){
         	(new File ( pictspda + ff2[i])).delete(); 
         }
//     	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

     StringBuffer buf = new StringBuffer();
     Iterator enume = prods.keySet().iterator();

     while (enume.hasNext()) {
	    	String asi = (String)enume.next();
	    	Product prod = (Product)prods.get(asi);
	    	String toode = prod.getEAN().trim();  
           if(toode.length() > 0){
            	
	   	  res2 = new File (pictssou);
          ff2 = res2.list();
         
     	 for(int i = 0;ff2.length>i;i++){
         	if( ff2[i].startsWith(toode)){
           // System.out.println("pilt olemas " + ff2[i]);
        	File in = new File ( pictssou + ff2[i]); 
          	if(in.isFile()){
          	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
              	Converter.shrinkAndCopyFile(in,inpda,400);
        	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
                	}
               }
             }
           }
         }
	
              
                	 
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
