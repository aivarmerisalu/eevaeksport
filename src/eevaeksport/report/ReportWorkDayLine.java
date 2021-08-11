package eevaeksport.report;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ReportWorkDayLine {
	//conc.getAgentName()+ sep +conc.agent + sep + dayStart.getTime()+
		// sep + dayEnd.getTime() + lineSep);
	
  private String agentCode = null;
  private String agentName = " ";
  private String daystarttime = null;
  private String dayendtime = null;
  private int milage = 0;
  private long daylong = 0;



    public ReportWorkDayLine(){
    	
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
 * @return Returns the dayendtime.
 */
public String getDayendtime() {
	return dayendtime;
}
/**
 * @param dayendtime The dayendtime to set.
 */
public void setDayendtime(String dayendtime) {
	this.dayendtime = dayendtime;
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
 * @return Returns the daystarttime.
 */
public String getDaystarttime() {
	return daystarttime;
}
/**
 * @param daystarttime The daystarttime to set.
 */
public void setDaystarttime(String daystarttime) {
	this.daystarttime = daystarttime;
}
/**
 * @return Returns the milage.
 */
public int getMilage() {
	return milage;
}
/**
 * @param milage The milage to set.
 */
public void setMilage(int milage) {
	this.milage = milage;
}
}