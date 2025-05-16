package vn.tnteco.common.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jooq.JSON;
import vn.tnteco.common.core.json.JsonArray;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Log4j2
public class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> int size(Collection<T> list) {
        if (list == null || list.isEmpty()) return 0;
        return list.size();
    }

    private static final Collector<?, ?, ?> SHUFFLER = Collectors.collectingAndThen(
            Collectors.toCollection(ArrayList::new),
            list -> {
                Collections.shuffle(list);
                return list;
            }
    );

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, List<T>> toShuffledList() {
        return (Collector<T, ?, List<T>>) SHUFFLER;
    }

    public static <T> boolean empty(Collection<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> List<T> mergeList(Collection<T>... list) {
        return Arrays.stream(list)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
    }

    public static <T> List<T> mergeListDistinct(Collection<T>... list) {
        return Arrays.stream(list)
                .flatMap(Collection::parallelStream)
                .distinct()
                .collect(Collectors.toList());
    }

    public static <K, V> List<K> extractField(Collection<V> list, Function<V, K> fieldFunction) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(fieldFunction)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public static <T, K, V> List<K> extractFieldInList(Collection<T> list,
                                                       Function<T, List<V>> listFunction,
                                                       Function<V, K> fieldFunction) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(listFunction)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(fieldFunction)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public static <K, V> List<K> extractTwoField(Collection<V> list,
                                                 Function<V, K> field1Function,
                                                 Function<V, K> field2Function) {
        return list.stream()
                .flatMap(v -> Stream.of(field1Function.apply(v), field2Function.apply(v)))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public static <K, V> List<K> extractField(Collection<V> list, Function<V, K> fieldFunction, Predicate<V> predicate) {
        return list.stream()
                .filter(predicate)
                .map(fieldFunction)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public static <K, V> Set<K> extractFieldToSet(Collection<V> list, Function<V, K> fieldFunction) {
        return list.stream()
                .map(fieldFunction)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <V> long countByFilter(Collection<V> list, Predicate<V> predicate) {
        return list.stream()
                .filter(predicate)
                .count();
    }

    public static <K, V> Set<K> extractFieldToSet(Collection<V> list, Function<V, K> fieldFunction, Predicate<V> predicate) {
        return list.stream()
                .filter(predicate)
                .map(fieldFunction)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <V> List<V> filterList(Collection<V> list, Predicate<V> predicate) {
        return list.stream()
                .filter(predicate)
                .distinct()
                .collect(Collectors.toList());
    }

    public static <K, V> Map<K, Long> groupCount(Collection<V> list, Predicate<V> predicate,
                                                 Function<V, K> keyFunction) {
        return list.stream()
                .filter(predicate)
                .collect(groupingBy(keyFunction, counting()));
    }

    public static <K, V> Map<K, V> filterCollectToMap(Collection<V> list, Predicate<V> predicate, Function<V, K> keyFunction) {
        return list.stream()
                .filter(predicate)
                .collect(toMap(keyFunction, Function.identity(), (v1, v2) -> v1));
    }

    public static <K, V> Map<K, List<V>> groupByKey(Collection<V> list, Function<V, K> keyFunction) {
        return list.stream()
                .collect(groupingBy(keyFunction));
    }

    public static <K, V> Map<K, List<V>> filterGroup(Collection<V> list, Predicate<V> predicate,
                                                     Function<V, K> keyFunction) {
        return list.stream()
                .filter(predicate)
                .collect(groupingBy(keyFunction));
    }

    public static <K, V> Map<K, V> collectToMap(Collection<V> list, Function<V, K> keyFunction) {
        return list.stream().collect(toMap(keyFunction, Function.identity(), (v1, v2) -> v1));
    }

    public static <K, V> Set<K> collectToSet(Collection<V> list, Function<V, K> keyFunction) {
        return list.stream()
                .map(keyFunction)
                .collect(toSet());
    }

    public static Double createDouble(String value, Double defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return NumberUtils.createDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T, K, V> Map<K, V> collectToMap(Collection<T> list, Function<T, K> keyFunction, Function<T, V> valueFunction) {
        return list.stream().collect(toMap(keyFunction, valueFunction, (v1, v2) -> v1));
    }

    public static <T, K, V> Map<K, V> collectToMap(Collection<T> list, Predicate<T> predicate,
                                                   Function<T, K> keyFunction, Function<T, V> valueFunction) {
        return list.stream()
                .filter(predicate)
                .collect(toMap(keyFunction, valueFunction, (v1, v2) -> v1));
    }

    public static <T> T getOrDefault(T t, T defaultValue) {
        if (t == null) return defaultValue;
        return t;
    }

    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (key == null) return defaultValue;
        return map.getOrDefault(key, defaultValue);
    }

    public static <K, V> V getOrNull(Map<K, V> map, K key) {
        if (key == null) return null;
        return map.getOrDefault(key, null);
    }

    public static <T> List<T> shuffle(List<T> list) {
        Collections.shuffle(list);
        return list;
    }

    public static <T> T shuffleFindFirst(List<T> list) {
        Collections.shuffle(list);
        return list.stream().findFirst().orElse(null);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T> Collection<T> flatList(Collection<? extends Collection<T>> list) {
        return list.stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private static <T> Collector<T, ?, List<T>> limitingList(int limit) {
        return Collector.of(ArrayList::new,
                (l, e) -> {
                    if (l.size() < limit) l.add(e);
                },
                (l1, l2) -> {
                    l1.addAll(l2.subList(0, Math.min(l2.size(), Math.max(0, limit - l1.size()))));
                    return l1;
                });
    }

    public static List<Integer> convertToListInt(JSON json) {
        if (json == null) return Collections.emptyList();
        try {
            return new JsonArray(json.data())
                    .stream()
                    .map(o -> Integer.parseInt(o.toString()))
                    .toList();
        } catch (Exception e) {
            log.error("fail to convert JSON to arr-integer: {}", json.data(), e);
            return Collections.emptyList();
        }
    }

    public static List<Integer> convertToListInt(String input) {
        if (input == null) return Collections.emptyList();
        try {
            return new JsonArray(input)
                    .stream()
                    .map(o -> Integer.parseInt(o.toString()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("fail to convert JSON to arr-integer: {}", input, e);
            return Collections.emptyList();
        }
    }

    public static <T> List<T> getPageElements(List<T> items, int page, int pageSize) {
        if (items == null || items.isEmpty() || page < 1 || pageSize <= 0) {
            return Collections.emptyList();  // Trả về danh sách rỗng nếu đầu vào không hợp lệ
        }

        int startIndex = (page - 1) * pageSize;
        if (startIndex >= items.size()) {
            return Collections.emptyList();  // Tránh lỗi truy cập ngoài danh sách
        }

        int endIndex = Math.min(startIndex + pageSize, items.size());
        return items.subList(startIndex, endIndex);
    }
}
