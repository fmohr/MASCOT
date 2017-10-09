package de.upb.crc901.mascot.template.completeInstantiator;

public class CompleteInstantiator {

	// private static Logger logger = Logger.getLogger("Instantiator");
	// private InstantiationEnvironment environment;
	//
	// public CompleteInstantiator(InstantiationEnvironment environment) {
	// super();
	// this.environment = environment;
	// }
	//
	// public List<TemplateInstantiation> instantiate() {
	// int instantiations = 0;
	// int successfullInstantiations = 0;
	// List<TemplateInstantiation> solutions = new
	// LinkedList<TemplateInstantiation>();
	//
	// for (Template template : this.environment.getTemplates()) {
	//
	// /* determine possible instantiations for template */
	// Hashtable<GenericOperationCall,List<GenericOperationCallInstantiation>>
	// possibleComponentCallInstantiations =
	// InstantiationUtil.getAllPossibleGenericComponentCallInstantiationsOfTemplate(template,
	// this.environment.getComponents());
	// Hashtable<GenericBooleanExpression,List<GenericBooleanExpressionInstantiation>>
	// possibleBooleanExpressionInstantiations =
	// InstantiationUtil.getAllPossibleGenericBooleanExpressionInstantiationsOfTemplate(template,
	// this.environment.getConstantParameters());
	// Hashtable<GenericAbstractLiteral,List<GenericAbstractLiteralInstantiation>>
	// possibleAbstractLiteralInstantiations =
	// InstantiationUtil.getAllPossibleGenericAbstractLiteralInstantiationsOfTemplate(template,
	// this.environment.getConstantParameters());
	// System.out.println("Computation of local instantiations complete.");
	// int pot = 1;
	// for (List<GenericOperationCallInstantiation> l :
	// possibleComponentCallInstantiations.values())
	// pot *= l.size();
	// for (List<GenericBooleanExpressionInstantiation> l :
	// possibleBooleanExpressionInstantiations.values())
	// pot *= l.size();
	// for (List<GenericAbstractLiteralInstantiation> l :
	// possibleAbstractLiteralInstantiations.values())
	// pot *= l.size();
	// System.out.println("Number of possible instantiations: " + pot);
	//
	// /* create open list */
	// Queue<InstantiationRestProblem> open = new
	// LinkedList<InstantiationRestProblem>();
	// open.add(new InstantiationRestProblem(template));
	// System.out.println(template);
	// int i = 0;
	//
	// /* start instantiation */
	// for (InstantiationRestProblem problem; (problem = open.poll()) != null;)
	// {
	// i++;
	// instantiations++;
	//
	// //System.out.println("implied rules : " + problem.getImpliedRules());
	//
	// /* determine the (reduced) consistency rules of the reduced template */
	// Template reducedTemplate = problem.getTemplate();
	//
	// /* check if template violates a negative constraint, has a contradictory
	// precondition of one consistency rule */
	// if (problem.hasSatisfiedNegativeConsistencyRules()) {
	// // System.out.println("Skipping problem " + problem.getId() + " that has
	// a satisfied negative consistency rule!");
	// continue;
	// }
	// if (problem.hasUnsatisfiablePositiveConsistencyRule()) {
	// //System.out.println("Contains unsolvable consisteny rule: " + problem);
	// continue;
	// }
	// if (problem.hasContradictoryPreconditionInPositiveConsistencyRule()) {
	// // System.out.println("Skipping problem " + problem.getId() + " that has
	// a contradictory precondition in a positive consistency rule!");
	// continue;
	// }
	//
	// /* check if template is a partial solution */
	// if (!problem.hasUnsatisfiedPositiveConsistencyRules()) {
	// solutions.add(InstantiationUtil.getTemplateInstantiationFromSolvedRestProblem(problem));
	// successfullInstantiations ++;
	// continue;
	// }
	//
	// /* now replace the next generic component call */
	// if (reducedTemplate.getComponents().size() > 0) {
	// GenericOperationCall componentToReplace =
	// reducedTemplate.getComponents().get(0);
	// for (GenericOperationCallInstantiation instantiation :
	// possibleComponentCallInstantiations.get(componentToReplace))
	// open.add(problem.calculateRestProblemForComponentInstantiation(instantiation,
	// this.environment.getKnowledgeBase()));
	// }
	//
	// /* replace next boolean expression */
	// else if (reducedTemplate.getBooleanExpressions().size() > 0) {
	// GenericBooleanExpression beToReplace =
	// reducedTemplate.getBooleanExpressions().get(0);
	// for (GenericBooleanExpressionInstantiation instantiation :
	// possibleBooleanExpressionInstantiations.get(beToReplace))
	// open.add(problem.calculateRestProblemForBooleanExpressionInstantiation(instantiation,
	// this.environment.getKnowledgeBase()));
	// }
	//
	// /* now replace helper predicates */
	// else if (reducedTemplate.getHelperPredicates().size() > 0) {
	// GenericAbstractLiteral literalToReplace =
	// reducedTemplate.getHelperPredicates().get(0);
	// for (GenericAbstractLiteralInstantiation instantiation :
	// possibleAbstractLiteralInstantiations.get(literalToReplace))
	// if
	// (InstantiationUtil.isGenericAbstractLiteralInstantiationReasonable(reducedTemplate,
	// instantiation))
	// open.add(problem.calculateRestProblemForHelperPredicateInstantiation(instantiation,
	// this.environment.getKnowledgeBase()));
	// }
	//
	// /* if nothing more can be done */
	// else {
	//
	// }
	// }
	// System.out.println(i);
	// }
	// logger.info(successfullInstantiations + "/" + instantiations + " success
	// rate of instantiations");
	// return solutions;
	// }
}