package de.upb.crc901.mascot.template.completeInstantiator;

public abstract class InstantiationUtilOld {

	// private static SetUtil<String> setTool = new SetUtil<String>();
	// private static SetUtil<List<String>> setToolForSignatures = new
	// SetUtil<List<String>>();
	// private static Reasoner reasoner = new Reasoner();
	//
	// public static void setKnowledgeBase(RuleSet base) {
	// reasoner.addRuleBase(base);
	// }
	//
	// public static
	// Hashtable<GenericOperationCall,List<GenericOperationCallInstantiation>>
	// getAllPossibleGenericComponentCallInstantiationsOfTemplate(Template
	// template, Set<Operation> components) {
	// Hashtable<GenericOperationCall,List<GenericOperationCallInstantiation>>
	// possibleComponentCallInstantiations = new
	// Hashtable<GenericOperationCall,List<GenericOperationCallInstantiation>>();
	// for (GenericOperationCall componentToReplace : template.getComponents())
	// {
	// List<GenericOperationCallInstantiation>
	// instantiationsForGenericComponentCall = new
	// LinkedList<GenericOperationCallInstantiation>();
	// for (Operation substitute : components) {
	// for (OperationCall componentCall :
	// getPossibleComponentCallsForComponent(componentToReplace, substitute)) {
	// instantiationsForGenericComponentCall.add(new
	// GenericOperationCallInstantiation(componentToReplace, componentCall));
	// }
	// }
	// possibleComponentCallInstantiations.put(componentToReplace,
	// instantiationsForGenericComponentCall);
	// }
	// return possibleComponentCallInstantiations;
	// }
	//
	// public static
	// Hashtable<GenericBooleanExpression,List<GenericBooleanExpressionInstantiation>>
	// getAllPossibleGenericBooleanExpressionInstantiationsOfTemplate(Template
	// template, Set<Literal> targetPredicates, Collection<ConstantParam>
	// constants) {
	// Hashtable<GenericBooleanExpression,List<GenericBooleanExpressionInstantiation>>
	// possibleBooleanExpressionInstantiations = new
	// Hashtable<GenericBooleanExpression,List<GenericBooleanExpressionInstantiation>>();
	// for (GenericBooleanExpression genericBooleanExpression :
	// template.getBooleanExpressions()) {
	// List<GenericBooleanExpressionInstantiation> possibleInstantiations = new
	// LinkedList<GenericBooleanExpressionInstantiation>();
	// for (Literal atom :
	// getPossibleMappingsForLiteral(genericBooleanExpression, targetPredicates,
	// constants, true, true, true)) {
	// possibleInstantiations.add(new
	// GenericBooleanExpressionInstantiation(genericBooleanExpression, atom));
	// }
	// possibleBooleanExpressionInstantiations.put(genericBooleanExpression,
	// possibleInstantiations);
	// }
	// return possibleBooleanExpressionInstantiations;
	// }
	//
	// public static
	// Hashtable<GenericAbstractLiteral,List<GenericAbstractLiteralInstantiation>>
	// getAllPossibleGenericAbstractLiteralInstantiationsOfTemplate(Template
	// template, Collection<ConstantParam> constants) {
	// Hashtable<GenericAbstractLiteral,List<GenericAbstractLiteralInstantiation>>
	// possibleBooleanExpressionInstantiations = new
	// Hashtable<GenericAbstractLiteral,List<GenericAbstractLiteralInstantiation>>();
	// for (GenericAbstractLiteral genericAbstractLiteral :
	// template.getHelperPredicates()) {
	// List<GenericAbstractLiteralInstantiation> possibleInstantiations = new
	// LinkedList<GenericAbstractLiteralInstantiation>();
	// Set<Predicate> targetPredicates =
	// LogicEnvironment.getInstance().getPredicates();
	// for (Literal atom :
	// InstantiationUtil.getPossibleMappingsForLiteral(genericAbstractLiteral,
	// targetPredicates, constants, false, false, false)) {
	// possibleInstantiations.add(new
	// GenericAbstractLiteralInstantiation(genericAbstractLiteral, atom));
	// }
	//
	// possibleBooleanExpressionInstantiations.put(genericAbstractLiteral,
	// possibleInstantiations);
	// }
	// return possibleBooleanExpressionInstantiations;
	// }
	//
	// public static boolean
	// isGenericAbstractLiteralInstantiationReasonable(Template template,
	// GenericAbstractLiteralInstantiation instantiation) {
	//
	// /* the instantiation is NOT reasonable if there is a consistency rule
	// that has this predicate on RHS but it cannot be derived from the LHS */
	// Predicate concreteP = instantiation.getLiteral().getPredicate();
	// Predicate abstractP = instantiation.getGenericLiteral().getPredicate();
	// int index =
	// template.getIndexOfPositiveConsistencyRuleThatHasPredicateOnRHS(abstractP.getBasicName());
	// if (index < 0)
	// return true;
	// LiteralSet precondition =
	// template.getPreconditionsOfPositiveConsistencyRules()[index];
	// for (Literal l : precondition) {
	// if (template.isPredicateUnbound(l.getPredicate()))
	// return true;
	// } // if the end of the loop is reached, the consistency rule LHS is
	// completely bound.
	// reasoner.addFactBase(precondition);
	// LiteralSet fb = reasoner.forwardChaining();
	// reasoner.rmFactBase(precondition);
	// for (Literal l : fb.getFacts()) {
	// if (l.getPredicate().equals(concreteP))
	// return true;
	// }
	//
	// /* otherwise, it does not make sense to use this */
	// return false;
	// }
	//
	// public static List<OperationCall>
	// getPossibleComponentCallsForComponent(GenericOperationCall
	// genericComponentCall, Operation component) {
	// String[] distinguishedInputs = genericComponentCall.getInputs();
	// String[] distinguishedOutputs = genericComponentCall.getOutputs();
	//
	// String[] inputsOfComponent = component.getIn();
	// String[] outputsOfComponent = component.getOut();
	//
	// /* make sure that we only insert component calls that have sufficient
	// inputs and outputs */
	// if (distinguishedInputs.length > inputsOfComponent.length ||
	// distinguishedOutputs.length > outputsOfComponent.length)
	// return new LinkedList<OperationCall>();
	//
	// /* determine all input/output combinations for the component call */
	// List<String> inputList =
	// getUnrolledInputsOfGenericComponentCallForConcreteComponent(genericComponentCall,
	// component);
	// List<List<String>> inputCombinations = new LinkedList<List<String>>();
	// for (List<String> inputSignature : setTool.getPermutations(inputList))
	// if (!inputCombinations.contains(inputSignature))
	// inputCombinations.add(inputSignature);
	// List<String> outputList =
	// getUnrolledOutputsOfGenericComponentCallForConcreteComponent(genericComponentCall,
	// component);
	// List<List<String>> outputCombinations = new LinkedList<List<String>>();
	// for (List<String> outputSignature : setTool.getPermutations(outputList))
	// if (!outputCombinations.contains(outputSignature))
	// outputCombinations.add(outputSignature);
	//
	// List<List<List<String>>> inputAndOutputCombos =
	// setToolForSignatures.getCartesianProduct(inputCombinations,
	// outputCombinations);
	//
	// /* create the subproblem for each instantiation */
	// List<OperationCall> suitableComponentCalls = new
	// LinkedList<OperationCall>();
	// for (List<List<String>> signature : inputAndOutputCombos) {
	//
	// /* create instantiation */
	// String[] inputsOfComponentCall = new String[inputsOfComponent.length];
	// String[] outputsOfComponentCall = new String[outputsOfComponent.length];
	// signature.get(0).toArray(inputsOfComponentCall);
	// signature.get(1).toArray(outputsOfComponentCall);
	// suitableComponentCalls.add(new OperationCall(component,
	// inputsOfComponentCall, outputsOfComponentCall));
	// }
	// return suitableComponentCalls;
	// }
	//
	// public static List<Literal> getPossibleMappingsForLiteral(Literal l,
	// Set<Literal> targetPredicates, Collection<ConstantParam> constants,
	// boolean skipPredicatesWithHigherArity, boolean injectiveArgumentMatching,
	// boolean onlyEvaluablePredicates) {
	// List<Literal> possibleMappings = new LinkedList<Literal>();
	// List<VariableParam> variables = new LinkedList<>();
	// for (VariableParam var : l.getVariableParams())
	// variables.add(var);
	// List<LiteralParam> possibleTargets = new LinkedList<>();
	// possibleTargets.addAll(variables);
	// possibleTargets.addAll(constants);
	//
	// int arityOfSourcePredicate = l.getParameters().size();
	// for (Literal targetPredicate : targetPredicates) {
	// if (targetPredicate.getProperty().startsWith("ABSTRACT_") ||
	// targetPredicate.getProperty().startsWith("PRE_") ||
	// targetPredicate.getProperty().startsWith("POST_"))
	// continue;
	// if (onlyEvaluablePredicates && !targetPredicate.isComputable())
	// continue;
	// int arityOfTargetPredicate = targetPredicate.getParameters().size();
	// if (skipPredicatesWithHigherArity && arityOfSourcePredicate <
	// arityOfTargetPredicate)
	// continue;
	//
	// /* if we have a bijective argument matching, find permutations, otherwise
	// the cartesian product */
	// if (injectiveArgumentMatching) {
	// for (List<String> subsetOfPossibleArgumentsWithAppropriateSize :
	// setTool.getAllPossibleSubsetsWithSize(possibleTargets,
	// l.getPredicate().getArity())) {
	//
	// /* if size is two and the predicate is symmetric, determine ONLY ONE OF
	// THE TWO mappings */
	// if (LogicEnvironment.getInstance().isPredicateSymmetric(targetPredicate))
	// {
	// String[] args = new
	// String[subsetOfPossibleArgumentsWithAppropriateSize.size()];
	// subsetOfPossibleArgumentsWithAppropriateSize.toArray(args);
	// try { possibleMappings.add(Literal.get(targetPredicate, args)); }
	// catch (Exception e ) { e.printStackTrace(); System.exit(1); }
	// }
	//
	// /* if the predicate is not symmetric, determine ALL mappings */
	// else {
	// for (List<String> arguments :
	// setTool.getPermutations(subsetOfPossibleArgumentsWithAppropriateSize)) {
	// String[] args = new String[arguments.size()];
	// arguments.toArray(args);
	// try { possibleMappings.add(Literal.get(targetPredicate, args)); }
	// catch (Exception e ) { e.printStackTrace(); System.exit(1); }
	// }
	// }
	// }
	// }
	// else {
	// for (List<String> arguments : setTool.getPotenceOfSet(variables,
	// arityOfTargetPredicate)) {
	// String[] args = new String[arguments.size()];
	// arguments.toArray(args);
	// try { possibleMappings.add(Literal.get(targetPredicate, args)); }
	// catch (Exception e ) { e.printStackTrace(); System.exit(1); }
	// }
	// }
	// }
	// return possibleMappings;
	// }
	//
	// static TemplateInstantiation
	// getTemplateInstantiationFromSolvedRestProblem(InstantiationRestProblem
	// irp) {
	// List<GenericOperationCallInstantiation> componentCalls = new
	// LinkedList<GenericOperationCallInstantiation>();
	// List<GenericBooleanExpressionInstantiation> booleanExpressions = new
	// LinkedList<GenericBooleanExpressionInstantiation>();
	// List<GenericAbstractLiteralInstantiation> helperPredicates = new
	// LinkedList<GenericAbstractLiteralInstantiation>();
	// InstantiationRestProblem p = irp;
	// while (p.getParent() != null) {
	// if (p.getPerformedComponentCallInstantiation() != null)
	// componentCalls.add(p.getPerformedComponentCallInstantiation());
	// else if (p.getPerformedLiteralInstantiation() != null)
	// booleanExpressions.add(p.getPerformedLiteralInstantiation());
	// else if (p.getPerformedHelperLiteralInstantiation() != null)
	// helperPredicates.add(p.getPerformedHelperLiteralInstantiation());
	// p = p.getParent();
	// }
	// return new TemplateInstantiation(p.getTemplate(), componentCalls,
	// booleanExpressions, helperPredicates);
	// }
	//
	// public static List<String>
	// getUnrolledInputsOfGenericComponentCallForConcreteComponent(GenericOperationCall
	// genericComponentCall, Operation component) {
	// String[] inputsOfComponent = component.getIn();
	// String[] distinguishedInputs = genericComponentCall.getInputs();
	// List<String> unrolledInputsOfGenericComponentCall = new
	// LinkedList<String>();
	// for (int i = 0; i < inputsOfComponent.length; i++)
	// unrolledInputsOfGenericComponentCall.add((i < distinguishedInputs.length)
	// ? distinguishedInputs[i] : (genericComponentCall.getVariableInputs() + (i
	// - distinguishedInputs.length)));
	// return unrolledInputsOfGenericComponentCall;
	// }
	//
	// public static List<String>
	// getUnrolledOutputsOfGenericComponentCallForConcreteComponent(GenericOperationCall
	// genericOperationCall, Operation operation) {
	// Set<VariableParam> outputsOfOperation = operation.getOut();
	// String[] distinguishedOutputs = genericComponentCall.getOutputs();
	// List<String> unrolledOutputsOfGenericComponentCall = new
	// LinkedList<String>();
	// for (int i = 0; i < outputsOfComponent.length; i++)
	// unrolledOutputsOfGenericComponentCall.add((i <
	// distinguishedOutputs.length) ? distinguishedOutputs[i] :
	// (genericComponentCall.getVariableOutputs() + (i -
	// distinguishedOutputs.length)));
	// return unrolledOutputsOfGenericComponentCall;
	// }
}
