//package pace;

//6/3/2014:  version 0.3, supporting learning orders, partOf (include), implement (implementedBy), Lixin Tao, Pace University, ltao@pace.edu
//6/17/2013: version 0.2, Lixin Tao, Pace University, ltao@pace.edu
//7/15/2012: version 0.1, supporting all features in file "camera.owl", Lixin Tao, Pace University, ltao@pace.edu
//package edu.pace.semweb;
import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

public class PaceJena extends DefaultHandler implements LexicalHandler {
	StringBuffer textBuffer;
	Stack<State> states = new Stack<State>(); // stack of states
	int pass = 1;
	
	Hashtable<String, Ontology> ontologyHash = new Hashtable<String, Ontology>();
	Ontology currentOntology;
	     
	public static void main(String argv[]){
	 /*if (argv.length < 1 || argv.length > 2) {
	   System.err.println("Usage: java PaceJena Ontology-file");
	   System.exit(1);
	 }*/
	 PaceJena o = new PaceJena();
	 o.readOntology("rel.owl");
	 System.out.println("--> reading complete");
	 o.printOntology();
	 o.printRelations();
	 o.printClasses();
	 o.printProperties();  
	 o.printDataTypes(); 
	 o.printLearningOrders();
	  
	 System.exit(0);     
	}
	
	public PaceJena(){}
	  
	public PaceJena(String ontologyFileName) {
	  //PaceJena o = new PaceJena();
	  readOntology(ontologyFileName);
	}
	
	String removeFilePath(String fileName) {
		int i1 = fileName.lastIndexOf('\\'); 
		int i2 = fileName.lastIndexOf('/');
		if (i1 > i2) i2 = i1;
		if (i2 == -1) return fileName;
		else return fileName.substring(i2+1);
	}
	
	String remove(String sharp){
		int i=sharp.lastIndexOf('#');
		return sharp.substring(i+1);
		}
	
	public void readOntology(String ontologyFileName) {
	  currentOntology = new Ontology();
	  currentOntology.filePath = ontologyFileName;
	  String ontologyNameBase = removeFilePath(ontologyFileName);
	  currentOntology.fileName = ontologyNameBase;
	  ontologyHash.put(ontologyNameBase, currentOntology);
		
	  boolean hasError = false;
	  SAXParserFactory factory = SAXParserFactory.newInstance();
	  factory.setNamespaceAware(true);
	 
	  InputStream stream = null;
	  try {
	    // Parse the input
	    SAXParser saxParser = factory.newSAXParser();
	    XMLReader xmlReader = saxParser.getXMLReader();
	    // Use an instance of the class as the SAX event handler
	    xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", this);    
	    pass = 1;
	    saxParser.parse(new File(ontologyFileName), this); // register classes and properties only
	    //stream = getClass().getResourceAsStream(ontologyFileName);
	 	  //saxParser.parse(stream, this); // register classes and properties only
	    textBuffer = null;
	    pass = 2;
	    saxParser.parse(new File(ontologyFileName), this); // process the rest
	    //stream.close();
	    //stream = getClass().getResourceAsStream(ontologyFileName);
	    //saxParser.parse(stream, this); // process the rest
	  } catch (SAXParseException spe) {
	    // Error generated by the parser
	    System.out.println("\n** Parsing error"
	      + ", line " + spe.getLineNumber()
	      + ", uri " + spe.getSystemId());
	    System.out.println("   " + spe.getMessage() ); 
	    // Use the contained exception, if any
	    Exception  x = spe;
	    if (spe.getException() != null)
	      x = spe.getException();
	    x.printStackTrace();
	  } catch (SAXException sxe) {
	    // Error generated by this application
	    // (or a parser-initialization error)
	    Exception  x = sxe;
	    if (sxe.getException() != null)
	      x = sxe.getException();
	    x.printStackTrace();
	  } catch (ParserConfigurationException pce) {
	    // Parser with specified options can't be built
	    pce.printStackTrace();
	  } catch (IOException ioe) {
	    // I/O error
	    ioe.printStackTrace();
	  } catch (Throwable t) {
	    t.printStackTrace();
	  } finally {
		  try {
			  if (stream != null) {
				  stream.close();
			  }
		  } catch(Exception e) {
			  e.printStackTrace();	  
		  }
	  }
	}
	
	// Return a vector of super class OwlClass objects for className
	public Vector<OwlClass> superClasses(String className) {
		Vector<OwlClass> classes = new Vector<OwlClass>();
		OwlClass o = findClass(className);
		if (o == null) return null;	
		Iterator<OwlClass> i = o.subClassOf.iterator();
		while (i.hasNext()) 
		  classes.add((OwlClass)i.next());  
		return classes;
	}
	
	// Return a vector of subclass OwlClass objects for className
	public Vector<OwlClass> subClasses(String className) {
		Vector<OwlClass> classes = new Vector<OwlClass>();
		OwlClass o = findClass(className);
		if (o == null) return null;	
		Iterator<OwlClass> i = o.superClassOf.iterator();
		while (i.hasNext()) 
		  classes.add((OwlClass)i.next());  
		return classes;
	}
	
	// Return a vector of equivalent class OwlClass objects for className
	public Vector<Object> equivalentClasses(String className) {
		Vector<Object> classes = new Vector<Object>();
		OwlClass o = findClass(className);
		if (o == null) return null;	
		Iterator<Object> i = o.equivalentClass.iterator();
		while (i.hasNext()) 
		  classes.add(i.next());    
		return classes;
	}
	
	// Return a vector of disjoint class OwlClass objects for className
	public Vector<OwlClass> disjointClasses(String className) {
		Vector<OwlClass> classes = new Vector<OwlClass>();
		OwlClass o = findClass(className);
		if (o == null) return null;	
		Iterator<OwlClass> i = o.disjointWith.iterator();
		while (i.hasNext()) 
		  classes.add((OwlClass)i.next());  
		return classes;
	}
	
	// Return an array of super class names for className
	public String[] superClassNames(String className) {
		Vector<OwlClass> classes = superClasses(className);
		String[] names = new String[classes.size()];
		for (int i = 0; i < classes.size(); i++) 
			names[i] = classes.get(i).id;
		return names;
	}
	
	// Return an array of subclass names for className
	public String[] subClassNames(String className) {
		Vector<OwlClass> classes = subClasses(className);
		String[] names = new String[classes.size()];
		for (int i = 0; i < classes.size(); i++) 
			names[i] = classes.get(i).id;
		return names;
	}
	
	// Return an array of equivalent class names for className
	public String[] equivalentClassNames(String className) {
		Vector<Object> classes = equivalentClasses(className);
		String[] names = new String[classes.size()];
		for (int i = 0; i < classes.size(); i++) {
			Object p = classes.get(i);
		    if ((p instanceof String) && (p != null))
		       names[i] = (String)p;
		    else if ((p instanceof OwlClass) && (p != null))
		       names[i] = ((OwlClass)p).id;  
		}
		return names;
	}
	
	// Return an array of disjoint class names for className
	public String[] disjointClassNames(String className) {
		Vector<OwlClass> classes = disjointClasses(className);
		String[] names = new String[classes.size()];
		for (int i = 0; i < classes.size(); i++) 
			names[i] = classes.get(i).id;
		return names;
	}
	
	// Returns className's ontology name
	public String classOntology(String className) {
		OwlClass o = findClass(className);
		return o.ontology.label;
	}
	  
	// Returns propertyName's ontology name
	public String propertyOntology(String propertyName) {
		PropertyClass o = findProperty(propertyName);
		return o.ontology.label;
	}
	  
	// Returns className's namespace
	public String classNamespace(String className) {
		OwlClass o = findClass(className);
		return o.namespace;
	}
	  
	// Returns propertyName's namespace
	public String propertyNamespace(String propertyName) {
		PropertyClass p = findProperty(propertyName);
		return p.namespace;
	}
	  
	  
	// Returns a vector of PropertyClass objects for className
	public Vector<PropertyClass> properties(String className) {
		OwlClass o = findClass(className);
		Iterator i = o.properties.iterator();
		Vector<PropertyClass> v = new Vector<PropertyClass>();
		while (i.hasNext()) 
		  v.add((PropertyClass)i.next());  
		return v;
	}
	  
	// Returns an array of property names for className
	public String[] propertyNames(String className) {
		Vector<PropertyClass> p = properties(className);
		String[] names = new String[p.size()];
		for (int i = 0; i < p.size(); i++) 
	    names[i] = p.get(i).id;
		return names;
	}
	  
	// Returns property type name of propertyName
	public String propertyType(String propertyName) {
		PropertyClass p = findProperty(propertyName);
		return p.propertyType.name();
	}
	  
	// Returns domain name of propertyName
	public String propertyDomain(String propertyName) {
		PropertyClass p = findProperty(propertyName);
		return p.domain.id; 
	}
	  
	// Returns range name of propertyName
	public String propertyRange(String propertyName) {
		PropertyClass p = findProperty(propertyName);
		switch (p.propertyType) {
		  case DatatypeProperty:
			  DatatypeProperty d = (DatatypeProperty)p;
			  return d.range;
		  case ObjectProperty:
			  ObjectProperty o = (ObjectProperty)p;
			  return o.range.id;
		  default: return "";
		}
	}
	   
	// Returns vector of OwlClass objects for learning order with name "name"
  public Vector<OwlClass> getLearningOrderClasses(String name) {
  	return currentOntology.learningOrderHash.get(name);
  }
    
  String[] classToString(Vector<OwlClass> c) {
    if (c == null) return null;
  	String [] names = new String[c.size()];
  	for (int j = 0; j < c.size(); j++)
  	  names[j] = c.elementAt(j).id;
  	return names;
  }
  
  // Returns list of learning order IDs
  public String[] getLearningOrderIDs() {
    Enumeration<String> e = currentOntology.learningOrderHash.keys();
    String[] s = new String[currentOntology.learningOrderHash.size()];
    for (int i = 0; e.hasMoreElements(); i++) 
	    s[i] = e.nextElement();

  	return s;
  }
  
  // Returns learning order for learning order ID id
  public String[] getLearningOrderNames(String id) {
    return classToString(getLearningOrderClasses(id));
  }  
	
	void printOntology() {
	 
		 System.out.println("Namespace:\n");
	  // Print namespace prefix definitions
	  Enumeration<String> j = currentOntology.nsPrefixHash.keys();
	  while (j.hasMoreElements()) {
	 	  String prefix = j.nextElement();
	      System.out.println("xmlns:" + prefix + "='" + currentOntology.nsPrefixHash.get(prefix) + "'");  
	  }
	  
	  System.out.println("\nOntology general data:\n");
	  if (currentOntology.about != null)
		  System.out.println("Ontology:about: " + currentOntology.about);
	  if (currentOntology.comment != null) 
		  System.out.println("Ontology:comment: " + currentOntology.comment);
	  if (currentOntology.label != null) 
		  System.out.println("Ontology:label: " + currentOntology.label);
	  if (currentOntology.versionInfo != null) 
		  System.out.println("Ontology:versionInfo: " + currentOntology.versionInfo); 
	  if (currentOntology.base != null) 
		  System.out.println("Ontology:base: " + currentOntology.base); 
	  if (currentOntology.allDifferent != null) 
	      System.out.println("Ontology:allDifferent: " + (currentOntology.allDifferent ? "true" : "false"));
	}
	void printRelations(){
		System.out.println("\nNew Relations:\n");
		for (String key: currentOntology.newRelationHash.keySet()) {
			Relation rel = currentOntology.newRelationHash.get(key);
			String id = rel.getIRI();
			System.out.println(id + "\n");
			String qName = id;
			if(id.split("#").length >= 2)
				qName = id.split("#")[1];

			if(rel.isFunctional())
				System.out.println(qName + ": is a Functional Relation");
			if(rel.isInverseFunctional())
				System.out.println(qName + ": is a Inverse Functional Relation");
			if(rel.isIrreflexive())
				System.out.println(qName + ": is a Irreflexive Relation");
			if(rel.isReflexive())
				System.out.println(qName + ": is a Reflexive Relation");
			if(rel.isSymmetric())
				System.out.println(qName + ": is a Symmetric Relation");
			if(rel.isTransitive())
				System.out.println(qName + ": is a Transitive Relation");
			if(rel.isAsymmetric())
				System.out.println(qName + ": is a Asymmetric Relation");
		}
	}
	void printClasses() {
	  System.out.println("\nClasses:\n");
	  Enumeration<String> j = currentOntology.owlClassHash.keys();
	  while (j.hasMoreElements()) {
	    OwlClass o = currentOntology.owlClassHash.get(j.nextElement());
	    System.out.println("Class " + remove(o.about) + ": namespace = " + o.namespace);
	    System.out.println("Class " + remove(o.about) + ": ontology = " + o.ontology.label);
	    Iterator i = o.subClassOf.iterator(); 
	    while (i.hasNext()) 
	      System.out.println("Class " + remove(o.about) + ": subclass of - " + remove(((OwlClass)i.next()).about));
	    i = o.superClassOf.iterator();
	    while (i.hasNext()) 
	      System.out.println("Class " + remove(o.about) + ": super class of - " + remove(((OwlClass)i.next()).about));
	    i = o.equivalentClass.iterator();
	    while (i.hasNext()) {
	      Object p = i.next();
	      if ((p instanceof String) && (p != null))
	        System.out.println("Class " + o.id + ": equivalent to - " + (String)p);
	      else if ((p instanceof OwlClass) && (p != null))
	        System.out.println("Class " + o.id + ": equivalent class of - " + ((OwlClass)p).id);  
	    }
	    i = o.disjointWith.iterator(); 
	    while (i.hasNext()) {
	      OwlClass c = (OwlClass)i.next();
	      if (c != null)
	        System.out.println("Class " + o.id + ": disjoint with - " + c.id); 
	    }
	    i = o.properties.iterator();
	    while (i.hasNext()) 
	      System.out.println("Class " + o.id + ": property - " + ((PropertyClass)i.next()).id);
	    i = o.propertyRestrictions.iterator();
	    while (i.hasNext()) 
	      printPropertyRestriction(o.id, (PropertyRestriction)i.next());
	    
		for(Relation rel : o.relationsMap.keySet()){
			List<OwlClass> relatedClasses = o.relationsMap.get(rel); 
			for(OwlClass b: relatedClasses)
				System.out.println("Class " + o.about + " : related by - " + rel.getName() + " : to -" + b.about);
		}
	    /*i = o.includes.iterator(); 
	    while (i.hasNext()) {
	      OwlClass c = (OwlClass)i.next();
	      if (c != null)
	        System.out.println("Class " +remove(o.about)+ ": includes - " + remove(c.about)); 
	    }
	    i = o.partOf.iterator(); 
	    while (i.hasNext()) {
	      OwlClass c = (OwlClass)i.next();
	      if (c != null)
	        System.out.println("Class " + remove(o.about) + ": part-of - " + remove(c.about)); 
	    }
	    i = o.implement.iterator(); 
	    while (i.hasNext()) {
	      OwlClass c = (OwlClass)i.next();
	      if (c != null)
	        System.out.println("Class " + o.id + ": implements - " + c.id); 
	    }  
	    i = o.implementedBy.iterator(); 
	    while (i.hasNext()) { 
	      Implementor imp = (Implementor)i.next();
	      System.out.print("Class " +remove(o.about) + ": implemented-by -"); 
	      Iterator ii = imp.impl.iterator(); 
	      while (ii.hasNext()) {
	        OwlClass c = (OwlClass)ii.next();
	        if (c != null) System.out.print(" " + remove(c.about));         
	      }
	      System.out.println();
	    }*/
	  } 
	}
	
	void printProperties() {
	  System.out.println("\nProperties:");
	  Enumeration<String> j = currentOntology.propertyHash.keys();
	  while (j.hasMoreElements()) {
	    PropertyClass o = currentOntology.propertyHash.get(j.nextElement());
	    if (o.namespace != null) System.out.println("Property " + o.id + ": namespace - " + o.namespace);
	    if (o.ontology != null && o.ontology.label != null) System.out.println("Property " + o.id + ": ontology - " + o.ontology.label);
	    if (o.propertyType != null) System.out.println("Property " + o.id + ": property type - " + o.propertyType.name());
	    if (o.domain != null) System.out.println("Property " + o.id + ": domain - " + o.domain.id);
	    if (o.propertyType != null && o.propertyType == PropertyType.DatatypeProperty) {
	      DatatypeProperty dp = (DatatypeProperty)o;
	      if (dp.range != null) System.out.println("Property " + o.id + ": range - " + dp.range);
	    }
	    else if (o.propertyType != null && o.propertyType == PropertyType.ObjectProperty) {
	      ObjectProperty op = (ObjectProperty)o;
	      if (op.range != null) System.out.println("Property " + o.id + ": range - class " + op.range.id);
	      if (op.inverseOf != null) System.out.println("Property " + o.id + ": inverse of property " + op.inverseOf.id);
	    }
	    Iterator i = o.type.iterator();
	    while (i.hasNext()) 
	      System.out.println("Property " + o.id + ": type - " + i.next()); 
	  }
	}
	
	void printPropertyRestriction(String className, PropertyRestriction o) {
	  if (o.basePropertyName != null) System.out.println("Class " + className + ": property restriction - base property name - " + o.basePropertyName);
	  if (o.baseProperty != null) System.out.println("Class " + className + ": property restriction - base property obj - " + o.baseProperty.id);
	  if (o.type != null) System.out.println("Class " + className + ": property restriction - type - " + o.type.name());
	  if (o.value != null) System.out.println("Class " + className + ": property restriction - value - " + o.value);
	  if (o.valueType != null) System.out.println("Class " + className + ": property restriction - value type - " + o.valueType);
	  if (o.valueClass != null) System.out.println("Class " + className + ": property restriction - value class - " + o.valueClass.id);
	}
	
	void printDataTypes() {
	  System.out.println("\nData Types:");
	  Enumeration<String> j = currentOntology.dataTypeHash.keys();
	  while (j.hasMoreElements()) {
	    DataType o = currentOntology.dataTypeHash.get(j.nextElement());
	    if (o.about != null) System.out.println("Data type: about = " + o.about); 
	    if (o.subClassOf != null) System.out.println("Data type:   subClassOf = " + o.subClassOf); 
	  }
	}
	
	void printLearningOrders() {
	  System.out.println("\nLearning Orders:");
	  String[] ids = getLearningOrderIDs();
	  for (int i = 0; i < ids.length; i++) {
	    String[] names = getLearningOrderNames(ids[i]);
	    System.out.print("Learning Order " + ids[i] + ":");
  		for (String s: names) 
  			System.out.println(" " + s);
  	  System.out.println();    
	  }
    System.out.println();
  }
 
  
	//===========================================================
	// SAX DocumentHandler methods
	//===========================================================
	
	public void setDocumentLocator(Locator l) {
	}
	
	public void startDocument() throws SAXException {
	}
	
	public void endDocument() throws SAXException {
	}
	
	// convert string names to values in enum OwlElement; improving efficiency and maintainability
	OwlElement toEnum(String n) {
		if (n == null) return OwlElement.NULL;
	  OwlElement elem;
	  try{
		elem = OwlElement.valueOf(n.toUpperCase());
	  }
	  catch(Exception e){
		elem = null;
	  }
		if (elem == null) return OwlElement.NODEF;
    return elem;
	}
	
	// return the value of the attribute with name ending with value of "name"
	String attrValue(Attributes attrs, String name) {
	  if (attrs == null || name == null) return "";
	  for (int i = 0; i < attrs.getLength(); i++) {
	    String aName = attrs.getQName(i); // Attr name 
	    if ("".equals(aName)) aName = attrs.getLocalName(i);
	    if (aName.lastIndexOf(name) >= 0) 
	      return attrs.getValue(i);
	  }
	  return "";
	}
	

	// Reference to class name may not start with "#"
	// need revision to deal with general case like global classes with URIs
	public OwlClass findClass(String name) {
	  if (name.startsWith("#")) name = name.substring(1); 
	  OwlClass o = currentOntology.owlClassHash.get(name);
	  if (o == null) error("Class" +" "+name + " missing");
	  return o;
	}
	
	// Reference to property name may not start with "#"
	// need revision to deal with general case like global properties with URIs
	public PropertyClass findProperty(String name) {
	  if (name.startsWith("#")) name = name.substring(1); 
	  PropertyClass o = currentOntology.propertyHash.get(name);
	  if (o == null) error("Property " + name + " missing");
	  return o;
	}
	
	public DataType findDatatype(String name) {
	  if (name.startsWith("#")) name = name.substring(1); 
	  DataType o = currentOntology.dataTypeHash.get(name);
	  if (o == null) error("DataType " + name + " missing");
	  return o;   
	}
	
	public Relation findRelation(String name) {
		  String iri = name;
		  if(iri.split("#").length >= 2)
			iri = iri.split("#")[1];
		  Relation o = currentOntology.newRelationHash.get(iri);
		  if (o == null) error("Relation " + name + " missing");
		  return o;   
	}
	
	static State stack_at(Stack<State> s, int i) { // index of stack top is 0
	  return s.get(s.size() - i - 1);
	}
	
	// Current learning order being assembled, and its ID is on stack top
	Vector<OwlClass> learningOrderVector;
	
	void processRelationRestriction(Relation rel, String prop){
		if(prop.split("#").length >= 2)
			prop = prop.split("#")[1];
		
		if(prop.equals("AsymmetricRelation"))
            rel.setAsymmetric(true);
        else if(prop.equals("FunctionalRelation"))
			rel.setFunctional(true);
        else if(prop.equals("InverseFunctionalRelation"))
			rel.setInverseFunctional(true);
        else if(prop.equals("IrreflexiveRelation"))
            rel.setIrreflexive(true);
        else if(prop.equals("ReflexiveRelation"))
            rel.setReflexive(true);
        else if(prop.equals("SymmetricRelation"))
			rel.setSymmetric(true);
        else if(prop.equals("TransitiveRelation"))
            rel.setTransitive(true);
        else
            System.out.println("----> INFO: No match found in processRelationRestriction");

	}
	
	public void startElement(String namespaceURI,
	                         String sName, // simple name
	                         String qName, // qualified name
	                         Attributes attrs)
	throws SAXException {
	  OwlElement n = toEnum(sName);  
	  OwlClass aClass, bClass;
	  if (pass == 1) { // solve the problem caused by use before declaration
	    switch (n) {
	      case CLASS: 
	        aClass = new OwlClass();
	        aClass.namespace = currentOntology.base; // need be revised to the class's namespace
	        aClass.about=attrValue(attrs,"about");
	        //aClass.id = attrValue(attrs, "ID");
	        //aClass.name = attrValue(attrs, "name");
	        aClass.ontology = currentOntology;
	        currentOntology.owlClassHash.put(aClass.about, aClass);       
	        break;
	      case DATATYPEPROPERTY:
	        DatatypeProperty dp = new DatatypeProperty();
	        dp.namespace = currentOntology.base;  // of no use, set again in pass 2
	        dp.id = attrValue(attrs, "ID");
	        dp.ontology = currentOntology;
	        dp.propertyType = PropertyType.DatatypeProperty;
	        currentOntology.propertyHash.put(dp.id, dp); // generalize it to cover namespace
	        break;
	      case DATATYPE: 
	        DataType dt = new DataType();
	        dt.about = attrValue(attrs, "about");
	        currentOntology.dataTypeHash.put(dt.about, dt);
	        break;
	      case NEWRELATION:
			String iri = attrValue(attrs,"about");
			if(iri.startsWith("#"))
				iri = currentOntology.base + iri;
	    	Relation rel= new Relation(iri);
	    	currentOntology.newRelationHash.put(rel.getName(),rel);
			break;
	      case OBJECTPROPERTY: 
	        ObjectProperty op = new ObjectProperty();
	        op.namespace = currentOntology.base; // of no use, set again in pass 2
	        op.id = attrValue(attrs, "ID");
	        op.ontology = currentOntology;
	        op.propertyType = PropertyType.ObjectProperty;
	        currentOntology.propertyHash.put(op.id, op); // generalize it to cover namespace
	        break;
	      default: break;
	    } 
	    return;
	  }
	  // pass 2 starts here
	  Object obj = new Object();
	  switch (n) {
		case RDF: 
	      obj = new Object(); 
	      currentOntology.base = attrValue(attrs, "base");
	      break;
		case ONTOLOGY: 
	      obj = currentOntology; 
	      currentOntology.about = attrValue(attrs, "about");
	      break;
		case COMMENT: obj = new Object(); break;
		case LABEL: obj = new Object(); break;
		case VERSIONINFO: obj = new Object(); break;
		case NEWRELATION:
			Relation rel=findRelation(attrValue(attrs, "about"));
			obj = rel; 	
			break;
		case CLASS: 
	      aClass = findClass(attrValue(attrs, "about")); 
	      if (aClass == null) error("Class " + attrValue(attrs, "about") + " missing");
	      aClass.namespace = currentOntology.base; // need be revised to the class's namespace
	      obj = aClass;      
	      break;
		case SUBCLASSOF: 
	      if (attrValue(attrs, "resource").equals("")) { // base class is a restriction
	        obj = new Object();       
	        break;
	      }
	      if (states.peek().getOwlElement() == OwlElement.CLASS) {
	        aClass = (OwlClass)states.peek().getObject(); // subClassOf must be directly nested in class or datatype
	        bClass = findClass(attrValue(attrs, "resource"));
	        aClass.subClassOf.add(bClass); 
	        bClass.superClassOf.add(aClass);
	      }
	      else if (states.peek().getOwlElement() == OwlElement.DATATYPE) {
	        DataType d = (DataType)states.peek().getObject(); // subClassOf must be directly nested in class or datatype
	        d.subClassOf = attrValue(attrs, "resource");
	      }
	      else error("Misplaced subClassOf");
	      obj = new Object();       
	      break;
		case DISJOINTWITH: 
	      if (states.peek().getOwlElement() != OwlElement.CLASS) error("Misplaced DisjointWith");
	      aClass = (OwlClass)states.peek().getObject();
	      aClass.disjointWith.add(findClass(attrValue(attrs, "resource")));
	      obj = new Object(); 
	      break;
		case RESTRICTION: 
	      if (states.peek().getOwlElement() != OwlElement.SUBCLASSOF) error("Misplaced Restriction");
	      PropertyRestriction pr = new PropertyRestriction();
	      aClass = (OwlClass)stack_at(states, 1).getObject(); 
	      aClass.propertyRestrictions.add(pr); 
	      obj = pr; 
	      break;
		case EQUIVALENTCLASS: 
	      if (states.peek().getOwlElement() != OwlElement.CLASS) error("Misplaced EquivalentClass");
	      aClass = (OwlClass)stack_at(states, 0).getObject();
	      if (attrValue(attrs, "resource").startsWith("http:"))
	        aClass.equivalentClass.add(attrValue(attrs, "resource"));
	      else
	        aClass.equivalentClass.add(findClass(attrValue(attrs, "resource"))); 
	      obj = new Object(); 
	      break;
		case ONPROPERTY: 
	      if (states.peek().getOwlElement() != OwlElement.RESTRICTION) error("Misplaced onProperty");
	      pr = (PropertyRestriction)stack_at(states, 0).getObject();
	      pr.basePropertyName = attrValue(attrs, "resource");
	      pr.baseProperty = findProperty(pr.basePropertyName);
	      obj = new Object(); 
	      break;
		case SOMEVALUESFROM: 
	      if (states.peek().getOwlElement() != OwlElement.RESTRICTION) error("Misplaced someValuesFrom");
	      pr = (PropertyRestriction)stack_at(states, 0).getObject();
	      pr.valueClass = (OwlClass)findClass(attrValue(attrs, "resource"));
	      pr.type = PropertyResctrictionType.SomeValuesFrom;
	      obj = new Object(); 
	      break;
		case HASVALUE: 
	      if (states.peek().getOwlElement() != OwlElement.RESTRICTION) error("Misplaced hasValue");
	      pr = (PropertyRestriction)stack_at(states, 0).getObject();
	      pr.valueType = attrValue(attrs, "datatype");
	      pr.type = PropertyResctrictionType.HasValue;       
	      obj = new Object(); 
	      break;
		case ALLDIFFERENT: obj = new Object(); break; // have not been worked on yet
		case DATATYPEPROPERTY: 
	      String name = attrValue(attrs, "ID");
	      DatatypeProperty dp = (DatatypeProperty)findProperty(name); 
	      dp.namespace = currentOntology.base;
	      obj = dp; 
	      break;
		case DOMAIN: 
	      if (states.peek().getOwlElement() != OwlElement.OBJECTPROPERTY && states.peek().getOwlElement() != OwlElement.DATATYPEPROPERTY) error("Misplaced domain");
	      PropertyClass p = (PropertyClass)stack_at(states, 0).getObject();
	      OwlClass o = (OwlClass)findClass(attrValue(attrs, "resource"));
	      p.domain = o;
	      o.properties.add(p);
	      obj = new Object(); 
	      break;
		case RANGE: 
	      if (states.peek().getOwlElement() == OwlElement.OBJECTPROPERTY) {
	        ObjectProperty op = (ObjectProperty)stack_at(states, 0).getObject();
	        op.range = (OwlClass)findClass(attrValue(attrs, "resource"));
	      }
	      else if (states.peek().getOwlElement() == OwlElement.DATATYPEPROPERTY) {
	        dp = (DatatypeProperty)stack_at(states, 0).getObject();
	        dp.range = attrValue(attrs, "resource");
	      }
	      else 
	        error("Misplaced range");
	      obj = new Object(); 
	      break;
		case DATATYPE: 
	      DataType dt = findDatatype(attrValue(attrs, "about"));
	      obj = dt; 
	      break;
		case TYPE:
		  if(states.peek().getOwlElement() == OwlElement.NEWRELATION)
		  {
			Relation nr =  (Relation)states.peek().getObject();
			processRelationRestriction(nr,attrValue(attrs, "resource"));
			obj = new Object();
			break;
		  }
	      if ((states.peek().getOwlElement() != OwlElement.OBJECTPROPERTY) && (states.peek().getOwlElement() != OwlElement.DATATYPEPROPERTY)) error("Misplaced type");
	      p = (PropertyClass)stack_at(states, 0).getObject();
	      p.type.add(attrValue(attrs, "resource"));
	      obj = new Object(); 
	      break;
		case OBJECTPROPERTY: 
	      name = attrValue(attrs, "ID");
	      ObjectProperty op = (ObjectProperty)findProperty(name);
	      op.namespace = currentOntology.base;
	      obj = op; 
	      break;
		case INVERSEOF: 
	      if (states.peek().getOwlElement() != OwlElement.OBJECTPROPERTY) error("misplaced inverseOf");
	      op = (ObjectProperty)stack_at(states, 0).getObject();
	      ObjectProperty op2 = (ObjectProperty)findProperty(attrValue(attrs, "resource"));
	      op.inverseOf = op2;
	      op2.inverseOf = op;
	      obj = new Object(); 
	      break; 
	    case DISTINCTMEMBERS: obj = new Object(); break; // have not been worked on yet
	    case LEARNINGORDER:
          String id = attrValue(attrs, "ID");
          if (id == null) id = "default";
          obj = id;   
          learningOrderVector = new Vector<OwlClass>();    
          break;
		case REF:
    	  String resourceName = attrValue(attrs, "resource");
          aClass = findClass(resourceName);
          if (states.peek().getOwlElement() == OwlElement.LEARNINGORDER) 
          { 
		    if (aClass != null)
        	 learningOrderVector.add(aClass);
          }
          obj = aClass;
          break;
		default:
		  if(namespaceURI.equals("http://www.pace.edu/rel-syntax-ns#")){
			if(findRelation(sName) == null){
				error("Relation not found");
				break;
			}
			if (states.peek().getOwlElement() == OwlElement.CLASS) {
				rel = findRelation(sName);
				aClass = (OwlClass)states.peek().getObject();
				bClass = findClass(attrValue(attrs, "resource"));
				if(aClass.relationsMap.get(rel) == null)
					aClass.relationsMap.put(rel,new ArrayList<OwlClass>());
				aClass.relationsMap.get(rel).add(bClass);
				obj = new Object();
			}
			break;
		  }
		  else
		    System.out.println("startElement(): undefined element: " + sName);
		}
	  states.push(new State(n,obj));
	}
	
	public void endElement(String namespaceURI,
	                       String sName, // simple name
	                       String qName  // qualified name
	                      )
	throws SAXException {
		OwlElement n = toEnum(sName);
	  if (pass == 1) return;
	  // pass = 2 starts here
		Object o = states.peek().getObject();
		if (n != states.pop().getOwlElement()) 
	    error(sName + " occured at wrong location");
		switch (n) {
			case RDF: break;
			case ONTOLOGY: break;
			case COMMENT: 
			  if (states.pop().getOwlElement() != OwlElement.ONTOLOGY) 
			  	error(sName + " occured at wrong location");
			  currentOntology.comment = text();
			  break;
			case LABEL:
			  if (states.pop().getOwlElement() != OwlElement.ONTOLOGY) 
			  	error(sName + " occured at wrong location");
			  currentOntology.label = text();
			  break;
			case VERSIONINFO:
			  if (states.pop().getOwlElement() != OwlElement.ONTOLOGY) 
			  	error(sName + " occured at wrong location");
			  currentOntology.versionInfo = text();
			  break;
			case NEWRELATION:
			case CLASS:
			case SUBCLASSOF:
			case DISJOINTWITH:
			case RESTRICTION: break;
			case EQUIVALENTCLASS: break;
			case ONPROPERTY:
			case SOMEVALUESFROM: break;
			case HASVALUE:
	             PropertyRestriction pr = (PropertyRestriction)(stack_at(states, 0).getObject());
	             pr.value = text();
	             break;
			case ALLDIFFERENT:  // have not been worked on yet
			case DATATYPEPROPERTY:
			case DOMAIN:
			case RANGE:
			case DATATYPE:
			case TYPE:
			case OBJECTPROPERTY:
			case INVERSEOF: 
			case DISTINCTMEMBERS:
			case REF: break;
	    case LEARNINGORDER:   
        currentOntology.learningOrderHash.put((String)o, learningOrderVector);
        break;		
		default:
			 if(namespaceURI.equals("http://www.pace.edu/rel-syntax-ns#")){
				if(findRelation(sName) == null){
					System.out.println("startElement(): undefined relation: " + sName);
					System.exit(1);
				}
				break;
			}
			else{
			  System.out.println("startElement(): undefined element: " + sName);
			  System.exit(1);
			}
		}
	}
	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		currentOntology.nsPrefixHash.put(prefix, uri);
	}
	                       
	public void endPrefixMapping(String prefix) throws SAXException {
	}
	                   
	public void characters(char buf[], int offset, int len)
	throws SAXException {
	  String s = new String(buf, offset, len);
	  if (textBuffer == null) {
	    textBuffer = new StringBuffer(s);
	  } else {
	    textBuffer.append(s);
	  }
	}
	
	public void ignorableWhitespace(char buf[], int offset, int len) {
	}
	
	public void processingInstruction(String target, String data) {
	}
	
	void error(String message) {
		System.out.println(message);
		//System.exit(1);
	}
	
	//===========================================================
	// SAX ErrorHandler methods
	//===========================================================
	
	// treat validation errors as fatal
	public void error(SAXParseException e)
	throws SAXParseException {
	  throw e;
	}
	
	// dump warnings too
	public void warning(SAXParseException err)
	throws SAXParseException {
	  System.out.println("** Warning"
	       + ", line " + err.getLineNumber()
	       + ", uri " + err.getSystemId());
	  System.out.println("   " + err.getMessage());
	}
	
	//===========================================================
	// LexicalEventListener methods
	//===========================================================
	 
	public void comment(char[] ch, int start, int length)
	throws SAXException {
	}
	
	public void startCDATA()
	throws SAXException {
	  text(); // echo anything we've seen before now
	  //emit("START CDATA SECTION");
	}
	
	public void endCDATA() throws SAXException {
	  text(); // echo the CDATA text
	  //emit("END CDATA SECTION");
	}
	
	public void startEntity(java.lang.String name)
	throws SAXException {
	}
	
	public void endEntity(java.lang.String name)
	throws SAXException {
	}
	
	public void startDTD(String name, String publicId, String systemId) {
	}
	
	public void endDTD() throws SAXException {
	}
	
	//===========================================================
	// Utility Methods ...
	//===========================================================
	
	// Return text accumulated in the character buffer
	private String text() throws SAXException {
	  if (textBuffer == null) return null;   
	  String s = "" + textBuffer;
	  textBuffer = null;
	  return s.trim(); 
	}
	
	protected static class State {
      OwlElement ele;
  	  Object obj;
	
	  Object getObject(){
		return obj;
	  }
	
	  OwlElement getOwlElement(){
		return ele;
	  }
	
	  State(OwlElement e, Object o){
		ele = e;
		obj = o;
	  }
    }
	
}

