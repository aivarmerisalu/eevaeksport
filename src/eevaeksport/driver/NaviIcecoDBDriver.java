package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
import eevaeksport.routines.EAN13CheckDigit;
import eevaeksport.util.Converter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviIcecoDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviIcecoDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
  }

  
  private String base = "UAB ICECO Trade";
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }


   public Vector getPriceMatrixData(){

	
	  	 String  sql = " select * from ( SELECT [Sales Code] AS custid,'' AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val from" +
            "   [" + base + "$Sales Price] where (getdate()+5 between [Starting Date] and [Ending Date]+5  or  " +
                       "  (year ([Ending Date]) = 1753 and getdate()+5 > [Starting Date]) ) and [Sales Type] = 0   " +
                          "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup,  " +
             "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val from  " +
             "  [UAB ICECO Trade$Sales Price] where ( getdate()+1 between [Starting Date] and [Ending Date] or (getdate()+1 > [Starting Date]) and  year ([Ending Date]) = 1753) " +
			  "  and [Sales Type] = 1 " +   
                          "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
            "   0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                       "  from [" + base + "$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0   " +     
                   "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
              " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                       "  from [" + base + "$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()-1 or  " +
                      "   year ([Ending Date]) = 1753) " +
                      " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                     " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                     "    from [" + base + "$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1        " +
                    " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
                   " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                    "     from [" + base + "$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()-1 or " + 
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

             
             if(date != null && !date.startsWith("1753")){
            	 mline.setEndDate(date.substring(0,10));
            	 if(sdate != null  && !sdate.startsWith("1753")){
                	 mline.setStartDate(sdate.substring(0,10));
                 }
             
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

   	
       DealerProduct dealerProds = new DealerProduct();
       try {




 		  String  cmd = "select kood , sum(kogus) as kokku , ladu from( " +
 " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [UAB ICECO Trade$Item Ledger Entry] l " +
 "        INNER JOIN [UAB ICECO Trade$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('NORDNETAS','MAR003','UTE002') and t.[Blocked]=0 " +
	"	   GROUP BY l.[Location Code], l.[Item No_] " +
	"	   ) as tabel  group by ladu,kood " +
	"	    ORDER BY kood ";
 		  
            if (isDebug()) System.out.println(cmd);
            Connection con = getNewConnection2();
               Statement stm = con.createStatement();
               ResultSet rst = stm.executeQuery(cmd);

                 while (rst.next()) {
                   String toode = rst.getString("kood");
                   if (toode != null){
                	
                   toode = toode.trim();
                   double seis = rst.getDouble("kokku");
                   String ladu  =  rst.getString("ladu").trim();
                  /* String real  =  rst.getString("real");
                   if(real != null){
                   	real =  real.trim().replaceAll(";", ",");
                   }else{
                   	real = " ";
                   }*/
                  String real = " ";
                   dealerProds.add(ladu, toode,seis+"",real," " );
                   
                   }
                   }
               rst.close();
               stm.close();
               con.close();

       } catch (Exception e) {
           getParent().showError(e);
       }

        return dealerProds;
       // return null;
      }


       public String getSaveStockInfo(){

 	  	  

    		  String  cmd = "SELECT [No_] as kood, Quantity as kokku , [Location Code] AS ladu   FROM [UAB ICECO Trade$Merisoft Stock] ORDER BY [No_] ";
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
   TreeMap tr = new TreeMap();
    		        	 
   
   String cmd2 = "select substring (Kodas,0,len(Kodas)) as toode ,(case when count(*)>0 Then sum(quantity) else 0 end ) as kogus from [UAB ICECO žuvis$Sales Line] u " + 
" INNER JOIN [UAB ICECO žuvis$Item] t ON t.[No_]=u.[No_] LEFT JOIN [UAB ICECO žuvis$Bar Codes] on Nuoroda =  u.[No_]" +
 "  where  u.[Location Code] in ('NORDNETAS') group by substring (Kodas,0,len(Kodas)) " +
 "  union " +
  "    select substring (Kodas,0,len(Kodas)) as toode ,(case when count(*)>0 Then sum(quantity) else 0 end ) as kogus from [UAB ICECO ledai$Sales Line] u " +
"	  INNER JOIN [UAB ICECO ledai$Item] t ON t.[No_]=u.[No_] LEFT JOIN [UAB ICECO ledai$Bar Codes] on Nuoroda =  u.[No_] " +
 "  where  u.[Location Code] in ('NORDNETAS') group by substring (Kodas,0,len(Kodas))";
   Statement st = con.createStatement();
     ResultSet rs = null ;//st.executeQuery(cmd2);

      		         /*  Statement st = con.createStatement();
      		           ResultSet rs = st.executeQuery(cmd2);

      		           while (rs.next()) {
      		        	   
      		           

      		             String koode = rs.getString("toode");
      		            		 if(koode != null){
                             koode = koode.trim();
      		                double seis = rs.getDouble("kogus");
      		            
      		           tr.put(koode,seis);
      	      		           }
    		           }
    		        	*/ 
    		            rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood");
    		            		 if(kood != null){

    		            		 kood = kood.trim();
    		             double seis = rs.getDouble("kokku");
    		             if(tr.get(kood) != null){
    		            	 seis = seis - Double.parseDouble(tr.get(kood) + "") ;
    		             }
    		            
    		             if(seis > 9999){
    	  	              		seis = 9999;
    	  	              	}
    	  	              	
    		             if(seis < 0){
    		            	 seis = 0;
    		             }
    		             
    		             String dealer = rs.getString("ladu").trim();
    		             if(seis > 0){
    		             
    		            	 products.append(kood +  ";");
    		                 products.append(" ;");
    		                 products.append(getParent().getNF().format(seis) + ";"  + dealer +   "\n"); // + dealer +  "\n");
    		                 if(dealer.startsWith("NORD"))
    		                 ladutr.put(kood, seis + "");
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
        	     String   cmd = "select ItemGroup as kood , ItemGroup as nimi from [UAB ICECO Trade$ActiveSalesItem] group by ItemGroup" ;
        	     	 try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = rs.getString("kood");     
        	                String name = rs.getString("nimi") ;
        	                tm.put(Converter.cleanLitLatEst(id.trim()), name);              
        	   	        }
        	   	      rs.close();
        	   	      stmt.close();
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
	 	          System.out.println(line);
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
			   // System.out.println("dubblers " + agent.getDubbler().toString());
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

     return agents;
   }

       public String getDistribution() {
         return null;
       }
   
  private HashSet sorts = new HashSet();
  

  
	
  
       public TreeMap getCustomers(Agent agent){

     	   Converter cc = new Converter();
	    	  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
	    	

	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [" + base + "$Cust_ Ledger Entry] a inner join " +
    	                            " [" + base + "$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-1000 AND a.[Open]= 1 " +
    	                            " and  a.[Customer No_] not in ('101937200') group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], " +
    	                            " a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

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
    	        if(doknr.trim().length()>6){
    	        	doknr = doknr.substring(2,doknr.length());
    	        }
    	        debt.setDoknr(doknr);
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
	  	"	  WHERE k.[Salesperson Code] != '' and k.[Remove Card] = 0 AND Blocked != 3  " ;

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
    	         
    	  
    	                String sql3 =  " select  a.[Customer No_] AS maksja, sum(b.[Amount]) AS yle FROM [UAB ICECO Trade$Cust_ Ledger Entry] a inner join " + 
    	                            " [UAB ICECO Trade$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_]  " +
    	                            " where  (a.[Document Type] = 2 or a.[Document Type]= 3 or a.[Document Type]= 0) and a.[Due Date] <getdate()+1    and a.[Posting Date] > getdate()- 1000 " +
    	                            " and a.[Customer No_] = '"+customer.getPayer() +  "' group by a.[Customer No_]  having sum(b.[Amount])<> 0  " ;

    			    try {
    			      Connection con3 = getNewConnection2();
    			      Statement stmt3 = con3.createStatement();
    			      ResultSet rs3 = stmt3.executeQuery(sql3);
    			      while(rs3.next()){
    			      
    			         double sum = rs3.getDouble("yle");
    			         if(sum !=0 && sum != -2){
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

    			   sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
	                         " (SELECT     SUM(de.[Amount (LCY)]) FROM [" + base + "$Cust_ Ledger Entry] c INNER JOIN  " +
	                         "  [" + base + "$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
	                         "   WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
	                         "   FROM  [" + base + "$Cust_ Ledger Entry] c INNER JOIN " +
	                         "    [" + base + "$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
	                         "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [Open] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
							 " 	 ) " +
						     " 	 END AS used FROM [" + base + "$Customer] k "+
						      "	WHERE  (No_ = '" + id + "') AND (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

			    try {
			      Connection con3 = getNewConnection2();
			      Statement stmt3 = con3.createStatement();
			      ResultSet rs3 = stmt3.executeQuery(sql3);
			      while(rs3.next()){
			      
			         double sum = rs3.getDouble("used");
			         if(sum !=0){
			         	customer.setUsedCredit(sum);
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
   private TreeMap ladutr = new TreeMap();
   private TreeMap ladupr = new TreeMap();
  
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
       ladutr = new TreeMap();
       ladupr = new TreeMap();
       this.getSaveStockInfo();
      TreeMap<String,Double>  prices = new TreeMap<String,Double>();
      
     try {
    	 String  cmd = " SELECT [Item No_] AS kood,  [Unit Price] AS hind ,[Starting Date],[Ending Date] from " +
                 " [UAB ICECO Trade$Sales Price] where ( getdate()+1 between [Starting Date] and [Ending Date]+2 or (getdate()+1 > [Starting Date]) and  year ([Ending Date]) = 1753) " +
   			 " and [Sales Type] = 1  and [Sales Code] = 'AKTYV25'  order by [Item No_],[Ending Date] ";

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
  	    stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
      
     
     TreeMap<String, Double> costs = new TreeMap<String, Double>();
     
     try {
    	 
     
   
      	String cmd =   "SELECT [No_] as kood, [Item Cost] AS cost   FROM [UAB ICECO Trade$Merisoft Stock] ORDER BY [No_] ";
      	
       System.out.println(cmd);
       
       this.getProdGroups();
       
       
      	Connection con = getNewConnection2();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(cmd);
         while(rs.next()){
             String id = rs.getString("kood").trim();
             double cost = rs.getDouble("cost");
             costs.put(id, cost);  
         }
         rs.close();
         stmt.close();
         con.close();
             
         } catch(Exception e) {
    		 e.printStackTrace();
    	 }
     
     
      
   //  getFixedAssortmentData();
 
  
     try {
     	String cmd = "   SELECT t.[No_] AS toode,t.[Description] +' '+t.[Description 2] AS nimi, 0 AS myygihind," + 
     			"     		 t.[ItemGroup] AS grupp   " + 
     			"	 ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount," + 
     			"			 '' AS type, [Units per Parcel] AS kastis,  '' AS kaal, [VAT Prod_ Posting Group] AS vat " + 
     			"			  FROM [UAB ICECO Trade$ActiveSalesItem] t   left join [UAB ICECO Trade$Item Cross Reference] ref on ref.[Item No_]=t.[No_] and ref.[Cross-Reference Type]=3 and " + 
     			"    ref.[Is Freight ID] = '0' AND" + 
     			"    ref.[Discontinue Bar Code] = '0' AND" + 
     			"    LEN (ref.[Cross-Reference No_]) = 13 " + 
     			"		  WHERE t.[Blocked]=0  and len(t.[Item Category Code]) > 0     ORDER BY t.[No_] "; 
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            String ean = " ";
              	  try{
              		  EAN13CheckDigit ee = new EAN13CheckDigit();
              		  ean = id.trim() + ee.calculate(id.trim());
                     
              	  }catch(Exception y){
              		  
              	  }
            
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
         	name =   Converter.firstToUpperCase(name);
         	
         	name = name.replaceAll("plombyras", "plo.");
         	name = name.replaceAll("zolotoj", "zol.");
         	name = name.replaceAll("horeca", "hrc");
         	name = name.replaceAll("ledai", "led.");
        	name = name.replaceAll("ananasais", "anan.");
        	name = name.replaceAll("kakaviniais", "kakav");


             double price = 1 ;
             
             if(prices.get(id) != null){
            	 price = prices.get(id);
             }
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
              
              if(ladupr.get(id) != null ){
            	  name = name + " @br:" + (String)ladupr.get(id);
              }
                String grupp = Converter.enCodeEst(rs.getString("grupp"));
                
                //asendame brandiga kui tootel pole gruppi 
              if(grupp != null && grupp.trim().length()>0 ){
               prod.setGroup(Converter.cleanLitLatEst(grupp.trim()));
              }
          
                prod.setUnit(rs.getString("unit"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
                 }
        if(costs.get(prod.getID()) != null) {
        	double oh = costs.get(prod.getID());
        	double kate = (1-oh/prod.getPrice())*100;
        	if(kate>0){
        		String kat = (kate + "");
        		  prod.setMargin(kat);
         	}	
          
        }
              
             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases>0){
                      prod.setInCases(cases);
                	}



                 	 String vat = rs.getString("vat");
                 	 double vati = 0;
                          if(vat != null && vat.trim().length()>0){
                            if(vat.equals("PVM21")){
                            	prod.setVat("21");
                            }else if (vat.equals("GRUPE2")){
                            	prod.setVat("5");
                            } else if (vat.equals("KM20_KAUP")){
                             	prod.setVat("20");
                             }
                          }

         
                          	
                   String add = "";
                   if(id.length() > 5){
                	   add = id.substring(0,5);
                   }
                   
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>5){
                         ee = " " + ean.substring(ean.trim().length()-5);
                       }
                	 prod.setName(Converter.enCodeEst(name) + ee);
                     tm.put(add + name, prod); 
                 
          
        }
  
        rs.close();
        stmt.close();
        con.close();
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
    		" FROM [" + base + "$Sales Shipment Line] r " +
    "  INNER JOIN [" + base + "$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [" + base + "$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-180 and r.[Quantity] > 0 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [" + base + "$Sales Invoice Line] r " +
		  " INNER JOIN [" + base + "$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 and r.[Quantity] > 0 " +
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
     return "NaviIceco";
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
   private String url2 = "jdbc:odbc:naviuus";
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


}
