package eevaeksport.report;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ReportVisitLine {

	/*"xxxx" + sep);
    reportData.append(conc.getAgentName()+ sep +conc.agent + sep + cust.getID() + sep 
    		+ cust.getName()+ sep + kaidud + sep + lastCalen.getTime().getTime() + sep 
			+visitplanned.size() + sep+ visitedCustomers.size()+ sep + todaysExtra.size() + lineSep*/
	
  private String agentCode = null;
  private String agentName = " ";
  private String clientCode = null;
  private String clientName = null;
  private boolean todayOK = false; 
  private String status = null; 
  private int plannedVisits = 0;
  private int doneVisits = 0;
  private int extraVisits = 0;
  private long daylong = 0;



    public ReportVisitLine(){
    	
    }
	/**
	 * @return Returns the agentCode.
	 */
	public String getAgentCode() {
		return agentCode;
	}
	/**
	 * @param agentCode The agentCode to set.
	 */
	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}
/**
 * @return Returns the agentName.
 */
public String getAgentName() {
	return agentName;
}
/**
 * @param agentName The agentName to set.
 */
public void setAgentName(String agentName) {
	this.agentName = agentName;
}
/**
 * @return Returns the clientCode.
 */
public String getClientCode() {
	return clientCode;
}
/**
 * @param clientCode The clientCode to set.
 */
public void setClientCode(String clientCode) {
	this.clientCode = clientCode;
}
/**
 * @return Returns the clientName.
 */
public String getClientName() {
	return clientName;
}
/**
 * @param clientName The clientName to set.
 */
public void setClientName(String clientName) {
	this.clientName = clientName;
}
/**
 * @return Returns the daylong.
 */
public long getDaylong() {
	return daylong;
}
/**
 * @param daylong The daylong to set.
 */
public void setDaylong(long daylong) {
	this.daylong = daylong;
}
/**
 * @return Returns the doneVisits.
 */
public int getDoneVisits() {
	return doneVisits;
}
/**
 * @param doneVisits The doneVisits to set.
 */
public void setDoneVisits(int doneVisits) {
	this.doneVisits = doneVisits;
}
/**
 * @return Returns the extraVisits.
 */
public int getExtraVisits() {
	return extraVisits;
}
/**
 * @param extraVisits The extraVisits to set.
 */
public void setExtraVisits(int extraVisits) {
	this.extraVisits = extraVisits;
}
/**
 * @return Returns the plannedVisits.
 */
public int getPlannedVisits() {
	return plannedVisits;
}
/**
 * @param plannedVisits The plannedVisits to set.
 */
public void setPlannedVisits(int plannedVisits) {
	this.plannedVisits = plannedVisits;
}
/**
 * @return Returns the todayOK.
 */
public boolean isTodayOK() {
	return todayOK;
}
/**
 * @param todayOK The todayOK to set.
 */
public void setTodayOK(boolean todayOK) {
	this.todayOK = todayOK;
}
/**
 * @return Returns the status.
 */
public String getStatus() {
	return status;
}
/**
 * @param status The status to set.
 */
public void setStatus(String status) {
	this.status = status;
}
}