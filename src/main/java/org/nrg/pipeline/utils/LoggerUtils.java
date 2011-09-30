/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LoggerUtils.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class LoggerUtils {

   public static void print(Object[] list) {
       if (list == null) return;
       for (int i = 0; i < list.length; i++) {
           logger.debug(list[i]);
       }
   }

   public static void print(Hashtable hash) {
       Enumeration keys = hash.keys();
       while (keys.hasMoreElements()) {
           Object key = keys.nextElement();
           logger.info("Key " + key);
           Object value = hash.get(key);
           logger.info("Value " + value);
       }
   }
   
   public static void print(HashMap hash) {
       Iterator keys = hash.keySet().iterator();
       while (keys.hasNext()) {
           Object key = keys.next();
           logger.info("Key " + key);
           Object value = hash.get(key);
           logger.info("Value " + value);
       }
   }
   
   
   static Logger logger = Logger.getLogger(LoggerUtils.class);
}
