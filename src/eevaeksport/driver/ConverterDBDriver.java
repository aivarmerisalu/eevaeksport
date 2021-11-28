package eevaeksport.driver;
import java.sql.SQLException;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ee.merisoft.sm.Product;
import eevaeksport.Agent;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.util.Converter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ConverterDBDriver extends AbstractDBDriver implements BulkUpdater,Discounter,FixLoader {

  public ConverterDBDriver(EksportPanel parent) {
    super(parent);
    this.subjectFilesTo = parent.getString("filemsg");
    user =  getParent().getProps().getProperty("rest.user");
    pass = getParent().getProps().getProperty("rest.pass");
    rcompany = getParent().getProps().getProperty("rest.companyx");
     }
  
  String user =  null;
  String pass = null;
  String rcompany = null;
  
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
	   TreeMap res = new TreeMap();
	   
	 JSONArray prods =  Converter.getWebProducts( rcompany, user ,  pass);
	 
	 for(int i = 0; i < prods.length(); i++) {
		 
		 try {
			JSONObject pr = (JSONObject) prods.get(i);
			//System.out.println("prod " + pr.toString());
			String code = (String)pr.get("code");
			String ean = (String)pr.get("EAN");
			String name = (String)pr.get("name");
			//System.out.println("prod " + code + " " + ean + " " + name );
			Product prod = new Product(code);
			prod.setName(name);
			prod.setEAN(ean);
			 eanprods.put(ean, code);
			 res.put(name, prod);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		 
	 }
      return res;
   }

   
   public String getAssortmentStr(Agent agent){
       return null;
  }


   public Vector getCustomerProductsList(String agentCode){
     return new Vector();
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


   private String subjectFilesTo = null;
  
  

  /**
   * setBulkSales
   *
   * @param agents Map
   */
  public void setBulkSales(Map agents) {
  }
  private TreeMap eanprods = null;
  
  @Override
  public void FixLoader() {
  	
  eanprods =  Converter.loadMore();
  	  
  	  if(eanprods == null) {
  		  eanprods = new TreeMap();
  	  }
  	this.getProducts(null);
  	TreeMap contracts = Converter.loadFixFromExcels("excelsort",eanprods,parent);
  	System.out.println("Contracts " + contracts.toString());
  	Converter.sendContractJson(contracts ,  rcompany,  user ,  pass);
  	System.exit(0);
  }

  }
