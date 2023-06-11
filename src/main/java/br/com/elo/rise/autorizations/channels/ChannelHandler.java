package br.com.elo.rise.autorizations.channels;

import java.util.Optional;

import br.com.elo.rise.autorizations.queue.ChannelQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelHandler extends ChannelInboundHandlerAdapter {

	private static final ISO8583Message RESPONSE_CODE = ISO8583Message.fromBytes(new byte[] { 0,0 });

	private ChannelQueue input = new ChannelQueue();
	private ChannelQueue output = new ChannelQueue();

	public ChannelHandler(ChannelQueue input, ChannelQueue output) {
		super();
		this.input = input;
		this.output = output;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("Cliente conectado: " + ctx.channel().id());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		readInput(ctx, (ISO8583Message) msg);
		writeOutput(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		System.out.println("[DEBUG] Iniciando escrita por inatividade...");
		writeOutput(ctx);
	}

	private void readInput(ChannelHandlerContext ctx, ISO8583Message msg) {
		try {
			this.input.offer(msg);
			ctx.writeAndFlush(RESPONSE_CODE);
			System.out.println("[DEBUG] Leitura: " + msg.toString());
		} catch (Exception ex) {
			// TODO tratar exception
			System.out.println("ERRO NA LEITURA: " + ex.getMessage());
		}
	}

	private void writeOutput(ChannelHandlerContext ctx) {
		ISO8583Message message = null;
		do {
			try {
				Optional.ofNullable(message = this.output.poll()).ifPresent(msg -> {
					ctx.writeAndFlush(msg);
					System.out.println("[DEBUG] Escrita: " + msg.toString());
				});
			} catch (Exception ex) {
				// TODO tratar exception
				System.out.println("ERRO NA ESCRITA: " + ex.getMessage());
			}
		} while (message != null);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("Cliente desconectado: " + ctx.channel().remoteAddress());
	}
}