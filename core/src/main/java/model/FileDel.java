package model;

import java.io.IOException;
import java.nio.file.Path;

public class FileDel extends AbstractCommand{

    private final String name;

    public FileDel(Path path) throws IOException {
        name = path.getFileName().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE;
    }

    public String getName() {
        return name;
    }
}
