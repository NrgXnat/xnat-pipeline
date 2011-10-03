/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.io.File;
import java.io.FilenameFilter;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: FileNameMatcher.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class FileNameMatcher implements FilenameFilter {

    public FileNameMatcher(String pattern) {
        this.pattern = pattern;
    }
    
    public boolean accept(File dir, String name) {
        return name.startsWith(pattern);
    }

    String pattern;
}
