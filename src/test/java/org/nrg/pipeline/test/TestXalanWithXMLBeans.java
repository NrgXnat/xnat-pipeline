/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import com.sun.org.apache.xpath.internal.XPathAPI;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: TestXalanWithXMLBeans.java,v 1.1 2006/07/20 14:01:16 capweb Exp $
 @since Pipeline 1.0
 */

public class TestXalanWithXMLBeans {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String xmlFile = "C:\\xnat_pipeline_collection\\pipeline.xml"; 
        BasicConfigurator.configure();
        try {
            XmlObject xmlObject = new XmlReader().read(xmlFile);
            if (!(xmlObject instanceof PipelineDocument)) {
                System.out.println("launchPipeline() :: Invalid XML file supplied. Expecting a pipeline document");
                System.exit(1);
            }
           PipelineDocument pipelineDoc = (PipelineDocument)xmlObject;
           Document pipelineDOMDocument = XMLBeansUtils.getDomDocument(pipelineDoc);
           NodeList nodes = XPathAPI.selectNodeList(pipelineDOMDocument,"/Pipeline");
           System.out.println("Number of param nodes is " + nodes.getLength());
           DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder = dbf.newDocumentBuilder();
           Document doc = builder.parse( new File("pipeline1.xml"));
           System.out.println("Main " + dbf + " :: " + builder);
           NodeList nodes1 = XPathAPI.selectNodeList(doc,"/Pipeline");
           System.out.println("Number of param nodes is " + nodes1.getLength());
        }catch(IOException ioe) {
            System.out.println("File not found " + xmlFile);
            System.exit(1);
        }catch (XmlException xmle ) {
            System.out.println(xmle.getLocalizedMessage());
            System.exit(1);
        }catch (PipelineEngineException ple ) {
            System.out.println(ple.getLocalizedMessage());
            System.exit(1);
        }catch (TransformerException ple ) {
            System.out.println(ple.getLocalizedMessage());
            System.exit(1);
        }catch (SAXException ple ) {
            System.out.println(ple.getLocalizedMessage());
            System.exit(1);
        }catch (ParserConfigurationException ple ) {
            System.out.println(ple.getLocalizedMessage());
            System.exit(1);
        }
        
        
    }

}
