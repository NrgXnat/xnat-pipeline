/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;
import org.nrg.pipeline.xmlbeans.ResourceData.Input.Argument;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ArgumentUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class ArgumentUtils {
    
   public static void copy(Argument from, Argument to, boolean completeCopy) {
       if (from.isSetPrefix()) to.setPrefix(from.getPrefix());
       if (from.isSetNospace()) to.setNospace(from.getNospace());
       to.setId(from.getId());
       if (from.isSetName()) to.setName(from.getName());
       to.setDescription(from.getDescription());
       if (from.isSetIsSensitive()) to.setIsSensitive(from.getIsSensitive());
       if (completeCopy && from.isSetValue())
           to.setValue(from.getValue());
   }

   
   public static String getCommandWithoutArguments(ResolvedResource rsc) {
       String rtn = "" ;
       if (rsc.isSetCommandPrefix()) {
           rtn = rsc.getCommandPrefix() + " ";
       }
       
       if (rsc.isSetLocation()) {
           rtn +=FileUtils.getAbsolutePath(rsc.getLocation(),false);
           rtn += FileUtils.getFileSeparatorChar(rsc);
       }
       
       rtn +=  rsc.getName() + " ";
       return rtn;
   }
    
    public static CommandStatementPresenter getCommandStatementPresenter(ResolvedResource rsc) {
        CommandStatementPresenter rtn =null ;
        String stmt = getCommandWithoutArguments(rsc);
        String publicStmt = stmt;
        if (rsc.isSetInput() && rsc.getInput().sizeOfArgumentArray() > 0) {
            Argument[] args = rsc.getInput().getArgumentArray();
            for (int i = 0; i < args.length; i++) {
                stmt += getOption(args[i],false);
                publicStmt += getOption(args[i],true);
            }
        }
        rtn = new CommandStatementPresenter(stmt,publicStmt);
        return rtn;
    }
    
    
    public static String getOption(Argument arg, boolean masked) {
        String rtn = "";
        if (arg.isSetName()) {
            if (arg.isSetPrefix()) {
                rtn = arg.getPrefix().toString();
            }
            else rtn = "-";
            rtn += arg.getName();
        }
        if (arg.isSetValue()) {
        	String tempRtn = rtn;
        	if (arg.isSetNospace() && arg.getNospace().toString().equals("true")) {
                tempRtn+=escapeSpecialShellCharacters(arg.getValue());
            }else tempRtn+= " " + escapeSpecialShellCharacters(arg.getValue()); 
         	if (masked) {
        		if (arg.isSetIsSensitive() && arg.getIsSensitive().equalsIgnoreCase("true")) { 
        			rtn +=" ########";
        		}else rtn = tempRtn;
         	}else rtn = tempRtn;
        }
        return (rtn + " ");
    }
    
    public static String escapeSpecialShellCharacters(String input) {
        String rtn=input;
        if (!System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")) {
            if ((!rtn.startsWith("'") && !rtn.endsWith("'")) && (!rtn.startsWith("\"") && !rtn.endsWith("\""))) {
                if (!checkEscaped(rtn,"\\")) rtn= StringUtils.replace(rtn,"\\","\\\\");
                if (!checkEscaped(rtn,"\"")) rtn= StringUtils.replace(rtn,"\"","\\\"");
                if (!checkEscaped(rtn,"{")) rtn= StringUtils.replace(rtn,"{", "\\{");
                if (!checkEscaped(rtn,"}")) rtn= StringUtils.replace(rtn,"}","\\}");
                if (!checkEscaped(rtn,"'")) rtn= StringUtils.replace(rtn,"'","\\'");
                if (!checkEscaped(rtn,"`")) rtn= StringUtils.replace(rtn,"`","\\`");
                if (!checkEscaped(rtn,"?")) rtn= StringUtils.replace(rtn,"?","\\?");
                if (!checkEscaped(rtn,"[")) rtn= StringUtils.replace(rtn,"[","\\[");
                if (!checkEscaped(rtn,"]")) rtn= StringUtils.replace(rtn,"]","\\]");
                if (!checkEscaped(rtn,"~")) rtn= StringUtils.replace(rtn,"~","\\~");
                if (!checkEscaped(rtn,"$")) rtn= StringUtils.replace(rtn,"$","\\$");
                if (!checkEscaped(rtn,"!")) rtn= StringUtils.replace(rtn,"!","\\!");
                if (!checkEscaped(rtn,"&")) rtn= StringUtils.replace(rtn,"&","\\&");
                if (!checkEscaped(rtn,";")) rtn= StringUtils.replace(rtn,";","\\;");
                if (!checkEscaped(rtn,"(")) rtn= StringUtils.replace(rtn,"(","\\(");
                if (!checkEscaped(rtn,")")) rtn= StringUtils.replace(rtn,")","\\)");
                if (!checkEscaped(rtn,"<")) rtn= StringUtils.replace(rtn,"<","\\<");
                if (!checkEscaped(rtn,">")) rtn= StringUtils.replace(rtn,">","\\>");
                if (!checkEscaped(rtn,"|")) rtn= StringUtils.replace(rtn,"|","\\|");
                if (!checkEscaped(rtn,"#")) rtn= StringUtils.replace(rtn,"#","\\#");
                if (!checkEscaped(rtn,"@")) rtn= StringUtils.replace(rtn,"@","\\@");
                if (!checkEscaped(rtn,"^")) rtn= StringUtils.replace(rtn,"^","\\^");
            }
        }
        return rtn;
       }
   
    
    private static boolean checkEscaped(String inStr, String checkStr) {
        boolean rtn = false;
        int index = inStr.indexOf(checkStr);
        if (index != -1) {
            if (inStr.indexOf("\\"+checkStr) != -1) rtn = true; 
        }
        return rtn;
    }
}
