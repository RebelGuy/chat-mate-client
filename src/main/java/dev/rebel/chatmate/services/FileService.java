package dev.rebel.chatmate.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class FileService {
  private final String dataFolder;
  private final Gson gson;

  public FileService(String dataDir) {
    this.dataFolder = dataDir + (dataDir.endsWith("/") ? "" : "/");
    this.gson = new Gson();

    File dataFolderObject = new File(this.dataFolder);
    if (!dataFolderObject.exists()) dataFolderObject.mkdir();
  }

  /** Returns null if the file was not found. */
  public <T> T readObjectFromFile(String fileName, Class<T> returnClass) throws IOException {
    File file = new File(this.dataFolder + fileName);

    if (file.exists()) {
      Reader reader = Files.newBufferedReader(file.toPath());
      return this.gson.fromJson(reader, returnClass);
    } else {
      return null;
    }
  }

  /** Overwrites contents if the file already exists. */
  public <T> void writeObjectToFile(String fileName, T contents) throws IOException {
    File file = new File(this.dataFolder + fileName);

    if (file.exists()) file.delete();
    file.createNewFile();

    Writer writer = new FileWriter(file);
    this.gson.toJson(contents, writer);
    writer.close();
  }

  /** If appending, will automatically add a newline after the contents. */
  public void writeFile(String fileName, String contents, boolean append) throws IOException {
    File file = new File(this.dataFolder + fileName);

    if (!append) {
      if (file.exists()) file.delete();
      file.createNewFile();
    }

    Writer writer = new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8);
    writer.write(contents + (append ? "\r\n" : ""));
    writer.close();
  }
}
