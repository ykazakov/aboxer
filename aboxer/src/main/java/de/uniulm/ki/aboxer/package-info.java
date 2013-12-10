/**
 * This is a utility package for converting (parts of) axioms in OWL ontologies
 * to assertions. This is done by detecting specific patterns in axioms that can
 * be converted to class assertions or object property assertions when replacing
 * classes with the corresponding individuals and introducing some auxiliary
 * (anonymous) individuals. For example, the axiom
 * 
 * SubClassAxiom(:A 
 * 	   ObjectIntersectionOf(:B 
 * 			ObjectSomeValuesFrom(:r ObjectSomeValuesFrom(:s C))))
 * 
 * can be converted to the following assertions when replacing concepts A and C
 * with individuals a and c:
 * 
 * ClassAssertion(:B :a) ObjectPropertyAssertion(:r :a _i)
 * ObjectPropertyAssertion(:s _i :C)
 * 
 * where _i is a new anonymous individuals.
 * 
 * The method {@link Aboxer#aboxify(OWLOntology)} implements conversion of
 * axioms to the assertion.
 * 
 * The class {@link UsageExample} contains a simple example of the conversion.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
package de.uniulm.ki.aboxer;

