package model;

public class AuthUser extends AbstractCommand{
    @Override
    public CommandType getType() {
        return CommandType.AUTOK;
    }
}
