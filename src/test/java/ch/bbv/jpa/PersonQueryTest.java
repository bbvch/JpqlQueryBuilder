package ch.bbv.jpa;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class PersonQueryTest {

	@ClassRule
	public static JpaUnitTestRule jpaRule = new JpaUnitTestRule();

	private EntityManager entityManager;

	@Before
	public void setup() {
		entityManager = jpaRule.getEntityManager();
		entityManager.getTransaction().begin();

		Address address1 = new Address();
		address1.setStreet("Sunset Boulevard 56");
		address1.setZipCode("93510");
		address1.setCity("Los Angeles");
		Person chuck = new Person();
		chuck.setFirstName("Chuck");
		chuck.setLastName("Norris");
		chuck.addAddress(address1);
		entityManager.persist(chuck);

		Address address2 = new Address();
		address2.setStreet("Sunset Boulevard 56");
		address2.setZipCode("5507");
		address2.setCity("Los Angelese");
		Person dianne = new Person();
		dianne.setFirstName("Dianne");
		dianne.setLastName("Norris");
		dianne.addAddress(address2);
		entityManager.persist(dianne);

		Person ronald = new Person();
		ronald.setFirstName("Ronald");
		ronald.setLastName("Reagan");
		entityManager.persist(ronald);

		entityManager.flush();
	}

	@After
	public void cleanup() {
		entityManager.getTransaction().rollback();
	}

	@Test
	public void personQueryTest() throws Exception {
		TypedQuery<PersonResult> q = QueryBuilder.of(PersonResult.class).build(entityManager);
		List<PersonResult> persons = q.getResultList();

		assertEquals(3, persons.size());
		assertContainsPerson(persons, "Chuck", "Norris");
		assertContainsPerson(persons, "Dianne", "Norris");
		assertContainsPerson(persons, "Ronald", "Reagan");
	}

	@Test
	public void personCountTest() throws Exception {
		TypedQuery<PersonCount> q = QueryBuilder.of(PersonCount.class).build(entityManager);
		PersonCount personCount = q.getSingleResult();

		assertEquals(3, personCount.getCount());
	}

	private void assertContainsPerson(List<PersonResult> persons, String firstName, String lastName) {
		boolean containsPerson = !persons.stream()
				.filter(p -> firstName.equals(p.getFirstName()) && lastName.equals(p.getLastName()))
				.collect(Collectors.toList()).isEmpty();
		assertTrue(format("Expected '%s %s' not in query result list", firstName, lastName), containsPerson);
	}
}
