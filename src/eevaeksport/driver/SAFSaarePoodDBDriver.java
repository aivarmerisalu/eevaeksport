
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


public class SAFSaarePoodDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFSaarePoodDBDriver(EksportPanel parent){
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
        	  String sql = "SELECT DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) from mj_assortment where DATEADD(dd, 0, DATEDIFF(dd, 0, sortdate)) = DATEADD(dd, 0, DATEDIFF(dd, 0, GETDATE())) ";
        	  boolean insertassort = true;
         	  String cmd = " select pr.id as grupp ,t.code as toode from grpricelist pr INNER JOIN grpricelistrel rel ON pr.id =rel.idgrpricelist " +
			" INNER JOIN grarticle t on t.id = idgrarticle where pr.code like '%sortiment%' " +
			" order by pr.id,t.code ";
         	System.out.println(cmd);
            	 Connection con = getNewConnection();
        	        Statement stmt = con.createStatement();
        	        
        	               	        	insertassort = false;
        	        	//kontrollime kas on vaja insertida 
        	        
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
                           if(insertassort){

                               
                           }
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

	  
	    
	  public void insertSuperVisorSQL(SupervisorInfoWrapper wr){
	      
	       }

	  public void insertSuperVisorSQLMeri(SupervisorInfoWrapperMeri wr){
	       
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

              if ( price != 0) {
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
			return new Vector();
		}


      public String getSaveStockInfo(){
		       return null;
		   }

      public String getSaveDebtInfo() {
    	  
    	  sendDistributionReport(null);
       	return null;
       }
       
      private void sendDistributionReport(Calendar ccc){ //parameeter on selleks et kui on vaja saata mingi muu p�eva aruannet
    	  
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
  	
    String cmd = "select C.ID as agent,C.NAME as nimetus from Lihatööstus.dbo.GRCompanyCat CC " + 
    		"inner join GRCompanyCatRel CCR on CCR.IDGRCOMPANYCAT=CC.ID " + 
    		"inner join GRCompany C on C.ID=CCR.IDGRCOMPANY " + 
    		"where CC.Code='Merisoftiga Lihaletid' ";
    
    if (isDebug()) System.out.println(cmd);
  
    try {
        Connection con = getNewConnection();
        Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          agent.setName(name);
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    st.close();
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
    TreeMap customers = new TreeMap();

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address,'' AS visit ,creditlimit AS limiit,kl.id AS agent, \r\n" + 
      		"           (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale ,phone,i_ladu  AS ladu  \r\n" + 
      		"          ,I_Sortiment as grupp,I_Sortiment2 as grupp2,I_Sortiment3 as grupp3,I_Sortiment4 as grupp4,I_Sortiment5 as grupp5 ,\r\n" + 
      		"         (select name from grcompany where id = kl.idsettlcompany) as chain  FROM grcompany kl\r\n" + 
      		"		  INNER JOIN grcompanycatrel rel ON rel.idgrcompany=kl.id INNER JOIN grcompanycat cat \r\n" + 
      		"           ON cat.id=rel.idgrcompanycat WHERE cat.Code='Merisoftiga Lihaletid'  ORDER BY ltrim(kl.name)";


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
         // System.out.println(limiit);
          if (limiit != null && limiit.trim().length()>0){
          	String lim = limiit.substring(0,limiit.indexOf(".")).trim();
          	if (lim.length()>0){
          		cust.setCredit(lim);		
          	}
          	}
          String ag = rs.getString("agent").trim();
          cust.addAgent(new Agent(ag));
         
          cust.setPayer(rs.getString("payer").trim());
          
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }

          cust.setShowfullpricelist(true);
          String ladu = rs.getString("ladu");
          if(ladu != null && ladu.trim().length()>0){
          	cust.setWare(ladu.trim());
          }
          
          Vector vv = null;
         String grupp = rs.getString("grupp");
          if(grupp != null && grupp.trim().length()>0){
        	  vv = new Vector();
        	   String cl = grupp.trim();
        	   if(cl.length() == 2){
        		   cl = "0"+ cl;
        	   }
        	
        	  vv.addElement(cl);
          }
          
          String grupp2 = rs.getString("grupp2");
          if(grupp2 != null && grupp2.trim().length()>0){
        	  if(vv == null){
            	  vv = new Vector();  
        	  }
        	  String cl = grupp2.trim();
       	   if(cl.length() == 2){
       		   cl = "0"+ cl;
       	   }
       	
       	  vv.addElement(cl);
        	  
         
          }
          
          String grupp3 = rs.getString("grupp3");
          if(grupp3 != null && grupp3.trim().length()>0){
        	  if(vv == null){
            	  vv = new Vector();  
        	  }
        	  String cl = grupp3.trim();
          	   if(cl.length() == 2){
          		   cl = "0"+ cl;
          	   }
          	
          	  vv.addElement(cl);
           	  
          }
          
          String grupp4 = rs.getString("grupp4");
          if(grupp4 != null && grupp4.trim().length()>0){
        	  if(vv == null){
            	  vv = new Vector();  
        	  }	  
        	  String cl = grupp4.trim();
          	   if(cl.length() == 2){
           		   cl = "0"+ cl;
           	   }
           	
           	  vv.addElement(cl);
            	  
          }
          
          String grupp5 = rs.getString("grupp5");
          if(grupp5 != null && grupp5.trim().length()>0){
        	  if(vv == null){
            	  vv = new Vector();  
        	  }
        	  String cl = grupp5.trim();
          	   if(cl.length() == 2){
          		   cl = "0"+ cl;
          	   }
          	
          	  vv.addElement(cl);
           	  
          }
          
         if(vv != null){
        	 	cust.setFixSortCode(vv);
         }
         
         String chain = rs.getString("chain");
         if(chain != null && chain.trim().length() > 0){
        	 cust.setChain(chain.trim());
         }
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


         rs.close();
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

    TreeMap   tm = new TreeMap();
  
        tm.put("KANA","KANA");
        tm.put("LIHA","LIHA");
        tm.put("MARINAAD","MARINAAD");
        tm.put("ABI","ABI");
    
  

    return tm;
}

    public TreeMap getProdGroupsSecond() throws SQLException {
        TreeMap   tm = new TreeMap();
               return tm;
  }

    



  public TreeMap getProducts(Agent agent){

    String cmd = "SELECT pr.code AS kood, arca.code as adde,  rtrim(pr.name)   AS nimi,\r\n" + 
    		"                          0 AS hind,U_ean AS EAN ,cat.idgrarticlecat AS grupp, (select ar.code from grarticlecat ar RIGHT JOIN grarticleCatrel ca \r\n" + 
    		"									  ON ar.id = ca.idgrarticlecat where ar.category='Liik' and ca.idgrarticle = pr.id ) as gr2, pr.contentquantity AS tykke ,y.name AS yhik,U_top as tops \r\n" + 
    		"         FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN\r\n" + 
    		"                          grarticlecat arca ON arca.id = cat.idgrarticlecat  \r\n" + 
    		"         RIGHT JOIN grunit y ON y.id=pr.idgrunit   WHERE " +
    		"                           (arca.category='Lihaletid')        order by pr.code ";
    
    String cmd2 = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		 "INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore=13 AND quantity >0 GROUP BY  pr.code " ;

    StringBuffer top = new StringBuffer();
    
    String cmd3 = " select pr.code as toode, Ltrim(kir.name) + " + 
"  case when U_kylmutatud > getdate()-400 then ' K:' + ltrim(str(day(U_kylmutatud)))+'.'+ltrim(str(month(U_kylmutatud)))+'.' " +
" +substring(ltrim(str(year(U_kylmutatud))),3,4) else '' end + " +
" case when U_BestBefore > getdate()-400 then ' P:' + ltrim(str(day(U_BestBefore)))+'.'+ltrim(str(month(U_BestBefore)))+ " +
" '.'+substring(ltrim(str(year(U_BestBefore))),3,4) else '' end + " +
" ' S:' as info , round(sum(jaak.quantity),2) as jaak from DCurDRStockReserve jaak inner join " +
"    	 DRStockReserve kir on kir.id=jaak.iddrstockreserve inner " +
"   	join  grarticle pr on pr.id=jaak.idgrarticle where jaak.quantity >0 group by pr.code,kir.name,U_kylmutatud,U_BestBefore" +
"	 order by pr.code" ;
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
          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
        //  toode.setStock(sto);
          //toode.setMeasure(ost);
         // tooted.put(id,toode);
        }
        
        rs2.close();
        
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
           	String partii = ((Product)tooteinfo.get(id)).getPartii() + "|" +info.trim() + kog;	
           	toode.setPartii(partii);
           	}else{
           	  toode.setPartii(info.trim()); 		
           	}
            
           
           // tooteinfo.put(id,toode);
           }
         
          }
          rs3.close();
          st2.close();
          con2.close();
        
        
       // System.out.println(tooted.toString() + tooted.size());

      Connection con = getNewConnection();
      Statement st = con.createStatement();
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

        Product info = (Product) tooteinfo.get(kood);
       
        
         Product prod = new Product(kood);
         if(info != null){
     	   prod.setPartii(info.getPartii());
     }
         
         String adde = rs.getString("adde");
         String nimetus = rs.getString("nimi").trim();
         
         if(adde == null || adde.trim().length() == 0) {
        	 adde = "";
         } else {
        	adde = adde.trim(); 
         }
         prod.setNameSorting(adde + nimetus);
         
        
         prod.setStock(-1);
        // System.out.println(prod.getStock() + "  " + kood);
         double hind = rs.getDouble("hind");
        // prod.setPrice(hind);
         prod.setName((nimetus).toUpperCase());
         String ean = rs.getString("ean");
         if (ean != null && ean.trim().length() > 5) {
           prod.setEAN(ean.trim());
         }

         String grupp = rs.getString("gr2");
         if (grupp != null && grupp.trim().length() > 0) {
        	 if(grupp.trim().equals("OSTU")) {
        		 grupp = "KANA";
        	 }
        	 if(grupp.trim().equals("MATERJAL")) {
        		 grupp = "MARINAAD";
        	 }
        	 
        	 if(adde.equals("LL13")) {
        		 grupp = "MARINAAD";
        	 }
        	 if(adde.equals("LL11")) {
        		 grupp = "ABI";
        	 }
           prod.setGroup(grupp.trim());
         }
         
         // prod.setInCases( (rs.getDouble("tykke")));
        String unit = rs.getString("yhik");

        if (unit != null && unit.trim().length() > 0) {
          unit = unit.trim();
          prod.setUnit(unit);
        }
        
        products.put(nimetus, prod);

    }
      rs.close();
      st.close();
      con.close();
   
    } catch (Exception e){
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
  	

    String sql1 = 	"SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice  , " +
    "  t.U_RealAeg as aeg, (case when r.docdate+t.U_RealAeg >getdate() then 0 else 1 end) as days " +
  "  FROM  sdconsignmd abi  INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
  " INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname  " +
  "  WHERE r.docdate>getdate()-30 AND len(ltrim(abi.idgrcompany))>0  " +
  "  AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.code,r.quantity,r.docdate,r.unitprice ,U_RealAeg " +
"	ORDER BY k.code,t.code,r.docdate " ; 
    
    
    String sql2 = " SELECT k.code AS klient,t.code AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdconsignmd abi INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname " +
      "WHERE r.docdate>getdate()-30 AND len(ltrim(abi.idgrcompany))>0 " +
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
                double kogus = round(Double.parseDouble(rs.getString("kogus").trim()),1);
                String lastdate = rs.getString("kuupaev").substring(0,10);
                 lastdate = lastdate.substring(5,10);
                double hind = Double.parseDouble(rs.getString("lastprice").trim());
                
              //  double aeg = rs.getDouble("aeg");
               
                Customer cust = (Customer)kliendid.get(kliendikood);
                if (cust == null){
                  cust = new Customer(kliendikood);
                }
                Product prod = new Product(tootekood);
                
                CustProd cp =  new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,lastdate,hind,0,0," ");
                
                //if (aeg >0){
                  //	 cp.setRedline(rs.getString("days"));
                 // }
                
                cust.addProduct(prod,cp,false);
                
                kliendid.put(kliendikood, cust);
              }
           
              rs.close();
              stmt.close();

              con.close();

            

	           int counter = 0;
	           int cou = 0;
	          String oldklient = null;
	          String olddate = " ";

	          String cmd2 =  "SELECT k.code AS klient,t.code AS toode, r.quantity AS kogus ,r.docdate as kuupaev " + 
 "   FROM  sdconsignmd abi INNER JOIN ddstoredrow r ON abi.id=r.iddoc  " +
 "  INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=abi.companyname  " +
 "  WHERE r.docdate>getdate()-30 AND len(ltrim(abi.idgrcompany))>0  " +
 "   AND (r.articletype='P' OR r.articletype='G') " +
 "  ORDER BY k.code,r.docdate desc,t.code "; // klient,kuupaev desc,toode
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

            
            
            
            } catch (Exception e) {
              getParent().showError(e);
            }
            connectionDown();
            return custProds;
  }

    
  public void setBulkSales(Map agents){
       
        }



  public double getSalary(String agentCode) {
      return 0;
    }


  public DealerProduct getDealerProductsList() {


    
       return null;
      }



  public Vector getDebt(String agentCode) throws SQLException{
      return  new Vector();
 }

    public ManagerData getManagerData() throws SQLException{
       return new ManagerData();
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
      return "SAFSaaremaaPood";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
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
  private Connection con;
  private TreeMap agents = new TreeMap();
  private     HashMap aHash = new HashMap();

  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
