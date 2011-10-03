package org.nrg.pipeline.ant.tasks;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.types.FileSet;
import org.nrg.pipeline.ant.ProjectUtils;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: MoveWrapper.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class MoveWrapper {
    
    String src;
    String destination;
    boolean overwrite;
    
    public MoveWrapper(String args[]) {
        overwrite = false;
        int c;
        LongOpt[] longopts = new LongOpt[3];
        longopts[0] = new LongOpt("src", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[1] = new LongOpt("dest", LongOpt.REQUIRED_ARGUMENT, null, 'd');
        longopts[2] = new LongOpt("overwrite", LongOpt.NO_ARGUMENT, null, 'o');
        Getopt g = new Getopt("MoveWrapper", args, "s:d:o;", longopts, true);
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
        Move move = new Move();
        move.setProject(project);
        move.setTaskName("move");
        File from =  new File(src);
        if (from.isDirectory()) { 
            FileSet fsFrom = new FileSet();
            fsFrom.setDir(from);
            move.addFileset(fsFrom);
        }else {
            move.setFile(from);
        }
        File dest = new File(destination);
        move.setTodir(dest);
        if (overwrite) move.setOverwrite(true);
        move.perform();
    }
    
    public static void printUsage() {
        System.out.println("AntMove options:");
        System.out.println("-src: source path to copy from");
        System.out.println("-dest: destination path to copy to");
        System.out.println("-overwrite: overwrite existing file");
    }
    
    
    public static void main(String args[]) {
        int exitStatus = 0;
        try {
            MoveWrapper move = new MoveWrapper(args);
            move.execute();
            System.out.println("Done Moving");
        }catch(Exception e) {
            System.out.println("Failed to move ..." + e.getMessage());
            MoveWrapper.printUsage();
            exitStatus = 1;
        }
        System.exit(exitStatus);
    }
}