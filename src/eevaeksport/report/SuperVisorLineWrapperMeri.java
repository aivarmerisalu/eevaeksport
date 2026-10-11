package eevaeksport.report;


import ee.merisoft.sm.SuperVisorEvalLine;

public class SuperVisorLineWrapperMeri{
  private SuperVisorEvalLine line;

  public SuperVisorLineWrapperMeri(Object paramObject)
  {
    this.line = ((SuperVisorEvalLine)paramObject);
  }

  public SuperVisorEvalLine getLine() {
    return this.line;
  }
}