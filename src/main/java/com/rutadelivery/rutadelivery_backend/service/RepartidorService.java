package com.rutadelivery.rutadelivery_backend.service;

import com.rutadelivery.rutadelivery_backend.dto.ActualizarPerfilRequest;
import com.rutadelivery.rutadelivery_backend.dto.CambiarContrasenaRequest;
import com.rutadelivery.rutadelivery_backend.dto.GoogleLoginRequest;
import com.rutadelivery.rutadelivery_backend.dto.LoginRequest;
import com.rutadelivery.rutadelivery_backend.dto.LoginResponse;
import com.rutadelivery.rutadelivery_backend.dto.RegistroRepartidorRequest;
import com.rutadelivery.rutadelivery_backend.dto.RepartidorResponse;
import com.rutadelivery.rutadelivery_backend.entity.Repartidor;
import com.rutadelivery.rutadelivery_backend.repository.RepartidorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SeguridadUsuarioService seguridadUsuarioService;
    private final GoogleTokenService googleTokenService;

    public RepartidorService(
            RepartidorRepository repartidorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SeguridadUsuarioService seguridadUsuarioService,
            GoogleTokenService googleTokenService
    ) {
        this.repartidorRepository = repartidorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.seguridadUsuarioService = seguridadUsuarioService;
        this.googleTokenService = googleTokenService;
    }

    @Transactional
    public RepartidorResponse registrar(
            RegistroRepartidorRequest request
    ) {
        String correoNormalizado =
                request.correo()
                        .trim()
                        .toLowerCase();

        if (
                repartidorRepository
                        .existsByCorreo(
                                correoNormalizado
                        )
        ) {
            throw new OperacionNoPermitidaException(
                    "Ya existe un repartidor registrado con este correo"
            );
        }

        Repartidor repartidor = new Repartidor();

        repartidor.setNombre(
                request.nombre().trim()
        );

        repartidor.setCorreo(
                correoNormalizado
        );

        repartidor.setContrasena(
                passwordEncoder.encode(
                        request.contrasena()
                )
        );

        repartidor.setTipoVehiculo(
                request.tipoVehiculo()
        );

        repartidor.setActivo(true);

        Repartidor guardado =
                repartidorRepository.save(
                        repartidor
                );

        return convertirAResponse(
                guardado
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(
            LoginRequest request
    ) {
        String correoNormalizado =
                request.correo()
                        .trim()
                        .toLowerCase();

        Repartidor repartidor =
                repartidorRepository
                        .findByCorreo(
                                correoNormalizado
                        )
                        .orElseThrow(() ->
                                new OperacionNoPermitidaException(
                                        "Correo o contraseña incorrectos"
                                )
                        );

        if (
                !Boolean.TRUE.equals(
                        repartidor.getActivo()
                )
        ) {
            throw new OperacionNoPermitidaException(
                    "La cuenta del repartidor está desactivada"
            );
        }

        boolean contrasenaCorrecta =
                passwordEncoder.matches(
                        request.contrasena(),
                        repartidor.getContrasena()
                );

        if (!contrasenaCorrecta) {
            throw new OperacionNoPermitidaException(
                    "Correo o contraseña incorrectos"
            );
        }

        String token =
                jwtService.generarToken(
                        repartidor
                );

        return new LoginResponse(
                "Inicio de sesión correcto",
                token,
                convertirAResponse(repartidor),
                false
        );
    }

    @Transactional
    public LoginResponse iniciarSesionGoogle(
            GoogleLoginRequest request
    ) {
        GoogleTokenService.GoogleUsuario usuarioGoogle =
                googleTokenService.verificar(
                        request.idToken()
                );

        Optional<Repartidor> existente =
                repartidorRepository
                        .findByCorreo(
                                usuarioGoogle.correo()
                        );

        boolean nuevoUsuario =
                existente.isEmpty();

        Repartidor repartidor;

        if (existente.isPresent()) {
            repartidor =
                    existente.get();
        } else {
            Repartidor nuevo =
                    new Repartidor();

            nuevo.setNombre(
                    usuarioGoogle.nombre()
            );

            nuevo.setCorreo(
                    usuarioGoogle.correo()
            );

            /*
             * El usuario Google no utiliza esta
             * contraseña. Se guarda una aleatoria
             * para mantener compatible la entidad.
             */
            nuevo.setContrasena(
                    passwordEncoder.encode(
                            UUID.randomUUID()
                                    .toString()
                    )
            );

            /*
             * Valor temporal únicamente hasta que
             * el usuario elija Moto o Carro.
             */
            nuevo.setTipoVehiculo(
                    Repartidor.TipoVehiculo.MOTO
            );

            nuevo.setActivo(
                    true
            );

            repartidor =
                    repartidorRepository.save(
                            nuevo
                    );
        }

        if (
                !Boolean.TRUE.equals(
                        repartidor.getActivo()
                )
        ) {
            throw new OperacionNoPermitidaException(
                    "La cuenta del repartidor está desactivada"
            );
        }

        String token =
                jwtService.generarToken(
                        repartidor
                );

        return new LoginResponse(
                "Inicio de sesión con Google correcto",
                token,
                convertirAResponse(repartidor),
                nuevoUsuario
        );
    }

    @Transactional(readOnly = true)
    public RepartidorResponse obtenerPorId(
            Long id
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        id
                );

        return convertirAResponse(
                obtenerEntidadPorId(id)
        );
    }

    @Transactional
    public RepartidorResponse actualizarPerfil(
            Long id,
            ActualizarPerfilRequest request
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        id
                );

        Repartidor repartidor =
                obtenerEntidadPorId(id);

        repartidor.setNombre(
                request.nombre()
                        .trim()
        );

        repartidor.setTipoVehiculo(
                request.tipoVehiculo()
        );

        return convertirAResponse(
                repartidorRepository.save(
                        repartidor
                )
        );
    }

    @Transactional
    public void cambiarContrasena(
            Long id,
            CambiarContrasenaRequest request
    ) {
        seguridadUsuarioService
                .validarRepartidorAutenticado(
                        id
                );

        Repartidor repartidor =
                obtenerEntidadPorId(id);

        boolean actualCorrecta =
                passwordEncoder.matches(
                        request.contrasenaActual(),
                        repartidor.getContrasena()
                );

        if (!actualCorrecta) {
            throw new OperacionNoPermitidaException(
                    "La contraseña actual es incorrecta"
            );
        }

        if (
                passwordEncoder.matches(
                        request.nuevaContrasena(),
                        repartidor.getContrasena()
                )
        ) {
            throw new OperacionNoPermitidaException(
                    "La nueva contraseña debe ser diferente a la actual"
            );
        }

        repartidor.setContrasena(
                passwordEncoder.encode(
                        request.nuevaContrasena()
                )
        );

        repartidorRepository.save(
                repartidor
        );
    }

    private Repartidor obtenerEntidadPorId(
            Long id
    ) {
        return repartidorRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró el repartidor con id "
                                        + id
                        )
                );
    }

    private RepartidorResponse convertirAResponse(
            Repartidor repartidor
    ) {
        return new RepartidorResponse(
                repartidor.getId(),
                repartidor.getNombre(),
                repartidor.getCorreo(),
                repartidor.getTipoVehiculo(),
                repartidor.getActivo()
        );
    }
}