package juego;

import modelo.carta;
import modelo.mano;
import dificultades.dificultad;

import java.util.*;

/** Controlador de Truco: mano completa con IA. */
public class Truco {

    // ===== Tipos =====
    public enum Estado { ESPERANDO_REPARTIR, JUGANDO, ESPERANDO_RESP_TRUCO, FIN_MANO, FIN_PARTIDA }
    public enum Canto  { NINGUNO, TRUCO, RETRUCO, VALE4 }

    public static final class Snapshot {
        public Estado estado;
        public Canto canto;
        public boolean turnoHumano;
        public boolean esManoHumano;
        public int puntosHumano, puntosCpu;

        // cartas
        public List<carta> manoHumano = new ArrayList<>();
        public List<carta> mesa = new ArrayList<>();   // bazas cerradas (en orden)
        public carta bazaHumano, bazaCpu;              // cartas en la baza actual
        public int cartasHumanoRestantes, cartasCpuRestantes;

        // UI flags
        public boolean puedeCantarTruco;

        public String mensaje;
    }

    // ===== Config =====
    private static final int PUNTOS_PARTIDA = 30;

    // ===== Jugadores =====
    private final mano manoHumano = new mano();
    private final mano manoCpu    = new mano();
    private int puntosHumano = 0, puntosCpu = 0;

    // ===== IA =====
    private final dificultad ia;

    // ===== Estado =====
    private Estado estado = Estado.ESPERANDO_REPARTIR;
    private Canto canto = Canto.NINGUNO;
    private boolean esManoHumano = true; // alterna
    private boolean turnoHumano = true;  // quién actúa ahora

    private int bazasHumano = 0, bazasCpu = 0, numBaza = 0; // 0..2
    private carta cartaHumano = null, cartaCpu = null;       // baza en curso
    private final List<carta> mesa = new ArrayList<>();      // historial de bazas (pares humano/cpu)

    private final Deque<String> log = new ArrayDeque<>();
    private final Random rnd = new Random();

    public Truco(dificultad ia) { this.ia = ia; log("Listo. Clic en 'Nueva mano'."); }

    // ====== API pública (para la UI) ======
    public void repartir() {
        // reset mano
        manoHumano.verCartas().clear(); // limpiar copia: no sirve, limpiamos real:
        // truco: mano no tiene clear(), así que recreo objetos
        setMano(manoHumano, new ArrayList<>());
        setMano(manoCpu, new ArrayList<>());
        mesa.clear();
        cartaHumano = cartaCpu = null;
        bazasHumano = bazasCpu = 0;
        numBaza = 0;
        canto = Canto.NINGUNO;

        // generar mazo 40 y repartir
        List<carta> mazo = generarMazo40();
        Collections.shuffle(mazo, rnd);
        for (int i = 0; i < 3; i++) manoHumano.agregarCarta(mazo.remove(0));
        for (int i = 0; i < 3; i++) manoCpu.agregarCarta(mazo.remove(0));

        estado = Estado.JUGANDO;
        turnoHumano = esManoHumano;
        log("Nueva mano. " + (esManoHumano ? "Sos mano." : "CPU es mano."));
        if (!turnoHumano) cpuJuega(); // si CPU es mano, sale
    }

    public void jugarCartaHumano(carta c) {
        if (estado != Estado.JUGANDO || !turnoHumano || c == null) return;
        manoHumano.quitarCarta(c);
        cartaHumano = c;
        log("Vos: " + c.getCodigo());
        turnoHumano = false;
        cpuJuega();                 // responde
        cerrarBazaSiCorresponde();  // si hay 2 cartas, cerramos
    }

    public boolean puedeCantarTruco() { return getSnapshot().puedeCantarTruco; }

    public void cantarTruco() {
        if (!puedeCantarTruco()) return;
        canto = (canto==Canto.NINGUNO) ? Canto.TRUCO
             : (canto==Canto.TRUCO)   ? Canto.RETRUCO
             : Canto.VALE4;
        log("Cantás: " + canto);
        estado = Estado.ESPERANDO_RESP_TRUCO;
        boolean acepta = ia.aceptarTruco(manoCpu, cartasMesaParaIA());
        responderTrucoCPU(acepta);
    }

    public void responderTrucoHumano(boolean quiero) {
        if (estado != Estado.ESPERANDO_RESP_TRUCO) return;
        if (!quiero) sumarNoQuieroA(Jugador.CPU); else {
            log("Querés " + canto + ".");
            estado = Estado.JUGANDO;
            if (!turnoHumano) cpuJuega();
        }
    }

    public Snapshot getSnapshot() {
        Snapshot s = new Snapshot();
        s.estado = estado; s.canto = canto;
        s.turnoHumano = turnoHumano; s.esManoHumano = esManoHumano;
        s.puntosHumano = puntosHumano; s.puntosCpu = puntosCpu;

        s.manoHumano = manoHumano.verCartas();
        s.mesa = new ArrayList<>(mesa);
        s.bazaHumano = cartaHumano;
        s.bazaCpu = cartaCpu;
        s.cartasHumanoRestantes = manoHumano.size();
        s.cartasCpuRestantes = manoCpu.size();

        boolean manoEmpezada = !s.mesa.isEmpty() || s.bazaHumano != null || s.bazaCpu != null;
        s.puedeCantarTruco = (estado == Estado.JUGANDO) && (canto != Canto.VALE4) && manoEmpezada;

        s.mensaje = log.peekLast();
        return s;
    }

    // ====== Interna ======
    private enum Jugador { HUMANO, CPU }

    private void cpuJuega() {
        if (estado != Estado.JUGANDO) return;
        cartaCpu = ia.jugarCarta(manoCpu, cartasMesaParaIA());
        log("CPU: " + cartaCpu.getCodigo());
        turnoHumano = true;
    }

    private void cerrarBazaSiCorresponde() {
        if (cartaHumano == null || cartaCpu == null) return;

        int cmp = Integer.compare(cartaHumano.getJerarquia(), cartaCpu.getJerarquia());
        if (cmp > 0) { bazasHumano++; log("Baza "+(numBaza+1)+": la ganás vos."); turnoHumano = true; }
        else if (cmp < 0) { bazasCpu++; log("Baza "+(numBaza+1)+": la gana CPU."); turnoHumano = false; }
        else { // parda
            log("Baza "+(numBaza+1)+": parda."); turnoHumano = esManoHumano;
        }

        mesa.add(cartaHumano); mesa.add(cartaCpu);
        cartaHumano = cartaCpu = null;
        numBaza++;

        if (bazasHumano == 2 || bazasCpu == 2 || numBaza == 3) {
            finDeMano();
        } else if (!turnoHumano) {
            cpuJuega();
        }
    }

    private void finDeMano() {
        int pts = puntosPorCantoAceptado();
        if (bazasHumano > bazasCpu) {
            puntosHumano += pts; log("Mano tuya. +" + pts);
        } else if (bazasCpu > bazasHumano) {
            puntosCpu += pts; log("Mano de CPU. +" + pts);
        } else { // mano parda: gana el mano
            if (esManoHumano) { puntosHumano += pts; log("Mano parda: gana el mano (vos). +" + pts); }
            else { puntosCpu += pts; log("Mano parda: gana el mano (CPU). +" + pts); }
        }

        esManoHumano = !esManoHumano; // alterna
        estado = (puntosHumano >= PUNTOS_PARTIDA || puntosCpu >= PUNTOS_PARTIDA) ? Estado.FIN_PARTIDA : Estado.FIN_MANO;
    }

    private void responderTrucoCPU(boolean quiero){
        if (!quiero) sumarNoQuieroA(Jugador.HUMANO);
        else { log("CPU quiere " + canto + "."); estado = Estado.JUGANDO; if (!turnoHumano) cpuJuega(); }
    }

    private void sumarNoQuieroA(Jugador j) {
        int pts = puntosPorNoQuiero();
        if (j == Jugador.HUMANO) puntosHumano += pts; else puntosCpu += pts;
        log("No quiere. +" + pts + " para " + (j==Jugador.HUMANO ? "vos" : "CPU"));
        estado = (puntosHumano >= PUNTOS_PARTIDA || puntosCpu >= PUNTOS_PARTIDA) ? Estado.FIN_PARTIDA : Estado.FIN_MANO;
    }

    private int puntosPorNoQuiero() {
        switch (canto){
            case TRUCO: return 1;
            case RETRUCO: return 2;
            case VALE4: return 3;
            default: return 0;
        }
    }
    private int puntosPorCantoAceptado() {
        switch (canto){
            case NINGUNO: return 1;
            case TRUCO:   return 2;
            case RETRUCO: return 3;
            case VALE4:   return 4;
            default: return 1;
        }
    }

    private List<carta> cartasMesaParaIA() {
        List<carta> l = new ArrayList<>();
        if (cartaHumano != null) l.add(cartaHumano);
        if (cartaCpu != null)    l.add(cartaCpu);
        return l;
    }

    // recrea contenido de una mano (utilidad interna)
    @SuppressWarnings("unused")
    private void setMano(mano m, List<carta> nuevas) {
        // mano no expone setter; hacemos reset recreando (no afecta tu API)
        try {
            java.lang.reflect.Field f = mano.class.getDeclaredField("cartas");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<carta> ref = (List<carta>) f.get(m);
            ref.clear();
            ref.addAll(nuevas);
        } catch (Exception ignore) {}
    }

    // Generador de mazo 40 con jerarquía real de Truco
    private List<carta> generarMazo40() {
        List<carta> m = new ArrayList<>();
        String[] palos = {"E","B","O","C"};
        int[] valores = {1,2,3,4,5,6,7,10,11,12};
        for (String p : palos) {
            for (int v : valores) {
                m.add(new carta(v + p, jerarquia(v, p)));
            }
        }
        return m;
    }
    private int jerarquia(int v, String p) {
        if (v==1 && p.equals("E")) return 14; // Ancho de espadas
        if (v==1 && p.equals("B")) return 13; // Ancho de bastos
        if (v==7 && p.equals("E")) return 12; // 7 de espadas
        if (v==7 && p.equals("O")) return 11; // 7 de oros
        if (v==3) return 10;
        if (v==2) return 9;
        if (v==1) return 8;
        if (v==12) return 7;
        if (v==11) return 6;
        if (v==10) return 5;
        if (v==7)  return 4; // (C,B)
        if (v==6)  return 3;
        if (v==5)  return 2;
        return 1; // 4
    }

    private void log(String s){ log.add(s); if (log.size()>60) log.pollFirst(); }
}
