/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.nrg.pipeline.constants.PipelineConstants;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: XPathUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class XPathUtils {
    
    public static String setNameSpace(String xPathExpression, boolean preFixDeclStmt) {
        String rtn = xPathExpression;
        if (xPathExpression.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && xPathExpression.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
            rtn = xPathExpression.substring(1,xPathExpression.length()-1);
            String temp  = setNameSpaceForFunction(rtn); 
            if ( temp != null) {
                rtn = temp;
                if (preFixDeclStmt) rtn = PipelineConstants.PIPELINE_NAMESPACE_DECL +  rtn;
                return rtn;
            }
            String nameSpacePrefix = "/" + PipelineConstants.PIPELINE_NAMESPACE + ":";
            StringTokenizer st = new StringTokenizer(rtn,"/");
            rtn = "";
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (!isFunction(token)) {
                    rtn += nameSpacePrefix + token; 
                }else {
                    temp = setNameSpaceForFunction(token);
                    rtn += "/" + temp;
                }
            }
            rtn = rtn.replaceAll("\\[\\s*(\\w)", "["+PipelineConstants.PIPELINE_NAMESPACE + ":$1");
            if (preFixDeclStmt) rtn = PipelineConstants.PIPELINE_NAMESPACE_DECL + "$this" + rtn;
        }
        //logger.debug("setNameSpace():: " + rtn);
        return rtn;
    }
    
    private static boolean isFunction(String xPathExpression) {
        boolean rtn = false;
        Pattern startingPattern = Pattern.compile("^\\w");
        Matcher startingPatternMatcher = startingPattern.matcher(xPathExpression);
        if (startingPatternMatcher.find()) {
            Pattern functionPattern = Pattern.compile("\\w+\\((.*)\\)");
            Matcher functionPatternMatcher = functionPattern.matcher(xPathExpression);
            rtn = functionPatternMatcher.find();
        }
        return rtn;
    }
    
    private static String setNameSpaceForFunction(String xPathExpression) {
        String rtn = null;
        //logger.debug("setNameSpaceForFunction():: Parsing " + xPathExpression);
        try {
            Pattern startingPattern = Pattern.compile("^\\w");
            Matcher startingPatternMatcher = startingPattern.matcher(xPathExpression);
            if (startingPatternMatcher.find()) {
                Pattern functionPattern = Pattern.compile("^(\\w+)\\((.*)\\)$");
                Matcher functionPatternMatcher = functionPattern.matcher(xPathExpression);
                if (functionPatternMatcher.find()) {
                    String functionName = functionPatternMatcher.group(1);
                    String functionArguments = functionPatternMatcher.group(2);
                    //logger.debug("setNameSpaceForFunction():: matched " +  functionName + ":" + functionArguments + ":");
                    if (functionArguments != null && functionArguments != "" && functionArguments != ")") {
                        StringTokenizer st = new StringTokenizer(functionArguments,",");
                        String fArg = "";
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();
                            if (!isFunction(token)) {
                                if (!token.startsWith("'"))
                                    fArg +=   setNameSpace("$" + token + "$", false) + ",";
                                else 
                                    fArg += token + ",";
                            }else 
                                fArg +=   setNameSpace("$" + token + "$", false) + ",";
                        }
                        if (fArg.endsWith(",")) functionArguments = fArg.substring(0,fArg.length()-1);
                        rtn = functionName + "(" + functionArguments + ")";
                    }
                }
            }
        }catch(IllegalStateException ise) {
        }
       // logger.debug("setNameSpaceForFunction():: Returing " + rtn);
        return rtn;
    }
    
    public static XmlObject[] executeQuery(XmlObject xmlDoc, String queryExpression) {
        XmlObject[] rtn = null;
        if (queryExpression.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && queryExpression.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
            queryExpression = setNameSpace(queryExpression,true) ;
            rtn = xmlDoc.selectPath(queryExpression);
        }else {
            rtn = new XmlString[1];
            XmlString xmlString = XmlString.Factory.newInstance();
            xmlString.setStringValue(queryExpression);
            rtn[0] = xmlString;
        }
        //logger.debug("executeQuery():: Query matched " + rtn.length + "  elements ");
        //System.out.println("Query returned " + rtn[0].xmlText());
        return rtn;
    }
    
    static Logger logger = Logger.getLogger(XPathUtils.class);
}
