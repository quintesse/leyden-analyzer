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

There is a `help` command that is very self-explanatory. Please, use it. 

You can jump to the **[example of usage](#example-of-usage)** if you are in a hurry, but that does not contain all the possiblities this tool offers. Really, use the `help` command. Documentation matters.

### Colors

This tool uses a lot of colors to make reading and understanding of the content easier. As a general guide, this is their meaning:

* Green: Good, as expected, you can ignore it.
* Red: Warning, bad, note this.
* Yellow: Type of asset. Typically, it can be Method, Class, TrainingData,...
* Cyan: Identifier for a Class, Method, Warning,...
### Load some information

You should start by using the `load` command to load different files. We can load java logs of our app generated 
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
> If you mix production and training runs, there's no way
> we can distinguish if an issue happened during the 
> creation of the cache or during the loading of the cache.

You may mix logs and aot map files from the same training or production run. Then, the information will complement each other. It is recommended to load first the AOT cache map so when processing the log we already have the details about the elements inside the cache.

After loading some information, we can start the analysis.

### Show summarized information

The `info` command is very useful to get a general idea of what is happening in your application:

```bash
> info
RUN SUMMARY: 
Classes loaded: 
  -> Cached:2.791 (18,41 %)
  -> Not Cached:12.372 (81,59 %)
Lambda Methods loaded: 
  -> Cached:0 (-0,00 %)
  -> Not Cached:1.934 (100,00 %)
  -> Portion of methods that are lambda: 1.933 (1,59 %)
AOT CACHE SUMMARY: 
Classes in AOT Cache: 2.791
  -> KlassTrainingData: 964 (34,54 %)
Objects in AOT Cache: 50.709
Methods in AOT Cache: 121.387
  -> MethodCounters: 6.509 (5,36 %)
  -> MethodData: 4.133 (3,40 %)
  -> MethodTrainingData: 4.661 (3,84 %)
  -> CompileTrainingData: 
      -> Level 1: 475 (0,39 %)
      -> Level 3: 2.780 (2,29 %)
      -> Level 4: 554 (0,46 %)
Adapters: 
  -> AdapterFingerPrint: 493
  -> AdapterHandlerEntry: 493
RecordComponent: 134
Misc Data: 2
```

### Listing assets

We have the `ls` command to list what we know is on the cache. Most options in all the commands are autocompletable, so you can use `tab` to understand what to fill in there.

```bash
> ls 
[....]
[Symbol] (Ljava/lang/classfile/ClassFileElement;)Ljava/lang/classfile/ClassFileBuilder;
[Symbol] (Ljava/lang/classfile/ClassFileElement;)V
[Symbol] (Ljava/lang/classfile/ClassFileTransform;Ljava/lang/classfile/ClassFileBuilder;)Ljdk/internal/classfile/impl/
TransformImpl$ResolvedTransform;
[ConstMethod] void org.infinispan.remoting.transport.jgroups.JGroupsMetricsManagerImpl.lambda$stop$1(org.infinispan.re
moting.transport.jgroups.JGroupsMetricsManagerImpl$ClusterMetrics)
[Untrained][Method] void org.infinispan.remoting.transport.jgroups.JGroupsMetricsManagerImpl.lambda$stop$1(org.infinis
pan.remoting.transport.jgroups.JGroupsMetricsManagerImpl$ClusterMetrics)
[ConstMethod] void org.infinispan.remoting.transport.jgroups.JGroupsMetricsManagerImpl.onChannelConnected(org.jgroups.
JChannel, boolean)
[Untrained][Method] void org.infinispan.remoting.transport.jgroups.JGroupsMetricsManagerImpl.onChannelConnected(org.jg
roups.JChannel, boolean)
Found 685694 elements.
```

We can filter by type of element and package (the parameters are auto-completable with suggestions):
```bash
> ls -t=ConstantPool -pn=sun.util.locale
[...]
[ConstantPool] sun.util.locale.provider.NumberFormatProviderImpl
[ConstantPool] sun.util.locale.provider.LocaleProviderAdapter$Type
[ConstantPool] sun.util.locale.provider.LocaleProviderAdapter$$Lambda/0x800000099
[ConstantPool] sun.util.locale.provider.LocaleResources$ResourceReference
Found 32 elements.
```
### Search for warnings

We can also explore the potential errors/warnings/incidents. They may have been loaded from a log file or they can be auto-detected.

```bash
> warning
000 [Unknown] Preload Warning: Verification failed for org.infinispan.remoting.transport.jgroups.JGroupsRaftManager
001 [Unknown] Preload Warning: Verification failed for org.apache.logging.log4j.core.async.AsyncLoggerContext
002 [StoringIntoAOTCache] Element 'org.apache.logging.log4j.core.async.AsyncLoggerContext' of type 'Class' couldn't be
 stored into the AOTcache because: Failed verification
003 [StoringIntoAOTCache] Element 'org.infinispan.remoting.transport.jgroups.JGroupsRaftManager' of type 'Class' could
n't be stored into the AOTcache because: Failed verification
Found 4 warnings.
```

If you want to auto-detect issues, you can run the command `warning check <n>` limiting the search for each type of warning to `n`, which will show the `n`-most relevant warnings.

```bash
> warning check 3
Trying to detect problems...
000 [Unknown] Preload Warning: Verification failed for org.infinispan.remoting.transport.jgroups.JGroupsRaftManager
001 [Unknown] Preload Warning: Verification failed for org.apache.logging.log4j.core.async.AsyncLoggerContext
002 [StoringIntoAOTCache] Element 'org.apache.logging.log4j.core.async.AsyncLoggerContext' of type 'Class' couldn't be
stored into the AOTcache because: Failed verification
003 [StoringIntoAOTCache] Element 'org.infinispan.remoting.transport.jgroups.JGroupsRaftManager' of type 'Class' could
n't be stored into the AOTcache because: Failed verification
024 [Training] Package 'org.apache.logging' contains 763 classes loaded and not cached.
025 [Training] Package 'io.reactivex.rxjava3' contains 724 classes loaded and not cached.
026 [Training] Package 'org.infinispan.server' contains 528 classes loaded and not cached.
027 [Training] Package 'org.infinispan.protostream' contains 42 methods that were called during training run but lack
full training (don't have some of the TrainingData objects associated to them).
028 [Training] Package 'io.reactivex.rxjava3' contains 30 methods that were called during training run but lack full t
raining (don't have some of the TrainingData objects associated to them).
029 [Training] Package 'org.apache.logging' contains 20 methods that were called during training run but lack full tra
ining (don't have some of the TrainingData objects associated to them).
Found 10 warnings.
The auto-detected issues may or may not be problematic.
It is up to the developer to decide that.
``````

> **The auto-detected issues may or may not be problematic. It is up to the developer to decide that.**

You can clean up the list by using the `warning rm <id>` command. 

### Looking for details

To explore a bit more about what is on stored on the cache, we can use the command `describe`. 

Depending on if it was loaded from one type of file or another, the details may vary:

```bash
-----
|  Class org.infinispan.server.loader.Loader on address 0x0000000800a59208 with size 528.
|  This information comes from: 
|    > AOT Map
|    > Java Log
|  This class has 5 Methods, of which 1 have been run and 1 have been trained.
|  It has a KlassTrainingData associated to it.
-----
```

It has a verbose option to show a bit more info:

```bash
-----
|  Class org.infinispan.server.loader.Loader on address 0x0000000800a59208 with size 528.
|  This information comes from: 
|    > AOT Map
|    > Java Log
|  This class has 5 Methods, of which 1 have been run and 1 have been trained.
|  It has a KlassTrainingData associated to it.
|  There are no elements referenced from this element.
|  Elements that refer to this element: 
|    _____
|    | [ConstantPool] org.infinispan.server.loader.Loader
|    | [KlassTrainingData] org.infinispan.server.loader.Loader
|    | [Untrained][Method] void org.infinispan.server.loader.Loader.main(java.lang.String[])
|    | [Untrained][Method] void org.infinispan.server.loader.Loader.<init>()
|    | [Untrained][Method] void org.infinispan.server.loader.Loader.run(java.lang.String[], java.util.Properties)
|    | [Untrained][Method] java.lang.ClassLoader org.infinispan.server.loader.Loader.classLoaderFromPath(java.nio.file
.Path, java.lang.ClassLoader)
|    | [Trained][Method] java.lang.String org.infinispan.server.loader.Loader.extractArtifactName(java.lang.String)
|    | [Symbol] Loader.java
|    | [Symbol] org/infinispan/server/loader/Loader
|    _____
-----
```

#### Tree information

The `tree` command shows related elements. It can be used with the `describe` command to check details on elements inside the AOT Cache.

To avoid infinite loops and circular references, each element will be iterated over on the tree only once. Elements that have already appeared on the tree will be colored blue and will not have children.

```bash
> tree -i=org.infinispan.xsite.NoOpBackupSender --level=0
+ [Untrained][Class] org.infinispan.xsite.NoOpBackupSender
 \
  + [Untrained][Method] java.lang.String org.infinispan.xsite.NoOpBackupSender.toString()
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupPrepar
e(org.infinispan.commands.tx.PrepareCommand, org.infinispan.transaction.impl.AbstractCacheTransaction, jakarta.transac
tion.Transaction)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupCommit
(org.infinispan.commands.tx.CommitCommand, jakarta.transaction.Transaction)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupRollba
ck(org.infinispan.commands.tx.RollbackCommand, jakarta.transaction.Transaction)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupWrite(
org.infinispan.commands.write.WriteCommand, org.infinispan.commands.write.WriteCommand)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupClear(
org.infinispan.commands.write.ClearCommand)
  |
  + [Untrained][Method] void org.infinispan.xsite.NoOpBackupSender.<init>()
  |
  + [Untrained][Method] void org.infinispan.xsite.NoOpBackupSender.<clinit>()
  |
  + [Trained][Method] org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()
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

## Example of Usage

The following section contains examples on how to use this tool to improve your training runs and get better performance thanks to the AOT Cache in Java.

### Detecting classes that should have been trained but were not trained

The title is self-explanatory.

#### Loading the information

We start by loading an aot cache map file to the tool:

```bash
load aotCache aot.map
```
```
Adding aot.map to our analysis...
This is a big file. The size of this file is 395 MB. This may take a while.
Consider using the `--background` option to load this file.
File aot.map added in 10108ms.
```

Note that as I haven't load any log file, we only work with information coming from the AOT cache, which is limited.

#### Asking the tool to find potential improvement areas

Then I ask for automatic warning checks:

```bash 
warning check
```
```
Trying to detect problems...
000 [Training] Package 'org.infinispan.protostream' contains 42 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
001 [Training] Package 'io.reactivex.rxjava3' contains 31 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
002 [Training] Package 'org.apache.logging' contains 20 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
003 [Training] Package 'org.infinispan.factories' contains 15 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
004 [Training] Package 'org.infinispan.configuration' contains 11 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
005 [Training] Package 'org.infinispan.commons' contains 11 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
006 [Training] Package 'io.micrometer.core' contains 5 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
007 [Training] Package 'org.infinispan.xsite' contains 5 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
008 [Training] Package 'org.infinispan.metrics' contains 5 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
009 [Training] Package 'org.jboss.logging' contains 5 methods that were called during training run but lack full training (don't have  some of the TrainingData objects associated to them).
Found 10 warnings.
The auto-detected issues may or may not be problematic.
It is up to the developer to decide that.
```

So I take for example warning `007`: the `org.infinispan.xsite` package contains methods that were run but not trained. 

#### Investigating the warning

Now I can list all methods in that package that were run.

```bash
ls --run -pn=org.infinispan.xsite
```
```
[Trained][Method] void org.infinispan.xsite.ClusteredCacheBackupReceiver.<init>()
[Trained][Method] org.infinispan.xsite.status.NoOpTakeOfflineManager org.infinispan.xsite.status.NoOpTakeOfflineManager.getInstance()
[Trained][Method] org.infinispan.xsite.metrics.NoOpXSiteMetricsCollector org.infinispan.xsite.metrics.NoOpXSiteMetricsCollector.getInstance()
[Trained][Method] org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()
[Trained][Method] void org.infinispan.xsite.statetransfer.NoOpXSiteStateTransferManager.<init>()
Found 5 elements.
```

And I found five elements. I can take any of them and see what is happening to it:

```bash
 describe -t=Method -i="org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()"
 ```
```
-----
|  Method org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance() on address 0x00000008017116c0 with size 88.
|  This information comes from: 
|    > AOT Map
|  This element refers to 1 other elements.
|  Belongs to the class org.infinispan.xsite.NoOpBackupSender
|  It has a MethodCounters associated to it, which means it was called at least once during training run.
|  It has no CompileTrainingData associated to it.
|  It has a MethodData associated to it.
|  It has a MethodTrainingData associated to it.
-----
```

So I discover there is no CompileTrainingData associated to it. 

This may mean that this method was not executed enough times during the training run to be worth recompiled and have CompileTrainingData associated to it. Or it may be that the associated data was rejected for some reason. But now I have something to investigate further.

#### Looking for extra information

I can also check what is happening on the main class for that method:

```bash
 describe -t=Class -i=org.infinispan.xsite.NoOpBackupSender
 ```
```
-----
|  Class org.infinispan.xsite.NoOpBackupSender on address 0x0000000801711128 with size 624.
|  This information comes from: 
|    > AOT Map
|  This class has 9 Methods, of which 1 have been run and 1 have been trained.
|  This class doesn't seem to have training data. 
-----
```

I can see the list of trained and untrained methods for that class using the `tree` command, where I use level=0 because I don't need any depth on the tree to search for what I am looking for.

```bash
tree -i=org.infinispan.xsite.NoOpBackupSender --level=0
```
```
+ [Untrained][Class] org.infinispan.xsite.NoOpBackupSender
 \
  + [Untrained][Method] java.lang.String org.infinispan.xsite.NoOpBackupSender.toString()
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupPrepar
e(org.infinispan.commands.tx.PrepareCommand, org.infinispan.transaction.impl.AbstractCacheTransaction, jakarta.transac
tion.Transaction)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupCommit
(org.infinispan.commands.tx.CommitCommand, jakarta.transaction.Transaction)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupRollba
ck(org.infinispan.commands.tx.RollbackCommand, jakarta.transaction.Transaction)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupWrite(
org.infinispan.commands.write.WriteCommand, org.infinispan.commands.write.WriteCommand)
  |
  + [Untrained][Method] org.infinispan.interceptors.InvocationStage org.infinispan.xsite.NoOpBackupSender.backupClear(
org.infinispan.commands.write.ClearCommand)
  |
  + [Untrained][Method] void org.infinispan.xsite.NoOpBackupSender.<init>()
  |
  + [Untrained][Method] void org.infinispan.xsite.NoOpBackupSender.<clinit>()
  |
  + [Trained][Method] org.infinispan.xsite.NoOpBackupSender org.infinispan.xsite.NoOpBackupSender.getInstance()
  ```