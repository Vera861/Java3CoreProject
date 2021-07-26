import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public class Server {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(auth, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(
                                new ObjectEncoder(),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                new MessageHandler()
                        );
                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(8089).sync();
        log.debug("Server started...");
        channelFuture.channel().closeFuture().sync();
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }
}

