package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator extends Thread {
    public static void main(String[] args) {
        SqlSession session;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MockData(session, 10000);
    }

    private static void MockData(SqlSession session, int maxCount) {
        List<News> newsList = session.selectList("com.github.hcsp.MockMapper.selectNews");
        int index = newsList.size();
        try {
            for (int i = index; i < maxCount; i++) {
                Random random = new Random();
                int randomRow = random.nextInt(index);
                int randomSecond = random.nextInt(3600 * 24 * 365);
                News news = new News(newsList.get(randomRow));

                Instant currentTime = news.getCreatedAt().minusSeconds(randomSecond);
                news.setCreatedAt(currentTime);
                news.setModifiedAt(currentTime);
                session.insert("com.github.hcsp.MockMapper.insertNews", news);
                System.out.println("Left:" + i);
                if (i % 1000 == 0) {
                    session.flushStatements();
                }
            }
            session.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }
}