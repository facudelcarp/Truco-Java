package dificultades;

import modelo.carta;
import modelo.mano;
import java.util.List;
import java.util.Random;
public class dificultaddificil extends dificultad {

    private Random random = new Random();

    public carta jugarCarta(mano mano, List<carta> cartasMesa) {
        carta cartaAMostrar;

        if (cartasMesa.isEmpty()) {
            cartaAMostrar = mano.getCartaMasFuerte();
        } else {
            carta ultimaMesa = cartasMesa.get(cartasMesa.size() - 1);
            carta ganadora = mano.getCartaQueLeGanaA(ultimaMesa);
            cartaAMostrar = (ganadora != null) ? ganadora : mano.getCartaMasDebil();
        }

        if (random.nextDouble() < 0.2) {
            cartaAMostrar = mano.getCartaRandom();
        }

        mano.quitarCarta(cartaAMostrar);
        return cartaAMostrar;
    }

    public boolean aceptarTruco(mano mano, List<carta> cartasMesa) {
        int fuerza = mano.calcularFuerza();
        if (fuerza >= 12) return true;
        if (fuerza >= 8 && random.nextDouble() < 0.7) return true;
        return false;
    }

    public boolean aceptarEnvido(mano mano) {
        int envido = mano.calcularEnvido();
        if (envido >= 27) return true;
        if (envido >= 20 && random.nextDouble() < 0.5) return true;
        return false;
    }
}

