# leyden-analyzer

This is an interactive console to help debug what is happening with the AOT Cache.

This is a very early work-in-progress. This README is going to be deprecated soon, so don't trust it blindly. Just use the `help` command to guide you.

## Packaging and running the application

To make it work, just `mvn package` and then `java -jar target/quarkus-app/quarkus-run.jar`.

Or if you have [JBang](https://jbang.dev) installed, just run:

```bash
jbang analyzer@delawen/leyden-analyzer
```

NB: The analyzer is not published officially, so JBang might not always detect that a new version is available.
In that case run JBang with `--fresh` to force it to download the latest version: `jbang --fresh analyzer@delawen/leyden-analyzer`.
You might need to be patient because it uses [JitPack](https://jitpack.io) to build the tool on demand.

The analyzer uses [picocli](https://picocli.info) and [JLine](https://github.com/jline/jline3) to run.

## How to use it

There is a `help` command:
```bash
> help
Usage:  [COMMAND]
Interactive shell to explore the contents of the AOT cache. Start by loading an
AOT map file.
Commands:
  clean       Empties the information loaded.
  count       Count what elements are on the AOT Cache based on the information
                we have loaded.
  describe    Describe an object, showing all related info.
  warning     Help detect and clarify warnings found. By default, it lists
                incidents detected on the logs.
  info        Show information and statistics on the AOT Cache based on the
                information we have loaded.
  ls          List what is on the cache. By default, it lists everything on the
                cache.
  load        Load a file to extract information.
  cls, clear  Clears the screen
  tree        Shows a tree with elements that are linked to the root.
  help        Display help information about the specified command.

Press Ctrl-D to exit.

```

### Load some information

You should start by using the `load` command to load different files:

```bash
> load help
Usage:  load [-hV] [COMMAND]
Load a file to extract information.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help      Display help information about the specified command.
  aotCache  Load an AOT Map cache generated with -Xlog:aot+map=trace:file=aot.
              map:none:filesize=0
  log       Load a log generated with -Xlog:class+load,aot*=warning:file=aot.
              log:level,tags
```

We can load java logs of our app generated 
with `-Xlog:class+load,aot*=warning:file=aot.log:tags` :

```bash
> load log aot.log.0 aot.log
Adding aot.log.0 to our analysis...
File aot.log.0 added in 223ms.
Adding aot.log to our analysis...
File aot.log added in 175ms.
Adding aot.log.1 to our analysis...
Rewriting value for 'ArchiveRelocationMode' previously it was ' 0'.
Rewriting value for 'initial full module graph' previously it was 'enabled'.
Rewriting value for 'Using AOT-linked classes' previously it was 'true (static archive: has aot-linked classes)'.
File aot.log.1 added in 17ms.
Adding aot.log.loading to our analysis...
File aot.log.loading added in 196ms.
```

There is a status bar on the bottom of the interactive console showing the current elements loaded in our playground:
```
Our Playground contains: 9014 elements | 768 packages | 2 element types | 42 warnings  
```
And we can add an AOT map file generated with `-Xlog:aot+map=trace:file=aot.
map:none:filesize=0`

```bash
> load aotCache aot.map
Adding aot.map to our analysis...
This is a big file. The size of this file is 331 MB. This may take a while.
Consider using the `--background` option to load this file.
File aot.map added in 5478ms.
```

Loading the AOT Map gives a better overview on what the cache contains. 

Loading log files gives a better overview of why things are (or are not) in the cache and detect potential errors.

> **Do not mix logs and caches from different runs.**
> 
> That will lead to inconsistent and wrong analysis.

You may mix logs and aot map files from the same training or production run. Then, the information will complement each other. It is recommended to load first the AOT cache map so when processing the log we already have the details about the elements inside the cache.

After loading some information, we can start the analysis.

### Show summarized information

The `info` command is very useful to get a general idea of what is happening in your application:

```bash
> info
RUN SUMMARY: 
Classes loaded: 
  -> Cached:9.163 (97,28 %)
  -> Not Cached:256 (2,72 %)
Methods loaded: 
  -> Cached:38.560 (96,09 %)
  -> Not Cached:1.571 (3,91 %)
Lambda Methods (4,37 % of total methods) loaded: 
  -> Cached:197 (11,24 %)
  -> Not Cached:1.556 (88,76 %)
Code Entries: 493
  -> Adapters: 493 (100,00 %)
  -> Shared Blobs: 0 (0,00 %)
  -> C1 Blobs: 0 (0,00 %)
  -> C2 Blobs: 0 (0,00 %)
AOT code cache size: 598432 bytes
AOT CACHE SUMMARY: 
Classes in AOT Cache: 9.163
Methods in AOT Cache: 38.560
  -> ConstMethods size: 38.362 (99,49 %)
  -> MethodCounters size: 0 (0,00 %)
  -> MethodData size: 0 (0,00 %)
ConstantPool: 3.066
  -> ConstantPoolCache: 3.066 (100,00 %)
```

### Listing elements (and errors!) detected

We have the `ls` command to list errors and what we know is on the cache. Most options in all the commands are autocompletable, so you can use `tab` to understand what to fill in there.

```bash
> ls help

Usage:

 ls [-hV] [-i[=<id>]] [--useArrays[=<showArrays>]] [--useNotCached
    [=<useNotCached>]] [-epn[=<exclude>[,<exclude>...]...]]... [-pn
    [=<packageName>[,<packageName>...]...]]... [-t[=<type>[,<type>...]...]]...
    [COMMAND]

Description:

List what is on the cache. By default, it lists everything on the cache.

Options:

      -epn, --excludePackageName[=<exclude>[,<exclude>...]...]
                            Exclude the elements inside this package.
                            Note that some elements don't belong to any
                              particular package.
  -h, --help                Show this help message and exit.
  -i, --identifier[=<id>]   The object identifier. If it is a class, use the
                              full qualified name.
      -pn, --packageName[=<packageName>[,<packageName>...]...]
                            Restrict the command to elements inside this
                              package.
                            Note that some elements don't belong to any
                              particular package
  -t, --type[=<type>[,<type>...]...]
                            Restrict the command to this type of element
      --useArrays[=<showArrays>]
                            Use array classes if true. True by default.
      --useNotCached[=<useNotCached>]
                            Use elements that are used in your app but were not
                              in the AOT Cache. False by default.
  -V, --version             Print version information and exit.

Commands:

  help  Display help information about the specified command.
```

```bash
> ls 
[....]
  > Symbol -> Ljdk/internal/misc/TerminatingThreadLocal<[Lsun/nio/fs/NativeBuffer;>;
  > TypeArrayOther -> 0x0000000800754aa8
  > Object -> (0xffd86a88) java.lang.String "M05"
  > Symbol -> setNormalizedYear
  > Class -> org.infinispan.configuration.cache.GroupsConfiguration
  > Symbol -> java/time/chrono/ThaiBuddhistChronology
  > Method -> void java.util.ArrayList$ArrayListSpliterator.forEachRemaining(java.util.function.Consumer)
  > ConstMethod -> boolean java.util.regex.CharPredicates.lambda$PUNCTUATION$0(int)
  > Method -> void jdk.internal.platform.CgroupSubsystemFactory.<init>()
  > Symbol -> jdk/internal/vm/vector/VectorSupport$VectorBlendOp
  > Object -> (0xffd4ac78) java.util.concurrent.ConcurrentHashMap$Node
  > ConstMethod -> java.util.List java.time.chrono.IsoChronology.eras()
  > Symbol -> Method javax/crypto/SecretKey.getEncoded()[B is abstract
  > TypeArrayU8 -> 0x0000000803e93be8
  > TypeArrayU1 -> 0x0000000802ad86a0
  > Method -> java.io.FileDescriptor sun.nio.ch.Net.serverSocket()
  > ConstMethod -> boolean java.io.ObjectStreamClass.classNamesEqual(java.lang.String, java.lang.String)
  > Class -> io.netty.util.concurrent.FutureListener
  > TypeArrayU1 -> 0x00000008028aac38
  > TypeArrayU1 -> 0x0000000802b17450
  > Object -> (0xffe7f538) [I length: 18
Found 260732 elements.
```

We can filter by type of element and package (the parameters are auto-completable with suggestions):
```bash
> ls -t=ConstantPool -pn=sun.util.locale
  > ConstantPool -> sun.util.locale.BaseLocale
  > ConstantPool -> sun.util.locale.BaseLocale$1
  > ConstantPool -> sun.util.locale.provider.BaseLocaleDataMetaInfo
Found 3 elements.
```

We can also explore the potential errors/warnings/incidents:

```bash
> warning 
[...]
  > Element 'org.infinispan.remoting.transport.jgroups.JGroupsTransport' of type 'Class' couldn't be stored into the A
OTcache because: 'nest host class org/infinispan/remoting/transport/jgroups/JGroupsTransport is excluded'
  > Element 'jdk.proxy1.$Proxy0' of type 'Class' couldn't be stored into the AOTcache because: 'Unsupported location'
  > [Unknown]: 'Preload Warning: Verification failed for org.apache.logging.log4j.core.async.AsyncLoggerContext'
  > [StoringIntoAOTCache]: 'JVM_StartThread() ignored: jdk.internal.misc.InnocuousThread'
  > Element 'org.infinispan.remoting.transport.jgroups.JGroupsTransport' of type 'Class' couldn't be stored into the A
OTcache because: 'nest host class org/infinispan/remoting/transport/jgroups/JGroupsTransport is excluded'
Found 42 warnings.
```

TODO: Add suggestions on how to solve these errors/warnings. Maybe on the `describe` command?

### Looking for details

To explore a bit more about what is on stored on the cache, we can use the command `describe`.

```bash
> describe help

Usage:

 describe [-hV] [-i[=<id>]] [--useArrays[=<showArrays>]] [--useNotCached
          [=<useNotCached>]] [-epn[=<exclude>[,<exclude>...]...]]... [-pn
          [=<packageName>[,<packageName>...]...]]... [-t[=<type>[,
          <type>...]...]]... [COMMAND]

Description:

Describe an object, showing all related info.

Options:

      -epn, --excludePackageName[=<exclude>[,<exclude>...]...]
                            Exclude the elements inside this package.
                            Note that some elements don't belong to any
                              particular package.
  -h, --help                Show this help message and exit.
  -i, --identifier[=<id>]   The object identifier. If it is a class, use the
                              full qualified name.
      -pn, --packageName[=<packageName>[,<packageName>...]...]
                            Restrict the command to elements inside this
                              package.
                            Note that some elements don't belong to any
                              particular package
  -t, --type[=<type>[,<type>...]...]
                            Restrict the command to this type of element
      --useArrays[=<showArrays>]
                            Use array classes if true. True by default.
      --useNotCached[=<useNotCached>]
                            Use elements that are used in your app but were not
                              in the AOT Cache. False by default.
  -V, --version             Print version information and exit.

Commands:

  help  Display help information about the specified command.
```

Depending on if it was loaded from one type of file or another, the details may vary:

```bash
-----
|  Class org.infinispan.server.loader.Loader on address 0x0000000800a58b58 with size 528.
|  This information comes from: 
|    > AOT Map
|  This class has the following methods:
|     ______
|     | void org.infinispan.server.loader.Loader.<init>()
|     | void org.infinispan.server.loader.Loader.main(java.lang.String[], java.lang.String)
|     | void org.infinispan.server.loader.Loader.run(java.lang.String[], java.lang.String, java.util.Properties)
|     | java.lang.ClassLoader org.infinispan.server.loader.Loader.classLoaderFromPath(java.nio.file.Path, java.lang.Cl
assLoader)
|     | java.lang.String org.infinispan.server.loader.Loader.extractArtifactName(java.lang.String)
|     | void org.infinispan.server.loader.Loader.<init>()
|     | void org.infinispan.server.loader.Loader.main(java.lang.String[], java.lang.String)
|     | void org.infinispan.server.loader.Loader.run(java.lang.String[], java.lang.String, java.util.Properties)
|     | java.lang.ClassLoader org.infinispan.server.loader.Loader.classLoaderFromPath(java.nio.file.Path, java.lang.Cl
assLoader)
|     | java.lang.String org.infinispan.server.loader.Loader.extractArtifactName(java.lang.String)
|     | ______
|  There are other elements of the cache that link to this element: 
|    _____
|    | Method -> void org.infinispan.server.loader.Loader.run(java.lang.String[], java.lang.String, java.util.Properti
es)
|    | Method -> void org.infinispan.server.loader.Loader.<init>()
|    | ConstantPoolCache -> org.infinispan.server.loader.Loader
|    | Method -> void org.infinispan.server.loader.Loader.main(java.lang.String[], java.lang.String)
|    | ConstMethod -> java.lang.ClassLoader org.infinispan.server.loader.Loader.classLoaderFromPath(java.nio.file.Path
, java.lang.ClassLoader)
|    | ConstMethod -> java.lang.String org.infinispan.server.loader.Loader.extractArtifactName(java.lang.String)
|    | ConstMethod -> void org.infinispan.server.loader.Loader.run(java.lang.String[], java.lang.String, java.util.Pro
perties)
|    | ConstMethod -> void org.infinispan.server.loader.Loader.<init>()
|    | Symbol -> Loader.java
|    | Symbol -> org/infinispan/server/loader/Loader
|    | ConstMethod -> void org.infinispan.server.loader.Loader.main(java.lang.String[], java.lang.String)
|    | ConstantPool -> org.infinispan.server.loader.Loader
|    | Method -> java.lang.String org.infinispan.server.loader.Loader.extractArtifactName(java.lang.String)
|    | Method -> java.lang.ClassLoader org.infinispan.server.loader.Loader.classLoaderFromPath(java.nio.file.Path, jav
a.lang.ClassLoader)
|    _____
-----
-----
|  ConstantPool org.infinispan.server.loader.Loader on address 0x0000000802f2cb48 with size 2824.
|  This information comes from: 
|    > AOT Map
|  This element refers to :
|   > Class -> org.infinispan.server.loader.Loader
-----
```

If we don't ask to describe something coming from the AOTCache, the information is much more limited:

```bash
> describe -i=org.infinispan.configuration.cache.GroupsConfiguration
-----
|  Class org.infinispan.configuration.cache.GroupsConfiguration with size null.
|  This information comes from: 
|    > Java Log
|  This class has the following methods:
|     ______
|     | null org.infinispan.configuration.cache.GroupsConfiguration.Lambda/0x8000000fa()
|     | null org.infinispan.configuration.cache.GroupsConfiguration.Lambda/0x000000080601e710()
|     | null org.infinispan.configuration.cache.GroupsConfiguration.Lambda/0x000000080501edb8()
|     | ______
-----
```

Or we can filter by type of element we want to explore:

```bash
> describe -i=org.infinispan.server.loader.Loader -t=ConstantPoolCache 
-----
|  ConstantPoolCache org.infinispan.server.loader.Loader on address 0x0000000800a58d68 with size 64.
|  This information comes from: 
|    > AOT Map
|  This element refers to :
|   > Class -> org.infinispan.server.loader.Loader
-----
```

#### Tree information

The `tree` command shows related elements. It can be used with the `describe` command to check details on elements inside the AOT Cache.

```bash
> tree help

Usage:

 tree [-hV] [-i[=<id>]] [--useArrays[=<showArrays>]] [--useNotCached
      [=<useNotCached>]] [-l[=<N>...]] [-max[=<N>...]] [-epn[=<exclude>[,
      <exclude>...]...]]... [-pn[=<packageName>[,<packageName>...]...]]... [-t
      [=<type>[,<type>...]...]]... [COMMAND]

Description:

Shows a tree with elements that are linked to the root.
This means, elements that refer to/use the root element.,
Blue italic elements have already been shown and will not be expanded.

Options:

      -epn, --excludePackageName[=<exclude>[,<exclude>...]...]
                            Exclude the elements inside this package.
                            Note that some elements don't belong to any
                              particular package.
  -h, --help                Show this help message and exit.
  -i, --identifier[=<id>]   The object identifier. If it is a class, use the
                              full qualified name.
  -l, --level[=<N>...]      Maximum number of tree levels to display.
      -max[=<N>...]         Maximum number of elements to display. By default,
                              100. If using -1, it shows all elements. Note
                              that on some cases this may mean showing
                              thousands of elements.
      -pn, --packageName[=<packageName>[,<packageName>...]...]
                            Restrict the command to elements inside this
                              package.
                            Note that some elements don't belong to any
                              particular package
  -t, --type[=<type>[,<type>...]...]
                            Restrict the command to this type of element
      --useArrays[=<showArrays>]
                            Use array classes if true. True by default.
      --useNotCached[=<useNotCached>]
                            Use elements that are used in your app but were not
                              in the AOT Cache. False by default.
  -V, --version             Print version information and exit.

Commands:

  help  Display help information about the specified command.

```

To avoid infinite loops and circular references, each element will be iterated over on the tree only once. Elements that have already appeared on the tree will be colored blue and will not have children.

```bash
> tree sun.util.locale.BaseLocale
+── [Class] sun.util.locale.BaseLocale
 \
  +──  [Object] (0xffe070c0) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe071a0) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06f00) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe07050) sun.util.locale.BaseLocale
  ├──  [ConstantPool] sun.util.locale.BaseLocale
  ├──  [Object] (0xffe07088) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06e58) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe07018) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe07130) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06e20) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06fa8) sun.util.locale.BaseLocale
  ├──  [ConstantPoolCache] sun.util.locale.BaseLocale
  ├──  [Symbol] sun.util.locale.BaseLocale
   \
    +──  [Object] (0xffe94558) java.lang.String "sun.util.locale.BaseLocale"
  ├──  [Object] (0xffe06f70) sun.util.locale.BaseLocale
  ├──  [Symbol] sun/util/locale/BaseLocale
  ├──  [Object] (0xffe06e90) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06fe0) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06ec8) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe071d8) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe07168) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe06f38) sun.util.locale.BaseLocale
  ├──  [Object] (0xffe07210) sun.util.locale.BaseLocale
  ├──  [Symbol] BaseLocale.java
  ├──  [Object] (0xffe070f8) sun.util.locale.BaseLocale
  ├──  [Method] sun.util.locale.BaseLocale.convertOldISOCodes

```

### Cleanup

We can clean the loaded files and start from scratch

```bash
> clean
Cleaned the elements. Load again files to start a new analysis.
> ls
Found 0 elements.
```

### Exiting

Just `exit`.