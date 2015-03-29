/*
 * Rsync filter rules
 *
 * Copyright (C) 1996-2011 by Andrew Tridgell, Wayne Davison, and others
 * Copyright (C) 2013, 2014 Per Lundqvist
 * Copyright (C) 2014 Florian Sager
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.perlundq.yajsync.filelist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.github.perlundq.yajsync.util.ArgumentParsingError;

public class FilterRuleList {

	public List<FilterRule> _rules = new ArrayList<FilterRule>();

	public void addRule(String rule) throws ArgumentParsingError {
		_rules.add(new FilterRule(rule));
	}

	public boolean include(String filename, boolean isDirectory) {

		for (FilterRule rule : _rules) {

			if (!isDirectory && rule.isDirectoryOnly())
				continue;

			boolean matches = rule.matches(filename);

			if (matches) {
				return rule.isInclusion();
			}
		}

		return false;
	}

	/*
	 * see http://rsync.samba.org/ftp/rsync/rsync.html --> FILTER RULES
	 */
	private class FilterRule {

		private boolean _inclusion;
		private boolean _directoryOnly;
		private boolean _absoluteMatching;
		private boolean _negateMatching;
		private boolean _patternMatching;
		private String _path;
		private Pattern _pattern;

		/*
		 * @formatter:off
		 * 
		 * Input samples: + /some/path/this-file-is-found + *.csv - * + !/.svn/
		 * 
		 * @formatter:on
		 */

		public FilterRule(String plainRule) throws ArgumentParsingError {

			String[] splittedRule = plainRule.split("\\s+");
			if (splittedRule.length != 2) {
				throw new ArgumentParsingError(
						String.format(
								"failed to parse filter rule '%s', invalid format: should be '<+|-> <modifier><path-expression>'",
								plainRule));
			}

			if (!"+".equals(splittedRule[0]) && !"-".equals(splittedRule[0])) {
				throw new ArgumentParsingError(
						String.format(
								"failed to parse filter rule '%s': must start with + (inclusion) or - (exclusion)",
								plainRule));
			}

			_inclusion = "+".equals(splittedRule[0]);

			_directoryOnly = splittedRule[1].endsWith("/");

			_negateMatching = splittedRule[1].startsWith("!");

			_path = splittedRule[1].substring(_negateMatching ? 1 : 0,
					_directoryOnly ? splittedRule[1].length() - 1
							: splittedRule[1].length());

			_absoluteMatching = _path.startsWith("/");

			// check if string or pattern matching is required
			// _patternMatching = _path.contains("*") || _path.contains("?") ||
			// _path.contains("[");
			_patternMatching = _path.matches(".*[\\*\\?\\[].*");

			if (_patternMatching) {

				StringBuilder b = new StringBuilder();

				if (_absoluteMatching) {
					b.append("^");
				}

				for (int i = 0; i < _path.length(); i++) {

					char c = _path.charAt(i);

					if (c == '?') {
						b.append("[^/]");
					} else if (c == '*' && i + 1 < _path.length()
							&& _path.charAt(i + 1) == '*') {
						b.append(".*");
					} else if (c == '*') {
						b.append("[^/]*");
					} else {
						b.append(c);
					}
				}

				_pattern = Pattern.compile(b.toString());
			}
		}

		public boolean matches(String filename) {

			boolean _result;

			if (_patternMatching) {
				_result = _pattern.matcher(filename).matches();
			} else {
				// string matching
				if (_absoluteMatching) {
					_result = filename.startsWith(_path);
				} else {
					_result = filename.contains(_path);
				}
			}

			return _negateMatching ? !_result : _result;
		}

		public boolean isInclusion() {
			return _inclusion;
		}

		public boolean isDirectoryOnly() {
			return _directoryOnly;
		}

		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(_inclusion ? "+" : "-").append(" ");
			buf.append(_negateMatching ? "!" : "");
			if (_patternMatching) {
				buf.append(_pattern.toString());
			} else {
				buf.append(_path);
			}
			if (_directoryOnly) {
				buf.append(" (directory only)");
			}

			return buf.toString();
		}
	}
}