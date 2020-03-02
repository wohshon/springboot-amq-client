package com.redhat.demo;


import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 */
@EnableJms
@RestController
@RequestMapping(value = "/")
public class Controller {

    @Value("${put.queue}")
    private String putQueue;

    @Value("${listen.queue}")
    private String listenQueue;

    //@Value("${loop}")
    //private int loop;

    @Autowired
    private JmsTemplate jmsTemplate;
    Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @GetMapping("/send/{message}")
    public String send(@PathVariable("message") String msg) {
        log.debug(msg);

        //sendMessage(msg,Integer.parseInt(loop));
        sendMessage(msg);
        return msg;
    }

    @GetMapping("/send/{message}/{loop}")
    public String send(@PathVariable("message") String msg, @PathVariable("loop") int loop) {
        log.debug(msg);

        //sendMessage(msg,Integer.parseInt(loop));
        sendMessage(msg,loop);
        return msg;
    }   
    public void sendMessage(String text, int loop) {
         String correlationId = null;
        System.out.println(String.format("Loop Sending '%s' '%s'", text,loop));
        System.out.println(String.format("Sending '%s'", text));
        for (int i=0; i < loop; i++) {
            correlationId = UUID.randomUUID().toString();
            this.jmsTemplate.convertAndSend(putQueue, text+"_"+loop, new CorrelationIdPostProcessor(correlationId));
        }
    }

    public void sendMessage(String text) {
        final String correlationId = UUID.randomUUID().toString();

        System.out.println(String.format("Sending '%s'", text));
        this.jmsTemplate.convertAndSend(putQueue, text, new CorrelationIdPostProcessor(correlationId));
    }

    
    @JmsListener(destination = "${listen.queue}")
    public void listen(Message msg) {
        try {
            System.out.println("listen queue");
            System.out.println(((TextMessage) msg).getText());
            System.out.println(((TextMessage) msg). getJMSCorrelationID());
            System.out.println(((TextMessage) msg).getJMSReplyTo());

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    //@JmsListener(destination = "${put.queue}")
    public void listenReply(Message msg) {
        try {
            System.out.println("put queue");
            String text= ((TextMessage) msg).getText();
            System.out.println(text);
            System.out.println(((TextMessage) msg). getJMSCorrelationID());
            System.out.println(((TextMessage) msg).getJMSReplyTo());
            System.out.println(String.format("replying '%s'",text));
            //this.jmsTemplate.convertAndSend(putQueue, text+"_reply", new CorrelationIdPostProcessor(((TextMessage) msg).getJMSCorrelationID()));
    
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }    
	private class CorrelationIdPostProcessor implements MessagePostProcessor {
		private final String correlationId;

		public CorrelationIdPostProcessor(final String correlationId) {
			this.correlationId = correlationId;
		}

		@Override
		public Message postProcessMessage(final Message msg)
				throws JMSException {
            msg.setJMSCorrelationID(correlationId);
            msg.setJMSReplyTo(new Queue(){
            
                @Override
                public String getQueueName() throws JMSException {
                    return putQueue;
                }
            });
			return msg;
		}
	}    

}