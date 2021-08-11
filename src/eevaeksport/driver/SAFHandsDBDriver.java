
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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


public class SAFHandsDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFHandsDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      
      stmt = con.createStatement();
      getFixedAssortmentData();
    } catch (Exception e){
       parent.showError(e);
      // System.exit(0);
    }
  }

  
 

  
  public String getProductSortingData(){
   	return null;
   }
  
      public String getDealerCustomerCSV(String agent){
       return null;
       }

      public Vector getFixedAssortmentData() {
    	  Vector lines = new Vector();
   
          try {
         	  String cmd = " select pr.id as grupp ,t.code as toode from grpricelist pr INNER JOIN grpricelistrel rel ON pr.id =rel.idgrpricelist " +
			" INNER JOIN grarticle t on t.id = idgrarticle where pr.code like '%sortiment%' " +
			" order by pr.id,t.code ";
         	System.out.println(cmd);
            	 Connection con = getNewConnection();
        	        Statement stmt = con.createStatement();
        	        
        	       ResultSet  rs = stmt.executeQuery(cmd);
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
                        	   String cl = clGroup.trim();
                        	   if(cl.length() == 2){
                        		   cl = "0"+ cl;
                        	   }
                        	   
                             mline.setCliGroup(cl);
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
      
	  public void connectionUp(){
	  }

	  
	 	  
	  public Vector getPriceMatrixData()  {
        Vector lines = new Vector();
        String    sql = "select '' AS custid, pl.idgrpricelist AS clgroup,pr.code  AS prodid,' ' AS prodgroup,pl.price AS price " +
            " FROM grpricelistrel pl INNER JOIN grarticle pr ON  pl.idgrarticle = pr.id  ORDER BY  pl.idgrpricelist,pr.name";
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
              MatrixLine mline = new MatrixLine();

              if (custID != null && custID.trim().length() > 0) {
                mline.setCustID(custID.trim());
              }
              if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
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
			return result;

	/*		int cou = 0;
			try {
				Connection con = getNewConnection();
				Statement st = con.createStatement();

				String cmd2 = "SELECT kl.code AS kood FROM grcompany kl INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id INNER JOIN grcompanycat cat " +
        "   ON cat.id=rel.idgrcompanycat WHERE cat.category='Müügiesindaja' AND ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";


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

			return result;*/
		}


      public String getSaveStockInfo(){

      	  String  cmd = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		 "INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore=1 AND quantity >0 GROUP BY  pr.code " ;


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
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
      public String getSaveDebtInfo() {
    	  
    
       	return null;
       }
       
      	  public String  getCustProdDiscounts(Agent agent){
	  return null;
	  }
	
 	  
 	  	   public String getPilotDayData(Agent agent){
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
          String header = " T�nased tehingud :\n\n";
          String header1 = " Jooksva kuu m��k : ";
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
                     //   System.out.println(agent.getID() + sb.toString());
           
       
            return sb.toString();

    }

	  public void connectionDown(){
	  }

  
	
	  
    public TreeMap getAgents(){
    	
    	
  	
    String cmd = "select id AS agent, name AS nimetus from grcompanycat cat where cat.category='Piirkond' ";
    StringBuffer sb2 = new StringBuffer();
    if (isDebug()) System.out.println(cmd);
    
    

    
  
   // String dubbler = getParent().getProps().getProperty("dubbler");
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
 
         /*if(dubbler != null && dubbler.trim().length()>1){
          	StringTokenizer st = new StringTokenizer(dubbler,";");
          	while(st.hasMoreElements()){
          			String dub = st.nextToken().trim();        	
              		agent.addDubbler(dub);
              		System.out.println("dubbler added " + agent.getID() + " dub. " + agent.getDubbler());     		
          	}
          }*/
          agent.setName(name);
          agent.setIsPilot(true);
          sb2.append(agent.getID() + ";" + name + "\n");  
         // agent.setObject("607");
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    con.close();

    if(sb2.length() > 2){
     	try{
//     		 Converter.saveFileInUTF8( "agent.txt", Converter.enCodeEst(sb2.toString()));
     	}catch (Exception e){
     		
     	}
       
      }
    } catch (Exception e){
        getParent().showError(e);
      }


    return agents;
  }



      public String getDistribution() {

    return null;
      }




  public TreeMap getCustomers(Agent agent){
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address,'' AS visit ,creditlimit AS limiit,cat.id AS agent, \r\n" + 
      		"           (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone,i_ladu  AS ladu , \r\n" + 
      		"         (select name from grcompany where id = kl.idsettlcompany) as chain  FROM grcompany kl left JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id left JOIN grcompanycat cat \r\n" + 
      		"           ON cat.id=rel.idgrcompanycat and cat.category='Piirkond'  ORDER BY ltrim(kl.name)";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = Converter.enCodeEst(rs.getString("nimi").trim());
          cust.setName(name);
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress1),"Estonia"));
          String visit = rs.getString("visit");
          if(visit != null && visit.trim().length()>0){
            cust.setVisit(visit.trim());
          }
       
         String limiit = rs.getString("limiit");
         if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	}
          	}
          
          
          String ag = rs.getString("agent");
          if(ag != null) {
        	  cust.addAgent(new Agent(ag.trim()));
          }
             cust.addAgent(new Agent("6"));
          
          if(accountnum.equals("UusKlient")) {
          	 Iterator enume = aHash.keySet().iterator();
               while (enume.hasNext()) {
                 cust.addAgent(new Agent(enume.next()+""));
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
          
        
         
         String chain = rs.getString("chain");
         if(chain != null && chain.trim().length() > 0){
        	 cust.setChain(chain.trim());
         }
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


      String cmd3 = "SELECT d.idpartner AS maksja, " +
		 "d.totalamount*exchangerate  - sum(m.amountbase) AS summa FROM sdinvoice d " +
		 "INNER JOIN grcompany k on k.id = d.idgrcompany " +
		 "LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
		 "WHERE docstate !='F'  AND d.promptdate < getdate()  group by d.sourcedocno,d.idpartner, " +
		 "d.promptdate,d.totalamount,exchangerate  HAVING d.totalamount*exchangerate  - sum(m.amountbase ) != 0 ORDER BY d.idpartner";
		
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
  

  public TreeMap getProdGroups() throws SQLException {

    Connection con = getNewConnection();
    TreeMap   tm = new TreeMap();
    String    sql = "   SELECT id AS liik , name AS nimetus ,category FROM grarticlecat \r\n" + 
    		"    		         WHERE category='Liik'  ORDER BY name ";
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
        Connection con = getNewConnection();
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='Alamgrupp'  ORDER BY name ";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
           // tm.put(rs.getString("liik").trim(),
             //      rs.getString("nimetus").trim());
        }
        rs.close();
        st.close();
        con.close();

        return tm;
  }

    



  public TreeMap getProducts(Agent agent){

    String cmd = "SELECT  pr.id AS kood,  rtrim(pr.name)   AS nimi, hi.price AS hind,'' AS EAN ,cat.idgrarticlecat AS grupp, \r\n" + 
    		"    		    		         pr.contentquantity AS tykke ,y.name AS yhik,'' as tops  \r\n" + 
    		"    		    		         FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON  \r\n" + 
    		"    		    		         hi.idgrarticle=pr.id RIGHT JOIN grpricelist pl ON pl.id=hi.idgrpricelist RIGHT JOIN \r\n" + 
    		"    							 grarticlecat arca ON arca.id = cat.idgrarticlecat   RIGHT JOIN grunit y ON y.id=pr.idgrunit   \r\n" + 
    		"    		    		         WHERE (pl.ID)=1 AND (arca.category='Liik') order by pr.id";
    
    String cmd2 = "SELECT pr.id AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		 "INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore=1 AND quantity >0 GROUP BY  pr.id " ;

    StringBuffer top = new StringBuffer();
    
   
    //siin on k�sa kas k�ik laod liidame kokku 
    
    if (isDebug()) System.out.println(cmd);
    if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    
    try {
      Connection con2 = getNewConnection();
      Statement st2 = con2.createStatement();
      ResultSet rs2 = st2.executeQuery(cmd2);
      Hashtable tooted = new Hashtable();
        while(rs2.next()){
          String id = rs2.getString("toode").trim();
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          toode.setStock(sto);
          tooted.put(id,toode);
        }
        rs2.close();
        st2.close();
        
        
       
        
        
     //   System.out.println(tooted.toString() + tooted.size());

    
      Statement st = con2.createStatement();
      ResultSet rs = st.executeQuery(cmd);

      while(rs.next()){
        String kood = rs.getString("kood").trim();
        Product toode = (Product)tooted.get(kood);
        double kogus;
    
        if (toode == null) {
          kogus = 0;      
        } else {
          kogus = round(toode.getStock(), 2);
         // System.out.println(kogus + "  " + kood);
         }

       
        
         Product prod = new Product(kood);
         String nimetus = rs.getString("nimi").trim();
         prod.setStock(kogus);
         System.out.println(  "Toode  " + nimetus);
         
         // System.out.println(prod.getStock() + "  " + kood);
         double hind = rs.getDouble("hind");
         prod.setPrice(hind);
         prod.setName((nimetus));
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

        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
        }
        
      
     
        System.out.println("Tooted sees " + nimetus);
        
        products.put(nimetus+ kood, prod);
        
      }
      rs.close();
      st.close();
      con2.close();
     

    } catch (Exception e){
    	e.printStackTrace();
      getParent().showError(e);
    }

    return products;
  }
  public static float Round(float Rval, int Rpl) {
	   float p = (float)Math.pow(10,Rpl);
	   Rval = Rval * p;
	   float tmp = Math.round(Rval);
	   return (float)tmp/p;
	   }

  public Vector getCustomerProductsList(String agentCode){
  	

    String sql1 = 	"SELECT k.code AS klient,t.id AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice  , " +
    " 0 as aegs, 0 as days " +
  "  FROM  sdconsignmd abi  INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
  " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname  " +
  "  WHERE r.docdate>getdate()-60 AND len(ltrim(abi.idgrcompany))>0  " +
  "  AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.id,r.quantity,r.docdate,r.unitprice  " +
"	ORDER BY k.code,t.id,r.docdate " ; 
    
    
    String sql2 = " SELECT k.code AS klient,t.id AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdconsignmd abi INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname " +
      "WHERE r.docdate>getdate()-60 AND len(ltrim(abi.idgrcompany))>0 " +
      "AND (r.articletype='P' OR r.articletype='G') GROUP BY t.id,k.code ORDER BY t.id,k.code ";

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
                double kogus = round(Double.parseDouble(rs.getString("kogus").trim()),1);
                String lastdate = rs.getString("kuupaev").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                
                double aeg = rs.getDouble("aegs");
               
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                
                CustProd cp =  new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," ");
                
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
                      cp.setMovedStockPluss(round(pkogus,1));
                      cp.setMovedStockMinus(round(mkogus,1));
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

	          String cmd2 =  "SELECT k.code AS klient,t.id AS toode, r.quantity AS kogus ,r.docdate as kuupaev " + 
 "   FROM  sdconsignmd abi INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
 "  INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname  " +
 "  WHERE r.docdate>getdate()-60 AND len(ltrim(abi.idgrcompany))>0  " +
 "   AND (r.articletype='P' OR r.articletype='G') " +
 "  ORDER BY k.code,r.docdate desc,t.id "; // klient,kuupaev desc,toode
	          System.out.println(cmd2);
			   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"
			    Connection con2 = getNewConnection();
			    Statement stmt2 = con2.createStatement();
			    ResultSet rs2 = stmt2.executeQuery(cmd2);


	  while (rs2.next()){
	       String klient = rs2.getString("klient").trim();
	               String toode = rs2.getString("toode").trim();
	               int kogus = 0;
	               String date = " ";
	                   kogus = (int)rs2.getDouble("kogus");
	                   date = rs2.getString("kuupaev").trim().substring(0,10);

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
	                     if (counter == 0){cp.setS1(kogus + "" );}
	                     if (counter == 1){cp.setS2(kogus + "" );}
	                     if (counter == 2){cp.setS3(kogus + "" );}
	                     if (counter == 3){cp.setS4(kogus + "" );}
	                     if (counter == 4){cp.setS5(kogus + "" );}

	                     custProds.add(cp);
	                   }
	                 }
	                 oldklient = klient;
	                 olddate = date;
	               }
	               cou++;
	             }
rs2.close();
stmt2.close();
con2.close();

            
            
            
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

    
  public void setBulkSales(Map agents){

    String cmd = "	SELECT  cat.id AS agent ,SUM(r.amount)  AS summa ,sum(r.netmass) as kg   " +
		   "  FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
		   " INNER JOIN grcompany cus  ON cus.id=d.idgrcompany " +
		   "   INNER JOIN grcompanycatrel rel ON rel.idgrcompany=cus.id INNER JOIN grcompanycat cat " +
           "  ON cat.id=rel.idgrcompanycat " +
		   " INNER JOIN grarticle t ON t.id= r.idgrarticle  " +
		   " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) AND r.idgrunit!=1 " +
		   " AND (r.articletype='P' OR r.articletype='G') and cat.category='Piirkond' GROUP BY cat.id";

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
                 // agent.setKgSales((int)rs.getDouble("kg"));
                 // agent.setMonthSalesVol((int)rs.getDouble("summavol"));
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


    
       return null;
      }



  public Vector getDebt(String agentCode) throws SQLException{
    Vector list = new Vector();
    String sql = "SELECT kl.code AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
    "  d.totalamount*exchangerate  - sum(m.amountbase ) AS summa ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
    "  FROM sdinvoice d INNER JOIN grcompany kl on kl.id=d.idgrcompany " +
    " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
    "  WHERE docstate !='F'  AND d.promptdate <= getdate() " +
    "   group by  d.sourcedocno,kl.code,d.promptdate,d.totalamount,exchangerate  HAVING d.totalamount*exchangerate  - sum(m.amountbase ) != 0  ORDER BY kl.code " ;

  if (isDebug()) System.out.println(sql);
    Connection con = getNewConnection();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(sql);

    while(rs.next()){
      Debt debt = new Debt();
      debt.setCustID(rs.getString("maksja").trim());
      debt.setDoknr(rs.getString("arve").trim());
      debt.setDuedate(rs.getDate("kuupaev"));
      
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
      return "SAFHands";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }

    public String getDaySalesData(Agent agent) {
      return null;
    }

    public void insertDefective(DefectiveWrapper wr){
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


  private String url = "jdbc:odbc:hands";
  private Connection con;
  private TreeMap agents = new TreeMap();
  private     HashMap aHash = new HashMap();

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
