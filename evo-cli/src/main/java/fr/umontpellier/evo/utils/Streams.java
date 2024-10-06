package fr.umontpellier.evo.utils;

public class Streams {

    Streams() {}

    /**
     * Permet d'appeler une méthode qui renvoie une exception dans un stream
     */
    public static <R, E extends Throwable> R unwrap(UnwrapFunction<R, E> f) {
        try {
            return f.apply();
        } catch (Throwable exception) {
            System.err.println("Failed to unwrap, see following error.");
            exception.printStackTrace();
        }
        return null;
    }

    public interface UnwrapFunction<R, E extends Throwable> {
        R apply() throws E;
    }

}
