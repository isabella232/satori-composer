package com.satori.mods.api;

import com.satori.mods.core.async.*;

import java.io.*;

import com.fasterxml.jackson.databind.*;

public interface IModContext extends IModOutput {
  
  IModRuntime runtime();
  
  default IModOutput output() {
    return null;
  }
  
  default void yield(JsonNode data, IAsyncHandler cont) throws Exception {
    IModOutput output = output();
    if (output == null) {
      cont.fail(new Exception("not defined"));
      return;
    }
    output.yield(data, cont);
  }
  
  void exec(Runnable action) throws Exception;
  
  Closeable timer(long delay, Runnable action);
  
}
