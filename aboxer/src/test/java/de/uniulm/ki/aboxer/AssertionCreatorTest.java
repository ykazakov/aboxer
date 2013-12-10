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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

public class AssertionCreatorTest extends TestUtils {

	@Test
	public void testSubsumptionB() {

		OWLClass A = getClass("A");
		OWLClass B = getClass("B");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B);

		factory.getOWLSubClassOfAxiom(A, B).accept(creator);
		// assertion B(a) should be created
		assertTrue(processor.getClassesInSignature().equals(asSet(B)));
		assertTrue(getIRIs(processor.getIndividualsInSignature()).equals(
				getIRIs(asSet(A))));
		assertEquals(1, processor.getClassAssertionAxioms().size());
		assertEquals(0, processor.getObjectPropertyAssertionAxioms().size());

	}

	@Test
	public void testExistential() {

		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor);

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectSomeValuesFrom(r, B)).accept(creator);
		// property assertion r(a,b) should be created
		assertEquals(0, processor.getClassesInSignature().size());
		assertTrue(getIRIs(processor.getIndividualsInSignature()).equals(
				getIRIs(asSet(A, B))));
		assertEquals(0, processor.getClassAssertionAxioms().size());
		assertEquals(1, processor.getObjectPropertyAssertionAxioms().size());

	}

	@Test
	public void testExistentialB() {

		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B);

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectSomeValuesFrom(r, B)).accept(creator);
		// class assertion Some(r B)(a) should be created
		assertEquals(processor.getClassesInSignature(), asSet(B));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A)));
		assertEquals(1, processor.getClassAssertionAxioms().size());
		assertEquals(0, processor.getObjectPropertyAssertionAxioms().size());

	}

	@Test
	public void testExistentialAB() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, A, B);

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectSomeValuesFrom(r, B)).accept(creator);
		// no assertion is produced
		assertEquals(processor.getClassesInSignature(), asSet(A, B));
		assertEquals(0, getIRIs(processor.getIndividualsInSignature()).size());
		assertEquals(0, processor.getClassAssertionAxioms().size());
		assertEquals(0, processor.getObjectPropertyAssertionAxioms().size());

	}

	@Test
	public void testConjunctionsBC() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B, C);

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectIntersectionOf(B, C)).accept(creator);
		// assertions B(a) and C(a) produced
		assertEquals(processor.getClassesInSignature(), asSet(B, C));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A)));
		assertEquals(2, processor.getClassAssertionAxioms().size());
		assertEquals(0, processor.getObjectPropertyAssertionAxioms().size());

	}

	@Test
	public void testNestedConjunctionsBCD() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLClass D = getClass("D");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B, C);

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectIntersectionOf(B,
						factory.getOWLObjectIntersectionOf(C, D))).accept(
				creator);
		// assertions B(a), C(a), and D(a) produced
		assertEquals(processor.getClassesInSignature(), asSet(B, C, D));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A)));
		assertEquals(3, processor.getClassAssertionAxioms().size());
		assertEquals(0, processor.getObjectPropertyAssertionAxioms().size());

	}

	@Test
	public void testConjunctionsExistentialsB() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B);

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectIntersectionOf(B,
						factory.getOWLObjectSomeValuesFrom(r, C))).accept(
				creator);

		// assertions B(a), and r(a, c) created
		assertEquals(processor.getClassesInSignature(), asSet(B));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A, C)));
		assertEquals(1, processor.getClassAssertionAxioms().size());
		assertEquals(1, processor.getObjectPropertyAssertionAxioms().size());
	}

	@Test
	public void testConjunctionsExistentialsBC() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B, C);

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectIntersectionOf(B,
						factory.getOWLObjectSomeValuesFrom(r, C))).accept(
				creator);

		// assertions B(a), and Some(r C)(a) created
		assertEquals(processor.getClassesInSignature(), asSet(B, C));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A)));
		assertEquals(2, processor.getClassAssertionAxioms().size());
		assertEquals(0, processor.getObjectPropertyAssertionAxioms().size());
	}

	@Test
	public void testExistentialConjunctionsBC() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B, C);

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectSomeValuesFrom(r,
						factory.getOWLObjectIntersectionOf(B, C))).accept(
				creator);

		// assertions r(a,_i), B(_i), C(_i), where _i is an anonymous
		// individual, created
		assertEquals(processor.getClassesInSignature(), asSet(B, C));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A)));
		assertEquals(2, processor.getClassAssertionAxioms().size());
		assertEquals(1, processor.getObjectPropertyAssertionAxioms().size());
	}

	@Test
	public void testNestedExistentialConjunctionsB() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B);

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectSomeValuesFrom(
						r,
						factory.getOWLObjectIntersectionOf(B,
								factory.getOWLObjectSomeValuesFrom(r, C))))
				.accept(creator);

		// assertions r(a,_i), B(_i), r(_i, c), where _i is an anonymous
		// individual
		assertEquals(processor.getClassesInSignature(), asSet(B));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A, C)));
		assertEquals(1, processor.getClassAssertionAxioms().size());
		assertEquals(2, processor.getObjectPropertyAssertionAxioms().size());
	}

	@Test
	public void testNestedExistentialConjunctionsBC() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Processor processor = new Processor();
		AssertionCreator creator = getAssertionCreator(processor, B, C);

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectSomeValuesFrom(
						r,
						factory.getOWLObjectIntersectionOf(B,
								factory.getOWLObjectSomeValuesFrom(r, C))))
				.accept(creator);

		// assertions r(a,_i), B(_i), Some(r C)(_i), where _i is an anonymous
		// individual
		assertEquals(processor.getClassesInSignature(), asSet(B, C));
		assertEquals(getIRIs(processor.getIndividualsInSignature()),
				getIRIs(asSet(A)));
		assertEquals(2, processor.getClassAssertionAxioms().size());
		assertEquals(1, processor.getObjectPropertyAssertionAxioms().size());
	}

	AssertionCreator getAssertionCreator(OWLAxiomProcessor processor,
			OWLClass... blacklisted) {
		return new AssertionCreator(asSet(blacklisted), factory, processor);
	}

	class Processor implements OWLAxiomProcessor {

		private final List<OWLAxiom> axioms_ = new LinkedList<OWLAxiom>();

		@Override
		public void process(OWLAxiom axiom) {
			axioms_.add(axiom);
		}

		public List<OWLAxiom> getAxioms() {
			return this.axioms_;
		}

		public Set<OWLClassAssertionAxiom> getClassAssertionAxioms() {
			Set<OWLClassAssertionAxiom> result = new HashSet<OWLClassAssertionAxiom>(
					16);
			for (OWLAxiom axiom : axioms_) {
				if (axiom instanceof OWLClassAssertionAxiom)
					result.add((OWLClassAssertionAxiom) axiom);
			}
			return result;
		}

		public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms() {
			Set<OWLObjectPropertyAssertionAxiom> result = new HashSet<OWLObjectPropertyAssertionAxiom>(
					16);
			for (OWLAxiom axiom : axioms_) {
				if (axiom instanceof OWLObjectPropertyAssertionAxiom)
					result.add((OWLObjectPropertyAssertionAxiom) axiom);
			}
			return result;
		}

		public Set<OWLClass> getClassesInSignature() {
			Set<OWLClass> result = new HashSet<OWLClass>(16);
			for (OWLAxiom axiom : axioms_) {
				result.addAll(axiom.getClassesInSignature());
			}
			return result;
		}

		public Set<OWLNamedIndividual> getIndividualsInSignature() {
			Set<OWLNamedIndividual> result = new HashSet<OWLNamedIndividual>(16);
			for (OWLAxiom axiom : axioms_) {
				result.addAll(axiom.getIndividualsInSignature());
			}
			return result;
		}

	}

}
