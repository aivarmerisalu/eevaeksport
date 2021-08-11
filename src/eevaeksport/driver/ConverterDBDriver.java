package eevaeksport.driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ConverterDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter {

  public ConverterDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
  }
  public String getSaveDebtInfo() {
   	return null;
   }
   
   public String getPilotDayData(Agent agent){
	return null;
         }

	public Vector getLastThreeData() {
		// TODO Auto-generated method stub
		return null;
	}
   public Vector getPriceMatrixData() {

   	Vector lines = new Vector();
   
	return lines;

   }

   public Vector getFixedAssortmentData() {
           Vector lines = new Vector();
           return lines;
   }

   public String getProductSortingData(){
   	return null;
   }
   
   public DealerProduct getDealerProductsList() {
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

   public String getDealerCustomerCSV(String agent){
       return null;
        }

       public String  getCustProdDiscounts(Agent agent){
                     return null;
            }

   public TreeMap getProdGroups() throws SQLException {
             return null;
   }

   public TreeMap getProdGroupsSecond() throws SQLException {
        return null;
        }
   
   
   public TreeMap getAgents(){
     TreeMap tm = new TreeMap();
            tm.put("HOUSE", new Agent("HOUSE"));
     return tm;
   }




       public String getDistribution() {
         return null;
       }


   public TreeMap getCustomers(Agent agent){
     return new TreeMap();
   }


   public TreeMap getProducts(Agent agent){
      return new TreeMap();
   }

   
   public String getAssortmentStr(Agent agent){
       return null;
  }


   public Vector getCustomerProductsList(String agentCode){
    

     return vec;
   }

   public String getStartingTime(){
     return null;
   }
   public String getDriverVersion(){
     return "Converter";
   }
   public String getSubjectFilesTo(){
     return this.subjectFilesTo;
   }
   public double getSalary(String agentCode){
     return 0;
   }

  

   public static final String SEPARATOR = "|";
   public static final String DATA_FILE = "DATA.TXT";
   public static final String SALES_BILLS_FILE = "orders.log";
   private String subjectFilesTo = null;
   private  Vector vec = new Vector();
  

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
  }
  }
