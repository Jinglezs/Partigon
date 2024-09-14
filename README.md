This is a fork of the original Partigon library by Gameoholic, which can be found [here](https://github.com/Gameoholic/Partigon).

This fork reworks some of the implementation details of the original to embrace a more Kotlin-like design:
* Replaces uses of BukkitScheduler with Kotlin coroutines
* Animations are now tied to a CoroutineScope, allowing for easier lifecycle management.
* PartigonParticle and implementations renamed to PartigonAnimation to better reflect their purpose.
* Kotlin Duration can alternatively be used in place of server ticks for delays and durations for readability purposes.

This fork can be used as a standalone library rather than a plugin. For personal use, clone the repository and publish
it to your local Maven repository. Ensure maven local is listed as a repository in your gradle build file and add the
dependency as normal.

The original documentation is still valid, excluding the class name changes.

# Partigon

A Minecraft particle animation library designed to make your life easier.

Create complex particle animations with just a few lines of code!

https://github.com/Gameoholic/Partigon/assets/30177004/07b13d7f-7630-463c-b83b-44c95c059d6a

To get started, head to the [documentation](https://partigon.gameoholic.xyz/).


## Using Partigon
This fork of Partigon makes use of the following libraries:
* [Kotlin Standard Library](https://central.sonatype.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-jdk8)
* [Kotlinx Coroutines](https://central.sonatype.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm)

To include these in your classpath at runtime, you can either shade them into your plugin jar or implement a Paper
[PluginLoader](https://docs.papermc.io/paper/dev/getting-started/paper-plugins#loaders) to download and cache the 
libraries during server start-up.