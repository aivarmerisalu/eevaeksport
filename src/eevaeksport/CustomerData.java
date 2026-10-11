/*
 * Created on 19.02.2006
 */
package eevaeksport;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public class CustomerData {

	 public CustomerData(String infotype,String content,String presentIn) {
	    this.infotype = infotype;
	    this.content = content;
	    this.presentIn = presentIn;
	  }

	private String infotype = null;
	private String  content = " ";
	private String  presentIn = " ";
	public String getInfotype() {
		return infotype;
	}
	public void setInfotype(String infotype) {
		this.infotype = infotype;
	}
	
	}
