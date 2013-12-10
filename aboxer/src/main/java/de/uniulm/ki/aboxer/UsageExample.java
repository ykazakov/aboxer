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

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple usage example for the {@link Aboxer}
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public class UsageExample {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(UsageExample.class);

	public static void main(String[] args) throws OWLOntologyCreationException,
			OWLOntologyStorageException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// loading axioms from a file
		File inputOntologyFile = new File(
				"src/main/resources/full-galen.fss.owl");
		LOGGER_.info("Loading axioms from {} ...", inputOntologyFile);
		OWLOntology ontology = manager
				.loadOntologyFromOntologyDocument(inputOntologyFile);

		// converting axioms to assertions
		OWLOntology outputOntology = Aboxer.aboxify(ontology);

		// saving the output ontology to a file
		File outputOntologyFile = new File(inputOntologyFile.toString()
				+ ".aboxed");
		LOGGER_.info("Saving the output ontology to {} ...", outputOntologyFile);
		OWLOntologyFormat format = manager.getOntologyFormat(ontology);
		manager.saveOntology(outputOntology, format,
				IRI.create(outputOntologyFile.toURI()));

		LOGGER_.info("Done.");
	}

}
