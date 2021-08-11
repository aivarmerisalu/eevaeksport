package eevaeksport.driver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
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
import eevaeksport.DealerProduct;
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

public class LevisDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public LevisDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }
  public String getSaveDebtInfo() {
   	return null;
   }
   
   public String getPilotDayData(Agent agent){

    String sep= System.getProperty("line.separator");
    StringBuffer sb = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    String header = " ";
   	
    try {

        FileReader fr = new FileReader(getDir() + SALES_BILLS_FILE);
        BufferedReader reader = new BufferedReader(fr);
        String line = " ";
        boolean first = true;
         header = ("\n " + getParent().getString("todaysbills") + "\n\n" +getParent().getString("name") + "  \t\t" + getParent().getString("number") +
            "\t" + getParent().getString("sum") + sep +"-------------------------------------------" + sep);
  
      while( ((line = reader.readLine())!= null   && line.trim().length() > 0 )){
      	StringTokenizer st = new StringTokenizer(line, ";");
        
        //System.out.println(line);
        String agID = " ";
        String numb = " ";
        String sales = " ";
        String nimi = " ";
        if (st.hasMoreTokens()) {
          agID = clear(st.nextToken());
        }
      //  System.out.println(agID);
        
        if (st.hasMoreTokens()) {
        	 clear(st.nextToken());
          }
        
        if (st.hasMoreTokens()) {
        	numb = clear(st.nextToken());
          }
        if (st.hasMoreTokens()) {
        	nimi = clear(st.nextToken());
          }
        if (st.hasMoreTokens()) {
        	sales = clear(st.nextToken());
          }      
        
       if(agID.equals(agent.getID())){
       	if(first){
	    sb.append(header);
	    first = false;
       	}
       	
	      sb.append(nimi + "\t\n");
	      sb.append("\t\t" + numb + "\t");
	      sb.append(sales + sep + sep);       	
       }else{
       	sb2.append(line + "\n")	;
      	} 
      }
      sb.append(sep);  
      sb.append(sep); 
      fr.close();
      reader.close();
      File ff = new File(getDir() + SALES_BILLS_FILE);
      if(ff.exists()){
      	ff.delete();
      }
      
      if(sb2.toString().trim().length()>0){
      	FileOutputStream fos = new FileOutputStream(getDir() + SALES_BILLS_FILE);  
        fos.write(sb2.toString().getBytes());
        fos.close();	
      }
      
      
      } catch (Exception e){
      	e.printStackTrace();
      }
      
    //  System.out.println(sb.toString());
      
      
      
      if(sb.toString().trim().length()>0){
      	 return sb.toString();
      } else {
      	return null;
      }
   }
	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}

   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   	/*
   	try {
    FileReader fr = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader = new BufferedReader(fr);
  String line = null;

  while(!(line = reader.readLine()).equals("\"[Get DiscountMatrix]\"")){
  }
  line = reader.readLine();
    System.out.println("line" +line);
  while(((line = reader.readLine()).trim().length()) > 3 ){
  	
    line = line.replaceAll("\",\"", "|");
    line = line.replaceAll("\",", "|");
    line = line.replaceAll("\""," ");
 
    StringTokenizer st = new StringTokenizer(line, SEPARATOR);
    double price = 0;
	String custID = null;
	String clGroup = null;
	String prodID = null;
	String discGroup = null;
	double discount = 0;
		if(st.hasMoreTokens()){
			custID = cleardots(st.nextToken());
			
		}
				if(st.hasMoreTokens()){
		 clGroup = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				 prodID = cleardots(st.nextToken());	
			}
			if(st.hasMoreTokens()){
				discGroup = cleardots(st.nextToken());	
			}
			
			if(st.hasMoreTokens()){
				 discount = Double.parseDouble(cleardots(st.nextToken()).replaceAll(",","."));	
			}
			
		//	System.out.println("'" + custID + "'" + clGroup + "'" + prodID + "'" + discGroup + "'" + discount) ;
			if(st.hasMoreTokens()){
				price = Double.parseDouble(cleardots(st.nextToken()));	
			}
			MatrixLine mline = new MatrixLine();

			if (custID != null && custID.trim().length() > 0) {
				mline.setCustID(custID.trim());
			}
			if (mline.getCustID() == null && clGroup != null
					&& clGroup.trim().length() > 0) {
				mline.setCliGroup(clGroup.trim());
			}

			if (prodID != null && prodID.trim().length() > 0) {
				mline.setProdID(prodID.trim());
			}
			if (mline.getProdID() == null && discGroup != null
					&& discGroup.trim().length() > 0) {
				mline.setDiscGroup(discGroup);
			}
			mline.setDiscount(discount);

			if (price != 0) {
				mline.setPrice(price);
			}
			lines.add(mline);
		}

	  

	} catch (Exception ex) {
		ex.printStackTrace();
	}*/
	return lines;

   }

   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           try {

                   FileReader fr = new FileReader(getDir() + DATA_FILE);
                   BufferedReader reader = new BufferedReader(fr);
                 String line = null;

                 while(!(line = reader.readLine()).equals("\"[Get Fixed Assortment]\"")){
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
                  


                   if (custID != null && custID.trim().length() > 0) {
                     mline.setCustID(custID.trim());
                   }
                   if (st.hasMoreTokens()) {
                    prodID = clear(st.nextToken());
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

   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {

   	DealerProduct dealerProds = new DealerProduct();
    try {

            FileReader fr = new FileReader(getDir() + DATA_FILE);
            BufferedReader reader = new BufferedReader(fr);
          String line = null;

          while(!(line = reader.readLine()).equals("\"[Get Stocks]\"")){
          }
          //line = reader.readLine();

           while((line = reader.readLine()) != null && (line.trim().length()) > 0 ){
                line = line.replaceAll("\",\"","|");
            //    System.out.println(line);
           	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
           	//   

            String toode = null;
            double seis = 0;
            String seisu = null;
            String ladu  = null;
           	if (st.hasMoreTokens()) {
             	toode = clear(st.nextToken());
             }
           	if (st.hasMoreTokens()) {
           		seisu = clear(st.nextToken());
           		if(seisu.equals(" ")){
           			seisu = "0";
           		}
             	seis = Double.parseDouble(seisu);
             }

           	if (st.hasMoreTokens()) {
             	ladu = clear(st.nextToken());
             }
           	if(ladu.equals("01")){
                dealerProds.add(ladu, toode,seis+""," "," " );	
           	}

              // 	System.out.println("toode" + toode+ "Stock" +ladu + "warehouse" + seis);
            }

    } catch (Exception e) {
        getParent().showError(e);
    }

     return dealerProds;
    // return null;
   }




       public String getSaveStockInfo(){
       return null;
       }

  private Date getDate(String mmddyy){
  	long lon = 0;
  	Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, Integer.parseInt("20" + mmddyy.substring(6, 8)));
    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(3, 5))-1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(0, 2)));
  // System.out.println(cal.getTime()); 
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
     //setTreeGroup(true);
     try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
    while(!(line = reader.readLine()).equals("\"[Get ProdGroups1]\"")){
    }
    //line=reader.readLine();
  //  System.out.println("size" + groups.size());
    while(((line = reader.readLine()).trim().length()) > 0 ){
       line = line.replaceAll("\",\"","| ");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
          String id = clear(st.nextToken());
                if (st.hasMoreTokens()){
                 String name = clear(st.nextToken());
                 if(name.trim().length()>0 && groups.contains(id)){
                 tm.put(cleardots(id),clear(name));
                 }
            }
          }
        }
      } catch (IOException e){
        e.printStackTrace();
      }
         return tm;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
   	TreeMap   tm = new TreeMap();
    //setTreeGroup(true);
    try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;
   while(!(line = reader.readLine()).equals("\"[Get ProdGroups2]\"")){
   }
   //line=reader.readLine();
   while(((line = reader.readLine()).trim().length()) > 0 ){
      line = line.replaceAll("\",\"","| ");
      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = clear(st.nextToken());
               if (st.hasMoreTokens()){
                String name = clear(st.nextToken());
                if(name.trim().length()>0){
                tm.put(cleardots(id),clear(name));
                }
           }
         }
       }
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

   
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
    line = reader.readLine();
     
      while(((line = reader.readLine()).trim().length()) > 0 ){

           line = line.replaceAll("\",\"","|");
         //  System.out.println("line" +  line);
      	StringTokenizer st = new StringTokenizer(line, SEPARATOR);

        if (st.hasMoreTokens()) {
          String id = clear(clear(st.nextToken()));
          Agent agent = new Agent(id);
          if (st.hasMoreTokens()) {
            String name = clear(st.nextToken());
            if (name.equals("")){ name = "<" + id + ">";}
            agent.setName(name);
            agent.setIsPilot(true);
            if (st.hasMoreTokens()) {
              String objekt = clear(st.nextToken());
              agent.setObject(objekt);
            
             
            }

            FileReader fr2 = new FileReader(getDir() + "aggroups.txt");
            BufferedReader reader2 = new BufferedReader(fr2);
          String line2 = null;

          while((line2 = reader2.readLine())!= null ){
              StringTokenizer st2 = new StringTokenizer(line2, ";");
           
               String group = clear(st2.nextToken());
               st2.nextToken();
               while(st2.hasMoreTokens()){
               	String ag = st2.nextToken();
               	if(ag.equals(id)){
               	   //System.out.println(ag + " " +id);
               	 	agent.addProdGroupList(group); 		
               	}
              }
          }
            
            tm.put(agent.getName(), agent);
          }
        }
      }


 /*Loe sisse kliendi pysi allalhindlused
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
*/
 // loe sisse yle Agentide grupid


     } catch (Exception e){
       e.printStackTrace();
     }
     return tm;
   }




       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){

   try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
        BufferedReader reader = new BufferedReader(fr);
      String line = null;
      Date datenow = new Date(Calendar.getInstance().getTimeInMillis());

      while(!(line = reader.readLine()).equals("\"[Get Debt]\"")){
     // while((line = reader.readLine()).startsWith("[Get Debt Information]")){
      }
      //line = reader.readLine();

       while((line = reader.readLine())!= null && line.trim().length()>0 ){
       
       line = line.replaceAll("\",\"","|");
          // System.out.println(line);
       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       	//   	"klient","number","kuup�ev","summa"
         if (st.hasMoreTokens()) {
           String custID = clear(st.nextToken());
           Debt debt = new Debt();
           debt.setCustID(custID);
           if (st.hasMoreTokens()) {
             String bill = clear(st.nextToken());
             debt.setDoknr(bill);
             if (st.hasMoreTokens()) {
               String date = clear(st.nextToken());
               Date due = getDate(date);
               debt.setDuedate(due);
               long days = datenow.getTime()-due.getTime();
               int dueint = Integer.parseInt((days/86400000)+"");
               
               debt.setDue(dueint + "");

               }

             if (st.hasMoreTokens()) {
                String sum = clear(st.nextToken());
                debt.setSum(Double.parseDouble(sum));
              }

            this.debtList.add(debt);
           // System.out.println();
           }
         }
       }

      } catch (Exception e){
        e.printStackTrace();
      }


     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     while(!(line = reader.readLine()).equals("\"[Get Customers]\"")){
     }
    // line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");
       // System.out.println(line);
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
        String id = clear(st.nextToken());
        Customer customer = new Customer(id);
         if (st.hasMoreTokens()){
         	 String payer = null;
            	payer = clear(st.nextToken());
                customer.setPayer(payer);
         	//System.out.println(line);

           
           if (st.hasMoreTokens()){
           	String agen = clear(st.nextToken());
           	if(agen.length()>2 && agen.length() < 4){
           		agen = agen.substring(0,2);
           	}
            customer.addAgent(new Agent(agen));
            if (customer.getID().equals("0011281")) {
              Enumeration enums = getParent().getAgentsHash().elements();
            	  while (enums.hasMoreElements()) {
            	    Agent a = (Agent) enums.nextElement();
            	    customer.addAgent(a);
            	  }
               }
          }

           if (st.hasMoreTokens()){
           	String name = clear(st.nextToken());
            
            if (name.equals("")) name = "<" + id + ">";
            customer.setName(name);
            
           	
           	 }

           if (st.hasMoreTokens()){
           	
           String aadress = clear(st.nextToken());
           if( aadress != null){
             customer.setAddress(aadress);
             customer.setName(customer.getName()+ " " + aadress);

           }
             //System.out.println(customer.getAddress());
           }

           if (st.hasMoreTokens()){
            customer.setDebt(Double.parseDouble(clear(st.nextToken()))); 
           }

           String visit = null;
           if (st.hasMoreTokens()){
           	visit = clear(st.nextToken());
              if (visit!= null && visit.trim().length()>0){
              //	customer.setVisit(visit.trim());
              }
           }

           if (st.hasMoreTokens()){
           	String num = clear(st.nextToken());
           	if(num.trim().length()>0 && discounts.get(num) != null ){
             customer.setDiscount(Double.parseDouble((String)discounts.get(num)));

           	}
           }
           if (st.hasMoreTokens()){
           	String group = clear(st.nextToken());
           	if(group.trim().length()>0  ){
             customer.setGroup(group);
           	}
           }
           
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           
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


   public TreeMap getProducts(Agent agent){
    HashMap kliendid = new HashMap();
    Vector prods = new Vector();
    try {
      FileReader fr = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader = new BufferedReader(fr);
   String line = null;
  
  //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

  while(!(line = reader.readLine()).equals("\"[Get Customer Products]\"")){
  }
 // line=reader.readLine();
 // while(((line = reader.readLine())) != null ){

   while(((line = reader.readLine()).trim().length()) > 0 ){
  	
     line = line.replaceAll("\",\"","|");

    // System.out.println(line);
     
     StringTokenizer st = new StringTokenizer(line, SEPARATOR);
      if (st.hasMoreTokens()){
        String custID = clear(st.nextToken());
              if (st.hasMoreTokens()){
                String prodID = clear(st.nextToken());
                double lastamount = 0;
                double amount = 0;
                String lastdate = " ";
                if (st.hasMoreTokens()){
                   lastamount = Double.parseDouble(clear(st.nextToken()));
               }
                if (st.hasMoreTokens()){
                    lastdate = clear(st.nextToken());
                }

                if (st.hasMoreTokens()){
                    lastamount = Double.parseDouble(clear(st.nextToken()));
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
                    prods.addElement(prodID);
                CustProd cp = new CustProd(custID, prodID,lastamount , amount, 0,lastdate,0,0,0," ");

                cust.addProduct(prod,cp,false);
              //  System.out.println("cust" + custID + " prod " + prodID);
                kliendid.put(custID, cust);
                  vec.addElement(cp);

          }
        }
      }


    } catch (IOException e){
      e.printStackTrace();
    }
     TreeMap tm = new TreeMap();
     HashMap prodHash = new HashMap();
     
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
       String line = null;
      int count = 0;
    
      while(!(line = reader.readLine()).equals("\"[Get Products]\"")){
       }
     //line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");
        line = line.replaceAll("\",","| ");
        count++;
       // line = line.replaceAll(",","| ");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = clear(st.nextToken());

         Product prod = new Product(id);
         
         	String name = clear(st.nextToken());
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
         	//System.out.println(line);
            //prod.setName(clear(name));
         	name = id.substring(0,3) + " " + name;
            prod.setName(name + " " + id);
           if (st.hasMoreTokens()){
           	String pr =st.nextToken().replace(',','.');
           	if(pr.equals("  ")){
           		pr = "0";
           	}
             double price = Double.parseDouble(clear(pr));
             if (price > 0){
               prod.setPrice(price);
               }
               if (st.hasMoreTokens()){
                prod.setInCases(Double.parseDouble(clear(st.nextToken())));
                if (st.hasMoreTokens()){
                	//clear(st.nextToken());
                   	prod.setStock(Double.parseDouble(clear(st.nextToken())));
                    }
                     if (st.hasMoreTokens()){
                       prod.setGroup(clear(st.nextToken()));
                       if(!groups.contains(prod.getGroup())){
                       	groups.addElement(prod.getGroup());
                       }
                       if (st.hasMoreTokens()){
                        prod.setGroupSecond(clear(st.nextToken()));
                         if (st.hasMoreTokens()){
                         	String unit =clear(st.nextToken());
                         	if(unit.length()>1){
                         		prod.setUnit(unit.substring(0,2));                               	
                         	}
                            if (st.hasMoreTokens()){

                        		//System.out.println(prod.getName() + "'" + line + "'");
                            	String ddd = clear(st.nextToken());
                            	if(ddd.trim().length()>0){
                            	
                            	double oh = Double.parseDouble(ddd);
                            	if(oh>0){
                            	
                            	double kate = (1-oh/prod.getPrice())*100;
		                                  if(kate>0){
		                            		String kat = (kate + "");
		                                    prod.setMargin(kat.substring(0,kat.indexOf(".")));   	
		                            	   }
                            			}
                            		}
                            	}
                    }
                 }
               }
             }
           }
           
           
         if (prod.getStock() > 1 || (prods.contains(prod.getID()) && prod.getStock() == 1 ) ){
         	prod.setStock(-1);
         	 prodHash.put(prod.getID(), prod);
             tm.put(prod.getName(), prod);	
         }
        }
     }
     

     
      fr = new FileReader(getDir() + DATA_FILE);
      reader = new BufferedReader(fr);
  
  while(!(line = reader.readLine()).equals("\"[Get BarCodes]\"")){
   }
   while(((line = reader.readLine()).trim().length()) > 5 ){
    line = line.replaceAll("\",\"","| ");

    StringTokenizer st = new StringTokenizer(line, SEPARATOR);
    if (st.hasMoreTokens()){
    	 String id = clear(st.nextToken());
    	 if(st.hasMoreTokens()){
    	 
    	 String code = clear(st.nextToken());
    	 
    	 if(code.startsWith("000")){
    	 	code = code.substring(2,code.length());
    	 	
    	 }
    	 if(code.startsWith("00")){
    	 	code = code.substring(1,code.length());
    	 }
         Product prod1 = (Product)prodHash.get(id);
         if (prod1 != null){
            if(prod1.getEAN()== null || prod1.getEAN().trim().length()== 0){
            	prod1.setEAN(code);
    //            prod1.addBarcode(code);
            }else{
                prod1.addBarcode(code);
             }
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

    try {
      FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
   while(!(line = reader.readLine()).equals("\"[Get Products]\"")){
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
    

     return vec;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "ArcoTXT";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
  private String clear (String ss){
    String ret =   ss.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace(',','.');
    ret =  ret.replace('"',' ').trim();
    ret =  ret.replace('"',' ').trim();
    ret =  ret.replace('"',' ').trim();
    ret = ret.replaceAll("�","a");
    ret = ret.replaceAll("�","e");
    ret = ret.replaceAll("�","n");
    ret = ret.replaceAll("�","i");
    ret = ret.replaceAll("�","l");
    ret = ret.replaceAll("�","k");
    ret = ret.replaceAll("�","K");
    ret = ret.replaceAll("�","E");
    ret = ret.replaceAll("�","I");
    ret = ret.replaceAll("�","U");
    ret = ret.replaceAll("�","A");
    ret = ret.replaceAll("�","S");
    ret = ret.replaceAll("�","L");
    ret = ret.replaceAll("�","N");
    ret = ret.replaceAll("�","C");
    ret = ret.replaceAll("�","g");
    ret = ret.replaceAll("�","G");
    
    
    
    
    ret = ret.replaceAll("�","e");
    ret = ret.replaceAll("�","I");
    ret = ret.replaceAll("�","u");
    ret = ret.replaceAll("�","c");
    ret = ret.replaceAll("�","z");
    ret = ret.replaceAll("�","s");
    ret = ret.replaceAll("�","S");
    ret = ret.replaceAll("�","c");
    ret = ret.replaceAll("�","I");
    ret = ret.replaceAll("�","u");
    ret = ret.replaceAll("�","l");
    ret = ret.replaceAll("�","C");
    ret = ret.replaceAll("�","Z");
    ret = ret.replaceAll("�","E");
    ret = ret.replaceAll("�","U");
    ret = ret.replaceAll("�","U");
  
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


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private  Vector vec = new Vector();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private Vector groups = new Vector();
  

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {

  }
}
