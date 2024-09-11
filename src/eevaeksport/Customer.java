package eevaeksport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.json.simple.JSONObject;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Customer {

  public Customer(String id) {
    this.id = id;
    setPayer( id);
  }
  public String restPriority = null;
  public double criticalDebt = 0;
  public String restPartner = null;
  public String restName = null;
  public boolean diffBack = false;
  public boolean diffBack2 = false;
  public boolean noOrder = false; 
  public boolean allowGift = false;
  public String GLNCode = null;
  public String payments = null;
  public String currency = null;
  public String taraDeal = null;
  public String category = null;
  public boolean noCashSales = false;
  public String restdirect = null;
  public JSONObject json = null;
  public boolean topstatus = true;
  public boolean investstatus = true;
  public String level = null;
  public Vector<MatrixLine> fixedLines = null;

  public String getID() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return this.address;
  }

  public void setAddress(String address) {
    if (address.equals(""))
      address = " ";
    this.address = address;
  }

  public double getDebt() {
    return this.debt;
  }

  public void setDebt(double debt) {
    this.debt = debt;
  }


  public void addDebt(double debt) {
    this.debt = this.debt + debt;
  }
  
  public double getFixDisc() {
    return this.fixdisc;
  }

  public void setFixDisc(double fixdisc) {
    this.fixdisc = fixdisc;
  }

  public double getDiscount() {
    return this.discount;
  }

  public void setDiscount(double discount) {
    this.discount = discount;
  }

  
  public void addProductDist(Product prod, CustProd cp ,boolean isWare) {
	    ArrayList al = new ArrayList();
	    al.add(prod);
	    al.add(cp);
	    String ids = prod.getID();
	    int cc = 0;
	    if(isWare && productsDist.containsKey(ids)){
	    	ids = prod.getID() + "" + cc;
	    	cc++;
	    	while(productsDist.containsKey(ids)){
	        	ids = prod.getID() + "" + cc;
	        	cc++;
	        	  
	        }
	    }
	  
	    productsDist.put(ids, al);
	 //   System.out.println( "Size laadimisel " + products.size() + " " + id + " " + ids);
	  }
  
  public void addProduct(Product prod, CustProd cp ,boolean isWare) {
    ArrayList al = new ArrayList();
    al.add(prod);
    al.add(cp);
    String ids = prod.getID();
    int cc = 0;
    if(isWare && products.containsKey(ids)){
    	ids = prod.getID() + "" + cc;
    	cc++;
    	while(products.containsKey(ids)){
        	ids = prod.getID() + "" + cc;
        	cc++;
        	  
        }
    }
  
    products.put(ids, al);
 //   System.out.println( "Size laadimisel " + products.size() + " " + id + " " + ids);
  }

  public TreeMap getProducts() {
    return this.products;
  }

  public TreeMap getSortedProducts(boolean ware , boolean ekstra) {
    TreeMap sorted = new TreeMap();
   // System.out.println("Prdoctys Size " + products.size());
    for (Iterator iter = products.values().iterator(); iter.hasNext(); ) {
      ArrayList al = (ArrayList) iter.next();
      Product prod = (Product) al.get(0);
      CustProd cp = (CustProd) al.get(1);
      String add = "";
      if(ekstra){
    	 if(cp.getAbiKood() != null && cp.getAbiKood().trim().length() > 0) {
    		 add = cp.getAbiKood();
    	 }else{
    		 add = "XXX";
    	 }
      }
  
      if(ware && prod.getSorting()!= null && prod.getSorting().trim().length() >0){
    	  add = add + prod.getSorting();
      }
      String name = add + prod.getNameSorting();
      
      if(ware && sorted.get(name) != null){
    	  name =  name + "1";
    	  while(sorted.get(name) != null){
        	  name = name + "1";
          }
      }
      sorted.put(name, al);
     // System.out.println("sorted " + this.id + " "+ prod.getID()  +  " "  +name);
    }
    return sorted;
  }

  
  
  public TreeMap getSortedDistProducts(boolean ware ) {
	    TreeMap sorted = new TreeMap();
	   // System.out.println("Prdoctys Size " + products.size());
	    for (Iterator iter = productsDist.values().iterator(); iter.hasNext(); ) {
	      ArrayList al = (ArrayList) iter.next();
	      Product prod = (Product) al.get(0);
	      String add = "";
	      if(ware && prod.getSorting()!= null && prod.getSorting().trim().length() >0){
	    	  add = prod.getSorting();
	      }
	      String name = add + prod.getName();
	      if(ware && sorted.get(name) != null){
	    	  name =  name + "1";
	    	  while(sorted.get(name) != null){
	        	  name = name + "1";
	          }
	      }
	      sorted.put(name, al);
	     // System.out.println("sorted " + this.id + " "+ prod.getID()  +  " "  +name);
	    }
	    return sorted;
	  }
  
  public String toString() {
    return "id=" + this.id;
  }

  public void setVisit(String visit) {
    this.visit = visit;
  }

  public String getVisit() {
    return this.visit;
  }

  /**
   *
   * @return the Dealer value of dealer.
   */
  public Dealer getDealer() {
    return dealer;
  }

  /**
   *
   * @param aDealer - the new value for dealer
   */
  public void setDealer(Dealer aDealer) {
    dealer = aDealer;
  }

  public void addAgent(Agent a) {
    getAgents().put(a.getID(), a);
  }

  /**
   *
   * @return the TreeMap value of agents.
   */
  public TreeMap getAgents() {
    return agents;
  }

  public Vector restVechicles = null;
  
  /**
   *
   * @return the String value of group.
   */
  public String getGroup() {
    return group;
  }
  public String getPersonalData() {
    return this.personalData;
  }
  public void setPersonalData(String pers) {
    this.personalData = pers;
  }
  public void adPersonalData(String pers) {
	    this.personalData = this.personalData +  pers;
	  }
  public String getLicence() {
     return this.licence;
   }
   public void setLicence(String pers) {
     this.licence = pers;;
   }




  public void setGroup(String aGroup) {
    group = aGroup;
  }
  public boolean hasAgents() {
	  	if (agents != null ){
	  		return agents.size() > 0; 		  		
	  	}
	  	
	    return false;
	  }
  
  public boolean hasAgent(String agentCode) {
  	if (agents != null && agentCode != null ){
  		return agents.keySet().contains(agentCode); 		  		
  	}
  	if(agentCode == null){
  		
  	}
    return false;
  }

  private String id = null;
  private int delExtraDays = -1;
  private TreeMap products = new TreeMap();
  private TreeMap productsDist = new TreeMap();
  public TreeMap getProductsDist() {
	return productsDist;
}

public void setProductsDist(TreeMap productsDist) {
	this.productsDist = productsDist;
}

private TreeMap agents = new TreeMap();
  private String name = null;
  private double debt = 0;
  private String address = " ";
  private String infoEmail = " ";
  
  public String getInfoEmail() {
	return infoEmail;
}

public void setInfoEmail(String infoEmail) {
	this.infoEmail = infoEmail;
}

private double discount = 0;
  private Vector debts = null;
  private String visit = " ";
  private boolean visitToday = false;
  private boolean askContactData = false;
  public String suggAssortCode = " ";
  private String visitabi = " ";
  private String callabi = " ";
  private boolean isPayer = false;
  private Dealer dealer = null;
  private String group = " ";
  private String blockingDealer = " ";
  private String printean = " ";
  private String info = " ";
  private String country = null;
  private double VAT = -1;
  private String pictEmail = " ";
  public String getPictEmail() {
	return pictEmail;
}

public void setPictEmail(String pictEmail) {
	this.pictEmail = pictEmail;
}

public double getVAT() {
	return VAT;
}

public void setVAT(double vAT) {
	VAT = vAT;
}

private String payer = " ";
  private double fixdisc = 0;
  private String confirmEmail = " ";
  private String returnEmail = " ";
  private String juridics = " ";
  private String logist = " ";
  private String delDays = " ";
  public String getDelDays() {
	return delDays;
}

public void setDelDays(String delDays) {
	this.delDays = delDays;
}

private String mainAgent = " ";
  public String getMainAgent() {
	return mainAgent;
}

public void setMainAgent(String mainAgent) {
	this.mainAgent = mainAgent;
}

private String ankeediTyyp = " ";
  private boolean salesBlock = false;
  private boolean salesBlockT = false;
  private boolean salesBlockPrepay = false;
  private String credit = " ";
  public Vector fixSortCode = new Vector();
  private String queryIDs = " ";
  private String distUpTo = " ";
    private String paymterm = " ";
    private boolean useSecondPricelist = false ;
    private String custType = " ";
    private double budget = 0;
    private double sold = 0;
    private double average = 0;
    private double maxOrderSum = 0;
    private String ware = " ";
    private double usedcredit = 0;
    private String personalData = " ";
    private boolean nodebt = false;
    private String primDealer = " ";
    private String licence = " ";
    private boolean adAction = false ;
    private CustomerInfo custinfo = null ; 
    private String contractOver = " ";
    private String lastCheckDate = " ";
    private String city = " ";
    private String callDay = " ";
    private String zipcode = " ";
    private Vector lastCheckMissingProds = new Vector();
    private Vector custDataVec = new Vector();
    private String weekVisit = " "; 
    private boolean supervisor2Filled = false;
    private boolean supervisorFilled = false;
    private boolean supervisormustfill = false;
    private String lastBillInfo = " ";
    private String vatnr = " ";
    private String regnr = " ";
    private double minsum = -1;
    private double maxdisc = -1;
    private String sortCode = " ";
    private String chain = " ";
    public String section = null;
    public String restPhone = null;
    public String restStreet = null;
    public String restCity = null;
    public String restZipCode = null;
    public String restPerson = null;
    public String restPerEmail = null;
    public String restPerPos = null;
    public Vector restRoute = null;
    public String restComment = null;
    public String restPerPhone = null;
 
  
  public String getAnkeediTyyp() {
	return ankeediTyyp;
}

public void setAnkeediTyyp(String ankeediTyyp) {
	this.ankeediTyyp = ankeediTyyp;
}


  public String getQueryIDs() {
	return queryIDs;
}

public void setQueryIDs(String queryIDs) {
	this.queryIDs = queryIDs;
}

public String getFixSortCode() {
	String asi = " ";
	for(int a = 0;a < fixSortCode.size();a++){
		if(asi.equals(" ")){
			  asi = (String) fixSortCode.elementAt(a);
		}else{
			  asi = (asi + "|" +  fixSortCode.elementAt(a)).trim();
		}
	}
	return asi ;
}

public void addFixSortCode(String fixSortCode) {
	this.fixSortCode.add(fixSortCode);
}

public void setFixSortCode(Vector fixSortCode) {
	this.fixSortCode = fixSortCode;
}
private boolean showmarketinfo = false;
  private boolean showfullpricelist = false;
  public boolean isShowfullpricelist() {
	return showfullpricelist;
}

public void setShowfullpricelist(boolean showfullpricelist) {
	this.showfullpricelist = showfullpricelist;
}
  
  public String getPrimDeal(){
          return primDealer;
  }

 
  public void setPrimDeal(String aP){
          primDealer = aP;
  }

  public double getUsedCredit() {
                  return usedcredit;
          }
          /**
           * @param budget The budget to set.
           */
          public void setUsedCredit(double budget) {
                  this.usedcredit = budget;
          }


	/**
	 * @return Returns the budget.
	 */
	public double getBudget() {
		return budget;
	}
	/**
	 * @param budget The budget to set.
	 */
	public void setBudget(double budget) {
		this.budget = budget;
	}
	/**
	 * @return Returns the sold.
	 */
	public double getSold() {
		return sold;
	}
	/**
	 * @param sold The sold to set.
	 */
	public void setSold(double sold) {
		this.sold = sold;
	}
	
	
	private boolean makeAmountRounding = false;
	public boolean restActive = true;
	public String nfctag = null;
	public String setSMS = null;
	public double bonusAvailable = 0;
	public boolean needsToBeVisited = false;
	    public boolean isMakeAmountRounding() {
			return makeAmountRounding;
		}

	public void setMakeAmountRounding(boolean makeAmountRounding) {
			this.makeAmountRounding = makeAmountRounding;
		}

	/**
	*
	* @return the String value of paymterm.
	*/
	public String getPaymTerm(){
		return paymterm;
	}

	/**
	*
	* @param aPaymterm - the new value for paymterm
	*/
	public void setPaymTerm(String aPaymterm){
		paymterm = aPaymterm;
	}




  /**
   *
   * @return true if showmarketinfo is set to true.
   */
  public boolean isShowmarketinfo() {
    return showmarketinfo;
  }


  public boolean isShowFullPricelist() {
    return showfullpricelist;
  }


  public String getUseSecondPricelist() {
   String result = "0" ;
   if (useSecondPricelist){
   result = "1" ;
   }
    return result;
  }



  public void setUseSecondPricelist(boolean aShow) {
     useSecondPricelist = aShow;
   }

  /**
   *
   * @param aShowmarketinfo - the new value for showmarketinfo
   */
  public void setShowmarketinfo(boolean aShowmarketinfo) {
    showmarketinfo = aShowmarketinfo;
  }

  public String getDistUpTo() {
    return distUpTo;
  }

  public void setDistUpTo(String logi) {
    distUpTo = logi;
  }

  public String getCredit() {
    return credit;
  }

  public void setCredit(String logi) {
    credit = logi;
  }

  public boolean isSalesBlock() {
    return salesBlock;
  }

  public void setSalesBlock(boolean sb) {
    this.salesBlock = sb;
  }

  public String getLogistics() {
    return logist;
  }

  public void setLogistics(String logi) {
    logist = logi;
  }

  public String getConfirmEmail() {
    return confirmEmail;
  }

  public void setConfirmEmail(String comMent) {
    confirmEmail = comMent;
  }

  /**
   *
   * @return the String value of payer.
   */
  public String getPayer() {
    return payer;
  }

  /**
   *
   * @param aPayer - the new value for payer
   */
  public void setPayer(String aPayer) {
    payer = aPayer;
  }

  /**
   *
   * @return the String value of info.
   */
  public String getInfo() {
    return info;
  }

  /**
   *
   * @param aInfo - the new value for info
   */
  public void setInfo(String aInfo) {
    info = aInfo;
  }

  /**
   *
   * @return the String value of printean.
   */
  public String getPrintean() {
    return printean;
  }

  /**
   *
   * @param aPrintean - the new value for printean
   */
  public void setPrintean(String aPrintean) {
    printean = aPrintean;
  }

  /**
   *
   * @return the String value of blockingDealer.
   */
  public String getBlockingDealer() {
    return blockingDealer;
  }
  public boolean getSalesBlock() {
    return salesBlock;
  }
  /**
   *
   * @param aBlockingDealer - the new value for blockingDealer
   */
  public void setBlockingDealer(String aBlockingDealer) {
    blockingDealer = aBlockingDealer;
  }

/**
 * @return Returns the custType.
 */
public String getCustType() {
	return custType;
}
/**
 * @param custType The custType to set.
 */
public void setCustType(String custType) {
	this.custType = custType;
}
/**
 * @return Returns the nodebt.
 */
public boolean isNodebt() {
	return nodebt;
}
/**
 * @param nodebt The nodebt to set.
 */
public void setNodebt(boolean nodebt) {
	this.nodebt = nodebt;
}
/**
 * @return Returns the juridics.
 */
public String getJuridics() {
	return juridics;
}
/**
 * @param juridics The juridics to set.
 */
public void setJuridics(String juridics) {
	this.juridics = juridics;
}
/**
 * @return Returns the custinfo.
 */
public CustomerInfo getCustinfo() {
	return custinfo;
}
/**
 * @param custinfo The custinfo to set.
 */
public void setCustinfo(CustomerInfo custinfo) {
	this.custinfo = custinfo;
}
/**
 * @return Returns the contractOver.
 */
public String getContractOver() {
	return contractOver;
}
/**
 * @param contractOver The contractOver to set.
 */
public void setContractOver(String contractOver) {
	this.contractOver = contractOver;
}
/**
 * @return Returns the lastCheckMissingProds.
 */
public Vector getLastCheckMissingProds() {
	return lastCheckMissingProds;
}
/**
 * @param lastCheckMissingProds The lastCheckMissingProds to set.
 */
public void addLastCheckMissingProds(String last) {
	this.lastCheckMissingProds.add(last);
}
/**
 * @return Returns the lastCheckDate.
 */
public String getLastCheckDate() {
	return lastCheckDate;
}
/**
 * @param lastCheckDate The lastCheckDate to set.
 */
public void setLastCheckDate(String lastCheckDate) {
	this.lastCheckDate = lastCheckDate;
}
/**
 * @return Returns the visitabi.
 */
public String getVisitabi() {
	return visitabi;
}
/**
 * @param visitabi The visitabi to set.
 */
public void setVisitabi(String visitabi) {
	this.visitabi = visitabi;
}
/**
 * @return Returns the visitToday.
 */
public boolean isVisitToday() {
	return visitToday;
}
/**
 * @param visitToday The visitToday to set.
 */
public void setVisitToday(boolean visitToday) {
	this.visitToday = visitToday;
}
/**
 * @return Returns the city.
 */
public String getCity() {
	return city;
}
/**
 * @param city The city to set.
 */
public void setCity(String city) {
	this.city = city;
}
public void addCity(String city) {
	if(this.city.trim().length() == 0) {
		this.city = city;
	}else {
		this.city = this.city + "|" +city;	
	}
	
}
/**
 * @return Returns the zipcode.
 */
public String getZipcode() {
	return zipcode;
}
/**
 * @param zipcode The zipcode to set.
 */
public void setZipcode(String zipcode) {
	this.zipcode = zipcode;
}
/**
 * @return Returns the callDay.
 */
public String getCallDay() {
	return callDay;
}
/**
 * @param callDay The callDay to set.
 */
public void setCallDay(String callDay) {
	this.callDay = callDay;
}
/**
 * @return Returns the ware.
 */
public String getWare() {
	return ware;
}
/**
 * @param ware The ware to set.
 */
public void setWare(String ware) {
	this.ware = ware;
}
/**
 * @return Returns the weekVisit.
 */
public String getWeekVisit() {
	return weekVisit;
}
/**
 * @param weekVisit The weekVisit to set.
 */
public void setWeekVisit(String weekVisit) {
	this.weekVisit = weekVisit;
}
/**
 * @return Returns the average.
 */
public double getAverage() {
	return average;
}
/**
 * @param average The average to set.
 */
public void setAverage(double average) {
	this.average = average;
}
/**
 * @return Returns the supervisorFilled.
 */
public boolean isSupervisorFilled() {
	return supervisorFilled;
}
/**
 * @param supervisorFilled The supervisorFilled to set.
 */
public void setSupervisorFilled(boolean supervisorFilled) {
	this.supervisorFilled = supervisorFilled;
}
/**
 * @return Returns the debts.
 */
public Vector getDebts() {
	return debts;
}
/**
 * @param debts The debts to set.
 */
public void setDebts(Vector debts) {
	this.debts = debts;
}
/**
 * @return Returns the adAction.
 */
public boolean isAdAction() {
	return adAction;
}
/**
 * @param adAction The adAction to set.
 */
public void setAdAction(boolean adAction) {
	this.adAction = adAction;
}
/**
 * @return Returns the lastBillInfo.
 */
public String getLastBillInfo() {
	return lastBillInfo;
}
/**
 * @param lastBillInfo The lastBillInfo to set.
 */
public void setLastBillInfo(String lastBillInfo) {
	this.lastBillInfo = lastBillInfo;
}
/**
 * @return Returns the regnr.
 */
public String getRegnr() {
	return regnr;
}
/**
 * @param regnr The regnr to set.
 */
public void setRegnr(String regnr) {
	this.regnr = regnr;
}
/**
 * @return Returns the vatnr.
 */
public String getVatnr() {
	return vatnr;
}
/**
 * @param vatnr The vatnr to set.
 */
public void setVatnr(String vatnr) {
	this.vatnr = vatnr;
}

/**
 * @return the minsum
 */
public double getMinsum() {
	return minsum;
}

/**
 * @param minsum the minsum to set
 */
public void setMinsum(double minsum) {
	this.minsum = minsum;
}

/**
 * @return the maxdisc
 */
public double getMaxdisc() {
	return maxdisc;
}

/**
 * @param maxdisc the maxdisc to set
 */
public void setMaxdisc(double maxdisc) {
	this.maxdisc = maxdisc;
}

/**
 * @return the supervisor2Filled
 */
public boolean isSupervisor2Filled() {
	return supervisor2Filled;
}

/**
 * @param supervisor2Filled the supervisor2Filled to set
 */
public void setSupervisor2Filled(boolean supervisor2Filled) {
	this.supervisor2Filled = supervisor2Filled;
}

/**
 * @return the salesBlockT
 */
public boolean isSalesBlockT() {
	return salesBlockT;
}

/**
 * @param salesBlockT the salesBlockT to set
 */
public void setSalesBlockT(boolean salesBlockT) {
	this.salesBlockT = salesBlockT;
}

/**
 * @return the supervisormustfill
 */
public boolean isSupervisormustfill() {
	return supervisormustfill;
}

/**
 * @param supervisormustfill the supervisormustfill to set
 */
public void setSupervisormustfill(boolean supervisormustfill) {
	this.supervisormustfill = supervisormustfill;
}

/**
 * @return the sortCode
 */
public String getSortCode() {
	return sortCode;
}

/**
 * @param sortCode the sortCode to set
 */
public void setSortCode(String sortCode) {
	this.sortCode = sortCode;
}

/**
 * @return the returnEmail
 */
public String getReturnEmail() {
	return returnEmail;
}

/**
 * @param returnEmail the returnEmail to set
 */
public void setReturnEmail(String returnEmail) {
	this.returnEmail = returnEmail;
}

/**
 * @return the maxOrderSum
 */
public double getMaxOrderSum() {
	return maxOrderSum;
}

/**
 * @param maxOrderSum the maxOrderSum to set
 */
public void setMaxOrderSum(double maxOrderSum) {
	this.maxOrderSum = maxOrderSum;
}

public Vector getCustDataVec() {
	return custDataVec;
}

public void setCustDataVec(Vector custDataVec) {
	this.custDataVec = custDataVec;
}

public void addToCustDataVec(String data) {
	this.custDataVec.add(data);
}

public boolean isSalesBlockPrepay() {
	return salesBlockPrepay;
}

public void setSalesBlockPrepay(boolean salesBlockPrepay) {
	this.salesBlockPrepay = salesBlockPrepay;
}

public String getChain() {
	return chain;
}

public void setChain(String chain) {
	this.chain = chain;
}

public int getDelExtraDays() {
	return delExtraDays;
}

public void setDelExtraDays(int delExtraDays) {
	this.delExtraDays = delExtraDays;
}

public String getCallabi() {
	return callabi;
}

public void setCallabi(String callabi) {
	this.callabi = callabi;
}

public String getCountry() {
	return country;
}

public void setCountry(String country) {
	this.country = country;
}

public boolean isPayer() {
	return isPayer;
}

public void setIsPayer(boolean isPayer) {
	this.isPayer = isPayer;
}

public boolean isAskContactData() {
	return askContactData;
}

public void setAskContactData(boolean askContactData) {
	this.askContactData = askContactData;
}
}
