package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

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

public class S1TabakDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public S1TabakDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	teamx = getParent().getProps().getProperty("salesteam");
	
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
     treeGr2 = new TreeMap();
     treeGr = new TreeMap();
     
     setTreeGroup(true);
     //
   //  "00000000299";"00000006843";"Orbitol";"0";
     
     for (String[] row : readCSVData("groups.csv")) {
    	 int cou = 0;
    	 String id = null;
    	 String id0 = null;
    	 String name = null;
         for (String cell : row) {
        	 if(cou == 0) {
                  id = cell.trim();
                  if(id.trim().length() > 4) {
                 	 id = id.substring(id.length()-4);
                  }
             }
        	 if(cou == 1) {
                 id0 = cell.trim();
                 if(id0.trim().length() > 4) {
                	 id0 = id0.substring(id0.length()-4);
                 }
             }
        	 if(cou == 2) {
                 name = cell.trim();
             }
             cou++;
         }
         if(id0 != null && id0.trim().length() == 0) {
            tm.put(id0, name);	
         } else {
        	 treeGr2.put(id0+id, name);
        	 treeGr.put(id, id0+id);
         }
         
         
     }
         return tm;
   }

   private TreeMap treeGr2 = null;
   private TreeMap treeGr = null;
   
   public TreeMap getProdGroupsSecond() throws SQLException {
	  return treeGr2;    
  }



   private List<String[]> readCSVData(String fname){
	  
	   String csvFile = getDir() + fname; 
       CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
       
       List<String[]> re = null;
               CSVReader reader = null;
			try {
				reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(parser) .build();
			 } catch (FileNotFoundException e) {
				e.printStackTrace();
			 }
      	 
            try {
			    re = reader.readAll();
		      } catch (IOException | CsvException e) {
			   e.printStackTrace();
		      }
            
	   return re;
   }
   
   private Hashtable custagents = new Hashtable();
   private String teamx = null;
   
   public TreeMap getAgents(){
              TreeMap tm = new TreeMap();
             try {
                 for (String[] row : readCSVData("agents.csv")) {
                	 int cou = 0;
                	 String id = null;
                	 String name = null;
                     for (String cell : row) {
                    	 if(cou == 0) {
                              id = cell.trim();
                         }
                    	 if(cou == 1) {
                             name = cell.trim();
                         }
                         cou++;
                     }
                     Agent agent = new Agent(id);
                     agent.setName(Converter.enCodeEst(name + " " + id));
                     tm.put(agent.getName(), agent);	
                     
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
             
     return tm;
   }


   
   


       public String getDistribution() {
         return null;
       }

          
       private Hashtable  readCustAgents() {

           Hashtable res = new Hashtable();
           try {
               for (String[] row : readCSVData("clients_agents.csv")) {
              	 int cou = 0;
              	 String custid = null;
              	 String agent = null;
              	 
                   for (String cell : row) {
                  	 if(cou == 1) {
                            custid = cell.trim();
                       }
                  	 if(cou == 0) {
                           agent = cell.trim();
                       }
                  	   cou++;
                   }
                   ArrayList ags = (ArrayList)res.get(custid);
                   if(ags == null) {
               		  ags = new ArrayList ();   
                   }
                   
                   ags.add(agent);
                   
                   res.put(custid, ags);	
                   
               }
               
           } catch (Exception e) {
               e.printStackTrace();
           }
    	   
    	   return res;
       }
       
       
   public TreeMap getCustomers(Agent agent){

          Hashtable custAgents = readCustAgents();
       
	    try {
	        
	    	    
	    	      Date datenow = new Date(Calendar.getInstance().getTimeInMillis());
	    	       this.debtList = new Vector();
	    	    
	    	       for (String[] row : readCSVData("debts.csv")) {
	    	    	      
	    	    	       String bill = null;
	    	        	   String docdate = null;	    	    	   
	    	               double summa = 0;
	    	        	   String duedate = null; 
	    	        	   String custID = null;
	    	        	   int cou = 0;
	    	           		   //
	    	        	  // "LT1011275:8741317";"2025-01-24";"2025-02-07";"62.79";"TNG25005077";
	    	        	   for (String cell : row) {
	    	              	 if(cou == 0) {
	    	              		custID = cell.trim();
	    	                   }
	    	              	 if(cou == 1) {
	    	              		docdate = cell.trim();
	    	                   }
	    	              	 if(cou == 2) {
	    	              		duedate = cell.trim();
	    	                   }
	    	              	 if(cou == 3) {
	  	    	        	   try{
	  	    	        		 summa = Double.parseDouble(cell.trim());  
	  	    	        	   }catch(Exception e){
	  	    	        	   }
	    	                   }
	    	              	if(cou == 4) {
	    	              		bill = cell.trim();
	    	                   }
	    	              	 cou++;
	    	               }

	    	        	   Date date = getDate(docdate);
	    	        	   Date due = getDate(duedate);
	    	              long days = datenow.getTime()-due.getTime();
	    	              int dueint = Integer.parseInt((days/86400000)+"");
	    	               
	    	        	   
	    	           	
	    	           Debt debt = new Debt();
	    	           debt.setDoknr(bill);
	    	           debt.setCustID(custID);
	    	           debt.setSum(summa);
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
	    	       
	    	       
	    	      } catch (Exception e){
	    	        e.printStackTrace();
	    	      }

	//   System.out.println(overdebts.toString());
	   

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

     try {
         for (String[] row : readCSVData("customers.csv")) {
        	 int cou = 0;
        	 String id = null;
        	 String name = null;
        	 String address = null;
        	 String contact = null;
        	 
             for (String cell : row) {
            	 if(cou == 0) {
                      id = cell.trim();
                 }
            	 if(cou == 1) {
                     name = cell.trim();
                 }
            	 if(cou == 2) {
                     address = cell.trim();
                 }
            	 if(cou == 4) {
                     contact = cell.trim();
                 }
                 cou++;
             }
             Customer  cust = new Customer(id);
             cust.setName( Converter.enCodeEst(name));
             cust.setAddress( Converter.adressLink(address, ""));
             cust.setPersonalData( Converter.enCodeEst(contact));
             if(custAgents.get(cust.getID()) != null) {
            	 ArrayList<String> arr = (ArrayList)custAgents.get(cust.getID());
            	 for(String ag : arr) {
            		 cust.addAgent(new Agent(ag));
            	 }
             }
             if(overdebts.get(id) != null) {
            	 cust.setDebt(Double.parseDouble(overdebts.get(id)+""));
             }

             //  System.out.println("cust " + name + " " + cust.getDebt());
             
             
             tm.put(cust.getName() + cust.getAddress(), cust);	
             
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return tm;
   }

private Hashtable prodNames = null;
   
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     try {
    	   this.getProdGroups();
    	    	 
        TreeMap prods = new TreeMap(); 
        
        for (String[] row : readCSVData("inventories.csv")) {
       	 int cou = 0;
         String ladu = null; // hetkel ladude p�hist ei teinud 
      	 String toode = null;
       	 double seis = 0 ;
       
       	   for (String cell : row) {
           	 if(cou == 0) {
                     toode = cell.trim();
                }
           	 if(cou == 1) {
                    seis = Double.parseDouble(cell.trim());
                }
           	 if(cou == 2) {
                    ladu = cell.trim();
                }
                cou++;
            }
        
              double alg = 0;
           	   if(prods.get(toode) != null){
           	   	alg = Double.parseDouble((String)prods.get(toode));
           	   }
           	  prods.put(toode,(seis+alg)+"");
           	  
        }
            


    

           for (String[] row : readCSVData("products.csv")) {
        	   
    	   int cou = 0;
         String id = null;
      	 String name = null;
      	 String unit = null;
      	 String ean = null;
      	 String gr2 = null;
      	 String gr1 = null;
         double price = 0;
         double inCase = 0;
        
         //"00000000510";"00000000311";"Plāksteri Medrull 'Natural care' N1";"0";"A";"0";"0.1";"12";"162";"0";"0";"NO";"0";"1.1221";"0";"gab.";"4742225004543";"510236";

     	   for (String cell : row) {
         	 if(cou == 0) {
                   id = cell.trim();
             }
         	 if(cou == 1) {
                  gr2 = cell.trim();
                  if(gr2.trim().length() > 4) {
                	  gr2 = gr2.substring(gr2.length()-4);
                  }
             }
         	 if(cou == 2) {
                  name = cell.trim();
             }
         	 if(cou == 14) {
                price = Double.parseDouble(cell.trim());
             }
         	 if(cou == 7) {
                inCase = Double.parseDouble(cell.trim());
             }
         	 
         	if(cou == 15) {
                unit = cell.trim();
             }
         	if(cou == 16) {
                ean = cell.trim();
             }
              cou++;
          }
         
         
         Product prod = new Product(id);
         
         if(prods.get(id)!= null){
         	prod.setStock( Double.parseDouble((String)prods.get(id)));         	
         }
             prod.setPrice(price);
             prod.setVat("21");
              if(unit != null) {
            	 prod.setUnit(unit);	 
              }
             if(gr2 != null && treeGr.get(gr2) != null) {
            	  prod.setGroup((treeGr.get(gr2)+"").substring(0,4));
            	  prod.setGroupSecond(treeGr.get(gr2)+"");
             }
             if(ean != null) {
             prod.setEAN(ean);
             }
             if(gr1 != null) {
         	 prod.setGroup(gr1);
         	 }
             
             if(inCase > 0) {
         	 prod.setInCases(inCase);
             }
             prod.setName(Converter.enCodeEst(name));
          
            
         	 // System.out.println(prod.getID() + " " + prod.getName() + " " + prod.getGroup() +"|"+ prod.getGroupSecond() + " " + prod.getUnit() + " " + prod.getPrice());
            	
               
            	   tm.put(prod.getName(), prod);   
               
           }
           
            }catch (Exception u){
        		u.printStackTrace();
        	}
           

    // System.exit(0);
      
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
	return lines;

}

}
