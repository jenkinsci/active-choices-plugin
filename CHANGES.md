# Active Choices plug-in changelog

## Version 2.8.0 (2023/10/??)

1. [#80](https://github.com/jenkinsci/active-choices-plugin/pull/80): Add `@Symbol` to support declarative pipelines (thanks to @mamh2021, original submitted in #43)

## Version 2.7.2 (2023/08/17)

1. [#85](https://github.com/jenkinsci/active-choices-plugin/pull/85): fix: When configured using a pipeline, handle null filterable #85, thanks @rahulsom

## Version 2.7.1 (2023/08/03)

1. Release failed.

## Version 2.7.0 (2023/07/23)

1. [#69](https://github.com/jenkinsci/active-choices-plugin/pull/69): Upgrade jQuery from 1.x to 3.x #69, thanks @basil
2. [#72](https://github.com/jenkinsci/active-choices-plugin/pull/72): Modernize JS - use $ instead of jQuery, template strings, let instead of var, and arrow functions, thanks @rahulsom
3. [#73](https://github.com/jenkinsci/active-choices-plugin/pull/73): Build container agents, thanks to @NotMyFault
4. [#71](https://github.com/jenkinsci/active-choices-plugin/pull/71): test: Add end-to-end tests for UI behavior, thanks to @rahulsom
5. [#74](https://github.com/jenkinsci/active-choices-plugin/pull/74): refactor: Extract helper functions and drop table-layout support, thanks @rahulsom
6. [#78](https://github.com/jenkinsci/active-choices-plugin/pull/78): Refresh plugin for June 2023, thanks @basil
7. [#75](https://github.com/jenkinsci/active-choices-plugin/pull/75): Add test for pure javascript code, thanks @rahulsom
8. [#76](https://github.com/jenkinsci/active-choices-plugin/pull/76): Move (most) javascript out of jelly files, thanks @rahulsom
9. [#81](https://github.com/jenkinsci/active-choices-plugin/pull/81): Use @LocalData to set up test job, thanks @basil
10. [#82](https://github.com/jenkinsci/active-choices-plugin/pull/82): Bump semver from 5.7.1 to 5.7.2, thanks @dependabot
11. [#83](https://github.com/jenkinsci/active-choices-plugin/pull/83): Bump word-wrap from 1.2.3 to 1.2.4, thanks @dependabot
12. [#84](https://github.com/jenkinsci/active-choices-plugin/pull/84): Remove outdated Jenkins version test conditional, thanks @MarkEWaite
12. [#79](https://github.com/jenkinsci/active-choices-plugin/pull/79): refactor: Replace synchronous proxy with standard proxy @rahulsom

## Version 2.6.5 (2023/04/18)

1. [#65](https://github.com/jenkinsci/active-choices-plugin/pull/65): Refresh plugin (incremental builds, remove unused spotbugs exclusion file), thanks @basil
2. [JENKINS-65235](https://issues.jenkins.io/browse/JENKINS-65235): groovy.lang.MissingPropertyException: No such property: jenkinsProject for class: groovy.lang.Binding (thanks @davidecavestro)
3. [#68](https://github.com/jenkinsci/active-choices-plugin/pull/68): Bump nodelabelparameter from 1.9.2 to 1.10.3.1 #68

## Version 2.6.4 (2022/08/30)

1. [JENKINS-69448](https://issues.jenkins.io/browse/JENKINS-69448): Groovy script returning a Map is not handled properly with timer, thanks to @ericcitaire
2. [JENKINS-67982](https://issues.jenkins.io/browse/JENKINS-67982): Active Choice filtering not working for checkboxes and radio options
3. [JENKINS-66703](https://issues.jenkins.io/browse/JENKINS-66703): Filter on labelled checkbox choices throws a Javascript exception (duplicate of JENKINS-67982) 

## Version 2.6.3 (2022/07/13)

1. [JENKINS-68013](https://issues.jenkins.io/browse/JENKINS-68013): Active Choices Reactive Reference Parameter is not referring Boolean parameter in Jenkins. The fix released in the previous release did not work for all the users of the plug-in with boolean parameters. Thanks to @piecko and Jeremy Cooper for the fix.

## Version 2.6.2 (2022/06/04)

The `pom.xml` had dependencies updated (such as Scriptler, Node Labels Plug-in, Jenkins parent, etc. Newer versions of Jenkins have changes in the UI, including DOM hierarchy changes, that cause the plug-in to fail to locate certain parameter types. We fixed one parameter type (Boolean) in this release, but others may still suffer from the same problem. If you find an issue similar to JENKINS-68013, please consider using it as reference to create a new pull request.

1. [JENKINS-68013](https://issues.jenkins.io/browse/JENKINS-68013): Active Choices Reactive Reference Parameter is not referring Boolean parameter in Jenkins

## Version 2.6.1 (2022/03/18)

1. [JENKINS-67934](https://issues.jenkins.io/browse/JENKINS-67934): Regression with pipelines, identified by @mtughan (thanks!). See the [pull request with the fix](https://github.com/jenkinsci/active-choices-plugin/pull/59) for more.

## Version 2.6.0 (2022/02/27)

1. [JENKINS-63983](https://issues.jenkins.io/browse/JENKINS-63983): Active Choice Plugin - Annotation Grapes cannot be used in the sandbox (thanks to @rafeeqpv)
2. [JENKINS-66411](https://issues.jenkins.io/browse/JENKINS-66411): Add checkbox to enable/disable Scriptler scripts, and remove duplicated Scriptler code (use a property in Jelly).
3. [JENKINS-62835](https://issues.jenkins.io/browse/JENKINS-62835): Node and Label Parameter not Compatible with Active Choices
4. Bump scriptler from 3.1 to 3.3, and antisamy-markup-formatter from 2.0 to 2.1 (maven enforcer issue) [#58](https://github.com/jenkinsci/active-choices-plugin/pull/58)
5. [JENKINS-66411](https://issues.jenkins.io/browse/JENKINS-66411) Use CSS flex for parameter div layout

## Version 2.5.7 (2021-11-21)

1. [JENKINS-63983](https://issues.jenkins.io/browse/JENKINS-63983) Use sandbox mode only when Script has not been approved.
2. [190e0a101d66d030d29da860dc8cde5253ca1a12](https://github.com/jenkinsci/active-choices-plugin/commit/190e0a101d66d030d29da860dc8cde5253ca1a12) Use plugin 4.18. Use Jenkins version 2.222.4
3. [761a378e000ef616105bf1d400d2b104df4e7f12](https://github.com/jenkinsci/active-choices-plugin/commit/761a378e000ef616105bf1d400d2b104df4e7f12) Use same versions as git-plugin
4. [SECURITY-2219](https://www.jenkins.io/security/advisory/2021-11-12/#descriptions): Active Choices Plugin 2.5.6 and earlier does not escape the parameter name of reactive parameters and dynamic reference parameters.
5. [JENKINS-66806](https://issues.jenkins.io/browse/JENKINS-63983) Add back images using the WayBack machine images to the README.md/plug-in site
6. [d59a284e653d60b5b5eadab4e015f04ab1fb0606](https://github.com/jenkinsci/active-choices-plugin/commit/d59a284e653d60b5b5eadab4e015f04ab1fb0606) Add a `mvn` default goal
7. [JENKINS-62835](https://issues.jenkins.io/browse/JENKINS-62835) Node and Label Parameter not Compatible with Active Choices
8. [JENKINS-66411](https://issues.jenkins.io/browse/JENKINS-66411) Add a checkbox to configure whether a script is sandboxed or not

## Version 2.5.6 (2021/03/25)

1. [JENKINS-65096](https://issues.jenkins.io/browse/JENKINS-65096): Additional fix to support both DIVs and TABLEs (thanks to @szjozsef)
2. [JENKINS-65173](https://issues.jenkins.io/browse/JENKINS-65173): Hide parameter name when the parameter is hidden (as before 2.277.1)

## Version 2.5.5 (2021/03/13)

1. [JENKINS-65096](https://issues.jenkins.io/browse/JENKINS-65096): Fix Active Choice Reactive Parameter rendering with Jenkins 2.277.1 (thanks @szjozsef)

## Version 2.5.4 (2021/02/28)

1. [JENKINS-64962](https://issues.jenkins.io/browse/JENKINS-64962): Fix radio buttons and check boxes not being displayed. The height in CSS was calculated with JS to 0 (zero). That was due to recent workaround for table-to-divs (JENKINS-62806). We may have to inspect more of the code that was changed, and confirm the table-to-divs is working as expected. This fix was released quickly to give users a fix ASAP.

## Version 2.5.3 (2021/02/24)

1. [SECURITY-2192](https://www.jenkins.io/security/advisory/2021-02-24/#SECURITY-2192): Fix XSS vulnerability

## Version 2.5.2 (2021/02/21)

1. [JENKINS-62806](https://issues.jenkins.io/browse/JENKINS-62806): active-choices plugin may break with tables-to-divs.

## Version 2.5.1 (2020/10/17)

1. [JENKINS-63963](https://issues.jenkins-ci.org/browse/JENKINS-63963): Groovy-backed cascade selects lose sorting/option order after 2.5 upgrade.

## Version 2.5 (2020/10/13)

1. [JENKINS-63284](https://issues.jenkins-ci.org/browse/JENKINS-63284): add note to README about pipelines support
2. [SECURITY-1954 - CVE-2020-2289](https://www.jenkins.io/security/advisory/2020-10-08/): Active Choices Plugin 2.4 and earlier does not escape the name and description of build parameters. This results in a stored cross-site scripting (XSS) vulnerability exploitable by attackers with Job/Configure permission. Active Choices Plugin 2.5 escapes the name of build parameters and applies the configured markup formatter to the description of build parameters.
3. [SECURITY-2008 - CVE-2020-2290 ](https://www.jenkins.io/security/advisory/2020-10-08/): Active Choices Plugin 2.4 and earlier does not escape List and Map return values of sandboxed scripts for Reactive Reference Parameter. This results in a stored cross-site scripting (XSS) vulnerability exploitable by attackers with Job/Configure permission. This issue is caused by an incomplete fix for SECURITY-470. Active Choices Plugin 2.5 escapes all legal return values of sandboxed scripts.
4. [pr/38](https://github.com/jenkinsci/active-choices-plugin/pull/38): Provide Scriptler example in README.md (thanks to mettacrawler)

## Version 2.4 (2020/07/09)

1. [JENKINS-62215](https://issues.jenkins-ci.org/browse/JENKINS-62215): antisamy-markup-formatter-plugin v2.0 filters input fields from uno-choice plugin. In this version we have added a built-in markup formatter in the plug-in (NB: not available in Jenkins as a markup formatter) that allows for input, textarea, select, and option HTML tags). The issue was found and fixed by Andrew Potter @apottere (thanks!)

## Version 2.3 (2020/05/16)

1. [JENKINS-61068](https://issues.jenkins-ci.org/browse/JENKINS-61068): Active Choices radio parameter has incorrect default value on parambuild URL. Fixed by Adam Gabryś in [pr/32](https://github.com/jenkinsci/active-choices-plugin/pull/32) (thanks!).
2. [JENKINS-61751](https://issues.jenkins-ci.org/browse/JENKINS-61751): let :disabled and :deleted at the same (thanks to @ivarmu)
3. [JENKINS-62317](https://issues.jenkins-ci.org/browse/JENKINS-62317): Upgrade dependencies pre 2.3 release (Jenkins LTS 2.204 now, Java 8, script-security 1.72, antisamy-markup-formatter 2.0, no more powermockito in tests, fixing spot bugs issues)
4. [JENKINS-39742](https://issues.jenkins-ci.org/browse/JENKINS-39742): Active Choices Plugin should honor ParameterDefinition serializability (was: Active Choices Plugin in Pipelines throw NotSerializableException)
5. [JENKINS-61243](https://issues.jenkins-ci.org/browse/JENKINS-61243): Artifactory plugin to fail active-choice plugin

## Version 2.2 (2019/09/13)

1. Updated the minimum version of Jenkins to 2.60.3
2. Updated dependency ssh-cli-auth to 1.4,
3. Updated dependency script-security to 1.39
4. Updated stapler-adjunct-jquery to 1.12.4-0
5. [JENKINS-51296](https://issues.jenkins-ci.org/browse/JENKINS-51296): Project renaming is not propagated to Active Choices projectName (thanks to sebcworks)
6. [JENKINS-39665](https://issues.jenkins-ci.org/browse/JENKINS-39665): Unnecessary Scrollbars on build with parameters screen when using Active Choices Plugin (thanks to Lubomir Stanko)
7. [JENKINS-53356](https://issues.jenkins-ci.org/browse/JENKINS-53356): Scriptlet 'ViewScript' link and Required Parameters not displayed in configuration
8. Fixed javadocs issues, missing license headers, simplified if expressions, and other minor code improvements
9. [Pull Request#25](https://github.com/jenkinsci/active-choices-plugin/pull/25): Initial version. Allow to disable any select option with :disable (thanks to Ivan Aragonés Muniesa)

## Version 2.1.1 (2018/08/25)

1. [JENKINS-49260](https://issues.jenkins-ci.org/browse/JENKINS-49260): this.binding.jenkinsProject not returning project of current build
2. [JENKINS-51296](https://issues.jenkins-ci.org/browse/JENKINS-51296): Project renaming is not propagated to Active Choices projectName
3. Updates to dependencies from Jenkins and plugins, in pom.xml 

## Version 2.1 (2018/01/01)

1. [JENKINS-48230](https://issues.jenkins-ci.org/browse/JENKINS-48230): Discover parameters on jobs' wrappers
2. [JENKINS-47908](https://issues.jenkins-ci.org/browse/JENKINS-47908): After upgrade to v2.0, the active choices updates randomly in the browser
3. [JENKINS-48448](https://issues.jenkins-ci.org/browse/JENKINS-48448): Add a variable with the parameter name
4. [JENKINS-43380](https://issues.jenkins-ci.org/browse/JENKINS-43380): Input parameter HTML description not working 

## Version 2.0 (2017/10/23)

1. [Fix security vulnerability](https://jenkins.io/security/advisory/2017-10-23/)
  1. **Important:** **Sandboxed** Groovy scripts for **Active Choices
    Reactive Reference Parameter** will no longer emit HTML that is
    considered unsafe, such as <script> tags. This may result in
    behavior changes on *Build With Parameters* forms, such as
    missing elements. To resolve this issue, Groovy scripts emitting
    HTML will need to be configured to run outside the script
    security sandbox, possibly requiring separate administrator
    approval in *In-Process Script Approval*.
2. [JENKINS-42685](https://issues.jenkins-ci.org/browse/JENKINS-42685): Remove custom stapler proxy as we can now use async requests
3. [JENKINS-31625](https://issues.jenkins-ci.org/browse/JENKINS-31625): Add configuration parameter, which defines, when filter must started work
4. [JENKINS-36158](https://issues.jenkins-ci.org/browse/JENKINS-36158): Active Choices reactive reference parameter not working on checkbox reference
5. [JENKINS-43380](https://issues.jenkins-ci.org/browse/JENKINS-43380): Input parameter HTML description not working
6. [Pull request #15](https://github.com/jenkinsci/active-choices-plugin/pull/15): Make Scriptler dependency optional (thanks to Daniel Beck) 

## Version 1.5.3 (2017/03/11)

1. [JENKINS-34487](https://issues.jenkins-ci.org/browse/JENKINS-34487): Do not hang the browser when parameter re-evaluated (thanks to Vladimir Fedorov)
2. [JENKINS-38532](https://issues.jenkins-ci.org/browse/JENKINS-38532): Active Choices Reactive Reference Parameters don't parse equals character ('=') properly.
3. [JENKINS-34188](https://issues.jenkins-ci.org/browse/JENKINS-34188): jenkins.log noise: "script parameter ... is not an instance of Java.util.Map."

## Version 1.5.2 (2016/11/16)

1. [JENKINS-39620](https://issues.jenkins-ci.org/browse/JENKINS-39620): Saving a job with Active Choices 1.4 parameters after upgrade to v1.5 resets scriptlet parameters
2. [JENKINS-39760](https://issues.jenkins-ci.org/browse/JENKINS-39760): Active Choices Parameters lost of Job Config save

## Version 1.5.1 (2016/11/11)

1. [JENKINS-36590](https://issues.jenkins-ci.org/browse/JENKINS-36590): Active-Choice jenkinsProject variable is not available under Folder or Multibranch-Multiconfiguration job
2. [JENKINS-37027](https://issues.jenkins-ci.org/browse/JENKINS-37027): 'View selected script option' in build configuration displays wrong scriptler script
3. [JENKINS-34988](https://issues.jenkins-ci.org/browse/JENKINS-34988): this.binding.jenkinsProject not returning project of current build
4. Upgraded build plug-ins
5. Fixed Findbugs issues
6. Upgraded parent in order to be able to release to Jenkins plug-in repositories
7. [JENKINS-39620](https://issues.jenkins-ci.org/browse/JENKINS-39620): Saving a job with Active Choices 1.4 parameters after upgrade to v1.5 resets scriptlet parameters
8. [JENKINS-39534](https://issues.jenkins-ci.org/browse/JENKINS-39534): When Jenkins restarts, the groovy scripts are lost
9. [JENKINS-34818](https://issues.jenkins-ci.org/browse/JENKINS-34818): Active Choices reactive parameter cannot access global parameters

## Version 1.5.0 (2016/11/04)

1. Removed from the update center. Read more about it [here](http://biouno.org/2016/11/11/what-happens-when-you-make-a-java-member-variable-transient-in-a-jenkins-plugin)

## Version 1.4 (2016/02/14)

1. [JENKINS-32405](https://issues.jenkins-ci.org/browse/JENKINS-32405): Groovy script failed to run if build started by timer
2. [JENKINS-32461](https://issues.jenkins-ci.org/browse/JENKINS-32461): jenkinsProject variable is not available in Multi-configuration project
3. [JENKINS-32566](https://issues.jenkins-ci.org/browse/JENKINS-32566): Render an AC Reactive reference as a functional Jenkins File Parameter

## Version 1.3 (2015/12/24)

1. [pull-request/6](https://github.com/jenkinsci/active-choices-plugin/pull/6): Let "PhantomJS Unit Testing" be able to run on both linux and windows...
2. [JENKINS-30824](https://issues.jenkins-ci.org/browse/JENKINS-30824): Active Choices Reactive Parameter - radios button value not passed to build (thanks to lyen liang for his PR showing how to fix it
3. [JENKINS-32044](https://issues.jenkins-ci.org/browse/JENKINS-32044): Fail to evaluate Boolean parameter to "on" when checked
4. [JENKINS-30592](https://issues.jenkins-ci.org/browse/JENKINS-30592): An AC Reactive Reference is always displayed unless it references a parameter
5. [JENKINS-32149](https://issues.jenkins-ci.org/browse/JENKINS-32149): Create random parameter name only once
6. [JENKINS-29476](https://issues.jenkins-ci.org/browse/JENKINS-29476): The 'jenkinsProject' variable is not set in the binding after restarting Jenkins (thanks to @perfhector for PR/5)

## Version 1.2 (2015/07/22)

1. [pull-request/1](https://github.com/jenkinsci/active-choices-plugin/pull/1): Use value of Reactive Reference dynamic input controls as build parameters. See [example](https://wiki.jenkins-ci.org/display/JENKINS/Reactive+Reference+Dynamic+Parameter+Controls)
2. [pull-request/2](https://github.com/jenkinsci/active-choices-plugin/pull/2): Update unochoice.js

## Version 1.1 (2015/06/28)

1. [JENKINS-29055](https://issues.jenkins-ci.org/browse/JENKINS-29055): Problem with Active Choices 1.0 and jQuery
2. [JENKINS-28764](https://issues.jenkins-ci.org/browse/JENKINS-28764): Environment variables are not expanded and thus cannot be used as parameter values to Scriptler scripts
3. [JENKINS-28785](https://issues.jenkins-ci.org/browse/JENKINS-28785): Referenced Parameters Not Passed to Scriptler Scripts

## Version 1.0 (2015/06/03)

1. Initial release to Jenkins update center, with Choice, Cascade Dynamic Choice and Dynamic Reference parameter types.

## Versions released to BioUno update center

The first commit happened on 2014/03/13, and the initial release on 2014/03/21. There were 24 releases to the [BioUno update center](http://biouno.org/jenkins-update-site.html), the last one being on 2015/03/26, when the project decided to publish it via Jenkins update center.
