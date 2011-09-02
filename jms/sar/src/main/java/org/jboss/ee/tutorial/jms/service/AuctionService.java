/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ee.tutorial.jms.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/**
 * @author thomas.diesler@jboss.com
 * @since 01-Sep-2011
 */
public class AuctionService implements AuctionServiceMBean, MessageListener {

    private static final Logger log = Logger.getLogger(AuctionService.class);

    // A new aution every 30sec
    static final Integer NEW_AUCTION_INTERVAL = 30000;
    // Every 2sec a broadcast of all auctions
    static final Integer AUCTIONS_BROADCAST_INTERVAL = 2000;
    // Interval after the last bid to close an auction
    static final Integer AUCTION_TIMEOUT = 30000;
    // A bid that immediately closes the auction
    static final Integer IMMEDIATE_CLOSE_BID = 10000;

    private final Map<String, Auction> auctions = new LinkedHashMap<String, Auction>();
    private final AtomicInteger auctionIndex = new AtomicInteger();

    private ExecutorService executor = Executors.newCachedThreadPool();
    private Connection connection;
    private Session session;
    private Topic topicBroadcast;
    private Topic topicClosed;
    private Queue queueBid;

    // [AS7-1699] Support @Resource injection for SAR deployed MBeans
    // @Resource(mappedName = "java:/ConnectionFactory")
    // public ConnectionFactory connectionFactory;

    @Override
    public void start() throws Exception {

        log.infof("Start auction service");
        
        InitialContext ctx = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("java:/ConnectionFactory");
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        topicBroadcast = (Topic) ctx.lookup("topic/auction-broadcast");
        topicClosed = (Topic) ctx.lookup("topic/auction-closed");
        queueBid = (Queue) ctx.lookup("queue/auction-bid");

        QueueReceiver queueReceiver = (QueueReceiver) session.createConsumer(queueBid);
        queueReceiver.setMessageListener(this);

        connection.start();

        // New auction thread
        executor.execute(new Runnable() {
            public void run() {
                while (!executor.isShutdown()) {
                    Auction auction = new Auction("auction-" + auctionIndex.incrementAndGet());
                    auctions.put(auction.getName(), auction);
                    try {
                        Thread.sleep(NEW_AUCTION_INTERVAL);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });

        // Auction close thread
        executor.execute(new Runnable() {
            public void run() {
                while (!executor.isShutdown()) {
                    long currentTime = System.currentTimeMillis();
                    for (Auction aux : new ArrayList<Auction>(auctions.values())) {
                        boolean timeout = aux.getLastBid() + AUCTION_TIMEOUT < currentTime;
                        if (timeout == true) {
                            aux.close();
                        }
                        if (aux.isClosed()) {
                            auctions.remove(aux.getName());
                            try {
                                MessageProducer producer = session.createProducer(topicClosed);
                                TextMessage msg = session.createTextMessage(aux.toString());
                                producer.send(msg);
                            } catch (JMSException ex) {
                                log.warnf("Cannot send message: %s", ex);
                            }
                        }
                    }
                    try {
                        Thread.sleep(AUCTIONS_BROADCAST_INTERVAL);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });

        // Auctions broadcast thread
        executor.execute(new Runnable() {
            public void run() {
                while (!executor.isShutdown()) {
                    log.infof("broadcast: %s", auctions.values());
                    try {
                        String text = auctions.keySet().toString();
                        text = text.substring(1, text.length() - 1);
                        MessageProducer producer = session.createProducer(topicBroadcast);
                        TextMessage msg = session.createTextMessage(text);
                        producer.send(msg);
                    } catch (JMSException ex) {
                        log.warnf("Cannot send message: %s", ex);
                        executor.shutdownNow();
                    }
                    try {
                        Thread.sleep(AUCTIONS_BROADCAST_INTERVAL);
                    } catch (Exception ex) {
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void stop() throws Exception {
        log.infof("Stop auction service");
        if (executor != null) {
            executor.shutdownNow();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void onMessage(Message message) {
        log.infof("onMessage: %s", message);
        try {
            if (message instanceof BytesMessage) {
                String name = message.getStringProperty("auction.name");
                String bid = message.getStringProperty("auction.bid");
                Auction auction = auctions.get(name);
                auction.addBid(new Integer(bid));
            } else {
                log.warnf("Invalid message: %s", message);
            }
            
        } catch (JMSException ex) {
            log.errorf(ex, "Cannot process message: %s", message);
        }
    }

    static class Auction {
        private final String name;
        private int highestBid;
        private long lastBid;
        private boolean closed;

        Auction(String name) {
            this.name = name;
            this.lastBid = System.currentTimeMillis();
            log.infof("new Auction: %s", this);
        }

        void addBid(Integer bid) {
            if (closed == false && bid > highestBid) {
                log.infof("accepted %d for %s:", bid, this);
                lastBid = System.currentTimeMillis();
                highestBid = bid;
                if (bid >= IMMEDIATE_CLOSE_BID) {
                    close();
                }
            } else {
                log.infof("rejected %d for %s:", bid, this);
            }
        }

        String getName() {
            return name;
        }

        Integer getHighestBid() {
            return highestBid;
        }

        long getLastBid() {
            return lastBid;
        }

        void close() {
            if (closed == false) {
                log.infof("close %s:", this);
                this.closed = true;
            }
        }

        boolean isClosed() {
            return closed;
        }

        @Override
        public String toString() {
            return "Auction [name=" + name + ", bid=" + highestBid + "]";
        }
    }
}