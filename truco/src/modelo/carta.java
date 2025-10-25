package modelo;

public class carta {

    private String codigo;   // ej: "1E", "7O", "10C"
    private int jerarquia;   // fuerza para Truco (ya la venís usando)

    public carta(String codigo, int jerarquia) {
        this.codigo = codigo;
        this.jerarquia = jerarquia;
    }

    public String getCodigo() { return codigo; }
    public int getJerarquia() { return jerarquia; }

    // --- NUEVO: helpers basados en (vc)(pa) ---

    // 'E' espadas, 'B' bastos, 'O' oros, 'C' copas
    public char getPaloChar() {
        return Character.toUpperCase(codigo.charAt(codigo.length() - 1));
    }

    public String getPalo() {
        switch (getPaloChar()) {
            case 'E': return "espadas";
            case 'B': return "bastos";
            case 'O': return "oros";
            case 'C': return "copas";
            default:  return "desconocido";
        }
    }

    // 1..7,10,11,12
    public int getValorNumero() {
        String num = codigo.substring(0, codigo.length() - 1);
        return Integer.parseInt(num);
    }

    // Envido: 10/11/12 valen 0; 1..7 su número
    public int getValorEnvido() {
        int v = getValorNumero();
        return (v >= 10) ? 0 : v;
    }

    // Útil para UI: mostrar "S", "C", "R" (Sota, Caballo, Rey)
    public String getDisplayValue() {
        int v = getValorNumero();
        if (v == 10) return "S";
        if (v == 11) return "C";
        if (v == 12) return "R";
        return String.valueOf(v);
    }
}
