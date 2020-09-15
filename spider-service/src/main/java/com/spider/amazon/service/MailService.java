package com.spider.amazon.service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import java.io.IOException;

public interface MailService {

    /**
     * Return true is the {@link MailService} is connected host
     * @return
     */
    boolean isLoggedIn();

    /**
     * Login gmail service on host
     *
     * @param username
     * @param password
     */
    void login(String username, String password) throws MessagingException;

    /**
     * Logout from mail service
     */
    void logout() throws MessagingException;

    /**
     * Using google voice to receive otp from amazon vc,
     * then use gmail server to get the code
     *
     * @return
     */
    String getLastAmazonVCOTP() throws MessagingException, IOException;

    Message[] getMessages();

}
