package com.philips.research.regression.app;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
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
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
        names = {"-i", "--myId"},
        required = true,
        description = "Id of this party.")
    private int myId;
    @Option(
        names = {"-p", "--party"},
        required = true,
        split=":",
        description = "Specification of a party. One of these needs to be present for each party. For example: '-p1:localhost:8871 -p2:localhost:8872'.")
    private String[] parties;
    @Option(
        names = {"--lambda"},
        defaultValue = "1.0",
        description = "Lambda for fitting the logistic model. If omitted, the default value is ${DEFAULT-VALUE}.")
    private double lambda;
    @Option(
        names = {"--iterations"},
        defaultValue = "5",
        description = "The number of iterations performed by the fitter. If omitted, the default value is ${DEFAULT-VALUE}.")
    private int iterations;
    @Option(
        names = {"--privacy-budget", "-b"},
        defaultValue = "0",
        description = "Enables using differential privacy for updating the weights given this budget. If omitted, differential privacy is not used."
    )
    private double privacyBudget;
    @Option(
        names = {"--sensitivity", "-s"},
        defaultValue = "0",
        description = "Determines the sensitivity parameter used for differential privacy. If omitted, it is 1 divided by the number of rows in the input data."
    )
    private double sensitivity;
    @Option(
        names = {"--unsafe-debug-log"},
        defaultValue = "false",
        description = "Enables debug logging. ⚠️ Warning: exposes secret values in order to log them! ⚠️"
    )
    private boolean unsafeDebugLogging;
    @Option(
        names = {"--dummy"},
        defaultValue = "false",
        description = "Evaluates using dummy arithmetic instead of real MPC with SPDZ"
    )
    private boolean dummyArithmetic;

    public static void main(String[] args) {
        CommandLine.call(new LogisticRegressionApp(), args);
    }

    @Override
    public Void call() throws IOException {
        setLogLevel();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Input input = new Gson().fromJson(reader, Input.class);
        Matrix<BigDecimal> m = map(matrix(input.predictors), BigDecimal::valueOf);
        Vector<BigDecimal> v = vectorOf(input.outcomes);

        LogisticRegression frescoApp = new LogisticRegression(myId, m, v, lambda, iterations, privacyBudget, sensitivity);
        ApplicationRunner<List<BigDecimal>> runner = createRunner(myId, createPartyMap());

        List<BigDecimal> result = runner.run(frescoApp);

        System.out.println(result);
        runner.close();
        return null;
    }

    private ApplicationRunner<List<BigDecimal>> createRunner(int myId, HashMap<Integer, Party> partyMap) {
        if (dummyArithmetic) {
            return new DummyRunner<>(myId, partyMap);
        } else {
            return new SpdzRunner<>(myId, partyMap);
        }
    }

    private void setLogLevel() {
        if (unsafeDebugLogging) {
            Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            logger.setLevel(Level.DEBUG);
        }
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

abstract class ApplicationRunner <Output> {
    Network network;
    BigInteger modulus;

    ApplicationRunner(int myId, Map<Integer, Party> partyMap) {
        network = new AsyncNetwork(new NetworkConfigurationImpl(myId, partyMap));
        network = new NetworkLoggingDecorator(network);
        modulus = ModulusFinder.findSuitableModulus(512);
    }

    abstract Output run(Application<Output, ProtocolBuilderNumeric> application);

    void close() throws IOException {
        ((Closeable)network).close();
    }
}

class SpdzRunner <Output> extends ApplicationRunner<Output> {

    private SecureComputationEngineImpl<SpdzResourcePool, ProtocolBuilderNumeric> sce;
    private SpdzResourcePoolImpl resourcePool;

    SpdzRunner(int myId, Map<Integer, Party> partyMap) {
        super(myId, partyMap);

        SpdzProtocolSuite protocolSuite = new SpdzProtocolSuite(200, 16);
        BatchEvaluationStrategy<SpdzResourcePool> strategy = EvaluationStrategy.SEQUENTIAL.getStrategy();
        strategy = new BatchEvaluationLoggingDecorator<>(strategy);
        ProtocolEvaluator<SpdzResourcePool> evaluator = new BatchedProtocolEvaluator<>(strategy, protocolSuite);
        evaluator = new EvaluatorLoggingDecorator<>(evaluator);
        sce = new SecureComputationEngineImpl<>(protocolSuite, evaluator);

        OpenedValueStore<SpdzSInt, BigInteger> store = new SpdzOpenedValueStoreImpl();
        SpdzDataSupplier supplier = new SpdzDummyDataSupplier(myId, partyMap.size(), modulus);
        resourcePool = new SpdzResourcePoolImpl(myId, partyMap.size(), store, supplier, new AesCtrDrbg());
    }

    @Override
    public Output run(Application<Output, ProtocolBuilderNumeric> application) {
        return sce.runApplication(application, resourcePool, network);
    }

    @Override
    public void close() throws IOException {
        super.close();
        sce.shutdownSCE();
    }
}

class DummyRunner <Output> extends ApplicationRunner<Output> {

    private DummyArithmeticResourcePoolImpl resourcePool;
    private SecureComputationEngine<DummyArithmeticResourcePool, ProtocolBuilderNumeric> sce;

    DummyRunner(int myId, Map<Integer, Party> partyMap) {
        super(myId, partyMap);

        DummyArithmeticProtocolSuite protocolSuite = new DummyArithmeticProtocolSuite(modulus,200,16);
        BatchEvaluationStrategy<DummyArithmeticResourcePool> strategy = EvaluationStrategy.SEQUENTIAL.getStrategy();
        ProtocolEvaluator<DummyArithmeticResourcePool> evaluator = new BatchedProtocolEvaluator<>(strategy, protocolSuite);
        sce = new SecureComputationEngineImpl<>(protocolSuite, evaluator);

        resourcePool = new DummyArithmeticResourcePoolImpl(myId, partyMap.size(), modulus);
    }

    @Override
    public Output run(Application<Output, ProtocolBuilderNumeric> application) {
        return sce.runApplication(application, resourcePool, network);
    }

    @Override
    public void close() throws IOException {
        super.close();
        sce.shutdownSCE();
    }
}
