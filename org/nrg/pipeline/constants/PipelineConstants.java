/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.nrg.pipeline.manager.PipelineManager;

//////////////////////////////////////////////////////////////////////////
//// ClassName: PipelineConstants
/**
 Constants for the pipeline.

 @author mohanar
 @version $Id: PipelineConstants.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineConstants {
    
    public static  Hashtable logicalOperators = new Hashtable();
    public static  final String PIPELINE_LOOPON = "PIPELINE_LOOPON";
    public static final String PIPELINE_LOOPVALUE = "PIPELINE_LOOPVALUE";
    public static final String PIPELINE_XPATH_MARKER = "^";
    public static final String PIPELINE_NAMESPACE_DECL =
        "declare namespace pipeline='http://nrg.wustl.edu/pipeline';";
    public static final String PIPELINE_NAMESPACE = "pipeline";
    public static final String PIPELINE_PARAM_REGEXP = "/Pipeline/parameters/parameter[name=";
    public static final String PWD_PARAMETER ="pwd";
    public static final char openingBracket = '(';
    public static final char closingBracket = ')';
    public static final String openingBracketStr = "(";
    public static final String closingBracketStr = ")";
    public static final String STEP_ID_SEPARATOR = ":";


    
    public static  Hashtable getLogicalOperators() {
        if (logicalOperators.isEmpty() || logicalOperators.size() == 0) {
            logicalOperators.put("AND","&&"); 
            logicalOperators.put("OR","||");
        }
        return logicalOperators;
    }
    
    
}
