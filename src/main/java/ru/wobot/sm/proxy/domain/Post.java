package ru.wobot.sm.proxy.domain;

public class Post {
    private final String id;
    private final String body;
    private final String digest;
    private final Long engagement;
    private final String fetchTime;
    private final String href;
    private final Boolean isComment;
    private final String parentPostId;
    private final String postDate;
    private final String profileId;
    private final String segment;
    private final String smPostId;
    private final String source;

    public Post(String id, String body, String digest, Long engagement, String fetchTime, String href, Boolean isComment, String parentPostId, String postDate, String profileId, String segment, String smPostId, String source) {
        this.id = id;
        this.body = body;
        this.digest = digest;
        this.engagement = engagement;
        this.fetchTime = fetchTime;
        this.href = href;
        this.isComment = isComment;
        this.parentPostId = parentPostId;
        this.postDate = postDate;
        this.profileId = profileId;
        this.segment = segment;
        this.smPostId = smPostId;
        this.source = source;
    }

    public String getId() {
        return id;
    }
}
