package eevaeksport;

import java.io.Serializable;

import eevaeksport.util.Converter;

public class MatrixLine implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
  public MatrixLine() {
	  
  }
   public   String custID =  null;
   public   String cliGroup = null;
   public   String prodID = null;
   public   String prodGroup = null;
   public   String startDate  = null;
   public   String endDate = null;
   public   double discount = -1;
   public   double price = -1;
   public   double minAm = 0;
   public    String prodLevel = null;
   public   String discGroup = null;
   public   String currency = null;
   public   String lineinfo = null;
   public   String dealer = null;

   public String getLineinfo() {
	return lineinfo;
}

public void setLineinfo(String lineinfo) {
	this.lineinfo = lineinfo;
}

public String getCurrency() {
	return currency;
}

public void setCurrency(String currency) {
	this.currency = currency;
}

public String getString(){
	return Converter.getMatixString(this);
   }

   public String getStringForFixedAssortment(){
	   
	   return Converter.getStringForFixedAssortment(this);
   
   }
   public String getStringForLastThree(){
	   return Converter.getStringForLastThree(this);
	    
	   }

   public void setCustID(String id)
   {
       this.custID = id;
   }

   public String getCustID()
   {
       return custID;
   }

   public void setCliGroup(String cligr)
   {
       this.cliGroup = cligr;
   }

   public String getCliGroup()
   {
       return cliGroup;
   }


   public double getDiscount()
   {
       return discount;
   }

   public void setDiscount(double discount)
   {
       this.discount = discount;
   }

   public double getPrice()
   {
       return price;
   }

   public void setPrice(double price)
   {
       this.price = price;
   }

   public void setProdID(String prod)
   {
       this.prodID = prod;
   }

   public String getProdID()
   {
       return prodID;
   }

   public void setProdGroup(String gr)
   {
       this.prodGroup = gr;
   }

   public String getProdGroup()
   {
       return prodGroup;
   }



/**
 * @return Returns the discGroup.
 */
public String getDiscGroup() {
	return discGroup;
}
/**
 * @param discGroup The discGroup to set.
 */
public void setDiscGroup(String discGroup) {
	this.discGroup = discGroup;
}

/**
 * @return the prodLevel
 */
public String getProdLevel() {
	return prodLevel;
}

/**
 * @param prodLevel the prodLevel to set
 */
public void setProdLevel(String prodLevel) {
	this.prodLevel = prodLevel;
}

/**
 * @return the startDate
 */
public String getStartDate() {
	return startDate;
}

/**
 * @param startDate the startDate to set
 */
public void setStartDate(String startDate) {
	this.startDate = startDate;
}

/**
 * @return the endDate
 */
public String getEndDate() {
	return endDate;
}

/**
 * @param endDate the endDate to set
 */
public void setEndDate(String endDate) {
	this.endDate = endDate;
}

public double getMinAm() {
	return minAm;
}

public void setMinAm(double minAm) {
	this.minAm = minAm;
}
}
