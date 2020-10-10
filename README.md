## Overview

The Active Choices plugin is used in parametrized freestyle Jenkins jobs to create
**scripted, dynamic and interactive job parameters**. Active Choice **parameters**
can be **dynamically updated** and can be
**rendered as combo-boxes, check-boxes, radio-buttons or rich HTML UI widgets**.

Active Choice parameters are scripted using Groovy, or (optionally) Scriptler Groovy scripts.
These custom scripts support the use of the Jenkins Java API, system environment variables,
global node properties, and potentially external Java and Javascript libraries.

Once the plugin is installed three new parameter types become available:

1.  Active Choices Parameter
2.  Active Choices Reactive Parameter
3.  Active Choices Reactive Reference Parameter

> **_NOTE:_**  The Reactive Reference Parameter allows for parameters to be displayed as
>formatted HTML. When configuring jobs with this feature, keep in mind how the parameter
will be rendered and whether it could be a security issue.

> **_NOTE:_**  The plug-in was developed in a way that it relies heavily on the HTML/DOM
>of the Jenkins UI. We navigate the DOM using JavaScript to create the relationship and
>reactivity between parameters. Follow [JENKINS-63284](https://issues.jenkins-ci.org/browse/JENKINS-63284)
>for updates on a version that does not require the UI. When that issue is closed, the
>plug-in should work fine with Pipelines, DSL, timers, cron, REST-API-triggered jobs, etc.

Active Choices parameters allow users to select value(s) for a job
parameter. Parameter values can be:

-   dynamically generated (using Groovy or a
    [Scriptler](https://wiki.jenkins-ci.org/display/JENKINS/Scriptler+Plugin)
    script)
-   dynamically updated based on other UI parameters
-   multi-valued (can have more than one value)
-   rendered with a variety of UI controls, including dynamic HTML (see NOTE above on the security risks)

We will introduce the Active Choices based UI controls by briefly
describing their behavior and rendering characteristics. We will then
provide a guide to their configuration

## Active Choices Parameter

### Behavior

-   An Active Choices parameter dynamically generates a list of value
    options for a build parameter using a Groovy script or a  script
    from the Scriptler catalog.

### Rendering

-   Active Choices parameters can be rendered as standard selection
    lists, check boxes and radio buttons.
-   A text box filter can be optionally shown to aid in filtering the
    value options.
![](https://wiki.jenkins.io/download/attachments/74875908/Untitled_Clipping_050415_011719_PM.jpg?version=1&modificationDate=1430759870000&api=v2)

## Active Choices Reactive and Reactive Reference Parameters

Both of these parameters have additional useful behaviors and Reactive
Reference has some unique rendering options.

### Behavior

Similarly to Active Choices Parameter above:

-   Active Choices Reactive and Reactive Reference
    parameters dynamically generate value options for a build parameter
    using a Groovy script or a Scriptler script

In addition:

-   Active Choices Reactive and Reactive Reference parameters can be
    **dynamically updated** (cascade update) when the value of other job
    UI control(s) change(s)

### Rendering Options

#### Active Choices Reactive

-   Active Choices Reactive parameters can be rendered as standard
    selection lists, check boxes and radio buttons.
-   A text box filter can be optionally shown to aid in filtering the
    value options.

#### Active Choices Reactive Reference

Active Choices Reactive Reference parameters are used to enhance a
Jenkins job form UI with reference information.

With this use case in mind, a Reactive Reference UI control can be
rendered as:

-   An HTML list (bulleted or numbered)
-   An HTML input text box
-   Dynamically generated HTML (image, iframe, etc.);

The dynamically generated HTML option, works with any well-formatted
HTML returned by the Groovy script.It enables rendering of a variety of
HTML elements, including **pictures, inline-frames, hyperlinks, richly
formatted text** etc.

In addition, Reactive Reference parameters can be **hidden** from the UI
and thus provide the option of dynamically generating hidden build
parameters. These options are further discussed in the Reactive
Reference configuration section.

### Rendering Example

![](https://wiki.jenkins.io/download/attachments/74875908/ACRR_b03_param.gif?version=1&modificationDate=1435871630000&api=v2)

In the example above the value options for the 'Professions' parameter
get updated when the 'Gender' parameter changes.

In addition, the Reactive Reference parameter 'Gender\_Balance' rendered
as a picture is also dynamically updated when the 'Gender' parameter is
updated.

### Behavior and Rendering Summary

The following table summarizes the behavior and rendering
characteristics of the three Active Choices parameter types.

![](https://wiki.jenkins.io/download/attachments/74875908/Untitled_Clipping_050415_012955_PM.jpg?version=1&modificationDate=1430760601000&api=v2)

## Active Choices Parameter Type Configuration

The following parameters are included in the plug-in:

-   Active Choices Parameter
-   Active Choices Reactive Parameter
-   Active Choices Reactive Reference Parameter

We now describe the details of their configuration.

### Active Choices Parameter: Configuration Options (Example 01)

![](https://wiki.jenkins.io/download/attachments/74875908/AC_param.gif?version=2&modificationDate=1435866421000&api=v2)

An Active Choices Parameter is configured by setting the following
options in the parameter configuration

#### The 'Name' and 'Description'

These are the typical parameter Name and Description that are common to
all Jenkins parameters

#### The 'Script'

The 'Script' is the **Groovy code or Scriptlet script** that will
dynamically generate the parameter value options

-   By selecting either of the two radio button options you can either
    type a Groovy script directly or use a Scriptler script
-   The script must return a **java.util.List**, an **Array** or a
    **java.util.Map**, as in the example below:

    ``` syntaxhighlighter-pre
    return ['Option 1', 'Option 2', 'Option 3']
    ```

#### The 'Fallback Script'

The 'Fallback Script' configuration option provides alternate parameter
value options in case the main Script fails either by throwing an
Exception, or by not return a java.util.List, Array or java.util.Map.

#### The 'Choice Type'

The **'Choice Type**' option provides four different rendering options
for the option values:

1.  A list box where a single selection is allowed
2.  A list box where multiple selections are allowed
3.  A list of check boxes (multiple selections allowed)
4.  A list of radio buttons (a single selection is allowed)

#### The 'Enable Filter'

The '**Enable Filter**' option will provide a text box filter in the UI
control where a text filter can be typed. Only value options that
contain the text are then listed.

This filer is case independent.

### Active Choice Parameter Rendering (Example 01)

The Example 01 Active Choice parameter configuration generates the
following build form UI control. The user can select a single 'State'
option from a filterable drop down list.

![](https://wiki.jenkins.io/download/attachments/74875908/AC_b01_param.gif?version=1&modificationDate=1435867415000&api=v2)

#### Making 'Default' selections

It is possible to have some of the options displayed in an Active
Choices UI control selected by default when the control is initialized.

You can **define the default value selections** by adding the suffix;
**:selected** to the element you want to be the default selection from
those returned by the script.  
In the example below, we will make the State of 'Parana' the default
selection when the parameter UI control is rendered.

![](https://wiki.jenkins.io/download/attachments/74875908/AC_cb01_param.gif?version=1&modificationDate=1435868473000&api=v2)

#### Making 'Disabled' selections

You also can **define disabled selections** by adding the suffix;
**:disabled** to the element(s) you want to be disabled.
In the example below, we will make various elements to be disabled and
unmutable.

![](./docs/images/making-disabled-selections-pic.png)

As you can see, both **:selected** and **:disabled** can be specified at the same time.

We credit the developers of the [Dynamic Parameter
plugin](https://wiki.jenkins-ci.org/display/JENKINS/Dynamic+Parameter+Plug-in) with
some of the initial concepts and code on which Active Choices was
implemented. However, there are several important differences and
improvements between the  Active Choices plugin and the original Dynamic
Parameter plugin:

1.  An Active Choices parameter can be **rendered** as a multi-select
    control (combo-box or check-box) allowing users to **select more
    than one value** for the parameter 
2.  The parameter **options value list can be filtered**. If the
    "**Enable Filters**" option is checked, an extra input box will be
    displayed allowing users to filter the options.
3.  You can define a '**fallback**' behavior if the value generator
    script raises an exception.
4.  You can define **default value selections** in the dynamically
    generated value list

### Active Choices Reactive Parameter: Configuration Options (Example 02)

![](https://wiki.jenkins.io/download/attachments/74875908/ACR_c02_param.gif?version=1&modificationDate=1435869402000&api=v2)

An Active Choices Reactive parameter is configured with a set of similar
options as those shown above for the Active Choices parameter. However,
a Reactive parameter provides the additional '**Referenced
parameters**' configuration option.

-   This option, takes a list of job parameters that trigger an
    **auto-refresh of the Reactive Parameter when any of the 'Referenced
    parameters' change**

#### The 'Referenced parameters'

The '**Referenced parameters**' text field contains **a list of comma
separated parameter Names** (from the current job) that will trigger a
refresh of the Reactive Parameter when their values change. The values
of these parameters are passed to the script binding context before the
script is re-executed to generate a new set of option values for the
Active Choices control. 

### Active Choices Reactive Parameter Rendering (Example 02)

Let's examine a Jenkins build form rendered with Active Choice
parameters that satisfies the following requirements. The form:

-   Allows users to select one of several Brazilian States
-   Provides an additional control where a set of Cities belonging to
    the selected State is dynamically displayed
-   Allows the user to select one or more of the displayed Cities

![](https://wiki.jenkins.io/download/attachments/74875908/ACR_b02_param.gif?version=2&modificationDate=1435870506000&api=v2)

The job UI is composed of two parameters:

#### 1) States: An Active Choice Parameter

The first parameter is the **States** Active Choice Parameter from
Example 01. It allows the user to select one of several Brazilian
States.  
We could have just as easily used a Jenkins Choice Parameter, but we use
an Active Choice parameter (as shown from Example 01). The Groovy script
for this is:

``` syntaxhighlighter-pre
return[
'Sao Paulo',
'Rio de Janeiro',
'Parana',
'Acre'
]
```

#### 2) Cities: An Active Choices Reactive Parameter

The second parameter is the **Cities** Active Choices Reactive Parameter
that **dynamically displays** a set of cities belonging to the selected
State and allows users to select multiple values. The Cities parameter
configuration is shown above in Example 02.

Note that:

-   The Cities Reactive parameter references the previously defined
    States parameter ('Referenced parameters'=States);
-   The 'Choice Type' is set to Check Boxes.This will allow the user to
    select one or more Cities by selecting multiple check boxes.
-   A custom 'Groovy Script' will be used to generate the Cities value
    options as shown below (the last list value returned by the script)

``` syntaxhighlighter-pre
if (States.equals("Sao Paulo")) {
  return ["Barretos", "Sao Paulo", "Itu"]
} else if (States.equals("Rio de Janeiro")) {
  return ["Rio de Janeiro", "Mangaratiba"]
} else if (States.equals("Parana")) {
  return ["Curitiba", "Ponta Grossa"]
} else if (States.equals("Acre")) {
  return ["Rio Branco", "Acrelandia"]
} else {
  return ["Unknown state"]
}
```

Whenever the user changes the option of the States parameter, the Cities
parameter will get dynamically updated. Note how that the **States**
referenced parameter is in the script binding and can be used directly.

You can use a Reactive parameter type for things like displaying the
list of Maven artifacts, given a group ID.

### Active Choices Reactive Reference Parameter: Configuration Options

![](https://wiki.jenkins.io/download/attachments/74875908/ACRR_param.gif?version=1&modificationDate=1435872391000&api=v2)

A Reactive Reference parameter is configured with a set of similar
options as those shown above for the Active Choices Reactive parameter.

However, a **Reactive Reference parameter provides a unique set of
rendering options** (see 'Choice Type').

-   Input text box
-   Numbered list
-   Bullet items list
-   Formatted HTML
-   Formatted Hidden HTML

Given the wide variety of rendering options the Active Choices Groovy
script must return the following types of variables for each option:

| Choice Type          | Groovy Returns | Comment                                                                                                                                                          |
|----------------------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Input text box       | String         | The return String appears in a simple text box                                                                                                                   |
| Numbered list        | List           | The return List displays as a numbered list                                                                                                                      |
| Bullet items list    | List           | The return List displays as a bulleted list                                                                                                                      |
| Formatted HTML       | String         | The return String must be well formatted HTML to display correctly. You can include any HTML tags here, e.g.: some \<table\>, or a \<form\> to another web site. |
| Formatted Hidden HTM | String         | The parameter won't be displayed in the UI                                                                                                                       |

A typical application of a Reactive Reference parameter is to
dynamically display reference information that can be used to guide the
user in making an appropriate value selection of another job parameter.

By design, the values of Reactive Reference parameters are NOT passed to
the build environment with one important exception. When the choice type
is set to **Formatted HTML** or **Formatted Hidden HTML** and the HTML
is an 'input' element the value can be passed to the build. See the
'Advanced Usage' section for additional instructions.

### Example Configuration: Active Choices Reactive Reference Parameter

Below we present 3 examples of Reactive References with different Choice
Types and their corresponding renderings in the Jenkins job UI.

![](https://wiki.jenkins.io/download/attachments/74875908/ACRR_r03_param.gif?version=1&modificationDate=1435873274000&api=v2)

Consider an example where the user needs to make a meal selection that
complements the available wine selection. The food selection would be
easier if some useful reference info could be offered when users
considered a particular wine. We call this reference information the
WINE\_RULE and we can easily implement it using a Reactive Reference
parameter.

The WINE\_RULE gets automatically updated when a user makes a new
selection from the WINE\_MENU (Note Referenced parameters=WINE\_MENU).
As a result, when we make a WINE\_MENU selection we also get a
WINE\_RULE that can guide the FOOD\_MENU selection.

![](https://wiki.jenkins.io/download/attachments/74875908/ACR_r03.gif?version=1&modificationDate=1435928651000&api=v2)

### Reactive Reference Configuration (Example 03)

The complete configuration of the WINE\_RULE parameter is shown below.  
![](https://wiki.jenkins.io/download/attachments/74875908/ACR_c03.png?version=1&modificationDate=1435929437000&api=v2)

### Reactive Reference Groovy script (Example 03)

The groovy script that generates the WINE\_RULE **formatted HTML** for
each of the choices is shown below.

``` syntaxhighlighter-pre
switch(WINE_MENU){
 case~/.*Champagne.*/:
    winerec='Champagne is perfect with anything salty'
    return "<b>${winerec}</b>"
 break
case ~/.*Sauvignon Blanc.*/:
    winerec='Sauvignon Blanc goes with tart dressings and sauces'
    return "<b>${winerec}</b>"
break
 case~/.*Grüner Veltliner.*/:
    winerec='Choose Grüner Veltliner when a dish has lots of fresh herbs'
    return "<b>${winerec}</b>"
 break
case~/.*Pinot Grigio.*/:
    winerec='Pinot Grigio pairs well with light fish dishes'
    return "<b>${winerec}</b>"
 break
case ~/.*Chardonnay.*/:
    winerec='Choose Chardonnay for fatty fish or fish in a rich sauce'
    return "<b>${winerec}</b>"
break
 case~/.*Off-Dry Riesling.*/:
    winerec='Off-Dry Riesling pairs with sweet & spicy dishes'
    return "<b>${winerec}</b>"
 break
 case~/.*Moscato dAsti.*/:
    winerec='Moscato dAsti loves fruit desserts'
    return "<b>${winerec}</b>"
 break
case ~/.*dry Rosé.*/:
    winerec='Pair a dry Rosé with rich, cheesy dishes'
    return "<b>${winerec}</b>"
break
 case~/.*Pinot Noir.*/:
    winerec='Pinot Noir is great for dishes with earthy flavors'
    return "<b>${winerec}</b>"
 break
}
```

## Advanced Usage Notes

### Considerations while writing your Groovy script

Your Groovy script binding has access to two additional variables for
use:

-   jenkinsProject -\> The Jenkins Project object
-   jenkinsBuild -\> The Jenkins Build object

### Passing Reactive Reference Values to the build

As was mentioned earlier, in general the values of reactive reference
parameters are not passed to the build. However, there are some
scenarios where the ability to pass these values would be of interest.
For a more extensive discussion of this feature you can read
[here](https://github.com/biouno/uno-choice-plugin/wiki/Using-Uno-Choice-for-Dynamic-Input-Text-Box-Defaults).

#### Scenario 1: Pass a dynamically created value that can be edited by the user

In this scenario, we want to provide the user a dynamic default value
that is also editable. This can be accomplished with the following
reactive reference configuration:

-   Choice Type: **Formatted HTML**
-   Groovy Script returning an **HTML input element** with the dynamic
    default value
-   Advanced Option set to
    ![](https://wiki.jenkins.io/download/attachments/74875908/ACR_c03_Advanced.png?version=1&modificationDate=1435931255000&api=v2)

#### Scenario 2: Pass a dynamically created value that is **hidden** (can't be edited by the user)

In this scenario, we want **the build to have access to a dynamic
parameter generated from user input/option selections** in the UI. The
parameter is created programmatically, and is not user-editable. This
can be accomplished with the following reactive reference configuration:

-   Choice Type: **Formatted Hidden HTML**
-   Groovy Script returning an **HTML input element** with the dynamic
    default value
-   Advanced Option set to
    ![](https://wiki.jenkins.io/download/attachments/74875908/ACR_c03_Advanced.png?version=1&modificationDate=1435931255000&api=v2)

    A 'Formatted Hidden HTML' Choice type is useful when you want to
    calculate values to use in the build, but these values should not be
    modified by the user(e.g. to compute the deploy URL).

    In both scenarios the groovy script must return an HTML element
    formatted as follows:

    ``` syntaxhighlighter-pre
    return "<input name=\"value\" value=\"${ReactiveRefParam}\" class=\"setting-input\" type=\"text\">"
    ```

    **ReactiveRefParam** is the Reactive Reference value that will be
    passed to the build

#### Scenario 3: Create an input control with dynamic HTML and pass its value to the build

This is an interesting application of the Reactive Reference parameter.
It allows you to create custom UI parameter controls with improved
interactivity. See
[example](https://wiki.jenkins-ci.org/display/JENKINS/Reactive+Reference+Dynamic+Parameter+Controls)

### Advanced Option: Omit Value Field

By default Reactive References pass to the build a hidden \<input
name="value" value=""\>. It means that your Reactive Reference parameter
will always be empty, but you can use a Formatted HTML parameter and
instruct the plug-in to not include this hidden value parameter.

You can click the Advanced button and there you will find an option to
omit the value field. This will you let you define a value for the
hidden parameter.

![](https://wiki.jenkins.io/download/attachments/74875908/ACR_c03_advanced.gif?version=1&modificationDate=1435933116000&api=v2)

### Using Active Choices with Scriptler scripts

We assume users that need to use Scriptler generated parameters are
already familiar with the Scriptler Plug-in. If you need further
information on how to use the Scriptler Plug-in, please refer to [its
Wiki
page](https://wiki.jenkins-ci.org/display/JENKINS/Scriptler+Plugin)
first.

Similarly to a Groovy script, a Scriptler script is also written in
Groovy and used to render the parameter. Your Scriptler script is also
expected to return a java.util.List, Array or java.util.Map for Active
Choices and Reactive parameters, or custom HTML elements for the
Reactive Reference parameter. Note that the value of other build
parameters (when using Scriptler in combination with Active Choices)
will be available in the Scriptler script context. You do not need to
define such parameters in the Scriptler interface, or during the job
definition. 

However, **the main advantage that the Scriptler Plug-in** provides is
the creation of a **reusable** **Groovy script catalog** that can be
used across multiple jobs or even for automation.

To make your Scriptler scripts reusable across multiple projects you
should parameterize them and assign script parameters using build
parameters.

#### Example

Environments.groovy in Scriptler
``` syntaxhighlighter-pre
return ["Select:selected", "DEV", "TEST", "STAGE", "PROD"]
```

HostsInEnv.groovy in Scriptler
``` syntaxhighlighter-pre
// Static content examples. These lists can be generated dynamically as an alternative.
List devList    = ["Select:selected", "dev1", "dev2"]
List testList   = ["Select:selected", "test1", "test2", "test3"]
List stageList  = ["Select:selected", "stage1"]
List prodList   = ["Select:selected", "prod1", "prod2", "prod3", "prod4"]

List default_item = ["None"]

if (Environment.equals('DEV')) {
  return devList
} else if (Environment.equals('TEST')) {
  return testList
} else if (Environment.equals('STAGE')) {
  return stageList
} else if (Environment.equals('PROD')) {
  return prodList
} else {
  return default_item
}
```

Pipeline in Jenkinsfile
``` syntaxhighlighter-pre
properties([
    parameters([
        [
          $class: 'ChoiceParameter',
          choiceType: 'PT_SINGLE_SELECT',
          name: 'Environment',
          script: [
            $class: 'ScriptlerScript',
            scriptlerScriptId:'Environments.groovy'
          ]
        ],
        [
          $class: 'CascadeChoiceParameter',
          choiceType: 'PT_SINGLE_SELECT',
          name: 'Host',
          referencedParameters: 'Environment',
          script: [
            $class: 'ScriptlerScript',
            scriptlerScriptId:'HostsInEnv.groovy',
            parameters: [
              [name:'Environment', value: '$Environment']
            ]
          ]
        ]
      ])
  ])

pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        echo "${params.Environment}"
        echo "${params.Host}"
      }
    }
  }
}
```

### Filter Supports Regular Expressions

Note that although the text 'Filter' box available for Active Choices
parameters provides easy, case insensitive filtering by simply typing
some text, it also support more sophisticated filtering using regular
expressions.

The following example shows such an example where a complex options list
is filtered using a regular expression.  
![](https://wiki.jenkins.io/download/attachments/74875908/Untitled_Clipping_050415_022629_PM.jpg?version=1&modificationDate=1430763997000&api=v2)

## Security
Active Choice versions before v2.0 may not be safe to use. Please review the
following warnings before using an older version:

-   [Arbitrary code execution
    vulnerability](https://jenkins.io/security/advisory/2017-04-10/)
-   [Stored cross-site scripting
    vulnerability](https://jenkins.io/security/advisory/2017-10-23/)

**Starting with Active Choices v2.0, sandboxed **Groovy scripts
for **Active Choices Reactive Reference Parameter** will no longer emit
HTML that is considered unsafe, such as `<script>` tags. This may result
in behavior changes on *Build With Parameters* forms, such as missing
elements. To resolve this issue, Groovy scripts emitting HTML will need
to be configured to run outside the script security sandbox, possibly
requiring separate administrator approval in *In-Process Script
Approval*.

Active Choices will load two extra Javascript files, JQuery and
unochoice.js.

## Languages Supported

1.  English
2.  Portuguese (Brazil)) Work in Progress
3.  If you want to include yours, send us a pull request with the
    messages.properties files or get in touch!

## Known Limitations

1.  \- The parameters are supposed to be handled only by humans, and at
    the moment do not work when the job is triggered by plug-ins, API or
    scripts. Please see [this
    issue](https://issues.jenkins-ci.org/browse/JENKINS-28735)
    for more.
2.  \- Before filing issues, please take a look at the [Troubleshooting
Page](https://wiki.jenkins.io/display/JENKINS/Troubleshooting)

# **Compatibility Matrix**

This plug-in is not developed for Hudson, and we don't fix bugs
happening in any version of Hudson.

|         |          |          |          |
|---------|----------|----------|----------|
| Plugin  | 1.0      | 1.1      | 1.2      |
| Jenkins | 1.580.1+ | 1.580.1+ | 1.580.1+ |

## Open Issues

## Sponsors

## ![](https://wiki.jenkins.io/download/attachments/74875908/tupilabs.png?version=1&modificationDate=1416498962000&api=v2)

For commercial support, please get contact us
via [@tupilabs](https://twitter.com/tupilabs)

## ![](https://wiki.jenkins.io/download/attachments/74875908/bio1-200x102.png?version=1&modificationDate=1424178857000&api=v2)

[BioUno](http://biouno.org)

## Resources

| URL                                                                                                                    | Description                                                                                                                                                           |
|------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <http://biouno.org/publications.html>                                                                                  | Contains a list of resources for several plug-ins created in the [BioUno](http://biouno.org) project, including Active Choices, R, and Image Gallery. |
| <https://www.cloudbees.com/event/topic/jenkins-ci-open-source-continuous-integration-system-scientific-data-and-image> | In Ioannis Moutsatsos' talk, you can find slides about the Active Choices (née Uno-Choice) plug-in, as well as watch it being used too.                               |

## Release Notes

##### Version 2.4 (2020/07/09)

1. [JENKINS-62215](https://issues.jenkins-ci.org/browse/JENKINS-62215): antisamy-markup-formatter-plugin v2.0 filters
input fields from uno-choice plugin. In this version we have added a built-in markup formatter in the plug-in
(NB: not available in Jenkins as a markup formatter) that allows for input, textarea, select, and option HTML
tags). The issue was found and fixed by Andrew Potter @apottere (thanks!)

##### Version 2.3 (2020/05/16)

1. [JENKINS-61068](https://issues.jenkins-ci.org/browse/JENKINS-61068): Active Choices radio parameter has incorrect default value on parambuild URL. Fixed by Adam Gabryś in [pr/32](https://github.com/jenkinsci/active-choices-plugin/pull/32) (thanks!).
2. [JENKINS-61751](https://issues.jenkins-ci.org/browse/JENKINS-61751): let :disabled and :deleted at the same (thanks to @ivarmu)
3. [JENKINS-62317](https://issues.jenkins-ci.org/browse/JENKINS-62317): Upgrade dependencies pre 2.3 release (Jenkins LTS 2.204 now, Java 8, script-security 1.72, antisamy-markup-formatter 2.0, no more powermockito in tests, fixing spot bugs issues)
4. [JENKINS-39742](https://issues.jenkins-ci.org/browse/JENKINS-39742): Active Choice Plugin should honor ParameterDefinition serializability (was: Active Choice Plugin in Pipelines throw NotSerializableException)
5. [JENKINS-61243](https://issues.jenkins-ci.org/browse/JENKINS-61243): Artifactory plugin to fail active-choice plugin

##### Version 2.2 (2019/09/13)

1.  Updated minimum version of Jenkins to 2.60.3
2.  Updated dependency ssh-cli-auth to 1.4,
3.  Updated dependency script-security to 1.39
4.  Updated stapler-adjunct-jquery to 1.12.4-0
5.  [JENKINS-51296](https://issues.jenkins-ci.org/browse/JENKINS-51296):
    Project renaming is not propagated to Active Choices projectName
    (thanks to sebcworks)
6.  [JENKINS-39665](https://issues.jenkins-ci.org/browse/JENKINS-39665):
    Unnecessary Scrollbars on build with parameters screen when using
    Active Choices Plugin (thanks to Lubomir Stanko)
7.  [JENKINS-53356](https://issues.jenkins-ci.org/browse/JENKINS-53356):
    Scriptlet 'ViewScript' link and Required Parameters not displayed in
    configuration
8.  Fixed javadocs issues, missing license headers, simplified if
    expressions, and other minor code improvements
9.  [Pull Request
    \#25](https://github.com/jenkinsci/active-choices-plugin/pull/25):
    Initial version. Allow to disable any select option with :disable
    (thanks to Ivan Aragonés Muniesa)

##### Version 2.1.1 (2018/08/25)

1.  [JENKINS-49260](https://issues.jenkins-ci.org/browse/JENKINS-49260):
    this.binding.jenkinsProject not returning project of current build
2.  [JENKINS-51296](https://issues.jenkins-ci.org/browse/JENKINS-51296):
    Project renaming is not propagated to Active Choices projectName
3.  Updates to dependencies from Jenkins and plugins, in pom.xml  

##### Version 2.1 (2018/01/01)

1.  [JENKINS-48230](https://issues.jenkins-ci.org/browse/JENKINS-48230):
    Discover parameters on jobs' wrappers 
2.  [JENKINS-47908](https://issues.jenkins-ci.org/browse/JENKINS-47908):
    After upgrade to v2.0, the active choices updates randomly in the
    browser 
3.  [JENKINS-48448](https://issues.jenkins-ci.org/browse/JENKINS-48448):
    Add a variable with the parameter name
4.  [JENKINS-43380](https://issues.jenkins-ci.org/browse/JENKINS-43380):
    Input parameter HTML description not working   

##### Version 2.0 (2017/10/23)

1.  [Fix security
    vulnerability](https://jenkins.io/security/advisory/2017-10-23/)
    1.  **Important:** **Sandboxed** Groovy scripts for **Active Choices
        Reactive Reference Parameter** will no longer emit HTML that is
        considered unsafe, such as \<script\> tags. This may result in
        behavior changes on *Build With Parameters* forms, such as
        missing elements. To resolve this issue, Groovy scripts emitting
        HTML will need to be configured to run outside the script
        security sandbox, possibly requiring separate administrator
        approval in *In-Process Script Approval*.

2.  [JENKINS-42685](https://issues.jenkins-ci.org/browse/JENKINS-42685):
    Remove custom stapler proxy as we can now use assync requests
3.  [JENKINS-31625](https://issues.jenkins-ci.org/browse/JENKINS-31625):
    Add configuration parameter, which defines, when filter must started
    work
4.  [JENKINS-36158](https://issues.jenkins-ci.org/browse/JENKINS-36158):
    Active choice reactive reference parameter not working on checkbox
    reference 
5.  [JENKINS-43380](https://issues.jenkins-ci.org/browse/JENKINS-43380):
    Input parameter HTML description not working 
6.  [Pull request
    \#15](https://github.com/jenkinsci/active-choices-plugin/pull/15):
    Make Scriptler dependency optional (thanks to Daniel Beck)  

##### Version 1.5.3 (2017/03/11)

1.  [JENKINS-34487](https://issues.jenkins-ci.org/browse/JENKINS-34487):
    Do not hang the browser when parameter re-evaluated (thanks to
    Vladimir Fedorov)
2.  [JENKINS-38532](https://issues.jenkins-ci.org/browse/JENKINS-38532):
    Active Choices Reactive Reference Parameters don't parse equals
    character ('=') properly.
3.  [JENKINS-34188](https://issues.jenkins-ci.org/browse/JENKINS-34188):
    jenkins.log noise: "script parameter ... is not an instance of
    Java.util.Map."

##### Version 1.5.2 (2016/11/16)

1.  [JENKINS-39620](https://issues.jenkins-ci.org/browse/JENKINS-39620):
    Saving a job with Active Choice 1.4 parameters after upgrade to v1.5
    resets scriptlet parameters
2.  [JENKINS-39760](https://issues.jenkins-ci.org/browse/JENKINS-39760):
    Active Choices Parameters lost of Job Config save

##### Version 1.5.1 (2016/11/11)

1.  [JENKINS-36590](https://issues.jenkins-ci.org/browse/JENKINS-36590):
    Active-Choice jenkinsProject variable is not available under Folder
    or Multibranch-Multiconfiguration job
2.  [JENKINS-37027](https://issues.jenkins-ci.org/browse/JENKINS-37027):
    'View selected script option' in build configuration displays wrong
    scriptler script
3.  [JENKINS-34988](https://issues.jenkins-ci.org/browse/JENKINS-34988):
    this.binding.jenkinsProject not returning project of current build
4.  Upgraded build plug-ins
5.  Fixed Findbugs issues
6.  Upgraded parent in order to be able to release to Jenkins plug-in
    repositories
7.  [JENKINS-39620](https://issues.jenkins-ci.org/browse/JENKINS-39620):
    Saving a job with Active Choice 1.4 parameters after upgrade to v1.5
    resets scriptlet parameters
8.  [JENKINS-39534](https://issues.jenkins-ci.org/browse/JENKINS-39534):
    When Jenkins restarts, the groovy scripts are lost
9.  [JENKINS-34818](https://issues.jenkins-ci.org/browse/JENKINS-34818):
    Active Choice reactive parameter cannot access global parameters

##### Version 1.5.0 (2016/11/04)

1.  Removed from the update center. Read more about it
    [here](http://biouno.org/2016/11/11/what-happens-when-you-make-a-java-member-variable-transient-in-a-jenkins-plugin)

##### Version 1.4 (2016/02/14)

1.  [JENKINS-32405](https://issues.jenkins-ci.org/browse/JENKINS-32405):
    Groovy script failed to run if build started by timer
2.  [JENKINS-32461](https://issues.jenkins-ci.org/browse/JENKINS-32461):
    jenkinsProject variable is not available in Multi-configuration
    project
3.  [JENKINS-32566](https://issues.jenkins-ci.org/browse/JENKINS-32566):
    Render an AC Reactive reference as a functional Jenkins File
    Parameter

##### Version 1.3 (2015/12/24)

1.  [pull-request/6](https://github.com/jenkinsci/active-choices-plugin/pull/6):
    Let "PhantomJS Unit Testing" be able to run on both linux and
    windows...
2.  [JENKINS-30824](https://issues.jenkins-ci.org/browse/JENKINS-30824):
    Active Choice Reactive Parameter - radios button value not passed to
    build (thanks to lyen liang for his PR showing how to fix it
3.  [JENKINS-32044](https://issues.jenkins-ci.org/browse/JENKINS-32044):
    Fail to evaluate Boolean parameter to "on" when checked
4.  [JENKINS-30592](https://issues.jenkins-ci.org/browse/JENKINS-30592):
    An AC Reactive Reference is always displayed unless it references a
    parameter
5.  [JENKINS-32149](https://issues.jenkins-ci.org/browse/JENKINS-32149):
    Create random parameter name only once
6.  [JENKINS-29476](https://issues.jenkins-ci.org/browse/JENKINS-29476):
    The 'jenkinsProject' variable is not set in the binding after
    restarting Jenkins (thanks to @perfhector for PR/5)

##### Version 1.2 (2015/07/22)

1.  [pull-request/1](https://github.com/jenkinsci/active-choices-plugin/pull/1):
    Use value of Reactive Reference dynamic input controls as build
    parameters. See
    [example](https://wiki.jenkins-ci.org/display/JENKINS/Reactive+Reference+Dynamic+Parameter+Controls)
2.  [pull-request/2](https://github.com/jenkinsci/active-choices-plugin/pull/2):
    Update unochoice.js

##### Version 1.1 (2015/06/28)

1.  [JENKINS-29055](https://issues.jenkins-ci.org/browse/JENKINS-29055):
    Problem with Active Choices 1.0 and jQuery
2.  [JENKINS-28764](https://issues.jenkins-ci.org/browse/JENKINS-28764):
    Environment variables are not expanded and thus cannot be used as
    parameter values to Scriptler scripts
3.  [JENKINS-28785](https://issues.jenkins-ci.org/browse/JENKINS-28785):
    Referenced Parameters Not Passed to Scriptler Scripts

##### Version 1.0 (2015/06/03)

1.  Initial release to Jenkins update center, with Choice, Cascade
    Dynamic Choice and Dynamic Reference parameter types.

##### Versions released to BioUno update center

The first commit happened on 2014/03/13, and the initial release on
2014/03/21. There were 24 releases to the [BioUno update center](http://biouno.org/jenkins-update-site.html), the last
one being on 2015/03/26, when the project decided to publish it via
Jenkins update center.
