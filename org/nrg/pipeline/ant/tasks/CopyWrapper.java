/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.ant.tasks;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.nrg.pipeline.ant.ProjectUtils;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: CopyWrapper.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class CopyWrapper {
    
    String src;
    String destination;
    boolean overwrite;
    
    public CopyWrapper(String args[]) {
        overwrite = false;
        int c;
        LongOpt[] longopts = new LongOpt[3];
        longopts[0] = new LongOpt("src", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[1] = new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 'd');
        longopts[2] = new LongOpt("overwrite", LongOpt.NO_ARGUMENT, null, 'o');
        Getopt g = new Getopt("CopyWrapper", args, "s:d:o;", longopts, true);
        g.setOpterr(false); // We'll do our own error handling
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 's':
                    src = g.getOptarg();
                    break;
                case 'd':
                    destination = g.getOptarg();
                    break;
                case 'o':
                    overwrite = true;
                    break;
            }
        }
    }
    

    public void execute() { //throws BuildException {
        Project project = ProjectUtils.createProject();
        Copy copy = new Copy();
        copy.setProject(project);
        copy.setTaskName("copy");
        File fromDir =  new File(src);
        FileSet fsFrom = new FileSet();
        fsFrom.setDir(fromDir);
        copy.setTodir(new File(destination));
        copy.addFileset(fsFrom);
        if (overwrite) copy.setOverwrite(true);
        copy.perform();
        
    }
    
    public static void printUsage() {
        System.out.println("AntCopy options:");
        System.out.println("-src: source path to copy from");
        System.out.println("-dest: destination path to copy to");
        System.out.println("-overwrite: overwrite existing file");
    }
    
    public static void main(String args[]) {
        int exitStatus = 0;
        try {
            CopyWrapper copy = new CopyWrapper(args);
            copy.execute();
            System.out.println("Done Copying");
        }catch(Exception e) {
            System.out.println("Failed to copy ..." + e.getMessage());
            CopyWrapper.printUsage();
            exitStatus = 1;
        }
        System.exit(exitStatus);
    }
}
