package br.com.elo.rise.autorizations.channels;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ISO8583Decoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		if (in.readableBytes() < 2) {
			// Aguarda até ter pelo menos 2 bytes para ler o tamanho da mensagem
			return;
		}

		int messageLength = in.readShort();
		if (in.readableBytes() < messageLength) {
			// Aguarda até ter todos os bytes da mensagem disponíveis
			in.resetReaderIndex();
			return;
		}

		byte[] messageData = new byte[messageLength];
		in.readBytes(messageData);

		ISO8583Message isoMessage = ISO8583Message.fromBytes(messageData);
		out.add(isoMessage);
	}
}
