/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.manager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.process.LocalProcessLauncher;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.FileUtils;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ResourceDocument;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step.Resource;
import org.nrg.pipeline.xmlreader.XmlReader;

//////////////////////////////////////////////////////////////////////////
//// ClassName: ResourceManager
/**
 ResourceManager maintains java representation (class org.nrg.pipeline.xmlbase.ResourceData) of Resource XML documents. 
 Query the ResourceManager to get the java object by passing the location+name string  
 
 @author mohanar
 @version $Id: ResourceManager.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class ResourceManager {
    	
    private ResourceManager() {
    }
    
    public static ResourceManager GetInstance() {
        if (self == null) {
            self = new ResourceManager();
        }
        return self;
    }
    
    
    /** getResource
     * If the ResourceData object representing the XML file at 
     * location / name.xml is already available return that 
     * else read the xml file and return the resourceData object 
     * @param location
     * @param name
     * @return
     */
    
    public ResourceDocument getResource(Resource resource) {
        return getResource(resource.getLocation(), resource.getName());
    }
    
//    public ResourceData getResource(String location, String name, boolean isSystemCall) {
    public ResourceDocument getResource(String location, String name) {
        String path = FileUtils.getAbsolutePath(location,name);
        return readResource(path);  
    }

    
    /**
     * importResources gets the listing of all executables in a directory
     * specified dirName and creates ResourceXml's in the directoyr specified by 
     * outDir
     * 
     * 
     * @param dirName
     */
    public void importResources(String dirName, String fileName, String outDir) {
        String[] children = null;
        if (dirName != null) {
            File dir = new File(dirName);
            if (! dir.isDirectory() || !dir.exists() ) {
                System.out.println("Dir " + dirName + " doesnt exist");
                System.exit(1);
            }
            if (fileName == null) {
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return (!name.startsWith(".") && !name.endsWith(".bin"));
                    }
                };
                children = dir.list(filter);
            }else {
                children = new String[]{fileName};
            }
        }else {
            children = new String[]{fileName};
        }
        
        if (children == null ) {
            System.out.println("Couldnt find any executables in " + dirName);
            System.exit(1);
        }
        if (children.length == 0 ) {
            System.out.println("Couldnt find any executable satisfying the filter" );
            System.exit(1);
        }
        File outFolder = new File(outDir);
        if (!outFolder.exists()) outFolder.mkdirs(); 
        
        
        //BufferedWriter out = null;
        //BufferedWriter scriptDescriptor = null;
        //try {
        //    out = new BufferedWriter(new FileWriter("resources.list", true));
        //    scriptDescriptor = new BufferedWriter(new FileWriter("resources.txt", true));
        //    out.write("\n*******************************************\n");
        //    out.write("Resources found at " + dirName +"\n");
        //    out.write("File specified " + fileName + "\n");
        //    out.write("Xml representation written to  " + outDir);
        //    out.write("\n*******************************************\n");
        //}catch(IOException ioe) {
        //    System.out.println(ioe.getLocalizedMessage());
        //    System.exit(1);
        //}
        for (int i = 0; i < children.length; i++ ) {
                try {
                   // out.write(dirName + File.separator + children[i] + "   ");
                    String path =  children[i];
                    if (dirName != null ) {
                        path = dirName + File.separator + path;
                    }
                   // if (new File(path).isFile()) {
                        ResourceDocument rscDoc = ResourceDocument.Factory.newInstance();
                        ResourceData rscData = rscDoc.addNewResource();
                        rscData.setName(children[i]);
                        if (dirName != null)rscData.setLocation(dirName);
                        rscData.setType(ResourceData.Type.EXECUTABLE);

                        LocalProcessLauncher processLauncher = new LocalProcessLauncher(null, null);
                        processLauncher.launchProcess(new CommandStatementPresenter(path),null,3000);
                        String desc = processLauncher.getStreamOutput();
                         if (processLauncher.getStreamErrOutput()!=null){
                           desc += " " + processLauncher.getStreamErrOutput();
                        }
                        rscData.setDescription(desc); 
                        rscDoc.save(new File(outDir + File.separator + rscData.getName() + ".xml"), new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
                        //scriptDescriptor.write(rscData.getLocation() + File.separator + rscData.getName() + "\n");
                        //scriptDescriptor.write(rscData.getDescription());
                        //scriptDescriptor.write("\n\n");
                        logger.info("Imported " + path + " to " + outDir + File.separator + rscData.getName() + ".xml" );
                    //}else System.out.println("Couldnt find file " + path);
                }catch(IOException e) {
                    logger.fatal(e.getClass() + "==>" + e.getLocalizedMessage());
                    //try {
                    //    out.write(" skipped ");
                    //}catch(IOException ioe) {
                    //}
                    //System.out.println("Couldnt launch");
                    //System.exit(1);
               }// catch(InterruptedException e) {
                //   logger.fatal(e.getClass() + "==>" + e.getLocalizedMessage());
                //   if (outputGobbler != null) outputGobbler.finish();
                //   System.out.println("Couldnt launch due to " + e.getClass());
                //  System.exit(1);
              // }
               // try {out.write("\n");}catch(IOException ioe) {}
                catch(PipelineEngineException pe) {
                    logger.debug("Encountered exception " + pe.getMessage());
                    System.out.println("Encountered exception " + pe.getMessage());
                    System.exit(1);
                }
        }
        //try {
           // if (out != null) out.close();
            //if (scriptDescriptor != null) scriptDescriptor.close();
       // }catch(IOException ioe) {
       // }

    }

    
    private ResourceDocument readResource(String directoryOrFilename) {
        ResourceDocument rscDoc = null;
        File dir = new File(directoryOrFilename);
        String[] children = null;
        if (dir.isDirectory()) {
            // Accept only xml files from this folder
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            };
            children = dir.list(filter);
        }else if (dir.isFile()){
           children = new String[1];
           children[0] = directoryOrFilename;
        }
        if (children == null) {
            return null;
        } else {
                for (int i=0; i<children.length; i++) {
                    String filename = null;
                    if (dir.isDirectory())
                        filename = directoryOrFilename + File.separator + children[i];
                    else 
                        filename = children[i];
                    try {
                        XmlObject xmlObject = new XmlReader().read(filename);
                        if ((xmlObject instanceof ResourceDocument)) {
                            logger.info("Loaded " + filename);
                            return (ResourceDocument)xmlObject;
                        }
                    }catch(IOException ioe) {
                        logger.error("File not found " + filename);
                        return null;
                    }catch (XmlException xmle ) {
                        logger.error(xmle.getLocalizedMessage());
                        return null;
                    }
                }
        }
        return rscDoc;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                         ///          
    private static ResourceManager self = null;
    static Logger logger = Logger.getLogger(ResourceManager.class);

    
}
