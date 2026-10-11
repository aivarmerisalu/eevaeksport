package eevaeksport;


import java.util.Vector;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Contract {

  public Contract(String code) {
    this.code = code;
  }
  public Vector<ContractLine> lines = null;
  public Vector customers = null;
  public String type = null;
  public String description = null;
  public String name = null;
  public String code = null;
  public long start = 0;
  public long end = 0;
  

}
