package com.github.perlundq.yajsync.filelist;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.perlundq.yajsync.util.ArgumentParsingError;

public class FilterRuleListTest {

	@Test
	public void testEmptyRules() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();

		assertEquals(true, list.include("./dir1", true));
		assertEquals(true, list.include("./file1", false));
		assertEquals(false, list.exclude("./file2", false));
	}

	@Test
	public void test1() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/file1");
		list.addRule("- /dir1/*");

		assertEquals(true, list.include("./dir1/file1", false));
		assertEquals(false, list.exclude("./dir1/file1", false));
	}

	@Test
	public void test2() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/file1");
		list.addRule("- /dir1/*");

		assertEquals(false, list.include("./dir1/file2", false));
		assertEquals(true, list.exclude("./dir1/file2", false));
	}

	@Test
	public void test3() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/*");

		assertEquals(true, list.include("./dir1/file2", false));
		assertEquals(false, list.exclude("./dir1/file2", false));
	}

	@Test
	public void test4() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/**");

		assertEquals(true, list.include("./dir1/dir2/file2", false));
		assertEquals(false, list.exclude("./dir1/dir2/file2", false));
	}

	@Test
	public void test5() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		// list.addRule("- /dir1");
		list.addRule("+ **.txt");

		assertEquals(true, list.include("./dir1/dir2/file2.txt", false));
		assertEquals(false, list.exclude("./dir1/dir2/file2.txt", false));
	}

	@Test
	public void test6() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1");

		assertEquals(true, list.include("./dir1/dir2/file2", false));
		assertEquals(false, list.exclude("./dir1/dir2/file2", false));
	}

	@Test
	public void test7() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ file2");

		assertEquals(true, list.include("./dir1/dir2/file2", false));
		assertEquals(false, list.exclude("./dir1/dir2/file2", false));
	}

	@Test
	public void test8() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/dir2/*.txt");

		assertEquals(true, list.include("./dir1/dir2/file2.txt", false));
		assertEquals(false, list.exclude("./dir1/dir2/file2.txt", false));
	}

	@Test
	public void test9() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("- *");
		list.addRule("+ /dir1/file2");

		assertEquals(false, list.include("./dir1/file1", false));
		assertEquals(true, list.exclude("./dir1/file1", false));
	}

	@Test
	public void test10() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("- *");
		list.addRule("+ /dir1/file2");

		assertEquals(true, list.include("./dir1/file2", false));
		assertEquals(false, list.exclude("./dir1/file2", false));
	}

	@Test
	public void test11() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ *");

		assertEquals(true, list.include(".", false));
		assertEquals(false, list.exclude(".", false));
		assertEquals(true, list.include(".", true));
		assertEquals(false, list.exclude(".", true));
	}

	@Test
	public void test12() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("- *");

		assertEquals(false, list.include(".", false));
		assertEquals(true, list.exclude(".", false));
		assertEquals(false, list.include(".", true));
		assertEquals(true, list.exclude(".", true));
	}
}