# PoC ECOS Project Panama

This is PoC for calling the [Embedded Conic Solver (ECOS)](https://github.com/embotech/ecos) written in C 
from the JVM using [Project Panama](https://openjdk.java.net/projects/panama/).

## Stubs Generation

To generate the stubs, run from the project root

```
export ECOS_HOME=$HOME/ecos

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
