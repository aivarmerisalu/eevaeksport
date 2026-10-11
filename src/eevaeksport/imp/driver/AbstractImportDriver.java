package eevaeksport.imp.driver;

import java.sql.SQLException;

import eevaeksport.imp.Order;

/**
 * Title:        Eeva eksport
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Tarmo Tubro
 * @version 1.0
 */

public abstract class AbstractImportDriver {

  private int numAddedLines = 0;
  public abstract String getTrouble() ;
  /**
   *
   * @return the int value of numAddedLines.
   */
  public int getNumAddedLines(){
    return numAddedLines;
  }

  /**
   *
   * @param aNumAddedLines - the new value for numAddedLines
   */
  public void setNumAddedLines(int aNumAddedLines){
    numAddedLines = aNumAddedLines;
  }

  public void addNumLine(){
    numAddedLines++;
  }

  public AbstractImportDriver() {
  }

  public abstract void makeOrder(Order order) throws SQLException;
  public abstract String identifyCustomer(String id,String regnr) throws SQLException;
  public abstract String identifyProduct(String ean, String prodID) throws SQLException;

  public String getImportFile(){
    return "C:/Mobile Tellimused/";
  }

  
  public String  getKodirovka(String ean,float status){
	  return null;
  }

  public String  getDriverVersion(){
	  return null;
  }
  
}