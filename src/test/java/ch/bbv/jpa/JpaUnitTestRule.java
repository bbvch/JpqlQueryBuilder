package ch.bbv.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JpaUnitTestRule implements TestRule {

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	protected void setupJpa() {
		entityManagerFactory = Persistence.createEntityManagerFactory("TEST_PU");
		entityManager = entityManagerFactory.createEntityManager();
	}

	protected void cleanupJpa() {
		entityManager.close();
		entityManagerFactory.close();
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				setupJpa();
				base.evaluate();
				cleanupJpa();
			}
		};
	}
}
