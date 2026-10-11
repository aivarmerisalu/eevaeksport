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
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>MOFF Preke
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviTobisKALDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviTobisKALDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
  }

   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
	   makePictureCat();
		  System.exit(0);
		  return null;
   }

   private void makePictureCat(){
	     
	   	String  cmd2 = " SELECT [No_] as toode from TechnoLit$Item where  [Show_in_mobile] = 1";
	    
	   	
	     	String picts = parent.getProps().getProperty("pict.dir");
	 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	 	 

	   	try {
	 	   Connection con = getNewConnection2();
		
	   		Statement stmt = con.createStatement();
	   	    ResultSet	      rs = stmt.executeQuery(cmd2);
	   	    Vector pictures = new Vector();
	   	 while (rs.next()) {
	   	  String toode = rs.getString("toode").trim();  
	   	  
	   	  if(!pictures.contains(toode)){
	   		  pictures.add(toode); 
	   	  }
	   	
	   	 }
	   	 rs.close();
	   	stmt.close();
	   	con.close();
	   	 
	   	
	   	  /*  	      rs = stmt.executeQuery(cmd2);
	   	 
		 while (rs.next()) {
		   	  String toode = rs.getString("toode").trim();  
		   	  
		   	  if(!pictures.contains(toode)){
		   		  pictures.add(toode); 
		   	  }
		   	
		   	 }*/
	   		 
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
	        
	        Enumeration enume = pictures.elements();
	        
	     //   System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
	   	    while (enume.hasMoreElements()) {
	   	    	a++;
	   	    	c++;
	   	    	
	   	     String toode = (String) enume.nextElement();  
	         toode = toode.replace('/', '_');
	   	    	if(c == 100){
	   	    		  c = 0;
	   	    		//  System.out.println("Piltide leidmine "  + a);	
	   	    		  
	   	    	}
	   	     
	              
	                 File f = new File(picts + toode+".jpg");
	                 String len = "0";
	                 if(f.exists()){
	                   len =	f.length() + "";
	                 } else if(cats.size() > 0 ){
	                	 for(int w = 0 ;w < cats.size();w++){
	                		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
	                		
	                		if(f.exists()){
	                			//System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
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
	   	    //rs.close();
	   	    //stmt.close();
	   	    //con.close();
	   	
	   	    //oleasoleva kataloogi kustutamine
	   //	  System.out.println("Kataloogi kustutamine "  + a);	    
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
//	        	 Lõppes kustutamine 
	        	 
	      //    System.out.println("Piltide kopeerimine ");	
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
	                	// System.out.println("gg " + picts + cats.elementAt(h)+ "/" + ff[i]);
	                	 //System.out.println("ff " +  pictspda + toUpper(ff[i]));
	                	
	                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
	                   	if(in.isFile()){   
	                   		
	                   	File inpda = new File ( pictspda + toUpper(ff[i])); 
	                         	copyFile(in,inpda);
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
	   
	   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
	 //  System.out.println(asi);
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

		
  	 String    sql = "  SELECT '' AS custid, 'TL' +[Price Group Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
          "    [Unit Price] AS price ,0 as disc, '' as paev, [Starting Date] as startday ,[Currency code] as val from " +
          "    [TechnoLit$Item Price]  where [Starting Date] < getdate()-5  and [Price Group Code]<>'' and [Item No_]<>'' " + 
        " union select '' AS custid, 'TL' +[Customer Disc_ Group] AS clgroup,'' AS prodid,[Item Discount Group] AS prodgroup, " +
      " 0 AS price ,[Discount %] as disc , '' as paev, '' as startday ,'' as val " +
       "     from [Technolit$Cust__Item Discount] where [Customer Disc_ Group]<>'' and [Item Discount Group]<>'' " +
		"	union			" +
  " SELECT '' AS custid, 'TK' +[Price Group Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
             " [Unit Price] AS price ,0 as disc, '' as paev, [Starting Date] as startday ,[Currency code] as val from " +
             " [TechnoLit$Item Price]  where [Starting Date] < getdate()-5  and [Price Group Code]<>'' and [Item No_]<>'' " +
        " union select '' AS custid, 'TK' +[Customer Disc_ Group] AS clgroup,'' AS prodid,[Item Discount Group] AS prodgroup, " +
      " 0 AS price ,[Discount %] as disc , '' as paev, '' as startday ,'' as val " +
       "     from [Tobis Kaliningrad$Cust__Item Discount] where [Customer Disc_ Group]<>'' and [Item Discount Group]<>'' " +
		"	union SELECT '' AS custid, 'AA' +[Price Group Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
       "       [Unit Price] AS price ,0 as disc, '' as paev, [Starting Date] as startday ,[Currency code] as val from " + 
       "       [TechnoLit$Item Price]  where [Starting Date] < getdate()-5  and [Price Group Code]<>'' and [Item No_]<>'' " + 
       "  union select '' AS custid,'AA' + [Customer Disc_ Group] AS clgroup,'' AS prodid,[Item Discount Group] AS prodgroup, " +
      " 0 AS price ,[Discount %] as disc , '' as paev, '' as startday ,'' as val " +
       "     from [Avto Assortiment$Cust__Item Discount] where [Customer Disc_ Group]<>'' and [Item Discount Group]<>'' " ; 
		
	 

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

         if(sdate != null  && !sdate.startsWith("1753") && !sdate.startsWith("1700")){
        	 try{
        //	  	 mline.setStartDate(sdate.substring(0,10));
        	        		 
        	 }catch(Exception e){
        	 }
         }
         if(date != null && !date.startsWith("1753") && !date.startsWith("1700")){
        	 
try{
  //	 mline.setEndDate(date.substring(0,10));
        	 }catch(Exception e){
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

     System.out.println("Pricematrix size " + result.size());

return result;
  }

 
 private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }
   private Connection getNewConnection3() throws SQLException{
	    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
	  }

   private Connection getNewConnection4() throws SQLException{
	    return DriverManager.getConnection(url3, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
	  }



	public Vector getLastThreeData() {
		// TODO Auto-generated method stubfg
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	    
	     }

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     
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

    String tableName = "dayreport_kal";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }

    String sql = " SELECT clid AS klient,sentdate AS kuupaev,docsum AS summa,linecount AS ridu," +
    		"text AS tekst,'' AS dealer,clgroup AS agentname" +
        " FROM  " + tableName +
        " WHERE sentdate>(GETDATE()-30) ORDER BY clid ";

   if (isDebug()) System.out.println(sql);
   this.con = getNewConnection();
   Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    StringBuffer sb = new StringBuffer();
    String sep = ";";

    while(rs.next()){
      String cliID = rs.getString("klient").trim();
      sb.append(cliID + sep);
      sb.append(rs.getString("kuupaev").trim() + sep);
      sb.append(getNF().format(rs.getDouble("summa")) + sep);
      sb.append(rs.getInt("ridu") + sep);
      String tekst = ensureLen(rs.getString("tekst")  ," ");
      tekst = tekst.replace('\'', ' ').replace('\"', ' ');
      tekst = tekst.replace('\n', ' ');
      String agendinimi = rs.getString("agentname");
      sb.append(tekst + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(getNF().format(0) + sep);
        sb.append(ensureLen((agendinimi), " ") + sep);
        sb.append(ensureLen((" "), " ") + sep);
        sb.append(ensureLen((""), " ") + sep);
        sb.append((" ") + sep);
        sb.append((" ") + sep);
      sb.append("\n");
    }

    ManagerData md = new ManagerData();
    md.setClientVisitInfo(sb.toString());

 StringBuffer bf2 = new StringBuffer();
 Enumeration enums = klient.elements();
 while(enums.hasMoreElements()){
 	Customer cust = (Customer)enums.nextElement();
 	Iterator agen = cust.getAgents().keySet().iterator();
 	while(agen.hasNext()){
    Agent ag = 	(Agent)agents.get((String)agen.next());
   bf2.append(cust.getID() + sep);
   bf2.append(ag.getID() + sep);
   bf2.append(ag.getName() + "\n");
 }
	}

    md.setClientAgentInfo(bf2.toString());
    return md;

  }


   
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           return lines;
   }



       public String getSaveStockInfo(){
    			  
    	   String cmd = " SELECT [Item No_] as kood       ,[Quantity] as kogus  ,[Location code] as ladu " +
"  FROM [_MERISOFT$Technolit_STOCK]";
   

    		          if (isDebug()) System.out.println(cmd);
    		          StringBuffer products = new StringBuffer();
    		          try {

                            	Connection con = getNewConnection();
    		        	        Statement stmt = con.createStatement();
    		        	        ResultSet rs = stmt.executeQuery(cmd);

    		            while (rs.next()) {
                         String kood = rs.getString("kood").trim();
    		              double seis = rs.getDouble("kogus");
    		              String dealer = rs.getString("ladu").trim();
    		              if(seis > 0){
    		                  products.append(kood +  ";");
    		                  products.append(" ;");
    		                  products.append(getParent().getNF().format(seis) + ";" + dealer  +  "\n"); // + dealer +  "\n");
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

	    	  String sql = " SELECT [Item No_] as kood  ,[Quantity] as kogus  ,[Location code] as ladu " +
              " FROM [_MERISOFT$Technolit_STOCK]";
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

   public TreeMap getProdGroups() throws SQLException {
     TreeMap   tm = new TreeMap();
   //  setTreeGroup(true);
     String cmd = "select dv.Code  as kood, dv.Name  as nimi from [TechnoLit$Dimension Value] dv where dv.[Dimension Code]='PROJECT'";
   //  System.out.println("Asike 1 " +cmd);
     try {
     	 Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	            String groupID = rs.getString("kood").trim();
 	          	                String name = rs.getString("nimi") ;
 	          	             if(name != null){
 	    	                	tm.put(groupID,Converter.enCodeEst(groupID + " " +name.trim()));
 	    	                }
 	        }
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



   public TreeMap getAgents(){

        String cmdold = "select [Pard agento kodas] as kood , Name as nimi , '' AS objekt from [TechnoLit$MOFF Pardavimo agentai] " +
	" inner join TechnoLit$Salesperson_Purchaser on " +
                       " [Pard agento kodas]=Code ";
        

        String cmd = "  select [TechnoLit$Salesperson_Purchaser].code as kood , [TechnoLit$Salesperson_Purchaser].Name as nimi ," +
        " '' AS objekt FROM [TechnoLit$Salesperson_Purchaser] " +
        " WHERE [TechnoLit$Salesperson_Purchaser].Merisoft=1 ";
	    if (isDebug()) System.out.println(cmd);
	    TreeMap agents = new TreeMap();
	    try {
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
               //  System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
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
	        
	        
	       
	    } catch (Exception e){
	        getParent().showError(e);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }


       public TreeMap getCustomers(Agent agent){

    	    String cmd = "select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, " +
	"  a.[Due Date] AS kuupaev, sum(b.Amount) AS summa, " +
     "        DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [TechnoLit$Cust_ Ledger Entry] a inner join " +
     "        [TechnoLit$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
      "        where " +
       "          (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Open]= 1 " +
        "      group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_],  " +
         "     a.[Due Date] , getdate()-a.[Due Date] ,a.[Customer No_]";

    	   
    	    try {

    	  
    	  System.out.println(cmd);
    	       this.debtList = new Vector();
    	     Connection con = getNewConnection2();
    	       Statement stmt1 = con.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	        Debt debt = new Debt();
    	       String custID = rs1.getString("klient");
    	        debt.setCustID((custID));
    	        String maksja = rs1.getString("maksja");
    	        debt.setCustID((maksja));
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
    	
    	      
    	        cmd = "select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, " +
    	    			"  a.[Due Date] AS kuupaev, sum(b.Amount) AS summa, " +
    	    		     "        DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [Tobis Kaliningrad$Cust_ Ledger Entry] a inner join " +
    	    		     "        [Tobis Kaliningrad$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
    	    		      "        where " +
    	    		       "          (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Open]= 1 " +
    	    		        "      group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_],  " +
    	    		         "     a.[Due Date] , getdate()-a.[Due Date] ,a.[Customer No_]";
   
    	      
   	    Connection   con4 = getNewConnection3();
   	        stmt1 = con4.createStatement();
   	        rs1 = stmt1.executeQuery(cmd);
   	       while(rs1.next()){
   	        Debt debt = new Debt();
   	       String custID = "TK" + rs1.getString("klient");
   	        debt.setCustID((custID));
   	        String maksja = "TK" + rs1.getString("maksja");
   	        debt.setCustID((maksja));
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
   	       con4.close();

   	       
   	       
 	      
	        cmd = "select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, " +
	    			"  a.[Due Date] AS kuupaev, sum(b.Amount) AS summa, " +
	    		     "        DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [Avto Assortiment$Cust_ Ledger Entry] a inner join " +
	    		     "        [Avto Assortiment$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
	    		      "        where " +
	    		       "          (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Open]= 1 " +
	    		        "      group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_],  " +
	    		         "     a.[Due Date] , getdate()-a.[Due Date] ,a.[Customer No_]";

	      
	    Connection   con5 = getNewConnection4();
	        stmt1 = con5.createStatement();
	        rs1 = stmt1.executeQuery(cmd);
	       while(rs1.next()){
	        Debt debt = new Debt();
	       String custID = "AA" + rs1.getString("klient");
	        debt.setCustID((custID));
	        String maksja = "AA" + rs1.getString("maksja");
	        debt.setCustID((maksja));
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
	       con5.close();

   	       
   	       
   	       
   	       
   	       
   	       
    	      } catch (Exception e){
      	        e.printStackTrace();
      	      }
      	      
    	      
    	      
    	/// KLIENDI ANDMETE LEIDMINE

    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();


    	   String sql =  "  SELECT k.Name AS nimi,k.No_ AS kood,k.[Bill-to Customer No_] AS payer,k.[Address]+' '+k.City AS aadress , a.Address+' '+a.City as addr2 " + 
                "       ,k.[Salesperson Code] AS agent ,  Blocked AS blokk, " +
                "  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type, " +
                "  0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum ,a.Contact +' ' + a.[Phone No_] as kontakt " +
                "     ,[Price Group Code] as alegr ,k.[Cust__Item Disc_ Gr_] as alegr2 " +
                " FROM [TechnoLit$Customer] k LEFT join [TechnoLit$Ship-to Address] a on  a.[Customer No_]=k.No_ " +
                "  WHERE k.[Salesperson Code] not in  ('')  AND Blocked not in( 3)" ;

    	   System.out.println(sql);
    	   try {
    	        Connection con = getNewConnection2();
    	        Statement stmt = con.createStatement();
    	        ResultSet rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name =  "TL " + rs.getString("nimi").trim();
    	           String id = rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer((id));
    	            String payer = null;
    	          
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst("TL"+id + " " + name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer( payer.trim());
    	             if(overdebts.get( payer.trim()) != null){
    	            	 customer.setDebt(Double.parseDouble((String)overdebts.get(payer.trim())));
    	              }
    	             
    	             if(usedcredit.get( payer.trim()) != null){
    	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get( payer.trim())));
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
      	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr2),"Russia"));
      	               name = name + " " + aadr2;
      	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadr2));
      	             }else if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Russia"));
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

    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlock(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	            // customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
    	           	}
    	           	
    	           	

    	        	customer.setGroup("TLSKALD");
    	       
    	          
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
    	        	customer.setGroup(Converter.cleanLitLatEst("TL"+ alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst("TL"+ alegr2.trim()));   
    	           }
    	           Dealer d = new Dealer("TLSKLAD");
    	           d.setName("TLSKLAD");
   	            customer.setPrimDeal("TLSKLAD");
   	            customer.setDealer(d);
   	            
    	          // customer.setPayer(id);
    	            tm.put("TL"+ Converter.cleanLitLatEst(id) + customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();

    	        
    	        sql =  "  SELECT k.Name AS nimi,k.No_ AS kood,k.[Bill-to Customer No_] AS payer,k.[Address]+' '+k.City AS aadress , a.Address+' '+a.City as addr2 " + 
    	                "       ,k.[Salesperson Code] AS agent ,  Blocked AS blokk, " +
    	                "  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type, " +
    	                "  0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum ,a.Contact +' ' + a.[Phone No_] as kontakt " +
    	                "     ,[Price Group Code] as alegr ,k.[Cust__Item Disc_ Gr_] as alegr2 " +
    	                " FROM [Tobis Kaliningrad$Customer] k LEFT join [Tobis Kaliningrad$Ship-to Address] a on  a.[Customer No_]=k.No_ " +
    	                "  WHERE k.[Salesperson Code] not in  ('')  AND Blocked not in( 3)" ;
    	       
    	        
    	         con = getNewConnection3();
    	         stmt = con.createStatement();
    	         rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name ="TK " +  rs.getString("nimi").trim();
    	           String id ="TK" +  rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer((id));
    	           customer.setVAT(0);
    	           Dealer d = new Dealer("TKSKLAD");
    	           d.setName("TKSKLAD");
   	            customer.setPrimDeal("TKSKLAD");
   	            customer.setDealer(d);
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(id + " " + name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer("TK" + payer.trim());
    	             if(overdebts.get("TK" + payer.trim()) != null){
    	            	 customer.setDebt(Double.parseDouble((String)overdebts.get("TK" + payer.trim())));
    	              }
    	             
    	             if(usedcredit.get("TK" + payer.trim()) != null){
    	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get("TK" + payer.trim())));
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
      	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr2),"Russia"));
      	               name = name + " " + aadr2;
      	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadr2));
      	             }else if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Russia"));
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

    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlock(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	           //  customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
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
    	           	customer.setGroup("TK"+ grupp.trim());
    	           }
    	


    	        	customer.setGroup("TKSKLAD");
    	          
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
    	        	customer.setGroup(Converter.cleanLitLatEst("TK"+ alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst("TK"+ alegr2.trim()));   
    	           }
    	          
    	          // customer.setPayer(id);
    	            tm.put(Converter.cleanLitLatEst(id) + customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();

    	        
    	        

    	        sql =  "  SELECT k.Name AS nimi,k.No_ AS kood,k.[Bill-to Customer No_] AS payer,k.[Address]+' '+k.City AS aadress , a.Address+' '+a.City as addr2 " + 
    	                "       ,k.[Salesperson Code] AS agent ,  Blocked AS blokk, " +
    	                "  [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,'' AS visit,'' AS type, " +
    	                "  0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum ,a.Contact +' ' + a.[Phone No_] as kontakt " +
    	                "     ,[Price Group Code] as alegr ,k.[Cust__Item Disc_ Gr_] as alegr2 " +
    	                " FROM [Avto Assortiment$Customer] k LEFT join [Avto Assortiment$Ship-to Address] a on  a.[Customer No_]=k.No_ " +
    	                "  WHERE k.[Salesperson Code] not in  ('')  AND Blocked not in( 3)" ;
    	       
    	        
    	         con = getNewConnection4();
    	         stmt = con.createStatement();
    	         rs = stmt.executeQuery(sql);
    	        while(rs.next()){
    	         String name ="AA " +  rs.getString("nimi").trim();
    	           String id ="AA" +  rs.getString("kood").trim();
    	           //System.out.println(line);
    	           Customer customer = new Customer((id));
    	           customer.setVAT(0);
    	           Dealer d = new Dealer("AASKLAD");
    	           d.setName("AASKLAD");
   	            customer.setPrimDeal("AASKLAD");
   	            customer.setDealer(d);
    	            String payer = null;
    	           if (name.equals("")) name = "<" + id + ">";
    	           customer.setName(Converter.enCodeEst(id + " " + name));
    	           	payer = rs.getString("payer");
    	           	if(payer != null && payer.trim().length()>0){
    	             customer.setPayer("AA" + payer.trim());
    	             if(overdebts.get("AA" + payer.trim()) != null){
    	            	 customer.setDebt(Double.parseDouble((String)overdebts.get("AA" + payer.trim())));
    	              }
    	             
    	             if(usedcredit.get("AA" + payer.trim()) != null){
    	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get("AA" + payer.trim())));
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
      	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr2),"Russia"));
      	               name = name + " " + aadr2;
      	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadr2));
      	             }else if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Russia"));
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

    	             if(rs.getString("blokk").equals("1")){
    	               customer.setSalesBlock(true);
    	             }

    	            String paym = rs.getString("payment");

    	           	String num = rs.getString("alekood");
    	           	if(num != null && num.trim().length()>0){
    	            // customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
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
    	           	customer.setGroup("AA"+grupp.trim());
    	           }
    	


    	        	customer.setGroup("AASKLAD");
    	          
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
    	        	customer.setGroup(Converter.cleanLitLatEst("AA" + alegr.trim()));   
    	           }
    	           
    	           String alegr2 = rs.getString("alegr2");
    	           if(alegr2 != null && alegr2.trim().length()>0){
    	        	customer.setGroup(Converter.cleanLitLatEst("AA"+ alegr2.trim()));   
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
      
     Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     
     
     TreeMap  ladutr = new TreeMap();
     TreeMap  ladulah = new TreeMap();
    
    try {
  	  String cmd = "select [Item No_] as kood, inventory as kogus,PickupQty from [_MERISOFT$Technolit_ProdSTOCK]" ;
  	  
     	 Connection con = getNewConnection2();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	             String toode = null;
 	             double seis = 0;;
 	              	toode =rs.getString("kood");
 	              	seis = rs.getDouble("kogus");
 	          	double seis2 = rs.getDouble("PickupQty");
 	              	ladutr.put(toode, seis + "");
 	              	if(seis2 > 0)
 	          	ladulah.put(toode, seis2 + "");
 	        }

 	      rs.close();
 	    stmt.close();
 	      con.close();

 	    System.out.println(cmd);
  } catch (Exception e) {
      e.printStackTrace();
  }

     
     
  
     try {
     
    	 String lubaSQl = " SELECT No_ as toode from TechnoLit$Item where Show_in_mobile = 1";
    	 HashSet luba = new HashSet();
    	 
    	 Connection con = getNewConnection2();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(lubaSQl);
         while(rs.next()){
        	  String id = rs.getString("toode").trim();
        	  luba.add(id);
         }
         //System.out.println("luba suurus " + luba.size());
           rs.close();
           stmt.close();
           con.close();
    	 String cmd = " SELECT  t.No_ AS toode,t.[Description] as nimi1, t.[Description 2]  AS nimi2, t.[Unit Price] AS myygihind, '' AS grupp2,t.[Global Dimension 1 Code] AS grupp " + 
                      "   ,t.[Base Unit of Measure] AS unit,[No_ 2] AS ean,'' AS status ,0 AS maxdiscount, " +
                      "    '' AS type, [Units per Parcel] AS kastis,[Item_Cust_ Disc_ Gr_] as alegr ,  '' AS kaal, [VAT Prod_ Posting Group] AS vat ,0 as pallet " +
                      "    ,[Unit List Price] as listprice,[Item Name] FROM TechnoLit$Item t  WHERE t.Blocked=0 ";
      System.out.println(cmd);
     	 con = getNewConnection2();
        stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            String weight ="";
            Product prod = new Product(id);
            String n1 = rs.getString("nimi1");
            String name = id;
            if(n1 != null){
            	 name = Converter.enCodeEst(id + " " + n1); 	
            }
         
         	name = name.trim();
         	String name2 =  rs.getString("nimi2");
         	if (name2 != null && name2.trim().length() > 0){
         		name = name + name2;
         	}
         	
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);// / 1.18
           
                 if(ladutr.get(id) != null ){
            	 // prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
                 }
                 
                 if(ladulah.get(id) != null ){
               	  prod.setPartii("PickedQty: " + (String)ladulah.get(id));
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
                 


          	 int vati = 18;
          	 prod.setVat(vati + "");   
                  
               
                   double pallet = rs.getDouble("pallet");
                   if(pallet >0){
                	   prod.setOnPallet(pallet);
                   }
                   
                   double listprice = rs.getDouble("listprice");
                   if(listprice >0){
                	   prod.setPriceSecond(listprice * ((100 + vati)/100));
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
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate,0 as lastamount " +
    	"	 FROM [TechnoLit$Sales Invoice Line] r  " +
     "INNER JOIN [TechnoLit$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN TechnoLit$Item t ON t.No_=r.No_ WHERE " +
     " d.[Posting Date]> getdate()-500 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.No_,d.[Posting Date]";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = (rs.getString("klient").trim());

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

         
         
         
         
   
 System.out.println(cmd);
//   "klient","toode","kogus","viimane kuupļæ½ev","viimane kogus"
 
cmd = "SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate,0 as lastamount " +
"	 FROM [Tobis Kaliningrad$Sales Invoice Line] r  " +
"INNER JOIN [Tobis Kaliningrad$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN [Tobis Kaliningrad$Item] t ON t.No_=r.No_ WHERE " +
" d.[Posting Date]> getdate()-500 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.No_,d.[Posting Date]";

 
 con = getNewConnection3();
 stmt = con.createStatement();
 rs = stmt.executeQuery(cmd);
 while(rs.next()){

          String custID = ("TK" +rs.getString("klient").trim());

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

          String cmd2 =  " SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate " +
    		" FROM [TechnoLit$Sales Invoice Line] r  " +
             "     INNER JOIN [TechnoLit$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN TechnoLit$Item t ON t.No_=r.No_ WHERE " + 
     " d.[Posting Date]> getdate()-30 ORDER BY  d.[Sell-to Customer No_],r.No_,d.[Posting Date] ";
          
          System.out.println(cmd2);
		    Connection con2 = getNewConnection2();
		    Statement stmt2 = con2.createStatement();
		    ResultSet rs2 = stmt2.executeQuery(cmd2);


  while (rs2.next()){
       String klient = ( rs2.getString("klient").trim());
               String toode = rs2.getString("toode");
               if(toode != null){
            	
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
  }
               cou++;
             }


  rs2.close();
  stmt2.close();
  con2.close();

  
  

   counter = 0;
   cou = 0;
 oldklient = null;
 olddate = " ";
 oldtoode = null;

 
 cmd2 =  " SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate " +
 		" FROM [Tobis Kaliningrad$Sales Invoice Line] r  " +
          "     INNER JOIN [Tobis Kaliningrad$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN [Tobis Kaliningrad$Item] t ON t.No_=r.No_ WHERE " + 
  " d.[Posting Date]> getdate()-30 ORDER BY  d.[Sell-to Customer No_],r.No_,d.[Posting Date] ";
      
 

 System.out.println(cmd2);
  con2 = getNewConnection3();
    stmt2 = con2.createStatement();
    rs2 = stmt2.executeQuery(cmd2);


while (rs2.next()){
String klient = ("TK" + rs2.getString("klient").trim());
      String toode = rs2.getString("toode");
      if(toode != null){
   	
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
}
      cou++;
    }


rs2.close();
stmt2.close();
con2.close();


  
  








 counter = 0;
 cou = 0;
 oldklient = null;
 olddate = " ";
 oldtoode = null;

 cmd2 =  " SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate " +
	" FROM [TechnoLit$Sales Invoice Line] r  " +
  "     INNER JOIN [TechnoLit$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN TechnoLit$Item t ON t.No_=r.No_ WHERE " + 
" d.[Posting Date]> getdate()-30 ORDER BY  d.[Sell-to Customer No_],r.No_,d.[Posting Date] ";

System.out.println(cmd2);
 con2 = getNewConnection2();
 stmt2 = con2.createStatement();
 rs2 = stmt2.executeQuery(cmd2);


while (rs2.next()){
String klient = ( rs2.getString("klient").trim());
    String toode = rs2.getString("toode");
    if(toode != null){
 	
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
}
    cou++;
  }


rs2.close();
stmt2.close();
con2.close();


cmd = "SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate,0 as lastamount " +
"	 FROM [Avto Assortiment$Sales Invoice Line] r  " +
"INNER JOIN [Avto Assortiment$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN [Avto Assortiment$Item] t ON t.No_=r.No_ WHERE " +
" d.[Posting Date]> getdate()-500 ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.No_,d.[Posting Date]";

 con = getNewConnection3();
 stmt = con.createStatement();
 rs = stmt.executeQuery(cmd);
 while(rs.next()){

          String custID = ("AA" +rs.getString("klient").trim());

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


counter = 0;
cou = 0;
oldklient = null;
olddate = " ";
oldtoode = null;


cmd2 =  " SELECT  d.[Sell-to Customer No_] AS klient,r.No_ AS toode,r.Quantity AS kogus, d.[Posting Date] AS lastdate " +
" FROM [Avto Assortiment$Sales Invoice Line] r  " +
"     INNER JOIN [Avto Assortiment$Sales Invoice Header] d ON r.[Document No_]= d.No_ INNER JOIN [Avto Assortiment$Item] t ON t.No_=r.No_ WHERE " + 
" d.[Posting Date]> getdate()-30 ORDER BY  d.[Sell-to Customer No_],r.No_,d.[Posting Date] ";


System.out.println(cmd2);
con2 = getNewConnection3();
stmt2 = con2.createStatement();
rs2 = stmt2.executeQuery(cmd2);


while (rs2.next()){
String klient = ("AA" + rs2.getString("klient").trim());
String toode = rs2.getString("toode");
if(toode != null){

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
     return "NaviTobisKAL";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
   public TreeMap getDealers() {
	   TreeMap	dealers = new TreeMap();
	   Dealer dealer = new Dealer("TKSKLAD");
	   dealer.setName("Tobis Kaliningrad");
	   dealers.put(dealer.getID(), dealer);
	   Dealer dealer2 = new Dealer("TLSKLAD");
	   dealer2.setName("TechnoLit");
	   dealers.put(dealer2.getID(), dealer2);
	   Dealer dealer3 = new Dealer("AASKLAD");
	   dealer3.setName("Avto Assortiment");
	   dealers.put(dealer3.getID(), dealer3);
		return dealers;
	    }

   public double getSalary(String agentCode){
     return 0;
   }


   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap usedcredit = new TreeMap();
  
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:MERISOFT";
   private String url2 = "jdbc:odbc:TL";
   private String url3 = "jdbc:odbc:TL";
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
  	 	String cmd = "  select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [TechnoLit$Cust_ Ledger Entry] " +
                            "  where Month(getdate())=Month([Posting Date]) and " +
            " Year(getdate())=Year([Posting Date])  group by [Salesperson Code]";
  	  Connection con = getNewConnection2();
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
            
                 String agentID = rs.getString("agent");
                 double sales = rs.getDouble("summa");
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                //   agent.setMonthSales();
                   agent.setStart("продажа TL : "  + (int)(sales) );
            }
          }

	        rs.close();
	        stmt.close();
	        con.close();
	        
	    	String cmd2 = "  select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Tobis Kaliningrad$Cust_ Ledger Entry] " +
                    "  where Month(getdate())=Month([Posting Date]) and " +
    " Year(getdate())=Year([Posting Date])  group by [Salesperson Code]";
Connection con2 = getNewConnection3();
Statement stmt2 = con2.createStatement();
ResultSet rs2 = stmt2.executeQuery(cmd2);
while(rs2.next()){

 String agentID = rs2.getString("agent");
 double sales = rs2.getDouble("summa");
 Agent agent = (Agent)agents.get(agentID);
 if (agent != null){
   agent.setStart("продажа TK : "  + (int)(sales) );
}
}

rs2.close();
stmt2.close();
con2.close();


 cmd2 = "  select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Avto Assortiment$Cust_ Ledger Entry] " +
        "  where Month(getdate())=Month([Posting Date]) and " +
" Year(getdate())=Year([Posting Date])  group by [Salesperson Code]";
 con2 = getNewConnection3();
 stmt2 = con2.createStatement();
 rs2 = stmt2.executeQuery(cmd2);
while(rs2.next()){

String agentID = rs2.getString("agent");
double sales = rs2.getDouble("summa");
Agent agent = (Agent)agents.get(agentID);
if (agent != null){
	if(agent.getStart().length() > 0){
		agent.setStart(agent.getStart() + "|продажа AA : "  + (int)(sales) );
	}else{
		agent.setStart("продажа AA : "  + (int)(sales) );	
	}
}
}

rs2.close();
stmt2.close();
con2.close();



  	 } catch (Exception e){
        e.printStackTrace();
     }

  }
}
