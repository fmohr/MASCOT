package de.upb.crc901.mascot.structure;

import java.util.HashSet;
import java.util.Set;

import jaicore.logic.fol.structure.VariableParam;

public class GenericOperationCall {

	private String name;
	private Set<VariableParam> inputs, outputs;

	public GenericOperationCall(String name, Set<VariableParam> inputs, Set<VariableParam> outputs) {
		super();
		this.name = name;
		this.inputs = inputs;
		this.outputs = outputs;
	}

	public String getName() {
		return name;
	}

	public Set<VariableParam> getInputs() {
		return inputs;
	}

	public Set<VariableParam> getOutputs() {
		return outputs;
	}

	public GenericLiteral getPreconditions() {

		/* determine preconditions and effects of generic com */
		String inputVarsAsCommaSeparatedString = "";
		for (VariableParam input : this.inputs)
			inputVarsAsCommaSeparatedString += (inputVarsAsCommaSeparatedString.length() > 0 ? ", " : "")
					+ input.getName();
		GenericLiteral l = null;
		try {
			l = GenericLiteral.get("PRE_" + this.name + "(" + inputVarsAsCommaSeparatedString + ")",
					new HashSet<String>());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return l;
	}

	public GenericLiteral getEffects() {

		/* determine preconditions and effects of generic com */
		String varsAsCommaSeparatedString = "";
		for (VariableParam input : inputs)
			varsAsCommaSeparatedString += (varsAsCommaSeparatedString.length() > 0 ? ", " : "") + input.getName();
		for (VariableParam output : outputs)
			varsAsCommaSeparatedString += (varsAsCommaSeparatedString.length() > 0 ? ", " : "") + output.getName();
		GenericLiteral l = null;
		try {
			l = GenericLiteral.get("POST_" + this.name + "(" + varsAsCommaSeparatedString + ")", new HashSet<>());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return l;
	}

	@Override
	public String toString() {
		return "GenericComponentCall [name=" + name + ", inputs=" + inputs + ", outputs=" + outputs + "]";
	}

	public String getCodeRepresentation() {
		return "(" + outputs + ") := " + this.name + "(" + inputs + ")";
	}
}
