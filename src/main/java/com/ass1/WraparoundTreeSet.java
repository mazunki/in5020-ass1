
package com.ass1;

import java.util.Iterator;
import java.util.TreeSet;

class WraparoundTreeSetIterator<T extends Identifiable & Comparable<T>> implements Iterator<T> {
	private T curr, next;
	private Iterator<T> head, tail;
	private Integer max = null;

	public WraparoundTreeSetIterator(WraparoundTreeSet<T> obj, T start) {
		this.tail = obj.tailSet(start, true).iterator(); // include start at beginning
		this.head = obj.headSet(start, false).iterator(); // but not when wrapping around
	}

	public WraparoundTreeSetIterator(WraparoundTreeSet<T> obj, T start, int max) {
		this(obj, start);
		this.max = max;
	}

	public T next() {
		if (this.max != null) {
			if (this.max <= 0) {
				return null;
			}
			this.max--;
		}

		if (this.tail.hasNext()) {
			next = this.tail.next();
		} else if (this.head.hasNext()) {
			next = this.head.next();
		} else {
			next = null;
		}

		this.curr = next;

		return next;
	}

	public boolean hasNext() {
		if (this.max != null && this.max <= 0) {
			return false;
		}
		return this.tail.hasNext() || this.head.hasNext();
	}
}

public class WraparoundTreeSet<T extends Identifiable & Comparable<T>> extends TreeSet<T> {
	public WraparoundTreeSet() {
		super();
	}

	public T getObject(T obj) {
		if (this.contains(obj)) {
			for (T element : this) {
				if (element.equals(obj)) {
					return element;
				}
			}
		}
		return null;
	}

	public void remove(T obj) {
		super.remove(obj);
	}

	public T getObjectById(Identifier id) {
		for (T obj : this) {
			if (obj.getId().equals(id)) {
				return obj;
			}
		}
		return null;
	}

	public Iterator<T> iterator(T start) {
		return new WraparoundTreeSetIterator<T>(this, start);
	}

	public Iterator<T> iterator(T start, int max) {
		return new WraparoundTreeSetIterator<T>(this, start, max);
	}

	public Iterator<T> iteratorFromId(Identifier start) {
		T obj = this.getObjectById(start);
		if (obj == null)
			return null;
		return this.iterator(obj);
	}
}
