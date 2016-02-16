package ch.bbv.jpa;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bbv.jpa.annotations.MappedFrom;
import ch.bbv.jpa.annotations.QueryFrom;

/**
 * QueryBuilder creates typed JPA queries for POJO result classes, that are
 * annotated with @QueryFrom. Properties of result class may be annotated
 * with @MappedFrom.
 *
 * @param <T>
 *            the result type
 */
public class QueryBuilder<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);

	private final Class<T> resultClass;
	private final QueryResultEnhancer<T> resultEnhancer;

	/**
	 * Factory method of <code>QueryBuilder</code> for given result class.
	 * 
	 * @param resultClass
	 *            the class of query result
	 * @return the created query builder
	 */
	public static <T> QueryBuilder<T> of(Class<T> resultClass) {
		return new QueryBuilder<>(resultClass);
	}

	private QueryBuilder(Class<T> resultClass) {
		if (resultClass.getDeclaredAnnotation(QueryFrom.class) == null) {
			throw new IllegalArgumentException("Result class type must be annotated with @QueryFom");
		}

		this.resultClass = resultClass;
		this.resultEnhancer = new QueryResultEnhancer<>(resultClass);
	}

	/**
	 * Creates JPQL query object which is typed by result bean type. The
	 * EntityManager for query creation.
	 * 
	 * @param em
	 *            the EntityManager
	 * @return the typed query
	 */
	public TypedQuery<T> build(EntityManager em) {
		StringBuilder queryStringBuilder = new StringBuilder();

		Class<T> enhancedResultClass = resultEnhancer.enhance();
		queryStringBuilder.append("SELECT NEW ").append(enhancedResultClass.getName()).append("(")
				.append(getPropertyNames()).append(") FROM ")
				.append(resultClass.getAnnotation(QueryFrom.class).value());

		TypedQuery<T> query = em.createQuery(queryStringBuilder.toString(), resultClass);

		LOGGER.info("Query: {}", queryStringBuilder);

		return query;
	}

	private String getPropertyNames() {
		List<Field> mappedFields = resultEnhancer.getMappedFields();
		return mappedFields.stream().map(f -> f.getAnnotation(MappedFrom.class).value())
				.collect(Collectors.joining(","));
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuilder strBuilder = new StringBuilder("QueryBuilder [");
		strBuilder.append(" resultClass: ").append(resultClass.getName());
		strBuilder.append(" ]");
		return strBuilder.toString();
	}
}
