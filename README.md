# leyden-analyzer

This is an interactive console to help debug what is happening with the AOT Cache.

This is a very early work-in-progress. This README is going to be deprecated soon, so don't trust it blindly. Just use the `help` command to guide you.

## Packaging and running the application

To make it work, just `mvn package` and then `java -jar target/quarkus-app/quarkus-run.jar`.

It's using picocli and JLine to run. 

## How to use it

There is a helpful help command:
```
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

You should start by using the load command to load different files:

```
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
> load log /home/delawen/git/infinispan-server-15.2.5.Final/aot.log
Adding /home/delawen/git/infinispan-server-15.2.5.Final/aot.log to our analysis.
Now the AOTCache contains 8780 elements.
> load aotCache /home/delawen/git/infinispan-server-15.2.5.Final/aot.map
>>> here comes many parsing errors I haven't debugged yet
Now the AOTCache contains 40771 elements.
```
Now we can start the analysis.

### Listing elements (and errors!) detected

```
> ls help
Usage:  ls [-hV] [COMMAND]
List what is on the cache. By default, it lists everything on the cache.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help     Display help information about the specified command.
  all      Lists everything on the cache.
  classes  Lists classes on the cache.
  errors   Lists errors on creating or loading the AOTCache.
  methods  Lists methods on the cache.
> ls all 
[....]
  > Method -> sun.security.x509.AlgorithmId.<init>
  > Method -> java.lang.invoke.VarHandleLongs$FieldInstanceReadWrite.getAndBitwiseXorRelease
  > Method -> java.lang.classfile.ClassReader.readU2
  > Class -> com.fasterxml.jackson.databind.ser.PropertyFilter
  > Class -> org.infinispan.counter.logging.Log
  > Class -> org.infinispan.security.SecureCache
  > Method -> sun.security.util.Debug.of
  > Class -> org.infinispan.expiration.impl.CorePackageImpl$2
  > Class -> org.infinispan.expiration.impl.CorePackageImpl$1
  > Method -> java.lang.classfile.ClassReader.readU1
Found 40771 elements.
> ls errors
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

### Looking for details

And explore a bit more about what is on stored on the cache (depending if it was read from one file or another, the details may vary):

```
> describe org.infinispan.counter.logging.Log
org.infinispan.counter.logging.Log on address null with 0 methods.
> describe java.util.AbstractMap
java.util.AbstractMap on address 0x00000008007cfdb8 with 18 methods.
 [method] eq
 [method] keySet
 [method] clear
 [method] put
 [method] containsValue
 [method] containsKey
 [method] get
 [method] toString
 [method] isEmpty
 [method] putAll
 [method] entrySet
 [method] equals
 [method] hashCode
 [method] clone
 [method] <init>
 [method] size
 [method] remove
 [method] values
> describe java.util.AbstractMap.putAll
Method putAll [compilation level: unknown] on class java.util.AbstractMap returning void.
```
