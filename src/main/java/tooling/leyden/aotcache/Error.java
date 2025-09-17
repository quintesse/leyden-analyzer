package tooling.leyden.aotcache;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents errors in storing or loading elements to/from the cache.
 */
public class Error {

	/**
	 * Element that suffered the problem.
	 */
	private Element element;

	/**
	 * Why this error happened. Usually this is just the log message that comes with the error.
	 */
	private String reason;

	/**
	 * Was this error caused on loading or writing?
	 */
	private Boolean load;

	public Error(Element element, String reason, Boolean load) {
		this.element = element;
		this.reason = reason;
		this.load = load;
	}

	public String getIdentifier() {
		return element.getKey();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (element != null) {
			sb.append("Element '");
			sb.append(element.getKey());
			sb.append("' of type '");
			sb.append(this.element.getType());
			sb.append("' couldn't be ");
			sb.append(this.load ? "loaded from" : "stored into");
			sb.append(" the AOTcache because: '");
			sb.append(this.reason);
			sb.append("'");
		} else {
			sb.append("During ");
			sb.append(this.load ? "cache load" : "cache save");
			sb.append(": '");
			sb.append(this.reason);
			sb.append("'");
		}

		return sb.toString();
	}
}
