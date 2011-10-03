/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.xmlbeans.Loop;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LoopUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class LoopUtils {
    
    public static void setLoopValues(PipelineDocument pipelineDoc) {
        try {
            String xPathToLoops = PipelineConstants.PIPELINE_XPATH_MARKER + "/Pipeline/loop" + PipelineConstants.PIPELINE_XPATH_MARKER;
            XmlObject[] loops = XPathUtils.executeQuery(pipelineDoc,xPathToLoops);
            //logger.debug("setLoopValues():: " + pipelineDoc.xmlText());
            if (loops != null && loops.length > 0) {
                for (int i = 0; i < loops.length; i++) {
                    Loop aLoop = (Loop)loops[i];
                    if (aLoop.isSetXpath()) {
                        String xPathExpression = aLoop.getXpath();
                        ArrayList loopValues = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(), xPathExpression);
                        if (loopValues == null || loopValues.size() == 0) {
                            xPathExpression = xPathExpression.replace("/values/list","/values/unique");
                            loopValues = XPathResolverSaxon.GetInstance().evaluate(pipelineDoc.getPipeline(), xPathExpression);
                        }
                        if (loopValues != null && loopValues.size() > 0) {
                            for (int j = 0; j < loopValues.size(); j++) {
                                aLoop.addValue((String)loopValues.get(j));
                               // System.out.println("Loop values for " + xPathExpression + " " + loopValues.get(j));
                            }     
                        }
                        aLoop.unsetXpath();
                    }
                }
            }
        }catch(PipelineException pe) {
           logger.info("setLoopValues():: " + pe.getLocalizedMessage());
        }catch (TransformerException te) {
            logger.info("setLoopValues():: " + te.getLocalizedMessage());
        }
    }
    
   public static String getLoopOnId(PipelineData pipelineData, String xpathExpression, boolean returnLoopId) throws PipelineException {
       String rtn = null;
       if (xpathExpression == null) return rtn;
       int indexOfLoopOn = xpathExpression.indexOf(PipelineConstants.PIPELINE_LOOPON);
       int endLoopOnMarker = -1;
       if (indexOfLoopOn != -1) {
           String loopOnId = getExpressionWhichIsBeingLooped(xpathExpression,PipelineConstants.PIPELINE_LOOPON, PipelineConstants.openingBracket,PipelineConstants.closingBracket);
           if (loopOnId != null) {
               endLoopOnMarker = xpathExpression.indexOf(PipelineConstants.closingBracket,indexOfLoopOn+PipelineConstants.PIPELINE_LOOPON.length()+1+loopOnId.length());
           }
          if (endLoopOnMarker != -1) {
              if (returnLoopId) 
                  rtn = loopOnId;
              else 
                  rtn = xpathExpression.substring(indexOfLoopOn ,endLoopOnMarker+1);
          }else {
              throw new PipelineException("Illegal token encountered while parsing " + xpathExpression + " [Missing )] in  pipeline identified by " + pipelineData.getName());
          }
       }
       return rtn;
   }
   
   public static String getLoopValueId(PipelineData pipelineData, String xpathExpression, boolean returnLoopId) throws PipelineException {
       String rtn = null;
       if (xpathExpression == null) return rtn;
       int indexOfLoopValue = xpathExpression.indexOf(PipelineConstants.PIPELINE_LOOPVALUE);
       int endLoopValueMarker = -1;
       if (indexOfLoopValue != -1) {
           String loopOnId = getExpressionWhichIsBeingLooped(xpathExpression,PipelineConstants.PIPELINE_LOOPVALUE, PipelineConstants.openingBracket,PipelineConstants.closingBracket);
           if (loopOnId != null) {
               endLoopValueMarker = xpathExpression.indexOf(PipelineConstants.closingBracket,indexOfLoopValue+PipelineConstants.PIPELINE_LOOPVALUE.length()+1+loopOnId.length());
           }
          if (endLoopValueMarker != -1) {
              if (returnLoopId) 
                  rtn = loopOnId;
              else 
                  rtn = xpathExpression.substring(indexOfLoopValue ,endLoopValueMarker+1);
          }else {
              throw new PipelineException("Illegal token encountered while parsing " + xpathExpression + " [Missing )] in  pipeline identified by " + pipelineData.getName());
          }
       }
       return rtn;
   }
   
    
    public static String loopNeedsToBeResolved(Object obj) throws PipelineException{
        String rtn = null;
        ArrayList input = null;
        if (obj instanceof String) {
            input = new ArrayList(); input.add(obj);
        }else if (obj instanceof ArrayList) {
            input = (ArrayList)obj;
        }else {
            if (obj != null) throw new PipelineException("Input Argument of type " + obj.getClass() + " not supported");
            else {
                throw new PipelineException("Input Argument of type not supported....argument could be null");
            }
        }
        if (input == null) return rtn;
        for (int i = 0; i < input.size(); i++) {
            if (((String)input.get(i)).indexOf(PipelineConstants.PIPELINE_LOOPON) != -1) {
                rtn = PipelineConstants.PIPELINE_LOOPON;
                break;
            }else if (((String)input.get(i)).indexOf(PipelineConstants.PIPELINE_LOOPVALUE) != -1) {
                rtn = PipelineConstants.PIPELINE_LOOPVALUE;
                break;
            }
        }
        return rtn;
    }
    
    /**
     * Returns the entity on which loops are being set or null if incorrect values are passed.  
     * @param xpathExpression is an XPath Expression from which one wants to extract
     * the entity on which values are being looped on
     * <p>
     * This value could be BLA....PIPELINE_LOOPVALUE(loop_name)...
     *  or ....PIPELINE_LOOPVALUE(concat(BLA())).... or ....PIPELINE_LOOPVALUE(loop_name))....
     * 
     * @param loopStr is one of PIPELINE_LOOPVALUE or PIPELINE_LOOPON
     */
    public static String getExpressionWhichIsBeingLooped(String xpathExpression, String loopStr, char openingChar,char closingChar) throws PipelineException {
        String rtn = null;
        if (xpathExpression == null || loopStr == null) return rtn;
        int indexOfLoopOn = xpathExpression.indexOf(loopStr);
        if (indexOfLoopOn == -1) throw new PipelineException("Couldnt locate presence of " + loopStr + " in " + xpathExpression);
        /* Stores the start index of ( as key and end index of matching ) as value */
        Hashtable startKeyEndValueHash = new Hashtable();
        /* Traverse through the string looking for (. Put the ( in the stack, 
         * as you see a ) pop from the stack and insert position index
         * into startKeyEndValueHash
         */
        Stack openingCharStack = new Stack();
        Stack openingIndexStack = new Stack();
        String openingCharStr = new String(""+openingChar);
        String closingCharStr = new String(""+closingChar);
        for (int i = 0; i < xpathExpression.length(); i++) {
            if (xpathExpression.charAt(i)==openingChar) {
                openingCharStack.push(openingCharStr);
                openingIndexStack.push(new Integer(i));
            }else if (xpathExpression.charAt(i)==closingChar) {
                if (openingCharStack.empty()) throw new PipelineException("Expression " + xpathExpression + " has mismatched " + openingChar + " and " + closingChar + "....possibly no " + openingChar);
                if (!openingCharStack.peek().equals(openingCharStr)) throw new PipelineException("Expression " + xpathExpression + " has mismatched " + openingChar + " and " + closingChar);
                else {
                    openingCharStack.pop();
                    Integer start = (Integer)openingIndexStack.pop();
                    startKeyEndValueHash.put(start,new Integer(i));
                }
            }
        }
 /*       Enumeration enume = startKeyEndValueHash.keys();
        while (enume.hasMoreElements()) {
            Integer key = (Integer)enume.nextElement();
            System.out.println("Key + " + key + " value " + startKeyEndValueHash.get(key));
        }*/
        if (!openingCharStack.empty()) throw new PipelineException("Expression " + xpathExpression + " has mismatched " + openingChar + " and " + closingChar + " ....possibly excess " + openingChar);
        int startIndex  = indexOfLoopOn + loopStr.length();
        int endIndex = ((Integer)startKeyEndValueHash.get(new Integer(startIndex))).intValue();
        rtn = xpathExpression.substring(startIndex+1, endIndex);
        return rtn;
    }
    
    public static void copyLoops(PipelineDocument from, PipelineDocument to) {
        if (from == null || to == null) 
            return;
        PipelineData fromPipeline = from.getPipeline();
        PipelineData toPipeline = to.getPipeline();
        if (fromPipeline == null || toPipeline == null)
            return;
        if (!(fromPipeline.sizeOfLoopArray() > 0)) return;
        for (int i = 0; i < fromPipeline.sizeOfLoopArray(); i++) {
            Loop toLoop = toPipeline.addNewLoop();
            //toLoop = (Loop)fromPipeline.getLoopArray(i).copy();
            copy(fromPipeline.getLoopArray(i), toLoop);
        }
    }
 
    public static void copy(Loop from, Loop to) {
       to.setId(from.getId());
       if (from.isSetXpath())
           to.setXpath(from.getXpath());
       if (from.sizeOfValueArray() > 0) {
           for (int i = 0; i < from.sizeOfValueArray(); i++) {
               to.addValue(from.getValueArray(i));
           }
       }
    }
    
    public static void main(String args[]) {
        try {
            String rtn = LoopUtils.getExpressionWhichIsBeingLooped("concat/Pipeline/parameters/parameter[name='workdir']/values/unique/text(),'/',PIPELINE_LOOPON(^concat(/Pipeline/parameters/parameter[name='workdir']/values/unique/text(),'/')^)","PIPELINE_LOOPON",'(',')');
            System.out.println("I got " + rtn);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    static Logger logger = Logger.getLogger(LoopUtils.class);
    
    
    
    
}
