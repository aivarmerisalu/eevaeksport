package eevaeksport.report;

import java.util.Date;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ReportLine {

  private String agentCode = null;
  private Date timeSent = null;
  private String clientCode = null;
  private String clientName = null;
  private Date endTime = null;
  private Date startTime = null;
  private double lineSum = 0;
  private int lineCount = 0;
  private String text = null;
  private String custGroup = null;
  private String dealerCode = null;

  private String contactType = null;
  private String agentName = " ";
  private boolean plannedVisit = false;
  private int newProdSold = 0;
  private long daystart = 0;
  private long dayend = 0;
  private int spidostart = 0;
  private int spidoend = 0;
  private int redlines = 0;
  private double longitude = 0;
  public double getLongitude() {
	return longitude;
}


public void setLongitude(double longitude) {
	this.longitude = longitude;
}
private double latitude = 0;

  //siin on uued asjad 
  // 1. mitu uut toodet on kliendile myydud
  // 2. mis kell algas tööpäev
  // 3. mis kell lõppes
  // 4. spidoalgseis
  // 5. spidolõppseis
  // 6. punaste toodete arv

  public double getLatitude() {
	return latitude;
}


public void setLatitude(double latitude) {
	this.latitude = latitude;
}


public String toString(){
    StringBuffer sb = new StringBuffer();
    sb.append("agentCode=" + getAgentCode() + ";timeSent=" + getTimeSent() + ";clientCode=" + getClientCode() +
              ";clientName=" + getClientName() + ";endTime=" + getEndTime() + ";startTime=" + getStartTime() +
              ";lineSum=" + getLineSum() + ";lineCount=" + getLineCount() + ";text=" + getText());
    sb.append(";custGroup=" + getCustGroup());
    sb.append(";dealerCode=" + getDealerCode());
    sb.append(";agentName=" + getAgentName());
    sb.append(";plannedVisit=" + isPlannedVisit());
    return sb.toString();
  }


	/**
	*
	* @return true if plannedVisit is set to true.
	*/
	public boolean isPlannedVisit(){
		return plannedVisit;
	}

	/**
	*
	* @param aPlannedVisit - the new value for plannedVisit
	*/
	public void setPlannedVisit(boolean aPlannedVisit){
		plannedVisit = aPlannedVisit;
	}



	/**
	*
	* @return the String value of AgentName.
	*/
	public String getAgentName(){
		return agentName;
	}

	/**
	*
	* @param aAgentName - the new value for AgentName
	*/
	public void setAgentName(String aAgentName){
		agentName = aAgentName;
	}





	/**
	*
	* @return the String value of dealerCode.
	*/
	public String getDealerCode(){
		return dealerCode;
	}

	/**
	*
	* @param aDealerCode - the new value for dealerCode
	*/
	public void setDealerCode(String aDealerCode){
		dealerCode = aDealerCode;
	}



      /**
	*
	* @return the String value of custGroup.
	*/
	public String getCustGroup(){
		return custGroup;
	}

	/**
	*
	* @param aCustGroup - the new value for custGroup
	*/
	public void setCustGroup(String aCustGroup){
		custGroup = aCustGroup;
	}



	/**
	*
	* @return the String value of text.
	*/
	public String getText(){
		return text;
	}

	/**
	*
	* @param aText - the new value for text
	*/
	public void setText(String aText){
		text = aText;
	}




	/**
	*
	* @return the int value of lineCount.
	*/
	public int getLineCount(){
		return lineCount;
	}

	/**
	*
	* @param aLineCount - the new value for lineCount
	*/
	public void setLineCount(int aLineCount){
		lineCount = aLineCount;
	}



	/**
	*
	* @return the double value of lineSum.
	*/
	public double getLineSum(){
		return lineSum;
	}

	/**
	*
	* @param aLineSum - the new value for lineSum
	*/
	public void setLineSum(double aLineSum){
		lineSum = aLineSum;
	}




	/**
	*
	* @return the Date value of startTime.
	*/
	public Date getStartTime(){
		return startTime;
	}

	/**
	*
	* @param aStartTime - the new value for startTime
	*/
	public void setStartTime(Date aStartTime){
		startTime = aStartTime;
	}




	/**
	*
	* @return the Date value of endTime.
	*/
	public Date getEndTime(){
		return endTime;
	}

	/**
	*
	* @param aEndTime - the new value for endTime
	*/
	public void setEndTime(Date aEndTime){
		endTime = aEndTime;
	}



	/**
	*
	* @return the String value of clientName.
	*/
	public String getClientName(){
		return clientName;
	}

	/**
	*
	* @param aClientName - the new value for clientName
	*/
	public void setClientName(String aClientName){
		clientName = aClientName;
	}




	/**
	*
	* @return the String value of clientCode.
	*/
	public String getClientCode(){
		return clientCode;
	}

	/**
	*
	* @param aClientCode - the new value for clientCode
	*/
	public void setClientCode(String aClientCode){
		clientCode = aClientCode;
	}




	/**
	*
	* @return the Date value of timeSent.
	*/
	public Date getTimeSent(){
		return timeSent;
	}

	/**
	*
	* @param aTimeSent - the new value for timeSent
	*/
	public void setTimeSent(Date aTimeSent){
		timeSent = aTimeSent;
	}



	/**
	*
	* @return the String value of agentCode.
	*/
	public String getAgentCode(){
		return agentCode;
	}

	/**
	*
	* @param aAgentCode - the new value for agentCode
	*/
	public void setAgentCode(String aAgentCode){
		agentCode = aAgentCode;
	}


  public ReportLine() {
  }

/**
 * @return Returns the dayend.
 */
public long getDayend() {
	return dayend;
}
/**
 * @param dayend The dayend to set.
 */
public void setDayend(long dayend) {
	this.dayend = dayend;
}
/**
 * @return Returns the daystart.
 */
public long getDaystart() {
	return daystart;
}
/**
 * @param daystart The daystart to set.
 */
public void setDaystart(long daystart) {
	this.daystart = daystart;
}
/**
 * @return Returns the newProdSold.
 */
public int getNewProdSold() {
	return newProdSold;
}
/**
 * @param newProdSold The newProdSold to set.
 */
public void setNewProdSold(int newProdSold) {
	this.newProdSold = newProdSold;
}
/**
 * @return Returns the redlines.
 */
public int getRedlines() {
	return redlines;
}
/**
 * @param redlines The redlines to set.
 */
public void setRedlines(int redlines) {
	this.redlines = redlines;
}
/**
 * @return Returns the spidoend.
 */
public int getSpidoend() {
	return spidoend;
}
/**
 * @param spidoend The spidoend to set.
 */
public void setSpidoend(int spidoend) {
	this.spidoend = spidoend;
}
/**
 * @return Returns the spidostart.
 */
public int getSpidostart() {
	return spidostart;
}
/**
 * @param spidostart The spidostart to set.
 */
public void setSpidostart(int spidostart) {
	this.spidostart = spidostart;
}


public String getContactType() {
	return contactType;
}


public void setContactType(String contactType) {
	this.contactType = contactType;
}
}