package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.commands.LoadFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LogParser implements Consumer<String> {

	private final LoadFile loadFile;
	private final AOTCache aotCache;

	public LogParser(LoadFile loadFile) {
		this.loadFile = loadFile;
		this.aotCache = loadFile.getParent().getAotCache();
	}

	@Override
	public void accept(String content) {
		// -Xlog:class+load,aot:file=aot.log:tags
		// Logs in Java have the following form:
		// [tag1, tag2,...] Log message

		if (content.indexOf("[") >=0 && content.indexOf("]") > 0) {
			String[] tags = content.substring(content.indexOf("[") + 1, content.indexOf("]")).split(",");
			String message = content.substring(content.indexOf("]") + 1);

			// [class,load] java.lang.invoke.DelegatingMethodHandle$Holder source: shared objects file
			if (containsTags(tags, "class", "load")) {
				Integer indexSourceShared = message.indexOf("source: shared objects file");
				if (indexSourceShared > 0) {
					String className = message.substring(0, indexSourceShared).trim();
					if (aotCache.getObject(className) == null) {
						ClassObject classObject = new ClassObject();
						classObject.setName(className.substring(className.lastIndexOf(".") + 1));
						classObject.setPackageName(className.substring(0, className.lastIndexOf(".")));
						aotCache.addElement(classObject);
					}
				} // else this class wasn't loaded from the AOTCache
				  // are we interested in storing this?
			}
		}
	}

	private boolean containsTags(String[] tags, String... wantedTags) {
		return Arrays.asList(wantedTags).containsAll(Arrays.asList(tags));
	}
}
