# ── kotlinx.serialization ────────────────────────────────────────────
# @Serializable 모델과 type-safe navigation 라우트가 여기에 걸린다.
# 직렬화기가 제거되면 저장된 JSON을 못 읽거나(=사용자 데이터 유실로 보임)
# 화면 이동에서 크래시가 난다.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── 크래시 스택을 읽기 위한 최소 정보 ────────────────────────────────
# 줄 번호를 남기고 원본 파일명은 감춘다. mapping.txt 와 함께 쓴다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
