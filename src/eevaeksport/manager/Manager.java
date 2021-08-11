package eevaeksport.manager;

import eevaeksport.EksportFrame;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Manager {

  public Manager() {
  }

  public static void main(String[] args) {
    EksportFrame eks = new EksportFrame(args);
    eks.getEksportPanel().doManagerTask();
    System.exit(0);
  }

}