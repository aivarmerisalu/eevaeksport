/*
 * Created on 14.10.2008
 */
package eevaeksport.report;

public class ReportVisitDurationLine {
	  private String agentCode = null;
	  private String agentName = " ";
	  private String custid = null;
	  private String custname = null;
	  private String  visitType = null;
	  private long duration = 0;
	  private long madeatday = 0;
	  private double longitude = 0;
	  public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLattitude() {
		return lattitude;
	}
	public void setLattitude(double lattitude) {
		this.lattitude = lattitude;
	}
	private double lattitude = 0;
	/**
	 * @return the agentCode
	 */
	public String getAgentCode() {
		return agentCode;
	}
	/**
	 * @param agentCode the agentCode to set
	 */
	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}
	/**
	 * @return the agentName
	 */
	public String getAgentName() {
		return agentName;
	}
	/**
	 * @param agentName the agentName to set
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	/**
	 * @return the custid
	 */
	public String getCustid() {
		return custid;
	}
	/**
	 * @param custid the custid to set
	 */
	public void setCustid(String custid) {
		this.custid = custid;
	}
	/**
	 * @return the custname
	 */
	public String getCustname() {
		return custname;
	}
	/**
	 * @param custname the custname to set
	 */
	public void setCustname(String custname) {
		this.custname = custname;
	}
	/**
	 * @return the visitType
	 */
	public String getVisitType() {
		return visitType;
	}
	/**
	 * @param visitType the visitType to set
	 */
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}
	/**
	 * @return the madeatday
	 */
	public long getMadeatday() {
		return madeatday;
	}
	/**
	 * @param madeatday the madeatday to set
	 */
	public void setMadeatday(long madeatday) {
		this.madeatday = madeatday;
	}
	  
	  
	  
}
