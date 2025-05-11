import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import datechooser.beans.DateChooserCombo;
import entidades.Temperaturas;
import servicios.ServicioTemperaturas;

public class FrmTemperaturas extends JFrame {

    private int ANCHO_ICONO = 32;
    private int ALTO_ICONO = 32;

    private DateChooserCombo dccDesde, dccHasta, dccEstadistica;
    private JTabbedPane tpTemperaturas;
    private JPanel pnlGrafica;
    private JPanel pnlEstadistica;

    private List<Temperaturas> datos;

    public FrmTemperaturas() {
        setTitle("Temperaturas");
        setIconImage(new ImageIcon(getClass().getResource("/iconos/temp.png")).getImage());
        setSize(800, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar toolBar = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/iconos/graph.png"))
                .getImage()
                .getScaledInstance(ANCHO_ICONO, ALTO_ICONO, Image.SCALE_SMOOTH)));
        btnGraficar.setToolTipText("Grafica Temperatura de Ciudad");
        toolBar.add(btnGraficar);
        btnGraficar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnGraficarClick();
            }
        });

        JButton btnCalcularEstadisticas = new JButton();
        btnCalcularEstadisticas.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/iconos/statistic.png"))
                .getImage()
                .getScaledInstance(ANCHO_ICONO, ALTO_ICONO, Image.SCALE_SMOOTH)));
        btnCalcularEstadisticas.setToolTipText("Estadisticas de la temperatura seleccionada");
        toolBar.add(btnCalcularEstadisticas);
        btnCalcularEstadisticas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCalcularEstadisticasClick();
            }
        });

        // Contenedor
        JPanel pnlTemperaturas = new JPanel();
        pnlTemperaturas.setLayout(new BoxLayout(pnlTemperaturas, BoxLayout.Y_AXIS));

        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setPreferredSize(new Dimension(pnlDatosProceso.getWidth(), 50));
        pnlDatosProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlDatosProceso.setLayout(null);

        JLabel lblCiudad = new JLabel("Rango de fecha");
        lblCiudad.setBounds(10, 10, 100, 25);
        pnlDatosProceso.add(lblCiudad);

        JLabel lblHasta = new JLabel("hasta");
        lblHasta.setBounds(235, 10, 100, 25);
        pnlDatosProceso.add(lblHasta);

        JLabel lblHastaEstds = new JLabel("Fecha para estadistica");
        lblHastaEstds.setBounds(460, 10, 160, 25);
        pnlDatosProceso.add(lblHastaEstds);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(120, 10, 100, 25);
        pnlDatosProceso.add(dccDesde);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(280, 10, 100, 25);
        pnlDatosProceso.add(dccHasta);

        dccEstadistica = new DateChooserCombo();
        dccEstadistica.setBounds(600, 10, 100, 25);
        pnlDatosProceso.add(dccEstadistica);

        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadistica = new JPanel();

        tpTemperaturas = new JTabbedPane();
        tpTemperaturas.addTab("Gráfica", spGrafica);
        tpTemperaturas.addTab("Estadísticas", pnlEstadistica);

        // Agregar componentes
        pnlTemperaturas.add(pnlDatosProceso);
        pnlTemperaturas.add(tpTemperaturas);

        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(pnlTemperaturas, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        datos = ServicioTemperaturas.cargarDatos(System.getProperty("user.dir") + "/src/datos/Temperaturas.csv");
    }

    private void btnGraficarClick() {
        LocalDate fechaDesde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fechaHasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        tpTemperaturas.setSelectedIndex(0);

        DefaultCategoryDataset dataset = ServicioTemperaturas.graficarPromediosPorCiudad(fechaDesde, fechaHasta, datos);

        JFreeChart grafica = ChartFactory.createBarChart(
            "Promedio de Temperaturas por Ciudad",
            "Ciudad",
            "Temperatura (°C)",
            dataset
        );

        ChartPanel panelGrafica = new ChartPanel(grafica);
        panelGrafica.setPreferredSize(new Dimension(680, 300));

        pnlGrafica.removeAll();
        pnlGrafica.add(panelGrafica);
        pnlGrafica.revalidate();
        pnlGrafica.repaint();
    }

    private void btnCalcularEstadisticasClick() {
        LocalDate fechaEstadistica = dccEstadistica.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        tpTemperaturas.setSelectedIndex(1);

        var maxTemp = ServicioTemperaturas.getMaxTempEnFecha(fechaEstadistica, datos);
        var minTemp = ServicioTemperaturas.getMinTempEnFecha(fechaEstadistica, datos);

        pnlEstadistica.removeAll();
        pnlEstadistica.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlEstadistica.add(new JLabel("Máxima Temperatura: " + (maxTemp.isPresent() ? maxTemp.get().getCiudad() + " - " + maxTemp.get().getTemperatura() : "Sin datos")), gbc);

        gbc.gridy = 1;
        pnlEstadistica.add(new JLabel("Mínima Temperatura: " + (minTemp.isPresent() ? minTemp.get().getCiudad() + " - " + minTemp.get().getTemperatura() : "Sin datos")), gbc);

        pnlEstadistica.revalidate();
        pnlEstadistica.repaint();
    }
}
