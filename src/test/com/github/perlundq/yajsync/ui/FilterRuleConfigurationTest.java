package com.github.perlundq.yajsync.ui;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.perlundq.yajsync.util.ArgumentParsingError;

public class FilterRuleConfigurationTest {

	private static Path rootDirectory;
	private static String mergeFile;

	@BeforeClass
	public static void beforeClass() throws IOException {
		File f = File.createTempFile(".rsyncMerge", ".filter");
		rootDirectory = f.getParentFile().toPath().toAbsolutePath();
		mergeFile = f.getName();

		PrintWriter writer = new PrintWriter(f);
		writer.println("# rsync filter file");
		writer.println("");
		writer.println("+ abc");
		writer.println("- def");
		writer.close();

		f.deleteOnExit();
	}

	@Test
	public void test1() throws ArgumentParsingError {

		FilterRuleConfiguration cfg = new FilterRuleConfiguration(null,
				rootDirectory);
		cfg.readRule("+ test");

		assertEquals(true, cfg.include("test", false));
	}

	@Test
	public void test2() throws ArgumentParsingError {

		FilterRuleConfiguration cfg = new FilterRuleConfiguration(null,
				rootDirectory);
		cfg.readRule("+ test");
		cfg.readRule(":e .rsyncInclude-not-exists");

		assertEquals(true, cfg.include("test", false));
	}

	@Test
	public void test3() throws ArgumentParsingError {

		FilterRuleConfiguration cfg = new FilterRuleConfiguration(null,
				rootDirectory);
		cfg.readRule("+ test");
		cfg.readRule(":e " + mergeFile);

		assertEquals(true,
				cfg.include("test", false) && !cfg.include(mergeFile, false));
	}

	@Test
	public void test3a() throws ArgumentParsingError {

		FilterRuleConfiguration cfg = new FilterRuleConfiguration(null,
				rootDirectory);
		cfg.readRule("+ test");
		cfg.readRule("+ *");
		cfg.readRule("dir-merge " + mergeFile);

		assertEquals(true,
				cfg.include("test", false) && cfg.include(mergeFile, false));
	}

	@Test
	public void test4() throws ArgumentParsingError {

		FilterRuleConfiguration cfg = new FilterRuleConfiguration(null,
				rootDirectory);
		cfg.readRule("+ test");
		cfg.readRule(".e " + mergeFile);

		assertEquals(
				true,
				cfg.include("test", false) && !cfg.include(mergeFile, false)
						&& cfg.include("abc", false)
						&& !cfg.include("def", false));
	}

	@Test
	public void test4a() throws ArgumentParsingError {

		FilterRuleConfiguration parentCfg = new FilterRuleConfiguration(null,
				rootDirectory);
		parentCfg.readRule("+ test");
		parentCfg.readRule("merge,e " + mergeFile);

		FilterRuleConfiguration dirCfg = new FilterRuleConfiguration(parentCfg, rootDirectory);

		assertEquals(
				true,
				dirCfg.include("test", false) && !dirCfg.include(mergeFile, false)
						&& dirCfg.include("abc", false)
						&& !dirCfg.include("def", false));
	}

	@Test
	public void test5() throws ArgumentParsingError {

		FilterRuleConfiguration parentCfg = new FilterRuleConfiguration(null,
				rootDirectory);
		parentCfg.readRule("+ test");
		parentCfg.readRule("dir-merge,n " + mergeFile);	// non-inherited per-dir rules

		FilterRuleConfiguration dirCfg = new FilterRuleConfiguration(parentCfg,
				rootDirectory);
		dirCfg.readRule("+ test");

		assertEquals(
				true,
				parentCfg.include("test", false) && dirCfg.include(mergeFile, false)
						&& parentCfg.include("abc", false)
						&& parentCfg.include("def", false)
						&& dirCfg.include("test", false)
						&& dirCfg.include("abc", false)
						&& !dirCfg.include("def", false));
	}

	@Test
	public void test5a() throws ArgumentParsingError {

		FilterRuleConfiguration parentCfg = new FilterRuleConfiguration(null,
				rootDirectory);
		parentCfg.readRule("+ test");
		parentCfg.readRule("dir-merge " + mergeFile);

		FilterRuleConfiguration subCfg = new FilterRuleConfiguration(parentCfg,
				rootDirectory);
		subCfg.readRule("+ test");

		assertEquals(
				true,
				parentCfg.include("test", false) && parentCfg.include(mergeFile, false)
						&& parentCfg.include("abc", false)
						&& parentCfg.include("def", false)
						&& subCfg.include("test", false)
						&& subCfg.include("abc", false)
						&& !subCfg.include("def", false));
	}

	@Test
	public void test6() throws ArgumentParsingError {

		FilterRuleConfiguration parentCfg = new FilterRuleConfiguration(null,
				rootDirectory);
		parentCfg.readRule("merge " + mergeFile);
		parentCfg.readRule("+ *");

		FilterRuleConfiguration dirCfg = new FilterRuleConfiguration(parentCfg,
				rootDirectory);
		dirCfg.readRule("+ test");

		assertEquals(
				true,
				parentCfg.include("test", false) && dirCfg.include(mergeFile, false)
						&& dirCfg.include("abc", false)
						&& !parentCfg.include("def", false)
						&& dirCfg.include("test", false)
						&& dirCfg.include("ghi", false)
						&& !dirCfg.include("def", false));
	}

	@Test
	public void testProtection() throws ArgumentParsingError {

		FilterRuleConfiguration parentCfg = new FilterRuleConfiguration(null,
				rootDirectory);
		parentCfg.readRule("merge " + mergeFile);
		parentCfg.readRule("+ *");

		FilterRuleConfiguration dirCfg = new FilterRuleConfiguration(parentCfg,
				rootDirectory);
		dirCfg.readRule("+ test");
		dirCfg.readRule("P test");
		dirCfg.readRule("R test2");

		assertEquals(
				true,
				parentCfg.include("test", false) && dirCfg.include(mergeFile, false)
						&& dirCfg.include("abc", false)
						&& !parentCfg.include("def", false)
						&& dirCfg.include("test", false)
						&& dirCfg.include("ghi", false)
						&& !dirCfg.include("def", false)
						&& dirCfg.protect("test", false)
						&& dirCfg.risk("test2", false));
	}
}
