package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;

public class Random {
    static Drbg getDrbg(int myId) {
        // This method was copied from Fresco AbstractSpdzTest
        byte[] seed = new byte[SpdzRunner.PRG_SEED_LENGTH / 8];

        /* Remark from the FRESCO authors:
         *
         * > The joint DRBG needs to be seeded with the same seed for all parties. Seeding it with a 0 byte array there
         * > instead of one containing the party's ID does the trick.
         * >
         * > The above is not secure, but will give you accurate performance results. I've been working on a somewhat
         * > related issue in a separate branch that will remove the necessity to seed the DRBG manually altogether.
         * > But at least for the time being, simply removing that line should give you what you need.
         */
        // new Random(myId).nextBytes(seed);

        return AesCtrDrbgFactory.fromDerivedSeed(seed);
    }
}
