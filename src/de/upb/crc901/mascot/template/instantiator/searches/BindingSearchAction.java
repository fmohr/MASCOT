package de.upb.crc901.mascot.template.instantiator.searches;

public class BindingSearchAction {
	private String abstractPredicate;
	private String concretePredicate;
	private int argument;

	public BindingSearchAction(String abstractPredicate, String concretePredicate, int argument) {
		super();
		this.abstractPredicate = abstractPredicate;
		this.concretePredicate = concretePredicate;
		this.argument = argument;
	}

	public String getAbstractPredicate() {
		return abstractPredicate;
	}

	public String getConcretePredicate() {
		return concretePredicate;
	}

	public int getArgument() {
		return argument;
	}

	@Override
	public String toString() {
		return "BindingSearchAction [abstractPredicate=" + abstractPredicate + ", concretePredicate="
				+ concretePredicate + ", argument=" + argument + "]";
	}
}
