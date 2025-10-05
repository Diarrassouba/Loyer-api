package ci.kossovo.immobilier_rest_api.exceptions;

import ci.kossovo.immobilier_rest_api.dtos.ErrorMaisonResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
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

@RestControllerAdvice
public class GlobalMaisonExceptionHandler {

  /**
   * Gère les exceptions lorsqu'une entité n'est pas trouvée (ex: findById échoue). Renvoie un
   * statut 404 Not Found.
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorMaisonResponseDTO> handleEntityNotFoundException(
      EntityNotFoundException ex, HttpServletRequest request) {

    ErrorMaisonResponseDTO errorResponse =
        new ErrorMaisonResponseDTO(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Gère les exceptions de validation des DTOs annotés avec @Valid. Renvoie un statut 400 Bad
   * Request avec les détails des champs invalides.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorMaisonResponseDTO> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, List<String>> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
            });

    ErrorMaisonResponseDTO errorResponse =
        new ErrorMaisonResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "La validation de la requête a échoué",
            request.getRequestURI(),
            errors);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Gère les arguments illégaux, typiquement pour les validations métier manuelles. Renvoie un
   * statut 400 Bad Request.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorMaisonResponseDTO> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {

    ErrorMaisonResponseDTO errorResponse =
        new ErrorMaisonResponseDTO(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Gère toutes les autres exceptions non capturées pour éviter de fuiter des traces de pile.
   * Renvoie un statut 500 Internal Server Error. En production, il faudrait loguer l'exception 'ex'
   * ici.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorMaisonResponseDTO> handleGenericException(
      Exception ex, HttpServletRequest request) {

    // Log l'exception pour le débogage
    // log.error("Une erreur inattendue est survenue", ex);

    ErrorMaisonResponseDTO errorResponse =
        new ErrorMaisonResponseDTO(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Une erreur interne est survenue. Veuillez contacter le support.",
            request.getRequestURI());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
