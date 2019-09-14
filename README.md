Active Choices Plug-in
=================

# Regression with version 2.2, reverted to 2.1

it impacted 2 servers while updating to the latest advisory Jenkins Security Advisory 2019-09-12 
(from LTS 2.176.3 2019-08-28):

https://jenkins.io/security/advisory/2019-09-12/
https://jenkins.io/changelog-stable/

java.lang.IllegalArgumentException: Unable to inject class hudson.model.UserIdMapper
                at hudson.init.TaskMethodFinder.lookUp(TaskMethodFinder.java:125)
                at hudson.init.TaskMethodFinder.invoke(TaskMethodFinder.java:105)
                at hudson.init.TaskMethodFinder$TaskImpl.run(TaskMethodFinder.java:175)
                at org.jvnet.hudson.reactor.Reactor.runTask(Reactor.java:296)
                at jenkins.model.Jenkins$5.runTask(Jenkins.java:1095)
                at org.jvnet.hudson.reactor.Reactor$2.run(Reactor.java:214)
                at org.jvnet.hudson.reactor.Reactor$Node.run(Reactor.java:117)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
                at java.lang.Thread.run(Thread.java:748)
Caused: org.jvnet.hudson.reactor.ReactorException
                at org.jvnet.hudson.reactor.Reactor.execute(Reactor.java:282)
                at jenkins.InitReactorRunner.run(InitReactorRunner.java:48)
                at jenkins.model.Jenkins.executeReactor(Jenkins.java:1129)
                at jenkins.model.Jenkins.<init>(Jenkins.java:936)
                at hudson.model.Hudson.<init>(Hudson.java:85)
                at hudson.model.Hudson.<init>(Hudson.java:81)
                at hudson.WebAppMain$3.run(WebAppMain.java:233)
Caused: hudson.util.HudsonFailedToLoad
                at hudson.WebAppMain$3.run(WebAppMain.java:250)

https://issues.jenkins-ci.org/browse/JENKINS-21163
java.lang.NullPointerException
                at hudson.plugins.git.GitSCM.onLoaded(GitSCM.java:1895)
Caused: java.lang.reflect.InvocationTargetException
                at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
                at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
                at java.lang.reflect.Method.invoke(Method.java:498)
                at hudson.init.TaskMethodFinder.invoke(TaskMethodFinder.java:104)
Caused: java.lang.Error
                at hudson.init.TaskMethodFinder.invoke(TaskMethodFinder.java:110)
                at hudson.init.TaskMethodFinder$TaskImpl.run(TaskMethodFinder.java:175)
                at org.jvnet.hudson.reactor.Reactor.runTask(Reactor.java:296)
                at jenkins.model.Jenkins$5.runTask(Jenkins.java:1095)
                at org.jvnet.hudson.reactor.Reactor$2.run(Reactor.java:214)
                at org.jvnet.hudson.reactor.Reactor$Node.run(Reactor.java:117)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
                at java.lang.Thread.run(Thread.java:748)
Caused: org.jvnet.hudson.reactor.ReactorException
                at org.jvnet.hudson.reactor.Reactor.execute(Reactor.java:282)
                at jenkins.InitReactorRunner.run(InitReactorRunner.java:48)
                at jenkins.model.Jenkins.executeReactor(Jenkins.java:1129)
                at jenkins.model.Jenkins.<init>(Jenkins.java:936)
                at hudson.model.Hudson.<init>(Hudson.java:85)
                at hudson.model.Hudson.<init>(Hudson.java:81)
                at hudson.WebAppMain$3.run(WebAppMain.java:233)
Caused: hudson.util.HudsonFailedToLoad
                at hudson.WebAppMain$3.run(WebAppMain.java:250)
 
 It had to revert the plugin manually with /plugin/uno-choice.bak, 
 beacause the servers were not more accessible via Jenkins UI.


# No tickets UI in the project (only accessible in the sub-sub-parent)

--> How to make it difficult to report errors, regressions. 

# Intro

A Jenkins **UI plugin for selecting one or multiple options for a job parameter**. It provides a number of capabilities
in a single plugin some, but not all, of which can be found amongst several other plugins. This project was previously
called Uno Choice Plug-in, while under the BioUno project.

As of July 2014 the plugin supports:

1. Selecting one or multiple options for a parameter
2. Combo-box, check-box and radio button UI
3. Dynamic generation of option values from a groovy command or Scriptler script
4. Cascading updates when job form parameters change
5. Displaying reference parameters (a new type of Jenkins UI parameter) which are dynamically generated, support
cascading updates, and are displayed as a variety of HTML formatted elements on the job form.
 
This plugin is developed in support of the diverse computational requirements of life-science Jenkins applications as
proposed by the [BioUno project](http://biouno.org/).

Many parts of the code may be adapted from existing plug-ins. We ask to keep issues related to this plug-in in
Jenkins JIRA.

Visit the [plug-in Wiki](https://wiki.jenkins-ci.org/display/JENKINS/Active+Choices+Plugin) for more details on each
parameter type.

# Building

If you have phantomjs in your $PATH, or if you edit pom.xml, you can run the JavaScript tests too with QUnit. It is executed with the Maven Exec Plug-in, but if you don't have phantomjs installed, the plug-in will simply ignore it and run only the Java tests.

`mvn clean test install`

# License

This plug-in is licensed under the MIT License. Parts of this plug-in have been adapted from existing plug-ins
with compatible licenses (e.g.: Apache License).
