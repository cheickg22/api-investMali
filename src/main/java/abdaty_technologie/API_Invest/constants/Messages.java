package abdaty_technologie.API_Invest.constants;

public final class Messages {
    private Messages() {}

    // GlobalExceptionHandler helpers
    public static String invalidEnumValue(String field, String allowedValuesCsv) {
        return "Valeur invalide pour le champ '" + field + "'. Valeurs acceptées: [" + allowedValuesCsv + "]";
    }

    public static String invalidFieldFormat(String field) {
        return "Format invalide pour le champ '" + field + "'";
    }

    public static String invalidParamEnum(String param, String allowedValuesCsv) {
        return "Paramètre '" + param + "' invalide. Valeurs acceptées: [" + allowedValuesCsv + "]";
    }

    public static String invalidParamType(String param) {
        return "Paramètre '" + param + "' a un type invalide";
    }

    // Messages d'erreur d'authentification
    public static final String INVALID_CREDENTIALS = "Nom d'utilisateur ou mot de passe incorrect";
    public static final String UTILISATEUR_NON_TROUVE = "Utilisateur non trouvé";
    public static final String MOT_DE_PASSE_INCORRECT = "Mot de passe incorrect";
    public static final String ACCOUNT_DISABLED = "Compte utilisateur désactivé";
    
    // Messages d'erreur de données
    public static final String PERSON_NOT_FOUND = "Personne non trouvée avec l'ID: ";
    public static final String ENTERPRISE_NOT_FOUND = "Entreprise non trouvée avec l'ID: ";
    public static final String PAYMENT_NOT_FOUND = "Paiement non trouvé avec l'ID: ";
    public static final String PAYMENT_NOT_FOUND_BY_REFERENCE = "Paiement non trouvé avec la référence: ";
    public static final String USER_NOT_FOUND = "Utilisateur non trouvé avec l'ID: ";
    
    // Messages d'erreur de fichier
    public static final String FILE_EMPTY = "Le fichier est vide";
    public static final String FILE_MUST_BE_EXCEL = "Le fichier doit être au format Excel (.xlsx)";
    public static final String FILE_READ_ERROR = "Erreur lors de la lecture du fichier Excel: ";
    
    // Messages d'erreur système
    public static final String CLASS_CANNOT_BE_INSTANTIATED = "Cette classe ne peut pas être instanciée";
    public static final String ERROR_PREFIX = "Erreur: ";
    public static final String FILE_PROCESSING_ERROR = "Erreur lors du traitement du fichier: ";
    public static final String INTERNAL_ERROR = "Erreur interne: ";
    public static final String INTERNAL_SERVER_ERROR = "Erreur interne du serveur";
    public static final String AUTHENTICATION_SUCCESS = "Authentification fonctionnelle !";
    public static final String USER_NOT_FOUND_OR_ERROR = "Utilisateur non trouvé ou erreur: ";
    public static final String ERROR_RETRIEVING_USERS = "Erreur lors de la récupération des utilisateurs: ";
    public static final String PAYMENT_DELETED_SUCCESS = "Paiement supprimé avec succès";
    public static final String DELETION_ERROR = "Erreur lors de la suppression: ";
    public static final String CALCULATION_ERROR = "Erreur lors du calcul: ";
    public static final String PAYMENT_CREATION_ERROR = "Erreur lors de la création du paiement: ";
    public static final String PAYMENT_RETRIEVAL_ERROR = "Erreur lors de la récupération des paiements: ";
    public static final String PAYMENT_NOT_FOUND_ERROR = "Paiement non trouvé: ";
    public static final String VALIDATION_ERROR = "Erreur lors de la validation: ";
    public static final String REFUSAL_ERROR = "Erreur lors du refus: ";
    public static final String CANCELLATION_ERROR = "Erreur lors de l'annulation: ";
}
