# KIE Servier Dialect (for use with kie business apps + thymeleaf)

This is a KIE Server dialect for jBPM business applications (start.jbpm.org) used 
with integration with Thymeleaf. 

It provides a number of useful markup options to perform operations inside your 
thymeleaf templates. 

# Installing the dialect
Once you have created your business appliation on start.jbpm.org you can easily install
Thymeleaf and this dialect

1. in your service module pom.xml add dependecies:
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<dependency>
  <groupId>org.jbpm.addons</groupId>
  <artifactId>thymeleaf-kie-server-dialect</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>

<!-- optional only if thymeleaf mode in step 2. is added, if not then dont need this depends -->
<dependency>
  <groupId>net.sourceforge.nekohtml</groupId>
  <artifactId>nekohtml</artifactId>
</dependency>
```

2. in your sr/main/resources/application*.xml used add (this is optional step):

```
spring.thymeleaf.mode=LEGACYHTML5
```

3.Register the dialect. In your existing @Configuration
class or a new one add the following bean definition:

```java
@Bean
public KieServerDialect kieServerDialect() {
    return new KieServerDialect();
}
```
# Using the dialect
Once registered you can start using the dialect markup in your Thymeleaf templates.
Regardless of the template markup used, you should add in your html node of the template:

1. Starting a process from Thymeleaf template:
In your template html node add:

```html
xmlns:kieserver="http://jbpm.org/"
}
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

