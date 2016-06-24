package ru.wobot.sm.proxy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collection;

@SpringBootApplication
public class Application {
    @Bean
    JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(@Value("${server.port:8888}") final String port,
                                                                              @Value("${jetty.threadPool.maxThreads:10}") final String maxThreads,
                                                                              @Value("${jetty.threadPool.minThreads:5}") final String minThreads,
                                                                              @Value("${jetty.threadPool.idleTimeout:60000}") final String idleTimeout) {
        final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(Integer.valueOf(port));
        factory.addServerCustomizers(new JettyServerCustomizer() {
            @Override
            public void customize(Server server) {
                // Tweak the connection pool used by Jetty to handle incoming HTTP connections
                final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
                threadPool.setMaxThreads(Integer.valueOf(maxThreads));
                threadPool.setMinThreads(Integer.valueOf(minThreads));
                threadPool.setIdleTimeout(Integer.valueOf(idleTimeout));
            }
        });
        return factory;
    }

    @Bean
    RestOperations restOperations() {
        return new RestTemplate();
    }

    @Bean
    HttpComponentsClientHttpRequestFactory getRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory();
    }

    @Bean
    Collection<JsonNode> logins(@Autowired ObjectMapper objectMapper, @Autowired ResourceLoader resourceLoader,
                                @Value("${wobot.cookies.file:cookies.json}") String cookiesFile) {
        Collection<JsonNode> result;
        try {
            result = objectMapper.readValue(resourceLoader.getResource("classpath:" + cookiesFile).getFile(),
                    new TypeReference<Collection<JsonNode>>() {
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't deserialize cookies from file provided in config.", e);
        }
        return result;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }
}