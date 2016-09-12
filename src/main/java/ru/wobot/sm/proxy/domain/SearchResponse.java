package ru.wobot.sm.proxy.domain;

import java.net.URL;
import java.util.List;

public class SearchResponse {
    private final String data;
    private final List<Post> posts;

    public SearchResponse(String data, List<Post> posts) {
        this.data = data;
        this.posts = posts;
    }


    public String getData() {
        return data;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
