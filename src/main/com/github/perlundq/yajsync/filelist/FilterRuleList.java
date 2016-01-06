/*
 * Rsync filter rules
 *
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

	public enum Result {
		EXCLUDED /* PROTECTED, HIDE */, INCLUDED /* RISK, SHOW */ , NEUTRAL
	}

	public FilterRuleList addList(FilterRuleList list) {
		this._rules.addAll(list._rules);
		return this;
	}

	public void addRule(String rule) throws ArgumentParsingError {
		_rules.add(new FilterRule(rule));
	}

	public Result check(String filename, boolean isDirectory) {

		for (FilterRule rule : _rules) {

			if (!isDirectory && rule.isDirectoryOnly())
				continue;

			boolean matches = rule.matches(filename);

			if (matches) {
				/*
				 * first matching rule matters
				 */
				if (rule.isInclusion()) {
					return Result.INCLUDED;
				} else {
					return Result.EXCLUDED;
				}
			}
		}

		return Result.NEUTRAL;
	}

	/*
	 * see http://rsync.samba.org/ftp/rsync/rsync.html --> FILTER RULES
	 */
	public class FilterRule {

		private final boolean _inclusion;
		private final boolean _directoryOnly;
		private final boolean _absoluteMatching;
		private final boolean _negateMatching;
		private final boolean _patternMatching;
		private final boolean _deletionRule;
		private final boolean _hidingRule;
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
								"failed to parse filter rule '%s', invalid format: should be '<+|-|P|R|H|S> <modifier><path-expression>'",
								plainRule));
			}

			if ("P".equals(splittedRule[0])) {
				splittedRule[0] = "-";
				_deletionRule = true;
			} else if ("R".equals(splittedRule[0])) {
				splittedRule[0] = "+";
				_deletionRule = true;
			} else {
				_deletionRule = false;
			}

			if ("H".equals(splittedRule[0])) {
				splittedRule[0] = "-";
				_hidingRule = true;
			} else if ("S".equals(splittedRule[0])) {
				splittedRule[0] = "+";
				_hidingRule = true;
			} else {
				_hidingRule = false;
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

			// add . for absolute matching to conform to rsync paths
			if (_absoluteMatching) {
				_path = "."+_path;
			}

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
						b.append("[^/].*");
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

				String path = _path + (_directoryOnly ? "/":"");

				// string matching
				if (_absoluteMatching) {
					if (filename.length()<path.length()) {
						// no matching if filename is shorter than _path
						_result = false;
					} else if (filename.length()==path.length()) {
						// matching if filename equals _path
						_result = filename.startsWith(path);
					} else if (filename.charAt(path.length())=='/') {
						// matching if filename is contained in _path
						_result = filename.startsWith(path);
					} else {
						_result = false;
					}
				} else {
					// tail matching
					if (path.length()<filename.length()) {
						_result = filename.endsWith("/"+path);
					} else {
						_result = filename.equals(path);
					}
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

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			if (_deletionRule) {
				buf.append(_inclusion ? "R" : "P").append(" ");
			} else if (_hidingRule) {
				buf.append(_inclusion ? "S" : "H").append(" ");
			} else {
				buf.append(_inclusion ? "+" : "-").append(" ");
			}
			buf.append(_negateMatching ? "!" : "");
			/* if (_patternMatching) {
				buf.append(_pattern.toString());
			} else { */
				buf.append(_path);
			// }
			if (_directoryOnly) {
				buf.append("/");
			}

			return buf.toString();
		}
	}

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();
		for (FilterRule rule : _rules) {
			buf.append(rule).append("; ");
		}
		return buf.toString();
	}
}
