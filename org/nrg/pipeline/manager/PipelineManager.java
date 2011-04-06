/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.manager;

import java.io.File;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.ArgumentNotFoundException;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.exception.PreConditionNotSatisfiedException;
import org.nrg.pipeline.task.StepManager;
import org.nrg.pipeline.utils.FileUtils;
import org.nrg.pipeline.utils.LoopUtils;
import org.nrg.pipeline.utils.ParameterUtils;
import org.nrg.pipeline.utils.PipelineProperties;
import org.nrg.pipeline.utils.PipelineUtils;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.PipelineData.Parameters;


//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PipelineManager.java,v 1.2 2009/11/11 21:03:41 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineManager {

    private PipelineManager() {
        
    }
    
    public static PipelineManager GetInstance(String pathToConfigFile) {
        if (self == null) {
           self = new PipelineManager(); 
        }
        if (pathToConfigFile != null) {
            try {
                PipelineProperties.init(pathToConfigFile);
            }catch(Exception e) {
                logger.error(e);
                System.exit(1);
            }
        }else {
            logger.debug("Config file for the Pipeline Engine not specified");
        }
        return self;
    }
    
/*     public AllResolvedStepsDocument getResolvedStepsDocument(Step aStep, PipelineDocument callerPipeline, boolean debug) throws  PipelineException, ArgumentNotFoundException, PreConditionNotSatisfiedException, TransformerException {
        String pathToPipelineDoc = FileUtils.getAbsolutePath(aStep.getPipelet().getLocation(), aStep.getPipelet().getName());
        PipelineDocument pipelineDoc = PipelineUtils.getPipelineDocument(pathToPipelineDoc);
        PipelineUtils.checkStepIds(pipelineDoc);
        System.out.println("Came to see pipelet " + pathToPipelineDoc);
        if (callerPipeline != null) {
            if (callerPipeline.getPipeline().isSetOutputFileNamePrefix())
                pipelineDoc.getPipeline().setOutputFileNamePrefix(callerPipeline.getPipeline().getOutputFileNamePrefix());
            LoopUtils.copyLoops(callerPipeline, pipelineDoc);
            ParameterUtils.copyParameters(callerPipeline, pipelineDoc);
            ParameterUtils.copyParameters(aStep, callerPipeline, pipelineDoc);
        }
        PipelineUtils.resolveXPath(pipelineDoc);
        logger.info("Pipeline " + pathToPipelineDoc + " successfully launched");
        AllResolvedStepsDocument rtn =  getResolvedStepsDocument(pipelineDoc,null, debug);
        //If the step had a precondition and this precondition to the pipelets set of conditions
        if (aStep.isSetPrecondition()) {
            AllResolvedSteps resolvedSteps = rtn.getAllResolvedSteps();
            for (int i = 0; i < resolvedSteps.sizeOfResolvedStepArray(); i++) {
                ResolvedStep aResolvedStep = resolvedSteps.getResolvedStepArray(i);
                if (aStep.isSetPrecondition()) {
                    String newPrecondition = aStep.getPrecondition();
                    if (aResolvedStep.isSetPrecondition()) newPrecondition += " and " + aResolvedStep.getPrecondition();
                    newPrecondition = StringUtils.replace(newPrecondition,PipelineConstants.PIPELINE_XPATH_MARKER+" and "+PipelineConstants.PIPELINE_XPATH_MARKER," and ");
                    aResolvedStep.setPrecondition(newPrecondition);
                }
            }
        }
        System.out.println("*************************Came to see pipelet " + pathToPipelineDoc);
        return rtn;
    }
    
    public AllResolvedStepsDocument getResolvedStepsDocument(PipelineDocument pipelineDoc, String startAtStepId, boolean debug) throws  PipelineException, ArgumentNotFoundException, PreConditionNotSatisfiedException , TransformerException{
        AllResolvedStepsDocument allStepsDoc = AllResolvedStepsDocument.Factory.newInstance();
        AllResolvedSteps allStep = allStepsDoc.addNewAllResolvedSteps();
        ParameterUtils.copyParameters(pipelineDoc,allStep);
        int noOfSteps = allStep.sizeOfResolvedStepArray();
        PipelineData pipelineData = pipelineDoc.getPipeline();
        if (pipelineData == null) return null;
        allStepsDoc.getAllResolvedSteps().setName(pipelineData.getName());
        allStepsDoc.getAllResolvedSteps().setLocation(pipelineData.getLocation());
        StepUtils.resolveAttributes(pipelineDoc,pipelineData.getSteps());
        if (pipelineDoc.getPipeline().isSetOutputFileNamePrefix())
            allStepsDoc.getAllResolvedSteps().setOutputFileNamePrefix(pipelineDoc.getPipeline().getOutputFileNamePrefix());
        logger.info("Step attributes resolved");
        Steps steps = pipelineData.getSteps();
        int startAt = 0;
        if (startAtStepId != null) {
            startAt = XMLBeansUtils.getStepIndexById(pipelineData,startAtStepId );
            if (startAt == -1) {
                throw new PipelineException("Couldnt find Step with Id = " + startAtStepId);
            }
        }
        for (int i = startAt; i < steps.sizeOfStepArray();) {
            Step aStep = steps.getStepArray(i);
            if (aStep.isSetPrecondition()) {
                if (aStep.getPrecondition().contains(PipelineConstants.PIPELINE_XPATH_MARKER)) 
                    throw new PreConditionNotSatisfiedException("Couldnt resolve xpath in " + aStep.getPrecondition());
                condition = ConditionUtils.checkCondition(pipelineData,aStep.getPrecondition(),debug);
                if (!(condition.booleanValue())) {
                    if (aStep.isSetPreconditionType()) {
                        if (aStep.getPreconditionType().intValue() == Step.PreconditionType.INT_STRICT) {
                            logger.info("Condition " + aStep.getPrecondition() + " for Step[ " + aStep.getId()+ " ] is not satisfied ");
                            //throw new PipelineException("Condition " + aStep.getPrecondition() + " for Step[" + aStep.getId() + "] is not satisfied ....");
                            return allStepsDoc; 
                        }
                    }
                }
            }
            if (aStep.isSetPipelet()) {
                AllResolvedStepsDocument resolvedPipelet = getResolvedStepsDocument(aStep, pipelineDoc, debug);
                try {
                    resolvedPipelet.save(new File("savedFile.xml"),new XmlOptions().setSavePrettyPrint().setUseDefaultNamespace());
                }catch(Exception e) {} 
                for (int j = 0; j < resolvedPipelet.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                    int loc = noOfSteps++;
                    allStep.insertNewResolvedStep(loc);
                    //rStep.setId(aStep.getId());
                    allStep.setResolvedStepArray(loc,XMLBeansUtils.copy(resolvedPipelet.getAllResolvedSteps().getResolvedStepArray(j)));
                    allStep.getResolvedStepArray(loc).setId(aStep.getId() + "::" + allStep.getResolvedStepArray(loc).getId());
                    
                    if (allStep.getResolvedStepArray(loc).isSetContinueOnFailure()) {
                        
                    }
                    //StepUtils.copy(resolvedPipelet.getAllResolvedSteps().getResolvedStepArray(j),rStep, null,null);
                }
                
            }else {
                if (aStep.sizeOfResourceArray() > 0) {
                    try {
                        AllResolvedStepsDocument resolvedDoc  = self.getInternalRepresentation(pipelineDoc, aStep);
                        for (int j = 0; j < resolvedDoc.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                            ResolvedStep rStep = allStep.insertNewResolvedStep(noOfSteps++);
                            StepUtils.copy(resolvedDoc.getAllResolvedSteps().getResolvedStepArray(j), rStep);
                        }
                    }catch(TransformerException te) {
                        throw new PipelineException("Couldnt construct internal representation. Encountered ",te);
                    }
                }
            }
            if (aStep.isSetGotoStepId() && condition.booleanValue()) {
                ArrayList newIndices = XMLBeansUtils.getStepIndicesById(pipelineData,aStep.getGotoStepId());
                if (newIndices == null || !(newIndices.size() > 0))
                    throw new PipelineException("Step " + aStep.getId().substring(0,aStep.getId().indexOf(":")) + " seems to have an invalid gotoStepId = " + aStep.getGotoStepId());
                for (int k = 0; k < newIndices.size(); k++) {
                    try {
                        AllResolvedStepsDocument resolvedDoc  = self.getInternalRepresentation(pipelineDoc, steps.getStepArray(((Integer)newIndices.get(k)).intValue()));
                        for (int j = 0; j < resolvedDoc.getAllResolvedSteps().sizeOfResolvedStepArray(); j++) {
                            ResolvedStep rStep = allStep.insertNewResolvedStep(noOfSteps++);
                            StepUtils.copy(resolvedDoc.getAllResolvedSteps().getResolvedStepArray(j), rStep);
                        }
                    }catch(TransformerException te) {
                        throw new PipelineException("Couldnt construct internal representation. Encountered " + te.getLocalizedMessage(),te);
                    }
                }
                i = ((Integer)newIndices.get(newIndices.size()-1)).intValue() + 1;
            }else 
                i++;
            if (aStep.isSetGotoStepId()) {
                ArrayList newIndices = XMLBeansUtils.getStepIndicesById(pipelineData,aStep.getGotoStepId());
                if (newIndices == null || !(newIndices.size() > 0))
                    throw new PipelineException("Step " + aStep.getId().substring(0,aStep.getId().indexOf(":")) + " seems to have an invalid gotoStepId = " + aStep.getGotoStepId());
            }
            i++;
        }
        return allStepsDoc;
    }
*/    
    public Parameters launchPipeline(String pathToPipelineXml, ParametersDocument parameterDoc, String startAtStepId, boolean debug) throws PipelineException, ArgumentNotFoundException, PreConditionNotSatisfiedException, TransformerException {
        PipelineDocument pipelineDoc = PipelineUtils.getPipelineDocument(pathToPipelineXml);
        ParameterUtils.addParameter(pipelineDoc, parameterDoc);
        launchPipeline(pipelineDoc, startAtStepId, debug);
        return pipelineDoc.getPipeline().getParameters();
    }
    
    
    public Parameters launchPipeline(String pathToPipelineXml, String parameterFile, String startAtStepId, boolean debug) throws PipelineException, ArgumentNotFoundException, PreConditionNotSatisfiedException, TransformerException {
        PipelineDocument pipelineDoc = PipelineUtils.getPipelineDocument(pathToPipelineXml);
        if (parameterFile != null) {
            ParameterUtils.addParameter(pipelineDoc, parameterFile);
        }
        launchPipeline(pipelineDoc, startAtStepId, debug);
        return pipelineDoc.getPipeline().getParameters();
    }
    
    
    private void launchPipeline(PipelineDocument pipelineDoc, String startAtStepId, boolean debug) throws PipelineException, PreConditionNotSatisfiedException, ArgumentNotFoundException, TransformerException {
        PipelineUtils.checkStepIds(pipelineDoc);
        PipelineUtils.resolveXPath(pipelineDoc);
        try {
            StepManager stepManager = new StepManager(pipelineDoc, startAtStepId, true);
            AllResolvedStepsDocument internal_repDoc = stepManager.execute();
            String savedFile = PipelineUtils.getResolvedPipelineXmlName(internal_repDoc);
            try{
                if (internal_repDoc != null) {
                    File saveDoc = new File(savedFile);
                    FileUtils.saveFile(saveDoc,internal_repDoc);
                }
            }catch(Exception e1){
                throw new PipelineException("Unable to save pipeline document " + savedFile,e1);
            }

        }catch(Exception e){
            e.printStackTrace();
            PipelineException pe1 = new PipelineException("Unable to complete pipeline " + e.getLocalizedMessage());
            if (e instanceof PipelineException){
            	PipelineException p = (PipelineException)e;
	            pe1.setErrorFileName(p.getOutputFileName());
	            pe1.setOutputFileName(p.getErrorFileName());
            }
            throw pe1; 
        }
    }
    
    private boolean launchPipeline(String pipelineXmlFile, PipelineDocument callerPipeline, boolean debug) throws PreConditionNotSatisfiedException {
        boolean success = true;
        try {
            PipelineDocument pipelineDoc = PipelineUtils.getPipelineDocument(pipelineXmlFile);
            if (callerPipeline != null) {
                if (callerPipeline.getPipeline().isSetOutputFileNamePrefix())
                    pipelineDoc.getPipeline().setOutputFileNamePrefix(callerPipeline.getPipeline().getOutputFileNamePrefix());
                LoopUtils.copyLoops(callerPipeline, pipelineDoc);
                ParameterUtils.copyParameters(callerPipeline, pipelineDoc);
                //try {pipelineDoc.save(new File("childTask.xml"),new XmlOptions().setSavePrettyPrint());}catch(Exception e){}
            }
            launchPipeline(pipelineDoc,null, debug);
            logger.info("Pipeline " + pipelineXmlFile + " successfully launched");
            return success;
        }catch(ArgumentNotFoundException ane) {
            logger.error(ane.getLocalizedMessage());
        }catch(PipelineException ane) {
            logger.error(ane.getLocalizedMessage());
        }catch(TransformerException ane) {
            logger.error(ane.getLocalizedMessage());
        }
        return !success;
    }
    
  


  /*  private AllResolvedStepsDocument getInternalRepresentation(PipelineDocument pipelineDoc, Step aStep) throws ArgumentNotFoundException, PipelineException, TransformerException {
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
            if (aStep.isSetSupressOutputFiles() && aStep.getSupressOutputFiles())
                rStep.setSupressOutputFiles(aStep.getSupressOutputFiles());
            else if (!aStep.isSetSupressOutputFiles())
                rStep.setSupressOutputFiles(true); 
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
    }*/

    String savedFile;
    static PipelineManager self;
    static Logger logger = Logger.getLogger(PipelineManager.class);
}
