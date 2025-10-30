package com.example.demo.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcpConfig {
	@Bean
    public Storage storage() {
        // 이 코드는 GCP SDK가 application-default-credentials를 사용하도록 합니다.
        // (gcloud auth application-default login 명령어로 인증 필요)
        // 또는 서비스 계정(JSON)을 사용하도록 설정할 수도 있습니다.
        return StorageOptions.getDefaultInstance().getService();
    }
}
