/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Represents the two possible views of the commandline statement. 
 One view is the public view which contains the sensitive information like password masked. The other view is the actual command. 
 @author mohanar
 @version $Id: CommandStatementPresenter.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class CommandStatementPresenter {
	String command;
	String publicCommand;
	public static final String PUBLIC_VIEW="masked_sesitive";
	public static final String PRIVATE_VIEW="sensitive";
	
	public CommandStatementPresenter(String cmd) {
		command = cmd; publicCommand = cmd;
	}
	
	public CommandStatementPresenter(String privateStmt, String publicStmt) {
		command = privateStmt;
		publicCommand = publicStmt;
	}
	
	public String getCommand(String view_type) {
		if (view_type.equalsIgnoreCase(PRIVATE_VIEW))
			return command;
		else 
			return publicCommand;
	}
	
	public String toString() {
		return publicCommand;
	}
	
}
