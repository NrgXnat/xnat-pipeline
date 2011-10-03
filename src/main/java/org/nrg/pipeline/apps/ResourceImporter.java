/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.apps;

import org.apache.log4j.BasicConfigurator;
import org.nrg.pipeline.manager.ResourceManager;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ResourceImporter.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class ResourceImporter {

    public ResourceImporter(String args[]) {
        inFileName = null; inDirName = null;
        if (args.length <1 || args.length >7){
            showUsage();
            System.exit(1);
        }
        for(int i=0; i<args.length; i++){
            if (args[i].equalsIgnoreCase("-d") ) 
                inDirName = args[i+1];                   
            else if (args[i].equalsIgnoreCase("-o") ) {
                 outDirName = args[i+1];
            }else if (args[i].equalsIgnoreCase("-f") ) {
                 inFileName = args[i+1];
            }
        }
        
    }

    public void fetch() {
        if (inDirName==null && inFileName == null) {
            showUsage();
            System.exit(1);
        }
        ResourceManager.GetInstance().importResources(inDirName, inFileName, outDirName);
    }
    
    
    private void showUsage() {
        System.out.println(getUsage());
    }

    private String getUsage() {
        String usage = "Usage: ResourceImporter [-d <directory containing executables>] [-f <Optional: single executable in directory specified by -d>] -o <directory into which xml representation of the Executables will be inserted>\n";
        usage += " NOTE: atleast one of -d or -f is required\n";
        return usage;
    }   

    public static void main(String args[]) {
        BasicConfigurator.configure();
        ResourceImporter rscImport = new ResourceImporter(args);
        rscImport.fetch();
    }
    
    String inFileName;
    String inDirName;
    String outDirName;
}
