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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@code AssertionableAxiomFilter} that converts sub-class axioms to the
 * corresponding {@link OWLClassAssertionAxiom} or
 * {@link OWLObjectPropertyAssertionAxiom} axioms if their corresponding
 * {@link OWLClass}es are not contained in a given set of black listed classes.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
class AssertionCreator extends AbstractAxiomVisitor {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(AssertionCreator.class);

	/**
	 * The factory used to create the new axioms
	 */
	private final OWLDataFactory factory_;

	/**
	 * The processor through which the axioms are returned
	 */
	private final OWLAxiomProcessor processor_;

	/**
	 * See {@link ThisPatternProcessor}
	 */
	private final ThisPatternProcessor patternProcessor_ = new ThisPatternProcessor();

	// counters for statistics
	private int countNewIndividuals_ = 0;
	private int countAnonymousIndividuals_ = 0;
	private int countNewClassAssertions_ = 0;
	private int countNewObjectPropertyAssertions_ = 0;

	/**
	 * @param factory
	 *            The factory used to create the new axioms
	 * @param processor
	 *            The processor through which the axioms are returned
	 */
	public AssertionCreator(Set<OWLClass> blackListedClasses,
			OWLDataFactory factory, OWLAxiomProcessor processor) {
		super(blackListedClasses);
		this.factory_ = factory;
		this.processor_ = processor;
	}

	OWLNamedIndividual individualOfClass(OWLClass owlClass) {
		return factory_.getOWLNamedIndividual(owlClass.getIRI());
	}

	@Override
	public void defaultVisit(OWLAxiom axiom) {
		processor_.process(axiom);
	}

	@Override
	void visitClassDeclaration(OWLClass declaredClass) {
		// TODO: no declarations for individuals in OWL??
		OWLAxiom axiom = factory_.getOWLDeclarationAxiom(factory_
				.getOWLNamedIndividual(declaredClass.getIRI()));
		countNewIndividuals_++;
		LOGGER_.trace("{}: created", axiom);
		processor_.process(axiom);
	}

	@Override
	void visitClassInclusion(OWLClass subClass, OWLClassExpression superClass) {
		OWLNamedIndividual individual = getIndividual(subClass);
		for (OWLClassExpression conjunction : superClass.asConjunctSet())
			patternProcessor_.process(individual, conjunction);
	}

	OWLNamedIndividual getIndividual(OWLClass convertable) {
		return factory_.getOWLNamedIndividual(convertable.getIRI());
	}

	OWLAxiomProcessor getProcessor() {
		return this.processor_;
	}

	void printStatistics() {
		LOGGER_.debug("new individual declarations: {}", countNewIndividuals_);
		LOGGER_.debug("new anonymous individuals: {}",
				countAnonymousIndividuals_);
		LOGGER_.debug("new class assertions: {}", countNewClassAssertions_);
		LOGGER_.debug("new object property assertions: {}",
				countNewObjectPropertyAssertions_);
	}

	/**
	 * The processor describing how to create assertions for concepts for which
	 * instances have been found.
	 * 
	 * @author "Yevgeny Kazakov"
	 * 
	 */
	class ThisPatternProcessor extends AbstractPatternProcessor<OWLIndividual> {

		@Override
		public void visitUnsplitable(OWLIndividual context,
				OWLClassExpression ce) {
			// create a class assertion
			OWLAxiom axiom = factory_.getOWLClassAssertionAxiom(ce, context);
			countNewClassAssertions_++;
			LOGGER_.trace("{}: created", axiom);
			processor_.process(axiom);

		}

		@Override
		public void visitSimpleExistential(OWLIndividual context,
				OWLObjectPropertyExpression property, OWLClass filler) {
			OWLAxiom axiom;
			if (!blacklisted.contains(filler)) {
				// if the filler can be converted to individual we create
				// a property assertion
				axiom = factory_.getOWLObjectPropertyAssertionAxiom(property,
						context, getIndividual(filler));
				countNewObjectPropertyAssertions_++;
			} else {
				// if not, we create a class assertion
				axiom = factory_.getOWLClassAssertionAxiom(
						factory_.getOWLObjectSomeValuesFrom(property, filler),
						context);
				countNewClassAssertions_++;
			}
			LOGGER_.trace("{}: created", axiom);
			processor_.process(axiom);
		}

		@Override
		public OWLIndividual getNewContext(OWLIndividual oldContext,
				OWLObjectPropertyExpression property) {
			// create a fresh anonymous individual
			OWLIndividual newContext = factory_.getOWLAnonymousIndividual();
			countAnonymousIndividuals_++;
			// create an object property assertion connecting it with the old
			// individual
			OWLAxiom axiom = factory_.getOWLObjectPropertyAssertionAxiom(
					property, oldContext, newContext);
			countNewObjectPropertyAssertions_++;
			LOGGER_.trace("{}: created", axiom);
			processor_.process(axiom);
			return newContext;
		}

	}

}
