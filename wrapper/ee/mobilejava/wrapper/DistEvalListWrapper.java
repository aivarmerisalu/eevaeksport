package ee.mobilejava.wrapper;

import ee.mobitrek.concept.DistEvalList;

public class DistEvalListWrapper{
  private DistEvalList distEval = null;

  public DistEvalListWrapper(Object o){
    this.distEval  = (DistEvalList)o;
  }

  public DistEvalList getDistEval(){
    return distEval;
  }

}