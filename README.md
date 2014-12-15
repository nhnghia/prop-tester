
Prop-tester
==============

This work was published in the proceeding of HASE'2012:

- Huu Nghia Nguyen, Pascal Poizat and Fatiha Za•di. Online Verification of Value-Passing Choreographies through Property-Oriented Passive Testing. in HASE'2012 - IEEE International Symposium on High Assurance Systems Engineering. October 2012, to appear


Overview
-------

The architecture of our online verification system is depicted by the image below. Each local tester is attached to a service to be tested. A local tester will collect input/output messages of its service at a point of observation (PO) which is put at a position such that all exchange messages from/to the service are captured. Based on collected log, the local tester will verify its local properties and give local verdict. Since the global properties consist of elements which are the same format with local properties so we use local testers to verify these elements, then the results will be used by the global tester to give verdict of global properties.


![alt tag](https://github.com/nhnghia/prop-tester/raw/master/demo/arch.png)


In our framework, the properties will be verified by 5 main steps:

- **Properties definition.** Standards or protocol experts provide the implementation behaviours to be tested. They are then formulated as our format.
- **Correctness of property.** If specification model of IUT is available, then the properties will be then formally verified on the model guaranteeing that they are correct wrt.  the requirements.
- **Translating property into XQuery.** A property is translated into an XQuery such that it returns false iff the property is violated, and true iff the property is validated. The Inconclusive verdict of the property will be delivered by tester when it found the end of stream of the log and no verdict was delivered before.
- **Extraction of execution traces.** An observer is put at each service to sniff all of its (input and output) messages exchange with its partners. Each time the observer captured a message, if the message relates to properties tested, then it is sent a pipe whose other end, where it will be verified, is an XQuery processor.
- **Properties tested on the traces.** The properties tested in XQuery form will be executed by MXQuery (http://mxquery.org) processor on the XML stream supplying by the observer. Based on result of the query, the verdict (Pass, Fail, or Inconclusive) will be delivered.

In this verification, the step 1, 2 and 3 are done only one time, while the step 4 and 5 are done on real time. We will present our tools supporting these verification: a tool for capturing SOAP message, local tester, and global tester.

The tool chain is downloaded here: https://github.com/nhnghia/prop-tester/tree/master/distribution. This consists of three tools:

- ``proptester-local.jar``: local tester of each separate Web Service
- ``proptester-global.jar``: global tester

Format of Properties
-------

Some examples of local property can be found here https://github.com/nhnghia/prop-tester/tree/master/properties. 
Each property is described in `<property></property>` tag. Namespaces using in these properties are declared in `<namespace></namespace>` tag. The boolean expression of each candidateEvent is a string *wrt.*  XPath syntax, describing in `<predicate></predicate>`. 


Local Tester
-------

Local tester can be run by

``
java -jar local-tester.jar propertyFile inputStreamURLMsg [-b:broadcastPort] [-v]
``

It takes as inputs 3 parameters:

* ``propertyFile`` is a string representing a path to XML file containing local properties.
* ``inputStreamURLMsg`` is an URL pointing to a stream of SOAP messages.
* ``-b:broadcastPort`` is a number representing a port where the results will be broadcast. This parameter is optional, when it is omit the results will be printed on the console.
* ``-v``: verbose

Example:

``
java -jar local-tester.jar /Users/tata/test/quotation.prop.xml http://localhost:8080/ode/processes/quotation -b:2020
``

The verdict output in continuous stream mode, i.e. , a verdict is issued each time a property is tested.  [[tools:prop-tester:verdict|Here]] is of verdicts of a property ''p1''.

Global Tester
-------

Based on global property, the global tester synthesizes results of local testers to give global verdict. It can be run by

``
java -jar global-tester.jar propertyFile inputStreamURL1 â..., inputStreamURLn  [-b:broadcastPort] [-v]
``

It takes as inputs the following parameters:

* ``propertyFile`` is a string representing a path to XML file containing global properties.
* ``inputStreamURLMsg_i`` is an URL pointing to a stream of results of local testers.
* ``-b:broadcastPort`` is a number representing a port where the results will be broadcast. This parameter is optional, when it is omit the results will be printed on the console.
* ``-v``: verbose

Example:

``
java -jar global-tester.jar /Users/tata/test/ad.gprop.xml http://localhost:2020 -b:3030 -v
``
