package com.rutadelivery.rutadelivery_backend.controller;

import com.rutadelivery.rutadelivery_backend.service.ServicioExternoException;
import com.rutadelivery.rutadelivery_backend.service.OperacionNoPermitidaException;
import com.rutadelivery.rutadelivery_backend.service.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.rutadelivery.rutadelivery_backend.service.ArchivoNoValidoException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(ServicioExternoException.class)
        public ResponseEntity<Map<String, Object>> manejarServicioExterno(
                        ServicioExternoException exception) {
                return construirRespuesta(
                                HttpStatus.BAD_GATEWAY,
                                exception.getMessage());
        }

        @ExceptionHandler(ArchivoNoValidoException.class)
        public ResponseEntity<Map<String, Object>> manejarArchivoNoValido(
                        ArchivoNoValidoException exception) {
                return construirRespuesta(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage());
        }

        @ExceptionHandler(OperacionNoPermitidaException.class)
        public ResponseEntity<Map<String, Object>> manejarOperacionNoPermitida(
                        OperacionNoPermitidaException exception) {
                return construirRespuesta(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage());
        }

        @ExceptionHandler(RecursoNoEncontradoException.class)
        public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
                        RecursoNoEncontradoException exception) {
                return construirRespuesta(
                                HttpStatus.NOT_FOUND,
                                exception.getMessage());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> manejarValidaciones(
                        MethodArgumentNotValidException exception) {
                Map<String, String> errores = new LinkedHashMap<>();

                for (FieldError error : exception.getBindingResult().getFieldErrors()) {

                        errores.put(
                                        error.getField(),
                                        error.getDefaultMessage());
                }

                Map<String, Object> respuesta = new LinkedHashMap<>();

                respuesta.put("fecha", LocalDateTime.now());
                respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
                respuesta.put("mensaje", "Existen datos inválidos");
                respuesta.put("errores", errores);

                return ResponseEntity
                                .badRequest()
                                .body(respuesta);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> manejarErrorGeneral(
                        Exception exception) {
                exception.printStackTrace();

                return construirRespuesta(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocurrió un error interno en el servidor");
        }

        private ResponseEntity<Map<String, Object>> construirRespuesta(
                        HttpStatus estado,
                        String mensaje) {
                Map<String, Object> respuesta = new LinkedHashMap<>();

                respuesta.put("fecha", LocalDateTime.now());
                respuesta.put("estado", estado.value());
                respuesta.put("mensaje", mensaje);

                return ResponseEntity
                                .status(estado)
                                .body(respuesta);
        }
}