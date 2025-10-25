package dificultades;

import modelo.carta;
import modelo.mano;
import java.util.List;
import java.util.Random;

public class dificultadexperta extends dificultad {

    private Random random = new Random();

    public carta jugarCarta(mano mano, List<carta> cartasMesa) {
        carta cartaAMostrar = mano.getCartaQueMaximizaChance(cartasMesa);
        if (cartaAMostrar == null) {
            cartaAMostrar = mano.getCartaMasFuerte();
        }

        if (random.nextDouble() < 0.1) {
            cartaAMostrar = mano.getCartaRandom();
        }

        mano.quitarCarta(cartaAMostrar);
        return cartaAMostrar;
    }

    public boolean aceptarTruco(mano mano, List<carta> cartasMesa) {
        int fuerza = mano.calcularFuerza();
        return fuerza >= 9 || random.nextDouble() < 0.6;
    }

    public boolean aceptarEnvido(mano mano) {
        int envido = mano.calcularEnvido();
        return envido >= 25 || random.nextDouble() < 0.5;
    }
}


