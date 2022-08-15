package dev.rebel.chatmate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class Environment {
  public final Env env;
  public final String serverUrl;
  public final String studioUrl;
  public final String buildName;

  private Environment(Env env, String serverUrl, String studioUrl, String buildName) {
    this.env = env;
    this.serverUrl = serverUrl;
    this.studioUrl = studioUrl;
    this.buildName = buildName;
  }

  public static Environment parseEnvironmentFile(Stream<String> lines) {
    Map<String, String> map = new HashMap<>();
    for (String line : lines.toArray(String[]::new)) {
      String[] split = line.split("=");
      if (split.length < 2) {
        throw new RuntimeException("Unable to parse environment line " + line);
      }

      map.put(split[0], String.join("=", Arrays.stream(split).skip(1).toArray(String[]::new)));
    }

    Function<String, String> getValue = (key) -> {
      if (map.containsKey(key)) {
        return map.get(key);
      } else {
        throw new RuntimeException("The environment does not contain a value for the key " + key);
      }
    };

    return new Environment(
        Enum.valueOf(Env.class, getValue.apply("ENVIRONMENT").toUpperCase()),
        getValue.apply("SERVER_URL"),
        getValue.apply("STUDIO_URL"),
        getValue.apply("BUILD_NAME")
    );
  }

  public enum Env { LOCAL, DEBUG, RELEASE }
}
