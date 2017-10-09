package de.upb.crc901.mascot.template.instantiator.searches;

import java.util.HashMap;
import java.util.Map;

import jaicore.logic.fol.structure.LiteralParam;



/**
 * maps each combination of abstract predicate, concrete predicate, and place of the concrete predicate to a literal parameter
 */
@SuppressWarnings("serial")
public class VariableBinding extends HashMap<String, Map<String, Map<Integer, LiteralParam>>> {

}
