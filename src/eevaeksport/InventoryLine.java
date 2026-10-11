/*
 * Created on 26.10.2007
 */
package eevaeksport;

import java.io.Serializable;
import java.util.Date;
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class InventoryLine implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String worker = null;
    private String workername = null;
    private String prodId = null;
    private String location = null;

    private String toLocation = null;
    private String prodName = null;
    private String ean = null;
    private String custId = null;
    private String machine = null;
    private String partii = null;
    private String warehouse = null;
    private String lineType = null;
    private String comment = null;
    private boolean trouble = false;

    private String toWarehouse = null;
    private double amount = -1;
    private double amountbybooks = -1;
    private Date  enterTime = null;

    /**
     * Constructor declaration
     *
     *
     * @param id
     * @param name
     *
     * @see
     */
    public InventoryLine(String worker, String workername,String prodId,double amount
    		,String location,String ean ,String custId , String prodName) {
   this.worker = worker;
   this.workername = workername;
   this.prodId = prodId;
   this.ean = ean;
   this.custId = custId;
   this.amount = amount;
   this.enterTime = new Date();
   this.location = location;
   this.prodName = prodName;
    }

	/**
	 * @return Returns the amount.
	 */
	public double getAmount() {
		return amount;
	}
	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}
	/**
	 * @return Returns the custId.
	 */
	public String getCustId() {
		return custId;
	}
	/**
	 * @param custId The custId to set.
	 */
	public void setCustId(String custId) {
		this.custId = custId;
	}
	/**
	 * @return Returns the ean.
	 */
	public String getEan() {
		return ean;
	}
	/**
	 * @param ean The ean to set.
	 */
	public void setEan(String ean) {
		this.ean = ean;
	}
	/**
	 * @return Returns the enterTime.
	 */
	public Date getEnterTime() {
		return enterTime;
	}
	/**
	 * @param enterTime The enterTime to set.
	 */
	public void setEnterTime(Date enterTime) {
		this.enterTime = enterTime;
	}
	/**
	 * @return Returns the location.
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location The location to set.
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return Returns the prodId.
	 */
	public String getProdId() {
		return prodId;
	}
	/**
	 * @param prodId The prodId to set.
	 */
	public void setProdId(String prodId) {
		this.prodId = prodId;
	}
	/**
	 * @return Returns the worker.
	 */
	public String getWorker() {
		return worker;
	}
	/**
	 * @param worker The worker to set.
	 */
	public void setWorker(String worker) {
		this.worker = worker;
	}
	/**
	 * @return Returns the workername.
	 */
	public String getWorkername() {
		return workername;
	}
	/**
	 * @param workername The workername to set.
	 */
	public void setWorkername(String workername) {
		this.workername = workername;
	}
	/**
	 * @return Returns the prodName.
	 */
	public String getProdName() {
		return prodName;
	}
	/**
	 * @param prodName The prodName to set.
	 */
	public void setProdName(String prodName) {
		this.prodName = prodName;
	}
	/**
	 * @return Returns the machine.
	 */
	public String getMachine() {
		return machine;
	}
	/**
	 * @param machine The machine to set.
	 */
	public void setMachine(String machine) {
		this.machine = machine;
	}
	/**
	 * @return Returns the amountbybooks.
	 */
	public double getAmountByBooks() {
		return amountbybooks;
	}
	/**
	 * @param amountbybooks The amountbybooks to set.
	 */
	public void setAmountByBooks(double amountbybooks) {
		this.amountbybooks = amountbybooks;
	}

	/**
	 * @return the partii
	 */
	public String getPartii() {
		return partii;
	}

	/**
	 * @param partii the partii to set
	 */
	public void setPartii(String partii) {
		this.partii = partii;
	}

	/**
	 * @return the warehouse
	 */
	public String getWarehouse() {
		return warehouse;
	}

	/**
	 * @param warehouse the warehouse to set
	 */
	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}

	/**
	 * @return the toLocation
	 */
	public String getToLocation() {
		return toLocation;
	}

	/**
	 * @param toLocation the toLocation to set
	 */
	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
	}

	/**
	 * @return the toWarehouse
	 */
	public String getToWarehouse() {
		return toWarehouse;
	}

	/**
	 * @param toWarehouse the toWarehouse to set
	 */
	public void setToWarehouse(String toWarehouse) {
		this.toWarehouse = toWarehouse;
	}

	/**
	 * @return the amountbybooks
	 */
	public double getAmountbybooks() {
		return amountbybooks;
	}

	/**
	 * @param amountbybooks the amountbybooks to set
	 */
	public void setAmountbybooks(double amountbybooks) {
		this.amountbybooks = amountbybooks;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the lineType
	 */
	public String getLineType() {
		return lineType;
	}

	/**
	 * @param lineType the lineType to set
	 */
	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	/**
	 * @return the trouble
	 */
	public boolean isTrouble() {
		return trouble;
	}

	/**
	 * @param trouble the trouble to set
	 */
	public void setTrouble(boolean trouble) {
		this.trouble = trouble;
	}
}



/*--- formatting done in "Sun Java Convention" style on 01-28-2003 ---*/

