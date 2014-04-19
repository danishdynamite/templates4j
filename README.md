Extensible template engine for Java
===================================

Based upon StringTemplate4, this engine extends it with some very useful features making it applicable for a wider range of templating needs.  

It aims to be backwards compatible with ST4-grammar.


User Functions
--------------

Often you'll find yourself needing to navigate your model in ways that the template engine does not support natively.  This is especially true for ST4, so I've introduced a way to supply user-defined functions that are callable from the template itself.  These are called in the same manner as the few built-in functions (strlen, trunc, etc..), but they can accept multiple arguments, each of which can be an expression itself. 

Let's say you would like to iterate over a list of objects present somewhere in a DOM-based model, you could do this by executing an XPath-query from your user-function which you'd call from the template like this:

    <xpath(model, {//book/author[id='xxx']/parent::chapters}) : outputChapter()>

where model is your DOM model, xpath is your user-function, outputChapter is a another template and { ... } is an anonymous template evaluating to the XPath expression for the query. 


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

This project could not have existed if it wasn't for the nice work by the authors of StringTemplate4, but as the features I found necessary in my work were not accepted and because there seems to be a general demand for them, I forked the project and extended it myself.  The new projectname is to avoid confusion and possible conflict with the original engine and signals a desire to make it extensible enough to be useful for just about everyone. 
