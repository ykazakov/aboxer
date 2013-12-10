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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class containing the methods for converting ontologies.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public class Aboxer {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory.getLogger(Aboxer.class);

	/**
	 * Converts axioms in the given ontology to assertions. The axioms are
	 * converted in two passes: the first pass computes the set of "blacklisted"
	 * {@link OWLClass}es that cannot be replaced with individuals; the second
	 * pass performs the replacement of the remaining {@link OWLClass}es with
	 * the corresponding individuals, and converting (parts of) axioms to
	 * assertions, possibly involving other anonymous individuals.
	 * 
	 * @param inputOntology
	 *            the ontology in which axioms should be (partially) replaced
	 *            with assertions
	 * @return the ontology that is obtained as the result of replacement; the
	 *         original ontology is not modified
	 * @throws OWLOntologyCreationException
	 *             if the resulting ontology cannot be created
	 */
	public static OWLOntology aboxify(OWLOntology inputOntology)
			throws OWLOntologyCreationException {
		LOGGER_.info("Computing blacklisted classes...");
		Blacklister blacklister = new Blacklister();
		// first pass over axioms
		for (OWLAxiom axiom : inputOntology.getAxioms()) {
			axiom.accept(blacklister);
		}
		Set<OWLClass> blacklisted = blacklister.getBlacklistedClasses();
		LOGGER_.debug("Blacklisted classes: {}", blacklisted.size());
		LOGGER_.info("Producing assertions...");
		OWLOntologyManager manager = inputOntology.getOWLOntologyManager();
		OntologyProducerProcessor ontologyProducer = new OntologyProducerProcessor(
				manager);
		AssertionCreator assertionCreator = new AssertionCreator(blacklisted,
				manager.getOWLDataFactory(), ontologyProducer);
		// second pass over axioms
		for (OWLAxiom axiom : inputOntology.getAxioms()) {
			axiom.accept(assertionCreator);
		}
		assertionCreator.printStatistics();
		return ontologyProducer.getOntology();
	}

}
