package tooling.leyden.commands.logparser;

import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.commands.LoadFile;

import java.util.function.Consumer;

public class AOTMapParser implements Consumer<String> {

	private final LoadFile loadFile;
	private final AOTCache aotCache;

	public AOTMapParser(LoadFile loadFile) {
		this.loadFile = loadFile;
		this.aotCache = loadFile.getParent().getAotCache();
	}

	@Override
	public void accept(String content) {
		// 0x0000000800001d80: @@ TypeArrayU1       600
		// 0x000000080082d490: @@ Class             760 java.lang.StackFrameInfo
		// 0x000000080082ac80: @@ Method            88 char example.Class.example(long)
		if (content.indexOf(": @@") == 18) {
			String address = content.substring(0, content.indexOf(":"));
			final var typeStart = content.indexOf("@@") + 2;
			try {
				String type = content.substring(typeStart, typeStart + 18).trim();
				final var identifier = content.substring(typeStart + 22).trim();
				if (type.equalsIgnoreCase("Class")) {
					ClassObject classObject = (ClassObject) this.aotCache.getObject(identifier);
					if(classObject != null) {
						// It could have been already loaded by some log file
						classObject = new ClassObject();
						classObject.setName(identifier.substring(identifier.lastIndexOf(".") + 1));
						classObject.setPackageName(identifier.substring(0, identifier.lastIndexOf(".")));
						this.aotCache.addElement(classObject);
					}
					classObject.setAddress(address);
				} else if (type.equalsIgnoreCase("Method")) {
					MethodObject methodObject = new MethodObject();
					String qualifiedName = identifier.substring(identifier.indexOf(" ") + 1, identifier.indexOf("("));
					methodObject.setName(qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1));
					methodObject.setReturnType(identifier.substring(0, identifier.indexOf(" ")));
					String className = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
					Element object = this.aotCache.getObject(className);
					if (object == null) {
						ClassObject classObject = new ClassObject();
						classObject.setName(className.substring(className.lastIndexOf(".") + 1));
						classObject.setPackageName(className.substring(0, className.lastIndexOf(".")));
						this.aotCache.addElement(classObject);
					} else if (object instanceof ClassObject classObject) {
						classObject.addMethod(methodObject);
					} else {
						loadFile.getParent().getOut().println("ERROR: " + methodObject + " couldn't be assigned to its " +
								"class.");
					}
					this.aotCache.addElement(methodObject);
				}
			} catch (Exception e) {
				loadFile.getParent().getOut().println("ERROR: " + e.getMessage());
				loadFile.getParent().getOut().println(content);
			}
		}
	}
}
