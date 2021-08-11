package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.SupervisorInfoWrapper;
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

public class NaviGPRLatDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public NaviGPRLatDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
  }

  
  private String base = "Pernod Ricard Latvia";
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getPriceMatrixData(){

	  // saane k�tt eyldisaed ALED kliendila  
	   String ale =   " SELECT [Sales Code] AS clgroup,Percentage as disc from " +
      " [" + base + "$Sales Discount Item Charge] where No_ ='DISCOUNT' and " +
     " [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or year ([Ending Date]) = 1753) 	and [Source Type] = 3 ";
	   
	   TreeMap yldaled = new TreeMap();
	   

	   try {
		   
      	   Connection con2 = getNewConnection2();
   	        Statement stmt2 = con2.createStatement();
   	        ResultSet rs2 = stmt2.executeQuery(ale);
   	        
           while (rs2.next()) {
        	  yldaled.put(rs2.getString("clgroup").trim(), rs2.getDouble("disc")) ;
           }
           
           rs2.close();
           stmt2.close();
           con2.close();
	   }catch (Exception k ){
		   k.printStackTrace();
	   }
	   
	   
	//1. ona vaja saada baashinnakiri m�llu 
	   
	   
	   
	   TreeMap baash = new TreeMap();
	   
	   String hind = "  SELECT t.[No_] AS toode, sp.[Unit Price] AS hind   FROM [" + base + "$Item] t " +
			 " left join  [" + base + "$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Code] ='FY16' and sp.[Starting Date] < getdate()+5  and " + 
			 "  (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +
		     " WHERE t.[Blocked]=0     and sp.[Unit Price] > 0  ORDER BY t.[No_] ";
	   
	   try {
		   
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(hind);
           while (rs.next()) {
        	  baash.put(rs.getString("toode").trim(), rs.getDouble("hind")) ;
           }
           
	   }catch (Exception k ){
		   k.printStackTrace();
	   }
	   
	   
	        
    System.out.println("baashinnad " + baash.size());
    //2. v�tame v�lja k�ik erihinnad ja paneme m�llu 

    
    String  sql = "    SELECT '' AS custid,[Sales Code] AS clgroup,[Source No_] AS prodid,'' AS prodgroup, " +
            "  [Unit Price] AS price ,Percentage as disc, [Ending Date] as paev, [Starting Date] as startday  from"+
             "  [" + base + "$Sales Discount Item Charge] where No_ ='DISCOUNT' and"
             + " [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or year ([Ending Date]) = 1753)  " ;
                          

       TreeMap erihinnad = new TreeMap();
         try {
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {
             String clGroup = rs.getString("clgroup").trim();
             String prodID = rs.getString("prodid").trim();
             double disc = rs.getDouble("disc");
             if(baash.get(prodID) != null && disc > 0){
            	 double baas = Double.parseDouble(baash.get(prodID)+ "");
            	 double hin = baas * (100-disc)/100;
            	 MatrixLine ml = new MatrixLine ();
            	 ml.setCliGroup(clGroup);
            	 ml.setProdID(prodID);
            	 ml.setPrice(hin);
            	 if(erihinnad.get(clGroup)!= null){
            		 TreeMap sisu = (TreeMap)erihinnad.get(clGroup);
            		  sisu.put(prodID, ml);
            		  erihinnad.put(clGroup, sisu);
            	 }else{
            		 TreeMap sisu = new TreeMap();
           		       sisu.put(prodID, ml);
           		        erihinnad.put(clGroup, sisu);
            	 }
              }
           }
           rs.close();
           stmt.close();
           con.close();
         }catch(Exception e){
        	 e.printStackTrace();
         }

    
    
    
    
    //2. leiamekampaaniad hinna gruppidele ja paneme m�llu 
	   
	  	 String  sql3 = "    SELECT '' AS custid,[Sales Code] AS clgroup,[Source No_] AS prodid,'' AS prodgroup, " +
            "  [Unit Price] AS price ,Percentage as disc, [Ending Date] as paev, [Starting Date] as startday  from"+
             "  [" + base + "$Sales Discount Item Charge] where No_ ='CAMP_DISC' and"
             + " [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or year ([Ending Date]) = 1753)  " ;
            
       Vector result = new Vector();
       HashSet sees = new HashSet();
         try {
          
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql3);

   	        while (rs.next()) {

             String clGroup = rs.getString("clgroup").trim();
             String prodID = rs.getString("prodid").trim();
             double disc = rs.getDouble("disc");
             String date = rs.getString("paev");
             String sdate = rs.getString("startday");
             
             MatrixLine mline = new MatrixLine();
             
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
             }

             if(sdate != null  && !sdate.startsWith("1753")){
            	 mline.setStartDate(sdate.substring(0,10));
             }
             if(date != null && !date.startsWith("1753")){
            	 mline.setEndDate(date.substring(0,10));
             }
             if (prodID != null && prodID.length() > 0) {
               mline.setProdID(prodID);
             }
             
             if (disc != 0) {
            	  if(erihinnad.get(clGroup)!= null){
                	 TreeMap sisu = (TreeMap)erihinnad.get(clGroup);
                	 if(sisu.get(prodID) != null){
                		 MatrixLine mm = (MatrixLine)sisu.get(prodID);
                		 double pr = mm.getPrice();
                		 mline.setPrice(pr *(100-disc)/100);
                	 
                     }else if (yldaled.get(clGroup) != null){
                	     double dis = (Double)yldaled.get(clGroup); 
                	     try{
                    		 double baas = Double.parseDouble(baash.get(prodID)+ "");
                        	 double hin = baas * (100-dis)/100;
                        	 hin = hin * (100-disc)/100;
                        	 mline.setPrice(hin); 
                	     }catch(Exception e){
                	    	 e.printStackTrace();
                	     }
                  }
            	  }else if (yldaled.get(clGroup) != null){
             	     double dis = (Double)yldaled.get(clGroup); 
             	     try{
                 		 double baas = Double.parseDouble(baash.get(prodID)+ "");
                     	 double hin = baas * (100-dis)/100;
                     	 hin = hin * (100-disc)/100;
                     	 mline.setPrice(hin); 
             	     }catch(Exception e){
             	    	 e.printStackTrace();
             	     }
               }	 
                 if( mline.getPrice() == -1 && baash.get(prodID) != null){
                	 mline.setPrice(Double.parseDouble(baash.get(prodID) +"") *(100-disc)/100 );
                 }
                 //leiame eri hinna - kui pole siis baas hinnast 
             }
                 result.add(mline); 
                 sees.add(clGroup + prodID);
           }
           rs.close();
           stmt.close();
           con.close();
         }catch(Exception e){
        	 e.printStackTrace();
         }
         
         //vaatame millised erihinnad paneme maatriksisse juurde
         
         Iterator itera  = erihinnad.keySet().iterator();
         while (itera.hasNext()){
        	 String clgr = itera.next()+"";
        	 TreeMap tr = (TreeMap)erihinnad.get(clgr);
        	  Iterator iteras  = tr.keySet().iterator();
              while (iteras.hasNext()){
            	  String prgr = iteras.next()+"";
            	  MatrixLine ml = (MatrixLine)tr.get(prgr);
            	  if(!sees.contains(clgr+prgr)){//kui ei ole seda kombinatsiooni siis l�heb kaaasa
            		  result.add(ml);
            	  }
              }
         }
         System.out.println("result " + result.size());
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


   public DealerProduct DealerProductsList() {
     return null;
   }


       public String getSaveStockInfo(){

    	   
    	   
 	  	  
    		  String  cmd = "  SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [" + base + "$Item Ledger Entry] l " +
          " INNER JOIN [" + base + "$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('SALES') and t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_] ";

    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         TreeMap bronn = new TreeMap();
    		         try {
    		        	 String aaa = "select r.No_ as toode, sum (r.Quantity) as kogus from [" + base + "$Sales Header] d inner join [" + base + "$Sales Line] r " +
    		    	   		     " on d.No_=r.[Document No_] where d.Ship=0 " +
    		    				 " group by r.No_";
    		        	 Connection con = getNewConnection2();
      		           Statement st = con.createStatement();
      		           ResultSet rs = st.executeQuery(aaa);

      		           while (rs.next()) {
      		        	   bronn.put(rs.getString("toode") , rs.getDouble("kogus"));
      		           }
      		           
      		           rs.close();
      		           st.close();
    		        	 
    		        	 
    		           st = con.createStatement();
    		            rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood").trim();
    		             double seis = rs.getDouble("kogus");
    		             
    		             if(seis > 0){
    		            	 if(bronn.get(kood) != null){
    		            		 seis = seis - Double.parseDouble(bronn.get(kood)+"");
    		            	 }
    		              	 if(seis > 0){
    		                     products.append(kood +  ";");
        		                 products.append(" ;");
        		                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
        		        	 }
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

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }

           
           public TreeMap getProdGroups() throws SQLException {
        	     TreeMap   tm = new TreeMap();
        	     setTreeGroup(true);
        	     String cmd = " SELECT  [Code] AS kood ,[Description] AS nimi  FROM [" + base + "$Item Category] " ;
        	     	
        	     
        	     	  try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = rs.getString("kood");     
        	                String name = rs.getString("nimi") ;
        	   	             tm.put((id), name);  	             
        	   	        }
        	   	      rs.close();
        	   	      con.close();

        	        } catch (Exception e){
        	          e.printStackTrace();
        	        }

        	         return tm;
        	   }

           
           

           private TreeMap koodJaGrupp = new TreeMap(); 
         
           public TreeMap getProdGroupsSecond() throws SQLException {
               return teised;
             }


           
           
           
           
  
   private TreeMap twoone = new TreeMap();
   private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   StringBuffer sb2 = new StringBuffer();
	    
	    String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM " + 
		"  [" + base + "$Salesperson_Purchaser] ";
        
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
	    		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
	    	}catch (Exception e){
	    	}
	      }

     return agents;
   }

       public String getDistribution() {
         return null;
       }
   
  private HashSet sorts = new HashSet();
  

  
	
  
       public TreeMap getCustomers(Agent agent){

     	   Converter cc = new Converter();
	    	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	    	  HashMap volad = new HashMap();
	    	  
	    	  
	    	  
	    	  

              String sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
                       " (SELECT     SUM(de.[Amount (LCY)]) FROM [" + base + "$Cust_ Ledger Entry] c INNER JOIN  " +
                       "  [" + base + "$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
                       "   WHERE   c.[Customer No_] = k.No_ and [Due Date] < getdate()  and c.[Posting Date] > getdate()- 200 ) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
                       "   FROM  [" + base + "$Cust_ Ledger Entry] c INNER JOIN " +
                       "    [" + base + "$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
                       "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [Open] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
						 " 	 and [Due Date] < getdate()   and c.[Posting Date] > getdate()- 200) " +
					     " 	 END AS yle FROM [" + base + "$Customer] k "+
					      "	WHERE   (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

		    try {
		      Connection con3 = getNewConnection2();
		      Statement stmt3 = con3.createStatement();
		      ResultSet rs3 = stmt3.executeQuery(sql3);
		      while(rs3.next()){
		         String cust = rs3.getString("kood");
		         try{
		        	 double sum = rs3.getDouble("yle");
			         if(sum !=0){
			         	volad.put(cust.trim(), sum);
			         }	 
		         }catch(Exception e){
		        	 
		         }
		      }

rs3.close();
stmt3.close();
con3.close();
		    } catch (Exception e){
		         e.printStackTrace();
		       }

          	


	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  
	    	  String ale =   " SELECT [Sales Code] AS clgroup,Percentage as disc from " +
	    		      " [" + base + "$Sales Discount Item Charge] where No_ ='DISCOUNT' and " +
	    		     " [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or year ([Ending Date]) = 1753) 	and [Source Type] = 3 ";
	    			   
	    			   TreeMap yldaled = new TreeMap();
	    			   

	    			   try {
	    				   
	    		      	   Connection con2 = getNewConnection2();
	    		   	        Statement stmt2 = con2.createStatement();
	    		   	        ResultSet rs2 = stmt2.executeQuery(ale);
	    		   	        
	    		           while (rs2.next()) {
	    		        	  yldaled.put(rs2.getString("clgroup").trim(), rs2.getDouble("dics")) ;
	    		           }
	    		           
	    		           rs2.close();
	    		           stmt2.close();
	    		           con2.close();
	    			   }catch (Exception k ){
	    				   k.printStackTrace();
	    			   }
	    	  
	    	  
	    	TreeMap discRel = new TreeMap();
	    	  try {

	    	      String cmd2 = " select [Source No_] as klient , Code As sort from "
	    	      		+ "[" + base + "$Drink Discount Relation] k " ;
	    	      Connection conv3 = getNewConnection2();
	    	       Statement stmt13 = conv3.createStatement();
	    	       ResultSet rs13 = stmt13.executeQuery(cmd2);
	    	       while(rs13.next()){
	    	       String custID = rs13.getString("klient").trim();
	    	       String sort = rs13.getString("sort").trim();
	    	       if(discRel.get(custID) != null){
	    	    	   discRel.put(custID,discRel.get(custID)+ "|" + sort);
		    	       
	    	       }else{
	    	    	   discRel.put(custID, sort);
		    	          
	    	       }
	    	         
	    	            }

	    	       rs13.close();
	    	       stmt13.close();
	    	       conv3.close();
	    	  }catch(Exception e){
	        	 e.printStackTrace();  
	           }
	    	  
	    	System.out.println("discRel " +  discRel.toString());
	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, a.[Posting Date] AS kuup, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa,  " +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [" + base + "$Cust_ Ledger Entry] a inner join " +
    	                            " [" + base + "$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1  " +
    	                            " and  a.[Customer No_] not in ('801615','806414') group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_],  " +
    	                            " a.[Due Date] ,a.[Posting Date], DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";
    	    //rimi   806414
     	        //maxima 801615

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection2();
    	       Statement stmt1 = conv.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient");
    	        debt.setCustID(custID);

    	        String doknr = rs1.getString("number");
    	        
    	        debt.setDoknr(doknr);
    	        debt.setDate(rs1.getDate("kuup"));
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


    	   String sql =  "   SELECT k.[Name] AS nimi , k.City as addr2 ,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address] + ' ' + k.[City] AS aadress " +  
    	"   	,k.[Salesperson Code] AS agent ,  k.Blocked AS blokk,   " +
    	"  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type,  " +
    	"	   0 AS extradays ,[Customer Price Group]  AS sort,'' AS debt , 0 AS minsum ,k.Contact +' ' + k.[Phone No_] as kontakt , 0 as sendall " +
    	" ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 FROM ["+ base + "$Customer] k  " +
	  	"	  WHERE k.[Salesperson Code] != ''  AND Blocked != 3  " ;

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	         String addr2 = rs.getString("addr2");
    	         if(addr2 != null){
    	        	
    	         }
    	         name = name.replaceAll("\"", " ");
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           
    	           boolean goes = true;
    	           
    	           
    	           Customer customer = new Customer(id);
    	           
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             }

    	           	
    	             String aadress = rs.getString("aadress");
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Latvia"));
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

    	             if(!rs.getString("blokk").equals("0")){
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

   	        
   	        //rimi   806414
   	        //maxima 801615

   	          if(customer.getPayer().equals("801615") ||
     	        		customer.getPayer().equals("806414")   ){
     	        	customer.setNodebt(true);
     	         }
    	      
    	           if (!customer.isNodebt()){
    	                 if(volad.get(customer.getID()) != null){
    	                	 customer.setDebt(Double.parseDouble(volad.get(customer.getID())+""));
    	                 }
    	      	        
    	           }
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("D"," ");
    	               paym = paym.replaceAll("SR","-1").trim();
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
    	           
    	           cc.addQuestiosToCustomer(customer,kyssad);    	          

    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0){
    	        	//customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	       //    System.out.println("sss " + discRel.get(customer.getID()));
    	       
    	     	   String ddd = discRel.get(customer.getID())+"";
            	   
    	           if(discRel.get(customer.getID()) != null && !ddd.trim().equals("FY16") ){
    	        	   customer.setGroup(Converter.cleanLitLatEst(ddd));
    	        	   StringTokenizer st = new StringTokenizer(ddd,"|");
    	        	   while(st.hasMoreTokens()){
    	        		   String aaa = st.nextToken();
    	        		   if(yldaled.get(aaa)!= null){
    	        			   customer.setDiscount(Double.parseDouble(yldaled.get(aaa)+ ""));
    	        		   }
    	        	   }
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	       
    	        	if(customer.getGroup().trim().length() >0){
    	        	//customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
    	        	 	//customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 	
    	        	}else{
    	        	 	//customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
    	        	}
    	           }
    	           
    	              if(goes){
    	            	    tm.put(customer.getName(), customer);
    	    	            this.customers.add(customer.getID());
    	    	      }
    	           
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
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      TreeMap  laduaks = new TreeMap();
      TreeMap  bronn = new TreeMap();
      
     try {
   	  String cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [" + base + "$Item Ledger Entry] l " +
          " INNER JOIN [" + base + "$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('SALES') and t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]";
   	  
   	String aaa = "select r.No_ as toode, sum (r.Quantity) as kogus from [" + base + "$Sales Header] d inner join [" + base + "$Sales Line] r " +
  		     " on d.No_=r.[Document No_] where d.Ship=0 " +
			 " group by r.No_";
	 Connection con = getNewConnection2();
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(aaa);

    while (rs.next()) {
 	   bronn.put(rs.getString("toode") , rs.getDouble("kogus"));
    }
    
    rs.close();
    st.close();
    
  	        Statement stmt = con.createStatement();
  	         rs = stmt.executeQuery(cmd);
  	        
  	        
  	        
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood");
  	              	seis = rs.getDouble("kogus");
  	              	
  	              if(bronn.get(toode) != null){
	            		 seis = seis - Double.parseDouble(bronn.get(toode)+"");
	            	 }
  	              	ladutr.put(toode, seis + "");
  	        }

  	        
  	      cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [" + base + "$Item Ledger Entry] l " +
  	            " INNER JOIN [" + base + "$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('EXCISE') and t.[Blocked]=0 " + 
  	            " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]";
  	    rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	             String toode = null;
	             double seis = 0;;
	              	toode =rs.getString("kood");
	              	seis = rs.getDouble("kogus");
	              	laduaks.put(toode, getParent().getNF().format(seis));
	        }
  	      rs.close();
  	    stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
   //  getFixedAssortmentData();
 
  
     try {
     	String cmd = "  SELECT t.[No_] AS toode,t.[Search Description] AS nimi, sp.[Unit Price] AS myygihind,t.[Product Group Code] AS grupp ,t.[Item Category Code] AS grupp2  " +
	" ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount, " +
	"		 '' AS type, vat.[Qty_ per Unit of Measure] AS kastis,pa.[Qty_ per Unit of Measure] AS alus,  '' AS kaal, [VAT Prod_ Posting Group] AS vat "+  
	"		 ,1 as goes ,rel.Code as discgr " +
	"		  FROM [" + base +"$Item] t " +
	"		  LEFT JOIN  [" + base +"$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='CAS' )  " +
	"		  LEFT JOIN  [" + base +"$Item Unit of Measure] pa ON (pa.[Item No_]=[No_] and pa.[Code]='PALBO' )  " +
	"		  left join  [" + base +"$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Code] ='FY16' and sp.[Starting Date] < getdate()+5  and  " +
	"		   (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +
	"		   left join [" + base + "$Item Cross Reference] ref on ref.[Item No_]=t.[No_]  and [Unit of Measure]=t.[Base Unit of Measure]   " +
	"     left join [" + base + "$Drink Discount Relation] rel on rel.[Source No_]=t.No_ and rel.[Source Type] = 3  " + 
	
	"		  WHERE t.[Blocked]=0     and sp.[Unit Price] > 0  ORDER BY t.[No_]"; 
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	name =   Converter.firstToUpperCase(name) + " " +id;
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
              if(laduaks.get(id) != null ){
            	  prod.setPartii("Aks:" + (String)laduaks.get(id));
              }
                String grupp = Converter.enCodeEst(rs.getString("grupp2"));
              if(grupp != null && grupp.trim().length()>0 ){
               prod.setGroup(grupp.trim());  
              }
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                //   name = name + " " + ean.substring(ean.trim().length()-3);
                 }
          /*          String xxx = rs.getString("status");
     try{
         int xx = Integer.parseInt(xxx);
         if(xx == 1){
          	prod.setStatusTop("1");
          } else if (xx == 2){
            prod.setMustBe("1");
          } else if (xx == 3) {
            prod.setMustBe("1");
            prod.setStatusTop("1");
          } else if (xx == 4) {
             prod.setStatusNew("1");
          } else if (xx == 5) {
             prod.setStatusSellout("1");
          }
     }catch(Exception e){
    	 
     }
            */  

             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}
                	 double	pall = rs.getDouble("alus");
                 	if(pall>0){
                       prod.setOnPallet(pall);
                 	}


                 	 String vat = rs.getString("vat");
                 	 double vati = 0;
                          if(vat != null && vat.trim().length()>0){
                            if(vat.equals("PVM21")){
                            	prod.setVat("21");
                            } else if (vat.equals("T20G")){
                             	prod.setVat("20");
                             }
                          }

         
                          	
                   String add = "";
                   if(id.length() > 5){
                	   add = id.substring(0,5);
                   }
                   
                   boolean goes = true;
                   double	goe = rs.getDouble("goes");
                 	if(goe==0 && !sorts.contains(id) && prod.getStock() < 1){
                 	// goes = false;
                 	}
                 
                   String discgr = rs.getString("discgr");
                 	
                 	if(discgr != null && discgr.trim().length()>0){
                    prod.setAlegrupp(discgr.trim());
                    }
                 	
                 if(goes ){
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>3){
                         ee = " " + ean.substring(ean.trim().length()-3);
                       }
                	 name = change(name);
                	 prod.setName(Converter.enCodeEst(name) + ee);
                     tm.put(add + name, prod); 
                 }
          
        }
  
        rs.close();
        stmt.close();
        con.close();
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

 
   private String change(String name){
	   name = name.replaceAll("chardonnay", "cha.");
	   name = name.replaceAll("chateauneuf", "chate.");
	   
	   name = name.replaceAll("reserva", "res.");  
	   name = name.replaceAll("sauvignon", "sauv.");
	   name = name.replaceAll("cabernet", "cab.");
	   name = name.replaceAll("scotch whisky", "sc.wh.");
	   name = name.replaceAll("limited edition", "lim.");
	   name = name.replaceAll("bordeaux", "bor.");
	   name = name.replaceAll("estate", "est.");
	   name = name.replaceAll("blanc", "bl.");
	   name = name.replaceAll("gewurztraminer", "gewur.");
	   name = name.replaceAll("cotes du rhone", "CdR");
	   name = name.replaceAll("rouge", "rou.");
	   name = name.replaceAll("meffre", "mef.");
	   name = name.replaceAll("shiraz", "shz.");
	   name = name.replaceAll("anniversary", "anniv.");
	   name = name.replaceAll("edition", "edi.");
	   name = name.replaceAll("champagne", "champ.");
	   name = name.replaceAll("riesling", "ries.");
	   name = name.replaceAll("whisky", "wh.");
	   name = name.replaceAll("trocken", "tro.");
	   name = name.replaceAll("qualit�tswein", "quali.");
	   name = name.replaceAll("seleccion", "sel.");
	   name = name.replaceAll("varietal", "var.");
	   
	   return name ;
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
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus,"
    		+ " d.[Posting Date] AS lastdate,r.[VAT Base Amount] as lastamount" +
    		" FROM [" + base + "$Sales Shipment Line] r " +
    "  INNER JOIN [" + base + "$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [" + base + "$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-180 and r.Type = 2 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
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
                    
                    double lastamount =  0;
                    try{
                    	lastamount = rs.getDouble("lastamount");
                    	if(lastamount > 0 && amount > 0){
                    		lastamount = lastamount / amount;
                    	}else{
                    		lastamount =  0;
                    	}
                    }catch(Exception e){
                    	
                    }
                    
                    Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,lastamount,0,0," ");

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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [" + base + "$Sales Invoice Line] r " +
		  " INNER JOIN [" + base + "$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
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
     return "NaviPernod";
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
   private String url2 = "jdbc:odbc:navilat";
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
 	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [" + base + "$Cust_ Ledger Entry] " +
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

 	String cmd2 = "  SELECT t.[No_] AS toode,t.[Search Description] AS nimi, sp.[Unit Price] AS myygihind,t.[Product Group Code] AS grupp ,t.[Item Category Code] AS grupp2  " +
" ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount, " +
"		 '' AS type, [Qty_ per Unit of Measure] AS kastis,  '' AS kaal, [VAT Prod_ Posting Group] AS vat "+  
"		 ,1 as goes " +
"		  FROM [" + base +"$Item] t " +
"		  LEFT JOIN  [" + base +"$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='CAS' )  " +
"		  left join  [" + base +"$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Code] ='FY16' and sp.[Starting Date] < getdate()+5  and  " +
"		   (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +
"		   left join [" + base + "$Item Cross Reference] ref on ref.[Item No_]=t.[No_]  and [Unit of Measure]='BT'   " +
"		  WHERE t.[Blocked]=0     and sp.[Unit Price] > 0  ORDER BY t.[No_]"; 
   	
     	String picts = parent.getProps().getProperty("pict.dir");
 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
 	   StringBuffer buf = new StringBuffer();
           Vector valid = new Vector();
   	try {
 	   Connection con = getNewConnection2();
	
   		Statement stmt = con.createStatement();
   	    ResultSet	      rs = stmt.executeQuery(cmd2);


      
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
         valid.add(toode);
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
                		//	System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
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
   	  System.out.println("List Sisu  "  + buf.toString());	    
               File f = new File("list.txt");
               if (f.exists())f.delete();
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
//        	 L�ppes kustutamine 
        	 
          System.out.println("Piltide kopeerimine ");	
          //Failide kopeerimine  
            String[] gg = new File(picts).list();
         	  //System.out.println("gg " + gg[0] + " " + gg.length);
            
             for(int i = 0;gg.length>i;i++){          	
             	File in = new File ( picts + gg[i]); 
             	if(in.isFile() && gg[i].indexOf("Thumbs") == -1){            	
             	File inpda = new File ( pictspda + toUpper(gg[i])); 
                   	Converter.shrinkAndCopyFile(in,inpda);
                   	if(valid.contains(getProdCode(gg[i]))){
                       	buf.append(getProdCode(gg[i])+".jpg;"  + inpda.length() + "\n");
                   }
             	}
             }
             
             //piltide kopeerimine alam kataloogist ..
             
             if(cats.size() >0){
            	 for(int h = 0;h < cats.size(); h++){
            		
            	   String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
               	 
                  
                   for(int i = 0;i < ff.length  ;i++){          	
                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
                   	if(in.isFile() && gg[i].indexOf("Thumbs") == -1){   
                   		
                   	File inpda = new File ( pictspda + toUpper(ff[i])); 
                   	Converter.shrinkAndCopyFile(in,inpda);
                   	if(valid.contains(getProdCode(ff[i]))){
                       	buf.append(getProdCode(ff[i])+".jpg;"  + inpda.length() + "\n");
                   }
                   	 }
                   }
                   
            	 }
             }
             
             File fff =  new File (pictspda +  "list.txt");
       	 // Converter.copyFile(f,fff);  	
       	  File fff0 =  new File (pictspda +  "list0.txt");
       	Converter.copyFile(f0,fff0);  	
   	} catch (Exception e) {
   	    getParent().showError(e);
   	}
   	
   	
    
    try {
		PrintWriter os = new PrintWriter(new FileWriter(
				pictspda + "list.txt", true));
		os.write(buf.toString());
		os.close();
	
	  }catch (Exception ex) {
		ex.printStackTrace();
	  }
	
   	
   
}
 
   private String toUpper(String asi ){
	   System.out.println(asi);
	   asi = asi.substring(0,asi.indexOf("_")).toUpperCase() + asi.substring(asi.lastIndexOf("."));
	   return asi ;
   }
   
   private String getProdCode(String asi ){
	   System.out.println(asi);
	   asi = asi.substring(0,asi.indexOf("_")).toUpperCase();
	   return asi ;
   }



@Override
public void PictureMaker() {
	makePictureCat();
	 System.exit(0);
	
}


}
