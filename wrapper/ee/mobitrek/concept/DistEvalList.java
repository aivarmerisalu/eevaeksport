package ee.mobitrek.concept;

import java.io.Serializable;
import java.util.Vector;

public class DistEvalList implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getAgentID() {
		return agentID;
	}

	public String getAgentName() {
		return agentName;
	}

	public DistEvalList(String agentID, String agentName) {
		this.agentID = agentID;
		this.agentName = agentName;
	}

	public void addDistEvalLine(DistEvalLine dl){
		lines.addElement(dl);
	}

	public Vector getLines(){
		return lines;
	}

	private String agentID = null;
	private String agentName = null;
	private Vector lines = new Vector();
}
