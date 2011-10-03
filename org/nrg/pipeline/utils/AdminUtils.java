/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.nrg.pipeline.manager.EventManager;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: AdminUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class AdminUtils {

    
    public static Calendar getTimeLaunched() {
       return Calendar.getInstance();
   }
    
    public static String formatTimeLaunched(Calendar time) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s");
        if (time == null) return "";
        return dateFormat.format(time.getTime());
    }
    
    
    
    
    public static synchronized void fireNotification(Notification notification) {
        EventManager.GetInstance().notify(notification);
    }
    
    static Logger logger = Logger.getLogger(AdminUtils.class);
    
}
