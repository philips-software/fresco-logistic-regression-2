package com.philips.research.regression.app;

import com.google.gson.Gson;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.logging.EvaluatorLoggingDecorator;
import dk.alexandra.fresco.logging.NetworkLoggingDecorator;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static com.philips.research.regression.util.MatrixConversions.map;
import static com.philips.research.regression.util.VectorUtils.vectorOf;

@CommandLine.Command(
    description = "Secure Multi-Party Logistic Regression",
    name="LogisticRegression",
    mixinStandardHelpOptions = true,
    version = "Logistic Regression 0.1.0")
public class LogisticRegressionApp implements Callable<Void> {
    @Option(
        names={"-i", "--myId"},
        required=true,
        description="Id of this party.")
    private int myId;
    @Option(
        names = {"-p", "--party"},
        required = true,
        split=":",
        description = "Specification of a party. One of these needs to be present for each party. For example: '-p1:localhost:8871 -p2:localhost:8872'.")
    private String[] parties;
    @Option(
        names={"--lambda"},
        defaultValue="1.0",
        description="Lambda for fitting the logistic model. If omitted, the default value is ${DEFAULT-VALUE}.")
    private double lambda;
    @Option(
        names={"--iterations"},
        defaultValue="5",
        description="The number of iterations performed by the fitter. If omitted, the default value is ${DEFAULT-VALUE}.")
    private int iterations;

    public static void main(String[] args) {
        CommandLine.call(new LogisticRegressionApp(), args);
    }

    @Override
    public Void call() throws IOException {
        HashMap<Integer, Party> partyMap = createPartyMap();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Input input = new Gson().fromJson(reader, Input.class);
        Matrix<BigDecimal> m = map(matrix(input.predictors), BigDecimal::valueOf);
        Vector<BigDecimal> v = vectorOf(input.outcomes);

        LogisticRegression frescoApp = new LogisticRegression(myId, m, v, lambda, iterations);
        BigInteger modulus = ModulusFinder.findSuitableModulus(512);

        Network network = new AsyncNetwork(new NetworkConfigurationImpl(myId, partyMap));
        network = new NetworkLoggingDecorator(network);

        int noOfPlayers = partyMap.size();

        SpdzProtocolSuite protocolSuite = new SpdzProtocolSuite(200, 16);
        BatchEvaluationStrategy<SpdzResourcePool> strategy = EvaluationStrategy.SEQUENTIAL.getStrategy();
        strategy = new BatchEvaluationLoggingDecorator<>(strategy);
        ProtocolEvaluator<SpdzResourcePool> evaluator = new BatchedProtocolEvaluator<>(strategy, protocolSuite);
        evaluator = new EvaluatorLoggingDecorator<>(evaluator);
        SecureComputationEngineImpl<SpdzResourcePool, ProtocolBuilderNumeric> sce = new SecureComputationEngineImpl<>(protocolSuite, evaluator);
        OpenedValueStore<SpdzSInt, BigInteger> store = new SpdzOpenedValueStoreImpl();
        SpdzDataSupplier supplier = new SpdzDummyDataSupplier(myId, noOfPlayers, modulus);
        SpdzResourcePoolImpl resourcePool = new SpdzResourcePoolImpl(myId, noOfPlayers, store, supplier, new AesCtrDrbg(new byte[32]));

//        DummyArithmeticProtocolSuite protocolSuite = new DummyArithmeticProtocolSuite(modulus,200,16);
//        BatchEvaluationStrategy<DummyArithmeticResourcePool> strategy = EvaluationStrategy.SEQUENTIAL.getStrategy();
//        ProtocolEvaluator<DummyArithmeticResourcePool> evaluator = new BatchedProtocolEvaluator<>(strategy, protocolSuite);
//        SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce =
//            new SecureComputationEngineImpl<>(
//                protocolSuite,
//                evaluator);
//        DummyArithmeticResourcePoolImpl resourcePool = new DummyArithmeticResourcePoolImpl(myId, partyMap.size(), modulus);

        List<BigDecimal> result = sce.runApplication(frescoApp, resourcePool, network);

        System.out.println(result);
        ((Closeable)network).close();
        sce.shutdownSCE();
        return null;
    }

    private class Input {
        Double[][] predictors;
        double[] outcomes;
    }

    private HashMap<Integer, Party> createPartyMap() {
        HashMap<Integer, Party> partyMap = new HashMap<>();
        for (int p = 0; p < parties.length/3; ++p) {
            int partyId = Integer.parseInt(parties[p*3]);
            String host = parties[p*3 + 1];
            int port = Integer.parseInt(parties[p*3 + 2]);
            partyMap.put(partyId, new Party(partyId, host, port));
        }
        return partyMap;
    }
}
