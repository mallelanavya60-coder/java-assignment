package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationGatewayTest {

  @Test
      public void testWhenResolveExistingLocationShouldReturn() {
      // given
      LocationGateway locationGateway = new LocationGateway();

      // when
      Location location =
              locationGateway.resolveByIdentifier("ZWOLLE-001");

      // then
      assertNotNull(location);
      assertEquals("ZWOLLE-001", location.identification);
  }

    @Test
    void returnsNullForUnknownIdentifier() {
        LocationGateway gateway = new LocationGateway();

        Location location = gateway.resolveByIdentifier("UNKNOWN");

        assertNull(location);
    }

    @Test
    void returnsNullForNullIdentifier() {
        LocationGateway gateway = new LocationGateway();

        assertNull(gateway.resolveByIdentifier(null));
    }

    @Test
    void returnsNullForBlankIdentifier() {
        LocationGateway gateway = new LocationGateway();

        assertNull(gateway.resolveByIdentifier(" "));
    }

}
