# KIE Servier Dialect (for use with kie business apps + thymeleaf)

This is a KIE Server dialect for jBPM business applications (start.jbpm.org) used 
with integration with Thymeleaf. 

It provides a set of directives to easily interact with many services exposed in your jBPM business application
straight in your Thymeleaf html templates. It is really easy to install and it eliminates the need for you
to write integration code for some functionalities provided. The dialect is also extendable so you control 
the display of the resulting information. 

# Installing the dialect
Once you have created your business appliation on start.jbpm.org you can easily install this dialect
in just a single step:

1. In your service module pom.xml add dependency:
```xml
<dependency>
  <groupId>org.jbpm.addons</groupId>
  <artifactId>thymeleaf-kie-server-dialect</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
And that's it! You are now ready to start using the dialect in your Thymeleaf templates!

# Setting things up
1. Kie Server dialect by default produces html which includes some boostrap style class names and js functions. In order for those to work
you need to add the boostrap style and js and jquery to your page <head> section, for example:

```html
<head>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css">  
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js"></script>
</head>
```
You are however not bound to use the default generated html and can create your own display of the 
resulting information. For this see the "Creating your own display" section.

2. All dialect tags start with &lt;kieserver:.../&gt; and you can define this as namespace as an attribute in your <html>
page tag (note this step is optional):

```html
<html xmlns:th="http://www.thymeleaf.org" xmlns:kieserver="http://jbpm.org/">
```
3. In order to display the process and task forms the dialect uses inline frames. This is by default disabled 
in your business application and you have to enable it. 
To enable edit your DefaultWebSecurityConfig.java file adding

```java
.headers().frameOptions().sameOrigin()
```

So your entire configure method can look like:

```java
@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/rest/*").authenticated()
                .and()
                .httpBasic()
                .and()
                .headers().frameOptions().sameOrigin();
    }
```

# Using the dialect
Now that you have set everything up here is how you can use the dialect

# Display all process definitions
In the body section of your template add:
```html
<kieserver:processdefs/>
```

This will generate a table on your page displaying all processes that are defined and registered, for example:

![Sample process definitions](sampleprocessdefs.png?raw=true)

You can start a process intsance by clicking on the "Start" action button. This will open the process form where you
can enter in process data defined in the form and start the business process.

![Sample start business process|50%](samplestartprocess.png?raw=true)

# Display deployment unit info
In the body section of your template add:
```html
<kieserver:deployments/>
```

This will display info for all deployment units:

![Sample deployment unit info](sampledeploymentunits.png?raw=true)

You can also pass a deployment unit id parameter to show info for a specific 
deployment unit:

```html
<kieserver:deployments deploymentid="${deploymentid}"/>
```

# Display process instances info
In the body section of your template add:
```html
<kieserver:processinstances/>
```

This will display info table with  all available process instances:

![Sample deployment unit info](sampleprocessinstances.png?raw=true)

If the task instance contains Tasks that can be worked on will be displayed in the "Work on Tasks" column. 
Each is a link which when clicked will open up the task form modal which includes the task form and buttons with which
you can advance the user task. 

![Sample work on task|50%](sampleworkontask.png?raw=true)

# Display process instances image
In the body section of your template add:
```html
<kieserver:processimages/>
```

This will display a dropdown table with a list of process instances. If there are no process instances
available the dropdown will be empty. You an pick a process instance id from the dropdown which will then display
the annotated process instance image, for example:

![Sample process image](sampleprocessimage.png?raw=true)


# Starting a business process
In the body section of your page add:
```html
<kieserver:startprocess processid="${processid}" 
                        containerid="${containerid}" 
                        processinputs="${processinputs}"/>
```

where ${processid} ${containerid} and processinputs are your Model attributes added in the 
@Get mapping of your template. 

Notes:
* processid and containerid attributes can also accept hard-coded string values
where as processinput has to be an expression mapping to a Map<String, Object> model object.

* processid and containerid are required attributes,
where processinputs is optional (in case your process started does not take any input).

When your page is being parsed by Thymeleaf the business process will be started. 
and the output will be an alert, for example:

![Sample process start result](sampleprocessstartresult.png?raw=true)

# Aborting a business process instance
In the body section of your page add:
```html
<kieserver:abortprocess deploymentid="${deploymentid}"
            processinstanceid="${processinstanceid}"/>
```

where ${deploymentid} ${processinstanceid} and processinputs are your Model attributes added in the 
@Get mapping of your template. 

Notes:
* deploymentid and processinstanceid attributes can also accept hard-coded string values
* deploymentid and processinstanceid are required attributes

# Signalling a process instance
In the body section of your page add:
```html
<kieserver:signalprocess deploymentid="${deploymentid}"
                processinstanceid="${processinstanceid}"
                signalname="${signalname}"
                event="${event}"/>
```

where ${deploymentid} ${processinstanceid} ${signalname} ${event} and processinputs are your Model attributes added in the 
@Get mapping of your template. 

Notes:
* deploymentid and processinstanceid attributes can also accept hard-coded string values, event has to be an expression resolving
to an Object type variable
* deploymentid and processinstanceid  and signalname are required attributes, event is optional

# Creating your own display
All dialect directives shown so far can take an extra attribute called "fragment" which you can use to control their display. 
We use Thymeleaf fragments (reusable components) for the display of each directive, you can find them in a single file 
[here](src/main/resources/templates/kieserverdialect.html).
With the "fragment" attribute you can change the default fragment name used to one you define yourself. Here are the steps to do this:

1. In your business application create an html file that will hold your fragments, for example /src/main/resources/templates/mykiedialectfragments.html.

2. Let's say you want to have your own display of the results of 
```html
<kieserver:processdefs/>
```
In your mykiedialectfragments.html add:

```html
<div th:fragment="myprocessdefs">
... YOUR CODE HERE ...
</div>
```

3. Tell the dialect directive to use your own fragment:

```html
<kieserver:processdefs fragment="${myfragment}"/>
```

where ${myfragment} can be a model attribute or a hard-coded string, for this example the value should be "mykiedialectfragments :: myprocessdefs".

And that's it, you now control the display completely.