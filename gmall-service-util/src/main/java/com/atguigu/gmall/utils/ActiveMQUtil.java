package com.atguigu.gmall.utils;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;


import javax.jms.Connection;
import javax.jms.JMSException;

public class ActiveMQUtil {

    PooledConnectionFactory pooledConnectionFactory=null;


    public  void init(String brokerUrl){
        ActiveMQConnectionFactory activeMQConnectionFactory=new ActiveMQConnectionFactory(brokerUrl);
        //加入连接池
        pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setExpiryTimeout(2000);//超时连接
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(10);//最大会话连接数
        pooledConnectionFactory.setMaxConnections(30);//最大ActivyMq连接数
        pooledConnectionFactory.setReconnectOnException(true);
        System.out.println("初始化mq连接池");
    }

    public Connection getConnection(){

        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
