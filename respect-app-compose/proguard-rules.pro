
# Begin XXHash rules
# XXHash factories uses reflection
-keep class net.jpountz.xxhash.XXHash32JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.XXHash32JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

# XXHash : XXHashFactory uses reflection
-keep class net.jpountz.xxhash.XXHash64JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.XXHash64JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.StreamingXXHash32JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.StreamingXXHash32JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

# XXHash : XXHashFactory uses reflection
-keep class net.jpountz.xxhash.StreamingXXHash64JavaSafe {
    public <init>(...);
    public static ** INSTANCE;
}

-keep class net.jpountz.xxhash.StreamingXXHash64JavaSafe$Factory {
    public <init>(...);
    public static ** INSTANCE;
}

# End XXHash rules


## Napier bug as per https://github.com/AAkira/Napier/issues/132
-keep class io.github.aakira.napier.** {
  *;
}
## End Napier rule
