package tot.log.logconsole.tool;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 程熙cjp：https://blog.csdn.net/weixin_41133233
 * 操作去重工具类
 *
 * @author chengxi
 * @date 2020/3/17 17:15
 */
public class DeduplicationTool{

    /**
     * 自定义函数去重（采用 Predicate函数式判断，采用 Function获取比较key）
     * 内部维护一个 ConcurrentHashMap，并采用 putIfAbsent特性实现
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 只获取重复的数据
     * 
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctNotByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) != null;
    }

}
