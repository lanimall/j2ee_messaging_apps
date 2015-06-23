package com.github.lanimall.messaging.samples.jmsgenerator;

import com.github.lanimall.messaging.samples.jmsgenerator.app.MsgPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@ComponentScan
@ImportResource("classpath:application-context.xml")
//@PropertySource("classpath:application.properties")
public class Application implements CommandLineRunner {
    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Autowired
    MsgPublisher msgPublisher;

    @Autowired
    ResourceLoader resourceLoader;

	static final Logger log = LoggerFactory.getLogger(Application.class);

	@Override
	public void run(String... args) {
        log.info("Initializing App...");

        msgPublisher.publishGoodMessage();

        log.info("Sent message...");
    }

	public static void main(String[] args) {
		log.info("Loading Spring ApplicationContext...");
        SpringApplication app = new SpringApplication(Application.class);
		ApplicationContext ctx = app.run(args);
		log.info("Loaded Spring ApplicationContext.");
	}
}
