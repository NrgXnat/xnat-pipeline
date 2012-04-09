/*
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 *
 */

package org.nrg.pipeline.utils;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: MailUtils.java,v 1.2 2009/11/11 21:03:41 mohanar Exp $
 @since Pipeline 1.0
 */

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.nrg.mail.services.impl.RestBasedMailServiceImpl;

import javax.mail.MessagingException;
import java.io.File;
import java.util.*;

public class MailUtils {

	public static void send(String subject, String body, List<String> emailIds, String outfilepath, String errorfilepath) throws EmailException {
        if (emailIds != null && emailIds.size() > 0) {
            List<String> tos = new ArrayList<String>();
            for (int i = 0; i < emailIds.size(); i++)
                tos.add((String) emailIds.get(i));

            String from = PipelineProperties.getPipelineEmail();
            String html = "<html>" + body + "</html>";
            String text = org.apache.commons.lang.StringUtils.replace(body, "<br>", "\n");

            Map<String, File> attachments = new HashMap<String, File>();

            if (outfilepath != null) {
                attachments.put(StringUtils.afterLastSlash(outfilepath), new File(outfilepath));
            }
            if (errorfilepath != null) {
                attachments.put(StringUtils.afterLastSlash(errorfilepath), new File(errorfilepath));
            }

            MailMessage message = new MailMessage();
            message.setFrom(from);
            message.setTos(tos);
            message.setSubject(subject);
            message.setHtml(html);
            message.setText(text);
            message.setAttachments(attachments);

            MailUtils.send(message);
        }
    }

	public static void send(MailMessage message) throws EmailException {
		try {
			getMailService().sendMessage(message);
            System.out.println("Message sent OK.");
		} catch (MessagingException exception) {
            System.out.println("Message failed to send through REST service, retrying with direct SMTP.");
            HtmlEmail email = message.asHtmlEmail();
            email.setHostName(PipelineProperties.getPipelineSmtpHost());
            if (!org.apache.commons.lang.StringUtils.isBlank(PipelineProperties.getPipelineSmtpUser()) &&
            	!org.apache.commons.lang.StringUtils.isBlank(PipelineProperties.getPipelineSmtpUser())) {
            		email.setAuthentication(PipelineProperties.getPipelineSmtpUser(), PipelineProperties.getPipelineSmtpPass());
            }
            email.send();
            System.out.println("Message sent OK.");
		}
	}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new RuntimeException("You must specify the location of the properties file and at least one email address to which to send a test email.");
        }
        PipelineProperties.init(args[0]);
        List<String> emails = new ArrayList<String>();
        for (int index = 1; index < args.length; index++) {
            emails.add(args[index]);
        }
        try {
            MailUtils.send("Test", "Hello there", emails, null, null);
        } catch (Exception e) {

        }
    }

    // TODO: Optimally, the _service field would be populated by dependency injection, e.g. Spring or Turbine.
    private static MailService getMailService() {
    	if (_service == null) {
			_service = new RestBasedMailServiceImpl(PipelineProperties.getPipelineRestMailService(), PipelineProperties.getPipelineRestMailUser(), PipelineProperties.getPipelineRestMailPassword());
    	}
    	return _service;
    }

    private static MailService _service;
}
