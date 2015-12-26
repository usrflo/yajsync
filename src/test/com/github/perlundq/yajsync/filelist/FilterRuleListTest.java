package com.github.perlundq.yajsync.filelist;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.perlundq.yajsync.filelist.FilterRuleList.Result;
import com.github.perlundq.yajsync.util.ArgumentParsingError;

public class FilterRuleListTest {

	@Test
	public void testEmptyRules() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();

		assertEquals(Result.NEUTRAL, list.check("./dir1", true));
		assertEquals(Result.NEUTRAL, list.check("./file1", false));
	}

	@Test
	public void test1() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/file1");
		list.addRule("- /dir1/*");

		assertEquals(Result.INCLUDED, list.check("./dir1/file1", false));
		assertEquals(Result.INCLUDED, list.check("./dir1/file1", false));
	}

	@Test
	public void test2() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/file1");
		list.addRule("- /dir1/*");

		assertEquals(Result.EXCLUDED, list.check("./dir1/file2", false));
	}

	@Test
	public void test3() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/*");

		assertEquals(Result.INCLUDED, list.check("./dir1/file2", false));
	}

	@Test
	public void test4() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/**");

		assertEquals(Result.INCLUDED, list.check("./dir1/dir2/file2", false));
	}

	@Test
	public void test5() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		// list.addRule("- /dir1");
		list.addRule("+ **.txt");

		assertEquals(Result.INCLUDED, list.check("./dir1/dir2/file2.txt", false));
	}

	@Test
	public void test6() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1");

		assertEquals(Result.INCLUDED, list.check("./dir1/dir2/file2", false));
	}

	@Test
	public void test7() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ file2");

		assertEquals(Result.INCLUDED, list.check("./dir1/dir2/file2", false));
	}

	@Test
	public void test8() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/dir2/*.txt");

		assertEquals(Result.INCLUDED, list.check("./dir1/dir2/file2.txt", false));
	}

	@Test
	public void test9() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/file2");
		list.addRule("- *");

		assertEquals(Result.EXCLUDED, list.check("./dir1/file1", false));
		assertEquals(Result.INCLUDED, list.check("./dir1/file2", false));
	}

	@Test
	public void test10() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ /dir1/file2");
		list.addRule("- *");

		assertEquals(Result.INCLUDED, list.check("./dir1/file2", false));
	}

	@Test
	public void test11() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("+ *");

		assertEquals(Result.INCLUDED, list.check(".", false));
		assertEquals(Result.INCLUDED, list.check(".", true));
	}

	@Test
	public void test12() throws ArgumentParsingError {

		FilterRuleList list = new FilterRuleList();
		list.addRule("- *");

		assertEquals(Result.EXCLUDED, list.check(".", false));
		assertEquals(Result.EXCLUDED, list.check(".", true));
	}
}
