/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id$
 @since Pipeline 1.0
 */

public class TestExecutableCall {
    public static void main(String args[]) {
        String stmt = "dir" ;
        String[] cmdArray = new String[]{"cmd.exe","/C",stmt};
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray);
            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            //String line = null;
            //System.out.println("<OUTPUT>");
            //while ( (line = br.readLine()) != null)
            //    System.out.println(line);
            System.out.println("</OUTPUT>");
            //int exitVal = proc.waitFor();            
            //System.out.println("Process exitValue: " + exitVal);
            proc.destroy();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
