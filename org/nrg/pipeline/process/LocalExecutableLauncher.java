/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.ProvenanceUtils;
import org.nrg.pipeline.utils.StringUtils;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LocalExecutableLauncher.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class LocalExecutableLauncher implements LauncherI {
    
    public int launchProcess(ParameterData[] parameters, ResolvedStep rStep, CommandStatementPresenter command,
            ResolvedResource rsc) throws PipelineEngineException {
        String workDirectory = rStep.getWorkdirectory();
        notification = new Notification();
        int exitVal = -1;
        try {  
            LocalProcessLauncher launcher = new LocalProcessLauncher(outputFileName,errorFileName);
            notification.setStepTimeLaunched(AdminUtils.getTimeLaunched());
            notification.setCommand(StringUtils.getAsString(launcher.getCmdArray()));
            
            if (!debug) {
                launcher.launchProcess(command,workDirectory, -1);
                exitVal = launcher.getExitValue();
            } else {
                exitVal = 0;
            }
            
            ProvenanceUtils.addProcessStep(rStep, command,rsc, notification.getStepTimeLaunched(), debug);
            return exitVal;
        } catch (Throwable t) {
            logger.info("Problem launching command " + command,t);
            throw new PipelineEngineException(t.getClass() + "==>" +  t.getLocalizedMessage());
        }    
    }
    
      

    public void setErrorFileName(String errorFileName) {
        this.errorFileName = errorFileName;
    }
    
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    
    public Notification getNotification() {
      return notification;   
    }
    
    public void setDebug(boolean debugMode) {
        debug = debugMode;    
    }
    
    Notification notification = null;
    boolean debug;

    String errorFileName = null, outputFileName = null;
    static Logger logger = Logger.getLogger(LocalExecutableLauncher.class);
    
    public static void main(String args[]) {
        BasicConfigurator.configure();
        LocalExecutableLauncher launch  = new LocalExecutableLauncher();
        launch.setErrorFileName("err.txt");
        launch.setErrorFileName("out.txt");
        ResolvedStep rStep = ResolvedStep.Factory.newInstance();
        ResolvedResource rsc = ResolvedResource.Factory.newInstance();
        try {
            launch.launchProcess(null,rStep,new CommandStatementPresenter("matlab  -nodisplay -nojvm -nosplash -r \"ls /tmp; exit;\"",""), rsc);
            System.out.println("All Done");
            System.exit(0);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
