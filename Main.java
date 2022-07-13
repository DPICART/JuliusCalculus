public class Main {

    public static void main(String[] args) {

        var game = new Game(10)
                .withNumbers( 5d, 8d, 4d, 3d)
                .withOperators("+", "-", "*", "/")
                .setReuseOperators(true)
                .setReuseNumbers(false)
                .setStopOnFirstFound(false) // A mettre pour les grands nombres ( trop de possibilités )
                .setMaxDepth(4);

        game.performComputation();


    }

}