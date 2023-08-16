package com.mongodb.ps.csfleworkshop.services;

import java.util.Map;
import java.util.UUID;

public interface KeyGenerationService {

    Map<String, Map<String, Object>> getKmsProviders();

    UUID generateKey();

    UUID generateKey(String keyAltName);

    public void deleteKey(String keyAltName);
}
