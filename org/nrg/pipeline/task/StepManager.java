/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.ArgumentNotFoundException;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.exception.PreConditionNotSatisfiedException;
import org.nrg.pipeline.manager.ExecutionManager;
import org.nrg.pipeline.manager.LaunchManager;
import org.nrg.pipeline.manager.ResourceManager;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.ConditionUtils;
import org.nrg.pipeline.utils.FileUtils;
import org.nrg.pipeline.utils.LoopUtils;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.OutPutUtils;
import org.nrg.pipeline.utils.ParameterUtils;
import org.nrg.pipeline.utils.PipelineUtils;
import org.nrg.pipeline.utils.ResourceUtils;
import org.nrg.pipeline.utils.StepPreConditionUtils;
import org.nrg.pipeline.utils.StepUtils;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.OutputData;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ResourceDocument;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument.AllResolvedSteps;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step.Resource;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step.Resource.Argument;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: StepManager.java,v 1.2 2009/11/11 21:03:41 mohanar Exp $
 @since Pipeline 1.0
 */

public class StepManager {
    PipelineDocument pipelineDoc;
    LinkedHashMap stepLink;
    static Logger logger = Logger.getLogger(StepManager.class);
    String startAtStepId = null;
    boolean debug = false;
    AllResolvedStepsDocument masterResolvedStepsDocument;
    private boolean primaryPipeline;
    int startAt;
    
    public StepManager(PipelineDocument pDoc, String startStepId, boolean isPrimary) {
        pipelineDoc = pDoc;
        stepLink = new LinkedHashMap();
        if (startStepId != null) startAtStepId = startStepId;
        primaryPipeline = isPrimary;
        startAt = 0;
    }
    
    
    
    
    public AllResolvedStepsDocument execute() throws PipelineException, PreConditionNotSatisfiedException, ArgumentNotFoundException, TransformerException {
        setResolvedStepsDocument();
        AllResolvedStepsDocument rtn = AllResolvedStepsDocument.Factory.newInstance();
        AllResolvedSteps rtnAllStep = rtn.addNewAllResolvedSteps();
        if (pipelineDoc.getPipeline().isSetOutputFileNamePrefix())
            rtn.getAllResolvedSteps().setOutputFileNamePrefix(pipelineDoc.getPipeline().getOutputFileNamePrefix());
        rtnAllStep.setName(pipelineDoc.getPipeline().getName());
        rtnAllStep.setLocation(pipelineDoc.getPipeline().getLocation());
        ParameterUtils.copyParameters(pipelineDoc,rtnAllStep);
        boolean isLast = true;
        Steps steps = pipelineDoc.getPipeline().getSteps();
        String savedfile = PipelineUtils.getResolvedPipelineXmlName(rtn);
        int stepIndex = 0;
        for (int i = startAt ; i < steps.sizeOfStepArray();) {
            int nextIndex = i+1;
            Step aStep = steps.getStepArray(i);
            isLast = ((i==steps.sizeOfStepArray()-1) && primaryPipeline);
            if (stepLink.containsKey(aStep)) { //Checked for startAt
                stepIndex++;
                Boolean condition = new Boolean(true);
                String preCondition ="";
                if (aStep.isSetPrecondition()) {
                    preCondition = StepPreConditionUtils.resolvePreCondition(pipelineDoc.getPipeline(), aStep.getPrecondition());
                    if (preCondition.contains(PipelineConstants.PIPELINE_XPATH_MARKER)) 
                        throw new PreConditionNotSatisfiedException("Couldnt resolve xpath in " + preCondition);
                    condition = ConditionUtils.checkCondition(pipelineDoc.getPipeline(),preCondition,debug);
                    if (!condition.booleanValue()) {
                        logger.info("Step " + aStep.getId() + " was SKIPPED (" + preCondition +")");
                        ResolvedStep rStep = rtnAllStep.addNewResolvedStep();
                        rStep.setStatus(ResolvedStep.Status.SKIPPED);
                        rStep.setId(aStep.getId()); rStep.setDescription(aStep.getDescription());
                        rStep.setPrecondition(preCondition);
                        if (aStep.isSetGotoStepId()) rStep.setGotoStepId(aStep.getGotoStepId());
                        if (aStep.isSetWorkdirectory())rStep.setWorkdirectory(aStep.getWorkdirectory());
                    }
                }
                if (condition.booleanValue()) {
                    String nextIndexStr = "";
                    if (aStep.isSetGotoStepId()) {
                        if (!StepUtils.getStepId(aStep).equals(StepUtils.getStepId( steps.getStepArray(nextIndex)))) { //Not continuation of current step
                            ArrayList newIndices = XMLBeansUtils.getStepIndicesById(pipelineDoc.getPipeline(),aStep.getGotoStepId());
                            if (newIndices == null || !(newIndices.size() > 0))
                                throw new PipelineException("Step " + aStep.getId().substring(0,aStep.getId().indexOf(":")) + " seems to have an invalid gotoStepId = " + aStep.getGotoStepId());
                            nextIndex = ((Integer)newIndices.get(0)).intValue();
                            try {
                                if (!StepUtils.getStepId( steps.getStepArray(nextIndex)).equals(StepUtils.getStepId(aStep))) 
                                    nextIndexStr = StepUtils.getStepId( steps.getStepArray(nextIndex));
                            }catch(Exception e) {}
                        }
                    }
                    if (aStep.isSetPipelet()) {
                            String pathToPipelineDoc = FileUtils.getAbsolutePath(aStep.getPipelet().getLocation(), aStep.getPipelet().getName());
                            PipelineDocument pipeletPipelineDoc = PipelineUtils.getPipelineDocument(pathToPipelineDoc);
                            PipelineUtils.checkStepIds(pipeletPipelineDoc);
                            if (pipelineDoc.getPipeline().isSetOutputFileNamePrefix()) 
                                pipeletPipelineDoc.getPipeline().setOutputFileNamePrefix(pipelineDoc.getPipeline().getOutputFileNamePrefix());
                            pipeletPipelineDoc.getPipeline().setName(pipelineDoc.getPipeline().getName());
                            if (pipeletPipelineDoc != null) {
                                //if (pipeletPipelineDoc.getPipeline().isSetOutputFileNamePrefix())
                                //    pipeletPipelineDoc.getPipeline().setOutputFileNamePrefix(pipelineDoc.getPipeline().getOutputFileNamePrefix());
                                LoopUtils.copyLoops(pipelineDoc,pipeletPipelineDoc);
                                ParameterUtils.copyParameters(pipelineDoc,pipeletPipelineDoc);
                                ParameterUtils.copyParameters(aStep, pipelineDoc, pipeletPipelineDoc);
                            }
                            PipelineUtils.resolveXPath(pipeletPipelineDoc);
                            for (int j = 0; j < pipeletPipelineDoc.getPipeline().getSteps().getStepArray().length; j++) {
                            	Step pipeletStep = pipeletPipelineDoc.getPipeline().getSteps().getStepArray(j);
                            	pipeletStep.setId(aStep.getId() + "++" + pipeletStep.getId());
                                if (pipeletStep.isSetGotoStepId()) pipeletStep.setGotoStepId(aStep.getId() + "++"+pipeletStep.getGotoStepId());
                            }
                            StepManager stepManager = new StepManager(pipeletPipelineDoc,null, false);
                            AllResolvedStepsDocument stepDoc = stepManager.execute();
                            for (int j = 0; j < stepDoc.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                                ResolvedStep rStep = rtnAllStep.addNewResolvedStep();
                                StepUtils.copy(stepDoc.getAllResolvedSteps().getResolvedStepArray(j), rStep);
                                if (aStep.isSetPrecondition()) rStep.setPrecondition(preCondition);
                            }
                     }else {
                            AllResolvedStepsDocument stepDoc = (AllResolvedStepsDocument)stepLink.get(aStep);
                            try {
                                if (primaryPipeline) {
                                    LaunchManager.registerStep(stepIndex);
                                }
                                ExecutionManager executionManager = new ExecutionManager(PipelineUtils.getPathToOutputFile(pipelineDoc,"OUTPUT"),PipelineUtils.getPathToOutputFile(pipelineDoc,"ERROR"));
                                executionManager.execute(stepDoc,nextIndexStr,  debug);
                                for (int j = 0; j < stepDoc.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                                    ResolvedStep rStep = rtnAllStep.addNewResolvedStep();
                                    StepUtils.copy(stepDoc.getAllResolvedSteps().getResolvedStepArray(j), rStep);
                                    if (aStep.isSetPrecondition()) rStep.setPrecondition(preCondition);
                                }
                            }catch(Exception e) {
                                isLast = false;
                                for (int j = 0; j < stepDoc.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                                    ResolvedStep rStep = rtnAllStep.addNewResolvedStep();
                                    StepUtils.copy(stepDoc.getAllResolvedSteps().getResolvedStepArray(j), rStep);
                                    if (aStep.isSetPrecondition()) rStep.setPrecondition(preCondition);
                                }
                                try{
                                    if (rtn != null) {
                                        File saveDoc = new File(savedfile);
                                        FileUtils.saveFile(saveDoc,rtn);
                                    }
                                }catch(Exception e1){
                                    logger.debug("Unable to save pipeline document " + savedfile,e1);
                                }
                                if (e instanceof PipelineException) {
                                	throw (PipelineException)e;
                                }else 
                                    throw new PipelineException("Unable to execute pipeline " ,e);

                            }
                        }
                    }
                    // if (primaryPipeline) {
                        try{
                            if (rtn != null) {
                                File saveDoc = new File(savedfile);
                                FileUtils.saveFile(saveDoc,rtn);
                            }
                        }catch(Exception e1){
                            logger.debug("Unable to save pipeline document " + savedfile,e1);
                        }
                     //}
                }
                if (aStep.isSetAwaitApprovalToProceed()) {
                    if (!primaryPipeline) {
                        throw new PipelineException("Await Approval to proceed set possibily on a pipelet for step " + aStep.getId());
                    }else {
                        //Look ahead to see if the next Step is expansion (due to PIPELINE_LOOPON) of the existing step or is a new step
                        if (i==steps.sizeOfStepArray()-1) { //is the last step 
                            break;
                        }else  {
                            if (StepUtils.getStepId(aStep).equals(StepUtils.getStepId( steps.getStepArray(nextIndex)))) {
                                i=nextIndex;
                                continue;
                            }else {
                                isLast = false; 
                                Notification notification = LaunchManager.getNotification();
                                notification.setCurrentStep(StepUtils.getStepId(aStep));
                                notification.setMessage(aStep.getDescription());
                                try {
                                    notification.setNextStep(StepUtils.getStepId( steps.getStepArray(i+1)));
                                }catch(ArrayIndexOutOfBoundsException aiobe) {
                                    
                                }
                                notification.setStatus("Awaiting Action");
                                AdminUtils.fireNotification(notification);
                                break;
                            }
                        }
                   }
              }
            i = nextIndex;
        }
        if (isLast) {
            Notification notification = LaunchManager.getNotification();
            notification.setCurrentStepIndex(LaunchManager.getTotalSteps());
            notification.setStepTimeLaunched(AdminUtils.getTimeLaunched());
            notification.setStatus("Complete");
            AdminUtils.fireNotification(notification);
        }
        return rtn;
    }
    
    
    private boolean isPrimaryPipeline() {
        return (primaryPipeline==true);
    }
    
    private void setResolvedStepsDocument() throws  PipelineException, ArgumentNotFoundException, PreConditionNotSatisfiedException , TransformerException{
        PipelineData pipelineData = pipelineDoc.getPipeline();
        if (pipelineData == null) return;
        StepUtils.resolveAttributes(pipelineDoc,pipelineData.getSteps());
        Steps steps = pipelineData.getSteps();
        if (startAtStepId != null) {
            startAt = XMLBeansUtils.getStepIndexById(pipelineData,startAtStepId );
            if (startAt == -1) {
                throw new PipelineException("Couldnt find Step with Id = " + startAtStepId);
            }
        }
        LaunchManager.setTotalSteps(steps.sizeOfStepArray() - startAt);
        if (isPrimaryPipeline()) LaunchManager.setPathToPipelineDescriptor(pipelineData.getLocation() + File.separator + pipelineData.getName());
        for (int i = startAt; i < steps.sizeOfStepArray();i++) {
            Step aStep = steps.getStepArray(i);
            AllResolvedStepsDocument allStepsDoc = AllResolvedStepsDocument.Factory.newInstance();
            if (!aStep.isSetPipelet()) {
                allStepsDoc = AllResolvedStepsDocument.Factory.newInstance();
                AllResolvedSteps allStep = allStepsDoc.addNewAllResolvedSteps();
                ParameterUtils.copyParameters(pipelineDoc,allStep);
                int noOfSteps = allStep.sizeOfResolvedStepArray();
                allStepsDoc.getAllResolvedSteps().setName(pipelineData.getName());
                allStepsDoc.getAllResolvedSteps().setLocation(pipelineData.getLocation());
                if (pipelineDoc.getPipeline().isSetOutputFileNamePrefix())
                    allStepsDoc.getAllResolvedSteps().setOutputFileNamePrefix(pipelineDoc.getPipeline().getOutputFileNamePrefix());
                logger.info("Step attributes resolved");
                if (aStep.sizeOfResourceArray() > 0) {
                    try {
                        AllResolvedStepsDocument resolvedDoc  = getInternalRepresentation(pipelineDoc, aStep);
                        for (int j = 0; j < resolvedDoc.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                            ResolvedStep rStep = allStep.insertNewResolvedStep(noOfSteps++);
                            StepUtils.copy(resolvedDoc.getAllResolvedSteps().getResolvedStepArray(j), rStep);
                        }
                    }catch(TransformerException te) {
                        throw new PipelineException("Couldnt construct internal representation. Encountered ",te);
                    }
                }
            }
            stepLink.put(aStep,allStepsDoc);
        }
    }
    
    private AllResolvedStepsDocument getInternalRepresentation(PipelineDocument pipelineDoc, Step aStep) throws ArgumentNotFoundException, PipelineException, TransformerException {
        AllResolvedStepsDocument allStepsDoc = AllResolvedStepsDocument.Factory.newInstance();
        AllResolvedSteps allStep = allStepsDoc.addNewAllResolvedSteps();
        ResolvedStep rStep = allStep.addNewResolvedStep();
        rStep.setId(aStep.getId());
        rStep.setStatus(ResolvedStep.Status.AWAITING_ACTION);
        if (aStep.isSetAwaitApprovalToProceed()) {
           rStep.setAwaitApprovalToProceed(aStep.getAwaitApprovalToProceed());
        }
        if (aStep.isSetContinueOnFailure()) {
            rStep.setContinueOnFailure(aStep.getContinueOnFailure());
        }
        if (aStep.isSetGotoStepId()) {
            rStep.setGotoStepId(aStep.getGotoStepId());
        }
        /*if (aStep.isSetSupressOutputFiles() && aStep.getSupressOutputFiles())
            rStep.setSupressOutputFiles(aStep.getSupressOutputFiles());
        else if (!aStep.isSetSupressOutputFiles())
            rStep.setSupressOutputFiles(true); */
        if (aStep.isSetPrecondition())rStep.setPrecondition(aStep.getPrecondition());
        rStep.setDescription(aStep.getDescription()); 
        if (aStep.isSetWorkdirectory())rStep.setWorkdirectory(aStep.getWorkdirectory());
        if (aStep.isSetPreconditionType()) {
            if(aStep.getPreconditionType().toString().equals("strict")) {
                rStep.setPreconditionType(ResolvedStep.PreconditionType.STRICT);
            }else if (aStep.getPreconditionType().toString().equals("relaxed")) {
                rStep.setPreconditionType(ResolvedStep.PreconditionType.RELAXED);
            }
        }
        if (aStep.sizeOfOutputArray() > 0) {
            for (int i = 0; i < aStep.sizeOfOutputArray(); i++) {
             OutputData rOut = rStep.addNewResolvedOutput();
             OutPutUtils.copy(aStep.getOutputArray(i), rOut);
            }
        }
        Resource[] resourceArray = aStep.getResourceArray();
        if (resourceArray != null) {
            for (int j = 0; j < resourceArray.length; j++) {
                Resource aResource = aStep.getResourceArray(j);
                ResourceDocument resourceDocument = ResourceManager.GetInstance().getResource(aResource);
                if (resourceDocument == null) {
                    throw new PipelineException("Couldnt not find " + aResource.getLocation() + File.separator + aResource.getName());
                }
                resourceDocument.getResource().setDescription("");
                ResourceData rscData;
                try { rscData = resourceDocument.getResource(); } 
                catch(NullPointerException ne ) {
                    logger.info("Possibly resource " + FileUtils.getAbsolutePath(aResource.getLocation(),aResource.getName()) + " at Step[" + aStep.getId() + "] doesnt exist");
                    throw new PipelineException("Possibly resource " + FileUtils.getAbsolutePath(aResource.getLocation(),aResource.getName()) + " at Step[" + aStep.getId() + "] doesnt exist", ne);
                }
                ResolvedResource internalResourceData = ResourceUtils.getInternalResourceData(resourceDocument);
                int noOfArgumentsToSet = aResource.sizeOfArgumentArray();
                Hashtable argumentsWhichHavePipelineLoopOn = new Hashtable();
                for (int k = 0; k < noOfArgumentsToSet; k++) {
                    Argument stepArgument = aResource.getArgumentArray(k);
                    String stepArgumentId = stepArgument.getId();
                    int argumentIndex = XMLBeansUtils.getArgumentIndexById(rscData,stepArgumentId);
                    if (argumentIndex == -1) {
                        logger.info("getInternalRepresentation():: Couldnt find argument " + stepArgumentId + " in step " + aStep.getId() + " Resource " + aResource.getLocation() + File.separator + aResource.getName());
                        throw new ArgumentNotFoundException( stepArgumentId + " in Step " + aStep.getId() + " at Resource " + aResource.getLocation() + File.separator + aResource.getName());
                    }
                    ResourceData.Input.Argument argumentData = rscData.getInput().getArgumentArray(argumentIndex);
                    String[] stepArgumentValues = stepArgument.getValueArray();
                    if (stepArgument.sizeOfValueArray() == 0) {
                        if(!ResourceUtils.addArgumentToInternalResource(internalResourceData,argumentData,null)) {
                            logger.info(" ResourceUtils.addArgumentToInternalResource():: Coudlnt add Argument " + argumentData.getId() + " to Internal Resource " + rscData.getLocation() + File.separator + rscData.getName() + " at step " + aStep.getId());
                            throw new PipelineException("Coudlnt add Argument " + argumentData.getId() + " to Internal Resource " + FileUtils.getAbsolutePath(rscData.getLocation(),rscData.getName()) + " at step " + aStep.getId());
                        } 
                        continue;    
                    }
                    for (int l = 0; l < stepArgumentValues.length; l++) {
                        String pipeline_loop = LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),stepArgumentValues[l],false);
                        if (pipeline_loop == null) 
                            pipeline_loop = LoopUtils.getLoopValueId(pipelineDoc.getPipeline(),stepArgumentValues[l],false);
                        if (pipeline_loop != null && pipeline_loop.startsWith(PipelineConstants.PIPELINE_LOOPON)) {
                            //this argument makes it necessary to launch a new resource
                            if (argumentsWhichHavePipelineLoopOn.containsKey(pipeline_loop)) {
                                LinkedHashMap argValues = (LinkedHashMap)argumentsWhichHavePipelineLoopOn.get(pipeline_loop);
                                if (argValues.containsKey(new Integer(argumentIndex))) {
                                    ((ArrayList)argValues.get(new Integer(argumentIndex))).add(stepArgumentValues[l]);
                                }else {
                                    ArrayList values = new ArrayList(); values.add(stepArgumentValues[l]);
                                    argValues.put(new Integer(argumentIndex),values);
                                }
                            } else {
                                ArrayList values = new ArrayList(); values.add(stepArgumentValues[l]);
                                LinkedHashMap argValues = new LinkedHashMap(); argValues.put(new Integer(argumentIndex),values);
                                argumentsWhichHavePipelineLoopOn.put(pipeline_loop,argValues);
                            }
                            continue;
                        }
                        ArrayList resolvedValues = null;
                        try {
                            resolvedValues = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(),stepArgumentValues[l]);
                        }catch(TransformerException te) {
                            logger.info("getInternalRepresentation()::" + te.getLocalizedMessage() + " while parsing " + stepArgumentValues[l], te.getCause() );
                            throw new PipelineException("Parsing " + stepArgumentValues[l] + te.getClass() + " ==> " + te.getLocalizedMessage(),te);
                        }
                        if (resolvedValues != null && resolvedValues.size() > 0) {
                            for (int m = 0; m < resolvedValues.size(); m++) {
                                if(!ResourceUtils.addArgumentToInternalResource(internalResourceData,argumentData,(String)resolvedValues.get(m))) {
                                    logger.info(" ResourceUtils.addArgumentToInternalResource():: Coudlnt add Argument " + argumentData.getId() + " to Internal Resource " + rscData.getLocation() + File.separator + rscData.getName() + " at step " + aStep.getId());
                                    throw new PipelineException("Coudlnt add Argument " + argumentData.getId() + " to Internal Resource " + FileUtils.getAbsolutePath(rscData.getLocation(),rscData.getName()) + " at step " + aStep.getId());
                                } 
                            }
                        }else {
                            if(!ResourceUtils.addArgumentToInternalResource(internalResourceData,argumentData,stepArgumentValues[l])) {
                                logger.info(" ResourceUtils.addArgumentToInternalResource():: Coudlnt add Argument " + argumentData.getId() + " to Internal Resource " + rscData.getLocation() + File.separator + rscData.getName() + " at step " + aStep.getId());
                                throw new PipelineException("Coudlnt add Argument " + argumentData.getId() + " to Internal Resource " + FileUtils.getAbsolutePath(rscData.getLocation(),rscData.getName()) + " at step " + aStep.getId());
                            } 
                            logger.debug("Couldnt resolve xpath expression on base document. Will try on the internal rep " + stepArgumentValues[l]);
                        }
                    }
                }
                //At this stage all the arguments that do not contain pipeline_loopOn have been added
                //Iterate through the arguments that do contain the pipeline_loopOn and create
                //New ResourceData children in the internalRepresentation
                
                 ResourceUtils.createResourcesForArguments(pipelineDoc, rStep, internalResourceData,rscData, argumentsWhichHavePipelineLoopOn, aResource, aStep);
            }
          
        }
    return allStepsDoc;
  }
    
}
