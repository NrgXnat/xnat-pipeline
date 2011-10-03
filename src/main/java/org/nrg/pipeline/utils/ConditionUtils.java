/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.PipelineData;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ConditionUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class ConditionUtils {
    public static Boolean checkCondition(PipelineData pipelineData, String condition, boolean debug) throws PipelineEngineException {
        boolean rtn = false;
        String expr = "";
        logger.info("Condition: " + condition);
        if (debug) return new Boolean(true);
        if (condition == null || condition.equalsIgnoreCase("true")) return new Boolean(true);
        if (condition != null && condition.equalsIgnoreCase("false")) return new Boolean(rtn);
        StringTokenizer st = new StringTokenizer(condition);
        
        ArrayList resolvedTokens = new ArrayList();
        while (st.hasMoreTokens()) {
            rtn = false;
            String token = st.nextToken();
            if (PipelineConstants.getLogicalOperators().containsKey(token) ) {
                resolvedTokens.add(PipelineConstants.getLogicalOperators().get(token));
                continue;
            }
            if (token.startsWith("EXISTS")) {
               if (!debug) rtn = FileUtils.fileExists(token);
            }else if (token.startsWith("!EXISTS")) {
                if (!debug) rtn = !FileUtils.fileExists(token.substring(1));
            }else if (token.equalsIgnoreCase("TRUE")) {
                 rtn = true;
            }else if (token.equalsIgnoreCase("FALSE")) {
                 rtn = false;
            }else {
                EvaluateConditionals c = new EvaluateConditionals();
                rtn = c.getResult(pipelineData,token);
            }
            resolvedTokens.add(rtn?new Integer(1):new Integer(0));
        }
        for (int i = 0; i < resolvedTokens.size(); i++) {
            expr += " " + resolvedTokens.get(i) + " ";
        }
        EvaluateConditionals c = new EvaluateConditionals();
        rtn = c.getResult(expr.trim());
        
        logger.info("Evaluated to  " + rtn);
        return new Boolean(rtn);
    }
    

    static Logger logger = Logger.getLogger(ConditionUtils.class);

    public static void main(String[] args) {
    	String condition = "";
    }
}
