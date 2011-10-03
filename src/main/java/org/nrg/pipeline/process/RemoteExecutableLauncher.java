/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.ProvenanceUtils;
import org.nrg.pipeline.utils.ssh2.MyUserInfo;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: RemoteExecutableLauncher.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class RemoteExecutableLauncher implements LauncherI {

    public int launchProcess(ParameterData[] parameters, ResolvedStep rStep, CommandStatementPresenter command,
            ResolvedResource rsc) throws PipelineEngineException {
        int rtn = 1;
        notification = new Notification();
        String cmd = "";
        String workDirectory = rStep.getWorkdirectory();
        if (workDirectory != null)
            cmd = "cd " + workDirectory + "; ";
        cmd += command.getCommand(CommandStatementPresenter.PRIVATE_VIEW);
        Calendar timeLaunched = AdminUtils.getTimeLaunched();
        try {
            if (!debug) {
                JSch jsch = new JSch();
                Session session = jsch.getSession(rsc.getSsh2User(), rsc.getSsh2Host(), 22);
                
                if (rsc.isSetSsh2Identity()) 
                    jsch.addIdentity(rsc.getSsh2Identity());
                if (rsc.isSetSsh2Password()) {
                    session.setUserInfo(new MyUserInfo(rsc.getSsh2Password()));
                }
                java.util.Properties config=new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
    
                session.connect(30000);
                
                
                final ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(cmd);
                FileWriter streamOut = null;
                if (outputFileName != null)
                    streamOut=new FileWriter(outputFileName, true);
    
                timeLaunched = AdminUtils.getTimeLaunched();
                notification.setStepTimeLaunched(timeLaunched);
                notification.setCommand(cmd);
                
                if (streamOut != null) {
                    streamOut.write("\n--------------------------------------------\n");
                    streamOut.write("Launching as " + rsc.getSsh2User() + "@" + rsc.getSsh2Host() +"\n");
                    streamOut.write(AdminUtils.formatTimeLaunched(notification.getStepTimeLaunched()) + "\n ");
                    if (workDirectory != null) streamOut.write("WorkDirectory " + workDirectory +"\n");
                    streamOut.write("Executing: " + command + "\n");
                    streamOut.write("\n--------------------------------------------\n");
                }
                FileOutputStream streamErr = null;
                if (errorFileName != null) {
                    streamErr = new FileOutputStream(errorFileName, true);
                    streamErr.write(new String("\n--------------------------------------------\n").getBytes());
                    streamErr.write(new String("Launching as " + rsc.getSsh2User() + "@" + rsc.getSsh2Host() +"\n").getBytes());
                    streamErr.write(new String(AdminUtils.formatTimeLaunched(notification.getStepTimeLaunched()) + "\n ").getBytes());
                    if (workDirectory != null) streamErr.write(new String("WorkDirectory " + workDirectory +"\n").getBytes());
                    streamErr.write(new String("Executing: " + command + "\n").getBytes());
                    streamErr.write(new String("\n--------------------------------------------\n").getBytes());
                    ((ChannelExec)channel).setErrStream(streamErr);
                }else {
                    ((ChannelExec)channel).setErrStream(System.err);
                }
    
                
                channel.connect();
    
                InputStream in=channel.getInputStream();
    
                byte[] tmp=new byte[1024];
                while(true){
                  while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    if (streamOut != null)
                        streamOut.write((new String(tmp, 0, i)));
                    else 
                        System.out.println((new String(tmp, 0, i)));
                  }
                  if(channel.isClosed()){
                    rtn = channel.getExitStatus(); 
                    if (streamOut != null)
                        streamOut.write("exit-status: "+ rtn);
                    break;
                  }
                  try{Thread.sleep(1000);}catch(Exception ee){}
                }
                channel.disconnect();
                session.disconnect();
                if (streamOut != null) {
                    try { //channel.disconnect will close its input and output stream. 
                    	streamOut.close();
                    }catch(IOException e){}
                }
                if (streamErr != null) 
                    try { streamErr.close(); } catch(IOException e){} //channel.disconnect will close its input and output stream. 
            }else {
                rtn = 0;
            }
            ProvenanceUtils.addProcessStep(rStep, command,rsc, timeLaunched, debug);
        return rtn;
    }catch(Exception e) {
        e.printStackTrace();
        throw new PipelineEngineException("Unable to launch remote process using " + rsc.getSsh2User() + "@"+ rsc.getSsh2Host() + " identity file " + rsc.getSsh2Identity()  + e.getLocalizedMessage(),e);
    }

    }

    public void setErrorFileName(String errorFileName) {
        this.errorFileName = errorFileName;
    }
    
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    
    public Notification getNotification() {
        return notification;
    }
    
    public void setDebug(boolean debugMode) {
        debug = debugMode;    
    }

    Notification notification = null;
    boolean debug;
    String errorFileName = null, outputFileName = null;
    static Logger logger = Logger.getLogger(RemoteExecutableLauncher.class);
    
    public static void main(String args[]) {
        RemoteExecutableLauncher launch  = new RemoteExecutableLauncher();
        //launch.setErrorFileName("err.txt");
        //launch.setErrorFileName("out.txt");
        ResolvedStep rStep = ResolvedStep.Factory.newInstance();
        ResolvedResource rsc = ResolvedResource.Factory.newInstance();
        rsc.setSsh2Host("MACHINE_BLA");
        rsc.setSsh2User("USERBLA");
        rsc.setSsh2Password("BLA");
        try {
            launch.launchProcess(null,rStep,new CommandStatementPresenter("echo $SHELL; echo $RELEASE",""), rsc);
            System.out.println("All Done");
            System.exit(0);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
