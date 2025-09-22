package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.DefaultTest;
import tooling.leyden.commands.LoadFileCommand;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AOTCacheParserTest extends DefaultTest {

	@Test
	void accept() throws Exception {
		File file = new File(getClass().getResource("aot.map").getPath());
		getSystemRegistry().execute("load aotCache " + file.getAbsolutePath());
		final var aotCache = getDefaultCommand().getAotCache();
		assertTrue(aotCache.getAll().size() > 0);
		assertEquals(0, aotCache.getErrors().size());

		//Now check individual values
		//Skip classes because they may have been indirectly generated
		aotCache.getAll().parallelStream().filter(e -> !e.getType().equalsIgnoreCase("Class")).forEach(e -> {
			assertNotNull(e.getAddress(), "Address of " + e + " shouldn't be null.");
			assertNotNull(e.getKey(), "Key of " + e + " shouldn't be null.");
			assertNotNull(e.getSize(), "Size of " + e + " shouldn't be null.");
			assertNotNull(e.getType(), "Type of " + e + " shouldn't be null.");
			assertEquals(1, e.getSources().size(), "We shouldn't have more than one source here " + e.getSources().stream().reduce((s, s2) -> s + ", " + s2));
		});

		assertEquals(655, aotCache.getByPackage(null, "Symbol").size());
		assertEquals(638, aotCache.getByPackage(null, "Class").size());
		assertEquals(114, aotCache.getByPackage(null, "ConstantPool").size());
		assertEquals(192, aotCache.getByPackage(null, "ConstantPoolCache").size());
	}

	@Test
	void acceptObjects() throws Exception {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		final var aotCache = loadFile.getParent().getAotCache();
		aotCache.clear();
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		var classObject = new ClassObject();
		classObject.setName("Object");
		classObject.setPackageName("java.lang");
		aotCache.addElement(classObject, "test");

		classObject = new ClassObject();
		classObject.setName("String");
		classObject.setPackageName("java.lang");
		aotCache.addElement(classObject, "test");

		aotCacheParser.accept("0x00000000fff601f0: @@ Object (0xfff601f0) [Ljava.lang.Object; length: 21");
		aotCacheParser.accept("0x00000000fff63458: @@ Object (0xfff63458) java.lang.String$CaseInsensitiveComparator");
		aotCacheParser.accept("0x00000000fff632f0: @@ Object (0xfff632f0) [I length: 0");
		aotCacheParser.accept("0x00000000fff62900: @@ Object (0xfff62900) java.lang.Float");

		assertEquals(7, aotCache.getAll().size());
		final var objects = aotCache.getByPackage("", "Object");
		assertEquals(4, objects.size());
		for (Element e : objects) {
			assertTrue(e instanceof ReferencingElement);
			ReferencingElement re = (ReferencingElement) e;
			if (!re.getKey().equals("[I length: 0")) {
				assertNotNull(re.getReference());
				assertTrue(re.getKey().contains(re.getReference().getKey()));
			}
		}

	}


}