package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

public class SamsonasCSVDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public SamsonasCSVDBDriver(EksportPanel parent) {
	super(parent);
	this.subjectFilesTo = parent.getString("filemsg");


  }
  private Hashtable dates = new Hashtable();
   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }
   
   

   public Vector getPriceMatrixData(){
       Vector result = new Vector();
	         try {
	          
	        	 FileReader fr = new FileReader(getDir() + "PriceMatrix.csv");
	             BufferedReader reader = new BufferedReader(fr);
	           String line = null;
	           reader.readLine();
	            while((line = reader.readLine()) != null ){

	                 line = line.replaceAll(";"," | ");
	            	StringTokenizer st = new StringTokenizer(line, "|");

	              if (st.hasMoreTokens()) {
	            	//System.out.println( line );
//  00012;;00012;0019D;;;7.6;;
                     st.nextToken();
                     st.nextToken();
                     
                     String clGroup = st.nextToken().trim();
                     String prodID = st.nextToken().trim();
                     st.nextToken();
                     st.nextToken();
                     double price = Double.parseDouble(st.nextToken().trim());
                     String sdate =  st.nextToken().trim();
   	                 String edate = st.nextToken().trim();
   	            
	             MatrixLine mline = new MatrixLine();
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
	            

	             if (price != 0) {
	                 mline.setPrice(price);
                 }
	             
	             if(mline.getPrice() > 0 ){
	                 result.add(mline);    
	               }
	             }
	            }
	            
	            reader.close();
	            fr.close();
	         }catch(Exception e){
	         e.printStackTrace();
	         }
		
	        // System.out.println(result.toString());
	    return result;
	      }

   


   public ManagerData getManagerData() throws SQLException{
	   return null;
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
   
   
   
   
   
   



       public String getSaveStockInfo(){
       return null;
       }








   public Vector getDebt(String agentCode){
        return this.debtList;
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
	    	  FileReader fr = new FileReader(getDir() + "Group.csv");
	    	  BufferedReader reader = new BufferedReader(fr);
	    	  String line = null;
	  
	    	  line=reader.readLine();
		     while((line = reader.readLine()) != null ){
		        line = line.replaceAll(";","| ");
		        StringTokenizer st = new StringTokenizer(line, "|");
		        
		        //GroupCode		ParentGrCode	Description

		         if (st.hasMoreTokens()){
		             String groupcode = st.nextToken().trim();
		             String parentgroupcode = st.nextToken().trim();
		             String name = st.nextToken().trim();
		             
		          
		             if (parentgroupcode.equals("15")) {
		        		 name = "PaDeLe " + name;
		        	 }
		             if (parentgroupcode.equals("3")) {
		        		 name = "PaDeRo " + name;
		        	 }

		        	 if (parentgroupcode.equals("4")) {
		        		 name = "SaruDe " + name;
		        	 }
		        	 if (parentgroupcode.equals("11")) {
		        		 name = "SaruGa " + name;
		        	 }
		        	 if (parentgroupcode.equals("1")) {
		        		 name = "ViDeRo " + name;
		        	 }
		        	 if (parentgroupcode.equals("2")) {
		        		 name = "ViDeLe " + name;
		        	 }
		        	 if(name.length()> 2) {
		        		 tm.put(Converter.cleanLitLatEst(parentgroupcode), Converter.enCodeEst(name));
		 		        
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
	   return tm;
   }



   private Hashtable custagents = new Hashtable();
   private String teamx = null;
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
     //getPriceMatrixData();
     //getCustomerProductsList(null);
   //  getDebt(null);
     try {
       FileReader fr = new FileReader(getDir() + "Agent.csv");
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     reader.readLine();
      while((line = reader.readLine()) != null ){

           line = line.replaceAll(";"," |");
      	StringTokenizer st = new StringTokenizer(line, "|");

        if (st.hasMoreTokens()) {
          String id = st.nextToken().trim();
          Agent agent = new Agent(id);
          if (st.hasMoreTokens()) {
            String name = st.nextToken().trim();
             
            agent.setName(Converter.enCodeEst(name));
            if (st.hasMoreTokens()) {
              String objekt = st.nextToken().trim();
              agent.setObject(objekt);
            }
                tm.put(agent.getName(), agent);	
      
          }
        }
      }
       reader.close();
       fr.close();
       //System.out.println(tm);
     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }




       public String getDistribution() {
         return null;
       }
       
       
       private Hashtable dueSums = new Hashtable();
       private Hashtable usedCredit = new Hashtable();

       
       
       private Date getDate(String mmddyy){
    	  	long lon = 0;
    	  	Calendar cal = Calendar.getInstance();
    	    cal.set(Calendar.YEAR, Integer.parseInt( mmddyy.substring(0, 4)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(4, 6))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(6, 8)));
    	  // System.out.println(cal.getTime()); 
    	   Date date = new Date(lon);
    	   date.setTime(cal.getTimeInMillis());
    	    return date;
    		
    	  }
      
       
       private boolean isDueDate(String yyyymmdd){
    	  	
    	  	Calendar cal = Calendar.getInstance();
    	  	if( yyyymmdd.trim().length() == 0 ) {
    	  		return false;
    	  	 }
    	  
    	    cal.set(Calendar.YEAR, Integer.parseInt( yyyymmdd.substring(0, 4)));
    	    cal.set(Calendar.MONTH, Integer.parseInt(yyyymmdd.substring(4, 6))-1);
    	    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(yyyymmdd.substring(6, 8)));
    		Calendar ca = Calendar.getInstance();
    		ca.add(Calendar.DAY_OF_YEAR, -1);
    		
    		if(cal.before(ca)){
    			return true;
    		}else{
    			return false;
    		}
    		
    	  }


   public TreeMap getCustomers(Agent agent){
     TreeMap tm = new TreeMap();
     

     try {
     FileReader fr = new FileReader(getDir() + "DebtList.csv");
	 BufferedReader reader = new BufferedReader(fr);
	 reader.readLine();
	 
	 String line = null;
	 
     while((line = reader.readLine()) != null){
    	 
    	 line = line.replaceAll(";"," |");
        StringTokenizer st = new StringTokenizer(line, "|");
           	while (st.hasMoreTokens()){
     	//DocumentNo;DocDate;PayDate;OpenAmount;CustomerCode;PayerCode	
           		String number = st.nextToken().trim();
           		String date = st.nextToken().trim();
           		String duedate = st.nextToken().trim();
           		String volg = st.nextToken().trim();
           		String custid = st.nextToken().trim();
     			String payer = st.nextToken().trim();
	        	
     		
	        
		        //	System.out.println(custid  + " " + number + " " + date  + " " + duedate  + " " + volg );	
		       
	        
	        		 double vv = Double.parseDouble(volg);
	        		 if(vv != 0){
	        			 if(isDueDate(duedate)){
				        		     Debt dd = new Debt();
				     				  dd.setCustID(custid);
				     				  dd.setPayerID(payer);
				     				  dd.setDoknr(number);
				     				  dd.setDate(getDate(date));
				     				  dd.setDuedate(getDate(duedate));
				     				  
				     				  int ff = (int)((Calendar.getInstance().getTimeInMillis() - getDate(duedate).getTime())/86400000);
				     				  dd.setDue(ff + "");
				     				  dd.setSum(vv);
				     				//	System.out.println("aaa" + date + " " + duedate + " " + number + " " + custid + " " +ff);	
						        			  
				     				  if(dueSums.get(payer) != null){
				     					  double ss = Double.parseDouble(dueSums.get(payer) + "")+ vv ;
				     					 dueSums.put(payer, ss);    
				     				  }else{
				     					 dueSums.put(payer, volg); 
				     				  }
				     				 debtList.add(dd);

							        		
				        	 }
	        			 

	     				  if(usedCredit.get(payer) != null){
	     					  double ss = Double.parseDouble(usedCredit.get(payer) + "")+ vv ;
	     					 usedCredit.put(payer, ss);    
	     				  }else{
	     					 usedCredit.put(payer, volg); 
	     				  }
	        			 
	        		 }
           	}
     }
     
     
	  fr = new FileReader(getDir() + "Customer.csv");
	  reader = new BufferedReader(fr);
      line = null;
     //Custcode	 CustName	PayerCode	Address	City	Post Code	DebtDue	SalespersonCode	Contact	PhoneNo	PaymentTerm	CreditLimit	UsedCredit	CustDiscount	Blocked

     line=reader.readLine();
     while((line = reader.readLine()) != null){
    	 
    	 line = line.replaceAll(";"," |");
        StringTokenizer st = new StringTokenizer(line, "|");
       if (st.hasMoreTokens()){

           String id = st.nextToken().trim();
           String name = st.nextToken().trim();
           String payer = st.nextToken().trim();
           String city = st.nextToken().trim();
           String address = st.nextToken().trim();
           String zipcode = st.nextToken().trim();
           String deptdue = st.nextToken().trim();
           String agentcode = st.nextToken().trim();
           String contactname = st.nextToken().trim();
           String contactphone = st.nextToken().trim();
           String payterm = st.nextToken().trim();
           String crlimit = st.nextToken().trim();
           String usedcredit = st.nextToken().trim();

           Customer customer = new Customer(id);
           customer.setName(Converter.enCodeEst(name));
           
            
           if( address.length() > 0){
        	   if(city.length() > 0) {
        		   address = address + " " + city;
            	   if(zipcode.length() > 0) {
            		   address = address + " " + zipcode;
            	   }
        	   }
             customer.setAddress(Converter.enCodeEst(Converter.adressLink(address,"Lithuania")));           
           }
           
	       	double dept = 0;
	       	try{
	       		dept = Double.parseDouble(deptdue);
	       		customer.setDebt(dept);
	       	}catch(Exception e ){
	       	}
           
           if(agentcode.length() > 0) {
        	   customer.addAgent(new Agent(agentcode));
           }
           
           
           String cont = "";
           if(contactname.length() > 0) {
        	   cont=contactname;
           }

           if(contactphone.length() > 0) {
        	   cont=cont + " Tel:" + contactphone.trim();
           }
           
           if (cont.length()>0){    	
              customer.setPersonalData(Converter.enCodeEst(cont.trim()));
         	}
           

         if(payer.length() > 0){
             customer.setPayer(payer);  
             if(dueSums.get(payer) != null) {
            	 customer.setDebt(Double.parseDouble(dueSums.get(payer) +""));
             }
           
         }
         
             
    	double limit = 0;
    	try{
    		limit = Double.parseDouble(crlimit);
    		if(limit > 0) {
    			customer.setCredit(limit + "");	
      		  if(usedCredit.get(payer) != null) {
               	 customer.setUsedCredit(Double.parseDouble(usedCredit.get(payer) +""));
                }
    		}
    	
    	}catch(Exception e ){
    	}
    	
       
        
        if(payterm.length() > 0) {
        	customer.setPaymTerm(payterm);
        }
   
        
	tm.put(Converter.cleanLitLatEst(name), customer);
    
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
     

     try {

     	//laoseisude lugemine ja liitmine algab
          
    	
         String line = null;
         
		 FileReader fr = new FileReader(getDir() + "Product.csv");
		 BufferedReader reader = new BufferedReader(fr);

        reader.readLine();
        while(( line = reader.readLine()) != null ){
        line = line.replaceAll(";"," |");
     
       StringTokenizer st = new StringTokenizer(line, "|");
      //Code		Description		Unit	incase	GroupCode	GroupSecCode	UnitPrice	EAN	Inventory	Pallet	Blocked

       if (st.hasMoreTokens()){
    	   String id = st.nextToken().trim();
    	   String description = st.nextToken().trim();
    	   String unit = st.nextToken().trim();
    	   String incase = st.nextToken().trim();
    	   String groupcode = st.nextToken().trim();
    	   String groupcodesec = st.nextToken().trim();
    	   String unitprice = st.nextToken().trim();
    	   String ean = st.nextToken().trim();
    	   String inventory = st.nextToken().trim();

    	   Product prod = new Product(id);
    	   //System.out.println(description);

			 if(Double.parseDouble(inventory) > 1) {
				 prod.setStock(Double.parseDouble(inventory));
			 }
			 
			 if(Double.parseDouble(incase) > 1) {
				 prod.setInCases(Double.parseDouble(incase));
			 }
			 
			 if(unit.length() > 0) {
				 prod.setUnit(unit);
			 }
			 
			 if(groupcodesec.length() > 0) {
				 prod.setGroup(groupcodesec);
			 }
			 
			 if(description.length() > 0) {
				 prod.setName(description + " "+ id);
			 
	
				 if(Double.parseDouble(unitprice) > 0) {
					 prod.setPrice(Double.parseDouble(unitprice));
				 }
				 
				 if(ean.length() > 0) {
					 prod.setEAN(ean);
				 }
				 
				 prod.setVat("21");
				 tm.put(Converter.cleanLitLatEst(description + prod.getUnit()), prod);
			 
			 }
			 
			 
			 

         }
       }
        
        reader.close();
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
  try {
	  
     FileReader fr = new FileReader(getDir() + "CustomerProducts.csv");
		 BufferedReader reader = new BufferedReader(fr);
		 String line = null;
      
		 reader.readLine();
      while(( line = reader.readLine()) != null ){
    	  line = line.replaceAll(";"," |");
    	   StringTokenizer st = new StringTokenizer(line, "|"); 
    	 //  CustCode;ProductCode;Qty;LastDate;InvoiceNo;LastPrice
    	   String custID = st.nextToken().trim();
        	String prodID = st.nextToken().trim();
			double sold = Double.parseDouble(st.nextToken().trim());							
			String last = st.nextToken().trim();
			String lastdoc = st.nextToken().trim();
			double lastprice = Double.parseDouble(st.nextToken().trim());	
			
			CustProd cp = new CustProd(custID, prodID,
					 sold, 0,0, last, lastprice, 0, 0, " ");
	
			cp.setAbiKood(lastdoc);
			custProds.addElement(cp);
      }
      
	  reader.close();
  }catch(Exception e) {
	  e.printStackTrace();
  }
	
  //System.out.println(custProds.toString());
	    return custProds;
	  }
   


   public String getDriverVersion(){
     return "SamsonasCSV";
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


   private String subjectFilesTo = null;
   private TreeMap<String,Double> overdebts = new TreeMap<String,Double>();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private Vector klient = new Vector();
   private TreeMap agents = new TreeMap();
   private NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH);

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
     
       StringTokenizer st = new StringTokenizer(line, "|");
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
	return null;
}
@Override
public String getStartingTime() {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void connectionUp() {
	// TODO Auto-generated method stub
	
}
@Override
public void connectionDown() {
	// TODO Auto-generated method stub
	
}
@Override
public void PictureMaker() {
	String pictssou = parent.getProps().getProperty("pict.dir");
    String pictspda = parent.getProps().getProperty("pictpda.dir");
   
	 try {
	  
    File f = new File(pictspda +"list.txt");
    if (f.exists())f.delete();
    //Kataloogi sisude kustutamine
	 	 File res2 = new File (pictspda);
         String[] ff2 = res2.list();
     	 for(int i = 0;ff2.length>i;i++){
         	(new File ( pictspda + ff2[i])).delete(); 
         }
//     	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

     StringBuffer buf = new StringBuffer();
     Iterator enume = prods.keySet().iterator();

     while (enume.hasNext()) {
	    	String asi = (String)enume.next();
	    	Product prod = (Product)prods.get(asi);
	    	String toode = prod.getEAN().trim();  
	    	//System.out.println("pildid" + toode);
           if(toode.length() > 0){
            	
	   	  res2 = new File (pictssou);
          ff2 = res2.list();
         
     	 for(int i = 0;ff2.length>i;i++){
         	if( ff2[i].startsWith(toode)){
           // System.out.println("pilt olemas " + ff2[i]);
        	File in = new File ( pictssou + ff2[i]); 
          	if(in.isFile()){
          	File inpda = new File ( pictspda + prod.getID()+ ".jpg");
          	if(!inpda.exists()){
              	Converter.shrinkAndCopyFile(in,inpda,400);
      	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
                	}
      	  	}   }
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
 System.exit(0);
}




}
