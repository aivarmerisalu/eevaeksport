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
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviOskarDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviOskarDBDriver(EksportPanel parent) {
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

	
	  	 String  sql = "  SELECT [Sales Code] AS custid,'' AS clgroup,[Item Code 1] AS prodid,'' AS prodgroup, " +
            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val from" +
            "   [Oskar LT$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or" +
             "           year ([Ending Date]) = 1753)  and [Sales Type] = 0    " +
                          "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item Code 1]  AS prodid,'' AS prodgroup, " +
             " [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val from " +
             " [Oskar LT$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or " +
                      "  year ([Ending Date]) = 1753) and [Sales Type] = 1 " +   
                          "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
            "   0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                       "  from [Oskar LT$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0   " +     
                   "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
              " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                       "  from [Oskar LT$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or  " +
                      "   year ([Ending Date]) = 1753) " +
                      " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                     " 0 AS price ,[Line Discount _] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency Code] as val " +
                     "    from [Oskar LT$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1        " +
                    " " ; 
			
  	 

       Vector result = new Vector();
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
             }
             
             if (curr != null && curr.length() > 0) {
                 mline.setCurrency(curr.trim());
               }
             
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
		Vector lines = new Vector();
		
		  try {
          	  String cmd = "  select [Item No_] as toode, s.[Shop Level] as grupp from " +
          	  		" [Oskar LT$Shop Level Assortment] s INNER JOIN [Oskar LT$Item] t on t.No_= [Item No_] " +
          	  		" ORDER BY [Search Description]";
          	System.out.println(cmd);
             	 Connection con = getNewConnection2();
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
                           
                            lines.add(mline);
                          }

         	       rs.close();
         	       con.close();
                         } catch (Exception e) {
                             getParent().showError(e);
                         }
           return lines;

	}


   public DealerProduct DealerProductsList() {
     return null;
   }


       public String getSaveStockInfo(){

 	  	  
    		  String  cmd = "  SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Oskar LT$Item Ledger Entry] l " +
          " INNER JOIN [Oskar LT$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('71VIA3L-T') and t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_] ";

    		         if (isDebug()) System.out.println(cmd);
    		         StringBuffer products = new StringBuffer();
    		         try {
    		        	 Connection con = getNewConnection2();
    		           Statement st = con.createStatement();
    		           ResultSet rs = st.executeQuery(cmd);

    		           while (rs.next()) {

    		             String kood = rs.getString("kood").trim();
    		             double seis = rs.getDouble("kogus");
    		             if(seis > 0){
    		                 products.append(kood +  ";");
    		                 products.append(" ;");
    		                 products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
    		            //   kogused.put(kood, seis + "");
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
        	   //  setTreeGroup(true);
        	     String cmd = " SELECT  [Code] AS kood ,[Description] AS nimi  FROM [Oskar LT$Product Group] " +
        	     		"  where [Item Category Code] !='' order by [Code] ";
        	     	  try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = rs.getString("kood");     
        	                String name = rs.getString("nimi") ;
        	                if(name == null || name.trim().length() == 0){
        	                	name = id;
        	                }
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
           
        	   TreeMap   tm = new TreeMap();
        	   //  setTreeGroup(true);
        	     String cmd = " SELECT  [Item Category Code] AS kood ,[Description] AS nimi  FROM [Oskar LT$Product Group] " +
        	     		"  where [Item Category Code] !='' order by [Code] ";
        	     	  try {
        	       	 Connection con = getNewConnection2();
        	       	System.out.println(cmd);
        	   	        Statement stmt = con.createStatement();
        	   	        ResultSet rs = stmt.executeQuery(cmd);
        	   	        while(rs.next()){
        	   	            String id = rs.getString("kood");     
        	                String name = rs.getString("nimi") ;
        	                if(name == null || name.trim().length() == 0){
        	                	name = id;
        	                }
        	   	             tm.put((id), name);  	             
        	   	        }
        	   	      rs.close();
        	   	      con.close();

        	        } catch (Exception e){
        	          e.printStackTrace();
        	        }

        	         return tm;
           
           }


           
           
           
           
  
   private TreeMap twoone = new TreeMap();
   private TreeMap teised = new TreeMap();
   
   public TreeMap getAgents(){
	   StringBuffer sb2 = new StringBuffer();
	    
	    String cmd = "SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM " + 
		"  [Oskar LT$Salesperson_Purchaser] ";
        
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
	          if(name == null || name.trim().length() == 0){
	        	  name = agent.getID();
	          }
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
	        

	        Agent aad = new Agent("ALDO");
	        aad.setName("ALDO");
	        agents.put("ALDO", aad);
	        
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
	    	
	    	  try{
	    		   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
	    	}catch(Exception e){

	    	}
	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [Oskar LT$Cust_ Ledger Entry] a inner join " +
    	                            " [Oskar LT$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
    	                            " and  a.[Customer No_] not in ('313071','313074','300006','300001','300004','300007','3437652300','2444685200','2371531250', " +
									" '230335177','117449707') group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], " +
    	                            " a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

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
    	"  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,[MJ Visit] AS visit , [MJ Phone Call] as call,[Chain Name] AS chain,  " +
    	"	   0 AS extradays ,[Shop Level]  AS sort,'' AS debt , 0 AS minsum ,k.Contact +' ' + k.[Phone No_] as kontakt , 0 as sendall " +
    	" ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 FROM [Oskar LT$Customer] k  " +
	  	"	  WHERE k.[Salesperson Code] != ''  AND Blocked != 3  " ;

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	         String addr2 = rs.getString("addr2");
    	         
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
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
    	               name = name + " " + Converter.enCodeEst(aadress);
    	             }
    	             
    	             customer.setName(Converter.enCodeEst(name));
    	         	
    	             String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag));
    	             customer.setMainAgent(ag.trim());
    	             customer.addAgent(new Agent("ALDO"));
                     customer.addAgent(new Agent("AIN"));
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
   	      	String call = rs.getString("call");
	             if(call != null && call.trim().length()>0 && !call.trim().toLowerCase().equals("x")){
	                customer.setCallDay(Converter.getVisitDay(call.trim()));
	             }

   	          String chain = rs.getString("chain");
	           	if(chain != null && chain.trim().length()>0){
	             customer.setChain(chain.trim());
	           	}
 	           	
 	        	String sort = rs.getString("sort");
 	           	if(sort != null && sort.trim().length()>0){
 	             customer.addFixSortCode(sort.trim());
 	           	}

   	             
   	          if(customer.getPayer().equals("313071") ||
     	        		customer.getPayer().equals("313074") ||
     	        		customer.getPayer().equals("300006") ||
     	        		customer.getPayer().equals("300001") ||
     	        		customer.getPayer().equals("300004") ||
     	        		customer.getPayer().equals("300007") ||
     	        		customer.getPayer().equals("3437652300") ||
     	        		customer.getPayer().equals("2444685200") ||
     	        		customer.getPayer().equals("2371531250") ||
     	        		customer.getPayer().equals("2303351772") ||
     	        		customer.getPayer().equals("117449707") ){
     	        	customer.setNodebt(true);
     	         }
    	      
    	           if (!customer.isNodebt()){
    	         
    	  
    	                String sql3 =  "  SELECT     No_ AS kood, CASE WHEN k.[Bill-to Customer No_] = '' THEN " +
    	                         " (SELECT     SUM(de.[Amount (LCY)]) FROM [Oskar LT$Cust_ Ledger Entry] c INNER JOIN  " +
    	                         "  [Oskar LT$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
    	                         "   WHERE   c.[Customer No_] = k.No_) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
    	                         "   FROM  [Oskar LT$Cust_ Ledger Entry] c INNER JOIN " +
    	                         "    [Oskar LT$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
    	                         "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [Open] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
    							 " 	 and [Due Date] <getdate()+7    and c.[Posting Date] > getdate()- 100) " +
    						     " 	 END AS yle FROM [Oskar LT$Customer] k "+
    						      "	WHERE  (No_ = '" + id + "') AND (Blocked <> 3) AND ([Salesperson Code] <> '') " ;

    			    try {
    			      Connection con3 = getNewConnection2();
    			      Statement stmt3 = con3.createStatement();
    			      ResultSet rs3 = stmt3.executeQuery(sql3);
    			      while(rs3.next()){
    			      
    			         double sum = rs3.getDouble("yle");
    			         if(sum !=0){
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
    	        	   paym = paym.replaceAll("P"," ");
    	               paym = paym.replaceAll("P�EVA"," ");
    	               paym = paym.replaceAll("P�EVA"," ");
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
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      
     try {
   	  String cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Oskar LT$Item Ledger Entry] l " +
          " INNER JOIN [Oskar LT$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('100','101','102','103') and t.[Blocked]=0 " + 
          " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]";
  

      	 Connection con = getNewConnection2();
  	        Statement stmt = con.createStatement();
  	        ResultSet rs = stmt.executeQuery(cmd);
  	        while(rs.next()){
  	             String toode = null;
  	             double seis = 0;;
  	              	toode =rs.getString("kood");
  	              	seis = rs.getDouble("kogus");
  	              	ladutr.put(toode, seis + "");
  	        }

  	      rs.close();
  	    stmt.close();
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }

   TreeMap  laduTag = new TreeMap();
   
   try {
 	  String cmd = " SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Oskar LT$Item Ledger Entry] l " +
        " INNER JOIN [Oskar LT$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('107') and t.[Blocked]=0 " + 
        " GROUP BY l.[Location Code],l.[Item No_] having sum(l.[Quantity]) != 0  ORDER BY l.[Item No_]";


    	 Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	             String toode = null;
	             double seis = 0;;
	              	toode =rs.getString("kood");
	              	seis = rs.getDouble("kogus");
	              	laduTag.put(toode, seis + "");
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
     	String cmd = "  SELECT t.[No_] AS toode,t.[Search Description] AS nimi, t.[Unit Price] AS myygihind,t.[Product Group Code] AS grupp ,t.[Item Category Code] AS grupp2  " +
	" ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount, " +
	"		 '' AS type, [Qty_ per Unit of Measure] AS kastis,  '' AS kaal, [VAT Prod_ Posting Group] AS vat "+  
	"		 ,1 as goes " +
	"		  FROM [Oskar LT$Item] t " +
	"		  LEFT JOIN  [Oskar LT$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KAST' )  " +
	"		  left join  [Oskar LT$Sales Price] sp on sp.[Item No_]=t.[No_] and sp.[Sales Type] =2 and sp.[Starting Date] < getdate()+5  and  " +
	"		   (sp.[Ending Date] > getdate()+1 or year (sp.[Ending Date]) = 1753) " +
	"		   left join [Oskar LT$Item Cross Reference] ref on ref.[Item No_]=t.[No_] and [Cross-Reference Type]=3  " +
	"		  WHERE t.[Blocked]=0    and t.[Unit Price] > 0  ORDER BY t.[No_]"; 
     	
      System.out.println(cmd);
      
      this.getProdGroups();
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = rs.getString("nimi").trim();
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }
                String grupp = Converter.enCodeEst(rs.getString("grupp"));
                
                //asendame brandiga kui tootel pole gruppi 
              if(grupp != null && grupp.trim().length()>0 ){
               prod.setGroup(grupp.trim());  
              }
          
              String grupp2 = Converter.enCodeEst(rs.getString("grupp2"));
              
              //asendame brandiga kui tootel pole gruppi 
            if(grupp2 != null && grupp2.trim().length()>0 ){
             prod.setGroupSecond(grupp2.trim());  
            }
              
                prod.setUnit(rs.getString("unit"));
                if(laduTag.get(id) != null ){
              	  name = name + " Aeg:" + (String)laduTag.get(id);
                }
                
                name =   Converter.firstToUpperCase(name);

              String ean = (rs.getString("ean"));
                 if (ean != null &&ean.trim().length()>3){
                   prod.setEAN(ean.trim());
               //    name = name + " " + ean.substring(ean.trim().length()-4);
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



                 	 String vat = rs.getString("vat");
                 	 double vati = 0;
                          if(vat != null && vat.trim().length()>0){
                            if(vat.equals("PVM21")){
                            	prod.setVat("21");
                            }else if (vat.equals("GRUPE2")){
                            	prod.setVat("5");
                            } else if (vat.equals("KM20")){
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
                 	
                 if(goes ){
                	 String ee = "";
                	 if (ean != null &&ean.trim().length()>3){
                         ee = " " + ean.substring(ean.trim().length()-4);
                       }
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
    		" FROM [Oskar LT$Sales Shipment Line] r " +
    "  INNER JOIN [Oskar LT$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Oskar LT$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-180 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, " +
          		"d.[Posting Date] AS lastdate FROM [Oskar LT$Sales Invoice Line] r " +
		  " INNER JOIN [Oskar LT$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
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
     return "NaviOskar";
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
 	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Oskar LT$Cust_ Ledger Entry] " +
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
