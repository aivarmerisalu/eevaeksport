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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
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

import ee.mobilejava.wrapper.SuperVisorLineWrapper;
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
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>MOFF Prekegetproducts
 * 
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NaviTobisDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public NaviTobisDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    parent.setAbiKood(true);
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
		  makeeCat();
		  System.exit(0);
   	return null;
   }

   
   private void makeeCat(){
	     
   	String  cmd2 = " SELECT [No_] as toode from [2015_Tobis$Item] where  [Show in mob_ device] = 1";
    TreeMap phsize = new TreeMap();
    TreeMap used = new TreeMap();
   	
     	String picts = parent.getProps().getProperty("pict.dir");
 	    String pictspda = parent.getProps().getProperty("pictpda.dir");
 	 

   	try {
 	    
   		Connection con = getNewConnection2();
		Statement stmt = con.createStatement();
   	    ResultSet rs = stmt.executeQuery(cmd2);

        StringBuffer buf = new StringBuffer();
        
        // leiame kataloogid ja peame meeles 
        File d  = new File(picts);
        File[] dd = d.listFiles();

        Vector cats = new Vector();
        
        for(int i = 0; i < dd.length ; i++){
        	File t = dd[i];
        	if(t.isDirectory()){
        		cats.add(t.getName());
        	}
        }
        
        System.out.println("Piltide leidmine - katalooge sees " + cats.size() + " " + cats.toString());
   	    while (rs.next()) {
   	    	
   	     String toode = rs.getString("toode").trim();  
         toode = toode.replace('/', '_');
         toode = toode.replace('/', '_');
        
   	             File f = new File(picts + toode+".jpg");
                 if(f.exists()){
                	 used.put(toode + ".jpg", toode+ ".jpg");
                 } else if(cats.size() > 0 ){
                	 for(int w = 0 ;w < cats.size();w++){
                		f = new File(picts + cats.elementAt(w) + "/" + toode+".jpg");
                		if(f.exists()){
                			System.out.println("Kataloogis leitud " + picts + cats.elementAt(w) + "/" + toode+".jpg");	
                			 used.put(toode + ".jpg", toode+ ".jpg");
                		}                     
                	 }
                   }
                 }
   	    
   	    rs.close();
   	    stmt.close();
   	    con.close();
   	
        	 
          System.out.println("Piltide kopeerimine ");	
          
            String[] gg = new File(picts).list();
         	 for(int i = 0;gg.length>i;i++){          	
             	File in = new File ( picts + gg[i]); 
             	if(in.isFile()){
             		String fname = toUpper(gg[i]);
             	File inpda = new File ( pictspda + fname); 
             	if(!inpda.exists() && used.get(fname) != null) {
             		
             		if("true".equals(parent.getProps().getProperty("pict.noshrink"))) {
             			Converter.copyFile(in,inpda);
             		}else {
             			Converter.shrinkAndCopyFile(in,inpda,300);
             		}
             	  
                 	
                 	}
             	phsize.put(fname, inpda.length());	
             	}
             }
             
             //piltide kopeerimine alam kataloogist ..
             
             if(cats.size() >0){
            	 for(int h = 0;h < cats.size(); h++){
            	  String[]  ff = new File(picts + cats.elementAt(h)+ "/").list();
               	   for(int i = 0;i < ff.length  ;i++){          	
                   	File in = new File ( picts + cats.elementAt(h)+ "/" + ff[i]); 
                   	if(in.isFile()){   
                   		String fname = toUpper(ff[i]);
                   	File inpda = new File ( pictspda + fname); 
                   	if(inpda.exists() && used.get(fname) != null) {
                   		if("true".equals(parent.getProps().getProperty("pict.noshrink"))) {
                 			Converter.copyFile(in,inpda);
                 		}else {
                 			Converter.shrinkAndCopyFile(in,inpda,300);
                 		}
                   	   
                   	   }
                	phsize.put(fname, inpda.length());
                   	 }
                   }
                 }
               }
             
          
             File f = new File(pictspda + "list.txt");
             if (f.exists())f.delete();
             
            
             Iterator itera =   used.keySet().iterator();
  	       while(itera.hasNext()) {
  	    	   String prid = (String)itera.next();
  	    	   String s = "0";
  	    	   try {
  	    		s = phsize.get(prid) + "";
  	    		if(s == null) {
  	    			s = "0";
  	    		}
  	    	   }catch(Exception e) {
  	    		   
  	    	   }
  	    		buf.append(prid + ";" + s + "\n");
  	               
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
 
   private String toUpper(String asi ){
	   
	   asi = asi.substring(0,asi.length()-4).toUpperCase() + asi.substring(asi.lastIndexOf("."));
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
            "   [2015_Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or" +
             "           year ([Ending Date]) = 1753)  and [Sales Type] = 0    " +
                          "  union  SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_]  AS prodid,'' AS prodgroup, " +
             " [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from " +
             " [2015_Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or " +
                      "  year ([Ending Date]) = 1753) and [Sales Type] = 1 " +   
                          "  union  select [Sales Code] AS custid, '' AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
            "   0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                       "  from [2015_Tobis$Sales Line Discount] where [Type] = 1 and [Sales Type] = 0 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or " +
                      "  year ([Ending Date]) = 1753)  " +     
                   "  union select [Sales Code] AS custid, ''  AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
              " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                       "  from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or  " +
                      "   year ([Ending Date]) = 1753) " +
                      " union select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
                     " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                     "    from [2015_Tobis$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or " +  
                     "  year ([Ending Date]) = 1753)       " +
                    " union select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
                   " 0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val " +
                    "     from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or " + 
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

   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }


	public Vector getLastThreeData() {
		// TODO Auto-generated method stubfg
		return null;
	}

	 public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	 public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     String sql = "INSERT into dbo.supervisor_lit values ("
	       + "?, "   //   agent	C(50),
	       + "?,"   //   agentname	C(200),
	       + "?,"   //   clientid	C(50),
	       + "?,"   //   clientname	C(200),
	       + "?,"   //	prodid	C(50),
	       + "?,"   //   prodname	C(200),
	       + "getdate() ,"   //	sentdate	D,
	       + " '' ,"   //	receivedTime	D,
	       + "?,"   //normalpr	N,
	       + "?,"   // amoun	N,
	       + "?,"   // grouptotal	N,
	       + "?,"   // cattotal	N,
	       + "?,"   // pertotcat	I,
	       + "?,"   // percolcat	I,
	       + "?,"   // perpalcat	I,
	       + "?,"   // pershecat	I,
	       + "?,"   // perrescat	I,
	       + "?,"   // isaction	L,
	       + "?,"   // ispartner	L,
	       + "?,"   // isbrand	L,
	       + "?,"   // isgroup	L
	       + "?,"   // actionaeva	N,
	       + "?,"   // actionbeva N,
	       + "?,"   // actiontota	N,
	       + "?,"   // custeval	N,
	       + "?,"   // custcomme	C(200),
	       + "?,"   // comment	C(200),
	       + "?,"   // custagent	C(200),
	       + "?,"   // custagentname	C(200),
	       + "?,"   // mustbe	I,
	       + "?,"   // ideal	I,
	       + "?,"   // resstr1	C(200),
	       + "?,"   // resstr2	C(200),
	       + "?,"   // resstr3	C(200),
	       + "?,"   // resint1	I,
	       + "?,"   // resint2	I,
		      + "?)" ; //	resint3	L

	       try {
	       	System.out.println("jam");
	        Connection con = getNewConnection();
	           PreparedStatement ps = con.prepareStatement(sql);

	           for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
	        	   eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
	             ps.setString(1, wr.getInfo().getAgentID());
	             ps.setString(2, wr.getInfo().getAgentName());
	             ps.setString(3, lw.getLine().getCustID());
	             ps.setString(4, lw.getLine().getCustName());
	             ps.setString(5, lw.getLine().getProdID());
	             ps.setString(6, lw.getLine().getProdName());
	             ps.setDouble(7, lw.getLine().getNormalPrice());
	             ps.setDouble(8, lw.getLine().getAmount());
	             ps.setDouble(9, 0);
	             ps.setDouble(10, 0);
	             ps.setInt(11, lw.getLine().getPercentageFromTotalCategory());
	             ps.setInt(12, lw.getLine().getPercentageFromColdCategory());
	             ps.setInt(13, lw.getLine().getPercentageFromPallCategory());
	             ps.setInt(14, lw.getLine().getPercentageFromShelfCategory());
	             ps.setInt(15, lw.getLine().getPercentageFromRestCategory());
	             ps.setBoolean(16, lw.getLine().isAction());
	             ps.setBoolean(17, lw.getLine().isPartner());
	             ps.setBoolean(18, lw.getLine().isBrand());
	             ps.setBoolean(19, lw.getLine().isGroup());
	             ps.setDouble(20, lw.getLine().getAction1Eval()*100);
	             ps.setDouble(21, lw.getLine().getAction2Eval()*100);
	             ps.setDouble(22, lw.getLine().getActionTotalEval()*100);
	             ps.setDouble(23, lw.getLine().getCustEval()*100);
	             String cc = lw.getLine().getCustComment();
	             if (cc == null){
	             	cc = " ";
	             }
	             ps.setString(24, cc);

	             String cd = lw.getLine().getComment();
	             if (cd == null){
	             	cd = " ";
	             }
	             ps.setString(25, cd);

	             String ce = lw.getLine().getCustAgentId();
	             if (ce == null){
	             	ce = " ";
	             }

	             ps.setString(26, ce);

	             String cf = lw.getLine().getCustAgentName();
	             if (cf == null){
	             	cf = " ";
	             }
	             ps.setString(27, cf);
	             ps.setInt(28, lw.getLine().getDealNr());
	             ps.setInt(29, lw.getLine().getIdealNr());

	             String cr1 = lw.getLine().getResStr1();
	             if (cr1 == null){
	             	cr1 = " ";
	             }
	             ps.setString(30, cr1);


	             String cr2 = lw.getLine().getResStr2();
	             if (cr2 == null){
	             	cr2 = " ";
	             }
	             ps.setString(31, cr2);


	             String cr3 = lw.getLine().getResStr3();
	             if (cr3 == null){
	             	cr3 = " ";
	             }
	             ps.setString(32, cr3);
	             ps.setInt(33, lw.getLine().getResInt1());
	             ps.setInt(34, lw.getLine().getResInt2());
	             ps.setInt(35, 111); //111 on eesti andmed
	             ps.executeUpdate();
	           }

	           ps.close();
	           con.close();
	       } catch (SQLException e) {
	           e.printStackTrace();
	       }
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

    String tableName = "dayreport_lit";
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
/*
           try {
          	  String cmd = " SELECT  [Asortimento kodas] AS grupp,[Prekës Nr_] AS toode  FROM [Premia Tallinna Külmhoone AS$Prekiø asortimentas] r" +
          	  		" INNER JOIN [Premia Tallinna Külmhoone AS$Item] t ON t.[No_]=r.[Prekës Nr_]  ORDER BY [Search Description]";
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

                            clGroup = rs.getString("grupp");
                            prodID = rs.getString("toode");

                            if (custID != null && custID.trim().length() > 0) {
                              mline.setCustID(custID.trim());
                            }
                            if (clGroup != null
                                && clGroup.trim().length() > 0) {
                              mline.setCliGroup(clGroup.trim());
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
                         */
           return lines;
   }



   public DealerProduct DealerProductsList() {
     return null;
   }

   
   private void  saveDebtFile(String debtstring) {
	   
       if(debtstring != null){
         String fpath = getParent().getProps().getProperty("debtrefdir");
         String fileName = "debtref.txt";
         if (fpath != null) {
           fileName = fpath + fileName;
         }
         File debtFile = new File(fileName);
         java.io.Writer wr;
		try {
			wr = new FileWriter(debtFile);
			   wr.write(debtstring);
		         wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       }
     }

   public void makeDebtFile(){
			  
	   String cmd = "select c.No_ as kood ,sum(case when [Due Date] < getdate() then a.amount else 0 end) as yle,max( c.Blocked) as blokk  from " +
       " [_Merisoft$Tobis$Customer_Open_Docs] a inner join [2015_Tobis$Customer] c on a.[Sell-to Customer No_]= c.No_  " +
       " Group by c.No_ having sum(case when [Due Date] < getdate() then a.amount else 0 end) != 0";

	          if (isDebug()) System.out.println(cmd);
		          StringBuffer products = new StringBuffer();
		          try {

		            	 Connection con = getNewConnection2();
		        	        Statement stmt = con.createStatement();
		        	        ResultSet rs = stmt.executeQuery(cmd);

		            while (rs.next()) {

		              String kood = rs.getString("kood").trim();
		              double yle = rs.getDouble("yle");
		              int bl = rs.getInt("blokk");
		              if(yle > 0){
		                  products.append(kood + ";" + getParent().getNF().format(yle) + ";" + bl + "\n");
		              }
		              }
		              rs.close();
		              stmt.close();
		              con.close();
		          } catch (Exception e) {
		              getParent().showError(e);
		          }
		          
		          
		          saveDebtFile(products.toString());
		          
		      }


       public String getSaveStockInfo(){
    			  
    	   String cmd = " SELECT [Item No_] as kood       ,[Quantity] as kogus  ,[Location code] " +
"  FROM [_MERISOFT$Tobis_Stock]";
   
    	  
    		   	  

    		          if (isDebug()) System.out.println(cmd);
    		          StringBuffer products = new StringBuffer();
    		          try {


    		            	 Connection con = getNewConnection2();
    		        	        Statement stmt = con.createStatement();
    		        	        ResultSet rs = stmt.executeQuery(cmd);

    		            while (rs.next()) {

    		              String kood = rs.getString("kood").trim();
    		              double seis = rs.getDouble("kogus");
    		              if(seis > 0){
    		                  products.append(kood +  ";");
    		                  products.append(" ;");
    		                  products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
    		              }
    		              }
    		              rs.close();
    		              stmt.close();
    		              con.close();
    		          } catch (Exception e) {
    		              getParent().showError(e);
    		          }
    		          
    		          makeDebtFile();
    		          
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
    // setTreeGroup(true);
     String cmd = "select dv.Code  as kood, dv.Name  as nimi from [2015_Tobis$Dimension Value] dv where dv.[Dimension Code]='PROJEKTAS'";
     System.out.println("Asike 1 " +cmd);
     try {
     	 Connection con = getNewConnection2();
     	 docCodes = new HashSet();
 	        Statement stmt = con.createStatement();
 	        ResultSet rs = stmt.executeQuery(cmd);
 	        while(rs.next()){
 	            String groupID = rs.getString("kood");
 	          String name = rs.getString("nimi") ;
 	               
 	                   tm.put(Converter.cleanLitLatEst(cleardots(groupID)),Converter.enCodeEst(groupID + " " +name));
 	                  if(!docCodes.contains(Converter.cleanLitLatEst(cleardots(groupID)))){
 	               	     docCodes.add(Converter.cleanLitLatEst(cleardots(groupID)));
 	                  }
 	        }
 	       // System.out.println("Teine" + tm.toString());
 	        rs.close();
 	        stmt.close();
 	        con.close();
      } catch (Exception e){
        e.printStackTrace();
      }  
      String inP = parent.getProps().getProperty("doc.origpath");
      String outP = parent.getProps().getProperty("doc.destination");
      Converter.prepareDocCatalogue(inP, outP, docCodes, false);
      return tm;
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


       String cmd = "  select [2015_Tobis$Salesperson_Purchaser].code as kood , [2015_Tobis$Salesperson_Purchaser].Name as nimi ," +
       " '' AS objekt FROM [2015_Tobis$Salesperson_Purchaser] " +
       " WHERE [2015_Tobis$Salesperson_Purchaser].Merisoft=1 ";
    
       
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
                 System.out.println("Lisan Duubleid " + "dub." +agent.getID() + " " + agent.getDubbler().toString());
			    }

				}
	          
	          agents.put(name+agent.getID(), agent);
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
             "select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[Document No_] AS number, a.[Due Date] AS kuupaev,a.[Document Date] AS kuupaev2, sum(b.[Amount]) AS summa, " + 
              "       DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer FROM [2015_Tobis$Cust_ Ledger Entry] a inner join " +
               "      [2015_Tobis$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_]  " +
                "     where   " +
				"  (a.[Document Type] = 2 or a.[Document Type]= 3)  AND a.[Open]= 1  " +
                 "    group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[Document No_], " + 
                  "   a.[Due Date],a.[Document Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_] having sum(b.[Amount])<> 0 order by  a.[Due Date] ";

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
    	        if(maksja.trim().equals("JUTA")) {
    	        	maksja = "JUTA1";
	           		}
    	       // debt.set(conv(maksja));
    	       
    	        String doknr = rs1.getString("number");
    	        if(doknr.trim().length()>6){
    	        	doknr = doknr.substring(2,doknr.length());
    	        }
    	        debt.setDoknr(doknr);
    	        debt.setDuedate(rs1.getDate("kuupaev"));
    	        debt.setDate(rs1.getDate("kuupaev2"));
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
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }
    	/// KLIENDI ANDMETE LEIDMINE

    	      
    	      HashMap contacts = new HashMap();
try{
    	      String cmd = " select c.No_ as kood ,k.[Organizational Level Code] as firma,k.Name as nimi, k.[Phone No_] as phone , k.[Mobile Phone No_] as phone2 from [2015_Tobis$Customer] c " +
                         " INNER JOIN [2015_Tobis$Contact Business Relation] b on b.[Link to Table]=1 and (c.No_=b.No_) INNER JOIN  " +
                        " [2015_Tobis$Contact] k on b.[Contact No_]=k.[Company No_] where k.Type=1";

    	  System.out.println(cmd);
    	    
    	       con = getNewConnection2();
    	       Statement stmt1 = con.createStatement();
    	       ResultSet rs1 = stmt1.executeQuery(cmd);
    	       while(rs1.next()){
    	    	   
    	       String custID = rs1.getString("kood");
    	       String res = "";

    	       String firma = rs1.getString("firma");
    	       if(firma != null && firma.trim().length() > 0){
    	    	   res = firma.trim();
    	       }
    	       
    	       String nimi = rs1.getString("nimi");
    	       if(nimi != null && nimi.trim().length() > 0){
    	    	   res = res + " " + nimi.trim();
    	       }
    	       
    	       String phone = rs1.getString("phone");
    	       if(phone != null && phone.trim().length() > 0){
    	    	   res = res + " " + phone.trim().replaceAll(" ", "-");
    	       }
    	       
    	       
    	       String phone2 = rs1.getString("phone2");
    	       if(phone2 != null && phone2.trim().length() > 0){
    	    	   res = res + " , " + phone2.trim().replaceAll(" ", "-");
    	       }
    	       
    	       res = Converter.enCodeEst(res);
    	       if(contacts.get(custID) != null){
    	    	   String asi = (String)contacts.get(custID);
    	    	   contacts.put(custID,asi + "|" + res); 
    	       }else{
    	    	  contacts.put(custID, res); 
    	       }
    	       
    	        }
    	       
    	       rs1.close();
    	       stmt1.close();
    	       con.close();
    	      } catch (Exception e){
    	        e.printStackTrace();
    	      }


HashMap lisainfo = new HashMap();
try{
String cmd = " select Customer_No as kood, Posting_Date as kuup, AmountLCY as summa from [_MERISOFT$Tobis$Customer_Last_Payment]";

System.out.println(cmd);

 con = getNewConnection2();
 Statement stmt1 = con.createStatement();
 ResultSet rs1 = stmt1.executeQuery(cmd);
 while(rs1.next()){
	   
 String custID = rs1.getString("kood");
 String res = "";

 String kuup = rs1.getString("kuup");
 double summa = rs1.getDouble("summa");
 if(kuup != null && kuup.trim().length() > 0){
	   res = "Paskutinis mokėjimas: (" + kuup.substring(0, 10).trim() + ") " + this.getNF().format(summa) + " eur" ;
 }
 
	   lisainfo.put(custID.trim(),res); 
 
  }
 
 rs1.close();
 stmt1.close();
 con.close();
 
 System.out.println("lisainfo " + lisainfo.size());
 
} catch (Exception e){
  e.printStackTrace();
}


HashMap points = new HashMap();

try{
String cmd = " SELECT c.no_ as klient, isnull(l.valid_points,0) as valid_points, isnull(l.unpaid_points,0) as unpaid_points \r\n" + 
		"from [2015_Tobis$Customer] c left join [_Merisoft$Tobis$Customer_loyalty_program] l on c.No_ = l.customer_no where c.[Lojalumo programa] = 1";

System.out.println(cmd);

 con = getNewConnection2();
 Statement stmt1 = con.createStatement();
 ResultSet rs1 = stmt1.executeQuery(cmd);
 while(rs1.next()){
	 
 String custID = rs1.getString("klient").trim();
 double xx = rs1.getDouble("valid_points");
 double yy = rs1.getDouble("unpaid_points");
 
	   points.put(conv(custID),"Sukaupti taškai: " + (int) xx + " (=" + (int) xx + "€ dovanų kortelei)" + "|Įsigalios apmokėjus SF: " + (int)yy); 
  }
 
 rs1.close();
 stmt1.close();
 con.close();
 
 System.out.println("points " + points.size());
 
} catch (Exception e){
  e.printStackTrace();
}


/// KLIENDI ANDMETE LEIDMINE

    	      
    	      docCodes = new HashSet();
    	     TreeMap tm = new TreeMap();
    	     this.customers = new ArrayList();


    	   String sql =  "   SELECT k.[Name] AS nimi,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address]+' ' +k.[Address 2]+k.[City] " +
    	   	 "   AS aadress , a.[Address]+' '+a.[Address 2]+a.[City] as addr2 ,k.[Salesperson Code] AS agent ,  Blocked AS blokk,   " +
             "    [Payment Terms Code] AS payment,'' AS alekood, [Credit Limit (LCY)] AS krediit,[Route Code] AS visit,'' AS type,  " +
             "  0 AS extradays ,'' AS sort,'' AS debt , 0 AS minsum ,k.Contact + '|' + k.[Phone No_] + '|' + k.[E-Mail] as kontakt  , " +
			 "  [Customer Price Group] as alegr ,k.[Customer Disc_ Group] as alegr2 ,av.AVG_Line_Count as lines,av.AVG_Amount as summa,av.Last_Invoice_Date as lastdeal , [Lojalumo programa] as loyal " +
             "  	  , k.[Address] as a1,k.[Address 2] as a2,k.[City] as a3, k.[Country Code]as coun , k.[Secondary Salesperson Code] as ag2 FROM [2015_Tobis$Customer] k LEFT join [2015_Tobis$Ship-to Address] a on  a.[Customer No_]=k.[No_] " +
              "     and a.Code=k.[Ship-to Code]  " +
				"   LEFT join [_MERISOFT$Tobis$Customer_Average_Invoice] av on  av.[Customer_No]=k.[No_] " +
				 "   WHERE k.[Salesperson Code] != ''  AND Blocked != 3" ;

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
    	           		payer= conv(payer.trim());
    	           		
    	           		if(payer.equals("JUTA")) {
    	           			payer = "JUTA1";
    	           		}
    	             customer.setPayer(payer);
    	             
    	            
    	            	 
    	             
    	             if(overdebts.get(payer) != null){
    	            	 customer.setDebt(Double.parseDouble((String)overdebts.get(payer)));
    	              }
    	             
    	             if(usedcredit.get(payer) != null){
    	            	    customer.setUsedCredit(Double.parseDouble((String)usedcredit.get(payer)));
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
      	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadr2),"Lihtuania"));
      	               name = name + " " + aadr2;
      	               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadr2));
      	             }else if( aadress != null && aadress.trim().length()>0){
    	               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Lihtuania"));
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
    	          //   customer.setDiscount(Double.parseDouble((num.trim())));
    	           	}
    	           	
    	           	int kred = rs.getInt("krediit");
    	           	if(kred>0){
    	             customer.setCredit(kred + "");
    	           	}
    	           	
    	           	
    	            String visit = rs.getString("visit");
    	            
     	             if(visit != null && visit.trim().length()>0){
     	        	  customer.setVisit(Converter.getVisitDayLetter(visit));
     	             }
     	               
    	       
    	          
    	           if(paym != null && paym.trim().length()>0){
    	        	   paym = paym.replaceAll("D"," ");
    	               paym = paym.replaceAll("PÄEVA"," ");
    	               paym = paym.replaceAll("PäEVA"," ");
    	               paym = paym.replaceAll("SR","-1").trim();
    	        	customer.setPaymTerm(paym);
    	             }

    	         
    	           String cont = rs.getString("kontakt");
    	           if(cont != null && cont.trim().length()>0){
    	               customer.setPersonalData(Converter.enCodeEst(cont.trim().replaceAll("\n", " ").replaceAll(System.getProperty("line.separator")," ").replaceAll("\r", " ")));   
    	           }
    	           if(contacts.get(customer.getID()) != null){
    	        	   if(customer.getPersonalData() != null && customer.getPersonalData().trim().length() > 0){
    	        		   customer.setPersonalData(customer.getPersonalData() + "|" + contacts.get(customer.getID()));
    	        	   }else{
    	        		   customer.setPersonalData((String)contacts.get(customer.getID())); 
    	        	   }
    	           }
    	           
    	           
    	           if(payer != null && lisainfo.get(payer) != null){
  	            	 String inff = (String)lisainfo.get(payer);
  	    	        	   if(customer.getPersonalData() != null && customer.getPersonalData().trim().length() > 0){
  	    	        		   customer.setPersonalData(customer.getPersonalData() + "|" + inff);
  	    	        	   }else{
  	    	        		   customer.setPersonalData(inff); 
  	    	        	 }
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
    	          
    	           String lines  = rs.getString("lines");
    	           if(lines != null && lines.trim().length()>0){
    	        	   double summa   = rs.getDouble("summa");
    	        	   String ldate  = rs.getString("lastdeal");
    	        	    	        	   
    	        	   
    	        	   customer.setInfo(Converter.enCodeEst("Paskutinė SF data: " + ldate.substring(0,10) + "|Vidutinė SF (iš 4 paskutinių):|" 
                   +  "*  Eilučių sk.:" + lines +"|  " +
    	        	   		"   Suma: " + (int)summa + " €." ));
    	           }
    	          
    	           String loyal  = rs.getString("loyal");
    	           if(loyal != null) {
    	        	   if( loyal.trim().equals("0")) {
    	        		   customer.setCity("Lojalumo statusas: Nedalyvauja");
    	               }else {
    	            	   String add = "";
    	            	   if(points.get(customer.getID()) != null) {
    	            		   add = (String)points.get(customer.getID());
    	            	   }
    	            	   customer.setCity("Lojalumo statusas: Taškai kaupiami|" + add );
    	                 }
    	        	   
    	           }
    	           
    	           String a1 = rs.getString("a1");
    	           if(a1 != null && a1.trim().length()>0){
    	        	   String str = a1.trim();
    	        	   String a2 = rs.getString("a2");
        	           if(a2 != null && a2.trim().length()>0){
        	        	   str = str + " " + a2.trim();
        	        	   
        	           }   
        	           customer.restStreet = str;
    	           }
    	           String a3 = rs.getString("a3");
    	           if(a3 != null && a3.trim().length()>0){
    	        	   customer.restCity = a3.trim();
    	           }
    	           String a4 = rs.getString("coun");
    	           if(a4 != null && a4.trim().length()>0){
    	        	   customer.setCountry(a4.trim());
    	           }
    	          // customer.setPayer(id);
    	           
    	           
    	       	String ag2 = rs.getString("ag2");
    	       	if(ag2 != null && ag2.trim().length() > 0) {
    	       		customer.addAgent(new Agent(ag2));
    		         	
    	       	}
	             
    	            tm.put(Converter.cleanLitLatEst(id) + customer.getName(), customer);
    	            this.customers.add(customer.getID());
    	            if(!docCodes.contains(customer.getID())){
               	     docCodes.add(customer.getID());
                  }
    	           
    	         }

    	        rs.close();
    	        stmt.close();
    	        con.close();



    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	     
    	     String inP = parent.getProps().getProperty("doc.origpath");
    	      String outP = parent.getProps().getProperty("doc.destination");
    	      Converter.prepareDocCatalogue(inP, outP, docCodes, true);
    	     return tm;
    	   }

   public TreeMap getAgents1() {
	   
		return agents1;
	}
   
   HashSet docCodes = new HashSet();
   private TreeMap agents1 = new TreeMap();
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();

     docCodes = new HashSet();

     
     TreeMap  ladutr = new TreeMap();
      TreeMap  ladulah = new TreeMap();
     
     try {
   	  String cmd = "select [Item No_] as kood, inventory as kogus,PickupQty from [_MERISOFT$Tobis_ProdStock]" ;
   	  
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
     
     Hashtable dateOut = new Hashtable();
    


    
     
  
     try {
     
    	 String lubaSQl = " SELECT [No_] as toode from [2015_Tobis$Item] where  [Show in mob_ device] = 1";
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
           
           
           
         String sql3 = "  select distinct ItemNo from  ( select sp.[Item No_] as ItemNo from [2015_Tobis$Sales Price] sp " +
         "  left join [2015_Tobis$Item] i on i.No_=sp.[Item No_] where sp.[Sales Type]=2 and CONVERT (date, GETDATE())between sp.[Starting Date] and sp.[Ending Date] " +
          " and i.[Sell Out]=0  union  select i.No_ as ItemNo from [2015_Tobis$Item] i where i.Promotion=1 and i.[Sell Out]=0) f ";

 HashSet top = new HashSet();
    	 
    	  con = getNewConnection2();
          stmt = con.createStatement();
          rs = stmt.executeQuery(sql3);
         while(rs.next()){
        	  String id = rs.getString("ItemNo").trim();
        	  top.add(id);
         }
         System.out.println("top suurus " + top.size());
           rs.close();
           stmt.close();
           con.close();
           
           
           
           
    	 String cmd = "  SELECT  t.[No_] AS toode,(t.[Description] + ' ' +   t.[Description 2] ) AS nimi, t.[Unit Price] AS myygihind,"
    	 		+ " '' AS grupp2,t.[Global Dimension 2 Code] AS grupp " +
			" ,t.[Base Unit of Measure] AS unit,[No_ 2] AS ean,'' AS status ,0 AS maxdiscount, " +
			 " '' AS type, [Units per Parcel] AS kastis,[Item Disc_ Group] as alegr ,  '' AS kaal, "
			 + "[VAT Prod_ Posting Group] AS vat ,[Units per Pallet] as pallet " +
			 " ,[Unit List Price] as listprice,[New until] as newprod ,[Nelikvidas]  as status2 , [Minimum Unit Price]  as jaeh FROM [2015_Tobis$Item] t  WHERE t.[Blocked]=0    ORDER BY t.[No_] ";
      
    	 System.out.println(cmd);
     	 con = getNewConnection2();
        stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            String weight ="";
            Product prod = new Product(id);
         	String name = Converter.enCodeEst(id + " " + rs.getString("nimi").trim());
         	
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
              if(ladutr.get(id) != null ){
            	  prod.setStock(new Double((String)ladutr.get(id)).doubleValue());
              }else{
            	  prod.setStock(0);
              }
              
              if(ladulah.get(id) != null ){
            	  prod.setPartii("PickedQty: " + (String)ladulah.get(id));
              }
             //   String grupp2 = cleardots(rs.getString("grupp2"));          
          	
      		//if(grupp2 != null && grupp2.trim().length() >0){
      			//prod.setGroupSecond(grupp2.trim());	
      		//}
              
              String grupp = cleardots(rs.getString("grupp"));
        		if(grupp != null && grupp.trim().length() >0){
        			prod.setGroup(Converter.cleanLitLatEst(grupp.trim()));	
        		}
        	
        		
                prod.setUnit(rs.getString("unit"));
              String ean = (rs.getString("ean"));
                 if (ean != null && ean.trim().length()>0){
                   prod.setEAN(ean);
                 }
                    
                    if(top.contains(prod.getID())){
                    	prod.setStatusTop("1");
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
                		prod.setAlegrupp(Converter.cleanLitLatEst(alegrupp));	
                	}
                 


          	 String vat = rs.getString("vat");
          	 int vati = 21;
                   if(vat != null && vat.trim().length()>0){
                	 	prod.setVat(vat.trim());
                	 	try{
                	 	//	vati = Integer.parseInt(vat.trim());	
                	 	}catch(Exception e){
                	 		
                	 	}
                	 
                   }
               
                   double pallet = rs.getDouble("pallet");
                   if(pallet >0){
                	   prod.setOnPallet(pallet);
                   }
                   
                   double listprice = rs.getDouble("listprice");
                   if(listprice >0){
                	   prod.setPriceSecond(Converter.Round((float)(listprice * 1.21), 2));
                   }
                   
                   Date newprod = rs.getDate("newprod");
                   if(newprod.after(new Date())){
                	//   prod.setStatusNew("1");
                	   prod.setMustBe("1");
                   }
                   try {
                	   int status = rs.getInt("status2");
                       if(status > 0) {
                    	   prod.setStatusSellout("1");
                       }   
                   }catch(Exception e) {
                	   
                   }
                 
                   try {
                	   double status = rs.getDouble("jaeh");
                       if(status > 0) {
                    	   prod.addPartii("|  Min pard. kaina: " + getNF().format(status) + "€");
                       }   
                   }catch(Exception e) {
                	   
                   }
                   
                   
                   if(luba.contains(prod.getID()) && !name.equals("")){
                	   prod.setName(Converter.enCodeEst(name));
                       tm.put(name, prod);  
                       String normcode = prod.getID().replace('/', '_');
                       if(!docCodes.contains(normcode)){
                    	     docCodes.add(normcode);
                       }
                   }
          
        }
        
  	  String inP = parent.getProps().getProperty("doc.origpath");
      String outP = parent.getProps().getProperty("doc.destination");
      Converter.prepareDocCatalogue(inP, outP, docCodes, false);
      
      
   
      
      
        rs.close();
        stmt.close();
        con.close();
        
        StringBuffer buf = new StringBuffer();
         cmd = " SELECT [Sales Code] AS action,[Item No_] AS prodid, \r\n" + 
        		"      [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val from\r\n" + 
        		"       [2015_Tobis$Sales Price] where [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or\r\n" + 
        		"                year ([Ending Date]) = 1753)  and [Sales Type] = 3  \r\n" + 
        		"				union \r\n" + 
        		"    select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val \r\n" + 
        		"               from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 3 and [Starting Date] < getdate()-5  and ([Ending Date] > getdate()+1 or \r\n" + 
        		"                 year ([Ending Date]) = 1753) ";
      
    	 System.out.println(cmd);
     	 con = getNewConnection2();
         stmt = con.createStatement();
         rs = stmt.executeQuery(cmd);
        while(rs.next()){
        	 String  gruppx = rs.getString("action").trim();
           	String  toodex = rs.getString("prodid").trim();
           	try{
           		double  hind  = rs.getDouble("price");
            	double  disc  = rs.getDouble("disc");
             	double  kogus = 0;//rs.getDouble("kogus");
              
               	
                java.sql.Date end = rs.getDate("paev");
                long ttt = 0;
                if(end.after(new Date())) {
               	 ttt = end.getTime();
                }
               	if(kogus == 0){
               		//kogus = 1;
               	}
               	
               
               	
               	// siia teha number format 
               	
               	      buf.append(toodex+ ";" + gruppx + ";" + gruppx + ";" + NF.format(hind).replace(',', '.') + "; ;" + kogus + "; ;" + ttt + ";" + disc );
                       buf.append("\n");	
                       
                       
                /*   	id = st.nextToken();
                	 	actcode = st.nextToken();
                   	 	actname = st.nextToken();        	 
                       	String hind  = st.nextToken();
                       		actgrupp = st.nextToken();
                       		actamount = Double.valueOf(st.nextToken()).doubleValue();
                       			bonamount = Double.valueOf(st.nextToken()).doubleValue();
                       		end = Long.parseLong(st.nextToken());
                    	  disc = Double.valueOf(st.nextToken()).doubleValue();
                    	  cust = 
                   	*/
                       
                       
           		
           	}catch(Exception e){
           		
           	}
            
        }
        
        rs.close();
        
        
       
        // siin kliendi põhised koguselised
                 cmd = "  SELECT [Sales Code] AS action,[Item No_] AS prodid, \r\n" + 
                 		"        [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val, [Minimum Quantity] as am from\r\n" + 
                 		"         [2015_Tobis$Sales Price] where [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or\r\n" + 
                 		"                  year ([Ending Date]) = 1753)  and [Sales Type] = 0 and  [Minimum Quantity] > 1 "
                 		+ " union SELECT [Sales Code] AS action,[Item No_] AS prodid,  \r\n" + 
                 		"                 		        [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Sales Type] as val, [Minimum Quantity] as am from \r\n" + 
                 		"                 		        [2015_Tobis$Sales Price] where [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                 		                 year ([Ending Date]) = 1753)  and [Sales Type] = 3 " + 
                 		"					union \r\n" + 
                 		"	select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val ,[Minimum Quantity] as am\r\n" + 
                 		"                 from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 0 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                   year ([Ending Date]) = 1753) and  [Minimum Quantity] > 1 union "
                 		+ "select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,\r\n" + 
                 		"							[Sales Type] as val ,[Minimum Quantity] as am\r\n" + 
                 		"                 		                 from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] in (3) and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                 		                   year ([Ending Date]) = 1753)";
               
             	 System.out.println(cmd);
              	 con = getNewConnection2();
                  stmt = con.createStatement();
                  rs = stmt.executeQuery(cmd);
                 while(rs.next()){
                 	    String  gruppx = rs.getString("action").trim();
                    	String  toodex = rs.getString("prodid").trim();
                    	try{
                        double  hind  = rs.getDouble("price");
                     	double  disc  = rs.getDouble("disc");
                      
                         java.sql.Date end = rs.getDate("paev");
                         long ttt = 0;
                         if(end.after(new Date())) {
                        	 ttt = end.getTime();
                         }
                         String  val  = rs.getString("val").trim();
                         String aa = " ";
                         if(val.equals("3")) {
                        	 aa = gruppx;
                        	 gruppx = " ";
                         }
                     	double  kogus = rs.getDouble("am");
                        	if(kogus == 0){
                        		kogus = 1;
                        	}
                        	
                        
                        	
                        	// siia teha number format 
                        	
                        	      buf.append(toodex+ ";" + aa + ";" + " " + ";" + NF.format(hind).replace(',', '.') + ";" 
                        	      + " " + ";" + (int)(kogus+0.001) + "; ;" + ttt + ";" + disc + ";" + gruppx);
                                buf.append("\n");	
                                
                                
                         /*   	id = st.nextToken();
                         	 	actcode = st.nextToken();
                            	 	actname = st.nextToken();        	 
                                	String hind  = st.nextToken();
                                		actgrupp = st.nextToken();
                                		actamount = Double.valueOf(st.nextToken()).doubleValue();
                                			bonamount = Double.valueOf(st.nextToken()).doubleValue();
                                		end = Long.parseLong(st.nextToken());
                             	  disc = Double.valueOf(st.nextToken()).doubleValue();
                             	  custid
                            	*/
                                
                                
                    		
                    	}catch(Exception e){
                    		
                    	}
                     
                 }
                 
                 rs.close();
                 
                 // siin mitte kliendi põhised koguselised
                 cmd = "  SELECT [Sales Code] AS action,[Item No_] AS prodid, \r\n" + 
                 		"        [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val, [Minimum Quantity] as am from\r\n" + 
                 		"         [2015_Tobis$Sales Price] where [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or\r\n" + 
                 		"                  year ([Ending Date]) = 1753)  and [Sales Type] = 2 and  [Minimum Quantity] > 1\r\n" + 
                 		"					union \r\n" + 
                 		"	select [Sales Code] AS action,[Code] AS prodid,  0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday ,[Currency code] as val ,[Minimum Quantity] as am\r\n" + 
                 		"                 from [2015_Tobis$Sales Line Discount] where [Type] = 0 and [Sales Type] = 2 and [Starting Date] < getdate()+5  and ([Ending Date] > getdate()+1 or \r\n" + 
                 		"                   year ([Ending Date]) = 1753) and  [Minimum Quantity] > 1 ";
               
             	 System.out.println(cmd);
              	 con = getNewConnection2();
                  stmt = con.createStatement();
                  rs = stmt.executeQuery(cmd);
                 while(rs.next()){
                 	    String  gruppx = rs.getString("action").trim();
                    	String  toodex = rs.getString("prodid").trim();
                    	try{
                        double  hind  = rs.getDouble("price");
                     	double  disc  = rs.getDouble("disc");
                      
                         java.sql.Date end = rs.getDate("paev");
                         long ttt = 0;
                         if(end.after(new Date())) {
                        	 ttt = end.getTime();
                         }
                        	
                     	double  kogus = rs.getDouble("am");
                        	if(kogus == 0){
                        		kogus = 1;
                        	}
                        	
                        
                        	
                        	// siia teha number format 
                        	
                        	      buf.append(toodex+ "; ; ;" + NF.format(hind).replace(',', '.') + ";" 
                        	      + " " + ";" + (int)(kogus + 0.001) + "; ;" + ttt + ";" + disc + ";" );
                                buf.append("\n");	
                                
                 
                                
                    		
                    	}catch(Exception e){
                    		
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
TreeMap sorting = new TreeMap();
     
     try {
    	 String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate,0 as lastamount , t.[Search Description] as nimi" +
    	 		",(r.[Unit Price]*(100-r.[Line Discount %])/100) as lastprice , 1 as type " +
    		" FROM [2015_Tobis$Sales Shipment Line] r " +
    "  INNER JOIN [2015_Tobis$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [2015_Tobis$Item] t ON t.[No_]=r.[No_]  WHERE " +
    "  d.[Posting Date]> GETDATE()-500 " +
   " ORDER BY  1, 6, 2 ,4";
  
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
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
                    String nimi = rs.getString("nimi");
                    
                    double lastprice = rs.getDouble("lastprice");
                    int type = rs.getInt("type");
                    String typeS = " ";
                    if(type == -1){
                    	typeS = "EXP";
                    }
                    
                  Product prod = new Product(prodID);
                    CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,lastprice,0,0," ");
                    cp.setAbiKood(typeS);
                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);
                 sorting.put( custID + nimi + prodID , cp);
           }
         rs.close();
         stmt.close();
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [2015_Tobis$Sales Invoice Line] r " +
		  " INNER JOIN [2015_Tobis$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
		  " Order by d.[Sell-to Customer No_],r.[No_],d.[Posting Date] desc ";
          System.out.println(cmd2);
		   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
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
     Vector vcc = new Vector();
     TreeMap sorting2 = new TreeMap();
     

     Iterator iterax =sorting.keySet().iterator();
     while (iterax.hasNext()){
    	 String key = iterax.next() + "";
    	 CustProd cp = (CustProd)sorting.get(key);
    	 String asi = "XXX";
    	 if(cp.getAbiKood() != null && cp.getAbiKood().trim().length() > 0){
    			asi =  cp.getAbiKood();
    	 }
    	 sorting2.put(key + asi, cp);
     }
     
     Iterator itera =sorting2.values().iterator();
     while (itera.hasNext()){
    	 vcc.add(itera.next());
     }
     System.out.println("vcc suurus " + vcc.size());
     return vcc;
   }




   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "NaviTobis";
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
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:MERISOFT";
   
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
  	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [2015_Tobis$Cust_ Ledger Entry] " +
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
