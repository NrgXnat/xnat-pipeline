/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.manager;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LoopManager.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

/* 
 *  Copyright Washington University in St Louis 2006
 *  All rights reserved
 *  
 */

import java.util.Hashtable;

import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.xmlbeans.Loop;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineDocument;

//////////////////////////////////////////////////////////////////////////
//// ClassName: LoopManager
/**
 LoopManager keeps track of all the loop definitions and their resolved values. A pipeline can have many loops. 
 A hashtable with key of pipeline name stores a hashtable of loops as values.
 The hashtable of loops is indexed by the loop id's and the values in this 
 hashtable is a loop object. @see org.nrg.pipeline.xmlbase.Loop 
 

 @author mohanar
 @version $Id: LoopManager.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class LoopManager {
    
    //////////////////////////////////////////////////////////////////////////
    ////                        public methods                            ///          
    

    public static LoopManager GetInstance() {
        if (self == null) {
            self = new LoopManager();
        }
        return self;
    }
   
    /*
     * Convenience method to resolve the xpath expressions
     * for each loop and convert them to loop values
     */
    public void setLoopValues(PipelineDocument pipelineDoc) {
        PipelineData pipelineData = pipelineDoc.getPipeline();
        Loop[] loopArray = pipelineData.getLoopArray();
        if (loopArray == null || !(loopArray.length > 0 )) return;
        for (int i = 0; i < loopArray.length; i++) {
            Loop aLoop = loopArray[i];
            if (aLoop.isSetXpath()) {
                String queryExpression = "$this" + aLoop.getXpath();
                XmlObject[] queryRtn = pipelineData.selectPath(queryExpression);
                
                
            }
        }
    }

    
    
    //////////////////////////////////////////////////////////////////////////
    ////                        private methods                            ///          
    private LoopManager() {
        pipelineLoops = new Hashtable();
    }
    
    static LoopManager self;
    Hashtable pipelineLoops;
    
}
