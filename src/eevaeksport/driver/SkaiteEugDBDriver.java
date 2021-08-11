/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
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

public class SkaiteEugDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Discounter{


    public SkaiteEugDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
	
	 url = "jdbc:odbc:EUGESTA";
	 if( getParent().getProps().getProperty("odbc") != null){
		 url = "jdbc:odbc:" + getParent().getProps().getProperty("odbc");
	 }
    }
    public String getProductSortingData(){
       	return null;
       }
    /**
     *
     */
    public void connectionUp(){
      connectionUp(this.url);
    }
    public String getSaveDebtInfo() {
       	return null;
       }
       
    public String getAssortmentStr(Agent agent){
    	return null;
    }

            public String getDealerCustomerCSV(String agent){
             return null;
             }


        public String getSaveStockInfo(){
        	  connectionUp(url);
        	  String  cmd = "  SELECT  productcode AS toode ,w.unitsinstockfree AS stock, w.warehouseasid AS ladu  " +  
	           " FROM asVwWareHouse w INNER JOIN   asVwProduct t ON w.productasid=t.productasid " +
               " WHERE unitsinstockfree !=0  ORDER BY warehouseasid" ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
		           String dealer = rs.getString("LADU").trim();
		           if(seis > 0 && dealer.equals("EQ")){
		               products.append(kood +  ";");
		               products.append(dealer + ";");
		               products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
		             }
		           }
		           rs.close();
		           st.close();
		           con.close();
		           
		       } catch (Exception e) {
		           getParent().showError(e);
		       }
		       return products.toString();
		   }
        
                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }

                                       	public Vector getLastThreeData() {
                                    		// TODO Auto-generated method stub
                                    		return null;
                                    	}
        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
                Vector lines = new Vector();

                try {
               	  String cmd = " SELECT  PriceListAsId AS grupp,t.ProductCode AS toode FROM asVwPriceListProducts r  " +
               	  		" INNER JOIN asVwProduct t ON t.ProductAsId=r.ProductAsId WHERE LevelNo is not null ORDER BY ProductName";
               	System.out.println(cmd);
                  	connectionUp(url);;
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
              	       stmt.close();
              	       con.close();
                              } catch (Exception e) {
                                  getParent().showError(e);
                              }
                return lines;
        }

    private void connectionUp(String url){
      try {
        if (con != null){
          con.close();
        }
           con = DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"),
           getParent().getProps().getProperty("dsn.pass"));
      } catch (Exception e) {
	    getParent().showError(e);
      }

    }

    /**
     *
     */
    public void connectionDown(){
        try {
           con.close();
           con = null;
	} catch (Exception e) {
	    getParent().showError(e);
	}
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */

    public TreeMap getProdGroups() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT prodgroupcode AS liik ,groupname AS nimetus ,(select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId= " +
                     " gr.ProdGroupAsId ) as parent from dbo.asVwProdGroup g where ProdGroupParentAsId is null " +
         " ORDER BY prodgroupcode";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
            tm.put(kood,
                   rs.getString("nimetus").trim() +" " + kood);
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }
    
    
    public TreeMap getProdGroupsSecond() throws SQLException {
    	connectionUp(url);
        TreeMap   tm = new TreeMap();
        String    sql = "  SELECT g.prodgroupcode AS liik ,g.groupname AS nimetus , " +
			" (select gr.groupname from  asVwProdGroup gr where g.ProdGroupParentAsId= gr.ProdGroupAsId ) " +
			" as parent,ProdGroupParentAsId as id from dbo.asVwProdGroup g " +
			" where g.ProdGroupParentAsId>0  ORDER BY g.prodgroupcode";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood= rs.getString("liik").trim();
          String nimi = rs.getString("nimetus").trim() +" " + kood;
          if(kood.length()>0 ){
          	String nimeosa = rs.getString("parent");
          	if(nimeosa != null && nimeosa.length()>3){
          		nimi = nimeosa.substring(0,3) + " " + nimi;	
          	}
          	tm.put(kood,nimi);
          }
          
        }
        rs.close();
        st.close();
        con.close();
        this.connectionDown();
        return tm;
 }

    public TreeMap getAgents() {
    
        connectionUp(url);
    	TreeMap agents = new TreeMap();
        try {  
        TreeMap gr = new TreeMap();
        String sss = "select pricelistasid as id,prodgroupcode as gr from " +
		" dbo.asVwPriceListProducts pr INNER JOIN asVwProduct t ON pr.productasid=t.productasid  " +
		" INNER JOIN asVwProdGroup gr ON gr.prodgroupasid=t.prodgroupasid " +
		" GROUP BY pricelistasid,gr.prodgroupcode " +
		" ORDER BY pricelistasid,gr.prodgroupcode";
        
   	 String uus = "SELECT  PriceListasId AS id, ProductCode AS prod FROM vMRS_PriceListsProducts  "+
   	" ORDER BY PriceListasId, ProductCode";
        
       PreparedStatement stmt2 = con.prepareStatement(uus);
	    ResultSet	      rs2 = stmt2.executeQuery();
	    while (rs2.next()) {
	    	String id = rs2.getString("id").trim();
	    	Vector vec = (Vector)gr.get(id);
	    	
	    	
	    }
	    rs2.close();
	    stmt2.close();

        
        
        String  cmd = "SELECT employeeasid ,employeecode AS yksus,empname AS nimi,warehouseno AS ladu , " +
        " pricelistasid AS hinnakiri FROM asVwEmployee WHERE len(pricelistasid) >0 order by empname";



            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi + " " + agent.getID());
              
              agent.setObject(parent.make(rs.getString("ladu")));
              String hk = parent.make(rs.getString("hinnakiri"));
              Vector ss = (Vector)gr.get(hk);
              if(ss != null){
              	for(int i=0;i<ss.size();i++){
              		agent.addProdGroupList((String)ss.get(i));
              	}
              }
              agent.setIsPilot(true);
              agents.put(nimi, agent);
	    }
	    
	 

	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public Vector getPriceMatrixData()  {
    /*     Vector lines = new Vector();
         String    sql = " SELECT SL01008 AS custid,'' AS clgroup,SC18003 + '03' AS prodid,'' AS prodgroup,SC18009, " +
         		"(case when SY24003=null then '0' else SY24003 end ) AS discount,SC18008 AS price " +
                " FROM SC180100 INNER JOIN SL010100 ON  left(SC18002,4)= SL01001 LEFT JOIN SY240100 on SC18009=SY24002 and SY24001='BG' WHERE  " +
                " SC18010 <= getdate() AND (SC18011 >= getdate() OR SC18011 = null)  and len(SL01008) >0  ";
           Statement st = null;
           try {
             connectionUp(url);
             st = con.createStatement();
             ResultSet rs = st.executeQuery(sql);

             while (rs.next()) {

               String custID = rs.getString("custid").trim();
               String clGroup = rs.getString("clgroup");
               String prodID = rs.getString("prodid").trim();
               String prodGroup = rs.getString("prodgroup");
               String sss = rs.getString("SC18009");
               String discount = rs.getString("discount");
               if(discount == null){
               	discount = sss.trim();
               }else{
               	discount = discount.trim().replaceAll("%"," ").trim();
               }
			   
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
         return lines;*/
    	return null;
       }


    
    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        String cmd = "SELECT customername + address AS nimi,customercode AS kood,address + ' ' + rtrim(city) AS aadress, " +
       " paymenttime AS term,'' AS ale, debt , " +
       "    ' ' AS visit, custphone AS info ,employeecode as agent, kl.PriceListAsId as grupp  FROM asVwCustomer kl INNER JOIN " +
       " asVwEmployee a  ON a.employeeasid=kl.employeeasid  ORDER BY customername ";
	log(cmd);

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

	try {
            stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").replaceAll("\""," ").trim();
	
                Customer cust = new Customer(rs.getString("kood").trim());
                
                if(nimi.startsWith("UAB ")){
                	nimi = (nimi.substring(4,nimi.length()) + " UAB").trim();
                }
                cust.setName(nimi);
                cust.setAddress(rs.getString("aadress").trim());
                
               /* String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                  cust.setDiscount(Double.parseDouble(ale.trim()));
                }*/
                String term = rs.getString("term");
                cust.setPaymTerm(parent.make(term));
                
                double debti = rs.getDouble("debt");
                if(debti!=0){
                	cust.setDebt(debti);
                }
                //cust.setVisit(rs.getString("visit"));
                
                String info = rs.getString("info");
                cust.setPersonalData(parent.make(info));
                
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
                //cust.addAgent(new Agent("HOUSE"));
                String group = rs.getString("grupp");
                if(group != null){
                  cust.setGroup(group.trim());
                }
                
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();


            String sql = " select customercode AS klient,employeecode as agent from  " +
              " asVwDepCustomers si INNER JOIN asVwCustomer kl ON kl.customerasid=si.customerasid " +
              " INNER JOIN asVwEmployee a  ON a.employeeasid=si.employeeasid ";

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = rs.getString("klient").trim();
               Agent aa = new Agent(rs.getString("agent").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.addAgent(aa);
              }
            }
            rs.close();
            stmt.close();
            con.close();
         
        
	} catch (Exception e) {
            getParent().showError(e);
        }

	return customers;
    }

    public DealerProduct getDealerProductsList() {


        DealerProduct dealerProds = new DealerProduct();
        try {


          String  sql = "  SELECT  productcode AS toode ,w.unitsinstockfree AS stock, w.warehouseasid AS ladu  " +  
		           " FROM asVwWareHouse w INNER JOIN   asVwProduct t ON w.productasid=t.productasid " +
                   " WHERE unitsinstockfree !=0  ORDER BY warehouseasid" ;

             if (isDebug()) System.out.println(sql);
             connectionUp(url);
                Statement stm = con.createStatement();
                ResultSet rst = stm.executeQuery(sql);

                  while (rst.next()) {
                    String toode = rst.getString("toode").trim();
                    int seis = (int)Double.parseDouble(rst.getString("stock").replace(',','.'));
                    String ladu  =  rst.getString("ladu").trim();
                    dealerProds.add(ladu, toode,seis+""," "," " );
                    //System.out.println(ladu+" "+toode+" "+seis+"");
                }
                rst.close();
                stm.close();
                con.close();
        } catch (Exception e) {
            getParent().showError(e);
        }

         return dealerProds;
        
       }
    
    public TreeMap getDealers() {
        return dealers;
      }
    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "select kl.customername, kl.customercode AS klient, billcode AS number,billissuedate+kl.paymenttime AS kuupaev, " + 
		"	billamount-paidamount AS summa  , datediff(day,billissuedate+kl.paymenttime ,getdate() )as yle from  " +
		"	asVwOrder  d INNER JOIN asVwCustomer kl ON d.customerasid=kl.customerasid  " +
		"	where billissuedate+kl.paymenttime < getdate() -5 and billamount-paidamount>0 and " +
         "         billissuedate > getdate()-120   order by kl.customercode";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());
        debt.setDuedate(rs.getDate("kuupaev"));
        debt.setSum(rs.getDouble("summa"));       
        debt.setDue(rs.getString("yle"));
        
        if(debt.getSum()>0.5 || debt.getSum()<-0.5){
        	list.addElement(debt);	
        }
        }
      rs.close();
      stmt.close();
      con.close();
      return list;
    }
    
    
    public static final String cleanLitLatEst(String ret){
        if (ret != null){
        
            ret = ret.replaceAll("�","e");
            ret = ret.replaceAll("�","I");
            ret = ret.replaceAll("�","u");
            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","z");
            ret = ret.replaceAll("�","s");
            ret = ret.replaceAll("�","S");
            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","I");
            ret = ret.replaceAll("�","u");
            ret = ret.replaceAll("�","i");
            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","Z");
            ret = ret.replaceAll("�","E");
            ret = ret.replaceAll("�","U");
            ret = ret.replaceAll("�","U");

            ret = ret.replaceAll("�","Z");
            ret = ret.replaceAll("�","A");
            ret = ret.replaceAll("�","a");
            ret = ret.replaceAll("�","C");

            ret = ret.replaceAll("�","c");
            ret = ret.replaceAll("�","E");
            ret = ret.replaceAll("�","e");
            ret = ret.replaceAll("�","G");
            ret = ret.replaceAll("�","g");
            ret = ret.replaceAll("�","I");
            ret = ret.replaceAll("�","i");
            ret = ret.replaceAll("�","K");
            ret = ret.replaceAll("�","k");
            ret = ret.replaceAll("�","L");
            ret = ret.replaceAll("�","l");
            ret = ret.replaceAll("�","N");
            ret = ret.replaceAll("�","n");
            
            ret = ret.replaceAll("�","o");
            ret = ret.replaceAll("�","a");
            ret = ret.replaceAll("�","o");
            ret = ret.replaceAll("�","u");
            
        	
        }
        if(ret == null || ret.trim().length()==0){
            return " ";
            }else {
            return ret;
            }        
          }
    
    
    private void makeXMLandCSVs(){
    	//kliendi XML tegemine
    	StringBuffer sb = new StringBuffer();
    	StringBuffer csv = new StringBuffer();
    	String date = getDateYYYMMDD(0);
    	
    	Vector unilever = new Vector();
    	unilever.add("26");
    	unilever.add("27");
    	unilever.add("50");
    	unilever.add("51");
    	unilever.add("52");
    	unilever.add("53");
    	
    	Vector npfoods = new Vector();
  
    	
    	 connectionUp(url);
    	 String vaikimisi = parent.getProps().getProperty("unilever.defaultdaysback");
    	 if(vaikimisi == null){
    		 vaikimisi = "7";
    	 }
    	 

    	 String aeg = "d.data > getdate()-" + vaikimisi;
    	 String algus = parent.getProps().getProperty("unilever.startday");
    	 
    	 String lopp = parent.getProps().getProperty("unilever.endday");
    	 
    	 if(algus != null && lopp != null){
    		 aeg = "d.data >= '" + algus+ "'  and d.data <= '" + lopp + "'";
    	 }
    	 
    	   String cmd = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED select kl.Customercode AS klient  , (kl.customername) AS nimi,(adr.indeksas)AS indeks, (kl.city) AS city , " +
                " address AS aadr,(PVMNr) as vat , kl.RegionasId as region ,reg.RegionName as regionname , " +
             " StoreChainAsId as chain,part.RegNr as regnr from asVwCustomer kl  " +
             "     INNER JOIN Partneriai AS part  ON part.PartneriaiId  = kl.CustomerASid  " +
             "     INNER JOIN Adresai AS adr     ON part.AdresoId = adr.AdresoId	" +
             "     INNER JOIN asVwRegion reg on reg.RegionAsId=kl.RegionasId " +
             "       ORDER BY customercode asc ";
    	log(cmd);


    	try {
                stmt = con.prepareStatement(cmd);

    	    ResultSet	      rs = stmt.executeQuery();
    	    boolean custok = false;
              String ls = System.getProperty("line.separator");
    	    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    	    sb.append("<ROOT>"+ ls);
    	    StringBuffer region = new StringBuffer();
    	    Vector reg = new Vector(); 
    	    csv.append("Customer Id;Name;Country;Region;Chain" + ls);
    	    region.append("Code;Name" +  ls);
    	    while (rs.next()) {
    	    	custok = true ;
    	    	String klid = rs.getString("klient").trim();
    	    	String nimi =  rs.getString("nimi").replaceAll("\""," ").replaceAll("'", " ").replaceAll("&", " and ").trim();
    	    	
    	    	  sb.append("     <CUSTOMER>"+ ls);
    	    	  sb.append("        <ID_KLIENT>" + klid + "</ID_KLIENT>" + ls);
    	    	  sb.append("        <ID_PLATNIK>" + klid + "</ID_PLATNIK>" + ls);
    	    	  sb.append("        <NAZWA_1>" + nimi + "</NAZWA_1>"+ ls);
    	    	  sb.append("        <KOD_POCZT>" +  parent.make(rs.getString("indeks")) + "</KOD_POCZT>"+ ls);
    	    	  sb.append("        <MIEJSCOW>" +  parent.make(rs.getString("city")) + "</MIEJSCOW>"+ ls);    	  
    	    	  sb.append("        <ADRES>" +  parent.make(rs.getString("aadr")).replaceAll("&", " ") + "</ADRES>"+ ls); 
    	    	  sb.append("        <NIP>" +  parent.make(rs.getString("vat")) + "</NIP>"+ ls);
    	    	  
    	    	  String regi = parent.make(rs.getString("region"));
    	    	  String reginame = parent.make(rs.getString("regionname"));
    	    	  String chaincode = parent.make(rs.getString("chain"));
    	    	  String regnr = parent.make(rs.getString("regnr"));

      	    	csv.append(cleanLitLatEst(klid) + ";" + cleanLitLatEst(nimi) + ";LT;" + cleanLitLatEst(regi) + ";" + cleanLitLatEst(chaincode) + ";" + regnr +  ls);
    	    	  
    	    	  if(!reg.contains(regi)){
    	    		 region.append(cleanLitLatEst(regi) + ";" + cleanLitLatEst(reginame) + ls ); 
    	    	  }
    	    	  reg.addElement(regi);
    	    	 // sb.append("        <ID_PLATNIK>0</ID_PLATNIK>"+ ls);
      	    	  sb.append("</CUSTOMER>"+ ls);              
    	    }
                rs.close();
                stmt.close();
                sb.append("</ROOT>");
                if(custok){
                	File f = new File(getOutDir() + "Customer.xml");
                    if (f.exists())f.delete();
                		PrintWriter os = new PrintWriter(new FileWriter(
                				getOutDir() + "Customer.xml", true));
                	String out = sb.toString();
                	byte[] someBytes = out.getBytes();
                	String encodingName = "UTF-8";
                	String s = new String ( someBytes, encodingName );
                		os.write(s);
                		os.close();
                		
                		File fcsv = new File(getOutCSVDir() + "Customer_" + date + ".csv");
                		 if (fcsv.exists())fcsv.delete();
                		PrintWriter osv = new PrintWriter(new FileWriter(
                				getOutCSVDir() + "Customer_" + date + ".csv", true));
                	String outv = csv.toString();
                	byte[] someBytesv = outv.getBytes();
                	String encodingNamev = "UTF-8";
                	s = new String ( someBytesv, encodingNamev );
                		osv.write(s);
                		osv.close();
                		
                	 fcsv = new File(getOutCSVDir() + "Region_" + date + ".csv");
               		 if (fcsv.exists())fcsv.delete();
               		 osv = new PrintWriter(new FileWriter(
               				getOutCSVDir() + "Region_" + date + ".csv", true));
               	 outv = region.toString();
                someBytesv = outv.getBytes();
               	encodingNamev = "UTF-8";
               	s = new String ( someBytesv, encodingNamev );
               		osv.write(s);
               		osv.close();
                		
                }
                
                
                String cmd2 = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED SELECT  max(pg.Kodas) as grupp, max(pg.Kodas) as abi ,productcode AS toode,max(productbarcode) AS ean,max(productname) " + 
     " AS nimi,max(retailprice/1.21) AS myygihind ,  " + 
     "               max(kiekiomv) as unit  ,sum( w.UnitsInStock) as stock ,max(tt.Mase) as kaal   " +
      "             FROM asVwProduct t  INNER JOIN prekes tt ON tt.Kodas =productcode  " +
    "  inner join PrekiuGrupes pg on tt.PrekiuGrupesId =pg.PrekiuGrupesID  " +
    "                 left join asVwWareHouse w ON w.productasid=t.productasid and  unitsinstockfree !=0  " +
     "              where (pg.Kodas in ('26','27','50','51','52','53') or  pg.Kodas in ('41','66','615'))  " +
      "            and productcode not like '%**%'   and  tt.Nenaudoti <> 1 "  +
       "           group by t.productcode  order by t.productcode ";
                
                
    	log(cmd2);


                stmt = con.prepareStatement(cmd2);
                 sb = new StringBuffer();
                 csv = new StringBuffer();
                 StringBuffer laoseis = new StringBuffer();
    	   	      rs = stmt.executeQuery();
    	    boolean prodok = false;
    	    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    	    sb.append("<ROOT>"+ ls);
    	    csv.append("Item Id NPF;Item name;Item Id;Unit;NetWeight" + ls);
    	    laoseis.append("Date;Item Id;Quantity;Unit"+ ls);
    	    while (rs.next()) {
    	    	prodok = true ;
    	    	String grupp = rs.getString("grupp");
    	    	String abi =  rs.getString("abi");
    	    
    	    	if(grupp == null){
    	    		grupp = abi;
    	    	}else{
    	    		grupp = grupp.trim();
    	    	}
    	    	String prodid = rs.getString("toode").trim();
    	    	String ean = parent.make(rs.getString("ean"));
    	    	String name = rs.getString("nimi").replaceAll("\""," ").replaceAll("'", " ").replaceAll("\n", " ").replaceAll("&", " and ").trim();
    	    	String hind = parent.make(rs.getString("myygihind"));
    	    	String unit = parent.make(rs.getString("unit"));
    	    	String stock = parent.make(rs.getString("stock"))  ;
    	    	String kaal = parent.make(rs.getString("kaal"))  ;
    	    	//'97748','','
    	    	
    	    	
    	    	if(unilever.contains(abi)){
    	    		 sb.append("     <PRICLIST>"+ ls);
       	    	  sb.append("        <ID_PRODUKT>" + prodid + "</ID_PRODUKT>" + ls);
       	    	  sb.append("        <EAN>" +  ean + "</EAN>"+ ls);   	    	  
       	    	  sb.append("        <NAZWA>" +  name + "</NAZWA>"+ ls);
       	    	  sb.append("        <CENA1_NETTO_PP>" +  hind + "</CENA1_NETTO_PP>"+ ls);
       	    	  sb.append("        <WALUTA1>EUR</WALUTA1>"+ ls);
       	    	  sb.append("        <VAT>18</VAT>"+ ls); 
       	    	  sb.append("        <VAT_ZW>NO</VAT_ZW>"+ ls); 
       	    	  sb.append("        <JM>" +  unit + "</JM>"+ ls);
       	    	  sb.append("        <STAN_PP>" +  stock + "</STAN_PP>"+ ls);
         	    	  sb.append("     </PRICLIST>"+ ls);
         	    	
    	    	} else {
    	    		//'278571','395420'

    	    		csv.append("npid;" + cleanLitLatEst(name) + ";" + cleanLitLatEst(prodid) + ";" + unit + ";" + kaal + ls );
    	    		laoseis.append(date + ";" + cleanLitLatEst(prodid) + ";" + stock + ";" + unit + ls);
    	    	}
    	    	 	
    	    }
                rs.close();
                stmt.close();
                sb.append("</ROOT>");
                if(prodok){
                	File f = new File(getOutDir() + "Pricelist.xml");
                    if (f.exists())f.delete();
                		PrintWriter os = new PrintWriter(new FileWriter(
                				getOutDir() + "Pricelist.xml", true));
                	String out = sb.toString();
                	byte[] someBytes = out.getBytes();
                	String encodingName = "UTF-8";
                	String s = new String ( someBytes, encodingName );
                		os.write(s);
                		os.close();
                		
                		
                		
                		File fcsv = new File(getOutCSVDir() + "Items_" + date + ".csv");
               		 if (fcsv.exists())fcsv.delete();
               		PrintWriter osv = new PrintWriter(new FileWriter(
               				getOutCSVDir() + "Items_" + date + ".csv", true));
               	String outv = csv.toString();
               	byte[] someBytesv = outv.getBytes();
               	String encodingNamev = "UTF-8";
               	s = new String ( someBytesv, encodingNamev );
               		osv.write(s);
               		osv.close();
                		
                		
               		

            	fcsv = new File(getOutCSVDir() + "Stock_" + date + ".csv");
           		 if (fcsv.exists())fcsv.delete();
           		osv = new PrintWriter(new FileWriter(
           				getOutCSVDir() + "Stock_" + date + ".csv", true));
           	 outv = laoseis.toString();
           	someBytesv = outv.getBytes();
           	encodingNamev = "UTF-8";
           	s = new String ( someBytesv, encodingNamev );
           		osv.write(s);
           		osv.close();
            		
               		
                }
                
           // Siin l�peb toodete tegemine     
                String cmd3 = " SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED  select pg.Kodas  as grupp,pg.Kodas as abi,d.OperacijosId as id, d.doknr AS doknr,(k.kodas) AS klient ,(k.pavad) AS kliendinimi " +
       		" ,(kl.RegionasId)  AS region ,(kl.StoreChainAsId)  AS chain  ,(d.data)  AS tehing, (d.data)  AS tellimus  , " +
    " productcode AS toode, r.kiekis AS kogus  ,retailprice as hhind,	  r.ccnuolaidosproc2 as ale, r.vvkainagalutinebepvm as mhind ,(kiekiomv) as unit ,k.RegNr as regnr ,employeecode as agent " +
    "  FROM Operacijos d  INNER JOIN Pard_BendraInf AS pbi  ON d.OperacijosId = pbi.OperacijosId " +
    "  INNER JOIN partneriai k ON k.PartneriaiId=pbi.PartneriaiId  " +
    "  INNER JOIN asVwCustomer kl ON kl.CustomerASId= k.PartneriaiId " +
    "  INNER JOIN PartneriaiPA AS partpa ON k.PartneriaiId = partpa.PartneriaiId " + 
    "  INNER JOIN MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId " +
    "  INNER JOIN asVwEmployee ag ON ag.employeeasid = kl.employeeasid " + 
    "  INNER JOIN PardavimuKunai  r  ON d.OperacijosId = r.OperacijosId  " +
    "  INNER JOIN prekes t ON t.prekesid =r.prekesid  INNER JOIN asVwProduct tt ON tt.productasid=r.prekesid " +
    "  inner join PrekiuGrupes pg on t.PrekiuGrupesId =pg.PrekiuGrupesID  " +
    "  INNER JOIN  asVwProdGroup g ON g.prodgroupasid=tt.prodgroupasid " +
    "  WHERE " + aeg + " and  (pg.Kodas in ('26','27','50','51','52','53') or pg.Kodas in ('41','66','615')) and OperacijosTipoId in (50, 45, 68, 164, 2000014)  and productcode not like '%**%'  order by r.PardavimuKunoId";
 
                log(cmd3);
     stmt = con.prepareStatement(cmd3);
      sb = new StringBuffer();
      try{
          rs = stmt.executeQuery();
      }catch(Exception ee){
    	  try{
              rs = stmt.executeQuery();
          }catch(Exception ee1){
        	  rs = stmt.executeQuery();
          }
      }
	
 boolean dokok = false;
 sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
 sb.append("<ROOT>"+ ls);
 
 Vector heads = new Vector();

 csv = new StringBuffer();
 csv.append("Date;Invoice Id;Customer Id;Customer name;Country;Region;Chain;Currency;Invoice Amount;Invoice Amount MST;PayerCode;DateCollected;AgentCode" + ls);
 
 

 while (rs.next()) {
 	dokok = true ;
 	String grupp = rs.getString("grupp");
 	String abi = rs.getString("abi").trim();
  	if(grupp == null){
		grupp = abi;
	}else{
		grupp = grupp.trim();
	}
 	String id = rs.getString("id").trim();
 	String num = rs.getString("doknr").trim();
 	String klient =  parent.make(rs.getString("klient"));

 	String kliendinimi =  parent.make(rs.getString("kliendinimi"));
 	String regi = parent.make(rs.getString("region"));
 	String chain = parent.make(rs.getString("chain"));
 	String invoDate = getDateYYYMMDD( rs.getDate("tehing").getTime() );
 	
 	
 	   if(num.length()>0 && !num.equals("Q")){
 	
 	if(!heads.contains(num)){
 	
 		if(unilever.contains(abi)){
 	
 	  sb.append("     <DOCUHEAD>"+ ls);
 	  sb.append("        <ID_DOK>" + id + "</ID_DOK>" + ls);
 	  sb.append("        <KRAJ>LITHUANIA</KRAJ>"+ ls);
	  sb.append("        <ID_TYP>INVOICE</ID_TYP>"+ ls);
	  sb.append("        <NUMER>" + num + "</NUMER>"+ ls);
 	  sb.append("        <ID_KLIENT>" + klient + "</ID_KLIENT>"+ ls);   	    	  
 	  sb.append("        <DATA_WYST>" + invoDate + "</DATA_WYST>"+ ls);
 	  sb.append("        <DATA_SPRZ>" +  getDate( rs.getDate("tellimus").getTime() ) + "</DATA_SPRZ>"+ ls);
 	  sb.append("        <FORMA_ZAPL>P</FORMA_ZAPL> "+ ls);
   	  sb.append("     </DOCUHEAD>"+ ls);              
  	
 	}else{
 		String regnr = parent.make(rs.getString("regnr"));
 	 	String agent = parent.make(rs.getString("agent"));
 	 		csv.append( invoDate + ";" + num + ";" + cleanLitLatEst(klient) + ";" + cleanLitLatEst(kliendinimi) + ";LT;" + cleanLitLatEst(regi)+ ";" + cleanLitLatEst(chain) + ";EUR;0;0;" + regnr + ";" + date + ";"+ agent+ ls);
 	 		
 	}
 	
 
	
 	}
 	heads.addElement(num);
 	   }	
	}
     rs.close();
     stmt.close();
     sb.append("</ROOT>");
     if(dokok){
     	File f = new File(getOutDir() + "DOCUHEAD.XML");
         if (f.exists())f.delete();
     		PrintWriter os = new PrintWriter(new FileWriter(
     				getOutDir() + "DOCUHEAD.XML", true));
     	String out = sb.toString();
     	byte[] someBytes = out.getBytes();
     	String encodingName = "UTF-8";
     	String s = new String ( someBytes, encodingName );
     		os.write(s);
     		os.close();
     		
     		
     		File fcsv = new File(getOutCSVDir() + "Invoices_" + date + ".csv");
      		 if (fcsv.exists())fcsv.delete();
      		PrintWriter osv = new PrintWriter(new FileWriter(
      				getOutCSVDir() + "Invoices_" + date + ".csv", true));
      	String outv = csv.toString();
      	byte[] someBytesv = outv.getBytes();
      	String encodingNamev = "UTF-8";
      	s = new String ( someBytesv, encodingNamev );
      		osv.write(s);
      		osv.close();
       		
       		
     		
     		
     }
                
   //  Dokumendi p�ised L�pp
     
   //  Dokumendi read Algus
     
     
     String cmd4old = " SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED  select pg.Kodas  as grupp,pg.Kodas as abi, d.doknr AS doknr,d.OperacijosId as id,(k.kodas) AS klient ,(k.pavad) AS kliendinimi " +
		" ,(kl.RegionasId)  AS region ,(kl.StoreChainAsId)  AS chain  ,d.data  AS tehing, (d.data)  AS tellimus  , " +
	    " productcode AS toode,(case when OperacijosTipoId = ('51') then -1 else 1 end )*r.kiekis AS kogus  ,retailprice/1.21 as hhind,	  r.ccnuolaidosproc2 as ale, r.ccnuolaidosproc3 as ale2," +
	    "  r.ppKainaBePVM/r.kiekis as mhind ,(kiekiomv) as unit " +
	    "  FROM Operacijos d  INNER JOIN Pard_BendraInf AS pbi  ON d.OperacijosId = pbi.OperacijosId " +
	    "  INNER JOIN partneriai k ON k.PartneriaiId=pbi.PartneriaiId  " +
	    " INNER JOIN asVwCustomer kl ON kl.CustomerASId= k.PartneriaiId " +
	    "  INNER JOIN PartneriaiPA AS partpa ON k.PartneriaiId = partpa.PartneriaiId " + 
	    "  INNER JOIN MokejimoSalygos AS msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId " +
	    "  INNER JOIN PardavimuKunai  r  ON d.OperacijosId = r.OperacijosId  " +
	    "  INNER JOIN prekes t ON t.prekesid =r.prekesid  INNER JOIN asVwProduct tt ON tt.productasid=r.prekesid " +
	    "  INNER JOIN  asVwProdGroup g ON g.prodgroupasid=tt.prodgroupasid " +
	    "  inner join PrekiuGrupes pg on t.PrekiuGrupesId =pg.PrekiuGrupesID  " +
	    "  WHERE " + aeg + " and  (pg.Kodas in ('26','27','50','51','52','53')  or  pg.Kodas in ('41','66','615')) and productcode not like '%**%'" +
	    		"and OperacijosTipoId in ('50','51')  order by r.PardavimuKunoId ";
     
     
     String cmd4 = "     SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED select pg.Kodas as grupp, " +
    " pg.Kodas as abi, d.doknr AS doknr, d.OperacijosId as id,(k.Kodas) AS klient,(k.Pavad) AS kliendinimi,(kl.RegionasId) AS region,(kl.StoreChainAsId) AS chain, " +
    " d.data AS tehing,(d.data) AS tellimus,t.Kodas AS toode, " +
    " (case when d.OperacijosTipoId = ('51') then -1 else 1 end )* r.kiekis AS kogus, round((r.ppKainaBePVM * d.Kursas),2) AS Suma ,tt.RetailPrice / 1.21 as hhind, " +
    " r.ccnuolaidosproc2 as ale,r.ccnuolaidosproc3 as ale2,r.ppKainaBePVM / r.kiekis as mhind,(tt.KiekioMV) as unit" +
    " FROM Operacijos d INNER JOIN Pard_BendraInf pbi ON d.OperacijosId = pbi.OperacijosId INNER JOIN partneriai k ON k.PartneriaiId=pbi.PartneriaiId " +
    " INNER JOIN asVwCustomer kl ON kl.CustomerASId= pbi.PartneriaiId " + 
    " INNER JOIN PartneriaiPA partpa ON k.PartneriaiId = partpa.PartneriaiId " + 
    " INNER JOIN MokejimoSalygos msal ON partpa.MokejimoSalygosId = msal.MokejimoSalygosId INNER JOIN PardavimuKunai r ON d.OperacijosId = r.OperacijosId INNER JOIN prekes t ON t.prekesid = r.prekesid " +
    " INNER JOIN asVwProduct tt ON tt.productasid = r.prekesid " +
    " INNER JOIN asVwProdGroup g ON g.prodgroupasid = tt.prodgroupasid inner join PrekiuGrupes pg ON t.PrekiuGrupesId = pg.PrekiuGrupesID  " +
    " WHERE d.OperacijosTipoId in (50, 45, 68, 164, 2000014) and " +
    "  " + aeg + " " +
    " and (pg.Kodas in ('26','27','50','51','52','53') or pg.Kodas in ('41','66','615')) and productcode not like '%**%' " +
    " order by r.PardavimuKunoId ";
	 
             log(cmd4);
        stmt = con.prepareStatement(cmd4);
         sb = new StringBuffer();
         try{
             rs = stmt.executeQuery();
         }catch(Exception ee){
       	  try{
                 rs = stmt.executeQuery();
             }catch(Exception ee1){
           	  rs = stmt.executeQuery();
             }
         }
          boolean dokposok = false;
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        sb.append("<ROOT>"+ ls);
        
        csv = new StringBuffer();
        csv.append("Date;Invoice Id;Item Id;Qty;Sales Unit;Currency;Sales price;Discount Amount;Line Amount;Line Amount MST;DateCollected;SpecDiscount" + ls);
while (rs.next()) {
	dokposok = true ;

	String grupp = rs.getString("grupp");

	String abi = rs.getString("abi").trim();
	if(grupp == null){
		grupp = abi;
	}else{
		grupp = grupp.trim();
	}
	
	String num = rs.getString("doknr").trim();

	   if(num.length()>0 && !num.equals("Q") && heads.contains(num)){
		   String id =  rs.getString("id").trim() ;
			String invoDate = getDateYYYMMDD( rs.getDate("tehing").getTime() ); 
		 	String toode =  rs.getString("toode").trim() ;
		 	double kogus =  rs.getDouble("kogus") ;
		 	double priceh = rs.getDouble("hhind");

		 	double ale = rs.getDouble("ale");
		 	double ale2 = rs.getDouble("ale2");
			double pricem = rs.getDouble("mhind");
			String yhik = parent.make(rs.getString("unit"));
			
		
			
			if(unilever.contains(abi)){
		 		sb.append("     <DOCUPOSI>"+ ls);
		 		  sb.append("        <ID_DOK>" + id + "</ID_DOK>" + ls);
		 		  sb.append("        <ID_PRODUKT>" + toode + "</ID_PRODUKT>" + ls);
		 		  sb.append("        <ILOSC_PP>" + kogus + "</ILOSC_PP>" + ls);
		 		 
		 		  
		 		  sb.append("        <CENA1_N_B_PP>" +priceh + "</CENA1_N_B_PP>"+ ls);
		 		  sb.append("        <CENA1_N_PP>" +pricem + "</CENA1_N_PP>"+ ls);
		 		
		 		if(ale2 !=0){
		 			ale = ale2;
		 		}
		 		  sb.append("        <RABAT>"  + ale + " </RABAT>"+ ls);
		 		  sb.append("        <WALUTA1>EUR</WALUTA1>"+ ls);
		 		  sb.append("     </DOCUPOSI>"+ ls);  

		 	}else {
		 			csv.append(invoDate +";" + num + ";" + cleanLitLatEst(toode) + ";" + kogus + ";" + yhik + ";EUR;" + priceh + ";" + ale +
			 				";" + (priceh*kogus) + ";" + (pricem * kogus)+ ";" + date + ";" + ale2 + ls);
			 	
		 	}
		 	
		 	
	   }
}
  rs.close();
  stmt.close();
  sb.append("</ROOT>");
  if(dokposok){
  	File f = new File(getOutDir() + "DOCUPOSI.XML");
      if (f.exists())f.delete();
  		PrintWriter os = new PrintWriter(new FileWriter(
  				getOutDir() + "DOCUPOSI.XML", true));
  	String out = sb.toString();
  	byte[] someBytes = out.getBytes();
  	String encodingName = "UTF-8";
  	String s = new String ( someBytes, encodingName );
  		os.write(s);
  		os.close();
  		
  		
  		
  		
  		File fcsv = new File(getOutCSVDir() + "InvoiceLines_" + date + ".csv");
 		 if (fcsv.exists())fcsv.delete();
 		PrintWriter osv = new PrintWriter(new FileWriter(
 				getOutCSVDir() + "InvoiceLines_" + date + ".csv", true));
 	String outv = csv.toString();
 	byte[] someBytesv = outv.getBytes();
 	String encodingNamev = "UTF-8";
 	s = new String ( someBytesv, encodingNamev );
 		osv.write(s);
 		osv.close();
  		
  		

  		
  		
  }
     con.close();
                
    	}  catch (Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	System.exit(0);
    	
    }
	  private String getDate(long ll){
		  Calendar cal = Calendar.getInstance();
		  cal.setTimeInMillis(ll);
		    StringBuffer sb = new StringBuffer();
		    String year =   cal.get(Calendar.YEAR) + "";
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		     if (day.length() == 1){
		      day = "0" + day;
		    }
		    if (month.length() == 1){
		     month = "0" + month;
		   }
		    sb.append(year + "-" + month + "-" + day);
		    return sb.toString();
		  }
    
	  

	  private String getDateYYYMMDD(long ll){
		  Calendar cal = Calendar.getInstance();
		  if(ll >0)
		  cal.setTimeInMillis(ll);
		    StringBuffer sb = new StringBuffer();
		    String year =   cal.get(Calendar.YEAR) + "";
		    String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		     if (day.length() == 1){
		      day = "0" + day;
		    }
		    if (month.length() == 1){
		     month = "0" + month;
		   }
		    sb.append(year + month + day);
		    return sb.toString();
		  }
    private String getOutDir(){
        String dir = getParent().getProps().getProperty("readout");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    private String getOutCSVDir(){
        String dir = getParent().getProps().getProperty("readoutcsv");
        if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
        return dir;
      }

    public TreeMap getProducts(Agent agent){
    	
   
        connectionUp(url);


        String cmd = " SELECT productcode AS toode,productname AS nimi,retailprice AS myygihind ,unitsinpack AS tykke,0 AS stock, " +
                     " prodgroupcode AS grupp,productbarcode AS ean, (select ProdGroupCode from  asVwProdGroup gr where g.ProdGroupParentAsId= " +
                     " gr.ProdGroupAsId ) AS teineGrupp ,(select UnitsInStockFree  from asVwWareHouse w where  " +
                     " w.WareHouseAsId ='EQBR' and ProductAsId=t.ProductAsId)   AS sellout FROM asVwProduct t INNER JOIN " +
                     " asVwProdGroup g ON g.prodgroupasid=t.prodgroupasid ";

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim().replaceAll("\"","");
		nimetus = nimetus.substring(0,1) + nimetus.substring(1,nimetus.length()).toLowerCase();
	
		
		if(nimetus.startsWith("UAB")){
			nimetus = nimetus.substring(4,nimetus.length()) + " UAB";
		}


                Product prod = new Product(kood);
                prod.setName("      " +nimetus);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));

                prod.setInCases(rs.getDouble("tykke"));
              //  prod.setStock(parseDouble(rs.getString("stock").trim()));
              //  prod.setMaxDiscount(parseDouble(rs.getString("maxale").trim()));
                

                String gr = rs.getString("grupp");
                
                if(gr != null && gr.trim().length()>0){
                  prod.setGroupSecond(gr.trim());
                }
                String  ean = rs.getString("ean");
                if(ean != null && ean.trim().length()>0){
                  prod.setEAN(ean.trim());
                }
                String  gr2 = rs.getString("teineGrupp");
                if(gr2 != null && gr2.trim().length()>0){
                  prod.setGroup(gr2.trim());
                }
               try{ 
	                double  sellout = rs.getDouble("sellout");
	                if(sellout > 1){
	                  prod.setStatusSellout("1");
	                  prod.setName(prod.getName() +  " @"  + sellout + "@");
	                }
                
	          } catch (Exception e) {
		       getParent().showError(e);
		      }
                
		products.put(nimetus, prod);

	    }
            rs.close();
            st.close();
            con.close();
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return products;
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public Vector getCustomerProductsList(String agentCode) {
        connectionUp(url);
	/*
   select "Customer Code" AS klient ,"Stock Code" AS toode,Qty AS kogus from st02 where"Salesman"='45'
AND "Invoice order date">'20021101' AND "Actual Del  Date">'00000000'
 group by "Stock Code","Customer Code",qty  order by "Customer Code","Stock Code","Invoice order date" */

     /*   SELECT  ST02001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015
         FROM ST020100 WHERE ST02015>? AND ST02016>0 AND ST02020>0 GROUP BY ST02017,ST02001, ST02020,ST02015 ORDER BY ST02001,ST02017 ,ST02015 asc
*/
//Seelega tuleb teha h�kk

        String sql = "select kl.customercode AS klient ,t.productcode AS toode, r.quantity AS kogus ,d.billissuedate  AS lastdate  " +
				"from asVwOrder d  WITH(NOLOCK)INNER JOIN asVwOrderDetails r  WITH(NOLOCK) ON d.DeliveredOrderAsID = r.DeliveredOrderAsID " +
				"INNER JOIN asVwCustomer kl WITH(NOLOCK)   ON kl.customerasid=d.customerasid "+
				"INNER JOIN asVwProduct t  WITH(NOLOCK) ON t.productasid=r.productasid  " +
				" INNER JOIN  asVwProdGroup g ON g.prodgroupasid=t.prodgroupasid " +
				" WHERE billissuedate > ? GROUP BY t.productcode,kl.customercode,r.quantity,d.billissuedate ORDER BY customercode asc, " +
				"productcode,billissuedate asc";
      

	if (isDebug()) {
	    System.out.println(sql);
	}

	Vector custProds = new Vector();
	
          try {
            stmt = con.prepareStatement(sql);
           stmt.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
            if (agentCode != null){
             // stmt.setString(2, agentCode);
            }
            ResultSet	      rs = stmt.executeQuery();

            while (rs.next()) {
              String kliendikood = rs.getString("klient").trim();
              String tootekood = rs.getString("toode").trim();
              CustProd cp = new CustProd(kliendikood, tootekood, Double.parseDouble(rs.getString("kogus").trim()),0 ,0,
              		rs.getString("lastdate").substring(5,10),0,0,0," ");
                    custProds.add(cp);
              
            }
            rs.close();
            stmt.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
       
        connectionDown();
	return custProds;
    }

    public String getPilotDayData(Agent agent){
    	makeXMLandCSVs();
        return null;

    }


    public String getNonMobileOrders(Agent agent){
    	return null;
    
     
    }

    /**
     * Method declaration
     *
     *
     * @param agentCode
     *
     * @return
     *
     * @see
     */
    public double getSalary(String agentCode) {
	return 0;
    }
    


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
         String cmd = "select  employeecode AS agent, sum(billamount) AS summa from  "  +
			" asVwOrder  d INNER JOIN asVwEmployee a  ON a.employeeasid=d.employeeasid " +
			" where MONTH( billissuedate)=MONTH(GETDATE()) AND YEAR( billissuedate)=YEAR(GETDATE()) group by employeecode";
         log(cmd);

         try {
             stmt = con.prepareStatement(cmd);
             //stmt.setString(1, agentCode);

             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 1);

             ResultSet	      rs = stmt.executeQuery();

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





    public ManagerData getManagerData() throws SQLException{
      String sql = "SELECT clid AS klient,docsum AS summa,linecount AS ridu,text AS tekst," +
                   "sentlong AS kuupaev FROM dayreport WHERE sentdate>(DATE()-30) ORDER BY clid";
      Connection c = DriverManager.getConnection(url2);
      Statement st = c.createStatement();
      ResultSet rs = st.executeQuery(sql);


      String sql2 = "SELECT SL01001 AS kood, SL01040 AS aastamyyk,SL01041 AS kuumyyk,SY24003 AS agendinimi, SL01006 AS kontaktisik " +
                    ",SL01011 AS telefon, SL01024 AS maksetin,SL01036 AS ale" +
                    " FROM SL010100 LEFT JOIN SY240100 ON SY24002=SL01035 ORDER BY " +
                    "SL01002";
      connectionUp();
      Statement st2 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet rs2 = st2.executeQuery(sql2);

      StringBuffer sb = new StringBuffer();
      String sep = ";";
      while(rs.next()){
        String cliID = rs.getString("klient").trim();
        sb.append(cliID + sep);
        sb.append(rs.getString("kuupaev").trim() + sep);
        sb.append(rs.getDouble("summa") + sep);
        sb.append(rs.getInt("ridu") + sep);
        String tekst = ensureLen(rs.getString("tekst")," ");
        tekst = tekst.replace('\'', ' ').replace('\"', ' ').replace(';', ' ');
        if (tekst == null) tekst = " ";

        sb.append(tekst + sep);

        boolean has = scrollTo(cliID, rs2);
        if (has){
         sb.append(ensureLen(rs2.getString("aastamyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("kuumyyk"), "0") + sep);
         sb.append(ensureLen(rs2.getString("agendinimi"), " ") + sep);
         sb.append(ensureLen(rs2.getString("kontaktisik"), " ") + sep);
         String tel =  rs2.getString("telefon") ;
         tel.replace('\'', ' ').replace('\"', ' ').replace(';',' ');
         sb.append(ensureLen(tel, " ") + sep);
        // System.out.println(ensureLen(tel, " "));
         sb.append(ensureLen(rs2.getString("maksetin"), " ") + sep);
         sb.append(ensureLen(rs2.getString("ale"), "0") + sep);
        }

        sb.append("\n");
      }

      ManagerData md = new ManagerData();
      md.setClientVisitInfo(sb.toString());

      sql = "SELECT OR20003 AS klient,OR20019 AS agent,OR20017 AS agentname FROM OR200100 " +
          "WHERE OR20015>? GROUP BY OR20003,OR20019,OR20017 ORDER BY OR20003";

      PreparedStatement ps = con.prepareStatement(sql);
      ps.setTimestamp(1, new Timestamp(getStartingDate().getTime()));
      rs = ps.executeQuery();
      StringBuffer bf2 = new StringBuffer();
      while(rs.next()){
        bf2.append(rs.getString("klient").trim() + sep);
        bf2.append(rs.getString("agent").trim() + sep);
        String agentName = rs.getString("agentname");
         if (agentName != null) agentName = agentName.trim();
        if (agentName == null) agentName = " ";
        bf2.append(agentName + "\n");
      }

      md.setClientAgentInfo(bf2.toString());
      con.close();
      return md;
    }

    private boolean scrollTo(String id, ResultSet rs) throws SQLException{
      rs.beforeFirst();
      while(rs.next()){
        if (id.equals(rs.getString("kood").trim())){
          return true;
        }
      }
      return false;
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
    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getStartingTime() {
      return null;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getDriverVersion() {
	return DRIVER_VERSION;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getSubjectFilesTo() {
	return this.subjectFilesTo;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @see
     */
    public void log(String s) {
	getParent().log(s);
    }

    private static double parseDouble(String s){
      if (s.length() > 0) return Double.parseDouble(s);
      else return 0;
    }

       public NumberFormat getNF(){
         return NF;
       }

       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
       public void setDealers(TreeMap aDealers) {
        dealers = aDealers;
      }

    // private String url = "jdbc:DBF:///c://work/avallone/dbf";
    public static final String DRIVER_VERSION = "SkaiteEug";
    private String             url = "jdbc:odbc:EUGESTA";
    private String             url2 = "jdbc:odbc:report";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance(); 
    private TreeMap  dealers = new TreeMap();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

