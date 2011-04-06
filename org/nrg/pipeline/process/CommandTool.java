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
 @version $Id: CommandTool.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class CommandTool {
    public String[] getCommandArray(String command) {
        String osName = System.getProperty("os.name");
        String[] cmdArray = null;
        if (osName.toUpperCase().indexOf("WINDOWS") == -1) {
            cmdArray = new String[] {"sh", "-c",  command };
        }else {
            if( osName.equals( "Windows 95" ) )
                cmdArray = new String[]{"command.com","/C",command};
            else
                cmdArray = new String[]{"cmd.exe","/C",command};
        }
        return cmdArray;
    }
}
