package com.example.demo.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class VertexAiConfig {

    @Value("${gcp.project.id}")
    private String projectId;

    @Value("${gcp.project.location}")
    private String location;

    @Bean
    public VertexAI vertexAI() throws IOException {
        return new VertexAI(projectId, location);
    }

    @Bean
    public GenerativeModel generativeModel(VertexAI vertexAI) {
        return new GenerativeModel("gemini-2.5-flash", vertexAI);
    }
}
