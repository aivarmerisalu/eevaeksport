package eevaeksport.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.CycleMatch;
import eevaeksport.Dealer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportFrame;
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

public class EugestaESTDBDriver extends AbstractDBDriver  implements Manager,Promo, BulkUpdater,Discounter,WebReporter,PictureMaker {
  public EugestaESTDBDriver(EksportPanel parent){
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

     PropertyConfigurator.configure("log4j.properties");
    try {
      Class.forName(databaseDriverName).newInstance();
    } catch (Exception e){
       parent.showError(e);
    }
    
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
    country = getParent().getProps().getProperty("country");
    
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  String country = null;
  
  

  
  /**
   *
   */
  public void connectionUp(){
  }

	public Vector getLastThreeData() {

		Vector result = new Vector();
		try {
		

	String sq3 = "   SELECT jou.clientBackOfficeId as klient, jou.deliverydate  as kuup , jou.productBackOfficeId as toode ,\r\n" + 
			"			 sum(case when jou.Quantity > 0 then jou.Quantity else 0 end) as kogus,\r\n" + 
			"			 sum(case when jou.Quantity < 0 then jou.Quantity else 0 end) as kogusmin, max (jou.invoiceNumber) as doknr ,\r\n" + 
			"			 case when  jou.deliverydate > CAST(GETDATE() AS DATE)then 1 else 0 end as color FROM EPASAULE_DELIVERIES  jou  WHERE  jou.deliverydate >=   \r\n" + 
			"						(select top 1 kuupaev  from ( 	 SELECT top 10 jou2.deliverydate as kuupaev FROM  EPASAULE_DELIVERIES jou2  where ltrim(jou2.clientBackOfficeId) = ltrim(jou.clientBackOfficeId)   \r\n" + 
			"						 and jou2.deliverydate > getdate()-14 and jou2.deliverydate < getdate()+4 group by jou2.clientBackOfficeId , jou2.deliverydate order by jou2.deliverydate desc) as dd order by kuupaev)   \r\n" + 
			"				 GROUP BY jou.clientBackOfficeId,jou.deliverydate , jou.productBackOfficeId ORDER BY jou.clientBackOfficeId,jou.deliverydate desc, max(jou.invoiceNumber) desc, jou.productBackOfficeId" ;
					
					Connection con3 = getNewConnection();
					Statement st3 = con3.createStatement();
					ResultSet rs3 = st3.executeQuery(sq3);
					while (rs3.next()) {
						MatrixLine ml = new MatrixLine();
						ml.setCustID(rs3.getString("klient").trim());
						ml.setStartDate(rs3.getString("kuup").substring(5,10));
						ml.setProdID(rs3.getString("toode").trim());
						ml.setPrice(rs3.getDouble("kogus"));
						double kmin = rs3.getDouble("kogusmin");
						
						ml.setMinAm(kmin);
						ml.setLineinfo(rs3.getString("doknr").trim());
						
						int color = rs3.getInt("color");
						if(color == 1) {
							ml.setCliGroup("green");
						}
                        
						if(kmin < 0) {
							ml.setCliGroup("red");
						}
					    
						result.add(ml);
					}
					rs3.close();
					st3.close();
					con3.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}


  public String getSaveDebtInfo() {
	
   	return null;
   }

  
  private void makePictureCat(){
	     
	  	    
	    	
	    	String pictssou = parent.getProps().getProperty("pict.dir");
	  	    String pictspda = parent.getProps().getProperty("pictpda.dir");
	       
	  	 try {
	     	  
	          File f = new File(pictspda +"list.txt");
	          if (f.exists())f.delete();
	         
	          File res2 = new File (pictspda);
	             String[] ff2 = res2.list();
	         	 for(int i = 0;ff2.length>i;i++){
	             	(new File ( pictspda + ff2[i])).delete(); 
	             }

	  		 TreeMap prods = this.getProducts(null);
	         StringBuffer buf = new StringBuffer();
	         Iterator enume = prods.keySet().iterator();
	    
	         while (enume.hasNext()) {
	    	    	String asi = (String)enume.next();
	    	    	Product prod = (Product)prods.get(asi);
	    	    	String toode = prod.getID().trim();  
	               if(toode.length() > 0){
	                	
	    	   	  res2 = new File (pictssou);
	              ff2 = res2.list();
	             
	         	 for(int i = 0;ff2.length>i;i++){
	             	if( ff2[i].startsWith(toode)){
	               // System.out.println("pilt olemas " + ff2[i]);
	            	File in = new File ( pictssou + ff2[i]); 
	              	if(in.isFile()){
	              	File inpda = new File ( pictspda + prod.getID()+ ".jpg"); 
	                  	Converter.shrinkAndCopyFile(in,inpda,400);
	            	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
	                    		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
	                    	}
	                   }
	                 }
	               }
	             }
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
	    



  public Vector getFixedAssortmentData() {
		Vector result = new Vector();
		
		try {
			
			String  cmd = " SELECT  assortimentBackOfficeId as ClientGR ,productBackOfficeId as ItemID ,NEWINASSORT as newin " + 
					"FROM EPASAULE_ASSORTMENTS where EG_ASSORTITEMSTATUS = 1 ";
		
		         if (isDebug()) System.out.println(cmd);
		           Connection con = getNewConnection();
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd);

		           while (rs.next()) {

		             String sortid = rs.getString("ClientGR").trim()+"x";
		             String prodID = rs.getString("ItemID").trim();
		             int optio = rs.getInt("newin");
		             
		   
						MatrixLine mline = new MatrixLine();
						

								if (sortid != null
										&& sortid.trim().length() > 1) {
									mline.setCliGroup(Converter.cleanLitLatEst(sortid.trim()).replaceAll(";", ":"));
								}

								if (prodID != null
										&& prodID.trim().length() > 0) {
									mline.setProdID(prodID.trim());
								}
								
								if(optio == 1) {
								   mline.setLineinfo("NEW"); // valikuline optional item
								}
								if(!inTT.contains(sortid)) {
									result.add(mline);	
								}
							}
						
					 rs.close();
					 st.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}


		System.out.println("fix size " + result.size());
		// System.exit(0);
		return result;

	
}


   public Vector getPriceMatrixData() {
       Vector lines = new Vector();
try {

	String sss = "Select c.pricelistBackOfficeId as pricegroup\r\n" + 
			"                       				 from  dbo.EPASAULE_CLIENTS c inner join EPASAULE_AGENTS_CLIENTS a on a.ClientBackofficeID = c.BackOfficeId group  by c.pricelistBackOfficeId";
	
                         
    Connection con = getNewConnection();
 	Statement stmt = con.createStatement();
    
 	 ResultSet  rs = stmt.executeQuery(sss);
 	 HashSet<String> prs = new HashSet<String>();

     while (rs.next()) {
    	 prs.add(rs.getString("pricegroup"));
     }
      rs.close();
 	
      
	    String sql4a = "SELECT p.pricelistBackOfficeId as grupp, p.productBackOfficeId as toode, p.price as hind" + 
	    		" FROM EPASAULE_PRICES p inner join EPASAULE_PRODUCTS a on a.backOfficeId = productBackOfficeId and p.price !=a.price where "
	    		+ "p.price > 0  " ;	
           rs = stmt.executeQuery(sql4a);
           while (rs.next()) {

                   String gr = rs.getString("grupp");
                   if(prs.contains(gr)) {
                   String tt = rs.getString("toode");
                   Double pr = rs.getDouble("hind");
                             MatrixLine mline = new MatrixLine();
                                       mline.setCliGroup(gr);
                                       mline.setProdID(tt);
                                       mline.setPrice(pr);
                               lines.add(mline);
                   }
                   
             }
           
           rs.close();    	
      
           String sql2 = "select   a.promotionBackOfficeId as clgroup , productBackOfficeId as prodid, [date from] as startday,[date to] as endday, \r\n" + 
           		"           		minPrice as price ,b.regionbackofficeid as dealer from dbo.EPASAULE_PROMOTIONOBJECTS a \r\n" + 
           		"           		inner join dbo.EPASAULE_PROMO b on a.promotionBackOfficeId = b.promotionBackOfficeId  \r\n" + 
           		"           		 where getdate() between [date from] and [date to] and datediff(day,[date from],[date to])< 91  and minqty = 0 \r\n" + 
           		"           		 group by  a.promotionBackOfficeId , productBackOfficeId, b.regionbackofficeid,  [date from] ,[date to], minPrice \r\n" + 
           		"           		 order by a.promotionBackOfficeId , productBackOfficeId, b.regionbackofficeid, [date from] ,[date to], minPrice";
           rs = stmt.executeQuery(sql2);
           while (rs.next()) {

           String clGroup = rs.getString("clgroup");
           String prodID = rs.getString("prodid").trim();
           String sdate = rs.getString("startday");
           String edate = rs.getString("endday");
           double price = rs.getDouble("price");
           String dealer = rs.getString("dealer");
           
           MatrixLine mline = new MatrixLine();
           mline.setCliGroup(clGroup);
           mline.setProdID(prodID);
           mline.setStartDate(sdate.substring(0,10));
           mline.setEndDate(edate.substring(0,10));
           mline.setPrice(price);
            if(dealer != null && !dealer.trim().equals("03N")) {// kui ei ole pealadu siis paneme külge
            	mline.dealer = dealer.trim();
            }
           lines.add(mline);
           
           }
           rs.close();
           stmt.close();
           

       } catch (SQLException ex) {
          ex.printStackTrace();
       }

       return lines;
       
   }
   
   
  public String getProductSortingData(){
   	return null;
   }

  public Vector getDebt(String agentCode) throws SQLException{
     return this.debt;
   }


  public String getAssortmentStr(Agent agent){
	  StringBuffer result = new StringBuffer();
		
		try {
			
			String  cmd = " SELECT  assortimentBackOfficeId as ClientGR ,productBackOfficeId as ItemID ,NEWINASSORT as newin " + 
					"FROM EPASAULE_ASSORTMENTS where EG_ASSORTITEMSTATUS = 1 ";
		
		         if (isDebug()) System.out.println(cmd);
		           Connection con = getNewConnection();
		           Statement st = con.createStatement();
		           ResultSet rs = st.executeQuery(cmd);

		           while (rs.next()) {

		             String sortid = rs.getString("ClientGR").trim();
		             String prodID = rs.getString("ItemID").trim();
		       
								if(inTT.contains(sortid)) {
									result.append( " ;" + Converter.cleanLitLatEst(sortid + "; ;" + prodID + "; ;")+ "\n");
		 						}
							}
						
					 rs.close();
					 st.close();
					con.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
             

		System.out.println("ASSO size " + result.toString());
	
		return result.toString();


  }

  public String getSaveStockInfo(){
	  	  

	  String cmd = " Select productBackofficeId as toode,sum(Inventory) as stock , regionBackofficeId as ladu   from "
	  		+ " dbo.EPASAULE_INVENTORIES   group by productBackofficeId,regionBackofficeId ";
       //where regionBackofficeId in ('03N', '03D', '03C')

	  
	       if (isDebug()) System.out.println(cmd);
	       StringBuffer products = new StringBuffer();
	       try {
	    	   Connection con = this.getNewConnection();
	         Statement st = con.createStatement();
	         ResultSet rs = st.executeQuery(cmd);
	      TreeMap koodid = new TreeMap ();
	         while (rs.next()) {

	           String kood = rs.getString("toode").trim();
	           double seis = rs.getDouble("stock");
	           String dealer = rs.getString("ladu");
	           
	           if(seis > 0){
	        	       products.append(kood +  ";");
	                   products.append(" ;");
	                   products.append(getParent().getNF().format(seis) + ";" + dealer +  "\n");
	                   products.append(kood +  ";");
	                   products.append(" ;");
	                   products.append(getParent().getNF().format(seis) + ";" + dealer +  "\n");
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

  
  HashSet usedProds = null;
  
  public DealerProduct getDealerProductsList() {
	  DealerProduct dealerProds = new DealerProduct();

      try {

    	  String  cmd = "Select productBackofficeId as toode,sum(Inventory) as stock ,regionBackofficeId as ladu from " + 
    	  		"	  		 dbo.EPASAULE_INVENTORIES  group by productBackofficeId,regionBackofficeId";
     // where regionBackofficeId in ('03N','03D','03C')
   		  if(usedProds == null){
   			  this.getProducts(null);
   		  }
           if (isDebug()) System.out.println(cmd);
           Connection con = getNewConnection();
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(cmd);
                while (rst.next()) {
                  String toode = rst.getString("toode");
                  if (toode != null){
                  toode = toode.trim();
                  if(usedProds.contains(toode)){
               	   double seis = rst.getDouble("stock");
               	        String ladu  =  rst.getString("ladu").trim();
	                    dealerProds.add(ladu, toode,((int)seis)+""," "," " );
                     }
               
                   }
                  }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          e.printStackTrace();
      }

      
        System.out.println( " dealerProds suurus " + dealerProds);
       return dealerProds;
     
     }

  
  
  public TreeMap getDealers() {

      TreeMap dealers = new TreeMap();
      try {

    	  String cmd = " select inventlocationid  as ladu , name from EPASAULE_REGIONS";

           if (isDebug()) System.out.println(cmd);
           Connection con = getNewConnection();
              Statement stm = con.createStatement();
              ResultSet rst = stm.executeQuery(cmd);
                while (rst.next()) {
                  String kood = rst.getString("ladu").trim();
                   Dealer dealer = new Dealer(kood);
                   dealer.setName(Converter.enCodeEst(rst.getString("name").trim()));
                   dealers.put(dealer.getID(), dealer);
                  }
              rst.close();
              stm.close();
              con.close();

      } catch (Exception e) {
          e.printStackTrace();
      }

      
      System.out.println("Dealers size :" + dealers.size());
      
	  return dealers;
	    }




              public String getDealerCustomerCSV(String agent){
               return null;
               }

                                                  public String  getCustProdDiscounts(Agent agent){
                                                   return null;
                                                   }


  /**
   *
   */
  public void connectionDown(){
  }

  private void makeCustprod(){
	  int counter = 1;
	  
	  String cmd2 = "WITH agg AS (\r\n" + 
	  		"    SELECT  \r\n" + 
	  		"        clientBackOfficeId AS klient,\r\n" + 
	  		"        productBackOfficeId AS toode,\r\n" + 
	  		"        (Quantity) AS sisse,\r\n" + 
	  		"        deliverydate as lastdate,\r\n" + 
	  		"        (lineAmount / CASE WHEN Quantity <> 0 THEN Quantity ELSE 1 END) AS lastprice,\r\n" + 
	  		"        invoiceNumber AS lastdoc,\r\n" + 
	  		"        ROW_NUMBER() OVER (\r\n" + 
	  		"            PARTITION BY clientBackOfficeId, productBackOfficeId\r\n" + 
	  		"            ORDER BY deliverydate DESC\r\n" + 
	  		"        ) AS rn\r\n" + 
	  		"    FROM EPASAULE_DELIVERIES\r\n" + 
	  		"    WHERE deliveryDate > GETDATE() - 180\r\n" + 
	  		"    GROUP BY \r\n" + 
	  		"        clientBackOfficeId,\r\n" + 
	  		"        productBackOfficeId,\r\n" + 
	  		"        invoiceNumber,\r\n" + 
	  		"        deliverydate,\r\n" + 
	  		"		Quantity,\r\n" + 
	  		"		lineAmount\r\n" + 
	  		")\r\n" + 
	  		"SELECT *\r\n" + 
	  		"FROM agg\r\n" + 
	  		"WHERE rn = 1\r\n" + 
	  		"ORDER BY klient, toode;";
	  
	  


	    if (isDebug()) System.out.println(cmd2);

	    try {
	    	
	    	
	    	Connection con = getNewConnection();
	     	Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(cmd2);
	
	
	      counter++;
	      while(rs.next()){
	        String kliendikood = rs.getString("klient").trim();
	        String tootekood = rs.getString("toode").trim();
	        double kogus = rs.getDouble("sisse");
	        
	        String lastdate = rs.getString("lastdate").substring(0,10);
	                 lastdate = lastdate.substring(5,10);
	                 double lastprice = rs.getDouble("lastprice");
	         String lastdoc = rs.getString("lastdoc").trim();
	      	               
	        CustProd cp = new CustProd(kliendikood, tootekood, kogus , 0,0,lastdate,lastprice,0,0," ");
                cp.setAbiKood(lastdoc);
	        //cp.setRedline(rs.getString("date"));
	        custProds.add(cp);
	        prods.addElement(tootekood);
	      }
	      rs.close();
	      stmt.close();
	      con.close();
	    } catch (Exception e){
	      getParent().showError(e);
	    }
  }
  
  public TreeMap getAgents(){
	  
	    
    String cmd = "select backOfficeId as kood ,agentName as name , repricingLimit as repr from  dbo.EPASAULE_AGENTS";

    String cmdg = "select Agent_BackofficeID as kood , group_backofficeID as grupp,forSale, forMerch from EPASAULE_AGENTS_EAGENTGROUPS";
    String cmdb = "select Agent_BackofficeID as kood , brand_backofficeID as brand,forSale, forMerch from EPASAULE_AGENTS_EAGENTBRANDS";
    
    if (isDebug()) System.out.println(cmd);
    TreeMap agents = new TreeMap();
    
    try {
    	Connection con = getNewConnection();
        Statement stmt =  con.createStatement();
        ResultSet	rs = stmt.executeQuery(cmdg);
        
        HashMap grs = new HashMap();
         
         while(rs.next()){
          String agentID = rs.getString("kood").trim();
          String gr = rs.getString("grupp").trim();
          ArrayList gr1 = (ArrayList)grs.get(agentID);
        		  if(gr1 == null) {
        			 gr1 =  new ArrayList();
        		  }
        	gr1.add(gr);
        	grs.put(agentID, gr1);
         }
        
        rs.close();
        
        HashMap grsS = new HashMap();
        
        rs = stmt.executeQuery(cmdb);
        while(rs.next()){
         String agentID = rs.getString("kood").trim();
         String br = rs.getString("brand").trim();
         ArrayList gr1 = (ArrayList)grsS.get(agentID);
       		  if(gr1 == null) {
       			 gr1 =  new ArrayList();
       		  }
       	gr1.add(br);
       	grsS.put(agentID, gr1);
        }
       
       rs.close();
        	
        rs = stmt.executeQuery(cmd);
    
        while(rs.next()){
        String agentID = rs.getString("kood").trim();
          Agent agent = new Agent(agentID);
          String name = rs.getString("name").trim();
          Double reprLim = rs.getDouble("repr");
          if(reprLim > 0) {
        	  agent.repriceLimit = reprLim;
          }
          agent.setName(name);
          agent.setIsPilot(true);
          
          if(grs.size() > 0 && grs.get(agentID) != null) {
        	  agent.setProdGroupList((ArrayList)grs.get(agentID));
          }
          
          if(grsS.size() > 0 && grsS.get(agentID) != null) {
        	  agent.setProdGroupListSec((ArrayList)grsS.get(agentID));
          }
          
          agent.setLanguage("EST");
          agent.setCountry("et");
          agent.setPassWord("0000");
        if(grs.size() > 0) {
        	
        }
          
          agents.put(name, agent);
          agent1.put(agent.getID(), agent);
        }
     rs.close();
     stmt.close();
     con.close();
         
   
    } catch (Exception e){
        getParent().showError(e);
      }
    
    return agents;
  }

  private TreeMap agent1 = new TreeMap();
  
  private TreeMap makePlansByCustomers(String fname){
	   TreeMap res = new TreeMap();
	  
	   try{
	
		Calendar cal = Calendar.getInstance();
	     String month = (cal.get(Calendar.MONTH)+1)+ "";
	     if(month.length() == 1){
		   month = "0" + month;
	      }
	   String jooksev = cal.get(Calendar.YEAR) + "" + month; 
	   int a = 0;
	   File f = new File (getExcelDir() + fname);
	   FileInputStream	 ff = new FileInputStream(f);
	
		XSSFWorkbook workbook = new XSSFWorkbook((ff));
		XSSFSheet sheet = workbook.getSheet(jooksev);
		System.out.println("jooksev " + jooksev + " " + sheet);
		XSSFRow row = null; 
				ff.close();	
				if(sheet != null){
				
					
					for(int i = 0 ; i < sheet.getLastRowNum();i++){
						 row = sheet.getRow(i);
						if(row == null && a == 0){
							a = i;
						}else{
							a = sheet.getLastRowNum();
						}
					}
					String gr1 = null;
					String gr1n = null;
				
					
					for(int i = 0 ; i < a + 1;i++){
						 row = sheet.getRow(i);
						String cust = null;
						double g1 = 0;
					
						
					if(i == 0){
						gr1 = "1";//(row.getCell((short) 1).getStringCellValue());
						gr1n = "salds";
						
						if(fname.equals("Budget.xlsx")) {
							gr1n = "maize";
						}
					}else{
						
						try {
							cust = (row.getCell((short) 0).getStringCellValue());
 					      } catch (Exception e) {
 					    	 cust =( (int)(row.getCell((short) 0).getNumericCellValue()))+"";
 						  // e.printStackTrace();
 					      }
		
						try {
						g1 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
						} catch (Exception e) {
	 	   					 e.printStackTrace();	
	  					      }
					
					
						
						TreeMap asi= new TreeMap();
						       Toode t1 = new Toode(gr1);
						       t1.setBrand(gr1n);
						       t1.setPlancur(g1);
				          asi.put(gr1, t1);
						
						if(cust != null){
				         res.put(cust,asi );
						}
 					  }
					}
				}
      		   
	   }catch (Exception e){
		   e.printStackTrace();
	   }
	//  System.out.println("kleindi plaanid " + res.toString());
	//  System.exit(0);
	   return res;
 }
  
 TreeMap custplans = null;
  
  
  public TreeMap getCustPlans(){
	  int counter = 1;
	TreeMap res = new TreeMap();
	if(custplans == null) {
		 custplans = this.makePlansByCustomers("Budget.xlsx");	
	}
     


String sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end as grupp ,round(SUM(lineamount),0) AS summa , "
		+ "round(sum(inv.weightkg*sal.qty),2) AS summakg \r\n" + 
		"   FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON  \r\n" + 
		"   sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid WHERE \r\n" + 
		"   inv.dataareaid='eee'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate()) and jou.orderaccount != '' \r\n" + 
		"   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";

 sqlo = "   SELECT  jou.orderaccount as agent,case when itemgroupid = 'SALD' then 'magus' else 'leib' end  as grupp ,round(sum(inv.weightkg*sal.qty),2) AS summakg , "
		+ "  round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa  \r\n" + 
		"  FROM custinvoicetrans sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN custinvoicejour jou with (nolock) ON \r\n" + 
		"  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid "
		+ "left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and " + 
		"    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid   WHERE " + 
		" inv.dataareaid='eee'  AND YEAR(sal.invoicedate) = YEAR(getdate())  \r\n" + 
		"   AND MONTH(sal.invoicedate) = MONTH(getdate())   GROUP BY jou.orderaccount,case when itemgroupid = 'SALD' then 'magus' else 'leib' end";
 



          try {
          	
          	Connection con = this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(sqlo);
              while(rs.next()) {
                String custID = rs.getString("agent").trim();
                String grupp = rs.getString("grupp").trim();
                
                int sum = (int)rs.getDouble("summa");
                if (custplans.get(custID) != null){
                  TreeMap tr = (TreeMap) custplans.get(custID);
if(grupp.equals("leib") ){
                  Toode tt = (Toode)tr.get("1");
                  tt.setDonecur(tt.getDonecur() + sum);
                  }

                }
              }
              rs.close();
              stmt.close();
              con.close();

              
                          
                            int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                            int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                      	  
                      	 // Iterreerime Plaani mappi ja sealme asjad paika 
                      	  
                            
                            
                      	Iterator itera =   custplans.keySet().iterator();
                      	while(itera.hasNext()){
                      		String custID = itera.next() + "";
                      		TreeMap tr = (TreeMap) custplans.get(custID);
                      		
                            if (tr != null){
                      	  

                        
                         String asi2 = "";
                         
                      	  Iterator enume = tr.keySet().iterator();
     	                	 while(enume.hasNext()){
     	                		 String brand = enume.next()+"";
     	                		 if(!brand.equals("allx")){
     	                			Toode tt = (Toode)tr.get(brand);
     	                			int teh = (int)tt.getDonecur();
     	                			if(tt.getPlancur() > 0){
     	                				asi2 = asi2 + ("Gr. " + tt.getBrand() + "    " +
         	                					((int)(teh/tt.getPlancur()*100))
         	                					+ "% Plan.: " +(int) tt.getPlancur() + " €.  Sales.: " + teh + " €.||");	
     	                			}
        	                		;
     	                		   }
     	                		 }
                         res.put(custID, asi2 );
                      	}
                            }
          } catch (Exception e) {
              getParent().showError(e);
              if(counter < 3 ) {
            	  counter++;
            	  getCustPlans();
              }
           }
          
          return res;
        }

  
  
  private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
  

  private TreeMap makeCustomerDeliveryDays(){
		TreeMap res = new TreeMap();
		try{

	      
	String sql3 = "select    BackOfficeId as klient ,  nextdelivery as paev1 ,nextdelivery2 as paev2 \r\n" + 
			"	  from dbo.EPASAULE_CLIENTS";

	Connection con = this.getNewConnection();
		Statement stmt = con.createStatement();

	ResultSet rs3 = stmt.executeQuery(sql3);
	while(rs3.next()){
	String klient = rs3.getString("klient").trim();
	java.sql.Date day1 = rs3.getDate("paev1");
	java.sql.Date day2 = rs3.getDate("paev2");

	String olemas = null;
	
	if (day1.getTime() > 10000) {
		olemas = day1.getTime() + "";
		
		if (day2.getTime() != day1.getTime() && day2.getTime() > 10000) {
	        olemas = olemas + "," +  day2.getTime();
		}
		
			res.put(klient, olemas);
		}
	


	}
	rs3.close();
	stmt.close();
	con.close();
	 
	  }catch (Exception e){
		  e.printStackTrace();
	    //getParent().showError(e);
	  }
	  return res;
	}
  

  
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final LocalDate CYCLE_START =   LocalDate.parse("01.11.2021", FMT);
  
  
 
  public static boolean matchesCycle( String pattern) {

      LocalDate date = LocalDate.now();  // or any date to test
	  
      long days = Duration.between(CYCLE_START.atStartOfDay(), date.atStartOfDay()).toDays();

        if (days < 0) {
          return false; // before cycle start
         }

      int weekInCycle = (int)((days / 7) % 8) + 1;  // 1–8
      int weekday = date.getDayOfWeek().getValue(); // 1=Mon…7=Sun

      char cycleWeekLetter = (char)('A' + (weekInCycle - 1)); // A–H

      // Build what we expect, e.g. "A1"
      String expected = "" + cycleWeekLetter + weekday;

      // Check if pattern contains expected code
      return pattern.contains(expected);
  }
  
  
  public static boolean matchesNextSevenDays(String pattern) {
	    return !nextSevenDaysMatches(pattern).isEmpty();
	}
  
  public static List<CycleMatch> nextSevenDaysMatches(String pattern) {

	    DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	    LocalDate cycleStart = LocalDate.parse("01.11.2021", FMT);

	    LocalDate today = LocalDate.now();
	    List<CycleMatch> result = new ArrayList<>();

	    for (int i = 0; i < 7; i++) {

	        LocalDate date = today.plusDays(i);

	        long days = ChronoUnit.DAYS.between(cycleStart, date);
	        if (days < 0) continue;

	        int weekInCycle = (int) ((days / 7) % 8);
	        int weekday = date.getDayOfWeek().getValue();

	        char cycleWeekLetter = (char) ('A' + weekInCycle);
	        String expected = "" + cycleWeekLetter + weekday;

	        if (pattern.contains(expected)) {
	            result.add(new CycleMatch(date, expected));
	        }
	    }

	    return result;
	}


	

	public static LocalDate nextMatchingDate(String pattern) {

	    LocalDate date = LocalDate.now().plusDays(1); // start from tomorrow

	    while (true) {

	        long days = ChronoUnit.DAYS.between(
	                CYCLE_START,
	                date
	        );

	        if (days >= 0) {
	            int weekInCycle = (int) ((days / 7) % 8) + 1; // 1–8
	            int weekday = date.getDayOfWeek().getValue(); // 1–7

	            char weekLetter = (char) ('A' + weekInCycle - 1);
	            String code = "" + weekLetter + weekday;

	            if (pattern.contains(code)) {
	                return date;
	            }
	        }

	        date = date.plusDays(1);
	    }
	}

  
  
  public TreeMap getCustomers(Agent agent){
	  nf.setMinimumFractionDigits(2);
	
   try{
    	   Converter.getManagersReplacements(agent1, rcompany, user, pass, false); 
    } catch (Exception e){
    	e.printStackTrace();
    }
	//  makeCustprod();
	  
    TreeMap deldays =  makeCustomerDeliveryDays();
   
     inTT = new HashSet<String>();
	
	HashMap<String, String> promos = new HashMap<String, String>();
	
    HashMap<String, Vector<Agent>> cliags = new HashMap<String, Vector<Agent>>();
  
    
    TreeMap customers = new TreeMap();
    TreeMap custMap = new TreeMap();
    TreeMap indeksT = new TreeMap();
    
    try {
    	
    	   String aaa = "SELECT AgentBackofficeID as kood , ClientBackofficeID as klient, route  from  dbo.EPASAULE_AGENTS_CLIENTS " ;
      		
    	   Connection con = this.getNewConnection();
    	 
    		 Statement stmt = con.createStatement();
    	     ResultSet rs1 = stmt.executeQuery(aaa);
    	               while(rs1.next()){
    	                 Agent ag = new Agent(rs1.getString("kood").trim());
    	                 String cli = rs1.getString("klient").trim();
    	                 String route = rs1.getString("route");
    	                 if(route != null && route.trim().length() > 0) {
    	                	 List<CycleMatch> ll =  nextSevenDaysMatches(route);
    	                	 if(ll.size() > 0){
    	                		 String dd = "";
    	                	   for(CycleMatch cc : ll) {
    	                		 if( cc.dayOfWeek == DayOfWeek.MONDAY ) {
    	                			 dd = dd + "E";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.TUESDAY ) {
    	                			 dd = dd + "T";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.WEDNESDAY ) {
    	                			 dd = dd + "K";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.THURSDAY ) {
    	                			 dd = dd + "N";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.FRIDAY ) {
    	                			 dd = dd + "R";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.SATURDAY ) {
    	                			 dd = dd + "L";
    	                		 }
    	                		 if( cc.dayOfWeek == DayOfWeek.SUNDAY ) {
    	                			 dd = dd + "P";
    	                		 }
    	                	 }
    	                	
    	                	 if(dd.length() > 0) {
    	                	   ag.setVisit(dd); 
    	                	 }
    	                		 
    	                	 }
    	                		
    	                	 
    	                	ag.nextContact = nextMatchingDate(route);
    	                 }
    	                 Vector<Agent> vv = cliags.get(cli);
    	                 if(vv == null) {
    	                	 vv = new Vector<Agent>();
    	                 }
    	                 vv.add(ag);
    	                 cliags.put(cli, vv);
    	               }
    	              rs1.close();
    		
    	              
    	              aaa = "SELECT [ACCOUNTNUM] as klient ,[ZIPCODE] as indeks FROM [DWHEE].[dbo].[BID02_CUSTORDER]";
    	              
    	
    	            	rs1 = stmt.executeQuery(aaa);
	       	               while(rs1.next()){
	       	                 String cli = rs1.getString("klient").trim();
	       	                 String indeks = rs1.getString("indeks");
	       	                 if(indeks != null && indeks.length() > 0) {
	       	                	indeksT.put(cli, indeks);
	       	                 }
	       	                  
	       	                  }
	       	              rs1.close();
	       	              
	    	  	            
    	              
    	              aaa = "select clientBackOfficeId as klient,  a.promotionBackOfficeId as promo   from dbo.EPASAULE_PROMOTIONOBJECTS a inner join dbo.EPASAULE_PROMO b on a.promotionBackOfficeId = b.promotionBackOfficeId\r\n" + 
    	              		"	where getdate() between [date from] and [date to] and datediff(day,[date from],[date to])< 91\r\n" + 
    	              		"	group by  a.promotionBackOfficeId , clientBackOfficeId \r\n" + 
    	              		"	order by  clientBackOfficeId  " ;
    	        		
    	              	rs1 = stmt.executeQuery(aaa);
    	       	               while(rs1.next()){
    	       	                 String cli = rs1.getString("klient").trim();
    	       	                 String route = rs1.getString("promo").trim();
    	       	                 String aa = promos.get(cli);
    	       	                 if(aa == null) {
    	       	                	 promos.put(cli, route);
    	       	                 }else {
    	       	                	promos.put(cli, aa + "|" + route);
    	       	                 }
    	       	                  
    	       	                  }
    	       	              rs1.close();
    	       	              stmt.close();
    	       		
    	       	
    	
    	
      	
    	TreeMap<String,Double> debts = new TreeMap<String ,Double>();
    	
    	 String cmd2 = "  SELECT sum(Amount) as tasumata , debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS where \r\n" + 
    	 		" payDate<getdate() group by debetorBackOfficeId"; 
    	 		
    	 
    	  stmt = con.createStatement();
            rs1 = stmt.executeQuery(cmd2);
                     while(rs1.next()){
                       double volg = rs1.getDouble("tasumata");
                       String pay = rs1.getString("maksja").trim();
                       debts.put(pay, volg);
                     }
                    rs1.close();
                    stmt.close();
    	
                    TreeMap<String,Double> debtsall = new TreeMap<String ,Double>();
                	
               	 String cmdall = "  SELECT sum(Amount) as tasumata , debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS group by debetorBackOfficeId"; 
               	 
               	    stmt = con.createStatement();
                       rs1 = stmt.executeQuery(cmdall);
                                while(rs1.next()){
                                  double volg = rs1.getDouble("tasumata");
                                  String pay = rs1.getString("maksja").trim();
                                  debtsall.put(pay, volg);
                                }
                               rs1.close();
                               stmt.close();
               	
                               
              	  
                    String cmd = " Select name ,BackOfficeId as accountnum, debitorBackOfficeId as payer,CreditLimit as limit , minOrderSum as minsum, '' as linedisc \r\n" + 
                    		"                       		, pricelistBackOfficeId as pricegroup,address ,\r\n" + 
                    		"                       		  Status AS block , channel as chain , clientType as typeo,b.description as type, phone , contactPerson as pername, email as peremail,\r\n" + 
                    		"                       		 contactPhone as perphone,  accountantemail as acco,  EG_EMAILDEBTS as bill ,consignationPeriod as term,city as city  , alcoLicense as alic, tobaccoLicense as tlic,\r\n" + 
                    		"                       		 contractDate as cdate , segmentname as seg , assortimentBackOfficeId as fixed ,subsegmentname as segsub , gkrnextdelivery as via3l, nextdelivery as del1, nextdelivery2 as del2 \r\n" + 
                    		"                       		 from  dbo.EPASAULE_CLIENTS a  left join BID29_LINEOFBUSINESS b on a.clientType=b.LINEOFBUSINESSID " ; 
                    

                     if (isDebug()) System.out.println(cmd);
                    
                    
                    
    	 stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(cmd);
 
      while(rs.next()){
        String name = rs.getString("name").trim();
        
        boolean goes = true;
        
        String accountnum = rs.getString("accountnum").trim();
       
        Customer cust = new Customer(accountnum);
        cust.setPayer(rs.getString("payer"));
      
        Double limit = rs.getDouble("limit");
        if(limit != null) {
        	cust.setCredit(limit+"");
        }
        
        Double minsum = rs.getDouble("minsum");
        if(minsum != null) {
        	cust.setMinsum(minsum);
        }
        
        
        boolean de = true;
        String regno  = cust.getPayer();
        
        if(regno != null && regno.trim().length() > 0 && 
        		(regno.trim().equals("10714195") || regno.trim().equals("10180925") // abc + circle
        				|| regno.trim().equals("10765896") || regno.trim().equals("10263574") // maxima + rimi
        				|| regno.trim().equals("10379733")  )) { // selver + 
        	//de = false;
        }
        
      
        	
        if(de) {
        	if(debts.get(cust.getPayer()) != null) {
        		 cust.setDebt(debts.get(cust.getPayer()));
        	}else if(debts.get(cust.getID()) != null) {
        		 cust.setDebt(debts.get(cust.getID()));
        	}
        	
        if(debtsall.get(cust.getPayer()) != null) {
       		 cust.setUsedCredit(debtsall.get(cust.getPayer()));
         	} else if(debtsall.get(cust.getID()) != null) {
       		cust.setUsedCredit(debtsall.get(cust.getID()));
       	    }
        	
        }
        

       
        
        String aleke = rs.getString("linedisc");
        if(aleke != null && aleke.trim().length()>0){
            aleke = aleke.trim();
            cust.setGroup(aleke);
        } else {
        	aleke = "000" ;
        }

        String pricegroup = rs.getString("pricegroup");
        if(pricegroup != null && pricegroup.trim().length()>0){
        	
       if(!pricegroup.trim().equals("BASE")) {
    	   
         if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
        	 cust.setGroup(cust.getGroup() + "|" +pricegroup.trim());
         } else {
        	 cust.setGroup(pricegroup.trim());
         }
       }
         
        } 
        
        if(promos.get(cust.getID()) != null) {
        	 if(cust.getGroup() != null && cust.getGroup().trim().length() > 0) {
            	 cust.setGroup(cust.getGroup() + "|" +promos.get(cust.getID()));
             } else {
            	 cust.setGroup(promos.get(cust.getID()));
             }
        }
        
        String aadr = rs.getString("address");
        if (aadr != null) {
        	if(aadr.endsWith(", %1")) {
        	 aadr = aadr.substring(0,aadr.indexOf(", %1")).trim();
        	}
        	aadr = aadr.replaceAll("\\s+", " ").trim();
        	
          cust.setAddress(Converter.adressLink(Converter.enCodeEst(aadr.trim().replace('\n',' ').replaceAll(country, " ").replaceAll("\\s+", " ").trim()), country));
       
          if(aadr.contains(",")) {
          		cust.restStreet = aadr.trim().substring(0,aadr.trim().indexOf(","));
          		cust.restCity = aadr.trim().substring(aadr.trim().indexOf(",")+1);
          	}else {
          		cust.restStreet = aadr.trim();	
          	}
           	cust.setCountry(country);
          }
        
        if(deldays.get(cust.getID())  != null){
        	cust.setDelDays((String)deldays.get(cust.getID()));
        }    

        
        cust.restName = name;
        name = Converter.enCodeEst(name);
        name = name.replaceAll("\\([^)]*\\)", "").replaceAll("\\s+", " ").trim();
        name = name + " " + cust.getAddress() + " " + accountnum ;
        cust.setName(name);
        
        if(indeksT.get(cust.getID()) != null){
        	cust.addCity("Postiindeks: " + indeksT.get(cust.getID()));
        }
        
        
      /*  String visit = rs.getString("visit");
        if (visit != null && visit.trim().length()>0) {
          cust.setVisit(visit.trim());
        }*/
        
       if(cliags.get(cust.getID()) != null) {
         Vector<Agent> vv = cliags.get(cust.getID());
         
         for(Agent agentL : vv) {
          cust.addAgent(agentL);
          if(agentL.nextContact != null) {
              cust.addCity("Järgmine: " + agentL.getID() + " '" + agentL.nextContact.format(FMT).replace('.', '-').replace('.', '-') + "'");  
          }
        
         Agent aa1 = (Agent)agent1.get(agentL.getID());
           if(aa1 != null) {
      		Vector vec = aa1.getDubbler();
  		      if(vec!= null && vec.size() >0){
  			   Enumeration en = vec.elements();
  			   while(en.hasMoreElements()){
  				 String age = (String) en.nextElement();
  				 cust.addAgent(new Agent(age));
  			   }
  	       }
          }
         }
     	}
      


        
       
        if(de) {
        String block = rs.getString("block");
        if (block.equals("B")) {
          cust.setSalesBlockT(true);
        }
        }
        
        
        
        String chain = rs.getString("chain");
        if(chain != null && chain.trim().length() > 0) {
        	cust.setChain(chain);
        }
        
        String type = rs.getString("type");
        if(type != null && type.trim().length() > 0) {
        	//cust.setCustType(type);
        }
        
        
        String info = rs.getString("phone");
        String pername = rs.getString("pername");
        String peremail = rs.getString("peremail");
        String perphone = rs.getString("perphone");
        
        if (pername != null && pername.trim().length()>0 && !cust.restName.contains(pername)) {
            cust.restPerson = pername.trim(); 
            cust.adPersonalData( pername.trim() + "|");
        }
        

        if (perphone != null && perphone.trim().length()>0) {
            cust.restPerPhone = perphone.trim(); 
            cust.adPersonalData("Tel:" + perphone.trim() + "|");
            }
        
        
        
        if (peremail != null && peremail.trim().length()>0) {
            cust.restPerEmail = peremail.trim();
            if(!cust.getPersonalData().contains(peremail.trim())) {
               cust.adPersonalData( " " +peremail.trim().toLowerCase() + "|");
            }
                 
        }
        

        if (info != null && info.trim().length()>0) {
        	 if(!cust.getPersonalData().contains(info.trim())) {
                cust.adPersonalData("Tel:" + info.replaceAll("/", " / ").trim() + "|");
          }}
        
          
        
        String raam = rs.getString("acco");
        
        if (raam != null && raam.trim().length()>0) {
           if(raam.contains(";")) {
        	   raam = raam.substring(0, raam.indexOf(";"));
           }
        
           if(raam.indexOf("@") != -1) {
        	   if(!raam.toLowerCase().contains("arvekontroll")) {
        	        cust.adPersonalData("RP: " + raam.trim().toLowerCase() + "|");
        	   }
           }
        }
        
        
String arve = rs.getString("bill");
        
        if (arve != null && arve.trim().length()>0) {
           if(arve.contains(";")) {
        	   arve = arve.substring(0, arve.indexOf(";"));
           }
           
           if(arve.indexOf("@") != -1) {
        	        cust.adPersonalData("Arve: " + arve.trim().toLowerCase());
           }
        }
        
        
        if(cust.getPersonalData().endsWith("|")) {
        	cust.setPersonalData(cust.getPersonalData().substring(0,cust.getPersonalData().length()-1));
        }
        
        Integer term = rs.getInt("term");
        if(term != null && term > 0) {
        	cust.setPaymTerm(term + "");
        }

        String alic = rs.getString("alic");
        if(alic != null && alic.trim().length() > 0) {
        	//cust.setPaymTerm(term + "");
        }
        
        String tlic = rs.getString("tlic");
        if(tlic != null && tlic.trim().length() > 0) {
        	//cust.setPaymTerm(term + "");
        }
        
        java.sql.Date cdate = rs.getDate("cdate");
        if(cdate.getYear() > 1900) {
        	
        }
        
        
        
        
        String seg = rs.getString("seg");
        
        if(seg != null && seg.trim().length() > 0) {
        	cust.addCity("Kett: " + seg.trim());
        	
        }

        
        
        String fixed = rs.getString("fixed");
        if(fixed != null && fixed.trim().length() > 0) {
        	if(seg != null && seg.trim().equals("TT")) {
        		inTT.add(fixed.trim());
        		cust.suggAssortCode = fixed.trim();
        	 } else {
        	  cust.addFixSortCode(fixed.trim()+"x");
        	}
        }
        String segsub = rs.getString("segsub");
        if(segsub != null && segsub.trim().length() > 0) {
        	cust.addCity("Segment: " + segsub.trim());
        }
        
        if(type != null && type.trim().length() > 0) {
        	cust.addCity("Tüüp: " + type.trim());
        }
        
        String via3l = rs.getString("via3l");
        String del1 = rs.getString("del1");
        String del2 = rs.getString("del2");
        if(via3l != null && via3l.trim().length() > 0) {
        	cust.addCity("GKR Tarne: '" + via3l.trim() + "'");
        }
        
        if(del1 != null && del1.trim().length() > 0) {
        	cust.addCity("EUG Tarne: '" + del1.trim() + "'");
        }
        if(del2 != null && del2.trim().length() > 0) {
        	//cust.addCity("Tarne 2: '" + del2.trim()+ "'");
        }
        
        cust.setDealer(new Dealer("03N"));
        
        if(goes) {
            customers.put(name, cust);
            custMap.put(cust.getID(),cust);	
        }
  
      }



  rs.close();
  

	 String sql = " SELECT clientBackOfficeId as klient, documentNo as number,payDate as maksepaev,\r\n" + 
	 		" Amount as tasumata ,Date as kuupaev , datediff(day,payDate,getdate())as yle ,debetorBackOfficeId as maksja  FROM dbo.EPASAULE_DEBTS "; 
  
  rs = stmt.executeQuery(sql);
  while(rs.next()){
    Debt debt = new Debt();
    debt.setCustID(rs.getString("klient").trim());
    debt.setPayerID(debt.getCustID());
    String number = rs.getString("number").trim();
    debt.setDoknr(number);
    debt.setDuedate(rs.getDate("maksepaev"));
    debt.setSum(rs.getDouble("tasumata"));
    debt.setDate(rs.getDate("kuupaev"));
    debt.setDue(rs.getString("yle"));
    debt.setPayerID(rs.getString("maksja").trim());
    this.debt.addElement(debt);
  }
  
  
  
  
  String fff = "select BackOfficeId as klient,contactPerson as nimi ,  \r\n" + 
  		"							 '' as synna , email  as email,contactPhone  as phone from  dbo.EPASAULE_CLIENTS where contactPerson != ''";
 

      StringBuffer sb2 = new StringBuffer();
   
	   rs1 = stmt.executeQuery(fff);
      while(rs1.next()){
   	   String klient = rs1.getString("klient").trim();
   	   String nimi = rs1.getString("nimi").trim();
   	   java.sql.Date synna = null ;//rs1.getDate("synna");
   	   if(synna == null){
   		   synna = new java.sql.Date(0);
   	   }
   	  String email = parent.make(rs1.getString("email"));
   	  String phone = parent.make(rs1.getString("phone"));
 //	  String cell = parent.make(rs1.getString("cellularphone"));
   	  if(klient.length() > 0) {
            sb2.append(klient+";"+ nimi + ";" + synna.getTime() + "; ;" + phone  + ";" + email +  "\n");
   	      }
        }
       rs1.close();  

     if(sb2.length() > 2){
        		// Converter.saveFileInUTF8( "custcontacts.txt", sb2);
        	}

              
				 
				  fff = " select a.promotionBackOfficeId as custgr ,a.productBackOfficeId as prodid, a.minqty, a.minPrice , max (a.[date to]) as paev , max(b.description) as nimi from EPASAULE_PROMOTIONOBJECTS as a INNER JOIN \r\n" + 
				  		"EPASAULE_PROMO b ON a.promotionBackOfficeId = b.promotionBackOfficeId where minqty > 0  group by a.promotionBackOfficeId,a.productBackOfficeId, minqty, minPrice\r\n" + 
				  		"				  				order by a.promotionBackOfficeId,productBackOfficeId, minqty, minPrice";
				 

				  StringBuffer   buf = new StringBuffer();
				   
					   rs1 = stmt.executeQuery(fff);
				      while(rs1.next()){
				   	  
				   	  String gruppx = parent.make(rs1.getString("custgr"));
				   	  String toodex = parent.make(rs1.getString("prodid"));
				   	  Double kogus = rs1.getDouble("minqty");
				   	  Double hind = rs1.getDouble("minPrice");
				      java.sql.Date end = rs1.getDate("paev");
				   	 String nimi = parent.make(rs1.getString("nimi"));
				   	 if(nimi.contains("2030")) {
				   		 nimi = nimi.substring(nimi.indexOf("2030")+ 11);
				   		 if(nimi.contains("2030")) {
					   		 nimi = nimi.substring(0,nimi.indexOf("2030") - 18);
					   	 }
				   	 }
                   
				   	  
				   	long ttt = 0;
                    if(end.after(new Date())) {
                   	 ttt = end.getTime();
                    }
				   	  



                	      buf.append(toodex+ ";" + gruppx + ";" + nimi + ";" + NF.format(hind).replace(',', '.') + ";" 
                	      + gruppx + ";" + (int)(kogus+0.001) + "; ;" + ttt + "; ;");
                        buf.append("\n");	
				   	  
				      }
				     rs1.close();  
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
				        
				    StringBuffer reprBuff = new StringBuffer();    
				        reprBuff.append("R101;20\nR212;30");
				        
				        
				        if(reprBuff.length() > 2){
				            File f = new File("reprgroups.txt");
				            if (f.exists())f.delete();
				              try {
				        		PrintWriter os = new PrintWriter(new FileWriter(
				        				 "reprgroups.txt", true));
				        		os.write(reprBuff.toString());
				        		os.close();
				        	  }catch (IOException ex) {
				        		ex.printStackTrace();
				        	  }
				         }else{
				            File f = new File("reprgroups.txt");
				            if (f.exists())f.delete();
				              try {
				        		PrintWriter os = new PrintWriter(new FileWriter(
				        				 "reprgroups.txt", true));
				        		os.write("");
				        		os.close();
				        	  }catch (IOException ex) {
				        		ex.printStackTrace();
				        	  }
				         }
				     
              
    } catch (Exception e){
      getParent().showError(e);
      System.exit(0);
    }
    return customers;
  }



  private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }


      public String getDistribution() {
        return null;
      }
private HashSet<String> inTT = null;

  public TreeMap getProdGroups() throws SQLException {

	   Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = " SELECT backOfficeId as liik,Name as nimetus FROM dbo.EPASAULE_BRANDS  ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	      String nimi = rs.getString("nimetus").trim();
	   
	        tm.put(liik, Converter.enCodeEst(nimi));
	    }
	    rs.close();
	    st.close();
	    con.close();
  return tm;	   
}


  public TreeMap getProdGroupsSecond() throws SQLException {
	  
	  Connection con = getNewConnection();
	    TreeMap   tm = new TreeMap();
	    String    sql = "  SELECT backOfficeId as liik, Name as nimetus FROM dbo.EPASAULE_GROUPS  ";
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(sql);

	    while (rs.next()) {
	      String liik = rs.getString("liik").trim();
	        tm.put(liik, Converter.enCodeEst(rs.getString("nimetus").trim()));
	    }
	    
	    rs.close();
	    st.close();
	    con.close();

	    return tm;

  }

  private Vector prodcodes = null;

  private HashMap<String,Double> prodprices = null;
  
  public TreeMap getProducts(Agent agent){
	  prodcodes = new Vector();
    String cmd = "  Select posWeight as sorting , a.backOfficeId as itemid,a.Name AS nimetus,c.Name as namebr, a.quantityBox AS kastis,a.quantityPallete as alusel , b.CONVERTLAYER as layer , a.quantityPack as pakk,a.increasestep as opt, a.minimalOrderQuantity as mini,  \r\n" + 
    		"    		a.Price AS price, a.barCode AS ean,a.groupBackOfficeId AS grupp,  \r\n" + 
    		"    		a.brandBackofficeId AS grupp2,a.unitName AS unit, a.vatPercent as vat , '' as sorting from  dbo.EPASAULE_PRODUCTS a LEFT join dbo.BID01_ITEM b on b.itemid= a.backOfficeId\r\n" + 
    		"			 left join EPASAULE_BRANDS c on c.backOfficeId = a.brandBackofficeId "  ;

             String cmd2 = " Select productBackofficeId as toode,sum(Inventory) as stock, regionBackofficeId as ladu, bestBefore as partii from  dbo.EPASAULE_INVENTORIES_detail  \r\n" + 
             		"             		group by productBackofficeId,regionBackofficeId,bestBefore order by productBackofficeId, case when regionBackofficeId = '303_SHOP' then '03A' else regionBackofficeId end desc,bestBefore";
        

             usedProds = new HashSet();
    if (isDebug()) System.out.println(cmd);
       if (isDebug()) System.out.println(cmd2);
    TreeMap products = new TreeMap();
    TreeMap<String, Double> stocks = new TreeMap<String, Double>();
    TreeMap<String, String> partii = new TreeMap<String, String>();
    prodprices = new HashMap<String,Double>();
    
    try {
        Connection con =   this.getNewConnection();
        Statement stmt = con.createStatement();
    	ResultSet rs1 = stmt.executeQuery(cmd2);
        
    	while(rs1.next()){
          String id = rs1.getString("toode").trim();   
          double sto = rs1.getDouble("stock");
          String ladu = rs1.getString("ladu").trim();
          String part = rs1.getString("partii");
          String gg = "";
          if(part != null && !part.startsWith("1900")) {
              gg = " - BB:" + part.substring(0,10);
          }
          gg = (int)sto + " - " +ladu  + gg;
          
    	  if(partii.get(id) != null) {
        	 gg = partii.get(id)+ "|" + gg;
           }  
    	  partii.put(id, gg);
          
          if(stocks.get(id) != null) {
        	  sto = stocks.get(id) + sto; //summeerime laoseisu 
          }
         // stocks.put(id, sto);
          }
       
    	rs1.close();
    	
    	
      ResultSet rs = stmt.executeQuery(cmd);
      HashMap prodHash = new HashMap();
      
      while(rs.next()){
        String sorting = rs.getString("sorting");
        while(sorting.length() < 7) {
        	sorting = "0" + sorting;
        }
        String kood = rs.getString("itemid").trim();
        Product prod = new Product(kood);
        String nimetus = Converter.enCodeEst((rs.getString("nimetus").trim())) ;//+ " " + kood;
        String nimibr = Converter.enCodeEst((rs.getString("namebr").trim()));
       // teeme nime puhastuse kui on topelt brand mini
        boolean cut = false;
       if(nimetus.endsWith(nimibr)) {
    	   String uu = nimetus.substring(0,nimetus.lastIndexOf(nimibr)-2);
           String nn = nimibr.toLowerCase();
    	   if(uu.toLowerCase().contains(nn)) {
    		   cut = true;
    	   }
       }

       if(cut) {
    	   nimetus = nimetus.substring(0,nimetus.lastIndexOf(nimibr)-2);
       }
       
       nimetus = nimetus.replaceAll("- BARILLA MEAL", " ").trim();
       nimetus = nimetus.replaceAll("- BIC (MRC)", "- (MRC)").trim();
       nimetus = nimetus.replaceAll("- HBC FA", "- HBC").trim();
       nimetus = nimetus.replaceAll("- PURINA GROCERY", " ").trim();
       nimetus = nimetus.replaceAll("- PURINA (MRC)", "- (MRC)").trim();
       nimetus = nimetus.replaceAll("- NESTLE (MRC)", "- (MRC)").trim();

       nimetus = nimetus.replaceAll("- HBC GLISS KUR", " ").trim();

       nimetus = nimetus.replaceAll("- PHILIPS (MRC)", "- (MRC)").trim();

       nimetus = nimetus.replaceAll("- KIMBERLY CLARK", " ").trim();
       nimetus = nimetus.replaceAll("- COFFEE SUPPLEMENTS", " ").trim();
       nimetus = nimetus.replaceAll("- NESTLE CONFECTIONERY", " ").trim();
       nimetus = nimetus.replaceAll("- EH AIR WICK", " ").trim();
       nimetus = nimetus.replaceAll("- HHC BREF", " ").trim();
       nimetus = nimetus.replaceAll("- RCore DUREX", " ").trim();

       nimetus = nimetus.replaceAll("- HHC PERWOLL", " ").trim();

       nimetus = nimetus.replaceAll("- HBC SCHAUMA", " ").trim();
       nimetus = nimetus.replaceAll("- SCHAR", " ").trim();
       nimetus = nimetus.replaceAll("- HBC SCHWARZKOPF", " ").trim();       
       

       nimetus = nimetus.replaceAll("- NESTLE COFFEE CAPSULES", " ").trim();

       nimetus = nimetus.replaceAll("- HALEON", " ").trim(); 
       nimetus = nimetus.replaceAll("- HHC SILAN", " ").trim(); 
       nimetus = nimetus.replaceAll("- HHC SOMAT", " ").trim(); 
       nimetus = nimetus.replaceAll("- HBC PALETTE", " ").trim();

       nimetus = nimetus.replaceAll("- HBC PERSIL", " ").trim();
       nimetus = nimetus.replaceAll("- HBC PUR", " ").trim();
       nimetus = nimetus.replaceAll("- HBC SYOSS", " ").trim();
       nimetus = nimetus.replaceAll("- HBC PALETTE", " ").trim();
       nimetus = nimetus.replaceAll("- NESTLE FOOD", " ").trim();
       nimetus = nimetus.replaceAll("- SERVICES FOR SALES", " ").trim();
       
       
       
       
       
        prod.setInCases(rs.getDouble("kastis"));
        Double pall = rs.getDouble("alusel");
        if(pall > 0) {
        prod.setOnPallet(pall);
        }
        Double layer = rs.getDouble("layer");
        if(layer > 0) {
        	prod.layer = layer;
        }
        Double pack = rs.getDouble("pakk");
        if(layer > 0) {
        	prod.pack = pack;
        }
        
        prod.setOptAmountInUnits(rs.getDouble("opt"));
        prod.setMinAmountInUnits(rs.getDouble("mini"));
        prod.setPrice(rs.getDouble("price"));
        prodprices.put(prod.getID(), prod.getPrice());

        String ean = rs.getString("ean");
        if(ean!=null && ean.trim().length()>0){
          prod.setEAN(ean);
        }

      String grupp = rs.getString("grupp");
       if(grupp!=null && grupp.trim().length()>0){
         prod.setGroupSecond(grupp.trim());
       }

      prod.setName(nimetus.trim() + " " + kood);

     String grupp2 = rs.getString("grupp2");
          if(grupp2 != null && grupp2.trim().length()>0){ 
              prod.setGroup(grupp2);
           }

          String unit = rs.getString("unit");
               if(unit!=null && unit.trim().length()>0){
                   prod.setUnit(unit);
               }

               
               if(stocks.get(prod.getID()) != null){
            	  // prod.setStock(stocks.get(prod.getID()));
               }
               
               if(partii.get(prod.getID()) != null) {
            	  prod.setPartii(partii.get(prod.getID())); 
               }
               

               Double vat = rs.getDouble("vat");
               prod.setVat(vat + "");
               prod.setNameSorting(sorting + nimetus);
               
       products.put(nimetus, prod);
       prodHash.put(prod.getID(), prod);
       prodcodes.add(prod.getID());
       usedProds.add(prod.getID());

    
      }
      rs.close();
      
  	StringBuffer sb = new StringBuffer();
    rs = stmt.executeQuery("select BACKOFFICEID as id, NAME as nimi from dbo.EPASAULE_RETURN_REASONS");
   while(rs.next()){
   	String id = rs.getString("id");
   	String name = rs.getString("nimi");
   	if(name != null && name.trim().length() > 0){
   	      sb.append( id+ ";" +Converter.enCodeEst(name.trim()) + "\n");	
   	}
   }
   rs.close();

   
	StringBuffer sb2 = new StringBuffer();
    rs = stmt.executeQuery("select inventlocationid  as id , name from EPASAULE_REGIONS");
   while(rs.next()){
   	String id = rs.getString("id");
   	String name = rs.getString("name");
   	if(name != null && name.trim().length() > 0){
   	      sb2.append( id+ ";" +Converter.enCodeEst(name.trim()) + " " + id + "\n");	
   	}
   }
   
   
  rs.close();
  stmt.close();
  con.close();
 // System.out.println("REASON " + sb.toString());
  if(sb.length() > 0){
	   Converter.saveFile("returnreason.txt", sb.toString());
  }
  
  if(sb2.length() > 0){
	 //  Converter.saveFile("warehouse.txt", sb2.toString());
   }
      
      
    } catch (Exception e){
      getParent().showError(e);
    }

    
    //Siia lisada Ladude select 
    
    
    
    return products;
  }




  public Vector getCustomerProductsList(String agentCode){
	  makeCustprod();
    return custProds;
  }




  public double getSalary(String agentCode) {
      setSales(0);
      return 0;
    }

  
  private TreeMap brandplans = null; 

  
  
  public void setBulkSales(Map agents){
	 
  
    /*
    String cmd =" SELECT  cus.salesrep as agent,round(SUM(lineamount),0) AS summa  " +
     "  FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON  " +
     "  sal.invoiceid=jou.invoiceid and jou.dataareaid= sal.dataareaid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount and jou.dataareaid= cus.dataareaid  WHERE " + 
	  "	 inv.dataareaid='pre' AND sal.dataareaid='pre' AND cus.dataareaid='pre'  AND jou.dataareaid='pre' AND YEAR(sal.invoicedate) = YEAR(getdate())  " +
    "    AND MONTH(sal.invoicedate) = MONTH(getdate())  and AssortmentGroup != 6 " +
    "  GROUP BY cus.salesrep ";

       if (isDebug()) System.out.println(cmd);
          try {
        	  Connection con =   this.getNewConnection();
              Statement stmt =  con.createStatement();
              ResultSet	      rs = stmt.executeQuery(cmd);
              while(rs.next()) {
                String agentID = rs.getString("agent").trim();
                Agent agent = (Agent)agents.get(agentID);
                if (agent != null){
                  agent.setMonthSales((int)rs.getDouble("summa"));
                 // System.out.println(agent.getMonthSales());
                }
              }
              rs.close();
              stmt.close();
              con.close();
          } catch (Exception e) {
              getParent().showError(e);
           }*/
	  
        }
  
  
  
  public void setBulkSalesOLD(Map agents){
   this.connectionUp();
 
    brandplans = this.makePlans("Budget.xlsx");
 

 String cmd =" SELECT  cus.statisticsgroup as agent ,round(sum(inv.weightkg*sal.salesqty),2) AS summakg , \r\n" + 
 		" 					 round(sum(lineamount * (case when b.percentmonth is not null then (100 -b.percentmonth)/100 else 1 end)),0) AS summa ,\r\n" + 
 		" 					 round(sum(lineamount ),0) AS summa2 \r\n" + 
 		" 					  FROM salesline sal INNER JOIN inventtable inv with (nolock) ON sal.itemid=inv.itemid and inv.dataareaid= sal.dataareaid INNER JOIN salestable jou with (nolock) ON  \r\n" + 
 		" 		 		  sal.salesid=jou.salesid and jou.dataareaid= sal.dataareaid INNER JOIN custtable cus ON cus.accountnum = jou.custaccount and jou.dataareaid= cus.dataareaid \r\n" + 
 		" 				   left join PJTURNOVERBONUS b on b.accountrelation = cus.custgroup and b.itemrelation = inv.itemgroupid and \r\n" + 
 		" 				    periodid = substring(CONVERT(varchar, getdate(), 23),0,8)  and b.dataareaid = cus.dataareaid\r\n" + 
 		" 				  WHERE inv.dataareaid='eee'  AND YEAR(jou.deliverydate) = YEAR(getdate())  \r\n" + 
 		" 		 		   AND MONTH(jou.deliverydate) = MONTH(getdate()) AND DAY(jou.deliverydate) <= DAY(getdate())   and cus.statisticsgroup != ''  and inv.asttelemaitemgroupid not in ('290','400') \r\n" + 
 		" 				    GROUP BY cus.statisticsgroup";


        if (isDebug()) System.out.println(cmd);
           try {
           	
           	Connection con = this.getNewConnection();
               Statement stmt =  con.createStatement();
               ResultSet	      rs = stmt.executeQuery(cmd);
               while(rs.next()) {
                 String agentID = rs.getString("agent").trim();
                 if(agentID.equals("Lääne-Eest")) {
                	 agentID = "Laane-Eest";
                 }
                 if(agentID.equals("Lõuna-Eest")) {
                	 agentID = "Louna-Eest";
                 }
                 Agent agent = (Agent)agents.get(agentID);
                 
                 int kgs = (int)rs.getDouble("summakg");
                 int sum = (int)rs.getDouble("summa");
                
                 if (agent != null && brandplans.get(agentID) != null){
                   Toode tt = (Toode)brandplans.get(agentID);
                   tt.setDonecur(tt.getDonecur() + sum);
                   tt.setDonevol(tt.getDonevol() + kgs);
                   }
               }
               
               rs.close();
               stmt.close();
               con.close();
            
                           
                             int dd = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                             int d2 = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                          
                       	 // Iterreerime Plaani mappi ja sealme asjad paika 
                       	  
                       	Iterator itera =   agents.keySet().iterator();
                       	while(itera.hasNext()){
                       		String agentID = itera.next() + "";
                       		Agent aa = (Agent)agents.get(agentID);
                       		
                          String asi2 = "";
                          
                          if(brandplans.get(aa.getID()) != null){
      	                	Toode tt =   (Toode)brandplans.get(aa.getID());
      	                	
      	                			int teh = (int)tt.getDonecur();
      	                			int kgs = (int)tt.getDonevol();
      	                	  		asi2 = asi2 + (((int)(teh/tt.getPlancur()*100))+ "% Plan.: " +(int) tt.getPlancur() + " €  Sales.: " + teh + " €||");
      	                	  	   asi2 = asi2 + (((int)(kgs/tt.getPlanvol()*100))+ "% Plan.: " +(int) tt.getPlanvol() + " kg.  Sales.: " + kgs + " kg.||");
      	                		   }
      	                		  
      	                	    
                          
                          aa.setStart( Converter.enCodeEst(asi2));
                        
                       	}
           } catch (Exception e) {
               getParent().showError(e);
            }
         }


   double totalPlanSum = 0;
   
   
   
   private TreeMap makePlans(String fname){
 	   TreeMap res = new TreeMap();
 	      totalPlanSum = 0;
 	   try{
 		
 	   Calendar cal = Calendar.getInstance();
 	   String month = (cal.get(Calendar.MONTH)+1)+ "";
 	   if (month.length() == 1){
 		   month = "0" + month;
 	   }
 	   String jooksev = cal.get(Calendar.YEAR) + "-" + month; 
 	   int a = 0;
 	   File f = new File (getExcelDir() + fname);
 		  FileInputStream	 ff = new FileInputStream(f);
 	
 		XSSFWorkbook workbook = new XSSFWorkbook((ff));
 		XSSFSheet sheet = workbook.getSheet(jooksev);
 		System.out.println("jooksev " + jooksev + " " + sheet);
 		XSSFRow row = null; 
 				ff.close();	
 				if(sheet != null){
 				
 					
 					for(int i = 0 ; i < sheet.getLastRowNum();i++){
 						 row = sheet.getRow(i);
 						if(row == null && a == 0){
 							a = i;
 						}else{
 							a = sheet.getLastRowNum();
 						}
 					}
 					String gr1 = null;
 					String gr1n = null;
 					
 					for(int i = 8 ; i < a + 1;i++){ // 9ndast reas hakkame scrollima nii kilosid kui ka summat
 						 row = sheet.getRow(i);
 						String agent = null;
 						double g1 = 0;
 						
 						
 						try {
 							agent = (row.getCell((short) 1).getStringCellValue());
 						
    					      } catch (Exception e) {
    					    	  System.out.println("agent ei  " + agent);
    						 //  e.printStackTrace();
    					      }
 			    
 						try {
 						g1 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
 						
 						} catch (Exception e) {
 	   					 e.printStackTrace();	
  					    }
 						
 					
 						if(agent != null) {
 							i++;
 							 row = sheet.getRow(i);
 							 double g2 = 0;
 							
 							 try {
 		 						g2 = Double.parseDouble(row.getCell((short) 5).getNumericCellValue()+ "");
 		 						} catch (Exception e) {
 		 	   					 e.printStackTrace();	
 		  					    }
 							 
 							 
 						TreeMap asi= new TreeMap();
 				        Toode t1 = new Toode(gr1);
						       t1.setPlanvol(g2);
						       t1.setPlancur(g1);
						       System.out.println("agent " + agent + " " + g1 + " " + g2);
					    res.put(agent, t1);
 					  }
 					}
 						
 					}
 				
 				
         		   
 	   }catch (Exception e){
 		   e.printStackTrace();
 	   }
 	   
 	//   System.out.println("totalPlanSum "+ totalPlanSum );
 	   return res;
    }
    
   
   
   

    public double getFoodSales(String agentCode) throws SQLException{
      return 0;
    }

    public double getAlcoSales(String agentCode) throws SQLException{
      return 0;
    }

    public ManagerData getManagerData() throws SQLException{
      
      return null;
    }


    public double getPromoSales(String agentCode, int nr) throws SQLException{
      return 0;
    }


  public String getStartingTime(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, - Integer.parseInt(getParent().getProps().getProperty("months")));
    int month = cal.get(Calendar.MONTH) + 1;
    String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    String sqlDate = cal.get(Calendar.YEAR) + "-" + month + "-" + day;
    return sqlDate;
  }

  public String getDriverVersion(){
    return "EugestaEST";
  }

  public String getSubjectFilesTo(){
    return this.subjectFilesTo;
  }

  	 public String getPilotDayData(Agent agent){

  	    String sql = "SELECT talinventstatistics1num as grupp, round(SUM(talnetweight),0) AS kogus,round(SUM(lineamount),0) AS summa " +
       " FROM custinvoicetrans sal INNER JOIN inventtable inv ON sal.itemid=inv.itemid INNER JOIN custinvoicejour jou ON " +
       " sal.invoiceid=jou.invoiceid  INNER JOIN custtable cus ON cus.accountnum = jou.orderaccount  WHERE len(talinventstatistics1num) >0 AND " +
       " inv.dataareaid='tal' AND sal.dataareaid='tal' AND cus.dataareaid='tal' AND cus.talsalesman='" + agent.getID() + "' AND YEAR(sal.invoicedate) = YEAR(getdate()) AND MONTH(sal.invoicedate) = MONTH(getdate())" +
       " GROUP BY talinventstatistics1num  ORDER BY talinventstatistics1num ";
  	  StringBuffer sb = new StringBuffer();

  	  System.out.println(sql);
  	 try{
       Connection con =  this.getNewConnection();
    	Statement stmt = con.createStatement();
  	    ResultSet	      rs = stmt.executeQuery(sql);

  	          sb.append("GrupiNimi     kilodes ja EUR\n");
  	          sb.append("---------------------------------------------------\n");

  	          double totalKokku = 0d;
                    double totalSumma = 0d;

  	          while (rs.next()) {
  	            sb.append(rs.getString("grupp").trim() + "\n");
  	          	int kogus = rs.getInt("kogus");
                            int summa = rs.getInt("summa");
  	            totalSumma = round(totalSumma + summa, 2);
  	            totalKokku = round(totalKokku + kogus, 2);
  	            sb.append("\t" + kogus + "\t" + summa + " EUR\n\n");
  	           }
  	          rs.close();
  	          stmt.close();
  	          con.close();

  	          sb.append("---------------------------------------------------\n");
  	          sb.append("\tKuu m��k kokku: " + totalKokku + " Kg." + totalSumma + " EUR");


  	                  } catch (Exception e) {
  	                      getParent().showError(e);
  	                  }


  	      return sb.toString();
  	  }






       public NumberFormat getNF(){
         return NF;
       }


    	@Override
       	public void WebReporter() {
       		
        	   ArrayList<String> filenames = new ArrayList<String>();
         // teeme kataloogi salvestamiseks
         		File f = new File("svdata");
         		if(!f.exists()) {
         			f.mkdir();
         		}
         		
         		
         		
         		Calendar cal = Calendar.getInstance();
         		String hh = cal.getTime().getHours()+"";
         		if(hh.length() == 1) {
         			hh = "0" + hh;
         		}
         		String mm = cal.getTime().getMinutes()+"";
         		if(mm.length() == 1) {
         			mm = "0" + mm;
         		}
         		String time2 = "_" + hh +"-" +mm;
         	// loeme signatuurid sisse kui on olemas	
         		HashSet done = null;
         		try {
         			done = (HashSet) Converter.readObject("signadone.obj");
         		}catch(Exception e) {
         			e.printStackTrace();
         		}
         		
         		if(done == null) {
         			 done = new HashSet();
         		}
         		
         		
         		
         	
         		Calendar algus = Calendar.getInstance();  
         	  	Calendar lopp = Calendar.getInstance();  	
         	  	Integer days = 8;
         	  if(	parent.getProps().getProperty("webreport.days") != null) {
         		  try {
         			  days = Integer.parseInt(parent.getProps().getProperty("webreport.days"));
         		  }catch(Exception e) {
         			  
         		  }
         	
         	  }

         	  System.out.println("days " + days);
         	  	algus.add(Calendar.DATE, -1 * days);
         	  	algus.set(Calendar.HOUR, 0);
         	  	lopp.set(Calendar.HOUR, 23);
         	  	lopp.set(Calendar.MINUTE, 59);
         	  	lopp.add(Calendar.DATE, +2);
         	 // Teeme SV listi 
         	  
         	  	try {
         	  		String fnameBASE = this.getExcelDir() + "ReportBase.xlsx";
         	  		FileInputStream excelifailBase = new FileInputStream(fnameBASE);
         	  		XSSFWorkbook  wbBASE = new XSSFWorkbook(excelifailBase);
         			
         			XSSFSheet worksheetBASE = wbBASE.getSheetAt(0);
         			
         			
         			
         			CreationHelper createHelper = wbBASE.getCreationHelper();
         			
         			 XSSFCellStyle hlinkstyle = wbBASE.createCellStyle();
         		      XSSFFont hlinkfont = wbBASE.createFont();
         		      hlinkfont.setUnderline(XSSFFont.U_SINGLE);
         		      hlinkfont.setColor(IndexedColors.BLUE.getIndex());
         		      hlinkstyle.setFont(hlinkfont);

         			
         	  		
         	  		TreeMap files = Converter.makeWebFilesTree(algus.getTime(), lopp.getTime(), rcompany, user, pass, true);
         	  	//	System.out.println("files " + files.toString());
       		
          		org.json.JSONArray cc = Converter.getSVDataArray(algus.getTime(), lopp.getTime(), rcompany, user, pass, false, "PRODUCT", null, "test");
          		
          		Converter.saveFile(f.getPath() + "/currSVjson.json", cc.toString());
          		int outofstocks = 1;
        		for (int i = 0; i < cc.length(); i++) {
          			org.json.JSONObject cob = (org.json.JSONObject) cc.get(i);
          			
          			String filterOutOfStock = (String)cob.get("resint3").toString();
          			String dateAndTime = (String)cob.get("recetime");
          		
          			 cal = Calendar.getInstance();
       	  			try {
       	  				cal.setTimeInMillis(Long.parseLong(filterOutOfStock));
       	  			}catch(Exception e) {
       	  				cal.setTimeInMillis((Long)cob.get("sentdate"));
       	  			}
          			
       	  			String dateOnly = Converter.getYYYYmMMmDDmString(cal.getTime());
       	  					//dateAndTime.split("T")[0];
       	  			String time = "";
       	  			String hour = cal.get(Calendar.HOUR_OF_DAY)+"";
       	  			if(hour.length() == 1) {
       	  				hour = "0"+hour;
       	  			}

       	  			String minute = cal.get(Calendar.MINUTE)+"";
       	  			if(minute.length() == 1) {
       	  				minute = "0"+minute;
       	  			}
       	  			String timeOnly = hour+":"+minute;
          			
          	    	String cagid = "";
          	    	try{
          	    		cagid =	(String)cob.get("custagent");
          	    	}catch(Exception e) {
          	    		
          	    	}
          			String cagname = "";
          			try{
          				cagname =	(String)cob.get("custagname");
          	    	}catch(Exception e) {
          	    		
          	    	}
          			String agid = (String)cob.get("agentid");
          			
          			String agname = "NoName Manager" ; 
          			try {
          			  agname = (String)cob.get("managerName");
          			 } catch(Exception r) {
          				
          			 }
          			String chain = (String)cob.get("custchain");
          			
          		
          			//System.out.println("signa " + signa + " " + filterOutOfStock + " " + filterProblem);
          			
          			String res2 = "";
       				try {
       					if((String)cob.get("resstr2") != null) {
       	  					res2 = (String)cob.get("resstr2");
       	  				}
       				}catch(Exception e) {
       					
       				}
       				try {
       				JSONParser parser = new JSONParser();
       				JSONObject obj = (JSONObject) parser.parse(res2);
       			    String segment = (String)obj.get("segment");
       			    String brand = (String)obj.get("brand");
       			    String type = (String)obj.get("type");
       			    Long start = (Long)obj.get("start");
       			    String startT = (String)obj.get("startT");
       			    Double  wei =  0d;
       			    try {
       			       wei = Double.parseDouble(obj.get("weight")+"");
       			     } catch(Exception e) {
       			    	
       			     }
       			    Long end = (Long)obj.get("end");
       			    String endT = (String)obj.get("endT");

       				System.out.println(obj.toJSONString());
       				
       				String pricet = "1";
       				String perct = "0";
       				Boolean isPerc = (Boolean)cob.get("isgroup");
       				if(isPerc) {
       					pricet = "0";
       					perct  = "1";
       				}
       				

       	   			String signa = dateAndTime + "" +(String)cob.get("custid") + "" + (String)cob.get("prodid") + brand ;
          		
          			
          			if( done != null && !done.contains(signa)){
          				
          				   				
          				Double addi = 0d;
          				
          				try {
          				  addi = Double.parseDouble(cob.get("grouptotal")+"");
          				 } catch(Exception e) {}
          				
          				worksheetBASE.createRow(outofstocks);
          				worksheetBASE.getRow(outofstocks).createCell(0).setCellValue(agid);
          				// agname = new String(agname.getBytes(), "UTF-8");
          				
          				if(agid.equals("Klaip")) {
          					agname = "Asta Razmienė";
          				}
          				
          				if(agid.equals("Panev")) {
          					agname = "Valentinas Jankevičius";
          				}
          				
          				
          				worksheetBASE.getRow(outofstocks).createCell(1).setCellValue(agname);
          				String cuname = (String)cob.get("custname");
          				String cuid = (String)cob.get("custid");
          				if(cuname.endsWith(cuid)) {
          					cuname = cuname.substring(0,cuname.indexOf(cuid)-1);
          				}
          				
          				worksheetBASE.getRow(outofstocks).createCell(3).setCellValue(cuname);
          				worksheetBASE.getRow(outofstocks).createCell(2).setCellValue(cuid);
       				String prodid = (String)cob.get("prodid");
          				
       				if(brand.startsWith("R") && brand.endsWith("ta")) {
       					brand = "Rīta";
       				}
       				if(brand.startsWith("Sp") && brand.endsWith("katota")) {
       					brand = "Spēkatota";
       				}
       				if(brand.startsWith("Gard")) {
       					brand = "Gardā";
       				}
       				if(brand.startsWith("M") && brand.endsWith("aize")) {
       					brand = "Mākoņmaize";
       				}
       				if(brand.startsWith("Tost") && !brand.contains("maiz") ) {
       					brand = "Tostē";
       				}
       				if(brand.startsWith("Kur")) {
       					brand = "Kurši";
       				}
       				if(brand.startsWith("Maizi")) {
       					brand = "Maiziņš";
       				}
       				
       				if(brand.startsWith("Dz")) {
       					brand = "Dzīvā";
       				}
       				
       				if(brand.startsWith("Isten")) {
       					brand = "Istenā";
       				}
          				worksheetBASE.getRow(outofstocks).createCell(4).setCellValue(brand);
          				
          				
          				worksheetBASE.getRow(outofstocks).createCell(5).setCellValue(startT);
          				worksheetBASE.getRow(outofstocks).createCell(6).setCellValue(endT);
          				

          				worksheetBASE.getRow(outofstocks).createCell(7).setCellValue(pricet);
          				worksheetBASE.getRow(outofstocks).createCell(8).setCellValue(perct);
       			   				
       			try {
       			    Double price = Double.parseDouble(cob.get("normalpr")+"");
       			     if(price > 0) {
       			   				worksheetBASE.getRow(outofstocks).createCell(9).setCellValue(price);
       			     }
       			     }catch(Exception e) {}
       			
       			try {
       			     Double priceA = Double.parseDouble(cob.get("amoun")+"");
       			     if(priceA > 0) {
       			   				worksheetBASE.getRow(outofstocks).createCell(10).setCellValue(priceA);
       			     }
       			     }catch(Exception e) {}   
       			
       			try {          
       			       if(wei > 0) {
       					  worksheetBASE.getRow(outofstocks).createCell(11).setCellValue(wei);
       					}
       			    } catch (Exception e) {}
       			

          				if(segment != null) {
          				  worksheetBASE.getRow(outofstocks).createCell(12).setCellValue(segment);
          				}

          				if(type != null) {
          				  worksheetBASE.getRow(outofstocks).createCell(13).setCellValue(type);
          				}
          				   				

          				if(addi > 0) {
          				  worksheetBASE.getRow(outofstocks).createCell(14).setCellValue(addi);
          				}
          				

          				try {
          	  				worksheetBASE.getRow(outofstocks).createCell(15).setCellValue((String)cob.get("comment"));
          	  				}catch(Exception e) {
          	  					
          	  				}
          				worksheetBASE.getRow(outofstocks).createCell(17).setCellValue(dateOnly);
          				worksheetBASE.getRow(outofstocks).createCell(18).setCellValue(timeOnly);
          
          				
          				String foto = null;
         				if(files != null && files.get(cuid) != null) {
         					ArrayList arr = (ArrayList)files.get(cuid);
         					for(int e = 0; e<arr.size(); e++) {
         						Toode t = (Toode)arr.get(e);
         						if(!t.getID().equals("prodid")) {
         						//System.out.println(t.getID()+ " " +  (String)cob.get("prodid") +" " + agid+ " " + t.comment +" " + t.getGr1() + " "  + dateOnly );
         						  if(t.comment.equals(agid) &&  t.getGr1().equals(dateOnly) && t.getID().equals((String)cob.get("prodid"))){
         							foto = t.getLadu();
         						   }
         					}}
         				}
          				
          				 //URL Link
          				if(foto != null) {
          		         Cell  cell = worksheetBASE.getRow(outofstocks).createCell(16);
          		         cell.setCellValue("Photo Link");
          		         XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(Hyperlink.LINK_URL);
          		         link.setAddress(URLEncoder.encode(foto, StandardCharsets.UTF_8.toString()));
          		         cell.setHyperlink((XSSFHyperlink) link);
          		         cell.setCellStyle(hlinkstyle);
          				}
          				//worksheetBASE.getRow(outofstocks).createCell(15).setCellValue(cagname);
          				
          				
          				outofstocks++;
          				done.add(signa);
          			}
                     }catch(Exception e) {
                   	  
                     }
          			
          	   			}
        		
       			if(outofstocks > 1) {
       				String exceldir = this.getExcelDir()  +"reports/est/";
    	   	   		File ff = new File( exceldir );
       	   	   		if(!ff.exists()) {
       	   	   			ff.mkdir();
       	   	   		}
       	   	   		excelifailBase.close();
       	   	   		

       	   	   		FileOutputStream output_file_oos =new FileOutputStream(exceldir +
       	   	   				"productCamp-" + Converter.getYYYY_MM_DD_String(new java.util.Date()) + time2 + ".xlsx");
       	   	   		filenames.add(exceldir + "productCamp-" + Converter.getYYYY_MM_DD_String(new java.util.Date())+ time2 + ".xlsx");
       	   	   		wbBASE.write(output_file_oos); //write changes
       	   	   		output_file_oos.close();  //close the stream
       	   			}else {
       	   				System.out.println("pole mida teha");
       	   			}
          		
          		try {
                Converter.saveObject(done,"signadone.obj");
         		}catch(Exception e) {
         			e.printStackTrace();
         		}
         	
         	  	}catch(Exception e) {
         	  		e.printStackTrace();
         	  	}
        	
       		System.exit(0);
       	}

 


       /**
        *
        * @param aNF - the new value for NF
        */
       public void setNF(NumberFormat aNF){
         NF = aNF;
    }
  private String url = "jdbc:odbc:eugestaest";

  private   Vector debt = new Vector();

  private Vector custProds = new Vector();
  private String subjectFilesTo = null;
  private Vector prods = new Vector();
    private NumberFormat NF = NumberFormat.getInstance();




	@Override
	public void PictureMaker() {
		  makePictureCat();
		  System.exit(0);
		
	}


}


