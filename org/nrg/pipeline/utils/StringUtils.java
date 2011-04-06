/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nrg.pipeline.constants.PipelineConstants;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: StringUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class StringUtils {
    
    
    public static Hashtable getParameterName(String expr, Hashtable existing) {
        Hashtable rtn = null;
        if (!expr.startsWith("$")) return rtn;
        int countOccurrences = org.apache.commons.lang.StringUtils.countMatches(expr,PipelineConstants.PIPELINE_PARAM_REGEXP);
        if (countOccurrences == 0) return rtn;
        rtn = new Hashtable();
        for (int i = 0; i < countOccurrences; i++) {
            int index = org.apache.commons.lang.StringUtils.ordinalIndexOf(expr,PipelineConstants.PIPELINE_PARAM_REGEXP,i);
            if (index != -1) {
                String matchedExpr = expr.substring(index);
                addParamName(matchedExpr, rtn, existing);
            }
        }
        return rtn;
    }
    
    private static void addParamName(String expr, Hashtable paramInfo, Hashtable existing) {
        String rtn = null;
        Pattern p = Pattern.compile("^" + PipelineConstants.PIPELINE_PARAM_REGEXP+"\\s+'(*)'\\s+]");
        Matcher m = p.matcher(expr);
        boolean found = m.find();
        if (found) {
            rtn = m.group(1);
            Pattern p1 = Pattern.compile("^" + PipelineConstants.PIPELINE_PARAM_REGEXP+"\\s+'(*)'\\s+]/value/text\\(\\)");
            Matcher m1 = p1.matcher(expr);
            if (m1.find()) {
                if (!existing.containsKey(rtn))
                    paramInfo.put(rtn,new Boolean("false"));
            }else { // is multi-valued
                if (!existing.containsKey(rtn)) 
                    paramInfo.put(rtn,new Boolean("true"));
            }
        }
        
    }
    
    public static ArrayList transformOutputExpression (String xPathExpression) {
        ArrayList rtn = new ArrayList();
        xPathExpression = xPathExpression.trim();
        if (xPathExpression.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && xPathExpression.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
            rtn.add(xPathExpression.substring(1,xPathExpression.length()-1));
        }
        return rtn;
    }
    
    public static ArrayList transformOutPutExpression(ArrayList stmts) {
        ArrayList rtn = new ArrayList();
        if (stmts != null) {
            for (int i = 0; i < stmts.size(); i++) {
                if (((String)stmts.get(i)).startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && ((String)stmts.get(i)).endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
                    rtn.add(((String)stmts.get(i)).substring(1,((String)stmts.get(i)).length()-1));
                }else 
                    rtn.add(stmts.get(i));
            }
        }
        return rtn;
    }
    
    public static String  getAsString (Object[] list) {
        String rtn = "";
        if (list == null) return rtn;
        for (int i = 0; i < list.length; i++) {
            rtn += list[i] + "  ";
        }
        return rtn;
    }
    
    public static String afterLastIndexofOrStr(String str, String searchStr) {
        String rtn = str;
        //System.out.println("String recd " + str);
        if (str != null) {
            int lindex = str.lastIndexOf(searchStr);
            if (lindex != -1) {
                rtn = str.substring(lindex+1);
            }
        }
        return rtn;
    }
    
    public static String afterLastSlash(String str) {
        return afterLastIndexofOrStr(str,"/");
    }
    
    public static void main(String[] args) {
        String str="*data*cninds01*data2*LSIIC.4dfp.img/";
        System.out.println("Str is: " + StringUtils.afterLastIndexofOrStr(str,"/"));
    }
    
}
