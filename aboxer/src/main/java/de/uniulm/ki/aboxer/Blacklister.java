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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link OWLAxiomVisitor} that, while processing axioms, accumulates the
 * signature of (unconvertible parts of) axioms -- those (parts) that cannot be
 * converted to assertions -- in a black listed set. The resulting set does not
 * depend on the order in which the axioms are processed.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
class Blacklister extends AbstractAxiomVisitor implements OWLAxiomVisitor {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(Blacklister.class);

	/**
	 * A {@link Map} that describes which {@link OWLClass}es should be
	 * blacklisted provided that some (other) {@link OWLClass} become
	 * blacklisted; it will be constructed to avoid repeated processing of
	 * axioms. For example, if one first processes an axiom SubClassOf(:A
	 * ObjectSomeValuesFrom(:r :B)), which does not result in any blacklisted
	 * classes (it can be converted to an assertion), but later class :A becomes
	 * blacklisted (e.g., it occurs in unconvertible axiom), then :B should be
	 * blacklisted as well. For this purpose we will keep the dependency from :A
	 * to :B in a map :A -> {:B}.
	 */
	private final Map<OWLClass, Set<OWLClass>> blacklistDependencies_ = new HashMap<OWLClass, Set<OWLClass>>(
			128);

	/**
	 * An auxiliary queue used to buffer {@link OWLClass}es that should be
	 * glacklisted; this is to avoid potential stack overflow (unbounded
	 * recursion) when there is a long chain of blacklist dependencies
	 */
	private final Queue<OWLClass> toBlacklis_ = new LinkedList<OWLClass>();

	/**
	 * See {@link ThisPatternProcessor}
	 */
	private final ThisPatternProcessor patternProcessor_ = new ThisPatternProcessor();

	Blacklister() {
		super(new HashSet<OWLClass>());
	}

	@Override
	public void defaultVisit(OWLAxiom axiom) {
		// we blacklist all classes occurring in non-convertible axioms
		blacklist(axiom.getClassesInSignature());
	}

	@Override
	void visitClassDeclaration(OWLClass declaredClass) {
		// we blacklist nothing as those could be converted
	}

	@Override
	void visitClassInclusion(OWLClass subClass, OWLClassExpression superClass) {
		// blacklist the unconvertible parts of the super class
		for (OWLClassExpression conjunction : superClass.asConjunctSet()) {
			patternProcessor_.process(subClass, conjunction);
		}
	}

	/**
	 * Blacklists the given {@link OWLClass}es together with their dependencies
	 * 
	 * @param toBlackist
	 */
	void blacklist(Collection<OWLClass> toBlackist) {
		toBlacklis_.addAll(toBlackist);
		processToBlacklist();
	}

	/**
	 * repeatedly processing the pending {@link OWLClass}es to be blacklisted
	 * together with their dependencies until of them are processed
	 */
	private void processToBlacklist() {
		for (;;) {
			OWLClass next = toBlacklis_.poll();
			if (next == null)
				return;
			// else
			LOGGER_.trace("{} : blacklisted", next);
			blacklisted.add(next);
			Set<OWLClass> dependent = blacklistDependencies_.remove(next);
			if (dependent == null)
				continue;
			// else
			toBlacklis_.addAll(dependent);
		}
	}

	/**
	 * This processor is used when subsumption axioms between an
	 * {@link OWLClass} (not currently blacklisted) and an
	 * {@link OWLClassExpression} is processed, to determine which
	 * {@link OWLClass} occurring in this {@link OWLClassExpression} cannot be
	 * replaced with individuals, and thus, should be blacklisted. Those that
	 * can be replaced with individuals, are not guaranteed to be so after the
	 * processing, since the {@link OWLClass} in this axiom can be blacklisted
	 * later on. To take this into account, new dependencies for this
	 * {@link OWLClass} are created, so it is used as additional "context"
	 * parameter of the methods to pass it over.
	 * 
	 * @author "Yevgeny Kazakov"
	 * 
	 */
	class ThisPatternProcessor extends AbstractPatternProcessor<OWLClass> {

		@Override
		public void visitUnsplitable(OWLClass context, OWLClassExpression ce) {
			// blacklist everything in the class expression
			blacklist(ce.getClassesInSignature());
		}

		@Override
		public OWLClass getNewContext(OWLClass oldContext,
				OWLObjectPropertyExpression property) {
			// we pass over the oldContext parameter that is used in
			// dependencies
			return oldContext;
		}

		@Override
		public void visitSimpleExistential(OWLClass context,
				OWLObjectPropertyExpression property, OWLClass filler) {
			// we do not blacklist anything, but create a dependency to make
			// sure that the filler is blacklisted whenever the left hand side
			// of the subsumption is blacklisted
			Set<OWLClass> oldDependencies = blacklistDependencies_.get(context);
			if (oldDependencies == null) {
				HashSet<OWLClass> newDependencies = new HashSet<OWLClass>(4);
				newDependencies.add(filler);
				blacklistDependencies_.put(context, newDependencies);
			} else {
				oldDependencies.add(filler);
			}
		}

	}

}
