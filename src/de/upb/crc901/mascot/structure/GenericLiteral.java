package de.upb.crc901.mascot.structure;

import java.util.List;
import java.util.Set;

import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.util.LiteralStringParser;
import jaicore.logic.fol.util.TypeUtil;


public class GenericLiteral extends Literal {
	public GenericLiteral(String name, List<LiteralParam> variables, boolean positive) {
		super(name, variables);
	}

	public GenericLiteral(Literal l) {
		super(l.getProperty(), l.getParameters());
	}

	public static GenericLiteral get(String string, Set<String> evaluablePredicates) {
		Literal l = LiteralStringParser.convertStringToLiteralWithConst(string, evaluablePredicates);
		TypeUtil.defineGodfatherDataTypes(l);
		return new GenericLiteral(l.getProperty(), l.getParameters(), l.isPositive());
	}
}
