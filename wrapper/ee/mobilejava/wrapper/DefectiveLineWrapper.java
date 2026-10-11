package ee.mobilejava.wrapper;


import ee.merisoft.sm.DefectiveLine;

public class DefectiveLineWrapper{
  private DefectiveLine line = null;

  public DefectiveLineWrapper(Object o){
    this.line  = (DefectiveLine)o;
  }

  public DefectiveLine getLine(){
    return line;
  }

}