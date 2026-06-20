package com.authkit.infrastructure.security;

/**
 * Puerto: QrCodeGenerator
 * Convierte una URI otpauth:// en una imagen QR como data URL (base64 PNG).
 * Equivalente al interface QrCodeGenerator de security.service.ts
 */
public interface QrCodeGeneratorPort {
    String toDataUrl(String text);
}
