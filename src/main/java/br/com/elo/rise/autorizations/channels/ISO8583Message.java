package br.com.elo.rise.autorizations.channels;

import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ISO8583Message {

	private byte[] message;

	private ISO8583Message(byte[] message) {
		this.message = message;
	}

	public static ISO8583Message fromBytes(byte[] message) {
		return new ISO8583Message(message);
	}

	public byte[] getMessage() {
		return message;
	}

	public byte[] toBytes() {
		return this.message;
	}

	public String toHex() {
	    return IntStream.range(0, this.message.length)
	    	.mapToObj(i -> String.format("%02X", this.message[i]))
	    	.collect(Collectors.joining());
	}

	public String toBase64() {
		return Base64.getEncoder().encodeToString(this.message);
	}

	@Override
	public String toString() {
		return toHex();
	}
}
