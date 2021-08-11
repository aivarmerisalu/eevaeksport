/*
 * Created on 17.02.2007
 */
package eevaeksport.driver;

import java.sql.SQLException;

import eevaeksport.report.ReportLine;
import eevaeksport.report.ReportVisitLine;

/**
 * @author Aivar  Merisalu
 * MobileJava Junior Programmer +37256248034 
 */
public interface AlternativeReport {
	  void setAlternativeReport(ReportVisitLine rr,ReportLine rl) throws SQLException;
	}