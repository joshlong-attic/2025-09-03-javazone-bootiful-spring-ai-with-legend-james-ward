# scriptable-java
hi, Spring fans! In this installment I look at the utterly absurd possibilities of using implicit classes and JBang together to handle scripting Java and Spring workloads in Java 25 or later.

You can run this program (`script.java`) by first installing `jbang` (which you can do via the amazing SDKMAN.io installer) and then giving the script executable permissions: `chmod a+x script.java` and then just.. _run_ it:

```shell
./script.java
```

Instant Spring Boot app!

If you have the JBang IntelliJ IDEA plugin, you can easily edit this scvript inside of IntelliJ. but, first you'll need to spin up a Maven project like this:

```shell
jbang export maven script.java
```

then just run `idea pom.xml` and make changes as you like. Easy!
