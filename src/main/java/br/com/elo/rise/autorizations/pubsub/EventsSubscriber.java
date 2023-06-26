package br.com.elo.rise.autorizations.pubsub;

import java.time.Duration;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import br.com.elo.rise.autorizations.channels.ISO8583Message;
import br.com.elo.rise.autorizations.queue.ChannelQueue;

public class EventsSubscriber extends Thread {

	private static final String TOPIC = "channel-out";
	private static final String BOOTSTRAP_SERVERS = "localhost:9092";

	private ChannelQueue queue = new ChannelQueue();
	private boolean active = true;

	public EventsSubscriber(ChannelQueue queue) {
		super("Events Subscriber");
		this.queue = queue;
	}

	@Override
	public void interrupt() {
		this.active = false;
	}

	@Override
	public void run() {
		this.active = true;
		runConsumer();
	}

    private void runConsumer() {

    	Properties properties = new Properties();
    	properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

//    	properties.setProperty("bootstrap.servers","pkc-n98pk.us-west-2.aws.confluent.cloud:9092");
//        properties.setProperty("security.protocol","SASL_SSL");
//        properties.setProperty("sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username='TRSW4OBVDC75USGY' password='YngDHYbC+rLONzlAKEGiZB21gdQkI82U77c6AX+X3okjPW7c2lu4PzhZM0fZbKnx';");
//        properties.setProperty("sasl.mechanism","PLAIN");
//        properties.setProperty("client.dns.lookup","use_all_dns_ips");
//        properties.setProperty("session.timeout.ms","45000");
//        properties.setProperty("acks","all");
//        properties.setProperty("schema.registry.url","https://psrc-mw731.us-east-2.aws.confluent.cloud");
//        properties.setProperty("basic.auth.credentials.source","USER_INFO");
//        properties.setProperty("basic.auth.user.info","PLNCGD7KVGUY7C7B:hXFYUj0D7JzvWnFh9MN2nD4s+vM5XkrvIbzjxTxMfxONS+r8DNqNu+B2G/22qEP3");

    	properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "channel-out");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(java.util.Collections.singletonList(TOPIC));

        try {
            while (active) {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    queue.offer(ISO8583Message.fromBytes(record.value()));
                }
            }
        } finally {
            consumer.close();
        }
    }
}