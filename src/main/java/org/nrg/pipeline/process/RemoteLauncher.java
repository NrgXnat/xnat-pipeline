/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.io.InputStream;

import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.ssh2.MyUserInfo;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: RemoteLauncher.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class RemoteLauncher {
    
    public  String launchRemote(String host, String ssh2User, String ssh2Password, String ssh2Identity, CommandStatementPresenter command, boolean checkReturn, long maxwait) throws PipelineEngineException {
        JSch jsch = new JSch();
        Session session = null;
        boolean timedOut = false;
        String rtnStr = "";
        try {
            if (ssh2User == null) {
                ssh2User = System.getProperty("user.name");
            }
            session = jsch.getSession(ssh2User, host, 22);
            String streamOut = "";
            String streamErr = "";
            
            if (ssh2Identity != null) 
                jsch.addIdentity(ssh2Identity);
            if (ssh2Password != null) {
                session.setUserInfo(new MyUserInfo(ssh2Password));
            }
            java.util.Properties config=new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
    
            session.connect(30000);
            
            final ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command.getCommand(CommandStatementPresenter.PRIVATE_VIEW));
            if (maxwait > 0)
                session.setTimeout((int)maxwait);
            
            channel.connect();
            InputStream in=channel.getInputStream();
            InputStream err = channel.getErrStream();
            if (maxwait > 0) {    
                    //wait for it to finish
                thread =
                    new Thread() {
                        public void run() {
                            while (!channel.isEOF()) {
                                if (thread == null) {
                                    return;
                                }
                                try {
                                    sleep(500);
                                } catch (Exception e) {
                                    // ignored
                                }
                            }
                        }
                    };
    
                thread.start();
                thread.join(maxwait);
    
                if (thread.isAlive()) {
                    // ran out of time
                    thread = null;
                    timedOut = true;
                }
            }
            if (!timedOut) {
                int rtn;
                byte[] tmp=new byte[1024];
                while(true){
                  while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                      streamOut += new String(tmp, 0, i);
                  }
                  rtnStr = streamOut;
                  if(channel.isClosed()){
                    rtn = channel.getExitStatus();
                    if (streamOut.equals("") ) {
                        while(err.available()>0){
                            int i=err.read(tmp, 0, 1024);
                            if(i<0)break;
                              streamErr += new String(tmp, 0, i);
                          }
                        rtnStr = streamErr;
                    }
                    if (checkReturn)
                        if (rtn != 0) { rtnStr = streamOut; }
                    break;
                  }
                  try{Thread.sleep(1000);}catch(Exception ee){}
                }
            }
            channel.disconnect();
            return rtnStr;
        }catch(Exception e) {
            throw new PipelineEngineException("Unable to launch remote process using " + ssh2User + "@"+ host + " identity file " + ssh2Identity  + e.getLocalizedMessage(), e);
        }finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
    
    public  String launchRemote(String host, String ssh2User, String ssh2Password, String ssh2Identity, CommandStatementPresenter command) throws PipelineEngineException {
        return launchRemote(host, ssh2User, ssh2Password, ssh2Identity, command, true,-1);
    }
    
    private Thread thread = null;
}
