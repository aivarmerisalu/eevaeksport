
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import ee.mobilejava.wrapper.SuperVisorLineWrapper;
import ee.mobilejava.wrapper.SupervisorInfoWrapper;
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


public class SAFMaagGruppDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFMaagGruppDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
     } catch (Exception e){
       parent.showError(e);
     }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;

  
  public String getProductSortingData(){
   	return null;
   }
  
      public String getDealerCustomerCSV(String agent){
       return null;
       }

      public Vector getFixedAssortmentData() {
    	  Vector lines = new Vector();
             return lines;
        }
      
	  public void connectionUp(){
	  }

	  
	  public void insertSuperVisorSQL(SupervisorInfoWrapper wr){}

	  public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	     	       }


	  
	  public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        
        String    sql = "select '' AS custid, pl.idgrpricelist AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,'' as begindate ,'' as enddate " +
             " FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id where pr.code not like '%arh%' " +
			 " and (getdate() between begindate and enddate or (year(begindate) = 1899 and   year(enddate) = 1899)) ORDER BY 1,2 ";
        
        String    sql2 = "	 select k.id AS custid, ''  AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,begindate ,enddate , DATEDIFF(dd, begindate, enddate) as days " +
         "    FROM grcpricelistrel pl INNER JOIN grcpricelist u on u.id=pl.idgrcpricelist INNER JOIN " +
		"	 dbo.GRCPriceLCompany cpl  on cpl.idgrcpricelist=pl.idgrcpricelist inner join grcompany k on k.id=cpl.idgrcompany INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id " +
		"	 where getdate()>begindate and getdate()<enddate  ORDER BY 1,2";
          Statement st = null;
          try {
            Connection con = getNewConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
       
             // String sss = rs.getString("SC18009");
              String discount ="";
            /*  if(discount == null){
              	discount = sss.trim();
              }else{
              	discount = discount.trim().replaceAll("%"," ").trim();
              }
			   */
              double price = rs.getDouble("price");
              String begin  = rs.getString("begindate");
              String end = rs.getString("enddate");
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if ( clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup.trim());
              }
              if(discount.trim().length()==0){
              	discount = "0";
              }
                mline.setDiscount(Double.parseDouble(discount));

              if (Double.parseDouble(discount) == 0 && price != 0) {
                mline.setPrice(price);

              }
              
              if (begin != null && begin.trim().length() > 0) {
                  mline.setStartDate(begin.trim().substring(0,10));
                }
              if (end != null && end.trim().length() > 0) {
                  mline.setEndDate(end.trim().substring(0,10));
                }
             lines.add(mline);
            }

            
          rs = st.executeQuery(sql2);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
       
             // String sss = rs.getString("SC18009");
              String discount ="";
            /*  if(discount == null){
              	discount = sss.trim();
              }else{
              	discount = discount.trim().replaceAll("%"," ").trim();
              }
			   */
              double price = rs.getDouble("price");
              String begin  = rs.getString("begindate");
              String end = rs.getString("enddate");
              int days = rs.getInt("days");
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if ( clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim());
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup.trim());
              }
              if(discount.trim().length()==0){
              	discount = "0";
              }
                mline.setDiscount(Double.parseDouble(discount));

              if (Double.parseDouble(discount) == 0 && price != 0) {
                mline.setPrice(price);

              }
              
              if (begin != null && begin.trim().length() > 0) {
                  mline.setStartDate(begin.trim().substring(0,10));
                }
              if (end != null && end.trim().length() > 0) {
                  mline.setEndDate(end.trim().substring(0,10));
                }
              if(days > 32){
            	  System.out.println("Päevad " + days);
            	  mline.setProdLevel("pr"); //m�rgime et on tegemist pika hinnaga mitte kampaania 
              }
             lines.add(mline);
            }
            
            
            rs.close();
            st.close();
            connectionDown();
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
        return lines;
      }


	  
	  public String getAssortmentStr(Agent agent){
	  	return null;
	  }

		public Vector getLastThreeData() {

			Vector result = new Vector();
			int cou = 0;
			try {
				Connection con = getNewConnection();
				Statement st = con.createStatement();

				String cmd2 = "SELECT kl.code AS kood FROM grcompany kl WHERE  ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";


				if (isDebug())
					System.out.println(cmd2);
				ResultSet rs = st.executeQuery(cmd2);

				while (rs.next()) {

					String accountnum = rs.getString("kood").trim();

					String sq1 = "SELECT top 5 d.docdate as kuupaev  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice  INNER JOIN grcompany k ON k.name=d.companyname " + 
				    " WHERE d.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0  and k.code = '" + accountnum +"'" +
					" GROUP BY k.code,d.docdate ORDER BY d.docdate desc";
					Connection con2 = getNewConnection();
					Statement st2 = con2.createStatement();

					ResultSet rs2 = st2.executeQuery(sq1);
					while (rs2.next()) {
						String kuup = rs2.getString("kuupaev");
						String sq3 = "SELECT t.code AS toode, r.quantity AS kogus " +
				 "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
				 "  INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +  
				 "   WHERE d.docdate='" + kuup + "' and k.code = '" + accountnum + "'  AND (r.articletype='P' OR r.articletype='G') " +
				 "   ORDER BY t.code,r.docdate ";
						if(cou == 0){
							System.out.println(sq3);
							cou++;
						}
						Connection con3 = getNewConnection();
						Statement st3 = con3.createStatement();
						ResultSet rs3 = st3.executeQuery(sq3);
						while (rs3.next()) {
							MatrixLine ml = new MatrixLine();
							ml.setCustID(accountnum);
							ml.setProdID(rs3.getString("toode").trim());
							ml.setPrice(rs3.getDouble("kogus"));
							ml.setStartDate(kuup.substring(0,10));
							result.add(ml);
						}
						rs3.close();
						st3.close();
						con3.close();
					}
					rs2.close();
					st2.close();
					con2.close();
				   }
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

	
      public String getSaveStockInfo(){

    	  String cmd = " SELECT pr.code AS toode , SUM(quantity) as ladu  FROM dcurbalancestore ss \r\n" + 
    	  	 		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in (37,36,47,54) AND quantity > 0 \r\n" + 
    	  	 		"	GROUP BY  pr.code "; 
      	 

		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {
		       	Connection con = getNewConnection();
		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("seis");
		           if(seis > 0){
		               products.append(kood +  ";");
		               products.append(" ;");
		               products.append(getParent().getNF().format(seis) + "; " +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
      
      
      
      
      public String getSaveDebtInfo() {
      
      String cmd3 = "SELECT d.idpartner AS klient, " +
		 "d.totalamount  - sum(case when m.amountbase is null then 0 else m.amountbase end ) AS summa FROM sdinvoice d " +
		 "INNER JOIN grcompany k on k.id = d.idgrcompany " +
		 "LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
		 "WHERE docstate !='F'  AND d.promptdate < getdate()  group by d.sourcedocno,d.idpartner, " +
		 "d.promptdate,d.totalamount  HAVING d.totalamount  - sum(case when m.amountbase is null then 0 else m.amountbase end )" +
		 " != 0 ORDER BY d.idpartner";
		

  		if (isDebug()) System.out.println(cmd3);
  		TreeMap custs = new TreeMap();

  		StringBuffer products = new StringBuffer();
  		try {

  	 	Connection con = getNewConnection();
  		Statement st = con.createStatement();
  		ResultSet rs = st.executeQuery(cmd3);

  		while (rs.next()) {

  		String kood = rs.getString("klient").trim();
  		double volg = rs.getDouble("summa");
  		Customer custw = new Customer (kood);
  		custw.setDebt(volg);
  		if(volg>0){
  			custs.put(kood,custw);
  		}
  		}
  		
  		

  		//siin pannakse peale PLOKKID
String cmd = "SELECT kl.id FROM grcompany kl " +  
		 "  WHERE case when message like '%mitte m%' then 1 else 0 end != 0 ";
  		rs = st.executeQuery(cmd);
  		while (rs.next()) {
  			String kood = rs.getString("id").trim();
  		    Customer cus =  (Customer)custs.get(kood);
  		    if(cus != null){
  		    	cus.setSalesBlock(true);
  		    }else{
  		    	Customer cusnew = new Customer(kood);
  		    	cusnew.setSalesBlock(true);
  		    	custs.put(kood,cusnew);
  		    }
  		 }

  		rs.close();
  		con.close();
  		} catch (Exception e) {
  		getParent().showError(e);
  		}
  		Iterator     codes = custs.values().iterator();
  		while (codes.hasNext()){
  		Customer cust = (Customer)codes.next();
  		String bl = "0";
  		if(cust.isSalesBlock()){
  			bl = "1";
  			}
  		 products.append(cust.getID() + ";" + getParent().getNF().format(cust.getDebt()) + ";" + bl + "\n");
  		}
  		
  	
  		return products.toString();

         }
       

 	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
 	  
 	  

 	  
 	    public String getPilotDayData(Agent agent){

 	        
  	       String sql =  " select d.id,case when substring(description,0,3)!='MJ' then 1 else 0 end as abi ,(d.companyname) AS nimi,(d.docdate) AS kuup ,(d.docno) AS number , (d.totalamount) as summa ,k.i_teostaja " +
  	    		    "  FROM sdorder d INNER JOIN grcompany k on k.id = d.idgrcompany " +
  	    			"	    WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND DAY(d.docdate)>=DAY(getdate()) and k.i_teostaja = ? "+ 
 					"		order by (d.companyname)";
  	    			     
  	        
  	StringBuffer sb = new StringBuffer();
  	  this.connectionUp();
  	try {
  		
          nf.setMinimumFractionDigits(2);
            	
         
  		Connection con = this.getNewConnection();
  	PreparedStatement stmt2 = con.prepareStatement(sql);
  	 stmt2.setString(1, agent.getID());
  	 String sep= System.getProperty("line.separator");
  	    ResultSet  rs = stmt2.executeQuery();
  	 double droidsum = 0;
  	 double muusum = 0;

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
  	 	    	sb.append("[" +nf.format(rs2.getDouble("amount"))+"] " 
  	 	       + rs2.getString("unit").trim()+ " " + rs2.getString("name").trim() + "\n");
  	 	    }
  	    
  	 	    rs2.close();
  	        stmt3.close();
  	        cc.close();
  	     	sb.append( "\n\n");
  	 	 	  
  	     
  		if(abi == 0){
  	  		droidsum = droidsum + summa;
  	  	}else{
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
  	return "";//sb.toString();

  	    }
  	  

 	  
 	  
 	   public String getPilotDayData2(Agent agent){

         StringBuffer sb = new StringBuffer();
  	   	
 	    String cmd = "SELECT  w.code AS agent ,SUM(r.amount)  AS summa " +
		    " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
		    " INNER JOIN grcompany cus  ON cus.id=d.idgrcompany " +
		    " INNER JOIN eremployee w ON w.id= cus.I_kliendihaldur " + 
		    " INNER JOIN grarticle t ON t.id= r.idgrarticle " +
		    " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND r.idgrunit!=1 " +
		    " AND (r.articletype='P' OR r.articletype='G') and  w.code='" + agent.getID()+ "' GROUP BY w.code";
	            
        String sql2 =  "select max(d.companyname) AS klient ,max(d.docno) AS arve, max(d.docdate) AS kuupaev , max(d.totalamount*exchangerate ) as summa " +
     " FROM sdorder d INNER JOIN eremployee w ON w.id=d.ideremployee " +
		   "  WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND DAY(d.docdate)=DAY(getdate()) " +
			" and substring(description,0,3)!='MJ'  and  w.code = '" + agent.getID()+ "'  GROUP BY d.docno";
	     
        System.out.println("siin");
          String header = " Tänased tehingud :\n\n";
          String header1 = " Jooksva kuu müük : ";
          boolean first = true;
          nf.setMinimumFractionDigits(2);
            try {
            	
            	
            	
            	
            	Connection con = getNewConnection();
	              Statement stmt = con.createStatement();
	              ResultSet	      rs = stmt.executeQuery(cmd);
	              if(rs.next()) {
	                sb.append(header1 + nf.format(rs.getDouble("summa"))+  " EUR\n\n");
	                
	              }
	              rs.close();
	              stmt.close();
	              con.close();
	         
            	
            	
            	
            	
	              Connection con2 = getNewConnection();
            	
                     Statement st = con2.createStatement();
                    ResultSet rs2 = st.executeQuery(sql2);
                        while (rs2.next()) {
                        	if(first){
                        		sb.append(header);
                        		first = false;
                        	}
                        	String nimi = rs2.getString("klient").trim() ;
                      
                      
                        sb.append(nimi  +
                        		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev").substring(0,10) + "] " +
                        		rs2.getString("summa") + "EUR summa "
                        		+  "\n\n");
                        }

                        
                        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
           
            return sb.toString();

    }

	  public void connectionDown(){
	  }

  
	
	  
    public TreeMap getAgents(){
  	
    String cmd = "select e.id as agent,  e.firstname + ' ' + e.surname as nimetus from eremployee e inner join EREmployeeCatRel r on e.id = r.ideremployee  \r\n" + 
    		"    	 where ideremployeecat = 75 " ;
  
    if (isDebug()) System.out.println(cmd);
  
   
    try {
        Connection  con = getNewConnection();     
        Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus");
          if(name == null) {
        	  name = agent.getID();
          }
          agent.setName(name);
          agent.setIsPilot(true);
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    stmt.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
  
    Agent hh = new Agent("HOUSE");
    hh.setIsPilot(true);
    hh.setName("Office");;
    agents.put("HOUSE", hh);
 
    return agents;
  }



      public String getDistribution() {
    return null;
      }


 

  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();
    
    if(rcompany != null){
		Converter.getManagersReplacements(aHash, rcompany, user , pass, false);
	 }
  
  
    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();
//       makeCurrentDistribution();
 	   Converter cc = new Converter();

 	   TreeMap taarad = new TreeMap();

 	 
 	   String cmd = "SELECT  d.idgrcompany  AS klient ,t.code ,SUM(r.quantity)  AS kogus ,sum(r.amount), max(t.name) as nimi " +
		  "  FROM gdocument d INNER JOIN sdinvoicerow r ON d.id=r.idsdinvoice  " +
		  "  INNER JOIN grcompany cus  ON cus.id=d.idgrcompany	" +
		  "  INNER JOIN grarticle t ON t.id= r.idgrarticle  " +
		  "	LEFT JOIN grarticleCatrel cat ON cat.idgrarticle=t.id " +
		  "    WHERE d.docdate >='2025-04-01 00:06:49.340'  "+
		  "    AND cat.idgrarticlecat = 116 GROUP BY d.idgrcompany,t.code HAVING SUM(r.quantity) != 0 and sum(r.amount)!= 0 ORDER BY  d.idgrcompany,t.code";
 	  
 	  ResultSet rs = st.executeQuery(cmd);
 	  while(rs.next()){
 		 String kl = rs.getString("klient").trim();
        
         double kogus = rs.getDouble("kogus");
         String name = rs.getString("nimi").trim();
         String asi = "["  + ((int)kogus) + "] " + name ; 
         if(taarad.get(kl) != null){
        	 String u = (String)taarad.get(kl);
        	 taarad.put(kl, u + "|" + asi); 
         }else{
        	 taarad.put(kl, asi); 
         }
         
 	  }
      rs.close();
 	
      TreeMap tellimused  = new TreeMap();
	   String cmd7 = " Select d.idgrcompany  AS klient ,(SOURCEDOCNO)as doknum,max(d.deliverydate) as dpaev ,count(r.amount)  AS kogus , " +
	   "sum(r.amount) as summa   FROM sdorder d INNER JOIN sdorderrow r ON d.id=r.idsdorder " +
	"	    INNER JOIN grcompany cus  ON cus.id=d.idgrcompany			    " +
	"	      WHERE d.deliverydate >=GETDATE()-2 GROUP BY d.idgrcompany,SOURCEDOCNO " + 
	"	      ORDER BY  d.idgrcompany,SOURCEDOCNO " ;
	   rs = st.executeQuery(cmd7);
	  while(rs.next()){
		 String kl = rs.getString("klient").trim();
        String doknr = rs.getString("doknum").trim();
        String kuup = rs.getString("dpaev").substring(0,10);
       double kogus = rs.getDouble("kogus");
       double summa = rs.getDouble("summa");
       String asi = "(" + doknr + ") " +kuup + " {ridu:"  + ((int)kogus) + "} " + ((int)summa)+ " �" ; 
       if(tellimused.get(kl) != null){
      	 String u = (String)tellimused.get(kl);
      	tellimused.put(kl, u + "|" + asi); 
       }else{
    	   tellimused.put(kl, asi); 
       }
       
	  }
        
	  
	  rs.close();
 	  
 	  String cmd2 = "SELECT  kl.id AS kood,ltrim(kl.name) AS nimi,(select name from grcompany where id=kl.idsettlcompany)as mnimi,kl.deliveryaddr AS address,'' AS visit " + 
    "  ,creditlimit AS limiit,kl.i_teostaja AS agent, I_Horeca_Myygiagent as agent2, " +
     "      (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone,'' AS ladu  " +
      "    ,idgrpricelist as grupp ,'' as sort ,case when message like '%mitte m%' then 1 else 0 end as block , message as info,'Transp.: ' + pr.name as transa  , " +
	"	 te.code as paymterm , e_mail as email ,idgrpricelistc as kamp , " +
	 " (select top 1 convert(varchar(30),description) + ' ' + convert(varchar(3),u_rent_kogus) from grcomment where i_rent=kl.id and i_rent is not null and (year(u_rent_lopp)= 1899 or (u_rent_lopp)> getdate()) ) as rentinfo " +
		"  FROM grcompany kl  LEFT JOIN grProject pr on pr.id=i_kaubaring LEFT JOIN grtermpayment te ON te.id = idgrtermpayment " +
	"	   WHERE  ltrim(kl.id)>0  and kl.i_teostaja is not null  and left(kl.code,1) != 'y' ORDER BY ltrim(kl.name)";

 	  
 	  
      if (isDebug()) System.out.println(cmd2);
       rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          String mnimi = rs.getString("mnimi");
          
          if(mnimi != null && mnimi.trim().length() >0 && name.endsWith(mnimi.trim())){
        	  name = mnimi.trim() + " " + name.substring(0,name.indexOf(mnimi.trim())).trim();
        	}
        
          
          cust.setName(Converter.enCodeEst(name));
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1),"Estonia"));
          String visit = rs.getString("visit");
          if(visit != null && visit.trim().length()>0){
            cust.setVisit(visit.trim());
          }
       
         String limiit = rs.getString("limiit");
         // System.out.println(limiit);
          if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	 }
          	}
          
          
          
          String ag = rs.getString("agent").trim();
          
          cust.addAgent(new Agent(ag));
          
          String ag2 = rs.getString("agent2");
          if(ag2 != null && ag2.trim().length() > 0) {
        	  cust.addAgent(new Agent(ag2));
          }
          
          cust.addAgent(new Agent("HOUSE"));
          
          Agent aa = (Agent)aHash.get(ag);
          if (aa != null){
          	if(aa.getDubbler()!= null && aa.getDubbler().size()>0){
          		for(int i = 0;i<aa.getDubbler().size();i++){
          			cust.addAgent(new Agent((String)aa.getDubbler().elementAt(i)));	
          		}
          	 }
          }
        
        
          cust.setPayer(rs.getString("payer").trim());
                  
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }
          
          

          String ladu = rs.getString("ladu");
          if(ladu != null && ladu.trim().length()>0){
          	cust.setWare(ladu.trim());
          }
          
         String grupp = rs.getString("grupp");
          if(grupp != null && grupp.trim().length()>0 && !grupp.trim().equals("1") &&!grupp.trim().equals("45") ){
          	cust.setGroup(grupp.trim());
          }
          
          String sort = rs.getString("sort");
          if(sort != null && sort.trim().length()>0){
          	//cust.addFixSortCode(sort.trim());
          }
          String plokk = rs.getString("block");
          if(plokk != null && plokk.trim().length()>0 && plokk.equals("1")){
          	cust.setSalesBlockT(true);
          }
          
          
          String info = rs.getString("info");
          if(info != null && info.trim().length()>0 && info.indexOf("mitte")==-1){
          	cust.setInfo(info.trim());
          }
         
          if(taarad.get(accountnum) != null){
        	  String taara = (String)taarad.get(accountnum);
        	  if(cust.getInfo() != null && cust.getInfo().trim().length()>0){
        		  cust.setInfo(info.trim() + "|Taara käes:|" + taara);
        	  }else{
        		  cust.setInfo("|Taara käes:|" + taara);  
        	  }
        	 
          }
          
          
          if(tellimused.get(accountnum) != null){
        	  String tellid = (String)tellimused.get(accountnum);
        	  if(cust.getInfo() != null && cust.getInfo().trim().length()>0){
        		  cust.setInfo(cust.getInfo() + "|Tuleviku tellimus:|" + tellid);
        	  }else{
        		  cust.setInfo("|Tuleviku tellimus:|" + tellid);  
        	  }
          }
          
        
          
          String transa = rs.getString("transa");
          if(transa != null && transa.trim().length()>0 ){
          	cust.setCity(transa.trim());
          }
          
          
          String paymterm = rs.getString("paymterm");
          if(paymterm != null && paymterm.trim().length()>0 ){
        	  try{
        		   	cust.setPaymTerm(Integer.parseInt(paymterm.trim()) + "");	  
        	  }catch(Exception e){  
        	  }      
          }
          
          
          String email = rs.getString("email");         
          if(email != null && email.trim().length()>0 ){
        	  email = email.replaceAll(";", ",");
        	  try{
        		 cust.setInfoEmail(email);  
        	  }catch(Exception e){
        	  }
           }
          

          
          String kamp = rs.getString("kamp");
          if(kamp != null && kamp.trim().length()>0 ){
        	if(cust.getGroup()== null || cust.getGroup().trim().length() == 0){
        		// cust.setGroup(kamp.trim());
        	}else{
        		// cust.setGroup(cust.getGroup() + "|" + kamp.trim());
        	}
        	cust.setCustType(kamp.trim());
        	 
           }
          
          
          String rentinfo = rs.getString("rentinfo");
          if(rentinfo != null && rentinfo.trim().length()>0 ){
        	if(cust.getCity()== null && cust.getCity().trim().length() == 0){
        		cust.setCity(cust.getCity() + " | " + Converter.deCode(rentinfo.trim()));
        	}else{
        		cust.setCity(Converter.deCode(rentinfo.trim()));
                
        	}
        	 
           }
          
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


      String cmd3 = "SELECT d.idpartner AS maksja, " +
		 "d.totalamount  - sum(case when m.amountbase is null then 0 else m.amountbase end ) AS summa FROM sdinvoice d " +
		 "INNER JOIN grcompany k on k.id = d.idgrcompany " +
		 "LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
		 "WHERE docstate !='F'  AND d.promptdate < getdate()  group by d.sourcedocno,d.idpartner, " +
		 "d.promptdate,d.totalamount  HAVING d.totalamount  - sum(case when m.amountbase is null then 0 else m.amountbase end ) != 0 ORDER BY d.idpartner";
		
	     if (isDebug()) System.out.println(cmd3);
	        ResultSet rs1 = st.executeQuery(cmd3);
	         while (rs1.next()) {
	           String id = rs1.getString("maksja").trim();
	           double debt =rs1.getDouble("summa");
	          
	           Iterator iter = custMap.values().iterator();
	           while(iter.hasNext()){
	          Customer cust = (Customer)iter.next();   	
	           	if(cust.getPayer().equals(id)){
	                cust.setDebt(cust.getDebt()+ debt);
	            //     System.out.println(id + "  " + debt);	
	           	}
	           }
	         }

         st.close();
         con.close();


    } catch (SQLException e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  
  private Connection getNewConnection(String url) throws SQLException{
	    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	  }
	  
  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "  SELECT id AS liik ,  name AS nimetus  FROM grarticlecat  WHERE left(code,1) ='0' ORDER BY code,name  ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
        		Converter.enCodeEst(rs.getString("nimetus").trim()));
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
      
        TreeMap   tm = new TreeMap();
    return tm;
  }

    



  public TreeMap getProducts(Agent agent){

    String cmd = " SELECT pr.code AS kood, rtrim(pr.name) + ' ' +  rtrim(pr.code)  AS nimi,  hi.price AS hind,U_unikood AS EAN ,cat.idgrarticlecat AS grupp,  \r\n" + 
    		"        pr.contentquantity AS tykke ,y.name AS yhik  , case when left(ltrim(pr.CODE),1) in ('0','2','3','4','6') then 'jahe' else 'kylm' end as tyyp \r\n" + 
    		"         FROM grarticle pr LEFT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id LEFT JOIN grpricelistrel hi ON \r\n" + 
    		"         hi.idgrarticle=pr.id LEFT JOIN grpricelist pl ON pl.id=hi.idgrpricelist LEFT JOIN   grarticlecat arca ON arca.id = cat.idgrarticlecat   \r\n" + 
    		"         LEFT JOIN grunit y ON y.id=pr.idgrunit  WHERE  \r\n" + 
    		"        ( left(pr.code,1)!='9' and pl.id in (1227,1224,1301,1449) and  (left(arca.code,1)= '0' or arca.id = 895) ) OR pr.code = 'T'  order by pr.code  " ;
    
  	 String cmd2 = " SELECT pr.code AS toode , SUM(quantity) as ladu  FROM dcurbalancestore ss \r\n" + 
  	 		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in (37,36,47,54) AND quantity > 0 \r\n" + 
  	 		"	GROUP BY  pr.code "; 

  	 

    
    String cmd3 = " select pr.code as toode, Ltrim(kir.name) +  " +
" case when U_Parimenne > getdate()-400 then ' P:' + ltrim(str(day(U_Parimenne)))+'.'+ltrim(str(month(U_Parimenne)))+  " +
" '.'+substring(ltrim(str(year(U_Parimenne))),3,4) else '' end + " +
" ' S:' as info , round(jaak.quantity,2) as jaak from DCurDRStockReserve jaak inner join " +
"    	 DRStockReserve kir on kir.id=jaak.iddrstockreserve inner " +
"    		join  grarticle pr on pr.id=jaak.idgrarticle order by pr.code " ;
    //siin on k�sa kas k�ik laod liidame kokku 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
     Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
       ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode").trim();
         
          String sto = rs2.getString("ladu");
          if(tooted.get(id) != null){
        	  Product toode = (Product) tooted.get(id);
        	  toode.setName(toode.getName() + " " + sto);
          }else{
        	  Product toode = new Product(id);
        	  toode.setName(sto);
        	  tooted.put(id,toode);
          }
         
          
         
        }
        
        
        
        ResultSet rs3 = st2.executeQuery(cmd3);
        Hashtable tooteinfo = new Hashtable();
          while(rs3.next()){
            String id = rs3.getString("toode").trim();
            Product toode = new Product(id);
        String info = rs3.getString("info");
        String kog = rs3.getString("jaak");
        if(kog.indexOf(".")!=-1){
        	kog = kog.substring(0,kog.indexOf("."));
        }
           if(info != null && info.trim().length()>0){
           	if(tooteinfo.containsKey(id)){
           	String partii = ((Product)tooteinfo.get(id)).getPartii() + "|" +info.trim().replaceAll(";", ":") + kog;	
           	toode.setPartii(partii);
           	}else{
           	  toode.setPartii(info.trim().replaceAll(";", ":")); 		
           	}
            
           
            tooteinfo.put(id,toode);
           }
         
          }
        
        
       // System.out.println(tooted.toString() + tooted.size());

      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product prod = new Product(kood);
        
        
        Product toode = (Product)tooted.get(kood);
        if(toode != null){
        	prod.setPartii( toode.getName());
        }

        Product info = (Product) tooteinfo.get(kood);
         if(info != null){
        	 if(prod.getPartii() != null && prod.getPartii().trim().length() >0){
        		 prod.setPartii(prod.getPartii() + " | " +info.getPartii()); 
        	 }else{
        		 prod.setPartii(info.getPartii());	 
        	 }
     	  
          }
         
         
         String nimetus = rs.getString("nimi").trim().replaceAll("Rannarootsi", "RR").replaceAll("Rannamõisa", "RM").replaceAll("marinaadis", "mar.")
        		 .replaceAll("küpsetuskotis", "küp.kotis");
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
       
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 5) {
           prod.setEAN(ean.trim());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }
         
 

         prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");
        
        prod.setType(rs.getString("tyyp"));
        prod.setName(Converter.enCodeEst(nimetus));
   
        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
          prod.setName(Converter.enCodeEst(nimetus + " mü: " +unit));
        }
    
        
    //    boolean topis = rs.getBoolean("tops") ;
      //  if(topis){
       // 	top.append(prod.getID() + ";" + prod.getName() + "\n");  	
        //}
     
        prod.setVat("24");
        
        products.put(nimetus, prod);
        //}
      }
      rs.close();
      st.close();
      con.close();
    

    } catch (Exception e){
      getParent().showError(e);
    }

    return products;
  }


  public Vector getCustomerProductsList(String agentCode){
  	
	  
    String sql1 = 	"SELECT k.id AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,DATENAME(month, r.docdate) as kuu ,r.unitprice AS lastprice  , " +
    "  t.U_SailAeg as aeg, (case when r.docdate+t.U_sailAeg >getdate() then 0 else 1 end) as days " +
  "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
  " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname  " +
  "  WHERE r.docdate>getdate()-50 " +
  "   GROUP BY k.id,t.code,r.quantity,r.docdate,r.unitprice ,t.U_SailAeg " +
"	ORDER BY k.id,t.code,r.docdate " ; 
    
    
    String sql2 = " SELECT k.id AS klient,t.code AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname " +
      "WHERE r.docdate > getdate()-30 " +
      " GROUP BY t.code,k.id ORDER BY t.code,k.id ";

         if (isDebug()) {
                System.out.println(sql1);
            }

            Vector custProds = new Vector();

            try {
            	
              Connection con = getNewConnection();
              PreparedStatement stmt = con.prepareStatement(sql1);

              //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
              ResultSet	      rs = stmt.executeQuery();

              HashMap kliendid = new HashMap();
              while (rs.next()) {
                String kliendikood = rs.getString("klient").trim();
                String tootekood = rs.getString("toode").trim();
                double kogus = Double.parseDouble(rs.getString("kogus").trim());
                String lastdate = rs.getString("kuupaev").substring(0,10);
                 lastdate = lastdate.substring(8,10);
                 
                 String kuu = rs.getString("kuu").substring(0,3);
                        lastdate = lastdate + " " + kuu;
                        
                double hind = Double.parseDouble(rs.getString("lastprice").trim());               
                double aeg = rs.getDouble("aeg");
               
                Customer cust = (Customer)kliendid.get(kliendikood);
                 if (cust == null){
                  cust = new Customer(kliendikood);
                 }
                Product prod = new Product(tootekood);
                
                CustProd cp =  new CustProd(cust.getID(), prod.getID(), kogus, 0, 0, lastdate, hind,0,0," ");
                
                if (aeg >0){
                  	 cp.setRedline(rs.getString("days"));
                  }
                
                cust.addProduct(prod,cp,false);
                
                kliendid.put(kliendikood, cust);
              }
           
              rs.close();
              stmt.close();

              stmt = con.prepareStatement(sql2);
              rs = stmt.executeQuery();
              while(rs.next()){
                String klient = rs.getString(1).trim();
                String toode = rs.getString(2).trim();
                double pkogus = rs.getDouble(3);
                double mkogus = rs.getDouble(4);
                Customer c = (Customer)kliendid.get(klient);
                if (c != null){
                  ArrayList al = (ArrayList)c.getProducts().get(toode);
                  if (al != null){
                    CustProd cp = (CustProd)al.get(1);
                    if (cp != null){
                      cp.setMovedStockPluss(pkogus);
                      cp.setMovedStockMinus(mkogus);
                      custProds.add(cp);
                      //System.out.println(cp.getProdID());
                    }
                  }
                }
              }

              rs.close();
              stmt.close();
              con.close();

            

	           int counter = 0;
	           int cou = 0;
	          String oldklient = null;
	          String olddate = " ";

	          String cmd2 =  "SELECT k.id AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,DATENAME(month, r.docdate) as kuu " +
  "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
  " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname  " +
  "  WHERE r.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0  " +
  "  AND (r.articletype='P' OR r.articletype='G') " +
"	ORDER BY k.id,r.docdate desc,t.code "; // klient,kuupaev desc,toode
	          System.out.println(cmd2);
			   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
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
	                   date = rs2.getString("kuupaev").trim().substring(8,10);
	                   String kuu = rs2.getString("kuu").substring(0,3);
	                   date = date + " " + kuu;
	               if(cou == 0){oldklient = klient;olddate = date;}
	               if(!oldklient.equals(klient)){
	                             counter = 0;
	                             oldklient = klient;
	                             olddate = date;
	               }
	               
	               Customer c = (Customer)kliendid.get(klient);
	               if (c != null ){

	                 ArrayList al = (ArrayList)c.getProducts().get(toode);
	                 if(!olddate.toString().equals(date.toString())){
	                  	 counter++;
	                  	 }
	                 
	                 if (al != null){
	                   CustProd cp = (CustProd)al.get(1);
	                   if (cp != null){
	                     if (counter == 0){cp.setS1(kogus );}
	                     if (counter == 1){cp.setS2(kogus );}
	                     if (counter == 2){cp.setS3(kogus );}
	                     if (counter == 3){cp.setS4(kogus );}
	                     if (counter == 4){cp.setS5(kogus );}

	                     custProds.add(cp);
	                   }
	                 }
	                 oldklient = klient;
	                 olddate = date;
	               }
	               cou++;
	             }


            
            
            
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

  
 


  
  public void setBulkSales(Map agents){

    String cmd = "	SELECT  d.IDWorker  AS agent ,SUM(r.amount)  AS summa  " +
		"   FROM gdocument d INNER JOIN sdinvoicerow r ON d.id=r.idsdinvoice " +
		"    INNER JOIN grcompany cus  ON cus.id=d.idgrcompany	  " +
	    "	inner  JOIN grarticleCatrel cat ON cat.idgrarticle=r.idgrarticle  " + 
	    "	inner JOIN grarticleCat ca  ON ca.id=cat.idgrarticlecat and ca.category='0'	" +    
	    "	WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) " +
	    "	AND cat.idgrarticlecat != 116 and d.IDWorker is not null GROUP BY d.IDWorker ";

       if (isDebug()) System.out.println(cmd);
          try {
            Connection con = getNewConnection();
              Statement stmt = con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summa"));
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }
        }



  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }


  public DealerProduct getDealerProductsList() {
       return null;
      }



  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();
    String sql = "SELECT kl.id AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev,  d.docdate AS kuupaev2," +
    "  d.totalamount - sum(case when m.amountbase is null then 0 else m.amountbase end ) AS summa ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
    "  FROM sdinvoice d INNER JOIN grcompany kl on kl.id=d.idgrcompany " +
    " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
    "  WHERE docstate !='F'  and d.partnername not like '%Selver%' and d.partnername not like '%Comarket%' and d.partnername not like '%Maxima%' and d.partnername not like '%Rimi%' and d.partnername not like '%S��stu%'" + //AND d.promptdate <= getdate()
    "   group by  d.sourcedocno,kl.id,d.docdate,d.promptdate,d.totalamount  HAVING d.totalamount - sum(case when m.amountbase is null then 0 else m.amountbase end ) != 0  ORDER BY kl.id " ;

  if (isDebug()) System.out.println(sql);
  Connection  con = getNewConnection();     
  Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(sql);

    while(rs.next()){
      Debt debt = new Debt();
      debt.setCustID(rs.getString("maksja").trim());
      debt.setDoknr(rs.getString("arve").trim());
      debt.setDuedate(rs.getDate("kuupaev"));
      debt.setDate(rs.getDate("kuupaev2"));
      
      double summa = rs.getDouble("summa");
      debt.setDue(rs.getString("paevi"));
      
           debt.setSum(summa);
        list.addElement(debt);
      
    }
    rs.close();
    stmt.close();
    con.close();
    return list;
 }

    public ManagerData getManagerData() throws SQLException{
       ManagerData md = new ManagerData();
       return md;
     }


    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }
    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }

    public String getStartingTime() {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH,
              -Integer.parseInt(getParent().getProps().getProperty("months")));
      int month = cal.get(Calendar.MONTH) + 1;
      String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
      String sqlDate = cal.get(Calendar.YEAR) + month + day;
      return sqlDate;
    }

    public String getDriverVersion() {
      return "SAFMaagGrupp";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }




    public void insertDefective(DefectiveWrapper wr){}



    public void insertDistribution(DistEvalListWrapper wr){
    }


    

    public TreeMap getDealers() {
      return dealers;
    }

    public void setDealers(TreeMap aDealers) {
      dealers = aDealers;
    }

    public NumberFormat getNF() {
      return NF;
    }

    public void setNF(NumberFormat aNF) {
      NF = aNF;
    }


  private String url = "jdbc:odbc:SAFTEST";
 
  private TreeMap agents = new TreeMap();
  private   TreeMap aHash = new TreeMap();

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
