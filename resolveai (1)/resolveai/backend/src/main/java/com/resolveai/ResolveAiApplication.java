package com.resolveai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // needed for the SLA escalation job in scheduler/SlaEscalationScheduler
public class ResolveAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResolveAiApplication.class, args);
    }
}
