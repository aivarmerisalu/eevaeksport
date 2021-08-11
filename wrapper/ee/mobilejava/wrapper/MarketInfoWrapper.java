package ee.mobilejava.wrapper;

import ee.mobitrek.concept.MarketInfo;

public class MarketInfoWrapper{
  private MarketInfo marketInfo = null;

  public MarketInfoWrapper(Object o){
    this.marketInfo = (MarketInfo)o;
  }

  /**
   *
   * @return the MarketInfo value of marketInfo.
   */
  public MarketInfo getMarketInfo(){
    return marketInfo;
  }

}