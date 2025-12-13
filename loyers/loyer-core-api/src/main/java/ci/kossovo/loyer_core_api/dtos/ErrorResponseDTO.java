package ci.kossovo.loyer_core_api.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponseDTO(
    LocalDateTime timestamp,
    int status,
    String error, // Le code d'erreur HTTP (ex: "Not Found", "Bad Request")
    String message, // Un message d'erreur général
    String path, // L'URL qui a causé l'erreur
    Map<String, List<String>> validationErrors // Spécifique pour les erreurs de validation
    ) {
  // Constructeur simplifié pour les erreurs non liées à la validation
  public ErrorResponseDTO(int status, String error, String message, String path) {
    this(LocalDateTime.now(), status, error, message, path, null);
  }

  // Constructeur pour les erreurs de validation
  public ErrorResponseDTO(
      int status,
      String error,
      String message,
      String path,
      Map<String, List<String>> validationErrors) {
    this(LocalDateTime.now(), status, error, message, path, validationErrors);
  }
}
