package eevaeksport.imp;

import java.io.Serializable;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class OrderLine implements Serializable {

	  private String toode = null;
	  private double kogus = -1;
	  private double bonus = 0;
	  private double discount = 0;
	  private String partii = null;
	  private String dealer = null;
	  public String selActCode = null;
	  public String docType = null;
	  
    public String getDealer() {
		return dealer;
	}

	public void setDealer(String dealer) {
		this.dealer = dealer;
	}
	private String ean = null;
    private int pl = 0;
    private double price = 0;
    private String prodname = " ";
    private String unit = " " ;

	/** 
	*
	* @return the double value of price.
	*/
	public double getPrice(){
		return price;
	}

	/**
	*
	* @param aPrice - the new value for price
	*/
	public void setPrice(double aPrice){
		price = aPrice;
	}




	/**
	*
	* @return the int value of pl.
	*/
	public int getPl(){
		return pl;
	}

	/**
	*
	* @param aPl - the new value for pl
	*/
	public void setPl(int aPl){
		pl = aPl;
	}






	/**
	*
	* @return the double value of discount.
	*/
	public double getDiscount(){
		return discount;
	}

	/**
	*
	* @param aDiscount - the new value for discount
	*/
	public void setDiscount(double aDiscount){
		discount = aDiscount;
	}
        public String getUnit(){
           return unit;
         }
         public void setUnit(String aToode){
            unit = aToode;
          }



  public OrderLine() {
  }

  /**
   *
   * @return the String value of toode.
   */
  public String getToode(){
    return toode;
  }
  public String getTooteNimi(){
     return prodname;
   }
   public void setTooteNimi(String aToode){
      prodname = aToode;
    }

  /**
   *
   * @param aToode - the new value for toode
   */
  public void setToode(String aToode){
    toode = aToode;
  }
  public String getEAN(){
     return ean;
   }

   /**
    *
    * @param aToode - the new value for toode
    */
   public void setEAN(String aToode){
     ean = aToode;
   }


  /**
   *
   * @return the double value of kogus.
   */
  public double getKogus(){
    return kogus;
  }

  /**
   *
   * @param aKogus - the new value for kogus
   */
  public void setKogus(double aKogus){
    kogus = aKogus;
  }

  /**
   *
   * @return the double value of bonus.
   */
  public double getBonus(){
    return bonus;
  }

  /**
   *
   * @param aBonus - the new value for bonus
   */
  public void setBonus(double aBonus){
    bonus = aBonus;
  }


/**
 * @return Returns the partii.
 */
public String getPartii() {
	return partii;
}
/**
 * @param partii The partii to set.
 */
public void setPartii(String partii) {
	this.partii = partii;
}
}
