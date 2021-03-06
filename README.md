Extensible template engine for Java
===================================

Based upon StringTemplate4, this engine extends it with some very useful features making it applicable for a wider range of templating needs.  

It aims to be backwards compatible with ST4-grammar.


User Functions
--------------

*Since 1.0.0*

Often you'll find yourself needing to navigate your model in ways that the template engine does not support natively.  This is especially true for ST4, so I've introduced a way to supply user-defined functions that are callable from the template itself.  These are called in the same manner as the few built-in functions (strlen, trunc, etc..), but they can accept multiple arguments, each of which can be an expression itself. 

Let's say you would like to iterate over a list of objects present somewhere in a DOM-based model, you could do this by executing an XPath-query from your user-function which you'd call from the template like this:

    <xpath(model, {//book/author[id='xxx']/parent::chapters}) : outputChapter()>

where model is your DOM model, xpath is your user-function, outputChapter is a another template and { ... } is an anonymous template evaluating to the XPath expression for the query. 


Multi-character delimiters
--------------------------

*Since 1.0.1*

The engine uses a start- and end-delimiter to locate the expressions in your template.  Previously, these were limited to a single character, but that has now been generalized so you can use strings of two or more characters as delimiters as needed.  This allows you to more easily select delimiters that wont conflict with your template content, making the use of backslash-escaping unnecessary resulting in more readable templates. 

To use, just specify `delimiters "<#", "#>"` in your .stg files, or manually supply them to the constructor of the STGroup or subclasses thereof. 


Usage
-----

    <dependency>
        <groupId>net.evilengineers.templates4j</groupId>
        <artifactId>templates4j</artifactId>
        <version>1.0.0</version>
    </dependency>

The artifact is released in the OSS Sonatype repository: 

    <repository>
        <id>sonatype-nexus</id>
        <url>http://oss.sonatype.org/content/repositories/releases/</url>
    </repository>


History
-------

This project could not have existed if it wasn't for the nice work by the authors of StringTemplate4, but as the features I found necessary in my work were not accepted and because there seems to be a general demand for them, I forked the project and extended it myself.  The new projectname is to avoid confusion and possible conflict with the original engine.

The `templates4j-core` sub-project started its life as a fork of `StringTemplate4 ver. 4.0.8` code. 
The `templates4j-mvn-plugin` sub-project was written from scratch. 

All are covered by a BSD-license. 
