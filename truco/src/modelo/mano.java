package modelo;

import java.util.*;

public class mano {

    private List<carta> cartas;

    public mano() {
        cartas = new ArrayList<>();
    }

    public void agregarCarta(carta c) { cartas.add(c); }
    public void quitarCarta(carta c)  { cartas.remove(c); }

    public carta getCartaMasFuerte() {
        carta mejor = null;
        for (carta c : cartas)
            if (mejor == null || c.getJerarquia() > mejor.getJerarquia())
                mejor = c;
        return mejor;
    }

    public carta getCartaMasDebil() {
        carta peor = null;
        for (carta c : cartas)
            if (peor == null || c.getJerarquia() < peor.getJerarquia())
                peor = c;
        return peor;
    }

    public carta getCartaRandom() {
        Random r = new Random();
        return cartas.get(r.nextInt(cartas.size()));
    }

    public carta getCartaQueLeGanaA(carta c) {
        for (carta carta : cartas)
            if (carta.getJerarquia() > c.getJerarquia())
                return carta;
        return null;
    }

    public int calcularFuerza() {
        int suma = 0;
        for (carta c : cartas)
            suma += c.getJerarquia();
        return suma;
    }

    // 👉 Nuevo método corregido de Envido
    public int calcularEnvido() {
        Map<Character, List<Integer>> porPalo = new HashMap<>();
        for (carta c : cartas) {
            char p = c.getPaloChar();
            porPalo.computeIfAbsent(p, k -> new ArrayList<>()).add(c.getValorEnvido());
        }

        int mejor = 0;

        for (List<Integer> vals : porPalo.values()) {
            if (vals.size() >= 2) {
                vals.sort(Collections.reverseOrder());
                int suma = 20 + vals.get(0) + vals.get(1);
                mejor = Math.max(mejor, suma);
            }
        }

        if (mejor == 0) {
            for (List<Integer> vals : porPalo.values())
                for (int v : vals)
                    mejor = Math.max(mejor, v);
        }

        return mejor;
    }
    public carta getCartaQueMaximizaChance(List<carta> mesa) {
    if (mesa == null || mesa.isEmpty()) {
        // Si no hay cartas en mesa, tiro la más fuerte
        return getCartaMasFuerte();
    }

    // Tomo la última carta jugada (la del rival)
    carta ultimaMesa = mesa.get(mesa.size() - 1);
    carta ganadora = getCartaQueLeGanaA(ultimaMesa);

    // Si tengo una que gana, juego esa. Si no, la más débil.
    return (ganadora != null) ? ganadora : getCartaMasDebil();
}
public java.util.List<carta> verCartas() {
    return new java.util.ArrayList<>(cartas); // copia segura
}

public int size() {
    return cartas.size();
}
// === Agregar en mano.java (dentro de la clase mano) ===

// Devuelve una copia segura de la mano para mostrar en la UI

}


