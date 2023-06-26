package br.com.elo.rise.autorizations.pubsub;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import br.com.elo.rise.autorizations.channels.ISO8583Message;
import br.com.elo.rise.autorizations.queue.ChannelQueue;

public class EventsPublisher extends Thread {

	private static final String TOPIC = "channel-in";
	private static final String BOOTSTRAP_SERVERS = "localhost:9092";

	private ChannelQueue queue = new ChannelQueue();
	private boolean active = true;

	public EventsPublisher(ChannelQueue queue) {
		super("Events Publisher");
		this.queue = queue;
	}

	@Override
	public void interrupt() {
		this.active = false;
	}

	@Override
	public void run() {
		this.active = true;
		runProducer();
	}

	private void runProducer() {

        // Configurações do produtor
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

//        properties.setProperty("bootstrap.servers","pkc-n98pk.us-west-2.aws.confluent.cloud:9092");
//        properties.setProperty("security.protocol","SASL_SSL");
//        properties.setProperty("sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username='TRSW4OBVDC75USGY' password='YngDHYbC+rLONzlAKEGiZB21gdQkI82U77c6AX+X3okjPW7c2lu4PzhZM0fZbKnx';");
//        properties.setProperty("sasl.mechanism","PLAIN");
//        properties.setProperty("client.dns.lookup","use_all_dns_ips");
//        properties.setProperty("session.timeout.ms","45000");
//        properties.setProperty("acks","all");
//        properties.setProperty("schema.registry.url","https://psrc-mw731.us-east-2.aws.confluent.cloud");
//        properties.setProperty("basic.auth.credentials.source","USER_INFO");
//        properties.setProperty("basic.auth.user.info","PLNCGD7KVGUY7C7B:hXFYUj0D7JzvWnFh9MN2nD4s+vM5XkrvIbzjxTxMfxONS+r8DNqNu+B2G/22qEP3");

        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        KafkaProducer<byte[],byte[]> producer = new KafkaProducer<>(properties);

        try {
        	while (active) {
        		if (!queue.isEmpty()) {
        			while (!queue.isEmpty()) {
        				ISO8583Message msg = queue.poll();
        				producer.send(new ProducerRecord<byte[],byte[]>(TOPIC, msg.toBytes()));
        			}
        			producer.flush();
        		}
        		Thread.sleep(100);
        	}
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}
