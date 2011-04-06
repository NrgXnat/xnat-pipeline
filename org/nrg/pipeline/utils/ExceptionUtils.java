/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.pipeline.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

public class ExceptionUtils {
    public static final Object getStackTrace(Exception e, String rtnType) {
        ByteArrayOutputStream byteStream = null;
        PrintWriter printWriter = null;
        String stackTrace = null;
        Vector stackArray = new Vector();
    
        byteStream = new ByteArrayOutputStream();
        printWriter = new PrintWriter(byteStream, true);
    
        e.printStackTrace(printWriter);
    
        printWriter.flush();
    
        stackTrace = byteStream.toString();
    
        printWriter.close();
    
        if (rtnType != null && rtnType.equalsIgnoreCase("vector")) {
            StringTokenizer tok = new StringTokenizer(stackTrace,"\n");
            while(tok.hasMoreTokens()){
                String s = tok.nextToken();
                stackArray.add(s);
            }
            return(stackArray);
        }else 
            return stackTrace;
    }
}
