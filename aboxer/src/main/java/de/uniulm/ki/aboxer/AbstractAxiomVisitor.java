package de.uniulm.ki.aboxer;

/*
 * #%L
 * TBox to ABox converter
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2013 Institute of Artificial Intelligence, University of Ulm
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

/**
 * A skeleton for building axiom processors; it filters out axioms that cannot
 * be converted to assertions, in particular, using a set of "blacklisted"
 * {@link OWLClass}es that cannot be replaced with the corresponding
 * individuals.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
abstract class AbstractAxiomVisitor implements OWLAxiomVisitor {

	/**
	 * The set of {@link OWLClass}es that cannot be replaced with individuals
	 */
	final Set<OWLClass> blacklisted;

	AbstractAxiomVisitor(Set<OWLClass> blacklisted) {
		this.blacklisted = blacklisted;
	}

	/**
	 * Processing axioms that cannot be converted to assertions
	 * 
	 * @param axiom
	 *            the axiom that cannot be converted to assertions
	 */
	public abstract void defaultVisit(OWLAxiom axiom);

	public Set<OWLClass> getBlacklistedClasses() {
		return this.blacklisted;
	}

	/**
	 * @param candidate
	 * @return {@code true} if the given {@link OWLClass} is blacklisted, and
	 *         {@code false} otherwise
	 */
	public boolean blacklisted(OWLClass candidate) {
		return this.blacklisted.contains(candidate);
	}

	/**
	 * Process a class declaration that can potentially be converted to an
	 * individual declaration.
	 * 
	 * @param declaredClass
	 *            the {@link OWLClass} in the declaration; it is not blacklisted
	 *            and hence can be potentially converted to an individual
	 */
	abstract void visitClassDeclaration(OWLClass declaredClass);

	/**
	 * Process a class inclusion axiom that can potentially be converted to a
	 * class assertion
	 * 
	 * @param subClass
	 *            an {@link OWLClass} which is the sub-class part of the axiom;
	 *            it is not blacklisted and hence can be potentially converted
	 *            to an individual
	 * @param superClass
	 */
	abstract void visitClassInclusion(OWLClass subClass,
			OWLClassExpression superClass);

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		OWLEntity declaredEntity = axiom.getEntity();
		if (!(declaredEntity instanceof OWLClass)) {
			defaultVisit(axiom);
			return;
		}
		// else
		OWLClass declaredClass = (OWLClass) declaredEntity;
		if (blacklisted.contains(declaredClass)) {
			defaultVisit(axiom);
			return;
		}
		// else
		visitClassDeclaration(declaredClass);
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClassExpression = axiom.getSubClass();
		if (!(subClassExpression instanceof OWLClass)) {
			defaultVisit(axiom);
			return;
		}
		// else
		OWLClass subClass = (OWLClass) subClassExpression;
		if (blacklisted.contains(subClass)) {
			defaultVisit(axiom);
			return;
		}
		// else
		visitClassInclusion((OWLClass) subClassExpression,
				axiom.getSuperClass());
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		defaultVisit(axiom);

	}

	@Override
	public void visit(SWRLRule rule) {
		defaultVisit(rule);

	}
}
