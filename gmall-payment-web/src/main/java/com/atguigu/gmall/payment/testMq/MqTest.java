package com.atguigu.gmall.payment.testMq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;
import java.awt.geom.QuadCurve2D;

public class MqTest {
    public static void main (String[] args){
        //建立工厂
        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://192.168.12.200:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
            Queue testQueue = session.createQueue("Boss Thirsty");
            MessageProducer producer = session.createProducer(testQueue);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText("我口渴了，我要喝水 ");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(activeMQTextMessage);
            session.commit();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
