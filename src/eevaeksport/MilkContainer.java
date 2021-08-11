package eevaeksport;

import java.text.NumberFormat;
import java.util.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MilkContainer {


  private String custid = null;
  private Date kuupaev = null;
  private double fatOfficial = 0;
  private double protOfficial = 0;
  private double fatOwn = 0;
  private double protOwn = 0;
  private double milkamount = 0;
  private double milkOwn = 0;
  private double milkOfficial = 0;
  private double vaheRBA = 0;
  private double vaheR = 0;

  
  private double getKoef(double fat,double prot){
	     	double res = 1;
       res = 1+(prot-3.4)*0.178+(fat-3)*0.267;	 	
  	return res;
  }

  private double getKoefR(double fat,double milk){
   	double res = 1;
 res = (milk * fat)/3.4;	 	
return res;
}

public String getCustid() {
	return custid;
}

NumberFormat nf = NumberFormat.getInstance();
     


public double getMilkOwn() {
	milkOwn = this.milkamount * getKoef(this.fatOwn, this.protOwn);
	return milkOwn;
}


public double getMilkOfficial() {
	milkOfficial = this.milkamount * getKoef(this.fatOfficial, this.protOfficial);
	return milkOfficial;
}


public double getVaheR() {
	vaheR =  getKoefR(this.fatOfficial, this.milkamount);
	return vaheR;
}


public double getVaheRBA() {
	if(this.fatOfficial == 0){
		return 0;
	}
	return getMilkOfficial() - getMilkOwn();
}

public String getVahe(){
	  nf.setMinimumFractionDigits(2);
      nf.setMaximumFractionDigits(2);
      double res = getVaheRBA();
      boolean nega = res < 0;
      if(!nega){
    	  return " " + nf.format(res);
      }
	return nf.format(res);
}

public void setCustid(String custid) {
	this.custid = custid;
}


public Date getKuupaev() {
	return kuupaev;
}


public void setKuupaev(Date kuupaev) {
	this.kuupaev = kuupaev;
}


public double getFatOfficial() {
	return fatOfficial;
}


public void setFatOfficial(double fatOfficial) {
	this.fatOfficial = fatOfficial;
}


public double getProtOfficial() {
	return protOfficial;
}


public void setProtOfficial(double protOfficial) {
	this.protOfficial = protOfficial;
}


public double getFatOwn() {
	return fatOwn;
}


public void setFatOwn(double fatOwn) {
	this.fatOwn = fatOwn;
}


public double getProtOwn() {
	return protOwn;
}


public void setProtOwn(double protOwn) {
	this.protOwn = protOwn;
}


public double getMilkamount() {
	return milkamount;
}


public void setMilkamount(double milkamount) {
	this.milkamount = milkamount;
}



}