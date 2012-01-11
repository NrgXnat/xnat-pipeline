/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.LoopUtils;
import org.nrg.pipeline.utils.ParameterUtils;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlreader.XmlReader;



//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: TestCreateXML.java,v 1.3 2006/08/08 21:10:15 capweb Exp $
 @since Pipeline 1.0
 */

public class TestCreateXML {
    public static void main(String[] args) {
        String xmlFile = "C:\\xnat_pipeline_collection\\pipeline.xml"; 
        BasicConfigurator.configure();
        //Bind the instance to the generated XMLBeans types.
     try {
             XmlObject xmlObject = new XmlReader().read(xmlFile);
             if (!(xmlObject instanceof PipelineDocument)) {
                 System.out.println("launchPipeline() :: Invalid XML file supplied. Expecting a pipeline document");
                 System.exit(1);
             }
             
            PipelineDocument pipelineDoc = (PipelineDocument)xmlObject; 
            String errors = XMLBeansUtils.validateAndGetErrors(pipelineDoc);
            if (errors != null) {
                throw new XmlException("Invalid XML " + xmlFile + "\n" + errors);
            }
            LoopUtils.setLoopValues(pipelineDoc);
            System.out.println("Loop Resolved");
            //Resolve values of Parameters
            ParameterUtils.setParameterValues(pipelineDoc);
            System.out.println("Parameter Resolved");
            //AllResolvedStepsDocument resolvedDoc = PipelineManager.GetInstance().getInternalRepresentation(pipelineDoc);
            //resolvedDoc.save(new File("resolved.xml"));
            //ExecutionManager.GetInstance().execute(resolvedDoc);
            System.out.println("All done");
        }catch(IOException ioe) {
            System.out.println("File not found " + xmlFile);
            System.exit(1);
        }catch (XmlException xmle ) {
            System.out.println(xmle.getLocalizedMessage());
            System.exit(1);
        } catch(PipelineEngineException ane) {
            System.out.println(ane.getLocalizedMessage());
            System.exit(1);
        } catch(TransformerException ane) {
            System.out.println(ane.getLocalizedMessage());
            System.exit(1);
        }
    } 
}