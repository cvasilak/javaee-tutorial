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
package org.jboss.ee.tutorial.jms.client;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.StompMessages;
import org.projectodd.stilts.stomp.Subscription.AckMode;
import org.projectodd.stilts.stomp.client.ClientSubscription;
import org.projectodd.stilts.stomp.client.MessageHandler;
import org.projectodd.stilts.stomp.client.StompClient;
import org.projectodd.stilts.stomp.client.SubscriptionBuilder;

/**
 * The JMS client test
 * 
 * @author thomas.diesler@jboss.com
 * @since 01-Sep-2011
 */
@RunAsClient
@RunWith(Arquillian.class)
public class StompClientTestCase {

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        File serverTargetDir = new File("../sar/target");
        String[] list = serverTargetDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        File warFile = new File(serverTargetDir + "/" + list[0]);
        JavaArchive archive = ShrinkWrap.createFromZipFile(JavaArchive.class, warFile);
        return archive;
    }

    @Test
    public void testAuctionService() throws Exception {
        StompClient client = new StompClient("stomp://localhost:61613");
        client.connect();

        final StringBuffer broadcastList = new StringBuffer();
        final CountDownLatch broadcastLatch = new CountDownLatch(1);
        class BroadcastHandler implements MessageHandler {
            public void handle(StompMessage message) {
                broadcastList.append(message.getContentAsString());
                broadcastLatch.countDown();
            }
        }
        
        SubscriptionBuilder broadcastBuilder = client.subscribe("jms.topic.auction-broadcast");
        broadcastBuilder.withMessageHandler(new BroadcastHandler());
        broadcastBuilder.withAckMode(AckMode.AUTO);
        ClientSubscription subscription = broadcastBuilder.start();
        
        assertTrue("Broadcast message received", broadcastLatch.await(10, TimeUnit.SECONDS));
        
        String[] split = broadcastList.toString().split(",");
        assertTrue("At least one auction", split.length > 0);
        
        subscription.unsubscribe();
        
        StompMessage msg = StompMessages.createStompMessage();
        msg.setDestination("jms.queue.auction-bid");
        msg.getHeaders().put("auction.name", split[0]);
        msg.getHeaders().put("auction.bid", "50000");
        client.send(msg);
        
        final StringBuffer closedMessage = new StringBuffer();
        final CountDownLatch closedLatch = new CountDownLatch(1);
        class ClosedHandler implements MessageHandler {
            public void handle(StompMessage message) {
                closedMessage.append(message.getContentAsString());
                closedLatch.countDown();
            }
        }
        
        SubscriptionBuilder closedBuilder = client.subscribe("jms.topic.auction-closed");
        closedBuilder.withMessageHandler(new ClosedHandler());
        closedBuilder.withAckMode(AckMode.AUTO);
        subscription = closedBuilder.start();
        
        assertTrue("Closed message received", closedLatch.await(10, TimeUnit.SECONDS));

        subscription.unsubscribe();

        client.disconnect();
    }
}
