package de.upb.crc901.mascot.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.upb.crc901.configurationsetting.compositiondomain.CompositionDomain;
import de.upb.crc901.configurationsetting.serialization.util.LiteralConverterUtil;
import de.upb.crc901.mascot.structure.GenericAbstractLiteral;
import de.upb.crc901.mascot.structure.GenericBooleanExpression;
import de.upb.crc901.mascot.structure.GenericOperationCall;
import de.upb.crc901.mascot.structure.Template;
import jaicore.logic.fol.structure.HornRule;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.TypeModule;
import jaicore.logic.fol.structure.VariableParam;

public class TemplateParser {
	private final static String GODFATHER_TYPE = "Thing";
	private static TypeModule typeMod;

	public Template parseTemplate(String templateText, CompositionDomain compDomain) throws Exception {
		Set<String> evaluablePredicates = compDomain.getInterpretedLiteralNames();
		typeMod = compDomain.getTypeModule();

		/* identify inputs, outputs, precondition, and effect */
		Set<VariableParam> inputs = Arrays
				.asList(findMatch("IN:(.*)\n", templateText).group(1).replace(" ", "").split(",")).stream()
				.map(n -> new VariableParam(n, typeMod.getType(GODFATHER_TYPE))).collect(Collectors.toSet());
		Set<VariableParam> outputs = Arrays
				.asList(findMatch("OUT:(.*)\n", templateText).group(1).replace(" ", "").split(",")).stream()
				.map(n -> new VariableParam(n, typeMod.getType(GODFATHER_TYPE))).collect(Collectors.toSet());
		LiteralSet precondition = LiteralConverterUtil.convertStringToLiteralSetWithConst(
				findMatch("PRE:(.*)\n", templateText).group(1).replace(" ", ""), evaluablePredicates);
		jaicore.logic.fol.util.TypeUtil.defineGodfatherDataTypes(precondition);
		LiteralSet postcondition = LiteralConverterUtil.convertStringToLiteralSetWithConst(
				findMatch("POST:(.*)\n", templateText).group(1).replace(" ", ""), evaluablePredicates);
		jaicore.logic.fol.util.TypeUtil.defineGodfatherDataTypes(postcondition);

		/* identify placeholders for generic component calls */
		List<GenericOperationCall> genericComponentCalls = new LinkedList<GenericOperationCall>();
		for (MatchResult r : findMatches("\\((.*)\\) := (.*)\\((.*)\\)", templateText)) {
			String name = r.group(2);
			Set<VariableParam> genericCallInputs = new HashSet<VariableParam>();
			for (String varname : r.group(3).split(","))
				genericCallInputs.add(new VariableParam(varname, typeMod.getType(GODFATHER_TYPE)));
			Set<VariableParam> genericCallOutputs = new HashSet<VariableParam>();
			for (String varname : r.group(1).split(","))
				genericCallOutputs.add(new VariableParam(varname, typeMod.getType(GODFATHER_TYPE)));
			genericComponentCalls.add(new GenericOperationCall(name, genericCallInputs, genericCallOutputs));
		}

		/* identify placeholders for boolean expressions */
		List<GenericBooleanExpression> booleanExpressions = new LinkedList<GenericBooleanExpression>();
		for (MatchResult r : findMatches("boolpred:(.*)\\((.*)\\)", templateText)) {
			String args = "";
			for (byte i = 1; i <= r.group(2).split(",").length; i++)
				args += ((args.length() == 0) ? "" : ",") + "arg" + i;

			booleanExpressions.add(GenericBooleanExpression.get(r.group(1) + "(" + args + ")", evaluablePredicates));
		}

		/* identify placeholders for boolean expressions */
		List<GenericAbstractLiteral> helperPredicates = new LinkedList<GenericAbstractLiteral>();
		for (MatchResult r : findMatches("helperpred:(.*)\\((.*)\\)", templateText)) {
			String args = "";
			for (byte i = 1; i <= r.group(2).split(",").length; i++)
				args += ((args.length() == 0) ? "" : ",") + "arg" + i;
			helperPredicates.add(GenericAbstractLiteral.get(r.group(1) + "(" + args + ")", evaluablePredicates));
		}

		/* identify positive consistency rules */
		List<HornRule> positiveRules = new LinkedList<>();
		for (MatchResult r : findMatches("RULE\\+: (.*) --> (.*)\\\n", templateText)) {
			try {
				Monom premise = new Monom(
						LiteralConverterUtil.convertStringToLiteralSetWithConst(r.group(1), evaluablePredicates));
				jaicore.logic.fol.util.TypeUtil.defineGodfatherDataTypes(premise);
				Literal conclusion = LiteralConverterUtil.convertStringToLiteralWithConst(r.group(2),
						evaluablePredicates);
				jaicore.logic.fol.util.TypeUtil.defineGodfatherDataTypes(conclusion);

				positiveRules.add(new HornRule(premise, conclusion));
			} catch (Exception e) {
				System.out.println("[Error] Rule could not be parsed: " + r.group(1) + " --> " + r.group(2));
				e.printStackTrace();
			}
		}

		/* identify negative consistency rules */
		List<HornRule> negativeRules = new LinkedList<>();
		for (MatchResult r : findMatches("RULE-: (.*) --> (.*)\\\n", templateText)) {
			try {
				Monom premise = new Monom(
						LiteralConverterUtil.convertStringToLiteralSetWithConst(r.group(1), evaluablePredicates));
				jaicore.logic.fol.util.TypeUtil.defineGodfatherDataTypes(premise);
				Literal conclusion = LiteralConverterUtil.convertStringToLiteralWithConst(r.group(2),
						evaluablePredicates);
				jaicore.logic.fol.util.TypeUtil.defineGodfatherDataTypes(conclusion);

				negativeRules.add(new HornRule(premise, conclusion));
			} catch (Exception e) {
				System.out.println("[Error] Rule could not be parsed: " + r.group(1) + " --> " + r.group(2));
				e.printStackTrace();
			}
		}
		return new Template(inputs, outputs, precondition, postcondition, genericComponentCalls, booleanExpressions,
				helperPredicates, positiveRules, negativeRules);
	}

	static Iterable<MatchResult> findMatches(String pattern, CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();
		for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();)
			results.add(m.toMatchResult());
		return results;
	}

	static MatchResult findMatch(String pattern, CharSequence s) {
		Matcher m = Pattern.compile(pattern).matcher(s);
		m.find();
		return m.toMatchResult();
	}

}
