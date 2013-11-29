/*
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 *
 */

package org.nrg.pipeline.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.process.OSInfo;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResourceData.Input.Argument;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: FileUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class FileUtils {

	 public static String filenameToURL(String filename)
	  {
	    File f = new File(filename);
	    String tmp = f.getAbsolutePath();
	    if (File.separatorChar == '\\')
	    {
	      tmp = tmp.replace('\\', '/');
	    }
	    // Note: gives incorrect results when filename already begins with file:///
	    if (!tmp.startsWith("file:///"))
	    	return "file:///" + tmp;
	    else
	    	return tmp;
	  }

	private static void maskSensitiveArguments(AllResolvedStepsDocument allResolvedStepsDocument) {
		if (allResolvedStepsDocument != null) {
			ResolvedStep[] resolvedSteps = allResolvedStepsDocument.getAllResolvedSteps().getResolvedStepArray();
			for (int i =0; i < resolvedSteps.length; i++) {
				ResolvedStep step = resolvedSteps[i];
				ResolvedResource[] rscs = step.getResolvedResourceArray();
				for (int j=0; j< rscs.length; j++) {
					ResolvedResource rsc = rscs[j];
					if (rsc.isSetInput()) {
						Argument[] args = rsc.getInput().getArgumentArray();
						for (int k=0; k< args.length; k++) {
							Argument arg = args[k];
							if (arg.isSetIsSensitive() && arg.isSetValue()) {
								arg.setValue("******");
							}
						}
					}
				}
			}
		}
	}

    public static void saveFile(File file, AllResolvedStepsDocument allResolvedStepsDocument) throws IOException {
        if (allResolvedStepsDocument != null) {
            ParameterUtils.maskPwdParameter(allResolvedStepsDocument);

            String savedFile = file.getAbsolutePath();
            try {
                allResolvedStepsDocument.save(file,new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
            }catch(Exception e) {
                ParameterData builddir = XMLBeansUtils.getParameterByName(allResolvedStepsDocument,"builddir");
                if (builddir != null) {
                   //System.out.println("buildir " + builddir.xmlText());
                   if (savedFile.startsWith(builddir.getValues().getUnique())) {
                       ParameterData archivedir = XMLBeansUtils.getParameterByName(allResolvedStepsDocument,"archivedir");
                       if (archivedir != null) {
                           savedFile = StringUtils.replace(savedFile,builddir.getValues().getUnique(),archivedir.getValues().getUnique());
                           //System.out.println("New saved file " + savedFile);
                           file = new File(savedFile);
                           if (!file.exists()) file.getParentFile().mkdirs();
                           allResolvedStepsDocument.save(file,new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
                       }
                   }
                }
            }
        }
    }



    public static String getAbsolutePath(String location, String name) {
        String absolutePath = location;
        boolean isAbsolute = IsAbsolutePath(location);
        if (!isAbsolute) {
        	absolutePath = PipelineProperties.getPipelineCatalogRootPath() + location;
        }
        if (!absolutePath.endsWith("/") && !absolutePath.endsWith(File.separator))
        	absolutePath += File.separator;
        if (name != null) {
	        if (!name.endsWith(".xml")) name += ".xml";
	           absolutePath += name;
        }
        return absolutePath;
    }

    public static String getAbsolutePath(String location, boolean appendSepChar) {
        String absolutePath = location;
        boolean isAbsolute = IsAbsolutePath(location);
        if (!isAbsolute) {
        	absolutePath = PipelineProperties.getPipelineCatalogRootPath() + location;
        }
        if (appendSepChar) {
	        if (!absolutePath.endsWith("/") && !absolutePath.endsWith(File.separator))
	        	absolutePath += File.separator;
        }
        return absolutePath;
    }

	public static boolean IsAbsolutePath(String path)	{
        if (path.startsWith("file:") || path.startsWith("http:") || path.startsWith("srb:"))    {
            return true;
        }
	    if (File.separator.equals("/"))        {
            if (path.startsWith("/"))            {
                return true;
            }else if (path.startsWith("$")) { //One can use environment variables
				return true;
			}
        }else{
            if (path.indexOf(":\\")!=-1)            {
                return true;
            }else if (path.indexOf(":/")!=-1)            {
                return true;
            }
        }
       return false;
	}

    public static boolean touchDir(String fileName) {
        boolean exists = false;
        if (fileName.startsWith("EXISTS"))
            exists = fileExists(fileName);
        else
            exists = fileExists("EXISTS(" + fileName + ")");
        if (exists) return true;
        String path = getDirectory(fileName);
        if (!path.equals(""))
            exists = new File(path).mkdirs();
        return exists;
    }

    public static String getDirectory(String path) {
        String rtn = path;
        int lastLocation = path.lastIndexOf(File.separator);
        if (lastLocation == -1) return "";
        rtn = path.substring(0, lastLocation);
        return rtn;
    }

    public static boolean fileExists(String condition) {
        boolean rtn = false;
        if (condition == null) return true;
        if (condition.startsWith("EXISTS")) {
            String fileName = condition.substring(7,condition.length()-1);
            File dir = new File(fileName);
            if (dir.exists()) rtn = true;
        }
        return rtn;
    }

    public static String getFileSeparatorChar(ResolvedResource rsc)  {
        String rtn = File.separator;
        try {
            OSInfo osUtils = OSInfo.GetInstance();
            if (osUtils.isRemote(rsc)) {
               try {
                   rtn = osUtils.getOS(rsc).getFileSeparatorChar();
               }catch(Exception e) {
               }
            }
        }catch(Exception pe) {}
        return rtn;
    }


}
