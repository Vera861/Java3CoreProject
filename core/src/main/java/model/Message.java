package model;

import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@ToString
@Getter

public class  Message extends AbstractCommand{

    private final String name;
    private final long size;
    private final byte[] data;

    public Message(Path path) throws IOException {
        name = path.getFileName().toString();
        size = Files.size(path);
        data = Files.readAllBytes(path);
    }

    @Override
    public CommandType getType(){
        return CommandType.MESSAGE;
    }
}
