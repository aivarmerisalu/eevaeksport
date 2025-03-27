package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

public class S1TabakDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager,PictureMaker {

  public S1TabakDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	teamx = getParent().getProps().getProperty("salesteam");
	//custgroups();
  }

   public String getPilotDayData(Agent agent){
     return null;
   }
   public String getSaveDebtInfo() {
   	return null;
   }
   
   private HashMap custgroups() {
	   HashMap res = new HashMap();
	   
	   Map<String, Set<String>> priceListToCustomers = new HashMap<>();
       Map<String, Map<String, Double>> priceGroupToProducts = new HashMap<>();
       Map<String, String> customerToPriceGroup = new HashMap<>();
       
       HashSet custs = new HashSet();
	   
	   for (String[] row : readCSVData("ClientsProductsDiscounts.csv")) {
	       	 int cou = 0;
	         String customer = null; 
	      	 String product = null;
	       	 Double price = null ;
	       	
	       
	       	  for (String cell : row) {
	       		   
	           	 if(cou == 0) {
	           		customer = cell.trim().replace(':', '_');
	           		if(!custs.contains(customer)) {
	           		custs.add(customer);
	                }
	           	 }
	           	 if(cou == 1) {
	           		product = cell.trim();
	                }
	           	 if(cou == 2) {
	           		price = Double.parseDouble(cell.trim());
                    }
	           	 
	                cou++;
	       	   }
	       	   

	            // Build a unique key for the price list
	            String priceListKey = customerToPriceGroup.getOrDefault(customer, "") + product + ":" + price + ";";

	            // Store the price list mapping
	            priceListToCustomers.computeIfAbsent(priceListKey, k -> new HashSet<>()).add(customer);

	            // Store the product-price mapping for this price list
	            priceGroupToProducts.computeIfAbsent(priceListKey, k -> new HashMap<>()).put(product, price);
	      
	         

	        
	     }

	       	   
	   
	       	   
	   // Step 2: Assign a unique PriceGroup ID
       int groupCounter = 1;
       Map<String, String> priceKeyToGroup = new HashMap<>();
       for (String priceListKey : priceListToCustomers.keySet()) {
           String priceGroup = "PG-" + groupCounter++;
           priceKeyToGroup.put(priceListKey, priceGroup);
       }

       // Step 3: Map Customers to their PriceGroup
       for (Map.Entry<String, Set<String>> entry : priceListToCustomers.entrySet()) {
           String priceListKey = entry.getKey();
           String priceGroup = priceKeyToGroup.get(priceListKey);

           for (String customer : entry.getValue()) {
               customerToPriceGroup.put(customer, priceGroup);
           }
       }

       // Step 4: Rename price group map keys
       Map<String, Map<String, Double>> finalPriceGroupToProducts = new HashMap<>();
       for (Map.Entry<String, Map<String, Double>> entry : priceGroupToProducts.entrySet()) {
           String newPriceGroup = priceKeyToGroup.get(entry.getKey());
           finalPriceGroupToProducts.put(newPriceGroup, entry.getValue());
       }

       
      
       // Step 5: Print Results
       System.out.println("Customer to PriceGroup:" +  custs.size());
     //  customerToPriceGroup.forEach((customer, pg) -> System.out.println(customer + " -> " + pg));

       System.out.println("\nPriceGroup to Customers:" +        priceListToCustomers.size());
       priceListToCustomers.forEach((key, customers) -> {
           String pg = priceKeyToGroup.get(key);
         //  System.out.println(pg + " -> " + customers);
       });

       System.out.println("\nPriceGroup to Product-Price:" + finalPriceGroupToProducts.size());
       finalPriceGroupToProducts.forEach((pg, productMap) -> {
          // System.out.println(pg + ":");
         //  productMap.forEach((product, price) -> System.out.println("  " + product + " -> " + price));
       });
 	   
	       	   
	       	   
	   
	             
	            
	   return res;
   }
   
   
   private HashMap clGroups = null;
   
   public Vector getPriceMatrixData(){
	  	
   HashSet check = new HashSet();
	       Vector result = new Vector();

	       // teeme kampaaniad kõigepealt
	       
	       //peame korduvaid ridu mitte saatma systeemi. 
	       //kliendi gruppide objekt peab olema 

	       for (String[] row : readCSVData("actions.csv")) {
  	       	 int cou = 0;
  	         String klient = null;
  	         String klientGR = null;
  	      	 String toode = null;
  	       	 double hind = 0 ;
  	       	 String start = null;
  	       	 String end = null;
  	       	 String lisa = null;
  	       	 //"";"";"Papīra dvieļi 'Ruta' MAX 1r. 2sl.";"1.79";"01.01.0001 00:00:00";"17.03.2025 00:00:00";
  	       
  	       	   for (String cell : row) {
  	           	 if(cou == 0) {
  	                     klient = cell.trim().replace(':', '_');;
  	                }
  	           if(cou == 1) {
  	        	 klientGR = cell.trim();
	                }
  	           	 if(cou == 3) {
  	                    toode = cell.trim();
  	                }
  	           	 if(cou == 4) {
  	                    hind = Double.parseDouble(cell.trim());
  	                }
  	           if(cou == 5) {
  	        	   start = cell.trim();
	                }
  	            if(cou == 6) {
                   end = cell.trim();
                   }
  	            
  	            if(cou == 7) {
                   lisa = cell.trim();
                   }
  	           	 cou++;
  	       	   }
  	              MatrixLine mline = new MatrixLine();

 	             if (klient != null && klient.length() > 0) {
 	               mline.setCustID(klient);
 	             }

 	             if (klientGR != null && klientGR.length() > 0) {
 	               mline.setCliGroup(klientGR);
 	              } else if (lisa != null && lisa.trim().length() > 0){
 	               mline.setCliGroup(lisa);
 	 	         }

 	             
 	             
 	              if (toode != null && toode.length() > 0) {
 	               mline.setProdID(toode);
 	             }
 	              
 	             if (hind > 0) {
 	 	               mline.setPrice(hind);
 	 	             }

 	             if (start != null && start.length() > 0) {
 	               mline.setStartDate(start.substring(0,10));
 	             }

 	             if (end != null && end.length() > 0) {
 	               mline.setEndDate(end.substring(0,10));
 	             }
 	            result.add(mline); 
	       }
	    
	       Calendar cal = Calendar.getInstance();
	       YearMonth yearMonth = YearMonth.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1);
	         LocalDate firstDay = yearMonth.atDay(1);
	         LocalDate lastDay = yearMonth.atEndOfMonth();
	       
	       
	       for (String[] row : readCSVData("ClientsProductsDiscounts.csv")) {
  	       	 int cou = 0;
  	         String klient = null; 
  	      	 String toode = null;
  	       	 double hind = 0 ;
  	  	     double disc = 0 ;
  	       	 String hh = null;
  	       
  	       	   for (String cell : row) {
  	           	 if(cou == 0) {
  	                     klient = cell.trim().replace(':', '_');
  	                }
  	           	 if(cou == 1) {
  	                    toode = cell.trim();
  	                }
  	           	 if(cou == 2) {
  	                    hind = Double.parseDouble(cell.trim());
  	                  hh = cell.trim();
  	                  }
  	           if(cou == 3) {
	                    disc = Double.parseDouble(cell.trim());
	                  }
  	                cou++;
  	       	   }
  	              MatrixLine mline = new MatrixLine();

 	             if (klient != null && klient.length() > 0) {
 	               mline.setCustID(klient);
 	             }
 	             

 	              if (toode != null && toode.length() > 0) {
 	               mline.setProdID(toode);
 	             }
 	              
 	             if (hind > 0) {
 	 	               mline.setPrice(hind);
 	 	             }
 	            if (disc > 0) {
	 	               mline.setDiscount(disc);
	 	             }
 	             mline.setStartDate(firstDay.toString());
 	             mline.setEndDate(lastDay.toString());
 	             boolean goes = true;
 	            if(prodPrices.get(toode) != null) {
 	            	String pr  = (String) prodPrices.get(toode);
 	            	if(pr.equals(hh)) {
 	            	   goes = false;
 	            	}
 	            }
 	             
 	             String signa = klient+toode+hh;
 	             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
 	            	 if(!check.contains(signa) && goes) {
 	            		    result.add(mline); 
 	            		 }
 	 	                 check.add(signa);
 	            	 }    
 	               }
  	            
	       
	       
	       for (String[] row : readCSVData("priceslists.csv")) {
  	       	 int cou = 0;
  	         String klient = null; 
  	      	 String toode = null;
  	       	 double hind = 0 ;
  	       	 String hh = null;
  	       
  	       	   for (String cell : row) {
  	           	 if(cou == 0) {
  	                     klient = cell.trim().replace(':', '_');
  	                }
  	           	 if(cou == 1) {
  	                    toode = cell.trim();
  	                }
  	           	 if(cou == 2) {
  	                    hind = Double.parseDouble(cell.trim());
  	                    hh = cell.trim();
  	                }
  	                cou++;
  	       	   }
  	              MatrixLine mline = new MatrixLine();

 	             if (klient != null && klient.length() > 0) {
 	               mline.setCustID(klient);
 	             }
 	             

 	              if (toode != null && toode.length() > 0) {
 	               mline.setProdID(toode);
 	             }
 	              
 	             if (hind > 0) {
 	 	               mline.setPrice(hind);
 	 	             }
 	             
 	             boolean goes = true;
 	            if(prodPrices.get(toode) != null) {
 	            	String pr  = (String) prodPrices.get(toode);
 	            	if(pr.equals(hh)) {
 	            	   goes = false;
 	            	}
 	            }
 	             
 	             String signa = klient+toode+hh;
 	             if(mline.getPrice() > 0 || mline.getDiscount() > 0){
 	            	 if(!check.contains(signa) && goes) {
 	            		 mline.setProdLevel("pr");
 	            		    result.add(mline); 
 	            		 }
 	 	                 check.add(signa);
 	            	 }    
 	               }
  	            
	        		
	    return result;
	      }

   
   
  @Override
		public void PictureMaker() {
			makePictureCat();
			 System.exit(0);
			
		}
	 
  private void makePictureCat(){
	    
  	
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
//       	 Lõppes kustutamine 
		 
		 TreeMap prods = this.getProducts(null);

       StringBuffer buf = new StringBuffer();
       Iterator enume = prods.keySet().iterator();
  
       while (enume.hasNext()) {
  	    	String asi = (String)enume.next();
  	    	Product prod = (Product)prods.get(asi);
  	    	String toode = prod.getEAN().trim();  
  	    	String artikkel = prod.artikkel; 
  	 //   System.out.println("toode " + toode);
             if(toode.length() > 3){
              	
  	   	  res2 = new File (pictssou);
            ff2 = res2.list();
           
       	 for(int i = 0;ff2.length>i;i++){
           	if( ff2[i].startsWith(toode + "_") || ff2[i].startsWith(toode + " _") || ff2[i].startsWith(toode + " ") ){
           //  System.out.println("pilt olemas " + ff2[i]);
          	File in = new File ( pictssou + ff2[i]); 
            	if(in.isFile()){
            		
            		if(ff2[i].toLowerCase().endsWith(".png")) {
            			File inpda = new File ( pictspda + prod.getID()+ ".png");
                    	if(!inpda.exists()){
                        	Converter.shrinkAndCopyFile(in,inpda,400);
                	    if(buf.toString().indexOf(prod.getID()+".png;"  + inpda.length()) == -1){
                          		buf.append(prod.getID()+".png;"  + inpda.length() + "\n");
                          	}
                	    }  
            		}else {
            			File inpda = new File ( pictspda + prod.getID()+ ".jpg");
                    	if(!inpda.exists()){
                        	Converter.shrinkAndCopyFile(in,inpda,400);
                	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                          		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
                          	}
                	    }  
            		}
            		
            	
            	
            	}
               }
             }
           }
             
             if(artikkel != null && artikkel.length() > 2){
               	
         	   	  res2 = new File (pictssou);
                   ff2 = res2.list();
                  
              	 for(int i = 0;ff2.length>i;i++){
                  	if( ff2[i].startsWith(artikkel + "_") || ff2[i].startsWith(artikkel + " _") || ff2[i].startsWith(artikkel + " ")){
                  //  System.out.println("pilt olemas " + ff2[i]);
                 	File in = new File ( pictssou + ff2[i]); 
                   	if(in.isFile()){
                   		
                   		if(ff2[i].toLowerCase().endsWith(".png")) {
                   			File inpda = new File ( pictspda + prod.getID()+ ".png");
                           	if(!inpda.exists()){
                               	Converter.shrinkAndCopyFile(in,inpda,400);
                       	    if(buf.toString().indexOf(prod.getID()+".png;"  + inpda.length()) == -1){
                                 		buf.append(prod.getID()+".png;"  + inpda.length() + "\n");
                                 	}
                       	    }  
                   		}else {
                   			File inpda = new File ( pictspda + prod.getID()+ ".jpg");
                           	if(!inpda.exists()){
                               	Converter.shrinkAndCopyFile(in,inpda,400);
                       	    if(buf.toString().indexOf(prod.getID()+".jpg;"  + inpda.length()) == -1){
                                 		buf.append(prod.getID()+".jpg;"  + inpda.length() + "\n");
                                 	}
                       	    }  
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

    	   StringBuffer products = new StringBuffer();
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

if(seis > 0) {
//	System.out.println("Seisx "+ id +" " + seisx);
 products.append(toode +  "; ;");
 products.append(getParent().getNF().format(seis) + "; "  +  "\n");
}
    	           	  
    	        }
       return products.toString();
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
                 name = Converter.enCodeEst(cell.trim());
             }
             cou++;
         }
         if(id.trim().length() > 1) {
         if(id0 != null && id0.trim().length() == 0) {
            tm.put(id, name);	
         } else {
        	 treeGr2.put(id0+id, name);
        	 treeGr.put(id, id0+id);
         }
         }
         
     }
         return tm;
   }

   private TreeMap treeGr2 = null;
   private TreeMap treeGr = null;
   
   public TreeMap getProdGroupsSecond() throws SQLException {
	  return treeGr2;    
  }

   
   public List<String[]> readCSVData(String fname) {
       String csvFile = getDir() + fname; // Adjust getDir() accordingly
       CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
       List<String[]> data = new ArrayList<>();

       try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile))
               .withCSVParser(parser)
               .build()) {
           String[] nextLine;
           while ((nextLine = reader.readNext()) != null) {
               data.add(nextLine); // Process each line without loading everything at once
           }
       } catch (IOException | CsvException e) {
           e.printStackTrace();
       }
       return data;
   }


   private List<String[]> readCSVDataOLD(String fname){
	  
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
                            custid = cell.trim().replace(':', '_');
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
          clGroups = new HashMap();
       
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
	    	              		custID = custID.replace(':', '_');
;	    	              		
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
        	 String name2 = null;
        	 String address = null;
        	 String contact = null;
        	 String group = null;
        	 String group2 = null;
        	 
             for (String cell : row) {
            	 if(cou == 0) {
                      id = cell.trim().replace(':', '_');;
                 }
            	 
            	 if(cou == 1) {
                     name = cell.trim();
                     name = name.replaceAll("'", " ").trim();
                     name = name.replaceAll("  ", " ");
                     name2 = Converter.cleanLitLatEst(name);
                 }
            	 
            	 if(cou == 2) {
                     address = cell.trim();
                 }
            	 
            	 if(cou == 4) {
                     contact = cell.trim();
                 }
            	 if(cou == 12) {
                     group2 = cell.trim();
                 }

            	 if(cou == 13) {
                     group = cell.trim();
                 }

            
            	 
                 cou++;
             }
             Customer  cust = new Customer(id);
             cust.setName( Converter.enCodeEst(name) + " " + Converter.enCodeEst(address));
             cust.setAddress( Converter.adressLink(Converter.enCodeEst(address), ""));
             cust.restStreet = address;
             cust.restPerson = contact;
             cust.setPersonalData( Converter.enCodeEst(contact));
             String setg = "";
             if(group != null && group.trim().length() > 0 ) {
            	 // System.out.println(id + " grupp " + group);
                 cust.setGroup(group);
                 setg = group+ "|";
             }
             if(group2 != null && group2.trim().length() > 0 ) {
             	//  System.out.println(id + " grupp2 " + group2);
            	 
                  cust.setGroup(setg + group2);
              }
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
             
             
             tm.put(name2 + cust.getAddress(), cust);	
             
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return tm;
   }

private Hashtable prodNames = null;

private HashMap prodPrices = null;   

   public TreeMap getProducts(Agent agent){
	   
	   if(cps ==  null) {
		   getCustomerProductsList2();
	   }
     TreeMap tm = new TreeMap();
     try {
    	   this.getProdGroups();
    	    	 
        TreeMap prods = new TreeMap(); 
        prodPrices = new HashMap();
        
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
            
        HashSet yesGroups = new HashSet();
        if(getParent().getProps().getProperty("yesgroups") != null){
        	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("yesgroups"),",");
        	while (st.hasMoreElements()){
        		String aa = st.nextToken();
        		yesGroups.add(aa);
        	}
        }
        
        HashSet noSubGroups = new HashSet();
        if(getParent().getProps().getProperty("noSubGroups") != null){
        	StringTokenizer st = new StringTokenizer(getParent().getProps().getProperty("noSubGroups"),",");
        	while (st.hasMoreElements()){
        		String aa = st.nextToken();
        		noSubGroups.add(aa);
        	}
        }
        
    
System.out.println(yesGroups.size() + " yesGroups");
           for (String[] row : readCSVData("products.csv")) {
        	   
    	   int cou = 0;
         String id = null;
      	 String name = null;
      	 String unit = null;
      	 String ean = null;
      	 String artikkel = null;
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
                  name = cell.trim().replaceAll("'", " ").trim().replaceAll("  ", " ");
             }
         	 if(cou == 13) {
                price = Double.parseDouble(cell.trim());
                prodPrices.put(id, cell.trim());
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
         	if(cou == 17) {
                artikkel = cell.trim();
             }
              cou++;
          }
         
         
         Product prod = new Product(id);
         boolean goes = false;
         

         if(gr2 != null && treeGr.get(gr2) != null) {
        	  prod.setGroup((treeGr.get(gr2)+"").substring(0,4));
        	  prod.setGroupSecond(treeGr.get(gr2)+"");
        	  
         }
         
         if(prods.get(id)!= null){
         	prod.setStock( Double.parseDouble((String)prods.get(id)));
         	 if(yesGroups.contains(prod.getGroup())) {
      		   goes = true;
      	    }
         	
         }
         
         if(noSubGroups.contains(prod.getGroupSecond())) {
     		   goes = false;
     	    }
             prod.setPrice(price);
             prod.setVat("21");
             
              if(unit != null) {
            	 prod.setUnit(unit);	 
              }
             
            

              String e4 = "";
             if(ean != null && ean.trim().length() > 4) {
               prod.setEAN(ean);
               e4 = " " +ean.substring(ean.length()-4);
             }
             
             if(inCase > 0) {
         	   prod.setInCases(inCase);
             }
             
             String name2 = Converter.cleanLitLatEst(name);
             prod.setName(Converter.enCodeEst(name) + " " + artikkel + e4);
           //  
          
             prod.artikkel = artikkel;
            
             if(usedProds.contains(id)) {
            	 if(yesGroups.contains(prod.getGroup())) {
            		   goes = true;
            	   }
             }
         	//  System.out.println(prod.getID() + " " + prod.getName() + " " + prod.getGroup() +"|"+ prod.getGroupSecond() + " " + prod.getUnit() + " " + prod.getPrice());

             // producti 0126
             String sorting = "aa";

             //alkogol 0007
             if(prod.getGroup().equals("0007")) {
            	 sorting = "bb";
             }
             //nonfood 0327

             if(prod.getGroup().equals("0327")) {
            	 sorting = "cc";
             }
             //tabaka 0060

             if(prod.getGroup().equals("0060")) {
            	 sorting = "dd";
             }
             //e.actise 6040

             if(prod.getGroup().equals("6040")) {
            	 sorting = "ee";
             }
             if(prod.getGroup().equals("0076")) {
            	 sorting = "ff";
             } 
             prod.setNameSorting(sorting + name);
               if(goes) {
            	   System.out.println( prod.getGroup() +" "+ sorting + name);
            	   tm.put(sorting+name2, prod); 
               }
               
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

   
  private  HashSet usedProds = null;
  private Vector cps = null;

  public Vector getCustomerProductsList(String agentCode){
	  return cps;
  }
   public void getCustomerProductsList2(){
		Calendar c = Calendar.getInstance();
   		c.add(Calendar.MONTH, -6);
   		Date start = new Date(c.getTimeInMillis());   
   		cps = new Vector();
   		usedProds = new HashSet();

//"PAŽ224170";"TR0010147";"LT1000243:107548";"4";"gab.";"2021-03-19";"0.89";
//"PAŽ224170";"TR0011922";"LT1000243:107548";"6";"gab.";"2021-03-19";"0.89";
     Vector vec = new Vector();
    
         for (String[] row : readCSVData("deliveries.csv")) {
      	   
  	   int cou = 0;
       String custID = null;
    	 String prodID = null;
    	 String doc = null;
    	 String lastdate = null;
       double price = 0;
       double amount = 0;
      

   	   for (String cell : row) {
   		if(cou == 0) {
            doc = cell.trim();
         }
   		if(cou == 1) {
            prodID = cell.trim();
         }
   		if(cou == 2) {
            custID = cell.trim().replace(':', '_');;
         }
   		if(cou == 3) {
            amount = Double.parseDouble(cell.trim());
         }
   		if(cou == 5) {
            lastdate = cell.trim();
         }
   		if(cou == 6) {
            price = Double.parseDouble(cell.trim());
         }

   		cou++;
   	   }      
   		// paneme kaasa siis kui on kuni 6 kuud
   		if(lastdate != null) {
   		Date dd = this.getDate(lastdate);
   

   		if(dd.after(start)) {
                    if(lastdate.length()>4){
                    }else{
                    lastdate =" ";
                    }
                    
                 CustProd cp = new CustProd(custID, prodID,amount , 0, 0,lastdate.substring(5),price,0,0," ");
                 cp.setAbiKood(Converter.cleanLitLatEst(doc));
          
                 cps.addElement(cp);
                 if(!usedProds.contains(prodID)) {
                	 usedProds.add(prodID);
                 }
   		}
         }}


     
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "S1Tabak";
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

  /*	 try {
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
      }*/
  }

public Vector getFixedAssortmentData() {
	Vector lines = new Vector();
	return lines;

}

@Override
public ManagerData getManagerData() throws SQLException {
	// TODO Auto-generated method stub
	return null;
}

}
