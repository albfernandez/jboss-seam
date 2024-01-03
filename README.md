# JBoss Seam - Contextual Component framework for Java EE

## Important

Upstream Seam 2 is not maintained since 2014. This fork is an attemp to keep the project live for a while.

Main concepts from Seam 2 were incorporated in Java EE 6 as CDI technology.

You should use CDI instead Seam 2 in new developments, and consider migrate old apps to CDI.

See https://github.com/seam/migration or more details here https://developer.jboss.org/en/seam/seam2/blog/2015/07/20/migration-from-seam-2-to-cdideltaspike


## License

This software is distributed under the terms of the FSF Lesser GNU
Public License (see http://www.gnu.org/licenses/lgpl-3.0.txt). 

## Seam 2: Next generation enterprise Java development

This is a modified fork of https://github.com/seam2/jboss-seam.

Seam 2.3 targets Java EE 7 capabilities such as JSF2 and JPA2 on the JBoss Enterprise Application Platform 6 (JBoss AS 7) 
Seam 2.3 also supports RichFaces 4.5

Seam 2 is a powerful open source development platform for building rich Internet applications in Java. 
Seam integrates technologies such as Asynchronous JavaScript and XML (AJAX), JavaServer Faces (JSF), Java Persistence (JPA),
 Enterprise Java Beans (EJB 3.1) and Business Process Management (BPM) into a unified full-stack solution, complete with sophisticated tooling.

Seam has been designed from the ground up to eliminate complexity at both architecture and API levels. 
It enables developers to assemble complex web applications using simple annotated Java classes, a rich set of UI components, a
nd very little XML. 
Seam's unique support for conversations and declarative state management can introduce a more sophisticated user 
experience while at the same time eliminating common bugs found in traditional web applications. 

## Security

List of vulnerabilities fixed

  - [CVE-2007-6433](https://nvd.nist.gov/vuln/detail/CVE-2007-6433) Fixed in 2.0.0.GA 2007-11-01
  - [CVE-2010-1871](https://nvd.nist.gov/vuln/detail/CVE-2010-1871) Fixed in 2.2.1.Final 2011-01-27
  - [CVE-2011-1484](https://nvd.nist.gov/vuln/detail/CVE-2011-1484) Fixed in 2.2.2.Final 2011-04-22
  - [CVE-2011-2196](https://nvd.nist.gov/vuln/detail/CVE-2011-2196) Fixed in 2.3.0.Final 2012-09-21
  - [CVE-2013-6447](https://nvd.nist.gov/vuln/detail/CVE-2013-6447) Fixed in 2.3.2.ayg 2016-03-08
  - [CVE-2013-6448](https://nvd.nist.gov/vuln/detail/CVE-2013-6448) Fixed in 2.3.2.ayg 2016-03-08
  - [CVE-2014-0149](https://nvd.nist.gov/vuln/detail/CVE-2014-0149) Fixed in 2.3.4.ayg 2016-03-23
  - [CVE-2014-0248](https://nvd.nist.gov/vuln/detail/CVE-2014-0248) Fixed in 2.3.10.ayg 2017-06-05


## Maven import

To use seam in your application via maven simply add the following dependency to your ``pom.xml`` file:

```xml

<dependency>
    <groupId>com.github.albfernandez.seam</groupId>
    <artifactId>jboss-seam</artifactId>
    <version>2.3.27.ayg</version>
</dependency>
<dependency>
    <groupId>com.github.albfernandez.seam</groupId>
    <artifactId>jboss-seam-ui</artifactId>
    <version>2.3.27.ayg</version>
</dependency>

<!-- 

Add more modules if you need them: 

jboss-seam
jboss-seam-ui
jboss-seam-debug
jboss-seam-excel
jboss-seam-mail
jboss-seam-pdf
jboss-seam-remoting
jboss-seam-resteasy
jboss-seam-rss
jboss-seam-wicket
jboss-seam-ioc

-->
```



## Get Up And Running Quick

1. Install JBoss AS 7.1.1.Final  
2. Start JBoss AS by typing `bin/standalone.sh` in the JBoss AS home directory
3. In the `examples/booking` directory, type `mvn clean package` and check  for any error messages.
4. In the booking-ear directory run:
    `mvn jboss-as:deploy`   
5. Point your browser to `http://localhost:8080/seam-booking/`    
6. Register an account, search for hotels, book a room...

## Learn more

* Read the documentation in https://albfernandez.github.io/jboss-seam/
* Old user forums https://developer.jboss.org/en/seam/seam2

## Compiling from sources

You need an install of Maven 3.0.x


To build Seam from github, just run 

```bash
git clone https://github.com/albfernandez/jboss-seam.git
cd jboss-seam
mvn -Pdistribution clean package
``` 

When finished you have the complete seam distribution in ``distribution/target/``

If you are making changes to source code and want to test quickly without making a full release, you can type

    mvn clean package

Resulting jar files will be in each sub-project target directory.



## Note for flex users.

jboss-seam-flex module was removed in 2.3.25.ayg. If you need it, you can use the jar included in 2.3.24.ayg.

In Seam version 2.3.17.ayg BlazeDS is upgraded to Apache Flex BlazeDS 4.7.3. 

One change is you must provide a service-config.xml yourself (Seam can not provide a default one by itself now), by creating a ``/WEB-INF/flex/services-config.xml`` file or using other file name and provide it via ``init-param`` named  ``services.configuration.file`` in web.xml 


