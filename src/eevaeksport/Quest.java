package eevaeksport;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Quest implements Serializable {
	 static final long serialVersionUID = 2L; 
	 public Long id = null;
  public String custID = null;
  public Long qStart = null;
  public Long qEnd = null;
  public Boolean forced = null;
  public String frequency = "DAILY";
  public String title = null;
  public String manager = null;
  public String appearance = "ON_CUSTOMER_SELECTION";
  public ArrayList<QuestLine> questLines = null;
 
   @Override   
  public String toString() {
	  return custID + " " + qStart + " " + qEnd + " " + forced + " " + questLines.toString();
  }

}
