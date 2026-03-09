package ci.kossovo.financial_command_service.exception;

import ci.kossovo.loyer_core_api.dtos.ErrorResponseDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Gère les exceptions de validation métier lancées par l'Agrégat. Celles-ci sont typiquement des
   * `IllegalArgumentException` ou `IllegalStateException`. Elles sont propagées via le
   * CompletableFuture du CommandGateway. Renvoie un statut 400 Bad Request.
   */
  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponseDTO> handleAggregateBusinessException(
      RuntimeException ex, WebRequest request) {

    ErrorResponseDTO errorResponse =
        new ErrorResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(), // Le message vient directement de l'agrégat
            request.getDescription(false));
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /** Gère les exceptions de validation des DTOs (@Valid). Renvoie un statut 400 Bad Request. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {

    Map<String, List<String>> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
            });

    ErrorResponseDTO errorResponse =
        new ErrorResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "La validation de la requête a échoué",
            request.getDescription(false),
            errors);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Gère toutes les autres exceptions non capturées. Renvoie un statut 500 Internal Server Error.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, WebRequest request) {

    // logger.error("Erreur inattendue dans le command-service", ex);

    ErrorResponseDTO errorResponse =
        new ErrorResponseDTO(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Une erreur interne est survenue lors du traitement de la commande.",
            request.getDescription(false));
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
