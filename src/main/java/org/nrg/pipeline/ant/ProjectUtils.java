/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.ant;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.NoBannerLogger;
import org.apache.tools.ant.Project;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ProjectUtils.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class ProjectUtils {
    public static Project createProject()    {
      Project project =  new Project();
        BuildLogger logger =  new NoBannerLogger();
      logger.setMessageOutputLevel(
        org.apache.tools.ant.Project.MSG_INFO);
      logger.setOutputPrintStream(System.out);
      logger.setErrorPrintStream(System.err);

      project.init();
      project.getBaseDir();
      project.addBuildListener(logger);
      return project;
    }
}
