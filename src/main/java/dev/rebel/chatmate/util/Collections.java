package dev.rebel.chatmate.util;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Collections {
  public static <T> int sum(List<T> items, Function<T, Integer> mapper) {
    int sum = 0;
    for (int n: items.stream().map(mapper).collect(Collectors.toList())) {
      sum += n;
    }
    return sum;
  }

  public static <T> float sumFloat(List<T> items, Function<T, Float> mapper) {
    float sum = 0;
    for (float n: items.stream().map(mapper).collect(Collectors.toList())) {
      sum += n;
    }
    return sum;
  }

  public static <T extends Number & Comparable> T max(Stream<T> items) {
    T max = null;
    for (T item : items.collect(Collectors.toList())) {
      if (max ==  null || item.compareTo(max) > 0) {
        max = item;
      }
    }
    return max;
  }

  public static <T extends Number & Comparable> T max(List<T> items) {
    return max(items.stream());
  }

    public static <T, N extends Comparable> T max(List<T> items, Function<T, N> valueGetter) {
    T maxItem = null;
    N maxValue = null;
    for (T item : items) {
      N thisValue = valueGetter.apply(item);
      if (thisValue != null && (maxItem ==  null || thisValue.compareTo(maxValue) > 0)) {
        maxItem = item;
        maxValue = thisValue;
      }
    }
    return maxItem;
  }

  public static <T, N extends Comparable> T min(List<T> items, Function<T, N> valueGetter) {
    T minItem = null;
    N minValue = null;
    for (T item : items) {
      N thisValue = valueGetter.apply(item);
      if (thisValue != null && (minItem ==  null || thisValue.compareTo(minValue) < 0)) {
        minItem = item;
        minValue = thisValue;
      }
    }
    return minItem;
  }

  public static <T> T eliminate(Collection<T> items, BiFunction<T, T, T> eliminator) {
    T winner = null;
    for (T item : items) {
      if (winner == null) {
        winner = item;
      } else {
        winner = eliminator.apply(winner, item);
      }
    }
    return winner;
  }

  public static <T> List<T> orderBy(List<T> items, ToDoubleFunction<T> valueGetter) {
    return items.stream().sorted(Comparator.comparingDouble(valueGetter)).collect(Collectors.toList());
  }

  public static <T, G> Map<G, List<T>> groupBy(List<T> items, Function<T, G> groupGetter) {
    Map<G, List<T>> groups = new HashMap<>();
    for (T item : items) {
      G thisGroup = groupGetter.apply(item);
      if (!groups.containsKey(thisGroup)) {
        groups.put(thisGroup, new ArrayList<>());
      }
      groups.get(thisGroup).add(item);
    }
    return groups;
  }

  public static <T, G> List<T> collapseGroups(Map<G, List<T>> groups, ToDoubleFunction<G> sorter) {
    List<T> list = new ArrayList<>();
    for (G group : Collections.orderBy(Collections.list(groups.keySet()), sorter)) {
      list.addAll(groups.get(group));
    }
    return list;
  }

  public static <T, R> List<R> map(List<T> items, Function<T, R> mapper) {
    // dumb
    return items.stream().map(mapper).collect(Collectors.toList());
  }

  public static <T, R> List<R> map(List<T> items, BiFunction<T, Integer, R> mapper) {
    // dumber
    ArrayList<R> result = new ArrayList<>();
    for (int i = 0; i < items.size(); i++) {
      result.add(mapper.apply(items.get(i), i));
    }
    return result;
  }

  public static <K, V, R> Map<K, R> map(Map<K, V> items, Function<V, R> mapper) {
    Map<K, R> result = new HashMap<>();
    items.forEach((k, v) -> result.put(k, mapper.apply(v)));
    return result;
  }

  public static <T, R> List<R> flatMap(List<T> items, Function<T, Iterable<R>> mapper) {
    ArrayList<R> result = new ArrayList<>();
    for (T item : items) {
      for (R inner : mapper.apply(item)) {
        result.add(inner);
      }
    }
    return result;
  }

  public static <T> List<T> filter(List<T> list, Predicate<T> filter) {
    if (list == null) {
      return new ArrayList<>();
    }
    return list.stream().filter(filter).collect(Collectors.toList());
  }

  public static <T> List<T> filter(List<T> list, BiPredicate<T, Integer> filter) {
    if (list == null) {
      return new ArrayList<>();
    }

    List<T> result =  new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      T item = list.get(i);
      if (filter.test(item, i)) {
        result.add(item);
      }
    }
    return result;
  }

  public static <T> List<T> trim(List<T> list, @Nullable Integer maxItems) {
    if (maxItems == null || list.size() <= maxItems) {
      return list;
    } else {
      return list.subList(0, maxItems);
    }
  }

  public static <T> List<T> list(T... items) {
    return new ArrayList<>(Arrays.asList(items));
  }

  public static <T> List<T> list(Iterable<T> collection) {
    List<T> list = new ArrayList<>();
    collection.forEach(list::add);
    return list;
  }

  public static @Nullable <T> T first(@Nullable List<T> list) {
    return (list == null || list.size() == 0) ? null : list.get(0);
  }

  public static @Nullable <T> T first(@Nullable List<T> list, Predicate<T> predicate) {
    if (list == null || list.size() == 0) {
      return null;
    } else {
      return Collections.first(Collections.filter(list, predicate));
    }
  }

  public static @Nullable <T> T last(@Nullable List<T> list) {
    return (list == null || list.size() == 0) ? null : list.get(list.size() - 1);
  }

  public static @Nullable <T> T last(@Nullable List<T> list, Predicate<T> predicate) {
    if (list == null || list.size() == 0) {
      return null;
    } else {
      return Collections.first(Collections.filter(Collections.reverse(list), predicate));
    }
  }

  public static <T> boolean any(@Nullable List<T> list) { return list != null && list.size() != 0; }

  public static <T> boolean any(@Nullable List<T> list, Predicate<T> predicate) {
    if (list == null || list.size() == 0) {
      return false;
    } else {
      return Collections.filter(list, predicate).size() > 0;
    }
  }

  public static <T> boolean any(@Nullable List<T> list, BiPredicate<T, Integer> predicate) {
    if (list == null || list.size() == 0) {
      return false;
    } else {
      return Collections.filter(list, predicate).size() > 0;
    }
  }

  public static <T> boolean all(@Nullable List<T> list, Predicate<T> predicate) {
    if (list == null || list.size() == 0) {
      return false;
    } else {
      return filter(list, predicate).size() == list.size();
    }
  }

  public static <T> int size(@Nullable List<T> list) { return list == null ? 0 : list.size(); }

  public static <T> T elementAt(List<T> list, int index) {
    int N = list.size();
    if (index < 0) {
      return elementAt(list, index + N);
    } else if (index > N - 1) {
      return elementAt(list, index - N);
    } else {
      return list.get(index);
    }
  }

  public static <T> List<T> reverse(List<T> list) {
    List<T> result = new ArrayList<>();
    for (int i = list.size() - 1; i >= 0; i--) {
      result.add(list.get(i));
    }
    return result;
  }

  public static <T> List<T> without(@Nullable List<T> list, @Nullable T itemToExclude) {
    if (list == null) {
      return new ArrayList<>();
    }
    return list.stream().filter(item -> item != itemToExclude).collect(Collectors.toList());
  }

  public static <T> List<T> replaceOne(@Nullable List<T> list, T replacement, Predicate<T> replacementPredicate) {
    if (list == null) {
      return new ArrayList<>();
    }

    @Nullable T oldItem = first(list, replacementPredicate);
    if (oldItem == null) {
      return list;
    }

    list = list(list); // lol
    int index = list.indexOf(oldItem);
    list.remove(index);
    list.add(index, replacement);
    return list;
  }

  public static <T> List<T> after(@Nullable List<T> list, @Nullable T item) {
    if (list == null) {
      return new ArrayList<>();
    } else if (item == null || !list.contains(item)) {
      return list;
    }

    int index = list.indexOf(item);
    return list.subList(index + 1, list.size());
  }
}
