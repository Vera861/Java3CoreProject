import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private Path currentPath;

    public MessageHandler() throws IOException {
        currentPath = Paths.get("serverFiles");
        if (!Files.exists(currentPath)) {
            Files.createDirectories(currentPath);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext chc) throws Exception {
        chc.writeAndFlush(new ListResponse(currentPath));
        chc.writeAndFlush(new PathUpResponse(currentPath.toString()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext chc, AbstractCommand command) throws Exception {
        log.debug("received: {}", command.getType());
        switch (command.getType()) {
            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) command;
                Message msg = new Message(currentPath.resolve((fileRequest.getName())));
                chc.writeAndFlush((msg));
                break;

            case MESSAGE:
                Message message = (Message) command;
                Files.write(currentPath.resolve(message.getName()), message.getData());
                chc.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_UP:
                if (currentPath.getParent() != null) {
                    currentPath = currentPath.getParent();
                }
                chc.writeAndFlush(new PathUpResponse(currentPath.toString()));
                chc.writeAndFlush(new ListResponse(currentPath));
                break;
            case LIST_REQUEST:
                chc.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN:
                PathInRequest request = (PathInRequest) command;
                Path newPath = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPath)) {
                    chc.writeAndFlush(new PathUpResponse(currentPath.toString()));
                    chc.writeAndFlush(new ListResponse(currentPath));
                }
                break;
        }
    }
}


