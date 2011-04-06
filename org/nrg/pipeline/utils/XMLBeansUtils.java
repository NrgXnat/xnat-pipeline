/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
*/

package org.nrg.pipeline.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ArgumentData;
import org.nrg.pipeline.xmlbeans.Loop;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResourceData.Input.Argument;
import org.nrg.pipeline.xpath.XPathResolverSaxon;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
////ClassName XMLBeanUtils
/**
Utility methods for xmlbeans 

@author mohanar
@version $Id: XMLBeansUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
@since Pipeline 1.0
*/


public class XMLBeansUtils {
    
    
    public static ResolvedStep copy(final ResolvedStep from) {
       try
        {
          return ((ResolvedStepDocument)XmlObject.Factory.parse( from.xmlText( new XmlOptions(  ).setSaveOuter() ) )).getResolvedStep();
          
        }
        catch ( XmlException xe )
        {
           throw new RuntimeException( xe );
        }
        //to = (ResolvedStep)from.copy();
        //to.setId(to.getId()+":"+from.getId()); to.setDescription(from.getDescription());
        /*if (from.isSetWorkdirectory()) to.setWorkdirectory(from.getWorkdirectory());
        if (from.isSetPrecondition()) to.setPrecondition(from.getPrecondition());
        if (from.isSetPreconditionType()) to.setPreconditionType(from.getPreconditionType());
        if (from.isSetAwaitApprovalToProceed()) to.setAwaitApprovalToProceed(from.getAwaitApprovalToProceed());
        if (from.isSetContinueOnFailure()) to.setContinueOnFailure(from.getContinueOnFailure());
        else to.setContinueOnFailure(false); */
    }
    
    public static void copy(final XmlObject from, XmlObject to, boolean copyAttributes) {
        XmlCursor cursor = from.newCursor();
        XmlCursor target = XmlObject.Factory.newInstance().newCursor();
        target.toNextToken();
        cursor.copyXmlContents(target);
        to = target.getObject();
        /*while (!cursor.toNextToken().isNone()) {
            System.out.println(cursor.currentTokenType() + " " + cursor.xmlText());
            System.out.println("TARGET " +target.currentTokenType() );
            switch(cursor.currentTokenType().intValue()) {
                case org.apache.xmlbeans.XmlCursor.TokenType.INT_ATTR:
                    if (copyAttributes) {
                        target.insertAttributeWithValue(cursor.getName(), cursor.getAttributeText(cursor.getName()));
                        //target.toNextToken();
                    }
                    break;
                case org.apache.xmlbeans.XmlCursor.TokenType.INT_TEXT:
                    target.toNextToken();
                    cursor.copyXml(target);                     
                    break;
                case org.apache.xmlbeans.XmlCursor.TokenType.INT_START:
                    cursor.copyXmlContents(target);
                    //target.toNextToken();
                    break;
            }
       }*/
        target.dispose(); cursor.dispose();
        
    }



    public static String validateAndGetErrors(XmlObject xo) {
        String rtn = null;
        ArrayList validationErrors = new ArrayList();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);
        
        boolean isValid = xo.validate(validationOptions);

        //Print the errors if the XML is invalid.
         if (!isValid)
         {
             rtn = "";
             Iterator iter = validationErrors.iterator();
             while (iter.hasNext())
             {
                 rtn += (">> " + iter.next() + "\n");
             }
         }
        return rtn;
    }
    
   public static ParameterData getParameterByName (PipelineData pipelineData, String nameToMatch) {
       ParameterData rtn = null;
       if (!pipelineData.isSetParameters()) return rtn;
       Object[] parameters =  pipelineData.getParameters().getParameterArray();
       int i = getMatchingObjectIndex(parameters,"getName", nameToMatch, null, null);
       if (i == -1) return rtn;
       rtn = (ParameterData)parameters[i];
       return rtn;
   }
   
   public static ParameterData getParameterByName (AllResolvedStepsDocument pipelineDoc, String nameToMatch) {
       ParameterData rtn = null;
       Object[] parameters =  pipelineDoc.getAllResolvedSteps().getParameters().getParameterArray();
       int i = getMatchingObjectIndex(parameters,"getName", nameToMatch, null, null);
       if (i == -1) return rtn;
       rtn = (ParameterData)parameters[i];
       return rtn;
   }

   
   public static int getParameterIndexByName (PipelineData pipelineData, String nameToMatch) {
       int rtn = -1;
       if (!pipelineData.isSetParameters()) return rtn;
       Object[] parameters =  pipelineData.getParameters().getParameterArray();
       rtn = getMatchingObjectIndex(parameters,"getName", nameToMatch, null, null);
       return rtn;
   }
   
   public static ParameterData getParameterByName (Parameters params, String nameToMatch) {
       ParameterData rtn = null;
       if (params.sizeOfParameterArray() == 0) return rtn;
       Object[] parameters =  params.getParameterArray();
       int i = getMatchingObjectIndex(parameters,"getName", nameToMatch, null, null);
       if (i == -1) return rtn;
       rtn = (ParameterData)parameters[i];
       return rtn;
   }
   
   public static int getParameterIndexByName (Parameters params, String nameToMatch) {
       int rtn = -1;
       if (params.sizeOfParameterArray() == 0) return rtn;
       Object[] parameters =  params.getParameterArray();
       rtn = getMatchingObjectIndex(parameters,"getName", nameToMatch, null, null);
       return rtn;
   }

   
   
   public static ArrayList getLoopValuesById(PipelineData pipelineData, String loopId) {
       ArrayList rtn = null;
       if (loopId.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
           try {
               rtn = XPathResolverSaxon.GetInstance().evaluate(pipelineData,loopId);
           }catch(Exception e) {
               logger.debug(e);
               return rtn;
           }
       }else {
           Object[] loops =  pipelineData.getLoopArray();
           int i = getMatchingObjectIndex(loops,"getId", loopId, null, null);
           if (i == -1) return rtn;
           Loop loop = (Loop)loops[i];
           rtn = new ArrayList(Arrays.asList(loop.getValueArray()));
       }
       return rtn;
   }
   
   public static ArrayList getArgumentsById (ResourceData resourceData, String idToMatch) {
       ArrayList rtn = null;
       Argument[] arguments =  resourceData.getInput().getArgumentArray();
       for (int i = 0; i < arguments.length; i++) {
           if (arguments[i].getId().equals(idToMatch)) {
               if (rtn == null) rtn = new ArrayList();
               rtn.add(arguments[i]);
           }
       }
       return rtn;
   }
   
   public static ArgumentData getArgumentById (ResourceData resourceData, String idToMatch) {
       ArgumentData rtn = null;
       Object[] arguments =  resourceData.getInput().getArgumentArray();
       int i = getMatchingObjectIndex(arguments,"getId", idToMatch, null, null);
       if (i == -1) return rtn;
       rtn = (ArgumentData)arguments[i];
       return rtn;
   }

   public static int getArgumentIndexById (ResourceData resourceData, String idToMatch) {
       int rtn = -1;
       Object[] arguments =  resourceData.getInput().getArgumentArray();
       rtn = getMatchingObjectIndex(arguments,"getId", idToMatch, null, null);
       return rtn;
   }

   public static ArrayList getStepIndicesById (PipelineData pipelineData, String idToMatch) {
       ArrayList rtn = new ArrayList();
       Steps steps = pipelineData.getSteps();
       for (int i = 0; i < steps.sizeOfStepArray(); i++) {
    	   Step aStep = steps.getStepArray(i);
    	   if (aStep.getId().startsWith(idToMatch+PipelineConstants.STEP_ID_SEPARATOR))
               rtn.add(new Integer(i));
       }
       return rtn;
   }

   public static ArrayList getStepIndicesById (AllResolvedStepsDocument rPipelineDoc, String idToMatch) {
       ArrayList rtn = new ArrayList();
       ResolvedStep[] steps = rPipelineDoc.getAllResolvedSteps().getResolvedStepArray();
       for (int i = 0; i < steps.length; i++) {
           if (steps[i].getId().startsWith(idToMatch+":"))
               rtn.add(new Integer(i));
           else if (steps[i].getId().endsWith(":++" + idToMatch+":")) {
        	   rtn.add(new Integer(i));
           }
       }
       return rtn;
   }

   public static Hashtable getAllStepIds(AllResolvedStepsDocument rPipelineDoc) {
       Hashtable rtn = new Hashtable();
       ResolvedStep[] steps = rPipelineDoc.getAllResolvedSteps().getResolvedStepArray();
       for (int i = 0; i < steps.length; i++) {
           String rStepId = steps[i].getId();
           int index = rStepId.indexOf(":");
           String originalStepId = rStepId;
           if (index != -1) originalStepId = rStepId.substring(0,index);
           ArrayList indices = null;
           if (rtn.containsKey(originalStepId)) {
               indices = (ArrayList)rtn.get(originalStepId); 
           }else {
               indices = new ArrayList(); 
           }
           indices.add(new Integer(i));
           rtn.put(originalStepId, indices);
       }
       return rtn;
   }
   
   public static String getStepId(ResolvedStep rStep) {
       int endIndex = rStep.getId().length();
       int marker = rStep.getId().indexOf(PipelineConstants.STEP_ID_SEPARATOR); 
       if (marker!=-1) {
           endIndex = marker;
       }
       return rStep.getId().substring(0,endIndex);
   }
   
   public static int getStepIndexById (PipelineData pipelineData, String idToMatch) {
       int rtn = -1;
       Steps steps = pipelineData.getSteps();
       for (int i = 0; i < steps.sizeOfStepArray(); i++) {
           if (steps.getStepArray(i).getId().startsWith(idToMatch+":")) {
               rtn = i;
               break;
           }
       }
       return rtn;
   }
   
   
    public static int getMatchingObjectIndex(Object[] objectArray, String methodName,  Object valueToMatch, Class[] methodParamsTypes, Object[] methodParamsValues ) {
        int rtn = -1;
        if (objectArray == null) return rtn;
        try {
            for (int i = 0; i < objectArray.length; i++) {
                Object arrayItem = objectArray[i];
                Class targetClass = arrayItem.getClass();
                Method method = targetClass.getMethod(methodName, methodParamsTypes);
                Object invokedObject = method.invoke(arrayItem, methodParamsValues);
                if (invokedObject.equals(valueToMatch)) {
                    rtn = i;
                    break;
                }
            }
        } catch(NoSuchMethodException cne) {
            logger.error("getMatchingObjectIndex()::" + cne.getLocalizedMessage());
        } catch(InvocationTargetException ite) {
            logger.error("getMatchingObjectIndex()::" + ite.getLocalizedMessage());
        } catch(IllegalAccessException iae) {
            logger.error("getMatchingObjectIndex()::" + iae.getLocalizedMessage());
        }
        return rtn;
    }
    
    
    public static Document getDomDocument(XmlObject xo) throws PipelineException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // MUST be TRUE or can't get back to bean land
            //However the parser doesnt return the XPath queries correctly this way
            //factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Switch to org.w3c.dom.Document - xmlbeans.getDomNode() doesn't
            // really seem to give you this
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            xo.save(out,new XmlOptions().setSavePrettyPrint());
            Document doc = builder.parse(new InputSource(new ByteArrayInputStream(out.toByteArray())));
            //XMLSerializer serializer = new XMLSerializer();
            //serializer.setOutputCharStream(new java.io.FileWriter("dom.xml"));
            //serializer.serialize(doc);
            return doc;
        } catch(Exception e) {
            System.out.println("getDomDocument " + e.getLocalizedMessage());
            logger.error("getDomDocument():: Encountered exception " + e.getClass(),e.getCause());
            throw new PipelineException(e.getClass() + "::" + e.getLocalizedMessage(), e);
        }
    }
    
    static Logger logger = Logger.getLogger(XMLBeansUtils.class);
    
}
