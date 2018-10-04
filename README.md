# KIE Servier Dialect (for use with kie business apps + thymeleaf)

This is a KIE Server dialect for jBPM business applications (start.jbpm.org) used 
with integration with Thymeleaf. 

It allows you to interact with many services exposed in your jBPM business application
straight in your Thymeleaf html templates, without writing any integration code at all.

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

# Using the dialect

# Setting up your template html
1. Kie Server dialect produces html which includes some boostrap style class names. In order for those to work
you need to add the boostrap style to your page <head> section, for example:

```html
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css">  
```
2. All dialect tags start with &lt;kieserver:.../&gt; and you can define this as namespace as an attribute in your <html>
page tag (note this step is optional):

```html
<html xmlns:th="http://www.thymeleaf.org" xmlns:kieserver="http://jbpm.org/">
```

# Display all process definitions
In the body section of your template add:
```html
<kieserver:processdefs/>
```

This will generate a table on your page displaying all processes that are defined and registered, for example:

![Sample process definitions](sampleprocessdefs.png?raw=true)

Once more processes are registered or unregistered a simple page-refresh will update 
this table for you. 

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
