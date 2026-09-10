package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * PRUEBA DE INTEGRACION: el caso de uso {@link Registry} contra una base de
 * datos H2 real (no un mock). Verifica que la persistencia realmente funciona.
 */
public class RegistryIT {

    private static final String JDBC_URL = "jdbc:h2:mem:regdb_usecase_it;DB_CLOSE_DELAY=-1";

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setup() throws Exception {
        RegistryRepository repository = new RegistryRepository(JDBC_URL);
        repository.initSchema();
        repository.deleteAll();

        repo = repository;
        registry = new Registry(repo);
    }

    @Test
    public void shouldRegisterValidPerson() throws Exception {
        Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);

        RegisterResult result = registry.registerVoter(p1);

        assertEquals(RegisterResult.VALID, result);
        assertTrue(repo.existsById(100));
    }

    @Test
    public void shouldPersistValidVoterAndRejectDuplicates() throws Exception {
        Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);
        Person p2 = new Person("AnaDos", 100, 40, Gender.FEMALE, true);

        RegisterResult result1 = registry.registerVoter(p1);

        assertEquals(RegisterResult.VALID, result1);
        assertTrue(repo.existsById(100));

        RegisterResult result2 = registry.registerVoter(p2);

        assertEquals(RegisterResult.DUPLICATED, result2);
    }

    @Test
    public void shouldRejectUnderagePerson() throws Exception {
        Person p = new Person("Carlos", 101, 17, Gender.MALE, true);

        RegisterResult result = registry.registerVoter(p);

        assertEquals(RegisterResult.UNDERAGE, result);
    }

    @Test
    public void shouldRejectInvalidAge() throws Exception {
        Person p = new Person("Laura", 102, -1, Gender.FEMALE, true);

        RegisterResult result = registry.registerVoter(p);

        assertEquals(RegisterResult.INVALID_AGE, result);
    }

    @Test
    public void shouldRejectDeadPerson() throws Exception {
        Person p = new Person("Pedro", 103, 30, Gender.MALE, false);

        RegisterResult result = registry.registerVoter(p);

        assertEquals(RegisterResult.DEAD, result);
    }

    @Test
    public void shouldRejectInvalidId() throws Exception {
        Person p = new Person("Maria", 0, 30, Gender.FEMALE, true);

        RegisterResult result = registry.registerVoter(p);

        assertEquals(RegisterResult.INVALID, result);
    }
}