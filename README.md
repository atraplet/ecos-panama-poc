# PoC ECOS Project Panama

This is a PoC for calling the [Embedded Conic Solver (ECOS)](https://github.com/embotech/ecos) written in C from the JVM
using [Project Panama](https://openjdk.java.net/projects/panama/).

The PoC has been tested on Ubuntu 20.04 (WSL2) using Java 17 (Zulu).

## Stubs Generation

To generate the stubs, install the [Project Panama Early-Access Build](https://jdk.java.net/panama/17/)
for Java 17 that contains the stub generation tool `jextract`.

Then from the project root run

```
$JAVA_HOME/bin/jextract \
        -d src/main/java \
        -l ecos \
        -t com.ustermetrics.ecos.stubs \
        --source \
        -I $ECOS_HOME/external/SuiteSparse_config \
        --include-function ECOS_setup \
        --include-function ECOS_solve \
        --include-function ECOS_cleanup \
        --include-function ECOS_ver \
        --include-struct pwork \
        $ECOS_HOME/include/ecos.h
```

where `JAVA_HOME` and `ECOS_HOME` should point to the JAVA HOME of the Project Panama Early-Access Build and to the
project root of ECOS, respectively. For example

```
export ECOS_HOME=$HOME/ecos
export JAVA_HOME=/usr/lib/jvm/openjdk-17-panama+3-167
```
