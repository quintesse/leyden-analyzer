package tooling.leyden.commands;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import tooling.leyden.aotcache.ClassObject;
import tooling.leyden.aotcache.Element;
import tooling.leyden.aotcache.MethodObject;
import tooling.leyden.commands.logparser.AOTMapParser;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ListCommandTest extends DefaultTest {

	@Test
	void checkUsedAndNotTrained() {
		final var loadFile = new LoadFileCommand();
		loadFile.setParent(getDefaultCommand());
		AOTMapParser aotCacheParser = new AOTMapParser(loadFile);

		aotCacheParser.accept("0x0000000801711128: @@ Class             624 org.infinispan.xsite.NoOpBackupSender");
		aotCacheParser.accept("0x00000008017113f0: @@ ConstantPoolCache 64 org.infinispan.xsite.NoOpBackupSender");
		aotCacheParser.accept("0x00000008017116c0: @@ Method            88 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x00000008017115b8: @@ Method            88 org.infinispan.interceptors" +
				".InvocationStage org.infinispan.xsite.NoOpBackupSender.backupClear(org.infinispan.commands.write.ClearCommand)");
		aotCacheParser.accept("0x0000000801b3d6e0: @@ MethodCounters    64 org.infinispan.interceptors" +
				".InvocationStage org.infinispan.xsite.NoOpBackupSender.backupClear(org.infinispan.commands.write.ClearCommand)");
		aotCacheParser.accept("0x0000000801711610: @@ Method            88 void org.infinispan.xsite.NoOpBackupSender.<init>()");
		aotCacheParser.accept("0x0000000801711668: @@ Method            88 void org.infinispan.xsite.NoOpBackupSender.<clinit>()");
		aotCacheParser.accept("0x0000000801b3d568: @@ MethodTrainingData 96 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x0000000801b3d5f0: @@ MethodData        240 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x0000000801b3d6e0: @@ MethodCounters    64 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x0000000802305250: @@ Symbol            48 org.infinispan.xsite.NoOpBackupSender)");
		aotCacheParser.accept("0x00000008025befb8: @@ Symbol            48 org/infinispan/xsite/NoOpBackupSender)");
		aotCacheParser.accept("0x00000008025befe8: @@ Symbol            48 ()Lorg/infinispan/xsite/NoOpBackupSender;)");
		aotCacheParser.accept("0x00000008026534d0: @@ Symbol            48 Lorg/infinispan/xsite/NoOpBackupSender;)");
		aotCacheParser.accept("0x000000080429e1e0: @@ ConstMethod       80 java.lang.String org.infinispan.xsite.NoOpBackupSender.toString())");
		aotCacheParser.accept("0x00000008017115b8: @@ ConstMethod            88 org.infinispan.interceptors" +
				".InvocationStage org.infinispan.xsite.NoOpBackupSender.backupClear(org.infinispan.commands.write.ClearCommand)");
		aotCacheParser.accept("0x000000080429e230: @@ ConstantPool      568 org.infinispan.xsite.NoOpBackupSender)");
		aotCacheParser.accept("0x000000080429e780: @@ ConstMethod       64 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");
		aotCacheParser.accept("0x0000000801cd5648: @@ CompileTrainingData 80 1 org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()");

		aotCacheParser.accept("0x0000000801711128: @@ Class             624 java.lang.UnsupportedOperationException");
		aotCacheParser.accept("0x0000000801bb65c8: @@ KlassTrainingData 40 java.lang.UnsupportedOperationException");

		ListCommand command = new ListCommand();
		command.parent = getDefaultCommand();
		command.run = false;
		command.trained = false;
		command.parameters = new CommonParameters();
		assertEquals(21, command.findElements(new AtomicInteger()).count());

		var count = new AtomicInteger();
		command.parameters.types = new String[]{ "Class" };
		assertTrue(command.findElements(count).allMatch(e -> e instanceof ClassObject));
		assertEquals(2, count.get());

		count = new AtomicInteger();
		command.parameters.types = new String[]{ "Class", "Method" };
		assertTrue(command.findElements(count).allMatch(e -> e instanceof ClassObject || e instanceof MethodObject));
		assertEquals(6, count.get());

		command.run = true;
		command.parameters.types = null;
		count = new AtomicInteger();
		assertTrue(command.findElements(count).allMatch(e -> e.isTraineable()));
		assertEquals(2, count.get());

		command.trained = true;
		count = new AtomicInteger();
		assertTrue(command.findElements(count).allMatch(e -> e.isTrained()));
		assertEquals(1, count.get());

		command.run = false;
		count = new AtomicInteger();
		assertTrue(command.findElements(count).allMatch(e -> e.isTrained()));
		assertEquals(2, count.get());
	}
}