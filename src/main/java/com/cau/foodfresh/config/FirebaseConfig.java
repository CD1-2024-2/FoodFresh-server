package com.cau.foodfresh.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize(){
        try{
//            FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");
            InputStream serviceAccount = getClass().getResourceAsStream("/serviceAccountKey.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .setDatabaseUrl("https://<PROJECT_ID>.firebaseio.com")
//                    .setStorageBucket("foodfresh-83b2c.firebasestorage.app")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
        catch(Exception e){
            throw new IllegalStateException("Firebase initialization failed", e);
        }
    }
}