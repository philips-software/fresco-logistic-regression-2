package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
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
    public Void call() throws IOException {
        HashMap<Integer, Party> partyMap = createPartyMap();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Matrix<BigDecimal> m = readMatrix(reader);
        Vector<BigDecimal> v = readVector(reader);

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

    private Matrix<BigDecimal> readMatrix(BufferedReader reader) throws IOException {
        String matrixString = reader.readLine();
        String preprocessed = matrixString.replace("]", "\n")
                                          .replace("[", " ")
                                          .replace(",", " ");
        BufferedReader matrixReader = new BufferedReader(new StringReader(preprocessed));
        ArrayList<ArrayList<BigDecimal>> rows = new ArrayList<>();
        String line = matrixReader.readLine();
        while (line != null && !line.isEmpty()) {
            ArrayList<BigDecimal> row = new ArrayList<>();
            Scanner sc = new Scanner(line);
            sc.useLocale(Locale.US);
            while (sc.hasNextDouble()) {
                double d = sc.nextDouble();
                row.add(BigDecimal.valueOf(d));
            }
            rows.add(row);
            line = matrixReader.readLine();
        }
        return new Matrix<>(rows.size(), rows.get(0).size(), rows::get);
    }

    private Vector<BigDecimal> readVector(BufferedReader reader) throws IOException {
        String vectorString = reader.readLine();
        String preprocessed = vectorString
            .replace("]", "\n")
            .replace("[", " ")
            .replace(",", " ");
        BufferedReader vectorReader = new BufferedReader(new StringReader(preprocessed));
        Vector<BigDecimal> vector = new Vector<>();
        Scanner sc = new Scanner(preprocessed);
        sc.useLocale(Locale.US);
        while (sc.hasNextDouble()) {
            vector.add(BigDecimal.valueOf(sc.nextDouble()));
        }
        return vector;
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
