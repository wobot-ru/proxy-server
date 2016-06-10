package ru.wobot.sm.proxy.domain;

public class Profile {
    private final String id;
    private final String appScopedId;
    private final String name;
    private final String rawContent;

    public Profile(String id, String appScopedId, String name, String rawContent) {
        this.id = id;
        this.appScopedId = appScopedId;
        this.name = name;
        this.rawContent = rawContent;
    }

    public String getAppScopedId() {
        return appScopedId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRawContent() {
        return rawContent;
    }
}
