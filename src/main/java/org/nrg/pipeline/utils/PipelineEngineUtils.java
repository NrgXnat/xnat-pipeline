/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PipelineUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineEngineUtils {

    public static PipelineDocument getPipelineDocument(String pathToPipelineXmlFile) throws PipelineEngineException {
        try {
            if (!pathToPipelineXmlFile.endsWith(".xml")) pathToPipelineXmlFile += ".xml";
            XmlObject xmlObject = new XmlReader().read(pathToPipelineXmlFile);
            if (!(xmlObject instanceof PipelineDocument)) {
                logger.error("getPipelineDocument() :: Invalid XML file supplied " + pathToPipelineXmlFile + " ==> Expecting a pipeline document"); 
                throw new PipelineEngineException("Invalid XML file supplied " + pathToPipelineXmlFile + " ==> Expecting a pipeline document");
            }
            PipelineDocument pipelineDoc = (PipelineDocument)xmlObject; 
            String errors = XMLBeansUtils.validateAndGetErrors(pipelineDoc);
            if (errors != null) {
                throw new XmlException("Invalid XML " + pathToPipelineXmlFile + "\n" + errors);
            }
            return pipelineDoc;
        }catch(IOException ioe) {
            logger.error("File not found " + pathToPipelineXmlFile);
            throw new PipelineEngineException(ioe.getClass() + "==>" + ioe.getLocalizedMessage(), ioe);
        }catch (XmlException xmle ) {
            logger.error(xmle.getLocalizedMessage());
            throw new PipelineEngineException(xmle.getClass() + "==>" + xmle.getLocalizedMessage(), xmle);
        }catch(PipelineEngineException ane) {
            logger.error(ane.getLocalizedMessage());
            throw new PipelineEngineException(ane.getClass() + "==>" + ane.getLocalizedMessage(), ane);
        }
        
    }
    
    public static String getPathToOutputFile(PipelineDocument pipelineDoc, String type) {
        String rtn = System.getProperty("user.home")+ File.separator + pipelineDoc.getPipeline().getName();
        if (pipelineDoc.getPipeline().isSetOutputFileNamePrefix()) {
            rtn = pipelineDoc.getPipeline().getOutputFileNamePrefix();
        }
        if (type != null) {
            if (type.equalsIgnoreCase("ERROR")) rtn += ".err";
            else rtn += ".log";
        }
        return rtn;
    }
    
    public static String getResolvedPipelineXmlName(AllResolvedStepsDocument pipelineDoc) {
        String rtn = System.getProperty("user.home") + File.separator + pipelineDoc.getAllResolvedSteps().getName();
        if (pipelineDoc.getAllResolvedSteps().isSetOutputFileNamePrefix()) {
            rtn = pipelineDoc.getAllResolvedSteps().getOutputFileNamePrefix();
        }
        rtn += "_" + pipelineDoc.getAllResolvedSteps().getName()+ ".xml";
        return rtn;
    }
    
    public static void resolveXPath(PipelineDocument pipelineDoc) throws PipelineEngineException, TransformerException {
        ParameterUtils.setParameterValues(pipelineDoc);
        logger.info("Parameter Resolved");
        LoopUtils.setLoopValues(pipelineDoc);
        logger.info("Loop Resolved");
 
        if (pipelineDoc.getPipeline().isSetOutputFileNamePrefix()) {
            try {
                ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(),pipelineDoc.getPipeline().getOutputFileNamePrefix());
                if (values != null && values.size() > 0) {
                    pipelineDoc.getPipeline().setOutputFileNamePrefix((String)values.get(0));
                }
            }catch(TransformerException te) {
                throw new PipelineEngineException("Couldnt resolve output filename prefix " + pipelineDoc.getPipeline().getOutputFileNamePrefix(), te);
            }
        }
        logger.info("Output resolved");
    }
    
    public static void checkStepIds(PipelineDocument pipelineDoc) throws PipelineEngineException {
        Step[] stepArray = pipelineDoc.getPipeline().getSteps().getStepArray();
        String[] stepIds = new String[stepArray.length];
        Hashtable seenIds = new Hashtable();
        for (int i = 0; i < stepArray.length; i++) {
            if (seenIds.containsKey(stepArray[i].getId())) {
                throw new PipelineEngineException("Duplicate step id " + stepArray[i].getId());
            }else 
                seenIds.put(stepArray[i].getId(),new Integer(0));
            stepIds[i] = stepArray[i].getId();
        }
        seenIds = null;
        //Arrays.sort(stepIds, String.CASE_INSENSITIVE_ORDER);
        //Step[] sortedSteps = new Step[stepArray.length];
        //for (int i = 0; i < stepArray.length; i++) {
        //    int index = Arrays.binarySearch(stepIds,stepArray[i].getId());
        //    sortedSteps[index] = stepArray[i];
       // }
       // CollectionUtils.free(stepIds);
       // pipelineDoc.getPipeline().getSteps().setStepArray(sortedSteps);
    }
    
    
    static Logger logger = Logger.getLogger(PipelineEngineUtils.class);
    
}
