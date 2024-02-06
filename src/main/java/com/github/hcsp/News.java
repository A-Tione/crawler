package com.github.hcsp;

import java.time.Instant;

public class News {
    private Integer id;
    private String url;
    private String content;
    private String title;
    private Instant createdAt;
    private Instant modifiedAt;

    public News() {}

    public News(String url, String title, String content) {
        this.url = url;
        this.content = content;
        this.title = title;
    }

    public News(News news) {
        this.id = news.id;
        this.url = news.url;
        this.content = news.content;
        this.title = news.title;
        this.createdAt = news.createdAt;
        this.modifiedAt = news.modifiedAt;
    }

    public News(Integer id, String url, String content, String title, Instant createdAt, Instant modifiedAt) {
        this.id = id;
        this.url = url;
        this.content = content;
        this.title = title;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
