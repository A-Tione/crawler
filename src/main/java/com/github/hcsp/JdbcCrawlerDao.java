package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:/Users/yanghe/project/crawler/news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextLink(String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = this.connection.prepareStatement(sql)){
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("select link from LINKS_TO_BE_PROCESSED LIMIT 1");
        if (link != null) {
            updateDatabase(link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
        }
        return link;
    }

    public void updateDatabase(String link, String sql) throws SQLException {
        try( PreparedStatement statement = this.connection.prepareStatement(sql) ) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("insert into news (url, title, content, created_at,MODIFIED_AT)values(?,?,?,now(),now())")) {
            statement.setString(1, url);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }


    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = this.connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public void insertProcessedLink(String link) {

    }

    @Override
    public void insertLinkToBeProcessed(String link) {

    }

}
