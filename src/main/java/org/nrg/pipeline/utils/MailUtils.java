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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.services.MailService;
import org.nrg.mail.services.impl.RestBasedMailServiceImpl;

import java.io.File;
import java.util.*;

public class MailUtils {

	public static void send(String subject, String body, List<String> emailIds, String outfilepath, String errorfilepath) throws EmailException {
        if (emailIds != null && emailIds.size() > 0) {
            _log.debug("Sending message with subject: " + subject);
            List<String> tos = new ArrayList<String>(emailIds);

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
		} catch (Exception exception) {
            _log.error("Message failed to send through REST service, retrying with direct SMTP.", exception);
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

	public static void send(MailMessage message, String username, String password) throws EmailException {
		try {
			getMailService().sendMessage(message, username, password);
            _log.debug("Message sent OK.");
		} catch (Exception exception) {
            _log.error("Message failed to send through REST service, retrying with direct SMTP.", exception);
            HtmlEmail email = message.asHtmlEmail();
            email.setHostName(PipelineProperties.getPipelineSmtpHost());
            if (!org.apache.commons.lang.StringUtils.isBlank(PipelineProperties.getPipelineSmtpUser()) &&
            	!org.apache.commons.lang.StringUtils.isBlank(PipelineProperties.getPipelineSmtpUser())) {
            		email.setAuthentication(PipelineProperties.getPipelineSmtpUser(), PipelineProperties.getPipelineSmtpPass());
            }
            email.send();
            _log.debug("Message sent OK.");
		}
	}

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new RuntimeException("You must specify the location of the properties file and at least one email address to which to send a test email.");
        }
        PipelineProperties.init(args[0]);
        List<String> emails = new ArrayList<String>(Arrays.asList(args).subList(1, args.length));
        try {
            MailUtils.send("Test", "Hello there", emails, null, null);
        } catch (Exception exception) {
            _log.error("Message failed to send through REST service, retrying with direct SMTP.", exception);
        }
    }

    // TODO: Optimally, the _service field would be populated by dependency injection, e.g. Spring or Turbine.
    private static MailService getMailService() throws NrgServiceException {
    	if (_service == null) {
            _service = new RestBasedMailServiceImpl(PipelineProperties.getPipelineRestMailService());
        }
    	return _service;
    }

    private static final Log _log = LogFactory.getLog(MailUtils.class);
    private static MailService _service;
}
