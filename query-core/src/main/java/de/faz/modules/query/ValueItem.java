package de.faz.modules.query;

/** @author Andreas Kaubisch <a.kaubisch@faz.de> */
public abstract class ValueItem {
	abstract CharSequence toCharSequence();

	@Override
	public String toString() {
		return toCharSequence().toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ValueItem) {
			return toCharSequence().equals(((ValueItem) obj).toCharSequence());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + toCharSequence().hashCode();
		return result;
	}
}