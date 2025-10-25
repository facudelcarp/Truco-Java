package vista;

import juego.Truco;
import dificultades.*;
import javax.swing.*;
import java.awt.*;

public class inicio extends JFrame {


    private JPanel panelTop, panelCenter, panelBottom, panelMesa, panelHumano, panelCpu;
    private JButton btnTruco, btnNuevaMano;
    private JComboBox<String> cmbDificultad;
    private JLabel lblPuntajes, lblMano;
    private JTextArea taMensajes;

    private Truco game;
    private dificultad iaActual;

    public inicio() {
        initComponents();                 // armado visual por código
        setLocationRelativeTo(null);
        setTitle("Truco Argentino");

        // IA y motor
        iaActual = new dificultadintermedia();
        game = new Truco(iaActual);

        // Eventos
        btnNuevaMano.addActionListener(e -> {
    game.repartir();
    taMensajes.setText("Nueva mano repartida.");
    refreshUI();
});


        btnTruco.addActionListener(e -> {
            game.cantarTruco();
            taMensajes.append("\n¡Cantaste Truco!");
        });

        cmbDificultad.addActionListener(e -> {
            String sel = (String) cmbDificultad.getSelectedItem();
            iaActual =
                "Fácil".equals(sel)   ? new dificultadinicial()   :
                "Media".equals(sel)   ? new dificultadintermedia() :
                "Difícil".equals(sel) ? new dificultaddificil()    :
                                         new dificultadexperta();
            game = new Truco(iaActual);
        });
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 750);
        getContentPane().setBackground(new Color(21, 50, 31));
        setLayout(new BorderLayout(10, 10));

        // TOP
        panelTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelTop.setOpaque(false);
        cmbDificultad = new JComboBox<>(new String[]{"Fácil", "Media", "Difícil", "Experta"});
        lblPuntajes = new JLabel("Humano 0 - 0 CPU");
        lblPuntajes.setFont(new Font("Arial", Font.BOLD, 18));
        lblPuntajes.setForeground(Color.WHITE);
        lblMano = new JLabel(" ");
        lblMano.setForeground(Color.LIGHT_GRAY);
        panelTop.add(new JLabel("Dificultad:"));
        panelTop.add(cmbDificultad);
        panelTop.add(lblPuntajes);
        panelTop.add(lblMano);
        add(panelTop, BorderLayout.NORTH);

        // CENTER
        panelCenter = new JPanel(new GridLayout(3, 1, 0, 10));
        panelCenter.setOpaque(false);
        panelCpu = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panelCpu.setOpaque(false);
        panelMesa = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panelMesa.setBackground(new Color(46, 31, 21));
        panelHumano = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        panelHumano.setOpaque(false);
        panelCenter.add(panelCpu);
        panelCenter.add(panelMesa);
        panelCenter.add(panelHumano);
        add(panelCenter, BorderLayout.CENTER);

        // BOTTOM
        panelBottom = new JPanel(new BorderLayout(10, 10));
        panelBottom.setOpaque(false);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        botones.setOpaque(false);
        btnNuevaMano = new JButton("Nueva mano");
        btnTruco = new JButton("Truco");
        botones.add(btnNuevaMano);
        botones.add(btnTruco);
        panelBottom.add(botones, BorderLayout.NORTH);
        taMensajes = new JTextArea(5, 70);
        taMensajes.setLineWrap(true);
        taMensajes.setWrapStyleWord(true);
        taMensajes.setEditable(false);
        panelBottom.add(new JScrollPane(taMensajes), BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void refreshUI() {
    juego.Truco.Snapshot s = game.getSnapshot();

    // Puntajes y mano
    lblPuntajes.setText("Humano " + s.puntosHumano + " - " + s.puntosCpu + " CPU");
    lblMano.setText(s.esManoHumano ? "Sos mano" : "CPU es mano");

    // Habilitar Truco según regla del controlador
    btnTruco.setEnabled(s.puedeCantarTruco);

    // CPU: mostrar dorso por cartas restantes
    panelCpu.removeAll();
    for (int i = 0; i < s.cartasCpuRestantes; i++) {
        JPanel back = cardBack();
        panelCpu.add(back);
    }
    panelCpu.revalidate();
    panelCpu.repaint();

    // Mesa: baza actual + bazas cerradas
    panelMesa.removeAll();
    if (s.bazaHumano != null) panelMesa.add(cardFront(s.bazaHumano));
    if (s.bazaCpu != null)    panelMesa.add(cardFront(s.bazaCpu));
    for (modelo.carta c : s.mesa) panelMesa.add(cardFront(c));
    panelMesa.revalidate();
    panelMesa.repaint();

    // Mano humano (click = jugar)
    panelHumano.removeAll();
    for (int i = 0; i < s.manoHumano.size(); i++) {
        final modelo.carta c = s.manoHumano.get(i);
        JPanel card = cardFront(c);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                game.jugarCartaHumano(c);
                refreshUI();
            }
        });
        panelHumano.add(card);
    }
    panelHumano.revalidate();
    panelHumano.repaint();

    // Mensajes
    String msg = (s.mensaje == null ? "" : s.mensaje);
    if (s.estado == juego.Truco.Estado.FIN_MANO) msg += "\nFin de la mano. Repartí para seguir.";
    if (s.estado == juego.Truco.Estado.FIN_PARTIDA) msg += "\n¡Partida terminada!";
    taMensajes.setText(msg);
}

// Helpers visuales (dentro de inicio.java)
private JPanel cardBack() {
    JPanel p = new JPanel(){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(139,69,19));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0,0,getWidth(),getHeight(),18,18);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial Black", Font.BOLD, 18));
            g2.drawString("Truco", getWidth()/2-30, getHeight()/2);
        }
    };
    p.setOpaque(false);
    p.setPreferredSize(new Dimension(140,190));
    return p;
}
private JPanel cardFront(final modelo.carta card) {
    JPanel p = new JPanel(){
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0,0,getWidth(),getHeight(),18,18);
            String codigo = card.getCodigo();
            String val = codigo.substring(0, codigo.length()-1);
            if (val.equals("10")) val = "S";
            if (val.equals("11")) val = "C";
            if (val.equals("12")) val = "R";
            char palo = codigo.charAt(codigo.length()-1);
            Color paloColor = (palo=='E' || palo=='B') ? Color.BLACK : Color.RED;
            g2.setFont(new Font("Arial Black", Font.BOLD, 18));
            g2.setColor(paloColor);
            g2.drawString(val, 10, 24);
            drawSuit(g2, palo, 18, 40, 12, paloColor);
            g2.drawString(val, getWidth()-28, getHeight()-10);
            drawSuit(g2, palo, getWidth()-18, getHeight()-28, 12, paloColor);
            drawSuit(g2, palo, getWidth()/2, getHeight()/2, 34, paloColor);
        }
        private void drawSuit(Graphics2D g, char palo, int x, int y, int size, Color color) {
            g.setColor(color);
            switch (Character.toUpperCase(palo)) {
                case 'E': g.drawLine(x,y,x,y+size); g.drawLine(x-size/4,y+size/4,x+size/4,y+size/4);
                          g.drawLine(x,y,x-size/4,y+size/4); g.drawLine(x,y,x+size/4,y+size/4); break;
                case 'C': g.fillOval(x - size/2, y, size, size/2);
                          int[] xp={x-size/2,x,x+size/2}; int[] yp={y+size/2,y+size,y+size/2};
                          g.fillPolygon(xp,yp,3); break;
                case 'O': g.fillOval(x - size/2, y - size/2, size, size);
                          g.setColor(Color.BLACK); g.fillOval(x-2,y-2,4,4); g.fillOval(x-6,y-6,4,4); g.fillOval(x+2,y+2,4,4);
                          break;
                case 'B': g.drawArc(x - size/2, y, size/2, size, 0, 180);
                          g.drawArc(x, y, size/2, size, 0, 180);
                          g.drawArc(x - size/4, y + size/4, size/2, size/2, 0, 180); break;
            }
        }
    };
    p.setOpaque(false);
    p.setPreferredSize(new Dimension(140,190));
    return p;
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new inicio().setVisible(true));
    }
    
    
}
