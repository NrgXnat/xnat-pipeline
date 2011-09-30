/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;

import org.nfunk.jep.JEP;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xpath.XPathResolverSaxon;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: EvaluateConditionals.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class EvaluateConditionals {

    private void parseExpression(String expr) {
        myParser.initSymTab(); // clear the contents of the symbol table
        myParser.addStandardConstants();
        myParser.addComplex(); // among other things adds i to the symbol table
        //myParser.addVariable("a", a);
        myParser.parseExpression(expr);
    }

    public boolean getResult(PipelineData pipelineData, String expr) throws PipelineException {
        boolean rtn = false;
        try {
            ArrayList stms = new ArrayList(); stms.add(expr);
            ArrayList values = XPathResolverSaxon.GetInstance().resolveXPathExpressions(stms,pipelineData);
        }catch (Exception e) {
            return getResult(expr);
        }
        return rtn;
    }

    
    public boolean getResult(String expr) throws PipelineException {
        boolean rtn = false;
        Object result;
        String errorInfo;
//      Set up the parser (more initialization in parseExpression()) 
        myParser = new JEP();
        myParser.initFunTab(); // clear the contents of the function table
        myParser.addStandardFunctions();
        //myParser.setTraverse(true);

        parseExpression(expr);
        // Get the value
        result = myParser.getValueAsObject();
        // Get the error information
        if ((errorInfo = myParser.getErrorInfo()) != null) {
            throw new PipelineException(errorInfo);
        } 
        // Is the result ok?
        if (result!=null) {
            rtn = result.toString().equals("1.0");
        }
        return rtn;
    }

    
    JEP myParser;
}
