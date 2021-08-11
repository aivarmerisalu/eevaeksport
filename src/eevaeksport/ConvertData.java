package eevaeksport;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ConvertData {

  public ConvertData() {
    
  }
  private String custid = null;
  private String prodOrder = null;
  private String prodOwn = null;
  private double convert = 0;
  private boolean roundup = false;
/**
 * @return Returns the custid.
 */
public String getCustid() {
	return custid;
}
/**
 * @param custid The custid to set.
 */
public void setCustid(String custid) {
	this.custid = custid;
}
/**
 * @return Returns the prodGroup.
 */

public String getProdOrder() {
	return prodOrder;
}
/**
 * @param prodOrder The prodOrder to set.
 */
public void setProdOrder(String prodOrder) {
	this.prodOrder = prodOrder;
}
/**
 * @return Returns the prodOwn.
 */
public String getProdOwn() {
	return prodOwn;
}
/**
 * @param prodOwn The prodOwn to set.
 */
public void setProdOwn(String prodOwn) {
	this.prodOwn = prodOwn;
}
/**
 * @return Returns the roundup.
 */
public boolean isRoundup() {
	return roundup;
}
/**
 * @param roundup The roundup to set.
 */
public void setRoundup(boolean roundup) {
	this.roundup = roundup;
}
/**
 * @return Returns the convert.
 */
public double getConvert() {
	return convert;
}
/**
 * @param convert The convert to set.
 */
public void setConvert(double convert) {
	this.convert = convert;
}
}


