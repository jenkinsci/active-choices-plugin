# Active Choices plug-in changelog

## Version 2.8.9 (20??/??/??)

- Bump @babel/cli from 7.27.0 to 7.28.0
- Bump @babel/core from 7.26.10 to 7.27.7
- Bump @babel/preset-env from 7.26.9 to 7.28.0
- Bump @babel/preset-flow from 7.25.9 to 7.27.1
- Bump @babel/preset-typescript from 7.27.0 to 7.27.1
- Bump eslint from 9.25.1 to 9.30.0
- Bump eslint-config-prettier from 10.1.2 to 10.1.5
- Bump io.github.bonigarcia:webdrivermanager from 6.1.0 to 6.1.1
- Bump io.jenkins.tools.bom:bom-2.479.x from 4669.v0e99c712a_30e to 4969.v6ffa_18d90c9f
- Bump io.jenkins.tools.incrementals:git-changelist-maven-extension from 1.8 to 1.10
- Bump jest from 29.7.0 to 30.0.4
- Bump jest-environment-jsdom from 29.7.0 to 30.0.4
- Bump org.jenkins-ci.plugins:job-dsl from 1.92 to 1.93
- Bump org.jenkins-ci.plugins:plugin from 5.12 to 5.18
- Bump org.seleniumhq.selenium:selenium-java from 4.31.0 to 4.34.0
- Bump prettier from 3.5.3 to 3.6.2
- Bump webpack from 5.99.7 to 5.100.1
- JENKINS-72282: Apply Jenkins styling to selects, radios and checkboxes (thanks @mawinter69) #429

## Version 2.8.8 (2025/05/04)

- Bump eslint from 9.24.0 to 9.25.1
- Bump io.github.bonigarcia:webdrivermanager from 6.0.1 to 6.1.0
- Bump io.jenkins.tools.bom:bom-2.479.x from 4607.v67a_4791074d7 to 4669.v0e99c712a_30e
- Bump org.jenkins-ci.plugins:plugin from 5.10 to 5.12
- Bump webpack from 5.99.5 to 5.99.7
- JENKINS-75596: Not loading Reactive Reference Parameter in Safari (copied polyfill code)

## Version 2.8.7 (2025/04/19)

- Bump @babel/cli from 7.25.9 to 7.27.0
- Bump @babel/core from 7.26.0 to 7.26.10
- Bump @babel/preset-env from 7.26.0 to 7.26.9
- Bump @babel/preset-typescript from 7.26.0 to 7.27.0
- Bump babel-loader from 9.2.1 to 10.0.0
- Bump eslint from 9.15.0 to 9.24.0
- Bump eslint-config-prettier from 9.1.0 to 10.1.2
- Bump io.github.bonigarcia:webdrivermanager from 5.9.2 to 6.0.1
- Bump io.jenkins.tools.bom:bom-2.479.x from 3559.vb_5b_81183b_d23 to 4607.v67a_4791074d7
- Bump jsdom from 25.0.1 to 26.1.0
- Bump org.jenkins-ci.plugins:job-dsl from 1.90 to 1.92
- Bump org.jenkins-ci.plugins:nodelabelparameter from 1.13.0 to 759.vb_b_e95db_f3251
- Bump org.jenkins-ci.plugins:plugin from 5.3 to 5.10
- Bump org.jenkins-ci.plugins:scriptler from 376.v152edd95b_ca_f to 397.vc46f19cb_3c18
- Bump org.jenkins-ci.plugins.workflow:workflow-aggregator from 600.vb_57cdd26fdd7 to 608.v67378e9d3db_1
- Bump org.seleniumhq.selenium:selenium-java from 4.26.0 to 4.31.0
- Bump prettier from 3.3.3 to 3.5.3
- Bump serialize-javascript from 6.0.1 to 6.0.2
- Bump ts-loader from 9.5.1 to 9.5.2
- Bump typescript from 5.6.3 to 5.8.3
- Bump webpack from 5.96.1 to 5.99.5
- Bump webpack-cli from 5.1.4 to 6.0.1
- Implement Describable from Script, not AbstractScript (thanks @jglick)
- Migrate from EE 8 to EE 9 (thanks @basil)
- Migrate from Acegi to Spring Security (thanks, again, @basil)
- Migrate tests to JUnit5 (thanks to @strangelookingnerd)
- Move js script to webapp folder to allow loading map files by the browser (thanks to @mawinter69)
- Upgrade node requirement from 18.18.0 to 22.14.0
- JENKINS-69016: This issue could not be reproduced in this version, possibly fixed by JENKINS-73239, JENKINS-75194, JENKINS-72826
- JENKINS-70380: This issue could not be reproduced in this version, possibly fixed by JENKINS-73239, JENKINS-75194, JENKINS-72826
- JENKINS-72129: Filtering parameter by a value that matches exactly one entry, results in incorrect result. This issue could not be
  reproduced in this version, possibly fixed by JENKINS-74967, JENKINS-74967, or JENKINS-75194/JENKINS-72826.
- JENKINS-72549: The [CloudBees tutorial](https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/client-and-managed-controllers/how-to-create-a-custom-ui-component-for-the-active-choices-plugin)
  for creating UI components with Active Choices is failing. Note: there is no pull request as this issue could not
  be reproduced with the latest code for 2.8.7. This issue has been marked as fixed but unreleased, but can be re-opened
  if users verify 2.8.7 is causing a similar issue in their environments.
- JENKINS-73210: Parameter values not displayed when references are null; note: there is no pull request linked
  to the JIRA issue, as it was likely fixed by another change in JS or Java code associated to another JIRA issue,
  e.g. JENKINS-75194/JENKINS-72826, or JENKINS-72129.
- JENKINS-73239: Added test to prevent JS errors when a cascade parameter does not reference other parameters
  (the issue itself was closed as will not implement, due to Jenkins security that prevents loading external JS files)
- JENKINS-73899: This issue could not be reproduced during the 2.8.7 milestone development, but
  a user with a similar issue/error reported that upgrading to 2.8.7 fixed their issue, so this was
  added to the changelog after the release, for completeness.
- JENKINS-73922: Starting from 2.8.4 I get "Cannot read properties of null (reading 'toString')". Note that there is
  no pull request associated with this issue, as it could not be reproduced with the latest code. It has probably been
  fixed in JENKINS-75194/JENKINS-72826, or JENKINS-72129.
- JENKINS-74963: Spinning animation is not removed due to exception, select element is not found by plug-in
  (duplicated issues: JENKINS-73919, JENKINS-73928, JENKINS-72949, JENKINS-73022, JENKINS-73118, JENKINS-73215)
- JENKINS-74967: Active choice parameter filter throws JavaScript error for parameter change event in browser console
- JENKINS-75017: Dropdowns not working with keyboard inputs in Firefox on Windows - Jenkins version 2.479.2;
  note: there is no pull request linked to the JIRA issue, as it was likely fixed by another change in JS or
  Java code associated to another JIRA issue.
- JENKINS-75194, JENKINS-72826: Use window.requestIdleCallback instead of setTimeout to force the browser to update parameters 
  in order, waiting for DOM to be updated (partially addresses JENKINS-75194, duplicated issues: JENKINS-75194)
- JENKINS-75434: DynamicReferenceParameters do not work properly after 'Active Choices Plug-in' update at Jenkins Pipeline Jobs;
  note: there is no pull request linked to the JIRA issue, as it was likely fixed by another change in JS or
  Java code associated to another JIRA issue (possibly fixed by JENKINS-75194/JENKINS-72826)

## Version 2.8.6 (2024/11/23)

- Bump @eslint/plugin-kit from 0.2.0 to 0.2.3
- Bump @types/jquery from 3.5.31 to 3.5.32
- Bump cross-spawn from 7.0.3 to 7.0.5
- Bump eslint from 9.13.0 to 9.15.0
- Bump org.jenkins-ci.plugins:job-dsl from 1.89 to 1.90
- Bump webpack from 5.95.0 to 5.96.1
- JENKINS-74029: Extract inline JavaScript from radioContent.jelly (thanks @yaroslavafenkin)
- JENKINS-74026: Improve CSP compatibility (thanks @yaroslavafenkin)
- JENKINS-74027: Improve CSP compatibility (thanks @yaroslavafenkin)
- JENKINS-74025: Extract inline JavaScript from checkboxContent.jelly (thanks @yaroslavafenkin)

## Version 2.8.5 (2024/11/02)

- Bump @babel/cli from 7.25.6 to 7.25.9
- Bump @babel/core from 7.25.2 to 7.26.0
- Bump @babel/preset-env from 7.25.4 to 7.26.0
- Bump @babel/preset-flow from 7.24.7 to 7.25.9
- Bump @babel/preset-typescript from 7.24.7 to 7.26.0
- Bump eslint from 9.11.1 to 9.13.0
- Bump io.jenkins.tools.bom:bom-2.462.x from 3559.vb_5b_81183b_d23 (set jenkins version to 2.462.3+)
- Bump org.jenkins-ci.plugins:nodelabelparameter from 1.12.0 to 1.13.0
- Bump org.jenkins-ci.plugins:plugin from 4.88 to 5.2
- Bump org.jenkins-ci.plugins:scriptler from 363.vd97ef616cb_f9 to 376.v152edd95b_ca_f
- Bump org.seleniumhq.selenium:selenium-java from 4.25.0 to 4.26.0
- Bump typescript from 5.6.2 to 5.6.3
- Fix IDE warnings and disable pipeline for Windows (broken selenium WebDriver?) PR #350
- JENKINS-74028: Extract inline script from choiceParameterCommon.jelly (thanks @yaroslavafenkin)
- JENKINS-74030: Extract inline script from ChoiceParameter/index.jelly (thanks @yaroslavafenkin)
- Require 2.479.1 LTS or newer #369 (thanks @basil)

## Version 2.8.4 (2024/10/06)

- Bump @babel/cli from 7.24.1 to 7.25.6
- Bump @babel/core from 7.24.4 to 7.25.2
- Bump @babel/preset-env from 7.24.4 to 7.25.4
- Bump @babel/preset-flow from 7.24.1 to 7.24.7
- Bump @babel/preset-typescript from 7.23.3 to 7.24.7
- Bump @babel/core from 7.24.0 to 7.24.4
- Bump @types/jquery from 3.5.29 to 3.5.31
- Bump babel-loader from 9.1.3 to 9.2.1
- Bump braces from 3.0.2 to 3.0.3
- Bump eslint from 8.57.0 to 9.11.1
- Bump io.github.bonigarcia:webdrivermanager from 5.7.0 to 5.9.2
- Bump io.jenkins.tools.bom:bom-2.426.x from 2815.vf5d6f093b_23e to 3413.v0d896b_76a_30d
- Bump io.jenkins.tools.incrementals:git-changelist-maven-extension from 1.7 to 1.8
- Bump Jenkins version to 2.426.3 (required by json.tools.bom change above)
- Bump jsdom from 24.0.0 to 25.0.1
- Bump micromatch from 4.0.5 to 4.0.8
- Bump org.jenkins-ci.plugins:job-dsl from 1.87 to 1.89
- Bump org.jenkins-ci.plugins:plugin from 4.80 to 4.88
- Bump org.jenkins-ci.plugins:scriptler from 348.v5d461e205da_a_ to 363.vd97ef616cb_f9
- Bump org.jenkins-ci.plugins.workflow:workflow-aggregator from 596.v8c21c963d92d to 600.vb_57cdd26fdd7
- Bump org.seleniumhq.selenium:selenium-java from 4.18.1 to 4.25.0
- Bump prettier from 3.2.5 to 3.3.3
- Bump typescript from 5.4.3 to 5.6.2
- Bump webpack from 5.91.0 to 5.95.0
- Bump ws from 8.17.0 to 8.17.1
- Reverted JENKINS-71365, pull request #79, making the Stapler proxy synchronous again, to ensure determinism in parameters resolution, fixing a regression
- Update pom.xml to switch from node 18.16 to 18.18 (for eslint 9)
- Update pom.xml to bump Jenkins version to Jenkins 2.462.2 (job-dsl requirement)

## Version 2.8.3 (2024/03/29)

- Replace EOL JSR 305 annotations with SpotBugs annotations, thanks @basil
- [JENKINS-72936]: Fix index.jelly for DynamicReferenceParameter, thanks @c3p0-maif

## Version 2.8.2 (2024/03/26)

- Bump @babel/core from 7.23.2 to 7.24.0
- Bump @babel/cli from 7.23.0 to 7.24.1
- Bump @babel/preset-env from 7.23.2 to 7.24.3
- Bump @babel/preset-flow from 7.22.15 to 7.24.1
- Bump @babel/preset-typescript from 7.23.2 to 7.23.3
- Bump @types/jquery from 3.5.25 to 3.5.29
- Bump eslint from 8.52.0 to 8.57.0
- Bump eslint-config-prettier from 9.0.0 to 9.1.0
- Bump io.jenkins.tools.bom:bom-2.387.x from 2516.v113cb_3d00317 to 2.426.x 2815.vf5d6f093b_23e
- Bump io.github.bonigarcia:webdrivermanager from 5.6.0 to 5.7.0
- Bump jsdom from 22.1.0 to 24.0.0
- Bump org.jenkins-ci.plugins:plugin from 4.75 to 4.80
- Bump org.jenkins-ci.plugins:scriptler from 334.v29792d5a_c058 to 348.v5d461e205da_a_
- Bump org.seleniumhq.selenium:selenium-java from 4.14.1 to 4.15.0
- Bump prettier from 3.0.3 to 3.2.5
- Bump org.seleniumhq.selenium:selenium-java from 4.15.0 to 4.18.1
- Bump release-drafter/release-drafter from 5 to 6
- Bump ts-loader from 9.5.0 to 9.5.1
- Bump typescript from 5.2.2 to 5.4.3
- Bump webpack from 5.89.0 to 5.91.0
- Test with jdk-17 on windows and jdk-21 on linux (thanks @debayangg)
- [JENKINS-72213] Add a waiting spinner during parameter building (thanks @c3p0-maif)

## Version 2.8.1 (2023/11/02)

1. [#147](https://github.com/jenkinsci/active-choices-plugin/pull/147): Use `Files.createTempDir` to make sure files are created with permissions that are not too permissive (in tests) (thanks to @caytec)
2. Bump @babel/preset-env from 7.22.20 to 7.23.2
3. Bump @babel/preset-typescript from 7.23.0 to 7.23.2
4. Fix badges on README.md, and update BOM
5. Bump @babel/core from 7.23.0 to 7.23.2
6. Bump webpack from 5.88.2 to 5.89.0
7. Bump org.seleniumhq.selenium:selenium-java from 4.13.0 to 4.14.1
8. Bump eslint from 8.51.0 to 8.52.0
9. Bump @types/jquery from 3.5.22 to 3.5.25
10. Bump org.jenkins-ci.plugins:scriptler from 321.v74a_851a_e7ed6 to 334.v29792d5a_c058
11. Add `@Deprecated` to methods deprecated (via Javadocs)
12. Add `commons-text-api` and `commons-lang3-api`, which are transitive dependencies of `jquery3-api`, but that without these in our `pom.xml` the build fails on `master` and pull requests.
13. Bump org.jenkins-ci.plugins:plugin from 4.74 to 4.75
14. Bump io.github.bonigarcia:webdrivermanager from 5.5.3 to 5.6.0
15. [JENKINS-72105] Fix scriptler script's parameter not persisted with JobDSL (thanks @limejuny !)

## Version 2.8.0 (2023/10/11)

1. [#80](https://github.com/jenkinsci/active-choices-plugin/pull/80): Add `@Symbol` to support declarative pipelines (thanks to @mamh2021, original submitted in #43)
2. [#124](https://github.com/jenkinsci/active-choices-plugin/pull/124): Allow developer control of parallel testing (thanks to @MarkEWaite)
3. Bumped Jenkins version from 2.375.3 to 2.387.3 so we can upgrade Scriptler too
4. Bump jest-environment-jsdom from 29.6.2 to 29.6.3
5. Bump jest from 29.6.2 to 29.6.3 
6. Bump eslint from 8.47.0 to 8.48.0
7. Bump jquery and @types/jquery
8. Bump jest-environment-jsdom from 29.6.3 to 29.6.4
9. Bump io.github.bonigarcia:webdrivermanager from 5.4.1 to 5.5.2
10. Bump @babel/preset-typescript from 7.22.5 to 7.22.11
11. Bump typescript from 5.1.6 to 5.2.2
12. Bump org.seleniumhq.selenium:selenium-java from 4.11.0 to 4.12.0
13. Bump jest from 29.6.3 to 29.6.4
14. Bump @babel/cli from 7.22.10 to 7.22.15
15. Bump io.github.bonigarcia:webdrivermanager from 5.5.2 to 5.5.3
16. Bump @babel/preset-flow from 7.22.5 to 7.22.15
17. Bump org.jenkins-ci.plugins:plugin from 4.72 to 4.73
18. Bump @types/jquery from 3.5.17 to 3.5.18
19. Bump @babel/preset-typescript from 7.22.11 to 7.22.15
20. Bump eslint from 8.48.0 to 8.49.0
21. Bump prettier from 3.0.2 to 3.0.3
22. Bump @babel/preset-env from 7.22.10 to 7.22.15
23. Bump org.seleniumhq.selenium:selenium-java from 4.12.0 to 4.12.1
24. Bump @types/jquery from 3.5.18 to 3.5.19
25. Bump jest from 29.6.4 to 29.7.0
26. Bump jest-environment-jsdom from 29.6.4 to 29.7.0
27. Bump @babel/core from 7.22.10 to 7.22.20
28. Bump @babel/preset-env from 7.22.15 to 7.22.20
29. Bump @babel/cli from 7.22.15 to 7.23.0
30. Bump @babel/core from 7.22.20 to 7.23.0
31. Bump @babel/preset-typescript from 7.22.15 to 7.23.0
32. Bump eslint from 8.49.0 to 8.50.0
33. Bump org.jenkins-ci.plugins:plugin from 4.73 to 4.74
34. Bump org.seleniumhq.selenium:selenium-java from 4.12.1 to 4.13.0
35. Bump @types/jquery from 3.5.19 to 3.5.20
36. Bump ts-loader from 9.4.4 to 9.5.0
37. Bump @types/jquery from 3.5.20 to 3.5.22
38. Bump eslint from 8.50.0 to 8.51.0
39. Bump org.jenkins-ci.plugins:scriptler from 3.5 to 321.v74a_851a_e7ed6
40. Bump org.jenkins-ci.plugins:nodelabelparameter from 1.10.3.1 to 1.12.0

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
13. [#79](https://github.com/jenkinsci/active-choices-plugin/pull/79): refactor: Replace synchronous proxy with standard proxy @rahulsom

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
  1.1. **Important:** **Sandboxed** Groovy scripts for **Active Choices
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
