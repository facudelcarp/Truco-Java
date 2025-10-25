package dificultades;

import modelo.carta;
import modelo.mano;
import java.util.List;
import java.util.Random;

public class dificultadinicial extends dificultad {

    private Random random = new Random();

    public carta jugarCarta(mano mano, List<carta> cartasMesa) {
        carta c = mano.getCartaRandom();
        mano.quitarCarta(c);
        return c;
    }

    public boolean aceptarTruco(mano mano, List<carta> cartasMesa) {
        return random.nextDouble() < 0.9;
    }

    public boolean aceptarEnvido(mano mano) {
        return random.nextDouble() < 0.9;
    }
}

