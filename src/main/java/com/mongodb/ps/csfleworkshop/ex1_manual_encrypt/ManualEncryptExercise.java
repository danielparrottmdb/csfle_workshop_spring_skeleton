package com.mongodb.ps.csfleworkshop.ex1_manual_encrypt;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.support.Repositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.ps.csfleworkshop.CsfleExercise;
import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.models.Employee;
import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.models.EmployeeName;
import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.models.EmployeeAddress;
import com.mongodb.ps.csfleworkshop.ex1_manual_encrypt.repositories.EmployeeRepository1;
import com.mongodb.ps.csfleworkshop.services.KeyGenerationService;

public class ManualEncryptExercise implements CsfleExercise {

    protected ApplicationContext appContext;

	protected static Logger log = LoggerFactory.getLogger(ManualEncryptExercise.class);
    
    public ManualEncryptExercise(ApplicationContext applicationContext) {
        this.appContext = applicationContext;
    }

	public EmployeeRepository1 getEmployeeRepository(ApplicationContext appContext) {
		Repositories repos = new Repositories(appContext);
		EmployeeRepository1 repo = (EmployeeRepository1) repos.getRepositoryFor(Employee.class).get();
		return repo;
	}

	public KeyGenerationService getKeyGenerationService(ApplicationContext applicationContext) {
		return applicationContext.getBean(KeyGenerationService.class);
	}

    public void runExercise() {
        // NB - there is a bug in spring-data-mongodb date conversion @ 4.1 so use LocalDate
        //Date dob = new Date(1989, 1, 1);
        LocalDate dob = LocalDate.of(1980, 10, 10);
		Employee e = new Employee(
				new EmployeeName("Manish", "Engineer"),
				new EmployeeAddress(
						"1 Bson Rd",
						"Mongoville",
						"3999",
						"Victoria",
						"Oz"),
				Arrays.asList("CTO"),
				dob,
				"1800MONGO",
				999999.99,
				"78SD02NN001");

		// Now make sure an encryption key for the employee exists
		// This is the easy way:
		// KeyGenerationService keyGenerationService = this.getKeyGenerationService(appContext);
		// final UUID dataKey1 = keyGenerationService.generateKey();
		// TODO - the hard way :)
		// TODO:SDE - this is ugly
		Environment environment = appContext.getEnvironment();
		String keyVaultDb =  environment.getProperty("spring.data.mongodb.keyvault.database", String.class);
		String keyVaultColl =  environment.getProperty("spring.data.mongodb.keyvault.collection", String.class);
		String keyAltName = "dataKey1";
        MongoClient keyVaultClient = (MongoClient) appContext.getBean(MongoClient.class);
        MongoCollection<Document> keyVaultCollection = null; // TODO keyVaultClient.get ???
        Bson byKeyAltName = Filters.eq("TODO", "TODO"); // TODO!
        com.mongodb.client.FindIterable<Document> keyDocs = keyVaultCollection.find(byKeyAltName);
		Document keyDocument = keyDocs.first();
		Binary bDataKeyId = keyDocument.get("_id", Binary.class);
		final UUID dataKey1 = this.toUUID(bDataKeyId);
		log.info("dataKey1: " + dataKey1);


		// Insert the employee doc
		EmployeeRepository1 employeeRepository = this.getEmployeeRepository(appContext);
		ObjectId eId = employeeRepository.insert(e).getId();
		log.info("eId: " + eId);
		// TODO - use this output as a command in mongosh to check if the data is encrypted
		// You expect to see Binary subtype 6
		log.info("Find: db.getSiblingDB('companyData').getCollection('employee').findOne({_id: '" + eId + "'})");

    }
    public BsonDocument getSchemaDocument(UUID dekUuid) {
        return null;
    }

    public boolean useAutoEncryption() {
        return false;
    }

    public UUID toUUID(Binary b) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(b.getData());
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

}

