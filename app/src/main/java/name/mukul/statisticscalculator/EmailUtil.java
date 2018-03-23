package name.mukul.statisticscalculator;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtil {

    public static boolean sendEmail(String emailFrom, String emailTo, String filePath) {
        final String username = "#####";
        final String password = "#####";

        String host = "smtp.sendgrid.net";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        session.setDebug(true);

        try {
            String subject = "Message from Hello Stats App";
            // create a message
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(emailFrom));
            InternetAddress[] address = {new InternetAddress(emailTo)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject, "UTF-8");

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText("Please see the attachment, for the information you have requested from Hello Stats App.\n", "UTF-8");

            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            mbp2.attachFile(filePath);

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            // add the Multipart to the message
            msg.setContent(mp);

            // set the Date: header
            msg.setSentDate(new Date());

            // send the message
            Transport.send(msg);

            return true;

        } catch (MessagingException mex) {
            return false;
        }
        catch (IOException ioex) {
            return false;
        }
    }

}
