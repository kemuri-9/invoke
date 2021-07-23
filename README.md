# Invocation Utilities

Starting in Java 7, Java provides a powerful "Invocation" system that is rather comparable to the reflection system.
The Invocation system is an underlying functionality that powers lambdas.

However, there are some differences between the Invocation and Reflection systems.
In Reflection, access checks are performed on every use of the Constructor, Field, or Method.
But in Invocation, access checks are not performed every use, instead the checks are performed at acquisition of the MethodHandle or VarHandle.
This has a distinct impact on performance where MethodHandles and VarHandles have better performance when used multiple times.

With the Invocation system there is a "Lookup" process (with an associated class and instances) to resolve MethodHandles and VarHandles. In the Invocation system there is additionally a Lookup that has full access to the entire JVM.
By utilizing the full access Lookup, a number of JVM security measures can be bypassed:
* Accessing protected, package private, or private members, even if residing in another module.
* Updating fields declared as  _final_

There are many applications of this capability, but care must be taken as defeating the one major security aspect of the JVM does have major risks.
One particular application of this is for simplifying the creation of white-box testing.

## Building

The project uses gradle through the gradle wrapper to build.
As such,  _./gradlew_  or simply  _gradlew_ on some operating systems in the project root directory should be utilized for building.
Any Java 8+ JVM can be utilized to run gradle itself.

Building and test execution use different JVM releases which encompass all of versions 8 through 17.
If gradle does not detect a JVM for that release version, it will attempt to download it.
Running a build for the first time can take a considerable amount of time depending on bandwidth to download the JVMs.
Further details on this behavior can be found at [https://docs.gradle.org/current/userguide/toolchains.html](https://docs.gradle.org/current/userguide/toolchains.html)

## Javadoc

Javadoc for releases are available at [https://kemuri-9.github.io/invoke/](https://kemuri-9.github.io/invoke/)
