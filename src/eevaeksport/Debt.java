package eevaeksport;

import java.util.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Debt {

  public Debt() {
  }

  public void setCustID(String id){
    this.custID = id;
  }

  public String getCustID(){
    return this.custID;
  }

  public void setDoknr(String nr){
    this.doknr = nr;
  }

  public String getDoknr(){
    return this.doknr;
  }

  public void setDuedate(Date date){
    this.duedate = date;
  }

  public Date getDuedate(){
    return this.duedate;
  }

  public void setSum(double sum){
    this.sum = sum;
  }

  public double getSum(){
    return this.sum;
  }

  public void setDate(Date date){
    this.date = date;
  }

  public Date getDate(){
    return this.date;
  }

  public String getDue(){
          return due;
  }


  public void setDue(String aDue){
          due = aDue;
  }

  private String custID = null;
  private String payerID = null;
  private String doknr = null;
  private Date duedate = null;
  private double sum = 0;
  private Date date = null;
  private String due = "0";
  private double tara = 0;
  public String extraInfo  = null;

/**
 * @return Returns the tara.
 */
public double getTara() {
	return tara;
}
/**
 * @param tara The tara to set.
 */
public void setTara(double tara) {
	this.tara = tara;
}

public String getPayerID() {
	return payerID;
}

public void setPayerID(String payerID) {
	this.payerID = payerID;
}
}