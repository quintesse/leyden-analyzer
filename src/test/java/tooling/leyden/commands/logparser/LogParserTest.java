package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tooling.leyden.aotcache.AOTCache;
import tooling.leyden.aotcache.Element;
import tooling.leyden.commands.DefaultTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class LogParserTest extends DefaultTest {

	@ParameterizedTest
	@ValueSource(strings = {"aot.log", "aot.log.0", "aot.log.1"})
	void accept(String file) throws Exception {
		File f = new File(getClass().getResource(file).getPath());
		getSystemRegistry().execute("load log " + f.getAbsolutePath());
		final var aotCache = getDefaultCommand().getAotCache();
		assertTrue(aotCache.getAll().size() > 0);

		//Now check individual values
		for (Element e : aotCache.getAll()) {
			assertNull(e.getAddress()); //Log doesn't provide this
			assertNotNull(e.getKey());
			assertNull(e.getSize()); //Log doesn't provide this
			assertNotNull(e.getType());
			assertEquals(1, e.getSources().size());
		}
	}
}