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
import java.util.HashMap;
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
import eevaeksport.Product;
import eevaeksport.manager.ManagerData;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Praline1SDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,Manager {

  public Praline1SDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    this.lang = parent.getProps().getProperty("language");
	String databaseDriverName = "sun.jdbc.odbc.JdbcOdbcDriver";

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
   
   public Vector getPriceMatrixData() {
   	return null;
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

  private Date getDate(String mmddyy){///"PIR00 04/19/12 98","PIR000007"
  	long lon = 0;
  	Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, Integer.parseInt("20" + mmddyy.substring(6, 8)));
    cal.set(Calendar.MONTH, Integer.parseInt(mmddyy.substring(0, 2))-1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mmddyy.substring(3, 5)));
    //System.out.println(cal.getTime());  //05.02.14
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
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;
    while(!(line = reader.readLine()).equals("[Get Prod Groups]")){
    }
   // line=reader.readLine();
    while(((line = reader.readLine()).trim().length()) > 0 ){
       line = line.replaceAll("\",\"","| ");
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
        if (st.hasMoreTokens()){
          String id = clear(st.nextToken());
                if (st.hasMoreTokens()){
                 String name = clear(st.nextToken());
          tm.put(cleardots(id),  name);
                 
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
    try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   while(!(line = reader.readLine()).equals("[Get Prod Groups]")){
   }
  // line=reader.readLine();
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
     while((line = reader.readLine()).startsWith("[Get Agents]")){
    
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
            if (st.hasMoreTokens()) {
              String objekt = clear(st.nextToken());
              agent.setObject(objekt);
            }
            tm.put(agent.getName(), agent);
          }
        }
      }
     }
      Agent ss =new Agent("HOUSE1");
      ss.setName("HOUSE1");
      tm.put("HOUSE1", new Agent("HOUSE1"));


 //Loe sisse kliendi pysi allalhindlused
/*      FileReader fr1 = new FileReader(getDir() + DATA_FILE);
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
 // loe sisse yle makset�htaja summad
    FileReader fr2 = new FileReader(getDir() + DATA_FILE);
    BufferedReader reader2 = new BufferedReader(fr2);
  String line2 = null;

  while(!(line2 = reader2.readLine()).equals("[Get Payer Overdue]")){
  }
  //line2=reader2.readLine();
  while(((line2 = reader2.readLine()).trim().length()) > 0 ){
      line2 = line2.replaceAll("\",\"","| ");
      StringTokenizer st2 = new StringTokenizer(line2, SEPARATOR);
     // System.out.println(line2);
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
           }
    //  line = reader.readLine();

       while(((line = reader.readLine()).trim().length()) > 0 ){
    	  // line = line.replaceAll("|","/");
            line = line.replaceAll("\",\"","|");
            line = line.replaceAll("\""," ");
            System.out.println(line);
       	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       	//   	"klient","number","kuup�ev","summa""PIR000010","MI-VII813616","16.03.06","44,98","PIR000007"
         if (st.hasMoreTokens()) {
           String custID = clear(st.nextToken());
           Debt debt = new Debt();
           debt.setCustID(custID);
           if (st.hasMoreTokens()) {
             String bill = clear(st.nextToken());
             if(bill.length()>6){
             	bill = bill.substring(bill.length()-6,bill.length());
             }
             debt.setDoknr(bill);
             System.out.println(bill + " "+ line);
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

              System.out.println(custID + " " + dueint + " " + old);
              //System.out.println("l�ks");
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
            this.debtList.add(debt);
           }
         }
       }

      } catch (Exception e){
        e.printStackTrace();
      }
System.out.println("debtList " +  debtList.size());

     TreeMap tm = new TreeMap();
     this.customers = new ArrayList();

     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;

     while(!(line = reader.readLine()).equals("[Get Customers]")){
    	 
     }
  //   line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");
        StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String name = st.nextToken();

         name = clear(name);
         
         if (st.hasMoreTokens()){
           String id = clear(st.nextToken());
     //      System.out.println(line);
           Customer customer = new Customer(id);
            String payer = null;
           if (name.equals("")) name = "<" + id + ">";
           customer.setName(name);
           if (st.hasMoreTokens()){
           	payer = clear(st.nextToken());
             customer.setPayer(payer);
           }

           if (st.hasMoreTokens()){
             String aadress = clear(st.nextToken());
             if( aadress != null){
               customer.setAddress(clearsec(aadress));
              // customer.setName(clearsec(customer.getName()+ " " + aadress));
             }

           }

           if (st.hasMoreTokens()){
        	   String ag = clear(st.nextToken());
        	   //System.out.println("agent " + ag);
             customer.addAgent(new Agent(ag));
             customer.addAgent(new Agent("HOUSE1"));
             
           }

           String paym = null;
           if (st.hasMoreTokens()){
           	paym = clear(st.nextToken());
            paym = paym.replaceAll("D","");
             //System.out.println(customer.getPaymTerm());
           }

           if (st.hasMoreTokens()){
           	String num = clear(st.nextToken());
           	if(num.trim().length()>0 && discounts.get(num) != null ){
             customer.setDiscount(Double.parseDouble((String)discounts.get(num)));

           	}
           }


           if (st.hasMoreTokens()){
             if(clear(st.nextToken()).equals("1")){
               customer.setSalesBlock(true);
             }
           }
       	String debti = "0";
        String used = "0";
       	if (st.hasMoreTokens()){
           	 debti = clear(st.nextToken());
           }


           if (st.hasMoreTokens()){
            customer.setVisit(clear(st.nextToken()));
            //System.out.println(customer.getVisit());
          }
         //  if (st.hasMoreTokens()){
           
//           st.nextToken();
         //   System.out.println(customer.getVisit());
          //}
           if (st.hasMoreTokens()){
            customer.setCustType(clear(st.nextToken()));

          }
           if (st.hasMoreTokens()){
             clear(st.nextToken());
           }
           



           if (st.hasMoreTokens()){
        	   
           	String aadr = clear(st.nextToken());
         
           }
           String cont = null;
           
           if (st.hasMoreTokens()){
              	 cont = clear(st.nextToken());
               }
           
           
       	   if(st.hasMoreTokens()){
 			    cont = cont + " " + clear(st.nextToken());
 		      }
       	 
       	   
       	  // System.out.println(" cont " + cont);
           if (cont != null && cont.trim().length()>0){    	
              customer.setPersonalData(cont.trim());
         	}
           
           
           if (st.hasMoreTokens()){  
        	   try{
        		   customer.setMinsum(Double.parseDouble(clear(st.nextToken())));
        	   }catch(Exception e){
        		  e.printStackTrace(); 
        	   }
        	    
           }
if(debti != null){
	try{

		  customer.setDebt(Double.parseDouble(debti));	
	}catch (Exception ee){
		
	}
}
          
        	customer.setPaymTerm(paym);
        	customer.setUsedCredit(Double.parseDouble(used.replaceAll(",",".").replaceAll(" ","")));


           customer.setPayer(id);

          if("Lithuanian".equals(lang) && !customer.getID().substring(0,1).equals("A")){
           	tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           } else if("Estonian".equals(lang) && !customer.getID().substring(0,1).equals("P")){
            tm.put(customer.getName(), customer);
            this.customers.add(customer.getID());
           }else{
           	if(customer.getVisit()!= null && customer.getVisit().trim().length()>0){
           		tm.put(customer.getName(), customer);
                this.customers.add(customer.getID());
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


   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();

     
     try {
     	//laoseisude lugemine ja liitmine algab
            FileReader fr = new FileReader(getDir() + DATA_FILE);
            BufferedReader reader = new BufferedReader(fr);
          String line = null;
        TreeMap prods = new TreeMap(); 
          while(!(line = reader.readLine()).equals("[Get stock]")){      	
          }
          if(line!=null){
        //  line = reader.readLine();
           while(((line = reader.readLine()).trim().length()) > 0 ){
                line = line.replaceAll("\",\"","|");
           	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
            String toode = null;
            double seis = 0;
           	if (st.hasMoreTokens()) {
             	toode = clear(st.nextToken());
             }
           	if (st.hasMoreTokens()) {
             	seis = Double.parseDouble(clear(st.nextToken()));
             }
           	   double alg = 0;
           	   if(prods.get(toode) != null){
           	   	alg = Double.parseDouble((String)prods.get(toode));
           	   }
                prods.put(toode,(seis+alg)+"");
                //System.out.println(ladu+" "+toode+" "+seis+"");
                
            }}

     	

       	//laoseisude lugemine ja liitmine algab
     	
     	
          
          
         fr = new FileReader(getDir() + DATA_FILE);
         reader = new BufferedReader(fr);
        
      TreeMap prices = new TreeMap(); 
        while(!(line = reader.readLine()).equals("[Get Pricelists]")){      	
        }
        if(line!=null){
      //  line = reader.readLine();
         while(((line = reader.readLine()).trim().length()) > 0 ){
              line = line.replaceAll("\",\"","|");
         	StringTokenizer st = new StringTokenizer(line, SEPARATOR);
          String toode = null;
          double hind = 0;
          String hinnakiri = null;
          if (st.hasMoreTokens()) {
             	hinnakiri = clear(st.nextToken());
             }
         	if (st.hasMoreTokens()) {
           	toode = clear(st.nextToken());
           }
         	if (st.hasMoreTokens()) {
           	hind = Double.parseDouble(clear(st.nextToken()));
           }
         	if(hinnakiri.equals("hulgihind")){
         		prices.put(toode,(hind)+"");
            }
              //System.out.println(ladu+" "+toode+" "+seis+"");
          }}
          
          
          
          
          
      fr = new FileReader(getDir() + DATA_FILE);
      reader = new BufferedReader(fr);
      line = null;

     while(!(line = reader.readLine()).equals("[Get Products]")){

     }
    // line=reader.readLine();
     while(((line = reader.readLine()).trim().length()) > 0 ){
        line = line.replaceAll("\",\"","| ");

       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = clear(st.nextToken());
         String weight ="";
         Product prod = new Product(id);
         if(prods.get(id)!= null){
         	prod.setStock(Double.parseDouble((String)prods.get(id)));
         	
         }
         
         if (st.hasMoreTokens()){
         	String name = clear(st.nextToken());
         	name = name.substring(0,1) + (name.substring(1,name.length())).toLowerCase();
         	//System.out.println(name);

           if (st.hasMoreTokens()){
           	st.nextToken().replace(',','.');
       
            String pr = (String)prices.get(prod.getID());
             double price = 0;
             if(pr != null){
                 price =  Double.parseDouble(pr); 
             }
             if (price > 0){
               prod.setPrice(price);}
               if (st.hasMoreTokens()){
                prod.setGroup(cleardots(clear(st.nextToken())));
            	//System.out.println(prod.getGroup() + " "  +line);
                
                if (st.hasMoreTokens()){
                    prod.setGroupSecond(cleardots(clear(st.nextToken())));
                    }
                     if (st.hasMoreTokens()){
                       prod.setUnit(clear(st.nextToken()));
                       // System.out.println("ladu"+prod.getStock());
                       if (st.hasMoreTokens()){
                         String ean = clear(st.nextToken());
                         if (ean.trim().length()>0){
                           prod.setEAN(ean);
                         }
                         if (st.hasMoreTokens()){
                            clear(st.nextToken());
                       
                         
                         
                         if (st.hasMoreTokens()){
                          clear(st.nextToken());
                         	
                         
                         	
                         String cases = clear(st.nextToken());
                            if (st.hasMoreTokens()){
                             	cases = clear(st.nextToken());
                             	if(cases != null && cases.trim().length()>0){
                             	//	System.out.println(cases + " "  +line);
                                prod.setInCases(Double.parseDouble(cases));
                             	}
                                if (st.hasMoreTokens()){
                                	 weight = clear(st.nextToken());
                                         if(weight != null && weight.trim().length()>0){
                                           weight = weight.trim();
                                         }

                                }
                                
                                if (st.hasMoreTokens()){
                                  	 String vat = clear(st.nextToken());
                                           if(vat != null && vat.trim().length()>0){
                                             if(vat.equals("GRUPE4")){
                                             	prod.setVat("18");
                                             }else if (vat.equals("GRUPE2")){
                                             	prod.setVat("5");
                                             }else if (vat.equals("PVN21")){
                                             	prod.setVat("21");
                                             }else if (vat.equals("PVN9")){
                                             	prod.setVat("9");
                                             }else if (vat.equals("PVN22")){
                                             	prod.setVat("22");
                                             }else if (vat.equals("PVN10")){
                                             	prod.setVat("10");
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
//           prod.setName(weight + " " + name);
           prod.setName("    " + name);
         }
        
            tm.put(prod.getName(), prod);
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
   // line=reader.readLine();
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
     HashMap kliendid = new HashMap();
     try {
       FileReader fr = new FileReader(getDir() + DATA_FILE);
     BufferedReader reader = new BufferedReader(fr);
   String line = null;

   //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

   while(!(line = reader.readLine()).equals("[Get Customer Products]")){
   }
  // line=reader.readLine();
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
 while(!(line = reader.readLine()).equals("[Get Customer Products]")){
 }
 //line=reader.readLine();
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
     }
     return vec;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Pralin1S";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
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

        ret = ret.replaceAll("�","11132xx");//
		ret = ret.replaceAll("�","11129xx");//
		ret = ret.replaceAll("�","11128xx");//
	//	ret = ret.replaceAll("","11111xx");//
		//ret = ret.replaceAll("","11112xx");//
		ret = ret.replaceAll("�","11113xx");//
		ret = ret.replaceAll("�","11114xx");//
		ret = ret.replaceAll("�","11115xx");//
		ret = ret.replaceAll("�","11116xx");//
		ret = ret.replaceAll("�","11117xx");//
		ret = ret.replaceAll("�","11118xx");
		ret = ret.replaceAll("�","11119xx");//
		ret = ret.replaceAll("�","11120xx");//
		ret = ret.replaceAll("�","11121xx");//
		ret = ret.replaceAll("�","11122xx");//
		ret = ret.replaceAll("�","11123xx");//
		ret = ret.replaceAll("�","11124xx");
		//ret = ret.replaceAll("","11125xx");//
		ret = ret.replaceAll("�","11126xx");//
		ret = ret.replaceAll("�","11127xx");//
		//ret = ret.replaceAll("?","11130xx");//
	
		
		
		ret = ret.replaceAll("11111xx","�");
		ret = ret.replaceAll("11112xx","�");
		ret = ret.replaceAll("11113xx","�");
		ret = ret.replaceAll("11114xx","�");
		ret = ret.replaceAll("11116xx","�");
		ret = ret.replaceAll("11116xx","�");
		ret = ret.replaceAll("11117xx","�");
		ret = ret.replaceAll("11118xx","�");	
		ret = ret.replaceAll("11120xx","�");
		ret = ret.replaceAll("11121xx","�");
		ret = ret.replaceAll("11122xx","�");
		ret = ret.replaceAll("11123xx","�");
		ret = ret.replaceAll("11124xx","�");
		ret = ret.replaceAll("11125xx","�");
		ret = ret.replaceAll("11126xx","�");
		ret = ret.replaceAll("11127xx","�");
		ret = ret.replaceAll("11128xx","�");
		ret = ret.replaceAll("11129xx","�");
		ret = ret.replaceAll("11130xx","�");
		ret = ret.replaceAll("11131xx","�");
		ret = ret.replaceAll("11115xx","�");
		ret = ret.replaceAll("11119xx","�");
		ret = ret.replaceAll("11132xx","�");
		//ret = ret.replaceAll("?","�");
		
	//	System.out.println(ret);
		if(ret == null || ret.trim().length()==0){
			return " ";
		}else {
			return ret;
		}
	}
   

	private String clearsec (String ss){
		//	Replace Riga

		String ret =   ss.replace(',','.');
		int riga = ss.indexOf("R");
		if(riga !=-1){
		}
		if(riga !=-1 && ret.substring(riga+1,ret.length()).indexOf("R")>0){
			riga  = riga +1 + ret.substring(riga+1,ret.length()).indexOf("R");

		}		
		if(riga > 0 && ret.length()> riga+3 && ss.substring(riga+2,riga+4).equals("ga")){
			ret = ret.substring(0,riga) + "R�" + ret.substring(riga+2,ret.length()) ;
		}
		
		
		
		// Replace Liepaja
		int liepa = ss.indexOf("Liep");
		if(liepa !=-1){
			//System.out.println( liepa  + " " + ret.substring(liepa+1,ret.length()));
				
		}
		if(liepa !=-1 && ret.substring(liepa+1,ret.length()).indexOf("Liep")>0){
			liepa  = liepa +1 + ret.substring(liepa+1,ret.length()).indexOf("Liep");

		}
		

		if(liepa > 0 && ret.length()> liepa+6 && !ss.substring(liepa,liepa+1).equals("a")){
			ret = ret.substring(0,liepa) + "Liep�" + ret.substring(liepa+5,ret.length()) ;

			//System.out.println( liepa + " " +ret );
		}
		ret = ret.replaceAll("11111xx","�");
		ret = ret.replace('?','�');
		ret = ret.replaceAll("11113xx","�");
		ret = ret.replaceAll("11114xx","�");
		ret = ret.replaceAll("11116xx","�");
		ret = ret.replaceAll("11116xx","�");
		ret = ret.replaceAll("11117xx","�");
		ret = ret.replaceAll("11118xx","�");	
		ret = ret.replaceAll("11120xx","�");
		ret = ret.replaceAll("11121xx","�");
		ret = ret.replaceAll("11122xx","�");
		ret = ret.replaceAll("11123xx","�");
		ret = ret.replaceAll("11124xx","�");
		ret = ret.replaceAll("11125xx","�");
		ret = ret.replaceAll("11126xx","�");
		ret = ret.replaceAll("11127xx","�");
		ret = ret.replaceAll("11128xx","�");
		ret = ret.replaceAll("11129xx","�");
		ret = ret.replaceAll("11130xx","�");
		ret = ret.replaceAll("11131xx","�");
		ret = ret.replaceAll("11115xx","�");
		ret = ret.replaceAll("11119xx","�");
		ret = ret.replaceAll("11132xx","�");
		//ret = ret.replaceAll("?","�");
		
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
   private String subjectFilesTo = null;
   private ArrayList customers = null;
   private TreeMap overdebts = new TreeMap();
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
        FileReader fr = new FileReader(getDir() + DATA_FILE);
      BufferedReader reader = new BufferedReader(fr);
    String line = null;

    //   "klient","toode","kogus","viimane kuup�ev","viimane kogus"

    while(!(line = reader.readLine()).equals("[Get Salary]")){
    }
   // line=reader.readLine();
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

public Vector getFixedAssortmentData() {
	// TODO Auto-generated method stub
	return null;
}
}
