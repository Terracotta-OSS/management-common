/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.management.resource.services.events;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.management.ServiceLocator;
import org.terracotta.management.resource.events.EventEntityV2;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * A resource service for sending events.
 * 
 * @author Ludovic Orban
 */
@Path("/v2/events")
public class AllEventsResourceServiceImplV2 {

  private static final Logger LOG = LoggerFactory.getLogger(AllEventsResourceServiceImplV2.class);

  private final EventServiceV2 eventService;

  public AllEventsResourceServiceImplV2() {
    this.eventService = ServiceLocator.locate(EventServiceV2.class);
  }

  @GET
  @Produces(SseFeature.SERVER_SENT_EVENTS)
  public EventOutput getServerSentEvents(@Context UriInfo info, @QueryParam("localOnly") boolean localOnly) {
    LOG.debug(String.format("Invoking AllEventsResourceServiceImplV2.getServerSentEvents: %s", info.getRequestUri()));

    final EventOutput eventOutput = new EventOutput();
    eventService.registerEventListener(new EventServiceV2.EventListener() {
      @Override
      public void onEvent(EventEntityV2 eventEntity) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        eventBuilder.reconnectDelay(100);
        eventBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE);
        eventBuilder.name(EventEntityV2.class.getSimpleName());
        eventBuilder.data(EventEntityV2.class, eventEntity);
        OutboundEvent event = eventBuilder.build();

        try {
          eventOutput.write(event);
        } catch (Exception e) {
          eventService.unregisterEventListener(this);
          try {
            eventOutput.close();
          } catch (IOException ioe) {
            LOG.warn("Error when closing the event output.", ioe);
          }
        }
      }

      @Override
      public void onError(Throwable throwable) {
        LOG.warn("Error when waiting for management events.", throwable);

        eventService.unregisterEventListener(this);
        try {
          eventOutput.close();
        } catch (IOException ioe) {
          LOG.warn("Error when closing the event output.", ioe);
        }
      }
    }, localOnly);

    return eventOutput;
  }

}