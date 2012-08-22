/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.PipeletData;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument.AllResolvedSteps;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.PipelineData.Parameters;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ParameterUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class ParameterUtils {
    
    public static String GetParameters(ParametersDocument paramDoc) {
        return GetParameters(paramDoc.getParameters());
    }
        
    public static String GetParameters(ParametersDocument.Parameters params) {
        String msg = "";
        if (params != null) {
            msg += "<br>Parameters passed::<br>";
            for (int i = 0; i < params.sizeOfParameterArray(); i++) {
                if (params.getParameterArray(i).getName().equals(PipelineConstants.PWD_PARAMETER)) {
                    msg += params.getParameterArray(i).getName() + " = [ ******** ]";
                    msg += "<br>";
                    continue;
                }
                msg += params.getParameterArray(i).getName() + " = [ " ;
                if (params.getParameterArray(i).getValues().isSetUnique())
                    msg += params.getParameterArray(i).getValues().getUnique();
                else
                    msg += StringUtils.getAsString(params.getParameterArray(i).getValues().getListArray()); 
                msg += " ]";
                msg += "<br>";
            }
        }
        return msg;
    }
    
    public static String GetParameters(Parameters params) {
        String msg = "";
        if (params != null) {
            msg += "<br>Parameters passed::<br>";
            for (int i = 0; i < params.sizeOfParameterArray(); i++) {
                if (params.getParameterArray(i).getName().equals(PipelineConstants.PWD_PARAMETER)) {
                    msg += params.getParameterArray(i).getName() + " =[ ******** ]";
                    msg += "<br>";
                    continue;
                }
                msg += params.getParameterArray(i).getName() + " = [ " ;
                if (params.getParameterArray(i).getValues().isSetUnique())
                    msg += params.getParameterArray(i).getValues().getUnique();
                else
                    msg += StringUtils.getAsString(params.getParameterArray(i).getValues().getListArray()); 
                msg += " ]";
                msg += "<br>";
            }
        }
        return msg;
    }
    
    
    
    
        public static void addParameter(PipelineDocument pipelineDoc, ParametersDocument parameterDoc) throws PipelineEngineException {
            if (parameterDoc.getParameters() != null ) {
                for (int i = 0; i < parameterDoc.getParameters().getParameterArray().length; i++) {
                   ParameterData param =  parameterDoc.getParameters().getParameterArray(i);
                   if (param != null) {
                       ParameterData existingParam = XMLBeansUtils.getParameterByName(pipelineDoc.getPipeline(),param.getName());
                       if (existingParam != null) {
                           copy(param,existingParam,true);
                           if (!param.getName().equals(PipelineConstants.PWD_PARAMETER)) {
                               if (existingParam.getValues().isSetUnique()) {
                                   logger.debug(" Unique: " + existingParam.getValues().getUnique());
                               }else {
                                   logger.debug(StringUtils.getAsString(existingParam.getValues().getListArray()));
                               }
                               logger.debug(" After copying values are " );
                               if (existingParam.getValues().isSetUnique()) {
                                   logger.debug(" Unique: " + existingParam.getValues().getUnique());
                               }else {
                                   logger.debug(StringUtils.getAsString(existingParam.getValues().getListArray()));
                               }
                           }
                       }else {
                           ParameterData newParam = null;
                           if (pipelineDoc.getPipeline().isSetParameters()) {
                               newParam = pipelineDoc.getPipeline().getParameters().addNewParameter();
                           }else {
                               newParam = pipelineDoc.getPipeline().addNewParameters().addNewParameter();
                           }
                           copy(param,newParam,true);
                       }
                   }
                }
            }
        }    
        
    
    
    public static void addParameter(PipelineDocument pipelineDoc, String parameterFile) throws PipelineEngineException {
        try {
            XmlObject xmlObject = new XmlReader().read(parameterFile);
            if (!(xmlObject instanceof ParametersDocument)) {
                logger.error("addParameter() :: Invalid XML file supplied. Expecting a parameter document"); 
                throw new PipelineEngineException("Invalid XML file supplied " + parameterFile + " ==> Expecting a parameters document");
            }
            ParametersDocument parameterDoc = (ParametersDocument)xmlObject; 
            String errors = XMLBeansUtils.validateAndGetErrors(parameterDoc);
            if (errors != null) {
                throw new XmlException(" Invalid XML " + parameterFile + "\n" + errors);
            }
            addParameter(pipelineDoc,parameterDoc);
        }catch(IOException ioe) {
            logger.error("File not found " + parameterFile);
            throw new PipelineEngineException(ioe.getClass() + "==>" + ioe.getLocalizedMessage(), ioe);
        }catch (XmlException xmle ) {
            logger.error(xmle.getLocalizedMessage());
            throw new PipelineEngineException(xmle.getClass() + "==>" + xmle.getLocalizedMessage(),xmle);
        }catch(PipelineEngineException ane) {
            ane.printStackTrace();
            logger.error(ane.getLocalizedMessage());
            throw new PipelineEngineException(ane.getClass() + "==>" + ane.getLocalizedMessage(),ane);
        }
        
    }
    
    
    public static void setParameterValues(PipelineDocument pipelineDoc) throws PipelineEngineException, TransformerException{
            String xPathToParameter = PipelineConstants.PIPELINE_XPATH_MARKER + "/Pipeline/parameters/parameter" + PipelineConstants.PIPELINE_XPATH_MARKER;
            XmlObject[] params = XPathUtils.executeQuery(pipelineDoc,xPathToParameter);
            PipelineData pipelineData = pipelineDoc.getPipeline();
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                	ParameterData aParam = (ParameterData)params[i];
                    ParameterData resolvedParam  = ParameterData.Factory.newInstance();
                    copy(aParam, resolvedParam, false);
                    resolveParameterValues(pipelineData, aParam,resolvedParam);
                    pipelineDoc.getPipeline().getParameters().setParameterArray(i,resolvedParam);
                        //params[i] = resolvedParam;
                }
            }
    }
    
    private static void resolveParameterValues(PipelineData pipelineData, ParameterData aParam, ParameterData resolvedParam) throws PipelineEngineException, TransformerException {
        Values aValues = aParam.getValues();
        Values resolvedValues = resolvedParam.getValues();
        if (resolvedValues == null) resolvedValues = resolvedParam.addNewValues();
        if (aValues.isSetUnique()) {
            ArrayList xparamValues = XPathResolverSaxon.GetInstance().evaluate(pipelineData, aValues.getUnique());
            if (xparamValues != null && xparamValues.size() > 1) {
                throw new PipelineEngineException("Incorrect formulation of the Pipeline xml. Check the Parameter node  " + aParam.xmlText());
            }
            resolvedValues.setUnique((String)xparamValues.get(0));
        }else {
            for (int i = 0; i < aValues.getListArray().length; i++) {
                ArrayList xparamValues = XPathResolverSaxon.GetInstance().evaluate(pipelineData, aValues.getListArray(i));
                if (xparamValues != null && xparamValues.size() > 0) {
                    for (int k = 0; k < xparamValues.size(); k++) {
                        resolvedValues.addList((String)xparamValues.get(k));
                    }
                }else if (xparamValues != null && xparamValues.size() == 0) {
                	System.out.println("ParameterUtils:: Looks like the parameter " + aParam.getName() + " doesnt resolve to any values");
                }
                else throw new PipelineEngineException("Couldnt resolve xpath expression " + aValues.getListArray(i));                
            }
        }
    }
    
    public static void copyParameters(PipelineDocument from, PipelineDocument to) {
        if (from == null || to == null) 
            return;
        PipelineData fromPipeline = from.getPipeline();
        PipelineData toPipeline = to.getPipeline();
        if (fromPipeline == null || toPipeline == null)
            return;
        if (!fromPipeline.isSetParameters()) return;
        Parameters toParameters = toPipeline.getParameters();
        if (toParameters == null) {
            toParameters = toPipeline.addNewParameters();
        }
        for (int i = 0; i < fromPipeline.getParameters().sizeOfParameterArray(); i++) {
            ParameterData toParam = toParameters.addNewParameter();
            //toParam = (ParameterData)fromPipeline.getParameters().getParameterArray(i).copy();
            copy(fromPipeline.getParameters().getParameterArray(i), toParam);
        }
        
    }
    
    public static void copyParameters(Step aStep, PipelineDocument from, PipelineDocument to) throws PipelineEngineException, TransformerException {
        if (aStep == null || to == null) 
            return;
        PipelineData toPipeline = to.getPipeline();
        if (toPipeline == null)
            return;
        
        if (aStep.isSetPipelet() && aStep.getPipelet().sizeOfParametersArray() > 0) {
            Parameters toParameters = toPipeline.getParameters();
            if (toParameters == null) {
                toParameters = toPipeline.addNewParameters();
            }
            PipeletData pipeletData = aStep.getPipelet();
            for (int i =0; i < pipeletData.sizeOfParametersArray(); i++) {
                //Look for existing parameter and check to see if override flag is set to true. If true
                //override the existing value
                ParameterData toParam = null;
                toParam = XMLBeansUtils.getParameterByName(toPipeline,aStep.getPipelet().getParametersArray(i).getName());
                if (toParam == null ) {
                    toParam = toParameters.addNewParameter();
                    copy(aStep.getPipelet().getParametersArray(i),toParam, false);
                }//else if (aStep.getPipelet().getParametersArray(i).isSetOverride() && aStep.getPipelet().getParametersArray(i).getOverride()) {
                  //  throw new PipelineException("Parameter has not been overridden for pipelet " + aStep.getPipelet().getName() + " at step[id= " + aStep.getId()+" ]");
                //}
                if (aStep.getPipelet().getParametersArray(i).isSetOverride() && aStep.getPipelet().getParametersArray(i).getOverride()) {
                    //override is true
                    Values toValues = toParam.getValues();
                    if (toValues == null) toValues = toParam.addNewValues();
                    //clear existing values
                    if (toValues.isSetUnique()) toValues.unsetUnique();
                    if (toValues.sizeOfListArray() > 0) {
                        for (int j = 0; j < toValues.sizeOfListArray(); j++) {
                            toValues.removeList(j);
                        }
                    }
                }
                resolveParameterValues(from.getPipeline(), aStep.getPipelet().getParametersArray(i),toParam);
                //toParam = (ParameterData)fromPipeline.getParameters().getParameterArray(i).copy();
                //copy(aStep.getPipelet().getParametersArray(i), toParam);
            }
        }
    }
    
    public static void copyParameters(PipelineDocument from, AllResolvedSteps to) {
        if (from == null || to == null) 
            return;
        PipelineData fromPipeline = from.getPipeline();
        if (fromPipeline == null)
            return;
        if (!fromPipeline.isSetParameters()) return;
        AllResolvedSteps.Parameters toParameters = to.getParameters();
        if (toParameters == null) {
            toParameters = to.addNewParameters();
        }
        for (int i = 0; i < fromPipeline.getParameters().sizeOfParameterArray(); i++) {
            ParameterData toParam = toParameters.addNewParameter();
            //toParam = (ParameterData)fromPipeline.getParameters().getParameterArray(i).copy();
            copy(fromPipeline.getParameters().getParameterArray(i), toParam);
        }
    }

    
    public static void copyParameters(PipelineDocument to, ParametersDocument from) {
        if (from == null || to == null) 
            return;
        ParametersDocument.Parameters fromParams = from.getParameters();
        PipelineData toPipeline = to.getPipeline();
        if (fromParams == null || toPipeline == null)
            return;
        Parameters toParameters = toPipeline.getParameters();
        if (toParameters == null) {
            toParameters = toPipeline.addNewParameters();
        }
        for (int i = 0; i < fromParams.sizeOfParameterArray(); i++) {
            ParameterData toParam = toParameters.addNewParameter();
            copy((ParameterData)fromParams.getParameterArray(i), toParam);
            //toParam = (ParameterData)fromParams.getParameterArray(i).copy();
        }
    }
    
    
    
    
    public static void copy(ParameterData from, ParameterData to, boolean copyValues) {
        if (from.isSetOverride()) to.setOverride(from.getOverride());
        to.setName(from.getName());
        if (from.isSetDescription())
            to.setDescription(from.getDescription());
        if (copyValues) {
            Values values = to.getValues();
            if (values == null) values = to.addNewValues();
            if (from.getValues().isSetUnique()) {
                values.setUnique(from.getValues().getUnique());
            }else {
                for (int i = 0; i < from.getValues().getListArray().length; i++) 
                    values.addList(from.getValues().getListArray(i));
            }
        }
    }
    
    public static void maskPwdParameter(AllResolvedStepsDocument allResolvedStepsDocument) {
        if (allResolvedStepsDocument != null) {
            AllResolvedStepsDocument.AllResolvedSteps.Parameters params = allResolvedStepsDocument.getAllResolvedSteps().getParameters();
            for (int i = 0; i < params.sizeOfParameterArray(); i++) {
                if (params.getParameterArray(i).getName().equals(PipelineConstants.PWD_PARAMETER)) {
                    params.getParameterArray(i).getValues().setUnique("********");
                    break;
                }
            }
            
        }
    }
    
    public static void copy(ParameterData from, ParameterData to) {
        copy(from,to,true);
    } 
    
    static Logger logger = Logger.getLogger(ParameterUtils.class);
    
    
}
