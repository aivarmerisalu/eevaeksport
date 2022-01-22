package eevaeksport.imp;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public class Orders {

  public Orders(String agent, boolean isPilot) {
    this.agent = agent;
    this.setAgentIsPilot(isPilot);
  }

  public void add(String firma, String toode, String prodname ,double kogus, String dokNr, String dealer, double bonus,
                  double discount, String shipdate, String productionCode,double price,int paymterm,boolean isBill,String agentCode,
				  int status,String unit,String warehouse, String partii,String ean,String shiptime,boolean shipDateUsed,
				  String selActCode, String docType){
  	existingOrder = null;
    Enumeration enumd = orders.elements();
    while (enumd.hasMoreElements()){
      Order order = (Order)enumd.nextElement();
      if(order.getDealer() == null || order.getDealer().startsWith("EQ") ){
      	if (order.getCompany() != null  && order.getCompany().equalsIgnoreCase(firma)){
            existingOrder = order;
          }
      	if (order.getDokNr() != null && order.getDokNr().equalsIgnoreCase(dokNr)){
            existingOrder = order;
          }
      	
      	
      }else{

      	if (order.getCompany().equalsIgnoreCase(firma) && order.getDealer().equalsIgnoreCase(dealer) ){
            existingOrder = order;
          }
      }
    }
    if (existingOrder == null){

      existingOrder = new Order(firma, getAgent());
      existingOrder.setShipCal(shipdate);
      existingOrder.setShipTime(shiptime);
      existingOrder.setEmailBody(getEmailBody());
      existingOrder.setCompName(getCompName());
      existingOrder.setInfo(getInfo());
      existingOrder.setReference(getInfo());
      existingOrder.add(toode, kogus, bonus, discount,productionCode,price,prodname,unit,partii,ean,dealer,selActCode,docType);
      existingOrder.setDokNr(dokNr);
      existingOrder.setDealer(dealer);
      existingOrder.setIsPilot(this.isAgentIsPilot());
      existingOrder.setPaymTerm(paymterm);
      existingOrder.setIsBill(isBill);
      existingOrder.setShipDateUsed(shipDateUsed);
      existingOrder.setAgentCode(agentCode);
      existingOrder.setStatus(status);
      existingOrder.setWarehouse(warehouse);
      existingOrder.setKm(getKm());
      existingOrder.setKogusumma(getSumma());
      existingOrder.setKmsumma(getKmsumma());
      existingOrder.setYmmardus(this.getYm());
      existingOrder.setCurrency(this.getCurrency());
      existingOrder.setTeostaja(this.getTeostaja());
      existingOrder.setILN(this.getILN());
      
      orders.add(existingOrder);
      
      System.out.println("Esimene " + existingOrder.getILN() + " " + existingOrder.isShipDateUsed() );
    } else {
      existingOrder.add(toode, kogus, bonus, discount,productionCode,price,prodname,unit,partii,ean,dealer,selActCode,docType);
      existingOrder.setDokNr(dokNr);
      existingOrder.setCurrency(currency);
      existingOrder.setTeostaja(teostaja);
      existingOrder.setILN(this.ILN);
      existingOrder.setKogusumma(getSumma());
      existingOrder.setKmsumma(getKmsumma());
      existingOrder.setShipDateUsed(shipDateUsed);
     // existingOrder.setShipTime(shiptime);
      
     // System.out.println("teine " + existingOrder.getILN());
      }
  }

  public Vector getOrders(){
    return orders;
  }

  public String getAgent(){
    return this.agent;
  }

  /**
   *
   * @return the String value of emailBody.
   */
  public String getEmailBody(){
    return emailBody;
  }

  /**
   *
   * @param aEmailBody - the new value for emailBody
   */
  public void setEmailBody(String aEmailBody){
    emailBody = aEmailBody;
  }

  private Vector orders = new Vector();
  public String agent = null;
  private String compName = null;
  private String emailBody = null;
  private double summa = 0; 
  private double kmsumma = 0;
  private double km = 0;
  private double ym = 0;
  private String currency = null;
  private String ILN = null;
  private String info = null;
  private String shipMode = null;
  private String photoname = null;
  private String signaname = null;
  private String contactid = null;
  private String fullDelyveryOnly = null;
  
  private String shipTime = null;
  private String teostaja = null;
  public String getTeostaja() {
	return teostaja;
}

public void setTeostaja(String teostaja) {
	if(existingOrder != null){
		existingOrder.setTeostaja(teostaja);
	}
	this.teostaja = teostaja;
}

private boolean agentIsPilot = false;
  private Order existingOrder = null;


  public boolean isAgentIsPilot() {
    return agentIsPilot;
  }

  public void setAgentIsPilot(boolean aIsPilot) {
    agentIsPilot = aIsPilot;
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
	if(existingOrder != null){
		existingOrder.setReference(info);
		existingOrder.setInfo(info);
	}
	this.info = info;
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
 * @return Returns the summa.
 */
public double getSumma() {
	return summa;
}
/**
 * @param summa The summa to set.
 */
public void setSumma(double summa) {
	this.summa = summa;
}
/**
 * @return Returns the ym.
 */
public double getYm() {
	return ym;
}
/**
 * @param ym The ym to set.
 */
public void setYm(double ym) {
	this.ym = ym;
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
public void setShipMode( String mode) {
	
		if(existingOrder != null){
			existingOrder.setShipMode(mode);
		}
		this.shipMode = mode;
	
}

public void setPhotoName( String mode) {
	
	if(existingOrder != null){
		existingOrder.setPhotoname(mode);
	}
	this.photoname = mode;

}

public void setSignaName( String mode) {
	
	if(existingOrder != null){
		existingOrder.setSignaname(mode);
	}
	this.signaname = mode;

}

public void setContactID( String mode) {
	
	if(existingOrder != null){
		existingOrder.setContactID(mode);
	}
	this.contactid = mode;

}

public void setFullDeliveryOnly( String mode) {
	
	if(existingOrder != null){
		existingOrder.fullDelyveryOnly = mode ;
	}
	this.fullDelyveryOnly = mode;

}

public void setShipTime( String mode) {
	
	if(existingOrder != null){
		existingOrder.setShipTime(mode);
	}
	this.shipTime = mode;

}


/**
 * @return the shipMode
 */
public String getShipMode() {
	return shipMode;
}

public String getShipTime() {
	return shipTime;
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
	if(existingOrder != null){
		existingOrder.setILN(iLN);
	}
	ILN = iLN;
}

public String getPhotoname() {
	return photoname;
}



public String getSignaname() {
	return signaname;
}

public String getContactID() {
	return contactid;
}

/**
 * @param shipMode the shipMode to set
 */

}
