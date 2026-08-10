package com.rutadelivery.rutadelivery_backend.service;

import tools.jackson.databind.JsonNode;
import com.rutadelivery.rutadelivery_backend.config.GoogleRoutesProperties;
import com.rutadelivery.rutadelivery_backend.dto.CalcularRecorridoRequest;
import com.rutadelivery.rutadelivery_backend.dto.RecorridoRealResponse;
import com.rutadelivery.rutadelivery_backend.entity.Entrega;
import com.rutadelivery.rutadelivery_backend.repository.EntregaRepository;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleRoutesService {

    private static final String FIELD_MASK =
            "routes.distanceMeters," +
            "routes.duration," +
            "routes.polyline.encodedPolyline";

    private static final int MAXIMO_INTERMEDIOS = 25;

    private final GoogleRoutesProperties properties;
    private final EntregaRepository entregaRepository;
    private final RepartidorRepository repartidorRepository;
    private final SeguridadUsuarioService seguridadUsuarioService;
    private final RestClient restClient;

    public GoogleRoutesService(
            GoogleRoutesProperties properties,
            EntregaRepository entregaRepository,
            RepartidorRepository repartidorRepository,
            SeguridadUsuarioService seguridadUsuarioService
    ) {
        this.properties = properties;
        this.entregaRepository = entregaRepository;
        this.repartidorRepository = repartidorRepository;
        this.seguridadUsuarioService = seguridadUsuarioService;
        this.restClient = RestClient.create();
    }

    @Transactional(readOnly = true)
    public RecorridoRealResponse calcularRecorridoReal(
            Long repartidorId,
            CalcularRecorridoRequest request
    ) {
        /*
         * El repartidor indicado en la URL debe ser
         * el mismo que está firmado dentro del JWT.
         */
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        repartidorId
                );

        validarConfiguracion();
        validarRepartidor(repartidorId);

        List<Entrega> entregasPendientes =
                entregaRepository
                        .findByRepartidorIdOrderByOrdenRutaAsc(
                                repartidorId
                        )
                        .stream()
                        .filter(this::esPendiente)
                        .toList();

        if (entregasPendientes.isEmpty()) {
            return new RecorridoRealResponse(
                    "No existen entregas pendientes",
                    repartidorId,
                    0,
                    0L,
                    0.0,
                    0L,
                    0,
                    ""
            );
        }

        /*
         * Google admite:
         *
         * 1 origen
         * hasta 25 puntos intermedios
         * 1 destino
         *
         * Esto permite hasta 26 entregas:
         * 25 intermedias + 1 destino.
         */
        if (
                entregasPendientes.size()
                        > MAXIMO_INTERMEDIOS + 1
        ) {
            throw new OperacionNoPermitidaException(
                    "Google Routes permite como máximo " +
                            (MAXIMO_INTERMEDIOS + 1) +
                            " entregas por recorrido."
            );
        }

        Map<String, Object> cuerpo =
                construirCuerpoSolicitud(
                        request,
                        entregasPendientes
                );

        JsonNode respuestaGoogle =
                ejecutarSolicitudGoogle(cuerpo);

        return convertirRespuesta(
                repartidorId,
                entregasPendientes.size(),
                respuestaGoogle
        );
    }

    private Map<String, Object> construirCuerpoSolicitud(
            CalcularRecorridoRequest request,
            List<Entrega> entregas
    ) {
        Map<String, Object> cuerpo =
                new LinkedHashMap<>();

        cuerpo.put(
                "origin",
                crearWaypoint(
                        request.latitudeInicial(),
                        request.longitudeInicial()
                )
        );

        Entrega destino =
                entregas.get(
                        entregas.size() - 1
                );

        cuerpo.put(
                "destination",
                crearWaypoint(
                        destino.getLatitude(),
                        destino.getLongitude()
                )
        );

        if (entregas.size() > 1) {
            List<Map<String, Object>> intermediarios =
                    new ArrayList<>();

            for (
                    int indice = 0;
                    indice < entregas.size() - 1;
                    indice++
            ) {
                Entrega entrega =
                        entregas.get(indice);

                intermediarios.add(
                        crearWaypoint(
                                entrega.getLatitude(),
                                entrega.getLongitude()
                        )
                );
            }

            cuerpo.put(
                    "intermediates",
                    intermediarios
            );
        }

        cuerpo.put(
                "travelMode",
                "DRIVE"
        );

        cuerpo.put(
                "routingPreference",
                "TRAFFIC_AWARE"
        );

        cuerpo.put(
                "computeAlternativeRoutes",
                false
        );

        cuerpo.put(
                "polylineQuality",
                "HIGH_QUALITY"
        );

        cuerpo.put(
                "polylineEncoding",
                "ENCODED_POLYLINE"
        );

        cuerpo.put(
                "languageCode",
                "es-419"
        );

        cuerpo.put(
                "units",
                "METRIC"
        );

        Map<String, Object> modificadores =
                new LinkedHashMap<>();

        modificadores.put(
                "avoidTolls",
                false
        );

        modificadores.put(
                "avoidHighways",
                false
        );

        modificadores.put(
                "avoidFerries",
                false
        );

        cuerpo.put(
                "routeModifiers",
                modificadores
        );

        return cuerpo;
    }

    private Map<String, Object> crearWaypoint(
            Double latitude,
            Double longitude
    ) {
        Map<String, Object> latLng =
                new LinkedHashMap<>();

        latLng.put(
                "latitude",
                latitude
        );

        latLng.put(
                "longitude",
                longitude
        );

        Map<String, Object> location =
                new LinkedHashMap<>();

        location.put(
                "latLng",
                latLng
        );

        Map<String, Object> waypoint =
                new LinkedHashMap<>();

        waypoint.put(
                "location",
                location
        );

        return waypoint;
    }

    private JsonNode ejecutarSolicitudGoogle(
            Map<String, Object> cuerpo
    ) {
        try {
            JsonNode respuesta =
                    restClient
                            .post()
                            .uri(properties.getUrl())
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .header(
                                    "X-Goog-Api-Key",
                                    properties.getApiKey()
                            )
                            .header(
                                    "X-Goog-FieldMask",
                                    FIELD_MASK
                            )
                            .body(cuerpo)
                            .retrieve()
                            .body(JsonNode.class);

            if (respuesta == null) {
                throw new ServicioExternoException(
                        "Google Routes devolvió una respuesta vacía."
                );
            }

            return respuesta;

        } catch (
                RestClientResponseException exception
        ) {
            String detalle =
                    extraerMensajeErrorGoogle(
                            exception.getResponseBodyAsString()
                    );

            System.err.println(
                    "Error Google Routes: " +
                            exception.getStatusCode() +
                            " - " +
                            exception.getResponseBodyAsString()
            );

            throw new ServicioExternoException(
                    "Google Routes rechazó la solicitud: " +
                            detalle,
                    exception
            );

        } catch (
                ServicioExternoException exception
        ) {
            throw exception;

        } catch (Exception exception) {
            exception.printStackTrace();

            throw new ServicioExternoException(
                    "No fue posible comunicarse con Google Routes.",
                    exception
            );
        }
    }

    private RecorridoRealResponse convertirRespuesta(
            Long repartidorId,
            int totalParadas,
            JsonNode respuestaGoogle
    ) {
        JsonNode rutas =
                respuestaGoogle.path("routes");

        if (
                !rutas.isArray() ||
                rutas.isEmpty()
        ) {
            throw new ServicioExternoException(
                    "Google Routes no encontró un recorrido disponible."
            );
        }

        JsonNode ruta =
                rutas.get(0);

        long distanciaMetros =
                ruta.path(
                        "distanceMeters"
                ).asLong(0L);

        String duracionTexto =
                ruta.path(
                        "duration"
                ).asText("0s");

        long duracionSegundos =
                convertirDuracionASegundos(
                        duracionTexto
                );

        String polyline =
                ruta.path("polyline")
                        .path("encodedPolyline")
                        .asText("");

        if (polyline.isBlank()) {
            throw new ServicioExternoException(
                    "Google Routes no devolvió la línea del recorrido."
            );
        }

        double distanciaKilometros =
                Math.round(
                        distanciaMetros / 10.0
                ) / 100.0;

        int duracionMinutos =
                (int) Math.ceil(
                        duracionSegundos / 60.0
                );

        return new RecorridoRealResponse(
                "Recorrido real calculado correctamente",
                repartidorId,
                totalParadas,
                distanciaMetros,
                distanciaKilometros,
                duracionSegundos,
                duracionMinutos,
                polyline
        );
    }

    private long convertirDuracionASegundos(
            String duracion
    ) {
        if (
                duracion == null ||
                duracion.isBlank()
        ) {
            return 0L;
        }

        try {
            String valor =
                    duracion
                            .trim()
                            .replace(
                                    "s",
                                    ""
                            );

            double segundos =
                    Double.parseDouble(valor);

            return (long) Math.ceil(
                    segundos
            );

        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String extraerMensajeErrorGoogle(
            String contenido
    ) {
        if (
                contenido == null ||
                contenido.isBlank()
        ) {
            return "respuesta sin detalles";
        }

        try {
            /*
             * Evitamos depender de otra instancia
             * de ObjectMapper y buscamos el mensaje
             * básico devuelto por Google.
             */
            int posicionMensaje =
                    contenido.indexOf(
                            "\"message\""
                    );

            if (posicionMensaje < 0) {
                return contenido;
            }

            int inicio =
                    contenido.indexOf(
                            ":",
                            posicionMensaje
                    );

            int primeraComilla =
                    contenido.indexOf(
                            "\"",
                            inicio
                    );

            int segundaComilla =
                    contenido.indexOf(
                            "\"",
                            primeraComilla + 1
                    );

            if (
                    primeraComilla >= 0 &&
                    segundaComilla > primeraComilla
            ) {
                return contenido.substring(
                        primeraComilla + 1,
                        segundaComilla
                );
            }

            return contenido;

        } catch (Exception exception) {
            return contenido;
        }
    }

    private boolean esPendiente(
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

    private void validarRepartidor(
            Long repartidorId
    ) {
        if (
                !repartidorRepository
                        .existsById(repartidorId)
        ) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el repartidor con id " +
                            repartidorId
            );
        }
    }

    private void validarConfiguracion() {
        if (!properties.tieneApiKey()) {
            throw new ServicioExternoException(
                    "La variable GOOGLE_MAPS_ROUTES_API_KEY no está configurada."
            );
        }

        if (
                properties.getUrl() == null ||
                properties.getUrl().isBlank()
        ) {
            throw new ServicioExternoException(
                    "La URL de Google Routes no está configurada."
            );
        }
    }
}