package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.EntregaResponse;
import com.rutadelivery.rutadelivery_backend.dto.OptimizarRutaRequest;
import com.rutadelivery.rutadelivery_backend.dto.ParadaRutaResponse;
import com.rutadelivery.rutadelivery_backend.dto.RutaOptimizadaResponse;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RutaService {

    private final EntregaRepository entregaRepository;
    private final RepartidorRepository repartidorRepository;
    private final SeguridadUsuarioService seguridadUsuarioService;

    public RutaService(
            EntregaRepository entregaRepository,
            RepartidorRepository repartidorRepository,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.entregaRepository = entregaRepository;
        this.repartidorRepository = repartidorRepository;
        this.seguridadUsuarioService = seguridadUsuarioService;
    }

    @Transactional
    public RutaOptimizadaResponse optimizarRuta(
            Long repartidorId,
            OptimizarRutaRequest request
    ) {
        /*
         * El repartidor de la URL debe coincidir
         * con el repartidorId firmado dentro del JWT.
         */
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarRepartidor(repartidorId);

        List<Entrega> todasLasEntregas =
                entregaRepository
                        .findByRepartidorIdOrderByOrdenRutaAsc(
                                repartidorId
                        );

        List<Entrega> pendientes =
                todasLasEntregas
                        .stream()
                        .filter(this::esEntregaPendiente)
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        ArrayList::new
                                )
                        );

        if (pendientes.isEmpty()) {
            return new RutaOptimizadaResponse(
                    "No existen entregas pendientes para optimizar",
                    repartidorId,
                    request.latitudeActual(),
                    request.longitudeActual(),
                    0,
                    0.0,
                    List.of()
            );
        }

        /*
         * Primera etapa:
         * construir una ruta rápida mediante
         * el algoritmo de vecino más cercano.
         */
        List<Entrega> rutaInicial =
                construirRutaVecinoMasCercano(
                        pendientes,
                        request.latitudeActual(),
                        request.longitudeActual()
                );

        /*
         * Segunda etapa:
         * aplicar 2-opt para buscar un recorrido
         * más corto que la ruta inicial.
         */
        List<Entrega> rutaMejorada =
                mejorarRutaConDosOpt(
                        rutaInicial,
                        request.latitudeActual(),
                        request.longitudeActual()
                );

        List<ParadaRutaResponse> paradas =
                guardarRutaYCrearParadas(
                        rutaMejorada,
                        request.latitudeActual(),
                        request.longitudeActual()
                );

        int siguienteOrden =
                rutaMejorada.size() + 1;

        colocarEntregasFinalizadasAlFinal(
                todasLasEntregas,
                siguienteOrden
        );

        double distanciaTotal =
                calcularDistanciaTotal(
                        rutaMejorada,
                        request.latitudeActual(),
                        request.longitudeActual()
                );

        return new RutaOptimizadaResponse(
                "Ruta optimizada con vecino más cercano y mejora 2-opt",
                repartidorId,
                request.latitudeActual(),
                request.longitudeActual(),
                rutaMejorada.size(),
                redondear(distanciaTotal),
                paradas
        );
    }

    private List<Entrega> construirRutaVecinoMasCercano(
            List<Entrega> entregasPendientes,
            double latitudeInicial,
            double longitudeInicial
    ) {
        List<Entrega> pendientes =
                new ArrayList<>(entregasPendientes);

        List<Entrega> ruta =
                new ArrayList<>();

        double latitudeActual =
                latitudeInicial;

        double longitudeActual =
                longitudeInicial;

        while (!pendientes.isEmpty()) {

            Entrega entregaMasCercana = null;

            double distanciaMasCercana =
                    Double.MAX_VALUE;

            for (Entrega entrega : pendientes) {

                double distancia =
                        calcularDistanciaKm(
                                latitudeActual,
                                longitudeActual,
                                entrega.getLatitude(),
                                entrega.getLongitude()
                        );

                if (distancia < distanciaMasCercana) {
                    distanciaMasCercana = distancia;
                    entregaMasCercana = entrega;
                }
            }

            if (entregaMasCercana == null) {
                break;
            }

            ruta.add(entregaMasCercana);

            latitudeActual =
                    entregaMasCercana.getLatitude();

            longitudeActual =
                    entregaMasCercana.getLongitude();

            pendientes.remove(entregaMasCercana);
        }

        return ruta;
    }

    private List<Entrega> mejorarRutaConDosOpt(
            List<Entrega> rutaOriginal,
            double latitudeInicial,
            double longitudeInicial
    ) {
        if (rutaOriginal.size() < 4) {
            return new ArrayList<>(rutaOriginal);
        }

        List<Entrega> mejorRuta =
                new ArrayList<>(rutaOriginal);

        double mejorDistancia =
                calcularDistanciaTotal(
                        mejorRuta,
                        latitudeInicial,
                        longitudeInicial
                );

        boolean huboMejora = true;

        int iteraciones = 0;
        int maximoIteraciones = 100;

        while (
                huboMejora &&
                iteraciones < maximoIteraciones
        ) {
            huboMejora = false;
            iteraciones++;

            for (
                    int inicio = 0;
                    inicio < mejorRuta.size() - 1;
                    inicio++
            ) {
                for (
                        int fin = inicio + 1;
                        fin < mejorRuta.size();
                        fin++
                ) {
                    List<Entrega> rutaCandidata =
                            invertirSegmento(
                                    mejorRuta,
                                    inicio,
                                    fin
                            );

                    double distanciaCandidata =
                            calcularDistanciaTotal(
                                    rutaCandidata,
                                    latitudeInicial,
                                    longitudeInicial
                            );

                    if (
                            distanciaCandidata
                                    < mejorDistancia - 0.001
                    ) {
                        mejorRuta = rutaCandidata;
                        mejorDistancia =
                                distanciaCandidata;
                        huboMejora = true;
                    }
                }
            }
        }

        return mejorRuta;
    }

    private List<Entrega> invertirSegmento(
            List<Entrega> ruta,
            int inicio,
            int fin
    ) {
        List<Entrega> nuevaRuta =
                new ArrayList<>(ruta);

        Collections.reverse(
                nuevaRuta.subList(
                        inicio,
                        fin + 1
                )
        );

        return nuevaRuta;
    }

    private List<ParadaRutaResponse>
    guardarRutaYCrearParadas(
            List<Entrega> ruta,
            double latitudeInicial,
            double longitudeInicial
    ) {
        List<ParadaRutaResponse> paradas =
                new ArrayList<>();

        double latitudeAnterior =
                latitudeInicial;

        double longitudeAnterior =
                longitudeInicial;

        for (
                int indice = 0;
                indice < ruta.size();
                indice++
        ) {
            Entrega entrega =
                    ruta.get(indice);

            int numeroParada =
                    indice + 1;

            double distanciaDesdeAnterior =
                    calcularDistanciaKm(
                            latitudeAnterior,
                            longitudeAnterior,
                            entrega.getLatitude(),
                            entrega.getLongitude()
                    );

            entrega.setOrdenRuta(
                    numeroParada
            );

            Entrega entregaGuardada =
                    entregaRepository.save(entrega);

            paradas.add(
                    new ParadaRutaResponse(
                            numeroParada,
                            redondear(
                                    distanciaDesdeAnterior
                            ),
                            convertirAResponse(
                                    entregaGuardada
                            )
                    )
            );

            latitudeAnterior =
                    entrega.getLatitude();

            longitudeAnterior =
                    entrega.getLongitude();
        }

        return paradas;
    }

    private double calcularDistanciaTotal(
            List<Entrega> ruta,
            double latitudeInicial,
            double longitudeInicial
    ) {
        if (ruta.isEmpty()) {
            return 0.0;
        }

        double distanciaTotal = 0.0;

        double latitudeAnterior =
                latitudeInicial;

        double longitudeAnterior =
                longitudeInicial;

        for (Entrega entrega : ruta) {

            distanciaTotal +=
                    calcularDistanciaKm(
                            latitudeAnterior,
                            longitudeAnterior,
                            entrega.getLatitude(),
                            entrega.getLongitude()
                    );

            latitudeAnterior =
                    entrega.getLatitude();

            longitudeAnterior =
                    entrega.getLongitude();
        }

        return distanciaTotal;
    }

    private boolean esEntregaPendiente(
            Entrega entrega
    ) {
        return (
                entrega.getEstado()
                        == Entrega.Estado.PENDIENTE
                ||
                entrega.getEstado()
                        == Entrega.Estado.EN_CAMINO
        );
    }

    private void colocarEntregasFinalizadasAlFinal(
            List<Entrega> todasLasEntregas,
            int siguienteOrden
    ) {
        List<Entrega> finalizadas =
                todasLasEntregas
                        .stream()
                        .filter(
                                entrega ->
                                        !esEntregaPendiente(
                                                entrega
                                        )
                        )
                        .toList();

        int orden = siguienteOrden;

        for (Entrega entrega : finalizadas) {

            entrega.setOrdenRuta(orden);

            entregaRepository.save(entrega);

            orden++;
        }
    }

    private void validarRepartidor(
            Long repartidorId
    ) {
        if (
                !repartidorRepository
                        .existsById(repartidorId)
        ) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el repartidor con id "
                            + repartidorId
            );
        }
    }

    private double calcularDistanciaKm(
            double latitudeOrigen,
            double longitudeOrigen,
            double latitudeDestino,
            double longitudeDestino
    ) {
        final double radioTierraKm =
                6371.0;

        double diferenciaLatitud =
                Math.toRadians(
                        latitudeDestino
                                - latitudeOrigen
                );

        double diferenciaLongitud =
                Math.toRadians(
                        longitudeDestino
                                - longitudeOrigen
                );

        double latitudOrigenRadianes =
                Math.toRadians(latitudeOrigen);

        double latitudDestinoRadianes =
                Math.toRadians(latitudeDestino);

        double valorA =
                Math.pow(
                        Math.sin(
                                diferenciaLatitud / 2
                        ),
                        2
                )
                +
                Math.cos(latitudOrigenRadianes)
                        *
                        Math.cos(latitudDestinoRadianes)
                        *
                        Math.pow(
                                Math.sin(
                                        diferenciaLongitud / 2
                                ),
                                2
                        );

        double valorC =
                2
                *
                Math.atan2(
                        Math.sqrt(valorA),
                        Math.sqrt(1 - valorA)
                );

        return radioTierraKm * valorC;
    }

    private double redondear(
            double valor
    ) {
        return Math.round(
                valor * 100.0
        ) / 100.0;
    }

    private EntregaResponse convertirAResponse(
            Entrega entrega
    ) {
        return new EntregaResponse(
                entrega.getId(),
                entrega.getCliente(),
                entrega.getTelefono(),
                entrega.getDireccion(),
                entrega.getReferencia(),
                entrega.getPrioridad(),
                entrega.getEstado(),
                entrega.getLatitude(),
                entrega.getLongitude(),
                entrega.getOrdenRuta(),
                entrega.getRepartidor().getId(),
                entrega.getFechaRegistro()
        );
    }
}