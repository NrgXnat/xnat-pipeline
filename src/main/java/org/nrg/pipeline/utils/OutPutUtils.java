/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;

import org.nrg.pipeline.xmlbeans.OutputData;
import org.nrg.pipeline.xmlbeans.OutputData.File;
import org.nrg.pipeline.xmlbeans.OutputData.File.Path;
import org.nrg.pipeline.xmlbeans.PipelineData.Steps.Step;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedOutput;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: OutPutUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class OutPutUtils    {
    
    public static String getContent(Step rStep, String resourceName) {
        String rtn = null;
        if (rStep.sizeOfOutputArray() > 0 ) {
            for (int i=0; i < rStep.sizeOfOutputArray(); i++) {
                OutputData rOut = rStep.getOutputArray(i);
                String[] outIdParts = rOut.getId().split(":");
                if (outIdParts != null && outIdParts.length==2) {
                    if (outIdParts[0].equals(resourceName)) {
                        if (rOut.isSetFile()) {
                            File rFile = rOut.getFile();
                            if (rFile.isSetContent())
                                rtn = rFile.getContent();
                        }
                    }
                }
            }
        }
        return rtn;
    }
    
    public static  boolean selfContainedOutput(Step rStep) {
        boolean rtn = false;
        int cnt = 0;
        if (rStep.sizeOfOutputArray() > 0 ) {
            for (int i=0; i < rStep.sizeOfOutputArray(); i++) {
                OutputData rOut = rStep.getOutputArray(i);
                if (rOut.isSetFile()) {
                    File rFile = rOut.getFile();
                    if (rFile.isSetName()) cnt++;  
                }
            }
            rtn = (cnt == rStep.sizeOfOutputArray());
        }
        return rtn;
    }
    
    public static void copy(OutputData from, OutputData to) {
        to.setId(from.getId());
        if (from.isSetFile()) 
            copyFile(from,to);
        if (from.isSetValue())
            to.setValue(from.getValue());
        /*try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            from.save(out,new XmlOptions().setSavePrettyPrint());
            to = OutputData.Factory.parse(new InputSource(new ByteArrayInputStream(out.toByteArray())).getByteStream());
            out.close();
        }catch(IOException ioe) {
        }catch(XmlException xmle) {
            
        }*/
        //System.out.println("From: " + from.xmlText());
        //System.out.println("To: " + to.xmlText());
        //XMLBeansUtils.copy(from,to,false);
    }
    
    private static void copyFile(OutputData from, OutputData to) {
        if (from.isSetFile()) {
            File oldFile = from.getFile();
            File newFile = to.addNewFile();
            newFile.setXsiType(oldFile.getXsiType());
            if (oldFile.isSetName())
                newFile.setName(oldFile.getName());
            if (oldFile.isSetPath()) {
                Path newPath = newFile.addNewPath();
                newPath.setRelativePath(oldFile.getPath().getRelativePath());
                newPath.setStringValue(oldFile.getPath().getStringValue());
            }
            if (oldFile.isSetFormat()) newFile.setFormat(oldFile.getFormat());
            if (oldFile.isSetDescription()) newFile.setDescription(oldFile.getDescription());
            if (oldFile.isSetContent()) newFile.setContent(oldFile.getContent());
            if (oldFile.isSetFileCount()) newFile.setContent(oldFile.getContent());
            if (oldFile.isSetFileList()) newFile.setFileList(oldFile.getFileList());
            if (oldFile.isSetPattern()) newFile.setPattern(oldFile.getPattern());
        }
    }
   
    public static ArrayList replaceFiles(Step rStep, String pipeline_loopOn, ArrayList loopValues, int index) {
        OutputData rStepOutput = rStep.getOutputArray(index);
        //System.out.println("Recd " + pipeline_loopOn + " " + loopValues);
        ArrayList rtn = new ArrayList();
        for (int i = 0; i < loopValues.size(); i++  ) {
            OutputData newOutPut = OutputData.Factory.newInstance();
            copy(rStepOutput, newOutPut);
            newOutPut.getFile().getPath().setStringValue(org.apache.commons.lang.StringUtils.replace(newOutPut.getFile().getPath().getStringValue(), pipeline_loopOn, "'" + (String)loopValues.get(i) + "'" ) );
            newOutPut.getFile().setName(org.apache.commons.lang.StringUtils.replace(newOutPut.getFile().getName(), pipeline_loopOn, "'" + (String)loopValues.get(i) + "'"));
            rtn.add(newOutPut);
           // System.out.println(" File " + newOutPut.getFile().getName() + " " + newOutPut.getFile().getPath().getStringValue());
        }
        //System.exit(1);
        //if (loopValues.size() >0)
        //    rStep.removeOutput(index);
        return rtn;
    }
    
}
