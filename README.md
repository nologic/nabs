(to be cleaned up)

Introduction
============

This documentation is intended for those who are interested in developing modules for the Eunomia Framework. It will describe, with plenty of examples from existing module, how to build new features into the system. This includes both the back end component and the Graphical User Interface. Those who embark on the mission of building a module will need to be very familiar with the Java language and programming environment. There are a couple of reasons for this. One, this documentation uses a lot of Java specific terms and two, the framework is written entirely in Java.

Most modules have a component executed by the '''Sieve''' and a component executed by the '''Console'''. So, by convention we will refer to the anything executed by the Sieve as ''Sieve Side'' and ''Console Side'' as anything that is executed on the Console.

Overview
========
Eunomia is a modular frame work designed for generic processing and reporting. It is optimized for network and real-time data. The system consists of the three components, ideally each would execute on a separate host:
 * '''Sieve''': This component performs all the heavy lifting. It executes the modules' processing functions, manages data and maintains state.
 * '''Console''': This is a stateless component that is used for accessing Sieve's functionality and data. It can be compared to a web browser. The console allows the user to connect to multiple Sieves at the same time.
 * '''Sensor''': The data that Sieve's modules process is generated a Sensor. The sensor resides on collects information from the network and formats it for processing by the Sieve. It is also possible for the Sensor to do some basic processing to summarize the data before it is fully processed by the modules. (This documentation will not cover internal details about this component.)

There are FIVE module types. Each type has it's own internal number and a constant to represent it:
    TYPE_PROC = 0, // Flow processor.
    TYPE_FLOW = 1, // Flow producer.
    TYPE_ANLZ = 2, // Static analysis
    TYPE_COLL = 3, // Flow collection (to DB).
    TYPE_LIBB = 4; // 3rd party library.

 * '''Flow processor''': Module that performs real-time processing on the incoming data from sensors. Each data segment is represented in a form of a flow. The flow is defined by TYPE_FLOW module. The Flow Processor module should be a highly tuned piece of code because the amount of data that the Sieve can process depends directly on how fast each flow processor module is.
 * '''Flow producer''': Module that parses the real-time data generated by each sensor. This data would be represented by a unique flow class. This class should contain all the necessary data for the TYPE_PROC module do its job. It is important to maintain this code highly efficient for the same reason as the flow processor module.
 * '''Static Analysis''': Module that sifts through data to produce some sort of knowledge from stored data. It is intended to be a module that computes various statistics on a database, the data was was stored by the TYPE_COLL module. Other jobs would include forensic analysis for pattern discovery. Since this is not a real-time data processing module it is not necessary to optimize it for performance. Performance may be sacrifices for other features, such as low memory foot print or minimal database access.
 * '''Flow Collection''': Module that performs real-time processing on the incoming data from sensors. The processing is very similar with 2 distinctions from the Flow Process module. One, it is intended for inserting the data into the database. Two, this module does not have a user interface component (It does not produce analytical results). Since it is processing data that is potentially high in volume and is real-time this module's processing functions should be optimized for speed.
 * '''3rd Party Library''': This is a special type of a module. It cannot be instantiated in a similar way the other modules can be. This type of module is designed to provide dependency support to external Java code. For example, is a module requires an external JAR for producing nice looking pie charts then it can use the JFreeChart library to do it. While other modules require a special format, this module is free form that only requires a file name of a specific format.

Building the Module
=====================

This section describes the process the module developer needs to go in order to build a fully functioning module. The scope of this section is limited to functionality needed to stand up a module that is stand alone and does not interact with the Eunomia in any way. It is similar to building a stand alone application. Please note that the '''3rd Party Library''' is a special case and does not share anything in common with other modules.

Common
------

There are some requirements that are shared between all modules. Those are needed to define certain properties of the module and to identify an entity as the Eunomia module.
 * It is required that all modules are defined within a single JAR file, this means that all resources and all required classes need to stored with in the JAR file. However, certain module external libraries can be excluded. If they are excluded then they must be defined by some other means. That could be through other modules.
 * Module JAR file does not need to named in any specific format. The name of the file in completely arbitrary.
 * The module JAR must have the Descriptor class completely defined. Read the DescriptorClass for a documentation on how to define the class.

Building a module requires several interfaces defined by the framework, those interface(and classes) can be found in the ModuleAPI project, for external users this may be just the ModuleAPI.jar file. The interfaces defined there are those that are used by the Console and/or Sieve.


== Module Name Space ==
|| '''Type'''    || '''Sieve'''                     || '''Console'''                    ||
||TYPE_PROC      ||''eunomia.module.receptor.proc.''||''eunomia.module.frontend.proc.'' ||
||TYPE_FLOW      ||''eunomia.module.receptor.flow.''||''eunomia.module.receptor.flow.'' ||
||TYPE_ANLZ      ||''eunomia.module.receptor.anlz.''||''eunomia.module.frontend.anlz.'' ||
||TYPE_COLL      ||''eunomia.module.receptor.coll.''||''eunomia.module.frontend.coll.'' ||
||TYPE_LIBB      ||                -                ||        -                         ||

All module name spaces should end with [moduleName] and have a 'Main' class in that package. For example: ''eunomia.module.receptor.proc.streamStatus.Main''

 * ''Flow Processor:'' Flow Processor modules are those that perform real-time analysis on flows. The required interfaces for this module are:
|| ''Side''      || ''Interfaces''                                            || ''Description''
||'''Sieve''':   || com.vivic.eunomia.module.receptor.ReceptorProcessorModule || ''eunomia.module.receptor.proc.[moduleName].Main'' will need to implement this interface. ||
||'''Sieve''':   || com.vivic.eunomia.module.receptor.FlowProcessor           || this is the object whose methods are called by the ''Sieve'' to process each Flow.                    ||
||'''Console''': || com.vivic.eunomia.module.frontend.FrontendProcessorModule || ''eunomia.module.frontend.proc.[moduleName].Main'' will need to implement this interface. ||

 * ''Flow Producer:'' Flow producer modules are those that parse data received from a sensor. This module is unique from others because there is always one and only one instance of it at runtime. This applies to both the Sieve and Console. As soon as the module is loaded it will be instantiated. The module is defined in the same way for both the Console and the Sieve.
||''Interfaces''                             ||    ''Description''                                                                  ||
|| com.vivic.eunomia.module.flow.FlowModule  || This interfaces defines the various components of the module.                       ||
|| com.vivic.eunomia.module.flow.Flow        || Internal representation of a flow. An instance of this is passed to every FlowProcessor interested (serially).||
|| com.vivic.eunomia.module.receptor.FlowCreator || This class is the entry point of data from the sensors. ||
|| com.vivic.eunomia.filter.FilterEntry      || An abstract class to perform filtering specific to the type of flow records generate by a flow module. ||
|| com.vivic.eunomia.module.frontend.FilterEntryEditor || Used by analysis console to edit flow specific information of the module. ||

 * ''Static Analysis:'' Static analysis modules are those that run in a background in a separate thread and produce some result based on some customized processing. Most likely they will be used for maintaining some sort of state for other modules or provide computationally heavy processing. In any case their behavior is defined entirely by the author. Similar to Flow Processor this module has both a Sieve and a Console component.
|| ''Side''      || ''Interfaces''                                            || ''Description''
|| '''Sieve'''   || com.vivic.eunomia.module.receptor.ReceptorAnalysisModule  || ''eunomia.module.receptor.anlz.[moduleName].Main'' will need to implement this interface. ||
|| '''Console''' || com.vivic.eunomia.module.frontend.FrontendAnalysisModule  || ''eunomia.module.frontend.anlz.[moduleName].Main'' will need to implement this interface. ||
Upon instantiation of the module (by Sieve) threadMain() method will be called on the object that implements ReceptorAnalysisModule. This will be done in a separate thread.

 * ''3rd Party Library: '' 3rd party modules are special type of modules. They do no require the definition of a '''Descriptor''' or any Main class. They are also treated the same on Console and Sieve. The modules are never instantiated they are merely there to allows other modules to depend on them. The idea behind this module is to create a way for other modules to use and share other products. For example, pieChart and lossyHistogram modules use JFreeChart to draw graphs. To prevent duplication JFreeChart is defined as a  3rd party library and the same JAR can be used by both modules.

   The only requirement is that the file name of the module should be in a specific format. The format is
   {{{
   [name]-[string version].jar
   }}}
   This creates a module reference within the framework that can be refered to by '''name'''. Internally an integer hash of the '''version''' is used as the version number.

== Project setup ==
The official development environment for Eunomia is Netbeans IDE (www.netbeans.org). Currently it is recommended that version 5.5 (and up) is used. While the developer can choose to use another IDE it is required that each project contains ant scripts with compatible build targets as the ones found produced by Netbeans.

You will need the following software:
 * Java 6: build 1.6.0-b105 - http://java.sun.com/javase/downloads/?intcmp=1281
 * NetBeans 5.5 - http://www.netbeans.org/
 * Ant 1.6.5 - http://ant.apache.org/

=== Directory layout ===
The general breakdown is as follows:

|| '''Directory'''   || '''Description'''                                                     ||
|| /Consoles         || Specialized console implementations. GATEKEEPER, REMOTEGATE           ||
|| /front-end(gui)   || General Console, this one is primarily for development and testing purposes.||
|| /front-end(lib)   || Console API implementation project.||
|| /installer        || Installer scripts for various purposes||
|| /middleware       || Sieve implementation project.||
|| /modules          || all the modules              ||
|| /modules/Veyron   || VEYRON modules               ||
|| /shared           || projects and libraries shared among all other components.||
|| /shared/config    || project for managing local configurations.||
|| /shared/libraries || 3rd party libraries that non-module components depoend on and that we cannot expect users to have by default.||
|| /shared/messages  || all message classes for Sieve to Console communication.||
|| /shared/ModuleAPI || project that defines interfaces used by modules to interact with Sieve and Console.||
|| /shared/Utilities || General support stuff that doesn't really fit anywhere else. ||

=== Starting a project ===
To start a project for a new module:
 * Go to ''File->New Project'' in Netbeans and select ''Java Class Library''. 
 * When given a choose of where to place the project files, select ''/modules/'' or, if you are working on project VEYRON, in ''/modules/Veyron''. 
 * Name the project with something descriptive that can easily be mapped to the name of the actual Eunomia module. If you are working on project VEYRON then prepend ''IMS_'' to the name.

Once that is done you can start implementing the classes:
 * Create proper packages (eunomia.module.[receptor | frontend].[TYPE].[module Name]) by right-clicking on the project name and selecting ''New->Java Package''.
 * Add required project by rigt-clicking on the project name and selecting ''Properties''. Then go to ''Libraries''
   * For most modules you'll just need ''ModuleAPI'' project, however it is also useful to add ''Utilities''
     * in case of project VEYRON, you may also need NABFlowV2 (from the modules directory).

Committing to the SVN:
 * Locate file: private.properties in [PROJECT]/nbproject/private/ and edit the paths to be relative.
   * for example (Win32, but will work with any JRE): ''F:\\NABS\\src\\client\\trunk\\shared\\ModuleAPI'' should become ''..\\..\\..\\shared\\ModuleAPI''
 * Then add all required directories to the SVN. Unless there is a specific purpose DO NOT ADD THE FOLLOWING:
   * ./dist/
   * ./test/
   * ./build/

Finding documentation:
 * The framework interfaces and actions are very much liquid, so documentation may lag behind a little. I will put a lot of effort to make sure it doesn't happen. To ensure that we remain more or less in sync, most of the usage documentation will be stored in the interfaces themselves and presented through Javadoc at /Eunomia/javadoc/.
 * Other forms of documentation, such as techniques for performing certain actions will be found on this page, you are, of course, welcome and encouraged to add and update content on the Wiki.

NOTE TO THE DEVELOPER: I realize that as module development begins we will find things in the framework that are broken or essential for development and deployment. If you find things like that submit a ticket in this TRAC, I will be checking it periodically. If it is something that is absolutely urgent then shoot me an e-mail point to the ticket number. You are absolutely welcome to propose new features and point out bugs, however for the moment do not make commits to the framework.

=== Deploying a module ===
 * ''On Sieve'' 
   * the default username/password are 'root'/'toor'
   * The configuration file is: config.nab
     * a sample in committed to the SVN, this file should be in the sieve's working directory.
   * Module list file: modules.nab
     * a sample in committed to the SVN, this file should be in the sieve's working directory.
     * the file should contain a list of all modules to be loaded at start up. Paths can be relative.
   * To run execute 'ant run'

 * ''On Console''
   * use the development console, located in /front-end(gui)
   * To run execute 'ant run'
     * if there is an error with loading a module, try deleting /front-end(gui)/modules directory
   * Double-click on ''Add Sieve'' icon.
   * Double-click on the icon that represents the Sieve you just added.
   * Finally, when the Sieve window shows up, click on 'Start Module' and select the one you want.