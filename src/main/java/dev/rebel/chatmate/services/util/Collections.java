package dev.rebel.chatmate.services.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
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

  public static <T extends Number & Comparable> T max(Stream<T> items) {
    T max = null;
    for (T item : items.collect(Collectors.toList())) {
      if (max ==  null || item.compareTo(max) > 0) {
        max = item;
      }
    }
    return max;
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

  public static <T> List<T> list(T... items) {
    return new ArrayList<>(Arrays.asList(items));
  }

  public static @Nullable <T> T last(List<T> list) { return list.size() == 0 ? null : list.get(list.size() - 1); }

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
}
