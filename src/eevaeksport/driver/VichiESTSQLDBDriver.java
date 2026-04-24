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

import org.json.JSONArray;
import org.json.JSONObject;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalLineWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;
import eevaeksport.util.ReportingService;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class VichiESTSQLDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,FixLoader {

  public VichiESTSQLDBDriver(EksportPanel parent) {
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

	        
	       String sql =  " select d.id,case when substring(description,0,3)!='MJ' then 1 else 0 end as abi ,(d.companyname) AS nimi,(d.docdate) AS kuup ,(d.docno) AS number , (d.totalamount) as summa ,k.i_agent " +
	    		    "  FROM sdorder d INNER JOIN grcompany k on k.id = d.idgrcompany " +
	    			"	    WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)>=MONTH(getdate())"
	    			+ " AND (day(d.docdate)= day(getdate()) or day(d.docdate)= day(getdate()+1)) and k.i_agent = ? "+ 
					"		order by (d.companyname)";
	    			     
	        
	StringBuffer sb = new StringBuffer();
	  this.connectionUp();
	try {
		
		 String header = " T�nased,homsed tehingud :\n\n";
       String header1 = " Jooksva kuu m��k : ";
       boolean first = true;
       NF.setMinimumFractionDigits(2);
         	
      
		Connection con = this.getNewConnection();
	PreparedStatement stmt2 = con.prepareStatement(sql);
	 stmt2.setString(1, agent.getID());
	 String sep= System.getProperty("line.separator");
	    ResultSet  rs = stmt2.executeQuery();
	 double droidsum = 0;
	 double muusum = 0;
	 boolean change = false;

	 boolean droid = true;
	 
	 while(rs.next()){
		 int id = rs.getInt("id");
		 int abi = rs.getInt("abi");
		  	 
	     String nimi = rs.getString("nimi");
	     if (nimi == null){
	     nimi = " ";
	     } else {nimi = nimi.trim();}

	 	sb.append(nimi + "\n");
	 	sb.append(rs.getString("kuup").trim().substring(0,10));
	 	sb.append("  Nr." + rs.getString("number").trim() + "\t");
	 	double summa = Double.parseDouble(rs.getString("summa").trim());
	 
	 	
	 	String sum = getNF().format(summa) ;
	     sb.append(sum + " eur\n" + sep);
	   
	     String sql3 = "select quantity as amount, case when idgrunit = 6 then 'kg' else 'tk' end as unit  , name from sdorderrow where idsdorder = " + id;
	     Connection cc = this.getNewConnection();
	     PreparedStatement stmt3 = cc.prepareStatement(sql3);
	 	 
	        ResultSet  rs2 = stmt3.executeQuery();
	    
	 	    while(rs2.next()){
	 	    	sb.append("[" +NF.format(rs2.getDouble("amount"))+"] " 
	 	       + rs2.getString("unit").trim()+ " " + rs2.getString("name").trim() + "\n");
	 	    }
	    
	 	    rs2.close();
	        stmt3.close();
	        cc.close();
	     	sb.append( "\n\n");
	 	 	  
	     
		if(abi == 0){
	  		droidsum = droidsum + summa;
	  	}else{
	       change = true;
	  		muusum = muusum + summa;
	  		if(droid && droidsum > 0 ){
	  			droid = false;
	  			sb.append("------------------------------\n");
	  			sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
	  		}
	  	}
	 }
	 rs.close();
	 stmt2.close();
   con.close(); 	 
		if(droid && droidsum > 0 ){
				droid = false;
				sb.append("------------------------------\n");
				sb.append("Androidist Kokku :" + getNF().format(droidsum)  + "eur\n\n");
			}
	 
	 if(muusum > 0){
			sb.append("------------------------------\n");
			sb.append("E-tellimused Kokku :" + getNF().format(muusum)  + "eur");
			 
	 }
	 
	} catch (Exception e) {
	 getParent().showError(e);
	}

	connectionDown();



	Converter.saveFile(parent.getProps().getProperty("stockrefdir") + "st"+ agent.getID() + "report.txt", sb.toString());
	//System.out.println(sb.toString());
	return "";//sb.toString();

	    }
	  
   public String getSaveDebtInfo() {
		  makePictureCat();
		  System.exit(0);
	   	return null;
	   }
   


	
	 private void makePictureCat(){

			String cmd2 = "SELECT t.[No_] AS toode  " +
	   "FROM [Vičiunai Baltic$Item] t WHERE [Blocked]=0 AND [Unit Price] > 0 AND len([Grupės kodas])>0 " ;

		    String picts = parent.getProps().getProperty("pict.dir");
           	String pictspda = parent.getProps().getProperty("pictpda.dir");

		   	try {
		   		
		   	//Kataloogi sisude kustutamine
		   	 File res2 = new File (pictspda);
	            String[] ff2 = res2.list();
	        	 for(int i = 0;ff2.length>i;i++){
	            	//(new File ( pictspda + ff2[i])).delete(); 
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
		                 
		                   if(f.isFile()){            	
		 		             	File inpda = new File ( pictspda + toode + ".jpg"); 
		 		             	if(!inpda.exists()) {
		 		             		try {
				 		             	Converter.shrinkAndCopyFile(f,inpda);
				 		             
				 		             	} catch(Exception e) {e.printStackTrace();}
		 		             	}
		 		           	len = inpda.length() + "";
		 		               }
		                 }else{
		                	 String[] gg = new File(picts).list();
				         	  
				            
				             for(int i = 0 ; gg.length > i ; i++){ 
				            	 
				            	 if(gg[i].startsWith(toode)){
				            		 
				            		System.out.println("gg " + gg[0] + " " + gg.length);
				            		 File in = new File ( picts + gg[i]); 
				            		
				            		
				            		 if(in.isFile()){            	
				 		             	File inpda = new File ( pictspda + toode + ".jpg"); 
				 		           	if(!inpda.exists()) {
				 		           		try {
				 		             	Converter.shrinkAndCopyFile(in,inpda);
				 		             	
				 		           		} catch(Exception e) {e.printStackTrace();}
				 		           	}
				 		           len = inpda.length() + "";
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
		               File f = new File(pictspda + "list.txt");
		               if (f.exists())f.delete();
		                 try {
		           		PrintWriter os = new PrintWriter(new FileWriter(pictspda +
		           				  "list.txt", true));
		           		os.write(buf.toString());
		           		os.close();
		           	  }catch (Exception ex) {
		           		ex.printStackTrace();
		           	  }
		          	
		     	
		   	} catch (Exception e) {
		   	    getParent().showError(e);
		   	}
		   
		}
		 
	
   
   public Vector getPriceMatrixData(){
		  
	  	 
	  	 String sql =" SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
		            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday from " +
		            "   [Vičiunai Baltic$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or " +
					"   year ([Ending Date]) = 1753) " +
					 "   and [Sales Type] = 0 " +
					 "	union " +
					"   SELECT '' AS custid,[Sales Code] AS clgroup,[Item No_] AS prodid,'' AS prodgroup, " +
		            "  [Unit Price] AS price ,0 as disc, [Ending Date] as paev, [Starting Date] as startday from " +
		            "   [Vičiunai Baltic$Sales Price] where [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1  " +
					"   )  and [Sales Type] in (1) " +
				
					"	union " +
				"		 select '' AS custid, [Sales Code] AS clgroup,'' AS prodid,[Code] AS prodgroup, " +
		        "       0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday " +
				"	    from [Vičiunai Baltic$Sales Line Discount] where [Type] = 1 and [Sales Type] = 1	" +
				"	 union " +
				"		 select '' AS custid, [Sales Code] AS clgroup,[Code] AS prodid,'' AS prodgroup, " +
		         "      0 AS price ,[Line Discount %] as disc , [Ending Date] as paev, [Starting Date] as startday " +
				"	    from [Vičiunai Baltic$Sales Line Discount] where [Type] = 0 and [Sales Type] = 1 and [Starting Date] < getdate()+6  and ([Ending Date] > getdate()+1 or " + 
			"		    year ([Ending Date]) = 1753) order by 1,2,7 desc ";

	       Vector result = new Vector();
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
	     
	             MatrixLine mline = new MatrixLine();
	             String check = "";

	             if (custID != null && custID.length() > 0) {
	               mline.setCustID(custID);
	               check = custID;
	             }
	             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
	               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
	               check = check + clGroup.trim();
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
	               mline.setProdID(prodID);
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
	            	      result.add(mline);
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
		
	    return result;
	      }



   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
   private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2, getParent().getProps().getProperty("dsna.user"), getParent().getProps().getProperty("dsna.pass"));
  }

   public void insertDefective(DefectiveWrapper wr){
  
  }
   
	public Vector getLastThreeData() {
		return null;
	}

	public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr) {
		String sql = "INSERT into dbo.supervisor values (" + "?, " // agent
																	// C(50),
				+ "?," // agentname C(200),
				+ "?," // clientid C(50),
				+ "?," // clientname C(200),
				+ "?," // prodid C(50),
				+ "?," // prodname C(200),
				+ "getdate() ," // sentdate D,
				+ " '' ," // receivedTime D,
				+ "?," // normalpr N,
				+ "?," // amoun N,
				+ "?," // grouptotal N,
				+ "?," // cattotal N,
				+ "?," // pertotcat I,
				+ "?," // percolcat I,
				+ "?," // perpalcat I,
				+ "?," // pershecat I,
				+ "?," // perrescat I,
				+ "?," // isaction L,
				+ "?," // ispartner L,
				+ "?," // isbrand L,
				+ "?," // isgroup L
				+ "?," // actionaeva N,
				+ "?," // actionbeva N,
				+ "?," // actiontota N,
				+ "?," // custeval N,
				+ "?," // custcomme C(200),
				+ "?," // comment C(200),
				+ "?," // custagent C(200),
				+ "?," // custagentname C(200),
				+ "?," // mustbe I,
				+ "?," // ideal I,
				+ "?," // resstr1 C(200),
				+ "?," // resstr2 C(200),
				+ "?," // resstr3 C(200),
				+ "?," // resint1 I,
				+ "?," // resint2 I,
				+ "?," // resint3 I,
				+ "?)"; // country L

		try {
			System.out.println("jam");
			Connection con = getNewConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			for (Iterator iter = wr.getInfo().getLines().iterator(); iter
					.hasNext();) {
				eevaeksport.report.SuperVisorLineWrapperMeri lw = new eevaeksport.report.SuperVisorLineWrapperMeri(
						iter.next());
				ps.setString(1, wr.getInfo().getAgentID());
				ps.setString(2, Converter.deCode(wr.getInfo().getAgentName()));
				ps.setString(3, lw.getLine().getCustID());
				ps.setString(4,  Converter.deCode(lw.getLine().getCustName()));
				ps.setString(5, lw.getLine().getProdID());
				ps.setString(6,  Converter.deCode(lw.getLine().getProdName()));
				ps.setDouble(7, lw.getLine().getNormalPrice() );
				ps.setDouble(8, lw.getLine().getAmount() );
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
				ps.setDouble(20, lw.getLine().getAction1Eval());
				ps.setDouble(21, lw.getLine().getAction2Eval());
				ps.setDouble(22, lw.getLine().getActionTotalEval());
				ps.setDouble(23, lw.getLine().getCustEval());
				String cc = lw.getLine().getCustComment();
				if (cc == null) {
					cc = " ";
				}
				ps.setString(24,  Converter.deCode(cc));

				String cd = lw.getLine().getComment();
				if (cd == null) {
					cd = " ";
				}
				ps.setString(25,  Converter.deCode(cd));

				String ce = lw.getLine().getCustAgentId();
				if (ce == null) {
					ce = " ";
				}

				ps.setString(26, ce);

				String cf = lw.getLine().getCustAgentName();
				if (cf == null) {
					cf = " ";
				}
				ps.setString(27,  Converter.deCode(cf));
				ps.setInt(28, lw.getLine().getDealNr());
				ps.setInt(29, lw.getLine().getIdealNr());

				String cr1 = lw.getLine().getResStr1();
				if (cr1 == null) {
					cr1 = " ";
				}
				ps.setString(30, cr1);

				String cr2 = lw.getLine().getResStr2();
				if (cr2 == null) {
					cr2 = " ";
				}
				ps.setString(31, cr2);

				String cr3 = lw.getLine().getResStr3();
				if (cr3 == null) {
					cr3 = " ";
				}

				

				ps.setString(32, cr3);
				ps.setInt(33, lw.getLine().getResInt1());
				ps.setInt(34, lw.getLine().getResInt2());
				ps.setInt(35, lw.getLine().getResInt3());
				ps.setString(36, "et");
				ps.executeUpdate();
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

    String tableName = "dayreport";
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


   public void insertDistribution(DistEvalListWrapper wr){
     
   }

 
   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
        
           
           try {
          	  String cmd = "  select s.[Item No_] as toode,s.[Assortment Code] + s.[Assortment Level] as grupp from " +
          	  		" [Vičiunai Baltic$Item Assortment Level] s INNER JOIN [Vičiunai Baltic$Item] t on t.No_= [Item No_] " +
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

   	DealerProduct dealerProds = new DealerProduct();
    try {
    	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Vičiunai Baltic$Item Ledger Entry] l " +
          " INNER JOIN [Vičiunai Baltic$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('TAL') " +
          " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupės kodas])>0 " +
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
  // 	sendDistributionReport(null);
   	System.exit(0);
    return null;
   }
   
   
   
   private TreeMap  getOpenOrders(){
		TreeMap res = new TreeMap();
			
		   String sql2 =  " select  [No_] AS id, '' as tyyp, [Sell-to Customer No_] AS kkood ,  [No_] as number ,[Shipment date] AS kuup , 0 as summa "
		   		+ "from [Vičiunai Baltic$Sales Header]  WHERE [Shipment date]  > getdate()  " ;
		
	 this.connectionUp();
	
	try {
	   NF.setMinimumFractionDigits(2);
	   Connection con = this.getNewConnection2();
	   PreparedStatement stmt2 = con.prepareStatement(sql2);
	   ResultSet  rs = stmt2.executeQuery();
	   StringBuffer sb = new StringBuffer();

	 while(rs.next()){
		
		String id = rs.getString("id");
		String text = rs.getString("tyyp").trim();
		String nimi = rs.getString("kkood");
	    
		if (nimi == null){
	    nimi = " ";
	    } else {nimi = nimi.trim();}

		//DPP00488444 (‘Pabeigts’ – 01.06.2018; merisoft).
	    sb.append(rs.getString("number").trim() + "  ");
	    String kuup =(rs.getString("kuup").trim().substring(0,10)) ;
		sb.append(Converter.turn(kuup) + " " + text + " ");
		
 	 sb.append("|" );	
		
 	 String summa = NF.format(rs.getDouble("summa"));
 	 
 	
	    String sql3 = "    select Quantity as amount,  [Unit of Measure] as unit  , Description as name \r\n" + 
	    		"	    						    FROM   [Vičiunai Baltic$Sales Line] \r\n" + 
	    		"	    							 where  [Document No_] =  '" + id + "' and  [Shipment date] >  getdate()  ";
	 //   System.out.println("sql 3 " + sql3 );
	    Connection cc = this.getNewConnection2();
	    PreparedStatement stmt3 = cc.prepareStatement(sql3);
	
	       ResultSet  rs2 = stmt3.executeQuery();
	   
		    while(rs2.next()){
		    	sb.append("[" +NF.format(rs2.getDouble("amount"))+"] " 
		       + rs2.getString("unit").replaceAll("\\r\\n|\\r|\\n", " ").trim()+ " " + rs2.getString("name").toLowerCase().replaceAll("\\r\\n|\\r|\\n", " ").trim() + "|");
		    }
	   
		    rs2.close();
	        stmt3.close();
	        cc.close();
	        
	       if(res.get(nimi) != null){
	    	   res.put(nimi,res.get(nimi) +" | |" + sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	     } else {
	    	   res.put(nimi, sb.toString().replaceAll("\\r\\n|\\r|\\n", " ").trim());
	   	   }
	   	sb = new StringBuffer();
	       
	}
	
	
	rs.close();
	stmt2.close();
	con.close(); 	 
	
	} catch (Exception e) {
	  getParent().showError(e);
	}

 	connectionDown();
 		
		return res;
	}
	
   

  HashMap ketid = new HashMap();
   private void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu päeva aruannet
	  	  
	  	 
	  	  System.exit(0);
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
     String cmd = " SELECT  [Kodas] AS kood,[Aprašas] AS nimi  FROM [Vičiunai Baltic$Prekių grupavimas] order by [Kodas]";
     	 
       	 Connection con = getNewConnection2();
     //  	System.out.println("asike " +cmd);
   	        Statement stmt = con.createStatement();
   	        ResultSet rs = stmt.executeQuery(cmd);
   	        while(rs.next()){
   	            String id = rs.getString("kood");
   	        if(cleardots(id).equals("28")){
   	        	id = "5.1.";
   	        }
   	                String name = rs.getString("nimi") ;
   	             if(id.trim().length()== 4  || id.trim().length() == 5) {
   	             tm.put(cleardots(id),  Converter.enCodeEst(name) + " " + id.substring(0,1) +" "+ cleardots(id) );
   	             }
   	        }
   	        
   	      rs.close();
   	      con.close();

         return tm;
   }



   public TreeMap getProdGroupsSecond() throws SQLException {
    TreeMap   tm = new TreeMap();
    String cmd = "SELECT  [Kodas] AS kood,[Aprašas] AS nimi  FROM [Vičiunai Baltic$Prekių grupavimas] order by [Kodas]";
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
		"[Vičiunai Baltic$Salesperson_Purchaser] WHERE len([Location Code])>0";
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
	        
	
	    } catch (Exception e){
	        getParent().showError(e);
	        System.exit(0);
	      }


     return agents;
   }

       public String getDistribution() {
         return null;
       }

private TreeMap agent1 = new TreeMap();

   public TreeMap getCustomers(Agent agent){
   HashSet custs = new HashSet();
   TreeMap openOrders = this.getOpenOrders();
 try {

try{
			   Converter.getManagersReplacements(agent1, rcompanyx, user, pass, false); 
				
		}catch(Exception e){
			
		}

   
      String cmd = " select a.[Sell-to Customer No_] AS klient, a.[Customer No_] AS maksja,a.[External Document No_] AS number, a.[Due Date] AS kuupaev, sum(b.[Amount]) AS summa," +
                            " DATEDIFF(dd,a.[Due Date],GETDATE()) AS due ,a.[Customer No_] AS payer , a.[Posting Date] as dd FROM [Vičiunai Baltic$Cust_ Ledger Entry] a inner join " +
                            " [Vičiunai Baltic$Detailed Cust_ Ledg_ Entry] b on a.[Entry No_]=b.[Cust_ Ledger Entry No_] " +
                            " where " +
							"  (a.[Document type] = 2 or a.[Document type]= 3) and a.[Due Date] < GETDATE()+7 AND a.[Posting Date] > getdate()-200 AND a.[Open]= 1 " +
                            " group by a.[Sell-to Customer No_],a.[Customer No_] ,a.[External Document No_], " +
                            " a.[Due Date] , DATEDIFF(dd,a.[Due Date],GETDATE()) ,a.[Customer No_],a.[Posting Date] having sum(b.[Amount])<> 0 order by  a.[Due Date]";

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
        debt.setPayerID(rs1.getString("payer"));
        debt.setDate(rs1.getDate("dd"));
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
       con.close();
       
       
       
	  cmd = "SELECT k.[No_] AS klient, [Balance total (LCY)]AS summa " +
	  " FROM [Vičiunai Baltic$Customer] k  WHERE k.[Salesperson Code] != '' AND [Balance total (LCY)]<>0 " ;
	  System.out.println(cmd);
	         con = getNewConnection2();
	         stmt1 = con.createStatement();
	         rs1 = stmt1.executeQuery(cmd);
	        while(rs1.next()){

	          String id = rs1.getString("klient");
	          Debt debt = new Debt ();
	         // debt.setDoknr(clear(st2.nextToken()));
	          debt.setDue(rs1.getString("summa"));
	          overdebts.put(id,debt);
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
  	TreeMap freeze = new TreeMap();

 	String ccc = "select  [Freezer Customer] as cust, No_ + ' ' +Description   " +
	" as last ,  [Last Date Modified] as dd from Navision.dbo. [Vičiunai Baltic$Fixed Asset]" ;
try{
	Connection cn = getNewConnection2();

	Statement stcn = cn.createStatement();
	ResultSet rscn = stcn.executeQuery(ccc);
	Vector kontroll = new Vector();
	while (rscn.next()) {
		String cid = rscn.getString("cust").trim();
		String name = rscn.getString("last");
		String dd = rscn.getString("dd").substring(0,10);
		name = name.trim() + " " + dd;
		if(freeze.get(cid) != null){
			Vector vv = (Vector)freeze.get(cid);
			if(!kontroll.contains(cid+name)){
				vv.addElement(name);
				freeze.put(cid, vv);
			}
			
		}else{
			if(!kontroll.contains(cid+name)){
				Vector vv = new Vector();
				vv.addElement(name);
				freeze.put(cid, vv);	
			}
			
		}
		kontroll.addElement(cid+name);
	}
	rscn.close();
	stcn.close();
	cn.close();
	

	
}catch(Exception e){
 e.printStackTrace();	
}



   String sql =  "  SELECT k.[Name]  AS nimi, k.[Name 2] AS nimi2 ,k.[No_] AS kood,k.[Bill-to Customer No_] AS payer,k.[Address] AS aadress ,k.[Salesperson Code] AS agent ,[Customer has Several VAT Codes] as toall,  " +
	" [Blokuoti SF išrašymą] AS blokk, " +
	" [Payment Terms Code] AS payment,[Line Discount %] AS alekood, [Balance total (LCY)] AS yle,[RouteCode] AS visit,[Customer type] AS type,  " +
	"   [Blokavimas po pradelstų dienų] AS extradays ,k.[Asortimento kodas]+k.[Assortment Level] AS sort,PDA_debt AS debt , 0 AS minsum ," +
	"   contact  as k1 , [Phone no_]  as k2 , k.[Home Page] as k3 , k.[E-Mail] as k4 , k.[Asortimento kodas] as kett  , k.tinklas as chain , " +
	" [Shipping Agent Code] AS rout  ,[Loading Code] as rout2 ,  [Customer Price Group] as hinnak " +
	"  FROM [Vičiunai Baltic$Customer] k LEFT JOIN [Vičiunai Baltic$Sales Line Discount] d ON d.[Sales Code] = k.[Customer Disc_ Group] " +
	"  WHERE k.[Salesperson Code] != ''  AND blocked != 3  " ;

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
           //System.out.println(line);
           Customer customer = new Customer(id);
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.setName(Converter.enCodeEst(name));
           	payer = rs.getString("payer");
           	if(payer != null && payer.trim().length()>0){
             customer.setPayer(payer.trim());
           //  customer.setCustType(payer.trim());
             }

             String aadress = rs.getString("aadress");
             if( aadress != null && aadress.trim().length()>0){
            	 aadress = aadress.replaceAll("  ", " ");
               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
               customer.setName(customer.getName()+ " " + Converter.enCodeEst(aadress));
             }
         	String ag = rs.getString("agent");
             customer.addAgent(new Agent(ag));
             if(ag != null){
             //   customer.setMainAgent(ag.trim());
                customer.addAgent(new Agent("21"));
                   customer.addAgent(new Agent("22"));
                   customer.addAgent(new Agent("23"));
             }
             
             if(openOrders.get(customer.getID()) != null){
            	 customer.addToCustDataVec(customer.getID() + ";tarasaldo; ;Open Order |" 
             +  openOrders.get(customer.getID()));
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
 					//	System.out.println(customer.getName() + customer.getAgents());
 					}
 				}
 			}
             
             String allagents = rs.getString("toall");
             if (allagents.trim().equals("1")) {
               Enumeration enume = getParent().getAgentsHash().elements();
               while (enume.hasMoreElements()) {
                 Agent a = (Agent) enume.nextElement();
                 customer.addAgent(a);
               }
             }
             
             customer.addAgent(new Agent("HOUSE1"));
        	 customer.addAgent(new Agent("LADU"));


             if(rs.getString("blokk").equals("1")){
               customer.setSalesBlock(true);
             }

            String paym = rs.getString("payment");

           	String num = rs.getString("alekood");
           	if(num != null && num.trim().length()>0){
           	double dd = Double.parseDouble(num.trim());
           	if(dd > 0)
             customer.setCity("Allahindlus : " + getNF().format(Double.parseDouble(num.trim())) + " %");
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

           if(freeze.get(customer.getID()) != null){
         	  Vector vv = (Vector) freeze.get(customer.getID());
         	  Enumeration enume = vv.elements();
         	  while (enume.hasMoreElements()){
         		  customer.setInfo(customer.getInfo() + "|" + enume.nextElement());
         	  }
           }else if(freeze.get(customer.getPayer()) != null){
         	  Vector vv = (Vector) freeze.get(customer.getPayer());
         	  Enumeration enume = vv.elements();
         	  while (enume.hasMoreElements()){
         		  customer.setInfo(customer.getInfo() + "|" + enume.nextElement());
         	  }
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
                         " (SELECT     SUM(de.[Amount (LCY)]) FROM [Vičiunai Baltic$Cust_ Ledger Entry] c INNER JOIN  " +
                         "  [Vičiunai Baltic$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
                         "   WHERE   c.[Customer No_] = k.No_ and[Due Date] <getdate()+1 ) WHEN k.[Bill-to Customer No_] <> '' THEN (SELECT   SUM(de.[Amount (LCY)]) " +
                         "   FROM  [Vičiunai Baltic$Cust_ Ledger Entry] c INNER JOIN " +
                         "    [Vičiunai Baltic$Detailed Cust_ Ledg_ Entry] de ON de.[Cust_ Ledger Entry No_] = c.[Entry No_] " +
                         "   WHERE  c.[Customer No_] = k.[Bill-to Customer No_] AND [OPEN] = 1 AND c.[Sell-to Customer No_] = k.No_ " +
						 " 	 and [Due Date] <getdate()+1    and c.[Posting Date] > getdate()- 100) " +
					     " 	 END AS yle FROM [Vičiunai Baltic$Customer] k "+
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
               paym = paym.replaceAll("PÄEVA"," ");
               paym = paym.replaceAll("PäEVA"," ");
               paym = paym.replaceAll("SR","-1").trim();
        	customer.setPaymTerm(paym);
             }

           
           
          //
           String kontakt = null;
           String cont1 = rs.getString("k1");
           if(cont1 != null && cont1.trim().length()>0){
               kontakt = cont1.trim(); 
           }
           
           String cont2 = rs.getString("k2");
           if(cont2 != null && cont2.trim().length()>0){
               if(kontakt == null) {
            	   kontakt = cont2;
               } else {
            	   kontakt = kontakt + "|" + cont2;  
               }
           }
           String cont3 = rs.getString("k3");
           if(cont3 != null && cont3.trim().length()>0){
        	   if(kontakt == null) {
            	   kontakt = cont3;
               } else {
            	   kontakt = kontakt + "|" + cont3;  
               }  
           }
           String cont4 = rs.getString("k4");
           if(cont4 != null && cont4.trim().length()>0){
        	   if(kontakt == null) {
            	   kontakt = cont4;
               } else {
            	   kontakt = kontakt + "|" + cont4;  
               }  
           }
           
           if(kontakt != null) {
        	   customer.setPersonalData(Converter.enCodeEst(kontakt));  
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
           
           String rout = rs.getString("rout");
           if(rout != null && rout.trim().length()>0){
           if(customer.getCity() != null && customer.getCity().trim().length() > 0){
             customer.setCity(customer.getCity() +"| VeoInfo : " + Converter.enCodeEst(rout.trim()));  
           }else{
             customer.setCity(" VeoInfo : "  +Converter.enCodeEst(rout.trim()));  
           }
              
           }
           
           String rout2 = rs.getString("rout2");
           if(rout2 != null && rout2.trim().length()>0){
           if(customer.getCity() != null && customer.getCity().trim().length() > 0){
             customer.setCity(customer.getCity() +"| VeoRing : " + Converter.enCodeEst(rout2.trim()));  
           }else{
             customer.setCity(" VeoRing : " + Converter.enCodeEst(rout2.trim()));  
           }
              
           }
           
           
            String hinnak = rs.getString("hinnak");
           if(hinnak != null && hinnak.trim().length()>0){
               customer.setGroup(hinnak.trim());  
               customer.setCustType(hinnak.trim());
           }
        //   " [Shipping Agent Code] AS rout  ,[Loading Code] as rout2 ,  [Customer Price Group] as hinnak 
           
           
           
          // customer.setPayer(id);
           if("Lithuanian".equals(lang) && !customer.getID().substring(0,1).equals("A")){
           	tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
              custs.add(customer.getID());
           }else if("Estonian".equals(lang) && !customer.getID().substring(0,1).equals("P")){
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
              custs.add(customer.getID());
           }
         }

        rs.close();
        con.close();
        
        
		            
        
			  HashSet asses = new HashSet();
			
				

		      if(this.rcompany != null){
		    	
		    	  
		    	     //Tõmbame alla kõikmis meil olemas
		      eevaeksport.util.ReportingService servu = new ReportingService();
		  		servu.user =  user;
		  		servu.pass = pass;
		  		servu.company = rcompany;   
		  		JSONArray assets = servu.getAssets(null);
		  		servu.closeClient();
		 
		  		
		  		HashMap asscusts = new HashMap();
		  	     HashSet cuus = new HashSet();	
		  		for (int i = 0; i < assets.length(); i++) {
					 JSONObject cob = assets.getJSONObject(i);
						 //   String id = cob.get("id") + "";
						    String code = (String)cob.get("code");
							//String name = (String)cob.get("name");
							
							try{

								JSONObject cust  = (JSONObject)cob.get("customer");
								 if(cust != null){
									 String kkood = (String)cust.get("code");
									// String kname = (String)cust.get("name");
									asscusts.put(code, kkood);
									cuus.add(code);
								 }	
							}catch(Exception e){
								
							}
				 }
			
		    	  
		    	  System.out.println("Asses size enne  " + asscusts.size());
		    	  Vector vec = new Vector(); 
		    	  String cmd3 = "SELECT [No_] as Code, [Description] as Name, '' as ean ,'' as nfcTag, [Freezer Serial No_] as factoryID ,[Freezer Model] as Type ,[Serial No_] as Description , " +
                            " [Įsigijimo data] as Born, [Serial No_] as Contract , [Freezer Customer] as custcode" + 
                            "  FROM [Vičiunai Baltic$Fixed Asset] WHERE ([Blocked] = 0) AND ([FA Subclass Code] = '11') "; // and qty_lv = 0
		    	  Connection con4 = getNewConnection2();
		    	Statement stm = con4.createStatement();
					
		    	  ResultSet rs1 = stm.executeQuery(cmd3);
		               int counter = 0;
		                 while(rs1.next()){
		                	
		                	 JSONObject obj = new JSONObject();
		                	 
		                        String code = rs1.getString("code").trim();
		                        obj.put("code", code);
		                        asses.add(code);
		                        try{
		                        	
		                        String name = rs1.getString("name");
		                        if(name == null){
		                        	name = code;
		                        }else{
		                        	name = name.trim();
		                        }
		                        
		                        obj.put("name", name);
		                        
		                        String ean = rs1.getString("ean");
		                        if(ean != null && ean.trim().length() > 0){
		                        	   obj.put("ean", ean);
		                        }
		                     
		                        String nfcTag = rs1.getString("nfcTag");
		                        if(nfcTag != null && nfcTag.trim().length() > 0){
		                     	   obj.put("nfcTag", nfcTag);
		                     }
		                        String factoryId = rs1.getString("factoryId");
		                        if(factoryId != null && factoryId.trim().length() > 0){
		                      	   obj.put("factoryId", factoryId);
		                          }
		                      
		                        String type = rs1.getString("type");
		                        if(type != null && type.trim().length() > 0){
		                        	type = type.trim();
		                        }else{
		                        	type = "";
		                        }
		                        
		                        String description = rs1.getString("description");
		                        if(description != null && description.trim().length() > 0){
		                      	   obj.put("description", (description + " "+type).trim());
		                      }
		                        java.sql.Date born = rs1.getDate("born");
		                        if(born != null ){
		                       	   obj.put("born", Converter.jsonDate(born));
		                       }
		                        
		                     //   java.sql.Date contract = rs1.getDate("contract");
		                        //if(contract != null ){
		                       	 //  obj.put("contract", Converter.jsonDate(contract));
		                      // }
		                    
		                        boolean goes = false;
		                        if(asses.contains(code)){
		                        	goes = true;
		                        }
		                        
		                        String ccode = rs1.getString("custcode");
		                        if(ccode != null && ccode.trim().length() > 0){
		                        	if(ccode.equals("1005")){
		                        		 obj.put("active", false);
		                        	}else{
		                        		 obj.put("active", true);
		                        	}
		                        	
		                        	if(ccode.equals("1005") || ccode.equals("1003")){
		                        		goes = true;
		                        		asses.add(code);
		                        	}
		                        	
		                      }
		                        
		                      
				                       
		                        if(goes && !cuus.contains(code)){
		                        	 vec.add(obj);
		                        	counter ++;
		                        }
		                           
		                          
		                        
		                        }catch (Exception r){
		                         r.printStackTrace();    
		                        }
		                        
		                       
		                        //java.sql.Date contract = rs1.getDate("contract");
		                      }
		                     rs1.close();
		                    
		    	  
		    	  
		                     
		                     System.out.println("Counter " + counter + " Asses " + asses.size());
		                     
		    	   Converter.sendAssetsJson(vec,this.user,this.pass,this.rcompany);     
		    	   
		                     
		                		
		             		
		             		
		    	   //siin saadame seosed ära jujul kui customer on olemas selline 
		     	   vec = new Vector(); 
		     	  cmd3 = "select [No_] as itemcode , '' as contract , [Freezer Customer]  as custcode " +
		  		   "  FROM [Vičiunai Baltic$Fixed Asset] WHERE ([Blocked] = 0) AND ([FA Subclass Code] = '11') and [Freezer Customer] != ''";
		           
		              int cou = 0;
		              int coua = 0;
		     	  rs1 = stm.executeQuery(cmd3);
		                  while(rs1.next()){
		                 	
		                 	 JSONObject obj = new JSONObject();
		                 	 coua++;
		                         String code = rs1.getString("itemcode").trim();
		                         obj.put("inventoryItemCode", code);
		                        // Date contract = rs1.getDate("contract");
		                         // obj.put("contractDate",contract.getTime() );
		                         String custcode = rs1.getString("custcode").trim();
		                         obj.put("customerCode", custcode);
		                         
		                       
		                         if(asses.contains(code) && custs.contains(custcode) ){
		                        	  String olemas = (String)asscusts.get(code);
		                        	  if(olemas == null){
		                                  vec.add(obj);  
		                                  cou++;  
		                         	  }else if(olemas != null){
		                        		 // if(!olemas.equals(custcode)){
		                        			   vec.add(obj);  
		                                       cou++; 
		                        		  //}
		                        	  }
		                          }
		                       }
		                  
		                      stm.close();
		                     con4.close();
		    	   System.out.println("assets cou " +  cou + " "+ coua);
		                      
		                      // siis peaks tegema vahele kontrolli et kui on muudatus siis ainult laksad ylesse
		    	   
		    	   
		    	   
		                    Converter.sendAssetsCustomerJson(vec,this.user,this.pass,this.rcompany);     
		               	   
		    	   
		    	   
		    	   
		      }
			
			
        



     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   public TreeMap getAgents1() {
		return agents1;
	}
   
   private TreeMap eanprods = null;
   private TreeMap agents1 = new TreeMap();
   private HashMap prodHash = new HashMap();
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
      TreeMap  ladutr = new TreeMap();
      prodHash = new HashMap();
     try {
   	  String cmd = "SELECT l.[Item No_] AS kood, sum(l.[Quantity]) AS kogus, l.[Location Code] AS ladu FROM [Vičiunai Baltic$Item Ledger Entry] l " +
         " INNER JOIN [Vičiunai Baltic$Item] t ON [No_]=l.[Item No_] WHERE  l.[Location Code] in ('TAL') " +
         " AND t.[Blocked]=0 AND t.[Unit Price]>0 AND len(t.[Grupės kodas])>0 " +
         " GROUP BY l.[Location Code],l.[Item No_] ORDER BY l.[Item No_] ";

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
  	      con.close();

  	    System.out.println(cmd);
   } catch (Exception e) {
       e.printStackTrace();
   }
     
     Hashtable dateOut = new Hashtable();
     StringBuffer buf = new StringBuffer();
     try {
     	String cmd = "SELECT [No_] AS toode,([Search Description]) AS nimi, [Unit Price] AS myygihind, [Grupės kodas] AS grupp,[Pogrupio kodas] AS grupp2 " +
			",[Base Unit of Measure] AS unit,[Brūkšninis kodas] AS ean,[Product Status] AS status ,[MaxDiscount] AS maxdiscount, "+
			  "[Type] AS type, [Qty_ per Unit of Measure] AS kastis,  [Net Weight] AS kaal, [VAT Prod_ Posting Group] AS vat, [Description] AS vatdesc " +
			 " , [Description 2] as info  FROM [Vičiunai Baltic$Item] LEFT JOIN [Vičiunai Baltic$Item Unit of Measure] vat ON (vat.[Item No_]=[No_] and vat.[Code]='KAST' ) " +
			 " WHERE ([Blocked]=0 AND [Unit Price]>0 AND len([Grupės kodas])>0 and  len([Search Description]) != 0)" +
			 " or [Pogrupio kodas] like '%2.08.01%' ORDER BY [No_] ";
     	
      System.out.println(cmd);
     	Connection con = getNewConnection2();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
            String id = rs.getString("toode").trim();
            double weight = 0 ;
            Product prod = new Product(id);
         	String name = rs.getString("nimi");
         	name =  "    " + name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
             double price = rs.getDouble("myygihind");
                prod.setPrice(price);
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
                 if (ean.trim().length()>0){
                   prod.setEAN(ean);
                   if(eanprods == null) {
                	 	eanprods = new TreeMap();
                   }
                   eanprods.put(ean.trim(), prod.getID());
                   
                 }
                    String xxx = rs.getString("status");
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
                                                      
                           

          	 String vat = rs.getString("vat");
                   if(vat != null && vat.trim().length()>0){
                     if(vat.equals("GRUPE4")){
                     	prod.setVat("18");
                     }else if (vat.equals("GRUPE2")){
                     	prod.setVat("5");
                     } else if (vat.equals("GRUPE20")){
                      	prod.setVat("20");
                     } else if (vat.equals("GRUPE22")){
                       	prod.setVat("22");
                      }
                     else if (vat.equals("GRUPE24")){
                        	prod.setVat("24");
                       }
                   }
                   prod.setVat("24");
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
           tm.put(name, prod);
           prodHash.put(prod.getID(), prod);
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
	 @Override
		public void FixLoader() {
		  eanprods =  Converter.loadMore();
		  
		  if(eanprods == null) {
			  eanprods = new TreeMap();
		  }
			this.getProducts(null);
			TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
			System.out.println("Contracts " + contracts.keySet().size() + " " + contracts.keySet().toString());
			if(contracts.size() > 0) {
				Converter.sendContractJson(contracts ,  rcompanyx,  user ,  pass);
			}
			
			
			System.exit(0);
		}
	
   
   private String getDir(){
	      String dir = getParent().getProps().getProperty("readin");
	      if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	      return dir;
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
     String cmd = " SELECT [No_] AS toode ,[Type] AS type FROM  [Vičiunai Baltic$Item] " +
     		" WHERE len([Type])>0  ORDER BY [Net Weight] , [Search Description]";
    try {
     	 Connection con = getNewConnection2();
     	System.out.println(cmd);
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd);
	        while(rs.next()){
        String id = rs.getString("toode");
        String type = rs.getString("type");
			//ABCD
			int i = type.trim().length();
			if(i>0){
			while (i != 0){
				sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
				i = i -1;
			 }
			}
	       }

	        rs.close();
	        con.close();
       } catch (Exception e){
      e.printStackTrace();
    }
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     HashMap kliendid = new HashMap();
     try {
    String cmd =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,sum(r.[Quantity]) AS kogus, d.[Posting Date] AS lastdate, max([Užsakytas kiekis]) as lastamount " +
    	"	FROM [Vičiunai Baltic$Sales Shipment Line] r " +
   " INNER JOIN [Vičiunai Baltic$Sales Shipment Header] d ON r.[Document No_]= d.[No_] INNER JOIN [Vičiunai Baltic$Item] t ON t.[No_]=r.[No_]  WHERE " +
   "   d.[Posting Date]> GETDATE()-60 " +
"	  group by d.[Sell-to Customer No_],r.[No_],d.[Posting Date],t.[Search Description] " +
"	  ORDER BY  d.[Sell-to Customer No_], t.[Search Description],r.[No_],d.[Posting Date]";
    System.out.println(cmd);
   //   "klient","toode","kogus","viimane kuupï¿½ev","viimane kogus"
    Connection con = getNewConnection2();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(cmd);
    while(rs.next()){

             String custID = rs.getString("klient").trim();

                 String prodID = rs.getString("toode").trim();;

                 String lastdate = " ";
               double  amount = rs.getDouble("kogus");
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
         con.close();


           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;

          String cmd2 =  "SELECT  d.[Sell-to Customer No_] AS klient,r.[No_] AS toode,r.[Quantity] AS kogus, d.[Posting Date] AS lastdate FROM [Vičiunai Baltic$Sales Invoice Line] r " +
		  " INNER JOIN [Vičiunai Baltic$Sales Invoice Header] d ON r.[Document No_]= d.[No_] WHERE d.[Posting Date]> GETDATE()-30 " +
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
     return "VichiESTSQL";
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
   private String url = "jdbc:odbc:vicireport";
   private String url2 = "jdbc:odbc:naviest";
   private String urlV = "jdbc:odbc:naviestview";
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

private Connection getNewConnectionView() throws SQLException {
	return DriverManager.getConnection(urlV, getParent().getProps()
			.getProperty("view.user"),
			getParent().getProps().getProperty("view.pass"));
}
  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

  	 try {
  	 	String cmd = " select [Salesperson Code] as agent ,sum([Sales (LCY)]) as summa from [Vičiunai Baltic$Cust_ Ledger Entry] " +
  	 			"where Month(getdate())=Month([Posting Date]) and " +
             "Year(getdate())=Year([Posting Date]) " +
              " and [Global Dimension 1 Code]='400' group by [Salesperson Code]";
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
