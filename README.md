# leyden-analyzer

This is an interactive console to help debug what is happening with the AOT Cache.

This is a very early work-in-progress. This README is going to be deprecated soon, so don't trust it blindly. Just use the `help` command to guide you.

## Packaging and running the application

To make it work, just `mvn package` and then `java -jar target/quarkus-app/quarkus-run.jar`.

It's using picocli and JLine to run. 

## How to use it

There is a `help` command:
```bash
> help
Usage:  [COMMAND]
Interactive shell to explore the contents of the AOT cache. Start by loading an
AOT map file.
Commands:
  clean       Empties the information loaded.
  describe    Describe an object, showing all related info.
  ls          List what is on the cache. By default, it lists everything on the
                cache.
  load        Load a file to extract information.
  cls, clear  Clears the screen
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
              log:tags
```
We can load java logs of our app generated with `-Xlog:class+load,aot*=warning:file=aot.
              log:tags` :
```bash
> load log /home/delawen/git/infinispan-server-15.2.5.Final/aot.log /home/delawen/git/infinispan-server-15.2.5.Final/aot.log.0
Adding /home/delawen/git/infinispan-server-15.2.5.Final/aot.log to our analysis.
Now the AOTCache contains 15084 elements and 35 errors.
Adding /home/delawen/git/infinispan-server-15.2.5.Final/aot.log.1 to our analysis.
Now the AOTCache contains 15084 elements and 35 errors.
```
And we can add an AOT map file generated with `-Xlog:aot+map=trace:file=aot.
map:none:filesize=0`
```bash
> load aotCache /home/delawen/git/infinispan-server-15.2.5.Final/aot.map.
Adding /home/delawen/git/infinispan-server-15.2.5.Final/aot.map to our analysis.
.. the AOT Cache contains now 5000 elements ...
Now the AOTCache contains 6718 elements and 0 errors.
```
Now we can start the analysis.

### Listing elements (and errors!) detected

We have the `ls` command to list errors and what we know is on the cache.
```bash
> ls help
Usage:  ls [-hV] [-pn=<packageName>] [<type>...] [COMMAND]
List what is on the cache. By default, it lists everything on the cache.
      [<type>...]   Restrict the listing to this type of element
  -h, --help        Show this help message and exit.
      -pn, --packageName=<packageName>
                    Restrict the listing to this package.
  -V, --version     Print version information and exit.
Commands:
  help  Display help information about the specified command.
  run   Lists everything on the cache.
```

```bash
> ls 
[....]
  > Annotations -> [random generated key] 1758018320071
  > Method -> java.net.URL.getUserInfo
  > Symbol -> Method javax/sql/DataSource.getParentLogger()Ljava/util/logging/Logger; is abstract
  > Method -> java.lang.AbstractStringBuilder.length
  > TypeArrayU4 -> [random generated key] 1758018319895
  > TypeArrayU2 -> [random generated key] 1758018319895
  > TypeArrayU1 -> [random generated key] 1758018319895
  > Class -> java.lang.management.ManagementFactory$$Lambda/0x8000000b0
  > Class -> org.jgroups.util.IntHashMap
  > Class -> java.lang.annotation.RetentionPolicy
  > Method -> jdk.internal.vm.vector.VectorSupport.libraryBinaryOp
  > Symbol -> java/lang/invoke/LambdaForm$DMH+0x80000001d
  > TypeArrayU4 -> [random generated key] 1758018319894
  > Method -> java.util.concurrent.atomic.AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl.isSamePackage
  > Class -> org.infinispan.xsite.spi.XSiteEntryMergePolicy
  > Method -> java.lang.VirtualThread.unblockVirtualThreads
  > Class -> org.infinispan.commons.util.ArrayMap
  > TypeArrayU2 -> [random generated key] 1758018319894
  > Class -> com.fasterxml.jackson.databind.cfg.MapperConfigBase
  > TypeArrayU1 -> [random generated key] 1758018319894
  > Symbol -> java/lang/ProcessHandleImpl$$Lambda+0x8000000a6
  > Class -> org.apache.logging.log4j.core.layout.PatternLayout$SerializerBuilder
  > AdapterHandlerEntry -> [random generated key] 1758017931833
  Found 15704 elements.
```
We can also explore the errors:
```bash
> ls error
[...]
  > Element 'org.apache.logging.log4j.core.async.AsyncLoggerContext' of type 'Class' couldn't be added to the cache because: 'Failed verification'
  > Element 'jdk.proxy1.$Proxy36' of type 'Class' couldn't be added to the cache because: 'Unsupported location'
  > Element 'org.apache.logging.slf4j.Log4jMarkerFactory' of type 'Class' couldn't be added to the cache because: 'Old class has been linked'
  > Element 'jdk.proxy1.$Proxy18' of type 'Class' couldn't be added to the cache because: 'Unsupported location'
  > Element 'org.apache.logging.slf4j.Log4jLoggerFactory$$Lambda+0x800000258' of type 'Method' couldn't be added to the cache because: 'nest_host class org/apache/logging/slf4j/Log4jLoggerFactory is excluded'
  > Element 'org.slf4j.Marker' of type 'Class' couldn't be added to the cache because: 'Unlinked class not supported by AOTConfiguration'
  > Element 'jdk.internal.event.ThreadSleepEvent' of type 'Class' couldn't be added to the cache because: 'JFR event class'
  > Element 'org.slf4j.event.LoggingEvent' of type 'Class' couldn't be added to the cache because: 'Unlinked class not supported by AOTConfiguration'
Found 35 errors.
```
TODO: Add suggestions on how to solve these errors/warnings. Maybe on the `describe` command?

TODO: Detect more errors, these were just the low hanging fruits with `ERROR` label.

### Looking for details

To explore a bit more about what is on stored on the cache, we can use the command `describe`.

```bash
> describe help
Usage:  describe [-hV] [-t=<type>] <name> [COMMAND]
Describe an object, showing all related info.
      <name>          The object name/identifier. If it is a class, use the
                        full qualified name.
  -h, --help          Show this help message and exit.
  -t, --type=<type>   Restrict the listing to this type of element
  -V, --version       Print version information and exit.
Commands:
  help  Display help information about the specified command.
```

Depending if it was loaded from one type of file or another, the details may vary:

```bash
> describe jdk.internal.vm.vector.VectorSupport.libraryBinaryOp
-----
|  Method jdk.internal.vm.vector.VectorSupport.libraryBinaryOp on address 0x0000000802bc8da0 with size 208.
|  This information comes from: 
|    > AOT Map
|  This is a ConstMethod.
|  Compilation level unknown.
|  Belongs to the class jdk.internal.vm.vector.VectorSupport
|  Returns jdk.internal.vm.vector.VectorSupport$VectorPayload.
-----
```

```bash
> describe org.infinispan.commons.util.ArrayMap
-----
|  Class org.infinispan.commons.util.ArrayMap with size null.
|  This information comes from: 
|    > Java Log
-----
```

```bash
> describe java.lang.ref.WeakReference
-----
|  Class java.lang.ref.WeakReference on address 0x00000008007abc00 with size 600.
|  This information comes from: 
|    > AOT Map
|  This class has the following methods:
|     ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x00000008007ac0b0 with size 88.
|     | This information comes from: 
|     |   > AOT Map
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x00000008007ac148 with size 88.
|     | This information comes from: 
|     |   > AOT Map
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x0000000802bcd5e0 with size 104.
|     | This information comes from: 
|     |   > AOT Map
|     | This is a ConstMethod.
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x0000000802bcd648 with size 120.
|     | This information comes from: 
|     |   > AOT Map
|     | This is a ConstMethod.
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x00000008007ac0b0 with size 88.
|     | This information comes from: 
|     |   > AOT Map
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x00000008007ac148 with size 88.
|     | This information comes from: 
|     |   > AOT Map
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x0000000802bcd5e0 with size 104.
|     | This information comes from: 
|     |   > AOT Map
|     | This is a ConstMethod.
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|     | 
|     | Method java.lang.ref.WeakReference.<init> on address 0x0000000802bcd648 with size 120.
|     | This information comes from: 
|     |   > AOT Map
|     | This is a ConstMethod.
|     | Compilation level unknown.
|     | Belongs to the class java.lang.ref.WeakReference
|     | Returns void.
|     | 
|     | ______
|  There are other elements of the cache that link to this element: 
|    _____
|    | ConstantPoolCache java.lang.ref.WeakReference on address 0x00000008007ac058 with size 64.
|    | This information comes from: 
|    |   > AOT Map
|    | This element refers to Class -> java.lang.ref.WeakReference
|    | ConstantPool java.lang.ref.WeakReference on address 0x0000000802bcd470 with size 344.
|    | This information comes from: 
|    |   > AOT Map
|    | This element refers to Class -> java.lang.ref.WeakReference
|    _____
-----
-----
|  ConstantPoolCache java.lang.ref.WeakReference on address 0x00000008007ac058 with size 64.
|  This information comes from: 
|    > AOT Map
|  This element refers to Class -> java.lang.ref.WeakReference
-----
-----
|  ConstantPool java.lang.ref.WeakReference on address 0x0000000802bcd470 with size 344.
|  This information comes from: 
|    > AOT Map
|  This element refers to Class -> java.lang.ref.WeakReference
-----
```
Or we can filter by type of element we want to explore:

```bash
> describe java.lang.ref.WeakReference --type=constantpool
-----
|  ConstantPool java.lang.ref.WeakReference on address 0x0000000802bcd470 with size 344.
|  This information comes from:
|    > AOT Map
|  This element refers to Class -> java.lang.ref.WeakReference
-----
```

TODO: How can we make this more understandable, showing maybe a graph?

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