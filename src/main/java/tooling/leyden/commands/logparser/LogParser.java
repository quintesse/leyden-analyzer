package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.commands.LoadFile;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * This class is capable of parsing (certain) Java logs.
 */
public class LogParser implements Consumer<String> {

	private final AOTCache aotCache;

	public LogParser(LoadFile loadFile) {
		this.aotCache = loadFile.getParent().getAotCache();
	}
// TODO
	// [1055.769s][warning][aot] Preload Warning: Verification failed for org.infinispan.remoting.transport.jgroups.JGroupsRaftManager
	//[1055.813s][warning][aot] Preload Warning: Verification failed for org.apache.logging.log4j.core.async.AsyncLoggerContext


	@Override
	public void accept(String content) {
		// -Xlog:class+load,aot:file=aot.log:tags
		// Logs in Java have the following form:
		// [tag1, tag2,...] Log message

		if (content.indexOf("[") >=0 && content.indexOf("]") > 0) {
			String[] tags = content.substring(content.indexOf("[") + 1, content.indexOf("]")).split(",");
			Arrays.stream(tags).map(String::trim).toArray(unused -> tags);
			final String message = content.substring(content.indexOf("]") + 1);
			// [class,load] java.lang.invoke.DelegatingMethodHandle$Holder source: shared objects file
			final var thisSource = "Java Log";
			if (containsTags(tags, "class", "load")) {
				Integer indexSourceShared = message.indexOf("source: shared objects file");
				if (indexSourceShared > 0) {
					String className = message.substring(0, indexSourceShared).trim();
					if (aotCache.getObjects(className, "Class").isEmpty()) {
						ClassObject classObject = new ClassObject();
						classObject.setName(className.substring(className.lastIndexOf(".") + 1));
						classObject.setPackageName(className.substring(0, className.lastIndexOf(".")));
						aotCache.addElement(classObject, thisSource);
					}
				}
				// else this class wasn't loaded from the aot.map
				// are we interested in storing this?
		 		// we are not adding anything that aot.map doesn't have
			} else if (containsTags(tags, "aot")) {
				//[1055.926s][warning][aot] Skipping org/apache/logging/log4j/core/async/AsyncLoggerContext: Failed verification
				//[1055.928s][warning][aot] Skipping org/apache/logging/slf4j/Log4jLoggerFactory$$Lambda+0x800000258: nest_host class org/apache/logging/slf4j/Log4jLoggerFactory is excluded
				//[1055.928s][warning][aot] Skipping jdk/proxy1/$Proxy29: Unsupported location
				//[1055.929s][warning][aot] Skipping org/slf4j/ILoggerFactory: Old class has been linked
				//[1055.929s][warning][aot] Skipping jdk/internal/event/SecurityProviderServiceEvent: JFR event class
				//[1055.929s][warning][aot] Skipping com/thoughtworks/xstream/security/ForbiddenClassException: Unlinked class not supported by AOTConfiguration
				if (message.trim().startsWith("Skipping ")) {
					String msg = message.substring(9);
					String className = msg.substring(0, msg.indexOf(":")).replace("/", ".");
					String reason = msg.substring(msg.indexOf(":") + 1).trim();
					if (className.contains("$$")) {
						MethodObject method = new MethodObject();
						method.setName(className.trim());
						method.addSource(thisSource);
						aotCache.addError(method, reason, false);
					} else {
						ClassObject classObject = new ClassObject();
						classObject.setName(className.substring(className.lastIndexOf(".") + 1).trim());
						classObject.setPackageName(className.substring(0, className.lastIndexOf(".")).trim());
						classObject.addSource(thisSource);
						aotCache.addError(classObject, reason, false);
					}
				}
			}
		}
	}

	private boolean containsTags(String[] tags, String... wantedTags) {
		return Arrays.asList(wantedTags).containsAll(Arrays.asList(tags));
	}
}
