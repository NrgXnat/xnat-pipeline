/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.manager;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.exception.PreConditionNotSatisfiedException;
import org.nrg.pipeline.process.LauncherI;
import org.nrg.pipeline.process.LocalExecutableLauncher;
import org.nrg.pipeline.process.OSInfo;
import org.nrg.pipeline.process.PersonnelNotificationLauncher;
import org.nrg.pipeline.process.RemoteExecutableLauncher;
import org.nrg.pipeline.process.TransformerLauncher;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.FileUtils;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.PipeUtils;
import org.nrg.pipeline.utils.PipelineUtils;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;


//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ExecutionManager.java,v 1.2 2009/11/11 21:03:41 mohanar Exp $
 @since Pipeline 1.0
 */

public class ExecutionManager {
    
    //////////////////////////////////////////////////////////////////////////
    ////                        public methods                            ///        
    
    public  ExecutionManager(String pathToOutputFile, String pathToErrorFile) {
        if (pathToOutputFile != null) {
            outputFileName = pathToOutputFile;
        }
        if (pathToOutputFile != null) {
            errorFileName = pathToErrorFile;
        }
    }
    
    public void execute(AllResolvedStepsDocument resolvedDoc, String nextStep, boolean debug) throws PipelineException, PreConditionNotSatisfiedException {
        FileUtils.touchDir(outputFileName);
        
        ResolvedStep[] resolvedSteps = resolvedDoc.getAllResolvedSteps().getResolvedStepArray();
        Notification notification =  LaunchManager.getNotification();
        String savedFile = PipelineUtils.getResolvedPipelineXmlName(resolvedDoc);
        //notification.setPipelineTimeLaunched(LaunchManager.getLaunchTime());
        //notification.setPathTopipelineDecsriptor(resolvedDoc.getAllResolvedSteps().getLocation() + File.separator + resolvedDoc.getAllResolvedSteps().getName());
        for (int j = 0; j < resolvedSteps.length;) {
            int nextIndex = j+1;
            logger.info("Launching step " + j); 
            notification.setCurrentStep(XMLBeansUtils.getStepId(resolvedSteps[j]));
            //notification.setCurrentStepIndex(currentStep);
            //notification.setTotalSteps(LaunchManager.getTotalSteps());
            notification.setMessage(resolvedSteps[j].getDescription());
            notification.setNextStep(nextStep);
            resolvedSteps[j].setStatus(ResolvedStep.Status.RUNNING);
            notification.setStatus("Running");
            notification.setStepTimeLaunched(AdminUtils.getTimeLaunched());
            AdminUtils.fireNotification(notification);
            
            LinkedHashMap commands = PipeUtils.getLinkedResources(resolvedSteps[j]);
            //ResourceData[] resolvedResourceArray = resolvedSteps[i].getResolvedResourceArray();
            Iterator iter = commands.keySet().iterator();
            while (iter.hasNext()) {
               CommandStatementPresenter stmtPresenter = (CommandStatementPresenter)iter.next();
               String logStmt = stmtPresenter.getCommand(CommandStatementPresenter.PUBLIC_VIEW);
                try { 
                    int exitVal = launch(resolvedDoc.getAllResolvedSteps().getParameters().getParameterArray(), resolvedSteps[j],stmtPresenter, (ResolvedResource)commands.get(stmtPresenter), notification, debug);
                    if (exitVal != 0 && resolvedSteps[j].isSetContinueOnFailure() && !resolvedSteps[j].getContinueOnFailure()) {
                            resolvedSteps[j].setStatus(ResolvedStep.Status.FAILED);
                            notification.setStatus("Failed");
                            if (errorFileName!= null)
                                notification.setMessage("See " + errorFileName + " for errors");
                            logger.fatal(" Execution of " + logStmt + "at Step[id= "+ resolvedSteps[j].getId()+  "] was unsuccesfull");
                            PipelineException pe = new PipelineException(" Execution of " + logStmt + "at Step[id= "+ resolvedSteps[j].getId()+  "] was unsuccesfull");
                            pe.setErrorFileName(errorFileName);
                            pe.setOutputFileName(outputFileName);
                            throw pe;
                        //break;
                    }else {
                        resolvedSteps[j].setStatus(ResolvedStep.Status.COMPLETE);
                        try {
                            File saveDoc = new File(savedFile);
                            FileUtils.saveFile(saveDoc,resolvedDoc);
                        }catch(Exception e){
                            notification.setStatus("Failed to save Pipeline Descriptor file");
                            PipelineException pe = new PipelineException("Unable to save pipeline document " + savedFile,e);
                            pe.setErrorFileName(errorFileName);
                            pe.setOutputFileName(outputFileName);
                            throw pe;
                        }
                        notification.setStatus("Running");
                        //AdminUtils.fireNotification(notification);
                    }
                }catch(PipelineException pe) {
                    resolvedSteps[j].setStatus(ResolvedStep.Status.FAILED);
                    notification.setStatus("Failed");
                    AdminUtils.fireNotification(notification);
                    logger.fatal(" Execution of " + logStmt + "at Step[id= "+ resolvedSteps[j].getId()+  "] was unsuccesfull");
                    PipelineException pe1 = new PipelineException(" Execution of " + logStmt + "at Step[id= "+ resolvedSteps[j].getId()+  "] was unsuccesfull",pe);
                    pe1.setErrorFileName(errorFileName);
                    pe1.setOutputFileName(outputFileName);
                    throw pe1;
                }
            }
/*            if (resolvedSteps[j].isSetAwaitApprovalToProceed()) {
                notification.setStatus("Awaiting Action");
                AdminUtils.fireNotification(notification);
                for (int k=nextIndex; k < resolvedSteps.length; k++) 
                    resolvedSteps[k].setStatus(ResolvedStep.Status.AWAITING_ACTION);
                break;
            }else  
*/                AdminUtils.fireNotification(notification);
            logger.info("Pipeline executed");
            j= nextIndex;
        }
        //if (isLast){
         //   notification.setStatus("Complete");
         //   AdminUtils.fireNotification(notification);
       // }
    }

   
    
    private int launch(ParameterData[] parameters, ResolvedStep rStep, CommandStatementPresenter cmdStmt, ResolvedResource rsc, Notification notification, boolean debug) throws PipelineException {
        int rtn = 1;
        LauncherI execLauncher = null;
        if (rsc.getType().equals(ResourceData.Type.EXECUTABLE)) {
            if (OSInfo.GetInstance().isRemote(rsc)) {
                if (!rsc.isSetSsh2User()) 
                    rsc.setSsh2User(System.getProperty("user.name"));
                execLauncher = new RemoteExecutableLauncher();
            }else {
                execLauncher = new LocalExecutableLauncher();
            }
             if (errorFileName != null)
                 execLauncher.setErrorFileName(errorFileName);
             if (outputFileName != null) 
                 execLauncher.setOutputFileName(outputFileName);
             execLauncher.setDebug(debug);
             rtn = execLauncher.launchProcess(parameters,rStep, cmdStmt, rsc);
        }else if (rsc.getType().equals(ResourceData.Type.HUMAN)) {
            execLauncher = new PersonnelNotificationLauncher();
            if (errorFileName != null)
                execLauncher.setErrorFileName(errorFileName);
            if (outputFileName != null) 
                execLauncher.setOutputFileName(outputFileName);
            rtn = execLauncher.launchProcess(parameters, rStep, cmdStmt, rsc);
	    }else if (rsc.getType().equals(ResourceData.Type.TRANSFORMER)) {
            execLauncher = new TransformerLauncher();
            if (errorFileName != null)
                execLauncher.setErrorFileName(errorFileName);
            if (outputFileName != null) 
                execLauncher.setOutputFileName(outputFileName);
            rtn = execLauncher.launchProcess(parameters, rStep, cmdStmt, rsc);
	    }
        notification.setStepTimeLaunched(execLauncher.getNotification().getStepTimeLaunched());
        notification.setCommand(execLauncher.getNotification().getCommand());
        return rtn;
    }
    
   
    
    
   
    
   
    
    //////////////////////////////////////////////////////////////////////////
    ////                        private methods                            ///        
    
    private  String outputFileName;
    private  String errorFileName;
    private static Logger logger = Logger.getLogger(ExecutionManager.class);
    private  Calendar launchTime;
}
