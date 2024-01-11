package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "123456";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:/Users/yanghe/project/crawler/news", USER_NAME, PASSWORD);

        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                storeIntoDatabase(connection, doc, link);
                executedSql(connection, link, "insert into LINKS_ALREADY_PROCESSED (link) values (?)");
            }
        }
    }

    private static String getNextLink(Connection connection, String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
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

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection, "select link from LINKS_TO_BE_PROCESSED LIMIT 1");
        if (link != null) {
            executedSql(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
        }
        return link;
    }


    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            processLink(connection, href);
        }
    }

    private static void processLink(Connection connection, String link) throws SQLException {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        if (!link.toLowerCase().startsWith("javascript")) {
            executedSql(connection, link, "INSERT INTO LINKS_TO_BE_PROCESSED (link) VALUES (?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
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

    private static void executedSql(Connection connection, String link, String sql) throws SQLException {
        try( PreparedStatement statement = connection.prepareStatement(sql) ) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private static void storeIntoDatabase(Connection connection, Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(link);
                System.out.println(title);
                try (PreparedStatement statement = connection.prepareStatement("insert into news (url, title, content, created_at,MODIFIED_AT)values(?,?,?,now(),now())")) {
                    statement.setString(1, link);
                    statement.setString(2, title);
                    statement.setString(3, content);
                    statement.executeUpdate();
                }
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        if(isKLink(link)) {
            RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
            httpGet.setConfig(defaultConfig);
        }
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        try(CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return  isAllLink(link) && isNotValidLink(link);
    }
    private static boolean isNotValidLink(String link) {
        return !link.contains("passport.sina.cn")
                && !link.contains("share.sina.cn")
                && !link.contains("mail.sina.cn")
                && !link.contains("games.sina.cn")
                && !link.contains("photo.sina.cn")
                && !link.contains("health.sina.cn")
                && !link.contains("jiaju.sina.cn")
                && !link.contains("zhibo.sina.cn");
    }

    private static boolean isKLink(String link) {
        return link.contains("k.sina.cn");
    }
    private static boolean isAllLink(String link) {
        return link.contains("sina.cn");
    }
}