package com.facebook.util;

import com.facebook.exception.InvalidTokenException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(Dotenv dotenv) throws Exception {
        String clientId = dotenv.get("GOOGLE_CLIENT_ID", System.getenv("GOOGLE_CLIENT_ID"));

        verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new InvalidTokenException("Invalid token.");
        }

        return idToken.getPayload();
    }
}
