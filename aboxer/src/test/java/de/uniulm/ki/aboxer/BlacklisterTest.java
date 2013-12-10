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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class BlacklisterTest extends TestUtils {

	@Test
	public void testSubsumption() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(A, B).accept(blacklister);
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));

		factory.getOWLSubClassOfAxiom(B, A).accept(blacklister);
		assertTrue(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
	}

	@Test
	public void testSubsumptionDependencies() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(B, C).accept(blacklister);
		assertFalse(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));

		factory.getOWLSubClassOfAxiom(A, B).accept(blacklister);
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));

		factory.getOWLSubClassOfAxiom(C, A).accept(blacklister);
		assertTrue(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));
	}

	@Test
	public void testExistentialDependencies() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLClass D = getClass("D");

		OWLObjectProperty r = getObjectProperty("r");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectSomeValuesFrom(r, B)).accept(blacklister);

		// the axiom can be converted to object property assertion
		assertFalse(blacklister.blacklisted(A));
		assertFalse(blacklister.blacklisted(B));

		factory.getOWLSubClassOfAxiom(B,
				factory.getOWLObjectSomeValuesFrom(r, C)).accept(blacklister);

		// the axioms can still be converted to object property assertions
		assertFalse(blacklister.blacklisted(A));
		assertFalse(blacklister.blacklisted(B));
		assertFalse(blacklister.blacklisted(C));

		factory.getOWLSubClassOfAxiom(C,
				factory.getOWLObjectSomeValuesFrom(r, A)).accept(blacklister);

		// still can be converted to assertions
		assertFalse(blacklister.blacklisted(A));
		assertFalse(blacklister.blacklisted(B));
		assertFalse(blacklister.blacklisted(C));

		factory.getOWLSubClassOfAxiom(D, A).accept(blacklister);

		// now A cannot be converted to individual and hence all other concepts
		// reachable from A
		assertTrue(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));

	}

	@Test
	public void testExistentialEquivalence() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");

		OWLObjectProperty r = getObjectProperty("r");

		Blacklister blacklister = new Blacklister();

		factory.getOWLEquivalentClassesAxiom(A,
				factory.getOWLObjectSomeValuesFrom(r, B)).accept(blacklister);
		// equivalent to two inclusion axioms, hence both A and B are
		// blacklisted
		assertTrue(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));

	}

	@Test
	public void testUniversalDependencies() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");

		OWLObjectProperty r = getObjectProperty("r");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectAllValuesFrom(r, B)).accept(blacklister);
		// fillers of universal restrictions cannot be converted to individuals
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));

	}

	@Test
	public void testConjunctions() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(A,
				factory.getOWLObjectIntersectionOf(B, C)).accept(blacklister);

		// can be converted to assertions B(a) and C(a)
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));

	}

	@Test
	public void testConjunctionsExistentials() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectIntersectionOf(B,
						factory.getOWLObjectSomeValuesFrom(r, C))).accept(
				blacklister);

		// can be converted to assertions B(a) and r(a, c)
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertFalse(blacklister.blacklisted(C));

	}

	@Test
	public void testExistentialConjunctions() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectSomeValuesFrom(r,
						factory.getOWLObjectIntersectionOf(B, C))).accept(
				blacklister);

		// can be converted to r(a,_i), B(_i), C(_i), where
		// _i is an anonymous individual
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));
	}

	@Test
	public void testNestedExistentialConjunctions() {
		OWLClass A = getClass("A");
		OWLClass B = getClass("B");
		OWLClass C = getClass("C");
		OWLObjectProperty r = getObjectProperty("r");

		Blacklister blacklister = new Blacklister();

		factory.getOWLSubClassOfAxiom(
				A,
				factory.getOWLObjectSomeValuesFrom(
						r,
						factory.getOWLObjectIntersectionOf(B,
								factory.getOWLObjectSomeValuesFrom(r, C))))
				.accept(blacklister);

		// can be converted to r(a,_i), B(_i), r(_i, c), where
		// _i is an anonymous individual
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertFalse(blacklister.blacklisted(C));

		factory.getOWLSubClassOfAxiom(
				C,
				factory.getOWLObjectSomeValuesFrom(
						r,
						factory.getOWLObjectIntersectionOf(B,
								factory.getOWLObjectSomeValuesFrom(r, A))))
				.accept(blacklister);

		// can be converted to r(c,_j), B(_j), r(_j, A), where
		// _j is a (different) anonymous individual
		assertFalse(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertFalse(blacklister.blacklisted(C));

		factory.getOWLSubClassOfAxiom(
				C,
				factory.getOWLObjectSomeValuesFrom(
						r,
						factory.getOWLObjectIntersectionOf(A,
								factory.getOWLObjectSomeValuesFrom(r, B))))
				.accept(blacklister);

		// now A becomes blacklisted, and hence C should be blacklisted as well
		assertTrue(blacklister.blacklisted(A));
		assertTrue(blacklister.blacklisted(B));
		assertTrue(blacklister.blacklisted(C));

	}

}
