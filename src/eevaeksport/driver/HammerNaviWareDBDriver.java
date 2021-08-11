package eevaeksport.driver;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
import eevaeksport.Email;
import eevaeksport.InventoryLine;
import eevaeksport.MatrixLine;
import eevaeksport.Product;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HammerNaviWareDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public HammerNaviWareDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }

   public String getPilotDayData(Agent agent){
     return null;
   }

   public Vector getPriceMatrixData() {
   	return null;
   }
   
   
   private Connection getNewConnection() throws SQLException{
    return DriverManager.getConnection(url, getParent().getProps().getProperty("dsn.user"), getParent().getProps().getProperty("dsn.pass"));
  }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public void insertDefective(DefectiveWrapper wr){
    String sql = "INSERT INTO dbo.MJ_QUALITYEST VALUES (?,?,?,?,?,?,?,?,?,?,getdate(),?,' ',' ',0,0)";
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
          } catch (NumberFormatException nfe){nfe.printStackTrace();};
          ps.setDouble(7, kogus);
          String bb = lw.getLine().getBestBefore();
          System.out.println(kogus+"bb '"+lw.getLine().getBestBefore()+"'"+bb+"'");
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

   public String getProductSortingData(){
   	return null;
   }
   
 

 

   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           try {

                   FileReader fr = new FileReader(getDir() + DATA_FILE);
                   BufferedReader reader = new BufferedReader(fr);
                 String line = null;

                 while(!(line = reader.readLine()).equals("[Get Group Assortiment]")){
                 }
                 line = reader.readLine();

                 while(((line = reader.readLine()).trim().length()) > 0 ){
                   line = line.replaceAll("\",\"", "|");
                  // System.out.println(line);
                   StringTokenizer st = new StringTokenizer(line, SEPARATOR);

                   String custID = " ";
                   String clGroup = " ";
                   String prodID = " ";
                   String prodGroup = " ";
                   MatrixLine mline = new MatrixLine();

                   if (st.hasMoreTokens()) {
                   	clGroup = clear(st.nextToken());
                   }
                   if (st.hasMoreTokens()) {
                     prodID = clear(st.nextToken());
                   }


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


                         } catch (Exception e) {
                             getParent().showError(e);
                         }


           return lines;
   }

   public DealerProduct getDealerProductsList() {
    return null;
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
    
        return tm;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
 
   	return null;
  }


 

   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // getDoknr("W");
     
   //  getDealerProductsList();
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()).startsWith("[Get Agents]")){
     }
      while(((line = reader.readLine()).trim().length()) > 0 ){

           line = line.replaceAll("\",\"","|");
      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

        if (st.hasMoreTokens()) {
          String id = clear(clear(st.nextToken()));
          Agent agent = new Agent(id);
          if (st.hasMoreTokens()) {
            String name = clear(st.nextToken());
            if (name.equals("")){ name = "<" + id + ">";}
            agent.setName(name);
              agent.setObject("TALLINNA L");
           
             if(id.equals("MARKOM")){
            	 agent.setObject("P�RNU LADU");
             }
            
		       	

				String agentAGENTS = getParent().getProps().getProperty("dub." +agent.getID());
				if(agentAGENTS != null){
      	           StringTokenizer stag = new StringTokenizer(agentAGENTS, ",");
			    while(stag.hasMoreTokens()){  	
                 agent.addDubbler(stag.nextToken());
			    }

				}
        	agents1.put(agent.getID(), agent);
            tm.put(agent.getName(), agent);
          }
        }
      }
     Agent aaa = new Agent ("LADU");
     aaa.setName("Ladu Hammer");
      agents1.put("LADU", aaa);
      tm.put(aaa.getName(), aaa);

 //Loe sisse kliendi pysi allalhindlused
      FileReader fr1 = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader1 = new BufferedReader(fr1);
    String line1 = null;

    while(!(line1 = reader1.readLine()).equals("[Get Customer Discount]")){
    }
    line1=reader1.readLine();
    while(((line1 = reader1.readLine()).trim().length()) > 0 ){
        line1 = line1.replaceAll("\",\"","| ");
        StringTokenizer st1 = new StringTokenizer(line1, SEPARATOR);
         String id = clear(st1.nextToken());
         String value = clear(st1.nextToken());
         discounts.put(id,value);
    }

    FileReader fr2 = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader2 = new BufferedReader(fr2);
  String line2 = null;

  while(!(line2 = reader2.readLine()).equals("[Get Payer Overdue]")){
  }
  line2=reader2.readLine();
  while(((line2 = reader2.readLine()).trim().length()) > 0 ){
      line2 = line2.replaceAll("\",\"","| ");
      StringTokenizer st2 = new StringTokenizer(line2, SEPARATOR);
      //System.out.println(line2);
       String id = clear(st2.nextToken());
       Debt debt = new Debt ();
       debt.setDoknr(clear(st2.nextToken()));
       if(st2.hasMoreTokens()){
       debt.setDue(clear(st2.nextToken()));
       }
       overdebts.put(id,debt);
  }





     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }




       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){

     
     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

    Customer customer = new Customer("LADU");
    customer.setName("LADU");
    customer.addAgent(new Agent("LADU"));
    
       
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());

     System.out.println("kliendid tehtud");
     
     return tm;
   }
    
    
   public String getSaveStockInfo(){
	 
	  
   	
   	
     StringBuffer products = new StringBuffer();
     try {

           FileReader fr = new FileReader(getDir() + DATA_2_FILE);
           BufferedReader reader = new BufferedReader(fr);
         String line = null;
         line = reader.readLine();
         line = reader.readLine();
         if(line!=null){

        

          while((line = reader.readLine())!= null ){
               line = line.replaceAll("\",\"","|");
              // System.out.println(line);
          	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
          	//   	           	"toode","Stock","warehouse"

           String toode = null;
           double seis = 0;;
           String ladu  = null;
          	if (st.hasMoreTokens()) {
            	toode = clear(st.nextToken());
            }
          	if (st.hasMoreTokens()) {
            	seis = Double.parseDouble(clear(st.nextToken()));
            }
          	if (st.hasMoreTokens()) {
            	ladu = clear(st.nextToken());
            }
             
          	
          	if((ladu.equals("TALLINNA L"))
          			&& seis > 0){
          		
          		 if(seis > 0){
                    products.append(toode +  ";");
                    products.append(" ;");
                    products.append(getParent().getNF().format(seis) + "; "  +  "\n"); // + dealer +  "\n");
                   }
                }    
             }
          }
         
     } catch (Exception e) {
         getParent().showError(e);
     }
     return products.toString();
 }


   public TreeMap getProducts(Agent agent){
	   System.out.println("tooted alustan");
	 // this.getCustomerProductsList(null);
	   System.out.println("custprodtehtud alustan");
     TreeMap tm = new TreeMap();
     TreeMap sto = new TreeMap();
   

 	/*
    try { 
        FileReader fr = new FileReader(getDir() + DATA_FILE);
        BufferedReader reader = new BufferedReader(fr);
      String line = null;

      while(!(line = reader.readLine()).equals("[Get stock]")){
      }
      if(line!=null){

      line = reader.readLine();

       while(((line = reader.readLine()).trim().length()) > 0 ){
            line = line.replaceAll("\",\"","|");
            //System.out.println(line);
       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       	//   	           	"toode","Stock","warehouse"

        String toode = null;
        double seis = 0;;
        String ladu  = null;
       	if (st.hasMoreTokens()) {
         	toode = clear(st.nextToken());
         }
       	try{
       		if (st.hasMoreTokens()) {
             	seis = Double.parseDouble(clear(st.nextToken()));
             }
       	}catch (Exception e){
       		e.printStackTrace();
       	}
       	
       	if (st.hasMoreTokens()) {
         	ladu = clear(st.nextToken());
         }
       	if(ladu != null &&ladu.equals("TALLINNA L") && seis > 0){
           //	if((ladu.equals("ABT LADU") || ladu.equals("HULGI")) && seis > 0){
       		if(sto.containsKey(toode)){
       			double seiss = seis + Double.parseDouble((String)sto.get(toode));
       			sto.put(toode,seiss+"");
       		}else{
       			sto.put(toode,seis+"");   
       		}
       //	 System.out.println(ladu+" "+toode+" "+seis+"");
       	}
  
           
        if(ladu != null && ladu.indexOf("RNU LADU")>-1 && seis > 0){
            //	if((ladu.equals("ABT LADU") || ladu.equals("HULGI")) && seis > 0){
        		if(par.containsKey(toode)){
        			double seiss = seis + Double.parseDouble((String)par.get(toode));
        			par.put(toode,seiss+"");
        		}else{
        			par.put(toode,seis+"");   
        		}
        	}
       }}
 
       
    

} catch (Exception e) {
    getParent().showError(e);
}
*/
     
     
     
     
     
     
System.out.println("laoseisud tehtud");
Hashtable dateOut = new Hashtable();
StringBuffer buf = new StringBuffer();

     
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     while(!(line = reader.readLine()).equals("[Get Products]")){
     }
     line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
     	
     	line = line.replaceAll("\",\",\"","| ");
		line = line.replaceAll("\",\"","| ");
		String name = "";
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = clear(st.nextToken());
         if(id.trim().length()>0){
         	        
         Product prod = new Product(id);
     	     
         if (st.hasMoreTokens()){
         	name = clear(st.nextToken());
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
         
           if (!st.hasMoreTokens()){
           	line = line + reader.readLine();
           	line = line.replaceAll("\",\",\"","| ");
    		line = line.replaceAll("\",\"","| ");
           	st  = new StringTokenizer(line, SEPARATOR);
           	st.nextToken();
           	st.nextToken();
        	System.out.println(line);

           }
           	st.nextToken();
             
               if (st.hasMoreTokens()){
               	st.nextToken();
                prod.setGroup(id.substring(0,2));
                if (st.hasMoreTokens()){
                  st.nextToken();
                    }
                     if (st.hasMoreTokens()){
                       st.nextToken();
                       if (st.hasMoreTokens()){
                         String ean = clear(st.nextToken());
                         if (ean.trim().length()>0){
                           prod.setEAN(ean.trim());
                           if (ean.trim().length()>13){
                           	prod.setEAN(ean.substring(0,13));
                           }
                         }
                    
                 }
               }
             }
               
           String abinimi = "";
            int cou = 1;
            while (st.hasMoreTokens()){
           
            	String tok = st.nextToken();
            	cou++;
            	if(cou == 19 ){
            	   abinimi = tok;
            	}
            }
         
            if(abinimi.trim().length() != 0){
            	name =  name + abinimi.substring(0,1) + (abinimi.substring(1,abinimi.length())).toLowerCase();
            }
           prod.setName(name);      
         
         boolean goes = true ;
         if(!prod.getID().startsWith("89")){
        	// goes = false;
         }
       
         if(prod.getName()!= null && prod.getName().trim().length()>3 && goes){	
        
        	 
             if(prod.getEAN().trim().length()==0){
             	 dateOut.put(id.trim(),id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
             	 buf.append(id.trim() + ";"+ id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
             }else{
             	 dateOut.put(prod.getEAN(),id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
             	 buf.append(prod.getEAN() + ";"+ id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");
             	 if(prod.getEanCase().trim().length() >0){
             		 dateOut.put(prod.getEanCase(),id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock());
                 	 buf.append(prod.getEanCase() + ";"+ id.trim() + "|" + deCode (name.trim()) + "|" + prod.getStock() + "\n");	 
             	 }
        	 
        	// tm.put(prod.getName(), prod);	           	
                }	
              }
            }

     }
     }}} catch (IOException e){
       e.printStackTrace();
     }
     
     if(buf.length() > 2){
         File f = new File("tootedRev.txt");
         if (f.exists())f.delete();
           try {
     		PrintWriter os = new PrintWriter(new FileWriter(
     				"tootedRev.txt", true));
     		os.write(buf.toString());
     		os.close();
     	  }catch (IOException ex) {
     		ex.printStackTrace();
     	  }
      }
     
     
     return tm;
   }
     
     
     public static String deCode(String res ){
       	return res;
       }
     

     
     public String getSaveDebtInfo() {
     	
     
     	
    
     	
     	        File dir = new File("import");
     	        File[] files = dir.listFiles();
     	        if(files != null){
     	        
     	        for (int i = 0; i < files.length ; i++){
     	          File file = files[i];
     	          if (file.isFile()){
     	            Vector vec = null;
     	            try {
     	            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
     	           
     				
     					vec = (Vector)ois.readObject();
     					ois.close();
     				} catch (IOException e) {
     					
     					e.printStackTrace();
     				} catch (ClassNotFoundException e) {
     					e.printStackTrace();
     				}
     				insertInventory(vec);
     	             File done = new File("import/done/" + file.getName());
     	             if(done.exists()){
     	             	done.delete();
     	             }
     	            file.renameTo(done);
     	             file.delete();
     	            
     	          }    
     	        }	
     	        }
     	        System.exit(0);
     	    	
     	    	return null;
     	       }
     
     private void insertInventory(Vector vec){
 	  	if(vec != null){ 
 	  		StringBuffer buf = new StringBuffer();
 			Vector tehtud = null;
 			File file = new File ("loetudid.vec");
 			if(file.exists()){
 			
 	           try {
 	           ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
 					tehtud = (Vector)ois.readObject();
 					ois.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 				
 			}else{
 				tehtud = new Vector();
 			}
 			String doknr = "";
 			boolean first = true;
 			int linecount = 10000;
 			TreeMap koond = new TreeMap();
 	  	
 	  		Enumeration enums = vec.elements();
 	  		while(enums.hasMoreElements()){
 	  			InventoryLine lin = (InventoryLine)enums.nextElement();
 	  			
 	  			String objekt = lin.getCustId();
 	  			if(objekt == null){
 	  				objekt = "";
 	  			}
 	  			String location = lin.getLocation();
 	  			if(location == null){
 	  				location = "";
 	  			}
 	  			String toLocation = lin.getToLocation();
 	  			if(toLocation == null){
 	  				toLocation = "";
 	  			}
 	  			String comment = lin.getComment();
 	  			if(comment == null){
 	  				comment = "";
 	  			}
 	  			String warehouse = lin.getWarehouse();
 	  			if(warehouse == null){
 	  				warehouse = "";
 	  			}
 	  			String toWarehouse = lin.getToWarehouse();
 	  			if(toWarehouse == null){
 	  				toWarehouse = "";
 	  			}
 	  			double kogus  = lin.getAmount();
 	  			String ean = lin.getEan();
 	  			if(ean == null){
 	  				ean = "";
 	  			}
 	  			String prName = lin.getProdName();
 	  			if(prName == null){
 	  				prName = "";
 	  			}
 	  			String wrk = lin.getWorker();
 	  			if(wrk == null){
 	  				wrk = "";
 	  			}
 	  			String wrkname = lin.getWorkername();
 	  			if(wrkname == null){
 	  				wrkname = "";
 	  			}
 	  			String prodid = lin.getProdId();
 	  			if(prodid == null){
 	  				prodid = "";
 	  			}
 	  			String partii = lin.getPartii();
 	  			if(partii == null){
 	  				partii = "";
 	  			}
 	  			String lineType = lin.getLineType();
 	  			if(lineType == null){
 	  				lineType = "";
 	  			}
 	  			
 	  			String lineid = ean + "" + lin.getEnterTime().getTime();
 	  			java.sql.Date entered = new java.sql.Date(lin.getEnterTime().getTime());
 	  		
 	  			if(lin.isTrouble() && !tehtud.contains(lineid)){
 	  				
 	  				
 	  	  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
 	  	  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() + "'" +warehouse + "'" +toWarehouse + "-" + partii );
 	  	  		 
 	  	  			try{
 	  	  			Email mail = new Email(null);
 	   	  		   
 	  	  	  		mail.setSMTP(parent.getProps().getProperty("smtphost"));
 	  	  	  		mail.setFilesTo(parent.getProps().getProperty("troubleemail"));
 	  	  	  		mail.setFrom(parent.getProps().getProperty("from"));
 	  	  	  		mail.setSubjectFilesTo("Kriitilised Probleemid laost toode " + prName);
 	  	  	  		
 	  	  	  		mail.setBody("\nLaoseisu KriitilineProbleemiraport\n\n" +
 	  	  	  				  "\nLadu: " + warehouse +
 	  	  	  				"\nAsukoht kust : " + location +
 	  	  	  				"\nAsukoht kuhu : " + toLocation +
 	  	  	  				"\nToode  :    " + prodid +
 	  	  	  			    "\nTootenimi : " + prName +
 	  	  	  			    "\nTootekogus : " + kogus +
 	  	  	  			    "\nToote partii : " + partii +
 	  	  	  				"\nEAN : " + ean +
 	  	  	  				"\nT��taja : " + wrkname +
 	  	  	  				"\nFikseerimiseaeg : " +  entered.toString() );
 	  	  	  		mail.setSendNotice(false);
 	  	  	  		mail.send();
 	  	  				
 	  	  			}catch(Exception e){
 	  	  			e.printStackTrace();	
 	  	  			}
 	  	  				
 	  			}
 	  			Calendar cal = Calendar.getInstance();
 	  			cal.setTimeInMillis(lin.getEnterTime().getTime());
 	  			String tund = cal.get(Calendar.HOUR_OF_DAY)+"";
 	  			if (tund.length() == 1){
 	  				tund = "0" + tund;
 	  			}
 	  			String minut = cal.get(Calendar.MINUTE)+"";
 					if (minut.length() == 1){
 						minut = "0" + minut;
 	  			}
 					
 					
 					
 					if(!tehtud.contains(lineid)){
 					
 	  			 try{
 	  			  
 	  	
 	  			
 	  			System.out.println(objekt + "'" + location+ "'" + toLocation + "'" + kogus + "'" +  prodid + "'" + 
 	  					ean + "'" + prName+ "'" + wrk + "'" + wrkname+ "'" + entered.toString() +
 	  					"'" +warehouse + "'" +toWarehouse + "-" + partii );
 	  		 
 	  			// Siin tuleb lisada kolm varianti inentuur ja rida ja p�is.
 	  			 
 	  			if(lineType.equals("INVENTORY")){
 	  				//inventuuri rida
                    	}	
 	  				
 	  				
 	  			
 	  			if(lineType.equals("MOVE")){
 	  				//laoliikumine rida
 	  			}
 	  			
 	  			if(lineType.equals("RECEIVEING")){
 	  			    
 	  				if(first){
 	  					doknr = getDoknr("W");
 	  					first = false;
 	  				}
 	  				if(koond.get(prodid) != null ){
 	  					int kog = Integer.parseInt((String)koond.get(prodid));
 	  					kog = kog +(int)kogus;
 	  					koond.put(prodid, kog +"");
 	  				}else{
 	  					koond.put(prodid, (int)kogus +"");
 	  				}
 	  				
 	  				
 	  				
 	  				
 	  			
                System.out.println("DokNr  " +  doknr);
 	  			}
 	  			 System.out.println("Line type  " +  lineType);
 	  			
 	  			if(lineType.equals("HEADERDONE")){
 	  				//P�is tehtud rida
 	  				
 	  			}
 	  			

 	  			if(lineType.equals("LINECHANGE")){
 	  	
 	  			}
 	  	 
 	            tehtud.add(lineid);
 				 try{

 	   			 saveObject(tehtud, "loetudid.vec"); 
 				 }catch(Exception e){
 					 e.printStackTrace();
 				 }
 	   		      	
 	   		      //  con.close();
 	   		        
 	  			 }catch (Exception e){
 	  			 	e.printStackTrace();
 	  			 }
 	  		
 	  			
 					}
 	  		}
 	  		if(koond.size() != 0){
 	  		Iterator itera = 	koond.keySet().iterator();
 	  			while (itera.hasNext()){
 	  				String id = (String)itera.next();
 	  				String kog = (String)koond.get(id);
 	  				buf.append(doknr + "\tKOMPLEKT\t" + linecount + "\t" + id + "\t" + getDate(0) +"\t\tTALLINNA L\t" +
 	 	  					(kog) + "\tKOMPLEKT\t\tPAKK" + lineSep );
 	 	  			linecount = linecount + 10000;   
 	  			}
 	  			         
 	  		}
 	  		
 	  	   
 	 	     if(buf.length() > 2){
 	 	         File f = new File(getOutDir() + doknr +".txt");
 	 	         if (f.exists())f.delete();
 	 	           try {
 	 	     		PrintWriter os = new PrintWriter(new FileWriter(
 	 	     				getOutDir() + doknr + ".txt", true));
 	 	     		os.write(buf.toString());
 	 	     		os.close();
 	 	     	   }catch (IOException ex) {
 	 	     		ex.printStackTrace();
 	 	     	  }
 	 	      }
 	  	}
 	  
 	  	
 	  }
   private String getDate(long time){
	
	   Calendar dat = Calendar.getInstance();
	   if(time != 0){
		   dat.setTimeInMillis(time);   
	   }
	   
	    	 String yy = (dat.get(Calendar.YEAR))+"";

	    	  String mm = (dat.get(Calendar.MONTH)+1)+"";
	    	  if(mm.length()== 1){
	    	  	mm = "0"+mm;
	    	  }
	    	  
	    	  
	    	  String dd = (dat.get(Calendar.DAY_OF_MONTH))+"";
	    	  if(dd.length()== 1){
	    	  	dd = "0" + dd;
	    	  }

		      
	    	  return  dd + "-" +  mm  + "-" + yy ;
	    	  
   }
 	  
     private String getDoknr(String serial){
    	 String res = "";
    	 Properties props = loadProps(getDir() + "numeraatorid.properties");
    	 if(props == null){
    		 props = new Properties();
    		 props.put(serial, "1");
    		 res = "1";
    	 }else{
    		 String value = props.getProperty(serial);
    		 if(value!= null){
    			 int num = Integer.parseInt(value);
    			 num++;
    			 res = num + "";
    			 props.put(serial, res);
    		 }else{
    			 props.put(serial, "1");
    			  res = "1";
    		 }
    	 }
    	 
    	 try {
			saveProps(props,getDir() +  "numeraatorid.properties", "Viimane salvestus: "+ serial);
		} catch (IOException e) {
			e.printStackTrace();
		}   	
		System.out.println("res " + res);
    	 return getNum(serial,res,6);
     }
 
     private String getNum(String serial, String value,int len){
    	 String res = value;
    	 int reslen = serial.length();
    	 int todo = len-reslen-value.length();
    	 while(todo != 0){
    		 res = "0" + res;
    		 todo--;
    	 }
    	 
    	 return serial + res;
     }
     
     
     void saveProps(Properties props, String filename, String header)
		throws IOException {
			FileOutputStream fos = new FileOutputStream(filename);
			props.save(fos, header);
			fos.close();
		}
     
 	

 	
	public Properties loadProps(String fileName) {
		Properties props = null;

		try {
			File file = new File(fileName);
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
					props = new Properties();
                	props.load(in);
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}
     

   public String getAssortmentStr(Agent agent){
    StringBuffer sb = new StringBuffer();

    try {
      FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
   while(!(line = reader.readLine()).equals("[Get Products]")){
    }
    line=reader.readLine();
    while(((line = reader.readLine()).trim().length()) > 0 ){
       line = line.replaceAll("\",\"","| ");

   // "toode","nimi","myygihind","grupp1",grupp2,"unit","ean","status","maxdiscount","tyypi kuuluvus"

      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
      if (st.hasMoreTokens()){
        String id = clear(st.nextToken());
        if (st.hasMoreTokens()){
           st.nextToken();
          if (st.hasMoreTokens()){
          	  st.nextToken();
              if (st.hasMoreTokens()){
               st.nextToken();
               if (st.hasMoreTokens()){
               	st.nextToken();
                   }
                    if (st.hasMoreTokens()){
                    	st.nextToken();
                    	if (st.hasMoreTokens()){
                    		st.nextToken();
                    			if (st.hasMoreTokens())
                    				{st.nextToken();
                    				if (st.hasMoreTokens()){
                    					st.nextToken();
                    					if (st.hasMoreTokens()){
                    						String type = clear(st.nextToken());
                    						//ABCD
                    						int i = type.trim().length();
                    						if(i>0){
                    						while (i != 0){
                    							sb.append(" ; ;" + type.substring(i-1,i) + ";" + id + "\n");
                    							i = i -1;
                    						}

                    						}

                            }
                    }}
                  }
                }
              }
            }
          }
        }
       }}


     catch (IOException e){
      e.printStackTrace();
    }
    return sb.toString();
  }


   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     return vec;
   }
   
	public static void saveObject(Object obj, String file) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(obj);
		oos.close();
	}
   
   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "HammerNaviWare";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
  private String clear (String ss){
  //	System.out.println(ss);
  	
    String ret =   ss.replace(',','.');
    ss.replace(';','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace('"',' ').trim();
    ret =  ret.replace('"',' ').trim();
    ret =  ret.replace('"',' ').trim();


    ret = ret.replaceAll("�","11111xx");

    ret = ret.replaceAll("�","11112xx");

    ret = ret.replaceAll("�","11113xx");


    ret = ret.replaceAll("�","11114xx");

    ret = ret.replaceAll("�","11115xx");

    ret = ret.replaceAll("�","11116xx");

    ret = ret.replaceAll("�","11117xx");

    ret = ret.replaceAll("�","11118xx");
    ret = ret.replaceAll("�","11119xx");
   
    ret = ret.replaceAll("�","11130xx");
    ret = ret.replaceAll("�","11120xx");
    ret = ret.replaceAll("�","11121xx");
    ret = ret.replaceAll("�","11122xx");
    ret = ret.replaceAll("�","11123xx");
    ret = ret.replaceAll("�","11124xx");
//O?
    ret = ret.replaceAll("�","11224xx");
    ret = ret.replaceAll("�","11225xx");
    ret = ret.replaceAll("�","11226xx");
    ret = ret.replaceAll("�","11227xx");
    ret = ret.replaceAll("�","11228xx");
    ret = ret.replaceAll("�","11229xx");
//�
    ret = ret.replaceAll("11224xx","�");
    ret = ret.replaceAll("11225xx","�");
    ret = ret.replaceAll("11226xx","�");
    ret = ret.replaceAll("11227xx","�");
    ret = ret.replaceAll("11228xx","�");
    ret = ret.replaceAll("11229xx","�");
    
    ret = ret.replaceAll("11130xx","�");
    ret = ret.replaceAll("11119xx","�");
    ret = ret.replaceAll("11120xx","�");
    ret = ret.replaceAll("11121xx","�");
    ret = ret.replaceAll("11122xx","�");
    ret = ret.replaceAll("11123xx","�");
    ret = ret.replaceAll("11124xx","�");
    ret = ret.replaceAll("11118xx","�");
    ret = ret.replaceAll("11119xx","�");
    ret = ret.replaceAll("11117xx","�");
    ret = ret.replaceAll("11116xx","�");
    ret = ret.replaceAll("11115xx","�");
    ret = ret.replaceAll("11114xx","�");
    ret = ret.replaceAll("11113xx","�");
    ret = ret.replaceAll("11111xx","�");
    ret = ret.replaceAll("11112xx","�");
    if(ret == null || ret.trim().length()==0){
    return " ";
    }else {
    return ret;
    }
  }
   public double getSalary(String agentCode){

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }
   private String getOutDir(){
	     String dir = getParent().getProps().getProperty("output");
	     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
	     return dir;
	   }


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "data.txt";
   public static final String GROUP_FILE = "grupid.txt";
   
   public static final String DATA_2_FILE = "laoseis.txt";
   private String subjectFilesTo = null;
   private String lineSep = System.getProperty("line.separator");
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private TreeMap agents1 = new TreeMap();
   
   private String url = "jdbc:odbc:report";


  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

  	 try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;


    while(!(line = reader.readLine()).equals("[Get Salary]")){
    }
    line=reader.readLine();
    while((line = reader.readLine())!= null &&  line.trim().length() > 0 ){
       line = line.replaceAll("\",\"","|");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
          double sales = Double.parseDouble(clear(st.nextToken()));
                if (st.hasMoreTokens()){
                 String agentID = clear(st.nextToken());
                 Agent agent = (Agent)agents.get(agentID);
                 if (agent != null){
                   agent.setMonthSales((int)(sales));
            }
          }
        }
    }
      } catch (IOException e){
        e.printStackTrace();
      }
  }
/**
 * @return Returns the agents1.
 */
public TreeMap getAgents1() {
	return agents1;
}
/**
 * @param agents1 The agents1 to set.
 */
public void setAgents1(TreeMap agents1) {
	this.agents1 = agents1;
}
}
