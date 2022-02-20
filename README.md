# ecos-java

To generate the stubs, run

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
