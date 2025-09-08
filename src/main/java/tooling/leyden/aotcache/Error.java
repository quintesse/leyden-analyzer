package tooling.leyden.aotcache;

import java.util.LinkedList;
import java.util.List;

public class Error {

	private Element element;

	private String reason;

	public Error(Element element, String reason) {
		this.element = element;
		this.reason = reason;
	}

	public String getIdentifier() {
		return element.getKey();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Element '");
		sb.append(element.getKey());
		sb.append("' of type '");
		sb.append(this.element.getType());
		sb.append("' couldn't be added to the cache because: '");
		sb.append(this.reason);
		sb.append("'");

		return sb.toString();
	}
}
