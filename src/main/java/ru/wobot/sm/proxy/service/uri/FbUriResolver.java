package ru.wobot.sm.proxy.service.uri;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import ru.wobot.sm.proxy.service.repository.AccountRepository;
import ru.wobot.sm.proxy.service.repository.ProxyRepository;

import java.net.HttpCookie;
import java.net.URI;
import java.util.Collection;
import java.util.List;

@Component
public class FbUriResolver implements UriResolver {
    private final HttpComponentsClientHttpRequestFactory requestFactory;
    private final RestOperations restOperations;
    private final ProxyRepository proxyRepository;
    private final AccountRepository accountRepository;

    public static final String FACEBOOK_API_VERSION = "2.5";
    public static final String FACEBOOK_API_URI = "https://graph.facebook.com/v" + FACEBOOK_API_VERSION;
    public static final String FACEBOOK_URI = "https://www.facebook.com";

    @Autowired
    public FbUriResolver(HttpComponentsClientHttpRequestFactory requestFactory, RestOperations restOperations,
                         ProxyRepository proxyRepository, AccountRepository accountRepository) {
        this.requestFactory = requestFactory;
        this.restOperations = restOperations;
        ((RestTemplate) this.restOperations).setRequestFactory(requestFactory);
        this.proxyRepository = proxyRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public String resolve(String appScopedId) {
        this.requestFactory.setHttpClient(getHttpClient(proxyRepository.getHost()));

        URI pictureLocation = restOperations.headForHeaders(FACEBOOK_API_URI + "/" + appScopedId + "/picture").getLocation();
        String part = pictureLocation.getPath();
        String[] parts = part.substring(part.lastIndexOf("/") + 1).split("_");
        if (parts.length >= 2) {
            try {
                pictureLocation = restOperations.headForHeaders(FACEBOOK_URI + "/" + parts[0] + "_" + parts[1]).getLocation();
                if (pictureLocation.getPath().contains("photo")) {
                    List<NameValuePair> params = URLEncodedUtils.parse(pictureLocation, "UTF-8");
                    if (params.get(1).getName().equals("set")) {
                        String[] paramValues = params.get(1).getValue().split("\\.");
                        String factUserId = paramValues[paramValues.length - 1];
                        if (!factUserId.equals("499829591"))  // I don't know who is this (his name is Will Chengberg), but his profile has default pictures
                            return factUserId;
                    }
                }
            } catch (Exception e) {
                // Any way, return redirect to app scoped URL
            }
        }

        return resolveAuthorized(appScopedId);
    }

    private String resolveAuthorized(String appScopedId) {
        String proxy = proxyRepository.getHost();
        this.requestFactory.setHttpClient(getHttpClient(proxy));

        Collection<HttpCookie> cookies = accountRepository.getCookies(proxy);
        String cookieString = StringUtils.collectionToDelimitedString(cookies, "; ");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookieString);

        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restOperations.exchange(FACEBOOK_URI + "/app_scoped_user_id/" + appScopedId,
                HttpMethod.HEAD, request, String.class);

        URI location = response.getHeaders().getLocation();
        String factId;
        //boolean screenName = false;
        List<NameValuePair> params = URLEncodedUtils.parse(location, "UTF-8");
        if (!params.isEmpty() && params.get(0).getName().equals("id"))
            factId = params.get(0).getValue();
        else {
            factId = location.getPath().substring(1); // screen name (user name) (w/o leading slash)
            if (factId.matches(".*\\.\\d+$"))
                factId = factId.replace(".", "");
            //screenName = true;
        }
        return factId;
    }

    private HttpClient getHttpClient(String proxy) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        HttpHost proxyHost = new HttpHost(proxy.split(":")[0], Integer.valueOf(proxy.split(":")[1]));
        String credentials = proxyRepository.getCredentials(proxy);
        credentialsProvider.setCredentials(new AuthScope(proxyHost), new UsernamePasswordCredentials(credentials.split(":")[0], credentials.split(":")[1]));
        return HttpClientBuilder.create().
                disableRedirectHandling().
                setProxy(proxyHost).
                setDefaultCredentialsProvider(credentialsProvider).
                setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy()).build();
    }
}
