
/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
package eevaeksport.driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;


public class SAFAgroDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater,Discounter{
  public SAFAgroDBDriver(EksportPanel parent){
    super(parent);
    if(parent.getParentFrame().isDaySales()){
         this.subjectFilesTo = getParent().getString("senddatamsg");
    } else {this.subjectFilesTo = parent.getString("filemsg");}
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      
      stmt = con.createStatement();
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
        return null;
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

	       
	            
        String sql2 =  "select max(d.companyname) AS klient ,max(d.docno) AS arve, max(d.docdate) AS kuupaev , max(d.totalamount) as summa " +
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
 		public Vector getLastThreeData() {
 			// TODO Auto-generated method stub
 			return null;
 		}
	  public void connectionDown(){
	  }

  
	
	  
    public TreeMap getAgents(){
  	
    String cmd = "SELECT er.code AS agent, er.firstname + ' ' + surname AS nimetus, er.code as abikood ,exportcode AS dubbler  " +
      "  FROM   eremployee er Order by er.code";
  
    if (isDebug()) System.out.println(cmd);
  
    String dubbler = getParent().getProps().getProperty("dubbler");
    try {
      ResultSet rs = stmt.executeQuery(cmd);
        while(rs.next()){
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
 
          if(dubbler != null && dubbler.trim().length()>1){
          	StringTokenizer st = new StringTokenizer(dubbler,";");
          	while(st.hasMoreElements()){
          			String dub = st.nextToken().trim();        	
              		agent.addDubbler(dub);
              		System.out.println("dubbler added " + agent.getID() + " dub. " + agent.getDubbler());     		
          	}
          }
          agent.setName(name);
          agent.setIsPilot(true);
         // agent.setObject("607");
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
    rs.close();
    con.close();

    } catch (Exception e){
        getParent().showError(e);
      }
  /*  Agent aa = new Agent("565");
    aa.setName("HALJALA");
    aa.setObject("607");
    agents.put(aa.getName(), aa);
*/
    return agents;
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

    try {
      Connection con = getNewConnection();
      Statement st = con.createStatement();

      String cmd2 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address, U_Kontaktisik AS visit ,creditlimit AS limiit," +
      		" er.code AS agent, " +
           " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale," +
		   " message AS info ,kl.phone AS phone  ,idgrpricelist as grupp ,I_Peagrupp as pea " + 
           " FROM grcompany kl INNER JOIN eremployee er ON I_kliendihaldur=er.id WHERE  ltrim(kl.id)>0  and len(rtrim(kl.name))>0  and u_aktiivne=1" +
           " ORDER BY ltrim(kl.name)";


      if (isDebug()) System.out.println(cmd2);
      ResultSet rs = st.executeQuery(cmd2);
      HashMap custMap = new HashMap();

      while(rs.next()){
          String accountnum = rs.getString("kood").trim();
          Customer cust = new Customer(accountnum);
          String name = rs.getString("nimi").trim();
          cust.setName(name);
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {aadress1 = aadress1.trim();} else {aadress1 = " ";}
          cust.setAddress(aadress1);
          String visit = rs.getString("visit");
          if(visit != null){
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
          if(ag.equals("khj")){
          	cust.addAgent(new Agent("khkk"));
          }
          Agent aa = (Agent)aHash.get(ag);
          //System.out.println("agents" +aa);
          if (aa != null){
           // System.out.println("agents" +aa.getDubbler().toString());
          	if(aa.getDubbler()!= null && aa.getDubbler().size()>0){
          		System.out.println("agents" +aa.getDubbler().toString());
          		for(int i = 0;i<aa.getDubbler().size();i++){
          			cust.addAgent(new Agent((String)aa.getDubbler().elementAt(i)));	
          			System.out.println("agent lisatude" + cust.getAgents());
          		}
              		
          	}
          }
         
          cust.setPayer(rs.getString("payer").trim());
         // double ale  = rs.getDouble("ale");
         // if (ale>0){cust.setDiscount(ale);}
         // cust.setDebt(rs.getDouble("debt"));
          

          String info = rs.getString("info");
     
          
          if (info != null && info.trim().length()>0){
        	  {cust.setInfo(info.trim());}
        	  if(info.indexOf("@$") != -1){
        		  cust.setSalesBlock(true);
        	  }
          }
      
          
          String phone = rs.getString("phone");
          if(phone != null && phone.trim().length()>0){
          	cust.setPersonalData("tel. " +phone.trim());
          }
          
          String grupp = rs.getString("grupp");
          if(grupp != null && grupp.trim().length()>0){
          	cust.setGroup(grupp.trim());
          }
          
          String pea = rs.getString("pea");
          if (pea != null && pea.trim().length()>0 && pea.trim().equals("4")){
        	  cust.setSalesBlock(false);
          }
          
   
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
    }


    String cmd3 = "SELECT d.idpartner AS maksja, " +
	 "d.totalamount - sum(m.amount) AS summa FROM sdinvoice d " +
	 "INNER JOIN grcompany k on k.id = d.idgrcompany " +
	 "LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
	 "WHERE docstate !='F' AND exchangerate=1 AND d.promptdate < getdate()  group by d.sourcedocno,d.idpartner, " +
	 "d.promptdate,d.totalamount HAVING d.totalamount - sum(m.amount) != 0 ORDER BY d.idpartner";
	
     if (isDebug()) System.out.println(cmd3);
        ResultSet rs1 = st.executeQuery(cmd3);
         while (rs1.next()) {
           String id = rs1.getString("maksja").trim();
           double debt =rs1.getDouble("summa");
          // System.out.println(id + "  " + debt);
           
           Iterator iter = custMap.values().iterator();
           while(iter.hasNext()){
          Customer cust = (Customer)iter.next();   	
           	if(cust.getPayer().equals(id)){
                cust.setDebt(cust.getDebt()+ debt);
                 System.out.println(id + "  " + debt);	
           	}
           }
         }

         
         //15 p�eva yle 
         String cmd4 = "SELECT d.idpartner AS maksja, d.totalamount - sum(m.amount) AS summa " +
         		"FROM sdinvoice d  INNER JOIN grcompany k on k.id = d.idgrcompany inner join  grcompanycatrel cat on cat.idgrcompany=k.id  " +
    	" LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id  WHERE docstate !='F' AND exchangerate=1 AND d.promptdate < getdate()-2" +
    	" and I_Peagrupp!=4 and u_kommentaar  like ''  group by d.sourcedocno,d.idpartner,   	 d.totalamount " +
		" HAVING d.totalamount - sum(m.amount) != 0 ORDER BY d.idpartner";
    	
         if (isDebug()) System.out.println(cmd4);
            ResultSet rs4 = st.executeQuery(cmd4);
            Vector dones = new Vector();
             while (rs4.next()) {
               String id = rs4.getString("maksja").trim();
              // System.out.println(id + "  " + debt);
               
               Iterator iter = custMap.values().iterator();
               while(iter.hasNext()){
              Customer cust = (Customer)iter.next();   	
               	if(cust.getPayer().equals(id)){
               		 	 cust.setSalesBlock(false);
                     dones.addElement(cust.getPayer());
               	}
               }
             }
         
             String cmd5 = "SELECT d.idpartner AS maksja, d.totalamount - sum(m.amount) AS summa " +
      		"FROM sdinvoice d  INNER JOIN grcompany k on k.id = d.idgrcompany inner join  grcompanycatrel cat on cat.idgrcompany=k.id  " +
 	" LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id  WHERE docstate !='F' AND exchangerate=1 AND d.promptdate < getdate()-5" +
 	" and I_Peagrupp!=4 and u_kommentaar  like ''  group by d.sourcedocno,d.idpartner,   	 d.totalamount " +
		" HAVING d.totalamount - sum(m.amount) != 0 ORDER BY d.idpartner";
 	
      if (isDebug()) System.out.println(cmd5);
         ResultSet rs5 = st.executeQuery(cmd5);
          while (rs5.next()) {
            String id = rs5.getString("maksja").trim();
            double debt =rs5.getDouble("summa");
           // System.out.println(id + "  " + debt);
            
            Iterator iter = custMap.values().iterator();
             dones = new Vector();
            while(iter.hasNext()){
           Customer cust = (Customer)iter.next();   	
            	if(cust.getPayer().equals(id)){
            	
                    
                  System.out.println(id + "  " + debt);
                  if(cust.getDebt()>499){
                 	 cust.setSalesBlock(false);
                 	 cust.setSalesBlockT(true);
                  }else{
                	 cust.setSalesBlockT(false);  
                  }
            	dones.addElement(cust.getPayer());
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
    String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='Peagrupp'  ORDER BY name ";
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
        Connection con = getNewConnection();
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT id AS liik , name AS nimetus FROM grarticlecat WHERE category='Alamgrupp'  ORDER BY name ";
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

    



  public TreeMap getProducts(Agent agent){

    String cmd = "  SELECT pr.code AS kood,  rtrim(pr.code)  + ' ' + rtrim(pr.name) AS nimi," +
    	"	hi.price AS hind,U_ean AS EAN ,cat.idgrarticlecat AS grupp, " +
       " pr.contentquantity AS tykke ,y.name AS yhik  "+
       "  FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id RIGHT JOIN grpricelistrel hi ON " +
       "  hi.idgrarticle=pr.id  RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat  " +
       "  RIGHT JOIN grunit y ON y.id=pr.idgrunit    "+
       "  WHERE hi.idgrpricelist=3 AND arca.category='Peagrupp'  AND (articletype='G' OR articletype='P')  ";
    
    String cmd2 = "SELECT pr.code AS toode,SUM(quantity) AS seis FROM dcurbalancestore ss  " +
    		 "INNER JOIN grarticle pr ON idgrarticle = pr.id WHERE ss.iddrstore=1 AND quantity >0 GROUP BY  pr.code " ;

    
    
    String cmd3 = "  select pr.code as toode, Ltrim(kir.name) + " +
" case when U_kylmut_kpv > getdate()-400 then ' K:' + ltrim(str(day(U_kylmut_kpv)))+'.'+ltrim(str(month(U_kylmut_kpv)))+'.'+substring(ltrim(str(year(U_kylmut_kpv))),3,4) else '' end + " +
" case when U_Parim_Enne > getdate()-400 then ' P:' + ltrim(str(day(U_Parim_Enne)))+'.'+ltrim(str(month(U_Parim_Enne)))+'.'+substring(ltrim(str(year(U_Parim_Enne))),3,4) else '' end + " +
" ' S:' as info , round(jaak.quantity,2) as jaak from DCurDRStockReserve jaak inner join " +
  "  	 DRStockReserve kir on kir.id=jaak.iddrstockreserve inner " +
    "		join  grarticle pr on pr.id=jaak.idgrarticle order by pr.code " ;
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
          toode.setStock(sto);
          //toode.setMeasure(ost);
          tooted.put(id,toode);
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
           	String partii = ((Product)tooteinfo.get(id)).getPartii() + "|" +info.trim() + kog;	
           	toode.setPartii(partii);
           	}else{
           	  toode.setPartii(info.trim()); 		
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
         String nimetus = rs.getString("nimi").trim();
         prod.setStock(kogus);
        // System.out.println(prod.getStock() + "  " + kood);
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
   " WHERE r.docdate>getdate()-60 AND len(ltrim(d.idgrcompany))>0 " +
   " AND (r.articletype='P' OR r.articletype='G') GROUP BY k.code,t.code,r.quantity,r.docdate,r.unitprice ORDER BY k.code,t.code,r.docdate";

    
    String sql2 = " SELECT k.code AS klient,t.code AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, " +
    		"SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
      "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
      "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
      "WHERE r.docdate>getdate()-60 AND len(ltrim(d.idgrcompany))>0 " +
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

    
    String cmd =   " SELECT  w.code AS agent ,sum (d.totalamountbase/1.20) as summa " +
   " FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice " +
   "INNER JOIN grcompany cus  ON cus.id=d.idgrcompany " +
   " INNER JOIN eremployee w ON w.id= cus.I_kliendihaldur  " +   
  " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) " +
   "	 group BY w.code ";
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


       /*  try {


          String  sql = " SELECT pr.code AS toode,(quantity) AS stock , ss.iddrstore AS ladu FROM dcurbalancestore ss " + 
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

        return dealerProds;*/
       return null;
      }



      public Vector getDebt(String agentCode) throws SQLException{
       Vector list = new Vector();
       String sql = "SELECT kl.code AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
       "  d.totalamount - sum(m.amount) AS summa ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
       "  FROM sdinvoice d INNER JOIN grcompany kl on kl.id=d.idgrcompany " +
       " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
       "  WHERE docstate !='F' AND exchangerate=1 AND d.promptdate <= getdate() " +
       "   group by  d.sourcedocno,kl.code,d.promptdate,d.totalamount HAVING d.totalamount - sum(m.amount) != 0  ORDER BY kl.code " ;

     if (isDebug()) System.out.println(sql);
       Connection con = getNewConnection();
       stmt = con.createStatement();
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
      return "SAFAgro";
    }
    



    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }


    public String getDaySalesData(Agent agent) {
      return null;
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
              //    System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
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
  private Connection con;
  private TreeMap agents = new TreeMap();
  private     HashMap aHash = new HashMap();
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  private Statement stmt;
  private String subjectFilesTo = null;
  private NumberFormat NF = NumberFormat.getInstance();
  private TreeMap  dealers = new TreeMap();
}
