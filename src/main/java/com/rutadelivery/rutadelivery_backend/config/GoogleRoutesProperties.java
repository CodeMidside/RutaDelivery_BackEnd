package com.rutadelivery.rutadelivery_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.maps.routes")
public class GoogleRoutesProperties {

    private String apiKey;
    private String url;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean tieneApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}