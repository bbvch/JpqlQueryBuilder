package ch.bbv.jpa;

import ch.bbv.jpa.annotations.MappedFrom;
import ch.bbv.jpa.annotations.QueryFrom;

@QueryFrom("Person p")
public class PersonResult {

	@MappedFrom("p.id")
	private Integer id;

	@MappedFrom("p.firstName")
	private String firstName;

	@MappedFrom("p.lastName")
	private String lastName;

	protected PersonResult() {
	}

	public int getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PersonResult)) {
			return false;
		}
		PersonResult other = (PersonResult) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PersonResult [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}
}
