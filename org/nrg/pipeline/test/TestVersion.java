/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.process.LocalProcessLauncher;
import org.nrg.pipeline.process.RemoteLauncher;
import org.nrg.pipeline.utils.CommandStatementPresenter;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: TestVersion.java,v 1.3 2007/10/06 12:08:56 mohanar Exp $
 @since Pipeline 1.0
 */

public class TestVersion {

    
    public String getProgramVersion(String command, boolean isRemote, String host, String user, String pwd, String identity, final int timeOut) throws PipelineEngineException{
        String rtn = getBestGuessProgramVersion(command + " --version", isRemote, host, user, pwd, identity,timeOut);
        if (rtn.equals(""))
            rtn = getBestGuessProgramVersion(command + " -version", isRemote, host, user, pwd, identity,timeOut);
        return rtn;
    }
    
    private String extractVersion(String inStr) {
        inStr = inStr.trim();
        if (inStr.startsWith("\n"))
            inStr = inStr.substring(1);
        String rtn = "";
        if (inStr.toLowerCase().indexOf("illegal") != -1 || inStr.toLowerCase().indexOf("unknown") != -1 ||  inStr.toLowerCase().indexOf("invalid") != -1 ||  inStr.toLowerCase().indexOf("unrecognized") != -1) {
          return rtn;   
        }
        
        int versionIndex = inStr.toLowerCase().indexOf("version");
        if (versionIndex != -1) {
            int verEndOfLine = inStr.indexOf("\n",versionIndex);
            if (verEndOfLine != -1) {
                int currentLine = -1;
                for(int i = verEndOfLine -1; i>0; i--) {
                    if (inStr.charAt(i)=='\n'){
                        currentLine = i; break;
                    }
                }
                if (currentLine != -1)
                    rtn = inStr.substring(currentLine+1,verEndOfLine);
                else
                    rtn = inStr.substring(0,verEndOfLine);
            }
        }else if (inStr.startsWith("$")) {
            int i = inStr.substring(1).indexOf("$");
            if (i != -1) {
                rtn = inStr.substring(1,i+1);
            }
        }else if (!inStr.toLowerCase().startsWith("usage")){
            //Return only first three lines 
            String[] lines = inStr.split("\n");
            if (lines != null && lines.length > 3) {
                rtn = lines[0] +" " + lines[1] + " " + lines[2];
            }
        }
        rtn = StringUtils.replace(rtn,"\n"," ");
        return rtn;
    }
    
    public String getBestGuessProgramVersion(String command, boolean isRemote, String host, String user, String pwd, String identity, final int timeOut) throws PipelineEngineException{
        String rtn ="";
        int exitVal = 0;
        if (isRemote) {
            System.out.println("Came here 1");
            rtn = new RemoteLauncher().launchRemote(host,user,pwd,identity,new CommandStatementPresenter(command), false, -1);
            return extractVersion(rtn);
        }
        try {
            LocalProcessLauncher launcher = new LocalProcessLauncher(null,null);
            //launcher.setCommand(command);
            if (launcher.getStreamErrOutput() != null)
               rtn = launcher.getStreamOutput();
            if (rtn == null && launcher.getStreamErrOutput() != null)
               rtn = launcher.getStreamErrOutput();
            return extractVersion(rtn);
        }catch(Exception e) {
            throw new PipelineEngineException("ProvenanceUtils::getProgramVersion " + e.getClass() + " " + e.getLocalizedMessage(), e);
        }
    }
    
    public static void main(String args[]) {
        String version = null;
        BasicConfigurator.configure();
        TestVersion t = new TestVersion();
        try {
            version = t.getBestGuessProgramVersion("ldd /data/solaris/4dfptoanalyze",true,"machine01","user_bla","bla",null,-1);
            System.out.println("Program Version is " + version);
        }catch(Exception e) {
            System.out.println("Couldnt get the version"); e.printStackTrace();
        }
    }

}
