/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.xpath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trans.IndependentContext;
import net.sf.saxon.trans.XPathException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.LoopUtils;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.xml.sax.InputSource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: XPathResolverSaxon.java,v 1.1 2009/09/02 20:28:23 mohanar Exp $
 @since Pipeline 1.0
 */

public class XPathResolverSaxon {
    
    private XPathResolverSaxon() {
    }

    public static XPathResolverSaxon GetInstance() {
        if (self == null) self = new XPathResolverSaxon();
        return self;
    }
    
    public ArrayList evaluate(PipelineData pipelineData, String xPathExpr) throws TransformerException, PipelineEngineException {
        ArrayList rtn = new ArrayList();
        //System.out.println("          " + xPathExpr);
        if (xPathExpr == null) {
            throw new PipelineEngineException("evaluate():: null values supplied for xPathExpression");
        }
        String expression = xPathExpr.trim().substring(0);
        if (expression.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
            if (!expression.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
                throw new PipelineEngineException("XPathResolverSaxon::evaluate():: Missing " + PipelineConstants.PIPELINE_XPATH_MARKER + " at the end of expression " + expression);
            }
            String xPathExpression = expression.substring(1,expression.length()-1);
            ArrayList resolvedExpr = null;
            String pipelineExpr = LoopUtils.loopNeedsToBeResolved(xPathExpression);
            if (pipelineExpr != null) {
                resolvedExpr = new ArrayList(); resolvedExpr.add(xPathExpression);
                while (pipelineExpr != null) {
                    resolvedExpr = resolveLoop(pipelineData, resolvedExpr, pipelineExpr);
                    pipelineExpr = LoopUtils.loopNeedsToBeResolved(resolvedExpr); 
                }    
                if (resolvedExpr != null) {
                    rtn.addAll(resolveXPathExpressions(resolvedExpr, pipelineData));
                }
            }else {
                ArrayList expr = new ArrayList(); expr.add(xPathExpression);
                rtn.addAll(resolveXPathExpressions(expr, pipelineData));
            }
        }else {
            rtn.add(expression);
        }
        //logger.debug("Expre " + expression + " " + rtn);
        return rtn;
    }
    
    private ArrayList resolveLoop(PipelineData pipelineData, Object xpathExpr, String loopStr) throws PipelineEngineException{
        ArrayList rtn = null;
        ArrayList loopValues = null;
        ArrayList input = null;
        if (xpathExpr == null) return null;
        if (xpathExpr instanceof String) {
            input = new ArrayList(); input.add(xpathExpr);
        }else if (xpathExpr instanceof ArrayList) {
            input = (ArrayList)xpathExpr;
        }else {
            throw new PipelineEngineException("Input Argument of type " + xpathExpr.getClass() + " not supported");
        }
        if (input == null) return null;
        for (int j = 0; j < input.size(); j++ ) {
            String xpathExpression = (String)input.get(j);
            //System.out.println("XpathExpre " + xpathExpression + " LOOP STR " + loopStr);
            int indexOfLoopOn = xpathExpression.indexOf(loopStr);
            
            int endLoopOnMarker = -1;
            if (indexOfLoopOn != -1) {
                String loopId = LoopUtils.getExpressionWhichIsBeingLooped(xpathExpression,loopStr,PipelineConstants.openingBracket,PipelineConstants.closingBracket);
                if (loopId != null) {
                    endLoopOnMarker = xpathExpression.indexOf(PipelineConstants.closingBracket,indexOfLoopOn+loopStr.length()+1+loopId.length());
                }
               if (endLoopOnMarker != -1) {
                   //System.out.println("Loop ID is " + loopId);
                   if (loopId.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && !loopId.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER) ) {
                       throw new PipelineEngineException("Illegal token encountered while parsing " + xpathExpression + " [Missing " + PipelineConstants.PIPELINE_XPATH_MARKER +" ] in  pipeline identified by " + pipelineData.getName());
                   }else if (loopId.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER) && loopId.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
                       PipelineDocument pipelineDoc = PipelineDocument.Factory.newInstance();
                       pipelineDoc.setPipeline(pipelineData);
                       loopValues = resolveXPathExpressions(xpathExpression.substring(indexOfLoopOn + loopStr.length()+2,endLoopOnMarker-1),pipelineDoc);
                   }else
                   loopValues = XMLBeansUtils.getLoopValuesById(pipelineData,loopId);
               }else {
                   throw new PipelineEngineException("Illegal token encountered while parsing " + xpathExpression + " [Missing )] in  pipeline identified by " + pipelineData.getName());
               }
               if (loopValues != null) {
                   if (rtn == null) rtn = new ArrayList();
                   for (int i = 0; i< loopValues.size(); i++) {
                       String aLoopValue = (String)loopValues.get(i);
                       if (aLoopValue.startsWith("'") && aLoopValue.endsWith("'")) {
                           rtn.add(StringUtils.replace(xpathExpression,xpathExpression.substring(indexOfLoopOn, endLoopOnMarker+1),aLoopValue));
                       }else 
                           rtn.add(StringUtils.replace(xpathExpression,xpathExpression.substring(indexOfLoopOn, endLoopOnMarker+1), "\'"+aLoopValue+"\'"));
                   }
               }   
            }else {
               rtn = input;     
            }
        }
        return rtn;
    }
    
    public  ArrayList resolveXPathExpressions (ArrayList xStmts, XmlObject xmlObj) throws PipelineEngineException{
        ArrayList rtn = new ArrayList();
        for (int i = 0; i < xStmts.size(); i++) {
            rtn.addAll(resolveXPathExpressions((String)xStmts.get(i),xmlObj));
        }    
        return rtn;
    }
    
    
    /*private Hashtable getNameSpace(String xStmt) {
        Hashtable rtn = new Hashtable();
        if (xStmt == null) return rtn;
        String[] tokens = xStmt.split(" ");
        if (tokens == null || tokens.length < 1 ) return rtn;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("xmlns:")) {
                String key = tokens[i].substring();
            }
        }
        return rtn;
    }*/
    
    public  ArrayList resolveXPathExpressions (String xStmt, XmlObject xmlObj) throws PipelineEngineException{
        ArrayList rtn = new ArrayList();
        if (xmlObj == null ) return rtn;
        //System.out.println(" xStmt " + xStmt);
        try {
            //Note not using  net.sf.saxon.xpath classes for problems with Default namespaces and prefix for an xpath expression
            XPathEvaluator xpe = new XPathEvaluator();
            
             XmlObject xmlObjDocument  = null;
             XmlOptions xmlOptions = new XmlOptions().setSavePrettyPrint();
             
             //System.out.println("Document name is " + xmlObj.getClass().getSimpleName());
             if (!xmlObj.getClass().getSimpleName().endsWith("DocumentImpl")) {
                 if (xmlObj.getDomNode().getPrefix() != null && xmlObj.getDomNode().getPrefix().equals("") && xmlObj.getDomNode().getNamespaceURI() != null) {
                     ((IndependentContext)xpe.getStaticContext()).declareNamespace("", xmlObj.getDomNode().getNamespaceURI());
                     xmlOptions.setUseDefaultNamespace();
                 }else if (xmlObj.getDomNode().getPrefix() != null && !xmlObj.getDomNode().getPrefix().equals("") && xmlObj.getDomNode().getNamespaceURI() != null) {
                     ((IndependentContext)xpe.getStaticContext()).declareNamespace(xmlObj.getDomNode().getPrefix(), xmlObj.getDomNode().getNamespaceURI());
                     HashMap suggestedPrefixes = new HashMap();
                     suggestedPrefixes.put(xmlObj.getDomNode().getNamespaceURI(),xmlObj.getDomNode().getPrefix() );
                     xmlOptions.setSaveSuggestedPrefixes(suggestedPrefixes);
                 }
                 String className =  StringUtils.replace(xmlObj.getClass().getName(),"DataImpl","Document$Factory").replaceFirst("\\.impl\\.","\\.");
                 String methodName = "set" +  StringUtils.replace(xmlObj.getClass().getSimpleName(),"DataImpl","");
                 Class c = Class.forName(className);
                 Method method = c.getMethod("newInstance", new Class[0]);
                 xmlObjDocument = (XmlObject)method.invoke(c, new Object[0]);
                 
                 Class[] parameterTypes = new Class[] {xmlObj.getClass().getInterfaces()[0]};
                 Object[] arguments = new Object[] {xmlObj};
                 Method setObj = xmlObjDocument.getClass().getMethod(methodName, parameterTypes);
                 setObj.invoke(xmlObjDocument,arguments);
             }else {
                 xmlObjDocument = xmlObj;
                 String methodName = "get" +  StringUtils.replace(xmlObj.getClass().getSimpleName(),"DocumentImpl","");
                 Class c = xmlObj.getClass();
                 Method method = c.getMethod(methodName, new Class[] {} );
                 xmlObj = (XmlObject)method.invoke(xmlObjDocument, new Class[] {} );

                 if (xmlObj.getDomNode().getPrefix() != null && xmlObj.getDomNode().getPrefix().equals("") && xmlObj.getDomNode().getNamespaceURI() != null) {
                     ((IndependentContext)xpe.getStaticContext()).declareNamespace("", xmlObj.getDomNode().getNamespaceURI());
                     xmlOptions.setUseDefaultNamespace();
                 }else if (xmlObj.getDomNode().getPrefix() != null && !xmlObj.getDomNode().getPrefix().equals("") && xmlObj.getDomNode().getNamespaceURI() != null) {
                     ((IndependentContext)xpe.getStaticContext()).declareNamespace(xmlObj.getDomNode().getPrefix(), xmlObj.getDomNode().getNamespaceURI());
                     HashMap suggestedPrefixes = new HashMap();
                     suggestedPrefixes.put(xmlObj.getDomNode().getNamespaceURI(),xmlObj.getDomNode().getPrefix() );
                     xmlOptions.setSaveSuggestedPrefixes(suggestedPrefixes);
                 }
             } 
             
             xmlOptions.setSaveNamespacesFirst();
             
             //System.out.println(xmlObjDocument.xmlText(xmlOptions));

             SAXSource ss = new SAXSource(new  InputSource(xmlObjDocument.newInputStream(xmlOptions)));
             XmlCursor cursor = xmlObjDocument.newCursor();
             assert cursor.isStartdoc();
             Map map = new HashMap();
              while (cursor.hasNextToken())
              {
                  if (cursor.isContainer())
                      cursor.getAllNamespaces(map); 
                  if (!map.isEmpty()) break;
                  if (cursor.toNextToken().equals(TokenType.NONE)) break;
                  if (cursor.isEnd() || cursor.isFinish() || cursor.isEnddoc()) break;
              }
             cursor.dispose();
             if (map.size() > 0) {
                 Iterator keys = map.keySet().iterator();
                 while (keys.hasNext()) {
                     String key = (String)keys.next();
                     String value = (String)map.get(key);
                     if (key != null && value != null )
                     ((IndependentContext)xpe.getStaticContext()).declareNamespace(key, value);
                 }
             }
             
             String resolvedXPath =   xStmt;
             XPathExpression xExpr =  xpe.createExpression(resolvedXPath);
             SequenceIterator rtns = xExpr.rawIterator(ss);
             Item xpathObj = rtns.next();
             while (xpathObj != null) {
                 xpathObj = rtns.current();
                 rtn.add(xpathObj.getStringValue());
                 xpathObj = rtns.next();
             }
             //System.out.println(" " + xStmt + " " + rtn);
             return rtn;
        } catch (IllegalAccessException e) {
            throw new PipelineEngineException(xStmt + " Encountered " + e.getClass() + "==>" + e.getLocalizedMessage(),e);     
        } catch (ClassNotFoundException e) {
            throw new PipelineEngineException(xStmt +" Encountered " + e.getClass() + "==>" + e.getLocalizedMessage(), e);
        }catch (NoSuchMethodException e) {
            throw new PipelineEngineException(xStmt +" Encountered " + e.getClass() + "==>" + e.getLocalizedMessage(), e);
        }catch (InvocationTargetException e) {
            throw new PipelineEngineException(xStmt +" Encountered " + e.getClass() + "==>" + e.getLocalizedMessage(), e); 
        }catch(XPathException e) {
            throw new PipelineEngineException(xStmt +" Encountered " + e.getClass() + "==>" + e.getLocalizedMessage(), e);
        }
    }
    
    private ArrayList resolveXPathExpressions (ArrayList xStmts,  PipelineData pipelineData) throws PipelineEngineException, TransformerException{
        ArrayList rtnList = new ArrayList();
        ArrayList rtn = new ArrayList();
        try {
            PipelineDocument pipelineDoc = PipelineDocument.Factory.newInstance();
            pipelineDoc.setPipeline(pipelineData);
            rtn.addAll(resolveXPathExpressions(xStmts,pipelineDoc));
            if (pathNeedsToBeResolved(rtn)) {
              for (int i = 0; i < rtn.size(); i++) {
                 ArrayList t = evaluate(pipelineData, (String)rtn.get(i));
                 rtnList.addAll(t);
              } 
            }else 
                rtnList.addAll(rtn);
            return rtnList;
        }catch(TransformerException e) {
            throw e;
        }
    }
 
    private boolean pathNeedsToBeResolved(Object obj) throws PipelineEngineException{
        boolean rtn = false;
        ArrayList input = null;
        if (obj instanceof String) {
            input = new ArrayList(); input.add(obj);
        }else if (obj instanceof ArrayList) {
            input = (ArrayList)obj;
        }else {
            throw new PipelineEngineException("loopNeedsToBeResolved::Input Argument of type " + obj.getClass() + " not supported");
        }
        if (input == null) return rtn;
        for (int i = 0; i < input.size(); i++) {
            if (((String)input.get(i)).indexOf(PipelineConstants.PIPELINE_XPATH_MARKER) != -1) {
                rtn = true;
                break;
            }
        }
        return rtn;
    }

   
    public static void main(String args[]) {
        try {
            BasicConfigurator.configure();
            XmlObject xmlObject = new XmlReader().read("C:\\data\\build\\CNTRACS_UCD_QC\\20110425_220901\\archive_trigger\\Pat\\Pat_params_20110425220906.xml");
            ArrayList resolvedValues = XPathResolverSaxon.GetInstance().resolveXPathExpressions("count(/Pipeline/parameters/parameter[name='bold']/values/list)",xmlObject);
            //ArrayList resolvedValues = XPathResolverSaxon.GetInstance().resolveXPathExpressions("nrgString:afterLastIndexofOrStr(/p:Parameters/p:parameter[p:name='target']/p:values/p:unique/text(),\"/\")",xmlObject);
            //ArrayList resolvedValues = XPathResolverSaxon.GetInstance().resolveXPathExpressions("nrgString:afterLastSlash(/Parameters/parameter[name='target']/values/unique/text())",xmlObject);
            System.out.println("ResolvedValues = " + resolvedValues.get(0));
            System.exit(0);
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }   
   
    
    
    private static XPathResolverSaxon self;
    static Logger logger = Logger.getLogger(XPathResolverSaxon.class);

}
