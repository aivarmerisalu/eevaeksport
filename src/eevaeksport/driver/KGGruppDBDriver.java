package eevaeksport.driver;

import java.io.BufferedReader;
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

public class KGGruppDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public KGGruppDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";
    teamx = getParent().getProps().getProperty("salesteam");
	try {
	    Class.forName(databaseDriverName).newInstance();
	} catch (Exception e) {
	    parent.showError(e);
	}
  }

   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
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
	        	 e.printStackTrace();
	         }
		
	    return result;
	      }

   
   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

   public void insertDefective(DefectiveWrapper wr){}

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
    cal.set(Calendar.YEAR, Integer.parseInt(mmddyy.substring(0, 4)));
    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(6, 7))-1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(9, 10)));
  //  System.out.println(cal.getTime());  //05.02.14
   Date date = new Date(lon);
   date.setTime(cal.getTimeInMillis());
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
        FileReader fr = new FileReader(getDir() + "ProductDictionary.csv");
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
            tm.put(cleardots(id), Converter.enCodeEst( name));
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

   public TreeMap getProdGroupsSecond() throws SQLException {
	    TreeMap   tm = new TreeMap();
	    //  setTreeGroup(true);
	      try {
	         FileReader fr = new FileReader(getDir() + "ProductDictionary.csv");
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
	             tm.put(cleardots(id), Converter.enCodeEst( name));
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
       FileReader fr = new FileReader(getDir() + "CustomerAssigment.csv");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
    
      while((line = reader.readLine()) != null ){

           line = line.replaceAll(";"," |");
      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

        if (st.hasMoreTokens()) {
         String custid = st.nextToken().trim(); 
          String id = st.nextToken().trim();
          Agent agent = new Agent(id);
          if (st.hasMoreTokens()) {
            String name = st.nextToken().trim();
             
            agent.setName(Converter.enCodeEst(name + " " + id));
            if (st.hasMoreTokens()) {
              String objekt = st.nextToken().trim();
              agent.setObject(objekt);
            }
            
            if(custagents.get(custid) == null){
            	Vector hs = new Vector();
            	hs.add(id);
            	custagents.put(custid, hs);
            }else{
            	Vector hs = ( Vector) custagents.get(custid);
            	hs.add(id);
            	custagents.put(custid, hs);
            }
            if(name.equals(teamx)){
                tm.put(agent.getName(), agent);	
            }
      
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
    	       FileReader fr = new FileReader(getDir() + "TaraBalance.csv");
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

    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   //  System.out.println(hh.toString());
    	     
    	   return hh ;
       }
       
       
       private Hashtable<String,Vector>  readCustAddr(){
    	   Hashtable<String,Vector> hh = new Hashtable<String,Vector>();

    	     try {
    	       FileReader fr = new FileReader(getDir() + "CustomerAddress_ID.csv");
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

    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   //  System.out.println(hh.toString());
    	     
    	   return hh ;
       }
       
       private Hashtable<String,String>  readContacts(){
    	   Hashtable<String,String> hh = new Hashtable<String,String>();

    	     try {
    	       FileReader fr = new FileReader(getDir() + "Contacts.csv");
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

    	     } catch (Exception e){
    	       e.printStackTrace();
    	     }
    	   //  System.out.println(hh.toString());
    	     
    	   return hh ;
       }
       

   public TreeMap getCustomers(Agent agent){

          Hashtable custContacts = readContacts();
          Hashtable taraRelations = readTara();
          Hashtable addresses = this.readCustAddr();
        //  System.exit(0);
	    try {
	        
	    	 // loe sisse yle makset�htaja summad ja ka k�ik v�lgnevused .
	    	    FileReader fr2 = new FileReader(getDir() + "Receivables.csv");
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
	    	           
	    	           if( datenow.before(due)){

	    	               if(this.overdebts.get(custID)!= null){
	    	            	  overdebts.put(custID, debt.getSum() + overdebts.get(custID) );
	    	                } else {
	    	            	   overdebts.put(custID, debt.getSum());
	    	               }
	    	           }
	    	           
	    	           this.debtList.add(debt);
	    	           }
	    	       }
	    	       reader2.close();
	    	       fr2.close();
	    	       
	    	      } catch (Exception e){
	    	        e.printStackTrace();
	    	      }

	   
	   

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

     try {
       FileReader fr = new FileReader(getDir() + "Customers.csv");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
//Customer_ID;CustomerName;CustomerName2;CustomerAddress1;CustomerAddress2;CustomerCity;CustomerZipCode;CustomerDistrict
     
     line=reader.readLine();
     while((line = reader.readLine()) != null){
    	 
    	 line = line.replaceAll(";"," |");
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){

           String id = st.nextToken().trim();
           Customer customer = new Customer(id);
           
           String name = st.nextToken().trim();
           //v�tame teise nime.
           name = st.nextToken().trim();
           name = clear(name);
           //      System.out.println(line);

           customer.setName(Converter.enCodeEst(name));
       
           
           String aadress = st.nextToken().trim();
   
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
           String email   =  clear(st.nextToken()).trim();
           if(email.equals("Neturi")){
        	   email = "";
           }
          // System.out.println("custphone " + mobphone);
           
           String cont = "";
           if(phone.length() > 1){
        	  cont = cont + phone; 
           }
           if(mobphone.length() > 1){
         	  cont = cont + "  " +  mobphone; 
            }
           
           if(email.length() > 1){
          	  cont = cont + "  " +  email; 
             }
       	   
       	  // System.out.println(" cont " + cont);
           if (cont.trim().length()>0){    	
              customer.setPersonalData(Converter.enCodeEst(cont.trim()));
         	}
           
           

           if(taraRelations.get(id) != null){
        		   customer.setInfo((String)taraRelations.get(id));
        	   }
           
           if(custContacts.get(id) != null){
        	   if(customer.getPersonalData() != null &&  customer.getPersonalData().length() > 1){
        		   customer.setPersonalData(customer.getPersonalData() + "|" +
        				   Converter.enCodeEst((String)custContacts.get(id)));
        	    }else{
        		   customer.setPersonalData(Converter.enCodeEst((String)custContacts.get(id)));
        	   }
           }
           st.nextToken();
           st.nextToken(); 
           customer.setPayer(id);

           if (this.custagents.get(id) != null){
        	   Vector hs = (Vector)custagents.get(id);
        	   if(hs.size() > 0 ){
        		   for(int i = 0 ; i< hs.size();i++){
        			   customer.addAgent(new Agent((String)hs.elementAt(i)));
        		   }
        	   }
           }
           
           
       
             customer.addAgent(new Agent("HOUSE1"));
             
             if(this.overdebts.get(id) != null){
            	customer.setDebt(overdebts.get(id));
             }
             
                          
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
            	customer.setCredit(limit + "");

           String paym = null;
           st.nextToken();
           st.nextToken();
           st.nextToken();
           	paym = st.nextToken().trim();
            paym = paym.replaceAll("I","");
             //System.out.println(customer.getPaymTerm());
           
           customer.setPaymTerm(paym);
          
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
          String team =  st.nextToken().trim();
          
      

           //;;CustomerRegCode;CustomerVATCode;CusCreditLimit;CusCredit;IsDistributor;
           //BillTo;Payer;PaymentTerm;CustomerCL1;CustomerCL2;CustomerCL3;CustomerCL4;SalesTeam_ID;Active;TerritoryID;CusCreditBlock;CustomerCL5;StopFlag
  

           if (st.hasMoreTokens()){
            //customer.setVisit(clear(st.nextToken()));
            //System.out.println(customer.getVisit());
          }
         //  if (st.hasMoreTokens()){
           
//           st.nextToken();
         //   System.out.println(customer.getVisit());
          //}
           if (st.hasMoreTokens()){
          //  customer.setCustType(clear(st.nextToken()));

          }


if(team.equals(teamx)){
	tm.put(customer.getName(), customer);
    this.customers.add(customer.getID());
    
    //kui on olemas aadressid siis tuleks lisada juurde .
    if(addresses.get(id) != null){
    
    	Vector vv = (Vector)addresses.get(id);
    	
    	if(vv.size() > 0 ){
    		for(int i = 0; i < vv.size() ; i++ ){
    			Toode tt = (Toode)vv.elementAt(i);
    			String addrid = tt.getID();
    			String addr = tt.getBrand();
    			
    		
    			Customer c = new Customer(id + "-" + addrid);
    			c.setName(Converter.enCodeEst(customer.getName() + " " + addr));
    			c.setDebt(customer.getDebt());
    			
    			if (this.custagents.get(id) != null){
    	        	   Vector hs = (Vector)custagents.get(id);
    	        	   if(hs.size() > 0 ){
    	        		   for(int e = 0 ; e< hs.size();e++){
    	        			   c.addAgent(new Agent((String)hs.elementAt(e)));
    	        		   }
    	        	   }
    	           }
    			
    			c.setPayer(customer.getPayer());
    			c.setPaymTerm(customer.getPaymTerm());
    			c.setAddress(Converter.adressLink(addr, "Lithuania"));
    			c.setPersonalData(customer.getPersonalData());
    			c.setInfo(customer.getInfo());
    			c.setUsedCredit(used);
            	c.setCredit(limit + "");
            	tm.put(c.getName(), c);
                this.customers.add(c.getID());
    		}
    	}
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

private Hashtable prodNames = null;
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();

     prodNames = new Hashtable();
     try {
     	//laoseisude lugemine ja liitmine algab
          
    	 FileReader fr = new FileReader(getDir() + "Stock.csv");
            BufferedReader reader = new BufferedReader(fr);
          String line = null;
        TreeMap prods = new TreeMap(); 
        TreeMap partiid = new TreeMap(); 
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
           

     	

       	//laoseisude lugemine ja liitmine algab
     	
     
          
          
         fr = new FileReader(getDir() + "PackagingUnits.csv");
         reader = new BufferedReader(fr);
        
      TreeMap<String,Double> packUnits = new TreeMap<String,Double>(); 
     
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
                   
          
          
    	  fr = new FileReader(getDir() + "Products.csv");
    	   reader = new BufferedReader(fr);
            line = null;

   
    
            reader.readLine();
     while(( line = reader.readLine()) != null ){
        line = line.replaceAll(";"," |");

       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
    	   
    	   //Product_ID;ProductName;ProductLongname;ProductPrice;ProductRPrice;ProductSequence;
    	   //ProductVAT;ProductPU1;ProductPU2;ProductPU3;ProductPU4;SerialNumber;POSMaterial;
    	   //FractionalQTY;ProductCL1;ProductCL2;SalesTeamID;ProductCategory;Batch;ProdNReserved;ProdSTCL1;
    	   //AssignToEveryone;ProdTReserved;ProductNote;ProductCL3 a14EUROPAL;Euro paletes;Euro 
         String id = clear(st.nextToken()).trim();
         
         Product prod = new Product(id);
         
         if(prods.get(id)!= null){
        	 double seis = Double.parseDouble((String)prods.get(id));
         	prod.setStock(seis);         	
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
             prod.setGroupSecond(st.nextToken().trim());
             String team = st.nextToken().trim();
         	  prod.setGroup(st.nextToken().trim());
            
         	 st.nextToken();
             st.nextToken();
             st.nextToken();
             st.nextToken();
             st.nextToken();
             String info = st.nextToken().trim();
             if(info.length() > 3 ){
            	   if(prod.getPartii() != null && prod.getPartii().trim().length()> 0){
            		   prod.setPartii(prod.getPartii() + "|" + info);
                   }else{
                	   prod.setPartii(info);
                   }
            	  // System.out.println("info " + info);
            	   }
             try{
            	  prod.setAlegrupp( st.nextToken().trim() ); 
             }catch(Exception e){
            	 
             }
           
         	//  System.out.println(prod.getID() + " " + prod.getGroup() + " " +  prod.getGroupSecond() + " " + prod.getUnit() + " " + prod.getPrice());
            	
           
               prod.setName(Converter.enCodeEst(name + " " + prod.getUnit()));
               
               prodNames.put(id, name + " " + prod.getUnit() );
               if( team.equals(teamx) || prod.getVat().equals("0")){
            	   tm.put(prod.getName(), prod);   
               }
            
              }catch (Exception u){
        		u.printStackTrace();
        	}
           }
         }
       }
     } catch (IOException e){
       e.printStackTrace();
     }
     return tm;
   }

   
   
   
   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
   /*  HashMap kliendid = new HashMap();
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

   line=reader.readLine();
   while((line = reader.readLine()) != null ){
      line = line.replaceAll(";"," |");
      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String custID = clear(st.nextToken());
               if (st.hasMoreTokens()){
                 String prodID = clear(st.nextToken());
              
                 double amount = 0;
                 String lastdate = " ";
                 if (st.hasMoreTokens()){
                 	String tok = st.nextToken();
                 	tok = tok.replaceAll("\"","");
                 	if(tok.indexOf(",") == 0){
                 		tok = tok.substring(1,tok.length());
                 	}
                 	
                try{
                	
            
                    amount = Double.parseDouble(clear(tok));
                
                 if (st.hasMoreTokens()){
                     lastdate = clear(st.nextToken());
                 }

                 if (st.hasMoreTokens()){
                    Double.parseDouble(clear(st.nextToken()));
                 }
                 
                    if(lastdate.length()>4){
                    lastdate = lastdate.substring(0,5);
                    }else{
                    lastdate =" ";
                    }
                    Customer cust = (Customer)kliendid.get(custID);
                    if (cust == null){
                      cust = new Customer(custID);
                    }
                    Product prod = new Product(prodID);

                 CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate,0,0,0," ");

                 cust.addProduct(prod,cp,false);
                 kliendid.put(custID, cust);
                 vec.addElement(cp);
                }catch (Exception e){
                	System.out.println("aM :" + tok);
                }
                 }

           }
         }
       }



    fr = new FileReader(getDir() + DATA_FILE);
    reader = new BufferedReader(fr);
  line = null;

           int counter = 0;
           int cou = 0;
          String oldklient = null;
          String olddate = " ";
          String oldtoode = null;
 
          line=reader.readLine();
 while((line = reader.readLine()) != null){
    line = line.replaceAll(";"," |");
    StringTokenizer st = new StringTokenizer(line, SEPARATOR);
     if (st.hasMoreTokens()){
       String klient = clear(st.nextToken());
             if (st.hasMoreTokens()){
               String toode = clear(st.nextToken());
               String kogus = "0";
               String date = " ";
               if (st.hasMoreTokens()){
                  kogus = clear(st.nextToken());
                  kogus = kogus.replace('.',',');
				  if(kogus.indexOf(",") != -1){
				  	kogus=kogus.substring(0,kogus.indexOf(",")+2);
				  }
              }
               if (st.hasMoreTokens()){
                   date = clear(st.nextToken());
               }


               if(cou == 0){oldklient = klient;olddate = date;oldtoode=toode;}
               if(!oldklient.equals(klient)){
                             counter = 0;
                             oldklient = klient;
                             olddate = date;
               }
               if(!toode.equals(oldtoode)){
               	counter = 0;
               }
               Customer c = (Customer)kliendid.get(klient);
               if (c != null ){

                 ArrayList al = (ArrayList)c.getProducts().get(toode);
                 if (al != null){
                   CustProd cp = (CustProd)al.get(1);
                   if (cp != null){
                    if(!olddate.equals(date)){counter++;}
                     if (counter == 0){cp.setS1(kogus );}
                     if (counter == 1){cp.setS2(kogus );}
                     if (counter == 2){cp.setS3(kogus );}
                     if (counter == 3){cp.setS4(kogus );}
                     if (counter == 4){cp.setS5(kogus );}
                     
                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
                 oldtoode = toode;
               }
               cou++;
             }}}

     } catch (IOException e){
       e.printStackTrace();
     }*/
     return vec;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "KGGrupp";
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

  	 try {
        FileReader fr = new FileReader(getDir() + "ManagerSales.csv");
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
		getParent().showError(e);
	}
	return lines;

}
}
