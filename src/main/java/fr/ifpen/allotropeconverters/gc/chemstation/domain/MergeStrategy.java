package fr.ifpen.allotropeconverters.gc.chemstation.domain;

/** Politique de fusion en cas de conflit entre valeurs provenant du .ch et d'autres fichiers (Result.xml, etc.). */
public enum MergeStrategy {
    /** Utiliser la valeur issue du .ch */
    USE_CH_FILE,
    /** Utiliser la valeur issue des autres fichiers (Result.xml, ...). */
    USE_OTHER_FILES,
    /** Lever une erreur si les valeurs diffèrent. */
    ERROR
}
