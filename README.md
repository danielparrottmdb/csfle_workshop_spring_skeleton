# CSFLE Workshop - Spring Skeleton

Code for a set of exercises showing how to use MongoDB CSFLE with Spring Boot.

## Installation

You will need to install [libmongocrypt](https://www.mongodb.com/docs/manual/core/csfle/reference/libmongocrypt/) to perform encryption on the client and either the [crypt_shared library](https://www.mongodb.com/docs/manual/core/queryable-encryption/reference/shared-library/) or [mongocryptd](https://www.mongodb.com/docs/manual/core/csfle/reference/mongocryptd/) for automatic encryption via schema.

## Running

- Create a file `src/main/resources/mongodb.properties` and add the following entries:
    - MongoDB URI `spring.data.mongodb.uri`
    - database `spring.data.mongodb.database`
    - collection `spring.data.mongodb.collection`
    - path to your mongo_crypt_shared library  (if using) `crypt.shared.lib.path`

Then you can run as a Spring Boot Application or just run the class `JavaSpringBootCSFLEApplication.java`:
```
mvn spring-boot:run
```

To specify the exercise to run:
```
mvn spring-boot:run -Dspring-boot.run.arguments=--csfle.exercise=7
```

### Generating a master key file

A master key is required to wrap the Data Encryption Keys.  If trying this out using a file-based "local" KMS (**never use file-based KMS in production!!!**) you can generate a master key on a *nix system in the shell using:
```
dd if=/dev/urandom of=master-key.txt bs=96 count=1
```

## Exercises

 1. Manual Encryption (Slide 79)
 2. Manual Decryption (Slide 85)
 3. Manual Complete
 4. Manual Encryption / Auto Decryption (Slide 101)
 5. Auto Encryption (Slide 120)
 6. Auto Decryption (Slide 124)
 7. Auto Complete
 8. Use Case 1 - Create (Slide 137)
 9. Use Case 2 - Create (Slide 139)
 10. Use Case Delete (Slide 142)
 11. Use Case Complete
