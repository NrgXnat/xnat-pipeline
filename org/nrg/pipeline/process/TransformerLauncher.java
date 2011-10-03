/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.nrg.pipeline.converter.XmlParamsToCshParams;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.ArgumentData;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: TransformerLauncher.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class TransformerLauncher implements LauncherI  {
	
    public int launchProcess(ParameterData[] parameters, ResolvedStep rStep, CommandStatementPresenter command,
            ResolvedResource rsc) throws PipelineEngineException {
        notification = new Notification();
        if (!rsc.getType().equals(ResourceData.Type.TRANSFORMER)) {
            logger.debug("Recd a non-transformer resource type");
            return -1;
        }
        try {
            if (!debug) {
            	
            	ArgumentData xsltScriptPath = XMLBeansUtils.getArgumentById(rsc,"script");
            	ArgumentData outFilePath = XMLBeansUtils.getArgumentById(rsc,"outfile");
            	ArgumentData skipParameters = XMLBeansUtils.getArgumentById(rsc,"skip");

            	if (xsltScriptPath == null) {
            		//Default tcsh parameters file which can be sourced by other processes.
            		XmlParamsToCshParams converter = new XmlParamsToCshParams(new String[]{outFilePath.getValue()});
            		converter.convert(parameters, skipParameters);
            	}else {
            		//TODO fill in this with appropriate call to the XSLT Transformer
            	}

            	if (outputFileName != null) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName, true));
                    out.write("\n--------------------------------------------\n");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    out.write( dateFormat.format(Calendar.getInstance().getTime()) + "\n");
                    out.write("Parameters created in  "  + outFilePath.getValue());
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
                notification.setCommand("Parameters created in " + outFilePath.getValue());
            }
            notification.setStepTimeLaunched(AdminUtils.getTimeLaunched());
            return 0;
        }catch(Exception e) {
            try {
                if (errorFileName != null) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(errorFileName, true));
                    out.write("\n--------------------------------------------\n");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    out.write( dateFormat.format(Calendar.getInstance().getTime()) + "\n");
                    out.write("Couldnt transform parameters. Exception:: " + e.getLocalizedMessage() );
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
            }catch(Exception e1){}
            throw new PipelineEngineException("Parameters couldnt be transformed " + e.getClass() + e.getLocalizedMessage(),e);
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
    
    static Logger logger = Logger.getLogger(TransformerLauncher.class);
    

}
