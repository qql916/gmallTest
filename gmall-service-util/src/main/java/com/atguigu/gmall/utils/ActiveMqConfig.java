package com.atguigu.gmall.utils;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;

@Configuration
public class ActiveMqConfig {


    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL;

    @Value("${activemq.listener.enable:disabled}")
    String listenerEnable;


    @Bean
    public ActiveMQUtil getActiveMQUtil() {
        if ("disabled".equals(brokerURL)) {
            return null;
        }
        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        activeMQUtil.init(brokerURL);
        return activeMQUtil;
    }


    //定义一个消息监听器连接工厂，这里定义的是点对点模式的监听器连接工厂
    @Bean(name = "jmsQueueListener")
    public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {

        if ("disabled".equals(listenerEnable)) {
            return null;
        }

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);

        factory.setSessionTransacted(false);

        //重连间隔时间
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        factory.setConcurrency("5");
        factory.setRecoveryInterval(5000L);

        return factory;

    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {

        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(brokerURL);
        return activeMQConnectionFactory;
    }
}
