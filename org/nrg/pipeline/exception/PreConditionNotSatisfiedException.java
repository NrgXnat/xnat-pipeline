/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.exception;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PreConditionNotSatisfiedException.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class PreConditionNotSatisfiedException extends Exception {
    public PreConditionNotSatisfiedException(String msg) {
        super(msg);
    }
}
