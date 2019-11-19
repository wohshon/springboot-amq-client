package com.redhat.demo.config;

import javax.jms.ConnectionFactory;

import org.amqphub.spring.boot.jms.autoconfigure.AMQP10JMSConnectionFactoryCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 */
@Configuration
@ComponentScan
public class Config {

    @Value("${broker.url}")
    String brokerURL;
    @Bean
    public AMQP10JMSConnectionFactoryCustomizer myAMQP10Configuration() {
        return (factory) -> {
            factory.setUsername("admin");
            factory.setPassword("admin");
            factory.setPopulateJMSXUserID(true);
            //factory.setRemoteURI("failover:(amqp://192.168.0.110:5672,amqp://192.168.0.110:6672)");
            factory.setRemoteURI(brokerURL);

            // Other options such as custom SSLContext can be applied here
            // where they might otherwise be difficult to set via properties
            // file or URI.
        };
    }
    
}