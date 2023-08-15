package com.mongodb.ps.csfleworkshop;

import java.util.UUID;

import org.bson.BsonDocument;
import org.springframework.context.ApplicationContext;

public interface CsfleExercise {
    public void runExercise(ApplicationContext appContext);
    public BsonDocument getSchemaDocument(UUID dekUuid);
}
