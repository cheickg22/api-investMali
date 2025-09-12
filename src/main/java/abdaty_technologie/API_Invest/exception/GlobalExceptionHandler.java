package abdaty_technologie.API_Invest.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import abdaty_technologie.API_Invest.constants.Messages;

/**
 * Gestionnaire global des exceptions REST.
 *
 * Standardise les réponses d'erreur pour:
 * - BadRequestException (400)
 * - NotFoundException (404)
 * - MethodArgumentNotValidException (400) -> map champ -> message
 * - HttpMessageNotReadableException (400) -> détail enum/format invalide
 * - MethodArgumentTypeMismatchException (400) -> paramètres de requête invalides
 * - Exception générique (500)
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** 400 pour erreurs fonctionnelles */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /** 404 pour ressources non trouvées */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /** 400 pour erreurs de validation des DTOs (@Valid) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /** 400 pour corps JSON illisible: précise les valeurs enum acceptées, sinon format invalide */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();
            String path = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                    .collect(Collectors.joining("."));
            Object invalidValue = ife.getValue();

            // Message générique
            Map<String, Object> body = new HashMap<>();
            body.put("field", path);
            body.put("invalidValue", invalidValue);

            if (targetType != null && targetType.isEnum()) {
                String allowed = Arrays.stream(targetType.getEnumConstants())
                        .map(e -> ((Enum<?>) e).name())
                        .collect(Collectors.joining(", "));
                body.put("message", Messages.invalidEnumValue(path, allowed));
            } else {
                body.put("message", Messages.invalidFieldFormat(path));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /** 400 pour paramètres de requête (Query/Path) avec type invalide */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("param", ex.getName());
        body.put("invalidValue", ex.getValue());
        Class<?> required = ex.getRequiredType();
        if (required != null && required.isEnum()) {
            String allowed = Arrays.stream(required.getEnumConstants())
                    .map(e -> ((Enum<?>) e).name())
                    .collect(Collectors.joining(", "));
            body.put("message", Messages.invalidParamEnum(ex.getName(), allowed));
        } else {
            body.put("message", Messages.invalidParamType(ex.getName()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** 500 catch-all: message brut (à enrichir si besoin d'un code d'erreur) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}