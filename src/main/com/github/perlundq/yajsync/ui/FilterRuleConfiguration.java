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

package com.github.perlundq.yajsync.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.github.perlundq.yajsync.filelist.FilterRuleList;
import com.github.perlundq.yajsync.util.ArgumentParsingError;

public class FilterRuleConfiguration {

	private FilterRuleConfiguration _parentRuleConfiguration = null;
	// private List<MergeRule> _mergeRuleList = new ArrayList<>();
	private FilterRuleList _localRuleList = new FilterRuleList();
	private boolean _inheritance = true;
	private String _dirMergeFilename = null;
	private String _dirname = null;

	// FSTODO: CustomFileSystem.getConfigPath( ... )

	public FilterRuleConfiguration(List<String> inputFilterRules) throws ArgumentParsingError {
		for (String inputFilterRule : inputFilterRules) {
			readRule(inputFilterRule);
		}
	}

	public FilterRuleConfiguration(
			FilterRuleConfiguration parentRuleConfiguration, String dirname)
			throws ArgumentParsingError {

		_parentRuleConfiguration = parentRuleConfiguration;
		if (_parentRuleConfiguration != null) {
			_inheritance = _parentRuleConfiguration.isInheritance();
			_dirMergeFilename = _parentRuleConfiguration.getDirMergeFilename();
		}
		_dirname = dirname;

		// directory specific initialization of rules, FSTODO: no inheritance?
		// _mergeRuleList = parentRuleConfiguration.getMergeRuleList();
		// readMergeRules();

		if (_dirMergeFilename != null
				&& (new File(_dirname + "/" + _dirMergeFilename)).exists()) {
			// merge local filter rule file
			readRule(". " + dirname + "/" + _dirMergeFilename);
		}
	}

	public void readRule(String plainRule) throws ArgumentParsingError {

		String[] splittedRule = plainRule.split("\\s+");

		if (splittedRule.length == 1 && (splittedRule[0].startsWith("!") || splittedRule[0].startsWith("clear"))) {
			// LIST-CLEARING FILTER RULE
			_localRuleList = new FilterRuleList();
			_parentRuleConfiguration = null;
			return;
		}

		if (splittedRule.length != 2) {
			throw new ArgumentParsingError(
					String.format(
							"failed to parse filter rule '%s', invalid format: should be '<+|-|merge|dir-merge>,<modifier> <filename|path-expression>'",
							plainRule));
		}

		Modifier m = readModifiers(splittedRule[0].trim(), plainRule);
		m.checkValidity(plainRule);

		if (m._merge == true || m._dirMerge == true) {

			if (m._noInheritanceOfRules == true) {
				_inheritance = false;
			}

			if (m._merge == true) {

				// _mergeRuleList.add(new MergeRule(m, splittedRule[1].trim()));

				try (BufferedReader br = new BufferedReader(new FileReader(
						_dirname + "/" + splittedRule[1].trim()))) {
					String line = br.readLine();
					while (line != null) {
						line = line.trim();
						// ignore empty lines or comments
						if (line.length() != 0 && !line.startsWith("#")) {

							if (m._exclude == true) {
								_localRuleList.addRule("- " + line);
							} else if (m._include == true) {
								_localRuleList.addRule("+ " + line);
							} else {
								readRule(line);
							}
						}
						line = br.readLine();
					}
				} catch (IOException e) {
					throw new ArgumentParsingError(String.format(
							"impossible to parse filter file '%s'",
							splittedRule[1]));
				}

				return;
			}

			if (_dirMergeFilename == null && m._dirMerge == true) {
				_dirMergeFilename = splittedRule[1].trim();
			}

			if (m._excludeMergeFilename && _dirMergeFilename != null) {
				_localRuleList.addRule("- " + _dirMergeFilename);
			}

			return;
		}

		if (m._exclude == true) {
			_localRuleList.addRule("- " + splittedRule[1].trim());
			return;
		} else if (m._include == true) {
			_localRuleList.addRule("+ " + splittedRule[1].trim());
			return;
		}

		throw new ArgumentParsingError(String.format("invalid rule %s",
				plainRule));
	}

	// FSTODO: korrigieren, dir-merge müsste zur Laufzeit akt. werden, nicht 'merge' ?
	/* public void readMergeRules() throws ArgumentParsingError {

		for (MergeRule mergeRule : _mergeRuleList) {

			try (BufferedReader br = new BufferedReader(new FileReader(
					_dirname + "/" + mergeRule._filename))) {
				String line = br.readLine();
				while (line != null) {
					line = line.trim();
					// ignore empty lines or comments
					if (line.length() != 0 && !line.startsWith("#")) {
						if (mergeRule._modifier._exclude == true) {
							_localRuleList.addRule("- " + line);
						} else if (mergeRule._modifier._include == true) {
							_localRuleList.addRule("+ " + line);
						} else {
							readRule(line);
						}
					}
					line = br.readLine();
				}
			} catch (IOException e) {
				throw new ArgumentParsingError(String.format(
						"impossible to parse filter file '%s' for %s",
						mergeRule._filename, _dirname));
			}
		}
	} */

	public boolean include(String filename, boolean isDirectory) {

		assureDirectoryPathname(filename, isDirectory);

		if (_localRuleList.include(filename, isDirectory)) {
			return true;
		}

		// search root and check against root only
		FilterRuleConfiguration parent = this;
		while (parent.getParentRuleConfiguration() != null) {
			parent = parent.getParentRuleConfiguration();
			if (parent.isInheritance()) {
				if (parent.include(filename, isDirectory)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean exclude(String filename, boolean isDirectory) {

		filename = assureDirectoryPathname(filename, isDirectory);

		if (_localRuleList.exclude(filename, isDirectory)) {
			return true;
		}

		// search root and check against root only
		FilterRuleConfiguration parent = this;
		while (parent.getParentRuleConfiguration() != null) {
			parent = parent.getParentRuleConfiguration();
			if (parent.isInheritance()) {
				if (parent.exclude(filename, isDirectory)) {
					return true;
				}
			}
		}

		return false;
	}

	private String assureDirectoryPathname(String filename, boolean isDirectory) {

		if (!isDirectory) return filename;
		if (isDirectory && !filename.endsWith("/")) {
			return filename + "/";
		}
		return filename;
	}

	public FilterRuleConfiguration getParentRuleConfiguration() {
		return _parentRuleConfiguration;
	}

	public boolean isInheritance() {
		return _inheritance;
	}

	public String getDirMergeFilename() {
		return _dirMergeFilename;
	}

	/* public List<MergeRule> getMergeRuleList() {
		return _mergeRuleList;
	} */

	// see http://rsync.samba.org/ftp/rsync/rsync.html --> MERGE-FILE FILTER
	// RULES
	private Modifier readModifiers(String modifier, String plainRule)
			throws ArgumentParsingError {

		Modifier m = new Modifier();

		int i = 0;
		while (i < modifier.length()) {

			char c = modifier.charAt(i);

			if (c == '-') {
				// exclude rule
				m._exclude = true;
				i++;
				continue;
			} else if (c == '+') {
				// include rule
				m._include = true;
				i++;
				continue;
			}

			if (i > 0) {
				if (c == 'e') {
					// exclude the merge-file name from the transfer
					m._excludeMergeFilename = true;
					i++;
					continue;
				} else if (c == 'n') {
					// don't inherit rules
					m._noInheritanceOfRules = true;
					i++;
					continue;
				} else if (c == 'w') {
					// A w specifies that the rules are word-split on whitespace
					// instead of the normal line-splitting
					throw new ArgumentParsingError(
							String.format(
									"the modifier 'w' is not implemented, see rule '%s'",
									plainRule));
				} else if (c == ',') {
					i++;
					continue;
				}
			}

			if (c == '.') {
				// merge
				m._merge = true;
				i++;
				continue;
			} else if (c == ':') {
				// dir-merge
				m._dirMerge = true;
				i++;
				continue;
			} else if (c == 'm' && i + 5 <= modifier.length()
					&& "merge".equals(modifier.substring(i, i + 5))) {
				// merge
				m._merge = true;
				i += 5;
				continue;
			} else if (c == 'd' && i + 9 <= modifier.length()
					&& "dir-merge".equals(modifier.substring(i, i + 9))) {
				// dir-merge
				m._dirMerge = true;
				i += 9;
				continue;
			}

			throw new ArgumentParsingError(String.format(
					"unknown modifier '%c' in rule %s", c, plainRule));
		}

		return m;
	}

	/* public void setFilterRuleList(FilterRuleList localRuleList) {
		this._localRuleList = localRuleList;
	} */

	public FilterRuleList getFilterRuleList() {
		return _localRuleList;
	}

	public boolean isFilterAvailable() {
		boolean result = _localRuleList._rules.size() > 0;
		if (!result && _inheritance && _parentRuleConfiguration!=null) {
			result = _parentRuleConfiguration.isFilterAvailable();
		}
		return result;
	}

	private class Modifier {
		boolean _include;
		boolean _exclude;
		boolean _excludeMergeFilename;
		boolean _noInheritanceOfRules;
		boolean _merge;
		boolean _dirMerge;

		public void checkValidity(String plainRule) throws ArgumentParsingError {
			if ((_merge && _dirMerge) || (_include && _exclude)) {
				throw new ArgumentParsingError(
						String.format(
								"invalid combination of modifiers in rule %s (processing %s)",
								plainRule,
								FilterRuleConfiguration.this._dirname));
			}
		}
	}

	/* private class MergeRule {
		Modifier _modifier;
		String _filename;

		public MergeRule(Modifier modifier, String filename) {
			_modifier = modifier;
			_filename = filename;
		}
	} */

	@Override
	public String toString() {

		StringBuilder buf = new StringBuilder();

		buf.append("dir=").append(_dirname).append("; ");
		buf.append("rules=[").append(_localRuleList.toString()).append("]; ");
		buf.append("inheritance=").append(new Boolean(_inheritance).toString()).append("; ");
		if (_dirMergeFilename!=null) {
			buf.append("dirMergeFilename=").append(_dirMergeFilename).append("; ");
		}

		if (_parentRuleConfiguration!=null) {
			buf.append("parent=").append(_parentRuleConfiguration.toString());
		}

		return buf.toString();
	}
}
