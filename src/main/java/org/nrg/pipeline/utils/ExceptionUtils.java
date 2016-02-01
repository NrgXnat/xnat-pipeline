/* 
 *	Copyright 2016, Washington University School of Medicine.
 *	All rights reserved.
 * 	
 * 	@author Mohana Ramaratnam
 */
package org.nrg.pipeline.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

public class ExceptionUtils {
    public static Object getStackTrace(final Throwable e, final String rtnType) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try (final PrintWriter printWriter = new PrintWriter(byteStream, true)) {
            e.printStackTrace(printWriter);
            printWriter.flush();
        }

        final String stackTrace = byteStream.toString();

        final Vector<String> stackArray = new Vector<>();
        if (rtnType != null && rtnType.equalsIgnoreCase("vector")) {
            final StringTokenizer tokenizer = new StringTokenizer(stackTrace, "\n");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                stackArray.add(token);
            }
            return (stackArray);
        } else {
            return stackTrace;
        }
    }
}
