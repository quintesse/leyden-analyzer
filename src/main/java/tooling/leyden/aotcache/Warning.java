package tooling.leyden.aotcache;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

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
	 * String ready to be printed regarding this error.
	 */
	private AttributedString message;

	public Warning(Element e, AttributedString message, WarningType type) {
		this.element = e;
		this.type = type;
		this.message = message;
	}

	public Warning(Element element, String description, WarningType type) {
		this.element = element;
		this.type = type;

		AttributedStringBuilder sb = new AttributedStringBuilder();

		if (this.element != null) {
			sb.append("Element '");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.BLUE));
			sb.append(this.element.getKey());
			sb.style(AttributedStyle.DEFAULT);
			sb.append("' of type '");
			sb.style(AttributedStyle::bold);
			sb.append(this.element.getType());
			sb.style(AttributedStyle::boldOff);
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
			sb.append("[");
			sb.style(AttributedStyle.DEFAULT.italic().foreground(AttributedStyle.BLUE));
			sb.append(this.type.name());
			sb.style(AttributedStyle.DEFAULT);
			sb.append("]: ");
		}

		sb.append("'");
		sb.append(description);
		sb.append("'");

		this.message = sb.toAttributedString();
	}

	public String getIdentifier() {
		return this.element.getKey();
	}

	public WarningType getType() {
		return type;
	}

	public AttributedString getDescription() {
		return this.message;
	}

	public String toString() {
		return message.toString();
	}
}
