package com.aiplatform.inspection.service;

import com.aiplatform.inspection.domain.AuthSession;
import com.aiplatform.inspection.domain.UserAccount;
import com.aiplatform.inspection.repository.InspectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {
    private static final long EXPIRES_IN_SECONDS = 8 * 60 * 60;
    private final InspectionRepository repository;
    private final String jwtSecret;

    public AuthService(InspectionRepository repository, @Value("${platform.auth.jwt-secret:local-demo-secret-change-me}") String jwtSecret) {
        this.repository = repository;
        this.jwtSecret = jwtSecret;
    }

    public AuthSession login(String username, String password) {
        String normalizedUsername = normalize(username);
        String normalizedPassword = normalize(password);
        if (!"demo123".equals(normalizedPassword)) {
            repository.appendAuditLog(normalizedUsername, "LOGIN", "user", normalizedUsername, "failed");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        UserAccount user = repository.userByUsername(normalizedUsername)
            .filter(account -> "active".equals(account.status()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        repository.appendAuditLog(user.username(), "LOGIN", "user", user.id(), "success");
        return new AuthSession(createToken(user), user, EXPIRES_IN_SECONDS);
    }

    public UserAccount me(String authorization) {
        String username = readUsername(authorization)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token"));
        return repository.userByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String createToken(UserAccount user) {
        long expiresAt = Instant.now().plusSeconds(EXPIRES_IN_SECONDS).getEpochSecond();
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"sub\":\"" + user.username() + "\",\"uid\":\"" + user.id() + "\",\"exp\":" + expiresAt + "}");
        String signature = hmac(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    private Optional<String> readUsername(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }
        try {
            String[] parts = authorization.substring(7).split("\\.");
            if (parts.length != 3 || !hmac(parts[0] + "." + parts[1]).equals(parts[2])) {
                return Optional.empty();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String username = readJsonString(payload, "sub").orElse("");
            long exp = Long.parseLong(readJsonNumber(payload, "exp").orElse("0"));
            if (username.isBlank() || Instant.now().getEpochSecond() >= exp) {
                return Optional.empty();
            }
            return Optional.of(username);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token", exception);
        }
    }

    private Optional<String> readJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return Optional.empty();
        }
        int valueStart = start + marker.length();
        int valueEnd = json.indexOf('"', valueStart);
        return valueEnd < 0 ? Optional.empty() : Optional.of(json.substring(valueStart, valueEnd));
    }

    private Optional<String> readJsonNumber(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return Optional.empty();
        }
        int valueStart = start + marker.length();
        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }
        return valueEnd == valueStart ? Optional.empty() : Optional.of(json.substring(valueStart, valueEnd));
    }
}
