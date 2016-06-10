package ru.wobot.sm.proxy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.wobot.sm.proxy.domain.Profile;
import ru.wobot.sm.proxy.service.ProfileService;

@RestController
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @RequestMapping("/facebook/{appScopedId}")
    public Profile getFbProfileData(@PathVariable String appScopedId) {
        return profileService.getProfile(appScopedId);
    }
}
