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
import org.apache.tools.ant.taskdefs.GZip;
import org.nrg.pipeline.ant.ProjectUtils;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: GZipWrapper.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class GZipWrapper {
    String src;
    final String EXTENSION = ".gz";
    final String LOGEXT = ".log";
    final String ERREXT = ".err";
    final String XMLEXT = ".xml";
    boolean deleteAfterZipping;

    public GZipWrapper(String args[]) {
        deleteAfterZipping = false;
        int c;
        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("src", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[1] = new LongOpt("delete", LongOpt.NO_ARGUMENT, null, 'd');
        Getopt g = new Getopt("GZipWrapper", args, "s:d;", longopts, true);
        g.setOpterr(false); // We'll do our own error handling
        //
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 's':
                    src = g.getOptarg();
                    break;
                case 'd':
                    deleteAfterZipping = true;
                    break;
            }
        }
    }
    
    /**
     * @return Returns the deleteAfterZipping.
     */
    public boolean isDeleteAfterZipping() {
        return deleteAfterZipping;
    }



    /**
     * @param deleteAfterZipping The deleteAfterZipping to set.
     */
    public void setDeleteAfterZipping(boolean deleteAfterZipping) {
        this.deleteAfterZipping = deleteAfterZipping;
    }
    

    public void execute() {
        execute(new File(src));
    }
    
    private void execute(File fileToZip) { 
        File[] listing = fileToZip.listFiles();
        if (listing == null || listing.length == 0) return;
        for (int i = 0; i < listing.length; i++) {
            if (listing[i].isDirectory()) execute(listing[i]);
            else gzip(listing[i]);
        }
    }

    private boolean skip (String file) {
        boolean rtn = false;
        if (file.endsWith(EXTENSION)) rtn = true;
        if (file.endsWith(LOGEXT)) rtn = true;
        if (file.endsWith(ERREXT)) rtn = true;
        if (file.endsWith(XMLEXT)) rtn = true;
        return rtn;
    }
    
    private void gzip(File file) {
        if (skip(file.getAbsolutePath())) return;
        Project project = ProjectUtils.createProject();
        GZip gzipper = new GZip();
        gzipper.setProject(project);
        gzipper.setTaskName("gzip");
        gzipper.setSrc(file);
        gzipper.setDestfile(new File(file.getAbsoluteFile() + EXTENSION));
        gzipper.execute();
        if (isDeleteAfterZipping())
            file.delete();
    }
    
    public static void printUsage() {
        System.out.println("AntGZip options:");
        System.out.println("-src: source path to zip using gzip compression");
        System.out.println("-delete: unzipped files");
    }
    
    
    public static void main(String args[]) {
        int exitStatus = 0;
        try {
            GZipWrapper zip = new GZipWrapper(args);
            zip.execute();
            System.out.println("Done zipping");
        }catch(Exception e) {
            System.out.println("Failed to zip ..." + e.getMessage());
            GZipWrapper.printUsage();
            exitStatus = 1;
        }
        System.exit(exitStatus);
    }
}
