package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.client.AutenticacionClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @Autowired
    AutenticacionClient autenticacionClient;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {

        // validar campos de entrada
        if (loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
                loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
                loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0){
            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));
        }

        try {

            // consumir servicio backend de autenticacion
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {

                        if(response.codigo().equals("00")){
                            return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), ""));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida", "", ""));
                        }

                    });

        } catch(Exception e) {

            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", "", ""));

        }

    }

    @PostMapping("/logout")
    public Mono<LogoutResponseDTO> logoutUser (@RequestBody String nombreUsuario) {
        System.out.println("Consumiendo con Spring Cloud!");
        if (nombreUsuario.isBlank()){
            return Mono.just(new LogoutResponseDTO(false, null,"Error: Ocurrio un error al hacer la petecion de logout"));
        }
        try{
            ResponseEntity<LogoutResponseDTO> responseLogout = autenticacionClient.logout(nombreUsuario);
            if (responseLogout.getStatusCode().is2xxSuccessful()){
                LogoutResponseDTO logoutResponseDTO = responseLogout.getBody();
                if(logoutResponseDTO.resultado()) {
                    return Mono.just(logoutResponseDTO);
                }else{
                    return Mono.just(new LogoutResponseDTO(false,null,"Error: Logout Fallo"));
                }
            }else{
                return  Mono.just(new LogoutResponseDTO(false, null, "Error: Logout Fallo"));
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
            throw new  RuntimeException(e.getMessage());
        }
    }
}
