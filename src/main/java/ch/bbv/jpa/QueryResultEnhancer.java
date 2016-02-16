package ch.bbv.jpa;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bbv.jpa.annotations.MappedFrom;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.modifier.MethodArguments;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * This service class enhances query result classes by sub-classing them and
 * adding a constructor that takes values for all properties that are mapped to
 * result class' instances. Mapped properties are recognized by a
 * {@see MappedFrom} annotation on the mapped field.
 * <p>
 * Note: Sub-classed bean classes are cached by this service. The Map used for
 * caching is keyed by the original result class.
 *
 * @param <T>
 *            the type of result objects
 */
class QueryResultEnhancer<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryResultEnhancer.class);

	// Cache for sub-classes of query results
	private static final Map<Class<?>, Class<?>> subClassCache = new ConcurrentHashMap<>();

	// Instance variables hold result bean class and its mapped fields
	private final Class<T> resultClazz;
	private final List<Field> mappedFields;

	/**
	 * Creates enhancer for given query result class.
	 * 
	 * @param resultClazz
	 *            the class of query result bean
	 */
	public QueryResultEnhancer(Class<T> resultClazz) {
		this.resultClazz = resultClazz;
		this.mappedFields = Arrays.asList(resultClazz.getDeclaredFields()).stream()
				.filter(f -> f.getAnnotation(MappedFrom.class) != null).collect(Collectors.toList());
	}

	/**
	 * Getter for list of mapped fields of result class. The order of fields is
	 * the same as the constructor arguments to initialize those fields.
	 * 
	 * @return list of mapped fields
	 */
	public List<Field> getMappedFields() {
		return mappedFields;
	}

	/**
	 * Returns enhanced subclass of query result class, which is computed if not
	 * already present in cache.
	 * 
	 */
	public Class<T> enhance() {
		subClassCache.computeIfAbsent(resultClazz, r -> generateQueryResultClass());
		return (Class<T>) subClassCache.get(resultClazz);
	}

	/**
	 * Generates enhanced subclass of query result class. The enhanced subclass
	 * does have a constructor for initializing all mapped fields.
	 * 
	 * @return the generated subclass
	 */
	private Class<?> generateQueryResultClass() {
		LOGGER.debug("Mapped fields of result: {}", mappedFields);
		Class<?>[] fieldTypes = mappedFields.stream().map(f -> f.getType()).collect(Collectors.toList())
				.toArray(new Class<?>[] {});

		Unloaded<T> unloadedSubClass;
		try {
			unloadedSubClass = new ByteBuddy().with(new NamingStrategy.SuffixingRandom("Query")).subclass(resultClazz)
					.defineConstructor(MethodArguments.VARARGS, Visibility.PUBLIC).withParameters(fieldTypes)
					.intercept(MethodCall.invoke(this.resultClazz.getDeclaredConstructor())
							.andThen(MethodDelegation.to(new ConstructorInitializer(mappedFields))))
					.make();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Generation of subclass for " + resultClazz.getName() + " failed", e);
		}

		return unloadedSubClass
				.load(Thread.currentThread().getContextClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded();
	}

	static class ConstructorInitializer {
		private final List<Field> fields;

		ConstructorInitializer(List<Field> fields) {
			this.fields = fields;
		}

		public void init(@This Object self, @AllArguments Object... args) {
			assert args.length == fields.size();

			IntStream.range(0, args.length).forEach(i -> setFieldValue(self, fields.get(i), args[i]));
		}

		private void setFieldValue(Object self, Field field, Object value) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			try {
				field.set(self, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Initialization of mapped property failed", e);
			}
		}
	}
}
