package com.hgz;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class rbbitmq {

    private static final String QUEUE="HGZ";

    public static void main(String[] args) {
        Connection connection=null;
        Channel channel=null;
        try {
            //创建连接工厂
            ConnectionFactory connectionFactory=new ConnectionFactory();
            connectionFactory.setHost("192.168.127.128");
            connectionFactory.setPort(5672);
            connectionFactory.setUsername("guest");
            connectionFactory.setPassword("guest");
            //rabbitmq默认虚拟机名称为“/”，虚拟机相当于一个独立的mq服务
            connectionFactory.setVirtualHost("/");
            //创建与RabbitMQ服务的TCP连接
            connection = connectionFactory.newConnection();
            //创建与Exchange的通道，每个连接可以创建多个通道，每个通道代表一个会话任务
            channel = connection.createChannel();
            /**
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            channel.queueDeclare(QUEUE,true,false,false,null);
            /***消息发布方法
             * param1：Exchange的名称，如果没有指定，则使用Default Exchange
             * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指定的消息队列
             * param3:消息包含的属性
             * param4：消息体
             */
            /** *这里没有指定交换机，消息将发送给默认交换机，
             * 每个队列也会绑定那个默认的交换机，但是不能显 示绑定或解除绑定
             * 默认的交换机，routingKey等于队列名称*/
            String string="Rabbitmq 测试"+System.currentTimeMillis();
            channel.basicPublish("",QUEUE,null,string.getBytes());
            System.out.println("Send Message");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            try {
                if(channel!=null){
                    channel.close();
                }
                if(connection!=null){
                    connection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}
