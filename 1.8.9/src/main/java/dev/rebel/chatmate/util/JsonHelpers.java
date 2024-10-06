package dev.rebel.chatmate.util;

import com.google.gson.Gson;

public class JsonHelpers {
  public static <Serialised> Serialised parseSerialisedObject(Object serialisedObject, Class<Serialised> clazz) {
    return new Gson().fromJson(new Gson().toJson(serialisedObject), clazz);
  }
}
