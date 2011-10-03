/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.xml.transform.TransformerException;

import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.OutputData;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.PipeletData;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step.Resource;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step.Resource.Argument;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: StepUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class StepUtils {
    
  public static String getStepId(Step aStep) {
      return StepUtils.getStepId(aStep.getId());
  }
  
  public static String getStepId(String id) {
      String rtn = id;
      int index = rtn.lastIndexOf(PipelineConstants.STEP_ID_SEPARATOR);
      if (index != -1) {
          rtn = rtn.substring(0, index);
      }
      return rtn;
  }
    
   public static void copy(ResolvedStep from, ResolvedStep to) {
       to.set(from);
       /*to.setId(from.getId()); to.setDescription(from.getDescription());
       to.setStatus(from.getStatus());
       if (from.isSetAwaitApprovalToProceed()) {
           to.setAwaitApprovalToProceed(from.getAwaitApprovalToProceed());
       }
       if (from.isSetContinueOnFailure()) {
           to.setContinueOnFailure(from.getContinueOnFailure());
       }else to.setContinueOnFailure(false);
       if (from.isSetPrecondition()) to.setPrecondition(from.getPrecondition());
       if (from.isSetPreconditionType()) to.setPreconditionType(from.getPreconditionType());
       if (from.isSetWorkdirectory()) to.setWorkdirectory(from.getWorkdirectory());
       if (from.isSetGotoStepId())  to.setGotoStepId(from.getGotoStepId());
       to.setResolvedResourceArray(from.getResolvedResourceArray());
       if (from.sizeOfResolvedOutputArray() > 0 ) {
           to.setResolvedOutputArray(from.getResolvedOutputArray());
       }*/
   }
    
   private static void copy(Step from, Step to, String replace, String replaceWith, int index) {
       to.setId(from.getId()+":"+(replaceWith==null?"":replaceWith));
       if (from.isSetAwaitApprovalToProceed()) {
           to.setAwaitApprovalToProceed(from.getAwaitApprovalToProceed());
       }
       if (from.isSetContinueOnFailure()) {
           to.setContinueOnFailure(from.getContinueOnFailure());
       }else to.setContinueOnFailure(false);
       to.setDescription(from.getDescription());
       if (from.isSetPrecondition()) {
           if (replace != null && replaceWith != null)
               if (replaceWith.startsWith("'")) 
                   to.setPrecondition(org.apache.commons.lang.StringUtils.replace(from.getPrecondition(),replace,replaceWith));
               else
                   to.setPrecondition(org.apache.commons.lang.StringUtils.replace(from.getPrecondition(),replace,"'" + replaceWith + "'"));
           else
              to.setPrecondition(from.getPrecondition());
       }
       if (from.isSetPreconditionType()) {
           to.setPreconditionType(from.getPreconditionType());
       }else {
           to.setPreconditionType(Step.PreconditionType.RELAXED);
       }
       if (from.isSetGotoStepId()) 
           to.setGotoStepId(from.getGotoStepId());
       if (from.isSetWorkdirectory()) {
           if (replace != null && replaceWith != null)
               if (replaceWith.startsWith("'"))
                   to.setWorkdirectory(org.apache.commons.lang.StringUtils.replace(from.getWorkdirectory(),replace,replaceWith));
               else 
                   to.setWorkdirectory(org.apache.commons.lang.StringUtils.replace(from.getWorkdirectory(),replace,"'" + replaceWith + "'"));
           else
               to.setWorkdirectory(from.getWorkdirectory());
       }
       if (from.isSetPipelet()) {
           PipeletData fromPipelet = from.getPipelet();
           if (fromPipelet.getParametersArray() != null) {
               PipeletData newPipelet = to.addNewPipelet();
               newPipelet.setName(fromPipelet.getName());
               newPipelet.setLocation(fromPipelet.getLocation());
               for (int k = 0; k < fromPipelet.sizeOfParametersArray(); k++) {
                   ParameterData fromPipeletParam = fromPipelet.getParametersArray(k);
                   ParameterData newPipeletParam = newPipelet.addNewParameters();
                   newPipeletParam.setName(fromPipeletParam.getName());
                   if (fromPipeletParam.isSetOverride()) newPipeletParam.setOverride(fromPipeletParam.getOverride());
                   if (fromPipeletParam.isSetDescription()) newPipeletParam.setDescription(fromPipeletParam.getDescription());
                   if (fromPipeletParam.getValues().isSetUnique()) {
                       if (replace != null && replaceWith != null) {
                           String replacedValue = org.apache.commons.lang.StringUtils.replace(fromPipeletParam.getValues().getUnique(),replace,"'"+replaceWith+"'");
                           if (index != -1) {
                               if (replace.indexOf("_LOOPON(")!= -1 ) {
                                   String pipeline_replace_index_str = org.apache.commons.lang.StringUtils.replace(replace,"LOOPON","REPLACE_INDEX");
                                   String newVal = org.apache.commons.lang.StringUtils.replace(replacedValue,pipeline_replace_index_str,""+index);
                                   newPipeletParam.addNewValues().setUnique(newVal);
                               }
                           }
                       } else
                           newPipeletParam.addNewValues().set(fromPipeletParam.getValues()); 
                   }else {
                       if (replace != null && replaceWith != null) {
                           for (int x = 0 ; x < fromPipeletParam.getValues().sizeOfListArray(); x++) {
                               String replacedValue = org.apache.commons.lang.StringUtils.replace(fromPipeletParam.getValues().getListArray(x),replace,"'"+replaceWith+"'");
                               if (index != -1) {
                                   if (replace.indexOf("_LOOPON(")!= -1 ) {
                                       String pipeline_replace_index_str = org.apache.commons.lang.StringUtils.replace(replace,"LOOPON","REPLACE_INDEX");
                                       String newVal = org.apache.commons.lang.StringUtils.replace(replacedValue,pipeline_replace_index_str,""+index);
                                       newPipeletParam.addNewValues().addList(newVal);
                                   }
                               }
                           }
                       }else {
                           newPipeletParam.addNewValues().set(fromPipeletParam.getValues());
                       }
                   }
               }
           }else 
               to.setPipelet(from.getPipelet());
       }else {
           for (int k = 0; k < from.sizeOfResourceArray(); k++) {
               Resource rsc = from.getResourceArray(k);
               Resource newRsc = to.addNewResource();
               if (rsc.isSetPipeId())newRsc.setPipeId(rsc.getPipeId());
               newRsc.setName(rsc.getName());
               newRsc.setLocation(rsc.getLocation());
               if (rsc.isSetSsh2Host()) newRsc.setSsh2Host(rsc.getSsh2Host());
               if (rsc.isSetSsh2Identity()) newRsc.setSsh2Identity(rsc.getSsh2Identity());
               if (rsc.isSetSsh2Password()) newRsc.setSsh2Password(rsc.getSsh2Password());
               if (rsc.isSetSsh2User()) newRsc.setSsh2User(rsc.getSsh2User());
               
               for (int l = 0; l < rsc.sizeOfArgumentArray(); l++) {
                   Argument arg = rsc.getArgumentArray(l);
                   Argument newArg = newRsc.addNewArgument();
                   newArg.setId(arg.getId());
                   if (arg.sizeOfValueArray() > 0) {
                       for (int m = 0; m < arg.sizeOfValueArray(); m++) {
                           if (replace != null && replaceWith != null) {
                               String replacedValue = org.apache.commons.lang.StringUtils.replace(arg.getValueArray(m),replace,"'"+replaceWith+"'");
                               if (index != -1) {
                                   if (replace.indexOf("_LOOPON(")!= -1 ) {
                                       String pipeline_replace_index_str = org.apache.commons.lang.StringUtils.replace(replace,"LOOPON","REPLACE_INDEX");
                                       String newVal = org.apache.commons.lang.StringUtils.replace(replacedValue,pipeline_replace_index_str,""+index);
                                       newArg.addValue(newVal);
                                   }
                               }
                           } else
                               newArg.addValue(arg.getValueArray(m));
                       }
                   }
               }
           }
       }
       if (from.sizeOfOutputArray() > 0 ) {
           for (int i = 0; i <  from.sizeOfOutputArray(); i++) {
               OutputData out = from.getOutputArray(i);
               OutputData newOut = to.addNewOutput();
               OutPutUtils.copy(out,newOut);
           }
       }
   }
    
  /* public static void createNewSpaceParameter(Step aStep, PipelineDocument from, AllResolvedStepsDocument resolvedDoc) {
       PipeletData pipeletData = aStep.getPipelet();
       for (int i =0; i < pipeletData.sizeOfParametersArray(); i++) {
           ParameterData aStepParam = aStep.getPipelet().getParametersArray(i);
           if (aStepParam.isSetOverride()) {
               //Change the name of the variable in the resolvedStep 
               //Add this new variable name to the from document
               Random generator = new Random();
               int number = generator.nextInt();
               
           }
       }
   }*/
   
   public static void resolveAttributes(PipelineDocument pipelineDoc, Steps steps) throws PipelineEngineException{
       LinkedHashMap replace = new LinkedHashMap();
       Steps newSteps = Steps.Factory.newInstance();
       int initSizeOfStepArray = steps.sizeOfStepArray(); 
       for (int i = 0; i < initSizeOfStepArray; i++ ) {
           String pipeline_loopOn = null;
           Step aStep = steps.getStepArray(i);
           if (aStep.isSetPrecondition()) {
               pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),aStep.getPrecondition(),false);
               if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                   replace.put(new Integer(i),pipeline_loopOn);
               }//else {
                //   aStep.setPrecondition(StepPreConditionUtils.resolvePreCondition(pipelineDoc.getPipeline(),  aStep.getPrecondition()));
               //}
           }
           if (aStep.isSetWorkdirectory()) {
               pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),aStep.getWorkdirectory(),false);
               if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                   replace.put(new Integer(i),pipeline_loopOn);
               }else {
                   try {
                       ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(),  aStep.getWorkdirectory());
                       if (values != null && values.size() > 0) 
                           aStep.setWorkdirectory((String)values.get(0));
                       else
                           throw new PipelineEngineException("Couldnt resolve " + aStep.getWorkdirectory()+  " XPath Expression for step[" + aStep.getId() +"]" );
                   }catch(TransformerException te) {
                       throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
                   } 
               }
           }
           if (pipeline_loopOn == null) {
               //look for PIPELINE_LOOPON() within the arguments passed
               if (aStep.isSetPipelet()) {
                   PipeletData pipelet = aStep.getPipelet();
                   for (int j =0; j < pipelet.sizeOfParametersArray() ; j++) {
                       ParameterData pipeletParameter = pipelet.getParametersArray(j);
                       if (pipeletParameter.getValues().isSetUnique()) {
                           pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(), pipeletParameter.getValues().getUnique(), false);
                       }else {
                           for (int k = 0; k < pipeletParameter.getValues().sizeOfListArray(); k++) {
                               pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(), pipeletParameter.getValues().getListArray(k), false);
                               if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) break;
                           }
                       }
                       if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                           replace.put(new Integer(i),pipeline_loopOn);
                           break;
                       }
                   }
               }else {
                   for (int j = 0; j < aStep.sizeOfResourceArray(); j++) {
                       Resource aStepRsc = aStep.getResourceArray(j);
                       for (int k = 0; k < aStepRsc.sizeOfArgumentArray(); k++) {
                           for (int l = 0; l < aStepRsc.getArgumentArray(k).sizeOfValueArray(); l++) {
                               pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(), aStepRsc.getArgumentArray(k).getValueArray(l), false);
                               if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                                   break;
                               }
                           }
                           if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                               break;
                           }
                       }
                   }
                   if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                       replace.put(new Integer(i),pipeline_loopOn);
                   }
               }
               
           }
       } 
       for (int i = 0; i < initSizeOfStepArray; i++ ) {
           if (!replace.containsKey(new Integer(i))) {
               Step rStep = newSteps.addNewStep();
               copy(steps.getStepArray(i),rStep, null,null,-1);
           }else {
                   String pipeline_loopOn = (String)replace.get(new Integer(i));
                   Step aStep = steps.getStepArray(i);
                   ArrayList loopValues = XMLBeansUtils.getLoopValuesById(pipelineDoc.getPipeline(),LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),pipeline_loopOn,true));
                   if (loopValues != null && loopValues.size() > 0) {
                       for (int j = 0; j < loopValues.size(); j++) {
                           Step rStep = newSteps.addNewStep();
                           copy(aStep,rStep, pipeline_loopOn,(String)loopValues.get(j), j+1);
                           try {
                               if (rStep.isSetWorkdirectory()){
                                   ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(), rStep.getWorkdirectory());
                                   if (values != null && values.size() > 0) 
                                       rStep.setWorkdirectory((String)values.get(0));
                                   else
                                       throw new PipelineEngineException("Couldnt resolve " + rStep.getWorkdirectory()+  " XPath Expression for step[" + rStep.getId() +"]" );
                               }
                               if (rStep.isSetPrecondition()) {
                                   //rStep.setPrecondition(StepPreConditionUtils.resolvePreCondition(pipelineDoc.getPipeline(),  rStep.getPrecondition())); 
                                  //ArrayList values = XPathResolver.GetInstance().evaluate(pipelineDoc.getPipeline(), dom, rStep.getPrecondition());
                                  // if (values != null && values.size() > 0) 
                                  //     rStep.setPrecondition((String)values.get(0));
                                  // else
                                  //     throw new PipelineException("Couldnt resolve " + rStep.getPrecondition()+  " XPath Expression for step[" + rStep.getId() +"]" );
                               }
                           }catch(TransformerException te) {
                               throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
                           } 
                       }
                   }
           }
       }
       for (int i =0; i < newSteps.sizeOfStepArray(); i++) {
           Step rStep = newSteps.getStepArray(i);
           ArrayList newOutput = new ArrayList();
           for (int j = 0 ; j < rStep.sizeOfOutputArray(); j++) {
               String pipeline_loopOn = null;
               if (rStep.getOutputArray(j).getFile().isSetName())
                   pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),rStep.getOutputArray(j).getFile().getName(),false);
               if (pipeline_loopOn == null && rStep.getOutputArray(j).getFile().isSetPath()) {
                   pipeline_loopOn = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),rStep.getOutputArray(j).getFile().getPath().getStringValue(),false);
               }
               if (pipeline_loopOn != null && pipeline_loopOn.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                   ArrayList loopValues = XMLBeansUtils.getLoopValuesById(pipelineDoc.getPipeline(),LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),pipeline_loopOn,true));
                   if (loopValues != null && loopValues.size() > 0) {
                       newOutput.addAll(OutPutUtils.replaceFiles(rStep, pipeline_loopOn, loopValues, j));
                   }
               }else {
                   OutputData to = OutputData.Factory.newInstance();
                   OutPutUtils.copy(rStep.getOutputArray(j),to);
                   newOutput.add(to);
               }
           }
           
           if (newOutput.size() > 0) {
               OutputData[] newOuts = new OutputData[newOutput.size()];
               for (int io=0; io < newOutput.size(); io++) {
                   newOuts[io] = (OutputData)newOutput.get(io);
                  // newOutput.remove(io);
               }
               rStep.setOutputArray(newOuts);
              // newOutput = null;
           }
           for (int j = 0 ; j < rStep.sizeOfOutputArray(); j++) {
               try {
                   if (!rStep.getOutputArray(j).getFile().isSetName()) continue;
                   ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(),  rStep.getOutputArray(j).getFile().getName());
                   String path = null;
                   if (rStep.getOutputArray(j).getFile().isSetPath()) {
                       path = (String)(XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(),  rStep.getOutputArray(j).getFile().getPath().getStringValue()).get(0));
                       if (rStep.getOutputArray(j).getFile().getPath().getRelativePath()) {
                           path = rStep.getWorkdirectory() + File.separator + path;
                       }
                   }else if (rStep.isSetWorkdirectory()) {
                       path = rStep.getWorkdirectory();
                   }
                   if (path == null) throw new PipelineEngineException("For the step " + rStep.getId() + " and output file " + j + " neither the path nor the working directory is set");    
                   if (values != null && values.size() > 0) {
                       rStep.getOutputArray(j).getFile().setName((String)values.get(0));
                       if (rStep.getOutputArray(j).getFile().isSetPath()) {
                           rStep.getOutputArray(j).getFile().getPath().setRelativePath(false);
                           rStep.getOutputArray(j).getFile().getPath().setStringValue(path);
                       }else {
                           rStep.getOutputArray(j).getFile().addNewPath().setStringValue(path);
                       }
                   }
                   else
                       throw new PipelineEngineException("Couldnt resolve " + rStep.getWorkdirectory()+  " XPath Expression for step[" + rStep.getId() +"]" );
               }catch(TransformerException te) {
                   throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
               } 
           }
       }
       pipelineDoc.getPipeline().setSteps(newSteps);
       //try {
       //    pipelineDoc.save(new File("intermediate.xml"),new XmlOptions().setSavePrettyPrint().setUseDefaultNamespace());
       //}catch(Exception e){
       //    throw new PipelineException("Unable to save intermediate pipeline document ",e);
      // }
   }
    
   
   /*private static void   addFiles(ArrayList newOutputs, Step aStep, ArrayList fileNames, ArrayList path, OutputData anOutput) {
       for (int j = 0; j < fileNames.size(); j++) {
           OutputData newOut = OutputData.Factory.newInstance();
           if (anOutput.isSetXsiType()) 
               newOut.setXsiType(anOutput.getXsiType());
           newOut.setName((String)fileNames.get(j));
           Path newPath = newOut.addNewPath();
           if (anOutput.getPath().getRelativePath())
               newPath.setStringValue(aStep.getWorkdirectory() + File.separator + path);
           else 
               newPath.setStringValue(path);
           if (file.isSetFormat())
               newFile.setFormat(file.getFormat());
           if (file.isSetDescription())
               newFile.setDescription(file.getDescription());
           if (file.isSetContent())
               newFile.setContent(file.getContent());
           if (file.isSetDimensions())
               newFile.setDimensions(file.getDimensions());
           if (file.isSetVoxelRes())
               newFile.setVoxelRes(file.getVoxelRes());
           if (file.isSetOrientation())
               newFile.setOrientation(file.getOrientation());
           if (file.isSetFileCount())
               newFile.setFileCount(file.getFileCount());
           if (file.isSetPattern()) {
               ArrayList pattern = XPathResolverSaxon.GetInstance().resolveXPathExpressions(StringUtils.transformOutputExpression(file.getPath().getStringValue()),rscDoc);
               String commaSeperated = "";
               if (pattern.size() > 1) {
                   for (int k = 0; k < pattern.size(); k++) {
                       commaSeperated += (String)pattern.get(k) + " ,";
                   }
                   if (commaSeperated.endsWith(","))
                       commaSeperated = commaSeperated.substring(0, commaSeperated.length()-1);
               }else 
                   newFile.setPattern((String)pattern.get(0));
           }    
       }
   }*/
  
   
   public static void resolveAttributes(PipelineData pipelineData,  Step step) throws PipelineEngineException {
       //if (step.isSetPrecondition()) {
        //   step.setPrecondition(StepPreConditionUtils.resolvePreCondition(pipelineData,  step.getPrecondition()));
      // }
       if (step.isSetWorkdirectory()) {
           try {
               ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineData,  step.getWorkdirectory());
               if (values != null && values.size() > 0) 
                   step.setWorkdirectory((String)values.get(0));
               else
                   throw new PipelineEngineException("Couldnt resolve " + step.getWorkdirectory()+  " XPath Expression for step[" + step.getId() +"]" );
           }catch(TransformerException te) {
               throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
           }
       }
   }

   public static void resolveAttributes(PipelineDocument pipelineDoc) throws PipelineEngineException{
       resolveAttributes(pipelineDoc, pipelineDoc.getPipeline().getSteps());        
   }
    
}
