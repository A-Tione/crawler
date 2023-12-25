package com.github.hcsp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // 待处理的连接池
        List<String> linkPool = new ArrayList<>();
        // 已经处理的连接池
        Set<String> processedLinks = new HashSet<>();

        linkPool.add("https://sina.cn");
        while (true) {
            if (linkPool.isEmpty()) {
                System.out.println("break");
                break;
            }

            // ArrayList从尾部删除更有效率
            String link = linkPool.remove(linkPool.size() -1 );
            if (processedLinks.contains(link)) {
                continue;
            }

            // 我们只关心news. sina的，我们要排除登陆页面
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                // 如果这是一个新闻的详情页，就存入数据库，否则，就什么都不做
                storeIntoDatabaseIfItIsNewsPage(doc);
                processedLinks.add(link);
            } else {
                // 这是我们不感兴趣的，不处理它
            }
        }
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                System.out.println("title");
                System.out.println(title);
            }
        }
    }

    private static boolean isInterestingLink(String link) {
        return ((isNewsPage(link) || isIndexPage(link)) && inNotLoginPage(link));
    }

    private static boolean inNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static Document httpGetAndParseHtml(String link) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        if (link.startsWith("//")) {
            link = "https:" + link;
            System.out.println(link);
        }
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");

        try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            Document doc = Jsoup.parse(html);
            System.out.println("-----------");
            return doc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}