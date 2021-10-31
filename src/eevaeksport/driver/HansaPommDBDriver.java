package eevaeksport.driver;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.Debt;
import eevaeksport.EksportPanel;
import eevaeksport.Email;
import eevaeksport.MatrixLine;
import eevaeksport.Product;
import eevaeksport.util.Converter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HansaPommDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,PictureMaker, FixReporter{

  public HansaPommDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");

    
	   user =  getParent().getProps().getProperty("rest.user");
	    pass = getParent().getProps().getProperty("rest.pass");
	    rcompany = getParent().getProps().getProperty("rest.companyx");
	     }
	  String user =  null;
	  String pass = null;
	  String rcompany = null;

  public void connectionUp(){
      if (con == null){
	try {
           con = DriverManager.getConnection(url);
        
	} catch (Exception e) {
	    getParent().showError(e);
	}
      }
    }

   public String getPilotDayData(Agent agent){
     return null;
   }
	public void setBulkSales(Map agents) {

	}

       public String getSaveStockInfo(){
       return null;
       }

       
       public String getProductSortingData(){
       	return null;
       }

       
       public Vector getFixedAssortmentData() {
        return null;
      }

       

       private String getDateString(Calendar cal){
         StringBuffer sb = new StringBuffer();
         String year =   cal.get(Calendar.YEAR) + "";
         sb.append(year.substring(2,4));
         String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
         if (month.length() == 1){
           sb.append("0");
         }
         sb.append(month);

         String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
         if (day.length() == 1){
           sb.append("0");
         }
         sb.append(day);

         return sb.toString();
       }  
       
       
       
   	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
       public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         HashSet olemas = new HashSet();
         try{
        	 
     		
        	
             
                BufferedReader reader = new BufferedReader(
          			   new InputStreamReader(
          	                      new FileInputStream(getDir() + PRODUCT_PRICES_FILE), "UTF8"));
              String line = null;
            
              
              while((line = reader.readLine()) != null){
            	  
            	  if(line.indexOf("reb2") != -1){
            		  System.out.println("sees " + line);
            		  while((line = reader.readLine()) != null && !line.startsWith("pldef2") ){
            			//  System.out.println("V�ljas2 " + line.length() + " " + line);
            			   
            			  if(line.length() > 140){
            				  line = line.replaceAll("\t","| ");
            				//  System.out.println(line);
            				  StringTokenizer st2 = new StringTokenizer(line, "|");
            				  for(int i =0 ;  i<19 ; i++){
            					  st2.nextToken();
            				  }
            				  String kuupa =st2.nextToken().trim(); 
            				  String kuupl =st2.nextToken().trim(); 
            				  String ha = st2.nextToken().trim();
            				  if (ha.endsWith("1")) {
            					  ha = ha.substring(0,ha.length()-1);
            				  }
            				  
            				  System.out.println(kuupa + kuupl + ha);
            				  while((line = reader.readLine()) != null && line.length() > 0 
            						  && line.length() < 25  ){
            					  line = line.replaceAll("\t","| "); 
                        			
                       			 StringTokenizer st = new StringTokenizer(line, "|");
                       			 
                       			 
                       			MatrixLine mline = new MatrixLine();
                          		
                    			  mline.setProdID(st.nextToken().trim());
                    			 
                    			  mline.setCliGroup(ha);
                               
                                double price = 0;
                                st.nextToken();
                             try{
                            	 Date alg = null;
                            	  Date lopp = null;
                            	 try{
                            		 alg = getDate(kuupa);
                            		 lopp = getDate(kuupl);
                            	 }catch(Exception e) {
                            		 
                            	 }
                            	
                            	 
                               
                            	 String dii = st.nextToken();
                                
                                 java.util.Date today = Calendar.getInstance().getTime();
                            //   System.out.println(mline.getProdID() + " " + dii);
                                
                                 if((alg == null  && lopp == null )) {
                                	
                          	       price = Double.parseDouble(dii.replace(',', '.').replaceAll(" ", "").replaceAll("%", "").replaceAll("_", ".").replaceAll("K", " ").trim());
                          	       mline.setDiscount(price);
                          	 	   mline.setStartDate("kamp");
                      	 	   
                          	   if(!olemas.contains(mline.getStringForFixedAssortment())){
                                 	 lines.add(mline);
                                 	 olemas.add(mline.getStringForFixedAssortment());
                              //	 System.out.println(mline.getCliGroup() + " " + mline.getProdID() + " " + mline.getPrice());
                                   
                          	   }
                                	 
                                 } else   if(  alg.before(today) && lopp.after(today)){
                            	 
                        	       price = Double.parseDouble(dii.replace(',', '.').replaceAll(" ", "").replaceAll("%", "").replaceAll("_", ".").replaceAll("K", " ").trim());
                        	 	 
                        	       mline.setStartDate(kuupa);
                        	 	   mline.setEndDate(kuupl);
                        	 	   mline.setDiscount(price);
                        	 	 
                        	   if(!olemas.contains(mline.getStringForFixedAssortment())){
                               	 lines.add(mline);
                               	 olemas.add(mline.getStringForFixedAssortment());
                            	// System.out.println(mline.getCliGroup() + " " + mline.getProdID() + " " + mline.getPrice());
                                 
                        	   }
                            }
                          	    
                          			
                                 
                             }catch(Exception e){
                            	 //e.printStackTrace();
                          	 }
                             
                               
                           
                       			 
            					//  System.out.println("V�ljas3 " + line.length() + " " + line);
            				  }
            				  
            			  }
            		  }
            	  }
                    	  
            	 
            	  
            	  
            	  
            	  
              	if(line.indexOf("AMF") != -1){
              	 	String ha = "AMF";
              		 while((line = reader.readLine()) != null ){
              			line = line.replaceAll("\t","| "); 
              			
              			 StringTokenizer st = new StringTokenizer(line, "|");
              			
              			if(line.trim().length() < 21 && line.trim().length() > 0 ){
              			// System.out.println(line.length() + "p " + line );
              			MatrixLine mline = new MatrixLine();
              		
              			  mline.setProdID(st.nextToken().trim());
              			 if(ha.length() == 0 ){
              				 ha = " ";
              			 }
              			  mline.setCliGroup(ha);
                         
                          double disc = 0;
                     
                       try{
                    	   String dii = st.nextToken();
                    	 disc = Double.parseDouble(dii.replace(',', '.').replaceAll(" ", "").replaceAll("%", "").replaceAll("_", ".").replaceAll("K", " ").trim());
                    	
                    	 if(dii.indexOf("K") != -1){
                    	 	 mline.setStartDate(getDateString(Calendar.getInstance()));
                          }
                          
                       }catch(Exception e){
                    	 }
                          mline.setDiscount(disc);
                    // System.out.println(mline.getCliGroup() + " " + mline.getProdID() + " " + mline.getDiscount());
                        if(!olemas.contains(mline.getStringForFixedAssortment())){
                        	 lines.add(mline);
                        	 olemas.add(mline.getStringForFixedAssortment());
                        }
                      
              		 }else if(line.trim().length() != 0){
              			st.nextToken();
              			st.nextToken();
              			st.nextToken();
              			
              			ha = st.nextToken().trim();
              		//	System.out.println("ha '" + ha  + "' " + line.length() + " " + line );
              		 }
              	   }
              	}
              }
              reader.close();
         }catch(Exception e){
        	 e.printStackTrace();
         }

         return lines;
       }

       
       
       
       
   	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(6,10 ));
		int MONTH = Integer.parseInt(kuup.substring(3, 5))-1;
		int DAY = Integer.parseInt(kuup.substring(0, 2));
		//System.out.println( YEAR + " " + MONTH + " " + DAY);
		cal.set(YEAR, MONTH, DAY, 1, 2, 3);
		Date now = new Date(cal.getTimeInMillis());
		return now;
	}
   	private boolean isThisMonth(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(0, 4));
		int MONTH = Integer.parseInt(kuup.substring(6, 7))-1;
		if(cal.get(Calendar.YEAR) == YEAR && cal.get(Calendar.MONTH) == MONTH){
			return true;
		}
		return false;
	}
   public Vector getDebt(String agentCode){
    Vector list = new Vector();

    try {

        FileReader fr = new FileReader(getDir() + "volg.TXT");
        BufferedReader reader = new BufferedReader(fr);
      String line = null;    //	System.out.println(line);

TreeMap tr = new TreeMap();
      while((line = reader.readLine()) != null){
      	
      if(line.indexOf("IPVc")!= -1){
      	 while((line = reader.readLine()) != null){
      	 if(!line.startsWith("2\t") && line.length()>0){
      	 
    	line = line.replaceAll("\t","; ");
    	
    	//System.out.println(line);
    	 StringTokenizer st = new StringTokenizer(line, ";");
    	 st.nextToken();
         st.nextToken();
      	String arve = st.nextToken().trim(); 
      double eek = 1;
      	for(int i = 0;i<10;i++){
      		String asi = st.nextToken();
      		
      	}
      	String token = st.nextToken().replaceAll(",",".").replaceAll(" ","");
      	try{
      		double dd = Double.parseDouble(token)/eek;
      		token = dd + "";
      	}catch(Exception e ){
      		
      	}
      	tr.put(arve,token);
        }	
   	   }
      }	
   	 }
    
       fr = new FileReader(getDir() + "volg.TXT");
       reader = new BufferedReader(fr);
     line = null;    //	System.out.println(line);


    while((line = reader.readLine()) != null){
    if(line.indexOf("IVVc2")!= -1){
    
    while((line = reader.readLine()) != null && !line.startsWith("IPVc") ){
    	if( !line.startsWith("1\t")&& !line.startsWith("3\t") && line.length()>0){
    	
    	line = line.replaceAll("\t","; ");
    //System.out.println(line);
   
    StringTokenizer st = new StringTokenizer(line, ";");
	Debt debt = new Debt();
	if (st.hasMoreTokens()) {
			String arve = st.nextToken().trim();
			String miinus = (String) tr.get(arve);
			double min = 0;
			if (miinus != null){
				min = Double.parseDouble(miinus);
			}
			debt.setDoknr(arve);
		st.nextToken();
		String id = st.nextToken().trim();
		debt.setCustID(id);
		
		if (st.hasMoreTokens()) {
			String kuup = st.nextToken().trim();
			debt.setDuedate(getDate(kuup));
			
		}
		
		double eek = 1;
		double kordaja = 1;
		for (int i =0;i<44;i++){
			String asi = st.nextToken();
		if(i == 17 && asi.trim().equals("3")){
			kordaja = -1;
		}
			if(arve.equals("50808")){
			//	System.out.println("id " + arve  + " " + kordaja + " "+ asi + " " + i);	
			}
		}
			
		
		if (st.hasMoreTokens()) {
			String sum = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
			double summa = Math.round(Double.parseDouble(sum) *1.20 *100 / eek) * kordaja;
			debt.setSum(summa/100-(min/eek));
		 }

		
		
		
		if(debt.getDuedate() != null && debt.getDuedate().before(new java.util.Date())){
			String open  = (String)overdue.get(debt.getCustID());
			if (open != null ){
				double res = Double.parseDouble(open) + debt.getSum();
				overdue.put(debt.getCustID(),res+"");
			}else{
				overdue.put(debt.getCustID(),debt.getSum()+"");	
			}
			
		}
		
		
		
		String open  = (String)creditused.get(debt.getCustID());
		if (open != null ){
			double res = Double.parseDouble(open) + debt.getSum();
			creditused.put(debt.getCustID(),res+"");
		}else{
			creditused.put(debt.getCustID(),debt.getSum()+"");	
		}
		
		
	int due = getDue(debt.getDuedate().getTime()) ;
	if(due < 400){
		debt.setDue(due + "");
		list.addElement(debt);
	    }
	   }
      }
    }
    }
	
    }
    
    } catch (Exception e){
      e.printStackTrace();
    }
    return list;
   }

  
   private int getDue(long dd){
      int res = 0;
      res =(int) ((-dd+(new java.util.Date()).getTime())/86400000) ;
   //   System.out.println("res " + res + " dd" + dd + " " +(( -dd+(new java.util.Date()).getTime())/86400000));
      return res ;
   }
   
   public void connectionDown(){
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

                                               public String  getCustProdDiscounts(Agent agent){
                                                return null;
                                                }

   public TreeMap getProdGroups() throws SQLException {
    TreeMap tm = new TreeMap();

   	try {
   
        BufferedReader reader = new BufferedReader(
   			   new InputStreamReader(
   	                      new FileInputStream(getDir() + PRODUCT_FILE), "UTF8"));
      String line = null;

      while((line = reader.readLine()) != null){
      	
      	// System.out.println(line);
      	if(line.indexOf("rec1") != -1){
      		
      	//	System.out.println("###" + line);
      		while((line = reader.readLine()) != null){
      			line = line.replaceAll("\t","; "); 
      		//	System.out.println(line);
      			 
      			 
        StringTokenizer st = new StringTokenizer(line, ";");
        if (st.hasMoreTokens()){
        	
          String id = st.nextToken().trim();
         
          if (st.hasMoreTokens()){
            String name = st.nextToken().trim();
            if (name.equals("")) name = "<" + id + ">";
            	 tm.put(id, clear(name));    
            	// System.out.println("see " + id + " " + name);
          }       
        }
      }	
      		
  		}
      
      
      }
      reader.close();
      } catch (Exception e){
        e.printStackTrace();
      }
      return tm;
   }

   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
    // getPriceMatrixData();
     try {
     
       BufferedReader reader = new BufferedReader(
   			   new InputStreamReader(
   	                      new FileInputStream(getDir() + AGENT_FILE), "UTF8"));
     String line = null;
     while((line = reader.readLine()) != null){
     	
     
     	if(line.indexOf("user2") != -1){
     	
     		while((line = reader.readLine()) != null){
     		
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
    	   st.nextToken().trim();
    	   st.nextToken().trim();
    	   st.nextToken().trim();
         String id = st.nextToken().trim();
         Agent agent = new Agent(id);
         if (st.hasMoreTokens()){
           String name = st.nextToken().trim();
           if (name.equals("")) name = "<" + id + ">";
           agent.setName(this.clear(name));
          /* if (st.hasMoreTokens()){
             String prodGroup = st.nextToken().trim();
             agent.setProdGroup(prodGroup);
           }*/
         }
         tm.put(agent.getName(), agent);    
         agent1.put(agent.getID(), agent);
       }
     }	
     		
 		}
     }
     
     reader.close();
     
     } catch (Exception e){
       e.printStackTrace();
     }
     tm.put("Kontor", new Agent("HOUSE"));
     return tm;
   }
    private TreeMap agent1 = new TreeMap();

   public TreeMap getTing(){
	     TreeMap tm = new TreeMap();

	     try {
	    	  BufferedReader reader = new BufferedReader(
	      			   new InputStreamReader(
	      	                      new FileInputStream(getDir() + "maksetin.TXT"), "UTF8"));
	     String line = null;
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     line = reader.readLine();
	     
	     
	     while((line = reader.readLine()) != null){	     	
	     		
	            StringTokenizer st = new StringTokenizer(line, SEPARATOR);
	       if (st.hasMoreTokens()){
	         String id = st.nextToken().trim();
	     //    System.out.println("aaaa " + line);
	         if (st.hasMoreTokens()){
	        	 st.nextToken();
	        	 if (st.hasMoreTokens()){

		        	 tm.put(id, st.nextToken()); 
	        	 }
	         }
	        // 
	       }
	     
	     }
	     reader.close();
	     } catch (Exception e){
	       e.printStackTrace();
	     }
	   //  System.out.println("aaaa " + tm.toString());
	     return tm;
	   }


       public String getDistribution() {
         return null;
       }

       
       
       

   public TreeMap getCustomers(Agent agent){
  
	   
	   TreeMap avegs = new TreeMap();
	   TreeMap thismonth = new TreeMap();
	   
	    try {
	       
	        BufferedReader reader = new BufferedReader(
	      			   new InputStreamReader(
	      	                      new FileInputStream(getDir() + CUSTOMER_PRODUCT_FILE ), "UTF8"));
	      String line = null;
	      while((line = reader.readLine()) != null){
	  		 line = line.replaceAll("\t","; ");
	    	//	System.out.println("XX " +line); 
	    		 StringTokenizer st = new StringTokenizer(line, ";");
	        if (st.hasMoreTokens()){
	        	 
	        	st.nextToken();
	        	 if (st.hasMoreTokens()){
	        		 
	        	 
	        	String lastdate = st.nextToken().trim();
	        	long tehing = Calendar.getInstance().getTimeInMillis() -  getDate(lastdate).getTime();
	        	
	        	
	          String custID = st.nextToken().trim();
	          if (st.hasMoreTokens())
	      	st.nextToken();	
	          if (st.hasMoreTokens())
	      	st.nextToken();
	                if (st.hasMoreTokens()){
	                  String prodID = st.nextToken().trim();
	                  String am = st.nextToken().replaceAll(" ","").replaceAll(",",".");
	                  if(am.length()==0){
	                  	am = "0";
	                  }
	                  double lastamount = Double.parseDouble(am);
	                  
	                  if(lastamount > 0){	                  	
	                  CustProd cp = new CustProd(custID, prodID, lastamount, 0, 0,lastdate.substring(0,5),0,0,0," ");
	                    custProds.addElement(cp);
	                  //  System.out.println(custID + " " + prodID + " "+ lastamount);
		                 
	                  }
	                  st.nextToken();
	                  st.nextToken();
	                  
	                  String sum = st.nextToken().replaceAll(" ","").replaceAll(",",".");
	                  //System.out.println("sum " + sum);
	                 try{
	                     double sales = Double.parseDouble(sum);
		                  
		                  if(tehing < Long.parseLong("7776000000")){
		                	   if(avegs.get(custID) == null){
		 	                	  avegs.put(custID, sales + "");
		 	                  }else{
		 	                	double sold = Double.parseDouble((String)avegs.get(custID));
		 	                	 avegs.put(custID, (sales + sold) + "");
		 	                  }  
		                  }
		                  
		                  if(this.isThisMonth(lastdate)){
		                	  if(thismonth.get(custID) == null){
		                		  thismonth.put(custID, sales + "");
		 	                  }else{
		 	                	double sold = Double.parseDouble((String)thismonth.get(custID));
		 	                	thismonth.put(custID, (sales + sold) + "");
		 	                  } 
		                  }
		                  
	                 }catch (Exception e ){
	                	 
	                 }
	             
	               
	                  
	          }
	        }}
	      }
	      reader.close();
	      } catch (Exception e){
	        e.printStackTrace();
	      }
	 if(rcompany != null){   
				Converter.getManagersReplacements(agent1, rcompany, user , pass);   
			 }   

     TreeMap tm = new TreeMap();
     StringBuffer buf = new StringBuffer ();
     this.customers = new ArrayList();
   String confirm = getParent().getProps().getProperty("confirmemail");
     getDebt(null);
     try {
     // TreeMap ting = getTing();
     	
       FileReader fr = new FileReader(getDir() + CUSTOMER_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
     	
     	
     	if(line.indexOf("cu5") != -1){
     		
  while((line = reader.readLine()) != null){
  	line = line.replaceAll("\t","| ");
  	line = line.replaceAll(";", ":");
  	
  	if(line.length() > 100) {
  	
 	  StringTokenizer st = new StringTokenizer(line, "|");
 	 //System.out.println(line);
       if (st.hasMoreTokens()){
    	   st.nextToken();
    	   st.nextToken();
    	   st.nextToken();
         String id = st.nextToken().trim();
         Customer customer = new Customer(id); 

         customer.setAddress(" ");
         if (st.hasMoreTokens()){
           String name = st.nextToken().trim();
           if (name.equals("")) name = "<" + id + ">";      
            name = name + " " + id;
          String cinfo =  st.nextToken();
          if(cinfo.trim().length()>0){
        	  customer.setPersonalData(this.clear(cinfo));
          }
           st.nextToken();
           if (st.hasMoreTokens()){
             String aadress = st.nextToken().trim();
               if( aadress != null){
             	 aadress = clear(aadress);            
               customer.setAddress(Converter.adressLink(aadress, "Estonia"));
             }
           }
           if (st.hasMoreTokens()){
            String lisa = st.nextToken().trim();
            if( lisa != null){
          
            }
          }
    //  System.out.println(line);

    //  System.out.println(name);
           name = clear(name);
             customer.setName(Converter.enCodeEst(name));  

             
             clear(st.nextToken().trim());
             
             if(confirm != null){
                 customer.setConfirmEmail(confirm); 
             }
             
             
           st.nextToken();
          
        String  aadress =  st.nextToken().trim();
           if( aadress != null && aadress.trim().length() > 0){
             customer.setInfo(Converter.enCodeEst(aadress));
           }
      
      if (st.hasMoreTokens()){
       String lisa = st.nextToken().trim();
       if( lisa != null){
        }
     
     }
           if(avegs.get(customer.getID())!= null){
        	   double sss =  Double.parseDouble((String) avegs.get(customer.getID()));       	   
        	   customer.setAverage((int)sss/3); 
           }

           if(thismonth.get(customer.getID())!= null){
        	   double sss =  Double.parseDouble((String) thismonth.get(customer.getID()));       	   
        	   customer.setSold((int)sss); 
           }
           
      st.nextToken();
      //     st.nextToken();
   //10202	A-Selver AS			P�rnu mnt 238	Tallinn 11624	Kadaka Selver		Kadaka tee 56A	Tallinn 12915		6673400	6673414	SELVE		45	0	0		1			KADSELVER	1			0	GAJAN				1	Kadaka Selver				075		0	09.02.2007					0									0				0	0			0	0	0	19.07.2004	0											0	0	0				0	0																					0	0	1									102068		0	0				0		0			
        
           if (st.hasMoreTokens()){
           	String info = st.nextToken().trim();
           	if(info.length()>0 && info.trim().length()>0){

                customer.setPersonalData(Converter.enCodeEst(customer.getPersonalData()) + " " +clear(info));	
           	}
            }
           st.nextToken();
           st.nextToken();
           st.nextToken();
          
          
           if (st.hasMoreTokens()){
           	String paym = st.nextToken().trim();
			if(paym.length()>0 && paym.trim().length()>0){
				
			//	if(ting.get(paym.trim()) != null ){
				//	customer.setPaymTerm((String)ting.get(paym.trim()));	
			//	}else{
					customer.setPaymTerm(paym.trim());
			//	}
				
		           	
			}
             }

           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
           st.nextToken();
          
        String hinnak =    st.nextToken();
       // System.out.println(  hinnak);
        if(hinnak != null && hinnak.trim().length() > 0){
        	customer.setGroup(hinnak.trim());	
        }
          st.nextToken();
          
           st.nextToken();
           st.nextToken();
           st.nextToken();

           if (st.hasMoreTokens()){
        	   String agg = st.nextToken().trim();
        	 //  System.out.println("agg" + agg);
        	   if(agg.length()>0){
                   customer.addAgent(new Agent(agg));
                   String ahh = agg;
                   if(agg.length() >3 && agg.indexOf("ARV") != -1){
                	   customer.addAgent(new Agent("ARV"));
                	   ahh = "ARV";
                   }
                   if(agg.length() >3 && agg.indexOf("MER") != -1){
                	   customer.addAgent(new Agent("MER"));
                	   ahh = "MER";
                   }
                   if(agg.length() >3 && agg.indexOf("URV") != -1){
                	   customer.addAgent(new Agent("URV"));
                	   ahh = "URV";
                   }

                   if(agg.length() >3 && agg.indexOf("TLN") != -1){
                	   customer.addAgent(new Agent("TLN"));
                	   ahh = "TLN";
                   }
                   if(agg.length() >3 && agg.indexOf("GUN") != -1){
                	   customer.addAgent(new Agent("GUN"));
                	   ahh = "GUN";
                   }
                   if(agg.length() >3 && agg.indexOf("AND") != -1){
                	   customer.addAgent(new Agent("AND"));
                	   ahh = "AND";
                   }
                   if(agg.length() >3 && agg.indexOf("PIL") != -1){
                	   customer.addAgent(new Agent("PIL"));
                	   ahh = "PIL";
                   }
                   customer.setCity(agg);
                   customer.addAgent(new Agent("HOUSE"));
                   customer.addAgent(new Agent("ARV"));
                   customer.addAgent(new Agent("EVE"));
                   customer.setMainAgent(ahh);
                 //  customer.addAgent(new Agent("GUN"));
                   	
                          		         if ( ahh != null && agent1.get( ahh.trim()) != null){
                          		                      Agent aa = (Agent) agent1.get( ahh.trim());
                          		                       Vector vec = aa.getDubbler();
                          		                       if( vec!= null && vec.size() > 0 ){
                          		                            Enumeration en = vec.elements();
                          		                            while(en.hasMoreElements()){
                          		                                 String ag = (String) en.nextElement();
                          		                              customer.addAgent( new Agent( ag));
                          		                              System.out.println("Asendan " + customer.getID()+ " "
                          		                              + ahh + " " + ag);
                          		                           }
                          		                      }
                          		                }
                          		        }
 
                               }

           
           if (st.hasMoreTokens()){
           	String cred = st.nextToken().trim();
           	if(cred.length()>0 && cred.trim().length()>0){
           	//	customer.setCredit(cred.replaceAll(" ", "").replaceAll(",", "."));	
           	}          
           }
           
           /*

           if (st.hasMoreTokens()){
            customer.setVisit(st.nextToken().trim());
            System.out.println(customer.getVisit());
          }*/
           
           String tok = "";
           String tok2 = "";
           String pricelist = "";
           String pricelist2 = "";
           String delivery = "";

           String visit = "";
           for(int i = 0 ;i<86;i++){
           String	toke = st.nextToken();
           //if(customer.getID().startsWith("1181"))//nexilis
            //   System.out.println(customer.getName() + " " + i + " " + toke);
           	if(i==9){
           tok = toke.trim();	
           	}
           	if(i==7){
                tok2 = toke.trim();	
                	}
        	if(i==6){
                pricelist = toke.trim();	
                	}
        	if(i==7){
                pricelist2 = toke.trim();	
                	}
        
        	if(i==41){
                delivery = toke.trim();	
                	}
        	if(i==27){
                visit = toke.trim();	
                	}
        	
           }
           if(customer.getID().startsWith("161"))//nexilis
         System.out.println(customer.getName() + " | blokk"  + pricelist2 + " " + delivery + " vis:"+ visit);

           if(delivery.trim().length()>0){
        	   customer.setAddress(delivery.trim());
           }
           if(visit.trim().length()>0){
        	   customer.setVisit(Converter.getVisitDay(visit.trim()));
           }
              
        	   
           if(pricelist.trim().length()>0){
              	customer.setGroup(pricelist);
              }
           if(pricelist2.trim().length()>0){
        	   if(customer.getGroup().length() > 0 ){
        		   customer.setGroup(customer.getGroup() + "|" + pricelist2);
        	   }else{
        		   customer.setGroup(pricelist2);
        	   }
             }
           
           if(tok2.trim().length()>0){
           	if(tok2.trim().length()>2){
           		tok2 = tok2.substring(0,2)+ "." + tok2.substring(2,tok2.length());
          		
           	}
          // 	double re = Double.parseDouble(tok2);    
           	//customer.setDiscount(re);
           }
         //	System.out.println("Ale " + customer.getName() + " " +customer.getDiscount() + " " + tok);
          
         //	customer.setPrimDeal(customer.getID());        

           if(overdue.containsKey(customer.getID())){
          // 	customer.setDebt(Double.parseDouble((String)overdue.get(customer.getID())));
          }
           if(creditused.containsKey(customer.getID())){
              	customer.setUsedCredit(Double.parseDouble((String)creditused.get(customer.getID())));
             }
           if(!tok.equals("1555")){
        	//	System.out.println("Ale2 " + customer.getName() + " " +customer.getDiscount() + " " + tok);
             tm.put(customer.getName(), customer);
             this.customers.add(customer.getID());
             buf.append(customer.getID() + ";" + customer.getName() + ";" +  customer.getAddress() + "; ; ; ; ; ; \n");
           }
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
     } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     
 
     
 
     
     return tm;
   }

   
  
  
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     StringBuffer buf = new StringBuffer();
     try {
    	  BufferedReader reader = new BufferedReader(
     			   new InputStreamReader(
     	                      new FileInputStream(getDir() + PRODUCT_FILE ), "UTF8"));
  
     String line = null;
     while((line = reader.readLine()) != null){
    	 boolean goes = false;    	
     	if(line.indexOf("artikel5") != -1){
     		 while((line = reader.readLine()) != null && line.indexOf("ITVc2") == -1){
     		
     		 line = line.replaceAll("\t","; ");
     		//System.out.println(line);
     		if(line.length()>100){
     		try{
     			
     		
       StringTokenizer st = new StringTokenizer(line, ";");
       if (st.hasMoreTokens()){
    	   st.nextToken();
    	   st.nextToken();
    	   st.nextToken();
         String id = st.nextToken().trim();
         Product prod = new Product(id);
         String out = "";
         String name = "";
         if (st.hasMoreTokens()){
        	 name = st.nextToken().trim();
          
          
     
           if (st.hasMoreTokens()){
           	String unit = st.nextToken().trim();
           	if(unit.length()>0){
           		prod.setUnit(unit);
           	}
          String kastis =  	st.nextToken();
         // System.out.println("kast " + kastis);
          if(kastis != null && kastis.trim().length()>0){
        	  prod.setInCases(Double.parseDouble(kastis.replace(',', '.')));
          }
           	st.nextToken();
           	st.nextToken();
            if (st.hasMoreTokens()){
            	String pr = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
            	if(pr.length() == 0){
            		pr = "0";
            	}
                double price = Double.parseDouble(pr);
                if (price > 0){
                  prod.setPrice(price);}
               //   System.out.println("price" + prod.getPrice());

               	st.nextToken();
               	String gr = st.nextToken().trim();
               	if (gr.length()>0){
               		//prod.setGroup(gr);
               	}
               	
               	
               	if(hinnad.get(prod.getID()) != null){
               	//	prod.setPrice(Double.parseDouble((String) hinnad.get(prod.getID())));
               	}
               	
           	String ii = "";

           	
           	
           	ii = st.nextToken();
           	ii = st.nextToken();      
           	ii = st.nextToken();
          // 	System.out.println("ii" + ii);
           	String cos = ii.trim().replaceAll(",",".").replaceAll(" ","");
           	if(cos.length() == 0){
        		cos = "0";
        	}
           	 double cost = Double.parseDouble(cos);
           	 
           	 double marg = (1-cost/prod.getPrice())*100 ;
           	 if(marg >0){
           	 	 prod.setMargin((int) marg + "");
                //	System.out.println("price" + prod.getPrice() + " " + (int)marg + " " + cos );
                	 
           	 }
           	 //|D198C21D-7057D9D0-46C25505-5B421912-DA9B79A3||1|1530|Kommikarp "RIVIERA" 125gr*12tk|KARP|12
            // |40121|1,35|1|MIL|||0,88442|SD||0||0,00076|0,08334|.....30.07.17|||KARP|0||4820075502492|2|
             String sortcode =  st.nextToken();
           	if(sortcode != null && sortcode.trim().length() > 0 ){
           		prod.setSorting(sortcode.trim());
           	}
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken(); 
           	ii =	st.nextToken();
           	String ean2 =	st.nextToken();
           //	System.out.println("ean2 " + ean2);
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	String grupp =	st.nextToken();
           
           	ii =	st.nextToken();
           
             	st.nextToken();
           	
          
 
           	if(grupp.trim().length()>0){
           		prod.setGroup(grupp.trim());
           		if(grupp.trim().equals("OTSAS")){
           			prod.setStock(0);
           		}
           		String aaa = grupp.trim().substring(0,3);
           		if(grupp.trim().length() > 3){
           			aaa = grupp.trim().substring(0,4);
           		}
           		name = aaa + " " + id + " " +clear(name).replaceAll("Pakitud", "Pk");
          
           		
           	    prod.setName(Converter.enCodeEst(name));
           		goes = true;
           		//System.out.println("name " + prod.getName() + " " + prod.getGroup());
           	}
           	
           	String partii = st.nextToken().trim();
           
         	
           	if(ean2.trim().length()>0){
           		prod.setPartii(ean2.trim());
           	}

         	st.nextToken();
    

       	if(partii.trim().length()>0){
       		partii = partii.trim().replaceAll(";", ",");
       		prod.setEAN(partii.trim());
       	}
       	
       out =   st.nextToken();
     
           	
           	st.nextToken();
           	st.nextToken();
           	st.nextToken();
    out=   	st.nextToken();
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
          
             if (st.hasMoreTokens()){
             	String ca = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
             	if(ca.trim().length()==0){
             		ca = "0";
             	}
                 double inCases = Double.parseDouble(ca);
                 if (inCases > 0){
                   prod.setInCases(inCases);
                  }
              }
             
             int o = 0; 
          while  (st.hasMoreTokens()) {
        	  o++;
        	 String tok = st.nextToken();
        	 	
        	 if("AA".equals(tok.trim())) {
        		 prod.setStatusTop("1");
        		 //System.out.println("aaa " + tok + " " + o);
        	 }
        	  
          }
  
            
           //	System.out.println("CAse" + st.nextToken());
             }
           }
         }
         
       //  prod.setStock(0)
         
       /*  if(sto.get(prod.getID()) != null){
         	prod.setStock( Double.parseDouble(((String)sto.get(prod.getID())).replaceAll(" ","").replaceAll(",",".") ));
         }*/
       //  System.out.println("Out" + prod.getName()+ " " + out);
        String ll = reader.readLine();
        ll = reader.readLine();
        if(ll != null && ll.trim().length() > 0 && ll.indexOf("//")!= -1){
        	String aaa = ll.substring(ll.indexOf("//")+2).trim() + " ";
        	if(prod.getPartii().trim().length() > 0){
        	 	prod.setPartii(prod.getPartii() + " " + aaa.replaceAll(";", ",").trim() + " ");
        	        		
        	}else{
        		prod.setPartii( aaa.replaceAll(";", ","));
        	    
        	}
        }
        
        ll = reader.readLine();
        if(ll != null && ll.trim().length() > 10){
        	String aaa = ll.trim();
        	if(prod.getPartii().trim().length() > 0){
        	 	prod.setPartii(prod.getPartii() + " " + aaa.replaceAll(";", ",").trim() + " ");
        	        		
        	}else{
        		prod.setPartii( aaa.replaceAll(";", ","));
        	    
        	}
        }
         
         	if( goes && prod.getName()!= null ){
                 tm.put(name, prod);       	
         	}
            
       }
     		 }catch(Exception r){
      			
      		}}	
      		}
     	}
     }
     reader.close();
     } catch (IOException e){
       e.printStackTrace();
     }

     
     return tm;
   }
   
   public Vector getCustomerProductsList(String agentCode){
     return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "HansaPomm";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }

   public double getSalary(String agentCode){
    
     try {
     
    	  BufferedReader reader = new BufferedReader(
    			   new InputStreamReader(
    	                      new FileInputStream(getDir() + AGENT_SALES_FILE ), "UTF8"));
       String line = null;
       while((line = reader.readLine()) != null){
         StringTokenizer st = new StringTokenizer(line, SEPARATOR);
         if (st.hasMoreTokens()){
           String agentID = st.nextToken().trim();
           if (agentID.equals(agentCode)){
             if (st.hasMoreTokens()){
               String sls = st.nextToken().trim();
               setSales(Double.parseDouble(sls));
             }
           }
         }
       }
       reader.close();
      
     } catch (IOException e){
       e.printStackTrace();
     }

     return 0;
   }

   private String getDir(){
     String dir = getParent().getProps().getProperty("readin");
     if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";
     return dir;
   }



   private String clear (String ss){
    //	System.out.println(ss);
      String ret =   ss.replace(',','.');
      ret =  ret.replace(',','.');
      ret =  ret.replace(',','.');
      ret =  ret.replace(',','.');
      ret =  ret.replace('"',' ').trim();
      ret =  ret.replace('"',' ').trim();
      ret =  ret.replace('"',' ').trim();


  
//  O?
      ret = Converter.enCodeEst(ret);
      
      if(ret.indexOf("�") > -1 ){
    	  ret = ret.substring(0,ret.indexOf("�")) + "ü" +ret.substring(ret.indexOf("�")+2 );
      }
 	
      if(ret.indexOf("�") > -1 ){
    	//  ret = ret.substring(0,ret.indexOf("�")) + "�" +ret.substring(ret.indexOf("�")+2 );
      }
 	
      if(ret == null || ret.trim().length()==0){
      return " ";
      }else {
      return ret;
      }
    }
   
   public static final String SEPARATOR = "\t";
   public static final String PRODUCT_FILE = "tooted.TXT";
   public static final String PRODUCT_PRICES_FILE = "hinnakirjad.TXT";
   public static final String AGENT_FILE = "agent.TXT";
   public static final String CUSTOMER_FILE = "kliendid.TXT";
   public static final String CUSTOMER_AGENT_FILE = "klagent.txt";
   public static final String CUSTOMER_PRODUCT_FILE = "klienditoode.TXT";
   public static final String CUSTOMER_DEBT_FILE = "volg.txt";
   public static final String AGENT_SALES_FILE = "5_sales_agents.txt";
   private TreeMap overdue = new TreeMap();
   private Vector custProds = new Vector();
   private TreeMap creditused = new TreeMap();
   private HashMap hinnad = new HashMap();
   private ArrayList customers = null;
   public static final String DRIVER_VERSION = "HansaPomm";
   private String	       url = "jdbc:odbc:aaaaa";
   private Connection	       con = null;
  private String	       subjectFilesTo = null;

  
  public String getImportFile(){
	    return parent.getProps().getProperty("imp.read");
	  }


  private void saveFile(String fileName,String content ){
		File fil = new File( getImportFile() + "reponse/");
  	if(!fil.exists()){
  		fil.mkdir();
  	}
try{
		PrintWriter os = new PrintWriter(new FileWriter( fileName , true));
		os.write(content);
		os.close(); 

      System.out.println("Faili salvestus " + fileName); 
}catch(Exception r){
	  r.printStackTrace();
}
    
       
}
  
  
  private void sendReExReport(){
 	  
 	  StringBuffer sb = new StringBuffer();
 	  String filename = "Order.csv";
 	  String tomail = "";
 	  String sub = "Order from Bidvest Lithuania";
 	 // peame tegema erinevatele saajatele objektid ku sees treemap tootenimi  + vector toode.objektiga 
 	  TreeMap saajad = new TreeMap();
 	  
 	  Calendar cal = Calendar.getInstance();
 	  cal.add(Calendar.DATE, -2);
        
  	   String ddd = Converter.getYYYYpMMpDDpString(cal.getTime());
 	  File dir = new File(getImportFile() + "report/");
 	    if (dir.isDirectory()){
 	      File[] files = dir.listFiles();
 	      for (int i = 0; i < files.length ; i++){
 	        if (files[i].isFile()){
 	    
 	    File file = files[i];
 	    
 	    
 	    if(file.getName().indexOf(ddd) != -1){ // nkui on t�nase p�eva objekt
 	    
         FileReader ff;
 		try {
 			ff = new FileReader(file);
 		
 		File hj = new File(filename);
 			if(hj.exists()){
 				hj.delete();
 			}
 		BufferedReader br = new BufferedReader(ff);
 		String line = null;
 		while( (line = br.readLine()) != null){
             String saaja = "";
             String klient = "";
             String toode = "";
             double kogus = 0;
             String yhik = "";
             String nimi = "";
 			
             StringTokenizer st = new StringTokenizer(line, ",");
 			if (st.hasMoreTokens()){
 				klient = st.nextToken();	
 		     }

 			if (st.hasMoreTokens()){
 				toode = st.nextToken();	
 		     }

 			if (st.hasMoreTokens()){
 				saaja = st.nextToken();	
 		     }

 			if (st.hasMoreTokens()){
 				kogus = Double.parseDouble(st.nextToken());	
 		     }
 			
 			if (st.hasMoreTokens()){
 				yhik = st.nextToken();	
 		     }
 			
 			if (st.hasMoreTokens()){
 				nimi = st.nextToken();	
 		     }

    System.out.println(nimi + " "+ yhik+ " "+kogus+ " "+saaja+ " "+toode+ " "+klient);
 			
 			
 			if(saajad.get(saaja) != null){
 				TreeMap prods = (TreeMap)saajad.get(saaja);
 				if(prods.get(nimi) != null){
 				 Vector vv = (Vector)prods.get(nimi); 

 					Product t2 = new Product(toode);
 					t2.setName(nimi);
 					t2.setUnit(yhik);
 					t2.setStock(kogus);
 					t2.setAgentGroup(klient);
 					 vv.add(t2);
 					      	prods.put(nimi, vv);
 					      	saajad.put(saaja, prods);
 				}else{
 					Product t2 = new Product(toode);
 					t2.setName(nimi);
 					t2.setUnit(yhik);
 					t2.setStock(kogus);
 					t2.setAgentGroup(klient);
 					Vector pr = new Vector();
 					 pr.add(t2);
 					      	prods.put(nimi, pr);
 					      	saajad.put(saaja, prods);
 							
 				}
 				
 			}else{
 				Product t2 = new Product(toode);
 				t2.setName(nimi);
 				t2.setUnit(yhik);
 				t2.setStock(kogus);
 				t2.setAgentGroup(klient);
 				TreeMap prods = new TreeMap();
 				Vector pr = new Vector();
 				 pr.add(t2);
 				      	prods.put(nimi, pr);
 				      	saajad.put(saaja, prods);
 			}
   	           
 	        }
 	    br.close();
 	    ff.close();
 		} catch (Exception e) {
 				e.printStackTrace();
 		}
 		
 	        }
 		
 		    }
 	      }
 	    
 	    }
 	    
 	  
 	    // n��d on vaja fail kokku panna ja summeerides koguseid klienid toote p�hiselt
 	    
 	 Iterator itera =   saajad.keySet().iterator();
 	 while (itera.hasNext()){
 		 String saaja = itera.next() + "";
 		 TreeMap prods = (TreeMap)saajad.get(saaja);
 	     TreeMap vahe = new TreeMap();
 		 
 	     // liidame kokku samatoote kogused sama kliendi juures 
 	     // 
 	     
 		 Iterator iterap =   prods.keySet().iterator();
 		 Vector<String> kliendid = new Vector<String>();
 		 while (iterap.hasNext()){
 			 String nimi = iterap.next() + "";
 			 Vector prs = (Vector)prods.get(nimi);
 			 
 			 for(int i =0; i < prs.size();i++){
 				 Product pp = (Product)prs.elementAt(i);
 				 String klient = pp.getAgentGroup();
 				 if(!kliendid.contains(klient)){
 					kliendid.add(klient); 
 				 }
 				 if(vahe.get(pp.getID() + " " + pp.getName()) != null){
 					 TreeMap klkogus = (TreeMap)vahe.get(pp.getID() + " " + pp.getName()) ;
 					 if(klkogus.get(klient) != null){
                        double dd = Double.parseDouble(klkogus.get(klient) + "");
 						 klkogus.put(klient, (pp.getStock() + dd) + "");
 					 }else{
 						 klkogus.put(klient, pp.getStock() + "");
 					 }
 					 vahe.put(pp.getID() + " " + pp.getName(), klkogus );
 					 //System.out.println(nimi + " zzz " + klient + " " + pp.getID());
 						
 				 }else{
 					 
 					// System.out.println(nimi + " yyy " + klient + " " + pp.getID());
 						
 					 TreeMap klkogus = new TreeMap();
 					 klkogus.put(klient, pp.getStock() + "");
 					 vahe.put(pp.getID() + " " + pp.getName(), klkogus );
 					 	 
 				 }
 				 }		 
 		 }
 			 
 		 
 		 String head = " ," ;
 		 for(String nn : kliendid){
 			 head = head + nn + " ,";
 		 }
 		 
 		 
 		 Iterator iteras =   vahe.keySet().iterator();
 		 while (iteras.hasNext()){
 			String nimi = iteras.next() + "";
 			TreeMap kls = (TreeMap)vahe.get(nimi);
 			 sb.append(  nimi  + "," );
 			// System.out.println("kls " + kls.toString() + " \n" );
 	 		//K�ime k�ik kliendid l�bi ja paneme sii styhiku kui pole kogust talle 
 			 for(String nn : kliendid){
 	 	 		// String klie = (String)iterad.next();
 				//	 System.out.println("nn " + nn + " " + kls.get(nn)  );
 					 if(kls.get(nn) != null){
 						 sb.append(kls.get(nn) + ",");
 					 }else{
 						 sb.append(" ,");
 					 }
 			 }
 			 sb.append(  "\n");
 		  }
 		
 		
 		tomail = parent.getProps().getProperty(saaja);
      tomail = "aivar@mobilejava.ee";
 		  if(sb.length() > 0 && tomail != null){
 			  saveFile(filename,"sep=,\n" + head + "\n" + sb.toString());
 			  sendEmail(head + "\n" + sb.toString(),sub,tomail,filename); 
 		  }
 		 
 	 }
 	    
 	  
 	  
 	  
 	  //renameme nyyd k�ik failid
 	   dir = new File(getImportFile() + "report/");
 	    
 	   
 	   
 	   if (dir.isDirectory()){
 	      File[] files = dir.listFiles();
 	       for (int i = 0; i < files.length ; i++){
 	         if (files[i].isFile()){
 	        	 
 	            File file = files[i];
 	            if(file.getName().indexOf(ddd) != -1){ // kui on t�nase p�eva fail 
 	        		 
 	        	 
 	            File olemas = new File(dir + "/sent/" + file.getName());
 	        try {
 	          // 	copyFile(file,olemas);
 				//	file.delete();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 	            }	
 	             }
 		   
 	     }
 	   }	  
      }
   
   
  public void copyFile(File in, File out) throws Exception {
      FileInputStream fis  = new FileInputStream(in);
      FileOutputStream fos = new FileOutputStream(out);
      byte[] buf = new byte[1024];
      int i = 0;
      while((i=fis.read(buf))!=-1) {
        fos.write(buf, 0, i);
        }
      fis.close();
      fos.close();
      }


   
   private void sendEmail(String body,String subjekt,String to , String filename ){
 		Email mail = new Email(null);
 		mail.setSMTP(parent.getProps().getProperty("smtphost"));
 		mail.setFrom(parent.getProps().getProperty("from"));
 		  mail.setUser(parent.getProps().getProperty("smtpuser"));
           mail.setPass(parent.getProps().getProperty("smtppass"));
           mail.setPort(parent.getProps().getProperty("smtpport"));
         
 		mail.setSubjectFilesTo(subjekt);
 		mail.setEncoding("UTF-8");
 		
 		//mail.addAttach(f.getAbsolutePath());
 		mail.setSendNotice(false);
 		try {
 			mail.setBody(body);
 			if(filename != null){
 				mail.addAttach(filename);
 			}
 		
 		} catch (MessagingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		Calendar cc = Calendar.getInstance();
 		if(cc.get(Calendar.HOUR_OF_DAY) > 16){
 			//to = "products@bidvest.lt";
 		}
 		
 		
 		StringTokenizer st = new StringTokenizer(to,",");
 		while(st.hasMoreTokens()){
 			String mai = st.nextToken();
 			mail.setFilesTo(mai);
 			try {
 				mail.send();
 			} catch (AddressException e) {
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (MessagingException e) {
 				e.printStackTrace();
 			}
 		  }	
         }
   
   
   

   
   
   
   

    
   
 @Override
 public void PictureMaker() {
 	  sendReExReport();
 		  System.exit(0);
 }

@Override
public void FixReporter() {
	Converter.createExcelReport(this.getProducts(null), this.getCustomers(null), this.getCustomerProductsList(null), getPriceMatrixData(), this.getExcelDir(), rcompany, user, pass);
	System.exit(0);
}
  
  
}