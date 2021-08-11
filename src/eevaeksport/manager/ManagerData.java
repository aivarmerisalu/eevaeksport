package eevaeksport.manager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ManagerData {
  private String clientVisitInfo = null;
  private String clientAgentInfo = null;


	/**
	*
	* @return the String value of clientAgentInfo.
	*/
	public String getClientAgentInfo(){
		return clientAgentInfo;
	}

	/**
	*
	* @param aClientAgentInfo - the new value for clientAgentInfo
	*/
	public void setClientAgentInfo(String aClientAgentInfo){
		clientAgentInfo = aClientAgentInfo;
	}



	/**
	*
	* @return the String value of clientVisitInfo.
	*/
	public String getClientVisitInfo(){
		return clientVisitInfo;
	}

	/**
	*
	* @param aClientVisitInfo - the new value for clientVisitInfo
	*/
	public void setClientVisitInfo(String aClientVisitInfo){
		clientVisitInfo = aClientVisitInfo;
	}


}