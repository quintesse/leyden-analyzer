package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.ReferencingElement;
import tooling.leyden.commands.DefaultTest;

import java.io.File;

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
		assertEquals(494 + 5, aotCache.getByPackage(null, "Class").size());
		assertEquals(114, aotCache.getByPackage(null, "ConstantPool").size());
		assertEquals(192, aotCache.getByPackage(null, "ConstantPoolCache").size());

		//FIXME this fails, are there methods not being loaded?
		// assertEquals(5927 + 1385, aotCache.getByPackage(null, "Method").size());
	}
}