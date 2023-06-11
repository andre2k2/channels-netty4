package br.com.elo.rise.autorizations.channels;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ISO8583Encoder extends MessageToByteEncoder<ISO8583Message> {

	@Override
    protected void encode(ChannelHandlerContext ctx, ISO8583Message message, ByteBuf out) throws Exception {
        byte[] encodedData = message.toBytes();
        out.writeBytes(encodedData);
    }
}
