package org.test.impl;

import org.test.EmailSender;
import org.test.ParsingSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

/**
 * Created by IVANMO on 11/5/2017.
 */
public class ParsingSenderCsv implements ParsingSender {

    private  BlockingQueue<String[]> queue;

    static final String[] POISON_PILL = new String[0];

    private String fileName;
    private EmailSender emailSender;
    private int cores;


    public ParsingSenderCsv(String fileName, EmailSender emailSender) {

        this.fileName = fileName;
        this.emailSender = emailSender;
        this.cores = Runtime.getRuntime().availableProcessors();
        this.queue = new ArrayBlockingQueue<>(cores * 5);
    }

    public void parseAndSend() {

        emailSender.startSender (cores, queue);

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            stream.map(line -> line.replace("\"", ""))
                    .map(line -> line.split(";"))
                    .forEach(arrLine -> {

                        try {
                            queue.put(arrLine);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    });


            queue.put(POISON_PILL);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        emailSender.stopSender();


    }
}
