package eevaeksport;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ContractLine {

  public ContractLine(String prodid) {
    this.prodid = prodid;
  }
 
  public double amount = 0;
  public double discount = 0;
  public boolean pos = false;
  public boolean display = false;
  public boolean optional = false;
  public String extrainfo = null;
  public String title = null;
  public String prodid = null;
  

}
