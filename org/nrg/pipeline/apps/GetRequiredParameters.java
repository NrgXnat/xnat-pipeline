/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.apps;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.utils.ParameterUtils;
import org.nrg.pipeline.utils.PipelineUtils;
//import org.nrg.pipeline.xmlbeans.ParameterDocument;
import org.nrg.pipeline.xmlbeans.PipelineDocument;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: GetRequiredParameters.java,v 1.1 2009/09/02 20:28:19 mohanar Exp $
 @since Pipeline 1.0
 */

public class GetRequiredParameters {
    
    public GetRequiredParameters(String args[]) {
        pipelineFileName = null;
        outFileName = null;
        if (args.length <1 || args.length >7){
            showUsage();
            System.exit(1);
        }
        for(int i=0; i<args.length; i++){
            if (args[i].equalsIgnoreCase("-p") ) 
                pipelineFileName = args[i+1];                   
            else if (args[i].equalsIgnoreCase("-o") ) {
                 outFileName = args[i+1];
            }else if (args[i].equalsIgnoreCase("-a") ) {
                 all = true;
            }

        }
    }

    public void saveParameterDocument() throws PipelineException, IOException{
       PipelineDocument pipelineDoc = PipelineUtils.getPipelineDocument(pipelineFileName);
       System.out.println("Loaded pipeline XML " + pipelineFileName);
       //ParameterDocument param = ParameterUtils.getParameterDocument(pipelineDoc, all);
      // if (param != null) {
       //    param.save(new File(outFileName), new XmlOptions().setSavePrettyPrint());
       //}
    }
    
    public String getOutFileName() {
        return outFileName;
    }
    private void showUsage() {
        System.out.println(getUsage());
    }

    private String getUsage() {
        String usage = "Usage: GetRequiredParameters -p <pipeline file> -o <output parameter file> -a <Optional: all parameters>\n";
        return usage;
    }   

    public static void main(String args[]) {
        BasicConfigurator.configure();
        GetRequiredParameters grParam = new GetRequiredParameters(args);
        try {
            if (grParam.getOutFileName() == null) {
                System.out.println("Missing out file name");
                grParam.showUsage(); System.exit(1);
            }
            grParam.saveParameterDocument();
        }catch(PipelineException pe) {
            System.out.println(pe.getClass() + "==>" + pe.getLocalizedMessage());
            System.exit(1);
        }catch(IOException pe) {
            System.out.println("Couldnt save parameter file to " + grParam.getOutFileName());
            System.out.println(pe.getClass() + "==>" + pe.getLocalizedMessage());
            System.exit(1);
        }
    }
    
    String pipelineFileName;
    String outFileName;
    boolean all = false;
}
