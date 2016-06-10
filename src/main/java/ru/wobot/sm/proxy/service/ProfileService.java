package ru.wobot.sm.proxy.service;

import ru.wobot.sm.proxy.domain.Profile;

public interface ProfileService {
    Profile getProfile(String appScopedId);
}
