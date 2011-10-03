/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PipelineException.java,v 1.2 2009/11/11 21:03:41 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineException extends Exception {
	
	private String errorFileName;
	private String outputFileName;
    
    public PipelineException(String msg, Exception e) {
        super(msg + " " + e.getMessage());
        logger.debug(msg);
        logError(e);
    }

    
    public PipelineException(Exception e) {
        super(e.getMessage());
        logError(e);
    }

    public PipelineException (String msg) {
        super(msg);
    }
    
    private void logError(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        logger.debug(sw.toString());
        sw=null;

    }
    
    static Logger logger = Logger.getLogger(PipelineException.class);

	public String getOutputFileName() {
		return outputFileName;
	}


	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}


	public String getErrorFileName() {
		return errorFileName;
	}


	public void setErrorFileName(String errorFileName) {
		this.errorFileName = errorFileName;
	}
}