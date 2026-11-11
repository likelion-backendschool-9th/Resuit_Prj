package com.newlecture.resuit.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * GitHub integrations.access_token 암·복호화.
 * 저장 형식: {@code v1:} + Base64(IV || ciphertext+tag). 접두사 없으면 레거시 평문으로 본다.
 */
@Component
public class TokenCipher {
    private static final String PREFIX = "v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final byte[] key;
    private final boolean configured;
    private final SecureRandom random = new SecureRandom();

    public TokenCipher(@Value("${resuit.integration.token-secret:}") String secret) {
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            this.key = null;
            this.configured = false;
        } else {
            try {
                this.key = MessageDigest.getInstance("SHA-256")
                        .digest(secret.getBytes(StandardCharsets.UTF_8));
                this.configured = true;
            } catch (GeneralSecurityException ex) {
                throw new IllegalStateException("Cannot initialize token cipher", ex);
            }
        }
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "암호화할 토큰이 없습니다.");
        }
        requireConfigured();
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "토큰을 암호화하지 못했습니다.", ex);
        }
    }

    public String decrypt(String stored) {
        if (stored == null || stored.isBlank()) {
            return stored;
        }
        if (!stored.startsWith(PREFIX)) {
            return stored;
        }
        requireConfigured();
        try {
            byte[] raw = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            if (raw.length <= IV_LENGTH) {
                throw new IllegalArgumentException("cipher text too short");
            }
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[raw.length - IV_LENGTH];
            System.arraycopy(raw, 0, iv, 0, IV_LENGTH);
            System.arraycopy(raw, IV_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "저장된 GitHub 토큰을 읽지 못했습니다. GitHub로 다시 로그인해 주세요.", ex);
        }
    }

    private void requireConfigured() {
        if (!configured) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "연동 토큰 암호키가 설정되지 않았습니다. INTEGRATION_TOKEN_SECRET을 설정해 주세요.");
        }
    }
}
