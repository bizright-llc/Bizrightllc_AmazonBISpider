package com.spider.amazon.service.impl;

import com.spider.amazon.service.MailService;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.*;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implement of {@link MailService}
 */
public class MailServiceImpl implements MailService {

    private Session session;
    private Store store;
    private Folder folder;

    // hardcoding protocol and the folder
    // it can be parameterized and enhanced as required
    private String protocol = "imap";
    private String file = "INBOX";

    private final String GMAIL_IMAP_HOST = "imap.gmail.com";
    private final int GMAIL_IMAP_PORT = 993;
    private final String GMAIL_POP3_HOST = "pop.gmail.com";

    final SearchTerm notSeanFilter = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
    final SearchTerm receiveTodayFilter = new ReceivedDateTerm(ComparisonTerm.EQ, new Date());

    @Override
    public boolean isLoggedIn() {
        if (store == null) {
            return false;
        }
        return store.isConnected();
    }

    @Override
    public void login(String username, String password) throws MessagingException {
//        URLName url = new URLName(protocol, GMAIL_HOST, 587, file, username, password);

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.ssl.enable", "true");

        Session session = Session.getDefaultInstance(props, null);

        store = session.getStore("imaps");
        store.connect("imap.gmail.com",username, password);

        // session.setDebug(true);
//        store = session.getStore("imaps");
//        store.connect("imap.gmail.com",username, password);

        folder = store.getFolder("Inbox");
        folder.open(Folder.READ_WRITE);
    }

    @Override
    public void logout() throws MessagingException {
        folder.close(false);
        store.close();
        store = null;
        session = null;
    }

    @Override
    public String getLastAmazonVCOTP() throws MessagingException, IOException {

        if(!isLoggedIn()){
            return "";
        }

        SearchTerm bodyContainerAmazonOTP = new BodyTerm("Amazon OTP");

        final SearchTerm[] filters = {notSeanFilter, receiveTodayFilter, bodyContainerAmazonOTP};

        final SearchTerm finalSearchTerm = new AndTerm(filters);

        Message[] messages = folder.search(finalSearchTerm);

        final String regex = "[0-9].+?(?= is your Amazon)";

        String otpCode = "";

        for (Message msg: messages){
            Date receiveDate = msg.getReceivedDate();
            String body = "";
            if(msg.isMimeType("text/plain")){
                body = msg.getContent().toString();
            }else if (msg.isMimeType("multipart/*")){
                MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
                body = getTextFromMimeMultipart(mimeMultipart);
            }

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(body);

            if(matcher.find()){
                String otp = matcher.group(0);

                otpCode = otp;

            }
        }

        return otpCode;
    }

    public String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder resultSb = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                resultSb.append("\n" + bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                resultSb.append("\n" + org.jsoup.Jsoup.parse(html).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                resultSb.append(getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent()));
            }
        }
        return resultSb.toString();
    }

    @Override
    public Message[] getMessages() {
        return new Message[0];
    }

//    public void connectGMailIMAPHost(String username, String password) throws MessagingException {
//        String host = "imap.gmail.com";
//        Properties props = new Properties();
//        props.setProperty("mail.imap.ssl.enable", "true");
//        // set any other needed mail.imap.* properties here
//        Session session = Session.getInstance(props);
//        Store store = session.getStore("imap");
//        store.connect(host, username, password);
//    }

    public static void main(String[] args) throws MessagingException, IOException {
        MailService mailService = new MailServiceImpl();
        mailService.login("bizright.spider@gmail.com", "Lovebizright");
        String otp = "";
        for (int i=0; i< 5; i++){
            otp = mailService.getLastAmazonVCOTP();
            if(StringUtils.isNotEmpty(otp)){
                break;
            }
        }

        System.out.println(String.format("Amazon OTP %s", otp));
    }

}
