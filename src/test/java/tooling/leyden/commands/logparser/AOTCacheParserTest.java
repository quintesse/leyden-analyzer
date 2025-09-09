package tooling.leyden.commands.logparser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tooling.leyden.commands.DefaultTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AOTCacheParserTest extends DefaultTest {

	@Test
	void accept() throws Exception {
		File file = new File(getClass().getResource("aot.map").getPath());
		getSystemRegistry().execute("load aotCache " + file.getAbsolutePath());
		assertTrue(getDefaultCommand().getAotCache().getAll().size() > 0);

		assertEquals(0, getDefaultCommand().getAotCache().getErrors().size());
	}
}