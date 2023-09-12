package com.mongodb.ps.csfleworkshop.ex4_manual_complete;

import java.time.LocalDate;
import java.util.Arrays;
// import java.util.Date;
import java.util.UUID;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.EncryptionAlgorithms;
import org.springframework.data.repository.support.Repositories;

import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.Employee;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeNameM;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeM;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories.EmployeeRepository4;
import com.mongodb.ps.csfleworkshop.ex4_manual_complete.repositories.EmployeeRepository4M;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class ManualEncryptAutoDecryptExercise implements CsfleExercise {

    protected ApplicationContext appContext;
    protected ClientEncryption clientEncryption;

    protected static Logger log = LoggerFactory.getLogger(ManualEncryptAutoDecryptExercise.class);
    
    public ManualEncryptAutoDecryptExercise(ApplicationContext applicationContext, ClientEncryption clientEncryption) {
        this.appContext = applicationContext;
        this.clientEncryption = clientEncryption;
    }

    public EmployeeRepository4 getEmployeeRepository(ApplicationContext appContext) {
        Repositories repos = new Repositories(appContext);
        EmployeeRepository4 repo = (EmployeeRepository4) repos.getRepositoryFor(Employee.class).get();
        return repo;
    }

    public EmployeeRepository4M getEmployeeMRepository(ApplicationContext appContext) {
        Repositories repos = new Repositories(appContext);
        EmployeeRepository4M repo = (EmployeeRepository4M) repos.getRepositoryFor(EmployeeM.class).get();
        return repo;
    }
    public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
        return applicationContext.getBean(KeyGenerationService.class);
    }

    public void runExercise() {

        BsonDocument dataKey1Doc = clientEncryption.getKeyByAltName("dataKey1");
        UUID dataKey1 = dataKey1Doc.getBinary("_id").asUuid();
        EncryptOptions options = new EncryptOptions(EncryptionAlgorithms.AEAD_AES_256_CBC_HMAC_SHA_512_Deterministic).keyId(new BsonBinary(dataKey1));
        // NB - there is a bug in spring-data-mongodb date conversion @ 4.1 so use LocalDate
        //Date dob = new Date(1989, 1, 1);
        LocalDate dob = LocalDate.of(1989, 1, 1);
        BsonBinary encrFirstName = clientEncryption.encrypt(new BsonString("Manish"), options);
        BsonBinary encrLastName = clientEncryption.encrypt(new BsonString("Engineer"), options);
        EmployeeM e = new EmployeeM(
                new EmployeeNameM(encrFirstName, encrLastName),
                new EmployeeAddress(
                        "537 Bson Rd",
                        "Mongoville",
                        "7258",
                        "Tasmania",
                        "Oz"),
                Arrays.asList("IC"),
                //ZonedDateTime.of(LocalDate.of(1989, 1, 1), LocalTime.of(0, 0, 0)),
                //ZonedDate.of(1989, 1, 1),
                dob,
                "1800MONGO",
                89000.0,
                "103-443-924");

        // Now make sure an encryption key for the employee exists

        // Insert the employee doc using manual encryption
        EmployeeRepository4M employeeRepositoryMan = this.getEmployeeMRepository(appContext);
        ObjectId eId = employeeRepositoryMan.insert(e).getId();
        log.info("eId: " + eId);

        // Find using the deterministically encrypted first and last names
        EmployeeRepository4 employeeRepositoryAuto = this.getEmployeeRepository(appContext);
        // EmployeeName nameQuery = new EmployeeName("Manish", "Engineer");
        // EmployeeX e2 = employeeRepositoryX.findByName(nameQuery).get(0);
        Employee eAuto = employeeRepositoryAuto.findById(eId.toString()).get();
        log.info("eAuto: " + eAuto);

    }
    public BsonDocument getSchemaDocument(UUID dekUuid) {
        return null;
    }

    public boolean useAutoEncryption() {
        return true;
    }
}

