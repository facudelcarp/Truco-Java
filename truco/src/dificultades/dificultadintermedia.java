package dificultades;

import modelo.carta;
import modelo.mano;
import java.util.List;
import java.util.Random;

public class dificultadintermedia extends dificultad {

    private Random random = new Random();

    public carta jugarCarta(mano mano, List<carta> cartasMesa) {
        if (cartasMesa.isEmpty()) {
            carta c = mano.getCartaMasFuerte();
            mano.quitarCarta(c);
            return c;
        }

        carta ultimaMesa = cartasMesa.get(cartasMesa.size() - 1);
        carta ganadora = mano.getCartaQueLeGanaA(ultimaMesa);
        if (ganadora != null) {
            mano.quitarCarta(ganadora);
            return ganadora;
        } else {
            carta c = mano.getCartaMasFuerte();
            mano.quitarCarta(c);
            return c;
        }
    }

    public boolean aceptarTruco(mano mano, List<carta> cartasMesa) {
        int fuerza = mano.calcularFuerza();
        if (fuerza >= 10) return true;
        return random.nextDouble() < 0.6;
    }

    public boolean aceptarEnvido(mano mano) {
        int envido = mano.calcularEnvido();
        if (envido >= 25) return true;
        return random.nextDouble() < 0.5;
    }
}
