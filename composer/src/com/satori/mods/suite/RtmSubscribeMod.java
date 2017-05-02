package com.satori.mods.suite;

import com.satori.composer.rtm.*;
import com.satori.composer.runtime.*;
import com.satori.mods.api.*;
import com.satori.mods.core.async.*;
import com.satori.mods.core.config.*;
import com.satori.mods.core.stats.*;

import com.fasterxml.jackson.databind.*;
import io.vertx.core.*;
import org.slf4j.*;

// low latency mode only is supported
// Duplicates are not possible
// Messages can be lost
// Minimum extra latencies
// Minimum overhead

public class RtmSubscribeMod extends Mod {
  public static final Logger log = LoggerFactory.getLogger(RtmSubscribeMod.class);
  
  private final RtmSubscribeModStats stats = new RtmSubscribeModStats();
  private final RtmDriverConfig config;
  
  public static final int defaultWindowMaxSize = 1024 * 1024;
  private int windowMaxSize = defaultWindowMaxSize;
  private int unconsumedMessages = 0;
  private Vertx vertx;
  
  private RtmChannelSubscriber rtm;
  
  public RtmSubscribeMod(JsonNode userData) throws Exception {
    this(Config.parseAndValidate(userData, RtmDriverConfig.class));
  }
  
  public RtmSubscribeMod(RtmDriverConfig config) throws Exception {
    this.config = config;
    log.info("created");
  }
  
  // IMod implementation
  
  @Override
  public void init(final IModContext context) throws Exception {
    super.init(context);
    this.vertx = ((Verticle) context.runtime()).getVertx();
    rtm = new RtmChannelSubscriber(vertx, config, config.channel) {
      @Override
      public void onChannelData(JsonNode msg) {
        RtmSubscribeMod.this.onChannelData(msg);
      }
    };
    rtm.start();
    stats.reset();
    log.info("initialized");
  }
  
  @Override
  public void dispose() throws Exception {
    super.dispose();
    this.vertx = null;
    if (rtm != null) {
      try {
        rtm.stop();
      } catch (Exception e) {
        // swallow exception
        log.error("failed to stop rtm connection", e);
      }
      rtm = null;
    }
    stats.reset();
    log.info("terminated");
  }
  
  @Override
  public void onStats(StatsCycle cycle, IStatsCollector collector) {
    log.debug("collecting statistic...");
    stats.drain(collector);
  }
  
  @Override
  public void onPulse() {
    if (rtm != null) {
      rtm.onPulse(Stopwatch.timestamp());
    }
  }
  
  // private methods
  
  private void onChannelData(JsonNode msg) {
    stats.recv += 1;
    unconsumedMessages += 1;
    if (unconsumedMessages > windowMaxSize) {
      rtm.unsubscribe();
    }
    IAsyncHandler cont = AsyncPromise.from(this::onMessageConsumed);
    try {
      yield(msg, cont);
    } catch (Exception e) {
      cont.fail(e);
    }
  }
  
  private void onMessageConsumed(IAsyncResult ar) {
    unconsumedMessages -= 1;
    if (ar.isFailed()) {
      log.warn("processing message error", ar.getError());
      stats.failed += 1;
    } else {
      stats.succeeded += 1;
    }
    if (unconsumedMessages < windowMaxSize / 2) {
      if (rtm != null) {
        rtm.subscribe();
      }
    }
  }
}
