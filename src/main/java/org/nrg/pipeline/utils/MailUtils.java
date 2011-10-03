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

import java.util.ArrayList;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

public class MailUtils {
        
    
    
    
    public static void send(String subject,String body, ArrayList emailIds, String outfilepath, String errorfilepath) throws Exception
    {
      if (emailIds != null && emailIds.size() > 0) {
          HtmlEmail sm = new HtmlEmail();
          sm.setHostName(PipelineProperties.PIPELINE_SMTP_HOST);
          for (int i = 0; i < emailIds.size(); i++)
              sm.addTo((String)emailIds.get(i));
          sm.setFrom(PipelineProperties.PIPELINE_EMAIL_ID);
          sm.setSubject(subject);
          sm.setHtmlMsg("<html>" + body + "</html>");
          sm.setTextMsg(org.apache.commons.lang.StringUtils.replace(body,"<br>","\n"));
          if (outfilepath != null ) {
              EmailAttachment attachment = new EmailAttachment();
              attachment.setPath(outfilepath);
              attachment.setDisposition(EmailAttachment.ATTACHMENT);
              sm.attach(attachment);
          }
          if (errorfilepath != null) {
              EmailAttachment attachment = new EmailAttachment();
              attachment.setPath(errorfilepath);
              attachment.setDisposition(EmailAttachment.ATTACHMENT);
              sm.attach(attachment);
          }
          sm.send();
          System.out.println("Message sent OK.");
      }
    }
    
    
    
    public static void main(String[] args) {
        ArrayList emails = new ArrayList();
        emails.add("mohanar@npg.wustl.edu");
        try { 
        MailUtils.send("Test", "Hello there",emails, null, null);
        }catch(Exception e) {e.printStackTrace();}
    }


}


