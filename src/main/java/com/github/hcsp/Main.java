package com.github.hcsp;

public class Main {
    public static void main(String[] args) {
        CrawlerDao dao = new MyBatisCrawlerDao();
        for (int i = 0; i < 5; i++) {
            new Crawler(dao).start();
        }
    }
}