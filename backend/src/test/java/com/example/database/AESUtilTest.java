package com.example.database;

import org.junit.jupiter.api.Test;

import com.example.database.security.AESUtil;

import static org.junit.jupiter.api.Assertions.*;

class AESUtilTest {

    @Test
    void testGenerateKey() throws Exception {
        String key = AESUtil.generateKey();
        assertNotNull(key);
        assertFalse(key.isEmpty());
    }

    @Test
    void testEncryptAndDecrypt() throws Exception {
        String originalText = "SensitiveData123!";
        String key = AESUtil.generateKey();

        String encrypted = AESUtil.encrypt(originalText, key);
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);

        String decrypted = AESUtil.decrypt(encrypted, key);
        assertEquals(originalText, decrypted);
    }

    @Test
    void testEncryptWithInvalidKeyLength() {
        String invalidKey = "too-short-key";
        Exception exception = assertThrows(Exception.class, () -> {
            AESUtil.encrypt("Test", invalidKey);
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testDecryptWithWrongKey() throws Exception {
        String text = "TopSecret";
        String correctKey = AESUtil.generateKey();
        String wrongKey = AESUtil.generateKey();

        String encrypted = AESUtil.encrypt(text, correctKey);

        assertThrows(Exception.class, () -> {
            AESUtil.decrypt(encrypted, wrongKey);
        });
    }
}
