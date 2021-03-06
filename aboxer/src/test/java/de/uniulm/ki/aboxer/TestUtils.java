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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestUtils {

	final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	final OWLDataFactory factory = manager.getOWLDataFactory();

	static <T> Set<T> asSet(T... elements) {
		return new HashSet<T>(Arrays.asList(elements));
	}

	static Set<IRI> getIRIs(Set<? extends OWLEntity> entities) {
		Set<IRI> result = new HashSet<IRI>();
		for (OWLEntity entity : entities) {
			result.add(entity.getIRI());
		}
		return result;
	}

	static Set<IRI> getIRIs(OWLEntity... entities) {
		return getIRIs(asSet(entities));
	}

	OWLClass getClass(String iri) {
		return factory.getOWLClass(IRI.create(iri));
	}

	OWLObjectProperty getObjectProperty(String iri) {
		return factory.getOWLObjectProperty(IRI.create(iri));
	}

}
