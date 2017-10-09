package de.upb.crc901.mascot.structure;

import java.util.List;
import java.util.Set;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.util.LiteralStringParser;
import jaicore.logic.fol.util.TypeUtil;

public class GenericBooleanExpression extends GenericLiteral {

	protected GenericBooleanExpression(String name, List<LiteralParam> variables, boolean positive) {
		super(name, variables, positive);
	}

	public static GenericBooleanExpression get(String predicateString, Set<String> evaluablePredicates) {
		Literal l = LiteralStringParser.convertStringToLiteralWithConst(predicateString, evaluablePredicates);
		TypeUtil.defineGodfatherDataTypes(l);
		return (new GenericBooleanExpression(l.getProperty(), l.getParameters(), l.isPositive()));
	}

}