package co.com.bancolombia.ecs.model.helpers;

import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class NonBlockingIdGenerator {

    private static final long VERSION_CLEAR_MASK = 0xffffffffffff0fffL;
    private static final long VERSION_4_MASK = 0x0000000000004000L;
    private static final long VARIANT_CLEAR_MASK = 0x3fffffffffffffffL;
    private static final long VARIANT_IETF_MASK = 0x8000000000000000L;

    public static String randomUuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long mostSignificantBits = (random.nextLong() & VERSION_CLEAR_MASK) | VERSION_4_MASK;
        long leastSignificantBits = (random.nextLong() & VARIANT_CLEAR_MASK) | VARIANT_IETF_MASK;
        return new UUID(mostSignificantBits, leastSignificantBits).toString();
    }
}
