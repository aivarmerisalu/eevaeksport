package ee.mobilejava.wrapper;

import ee.mobitrek.concept.DistEvalLine;

public class DistEvalLineWrapper{
  private DistEvalLine line = null;

  public DistEvalLineWrapper(Object o){
    this.line  = (DistEvalLine)o;
  }

  public DistEvalLine getLine(){
    return line;
  }

}