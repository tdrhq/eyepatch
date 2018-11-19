.class public Lio/jippo/SkeletonGeneratorTest$DummyClass;
.super Ljava/lang/Object;
.source "io.jippo.SkeletonGeneratorTest$DummyClass.generated"

.field public static __jippo_fields:Ljava/util/Set;

.method private static constructor <clinit>()V
  .registers 1
    new-instance v0, Ljava/util/HashSet;
    invoke-direct { v0 }, Ljava/util/HashSet;-><init>()V
    sput-object v0, Lio/jippo/SkeletonGeneratorTest$DummyClass;->__jippo_fields:Ljava/util/Set;
    nop # bad op
.end method

.method public constructor <init>()V
  .registers 1
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public foo()Ljava/lang/String;
  .registers 8
    const/4 v5, 0
    const/4 v4, 1
    const/4 v6, 0
    new-array v3, v5, [Ljava/lang/Object;
    const-string v2, "io.jippo.JippoClassLoaderTest$Simple#foo"
    const-string v1, "mockedreturn"
    return-object v1
.end method
