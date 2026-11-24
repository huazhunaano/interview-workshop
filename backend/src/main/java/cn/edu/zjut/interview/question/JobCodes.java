package cn.edu.zjut.interview.question;

import java.util.Locale;
import java.util.Map;

/**
 * 岗位编码归一化（issue #1）。
 *
 * <p>库里与前端岗位画像统一用 {@code java} / {@code frontend}；但 issue 的验收示例写的是
 * {@code java-backend}，外部调用方也常按「语言-方向」命名。这里把常见写法折到标准值，
 * 避免「接口没错、只是名字没对上」就返回空数组。
 *
 * <p>未知编码原样返回小写形式，不加白名单——「这个岗位还没录题」是正常状态，不是 400。
 */
public final class JobCodes {

    private static final Map<String, String> ALIASES = Map.of(
            "java", "java",
            "java-backend", "java",
            "java_backend", "java",
            "javabackend", "java",
            "backend-java", "java",
            "frontend", "frontend",
            "front-end", "frontend",
            "front_end", "frontend",
            "web-frontend", "frontend");

    private JobCodes() {
    }

    /** 归一化岗位编码：去空白、转小写、折叠常见别名；空值原样返回（由调用方判 400）。 */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return text;
        }
        return ALIASES.getOrDefault(text, text);
    }
}
