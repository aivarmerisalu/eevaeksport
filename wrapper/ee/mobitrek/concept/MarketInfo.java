package ee.mobitrek.concept;

import java.io.Serializable;
import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MarketInfo implements Serializable{

	static final long serialVersionUID = 867357236140203300L;

	public MarketInfo() {
	}

	private String agentCode = null;
	private String agentName = null;
	private String custCode = null;
	private String custName = null;
	private long time = 0;

	/**
	 *
	 * @return the long value of time.
	 */
	public long getTime(){
		return time;
	}

	/**
	 *
	 * @param aTime - the new value for time
	 */
	public void setTime(long aTime){
		time = aTime;
	}



	/**
	 *
	 * @return the String value of custName.
	 */
	public String getCustName(){
		return custName;
	}

	/**
	 *
	 * @param aCustName - the new value for custName
	 */
	public void setCustName(String aCustName){
		custName = aCustName;
	}



	/**
	 *
	 * @return the String value of custCode.
	 */
	public String getCustCode(){
		return custCode;
	}

	/**
	 *
	 * @param aCustCode - the new value for custCode
	 */
	public void setCustCode(String aCustCode){
		custCode = aCustCode;
	}



	/**
	 *
	 * @return the String value of agentName.
	 */
	public String getAgentName(){
		return agentName;
	}

	/**
	 *
	 * @param aAgentName - the new value for agentName
	 */
	public void setAgentName(String aAgentName){
		agentName = aAgentName;
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



	public void add(String question, String type){
		list.addElement(new String[]{question, type, null});
	}

	public Vector getList(){
		Vector ll = new Vector();
		int i = 0;
		int a = this.list.size()-1;
		for (i = a;i>-1;i--){
			ll.addElement(list.elementAt(i));
		}
		return ll;
	}

	public void setValue(int index, String value){
		((String[])list.elementAt(index))[2] = value;
	}

	private Vector list = new Vector();
}