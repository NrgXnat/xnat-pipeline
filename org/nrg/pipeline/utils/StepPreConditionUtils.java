/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xpath.XPathResolver;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: StepPreConditionUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class StepPreConditionUtils {
    public static String resolvePreCondition(PipelineData pipelineData,  String preCondition) throws PipelineEngineException {
        String rtn = "";
      // logger.info("Recd condition " + preCondition);
        ArrayList resolvedTokens = new ArrayList();
        if (preCondition.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && preCondition.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
            try {
                ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineData, preCondition);
                if (values == null || !(values.size() > 0)) {
                    throw new PipelineEngineException("Couldnt resolve precondition  " + preCondition);
                }
                return (String)values.get(0);
            } catch(TransformerException te) {
                throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
            }
        }
        StringTokenizer st = new StringTokenizer(preCondition);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            //logger.debug("Token " + token);
            if (PipelineConstants.getLogicalOperators().containsKey(token) ) {
                resolvedTokens.add(token);
             //   logger.debug("Added into the array and next");
                continue;
            }else {
                if (token.startsWith("EXISTS")) {
                    String xPathExpression = getArgumentToExists(token);
                    try {
                        ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineData,  xPathExpression);
                        if (values == null || !(values.size() > 0)) {
                            throw new PipelineEngineException("Couldnt resolve precondition  " + preCondition);
                        }
                        resolvedTokens.add("EXISTS(" + (String)values.get(0) + ")");
                    } catch(TransformerException te) {
                        throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
                    }
                }else if (token.startsWith("!EXISTS")) {
                    String xPathExpression = getArgumentToNotExists(token);
                    try {
                        ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineData,  xPathExpression);
                        if (values == null || !(values.size() > 0)) {
                            throw new PipelineEngineException("Couldnt resolve precondition  " + preCondition);
                        }
                        resolvedTokens.add("!EXISTS(" + (String)values.get(0) + ")");
                    } catch(TransformerException te) {
                        throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
                    }
                }
                else if (token.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && token.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
                    try {
                        ArrayList values = XPathResolverSaxon.GetInstance().evaluate(pipelineData, token);
                        if (values == null || !(values.size() > 0)) {
                            throw new PipelineEngineException("Couldnt resolve precondition  " + preCondition);
                        }
                        resolvedTokens.add((String)values.get(0));
                    } catch(TransformerException te) {
                        throw new PipelineEngineException(te.getClass() + "==>" + te.getLocalizedMessage(), te);
                    }
                }else {
                    //logger.debug("         Adding " + token);
                    //token = org.apache.commons.lang.StringUtils.replace(token,"\\$","$");
                    resolvedTokens.add(token);
                }
            }
            
        }
        for (int i = 0; i < resolvedTokens.size(); i++) {
            rtn += " " + resolvedTokens.get(i) + " ";
        }
        rtn = rtn.trim(); 
       logger.info(preCondition + " resolved to " + rtn);
   return rtn;
    }
    
    static Logger logger = Logger.getLogger(StepPreConditionUtils.class);
    
   
    
    private static String getArgumentToExists(String condition) {
        String rtn = condition;
        if (condition.startsWith("EXISTS")) {
            rtn  = rtn.substring(7,rtn.length()-1);
        }
        return rtn;
    }
    
    private static String getArgumentToNotExists(String condition) {
        String rtn = condition;
        if (condition.startsWith("!EXISTS")) {
            rtn  = rtn.substring(8,rtn.length()-1);
        }
        return rtn;
    }

}
