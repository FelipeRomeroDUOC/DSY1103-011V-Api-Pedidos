package cl.apipedidos.authservice.service;

import cl.apipedidos.authservice.dto.LoginRequestDTO;
import cl.apipedidos.authservice.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}
