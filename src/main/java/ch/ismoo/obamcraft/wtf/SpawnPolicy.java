package ch.ismoo.obamcraft.wtf;

/** Fixed beta geometry; these predicates do not grant claim or Trust authority. */
final class SpawnPolicy {
  static final int SPAWN_MIN = -40, SPAWN_MAX = 39, RESERVE_RADIUS = 512, ROAD_HALF_WIDTH = 3;

  private SpawnPolicy() {}

  static boolean spawn(int x, int z) {
    return x >= SPAWN_MIN && x <= SPAWN_MAX && z >= SPAWN_MIN && z <= SPAWN_MAX;
  }

  static boolean road(int x, int z) {
    return Math.abs((long) x) <= ROAD_HALF_WIDTH || Math.abs((long) z) <= ROAD_HALF_WIDTH;
  }

  static boolean protectedBlock(int x, int z) {
    return spawn(x, z) || road(x, z);
  }

  static boolean reserve(int x, int z) {
    return Math.hypot((double) x, (double) z) <= RESERVE_RADIUS;
  }

  static boolean claimGeometry(int chunkX, int chunkZ) {
    long minX = (long) chunkX * 16, minZ = (long) chunkZ * 16, maxX = minX + 15, maxZ = minZ + 15;
    if (minX < Integer.MIN_VALUE
        || maxX > Integer.MAX_VALUE
        || minZ < Integer.MIN_VALUE
        || maxZ > Integer.MAX_VALUE) return false;
    // A full chunk must avoid protected roads and every part of the circular reserve.
    if (minX <= ROAD_HALF_WIDTH && maxX >= -ROAD_HALF_WIDTH
        || minZ <= ROAD_HALF_WIDTH && maxZ >= -ROAD_HALF_WIDTH) return false;
    long closestX = minX > 0 ? minX : maxX < 0 ? maxX : 0;
    long closestZ = minZ > 0 ? minZ : maxZ < 0 ? maxZ : 0;
    return Math.hypot((double) closestX, (double) closestZ) > RESERVE_RADIUS;
  }
}
