package de.upb.crc901.mascot.template.instantiation;

import java.util.List;
import java.util.Set;

import de.upb.crc901.configurationsetting.operation.Operation;
import de.upb.crc901.mascot.structure.Template;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.HornFormula;
import jaicore.logic.fol.structure.TypeModule;

public class InstantiationEnvironment {

	private TypeModule typeModule;
	private List<Template> templates;
	private Set<Operation> components;
	private HornFormula knowledgeBase;

	public InstantiationEnvironment(List<Template> templates, Set<Operation> components, HornFormula knowledgeBase, TypeModule typeModule) {
		super();
		this.templates = templates;
		this.components = components;
		this.knowledgeBase = knowledgeBase;
		this.typeModule = typeModule;
	}

	public List<Template> getTemplates() {
		return templates;
	}

	public Set<Operation> getComponents() {
		return components;
	}

	public HornFormula getKnowledgeBase() {
		return knowledgeBase;
	}

	public Set<ConstantParam> getConstantParameters() {
		return this.knowledgeBase.getConstantParams();
	}
	
	public TypeModule getTypeModule() {
		return typeModule;
	}
}