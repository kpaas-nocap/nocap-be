package com.example.nocap.global;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
            .region(Region.of(region))
            .credentialsProvider(DefaultCredentialsProvider.create()) // EC2 역할이나 credentials 파일을 자동으로 찾음
            .httpClient(ApacheHttpClient.builder()
                .socketTimeout(Duration.ofMinutes(2)) // 응답을 기다리는 최대 시간
                .connectionTimeout(Duration.ofSeconds(10)) // 연결을 맺는 최대 시간
                .build())
            .build();
    }
}