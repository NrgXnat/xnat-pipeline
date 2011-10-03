/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.nrg.pipeline.exception.ArgumentNotFoundException;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.exception.PreConditionNotSatisfiedException;
import org.nrg.pipeline.manager.EventManager;
import org.nrg.pipeline.manager.PipelineManager;
import org.nrg.pipeline.utils.CommandLineArguments;
import org.nrg.pipeline.utils.MailUtils;
import org.nrg.pipeline.utils.ParameterUtils;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.PipelineData.Parameters;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;

//////////////////////////////////////////////////////////////////////////
//// ClassName PipelineRunner
/**
 PipelineRunner will execute the steps in a pipeline while monitoring each step.  

 @author mohanar
 @version $Id: PipelineRunner.java,v 1.2 2009/11/11 21:03:40 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineRunner implements Observer {
    
    
        /** Constructor 
         * 
         * @param args String array of command line arguments
         */
        public PipelineRunner(String args[]) {
            cmdArgs = new CommandLineArguments(args);
        }


        /** Returns true if the PipelineRunner is to send out notifications
         *           
         * @return true if -supressNotification is not used while invoking PipelineRunner
         * @see CommandLineArguments
         */
        public boolean doesNotification() {
            return !cmdArgs.isSupressNotification();
        }

        /**
         * 
         * @return name of the pipeline xml which was used to invoke PipelineRunner
         */
        
        public String getPipelineFile() {
            return cmdArgs.getPipelineFile();
        }
        
        /**
         * 
         * @return
         */
        
        public String getCsvFile() {
            return cmdArgs.getCsvFile();
        }

        public ArrayList getEmailIds() {
            return cmdArgs.getNotificationEmailIds();
        }
        
        public String getLogPropertiesFile() {
            return cmdArgs.getLogPropertiesFile();
        }
        
        
        public String getParamsFromDir() {
            return cmdArgs.getParameterDir();
        }
        


        
        public static void main(String args[]) {
            PipelineRunner pr =  new PipelineRunner(args);
            if (pr.getLogPropertiesFile() != null) {
                PropertyConfigurator.configure(pr.getLogPropertiesFile());
            }else {
                BasicConfigurator.configure();
            }
            
            try {
               String msg = pr.run();
               if (pr.doesNotification()) MailUtils.send("Pipeline Complete", "Pipeline: " + pr.getPipelineFile() + " was succesfully completed  " + msg, pr.getEmailIds(), null, null);
               System.out.println("Done");
               System.exit(0);
            }catch(Exception e ) {
                logger.info("Encountered exception " + e.getClass() + " ==> " + e.getLocalizedMessage());
                e.printStackTrace();
                logger.debug(e.getStackTrace()[0]);
                try {
                	if (pr.doesNotification())
                        MailUtils.send("Pipeline Failed", "Pipeline: " + pr.getPipelineFile() + " failed to execute <br> " + e.getLocalizedMessage(),pr.getEmailIds(),null,null);
                }catch (Exception e1) {
                    System.out.println("Couldnt send email msg");
                    e1.printStackTrace();
                }
                System.out.println("Encountered problems launching pipeline. Please look at the log file");
                System.exit(1);
            }
        }   

        public synchronized void update(Observable obj, Object msg) {
            System.out.println(msg);
        }

        
        private String run()  throws PipelineEngineException{
            String msg = "";
            if (cmdArgs.getPipelineFile() == null) {
                showUsage();
                System.exit(1);
            }
            EventManager.GetInstance().addObserver(this);
            try {
                if (cmdArgs.getParameterFile() != null) {
                    msg = launchPipeline(cmdArgs.getParameterFile());
                } else if (getCsvFile() != null) {
                    msg = launchFromCSV();
                }else if (cmdArgs.getParameterDir() != null){
                    msg = launchFromDirectory();
                } else {
                	ParametersDocument paramDoc = cmdArgs.getParametersDocument();
                	if (paramDoc.getParameters().sizeOfParameterArray() > 0) {
                        Parameters params = PipelineManager.GetInstance(cmdArgs.getConfigFile()).launchPipeline(cmdArgs.getPipelineFile(),paramDoc, cmdArgs.getStartAt(), cmdArgs.debug());
                        msg = ParameterUtils.GetParameters(params);
                	}else {
	                    String paramFile = null;
	                    Parameters params = PipelineManager.GetInstance(cmdArgs.getConfigFile()).launchPipeline(cmdArgs.getPipelineFile(),paramFile, cmdArgs.getStartAt(), cmdArgs.debug());
	                    msg = ParameterUtils.GetParameters(params);
                	}
                }
            }catch(Exception e) {
                throw new PipelineEngineException("Pipeline Failed ", e);
            }
            return msg;
        }
        
        
        private String launchPipeline(String pathToParamFile) throws PipelineEngineException, ArgumentNotFoundException, XmlException, PreConditionNotSatisfiedException, TransformerException {
            String msg = "";
            Parameters params = PipelineManager.GetInstance(cmdArgs.getConfigFile()).launchPipeline(cmdArgs.getPipelineFile(),pathToParamFile, cmdArgs.getStartAt(), cmdArgs.debug());
            msg = ParameterUtils.GetParameters(params);
            return msg;
        }

        private String launchFromDirectory() throws PipelineEngineException, ArgumentNotFoundException, XmlException, PreConditionNotSatisfiedException, TransformerException {
            String msg = " in batch mode. Parameters sepecified in " + cmdArgs.getParameterDir() + " <br>";
            File paramdir = new File(cmdArgs.getParameterDir());
            String[] paramFiles = paramdir.list(new FilenameFilter() {
                public boolean accept(File d, String name) { return name.endsWith(".xml"); }
              });
            String file="";
            try {
                for (int i = 0; i < paramFiles.length; i++) {
                    file = paramdir.getAbsolutePath() + File.separator + paramFiles[i];
                    launchPipeline(file);
                }
            }catch(Exception e) {
                
                throw new PipelineEngineException("Unable to completely run pipeline in batch mode. Failed on file " + file,e);
            }
            return msg;
        }    
        
        private String launchFromCSV() throws PipelineEngineException  {
            String msg = " in batch mode. Parameters sepecified in " + cmdArgs.getCsvFile() + " <br>";
            int lastCompleteLineNumber = -1;
            try {
                LabeledCSVParser lcsvp = new LabeledCSVParser(new CSVParser(new BufferedReader( new FileReader( cmdArgs.getCsvFile()) )));
                String[] labels = lcsvp.getLabels();
                while (lcsvp.getLine() != null) {
                    ParametersDocument paramDoc = ParametersDocument.Factory.newInstance();
                    ParametersDocument.Parameters params = paramDoc.addNewParameters();
                    for (int i = 0; i < labels.length; i++) {
                        ParameterData param = params.addNewParameter();
                        param.setName(labels[i]);
                        String csvValues = lcsvp.getValueByLabel(labels[i]);
                        StringTokenizer tokens = new StringTokenizer(csvValues,",");
                        Values values = param.addNewValues();
                        if (tokens.countTokens() == 1) {
                            values.setUnique(tokens.nextToken());
                        }else { 
                            while (tokens.hasMoreTokens()) {
                                values.addList(tokens.nextToken());
                            }
                        }
                    }
                    PipelineManager.GetInstance(cmdArgs.getConfigFile()).launchPipeline(getPipelineFile(),paramDoc,  cmdArgs.getStartAt(), cmdArgs.debug());
                }
                lastCompleteLineNumber = lcsvp.getLastLineNumber();
                msg += " Last complete line number " + lastCompleteLineNumber;
            }catch(IOException io) {
                throw new PipelineEngineException("Unable to read CSV file " +  cmdArgs.getCsvFile(),io);
            }catch(Exception e) {
                throw new PipelineEngineException("Unable to completely run pipeline in batch mode. Last complete successful line number " + lastCompleteLineNumber + " from Csv File " + cmdArgs.getCsvFile() ,e);
            }
            return msg;
        }

        /**
         * Display usage for invoking PipelineRunner
         *
         */
        
        private void showUsage() {
            cmdArgs.printUsage();
        }


        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////
        
        CommandLineArguments cmdArgs;
        static Logger logger = Logger.getLogger(PipelineRunner.class);
}

