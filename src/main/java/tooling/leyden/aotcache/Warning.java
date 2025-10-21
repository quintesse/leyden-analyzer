package tooling.leyden.aotcache;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents errors in storing or loading elements to/from the cache.
 */
public class Warning {

	private String id;

	private WarningType type;

	/**
	 * Element that suffered the problem.
	 */
	private Element element;

	/**
	 * String ready to be printed regarding this error.
	 */
	private AttributedString message;

	private static AtomicInteger idGenerator = new AtomicInteger();

	public Warning(Element e, AttributedString message, WarningType type) {
		this.element = e;
		this.type = type;
		this.message = message;
		this.setId(idGenerator.getAndIncrement());
	}

	public Warning(Element element, String description, WarningType type) {
		this.element = element;
		this.type = type;
		this.setId(idGenerator.getAndIncrement());

		AttributedStringBuilder sb = new AttributedStringBuilder();

		if (this.element != null) {
			sb.append("Element '");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.CYAN));
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
		}

		sb.append(description);

		this.message = sb.toAttributedString();
	}

	public String getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = String.format("%03d", id);
	}

	public WarningType getType() {
		return type;
	}

	public AttributedString getDescription() {

		AttributedStringBuilder sb = new AttributedStringBuilder();
		sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.CYAN));
		sb.append(this.getId());
		sb.style(AttributedStyle.DEFAULT);
		sb.append(" [");
		sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.YELLOW));
		sb.append(this.type.name());
		sb.style(AttributedStyle.DEFAULT);
		sb.append("] ");
		sb.append(this.message);
		return sb.toAttributedString();
	}

	public String toString() {
		return message.toString();
	}
}
