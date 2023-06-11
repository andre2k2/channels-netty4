package br.com.elo.rise.autorizations.queue;

import java.util.concurrent.ConcurrentLinkedQueue;

import br.com.elo.rise.autorizations.channels.ISO8583Message;

public class ChannelQueue extends ConcurrentLinkedQueue<ISO8583Message> {

	private static final long serialVersionUID = 1L;

	public ChannelQueue() {
	}

	@Override
	public boolean offer(ISO8583Message element) {
		boolean added = super.offer(element);
		return added;
	}

	@Override
	public ISO8583Message poll() {
		ISO8583Message elem = super.poll();
		return elem;
	}

}
