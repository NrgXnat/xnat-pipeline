/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.util.Hashtable;

import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: OSInfo.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class OSInfo {

    private OSInfo() {
        OSTypes = new Hashtable();
    }

    public static OSInfo GetInstance() {
        if (self==null) self = new OSInfo();
        return self;
    }
    
    public  boolean isRemote(ResolvedResource rsc) {
        boolean rtn = false;
        if (rsc.isSetSsh2Host())
            rtn = true;
        return rtn;
    }
      
    
    public OS getOS(String host, String user, String password, String identity) throws PipelineException {
        OS rtn = null;
        if (OSTypes.containsKey(host)) {
            rtn = (OS)OSTypes.get(host);
        }else {
            setOS(host,user, password, identity);
            rtn = (OS)OSTypes.get(host);
        }
        return rtn;
    }
    
    public  OS  getOS(ResolvedResource rsc) throws PipelineException{
        return getOS(rsc.getSsh2Host(), rsc.getSsh2User(), rsc.getSsh2Password(), rsc.getSsh2Identity());
    }
    
    private  void setOS(String host, String user, String password, String identity) throws PipelineException{
        if (OSTypes.containsKey(host)) return;
        try {
            String unameOut = new RemoteLauncher().launchRemote(host,user,password,identity,new CommandStatementPresenter("uname -srp"));
            String[] tokens = unameOut.split("\\s+");
             OS remoteOS = new OS();
             remoteOS.setPlatform(tokens[0]);
             remoteOS.setVersion(tokens[1]);
             remoteOS.setMachine(tokens[2]);
             OSTypes.put(host,remoteOS);
        }catch(Exception e) {
            throw new PipelineException("Couldnt setOS info for " + host);
        }
    }


    private static OSInfo self;
    static Hashtable OSTypes;
    
    
    public static void main(String args[]) {
        try {
            OS os = OSInfo.GetInstance().getOS("cninds05l.neuroimage.wustl.edu","USERBLA","BLA",null);
            System.out.println(os.toString());
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
