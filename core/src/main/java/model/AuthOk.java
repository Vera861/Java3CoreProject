package model;

import java.sql.*;

public class AuthOk extends AbstractCommand {

    public String login;
    public String pass;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AuthOk(String log, String pass) throws SQLException {
        login = log;
        this.pass = pass;

    }

    private boolean isAuth() {
        try (
                Connection connection = DriverManager
                        .getConnection("jdbc:postgresql://Localhost:5432/auth_users", "Evka", "123");) {
            PreparedStatement ps = connection.prepareStatement("select * from auth_user where login_us =? and pass_us=?");
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } return false;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public CommandType getType() {
        if (isAuth()) return CommandType.AUTHOK;
else return CommandType.AUTHERR;
    }
}
