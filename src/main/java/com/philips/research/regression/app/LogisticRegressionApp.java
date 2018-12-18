package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static com.philips.research.regression.util.MatrixConversions.transpose;
import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;

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
    int myId;
    @Option(
        names = {"-p", "--party"},
        required = true,
        split=":",
        description = "Specification of a party. One of these needs to be present for each party. For example: '-p1:localhost:8871 -p2:localhost:8872'.")
    String[] parties;
    @Option(
        names={"--lambda"},
        defaultValue="1.0",
        description="Lambda for fitting the logistic model. If omitted, the default value is ${DEFAULT-VALUE}.")
    double lambda;
    @Option(
        names={"--iterations"},
        defaultValue="5",
        description="The number of iterations performed by the fitter. If omitted, the default value is ${DEFAULT-VALUE}.")
    int iterations;

    public static void main(String[] args) {
        CommandLine.call(new LogisticRegressionApp(), args);
    }

    @Override
    public Void call() {
        HashMap<Integer, Party> partyMap = createPartyMap();

        Matrix<BigDecimal> m = myId == 1 ? X1 : X2;
        Vector<BigDecimal> v = myId == 1 ? CarDataSet.am1 : CarDataSet.am2;

        LogisticRegression frescoApp = new LogisticRegression(myId, m, v, lambda, iterations);
        BigInteger modulus = ModulusFinder.findSuitableModulus(512);

        AsyncNetwork network = new AsyncNetwork(new NetworkConfigurationImpl(myId, partyMap));

        int noOfPlayers = partyMap.size();

        SpdzProtocolSuite protocolSuite = new SpdzProtocolSuite(200, 16);
        BatchEvaluationStrategy<SpdzResourcePool> strategy = EvaluationStrategy.SEQUENTIAL.getStrategy();
        ProtocolEvaluator<SpdzResourcePool> evaluator = new BatchedProtocolEvaluator<>(strategy, protocolSuite);
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
        network.close();
        sce.shutdownSCE();
        return null;
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

    private static BigDecimal[] ones;
    static {
        ones = new BigDecimal[CarDataSet.hp1.length];
        fill(ones, BigDecimal.ONE);
    }

    private static Matrix<BigDecimal> X1 = transpose(matrix(new BigDecimal[][]{
        CarDataSet.hp1,
        CarDataSet.wt1,
        ones
    }));

    private static Matrix<BigDecimal> X2 = transpose(matrix(new BigDecimal[][]{
        CarDataSet.hp2,
        CarDataSet.wt2,
        ones
    }));
}

class CarDataSet {

    static BigDecimal[] hp1 = stream(new Double[]{
        110.0, 110.0, 93.0, 110.0, 175.0, 105.0, 245.0, 62.0,
        95.0, 123.0, 123.0, 180.0, 180.0, 180.0, 205.0, 215.0
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    static BigDecimal[] hp2 = stream(new Double[]{
        230.0, 66.0, 52.0, 65.0, 97.0, 150.0, 150.0, 245.0,
        175.0, 66.0, 91.0, 113.0, 264.0, 175.0, 335.0, 109.0
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    static BigDecimal[] wt1 = stream(new Double[]{
        2.62, 2.875, 2.32, 3.215, 3.44, 3.46, 3.57, 3.19,
        3.15, 3.44, 3.44, 4.07, 3.73, 3.78, 5.25, 5.424
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    static BigDecimal[] wt2 = stream(new Double[]{
        5.345, 2.2, 1.615, 1.835, 2.465, 3.52, 3.435, 3.84,
        3.845, 1.935, 2.14, 1.513, 3.17, 2.77, 3.57, 2.78
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    static Vector<BigDecimal> am1 = stream(new Double[]{
        1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    }).map(BigDecimal::valueOf).collect(toCollection(Vector::new));

    static Vector<BigDecimal> am2 = stream(new Double[]{
        0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
    }).map(BigDecimal::valueOf).collect(toCollection(Vector::new));
}
