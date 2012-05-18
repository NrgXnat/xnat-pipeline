/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import org.apache.log4j.Logger;
import org.nrg.mail.api.MailMessage;
import org.nrg.pipeline.exception.PipelineEngineException;
import org.nrg.pipeline.utils.*;
import org.nrg.pipeline.xmlbeans.ArgumentData;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;
import org.nrg.pipeline.xmlbeans.ResourceData;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: PersonnelNotificationLauncher.java,v 1.2 2010/05/03 07:05:56 mohanar Exp $
 @since Pipeline 1.0
 */

public class PersonnelNotificationLauncher implements LauncherI {

    public int launchProcess(ParameterData[] parameters, ResolvedStep rStep, CommandStatementPresenter command,
            ResolvedResource rsc) throws PipelineEngineException {
        notification = new Notification();
        if (!rsc.getType().equals(ResourceData.Type.HUMAN)) {
            logger.debug("Recd a non-human resource type");
            return -1;
        }
        try {
            if (!debug) {
                List<ArgumentData> toArgs = XMLBeansUtils.getArgumentsById(rsc,"to");
                ArgumentData tolist = XMLBeansUtils.getArgumentById(rsc,"tolist");
                ArgumentData bodycontents = XMLBeansUtils.getArgumentById(rsc,"bodycontents");

                if ((toArgs == null || toArgs.size() == 0) && (tolist == null )) return -1;
                if (toArgs == null || toArgs.size() == 0) {
                	//Generate toArgs from tolist
                	//Read the file
                	toArgs = new ArrayList<ArgumentData>();
                	File toFile = new File(tolist.getValue());
                	if (!toFile.exists()) throw new PipelineEngineException("Email file list not found at " + tolist.getValue());
                	FileReader fr = new FileReader(toFile);
                	BufferedReader br = new BufferedReader(fr);
                	String s;
                	while((s = br.readLine()) != null) {
	                	String ids[] = s.split(",");
                        for (String id : ids) {
	                		ArgumentData toId = ArgumentData.Factory.newInstance();
                            toId.setName("to");
                            toId.setValue(id);
	                		toArgs.add(toId);
	                	}
	                }
                	br.close();
                	fr.close(); 
                }

                ArgumentData usernameArg = XMLBeansUtils.getArgumentById(rsc,"user");
                ArgumentData passwordArg = XMLBeansUtils.getArgumentById(rsc,"password");
                ArgumentData fromArg = XMLBeansUtils.getArgumentById(rsc,"from");
                List<ArgumentData> ccArgs =  XMLBeansUtils.getArgumentsById(rsc,"cc");
                List<ArgumentData> bccArgs = XMLBeansUtils.getArgumentsById(rsc,"bcc");
                ArgumentData subjectArg = XMLBeansUtils.getArgumentById(rsc,"subject");
                ArgumentData bodyArg = XMLBeansUtils.getArgumentById(rsc,"body");
                ArgumentData notifyAdminArg = XMLBeansUtils.getArgumentById(rsc,"notifyAdmin");
                List<ArgumentData> attachArgs = XMLBeansUtils.getArgumentsById(rsc, "attachment");

                String username = usernameArg.getValue();
                String password = passwordArg.getValue();

                //Create the email message
                String from = fromArg.getValue();

                List<String> tos = new ArrayList<String>();
                for (Object toArg : toArgs) {
                    tos.add(((ArgumentData) toArg).getValue());
                }

                List<String> ccs = new ArrayList<String>();
                if (ccArgs != null) {
                    for (Object ccArg : ccArgs) {
                        ccs.add(((ArgumentData) ccArg).getValue());
                    }
                }
                if (notifyAdminArg != null && notifyAdminArg.getValue().equals("1")) {
                    ccs.add(PipelineProperties.getPipelineAdminEmail());
                }

                List<String> bccs = new ArrayList<String>();
                if (bccArgs != null) {
                    for (Object bccArg : bccArgs) {
                        bccs.add(((ArgumentData) bccArg).getValue());
                    }
                }
                
                String subject = subjectArg.getValue();
               	String html = "";
                
                if (bodyArg == null)  {
                	if (bodycontents != null) {
                       	File bodyFile = new File(bodycontents.getValue());
                    	if (!bodyFile.exists()) throw new PipelineEngineException(" File containing email body not found at " + bodycontents.getValue());
                    	FileReader fr = new FileReader(bodyFile);
                    	BufferedReader br = new BufferedReader(fr);
                      	String s;
                    	while((s = br.readLine()) != null) {
                    		html += s + "</br>";
                    	}
                    	br.close(); fr.close();
                	}
                }else {
                	html = bodyArg.getValue();
                }
 
                //System.out.println("Email sent with html");
                String text = org.apache.commons.lang.StringUtils.replace(html,"<br>","\n");
                text = org.apache.commons.lang.StringUtils.replace(text,"<br/>","\n");
                text = org.apache.commons.lang.StringUtils.replace(text,"</br>","\n");
                   
                html = "<html>" + html + "</html>";
      
                Map<String, File> attachments = new HashMap<String, File>();
    
                if (attachArgs != null && attachArgs.size() > 0) {
                    for (Object attachArg : attachArgs) {
                        String path = ((ArgumentData) attachArg).getValue();
                        attachments.put(StringUtils.afterLastSlash(path), new File(path));
                    }
                }

                MailMessage message = new MailMessage();
                message.setFrom(from);
                message.setTos(tos);
                message.setCcs(ccs);
                message.setBccs(bccs);
                message.setSubject(subject);
                message.setHtml(html);
                message.setText(text);
                message.setAttachments(attachments);
                MailUtils.send(message, username, password);

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String time = dateFormat.format(Calendar.getInstance().getTime());

                if (outputFileName != null) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName, true));
                    out.write("\n--------------------------------------------\n");
                    out.write( time + "\n");
                    out.write("Email sent to " );
                    for (Object toArg : toArgs) {
                        out.write(((ArgumentData) toArg).getValue());
                    }
                    out.write("Subject: " + subjectArg.getValue() + "\n");
                    out.write("Body: " + html);
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
                notification.setCommand("Email sent to " + toArgs.get(0).getValue() + "  at " + time + "  Subject: " + subjectArg.getValue());
            }
            notification.setStepTimeLaunched(AdminUtils.getTimeLaunched());
            return 0;
        }catch(Exception e) {
            try {
                if (errorFileName != null) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(errorFileName, true));
                    out.write("\n--------------------------------------------\n");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    out.write( dateFormat.format(Calendar.getInstance().getTime()) + "\n");
                    out.write("Failed sending email. Exception: " + e.getLocalizedMessage() );
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
            } catch(Exception ignored) {}
            throw new PipelineEngineException("Personnel notification email could not be sent " + e.getClass() + e.getLocalizedMessage(),e);
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
    
    static Logger logger = Logger.getLogger(PersonnelNotificationLauncher.class);
    
}
