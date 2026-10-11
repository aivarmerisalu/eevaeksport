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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveWrapper;
import ee.mobilejava.wrapper.DistEvalListWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */



public class SAF6xDBDriver extends AbstractDBDriver implements Manager,Promo,BulkUpdater{
  public SAF6xDBDriver(EksportPanel parent){

    super(parent);
    if (parent.getParentFrame().isDaySales()) {
      this.subjectFilesTo = getParent().getString("senddatamsg");
    }
    else {
      this.subjectFilesTo = parent.getString("filemsg");
    }
    //String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    try {
      Class.forName(databaseDriverName).newInstance();
      con = getNewConnection();
      stmt = con.createStatement();
      
      Properties connInfo = new Properties();
  	  connInfo.put("user", "liides-remedia");
  	  connInfo.put("password", "remedia123");
  	  connInfo.put("charSet", "Cp1257");
      conlp = DriverManager.getConnection(url2,connInfo);
      conlp2 = DriverManager.getConnection(url2,connInfo);
      conlp3 = DriverManager.getConnection(url2,connInfo);
      
    }
    catch (Exception e) {
      parent.showError(e);
    }
  }

  public String getSaveDebtInfo() {
   	return null;
   }
   
  public String getAssortmentStr(Agent agent){
  	return null;
  }
    /**
     *d
     */
    public void connectionUp() {
    }

    public String getSaveStockInfo() {
      return null;
    }

    public String getDealerCustomerCSV(String agent) {
      return null;
    }

                                       public String  getCustProdDiscounts(Agent agent){
                                        return null;
                                        }


    /**
     *
     */
    public void connectionDown() {
    }

    public TreeMap getAgents() {
      String cmd =
          "SELECT id AS agent, name AS nimetus FROM grcompanycat WHERE category like 'agent%'";
      if (isDebug())
        System.out.println(cmd);
      TreeMap agents = new TreeMap();
      HashMap aHash = new HashMap();
      try {
        ResultSet rs = stmt.executeQuery(cmd);
        while (rs.next()) {
          Agent agent = new Agent(rs.getString("agent").trim());
          String name = rs.getString("nimetus").trim();
          agent.setName(name);
          agent.setObject("1");
          agents.put(name, agent);
          aHash.put(agent.getID(), agent);
        }
        rs.close();
        con.close();

      }
      catch (Exception e) {
        getParent().showError(e);
      }
      /* String  cmd2 = "SELECT MWWHLO AS yksus ,MWWHNM AS nimetus FROM mvxjdta.MITWHL WHERE " +
           " MWCONO='200' AND (MWWHLO = '200' OR MWWHLO = '205' OR MWWHLO = '215')";
        try {
          Connection con = getNewConnection();
            Statement stmt = con.createStatement();
            ResultSet	      rs = stmt.executeQuery(cmd2);
            while (rs.next()) {
              Dealer dealer = new Dealer(rs.getString("yksus").trim());
               dealer.setName(rs.getString("nimetus").trim());
                     addDealer(dealer);
            }
            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            getParent().showError(e);
         }*/

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

    public TreeMap getCustomers(Agent agent) {
      TreeMap customers = new TreeMap();

      try {
        Connection con = getNewConnection();
        Statement st = con.createStatement();

        String cmd2 = " SELECT kl.code AS kood,ltrim(kl.name) AS nimi,kl.locationaddr AS address,' ' AS visit ,'' AS agent, " +
            " (case when len(ltrim(kl.idsettlcompany)) >0 then kl.idsettlcompany else kl.id end) AS payer,-kl.pricecut AS ale " +
            " FROM grcompany kl  WHERE  ltrim(kl.id)>0 ORDER BY ltrim(kl.name)";

        if (isDebug())
          System.out.println(cmd2);
        ResultSet rs = st.executeQuery(cmd2);
        HashMap custMap = new HashMap();

        while (rs.next()) {
          String accountnum = rs.getString("kood").trim();
          String name = rs.getString("nimi").trim();
          Customer cust = (Customer)customers.get(name);
          if(cust == null){
           cust = new Customer(accountnum);
          }

          cust.setName(name);
          String aadress1 = rs.getString("address");
          if (aadress1 != null) {
            aadress1 = aadress1.trim();
          }
          else {
            aadress1 = " ";
          }
          cust.setAddress(aadress1);
          String visit = rs.getString("visit");
          if (visit != null) {
            // cust.setVisit(visit.trim());
          }
          String agent1 = rs.getString("agent").trim();
          cust.addAgent(new Agent(agent1));
          cust.setPayer(rs.getString("payer").trim());
          double ale = rs.getDouble("ale");
          if (ale > 0) {
            cust.setDiscount(ale);
          }
          
          

          String agname = "";
          Enumeration enums = getParent().getAgentsHash().elements();
          while (enums.hasMoreElements()) {
            Agent a = (Agent) enums.nextElement();
           if( a.getID().equals(agent.getID())){
           	agname = enCode((a.getName()));
           }
          }
          
          
          String indeks = "";
          String asula = "";
          String ins = null;
          try{//vana variant koos toimetamistega
          
          //uus variant ilma nendeta
          ins = "insert into [LPK - Klient]([Andmevahetuse tunnus], [LPK nr], Nr, Nimi " +
 	       ", Aadress, Postiindeks, Asula, Telefon, [Muugiisiku tahis], [Muugiisiku nimi], [Muugiisiku telefon] " +
 	       ", Tarneinfo, [Makseviisi tahis] " + //
 	       ", [Aktsepteerimise viga], [Aktsepteerimise veateade]) VALUES (0,'2553','" + cust.getID()+ "',dbo.Rus2Est('" + enCode((cust.getName())) + "'),dbo.Rus2Est('" +
 	       enCode((cust.getAddress())) + "'),'" + indeks + "',dbo.Rus2Est('"+ enCode((asula))  + "'),dbo.Rus2Est('" + enCode((cust.getPersonalData())) + "'),'" + agent.getID() 
 		   + "',dbo.Rus2Est('" + enCode((agname)) + "'),'','tarneinfo'" +
 		   //",'',''" +
 		   ",'',0, '')";
        
          
          // System.out.println(ins);
  	      //" + cust.getPaymTerm()+ "
  	      Statement st3 = conlp.createStatement();
  	      st3.executeUpdate((ins));
  	      st3.close();
          }catch (Exception e){
          //   System.out.println(cust.getID()+ " " + cust.getName() + " " + cust.getPersonalData() + "\n" + ins);
             
        	//  e.printStackTrace();
             /*
               ins = "update [LPK - Klient] set ([Andmevahetuse tunnus], [LPK nr], Nr, Nimi " +
      	       ", Aadress, Postiindeks, Asula, Telefon, [Muugiisiku tahis], [Muugiisiku nimi], [Muugiisiku telefon] " +
      	       ", Tarneinfo, [Kohaletoimetamine alates], [Kohaletoimetamine kuni], [Makseviisi tahis] " +
      	       ", [Aktsepteerimise viga], [Aktsepteerimise veateade]) VALUES (11111,2553,'" + cust.getID()+ "','" + cust.getName() + "','" + cust.getAddress()
      		   + "','" + indeks + "','"+ asula  + "','" + cust.getPersonalData() + "','" + agent.getID() + "','" + agname + "','','tarneinfo'" +
      		   ",getdate(),getdate(),'" + cust.getPaymTerm()+ "',0, '')";
              
              System.out.println(ins);
      	      
      	      Statement st3 = conlp.createStatement();
      	      st3.executeUpdate(ins);
      	      st3.close();*/
              
             }
  	      
          
          customers.put(name, cust);
          custMap.put(cust.getID(), cust);
        }



      /*  String cmd3 = "SELECT kl.code AS saaja, (d.totalamount) AS alus ,sum(-m.amount) AS SUMMA  FROM sdinvoice d " +
            " INNER JOIN grcompany kl ON kl.id=d.idgrcompany " +
              " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id WHERE docstate !='F' AND exchangerate=1 AND d.promptdate <=getdate() GROUP BY kl.code,d.totalamount ";

         if (isDebug()) System.out.println(cmd3);
            ResultSet rs1 = st.executeQuery(cmd3);
             while (rs1.next()) {
               String id = rs1.getString("saaja").trim();
               double debt2 = rs1.getDouble("alus");
               double debt = rs1.getDouble("SUMMA");
              // System.out.println(id + "  " + debt);
               Customer cust = (Customer) custMap.get(id);
               if (cust != null  ) {
                 cust.setDebt(cust.getDebt() + debt + debt2);
                //  System.out.println(id + "  " + debt);
               }
             }

        st.close();
        con.close();*/

      }
      catch (SQLException e) {
        getParent().showError(e);
      }
      return customers;
    }

    private Connection getNewConnection() throws SQLException {
      return DriverManager.getConnection(url,
                                         getParent().getProps().
                                         getProperty("dsn.user"),
                                         getParent().
                                         getProps().getProperty("dsn.pass"));
    }

    public TreeMap getProdGroups() throws SQLException {

      Connection con = getNewConnection();
      TreeMap tm = new TreeMap();
      String sql =
          "SELECT id AS liik , name AS nimetus FROM grarticlecat ORDER BY name";
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

    private String enCode(String res ){
     	 

     	res =res.replaceAll("�","11111xx");
     	res =res.replaceAll("�","11112xx");
   	res =res.replaceAll("�","11114xx");
   	res =res.replaceAll("�","11113xx");

   	res =res.replaceAll("�","11115xx");
   	res =res.replaceAll("�","11116xx");
   	res =res.replaceAll("�","11117xx");
   	res =res.replaceAll("�","11118xx");
   	
   	res =res.replaceAll("�","11119xx");
   	res =res.replaceAll("�","11120xx");
   	res =res.replaceAll("�","11121xx");
   	res =res.replaceAll("�","11122xx");



    	 
     	return res;
     }
    
    public TreeMap getProdGroupsSecond() throws SQLException {
      return null;
    }

    public TreeMap getProducts(Agent agent) {

      String cmd =
          "SELECT pr.code AS kood,u_aasta AS aasta,pr.name AS nimi,hi.price AS hind,pr.u_ean AS EAN " +
          ",cat.idgrarticlecat AS grupp, pr.u_kastis AS tykke ,y.name AS yhik,pr.u_parimenne AS partii " +
          " ,U_Regnr as areto,U_Liik as liik,U_Suurus as maht ,U_Alcoh As alko ,U_Regkpv AS aretokpv, f.code as fkood ,U_tootja as fname " +
          " FROM grarticle pr RIGHT JOIN grarticleCatrel cat ON cat.idgrarticle=pr.id " +
          " RIGHT JOIN grpricelistrel hi ON hi.idgrarticle=pr.id RIGHT JOIN grarticlecat arca ON arca.id = cat.idgrarticlecat " +
          "RIGHT JOIN grunit y ON y.id=pr.idgrunit  right join grcompany f on f.id= pr.idgrcompany " +
          "WHERE hi.idgrpricelist=26 AND u_mitteaktiivne=0 " +
          "AND articletype='G' order by cat.idgrarticlecat , pr.name ";
      
      String cmd2 = "SELECT idgrarticle AS toode,SUM(quantity) AS seis FROM dcurbalancestore WHERE  quantity!=0 " +
          " GROUP BY idgrarticle";

      if (isDebug())
        System.out.println(cmd);
      TreeMap products = new TreeMap();
      try {
        Connection con2 = getNewConnection();
        Statement st2 = con2.createStatement();
        ResultSet rs2 = st2.executeQuery(cmd2);
        Hashtable tooted = new Hashtable();
        while (rs2.next()) {
          String id = rs2.getString("toode");

          Product toode = new Product(id);
          double sto = rs2.getDouble("seis");
          toode.setStock(sto);
          tooted.put(id, toode);
        }

        Connection con = getNewConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(cmd);

        while (rs.next()) {
          String kood = rs.getString("kood").trim();

          String aasta = rs.getString("aasta");
          if (aasta != null) {

            aasta = aasta.trim();
          } else {
          aasta = " ";
          }

          Product toode = (Product) tooted.get(kood);
          double kogus;
          if (toode == null) {
            kogus = 0;
          }
          else {
            kogus = round(toode.getStock(), 2);
          }

          Product prod = new Product(kood);
          String nimetus = rs.getString("nimi").trim();
          prod.setStock(kogus);
          double hind = rs.getDouble("hind");
          prod.setPrice(hind);
          //double margin = (1-(ost/hind))*100;
          int margi = 0;
          try {
            // margi = Integer.parseInt(margin + "");
          }
          catch (Exception e) {}
             nimetus = (nimetus + " " + aasta ).trim();
          prod.setName(nimetus.replace('\'',' '));
          if (margi != 0) { //prod.setMargin(margi + "");
          }
          String ean = rs.getString("ean");
          if (ean != null && ean.trim().length() > 7) {
            prod.setEAN(ean.trim());
          }

          String grupp = rs.getString("grupp");
          if (grupp != null && grupp.trim().length() > 0) {
            prod.setGroup(grupp.trim());
         //   prod.setName(grupp.trim() + " " + prod.getName());
          }

          prod.setInCases( (rs.getDouble("tykke")));
          String unit = rs.getString("yhik");

          if (unit != null && unit.trim().length() > 0) {
            unit = unit.trim();
            prod.setUnit(unit);
          }
          String partii = rs.getString("partii");

          if (partii != null && partii.trim().length() > 0) {
            partii = partii.trim();
            prod.setPartii(partii);
          }

          
          
          String areto = rs.getString("areto");
          if (areto == null){
          	areto = "";
          }else {
          	areto = areto.trim();
          }
          
          String grupinimi = rs.getString("liik");
          if (grupinimi == null){
          	grupinimi = "";
          }else {
          	grupinimi = grupinimi.trim().replace('\'',' ');
          }
          String gkood = "";
          if(grupinimi.length()>2){
          	gkood = grupinimi.substring(0,3) + grupinimi.substring(grupinimi.length()-1,grupinimi.length());
          }
            
          
            
            
            
            
            //lisame toote LP-sse
          	double nkaal = 0;
        	double bkaal = 0;
        	double maht = rs.getDouble("maht");
        	
        	double alusel = 0;
        	double protsent  = rs.getDouble("alko");
        	String pandikood = "" ;
        	String maa = "";
        	String ekn = "";
        	
        	java.util.Date aretod = rs.getDate("aretokpv");
            prod.setDate1(aretod);
            
        	String tootjaKood = rs.getString("fkood");
        	
        	if(tootjaKood == null){
        		tootjaKood="";
        	}
        	
        	String tootjaNimi = rs.getString("fname");
        	if(tootjaNimi == null){
        		tootjaNimi="";
        	}else {
        		tootjaNimi = tootjaNimi.replaceAll("'", " ");
        	}
        	if(tootjaNimi.length()>50){
        		tootjaNimi = tootjaNimi.substring(0,49);
        	}
        	
            
            products.put(nimetus, prod);
            
           try{
            String ins = " insert into [LPK - Kaup]([Andmevahetuse tunnus], [LPK nr] " +
            "	, Nr, Kirjeldus, Ribakood, [Pohimootuhiku tahis], Netokaal, Brutokaal, Maht, Kastis, Alusel, Aktiivne, [Kanguse protsent] " +
        	", [Seotud pandikauba nr], [Paritolumaa tahis], [EKN kood], [Areto nr], [Tootja tahis], [Tootja nimetus] " +
        	", [Aktsepteerimise viga], [Aktsepteerimise veateade]" +
        	", [KM maar],[Kaubagrupi tahis], [Kaubagrupi nimetus],[Areto nr reg kp]) VALUES (1,'2553','" + prod.getID()+ "',dbo.Rus2Est('" + enCode((prod.getName().trim())) + "'),'" + prod.getEAN() + "','" + prod.getUnit()+ "',"
        	 + nkaal + "," + bkaal + "," + maht + "," + prod.getInCases() + "," + alusel + "," + 1  + "," + protsent + ",'" + pandikood + "','" +
        	 maa + "','" + ekn + "','" + areto + "','" + tootjaKood.trim() + "',dbo.Rus2Est('" + enCode((tootjaNimi.trim())) + "'), 0, '',20,dbo.Rus2Est('"
        	 + gkood + "'),dbo.Rus2Est('"+ grupinimi.trim()	+"'), ? )";
             
              
              System.out.println(ins);
           
           	PreparedStatement st3 = conlp2.prepareStatement(ins);
           	st3.setTimestamp(1, new java.sql.Timestamp(prod.getDate1().getTime()));
            st3.executeUpdate();
            st3.close();
           }catch (Exception e){
           //	e.printStackTrace();
           	
            String ins = " update [LPK - Kaup]set [Kaubagrupi tahis] = dbo.Rus2Est('" + gkood + "')" +
            		", [Kaubagrupi nimetus]= dbo.Rus2Est('" + grupinimi.trim() + "'),[Areto nr] = '" + areto+ "'" +
            				" , [Areto nr reg kp] = ? , [Tootja tahis] ='" + tootjaKood + "' , [Tootja nimetus] =" +
            						"  dbo.Rus2Est('" + enCode((tootjaNimi).trim()) + "') where  [LPK nr] = '2553' and Nr= '" + prod.getID() + "' " ;
           
              
              System.out.println(ins);
           	PreparedStatement st3 = conlp3.prepareStatement(ins);
           	st3.setTimestamp(1, new java.sql.Timestamp(prod.getDate1().getTime()));
            st3.executeUpdate();
            st3.close();
           
           }
              
            
            
            
            
            
          //}
        }
        rs.close();
        st.close();
        con.close();

      }
      catch (Exception e) {
        getParent().showError(e);
      }

      return products;
    }

    
  	public static String changeEnc(String s)
 	 {
 	  byte[] bb;
 	  try { bb = s.getBytes("Cp1251"); }
 	  catch( java.io.UnsupportedEncodingException e ) { return s; }
 	  char[] cb = new char[bb.length];
 	  for(int i=0; i < bb.length; i++)
 	    {
 	     cb[i] = (char)( bb[i] & 0x00FF );
 	    }
 	  return new String(cb);
 	 }

    
    public Vector getCustomerProductsList(String agentCode) {

      String sql1 = "SELECT d.idgrcompany AS klient,r.idgrarticle AS toode, r.quantity AS kogus ,r.docdate as kuupaev ,r.unitprice AS lastprice " +
          "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
          "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
          "WHERE r.docdate>getdate()-120 AND len(ltrim(d.idgrcompany))>0 " +
          "AND r.articletype='G' GROUP BY d.idgrcompany,r.idgrarticle,r.quantity,r.docdate,r.unitprice ORDER BY d.idgrcompany,r.idgrarticle,r.docdate";

      String sql2 = " SELECT d.idgrcompany AS klient,idgrarticle AS toode,SUM(case when quantity>0 then quantity else 0 end) AS plusskogus, SUM(case when quantity<0 then quantity else 0 end) AS minuskogus " +
          "FROM sdinvoice d INNER JOIN sdconsignmd abi ON d.idref=abi.idsdinvoice INNER JOIN ddstoredrow r ON abi.id=r.iddoc " +
          "INNER JOIN grarticle t ON t.id=r.idgrarticle INNER JOIN grcompany k ON k.name=d.companyname " +
          "WHERE r.docdate>getdate()-30 AND len(ltrim(d.idgrcompany))>0 " +
          "AND r.articletype='G' GROUP BY r.idgrarticle,d.idgrcompany ORDER BY d.idgrcompany,idgrarticle ";

      if (isDebug()) {
        System.out.println(sql1);

      }

      Vector custProds = new Vector();

      try {
        Connection con = getNewConnection();
        PreparedStatement stmt = con.prepareStatement(sql1);

        //stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
        ResultSet rs = stmt.executeQuery();

        HashMap kliendid = new HashMap();
        while (rs.next()) {
          String kliendikood = rs.getString("klient").trim();
          String tootekood = rs.getString("toode").trim();
          double kogus = Double.parseDouble(rs.getString("kogus").trim());
          String lastdate = rs.getString("kuupaev").substring(0, 10);
          lastdate = lastdate.substring(5, 10);
          double hind = Double.parseDouble(rs.getString("lastprice").trim());
          Customer cust = (Customer) kliendid.get(kliendikood);
          if (cust == null) {
            cust = new Customer(kliendikood);
          }
          Product prod = new Product(tootekood);
          cust.addProduct(prod,
                          new CustProd(cust.getID(), prod.getID(), kogus, 0, 0,
                                       lastdate, hind, 0, 0, " "),false);
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
        while (rs.next()) {
          String klient = rs.getString(1).trim();
          String toode = rs.getString(2).trim();
          double pkogus = rs.getDouble(3);
          double mkogus = rs.getDouble(4);
          Customer c = (Customer) kliendid.get(klient);
          if (c != null) {
            ArrayList al = (ArrayList) c.getProducts().get(toode);
            if (al != null) {
              CustProd cp = (CustProd) al.get(1);
              if (cp != null) {
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
      }
      catch (Exception e) {
        getParent().showError(e);
      }
      connectionDown();
      return custProds;
    }

    public void setBulkSales(Map agents) {

      String cmd =
          "SELECT cat.id AS agent ,SUM(d.totalamount)  AS summa  FROM  sdinvoice d  " +
          " INNER JOIN grcompanycatrel rel ON rel.idgrcompany=d.idgrcompany INNER JOIN grcompanycat cat  " +
          " ON cat.id=rel.idgrcompanycat " +
          " WHERE YEAR(d.docdate)=YEAR(getdate()) AND MONTH(d.docdate)=MONTH(getdate()) " +
          " AND cat.category like 'agent%' GROUP BY cat.id";

      if (isDebug())
        System.out.println(cmd);
      try {
        Connection con = getNewConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(cmd);
        while (rs.next()) {
          String agentID = rs.getString("agent").trim();
          Agent agent = (Agent) agents.get(agentID);
          if (agent != null) {
            agent.setMonthSales( (int) rs.getDouble("summa"));
          }
        }
        rs.close();
        stmt.close();
        con.close();
      }
      catch (Exception e) {
        getParent().showError(e);
      }
    }

    public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

    public DealerProduct getDealerProductsList() {

      /*try {
         String  sql = " SELECT  MBITNO AS toode,MBSTQT-MBREQT AS STOCK , MBWHLO AS LADU " +
         "from mvxjdta.MITBAL l WHERE MBWHLO='200' OR MBWHLO='205' OR MBWHLO='215' ORDER BY MBITNO" ;
           if (isDebug()) System.out.println(sql);
              Connection con1 = getNewConnection();
              Statement stm = con1.createStatement();
              ResultSet rst = stm.executeQuery(sql);
                while (rst.next()) {
                  String toode = rst.getString("toode").trim();
           int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                  String ladu  =  rst.getString("ladu").trim();
                  dealerProds.add(ladu, toode,seis+"" );
                  //System.out.println(ladu+" "+toode+" "+seis+"");
              }
              rst.close();
              stm.close();
              con1.close();
              } catch (Exception e) {
          getParent().showError(e);
              }*/

      //return dealerProds;
      return null;
    }

    public Vector getDebt(String agentCode) throws SQLException {
      Vector list = new Vector();
      String sql =
          "  SELECT d.idpartner AS maksja,d.sourcedocno AS arve, d.promptdate AS kuupaev, " +
         " d.totalamount AS SUMMA ,sum(m.amount) AS minus ,-datediff(day,getdate(),d.promptdate)  AS paevi " +
         " FROM sdinvoice d " +
         " LEFT JOIN vdvoucherrow m ON m.idsourcedoc=d.id " +
         " WHERE docstate !='F' AND exchangerate=1 AND d.promptdate <= getdate() " +
          " group by  d.sourcedocno,d.idpartner,d.promptdate,d.totalamount ORDER BY d.idpartner ";

      if (isDebug())
        System.out.println(sql);
      Connection con = getNewConnection();
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql);

      while (rs.next()) {
        Debt debt = new Debt();
        debt.setCustID(rs.getString("maksja").trim());
        debt.setDoknr(rs.getString("arve").trim());
        debt.setDuedate(rs.getDate("kuupaev"));

        double summa = rs.getDouble("summa");
        double minus = rs.getDouble("minus");
        debt.setDue(rs.getString("paevi"));
        // if(duedate.getTime()<now.getTime()){
        if (summa + minus > 1 || summa + minus < -1) {
          debt.setSum(summa + minus);
          list.addElement(debt);
        }
      }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }

    public ManagerData getManagerData() throws SQLException {
      ManagerData md = new ManagerData();
      return md;
    }

    public double getFoodSales(String agentCode) throws SQLException {
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException {
      return 0;
    }

    public double getPromoSales(String agentCode, int nr) throws SQLException {
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
      return "SAF6x";
    }

    public String getSubjectFilesTo() {
      return this.subjectFilesTo;
    }

    public String getDaySalesData(Agent agent) {
      return null;
    }

    public String getPilotDayData(Agent agent) {
      return null;
    }

    public void insertDefective(DefectiveWrapper wr) {

    }

    public void insertDistribution(DistEvalListWrapper wr) {
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
    private Statement stmt;
    private String url2 = "jdbc:odbc:lp";
    private Connection conlp;
    private Connection conlp2;
    private Connection conlp3;
    private String subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
    private TreeMap dealers = new TreeMap();

}
