Intro
=====
Scans classpath and lists all `Serializable` or `Externalizable` subtypes that are missing a `serialVersionUID` field.

How to use
==========

**Programmatically**

    List<Class<?>> classes = SvuidChecker.checkPackage("com.your.package");

**In a command line**

To inspect `your.jar`:

    java -cp svuid-checker.jar:your.jar:lib/* com.forecastthis.svuidchecker.SvuidChecker --package com.your.package
    
(All required dependencies inside `lib` directory)
    
    