/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.io.File;

import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.StreamGobbler;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LocalProcessLauncher.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class LocalProcessLauncher {
   
    

    public LocalProcessLauncher(String outFile, String errFile) {
        this.outputFileName = outFile;
        this.errorFileName = errFile;
        cmdArray = null;
    }
    
    public void launchProcess(CommandStatementPresenter command,String workDirectory, final long timeOut) throws PipelineEngineException {
        try {  
            setCommand(command);
            File dir = null;
            if (workDirectory != null) {
                dir = new File(workDirectory);
                if (!dir.exists()) {
                    logger.info("Work Directory: " + workDirectory + " doesnt exist ");
                    throw new PipelineEngineException("Work Directory: " + workDirectory + " doesnt exist ");
                }
                logger.info("Command to be executed in " + workDirectory);
            }
            logger.info("Executing " + command.getCommand(CommandStatementPresenter.PUBLIC_VIEW));
    
            Runtime rt = Runtime.getRuntime();
            if (cmdArray != null ) {
                for (int i = 0; i < cmdArray.length; i++) {
                    System.out.print(cmdArray[i] + " ");
                }
                System.out.println();
            }
            final Process proc = rt.exec(cmdArray,null,dir);
            StreamGobbler errorGobbler = new 
            StreamGobbler(proc.getErrorStream(), "ERROR"); 
            if (workDirectory != null)errorGobbler.log("WorkDirectory " + workDirectory +"\n");
            errorGobbler.log("Executing: " + command.getCommand(CommandStatementPresenter.PUBLIC_VIEW));
            if (errorFileName != null)
                errorGobbler.setFile(errorFileName);
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT");
            if (workDirectory != null)outputGobbler.log("WorkDirectory " + workDirectory +"\n");
            outputGobbler.log("Executing: " + command.getCommand(CommandStatementPresenter.PUBLIC_VIEW) + "\n");
            if (outputFileName != null)
                outputGobbler.setFile(outputFileName);
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            Thread destroyProcess = null;
            if (timeOut > 0) {
                 destroyProcess = new Thread() {
                  public void run() {
                    try {
                      sleep(timeOut);
                    } catch (InterruptedException ie) {
                    } finally {
                      proc.destroy();
                    }
                  }
                };
                destroyProcess.start();
           }
                                    
            // any error???
            exitValue = proc.waitFor();
            if (exitValue == 0 && destroyProcess != null &&  destroyProcess.isAlive())
                destroyProcess.interrupt();
            streamOutput = outputGobbler.getString();
            streamErrOutput = errorGobbler.getString();
            
            errorGobbler.finish(); outputGobbler.finish();
            logger.info("ExitValue: " + exitValue);
        } catch (Throwable t) {
            System.out.println("Unable to launch " + command);
            t.printStackTrace();
            logger.info("Problem launching command " + command,t);
            throw new PipelineEngineException(t.getClass() + "==>" +  t.getLocalizedMessage());
        }    
    }
    /**
     * @return Returns the cmdArray.
     */
    public String[] getCmdArray() {
        return cmdArray;
    }
    
    public void setCommand(CommandStatementPresenter command) {
        this.command = command.getCommand(CommandStatementPresenter.PRIVATE_VIEW);
        cmdArray = new CommandTool().getCommandArray(command.getCommand(CommandStatementPresenter.PRIVATE_VIEW));
    }

    /**
     * @return Returns the errorFileName.
     */
    public String getErrorFileName() {
        return errorFileName;
    }

    /**
     * @return Returns the exitValue.
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * @return Returns the outputFileName.
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * @return Returns the streamErrOutput.
     */
    public String getStreamErrOutput() {
        return streamErrOutput;
    }

    /**
     * @return Returns the streamOutput.
     */
    public String getStreamOutput() {
        return streamOutput;
    }
    
    String command;
    String cmdArray[];
    String streamOutput, streamErrOutput;
    String errorFileName = null, outputFileName = null;
    int exitValue;
    static Logger logger = Logger.getLogger(LocalProcessLauncher.class);
}
