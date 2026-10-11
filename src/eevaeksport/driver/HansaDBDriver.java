package eevaeksport.driver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.CustProd;
import eevaeksport.Customer;
import eevaeksport.EksportPanel;
import eevaeksport.Product;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HansaDBDriver extends AbstractDBDriver {

  public HansaDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }

   public String getPilotDayData(Agent agent){
     return null;
   }


       public String getSaveStockInfo(){
       return null;
       }



   public Vector getDebt(String agentCode){
     return null;
   }

   public void connectionDown(){
   }
   public void connectionUp(){
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
     TreeMap   tm = new TreeMap();
     return tm;
   }

   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();

     try {
       FileReader fr = new FileReader(getDir() + AGENT_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = st.nextToken().trim();
         Agent agent = new Agent(id);
         if (st.hasMoreTokens()){
           String name = st.nextToken().trim();
           if (name.equals("")) name = "<" + id + ">";
           agent.setName(name);
           if (st.hasMoreTokens()){
             String prodGroup = st.nextToken().trim();
             agent.setProdGroup(prodGroup);
           }
         }
         tm.put(agent.getName(), agent);
       }
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

     try {
       FileReader fr = new FileReader(getDir() + CUSTOMER_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = st.nextToken().trim();
         Customer customer = new Customer(id);
         customer.setAddress(" ");
         if (st.hasMoreTokens()){
           String name = st.nextToken().trim();
           if (name.equals("")) name = "<" + id + ">";
           customer.setName(name);
           if (st.hasMoreTokens()){
             customer.setDebt(Double.parseDouble(st.nextToken().trim()));
           }
           if (st.hasMoreTokens()){
             String aadress = st.nextToken().trim();
             if( aadress != null){
               customer.setAddress(aadress);
             }
           }
           if (st.hasMoreTokens()){
             customer.setDiscount(Double.parseDouble(st.nextToken().trim()));
           }

           if (st.hasMoreTokens()){
             customer.addAgent(new Agent(st.nextToken().trim()));
             //System.out.println(customer.getAgents());
           }
           if (st.hasMoreTokens()){
            customer.setVisit(st.nextToken().trim());
            System.out.println(customer.getVisit());
          }

           if(customer.getAgents().containsKey(agent.getID())){
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
     return tm;
   }

   public TreeMap getProducts(Agent agent){
     TreeMap tm = new TreeMap();
     try {
       FileReader fr = new FileReader(getDir() + PRODUCT_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String id = st.nextToken().trim();
         Product prod = new Product(id);
         if (st.hasMoreTokens()){
           prod.setName(st.nextToken().trim());
           if (st.hasMoreTokens()){
             double price = Double.parseDouble(st.nextToken().trim());
             if (price > 0){
               prod.setPrice(price);}
              // System.out.println("price" + prod.getPrice());
               if (st.hasMoreTokens()){
                 double inCases = Double.parseDouble(st.nextToken().trim());
                 if (inCases > 0){
                   prod.setInCases(inCases);
               //System.out.println("CAse" + prod.getInCases());
             }
                     if (st.hasMoreTokens()){
                       prod.setStock(Double.parseDouble(st.nextToken().trim()));
                       // System.out.println("ladu"+prod.getStock());


               }
             }
           }
         }
            tm.put(prod.getName(), prod);
       }

     }
     } catch (IOException e){
       e.printStackTrace();
     }
     return tm;
   }

   public Vector getCustomerProductsList(String agentCode){
     Vector vec = new Vector();
     try {
       FileReader fr = new FileReader(getDir() + CUSTOMER_PRODUCT_FILE);
       BufferedReader reader = new BufferedReader(fr);
     String line = null;
     while((line = reader.readLine()) != null){
       StringTokenizer st = new StringTokenizer(line, SEPARATOR);
       if (st.hasMoreTokens()){
         String custID = st.nextToken().trim();
         if (this.customers.contains(custID)){
               if (st.hasMoreTokens()){
                 String prodID = st.nextToken().trim();
                 CustProd cp = new CustProd(custID, prodID, 0, 0, 0," ",0,0,0," ");
                 Product prod = (Product)getParent().getProducts().get(prodID);
                 if (prod != null){
                   vec.addElement(cp);
                 }
           }
         }
       }
     }
     } catch (IOException e){
       e.printStackTrace();
     }
     return vec;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return null;
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

   public static final String SEPARATOR = ";";
   public static final String PRODUCT_FILE = "3_products.txt";
   public static final String AGENT_FILE = "1_agents.txt";
   public static final String CUSTOMER_FILE = "2_clients.txt";
   public static final String CUSTOMER_AGENT_FILE = "klagent.txt";
   public static final String CUSTOMER_PRODUCT_FILE = "4_sales_clients.txt";
   public static final String CUSTOMER_DEBT_FILE = "debt.txt";
   public static final String AGENT_SALES_FILE = "5_sales_agents.txt";
   private String subjectFilesTo = null;
   private ArrayList customers = null;
}