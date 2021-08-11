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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HansaSoudalEstDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter{

  public HansaSoudalEstDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
	// String databaseDriverName = "com.hxtt.sql.dbf.DBFDriver";
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

	try {
	    Class.forName(databaseDriverName).newInstance();

	} catch (Exception e) {
	    parent.showError(e);
	}
    
  }

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

   	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
       public Vector getPriceMatrixData()  {
         Vector lines = new Vector();
         
         try{
        	    FileReader fr = new FileReader(getDir() + PRODUCT_PRICES_FILE);
                BufferedReader reader = new BufferedReader(fr);
              String line = null;
              while((line = reader.readLine()) != null){
             
              	
              	if(line.indexOf("pl2") != -1){
              	 	
              		line = reader.readLine();
              		 while((line = reader.readLine()) != null ){
              			 
              			if(line.trim().length()>10){
              			line = line.replaceAll("\t","| ");
              	//	 System.out.println(line);
              		  	  StringTokenizer st = new StringTokenizer(line, "|");
              		    st.nextToken();
                 	   st.nextToken();
                 	   st.nextToken();
              			MatrixLine mline = new MatrixLine();
                          mline.setCliGroup(st.nextToken().trim());
                          st.nextToken();
                          mline.setProdID(st.nextToken().trim());
                      double price = 0;
                      try{
                    	 price = Double.parseDouble(st.nextToken().replace(',', '.').replaceAll(" ", ""));
                      }catch(Exception e){
                    	  
                      }
                          mline.setPrice(price);
                      //  System.out.println(mline.getCliGroup() + " " + mline.getProdID());
                          if(mline.getCliGroup().equals("3")){
                        	 // hinnad.put(mline.getProdID(), mline.getPrice()+ "");
                          }
                       lines.add(mline);
              			 
              			 
              			 
              		 }	
              	   }
              	}
              }
         }catch(Exception e){
        	 e.printStackTrace();
         }

     
         
         
         
         
         
 	  /*   File f = new File(this.getDir() + "soodukad.xls" );
         
	    	FileInputStream ff = null;
			try {
			/*	ff = new FileInputStream(f);
			
			

			HSSFWorkbook workbook = null;
			try {
				workbook = new HSSFWorkbook((ff));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			HSSFSheet sheet = workbook.getSheet("info");
			 	    System.out.println("lastnum" +sheet.getLastRowNum());
			 	   // System.exit(0);

			for(int i = 1 ; i < sheet.getLastRowNum()+1;i++){
			
				HSSFRow row = sheet.getRow(i);
				boolean isOk = false;
				

		 	   
				java.util.Date start = null;
				java.util.Date end  = null;
				
				if(row.getCell((short)6)!= null){
					try{
						 start = row.getCell((short)6).getDateCellValue();
						// System.out.println("nimi" + row.getCell((short)3).getStringCellValue() + " " + row.getCell((short)1).getStringCellValue());
					}catch (Exception e ){
						System.out.println("nimi" + row.getCell((short)3).getStringCellValue() );
					//	System.exit(0);
					}
					
				}
				
				if(row.getCell((short)7)!= null){
					 end = row.getCell((short)7).getDateCellValue();
				}
				//System.out.println(start +" " + end + " ");
				//kui kuup�evi pole
				if(start == null && end == null){
					isOk = true;
				}
				
                //on algus aga l�ppu pole
				if(start != null && start.before(new java.util.Date()) && end == null){
					isOk = true;
				}
				//algust pole aga l�pp on
				if(start == null &&  end != null && end.after(new java.util.Date())){
					isOk = true;
				}
				//m�lemad on t�idetud
				if(start != null && start.before(new java.util.Date()) &&  end != null && end.after(new java.util.Date())){
					isOk = true;
				}
				
				if(isOk){
				
				String custID = "";
				try{
					custID = (int)(row.getCell((short)0).getNumericCellValue())+"";
				}catch (Exception e){
					//e.printStackTrace();
					try{
						custID = row.getCell((short)0).getStringCellValue() + "";
					}catch(Exception es){
						es.printStackTrace();
					}
					//custID = row.getCell((short)0).getStringCellValue() + "";
				}
				
				
				// System.out.println("last " + custID);
               String clGroup = " ";
               String prodID = "";
              // System.out.println();
           	try{
           		prodID = (int)(row.getCell((short)2).getNumericCellValue())+"";
			}catch (Exception e){
				//e.printStackTrace();
				
				try{
					prodID = row.getCell((short)2).getStringCellValue() + "";
				}catch(Exception aa){
					aa.printStackTrace();
				}
			}
               String prodGroup = " ";
               
               
               
               double discount = 0;
               if(row.getCell((short)5) != null){
               	
           	try{
           		discount = (row.getCell((short)5).getNumericCellValue());
			}catch (Exception e){
				try{
				discount = Double.parseDouble(row.getCell((short)5).getStringCellValue());
				}catch (Exception ea){
					ea.printStackTrace();
				}
			}

               }
			
               
               
               
			double price = 0 ;
			 if(row.getCell((short)4) != null){
			 
			try{
				price = (row.getCell((short)4).getNumericCellValue());
			}catch (Exception e){
				//e.printStackTrace();
				price = Double.parseDouble(row.getCell((short)4).getStringCellValue());
			}
			
			 }
			 
			 
			 
			 
			 
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
                 mline.setProdGroup(prodGroup);
               }
                 mline.setDiscount(discount);

               if (price != 0) {
                 mline.setPrice(price);
               }
               //System.out.println(mline.getCustID() + " " + mline.getProdID());
              lines.add(mline);
          	
				}
			}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
         return lines;
       }
       
   	private Date getDate(String kuup) {

		Calendar cal = Calendar.getInstance();
		int YEAR = Integer.parseInt(kuup.substring(7,10 ));
		int MONTH = Integer.parseInt(kuup.substring(4, 5))-1;
		int DAY = Integer.parseInt(kuup.substring(0, 2));
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
      		if(st.nextToken().indexOf("EEK") != -1){
      			eek = 15.64666;
      		}
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
    if(line.indexOf("IVVc")!= -1){
    
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
		
		for (int i =0;i<44;i++){
			if(st.nextToken().indexOf("EEK") != -1){
				eek = 15.64666;
			//	System.out.println("EEK" + id);
			}
		}
		
		if (st.hasMoreTokens()) {
			String sum = st.nextToken().trim().replaceAll(",",".").replaceAll(" ","");
			double summa = Math.round(Double.parseDouble(sum) *1.20 *100 / eek);
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
		
		
	
			debt.setDue(getDue(debt.getDuedate().getTime()));
		

			list.addElement(debt);
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

  
   private String getDue(long dd){
      String res = "0";
      res = ((-dd+(new java.util.Date()).getTime())/86400000) + "";
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
        FileReader fr = new FileReader(getDir() + PRODUCT_FILE);
        BufferedReader reader = new BufferedReader(fr);
      String line = null;

      while((line = reader.readLine()) != null){
      	
      	// System.out.println(line);
      	if(line.indexOf("ITVc1") != -1){
      		reader.readLine(); 
      		///System.out.println("###" + line);
      		while((line = reader.readLine()) != null && line.indexOf("artikel3") == -1){
      			/// System.out.println(line);
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
        	   st.nextToken();
        	   st.nextToken();
        	   st.nextToken();
          String id = st.nextToken().trim();
         
          if (st.hasMoreTokens()){
            String name = st.nextToken().trim();
            if (name.equals("")) name = "<" + id + ">";
            	 tm.put(id, clear(name));                 	
           
          }
          
        }
      }	
      		
  		}
      
      
      }
      } catch (Exception e){
        e.printStackTrace();
      }
      return tm;
   }

   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();

     try {
       FileReader fr = new FileReader(getDir() + AGENT_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
     	
     
     	if(line.indexOf("user2") != -1){
     		reader.readLine();
     		while((line = reader.readLine()) != null){
     		
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
    	   st.nextToken();
    	   st.nextToken();
    	   st.nextToken();
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
       }
     }	
     		
 		}
     
     
     }
     } catch (Exception e){
       e.printStackTrace();
     }
     tm.put("Kontor", new Agent("HOUSE"));
     return tm;
   }


   public TreeMap getTing(){
	     TreeMap tm = new TreeMap();

	     try {
	       FileReader fr = new FileReader(getDir() + "maksetin.TXT");
	       BufferedReader reader = new BufferedReader(fr);
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
	        FileReader fr = new FileReader(getDir() + CUSTOMER_PRODUCT_FILE);
	        BufferedReader reader = new BufferedReader(fr);
	      String line = null;
	      while((line = reader.readLine()) != null){
	  		 line = line.replaceAll("\t","; ");
	    		//System.out.println("XX " +line); 
	    		 StringTokenizer st = new StringTokenizer(line, ";");
	        if (st.hasMoreTokens()){
	        	 
	        	st.nextToken();
	        	String lastdate = st.nextToken().trim();
	        	long tehing = Calendar.getInstance().getTimeInMillis() -  getDate(lastdate).getTime();
	        	
	        	
	          String custID = st.nextToken().trim();
	      	st.nextToken();	st.nextToken();
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
	        }
	      }
	      } catch (Exception e){
	        e.printStackTrace();
	      }
	   
	   
	   

     TreeMap tm = new TreeMap();
     StringBuffer buf = new StringBuffer ();
     this.customers = new ArrayList();
   String confirm = getParent().getProps().getProperty("confirmemail");
     getDebt(null);
     try {
      TreeMap ting = getTing();
     	
       FileReader fr = new FileReader(getDir() + CUSTOMER_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
     	
     	
     	if(line.indexOf("cu4") != -1){
     		//reader.readLine();
  while((line = reader.readLine()) != null){
  	line = line.replaceAll("\t","| ");
  	line = line.replaceAll(";", ":");
 	  StringTokenizer st = new StringTokenizer(line, "|");
 //System.out.println("eee " + line);
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
        	  customer.setInfo(this.clear(cinfo));
          }
           st.nextToken();
           if (st.hasMoreTokens()){
             String aadress = st.nextToken().trim();
               if( aadress != null){
             	 aadress = clear(aadress);            
               customer.setAddress(aadress);
             }
           }
           if (st.hasMoreTokens()){
            String lisa = st.nextToken().trim();
            if( lisa != null){
              customer.setAddress(customer.getAddress() + " " + clear(lisa));
            }
          }
      
           
           name = clear(name);
             customer.setName((name + " " + clear(st.nextToken().trim())).trim());  

             
             
             
             if(confirm != null){
                 customer.setConfirmEmail(confirm); 
             }
             
             
           st.nextToken();
          
        String  aadress =  st.nextToken().trim();
           if( aadress != null && aadress.trim().length() > 0){
            customer.setAddress(clear(aadress));    	 
           }
      
      if (st.hasMoreTokens()){
       String lisa = st.nextToken().trim();
       if( lisa != null){
         customer.setAddress(customer.getAddress() + " " + clear(lisa));
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

                customer.setPersonalData(clear(info));	
           	}
            }
           st.nextToken();
           st.nextToken();
           st.nextToken();
          
          
           if (st.hasMoreTokens()){
           	String paym = st.nextToken().trim();
			if(paym.length()>0 && paym.trim().length()>0){
				
				if(ting.get(paym.trim()) != null ){
					customer.setPaymTerm((String)ting.get(paym.trim()));	
				}else{
					customer.setPaymTerm(paym.trim());
				}
				
		           	
			}
             }

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
        	   String agg = st.nextToken().trim();
        	 //  System.out.println("agg" + agg);
        	   if(agg.length()>0){
                   customer.addAgent(new Agent(agg));
                   customer.setCity(agg);
                   customer.addAgent(new Agent("HOUSE"));
        	   }
          }

           
           if (st.hasMoreTokens()){
           	String cred = st.nextToken().trim();
           	if(cred.length()>0 && cred.trim().length()>0){
           		customer.setCredit(cred.replaceAll(" ", "").replaceAll(",", "."));	
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
           String delivery = "";
           for(int i = 0 ;i<86;i++){
           String	toke = st.nextToken();
          // if(customer.getID().startsWith("012121") || customer.getID().startsWith("0084"))//nexilis
              // System.out.println(customer.getName() + " " + i + " " + toke);
           	if(i==9){
           tok = toke.trim();	
           	}
           	if(i==7){
                tok2 = toke.trim();	
                	}
        	if(i==6){
                pricelist = toke.trim();	
                	}
        	if(i==41){
                delivery = toke.trim();	
                	}
        	
           }
         //  if(customer.getID().startsWith("967"))//nexilis
        //  System.out.println(customer.getName() + " | blokk" + tok + " " +  tok2 + " " + pricelist + " " + delivery);

           if(delivery.trim().length()>0){
        	   customer.setAddress(delivery.trim());
           }
              
        	   
           if(pricelist.trim().length()>0){
              	customer.setGroup(pricelist);
              }
           
           if(tok2.trim().length()>0){
           	if(tok2.trim().length()>2){
           		tok2 = tok2.substring(0,2)+ "." + tok2.substring(2,tok2.length());
          		
           	}
           	double re = Double.parseDouble(tok2);    
           	customer.setDiscount(re);
           }
         //	System.out.println("Ale " + customer.getName() + " " +customer.getDiscount() + " " + tok);
          
         	customer.setPrimDeal(customer.getID());        

           if(overdue.containsKey(customer.getID())){
           	customer.setDebt(Double.parseDouble((String)overdue.get(customer.getID())));
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
     reader.close();
     fr.close();

     } catch (IOException e){
       e.printStackTrace();
     } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     
 
     
     if(buf.length()>2){
    	 
    	 String custfile = "929CUST_" + getYYYY() +  "." + (System.currentTimeMillis()+ "").substring(6,13);
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
     
     return tm;
   }

   
   private TreeMap getStocks(){
   	
   	TreeMap res = new TreeMap();
   	try{
   		/*
   	
   	FileReader fr = new FileReader(getDir() + "laoseis.TXT");
    BufferedReader reader = new BufferedReader(fr);
  String line = null;
  while((line = reader.readLine()) != null){
  	if(line.indexOf("purchase2") != -1){
  		//reader.readLine();
  		 while((line = reader.readLine()) != null ){
  		 	if(line.indexOf("Stock Balance") == -1 && line.trim().length()>0){
  		 	
  		 line = line.replaceAll("\t","; ");
  		// System.out.println("XX " +line); 
  		 StringTokenizer st = new StringTokenizer(line, ";");
  		 res.put(st.nextToken(),st.nextToken());
  		 
  		 
  		 
  		
  		 	}
  		  }
    }
  }
  
  */
  
  String[] files = (new File(getDir())).list();
	String file1 = null;
	String file2 = null;
  for(int i=0; i<files.length; i++){
 
  if(files[i].startsWith("STQT_")){
  	if(file1 == null){
  		 file1 = files[i];		
  	}else{
  		file2 = files[i];
  	}
  }
  }
  
 // System.out.println("files " + file2 + " " + file1);
  File f1 = new File(getDir() + file1);
  File f2 = new File(getDir() + file2);
  String file = null;
  if(f1.lastModified()>f2.lastModified()){
  	// System.out.println("1111");
  	file = file1;
  	f2.delete();
  }else{
  	 //System.out.println("2222");
  	file = file2;
  	f1.delete();
  }
  
  FileReader fr = new FileReader(getDir() + file);
  BufferedReader  reader = new BufferedReader(fr);
     String line = null;
    	String prod = null;
    	int stock = 0;
  while((line = reader.readLine()) != null){

  	if(line.indexOf("LIN_9#") != -1){
  		 prod = line.substring(35,70).trim();	
  	//	 System.out.println("prod " + prod + " " + stock);
    }
  	if(line.indexOf("QTY_9,11#               90") != -1){
//QTY_9,11#               90 +0000000000003000
 		 stock = Integer.parseInt(line.substring(28,41).trim());	
 		 res.put(prod,stock+"");
 		//System.out.println(line);
  		// System.out.println(prod + " " + stock);
      }
  	
      }
  reader.close();
  fr.close();
  		}catch (Exception e){
  	   	e.printStackTrace();	
  	   	}	
   	return res;
   }
   
  
   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     StringBuffer buf = new StringBuffer();
     try {
     	
     	this.getPriceMatrixData();
     	
     	TreeMap sto  = getStocks();
     //	TreeMap cost  = getCosts();
     	
     	
     	
       FileReader fr = new FileReader(getDir() + PRODUCT_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
     	
     	if(line.indexOf("artikel4") != -1){
     		 while((line = reader.readLine()) != null && line.indexOf("ITVc2") == -1){
     		
     		 line = line.replaceAll("\t","; ");
     		//System.out.println(line);
     		if(line.length()>100){
     		
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
           prod.setName( "    " + id + " " +clear(name));
           if(id.equals("106967")){
        	 //  System.out.println(prod.getName() );
           }
        //   System.out.println(prod.getName());
           if (st.hasMoreTokens()){
           	String unit = st.nextToken().trim();
           	if(unit.length()>0){
           		prod.setUnit(unit);
           	}
           	st.nextToken();
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
               		prod.setGroup(gr);
               	}
               	
               	
               	if(hinnad.get(prod.getID()) != null){
               	//	prod.setPrice(Double.parseDouble((String) hinnad.get(prod.getID())));
               	}
               	
           	String ii = "";

           	
           	
           	ii =	st.nextToken();
           	ii =st.nextToken();      
           	ii =st.nextToken();
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
          
           	ii =  st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken(); 
           	ii =	st.nextToken();
           	String ean2 =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();
           	ii =	st.nextToken();

           	String ean = st.nextToken().trim();
           	if(ean.trim().length()>0){
           		prod.setEAN(ean);
           	}
         	if(ean2.trim().length()>0){
           		prod.setEAN(ean2.trim());
           	}

           	st.nextToken();
           	st.nextToken();
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
               //System.out.println("CAse" + prod.getInCases());
             }
              }
             

           //	System.out.println("CAse" + st.nextToken());
             }
           }
         }
         
         prod.setStock(0);
         
         if(sto.get(prod.getID()) != null){
         	prod.setStock( Double.parseDouble(((String)sto.get(prod.getID())).replaceAll(" ","").replaceAll(",",".") ));
         }
       //  System.out.println("Out" + prod.getName()+ " " + out);
         if(prod.getName().length()>0){
         	if( !prod.getName().startsWith("-") && !out.trim().equals("1") && id.length()>3){
                  buf.append( prod.getID() + ";" + clear(name)  + "; ;" + prod.getUnit()+ "; ; ; ; ; ; ; ; ; ; ; ; " + System.getProperty("line.separator"));
         	 	  tm.put(prod.getName(), prod);
        	
         	}
         }
            
       }
     		 }	
      		}
     	}
     }
     } catch (IOException e){
       e.printStackTrace();
     }
     if(buf.length()>2){
    	 
    	 String prodfile = "929PROD_" + getYYYY() +  "." + (System.currentTimeMillis()+ "").substring(6,13);
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
     
     return tm;
   }
   
   
   private String getYYYY(){
	    Calendar cal = Calendar.getInstance();
	    StringBuffer sb = new StringBuffer();
	    String year =   cal.get(Calendar.YEAR) + "";
	    sb.append( year);
	    
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


   public Vector getCustomerProductsList(String agentCode){
     return custProds;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "HansaLumene";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }

   public double getSalary(String agentCode){
    
     try {
       FileReader fr = new FileReader(getDir() + AGENT_SALES_FILE);
       BufferedReader reader = new BufferedReader(fr);
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
       fr.close();
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

   private String getOutDir(){
	    String dir = getParent().getProps().getProperty("outdir");
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
      ret = ret.replaceAll("ä","11224xx");
      ret = ret.replaceAll("ö","11225xx");
      ret = ret.replaceAll("õ","11227xx");
      ret = ret.replaceAll("Ö","11228xx");
      ret = ret.replaceAll("Ä","11229xx");
      ret = ret.replaceAll("Õ","11226xx");
      ret = ret.replaceAll("ü","11112xx");
      ret = ret.replaceAll("Ž","11114xx");
      //ret = ret.replaceAll("","11119xx");
      
      ret = ret.replaceAll("11224xx","�");
      ret = ret.replaceAll("11225xx","�");
      ret = ret.replaceAll("11226xx","�");
      ret = ret.replaceAll("11227xx","�");
      ret = ret.replaceAll("11228xx","�");
      ret = ret.replaceAll("11229xx","�"); 
      ret = ret.replaceAll("11112xx","�");
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
      
      if(ret.indexOf("�") > -1 ){
    	  ret = ret.substring(0,ret.indexOf("�")) + "�" +ret.substring(ret.indexOf("�")+2 );
      }
 	
      if(ret.indexOf("�") > -1 ){
    	  ret = ret.substring(0,ret.indexOf("�")) + "�" +ret.substring(ret.indexOf("�")+2 );
      }
 	
      if(ret == null || ret.trim().length()==0){
      return " ";
      }else {
      return ret;
      }
    }
   
   public static final String SEPARATOR = "\t";
   public static final String PRODUCT_FILE = "tooted.TXT";
   public static final String PRODUCT_PRICES_FILE = "hinnad.TXT";
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
   public static final String DRIVER_VERSION = "HansaSoudalEst";
   private String	       url = "jdbc:odbc:aaaaa";
   private Connection	       con = null;

   private String	       subjectFilesTo = null;
   
}