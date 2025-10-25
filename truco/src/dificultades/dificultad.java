package dificultades;

import modelo.carta;
import modelo.mano;
import java.util.List;

public abstract class dificultad {
    public abstract carta jugarCarta(mano mano, List<carta> cartasMesa);
    public abstract boolean aceptarTruco(mano mano, List<carta> cartasMesa);
    public abstract boolean aceptarEnvido(mano mano);
}
