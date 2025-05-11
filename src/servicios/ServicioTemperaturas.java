package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jfree.data.category.DefaultCategoryDataset;

import entidades.Temperaturas;

public class ServicioTemperaturas {

    public static List<Temperaturas> cargarDatos(String archivoCSV) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");

        try (Stream<String> lineas = Files.lines(Paths.get(archivoCSV))) {
            return lineas.skip(1)
                .map(linea -> linea.split(","))
                .map(campos -> new Temperaturas(
                        campos[0],
                        LocalDate.parse(campos[1], formatoFecha),
                        Double.parseDouble(campos[2])))
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<String> getCiudades(List<Temperaturas> datos) {
        return datos.stream()
        .map(Temperaturas::getCiudad)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
    }

    public static List<Temperaturas> filtrar(LocalDate desde, LocalDate hasta, List<Temperaturas> datos) {
        return datos.stream()
        .filter(dato -> !dato.getFecha().isBefore(desde) && !dato.getFecha().isAfter(hasta))
        .collect(Collectors.toList());
    }

    public static double getPromedio(List<Double> temperaturas) {
        return temperaturas.isEmpty() ? 0 : temperaturas.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
    }

    public static double getMaximo(List<Double> temperaturas) {
        return temperaturas.isEmpty() ? 0 : temperaturas.stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0);
    }

    public static double getMinimo(List<Double> temperaturas) {
        return temperaturas.isEmpty() ? 0 : temperaturas.stream()
            .mapToDouble(Double::doubleValue)
            .min()
            .orElse(0);
    }

    public static Map<String, Double> getEstadisticas(LocalDate desde, LocalDate hasta, List<Temperaturas> datos) {
        var datosFiltrados = filtrar(desde, hasta, datos);
        var temperaturas = datosFiltrados.stream().map(Temperaturas::getTemperatura).collect(Collectors.toList());

        Map<String, Double> estadisticas = new LinkedHashMap<>();
        estadisticas.put("Promedio: ", getPromedio(temperaturas));
        estadisticas.put("Máximo: ", getMaximo(temperaturas));
        estadisticas.put("Mínimo: ", getMinimo(temperaturas));

        return estadisticas;
    }

    public static Optional<Temperaturas> getMaxTempEnFecha(LocalDate fecha, List<Temperaturas> datos) {
        return datos.stream()
                .filter(dato -> dato.getFecha().equals(fecha))
                .max(Comparator.comparing(Temperaturas::getTemperatura));
    }

    public static Optional<Temperaturas> getMinTempEnFecha(LocalDate fecha, List<Temperaturas> datos) {
        return datos.stream()
                .filter(dato -> dato.getFecha().equals(fecha))
                .min(Comparator.comparing(Temperaturas::getTemperatura));
    }

    public static DefaultCategoryDataset graficarPromediosPorCiudad(LocalDate desde, LocalDate hasta, List<Temperaturas> datos) {
        var datosFiltrados = filtrar(desde, hasta, datos);

        Map<String, Double> promediosPorCiudad = datosFiltrados.stream()
                .collect(Collectors.groupingBy(
                        Temperaturas::getCiudad,
                        Collectors.averagingDouble(Temperaturas::getTemperatura)
                ));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        promediosPorCiudad.forEach((ciudad, promedio) -> {
            dataset.addValue(promedio, "Promedio", ciudad);
        });

        return dataset;
    }
}
