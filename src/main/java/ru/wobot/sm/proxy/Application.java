package ru.wobot.sm.proxy;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory
            .getLogger(Application.class);

    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(@Value("${server.port:8888}") final String port,
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

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            logger.info(beanName);
        }
    }
}