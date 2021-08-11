package ee.mobilejava.wrapper;

import ee.merisoft.sm.Defective;

public class DefectiveWrapper{
  private Defective defective = null;

  public DefectiveWrapper(Object o){
    this.defective  = (Defective)o;
  }

  public Defective getDefective(){
    return defective;
  }

}