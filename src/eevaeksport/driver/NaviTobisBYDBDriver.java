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

public class NaviTobisBYDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviTobisBYDBDriver(EksportPanel parent) {
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
  	   
  	 String    sql = "  SELECT [Sales Code] AS custid,'' AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
     "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from" +
     "   [Amazis 4_0$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or" +
      "           year ([Ending Date]) = 1753)  and [Sales Type] = 0    " +
                   "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
      " [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from " +
      " [Amazis 4_0$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or " +
               "  year ([Ending Date]) = 1753) and [Sales Type] = 1 " +   
                   "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
     "   0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                "  from [Amazis 4_0$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0   " +     
            "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
       " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                "  from [Amazis 4_0$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or  " +
               "   year ([Ending Date]) = 1753) " +
               " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
              " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
              "    from [Amazis 4_0$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1        " +
             " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
            " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
             "     from [Amazis 4_0$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or " + 
              "    year ([Ending Date]) = 1753) " ; 
		


  	 

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

             if (curr != null && curr.length() > 0) {
                 mline.setCurrency(curr.trim());
               }
             if (custID != null && custID.length() > 0) {
               mline.setCustID(custID);
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
               mline.setProdGroup(prodGroup.trim());
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
    			  
    	   String cmd = "   SELECT l.[Item No_] AS kood, (l.[Quantity]) AS kogus, l.[Location Code] as ladu"
    	   		+ " from _MERISOFT$Amazis_Stock as l  where l.[Location Code] = 'MINSK'";
   

    		   	  

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

String sql = "   SELECT l.[Item No_] AS kood, (l.[Quantity]) AS stock, l.[Location Code] as ladu"
		+ " from _MERISOFT$Amazis_Stock as l";

	           if (isDebug()) System.out.println(sql);
	           Connection cc = getNewConnection2();
	              Statement stm = cc.createStatement();
	              ResultSet rst = stm.executeQuery(sql);

	                while (rst.next()) {
	                  String toode = rst.getString("kood").trim();
	                  int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
	                  String ladu  =  rst.getString("ladu").trim();
	                  if(seis > 0){
	                	  dealerProds.add(ladu, toode,seis+""," "," " );  
	                  }
	                 
	              }
	              rst.close();
	              stm.close();
	              cc.close();

	      } catch (Exception e) {
	          getParent().showError(e);
	      }

	       return dealerProds;
	     }


   
           public String getDealerCustomerCSV(String agent){
            return null;
            }

           public String  getCustProdDiscounts(Agent agent){
            return null;
            }
           

           public TreeMap getDealers() {

               TreeMap dealers = new TreeMap();
               try {

             	  String cmd = "  SELECT l.[Location Code] as ladu from _MERISOFT$Amazis_Stock as l group by l.[Location Code]";

                    if (isDebug()) System.out.println(cmd);
                    Connection con = getNewConnection2();
                       Statement stm = con.createStatement();
                       ResultSet rst = stm.executeQuery(cmd);
                         while (rst.next()) {
                           String kood = rst.getString("ladu").trim();
                            Dealer dealer = new Dealer(kood);
                            dealer.setName(kood);
                            dealers.put(dealer.getID(), dealer);
                           }
                       rst.close();
                       stm.close();
                       con.close();

               } catch (Exception e) {
                   e.printStackTrace();
               }

               
               System.out.println("Dealers size :" + dealers.size());
               
         	  return dealers;
         	    }
           
           

           

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
    // setTreeGroup(true);
     String cmd = "  select dv.Code  as kood, dv.Name  as nimi from [Amazis 4_0$Dimension Value] "
     		+ "dv where dv.[Dimension Code]='PROJECT'";
     System.out.println("Asike 1 " +cmd);
     try {
     	 Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	            String groupID = rs.getString("kood");
 	          	                String name = rs.getString("nimi") ;
 	               
 	                   tm.put(cleardots(groupID),Converter.enCodeEst(groupID + " " +name));
 	           
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

        String cmd = "  select [Amazis 4_0$Salesperson_Purchaser].code as kood , [Amazis 4_0$Salesperson_Purchaser].Name as nimi ," +
        " '' AS objekt FROM [Amazis 4_0$Salesperson_Purchaser] " +
        " WHERE [Amazis 4_0$Salesperson_Purchaser].Merisoft=1 ";
        
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
	        Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
	          Agent agent = new Agent(rs.getString("kood").trim());
	          String name = rs.getString("nimi").trim();
	          agent.setObject(rs.getString("objekt").trim());
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

	        Agent aa = new Agent("HOUSE1");
	        aa.setName("office1");
	        agents.put("office1", aa);
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

    	      String cmd = 
                             "select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa, " + 
    	                      "       DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer ,a.[Posting Date] as kuup FROM [Amazis 4_0$Cust_ Ledger Entry] a inner join " +
    	                       "      [Amazis 4_0$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_]  " +
    	                        "     where   " +
    							"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Open]= 1 and DATEDIFF(dd,a.[Due Date],GETDATE()) < 130   " +
    	                         "    group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_], " + 
    	                          "   a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_],a.[Posting Date] having sum(b.[Amount])<> 0 order by  a.[Due Date] ";
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
    	        debt.setSum(rs1.getDouble("summa"));
    	        int dueint = rs1.getInt("due");
    	        debt.setDate(rs1.getDate("kuup"));
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


    		   String sql =  "  SELECT k.[Name] AS nimi,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address]+' '+k.[City] AS aadress , a.[Address]+' '+a.[City] as addr2 " +
       	   	"	,k.[Salesperson Code] AS agent ,  Blocked AS blokk, " + 
       		"  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type, " +
       		 "  0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum ,a.Contact +' ' + a.[Phone No_] as kontakt  ,[Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 "
       		 + ", a.[Address] as restaddr ,a.[City] as restcity , k.[Location Code] as dealer " +
       		" FROM [Amazis 4_0$Customer] k LEFT join [Amazis 4_0$Ship-to Address] a on  a.[Customer No_]=k.[No_] " +
       		"  WHERE k.[Salesperson Code] != ''  " ;

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
    	           if(alegr != null && alegr.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst(alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
       	        	if(customer.getGroup().trim().length() >0){
       	        	customer.setCustType(Converter.cleanLitLatEst(alegr.trim()));
       	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 	
       	        	}else{
       	        	 	customer.setGroup(Converter.cleanLitLatEst(alegr2.trim())); 
       	        	}
       	           }
    	           
    	           
    	           customer.restStreet = rs.getString("restaddr");
    	           customer.restCity = rs.getString("restcity");
    	           String ladu = rs.getString("dealer");
    	           if(ladu != null && ladu.trim().length() > 0){
    	        	   customer.setDealer(new Dealer(ladu.trim()));
    	           }
    	           
    	          // customer.setPayer(id);
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
	  HashMap   laoseis = new HashMap();
      
      try {
   	  	  
   	String cmd = "   SELECT l.[Item No_] AS kood, (l.[Quantity]) AS kogus, l.[Location Code] as ladu"
	   		+ " from _MERISOFT$Amazis_Stock as l  where l.[Location Code] = 'MINSK'";
   	  
   	  

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
  	      //con.close();

  	    System.out.println(cmd);
  	    
  	    
  
  	String sql = " SELECT l.[Item No_] AS kood, max(case when l.[Location Code] ='MINSK' then (l.[Quantity])else 0 end )  AS mseis, " +
" max(case when l.[Location Code] ='BREST' then (l.[Quantity])else 0 end )  AS bseis, " +
" max(case when l.[Location Code] ='SKLAD' then (l.[Quantity])else 0 end )  AS sseis " +
"	   	 from _MERISOFT$Amazis_Stock as l group by l.[Item No_] ";
  	
  	stmt = con.createStatement();
  	  rs = stmt.executeQuery(sql);

  	while (rs.next()) {
  	String item = rs.getString("kood").trim();
  	Toode tt = new Toode(item);
  	tt.setKogus(rs.getDouble("mseis"));
  	tt.setKastis(rs.getDouble("bseis"));	
  	tt.setDonecur(rs.getDouble("sseis"));	

  	

  	laoseis.put(item, tt );
  	}
  	rs.close();
  	stmt.close();
    con.close();
  	    
  	    
  	    
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     
     
     
  
     try {
     
    	 String lubaSQl = " SELECT [No_] as toode from [Amazis 4_0$Item] where  [Show in mob_ device] = 1";
    	 HashSet luba = new HashSet();
    	 
    	 Connection con = getNewConnection2();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(lubaSQl);
         while(rs.next()){
        	  String id = rs.getString("toode").trim();
        	  luba.add(id);
         }
         System.out.println("luba suurus " + luba.size());
           rs.close();
           stmt.close();
           con.close();
    	 String cmd ="   SELECT   t.[No_] AS toode,  t.[Description] + t.[Description 2] " +
    "	 as nimi,  t.[Unit Price] AS myygihind,  '' AS grupp2, t.[Global Dimension 2 Code] AS grupp  , " +
    "	 t.[Base Unit of Measure] AS unit, [No_ 2] AS ean, '' AS status , 0 AS maxdiscount,  '' aS type, [Units per Parcel] AS kastis, " +
    "	 [Item Disc_ Group] as alegr ,  '' AS kaal,[VAT Prod_ Posting Group] AS vat , [Units per Pallet] as pallet  ," + 
    "	 [Unit List Price] as listprice   FROM [Amazis 4_0$Item] t     WHERE t.[Blocked]=0  " +
    "	  and t.[Show in mob_ device]=1  ORDER BY t.[No_] " ;

    	 
    	 
    	 
    	 System.out.println(cmd);
     	 con = getNewConnection2();
        stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
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
        			prod.setGroup(grupp.trim());	
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
                 


          	 String vat = rs.getString("vat");
          	 int vati = 21;
                   if(vat != null && vat.trim().length()>0){
                	 	prod.setVat(vat.trim());
                	 	try{
                	 		vati = Integer.parseInt(vat.trim());	
                	 	}catch(Exception e){
                	 		
                	 	}
                	 
                   }
               
                   double pallet = rs.getDouble("pallet");
                   if(pallet >0){
                	//   prod.setOnPallet(pallet);
                   }
                   
                   double listprice = rs.getDouble("listprice");
                   if(listprice >0){
                	   prod.setPriceSecond(listprice * ((100 + vati)/100));
                   }
                   
                  /* Date newprod = rs.getDate("newprod");
                   if(newprod.after(new Date())){
                	   prod.setStatusNew("1");
                   }*/
                   
                   
                   if(laoseis.get(id) != null){
                	   Toode tt = (Toode)laoseis.get(id);
                	   double mseis = tt.getKogus();
                	   double bseis = tt.getKastis();
                	   double sseis = tt.getDonecur();
                	   
                	   if(mseis > 0){
                		  // prod.setStock((int)mseis);
                		   prod.setPartii( "| Minsk: " + (int)mseis);
                	   }
                	   
                	   if(bseis > 0){
                		  // prod.setStock((int)bseis);
                		   prod.setPartii((prod.getPartii() + "| Brest: " + (int)bseis).trim());
                	   }
                	   if(sseis >0 ){
                		 prod.setPartii((prod.getPartii() + "| Sklad: " + (int)sseis).trim());
                	   }
                   }
                   
                   
                   if(luba.contains(prod.getID()) && !name.equals("")){
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
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount , t.[Search Description]" +
    		" FROM [Amazis 4_0$Sales Shipment Line] r " +
    "  INNER JOIN [Amazis 4_0$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Amazis 4_0$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-500 " +
    "  ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
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

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [Amazis 4_0$Sales Invoice Line] r " +
		  " INNER JOIN [Amazis 4_0$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
		    Connection con2 = getNewConnection2();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = conv(rs2.getString("klient").trim());
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
  stmt2.close();
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
     return "NaviTobisBY";
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
   private String url2 = "jdbc:odbc:NAV40";
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
  	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Amazis 4_0$Cust_ Ledger Entry] " +
  	 			"where Month(getdate())=Month([Posting Date]) and " +
             "Year(getdate())=Year([Posting Date])  group by [Salesperson Code]";
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
