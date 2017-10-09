package de.upb.crc901.mascot.structure;

import java.util.List;
import java.util.Set;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.util.LiteralStringParser;
import jaicore.logic.fol.util.TypeUtil;

public class GenericAbstractLiteral extends GenericLiteral {

	public GenericAbstractLiteral(String name, List<LiteralParam> params, boolean positive) {
		super(name, params, positive);
	}

	public static GenericAbstractLiteral get(String predicateString, Set<String> evaluablePredicates) {
		Literal l = LiteralStringParser.convertStringToLiteralWithConst(predicateString, evaluablePredicates);
		TypeUtil.defineGodfatherDataTypes(l);
		return new GenericAbstractLiteral(l.getProperty(), l.getParameters(), l.isPositive());
	}
}