package eevaeksport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import eevaeksport.driver.Toode;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */


public class Agent {

  public Agent(String kood) {
    this.id = kood;
  }

  public long lastreport = 0;
  public void setObject(String objekt) {
    this.objekt = objekt;
  }

  public String getObject() {
    return this.objekt;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getID() {
    return this.id;
  }

  public String getDays() {
    return days;
  }

  public void setDays(String days) {
    this.days = days;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getVisit() {
    return visit;
  }

  public void setVisit(String visit) {
    this.visit = visit;
  }

  public boolean needsZipPass = false;
  private String id = null;
  private String objekt = null;
  private String objektmain = null;
  private String name = null;
  private String days = null;
  private String prodGroup = null;
  public int buffer = 0;
  public int buffer2 = 0;
  public String zipPass = null;
  private Vector todayCusts = null;
  private String passWord = null;
  private boolean isPilot = false;
  private boolean showmarketinfoall = true;
  private String code = null;
  private String visit = null;
  private String agcust = null;
  private String email = null;
  private String phone = null;
  private String call = null;
  private boolean  active = false;
 
  public Vector klienteKokku = null;
  public Vector klienteAkt30 = null;
  public Vector klienteAkt60 = null;
  public Vector klienteSee = null;
  public Vector klienteEelm = null;
  public Vector klienteEile = null;
  public Vector klienteViim7 = null;
 
  
  public ArrayList prodGroupList = new ArrayList();
  public ArrayList prodGroupListSec = new ArrayList();
  private int monthSales = 0;
  private int monthSales1 = 0;
  private boolean hasEmail = false;
  public boolean isHasE() {
	return hasEmail;
}

public void setHasEmail(boolean hasEmail) {
	this.hasEmail = hasEmail;
}

private int monthSales2 = 0;
  private String startText = null;
  public String getStartText() {
	return startText;
}

public void setStartText(String startText) {
	this.startText = startText;
}

private int monthSalesVol = 0;
  private int monthBudget = 0;
  private int monthBudget2 = 0;
  private int monthBudget1 = 0;
  private int kgSales = 0 ;
  private int monthProfit = 0;
  private int monthSales3 = 0;
  private int monthFoodSales = 0;
  private String start = "";
  public String techdata = "";
  private TreeMap custsort = null;
  public TreeMap getCustsort() {
	return custsort;
  }

public void setCustsort(TreeMap custsort) {
	this.custsort = custsort;
}

public String getStart() {
	return start;
}

public void setStart(String start) {
	this.start = start;
}

private int monthBonusSales = 0;
  private int monthAlkoSales = 0;
  private int monthHORECA = 0;
  private int monthOFFTRADE = 0;
  private boolean sednAllClients = false;
  private boolean isManager = false;
  private Vector dubbler = new Vector();
  private String custstaticinfo = null;
  private String language  = null;
  private String country  = null;
  private String group  = null;
  private Vector discountProds = null;
  private Vector pricelists = new Vector();

  public void addDiscountProds(Toode prodid){
	  if(discountProds == null){
		  discountProds = new Vector();
	  }
	  discountProds.add(prodid);
  }
  
  public void setMonthOFFSales(int aMonthSales) {
     monthOFFTRADE = monthOFFTRADE + aMonthSales;
   }

   public int getMonthOFFSales() {
     return monthOFFTRADE;
   }


  public void setMonthHorecaSales(int aMonthSales) {
     monthHORECA = monthHORECA + aMonthSales;
   }

   public int getMonthHorecaSales() {
     return monthHORECA;
   }



  public void setMonthAlkoSales(int aMonthSales) {
     monthAlkoSales = monthAlkoSales + aMonthSales;
   }

   public int getMonthAlkoSales() {
     return monthAlkoSales;
   }

  public void setMonthFoodSales(int aMonthSales) {
     monthFoodSales = monthFoodSales + aMonthSales;
   }

   public int getMonthFoodSales() {
     return monthFoodSales;
   }

  /**
   *
   * @return true if showmarketinfoall is set to true.
   */
  public boolean isShowmarketinfoall() {
    return showmarketinfoall;
  }

  public ArrayList getProdGroupList(){
  return this.prodGroupList;
  }
  
  public ArrayList getProdGroupListSec(){
	  return this.prodGroupListSec;
	  }

  /**
   *
   * @param aShowmarketinfoall - the new value for showmarketinfoall
   */
  public void setShowmarketinfoall(boolean aShowmarketinfoall) {
    showmarketinfoall = aShowmarketinfoall;
  }

  /**
   *
   * @return true if useEAN is set to true.
   */
  public boolean isIspilot() {
    return isPilot;
  }

  /**
   *
   * @param aUseEAN - the new value for useEAN
   */
  public void setIsPilot(boolean aIsPilot) {
    isPilot = aIsPilot;
  }

  /**
   *
   * @return the int value of monthSales.
   */
  public int getMonthSales() {
    return monthSales;
  }

  /**
   *
   * @param aMonthSales - the new value for monthSales
   */
  public void setMonthSales(int aMonthSales) {
    monthSales = monthSales + aMonthSales;
  }

  public int getMonthSalesVol() {
    return monthSalesVol;
  }

  /**
   *
   * @param aMonthSales - the new value for monthSales
   */
  public void setMonthSalesVol(int aMonthSales) {
    monthSalesVol = monthSalesVol + aMonthSales;
  }

  public boolean hasProdGroupList(String prodGroup) {
    return prodGroupList.contains(prodGroup);
  }
  public boolean hasProdGroupListSec(String prodGroup) {
	    return prodGroupListSec.contains(prodGroup);
	  }

  public void addProdGroupList(String prodGroup) {
    prodGroupList.add(prodGroup);
  }
  public void setProdGroupList(ArrayList prodGroup) {
	    prodGroupList = prodGroup;
	  }

  public void setProdGroupListSec(ArrayList prodGroup) {
	    prodGroupListSec = prodGroup;
	  }
  public void addProdGroupListSec(String prodGroup) {
	    prodGroupListSec.add(prodGroup);
	  }
  /**
   *
   * @return the String value of prodGroup.
   */
  public String getProdGroup() {
    return prodGroup;
  }

  /**
   *
   * @param aProdGroup - the new value for prodGroup
   */
  public void setProdGroup(String aProdGroup) {
    prodGroup = aProdGroup;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("id=" + getID() + ";name=" + getName() + ";prodGroupList=");
    for (Iterator iter = prodGroupList.iterator(); iter.hasNext(); ) {
      sb.append( (String) iter.next() + ",");
    }
    for (Iterator iter = prodGroupListSec.iterator(); iter.hasNext(); ) {
        sb.append( (String) iter.next() + ",");
      }
    sb.append("day:" + getDays() + " type:" + getCode() + " sales:" + this.getMonthSales());
    sb.append("\n");
    return sb.toString();
  }

  public boolean hasProdGroups() {
    if (prodGroupList.size() == 0) {
      return false;
    }
    else {
      return true;
    }
  }
  
  public boolean hasProdGroupsSec() {
	    if (prodGroupListSec.size() == 0) {
	      return false;
	    }
	    else {
	      return true;
	    }
	  }
/**
 * @return Returns the objektmain.
 */ 
public String getObjektmain() {
	return objektmain;
}
/**
 * @param objektmain The objektmain to set.
 */
public void setObjektmain(String objektmain) {
	this.objektmain = objektmain;
}
/**
 * @return Returns the dubbler.
 */
public Vector getDubbler() {
	return dubbler;
}
/**
 * @param dubbler The dubbler to set.
 */
public void addDubbler(String dubb) {
	dubbler.addElement(dubb);
}
/**
 * @return Returns the sednAllClients.
 */
public boolean isSednAllClients() {
	return sednAllClients;
}
/**
 * @param sednAllClients The sednAllClients to set.
 */
public void setSednAllClients(boolean sednAllClients) {
	this.sednAllClients = sednAllClients;
}
/**
 * @return Returns the isManager.
 */
public boolean isManager() {
	return isManager;
}
/**
 * @param isManager The isManager to set.
 */
public void setManager(boolean isManager) {
	this.isManager = isManager;
}
/**
 * @return Returns the custstaticinfo.
 */
public String getCustStaticInfo() {
	return custstaticinfo;
}
/**
 * @param custstaticinfo The custstaticinfo to set.
 */
public void setCustStaticInfo(String custstaticinfo) {
	this.custstaticinfo = custstaticinfo;
}
/**
 * @return Returns the monthBonusSales.
 */
public int getMonthBonusSales() {
	return monthBonusSales;
}
/**
 * @param monthBonusSales The monthBonusSales to set.
 */
public void setMonthBonusSales(int monthBonusSales) {
	this.monthBonusSales = monthBonusSales;
}
/**
 * @return Returns the monthSales3.
 */
public int getMonthSales3() {
	return monthSales3;
}
/**
 * @param monthSales3 The monthSales3 to set.
 */
public void setMonthSales3(int monthSales3) {
	this.monthSales3 = monthSales3;
}
/**
 * @return Returns the monthBudget.
 */
public int getMonthBudget() {
	return monthBudget;
}
/**
 * @param monthBudget The monthBudget to set.
 */
public void setMonthBudget(int monthBudget) {
	this.monthBudget = monthBudget;
}
/**
 * @return Returns the passWord.
 */
public String getPassWord() {
	return passWord;
}
/**
 * @param passWord The passWord to set.
 */
public void setPassWord(String passWord) {
	this.passWord = passWord;
}
/**
 * @return Returns the language.
 */
public String getLanguage() {
	return language;
}
/**
 * @param language The language to set.
 */
public void setLanguage(String language) {
	this.language = language;
}
/**
 * @return Returns the monthProfit.
 */
public int getMonthProfit() {
	return monthProfit;
}
/**
 * @param monthProfit The monthProfit to set.
 */
public void setMonthProfit(int monthProfit) {
	this.monthProfit = monthProfit;
}
/**
 * @return Returns the kgSales.
 */
public int getKgSales() {
	return kgSales;
}
/**
 * @param kgSales The kgSales to set.
 */
public void setKgSales(int kgSales) {
	this.kgSales = this.kgSales  + kgSales;
}

/**
 * @return the discountProds
 */
public Vector getDiscountProds() {
	return discountProds;
}

/**
 * @return the todayCusts
 */
public Vector getTodayCusts() {
	return todayCusts;
}

/**
 * @param todayCusts the todayCusts to set
 */
public void addTodayCusts(String todayCust) {
	if(todayCusts == null){
		todayCusts = new Vector();
	}
	 todayCusts.addElement(todayCust);
}

/**
 * @return the monthBudget2
 */
public int getMonthBudget2() {
	return monthBudget2;
}

/**
 * @param monthBudget2 the monthBudget2 to set
 */
public void setMonthBudget2(int monthBudget2) {
	this.monthBudget2 = monthBudget2;
}

/**
 * @return the monthBudget1
 */
public int getMonthBudget1() {
	return monthBudget1;
}

/**
 * @param monthBudget1 the monthBudget1 to set
 */
public void setMonthBudget1(int monthBudget1) {
	this.monthBudget1 = monthBudget1;
}

/**
 * @return the monthSales1
 */
public int getMonthSales1() {
	return monthSales1;
}

/**
 * @param monthSales1 the monthSales1 to set
 */
public void setMonthSales1(int monthSales1) {
	this.monthSales1 = monthSales1;
}

/**
 * @return the monthSales2
 */
public int getMonthSales2() {
	return monthSales2;
}

/**
 * @param monthSales2 the monthSales2 to set
 */
public void setMonthSales2(int monthSales2) {
	this.monthSales2 = monthSales2;
}

/**
 * @return the pricelists
 */
public Vector getPricelists() {
	return pricelists;
}

/**
 * @param pricelists the pricelists to set
 */
public void addPricelists(String pricelist) {
	pricelists.add(pricelist);
}

public void setHasEmail(String parseFirstEmail) {
	// TODO Auto-generated method stub
	
}

public String getCall() {
	return call;
}

public void setCall(String call) {
	this.call = call;
}

public String getGroup() {
	return group;
}

public void setGroup(String group) {
	this.group = group;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getPhone() {
	return phone;
}

public void setPhone(String phone) {
	this.phone = phone;
}

public String getCountry() {
	return country;
}

public void setCountry(String country) {
	this.country = country;
}

public boolean isActive() {
	return active;
}

public void setActive(boolean active) {
	this.active = active;
}

public String getAgcust() {
	return agcust;
}

public void setAgcust(String agcust) {
	this.agcust = agcust;
}
}

