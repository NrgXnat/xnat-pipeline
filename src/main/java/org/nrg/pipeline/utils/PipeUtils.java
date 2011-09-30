/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.nrg.pipeline.manager.ExecutionManager;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PipeUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class PipeUtils {
    
    public static LinkedHashMap getLinkedResources(ResolvedStep resolvedStep) {
        LinkedHashMap rtn = new LinkedHashMap();
        LinkedHashMap pipeId = new LinkedHashMap();
        ResolvedResource[] resolvedResourceArray = resolvedStep.getResolvedResourceArray();
        for (int j = 0; j < resolvedResourceArray.length; j++) {
            String key = null;
            ResolvedResource rsc = resolvedResourceArray[j];
            if (rsc.isSetPipeId()) {
                key = rsc.getPipeId();
            }else {
                key = resolvedStep.getId() + "_" + rsc.getName() + "_" + j;
            }
            if (pipeId.containsKey(key)) {
                ((ArrayList)pipeId.get(key)).add(rsc);
            }else {
                ArrayList value = new ArrayList(); value.add(rsc);
                pipeId.put(key,value);
            }
        }
        Iterator iter = pipeId.keySet().iterator();
        while (iter.hasNext()) {
            ArrayList pipedRsc = (ArrayList)pipeId.get(iter.next());
            String stmt = "";
            String publicStmt= ""; //command without sensitive information
            //Calendar eta = new GregorianCalendar();
            //eta.set(Calendar.SECOND,0); eta.set(Calendar.MINUTE,0); eta.set(Calendar.HOUR,0);
            for (int i = 0; i < pipedRsc.size(); i++) {
                ResolvedResource rsc = (ResolvedResource)pipedRsc.get(i);
                CommandStatementPresenter cmd = ArgumentUtils.getCommandStatementPresenter(rsc);
                if (cmd != null) {
                	stmt += cmd.getCommand(CommandStatementPresenter.PRIVATE_VIEW) + " | ";
                	publicStmt +=  cmd.getCommand(CommandStatementPresenter.PUBLIC_VIEW) + "|";
                }
              //  if (rsc.isSetEstimatedTime()) {
                    //eta.add(Calendar.SECOND, rsc.getEstimatedTime().get(Calendar.SECOND));
                    //eta.add(Calendar.MINUTE, rsc.getEstimatedTime().get(Calendar.MINUTE));
                    //eta.add(Calendar.HOUR, rsc.getEstimatedTime().get(Calendar.HOUR));
               // }else eta = null;
            }
            stmt = stmt.trim();
            publicStmt = publicStmt.trim();
            if (stmt.endsWith("|")) stmt = stmt.substring(0, stmt.length() - 1);
            if (publicStmt.endsWith("|")) publicStmt = publicStmt.substring(0, publicStmt.length() - 1);
            CommandStatementPresenter commandStmt = new CommandStatementPresenter(stmt,publicStmt);
           // rtn.put(stmt, eta);
            rtn.put(commandStmt,pipedRsc.get(0));
        }
        return rtn;
    }

    static Logger logger = Logger.getLogger(PipeUtils.class);
}
