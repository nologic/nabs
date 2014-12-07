/*
 * DatabaseReportListener.java
 *
 * Created on December 3, 2006, 5:28 PM
 *
 */

package eunomia.core.data.staticData;

import eunomia.messages.receptor.ncm.AnalysisSummaryMessage;
import com.vivic.eunomia.module.frontend.GUIStaticAnalysisModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DatabaseReportListener {
    public void setAnalysisSummaryReport(AnalysisSummaryMessage sum);
}