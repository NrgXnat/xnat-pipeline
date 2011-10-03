/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.pipeline.test;

import java.util.ArrayList;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.ResourceDocument;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.pipeline.xpath.XPathResolverSaxon;


public class TestXMLBeans {
    public static void main(String[] args) {
     //String xmlFile = "arc_mprage.xml"; 
     //String xmlFile = "Z:/test/catalog/arc_mprage.xml"; 
        String xmlFile = "Z:/test/bin/resolved1.xml";
//      Bind the instance to the generated XMLBeans types.
     try {
         XmlObject xmlObject = new XmlReader().read(xmlFile);
         if (!(xmlObject instanceof ResourceDocument)) {
            System.out.println("launchPipeline() :: Invalid XML file supplied. Expecting a pipeline document");
             System.exit(1);
         }
         
        ResourceDocument resourceDoc = (ResourceDocument)xmlObject;
        
       // String errors = XMLBeansUtils.validateAndGetErrors(resourceDoc);
        //if (errors != null) {
         //   throw new XmlException("Invalid XML " + xmlFile + "\n" + errors);
       // }
      
     System.out.println("Prefix is " + resourceDoc.getResource().getDomNode().getPrefix() + ": " + resourceDoc.getResource().getDomNode().getNamespaceURI());   
        
     //String stmt = "/pip:Resource/pip:input/pip:argument[@id='8']/pip:value";
     //String stmt = "/Resource/input/argument[@id='sessionId']/value";
     //String stmt = "if (count(/pip:Resource/pip:input/pip:argument[@id='8']/pip:value)) then concat(substring-before(/pip:Resource/pip:input/pip:argument[@id='4dfp_file']/pip:value/text(),'.'),'_8bit.img')                     else concat(substring-before(/pip:Resource/pip:input/pip:argument[@id='4dfp_file']/pip:value/text(),'.'),'.4dint.img')";
     //String stmt = "if (count(/pip:Resource/pip:input/pip:argument[@id='8']/pip:value)) then for $i in (1 to count(/pip:Resource/pip:input/pip:argument[@id='8']/pip:value)) return concat('MOhana',$i)   else 'Arjun'";
     String stmt = "if (count(/pip:Resource/pip:input/pip:argument[@b]/pip:value)) then concat(/pip:Resource/pip:input/pip:argument[@b]/pip:value/text(),'.4dfp.img') else 'analyze.4dfp.img'";
     ArrayList stms = new ArrayList(); stms.add(stmt);
     System.out.println("XPATH " + XPathResolverSaxon.GetInstance().resolveXPathExpressions(stms,resourceDoc.getResource()));
     
     }catch(Exception ioe) {
        ioe.printStackTrace();
        System.exit(1);
    }
    }
    
    
}
