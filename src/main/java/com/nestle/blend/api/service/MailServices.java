package com.nestle.blend.api.service;

import com.nestle.blend.api.vo.MailRecipient;
import com.nestle.blend.api.vo.MailVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class MailServices {

    @Value("${spring.mail.from}")
    private String emailFrom;
    @Value("${spring.mail.from.name}")
    private String emailFromName;

    @Autowired
    private Configuration configuration;
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    public void sendMail(MailVo mail) throws MessagingException {
        MimeMessage msg = this.javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        String[] recipients = new String[mail.getRecipients().size()];
        int i = 0;
        for(MailRecipient m : mail.getRecipients()){
            recipients[i++] = m.getRecipient();
        }
        try {
            helper.setFrom(this.emailFrom, this.emailFromName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        helper.setTo(recipients);

        helper.setSubject(mail.getSubject());
        helper.setText(mail.getMessage(), mail.isHtml());

        helper.addInline(
                "logo",
                new ClassPathResource("templates/logo.png")
        );

        this.javaMailSender.send(msg);
    }

    public String getEmailContent(String emailTemplate, Map<String, Object> params) throws IOException, TemplateException {
        Template template = this.configuration.getTemplate(emailTemplate);
        StringWriter stringWriter = new StringWriter();
        template.process(params, stringWriter);
        String html = stringWriter.getBuffer().toString();
        return html;
    }
}
