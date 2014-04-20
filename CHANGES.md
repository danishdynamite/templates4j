### 1.0.1 / in-progress

* Added support for multi-character expression-delimiters.  It has often proved difficult finding good single-character start- and stop-delimiters 
  that doesn't result in you having to escape your actual content making it much less readable.  Now you can use delimiters such as <# and #> or longer, 
  reducing the need to ever use escape sequences.  (Henrik Gram)

### 1.0.0 / 20014-04-18

Initial release:

* Removed the ant build scripts.  Updated to Java7.  Cleaned up the maven build and warnings in the code.  (Henrik Gram)   

* Added support for `multi-argument functions`.  The grammar and interpreter was changed slightly to be backwards compatible with the built-in functions.  (Henrik Gram)  

* Added support for `user-funtions`.  (Henrik Gram)

* Changed the java package and project names to avoid conflicts with the original project.  (Henrik Gram)
  
* Forked `antlr/stringtemplate4 4.0.8` due to a desire to extend the framework in ways that were unacceptable to the original author. 
