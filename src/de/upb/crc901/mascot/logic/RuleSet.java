package de.upb.crc901.mascot.logic;

import java.util.HashSet;
import java.util.Set;

import jaicore.logic.fol.structure.ConstantParam;

/**
 * This is a structure for maintenance of rules. Even though there are currently
 * no methods for such a set, it is good to have a unified class for them. This
 * also facilitates type checks, because no generics are necessary.
 * 
 * @author Felix
 *
 */
public class RuleSet extends HashSet<Rule> {
	private static final long serialVersionUID = 8220935169569648914L;

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		for (Rule r : this) {
			constants.addAll(r.getConstantParams());
		}
		return constants;
	}
}