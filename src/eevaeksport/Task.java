package eevaeksport;

import java.util.GregorianCalendar;

/**
 * <p>Title: EevaEksport</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Tarmo Tubro
 * @version 1.0
 */

public class Task extends GregorianCalendar {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public Task() {
  }

  public void setType(String type){
    this.type = type;
  }

  public String getType(){
    return this.type;
  }

  private String type = null;
}