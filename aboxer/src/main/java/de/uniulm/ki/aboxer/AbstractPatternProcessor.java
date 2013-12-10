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

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A high level specification of the processor for parts of axioms that can be
 * converted to assertions. Currently, we can convert to assertions axioms of
 * the form SubClassOf(:A :C), where :A is an atomic class (which can be
 * replaced with individuals). If :C is a conjunction, containing existential
 * restrictions, then those can be additionally converted to role assertions
 * (possibly involving anonymous individuals). For example, if C is a
 * conjunction of concepts B and ObjectSomeValuesFrom(r D), then the axiom can
 * be replaced with assertions B(a) and r(a, d) (if D is an atomic concept), or
 * with r(a, _i), D(_i) (if D is not an atomic concept, _i is an fresh anonymous
 * individual). This class allows to process the corresponding parts of class
 * expressions -- for which new individual assertions can be created -- using a
 * generic parameter C (called "context") that can be used to pass additional
 * objects, e.g., the replaced individual.
 * 
 * @author "Yevgeny Kazakov"
 * 
 * @param <C>
 *            the type of the additional parameter that can be used for
 *            processing
 */
abstract class AbstractPatternProcessor<C> {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(AbstractPatternProcessor.class);

	/**
	 * Process the {@link OWLClassExpression} that cannot be (partially or
	 * fully) converted to assertions; thus it should be copied over to the
	 * resulting ontology
	 * 
	 * @param context
	 * @param ce
	 */
	public abstract void visitUnsplitable(C context, OWLClassExpression ce);

	/**
	 * Process an existential restriction defined by the given
	 * {@link OWLObjectPropertyExpression} and the given {@link OWLClass}
	 * filler; it can potentially converted to an object property assertion by
	 * replacing the filler with the corresponding individual.
	 * 
	 * @param context
	 * @param property
	 * @param filler
	 */
	public abstract void visitSimpleExistential(C context,
			OWLObjectPropertyExpression property, OWLClass filler);

	/**
	 * Creates a new context by following an existential restriction with a
	 * complex filler that can be recursively processed further on using the new
	 * context. For example, if we have an axiom SubClassOf(:A
	 * ObjectSomeValuesFrom(:r ObjectSomeValuesFrom(:s :C))), then it can be
	 * recursively converted to object property assertions r(a, _i1), r(_i1,
	 * _i2), C(_i2). This method thus, e.g., can be used to create an
	 * (anonymous) individual _i2 from the individual _i1 knowing that it was
	 * constructed by following the object property r.
	 * 
	 * @param oldContext
	 * @param property
	 * @return
	 */
	public abstract C getNewContext(C oldContext,
			OWLObjectPropertyExpression property);

	/**
	 * Recursively processes the given {@link OWLClassExpression} for which an
	 * assertion can be created to determine on which further assertions can be
	 * produced. For example, if the {@link OWLClassExpression} is a
	 * conjunction, one can produce assertions separately for the conjuncts; if
	 * any of these conjuncts are existential restrictions, then role assertions
	 * (with named or anonymous individuals) can be created.
	 * 
	 * @param context
	 * @param ce
	 */
	public void process(C context, OWLClassExpression ce) {
		if (!(ce instanceof OWLObjectSomeValuesFrom)) {
			LOGGER_.trace(" {} : unconvertable", ce);
			visitUnsplitable(context, ce);
			return;
		}
		OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) ce;
		OWLObjectPropertyExpression property = restriction.getProperty();
		OWLClassExpression filler = restriction.getFiller();
		if (filler instanceof OWLClass) {
			visitSimpleExistential(context, property, (OWLClass) filler);
			return;
		}
		// else
		C newContext = getNewContext(context, property);
		for (OWLClassExpression conjunct : filler.asConjunctSet()) {
			process(newContext, conjunct);
		}
	}

}
