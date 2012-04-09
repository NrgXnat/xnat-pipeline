/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.nfunk.jep.JEP;


//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 TestIfConditional
 Test to see if the conditional specified as a string can be evaluated
 
 @author mohanar
 @version $Id: TestIfConditional.java,v 1.2 2006/08/08 21:10:16 capweb Exp $
 @since Pipeline 1.0
 */

public class TestIfConditional {
	@Before
    public void initialize() {
		// Set up the parser (more initialization in parseExpression()) 
        myParser = new JEP();
        myParser.initFunTab(); // clear the contents of the function table
        myParser.addStandardFunctions();
        myParser.setTraverse(true);
        _log.debug("Initialization method called");
    }
       
    private void parseExpression(String expression) {
        _log.debug("Expression is: " + expression);
        myParser.initSymTab(); // clear the contents of the symbol table
        myParser.addStandardConstants();
        myParser.addComplex(); // among other things adds i to the symbol table
        //myParser.addVariable("a", a);
        myParser.parseExpression(expression);
    }

    @Test
    public void testExpression() {
        String conditionalStr = "1 && 0 || 1";
        parseExpression(conditionalStr);

        
        // Get the value
        Object result = myParser.getValueAsObject();
        assertNotNull(result);
        _log.debug("Got result: " + result);

        String errorInfo = myParser.getErrorInfo();
        assertNotNull(result);
        _log.debug("Got error info: " + errorInfo);
    }

    private static final Log _log = LogFactory.getLog(TestIfConditional.class);
    private JEP myParser;
}

