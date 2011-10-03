/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: CollectionUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class CollectionUtils {
    
    public static void free(Object[] arr) {
        if (arr != null && arr.length > 0)
        for (int i = 0; i < arr.length; i++ ) {
            arr[i] = null;
        }
    }

}
