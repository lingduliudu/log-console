package tot.log.logconsole.websocket;



import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ServerWebChildInitializer extends ChannelInitializer<SocketChannel> {
	

	@Override
	protected void initChannel(SocketChannel sc) throws Exception {
		ChannelPipeline pipeline = sc.pipeline();
		pipeline.addLast("HttpServerCodec",new HttpServerCodec());
		pipeline.addLast("ChunkedWriteHandler",new ChunkedWriteHandler());
		pipeline.addLast("HttpObjectAggregator",new HttpObjectAggregator(8192));
		pipeline.addLast("WebSocketServerProtocolHandler",new WebSocketServerProtocolHandler("/ws"));
		pipeline.addLast("ServerWebChildHandler",new ServerWebChildHandler());
		
	}
}
