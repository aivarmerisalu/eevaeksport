package eevaeksport.driver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
/*
 * pop3 server mail.saarioinen.ee

mail_saarioinen_pda:D945DDEA
mail_saarioinen_aruanne:3792796A


*/
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviSaarioinenDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,FixLoader {

  public NaviSaarioinenDBDriver(EksportPanel parent) {
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

  private TreeMap eanprods = null;
  
  @Override
  public void FixLoader() {
  	
  eanprods =  Converter.loadMore();
  	  
  	  if(eanprods == null) {
  		  eanprods = new TreeMap();
  	  }
  	this.getProducts(null);
  	TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
  	System.out.println("Contracts " + contracts.toString());
  	Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
  	System.exit(0);
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


   public Vector getPriceMatrixData() {
   	return null;
   }

   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
 
   public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
	 
	  }

   public void insertDefective(DefectiveWrapper wr){}
   
   
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	 
	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	   
	     }


	 public String getProductSortingData(){
	   	return null;
	   }



   public ManagerData getManagerData() throws SQLException{

    return null;

  }


   public void insertDistribution(DistEvalListWrapper wr){}


   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();

           return lines;
   }



   public DealerProduct DealerProductsList() {

 
     return null;
   }


   public String getSaveStockInfo(){

	  	  
		  String  cmd = 
"   SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Saarioinen Eesti OÜ$Item Ledger Entry] l \r\n" + 
"           INNER JOIN [Saarioinen Eesti OÜ$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('RAPLA') and t.[Blocked]=0   \r\n" + 
"		   GROUP BY l.[Location Code], l.[Item No_]" ;


		  
		  
		         if (isDebug()) System.out.println(cmd);
		         StringBuffer products = new StringBuffer();
		         try {
		        	 Connection con = getNewConnection();

		        	 


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
		            
		                 products.append(kood +  ";");
		                 products.append(" ;");
		                 products.append(getParent().getNF().format(seis) + "; \n"); 
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
     String cmd = "  SELECT  [Product Group Code] AS kood ,gr.[Description] AS nimi FROM [Saarioinen Eesti OÜ$Item] t " + 
	     		" INNER JOIN  [Saarioinen Eesti OÜ$Product Group]  gr ON gr.code = [Product Group Code] " + 
	     		"      	     		 where t.[Item Category Code] ='OMATOOD'  and [Product Group Code]  != '' " + 
	     		"      	     		 group by [Product Group Code],gr.[Description] order by [Product Group Code]";
     	  try {
       	 Connection con = getNewConnection();
       	System.out.println(cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood");
                String name = rs.getString("nimi").trim() ;
   	             if(id != null && id.trim().length()>0) {
   	            	 name = name.replaceAll("HoReCa", "Ho");
   	            	 name = name.replaceAll("Mamma", "Ma");
   	             tm.put(id.trim(), name);
   	             }
   	        }

   	      rs.close();
   	      con.close();

        } catch (Exception e){
          e.printStackTrace();
        }

         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
   
        return tm;
  }


 

   public TreeMap getAgents(){

        String cmd = " SELECT [Code] AS kood,[Name] AS nimi,'' AS objekt FROM  " + 
        		"		  [Saarioinen Eesti OÜ$Salesperson_Purchaser] ";
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	         
	          agent.setName(name);

	          
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	    
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

    	   try{
    		   Converter.getManagersReplacements(agents1, rcompany, user, pass); 
    			
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	   

    	   
	    	  
	    try {

    	      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number,a.[Document No_] AS number2"
    	      		+ ", a.[Posting Date] AS kuupaev1 , a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
    	                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [Saarioinen Eesti OÜ$Cust_ Ledger Entry] a inner join " +
    	                            " [Saarioinen Eesti OÜ$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	                            " where " +
    								"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Posting Date] > getdate()-1000 AND a.[Open]= 1 " +
    	                            "  group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_],a.[Document No_], " +
    	                            " a.[Posting Date] ,a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	      Connection conv = getNewConnection();
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
    	"  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type,  " +
    	"	   0 AS extradays ,[Customer Price Group]  AS sort,'' AS debt  ,k.Contact +' ' + k.[Phone No_] as kontakt , 0 as sendall " +
    	" ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 , k.[Address] as street , k.[City]"
    	+ " ,k.[Post Code] as inde,  k.[E-Mail] as mail,'' as regno , 0 as minsum FROM [Saarioinen Eesti OÜ$Customer] k  " +
	  	"	  WHERE k.[Salesperson Code] != ''  AND Blocked != 3  " ;

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection();
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

    	           	
    	           	
    	             String aadress = rs.getString("aadress");
    	             if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
    	               name = name + " " + Converter.enCodeEst(aadress);
    	              customer.setName(Converter.enCodeEst(name));
    	             }
    	         	String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag.trim()));
    	       
    	             
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
     
     if(eanprods == null) {
    	 eanprods = new TreeMap();
     }
      TreeMap  ladutr = new TreeMap();
     try {
    	 String  cmd = 
    			 "   SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Saarioinen Eesti OÜ$Item Ledger Entry] l \r\n" + 
    			 "           INNER JOIN [Saarioinen Eesti OÜ$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('RAPLA') and t.[Blocked]=0   \r\n" + 
    			 "		   GROUP BY l.[Location Code], l.[Item No_]" ;

      	 Connection con = getNewConnection();
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
  	      con.close(); 

 
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     try {
    	  	String cmd = " 	SELECT t.[No_] AS toode,t.[Description] AS nimi, [Unit Price] AS myygihind,  " + 
    	  			"    t.[Product Group Code] AS grupp  ,t.[Base Unit of Measure] AS unit,[Cross-Reference No_] AS ean,'' AS status ,0 AS maxdiscount," + 
    	  			"    	 '' AS type, [Units per Parcel] AS kastis,  [Net Weight]   AS kaal, [VAT Prod_ Posting Group] AS vat,[Item Disc_ Group] as grd " + 
    	  			"     FROM [Saarioinen Eesti OÜ$Item] t   left join [Saarioinen Eesti OÜ$Item Cross Reference] ref on ref.[Item No_]=t.[No_] and ref.[Cross-Reference Type]=3 " + 
    	  			"      WHERE t.[Blocked]=0 and  t.[Inventory Posting Group] in ('OMATOOD','SAARI') and  t.[Item Category Code] != 'SOY'"
    	  			+ " and  t.[Product Group Code] not in ('PLRIMI','MTAID','MKOOD2','KEED')  ORDER BY t.[No_]"; 
    	  	     	
    	  	      System.out.println(cmd);
    	  	      
    	  	     	Connection con = getNewConnection();
    	  	        Statement stmt = con.createStatement();
    	  	        ResultSet rs = stmt.executeQuery(cmd);
    	  	        while(rs.next()){
    	  	            String id = rs.getString("toode").trim();
    	  	              	 
    	  	            
    	  	            Product prod = new Product(id);
    	  	         	String name = rs.getString("nimi").trim();
    	  	         	
    	  	         	name =   Converter.firstToUpperCase(name);
    	  	         	name = name + " " + id;
    	  	         	
    	  	             double price = 1 ;
    	  	           
    	  	            prod.setName(name);
    	  	             
    	  	             // System.out.println("nimi " + name);
    	  	             
    	  	                prod.setPrice(price);
    	  	              if(ladutr.get(id) != null ){
    	  	            	  prod.setStock(Double.parseDouble(ladutr.get(id)+""));
    	  	              }
    	  	             
    	  	              
    	  	                String grupp = Converter.cleanLitLatEst(rs.getString("grupp"));
    	  	                
    	  	              if(grupp != null && grupp.trim().length()>0 ){
    	  	                 prod.setGroup(grupp.trim());
    	  	              }
    	  	          
    	  	                prod.setUnit(rs.getString("unit"));
    	  	                
    	  	              
    	  	            
    	  	                String ean = rs.getString("ean");
    	  	            
    	  	                if (ean != null && ean.trim().length()>3){
    	  	                   prod.setEAN(ean.trim());
    	  	                   eanprods.put(ean.trim(), prod.getID());
    	  	                   
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
    	  	                       prod.setInUnit(kaal);
    	  	                  	}
    	  	                 	
    	  	                 

    	  	                 	 
    	  	                 	 
    	  	                 	 prod.setVat("20");
    	  	                           

    	  	                 	     String grd = rs.getString("grd");
    	  	                	      if(grd != null && grd.trim().length()>0){
    	  	                           	prod.setDiscgrupp(Converter.cleanLitLatEst(grd.trim()));
    	  	                          }
    	  	                	   /*   String maxdays = (rs.getString("maxdays"));
    	  	                	      
    	  	                	      if(maxdays != null && maxdays.trim().length()>0 && !maxdays.trim().equals("0") ){
    	  	       			        	prod.setPartii("MaxDays " + maxdays.trim());
    	  	       			        }*/
    	  	               
    	  	               
    	  	                	 tm.put( name, prod); 
        }


        
        rs.close();
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
  
   	return null;
  }


   public Vector getCustomerProductsList(String agentCode){
	     Vector vec = new Vector();
	     HashMap kliendid = new HashMap();
	     try {
	    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount\r\n" + 
	    		"	    		 FROM [Saarioinen Eesti OÜ$Sales Shipment Line] r \r\n" + 
	    		"	      INNER JOIN [Saarioinen Eesti OÜ$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Saarioinen Eesti OÜ$Item] t ON t.[No_]=r.[No_]  WHERE \r\n" + 
	    		"	      d.[Posting Date]> GETDATE()-90 and r.[Quantity] > 0 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
	    System.out.println(cmd);
	   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
	    Connection con = getNewConnection();
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

	          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [Saarioinen Eesti OÜ$Sales Invoice Line] r " +
			  " INNER JOIN [Saarioinen Eesti OÜ$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 and r.[Quantity] > 0 " +
			  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
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
     return "NaviSaarioinen";
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

   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:navi";

   private Connection con;
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


  /**
   * setBulkSales
   *
   * @param agents Map
   */
public void setBulkSales(Map agents) {

	 try {
	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Saarioinen Eesti OÜ$Cust_ Ledger Entry] " +
	 			"where Month(getdate())=Month([Posting Date]) and " +
           "Year(getdate())=Year([Posting Date]) " +
            " group by [Salesperson Code]";// and [Global Dimension 1 Code]='400'
	  Connection con = getNewConnection();
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
