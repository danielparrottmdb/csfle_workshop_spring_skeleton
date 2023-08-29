package com.mongodb.ps.csfleworkshop.services;

import java.util.Map;
import java.util.UUID;

public interface KeyManagerService {

    UUID generateKey(Map<String, Map<String, Object>> kmsProviders, String kmsProvider);

    UUID generateKey(Map<String, Map<String, Object>> kmsProviders, String kmsProvider, String keyAltName);

    void deleteKey(String keyAltName);

}