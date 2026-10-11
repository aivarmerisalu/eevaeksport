package eevaeksport;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Dealer {


          /**
          *
          * @return the String value of lastStockDate.
          */
          public String getLastStockDate(){
                  return lastStockDate;
          }

          /**
          *
          * @param aLastStockDate - the new value for lastStockDate
          */
          public void setLastStockDate(String aLastStockDate){
                  lastStockDate = aLastStockDate;
          }





          /**
          *
          * @return the String value of lastBlockDate.
          */
          public String getLastBlockDate(){
                  return lastBlockDate;
          }

          /**
          *
          * @param aLastBlockDate - the new value for lastBlockDate
          */
          public void setLastBlockDate(String aLastBlockDate){
                  lastBlockDate = aLastBlockDate;
          }


  public Dealer(String id) {
    this.id = id;
  }

  /**
   *
   * @return the String value of name.
   */
  public String getName(){
    return name;
  }

  /**
   *
   * @param aName - the new value for name
   */
  public void setName(String aName){
    name = aName;
  }

  public String getID(){
    return this.id;
  }

  private String id;
  private String name;
  private String lastStockDate;
  private String lastBlockDate;


}