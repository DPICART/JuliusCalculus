import java.util.*;

public class Game {

    private final double target;
    private final List<Double> numbers = new ArrayList<>();
    private final List<String> operators = new ArrayList<>();
    private boolean reuseOperators = false;
    private boolean reuseNumbers = false;
    private boolean stopOnFirstFound = false; // Possible qu'on est quand même plus d'un résultat ( parallélisme )
    private int maxDepth = 4; // Permet d'éviter les boucles infinies dans le cas où les opérateurs / nombres sont réutilisables

    private final Set<String> results = new HashSet<>();

    private long deltaMs = 0;

    public Game(int target) {
        this.target = target;
    }

    public Game withNumbers(Double... numbers) {
        this.numbers.addAll(Arrays.stream(numbers).toList());
        return this;
    }

    public Game withOperators(String... operators) {
        this.operators.addAll(Arrays.stream(operators).toList());
        return this;
    }

    public Game setReuseNumbers(boolean reuseNumbers) {
        this.reuseNumbers = reuseNumbers;
        return this;
    }

    public Game setReuseOperators(boolean reuseOperators) {
        this.reuseOperators = reuseOperators;
        return this;
    }

    public Game setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public Game setStopOnFirstFound(boolean stopOnFirstFound) {
        this.stopOnFirstFound = stopOnFirstFound;
        return this;
    }

    public void performComputation() {

        if (numbers.isEmpty() || operators.isEmpty()) {
            throw new IllegalArgumentException();
        }

        var start = System.currentTimeMillis();
        computeMovesRecursively(0,
                null,
                null,
                List.of(),
                numbers,
                List.of(),
                operators
        );
        var end = System.currentTimeMillis();
        this.deltaMs = end - start;
        prettyPrintResult();
    }

    private void computeMovesRecursively(
            int currentDepth,
            Double currentResult,
            String currentString,
            List<Double> usedNumbers,
            List<Double> numbers,
            List<String> usedOperators,
            List<String> operators
    ) {
        if (
                (stopOnFirstFound && results.size() > 0)
                        || currentDepth >= maxDepth
                        || numbers.size() == 0
                        || operators.size() == 0
                        || (null != currentResult && target == currentResult)
        ) {
            return;
        }


        numbers
                .stream()
                .parallel()
                .forEach(currentNumber -> {

                    var newUsedNumbers = new ArrayList<>(usedNumbers);
                    newUsedNumbers.add(currentNumber);

                    var newNumbers = new ArrayList<>(numbers);
                    if (!reuseNumbers) {
                        newNumbers.remove(currentNumber);
                    }

                    operators
                            .stream()
                            .parallel()
                            .forEach(currentOperation -> {

                                var newUsedOperators = new ArrayList<>(usedOperators);
                                newUsedOperators.add(currentOperation);

                                var newOperators = new ArrayList<>(operators);
                                if (!reuseOperators) {
                                    newOperators.remove(currentOperation);
                                }

                                if (currentResult == null) {
                                    if (target == currentNumber) {
                                        var resultString = currentNumber + " = " + target;
                                        results.add(resultString);
                                    }
                                    computeMovesRecursively(
                                            currentDepth + 1,
                                            currentNumber,
                                            currentNumber + "",
                                            newUsedNumbers,
                                            newNumbers,
                                            newUsedOperators,
                                            newOperators);
                                } else {
                                    var newResult = applyOperation(currentResult, currentNumber, currentOperation);
                                    var newCurrentString = "(" + currentString + currentOperation + currentNumber + ")";
                                    if (target == newResult) {
                                        var resultString = newCurrentString + " = " + target;
                                        results.add(resultString);
                                    }
                                    computeMovesRecursively(
                                            currentDepth + 1,
                                            newResult,
                                            newCurrentString,
                                            newUsedNumbers,
                                            newNumbers,
                                            newUsedOperators,
                                            newOperators);
                                }
                            });

                });
    }

    private static Double applyOperation(Double value, Double number, String currentOperation) {
        return switch (currentOperation) {
            case "+" -> value + number;
            case "-" -> value - number;
            case "*" -> value * number;
            case "/" -> value / number;
            case "^" -> Math.pow(value, number);
            case "%" -> value % number;
            default -> throw new UnsupportedOperationException();
        };
    }

    private void prettyPrintResult() {
        System.out.println("##############################################################################");
        System.out.println("Found " + results.size() + " result" + (results.size()>1?"s": ""));
        for (var result : results) {
            System.out.println(" -\t" + result);
        }
        System.out.println("Computation took " + deltaMs + "ms.");
        System.out.println("##############################################################################\n");
    }
}
