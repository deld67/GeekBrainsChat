package ru.geekbrains.java2.server.auth;

import java.sql.*;

public class PostgreSQLAuthService implements AuthService  {
    private Connection conn;
    private static final String pg_server = "localhost";
    private static final String pg_port = "5432";
    private static final String pg_base_name = "GeekBrainsChat";
    private static final String pg_admin = "gb_chat_admin";
    private static final String pg_pass = "123";

    private static class UserData {
        private String login;
        private String password;
        private String username;

        public UserData(String login, String password, String username) {
            this.login = login;
            this.password = password;
            this.username = username;
        }
    }
    @Override
    public void updateUsername(String newUsername, String oldUsername){
        try {
            Statement statement = conn.createStatement();
            boolean resultSet = statement.execute("update gb_chat_users set username = '"+newUsername+"' where username = '"+oldUsername+"'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select username  from gb_chat_users \n"+
                    "where login = '"+login+"' and password ='"+password+"'");
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void start() {
        try {
            this.conn = DriverManager.getConnection("jdbc:postgresql://"+pg_server+":"+pg_port+"/"+pg_base_name, pg_admin, pg_pass);
        }catch (SQLException e){
            System.out.println("Connection to database failure");
            e.printStackTrace();
        }
        if (this.conn != null){
            try {
                Statement stmt = conn.createStatement();
                ResultSet resultSet = stmt.executeQuery("select count(1)  from gb_chat_users");
                resultSet.next();
                int cnt_rec = resultSet.getInt(1);
                System.out.printf("В базе даннных обнаружено %d  пользователей%n",cnt_rec);
                if (cnt_rec == 0) addUsers();
            }catch (SQLException e){
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.executeQuery("CREATE TABLE public.gb_chat_users(\n" +
                                "     ID  SERIAL PRIMARY KEY,\n" +
                                "    login character varying(20) NOT NULL,\n" +
                                "    password character varying(20) NOT NULL,\n" +
                                "    username character varying(20) NOT NULL\n" +
                                ");\n\n" +
                                "ALTER TABLE public.gb_chat_users    OWNER to postgres;");
                    }catch (SQLException l){
                        try {
                            addUsers();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
            }
        }
    }

    private void addUsers() throws SQLException {
        conn.setAutoCommit(false);
        PreparedStatement preparedStatement = conn.prepareStatement("insert into public.gb_chat_users \n"+
                "(login, password, username) values (?, ?, ?);"
        );
        for (int i = 1; i < 4; i++) {
            preparedStatement.setString(1, "login" + i );
            preparedStatement.setString(2, "pass" + i );
            preparedStatement.setString(3, "username" + i );
            preparedStatement.addBatch();
        }
        //int result = preparedStatement.executeUpdate();
        int[] result = preparedStatement.executeBatch();

        conn.commit();
        System.out.printf("В базу данных добавлено %d пользователей. %n", result.length);
    }

    @Override
    public void stop() {
        try {
            this.conn.close();
            System.out.println("Connection to database is closed");
        }catch (SQLException e){
            System.out.println("Connection to database is closed");
        }

    }
}
