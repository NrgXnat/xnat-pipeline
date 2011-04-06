/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: WebServiceLauncher.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */
import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;



public class WebServiceLauncher {
    
         public static void main(String [] args) {
           try {
             String endpoint =
                 "http://localhost:8080/cnda_xnat/axis/FieldValues.jws";
      
            Service  service = new Service();
            Call     call    = (Call) service.createCall();
      
            call.setTargetEndpointAddress( new java.net.URL(endpoint) );
            call.setOperationName("search");
      
           Object ret =  call.invoke( new Object[] { "user_bla","bla","xnat:mrSessionData.ID","LIKE","0606","xnat:mrSessionData.ID","DESC" } );
      
           System.out.println("ret is an instance of " );
           } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    
    