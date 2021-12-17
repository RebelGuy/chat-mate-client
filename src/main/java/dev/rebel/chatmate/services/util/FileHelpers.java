package dev.rebel.chatmate.services.util;

import dev.rebel.chatmate.ChatMate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class FileHelpers {
  public static Stream<String> readLines(String fileName) {
    InputStream stream = ChatMate.class.getResourceAsStream(fileName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    return reader.lines();
  }
}
