package tooling.leyden.aotcache;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.HashSet;
import java.util.Set;


/**
 * This element represents a class inside the AOT Cache.
 */
public class ClassObject extends ReferencingElement {

	private String name;
	private String packageName;
	private Set<MethodObject> methods = new HashSet<>();
	private String arrayPrefix = "";
	private Element klassTrainingData;

	public ClassObject(String identifier) {
		this.setName(identifier.substring(identifier.lastIndexOf(".") + 1));
		if (identifier.indexOf(".") > 0) {
			this.setPackageName(identifier.substring(0, identifier.lastIndexOf(".")));
		}
	}

	public String getType() {
		return "Class";
	}
	public String getKey() {
		return getPackageName() + "." + getName();
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return arrayPrefix + packageName;
	}

	public Set<MethodObject> getMethods() {
		return methods;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Element getKlassTrainingData() {
		return klassTrainingData;
	}

	public void setKlassTrainingData(Element klassTrainingData) {
		this.klassTrainingData = klassTrainingData;
	}

	public void setPackageName(String packageName) {
		while (packageName.startsWith("[")) {
			if (packageName.startsWith("[L")) {
				arrayPrefix += "[L";
				packageName = packageName.substring(2);
			} else {
				arrayPrefix += "[";
				packageName = packageName.substring(1);
			}
		}
		this.packageName = packageName;
	}

	public void addMethod(MethodObject method) {
		this.methods.add(method);
		method.setClassObject(this);
	}

	public Boolean isArray() {
		return !this.arrayPrefix.isBlank();
	}

	@Override
	public AttributedString getDescription(String leftPadding) {
		AttributedStringBuilder sb = new AttributedStringBuilder();
		sb.append(super.getDescription(leftPadding));
		if (!this.getMethods().isEmpty()) {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "This class has ");
			sb.style(AttributedStyle.DEFAULT.bold());
			sb.append(Integer.toString(this.getMethods().size()));
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" methods, of which");

			int trained = 0;
			for (MethodObject method : this.getMethods()) {
				if (method.getMethodCounters() != null) {
					trained++;
				}
			}
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
			sb.append(" " + trained);
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" have been trained.");
		}

		if (this.klassTrainingData != null) {
			sb.append(AttributedString.NEWLINE);
			sb.append(leftPadding + "It has a ");
			sb.style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN));
			sb.append("KlassTrainingData");
			sb.style(AttributedStyle.DEFAULT);
			sb.append(" associated to it: ");
			sb.style(AttributedStyle.DEFAULT.bold());
			sb.append(this.klassTrainingData.getAddress());
			sb.style(AttributedStyle.DEFAULT);
		}

		return sb.toAttributedString();
	}

}
