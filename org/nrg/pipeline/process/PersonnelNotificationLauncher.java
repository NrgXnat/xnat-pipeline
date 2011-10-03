/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.nrg.pipeline.exception.PipelineException;
import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.CommandStatementPresenter;
import org.nrg.pipeline.utils.Notification;
import org.nrg.pipeline.utils.PipelineProperties;
import org.nrg.pipeline.utils.StringUtils;
import org.nrg.pipeline.utils.XMLBeansUtils;
import org.nrg.pipeline.xmlbeans.ArgumentData;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ResourceData;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep;
import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

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
            ResolvedResource rsc) throws PipelineException {
        notification = new Notification();
        if (!rsc.getType().equals(ResourceData.Type.HUMAN)) {
            logger.debug("Recd a non-human resource type");
            return -1;
        }
        try {
            if (!debug) {
                ArrayList toArgs = XMLBeansUtils.getArgumentsById(rsc,"to");
                ArgumentData tolist = XMLBeansUtils.getArgumentById(rsc,"tolist");
                ArgumentData bodycontents = XMLBeansUtils.getArgumentById(rsc,"bodycontents");

                if ((toArgs == null || toArgs.size() == 0) && (tolist == null )) return -1;
                if (toArgs == null || toArgs.size() == 0) {
                	//Generate toArgs from tolist
                	//Read the file
                	toArgs = new ArrayList();
                	File toFile = new File(tolist.getValue());
                	if (!toFile.exists()) throw new PipelineException("Email file list not found at " + tolist.getValue());
                	FileReader fr = new FileReader(toFile);
                	BufferedReader br = new BufferedReader(fr);
                	String s;
                	while((s = br.readLine()) != null) {
	                	String ids[] = s.split(",");
	                	for (int j=0;j<ids.length;j++) {
	                		ArgumentData toId = ArgumentData.Factory.newInstance();
	                		toId.setName("to"); toId.setValue(ids[j]);
	                		toArgs.add(toId);
	                		
	                	}
	                	}
                	br.close();
                	fr.close(); 
                }
                ArgumentData fromArg = XMLBeansUtils.getArgumentById(rsc,"from");
                ArgumentData hostArg = XMLBeansUtils.getArgumentById(rsc,"host");
                ArrayList ccArgs =  XMLBeansUtils.getArgumentsById(rsc,"cc");
                ArrayList bccArgs = XMLBeansUtils.getArgumentsById(rsc,"bcc");
                ArgumentData subjectArg = XMLBeansUtils.getArgumentById(rsc,"subject");
                ArgumentData bodyArg = XMLBeansUtils.getArgumentById(rsc,"body");
                ArgumentData notifyAdmin = XMLBeansUtils.getArgumentById(rsc,"notifyAdmin");
                ArrayList attachArgs = XMLBeansUtils.getArgumentsById(rsc,"attachment");

                
                //Create the email message
                HtmlEmail email = new HtmlEmail();
                if (hostArg == null)
                    email.setHostName(PipelineProperties.PIPELINE_SMTP_HOST);
                else
                    email.setHostName(hostArg.getValue());
                for (int i = 0; i < toArgs.size(); i++) {
                    email.addTo(((ArgumentData)toArgs.get(i)).getValue());
                }
                email.setFrom(fromArg.getValue());
                if (notifyAdmin!= null && notifyAdmin.getValue().equals("1") && ccArgs != null) {
                    for (int i = 0; i < ccArgs.size(); i++) 
                        email.addCc(((ArgumentData)ccArgs.get(i)).getValue());
                }else if (notifyAdmin== null) {
                    for (int i = 0; i < ccArgs.size(); i++) 
                    	email.addCc(((ArgumentData)ccArgs.get(i)).getValue());
                }
                if (bccArgs != null) {
                    for (int i = 0; i < bccArgs.size(); i++) 
                        email.addBcc(((ArgumentData)bccArgs.get(i)).getValue());
                }
                
                email.setSubject(subjectArg.getValue());
               	String emailbody = "";
                
                if (bodyArg == null)  {
                	if (bodycontents != null) {
                       	File bodyFile = new File(bodycontents.getValue());
                    	if (!bodyFile.exists()) throw new PipelineException(" File containing email body not found at " + bodycontents.getValue());
                    	FileReader fr = new FileReader(bodyFile);
                    	BufferedReader br = new BufferedReader(fr);
                      	String s;
                    	while((s = br.readLine()) != null) {
                    		emailbody += s + "</br>";
                    	}
                    	br.close(); fr.close();
                	}
                }else {
                	emailbody = bodyArg.getValue();
                }
 
                email.setHtmlMsg("<html>" + emailbody + "</html>");
                //System.out.println("Email sent with html");
                String txtMsg = org.apache.commons.lang.StringUtils.replace(emailbody,"<br>","\n");
                txtMsg = org.apache.commons.lang.StringUtils.replace(txtMsg,"<br/>","\n");
                txtMsg = org.apache.commons.lang.StringUtils.replace(txtMsg,"</br>","\n");
                   
                email.setTextMsg(txtMsg);
      
                //email.setMsg(bodyArg.getValue());
    
                if (attachArgs != null && attachArgs.size() > 0) {
                    for (int i = 0; i < attachArgs.size(); i++) {
                        //Create the attachment
                        EmailAttachment attachment = new EmailAttachment();
                        attachment.setPath(((ArgumentData)attachArgs.get(i)).getValue());
                        attachment.setDisposition(EmailAttachment.ATTACHMENT);
                        attachment.setDescription(((ArgumentData)attachArgs.get(i)).getDescription());
                        attachment.setName(StringUtils.afterLastSlash(((ArgumentData)attachArgs.get(i)).getValue()));
                        email.attach(attachment);
                    }
                }
                //send the email
                email.send();
                if (outputFileName != null) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName, true));
                    out.write("\n--------------------------------------------\n");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    out.write( dateFormat.format(Calendar.getInstance().getTime()) + "\n");
                    out.write("Email sent to " );
                    for (int i = 0; i < toArgs.size(); i++) 
                        out.write(((ArgumentData)toArgs.get(i)).getValue());
                    out.write("  on " + email.getSentDate() + "\n");
                    out.write("Subject: " + subjectArg.getValue() + "\n");
                    out.write("Body: " + emailbody);
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
                notification.setCommand("Email sent to " + ((ArgumentData)toArgs.get(0)).getValue() + "  on " + email.getSentDate() + "  Subject:: " + subjectArg.getValue());
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
                    out.write("Couldnt send email. Exception:: " + e.getLocalizedMessage() );
                    out.write("\n--------------------------------------------\n");
                    out.close();
                }
            }catch(Exception e1){}
            throw new PipelineException("Personnel notification email couldnt be sent " + e.getClass() + e.getLocalizedMessage(),e);
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
