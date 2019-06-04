package com.philips.research.regression.app;

import com.philips.research.regression.logging.TimestampedMarker;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultPreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.CloseableNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzMascotDataSupplier;
import dk.alexandra.fresco.tools.ot.otextension.RotList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.philips.research.regression.util.ListConversions.unwrap;

class PreprocessedValuesSupplier {
    private final CloseableNetwork pipeNetwork;
    private final SpdzMascotDataSupplier tripleSupplier;
    private final int maxBitLength;
    private final SpdzProtocolSuite protocolSuite;
    private final int myId;
    private final int numberOfPlayers;
    private final Drbg drbg;

    PreprocessedValuesSupplier(int myId, int numberOfPlayers, NetworkFactory networkFactory, SpdzProtocolSuite protocolSuite, int modBitLength, FieldDefinition definition, Map<Integer, RotList> seedOts, FieldElement ssk, int maxBitLength) {
        this.pipeNetwork = networkFactory.createExtraNetwork(myId);
        this.tripleSupplier = SpdzMascotDataSupplier.createSimpleSupplier(
            myId,
            numberOfPlayers,
            () -> pipeNetwork,
            modBitLength,
            definition,
            null,
            seedOts, Random.getDrbg(myId), ssk);
        this.maxBitLength = maxBitLength;
        this.protocolSuite = protocolSuite;
        this.myId = myId;
        this.numberOfPlayers = numberOfPlayers;
        this.drbg = Random.getDrbg(myId);
    }

    SpdzSInt[] provide(Integer pipeLength) {
        SpdzResourcePoolImpl resourcePool = createResourcePool();
        BuilderFactoryNumeric builderFactory = protocolSuite.init(resourcePool);
        ProtocolBuilderNumeric sequential = builderFactory.createSequential();
        DefaultPreprocessedValues preprocessedValues = new DefaultPreprocessedValues(sequential);
        TimestampedMarker.log(sequential, "... providing exponentiation series ...");
        DRes<List<DRes<SInt>>> exponentiationSeries =
            preprocessedValues.getExponentiationPipe(pipeLength);
        evaluate(sequential, resourcePool, pipeNetwork);
        TimestampedMarker.log(sequential, "... done");
        List<SInt> result = unwrap(exponentiationSeries);
        return result.stream().map(i -> (SpdzSInt) i).toArray(SpdzSInt[]::new);
    }

    private SpdzResourcePoolImpl createResourcePool() {
        return new SpdzResourcePoolImpl(
            myId,
            numberOfPlayers,
            new OpenedValueStoreImpl<>(),
            tripleSupplier,
            drbg);
    }

    private void evaluate(ProtocolBuilderNumeric spdzBuilder, SpdzResourcePool tripleResourcePool,
                          Network network) {
        BatchedStrategy<SpdzResourcePool> batchedStrategy = new BatchedStrategy<>();
        SpdzProtocolSuite spdzProtocolSuite = new SpdzProtocolSuite(maxBitLength);
        BatchedProtocolEvaluator<SpdzResourcePool> batchedProtocolEvaluator =
            new BatchedProtocolEvaluator<>(batchedStrategy, spdzProtocolSuite);
        batchedProtocolEvaluator.eval(spdzBuilder.build(), tripleResourcePool, network);
    }
}
