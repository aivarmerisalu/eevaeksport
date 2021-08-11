package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import ee.mobilejava.wrapper.DefectiveLineWrapper;
import ee.mobilejava.wrapper.DefectiveWrapper;
import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.DealerProduct;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class KGGrupp2DBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public KGGrupp2DBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
    String databaseDriverName = "oracle.jdbc.driver.OracleDriver";
    teamx = getParent().getProps().getProperty("salesteam");
	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
	
	  try {
	      Class.forName(databaseDriverName).newInstance();
	      con = DriverManager.getConnection("jdbc:oracle:thin:@kp-srv-ora:1521:AXDB", getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
	     
	    } catch (Exception e){
	       parent.showError(e);
	    }
	
	 prepareDateInfo();
  }
  private Hashtable dates = new Hashtable();
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }
   
   
   private void  prepareDateInfo(){
		  String[] list = new File(getDir()).list();

		  for(int i = 0 ; i < list.length ; i++){
			  String name = list[i];
			//  System.out.println(list[i]);
			  
			  if(name.indexOf("Customers-KPVP-")!= -1){
				  String date = name.substring(name.indexOf("Customers-KPVP-")+15,name.indexOf("Customers-")+23);
				  String ddd = (String) dates.get("Customers");
				 // System.out.println(date + " " + ddd + " " + (date));
				  //Customers-KPVP-20130927-0616
				
				  if(ddd == null){
					  dates.put("Customers", date); 
				  }else{
					//  System.out.println(date + " " + ddd + " " + (date) + " " + ddd.compareTo(date));
					  if(ddd.compareTo(date) <= -1){
						  dates.put("Customers", date); 
					  }
				  }
			  }
			  
			  if(name.indexOf("Discounts")!= -1){
				  String date = name.substring(name.indexOf("Discounts")+15,name.indexOf("Discounts")+23);
				 // System.out.println(date);
				  
				  String ddd = (String) dates.get("Discounts");
				  if(ddd == null){
					  dates.put("Discounts", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("Discounts", date); 
					  }
				  }
			  }
			 
			  if(name.indexOf("Products-")!= -1){
				  String date = name.substring(name.indexOf("Products-")+14,name.indexOf("Products-")+22);
				 // System.out.println(date);
				  
				  String ddd = (String) dates.get("Products");
				  if(ddd == null){
					  dates.put("Products", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("Products", date); 
					  }
				  }
			  }
			  
			  
			  if(name.indexOf("ProductsPa")!= -1){
				  String date = name.substring(name.indexOf("ProductsPa")+22,name.indexOf("ProductsPa")+30);
				 // System.out.println(date);
				  
				  String ddd = (String) dates.get("ProductsPA");
				  if(ddd == null){
					  dates.put("ProductsPA", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("ProductsPA", date); 
					  }
				  }
			  }
			  if(name.indexOf("Receivable")!= -1){
				  String date = name.substring(name.indexOf("Receivable")+18,name.indexOf("Receivable")+26);
				//  System.out.println(date);
				  
				  String ddd = (String) dates.get("Receivable");
				  if(ddd == null){
					  dates.put("Receivable", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("Receivable", date); 
					  }
				  }
			  }
			  
			  
			  if(name.indexOf("SalesLines")!= -1){
				  String date = name.substring(name.indexOf("SalesLines")+16,name.indexOf("SalesLines")+24);
				 // System.out.println(date);
				  
				  String ddd = (String) dates.get("SalesLines");
				  if(ddd == null){
					  dates.put("SalesLines", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("SalesLines", date); 
					  }
				  }
			  }
			  
			  if(name.indexOf("TaraBalance")!= -1){
				  String date = name.substring(name.indexOf("TaraBalance")+17,name.indexOf("TaraBalance")+25);
				//  System.out.println(date);
				  
				  String ddd = (String) dates.get("TaraBalance");
				  if(ddd == null){
					  dates.put("TaraBalance", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("TaraBalance", date); 
					  }
				  }
			  }
			  if(name.indexOf("ManagerSales")!= -1){
				  String date = name.substring(name.indexOf("ManagerSales")+18,name.indexOf("ManagerSales")+24);
				 // System.out.println(date);
				  
				  String ddd = (String) dates.get("ManagerSales");
				  if(ddd == null){
					  dates.put("ManagerSales", date); 
				  }else{
					  if(ddd.compareTo(date) <= -1){
						  dates.put("ManagerSales", date); 
					  }
				  }
			  }		  
		  }
		//  System.out.println(dates.toString());
	  }   
   
   public Vector getPriceMatrixData(){
       Vector result = new Vector();
	         try {
	          
	        	 FileReader fr = new FileReader(getDir() + "Discount.csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(";"," |");
	            	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

	              if (st.hasMoreTokens()) {
	            	
//  Discount_ID;DiscountPercent;Is_Manual;DiscountDescribtion;ValidFrom;ValidTill;Priority;ProductCL3;Customer_ID
                     st.nextToken();
                  double percent = Double.parseDouble(st.nextToken().trim());
                     st.nextToken();
                     st.nextToken();
                  String sdate =  st.nextToken().trim() ;
   	              String edate = st.nextToken().trim();
   	                 st.nextToken();
   	             String prodGroup = st.nextToken().trim();
	             String custID = st.nextToken().trim();
	             String clGroup = null;
	             String prodID = null;
	            
	             MatrixLine mline = new MatrixLine();

	             if (custID != null && custID.length() > 0) {
	               mline.setCustID(custID);
	             }
	             if (mline.getCustID() == null && clGroup !=null && clGroup.trim().length() > 0) {
	               mline.setCliGroup(Converter.cleanLitLatEst(clGroup.trim()));
	             }

	             if(sdate != null  && !sdate.startsWith("1753") && sdate.trim().length() > 0 ){
	            	 mline.setStartDate(sdate.substring(0,10));
	             }
	             if(edate != null && !edate.startsWith("1753") && sdate.trim().length() > 0 ){
	            	 mline.setEndDate(edate.substring(0,10));
	             }
	             if (prodID != null && prodID.length() > 0) {
	               mline.setProdID(prodID);
	             }
	             if (mline.getProdID() == null &&  prodGroup != null && prodGroup.trim().length() > 0) {
	               mline.setDiscGroup(prodGroup.trim());
	             }

	             if (percent != 0) {
	                 mline.setDiscount(percent);
                 }
	             
	             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
	                 result.add(mline);    
	               }
	             }
	            }
	            
	            reader.close();
	            fr.close();
	         }catch(Exception e){
	        //	 e.printStackTrace();
	         }
		
	    return result;
	      }

   
   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

   public void insertDefective(DefectiveWrapper wr){
    String sql = "INSERT INTO dbo.MJ_QUALITYLAT VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
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
          //System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
          ps.setString(8, bb);
          ps.setInt(9, lw.getLine().getDefectCode());
          ps.setString(10, lw.getLine().getProdType());
          ps.setString(11, lw.getLine().getDefectText());

          ps.executeUpdate();
        }

        con.close();
        ps.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
  }

   public ManagerData getManagerData() throws SQLException{ 	

    String tableName = "dayreport";
    if(getParent().getProps().getProperty("report.tablename") != null && !getParent().getProps().getProperty("report.tablename").trim().equals("")){
    	tableName = getParent().getProps().getProperty("report.tablename");
    }
   	
    String sql = " SELECT clid AS klient,endlong AS kuupaev,docsum AS summa,linecount AS ridu,text AS tekst,'' AS dealer,clgroup AS agentname" +
        " FROM  " + tableName + 
        " WHERE enddate>(GETDATE()-30) ORDER BY clid ";

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
   public String getProductSortingData(){
   	return null;
   }

 
   public DealerProduct getDealerProductsList() {
       return null;
   }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
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



       public String getSaveStockInfo(){
       return null;
       }

  private Date getDate(String mmddyy){//2008-12-31 00:00:00
  	long lon = 0;
  	Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, Integer.parseInt(mmddyy.trim().substring(0, 4)));
    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.trim().substring(5, 7))-1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.trim().substring(8, 10)));
  
   Date date = new Date(lon);
   date.setTime(cal.getTimeInMillis());
//   System.out.println(mmddyy + " " + date.toLocaleString() + " xxx " + mmddyy.trim().substring(0, 4) + " " + Integer.parseInt(mmddyy.trim().substring(5, 7)));  //05.02.14
  return date;
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
   //  setTreeGroup(true);
     try {
	    	 FileReader fr = new FileReader(getDir() + "static/ProductCodelist.csv");
     
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
 
    line=reader.readLine();
    while((line = reader.readLine()) != null ){
       line = line.replaceAll(";","| ");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
        	String asi = clear(st.nextToken().trim());
        	if(asi.equals("ProductCategory")){
        		  String id = clear(st.nextToken().trim());
                  if (st.hasMoreTokens()){
                   String name = clear(st.nextToken().trim());
                   tm.put(cleardots(Converter.cleanLitLatEst(id)), Converter.enCodeEst( name));
             }
            }
          }
        }
    reader.close();
    fr.close();
	    	
      } catch (IOException e){
        e.printStackTrace();
      }
   //   System.out.println(tm.toString());
         return tm;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
	    TreeMap   tm = new TreeMap();
	    //  setTreeGroup(true);
	      try {
	    	  FileReader fr = new FileReader(getDir() + "static/ProductCodelist.csv");
	    	     
	       BufferedReader reader = new BufferedReader(fr);
	     String line = null;
	  
	     line=reader.readLine();
	     while((line = reader.readLine()) != null ){
	        line = line.replaceAll(";","| ");
	        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
	         if (st.hasMoreTokens()){
	         	String asi = clear(st.nextToken().trim());
	         	if(asi.equals("ProductCL2")){
	         		  String id = clear(st.nextToken().trim());
	                   if (st.hasMoreTokens()){
	                    String name = clear(st.nextToken().trim());
	             tm.put(cleardots(Converter.cleanLitLatEst(id)), Converter.enCodeEst( name));
	              }
	             }
	           }
	         }
	     reader.close();
	     fr.close();
		    	
	       } catch (IOException e){
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

   
   private Hashtable custagents = new Hashtable();
   private String teamx = null;
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
  
     try {
       FileReader fr = new FileReader(getDir() + "static/Users.csv");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     reader.readLine();
      while((line = reader.readLine()) != null ){

           line = line.replaceAll(";"," |");
      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

        if (st.hasMoreTokens()) {
        // String custid = st.nextToken().trim(); 
          String id = st.nextToken().trim();
          Agent agent = new Agent(id);
          if (st.hasMoreTokens()) {
            String name = st.nextToken().trim();
             
            agent.setName(Converter.enCodeEst(name + " " + id));
            if (st.hasMoreTokens()) {
              String objekt = st.nextToken().trim();
              agent.setObject(objekt);
            }
            
          /*  if(custagents.get(custid) == null){
            	Vector hs = new Vector();
            	hs.add(id);
            	custagents.put(custid, hs);
            }else{
            	Vector hs = ( Vector) custagents.get(custid);
            	hs.add(id);
            	custagents.put(custid, hs);
            }*/
        //    if(name.equals(teamx)){
                tm.put(agent.getName(), agent);	
          //  }
      
          }
        }
      }
       reader.close();
       fr.close();
      Agent ss =new Agent("HOUSE1");
      ss.setName("HOUSE1");
      tm.put("HOUSE1", new Agent("HOUSE1"));

     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }




       public String getDistribution() {
         return null;
       }

       private Hashtable<String,String>  readTara(){
    	   Hashtable<String,String> hh = new Hashtable<String,String>();
               getProducts(null);
    	     try {
    	    	  String[] list = new File(getDir()).list();
  		         for(int u = 0 ; u < list.length ; u++){
  		    		  String fname = list[u];
  		    		  
  		    	if(fname.indexOf("TaraBalance") != -1 && fname.indexOf((String)dates.get("TaraBalance")) != -1){
  		    		 
  				 FileReader fr = new FileReader(getDir() + fname);
  		    	 
    	       BufferedReader reader = new BufferedReader(fr);
    	     String line = null;
    	     reader.readLine();
    	      while((line = reader.readLine()) != null ){

    	           line = line.replaceAll(";"," |");
    	      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
//Contact_ID;Customer_ID;ContactName;ContactName2;ContactName3;Contact_Mobile;
  	//ContactEmail;ContactPhone;ContactFax;ContactNote;ContactN
    	        if (st.hasMoreTokens()) {
    	        	st.nextToken(); 
    	          String custid = st.nextToken().trim(); 
    	          String prodid = clear(st.nextToken()).trim();
    	          String name = "";
    	          try{
    	        	 name =  (String)prodNames.get(prodid);
    	          }catch(Exception e){
    	        	  e.printStackTrace();
    	          }
    	         
    	          String amount = clear(st.nextToken()).trim();
    	          String value = clear(st.nextToken()).trim();
    	          clear(st.nextToken()).trim();
    	    
    	          String team =  clear(st.nextToken()).trim();
    	          String jama = "[" + amount + "] " +  name ;
    	          
    	          if(team.equals(teamx)){
    	        	  if(hh.get(custid) == null){
      	            	hh.put(custid, jama);
      	            }else{
      	            	hh.put(custid, hh.get(custid) + "|" + jama);
      	              }  
    	          }
    	            
    	            }
    	          }
    	       
    	       reader.close();
    	       fr.close();
  		    	}}
    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   //  System.out.println(hh.toString());
    	     
    	   return hh ;
       }
       
       
       private Hashtable<String,Vector>  readCustAddr(){
    	   Hashtable<String,Vector> hh = new Hashtable<String,Vector>();

    	     try {
    	    	  String[] list = new File(getDir()).list();
 		         for(int u = 0 ; u < list.length ; u++){
 		    		  String fname = list[u];
 		    		  
 		    	if(fname.indexOf("CustomerAddress_ID") != -1 && fname.indexOf((String)dates.get("CustomerAddress_ID")) != -1){
 		    		 
 				 FileReader fr = new FileReader(getDir() + fname);
 		    	 
    	       BufferedReader reader = new BufferedReader(fr);
    	     String line = null;
    	     reader.readLine();
    	      while((line = reader.readLine()) != null ){

    	           line = line.replaceAll(";"," |");
    	      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
//Contact_ID;Customer_ID;ContactName;ContactName2;ContactName3;Contact_Mobile;
  	//ContactEmail;ContactPhone;ContactFax;ContactNote;ContactN
    	        if (st.hasMoreTokens()) {
    	        
    	          String custid = st.nextToken().trim(); 
    	          String addrid = st.nextToken().trim();
    	          String name = st.nextToken().trim(); 
    	         
    	          Toode tt  = new Toode(addrid) ;
    	          tt.setBrand(name);
    	          
    	        	  if(hh.get(custid) == null){
    	        		  Vector vv = new Vector();
    	        		  vv.add(tt);
      	            	hh.put(custid, vv);
      	            }else{
      	            	 Vector vv = (Vector)hh.get(custid);
   	        		     vv.add(tt);
     	            	hh.put(custid, vv);
      	              }  
    	            }
    	          }
    	       
    	       reader.close();
    	       fr.close();
 		    	}}
    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   //  System.out.println(hh.toString());
    	     
    	   return hh ;
       }
       
       private Hashtable<String,String>  readContacts(){
    	   Hashtable<String,String> hh = new Hashtable<String,String>();

    	     try {
    	    	 
    	    	  String[] list = new File(getDir()).list();
    		         for(int u = 0 ; u < list.length ; u++){
    		    		  String fname = list[u];
    		    		  
    		    	if(fname.indexOf("Contacts") != -1 && fname.indexOf((String)dates.get("Contacts")) != -1){
    		    		 
    				 FileReader fr = new FileReader(getDir() + fname);
    		    	 
    	       BufferedReader reader = new BufferedReader(fr);
    	     String line = null;
    	     reader.readLine();
    	      while((line = reader.readLine()) != null ){

    	           line = line.replaceAll(";"," |");
    	      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
//Contact_ID;Customer_ID;ContactName;ContactName2;ContactName3;Contact_Mobile;
  	//ContactEmail;ContactPhone;ContactFax;ContactNote;ContactName4;RelationActive;SalesTeam_ID;CustToContRelType
    	        if (st.hasMoreTokens()) {
    	        	st.nextToken(); 
    	          String custid = st.nextToken().trim(); 
    	          String name = clear(st.nextToken()).trim();
    	          String name1 = clear(st.nextToken()).trim();
    	          String name2 = clear(st.nextToken()).trim();
    	          String phone = clear(st.nextToken()).trim();
    	          String email = clear(st.nextToken()).trim();
    	          String phone2 = clear(st.nextToken()).trim();
    	          clear(st.nextToken()).trim();
    	          clear(st.nextToken()).trim();
    	          clear(st.nextToken()).trim();
    	          clear(st.nextToken()).trim();
    	         String team =  clear(st.nextToken()).trim();
    	          
    	          String jama = name + " " + name1 + " " + name2 + " " + phone + " " + email + " " + phone2 ;
    	          if(team.equals(teamx)){
    	        	  if(hh.get(custid) == null){
      	            	hh.put(custid, jama);
      	            }else{
      	            	hh.put(custid, hh.get(custid) + "|" + jama);
      	              }  
    	          }
    	            
    	            }
    	          }
    	       
    	       reader.close();
    	       fr.close();
    		    	}}
    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   //  System.out.println(hh.toString());
    	     
    	   return hh ;
       }
       

   public TreeMap getCustomers(Agent agent){

        //  Hashtable custContacts = readContacts();
          Hashtable taraRelations = readTara();
        //  Hashtable addresses = this.readCustAddr();
        //  System.exit(0);
	    try {
	    	   
	         String[] list = new File(getDir()).list();
	         for(int u = 0 ; u < list.length ; u++){
	    		  String fname = list[u];
	    		  
	    	if(fname.indexOf("Receivable") != -1 && fname.indexOf((String)dates.get("Receivable")) != -1){
	    		 
			 FileReader fr2 = new FileReader(getDir() + fname);
	    	 // loe sisse yle makset�htaja summad ja ka k�ik v�lgnevused .
	    	    BufferedReader reader2 = new BufferedReader(fr2);
	    	    
	    	    String   line = null;
	    	      Date datenow = new Date(Calendar.getInstance().getTimeInMillis());
	    	       this.debtList = new Vector();
	    	       reader2.readLine();

	    	       while((line = reader2.readLine()) != null){
	    	    	   line = line.replaceAll(";"," |");
	    	       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

	    	         if (st.hasMoreTokens()) {
	    	        	 String bill = st.nextToken().trim();
	    	             if(bill.length()>6){
	    	             //	bill = bill.substring(bill.length()-6,bill.length());
	    	             }

	    	              String sum = clear(st.nextToken()).trim();
	    	        	   double summa =  0 ;
	    	        	   try{
	    	        		 summa = Double.parseDouble(sum);  
	    	        	   }catch(Exception e){
	    	        	   }
	    	        	 
	    	        	   
	    	        	  String pai = clear(st.nextToken()).trim();
	    	        	   double paid =  0 ;
	    	        	   try{
	    	        		   paid = Double.parseDouble(pai);  
	    	        		   
	    	        	   }catch(Exception e){
	    	        	   }

	    	        	  String docdate = st.nextToken().trim();
	    	        	   Date date = getDate(docdate);
	    	        	  
	    	        	  String duedate = st.nextToken().trim();
	    	               Date due = getDate(duedate);
	    	               
	    	              long days = datenow.getTime()-due.getTime();
	    	              int dueint = Integer.parseInt((days/86400000)+"");
	    	               
	    	               st.nextToken();
	    	        	   
	    	           	
	    	           String custID = st.nextToken().trim();
	    	           Debt debt = new Debt();
	    	           debt.setDoknr(bill);
	    	           debt.setCustID(custID);
	    	           debt.setSum(summa - paid);
	    	           debt.setDuedate(due);
	    	           debt.setDue(dueint + "");
	    	           debt.setDate(date);
	    	           
	    	           if( datenow.after(due)){
                      String payer = custID.substring(0, custID.indexOf("_")+1);
	    	               if(this.overdebts.get(payer)!= null){
	    	            	  overdebts.put(payer, debt.getSum() + overdebts.get(payer) );
	    	                } else {
	    	            	   overdebts.put(payer, debt.getSum());
	    	               }
	    	              // System.out.println("overdebt " + payer );
	    	           }
	    	           
	    	           this.debtList.add(debt);
	    	           }
	    	       }
	    	       reader2.close();
	    	       fr2.close();
	    	}}
	    	      } catch (Exception e){
	    	        e.printStackTrace();
	    	      }

	   
	   

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

     try {

         
         
         String[] list = new File(getDir()).list();
         for(int u = 0 ; u < list.length ; u++){
    		  String fname = list[u];
    		  
    	if(fname.indexOf("Customers") != -1 && fname.indexOf((String)dates.get("Customers")) != -1){
    		 
		 FileReader fr = new FileReader(getDir() + fname);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
//Customer_ID;CustomerName;CustomerName2;CustomerName4;CustomerAddress1;CustomerAddress2;CustomerCity;CustomerZipCode;
     //CustomerDistrict;CustomerCountry;CusLatitude;CusLongitude;CustomerPhone;OrderPhone#1;OrderPhone#2;OrderPhone#3;
     //CustomerFax;CustomerEmal;CustomerRegCode;CustomerVATCode;CusEURegCode;CusCreditLimit;CusCredit;CustomerAccount;IsDistributor;CusCreditBlock;IsDeliveryOnMonday;IsDeliveryOnTuesday;IsDeliveryOnWednesday;IsDeliveryOnThursday;IsDeliveryOnFriday;IsDeliveryOnSaturday;IsDeliveryOnSunday;BillTo;Payer;PaymentTerm;CustomerCL1;CustomerCL2;CustomerCL3;CustomerCL4;CustomerCL5;CustomerCL6;CustSTCL1;CustSTCL2;CustSTCL3;CustSTCL4;CustSTCL5;CustSTCL6;CustomerMCL1;CustomerMCL2;CustomerMCL3;CustomerMCL4;CustomerMCL5;CustomerMCL6;CusTResved;CustomerNoteRO;SalesTeam_ID;TerritoryID;CusTReserved2;Active;CustSTTReserved
    
     line=reader.readLine();
     while((line = reader.readLine()) != null){
    	 
    	 line = line.replaceAll(";"," |");
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){

           String id = st.nextToken().trim();
           Customer customer = new Customer(id);
           String name = st.nextToken().trim();
           //v�tame teise nime.
            st.nextToken().trim();
       
           st.nextToken().trim();
        
           String aadress = st.nextToken().trim();
           aadress = aadress + " " + st.nextToken().trim();
           aadress = aadress.trim() + " " + st.nextToken().trim();
           name = clear(name) + " " + aadress;
           //      System.out.println(line);

            customer.setName(Converter.enCodeEst(name));
           if( aadress != null){
             customer.setAddress(Converter.enCodeEst(Converter.adressLink(aadress,"Lithuania")));           
           }
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
       
           String mobphone   =  clear(st.nextToken()).trim();
          
           if(mobphone.equals("Neturi")){
        	   mobphone = "";
           }
           String phone   =  clear(st.nextToken()).trim();
           if(phone.equals("Neturi")){
        	   phone = "";
           }
           st.nextToken();
           st.nextToken();
           st.nextToken();
           String email   =  clear(st.nextToken()).trim();
           if(email.equals("Neturi")){
        	   email = "";
           }
           
  
           String cont = "";
           if(phone.length() > 5){
        	  cont = cont + phone; 
           }
           if(mobphone.length() > 5){
         	  cont = cont + " ; " +  mobphone; 
            }
           
           if(email.length() > 5){
          	  cont = cont + " ; " +  email; 
             }
       	   
       	  // System.out.println(" cont " + cont);
           if (cont.trim().length()>0){    	
              customer.setPersonalData(Converter.enCodeEst(cont.trim()));
         	}
           
           

           if(taraRelations.get(id) != null){
        		   customer.setInfo((String)taraRelations.get(id));
        	   }
           
          /*
          if(custContacts.get(id) != null){
        	   if(customer.getPersonalData() != null &&  customer.getPersonalData().length() > 1){
        		   customer.setPersonalData(customer.getPersonalData() + "|" +
        				   Converter.enCodeEst((String)custContacts.get(id)));
        	    }else{
        		   customer.setPersonalData(Converter.enCodeEst((String)custContacts.get(id)));
        	   }
           }*/
           
         String payer = st.nextToken().trim();
         if(payer.length() > 0){
             customer.setPayer(payer);        	 
         }

           if (this.custagents.get(id) != null){
        	   Vector hs = (Vector)custagents.get(id);
        	   if(hs.size() > 0 ){
        		   for(int i = 0 ; i< hs.size();i++){
        			   customer.addAgent(new Agent((String)hs.elementAt(i)));
        		   }
        	   }
           }
           
           
       
             customer.addAgent(new Agent("HOUSE1"));
             
            
             
          

             st.nextToken();
             st.nextToken();

             
            	double limit = 0;
            	String lim = st.nextToken().trim();
                
              
            	try{
            		limit = Double.parseDouble(lim);
            	}catch(Exception e ){
            		
            	}
            	
                double used = 0;
                String use = st.nextToken().trim();
                try{
            		used = Double.parseDouble(use);
            	}catch(Exception e ){
            	}
            	
            	customer.setUsedCredit(used);
            	if(limit != 0){
            		customer.setCredit(limit + "");	
            	}
            	

            	st.nextToken();
            	st.nextToken();
            	
            	String blokk = st.nextToken().trim();
            	if(blokk.equals("1")){
            		customer.setSalesBlock(true);
            	}
            
               
            	
            	String mon =  st.nextToken().trim();
            	String tue =  st.nextToken().trim();
            	String wed =  st.nextToken().trim();
            	String thu =  st.nextToken().trim();
            	String fri =  st.nextToken().trim();
            	String sat =  st.nextToken().trim();
            	String sun =  st.nextToken().trim();
            	String deinf = "";
            	if(mon.equals("1")){
            		deinf =deinf + "Mon.";
            	}
            	if(tue.equals("1")){
            		deinf =deinf + "Tue.";
            	}
            	if(wed.equals("1")){
            		deinf =deinf + "Wed.";
            	}
            	if(thu.equals("1")){
            		deinf =deinf + "Thu.";
            	}
            	if(fri.equals("1")){
            		deinf =deinf + "Fri.";
            	}
            	if(sat.equals("1")){
            		deinf =deinf + "Sat.";
            	}
            	if(sun.equals("1")){
            		deinf = deinf + "Sun.";
            	}
            	
            	if(deinf.length() > 0){
            		customer.setCity("Delivery :" + deinf);
            	}
            	
           String paym = null;
          
           st.nextToken();
          payer =  st.nextToken().trim();
          if(payer != null && payer.trim().length() >0){
        	   customer.setPayer(payer);  
        	   if(this.overdebts.get(payer) != null){
                  	customer.setDebt(overdebts.get(payer));
                   }
                
          }
       
           	paym = st.nextToken().trim();
            paym = paym.replaceAll("I","").replaceAll("C", "");
             //System.out.println(customer.getPaymTerm());
           
           customer.setPaymTerm(paym);
          
           //GRINVI_CHE |Grinvil�, UAB |GRINVI_CHE |Kauno apskr. |Chemik� g. 102A | |Jonava | 
           //LT-55001 |Jonavos r.sav. |LT | | |nera |869934006 | | | |info@grinvile.lt |           302606583 |LT100006013411 | |2000 
           //|0 |  |0 |1 |0 |1 |0 |1 |0 |1 |0 |GRINVI_CHE |GRINVI_CHE |I0 | | | |Kiti |  | | | |Veikiantis | |KP/VP transp.
       	//|Miestas | | |veikiantis | | | |0 | |KPVP |KAU3 |2011/08/30 |1 |
  
           //Customer_ID;CustomerName;CustomerName2;CustomerName4;CustomerAddress1;CustomerAddress2;CustomerCity;CustomerZipCode;
           //CustomerDistrict;CustomerCountry;CusLatitude;CusLongitude;CustomerPhone;OrderPhone#1;OrderPhone#2;OrderPhone#3;
           //CustomerFax;CustomerEmal;CustomerRegCode;CustomerVATCode;CusEURegCode;CusCreditLimit;CusCredit;CustomerAccount;
           //IsDistributor;CusCreditBlock;IsDeliveryOnMonday;IsDeliveryOnTuesday;IsDeliveryOnWednesday;IsDeliveryOnThursday;IsDeliveryOnFriday;
           //IsDeliveryOnSaturday;IsDeliveryOnSunday;BillTo;Payer;PaymentTerm;CustomerCL1;CustomerCL2;CustomerCL3;CustomerCL4;CustomerCL5;CustomerCL6;
           //CustSTCL1;CustSTCL2;CustSTCL3;CustSTCL4;CustSTCL5;CustSTCL6;CustomerMCL1;CustomerMCL2;CustomerMCL3;CustomerMCL4;CustomerMCL5;CustomerMCL6;CusTResved;CustomerNoteRO;SalesTeam_ID;TerritoryID;CusTReserved2;Active;CustSTTReserved
          
          // System.out.println("custphone " + mobphone);
           
       
           
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           String team =  st.nextToken().trim();
          String ag =  st.nextToken().trim();
          if(ag.length() > 0){
        	  customer.addAgent(new Agent(ag));
        	  if (ag.equals("reala22")){
        		  customer.addAgent(new Agent("KAU1"));  
        	  }
              customer.setMainAgent(ag);  
          }
          
      
          
     

if(team.equals(teamx)){
	tm.put(Converter.cleanLitLatEst(name), customer);
    this.customers.add(customer.getID());
    
     
}
           	
       }
     }
     
     reader.close();
     fr.close();
    	}}
     } catch (IOException e){
       e.printStackTrace();
     }
     return tm;
   }

private Hashtable prodNames = null;
   
private HashSet getListings(){
	HashSet res = new HashSet();
	try{
		 FileReader fr = new FileReader(getDir() + "static/listings_products.csv");
	     
	     BufferedReader reader = new BufferedReader(fr);
	   String line = null;

	   line=reader.readLine();
	   while((line = reader.readLine()) != null ){
	      line = line.replaceAll(";","| ");
	      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
	      
	       	res.add(clear(st.nextToken().trim()));
	       
	         
	       }	
	}catch(Exception e){
		e.printStackTrace();
	}
	
	return res ;
}

   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     HashSet listings = getListings();
     
     TreeMap<String,Double> packUnits = new TreeMap<String,Double>(); 
     prodNames = new Hashtable();
     try {
     	//laoseisude lugemine ja liitmine algab
          
    	
         String line = null;
         TreeMap prods = new TreeMap(); 
         TreeMap partiid = new TreeMap(); 
    	 try{
    		 
    		   
             
             
             String[] list = new File(getDir()).list();
             for(int u = 0 ; u < list.length ; u++){
        		  String fname = list[u];
        		  
        	if(fname.indexOf("Stock") != -1 && fname.indexOf((String)dates.get("Stock")) != -1){
        		 
    		 FileReader fr = new FileReader(getDir() + fname);
    	
            BufferedReader reader = new BufferedReader(fr);
     
          line = reader.readLine();
           while((line = reader.readLine()) != null ){
                line = line.replace('|','-').replace('|','-').replace('|','-').replaceAll(";"," |");
             //   System.out.println(line);
           	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
                String ladu = clear(st.nextToken()).trim(); // hetkel ladude p�hist ei teinud 
             	String toode = clear(st.nextToken()).trim();
              	double seis = Double.parseDouble(clear(st.nextToken()).trim());
                double alg = 0;
           	   if(prods.get(toode) != null){
           	   	alg = Double.parseDouble((String)prods.get(toode));
           	   }
           	  prods.put(toode,(seis+alg)+"");
           	  try{
           		 String partii = clear(st.nextToken()).trim();
             	   if(partii.trim().length() > 0){
             		  String aa = "";
             		   if(partiid.get(toode) != null){
             			   aa = (String)partiid.get(toode);
             		   }
             		   partiid.put(toode,(aa + "|" + partii));   
             	   }  
           	  }catch (Exception r){
           		  
           	  }
            }
        	}}
      }catch(Exception r){
    		 
    	 }

       	//laoseisude lugemine ja liitmine algab
     	
     
          
      String[] list = new File(getDir()).list();
      for(int u = 0 ; u < list.length ; u++){
 		  String fname = list[u];
 		  
 	if(fname.indexOf("ProductsPa") != -1 && fname.indexOf((String)dates.get("ProductsPA")) != -1){
 		 
		 FileReader fr = new FileReader(getDir() + fname);
         BufferedReader reader = new BufferedReader(fr);
        
   
     
         line = reader.readLine();
         while((line = reader.readLine()) != null){
              line = line.replaceAll(";"," |");
         	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
          String toode = st.nextToken().trim();
          String unit  = st.nextToken().trim();
          double sisu  = Double.parseDouble(st.nextToken().trim());
          if(unit.equals("vnt") && sisu > 1){
        	  packUnits.put(toode,sisu);
        	
          }
          
              //System.out.println(ladu+" "+toode+" "+seis+"");
          }
 	}}
                   
       
      
   list = new File(getDir()).list();
      for(int u = 0 ; u < list.length ; u++){
 		  String fname = list[u];
 		  
 	if(fname.indexOf("Products-") != -1 && fname.indexOf((String)dates.get("Products")) != -1){
 		 
		 FileReader fr = new FileReader(getDir() + fname);
      
		 BufferedReader	   reader = new BufferedReader(fr);
            line = null;

   
    
            reader.readLine();
     while(( line = reader.readLine()) != null ){
        line = line.replaceAll(";"," |");
     
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       int i = st.countTokens();
       while(i < 37){
    	   line = line + "yyyy" + reader.readLine().replaceAll(";"," |");
    	   st = new StringTokenizer(line, SEPARATOR);
    	   i= st.countTokens();
       }
       
 // System.out.println(line);
       
  if (st.hasMoreTokens()){  	
         String id = clear(st.nextToken()).trim();
         Product prod = new Product(id);
        
         if(prods.get(id)!= null){
        	 double seis = Double.parseDouble((String)prods.get(id));
         	prod.setStock(seis);         	
         }else{
        	 prod.setStock(0);     
         }
         if(packUnits.get(id)!= null){
          	prod.setInCases(packUnits.get(id));         	
          }
         
         
         if(partiid.get(id)!= null){
          	prod.setPartii((String)partiid.get(id));         	
          }
         if(st.hasMoreTokens()){
        	try{
        		
        	
        	 st.nextToken();
         	String name = clear(st.nextToken()).trim();
         //	Product_ID;ProductName;ProductLongname;ProductPrice;ProductRPrice;ProductSequence;ProductVAT;ProductPU1
         	//;ProductPU2;ProductPU3;ProductPU4;SerialNumber;POSMaterial;isTaraMaterial;FractionalQTY;ProductCL1;
         	//ProductCL2;ProductCL3;ProductCL4;ProductCL5;ProductCL6;ProdSTNReserved;ProdSTNReserved2
         	//;ProdLReserved;ProductCategory;ProductPLU;ProdTReserved2;ProdNReserved;SalesTeamID;ProductNote;AssignToEveryone;ProdSTTReserved;ProdSTCL2;ProdSTCL3;ProdSTCL4;ProdSTNote;ProdSTCL1
 	
             String pr = st.nextToken().replace(',','.').trim();
             double price =  Double.parseDouble(pr);        
             prod.setPrice(price);
             st.nextToken();
             st.nextToken();
             prod.setVat(st.nextToken().trim());
             st.nextToken();
             st.nextToken();
             st.nextToken();
             prod.setUnit(st.nextToken().trim());
            // System.out.println( st.nextToken());
            // System.out.println( st.nextToken());
             st.nextToken();
             st.nextToken();
             st.nextToken();
             st.nextToken();
             String gr2 = Converter.cleanLitLatEst(st.nextToken().trim()); 
             if(gr2.length() > 0){
            	    prod.setGroup(gr2); 
             }
            
             String team = "" ;
           
             String gr = Converter.cleanLitLatEst(st.nextToken().trim()); 
             if(gr.length() > 0){
           	  prod.setGroupSecond(gr);	 
             }
           String addname =  st.nextToken().trim();
             st.nextToken();
             st.nextToken();
             st.nextToken();
             st.nextToken();
             st.nextToken();
             st.nextToken();
         	 st.nextToken();
             st.nextToken();
             st.nextToken();
           String minPrice =   st.nextToken().trim();
           double minpri = 0;
           try{
        	   minpri = Double.parseDouble(minPrice);  
           }catch(Exception r){
        	   
           }
           team =  st.nextToken();
           
             String info = Converter.enCodeEst(st.nextToken().trim()).replaceAll("\"", "").replaceAll("yyyy", "|");
             //System.out.println("team " + team + " " +minPrice + " " + minpri );
             if(info.length() > 3 ){
            	   if(prod.getPartii() != null && prod.getPartii().trim().length()> 0){
            		   prod.setPartii(prod.getPartii() + "|" + info + "|");
                   }else{
                	   prod.setPartii("|" + info + "|");
                   }
            	  // System.out.println("info " + info);
            	   }
             try{
            	 st.nextToken();
            	 st.nextToken();
            	 st.nextToken();
            
            	 
            	 String type = st.nextToken().trim() ;
            	 if(type.length() > 0){
            		  prod.setType(type); 
               	     addname = type; 
            	 }
            	 st.nextToken();
            	 
            	 while(st.hasMoreTokens()){
            		type =  st.nextToken().trim() ;
                	 if(type.length() > 0){
                		  prod.setType(type); 
                   	     addname = type; 
                	 }
                	 
            	 }
            	 
            //	 System.out.println("type " + type);
            	
             }catch(Exception e){
            	 
             }
           
         	//  System.out.println(prod.getID() + " " + prod.getGroup() + " " +  prod.getGroupSecond() + " " + prod.getUnit() + " " + prod.getPrice());
            	String minp = "";
            	if(minpri > 0){
            		minp =" @:" + this.getNF().format(minpri) + "lt";
            		prod.setMinPrice(minpri);
            	}
           
               prod.setName(Converter.enCodeEst(name + " " + prod.getUnit()) +  minp + " " + addname);
                if( name.toLowerCase().indexOf(" rimi") == -1 && name.toLowerCase().indexOf(" iki") == -1
                		&& name.toLowerCase().indexOf(" cento") == -1 && minpri > 0.01 && listings.contains(prod.getID())){
                  prodNames.put(id, name + " " + prod.getUnit() );
            	   tm.put(Converter.cleanLitLatEst(name + prod.getUnit()), prod);   
               }
            
              }catch (Exception r){
        		r.printStackTrace();
        		//System.out.println(line);
        	}
         
           }
         }
       }}}
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }

   
   
   
   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
	   Vector custProds = new Vector();
	    HashMap custs = new HashMap();
	    //if (true) return custProds ;
	 int counter = 1;
	   String cmd = "  SELECT custaccount as customer,itemid ,qty,invoicedate AS lastdate,(lineamountmst) as summa " +
	      " FROM HAM_INVOICETRANS   WHERE  invoicedate > sysdate-60   " +
	      " GROUP BY custaccount, itemid, invoicedate ,qty,lineamountmst  Order by custaccount,itemid,invoicedate  " ;
	  

	   if (isDebug()) System.out.println(cmd);

	    try {
	    	Statement stmt =  this.con.createStatement();
	      ResultSet rs = stmt.executeQuery(cmd);
	      counter++;
	      while(rs.next()){
	    	
	    	  
	        String kliendikood = rs.getString("customer").trim();

	        String tootekood = rs.getString("itemid").trim();
	        if(!custs.keySet().contains(kliendikood+ tootekood)){
			        double kogus = rs.getDouble("qty");
			        String lastdate = rs.getString("lastdate").substring(0,10);
			        double summa = rs.getDouble("summa") / kogus + 0.0049;
			        summa = Converter.Round((float)summa, 2);
			      
			        lastdate = lastdate.substring(0,10);
			        custs.put(kliendikood + tootekood,lastdate); 
			        lastdate = lastdate.substring(5,10); 
			        CustProd cp = new CustProd(kliendikood, tootekood, kogus ,0 ,0,lastdate+ " " +getNF().format( summa)+ "lt",summa,0,0," ");
			        custProds.add(cp);
	        }
	      }
	   } catch (Exception e){
	      getParent().showError(e);
	    }
	  
	    return custProds;
	  }
   

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "KGGrupp2";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
	
	private String clear (String ss){
			return ss;
	}
   
   
   public double getSalary(String agentCode){

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "data.txt";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap<String,Double> overdebts = new TreeMap<String,Double>();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private String url = "jdbc:odbc:vicireport";
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


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

  	
  		 
  		  String[] list = new File(getDir()).list();
	        for(int u = 0 ; u < list.length ; u++){
			  String fname = list[u];
			  
		if(fname.indexOf("ManagerSales") != -1 && fname.indexOf((String)dates.get("ManagerSales")) != -1){
			 try {
        FileReader fr = new FileReader(getDir() + fname);
         BufferedReader reader = new BufferedReader(fr);
         String line = null;
         
    line=reader.readLine();
    while((line = reader.readLine()) != null){
       line = line.replaceAll(";"," |");
     
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
        	 String agentID = st.nextToken().trim();
          double sales = Double.parseDouble(st.nextToken().trim());
          double volume = Double.parseDouble(st.nextToken().trim());
               
        //  System.out.println("nnn " + sales + " " + volume + " " + agentID + ".");
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
                   agent.setKgSales((int)volume);
            }
         }
      }
    reader.close();
    fr.close();
      } catch (IOException e){
        e.printStackTrace();
      }
  	
			}
		        }
  }

public Vector getFixedAssortmentData() {
	Vector lines = new Vector();

	
	//Listing_ID;HeaderActive;ListingName;ListingParent_ID;ListingTReserved;ListingLReserved;ListingNReserved;
	//ListingNote;RowActive;Product_ID;RowValidFrom;RowValidTill;ShelfPrice;SequenceNumber;OnTheShelf;
	//Excluded;TReserved;LReserved;NReserved;Note;CustomerActive;Customer_ID;SubTree;ValidFrom;ValidTill
	try {

		
		 FileReader fr = new FileReader(getDir() + "Listing.csv");
         BufferedReader reader = new BufferedReader(fr);
       String line = null;
       reader.readLine();
        while((line = reader.readLine()) != null ){

             line = line.replaceAll(";"," |");
        	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          String prodID = st.nextToken().trim();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          st.nextToken();
          
		 String custID =  st.nextToken().trim();
			String clGroup = " ";
         	String prodGroup = " ";
			//System.out.println(prodID + "|"+ custID);
			
			MatrixLine mline = new MatrixLine();

		

			if (custID != null && custID.trim().length() > 0) {
				mline.setCustID(custID.trim());
			}
			if (clGroup != null && clGroup.trim().length() > 0) {
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

        
        reader.close();
        fr.close();
        
	} catch (Exception e) {
		//getParent().showError(e);
	}
	return lines;

}
}
