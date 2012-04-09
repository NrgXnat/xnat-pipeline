/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.IndependentContext;
import net.sf.saxon.trans.XPathException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.manager.ResourceManager;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ResourceDocument;
import org.nrg.pipeline.xmlbeans.OutputData.File.Path;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step.Resource;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedOutput;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;
import org.nrg.pipeline.xmlbeans.ResourceData.Input;
import org.nrg.pipeline.xmlbeans.ResourceData.Input.Argument;
import org.nrg.pipeline.xpath.XPathResolverSaxon;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ResourceUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class ResourceUtils {
    
  
    
    
    /*public static void resolveOutputs(ResourceData internalResourceData) throws PipelineException {
        ResourceDocument internalResourceDocument = ResourceDocument.Factory.newInstance();
        internalResourceDocument.setResource(internalResourceData);
        if (internalResourceData.isSetOutputs()) {
            Output[] outputArray = internalResourceData.getOutputs().getOutputArray();
            for (int j = 0; j < internalResourceData.getOutputs().sizeOfOutputArray(); j++ ) {
                Output out = outputArray[j];
                String xPathExpression = out.getValue();
                if (xPathExpression.startsWith("$") && xPathExpression.endsWith("$")) {
                    try {
                      ArrayList  resolvedValues = XPathResolver.GetInstance().resolveXPathExpressions(StringUtils.transformOutputExpression(xPathExpression), XMLBeansUtils.getDomDocument(internalResourceDocument));
                      if (resolvedValues == null || !(resolvedValues.size() > 0)) {
                         throw new PipelineException("Couldnt resolve output " + xPathExpression + " for " +  FileUtils.getAbsolutePath(internalResourceData.getLocation(), internalResourceData.getName()));     
                      }
                      out.setValue((String)resolvedValues.get(0));
                    }catch(TransformerException te) {
                        logger.info("getInternalRepresentation()::" + te.getLocalizedMessage(), te.getCause());
                        throw new PipelineException(te.getClass() + " ==> " + te.getLocalizedMessage());
                    }
                }
            }
        }
    }*/
    
    
    public static ResolvedResource getInternalResourceData(ResourceDocument rscDoc) {
        ResolvedResource internalResourceData = ResolvedResource.Factory.newInstance();
        ResourceData rscData = rscDoc.getResource();
        copy(rscData,internalResourceData,false);
        internalResourceData.setPrefix(rscDoc.getResource().getDomNode().getPrefix());
        return  internalResourceData;
    }
    
  
    
    public static boolean addArgumentToInternalResource(ResolvedResource internalResourceData, Argument argument, String value) {
        boolean success = true;
        if (internalResourceData == null) return !success;
        Input internalResourceDataInput = internalResourceData.getInput(); 
        if (internalResourceDataInput == null) {
           internalResourceDataInput =  internalResourceData.addNewInput();
        }
        Argument internalArgument = internalResourceDataInput.addNewArgument();
        if (argument.isSetName())internalArgument.setName(argument.getName());
        internalArgument.setDescription(argument.getDescription());
        if (value != null) {
            
            internalArgument.setValue(value);
        }
        if (argument.isSetNospace())internalArgument.setNospace(argument.getNospace());
        if (argument.isSetPrefix())internalArgument.setPrefix(argument.getPrefix());
        internalArgument.setId(argument.getId());
        return success;
    }
    
    
    /**
     * createResourcesForLoopOnArguments
     * 
     * Purpose: 
     *  Each of the arguments which contain PIPELINE_LOOPON
     *  lead to launch of a resource. This method creates new resources
     *  with a value of the argument plugged in. 
     *  
     * Input:
     *     rStep: The internal representation of a Step  
     *     @see org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep
     *     internalResourceDocument: The internal representation of a resource with
     *                               with argument values populated which are not of the loopOn kind     
     *      @see org.nrg.pipeline.xmlbeans.ResourceDocument
     *       
     *     originalRscData: The resourceData which represents the xml describing a resource. This object doesnt have the values populated
     *     @see org.nrg.pipeline.xmlbeans.ResourceData
     *      
     *     argumentsWhichHavePipelineLoopOn: Hashtable whose keys is the index of the argument for which the values are to be added
     *       the value of against the key is an arraylist 
     *        
     *    @see org.nrg.pipeline.manager.PipelineManager    
     *  
     *  
     */
    
    public static void createResourcesForArguments(PipelineDocument pipelineDoc, ResolvedStep rStep,  ResolvedResource internalSeedResourceData, ResourceData originalRscData, Hashtable argumentsWhichHavePipelineLoopOn, Resource stepResource, Step originalStep) throws PipelineEngineException, TransformerException {
        LinkedHashMap argumentsWithResolvedValues = resolvePipelineValues(pipelineDoc, rStep,internalSeedResourceData,originalRscData, argumentsWhichHavePipelineLoopOn);
        //LoggerUtils.print(argumentsWhichHavePipelineLoopOn);
        createResourcesForArguments(rStep,internalSeedResourceData,originalRscData, argumentsWithResolvedValues, stepResource, originalStep);
        argumentsWithResolvedValues .clear();
        argumentsWithResolvedValues  = null;
    }

    private static LinkedHashMap resolvePipelineValues(PipelineDocument pipelineDoc, ResolvedStep rStep,  ResolvedResource internalSeedResourceData, ResourceData originalRscData, Hashtable argumentsWhichHavePipelineLoopOn) throws PipelineEngineException, TransformerException{
        Enumeration loopOnStmts = argumentsWhichHavePipelineLoopOn.keys();
        LinkedHashMap argumentsWithResolvedLoopOnStmts = new LinkedHashMap();
        while(loopOnStmts.hasMoreElements()) {
            String consStr = (String)loopOnStmts.nextElement(); //consStr = PIPELINE_LOOPON(*)
            ArrayList loopValues = XMLBeansUtils.getLoopValuesById(pipelineDoc.getPipeline(),LoopUtils.getLoopOnId(pipelineDoc.getPipeline(),consStr,true));
            ArrayList values = null; 
            Iterator argIndices = ((LinkedHashMap)argumentsWhichHavePipelineLoopOn.get(consStr)).keySet().iterator();
            Object argumentIndexKey = null;
            while(argIndices.hasNext()) {
               argumentIndexKey = argIndices.next(); 
               if (argumentsWithResolvedLoopOnStmts.containsKey(argumentIndexKey)) {
                   values = (ArrayList)argumentsWithResolvedLoopOnStmts.get(argumentIndexKey);
               }else {
                   values = (ArrayList)((HashMap)argumentsWhichHavePipelineLoopOn.get(consStr)).get(argumentIndexKey);
               }
               if (values != null) {
                   ArrayList resolvedValues = new ArrayList();
                   for (int j = 0; j < values.size(); j++)
                       for (int i = 0; i < loopValues.size(); i++)  {
                           resolvedValues.add(org.apache.commons.lang.StringUtils.replace((String)values.get(j),consStr,"'" + (String)loopValues.get(i) +"'" ) );
                       }
                   argumentsWithResolvedLoopOnStmts.put(argumentIndexKey,resolvedValues);
               }
            }
        }
        Iterator argValues = argumentsWithResolvedLoopOnStmts.keySet().iterator();
        while (argValues.hasNext()) {
            Object key = argValues.next();
            ArrayList rVal = XPathResolverSaxon.GetInstance().resolveXPathExpressions(StringUtils.transformOutPutExpression((ArrayList)argumentsWithResolvedLoopOnStmts.get(key)),pipelineDoc);
            argumentsWithResolvedLoopOnStmts.put(key,rVal);
        }
        //LoggerUtils.print(argumentsWithResolvedLoopOnStmts);
        return argumentsWithResolvedLoopOnStmts;
    }
    
    
    private static void createResourcesForArguments(ResolvedStep rStep, ResolvedResource internalSeedResourceData, ResourceData originalRscData, LinkedHashMap argumentsWhichHavePipelineLoopOn, Resource stepResource, Step originalStep) throws PipelineEngineException{
       ArrayList internalSeedResourceDataArray = new ArrayList();
       internalSeedResourceDataArray.add(internalSeedResourceData);
       internalSeedResourceDataArray = getNewResources(internalSeedResourceDataArray, originalRscData, argumentsWhichHavePipelineLoopOn, stepResource);
       if (internalSeedResourceDataArray != null && internalSeedResourceDataArray.size()>0) {
            for (int i = 0; i < internalSeedResourceDataArray.size(); i++) {
                ResolvedResource rRsc = rStep.addNewResolvedResource();
                copy(reindex((ResolvedResource)internalSeedResourceDataArray.get(i), stepResource.getLocation(), stepResource.getName()),rRsc,true);
                if (stepResource.isSetSsh2Host()) rRsc.setSsh2Host(stepResource.getSsh2Host());
                if (stepResource.isSetSsh2Identity()) rRsc.setSsh2Identity(stepResource.getSsh2Identity());
                if (stepResource.isSetSsh2Password()) rRsc.setSsh2Password(stepResource.getSsh2Password());
                if (stepResource.isSetSsh2User()) rRsc.setSsh2User(stepResource.getSsh2User());
                resolveOutput(rRsc,rStep, originalStep);
                //resolveProvenance(rRsc);
            }
       }
    }
    
    
    
    
    public static ArrayList resolveXPathExpressions (ArrayList xStmts, ResolvedResource xmlObj) throws PipelineEngineException{
        ArrayList rtn = new ArrayList();
        for (int i = 0; i < xStmts.size(); i++) {
            rtn.addAll(resolveXPathExpression((String)xStmts.get(i),xmlObj));
        }    
        return rtn;
    }
    
    private static ArrayList resolveXPathExpression (String xStmt, ResolvedResource rsc) throws PipelineEngineException {
        ArrayList rtn = new ArrayList();
        if (rsc == null ) return rtn;
        try {
            XPathEvaluator xpe = new XPathEvaluator();
            XmlOptions xmlOptions = new XmlOptions().setSavePrettyPrint();
            ResourceDocument rscDoc = ResourceDocument.Factory.newInstance();
            rscDoc.setResource(rsc);
            rscDoc.getResource().getDomNode().setPrefix(rsc.getPrefix());

            if (rscDoc.getResource().getDomNode().getPrefix() != null && rscDoc.getResource().getDomNode().getPrefix().equals("") && rscDoc.getResource().getDomNode().getNamespaceURI() != null) {
                ((IndependentContext)xpe.getStaticContext()).declareNamespace("", rscDoc.getResource().getDomNode().getNamespaceURI());
                xmlOptions.setUseDefaultNamespace();
            }else if (rscDoc.getResource().getDomNode().getPrefix() != null && !rscDoc.getResource().getDomNode().getPrefix().equals("") && rscDoc.getResource().getDomNode().getNamespaceURI() != null) {
                ((IndependentContext)xpe.getStaticContext()).declareNamespace(rscDoc.getResource().getDomNode().getPrefix(), rscDoc.getResource().getDomNode().getNamespaceURI());
                HashMap suggestedPrefixes = new HashMap();
                suggestedPrefixes.put(rscDoc.getResource().getDomNode().getNamespaceURI(),rscDoc.getResource().getDomNode().getPrefix() );
                xmlOptions.setSaveSuggestedPrefixes(suggestedPrefixes);
            }
    
            SAXSource ss = new SAXSource(new  InputSource(rscDoc.newInputStream(xmlOptions)));
            
            String resolvedXPath =   xStmt;
            //logger.info("CAME HERE " + resolvedXPath);
            XPathExpression xExpr =  xpe.createExpression(resolvedXPath);
            SequenceIterator rtns = xExpr.rawIterator(ss);
            Item xpathObj = rtns.next();
            while (xpathObj != null) {
                xpathObj = rtns.current();
                rtn.add(xpathObj.getStringValue());
                xpathObj = rtns.next();
            }
            return rtn;
        }catch(XPathException e) {
            throw new PipelineEngineException("Encountered " + e.getClass() + "==>" + e.getLocalizedMessage(), e);
        }
    }
    
    
    private static void resolveOutput(ResolvedResource rsc, ResolvedStep rStep, Step originalStep) throws PipelineEngineException{
        //ResourceDocument rscDoc = ResourceDocument.Factory.newInstance();
        //rscDoc.setResource(rsc); 
        if (OutPutUtils.selfContainedOutput(originalStep)) return;
        if (rsc.isSetOutputs()) {
            ResourceData.Outputs newOut = ResourceData.Outputs.Factory.newInstance();
            for (int i = 0; i < rsc.getOutputs().getOutputArray().length; i++) {
                ResourceData.Outputs.Output output = rsc.getOutputs().getOutputArray(i);
                ArrayList fileNames = ResourceUtils.resolveXPathExpressions(StringUtils.transformOutputExpression(output.getFile().getName()),rsc);
                String path = null;
                if (output.getFile().isSetPath()) { 
                  path = (String)ResourceUtils.resolveXPathExpressions(StringUtils.transformOutputExpression(output.getFile().getPath().getStringValue()),rsc).get(0);
                  if (output.getFile().getPath().getRelativePath())
                      path = rStep.getWorkdirectory() + FileUtils.getFileSeparatorChar(rsc) + path;
                }else 
                    path = rStep.getWorkdirectory();
                for (int j = 0; j < fileNames.size(); j++) {
                    ResolvedOutput rOut = rStep.addNewResolvedOutput();
                    rOut.setId(output.getId());
                    org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedOutput.File rOutFile = rOut.addNewFile();
                    rOutFile.setXsiType(output.getFile().getXsiType());
                    rOutFile.setName((String)fileNames.get(j));
                    Path newPath = rOutFile.addNewPath();
                    newPath.setStringValue(path); newPath.setRelativePath(false);
                    if (output.getFile().isSetFormat())
                        rOutFile.setFormat(output.getFile().getFormat());
                    if (output.getFile().isSetDescription())
                        rOutFile.setDescription(output.getFile().getDescription());
                    String desiredContent = OutPutUtils.getContent(originalStep,rsc.getName()); 
                    if (desiredContent != null) {
                        rOutFile.setContent(desiredContent);
                    }else if (output.getFile().isSetContent())
                        rOutFile.setContent(output.getFile().getContent());
                    if (output.getFile().isSetDimensions())
                        rOutFile.setDimensions(output.getFile().getDimensions());
                    if (output.getFile().isSetVoxelRes())
                        rOutFile.setVoxelRes(output.getFile().getVoxelRes());
                    if (output.getFile().isSetOrientation())
                        rOutFile.setOrientation(output.getFile().getOrientation());
                    if (output.getFile().isSetFileCount())
                        rOutFile.setFileCount(output.getFile().getFileCount());
                    if (output.getFile().isSetPattern()) {
                        ArrayList pattern = ResourceUtils.resolveXPathExpressions(StringUtils.transformOutputExpression(output.getFile().getPath().getStringValue()),rsc);
                        String commaSeperated = "";
                        if (pattern.size() > 1) {
                            for (int k = 0; k < pattern.size(); k++) {
                                commaSeperated += (String)pattern.get(k) + " ,";
                            }
                            if (commaSeperated.endsWith(","))
                                commaSeperated = commaSeperated.substring(0, commaSeperated.length()-1);
                        }else 
                            rOutFile.setPattern((String)pattern.get(0));
                    }    
                }
            }
        }
    }
    

    private static ResolvedResource reindex(ResolvedResource rsc, String location, String name) {
        if (!rsc.isSetInput()) return rsc;
        //ResourceDocument rtn = ResourceDocument.Factory.newInstance();
        ResolvedResource rtnRsc = ResolvedResource.Factory.newInstance();
        if (rsc.isSetPipeId()) rtnRsc.setPipeId(rsc.getPipeId());
        ResourceDocument resourceDocument = ResourceManager.GetInstance().getResource(location, name);
        copy(resourceDocument.getResource(), rtnRsc,false);
        rtnRsc.setPrefix(rsc.getPrefix());
        Input input = rtnRsc.addNewInput();
        Object[] indices = new Object[resourceDocument.getResource().getInput().sizeOfArgumentArray()];
        for (int i = 0; i < rsc.getInput().sizeOfArgumentArray(); i++) {
            int index = XMLBeansUtils.getArgumentIndexById(resourceDocument.getResource(), rsc.getInput().getArgumentArray(i).getId());
            Argument arg = (Argument)resourceDocument.getResource().getInput().getArgumentArray(index).copy();
            if (rsc.getInput().getArgumentArray(i).isSetValue())
                arg.setValue(rsc.getInput().getArgumentArray(i).getValue());
            if (indices[index]==null) {
                ArrayList args = new ArrayList();
                args.add(arg);
                indices[index]=args;
            }else {
                ((ArrayList)indices[index]).add(arg);
            }
        }
        for (int i = 0; i < indices.length; i++) {
            ArrayList args = (ArrayList)indices[i];
            if (args != null) {
                for (int j = 0; j < args.size(); j++) {
                    Argument newarg = input.addNewArgument();
                    ArgumentUtils.copy((Argument)args.get(j), newarg, true);
                }
            }
        }
        rtnRsc.setDescription("");
        return rtnRsc;
    }
    
    public static void copy(ResourceData from, ResourceData to, boolean completeCopy) {
        if (from.isSetPipeId()) to.setPipeId(from.getPipeId());
        to.setName(from.getName());
        if (from.isSetLocation())to.setLocation(from.getLocation());
        if (from.isSetCommandPrefix())to.setCommandPrefix(from.getCommandPrefix());
        to.setType(from.getType());
        to.setDescription(from.getDescription());
        if (from instanceof ResolvedResource && to instanceof ResolvedResource) {
            ((ResolvedResource)to).setPrefix(((ResolvedResource)from).getPrefix());
            if (((ResolvedResource)from).isSetSsh2Host()) ((ResolvedResource)to).setSsh2Host(((ResolvedResource)from).getSsh2Host());
            if (((ResolvedResource)from).isSetSsh2Identity()) ((ResolvedResource)to).setSsh2Identity(((ResolvedResource)from).getSsh2Identity());
            if (((ResolvedResource)from).isSetSsh2Password()) ((ResolvedResource)to).setSsh2Password(((ResolvedResource)from).getSsh2Password());
            if (((ResolvedResource)from).isSetSsh2User()) ((ResolvedResource)to).setSsh2User(((ResolvedResource)from).getSsh2User());
        }
        if (from.isSetEstimatedTime())to.setEstimatedTime(from.getEstimatedTime());
        if (completeCopy && from.isSetInput()) {
           to.setInput(from.getInput()); 
        }
        if (from.isSetOutputs()) to.setOutputs(from.getOutputs());
        if (from.isSetProvenance()) to.setProvenance(from.getProvenance());
    }
    
    
    
    public static ResourceData copy(ResourceData from) {
        ResourceData  rtn = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            from.save(out,new XmlOptions().setSavePrettyPrint());
            rtn = ResourceData.Factory.parse(new InputSource(new ByteArrayInputStream(out.toByteArray())).getByteStream());
            out.close();
        }catch(IOException ioe) {
        }catch(XmlException xmle) {
        }
        return rtn;
    }
    

    
    private static ArrayList getNewResources(ArrayList internalSeedResourceDataList, ResourceData originalRscData, LinkedHashMap argumentsWhichHavePipelineLoopOn, Resource stepResource) throws PipelineEngineException{
        ArrayList resourcesWithLoopValuesResolved = new ArrayList();
        if (argumentsWhichHavePipelineLoopOn != null && argumentsWhichHavePipelineLoopOn.size() > 0) {
            int commonLength = getCommonLength(argumentsWhichHavePipelineLoopOn);
            if (commonLength == -1) return null;
            if (internalSeedResourceDataList.size() == 1) {
                Integer hashKey = (Integer)argumentsWhichHavePipelineLoopOn.keySet().iterator().next();
                Argument arg = originalRscData.getInput().getArgumentArray(hashKey.intValue());
                ArrayList resolvedValues =  (ArrayList)argumentsWhichHavePipelineLoopOn.get(hashKey);
                for (int m = 0; m < resolvedValues.size(); m++) {
                    ResolvedResource rsc = ResolvedResource.Factory.newInstance();
                    copy((ResolvedResource)internalSeedResourceDataList.get(0), rsc, true);
                    if (stepResource.isSetPipeId()) {
                        rsc.setPipeId(stepResource.getPipeId() + "_" + (String)resolvedValues.get(m));
                    }
                    if(!ResourceUtils.addArgumentToInternalResource(rsc,arg,(String)resolvedValues.get(m))) {
                        logger.info("getNewResources():: Coudlnt add Argument " + arg.getId() + " to Internal Resource " + originalRscData.getLocation() + File.separator + originalRscData.getName());
                        throw new PipelineEngineException("Coudlnt add Argument " + arg.getId() + " to Internal Resource " + FileUtils.getAbsolutePath(originalRscData.getLocation(), originalRscData.getName()));
                    }
                    resourcesWithLoopValuesResolved.add(rsc);
                }
                argumentsWhichHavePipelineLoopOn.remove(hashKey);
            }
            Iterator keys = argumentsWhichHavePipelineLoopOn.keySet().iterator();
            while (keys.hasNext()) {
                Integer hashKey = (Integer)keys.next();
                Argument arg = originalRscData.getInput().getArgumentArray(hashKey.intValue());
                ArrayList resolvedValues =  (ArrayList)argumentsWhichHavePipelineLoopOn.get(hashKey);
                for (int i = 0; i < commonLength; i++) {
                    if(!ResourceUtils.addArgumentToInternalResource((ResolvedResource)resourcesWithLoopValuesResolved.get(i),arg,(String)resolvedValues.get(i))) {
                        logger.info("getNewResources():: Coudlnt add Argument " + arg.getId() + " to Internal Resource " + originalRscData.getLocation() + File.separator + originalRscData.getName());
                        throw new PipelineEngineException("Coudlnt add Argument " + arg.getId() + " to Internal Resource " + FileUtils.getAbsolutePath(originalRscData.getLocation(), originalRscData.getName()));
                    }
                }
            }
        }else{
            if (stepResource.isSetPipeId()) {
                for (int i = 0; i < internalSeedResourceDataList.size(); i++) {
                    ((ResolvedResource)internalSeedResourceDataList.get(i)).setPipeId(stepResource.getPipeId());
                }
            }
            resourcesWithLoopValuesResolved = internalSeedResourceDataList;
        }
        return resourcesWithLoopValuesResolved;
    }
    
    private static int getCommonLength(LinkedHashMap argValueHash) {
        int rtn = -1;
        if (argValueHash == null) return rtn;
        Iterator values = argValueHash.values().iterator();
        while (values.hasNext()) {
            ArrayList val = (ArrayList)values.next();
            if (rtn == -1) rtn = val.size();
            else if (rtn != val.size()){
                rtn = -1;
                break;
            }
        }
        return rtn;
    }
    
    /*private static ArrayList getNewResources(ArrayList internalSeedResourceDataList, ResourceData originalRscData, Hashtable argumentsWhichHavePipelineLoopOn) throws PipelineException{
        ArrayList newResources = internalSeedResourceDataList;
        //ArrayList resourcesWithLoopValuesResolved = new ArrayList();
        System.out.println("Size of Array " + internalSeedResourceDataList.size());
        if (argumentsWhichHavePipelineLoopOn != null && argumentsWhichHavePipelineLoopOn.size() > 0) {
            Integer hashKey = (Integer)argumentsWhichHavePipelineLoopOn.keys().nextElement();
            Argument arg = originalRscData.getInput().getArgumentArray(hashKey.intValue());
            ArrayList resolvedValues =  (ArrayList)argumentsWhichHavePipelineLoopOn.get(hashKey);
            if (resolvedValues != null) {
                for (int n = 0; n < newResources.size(); n++) {
                    for (int m = 0; m < resolvedValues.size(); m++) {
                        //ResourceData rsc = copy((ResourceData)newResources.get(n));
                        if(!ResourceUtils.addArgumentToInternalResource((ResourceData)newResources.get(n),arg,(String)resolvedValues.get(m))) {
                            logger.info("getNewResources():: Coudlnt add Argument " + arg.getId() + " to Internal Resource " + originalRscData.getLocation() + File.separator + originalRscData.getName());
                            throw new PipelineException("Coudlnt add Argument " + arg.getId() + " to Internal Resource " + FileUtils.getAbsolutePath(originalRscData.getLocation(), originalRscData.getName()));
                        }
                        //resourcesWithLoopValuesResolved.add(rsc);
                    }
                }
            }
            argumentsWhichHavePipelineLoopOn.remove(hashKey);
            newResources = getNewResources(newResources,originalRscData, argumentsWhichHavePipelineLoopOn);
        }else {
            //resourcesWithLoopValuesResolved = newResources;
        }
        //resolveOutputs(resourcesWithLoopValuesResolved);
        //return resourcesWithLoopValuesResolved;
        return newResources;
    }*/
    static Logger logger = Logger.getLogger(ResourceUtils.class); 
}
