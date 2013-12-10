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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * 
 * An {@link OWLAxiomProcessor} that creates a new ontology from the axioms
 * given for processor.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
class OntologyProducerProcessor implements OWLAxiomProcessor {

	/**
	 * The manager that is used to operate the ontology
	 */
	private final OWLOntologyManager manager_;

	/**
	 * The ontology that will be produced
	 */
	private final OWLOntology ontology_;

	public OntologyProducerProcessor(OWLOntologyManager manager)
			throws OWLOntologyCreationException {
		this.manager_ = manager;
		this.ontology_ = manager_.createOntology();
	}

	/**
	 * @return the ontology containing all {@link OWLAxiom}s processed by this
	 *         {@link OWLAxiomProcessor}
	 */
	public OWLOntology getOntology() {
		return ontology_;
	}

	@Override
	public void process(OWLAxiom axiom) {
		manager_.addAxiom(ontology_, axiom);
	}

}
