/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */
public class Scala5iLATDOMDBDriver extends AbstractDBDriver implements BulkUpdater,Manager,Promo,Discounter{

    /**
     * Constructor declaration
     *
     *
     * @param parent
     *
     * @see
     */
    public Scala5iLATDOMDBDriver(EksportPanel parent) {
	super(parent);
        this.subjectFilesTo = parent.getString("filemsg");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
	
	 if(parent.getProps().getProperty("rate") != null){
    	 try {
    	    rate = Double.parseDouble(parent.getProps().getProperty("rate"));
    	    } catch (Exception e){
    	       parent.showError(e);
    	    }
    }
    }
    public String getProductSortingData(){
       	return null;
       }
    /**
     *
     */
	
    private double  rate = 0.702804;
    
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
        	public Vector getLastThreeData() {
        		// TODO Auto-generated method stub
        		return null;
        	}

        public String getSaveStockInfo(){
        	  connectionUp(url);
        	  String  cmd = "  SELECT  RTRIM(SC03001) AS toode , '01' AS LADU ,SC03003 AS stock   " +
		      "  FROM SC030200  WHERE SC03002='01' AND SC03003!=0 ORDER BY SC03001" ;


		       if (isDebug()) System.out.println(cmd);
		       StringBuffer products = new StringBuffer();
		       try {

		         Statement st = con.createStatement();
		         ResultSet rs = st.executeQuery(cmd);

		         while (rs.next()) {

		           String kood = rs.getString("toode").trim();
		           double seis = rs.getDouble("stock");
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
        


                                           public String getCustProdDiscounts(Agent agent){
                                            return null;
                                            }


        public String getDistribution() {
          return null;
        }

        public Vector getFixedAssortmentData() {
          return null;
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
    	StringBuffer buf = new StringBuffer();
        TreeMap   tm = new TreeMap();
        String    sql = " SELECT SY24002 AS liik ,SY24003 AS nimetus FROM SY240200 WHERE RTRIM(SY24003)<>'' AND SY24001 = 'IB' ORDER BY SY24003";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
          String kood = rs.getString("liik").trim();
          String nimi = rs.getString("nimetus").trim();
            tm.put(kood,Converter.enCodeEst(nimi) +" " + kood);
            buf.append(kood + ";" + nimi +"\n");
        }
        rs.close();
        st.close();
        con.close();

        if(buf.length()>2 ){
       	 
      	  String prodfile = "PRODGROUPS.csv";
                 File f = new File(getOutDir() + prodfile);
                 if (f.exists())f.delete();
                   try {
             		PrintWriter os = new PrintWriter(new FileWriter(
             				getOutDir() + prodfile, true));
             		os.write(buf.toString());
             		os.close();
             	  }catch (IOException ex) {
             		ex.printStackTrace();
             	  }
              
        }

        
        this.connectionDown();
        return tm;
 }

    public static final String cleanLitLatEst(String ret){
	    if (ret != null){
	      
	        ret = ret.replaceAll("�","e");
	        ret = ret.replace((char) 0x3f,' ');
	        
	        
	       // ret = ret.replaceAll("?","s");
	       // ret = ret.replaceAll("?","c");
	     // ret = ret.replace('',' ');&

	        ret = ret.replaceAll("&","and");
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
	        ret = ret.replaceAll("�","Pg.");
	        
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
	       
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","A");
	        ret = ret.replaceAll("�","O");
	        ret = ret.replaceAll("�","U");
	    }
	        
	        if(ret == null || ret.trim().length()==0){
	        return " ";
	        }else {
	        	//System.out.println(ret);
	        return ret;
	        }        
	      }
	

    
    public TreeMap getAgents() {
        connectionUp(url);
        String  cmd = "SELECT SY24002 AS yksus,SY24003 AS nimi FROM SY240200 WHERE SY24001='BK'";
	TreeMap agents = new TreeMap();
	
	try {
            stmt = con.prepareStatement(cmd);
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
              Agent agent = new Agent(rs.getString("yksus").trim());
              String nimi = rs.getString("nimi").trim();
              agent.setName(nimi);
              agent.setObject("01");
              agent.setIsPilot(true);
              agents.put(nimi, agent);
	    }
	    Agent aaa = new Agent("HOUSE1");
	    aaa.setName("HOUSE1");
	    aaa.setObject("01");
        aaa.setIsPilot(true);
	    agents.put("HOUSE1", aaa);
	    
	} catch (Exception e) {
	    getParent().showError(e);
	}
        connectionDown();
	return agents;
    }

    public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         StringBuffer buf = new StringBuffer();
         String sep= System.getProperty("line.separator");
         String header1 = " <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n <PriceList>" + sep +
         "  <PartnerId>PER</PartnerId>" + sep +
         "  <PartnerName>Pernod Ricard Latvia SIA</PartnerName>" + sep +
         "  <Relation>CUSTOMER</Relation>" +  sep +
         "  <Code>5414</Code>" + sep +
         "  <Currency>EUR</Currency>" + sep+

         "  <Type>Full</Type>" + sep+
          
         "  <details>" + sep;
         boolean first = true;
         String bottom1 = "    </details>\n" +
                          "    </PriceList>\n";
         
         int coun = 0;
         
         String sql = "select '' AS custid ,SC39002 AS clgroup ,SC39001  AS prodid,' ' AS prodgroup, " +
         " case when SC39006 <= getdate() AND (SC39007 > getdate()-1 OR SC39007 = null) then " +
         " SC39005 else SC39004 end AS price , (LEFT(RTRIM(SC01037),1)+' '+RTRIM(SC01002)+' ' " +
                  " +SC01003) AS nimi, SC01132 AS ean " +
         " FROM SC390200 left outer JOIN SC010200 ON SC01001  = SC39001 order by SC39002,SC39001";
         
           Statement st = null;
           try {
             connectionUp(url);
             st = con.createStatement();
             ResultSet rs = st.executeQuery(sql);

             while (rs.next()) {

               String custID = rs.getString("custid").trim();
               String clGroup = rs.getString("clgroup").trim();
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
               String nimi = rs.getString("nimi");
               String ean = rs.getString("ean");
               
             //  double disc = rs.getDouble("discPERC");
              // if(disc >0){
               //	price = price*(1-(disc/100));
              // }
               
               MatrixLine mline = new MatrixLine();
               

               if (custID != null && custID.trim().length() > 0) {
                 mline.setCustID(custID.trim());
               }
               if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
                 mline.setCliGroup(clGroup.trim());
                 if(clGroup.equals("58")){
                	 if(first){
                		 buf.append(header1);
                		 first = false;
                	 }
                	 coun++;
                	 
                	 buf.append("        <line number=\"" + coun + "\">" + sep);
                	 buf.append("        <item>" + prodID + "</item>" + sep);
                	 buf.append("        <name>" + cleanLitLatEst(nimi) + "</name>" + sep);
                	// buf.append("        <ean>" + ean + "</ean>" + sep);
                	 buf.append("        <price>" + price + "</price>" + sep);               	                 	 
                	 buf.append("        <promotion>false</promotion>" + sep +  
                                "        <fromdate>2011-01-01T00:00:00</fromdate>" + sep +
                			    "        <todate></todate>" + sep +
                			    "        </line>" + sep);
                	
                 }
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
             
             if(buf.length()>2){ 
          	   buf.append(bottom1);
              	 String custfile = "PRICE-" + getYYYYpMMpDDpTimeNiceString() + ".xml";
                   File f = new File(getOutDir() + custfile);
                   if (f.exists())f.delete();
                     try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getXMLDir() + custfile, true));
               		os.write(buf.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                
          }
             
           }
        
       catch (SQLException ex) {
             ex.printStackTrace();
           }
         return lines;
       }






    public TreeMap getCustomers(Agent agent){
        connectionUp(url);
        String cmd = "SELECT SL01002 AS nimi,SL01001 AS kood,(SL14004 + ' (' + SL14005 + ')') AS aadress, SC45004 AS ale,SL01036 AS ale2 ,  " +
       "   SL01009 AS visit, ' ' AS info ,SL01035 as agent,SL22005 as paym , SL01029 as klgrupp, " +
       " SL01075 as block , SL01052 as bank ,SL01094 as acc, " +
 " (SELECT (SL01003 + ' (' + SL01004 + ')') from SL010200 where SL01001=kl.SL01056) as jur,SL01026 as shipday " +
 " FROM SL010200 kl left JOIN SC450200   " +
 "		   ON SL01036=SC45003    LEFT OUTER JOIN SL140200 ON SL01001=SL14001 AND SL14002='00' " +
 "         LEFT OUTER JOIN SY240200 ON SL01035=SY24002 AND SY24001='BK' "+
  "        LEFT OUTER JOIN SL220200 ON SL01024=SL22002 AND SL22001='0'  "+
   "       WHERE ( SL01002 != '') or SL01001 = '2131' or SL01001 = '5582'  ORDER BY SL01002";  //2131 uus klient
        

        
	log(cmd);
	StringBuffer buf = new StringBuffer();

	TreeMap customers = new TreeMap();
        HashMap custMap = new HashMap();

	try {
            stmt = con.prepareStatement(cmd);
            if (agent != null){
//              stmt.setString(1, agent.getID());
            }
	    ResultSet	      rs = stmt.executeQuery();

	    while (rs.next()) {
		String nimi = rs.getString("nimi").trim().toUpperCase();
                Customer cust = new Customer(rs.getString("kood").trim());
                nimi = nimi + " " + cust.getID();
                cust.setName(Converter.enCodeEst(nimi));
                String aadress = parent.make(rs.getString("aadress"));
                cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadress), "Latvia"));
                String ale = rs.getString("ale");
                if (ale != null && ale.length() > 0){
                 // cust.setDiscount(Double.parseDouble(ale.trim()));
                }
                String ale2 = rs.getString("ale2");
                if (ale2 != null && ale2.trim().equals("04")){
                	//cust.setDiscount(17);
                	
                }
                cust.setVisit(rs.getString("visit").trim());
                String agen = rs.getString("agent");
                if(agen != null){
                  cust.addAgent(new Agent(agen.trim()));
                }
                
                String paym = rs.getString("paym");
                if(paym != null && paym.trim().length()>0){
                	cust.setPaymTerm(paym.trim());
                }
                cust.addAgent(new Agent("HOUSE1"));
                cust.addAgent(new Agent("SK"));
                
                if (cust.getID().equals("0100000")) {
                    Enumeration enums = getParent().getAgentsHash().elements();
                      while (enums.hasMoreElements()) {
                        Agent a = (Agent) enums.nextElement();
                        cust.addAgent(a);
                        }
                      }
             
                cust.setGroup(rs.getString("klgrupp"));
                
                String block  =  rs.getString("block");
                if(block != null && block.trim().length()>0){
                	if(block.trim().equals("1")){
                		cust.setSalesBlockT(true);
                	}
                	
                }
                
                
                String bank = rs.getString("bank");
                String acc = rs.getString("acc");
                String jur = rs.getString("jur");
                
                String shipDay  =  rs.getString("shipday");
                if(shipDay != null && shipDay.trim().length()>0 && shipDay.trim()!= "00"){       	
                	cust.setPersonalData("DelDay: " + shipDay);
                }
                
                buf.append(cust.getID().trim() + ";" + nimi + ";" +  agen + ";"  + aadress + ";" + cust.getPaymTerm() + ";" + 
                		parent.make(bank)+ ";" + parent.make(acc)+ ";" + parent.make(jur) + "\n");
                
                customers.put(cust.getName(), cust);
                custMap.put(cust.getID(), cust);
	    }
            rs.close();
            stmt.close();


            String sql = "SELECT SL01001 AS id,SUM(SL03013-SL03053) AS debt FROM SL030200 INNER JOIN SL010200 ON SL03001 = SL01001  WHERE  " +
                         " NOT(SL03013=SL03053) AND SL03006<=getdate() GROUP BY SL01001 ";

            stmt = con.prepareStatement(sql);

            rs = stmt.executeQuery();
            while(rs.next()){
              String id = rs.getString("id").trim();
              double debt = Double.parseDouble(rs.getString("debt").trim());
              Customer cust = (Customer)custMap.get(id);
              if (cust != null){
                cust.setDebt(cust.getDebt() + debt);
              }
            }
            rs.close();
            stmt.close();

            
            if(buf.length()>2){     	 
              	 String custfile = "CUSTOMER.csv";
                   File f = new File(getOutDir() + custfile);
                   if (f.exists())f.delete();
                     try {
               		PrintWriter os = new PrintWriter(new FileWriter(
               				getOutDir() + custfile, true));
               		os.write(buf.toString());
               		os.close();
               	  }catch (IOException ex) {
               		ex.printStackTrace();
               	  }
                
          }
            
        } catch (Exception e) {
            getParent().showError(e);
        }

        
	return customers;
    }

    public Vector getDebt(String agentCode) throws SQLException{
      Vector list = new Vector();
      String sql = "SELECT SL01001 AS klient, SL03002 AS number,SL03006 AS kuupaev,SL03013-SL03053 AS summa " +
      		"FROM SL030200 INNER JOIN SL010200 ON SL03001 = SL01001  WHERE NOT(SL03013=SL03053) AND " +
                   "LEN(LTRIM(SL03013))>0 AND SL03006<GETDATE()+5 ";
      sql += " ORDER BY SL01001";

      connectionUp(url);
      stmt = con.prepareStatement(sql);


      ResultSet rs = stmt.executeQuery();

      while(rs.next()){
        Debt debt = new Debt();
        debt.setCustID(rs.getString("klient").trim());
        debt.setDoknr(rs.getString("number").trim());

        debt.setDuedate(rs.getDate("kuupaev"));

        debt.setSum(Double.parseDouble(rs.getString("summa").trim()));
        if(debt.getSum()>0.04 || debt.getSum()<-0.04){
        	list.addElement(debt);	
        }
        }
      rs.close();
      stmt.close();
      return list;
    }

    public TreeMap getProducts(Agent agent){
        connectionUp(url);


        String cmd = "SELECT  RTRIM(SC01001) AS toode,(LEFT(RTRIM(SC01037),1)+' '+RTRIM(SC01002)+' ' " +
                  " +SC01003) AS nimi,SC39004 AS myygihind ,SC01010 AS tykke,0 AS stock, SC01011 AS maxale,SC01150 AS pandikas , " +
                   " case when  SC01031 = 1 then 1 else 0 end AS out ,SC01037 AS grupp,SC01132 AS ean " +
                " FROM SC010200  left outer JOIN SC390200 ON SC01001  = SC39001 and SC39002='00'" +
                 "     WHERE   LEN(LTRIM(SC01037))>0 and SC39004 is not null ORDER BY LEFT(RTRIM(SC01037),1),SC01001";
        

	if (isDebug()) {
	    System.out.println(cmd);
	}

	TreeMap products = new TreeMap();

    StringBuffer buf = new StringBuffer ();
	try {
          Statement st = con.createStatement();
	    ResultSet	      rs = st.executeQuery(cmd);

	    while (rs.next()) {
		String kood = rs.getString("toode").trim();
		String nimetus = rs.getString("nimi").trim();

                Product prod = new Product(kood);
                prod.setName("      " +Converter.enCodeEst(nimetus) + " " + kood);
                prod.setPrice(parseDouble(rs.getString("myygihind").trim()));
try{
	 prod.setInCases(parseDouble(rs.getString("tykke").trim()));	
}catch(Exception e){
	
}
               
                prod.setStock(0);

                String pant = rs.getString("pandikas");
                if(pant != null && pant.trim().length()>0){
                  prod.setPandPakendiKood(pant.trim());
                }
                String out = rs.getString("out");
                if(out != null && out.trim().equals("1")){
                //  prod.setStatusSellout("1");
                }
                String gr = rs.getString("grupp");
                if(gr != null && gr.trim().length()>0){
                  prod.setGroup(gr.trim());
                }
                 gr = rs.getString("ean");
                if(gr != null && gr.trim().length()>0){
                  prod.setEAN(gr.trim());
                }

                buf.append(kood + ";" + nimetus + ";" +prod.getInCases()
                		+ ";" + prod.getEAN() + ";" + prod.getGroup() + "\n");                
                prod.setVat("21");
		products.put(nimetus, prod);

	    }
            rs.close();
            st.close();
            
            

            if(buf.length()>2 ){
           	 
          	  String prodfile = "PRODUCTS.csv";
                     File f = new File(getOutDir() + prodfile);
                     if (f.exists())f.delete();
                       try {
                 		PrintWriter os = new PrintWriter(new FileWriter(
                 				getOutDir() + prodfile, true));
                 		os.write(buf.toString());
                 		os.close();
                 	  }catch (IOException ex) {
                 		ex.printStackTrace();
                 	  }
                  
            }
	} catch (Exception e) {
	    getParent().showError(e);
	}

	return products;
    }

    
    

    private String getOutDir(){
 	    String dir = getParent().getProps().getProperty("outdir");
 	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
 	    return dir;
 	  }
    

    private String getXMLDir(){
 	    String dir = getParent().getProps().getProperty("xmldir");
 	    if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
 	    return dir;
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

        String sql = "SELECT SL01001 AS klient ,OR21005  AS toode, (OR21011) AS kogus ,OR20015  AS lastdate FROM OR200200 INNER JOIN  " +
                    " OR210200 ON OR20001=OR21001 INNER JOIN SL010200 ON OR20003 = SL01001 WHERE OR20015>?   " +
                   " GROUP BY OR21005,SL01001,OR21011,OR20015 ORDER BY SL01001 asc,OR21005,OR20015 asc ";
        String sql2 = "SELECT  SL01001 AS klient ,ST02017 AS toode ,ST02020 AS Kogus,ST02015  as lastdate " +
                       " FROM ST020200 INNER JOIN SL010200 ON ST02001 = SL01001 WHERE ST02015>? AND ST02016>0 AND ST02020>0  " +
                      " GROUP BY ST02017,SL01001, ST02020,ST02015 ORDER BY SL01001,ST02017 ,ST02015 ASC";

	if (isDebug()) {
	    System.out.println(sql);
	}

	Vector custProds = new Vector();
        String[] sqls = new String[]{sql2, sql};
        for (int i = 0 ; i < sqls.length ; i++){
          try {
            stmt = con.prepareStatement(sqls[i]);
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
              if (i == 0){
                custProds.add(cp);
              } else if(!custProds.contains(cp)){
                custProds.add(cp);
              }
            }
            rs.close();
            stmt.close();
          } catch (Exception e) {
            getParent().showError(e);
          }
        }
        connectionDown();
	return custProds;
    }

    
	private String getDir() {
		String dir = getParent().getProps().getProperty("readin");
		if (!(dir.endsWith("/") || (dir.endsWith("\\"))))
			dir = dir + "/";
		return dir;
	}
    
	public  String getDateAAAAsDDsMMStr(){
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {day = "0" + day;}

		String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
		if (month.length() == 1){month = ("0") + month;}

		sb.append(("" + cal.get(Calendar.YEAR)));
		sb.append("-" + month + "-");
		sb.append(day +"");
		return sb.toString();
	}
	
	  public static String getYYYYpMMpDDpTimeNiceString(){
		    StringBuffer sb = new StringBuffer();
		    Calendar cal= Calendar.getInstance();
		    
		    String hour =   cal.get(Calendar.HOUR_OF_DAY) + "";
		    
		    
			String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			if (day.length() == 1) {day = "0" + day;}

			String month = String.valueOf(cal.get(Calendar.MONTH ) + 1);
			if (month.length() == 1){month = ("0") + month;}

			sb.append("" + cal.get(Calendar.YEAR));
			sb.append(month);
			sb.append(day +" ");
		    
		    String min = String.valueOf(cal.get(Calendar.MINUTE));
		    String sec = String.valueOf(cal.get(Calendar.SECOND));
		     if (hour.length() == 1){
		      sb.append("0");
		    }
		     sb.append(hour);
		     sb.append("_");
		    if (min.length() == 1){
		     sb.append("0");
		   }
		     sb.append(min);
		     sb.append("_");
		     if (sec.length() == 1){
		        sb.append("0");
		      }
		    sb.append(sec);
		    return sb.toString();
		  }

	
	public String getPilotDayData(Agent agent){
    	StringBuffer sb = new StringBuffer();
    	String[] names = (new File(getDir())).list();
    	String sep= System.getProperty("line.separator");
    	 
		String file = null;
		for (int i = 0; i < names.length; i++) {
			//orderreport-2010-03-03.txt
			System.out.println(names[i]);
			if ((names[i]).indexOf("orderreport") != -1 && (names[i]).indexOf(getDateAAAAsDDsMMStr()) != -1) {
				file = names[i];
				System.out.println("found" + names[i]);
			}
		}
		if (file != null) {

			try {
				FileReader fr = new FileReader(getDir() + file);
				BufferedReader reader = new BufferedReader(fr);
				String line = null;
				
				String header = getParent().getString("name") + "  \t\t" + getParent().getString("number") +
				 "\t" + getParent().getString("sum") + sep + "-------------------------------------------" + sep;
				 boolean heade = true;
				
				while ((line = reader.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(line, ";");
					   String nimi = null;
					   String number = null;
					   String ag = null;
					   String summa = null;
					   String kuupaev = null;
					  // String kood = null;
					   String lines = null;
					   //SE108-4;2010-03-02;5174;SE108;122.35;4
					if (st.hasMoreTokens()) {
				      number = st.nextToken();		
					}
					if (st.hasMoreTokens()) {
					 kuupaev = st.nextToken();				
					}

					if (st.hasMoreTokens()) {
					nimi = getCustName(st.nextToken());				
					}
					

					if (st.hasMoreTokens()) {
					 ag = st.nextToken();				
					}
					if (st.hasMoreTokens()) {
						 summa = st.nextToken();				
						}
					if (st.hasMoreTokens()) {
						 lines = st.nextToken();				
						}
					
				  
				     if (nimi == null){
				         nimi = " ";
				     } else {
				    	 nimi = nimi.trim();
				     }

				   
				     
				     if(ag!= null && ag.equals(agent.getID())){
				    		if (heade){
						 		sb.append(header);
						 		heade = false;
						 	}
				 	sb.append(nimi + " " +  kuupaev + "\n");
				    sb.append(number + "\t");
				    String sum = getNF().format(Double.parseDouble(summa.trim())) ;
				    sb.append(sum +  " <" + lines + ">\n" + sep);
				   
				   }				
				}
			}catch (Exception e ){
				
			}
			
		}
		return sb.toString();
    }
	 
	 private String getCustName(String kood){
		 
		 connectionUp(url);
	       String res = "";
	       try{
	    	   String    sql = " SELECT SL01002 AS nimi FROM SL010200 " +
                               " WHERE SL01001 = '" + kood + "'";
		        Statement st = con.createStatement();
		        ResultSet rs = st.executeQuery(sql);

		        while (rs.next()) {
		        	
		         res = rs.getString("nimi").trim();
		        }
		        rs.close();
		        st.close();
		        con.close();
		         
	       }catch(Exception e){
	    	   
	       }
	       System.out.println("nimi " + res);
	       
	        this.connectionDown();
	        return res;
		 
	 }
    
    public String getPilotDayData2(Agent agent){
        String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020200  " +
        " LEFT JOIN OR200200 ON  OR20001=ST02009 LEFT JOIN SL140100 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010100 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

StringBuffer sb = new StringBuffer();
  this.connectionUp();
try {
 stmt = con.prepareStatement(sql);
 stmt.setString(1, agent.getID());
 String sep= System.getProperty("line.separator");
 ResultSet	      rs = stmt.executeQuery();
 String header = getParent().getString("name") + "  \t\t" + getParent().getString("number") +
 "\t" + getParent().getString("sum") + sep + "-------------------------------------------" + sep;
 boolean heade = true;
 while(rs.next()){
 	if (heade){
 		sb.append(header);
 		heade = false;
 	}
 	
     String nimi = rs.getString("nimi");
     if (nimi == null){
     nimi = " ";
     } else {nimi = nimi.trim();}

 	sb.append(nimi + "\n");
   sb.append(rs.getString("number").trim() + "\t");
   String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
   sb.append(sum + "\n" + sep);
 }
} catch (Exception e) {
 getParent().showError(e);
}

connectionDown();

System.out.println(sb.toString());
return sb.toString();

    }


    public String getNonMobileOrders(Agent agent){
      connectionUp(url);

      /*String sql = "SELECT \"Order value\" AS summa,(RTRIM(\"sl01002\")+' '+(\"Customer name\")) AS Nimi," +
                  " i.\"order Number\" AS number FROM st02 i INNER JOIN or20 o ON " +
                   " o.\"Order Number\"=i.\"Order Number\" INNER JOIN sl14 k ON " +
                   " i.\"Customer Code\"=k.\"Customer Code\" INNER JOIN sl01 l ON \"sl01001\"= " +
                   " k.\"Customer Code\" WHERE \"actual del  date\"=? AND  length(\"Shipment Mark\")=0 ";
      */
      String sql = "SELECT RTRIM(SL01002) AS nimi, ST02009 AS number, sum(ST02040) AS summa FROM ST020200  " +
        " LEFT JOIN OR200200 ON  OR20001=ST02009 LEFT JOIN SL140200 k ON ST02001=SL14001  " +
        " LEFT JOIN SL010200 l ON SL01001=SL14001 WHERE ST02016>GETDATE()-1 AND ST02007=? " +
        " GROUP BY ST02009,SL01002";

      StringBuffer sb = new StringBuffer();

        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, agent.getID());
            String sep= System.getProperty("line.separator");
            ResultSet	      rs = stmt.executeQuery();
            String header = getParent().getString("name") + "  \t\t" + getParent().getString("number") +
            "\t" + getParent().getString("sum") + sep + "-------------------------------------------" + sep;
            boolean heade = true;
            while(rs.next()){
            	if (heade){
            		sb.append(header);
            		heade = false;
            	}
                String nimi = rs.getString("nimi");
                if (nimi == null){
                nimi = " ";
                } else {nimi = nimi.trim();}

            	sb.append(nimi + "\n");
              sb.append(rs.getString("number").trim() + "\t");
              String sum = getNF().format(Double.parseDouble(rs.getString("summa").trim())) ;
              sb.append(sum + sep +  sep);
            }
        } catch (Exception e) {
            getParent().showError(e);
        }

        connectionDown();
        return sb.toString();
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
      if (agentCode == null) return 0;
      connectionUp(url);
        String cmd = "SELECT SUM(OR20024) AS sales FROM OR200100 WHERE MONTH(OR20015)=" +
                     "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) AND OR20019=?";
        log(cmd);

        try {
            stmt = con.prepareStatement(cmd);
            stmt.setString(1, agentCode);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            ResultSet	      rs = stmt.executeQuery();

            while(rs.next()) {
              setSales(rs.getDouble("sales"));
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            getParent().showError(e);
        }

	return 0;
    }


    public void setBulkSales(Map agents) {
       //if (agentCode == null) return 0;
       connectionUp(url);
         String cmd = "SELECT OR20019 AS agent,SUM(OR20024) AS sales FROM OR200100 WHERE MONTH(OR20015)=" +
                      "MONTH(GETDATE()) AND YEAR(OR20015)=YEAR(GETDATE()) GROUP BY OR20019";
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
                   agent.setMonthSales((int)rs.getDouble("sales"));
                       }
                    }

             rs.close();
             stmt.close();

         } catch (Exception e) {
             getParent().showError(e);
         }

     }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      String sql = "SELECT ST02020 AS  kogus, SC01071 AS sisu FROM ST020100 INNER JOIN " +
                   "SC010100 ON ST02017=SC01001 WHERE ST02007=?" +
                   " AND YEAR(ST02015)=YEAR(GETDATE()) AND MONTH(ST02015)=MONTH(GETDATE()) AND SC01012=?";
      PreparedStatement pst = con.prepareStatement(sql);
      pst.setString(1, agentCode);
      //Calendar cal = Calendar.getInstance();
      //cal.set(Calendar.DAY_OF_MONTH, 1);
      //pst.setTimestamp(2, new Timestamp(cal.getTime().getTime()));
      pst.setString(2, String.valueOf(nr));
      ResultSet rs = pst.executeQuery();
      double total = 0;
      while(rs.next()){
        double kogus = rs.getDouble("kogus");
        String sisu = rs.getString("sisu");
        if (!sisu.equals("")){
          total += kogus*Double.parseDouble(sisu);
        }
      }
      rs.close();
      pst.close();
      //System.out.println("nr:" + nr + ", total:" + total);
      return total;
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

    public static final String DRIVER_VERSION = "Scala5iLATDOM";
    private String             url = "jdbc:odbc:scalaDB";
    private String             url2 = "jdbc:odbc:reportLAT";
    private Connection	       con = null;
    private PreparedStatement  stmt = null;
    private String	       subjectFilesTo = null;
    private NumberFormat NF = NumberFormat.getInstance();
}



/*--- formatting done in "Sun Java Convention" style on 04-26-2002 ---*/

