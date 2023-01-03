package eevaeksport.imp;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import eevaeksport.Product;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class Order {

  public Order(String company, String agentFirstName) {
    this.company = company;
    this.agentFirstName = agentFirstName;
  }

  public String getCompany() {
    return company;
  }
  
  public String getSignature(){
	  String res = "";
	  try{
		  res = getCompany() + "" + getOrderLines().size() + "" + 
	  this.getTotalAmountOfUnits() +""+ getTotalSum() + "" + getCodes() + "" +getPartiis()+""+
				  this.getShipCal().getTime().toLocaleString().substring(0, 10)+ "" +this.getInfo();
	  }catch(Exception e){
		  
	  }
	  return res;
  }

  public Vector getOrderLines() {
    return this.orderLines;
  }

  public void setOrderLines(Vector lines ) {
    orderLines = lines;
  }
  
  public int getTotalAmountOfUnits(){
	  int res = 1 ;
	 Enumeration enume =  orderLines.elements();
	 while(enume.hasMoreElements()){
		 OrderLine ol  = (OrderLine)enume.nextElement();
		 res = res + (int)ol.getKogus();
	 }
	  return res;
  }
  
  
  public int getTotalSum(){
	  int res = 1 ;
	 Enumeration enume =  orderLines.elements();
	 while(enume.hasMoreElements()){
		 OrderLine ol  = (OrderLine)enume.nextElement();
		 res = res + (int)ol.getPrice();
	 }
	  return res;
  }
  
  public double getCalcSum(){
	  double res = 1 ;
	 Enumeration enume =  orderLines.elements();
	 while(enume.hasMoreElements()){
		 OrderLine ol  = (OrderLine)enume.nextElement();
		 res = res + (ol.getPrice() * ol.getKogus());
	 }
	  return res;
  }
  public String getCodes(){
	  String res = "" ;
	 Enumeration enume =  orderLines.elements();
	 while(enume.hasMoreElements()){
		 OrderLine ol  = (OrderLine)enume.nextElement();
		 res = res + ol.getToode();
	 }
	  return res;
  }
  public String getPartiis(){
	  String res = "" ;
	 Enumeration enume =  orderLines.elements();
	 while(enume.hasMoreElements()){
		 OrderLine ol  = (OrderLine)enume.nextElement();
		 if(ol.getPartii()!= null)
		 res = res + ol.getPartii();
	 }
	  return res;
  }
  
  public void add(String toode, double kogus, double bonus, double discount,
                  String productionCode, double price, String name , String unit ,String partii,String ean
                  ,String dealer ,String selActCode, String docType ) {
    OrderLine line = new OrderLine();
    line.setToode(toode);
    line.setKogus(kogus);
    line.setBonus(bonus);
    line.setTooteNimi(name);
    line.setDiscount(discount);
    line.setPartii(partii);
    line.setEAN(ean);
    line.setDealer(dealer);
    line.selActCode = selActCode;
    line.docType = docType;
    try{
    	line.setPl(Integer.parseInt(productionCode));
        	
    }catch (Exception e){
    	
    }
    line.setPrice(price);
    line.setUnit(unit);
    orderLines.addElement(line);
  }

  public void setDokNr(String nr) {
    this.dokNr = nr;
  }

  public String getDokNr() {
    return this.dokNr;
  }

  public String getAgentFirstName() {
    return this.agentFirstName;
  }

  public String getAgent() {
    return this.agentFirstName;
  }

  public String setAgentFirstName(String agent) {
	    return this.agentFirstName = agent;
	  }

  
  /**
   *
   * @return the String value of dealer.
   */
  public String getDealer() {
    return dealer;
  }

  /**
   *
   * @param aDealer - the new value for dealer
   */
  public void setDealer(String aDealer) {
    dealer = aDealer;
  }

  /**
   *
   * @return the String value of emailBody.
   */
  public String getEmailBody() {
    return emailBody;
  }

  /**
   *
   * @param aEmailBody - the new value for emailBody
   */
  public void setEmailBody(String aEmailBody) {
    emailBody = aEmailBody;
  }


  public String getAgentCode() {
    return agentCode;
  }

  /**
   *
   * @param aEmailBody - the new value for emailBody
   */
  public void setAgentCode(String aaa) {
    agentCode = aaa;
  }


  /**
   *
   * @return the double value of sum.
   */
  public double getSum() {
    return sum;
  }

  /**
   *
   * @param aSum - the new value for sum
   */
  public void setSum(double aSum) {
    sum = aSum;
  }

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
   * @return the int value of paymTerm.
   */
  public int getPaymTerm() {
    return paymTerm;
  }

  /**
   *
   * @param aPaymTerm - the new value for paymTerm
   */
  public void setPaymTerm(int aPaymTerm) {
    paymTerm = aPaymTerm;
  }

  /**
   *
   * @return true if isBill is set to true.
   */
  public boolean isIsBill() {
    return isBill;
  }

  /**
   *
   * @param aIsBill - the new value for isBill
   */
  public void setIsBill(boolean aIsBill) {
    isBill = aIsBill;
  }
  

  private String company;
  private String compName;
  private Vector orderLines = new Vector();
  private String agentFirstName = null;
  private String warehouse = null;
  private String dokNr = null;
  private String dealer = null;
  private String emailBody = null;
  public String driver = null;
  public String car = null;
  public String trailer = null;
  public int carsec1 = 0;
  public int carsec2 = 0;
  public int carsec3 = 0;
  public int trasec1 = 0;
  public int trasec2 = 0;
  public int trasec3 = 0;
  public int trasec4 = 0;
  public String dstart = null;
  public double wplanned = 0;
  public String dend = null;
  public String colorStatus = null;
  
  public String filename = null;
  
  public String routeName = null;
  public String id = null;
  private String contactID = null;
  public String getContactID() {
	return contactID;
}

public void setContactID(String contactID) {
	this.contactID = contactID;
}




  public String fullDelyveryOnly = null;
  private String photoname = null;
  private String signaname = null;
  private String info = null;
  private boolean isBill = false;
  private boolean isPilot = false;
  private boolean shipDateUsed = false;
  private int paymTerm = 0;
  private double km = 0;
  private double kogusumma = 0;
  private double kmsumma = 0;
  private double ymmardus = 0;
  private Calendar shipCal = Calendar.getInstance();
  private double sum = 0;
  private String agentCode = null;
  public String restPriority = null;
  public Date completedDate= null;
  public Date deliveryDate= null;
  public String completedMsisdn = null;
  public String docType = null;
  public String completedComment = null;
  public String extDoc = null;
  public String partner = null;
  public String origId = null;
  public String extId = null;
  public String route = null;
  
  public String routeToNext = null;
  
  public Vector<Product> prodLines = null;
  
  
  private int status = 0;
  private String reference = null;
  private String shipMode = null;
  private String shipTime = null;
  private String ILN = null;

  private String teostaja = null;
  public String getTeostaja() {
	return teostaja;
}

public void setTeostaja(String teostaja) {
	this.teostaja = teostaja;
}

private String currency = null;
  private boolean changed = false;
public String duration = null;

  /**
   *
   * @return the Calendar value of shipCal.
   */
  public Calendar getShipCal() {
    return this.shipCal;
  }

  public int getStatus() {
    return this.status;
  }

  public void setStatus(int st) {
     this.status = st;
   }

  /**
   *
   * @param aShipCal - the new value for shipCal
   */
  public void setShipCal(String hipCal) {
    try {
      if (hipCal != null && hipCal.trim().length() > 0) {
        long lon = Long.parseLong(hipCal);
        this.shipCal.setTimeInMillis(lon);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

/**
 * @return Returns the info.
 */
public String getInfo() {
	return info;
}
/**
 * @param info The info to set.
 */
public void setInfo(String info) {
	this.info = info;
}
/**
 * @return Returns the reference.
 */
public String getReference() {
	return reference;
}
/**
 * @param reference The reference to set.
 */
public void setReference(String reference) {
	this.reference = reference;
}
/**
 * @return Returns the warehouse.
 */
public String getWarehouse() {
	return warehouse;
}
/**
 * @param warehouse The warehouse to set.
 */
public void setWarehouse(String warehouse) {
	this.warehouse = warehouse;
}
/**
 * @return Returns the km.
 */
public double getKm() {
	return km;
}
/**
 * @param km The km to set.
 */
public void setKm(double km) {
	this.km = km;
}
/**
 * @return Returns the kmsumma.
 */
public double getKmsumma() {
	return kmsumma;
}
/**
 * @param kmsumma The kmsumma to set.
 */
public void setKmsumma(double kmsumma) {
	this.kmsumma = kmsumma;
}
/**
 * @return Returns the kogusumma.
 */
public double getKogusumma() {
	return kogusumma;
}
/**
 * @param kogusumma The kogusumma to set.
 */
public void setKogusumma(double kogusumma) {
	this.kogusumma = kogusumma;
}
/**
 * @return Returns the ymmardus.
 */
public double getYmmardus() {
	return ymmardus;
}
/**
 * @param ymmardus The ymmardus to set.
 */
public void setYmmardus(double ymmardus) {
	this.ymmardus = ymmardus;
}

/**
 * @return the compName
 */
public String getCompName() {
	return compName;
}

/**
 * @param compName the compName to set
 */
public void setCompName(String compName) {
	this.compName = compName;
}

/**
 * @return the shipMode
 */
public String getShipMode() {
	return shipMode;
}

/**
 * @param shipMode the shipMode to set
 */
public void setShipMode(String shipMode) {
	this.shipMode = shipMode;
}

/**
 * @return the shipTime
 */
public String getShipTime() {
	return shipTime;
}

/**
 * @param shipTime the shipTime to set
 */
public void setShipTime(String shipTime) {
	this.shipTime = shipTime;
}

/**
 * @return the changed
 */
public boolean isChanged() {
	return changed;
}

/**
 * @param changed the changed to set
 */
public void setChanged(boolean changed) {
	this.changed = changed;
}

/**
 * @return the currency
 */
public String getCurrency() {
	return currency;
}

/**
 * @param currency the currency to set
 */
public void setCurrency(String currency) {
	this.currency = currency;
}

public String getILN() {
	return ILN;
}

public void setILN(String iLN) {
	ILN = iLN;
}

public boolean isShipDateUsed() {
	return shipDateUsed;
}

public void setShipDateUsed(boolean shipDateUsed) {
	this.shipDateUsed = shipDateUsed;
}

public String getPhotoname() {
	return photoname;
}

public void setPhotoname(String photoname) {
	this.photoname = photoname;
}

public String getSignaname() {
	return signaname;
}

public void setSignaname(String signaname) {
	this.signaname = signaname;
}
}
