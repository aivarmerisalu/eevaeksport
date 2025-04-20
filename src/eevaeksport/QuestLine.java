package eevaeksport;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class QuestLine implements Serializable {
	 static final long serialVersionUID = 2L; 
	 public Long id = null;
  public String qname = null;
  public Long points = null;
  public String qtype = null;
  public ArrayList<String> choices = null;
  
  @Override   
  public String toString() {
	  return qname + " " + qtype + " " + points + " " + choices;
  }

}
