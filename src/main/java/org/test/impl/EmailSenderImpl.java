package org.test.impl;

import org.test.EmailSender;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IVANMO on 11/5/2017.
 */
public class EmailSenderImpl implements EmailSender {

    public static int counter;
    private CountDownLatch latch;

    private BlockingQueue<String[]> queue;
    private ExecutorService executorService;
    private final static String myAddress = "ivanmo@gmail.com";

    private String host;
    private String port;

    EmailSenderImpl(String host, String port){

        this.host = host;
        this.port = port;
    }

    @Override
    public void startSender(int cores, BlockingQueue queue) {

        this.queue = queue;

        this.latch = new CountDownLatch(cores);
        this.executorService = Executors.newFixedThreadPool(cores);
        for (int i=0; i<cores; i++) {

            this.executorService.execute( new EmailSenderWorker());

        }
    }

    @Override
    public void stopSender() {

        this.executorService.shutdown();
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class EmailSenderWorker implements Runnable {

        Properties properties;

        public EmailSenderWorker() {

            properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
        }

        public void run() {

            Session session = Session.getInstance(properties);
            Transport transport = null;

            try{
                transport = session.getTransport();
                transport.connect();

                while (true) {

                    try {

                        String [] lineArr = queue.take();
                        if (lineArr==ParsingSenderCsv.POISON_PILL){
                            queue.put(ParsingSenderCsv.POISON_PILL);
                            latch.countDown();
                            return;
                        }

                        MimeMessage message = getMimeMessage(session, myAddress, lineArr);
                        transport.send(message);

                        Thread.sleep(500);
                        System.out.println("Email was sent to " + lineArr[1] + " " + lineArr[2] + "...");
                        counter++;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            } catch (MessagingException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(transport != null) {
                        transport.close();
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }

        }

        private MimeMessage getMimeMessage(Session session, String fromAddress, String[] lineArr) throws MessagingException {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(lineArr[0]));
            message.setSubject("Hello");
            message.setText("Hello " + lineArr[1] + " " + lineArr[2] + "!");
            return message;
        }
    }


}
