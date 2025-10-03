package tooling.leyden.aotcache;

/**
 * This class represents errors in storing or loading elements to/from the cache.
 */
public class Warning {

	private WarningType type;

	/**
	 * Element that suffered the problem.
	 */
	private Element element;

	/**
	 * Why this error happened. Usually this is just the log message that comes with the error.
	 */
	private String reason;

	public Warning(Element element, String reason, WarningType type) {
		this.element = element;
		this.reason = reason;
		this.type = type;
	}

	public String getIdentifier() {
		return this.element.getKey();
	}

	public WarningType getType() {
		return type;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (this.element != null) {
			sb.append("Element '");
			sb.append(this.element.getKey());
			sb.append("' of type '");
			sb.append(this.element.getType());
			sb.append("' couldn't be ");
			if (this.type == WarningType.StoringIntoAOTCache) {
				sb.append("stored into the AOTcache");
			} else if (this.type == WarningType.LoadingFromAOTCache) {
				sb.append("loaded from the AOTcache");
			} else {
				sb.append("processed");
			}
			sb.append(" because: ");
		} else {
			sb.append("[" + this.type + "]: ");
		}

		sb.append("'");
		sb.append(this.reason);
		sb.append("'");

		return sb.toString();
	}
}
