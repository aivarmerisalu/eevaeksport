package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HammerNaviDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public HammerNaviDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }

   public String getPilotDayData(Agent agent){
     return null;
   }

   public Vector getPriceMatrixData() {
   	return null;
   }
   
   public String getSaveDebtInfo() {
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

   	DealerProduct dealerProds = new DealerProduct();
   	System.out.println("teen Delerprodsi");
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
         //   System.out.println(line);
       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       	//   	           	"toode","Stock","warehouse"

        String toode = null;
        double seis = 0;
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
    	
       	if(  ladu.indexOf("RNU L") != -1 && seis > 0.5) //ladu != null &&(ladu.indexOf("TALLINN") != -1 || ladu.indexOf("TARTU L") != -1 ||
       	dealerProds.add(ladu, toode,seis+""," "," " );
       	}
      }
       }catch (Exception e){
       	e.printStackTrace();	
       	}
     
   	
   	System.out.println("l�pp");
   //	System.exit(0);
    return dealerProds;
 
   }





  private Date getDate(String mmddyy){
  	long lon = 0;
  	Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, Integer.parseInt("20" + mmddyy.substring(6, 8)));
    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(3, 5))-1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(0, 2)));
    //System.out.println(cal.getTime());  05.02.14
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
    setTreeGroup(true);
    try {
    	
       FileReader fr = new FileReader(getDir() + GROUP_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;
  // while(!(line = reader.readLine()).equals("[Get Prod Groups]")){
  // }
   line=reader.readLine();
   while(((line = reader.readLine()).trim().length()) > 0 ){
      line = line.replaceAll("\",\"","| ");
      StringTokenizer st = new StringTokenizer(line, ";");
       if (st.hasMoreTokens()){
         String id = clear(st.nextToken());
               if (st.hasMoreTokens()){
                String name = clear(st.nextToken());
                if( id.trim().length()>0) {
                tm.put(cleardots(id),  Converter.enCodeEst(name));
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
  /*  TreeMap   tm = new TreeMap();
    try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   while(!(line = reader.readLine()).equals("[Get Prod Groups]")){
   }
   line=reader.readLine();
   while(((line = reader.readLine()).trim().length()) > 0 ){
      line = line.replaceAll("\",\"","|");
      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String groupID = clear(st.nextToken());
               if (st.hasMoreTokens()){
                String name = clear(st.nextToken());
                if(groupID.length()>5){
                   tm.put(cleardots(groupID),cleardots(groupID) + " " +name);
                }
           }
         }
       }


     } catch (IOException e){
       e.printStackTrace();
     }*/
       // return tm;
   	return null;
  }


   private String cleardots(String idx){
   	while(idx.indexOf(".")>0){
    idx = idx.substring(0,idx.indexOf("."))+idx.substring(idx.indexOf(".")+1);
   	}
	   return idx;
   }

   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();

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

    try {
        FileReader fr = new FileReader(getDir() + DATA_FILE);
        BufferedReader reader = new BufferedReader(fr);
      String line = null;
      Date datenow = new Date(Calendar.getInstance().getTimeInMillis());
      this.debtList = new Vector();
      while(!(line = reader.readLine()).equals("[Get Debt Information]")){
     // while((line = reader.readLine()).startsWith("[Get Debt Information]")){
      }
      line = reader.readLine();

       while(((line = reader.readLine()).trim().length()) > 0 ){
            line = line.replaceAll("\",\"","|");
           // System.out.println(line);
       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       	
         if (st.hasMoreTokens()) {
           String custID = clear(st.nextToken());
           Debt debt = new Debt();
           debt.setCustID(custID);
           boolean add = true;
  
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
               int old = 0;
               if(debtDays.get(custID) != null){
                old = Integer.parseInt((String)debtDays.get(custID));

               }
               if(old<dueint){
              this.debtDays.put(custID,dueint+"") ;

               }
               }

             if (st.hasMoreTokens()) {
                String sum = clear(st.nextToken());
                debt.setSum(Double.parseDouble(sum));
              }

             if (st.hasMoreTokens()) {
              clear(st.nextToken());
                //debt.setCustID(id);
              }
             	if(add){
                    this.debtList.add(debt);	
             	}
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

     while(!(line = reader.readLine()).equals("[Get Customers]")){
     }
     line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");
        //System.out.println(line);
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String name = clear(st.nextToken());
         if (st.hasMoreTokens()){
           String id = clear(st.nextToken());
           //System.out.println(line);
           Customer customer = new Customer(id);
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.setName(name);
           if (st.hasMoreTokens()){
           	payer = st.nextToken();
           	payer = id;
           	int ii = payer.indexOf("#");
          //System.out.println(payer);
           	if(ii != -1){
           		payer = payer.substring(0,ii);
           		
           	}
             customer.setPayer(payer);
            // System.out.println(payer);
             //customer.setPayer(id); //see onlisatud et payeri m�istet poleks
             //System.out.println(customer.getPayer());
           }

           if (st.hasMoreTokens()){
             String aadress = clear(st.nextToken());
             if( aadress != null){
               customer.setAddress(Converter.adressLink(Converter.enCodeEst(aadress),"Estonia"));
               int ii = customer.getName().indexOf(" O");
               if(customer.getName().indexOf(" O") != -1 && customer.getName().indexOf("OIL") == -1){
               	customer.setName(customer.getName().substring(0,ii)+ " O�" + customer.getName().substring(ii+3,customer.getName().length()));
           	    	
               }
               int iii = customer.getName().indexOf(" T");
               if(customer.getName().indexOf(" T") != -1 && customer.getName().indexOf(" TA") == -1 && customer.getName().indexOf(" TE") == -1
               		&& customer.getName().indexOf(" T�") == -1 && customer.getName().indexOf(" TO") == -1 && customer.getName().indexOf(" Tu") == -1){
               	customer.setName(customer.getName().substring(0,iii)+ " T�" + customer.getName().substring(iii+3,customer.getName().length()));
           	    	
               }
               customer.setName(Converter.enCodeEst(customer.getName()+ " " + aadress));
             }
             //System.out.println(customer.getAddress());
           }

           if (st.hasMoreTokens()){
           	String ag = st.nextToken();
           	if(ag.trim().length()>0){
           		
           	
             customer.addAgent(new Agent(clear(ag)));
             customer.addAgent(new Agent("ANDI"));
             
             if (ag != null && ag.trim().length()>0){
				Agent agg = ((Agent)this.getAgents1().get(ag.trim()));
				//System.out.println(agg);
				if(agg != null && agg.getDubbler().size()>0){
					for (int i = 0 ; i < agg.getDubbler().size() ; i++){
						customer.addAgent(new Agent((String)agg.getDubbler().elementAt(i)));
						//System.out.println(customer.getName() + customer.getAgents());
					}
				}
			}
           	}
             //System.out.println(customer.getAgents());
           }

           String paym = null;
           if (st.hasMoreTokens()){
           	paym = clear(st.nextToken());
            paym = paym.replaceAll("D"," ");
            paym = paym.replaceAll("P�EVA"," ");
            paym = paym.replaceAll("TAEM"," ");
            paym = paym.replaceAll("P"," ");
            paym = paym.replaceAll("KP"," ");
            paym = paym.replaceAll("K"," ");
            paym = paym.replaceAll("SR","-1").trim();
         //    System.out.println(paym);
           }

           if (st.hasMoreTokens()){
           	String num = clear(st.nextToken());
     
           	num = num.replaceAll("%"," ");    
           	if(num.trim().length()==3)num = num.substring(1,3);
           //	System.out.println(num);
           	num = num.replaceAll("A"," ");
           	num = num.replaceAll("B"," ");
           	num = num.replaceAll("C"," ");
           	num = num.replaceAll("D"," ");
           	num = num.replaceAll("E"," ");
           	num = num.replaceAll("F"," ");
           	num = num.replaceAll("G"," ");
           	num = num.replaceAll("H"," ");
           	num = num.replaceAll("I"," ");
           	num = num.replaceAll("J"," ");
           	num = num.replaceAll("P"," ");
           	num = num.replaceAll("K"," ");
           	num = num.replaceAll("T"," ");
           	num = num.replaceAll("M"," ");
           	num = num.replaceAll("O"," ");
           	num = num.replaceAll("�"," ");
          //	System.out.println(num);	
          	if(num.trim().length()>0){
                customer.setDiscount(Double.parseDouble(num.trim()));	
          	}

           }


           if (st.hasMoreTokens()){
             if(clear(st.nextToken()).equals("1")){
               customer.setSalesBlock(true);
             }
           }
       	String debti = null;
        String used = "0";
       	if (st.hasMoreTokens()){
           	 debti = clear(st.nextToken());

         //  	System.out.println("debti" + id + overdebts.containsKey(id)+overdebts.containsKey(payer));
           		if (overdebts.containsKey(id)){
           		debti = ((Debt)overdebts.get(id)).getDoknr();
                           used = ((Debt)overdebts.get(id)).getDue() ;
           		}
           		if (overdebts.containsKey(payer)){
               		debti = ((Debt)overdebts.get(payer)).getDoknr();
                     used = ((Debt)overdebts.get(payer)).getDue() ;
               		}

           }


           if (st.hasMoreTokens()){
            customer.setVisit(clear(st.nextToken()));
            //System.out.println(customer.getVisit());
          }
         //  if (st.hasMoreTokens()){
           // customer.setGroup(clear(st.nextToken()));
            //System.out.println(customer.getVisit());
          //}
           if (st.hasMoreTokens()){
            customer.setCustType(clear(st.nextToken()));

          }
           if (st.hasMoreTokens()){
           	String bll = clear(st.nextToken());
           	if (customer.getSalesBlock()){
              	 customer.setSalesBlock(false);
           	int extra = Integer.parseInt(bll);
           		if(debtDays.get(customer.getID())!= null && customer.getPaymTerm().trim().length()>0 &&
           				(extra < Integer.parseInt((String)debtDays.get(customer.getID())) )){

               		//System.out.println(customer.getID()+" "+extra+" "+debtDays.get(customer.getID()));
           	 customer.setSalesBlock(true);
           	}
          }
           }
           if (st.hasMoreTokens()){
            customer.setGroup(clear(st.nextToken()));
          }

           if (st.hasMoreTokens()){
           	String yes = clear(st.nextToken());

           	if (yes != null && !yes.trim().equals("0")){
                customer.setNodebt(true);
           	}
          }
           if (st.hasMoreTokens()){
           	String yes = clear(st.nextToken());

           	if (yes != null && yes.trim().length()>0){
                customer.setPersonalData("Kontakt tel.:" + yes);
              //  System.out.println("wwww" + customer.getPersonalData());
           	}
           	
          }

           if(paym.trim().length()==0){
           	paym = " ";
           }
          // if (!customer.isNodebt()){
           if (debti.trim().length()>0){
           	customer.setDebt(Double.parseDouble(debti));
        	
           }
            customer.setPaymTerm(paym);
        	customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));
           if(customer.getAgents().size()>0){
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           }
           
         }
       }
     }

     reader.close();
     fr.close();

     } catch (IOException e){
       e.printStackTrace();
     }
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
	   this.getCustomerProductsList(null);
	   System.out.println("custprodtehtud alustan");
     TreeMap tm = new TreeMap();
     TreeMap sto = new TreeMap();
     TreeMap par = new TreeMap();
   
     
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
  
           
      /*  if(ladu != null && ladu.indexOf("RNU LADU")>-1 && seis > 0){
            //	if((ladu.equals("ABT LADU") || ladu.equals("HULGI")) && seis > 0){
        		if(par.containsKey(toode)){
        			double seiss = seis + Double.parseDouble((String)par.get(toode));
        			par.put(toode,seiss+"");
        		}else{
        			par.put(toode,seis+"");   
        		}
        	}
       */
       }	}
 
       
    

} catch (Exception e) {
    getParent().showError(e);
}

     
     
     
     
     
     
System.out.println("laoseisud tehtud");
     
     
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
         String weight ="";
         if(id.trim().length()>0){
         	
         
         Product prod = new Product(id);
     try{
     	
     
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
        //	System.out.println(line);

           }
           	String pr =st.nextToken().replace(',','.');
            //System.out.println("price" + pr);
             double price = 0;
             if(pr.trim().length()>0){
             	price = Double.parseDouble(clear(pr));
             }
             if (price > 0){
               prod.setPrice(price);}
               if (st.hasMoreTokens()){
               	st.nextToken();
               // prod.setGroup(cleardots(clear(st.nextToken())));
                prod.setGroup(id.substring(0,2));
                if (st.hasMoreTokens()){
                    prod.setGroupSecond(cleardots(clear(st.nextToken())));
                    }
                     if (st.hasMoreTokens()){
                       prod.setUnit(clear(st.nextToken()));
                       // System.out.println("ladu"+prod.k());
                       if (st.hasMoreTokens()){
                         String ean = clear(st.nextToken());
                         if (ean.trim().length()>0){
                          // prod.setEAN(ean.trim());
                           if (ean.trim().length()>13){
                           //	prod.setEAN(ean.substring(0,13));
                           }
                         }
                         if (st.hasMoreTokens()){
                           st.nextToken();
                        /* int xx = Integer.parseInt(xxx);
                         if(xx == 1){
                         	prod.setStatusTop("1");
                         }else if (xx == 2){
                           prod.setMustBe("1");
                         }
                         else if (xx == 3) {
                           prod.setMustBe("1");
                        	prod.setStatusTop("1");
                         }
                         else if (xx == 4) {
                            prod.setStatusNew("1");
                          }else if (xx == 5) {
                            prod.setStatusSellout("1");
                         }*/
                         if (st.hasMoreTokens()){
                         	String discou = clear(st.nextToken());
                         	if(discou != null && discou.trim().length()>0){
                         		//System.out.println(line);
                            prod.setMaxDiscount(Double.parseDouble(discou));
                         	}
                         String cases = clear(st.nextToken());
                            if (st.hasMoreTokens()){
                             	cases = clear(st.nextToken());
                             	if(cases != null && cases.trim().length()>0){
                             	//	System.out.println(line);
                              //  prod.setInCases(Double.parseDouble(cases));

                                if (st.hasMoreTokens()){
                                	 weight = clear(st.nextToken());
                                         if(weight != null && weight.trim().length()>0){
                                           weight = weight.trim();
                                         }

                                }
                                if (st.hasMoreTokens()){
                               	 String vat = clear(st.nextToken());
                                        if(vat != null && vat.trim().length()>0){
                                          vat = vat.replaceAll("KM","");
                                          vat = vat.replaceAll("%","");
                                          	prod.setVat("20");
                                          
                                        	
                                    }
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
                                            			if (st.hasMoreTokens()){
                                            				st.nextToken();
                                                			if (st.hasMoreTokens()){
                                                    			st.nextToken();
                                                    			if (st.hasMoreTokens()){
                                                        			st.nextToken();
                                                        			if (st.hasMoreTokens()){
                                                            			String oh =st.nextToken().replace(',','.');
                                                                        //System.out.println("price" + pr);
                                                                         double cost = 0;
                                                                         if(oh.trim().length()>0){
                                                                         	cost = Double.parseDouble(clear(oh));
                                                                         }
                                                                         
                                                                         
                                                                         if(prod.getPrice() != 0 && cost != 0){
                                                                        	prod.setMargin(round((1-cost/prod.getPrice())*100,2) +"");
                                                                            prod.setPartii("OH:" + getParent().getNF().format(cost));
                                                                         }
                                                            		//	 (case when tp_myyntihinta=0 " +
                                                            				//	 " or tp_ostohinta=0 then 0 else round((1-tp_ostohinta/tp_myyntihinta)*100,2) end)
                                                            			
                                                                    }  
                                                                }   
                                                            }   
                                                        }   
                                                    }  
                                                }  
                                            }  
                                          }
                                        
                                        
                               }
                            }
                        }
                     }
                   }
                 }
               }
             }
               
           
          // prod.setName(id.substring(0,2) + " " +name);
           prod.setName("   " + id + " " + Converter.enCodeEst(name));
           
         }
         
         
     }catch (Exception e){
     	e.printStackTrace();
     }
         if(sto.containsKey(id)){
         	prod.setStock(Double.parseDouble((String)sto.get(id)));
         }
        
         
         boolean goes = false ;
         if(prod.getStock()>0){
        	 goes = true;
         }
             
         if(sold.contains(prod.getID()) ){ //
        	 goes = true;
         }
         
        
         
         

         
         if(prod.getName()!= null && prod.getName().trim().length()>3 && getallow(prod)){	
        
         
             /*if(sto.containsKey(id)){
              	String xxx = (String)sto.get(id);
              	prod.setPartii(prod.getPartii() + " Tal"  + xxx.substring(0,xxx.indexOf(".")));
              }
             
             if(par.containsKey(id)){
             	String xxx = (String)par.get(id);
             	prod.setPartii(prod.getPartii() +" P�r" + xxx.substring(0,xxx.indexOf(".")));
             }
        	*/ 
        	 
        	 tm.put(prod.getName(), prod);	           	
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

   private boolean getallow(Product prod){
       
       if(        prod.getID().startsWith("93/") 
      		 || prod.getID().startsWith("97/")
      		 || prod.getID().startsWith("99/")
      		 || prod.getID().startsWith("95/")
      		 || prod.getID().startsWith("89/02")
      		 || prod.getID().startsWith("89/03")
      		 || prod.getID().startsWith("89/04")
      		 || prod.getID().startsWith("89/05")
      		 || prod.getID().startsWith("89/06")
      		 || prod.getID().startsWith("89/07")
      		 || prod.getID().startsWith("89/08")
      		 || prod.getID().startsWith("89/09")
      		 || prod.getID().startsWith("89/1")
      		 || prod.getID().startsWith("89/2")
      		 || prod.getID().startsWith("89/3")
      		 || prod.getID().startsWith("89/4")
      		 || prod.getID().startsWith("89/5")
      		 || prod.getID().startsWith("89/6")
      		 || prod.getID().startsWith("89/7")
      		 || prod.getID().startsWith("89/8")
      		 || prod.getID().startsWith("89/9")
      		 || prod.getID().startsWith("81/")
      		 || prod.getID().startsWith("80/")
      		// || prod.getID().startsWith("76/") kristjani soovil dets 2012
      		 || prod.getID().startsWith("62/")
      		 || prod.getID().startsWith("46/")
      		 || prod.getID().startsWith("62/")        		 
      		 || prod.getID().startsWith("46/")
      		 || prod.getID().startsWith("36/")
      		 || prod.getID().startsWith("32/")
      		 || prod.getID().startsWith("28/")
      		  || prod.getID().startsWith("38/")
      		 || prod.getID().startsWith("53/")
      		 || prod.getID().startsWith("20/")
      		 || prod.getID().startsWith("17/")
      		 || prod.getID().startsWith("15/")
      		 || prod.getID().startsWith("14/")
      		 ){
      	       return  false;
                }
       return true;
   }
   

   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     sold = new Vector();
     HashMap kliendid = new HashMap();
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;


   while(!(line = reader.readLine()).equals("[Get Customer Products]")){
   }
   line=reader.readLine();
   while(((line = reader.readLine()).trim().length()) > 0 ){
      line = line.replaceAll("\",\"","|");
      StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String custID = clear(st.nextToken());
               if (st.hasMoreTokens()){
                 String prodID = clear(st.nextToken());
                 double amount = 0;
                 String lastdate = " ";
                 if (st.hasMoreTokens()){
                    amount = Double.parseDouble(clear(st.nextToken()));
                }
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
                 //if(custID.equals("0094")){
                	// System.out.println("LisID :" + custID + cust.getID()+ " " +prod.getID());
               // }
                
                 if(getallow(prod)){
                	 if(!sold.contains(prodID)){
                      	sold.addElement(prodID);
                      }
                      
                	  cust.addProduct(prod,cp,false);
                      kliendid.put(custID, cust);
                        vec.addElement(cp); 
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
 while(!(line = reader.readLine()).equals("[Get Customer Products]")){
 }
 line=reader.readLine();
 while(((line = reader.readLine()).trim().length()) > 0 ){
    line = line.replaceAll("\",\"","|");
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
               //      if(klient.equals("0094")){
                   //  System.out.println(toode+ "Counter: " + counter +" date " + date.toString()+" olddate " + olddate.toString() + " kogus "+ kogus +"oldklient" + oldklient);
                 //    }
                     vec.add(cp);
                   }
                 }
                 oldklient = klient;
                 olddate = date;
                 oldtoode = toode;
               }
               cou++;
             }}}

     } catch (Exception e){
       e.printStackTrace();
     }
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
     return "HammerNavi";
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


   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "data.txt";
   public static final String GROUP_FILE = "grupid.txt";
   
   public static final String DATA_2_FILE = "laoseis.txt";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
   private TreeMap discounts = new TreeMap();
   private Vector debtList = new Vector();
   private TreeMap agents1 = new TreeMap();
   private Vector sold = null;
   private String url = "jdbc:odbc:report";
   private TreeMap debtDays = new TreeMap();


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
