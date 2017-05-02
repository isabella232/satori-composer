package com.satori.mods.examples;

import com.satori.mods.api.*;
import com.satori.mods.core.async.*;
import com.satori.mods.suite.*;

import org.slf4j.*;

public class HelloWorldMod extends Mod {
  private final static Logger log = LoggerFactory.getLogger(HelloWorldMod.class);
  
  @Override
  public void init(IModContext context) throws Exception {
    super.init(context);
    exec(this::loop);
  }
  
  private void loop() {
    try {
      yield("Hello world", AsyncPromise.from(
        () -> { // success continuation
          loop();
        },
        cause -> { // failure continuation
          log.error("failure", cause);
        }
      ));
    } catch (Exception e) {
      log.error("failure", e);
    }
  }
}

