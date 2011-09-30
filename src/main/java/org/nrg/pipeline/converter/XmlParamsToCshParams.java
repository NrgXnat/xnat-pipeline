/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.AllResolvedStepsDocument;
import org.nrg.pipeline.xmlbeans.ArgumentData;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.PipelineData.Parameters;
import org.nrg.pipeline.xmlreader.XmlReader;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: XmlParamsToCshParams.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class XmlParamsToCshParams {
	
	private String cvs_id = "$Id: XmlParamsToCshParams.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $";
	String inPath = null, outPath=null; 
	
	public XmlParamsToCshParams(String args[]) {
		if (args.length == 0 || args.length > 2) {
			usage();
		}else {
			outPath = args[0];
			if (args.length == 2) {
			     inPath = args[1];		
			}
		}
	}
	
	public void convert() {
		if (inPath != null) {
			File f = new File(inPath);
			if (!f.exists()) {
				System.out.println("File " + inPath + " doesnt exist.....aborting");
				System.exit(1);
			}
			if (!convertForCSHFromResolved()) {
				convertForCSHFromParametersDoc();
			}
		}
	}
	
  
   
	   public boolean convert(ParameterData[] paramArr, ArgumentData csvSkipParamArg) {
		   String csvSkipParamList = null;
		   try {
			   csvSkipParamList = csvSkipParamArg.getValue();
		   }catch(NullPointerException npe) {}
		   return convert(paramArr, csvSkipParamList);
	   }
	
	public boolean convert(ParameterData[] paramArr, String csvSkipParamList) {
		 boolean success = false;
		 Hashtable skipParams = new Hashtable();
		 if (csvSkipParamList != null) {
			 String[] skipParameters = csvSkipParamList.split(",");
			 for (int i = 0; i < skipParameters.length; i++) {
				 skipParams.put(skipParameters[i],"");
			 }
		 }
			if (paramArr == null ) {
				bailOut("No parameters specified");
			}
			try {
		        BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
				for (int i = 0; i < paramArr.length; i++) {
					ParameterData aParam = paramArr[i];
					if (skipParams.containsKey(aParam.getName())) continue;
					Values val = aParam.getValues();
					if (val.isSetUnique()) {
						//try {
						//   Integer iVal = Integer.parseInt(val.getUnique().trim());
						//   out.write("@" + aParam.getName().trim() + "=" + iVal.intValue() + "\n");
					//	}catch (NumberFormatException nfe) {
							out.write("set " + aParam.getName().trim() + "=" + val.getUnique().trim() + "\n");
					//	}
					}else {
						String[] listValues = val.getListArray();
						out.write("set " + aParam.getName().trim() + "=(");
						for (int j = 0; j<listValues.length; j++) {
							out.write(" " + listValues[j].trim());
						}
						out.write(")\n");
					}
				}
		        out.close();
		        success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return success;
 }
	
	private boolean convertForCSHFromParametersDoc() {
		ParametersDocument.Parameters params = getParametersFromParametersDocument();
		 boolean success = false;
			if (params == null ) {
				bailOut("No parameters specified");
			}
			try {
		        BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
		        out.write("#Parameters created from " +  inPath + " \n");
		        ParameterData[] paramArr = params.getParameterArray();
				for (int i = 0; i < paramArr.length; i++) {
					ParameterData aParam = paramArr[i];
					Values val = aParam.getValues();
					if (val.isSetUnique()) {
						out.write("set " + aParam.getName().trim() + "=" + val.getUnique().trim() + "\n");
					}else {
						String[] listValues = val.getListArray();
						out.write("set " + aParam.getName().trim() + "=(");
						for (int j = 0; j<listValues.length; j++) {
							out.write(" " + listValues[j].trim());
						}
						out.write(")\n");
					}
				}
		        out.close();
		        success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return success;
	}
	
	private boolean convertForCSHFromResolved() {
		 AllResolvedStepsDocument.AllResolvedSteps.Parameters params = getParametersFromResolvedPipeline();
		 boolean success = false;
			if (params == null ) {
				return false;
			}
			try {
		        BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
		        out.write("#Parameters created from " +  inPath + " \n");
		        ParameterData[] paramArr = params.getParameterArray();
				for (int i = 0; i < paramArr.length; i++) {
					ParameterData aParam = paramArr[i];
					Values val = aParam.getValues();
					if (val.isSetUnique()) {
						out.write("set " + aParam.getName().trim() + "=" + val.getUnique().trim() + "\n");
					}else {
						String[] listValues = val.getListArray();
						out.write("set " + aParam.getName().trim() + "=(");
						for (int j = 0; j<listValues.length; j++) {
							out.write(" " + listValues[j].trim());
						}
						out.write(")\n");
					}
				}
		        out.close();
		        success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return success;
	}
	
	private void convertForCSH() {
		Parameters params = getParametersFromPipeline();
		if (params == null ) {
			bailOut("No parameters specified");
		}
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
	        out.write("#Parameters created from " +  inPath + " \n");
	        ParameterData[] paramArr = params.getParameterArray();
			for (int i = 0; i < paramArr.length; i++) {
				ParameterData aParam = paramArr[i];
				Values val = aParam.getValues();
				if (val.isSetUnique()) {
					out.write("set " + aParam.getName().trim() + "=" + val.getUnique().trim() + "\n");
				}else {
					String[] listValues = val.getListArray();
					out.write("set " + aParam.getName().trim() + "=(");
					for (int j = 0; j<listValues.length; j++) {
						out.write(" " + listValues[j].trim());
					}
					out.write(")\n");
				}
			}
	        out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void bailOut(String msg) {
        System.out.println(msg);
        System.exit(1);
	}
	
	private  Parameters getParametersFromPipeline() {
		Parameters rtn = null;
		try {
			 XmlObject xmlObject = new XmlReader().read(inPath);
	         if (!(xmlObject instanceof PipelineDocument)) {
	        	 return rtn;
	         }
	 	 	PipelineDocument pipelineDoc = (PipelineDocument)xmlObject;
		    String errors = XMLBeansUtils.validateAndGetErrors(pipelineDoc);
		     if (errors != null) {
		          bailOut("Invalid XML " + inPath + "\n" + errors);
		     }
		     rtn = pipelineDoc.getPipeline().getParameters();
		}catch(Exception e) {
		   bailOut(e.getMessage());	
		}
        return rtn;
	}

	private  ParametersDocument.Parameters getParametersFromParametersDocument() {
		ParametersDocument.Parameters rtn = null;
		try {
			 XmlObject xmlObject = new XmlReader().read(inPath);
	         if (!(xmlObject instanceof ParametersDocument)) {
	        	 return rtn;
	         }
	         ParametersDocument paramDoc = (ParametersDocument)xmlObject;
		    String errors = XMLBeansUtils.validateAndGetErrors(paramDoc);
		     if (errors != null) {
		          bailOut("Invalid XML " + inPath + "\n" + errors);
		     }
		     rtn = paramDoc.getParameters();
		}catch(Exception e) {
		   bailOut(e.getMessage());	
		}
        return rtn;
	}

	
	
	private  AllResolvedStepsDocument.AllResolvedSteps.Parameters getParametersFromResolvedPipeline() {
		AllResolvedStepsDocument.AllResolvedSteps.Parameters rtn = null;
		try {
			 XmlObject xmlObject = new XmlReader().read(inPath);
	         if (!(xmlObject instanceof AllResolvedStepsDocument)) {
	        	 return rtn;
	         }
	         AllResolvedStepsDocument pipelineDoc = (AllResolvedStepsDocument)xmlObject;
		    String errors = XMLBeansUtils.validateAndGetErrors(pipelineDoc);
		     if (errors != null) {
		          bailOut("Invalid XML " + inPath + "\n" + errors);
		     }
		     rtn = pipelineDoc.getAllResolvedSteps().getParameters();
		}catch(Exception e) {
		   bailOut(e.getMessage());	
		}
        return rtn;
	}

	
	private void usage() {
		System.out.println("XmlParamsToCshParams <outpath to params file> [<path to xml file>]");
		System.out.println(cvs_id);
		System.out.println("PURPOSE: Convert an xml based parameters to CSH bassed params file");
		System.exit(1);
	}
	
	public static void main(String args[]) {
		XmlParamsToCshParams converter = new XmlParamsToCshParams(args);
		converter.convert();
		System.out.println("Document converted");
		System.exit(0);
	}
}
