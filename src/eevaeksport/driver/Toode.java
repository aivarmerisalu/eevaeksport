package eevaeksport.driver;

import java.sql.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Toode {

  public Toode(String ID) {
    this.ID = ID;
  }

  public void setAlguspaev(Date alguspaev){
    this.alguspaev = alguspaev;
  }

  public void setKogus(double kogus){
    this.kogus = kogus;
  }

  public void setKastis(double kogus){
     this.kastis = kogus;
   }

  public double getKogus(){
    return this.kogus;
  }
  public double getKastis(){
   return this.kastis;
 }


  public void addKogus(double kogus){
    this.kogus = this.kogus + kogus;
  }

  public String toString(){
    StringBuffer sb = new StringBuffer();
    sb.append("ID = " + ID + "\n");
    sb.append("\tkogus = " + kogus + "\n");
    sb.append("\talguspaev = " + alguspaev + "\n");
    sb.append("\tplancur = " + plancur + "\n");
    sb.append("\tdonecur = " + donecur + "\n");
    sb.append("\tplanvol = " + planvol + "\n");
    sb.append("\tdonevol = " + donevol + "\n");
    sb.append("\tbrand = " + brand + "\n");
    
    return sb.toString();
  }

  private String ID = null;
  private Date alguspaev = null;
  private String ladu = null;
  private String gr1 = null;
  private String gr2 = null;
  private double kogus = 0;
  private double kastis = 0;
  private double planvol = 0;
  private double donevol = 0;
  private double plancur = 0;
  private double donecur = 0;
  private String brand = null;
  public String comment = null;
public double getPlanvol() {
	return planvol;
}

public void setPlanvol(double planvol) {
	this.planvol = planvol;
}

public double getDonevol() {
	return donevol;
}

public void setDonevol(double donevol) {
	this.donevol = donevol;
}

public double getPlancur() {
	return plancur;
}

public void setPlancur(double plancur) {
	this.plancur = plancur;
}

public double getDonecur() {
	return donecur;
}

public void setDonecur(double donecur) {
	this.donecur = donecur;
}

public String getBrand() {
	return brand;
}

public void setBrand(String brand) {
	this.brand = brand;
}

/**
 * @return Returns the ladu.
 */
public String getLadu() {
	return ladu;
}
/**
 * @param ladu The ladu to set.
 */
public void setLadu(String ladu) {
	this.ladu = ladu;
}

/**
 * @return the iD
 */
public String getID() {
	return ID;
}

/**
 * @return the gr1
 */
public String getGr1() {
	return gr1;
}

/**
 * @param gr1 the gr1 to set
 */
public void setGr1(String gr1) {
	this.gr1 = gr1;
}

/**
 * @return the gr2
 */
public String getGr2() {
	return gr2;
}

/**
 * @param gr2 the gr2 to set
 */
public void setGr2(String gr2) {
	this.gr2 = gr2;
}
}