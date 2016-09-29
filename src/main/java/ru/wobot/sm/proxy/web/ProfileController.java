package ru.wobot.sm.proxy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.wobot.sm.proxy.service.SearchService;
import ru.wobot.sm.proxy.domain.Profile;
import ru.wobot.sm.proxy.service.ProfileService;

@RestController
public class ProfileController {
    private final ProfileService profileService;
    private final SearchService searchService;

    @Autowired
    public ProfileController(ProfileService profileService, SearchService searchService) {
        this.profileService = profileService;
        this.searchService = searchService;
    }

    @RequestMapping("/facebook/{appScopedId}")
    public Profile getFbProfileData(@PathVariable String appScopedId) {
        return profileService.getProfile(appScopedId);
    }


    @RequestMapping("/facebook/search/latest/{query}/{pages}/pages/{maxHeight}/height")
    public String search(@PathVariable String query, @PathVariable Integer pages, @PathVariable Integer maxHeight) {
        return searchService.getLatest(query, pages, maxHeight);
    }
}
