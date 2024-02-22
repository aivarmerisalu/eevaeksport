package eevaeksport;

import java.util.ArrayList;

public class DashboardData {
	
	 public DashboardData(String id) {
		    this.id = id;
		  }
  public String sorting = null;
  public String id = null;
  public String dataLabel = null;
  public String dataType = null;
  public Double dataValue = null;
  public ArrayList<DashboardData> dataArr = null;

  public void addArrElement(DashboardData dd) {
	  if(dataArr == null) {
		  dataArr = new ArrayList<DashboardData>();
	  }
	  dataArr.add(dd);
  }
  
}
