/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package eevaeksport;

import java.util.Date;
import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class Product {

    /**
     * Constructor declaration
     *
     *
     * @param id
     *
     * @see
     */
    public Product(String id) {
	this.id = id;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getID() {
	return this.id;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getName() {
    	if(name != null) {
    		return name.replaceAll("~","-");
    	}
	return null;
    }

    /**
     * Method declaration
     *
     *
     * @param name
     *
     * @see
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Method declaration
     *
     *
     * @param incases
     *
     * @see
     */
    public void setInCases(double incases) {
	this.inCases = incases;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public double getInCases() {
	return this.inCases;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String toString() {
	return "id=" + id + ";name=" + name + ";kogus=" + stock;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public double getPrice() {
	return price;
    }

    /**
     * Method declaration
     *
     *
     * @param price
     *
     * @see
     */
    public void setPrice(double price) {
	this.price = price;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public double getStock() {
	return stock;
    }

    /**
     * Method declaration
     *
     *
     * @param stock
     *
     * @see
     */
    public void setStock(double stock) {
	this.stock = stock;
    }

    /**
     *
     * @return the String value of group.
     */
    public String getGroup() {
	return group;
    }

    /**
     *
     * @param aGroup - the new value for group
     */
    public void setGroup(String aGroup) {
	group = aGroup;
    }

    /**
     *
     * @return the double value of measure.
     */
    public double getMeasure() {
	return measure;
    }

    /**
     *
     * @param aMeasure - the new value for measure
     */
    public void setMeasure(double aMeasure) {
	measure = aMeasure;
    }

    /**
    *
    * @return the double value of maxDiscount.
    */
    public double getMaxDiscount(){
            return maxDiscount;
    }

    /**
    *
    * @param aMaxDiscount - the new value for maxDiscount
    */
    public void setMaxDiscount(double aMaxDiscount){
            maxDiscount = aMaxDiscount;
    }

    /**
     *
     * @return the String value of EAN.
     */
    public String getEAN(){
      return EAN;
    }
    public String getSendSeparate(){
      return sendSeparate;
    }

    /**
     *
     * @param aEAN - the new value for EAN
     */
    public void setEAN(String aEAN){
      EAN = aEAN;
    }

    public void setSendSeparate(String aEAN){
          sendSeparate = aEAN;
        }

        public void setStatusDiscount(String status){
          statusDiscount = status;
        }

        public String getStatusDiscount(){
          return statusDiscount;
        }

        public void setStatusSellout(String status){
          statusSellout = status;
        }

        public String getStatusSellout(){
          return statusSellout;
        }
        
        public String suggAle = null;

        public void setStatusNew(String status){
          statusNew = status;
        }

        public String getStatusNew(){
          return statusNew;
        }


        public void setStatusTop(String status){
          statusTOP = status;
        }

        public String getStatusTop(){
          return statusTOP;
        }
        public String getPartii(){
          return partii;
        }


        public void setPartii(String status){
          partii = status;
        }


    public String idabi = null;
    private String sendSeparate = " ";
    private Vector barcodes = new Vector();
    private String id = null;
    private String name = null;
    public String weblink = null;
    private boolean allowGift = true;
    private String alegrupp = " ";
    private double inCases = 0;
    private double price = 0;
    private double stock = -1;
    private long timeToSell = 0;
    private String group = " ";
    private String groupname = null;
    private String groupSecname = null;
    private double measure = 0;
    private double boxbrutto = 0;
    public double greendisc = 0;
    private double boxnetto = 0;
    private double maxDiscount = 0;
    private double minPrice = 0;
    private boolean label = false;
    private boolean noCommas = false;
    public String licence = null;
    private String nameSorting = null;
    public String getNameSorting() {
    	if(nameSorting == null){
    		return name;
    	}
		return nameSorting;
	}

	public void setNameSorting(String nameSorting) {
		this.nameSorting = nameSorting;
	}

	public double getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(double minPrice) {
		this.minPrice = minPrice;
	}



	private String defectiveList = " ";
    private double palletSpace = 0;
    public double innerCaseContent = 0;
    private String photoid = " ";
    private String EAN = " ";
    private String agentProduct = null;
    private double onPallet = 0 ;
    private String unit = " ";
    private String sorting = " " ;
    private String oldname = " ";
    private double inunit = 0;
    private String statusMustbe = " " ;
    private String statusDiscount = " " ;
    private String statusGiftPack = " " ;
    public String statusSmallPack = " " ;
    private String statusSellout = " " ;
    private String statusNew = " " ;
    private String statusTOP = " " ;
    private String partii = " " ;
    private String eanCase = " ";
    private String type = " ";
    private String type3 = " ";
    private String prodDiscinfo = " ";
    public String getProdDiscinfo() {
		return prodDiscinfo;
	}

	public void setProdDiscinfo(String prodDiscinfo) {
		this.prodDiscinfo = prodDiscinfo;
	}

	public String getType3() {
		return type3;
	}

	public void setType3(String type3) {
		this.type3 = type3;
	}



	private String vat = "20" ;
    private boolean showOnSeparateOrderList = false;
    private String mainWarehouseStock = null;
    private String groupSecond = " ";
    private String margin = "0";
    private String sendExtra = " ";
    private String agentGroup = " ";
    private String ediname = " "; 
    private boolean diffColor = false;
    public boolean sumNoCount = false;
    private double priceSecond = 0;
    private double optAmountInUnits = -2;
    private double minAmountInUnits = -2;
    private String type2 = " ";
    private String typeSimi = " ";
    public String getType2() {
    	return type2;
    }

    public void setType2(String type2) {
    	this.type2 = type2;
    }
    public double getMinAmountInUnits() {
		return minAmountInUnits;
	}

	public void setMinAmountInUnits(double minAmountInUnits) {
		this.minAmountInUnits = minAmountInUnits;
	}



	private String pandPakendiKood = " ";
    private Date date1 = null;
	public String restName = null;
	public String sortRest = null;
	public double layer = 0;
	public Integer points = 0;
	public boolean noOrder = false;
	
   
   public double getOptAmountInUnits() {
   	return optAmountInUnits;
   }
   public void setOptAmountInUnits(double otpAmountInUnit) {
   	this.optAmountInUnits = otpAmountInUnit;
   }
   
	/**
	*
	* @return the double value of priceSecond.
	*/
	public double getPriceSecond(){
		return priceSecond;
	}

	/**
	*
	* @param aPriceSecond - the new value for priceSecond
	*/
	public void setPriceSecond(double aPriceSecond){
		priceSecond = aPriceSecond;
	}

        /**
        *
        * @return the double value of priceSecond.
        */
        public double getInUnit(){
                return inunit;
        }

        /**
        *
        * @param aPriceSecond - the new value for priceSecond
        */
        public void setInUnit(double ain){
                inunit = ain;
        }



   public void setDiffColor(boolean bb){
   diffColor = bb;
   }

   public boolean isDiffColor(){
   return diffColor;
   }
        public String getSendExtra() {
           return sendExtra;
         }

         public void setSentExtra(String aVat) {
              sendExtra = aVat;
            }

    public String getMargin() {
          return margin;
        }

        public String getOldName() {
              return oldname;
            }

    public void setMargin(String aVat) {
          margin = aVat;
        }
        public void setOldName(String aVat) {
                 oldname = aVat;
               }



    public double getPalSpace() {
          return palletSpace;
        }

    public void setPalSpace(double aVat) {
          palletSpace = aVat;
        }


    public String getVat() {
           return vat;
        }

    public String getGroupSecond() {
      return groupSecond;
    }

    public void setGroupSecond(String aVat) {
      groupSecond = aVat;
    }

    public void setVat(String aVat) {
      vat = aVat;
    }

    public String getMustBe() {
      return this.statusMustbe;
    }

    /**
     *
     * @param aSorting - the new value for sorting
     */
    public void setMustBe(String aFocus) {
      statusMustbe = aFocus;
    }

    /**
     *
     * @return the String value of sorting.
     */
    public String getSorting() {
      return sorting;
    }

    /**
     *
     * @param aSorting - the new value for sorting
     */
    public void setSorting(String aSorting) {
		sorting = aSorting;
	}




	/**
	*
	* @return the String value of unit.
	*/
	public String getUnit(){
		return unit;
	}

	/**
	*
	* @param aUnit - the new value for unit
	*/
	public void setUnit(String aUnit){
		unit = aUnit;
	}




	/**
	*
	* @return the double value of onPallet.
	*/
	public double getOnPallet(){
		return onPallet;
	}

	/**
	*
	* @param aOnPallet - the new value for onPallet
	*/
	public void setOnPallet(double aOnPallet){
		onPallet = aOnPallet;
	}





	/**
	*
	* @return the String value of agentProduct.
	*/
	public String getAgentProduct(){
		return agentProduct;
	}

	/**
	*
	* @param aAgentProduct - the new value for agentProduct
	*/
	public void setAgentProduct(String aAgentProduct){
		agentProduct = aAgentProduct;
	}


	/**
	 * @return Returns the alegrupp.
	 */
	public String getAlegrupp() {
		return alegrupp;
	}
	/**
	 * @param alegrupp The alegrupp to set.
	 */
	public void setAlegrupp(String alegrupp) {
		this.alegrupp = alegrupp;
	}
	
	public void setDiscgrupp(String alegrupp) {
		this.alegrupp = alegrupp;
	}
	/**
	 * @return Returns the onlyDefectiveList.
	 */
	
	public String getMainWarehouseStock() {
		return mainWarehouseStock;
	}
	/**
	 * @param mainWarehouseStock The mainWarehouseStock to set.
	 */
	public void setMainWarehouseStock(String mainWarehouseStock) {
		this.mainWarehouseStock = mainWarehouseStock;
	}
	/**
	 * @return Returns the pandPakendiKood.
	 */
	public String getPandPakendiKood() {
		return pandPakendiKood;
	}
	/**
	 * @param pandPakendiKood The pandPakendiKood to set.
	 */
	public void setPandPakendiKood(String pandPakendiKood) {
		this.pandPakendiKood = pandPakendiKood;
	}
	/**
	 * @return Returns the barcodes.
	 */
	public Vector getBarcodes() {
		return barcodes;
	}
	/**
	 * @param barcodes The barcodes to set.
	 */
	public void addBarcode(String barcodes) {
		this.barcodes.addElement(barcodes);
		
	}
	/**
	 * @return Returns the defectiveList.
	 */
	public String getDefectiveList() {
		return defectiveList;
	}
	/**
	 * @param defectiveList The defectiveList to set.
	 */
	public void setDefectiveList(String defectiveList) {
		this.defectiveList = defectiveList;
	}

	/**
	 * @return the date1
	 */
	public Date getDate1() {
		return date1;
	}

	/**
	 * @param date1 the date1 to set
	 */
	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	/**
	 * @return the eanCase
	 */
	public String getEanCase() {
		return eanCase;
	}

	/**
	 * @param eanCase the eanCase to set
	 */
	public void setEanCase(String eanCase) {
		this.eanCase = eanCase;
	}

	/**
	 * @return the showOnSeparateOrderList
	 */
	public boolean isShowOnSeparateOrderList() {
		return showOnSeparateOrderList;
	}

	/**
	 * @param showOnSeparateOrderList the showOnSeparateOrderList to set
	 */
	public void setShowOnSeparateOrderList(boolean showOnSeparateOrderList) {
		this.showOnSeparateOrderList = showOnSeparateOrderList;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	public void setEdiname(String ediname) {
		this.ediname = ediname;
	}

	public String getEdiname() {
		return ediname;
	}

	public void setAgentGroup(String agentGroup) {
		this.agentGroup = agentGroup;
	}

	public String getAgentGroup() {
		return agentGroup;
	}

	public double getBoxbrutto() {
		return boxbrutto;
	}

	public void setBoxbrutto(double boxbrutto) {
		this.boxbrutto = boxbrutto;
	}

	public double getBoxnetto() {
		return boxnetto;
	}

	public void setBoxnetto(double boxnetto) {
		this.boxnetto = boxnetto;
	}

	public String getPhotoid() {
		return photoid;
	}

	public void setPhotoid(String photoid) {
		this.photoid = photoid;
	}

	public String getTypeSimi() {
		return typeSimi;
	}

	public void setTypeSimi(String typeSimi) {
		this.typeSimi = typeSimi;
	}

	public boolean isAllowGift() {
		return allowGift;
	}

	public void setAllowGift(boolean allowGift) {
		this.allowGift = allowGift;
	}

	public boolean isLabel() {
		return label;
	}

	public void setLabel(boolean label) {
		this.label = label;
	}

	public boolean isNoCommas() {
		return noCommas;
	}

	public void setNoCommas(boolean noCommas) {
		this.noCommas = noCommas;
	}

	public long getTimeToSell() {
		return timeToSell;
	}

	public void setTimeToSell(long timeToSell) {
		this.timeToSell = timeToSell;
	}

	public String getStatusGiftPack() {
		return statusGiftPack;
	}

	public void setStatusGiftPack(String statusGiftPack) {
		this.statusGiftPack = statusGiftPack;
	}

	public String getGroupname() {
		if(groupname != null){
			return groupname;
		}else{
			return group;
		}
		
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getGroupSecname() {
		if(groupSecname != null){
			return groupSecname;
		}else{
			return groupSecond;
		}
	}

	public void setGroupSecname(String groupSecname) {
		this.groupSecname = groupSecname;
	}
}



/*--- formatting done in "Sun Java Convention" style on 10-08-2002 ---*/

