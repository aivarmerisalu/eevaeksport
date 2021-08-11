
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.report.DefectiveLineWrapperMeri;
import eevaeksport.report.SupervisorInfoWrapperMeri;
import eevaeksport.util.Converter;


public class SAFWiruDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFWiruDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = "WIRU " + getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = "WIRU " + parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      con3 = getNewConnection2();
      
      stmt = con.createStatement();
    } catch (Exception e){
       parent.showError(e);
       System.exit(0);
    }
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompanyx = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompanyx = null;
  
  
  

  public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = "   select  '' AS custid, pl.idgrpricelist AS clgroup,' 'as lisa,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,'' as begindate ,'' as enddate " + 
        		"            FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id INNER JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id " + 
        		"           INNER JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat where pl.idgrpricelist != 23 " + 
        		"			AND arca.category='PDA'  AND (articletype='G' OR articletype='P') AND right(pr.code,1)!='T' " +
		"	union " +
		"	   select '' AS custid, pl.idgrpricelist AS clgroup,' 'as lisa,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,'' as begindate ,'' as enddate " + 
		"            FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id INNER JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id " + 
		"           INNER JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat where pl.idgrpricelist = 23 and  getdate()>begindate and getdate()<enddate " + 
		"			AND arca.category='PDA'  AND (articletype='G' OR articletype='P') AND right(pr.code,1)!='T'" +
			" ORDER BY 1,2 " ;
		
        
        
        
        Statement st = null;
          try {
            Connection con = getNewConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

          
              String clGroup = rs.getString("clgroup");
              String lisa = rs.getString("lisa");
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

             
              if ( clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim() + "bb");
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
               // mline.setDiscount(Double.parseDouble(discount));

              if ( price != 0) {
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

            System.out.println("lines " + lines.size());
          

            String    sql2 = "		select k.code AS custid, ''  AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price ,begindate ,enddate , DATEDIFF(dd, begindate, enddate) as days " + 
            		"            FROM grcpricelistrel pl INNER JOIN grcpricelist u on u.id=pl.idgrcpricelist INNER JOIN " + 
            		"			 dbo.GRCPriceLCompany cpl  on cpl.idgrcpricelist=pl.idgrcpricelist inner join grcompany k on k.id=cpl.idgrcompany INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id " + 
            		"			 INNER JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id " + 
            		"           INNER JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " + 
            		"			 where getdate()>begindate and getdate()<enddate and u.id != 23 AND arca.category='PDA'  AND (articletype='G' OR articletype='P') AND right(pr.code,1)!='T' ORDER BY 1,2 ";
            
            
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
                mline.setCliGroup(clGroup.trim() + "bb");
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
          //    if(days < 32){
            	//  System.out.println("P�evad " + days);
            	//  mline.setProdLevel("pr"); //m�rgime et on tegemist pika hinnaga mitte kampaania 
            	  lines.add(mline);
            //  }
            
            }
            
            
            rs.close();
          
            
            String sql3 = " select '' AS custid, pl.idgrcompanypricecat AS clgroup,' 'as lisa,pr.code  AS prodid,' ' AS prodgroup,pl.pricecut AS disc ,'' as begindate ,'' as enddate " + 
            		"            FROM grpricecatrel pl left JOIN grarticle pr ON  pl.idgrarticlepricecat = pr.idgrarticlepricecat  left JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id " + 
            		"           left JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat where " + 
            		"			 arca.category='PDA'  AND (articletype='G' OR articletype='P') AND right(pr.code,1)!='T' and pl.pricecut  != 0";
            
            rs = st.executeQuery(sql3);

            while (rs.next()) {

              String custID = rs.getString("custid").trim();
              String clGroup = rs.getString("clgroup");
              String prodID = rs.getString("prodid").trim();
              String prodGroup = rs.getString("prodgroup");
              double disc = rs.getDouble("disc") * -1 ;
              String begin  = rs.getString("begindate");
              String end = rs.getString("enddate");
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if ( clGroup !=null && clGroup.trim().length() > 0) {
                mline.setCliGroup(clGroup.trim() + "bb");
              }

              if (prodID != null && prodID.trim().length() > 0) {
                mline.setProdID(prodID.trim());
              }
              if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
                mline.setProdGroup(prodGroup.trim());
              }
                mline.setDiscount(disc);
              
              if (begin != null && begin.trim().length() > 0) {
                  mline.setStartDate(begin.trim().substring(0,10));
                }
              if (end != null && end.trim().length() > 0) {
                  mline.setEndDate(end.trim().substring(0,10));
                }
          //    if(days < 32){
            	//  System.out.println("P�evad " + days);
            	//  mline.setProdLevel("pr"); //m�rgime et on tegemist pika hinnaga mitte kampaania 
            	  lines.add(mline);
            //  }
            
            }
            
            
            rs.close();
            st.close();

            
            
            
            connectionDown();
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
       System.out.println("price lines size " + lines.size());
          return lines;
      }



      public String getDealerCustomerCSV(String agent){
       return null;
       }

	  public void connectionUp(){
	  }

	  
	  public String getAssortmentStr(Agent agent){
	  	return null;
	  }

      public String getSaveStockInfo(){
      return null;
      }
      public String getSaveDebtInfo() {
       	return null;
       }
       
 	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
 	   public String getPilotDayData(Agent agent){

 	   	

        StringBuffer sb = new StringBuffer();
 	   	
 	   	
 	    String cmd = "SELECT  SUM(r.amount)  AS summa, sum(case when DAY(d.docdate)= DAY(getdate()) " +
 	    		" then r.amount else 0 end) as summa2, SUM(t.u_maht*r.quantity)AS summavol " +
	    " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
	   " INNER JOIN eremployee w ON w.id= d.idworker  " +
	   " INNER JOIN grcompanycat cat  ON cat.code=w.code " +
	   " INNER JOIN grarticle t ON t.id= r.idgrarticle" +
	   " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND r.idgrunit!=1 " +
	   " AND (r.articletype='P' OR r.articletype='G')  AND cat.id =" + agent.getID()+ "GROUP BY cat.id";

	       
	            
        String sql2 =  " select max(kliendikood) AS klient ,max(tellnr) AS arve, max(kuupaev) AS kuupaev , count (tellnr) as ridu " +
" from tp inner join ts on tp.tp_id=ts.tp_id  WHERE year(kuupaev)=year(getdate()) AND month(kuupaev)=month(getdate()) and " +
"day(kuupaev)=day(getdate()) "+
" and sisestaja =  '" + agent.getName() + "' " +
" group by tp.tp_id order by max(sisestaja)";
	     
   
        
        System.out.println("siin");
          String header = " T�nased tehingud :\n\n";
          String header1 = " Jooksva kuu m��k : ";
          String header3 = " T�nane m��k : ";
          boolean first = true;
          nf.setMinimumFractionDigits(2);
            try {
            	
            	
            	
            	
            	Connection con = getNewConnection();
	              Statement stmt = con.createStatement();
	              ResultSet	      rs = stmt.executeQuery(cmd);
	              if(rs.next()) {
	                sb.append(header1 + nf.format(rs.getDouble("summa"))+  " EUR\n\n");
	                sb.append(header3 + nf.format(rs.getDouble("summa2"))+  " EUR\n\n");
	                
	              }
	              rs.close();
	              stmt.close();
	              con.close();
	         
            	
            	
            	
            	
            	
            	
                     Statement st = con3.createStatement();
                    ResultSet rs2 = st.executeQuery(sql2);
                        while (rs2.next()) {
                        	if(first){
                        		sb.append(header);
                        		first = false;
                        	}
                        	String klient = rs2.getString("klient").trim() ;
                        	String sql3 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi " +
					            "   FROM grcompany kl " +
					            "  WHERE kl.code= '" + klient + "'";
                        	Connection con4 = this.getNewConnection();
                      Statement st3 = con4.createStatement();
                      ResultSet rs3 = st3.executeQuery(sql3);
                      String nimi = "";
                      if (rs3.next()){
                      	nimi = rs3.getString("nimi").trim();
                        	
                      }
                      rs3.close();
                      st3.close();
                      con4.close();
                      
                        sb.append(nimi  +
                        		"\n    (" + rs2.getString("arve").trim() + ")[" + rs2.getString("kuupaev").substring(0,10) + "] " + 
                        		getParent().getNF().format(rs2.getDouble("ridu"))  
                        	+" rida "	+  "\n\n");
                        }

                        
                        
                        } catch (Exception e) {
                            getParent().showError(e);
                        }
                        System.out.println(agent.getID() + sb.toString());
           if(sb.toString().trim().length()>0){
           	
            return sb.toString();
           }else {
           	return null;
           }
    }

	  public void connectionDown(){
	  }

  
	  private String getCode(String ag){
	    
	  	if(ag.equals("1309")){
	  		ag = "3";
	  	}
		if(ag.equals("1324")){
	  		ag = "4";
	  	}	
		if(ag.equals("1335")){
	  		ag = "6";
	  	}
		if(ag.equals("1308")){
	  		ag = "7";
	  	}
		if(ag.equals("1302")){
	  		ag = "8";
	  	}
		if(ag.equals("1301")){
	  		ag = "9";
	  	}
		if(ag.equals("1313")){
	  		ag = "10";
	  	}
		if(ag.equals("1337")){
	  		ag = "11";
	  	}
		
	  	return ag;
	  }
	  
    public TreeMap getAgents(){
  	
    String cmd = "SELECT gr.id AS agent, gr.name AS nimetus, gr.code as abikood ,U_asendaja AS dubbler " +
       " FROM  grcompanycat gr INNER JOIN eremployee er ON er.code=gr.code WHERE Category='myygimees'";
  
    if (isDebug()) System.out.println(cmd);
  

    try {
    	 Connection con2 = getNewConnection();
         Statement ps = con2.createStatement();
    	
      ResultSet rs = ps.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          String dubbler = rs.getString("dubbler");
          if(dubbler != null && dubbler.trim().length()>0){
          	StringTokenizer st = new StringTokenizer(dubbler,",");
          	while(st.hasMoreElements()){
          		String ag = st.nextToken().trim();
          		//System.out.println(ag);
          		ag = getCode(ag);
          		if(!agent.getID().equals(ag)){
              		agent.addDubbler(ag);
              		
              		System.out.println("dubbler added " + agent.getID() + " dub. " + ag);
          		}
          	}
          }
          agent.setName(name);
          agent.setIsPilot(true);
          agent.setObject("607");
          
          agent.setLanguage("EST");
          agent.setCountry("ee");
          agent.setPassWord("0000");
          String email = parent.parseFirstEmail(parent.getEmail(agent.getID()));
          if(email.length() > 3){
        	  agent.setEmail(email);
          }
          if(email.length() > 3){
        	  agent.setActive(true);
          }
       
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    ps.close();
    con2.close();

    } catch (Exception e){
        getParent().showError(e);
      }
    Agent aa = new Agent("HOUSE");
    aa.setName("Kontor");
    aa.setObject("607");
    agents.put(aa.getName(), aa);

    return agents;
  }

    public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
        String sql = "INSERT into dbo.supervisor values ("
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
          + "?,"
          + "0,"
          + "0,"
          + "0,"
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
 	      + "?)" ; //	resint4	L

          try {
          	System.out.println("jam");
            Connection con2 = getNewConnection();
              PreparedStatement ps = con2.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                SuperVisorLineWrapper lw = new SuperVisorLineWrapper(iter.next());
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
                ps.setDouble(17, lw.getLine().getAction1Eval()*100);
                ps.setDouble(18, lw.getLine().getAction2Eval()*100);
                ps.setDouble(19, lw.getLine().getActionTotalEval()*100);
                ps.setDouble(20, lw.getLine().getCustEval()*100);
                String cc = lw.getLine().getCustComment();
                if (cc == null){
                	cc = " ";
                }
                ps.setString(21, cc);

                String cd = lw.getLine().getComment();
                if (cd == null){
                	cd = " ";
                }
                ps.setString(22, cd);

                String ce = lw.getLine().getCustAgentId();
                if (ce == null){
                	ce = " ";
                }

                ps.setString(23, ce);

                String cf = lw.getLine().getCustAgentName();
                if (cf == null){
                	cf = " ";
                }
                ps.setString(24, cf);
                ps.setInt(25, lw.getLine().getDealNr());
                ps.setInt(26, lw.getLine().getIdealNr());

                String cr1 = lw.getLine().getResStr1();
                if (cr1 == null){
                	cr1 = " ";
                }
                ps.setString(27, cr1);


                String cr2 = lw.getLine().getResStr2();
                if (cr2 == null){
                	cr2 = " ";
                }
                ps.setString(28, cr2);


                String cr3 = lw.getLine().getResStr3();
                if (cr3 == null){
                	cr3 = " ";
                }
                ps.setString(29, cr3);
                ps.setInt(30, lw.getLine().getResInt1());
                ps.setInt(31, lw.getLine().getResInt2());
                ps.setInt(32, lw.getLine().getResInt3());
                ps.executeUpdate();
              }

              ps.close();
          } catch (SQLException e) {
              e.printStackTrace();
          }
        }

    
    public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
        String sql = "INSERT into dbo.supervisor values ("
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
          + "?,"
          + "0,"
          + "0,"
          + "0,"
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
 	      + "?)" ; //	resint4	L

          try {
          	System.out.println("jam");
            Connection con2 = getNewConnection();
              PreparedStatement ps = con2.prepareStatement(sql);

              for (Iterator iter = wr.getInfo().getLines().iterator(); iter.hasNext(); ) {
                eevaeksport.report.SuperVisorLineWrapperMeri lw = new  eevaeksport.report.SuperVisorLineWrapperMeri(iter.next());
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
                ps.setDouble(17, lw.getLine().getAction1Eval()*100);
                ps.setDouble(18, lw.getLine().getAction2Eval()*100);
                ps.setDouble(19, lw.getLine().getActionTotalEval()*100);
                ps.setDouble(20, lw.getLine().getCustEval()*100);
                String cc = lw.getLine().getCustComment();
                if (cc == null){
                	cc = " ";
                }
                ps.setString(21, cc);

                String cd = lw.getLine().getComment();
                if (cd == null){
                	cd = " ";
                }
                ps.setString(22, cd);

                String ce = lw.getLine().getCustAgentId();
                if (ce == null){
                	ce = " ";
                }

                ps.setString(23, ce);

                String cf = lw.getLine().getCustAgentName();
                if (cf == null){
                	cf = " ";
                }
                ps.setString(24, cf);
                ps.setInt(25, lw.getLine().getDealNr());
                ps.setInt(26, lw.getLine().getIdealNr());

                String cr1 = lw.getLine().getResStr1();
                if (cr1 == null){
                	cr1 = " ";
                }
                ps.setString(27, cr1);


                String cr2 = lw.getLine().getResStr2();
                if (cr2 == null){
                	cr2 = " ";
                }
                ps.setString(28, cr2);


                String cr3 = lw.getLine().getResStr3();
                if (cr3 == null){
                	cr3 = " ";
                }
                ps.setString(29, cr3);
                ps.setInt(30, lw.getLine().getResInt1());
                ps.setInt(31, lw.getLine().getResInt2());
                ps.setInt(32, lw.getLine().getResInt3());
                ps.executeUpdate();
              }

              ps.close();
          } catch (SQLException e) {
              e.printStackTrace();
          }
        }

    
    public Vector loadSupervisorList() throws IOException{

        Vector result = null;
        File f = new File("asksup.txt");
        if (f.exists()){
          result = new Vector();
          InputStreamReader is = new FileReader(f);
          BufferedReader br = new BufferedReader(is);
          String line = null;
          while( ( line = br.readLine()) != null){
          	  result.add(line);       
        }
          is.close();
          br.close();
        }
        return result;
      }

      public String getDistribution() {
/*
        StringBuffer sb = new StringBuffer();

        try {
          Connection con = getNewConnection();
          Statement st = con.createStatement();
          String cmd = "select mmitno as kood ,mmitds AS Nimetus , mmdim1 from mvxjdta.mitmas where MMCFI5 = 'Y'";

          if (isDebug())
          System.out.println(cmd);
          ResultSet rs = st.executeQuery(cmd);
         while(rs.next()){
           sb.append(rs.getString("kood") + ";" + rs.getString("nimetus") + "\n");
         }
         st.close();
         con.close();
       } catch (Exception e){}
         System.out.println(sb.toString());
        return sb.toString();*/
    return null;
      }




  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();
  TreeMap payers = new TreeMap();
  
  Converter cc = new Converter();
  HashMap kyssad =   cc.getQuestionaire(this.getExcelDir());
    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();


      
      String cmd2 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address,U_kylastus AS visit ,creditlimit AS limiit,cat.id AS agent, " +
          " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone,i_ladu  AS ladu , idgrpricelist as grupp , idgrcompanypricecat as gr2 "+
          " FROM grcompany kl LEFT JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id LEFT JOIN grcompanycat cat " +
          " ON cat.id=rel.idgrcompanycat WHERE (cat.category='myygimees' AND ltrim(kl.id)>0) or kl.code='1998' ORDER BY ltrim(kl.name)";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      HashMap payercusts = new HashMap();
      
      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          
          Customer cust = new Customer(accountnum);

          
          String name = rs.getString("nimi").trim();
          cust.setName(name);
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(aadress1);
          
          cust.restStreet = aadress1;
          
  		

         cust.setCountry("Estonia");
          
          String visit = rs.getString("visit");
          if(visit != null){
           // cust.setVisit(visit.trim());
          }
          //String info = rs.getString("info");
          //if (info != null){cust.setInfo(info.trim());}
         String limiit = rs.getString("limiit");
         // System.out.println(limiit);
          if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	}
          	}
          String ag = rs.getString("agent");
          if(ag != null){
        	 ag = ag.trim();
        	  cust.addAgent(new Agent(ag));
        	    Agent aa = (Agent)aHash.get(ag);
                //System.out.println("agents" +aa);
                if (aa != null){
                 // System.out.println("agents" +aa.getDubbler().toString());
                	if(aa.getDubbler()!= null && aa.getDubbler().size()>0){
                	//	System.out.println("agents" +aa.getDubbler().toString());
                		for(int i = 0;i<aa.getDubbler().size();i++){
                			cust.addAgent(new Agent((String)aa.getDubbler().elementAt(i)));	
                		//	System.out.println("agent lisatude" + cust.getAgents());
                		}
                    		
                	}
                }
          }
          
          cust.addAgent(new Agent("HOUSE"));
          cust.addAgent(new Agent("31"));
          cust.addAgent(new Agent("35"));
      
          
          if(cust.getID().equals("1998")){
        	  Enumeration enums = getParent().getAgentsHash().elements();
              while (enums.hasMoreElements()) {
                Agent a = (Agent) enums.nextElement();
                cust.addAgent(a);
              }
          }
          
          cust.setWare("1");
         
          cust.setPayer(rs.getString("payer").trim());
          if(payercusts.get(cust.getPayer())== null){
              Vector vec =  (new Vector());
              vec.addElement(cust.getID());
              payercusts.put(cust.getPayer(),vec);  
          }else{
        	  Vector vec = (Vector)payercusts.get(cust.getPayer());
        	  vec.add(cust.getID());
        	  payercusts.put(cust.getPayer(),vec); 
          }
          
          
     /*     if(payers.get(cust.getPayer())!= null){
        	  cust.setDebt(Double.parseDouble((String)payers.get(cust.getPayer())));
          }
          if(payers.get(cust.getID())!= null){
        	  cust.setDebt(Double.parseDouble((String)payers.get(cust.getID())));
          }
       */   
          double ale  = rs.getDouble("ale");
          if (ale>0){cust.setDiscount(ale);}
         // cust.setDebt(rs.getDouble("debt"));
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }
          
          String ladu = rs.getString("ladu");
          if(ladu != null && ladu .trim().length()>0){
          	cust.setWare(ladu.trim());
          }
          
          int grupp = rs.getInt("grupp");
          if (grupp != 23 && grupp > 0){
        	  cust.setCustType(grupp+"bb");
          	  cust.setGroup(grupp+"bb");   
          	  
           }
        
          String  gr2 = rs.getString("gr2");
          if (gr2 != null &&  gr2.trim().length() > 0){
        	  cust.setCustType(gr2+"bb");
          	  cust.setGroup(gr2+"bb");   
          	  
          	  System.out.println("grupp " + cust.getName() + " "+ cust.getGroup());
           }
        
          
          cc.addQuestiosToCustomer(cust,kyssad);
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }

      String cmd3 = "SELECT d.idpartner AS maksja, " +
 	"( d.totalamount - sum(case when m.amountbase is null then 0 else m.amountbase end)) AS summa FROM sdinvoice d " +
 	" INNER JOIN grcompany k on k.id = d.idgrcompany " +
 	" LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
 	" WHERE docstate !='F'  AND d.promptdate < getdate()-3  group by d.sourcedocno,d.idpartner, " +
 	" d.promptdate,d.totalamount  HAVING abs( d.totalamount - " +
    " sum(case when m.amountbase is null then 0 else m.amountbase end))  > 0.05  ORDER BY d.idpartner";
 	
      if (isDebug()) System.out.println(cmd3);
         ResultSet rs1 = st.executeQuery(cmd3);
          while (rs1.next()) {
            String id = rs1.getString("maksja").trim();
            double debt =rs1.getDouble("summa");
           // System.out.println(id + "  " + debt);
            
            Iterator iter = custMap.values().iterator();
            while(iter.hasNext()){
           Customer cust = (Customer)iter.next();   	
            	if(cust.getPayer().equals(id) || cust.getID().equals(id)){
                 cust.setDebt(cust.getDebt()+ debt);
                 // System.out.println(id + "  " + debt);	
            	}
            }
          }

 

         String cmd4 = "SELECT kl.code AS kood FROM grcompany kl INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id " +
         		"INNER JOIN grcompanycat cat " +
          " ON cat.id=rel.idgrcompanycat WHERE cat.category='hinnauuring' AND ltrim(kl.id)>0 group by kl.code,kl.name ORDER BY ltrim(kl.name)";

              if (isDebug()) System.out.println(cmd4);
                 ResultSet rs2 = st.executeQuery(cmd4);
                  while (rs2.next()) {
                    String id = rs2.getString("kood").trim();
                   
                   // System.out.println(id + "  " + debt);
                    Customer cust = (Customer) custMap.get(id);
                    if (cust != null ) {
                      cust.setSupervisormustfill(true);
                     //  System.out.println(id + "  " + debt);
                    }
                  }
         
                  //Kasutatud krediidi m��ra leidmine 
                  String cmd5 = "SELECT d.idpartner AS maksja,k.code AS saaja , " +
                  " d.totalamount*d.exchangerate - sum(d.exchangerate*m.amount) AS summa " +
                  " FROM sdinvoice d " +
                  " INNER JOIN grcompany k on k.id = d.idgrcompany " +
                  " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
                  " WHERE docstate !='F' group by d.sourcedocno,d.idpartner," +
                  "k.code,d.promptdate,d.totalamount,d.exchangerate ORDER " +
                  " BY d.promptdate,d.idpartner";

                       if (isDebug()) System.out.println(cmd5);
                          ResultSet rs5 = st.executeQuery(cmd5);
                           while (rs5.next()) {
                             String id = rs5.getString("saaja").trim();
                             double debt =rs5.getDouble("summa");
                            // System.out.println(id + "  " + debt);
                            
                             Customer cust = (Customer) custMap.get(id);
                             if (cust != null && debt != 0) {
                            //   cust.setDebt(cust.getDebt()+ debt);
                              //  System.out.println(id + "  " + debt);
                             }
                             
                             
                           }
           
         
         st.close();
         con.close();

         Iterator iter = custMap.values().iterator();
         while(iter.hasNext()){
        Customer cust = (Customer)iter.next();   	
         	if( cust.getDebt() < 0 && cust.getDebt() > -2.1 ){
              cust.setDebt(0);
             	
         	}
         }
       
         

    } catch (Exception e){
      getParent().showError(e);
    }
    return customers;
  }

  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }
  private Connection getNewConnection2() throws SQLException{
    return DriverManager.getConnection(url2,"sa", "Viruolu1");
  }

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='PDA'  ORDER BY name ";
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery(sql);

    while (rs.next()) {
        tm.put(rs.getString("liik").trim(),
               rs.getString("nimetus").trim());
    }
    rs.close();
    st.close();
    con.close();

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
  }

    



  public TreeMap getProducts(Agent agent){

    String cmd = "  SELECT pr.code AS kood,  rtrim(pr.name)  + ' ' + rtrim(pr.code) AS nimi," +
    		"hi.price AS hind,U_ean_teg AS EAN ,cat.idgrarticlecat AS grupp, " +
       "  pr.contentquantity AS tykke ,y.name AS yhik " +
       "  FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " + 
       "  hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
       "  RIGHT JOIN grunit y ON y.id=pr.idgrunit   " +
       "  WHERE hi.idgrpricelist=23 AND arca.category='PDA'  AND (articletype='G' OR articletype='P') AND right(pr.code,1)!='T' ";
    
    
    String cmd2 = "SELECT pr.code AS toode,SUM(quantity + pquantity + rquantity) AS seis FROM dcurbalancestore ss " +
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore!=6 AND quantity!=0 " +
	        " GROUP BY  pr.code " ;

    //siin on k�sa kas k�ik laod liidame kokku 
    
    if (isDebug()) System.out.println(cmd);
    TreeMap products = new TreeMap();
    try {
      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode");
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          toode.setStock(sto);
          //toode.setMeasure(ost);
          tooted.put(id,toode);
        }

      Connection con = getNewConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();

         Product prod = new Product(kood);
         String nimetus = "  "  + rs.getString("nimi").trim();
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         prod.setName(nimetus);
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 7) {
           prod.setEAN(ean.trim());
         }

         String grupp = rs.getString("grupp");
         if (grupp != null && grupp.trim().length() > 0) {
           prod.setGroup(grupp.trim());
         }
         
         if(prod.getName().indexOf("alus") >0 || prod.getName().indexOf("1/2") >0  || prod.getName().indexOf("1/2") >0){
            prod.setDiffColor(true);
            }

         prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");

        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
        }
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

    String sql1 = 	"SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice  " +
   " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc "+
   " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
   " WHERE r.docdate>getdate()-90 AND len(ltrim(d.idgrcompany))>0 " +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.code,r.quantity,r.docdate,r.unitprice ORDER BY k.code,t.code,r.docdate";

    
    String sql2 = " SELECT k.code AS klient,t.code AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
      "WHERE r.docdate>getdate()-90 AND len(ltrim(d.idgrcompany))>0 " +
      "AND (r.articletype='P' OR r.articletype='G') GROUP BY t.code,k.code ORDER BY t.code,k.code ";

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
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                cust.addProduct(prod, new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," "),false);
                kliendid.put(kliendikood, cust);
                //double pluskogus = Double.parseDouble(rs.getString("plusskogus").trim());
                //double minuskogus = Double.parseDouble(rs.getString("minuskogus").trim());
                //CustProd cp = new CustProd(kliendikood, tootekood, kogus ,pluskogus ,minuskogus);
                //custProds.add(cp);
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
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

  public void setBulkSales(Map agents){

    String cmd = "SELECT  cat.id AS agent ,SUM(r.amount)  AS summa, SUM(t.u_maht*r.quantity)AS summavol " +
    " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
   " INNER JOIN eremployee w ON w.id= d.idworker  " +
   " INNER JOIN grcompanycat cat  ON cat.code=w.code " +
   " INNER JOIN grarticle t ON t.id= r.idgrarticle" +
   " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND left(t.code,1) in ('1','2','3','4','5','6','7')" +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY cat.id";

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
                  agent.setMonthSalesVol((int)rs.getDouble("summavol"));
                 // System.out.println(agent.getMonthSales());
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


       DealerProduct dealerProds = new DealerProduct();
       try {


          String  sql = " SELECT pr.code AS toode,(quantity + pquantity + rquantity) AS stock , ss.iddrstore AS ladu FROM dcurbalancestore ss " + 
    		" INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore in ('601','600','607') AND quantity!=0 " +
	        " order BY  pr.code" ;

            if (isDebug()) System.out.println(sql);
               Connection con1 = getNewConnection();
               Statement stm = con1.createStatement();
               ResultSet rst = stm.executeQuery(sql);

                 while (rst.next()) {
                   String toode = rst.getString("toode").trim();
                   int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                   String ladu  =  rst.getString("ladu").trim();
                  	double amo = dealerProds.getDealerProductStock(ladu,toode);
                   if(amo>0){
                   	dealerProds.remDealerProduct(ladu,toode);
                    dealerProds.add(ladu, toode,(amo+seis)+"" ,"","");
                   }else{
                    dealerProds.add(ladu, toode,seis+"" ,"","");     	
                   }
                  }
               rst.close();
               stm.close();
               con1.close();

       } catch (Exception e) {
           getParent().showError(e);
       }

        return dealerProds;
        //return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = " SELECT d.idgrcompany as saaja ,  d.idpartner AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
        " d.totalamount*d.exchangerate AS SUMMA ,sum(m.amountbase) AS minus ,  " +
        "   -datediff(day,getdate(),d.promptdate)  AS paevi " +
        " FROM sdinvoice d " +
        " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
       " WHERE docstate !='F'  AND d.promptdate < getdate() +5 " +
        " group by  d.sourcedocno,d.idpartner,d.idgrcompany ,d.promptdate,d.totalamount,d.exchangerate ORDER BY d.idpartner " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
       ResultSet rs = stmt.executeQuery(sql);

       while(rs.next()){
         Debt debt = new Debt();
         debt.setCustID(rs.getString("saaja").trim());
         debt.setPayerID(rs.getString("maksja").trim());
         debt.setDoknr(rs.getString("arve").trim());
         debt.setDuedate(rs.getDate("kuupaev"));
         
         double summa = rs.getDouble("summa");
         double minus = -rs.getDouble("minus");
         debt.setDue(rs.getString("paevi"));
         
         if( summa+minus > 2 || summa+minus < -2){
              debt.setSum(summa+minus);
           list.addElement(debt);
         }
         
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
      return "SAFWiru";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
    }


    public void insertDefectiveMeri(eevaeksport.report.DefectiveWrapperMeri wr){
    	   String sql = "INSERT INTO dbo.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
           try {
             Connection con = getNewConnection();
               PreparedStatement ps = con.prepareStatement(sql);

               ps.setString(1, wr.getDefective().getAgentID());
               ps.setString(2, wr.getDefective().getAgentName());

               for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
                 DefectiveLineWrapperMeri lw = new DefectiveLineWrapperMeri(iter.next());
                 ps.setString(3, lw.getLine().getCustID());
                 ps.setString(4, lw.getLine().getCustName());
                 ps.setString(5, lw.getLine().getProdID());
                 ps.setString(6, lw.getLine().getProdName());

                 double kogus = 0;
                 try {
                   kogus = Double.parseDouble(lw.getLine().getAmount());
                 } catch (NumberFormatException nfe){};
                 ps.setDouble(7, kogus);
                 String bb = lw.getLine().getBestBefore();
            //     System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
                 ps.setString(8, bb);
                 ps.setInt(9, lw.getLine().getDefectCode());
                 ps.setString(10, lw.getLine().getProdType());
                 ps.setString(11, lw.getLine().getDefectText());

                 ps.executeUpdate();
               }

               con.commit();
               ps.close();
           } catch (SQLException e) {
               e.printStackTrace();
           }
         }

    public void insertDefective(DefectiveWrapper wr){
            String sql = "INSERT INTO dbo.MJ_QUALITY VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
            try {
              Connection con = getNewConnection();
                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, wr.getDefective().getAgentID());
                ps.setString(2, wr.getDefective().getAgentName());

                for (Iterator iter = wr.getDefective().getLines().iterator(); iter.hasNext(); ) {
                  DefectiveLineWrapper lw = new DefectiveLineWrapper(iter.next());
                  ps.setString(3, lw.getLine().getCustID());
                  ps.setString(4, lw.getLine().getCustName());
                  ps.setString(5, lw.getLine().getProdID());
                  ps.setString(6, lw.getLine().getProdName());

                  double kogus = 0;
                  try {
                    kogus = Double.parseDouble(lw.getLine().getAmount());
                  } catch (NumberFormatException nfe){};
                  ps.setDouble(7, kogus);
                  String bb = lw.getLine().getBestBefore();
               //   System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
                  ps.setString(8, bb);
                  ps.setInt(9, lw.getLine().getDefectCode());
                  ps.setString(10, lw.getLine().getProdType());
                  ps.setString(11, lw.getLine().getDefectText());

                  ps.executeUpdate();
                }

                con.commit();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
          }



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


  private String url = "jdbc:odbc:SAF";
  private String url2 = "jdbc:odbc:import";
  private Connection con;
  private TreeMap agents = new TreeMap();
  private     HashMap aHash = new HashMap();
  private Connection con3;
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();




@Override
public Vector getFixedAssortmentData() {
	// TODO Auto-generated method stub
	return null;
}



@Override
public String getProductSortingData() {
	// TODO Auto-generated method stub
	return null;
}



@Override
public Vector getLastThreeData() {
	// TODO Auto-generated method stub
	return null;
}
}
