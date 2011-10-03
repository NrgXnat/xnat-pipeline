/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

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
    public TestIfConditional() {
//      Set up the parser (more initialization in parseExpression()) 
        myParser = new JEP();
        myParser.initFunTab(); // clear the contents of the function table
        myParser.addStandardFunctions();
        myParser.setTraverse(true);
        System.out.println("Constructor called");
    }
       
    private void parseExpression(String expr) {
        System.out.println("Expr is " + expr);
        myParser.initSymTab(); // clear the contents of the symbol table
        myParser.addStandardConstants();
        myParser.addComplex(); // among other things adds i to the symbol table
        //myParser.addVariable("a", a);
        myParser.parseExpression(expr);
    }
    
    private void updateResult() {
        Object result;
        String errorInfo;
        
        // Get the value
        result = myParser.getValueAsObject();
        
        // Is the result ok?
        if (result!=null) {
            System.out.println(result.toString());
        } else {
            System.out.println("Result is null");
        }
        
        // Get the error information
        if ((errorInfo = myParser.getErrorInfo()) != null) {
            System.out.println(errorInfo);
        } else {
            System.out.println("Error is null");
        }
  
    }

    public static void main(String[] args) {
        System.out.println("Expre is ");
        String conditionalStr = "1 && 0 || 1";
        TestIfConditional t = new TestIfConditional();
        t.parseExpression(conditionalStr);
        t.updateResult(); 
    }
    int a = 9;
    JEP myParser;
    
}

