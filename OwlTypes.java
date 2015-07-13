//package pace;

//package edu.pace.semweb;

import java.util.*;

class Ontology {
	String about;       // url for this ontology
	String comment;     // comments for this ontology
	String label;       // title of the ontology
	String fileName;    // name of the ontology file
	String filePath;    // path to the ontology file
	String versionInfo; // ontology version
	Hashtable<String, String> nsPrefixHash = new Hashtable<String, String>(); // nsPrefix -> url
  Hashtable<String, OwlClass> owlClassHash = new Hashtable<String, OwlClass>(); // full name -> class object
  Hashtable<String, PropertyClass> propertyHash = new Hashtable<String, PropertyClass>(); // name -> property object
  Hashtable<String, DataType> dataTypeHash = new Hashtable<String, DataType>();
  Hashtable<String, Relation> newRelationHash = new Hashtable<String, Relation>();
  String base;        // the base URL for the ontology
  Boolean allDifferent; // no shared objects among the classes in the ontology; default to be false
  Hashtable<String, Vector<OwlClass>> learningOrderHash = new Hashtable<String, Vector<OwlClass>>();
}

class OwlClass {      // Each object represents an OWL class
	String id;          // class tag name (no space)
	String name;        // extended natural name allowing spaces
	String namespace;   // class namespace
	String about;
  Ontology ontology;  // the ontology object containing this class	
	Vector<OwlClass> subClassOf = new Vector<OwlClass>();       // list of super classes
  Vector<OwlClass> superClassOf = new Vector<OwlClass>();     // list of subclasses
	Vector<Object> equivalentClass = new Vector<Object>();      // list of equivalent classes
	Vector<OwlClass> disjointWith = new Vector<OwlClass>();     // list of disjoint classes
	Vector<PropertyClass> properties = new Vector<PropertyClass>(); // list of properties for this class
	Vector<PropertyRestriction> propertyRestrictions = new Vector<PropertyRestriction>(); // list of restrictions on the properties
	Map<Relation,List<OwlClass>> relationsMap = new HashMap<Relation,List<OwlClass>>();
  String resource;
}

class Implementor {
  Vector<OwlClass> impl = new Vector<OwlClass>(); // all classes that together implement
}

class PropertyRestriction { // Each instance represents an restriction on a property
	String basePropertyName;
	PropertyClass baseProperty;
	PropertyResctrictionType type; 
	String value;        // hasValue
	String valueType;    // hasValue
	OwlClass valueClass; // someValuesFrom
}

class PropertyClass {  // Each object represents an OWL property class
	String id;           // name of the property
  String namespace;    // property namespace
	Ontology ontology;   // the ontology object containing this property	
	PropertyType propertyType; // DatatypeProperty or ObjectProperty
	OwlClass domain;     // Property domain
  Vector<String> type = new Vector<String>(); // list of properties like transitive, symmetric, ...
}

class DatatypeProperty extends PropertyClass { // A data type property: the range is a string
	String range; // Property range
} 

class ObjectProperty extends PropertyClass { // An object property: the range is a class
  OwlClass range;      // Property range
  ObjectProperty inverseOf;  // if it is not null, it points to its inverse property object
}

class DataType { // Declares a data type or its restriction
	String about;  // what is a data type; hash key
	String subClassOf; // id is a subclass of which DataType
}
class Relation{
	private String iri;
	private String name;
	private String namespace;
	private boolean Asymmetric = false;
    private boolean Functional = false;
    private boolean InverseFunctional = false;
    private boolean Irreflexive = false;
    private boolean Reflexive = false;
    private boolean Symmetric = false;
    private boolean Transitive = false;
	
	public Relation(String iri) {
		setIRI(iri);
	}
	
	void setIRI(String iri){
		this.iri = iri;
		if(iri.split("#").length >= 2){
			namespace = iri.split("#")[0];
			name      = iri.split("#")[1];
		}
		else{
			namespace = "";
			name      = iri;
		}
	}
	
	String getIRI(){
		return iri;
	}
	
	String getName(){
		return name;
	}
	
	String getNamespace(){
		return namespace;
	}
	
	public boolean isAsymmetric(){
		return Asymmetric;
	}
	
	public boolean isFunctional(){
		return Functional;
	}

    public boolean isInverseFunctional(){
		return InverseFunctional;
	}
	
    public boolean isIrreflexive(){
		return Irreflexive;
	}
	
    public boolean isReflexive(){
		return Reflexive;
	}
	
    public boolean isSymmetric(){
		return Symmetric;
	}

	
    public boolean isTransitive(){
		return Transitive;
	}

	public void setAsymmetric(boolean b){
		this.Asymmetric = b;
	}
	
    public void setFunctional(boolean b){
		this.Functional = b;
	}

    public void setInverseFunctional(boolean b){
		this.InverseFunctional = b;
	}
	
    public void setIrreflexive(boolean b){
		this.Irreflexive = b;
	}
	
    public void setReflexive(boolean b){
		this.Reflexive = b;
	}
	
    public void setSymmetric(boolean b){
		this.Symmetric = b;
	}
	
    public void setTransitive(boolean b){
		this.Transitive = b;
	}
	
}

// The following enums are to be extended as needed
// The enums are for improving code reability and execution efficiency - replacing string comparison with integer comparison
enum PropertyType {DatatypeProperty, ObjectProperty}
//enum Property {Inverse}
//enum ObjPropType {Transitive, Symmetric}
//enum ObjPropRelation {Inverse}
enum PropertyResctrictionType {SomeValuesFrom, HasValue}
enum OwlElement {RDF, ONTOLOGY, COMMENT, LABEL, VERSIONINFO, NEWRELATION, CLASS, SUBCLASSOF, DISJOINTWITH, RESTRICTION, 
	    EQUIVALENTCLASS, ONPROPERTY, SOMEVALUESFROM, HASVALUE, ALLDIFFERENT, DATATYPEPROPERTY, DOMAIN, RANGE,
	    DATATYPE, TYPE, OBJECTPROPERTY, INVERSEOF, DISTINCTMEMBERS,
	    // extensions for new relations
	    NAME, REF, LEARNINGORDER, 
	    NODEF,  // no definition; probably new values need be added to the enum
	    NULL    // the String version is null; for debugging
	   }
