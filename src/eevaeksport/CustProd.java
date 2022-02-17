package eevaeksport;

import java.io.Serializable;
import java.sql.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class CustProd implements Serializable {
	 static final long serialVersionUID = 2L; 
  private String custID = null;
  private String prodID = null;
  public String soldString = null;
  private String group = null;
  public String getGroup() {
	return group;
}

public void setGroup(String group) {
	this.group = group;
}
  public double lastAmount = 0;
  public String lastAmountDate = " ";
  private double movedStockPluss = 0;
  private double movedStockMinus = 0;
  private double lastPrice = 0;
  //l�bi lastPrice = -1 teeme systeemi kus n�itame fixsortimendis eraldi kui mitt olemas olevat 
  private double fixPrice = 0;
  private double openAmount = 0;
  public String s1 = "=";
  public String s2 = "=";
  public String s3 = "=";
  public String s4 = "=";
  public String s5 = "=";
  public double ale = 0; 
  private String redline = "0";
  private String abiKood = " ";
  private Date reportmonth = null;
  public java.util.Date checkdate = null;
  
  /**
   *
   * @param aS5 - the new value for s5
   */
  public void setS1(String aS1) {
    s1 = aS1;
  }

  public void setS2(String aS2) {
    s2 = aS2;
  }

  public void setS3(String aS3) {
    s3 = aS3;
  }

  public void setS4(String aS4) {
    s4 = aS4;
  }

  public void setS5(String aS5) {
    s5 = aS5;
  }

 

  public double getLastAmount(){
    return lastAmount;
  }

  public double getOpenAmount(){
    return openAmount;
  }

  public double getLastPrice(){
     return lastPrice;
   }
   public double getFixPrice(){
      return fixPrice;
    }


 public double getMovedStockPluss(){
   return movedStockPluss;
 }



  public double getMovedStockMinus(){
    return movedStockMinus;
  }

  /**
   *
   * @return the String value of custID.
   */
  public String getCustID(){
    return custID;
  }

  /**
   *
   * @return the Product value of prod.
   */
  public String getProdID(){
    return prodID;
  }


  public String getLastAmountDate(){
    return lastAmountDate;
  }


  public boolean equals(Object o){
    if (o instanceof CustProd){
      CustProd cp = (CustProd)o;
      if (cp.getCustID().equals(getCustID())){
        if (cp.getProdID().equals(getProdID())){
          return true;
        }
      }

    }
    return false;
  }

  public CustProd(String custID, String prodID, double lastAmount ,double movedStockPluss ,double movedStockMinus,String lastAmountDate,double lastPricer,double openAmounter,double fixPricer,String soldString) {
    this.custID = custID;
    this.prodID = prodID;
    this.lastAmount = lastAmount;
    this.setMovedStockPluss(movedStockPluss);
    this.movedStockMinus = movedStockMinus;
    this.lastAmountDate = lastAmountDate;
    this.lastPrice = lastPricer;
    this.fixPrice = fixPricer;
    this.openAmount = openAmounter;
    this.soldString = soldString;
  }

  public void setMovedStockPluss(final double movedStockPluss) {
        this.movedStockPluss = movedStockPluss;
  }
  public void setOpenAmount(final double openAmounter) {
        this.openAmount = openAmounter;
  }
  public void setMovedStockMinus(final double movedStockMinus) {
        this.movedStockMinus = movedStockMinus;
  }
/**
 * @return Returns the redline.
 */
public String getRedline() {
	return redline;
}
/**
 * @param redline The redline to set.
 */
public void setRedline(String redline) {
	this.redline = redline;
}
/**
 * @return Returns the reportmonth.
 */
public Date getReportmonth() {
	return reportmonth;
}
/**
 * @param reportmonth The reportmonth to set.
 */
public void setReportmonth(Date reportmonth) {
	this.reportmonth = reportmonth;
}
/**
 * @return Returns the abiKood.
 */
public String getAbiKood() {
	return abiKood;
}
/**
 * @param abiKood The abiKood to set.
 */
public void setAbiKood(String abiKood) {
	this.abiKood = abiKood;
}
}
