package br.com.elo.rise.autorizations.channels;

import java.util.concurrent.TimeUnit;

import br.com.elo.rise.autorizations.pubsub.EventsPublisher;
import br.com.elo.rise.autorizations.pubsub.EventsSubscriber;
import br.com.elo.rise.autorizations.queue.ChannelQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class SocketChannels {

	public static void main(String[] args) throws Exception {

		ChannelQueue input = new ChannelQueue();
		ChannelQueue output = new ChannelQueue();

		EventsPublisher publisher = new EventsPublisher(input);
		EventsSubscriber subscriber = new EventsSubscriber(output);

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
    		.group(bossGroup, workerGroup)
    		.channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS));
                    pipeline.addLast(new ISO8583Decoder());
                    pipeline.addLast(new ISO8583Encoder());
                    pipeline.addLast(new ChannelHandler(input, output));
                }
            });

        publisher.start();
        subscriber.start();

        serverBootstrap.bind(8888)
        	.sync().channel().closeFuture().sync();
    }

}
