package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

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
 * <p>Copyright: Copyright (c) 2002</p>MOFF Preke
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviKALDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviKALDBDriver(EksportPanel parent) {
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
    rcompanyx = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
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
	     
   	String  cmd2 = " SELECT [No_] as toode from [Amazis 4_0$Item] where  [Show in mob_ device] = 1";
    
   	
     	String picts = parent.getProps().getProperty("pict.dir");
 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
 	 

   	try {
 	   Connection con = getNewConnection2();
	
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
//        	 Lõppes kustutamine 
        	 
          System.out.println("Piltide kopeerimine ");	
          //Failide kopeerimine  
            String[] gg = new File(picts).list();
         	  //System.out.println("gg " + gg[0] + " " + gg.length);
            
             for(int i = 0;gg.length>i;i++){          	
             	File in = new File ( picts + gg[i]); 
             	if(in.isFile()){            	
             	File inpda = new File ( pictspda + toUpper(gg[i])); 
                   	copyFile(in,inpda);
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
                         	copyFile(in,inpda);
                         	 System.out.println("alamkataloog  " + ff[i] );
                   	 }
                   }
                   
            	 }
             }
             
             File fff =  new File (pictspda +  "list.txt");
       	  copyFile(f,fff);  	
       	  File fff0 =  new File (pictspda +  "list0.txt");
       	  copyFile(f0,fff0);  	
   	} catch (Exception e) {
   	    getParent().showError(e);
   	}
   
}
 
   private String toUpper(String asi ){
	   
	   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.indexOf("."));
	   System.out.println(asi);
	   return asi ;
   }
   
 public void copyFile(File in, File out) throws Exception {
     FileInputStream fis  = new FileInputStream(in);
     FileOutputStream fos = new FileOutputStream(out);
     byte[] buf = new byte[1024];
     int i = 0;
     while((i=fis.read(buf))!=-1) {
       fos.write(buf, 0, i);
       }
     fis.close();
     fos.close();
     }


   public Vector getPriceMatrixData(){
  	   
  	 String    sql = "    SELECT '' AS custid,[Customer_Price_Group_Code] AS clgroup,[Item_No] AS prodid,'' AS prodgroup, " +
     " [Unit Price]/1.18 AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from " +
      "  [Price Matrix]  where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or " +
       "          year ([Ending Date]) = 1753)   and [Unit Price] > 0 " +
			"	 union  select '' AS custid,[CustDiscGroup] AS clgroup,'' AS prodid,[ItemDiscGroup] AS prodgroup, " +
    " 0 AS price ,Discount as disc, [EndingDate] as paev, [StartingDate] as startday ,'' as val from  [Discounts] " +
	" where [StartingDate] < getdate()-5  and ([EndingDate] > getdate()+1 or " +
     "            year ([EndingDate]) = 1753)   and [Discount] > 0 " ;
     


  	 

       Vector result = new Vector();
         try {
          
      	   Connection con = getNewConnection2();
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(sql);
           while (rs.next()) {

        	   boolean goes = true;
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

             if (curr != null && curr.length() > 0) {
                 mline.setCurrency(curr.trim());
               }
             if (custID != null && custID.length() > 0) {
               mline.setCustID(custID);
             }
             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
               if( clGroup.contains("BAZOVA")){
            	  goes = false; 
               }
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
               mline.setProdGroup(prodGroup.trim());
             }

             if (price != 0) {
               mline.setPrice(price);

             }
             if (disc != 0) {
                 mline.setDiscount(disc);

               }
             if((mline.getPrice() > 0 || mline.getDiscount() > 0 ) && goes){
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
		// TODO Auto-generated method stubfg
		return null;
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
    ManagerData md = new ManagerData();
    return md;

  }


   
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           return lines;
   }



   public DealerProduct DealerProductsList() {
     return null;
   }


       public String getSaveStockInfo(){
    			  
    	  
    	   	String cmd = " SELECT l.[ItemNo] AS kood, (l.[QtyInStock]) AS kogus, l.[Location] as ladu " +
    	   	   	 " from  [ProdStock] as l  where l.[Location] ='TECHNOLIT'";
    	 	   	  
    		   	  

    		          if (isDebug()) System.out.println(cmd);
    		          StringBuffer products = new StringBuffer();
    		          try {


    		            	 Connection con = getNewConnection2();
    		        	        Statement stmt = con.createStatement();
    		        	        ResultSet rs = stmt.executeQuery(cmd);

    		            while (rs.next()) {

    		              String kood = rs.getString("kood").trim();
    		              double seis = rs.getDouble("kogus");
    		              String ladu = rs.getString("ladu").trim();
    		              if(seis > 0){
    		                  products.append(kood +  ";");
    		                  products.append(" ;");
    		                  products.append(getParent().getNF().format(seis) + ";" + ladu  +  "\n"); // + dealer +  "\n");
    		              }
    		              }
    		              rs.close();
    		              stmt.close();
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

   
   public DealerProduct getDealerProductsList() {
		 
	   
	      DealerProduct dealerProds = new DealerProduct();
	      try {

	    	  String sql = " SELECT [ItemNo] as kood  ,[QtyInStock] as kogus  ,[Location ] as ladu " +
           " FROM ProdStock";
	           if (isDebug()) System.out.println(sql);
	          Connection con = getNewConnection2();
	         
	              Statement stm = con.createStatement();
	              ResultSet rst = stm.executeQuery(sql);

	                while (rst.next()) {
	                  String toode = rst.getString("kood").trim();
	                  int seis = (int)Double.parseDouble(rst.getString("kogus").replace(',','.'));
	                  String ladu  =  rst.getString("ladu").trim();
	                  if(seis != 0){
	                	  dealerProds.add(ladu, toode,seis+""," "," " );
	                  }
	              }
	              rst.close();
	              stm.close();
	              con.close();

	      } catch (Exception e) {
	          getParent().showError(e);
	      }
	      
	      System.out.println("DealerProds size " + dealerProds.getDealers().size());
	      
	       return dealerProds;
	     }

	  


   
           public String getDealerCustomerCSV(String agent){
            return null;
            }

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }
           

           public TreeMap getDealers() {
        	   
        		   TreeMap	dealers = new TreeMap();
        		
        		 	String cmd = "SELECT DISTINCT Location FROM ProdStock";
   
        		 	if (isDebug()) System.out.println(cmd);
        	    		          try {
        	    		            	    Connection con = getNewConnection2();
        	    		        	        Statement stmt = con.createStatement();
        	    		        	        ResultSet rs = stmt.executeQuery(cmd);

        	    		            while (rs.next()) {
        	    		               String kood = rs.getString("Location").trim();
        	    		         	   Dealer dealer = new Dealer(kood);
        	    	        		   dealer.setName(kood);
        	    	        		   dealers.put(dealer.getID(), dealer);
        	    		            }

        	    		            rs.close();
        	    		            stmt.close();
        	    		            con.close();
        	    		        	  
        	    		          }catch(Exception e){
        	    		        	  e.printStackTrace();
        	    		          }

        	
           			 return dealers;
        		    }

           
           
           
TreeMap groups = new TreeMap();
           

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
    // setTreeGroup(true);
     groups = new TreeMap();
     String cmd = " select [Product_Group_Code] as kood, [Product Group Name]  as nimi from [Product Group List]"
     		+ " where [Product_Group_Code]  != '' ";
    
     System.out.println("Asike 1 " +cmd);
     try {
     	 Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	            String groupID = rs.getString("kood");
 	          	                String name = rs.getString("nimi") ;
 	                   tm.put(Converter.enCodeEst(name),Converter.enCodeEst(name));
 	                  groups.put(cleardots(groupID),Converter.enCodeEst(name));
 	        	           
 	        }
 	       // System.out.println("Teine" + tm.toString());
 	        rs.close();
 	        stmt.close();
 	        con.close();
      } catch (Exception e){
        e.printStackTrace();
      }         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();

  
        return tm;
  }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}

	   return idx;
   }

   public TreeMap getAgents(){

        String cmd = " select * from [Sales Agent list]";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("Code").trim());
	          String name = rs.getString("Name").trim();
	          agent.setIsPilot(true);
	          agent.setName(name);

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
    	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
	          
	          agents.put(name, agent);
	          agents1.put(agent.getID(), agent);
	        }

	        rs.close();
            stmt.close();
	        con.close();
	        
	        
	/*  cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa " +
	  " FROM [Premia Tallinna Külmhoone AS$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 " ;
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
*/
            
	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }
private String conv (String ss){
	return Converter.enCodeEst(ss);
}

       public TreeMap getCustomers(Agent agent){

    	    try {

    	      String cmd = "select  a.[Sell_To_Customer_No] AS klient, a.[Bill_To_Customer_No] AS maksja,a.[Document_No_] AS number, a.[Due_Date] AS kuupaev,"
    	      		+ " DATEDIFF(dd,a.[Due_Date],GETDATE()) AS due , a.[Due_Date] as kuup, " + 
 " sum (a.[Remaining Amount LCY]) AS summa from [Open Customer Debts] a group by a.[Sell_To_Customer_No], a.[Bill_To_Customer_No],a.[Document_No_] , " +
 " a. [Due_Date] ,a.[Due_Date]  having   sum(a.[Remaining Amount LCY])<> 0 " ;
    	      
    	      System.out.println(cmd);
    	       this.debtList = new Vector();
    	       con = getNewConnection2();
    	       Statement stmt1 = con.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient");
    	        debt.setCustID(conv(custID));
    	        String maksja = rs1.getString("maksja");
    	        debt.setCustID(conv(maksja));
    	        String doknr = rs1.getString("number");
    	        if(doknr.trim().length()>6){
    	        	doknr = doknr.substring(2,doknr.length());
    	        }
    	        debt.setDoknr(doknr);
    	        debt.setDuedate(rs1.getDate("kuupaev"));
    	        int dueint = rs1.getInt("due");
    	        debt.setDate(rs1.getDate("kuup"));
    	        debt.setSum(rs1.getDouble("summa"));
    	        debt.setDue(dueint + "");
    	        int old = 0;
    	       if(dueint >= 0){
    	    	   if( overdebts.get(maksja) == null){
      	        	 overdebts.put(maksja,debt.getSum() + "");
      	        }else{
      	        	double ss = Double.parseDouble((String)overdebts.get(maksja));
      	        	  ss= ss+ debt.getSum();
      	        	 overdebts.put(maksja,ss + "");
      	        }  
    	       }
    	       
    	        
    	        if( usedcredit.get(maksja) == null){
    	        	usedcredit.put(maksja,debt.getSum() + "");
   	              }else{
   	        	  double ss = Double.parseDouble((String)usedcredit.get(maksja));
   	        	  ss= ss+ debt.getSum();
   	        	   usedcredit.put(maksja,ss + "");
   	             }
    	        
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
    	       con.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	/// KLIENDI ANDMETE LEIDMINE

    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();


    		   String sql =  "  select k.[Sell-to Customer Name] AS nimi,k.[Sell_To_Customer_No] AS kood,k.[Bill-to Customer No] AS payer, " +
" k.[Address] AS aadress , k.[Address] as addr2 ,k.[Salesperson Code] AS agent ,  Blocked AS blokk, [Payment Terms] AS payment, " +
" '' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type,0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum, " +
" k.[Contact_Phone No] as kontakt , [Customer Price Group Code] as alegr ,k.[Customer Discount Group Code] as alegr2 " +
"  from [Customer List] k WHERE k.[Salesperson Code] != ''" ;

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name = rs.getString("nimi").trim();
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer(conv(id));
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(id + " " + name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer(payer.trim());
    	             if(overdebts.get(payer.trim()) != null){
    	            	 customer.setDebt(Double.parseDouble((String)overdebts.get(payer.trim())));
    	              }
    	             
    	             if(usedcredit.get(payer.trim()) != null){
    	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get(payer.trim())));
    	                 }
    	             }
    	           	
    	           	if(overdebts.get(id) != null){
   	            	    customer.setDebt(Double.parseDouble((String)overdebts.get(id)));
   	                 }
    	           	
    	           	if(usedcredit.get(id) != null){
   	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get(id)));
   	                 }
    	           	
    	            String aadress = rs.getString("aadress");
   	             String aadr2 = rs.getString("addr2");
   	             if( aadr2 != null && aadr2.trim().length()>0){
     	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr2),"Belorus"));
     	               name = name + " " + aadr2;
     	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadr2));
     	             }else if( aadress != null && aadress.trim().length()>0){
   	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Belorus"));
   	               name = name + " " + aadress;
   	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadress));
   	             }
    	         	String ag = rs.getString("agent");
    	             customer.addAgent(new Agent(ag));
    	             
    	             if (ag != null && ag.equals("MITU")){
    	 			          Enumeration enums = getParent().getAgentsHash().elements();
    	 			          while (enums.hasMoreElements()) {
    	 			            Agent a = (Agent) enums.nextElement();
    	 			            customer.addAgent(a);
    	 			          }

    	 			}

    	             String bl = rs.getString("blokk");
    	             if(bl.equals("1")){
    	               customer.setSalesBlock(true);
    	             }
    	             if(bl.equals("3")){
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
    	           	
    	           	
    	           	/*
    	       	String debti = null;
    	        String used = "0";
    	           	 debti = rs.getString("krediit");


    	      		if (overdebts.containsKey(id)){
    	           		debti = ((Debt)overdebts.get(id)).getDoknr();
    	                          used = ((Debt)overdebts.get(id)).getDue() ;
    	           		}
    	      		if (overdebts.containsKey(payer)){
    	               	debti = ((Debt)overdebts.get(payer)).getDoknr();
    	                          used = ((Debt)overdebts.get(payer)).getDue() ;
    	               		}


    	      		String visit = rs.getString("visit");
    	             if(visit != null && visit.trim().length()>0){
    	                customer.setVisit(visit.trim());
    	             }

    	            String type = rs.getString("type");
    	            if(type != null && type.trim().length()>0){
    	            customer.setCustType(type.trim());
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
    	           	customer.setGroup(grupp.trim());
    	           }
    	*/


    	       
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("D"," ");
    	               paym = paym.replaceAll("PÄEVA"," ");
    	               paym = paym.replaceAll("PäEVA"," ");
    	               paym = paym.replaceAll("SR","-1").trim();
    	        	customer.setPaymTerm(paym);
    	             }

    	           
    	           
    	          //
    	           String cont = rs.getString("kontakt");
    	           if(cont != null && cont.trim().length()>0){
    	               customer.setPersonalData(Converter.enCodeEst(cont.trim().replaceAll("\n", " ").replaceAll(System.getProperty("line.separator")," ").replaceAll("\r", " ")));   
    	           }
    	           
    	           String alegr = rs.getString("alegr");
    	           if(alegr != null && alegr.trim().length()>0 && !alegr.contains("BAZOV")){
    	        	   
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim()));   
    	           }
    	          
    	          // customer.restStreet = rs.getString("restaddr");
    	          // customer.restCity = rs.getString("restcity");
    	           
    	          
    	           tm.put(Converter.cleanLitLatEst(id) + customer.getName(), customer);
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
   	  	  
    	  getProdGroups();
    	  
   	String cmd = " SELECT l.[ItemNo] AS kood, (l.[QtyInStock]) AS kogus, l.[Location] as ladu " +
	   	 " from  [ProdStock] as l  where l.[Location] ='TECHNOLIT'";
   	  
   	  

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
     
     Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     
     
     
  
     try {

    	 String cmd = "select  t.[ItemNo] AS toode,  t.[Description]   as nimi,  t.[Unit Price]/1.18 AS myygihind, ''  AS grupp2, t.[Product Group Code (Global Dimension 1 Code)] AS grupp , " +
  " t.[Base Unit of Measure] AS unit,  ean, '' AS status , 0 AS maxdiscount,  '' aS type, [Unit per Parcel] AS kastis, " +
   " [Item Discount Group] as alegr ,  '' AS kaal,[VAT Group] AS vat  from [Item List] t " ;

    	 Connection con = getNewConnection2();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(cmd);
         
    	 
    	 System.out.println(cmd);
     	
        while(rs.next()){
            String id = rs.getString("toode").trim();
            Product prod = new Product(id);
         	String name = Converter.enCodeEst(id + " " + rs.getString("nimi").trim());
         	
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	//  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
            	//  prod.setStock(0);
              }
             //   String grupp2 = cleardots(rs.getString("grupp2"));          
          	
      		//if(grupp2 != null && grupp2.trim().length() >0){
      			//prod.setGroupSecond(grupp2.trim());	
      		//}
              
              String grupp = rs.getString("grupp");
        		if(grupp != null && grupp.trim().length() >0){
        			if(groups.get(grupp.trim()) != null){
        				prod.setGroup(groups.get(grupp.trim())+"");
        			}
        				
        		}
        	
        		
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null && ean.trim().length()>0){
                   prod.setEAN(ean);
                   if(ean.length() > 4){
                	   name = name + " " + ean.substring(ean.length()-4);
                   }
                 }
                    String xxx = rs.getString("status");
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
              

             	String discou = rs.getString("maxdiscount");
             	if(discou != null && discou.trim().length()>0){
                prod.setMaxDiscount(Double.parseDouble(discou));
                }

                double	cases = rs.getDouble("kastis");
                	if(cases > 0){
                       prod.setInCases(cases);
                	}
                
                	String alegrupp = rs.getString("alegr");
                	if(alegrupp != null && alegrupp.trim().length() > 0){
                		prod.setAlegrupp(alegrupp);	
                	}

                	 	prod.setVat("18");
                
                   if(ladutr.get(id) != null){
                	//   prod.(Double.parseDouble((String)ladutr.get(id)));
                   }
                   
                   if( !name.equals("")){
                	   prod.setName(Converter.enCodeEst(name));
                       tm.put(name, prod);   
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
   return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  r.[Sell_to_Customer_No] AS klient, r.[ItemNo] AS toode, r.[Quantity] AS kogus, r.[Sales_Date] AS lastdate, r.[Sales Price] as lastprice " +
    		"  FROM [Customer Product list] r ";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = conv(rs.getString("klient").trim());

                 String prodID = rs.getString("toode").trim();;

                 double amount = 0;
                 String lastdate = " ";
                 amount = rs.getDouble("kogus");
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
     return "NaviKAL";
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
   private TreeMap usedcredit = new TreeMap();
   private Vector debtList = new Vector();
   private String url2 = "jdbc:odbc:s1";
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
  	 	String cmd = " select [Salesperson_Code] as agent ,sum([Sales Amount (LCY)]) as summa from [Sales Data] " +
  	 			" where Month(getdate())=Month([Posting_Date]) and " +
                " Year(getdate())=Year([Posting_Date])  and [Salesperson_Code] != ''  group by [Salesperson_Code]";
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
	        stmt.close();
	        con.close();
      } catch (Exception e){
        e.printStackTrace();
     }

  }
}
