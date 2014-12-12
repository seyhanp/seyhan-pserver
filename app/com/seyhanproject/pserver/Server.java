/**
* Copyright (c) 2015 Mustafa DUMLUPINAR, mdumlupinar@gmail.com
*
* This file is part of seyhan project.
*
* seyhan is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.seyhanproject.pserver;

import java.util.HashMap;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	private static final long STORAGE_LIMIT = 1024 * 1024 * 2; //2 MB
	private static final long EXPIRATION = 20 * 60 * 1000; //twenty minutes
	private static final String DEFAULT_BROKER_IP = "failover:(tcp://localhost:61616)";

	private static boolean isStarted = false;
	private static BrokerService broker;
    private static ActiveMQSession session;
    private static ActiveMQConnection connection;

    static Map<String, MessageProducer> producerMap = new HashMap<String, MessageProducer>();

    static void stop() {
    	log.info("Session, Connection and Broker are closed...");
    	if (session != null) {
	    	try { session.close(); } catch (JMSException e) { }
	    	if (connection != null) {
		        try { connection.close(); } catch (JMSException e) { }
		        try { if (broker != null) broker.stop(); } catch (Exception e) { }
	    	}
    	}
        log.info("Session, Connection and Broker have closed.");
    }

    public static boolean start(String brokerIp) {
    	String ip = (brokerIp != null && ! brokerIp.trim().isEmpty() ? brokerIp : DEFAULT_BROKER_IP).replace("failover:(", "").replace(")", "");
        try {
            broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector(ip);

            SystemUsage systemUsage = broker.getSystemUsage();
            systemUsage.getStoreUsage().setLimit(STORAGE_LIMIT);
            systemUsage.getTempUsage().setLimit(STORAGE_LIMIT);
            systemUsage.getMemoryUsage().setLimit(STORAGE_LIMIT);

            broker.start();
            log.info("Printing Broker has built on " + ip + " successfuly");
            isStarted = true;
        } catch (Exception e) {
			log.error("ERROR", e);
        }

        return isStarted;
    }
 
    private static MessageProducer getProducer(String queueName, String brokerIp) {
    	if (! isStarted) {
    		if (! start(brokerIp)) return null;
    	}

    	if (connection == null || connection.isClosed() || connection.isClosing()) {
	    	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerIp != null && ! brokerIp.trim().isEmpty() ? brokerIp : DEFAULT_BROKER_IP);
	        try {
	            connection = (ActiveMQConnection) connectionFactory.createConnection();
	            connection.start();
	        } catch (JMSException e) {
	        	log.error("ERROR", e);
	        }
    	}

		if (session == null || session.isClosed() || ! session.isRunning()) {
	        try {
	        	session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        } catch (JMSException e) {
	        	log.error("ERROR", e);
	        }
		}

    	MessageProducer producer = producerMap.get(queueName);
    	if (producer == null) {
	        try {
		    	Destination destination = session.createQueue(queueName);
		        producer = session.createProducer(destination);
		        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		        producer.setTimeToLive(EXPIRATION);
		        producerMap.put(queueName, producer);
	        } catch (JMSException e) {
	        	log.error("ERROR", e);
	        }
    	}

        return producer;
    }
 
    static void send(Document document) {
    	MessageProducer producer = getProducer(document.targetName, document.brokerIp);
    	if (producer != null) {
	    	ObjectMessage msg;
			try {
				msg = session.createObjectMessage();
				msg.setObject(document);
				producer.send(msg);
			} catch (JMSException e) {
				log.error("ERROR", e);
			}
    	} else {
    		log.error("There was an error when getting " + document.targetName + " queue!");
    	}
    }

}
