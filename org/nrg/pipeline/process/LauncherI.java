/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LauncherI.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public interface LauncherI {
    
    public int launchProcess(ParameterData[] parameters, ResolvedStep rStep, CommandStatementPresenter  command, ResolvedResource rsc) throws PipelineEngineException;
    
    public void setErrorFileName(String errorFileName);
    
    public void setOutputFileName(String outputFileName);
    
    public Notification getNotification();
    
    public void setDebug(boolean debugMode);

}
