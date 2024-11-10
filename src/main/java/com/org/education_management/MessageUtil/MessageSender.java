package com.org.education_management.MessageUtil;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;

public class MessageSender {

    private static MessageSender messageSenderUtil = null;

    public static MessageSender getInstance(){
        if(messageSenderUtil == null){
            messageSenderUtil = new MessageSender();
        }
        return messageSenderUtil;
    }

    public HashMap sendMail(String recipient, String subject, String htmlContent, String attachmentPath){
        HashMap response = new HashMap<>();
        try{
            String sender = "academiccentralsup@gmail.com";
            String host   = "smtp.gmail.com";

            // Set up properties for the mail server
            // In future this props should be stored as an json file
            // That json file should be encrypted in db
            // There should be an util to decrypt the json and get the key values including the passwords
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            // Authenticate with the email server
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sender, "rsqt tkvb rmgn rgod");// replace with your password
                    //Academy@12345 account password for login
                }
            });

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Body part for the HTML message content
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html");
            multipart.addBodyPart(messageBodyPart);

            // Body part for the attachment
            if (attachmentPath != null) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                attachmentBodyPart.attachFile(new File(attachmentPath));
                multipart.addBodyPart(attachmentBodyPart);
            }

            // Set the multipart content
            message.setContent(multipart);

            // Send the message
            Transport.send(message);
            response.put("Email status","Mail sent successfully");
        }
        catch (Exception e){
            response.put("Email status","Mail sent failed with an exception : "+ e);
        }
        return response;
    }

    public HashMap sendText(String recipient, String content){
        HashMap response = new HashMap<>();
        try{
            Twilio.init("ACacb2519f03a73e88aa1cfcff82fae60c", "559e3a898aca3955b84a89f5ac979b7f");

            String textContent = Jsoup.parse(content).text();

            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(recipient),
                    "MG7e41d04ccc2a8da8b647cc1f399d2c8a",
                    textContent).create();
            if(message.getSid() != null){
                response.put("Text status","Text sent successfully");
            }
        }
        catch (Exception e){
            response.put("Text status","Text sent failed with an exception : "+ e);
        }
        return response;
    }
}
