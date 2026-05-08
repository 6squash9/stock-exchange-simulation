package com.tradecore.exchange.sequencer;

import com.tradecore.exchange.matchingengine.MatchingEngine;
import com.tradecore.exchange.order.ISimpleOrder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SequencerService implements Sequencer,Runnable{
    private static final Logger logger = LoggerFactory.getLogger(SequencerService.class);
    //shared space
   private final BlockingQueue<ISimpleOrder> orderQueue = new LinkedBlockingQueue<>();
//    thread that runs the sequencer
    private Thread workerThread;
    //flag for sequencer
    private volatile boolean running = false;
    private final MatchingEngine matchingEngine;

    //constructor
    public SequencerService(MatchingEngine matchingEngine){
        this.matchingEngine = matchingEngine;
    }


    @Override
    public void enqueueOrder(ISimpleOrder order) {
        orderQueue.offer(order); //nonblocking
        logger.debug("[Sequencer] Received order {} with sequence ID: {}", order.getOrderId(), order.getSequenceId());
    }

    @Override
    public void start() {
        if(running){
            return; //keep the sequencer running
        }
        running = true;
        this.workerThread = new Thread(this,"sequencer thread"); //runnable task,String ,  //same object , different call stacks
        this.workerThread.start(); // start the thread
        logger.info("[Sequencer] Service started.");
    }

    @Override
    public void run(){
        //we keep the service running until running = false
        while (running){
            try {
                ISimpleOrder order = this.orderQueue.take(); //waits until the queue has something
                logger.debug("[Sequencer] Processing order {} (seq: {}) → Matching Engine", order.getOrderId(), order.getSequenceId());

                //capture order time stamp just before sending it to matching engine (covers queue wait time + sequencer processing time)
                long timeSequenced = System.nanoTime();
                order.setTimestampSequenced(timeSequenced);

                logger.info("[Sequencer] Sending order {} → MatchingEngine", order.getOrderId());
                matchingEngine.AddOrder(order);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
                logger.info("[Sequencer] Thread interrupted, shutting down...");
            }

        }
    }

    @Override
    public void stop() {
        running = false;
        if(this.workerThread != null){
            this.workerThread.interrupt();
        }
        logger.info("[Sequencer] Service stopped.");
    }

    public int getQueueSize(){
        return orderQueue.size();
    }
    public boolean isRunning(){
        return running;
    }
}
