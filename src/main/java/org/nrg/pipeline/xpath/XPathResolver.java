/* 
 *  Copyright Washington University in St Louis 2006
 *  All rights reserved
 *  
 */

package org.nrg.pipeline.xpath;

//import javax.xml.namespace.QName;
//import javax.xml.xpath.XPath;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;

import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.nrg.pipeline.constants.PipelineConstants;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.LoopUtils;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.PipelineData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: XPathResolver.java,v 1.1 2009/09/02 20:28:23 mohanar Exp $
 @since Pipeline 1.0
 */

public class XPathResolver {

       private XPathResolver() {
       }

       public static XPathResolver GetInstance() {
           if (self == null) self = new XPathResolver();
           return self;
       }
       
       public ArrayList evaluate(PipelineData pipelineData, Node xmlElement, String xPathExpr) throws TransformerException, PipelineEngineException {
           ArrayList rtn = new ArrayList();
           if (xPathExpr == null) {
               throw new PipelineEngineException("evaluate():: null values supplied for xPathExpression");
           }
           String expression = xPathExpr.substring(0);
           if (expression.startsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
               if (!expression.endsWith(PipelineConstants.PIPELINE_XPATH_MARKER)) {
                   throw new PipelineEngineException("XPathResolver::evaluate():: Missing $ at the end of expression " + expression);
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
                       rtn.addAll(resolveXPathExpressions(resolvedExpr, xmlElement, pipelineData));
                   }
               }else {
                   ArrayList expr = new ArrayList(); expr.add(xPathExpression);
                   rtn.addAll(resolveXPathExpressions(expr, xmlElement, pipelineData));
               }
           }else {
               expression = StringUtils.replace(expression,"\\\\$","$");
               rtn.add(expression);
           }
           return rtn;
       }
       
       private ArrayList getValues(Object inObj) throws  TransformerException, PipelineEngineException{
           ArrayList rtn = new ArrayList();
           ArrayList input = null;
           try {
               if (inObj instanceof XObject) {
                   input = new ArrayList(); input.add(inObj);
               }else if (inObj instanceof ArrayList) {
                   input = (ArrayList)inObj;
               }else {
                   throw new PipelineEngineException("Input Argument of type " + inObj.getClass() + " not supported");
               }
               if (input == null) return null;
               for (int j = 0; j < input.size(); j++) {
                   Object obj = input.get(j);
                   XObject xobj = (XObject) obj;
                   if (xobj == null) {
                       logger.info("getValues() recd a null XObject");
                       return null;
                   }
                   if (xobj.getType() == XObject.CLASS_NULL || xobj.getType() == XObject.CLASS_UNKNOWN) {
                       logger.info("getValues() recd a null XObject");
                       return null;
                   }
                   if (xobj.getType() == XObject.CLASS_NODESET ) {
                          NodeList nValueNodes =  xobj.nodelist();
                          if (xobj != null) {
                              for (int i = 0; i < nValueNodes.getLength(); i++) {
                                  Node aNode = (Node)nValueNodes.item(i);
                                  if(aNode.getFirstChild() != null) {
                                      rtn.add(aNode.getFirstChild().getNodeValue());
                                  } else {
                                      rtn.add(aNode.getNodeValue());
                                  }
                              }
                          }
                   }else if (xobj.getType() == XObject.CLASS_STRING ) {
                       rtn.add(xobj.str());
                   }else if (xobj.getType() == XObject.CLASS_NUMBER ) {
                       rtn.add(xobj.toString());
                   }else if (xobj.getType() == XObject.CLASS_BOOLEAN ) {
                       rtn.add(xobj.bool()+"");
                   }
               }
           } catch(TransformerException e) {
               throw e;
           }
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
               int indexOfLoopOn = xpathExpression.indexOf(loopStr);
               int endLoopOnMarker = -1;
               if (indexOfLoopOn != -1) {
                  endLoopOnMarker = xpathExpression.indexOf(")", indexOfLoopOn);
                  if (endLoopOnMarker != -1) {
                      String loopId = xpathExpression.substring(indexOfLoopOn + loopStr.length()+1,endLoopOnMarker);
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

       public  ArrayList resolveXPathExpressions (ArrayList xStmts, Node xmlElement) throws PipelineEngineException, TransformerException{
           ArrayList rtn = new ArrayList();
           try {
               for (int i = 0; i < xStmts.size(); i++) {
                   String resolvedXPath =   (String)xStmts.get(i);
                   XObject xo = XPathAPI.eval(xmlElement, resolvedXPath);
                   rtn.addAll(getValues(xo));  
               }   
               return rtn;
           }catch(TransformerException e) {
               throw e;
           }
       }
       
       private ArrayList resolveXPathExpressions (ArrayList xStmts, Node xmlElement, PipelineData pipelineData) throws PipelineEngineException, TransformerException{
           ArrayList rtnList = new ArrayList();
           ArrayList rtn = new ArrayList();
           try {
               for (int i = 0; i < xStmts.size(); i++) {
                   String resolvedXPath =   (String)xStmts.get(i);
                   XObject xo = XPathAPI.eval(xmlElement, resolvedXPath);
                   rtn.addAll(getValues(xo));
               }   
               if (pathNeedsToBeResolved(rtn)) {
                 for (int i = 0; i < rtn.size(); i++) {
                    ArrayList t = evaluate(pipelineData, xmlElement, (String)rtn.get(i));
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

       
       private static XPathResolver self;
       static Logger logger = Logger.getLogger(XPathResolver.class);

}



