package eevaeksport.driver;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.Agent;
import eevaeksport.DealerProduct;
import eevaeksport.EksportPanel;
import eevaeksport.PlansBonuses;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public abstract class AbstractDBDriver {

  public AbstractDBDriver(EksportPanel parent){
    setParent(parent);
  }

  public abstract TreeMap getAgents();
  public abstract TreeMap getCustomers(Agent agent);
  public abstract TreeMap getProducts(Agent agent);
  public abstract String getCustProdDiscounts(Agent agent);
  public abstract String getAssortmentStr(Agent agent);
  public abstract Vector getCustomerProductsList(String agentCode);
  public abstract String getStartingTime();
  public abstract String  getDealerCustomerCSV(String agentCode);
  public abstract String getDriverVersion();
  public abstract String getSubjectFilesTo();
  public abstract double getSalary(String agentCode);
  public abstract void connectionUp();
  public abstract void connectionDown();
  public abstract String getPilotDayData(Agent pilot );
  public abstract String getDistribution();
  public abstract String getSaveStockInfo();
  public abstract String getSaveDebtInfo();

  
  public  String getExcelDir(){
      String dir = getParent().getProps().getProperty("excel.dir");
      if(dir != null){
    	   if (!(dir.endsWith("/") || (dir.endsWith("\\")))) dir = dir + "/";  
      }else{
    	    dir =  "excel/"; 
      }
   
      
      return dir;
    }
  
 
  
  
  private void setParent(EksportPanel parent){
    this.parent = parent;
  }

  /**
   *
   */
  public void setSales(double sales){
    this.sales = sales;
  }

  /**
   *
   */
  public double getSales(){
    return sales;
  }

  public void setSales3(double sales){
  this.sales3 = sales;
}

/**
 *
 */
public double getSales3(){
  return sales3;
}

public void setSales4(double sales){
this.sales4 = sales;
}

/**
*
*/
public double getSales4(){
return sales4;
}


  public TreeMap getDealers(){
    return null;
  }



  public DealerProduct getDealerProductsList(){
    return null;
  }

  public abstract TreeMap getProdGroups() throws SQLException;
  public  TreeMap getProdGroupsSecond() throws SQLException {
    return null;
  }

  public double getFoodSales(String agentCode) throws SQLException{
    return -1;
  }

  public double getAlcoSales(String agentCode) throws SQLException{
    return -1;
  }

  public PlansBonuses getPlansBonuses(String agentCode) throws SQLException{
    return null;
  }

  public String getNonMobileOrders(Agent agent){
    return null;
  }




  /**
   *
   */
  public double getPercent(){
    double percent = 0;
    if (budget != 0) {
      percent = sales * 100 / budget;
    }
    percent = Math.round(percent*100d)/100d;
    return percent;
  }
  
  public double getPercent2(){
	    double percent = 0;
	    if (budget2 != 0) {
	      percent = sales2 * 100 / budget2;
	    }
	    percent = Math.round(percent*100d)/100d;
	    return percent;
	  }
  
  public double getPercent1(){
	    double percent = 0;
	    if (budget1 != 0) {
	      percent = sales1 * 100 / budget1;
	    }
	    percent = Math.round(percent*100d)/100d;
	    return percent;
	  }
    public Date getStartingDate(){
        Calendar cal = Calendar.getInstance();
	cal.add(Calendar.MONTH,
		-(Integer.parseInt(parent.getProps().getProperty("months")) + 1));
      return new Date(cal.getTime().getTime());
    }
    
    public Timestamp getStartingStamp(){
        Calendar cal = Calendar.getInstance();
	cal.add(Calendar.MONTH,
		-(Integer.parseInt(parent.getProps().getProperty("months")) + 1));
      return new Timestamp(cal.getTime().getTime());
    } 
    

  /**
   *
   */
  public void setBudget(double budget){
    this.budget = budget;
  }

  public static double round(double value, double comma){
    double a = Math.pow(10, comma);
    return Math.round(value * a)/a;
  }

  public abstract Vector getDebt(String agentCode) throws SQLException;

  /**
   *
   */
  public double getBudget(){
    return budget;
  }

  /**
   *
   * @return true if debug is set to true.
   */
  public boolean isDebug(){
    return debug;
  }

  /**
   *
   * @param aDebug - the new value for debug
   */
  public void setDebug(boolean aDebug){
    debug = aDebug;
  }
  public boolean isTreeGroup(){
    return treegroup;
  }

  /**
   *
   * @param aDebug - the new value for debug
   */
  public void setTreeGroup(boolean aDebug){
    treegroup = aDebug;
  }

  public EksportPanel getParent(){
    return this.parent;
  }

  private double                sales = 0;
  private double                sales1 = 0;
  private double                sales2 = 0;
  private double                sales3 = 0;
  private double                sales4 = 0;
  private double                budget = 0;
  private double                budget1 = 0;
  private double                budget2 = 0;
  public EksportPanel       parent;
  private boolean	       debug = false;
  private boolean	       treegroup = false;
/**
 * @return the sales1
 */
public double getSales1() {
	return sales1;
}

/**
 * @param sales1 the sales1 to set
 */
public void setSales1(double sales1) {
	this.sales1 = sales1;
}

/**
 * @return the sales2
 */
public double getSales2() {
	return sales2;
}

/**
 * @param sales2 the sales2 to set
 */
public void setSales2(double sales2) {
	this.sales2 = sales2;
}

/**
 * @return the budget1
 */
public double getBudget1() {
	return budget1;
}

/**
 * @param budget1 the budget1 to set
 */
public void setBudget1(double budget1) {
	this.budget1 = budget1;
}

/**
 * @return the budget2
 */
public double getBudget2() {
	return budget2;
}

/**
 * @param budget2 the budget2 to set
 */
public void setBudget2(double budget2) {
	this.budget2 = budget2;
}

public TreeMap getProdGroupsAlternative() {
	// TODO Auto-generated method stub
	return null;
}




}
