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
