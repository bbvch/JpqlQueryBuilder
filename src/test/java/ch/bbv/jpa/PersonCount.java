package ch.bbv.jpa;

import ch.bbv.jpa.annotations.MappedFrom;
import ch.bbv.jpa.annotations.QueryFrom;

@QueryFrom("Person p")
public class PersonCount {

	@MappedFrom("COUNT(p.id)")
	private Long count;

	protected PersonCount() {
	}

	public long getCount() {
		return count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count.intValue();
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
		if (!(obj instanceof PersonCount)) {
			return false;
		}
		PersonCount other = (PersonCount) obj;
		if (count != other.count) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PersonCount [count=" + count + "]";
	}
}
