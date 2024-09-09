package com.ass1;

public class Identifier { // this could kinda be a public record instead, but i prefer being explicit tbh
	private final String _id;

	public Identifier(String id) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("identifiers mustn't be empty");
		}
		this._id = id;
	}

	public Identifier(int id) {
		this(String.valueOf(id));
	}

	public String getValue() {
		return this._id;
	}

	public String toString() {
		return this._id;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj == null)
			return false;
		else if (obj instanceof Identifier)
			return this._id.equals(((Identifier) obj)._id);
		else if (obj instanceof String)
			return this._id.equals(obj);
		return false;
	}

	public int hashCode() {
		return this._id.hashCode();
	}
}
