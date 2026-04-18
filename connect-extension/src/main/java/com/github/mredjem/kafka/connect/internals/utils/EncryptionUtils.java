package com.github.mredjem.kafka.connect.internals.utils;

import com.github.mredjem.kafka.connect.EncryptedSecret;
import com.github.mredjem.kafka.connect.internals.exceptions.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public final class EncryptionUtils {

  private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";

  private static final Random RANDOM = new SecureRandom();

  private EncryptionUtils() {}

  public static EncryptedSecret encrypt(String secret, String masterKey) {
    try {
      byte[] salt = generateSalt();
      byte[] iv = generateIv();

      Key aesKey = generateAESKey(masterKey, salt);

      GCMParameterSpec gcmParameterSpec = generateParameterSpec(iv);

      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);

      byte[] encrypted = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));

      return EncryptedSecret.of(encrypted, salt, iv);

    } catch (final Exception e) {
      throw new EncryptionException("Failed to encrypt secret", e);
    }
  }

  public static byte[] decrypt(EncryptedSecret encryptedSecret, String masterKey) {
    try {
      Key aesKey = generateAESKey(masterKey, encryptedSecret.getSalt());

      GCMParameterSpec gcmParameterSpec = generateParameterSpec(encryptedSecret.getIv());

      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmParameterSpec);

      return cipher.doFinal(encryptedSecret.getSecret());

    } catch (final Exception e) {
      throw new EncryptionException("Failed to decrypt secret", e);
    }
  }

  public static String checksum(String secret) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");

      byte[] digested = md.digest(secret.getBytes());

      StringBuilder hexSb = new StringBuilder();

      for (byte b : digested) {
        hexSb.append(String.format("%02x", b));
      }

      return hexSb.toString();

    } catch (final NoSuchAlgorithmException e) {
      throw new EncryptionException("Failed to compute checksum", e);
    }
  }

  private static Key generateAESKey(String masterKey, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    PBEKeySpec keySpec = new PBEKeySpec(masterKey.toCharArray(), salt, 1_000, 128);

    SecretKey pbeKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec);

    return new SecretKeySpec(pbeKey.getEncoded(), "AES");
  }

  private static GCMParameterSpec generateParameterSpec(byte[] iv) {
    return new GCMParameterSpec(128, iv);
  }

  private static byte[] generateSalt() {
    byte[] salt = new byte[100];

    RANDOM.nextBytes(salt);

    return salt;
  }

  private static byte[] generateIv() {
    byte[] iv = new byte[12];

    RANDOM.nextBytes(iv);

    return iv;
  }
}
