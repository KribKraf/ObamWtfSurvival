package ch.ismoo.obamcraft.wtf;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SpawnPolicyTest {
  @Test
  void exactEightySquareHasNoOffByOne() {
    int cells = 0;
    for (int x = -41; x <= 40; x++)
      for (int z = -41; z <= 40; z++) if (SpawnPolicy.spawn(x, z)) cells++;
    assertEquals(6400, cells);
    assertTrue(SpawnPolicy.spawn(-40, -40));
    assertTrue(SpawnPolicy.spawn(39, 39));
    assertFalse(SpawnPolicy.spawn(40, 20));
  }

  @Test
  void roadProtectionContinuesIntoWildAndHandlesOverflow() {
    assertTrue(SpawnPolicy.road(3, 20000));
    assertTrue(SpawnPolicy.road(-20000, -3));
    assertFalse(SpawnPolicy.road(4, 4));
    assertFalse(SpawnPolicy.road(Integer.MIN_VALUE, Integer.MIN_VALUE));
  }

  @Test
  void claimChunkMustEntirelyAvoidReserveAndRoad() {
    assertFalse(SpawnPolicy.claimGeometry(31, 1));
    assertTrue(SpawnPolicy.claimGeometry(32, 1));
    assertFalse(SpawnPolicy.claimGeometry(100, 0));
    assertTrue(SpawnPolicy.claimGeometry(33, 1));
    assertTrue(SpawnPolicy.claimGeometry(-34, -2));
    assertFalse(SpawnPolicy.claimGeometry(Integer.MAX_VALUE, 1));
  }

  @Test
  void radialReserveIsSymmetricAndIncludesBoundary() {
    assertTrue(SpawnPolicy.reserve(512, 0));
    assertFalse(SpawnPolicy.reserve(513, 0));
    assertTrue(SpawnPolicy.reserve(-512, 0));
    assertTrue(SpawnPolicy.reserve(300, 400));
    assertFalse(SpawnPolicy.reserve(400, 400));
  }
}
