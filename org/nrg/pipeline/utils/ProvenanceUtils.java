/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.net.InetAddress;
import java.util.Calendar;

import net.nbirn.prov.ProcessStep;
import net.nbirn.prov.ProcessStep.Library;
import net.nbirn.prov.ProcessStep.Platform;
import net.nbirn.prov.ProcessStep.Program;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.process.LocalProcessLauncher;
import org.nrg.pipeline.process.OS;
import org.nrg.pipeline.process.OSInfo;
import org.nrg.pipeline.process.RemoteLauncher;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.Provenance;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: ProvenanceUtils.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class ProvenanceUtils {
    
    private static void insertLocalMachineDetails(ProcessStep processStep) {
        Platform platform = null;
        platform = processStep.getPlatform();
        if (platform == null) {
            platform = processStep.addNewPlatform();
        }
        platform.setStringValue(System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        platform.setVersion(System.getProperty("os.version"));
        try {
            InetAddress addr = InetAddress.getLocalHost();
            processStep.setMachine(addr.getHostName());
        }catch(Exception e) {}
        processStep.setUser(System.getProperty("user.name"));
    }
    
    private static void insertRemoteMachineDetails(ResolvedResource rsc, ProcessStep processStep) throws PipelineException{
        Platform platform = null;
        platform = processStep.getPlatform();
        if (platform == null) platform = processStep.addNewPlatform();
        OS os = OSInfo.GetInstance().getOS(rsc);
        platform.setStringValue(os.getPlatform());
        platform.setVersion(os.getVersion());
        processStep.setMachine(os.getMachine());
        processStep.setUser(System.getProperty("user.name"));
    }
    
       
    
    public static void addProcessStep(ResolvedStep rStep, CommandStatementPresenter command, ResolvedResource rsc, Calendar timeStamp, boolean debug ) throws PipelineException {
        Provenance prov = rStep.getProvenance(); 
        if (  prov == null ) {
            prov = rStep.addNewProvenance();
        }
        ProcessStep processStep = prov.addNewProcessStep();
        Program prog = processStep.addNewProgram();
        prog.setVersion(getProgramVersion(rsc, 30000));
        boolean isRemote = false;
        if (OSInfo.GetInstance().isRemote(rsc)) {
            insertRemoteMachineDetails(rsc,processStep);
            isRemote = true;
        }else
            insertLocalMachineDetails(processStep);
        if (rsc.isSetProvenance()) {
            processStep.setLibraryArray(rsc.getProvenance().getLibraryArray());
        }else {
            addLibrary(processStep,rsc,isRemote);
        }
        prog.setStringValue(command.getCommand(CommandStatementPresenter.PUBLIC_VIEW));
        processStep.setProgram(prog);
        processStep.setTimestamp(timeStamp);
    }
    
    private static void addLibrary(ProcessStep processStep, ResolvedResource rsc, boolean isRemote) {
        String osName = System.getProperty("os.name");
        if (!osName.toLowerCase().startsWith("windows")) {
            try {
                String command = "ldd " + ArgumentUtils.getCommandWithoutArguments(rsc);
                String librariesStr = null;
                //System.out.println("REMOTE " + isRemote + "  " +  command + " " + rsc.getSsh2Host());
                if (!isRemote) {
                    LocalProcessLauncher launcher = new LocalProcessLauncher(null,null);
                    launcher.launchProcess(new CommandStatementPresenter(command,command),null, 30000);
                    if (launcher.getExitValue()==0) {
                        librariesStr = launcher.getStreamOutput();
                    }    
                }else {
                    librariesStr = new RemoteLauncher().launchRemote(rsc.getSsh2Host(),rsc.getSsh2User(),rsc.getSsh2Password(),rsc.getSsh2Identity(),new CommandStatementPresenter(command,command), false, -1); 
                }
                if (librariesStr != null) {
                    String[] lib = librariesStr.split("\n");
                    if (lib != null) {
                        for (String aLib: lib) {
                            Library library =  processStep.addNewLibrary();
                            library.setStringValue(aLib);
                        }
                    }
                }
        }catch(Exception e) {
                
            }
        }
    }
    
    private static String extractVersion(String inStr) {
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
    
    
    public static String getProgramVersion(ResolvedResource rsc,  final int timeOut) throws PipelineException{
        String command = ArgumentUtils.getCommandWithoutArguments(rsc);
        String rtn = extractVersion(launchProcess(new CommandStatementPresenter(command + " --version",command + " --version"), OSInfo.GetInstance().isRemote(rsc), rsc.getSsh2Host(), rsc.getSsh2User(), rsc.getSsh2Password(), rsc.getSsh2Identity(),timeOut));
        if (rtn.equals(""))
            rtn = extractVersion(launchProcess(new CommandStatementPresenter(command + " -version",command + " -version"), OSInfo.GetInstance().isRemote(rsc), rsc.getSsh2Host(), rsc.getSsh2User(), rsc.getSsh2Password(), rsc.getSsh2Identity(),timeOut));
        return rtn;
    }
    
    public static String launchProcess(CommandStatementPresenter command, boolean isRemote, String host, String user, String pwd, String identity, final int timeOut) throws PipelineException{
        String rtn ="";

        if (isRemote) {
            rtn = new RemoteLauncher().launchRemote(host,user,pwd,identity,command, false, timeOut);
            return rtn;
        }
        try {
            LocalProcessLauncher launcher = new LocalProcessLauncher(null,null);
            launcher.launchProcess(command,null, timeOut);
            if (launcher.getStreamErrOutput() != null)
               rtn = launcher.getStreamOutput();
            if (rtn == null && launcher.getStreamErrOutput() != null)
               rtn = launcher.getStreamErrOutput();
            return rtn;
        }catch(Exception e) {
            throw new PipelineException("ProvenanceUtils::getProgramVersion " + e.getClass() + " " + e.getLocalizedMessage(), e);
        }
    }
    
    static Logger logger = Logger.getLogger(ProvenanceUtils.class);

    
    public static void main(String args[]) {
        BasicConfigurator.configure();
        try {
            //String unameOut = new RemoteLauncher().launchRemote("host", "usr", "pwd", null,"uname -a");
            String unameOut = new RemoteLauncher().launchRemote("machine01", "user_bla", "bla", null,new CommandStatementPresenter("/usr/local/grad_unwarp_bash/scripts/grad_unwarp_nodisplay -i /data/cninds01/data2/WORK/PIPELINE_TEST/SA23486_GU/RAW/1.MR.Head_Mintun.3.97.20070205.143554.453000.6200022061.dcm -o SA23486_GU_6_uw.mgh -unwarp sonata",""));
        if (unameOut == null) {
            logger.info("Maynot be a Unix based machine");
        }
        String[] tokens = unameOut.split("\\s+");
        System.out.println("Uname " + unameOut + " : " );
        LoggerUtils.print(tokens);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
