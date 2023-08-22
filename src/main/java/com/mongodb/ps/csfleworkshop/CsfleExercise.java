package com.mongodb.ps.csfleworkshop;

import java.util.UUID;

import org.bson.BsonDocument;

public interface CsfleExercise {
    //public void runExercise(ApplicationContext appContext);
    public void runExercise();
    public BsonDocument getSchemaDocument(UUID dekUuid);
    public boolean useAutoEncryption();
}
