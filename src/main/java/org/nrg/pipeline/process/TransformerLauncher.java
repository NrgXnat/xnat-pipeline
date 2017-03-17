/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.converter.XmlParamsToCshParams;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.FileUtils;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.ArgumentData;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
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
	
    public int launchProcess(final ParameterData[] parameters, final ResolvedStep rStep, final CommandStatementPresenter command, final ResolvedResource rsc) throws PipelineEngineException {
        notification = new Notification();
        if (!rsc.getType().equals(ResourceData.Type.TRANSFORMER)) {
            logger.debug("Recd a non-transformer resource type");
            return -1;
        }
        try {
            if (!debug) {
            	
            	final ArgumentData xsltScriptPath = XMLBeansUtils.getArgumentById(rsc,"script");
            	final ArgumentData outFilePath = XMLBeansUtils.getArgumentById(rsc,"outfile");
            	final ArgumentData skipParameters = XMLBeansUtils.getArgumentById(rsc,"skip");

            	if (xsltScriptPath == null) {
            		//Default tcsh parameters file which can be sourced by other processes.
            		XmlParamsToCshParams converter = new XmlParamsToCshParams(new String[]{outFilePath.getValue()});
            		converter.convert(parameters, skipParameters);
            	} else {
            	    // Only write out the parameters that are not "skipped"
                    final Set<String> paramsToSkip =
                            (skipParameters != null && skipParameters.getValue() != null && skipParameters.getValue().contains(",")) ?
                                    new HashSet<>(Arrays.asList(skipParameters.getValue().split(","))) :
                                    null;
                    final ParameterData[] parametersToTransform;
                    if (paramsToSkip != null) {
                        final Set<ParameterData> notSkippedParams = new HashSet<>();
                        for (final ParameterData parameter : parameters) {
                            if (paramsToSkip.contains(parameter.getName())) {
                                continue;
                            }
                            notSkippedParams.add(parameter);
                        }
                        parametersToTransform = notSkippedParams.toArray(new ParameterData[notSkippedParams.size()]);
                    } else {
                        parametersToTransform = parameters;
                    }

                    transform(parametersToTransform, xsltScriptPath.getValue(), outFilePath.getValue());
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
        } catch (Exception e) {
            try {
                if (errorFileName != null) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(errorFileName, true));
                    out.write("\n--------------------------------------------\n");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    out.write( dateFormat.format(Calendar.getInstance().getTime()) + "\n");
                    out.write("Could not transform parameters. Exception:: " + e.getLocalizedMessage() );
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
            } catch (IOException ignored) {
                // ignored
            }
            throw new PipelineEngineException("Parameters could not be transformed " + e.getClass() + e.getLocalizedMessage(),e);
        }
        
    }

	
    public void setErrorFileName(String errorFileName) {
        this.errorFileName = errorFileName;
    }

    private void transform(final ParameterData[] parameters, final String xsltScriptPath, final String outFilePath) throws IOException, TransformerException {
        // Create the parameters XML and get it into a stream
        final ParametersDocument parametersDoc = ParametersDocument.Factory.newInstance();
        final Parameters params = parametersDoc.addNewParameters();
        params.setParameterArray(parameters);

        final ByteArrayOutputStream parameterDocOutputStream = new ByteArrayOutputStream();
        parametersDoc.save(parameterDocOutputStream);

        final StreamSource parameterDocXmlStream = new StreamSource( new ByteArrayInputStream(parameterDocOutputStream.toByteArray()));

        // Get the style sheet XML into a stream
        final File stylesheet = new File(xsltScriptPath);
        final StreamSource stylesheetStream = new StreamSource(stylesheet);

        // Use a Transformer for output
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        final Transformer transformer = tFactory.newTransformer(stylesheetStream);

        final String outputFilePathUrl = FileUtils.filenameToURL(outFilePath);
        System.out.println("Transformer streaming result to " + outputFilePathUrl);
        final StreamResult outputFileStreamResult = new StreamResult(outputFilePathUrl);

        transformer.transform(parameterDocXmlStream, outputFileStreamResult);
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
