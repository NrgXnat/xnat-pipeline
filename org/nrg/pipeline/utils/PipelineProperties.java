/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PipelineProperties.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipelineProperties {
    
    private static boolean inited = false;
    public static String PIPELINE_SMTP_HOST = "artsci.wustl.edu";
    public static String PIPELINE_EMAIL_ID = "PipelineRunner@nrg.wustl.edu";
    public static String PIPELINE_ADMIN_EMAIL_ID = null;
    public static String PIPELINE_CATALOG_ROOT_PATH = null;
        
    public static void init(String configFilePath) throws Exception {
        if (!inited) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(configFilePath));
            init(properties);
        }
    }
    
    public static void init(Properties properties) {
        if (properties != null && properties.size() > 0) {
            String prop = properties.getProperty("PIPELINE_EMAIL_ID");
            if (prop != null)
                PIPELINE_EMAIL_ID = prop;
            prop = properties.getProperty("PIPELINE_SMTP_HOST");
            if (prop != null)
                PIPELINE_SMTP_HOST = prop; 
            prop = properties.getProperty("PIPELINE_ADMIN_EMAIL");
            if (prop != null) PIPELINE_ADMIN_EMAIL_ID = prop;
            String catalog_path = properties.getProperty("PIPELINE_CATALOG_ROOT_PATH");
            if (catalog_path != null) {
            	PIPELINE_CATALOG_ROOT_PATH = catalog_path;
            	if (!PIPELINE_CATALOG_ROOT_PATH.endsWith(File.separator)) {
            		PIPELINE_CATALOG_ROOT_PATH += File.separator;
            	}
            }
            inited= true;
        }
    }
}
