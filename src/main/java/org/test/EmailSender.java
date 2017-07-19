package org.test;

import java.util.concurrent.BlockingQueue;

/**
 * Created by IVANMO on 11/5/2017.
 */
public interface EmailSender {

    void startSender(int cores, BlockingQueue queue);
    void stopSender();
}
