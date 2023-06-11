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