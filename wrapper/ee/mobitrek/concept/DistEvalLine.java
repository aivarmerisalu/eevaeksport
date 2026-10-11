package ee.mobitrek.concept;

import java.io.Serializable;
import java.util.Date;

public class DistEvalLine implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public String getProdID() {
		return prodID;
	}

	public String getProdName() {
		return prodName;
	}

	public int getPalAmount() {
		return palAmount;
	}

	public String getEvalCode() {
		return evalCode;
	}

	public Date getDate() {
		return date;
	}

	public String getBestBefore() {
		return bestBefore;
	}

	public String getEvalName() {
		return evalName;
	}

	public String getCustID() {
		return custID;
	}

	public String getCustName() {
		return custName;
	}

	public boolean isOwnProduct() {
		return ownProduct;
	}

	public double getNormalPrice() {
		return normalPrice;
	}

	public int getFaceAmountBefore() {
		return faceAmountBefore;
	}

	public boolean isAction() {
		return action;
	}

	public DistEvalLine(String custID, String custName, String prodID, String prodName, boolean ownProduct,boolean isBrand,
							  String bestBefore, String evalCode, String evalName, double normalPrice,double promoPrice,
							  int spaceProsentage, int faceAmountBefore, int faceAmountAfter, int palnoeval,String planoComment, int palAmount, boolean action,
							  int dominance, int firstinflow, int placespresent, boolean presence , double stockAtDisplay, double stockAtWarehouse , String comment,
							  int numberOF11,int numberOF12,int numberOF21,int numberOF22,int vobbler,int pricetag,int a2,int a3,int a4,int package1,int variation,
							  String res1,String res2,int resint1,int resint2,double resdoub1,double resdoub2 ) {
		this.custID = custID;
		this.custName = custName;
		this.prodID = prodID;
		this.prodName = prodName;
		this.ownProduct = ownProduct;
		this.isBrand = isBrand;
		this.bestBefore = bestBefore;
		this.evalCode = evalCode;
		this.evalName = evalName;
		this.normalPrice = normalPrice;
		this.promoPrice = promoPrice;
		//kauba v�ljapank DISPLAY OSA
		this.spaceProsentage = 0;
		this.faceAmountBefore = faceAmountBefore;
		this.faceAmountAfter = faceAmountAfter;
		this.palnoeval = palnoeval;
		this.planoComment = planoComment;
		this.palAmount = palAmount;
		this.action = action;
		this.dominance = dominance;
		this.firstinflow = firstinflow;
		this.placespresent = placespresent;
//  kauba olemasolu ja kogus
		this.presence = presence;
		this.stockAtDisplay = stockAtDisplay;
		this.stockAtWarehouse = stockAtWarehouse;
		this.comment = comment;
		this.vobbler = vobbler;
		this.pricetag = pricetag;
		this.a2 = a2;
		this.a3 = a3;
		this.a4 = a4;
		this.reserv1 = res1;
		this.reserv2 = res2;
		this.numberOF11 = numberOF11;

		this.numberOF12 = numberOF12;
		this.numberOF21 = numberOF21;
		this.numberOF22 = numberOF22;

		this.resint1 = resint1;
		this.resint2 = resint2;
		this.resdouble1 = resdoub1;
		this.resdouble2 = resdoub2;
		this.package1 = package1;
		this.variation = variation;

	}

	private String prodID = null;//
	private String prodName = null;//
	private String evalCode = null;
	private Date date = new Date();
	private String bestBefore = null;
	private String custID = null;//
	private String custName = null;//
	private boolean ownProduct = false;
	private boolean isBrand = false;
	private String evalName = null;
	private int variation = 0;
	private int package1 = 0;
	//kauba olemasolu ja kogus
	private boolean presence = false;
	private double stockAtDisplay = 0;
	private double stockAtWarehouse = 0;
	//kauba v�ljapank DISPLAY OSA
	private int spaceProsentage = 0;
	private int faceAmountBefore = 0;
	private int faceAmountAfter = 0;
	private int palnoeval = 0;
	private int palAmount = 0;
	private boolean action = false;
	private int dominance = 0;
	private int firstinflow = 0;
	private int placespresent = 1;
	//hinna�levaade
	private double normalPrice = -1;
	private double promoPrice = -1;
	private String comment = null;
	//reserv v�ljad teades et l�heb vaja juurde tulevikus
	private String reserv1 = null;
	private String reserv2 = null;
	private int resint1 = 0;
	private int resint2 = 0;
	private double resdouble1 = 0;
	private double resdouble2 = 0;
	private int a2 = 0;
	private int a3 = 0;
	private int a4 = 0;
	private int vobbler = 0;
	private int pricetag = 0;
	private int numberOF11 = 0;
	private int numberOF12 = 0;
	private int numberOF21 = 0;
	private int numberOF22 = 0;
	private String planoComment = null;
	/**
	 * @return Returns the dominance.
	 */
	public int getDominance() {
		return dominance;
	}
	/**
	 * @param dominance The dominance to set.
	 */
	public void setDominance(int dominance) {
		this.dominance = dominance;
	}
	/**
	 * @return Returns the faceAmountAfter.
	 */
	public int getFaceAmountAfter() {
		return faceAmountAfter;
	}
	/**
	 * @param faceAmountAfter The faceAmountAfter to set.
	 */
	public void setFaceAmountAfter(int faceAmountAfter) {
		this.faceAmountAfter = faceAmountAfter;
	}
	/**
	 * @return Returns the firstinflow.
	 */
	public int getFirstinflow() {
		return firstinflow;
	}
	/**
	 * @param firstinflow The firstinflow to set.
	 */
	public void setFirstinflow(int firstinflow) {
		this.firstinflow = firstinflow;
	}
	/**
	 * @return Returns the palnoeval.
	 */
	public int getPalnoeval() {
		return palnoeval;
	}
	/**
	 * @param palnoeval The palnoeval to set.
	 */
	public void setPalnoeval(int palnoeval) {
		this.palnoeval = palnoeval;
	}
	/**
	 * @return Returns the placespresent.
	 */
	public int getPlacespresent() {
		return placespresent;
	}
	/**
	 * @param placespresent The placespresent to set.
	 */
	public void setPlacespresent(int placespresent) {
		this.placespresent = placespresent;
	}
	/**
	 * @return Returns the presence.
	 */
	public boolean isPresence() {
		return presence;
	}
	/**
	 * @param presence The presence to set.
	 */
	public void setPresence(boolean presence) {
		this.presence = presence;
	}
	/**
	 * @return Returns the promoPrice.
	 */
	public double getPromoPrice() {
		return promoPrice;
	}
	/**
	 * @param promoPrice The promoPrice to set.
	 */
	public void setPromoPrice(double promoPrice) {
		this.promoPrice = promoPrice;
	}
	/**
	 * @return Returns the spaceProsentage.
	 */
	public int getSpaceProsentage() {
		return spaceProsentage;
	}
	/**
	 * @param spaceProsentage The spaceProsentage to set.
	 */
	public void setSpaceProsentage(int spaceProsentage) {
		this.spaceProsentage = spaceProsentage;
	}
	/**
	 * @return Returns the stockAtDisplay.
	 */
	public double getStockAtDisplay() {
		return stockAtDisplay;
	}
	/**
	 * @param stockAtDisplay The stockAtDisplay to set.
	 */
	public void setStockAtDisplay(double stockAtDisplay) {
		this.stockAtDisplay = stockAtDisplay;
	}
	/**
	 * @return Returns the stockAtWarehouse.
	 */
	public double getStockAtWarehouse() {
		return stockAtWarehouse;
	}
	/**
	 * @param stockAtWarehouse The stockAtWarehouse to set.
	 */
	public void setStockAtWarehouse(double stockAtWarehouse) {
		this.stockAtWarehouse = stockAtWarehouse;
	}
	/**
	 * @param action The action to set.
	 */
	public void setAction(boolean action) {
		this.action = action;
	}
	/**
	 * @param bestBefore The bestBefore to set.
	 */
	public void setBestBefore(String bestBefore) {
		this.bestBefore = bestBefore;
	}
	/**
	 * @param custID The custID to set.
	 */
	public void setCustID(String custID) {
		this.custID = custID;
	}
	/**
	 * @param custName The custName to set.
	 */
	public void setCustName(String custName) {
		this.custName = custName;
	}
	/**
	 * @param date The date to set.
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	/**
	 * @param evalCode The evalCode to set.
	 */
	public void setEvalCode(String evalCode) {
		this.evalCode = evalCode;
	}
	/**
	 * @param evalName The evalName to set.
	 */
	public void setEvalName(String evalName) {
		this.evalName = evalName;
	}
	/**
	 * @param faceAmountBefore The faceAmountBefore to set.
	 */
	public void setFaceAmountBefore(int faceAmountBefore) {
		this.faceAmountBefore = faceAmountBefore;
	}
	/**
	 * @param normalPrice The normalPrice to set.
	 */
	public void setNormalPrice(double normalPrice) {
		this.normalPrice = normalPrice;
	}
	/**
	 * @param ownProduct The ownProduct to set.
	 */
	public void setOwnProduct(boolean ownProduct) {
		this.ownProduct = ownProduct;
	}
	/**
	 * @param palAmount The palAmount to set.
	 */
	public void setPalAmount(int palAmount) {
		this.palAmount = palAmount;
	}
	/**
	 * @param prodID The prodID to set.
	 */
	public void setProdID(String prodID) {
		this.prodID = prodID;
	}
	/**
	 * @param prodName The prodName to set.
	 */
	public void setProdName(String prodName) {
		this.prodName = prodName;
	}
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	/**
	 * @return Returns the resdouble1.
	 */
	public double getResdouble1() {
		return resdouble1;
	}
	/**
	 * @param resdouble1 The resdouble1 to set.
	 */
	public void setResdouble1(double resdouble1) {
		this.resdouble1 = resdouble1;
	}
	/**
	 * @return Returns the resdouble2.
	 */
	public double getResdouble2() {
		return resdouble2;
	}
	/**
	 * @param resdouble2 The resdouble2 to set.
	 */
	public void setResdouble2(double resdouble2) {
		this.resdouble2 = resdouble2;
	}
	/**
	 * @return Returns the reserv1.
	 */
	public String getReserv1() {
		return reserv1;
	}
	/**
	 * @param reserv1 The reserv1 to set.
	 */
	public void setReserv1(String reserv1) {
		this.reserv1 = reserv1;
	}
	/**
	 * @return Returns the reserv2.
	 */
	public String getReserv2() {
		return reserv2;
	}
	/**
	 * @param reserv2 The reserv2 to set.
	 */
	public void setReserv2(String reserv2) {
		this.reserv2 = reserv2;
	}
	/**
	 * @return Returns the resint1.
	 */
	public int getResint1() {
		return resint1;
	}
	/**
	 * @param resint1 The resint1 to set.
	 */
	public void setResint1(int resint1) {
		this.resint1 = resint1;
	}
	/**
	 * @return Returns the resint2.
	 */
	public int getResint2() {
		return resint2;
	}
	/**
	 * @param resint2 The resint2 to set.
	 */
	public void setResint2(int resint2) {
		this.resint2 = resint2;
	}
	/**
	 * @return Returns the a2.
	 */
	public int getA2() {
		return a2;
	}
	/**
	 * @param a2 The a2 to set.
	 */
	public void setA2(int a2) {
		this.a2 = a2;
	}
	/**
	 * @return Returns the a3.
	 */
	public int getA3() {
		return a3;
	}
	/**
	 * @param a3 The a3 to set.
	 */
	public void setA3(int a3) {
		this.a3 = a3;
	}
	/**
	 * @return Returns the a4.
	 */
	public int getA4() {
		return a4;
	}
	/**
	 * @param a4 The a4 to set.
	 */
	public void setA4(int a4) {
		this.a4 = a4;
	}
	/**
	 * @return Returns the pricetag.
	 */
	public int getPricetag() {
		return pricetag;
	}
	/**
	 * @param pricetag The pricetag to set.
	 */
	public void setPricetag(int pricetag) {
		this.pricetag = pricetag;
	}
	/**
	 * @return Returns the vobbler.
	 */
	public int getVobbler() {
		return vobbler;
	}
	/**
	 * @param vobbler The vobbler to set.
	 */
	public void setVobbler(int vobbler) {
		this.vobbler = vobbler;
	}

	/**
	 * @return Returns the numberOF11.
	 */
	public int getNumberOF11() {
		return numberOF11;
	}
	/**
	 * @param numberOF11 The numberOF11 to set.
	 */
	public void setNumberOF11(int numberOF11) {
		this.numberOF11 = numberOF11;
	}
	/**
	 * @return Returns the numberOF12.
	 */
	public int getNumberOF12() {
		return numberOF12;
	}
	/**
	 * @param numberOF12 The numberOF12 to set.
	 */
	public void setNumberOF12(int numberOF12) {
		this.numberOF12 = numberOF12;
	}
	/**
	 * @return Returns the numberOF21.
	 */
	public int getNumberOF21() {
		return numberOF21;
	}
	/**
	 * @param numberOF21 The numberOF21 to set.
	 */
	public void setNumberOF21(int numberOF21) {
		this.numberOF21 = numberOF21;
	}
	/**
	 * @return Returns the numberOF22.
	 */
	public int getNumberOF22() {
		return numberOF22;
	}
	/**
	 * @param numberOF22 The numberOF22 to set.
	 */
	public void setNumberOF22(int numberOF22) {
		this.numberOF22 = numberOF22;
	}
	/**
	 * @return Returns the planoComment.
	 */
	public String getPlanoComment() {
		return planoComment;
	}
	/**
	 * @param planoComment The planoComment to set.
	 */
	public void setPlanoComment(String planoComment) {
		this.planoComment = planoComment;
	}
	/**
	 * @return Returns the variation.
	 */
	public int getVariation() {
		return variation;
	}
	/**
	 * @param variation The variation to set.
	 */
	public void setVariation(int variation) {
		this.variation = variation;
	}
	/**
	 * @return Returns the package1.
	 */
	public int getPackage1() {
		return package1;
	}
	/**
	 * @param package1 The package1 to set.
	 */
	public void setPackage1(int package1) {
		this.package1 = package1;
	}
	/**
	 * @return Returns the isBrand.
	 */
	public boolean isBrand() {
		return isBrand;
	}
	/**
	 * @param isBrand The isBrand to set.
	 */
	public void setBrand(boolean isBrand) {
		this.isBrand = isBrand;
	}
}
