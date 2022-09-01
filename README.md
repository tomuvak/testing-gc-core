# `com.tomuvak.testing-gc-core` – a multi-platform Kotlin library with primitive utilities for tests relying on garbage collection
[![Licence][1]][2]
[![Latest release version][3]][4]
[![Build and tests status][5]][6]

This library is licensed under the [MIT License](https://en.wikipedia.org/wiki/MIT_License);
see [LICENSE.txt](LICENSE.txt).

See also [`com.tomuvak.testing-gc`][7]: this library offers _primitive_ utilities for triggering garbage collection,
while that one (which uses these primitive utilities under the hood) offers higher-level constructs to help in actual
testing scenarios involving garbage collection, using [weak references][8].

## Table of contents
* [Rationale](#rationale)
* [How to use `com.tomuvak.testing-gc-core`](#how-to-use-comtomuvaktesting-gc-core)
  * [Including this library in a Kotlin project](#including-this-library-in-a-kotlin-project)
  * [Using the functionality in code](#using-the-functionality-in-code)
    * [General approach](#general-approach)
    * [Caveats](#caveats)
    * [Using `whenCollectingGarbage`](#using-whencollectinggarbage)
    * [Using `tryToAchieveByForcingGc`](#using-trytoachievebyforcinggc)
      * [Timeout](#timeout)

## Rationale
Part of the correctness of code lies within its releasing of resources once it no longer needs them. And part of these
resources is memory allocated for objects. When writing in a language with automatic
[garbage collection](https://en.wikipedia.org/wiki/Garbage_collection_(computer_science)) it is not normally the
responsibility of the programmer to actually make sure memory which is no longer referenced anywhere in the program
gets freed. But – and this is too often overlooked – **it _is_ their responsibility to make sure _objects which are no
longer needed are no longer referenced_**. Subtle bugs where this responsibility is neglected abound ([here's an
example](https://github.com/JetBrains/kotlin/commit/c0cac21b8a3170b2d6ec1e077562a78e557f0b5f#r78800018) from Kotlin's
standard library).

Being aware of this issue a programmer can try and write correct code. But that's not always enough: ideally the
correctness of the code should also be verified by tests – both to make sure that [future changes don't accidentally
spoil it](https://en.wikipedia.org/wiki/Software_regression), and to make sure that what seems to be correct actually
does work as expected ([two](https://stackoverflow.com/a/71537602) [examples](https://stackoverflow.com/a/73070221)
where releasing memory in Kotlin might not work as expected).

Unfortunately, it seems that most software developers aren't too aware of or concerned with the issue, and also, perhaps
relatedly, that there isn't great tooling to tackle the problem. In addition, because of the nature of the systems
involved, whatever tooling does exist naturally tends to be platform-specific.

This library tries to offer some remedy, by providing utilities which (at least to some extent) do enable testing for
the releasing of memory and (at least to some extent) are exposed with a unified interface that allows users of the
library to write a single version of their tests which can then run cross-platform. See also
[`com.tomuvak.testing-gc`][7], which uses this library under the hood to offer higher-level constructs for verifying
that memory can be reclaimed, with the help of [weak references][8].

## How to use `com.tomuvak.testing-gc-core`

### Including this library in a Kotlin project
To add the library from
[GitHub Packages](https://docs.github.com/en/packages/learn-github-packages/introduction-to-github-packages), a
reference to this repository's GitHub Packages
[Maven repository](https://maven.apache.org/guides/introduction/introduction-to-repositories.html) needs to be added
inside the `repositories { ... }` block in the project's `build.gradle.kts` file:

```kotlin
    maven {
        url = uri("https://maven.pkg.github.com/tomuvak/testing-gc-core")
        credentials { // See note below
            username = "<GitHub user name>"
            password = "<GitHub personal access token>"
        }
    }
```

and the dependency should be declared for the relevant source set(s) inside the relevant `dependencies { ... }` block(s)
inside the `sourceSet { ... }` block, e.g.

```kotlin
        val commonTest by getting {
            dependencies {
                implementation("com.tomuvak.testing-gc-core:testing-gc-core:0.0.3")
            }
        }
```

to add it for the test source sets on all platforms in a multi-platform project.

Note about credentials: it seems that even though this repository is public and everyone can download this library from
GitHub Packages, one still needs to supply credentials for some reason. Any GitHub user should work, when provided with
a [personal access
token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
for the user with (at least) the `read:packages` scope.

**You might want to keep the credentials private**, for example in case the GitHub user has access to private packages
(as GitHub personal access tokens can be restricted in the type of operations they're used for, but not in the
repositories they can access), or all the more so in case the token has a wider scope (and note also that one can change
a token's scope after its creation, so it's possible that at some future point the user might inadvertently grant a
token which was meant to be restricted more rights).

See this library's own [Gradle script](build.gradle.kts) for an example of one way this could be done by means of
storing private information in a local file which is not source-controlled. In this case the file – which is Git-ignored
– is called `local.properties`, and it includes lines like the following:

```properties
githubUser=<user name>
githubToken=<personal access token for the user above, with the `read:packages` scope>
```

### Using the functionality in code

#### General approach
To verify references to objects aren't kept, one needs to set up some effect which, once garbage has been collected,
will be observable if and only if the respective objects have been reclaimed. Such an effect might be triggered by a
[finalizer](https://en.wikipedia.org/wiki/Finalizer) (supported on
[JVM](https://kotlinlang.org/docs/java-interop.html#finalize) and
[JS](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry), but not on
Native – see e.g. [Kotlin Discussions](https://discuss.kotlinlang.org/t/finalize-alternative-in-kotlin-native/5030),
[Stack Overflow](https://stackoverflow.com/questions/44747862/does-kotlin-native-have-destructors),
[YouTrack][10], [GitHub issue](https://github.com/JetBrains/kotlin-native/issues/2327)), or it can be the nullification
of a [weak reference][8] (supported [on](https://docs.oracle.com/javase/8/docs/api/java/lang/ref/WeakReference.html)
[all](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.native.ref/-weak-reference/)
[platforms](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakRef), though without a
unified interface; another library might be used for cross-platform weak reference support, e.g.
[`com.tomuvak.weak-reference`][9]).

#### Caveats
* This library provides functionality which helps trigger garbage collection. It does **not** provide any functionality
  to help set up finalizers and/or weak references. Users of the library should take care of that on their own or use a
  separate library for that (e.g. [`com.tomuvak.weak-reference`][9]), or consider using [`com.tomuvak.testing-gc`][7].
* The functionality provided – while proven to have worked for some projects under some circumstances – cannot and does
  not guarantee that attempting to trigger garbage collection will indeed always trigger garbage collection.
* Even when garbage does indeed get collected, this does not guarantee execution of finalizers. (Empiric evidence
  suggests that on the JVM calling `Thread.sleep(1)` after triggering garbage collection addresses this issue. The
  author hasn't currently got any information regarding JS. It is the library's user responsibility to do whatever is
  needed for their use case; the library currently does nothing out of the box other than requesting the runtime to
  perform garbage collection.)
* As in the [two](https://stackoverflow.com/a/71537602) [examples](https://stackoverflow.com/a/73070221) mentioned
  above, it's possible there'll be hidden references preventing objects from being reclaimed. Diagnosing such cases
  might not be trivial. Extracting the generation of the objects which are to be reclaimed to other functions might
  help ([`com.tomuvak.testing-gc`][7] offers some functionality for that as well).

#### Using `whenCollectingGarbage`
`com.tomuvak.testing.gc.core.whenCollectingGarbage` compiles and runs on all platforms, but does nothing where the
functionality is not supported (= on JS).

The following example uses `whenCollectingGarbage()` with a finalizer; it assumes JVM (no other platform supports both).

```kotlin
@Test fun `template for using whenCollectingGarbage with finalizer`() {
    var hasBeenFinalzied = false
    val objectUnderTest = ObjectUnderTest(generateInputForObjectUnderTestWithFinalizer { hasBeenFinalzied = true })
    objectUnderTest.doStuffAfterWhichInputIsNoLongerNeeded()

    whenCollectingGarbage()
    Thread.sleep(1) // Seems to be necessary to allow finalizers to run.
    assertTrue(hasBeenFinalized)
}

private fun generateInputForObjectUnderTestWithFinalizer(onFinalization: () -> Unit): InputForObjectUnderTest =
    object : InputForObjectUnderTest {
        protected fun finalize() = onFinalization()
    }
```

The following example uses `whenCollectingGarbage()` with a weak reference; it assumes either JVM or Native, and uses
the cross-platform `WeakReference` implementation offered by [`com.tomuvak.weak-reference`][9].

```kotlin
@Test fun `template for using whenCollectingGarbage with weak reference`() {
    val (objectUnderTest, reference) = generateObjectUnderTestAndWeakReferenceToInput()
    objectUnderTest.doStuffAfterWhichInputIsNoLongerNeeded()

    whenCollectingGarbage()
    assertNull(reference.targetOrNull)
}

private fun generateObjectUnderTestAndWeakReferenceToInput():
        Pair<ObjectUnderTest, WeakReference<InputForObjectUnderTest>> {
    val input = InputForObjectUnderTest()
    return Pair(ObjectUnderTest(input), WeakReference(input))
}
```

#### Using `tryToAchieveByForcingGc`
Code using `com.tomuvak.testing.gc.core.tryToAchieveByForcingGc` will compile and work on all platforms. It is more
complicated to use than [`whenCollectingGarbage`](#using-whencollectinggarbage), so it's not recommended unless a test
targeting (also) platforms where the latter function can't be used (i.e. JS) is required.

It can only be run from a coroutine, so an "async" test is required. On JVM and Native this is easy to achieve with
[`kotlinx.coroutines.runBlocking`][11], but then again if targeting only JVM and/or Native this function is not likely
to be used in the first place. There are other ways to run async tests on Kotlin JS; the sister library
[`com.tomuvak.testing-coroutines`](https://github.com/tomuvak/testing-coroutines) offers a unified interface for async
tests which works with the same code on all platforms (the following example uses
[`com.tomuvak.testing.coroutines.asyncTest`][12]).

The following example uses `tryToAchieveByForcingGc()` with a weak reference; it is cross-platform, and uses the
`WeakReference` implementation provided by [`com.tomuvak.weak-reference`][9].

```kotlin
@Test fun `template for using tryToAchieveByForcingGc with weak reference`() = asyncTest { // Note use of asyncTest
    val (objectUnderTest, reference) = generateObjectUnderTestAndWeakReferenceToInput()
    objectUnderTest.doStuffAfterWhichInputIsNoLongerNeeded()
    assertTrue(tryToAchieveByForcingGc { reference.targetOrNull == null })
}

private fun generateObjectUnderTestAndWeakReferenceToInput():
        Pair<ObjectUnderTest, WeakReference<InputForObjectUnderTest>> {
    val input = InputForObjectUnderTest()
    return Pair(ObjectUnderTest(input), WeakReference(input))
}
```

(Note: the sister library [`com.tomuvak.testing-gc`][7] offers utilities to write tests like the last example above more
concisely.)

##### Timeout
On platforms not supporting direct triggering of garbage collection (= JS), the implementation of
`tryToAchieveByForcingGc` performs heavy computations, in the hope that that will cause garbage collection to run. As a
result, tests using it might require a long time to run, and with default configuration might exceed the allotted
timeout and fail.

To change the timeout assign the desired value (e.g. `"12s"` – that is twelve seconds – suffices for known usages, but
the exact time needed for a specific test might vary greatly depending on the specifics of the test and of the
environment on which it is run) to `timeout` within the `useMocha` block within the `testTask` block within the
`browser`/`nodejs` block within the `js` block within the `kotlin` block in the `build.gradle.kts` file (creating the
relevant blocks if they don't already exist), e.g.:

```kotlin
.
.
.

kotlin {
    .
    .
    .

    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
            testTask {
                useMocha {
                    timeout = "12s"
                }
            }
        }
    }

    .
    .
    .
}

.
.
.
```

[1]: https://img.shields.io/github/license/tomuvak/testing-gc-core?label=Licence
[2]: LICENSE.txt
[3]: https://img.shields.io/github/v/tag/tomuvak/testing-gc-core?label=Latest%20release
[4]: https://github.com/tomuvak/testing-gc-core/tags
[5]: https://github.com/tomuvak/testing-gc-core/actions/workflows/check-on-push.yaml/badge.svg
[6]: https://github.com/tomuvak/testing-gc-core/actions/workflows/check-on-push.yaml
[7]: https://github.com/tomuvak/testing-gc
[8]: https://en.wikipedia.org/wiki/Weak_reference
[9]: https://github.com/tomuvak/weak-reference
[10]: https://youtrack.jetbrains.com/issue/KT-44191/Cleanup-hook-for-KotlinNative-that-gets-called-when-or-after-an-object-is-garbage-collected
[11]: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html
[12]: https://github.com/tomuvak/testing-coroutines#using-the-functionality-in-code
