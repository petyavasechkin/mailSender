package org.test.impl;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.test.ParsingSender;
import org.test.EmailSender;

import static org.junit.Assert.*;

/**
 * Created by IVANMO on 11/5/2017.
 */
public class ParsingSenderCsvTest {


    private ParsingSender parsingSender;
    private EmailSender emailSender;
    private GreenMail mockMail;


    @Before
    public void testInit(){

        String fileName = "file.csv";
        String host = "localhost";
        int port = 30777;

        ServerSetup serverSetup = new ServerSetup(port, host, ServerSetup.PROTOCOL_SMTP);
        mockMail = new GreenMail(serverSetup);
        mockMail.start();

        emailSender = new EmailSenderImpl(host, String.valueOf(port));
        parsingSender = new ParsingSenderCsv(fileName ,emailSender);

    }

    @Test
    public void testParsingAndSend(){

        parsingSender.parseAndSend();
        Assert.assertEquals(20, EmailSenderImpl.counter);
    }

    @After
    public void shutDown(){
        mockMail.stop();
    }



}